/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


import java.util.zip.Inflater;

import org.eclipse.photran.internal.core.parser.Parser.Nonterminal;
import org.eclipse.photran.internal.core.parser.Parser.Production;

@SuppressWarnings("all")
abstract class ParsingTables
{
    // Constants used for accessing both the ACTION table and the error recovery table
    public static final int ACTION_MASK   = 0xC000;  // 1100 0000 0000 0000
    public static final int VALUE_MASK    = 0x3FFF;  // 0011 1111 1111 1111

    // Constants used for accessing the ACTION table
    public static final int SHIFT_ACTION  = 0x8000;  // 1000 0000 0000 0000
    public static final int REDUCE_ACTION = 0x4000;  // 0100 0000 0000 0000
    public static final int ACCEPT_ACTION = 0xC000;  // 1100 0000 0000 0000

    // Constants used for accessing the error recovery table
    public static final int DISCARD_STATE_ACTION    = 0x0000;  // 0000 0000 0000 0000
    public static final int DISCARD_TERMINAL_ACTION = 0x8000;  // 1000 0000 0000 0000
    public static final int RECOVER_ACTION          = 0x4000;  // 0100 0000 0000 0000

    public abstract int getActionCode(int state, org.eclipse.photran.internal.core.lexer.Token lookahead);
    public abstract int getActionCode(int state, int lookaheadTokenIndex);
    public abstract int getGoTo(int state, Nonterminal nonterminal);
    public abstract int getRecoveryCode(int state, org.eclipse.photran.internal.core.lexer.Token lookahead);

    protected static final int base64Decode(byte[] decodeIntoBuffer, String encodedString)
    {
        int[] encodedBuffer = new int[4];
        int bytesDecoded = 0;
        int inputLength = encodedString.length();

        if (inputLength % 4 != 0) throw new IllegalArgumentException("Invalid Base64-encoded data (wrong length)");

        for (int inputOffset = 0; inputOffset < inputLength; inputOffset += 4)
        {
            int padding = 0;

            for (int i = 0; i < 4; i++)
            {
                char value = encodedString.charAt(inputOffset + i);
                if (value >= 'A' && value <= 'Z')
                    encodedBuffer[i] = value - 'A';
                else if (value >= 'a' && value <= 'z')
                    encodedBuffer[i] = value - 'a' + 26;
                else if (value >= '0' && value <= '9')
                    encodedBuffer[i] = value - '0' + 52;
                else if (value == '+')
                    encodedBuffer[i] = 62;
                else if (value == '/')
                    encodedBuffer[i] = 63;
                else if (value == '=')
                    { encodedBuffer[i] = 0; padding++; }
                else throw new IllegalArgumentException("Invalid character " + value + " in Base64-encoded data");
            }

            assert 0 <= padding && padding <= 2;

            decodeIntoBuffer[bytesDecoded+0] = (byte)(  ((encodedBuffer[0] & 0x3F) <<  2)
                                                      | ((encodedBuffer[1] & 0x30) >>> 4));
            if (padding < 2)
               decodeIntoBuffer[bytesDecoded+1] = (byte)(  ((encodedBuffer[1] & 0x0F) <<  4)
                                                         | ((encodedBuffer[2] & 0x3C) >>> 2));

            if (padding < 1)
               decodeIntoBuffer[bytesDecoded+2] = (byte)(  ((encodedBuffer[2] & 0x03) <<  6)
                                                         |  (encodedBuffer[3] & 0x3F));

            bytesDecoded += (3 - padding);
        }

        return bytesDecoded;
    }
}

@SuppressWarnings("all")
final class ExecutableProgramParsingTables extends ParsingTables
{
    private static ExecutableProgramParsingTables instance = null;

    public static ExecutableProgramParsingTables getInstance()
    {
        if (instance == null)
            instance = new ExecutableProgramParsingTables();
        return instance;
    }

    @Override
    public int getActionCode(int state, org.eclipse.photran.internal.core.lexer.Token lookahead)
    {
        return ActionTable.getActionCode(state, lookahead);
    }

    @Override
    public int getActionCode(int state, int lookaheadTokenIndex)
    {
        return ActionTable.get(state, lookaheadTokenIndex);
    }

    @Override
    public int getGoTo(int state, Nonterminal nonterminal)
    {
        return GoToTable.getGoTo(state, nonterminal);
    }

    @Override
    public int getRecoveryCode(int state, org.eclipse.photran.internal.core.lexer.Token lookahead)
    {
        return RecoveryTable.getRecoveryCode(state, lookahead);
    }

    /**
     * The ACTION table.
     * <p>
     * The ACTION table maps a state and an input symbol to one of four
     * actions: shift, reduce, accept, or error.
     */
    protected static final class ActionTable
    {
        /**
         * Returns the action the parser should take if it is in the given state
         * and has the given symbol as its lookahead.
         * <p>
         * The result value should be interpreted as follows:
         * <ul>
         *   <li> If <code>result & ACTION_MASK == SHIFT_ACTION</code>,
         *        shift the terminal and go to state number
         *        <code>result & VALUE_MASK</code>.
         *   <li> If <code>result & ACTION_MASK == REDUCE_ACTION</code>,
         *        reduce by production number <code>result & VALUE_MASK</code>.
         *   <li> If <code>result & ACTION_MASK == ACCEPT_ACTION</code>,
         *        parsing has completed successfully.
         *   <li> Otherwise, a syntax error has been found.
         * </ul>
         *
         * @return a code for the action to take (see above)
         */
        protected static int getActionCode(int state, org.eclipse.photran.internal.core.lexer.Token lookahead)
        {
            assert 0 <= state && state < Parser.NUM_STATES;
            assert lookahead != null;

            Integer index = Parser.terminalIndices.get(lookahead.getTerminal());
            if (index == null)
                return 0;
            else
                return get(state, index);
        }

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 19, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 110, 189, 190, 0, 191, 192, 101, 29, 1, 35, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 57, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 57, 3, 1, 8, 123, 4, 124, 15, 5, 127, 125, 221, 237, 6, 7, 128, 126, 0, 173, 238, 206, 212, 8, 214, 239, 215, 88, 29, 9, 216, 217, 219, 218, 101, 29, 114, 10, 220, 11, 240, 222, 12, 227, 13, 0, 14, 228, 2, 129, 230, 150, 231, 241, 242, 15, 16, 243, 29, 244, 245, 17, 246, 247, 30, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 259, 137, 260, 261, 262, 263, 22, 23, 264, 265, 24, 266, 267, 3, 25, 268, 269, 270, 26, 27, 152, 154, 28, 243, 271, 272, 237, 241, 273, 274, 4, 275, 276, 39, 29, 39, 244, 277, 278, 279, 0, 88, 39, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 56, 291, 30, 292, 293, 156, 6, 294, 295, 296, 245, 297, 298, 299, 238, 300, 301, 103, 302, 7, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 29, 39, 31, 313, 314, 315, 32, 316, 5, 317, 318, 33, 319, 320, 0, 1, 2, 321, 322, 323, 29, 34, 324, 239, 325, 144, 326, 327, 328, 57, 8, 329, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 330, 255, 256, 331, 242, 9, 173, 10, 332, 35, 333, 334, 88, 335, 257, 336, 337, 338, 258, 180, 250, 259, 339, 340, 341, 263, 265, 342, 343, 101, 344, 345, 346, 347, 348, 349, 11, 36, 37, 350, 12, 13, 14, 15, 0, 351, 352, 16, 17, 18, 38, 19, 39, 20, 267, 40, 41, 21, 22, 23, 353, 354, 0, 355, 24, 356, 26, 28, 31, 42, 357, 358, 359, 360, 361, 362, 363, 32, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 33, 34, 393, 394, 395, 396, 43, 44, 397, 45, 46, 35, 36, 398, 37, 38, 40, 41, 399, 400, 47, 401, 48, 49, 402, 50, 51, 52, 53, 54, 1, 403, 404, 405, 406, 407, 55, 56, 58, 2, 59, 60, 408, 61, 3, 62, 409, 63, 64, 65, 0, 410, 66, 411, 67, 68, 4, 47, 412, 69, 70, 413, 71, 6, 414, 3, 415, 4, 48, 72, 73, 5, 416, 417, 6, 418, 419, 74, 420, 421, 75, 76, 7, 422, 77, 78, 423, 49, 50, 79, 8, 424, 80, 81, 425, 82, 426, 427, 1, 428, 429, 430, 431, 432, 433, 123, 83, 84, 85, 434, 435, 86, 9, 87, 53, 88, 89, 10, 0, 90, 8, 11, 91, 436, 92, 93, 437, 12, 94, 95, 1, 96, 97, 98, 13, 99, 14, 0, 100, 438, 102, 104, 105, 106, 107, 439, 108, 109, 110, 440, 111, 112, 113, 441, 114, 442, 443, 444, 116, 15, 445, 446, 447, 448, 449, 450, 451, 117, 118, 452, 119, 453, 120, 17, 121, 181, 454, 455, 8, 456, 122, 123, 19, 124, 126, 457, 458, 459, 460, 127, 129, 130, 20, 131, 21, 132, 15, 133, 134, 461, 22, 462, 463, 464, 128, 465, 466, 467, 468, 135, 136, 0, 54, 137, 138, 139, 140, 141, 469, 142, 23, 470, 471, 472, 473, 143, 55, 116, 145, 146, 147, 148, 474, 475, 476, 149, 150, 151, 152, 24, 8, 153, 477, 478, 479, 480, 481, 482, 101, 483, 484, 154, 485, 486, 155, 56, 487, 488, 156, 489, 490, 491, 492, 493, 157, 494, 495, 251, 496, 497, 173, 169, 158, 498, 499, 500, 501, 502, 159, 503, 504, 160, 505, 506, 507, 508, 2, 509, 510, 56, 161, 511, 162, 512, 513, 514, 515, 516, 163, 517, 518, 519, 520, 521, 164, 165, 522, 523, 524, 101, 170, 525, 526, 166, 527, 167, 528, 529, 530, 531, 15, 260, 25, 532, 168, 533, 261, 534, 262, 535, 268, 536, 270, 171, 19, 172, 174, 175, 26, 537, 176, 538, 539, 177, 540, 271, 541, 542, 543, 15, 276, 544, 7, 8, 57, 9, 10, 178, 545, 546, 11, 547, 548, 549, 16, 143, 550, 17, 277, 551, 58, 0, 3, 552, 553, 554, 555, 556, 1, 557, 280, 3, 558, 285, 559, 560, 561, 562, 563, 564, 565, 21, 566, 148, 567, 568, 569, 27, 168, 570, 571, 174, 572, 573, 29, 574, 32, 18, 575, 576, 577, 179, 180, 578, 181, 579, 182, 580, 183, 581, 582, 583, 584, 585, 586, 587, 588, 39, 589, 590, 591, 592, 593, 594, 43, 595, 44, 45, 46, 596, 597, 598, 599, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 610, 611, 612, 613, 614, 47, 615, 616, 617, 618, 619, 620, 621, 622, 623, 624, 625, 626, 627, 628, 629, 630, 631, 48, 49, 63, 66, 59, 632, 50, 77, 633, 634, 4, 635, 184, 636, 637, 185, 638, 639, 640, 641, 5, 642, 643, 6, 644, 12, 14, 645, 646, 647, 27, 648, 649, 650, 186, 651, 652, 187, 188, 653, 78, 654, 655, 656, 657, 658, 659, 189, 190, 660, 191, 661, 182, 662, 192, 15, 663, 664, 665, 666, 667, 668, 80, 81, 669, 670, 671, 82, 672, 87, 88, 94, 95, 193, 673, 100, 674, 675, 2, 676, 101, 102, 103, 677, 678, 194, 679, 680, 112, 114, 115, 117, 118, 60, 681, 682, 683, 684, 33, 19, 685, 686, 687, 119, 7, 20, 23, 688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 125, 4, 703, 704, 705, 133, 135, 134, 706, 136, 195, 61, 143, 144, 145, 147, 707, 148, 153, 154, 708, 155, 156, 157, 709, 6, 158, 159, 160, 196, 197, 62, 198, 199, 710, 64, 65, 184, 67, 68, 69, 711, 712, 8, 9, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 28, 29, 30, 723, 724, 725, 726, 727, 728, 729, 730, 731, 732, 733, 734, 179, 735, 736, 737, 738, 739, 740, 741, 742, 743, 161, 744, 162, 745, 746, 747, 163, 748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 758, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, 769, 770, 771, 772, 24, 25, 26, 31, 773, 774, 775, 776, 777, 164, 778, 165, 779, 166, 207, 167, 780, 200, 781, 201, 782, 783, 168, 784, 34, 785, 786, 787, 788, 789, 210, 790, 169, 791, 792, 793, 794, 795, 796, 797, 798, 799, 170, 800, 801, 802, 803, 173, 804, 805, 806, 807, 808, 10, 809, 810, 811, 812, 813, 814, 815, 816, 70, 7, 176, 177, 817, 818, 819, 820, 821, 822, 823, 824, 825, 826, 178, 35, 184, 185, 827, 186, 187, 202, 1, 188, 71, 189, 190, 191, 193, 195, 73, 196, 197, 198, 199, 203, 204, 205, 207, 828, 829, 208, 830, 831, 0, 832, 35, 32, 833, 834, 835, 209, 210, 211, 74, 212, 75, 290, 836, 43, 837, 213, 214, 215, 217, 218, 220, 221, 838, 222, 203, 839, 204, 840, 841, 842, 843, 844, 35, 223, 76, 845, 846, 224, 225, 8, 847, 225, 226, 227, 848, 77, 849, 275, 850, 228, 229, 230, 231, 851, 852, 291, 853, 205, 854, 232, 233, 234, 855, 856, 206, 207, 857, 209, 858, 859, 210, 860, 861, 862, 863, 211, 864, 865, 45, 213, 214, 866, 867, 219, 215, 868, 869, 870, 871, 217, 872, 220, 873, 874, 875, 44, 221, 876, 222, 877, 878, 879, 78, 235, 236, 880, 881, 35, 79, 46, 83, 84, 47, 50, 85, 51, 86, 882, 52, 238, 237, 239, 883, 884, 223, 885, 240, 224, 886, 887, 888, 225, 889, 57, 88, 36, 244, 247, 37, 294, 101, 226, 890, 38, 891, 227, 892, 893, 248, 894, 895, 896, 1, 39, 242, 250, 2, 40, 251, 80, 254, 256, 41, 257, 897, 301, 898, 243, 53, 899, 228, 900, 901, 249, 253, 258, 229, 902, 903, 231, 904, 905, 232, 906, 907, 233, 908, 81, 246, 259, 264, 54, 265, 267, 0, 234, 268, 269, 270, 271, 272, 235, 909, 910, 911, 273, 274, 55, 56, 59, 60, 61, 62, 64, 65, 67, 68, 69, 70, 71, 74, 278, 276, 279, 280, 281, 282, 283, 284, 285, 286, 287, 1, 912, 288, 289, 290, 291, 292, 293, 913, 294, 914, 915, 295, 296, 916, 917, 297, 298, 918, 299, 300, 301, 919, 302, 303, 920, 921, 42, 75, 922, 923, 924, 925, 926, 927, 928, 929, 930, 931, 304, 305, 932, 933, 934, 935, 936, 937, 938, 939, 940, 941, 942, 943, 944, 945, 946, 306, 947, 236, 0, 948, 307, 949, 308, 950, 309, 951, 76, 952, 953, 954, 238, 245, 310, 311, 240, 312, 297, 313, 314, 955, 956, 315, 316, 317, 318, 241, 957, 319, 320, 321, 322, 324, 110, 325, 327, 43, 329, 958, 323, 326, 328, 330, 331, 959, 333, 960, 961, 962, 250, 334, 336, 337, 338, 963, 964, 965, 339, 966, 340, 341, 44, 335, 89, 342, 343, 344, 345, 346, 90, 91, 967, 347, 968, 251, 969, 348, 349, 970, 350, 353, 361, 362, 2, 971, 972, 365, 367, 368, 375, 82, 381, 973, 392, 383, 386, 387, 388, 393, 395, 89, 396, 397, 310, 398, 400, 313, 401, 974, 975, 402, 403, 976, 977, 405, 978, 979, 980, 981, 404, 982, 407, 11, 983, 984, 408, 410, 92, 93, 96, 412, 90, 985, 986, 987, 252, 91, 254, 988, 989, 990, 406, 991, 992, 3, 993, 994, 995, 996, 92, 997, 97, 998, 999, 1000, 409, 1001, 4, 1002, 1003, 413, 1004, 1005, 96, 6, 1006, 1007, 1008, 98, 1009, 1010, 1011, 1012, 259, 1013, 1014, 260, 97, 98, 1015, 261, 1016, 414, 416, 419, 420, 421, 422, 423, 45, 0, 425, 1, 426, 2, 427, 428, 46, 429, 99, 2, 47, 430, 431, 433, 434, 435, 99, 436, 437, 438, 439, 440, 441, 442, 444, 445, 446, 447, 448, 450, 452, 453, 454, 455, 456, 458, 3, 262, 459, 460, 461, 462, 463, 464, 465, 466, 467, 469, 470, 471, 472, 473, 432, 474, 263, 475, 264, 476, 477, 479, 1017, 112, 484, 485, 486, 4, 265, 478, 480, 487, 481, 5, 489, 1018, 491, 482, 267, 269, 483, 488, 490, 492, 493, 494, 495, 1019, 270, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 1020, 1021, 510, 511, 1022, 1023, 1024, 271, 512, 513, 3, 114, 115, 514, 1025, 515, 1026, 1027, 1028, 1, 4, 516, 517, 116, 100, 518, 519, 1029, 520, 521, 114, 48, 1030, 1031, 522, 523, 524, 1032, 272, 1033, 1034, 277, 525, 1035, 279, 7, 1036, 1037, 281, 1038, 1039, 1040, 526, 117, 527, 528, 1041, 530, 532, 1042, 282, 1043, 529, 303, 1044, 534, 1045, 283, 284, 535, 537, 538, 1046, 1047, 1048, 1049, 536, 1050, 1051, 1052, 285, 1053, 1054, 118, 1055, 0, 1056, 1057, 1058, 286, 1059, 1060, 1061, 1062, 1063, 1064, 120, 101, 102, 103, 121, 123, 124, 1065, 127, 129, 130, 131, 1066, 1067, 104, 1068, 1069, 49, 1070, 1071, 312, 1072, 539, 540, 541, 542, 543, 544, 545, 315, 1073, 132, 1074, 1075, 5, 546, 547, 50, 548, 137, 549, 105, 123, 51, 1076, 52, 1077, 550, 551, 124, 552, 1078, 1079, 318, 1080, 287, 1081, 1082, 553, 1083, 554, 555, 1084, 556, 1085, 1086, 289, 106, 1087, 107, 557, 558, 559, 560, 561, 565, 562, 1088, 563, 564, 566, 567, 1089, 568, 569, 570, 1090, 571, 1091, 573, 1092, 1093, 572, 1094, 1095, 1096, 1097, 1098, 1099, 138, 1100, 1101, 574, 1102, 1103, 1104, 576, 1105, 1106, 1107, 577, 575, 6, 7, 578, 579, 580, 581, 1108, 288, 1109, 1110, 1111, 292, 584, 1112, 293, 1113, 297, 1114, 587, 582, 1115, 1116, 589, 108, 590, 591, 592, 593, 594, 2, 1117, 1118, 1119, 125, 53, 585, 54, 595, 1120, 300, 599, 1121, 1122, 1123, 1124, 302, 597, 1125, 1126, 1127, 1128, 1129, 1130, 1131, 1132, 600, 607, 1133, 1134, 608, 616, 1135, 617, 304, 1136, 1137, 619, 620, 1138, 630, 1139, 1140, 139, 1141, 1, 1142, 1143, 598, 601, 1144, 632, 624, 109, 9, 602, 626, 140, 305, 12, 1145, 603, 1146, 1147, 1148, 1149, 306, 1150, 307, 1151, 141, 142, 149, 635, 55, 1152, 1153, 1154, 1155, 1156, 636, 1157, 634, 1158, 638, 308, 639, 310, 641, 1159, 640, 110, 1160, 1161, 10, 642, 643, 645, 646, 647, 1162, 1163, 648, 1164, 649, 650, 311, 651, 111, 1165, 1166, 11, 1167, 653, 652, 314, 1168, 316, 1169, 654, 146, 1170, 1171, 1172, 150, 1173, 151, 1174, 317, 1175, 332, 342, 1176, 1177, 56, 604, 1178, 1179, 1180, 0, 1181, 1182, 1183, 1184, 1185, 655, 1186, 1187, 1188, 112, 343, 1189, 1190, 1191, 605, 606, 609, 57, 656, 1192, 657, 658, 1193, 659, 1194, 1195, 660, 1196, 1197, 1198, 1199, 152, 661, 662, 1200, 1201, 663, 664, 1202, 0, 1203, 1204, 1205, 8, 167, 171, 610, 611, 1206, 1207, 665, 172, 612, 613, 1208, 614, 1209, 174, 175, 1210, 344, 323, 1211, 666, 1212, 674, 1213, 667, 1214, 1215, 675, 669, 672, 1216, 12, 1217, 345, 615, 179, 1218, 676, 1219, 677, 347, 678, 348, 349, 1220, 350, 679, 1221, 1222, 326, 680, 682, 1223, 1, 1224, 1225, 351, 1226, 1227, 115, 1228, 116, 1229, 352, 1230, 365, 1231, 58, 3, 4, 618, 622, 1232, 126, 59, 367, 1233, 368, 623, 627, 1234, 1235, 683, 180, 628, 1236, 9, 1237, 181, 328, 685, 637, 686, 687, 688, 689, 127, 631, 1238, 375, 690, 117, 1239, 118, 1240, 1241, 1242, 182, 1243, 691, 13, 1244, 692, 693, 694, 1245, 695, 14, 696, 1246, 697, 1247, 15, 17, 18, 1248, 698, 1249, 1250, 1251, 1252, 186, 699, 1253, 1254, 700, 701, 1255, 702, 393, 703, 704, 332, 705, 707, 1256, 1257, 1258, 711, 709, 712, 713, 2, 128, 60, 119, 714, 715, 716, 1259, 1260, 717, 1261, 381, 1262, 333, 120, 121, 0, 123, 124, 718, 719, 187, 61, 62, 720, 721, 63, 722, 188, 64, 723, 1263, 383, 724, 725, 726, 727, 728, 729, 730, 731, 732, 734, 1264, 733, 735, 1265, 736, 1266, 1267, 737, 125, 1268, 189, 187, 1269, 1270, 1271, 392, 738, 386, 1272, 739, 740, 1273, 127, 1274, 1275, 741, 1276, 19, 394, 131, 1277, 1278, 742, 743, 744, 8, 1279, 1280, 1281, 20, 132, 397, 1282, 745, 746, 1283, 387, 2, 190, 191, 192, 388, 395, 1284, 747, 1285, 1286, 748, 749, 65, 750, 193, 751, 752, 135, 753, 754, 755, 1287, 756, 757, 758, 396, 1288, 1289, 136, 1290, 1291, 1292, 1293, 759, 760, 1294, 761, 398, 1295, 1296, 1297, 197, 762, 763, 764, 1298, 765, 195, 766, 1299, 1300, 767, 1301, 768, 1302, 399, 769, 770, 771, 335, 772, 9, 196, 773, 10, 11, 1303, 774, 775, 1304, 1305, 1306, 400, 1307, 401, 1308, 402, 1309, 1310, 405, 1311, 1312, 137, 1313, 138, 1314, 1315, 1316, 1317, 1318, 345, 198, 776, 1319, 346, 129, 413, 66, 350, 1320, 777, 1321, 1322, 778, 1323, 779, 780, 781, 782, 783, 784, 785, 1324, 130, 67, 786, 1325, 1326, 1327, 199, 202, 787, 788, 1328, 789, 790, 1329, 798, 791, 1330, 1331, 1332, 792, 1333, 1334, 1335, 1336, 1337, 420, 10, 794, 11, 12, 1338, 1339, 793, 795, 796, 21, 22, 203, 797, 1340, 204, 1341, 68, 799, 1342, 800, 1343, 1344, 1345, 801, 1346, 802, 1347, 804, 1348, 803, 1349, 805, 806, 807, 809, 421, 69, 808, 1350, 141, 1351, 810, 13, 1352, 23, 811, 142, 1353, 1354, 1355, 1356, 1357, 422, 812, 14, 1358, 143, 424, 1359, 1360, 1361, 1362, 1363, 425, 813, 1364, 427, 433, 1365, 434, 1366, 1367, 435, 1368, 1369, 1370, 1371, 6, 13, 1372, 1373, 1374, 1375, 205, 1376, 814, 815, 816, 817, 1377, 818, 819, 338, 12, 207, 208, 1378, 820, 821, 824, 13, 826, 827, 354, 1379, 436, 437, 15, 1380, 17, 1381, 209, 1382, 1383, 438, 1384, 1385, 1386, 144, 145, 7, 8, 828, 829, 830, 832, 439, 833, 351, 1387, 1388, 440, 831, 14, 834, 355, 1389, 1390, 356, 210, 835, 1391, 70, 211, 212, 441, 442, 836, 837, 838, 1392, 1393, 1394, 839, 840, 1395, 1396, 1397, 1398, 1399, 1400, 1401, 15, 843, 1402, 1403, 841, 842, 844, 1404, 1405, 341, 216, 223, 242, 1406, 1407, 1408, 188, 1409, 1410, 1411, 24, 443, 1412, 1413, 1414, 1415, 444, 448, 845, 452, 1416, 1417, 846, 1418, 1419, 1420, 1421, 453, 454, 847, 455, 1422, 1423, 1424, 243, 190, 1425, 71, 848, 849, 1426, 0, 244, 850, 851, 456, 245, 1427, 852, 853, 854, 1428, 855, 1429, 1430, 856, 857, 858, 860, 1431, 861, 859, 392, 1432, 1433, 862, 1434, 864, 1435, 457, 1436, 1437, 1438, 1439, 352, 357, 358, 1440, 72, 458, 459, 359, 863, 865, 866, 867, 868, 869, 871, 1441, 461, 19, 1442, 147, 148, 1443, 1444, 1445, 870, 1446, 1447, 1448, 1449, 1450, 872, 16, 873, 874, 875, 876, 1451, 877, 460, 1452, 1453, 878, 879, 880, 881, 462, 1454, 1455, 463, 464, 882, 465, 1456, 1457, 152, 1458, 883, 466, 884, 467, 1459, 1460, 153, 1461, 469, 1462, 1463, 1464, 150, 885, 1465, 470, 886, 1466, 887, 1467, 888, 889, 471, 890, 891, 892, 893, 894, 472, 1468, 360, 363, 1469, 1470, 895, 393, 896, 1471, 151, 154, 155, 1472, 1473, 897, 898, 899, 900, 901, 902, 1474, 1475, 1476, 1477, 1478, 903, 1479, 904, 1480, 1481, 473, 1482, 1483, 156, 1484, 1485, 25, 1486, 158, 1487, 1488, 26, 194, 905, 1489, 2, 1, 1490, 906, 907, 908, 909, 398, 364, 366, 369, 475, 490, 915, 403, 1491, 1492, 1493, 247, 248, 1494, 916, 917, 1495, 918, 1496, 920, 1497, 1498, 945, 947, 249, 949, 1499, 1500, 27, 493, 1501, 1502, 28, 494, 1503, 1504, 250, 159, 951, 953, 927, 370, 1505, 371, 932, 251, 253, 254, 495, 496, 255, 256, 257, 1506, 1507, 948, 1508, 950, 954, 499, 1509, 1510, 502, 503, 1511, 1512, 504, 955, 14, 956, 498, 505, 506, 514, 1513, 1514, 957, 959, 960, 258, 262, 1515, 515, 1516, 1517, 518, 1518, 263, 372, 1519, 1520, 1521, 961, 962, 1522, 1523, 963 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 6, 1, 21, 2, 22, 2, 23, 24, 25, 2, 2, 7, 26, 0, 27, 28, 29, 30, 31, 32, 8, 33, 34, 0, 35, 29, 36, 37, 38, 39, 9, 2, 6, 9, 40, 14, 41, 42, 43, 31, 44, 45, 18, 46, 47, 18, 48, 32, 49, 29, 1, 38, 50, 4, 51, 31, 52, 53, 38, 54, 40, 55, 56, 57, 58, 59, 60, 61, 0, 62, 63, 64, 2, 65, 3, 66, 67, 41, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 34, 80, 81, 41, 8, 82, 45, 83, 84, 0, 85, 59, 86, 49, 87, 88, 89, 90, 56, 3, 91, 0, 92, 93, 2, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 59, 68, 107, 108, 109, 5, 69, 110, 111, 70, 112, 72, 4, 113, 4, 32, 114, 115, 24, 116, 117, 3, 118, 14, 3, 73, 119, 120, 121, 122, 123, 4, 124, 125, 126, 127, 128, 129, 130, 17, 131, 6, 74, 8, 132, 133, 75, 90, 134, 135, 136, 91, 137, 100, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 106, 152, 2, 107, 50, 153, 154, 155, 156, 1, 157, 3, 158, 159, 0, 160, 161, 162, 163, 164, 6, 3, 165, 166, 0, 167 };

    public static int get(int row, int col)
    {
        if (isErrorEntry(row, col))
            return 0;
        else if (columnmap[col] % 2 == 0)
            return lookupValue(rowmap[row], columnmap[col]/2) >>> 16;
        else
            return lookupValue(rowmap[row], columnmap[col]/2) & 0xFFFF;
    }

    protected static boolean isErrorEntry(int row, int col)
    {
        final int INT_BITS = 32;
        int sigmapRow = row;

        int sigmapCol = col / INT_BITS;
        int bitNumberFromLeft = col % INT_BITS;
        int sigmapMask = 0x1 << (INT_BITS - bitNumberFromLeft - 1);

        return (lookupSigmap(sigmapRow, sigmapCol) & sigmapMask) == 0;
    }

    protected static int[][] sigmap = null;

    protected static void sigmapInit()
    {
        try
        {
            final int rows = 1218;
            final int cols = 8;
            final int compressedBytes = 3316;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXc1vFscZnx3WdHA/slgQrJ7WhFBX4kB6SXugjAlpSKSqlg" +
                "EpUYUq9dBy8KnKkUhjF1WmJx84cHzFASm3/AlWxSHixKFRK079" +
                "Uzq7+37s7jszv2fm2X0NbxIT83hmn3m+P+ZZv3nvL/ff//bauX" +
                "trB9e++9HzByc3H/1KP/78h/Pm7qsbtzfEmz9cvf/+k4efX1g7" +
                "2Lfw+xV85/Hvfzh/fPdNA79y/9KT7S+fVvBPnt9/+fGjvXr98d" +
                "3/3tADrH/vKsCP93xp/ymEyET1OWv/29y9br8aIbRcy8UA5x+Z" +
                "PmB9/SlF4JP1/6JofzM2/ic/vXJ77WBbXRDi2nc7hxdPzm7vCa" +
                "M+tUhosRL6Ifm7Yve3+FXyd8bu//Gjjan8fT9bb+F36ue34WT8" +
                "ABzI//q6uLihlBBrB1rIUouPvi7040+PM7N3dKPm318fXHqy/8" +
                "d/f/j3/eefvfjTy5uP/vzJPx78p9r/m9vj6xeF//nB9ubTiv/Z" +
                "4cWXZ7c34vjPw+/n69LS705m6Wf139LvN19f1493Kvr980Yu3n" +
                "r9puDnlt+9Wn6RfRjfvmP94ukvkG+m/Dfyozrys9OSH6FE83X6" +
                "sd8Ucvr/Wgi+fWDSR7aZrVtfp55hfP8XhrPlE/L3dOl/2vClj+" +
                "nTl0uft5y+o/s/nn/v2o98Fp/ptvwH4w+vfd2r4a8W8H2Xfxj9" +
                "/Ez6UOQnHJ+x4+vcHT9nJPtkd1ibB9x1MnKvsF+r/EOsCX78Me" +
                "XvHZ//H9u/w/yKrX+k+LLJL7I6v+jFl7+YxscH+5N5fNycbx4f" +
                "P2nwmzT4XV6pfRg5fpRa5k7+FDsbFX/Y8GX+l23+o/WO/LSfv+" +
                "ah/BXBUf6J4P9q4E3+slPnL3N4NgCcmx9z949ZP2nW34rBD9df" +
                "kH9D8f3C/02a9bKdX8D8mZl/J8BFN38fN3+erffbx9U837e+DZ" +
                "80cNmGu/L3dv6F+Df2+rcRrnnry059xBdfHu99T6k/1P7p2/0O" +
                "fXca+Vj4vwDcSkGO6pdu/Z8+n8RflSwfCfuXMfUDLryrny9q/b" +
                "w11c9v9AZ7fez+orW/SDufGPJ8zqR4Aaj9Y+7xnxkl/1ZaUD5F" +
                "EGq8EFTfgR8VBnPz17HhLv536b9y/EQc/jz7iPJ/f/7a/OmPn/" +
                "Zi8k9vfaENT7HPDvxFN/8O5z8o/55uJBc1yLyjimj92PDe+WXf" +
                "VCTsL2Kez60vtNe7+N+Kj8Uifq7947MbgpBfCvmTUH6B4IT+MW" +
                "v/9S+856vjf1y/4NU/2vBJA+/E1xGuxgPX2InA9YGP5Pkv+HwV" +
                "73MbbuuuOS2UbC9T0/+RKiy/TXxQLlDpWLu8+kvZQdT0QgWljc" +
                "gr89XAlRGb1Z/b9Z6KzV+LvyyKCn/Z4P+swr+wP3UyxZ9OepnC" +
                "P3B+jJ8eST7m9O//YBEjX8i+deVMqnj8XfJVTH8wx/hV8tum76" +
                "aLvi75bfbH5wsbgI7Dq7/ZnSrX3D8Oq99ovXHyN+fvrxLtrwjE" +
                "SEPQRwOdptg3UOEN/T2WT6L+q8RUa2bfhdO+T/cP8j/3PFdS6K" +
                "982aKJ0P/Y/SNS1ST8I/RDQPsqmysEff9Q0uBc/8fVLwX0F9pv" +
                "TfRPsfETzT8R7HtctcMMjB/wX4i/7vgtEP+YSP3E50fyzYxvtI" +
                "M5rRKd8q2n+YcluGHKp6Han8T8w8T5t2j57++P+qNA/vH9hSzn" +
                "9Fe5/UkkHx9svpHFBbF2uTgrs98KsfXs9ZYoi4kS/7u8dsVExO" +
                "8FIVmckk3TLVIafxb6z+0v0gRdBY6rw1oK6Dv2/SzUn2zDXfWL" +
                "VeHn61/W9CPZdI98MvNTbv9y2X7d8+SfHlFD8gP6hwg/b/26WN" +
                "6/U18k7p/Qf1y+PxlaD/l7K2i/cH1ch9O/KP3m9/e4/cd+fzBh" +
                "vYhbrynxGa5S4XQplD8m84drP1B/E/nvofqfqfiR7z8XMdXJgg" +
                "zn9x8j6bMUtIbrE9A++Y1HQVuv/fYr58cXaL2B8Z8O6mnk/V9K" +
                "/01E9vdEqP/o6E+6an8zuHSsP1X4AP3ZIP0T+BfXf4J1y5T6yh" +
                "D1O6L+OPVDkfdn9Def9e4/UuGi1/9F/ddg/OpYj+BR/WHU33XA" +
                "RRvO71/z+sNt+Sic+YkI9xfJ/TH3B90fqepf1Q5b9olL9a8z8/" +
                "uLZ8SHvvyB2f8j1ufI9a+l+EKD+IO6nlhfjezvwf5fbP3PC5e8" +
                "/kCRWF+O729dj6nvcZ9v6cyTj+1modUfkxvLNyMLVZRKZJaNP3" +
                "b0x0x/f0PUb9/6kzB+Srco7UoBkXwz6SNed9dP8Vf9+qRlvqs+" +
                "+cHmqy58s4GLPly54VJ9ISw8q0//Mytom69n+nV5Wl80DW59+0" +
                "uzz3D/ij55qD4bPh/CD50f4Y/8G5n+Pf4JOv9q+l22Z0zh72y9" +
                "XFp/Qjo/wk+qW0H+0umTdr4Z/uPwXyScf3dZf3KsP1i/1gF+Mk" +
                "m/kX3B/S1mfKDCJSx8/wfEh0cALnaBf9yN6w/3+4Oo/xgLj/U/" +
                "8H4P8l9U/5YGR/zl9veqG2vF7Pmz+1uind/z4kcufm7+F+nwge" +
                "NzQn88eD8Az2+k1C8GtD9c+jD3x/WjkfGD99P0yPQHx0H2Hda/" +
                "dqc1OCld8fX48gHz9+rfqpLV1Z/cZM3PDuVfU/kHzofyO2fRuI" +
                "jI38B6SPY5/0WA/znov+Rk+prY/DS8P76fQuJvniwfSfo3nP6g" +
                "+R+u/6bffxmyvjJcfezU4xfn/b0Y/93rH5LlbzIznqz8nST/dT" +
                "QjO/ZZ1c8P4CfLxWIjVLXoeNH/0KuKX2B8MbZ/gPlNpP6YofFD" +
                "YSd5/4waMUXFZ9a/GpFNfnn40C75ymyY7OnEmv/fbZe5ObOK+J" +
                "PLXzr/5TsZ31Pky/HzOl9RfnlSkTUva+fRxqeMhMs0+Dw/dPfP" +
                "8PO16NhfDzyi/lHE0QfR93V3/Ty+pJ4f5r+gvg7iA2s/7BMmW8" +
                "LaD3GutLsdGxuf6+3Q86PrA2B+hAMXCeuL9OeT6isR8RXp/uTQ" +
                "+j+cfcPzdTB+iviUjPpPML9bQX3H/XxC/TpcPz4Sh2U4/j1fg/" +
                "KKRTZ/KSuMdorMFEebNPxr+9W9v9ehH4D7YnqzEvqrzvhm6Xip" +
                "DjU+kd76TNrHUPNrYn012X+kwCXZ/jjrt6h/HlNfBOEhO/+OhB" +
                "smfMX1Ofb7m9H7mdjxaWR9hVufHti/W/ocjlp/oMunZMIHzZ8X" +
                "3pwZ3+D+EKgPRdev4uBIieF8EjM+ZL7fmKufaH4O048rH+PeDx" +
                "2bPwnzZx0VhevJ9J/dz+4/7haTf1cfgPfLPWDNr71V+uWYj8L+" +
                "A72fEsh3JH2M19bLxPjRdDK3fn0c7hKj//H0hfNlyvF+n+n9kJ" +
                "ywHr3fjGt/KfEDB3+2/Ry0//Qizf/Hy58m4ZcLvn1i2sex46e0" +
                "/tDS/BBnPiy8P/P9UIT3o7DmxwRzPgzOnwH+4fk1Lack9dVvPm" +
                "rqR0vEPGq0Cq0ft//Cvb+H5Xvc+JQ7v8aNX1HVBOFHnw8O3J/J" +
                "/fCt4pm08Kp/IrNfW4kzJhNldYGoLO03tP2D8DD+8P4Huf4X7j" +
                "8lvx+M+3ykfOT3/0lqf+C6uz8gafibREH2wWH9inc/gvv7H7eK" +
                "o6n856KWf1HJf8X8Sv6P4PMj5sOS4ns6f93yM8j9G1J8qkD9ng" +
                "hP1G/y+zNj9Y/5ftrR7UPs+w2HPv+81ui9fzC0/ezzPzifw5ZP" +
                "9v0eZv+Yix9c/5qoX8g+rDvrv0vzTcvzX6z6sm8+LHA/tyNccL" +
                "4RxE9h+VMU+8m9PxlbPyhC2pd5zxd4v3geWN/7+OC+9aT503p+" +
                "r0//Lv4Y7p3/Q/YT2Z93BK6Y8Hf0/M7gbHj9xvOn66nyF4ST5s" +
                "tJ50s7P5qPHYA+Qfhw/sft3/j5ceHIv9r1HQQP9wfT7hfT8a/v" +
                "V11gzAc7/WtE/hI7H7zM30MVku/+/o7889KTh1/V+H1m8bv56G" +
                "+9/FVy8it8fkPMvxPzw6T83vV+q7T6wcp/P3pfkEauXwzQf+Ld" +
                "T2Pfn6Xm5yPRTy1yXdfvz6Ht719/2vNd+PdT+fuPa2Kg+eaAfc" +
                "bvRx6d/4dgPs0x31tU8729E8qQ/fLD5/v36i+z+WH/82lw2vOX" +
                "7XsuhjkfG477g9zzi8B8ATH+SZ9/YMd/zPqmalxAqRfyXyFb1v" +
                "OPWizNXyfXdxSIfwWIbwUtfo2t/yU9v01W7aAvnf4o/oPz4974" +
                "hxg/ceWfVp+NnM8XZP9x2v0jQv+RWb/j9rfGjX/R/R/C+62R/Q" +
                "i//5pe/4+cT/fWP2nrZenM7zMQH2Xe/AfVVxv/d65+fuGtL2Su" +
                "/kNOsIVL7wfvbOyYv5xQ+eOZP53Q9sf1Ad7+3v7UJFDf6dAf7Q" +
                "/ghWc+dUKrP5MT6OT6Uxg/KF9aRH1M7HoUP2qHwBeS/nwMD/5+" +
                "wgH2J9SvxotvEfz/dk5edw==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap1 = null;

    protected static void sigmap1Init()
    {
        try
        {
            final int rows = 1218;
            final int cols = 8;
            final int compressedBytes = 2934;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2PHLcV59Cz59Hlw/TBkg+pRooQbKHCSWOnUDwnW4kdIL" +
                "BwsoC4MOBSharApQLwhCskVypcqFyoMOBOf8LacCFcdY2B4Kr8" +
                "KZmZnf2YGZK/Rz7y9qxoC33s28chHx/fx498HFGI5pMLy+fss+" +
                "v33n0y/ee3k6MHLz56fu+n9x8eVsef/vz207v/uVntCVEI96cQ" +
                "j0QuRdn9sNBiv/l7Wn/TECO0H/j82YIZ8J999od7V57c//Sdpn" +
                "9vPr83//Dhnw6O/9H07+zmx3sOTg06RqR7jL8wEGD/+fL9hdAL" +
                "s9gR//ijXk35JKKvPtIswtT82+4/pmuXfbwA89sOvFyvn27w5f" +
                "+F/nLtZ+PfrjyZFjX/jRdv1PzvP9xr/Zs+fNn5NznWr4vUf6b/" +
                "MK6fc6BXtPiFHn8M9d/qX/zo2L64118k+1qO+Eua/0M/geOfNw" +
                "/Obc+H8yNOO/4dc//J9nfXHB8C+eL+LaNr7vpG8UsiOjn+QfyS" +
                "qh++/Sf6z+D2if4vML7cvvwi6YdtfaT2P5Uzf4V0IQA/mj/e88" +
                "/eqsf3/Y1LnzfxwZvPv2zGt4gP7p404+Pmx/H8N0xbZVD8zPRf" +
                "EfJbhA846bK4JZQSE6GkbF3Qszu1YVC68WuTetZlIRt61o6voe" +
                "/36dz2of0z+h/lP33h8+eWr9XFaOCCdD/Q8wxRCujidBhs493/" +
                "c6b/0vsfm+6LD47i1wrFvyg+CVgfG8utWIQoZbXmb5Z7Kctl39" +
                "5um8mz5tuqeWwlDlSm1eP9CPhjdP9y0fV/8HU9wolaz0lt7D9X" +
                "9Z+6kfIkGF/ywadUn18vviti2W92+zx+Un7lfH4F8Rl3+0x+vP" +
                "5Z+F+a/QlFZkuDD6kAunkB0fUHDT6sf97P13H7f/aWDX+8+9KO" +
                "T6gE8yND/SP4ZDkNn7B9uPaBMH6jfHUU+cx/c/3jyVE9v0LceH" +
                "Hw6PJ8Z3oodHFbNQMj2K/k9hHkt11+HN4/lD8DeiO//Gi638ov" +
                "a+W315MfzK948mPjh2K78hu3n3s9/3e78vJe8UkmJkd1flzHp3" +
                "/++r2D44OnmT785maO6Uh/av/05ZXvH/T6f7CQ75m5/5H5ufIt" +
                "RD6wYkp6wS8++vldq3+3Ov37N8V+oP6x7bvqD9CalsTwz+Px/7" +
                "iwD0b7mlWW+CrryV2O418v+Azxc9vnPp/FH+ZfBcG/xtGP+eb8" +
                "G/yDSX/C84fvEuQ3bvuF6YnzoxU+MRrM483V5J/JU/HBMPxw8H" +
                "i53i7N1UA1s+GTuuhP975VgfsPoH84P4T6g/I7gF9BfMyNrwXn" +
                "z0T8LDg/5tAp03qR8V2P/oGBxsPPhId998FPUsf3XPsUQvdZ34" +
                "nzR2Z+xY8v3e3v7oo6/6gbmRxVQtb5xx+/VtXx7Sb/eHzTpGve" +
                "8Wkc/LYc6XcZDb+pWidcjlneoOSfqH2dFl/yip/K8W/h/qx7fx" +
                "fu/0aLT875fN1reiR6yPxS8EXq+uKuv23zA3q5TCJKnetm3UlR" +
                "qLJoo/JfbeJHRc/+Xhz7xWy/mPPGh+QzXTBmYrqQr5aqJ1+ufQ" +
                "PnzwL2RwKfb/ucRvHfNvrv98+kekdMrtXOI/uLEFefnV4VpZoV" +
                "4r/XJtc15L+qnsnaPjTnY2X2Qb0ctK6XQjNBs7L+Dxwf6fk5JT" +
                "+QlvZP+u3vL9oXXfvbpqP4EvHL4u+ipmdNaiN+W6+T/dOl/7+2" +
                "iA9uDeh3enTYP0r+k4fvL9HlJ0Pl347fxi+LE6f8uPKP0H/w/F" +
                "us50dZn077jkIXFN8rlJ8RzzeF1i+A/AniQ57+h4Q/b4wKxP/s" +
                "+ldxx4+uY+Pn6PlU/4eIvv6tbP0bX76p808evk/CT/KU+NIc9B" +
                "/Rl/h4t2lWDgFEYv9UKH4fEv95nN9IMv90Ovf8CsQ38f41C3/b" +
                "Dj7qMf7E+G/V7D/+UBb1Er5RZvryfKdomG63v4iCH1Px/YT1A5" +
                "zzUZHpmS9+s4KYB/lpPhyhtO6PFC19UF+W135Mk+x/x7+7wa82" +
                "+MHzAb3ZH3j3yf0vWv39W62/Hz78l+F8qaP9xOsX4zsG/6jW/h" +
                "HiF6YMVxkjqIR0d/zm4sf1QW397MpO662Mb5vy649/zI/o/eXY" +
                "rd8yUf+926fmj6qOn0PyR+P+l0f+VaD54bYP8Ev2/SAVS/6Q34" +
                "Gw0fpfAYFGiB8c+e8qP1M7Yp2fiVV+xt8fUcu/zOcD4f4wkE9Q" +
                "fiXo/SPndwv/fmnlv1brx6n/pPWdD+OfXnyi6fd3mc6PKuL4As" +
                "/32Ohyg3/TXnb2s2j5e9Pt1B8xej5N/yE/kd6/n0L18TNX/Az1" +
                "V/HsHz5/ZbnfYxZn/uH54FjjM98PEnZ+c/05WdTnfGK7H+hkXb" +
                "/zwETH+JbRfkoPfImPP/l8tCc99fmh5PjDtvEjv4nISJNobx/x" +
                "Z9H5ueP35NfeImE+f9v6FQH/YdH1lvH51/St0stGA7LZpUdf1f" +
                "/4QiuRHc/q8PavU5HrKOcTQ/HL1/NzMfybD70M0A+kX4njh4Lq" +
                "iGz22X3/Hp/ukO25rM+5+35Acvu2/oPx4/Nj/f6t8K0yof75yF" +
                "dvmc7s31TIWgKzq+J+g56U9c+falUW1bQk6l+E/r/S53O3TQ+1" +
                "jz18NE9mv1Pf/5zc/1DzL8b5eMf6wPUTbv1l1y+Szq8Z5FXlRP" +
                "l1+tfDhwfzk/vmd95pPX3/bIwvJtW/oieycvPnmjTQ1Pe/M+ne" +
                "9UFk/Gu2th9Rzndaz1cy7VMEfFa48P2K275lfejOvlDvbxEe8Y" +
                "HP+gH6l3h9suefcn8paX1ZiMb2Fb39JP5XeNh33vwQ/B9x/xzN" +
                "365dvg7/ju6vYp8/TH7/kXv9kuxD5eqf5f62p93+D+gfvD+JeT" +
                "8TvD+Eef8X3F9m34/kuX9op3vcTxQxfob1+Ymfz+wf3j91y59/" +
                "fw0lfizX/8wHjhvHj7lY3w8jh/fDhN2//krdzxxJPmH35/ren5" +
                "rZ/Lva7EBI/uaD36bYf7NmVeh+JrR+wu63hudXlvy8+CjvT2m1" +
                "QdWUEJNbXySF/LVa9mGR/723eHbV1Yelvb8pdX0Ixj8jtM/h94" +
                "rvS7zWNDF+04ek+3+58ktNR/V3bHwO1nelpUe5/1ek1H9qfm57" +
                "f88pwnfCzl+u7POpcXxU/AjWN8P69VD+mRjUv8vN+mzb+dkg/I" +
                "qAf8he/KEC5LNrGV+E+nHHQKPU3+bD+MseX8Y5P+aBf24dH3bX" +
                "d8d4f3Ph1E9m/bdv+2P8W3Lwt3jxi9X+8t7/4JtfacpcXPT8UP" +
                "Teb8PB7wn5Hag/R4EeFZ8P4+fG/9z4AucP3PyC2n+zfNr66KNe" +
                "fXQ9ULWqjzbT1/XT9P1hC76N46NHvPqWyrL/NPSQYfW9qH6YX/" +
                "/rptPxl0Tv94m2/8R4/13ujt+d7/cKur9O0NtHhhb2b8Sv3BGe" +
                "Tb/NdFzfdqevf97xGxXfpx7AiBEf0uMbYdEwOr7qtj+E/Rnkv3" +
                "v7N/nG/k0Vw/+x398SYp/o+LQZPxnUx5LuxzDHF4T5SVp/SaqP" +
                "Y9SfDuWb+cbf1ue78HsffJ0W3+D6SMf+Os1+2fIn5vlDuADc9Z" +
                "8r+g6gy1A6rz6W+35dfH+d5fz3jK5fLP00+7fY+JVVP85DPqz8" +
                "PwJ+gPy7Az9cvp/s2+79Mz/5vp8sKL61+ed82fmK2v8I/ttjfy" +
                "YE/wk9H1zS9i/558MtPy3o8UnOkG+bX5TrnzrOPxRh8mXlX3z8" +
                "wro/95K2P2faHxQR+xeyflLUpyNBpHp/L6iPwvtPTn7UP/x+zb" +
                "T6iepTouQnrP35WPqL/Ld5/7C9X1mJSR0myHaKn43rQ0pXVIT2" +
                "Vx677RO9PgXtPwW/P0J2IpFB6xvXvwB8qFrcjzZ4Pv38I88+hb" +
                "3/2qd+hTk/zPglzfvlfe63du9/bv98Ttr9WY/zozIF/sR9/w8X" +
                "X4fj/x9FI4lU");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap1 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap1[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap2 = null;

    protected static void sigmap2Init()
    {
        try
        {
            final int rows = 822;
            final int cols = 8;
            final int compressedBytes = 2039;
            final int uncompressedBytes = 26305;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXL1vHUUQn1v2OZdIKIvlEIvqnFjIhQuUJlCYnCERpECJnF" +
                "iCgj4FFaI00tpKkVClSJHScoFElz/BQilQKjdIyBV/Cnf3Xux3" +
                "59v9ze7sYsMV9rPn7dfsztdvZo+oeXT7Q9HpY+Y+18R7PO21rx" +
                "3ovyRLWlHVfOj+tLTc/l5r/kPd/2TzO36wun39+dq3Lye7P7z+" +
                "8mD7ze2drfrp/T8/ePHor416EdKJiuHqTBh//Pw/vvrx9oe/rV" +
                "9+PNldf33p4PvDOzu3uvHto7cbdxcxC0s/fz66oq4tll8XNNkl" +
                "Wqhq+uynTzafbr4o7NYvG5q1P3q2Ee+WYtT8zpZofwa8sGf657" +
                "ZXce398/v9/dW7enetXCJaf725d+1wYW2LbHmvGbUuuv2Xtnc9" +
                "lnWqML0GXwZ0wL9mv4thT2Y6e+tvabny75jfoL17/WpGV5Hr9+" +
                "sfrJ9Gxy+9+oFz5i3zfPefgkLlwz+/5gRP5iRQkXncrtS2fJ04" +
                "t92Q/3hcHLrus6yeOx2Wc36OHzT6+/mT+0ut/bh0sN3q782n37" +
                "T243iqv6X21a82jq+uNuM3+qe1H+8149/eWZzaj62Z/SKZ/sX0" +
                "vXH52B80VlHtV8wr1WySrmhBFZ8SVdYWVJmqpKpq/sAH2Cm/vv" +
                "mZgP058tOj5M/wBRT5d8L131x+q8wSTW7QAhWfE60sH600/G8a" +
                "/31jsmpDFFwc/WT+V7LM/+bycZ/+arA+sH98/ihg2FVPf5YO+b" +
                "jcrd+crj9K/ox7fDozPjjIyP946Nc/Qv+Z7ynFyXer368/f/Jd" +
                "Fx981cQHd3Z+7Ol3uf8wEj8F+C/i9g5+1HrgUhTTziqWgXWTbW" +
                "L/Veg/Yfstiy+R/T+cxg/LL9v4odi79mZhbfFd/EDj8YOJYTum" +
                "I5662hvq+b9jbNfx8jekF2xP2A7GRwuRzi9N/1Hr61aoRs+/9H" +
                "wy56/BWcL0Ukin86I/7PPfhvp/tWP/2PYZ2Bd/+yk+VLrxIfb8" +
                "Ccx/XD+0+m/iwE/6+k9hp7HuG4xiHJ8yQfhjlP0OxE9F+Ghfvx" +
                "bAfyq89uvXTj98MdMPP3f89+/fWPuefhn1341z/vHxk8vBRvsD" +
                "7Jf4fDDsn5fewa1t/DuID/a9tqjk+ic4vsfr8/If+Qdgf7B+Q/" +
                "zx0pPYR/QV7WbPCb5hmvjxBN+gEXxDRQ5eA/8XrV/m//L7J4f+" +
                "ivH/Ef5s+OojdXwSG/+YuPHj8EmTcP0x+H//fDrwzT9S4JuQP9" +
                "H6kYu/HI3qJ5oF2tj+Hfbb06A95r8fP8L0CPuHnoT4f3nmfJgw" +
                "+WPBl27/F/NPNr+ui6b/+rT/djKVqmZtE+sHm2v/xuWDn39C8R" +
                "NSC5H4LOwf0DPjg1L7zPKPtMS/QPpJ5t9AfA3VN4jrIziy4o6v" +
                "pO1RfQKsX+g6qk771AO1J80fTntUsyGaNWgGfh+gf4Tj4/qHOg" +
                "g+HVGk/vVH1bd44md7Bv8A+SNh/7nx/dP2cfUfmfMLuL4gBsgP" +
                "wHfl8smUvxB6AL7Oxh/C8vN8/XoE7L9/fFd+uTzNL8vxJe/4cf" +
                "nvMlH+G/bPzj+QOz+ix/ADy2xvpPFRivwJJch/CwU0d32aiou/" +
                "hfhOKP4UrH8wnXngVGD8yJN/5vp1n63p8lcn8m8W1Lj8G+b5Jx" +
                "DfefMrOhqfYttnTv41Q35loB8KZvybDt+H+Qs//g/GZ/g3kfIJ" +
                "8Cvli78j1h/tf2XOn2TmHz7/iL/+/M3hfPxadPHroH5Ghh+I76" +
                "cAfADnv/n4eTlCwPh27Y3v2fbZccxXzLNZ/a6e5rdomt+iLr/1" +
                "jBmfxM8vLr+VMP6OwpdxfqBMlh8A/Z/1mE2wf478S09+QJWKjG" +
                "m7bejtFJcfNhMztl3XRPfwF3/8H3s/BK5v5l84+/fnByC+Lbzf" +
                "hOmJ7JMEX9Be+ZP5B0J8WI4fA3wX0gMgqSz6KRE+F4SPmnT4aC" +
                "r58cs30g/O9gz5D40ve09c/ixH/B8SP4XYj/n9q5j7qwLiS4Af" +
                "InwQ+M8IH2PdL9FAPj106fjx+OY+N75D91+E+KGJ3L9QfBbmL+" +
                "L0b7L7DR7lpiXy2acXgfgqxI8A/3F9cK74mIufXOz4nfV+h4z1" +
                "zxA/wP6pvz70wuMf58tfjn+cs34/Dr+nZPo1RH+WYv8llX+eMv" +
                "+C8AEhvgDrJ2vv+Ub4ABs/iOe/9P0Tovo9JP/I/xfXD2D/KUV7" +
                "7fWfdHx8n+D9Bxx0w1ffJKOL4y92/Bu5vszyk+T+s8A/D8IXKv" +
                "Dd6owsi9/vAeiwf6l/BPQTjG+F/m98/7z5i+NLTv+C+D7P/Y85" +
                "aqdfq7KZwnpV2Mb/K9tO73U9jOvXpO9Pc7e3zPay+Ou/7t/nr0" +
                "9A9/P+7+sH9lWYfw3GdyzHvvfrh/z3r5D/AOIDqf5Nc79HUB/E" +
                "vR/ioufOz6Dzl9l/DfWfA/cP5zdAfJ/Zv4L0FPXZoveroPwGCe" +
                "+vI/8vb310ovoQ3/0+Dv4b3V7qX5y7/RPi4xd9fjC/kAJfZPlH" +
                "46qCf386sv5IWF/DyE/nxU/w+2W5+Ehsfj83/hSUnwvOP0H/A9" +
                "6/Ycmf6slU+P0gV/t0+XeBfUT5Vd/7w3F+7GLnJzOPH/d+vxD/" +
                "TpofgfGJFP8V4rNC/RGV/wqhx8gPny7GJ6XvH08Rv15kfOvfGN" +
                "/DP1ifLn2/SmY6il9xfMuTf6f95dAF8nf+8Y+sf4Z+9Opn6f7K" +
                "9x/FLwn46xv/HyZOQdc=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap2 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap2[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int lookupSigmap(int row, int col)
    {
        if (row <= 1217)
            return sigmap[row][col];
        else if (row >= 1218 && row <= 2435)
            return sigmap1[row-1218][col];
        else if (row >= 2436)
            return sigmap2[row-2436][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in sigmap2 lookup");
    }

    protected static int[][] value = null;

    protected static void valueInit()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 4767;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq1XA2QVdWRfv6MQyYQBAmwiQZrU/7F6LpARLNuODNvHo4mlc" +
                "pWUroa4gjqlkBmAJfg4ECce8+97828N2MRLGBGs1VhwviTn9JN" +
                "md2N7JrExBXIootrBTU/7pqUm80GjSQClhPdPj/9uvuciyZUll" +
                "P39jndX3d/3XPfu38zVCYrk/rrlUkzkt76+uyv3NwMfWBgemVy" +
                "sEoawDzQnD1YmRy6U51Umax/pjJZKlUm8ymVybR7eBbsR4zG6C" +
                "DKPbrHx/taelKpVL3MzJ0NfEbMfuATpVJ6eYn9o4xmztcucvur" +
                "A/ulTtoh5jz0pb2R6SuE5rGzPZZPS6lU218q6X/wNbXKqHI4nb" +
                "5ffwUjqS7Vpe1edWUlmD+eneJWMD+QHAHtqP5f1MBqNs6Sb6qu" +
                "+l3qZNVVLTtNPsXshz8PqDmqCzgdtVG+m435eLvrp2fT9GqY/a" +
                "AZb7u3fS97U7+pHyuVspOzk7KZlFE/AdvTtIbV69DPQ8khrsva" +
                "mH1vqeS1rbB61M6m6D3Wtl/3MuQbsH3fzWt7bQ0tqmuwQ/97dh" +
                "lYoPq8tZlhBs/n/X+n98H+Sf1UBn7ph0E3rsYh+zhK/TiuzDrd" +
                "osbzHxoNx9BonK5OVOPDm5we+un11RfJR393aL7T6t3m59k5F2" +
                "Y/QCT009m+Z2z6MePl/iEC+jkO/WRZjW3oguQ3Ifemx15c24oe" +
                "dVygn8a23+QXFX/fzQcrZg/9HB86Cv1c4mtq5VnD+iHqPtign8" +
                "16V6lV2c/MHuRB2J7JXnIrmP9an65W1U7IXkONWgU+aH0atgPf" +
                "gXX2stc8Z/evqlUdPSCPOGz2VP1eb/8RHH3/Uf1LmD2PUaof87" +
                "b/zF7Ifp79InsxeyV7NvsxZcwOwXaU1rD6SfbToauSw0L3X2z+" +
                "P9kvia1jAZl/ZW2/Ndx4NdkPeWXOA9BXcG9ZOfe3EX6THW5iU5" +
                "WCTFFaRIrr5FWVDk1zesLQcGh9EH1Rn62mdalUv89p89MMvvp3" +
                "HFl7L0Vy0WjGWcmswOr65PWQe+xBFWFE6FRvWDGPgNjsqjAz2W" +
                "X9pVI+PZ/RxE5Tpl/TnLRZp+HQB/TNICdIQ1g3cyucuc3sOz6L" +
                "M6v1EfLT7PnoIRFnguemfzIjXzvs0FePxYsyc0bExnEriD3BeZ" +
                "iVzCxZcZ3vp8MOq2GQwygtYhjXeokaHprt9ISh4dB8c6NjJa1L" +
                "pcYDhIYzySKOrF3MbchAsoizAqufpx8xc12JefHcxBE1HSvDin" +
                "kExGYrw8xkl/Vz5qBbrcxnczVK6OEn3crOP6VWD92Kdo5FtNtz" +
                "m9nXTkWbkY19aLef93N4lNoj3MbYrZZ5aO2wHT9K26VO2kNfYl" +
                "M9uzg2sbf9HAwzSw48EzI2W7IuOTnbR9do5l/yuWR6cjSBb7v0" +
                "+mRWA47lZDZcb74XJPRWa5ifBfO+5Byd2LPyweQDsP6gTpMLrP" +
                "9FtY+5aMmHkkugn0/wTnU+D3GXgW1JcnnSVTucXJF8lKzJBrr+" +
                "TD6TXJeXQbfMWm5Mbk5WJCckJ5pVfUP6KY96V3Kq80jeDdtcnQ" +
                "F2vbn+TOZBX/7N+p7H8yfzPf5Sn/OyZHHSkY5zltkI9cNiPoG8" +
                "8q80+S3lfUtugO0mtUKtgL6uQGk7vQLXekitqD9PGsJyNPd3+8" +
                "ovKBJF9PX8hEepv5/b2PG5QuahtcPWH06XS520h77ExuQvip2O" +
                "86qyR8PMkgPPhIzN1rmwcyHkWIjSVrwQ17rRubCxxOkJQ8Oh+e" +
                "ZG7Vu0poj+8/4Bjqx/hNuQgWQRZ+1c2PFcekvIPfagijAi5D8v" +
                "rNjN03Huke0OM1NEWT9n3rlQ5SqHvuYobadzXOsHVN7ocnrC0H" +
                "Bovrkx+AKtKaLPeydH1q/kNmQgWcRZVd7xbLoh5B57UEUYUebn" +
                "seH4ZB7Zk2Fmiijr58xB1626QXajtJZuXOtvqu5GP2kIy9Hc3+" +
                "3rZ1MkiujP75fwKPUvcBtj1y3z0NphGw+nt0udtIe+xEY/VByb" +
                "2Nt+ToaZJQeeCRlbxKgahfv3UTPg/n0UIp3iVqA/UPmlGq0fhv" +
                "t30HvMbLSaYeOMwv27tSMGUHPMHO7fmxicZdNMTELW95JNv2n0" +
                "7v6dEDKy5fW6Gm18O22g1WZs46xQD/fvPn82xWng/MHyw/37KL" +
                "EjX7hGPxGsR0RNM3g+7/+75vmrRY3C/fuo6lE90M8eM6CfPbaf" +
                "PW7oAx1/onoad0M/e3BAP/2sZNFmD/1satw+m2Mk9LMHMTjLpl" +
                "V3IdKMwT6ymX42n4f0yDy0Bl6vw/H5bHoH10E/mQfioZ/e1/bT" +
                "2kxNzUhvUGxib/t5MliP8MzQz55wmH76ulpUD/Szx1wv5f3pif" +
                "mtA3uaZ364Xqq+31wvdcyB66V/NddLsJnrJbhmTc6E7SyLK7xe" +
                "SkaSi6z1Drxekv+qT/os9nqp8mt5vTSwj12jmOulDUXXS0mj8U" +
                "r6JY/y10swezds9nppAK6S3PWSt7DrJahpvteK6yXJMr84PYEx" +
                "+TheLzGmT7jrpSbGXS+NqTHo8RhK+7Mdw7X+RzWWTjg9YWg4NN" +
                "9Cvdmn95IW+rmQI9MvcxsykCzirGqs45n0npB77EEVYUTIvyCs" +
                "2DMZ5x7pfXFNxfVz5qCrqArICkprqeBa/5OqNHaThrAczf1RT5" +
                "Eooj+/b+FR6vu5jbGryDy0Rmz6sNRJe+hLbEz+oticPRyfHWFm" +
                "yYFnQsYWsUPtALkDpbXswHXlEGo4hgbZOEb6UER/h3GII+tPcR" +
                "sykCzirGZLd4fcYw+qCCPC8XljWDGPgFhkKXkU1c+Zqx3mvqna" +
                "Vj1t+M+SKfxdg39+/zI+00/eA7jpZpWcmfwpviWwn99T8Vk/f7" +
                "uQLGCRXq6y50e1d/I3Go134PsO1Ppvo6uTa4xl+CKnq86Ubxmg" +
                "n88Bqi1ZiZoEEMmcJt8z3PsOGdXOfU3JItTyqM3vzyuSK/n3p6" +
                "/1ncknveavk8/6ON2UozKp1qv10Nf1KG2n1+Na70INx9BwNn0Q" +
                "faXerSmiy1u+nyMbs7gNGUgWcVazpT8OucceVBFGhE6tDSvmER" +
                "Cb3xLXVFw/Zw66DgVnNrN30lo6cK0fkRrCcjT3Rz35kb///pzg" +
                "URqf5zbGrkPmoTVi00NSJ+2hL7Ex+Ytic/bQz8+FmSUHngkZW8" +
                "RWtRXkVpTWstWN8tzyXNRwDA2ycYz0oYj++JzLkY3buQ0ZcBYy" +
                "Mmn0lJB77EEVYUSZP64MsfntcU3F9XPmoKupGsgaSmup4Vp/Bz" +
                "UcQ4NsHCN9KKL//pzBkY0t3IYMJIs4q+X2jpB77EEVYUTw+3ZY" +
                "MY+A2HxrXFNx/Zy5qvF31+G7b32g8346w4TvufnZJz4f8TObjN" +
                "9+hOsab8jzUfwGvvj9O/RlquQe2o/1/t3VFMcmtraf94eZZR/i" +
                "c5g/H81Ss6Cvs1DaTs/CtT4DNRwTovlGeoxEEf3xuZlHGb6V28" +
                "TPW+TBFWH1+0Lu0h5yRE1+JKyYR0BsfjTMTPYwE+aztqUK7prM" +
                "3klrWYrrzvukhrAczf1RT37k74/PozzKcD+3MXZLZR5aI1afI3" +
                "XSHvoSG1NTUWzOHq6M5oSZJQeeCRlbxFq1FuRalNayFtf6XKkh" +
                "LEdzf9STH/n7+81reZThrdzG+rlW5qE1YvV5UiftoS+xqV5THJ" +
                "uzB9QjYWbJgWdCxhbRqlpBtjpp9a04yjPKM8iKGD5zK5y5Tc5I" +
                "YnwTk+IMj3Eb62drnIc0tp+nFPOSGYkRsuE18djE1n4bXh5mlq" +
                "y4DhmbLf379Bv8zMHPMOZ9HFWY2t/q0vPNPn3wGPcek6l5Yvtp" +
                "X/HR5KH4DqS6DFDLwvuRovMR7tPrZBYffQHFDM8w7nwkozTPR7" +
                "ciVt8SsmdVdYH9iDzPhuejItbqmT9sFHnEOqcJ9UUZEWkk7TnK" +
                "6UIf6Gf5WBzcfGAe98WYRZFCzvaseW2YubhyztgipqqpIKc6af" +
                "VTccDP8l41VV9HGsK6mVvhzG1yRhLjtx/mcdIvcxv7OU+N85DG" +
                "Hp/dxbxkRmKEbExN0u6ZjJt97Qs++vVhZsmK65CxtY+oEZAjKK" +
                "1lBNf6JdRwDA1ng/v3EY6RPhTRf95vkEhuQwaSRZzVcrsp5B57" +
                "UEUYEfrZF1bMIyC29nRcU3H9nDnoelUvyF6U1tKL645BqSEsR3" +
                "N/1JMf+fu8C8IoZGPsemUeWiNWr5I6aQ99iY2pqSg2Zw/9bAsz" +
                "Sw48EzK2iJ1qJ8idKK1lJ66zM9VO+3xeYGg4NByfOzkG9W5tn8" +
                "83tcB0B0fC553ZkIFkEWc1m/7bkHvsQRVhRKhpXlixZzLOPfS6" +
                "uKbi+jlztdOdpwbfB+fDPfHzz+xCfp+V3+Cef8qzZv7p8J5T3p" +
                "tZzDXs+efj+TJ+zpf3m/z9kf2dlutdpPzG8DfA9Zb8WnlWpjOx" +
                "e39k/W7m1yD5cqjpAqv/Gx/3KmKSX5c3r2eqjwyeAZqb5PPP/G" +
                "q7X5p3m/dHhfebt6nboK+3obSdvs2Ncle5CzUcQ4NsHCN9KKK/" +
                "/uySSG5DBpyFjEyayksh99iDKsKIMn9cGWIHz49rKq6fMwfddr" +
                "Ud5HaU1rLdDX2gcw9qOIYG2ThG+lBE//xzj0RyGzLgLGRk0ugH" +
                "Q+6xB1WEEWX+uDLEDl4Y11RcP2cOurIqgyyjtJayG+Xp5elSQ1" +
                "iO5v6oJz/y98fn9DAK2Ri7ssxDa8Tqb0mdtIe+xMbkL4rN2UM/" +
                "Lw0zSw48EzI2m14Uv0Gh7yFt31DrS/i3obwLsX8vcpC+Ue390d" +
                "Km/4cK7o82vv29kfwWPsb90cNFz9GIybHuj6r9Yfyi+6P2qfFz" +
                "yPDvZeL7Q+joF9UX3d5J1Ll1517UcAwNsnGM9KGI/vO+VyK5DR" +
                "lIFnFWs+l/DrnHHlQR1crzx5UhdnBJXFNx/Zw56AbUAMgBlNYy" +
                "gOvO3ajhGBpk4xjpQxF9P3dLJLchA8kizmo2/S8h99iDKsKIMn" +
                "9cGWLbp8U1FdfPmYNuuYJrCLN30lqWu1GeWZ6plusXSENYjobr" +
                "T2ZzeopEEf3358wwCtkYu+UyD60Rq38mddIe+hIbk78oNlx/sq" +
                "pq88LMkgPPhIwtYpPaBHITSmvZhOvKa2pT5TWnJwwN+DluRD9u" +
                "c1HcmiL668+fSiS3IQPJIs6qNrVvNNyKefHcxAU1yDiMLT0G74" +
                "1rKqqfHQHOtlgtBrkYpbUsxnV2tlqcnUsawiK6vV8thuOT2Zye" +
                "IlFEl3dwdhiFbIzdYpmH1g7b3p+dJ3XSHvoSG8O4KDZnDyx3hZ" +
                "klB54JGVtEn+oD2YfSWvpwXfkVajiGhrNBP/s4RvpQRN/Pd0kk" +
                "tyEDySLOqvqq2nAr5sVzExfUVNOwYh4BsUPnxzUV18+Zq77wHa" +
                "F8s5id/3bvN9s3/KHvNwfPlLrje7/ZviH74PG930TGb/1+c+jC" +
                "t7qHfov3m9vUNujrNpS209twnf05ajiGBvy0B9GP27gPRfRXdn" +
                "dJJLchA8kizmq5XR1yjz2oIoxoj8+gYh4BsUML4pqK6mfHp7Ot" +
                "UWtArkFpLWtwnV0sNYTlaO6PevIjf//7rPvCKGRj7NbIPLRGrH" +
                "5R6qQ99CU2Jn9RbM4e+rkyzCw58EzI2CI2q80gN6O0ls24zhah" +
                "hmNokI1jpA9F9PdHd0oktyEDySLOarnVQ+6xB1WEESH/lrBiHg" +
                "GxQ1+KayqunzNXm9/q90OA8yX/D78fcs8f4/sTuDWO7/uzevfv" +
                "9f359eP8/ZCNaiP0dSNK2+mNuO6YjxqOoUE2jpE+FNH3826J5D" +
                "ZkIFnEWc2WXRpyjz2oIoyINcWxpcfQN+KaiuvnzEHXr8zfF/Wj" +
                "tJZ+XFdeRA3H0CAbx0gfiujP72dJJLchA8kizorcinnx3MQFNa" +
                "6mOLb0GHoorqm4fs4cdG2qDWSbk1bfhgOOgb8gK+r4zK1w5jY5" +
                "I4nxBz8qc3IbY9cW5yGN/bw/VsxLZiRGyCb7sMwumTQ/75NhZs" +
                "mK65CxtbeoFpAtTlp9Cw74bFxEVtTxmVvhzG0000cxKo9fG5M5" +
                "uY31syXOQxr71uGOYl4yIzFCrrwmHhvt+L5Dtegjsl7JgnTI2N" +
                "rXqXUg16G0lnW4rryCGo6hQTaOkT4U0efdJ5Hchgwkizir2fK7" +
                "Qu6xB1WEEbGmOLb0qF8Y11RcP2eOmmM9T65skzr5PFmr4t+3yX" +
                "aFT1l5fLX/j/M8Od/1+z5P5lcZWNPbPU+uX3N8z5O1OStN2Hd4" +
                "U5xUE83KJ5JX7f+kAfrkPfAZmA7XAxPm7zs4h+qpzs/G+K3pp0" +
                "UtwLgWw/9fjgnx13Lnsp/wBCLA/+qk+U7U9LM6k+MMQ8uqLfnv" +
                "JldAJHOaTM4w/aRc8SxZxLVyDv3sTa5kdX3c2eXfd3gO3cL7/w" +
                "COnHFn");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value1 = null;

    protected static void value1Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 3861;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqlW2usnFd1vTQKsnH44ZQ0SkRlxwpJID8akJIqQKSTmTsY2x" +
                "JQWkSQwq0j27KxrEoYIsdCsjMz370zcxNwbQeV2PVDdlPES0JB" +
                "IKQ+/CNNEAQK9EHV/oB/4SFFgqQVCbmpOI9v3bX2Pt91QrhHZ/" +
                "Y5e6+99trH883jmyTcF+6bmwv3hdbOzWGX1s3AeohVtObDzzzm" +
                "z+W/uz/vWRjjn62oe2CX3mV9Nu5zqaaZ7+ZW9XNzy3/vK1sNWg" +
                "mKM2Jr2BrtVtgc2Yp9b5P1EKtozYefecwvdad7PQtjcp5bbR3u" +
                "gR3dbX027nOpJvXUxa3q43l+2Ve2GrQSFGfE6XA62tOwOXIa+9" +
                "4fh9NLf1P8xHAUtE7vV8a27nctUmNQYFXUVdOcrPPa6wx2BMbS" +
                "k+24rEcXNGP5p3VP3f2r8uhrQhNtA5sjDfbNe0PTfLz4ieEoaJ" +
                "3enx4nt9Abff+qyMlNGoMCq6Kumllv9trrDHYExtjTVt9xWcfz" +
                "lIzlX9U9dfevyqNvT9gT7R7YHNmDfW+z9RCr6PGzGit+5jG/rf" +
                "sDz8KYqNtj63AP7OQu67Nxn0s1qacublUfUVf6ylaDVoLijLgY" +
                "LkZ7ETZHLmLfvC9czNe7wXAUtE7vV8ZSt/d9i9QYFFgVddU0lx" +
                "712usMdgRGW1+5lz6rGcvP1T1196/Ko+9oOBrtUdgcOYp97wg8" +
                "iuFgTDE2h4xt3e9bpMagwKqoq6Y56XvtdQY7AiN6qrltxvLzdU" +
                "/d/avy6DsUDkV7CDZHDmHffAAexXAwphibQ8a27n9apMagwKqo" +
                "q6Y5mffa6wx2BMbY0/t9x8oA7ENvqXvq7l+VR9+RcCTaI7A5cg" +
                "T75s/Dkd6Pip8YjoLW6f3K2F7vP7JIjUGBVVFXTTPxdOvS2tQC" +
                "j9ZXbvLl87y37qm7f1UefevCumjXFZv96zDieX6IUfh0VXZYlW" +
                "lXtKv8/25rakzUravr0JPfj97brctWpCKoaf7CVrdKUP+hvb6y" +
                "VaU+KM7x7WF7tNthc2Q79r1brYdYRWs+/Mxjflv33zwLY3Ke22" +
                "0d7oEdnbc+G/e5VJN66uJW9fE8x76y1aCVoDgjdofd0e6GzZHd" +
                "2Df7rIdYRWs+/Mxjflv3PzwLY3Keu20d7oGdbLc+G/e5VNPs7e" +
                "ZW9fE8P+0rWw1aCYoz4pHwSLSPwObII9j33g2PYjgYU4zNIWP7" +
                "fXOfRWoMCqyKumqak3/y2usMdgRG9FRz24yHflL31N2/Ko++pb" +
                "AU7RJsjixh33snPIrhYEwxNoeM7Xl+zCI1BgVWRV01zcnTXnud" +
                "wY7AiJ5qbpvx8Ma6p+7+VXn0nQwnoz0JmyMnse+dCifz902D4S" +
                "honWX0TnFPxvb9/ZQibQwKrIq6aprNQa+9zmBHYLT1lTt+35SM" +
                "h9/iK5PR9q/Ko+9sOBvtWdgcOYt97ww8iuFgTDHhbO8M92Rsz/" +
                "OMIm0MCqyKumqak+967XUGOwKjrV93RgW+MuO2f1Uezs7fPn/7" +
                "3Fx6LDZFyi6tmwfhUQwHY4qZv70Zck/G9nrfr0gbgwKroq6a5u" +
                "S/vfY6gx2BMfZ01HesDFTgKzNu+1fl87eHaZjGc53C5pOeYt9M" +
                "4FEMB2OKsTlkbM/zUxapMSiwKuqqaU7+x2uvM9gRGGNPS75jZa" +
                "CCuqfu/lV59M2H+WjnYXNkHvveHdZDrKLHz2qs+JnH/PY8j3kW" +
                "xkTdvK3DPbDj66zPxn0u1aSeurhVfUHbylaDVlJ82B/2R7sfNk" +
                "f2Y9/MrIdYRWs+/Mxjfvv6+WnPwpic535bh3tg33Ol9dm4z6Wa" +
                "VL+LW9Wzr1qpV2LxYVfYFe0u2BzZhf3gp2FXfn/fRZ+uClrz4S" +
                "cTGdvffZ5SlslNGpPz3GXrcA/s5Gbrs3GfSzVLT3Zzx/d36Ypd" +
                "dFWwlRQftoVt0W6DzZFt2DcPW0/YNuozipjmw8885re/x/0tMz" +
                "TXnec2W4d7YEfz1mfjPpdq4utnJ7eqZ1+1Uq/E4sPhcDjaw7A5" +
                "chj75jPwKIaDMcWEw8293JOxfX7+RJE2BgVWRV01zelWr73OYE" +
                "dgjPV/7DtWBmCbj/rKjNv+VXn0LYflaJdhc2QZ++ZYWM735w2G" +
                "o6B1ljG4hXsylrqDWxRpY1BgVdRV05xc6bXXGewIjLa+csfrXT" +
                "KA8krr/lV59M3CLNoZbI7MsG/+Gh7FcDCmmDAb3Mg9GdvzvFGR" +
                "NgYFVkVdNc3p97z2OoMdgdHWrzsDFiivtO5flUff+rA+2vXFZv" +
                "96jPheeCOjZcTXz9UoYliVaVe04L/7c7amxkTd+roOPe3r53qL" +
                "0rjmWjXak3JTLRTYylaV+hQfhiF+l0mPxebIEPvezfDAG89zyM" +
                "GYYmwOGdvzPGORGoMCq0Lx9MTzdNrrDHYERvRUc9uMrp58HWqV" +
                "fneGndHuhM2Rndg3l6wn7By9yChimg8/85jfnudZZmiue37utH" +
                "W4B3b0G+uzcZ9LNc0/d3OrevZVK/VKLD4shIVoF2BzZAH73k3W" +
                "Exbi83PBozUffuYxvz3PR5mhue48F2wd7oGNz88Fi9K4z6Wa1F" +
                "MXt6pnX7VSr8TiByuDlfjqu1JsfiVewYj/lk8wCp+uIsPrsMLM" +
                "iLetrlbI2fK/zdZMjw9+wJ+nreg15E80/9KtCxUf3KSKqKZoq7" +
                "mpFiptZatKfVDMdd4/UXubp+rocHNZd/2N7xj9Za56/dqYwfVz" +
                "r+Jv8AQZRgtdiOZbFpVWuntw01r1x59tK6xcpr7pYPg+ew5a2X" +
                "Yarg5Xl8di4Sv73g54FOPROuknO3Pz9f6SZ2HMKyMfdsROf+i1" +
                "27jXCE/pqeb26n1le1a16hKrr3d9hjdPD1ZGj9VXBa8Ve63bKw" +
                "BX0Ojzyt+7Sq+d0cXu693X8VdZ76rxJ+rXDa2z1vXefKf7eh9d" +
                "0DPwfdlHW0nx+srQcZ4/HKyk+yG/33lO/1erTy8py/S513aeke" +
                "f513ieP3it57mWEsWHY+FYfJ4eg83P3GPYNy+FY7Mrip8YjoIe" +
                "P4tc6y97Mrbn8DNFzv5AY1BgVdRV0xw/47XXGewIjLGn3/iOyz" +
                "p+35QMRKyOrv5VefQdiOOx/Jht9GEX94Nh9tJDbEIeyDyrvrw+" +
                "AA+Y4gn+HPHsewxI5iC2ipCKpY4oOLCKO9Cla1UVO2pzSiz3dK" +
                "CKH4Daki9ddFRQJat9FWz8izbA5ljAfvCC9RCraM0vj4O3Mo/5" +
                "7XXxVs/CmPxrB1snBPVknhesz8Z9LtWk+l3cVK8qa6VeCfS0iB" +
                "1hR7Q7YHNkB/bNy9ZDrKI1vzwOtjCP+e05bPEsjMl57rB1uAe2" +
                "+X/rs3GfSzWpfhc31avKWqlXAj0tYkPYEO2GYrN/A0ZkfTZsmP" +
                "0hPcSWVdlhVaZd0YJ/+gvlmb1JY3KeG+o69OTPus9067IVqQhq" +
                "Uk82Xtbx9dPo8JWtKvUpPhwMB6M9CJsjB7FffJ31EKvo+H4kse" +
                "JnHvNL3dnrPQtj0s1BW4f7VZ7N1mfjPpdqFtfgVvUFbStbDVpJ" +
                "8eGacE2018DmyDXYD07AoxiP1kk/mMjYnsN1noUxc6KmDnbCc4" +
                "PXbuNeIzylp5rbq/eVGfeVFB9OhBPRnoDNkRPYL14Bj2I4GFOM" +
                "zSFjew53WKTGoMCqqKumOXu7115nsCMw5t+PXMfKQAV1T939q/" +
                "Lo2xv2RrsXNkf2Yr+43nqIVbTml8fBrcxjfvt+dKtnYUzU7bV1" +
                "uAd2tsf6bNznUk2q38VN9aqyVuqVQA9zp++Yvn36J4NL09vab/" +
                "+Pr94H+Fr89/x45r8k90MurXUXYfEb6X5I8w/Ad/3Nf1130+fy" +
                "e8vZ6tvRJTJ03w+ZPm9RaaW7te6HoP7gUvOP6h9d8Ar4zUfuh1" +
                "zy+myn4Xw4Xx6Lha/sF98Qzs8eLX5iOApap/crY/v75s8tUmNQ" +
                "YFXUVdOM7+/nu3VpbWqBZ+lnvuOyju/vktHVU3f/qhwee5du+E" +
                "14x38Wr6v8f9WNP6jfX9e+z5Wen/57uHt+PK672ZvWvGO2cvnn" +
                "5/gZd79xxfax1vNz6Rfduqrn58pl7yeucQrhVDhVHouFL43+Df" +
                "0bwqn8/X0VkzwcBa0TqLQuWDKmVeEkUmNgozKqslXTjM9Pp73O" +
                "YEfsVesrd3x+Sgb0e6VeiZ5kjt0f7o/2ftgcub+M/p39O+GBN3" +
                "k4GFNMQqV1wZIxf6t5Y+EkA2Ngw0pV2appzr7qtdcZ7AiM8Tzv" +
                "9B0rAxUU/V6pVyLnmWOXu183/q9+39+nav7o8vfrymNzbX4u9r" +
                "t+7yDn5X7veOX7dbMnX9v9ulL/lX7vSDsgf6f7dWdC+m+Jz8Dm" +
                "kz5TRv+u/l3wwJs8HIwpJqHSumDJ2J7nXYq0MShQFZaZntlTXn" +
                "udwY7AaOsrt82Afq/UK5HnZ4ntC/ui3QebI/vK6G/qb7Ke5MOK" +
                "aM0vjyWvYJmfT7PlVBbEjLp9tg73wM7+z/ps3OfCB1U1N9VDQb" +
                "dSr4T4jDgejkd7HDZHjmM/fygc710ofmI4Clqn+scvcI1V4SRy" +
                "8EuNQYFVUVdNs3fBa68z2BEYbX3lJh/qj39te+ruX5WrRz4vrb" +
                "7Ozj8wN7f8TkaHX8yf5+V1Y/iV+pVveJv9tDH8gvm89MD0/Kv4" +
                "fdO9Ok3N/xs9/FJ6f5+es6/69peWNT/PP6D9Tv/ulT+vmd83V7" +
                "w+qzNsDBvLY7LwlNHf3N8cNi4P6Ek+rBKS+ZzpMaHilbUZrOQv" +
                "nKy5/B6tbXX5OvS09+s2WpTGNZePUGXjyqAKFKmsvhLwOT4O42" +
                "jHsDkyxn7xWngUw8GYYmwOGdvfi1+0SI1BgVVRV01z+WNee53B" +
                "jsAYXyc+4TtWBiqoe+ruX5VH37lwLtpzsDlyroz+lv6WcG7xuu" +
                "KHD9GSgZhiEiqtC5aM+fmZOYnUGNiwUlW2apqL13vtdQY7AmNS" +
                "4Dsu6/h5XjKg3yv1SuQ8cyy9Dix+avRXi4eG6+xrV5qLbx6spP" +
                "+eVj9jDjcPt9S/C1/u92L7N/uwfr5c+/Pn8CNZ22H99Kif+iZX" +
                "RswbWGN49WBleC3rlc+f9Wtc75Mt/k/tf2Hkv28Ot+vrp+9jeM" +
                "/wQOfv7y9fboy+M3h5+bx6Yk6Fqn3F4/3xO/LT8Tzv6UKuqnoZ" +
                "VhGWqXjGz6yloaxH39ZccMbzPNjNTc1UorzdnVf43wKImNfE");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value1 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value1[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value2 = null;

    protected static void value2Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 4167;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuQHFUVniiER4iLBAhZgwlWiUsqCeElaDD0bM8sGDbZZU" +
                "Ne5MXLqKgIGh4iYu7M9M7uzBYI4VGUFRKWl+gPyx+KoEDtmqz6" +
                "h6RSKX+sf62UgDECsYIkobz3nj59zrn39uxSUuV03dd3Xt89e/" +
                "v2Tnfvqk+qEwoFdaKaqk4u2I9aqKapNjVdzSgUksvUmY1hdak6" +
                "W7Wrz6jZ6rNaPld9Tn1ena97X7D6HbrM02W+LgssskiXS2zvMn" +
                "WFbb+iIrUE/A/eqsrq6xrrUleraxpd6qvq2oL3UavVWrVBbVQ3" +
                "6v5NutysblXfUN9UU9QnQKN6QJ2iTlWnqU+p09WntcYZ6iw1U5" +
                "2jZln7c7fOUXN0e54dXWDrC9VFhUKtX12s+19Ul6sv6XaxLleq" +
                "q1SnbmNVYgyW6tKtyzLVo5arXotdp1ao69VKtUqtUevUeottSv" +
                "Vv0eVr0YPRg4WCqaE1EhiZfvltRLgOHSCrHkRbicOYPKb5fEBq" +
                "chkykCz8qKY0drncfQuaEXosFDrvcGfMPRADf07h+XPmGtscbd" +
                "btZmytZDOOkyUSIV2urfPJZICTHdmn+XzE9UIyxm6zjENj1G3s" +
                "lpiUu7bEpj417JuzB20ZWXLgkbh+tCHaoNsN2FrJBhwnV0mEdL" +
                "k2t4e6/B7ZkT3EBZnrz8vnBhmHxqjb+LvEpNy1JTYmfsg3sUcG" +
                "MrLkwCNx/agr6tJtF7ZW0oXjpCgR0uXa3B5xsiP7dH0+4XohGc" +
                "tnl4xDY9Rt/FNiUu7aEpskCvvm7GlePlOXidSPmlFTt01sraSJ" +
                "485eRLgOHSTjOtKGPELc4tNSk8uQgWThRzWlccjl7lvQjNAjzs" +
                "n3LS1CcwrPnzPXWDEq6raIrZUUcdy5XiKky7W5PeJkR/YQF3y6" +
                "/rz1WZRxaMz9hHjJiMQKW27HfQOOzGkWoQgyEtev/SabwQoo+q" +
                "r/H8SSa7i0v83Uai5o6d88XjZ1/+luLmq/Q0+1VwDpn07S4rCj" +
                "/aouv/XyuQKjaOlL2sMZrkZlp9Qi/q69/CRX134NGrXfh+Rox3" +
                "2p5dD2TyOpr4efQZ2XwZeilVq20ubzOEo6V2nez1mrlVjrfK4s" +
                "TPjhOpUXuKT4jMjKM3n2Msqgl/HK81KL+Ifs6WPm5DK0/oY5Xh" +
                "G+0nyyCORf+qlMy6xWQdHYSZn0VP1780aUQq3zuSrEs3Ja2k4l" +
                "/dCn3j6wcxL5XCU9DHh61U0DO4xW5WSXf8iexZ9F+trvs14+V8" +
                "nW9NJ8ruJSXy/jevHARQMXlo8NLIr6rOdy9rNcg73yMWvbZ/IJ" +
                "/daffB3y2dp+oijJtVLL9PgI5pIf3/WfLE3z0+fKo740n32EUm" +
                "SfJ5zv5TGUVE7Mzs1d+jo611qNoa3O59gk8pGrU9zBR80T8+yl" +
                "B/98rx6QWqbHR1vn5MTfFWZYGc6fAeSToxQ5b6bFevF2U0NJf2" +
                "Z1ktr69kqs5pI8/5OvU6zz8z1fS3rwz/dKaWAH15LMYTatmEFL" +
                "53ur+JRPspZ++HmVnu+jA4v4WrblMKxZjYwCVj6s1yf2vXMt0z" +
                "5cHmWeDgumz1EUq6nrrb3uPqFjjGKEzGt6pBqHHa1RZAl8t86R" +
                "/LL+YdKXTJz1OcrzSbw4P4HY+ZcKpfR8x/tLhYLBCrYUx0opUv" +
                "kXYMm3zP2lUqaFFlSs1h1wfwl0ku+KfD5LUbTmd0qeJ7y/VMr8" +
                "mfO9VMAjPXf0+lOnqtsQUWeUCmomWqhzQ14LbE7q8lJBMLnNuc" +
                "O1lOcz45X+dqTWqG8DVjnEcxD1RD3prtuT7b8WMyXZEvXU7oQ+" +
                "FtQlSylHnPrN6SKfOylK1NOcBi33JHkgBnrErXma1OIMQ/yyPe" +
                "z77ozBq7t/Or9v9PiIxNKYvVF6rkXZOQeYKcU3ol6bz14qqEuW" +
                "Uo449ZszxZXlRYoS9TbPgpZ7kjwQAz3i1jxbanGGIX7Zz/MNd8" +
                "bgdYJ89vqIxNKYy6N0r42yPRcwU5L7AcUx1yVLKUfc1cl+X3md" +
                "oqCO9CR5IAZ6aNP5evIjqcUZhvhRfHfGkkFOPpf7iMQgZvxh/G" +
                "G6n+5GCWCmlD8AafxheTdiRhP6aAk4ya3lbvLk7PMfUBT0Iz1B" +
                "DBOF/KEeYvXzjB+uZXrADPn6Xq3lXD4LzsThuZuP3CxBZI6kcz" +
                "kaH9XtUWyt5CiOkyFEuA4dIKseRFuJw5g8putjRGpyGTKQLPyo" +
                "8dHOkWaHy923oBmhRxnfnxnq1uf4cwrPnzOPj0bLomXpel2WrV" +
                "yLmVK6ClAcc12ylHLEXZ3sfBulKKgjPUkeiIEe2nSONhdKLc4w" +
                "xI/iuzOWDHLO92U+IjGIWZpSmpKu4F3ZbxcWM6UzKk0x16PSlP" +
                "IuxIwm9NEScJKbGrxxnex69EuKUprSnAYt9wQxTBTyh/qka67v" +
                "XMv0gBny9b3afEZ8FuTZ+360i4/cLEFkjsD8o+6oO81vd5Zpi5" +
                "mS/CTqttf3biqoS5bVg1yOuNRh+fwLRYm69fW9m2txfe4J9Ylb" +
                "dn3vlqxCsbnX5CF3xuB1gut7t49IDGLGx+Pj+rw/jq3dCY7juL" +
                "OECNehg2RcR9qQx/Q5xTlSk8uQgWThR0VuYV48NnFBBObk+5YW" +
                "oTmF58+ZcwS+PalXdbkrOzc6Helrre8dpd8dFrXSiS79mO4vPd" +
                "z6/lLe/RCcU/mYef7+Ue+QTXR/KZoRzYAaWsRg3NkVzTD3l6SO" +
                "q80L4eSdbO3P8TXupfkQl7nMyB+OSLd6wOUu5S5HRMyc5IyhXx" +
                "mW7N3IMlc+61TWFrXptg1ai7fhodfAo1Fb7U5CSBd6MMIeFNkz" +
                "bfMx7r8xn/tpbuMyxq7Nj0OIvR49GuZFkSUjZJNs4zOmvs6n4O" +
                "FGlqw4xvXVFvd9Gz2i922eVGea65HSez+8bxM3JvO+TdywK7EB" +
                "79s0d9D7NnFDo/Z+Nbxv09weft9Gyyd436b5lHnfRkuC79sUCn" +
                "nv2+j4wfdtKsP8fRuttVQX730bXZz3bTK+9n2b0H5FO1H17kLB" +
                "5FPex3N3Kr67VDbl7YaoV5/H7ZvbWu9fpq5sDO1mzUflHoYxEI" +
                "P9073jp+NfwPmQ38qw3LP5PUg/DulW7xHn1eJoMdSmRQTHyfZo" +
                "sT7fF9NRO5ukaIk9KKauzTS9+MfolftHFLDmdi6TvNw4hMD6lJ" +
                "iUc1uqkZWUQ1+f74yH0eKa3KsbCRlTX/eWREuoD0XnUzybAB2s" +
                "Ca0e5JbBa/oS8t/okfa5vwcs4R59/WQn+URe3DO3x9q0yVOcD/" +
                "fLc0BZcH2CrdQNsL8yupL6UPTa/rerEw+AptR2+/EA1WgJsnjA" +
                "eJBWeYy4R19/6CLiDB4lM26PtWkNK+Ij2dPIsCSmfhyuG9z9+f" +
                "XoAV3sPeDkaaEzW22NK+Z6pPvnZ2gH01iQsqlQ7b7/qT3o65H6" +
                "8UT5VGttfWM6utnL58VwPbLS7Hqk21l8/wxcjzQrpcz1yGJspx" +
                "HXo4q+HlXgekTPj+T1KMQ63h/v7zxkamj172fpyIyLd8X7a7MA" +
                "Jx06wAPKUccgiKMO9joPcW/x/sYCLgMrc5CG9Ew8a+0odXmRBW" +
                "iCV0AKBTMnboX9yjDZynnxCC6T7FtCqhv6HkLXyehXdBV0r3OV" +
                "x8PP4/iVM+S9sYJf800E/jwu9B3Jfx5nylBf6JsKxct7Hodzkt" +
                "91Jvo25ssrT0gMosd74j06r3uwtT+ZPTgufg8RrkMHyKoH0Vbi" +
                "MCaPaT6v55rJ81yGDCQLP6opQze53H0LmhF6xDn5vqUFSiSP0P" +
                "w583gP++mO+esz+Vn5mPm+qZEx9jx4zP3dzV+f+P5DYH1u4ati" +
                "6Jrw+jTvC7R+XozvM2RaY8gS32cIr8/kBTkL9NzqfQa2lsc4P4" +
                "HA+twb79V53YutzfReHCe/iPeafJJOXEMpaKBM6Ggt26+RDvZq" +
                "0w1KmkPLSIbesMdZyaimVA+43H0LmhF61HXNnTH09f7JLWrAX/" +
                "LwmbD1aWVsfY4E1ufL2doYYetzZBLrU+tU3w+tz3i93LVy1ueI" +
                "8dBy/7zZ0RpBljDKW5/xejmL8P5Zv7A8Uj0SWJ8jnJ9AYH3ui/" +
                "fpGPuwtRH34Th5BZEMraMUNFAmdLSW7ddJJ+vVDcpjkAy9YY+z" +
                "klFNGbrF5e5b0IzQo2XgzJh7yHTrwN9l6jJhPyeQjcfj7n4BmC" +
                "nJa/G4Pd/Hy2OAxf1GE2zQEnpUjJbRifvJe6bZbzxglHh8aAu0" +
                "of0TcazxAAT3T9QyPVOqJRxJfoxBps+ZyP3TzqCfjcf9/RP9yB" +
                "zY3yFWmyP7jWI1FJ3PP+iVfwlh0ep4EDSltqy130HTiwdJB/3H" +
                "gyCb8NvRaunRt6lfSl6RHTFzJYQBK9e/uR/Cc2BYAlM+W4iBR4" +
                "hZNC+aB7VpEcFx8udoXmMuIaQLPbKnInvUov/y37ifoXu5TPJy" +
                "4xCS3k8O8pIRiRGyMfGlHPo6n4KHG1my4pjUt/0bzJF5ugGKzu" +
                "cbEuM1l8ha9lz/9SukLGd93iA9+jZD28kr8iJmroSw+uUh/xyT" +
                "WeCzhRh4BGfaEXVAHXUQguNkT9RR/zIhUUf5CEnREntQTG209E" +
                "o4gl7Jf/kIouSFYktebhxC7M9lscSknNtSjaykHPp6fTIeRotr" +
                "cq9uJGTM+mvNkc1lLRSdz3GJ8ZpLZC17rv/SO1KWsz7XSo8hG/" +
                "KKvIiZKyGs9E7IP8dkFvhsIQYeLZgtMAeNoOh8/lVivOYSWcue" +
                "67/0rpTl5HOB9BiyIa/Ii5i5EsJK74b8c0xmgc8WYuDRgtl8c9" +
                "AIis7nAYnxmktkLXuu//oPpCwnn/Olx5ANeUVexMyVEFa/N+Sf" +
                "YzILfLYQA48WzNaZg0ZQdD7flhivuUTWsuf6rz8erZtEPtdJjy" +
                "Eb8oq8iJkrIaz+WMg/x2QW+GwhBh4tmK0xB42g6Hz+Q2K85hJZ" +
                "y57rv/RWNIm/mEF91A3ZkFfkRcxcCWGlt0L+OSazwGcLMfDIZy" +
                "a/wdI39eRdieU/3wx9o6++H3y++eTk/n5JegzZyKeU8vkmvh/i" +
                "P98s3RPy798NKx+rHvHv+/Ijn1m00Bw0gqLz+Z7EeM0lspY913" +
                "/jPinLWZ8LpceQDXlFXsTMlRBW/2nIP8dkFvhsIQYe+cyc9xno" +
                "+VGWe9Vunx89Av8/xH9+pLbx50f4/0NCz4/qO1V54nyav+9o/f" +
                "xIY+b/hzwcfn5k/n9I+PkR7J8TPT/i/z+EPz/i/z8kxTaFnm3I" +
                "8x3H1evwrKj28ftr8szy//7IP2vZ+f5zieU975Bx/PPM5e7K4X" +
                "5d4H2GF93dgXtw310IP8Nxmfh7jsxQls+7Q/vT/5bP0n3/33xC" +
                "/Px8TuaZGI+E74eALGqP9JlsamixD+Pi+RKJ2vunkhRl0h5x6J" +
                "m2/xSU2/3zh9ym/yQuY7tXux+HEPPpP1liUu5zxJbPifvuP4HP" +
                "gWYRiiAjcf1odjRbt7OxxT6My29KJJptEFdb2iMOPfQCPZvPGr" +
                "eRMpbP2X4cQux6eFNiUu5z5GxCMwZ/yJxmEYogI0l9/6lTJfuv" +
                "DMkfJ/OOqXiKumkijdL9hY/4qWz8KNrJn/j7DB9H/PCn2gjC/w" +
                "VDKVOa");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value2 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value2[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value3 = null;

    protected static void value3Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 3108;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXH2MXFUVnzayVGu3C9rtVq11Te2HlqzVCAq1zNs3C4ooFD" +
                "+gKlqXFiFF/xHjP4b0zrYzL7v/SJRsbQqYTaroX6Kmlq9S0rKt" +
                "IiYmJogG0/oRpWsTpCViLMF733lnzjn347273e32Te7XOb9zzu" +
                "/eeffeN2/ejLpbvaFWUxepLrVAl3tr+lAL1WK1SL2lVmv1qrfm" +
                "kl6d3q7eoe7X5bvUu9V71CpdW53r1uj0Xp3W6XRZLnl/rTjUh9" +
                "SH8/Kjqq42gmy0rYZy2TXqWvUxXX5cfaLmOdRmdav6ktqia1/R" +
                "aVhtVXeoO9U8Nb+DeKN6k867VY+6RJeXqiVqqepTy0C7Y4Vaoa" +
                "X9OXJtng+o9bpPS9QHdP1ydYX6iC6v0mmDuloN6jJVDRb/Op2u" +
                "1+mTOn1K3ZjLNun0GfVZ9Tl1i/qC+qLge5tO2+rd9e5azeSmNA" +
                "fUoZ0sJy3KeA1aWIMka1Si/9FMxuQ6OmREm4ONkQgekRghG94n" +
                "7pvYoncZ2Y5IMok3x9BZ80JPpgat1tukjOdc49a5VPpvH5I6/4" +
                "F4xPpsyCvyImZDZ3eskLyxbD/p889lchR4byEGvnzM6uvr6yE3" +
                "JUqw3VpOWpTxGtlTkjUq0f/gsIzJdZKXHYckNkYieERihGxMfK" +
                "nnHjgDGdmOSDKOrw/UB3Q5AGUuH8CXHs/3kRZlvAatkVPUAg9U" +
                "oxL9Jy/KmFzHejPgxiGJjZEIHpEYIRsTX+q5B85ARrYjkkzi87" +
                "W02I+K1j06LTK1xjMdmZ75ej96EPYjna/qaNawNfmyTu2DYq3m" +
                "+9F3YT8qP9TNanNebinawx6M3o/UvXmtsx/pMt+P1HKY72Y/Uj" +
                "/g+xH0yexHeXkV81eyHxWyTerTtB8Vsi8HVqxJd+1r3SG1xZoz" +
                "6VsxrZUniBk9UIs4hia5B18EG2VqJo3sgRaMp3u0vmr3t2o8fF" +
                "KKHPIzdAhr6UNYazwrtaAhJMdKWRgzejBqPA9xD74INsrUeMvH" +
                "jPepzH9Yj1KKHPJD8z39YSf2b61+5Boz311sWCbbyUO1qAPme2" +
                "A8C4/m+rODv1SnpeXMeJ9gvpfEv65Ue4u6y8tsPB2HHEts6dcy" +
                "/RpvbTcSicEXWXCMqVFbSnVrmZRxHVgRM+5dRvVzty0IyfDLpJ" +
                "Xbe2LiMrWZ8JHk7IXmR5215gGdHvRp9HXcNbbEh3LbyVRtxofx" +
                "uHNtNcZ3zEb8vP/XeqPuTfdCjiW2TK3143Rv6ydGIjGEwJxj0r" +
                "3g2fWX9+eklHEdWBEz7l1G9XO3LQhJeB7f7RkxcPvk6z8fyVy3" +
                "O92ty91UYsvUWk+Z3EgkhhCYc4y0kVK9Hz0vZVwHVlgjhBsVET" +
                "5eMjZ5BUnyiLRye5+/64+4ffL1n43nbs5er9NrPddLh/3XKdXX" +
                "S+FjcFGZtrE2brZVewldLyVPxfivRlUxbXSuy9OJzngesVaHCW" +
                "e9mPCsIRNl7cHuOBala+NEtRcfs3ykfhk1npUoP9N0T7oHciyx" +
                "ZWqtZ9I9gJIYQmDOMdJGSvV8f03KuA6siBn3LqP6udsWhCR88r" +
                "C0cnufj+fDbp98/ecjydnr8V7tme+/iZnvI6fKUbI9trz0XV8d" +
                "dbVfcYfKeAnO9yejzs9KlJ9pY2VjZX0f1KCs7zMyaLeeNbmRmD" +
                "ZhTB1zsEC9z9PYO9ldhn2gg8hgA6XRgS1iUI45vtA7+UFbzoyQ" +
                "xCgfqV8gb+oNxyKKItge7JHjY9Dob/Trsh/LHNmP7cEdKOEYep" +
                "GOY6QNeSzOzxUSyXXIQLJwo5JXHy8em7igBPrk+pYWvj75+8+Z" +
                "N/rrXfUuPbpdUOYj3YUvPd//SlqU8Rq0sAZJ1qhE/2OrZUyuY+" +
                "dxlxuHJDZGInhEYoRskkdldMmkc34+ake2I5IM2VA9H9/8Dlw6" +
                "CanYi/Xno/Y8tuZORu2+k8YueKXzQOmqtGo2PrsYLyGuFD+uNz" +
                "Njmh6BVHymWulq3cPej/yozn60oTYnR4hFcjCGZ3JwllgchlS0" +
                "vudqI8bzcOl4NuZoPAMsksdieBJqhiyOQirOzw2uNmI8j5aO55" +
                "VzNJ4BFsnPYngSappR96f7IccSW6bW3mhyI5EYQqCOY6QNRoGa" +
                "Hs+bJZLrwIqYIcKOSl59vHhs4oKS5IC0cnufj+cBt0/+/nPmUi" +
                "K/K87Pz6undx0dgxrbPjfnZ/B6/vEYnoSa4Sx5GlI+j59LG7Z2" +
                "Z2+Mj535ffLUu1Kmc7V+Ph2QN6oQs8k0PQApr69L1+lz9H6uNZ" +
                "JqH4DyYcHnnIxn4Hs/ip9WfDM4G0zTY5CK+f4rVxvno2S+3zVH" +
                "4xlgsesb0+vN9I56b70XciixDu3GRinhLUJLe5STd9Ln47nDti" +
                "Gd5OVnRVg/LxmROGKZPOHvMfUlXz+fsCPLeDwSsskRffU+XfZh" +
                "iXVot/8lJbxFaGmPcqhRCTU9nk3bhnRsPPvcOCSxMTZvt0fEJt" +
                "nl7zH1JR/PXXZkGY9HQjYm0TNe/FkubGfzfM9b2U9e2c9NSU/k" +
                "sRjPn0uZyXfc6LtK4HHc57D482kSgfu7zQh0yX776THuobO/75" +
                "f9krnNxL0y0h4yzKFmjqxLaofONlOuL7neyKavkagqXLMhUZJ5" +
                "kgWvl7IYHr74+buR2XqJU+J52kLWeZ42u9h6nvbwzJ+nHXvswj" +
                "5Pm/w05nlajXKep1VHqp+nTafSKb3TTWGZ73tT2E6OooRj6AW6" +
                "kVNoK+XQJo/FeD4ukVyHDCQLNyp59fHisYkLSqBPrm9pgRqbqd" +
                "t/zjydks+aOuvngpj1c+TUtNbPX9te5nb9bD8Xs362/+Cun2Em" +
                "pIP5rs9U/3xfaM33o7Mw318w811NXrD5Pj9qvs8/x+fne+o9ep" +
                "/vgTLf93vwpcdzEWlRxmvQwhokWaMS/Y8dlzG5jl0v9bhxSGJj" +
                "JIJHJEYdrq/L6JJJx/frdmQ7IsmQDdWtM+ObnT30JktzLPS8fP" +
                "zdkrET5/dzEc33wM4d5YWj1A3T+Hx0sn4ScihRBu1scf1ka5wk" +
                "hOVobo9y8kQei+8bfm97IZ3kxeNQG7Ht41Im9bYtsTHxfb6bE7" +
                "xX1AtfBBlJ4t1r0aHfoTTr8d3NizsPQ2dt+y/nZleFsX+LETo/" +
                "2yfi/Jfry0ehfgZSMcs/j9LsEh+OkFEzwEIP/vnc7Kow2AeUhe" +
                "wxfrn/5q3l+tAoJC+55yfIjARr5ecnfN+BWJ+NWD//M7vnJ7GN" +
                "Oz85vmT9fGlG5+crkIr35jaUZvpqqTVu4wgZdZ5Z6MET52bnzN" +
                "vjEoN9QFnIHuPb+qZ4fqx5e3n80Cg0F+bvRjekQnZx8R51N++0" +
                "3rUcl3ifbWu+uSi7XBsxDn+PG8+kOw7TXMC5kV3Ivv23GP/N7e" +
                "X60CgwxCjmUMvvvR6VWjjHSV/lzTevYqwNKi4KoZD5rmPQCs73" +
                "0RgeofhkXeYnWQypeG/uRmm2RL+f/7BxhIwaGQs9ePzc7Jzz7J" +
                "8Sg31AWcge49t6a75/qzx++Sioi/jvZfB6Xj2fLbVwf2z2qj/x" +
                "38tUH4rt503tr/1ipN3mivt1Pf7fy6gXKq6X7ivw5+X3MsXq+j" +
                "IkeX7WX876fDhCBvv6bWkj+hP5+4rqKBKDfUBZyL59MsZ/855y" +
                "fWgUKu4vrZru/Xn7t9ye+0uvXtj78+1/x9xf8vUr5v5Sejo9Xa" +
                "uZHMr8Tt5pbGdrUMIx9CIdx0gb8ghxG7dLJNchA8nCjUpefbx4" +
                "bOKCEh7f7RkxcPvk7z9nnp6uuF+31rpfd7zqfp06UXW/rv3Khb" +
                "0/3z5zPv/vwne9T/Mku9xdG+L+n4Ef2RXi/xn+O52ndqr+n0F+" +
                "bnM/H3nm+6tyJQp95vF9z+hbVzy8himX9+uyK32YoeGI0fBimi" +
                "N6vm2KGs3hqijNtkSZGm+F7DF+lX+/HqXk38XZ71TzO53xvN6H" +
                "8X9+j/303fha3Nk5nX8Z8dwjOxu6XsL45f6b9/r1/J9EvPeAtw" +
                "xtgRxKlEE7u0FKCMvR3B7lZEf2xXw/a3shneTF41CbsFIm9bYt" +
                "sWn/z++bs6d+uUxtJjZe59soz9+bzlPJzfs63Lf5kCVnThDT+H" +
                "rU+bktLgqhTI23QvYYv8q/X49S8u/ihrZSbs33m3wYQpawCWLa" +
                "r0WN59a4KIQyNd4K2bfPVDHMx+D7fj1Kyb+F+z8x3BoE");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value3 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value3[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value4 = null;

    protected static void value4Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 2881;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXGuMXVUVvjxaCQmlsQ4MtGYSsCJCfYAGHJx4b+89tswAYq" +
                "wWtGBMtWViaSsUjfFP97mBnqu3ifjCRyW1iv7RmBgUy8NmoIW/" +
                "TibDjDTywx9qjMQQww91DO6z91lnrbXfc+e2xHtz9tl7rW+t9a" +
                "1193nsM5PTaOhPtpQtla3uQ6/Y1iAfwKAe0K6+aQm64jwfyrSh" +
                "Hl026BN4UWYHx0wvWl+cS205ezMXuvE4HOvzYda22M4rzLODUf" +
                "cVrqe1tr131vIal+3B20PcsBquPMyqgvzgmPs376w1fyOX1+J8" +
                "168bkunonZHOiIwxAnsVcQTGxZ0goRj8oo5iuA16rPJZw5FUBw" +
                "w4CzsqenXxorGRSy1ZY2ZMPSADOyd3/pS5lG3obFC/xw6iUbJy" +
                "K+7SWhjrEexhZOqpHPv1L78eddrGhQJEcTd6ha+N4izs2NTm0N" +
                "U8ixADGiUm09HFAXG+iRMPiItlu07m/qB4i5JcIjdZB/Fqhdio" +
                "2qtgxot3yu3abElsUqP31J7eL2606lmxEB8WW8RWub9ZTLlzEH" +
                "eJu9X+M6r9rLhHTItzxLkGao2oziBCzhAxiudPMVZjrib1vEFc" +
                "V0k/UO0/KD4kNofrKRxnJLGDjXbK7XONRr4pvyaXOebvE3tl+7" +
                "aynoDJ3849dN/tipVf6eaQW7XM3yF/w8sbQ/nkYxH9dZ6Z5omf" +
                "b7Qk1y+T0bhV8SN6ftZz6WG5fYPU87b8I7af7ivJES+V+awfUj" +
                "1vCesPeuo9rPjLYPqJurfdqNwDK/R8h8znsrOUxcc99Rw4vjiZ" +
                "jFwltwvqiNUvL/6QHZP3AMcI7iW5nRZXLIvFy8b586pEuzsjVw" +
                "jJUlxI8G+Wm5z/4o88C/NzqDqXihsi8SeD2jvEnrB9q9+6t2z1" +
                "VuX+fdSq9t68TfUhb8vXGGwiuLzDUZy5ziYUP+zfFx+t0/wopk" +
                "fq3g+NX+WlAY4SMj/zRxuNzRcP42iOe8l/4KlU0vWwteKrZnOb" +
                "3iSTAzWnn1Mtb4O5/MKP6a9NYxOPwlHIP2xfTNKM3HWQGfySez" +
                "YjoH+fn/K+rflRfn1vTfO1ldanrL/9mP5I2uo9LQpfU9KR5uqY" +
                "edNhhtoufxz14Knc0zVqLFO8n69ZPaq3eJXSP/1Lh3P9pryWc7" +
                "+UP5EyE/LfDvFO4+m690zqrEufqZt/cnbul3z1TIuf/25F585/" +
                "6q3ydQKkxSYXDpGpvtn87A1mF8NADiDz2RfXpvkP65dThfzZ+l" +
                "xjrMFaN4fGptYt7X/1zM5LiOdmY+fkqcGpIR7vX6+P1S8ZXLa6" +
                "xrDeNLVuaf+bZ7ieW0Ns7Jw8NXhhcAZddaeG6yOyfv9VLVP3Y9" +
                "0j+ZzsJ6yPxPXO5yHzsp5Hku5at8fWR/nvy/WR+Iu5PqpGb81n" +
                "Pcd7O2V9VHTEpOt5iPiYuT4Sn06/vhfPcc2DJ8t6Js9zdz2PDu" +
                "koWozoPfVsRe4vurcAarnPl4w4X4NW91Q9XzW0U2q9WetbU+x4" +
                "nzK9mVKuCbOJ4dR6k6CAuY4nNVOhPGM8fPHROs2Pcf78suEtC9" +
                "0Lue+mTJv+j87s+RNYmHF9OXlq8PIQr0f1erN4zajNZGiMo/xP" +
                "fpv+sTN8PZp0x63165K8rBsw+j/stZqWlRLo8TWdOQ/18Q5Ylw" +
                "2r52ODrQN8GGTL8/Ddz1N8qC5hfdp6uJpdf657f03JMfx83qrn" +
                "T9/Y9VEr6flBGsqT8RewNer5NxcGkf56mhhSz5+lcfJ7cKPKHh" +
                "357FujMYYa5daDFP3buGwvtsb90m0uDCID9dzrreevk+q51+/B" +
                "jSp7dOSzL26NMVQz6e9uPUjRv43LdmKrfNUr096GRuOhR0wMIg" +
                "OZejGbf5xUz52xKIcu4KiyR0c+e4hv6nN2ncxfc9uDFP374rR6" +
                "0LbqJxa9MUPbwX74fgkx2salCR5vvRQcRQFzzUJqOqE8Yzx88d" +
                "E65qd1GFrdU/W80tBuyf9F9a0tzMMW05uNoZpgPodjuPzfHAXM" +
                "dTyp2RLKM8bDFx+tY36ymZrpf+t63si11X9mzSScP2e81/ffJB" +
                "3vM2aUGKrs0ZH3efJ/TIbOe5Zz3HqQYmQbl92PrfK1qq7nuAuD" +
                "yECmXkz/eFI979ceejd5M16NKIhIRz4G3fNiDBXqTW49SNF/vB" +
                "p4fe9NWL/uip+HdL43nPvL7oWDPQ9Jjz/o85BsH7aqV/+Vo5e5" +
                "MIiM+XTm8+00TmlRCOt9LAevPcSP+XfrQYr+4zxx/d5dI7d1jf" +
                "/Lj+/82b3o7MTP7qkjXlLPz0lby/txb9b8+G4am1iU7mUcVfbo" +
                "yGcP8cP+u+vdepCifxeutVpv/PzZWt0dc+EQmXTnY6A73xnMLo" +
                "aBHEDms4f4Mf9h/XKqAMd7d6E3NfzjoPPIkK5HVwx2vA8rfrDa" +
                "q/RWMd0I0t6tLhwiU32zI+bJwexiGMgBZD57iB/zH9aHqpDtx1" +
                "bV85r6/LnNhUFk4PznxRSfSjp/7k+LgqiyR0c+++KTMYaqBu9y" +
                "60GK/jmutae1R7d6DzI97u3jEsRSNLUHOdqhfRX/adML6jgvGg" +
                "fHgO3t5zKuN22RTRnf5Zuyx7xspiYTjm+/3n7dzEXLym3zqNbC" +
                "GLC6j5ZcT+W2d6oDP9yTaQ0tfF1sKQs3v/r50ijFUyahj0vPZV" +
                "XMpbZcX5et3ivNEoyLXSChGPyijmK4DXqsIzOkrYMeZcWjolcX" +
                "LxobuYCk+7iZMfWADOyc3PnzrMpe8zHdlvuybVZ/4UEJYPS+Sf" +
                "4CZI65JWK4htvzvmZg4kwbys2OWPbK6zsdmz1XJpyni6kdj8vV" +
                "Ge0+bNX9Uv33ju57XRhEBq4UXkwn6b/6s/vSoiCq7NGRzx7ix/" +
                "y79SBF/z4/2bTjed0BW8v7XjbTjRV9sum0KIgqe3Tks6+f101H" +
                "ntdNh/JC/xzXHG+O67Y5jhIYF59HLchoD+1x4z3cN8fdMbmOYs" +
                "w4KDExHEEjIiOI+9BuHp0zoQx4ZDMiyji+Gk00J7CvNzk/v8hy" +
                "nNAa1GspPk9uTnjOeBPov3Oc23vPkhPUowuPPoEX9UztoS33ne" +
                "OUD/VLa4BVMH1qW451zuRd5vmzu9C9ydbyftybdf5K+s/fbFda" +
                "FESVPTry2UP8mH+3HqTon+PaL7Zf1K3eg6z8yvX7V0BCMfhFHc" +
                "VwG/RY5fMCR1IdMKAsuGeTp4sXjY1cQELj25khAzsnd/6UuZQt" +
                "tBfkfgH2SrMA414XJBSDX9RRDLdBj1U9T3Ak1QEDzsKOil5dvG" +
                "hs5AISGt/ODBnYObnzp8ylbLG9KPeLsFeaRf2V87MPEorBL+oo" +
                "htugx6qeMxxJdcCAsuCeTZ4uXjQ2cgEJjW9nhgzsnNz5U+btxf" +
                "D7LnqHU953Yb5rI/K+i6dQ94a87+KplPdd2FxiMh0d3yeQ7cbn" +
                "yfp9AnJ+PgzvE8h2q/cJ9DUS3idQW6j3Cchtk9wOl+8T0N5c7x" +
                "PoPF/ZBN8nkO2WUaLvE1Co6n0Csjcit1Gicb5PoPO8fp+A1Aff" +
                "J4D1UJjbuVT530El+n0C7fn2vJyn87BXM3cexr1vgYRi8Is6iu" +
                "E26LHK5xmOpDpgwFnYUdGrixeNjVxAQuPbmSEDOyd3/pS5lM22" +
                "Z+V+FvZKMwvj3gmQUAx+UUcx3AY9VvV8giOpDhhwFnZU9OriRW" +
                "MjF5DQ+HZmyMDOyZ0/ZS5lc+05uZ+DvdLMwTg72p7Ljmo5YvCr" +
                "0XQz5dRjVc8nOZLqgAFnYUcFbm5eNDZyAQmNb2eGDOyc3PlT5l" +
                "J2un1arYSeJRola5+W58/ntFaP9QZjHHE9eKR9dv48hVEAY6MA" +
                "0TuJ0eBLI9MYXEO9UnznFM8ixIBGicmq6P8D0VcRGA==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value4 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value4[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value5 = null;

    protected static void value5Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 3007;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXP+PHVUVfzW0EQna1tW69Ntatyt127igfLGozL55r1htiS" +
                "aA4LdYQCr4g24sVaNi73sbnL5EE2IRN9hdLEq0hmqiScVUY9Tf" +
                "JWmw/4G/tLuNCZFgavTeOe/M55x778w8ln0lOJP77ZzPOedzbu" +
                "e+N/PuThsNdyT/okJH5x+NvrR7a0MdhGNk98ONAQ74paP1u0Zj" +
                "OXZ1GM6BZWX2HL/Of7Ues1B+TH9Fz6edsWYc8WqO1p8al+Uo4z" +
                "rc+MmOZAfVrmUJnd1z7aeTHa7ghN71YI+ie2jZf+vPOqbUaV5+" +
                "HEh8jEbIiGDEbFx8rZceJAMd2Y8Imcbn/Ul3Fp4mqTQa7QUqLJ" +
                "O1ROta93z/rTNaV/LvPKk9hjbtBXhlXmDmayBrnYn5lzI9CzJb" +
                "isFnGTP/EOv9IzVrak/5qHS9/2XI63xPNZvhxm9fal+i2rWQ0b" +
                "j1S5JKDLRSh8I1vIf+pczVRz4eZ4Y4PPJ5hrxYf2Srz0jmFPqG" +
                "P7CUCD0PmonE59fsFncWV/oWKo1G9mUtk7XU6Fr34v5r1/sW7T" +
                "FmA6/MC8x8DWSPHoz5lzI9CzJbisFnjJk5ZK6w9WqzxryxkF1l" +
                "3mKuNm9tNI4umZFc8nZbNppN3f22HTPbzHYzYXvvznXX2vIeW3" +
                "basiuXTBWebjA35+2HTGKKuyvTzus95jZjP0/MXvOx2Iyae8xn" +
                "zefM523vgC33mvvNF82DZpV5Q4G40rzJ1m82a8062643bzMbzD" +
                "vMKF+fZquVvjNH5t8T5r3mOnuNzJjrbf9Gc5P5gG1vseWD5lZj" +
                "72RMalr2M25f3/tHbbF943K+3eQryHzCljvMneYuc7f5tPmM4n" +
                "ufLV/oz/GYO4t/mTEqdm2c1DJZS42udS/uv/b6HNMeYzbwyrzA" +
                "zNdARjn5/qVMz4LMlmLwGc10Y7KRamq57850JB1JNs7OQCL1QG" +
                "t7lsM79O5wPrUNdJpXnBVjs6/GeemI4Mitix/LuHNC5oAsYhF0" +
                "JIlPNiebbbuZW+7T+Oh/tUSOgNb2LKceWurZz+xnfBvoxHxuDu" +
                "NA4mN83mFGYOPixzJGLuxdR9bxZCSNz+8t1lPR90vT67ve9y7h" +
                "gBzo/sVDZw8vz64OwzmwrMw+OzSY/2p92SwkU8kU1ckUJDzurY" +
                "KWZbIHexTdQ8v+s6/pmFKneflxIPExGiEjghGzyQ7r6JqJZKAj" +
                "+xEh0/hGo3mRJNQ2L/L12bzYvavRmB1lvcTKI5SxpxhWS7Ovh1" +
                "Y+Ju599ppqDke2Smmsp/udE74/7bM6c+6lx9PjVHPLI9frjbja" +
                "STQGCK4lRttoqfYm/UIHZtK7jhrn7lsACXz2bW0VZi9Z+kx9Jj" +
                "j6url0zrZzaHnker3trnYSjQHC1d1F6klLjLXU3k9/S8ukjqy4" +
                "B0QYlRExXjo2vJIke0RbhdmDScjUZyLmc06yV5qnudfbUaYpl4" +
                "QyPc46r/4pORZ1UAx/Hw3nSJ9Mn6SaWx65Xm/S1U6iMUBwLTHa" +
                "RkvtJ83zWiZ1ZAVm0ruOGufuWwAJvIwfZgYGYU6x/OVMSvb+k3" +
                "3/+rzBxj8X00hJd7HMPvptcm5lroQ6P+776JXYrQyv9Kn0Kaq5" +
                "5ZHrNY+62kk0BgiuJUbbaKn2Jv1CB2bSu44a5+5bAAk85RT61h" +
                "axnGL5y5mEbWdTcdcwbp/qizvu3k3ebxQng18tng2vTzNV+Qvh" +
                "yZW5Ph/9Z82vuNcPN37J9bmQLlDNLY9cr9dKFwilMUC42n6/L0" +
                "iMttFS+31wTMukjqzAjBFh1Dh33wJI4LMfaKswezAJmfpM5ExK" +
                "9iW/z9/jPWUdrBr72ri0/dOVuRK6nyp5EjxYxWbl4leuDbXexX" +
                "zeO4QdgVOv8Xo/NdT1Pp/ONy+6mlse5b3H03n3vJnOE1Jq3Ql7" +
                "12OMk7g+YSHlp0kpy34odWRFz5syBkaIN3uN9C55wYKQ5JUkLo" +
                "q24n7nBGzZRueks+E8+XlT2nor6b6id7+3lvZVjX1tXNo+O+T9" +
                "uH1VbIYdH7tKrW3+Ppf9Ptof7rnJ/S1/L6psP05FfEHut/n7cW" +
                "Chd8HC/bYwBhg4L7QfF94Rt1/QWfi7hWXzFOyTbovtx7WmWvbu" +
                "xtXU5sgpHvfuZInE4IROYrQNPPbj/l0jpY4ZaBZhVHiN8ZKxwY" +
                "UlMn6YGRiEOcXzl8xJ0j6MOl/lDxa9h4rZPxxDVvx7lmKyuYHW" +
                "zeHBogDlenJUZp/9qI5hlZ6l8K9xrfHWOPe0LC9/I2kxVlgedR" +
                "e13vdU8ncF4xxHoiSe5VzzGfqBreQb95od9zOOeQ3Zxvj7CCsb" +
                "a7k9vzFuc81YMT7LEonBCZ3EaBt4LCIrZKjjnmSlo8JrjJeMDS" +
                "6F5KyfsfQABmFO8fx1Vunp9LT9nj+Nlkeu13re1U6iMUCwTmK0" +
                "DfW5p71Jv9BxDwg/KrzGeMnY4MKSbF5bhdlLlj7TMH+dVXA/+h" +
                "yVvL82XRtqB7inrUT5Pod2Z/1cXfzBslnOkbxERT9vJi91Z2I4" +
                "IAf1rb4PTi3Prg7DObCszD57djD/1fqyWUg2JBuoppb77kz3pn" +
                "u1RI6A1vYsh3fo86tir28DneYVZwVsnJeOCI7cypykb+TC3nVk" +
                "HU9GkvhkNBm17Si33Kfx9G4tkSOgtT3LqYc2GS3mwbOROomJs9" +
                "J+Ql46IjhyK3OSvsGLvevIOp6MpPHiSW0m+H3pG96z3MwK/K5+" +
                "5eX5/CzjOtz46YX0AtXUsozG07ewRGJwQicx2sZ+Yj0DqfYm/U" +
                "IHZmClo4JnjJeMDS4soZxC39oillM8f51V/99yHRV9fU6v637T" +
                "+zfPcUAOdJ146OZVy7Orw3AOLCuz5/h1/qv1ZbOQXEguUE0ty2" +
                "ic/UJLgJVoac9y2MEeMbUXqZO8ZByMpZ8YLx0RrLjNfh73LdmD" +
                "ZcjUZ6LxyVKyZNslbnPNEo+b12kJsBIt7VkOO9gX8+B5kTpgdB" +
                "yMpZ8YLx0RrLh1OcV8S/ZgGTL1mWh8cj45b9vz3Oaa8zzOfqUl" +
                "wEq0tGc57GBfzIPnReqA0XEwln5ivHREsOJ2dnfct2QPliFTn4" +
                "mPj/ye/P1if/OTjdftUbb/3v3eQLtTj60ck+7qonds5fPMfv3a" +
                "zud3Hx/q/dKL6YtUU8syGrefYInE4IROYrQNPCKmRIY6MAMrHR" +
                "VeY7xkbHBhCeUU+tYWsZzi+eus+mv/ZSre8/vL3Se8T7UcB+RA" +
                "z+EeevrS8uzqMJwDy8rsszOD+a/W181C+1D7kKupz73sNzEM9I" +
                "yO9X3LuEUVI4mN2cAn85LMpD3Xrp1tSVvN3s9FFpm/zqd6X07u" +
                "azW3hBj9Fpm313Wp3Hvcom4HzN87q9qJ08zo8zN8P45y0nt9sd" +
                "3D2Ht/sffw4od7n2u6/zacecSWq/Pr87cKs6n7ML3PZeuJQnqt" +
                "QOwq8R55n6vuMPnfprj3ufI28pcV9D5X3ive57Jt/zcJyiZ8n2" +
                "v2trx/o8n/Nsu9z1X4y9/nKkbqfa5c8lf9Plf0KjjQPkA1tSyj" +
                "cfM7WgKsREt7lsMO9oipvUid5CXjYCz9xHjpiGDFrcsp5luyB8" +
                "uQqc/Ex/t77mq97/w/XO87l7ve/fdh69a7rX/srfffe+v99tfP" +
                "eqf5jKz3O5a73v33N6P7oCOtEaqpZRmNe3ezRGJwQicx2gYe+5" +
                "9r/9ZIqWMGmkUYFV5jvGRscGFJ9gc/Y+kBDMKc4vlL5la2uRW8" +
                "60cyV3oPkZbHjIW0v/+u9PAY895cAx3ZxFCM6H0JXvmMsZUsw9" +
                "jSprlGZxFn0FztR4nx80eudHZ1Jt3fK3ben9/Lj9v5+Unx/N4b" +
                "ZGV23lUivzmQ2M+G5hUr82TX2VqjL/l7xbL4ne2B5H2vkNHukk" +
                "+u4u+9W/7/b/NA1djXltj8Z8j7HA9UsbHr/Y+X+5eZTvErSPdn" +
                "Htf9VWNfG5c2Vw15PvdXsRl2/HQxXZQt92ncO8Zalkqt1KG4uj" +
                "0Remf/7Qnfi4ytmSEOj3yeIS/W+xxZIuOHmRV3SxM6L38eQtZ9" +
                "7P8AAv6Dow==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value5 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value5[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value6 = null;

    protected static void value6Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 2698;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNW12IHWcZPqhVCai1F9Y/sJoKghaCmlQM6syZPS3EuNlutj" +
                "Gpeu2FXhqE3mXnqEsWtAiKNyIVTbUYf4JCcqlV6w+2TWMqu1mW" +
                "ojURutuKCDYbNc4773nned/vb+ac3SM7w3zf+/N87/N83575Ob" +
                "O7vV6vV57tme3USbFOf63nbIuXet5WbrqR4Sdr7Afq7JkG94PF" +
                "y73e0q96O7KV30vnFy+G419c71J9+Kmqwvs6Kvl++aiNDJ6lHR" +
                "4f1Xp+3cZ0a5HI1N5eVHPru7nwJvhm1LNhjGXXenxdo/zeUH0d" +
                "g0p9gFH2uDJnvW9pPp/fsJl8tjxn/bA3eGsMY3PT2ITP5R2Pf/" +
                "sq869Ky1a9nt90svOw4Ye8GEZn0mq64DRKlItGl9flT9eP8WN0" +
                "W538IWnZqtfzO072E7Dhh7wYRmeS83moC06jRLlodHld/nT9GD" +
                "9Gd6tTPq7vR/0PefnL3T/xo/vRz5uxv1kcjS5/79V9rG4D96ry" +
                "ier44zj3o/J3+n5U/mLU/7ZuL7pzKn8ZqPBUXaHD/aj8Q3U8WT" +
                "5to9lLfPA28y2JimVxQHbZNNqt13VcaKNaGiNzkFhsPGa3Hf70" +
                "KuS38mGfl/Jby7UQDshOZ65CL5aTjQt+MtYtRuYgsdj4cqVb/X" +
                "Q+vQr5q/gY/QSPSpQs8YADstO6OGhdLxXvwqIxMgeJxcZjduPp" +
                "drPxfP86t9RTy75Eyr9yvK5yArbv5ydQUY+xXBgd16FVhHH96+" +
                "VVXQ2MzNe/Tj1q+JadSXXufNvVaRWIOpfPxmkr9nNLPbXsS4Tz" +
                "bOVzsH0/n0NFPUarQnV/gw6tIozTmi0j8xX7qUcN37Iz8TlcBV" +
                "gLy2fj9bV1tfoU/i1bpZ5a6jnOFsfrtTkC2/fzI6iox5jr+CpG" +
                "e9f4VUFoFWEcsC4j82Wr1KOGb9mZ+ByuAqyF5bNxtf6HpWUrFM" +
                "uPIsq+xudH3Wpu1GbiW3GY9jRCcBjBUdGYH02NbNPh82MtXGbn" +
                "KfGuxXcvVt+uFt9fe3ea58+DO/tdcPFdO1jrbS3590auvpE5Lb" +
                "5zh3Ql1nPp150q7E09z++29Rx+uut6dn2/1OA/aD7Pc9IWczZW" +
                "Pi9WvoAo+xrPWT3SjdpM4myboz2NEBxGcFQ0urwuf1t9N4/1cZ" +
                "m9M+AFbqmnln2JcJ6t/AHYvp8/gIp6jOXC6LgOrSKM05otI/P1" +
                "X6AeNXzLzsTncBVgLSyfjVd3pmdop549ser3Id+d7FzsfyZy13" +
                "xm5873tlqx/PS1ZVeqc/rv2RXqqaWe42xxvP6sHYft+/lxVNRj" +
                "LBdGh3SIgjYcsC4j82VXqEcN37Iz8TlcBVgLy2fj6fvR6bOT3Y" +
                "/6D07/flT+Y8L7+4PTvB8N1gfr3HJfv5teF//0TySiMdiR05jB" +
                "evYc/KrKOaBtNV0XObG0KssKnSFdmhsaJZI9585YV4ACl9nyWd" +
                "UaXzGscUs9tdnorZJYHK/P3ftg+35+X3NurOkx5qxZy9ai51ij" +
                "Q6sI47Rmy8h82Rr1qOFbdiY+h6sAa2H5bLw+Aza4pZ5a9iXCeb" +
                "byY7B9Pz+GinqMOds2MNo7ExsdWkUY19+o34c4Wulgvv4G9ajh" +
                "W3YmPoerAGth+Wy8tm9ySz217EuE82zl98P2/fx+VNRjjKqbGO" +
                "3NodGhVYRxWrNlZL7+TepRw7fsTHwOVwHWwvLZeP1Eeg+31FPL" +
                "vkQ4z1b+ceDZR06yqFm+5EbtaO8ZutGhVYRxWrNWD43Uo4Zvae" +
                "UhjuKe8l8hdS6fjdfn/rVq7lvZNeqppZ7jbHEcWLFDvh7px1Ez" +
                "eM26JgitIowDNswI5drXVkwh6tmsrQg+l6lay3/XK3xoyO0ri0" +
                "Oj+J/YolZi4mtb+yFM10xZnzPDVwxfXhyiPYq7IVWAYgtj2Bo2" +
                "79CHr3b5qS//G1fp8mMtkC//0zDd0un587HJnj+zH+7e50/WNs" +
                "33If3nuaWeWvYlwnnEYId8PdKPo2bwHtDo0CrCOK05xAjl2tdW" +
                "TCHq2aytCD6Xqfk8z0tbzNuYtXy7CPwlRjEfPZPmO7xfmqc9jR" +
                "Dc6HzeE1Me42+r7+axPjbv1ynu5pZ6atmXCOcRgx3y9Ug/Xs37" +
                "NaGoq0OrCOO05hAjlGtfWzGFqGeztiL4XKbKPsgt9dQWByUuGc" +
                "FYO+TrkUXwTXgRfecPHVpFGKc1hxih3GWNzcSfgc3aiuBzmaor" +
                "9Er1uXlttkI9tdmKxNniOLBih3w90o+jZvBOsSIIrSKMAzbMCO" +
                "Xa11ZMIerZrK0IPo/pPO3UsydWfX/f6PS+77wX+XNX5DbeMp6f" +
                "LD99bVn1TXj4uuwq9dRSz3G2OA6s2CFfj/TjqBnTIQracMCGGa" +
                "Fc+9qKKUQ9m7UVwecyJZ8//znh+8/P797fx7G2aTx/Ngyb3FJP" +
                "bX9T4pIRjLVDvh7px1EzrUOrCOO05hAjlGtfWzGFqGeztiL4XK" +
                "bm/rUgbbFgY9by7SLwm8RiIXrnXGj/CQ9vKxbSOFFrFYb1hvnb" +
                "6rv54evt6Fid7ALt1LMnFm3LL+t0/b3gRR7titzGVf/CZPlpa0" +
                "tdP5ffMuH39x9FkLvg+hnVtkN/H5I9Tjv17IlVr+cd3Sp4V5+T" +
                "4bitvl3dk+X7J7viJ1VaZNxSTy37EuG8xIZvgO3m1LXm9nAcbG" +
                "kdWkUYpzVr9W4tnxUj0lpcBbYi+FymBj8rbTGbivl2EfhPn2I2" +
                "qnS2w094lvY0QnAY4StP8bfVd/NYC5fZOwNe5JZ6atmXyPBNHA" +
                "dW7JCvR/pxyUTOxEaHVhHG9V8cvllXcxmllvW15Sp0/j7ZU2Ar" +
                "gs9lqq8UW9m5bKup/DmJLrtXlBoH5OjNrvfslZ3TYya8PraMG2" +
                "xZTLaFIzVetLXVT+f9VdBbfoAP3pr/7zgweLjS/bCLAzK2LT1l" +
                "xzQ/qXH+v6OFhXRpjMxBYrHxS092q5/Ot69C/mVp2arv73ts1r" +
                "fDn08f0yVjUV1wGmWVt/On68f4MTpWJ/n8+fYJnz/P7t7v79nZ" +
                "aX1/p98fDe/k9Vy6FFzP93hny9Od1vPMLl7PM9Ncz2ad1P9lzn" +
                "ysWc83eusZ+k/KzfCb4eluUDle/v+hLbvBh70fZTeWD4RwQEbr" +
                "/cWOaX6Ct4+nKflpOGwxMgeJxcaLtrb66Xz7KuRfkZat+vOZ26" +
                "xvp6uNl7GoNtxy36Ks8nb+dP0YP0aH68zcO3Mvt9xLbOTPSURj" +
                "sHOu3JSxEs8egY+K4DQcXg7KoMqyompIl+aGfolkj7gz1hWgwG" +
                "W2fFa1xjdnefMXKcPmSr780dDP6NQ7Ws/VHyfebXa8yp860QGz" +
                "R9m3VUeH64loO3V3S+1DyezxU58NXrWPzRzjlnuJsb88KxGNwY" +
                "6cxtgxqDi6o61YpM6JAqvCZ0XVkC7NDS0S+dId7ox1BSjw5xSe" +
                "v1YukXw/H6Mr/VWJDj9cfTP9mbp61Lh8/zj3uvHQ3ceRLo2ROU" +
                "gsNl7m4+b1PNv5x1kF9f+GX/Duqpe6VOBxgefPy70d24Yfabn/" +
                "X0xp6/Q8OcHv4wb7Bvu4pV4i4i8fGezr/xQRYNnCeBzWQi/1XU" +
                "6b0xiXB5F6XSK6LCMUCS+Ns3m2uZ5WYJmtKh0z+P8B/60eog==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value6 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value6[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value7 = null;

    protected static void value7Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 2004;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXFmMFEUYHgRCVAgBFV6IsHIfi6AYiMfDzkw/qGhMxAtxQV" +
                "Si7/qgJgS6q4DJdGI2EE1MTAwJKDz45KsmHgTFEwHxVg7xXBU5" +
                "BAOLPV1d/f9/VXVPT28fKz3p6qr/+r6q6aquru6ZSkXf7GMyx6" +
                "qVBJvTX+lgq75byWBLHyWZ56azlcy29U+F2BtSct5QGbJbvtyq" +
                "k6qTRCqOUhaUF1MJ2GJr7C/l4Af+gEmjYB3mhXGgjOOYeFFEYB" +
                "WyW2yOjdkDS52pykS11zd2e5hbmm1/Z3dUKrUHizkP2W1meTL8" +
                "xuVpce1ue67ttau9yC9Nw/1900+JIkyNkC/RJLOyay97chv9dR" +
                "HtHNHf7ema5PoOGd0YMQZsxTkodTCKbE2qSxN9cJ5RfumZkG9r" +
                "DVvOetn9RLYqPD9/Sxl1RYR8tbc/mlF/btNv2crOzk/F6qEMR5" +
                "4nZK65XNNNSBRhoj9SrQuikJpLaVlbMvyWVRZMnd1kvqSPKfs6" +
                "GE/88dN5K4z9nn0gyH2g4b7jp7sMjD729v0G+auRddjjo+8NSm" +
                "8Hx/f9dK9aJ8cwH3U+TTp+Oh95+yfOZ7EjzBo9l9HsbE35sbKu" +
                "U4Lz6skQe1ZlyG72xpTtWXidoL+zZzNuA6f89mTP5H4Htl2krW" +
                "MrFWUskTbiKPOmMvXE8bFGxVZ5YBZmvpibjgixdJyomthb1RqY" +
                "mOp4KpLhmx8Z3iVMr1x0G3uzcEQ7nC89nX30xoxy23PjyZzH9Z" +
                "j7zaab/f1mI6PrQdr7zcbMPO83a6dqp/z5a3CUeVFuvii1Uoq1" +
                "Uuf0yxKW46jSw6/PbCrDOjKjJjiypPLUeUk95QiSjSepF42A8f" +
                "GutoPOmtbcWmYtg7zYvbrPwR7CRqbY2pRXPU3x4zZhIe1M9hBT" +
                "8sKRsb9MW0e2B/Oh7NW64J3iUFvT1rNFpiLn9/eXqVbPR63XqT" +
                "ZJNNQqiR22oszb48fHj8IHb3Ocem+9V6TiKGWi3NwmJdgGPkLn" +
                "9EtfKpfRwVdiYktdB8yAFUWFqCZeGBu4SMmmN9Qa4wjAQK+Tuf" +
                "60Vsaedi680m+pDNGt53g7i3UR16vGvPLuj5rbh2x7nijec/Dz" +
                "pWqi5x3/r/X5qDrR+RJ7Ke36fFx7NhZcfO3ZuDbP+ad1yDokUn" +
                "H0x89Dsty4QUqwDXxAh22oD0QMx2diqetkDrOiqBDVxAtjAxcp" +
                "aSxSa4wjAAO9Tub601rFj5/WrRff/XvedbK7Y9rTHlTkJcbVgd" +
                "cyWmXY1ka/M6I97UTj547O+3uC+/dXsh8/rXHljp9R+NCe7PX0" +
                "42e1q9olUnH0r4BdstzcQSVgi62xv5SDH/gH894zahTQoatwF8" +
                "WBMthSGdWrvsCmhW+KjdlDvXSmKhPV3tBTwidozZ05jF+Xljx+" +
                "5orPDnj7kSDv3YezL9gfsr+zv5r+sz+G3jWD55tsv7cf9HN/Bp" +
                "Kv/DRYY2H/iP7Owmd/7Gv7ANtnXeXlvtd4/MAOs6PsZ3aMHWdf" +
                "sm+Q5m9vP0Nsv2Xfqc83Gbqysl/Yr/B8M5T+7qcnW/hE/nlk/0" +
                "3Y39kJdjr8ttZaa0UqjlImys1dVAK22Br7Szn4gX+AOF6NAjp0" +
                "Fq2lOFAGWyqjetUX1XW8OTZmD/XSmapMqL01YA14xwF59DUDst" +
                "z8UEqwDXxAh22oD0QMcMdQS6yTDCgLHRWimnhhbOASSsaoNcYR" +
                "gIFeJ3P9MXMqUedLzsGm9py+8/cZvG8l8OFXtN5nqJ/KZqSKfp" +
                "8hQN9rlifHT9rf+Vg+LuF6yMGM57qt9jxfcnueL/oKiNrzcBbn" +
                "Z/g9ivPzdCdcmkcyb8/T+Z6fYj7fPGqcz/+Y/Xy+/m+58/ko/K" +
                "zf/+zpk6nI+e15gWr1vH+mmJ539EWuP/YlWqXsS2KHrSjz9vjx" +
                "8aPwwdschy/0dvQ98Cl8ljw/+Rx3RJL+LtuTX+On84PSAnF+8q" +
                "vD2FNb/d0aYewzM709OKN4V9seNkPt73x2u/7Op/npPBWfTx50" +
                "f5/Lu+P01S16rrMtzs8qe32pcER4v44vThnhcEx9hpXcnsPKa8" +
                "9qX8rzM8bPGltyexaOD+sh7shEM0HtesRviqnP6JLbs3D86gt6" +
                "Lm2Eobfxmwtvz+f0XNoIhvnfQLntWTx+9Xk9lzZCjqPSfSnPz1" +
                "tyHU0WWgtF2jpKiSy7o0ArZTgH/rDTHBxl/Po5iol1lJeKAxLV" +
                "hlpgRGAk2bTwqR5HwAwosooIMmxfr9VrHkZNHv0a12TZHS0l2A" +
                "Y+Quf0S18qF2WIGOCOpJZYJxlQFjoqRDXxwtjARUowvl4zYKDX" +
                "yVx/zJxKwm8mfCPaHZPu+h7bIy4r+fpeOL4Vrnq6OczVrAklt2" +
                "fh+M7csD1zwK6fLbc9i8Lv2SxTkfOxe6hWz0euL22Ox2nPJokd" +
                "tqLM2+PHx4/CB+9kcVobrH+6OfwWw5qY0bz8zjb6pfniR6Dehd" +
                "8P4ffQ9WRX+y0EN7w13m492Q3fqvPfD7kyo1WGtO+HRODT9WR+" +
                "d3b/J2CHT1hcMvflj/hn7xSlhRP8fwR/AI1fF/jDyTmuXxVEMP" +
                "xqni9PgPw4Ka1u4fu54Fex/F6k7eWrFO/HAhZBX+D+PQRfwVd2" +
                "dD0KV0Hc7hzmS8M7sXbnZz7eDK8UvMF6nZvD/y9Zl5Q8XyocH4" +
                "2f9Rzac1TJ7Zkv/n+r7dJp");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value7 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value7[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value8 = null;

    protected static void value8Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 2386;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXF+IHVcZv4pKQNoq0VhQCCUuaqPBjU22DSW7mztjGxVbwZ" +
                "BttYLWlfogYhrxSc3cuxP31mstCCK+rVbQUluNin0RRFAKpYja" +
                "EhcsKq55cn0SCpoHz8w33/x+3zln7t6ud3Zs5jDnz+/7zvf7fW" +
                "dn5sy95KbXC4/s89obp75t8IfQf7jdix6DW+043z94vtdL/tOb" +
                "yTH8/mT74HdxfHr+wXum88tvyF8/0b5er+cdvZkf6f5ep8fe8w" +
                "/O1+v5Pncd7MjfdH02XB//6nY92+VPbktuk1paxWQ8/oAi7IMC" +
                "G/vYOYhYXR/XW0+2qQKrImRF1Jgu5oYWRZg/zAwKwpzi+bNyi4" +
                "TXZ/7QNNfny+sYXrfnjAfq+/2uFp5fr+n4+dkqf3ImOSO1tIrJ" +
                "ePxhRdgHBTb2sXMQsWJ80XqyTRVYFSErosZ0MTe01MiLfsYcAQ" +
                "rCnOL5s3KHrSarrl3VtrSs6nh8jyLsgwIb+9g5iFhdH6+1nmxT" +
                "BVZFyIqoMV3MDS2KMH+YGRSEOcXzZ+UWibx/frSF++11Hd/vrf" +
                "Inp5PTUkurmIzHDyrCPihiG27rXItrdMwt89lnPdmmCqyKkBVR" +
                "Y7qYG1oUYf4wMygIc4rnz8qLc/CuweHBW1x7S7m3v5Wvz1Gw/+" +
                "ePRt4HDk33+cghb5/hW/LBHexH4/javxv853b7+aj2P+FW9Fxy" +
                "Tmppy5U+p+PxQBH2QYGNfewcF2UI1P2NbreebFMFVkXICp0xXc" +
                "wNLYqsf8vPmCNAQZhTPH9WbpHw+Zl+o3fNHe3mlOxL9kktrWIy" +
                "Hp1UhH1QYGMfOwcRwcmeoQ3KoMqyImpMF3NDiyJ5z8+YI0BBmF" +
                "M8f5tVdI2vC3sz/Av+o+PP7y3zT9yPFqeK8PLajy5Osx/lT+5u" +
                "P5r8/Mx/1ML18c+Or8895x9cqdfzJy3ks93xem53uJ4/m3300e" +
                "lu13P9fIfr+VQL18fXOr4+W+VPz6ZnpS5aRaT0T/RPwKqY9goc" +
                "83EWtcwTX8SU+IoiCmxWl88DxPexHsyoI+TKOXFstSO6zd5nBK" +
                "b+pX0lXXHtirQlvqKlf7J/ElbFtFfgYtOenEUt88QXMav1PGk5" +
                "2UbZrIQ8QHwf68GMOlJM+K2dI7ACm73PCEz9yzmb/U3Hsaltmf" +
                "FmVRZdqZAaXaytm2wzPs6r7C/CR3uuv8ie1qYKWIUX2dPJ2sMZ" +
                "8CT/RT9jjgAFot9X6ivBUdm2+lv+tSGYqw+7siWInv3DOtba2m" +
                "V25XU4jF7EBEvtvxV7FimutZa4FxT4OKOiyqJNClSpHyGOWfbY" +
                "fjS8nP+ihf3g6x3vR63y96/0r0gtrWJFGV7up4oounZArTJDbe" +
                "zjvN5U9lP4aM/1U/a0NlXAKmxkXydrD2fAk/xTP2OOAAWi31fq" +
                "K+GVlLP58+bw8nht9p83k4e6/bzZxD+b7z9jR/7LasXn+o5j7X" +
                "PmrzA31VU/1+QrMWfyueDOXd6RDfyD70zrudv3eff8/FULz68v" +
                "d/z8bJU/PZ4el7poFZHi7vdvwqo+3JPRcBsjiYAeWo2fZJaTbV" +
                "aXzwPE97EezAhFqqbgt3aOwAoss88IjP3ThXTBtQvSlviCll5v" +
                "/G1YFeOejNx61iOJgB5ajZ98yXKyjbJZCHmA+D7WgxmhSNUU/N" +
                "bOEViBZfYZgVn/4lg6WBSNVPSK012f37UY12yxte358ZOHlw7u" +
                "fN+ov/rG5iCq6oIy3wIseTgWnzG7CpytcGiZoOymomAkp1vPRy" +
                "3GNVtsbXt+/GRgbQ3reZONGJuDqKoLynwLsGQQi8+YXQXOVji0" +
                "NCtr3I9+08J+MO54P2qVP/0jSjFixD0/f2wRHsHbPT8JUVyjg6" +
                "XKZ8hxrC2my1cF37guy6gWqCn4Qzv8NL7PbPmYif3TyyhuXJ5a" +
                "3P3+mEV4BG+3noQoLj200nP5jDiOtdF6NqqCb1yXZVQL1BT8oR" +
                "1+Gt9ntnzMZP0n3u/PtHC/faXj+33P+en7+WdbyOeLHa9nq/xL" +
                "x5aOSV20ikhx9/vjsKoP92Q03MZIIqCHVuMnX7WcbLO6fB4gvo" +
                "/1YEYoUjUFv7VzBFZgmX1GYOy/dHzJvdcX9VL1ni99GY9/CKti" +
                "3JORW896JBHQQ6vxk9xyso2yOR7yAPF9rAczQpGqKfitnSOwAs" +
                "vsMwJj//5Gf8N98t9Aq6P+hrs+nyzGBWJ9tMhouC092HiORV0+" +
                "Fy3GNpmlPXiErOoR02W5EVUQ5g8zg4Iwp1j+9O3JBqs3lifq7+" +
                "suxS2TkDhGz6/1vXlONqlom3/i959T/XuGl/j95yMdf//5yF5/" +
                "/0nvS79vYX9d63V67D0/redzLeRzYTZx8ue75Z/+oH9f98HeNX" +
                "c0/fu62T0/m9fz1If+p8i3trou39vB/lgcb8rJPj/XfvDSn5/l" +
                "Lnipf0lqbXVU9EZ39y+Jl/WBh9bsY+dY1EbjuLBBGUe3rHHt/g" +
                "x4wj9fsrPC7Fmlr9RXwitZnMmR5Ej1XDlSP2FKrDhPnRVUx9aX" +
                "Z7Ddj9TwHDuiPOzF/oprrSWMY1VoLx5Vc+JsJukMI8Rz4/Wxx/" +
                "DV9fPzTHQn2NqL51z+tyl8/kT9P7vz71PMqX7Tn/9lB78XJlr/" +
                "2rDuc8mc9ixWnOOfCqpj66uj4ba1+5HsMboXNpnDXuyvuNZaJm" +
                "XAeuNRR/f4GceihuvUvHLMnp3PXhXuR9kNrt7v1vPn2RtK5IA7" +
                "3+zOL1QeZaTsbbqe2c1u9E53lntb9u460rEs2JNG1W8Ys/dmd2" +
                "R3uvZ09v6G94z7so+V7cfL+pPZA9mns1dkr/S8rs+qXxBmb3Tn" +
                "jWSp3/izdxD/R7LqTT+rfq2W3Z4tZqd2eOe5O4KZX2Nm97tzNZ" +
                "lP5t26zmtbrvS8jpefVoR9UGBjn2R+dD/G7t74LLxttGQ+/0xo" +
                "0x6rsqzFOfqErz2cgYw0ouYUxrYzwpxgt/l7WR1N3F+rqKUtLU" +
                "d1vPyCIuyDIjZ3vx9lHzsHEWtm4xnatMeqLCuixnQxN7QoIjmF" +
                "se0MtfhKw/xtVpP3o/FT0+xH4f8fMvrU/+9+tP7bNvej9Gp6tV" +
                "rfQxYTHDUwawvtjKMfY5Y56dUL9dMpORRq01pLUwZQUES5cNDq" +
                "02P5os0CSvhYvuizBPvRIYsp+6Tfcy3/evbfL83sc90uv19qyq" +
                "m975ey+i4fPXDtfX7PH2w1/H8BrU2HQw==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value8 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value8[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value9 = null;

    protected static void value9Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1763;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHEUUHlDx52hYom6IGndBkBDIuig5TU/PIPiDt6AnxZ" +
                "ibCpLsxd2g07s7m/YQEXIMOahJjLjJzYAgmFMiG0QRr0GUeFsn" +
                "gjER8adra95871XVzPT0VHdPppvpqnr1ve99r7aqf5NwI9yoVM" +
                "KNsFtSS9VUnY4cYyLwM+1Up1qlEtwpkbyP4sHHZIZFquUIHltm" +
                "oUoe384MCuyc3Plz5dJCW/Qr1VZ/q3jf4rcqpW7xm/nyR7tNS3" +
                "PBE/PTLmv1Hz/sK6cG9H/mtveKH80K77OJZW54VY1L2FWLW3R/" +
                "7ZqJMdFk0xayEzui6Iiaj/ugz6XLVCV5bF0yIjRSqfzsfsqTlC" +
                "MLVwQZSeL7rvff0/xNljcrt9EWH8mTvXEZu2pxS/K3DKSFt4Am" +
                "m7aQndgRhcfkPrzP1mWqkjy2LhkRGqnkOXFu6CJ2GVnG45E4Pm" +
                "yH7eS61KZy60rVpjYsHIMdfRwjfcDYmR/vSiTvo3hShR0VrC5d" +
                "PLbMQpVH182MOQMU2Dm58+fKpcWx3m/ksN5Wy13va7fyZK9uVj" +
                "f1UZdk0+3aM9ICLEdzf7LDD/6IKVl4H9fF46DNeVy6ZESoolLl" +
                "5OLm6qHSVmoqMfGO68uxygRvq3+musZ+6DHiX9212ZzA8fwgFe" +
                "pvjxH/vS3G5b+MfqmeJ1pZz5/Xq9f1UZdk023TAixHc3+yww/+" +
                "nTn/vsmCPqmLx0EbWGmT/aYv1KzdcnNz9cjLVmoqkfhqu5pc49" +
                "VRl1s9bWoHh6QFWI5e3uR92g4/+HfHwWDhfcDIOGhzHpcuGRGq" +
                "qFQ5ubi5eqi0lZpKTLzjXHxXd9bf4f/5qH5/ueeJouI37+m+C9" +
                "hLteCwE7lrlDitB1LqeTkF5j5WT8apuX2wD+XUfGoA97N9e19q" +
                "vuFknwvm9FGXZNPt+Ji0AMvR3J/s8IM/YkoW3sd18Thocx6XLh" +
                "kRqqhcPePm5uqh0lZqKjHx/Z6PWjvG+L3GqWx+wav+UPYW3gxv" +
                "6qMuyabbsHAMdvRxjPQBI2JypN0HZVAlo4LVpYvHllmoMnjdzJ" +
                "gzEJZQplI7f5lV913NQmNBHXWdasY7nw5G9nNko8ebaOXj9ujz" +
                "5muBY3vrgWKpjPvTUZXBAe4r1aMVHCAuPi6Iw7EDrxgPT/LzZv" +
                "CaP1TK7xQ4f+6avPGMT6dCnfEXsTXTrc1O4Px8xR+qz9lqf2M/" +
                "6vrnxtCRo11103Mwv+nDGXvrAaNUxv3pqMrgINcj1aMVHCQuk1" +
                "P7cuzA9d69iw9mvMz3e/v03V34/JwpOiK+Fwd7RnnetL8XRz/6" +
                "U7n86YBZ8X2P8dyTel7N+R5PR4wfhriuOcczmC53PONzRY9ndD" +
                "jX9VbyM9fR9RLX+06/zNF2j1zPZ/x77ix8PLtvsOMv/LO39pU7" +
                "P1tT5Y3nJG7xV3my16ZqU/qoS7LptmkBlqO5P9nhB38dsX7VZE" +
                "Gf1MXjoA2stMl+0xdqVHwXN1ePvGylphITP/r8HPL9/E/lzs98" +
                "41cPVQ/poy7JptumBViO5v5khx/8O+vta5MFfVIXj4M2sNIm+0" +
                "1fqFn7w83N1SMvW6mpxMRvPUfNq7373DWvf+LZbl6jcOQ98ihr" +
                "Jn/9uMntfNqcl4wuH7CSLiiT/txWP+7i5zY5CjxbHYP23sqMK+" +
                "Bzea73YL3c9V58/NYLueZztuTxLDy+r3/v7bgHX07yOe/pefNq" +
                "xvE8n+fY1U/WT+qjLsmm27BwDHb0cYz0AWPnenRZInkfxZMq7K" +
                "hgdenisWUWqowvmRlzBiiwc3Lnz5VLS0HP7+dKXu85x492R09E" +
                "O5Lyya3WzPDrPXos9fulx5N8Pveke8BXrmhvj/HsET+aHfX9Ur" +
                "Rv0Pmz8Y6PzMEyDu9D/OSU7XrUWPIynktjNZ5LeY9frc3LWpvG" +
                "U7dXHqy1bazL37TY9mi51pbW+Bvby8S42Vce6q9BWl01WY8+6p" +
                "9X/8z7KWbzc9HL/Fwcq/m5WOJ69/J/n8AyFuN5pOjxXIlyveM9" +
                "XSl1Kz5+3+9xL440p5L5Wf+4mPn5Xo/7KV/xx+J5U43nJ+Wud1" +
                "/xs4xn/YqVxcjfi31tWb8X2zn1ZPD8/T3cFm5L7mi+8zs/FWeZ" +
                "87NXfPt+yVvWnef3cDqcTq5Ob/tlDz39e4b4it/4+Y9nqrcLhT" +
                "+9jRo3b8XVL9WuSt2i2jAMae1Z2N3b2qODeHr1x78Mo3b8r++1" +
                "b8s9f/qKn994pv9+pN7Pe3rncDHr+/naxVRn558z3o2dqJ/QR1" +
                "2STbdVvXZB24HBrhH8Z9o5YyefCxLJ+0iBVGFHVT/F49bFY0ML" +
                "WXh8zg0+KLGV2vlz5dJSwHpP5md8zdP9Z8b5ufZIrov7f+1uZx" +
                "M=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value9 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value9[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value10 = null;

    protected static void value10Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1676;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXL1vHEUUX9EgKFFEGmRHJChIGIoEIUTD+Tbb4f+DgigChE" +
                "AiDWf7nDMi7h1RECFIRJlgiiSOEyDCcUHp0koTqgiEEEhIIPz2" +
                "+d2bz9s3b/d2HbEjzfvc3/vN3M3O3vrOWeYfn3yQTekYHM2y/q" +
                "cNYb2lO6+p+vJjevMJx+iXrNNj9HC6+IOXBy8NntuXr5bWifT5" +
                "HByP+F/3PC9m2fBcQ7xnK+KnIvP5WyT/BdNa+nzfczqR0Rso86" +
                "exBwk92uQZvod+ziU9ZJtn+n6KhA/mYbII55mcQxUpbtumFmPI" +
                "eHbURuR6bqW213vxTbfrfdr1J6330T/Nr/fGeCvX+4Ur017v/b" +
                "+xBwk92uTBOPtYD9nmmb6fMYM775iHySKcZ3IOVWTmpm1qLsPB" +
                "ZZenzcBG5HpuJf8YfpT2uiw+Sskenm9nXQ8/jOxHv067cv4a9i" +
                "ChR5s8GGcf6yHbPNP3M+ZkHiaLcJ7JOVSRmZu2qcUYMp4dtRG5" +
                "nlup9xM0kGiRJj9i+b5fg55atSpefCbJhyzdPLR9/9n1flSMJP" +
                "sRZOn2o94uNJBokZbwuuxK/anImqpV8dGfkvzRX7p5KPaKPexR" +
                "lq/NHtnsMXO4cczMsc9hxPFrb2X6MdJMVnZVRg3xMmvboyg9F9" +
                "0Rmwjj3Iv+mMLjt0f1P1zva6L1vqa//6x7v3Q4j9j9UiF6vlSo" +
                "n0JNen/2Tx/e9+fi77r3Z2xM9vuz9Gg/H32BPUjo0SYPxtnHes" +
                "g2z/T9jBkc6ZiHySKcZ3IOVWTmpm1qMYaMZ0dtRK7nVuptQAOJ" +
                "FmkJ+9pGSmYqurZqLL76RN1RVNT9DhpItEhLQfA8V+KZqejyqr" +
                "L4JG7Tfr5Ub38PZO5fP1c73t9X/pBePxvbGWvv78VJXayV/f1k" +
                "3RFUrIsfoIFEi7QUBM9zNeJXoMuryuLITTQKFdPe99BAokVaCo" +
                "LUr0FPrVoV77+bwlbB6xY0kGiRVtYeyBA8zoOw30avOZ+3dPHY" +
                "mMJsFbzuQQOJFmll7QUZgtRvo9ecz3u6eGxMYbYKXlvQQKJFWn" +
                "mvdlaGIPXb6DXnc0sXX30nha2C1yY0kGiRVr6Wz8gQpH4bveZ8" +
                "burisTGF2R6O+89iNv75vZjt+PnSrOj50mz65/cpzufMhPmc6X" +
                "g+Z0TzOaOdz95daCDRIq1cG0dkCFK/jV5zvd/VxWNjCrNV8LoB" +
                "DSRapMEx/7N3NT8fQogho1w+Z3rs7OWz6vm8oYv7Y4rlV1WI4N" +
                "yGBhIt0sra2zIEqd9Gr/n+vK2Lx8YUZqvgdQcaSLRIK2vvyhCk" +
                "fhu95nze0cVjYwqzVfC6CQ0kWqSVq3tZhiD12+g15/OmLr78fg" +
                "pbBa8foYFEi7QUBKlfg55atZLVv5J8yNIx7W1DA4kWaeVemMsQ" +
                "pH4bveZ8buvisTGF2aYzmsr959yE+8+5ju8/50T3n3Pav8e1/X" +
                "z+zEa38xmr39Tz+d41aCDRIq3cC9+WIchrpWTXqxqLx8bUFC//" +
                "mO73Gc5cz1o5Yn/vaKt+a/N5reP5bKl+vkB9vjDJ5+v5QgwtXq" +
                "eKCzQJW8pCzWU+qX4VvhvnuXArO9eNHWgg0SKtvJ+/KrqS7Uj9" +
                "NnrN6+eOLn5hLoVtOqPefWgg0SKtvHa/4j0PCVx9OD/sHz5leu" +
                "zs4ZPq+byvi/tjiuVXVQjnt/19xdXnu71fWjkmvV9S/z5uHnuQ" +
                "0KNNHoyTb+lZ1t0YH0tHw36uFo8gA2YRzjM5m+xdLL8qnzGZi8" +
                "vARuR6bqXWv29z4nF5fyqvQ9ehgUSLtPJac1yGIK+Vkl2vaiwe" +
                "G1NjvL6FBhIt0lIQUjJT0bVVY/H5I5L8+SN6ZvlBnx/0+dgP2n" +
                "AtH+dkmamH7MzCyiKRKh55ZV7uoLkVc6MPazGGmcEixM6t51by" +
                "j9TrZ9rvDbs+hh9PF3/SflSIPnk+Xv9PIDamZn5f3F/pr2CPEv" +
                "xolfal/srqMfZwrpltnn/QX2IkRsSqGHPxXG52RdOm3MWHts+O" +
                "u+cyG6gfwh5cNkdFEZ+py8TNH1+BH1CPmnMVfxDWY+s9hFEVsb" +
                "MkeWaWzby6/mT8WH0+O4bT9uej/lYTq72/pV3vsfrNfD4q1ot1" +
                "7FGWV5h1stlj5nDD2OIjOtc9k3XS9sezaWeaMapns/CrMmqIl1" +
                "nbHgVIs74/MsqlLJepP36Tue1pZ3/vN/L9Tz2K7MzVU8q7h6/T" +
                "1vvwq9r7+5fd7u/zb0rWO2Spnof8B6g4MDA=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value10 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value10[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value11 = null;

    protected static void value11Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 2195;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2IHFUQ7pNRVIIGDWQFxTWgkWTBTTTxoNmeacgheDCH7K" +
                "66kcQ1uWhQzCm74M7MzspEMIeAePASECPExRz8Q/DnGvQgkoss" +
                "0cMqBFmIGlDRoP2mpuareu/1bE9P97bsDPN+qr6q76s3r7u3O6" +
                "PRUrQUBNFS1OmDgGc8lhZgJaK+YvtoxC3ym1d40UWyj/lsRps1" +
                "WgovujZ3jFjOSfy+3NAMJT6lbv0a777mrgeJr+b7wYCv1sNBqa" +
                "+Fe4rNX9tee7B2V9zvbM/ui9fzRJ8ZhhPsux3L/fF3+FM+upsf" +
                "rKLqIb89ib+21bGM9rkOjwZB9UL1ArXUGzvNeMwtMHjDJzFsr/" +
                "+JMY+CoHJYI6WP+bQKlxVZfboQIfWzRfK7lTG2tTvW/4er1K1f" +
                "KteW7vHeY39W6mm+KUK52NrmtBlS7Ib92eLSV5BFaXVfdR+11L" +
                "ON5rCwtXKKvRKBD6PMmLDI2NZ5ylglB3zg0yok3tYptbsRUj9b" +
                "JL/MrSMMyqfUViJXUqpXZ6ZPgnX8an6UCvVptuzVg9WD1FLPNp" +
                "rDwtbKPHslAh9GmTFhkbFzJM1LpPYxn1Yh8bZOqd2NkPrZIvll" +
                "bh1hUD6lthK5km3fRHUi7ie4b3smeA4LWxt3slci8OmgNptxZQ" +
                "4YHsVK5yRS+5hPq5B4W6fU7kZI/WyR/DK3jjAon1JbiVhP8o1X" +
                "x+N+nPu2Z5znsEgM3uSrr3CsHYkxj2KlUxopfcynVbisyOrTJb" +
                "l1FaaX/G5ljG097tbkr18q15bulXOyv3NGfaWv6+tEuefPYvnD" +
                "kXCEWurZRvP2eBEWM4OXETK+0y62+0WRhUeLHd+IjCGfrUvyCA" +
                "UdLJTIPDYjVHXVLSbkXlRVLXar8DBoJlaM8WD7s8/7zcp6vt+s" +
                "RtWIWurZRnNYJAZv+CRGxyBj53g7pJHSx3xahcuKrD5dkltXYX" +
                "rJ71YGBW5N/vqlcm1Jc39U+66POxjr/n1+U+1SvD/357MT6udW" +
                "Yf/Wb3/tldT6U96/z2+cv637dGA0GqU2GoUF82g0PA+LnBkf4v" +
                "HhmPhsch5ZeBSeZyuygFs8tRh1eaQuzi5RWjdi0bIq7ZeVdc/f" +
                "57kKN6vNxIoxtr6XI4WeP58o9/xZf7HI7NGZ6Ay11LON5rYFWI" +
                "mW8WxHHOI755nf7SzwaV2SB3NgtU377VioMfy+3FI96nKV2kps" +
                "/Frvz+ov5e7PYvlrN3tsGzJluqXT37BKPVdyruDGPtfzSpHrGQ" +
                "6FQ9RSzzbzroxURsKhxkuwGBuPgJbx1BpU/JfJCM3hNzbyySzs" +
                "s3VJHswZ2zqubdpvx6JWVKBz187KqlCFj0EzMd58eu3PynBlON" +
                "b9srpbG06zPwnlw1LOXK4rP2fbn0n8tbNpkT13/2R1klrq2UZz" +
                "WCQGb/gkRscgY4fxpEZKH/NpFS4rsvp0SW5dRdty0q5YZoACty" +
                "Z//VJ5dTKcDWfjfTrLfXvnzvLctgAr0fUV6SM74hDfOS6es7PA" +
                "J473Wc2DObDapv12LNQYfl9uqZ4r9ym1lWh8OBPOxP0M923PDM" +
                "9tC7ASLePZjjjEd3iP2FngE+s5o3kwB1bbtN+OhRrD78st1aMu" +
                "V6mtROMrlyuXnfNG22Y+4SR5ec5YWDHHR8a42cNJsDDGRUEH2P" +
                "gtmSWH9sisEs81yWqSFEiW1Wy2LnFm7t7hhgU8KwxLfv6ZxN86" +
                "kUv2beE2aqlnG83HLmpLuK01By/7ZDy1rTrigmD+OPuZExnnX9" +
                "A+qUvyYC7zaJT027FQY2ry5W69Kqtij1wNySeZNL7X/izk/v31" +
                "9fz8s9/f2zS/92To6/c24Q856b57FX/C722S+PXvbZqfZ/u9Te" +
                "P5hvM0vvHswE9ZtvrtjcPJvn5fjadW+Rf0pf60ZUF5VMXr2Zhq" +
                "jOe7no2nk9ez9cbarGfjUMLx/kCq7M9k3Enb6a1tA+/P7cn2wb" +
                "OnU9lLw+DZE+N20FvbBq51R7J98OzpVPbSMHj2Xsd7/ufPRDZz" +
                "vJ9em+M98fr+SJFX92LOn9FQj+vRULnnz3T8eal0//5svpkhw0" +
                "yPO4hLOf0d+WPGO5hLaZ/XZcq+J9xDLfVso3l7vAwLsBIt49mO" +
                "TMjYYVy2s8CndUkezGUeny7NCFVddcsJuZdlVajCx6CZNL6I+6" +
                "Ne+7N6tNz7o7Xn7/e/R0j/e7BaPa7nWE5npYWM63msyLVbCJpX" +
                "zf1mc6W2s3md7jebv+WTu/mvY7kW1/NkTtl/XcX/d8J6rsrf/K" +
                "vT/1PO/uzzechbJT8PufZ/eh7izdDX85DqxnKfhyTx6+chjbez" +
                "PQ/xXvOPuqN8Xnnmy5orXG/Xo00lX482Fbl24XQ4TS31bKP52L" +
                "3aEk63PsaYfTJejrht3gS0jginmxtcn2bUeJ9OzW3rYBXcm5p8" +
                "uVsfytzssWvSb624q+s9ak1vWppLC2Oo57FvriOB0R4dr8ekwM" +
                "bZMVKby4hcLk+vSrROn1KXz2Yq4u/5hVuLPyu1vsgWN7Y3P5Tn" +
                "u54L56ilnm00ty3ASrSMZzviEA9OnUX6pC7Jg7nM49OlGaGK+7" +
                "HQn1uqJ5Rfqa2E9SDWeTJTC9bxa+yx/FApj9bb1/N6tr5Mhfoq" +
                "4/G+K9xFLfVso7ltAVaiZTzbEYd4cOos0id1SR7MZR6fLs0IVd" +
                "yPVfy5pXpC+ZXaSlgPYp3r0eki90f0Wbn7s3j+ve/ELAeiA6Y3" +
                "s+iAmZPdtNQzxrTkZyv7YeVI2CmKssn46KrWQQhSJDOCDzjOSR" +
                "qYQ0YQF2skP4+k386HnJQHcRyLXFJTGffvra/LvX9P+n1IPv//" +
                "EN96LtxR5HpG75a7nkn8+axnOBVOUUt9+8w6Re/KlsqWcKr1DS" +
                "zASrSMZzsyISOxmpw6C3ziejSleTAHVtu0346FGsPvyy3Voy5X" +
                "qa3Exq/59ehcydejYvn/AxVY6ok=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value11 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value11[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value12 = null;

    protected static void value12Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1353;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW71vE0kUX0BCCOmqK65BAhJASKcrDiiAKozXDR81Ebq72O" +
                "IfSAEoiAZswInzLyBFOgECkYgougaupDgogu6OpIACOiSQ0kEa" +
                "GnYzfrz35sPsrmdn1h7LOzPv6/d7L2/Xu8bEU/FUFMVTcW+OIt" +
                "jBGo5ogy+pa2+Ar+qJa1hFUf0ht6Q6wOMsdFSMauJFsXkW6Uzx" +
                "9cyQgZ6TOX/KPJE142YyN2He0jRhjxJqgy/UURvugxElbvdfbk" +
                "l1gMdZ6KgY1cSLYvMs0rmzT82YRkAGek7m/CnzRNaIG8ncgHlL" +
                "04A9SqgNvlBHbbgPRuzVc51bUh3gcRY6KkY18aLYPIt0TuqpZE" +
                "wjIAM9J3P+lHncqL2svYyi9Agz7NJVuhZP0iO34Rb0rcphDaso" +
                "Ek+4JdVJL1ihhYqavtM4Zl4UG7mAhOLT2BgPmehM9fwp80S2Wl" +
                "tN5lWcYZeu0jUcqY1qgW9VDmtYJfm84pZUB3joo0ZGCWdLLSg2" +
                "zyKdKb6eGTLQczLnT5lzCYwbl+mu+y5yOsR/UdBhw+++LQOt9l" +
                "S+e9hdXZstRvhhY4E5lcOz9Uvr59aeZD62tTug9meGCOMW+XFN" +
                "cji5X7nniPfe7+iPmOU2/NZBTXI0J6OTZnneerY38ljXF8P2bd" +
                "n43vvzceD+fPy9/rz51yD96bue4pmTT5VnRetpw+fne2d8WOpZ" +
                "Xwvcn2vlXj9917N2IWw9bfhKf/5aRj3j/wepp8HysMPrfsF62n" +
                "LS+9MZ00vf/koTyaf3j9FQjuuWerd/8M0E6xlnepbR75daS9Wt" +
                "c1zq85kYE2PyKGeQyX28xiVoS63bG1Qn5eiH/ojJo1Ad5UVxcE" +
                "/jmHhxRGQFc5qTKTZljyx1pioT1b5vf64X7PAq9+d6qdEvxhfl" +
                "Uc4gk/vuB5BQG3yhjtpwH4yImNRS1yEzZMVRMaqJF8VGLiCZfa" +
                "BmTCMgAz0nc/48q/7Pm6dORCM3ys4p3/3nrbuD3n+Gvl+a28xy" +
                "v9T9WOz+M56Op+VRzludOw17lFAbfKGO2nAfjNi7bu/mllQHeJ" +
                "yFjopRTbwoNs8inSm+nhky0HMy50+Zc4mf75fEzrDnu3/8vPXM" +
                "mc+OwPXcUaXr5+03A3+/FPj6acNXnt8nw32fnLM/dgXuz1LxxW" +
                "lxWh7lDDK5VyVoS62pP8jRD/17iNvVKKjjvCgO7tGWy7he9SW5" +
                "bjfHpuwxL52pykS1L6M/6/uL6XyMbPhFWXb+MMh+H4Rv57f++u" +
                "7nsPWc+6fUv1atXpNHOYNM7lUJ2lJr6i+P4gr6oT9i8ihUR3lR" +
                "HNzTOCZeHBFZwZxyM8VG9pSlzlRlotr3+z6k8FX5WnWfN/1wm3" +
                "3lsp4T96tbTz/cZted1vN1hevpnZuD8/1qhc/3q8NXz4nFCvfn" +
                "4hD250yF+3NmCPvzUYX70zs3fD4Sdwr2gMWv9VPxmFqss265+e" +
                "hPsZA8z2wW4LxQTJdntN8XrOdC2PPd+e+T/w58/bTgt/4c0noG" +
                "/vWyDd9HPUW7IOd2VNnhn9vgn+/9xvy2sPXs7Kt6PXP+e9xzJ1" +
                "323L9nRev5wkk9X/j3rGY9a82w57t//JLreT5wPc+PWD0nA9dz" +
                "Mmw9xYp9Z71GrdhsxUq2CFkx3PnpebpiqtRz2b6zcl622YrlbB" +
                "GyYrjz0/N0xVSp55J9Z+W8ZLMVS8LRb22LxrH58eej1MoV05Lv" +
                "58cC389/Gq3nIzfj1Cf/noPXc/5ANHJjfrzk6vX5fd38oUwRhu" +
                "v3yccs9qX9f23sz/qX0etP/zmRep4bwXqeC1jPMyNYz3Jz+go5" +
                "MOAB");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value12 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value12[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value13 = null;

    protected static void value13Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1375;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW0GLFEcUrpCDJPddCQRiyAQSZUCihxAI2NXTgywBBe9iEg" +
                "8imESIl+QgmV3X3e2w7EVIDoHFkyieQm4J0Vw8RFCReBRkIXjZ" +
                "X2DCZnpf3rz3qqpnepvqrpmyhqnqevW9975686q7q2dGKbsMvs" +
                "aj7IXyUPL31RQVP3PaS/nu0igSh6xYP65uZ/ChJfnLH8vFGxO8" +
                "Pyr5dA9W5n/ED1OWnydVdKX9ObF4nogwnicCxnMhwnguhItnEy" +
                "X/IGw8rx4IFc+0m3aH8xdn5UIyuQDKhQWbXq5Hf9fTq+7fF9NR" +
                "PDtpx/LRqcSkU4Z12Wy3VPfvi+nVFRVxSe74QznOzpezy1BDiz" +
                "LomxLCcjTXRznpkT75lFb4GOfF/VCf23Hxkh6JFbbJz27bnD2g" +
                "3ExNJsiHdK31/mvU+fmLP5QjP+9n96GGFmXQTx6ihGNMtHxjTd" +
                "ZJF6VcJsckM/KDPWnHxQvHTY4ogTnZtoktseQIGQebNXmz9pv/" +
                "lsd/+Xale6KPx5yd18Lm58qDcPefyZ8RrvfW5zT4Y+T7aYTxbH" +
                "RO2Ua2ATW0KIO+KSEsR3N9lJMe6ZNPaYWPcV7cD/W5HRcv6ZFY" +
                "YZu86rbN2QPKzdRkgnxItzw/o7y+v+IPZRd9Rp+BGlqUQd+UEJ" +
                "ajuT7KSY/0weOxf0wrNCZ5cT/UJ6yUyXFTl9gU/l22OXual83U" +
                "ZGLiHfm50ejzkDRsfq5eaPT8uZPtQA0tyqBfHOvnICcMvQDB36" +
                "acW/z/c3wukXwMGUgWttfiXdhx8+K+iQtKuH9um+wRE5upPX/O" +
                "PNvpq/7wuK+wVaNecdw7hhKO6TNE0V/c7guM1FFMVxnW+s4xZb" +
                "CwvZJVFy/uu8+sgiRfMGfMLRCDvpOpPX9l4B3386Pvj/Rxz3di" +
                "U/D9ke85TS4UT+93tlMQz+R0pev76SbimXQjjGc3XH4mb1ssZ/" +
                "77YntOpRa8f1+cHI7wfv5wuPzMP41vvS/PNRm73rPeM6ihRRn0" +
                "ScIx9KIxjpE6ZJF8cqQ9RsyIlfRKVl28uG85i6JN3jBnzC0gFl" +
                "EmU3v+clYTzp+vN70W8s9aX+8Nz2nQHRwavDlsj+72OmK9n61k" +
                "4Z3K16P3huvtS0+835owXvI9f/55Cf5d3lv6ae/Xo8FHwwzd6m" +
                "1BDe1u5m5hP3kNJRxDLxrjGKlDFkcrQyDtMTzirKRXsurixX0T" +
                "F5TAnGzbUsM1J/f85azGX9+jfF437w8VfH+036OtT+rprd2pdF" +
                "4/NyvxTG6Gjacv/1MTz1uB43krrnjmX4SNZ/u/r8N4pnPpcC+x" +
                "dsBvPNO5sPEs8z+4PqP5efFlzc+G4nkp7niO2x9VtLCn/ZH+Pu" +
                "z+qMy/3B/tSmrsj3zk5+J2+/ef+m7d/NR3K91/fjMr6z27HXa9" +
                "+/LvJ56u5/Nl+el+Pr/6xA/Lus8/e48qf2JHZiE/Q+83szV/qI" +
                "pM8f8I8+m8UksX/cY09fSkIf/Kr/827j/zpfjyc+VGXPefoeOZ" +
                "X4krnnpf2Hj68j8t8Ux2wsbTl/868dSbNXNgc0x+bgbOz80mY6" +
                "dP6VNQQ4sy6JsSwnI014c6OU96pE8+pRU+xnlxP9RHbL4qZXLc" +
                "1CU2BTeXbWLPWdpMTSYm3vHJi9/T5t96/gR/U0FLs/71ul6HGl" +
                "qUQd+UEJajF7f5GMhJj/TJp7TCxzgv7of63I6Ll/RIrLDNc7dt" +
                "zp5Y2kxNJibeEeMNflQnP/WYX4zrwP9mbN+/vmYf1bXg2J/8Hj" +
                "ae1fzn6/73m8O41Nwf6SU1taV9bvpH+2jsk57tcgsO6/e8cLzX" +
                "vmZtjz/YR3uM5xi99Gzg9d6s//8AGX5QKQ==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value13 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value13[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value14 = null;

    protected static void value14Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 2331;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWkuLHFUUbgImZgQXCoKY6ECiMxo1wxjIjChaLxcmKOIDR+" +
                "PCvUFc+AOccXoSe35FFuIuZKHgRtC14nOhxlUkTlBGfG1ERevW" +
                "6dPfd+691V3TqZ4kY13qPs75zne+U1NVXV3TxXwx3+kU80U1uk" +
                "3muoZXbTyT1comVohRLnAKf/ZcYXKyD5vN6GvwMRbBGaFI1bj8" +
                "1s8MrMBm9jPyEQI+L7dOJ891dJ4817Wbp+fELrb0XE6bIHhXlE" +
                "QCo7P0nLNyDviUTWesymZ1u7JbHhuBipRR4mzFXJli3Sqm1FeC" +
                "DbH+9sbrOktv7uy4bbI1pU+mT0ovo9pk3XvXWoBlNMerHXGIR0" +
                "7Lwj7WxXmwZp6YLpsRqnR865U4N6uHylCpr8THDzs/J7H13ruy" +
                "5+fa9HZnXP69/8lxV3ZX+fc0+Z1l9CaoGFY429hWfhgvri7/8p" +
                "mmyKHX+7Pps9LLqDbXsvuz+63F2XQGNMdLL3GCRXylsc/JLOrz" +
                "dXEerIG1Nuv3Y1ErKrDcUK/scaW+EuAxrzk/Z7PZTufN18zfbL" +
                "bROTBbhxXOVu4b7495fs42Pj/HUrp83/Kh5X3leKRaHeT7Z/K5" +
                "j+5+G2E4UMO8IOPqqwNLoHD15Nh3pTtG+Ofj9rCmPv5Oc5d9vr" +
                "Q8sEVFD448nud34PE83+R4dj8Y93gO8mxqLzPY7MzOazRv1uOH" +
                "reoY4giLqlMeixtdRV1+RI/iSS5oL7OYN5zX6rmwdY9FNcsClF" +
                "U+Ov9w/rr8iK7jGXq9fxNcn89s/XonS0ufRZd1vX/T5Hpfffqy" +
                "r/eftZcZbHZm5zWaf67HD1vVMcQRFlWnPBY3uoq6/Iiu4xl6fn" +
                "7c6O8SnJ+9D6/iz6OPm5yflaX1z/dwa3a99z66eq/3U59O9nof" +
                "djx7n7XxvHR1Hc9eo+M5/vPnsPchyUtX73u31SdG+I/XXO8Trm" +
                "no+fn5ePfPa//8nMz9M7m3jeu9u7f+eHb3bPvn0b2T/H601un+" +
                "4o5nd3P5SPcfOZ7d39o5g7r/BpY/2js/u7+O8P9VczxfHsn8p6" +
                "C6f7d0Jf3T2cHb6bcbvb36ajz2ZCVZkV5GtfXX09YCLKM5Xu2I" +
                "c9e7+jUnWLp7rI91cR6smSemixUjlmqdjnOzeqi09fpNcYz/v5" +
                "2fycPtoZpta+e3hl/ZvJaOZ++H7c64cp2M2b5sn///o8vfHOeV" +
                "3Oryh+/nx9nyjXxDehnV5lq2P9ufb7j/d1gMmqB59+3M2K9nv0" +
                "WyTxWwCssMS2/D1x5GoCLUyvmZe/kMR8RqitfPykvb0fxoOR7V" +
                "sfIc1TUsjEET38qmxvqRmOvMsjEvfDpjVTYrWGO6OLetwo29H/" +
                "2KmQEKwpri9XtVLeTlU7frZaw8C7qGhTFo4iuP5wJjbAwYB5kN" +
                "MvTpjFXZrGCN6eLctgo39n7yK2YGKAhritdvq9rq+6Vr//tmsd" +
                "7k+5FDTeD90h8773ieemey398jn++38Cpt+fcxbfGNy1MX146u" +
                "9JH0EellVJusk4esBVhGc7zaEYd45LQs7GNdnAdr5onpshmhSk" +
                "dXU4yb1UNlqNRXYvFpkRblWOhYeQpd+xZgGc3xakcc4iXv+m6f" +
                "BT46noXNgzWw1mb9fizUrE3HuVk96gqV+kp8/Mjr/Wz9qvasP1" +
                "uHTc82Y2iao724sM5xMuSP549LL6PaZA0LY9DgY4yNAWP//LzR" +
                "Itmn+ayKMCtYY7o4t63CjWvTfsXMAAVhTfH6WXlpO5YfK8djOl" +
                "aeY7pev0ktjEGDjzE2BoyDzAYZ+nTGqmxWsMZ0cW5oUcvpI37F" +
                "zAAFYU3x+m1Vj+16bFen43oZnUdWbr5+s1oYgwYfY2wMGDWzRY" +
                "Y+nbEqmxWsMV2cG1rU0rvHr5gZoCCsKV6/rao4XBwun14Py9h/" +
                "lj2s6+IpsTKGvOTDrr0ygRFWtlkfPVGbPLrydYa61O9rHFiesl" +
                "GWgfPbzPY4hKoVP+x5vji+A78fHd/25/lD/TdBc9lc8HvvuUbv" +
                "xObqsMLZylvFcX/vPdf0fV1rSvV4zmQzQY6ZRppn6rAxzm1+/z" +
                "nTPpI+lfbme6WXUW2uZfPZfL63ev85wDgLmqB5V5SbCxaMlcaK" +
                "E0j2KRuUQZXN6vb1W33tYQQqQq2cn7mXz3CE6veV+kr4SMo+7P" +
                "6Zvtj+/bMZ5+Tun3X527l/5rvz3cE5W9ncnnwhXl0rVuaItH62" +
                "h+zsUx7L5Edrry2mllXE9Q3+0/YF41nJ0Ot49yhbP+dUPlWOUz" +
                "pWnildJ9+phTFo8DHGxoBxkNkgQ5/OWJXNCtaYLs4NLWqRmkJu" +
                "GxGrKV6/rao4UZwonyFOyFg9T5zQVub+El5p67fBqz6dyW5nbl" +
                "x9lfkR49rqSetjjJ8HFh9jEfCxIs3LNTG3+sFuM/sZYbP4yH1g" +
                "8CRyaqr9z9f126/s5/vpk5NkL14oXpDejWrRdfIJvGrjGeKx2x" +
                "lG5fdzWh9j/Dyw+BiL4IxQpHm5JuaGWmW3mf2MsDG+WCqWynFJ" +
                "xsq+pK3M/TW8auOZrHQmu51hVH7EhLmpmqUwDyw+xiI4IxRpXq" +
                "6JuaFW2W1mPyNsjM8uZheD59jKJvbsYvq9zGGDDxG8sz1kd3zq" +
                "U3yI4mjttYU8VkWYm/nT720Vyqx8Nc/2F0fZNPvQ338e2nnf3+" +
                "tqauf5M7uUXZJexupIX9K1m6cXxA4MmiB49+3M2D8/Llgk+1SB" +
                "VRFmdbvjievi3NCiFs7P3OCDklBpWD8rL20bmfsNyoaOlWdD18" +
                "k9alHr+t3qFYT6GGNj3O8VYbVs2UZ3T+jTGauyWaGTtYcRQAIv" +
                "NYXcNiJWk58nM7+3EV+xWCyW99FFGav76qK2Mvcd8KqNZ7LSme" +
                "x2hlH5ERPmpk+DxTAPLD7GIjgjFGlerom5oVbZbWY/I2yMLxaK" +
                "8i7n+qJ/t5O5rJMb4FUbz2S1somVMGCGsRjcTW1O62OMnwcWH2" +
                "MRnBGKNC/XxNxQq+w2s58RNot326MHXVMmN5NVcr21cc8e29tZ" +
                "nH/UpnjFxmLAqrqgzPfAJjX5/GyzR4GrlRzahig74BpWsluEoN" +
                "Czx/Z2FucfeTwPWMZYDFhVF5T5HtiSJMbPtupMSpSLq5Uc2qLK" +
                "/gORenXX");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value14 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value14[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value15 = null;

    protected static void value15Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 2392;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWk2MW1cV9g4hlixmg4aRAIm2QtWQoAk/AtvPZkMFQqioAh" +
                "YgJH6mtIgFm6pVwYbM0FgsG6Rs2qy6QAIxXbBCDT8qgUCgKpXI" +
                "IoJNWEXK7Cbqhnvf8fH3nXPPm3mePMdt/eR7z+/3fef5Z+w37v" +
                "XK2+SWWvvv7XV+m32st9bb3tYq0cf/xpE9jqTZH7IR9lCtMYlo" +
                "XNHBwpzcw7lSl1dlcUpdlhEadd9/yStmBFWOKSIGy8T14xs4kl" +
                "/f4fsIe8hpTCIaFwu7WOkx+rjvQY7OZ6Mq1Ma6LCM06r635RUz" +
                "girHFBGDP0Oo75/rn+v18pr3fBNb/f654W8RQa1Y4qkld2thV3" +
                "zBQ4xzuFlG9i1OqcsyQpGqyX02L7bqggLLbFVxjOv7O/2dtO/I" +
                "Xsd39BCPI+whp5bcrYVd8YevWU7O0TQ7JQ/rsjW2ghmhSNVkfp" +
                "tnBFZgmT0jnyE7tb9Vv2p6ty0zOTK93bb/ft6aVMw+sVLWl6uX" +
                "ZdVdvWxlW1euQUVep7fFKjtLPIvGuMhBmVaUrLF238H6NTL7pO" +
                "0qp2eVXqlXwmeS1ZvMrxeP5aeaMs2ROLaG52eDiv2XVss7+cjk" +
                "ocn70n629j7Y6/34h0sifKAhXvyVmXw4PUaf7kj3+0/IfzSO/+" +
                "zVhvoPFZEzSypqeCdZ9nz6988TPs/31/u8nX1mpa+Kg+pAVt3V" +
                "y1a2deUaVOjKNbbHRpN3xcY4p3yqjNEta6zdd7D+ReSK7Sqnr8" +
                "/6oJwpmp/PZL6PHh49LL7u2cq2+FgRs7n8/LR5j+QewVeQkx6u" +
                "4nqN66qHxbMTsN4YtXrFTxyh+luZ9z1zzjOj9C6RV9nrzBn1Ee" +
                "EaHMhxje0B4vz19llbyTnlsypKVqBGupjbTpH3vS0/MSNAQTlT" +
                "PD8rT7GN0UZx5uuYxEcbwwOx5T48kChWtXxP+uR8UKIPD4YHYJ" +
                "GeSAN0AFUPRbJVrMDGOSqqbFRQhwdWJxg8QhwT9upadS29Bq5h" +
                "Vy9b2daVa3wF7j6utlrp+fk5W8k55UOPR0bEquUK5rZT5H1vy3" +
                "aV00NJqbScn5Wn2NXqatqvYlcvW+On8pojtgYVmuMa25PO4JcQ" +
                "tWiMi5xaqPCsyhDrYm5o0YjMVGLbjmimeH471fgvOBJXfdej2q" +
                "62bSTHymqtkEhepU9qJaKWYNoezZnrS42qUOu1c77UqDvPxNiY" +
                "RdHt9JaPmbS+rriKI/n1Hb6PsIecxiSicbGwj+eP5uzLvgc5Op" +
                "+NqlAb67KM0Kj73pZXzAiqHFNEDP4Mob66U91Jj9kd3evn0B31" +
                "B69rhGtwIMc1tgeIi1eGqSxzarEqywrUSBdzQ4tGZKYS23ZEM8" +
                "Xzu6kOq8O0H+peZw7VR4RrcCDHNdXh7DH4QFwwHzJHmVOLVVlW" +
                "oEa6mNtOkff9635iRoACz2z5rGquD64YmO/dw/91+42sK7zT4j" +
                "T1daOrf9g/lFV2jYmf7eEtRFDL1dyvcSABca77lkdBzupiHviM" +
                "E+myjFClO/cxtsRVOaaIGCwT1w/ODs6m95SzuueMeNmefcVGUM" +
                "vV3K9x9KFfz5VH4RxqLA98xol0WUao0v3C9Rib1UNlqdQr8fXH" +
                "Xa+bfdXPef7GvV6v6+w64ymv11243uZ63d5jq7heN/ha7y17++" +
                "nnT8g/EsdXPdNxz8/BA91fT17387Nppq6uJx/7ev/6O+98Nv2/" +
                "w57P87/r7vU+/flKr89/e83X5791vxmXPZ9LzvOd9Z7P1f5+ab" +
                "A92JZVdo3lo9qoNgbbz20hglqu5n6NAwmI8+8RGx4FOauLeeCj" +
                "1sZs3vdCTeaPsCeXeSpMETFYJq6v7lZ3E8dd3euJ76qPCNfgkN" +
                "z0tvb6Tthqpefnrq3knPJZFSUrUCNdzG2nyPvsu35iRoCCcqZ4" +
                "flaeYkfVUdqPdK8zR+ojwjU4JJfO5xHX2B4gzs/n92wl55TPqi" +
                "hZgRrpYm47Rd5nj/uJGQEKypni+Vl5dTTeHe/2enmVvb7ytKu+" +
                "j6CWq7lf4+hD//x8PuFRkKPrdbuWBz5qbczmfS/U7G3F2Kwec5" +
                "VKvRJfH/zdx+9pv3jKd+XH37rfA1arbbQ52pRVdo2JjwjX4ECO" +
                "a0abwyfhAxGczFHmoAyqLCtQI13MbafI+/BJPzEjQIFntnxWNd" +
                "eXn+enLy4+2zz9zvs8f+HB9X0/aomz3O+Xnl3v+Wz6/Ln63y9V" +
                "D1Tpu+5zhr9q9Y1eqqJawezkW8et0/U18U8ut61scxs/On4Utt" +
                "zTe/e/yhpduTqy6UrP93NPhJ9yTzQrYsQIGZiqi5G5X9e855mg" +
                "x6r3s/Dd8tja1teXiqud+998219futHm9b7/jXt9vQ8u6iqWU3" +
                "Exths/lVxcPmOr2rGgyio/mf94/CZ+dJ+EM3heV7GibGk36nl+" +
                "+YytaseCKqv8ZP7j8Zv40d0Op/4k9UJpdfRp94X1Y3U9UwvG+e" +
                "fP0S9HL64GeZ1YXc/UgvFyaXWNvD6srmc64VPdq+bve/H3ePKF" +
                "pRGvnH/33PrzZP6LyelfF/9XeNc88od6/VPQ//d0fz34a/pII2" +
                "P9C60fzf/+T38/3+vfGU3/6Wea/jFA+Efr6f6W7tenrx1Xg/M5" +
                "eqPbR2sy7fCRv3nK5+cbK31tXxpdklV2jeWj2qw2R5fy9yNbg0" +
                "Oq+e7jjDj/1rFpKzmnCliFRfY6I13MDS0aYX7GnlzmjmimeH5W" +
                "biPl87PFJ5Zne2u5nZ539YrHv5A173kVnyNaI7vakW87I64o6n" +
                "WwiriOtUWMimV9tqJJrE4fsd3a75nu8fn5zJqen8/c/862t+Gb" +
                "suY9r+Jr5Cc/kDhq1Y587izjmjlJB6uI64Zv7j/FaJ5RsazPll" +
                "dory+VCiwi+DxTeod+j6x5z6v4GpE8YrAjnzvLODDDK2kLHawi" +
                "rmPNESOUs89Wk0Lg2axFBJ9n6vX6/5E173kVXyOSRwx25HNnGQ" +
                "dmdIMOVhHXseaIEcrZZ6tJIfBs1iKCzzMl+zey5j2v4mtE8ojB" +
                "jnzuLOPADGdY6GAVcR1rjhihnH22mhQCz2YtIvg8U7Jvypr3vP" +
                "ZvalwzWmPtyOfOfvCJG+hRTitYRVzHmiNGKGefrSaFwLNZiwg+" +
                "z5Ts/8qa97yKrxHJIwY78rmzjAMznGGhg1XEdaw5YoRy9tlqUg" +
                "g8m7WI4PNMx16fb/Xd7G12fb5hpo7+H/d/8pYsVg==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value15 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value15[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value16 = null;

    protected static void value16Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1933;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj2IXFUYncbGTguTQomgokRszBZBLMz8JIWSoBIQs6tiIe" +
                "lEiGgVNjOPOISAP21iYyNoYyW2/qFG/EWIRkSbDYrYbKPFIr47" +
                "33zvnO/e7755O5nZnaz7Hrn3+zn3nHPfvn0zeUmr1Wq1/22Nxj" +
                "CHUXKtSB81xF7OK9M6OL0DPtiFj2PPniKcc85RziH4bNcyQi9W" +
                "0qNzTEeJcrU05tzDNO0wJpz1CMVhReq8Tn8Sf9zHtYiVMwzHdZ" +
                "QINRulMecepmkHR3Fz53g9Tt1ah75fX38Sf9wvbrKr8zwP/Spj" +
                "mMMouVakjxpiL+eVaR2c3gEf7MLHsWdPEc455yjnEHy2axmhFy" +
                "v17+vf27+1nJdG2Z2t1pkXtXfo7laDo39Hpn4wqdzTmtkxWJ/g" +
                "6n6/nttT/66kcmBzjvoPjBVeljHMYZRcK9JHDbGX88q0Dk53p5" +
                "UPduHj2LOnCOecc5RzCD7btYzQi5XKJ0BbxjCHUXKtSF9rxS2I" +
                "4x49a/b4dajlO+IALnwce2b3MVeqihX1XmIHlhF6sVL5u/+HjG" +
                "EOo+RakT5qiL2cV6Z1cLrPrMoHu/Bx7NlThHPOOco5BJ/tWkbo" +
                "xUrltT0iY5jDKLlWpK814BFzzvXBP+5P/kj2nqh8sAsfx57Zfc" +
                "yVqmJFvZfOkcHfnrtYL1aq8A/rKFF5PS6nNSARc+5hmnYYE856" +
                "hOKwgrkn60/ij/u4FrFy8ux9ScYwh1FyrUgfNcRezivTOjjdz4" +
                "DKB7vwcezZU4RzzjnKOQSf7VpG6MVK1fV/VEeJUBv8ybU05tzD" +
                "NO0wJpz1CMVhReq8Tn8Sf9zH9YmVk7UHZQxzGDsHta4dxdjYy3" +
                "llWodaviMO4MLHsWdPEc5j1dxO0h3YrmWEXqxUfjb9ImOYwyi5" +
                "VqSPGmIv55VpHZzuZ2rlg134OPbsKcI55xzlHILPdi0j9GKlMv" +
                "5dxjCHUXKtSB81xF7OK9M6ON09VD7YhY9jz54inHPOUc4h+GzX" +
                "MkIvVqru58d1lAg1G6Ux5x6maYcx4axHKG78N4gbc85z+pP44z" +
                "6uj+2nPJ0HZQxzGCXXivRRQ+zlvDKtQy3fEQdw4ePYs6cI57Fq" +
                "bifpDmzXMkIvVkoPvA9p+Gbir82gz7/W2tbj/KvzVmivyxjmML" +
                "bXtR6i4gWpA9tet2vb6ymjX9fOJB/swse114dnmC1WVC6bcxQ7" +
                "7L8V+7QOLCP0bH1YOO+dTs31/nxjm+/P1+fLX/f+s/tjjB6eW5" +
                "T3n/19073/TPfkvf8s3pz+/efWPj87H23v/Tlv/dr788rivp+f" +
                "+v68sl33Z+dAp2Q8d7v56TbSEFSK7Q+EcxbH4OqU92dG334eNd" +
                "9r9o3KbzpK5HXTuJ5tcx2LaqYClHU+Wb+eP6eP1T5P70Lvgowy" +
                "ay2cnf2d/VrRaqjgRI8xARViwYJx9DMfc4IBPWWDM7iyqmBl7+" +
                "kKIIFnfea2K9R/7DR2wleS3V/L55HzDfH51sIe8/XWu9i7KKPM" +
                "WhvnR7XCGJzoMcauASM0jUbSgzO4sqpg9XyxNrxUlaPxjpkBDt" +
                "I9+fu3u6r9PFrqLCXP6KVGz/ylHNbj3OLvS0uzRzb+vnR5cb8v" +
                "nX1vyu9Ll5t8XxpVZvR96ewHrR18DN9phHp3Ovbu4e5hGWXWmu" +
                "SoMAZn+W1hVddxj9eAEZqMTHtwBldWtXv40GrqPV3B/rWijmNu" +
                "u8Lbk7d/vpKj3lp3rZzXdB511jRHhTE4S3endR33eA0YK2WDTH" +
                "sasSur2l07dFrZU1+sbXfBjmNuu8Lbk7d/up6jXm+pVz43w9gb" +
                "P30llrz7E7pa40gyjeSPjTD3qqe71bQ9xsQ6qMQYi2BFOFJd3h" +
                "Nzw62yW+VYETWLr/v+2f155z0/57un7onuCRll1prkvce0whic" +
                "6DHGrgEjNBmZ9uAMrqwqWD1frA0vWpE9pdx2hbcnf//Rrla6K+" +
                "W8ovOos6I5KozBiR5j7BowVsoGmfY0YldWFayeL9a2u/B2Yxng" +
                "IN2Tv/9oV8vd5XJe1nnUWdYcFcbgRI8xdg0YK2WDTHsasSurCl" +
                "bPF2vbXXi7sQxwkO7J37/dVXn/bySfBRuhJnWMqKE6fB85eJgx" +
                "ZWcV1vARMd7WPJRGq/vy2hbrsTb6u/pGnPU2iueKJ2Nc8cy8nt" +
                "bFszPkOrGIn3bhehZPFU/M9noWy9t/PYunr4l9ZZGuZ++2/PXM" +
                "9bbqejbTn9Zlb29vr4wya03yuAKsRcQ95sV6aMZI20sVY9XYlU" +
                "VYdusizw3P7DJ16u2f8XXvl4Zf7Lz3S8NLW/l+afBZ/fv5/rHN" +
                "7nvwYRV93t8zjr5MUB+Pxk+d9V+Xf35wnDySVbwkn+/jbPzvw4" +
                "PRvTH4LkF/4jB823h3X5V/vhl83+z9Z8Ofy3X178VZ/Nzef177" +
                "vx/9v4/22zKGOYySc0UxMmvs5XYl83Mn1o59sAvfL3tLFcGV6t" +
                "TtxPr0nKZ6sdLu77v3+z7L/x8yvC7e0vVfWVBfu/fnnO/P9sk0" +
                "mtGT+uT2c816Tw2u8w27n9G713Nxj6K/ew1men+e2r0Gszzw3n" +
                "l49frdxeq+3e9LO+Dv7/8BijMH8A==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value16 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value16[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value17 = null;

    protected static void value17Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 999;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWztvE0EQvhqJig4hXgkQMEG8FEVIKezzH6CjoUwFEhIKHU" +
                "1sn4mlVEiUlEhATUGBKJDyAxCiQfR0aZBChcB36/G89s6+8zn2" +
                "2XOR9zEz+803k/U+TkkQ6Cf8pFvVe2bFvbXeqrXO9Ou7SW81CH" +
                "af5URYSZFvKslaibzPjdDfzol3SUnu5ES4N4t8Ng7KyGbjoGg+" +
                "0/zzfO79qUo+qzE/O6+L5rP/G/vpyriOS9enErBxNbR9fT6S4l" +
                "MNH8/bjoG0k2MoN+0RsbSfrEg4Tx9T7U96svlZ5voZbofbroQa" +
                "enErbkNJbdACSmrDx3ApR6O4qIMWWmivYOHjxX3TKHzR6OgpS8" +
                "lUMiHniYG9zc9q7++Lvh9ZPmU+Ox8mm59Snjefqfib07yHdN6M" +
                "0L+fZH7u/cufz2nv7x7LSq2f83bftHxaPqv3PiR8ONt8pvnn+e" +
                "ydsP19Ps6f+ilrf1/Ox+anneftvrk889PWz2qvn90nizw/w/1w" +
                "35VQQy9uxW0oqY20wI+UQxtaHI3iog5aaCG9IqqPF/XNo/BFo6" +
                "OnLCVTHb+I6ig8SnqDGtoohxJkqKU6tAE5RUU8iknxPOdu5gd6" +
                "kqfmBXrJkbKhozgC9S/jSmdCdIfhYdIb1NBGOZQgQy3V4QdKio" +
                "p4HI3iqnwyP9CTPDUv0EuOlA0dxRGof/qRIzVrp2tuNbeCIC5d" +
                "HWtcD9pQog3+oI7a8DGICJ65pdZBi7LiXhHVx4v65lH4ouEIyE" +
                "DH5I+fR+VdV3eGt9jTQdA+tVj7b/vkcXvc/WinHrtv2v3I8mn5" +
                "LHTf/GurYC8scT+y+3vhJ7rV/5B5HZ2P1iCf0TXPnP6WgXUxKW" +
                "8Mejfd9z06O9SvtL6njr3S/wy+odGFkawv9889b5nkqmL6VYxZ" +
                "TcrrCit13Rj3+x7VovXMdWDH5lmpJ6hhPhsviyEUHbega/F9y4" +
                "Gd5+f3vNSu2Syb/Kn/gtK1fFrdzkbLp+FW43lBK858tP9s/DT/" +
                "ONqPU+/UO650NchcX0rQllrT8SDHcTgefXIUqqO8qB/sUxwfL+" +
                "4RWUk2EpuyR5aaqWQi7fOtn90fy7F+vnhwPPf3Zcln93N5+1HL" +
                "7u/lnj8fWQ5Kzedjy0EJ56VXULqWT6vb2Wj5NNxqPC9oxZmP9p" +
                "+Nn+YfR6fh2P3Itx8V/fuQ3lN7vzTtx94nWz7n6f3StPJZ5f/n" +
                "6rwr+v9cnvPSc5tlpZ6cvsweYdbRFY2gudHccKWrQeb6KKE2+I" +
                "M6asPHICL6pJZah8yQFfeKqD5e1DePwhcNR0AGOiZ//Dwq70ry" +
                "276jtr/Pz/5u9yO9vyeSIu/r/gO2jLk6");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value17 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value17[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value18 = null;

    protected static void value18Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1942;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXD1vHFUUnYog2Q1NKqRIAUdIiCZQ4FR2Zuc/IFnxH4gbCt" +
                "xn84FcukHCwiCBkBANSZE2PyC0SEhgUSKldUckJObNzZ1zzn13" +
                "k6xZggUzo3n3+9xzX3ay413LTTN/b/7u/M2mmX/Q9Mf87aa59U" +
                "mz1DF/a4H/w8rzTrOyY37pBfGruf/gYEH+RuV5f0lG13L/svs5" +
                "HTi297f3bTXpPrOjB7mczfXuRx3q0VNROMa8uA9sxsl4aUewim" +
                "wiNrMHy5ppZBLz/2/3+8J8ud/v/rC6+322XmvT8dK7+opfn7M3" +
                "/t3X56L+q3k/ml2YXag6Dj7zY4XPdVRqnP01OsccR5Fita9+Zm" +
                "yZRc6PK6J8Hs8MYRFqj7M2W+vlmsshsuY2PJyDEzHO0Rogjp0l" +
                "s465xqy0K1AzXtxbp8imUQQwqGfK59epuhvdjaYpa5HlMN1tRN" +
                "3Hmlmu2aUapOOjpu6NQztGDjFHM7gjGDEbjTMCM9DOsSOjEepu" +
                "t9vLXZODf9dPs9jDFmKu2aUapOOjpu5N0+zWfZiX5mgGdwQjZq" +
                "NxRmAG2jl2ZDRC3el2erljcvDv+GkWe9hCzDW7VIN0fNTUvWma" +
                "nboP89IczeCOYMRsNM4IzEA7x46Mhvz2SfukacpqskTMct1X5O" +
                "BEjHO0BojOVDPrmGvMSrsCNePFvXWKbBpFAIN6pnx+nWrr8tbl" +
                "pilrkXaYZTZW9yHKMVy+OhIQ4WWfxnBoH7ciz5qXxyNHZsNVig" +
                "D2OlesrFlbrLvW9U9NZe2ePd2b7jai7mPNLNfsUg2yG3960J4a" +
                "45zYh3lpjmZwRzBiNhpnBGagnWNHRtOp+33dKOe40xt2ye5vWB" +
                "ZWjuiqWo7/osPzPTerAarzArMYYV+GH+PYBZ7Wevj5HGZXygnL" +
                "Ls2wLKwc0VW1HP+F+3lFEbMaoDovMIsR9mX4MY5d4Gmth58Zs+" +
                "5XnMVij8XrnJjtPvO439HRhXtyDcdqXhmHnLvyjhxZ1nGexdG1" +
                "s/bTHUL+1ubWZr/HmyaH/d700yz2sIWYa3apBun4qKl706tjs+" +
                "7DvDRHM7gjGDEbjTMCM9DOsSOj6dTVz/W/j59lfzZ9XrTc0T5s" +
                "H9rq0q2iFd1XzkGGr5yjNepVNMZFDMwYXbvm3GMF83dPnKaenl" +
                "lGppEJ7ySzl8iDhbv/4GVyF9e/0lfKg/Pymj34YtSOpzt4Bfv5" +
                "5ah9Ne3GknfF4/axrS7dKlrRfeWcmIEr+l13TdEYFzEw84zYFa" +
                "gZL+6tU2TT1NMzy8i0nl+n6n7E2T8/DRfs6GELMfeZx/2mQZoG" +
                "r+JVz58LWSlOzUs7giPLOs6zOLp21n66Q8hvT9vTfl9PXQ47fe" +
                "o2PJyDEzHO0Rogjv+SklnHXGNW2hWoGS/urVNk0ygCGNQz5fPr" +
                "VPO15Pnzwpm+H3v2Xej8tVf8feLr5/796JvpfWWVx90/pj1Y6e" +
                "vzu2kPVrqf3097cNZj++r2VVtNus/s6EEuZ3O9+1GHevRUFI4x" +
                "L+4Dm3EyXtoRrCKbiM3swbJmGpnE/OV+3+beL8k77H/w98E+/e" +
                "isvw82/X5dtp/3Hp1tP9un7VNbTQ5Ppk/dhodzcCLGOVoDxPHJ" +
                "VzLrmGvMSrsCNePFvXWKbBpFAIN6pnx+nSq9py9N7ytnPbqb3U" +
                "1bTbrP7OhBLmdzvftRh3r0VBSOMS/uA5txMl7aEawim4jN7MGy" +
                "ZhqZaH631+31cs/lENlzO3qQy9lc737UoX7ch4DCMeRoH9iMk/" +
                "HSjmAV2URsZg+WNdPIRPNnl2b93V1WkyViluu+IgcnYpyjNUD0" +
                "fdDMOuYas9KuQM14cW+dIptGEcCgnimfP0x1Mjvp5YnLIXLiNj" +
                "ycgxMxztEaII6dJbOOucastCtQM17cW6fIplEEMKhnyucPU+3P" +
                "9nu573KI7LsND+fgRIxztAaIY2fJrGOuMSvtCtSMF/fWKbJpFA" +
                "EM6pny+cNUh7PDXh66HCKHbsPDOTgR4xytAeLYWTLrmGvMSrsC" +
                "NePFvXWKbBpFAIN6pnx+nSp7z7/15/Tcc+bnpfvdfVuLhM9trO" +
                "5DlGO4fAV6jc8+jSkz9HEr8qx5eTxyZDZcpQjcny9dIxPkX794" +
                "/WLTlNVkiZjlOnuQy9lc737Uod47RxSOIUf7wGacjJd2BKvIJm" +
                "Ize7CsmUYmmj87nh339/2xy+F/gmO34eEcnIhxjtYAcfyfRjLr" +
                "mGvMSrsCNePFvXWKbBpFAIN6pnz+MNXR7KiXRy6HyJHb8HAOTs" +
                "Q4R2uAOHaWzDrmGrPSrkDNeHFvnSKbRhHAoJ4pn1+nSj9PfvyP" +
                "fXd2e3VYt387n+9I0+d12ed1d46nzz/Px34mz5/T3xP4e8+gn9" +
                "taZFnNZo/nmHQ9s7Uy65V5Iw9mkecxt6yjY6nNWjaJ8owerfb6" +
                "2Gm637P7ffCc8X5v120tsqztuvuLdudj8yO3pb8yEG2ubJO/Rg" +
                "D0LOYZzCLPa9cPfmK02NGx1GYtMpx/HXkqA0VEv9hpen2u+v3o" +
                "+re2FllWs91jcfigZzZX1n5gcr3qxiDmxRrmnHUE89jneZMoz4" +
                "xp3S92Sp7nf57eo1f5PL/sz0fT+xHd738BBcT9VA==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value18 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value18[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value19 = null;

    protected static void value19Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1725;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXEFrJFUQbl314GFVFHGjqKCCUTcHzSGIINszo4JHL6JmXV" +
                "0V92gY0WvPDmMyp71kb7kIKgiSHDx48hfsLxDRm4dVIYoX0ajd" +
                "/VLzfVWvJpOe9GRk0z3kvVdVX3311dvuSXcHTZIkaZ1KyrGYiz" +
                "HY4ul/EPziw9qzOTP2S8Q/oINV+LjWqY3vmM1WFC5t88oq7H1q" +
                "dWoFmhH1bKXe2d6TvQfyebm0Hk2S7MOk0tF7ZIx/JfI8niQb3y" +
                "e1HL2HJsSfrsj3GFv9rdzzTEWGZ8Oc7oaxmIsx2OLZ+CH4gZW1" +
                "Z3Nm7JeIf0AHq/Bx6e7ln5jNVhQubfPKKtTnZ6xAM6KerRQfVc" +
                "/PCv+Cl5Mb/EivpdfCGGbxBRsexuCDGGN0DhhRk5FxDMqgSlcF" +
                "q6eLa+suvG40AxTEPfn9666O9/w8Sce5H2UMKy8arw9mqxbRqM" +
                "NVAUorn1z/YP5x9ZHt83S2OlthDLP4gg0PY/BBjDE6B4yoycg4" +
                "BmVQpauC1dPFtXUXXjeaAQrinvz+dVfN9V7vMfii2v3n4POj3n" +
                "/Wdq8w0/vP0jPF/efGL9X2c+PnZj8n3c9H1/veAefnV801fdDR" +
                "3mnvhDHM4gs2PIzBBzHG6BwwoiYj4xiUQZWuClZPF9fWXXjdaA" +
                "YoiHvy+9ddHf/ze23f/Dv/z+u92c9Zf38Ovmm+B2u9h2r2c+qj" +
                "s95ZD2OYxRds6wGW0ZwvfuQhHzU1C8dYF9eBzTyeLl0Rqqway8" +
                "3qoTJWapVofLqULiVJMYa5fFOyJLb1AMtozhc/8pA/ehNjWDgG" +
                "jK4Dm3k8XboiVFk1lpvVQ2Ws1CrR+Ha33c1/z3dlLn/zd8WGhz" +
                "H4IMYYnQPG0Z2FQsYxWbEqXRWsni6urbvwutEMUBD35Pevu+ps" +
                "djbz83RT5vLM3RTbeoBlNOeLH3nIH12nhoVjwOg6sJnH06UrQp" +
                "VVY7lZPVTGSq0SjU8X0oX8PF2QuTxzF8S2HmAZzfniRx7yR9ep" +
                "YeEYMLoObObxdOmKUGXVWG5WD5WxUqtE41vXW9eTpBjDXESCJW" +
                "sZgcEHMcboHDDKPmhkHJMVq9JVwerp4tq6C68bzQAFcU9+/7qr" +
                "/ByNntY7e4Uv+DHCp2Mc52i8jqtYHk+Hxmufh5pUu/2KjXush7" +
                "o32rNW8ZPdEr//zO7Ix7vL9T3leG/+c3+SDG/dR5TPEsPR35yz" +
                "J/Kfp8QaEmO2Mvavfi9kL2YvlauXxyBWs/NkvZO9n13KbspuNq" +
                "jT2Z1k3UfrCW+ghrft457Lns/S6vuZvaGsi/nPu+liuphf94sy" +
                "l98Ei2JbD7CM5nzxIw/5o+89w8IxYHQd2Mzj6dIVocqqsdysHi" +
                "pjpVaJxVd7fh/eftTn93P/zPf957j6J/19yGzfJw9PH89+Du9q" +
                "3s8352f1/Rx8W9/7uvRsvKp2TJt3HMdstfXf67/WP99/VfkuHP" +
                "mt1cNjqr09PlZZ+esT4m9W0zYNysk70zkTxjCLL9jWA6xG2Bjz" +
                "Ih81LVLH4oq2qlWlEZpdqxjPDc2sMlbq9Q98upLm33LFGObyil" +
                "gR23qAZTTnix95yB9dcYaFY8DoOrCZx9OlK0KVVWO5WT1Uxkqt" +
                "Eo1vb7e382ewbZnLJ7JtseFhDD6IMUbngHH0xKeQcUxWrEpXBa" +
                "uni2vrLrxuNAMUxD35/euumr/Hzfd+af3iybhfWn9ruv38JBns" +
                "Fvs5+LW3PNgL+zn4vaYz6N/I80eN5+dvE+J/Tc385/78d013aJ" +
                "fiVd3M8+Oquyd93Ljn53BhHudns5+zv94HH8/qahg+eHzPlYOP" +
                "5vZE+2UYi7kYg80ewYRZ1p6tM5mfI7a21cEqfL2sLa4IrrjOQZ" +
                "1onZ7SuJ6t5Pze7ybNMeXRutq6GkaZxSpWxVpGxgAhI2N0jvZq" +
                "NuZFDMqYXVf1tdsM1i8e203cPau0Sq0S3klWz0fz3yPUfM5+dv" +
                "iIh7W+8Xz1K5xl1UP9LlpOl8MYZvEF23qAZTTnix95yEdNzcIx" +
                "1sV1YDOPp0tXhCqrxnKzeqiMlVolFt/8vaPO9yHparoaxjCXO7" +
                "0qtvUAy2jOFz/ykD86rwwLx4DRdWAzj6dLV4Qqq8Zys3qojJVa" +
                "JRrfWmut5d81a5jFKlbFWkbGACEjY3SO9mo25kVMVkDEVQXh6d" +
                "K1uQuvm7h7VmmVWiX0fb2Pb673eb7/PCn7Of3/76LZT7uf/a+n" +
                "Pz9bV1pXwiizWMWqWMvIGIvAj/XLWlaajXkRkxUQtipYPV1cW3" +
                "fhdRN3zyqt0rh/3VVzfjbfn81+8n62Lsx3P8fVr2k//wMt4BHf");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value19 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value19[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value20 = null;

    protected static void value20Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1697;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWl1rHVUUnVIEUX+C4EMUBPFF+yA2UDKZW+hLwZA3P2LEn6" +
                "BI3uJUbiBvoc/+B98L/oP8gUoohDSUUomKL6Kikznds9baZ8+9" +
                "uelNSJM5Q845e++11177ZO6d9N4WRf1+/V79ZlHUN4pm1G8Xxf" +
                "ffFjONeqHH/1HmebeY26jfmhL/YEa+dzLPhzMyfBz7Zz3PYcip" +
                "DvfnXO/P4TyH1/vLNYbzHN4/h9f75RzVzepmmtNqvmTDwxhciD" +
                "FGc8CImozMY1AGVVoVrJEurq1dRN0oAxTkPcX9u64Wq8VmXbS1" +
                "jSyaDQ9jcCHGGM0BY1dZkHnMdqxKq4I10sW1tYuoG2WAgrynuH" +
                "/tavnR8iN/zyZf8mOGT2M+boy8j8ZxzDAxipkMx9oUpQq1tvJr" +
                "F5MU5Dn9Pqs+PI/O+u/57U+G85zX8335QfrpuesfnIT3ZKizHt" +
                "NVnIXOcqFcSHNazZds7wGW0ZxvfuQhHzWVhWOsi+vAZp5Il1aE" +
                "Kq/Gc7N6qMyVeiUeP/z9Odf78055J81pNV+yvQdYRnO++ZGHfN" +
                "RUFo6xLq4Dm3kiXVoRqrwaz83qoTJX6pV4/GzP9+3XhufR8O/N" +
                "8xvbn+a+rc9fhHHrs6t8nqNbo1tpTqv5ku09wDKa882PPOSjpr" +
                "JwjHVxHdjME+nSilDl1XhuVg+VuVKvxOODe/aL4XV76nf114Pz" +
                "/KrbfT0D0xvPc7485w5evehnPLx/vsD7ZzWq0pxW8yXbe4BlNO" +
                "ebH3nIR01l4Rjr4jqwmSfSpRWhyqvx3KweKnOlXonHv6yfL41/" +
                "uoh/f1ZH1VGa09p+MnpkNjyMwYUYYzQHjN0nr4LMY7ZjVVoVrJ" +
                "Eurq1dRN0oAxTkPcX9a1ejzdFmc59u2treuZtmew+wjOZ88yMP" +
                "+d3r1LFwDBitA5t5Il1aEaq8Gs/N6qEyV+qVKH60O9pt1l1b28" +
                "iu2fAwxqP1x2ZjAiO87NMYnajUMUt5Il0W9xpZjXbMDFyff3xm" +
                "rjrFbl+7fa0ojue0HkeSZXubgcGFGGM0B4xWWZF5zHasSquCNd" +
                "LFtbWLqBtlgIK8p7h/7Wq0M9ppznXH1vakd8z2HmAZzfnmRx7y" +
                "u9+kY+EYMFoHNvNEurQiVHk1npvVQ2Wu1CtRfLlWrhXF8ZzW9p" +
                "OnNbO9B1hGc775kYf87nM2x8IxYLQObOaJdGlFqPJqPDerh8pc" +
                "qVei+Gq/2m+eS/u2tk+qfbPhYQwuxBijOWDsnoSCzGO2Y1VaFa" +
                "yRLq6tXUTdKAMU5D3F/WtXw/eb8d+fP/x4yr8/D6qDNKe1PekD" +
                "s+FhDC7EGKM5YOx+k4LMY7ZjVVoVrJEurq1dRN0oAxTkPcX9u6" +
                "6uV9f9OSdf8mOGz/bI1Dj7c3aOGY8y+Wyb7YrUsopYH2f4dZLO" +
                "iKGPteHZqDaadcPWNrJhNjyMwYUYYzQHjF1lQeYx27EqrQrWSB" +
                "fX1i6ibpQBCvKe4v61q3KlXGmeSyu2tk+qFbO9B1hGc775kYf8" +
                "7rnsWDgGjNaBzTyRLq0IVV6N52b1UJkr9Uo8Pnhf/aZTfP+U35" +
                "reL67oKO+Wd9OcVvMl23uAZTTnmx95yEdNZeEY6+I6sJkn0qUV" +
                "ocqr8dysHipzpV6J4sv1cr1Z121tI+tmew+wjOZ88yMP+d05OB" +
                "aOAaN1YDNPpEsrQpVX47lZPVTmSr0SxZer5WqzrtraRlbN9h5g" +
                "Gc355kce8rtzcCwcA0brwGaeSJdWhCqvxnOzeqjMlXolHm9j6Z" +
                "nNaQef7nQfj6Vn/fhJVh9DjFBUn/Iob3oXffWRPY1n6bHNaRdF" +
                "832vnsezRxR1sipAqfLp9Sfz99VHdsyz/d3w7815ft9BJ39oc9" +
                "q538thvO/9/R7OHlHUyaoApcqn15/M31cf2X08s92f44dX4/4c" +
                "/3y6+3OrGP92fJ7jX+sb43/TeY7/mNM3kP9lnj/n+P3m71Pif5" +
                "+a+a/n6z+z5y7VS3Wa02q+ZHsPsIzmfPMjD/moqSwcY11cBzbz" +
                "RLq0IlR5NZ6b1UNlrtQr8fh83HulGMYcx9YvwxkM53lxx9n9/+" +
                "T63qX/fOlp+TTNaTVfsuFhDC7EGKM5YERNRuYxKIMqrQrWSBfX" +
                "1i6ibpQBCvKe4v5dV3vlXrPu2dpG9syGhzG4EGOM5oCxqyzIPG" +
                "Y7VqVVwRrp4traRdSNMkBB3lPcv3Z1vq/3yz+qJ9WTNKfVfMmG" +
                "hzG4EGOM5oARNRmZx6AMqrQqWCNdXFu7iLpRBijIe4r7166G79" +
                "/n/HnI/0YBELc=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value20 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value20[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value21 = null;

    protected static void value21Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1484;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWT1vXEUUfSnyL5AoAIFMaAwForCs3be/I0JJE8kpLMEPYE" +
                "1QWMn8AhB9ChO5QkKUERI1okBOGfeIgpYdX1+fc+7cZ++u145j" +
                "Zp72zv2ac8+d+L2dtxltjba6brQ1Op27zi3X2YNczub17sc6rO" +
                "9OR0ThGHK0DmzGyXhpRbCKbCI2swfLmmlkovn9w/5h1xVpc4mY" +
                "5bpL5OBCjHN0DRB9HzSzjrnGrLQqUDNeXFu7yLpRBDCoe8r716" +
                "66bvrR9MPpW/P5k+Kdvtt1X33ZLTWm7wz4P608H3RrG9O3L4hv" +
                "Lon3XuX5eEmEz+Y7utPvmLT5ZKd33IaHc3Ahxjm6Bohn/5KSWc" +
                "dcY1ZaFagZL66tXWTdKAIY1D3l/Yeudvvd+bzr80lk1214OAcX" +
                "Ypyja4B4Vlky65hrzEqrAjXjxbW1i6wbRQCDuqe8f+1qcn9yv+" +
                "uKLHMZpruNqPtYM8s1+6iG2fGxpq6NoRUjh5ijGVwRjJiNxhmB" +
                "GWjlWJHRtOvqOXDs2uz7ro0Vxvb75YJlH82wLEiOqFQtx1+EEe" +
                "dma4DqvMAsRtiX4cc4doG7tRp+LdrN+NnikSw3+obxVh2LIK6/" +
                "6sonEdzvP7Z79/Jj2fNnG20/r2r09/p7rqnPbEj4NFbHI9JwZV" +
                "vDWZzvfpd+ndcB881RYxeRwRDb4Z3j6v1Gv3Fqb5xFTnxmQ8Kn" +
                "sToekQYYbngdzuJ897v0q8ZRFq7lqLGLyGCIbcY/ZuQos2ftvl" +
                "3nmP10q7s7WCjr+cqno/b70hp/X2r7ue79rMfed+2Zt9YnzC9t" +
                "D1Ydk8eTxyZtdp/Z0YNczub17sc6rEdNReEY8+I6sBkn46UVwS" +
                "qyidjMHixrppGJ5k8OJ4fz+dDm08ghbEj3IcoxfFw6EhDhZZ/G" +
                "aEeljluRZ83L45Ejs+FVisD1+aMyMuH89r555c/P39oeXOLcsd" +
                "R56emD/8d56enn13P+bPvZzvOr7OfXP6zvPN++j67v+dn+Pi/+" +
                "+9x+ZbLMRZrtHovDBz2zeWXtB2Y2wINZ5HnMOasI5myzNsQQeB" +
                "pVRNSLla7773P2x23/+xz9Y7LMRZrtHovDBz2zeWXtB2Y2wINZ" +
                "5HnMOasI5myzNsQQeBpVRNSLlbpufMdkmYs02z2zP82PXNczm1" +
                "fWfo/kAzyYRZ43vrN3zGixomOpzdoQQ+BpVBFRL1Zq3+/rHqO/" +
                "TZa5SLPdY3H4oGc2r6z9wDyfB7PI85hzVhHM2WZtiCHwNKqIqB" +
                "crLfd9NPvrZpyXRi9W/T4avVjo/ejf63k/mh218+fNeT+a7t32" +
                "Z+fk5eSlSZvdZ3b0IFczYoxxsR41Y6bG6oqxamSlGYquLIaxwZ" +
                "lZ1kyz/jm/vt+//f1NeN/85vlV3u8nnjU9P9t+XmY/E6Y/tzPk" +
                "ys/PJ5MnJm12n9nRg1zO5vXuxzqsR01F4Rjz4jqwGSfjpRXBKr" +
                "KJ2MweLGumkYnm98f9/L2tSJtLxCzXXSIHF2Kco2uA6PugmXXM" +
                "NWalVYGa8eLa2kXWjSKAQd1T3r92NT4aVydK85kfEj6Nxbgjsp" +
                "6+IR95naEsRvI85qZZylBrK752cR6Des2wz6r3B/3BfF8PfD7Z" +
                "6QO34eEcXIhxjq4B4tm/pGTWMdeYlVYFasaLa2sXWTeKAAZ1T3" +
                "n/2lX7PaT9f8d1vG9e7ry0vVkuWPaRX/s3LQuSI7Wu62v8i4bn" +
                "e262BqjOC8xihH0ZfoxjF7hbq+HXot1Mv2h37aqjv9vfzX3mh4" +
                "TPdazUOPtrdI45jiLF1S79GupAGQyhahfM5Px9usjH+xPOAb/W" +
                "2ps3Xhf39n101b+HtP1s+3lT9nP8aPzIpM9uFa3oLjkHGS45R9" +
                "eoV9EYFzHXkFFX9YyMl9bmLrJu6u6ZZWQamdDz2mL74/35vI/Z" +
                "raIV3SXnxAx8ot911xSNcRFzDRmxKlAzXlxbu8i6qbtnlpFp3b" +
                "92df33+/jB673fh+qv6fn5H19mQuY=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value21 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value21[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value22 = null;

    protected static void value22Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1327;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj2PG1UUtbQrQIIiPwAJJEBCsraBFChdvOOtIFsAUhqalW" +
                "jcbLU/YA3rSOxvoKFdfkt+AFWUIh0SIBqEI/D47fU559472dia" +
                "GJN9M8r7uPe8c895Go/HsxkM4nF+NqjHxsf0YDqcvrvo7y5nH6" +
                "6/n9MPOuKfhcjHPep+74b8J2vyfRQin67JcK/uZ9/7OX46flra" +
                "0rfxMrOxtcDgRI4xugaMVleRMWcjVqVVwZrp4trqInOjDFAQPe" +
                "X+1dWg3j/r/bPeP2/Nfjb3muu29G28zGxsLTA4kWOMrgGj1VVk" +
                "zNmIVWlVsGa6uLa6yNwoAxRET7l/5+qkOVn0J9YvMyc2R4QxOJ" +
                "FjjK4B46qyIGPORqxKq4I108W11UXmRhmgIHrK/TtXk2ay6CfW" +
                "LzMTmyPCGJzIMUbXgHFVWZAxZyNWpVXBmuni2uoic6MMUBA95f" +
                "6dq9PmdNGfWr/MnNocEcbgRI4xugaMq8qCjDkbsSqtCtZMF9dW" +
                "F5kbZYCC6Cn3r67Gi2Px3DS2fvkkNba5jwDLaF5vcazD+tWTmm" +
                "PhHDBaZzzmiMcogitClVfjuVk9VEalXonHx+PyoD5FbnqMH48f" +
                "l7b0FitzRBjj0frPWrBjrUU5pjlVhjo2U55Ml+W9RlajjpmB6/" +
                "M/vzKqRrXw++h5vc42PUZ3RndKW3qLlbmPAMtoXm9xrMN61FQW" +
                "zrEuroM582S6tCJUeTWem9VDZVTqlXh8vT7r7/dX/3vz+x83/f" +
                "1e3y/9l9fn7JfbcX0+erid90sXX92O/bz4sr6v2433n//X++fF" +
                "gxvyn9f75+tw/6zXZ5/Ho8Hst/b6nP06vTt7Xq7P2R/9cM/+CZ" +
                "E/+1M++/2G/N8bM/913c/XX3v//P55aUtvsTL3EWAZzestjnVY" +
                "j5rKwjnWxXUwZ55Ml1aEKq/Gc7N6qIxKvRKPr9/vfX6/H+0f7Z" +
                "e29G28zGxsLTA4kWOMrgGj1VVkzNmIVWlVsGa6uLa6yNwoAxRE" +
                "T7l/ddU8aBb39bYtfZspMxtbCwxO5Bija8BolRUZczZiVVoVrJ" +
                "kurq0uMjfKAAXRU+7fuTpujhf9sfXLzLHNEWEMTuQYo2vAuKos" +
                "yJizEavSqmDNdHFtdZG5UQYoiJ5y/+rqaO9ob3Gd7lm/vHL3bI" +
                "4IY3AixxhdA8bVJ0OQMWcjVqVVwZrp4trqInOjDFAQPeX+1VV6" +
                "X31mo8sv6vui9Y7DJ4dP8liJo0VMcz5vjDzuqmyYHMVMhmNt3o" +
                "FmmFX51cWLFMQ13TGvizJXnaxXL4P1sW6+ja+Cq34wW7pmf375" +
                "TIb1sW6+/hW+yqqbHvX9fN3PXTmag+bARhorc7SIaS7mPVN35b" +
                "KGUYy3uLV2vsgB681ZvQuvoEtt985x9WbYDK/nw1VmGStztIhp" +
                "LuY9U4fCodVhFOMtbq2dkUdV2Chn9S68gi61mX6PaIbTt5Pnzz" +
                "c3ej/xznX/xpbfh7+16/eAy6/rfbDu5w7v58O6B73u5zd1D27f" +
                "82f9e3H9e3G9Pnu5f35b73kb/948a85KW3qLlTkijMGJHGN0DR" +
                "hRk5ExB2VQpVXBmuni2uoic6MMUBA95f7VVX0fsoXP+6Tuwba+" +
                "31OG+v8ZXoPv9909RvPStn3bjuYWb0c/vF/iwI7munY0j4x53D" +
                "I36WAVOW40/+4Zs/mKxqVzHnmF05+8TlWgjKjnK9XPe7+f97qf" +
                "fd8/D/dL2/Zte7hvccsYRsfZnFfGODizAzpYRY5jzVlFKOc5j7" +
                "oUgk+zyoh6rtK/ep7kpw==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value22 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value22[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value23 = null;

    protected static void value23Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1394;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXD1vXFUQXblCgjoVUpAACQnREApEmuB9m18BCFGkcYUiIk" +
                "pYLBx5K/8AGkSBhJYqBRK/Axoqx4jaRZQqBfveZPacM3fe8+76" +
                "yVjm3hVz7505c+bMy/vaTcRkMpnsP5t0tp1ba3v3HL9hfvdhne" +
                "05s/R7JB/QwSpy3P6z7/5htljRuXTPq6hw/mPUqQqUEfVipXJ8" +
                "+9WkjkuM6Z7Zdm6t7d1jcfiwzvacWfrBOayDVeQ41pxVhHLe86" +
                "pPIfg0qoyoFyutztXnZtu5tbZ3z+GX5gfW19meM0u/R3qu47UO" +
                "VpHj9p8vvma2WNG5dM+rqDBc74UCZUS9WGn+3vzd+eur+YNu99" +
                "bw9f74uPTN38yx8w8LzzvjXVXz2xfE39+S723eHf6w8tzZkuGj" +
                "ev+8ivH9b/UY7DpmR7Mjsza7z/bRAyyjOd/9yEM+aioLx1gX18" +
                "GeeTJdWhGqoprIzeqhslQalSi+edg8nExaa3MbsZ2v3QKDD2KM" +
                "0Rww+nFQZBnzFavSqmDNdHFt7SLrRhmgoOwp71+72vZ5lN6Hb+" +
                "DzqPPs9Dyqx3Pc45ncU1/chCfDN7f/m7rbnZ+Lx/X8HDo/m2Wz" +
                "NGtzd2dd+h4exuCDGGM0B4zrO7cgy5ivWJVWBWumi2trF1k3yg" +
                "AFZU95/9pVfZ8ff+z/bLadW2t79jjGZl9ne81kfo7E2lEHq8j1" +
                "srayIrjKOkOdqM5MaVlP/dNH00dmffZdu2rXbhkDhFvGaI56lY" +
                "15EYMyZtequfaYwfrdE7spu2eVUWlUQr8/WexkerKaTzD7rl21" +
                "a7eMiQj8F/2+9pWyMS9ivgIiVgVrpotraxdZN2X3rDIqLfvXru" +
                "r7Z32fv87Hc3Y2OzNrc/c+f+Z7eBiDD2KM0Rwwrr8vCLKM+YpV" +
                "aVWwZrq4tnaRdaMMUFD2lPevXdXzs17v1/v3+Xo84/E8fDLu7y" +
                "H1+9HV/R5S75/1eq/Hsz6P6vPIjuf0wfSBWZ99167atVvGAOGW" +
                "MZqjXmVjXsR8BURZ1RGZLq3NXWTdlN2zyqg0KqHv712sudXcis" +
                "fZfOaHhU9jZZz9JTvHLKcP5X63/slRqlBra452MaSgzOn3efV6" +
                "vY/7PCrH9Ke+jDKSYaOvn2/XsQnj+FU3Gc1es5f7zA8Ln6+RqX" +
                "H2N3tDlZ1HmWK2W//0daAK+li1C1YyfJwu8lnNxS8l7ujTy/wJ" +
                "HX3yf/52NLs7u2vWZvfZPnqAZTTnux95yEdNZeEY6+I62DNPpk" +
                "srQlVUE7lZPVSWSqMSxc9fLY/x8Re+Wvy6xd34tZfZn1/x9+VX" +
                "rtX5eW92z6zN7rN99ADLaM53P/KQj5rKwjHWxXWwZ55Ml1aEqq" +
                "gmcrN6qCyVRiURn5yfn9VfiXa+Wgav962Y6vXenqX3Z/fN2uw+" +
                "20cPsIzmfPcjD/moqSwcY11cB3vmyXRpRaiKaiI3q4fKUmlUov" +
                "jmvDlfvTed+9y9SZ37Hh7G4IMYYzQHjOs3NUGWMV+xKq0K1kwX" +
                "19Yusm6UAQrKnvL+tavsnF08qffBMUf9+44xxsd/u7VVFi3Xw2" +
                "zbRRS1WRWgVPnF9Yf5++ojezOe7hvjX/Xsqtd7PZ439I2+/p5s" +
                "+HH+/fxBc2DW5u5N6sD38DAGH8QYozlgXL+pCbKM+YpVaVWwZr" +
                "q4tnaRdaMMUFD2lPevXU2fTp8Wv2x3PvPDwqexGHdGXmejjTkm" +
                "RzGT41ibolSh1lZ+7WJIQZnT73vZy+n0tMB1PvPDwqexGHdGXq" +
                "cKT71OH4qZHMfaFKUKtbbyaxdDCsqcfl/URZFlL+tyE2z09fPt" +
                "OjZhHL/qrmPxR31Kj3o8/6zHoL4vXY/3pWwc/17PsjFH/f9dXG" +
                "L8C2Iv1SY=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value23 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value23[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value24 = null;

    protected static void value24Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1473;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE1rXVUUvYPGjipkLii0glCcaAfiLO/m/QR/gDhIM5TiD/" +
                "AZnsH4F5w4dVTIwLl0lN/QUUDsoPiBUkSL3pOd/dZa++z7kve8" +
                "pQXPCdlnf6699uHc3vT1o+sW7y7uLt7ousW9bliLO133+WfdRm" +
                "txe8T/QeV5p5tsLd68Iv7ehnhvs3X0zeB5f0OED23vb5kse5Fm" +
                "u+ert8yPXNczmytrv0fyBR7MIs/rb33xI6PFjo6lNmuR4eLbyF" +
                "MZKCL6xU5dN3tusuxFmu0ei8MHPbO5svYDM1vgwSzyPOacdQRz" +
                "tlkbYwg8jSoi+sVOw9numCx7kWa7Z7ifO55ju+uZzZW13yMj92" +
                "7Fg1nkef3Oxf0MXFHjWGqzFhmG+1kxUET0i52Gs/3DZNmLNNs9" +
                "FocPemZzZe0HZnonVjyYRZ7HnLOOYM42a2MMgadRRUS/2Gk425" +
                "smy16k2e45+tT8yHU9s7my9ntk5N6teDCLPK+/efKU0WJHx1Kb" +
                "tcgw3M+KgSKiX+zU3u9Tvt9nZ7Mzk7Zf3N0zt+HhHHwhxjlaA8" +
                "TVkyOZdcw1ZqVdgZrx4t46RTaNIoBBPVM+v0616f08+fnVuJ+z" +
                "R9vez9mj69zP42fb//wZ16bPe1tyqu3Xz+R+Xni2up/tPKc9z3" +
                "otv29P7bZrfjQ/Mmm7+8yOHuRyNte7H3WoR09F4Rjz4j6wGSfj" +
                "pR3BKrKJ2MweLGumkUnM3+x5P/7k//G8H3+8/fM++85k2Ys0mz" +
                "2eY7vrma2VjM8RrVfdGMS8WMPc6o7Aqvusm0R5ZkzrfrFTex+1" +
                "93s7z3ae7TzbebbzbOfZPv9s57nZeR6dbn8/+/v9fZO+u1W0or" +
                "vkHGS45BytUa+iMS5iriGj7uoZGS/tzVNk09TTM8vINDLB4tr2" +
                "+Wf7/LO9j9r7qL2PNn4fPegfmPTdraIV3SXnIMMl52iNehWNcR" +
                "FzDRl1V8/IeGlvniKbpp6eWUamkQm9jyx20B8M+wF2t4pWdJec" +
                "gwyXnKM16lU0xkXMNWTUXT0j46W9eYpsmnp6ZhmZRiZ0nhZ73D" +
                "+O99Z85oeET2Mx7oisZ6vEPCfPYiTPY26apQy1t+LrFOsY1DXj" +
                "Puu+f2P/Rswzn/kh4XMdlRpnf43OMcdRpFjt0r8ytswi58cVcV" +
                "/HM0MYQx1wDvcPh/3Q94vIodvwcA6+EOMcrQHiqrNk1jHXmJV2" +
                "BWrGi3vrFNk0igAG9Uz5/DrVfG++13VF2l4iZrnOHuRyNte7H3" +
                "Wo984RhWPI0T6wGSfjpR3BKrKJ2MweLGumkYnmz3Znu11XpO0X" +
                "fxay63b0IJezud79qEP96k9iAgrHkKN9YDNOxks7glVkE7GZPV" +
                "jWTCOTmN9+/pz253lfez+5NE0X+7L4uvzrRjTrel2Qpcyv7r8e" +
                "f6w/qq/C2Tt3aVoWrfVRPuebRzTrel2Qpcyv7r8ef6w/qnOcL7" +
                "vlL+V5Xz5d3Fs+t+d9+ds0T+Tyn8rz+3TP+/LXK+J/bY385+X+" +
                "9+a1syezJyZtd5/Z8HAOvhDjHK0BInpyZh0DM7DSrkDNeHFvnS" +
                "KbRhHAoJ4pn1+nap9/vvjVznPatffDy0d42dNtO0F/3p/nPvND" +
                "wqexGHdE1sc6e06exUiex9ziBBphVMXXKdYxqGvGfZEX1slH7R" +
                "mdcrXznHZ9fbudwaTneaedwZSr/XuE7df84fyhybLD5zak+xDl" +
                "GL5dAr3GZ5/GlBn6uBV51rw8HjkyG65SBO7P3yojE0I9nZ8O+6" +
                "ntl5FT2JDuQ5Rj+HbpSECEl30aI37Sx63Is+bl8ciR2XCVInB/" +
                "/lYZmXB++/zzMr/9/ZAXeJ7/6f9jed1k2Ys02z0Whw96ZnNl7Q" +
                "dm+nuQFQ9mkecx56wjmLPN2hhD4GlUEdEvdmr3sz3vr/J5Dnf1" +
                "NZNlL9Js91gcPuiZzZW1H5jpM7biwSzyPOacdQRztlkbYwg8jS" +
                "oi+oVO/wL8NPKn");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value24 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value24[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value25 = null;

    protected static void value25Init()
    {
        try
        {
            final int rows = 58;
            final int cols = 84;
            final int compressedBytes = 1699;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWsuKHVUUrWGLfoKQgAqCOFEHIukMbqVmIgiZGkQcJYFMOi" +
                "SBDOJtDRJCAslYBH8hH9L9Ff5CHGRgnXt69Vpr793GTi6mJVWh" +
                "9tmPddZe+9D3nWFYf7r+ZP3+MKy/GOZr/eEw/HRzONW1/uCE/J" +
                "cp8/GwtWt97iX1z07J95FGP/82Zz4/JcNXwzDdn+5329eW7xF8" +
                "zRCraN2PPPdxP/pGFq0R430YK0+lyztSVVQTuVU9VWalUUnCv0" +
                "izvGi5nqdlzmta12r2c5fIU+lwvOcqFLx7507u7diK9d9cvsfP" +
                "4GTc//W6d+7N9F2eP6vnz03mFZ4/x6vj1W6xImpe82EVQwSsYn" +
                "yPZ51NeVmDR0TuCkSly3vrFNU0eXpVGZVGJbyOajfH+a+xWayI" +
                "xs1fKa1iiIBVjO/xrLMpL2vwiMhdgah0eW+dopomT68qo9KoRM" +
                "6z126MN+b1BldEzWs+rGKIgFWM7/Gssykva/CIyF2BqHR5b52i" +
                "miZPryqj0qhEzrPXHo+P5/UxV0TNaz6sYiKCd8zDh+dsyssaPC" +
                "JiV7JWurS3T1FNk6dXlVFpnj9MtTfuzeseV0TNaz6sYoiAVYzv" +
                "8ayzKS9r8IjIXYGodHlvnaKaJk+vKqPSqETOs9euj9fn9TpXRM" +
                "1rPqxiiIBVjO/xrLMpL2vwiMhdgah0eW+dopomT68qo9KoRM6z" +
                "1x6Nj+b1EVdEzWs+rGIignfMw4fnbMrLGjwiYleyVrq0t09RTZ" +
                "OnV5VRaZ7fp1q/m99HPfgB3sNvT/Hu672j3d//x++fd87Sp4jp" +
                "wnSh274i1+OYIVbRuh957uN+9nQWraku7cNYeSpd3pGqoprIre" +
                "qpMiuNShw/rabVvK6wbiorxDFDrKJ1P/Lcx/3H5xBYtEaM92Gs" +
                "PJUu70hVUU3kVvVUmZVGJY6fDqaDeT3AuqkcIGZGMRHtNyyYyM" +
                "is5rwmJ2p9EDlPpQv1qFHV+MTKoP31jjuzamCXz+/b/P6zuk57" +
                "nm/m+uWbl9S/PitKH343LNc2z/PKcgbbvJbH+2u8/3w2Peu2rc" +
                "whpkWOVa3xhiV75tec11wZ+yCKOrMu1KNGVaO7nEH76+02KlF8" +
                "8Xj/cXmMvn2P97N7rZ5329Zme4zMg/M9Tyz8KtadOY/Ky3Soih" +
                "q3er7/p7LFjuDyWL2ocP1H1OkKnJH9Yqfl/fw2f49bznO75znt" +
                "T/vd9nXzSrWPOGaIVbTuR577uP/4lTCwaI0Y78NYeSpd3pGqop" +
                "rIreqpMiuNShx/6fKly8PQbF9bpUfwYYnhP9YU43vIiHNwZK7B" +
                "U1XelayVLu3tU1TTOAMV5Jnq+X2q5fG+PH+e5fMcb423usWKqH" +
                "nNh1UMEbCK8T2edTblZQ0eEbkrEJUu761TVNPk6VVlVBqV8Dqq" +
                "XRuvzes1roia13xYxRABqxjf41lnU17W4BGRuwJR6fLeOkU1TZ" +
                "5eVUalUYmcZ6/dHe/O612uiJrXfFjFEAGrGN/jWWdTXtbgEZG7" +
                "AlHp8t46RTVNnl5VRqVRiZxnrz0dn87rU66Imtd8WMVEBO+Yhw" +
                "/P2ZSXNXhExK5krXRpb5+imiZPryqj0jy/T7W8Hm359ejOeKdb" +
                "rIia13xYxRABqxjf41lnU17W4BGRuwJR6fLeOkU1TZ5eVUalUY" +
                "n8ffbak/HJvD7hiqh5zYdVTETwjnn48JxNeVmDR0TsStZKl/b2" +
                "Kapp8vSqMirN8/tUy+N9y5/fd6fdbvu6+SS6izhmiFW07kee+7" +
                "j/+HN3YNEaMd6HsfJUurwjVUU1kVvVU2VWGpU4fjqcDuf1EOum" +
                "coiYGcVEtN+wYCIjs5rzmpyo9UHkPJUu1KNGVeMTK4P21zvuzK" +
                "rZLf3ecX/5jn35vePsXg9/Xc7gNV4nl9f35fu65TzfmvOc39Xv" +
                "dNvWZscd5Jv34HzPEzvK/7COse4ci/+JTfaqBoSqqHHjTvu9OG" +
                "rlHnB5rF5U6L8XZwXOyH6x0zCs/uq2rc32GJleZ45+FevOnCdn" +
                "dVGHqqhxqrnqSOUaq3eSQvJ51RnZz/Pj7fF2t1gRNa/5sIohAl" +
                "Yxvsezzqa8rFGZsnvXWnvcofqRidPk6VVlVBqVyN/vEX55/lxe" +
                "j5bzXM5zOc9X+r7u4nSx275uvim5iDhmiFW07kee+7j/+JuYwK" +
                "I1YrwPY+WpdHlHqopqIreqp8qsNCqJ+OLz++/Lp/DXucZ3um1r" +
                "sz1GpteZo1/FujPnyfnPOlRFjVPNVUcq11i9kxSSz6vOyH6x0/" +
                "L8udXXo78Borlk1A==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value25 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value25[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value26 = null;

    protected static void value26Init()
    {
        try
        {
            final int rows = 16;
            final int cols = 84;
            final int compressedBytes = 725;
            final int uncompressedBytes = 5377;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtVMuKGzEQ1IcEckgCgZBLkkPIVcznrFkfcrHxQuaf7LX/Lp" +
                "Lloqq6FYLBxx1Qv6rUXT2MXcr6df2yvitl/VHas34s5c/vctez" +
                "fvhH/WeqfC4Pe9b3/8G/3dnvU6p8v7PDr6t9e58PfJ/1uT4PC4" +
                "+sRz2GVQ4ZsMrxO171btqXGCIy8lQwZrp8tm4x2yZvryqj0qiE" +
                "D/hv3+dDv8+n+jQsPLIe9RhWOWTAKsfveNW7aV9iiMjIU8GY6f" +
                "LZusVsm7y9qoxKoxL5Pq/Y8rq8ltJt9+MZ2chpUSOqGA8sOrEj" +
                "q1pzjI/PQRZ1Zl3Ao0ZVo7e8g87X4zYqka7H5dj8cfgbcmROix" +
                "pRxXhg0YkdWdWaY6LP5iCLOrMu4FGjqtFb3kHn63EblZBfD/XQ" +
                "vtMDPbIe9RhWOWTAKsfveNW7aV9iiMjIU8GY6fLZusVsm7y9qo" +
                "xKoxL5vQ9sW7fNb+mR9ajHsMohA1Y5fser3k37EkNERp4KxkyX" +
                "z9YtZtvk7VVlVBqVyPsc2K7umt/RI+tRj2GVQwascvyOV72b9i" +
                "WGiIw8FYyZLp+tW8y2yduryqg0KpH3ObBN3TS/oUfWox7DKocM" +
                "WOX4Ha96N+1LDBEZeSoYM10+W7eYbZO3V5VRaVQi7/OKLZfl0v" +
                "5HL8Pf/lkvzGlRI6oYDyw6sSOrWnNM/t9tDrKoM+sCHjWqGr3l" +
                "HXS+HrdRiXQ9LafmT8PfkBNzWtSIKsYDi07syKrWHBN9NgdZ1J" +
                "l1AY8aVY3e8g46X4/bqES6npdz8+fhb8iZOS1qRBXjgUUndmRV" +
                "a46JPpuDLOrMuoBHjapGb3kHna/HbVRCfn2pL+13/0KPrEc9hl" +
                "UOGbDK8Tte9W7alxgiMvJUMGa6fLZuMdsmb68qo9KoRP4/B7av" +
                "++b39Mh61GNY5ZABqxy/41Xvpn2JISIjTwVjpstn6xazbfL2qj" +
                "IqjUrkfQ7+X6NcGrs=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value26 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value26[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int lookupValue(int row, int col)
    {
        if (row <= 57)
            return value[row][col];
        else if (row >= 58 && row <= 115)
            return value1[row-58][col];
        else if (row >= 116 && row <= 173)
            return value2[row-116][col];
        else if (row >= 174 && row <= 231)
            return value3[row-174][col];
        else if (row >= 232 && row <= 289)
            return value4[row-232][col];
        else if (row >= 290 && row <= 347)
            return value5[row-290][col];
        else if (row >= 348 && row <= 405)
            return value6[row-348][col];
        else if (row >= 406 && row <= 463)
            return value7[row-406][col];
        else if (row >= 464 && row <= 521)
            return value8[row-464][col];
        else if (row >= 522 && row <= 579)
            return value9[row-522][col];
        else if (row >= 580 && row <= 637)
            return value10[row-580][col];
        else if (row >= 638 && row <= 695)
            return value11[row-638][col];
        else if (row >= 696 && row <= 753)
            return value12[row-696][col];
        else if (row >= 754 && row <= 811)
            return value13[row-754][col];
        else if (row >= 812 && row <= 869)
            return value14[row-812][col];
        else if (row >= 870 && row <= 927)
            return value15[row-870][col];
        else if (row >= 928 && row <= 985)
            return value16[row-928][col];
        else if (row >= 986 && row <= 1043)
            return value17[row-986][col];
        else if (row >= 1044 && row <= 1101)
            return value18[row-1044][col];
        else if (row >= 1102 && row <= 1159)
            return value19[row-1102][col];
        else if (row >= 1160 && row <= 1217)
            return value20[row-1160][col];
        else if (row >= 1218 && row <= 1275)
            return value21[row-1218][col];
        else if (row >= 1276 && row <= 1333)
            return value22[row-1276][col];
        else if (row >= 1334 && row <= 1391)
            return value23[row-1334][col];
        else if (row >= 1392 && row <= 1449)
            return value24[row-1392][col];
        else if (row >= 1450 && row <= 1507)
            return value25[row-1450][col];
        else if (row >= 1508)
            return value26[row-1508][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value26 lookup");
    }

    static
    {
        sigmapInit();
        sigmap1Init();
        sigmap2Init();
        valueInit();
        value1Init();
        value2Init();
        value3Init();
        value4Init();
        value5Init();
        value6Init();
        value7Init();
        value8Init();
        value9Init();
        value10Init();
        value11Init();
        value12Init();
        value13Init();
        value14Init();
        value15Init();
        value16Init();
        value17Init();
        value18Init();
        value19Init();
        value20Init();
        value21Init();
        value22Init();
        value23Init();
        value24Init();
        value25Init();
        value26Init();
    }
    }

    /**
     * The GOTO table.
     * <p>
     * The GOTO table maps a state and a nonterminal to a new state.
     * It is used when the parser reduces.  Suppose, for example, the parser
     * is reducing by the production <code>A ::= B C D</code>.  Then it
     * will pop three symbols from the <code>stateStack</code> and three symbols
     * from the <code>valueStack</code>.  It will look at the value now on top
     * of the state stack (call it <i>n</i>), and look up the entry for
     * <i>n</i> and <code>A</code> in the GOTO table to determine what state
     * it should transition to.
     */
    protected static final class GoToTable
    {
        /**
         * Returns the state the parser should transition to if the given
         * state is on top of the <code>stateStack</code> after popping
         * symbols corresponding to the right-hand side of the given production.
         *
         * @return the state to transition to (0 <= result < Parser.NUM_STATES)
         */
        protected static int getGoTo(int state, Nonterminal nonterminal)
        {
            assert 0 <= state && state < Parser.NUM_STATES;
            assert nonterminal != null;

            return get(state, nonterminal.getIndex());
        }

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 35, 0, 4, 0, 36, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 6, 0, 0, 0, 38, 39, 7, 0, 40, 8, 0, 0, 0, 41, 42, 0, 43, 0, 0, 44, 0, 9, 0, 45, 0, 10, 46, 11, 0, 47, 0, 0, 0, 48, 49, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 11, 0, 0, 0, 0, 0, 0, 51, 1, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 14, 15, 0, 0, 0, 52, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 0, 1, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 53, 0, 0, 0, 20, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 55, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 56, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 57, 0, 58, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 59, 0, 23, 0, 9, 0, 0, 1, 10, 0, 0, 0, 60, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 11, 0, 2, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 61, 14, 0, 62, 0, 0, 0, 0, 63, 0, 0, 64, 65, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 14, 0, 0, 66, 15, 0, 0, 16, 0, 0, 67, 17, 0, 0, 0, 0, 0, 24, 25, 1, 0, 26, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 28, 1, 0, 0, 0, 0, 3, 4, 0, 0, 0, 29, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 2, 32, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 34, 0, 0, 0, 5, 4, 0, 0, 35, 0, 36, 37, 0, 0, 0, 0, 0, 0, 0, 38, 3, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 39, 0, 16, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 1, 6, 0, 0, 5, 43, 0, 7, 1, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 68, 45, 0, 46, 47, 0, 48, 49, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 54, 0, 1, 0, 55, 0, 0, 8, 56, 0, 57, 0, 58, 0, 0, 0, 6, 7, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 59, 60, 9, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 8, 61, 62, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 63, 0, 0, 0, 69, 0, 0, 0, 0, 64, 0, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 66, 67, 17, 18, 0, 19, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 68, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 69, 25, 26, 0, 70, 71, 0, 0, 0, 0, 0, 4, 0, 72, 0, 0, 0, 5, 70, 73, 1, 0, 0, 0, 27, 74, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 1, 0, 71, 0, 0, 0, 0, 0, 0, 72, 0, 0, 6, 0, 0, 11, 0, 0, 0, 0, 19, 0, 0, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 11, 0, 75, 76, 12, 0, 73, 77, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 5, 0, 0, 0, 0, 0, 78, 0, 13, 79, 80, 81, 82, 0, 83, 74, 84, 1, 85, 0, 75, 86, 87, 76, 88, 14, 2, 15, 0, 0, 0, 89, 90, 0, 0, 0, 0, 91, 0, 92, 0, 93, 94, 0, 95, 96, 9, 0, 0, 2, 0, 97, 0, 0, 98, 1, 0, 99, 3, 0, 0, 0, 0, 0, 100, 2, 0, 0, 0, 0, 0, 0, 101, 102, 0, 0, 0, 0, 0, 0, 0, 0, 103, 104, 0, 3, 4, 0, 0, 0, 105, 1, 106, 0, 0, 0, 107, 108, 0, 0, 10, 0, 1, 0, 0, 0, 4, 109, 5, 0, 1, 110, 111, 0, 0, 4, 112, 0, 6, 113, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 114, 0, 0, 0, 0, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 6, 0, 16, 0, 17, 115, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 116, 7, 0, 117, 118, 0, 11, 0, 0, 0, 12, 0, 119, 0, 0, 0, 0, 20, 0, 2, 0, 0, 7, 0, 0, 0, 4, 0, 120, 121, 0, 5, 0, 0, 0, 0, 0, 122, 0, 0, 0, 123, 124, 125, 0, 8, 0, 126, 0, 9, 13, 0, 0, 2, 0, 127, 0, 2, 3, 128, 0, 0, 14, 129, 0, 0, 0, 15, 10, 0, 0, 0, 0, 77, 0, 1, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 130, 131, 0, 132, 133, 134, 135, 0, 0, 0, 0, 136, 0, 0, 23, 24, 25, 26, 27, 28, 29, 137, 30, 78, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 138, 46, 47, 48, 49, 139, 50, 51, 52, 55, 56, 57, 0, 0, 1, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 140, 141, 142, 0, 143, 0, 59, 4, 79, 0, 144, 7, 0, 0, 145, 146, 0, 0, 11, 60, 147, 148, 149, 150, 80, 151, 0, 152, 153, 154, 155, 156, 157, 158, 61, 159, 0, 160, 161, 162, 163, 0, 0, 7, 0, 0, 0, 0, 62, 0, 0, 0, 0, 164, 0, 165, 0, 0, 0, 0, 1, 0, 2, 166, 167, 0, 0, 168, 0, 169, 12, 0, 0, 0, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 171, 172, 0, 173, 174, 0, 8, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 175, 176, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 82, 0, 13, 0, 0, 0, 177, 2, 0, 3, 0, 0, 0, 14, 0, 178, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 179, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 180, 181, 19, 0, 0, 0, 0, 0, 4, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 182, 0, 183, 184, 185, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 186, 0, 187, 188, 0, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 189, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 9, 10, 0, 11, 0, 12, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 190, 0, 0, 191, 0, 0, 0, 192, 22, 0, 0, 0, 0, 23, 193, 24, 18, 0, 0, 0, 0, 0, 0, 194, 0, 0, 1, 0, 0, 19, 195, 0, 3, 0, 7, 15, 0, 1, 0, 0, 0, 1, 0, 196, 25, 0, 63, 0, 0, 197, 0, 198, 0, 199, 0, 200, 20, 0, 0, 201, 0, 0, 21, 0, 0, 0, 83, 0, 26, 0, 202, 0, 0, 0, 0, 0, 203, 22, 0, 0, 0, 0, 18, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 204, 0, 0, 0, 0, 0, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 0, 0, 0, 1, 205, 206, 0, 0, 0, 0, 0, 0, 207, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 208, 0, 0, 0, 209, 64, 0, 210, 0, 2, 3, 0, 3, 0, 0, 65, 86, 0, 0, 24, 0, 0, 0, 27, 211, 0, 212, 25, 28, 0, 213, 214, 0, 26, 215, 0, 0, 216, 217, 218, 219, 29, 220, 27, 221, 222, 223, 28, 224, 0, 225, 226, 6, 227, 228, 30, 0, 229, 230, 0, 0, 0, 0, 0, 66, 0, 2, 231, 0, 0, 0, 232, 0, 233, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 234, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 235, 0, 23, 24, 30, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 236, 4, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 237, 238, 1, 0, 1, 32, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 239, 69, 0, 0, 240, 0, 0, 241, 242, 0, 0, 0, 0, 33, 34, 0, 0, 3, 0, 0, 243, 0, 244, 0, 87, 245, 0, 246, 0, 0, 35, 0, 0, 0, 247, 0, 248, 36, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 38, 0, 0, 0, 0, 0, 20, 0, 249, 0, 250, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 7, 0, 0, 0, 0, 40, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 251, 37, 252, 253, 38, 254, 0, 255, 39, 256, 0, 41, 0, 257, 0, 40, 258, 41, 0, 259, 0, 260, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 261, 262, 0, 0, 263, 0, 8, 0, 0, 43, 0, 264, 265, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 267, 268, 269, 270, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 271, 0, 0, 272, 44, 10, 0, 0, 12, 0, 13, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 70, 0, 0, 273, 0, 0, 0, 274, 0, 0, 0, 0, 43, 45, 0, 0, 275, 276, 277, 0, 46, 278, 0, 279, 47, 48, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 0, 8, 49, 283, 284, 50, 285, 0, 0, 51, 0, 3, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 0, 52, 0, 289, 290, 0, 0, 53, 0, 0, 291, 0, 0, 0, 292, 0, 0, 293, 1, 0, 0, 0, 5, 2, 0, 294, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, 295, 45, 0, 0, 0, 0, 0, 71, 0, 0, 54, 0, 0, 0, 0, 0, 0, 0, 0, 296, 0, 0, 0, 0, 2, 0, 297, 14, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 298, 46, 0, 0, 0, 299, 0, 0, 0, 0, 0, 300, 0, 0, 0, 0, 0, 0, 55, 0, 0, 56, 0, 301, 0, 0, 0, 0, 0, 0, 57, 0, 0, 36, 0, 0, 0, 37, 5, 302, 6, 303, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 2, 0, 304, 305, 3, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 306, 0, 307, 0, 308, 0, 0, 309, 0, 0, 0, 310, 0, 0, 58, 311, 0, 0, 0, 0, 0, 312, 0, 0, 7, 313, 0, 0, 0, 314, 315, 0, 47, 316, 0, 0, 0, 59, 88, 0, 0, 0, 317, 318, 60, 0, 61, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 89, 0, 0, 0, 2, 48, 62, 0, 0, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 319, 0, 49, 320, 50, 0, 72, 0, 51, 0, 0, 0, 0, 321, 322, 64, 0, 0, 323, 65, 66, 0, 52, 0, 324, 67, 325, 0, 68, 53, 326, 327, 69, 70, 0, 54, 0, 328, 329, 0, 55, 71, 330, 0, 56, 0, 0, 0, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 331, 0, 9, 332, 0, 0, 333, 334, 335, 73, 0, 0, 0, 336, 0, 0, 0, 337, 338, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 57, 0, 0, 58, 59, 339, 74, 0, 0, 0, 0, 75, 0, 0, 38, 0, 0, 0, 0, 0, 340, 60, 341, 61, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 342, 343, 0, 344, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 345, 0, 0, 0, 0, 0, 346, 0, 62, 347, 63, 0, 64, 348, 349, 0, 0, 65, 350, 0, 66, 0, 0, 76, 0, 0, 351, 352, 0, 0, 77, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 353, 354, 90, 0, 355, 0, 0, 0, 356, 0, 0, 0, 78, 0, 0, 0, 0, 0, 0, 67, 0, 79, 0, 357, 0, 80, 68, 358, 0, 359, 360, 361, 81, 82, 0, 362, 69, 83, 363, 364, 365, 366, 0, 84, 0, 0, 0, 0, 367, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 0, 0, 0, 0, 0, 70, 368, 0, 71, 0, 0, 0, 85, 0, 4, 5, 0, 0, 6, 0, 0, 3, 0, 0, 0, 369, 0, 370, 86, 371, 0, 0, 0, 0, 0, 72, 73, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 372, 1, 0, 0, 0, 6, 0, 0, 0, 0, 0, 87, 74, 75, 373, 76, 0, 88, 89, 77, 0, 78, 374, 0, 375, 376, 0, 0, 377, 378, 0, 0, 0, 7, 0, 91, 90, 0, 0, 379, 0, 380, 0, 381, 382, 383, 0, 91, 384, 385, 386, 387, 92, 93, 0, 0, 0, 388, 0, 389, 390, 391, 0, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 79, 0, 80, 392, 0, 0, 0, 0, 0, 7, 0, 16, 0, 0, 0, 0, 393, 0, 394, 0, 0, 96, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 97, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 395, 396, 0, 0, 397, 398, 0, 399, 0, 0, 0, 0, 98, 99, 0, 0, 0, 92, 93, 0, 100, 0, 101, 102, 400, 0, 103, 104, 0, 0, 0, 0, 81, 0, 0, 105, 0, 0, 0, 0, 82, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 401, 0, 402, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 403, 0, 0, 0, 0, 0, 0, 0, 0, 404, 106, 0, 83, 107, 108, 0, 84, 405, 406, 0, 0, 0, 407, 0, 408, 0, 109, 0, 0, 85, 0, 409, 0, 0, 86, 0, 410, 0, 0, 0, 0, 0, 0, 0, 0, 87, 8, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 411, 0, 0, 0, 412, 0, 88, 413, 0, 414, 0, 89, 0, 110, 111, 112, 113, 0, 415, 0, 114, 416, 417, 0, 115, 418, 0, 0, 0, 90, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 116, 117, 118, 0, 419, 0, 420, 0, 0, 119, 421, 0, 120, 121, 422, 0, 122, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 123, 124, 0, 125, 0, 0, 126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 3, 0, 4, 5, 2, 6, 3, 2, 0, 3, 3, 7, 8, 1, 9, 1, 2, 0, 4, 10, 1, 6, 9, 6, 1, 0, 7, 9, 11, 5, 12, 1, 13, 3, 1, 1, 3, 7, 0, 14, 15, 16, 12, 9, 17, 18, 3, 2, 16, 19, 3, 6, 16, 20, 4, 9, 7, 21, 22, 23, 24, 1, 0, 25, 26, 2, 27, 28, 1, 29, 30, 0, 3, 31, 16, 2, 32, 0, 17, 33, 34, 12, 1, 0, 8, 35, 36, 16, 1, 37, 38, 4, 1, 39, 1, 5, 6, 40, 41, 6, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 1, 48, 49, 3, 3, 50, 9, 51, 52, 53, 54, 1, 1, 3, 1, 55, 56, 7, 4, 5, 1, 57, 0, 58, 59, 10, 8, 60, 61, 62, 63, 2, 18, 15, 64, 65, 66, 17, 67, 20, 68, 2, 69, 4, 70, 0, 71, 72, 73, 0, 0, 1, 20, 74, 2, 75, 76, 77, 21, 5, 78, 18, 79, 80, 81, 3, 82, 83, 10, 6, 11, 2, 84, 3, 85, 86, 5, 87, 1, 88, 1, 89, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 1, 11, 99, 12, 2, 100, 101, 102, 103, 17, 104, 105, 106, 0, 107, 108, 4, 109, 0, 110, 25, 8, 9, 3, 27, 111, 112, 9, 5, 113, 3, 3, 1, 114, 2, 12, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 9, 14, 0, 125, 11, 1, 1, 126, 127, 2, 29, 0, 4, 0, 128, 10, 2, 14, 129, 30, 130, 131, 132, 3, 13, 29, 1, 133, 11, 1, 134, 5, 21, 5, 1, 135, 21, 17, 7, 136, 137, 138, 23, 20, 12, 5, 17, 139, 1, 8, 140, 141, 22, 142, 4, 143, 144, 5, 145, 146, 147, 148, 149, 150, 31, 33, 151, 152, 9, 12, 153, 35, 24, 8, 154, 155, 4, 156, 6, 157, 158, 159, 160, 14, 161, 2, 162, 163, 164, 36, 18, 165, 166, 167, 40, 168, 2, 7, 8, 169, 170, 9, 39, 171, 172, 0, 173, 174, 175, 43, 22, 44, 176, 177, 3, 178, 56, 9, 10, 179, 180, 13, 45, 181, 182, 183, 184, 185, 186, 15, 4, 187, 188, 12, 3, 1, 20, 189, 190, 191, 10, 192, 193, 11, 1, 194, 195, 196, 18, 0, 2, 17, 197, 28, 17, 198, 2, 20, 23, 9, 5, 8, 25, 0, 199, 13, 200, 201, 0, 8, 11, 202, 203, 204, 6, 205, 206, 12, 207, 22, 208, 209, 2, 3, 210, 211, 212, 31, 17, 20, 8, 2, 1, 213, 9, 30, 10, 214, 215, 9, 216, 217, 52, 218, 20, 219, 220, 221, 2, 222, 223, 224, 11, 24, 47, 3, 21, 225, 18, 14, 226, 227, 6, 228, 229, 46, 230, 58, 231, 232, 233, 1, 12, 234, 235, 236, 237, 238, 3 };

    public static int get(int row, int col)
    {
        if (isErrorEntry(row, col))
            return -1;
        else if (columnmap[col] % 2 == 0)
            return lookupValue(rowmap[row], columnmap[col]/2) >>> 16;
        else
            return lookupValue(rowmap[row], columnmap[col]/2) & 0xFFFF;
    }

    protected static boolean isErrorEntry(int row, int col)
    {
        final int INT_BITS = 32;
        int sigmapRow = row;

        int sigmapCol = col / INT_BITS;
        int bitNumberFromLeft = col % INT_BITS;
        int sigmapMask = 0x1 << (INT_BITS - bitNumberFromLeft - 1);

        return (lookupSigmap(sigmapRow, sigmapCol) & sigmapMask) == 0;
    }

    protected static int[][] sigmap = null;

    protected static void sigmapInit()
    {
        try
        {
            final int rows = 609;
            final int cols = 16;
            final int compressedBytes = 1575;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXD2O3DYUfuRw18pkCq6xhe2KiQepUiyCHIAxtgiMFFOkSL" +
                "nApkvjI9CLBAiSFD7Cuk+RIwyQHCBH8BFc5AAhNRqNNCJFaSRK" +
                "lOZ9gMerXWokvZ/vPb5H6p68hV9fqAv9eflKPf9BPgHxjfzxS/" +
                "4PfK/g3X+fsw1b0E9X7PXfXz198/hcrIm4hiv1y58ffv/uL0DM" +
                "AbTym6X+x5xjpPlQAAJFN1Pwiv4LIPsfZqD/l9qOkwRA8x+j6h" +
                "mHSxBEfmGe7BbgRlL2L/kElismF4JyuII1F7f6v4s/7tQK+Q/u" +
                "Tfx4Bubzkpr4cWnix3qdxY8PlG2AUCM/HT82Jn5AFj/ufsP4oa" +
                "n0LQHFjUslmmGJlDt6ZXwLTIF6NF6oqHHG7VMuHxOxc853SsBS" +
                "Iv/25v/vtf8vc/9fa8k+av/n2v83hLDU/+92/g/rOfm/JAttWb" +
                "n9fSu1dckEVlep/W1T+zOxn9Hc/rS5ws9KKLS/OPhXOPj3VvPv" +
                "R8O/V3b+xfzdH78+luLXG5RfQP550Mx7xD/KxT/Kwj9JlrfuU1" +
                "h2mLmo7A/MHIlsmEiQvw5g3gEJCgmBQCAQs8r/6uvfsDD53yt3" +
                "/bsQGLk1oqYDyL6EKnbZyA30kX/8RN6DL/8nNfn//UUp/920OB" +
                "/rN4iQuPaO6DN/J6nzJgVnTv2XZ8cJL0800hNEJPyV+e/iXP2X" +
                "Zh+MZaRLqjMaGth+RkLev+DF/oWpX8Khfpn2L15m/QsI0L8oip" +
                "rSvbx1oDv0iipT9VKsRCD654Sm9ketAwbuf8viAW9Skenj+vb+" +
                "554/blL+wP4nIixY8PTOnz+UMLH1L5b465dfyn+SYP/5yBhow4" +
                "G95C9j95+L/L+05Y/rff6YX1+EuH71+cXx84t4++8j8xfibGH6" +
                "p6r9+g0Vbv3GYf7XJP+299+/ttYvXtjql+7+/e1+/dnVNOsfzB" +
                "toRPf436P9PVTtT41gf8Pa79EvbfX/8voDwPUHcYLUrB859rrd" +
                "sP70x2IQwHTWz2Dl8AQTYnHoCFcRRYe2/WvosX89I/4cBff1/f" +
                "8G+y/E6UKNIP/0rF9ouP9kvPuPJ/1x9L8ttF3qfydVCWY/XzjY" +
                "JEyBxHf/kfbvawxqkP7P2FmYZ/3CQPvHutTPP6vWz1m5fg5Z/X" +
                "xzqJ8rb/28/fyzw5R3LwYGly77m+n6iSb9D77X37aqv/r+6VDr" +
                "L8LkT1iemB3EIFegToaZ48TaD+ULmtZrrHLuJyxLWNKR5OhMbj" +
                "6TXLUCqMhTIk1m6QF50HfBloVFJjpIDenfss8awMk3PrAZyk42" +
                "xkj+Faw8kjnSgpPld77z54a6EhgPOzmWHOpGrPy6neykndSYI+" +
                "81agn3RGNEdXZC4jRV2bOSrq0XYxbBKj6UIbKYDHZiULE5/IRm" +
                "AUmA75DtNKd80pQBfI11eMD48i86vi2w1vKR3X17gvxFS/Lh5W" +
                "dRRX9nVll1PX9WVQxykgKEy8hq86fBowwJdT4LpifSMtWfA4RP" +
                "epMgqcYRRNoYRdY9eJfgJGMU1MT4t2H85gV+IHVKUNWD5DiHiN" +
                "rmWaAvZY7vl1HdKKIf22mtHInSOysE48CRF2V22389bP6AiNB+" +
                "x4N3/2S+/mNRs/4D11+chf0RVEFs8m+yf9Cx/zD0/kHmjdLCNv" +
                "RM1y+j/+Lzj4ua/Z/HXjvL/YMzth+J/tMg/6Un57+IVsDIjfFz" +
                "DP5C/WH8HI7WQu9/64dFE+fNDrv/ccpZcxP5QeTyO3X/7Hj2j6" +
                "gHLc/dIkm6phzKI4xf1v27YN+/yxvt3x32/ZFBrV/F//7uNvt3" +
                "z0p/E0F1/+Jdvn9xkSo4PfDsX+y7/qUG9rTS+i9VpEtVjPOsEJ" +
                "BwSneaL6LcEAgEwhV/oBx/6NH6YzEcjzZelsaqY8lhkmzLDijv" +
                "8/qInoMuOa+gzcqmJopCEA573OW/vb7/I9C6SHE28xfqkX8ikz" +
                "r5x4NRKzUqFikk/j/Gof/u/IjFjK78fdCmRf/Gpg3/Agzm/2S0" +
                "57fFL+PTpeeH0M8fU/ImWyiNNPBOGZ39u8Ut7HfOe7typ/wn2v" +
                "eniXBcj7BYkYNBpW1k03Izzh+nq3xMmRDTmJWN+/6A/wFyab0M");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap1 = null;

    protected static void sigmap1Init()
    {
        try
        {
            final int rows = 609;
            final int cols = 16;
            final int compressedBytes = 1139;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtyEzEQlYSSEqksRFUWLJUiB+AITVUWWXIEjiIoFiyy4C" +
                "gcgSNwJMaJY8/Ykkf/3/RbhHKwRlJ/Xr+WJ2NCPEBt/8EJAoHY" +
                "HqbMl7ufgqj9bxRhakYX8PKC/iBE8xsCh4HADyMQjXl0CXX4sQ" +
                "dDI6UEW9hcLsutnhufv72YhsDQNqFpLwc9pNzlPevX4NCG/dDd" +
                "BYTtskuW1fI8zvIDoo1TUGuKxPUPsP4hEAgHSDvRGcrDgXLq9d" +
                "8G/rtz1f/3zfQPPMpTd+RKzKv0wmNseqc0FEPgZ/bT2D/VB6AJ" +
                "EBilyftX9UazAvMMgcgqHRH1rSc8TxKCVz7jX+1wfpi/gugaXY" +
                "il/hDb/ocp93X930D/dqY/pOP+R/kcg9ktKS7Fk0Oe+o9HXYdA" +
                "oIRyoQTkikbAXUjfv/5Tp/oPy2hgzB4aNPUHw8klPyLShLpLVe" +
                "bePubiPOazPLiY//N3idXgbq//zO9UKD4/xcKJqAmKJrBXfR7A" +
                "yVsSlRwts6Kfn+qdn2H8IurFv7zQP3LX+C90sE8rj8cajhjM/1" +
                "39/RDmD8Y/Yrv1ExHC35CMv5uNn3r3jyPiIM+8sQIoFz8K7b9q" +
                "/09Tyogpe76TX5zpj5JcE0XhYWe6x+mywPhXSsnNLYd3iknygT" +
                "wQ9Tj9c/X8Td/+OeUvlU5/4u2YzvyL/If6Z0D7MbI4Qc1ECMJa" +
                "Qa5Wp8xQYBRGWWwtdBeVPRobyhnQY01qmbZr0BisUc1PpwoJ66" +
                "+nXRjavwFgTW6CAfmR8iDfAvzvv2OR4xvY/5pReJADKbHcKcl7" +
                "Sna6reMJVaIQrczPKs4fpMPgNR7YXN2+N93yq88aT0QaqhK2tF" +
                "VGumEy7fSh/J3m8/tY/k0wXniqJ0myOyCy/tSqfyLR/vlxfn46" +
                "Py2TlEXsF6+/avs/sf7TBflr5AaE2t7pwn8l6w/W78r+LyMgTP" +
                "VzGvnb/fmBsNAP922sP1j/uK8/lq9U1R5QZe9f5vYnJvuTvf2J" +
                "WX9WZQ6f+Alcfz7/+61fQMj6W/DT6PVrnj9k7r/9i50mW8QfaS" +
                "P+EMX8f4F/LvCv03jEhpVzgvrtxl8kmL/yKCBZbHz3/g/QP35P" +
                "sWk7fuqcH/TTv8WkpkucQFvu2thHLypd/zRA/wFp8tdgP+VsP9" +
                "6UflPLFxC6f/JyFn+y/y+2+LnOv3/hsQsDyp3/JXMjtgSdQJzG" +
                "J3jmaQlaPJm6zeAa9/lpjRfNlGBZ5/9brueTledHOCse2dVqm+" +
                "qUoNQmV+e39i/ilf/5v6P+/HlzfPD6005/B/aBLPVZQN6ziLPa" +
                "vfXHkuFj2epyGKvu/T7En+h8/a6gHveP6hMhpcFgq/vlWw2lQ+" +
                "2u+jkf5/qMjPr+zzLiYuBmqYH9t//940O2s7mEmthkigxTi4qr" +
                "9ajPX7I9f6cT+yFa6Dmr7jzb309dhOH+ZR08f7/83cPzZ+Pn6V" +
                "H/io7yLx4p5bAeLP5J9vjPff/zEP0G6u/C0kDOYq309z/1q8ga" +
                "aF/1WjcB40Tpf0vOoEo=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap1 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap1[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap2 = null;

    protected static void sigmap2Init()
    {
        try
        {
            final int rows = 609;
            final int cols = 16;
            final int compressedBytes = 972;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtu2zAQpQiiIIqgUIEegAF6gB5hsssyR+hR2KKLLnuUHq" +
                "VHquXYspSQtix+hiO9F8CIFY2H5PzeSLSi1Bx+fBnQqTBIZYRV" +
                "yoT/om8LHyT74fVZudMRp7SbjJ6Ob7qfh1mZj5OBkxklOMc/zi" +
                "L2GVGdZ0k6zt/O50/DGcf5vxyP/xjm/+s8/4N9aTb/u/UD7+xX" +
                "Ht0q38o2/NdQC8U/5HnlIx/Qxc40me1/06sM/A/yBf2fOf/tPf" +
                "8Knv+ZP9oU/rjv+NOzD+jn9ce/Lul7PYT8tZn4LQBdUZebv6mw" +
                "OCkqPPOwfNGV6dBMyu5/+yu2NBXk1Rr51FSVWP8a468++6gMr/" +
                "+5NcOwk6tsguJvKfpR8s8b/vslzn9pxn8fhayOKx4/iddfxMun" +
                "xn8R/cnxU2P9svSfvPbPUP+4r1/u1/8azh8sQP+RtcLaa42mx8" +
                "JV4scr9TtYJiEaXLP6u6nl0/YPRPnLSd78u/CXqfzzIG+biLw4" +
                "/+pU8f0jefhfYv8jO3Oy2g8AAEBu/sy1f/Am6a+f/xfUT27+4i" +
                "mwZo9X2qPX890wqm/BE7Su3N/S4oNlumsKqayoH+AHLTxPNzdS" +
                "l3N6fM1m2+MnRMge+7+U+p+t/7ZNlh6bk6j5lmbmRay/jNAK8/" +
                "sm6ijWv60Sv9P6ETFYRVpimPW3DyfEf4DGAQ4B/0f9BJB/AEBK" +
                "/SG1cv/sVfQg2ijITPpFEAoN+4M/AkD7oGmIpeYtXBrnpYMaBh" +
                "JlsBz7zzK4zQT2Psmi+8+m8sf5n75U18r+eQCQ0YT0HLo59QNA" +
                "W2jw+vX7+vu96v2rDPfPMjx/uPWLG7Rd/QF42VEufPgrG4H9Nt" +
                "zB/OXi+asf8xceOQ4AAJBOUUrUL+76t3f9bXkT1h/+U45FBg5e" +
                "nh823ePuhpeuufHbeLyFZqf7zdvvOrB/qiJItP0cDCiu/1dK+v" +
                "7/7Wy2dovtp2/Yz1a0Hx958lKnsNudLdz8YEv8p1sw2Hb42Ji/" +
                "/Fh/viytP4+c9i9Xf3nmb5dVHzofucP/2+d/276RsVP+bdqxL8" +
                "F/99o/PcXy9wc8/7cIcb5JonFVN+5mcDWgNmTvfwNE49xD+cOP" +
                "9rZXh7Lc0dextGv1cvj104Oibrjn8/mcIc1v8g9/sX7of4AUtl" +
                "arF3LMZWW5fvRye8pGb/pHva5/lL7/AtcP8Pw1QHgTJ5pw8Y5/" +
                "vzU/y/0vczn9Agpo6097A6it8TftW4Y1FLcJii1sf9fCC8mz4C" +
                "mUwv8omD+eqn1/2Sr2/19JxSxT4PkvfVv5/8b6/weIzqfl");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap2 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap2[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap3 = null;

    protected static void sigmap3Init()
    {
        try
        {
            final int rows = 609;
            final int cols = 16;
            final int compressedBytes = 904;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnU1y2yAUxxFDMxqvWPgA9AY9wusuyx6JZnKAHq1HaqXYiW" +
                "wDBvGN/r9FJvH4BfS+eAiEBGOSMcFmptgHivHLr9Pyg9Y/pjfG" +
                "tDgxYldIfErURET23yB/9pX/fievd+rvzMRsurZVnv////LmY7" +
                "rKO5iYQcSlxXlrzW3/L3+s13a6lfG1v/KwYlz7fvLCIC8K+L/K" +
                "6L/Joihd/AXFj525VvzLBP0PjL+q9vvjbz8KsB+IRVodiso27g" +
                "hL0Xn9UKD+oJzXf3T9AwAAAM0xQwUAAMaOXGxjngEAGJTlNotG" +
                "skuBhgqG0f8cMP/ZsX4hQ75MMG2N+Wvv8mX0x2/8XxoGFmUIE1" +
                "oFx7Z/7JB6DP9B/HdvfxXYA/IdrWPlmyfh+qcMbhxlRReMPCUT" +
                "jEz+T0vkr/7/a/389+L/76evwCd6sv/tIi/+fsXPVv51kZ/jwq" +
                "eX8RJTeq8sSM9T49TBlIfDoiCm/ki0/9WVW6k9LVgSw9yLFSM4" +
                "s2/Xy9QP2W7f/n2QcPxVfV5vR922+DIv0nKr+1+FT36x9V+X6/" +
                "9c036Y/0U6mazr/7WfnwKIPwC8mI564RSpqeLyADSUKQ7uv5u7" +
                "GrLxrqLWAKASEjHdf3lY9qZP7Pp1fPvu9e/t/Yu1/cumvMf171" +
                "bg0x6rY14GUNONqCwEF/yvqQIDAHBfhc62mFHG1Vme6PmnlOdf" +
                "XuRl+vPj6Ln+KuWhMvsP8tmv3fGTIvVPDZ1/Vtx+5c+fnAOtLx" +
                "O3j/oPHLl+SDB+qWzjV9Yrr77/Tw2eLFST9nuJPL/7BXkd3Lu5" +
                "ajhUHHDL70bGOZUQ5yuCges3VablXeOn8fnhyVNeM9f7F6zyQc" +
                "8v+7R/oPyhQkYI0FPm2Po/f/D/n23Xv7b4Vfb4lZ/zNxHu+/do" +
                "MpQSrhujH99Xy//80YD+9vafJep/mfbNFpTjhjX2H4OMkL3obK" +
                "j+Pd74rRje35PN35PUzo3Xf6zH91+WnPuk1l9t+5Xw397yjG5M" +
                "//H1X+7+5z5/+Yiku7mUM3+1kX/Grv8q5M/ATd/747/39xeD8c" +
                "kZf2nz75jzv8b7Ofz5Vzn8l5ryvzL5f2rYN3ikIPecTsnqHXb6" +
                "b3frXwCA1qGQ8bPD5zeAvTa0+4NG/Q0OWv9j/dE/W1Sxn+f7Wx" +
                "z7d7Qj9Zn3P2E/ds+lTA/wzrXKs2WBvc/fqvGdxkt/APUHCOcf" +
                "1WbKvQ==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap3 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap3[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap4 = null;

    protected static void sigmap4Init()
    {
        try
        {
            final int rows = 609;
            final int cols = 16;
            final int compressedBytes = 778;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnVFy4yAMhmUP0/HuEw89AL3BHkFH2CN5d/bgrdM2TbdxjY" +
                "2FJPi/h0zaGQUQEpKAOETbJPKNZP8DUVxep2srica3t8Pywpc/" +
                "hr9Ec/hJfBXk4F6vvdDMPA2dT+Qgpamwvy/RdPsAaPBiyCO0cJ" +
                "SoMmGH8h9+y3/2yP97l58/5Bte00b65AxR044QLUCu2ebmLgxd" +
                "NRS29VreG3/itf4OKu3Tme0L9P8xt/9PPduvo4iYMP+WLa9F/W" +
                "P/okb8uC8f7dkPb2thylu9uMOKJBUHtdCsbtb9hzfs/2Fj/yFP" +
                "XnD+tO2n4oI8rrwHlc0kQQ0Wi5LQ0bAngc/gYsfofWEAAAAnyO" +
                "8/VIXF9g/Oqd9V9BEs6B9J6qb9TbRv/2qXvPf8pYb/lo5fQX9D" +
                "Lf+zoX/b+9/l8y8Xv+A/FuLXcfv1cX9ebv+gh/Fj/wMAAAAwya" +
                "wY7IaS9o3en+rm/lMgvpe/8nIEd1HC78v//6x9f+Pb/Hdoo/6z" +
                "z2f7m6gfSu1XW38z32nz6b81/Wspl5a+/0LcA13U/ywmj/hjn2" +
                "b0N2Aua+Q/5+ffbLx9N9VpedkzK8uDDUfYvP+NC+KG6r9z6ifU" +
                "r8AK2csLt+r/oKrmGzj/by5/zb5/5uH+kn6lqm0/0vZvG8N28m" +
                "H/89X+D5zftR2/EoFMNUFVneU/GfK8I37G9aA0iDhm6fMTUX/0" +
                "sbABV1T7/oq2/XioX8rbYbPjF4ifu/LvW/lZaPzJsP3XiV8sNn" +
                "7t/QsP9of4XSt+JMLvp1RKSs46RR03P25eK7CMEJf1Nd4M4rXD" +
                "P+4dJN6MZaJT1Dnuk8eD2FE/nJJ/YP+8vfp9UpbXxm//cx2k0b" +
                "tPoen2Jesv4CF+MVQIgPXixtHvp327/vdZJCbCVluj9Y9c/BV6" +
                "fkBT538H5E2NX2H+K6//fu9P4P5ss/HvtN8vSA7tN5GD3x+s47" +
                "8T1T0/dnX+i/N3k/mLvyjaTvzG/Y/u7BcAALB/AApQOP/H8/d8" +
                "56822redf57rMMryAEgR4f/Kw0SpXJtnKwHTnA==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap4 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap4[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap5 = null;

    protected static void sigmap5Init()
    {
        try
        {
            final int rows = 213;
            final int cols = 16;
            final int compressedBytes = 306;
            final int uncompressedBytes = 13633;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmm1qwzAMhl8bU7JRRgo7gAc7iI7QI5n+2nF6xDZZUgjNoG" +
                "vrWrLfJ+AmAQVZlq0PCjxGAPph7BCnNxF+unXDIOODOwApvEMu" +
                "ghJGiRX5z1vlv1CeDPP/l3wG/U3Z3wKxsLxmZh9K58unrscG0c" +
                "n3xTU99ufbjy3EwffYzcYIP5K2x6vP9UvX1mHYWLH/lT6/uP9U" +
                "4paP0vT5zfVXvP5+8bNG+ivAEPvoyH+XqUrHZSHktacA9x95kv" +
                "/Ul39KUf0LzP/m/NV6/09h/lW6/1ld/6Lm+q/S/UP7Ecvc579M" +
                "vNvGbHMtNLtkNuKHqNVfqf1U9e9j4/I11y8t1M/R+PrVXP8Qoh" +
                "3Gr7b7f63Hn8j5Z6665bf0nv4Ek8bxba0fk9isIYyftvQH9Sck" +
                "Dyftkkx1");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap5 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap5[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int lookupSigmap(int row, int col)
    {
        if (row <= 608)
            return sigmap[row][col];
        else if (row >= 609 && row <= 1217)
            return sigmap1[row-609][col];
        else if (row >= 1218 && row <= 1826)
            return sigmap2[row-1218][col];
        else if (row >= 1827 && row <= 2435)
            return sigmap3[row-1827][col];
        else if (row >= 2436 && row <= 3044)
            return sigmap4[row-2436][col];
        else if (row >= 3045)
            return sigmap5[row-3045][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in sigmap5 lookup");
    }

    protected static int[][] value = null;

    protected static void valueInit()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 4263;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWwmY1EQWfvWSVDrdAyswCIiwnLLCcIiggoLACoK34IUXoh" +
                "weKCCKCIKAoiKoqKio6wHiCbreKKIC4rGuynqfKHiuJyoKiC69" +
                "rypVObqTnvRMj7ib7+tOUp1UVerP/79Xr14b7YEBGg0hDRncCn" +
                "WhnDeFxtACOkJraA8djC7Qxcyw7vYH0B2HQw/oaZZhm/QQ6IeD" +
                "8Wx4EQ60OAwygC+FE2AYDDfawmlwOj7DLWuU8Y2zD5wD42EynM" +
                "+XgAkpqG1twSvM5WwrNICG0NSoj7eYw1nL1FGwk3kBa2f8Au1w" +
                "I+wCu6beSj/JfsOrYQ/ohgthb9gH9oUBxkb8DQ4zdsBx+AQcDc" +
                "eYbdhSOJ4NdN4x6+Ns3GzWg9G4Cc6CCcYVcB4bwWwcYLwDZVAP" +
                "l/NmsD3Uhx2gEY6HJsZsaAbNWStoAztDW6McTXMwdDLWw+6wGz" +
                "qwF87n50If+Cv0xWF4THoZ7A8HsCz2MofCoXCkszscC0OMBnAS" +
                "nGjMgX/CSDgZzoCxsBrG4bEwCabYQ8AACzh+ATY42M9AtgVqwX" +
                "bGTvYaqAM7Yq3MYYYJLdnj0ApXmW1To+AvUAGdoat5EvZhD+De" +
                "mMWfcALsyXpDL+htvgL9+TLYDw6Cg2EgHAGH2yfAUTAYjoOh7G" +
                "gYAafAqTCKT4QxcCacbVTAuTCRP5nNWuOtvuYU+0drAlyIW/FU" +
                "nOg8xfeAqdCR756lzeqHkwS+WbnhyVSSwTaZB+h4cDbLHs1mnS" +
                "HZrAGpxdnQ5ix2hhK+/fQ5X6LKF+I8qzvvpsvtAXytNVFecSTV" +
                "XZ9KVoiz1NuZD6jehX6NMI3OCV95V197pltqfUW9GJjNmnRnqq" +
                "VZj/q1ia4lfKmc8KV71mYjNmO2+Gat9Lk5WZaul085ko6O4+er" +
                "nj+fjdxgiHWVvGcOWxIs50+L7/SxasS+VO2h/j0te46jMkcYV6" +
                "sxuIzfmvJa4WvwUGMUv0HcgNQr1tsbq++D7RC+090jdpp370Q1" +
                "KvvABXRG+MJFuAjvhovxHqptq+zJQ/nPwrqnZ9H1L/B/mGi3Vz" +
                "0frPr7mcTtF9Wq+315+qH05NBTe2OAz1F9W72n/V7wV97zJ9ZO" +
                "4Isbs7Fbek/7zZyeLVXttvPql/jK30ZI5Fg8vt5YrU99xNcF8S" +
                "X+zlD1DePL1TP0pNqGBupoIN/E/uGa8dhQO51y8bXXuPiWNTeI" +
                "JU4TOl4VvIP4e4O6ciXeV2AsRnpjMCkXX+Iv9d6hUU4BfgqI61" +
                "x95i9CuTUFLsG1MJP0+XLoQGN1NVzGumeegSsFf6EnzJFvr2Im" +
                "HEifWfZb9H2CrFnqM7X+aHo6fkSlpM8S/y1gqudfI/AV+kxPsI" +
                "H6tFbgCzul1rF2NrXnTBX6LHu7t2yBuC70WT7Tvi5/8zfBX3n1" +
                "aBff1AAPXxphqKd+lVfhJy6+0Jw+s119Tq3V/CV9Fvxdx2cJff" +
                "bYKvVZHl0KRwbxNWg84AqhzwFuT8lIPQNudPfxhVouvlBH4tvC" +
                "eADEk3saAhX06co/xve9d+Uz2JPKenlXSH2mvdRn2rv6HMDX0+" +
                "erQGJtfSjsr7mjb3+tj4X95Ytc++sMMhvTSC3m9CaZTYT9VS0F" +
                "8R3kjEx/LeyvLJH4Zt7JPCnPgvZ3LtSm56Ox5otdfNX7J/GlPf" +
                "EXiIcaX9WChy/Z3y+E/VXlZH/l/hqhz1aZxpc+18KE9ONwHtVI" +
                "cmLVEvZX/ibtr89f3/56bJP4OvNhL+NTPjsXXzmCnwr765VK+0" +
                "t7ZX/paBx9yP7StyHx7SHsr8YXtpN3kf11FlBZHr6u/U392Xtr" +
                "m2r7S7/21/i69lfeEbC/dObZX9Ln6xS+XxC+89JDzfFwIx8Adc" +
                "1zNL68D7Q3x5lnwfV8P/KvZsn6evDenEY6PZw+J3n9GkR4knV0" +
                "8XW3zHuZp7hSLhdfeVTbPNtuIPANaWxLtW/H+0K7eEWCAWnxNu" +
                "fgq95c+b7xfdVvpM/Cv5JlvUBjvz19bsips01ITder0r3MhnxW" +
                "TC8ODRwTvoEzD195Zsgae7v4qiu2U/sd0yMEvnl1dyZFXZ5q7u" +
                "E7QZUrfNXZwBwbNcnH17vmb2p/s7WH1meYT+flDqkB3AK3Qmva" +
                "d3B6w02wgJHPIPQZbiN9Xpivz4Tnh64+a/6Wtc0sD+qz4K+yKu" +
                "T5OH18/qZWBvkra4jhr9DnqBHX/pXmr7a/Ln+j9VmeCf7Ozudv" +
                "RtQ3ho4i+Jta4etzoI95+qz23Aj02NVnwV8aL+IExyh9Jv62CO" +
                "qz4G+UPgf5G2hD8pf2t7v85d8Qf+9MD4V74O98A+nz3fSbnB85" +
                "K0ifiWd8E/9O+M/wINwHPWSrbdLD4f7MvXwj/9nj789h/pbNKN" +
                "vR6hbmL9wh9fkxvtn3n8P8FfMjxceteWN4F/H37Tj+an3O56/Q" +
                "5wB/F/n4FuTvedHsTa3S/IUhifh7cDx/+foo/hK+rXx8C/EX7s" +
                "3j7xbvGqUNEl/MPCj4K/Eth4cFvs63rv21ptmvKXwfsaa69lfi" +
                "S/wV+Lr2l/CsCNtfKpmWZ39TAl+6b3Oc/bU7aPvr4xu0v+lN+f" +
                "jC8WH/Khdfs1PY/oI3P5D+VYT9FfhaDaP5m66Xa3/lPt7+Hu7j" +
                "G7S/Lr7R9jfgNTZ1/atK7K/EFx4V+Hr8XQJKqwW+xF/Pv3L5S9" +
                "8S30xHd35kjXTtL/S0Zgr7C/3KDvH9K+so4T+H8VW1K3zpyMM3" +
                "rM/EsFNcfK3jaF/Qv8p0ifCvjg8xQPhXZxXEt1ESfPn4aHyF/U" +
                "2KLzxG+B4V7V8JfO2PovAN6UndRPh+oOMbAX2+ztdnfi2fR/r8" +
                "uOTv0kwnFb96QuizQzUH9dmaZdRB0jS4n1Bu4Ouz3A+L8Ugkvl" +
                "qfBX+dPtH67OIbr89yH6nPOdfm4BvW5yT+FZ8bW3dx+jw0Wp/l" +
                "M0bqcxjfKuvzMqXwzeBZeNI2eRk8g115HVgFz8NKWM4JCXiB+l" +
                "CBu+Du2AMrAjP4NkizDGyCOyOxG8l/wb1QcgD35JLv2BibIrED" +
                "fU+hG8i4Dm9Pxx3CT8HL80cRd8sm3tjAKHyD/lXsna1yxtMb8X" +
                "h8i9uMMQUiFBfl9Pqp3Ct4B+FfVfr8pwXqWKH2zwXtr20LfU43" +
                "FvqMY4LxZ14BXfh3OMh+HbrjaG1/tf/s219Xn2E471xYnwW+fD" +
                "OdyfhzvD5r/sJrWp9F/DmKv/H6rGxYhD5DE/o0i9JnEX/W/C2s" +
                "z0H+xuizRfo8Idr+uvwN6rOOP+fiG6/POv6MY7U+pxt5+nyu1m" +
                "eciZfgRcTf99JNiL/v4DScDsJPfYuQnMorhD7jpXhhkL9qP8PV" +
                "51CPDo7VtTc1fwW+lfE3X5+L2TR/dfwqbsPv4vhrty6yxTdCZx" +
                "94NU6MvydXn+H1KP5W0u5xQf4Svu+q8veD/IU1Af/5Q8FffEjz" +
                "N30+4fsIPqr9K5+/Qf9Z+1f8hOT89eYcbV3+yusL+s/J+Kvtbw" +
                "x/P0riX9mdq+9fSf/52qT8jfKvCvNX+1fGFMnftSH/eV1g/ovw" +
                "sY9v2H/mFZlehO/jqgeV43tiPL4ifhWFrz8/qhF8WQ6+nyTCt1" +
                "tp8DV71Dy+Qf/Kw/dTjW+qC33Pk8cbkDQ5tSuuVur1mpzHNiF8" +
                "38ZX6Pxlz796SV6/MV+fY5UwoF+5+pzqWj19zvWv4LPC/hX/1+" +
                "/nX5m9k+tzxKhNCK4PVu5fBfznz9X+a/p8Y38C/4YvqL534Sv6" +
                "Xg3fwQ/wrYuvbIfwhfUaXzp38X01+XMKfOHLmP4F5kdV3fLjV9" +
                "4vtfL6Mj7W2y05vtaD1bkbJyR6vydF2GW1Vmx2Ffqcmi/jz1Kf" +
                "8eugPtOTfkbtfJta4Otz/vqRr8+F4htCn73yoP29Pd/+Rsc3Sm" +
                "J/f0ykz5+XRp+th+P1mY4q0WecnESfzS5anwPxjZ+UPjeCzbAF" +
                "NjmnwK/wi5HhgsWkz/Cb1mec5PLX12fN36LexDcKvOMTsyXekv" +
                "rP8fyt3H+GRLbJWlIt/k5OZAO6RPROZUnAf+iDtin4a5c5D4A3" +
                "VyH+bgUViTCaifUjThhDT8hGrR/p9f0o/qojM9R+gL9ifqT5W3" +
                "j9KAGyo6PwDa8fhfg7O4q/rj7H8Leo9SNrWfT6UTR/xfpRKK+k" +
                "dvz6vs9fHJu/fsTA5a/zBNWyv22L/JyAop8OHQO5VEt1/lXNbM" +
                "6yRMhNK5a/fv5G8fyt3P6K+EYC/q6oFn/vK9iD6QV+u0AhOdo+" +
                "EO9mY8P45lnwGsW3FP5VqfEtNr4Ri++zNYdvIf+KBeKi2r+Sx0" +
                "qfRf6VWN/nFWwcO5Mtdn4V6/tSn+cU0md7aGF95u0L+1d/FH02" +
                "G5ZIn1+sjj7r+W9hfQ71RPtXKv8Km6UXGQuMWwjxiejVLfJjVQ" +
                "tFri/YJ+r1BePW8PqCqi9ifSFq/pu/vlC8Ptewf5VMn1+qDn95" +
                "h+rqc3olqPxRRr4ayPUod35EexFrqmDnOUuN23T8KrKuwPqgfV" +
                "L8+qCOXxWyv4Xzr4pbH/TnR6q0iPyreHyLy7+yXo1fH4you3M0" +
                "voXzr6jEmx95JV7+lbEIMP25m39l3Cf0GVez6SL/Cl+T60c3wQ" +
                "IZ37gSX9b5V/hSrD4Pi9ZnfKOQPht3/t/q8+vbSJ9V/hW7INUF" +
                "0HhJ4MsuNN6C8sxgNz4JrY3XBb50zQyNr7S/hfEdXgV8u/7x7C" +
                "/OLxG+3ybD13iutPiyi1x8ib/vE75y/YhdYrwH5YZcX9H5k6TP" +
                "F2dGiPVB4V+xmdCTXar9K5wRge+I4v0roc9/NHyFPpcE3++3LX" +
                "9F/hW7gr7l/Mi3v8K/gvZsDpd+FfYI50/iDiI/B3eOsL8yT8tQ" +
                "kWbXvwrm50T5V6H+tYv1r+6CAY5ZbfubMD/HYZXZ32T5OdZPNW" +
                "d/dX5OpP1V+ZPYTMSf2TUyv66riD+zeeH1waD/rNcHCd9+rv8M" +
                "B2InGCT8ZxF/Jv/5FPn/QeE/i5WKFjr+jN389V+Bb5C/suac+L" +
                "OPbzD+7NSvfvyZzU0Sf3bqlib+zBtXJ/6cbH3QxZddG4w/s+v0" +
                "/Ej+P/RnH1/jB4Gv8VMuvuz6RPie5uG7IQ/fudXFN3J94Zri8M" +
                "3R57j1hdYlwrf574VvzvrCEo2vfC4ZK3fnv8YPclSc3PkvuzHR" +
                "/HeUN//dkHT+Gxm5KSK/btvMf5NtKafm5r8FY1rePNCd/wr7K/" +
                "5/xG7W9jdzv2t/cRe+n8DXtb/i/0eu/TW3c+2v4C99ixwbwd/T" +
                "Nb5UX3/f/gp8K7e//v+PirW/Uf8/Ur/0qpL9nVca+5uqu43srx" +
                "dTYCp2wRZw0j9YxRay+eK87H6dHyvw9VpU+EoMFL4BrpTbZ+Tj" +
                "q1p8unL+8r6F+evUj70zD1/vl15V4a9zQ4n4e0gRqhOZH1ukcu" +
                "Xkx7K74Fl2u9nCbM7uiBw3138O5U96GMzIL7NHx7b8dHB+VMWV" +
                "JrN4fS5mC+DLSrQ2ds22wZfd6fnPz4L7T91nhP1189uZXLeoSn" +
                "67PUbz12yZNL+9Zu1v1fA1G5YG3/SN25a/rn8lEV/mry+wJ9z8" +
                "5yrgO9bDt3UOvm/+L+Fbqvzn9M1F1FHF/OfQeU7+s9lRzI/wID" +
                "k/2l/Of5eH579ytBvp9QU3/wprx+Vf2WcWyH+e668vVDY/iopf" +
                "VW1+REdVmB85q0szP8ocsI3mR+sgIuvJJF+ErQzmxyp8E+df2e" +
                "Ni+PiGPz+q+e3352/NbNWYH2md/i90tDzO");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value1 = null;

    protected static void value1Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 2083;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW2lwFEUUft3Ts0uyEJMQMHLJFVMICYcGCzkUUBSQW6XUwp" +
                "MCFRC5EeSQI2igSlEuAQ8IAlWACv6wyh9S/PCHCoQAGrSkUiWg" +
                "nKJV+kOhanzd29M7uzuz2ewuszshXdX9enpnenf6m++912/eGg" +
                "YQoOQbyIaAYUA+FGDbGjpiWwQlUOrrZmBhgw1RoD8MkL0hUo7A" +
                "Ok70noWJhuGfDVNgmqEKzIO5sAiWYI9hzfGVyPFbodCwFLhDyq" +
                "5Ye8Jdlk/6YL0PHsB2qDh+VI4/CU8JuT5snuminY/1dTXWFJoL" +
                "2RJaQCvLuR2wFkMXuNMc0a7iSG+s/fxF2A6Cwerc4fCI7I2B8W" +
                "r0BXheyMnwIrwCM7A3C+tCWIythtWH1Q9Z8uxmkCtkHrSRI52w" +
                "dlazdYNeUGa9G18p9MXx+2Egtg9jHQYjYRSMhcfhMXHFEzABnk" +
                "M5CV7CdirWV2EmzEF5FhbwM2h77F/mPfItxbnhouh/Rw7DFfEN" +
                "3WhPeg/tT7vBVfNbaTHFVaJtaRfaHWUPrP2oWCPa1z9HyNa0HR" +
                "uLsqO6hqN0AedDfGmpUWehve1Gs2qNuArHN5GiqXvMOm1kQPGV" +
                "Jnol+V7KE+QIqWZTSRU5Ro4zwTxaJdf4OMdX9GroUWyPqNU/7D" +
                "Svf64DXiflLy5J5n6zO7iFb/bt7iLJpqQY36Nq5d+Cy7Sc/EAE" +
                "AnCRvUaXkR/hL85fulTiW0FXhPirrlxpg++8GKt+IVl84y3J48" +
                "sKGwZ/aXsmUSI1tMzmG5R+tuAaSz8vUPp5RaR+NvmbuH52D19u" +
                "fz2N7yn5nK7n/hWuCPpXpNbRv9oQt3+18Eb6V9mz3PKvsmd43L" +
                "/62/SvgLDNvk1sK/mNlkE+OUt+5fiyD/Gckkj+Qn87/sK4IH9h" +
                "IvJ3keLvFpO/HN8gf4P4xuIvx9eJv/b+sxN/rfhK2RKf6zMRZx" +
                "bb8Rf6+dY5zj3G0pf4yiOFrzjS5KjCF/u5UrZxmLuXPX9NfOXR" +
                "2KjrFL6Kv+dM/Uwuwn8ELSNcs+pnuJ6gfl6s8P3Iy/o5u07/Gf" +
                "7JaP18Xso/gFh+cz65ZOpnk79B/4p9TK6Y/I2603FScv28xOZz" +
                "oZ9D/I25al1tsN7hPn/pNo/z97LyFCuBst3c/rLtbCcUsM/ZXr" +
                "aD7YIicpXbX/YZnrMfr14rZhpA/sTjbWH2d3XQ/gq/ZKnV/rI9" +
                "3P6K3j5uf0P+s5P9FX0H+xsXc6fb8Tdof1G2CBvl9neNnf1F/v" +
                "7raH8rQvZXffo2t7+W48VS+sLOaiZlnhqJsL9Ybe2vOkPYX5QR" +
                "9tfyHdL+EpD2N4/m0+a0gBbSW2w1RJC/t0UwKsdx/7usDo3jyv" +
                "6ItkhaP1/ztv+sVmIEHYbtSDoa2+EpwHd5w8BXm9BA8G3PvhDy" +
                "ENZU7H9XKP/qQOr9q4DPLXwDrGHgyy7JOxPamZ2LjE/KnopPar" +
                "laXqz4pOOKn3RzXZLHN3Z8g/3uzn1o+UnP0EEXulbrxFu9wIqv" +
                "nh+Nb13x5/Ci53oV30BuJvBX63xDVqeKTIPuYfa3Rm8Z5/uFlT" +
                "fK/sIbmRWfhGfSrZ8hhi8Lyg/SW5vxSa2fXsDjk0H+QpGeb8Yn" +
                "OX9hLccX90cVHF/H/VG5dX+k55r7I3rSW/ujQCuP74/KQ/FJE1" +
                "8en4QCbWh4/Dk8Psnjz9y/giFB/wpGmPFJHn9G/2oVx5f7V3pb" +
                "7l+Z8WfaBxisC8Un64o/h/yr9Lzf5/ErL8efycYgvnqR3t1iL+" +
                "/Gexwey7+qn/1Nl3+VvH4OPJgJ9te/JmHLPUwiOoJUypHxzvEN" +
                "fVTcv2iT45ofdG//mwJ8H3IfTfg62v5y/tZrjkMyPrnL3P/qY0" +
                "P6GWWBOpNnIFn0sxmf1CZF6efVQf0s9r/blX4eY+rn4P43ZH9j" +
                "6WfT/trpZzftb4z8K0/YX22y1M/L9WW6iEloi2Lwd2Xc/D2QCf" +
                "Grxvf75vt9ckIvJ9X+r3j+lbY0tftfWpuu/W9sfOkZr8Sv6LmE" +
                "7e8She9GUq1vIFWaA0eb4E5KF1aVtTU8UhoKf1m7ZPFF+1vJ82" +
                "ObnNbKQ/mx2qpgfiybxWbXLz9WnsHt73a7/Fg2J6n480yHZ/CX" +
                "lPtXGcFfNj/RK838OuTmJ1g34L292cRRb+mbMoOXgbmu+c8Zga" +
                "8/J1n7y+Mb+qf6XtEvA7S42iZr/gaOhsU3slprebHyryz83WeX" +
                "fyU+TWP+lfZ+xJkO+RvO+tnN/A3z/ULi+Vc4SgwjS+5vQcwXjq" +
                "/g7378rg/iyc8J96/kuCU/p07mdY3x2dDAO6nA1+pfOeHrzF83" +
                "8TX9q8Tzc/j/U9RsZbaWsF7vfy38Paj3sdpf9q7qpzm/Lh7/ub" +
                "7+FVubWf4ze0/5V8f0LcjQrXXjmzWrPvja57e7g++Nfv+b+f5z" +
                "2NMu41fZxdHxK7leFXpNKH4lpMP7I5RR+e2yF6bxEnt/FPjSrf" +
                "gV18/pj1/5c5KNX5ETrJJUs918/6ufYjuRyT8F8yfVMyTzJ8Oe" +
                "q23xPDd6H7ZHXbHPW/GNzOBv8vENSyyMZF939LbvNTxVgviSSV" +
                "7Xz6nYH+kXBLplKfevzjfa34yw4ZVYd/tQn+tXuX4289uT1888" +
                "vz09+rmh5E+mrvgse0nz/YKvhxrx2Pv9Rv5G4fu0aHta8c28/I" +
                "3AhUb+1hNXYefoSP9A+/8vyPVS/1/wDxL5zznp+bWByzcXvsnn" +
                "PyPCI3Ge0/6XxXw/Y621Wa8KusJmdGWmPrWN+lmthIxP+jen2n" +
                "/2jU69/9y08Obibwp2iv8DxCSvTg==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value1 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value1[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value2 = null;

    protected static void value2Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 875;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm0toE0EYx4fp5ktqVHziC6UqKhVRg1QRX6AnrY1PqoIiHh" +
                "RfLfURqYIlSo00YLGKl1oEFdGbqBT14MWzD6jR+kBEqCcRbwpe" +
                "1slkM92N2bi73aQ7u9/APDNTOvOb//fNbLKwRWWBxsN3WVqvmg" +
                "Q6paA+WvV0oBOdjav6mS+F56i+CLCVzeU3jYd6ZOA7cn2l+EYV" +
                "1Tch/Ae2ZfmWWK80bS/SeqHifOPI16Z+t+fsc+Qa2mdf8m1UVU" +
                "IZ38ckaoUvWW3kSxoK+5IjpMlQP6XlhhUjk8hkQ32urhwjSwZr" +
                "kV7WskbTb7O1WUWeFPxP44v1IjN5WkvmF+dL1pJ1om892SjKO4" +
                "r8rYOGWpuWg6F1lJaPFS2z7dEiG1jcxGKjVt/F4j5DjxZynCTY" +
                "CjwlZzipBtiZ1S/dLIX/TaB+bep3d84+87SsfCMDLvA9bXGftw" +
                "adb+SbsV6d4rT3cHqvNYp9gmc/fcXSl6L+wrZHzLii3yTq16Z+" +
                "9+b0O+Irnq/8ej/ifH8w1vs97387ka9tBR9g8/qsze8ji1/Kc/" +
                "9VrrjA94ZX/a/S5S2qylXB91CWb/RXufnKZZ/9FPL3Xzjs9fuv" +
                "5dPjkO+//BMf3H8t6iENR4d8Yh+o4I5tDbp+8/cjWgMtSjc0QT" +
                "Mr1+nst/B0NEaXwTG6QEd7Hp3K0um0li5i+WIWV1KuAbpC6zGN" +
                "zlCus3yWGLNcN36hhR21dDhWxZ/22ZJ+TwRhnv7jS2t0ZZ1+Ia" +
                "HXL11lX79wEvXrMdZ1RVtjLJrw5TXGF87+y5fnyNej+tXzhTYn" +
                "+kW+3uQLyUK+Du3zOeQrxQ7g9tnQwvmyXNhn4bXbzfRrGI98pe" +
                "MLKbTPMvOFi6X5ov/1ZoAON87PeL6SeAekrftf0QP9r0T3I3P9" +
                "wiXUr/T67UT9+v6MlYbLQZinH+2z9v1RF35/FFT/6/D5VQ/ylc" +
                "YH3wzGPH1pn+/A7f/pF+450O8t1K8n+N6HR/CwDPb5AfL1DGX+" +
                "fgr0mn4u2e/bka/Otz4fLIcmcHra+ymhcYKnw/dTQmPEiAzyHS" +
                "b7/AnewFvIZO0zfIA+6HfJPr9D++yyEt/b7P+Mp9/ZaqZMVjmZ" +
                "f74h1+/bUb/2Qpji/Uje5xtKNyNYVer5lRP7jM+vPEK4w2ifw9" +
                "X0PNpnvwTyF9l0iXQ=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value2 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value2[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value3 = null;

    protected static void value3Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 507;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmstKw1AQhmVscnauRLy1lG6kUrRdVBHtEwj1rogv4EbQvR" +
                "u3XaqoqFTwhuDd4luoBRGLb+DOR4jhUGJiq6I2es7kH8g5SZoU" +
                "+n/5ZzJJKWo5QWmrSlCKeilDCdeeDmq1xzDFqduek/YyQJ3yk/" +
                "7yEW0UEY32HHPO6XOd32V9GdRj/UPUv1jMgqKiyR++xjP46hKi" +
                "ORi/k6N/fcvP7fCvNldBToThX03rb8Qf/4au4F/W+TkGvqz5Jv" +
                "Tjy7Cy+sc3Cb5aXAEpe8l49ki+9hyXY9LVR6Xf+Mo5VvUbwfcv" +
                "e9usZRlrnx9jbEAnbe+fh/zJz8Yu8rMS/h2GfwPo64Jn61qMQB" +
                "NN8/OUmBDjYkxMUtp8EqNmqTb52XxEflY4a097/QtFWNOegQaa" +
                "kpuV/szKcfDDbNnybrsByulSf13rtX1+tYj6q5ybl6S2xbLG94" +
                "7aJbqzx1tn++bb19ED1FWG8nIFHXlP/Ru+CIX4roBvwKq05CtW" +
                "wVdz566j/gbArcVK/4pt5GfGxHMiDxX07H/N+dCmOSd2vP1vKO" +
                "/uf82FH/y/bgv9rwIVd0+qiedXXPnugy9rvgfgy5rvIfiy5nsE" +
                "vqz5HoMva74n4Mu7/3Wt1/b97yn6XwX8ewb/suZ7Dr4Bon0h6e" +
                "H9ER+il/Ava74F8OUcda8kvflU");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value3 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value3[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value4 = null;

    protected static void value4Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 532;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm8svQ0EUxmV6OzMLsbNohYiNEPFYlIjWYy2e1W6JsCRiJ5" +
                "GwqKCsRUjEH+GxwMojNh7x7kL8BZWIvRpV1dZFW70xM/lO0ntv" +
                "b89tm+93vzNnJrlsIyKCdEa37ZFvgjhS3hdEEEoE2wRfrfluga" +
                "/WfLfBF8F2oIE2LHdN/BuGLmoGKWX7zMf6mJf5iYuGWC+9Tcmo" +
                "Iw3EQ6oSzpQTp9gWkwpSI/a14uUmldFPmmIZRaSE3oh9Wfyaxo" +
                "Trq9P4X/VgkyO+B/SSXtMrcSz40gvwlTXoXYb5e1G+h3FNXaZK" +
                "Z8nX3gO+Mvg34TinfNkR+EpRn4/hX635nqC/0ng2dJpOlr0LSm" +
                "H8TRl/z+BfKerzOeZHOs+PLPNvGHy1rs+P4Ks13xfw1Zkvt4Ov" +
                "1nzzwVcGvtxhDV8jAL7K3AVB7oQKivq3CeuT+gZ3p5OF9Un0V1" +
                "/6Kw/8K0V9bhUubnnnS0O8GfVZ2lrblln+x/ok91rhX9s6+Gpd" +
                "n33gK4Xr/aJ/Wvqlv1qGTsqOv4N8gA//7F8+lIV/++FfCfh28N" +
                "G351NIt07Pp5BC5aroiJXfbruP/cqYOH4w0StIAiZnZ+APTVwe" +
                "5ONQQWu+E1AB86Ok/moS/ZUsfPnUJ1/bU47WN57BV6H6PA0VFJ" +
                "3/zhsrwsFzyfXZWPtzfZ6Ff5WZnS1AA3XH36h/F3PtX2MV/v3/" +
                "yHsFODhZ2A==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value4 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value4[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value5 = null;

    protected static void value5Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 598;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm01rE0EYx8sweXYGJNZD6YsYShBKRDQBWyltzx5ai/Ttpt" +
                "9AMN8hXgIVT22pRZAWPdgeUs/eBVuFNBrrwU/QWk9SfNtOlrjm" +
                "zby0WZiZ/B/IzOzubEie3/yf59lNVjxylfGoWHTrGEuzVI29D1" +
                "2YFSYewwdW812GD8w0NqjordG2Nx6uOSPBbrIJdrVkzxAbUO0l" +
                "FmPXVR9Xr3F2xTsyVpxxkUXEE9VH/XNGS86/1sTnGgGbNmkz47" +
                "qhpfpzQivwk6n6FW/oVRD6pQz0qwHf22JHtdPsjmon/zurv2I7" +
                "rPm36jEuir4NLv/64/bm33fQrzGr6xN8YG79zFcVwc/l+uVPz6" +
                "pfjvpZJ75fwNfS+vlAHDbKv+LrKfimwFcLvt8Cqq+OwFeP+BwQ" +
                "3+/gqwdfcVzNF/kX+m2g3x/gazXfn+Cr0fXRL8RnS+vn38Hol2" +
                "+Drw7G15V6/xTHz712k2/wF3+Pyy61J1NxzrMm3/ulP9qCp/U0" +
                "GVJqmijTlqdf1ce8Nl5DfUq/Xh+tqU3oVye+DvhanaETzfOVAn" +
                "w75fpIStRXVvM9B77G5OAwfGD2/Q15Hvc3EJ9bis/d4KsHX3mh" +
                "mi/021H5txc+MFW/9ICv0n3ZV0+/lIR+kX8r8m8EfHXgK6PB8M" +
                "X/6yzX72XwNWYVpOUteMHQ2njq3zh0w6P5vkg16/PNs8LTRLv+" +
                "9k7LKyQHT1sWn2cQn7Wor+46886cM+sssGHad2Yo3x6+9BF8te" +
                "B7j/boA+UK+qV9yoKvrkYtPsVJr0t+X0ji/qSN1nUCGD/1Cw==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value5 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value5[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value6 = null;

    protected static void value6Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 589;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms9LHDEUxzVmM0naogh6UBTpxVZEXXQVsSr+C2p7rQfbcz" +
                "0UavHkDxQ96ElEQfAHdqEVUfHoXyAKKrYsRdSb9tbWgye3cbXL" +
                "7ro6ujujmfB9kJkwkwzD+/B9L28mvj7xMWxrvoEwzGATn+ADrx" +
                "qdUy0YodhNF9TxM/1G5+mX6P0l1ZYT5szc8dlfo71FePoR9dlr" +
                "E58n4CNvGimJ6QeSjvCTOtJIymOulJICdSwiL0mlOlep9oqURe" +
                "40XI0oJMWiX52fR+fUx8yvuMN71YKNM3zFkEt8B8FXi9g8ouLv" +
                "OOKzufFZjLqhX98i9Gt0/h0DXy3y76r12mq32qw3JMBCViv74Q" +
                "xf9h18teC7Fg7z5kv9shBvAl9djbfcbzxb/9/L2r96wgfVP0ji" +
                "7WHSn+SqNl8txQbo35x/6aTy0GZ8/qXT6eqXTkG/evDlZ2IrcX" +
                "0Vz5d1ga+na+Btm/s78BHqo4T6aBf61Sj/7iH/Qr/34ct7Ho6v" +
                "CIGkjX5/Qr8Gr68Obbytef0LS5P/EXxgNN9j+MCr+Vec2K+vWD" +
                "CF78+zyL9G17+/wFcXvuKPC3x/g682fE9d4PsXfL1i0vLiW2e+" +
                "B7nr+VcKZ/QrOfSrA1/5LJavfOJUfJZPwVc3/cocmY39OSbHZ5" +
                "kL/Zpi4jwSn/PYKltxfv3MlsFXi/ybzzv4u9v58s4U/g++BV/P" +
                "1Ecv4APP6rcsvj5yKj77jqFfHetfx9ZXleCrB9+L/RuyCvs3DI" +
                "3Pfnf0S1fA1+j4XA2+RvOtAd/Ht4x/34fomQ==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value6 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value6[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value7 = null;

    protected static void value7Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 483;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmr1Lw1AUxeWSPJI7OSuKdBFF/BhUiq2ok+IiDg4uLgqOjk" +
                "5OugR0cFBREMS/QJ1dRREUrB8VF1EHZxXpFmOR0taSUvJa3ns9" +
                "F9pA+rKcX8657yalNj9X1O+XKOqjQUpSV96ZdmoKvluog3qCY2" +
                "/wSVBn9pehvxXN1Mrx4BjLXRPPu77bL1s04KMkFFWPbwJ8tbkL" +
                "PE5CBfi3wL8j8K/RfEfBV5fiMWhgeP8dhwrI54J8nkA+a5PPk9" +
                "AA/i3y7xT8qwJfnq4OX/sdfJXI3hnft7fC19g70MmUfGZXUj47" +
                "8K82Hp+FBtr230VxIo7l919xBP+qkc/WbuDQpUK+1n5UvtYe+B" +
                "o9Hy2DrxL5vCJS4k7c/vIVj+JGPEjK53vwlVsiXeH609z+ab2M" +
                "2h6tlji7Bs0N2T9vQAP036L+u4l8NprvNvhqcxd4fAAVjOZ7CB" +
                "XMyGc8fzaXLzc6w5h/63A+OoMG+vqXz52F8P2zM1+5f505+FeV" +
                "fOYL+fMR+KrCN/v+6BLvj+p6PrqCClr79xr+NZpvCnzrOp/TUE" +
                "H/5xtS98/f8K8afJ3Mf77R85mfwFeVsmL8HJ7P+P+GzvnsfsjP" +
                "Z36pnX/5FSRD+X7J5+t+1pDvG0hGKc5AAz2r4QeiPoDA");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value7 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value7[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value8 = null;

    protected static void value8Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 414;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmj1Lw1AUhuWY5sZcUNBJRSkuoojaodZSP2YnrV+jujqIg4" +
                "KLbmJLNxcFBX+W4q9wd/AaRUoTBEUTmnt5DiSBJHd5n7znvQci" +
                "RdMuKZtvSkpSkWWZ6bgzKSPReUymZC66zkfHkkx/Pql9vTEq4+" +
                "FrdJ1or6l2rJ81P5YsGCqFkmL4lg3f4AW+ueBr4Osy36z6s+6F" +
                "r9N8FXyt+QpaOkAF/Bvzbz/+dZrvAHwt6s+DqIB/Y/4dwr9O8x" +
                "2Gbz74enfG6FKcr/fwX77ePXwtyt8yKtjpX11RO2pbbaldKfvP" +
                "atN/Sqc/+4/4Nxd8F7PJ30Idvk7vr6rw7X7p2m/eKqyjlLX9eY" +
                "X8ddi/q/iX/P1T/q7hX4vm3w1UcJpvHRWszN898tfx/fM++2eH" +
                "/XuAfyl9iAbMR4n56Ij+bNH++RgV8G/Mvyf412m+p/C1qD+foY" +
                "Kl8+/5x/9XQTPt/6+CBv61Zj66QAPyN5G/V/g3F/25kQ1f7xK+" +
                "efRv2JcO3zCArzX520ID8jeRv9f412m+N/B1mu8tfLtfPe+qzF" +
                "hP");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value8 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value8[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value9 = null;

    protected static void value9Init()
    {
        try
        {
            final int rows = 40;
            final int cols = 120;
            final int compressedBytes = 399;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmstKw0AUhusxwtTJE1TU4EYU8YK2VdRn0YW+jE/gDcHLwr" +
                "Ur30DER/AFulKxIHhjjEVjCaK0NDBz+h1IMpBkc77+8/8nVBLn" +
                "on3n7K5zUnVZRUffK1mQuqzLzM89mZRKeh6VKZlLr/PpsSbTrT" +
                "urX0+MyFh0kF4nsndW2t6fdf+W1BzVg5LE7mXr6q9PdMv3Ar6h" +
                "lD2kB6Hqt23dU/3aY/Srmu8JfP3g28pXp+QrpfnqjHylmu95MX" +
                "yHGvAN5lewYy/pguL56IoeqOZ7TQ+Yj3Lz0Q3+q5rvLXw9mn8b" +
                "zL+q+d7Bl/25E77mCb7B5OcHeoB+c/nqEf2q5tuErw987bPZNN" +
                "t/8zVbXfjvBnxV6/cFvl7o97Wg/HwPX9X6fYOvar7v8NXMNy7B" +
                "VzXfAfj6wffz+3MsfH/WyTceLEa//L8unIoNPcB/c/5bRr8e+e" +
                "8w/qvUfy3+2wf6raDfPs5X4/RANd+EHpCfc/l5kf3ZI/9dwn/R" +
                "b0f6rcFXNd86fD3an5fZnzVW6QPbLI0Z");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value9 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value9[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value10 = null;

    protected static void value10Init()
    {
        try
        {
            final int rows = 23;
            final int cols = 120;
            final int compressedBytes = 240;
            final int uncompressedBytes = 11041;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2c8KAVEUBnCdxu4+AZFsRPJngYSVB7CSpZW1PwsLL2CBnS" +
                "LKmpRXPG7SNKQoNN2v76uZW3fu3ZxfpzvTSEr9SEVfRMpSk5bk" +
                "AzMZidl7QrJStGPJXk3J3Z407ivikjRtO6b9PfXA/oK+jVSV+U" +
                "HE+no7VdN99PUO3/p6e/qGH9P7ZFW0w0o56tunL2MGrIG75++f" +
                "3q+GPH+hfUf0hfYd0xfad0JfaN8pfaF9Z/R15vtozhqwf5/6d8" +
                "H+hfZd0hfad0VfaN81faF9N/SF9t3SF9qX//exfY/0hfY90Rfa" +
                "90xfaN8LfcNP5ApjOfwQ");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value10 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value10[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int lookupValue(int row, int col)
    {
        if (row <= 39)
            return value[row][col];
        else if (row >= 40 && row <= 79)
            return value1[row-40][col];
        else if (row >= 80 && row <= 119)
            return value2[row-80][col];
        else if (row >= 120 && row <= 159)
            return value3[row-120][col];
        else if (row >= 160 && row <= 199)
            return value4[row-160][col];
        else if (row >= 200 && row <= 239)
            return value5[row-200][col];
        else if (row >= 240 && row <= 279)
            return value6[row-240][col];
        else if (row >= 280 && row <= 319)
            return value7[row-280][col];
        else if (row >= 320 && row <= 359)
            return value8[row-320][col];
        else if (row >= 360 && row <= 399)
            return value9[row-360][col];
        else if (row >= 400)
            return value10[row-400][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value10 lookup");
    }

    static
    {
        sigmapInit();
        sigmap1Init();
        sigmap2Init();
        sigmap3Init();
        sigmap4Init();
        sigmap5Init();
        valueInit();
        value1Init();
        value2Init();
        value3Init();
        value4Init();
        value5Init();
        value6Init();
        value7Init();
        value8Init();
        value9Init();
        value10Init();
    }
    }

    /**
     * The error recovery table.
     * <p>
     * See {@link #attemptToRecoverFromSyntaxError()} for a description of the
     * error recovery algorithm.
     * <p>
     * This table takes the state on top of the stack and the current lookahead
     * symbol and returns what action should be taken.  The result value should
     * be interpreted as follows:
     * <ul>
     *   <li> If <code>result & ACTION_MASK == DISCARD_STATE_ACTION</code>,
     *        pop a symbol from the parser stacks; a &quot;known&quot; sequence
     *        of symbols has not been found.
     *   <li> If <code>result & ACTION_MASK == DISCARD_TERMINAL_ACTION</code>,
     *        a &quot;known&quot; sequence of symbols has been found, and we
     *        are looking for the error lookahead symbol.  Shift the terminal.
     *   <li> If <code>result & ACTION_MASK == RECOVER_ACTION</code>, we have
     *        matched the error recovery production
     *        <code>Production.values[result & VALUE_MASK]</code>, so reduce
     *        by that production (including the lookahead symbol), and then
     *        continue with normal parsing.
     * </ul>
     * If it is not possible to recover from a syntax error, either the state
     * stack will be emptied or the end of input will be reached before a
     * RECOVER_ACTION is found.
     *
     * @return a code for the action to take (see above)
     */
    protected static final class RecoveryTable
    {
        protected static int getRecoveryCode(int state, org.eclipse.photran.internal.core.lexer.Token lookahead)
        {
            assert 0 <= state && state < Parser.NUM_STATES;
            assert lookahead != null;

            Integer index = Parser.terminalIndices.get(lookahead.getTerminal());
            if (index == null)
                return 0;
            else
                return get(state, index);
        }

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 23, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 38, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246 };

    public static int get(int row, int col)
    {
        if (isErrorEntry(row, col))
            return 0;
        else if (columnmap[col] % 2 == 0)
            return lookupValue(rowmap[row], columnmap[col]/2) >>> 16;
        else
            return lookupValue(rowmap[row], columnmap[col]/2) & 0xFFFF;
    }

    protected static boolean isErrorEntry(int row, int col)
    {
        final int INT_BITS = 32;
        int sigmapRow = row;

        int sigmapCol = col / INT_BITS;
        int bitNumberFromLeft = col % INT_BITS;
        int sigmapMask = 0x1 << (INT_BITS - bitNumberFromLeft - 1);

        return (lookupSigmap(sigmapRow, sigmapCol) & sigmapMask) == 0;
    }

    protected static int[][] sigmap = null;

    protected static void sigmapInit()
    {
        try
        {
            final int rows = 1218;
            final int cols = 8;
            final int compressedBytes = 129;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3ckNwCAMBED6L5qlA/PEyDMF5MPiI4qUpLIXTBb3A/l1vw" +
                "A+q3+356u/8gUAYL4EsP/ZP3G+AEzrT+Yn+fF+w/k7X+QD3F/1" +
                "AfnSP0H+kS8A0F/1VwDA/AEAAOZfAPUTAAAAsL8DAAAAAAAAAA" +
                "AAAEDt9f9b2n/ffwCvJDjd");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap1 = null;

    protected static void sigmap1Init()
    {
        try
        {
            final int rows = 1218;
            final int cols = 8;
            final int compressedBytes = 71;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt1DENAAAIBLH3LxpwQJhJK+GGSwAAAAAAAAAAAAAAAAAAAA" +
                "AAAOCuVyUQgH8DAAAAAAAAAAAAAAAAAAAAAMAfA13BPcE=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap1 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap1[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] sigmap2 = null;

    protected static void sigmap2Init()
    {
        try
        {
            final int rows = 822;
            final int cols = 8;
            final int compressedBytes = 60;
            final int uncompressedBytes = 26305;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt1LERAAAEADH7D40NFDouWeC7jwAAAACuq1G+7wP+BAAAAA" +
                "AAAAAAAAAAAAAAAAAAAADAUgO0MVyh");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            sigmap2 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                sigmap2[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int lookupSigmap(int row, int col)
    {
        if (row <= 1217)
            return sigmap[row][col];
        else if (row >= 1218 && row <= 2435)
            return sigmap1[row-1218][col];
        else if (row >= 2436)
            return sigmap2[row-2436][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in sigmap2 lookup");
    }

    protected static int[][] value = null;

    protected static void valueInit()
    {
        try
        {
            final int rows = 39;
            final int cols = 124;
            final int compressedBytes = 179;
            final int uncompressedBytes = 19345;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2ksOgjAARdHO/P9QBF0Q6+rS3YCJMmzfyZ0SBu8EEhpqqT" +
                "9aNrWoj8of1/DmLd7iLd7iLd7iLd7iLd7izfuL97DunsvZrk17" +
                "z3aK8t7ZKcr7aKco74Odorwfdory3topynuyk+db3Xrf7BTlfb" +
                "GT83P5/hZvNef9slOU995OUd5PO0V5v+0U5X21U5L32vzP1Pjz" +
                "fbJTlPfdTs5b1K33aKco78FO3udq07t8AJRXyng=");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int[][] value1 = null;

    protected static void value1Init()
    {
        try
        {
            final int rows = 1;
            final int cols = 124;
            final int compressedBytes = 22;
            final int uncompressedBytes = 497;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNprYGggAN24GxhG4fCAQAAAym17Ug==");
            
            byte[] buffer = new byte[uncompressedBytes];
            Inflater inflater = new Inflater();
            inflater.setInput(decoded, 0, compressedBytes);
            inflater.inflate(buffer);
            inflater.end();
            
            value1 = new int[rows][cols];
            for (int index = 0; index < uncompressedBytes-1; index += 4)
            {
                int byte1 = 0x000000FF & (int)buffer[index + 0];
                int byte2 = 0x000000FF & (int)buffer[index + 1];
                int byte3 = 0x000000FF & (int)buffer[index + 2];
                int byte4 = 0x000000FF & (int)buffer[index + 3];
                
                int element = index / 4;
                int row = element / cols;
                int col = element % cols;
                value1[row][col] = byte1 << 24 | byte2 << 16 | byte3 << 8 | byte4;
            }
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    protected static int lookupValue(int row, int col)
    {
        if (row <= 38)
            return value[row][col];
        else if (row >= 39)
            return value1[row-39][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value1 lookup");
    }

    static
    {
        sigmapInit();
        sigmap1Init();
        sigmap2Init();
        valueInit();
        value1Init();
    }
    }

}
