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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 18, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 109, 189, 190, 0, 191, 192, 101, 1, 35, 30, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 57, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 3, 57, 1, 8, 123, 4, 124, 15, 127, 5, 221, 125, 237, 6, 7, 128, 126, 173, 0, 238, 209, 8, 212, 214, 239, 215, 88, 30, 9, 216, 217, 218, 219, 101, 30, 113, 10, 220, 11, 240, 222, 12, 13, 227, 0, 14, 228, 2, 129, 230, 150, 241, 231, 242, 15, 16, 243, 30, 244, 17, 245, 246, 247, 29, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 259, 137, 260, 261, 262, 263, 22, 23, 264, 265, 24, 266, 267, 3, 25, 268, 269, 270, 26, 27, 152, 154, 28, 243, 271, 272, 237, 241, 273, 274, 4, 275, 276, 39, 29, 39, 244, 277, 278, 279, 0, 88, 39, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 56, 291, 30, 292, 293, 156, 6, 294, 295, 296, 245, 297, 298, 299, 238, 300, 301, 103, 302, 7, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 30, 39, 313, 31, 314, 315, 32, 316, 5, 317, 33, 318, 319, 320, 0, 1, 2, 321, 322, 323, 30, 34, 324, 325, 57, 326, 239, 327, 144, 328, 8, 329, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 330, 255, 256, 331, 242, 9, 173, 10, 332, 333, 35, 334, 88, 335, 257, 336, 337, 338, 258, 180, 250, 259, 339, 340, 341, 263, 265, 342, 343, 101, 344, 345, 346, 347, 348, 349, 11, 36, 37, 350, 12, 13, 14, 15, 0, 351, 352, 16, 17, 18, 19, 20, 353, 354, 0, 355, 21, 356, 22, 23, 24, 38, 39, 26, 40, 28, 267, 41, 42, 31, 357, 358, 359, 360, 361, 362, 363, 32, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 33, 34, 393, 394, 395, 396, 43, 44, 45, 46, 397, 35, 36, 398, 37, 40, 41, 42, 399, 400, 401, 47, 48, 49, 402, 50, 51, 52, 53, 54, 1, 403, 404, 405, 406, 407, 55, 56, 2, 58, 59, 60, 408, 61, 3, 62, 409, 63, 64, 65, 0, 410, 66, 411, 67, 68, 47, 4, 412, 69, 70, 413, 6, 71, 414, 415, 3, 416, 4, 48, 72, 5, 417, 73, 418, 6, 419, 74, 420, 421, 75, 76, 7, 422, 77, 78, 423, 49, 50, 424, 79, 8, 80, 81, 425, 82, 426, 427, 1, 428, 429, 430, 431, 432, 433, 123, 83, 84, 434, 85, 435, 86, 9, 87, 53, 88, 10, 89, 0, 90, 436, 8, 11, 91, 92, 93, 12, 94, 95, 1, 96, 97, 98, 13, 99, 14, 0, 100, 437, 102, 104, 105, 106, 107, 438, 108, 109, 439, 110, 111, 112, 113, 440, 114, 441, 442, 443, 116, 15, 444, 445, 446, 447, 448, 449, 450, 117, 118, 451, 119, 452, 120, 18, 121, 181, 453, 454, 8, 455, 122, 123, 19, 124, 126, 456, 457, 458, 459, 127, 129, 130, 25, 131, 20, 132, 15, 133, 134, 460, 21, 461, 462, 463, 128, 464, 465, 466, 467, 135, 136, 54, 0, 137, 138, 139, 140, 141, 468, 142, 22, 469, 470, 471, 472, 143, 55, 145, 116, 146, 147, 148, 473, 474, 475, 149, 150, 151, 152, 23, 8, 153, 476, 477, 478, 479, 480, 481, 101, 482, 483, 154, 484, 485, 155, 56, 486, 487, 156, 488, 489, 490, 491, 492, 157, 493, 494, 251, 495, 496, 173, 169, 158, 497, 498, 499, 500, 501, 159, 502, 503, 160, 504, 505, 506, 507, 161, 508, 2, 509, 510, 56, 162, 511, 512, 513, 514, 515, 516, 517, 163, 518, 519, 520, 164, 165, 521, 522, 523, 101, 166, 524, 525, 170, 526, 527, 167, 528, 529, 530, 15, 260, 27, 531, 168, 532, 261, 533, 262, 534, 268, 535, 270, 18, 171, 172, 174, 175, 24, 176, 536, 537, 538, 177, 539, 271, 540, 541, 542, 15, 276, 543, 7, 8, 57, 9, 10, 544, 11, 545, 546, 547, 16, 143, 548, 18, 178, 549, 277, 550, 58, 0, 3, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 562, 28, 563, 148, 564, 565, 566, 30, 168, 567, 568, 569, 174, 570, 31, 571, 32, 17, 572, 573, 574, 179, 180, 575, 181, 576, 182, 577, 183, 578, 579, 1, 580, 280, 3, 581, 285, 582, 583, 584, 585, 586, 587, 39, 588, 589, 590, 591, 592, 593, 594, 43, 44, 45, 46, 595, 596, 597, 598, 599, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 610, 611, 612, 613, 47, 614, 615, 616, 617, 618, 619, 620, 621, 622, 623, 624, 625, 626, 627, 628, 629, 630, 48, 49, 63, 66, 59, 631, 50, 77, 632, 633, 4, 634, 184, 635, 636, 185, 637, 638, 639, 640, 5, 641, 6, 642, 12, 14, 643, 644, 645, 646, 26, 647, 648, 649, 186, 650, 651, 187, 188, 652, 78, 653, 654, 655, 656, 657, 658, 189, 190, 659, 191, 660, 182, 661, 192, 15, 662, 663, 664, 665, 666, 667, 80, 81, 668, 669, 670, 82, 671, 87, 88, 94, 95, 193, 672, 100, 673, 674, 2, 675, 101, 102, 103, 33, 20, 676, 677, 194, 678, 679, 112, 114, 115, 117, 118, 60, 680, 681, 682, 683, 684, 685, 686, 119, 7, 21, 22, 687, 688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 701, 125, 4, 702, 703, 704, 134, 135, 133, 705, 136, 61, 195, 143, 144, 145, 147, 706, 148, 153, 154, 707, 155, 156, 157, 708, 6, 158, 159, 160, 196, 197, 62, 198, 199, 709, 64, 185, 65, 67, 68, 69, 710, 711, 8, 9, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 179, 731, 732, 733, 734, 735, 736, 737, 738, 739, 161, 740, 162, 741, 742, 743, 163, 744, 745, 746, 747, 748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 28, 29, 30, 758, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, 769, 770, 771, 24, 25, 26, 27, 772, 773, 774, 775, 776, 164, 777, 165, 778, 166, 205, 167, 779, 200, 780, 201, 781, 782, 168, 783, 34, 784, 785, 786, 787, 788, 210, 789, 169, 790, 791, 792, 793, 794, 795, 796, 797, 798, 170, 799, 800, 801, 802, 173, 803, 804, 805, 806, 807, 808, 10, 809, 810, 811, 812, 813, 814, 815, 70, 7, 176, 177, 816, 817, 818, 819, 820, 821, 822, 823, 824, 825, 178, 35, 184, 185, 826, 186, 187, 202, 1, 188, 71, 189, 190, 191, 193, 195, 73, 196, 197, 198, 199, 203, 204, 205, 206, 827, 828, 829, 207, 830, 0, 831, 35, 32, 832, 833, 834, 208, 210, 74, 211, 212, 75, 290, 835, 43, 836, 213, 214, 215, 217, 219, 220, 221, 837, 222, 203, 838, 204, 839, 840, 841, 842, 843, 35, 223, 76, 844, 845, 224, 225, 8, 846, 225, 847, 226, 227, 77, 848, 275, 849, 228, 229, 230, 231, 850, 851, 291, 852, 205, 853, 232, 233, 234, 854, 855, 206, 208, 856, 209, 857, 858, 210, 859, 860, 861, 862, 211, 863, 864, 45, 213, 214, 865, 866, 218, 215, 867, 868, 869, 870, 217, 871, 220, 872, 873, 874, 44, 221, 875, 222, 876, 877, 878, 879, 78, 235, 236, 880, 79, 35, 46, 83, 84, 47, 50, 85, 51, 86, 52, 881, 238, 237, 239, 882, 883, 223, 884, 240, 885, 224, 886, 225, 887, 888, 57, 88, 36, 244, 246, 37, 294, 101, 226, 889, 38, 890, 227, 891, 39, 242, 247, 2, 40, 250, 80, 251, 254, 41, 255, 892, 248, 893, 894, 895, 1, 896, 301, 897, 898, 243, 53, 228, 899, 900, 249, 253, 258, 229, 901, 902, 231, 903, 904, 232, 905, 906, 233, 81, 257, 259, 264, 54, 265, 267, 0, 234, 268, 269, 907, 270, 271, 272, 235, 908, 909, 910, 273, 274, 278, 276, 279, 280, 281, 282, 283, 284, 285, 286, 287, 1, 911, 288, 289, 290, 291, 292, 293, 912, 294, 913, 914, 295, 296, 915, 916, 297, 298, 917, 299, 300, 301, 918, 302, 303, 55, 56, 59, 60, 61, 62, 64, 65, 67, 68, 69, 70, 71, 74, 919, 920, 42, 75, 921, 922, 923, 924, 925, 926, 927, 928, 929, 930, 304, 305, 931, 932, 933, 934, 935, 936, 937, 938, 939, 940, 941, 942, 943, 944, 945, 306, 946, 236, 0, 947, 307, 948, 308, 949, 309, 950, 76, 951, 952, 953, 238, 245, 310, 311, 240, 312, 297, 313, 314, 954, 955, 315, 316, 317, 318, 956, 241, 319, 321, 322, 324, 326, 109, 327, 329, 43, 330, 957, 320, 323, 325, 331, 334, 958, 328, 959, 960, 961, 247, 336, 337, 338, 339, 962, 963, 964, 340, 965, 341, 44, 333, 89, 335, 342, 343, 344, 345, 90, 91, 346, 966, 347, 967, 250, 968, 348, 349, 969, 350, 353, 361, 362, 2, 970, 971, 972, 973, 365, 367, 368, 375, 82, 381, 974, 392, 382, 385, 387, 388, 393, 395, 89, 396, 397, 310, 398, 400, 313, 401, 975, 976, 402, 403, 977, 978, 404, 979, 980, 405, 981, 407, 11, 982, 983, 408, 410, 92, 93, 96, 412, 90, 984, 985, 986, 251, 91, 252, 987, 988, 989, 406, 990, 3, 991, 992, 993, 994, 995, 92, 996, 97, 997, 998, 999, 409, 1000, 4, 1001, 1002, 413, 1003, 1004, 96, 6, 1005, 1006, 1007, 98, 1008, 1009, 1010, 1011, 254, 1012, 97, 98, 1013, 259, 1014, 260, 1015, 414, 415, 417, 420, 421, 422, 423, 45, 0, 425, 1, 426, 2, 427, 428, 429, 430, 431, 433, 99, 434, 435, 436, 437, 438, 439, 440, 441, 443, 444, 445, 446, 447, 449, 451, 452, 453, 454, 3, 261, 455, 457, 459, 458, 460, 461, 462, 463, 464, 465, 466, 468, 469, 470, 46, 432, 99, 2, 47, 471, 472, 473, 474, 262, 475, 263, 476, 478, 483, 1016, 112, 484, 485, 486, 4, 264, 477, 479, 488, 480, 5, 490, 1017, 492, 481, 265, 267, 482, 487, 489, 491, 493, 494, 495, 1018, 269, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 1019, 1020, 510, 511, 1021, 1022, 1023, 270, 512, 513, 3, 114, 115, 514, 1024, 515, 1025, 1026, 1027, 1, 4, 516, 517, 116, 100, 518, 519, 1028, 520, 521, 113, 48, 1029, 1030, 522, 523, 524, 1031, 271, 1032, 1033, 272, 525, 1034, 277, 7, 1035, 1036, 279, 1037, 1038, 1039, 117, 526, 528, 527, 1040, 529, 531, 1041, 281, 1042, 533, 303, 1043, 534, 1044, 282, 283, 536, 537, 539, 1045, 1046, 1047, 1048, 535, 1049, 1050, 1051, 284, 1052, 1053, 118, 1054, 0, 1055, 1056, 1057, 285, 1058, 1059, 1060, 1061, 1062, 1063, 120, 101, 102, 103, 121, 123, 124, 1064, 127, 129, 130, 131, 1065, 1066, 104, 1067, 1068, 49, 1069, 1070, 312, 1071, 538, 540, 541, 542, 543, 544, 545, 315, 1072, 132, 1073, 1074, 123, 50, 1075, 51, 1076, 5, 546, 547, 52, 548, 137, 549, 105, 550, 551, 124, 552, 1077, 1078, 320, 1079, 286, 1080, 1081, 553, 1082, 554, 555, 1083, 556, 1084, 1085, 289, 106, 1086, 107, 557, 558, 559, 560, 561, 562, 565, 1087, 1088, 563, 564, 566, 1089, 567, 1090, 570, 1091, 1092, 568, 1093, 1094, 1095, 569, 571, 572, 573, 1096, 1097, 1098, 138, 1099, 1100, 574, 1101, 1102, 1103, 575, 1104, 1105, 1106, 576, 577, 6, 7, 578, 580, 581, 583, 1107, 287, 1108, 1109, 1110, 288, 584, 1111, 292, 1112, 293, 586, 579, 1113, 1114, 1115, 588, 108, 589, 590, 591, 592, 593, 2, 1116, 1117, 1118, 125, 53, 594, 54, 596, 1119, 297, 598, 1120, 1121, 1122, 1123, 300, 597, 1124, 1125, 1126, 1127, 1128, 1129, 1130, 1131, 605, 606, 1132, 1133, 613, 615, 1134, 616, 302, 1135, 1136, 618, 620, 139, 304, 1137, 629, 1138, 1139, 140, 1140, 1, 1141, 1142, 599, 600, 1143, 631, 623, 109, 9, 601, 625, 12, 1144, 602, 1145, 1146, 1147, 1148, 305, 1149, 306, 1150, 141, 142, 634, 55, 1151, 1152, 1153, 1154, 1155, 635, 1156, 633, 1157, 637, 307, 638, 308, 640, 1158, 639, 110, 1159, 1160, 10, 641, 643, 644, 645, 646, 1161, 1162, 647, 1163, 648, 649, 311, 650, 111, 1164, 1165, 11, 1166, 652, 651, 310, 1167, 314, 1168, 653, 1169, 1170, 146, 1171, 149, 1172, 150, 1173, 316, 1174, 317, 318, 1175, 1176, 56, 603, 1177, 654, 1178, 1179, 1180, 1181, 0, 1182, 1183, 1184, 1185, 1186, 1187, 112, 332, 1188, 1189, 1190, 604, 607, 608, 57, 655, 1191, 656, 657, 1192, 658, 1193, 1194, 659, 1195, 1196, 1197, 1198, 151, 660, 661, 1199, 1200, 662, 663, 1201, 0, 1202, 1203, 1204, 8, 152, 167, 609, 610, 1205, 1206, 664, 171, 611, 612, 1207, 614, 1208, 172, 174, 1209, 342, 323, 1210, 665, 1211, 666, 1212, 668, 1213, 1214, 673, 671, 674, 1215, 12, 343, 1216, 617, 175, 1217, 675, 1218, 676, 344, 677, 345, 348, 1219, 349, 678, 1220, 1221, 325, 679, 681, 1222, 1, 1223, 1224, 350, 1225, 1226, 115, 1227, 116, 1228, 351, 1229, 352, 1230, 58, 3, 4, 621, 622, 1231, 126, 59, 365, 1232, 367, 626, 1233, 9, 1234, 179, 627, 630, 1235, 1236, 682, 180, 328, 684, 636, 685, 686, 687, 688, 127, 689, 1237, 368, 690, 117, 1238, 118, 1239, 1240, 1241, 181, 1242, 693, 13, 1243, 691, 692, 694, 1244, 695, 14, 696, 1245, 697, 1246, 15, 17, 18, 1247, 698, 1248, 1249, 1250, 1251, 182, 699, 1252, 1253, 700, 701, 1254, 702, 393, 703, 710, 332, 704, 706, 1255, 1256, 1257, 711, 708, 712, 713, 2, 128, 60, 119, 714, 715, 716, 1258, 1259, 717, 1260, 375, 1261, 333, 120, 121, 0, 123, 124, 718, 719, 186, 61, 62, 720, 721, 63, 722, 187, 64, 723, 1262, 381, 1263, 724, 725, 726, 727, 728, 729, 730, 731, 732, 733, 1264, 734, 735, 1265, 736, 1266, 737, 125, 1267, 188, 187, 1268, 1269, 392, 738, 382, 1270, 739, 740, 1271, 127, 1272, 1273, 741, 1274, 19, 131, 394, 1275, 1276, 742, 743, 744, 8, 1277, 1278, 1279, 20, 397, 132, 1280, 745, 747, 1281, 385, 189, 190, 191, 2, 387, 388, 1282, 746, 1283, 1284, 748, 395, 749, 65, 750, 192, 751, 752, 135, 753, 754, 755, 1285, 756, 757, 758, 1286, 1287, 136, 1288, 1289, 1290, 1291, 759, 760, 1292, 761, 396, 1293, 1294, 1295, 193, 762, 763, 764, 1296, 765, 195, 766, 1297, 1298, 767, 1299, 768, 1300, 399, 769, 770, 771, 335, 772, 9, 196, 773, 10, 11, 1301, 774, 775, 1302, 1303, 1304, 398, 1305, 400, 1306, 401, 1307, 1308, 402, 1309, 1310, 137, 1311, 138, 1312, 1313, 1314, 1315, 1316, 345, 197, 776, 1317, 347, 129, 404, 66, 350, 1318, 777, 778, 779, 780, 781, 783, 1319, 782, 1320, 1321, 784, 785, 1322, 130, 67, 786, 1323, 1324, 1325, 198, 199, 787, 788, 1326, 789, 790, 1327, 798, 791, 1328, 1329, 1330, 792, 1331, 1332, 1333, 1334, 1335, 413, 10, 793, 11, 12, 1336, 1337, 794, 795, 796, 21, 22, 202, 797, 1338, 203, 1339, 68, 799, 1340, 800, 1341, 1342, 1343, 801, 1344, 802, 1345, 803, 1346, 804, 1347, 805, 806, 807, 809, 420, 808, 1348, 141, 1349, 810, 13, 1350, 23, 811, 142, 1351, 1352, 1353, 1354, 1355, 422, 812, 14, 1356, 143, 424, 1357, 1358, 1359, 1360, 1361, 425, 813, 1362, 421, 1363, 427, 429, 1364, 1365, 430, 1366, 1367, 1368, 1369, 1370, 6, 13, 1371, 1372, 1373, 204, 1374, 814, 815, 816, 817, 1375, 818, 832, 338, 12, 205, 206, 1376, 819, 820, 823, 13, 825, 826, 354, 1377, 431, 433, 15, 1378, 17, 1379, 207, 1380, 1381, 434, 1382, 1383, 1384, 144, 145, 7, 8, 827, 828, 829, 831, 435, 833, 351, 1385, 1386, 436, 355, 1387, 1388, 356, 830, 14, 834, 208, 835, 1389, 69, 210, 211, 438, 437, 836, 837, 838, 1390, 1391, 1392, 839, 842, 1393, 1394, 1395, 1396, 1397, 1398, 1399, 15, 843, 1400, 1401, 840, 841, 844, 1402, 1403, 341, 212, 216, 223, 1404, 1405, 1406, 188, 1407, 1408, 24, 439, 1409, 1410, 1411, 1412, 440, 442, 845, 441, 1413, 1414, 846, 1415, 1416, 1417, 1418, 443, 449, 847, 451, 1419, 1420, 242, 190, 1421, 1422, 70, 848, 849, 1423, 0, 243, 850, 851, 452, 244, 1424, 852, 853, 855, 1425, 854, 1426, 1427, 856, 857, 859, 860, 1428, 861, 858, 392, 1429, 1430, 863, 1431, 864, 1432, 453, 1433, 1434, 1435, 1436, 352, 357, 358, 1437, 71, 454, 455, 359, 865, 862, 866, 867, 868, 869, 870, 1438, 456, 18, 1439, 147, 148, 1440, 1441, 1442, 871, 1443, 1444, 1445, 1446, 1447, 872, 16, 873, 874, 875, 876, 1448, 877, 457, 1449, 1450, 878, 879, 880, 460, 1451, 1452, 461, 462, 881, 458, 1453, 1454, 149, 1455, 882, 463, 883, 459, 1456, 1457, 152, 1458, 464, 1459, 1460, 1461, 150, 884, 1462, 465, 885, 1463, 886, 1464, 887, 888, 466, 889, 890, 891, 892, 893, 468, 1465, 360, 363, 1466, 1467, 894, 393, 895, 1468, 151, 153, 154, 1469, 1470, 896, 897, 898, 899, 900, 901, 1471, 1472, 1473, 1474, 1475, 902, 1476, 903, 1477, 469, 1478, 1479, 155, 1480, 1481, 25, 1482, 157, 1483, 1484, 26, 194, 904, 1485, 2, 1, 1486, 905, 906, 907, 908, 398, 364, 366, 369, 470, 475, 914, 403, 1487, 1488, 1489, 245, 246, 1490, 915, 916, 1491, 917, 1492, 919, 1493, 1494, 944, 946, 247, 1495, 1496, 27, 493, 1497, 1498, 28, 494, 1499, 1500, 248, 158, 948, 950, 926, 370, 1501, 371, 931, 249, 250, 251, 489, 495, 253, 254, 255, 1502, 1503, 947, 1504, 949, 952, 497, 1505, 1506, 501, 502, 1507, 1508, 503, 953, 14, 954, 498, 504, 505, 508, 1509, 1510, 955, 956, 958, 256, 258, 1511, 515, 1512, 1513, 518, 1514, 262, 372, 1515, 1516, 1517, 959, 960, 1518, 1519, 961 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 6, 1, 21, 2, 22, 2, 23, 24, 25, 2, 2, 7, 26, 0, 27, 28, 29, 30, 31, 32, 8, 33, 34, 0, 35, 29, 36, 37, 38, 39, 9, 2, 6, 9, 40, 18, 41, 42, 43, 31, 44, 45, 18, 46, 47, 29, 48, 32, 49, 31, 1, 38, 50, 4, 51, 34, 52, 53, 38, 54, 40, 55, 56, 57, 58, 59, 60, 61, 0, 62, 63, 64, 2, 65, 3, 66, 67, 41, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 41, 80, 81, 45, 8, 82, 56, 83, 84, 0, 85, 59, 86, 49, 87, 88, 89, 90, 59, 3, 91, 0, 92, 93, 2, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 68, 69, 107, 108, 109, 5, 70, 110, 111, 72, 112, 73, 4, 113, 4, 32, 114, 115, 24, 116, 117, 3, 118, 14, 3, 74, 119, 120, 121, 122, 123, 6, 124, 125, 126, 127, 128, 129, 130, 17, 131, 6, 75, 8, 132, 133, 90, 91, 134, 135, 136, 100, 137, 106, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 107, 152, 2, 111, 54, 153, 154, 155, 156, 1, 157, 3, 158, 159, 0, 160, 161, 162, 163, 164, 6, 3, 165, 166, 0, 167 };

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
            final int compressedBytes = 3319;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXbtvHscRn1sdlZPy8ImgJCLVUZYVBlAhp3FSMFrKdiwbCE" +
                "JQEmAjIAKkSFSwClxKwJERAjIVCxUqP6gQ4M5/AmGoCFSpiJFA" +
                "Vf6U3ON73GN3f7M7dx/ps0yRmm/2Ne+ZneO79/7y6No3ty89XD" +
                "m4/e2PXu6d3n3yK/3s8++v5A/ebH2ySu/+cOvRtePHn6+tHOwX" +
                "8EclfPvZ77+/cvLgXQ2/+ej68eaXz0v4xy8fvf7oyW6Ff/Lgv1" +
                "t6APz3boH1yeZXxX8pUUTlc7H4f33nTvE1J9JqJaYB9j/y+QD8" +
                "6snI8UTdf0ibP0jnP/3pzU9WDjaTNaLb324fXj29uLlLefJpMY" +
                "mmpZyPHH7tePN+Nf+FYv6Pnqy2+bOCJ2slfzbh+YN/sdYP+Rvt" +
                "/69714/3//jvD/6+//KzV396fffJnz/+x95/SvjTAn75Ml1dTR" +
                "KilQNNKtP04depfvbpSZTvHm2V9B1ZvhB+yR/xweb685I/osOr" +
                "ry9urvrxh2x9P7+sivO5HxXnU8h/cT6/+fqOfrZdns8/t2I69/" +
                "LN5F8Df+5W/In0w/j6HcnPyPZBiF/zT9Lin+0G/1BC9dfpU/yQ" +
                "qun3mjj2ZVz9p5rE1o2vU8tw1vpZzJ9i/Xr29mdMeO/Ju+crPZ" +
                "9zfr5i/hDKB5i/rT/imX+mm/zv8k/eLPTrvkn/2/Xv7kD2TQgf" +
                "gD5O/03uX8dm/zli6adihJW5w10FIw/T4msZf9AKyf2PKf3v2+" +
                "z/+PHTuPZ36j/W8UVUxRee/uMvpv7zwf5k7j/X+3s647/jen2T" +
                "en03hvWPx9aPgH5axUb6pdurJf3E8D5/ZE3+QPiG+LQbv8au+B" +
                "XBUXyK4N/V8Dp+2a7ilzk8GgAujZ+l4/vgT2r8ez7r4+gPd/wN" +
                "9EfDvk1qfNWML6TxMcIPgFNr/pHj5xl+qP4can4bfhM+qeGqCT" +
                "fF7834C9FvbPzzCNcy/KyVH7H5jycL/8KVf6js0zf7rfPdrvlj" +
                "Yf8c8IILYpS/NMv/zP/l0DcJ5o+A8TOf/IEU3pbPV5V83pvK59" +
                "Oe/Pbh0vG7+NTAp7D9kWT+3vimoFjz8w/fNf3Tjn2Nqvk14Se3" +
                "QlD+xuIU8Z/EDZbGr2cNN/FHKz8y/PwUvj5rfGeN75two/4T6l" +
                "+UP7DHx/Xfdv9sl+XfGcandvztjn9Q/D0dSC1ykHFLlBC+FN7Z" +
                "n+qK8gjzkw++NL/QxDfxZ8M/poX/XNnHF1vEiC9J/cQVXyA4I/" +
                "8hGv/yF9b9Vf6/NL+B8JvwSQ1v+dcepigQroGRAfZRyewXXF8S" +
                "sNFosW6VuPlzLs5poprDJotvVGsheceUV/5DtvhESxvGJTynuF" +
                "RfNX6S03r592aFk4jpV+xPpWm5P1Xv70W5v7T41Gm9v2Q4+igz" +
                "vvN84Pog/bVs/XP8xOyKAf5C+m0hH2m1W3/+NvFPOv1gjNdX8n" +
                "fzfNe79LfxZz0+3p9bAbQMYvXDzlR45vZzWPlG+LmRvnHw+Oz5" +
                "Exd+PJ7+00BmAX0Z8U/k+Pdw/ZPz5BPSZ6a/yaK/NaR/bJlXjc" +
                "Gf1JRvzvl4R4xe+k28P4L6VdVXCLr2IePBpfZPKl8JkF9on5B+" +
                "D7VvPPvE0O+QhSTnB9cH7Beir9m/c+ifvCN/cv2P+Fvo/2oDcR" +
                "opusSGz7MPPbjU/8q5+oc89YtF/ofm/+74qD4K+B/fX4hiSX1V" +
                "Wp9E/PH++juVrtHKjfSiin5LtPHi7QZl6SSh/91YuZkz86dz/5" +
                "hHf83XSGH0Wci/tL7IY/TEsV3tllJwvmPfz0L1ySbclL9Y1vps" +
                "9cvq/Fg63cKfbP623N8T1i/7+uuhJf60sBriH1A/ROuz5pfT/v" +
                "it/CJz/ID6Y//+pAsf0veeU3/h/Lh2h39e8n3+6oMB+OSHrzn+" +
                "WXiWC9I/bS+7E196r99Tf8D6JcnsA9RfS6mvcgqkCnzYDJfXFz" +
                "3p13Na3fkJSB8wfw78K6j/7Mop5eFr+/ys/JJ2yqnn/V9OfY48" +
                "63/kqj8a6pOm3N8Mrgz4ZwofoD7rPP8A+vnVn2DeMiS/khryk4" +
                "H+H1v/NuUnYdsnQX3zRef+IxdOnfovqr86/VcDPoJ71YdRfdcA" +
                "pyZcXr+W1Yeb/JEa4xNy1xexZ6RKDtsoMHr5qwuY/9D9l6l/f4" +
                "E+sMUPMvni5ufY+a+ef6GB/8HF93J22PU9WP9j5/9UUH2pmEd2" +
                "Pps1YsF/eZwX685VmqRZQlGxjR8b6kN5d/yckX934Z+615fohv" +
                "EzhUCIvsLzobdt/On6+WUbbWA5j/y+f33xjk9+VTr/PD9ZTG7K" +
                "T76//qYNX6/h1IUnZrhKvqACHlXU/1mx0fW3M/m6Mc0v5jVtuv" +
                "qXp5/h+OX+Y9f+3ftD60P7R+tH9o19/h36EZ9+1fndKPbogqse" +
                "/JRFf7R/tD6V3HPSl38+Koh/Z+sP40/MHyz+c8zfP5+dvnyx1n" +
                "/ZvX7L+SH5Q/oF17eE/kHiTmHh+z/APzwCcNoB+nnHT3/7wlF9" +
                "UWp/4f0dZL+59j0Mjv2/2afV4v4V8fuXvevDnvDB58+B/OSe8i" +
                "X0z3F9BfZngPsDIfkLWt7+xeO74Th/NPL64f00Pf75p076A/2N" +
                "+GdnmoNTyhxfDMw/OTGfeaK8/FNmstryEedR/ZGh7Kvvw+QfGN" +
                "+yJovZ/Jkbzz8Op8+cP1zxZ8w+X7P9j+32WbY/eH9lPn8gfwTJ" +
                "33D6F/X/SO03//5LWHwflr+S6vcl+i/G+3s+9rtTP2Tz34QVn4" +
                "v5e1rIzBb6Ia3RJm58lS2qoDklJdLJov6hz43/IPavPPlTmh/z" +
                "ta+B62f7B4X9yyma/PLwcTHlV/lqHj2fFOr3d5tZnF8wlOvYFY" +
                "Hl+J9y+vHpo0bxH8f27zmMavi8jpcUP56WxxpnlXFprifzhKsw" +
                "+Dy+s9TX4PyaWvq1P37XP0pxfSf1OB90vm/b+HP/kLt/GN+C/D" +
                "nwHwr9U8ww2aBC/9ClrBjtJC/8b73pmt87/kf9IUuG9+wXH5+V" +
                "P1me/8S7XzmefsP9c2xDyXgyQX7HFp8tK79jnp+Rn3bnd4/oMH" +
                "P4t8WfKxUoLklUxCdZuaLtNMrTo3Xe+iv91b6f1zo/ALf57PlS" +
                "zj9ptWdmhpfmmOjj4Z9I8yNS+ZDbjxC4YusvY34W3Q/wyR+y42" +
                "s1Tv7RxB8D5t9Hjq/E72dG718S+6ee8R1+Uizfw9n34nwOJfcD" +
                "BqxfKCFcEN+4rLnQP8L1H2f+h9efLIAjIZbWx4T+4djyifrjPO" +
                "+/B/CH9P7n2dInoL+sJaIQn33+s/vX3enuCel3aw+8321P1J92" +
                "ruTL0P+E7Qd6/yTgb8/zySXyZ9pf6va/OPn1ZuTXzZ/DUdzrx/" +
                "1Zhvf3TO+PxAx89P4yjv2XzH/m+nPQ/MirMPvvzz96OPsj2H9M" +
                "cv0n17/u+hDqD5pxbtbYVG+Xzv4s9/jC9z8x3n8i6g8jYf8X7C" +
                "8D9Gf0p31Y54d6h3VUS41W0yN35X9c8DOov/DPF90PHNs/lfan" +
                "Sf1X1P2D1sfv/3P0J8d2+Eb6QhXwsn6iol8XHJnnEWXlBaEsK3" +
                "7gje+Cs/N3bvzg93dJ50fCw3v/Hvn13ywedP8F9r8kTEa05mc9" +
                "8T39zzD7t3jw72/kjm+m3xLur7j5xxceKF+YfwP5n5lft9ePR5" +
                "ZPtv6Qng/K1aH6+mD6q0t/Z/+KmD/F96dC8mfpcOuD+G9l9s96" +
                "/nX+dCM9mtrnmCr7TKV9LolT2ucjOL5Hf2RQ/gCN3+tf6vdnif" +
                "LLtv4vR39USzhg/xjwn9zyk/DuL8rqF775g9TLPnWYI7L4l054" +
                "47HBbfj8/r7u+bf3h+Fh/X0/HHgihP9g988KflWQfeTLN+IvB3" +
                "8618/oD2fKP9pf2P5R/+sA5+OED2d/zPZZHB/Pv7fdn0Jwd32Q" +
                "kT9j5occ/aFrgv5fo331iL9Q/y+m72Hi4u/u+Ib49frx46+q9X" +
                "1WrO/uk7914ksliQ/x/nNm/O2Tn0DnZ3p/dVh+4Nz9/vMuI42c" +
                "vxigfiS7nya+/yrM/w1yvtliqM7vv5Hin/n9Y/j7p+z1yxUaqH" +
                "/ZoZ/x+4/F9D103y/C/NXv303L/l1LBGPUb3b4fPxO/mjWH2yf" +
                "nwfnzd/X7zENsz8xXIfSh71/cvQXMP2fGOgnxu/PCfX/QuDEt7" +
                "+M/F1pQjK9kK9ys1nVH6mp138dnN9KgP9MwD8mnv/rm/8Mmr95" +
                "rNpwvvz8IfIf8e8/EPI/u//fyZ8xwj6v9SPYXy7O38nWt1z/+J" +
                "U3nCH/7vdb8+sXnv3nGdc/cPaHd6ePgH8UWeMjS/6zk1+4VM2f" +
                "WvMLkU0/9d7v3fBPYyh4hv7LCZc+lv7TCW98nB+QjW+tr00c+Z" +
                "3W+YPxU0v/qd/+rfkvdoAdnH8C60f8o8nryX3xkf+oDcY7Vfz5" +
                "3b9fkIM/Knxs/1bq//4f7TFSpg==");
            
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
            final int compressedBytes = 2925;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtHT1vVEdw3/LOPJwPFsSHlWohVnSFC5KGpCA8E0ggUgQyWA" +
                "oFUkoKqojSkdaWC0hFQUF5okCi4ydcIgrkyk2kyFV+SvZ9+O7e" +
                "x+7M7uzeGcQpSpybm32zs7PzubPv4Pbq5vmnw1+fD7Yfvfnh5e" +
                "bby1sb+e6tf049u/vvlfw0YxmzfzK2w1LOZP3DTLGV4r9D/U0B" +
                "PKCP7/n8UYUM4B/c/mrz3NOHt84U9B1/uTm+uvXN+u4vBX0HV6" +
                "4Hoe89gdd/KEf87kd8mPyJBJ98eD8LY+Mvmn4Yrmz65Qisbzlx" +
                "Od0/9eTlByaf7Y8KA3egL+sBUPU3HX9V4w8zjb/25pjGv7x1ur" +
                "RvauNdbd94d3+EW59Fz79//88Bnk/5b/Mv8P5He/8a7aMbHNaP" +
                "dv0RyD7IDr7E2W/oJ+D8x8WDU9PzwfVh+zX+Uj/9aPux3O8fAv" +
                "yF6SuoC7G/If8rEhztv0H4HCsfrvQj7b/3+Ej7TeU/1X9atHyY" +
                "9gc1fsonO8jPPtnxQfjMDvbbvxD9gH06qef3eu3EvcJ/OP7yQT" +
                "G/yn+4uzdf+w26bdxLfon2K0B8DuUHrHCeXWNCsAETnJcm6MUd" +
                "rRiEKuzaQK86z3gBT8r5FfCVJpw6Pqj/eu2PcF8+//Wz89doYh" +
                "RgglTT0XN0UTLQxCm/sMWZ/jnD33f6Q8Nd84Nd/x7YfznkH3vs" +
                "j5ntllUuisyn+MV2l1wePvtUOUyaFN/mBVk5WxeJEk9WAsTfwe" +
                "3LUZfv1td6hgMxXROt7O8J/W9VcHng7d+5+Ieiia+q77JQ+ps8" +
                "Pg0fFV9Zn5+D+Rn7+ER8OD4h5S/B/eeVfxNotDj5IeEB799AeP" +
                "npH5wuf2j6Dfwl0n/SlH+8+86cnxAR1sfoH9Ly9yxJcfkJ04eq" +
                "HxDz7+WvCsKf8Wer1wfben0ZW3uzvnN2vDTcYCq7IYqJIfRXdP" +
                "0Ixq9E+oDxIXjBv3R7uFLyLyn5d7rBPy//ziG+p+YP2WL51x0/" +
                "dXr+F8v87OnsZsIG2zo+1v7pd48vre+uP0vUxp9XUhgOyY+2Tw" +
                "/OvX7UoH+94u9BP/2B8an8zVja0mKCO6VfXOTzVSl/12r5+wOj" +
                "PyD6gP0DPh/t/9YMMIYtfvbp70o/9OrXpNqfvOvfQiarb1044B" +
                "gZx0c+37u+Rh2fhO+nH8P5J+PZ9e+xD33y6x8/vHL2L6LHL0T9" +
                "COvPw/xER1k9md0t7pE+Nj/olz9sPZ5Py6WpaFnDpP2k2vtTjW" +
                "+FZ30BoA+OD8H1jRRfUuAYthzl/KgDfWB+EMwv0vKXRjg+P2n7" +
                "UPNnfvLpUv+I7d9T9ZMP3CV/Ezl+JMZX9PxBjtjrmLR4rPxp9Z" +
                "Ed+ZTB8jN5aWRlF+UYhj/Q+MoKX15mOr7T3Bhs54zr+O7rxyLf" +
                "vVHEd0+uzCP/1Jif7P4WrM/a67tg/Tf6/vfNT+HWD4azI44PwO" +
                "WhEypVqop14ywTMiu9uk9m8w9ZY39/MPPPxrT5QfwZVogJG1b8" +
                "VVw0+EuVf+D8kkd+3fP5ps9+EPtggn+5csDFGTa4qJVP8j1jF1" +
                "7sX2BSjDL238XBqgLxL4gXXOuH4nwlT77V20EpvRWKBRpJ/T8h" +
                "8gsf4YuEA/KJkp8UE99ww/h7zfFXqvFZPf6i4ZB/CeHz7Gem4U" +
                "kR2rDPtZ5b2T+0/xcr/+BaC36nAQfpw/isqX99Cc8/boKX8zPB" +
                "ebZn5Q91/SD+I+gnPv8a6flB9qfVPkOqwzE+6uoX5Pkm3/4FIL" +
                "4C8xeO/kPfx1Kfg/x/cv8ru+MGVxj6XfxH6PlY/wUCuvonsvRP" +
                "6PyNHX+C8k/LjxTwNGZ8OQbmB8EP8+N1UUy2E6BI+oRv/t7Hf3" +
                "c4vxE0P+EOp55fid2f4Hd+QcwNTs3vUvO/eVF//EtmeguvyUSd" +
                "HS9lBdKN8heo/DG2/hCxP4By/ikwPHGOn2D9kPXlF9I2B7ipvl" +
                "P8k3b6I1NtxxRif0zwl2fwxQw+8HwAXtQHzj99eL+U35+0/F7d" +
                "+r2nvmIZP/L+hfNzPfZTTO0nlH8K0x+fAvszNtzu/9nw4f6isv" +
                "92wge1kPktkn/N+XfxIXhTnOr9LyPR7zw+Nv4U2v/2iT9762sO" +
                "8VsGrQ91fCi/n5P4B+JbMmyo8bv1WxHWv7LHv5P4TCyxaXzGJv" +
                "EZ2f4e/i0M5//A+jDAH0z8lSLCWRN96Pp2Zd9PTOzXRP6t8ova" +
                "n2nbP2r4J8D8BZJ+z/MlJjiv8BHnR1VD39X6LyvHbyy3VX5Yhz" +
                "6c/IP4SHjzfgrRzJ/Z/Gv6+nmu76jhH3Tv9xiFkQ/w/G+o+fXf" +
                "D+J3fnP62av6c26a7gfam/bvPOqDw/mtXv3JGV7/uH2UIzz2+Z" +
                "8o+HM+nxE1f+S2kAlKCMzjQ/hJcPxF838O+RUrXB3B+X2Ezw0u" +
                "CwlIRid2ftN/3FeCJbsj7f79OGSpCnK+zzf/93F95gs36XdH+6" +
                "CcVT7x+fOESw/5h/ZPZPoz7EKZ7Iv9/j063MLbueifsf1+QPT4" +
                "JvqB+cPn/5r0TfJTMqL8ufBXLRhOpG/IuObA6AJ7WGRPpP75My" +
                "Vklg8lUv4C0H+kz1cvenwq3Fc/NvKjaTT9Hfv+6uj2Bxt/Ec6n" +
                "WvYH3D9hl0/E/TSk+/+M/MpTJP9q+Wvkh1vrk7rGr85hPb7+1c" +
                "0vRpW/rMEyOftzhZpo7PvriXDn/iB0/mw01R9Bzncaz1cS9VOA" +
                "/Cyz5fdz6viG/aFq/UK9v4URz+cB8hd5f5LXH3N/qVt8i6jfCf" +
                "z4Uewvc9DvtPVB2D9k/Rtav2Uzfy32Hbq/inz+MPr9R/b9jdIP" +
                "uY0+w/1tz+r6D0AfeH8S8X4m8P4P4v1fiPou8X4kx/qhGe5w/1" +
                "BA/xnsz4/8fCJ9cP3Uzn/4/C14/wzCf5TTP9OW4Yb9x5RN74fh" +
                "7fth/OqrH9T9zIH449ffiLg/BLqfCJIfv/udwfMb/fiJyf8Qsw" +
                "zyiS9d8ssx6pumqI/mH6VNluUzUIVxMan9RZzxT8UhDVX8d6l6" +
                "dl73h8W9vyl2fwic/wwwPgXfyb+XsCwrpP+mNvrvF1Bu+bHY+Q" +
                "kqHO7PiyufVDj5/ibw/Tn7UH7F73zkxD7s984Pm78B+4ux56cE" +
                "oJ8jwf3pr/g309/NZ/uzTednvfJXiPwHb9h3n/ktG+YXoH/cMt" +
                "Eg/bdp278x+5dhzo855D8Xnh+293cHeL/yTmaVT2L/t+v43fw3" +
                "p+XfsPbV2/+jvf/BNb5SmLU46vEhQ98fGSC+A/rPIUcPm5/3w6" +
                "f6/1T/BvbPqP4blv5+/pT90duN/mg9UTHpj+6Hz/RPT+JzQ/4a" +
                "9r92aP0tuaG+hJbfGt+vvxfqH6b3/9rh+PtbI73fJ1j9yTf/Ad" +
                "ZX4PxRao8/rO8H87q/kOHHhxQ1SF8HX9g9xN6fGOFwf9ydpvw6" +
                "+3/Y+gD2AIeX/Pn3jzJIPxHf39Cq36Qz9Zs8hP0jv7/FRz851k" +
                "9Q92P0+w+o/sc591/a8n/u/aVN/iVd/eb0/tMElf92yU/j/Be4" +
                "/9FSP8fpF1N8FCT/YBFge3/nBL4EwLkvnNb/Sn1/Lnw/neF89w" +
                "gvXyT57Lc/6PXH1ye5n30JwB+K/xf7/W9R5G/6OXx/2fP6/TRv" +
                "2+8vg/23tPUg0WGK9XwL2T471F/c4f7nfyVD1Wfh/D8pvgiQvz" +
                "OwOsP7Nylh/cr4Qk5/ajk/kXmsHyb/YKi/vcPV3/rqf3j9QKV/" +
                "fv3nECNivZ8X6H+C61tWfIg++P2ZcfNnUP9JkPiDVH8PJb+Q/e" +
                "6vT5b3Jws20G4CL5f4Rbf/Q9qcEqh+8sSun/H9J1B9yTc+zHnN" +
                "Eu61vxHvt7fnb/Lq/rLW8/HnG2n6yf/9TgrpXxDXh+i/xHl/vM" +
                "v91fb65uLP38Stvzq8/4N75m+J98vYH0/Nn9PPbwHz/x/SMYYh");
            
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
            final int rows = 814;
            final int cols = 8;
            final int compressedBytes = 2026;
            final int uncompressedBytes = 26049;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXL1vHEUUfzvsXTaRUAbLJieqdWIhFy5QmkBhsoZEkALFcm" +
                "IJCvoUVIjSSGvLRUyVIkVKywUSnf8EC6VAqdwgIVf8KezunX23" +
                "ezvzezNvJj4xTWI/z+eb9/V7b5bosimaNj3z/4IoJUsryNoyKi" +
                "lVlFf/aX4saVT/u179hprfgf5gfRdP13bvHK1//3qw/9Pp1ye7" +
                "bx/s7RSHT/7+6NWzfzaLJUgnSrq70y7zNy21rO/2p7sf/7Fx8/" +
                "lgf+P0xsmPZw/37jfzl8/ebT5awkeY2c/nk1tqZSn7NqHBPtEw" +
                "L+iLXz7bOtx6lZQ7v22mLP6kE0ZcbkWrWc5miD+dsyjnxuf2V3" +
                "797ev788O1R+n+erZMtHG6dbByNlzfoTJ7XM1aJA3/pf1NrWTd" +
                "KkwvwB8DOji/it9JdyQ9Xn1p71ly5d+wvk5/TQ5tpn9GTi0h1/" +
                "vVJ/8t/qjJkpQnf+z6EevP3vkz5vqrGzyYkUBF+nk9Ulmve2Bk" +
                "uyb79VgcetpmeTFz+iWHPxdPK/199OLJcm0/bpzs1vp76/C72n" +
                "5cjPW3zH4htXFxe62av9I/tf34oJr/wd7S2H7sTOwXyfQvph/0" +
                "37/jTmfl1X9Vv1EVk9Kchir5nCgvy4RynWeU59UP+AIb5cO2Pu" +
                "3An3M73Ut/aBcFY/UvpPu/N3qn9DIN7tKQki+JVkfnq9X5V53/" +
                "vTtYK3nrk9Cv1n8ryvrvjS7a9Ded/QH+8c9HAcOuWvozM8jHzW" +
                "b/erp/L/nT5vlpbn5wkZH/sW3XP0L/me8p+cl3rd/vHL34oYkP" +
                "vqnig4d7P7f0u9w+98RPDv6BuL/hPIq04xIl48FyloE1k8vA/q" +
                "vQ/8P2WxZfIvt/No4fRq/r+CE5WHk7XF+6jB+oP37QPseO6UwH" +
                "uzT00+34LHOWXx49YXvyZWd+tBHp+sKM77W/Zoeq9/5L7+cYP8" +
                "gs+AFrfym4a5ieCel0XfTtNn9KV/+wMPCXbb+B/eHaf7Q+Auvr" +
                "1w+1/hsY8JO2/lPYaSzaBiPpx6e0M/6YSvx3Kf7Z1p8J8I8Sq3" +
                "36vZH/ryby/2tzvnb+9PVv6Y9e/1wb1+8fH5kcaGR/AP/E/hvD" +
                "vlnpDZxax7cd///Yamsyrv+B43e8P+v5I/kA/MH6CZ2PlS61f/" +
                "38m7YrfEJX8d8VPkE9+ITyhKgLjv61+Ldo/zL/lj8+GfSXz/5c" +
                "/C9ufxUHXw8V/2i/+f3wSR1u/zD+MuKXf4XAL+H+vfUjF18579" +
                "VPNAmksf07a/enTn/sX9jxIUz3sH+oBcT3s7n7of30h4IWwHC/" +
                "0fnJ1tcMUY1fTMevF5OrfNI3sPyXsfjXLx/8/BKKf5Ba8MRf4f" +
                "iAHhn/k9pnln+USvwLpJ9k/g3Ez1D9grj+gSMr5vhK2h/VH8D6" +
                "hGagfDpm2lF70vzgeEQ1maLaQ8rA5x30j3B+XN9QOMGjPYrUvn" +
                "+v+hUH/Hva36/+IDT+7m7/4trXgPUx1Fs/4APUO+C3cvlkyp8L" +
                "3QE/Z+MPbvl3vn49B/bfPr8pf5xN88dyfMk6v19+OwuU34bjs+" +
                "tfyJz/SPvwg5LZX0vjIw/5ccwf8fLbQgGNZl+t4/Pzq374jSv+" +
                "5Kx/MJ154ZSjfePJP3P/aftYw+WfruRfD1W//Gvm/ScQ31nzK6" +
                "mbfefwp3w/+ZOO/CfM+DYcfg/zEzL83psO8Cc17i+NX9n79/av" +
                "4uan4P2MfP6M/I81P3M2G58mTXzaqX8phPdfhi+g+B/nr4X5mc" +
                "x01TImvl1Y43u2fTaIwap+OanPTcf5Lxrnv6jJf71kxicB1meu" +
                "LxbG1z74Mcb/s2D4Pxh/3iPWzv438h8t+L/KFGldD1vR6yWOtq" +
                "uF6bLe1yBt4St2fML3/QTc38R/MI5vx/8hfi18n4TpgeyTBD9I" +
                "rfLnYL/C10/K8WGA30K6A2QWRT8Fwg99319J8U+h/DDk0zW+cx" +
                "sf6i+WfmL0jx3fI/zSF56Y5V/O5K9yiB8BPojwP+AfI/yL9T4k" +
                "BfJpoUvn98cvj3n6G79fEeKD2pN/rvgrzK9EyS944GfayT47ym" +
                "/iiJ9CfAicP67vDcZfv/rGa8Y/WN9fiFi/zMAfkC2y13e+V/yo" +
                "D99b7PONXT+P/Gc//J2C6U8X/ZiJ/ZNQ/nfI/AmK/4X4Aax/LK" +
                "z3G8X/bHzA//yl318Q1d8h+Ufxgzj/j/2jEP1Tq3+U+sfvAb5P" +
                "wEEvbPVJMro4vmLiQ9p3f4X8/XFM+ZP63074QQ7+Np+TZfH3Nw" +
                "Adji/1j4B+gvGr0P/1H5+3fnH8yBlfEL/HeZ8xQ230a55VS9jI" +
                "k7LyD7N60MfNCP36Nej3zTC9iJv/XHD/PX59AXo/93/fP7Cfwv" +
                "yoMz5Tcux3u77H/j4K+QfA/5fq1zDvbwT1O9z3GyZ67PwKun+R" +
                "/VNX/9iRf+z8h8n+RfafID1E/bTo+yYoPyG1r8i/i1u/jPWnY/" +
                "3ivP7h4Lup2f+S+Q/Xbt+E+Pairw/i/yHwQZb/068K+O+XPeuD" +
                "hPUvjPeVcfEPiE+w8Q3ltb/4+JFT/sw5fwT9C/j+hSV/qiVT7u" +
                "9zTP3D5cdDxp9O+avFzh9Gnl9cfwn9M2n+AsYXUnyWp9+c8P+A" +
                "+kNcP0i4PkBAF+OH0u9jh4g/Fxmfeh/zW84P1n9Lv18SmY7iTx" +
                "yf8uTfaF85dIH8XX98IxufoR+t+lnKXzn/UXwS4Hxt8/8HK8M7" +
                "Zw==");
            
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
            final int compressedBytes = 4756;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq1XA2QXVWRfiRMJoTEhBiCgoBFifypi8Ovwpozb+bhCFVqoe" +
                "APmCERV0KY/EgYQn507j33vvfmvRk26JJMlLJkzAShLMVfNBoL" +
                "hMrPanBYd1kwsC5QLApBFGTCiFLb56dfd59zA5rSnLq3T3d/3f" +
                "31mTvvnXvfm1QmK5P6jsqkGcmyxrXZB93cDD0+MLsyWc/JAphv" +
                "tmZ3ViYHb1JTK5ONyyqTpVJlMm+vTKZXDL0OzsPGYmyQ5av6Gp" +
                "/v9rStVKqeZ+bOBzHD5jzwwVIpfV+J/aOKZs51l7nzxYEHpE36" +
                "IedxGEtnI9PfE5rnznZZPsCwNl4q6e/5nqbJrHI4m96qb8NMqk" +
                "f16D+ZM8hX4Lg7m+o0mI8n+1VPtkk/jRbQjsRZ8l3V09ikDlU9" +
                "1X9xlrzdnIf6ATVf9QCn/TbLtmzE57uncUw2Uy+D2X2tfBu978" +
                "fZK/ov+ielUjYlOyQ7girqnXDsIR00YN75fPI8t2WHMf+9pZK3" +
                "toH2Izubpn9qfbt1H0P+GY7tbl7bbXuAiPoF+t+z88AzAfq0Vo" +
                "U5vJ6Pf1nvgPMu/bMM1iF9N9hSlUL1FKW+GzWjp59Xaf6gsXAM" +
                "jeYxaopKh65zdlhPb68+STF62+AZzqrvMT/P7vkwuw+RsJ7O92" +
                "Pj0z8xUe4fImA9U1hPVtX4Bk9LXgi5tyLuRd129CPHBdbT+Hab" +
                "+qLj7W5eP9+cYT3TwQlYzy7f0zReNewfsu6AA9az1e9StTR7xJ" +
                "xB/h8cD2RPOU0tBeQxamltSvZbtBgbzrLn4dhzN+jZuLf80p5/" +
                "o5aW+0A+57DZzxpbvP8/4er7ffV8mP0cs1SXe99D2cPZ3uyx7N" +
                "Hs19nT2X9QxewZOP5AOmgPZv81+OHkj8L232z+q+xxP3sCGUPl" +
                "J63lWcONd5PdzzszZ/PTzHpwHnfO422GfdnvWthRNQpyFKVFjK" +
                "KeTKjRwcOdnTA0HFrvw1i0ZytIL5UaY86azzX46i0cWTuaMrls" +
                "NOOsZFVgtTB5OeQeR1BHmBFWannYMc+A2OzSsDL5Zf+lUv66fE" +
                "4LO0vNAjnLSVt1Fg49rj8NcowshHUzp+HMHeZcvgZn1uoz5HPt" +
                "+9F3RJ4xXpv+yYpcd9jBbxyIF1XmjIiN41aQe4zzMJqsLFlxm1" +
                "9Phx1SQyCHUFrEEOq6ooYG5zk7YWg4ND/cKF9NeqnUvIPQ8E5y" +
                "DkfWzuI+ZCBZxFWB1ePpe8xcd8e8eG3iiJby1WHHPANisyVhZf" +
                "LL/jlzsC1Xy0EuRwlreLHT7PxDavngdejnWES7M/eZc202+oxs" +
                "7kK//X1/K89S2859jN1yWYd0hy3vTTulTfrDWGJTPbE4N7G361" +
                "kLK0sOvBIyNkeyKpma/ZH2aOZfcl0yO3kpeT28v/cmRzbhOk7m" +
                "w37zGJDXwxqnMD8R5quTk/SAfVfel5wK+tt0krzDxp9eu8hlS8" +
                "5KzoX13M1XqvtRyLsIfBckPcl7ay8m70suIm9yA+0/k08kC/My" +
                "2BZZz5XJVcmS5JBkitEa/enFHjUrmeMiknlwvFFrwPab/WdyPK" +
                "zLCzb2FF4/6fD4d/ma5yfvScrpKGeZDdF6WMz7kVd+e4vf5Xzd" +
                "kk/C8Sm1RC2BdV2C0q70EtR1XS1pPEIWwnI0j3fnypOUiTL6fv" +
                "byLI0TuI9dn0tkHdIdtvGtdLG0SX8YS2xM/aLc6SjvKrsnrCw5" +
                "8ErI2BzdHd3w0zJnJ23HHajrRndHs+zshKHh0Pxwo/YD0imj/3" +
                "0/hSMb53EfMpAs4qrdHeVfpZ8JuccR1BFmhPonhx27eTrKI7Kd" +
                "YWXKKPvnzLs7VK5yWNccpV3pHHX9DZU3u52dMDQcmh9u1B8jnT" +
                "L6ujdxZOO93IcMJIu4qsrLD6c3hNzjCOoIM8r6PDdcnywimwgr" +
                "U0bZP2cOtl7VC7IXpfX0oq6/q3qb15OFsBzN49258RbKRBn99b" +
                "mNZ2ncyH2MXa+sQ7rDNr+Xfk7apD+MJTb6O8W5ib1dz5fDypID" +
                "r4SMLWJEjcD9+4iVr6gRyDTVaaCPV55SI40X4P4d7M4G9+8jNG" +
                "yeEbh/t37EAGq+mcP9ewuDs2ymyUnIxk7y6b8Yu7t/J4TMbHlN" +
                "qpHmtnQAvbbiYZwV2uH+3dfPpjkLvH+w+nD/PkLsKBb26IeAd0" +
                "L0NIfX8/Evt96/DlUjcP8+ovpUH6xnn5WvqD67nn1eHy+/UfU1" +
                "N8J69uGA9fQzyNxnufbBerYs7pzNNxLWsw8xOMtmdp6LSDPq15" +
                "LPrGfreUifrEM68IJ31OaD6Y3cBuvJIhAP6+lj7Xpan+mplenP" +
                "lJvY2/WEVYD1ZJVhPfvCYdbT93Wo6oP17DP7pXxNOiXvH9jdeu" +
                "eH/VL1BLNfKh8F+6V7zH4JDrNfgj1r8mY4TrS4wv1SMpycbr03" +
                "4n5J/qve76vY/VLlWblfGvgF26OY/dL1RfulpNncl37Fo/x+CW" +
                "Zw5+H2SwM/h8Pul7yH7ZegJ/8+IvdLkmV+VnoIY/IB3C8xpnvc" +
                "fqmFcfulzWozrPFmlPZnuxl1/X21Od3i7ISh4dD8CO3mnN5GVl" +
                "jPDo5Mx7gPGUgWcVW1ufxQujXkHkdQR5gR6r8z7NgzGeUR6dfi" +
                "nor758zBVlEVkBWU1lNBvfKcqjTvJQthOZrHo50yUUa/I36OZ2" +
                "ncz32MXUXWIR2x6V3SJv1hLLGpXlKcm7MnlrLfcGD/tA5mn1+d" +
                "Wp0J1+r0oVPo3sTt+/Vd+Aw6ORpw042WvDk5AZ9q2+vtMHw2zZ" +
                "+GJ2dQJsBMY/u0DfwJfOMX+Hwerf635yPJx4xn6FRnqx4un4rD" +
                "eu4E1IzkarQkc4HlUS2+x7rn8zKrq+/xZ6OVZ239vncmF/Lfd9" +
                "/HocmHvOWjyTU+Ty/VqEyqW9WtsK63orQrfasbXW/oegNaOIYG" +
                "+ThGxlBGV9flRGSzjfuQAWchM5MlfTjkHkdQR5hR1o87Q2zeE/" +
                "dU3D9nDraygldic3bSesqo6x9KC2E5msejneIo3r9+PsOzNOdw" +
                "H2NXlnVIR2y6V9qkP4wlNvoHxbk5e1jPVWFlyYFXQsYW0a/6Qf" +
                "ajtJ5+N/R4921o4Rgazqf3Yay0O50y+uvzdo5sruE+ZMBZyMxk" +
                "Sf8Qco8jqCPMCL/vt4Ud8wyIzT8b91TcP2cOtpvVzSBvRmk9N6" +
                "PevRUtHEODfBwjYyijq1ubw5HNtdyHDCSLuKo5dHvIPY6gjjAj" +
                "9hTnlhH5v8U9FffPmYOtpmogayitp+ZG1xFdR6CFY2iQj2NkDG" +
                "X01+cRHNkc5j5kwFnIzGTR00PucQR1hBll/bgzxOZfi3sq7p8z" +
                "B9s8ZZ6/z0NpPfNQ7x5DC8eEaH6QHTNRRn99/ivP0nyJ+wQ/UQ" +
                "c1wurDQ+7SH3JEi+spzi0jqvPDyuQPK2E9c/DvAoTfJQDOb6Id" +
                "UPi9Ab47ivdLfOcl83fu57ah5XK/FH+jofj7DMDtWMk99B/o+w" +
                "z5iuLvMxBbe32uDCvLdYj3WH6/dLmCuyZzdtKu9OWoV/ZJC2E5" +
                "msejneJcFjez6/kSzzJ0Lfexn/blsg7piNVvlTbpD2NjNmFuzh" +
                "6uz+1hZcmBV0LGFtGu2kG2O2nt7TiA80nkRRufOQ1n7pAzkpi/" +
                "c4LnGRrmPrae7XEdstj1PLmYl6xIjJBNPsE7lnPVXvN3G/n+sL" +
                "JkxW3I2PpXqpUgV6K0npWo619LC2E5msejneIo3t+ffJVnGbqJ" +
                "+9h6rpR1SEesbpM26Q9jiY2pX5Sbs4eruBJWlhx4JWRsEQ/9bU" +
                "O/M7ZBnkIL2slv6xYijaQzRzlbGAPr2XEgDm4+cByPxZxFmULO" +
                "dj0vCCvHXYaMzZF+K/02f+fg7zDm8026YtI+etVO7yy+Nzbfr4" +
                "NOP966770rvkOuXgSoReH9ctH7EZ7ThbKKvz7LlDN8h3HvRzIL" +
                "9li9ELF6ZcietNpA/D4bvh8VsVYz1Ux3NhItqOvH1UzdSxbCuh" +
                "nF00EzvR+z8vzVi3medIz7JK+wDlnsel5RzEtWJEbItfs63jFj" +
                "Msp56EXQ+YTsV7IgGzK2/mE1DHIYpfUMo66fRQvH0HA+uN8c5h" +
                "gZQxn9en5YIrkPGUgWcVXL7cqQexxBHWFGWM/+sGOeAbG1X8Y9" +
                "FffPmYNtmVoGchlK61mGerkuLYTlaB6PdoqjeF/3jDAL+Ri7Zb" +
                "IO6YjVV0mb9IexxMb0VJSbs4ef+tNhZcmBV0LGFrFFmefvW1Ba" +
                "zxbUs+PVltTbCUPDoeH6FBi0O90+n29Z4Sf/FY6E33fmQwaSRV" +
                "zVHPozIfc4gjrCjNDTcWHHnskoj9DXxj0V98+Zqy3udbUOr90D" +
                "u+Xrsjmyt/P7gnyxe54sX+Xzj4f3SPJewmI+SnVrO/Ir+HuUvD" +
                "/inx/Z77Rc4TLlnwy/Aa6/kH9MvovQO4f7/MjGfZq/Z+aLoKe3" +
                "WfunfN5LiEn+ibz1/lvdXj8WLFfK58n5pfZ8Wb7QfH5UeH+0Rq" +
                "2BdV2D0q70Gje6erp60MIxNMjHMTKGMvrnIT0SyX3IgLOQmclS" +
                "eSbkHkdQR5hR1o87Q2z91Lin4v45c7BtUptAbkJpPZvc0OPdO9" +
                "HCMTTIxzEyhjL6/fxOieQ+ZMBZyMxk0d8MuccR1BFmlPXjzhBb" +
                "f3vcU3H/nDnYulQXyC6U1tPlRtfcrrmqS+8lC2E5msejnTJRRn" +
                "99zg2zkI+x65J1SEesfkTapD+MJTamflFueP1kXdXPDStLDrwS" +
                "MjaHPjv+RIpeh7T99Fyfw18N5a7Z/r3IPnpFtfv5y1rxZxbs59" +
                "e+9l5evgofYD//aNFzH2JywP38mjB/0X6+c2bxfj5+uiSZqVvU" +
                "Le7sJNrM6JrdNRstHEODfBwjYyijvz5nSyT3IQPOQmYmi/6fkH" +
                "scQR1Rr7x+3Bli6/8c91TcP2cOtgE1AHIApfUMoN69Cy0cQ4N8" +
                "HCNjKKN//dwlkdyHDCSLuKo59P+G3OMI6ggzyvpxZ4jtnBX3VN" +
                "w/Zw62xWoxyMUorWcx6t07pIWwHA37T+ZzdoqjeL+eO8Is5GPs" +
                "Fss6pCNWPyZt0h/GEhtTvyg3Zw+7uuPCypIDr4SMLWK9Wg9yPU" +
                "rrWY96ZUKtr0w4O2FowM9xHcZxn8vidMro95+PSiT3IQPJIq6q" +
                "1neuM9yKefHaxAUtyDjMLSPqW+OeivpnV4DzLVALQC5AaT0LUM" +
                "9OVAuyk8hCWER3rlUL4PpkPmenTJTR1a0fGWYhH2O3QNYh3WE7" +
                "12YnS5v0h7HExjAuys3ZA8sfhpUlB14JGVvEarUa5GqU1rMa9c" +
                "pv0cIxNJwP1nM1x8gYyujXc5ZEch8ykCziqmp1NTXcinnx2sQF" +
                "LdUk7JhnQOzgKXFPxf1z5mp1+JmW/CQsO/W1Po/rvOFv/Tyufr" +
                "y0HdzncZ03ZKcd3OdxyPjVP48b/KdXu4d+lc/jNqqNsK4bUdqV" +
                "3oh6diZaOIYG/LRrGMd9PIYy+p3dFyWS+5CBZBFXtdwuCbnHEd" +
                "QRZrTXZ9Axz4DYwTPjnor6Z9en861Q5m8tV6C0nhWoZ2dJC2E5" +
                "msejneIo3n+fdU+YhXyM3QpZh3TE6ielTfrDWGJj6hfl5uxhPa" +
                "8KK0sOvBIytogNagPIDSitZwPq2dlo4Rga5OMYGUMZ/f3R5yWS" +
                "+5CBZBFXtdwGQ+5xBHWEGaH+TWHHPANiB78c91TcP2euNrz69x" +
                "myc/4B32fY+vd4/QRujYN7/ax+8a96/bzjIL/PsE6tg3Vdh9Ku" +
                "9DrUyx1o4Rga5OMYGUMZ/Xp+SSK5DxlIFnFVc2TnhtzjCOoIM2" +
                "JPcW4ZMfj1uKfi/jlzsM1QM0DOcNLaZ+CA9X6CvGjjM6fhzB1y" +
                "RhLz198ia3IfYzcjrkMWez08UcxLViRGyIb3xHMTW7ued4aVJS" +
                "tuQ8bWv1atBbkWpfWsRT17N1o4hgb5OEbGUEa/nhdKJPchA8ki" +
                "rmq53RdyjyOoI8wIce8KO+YZEDv4p7in4v45c7C1qTaQbU5aex" +
                "sO+N04nbxo4zOn4cwdNNP7MSvPXxuRNbmPsWuL65DFfuowVMxL" +
                "ViRGyJX3xHOjHz/vUG16QvYrWZANGVv/KrUK5CqU1rMK9crv0M" +
                "IxNMjHMTKGMvq6eySS+5CBZBFXNUe+OeQeR1BHmBF7inPLiMZp" +
                "cU/F/XPmaDnQ8+TKF6RNPk/WC4q/H5JtC5+y8vzqgb/P8+R821" +
                "/7PJnvMrCn13qe3Lj04J4na/Ot7zH7Gd50J9VYq/OxZML+Txpg" +
                "T46G34HpsB8YM38vwzlUD3NxNseLqf08ElBnYF76exmT12Vjfy" +
                "13EvsJ+/pmmL+XaX3TZ6H5exmOc5ns38s81eI6F/SjWkyONetJ" +
                "teJZcja3yjms59LkQtbXB5xf/r2M59Arov8fiktqOw==");
            
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
            final int compressedBytes = 3868;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqlW22MXGUVXoiSruVPmxRqFLpsoEA0ijZiIGBeZnZoto3idz" +
                "CazZK2tGk2/ii0Tfmx7czc3fngo+lHFFp327QQgvrDSEyMFkME" +
                "jKABgkrURH/RAAYVNAhTNfp+3DPPc857t2DdN3fO+57znOc85+" +
                "29d2bugLvN3TYy4m5zpR0ZkVWYFw3tAZbRnC9+5CF/JP7d9LBl" +
                "QQx/uiKvBTt/vfbpuM2FmmKimpvVj4z0T9rKWgNXEsURsd6t93" +
                "a92BhZL+vaGu0BltGcL37kIT/V7d5pWRCj/Vyv62At2NZN2qfj" +
                "NhdqQk9V3Kze7+cjtrLWwJVEcUQcdUe9PSo2Ro7KunaJOzr/je" +
                "QHBiOh+bB+Zizr/lIjOSYKtIq8ajg6o1Z7noGOhDH1pDtO89YJ" +
                "zui/lPdU3T8r977CFd4WYmOkkHWx3hXFjuQHBiOh+bD+8NpZC6" +
                "/3PcvIzuUcEwVaRV41sl5htecZ6EgYfU83247T3O8nZfT/kvdU" +
                "3T8r976T7qS3J8XGyElZ18bEwxiMFGu/Jrnan9ZgLOs+r5EcEw" +
                "VaRV41HJ0brPY8Ax0Jo/SUc+uM2nvznqr7Z+Xet8Vt8XaL2BjZ" +
                "Iuvi025LvN63wMezhOZ88YMJjKlu7TnLghip26LrYC3Y+fu1T8" +
                "dtLtSE+lXc84e5q/5fbWWtgSuJ4ojY6/Z6u1dsjOyVdW1WPIzB" +
                "QIwxOgeMZd3nNJJjokCryKuGo1Oz2vMMdCSM0lPOrTP6r+c9Vf" +
                "fPyr1vt9vt7W6xMbJb1sUt4mEMBmKM0TlgLOv+RiM5Jgq0irxq" +
                "ODoNqz3PQEfC6Hv6jO2YGQR793jeU3X/rNz7Zt2st7NiY2RW1s" +
                "Xn3WztxeQHBiOh+bB+Ziyv9xc1kmOiQKvIq4Yj8FTr4trQIh6u" +
                "z9zgi/t5a95Tdf+s3PuWuWXeLks2+pfJ8Pv5RUTFx7O0klk69A" +
                "x2yP8rXZNjpG5ZXgee8NdZX61LV4QiUVN8QVfXSqT+3ZtsZa2K" +
                "faI4xje7zd5uFhsjm2Vd+5D2AMtozhc/8pBf1n3BsiBG+7lZ18" +
                "FasK3j2qfjNhdqQk9V3Kze7+deW1lr4EqiOCI2uA3ebhAbIxtk" +
                "XWzVHmAZzfniRx7yy7q/tiyI0X5u0HWwFmxnUvt03OZCTXF7NT" +
                "er9/v5e1tZa+BKojgiDrvD3h4WGyOHZV27QTyMwUCMMToHjOX3" +
                "zZ0ayTFRoFXkVcPROWW15xnoSBilp5xbZ9yzPO+pun9W7n3zbt" +
                "7bebExMi/r2vXiYQwGYozROWAs93OXRnJMFGgVedVwdJ6x2vMM" +
                "dCSM0lPOrTPuWZP3VN0/K/e+Q+6Qt4fExsghWdeOuEPx+6bCYC" +
                "Q0H2nUjmANxvL9/QgjdUwUaBV51XAUd1jteQY6EkZdn7n9903K" +
                "yHsCo+6flXvfolv0dlFsjCzKurYgHsZgIMYYt1hbwBqM5X4uMF" +
                "LHRIFWkVcNR+cXVnuegY6EUdfPO4MCWxlx3T8rd4sT6ybWjYyE" +
                "12RDJK3CvNgrHsZgIMaYiXXFPqzBWF7vuxmpY6JAq8irhqPzW6" +
                "s9z0BHwuh7mrUdMwMU2MqI6/5Z+cQ613Vdv69dsXGnu7Iu5sXD" +
                "GAzEGKNzwFju510ayTFRoFXkVcPR+Z3VnmegI2H0Pc3ZjpkBCv" +
                "Keqvtn5d434Sa8nRAbIxOyrl2rPcAyuv0ax5Ifecgv9/M+y4IY" +
                "qZvQdbAWbHu19um4zYWa0FMVN6tPaF1Za+BKjHeb3CZvN4mNkU" +
                "2yLrraAyyjOV/8yEN+ef+817IgRvu5SdfBWrA3n699Om5zoSbU" +
                "r+Jm9egrV2qVaLybdJPeToqNkUlZN15yk/H9fRI+niU054sfTG" +
                "Asn7M9xSydyzlG+zmp62At2M4V2qfjNhdq5p+s5vbv79QVuqiq" +
                "oCsx3m13273dLjZGtsu6uFt73PZWHVGJcb74kYf88ve4byGDc8" +
                "1+btd1sBZsa0L7dNzmQo2/f1Zys3r0lSu1SjTe7XF7vN0jNkb2" +
                "yLq4VzyMwUCMMW5P8VWswVien39kpI6JAq0irxqO7qes9jwDHQ" +
                "mjr/8H2zEzCLb4mq2MuO6flXtf3/W97YuNkb6si/tcPz6fVxiM" +
                "hOYjjcZarMGY6jbWMlLHRIFWkVcNR+cCqz3PQEfCqOszt7/eKU" +
                "NQVmnePyv3vp7redsTGyM9WRf7xcMYDMQY43qNcazBWO7nOCN1" +
                "TBRoFXnVcHSftdrzDHQkjLp+3plgBWWV5v2zcu8bdaPejiYb/a" +
                "My/Hvh5Yim4e+fw6jEZJYOPYMV/pvu1zU5RupG8zrwlPfPUY3i" +
                "OOdqNdwTc0OtKNCVtSr2Md5Nu2lvp8XGyLSsa1dqj5v2+zlt0Z" +
                "wvfuQhv9zPBWRwrtnPaV0Ha8H6/ZzWKI7bXKgJPVVxs3r0lSu1" +
                "SjTeNV3T26bYGGnKuviJeMTbeluiCSExxugcMJb7uaiRHBMFWg" +
                "Xj4WkNrPY8Ax0Jo+/pMdsxM0BB3pOtA63U75Sb8nZKbIxMybq2" +
                "VnvclD8/pyya88WPPOSX+/kAMjjXnJ9Tug7WgvXn55RGcdzmQk" +
                "3oqYqb1aOvXKlVovGNQWPg776DZOOdeCDD/1v+FFHx8cwznCcz" +
                "OSLiquFsAM6S/ypdM7zu+6zdT13RaoifaJ6o1iUV913KiqAmac" +
                "u5oVZU6spaFftEMeZx/XjuLZ7Ko82xNK/6a3+iNR2rrl4a01g9" +
                "8i7+Go+DoTVVhSh+plFhxqt9ly5Vv324rDA4S33VQfMWvQ9cWX" +
                "fqVrqV6TVZ8aV1baN4GGPRfMAPduTG6/2flgUxqwx8sgK2+4LV" +
                "ruNWo3hSTzm3VW8r673KVacYn8n2WvPnwDONQevB/KrAtaKvdX" +
                "0FCGvrYb5OaxcyS+uh6uvd1rFXWe3C9o78vsF1lrrei6err/fW" +
                "Cd4D2xf3aisxPr9/qv18uzEIz0P+v/3s/o35u4/xvaj7+rntp+" +
                "d54xz3861z3U9bp2o/3X6335+n+8XGM3e/rIszbn9vJPmBwUjo" +
                "9muSq/1pDcZyH15mZO88jokCrSKvGo72aas9z0BHwuh7GtiO09" +
                "x/36QMiWgdVf2zcu+b8eOh+Bqt98nKrxuz0QsPsAE5E3mGvjif" +
                "EY8w+R18ReLR95AgkSOxIYIqpjqkYGaIm6nSNVSFjsqcFIs9zW" +
                "TxGVGb8qmLigqsZNhXwvo/b53YGHOybrypPcAymvPTa2MMecgv" +
                "r4sxy4IY/Ws7Xcc59kSeN7VPx20u1IT6VdxQzypzpVaJ6CkRG9" +
                "1GbzeKjZGNsi7+pT3AMprz02vjSuQhv9yHKy0LYrSfG3UdrAVb" +
                "/Fv7dNzmQk2oX8UN9awyV2qViJ4Ssdwt93Z5stG/XIZn/ZNb3l" +
                "sJD7BpllYyS4eewQp/91Xm6a3gGO3n8rwOPPGz7ulqXboiFIma" +
                "0JOOp7m/fyodtrJWxT7Gux0u/J6xQ2yM7JD1nPEAy2j/fkSx5E" +
                "ce8lPd3nssC2LUzQ5dB+shzwe1T8dtLtQU/6nmZvUJrStrDVyJ" +
                "8W6VW+XtKrExskrWjf3iYYxF8wG/MIGx3IeLLQtiakdVHVkRzy" +
                "VWu45bjeJJPeXcVr2tjLitxHh30B309qDYGDko67nzxcMYDMQY" +
                "o3PAWO7DhzWSY6JAq8irhqP3cas9z0BHwhh/PzIdMwMU5D1V98" +
                "/KvW+r2+rtVrExslXWc8u0B1hGc356bVyNPOSX70dXWxbESN1W" +
                "XQdrwfY2aZ+O21yoCfWruKGeVeZKrRLRg9zux7rXdD/SONX9aP" +
                "nt//vD5wCP+n/PeH9onKLnIaeWeoow94PwPKT4keCr/iYe5VX3" +
                "9fjespB9OzoFhurnId03NCrMeLXU8xCp3zhV/Jj9rRNWAb750P" +
                "OQU1af7tQdd8fTa7LiS+u5UXe8983kBwYjofmwfmYsf998RSM5" +
                "Jgq0irxqOPz7+/FqXVwbWsQz/7LtOM39+ztlVPVU3T8rF49+St" +
                "f8oXjb/nt174F4Bn2Ov/Mv/ZyrNZ1/Dzfnx/d41Vux5BOzwdnP" +
                "z/Zp87xxoPtY6vycf7VaV3Z+Ds76PHGJXXBH3JH0mqz4wqhfVr" +
                "/MHYnf34eY4MEYGcYYE1BhnrBgDLPECSTHhA3KoEpXDYc/P432" +
                "PAMdoVeuz9z+/KQM0W+VWiW8kzG20+30dqfYGNmZRv26+nXiEW" +
                "/wYCDGmIAK84QFY/xWc2HiBANiwiYzVqWrhqP3qNWeZ6AjYfT7" +
                "eZ3tmBmgIOm3Sq0S2s8YO9vzz/bz9bp97lesOvvzuvRaXBTPxX" +
                "rV7x3gPNvvHe/8vK73xLk9r0v13+n3jrAS5P/y/NMtuPDfEi+I" +
                "jTu9kEb9xvqN4hFv8GAgxpiACvOEBWO5nzcyUsdEAavQzPD0nr" +
                "Ta8wx0JIy6PnPrDNFvlVoldH6m2Da3zdttYmNkWxr1NfU12hN8" +
                "MgOa89NryktY5MfdLDmZRWJK3TZdB2vB9v6ufTpuc8UnqnJuqB" +
                "cF1UqtEuAx1+9nzeF9YcLPaicQbX47fv6k87z53fxKbV6T8O23" +
                "jP+RxNk9RlX//O7eP7vq3bf5naCru6jvUvqXgSU/f+7kfrvD/+" +
                "e6lr2/t/9R8XvcwOrTOt0BdyC9Jiu+tJ7Y5Q70P5n8wGAkNB/W" +
                "z4xlP7s0kmOiQKvIq4bDv78fqNbFtaFFPFw/7wwK8p6q+2fl3r" +
                "fC+U+A4dWVnwTTPIz6WH3MrejX4Ak+mQVkQsssHeE1oPyVNSas" +
                "4E+cqNmvc21StyKvA0/5vG6FRnGcc/EqqnScGVgBI5nVVhJ8jL" +
                "dd29u22Bhpy3ruIvEwBgMxxugcMJa/Fw80kmOiQKvIq4ajf7vV" +
                "nmegI2H01/UdtmNmgIK8p+r+Wblrh/vA3F2tr8/tai7T9y7/eW" +
                "G8Pu6jq/nzUH28OdYcz3/H1L9v1sfjuTief4sInPx5aOnPS82v" +
                "RG27+dMOf0qZe7/HvA93suZK/z37YtRLn5fye1y9VN+8Vv8XMf" +
                "b7UXODKA33T9tH89bmTOXnpWPOvzuE12TjTh+T9dwH3LH439Mq" +
                "DEZC82H9zFi+L39JIzkmCrSKvGo4OhdY7XkGOhJGf37eaTtOc/" +
                "/9iDKqeqrun5W7Y40zZxutpxtn+gvs8f8GGSr3JY/4EW894/fz" +
                "y1XI4b/yGbGM0BWSp316KQ1p3vo55wqn388d1dzQDCXMW915hv" +
                "8vkGXPDw==");
            
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
            final int compressedBytes = 4141;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuMXVUVviiWpwy00JZaSjUBh1rKUxALw75z5g5Spu1Q7L" +
                "uFiloFEaFKrGKk+9575s7cOykIopVEisNL/acRAZWZdIagf9o0" +
                "jSaN//xRQqSx1DQ0kyJ1773OOmutvfe5M0QS78l+fev17XX3Oe" +
                "eex4w+VX+0VNIf0zP06SX30Zfrs3WHPkfPKpXSa/UFzd36Wj1b" +
                "z9Of0PP1AiNfqD+lL9GXmt6nnX6nKYtMWWzKEodcaco1rvdZ/T" +
                "nX3qSVvhn8D92tK/prBuvVX9C3NBN9q76tFHz0Wr1eb9Z36i2m" +
                "/yVT7tZf0V/X9+hT9EdAo3ZIn6HP1Gfpj+tz9XlGY6Y+X8/RF+" +
                "q5zv6iHQv0xab9pBtd5uor9FWlUj3VV5v+9fo6fYNpl5pyo+7S" +
                "3aZNdA9jsMyUPlOW6369Uq9w2O36Dv1FvVqv0ev0Rr3JYXdl+l" +
                "825atqp9pZKtkaWiuBke1X3kKE69AGstphtJU4jMljls+HpSaX" +
                "IQPJIoxqS3OPzz20oBmhx1Kp+wF/xtwDMQjnFJ8/Z26wrWqrab" +
                "di6yRbcZzeJBHS5domn0wGONmRfZbPx3wvJGPstso4NEbd5rjE" +
                "pNy3JTaN0+K+OXvQlpElBx6J66vNarNpN2PrJJtxnHZJhHS5Nr" +
                "eHuvIO2ZE9xAWZ7y/I52YZh8ao2zwkMSn3bYmNjR/zTeyRgYws" +
                "OfBIXF/1ql7T9mLrJL04TpVESJdrc3vEyY7ss/X5pO+FZCyfvT" +
                "IOjVG3+bbEpNy3JTbpzXHfnD3NK2TqM5H6qqVapm1h6yQtHHf3" +
                "I8J1aCMZ15E25BHilp+VmlyGDCSLMKotzcM+99CCZoQecU6hb2" +
                "kRm1N8/py5wcqqbNoytk5SxnH3JomQLtfm9oiTHdlDXPDp+wvW" +
                "Z1nGoTH3E+MlIxIrbLkd9w04MqdZxCLISFy//lI+gzugmLP+JG" +
                "LpLVw6cLqt9ULQMr88XrX1wBl+Lup/QE/1VwAZmEHS8i887T+Z" +
                "8nKQzzswipH+3ng4y9eoPiO1iL9vLz9pb/13oFH/Y0yOdtyXXg" +
                "ntwKkkDfXwM2T4Dr2kVhvZapfP/6Cke43h/ZyzWo21yefq0pQf" +
                "rlN9kUvKIyIrzxfZyyhDQcarL0gt4h+zp4+dk8/Q+RvheFX4yv" +
                "LJIpB/6ac6J7daA8Vgs3LpbPO7+S6UQm3yuSbGszo3a2eSfuzT" +
                "uHBw9zTyuUZ6GBzxNWpbBp+2WtXzff4xexZ/Lukbv88G+VwjW9" +
                "vL8rmGS0O9nOtVg1cOLqlMDl6hVjnPlfy7XIe9ijsGWLleWJmc" +
                "en0W65DP9vZTRUmXSS3b4yOYS3F83396a5afVb5crcryuYpQih" +
                "zyhP29MoGS6qn5vvm6OY9e7Kwm0Nbkc2Ia+SjUKe/mo9YpRfbS" +
                "Q7i/1w5JLdvjox0LCuK/HmdYHSmeAeSToxS5aKblRvl+W0PJvr" +
                "MBkrr6/mqiF5K8+FOsU24M7p6OvfQQ7u/VnsGnuZZkDrNpxwxa" +
                "2t/bxad8krX0w/erbH8fG7yCr2VXjsKaNcgYYJWjZn1iP9jXcu" +
                "2jlTHm6ahg+jxFcZqm3tHvHydMjDGMkHvNtkzjqKc1hiyB744F" +
                "kl/eP0r6kom3Psd4PokX5ycQN//k/eR92N/x/lKpZDFbm7m/YW" +
                "vbrx4BLL3X3l+CPshATsVpfQvuL4FOKlZK+TmKYjS/AS33hPeX" +
                "ELf10MugR7rpN43WmfpeRPTM5H09By30RTGvLn42J30dohmTe7" +
                "w7XMt4PnNe2a8jvU7fB1j1XzwHaqXK1rLK1zRgtqTfVivr26CP" +
                "BXXJUsoRp37rdJHPZyiKWtmaAS33JHkgBnrErXWa1OIMY/zyY9" +
                "g2f8bgtTrS9vffyhCRWBazX2X7msr3OcBsKe9T/S6f/VRQlyyl" +
                "HHHqt84XZ5YXKYrqb82ElnuSPBADPeLWmiW1OMMYv/z73OfPGL" +
                "xOkc/+EJFYFnOFWpGNV+QSh9lSOQ4ojrkuWUo54r5Oflw6TlFQ" +
                "R3qSPBADPbRpLOR+JIs4v/z358X+jCWDgnyuCBGJQczkveS9bJ" +
                "7j+dHGYbakD4M0ea8yjpjVhD5aAk5yW4M3rpP//hujKOhHeoIY" +
                "Ngr5Qz3EusfSH0gt2wNmyDf0CpZ8FpyJ972P85GfJYjMkWwuk4" +
                "k5L9kaWieZxHHaQoTr0Aay2mG0lTiMyWM2nz1Sk8uQgWQRRk0m" +
                "u/e0LvG5hxY0I/Qo44czQ93GgnBO8flz5smkWq6WZ+t1eb5yHW" +
                "ZLz02A4pjrkqWUI+7r5OtjnKKgjvQkeSAGemjTPd5aJLU4wxg/" +
                "iu/PWDIo2N+Xh4jEIGZyMjmZreA9eaYdZku3Sk7a81FysrIHMa" +
                "sJfbQEnOS2Bm9cJz8f/ZaiJCdbM6DlniCGjUL+UJ907fmda9ke" +
                "MEO+oVeXT8VnQZ6D66M9YvV5WYLIHIH5qz7Vl+W3L8+0w2xJH1" +
                "V97vzeRwV1ybJ2mMsRlzosn3+lKKrPnN/7uBbX555Qn7jl5/c+" +
                "ySoWm3tNd/ozBq9TnN/7QkRiEDM5kZwweT2Brcv0CRx3dyPCdW" +
                "gjGdeRNuQxi3ut1OQyZCBZhFFtSR/zuYcWNCP0iHMKfUuL2Jzi" +
                "8+fMOQJXT/o1Ux7K940eTzra/t5Rdu1wZTud5pwP5/6S5dbu/l" +
                "LR/RCcU2XSPn//oHfIprq/pGapWVBDixiMu3vVLHt/Ser42rwQ" +
                "Tt7J1n2Po9xLq8llPjPyhyPSrR3yuUu5zxEROyc5Y+hXRyR7P7" +
                "LMVcg6k3WoDtN2QOvwDtzMPvWE6qhvI4R0oQcj7EGRPdu2HuP+" +
                "m4u4n9ZOLmPsOsI4hLjz0aNxXhRZMkI26eN8xtQ3+RQ8/MiSFc" +
                "e4vv6O/76NGdH7Nrv0BfZ8pGeb4t63SZrTed8mabqV2IT3bVpP" +
                "0fs2SdOg7n41vG/T2hV/38bIp3jfpvUz+76NkUTftymVit63Mf" +
                "Gj79tUR/j7NkZrmSnB+zameO/b5Hzd+zax4xUdiWrbSyWbT3kf" +
                "zz9S8aNLdUvR0RD1Gpdx+9bO9scvW1fvjB3NWo/KYxjGQAyOn/" +
                "4dPxO/k/Mhv9UReczm9yDDOKRb+57Yr5aqpVDbFhEcp0+ppWZ/" +
                "X0pb/QKSoiX2oNi6Ptv2kkfQK/ePKGCtXVwmeflxCIH1KTEp57" +
                "ZUIysph77Z3xkPq8U1uVc/EjKmvul1qS7qQzH5fFrMsQskJAe0" +
                "dphbRn+9dZH/5kppX/iLr4t7DPXT3eQTeXHP3B5r26Y/53y4X5" +
                "4DyoLvE2ylboT9jepG6kMxa/vfvk4yCJpS2+8ng1SjJciSQetB" +
                "WhUx4h5D/eHPEGfwKJlxe6xta1kRH8meRpYlMQ3jcN3o0Z+fj3" +
                "5oyjluDTwjdObrHUnVno9M/9Ic7WQaSzI2Var99z+NB3M+0o9M" +
                "lU+93tXZ8VjfHeRzMZyPnDQ/H5l2Lj9+Rs5HhpXW9nzkMHakEe" +
                "ejqjkfVeF8RM+P5Pkoxjo5kByAGlrEYFx+KDlgf39KHdpAmxcf" +
                "5x6z/b2faw6XuQwZSBZhVFtqh3zuoQXNiOZq5yRnDP3qCLeIzS" +
                "k+f848ORC7DqHzZHkbnQX981z1J/kZ7rA8F3Kb0Hvzdn7OT5+X" +
                "z+Ni10jh8zhbhm+LXalQvKLncTgnea0z1dVYKK/+VGLZ87i9yV" +
                "6T173YukzvxbH6DSJchzaScR1pQx6zfD7INW0EkiEDySKMasvw" +
                "Bp97aEEzQo84p9C3tIjNKT5/zjzZy77diXB9pi9WJu3+bpAJ9j" +
                "x4wv/tFq5PfP8hsj6/y1fFcG98fdr3Bdo/L8b3GXKtCWSJ7zPE" +
                "12f6gpwFem73PgNbyxOcn0Bgfe5L9pm87sPWZXofjtNfI5KjdZ" +
                "SCBsqEjtFy/TrpYK9+tkV5DJKhN+xxVjKqLcMbfe6hBc0IPZq6" +
                "7s+Ye8h168DfZ+ozYevTydj6HI2sz1fytTHK1ufoNNan0akdj6" +
                "3PZJM8ahWsz1Hroe3xc5OnNYosYVS0PpNNchbx42djSWW09m5k" +
                "fY5yfgKB9bk/2W9i7MfWRdyP4/TVZL87v+c6SQOloIEyoWO0XL" +
                "9BOnmvYVHSHL6PZOgNe5yVjGqLOb973EMLmhF6dAy8GUPfnN+5" +
                "RQP4Sx4hE/Y9gexgctA/XgBmS/pacrBxte1XJgBLhqwm2KAl9K" +
                "hYLauTDJH3XHPIesAouf7B2PETcaxxA6RxjdSyPVtqCY4kP8Yg" +
                "1+dM5PHTzWCIjQ+Gx0/0I3PgfvevtVt+nbAWisnnOL5fB5hamw" +
                "yAptSWtfE7YHvJAOmg/2QAZPmv8gcKro7WSo/cJr//uZZr4QgR" +
                "KSEMWPn+7f0QngPLEpjy2UIM3GLM1CK1CGq1iBAcp38hKWK8R/" +
                "ZUZI9a9F/5h4zJZZKXH4cQ9008GeclIxIjZGPjSzn3wBnIyJIV" +
                "x6S+62+wW+5pAxSTz70S4zWXyFr2fP+N66WsYH1ukB5jNuQVeR" +
                "EzX0JY47qYf47JLPDZQgzcojPtVJ1Qq05CcJzuU52NGwhRnZVj" +
                "JEVL7EGxtdUyK+EYeiX/lWOIkheKLXn5cQhx38vnJSbl3JZqZC" +
                "Xl0Df7O+Nhtbgm9+pHQsasv95u+VzWQzH5/JvEeM0lspY933/P" +
                "ESkrWJ/rpceYDXlFXsTMlxDWcyTmn2MyC3y2EAO3NswutxuNoJ" +
                "h8/l1ivOYSWcue77/nHSkryOfl0mPMhrwiL2LmSwjreSfmn2My" +
                "C3y2EAO3NswW241GUEw+35QYr7lE1rLn+288KGUF+VwsPcZsyC" +
                "vyIma+hLDGAzH/HJNZ4LOFGLi1YbbRbjSCYvL5T4nxmktkLXu+" +
                "/8aTauM08rlReozZkFfkRcx8CWGNH8f8c0xmgc8WYuDWhtk6u9" +
                "EIisnn2xLjNZfIWvZ8/z1vqWn8xQzqo27MhrwiL2LmSwjreSvm" +
                "n2MyC3y2EAO3YmbyCpau1NMjEit+vhm7oq8djz7f3DW9v1+SHm" +
                "M28imlfL6J74eEzzd7Hor5D++GVSZr74b3fflWzEwtsRuNoJh8" +
                "HpMYr7lE1rLn+29ul7KC9blEeozZkFfkRcx8CWGNp2L+OSazwG" +
                "cLMXArZua9z0DPj/L3m/Q89/zocfj/IeHzI/0Ef36E/z8k9vyo" +
                "sVtXps6n/fuO9s+PDGb/f8iP4s+P7P8PiT8/guPnVM+P+P8P4c" +
                "+P+P8PybC7Ys825P6O41o/7hW12/n9NblnhX9/FO61bH//lcSK" +
                "nnfIOOF+5nP35XC/LvI+wy/9owP34L+7EH+G4zMJjzkyQ3k+t8" +
                "eOT/9bPnu2/3/zCfGL81l8TzfOBN8PAZmap8yebGtosQ/j8qUS" +
                "UfMGziUpyqQ94tCz7cAslLvjp+Y2A+dxGTt6zQvjEGI/AzMlJu" +
                "UhR2z5nLjvgXP4HGgWsQgyEtdX89V8087HFvswrrwpETXfIr62" +
                "tEcceugFei6fdW4jZSyf88M4hLj18KbEpDzkyNnEZgz+kDnNIh" +
                "ZBRpL64VOnav5fGdI3pvOOqXiKumUqjZ7vlz7gx74PNv1P+mf+" +
                "PsOHET/+qQ1F4f8CSTmJeg==");
            
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
            final int compressedBytes = 3127;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuMXVUVvtZ0REJnxtInFqoNpdU0jNW0Qgp4zpx7UUGBBh" +
                "+tQq1TiBD4ZTDxB5LuO+29JzN/IFGKpKBpUsU/RoyWRynYTmlR" +
                "iPGHwQcKrRq1pT/6wGjRBPc+66yz1tp7n3P2nZlOz81+rfXttb" +
                "6179n7PO6eUfepdzcaarbqUxfocmdDH+oiNaD61cWNRme+mp9J" +
                "Fuj0frVEPabLD6hlarm6QtdWZLqVOn1Yp1U6XZlJPtLID7VGXZ" +
                "WV16pIfQJkY9tVK5Ndrz6lPqnLT6sbG55DbVS3q01qs659VacR" +
                "dYe6S92t3qVmFYj3qgt1PkcNqvfpcq6apxaqxWoRaLdeppZq6Q" +
                "cz5IeyfEit1jHNUx/V9Y+rtepqXa7T6Rp1nRrWZaKazP8NOn1G" +
                "p8/qdLO6KZOt1+lz6vPqC2qD+rK6TfDdotOdUX/U32iY3JTmgD" +
                "q040tJizJegxbWIMkalWh/7EHpk+vokB5tDjZGIrhHYoRseEzc" +
                "NrFF69Kz7ZFkEm+O1lnzQUumBq3OQinjOde4dS6V9rsvSJ3/QD" +
                "xifX3IKvIiZq2zWy+TvLHsPu+zz2VyFHi04AM/PmbR6mg15KZE" +
                "CbY7S0iLMl6j/pRkjUq0PzwifXKd5GX7IYmNkQjukRghG+Nf6r" +
                "kFzkB6tj2SjOOjoWhIl0NQZvIh/OjxXENalPEatEZPUAssUI1K" +
                "tB8fkz65jkUz5PohiY2RCO6RGCEb41/quQXOQHq2PZJM4rO1NL" +
                "8e5a0HdMpWguYvC9klOi1R34Prkc6vKDQr2Zp8ZVH7mFir+fXo" +
                "53A9qj7UF9XGrNyct0c8GH09Ug9lteJ6pMvseqQuhflurkfq+/" +
                "x6BDGZ61FWrmP2Kq5HuWy9upWuR7nsKyUr1oS79nU2Sm2+5kz4" +
                "Vkxr5Znwy/V47m0EHK0JnwXbK0eZmkmj34UWjKd7dDbY8daNh0" +
                "9KnsvstPZhLXkCa82XpRY0hORYKeMYiY2fCBrPfT4LtleOMjXe" +
                "Skr8YExV9sv1KCXPZXZovic/KHy/YsWRacx8d7HVMpKPvdoIOt" +
                "TGivHMPZj7zwI/V6eF9SwwJpjvFf5vqNRuUPd6me1IdkCOJbb0" +
                "Z7H+7OjcZiQSgx/qwTGmRm30UtQXSyRpsBcx49alVz93uwchGX" +
                "6x7OVGT0xcpjYTPpKZbmein4lMjiW2TK3zWLKz87iRSAwhMOeY" +
                "JHvKIq2Ux29KJGmwF9YI4XptNLatdLnbPdAesZD+3ciIgRuTL3" +
                "42njs5e6H5YbF26zWo8yOfRt8XJ7bEh3Ll8fHGlI8yDyGY6fCf" +
                "xd/0en0keQRyLLFlap1fmNxIJIYQmHOM7INesD48RyJJg72IGb" +
                "cuvQ7P8XG3e6A9YqHH8xnZy40+G/Vn3Jh88fOR5Oz1Or3Cc790" +
                "wH+fUn+/VH4M91dpiUX1UW+l7H4p3h9ivx5Vx7RZ3KEnu4rxtO" +
                "6tSFMu8ctIPvZ2GIvKubyrPpYyTPxU0HjWovxMk0eTRyHHElum" +
                "1vmVyY1EYghh8tETUOM9qY1esD6+SCJJg72IGSJcr37udg9CEj" +
                "5+UvZyo8/G80k3Jl/8fCSxb7Q7G+/lUEa7cb5GuzsvmxzkMJOp" +
                "TnPblaElaI8v5lgbzZ6YdwOLKgwxlBjZai6H+U5SqsUv+HtJCw" +
                "ZV5QF8uPabS5tLIYcSZdDuvIISjqEP6ThG9iGL+eheIpFchwwk" +
                "C9crWfXx4r6JC0rin9kRcwuIRZTN1I2fM9eyZc38aadZPPWAzK" +
                "ThrSDFtsTyHlxvW5LH+OWkgz4cxfEoxxw/1nkiIuB8/VYxJh6N" +
                "j6ftxZVIGfiM+qI+fbb2QZmduX340efnX0iLMl6D1ugJaoEFql" +
                "GJ9sfXSp9cx2ZXn+uHJDZGIrhHYoRs4meld8mkmO/P2p5tjyRD" +
                "NlTPxjc7b5KDkPLvUj8bdWexNfdg0NX3oOlXeqfzeOV5cPl03G" +
                "sbK2VcyX9YNFNjmhyAlD8DrHC17jF6IgRVzPerGjNylLGInw/h" +
                "SagpstgPKW9929UGjGflvfB4NEPjWcIi3hvCM947TSxehJSfn+" +
                "tcbZiNivG8dYbGs4RF/NMQnoSa2iF/u+xe28uTZBhq/GszM56l" +
                "z5tPh/AkVI/f4p5kD+RYYsvUuteZ3EgkhhCo4xjZB71ATY/nXR" +
                "LJddCLmCHC9kpWfby4b+KCkvg52cuNPhvP59yY/PFz5lKSSScg" +
                "Zevib5Kmrd02P+A7mti2ICv9b7SaMzTfS37XIf9JzS9I08E0eR" +
                "pSVl+VrNLn6E6uNZJ6G4DyYcHmjIxnyYwl/0nNnJ4OpskhSPn6" +
                "+ZKrDbNRsX7eP0PjWcJi+9d7i6ZHr8eSY5BDiTJoN69BCcfQh3" +
                "QcI/uQxXw8H5BIrkMGkoXrlaz6eHHfxAUl8T47Ym4BsYiymbrx" +
                "c+bJMbm3B697uD+n+6Zvf5C9U8je5yMtkcV8PH9sW2md3XqL7y" +
                "6B+3H3DfH9VBKB13ebEeji7fZuJ26huL5vl3HZ4yCZkE5l+xUb" +
                "DbE/pNivmDas/YoHpr5fcXyP2R+i9p+v/YrxnpD9iho1uf2KCy" +
                "I9ViaHEuvQTmdLCW8RevQEl6AcalRCTY/nU7ZP0rH3IQtcPySx" +
                "MTZvNyJiE//EHzHqAYsoGa/9wWgp6mhRpL9Pk0OJdWjHh6WEtw" +
                "itx5NJUA41KqP8vBl/0fZJOjaei1w/JLExNm83ImLDY+K2UY/2" +
                "bc/SH/fE8XINsleutO8crJ+/l7KZXj+7r4asn93fueun7ce3fr" +
                "InrBRzqJkjfY/Uts62E66veF5Le9dIVB2u3ZQoyTxOS5830xAe" +
                "Pv/Z1Sy19RKnxP5553p0oXU9OjQN16PXzu/++XhW0PVolns9Uo" +
                "cDrkeD0aCe94NQZuvAIH70eF5EWpTxGrSwBknWqET743+SPrmO" +
                "rZ+Drh+S2BiJ4B6JUcH1HeldMilsv2N7tj2SDNlQ3TozvlHMKe" +
                "vdmnrJt18e33+GvX0aP31un4to/SyZyUFWOErdEu49Oh4dhxxK" +
                "lEE77Y+Odx4mCWE5mvdHOVkii/nvDb+1rZBO8uJ+qI3Y7htSJv" +
                "V2X2Jj/Ptst3fxqCgKnwfpSeLda1Pr1yhNB3xvR8POw7K3jN2j" +
                "k+tXh7H/FqPs/OweCbNfra8ehegtSPks/xJK00EfjpBBM8BCD7" +
                "8+uX51GIwBZWX90X+1/fbt1fqyUYhPuucnyIwEayHnJ2J9fcT6" +
                "eWZ6z09iG3Z+cnzF+nlySufnvyDl380WlKbmbw4ftnGEDDrPLP" +
                "Tw0cn1c+btGxKDMaCsrD/6t/VtsX+sfWe1//pRiMcwh1r2rvCQ" +
                "1MJ3Qvo6a77zIKS3QYV5IRQy334YWqXn51gIjzL/1Ntvp53t4I" +
                "/7IeWyi3Nkf/tuy1qGi717L9v5fXR7rttHnFd/Czs/4/4wTHse" +
                "50b9yvp3/xpiv31Ptb5sFHLtAKTc1n0oTTXX7t9tXDzQy3y30c" +
                "NHJtfPGZd/SAzGgLKy/ujf1lvz/ZvV/qtHQc3mfy+D9/PqD6n1" +
                "Hk39sd2vXlPLehlP9WfGUnPo/jOw38aa5/cL/H8vo16vuV/K92" +
                "icm7+XyVfX05Dk+RmdThf6cFHtE077ftlHxBP49wDR6d4wGAPK" +
                "yvp3j4XYb3+rWl82CtW/d6TLe31fZ/8tt/u+bnhgOt7XDQ9M+n" +
                "3dyZD3db64Qn7vSE4lpxoNk0OZ/bJ0CtvpCpRwDH1IxzGyD1kE" +
                "v807JJLrkIFk4Xolqz5e3DdxQQn370ZGDNyY/PFz5smpmt+PVl" +
                "rv647Uva9TR+ve13XfOr/v67pnzuX/u/Dd79M8Sde4a0PY/2fg" +
                "R7pW/H+G//SyC6ru/zPI5zb3+cgz3/8tV6KyZx7f7w6+dcXDaz" +
                "Pl8n1derUPQ8iK0fBi2tv0fLs5aDQ313lpdyXK1HirrD/6r7Pv" +
                "16OU7Ls4+5tqP1iM540+jP3N2Ps/q5+Om/eEnZ29/JcRzzuys2" +
                "X3S+i/2n77Ib+e/ycRn53WptYmyKFEGbTTm6SEsBzN+6Oc+lH/" +
                "fL7/17ZCOsmL+6E2YaVM6u2+xKb7tt82Z09xuUxtJjZe51soz7" +
                "6bYldy+zsF9y0+ZMWZU4pp3ht0fm4J80IoU+Otsv7ov86+X49S" +
                "su/iWiOUW/N9vQ/TGgmItBTT/V/QeI6EeSGUqfFWWf/umTqG2R" +
                "g86tejlOxbuP8DcoEOqw==");
            
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
            final int compressedBytes = 2884;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXG+MXFUVH0raACkgLKXbRduIgFVLQyAgLNbsdOa5bRfwDw" +
                "pFi6axpTbZrdSUGL/1vVH6xk4jKqBSSgnqJ4OGEFOqtEqL+nHb" +
                "bFtTDH4xhhhJGsUPtpHE+959551z7j33z85Oa5zJve/ec37nnN" +
                "85c9/f3bxGQ3+Ss8nZotdjGOX3N8gHMKgHtDQ2LUGXz3OhTBvq" +
                "UbJBn8CLMtu51PSi9flF1JazN3OhjcfhWJcPs7b5A7zCPDuYdd" +
                "7melpr23v7Cl7jot/5KR83rIaUh1lVkO9cKv/m7SvM30jyml8s" +
                "/bo+mY7eHmoPqRhDsC0jDsE8Xw8SisEv6iiG26DHKp+FHEl1wI" +
                "CzsKOiV4kXjY1caslCM2PqARnYOcn5U+ZKNtIeqX+TL1SaUla0" +
                "fIPWwhywKMU56qkcx3WUEdRpGwkFiPxh9ApfG8VZ2LGpza7lPA" +
                "sfAxolJNPR0x3pxSYufSy9UvXF+vxmuqiUXKvadar9o0LcWPYf" +
                "hBWffli1FcnZdGU5u6X2dHt6p1XP6yrdJ9I16bjark0n5BzSh9" +
                "MvltuNZb8p/Uq6Nb0onWegLk/fU42uUW0JHj/TZTXmQ6Sed6S3" +
                "VtK7qu3H0o+nq/31TO8TZBvY7MuqbW40spuzFdl71fb2dJvqby" +
                "jqCZjsJu6hs1KKlX1A5pB91JIsV7/hcGMgn2xZQH+rY6U54mc3" +
                "WpLbZslo1Kr4s3p91mvpCdW+S+p5b/ZJ20/n7eiIat23lwyonv" +
                "f49fr8LtRzQPFnwfSBevSgUbmvz9HzepXP4guUxecc9ew7fvqH" +
                "aOR81S6pI66rpKeT/eoaYD/BvaHan9LrZ8XiTeP4eVOk3UOBM4" +
                "RimV5G8FerpmqV/plnYX52La/wdwTir/Nq16eTfvtmrzlV9LpV" +
                "ue9FbdlPZS2q93mbvcZgE8BlbY7izHU2vvh+/674aB3np2T6bD" +
                "3aZ/wqb/Sxl5D1mT3XaPQuu0D7+15HpUaifs+RucYf+6xuismO" +
                "mtPPqJb33lxedGN6V8axCUfhKOTvt8/X0ozkOqgMfsE9mxHQv8" +
                "tPcd029ml+fm9u5fdWWh9z/+3G9K6Ku3uPi8LvKelMcxVW3lY/" +
                "Q22XvYx68FRs6T1qKFO8nq9Z7dUtXKX4Ty8dzP6c7O3veil7JW" +
                "YlZAcHeOR5td43VsSuuviV2ssuzPHTVc/8I1E1ODSnY+c7ulW+" +
                "DoM0f0nCITLmet5E93bHc5oNBnIAmcs+H43xn9/t19tV8Pw2r9" +
                "XHGuMerLnWNze1srT35PldlxBPZmPn5KjB6wPc35+o99XHDC5r" +
                "fHNTK0t7PzzP9VzjY2Pn5KjB7/tn0HmmPAtdYp+P8iO1rLwe6+" +
                "zNZtQ44v4ovU18HnJC1fNHUVetD4buj7Lp4v4ofcu8P6pm78uO" +
                "Oeq9OOb+qLk4XSc9D0nvN++P0i/Fn9/zM1zzraNFPWOfhzjq+c" +
                "yA9qI/BvSueg4FVtcEoGb7fMmIsxv6Zn3GyN8xtEl5v7lbPnfT" +
                "GWKaiRQnzCaEK+83CQqYaxZKk/jyDPFwxUfrOD/G8dN4ntSckO" +
                "awPpvic2FT2tt3no+fEz42dk6OGrw5wPNRfb/ZNe5im+ukeV3P" +
                "Wpv9xW3Te+4813OdHLfWL4rysmguHPi9WvZXkGZvSTjzCt1//W" +
                "ldz+/v7z4ghDH/Buu6nm8Ox/hvDvv1rvvh5hmfzNTKPnQ9ARuy" +
                "6b044PV4Znb3Ry58PyhHrb+KPV2favQ3CYPIkE/ps/oncZzioi" +
                "CqGNGZyx7ih/zLepCifxuXTGJvXC/dK2GSyfD+njifXvd+HlXP" +
                "yWRydqhiRGcu+/yeEMNyJf1d1oMU/du4ZCP2pa9/1uejZY3G40" +
                "+bGER6MnViVv84qp4bQ1F2XcpRxYjOXPYQ39RnL7DZv2R7kKJ/" +
                "V5xmF3o9Kuv5fkPbxnHg+rPGaBtJ4z1+dWNwFAXMq+vPrhnXjO" +
                "/374qP1iE/zT3Q61FZz5WGdjz7N9U3x5mHcdObjaEabz57Qrjs" +
                "LEcBcx1PacZ9eYZ4uOKjdchPcrhm+p+6nndybfWfWYcj9tfDzv" +
                "P7S1H7+2HtoXtXDAoi0pnzefI5k6F4zeLQgxQj27hkO/alr/o/" +
                "cLp3SxhEejLd3pjTJ9ke8tCZz1HFiM5c9p15MQw7C2Q9SNF/OF" +
                "M8v3db1q87M4v7LPF5SPsHg7nu7Fza3/OQ+Pj9Pg9JprAvR7+q" +
                "67lGwiRTsT7FfL4fxykuCmE9xXJw2kP8kH9ZD1L0H+aJ9++dy1" +
                "W7qvF/+XEdPzsLL0z85JE64rX1+lxra/k47M1aH0/HsQlF6Qxz" +
                "VDGiM5c9xPf774zIepCifwnXXKAbP342F3SWSjhERl35GOj2k/" +
                "3ZhTCQA8hc9hA/5N+vn00VYH/vHOtODH4/aD81oPPR9f3t74OK" +
                "7632fN0qpjeAtPsZCYfIWN9sjznQn10IAzmAzGUP8UP+/XpfFZ" +
                "Jt2Jf1rP/i352SMIj0HP+cmPzzUcfPbaEo3W0clWyjUd32+UMh" +
                "hmUNbpb1IEX/HNecbE7qXm9Bpufdr3EJYima2oMc7dC+in/Q9I" +
                "I6zovGwTliuYzrTVtkU8SXfFP2mJfN1GTC8a13W++auWhZ0VYP" +
                "ay3MAavHaMn1VG57pzrwwz2Z1tDDV2JLWcj86udLwxRPmfg+kp" +
                "7LqpjnWup+tuj1ttScg3m+GSQUg1/UUQy3QY91ZIa0dTCirHhU" +
                "9CrxorGRC0g6L5sZUw/IwM5Jzp9nVYzGfqr7Ylv0ek4lgNFbGE" +
                "tzbokYruH2fKwZmDjThnKzIxaj4vxO5+ZIyoTzlJja8bi8PKI9" +
                "in15vVT/vaNzi4RBpOd84sS0j0Sdjx6Ni4KoYkRnLnuIH/Iv60" +
                "GK/l1+ki3C87odtpaPnWy2NOb0SbbERUFUMaIzl339vG5L4Hnd" +
                "Fl9e6J/jxkbHRnU/NooSmOePoBZkdIT22PgIt2OjckyuoxgzDk" +
                "pMDEfQiMgI4j6+nkfnTCgDHtmMiDKOr2arxlbhWDe1Pr/Bclyl" +
                "NajXUvx7HEqN6qxC/+0D3N55lFxFPUp49Am8qGdqD32xbR+gfK" +
                "hfWgOsgulT23KsuJI3mcfPzrHOqK3l47A36/j1atT+vikuCqKK" +
                "EZ257CF+yL+sByn657jWidYJ3estyIqvun9PQUIx+EUdxXAb9F" +
                "jl8zuOpDpgQFlwzyZPiReNjVxAQuPbmSEDOyc5f8pcyU62Tqrt" +
                "SdiWmpMw734bJBSDX9RRDLdBj1U9D3Ek1QEDzsKOil4lXjQ2cg" +
                "EJjW9nhgzsnOT8KXMlO9U6pbanYFtqTumvWp+7QUIx+EUdxXAb" +
                "9FjV8zccSXXAgLLgnk2eEi8aG7mAhMa3M0MGdk5y/pR565T/fR" +
                "fd78S878J810bgfRcHUfc/ed/FwZj3XdhcQjIdHd8nkGzG58n6" +
                "fQJqfX4P3ieQbC7fJ9DTSHifQG1Rvk9AtZWq7SneJ6C9Se8TaF" +
                "f/6e9/n0CyWUUJvk+gRFXvE1Cja1RbQjTi+wTar+v3CSi9930C" +
                "WI8Scx+Xlv43UIl+n0BrpjWj1ukMbMuVOwPz7iGQUAx+UUcx3A" +
                "Y9Vvn8miOpDhhwFnZU9CrxorGRC0hofDszZGDnJOdPmSvZdGta" +
                "badhW2qmYZ7sa00n+7QcMfjVaNpMOfVY1fOXHEl1wICzsKMCN5" +
                "kXjY1cQELj25khAzsnOX/KXMmOt46r7XHYlprjME+ebx1Pntdy" +
                "xOBXo2kz5dRjVc9XOJLqgAFnYUdFrxIvGhu5gITGtzNDBnZOcv" +
                "6UuZKdbp2u79Z/W2lKWeu0On6+prV6rhvMccb14JGO2fHzKEYB" +
                "jI0CRPcIRoMvjUxjcA31SvHtozwLHwMaJSSrov8X4zEDlw==");
            
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
            final int compressedBytes = 3013;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW+uPHEcR35CHCI9gHwbndPblOJyHsTkcQwiJSZjd2TU42I" +
                "RHjBxBCDgQh4fANo74EB7unZMz3ggJQgJYfuIo5AlIIBHFoAg+" +
                "8QlkEqT8BXyJT7bkD0SHiKB7emt+Vd01s+vLrQPMqF9Vv6r6Vd" +
                "/03M72TqPhjuQfvvij+/dGX5rd3BCHxxEyu6kxxAG//mj/ttFY" +
                "iN0gDOVAsip7ij/If70es1B9NHfK+cxOZk0d8WqO9h8a5+Wo4j" +
                "ra+MnqZLWvXUsSGncOJatd4TLegz2K7KEl/+3nZEyuk7zCOJC4" +
                "o4qXjAhGxMbFl3rugTOQkSUrLpP4or/GnaWnNb40GvvnpIzXXC" +
                "Nr2Qv9t09IXcXfeY30qNnAK/ECs1ADWfuE5p/L5CzwbH0MOquZ" +
                "yYPWu13xHx6wpjZWjyrX+x9HvM431rMZbfzOfGfe166FzI/bT3" +
                "gpx0DLdShUw3vsn8tcvffjOjPEoVHIM+ZF+r2TISOeU+wb/sCS" +
                "I+Q8SCYcX1yzk+4sr/RJXxqN/CtSxmuukbXs6f4HrvdJ6VGzgV" +
                "fiBWahBrJ92zT/XCZngWfrY9CpMTN7zIW2vthcYl5fyt5k3mIu" +
                "M2+1989XzNsKydttmTArss22nTLT5kpzle1dXeiuseVdtqy1Za" +
                "aQrCs9XWc+ULQ3mcR8qJR2inqj+Yix9xOzyXxUm1Fzu7nDfM58" +
                "3va+YMt280Vzj/myucC8rkRcat5g6zebJWapbcfMMrPcjJvL6f" +
                "o0V1jpOwpk8X/CvMdca6+Rr5n1tn+9eb+5wbYbbPmgudm0bJua" +
                "tr3H9dmYW2xx+W6x5VbzsUL2CVtuM1vNp8028xnzWcH3Llu+1J" +
                "/jKXeWf5kpX+zaeFzKeM01spY93f/A63NKetRs4JV4gVmogczn" +
                "FPrnMjkLPFsfg04104lkwte+pb4702XpsmRidjckXA+0tCc5vE" +
                "PvDudT2kAneemsCJt/XeclI4IjtS6+lnH3OM8BWWgRZCSOT1Ym" +
                "K227klrq+/H+f0sJHwEt7Unue2h9z96zHwltoGPzuTKOA0mICX" +
                "nHGYGNi69ljFzIu4ws4/FIEl98thjzRX5eao5ltwafRQockEN9" +
                "fgnQ+c6F2Q3CUA4kq7LPvzGc/3p91Swk65J1vk7WQULj3hi0JO" +
                "M92KPIHlryn++WMblO8grjQBJiJIJHBCNik++S0SUTzkBGDiNC" +
                "JvGNRuuMl/i2dYauz9aZbGujMXs56TmWH7GMPGlYKc2/GVuFGN" +
                "377Hg9h72TXKr1ZL97PPQnfdZnTr30cHrY19TSyPV6y1ztJBID" +
                "BNUcI20oCvVDJNd4HZhx7zKqzj20ABL4/NvSKs6eswyZhkxw9H" +
                "UH0gO2PYCWRq7Xu9rVTiIxQLg6m/M9bomx70O+7z6JhIasqAdE" +
                "HJUQGi8eG1xIkn9HWsXZg0nMNGTC5vMAZy80j1Cvt7pKUy3RZZ" +
                "Dn5tU/JVdFGAZD/49Gc6QH04O+ppZGrtdb72onkRggXG2vz4Mc" +
                "I20oCvVbJyUSGrICM0LEUXXuoQWQwPP4cWZgEOek5c9nkrMPn+" +
                "z71+f7bPwXNU21RJexe/iLi3MlDPKzd/Lc7BaHV3osPeZramnk" +
                "eq39rnYSiQGCao6RNhSF+iGSa7wOzLh3GVXnHloACbzPKfYtLb" +
                "SctPz5TMK2u6L81LDKPtXfW2IeDr6jeDL61uLpRiObC2Trar8h" +
                "fGxxrs99Zwd8i7t+tPErrs+j6VFfU0sj1+s1Xe0kEgOEq+398y" +
                "jHSBuKQv38RxIJDVmBGSHiqDr30AJI4PMHpVWcPZjETEMmfCY5" +
                "+4rv57cFT1k76sahVpd2frY4V0J2e8WT4I46NosXv3ZtiPXO5v" +
                "OOEewIPPUar/enRrrej6RHWmdcTS2NXK93S3rEPW+mRzySa90J" +
                "e9cjjJO4vsdSFN93z5IcmT9MGv/M5qz88yaPgRHizY6DgeQFC4" +
                "/0Xr3ERZFW1O8ehy3ZyJx4PORJz5vcNlhJ28veXcFa2lw3DrW6" +
                "tHNyxPtxm+vYjDo+dpXaU+E+l/1/9Ml4z43vb4V7UVX7cSLi83" +
                "y/LdyPa0/F3Pj+XewREjBwXvx+XPyJuPO8zCLcLayap2ifdErb" +
                "j2vPtGesbobaAjlD495tJOEYnNBxjLSBx37cFySS64iBZBFHhV" +
                "eNF48NLiTh8ePMwCDOSc+fM7eyybZ9jnC1bwvNJI17W0nCMTih" +
                "4xhpA4/95+efSiTXEQPJIo4KrxovHhtcSJL/JMyYewCDOCc9f8" +
                "7cyqbb0/3xdKkpZEX5s5eWY4GlUTYn9aGnit8VTFMcjuJ4klNN" +
                "Z+wHtpyv7jU/FGaseY3ZavxDhJd09qAu5qfcdc++Wt4d9mjImv" +
                "tNJSb/xVD39T3DRQHK9fioyj5/ehDDOj1J4V/i0mfSZ3xNLY1c" +
                "r/1XVzuJxABBOo6RNhTF96Q37hc6MCNEGBVeNV48NriQxOcU+5" +
                "YWWk56/jKr6PPos770r+G/xNohPtM+2/gvOKpY5IdHzzN52Rf5" +
                "vJm8nO3UcEAO61t8L3bpwuwGYSgHklXZU/xB/uv1VbOQvpS+5G" +
                "vfkqw4l9izL+EYnNBxjLSBx37EJRLJdcSAs5CeQ54aLx4bXErJ" +
                "kjBj7gEM4pz0/DlzKWnuir4PuTd49ti1CN8Dv/H8rPcqrqONny" +
                "xPlvvat9R3Z7op3SQlfAS0tCc5vENf/B03hTbQSV46K2B1XjIi" +
                "OFLLc+K+kQt5l5FlPB6J45PxZNy249RS34+bN0oJHwEt7Unue2" +
                "iTcr88tOE6jtFZST8xLxkRHKnlOXHf4EXeZWQZj0eS+GJtLPVF" +
                "rvfm0uy+YA0VOCCHWncBOv/VwuwGYSgHklXZ3//QcP7r9VWzkM" +
                "wlc772Lcn8uLlBSoDlaG5PctjZGXyU9BRTeuE6zovHwZj70Xhx" +
                "xrAFG5eT5puzB8uYachE4pPTyWnbnqa20Jymcf64lADL0dye5L" +
                "CDfTkPgReuA0bGwZj70XjJiGBFbf6Y7puzB8uYachE4pNTySnb" +
                "nqK20JyicetaKQGWo7k9yWEH+3IeAi9cB4yMgzH3o/GSEcGKWp" +
                "eT5puzB8uYacgkxCvfJ3+/3H/f8Vo/5fTuWahl1f579sBQu1M/" +
                "WLwcsgvL3oMj+Fz4r/Pzl6iaz/zESJ9yz6Znfe1bkvlx/kuScA" +
                "xO6DhG2sAjYnJkrAMzsJJR4VXjxWODC0lmbwgz5h7AIM5Jz19m" +
                "1V/7874Ez+/z2Y+Du1qBS+bP6YkhQDf/uTC7QRjKgWRV9vnvhv" +
                "Nfrx80C53dnd2u9n3q5b/WMNATWuuHlrpFHSOO1Wzgk3hxZtye" +
                "atfOtrmtZB/mwgvPX+ZTvy8ndtYeijHyLbJgr2u+2rtuMWgHLN" +
                "w7q9uJk8z8/TN+P87nJPf6tN1D7b0/7T08/XDvczX7v4w037Xl" +
                "suK7A/nu14rsW/59LltfVUqvYYiZCu/K+1yDDlP8/sO9z1W02x" +
                "VE8T5X0Svf57Jt/30un038PpfPyb3PVbQbmL/ifa5yJN7nKiR/" +
                "ku9zqVfBnZ07fe1bkvlx/hspAZajuT3JYQd7xJReuI7z4nEw5n" +
                "40XjIiWFE7u1H3zdmDZcw0ZBLiwz13foW3vvf/t959TgtZ7+H7" +
                "sIPWu60PBet9bbDet/zvrHc/n8p6X7vQ9R6+v6nug461x3ztW5" +
                "L5ce9+knAMTq/L5shWysk7bIu/0SUSyXXEQLKIo8KrxovHBheS" +
                "8PhxZoRtXRznpOfPmVvZRDt6d9LLipJ4bTmeIATqWM/lsffWRd" +
                "B5Gw3FrammU0dJhjI2t2ldJLOoY8CjDJJR9O67u2vd7xW71xWf" +
                "5VfZp8xj5dPzD4dZmd13VsivjyT23tB8ZXGe7LpXDNBX/F4x/3" +
                "0F/spI8t5zZHRjxZ2r3DXqBc/vzbu1Mf3eO9Tq0tYFI943uruO" +
                "zejj2/l4smo+8+eiv8EL5/D3Kq7PrHwPKHui+7dFZP3ogOgVv0" +
                "uc/dTQ/Ie8PrOfZ7W/ye+WT0VZ8NuY5pa6MUadVdU2XDeS63OL" +
                "Hvfc4i+Y5X8At2Ihmw==");
            
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
            final int compressedBytes = 2717;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNW12MXVUVHkVFjIZi4m8wDaKJD9r4k6GJDeSce+ZiMjTDMF" +
                "5KCz7zAo82JrzN3KtOZhIlGl9MJJFETEgsSDRp/Qsqij/RVCmY" +
                "ztAXBBptpfoitlXqWWfddb619l77nDN35pI5J3fv9fPt9X17z/" +
                "m7Z2aKc8W5mXIrxr3Y7K99XbIS1Vmdw4fa0T2ohIocW30qrKK5" +
                "sVke8UKdsS7Jhxol8qUzdpStILVHn7PzCtchVg22mZn+GdolQx" +
                "Z7a9+wMd1aJDKVdwOqhfXDnL8Jvh51xsdYdq0n1jXO3+DV1zGo" +
                "1B8wyp5WZrfhVWKtfdNm8oXh963ve/33pzA2N41N+ELerfFvX2" +
                "X+NWnZqtbz20F2CTZ8z0thdKZZTRecRoly0RjyhvzN9VP8GN1W" +
                "J39QWraq9Xw4yN4DG77npTA60zifB7vgNEqUi8aQN+Rvrp/ix+" +
                "hudYZPzswsHxWvd3OU/2P3I35lfzXiR/XYn6+cGltPRXV/UrU/" +
                "cxQ9XX7+4MS/k5zDLyv2k2Pvx+P+F1X723BOw586FX5XVfhU+x" +
                "yHvy4/vxn+3kaz//CHt7lvSVQsiwOyy6bRYb2u47yNammMzEFi" +
                "qfGY3Xb4m1ch38Mf3uT4zPcMn/VwQHY6cxV6ZTTZOPfI+IvFyB" +
                "wklho//HO3+s355lXIr+bP+Cd4h0TJEg84IDutS4DW9ZriXVg0" +
                "RuYgsdR4zG5rusNsOt+7yC311LIvkeGLHK+qHIEd+/kRVNRjLB" +
                "dGp3VoFT6ud3H4kq4GRubrXaQeNWLLzqQ8dx4OdVoFoi7ks/Hq" +
                "yX6WW+qpZV8inGcrX4Qd+/kiKuox5lvELEZH34hqHVqFj9OaLS" +
                "PzFbPUo0Zs2ZnEHKECrIXls/Hq2rpRHoVnsw3qqaWe42xxvFqb" +
                "22HHfn47Kuox5jq+gdHRNX5DEFqFjwM2ZGS+bIN61IgtO5OYI1" +
                "SAtbB8Nq7W/6C0bHmx/LOIsq/xnNUjw6jNpLfiIO3NCMFhBEdF" +
                "Y8gb8rfVD/NYi5A5eEr82MpHV64v++qIXfmQef48sLPfBVc+so" +
                "O19rbkP5m4+ibmtPLhHdLVsJ6rv+pU4cam5/ndtp6je7uuZ5fn" +
                "eYP/tDmeF6UtFm1s+Hex8gGi7Gs8Z/XIMGozDWfbIu3NCMFhBE" +
                "dFY8gb8rfVD/NYn5A5OgNe4ZZ6atmXCOfZyu+GHfv53aiox1gu" +
                "jE7r0Cp8nNZsGZmv9wr1qBFbdiYxR6gAa2H5bLy8Mz1HO/XsiV" +
                "W9D3l0snOxd1/irvnczp3vbbVS+elryzbLc/pCtkk9tdRznC2O" +
                "V8faYdixnx9GRT3GcmG0p0MUtOGADRmZL9ukHjViy84k5ggVYC" +
                "0sn40334/Wjk12P+o9oO9Ha49N4340/OeE9/cHpnk/6m/2N7nl" +
                "vno3vSn+2pMS0RjsyGlMfzM7Bh8V63ffm5ojzomlVVlWVPV0aW" +
                "5olEh2LJyxrgAFIbPls6o1vmR4nlvqqWVfIpxnK78Dduzn9RsN" +
                "qWWjuqZ7jtU6tAofpzVbRubLnqceNWLLziTmCBVgLSyfjVdnwH" +
                "luqaeWfYlwnq38EOzYzw+hoh5jzrbzGB2dibUOrcLH9c5X70MC" +
                "rfRhvt556lEjtuxMYo5QAdbC8tl4ZV/hlnpq2ZcI59nK74Qd+/" +
                "mdqKjHGFVXMDqaQ61Dq/BxWrNlZL7eFepRI7bsTGKOUAHWwvLZ" +
                "ePVEeiu31FPLvkQ4z1Z+F/DsIydZ1By+Gkbt6OgZutahVfg4rV" +
                "mrh0bqUSO2tHKPo7h1+G9PXchn49Wd8lIVmR++Ru3oqmJ+HD/J" +
                "FrUSE1/b2vcwXTPD/1XfB984ekMxT3sSd1GqAMUWxrA1enP9Lf" +
                "MtIT/1w/+mVYb8WAvkh5drpjepK+3Z0r86O0s9tdRznC2OAyu2" +
                "5+uRcRw13XvAWUFoFT4OWJ8RyrWvrZRC1LNZWxF8IVPj8+ffJn" +
                "v+zF6c/vuQSZ8/Wds034f0znFLPbW9cxKXjGCs7fl6ZM/5qyRU" +
                "93KC0Cp8nNbsMUK59rWVUoh6Nmsrgi9kqq8PS9IWSzZmrdgunL" +
                "/EKJaSV6alDu+XlmhvRghufH28JqU8xd9WP8xjfWw+rlPs55Z6" +
                "aov9EpeMYKzt+XpkHC/n/XYvGurQKnyc1uwxQrn2tZVSiHo2ay" +
                "uCL2Qq7QPcUk9tcUDikhGMtT1fjyzcN+FF8p0/dGgVPk5r9hih" +
                "PGRNzSSegc3aiuALmcor9OnyuHlHdpp6aqnnOFscB1Zsz9cj4z" +
                "hquneK04LQKnwcsD4jlGtfWymFqGeztiL4IqbjtFPPnljV/f1f" +
                "nd73HY8iL3RFbuMt4/HJ8tPW1vi89NqE7+u+sHt/f8TapvG8VP" +
                "9kXi7P92uzl6mnlnqOs8VxYMX2fD0yjqNmSocoaMMB6zNCufa1" +
                "lVKIejZrK4IvZCp/Yv/glnpq2ZcI5xGD7fl6ZBxHTffIqXVoFT" +
                "5Oa/YYoVz72kopRD2btRXBFzJlJ2innj2xaFt/T6fj+0QUebQr" +
                "chvXzxOT5V8PbdUdfyBtMbAxa8V2MUhVS/M0b6PrikEzTtRahb" +
                "5en7+tfpgf7bGjU3Wa7kfr10/4/f2xBHIX3I+S2nbo70Oyp2mn" +
                "nj2xqvX8QLcK0dXnqB+31bere7J872hX/KRKi4xb6qllXyKcl9" +
                "joXbDDnDo33u3HwdasQ6vwcVqzVh/WilkxollLqMBWBF/IVOMX" +
                "pC0WmmKxXTj/6VMsJJUudPgJL9DejBAcRsTKm/jb6od5rEXIHJ" +
                "0BF7ilnlr2JTJ6L8eBFdvz9cg4LpnEmVjr0Cp8XO/C6H26Wsgo" +
                "tayvrVBh8PfJkQJbEXwhU3WluJQ9kV2qK39eomvB7wMYB+T4zW" +
                "70W8LsCT1mwutjy7j+qxZDtnyaxou2tvrN+XgV9JbfxB/e6v/v" +
                "uKn/UKn7oRAHZGpb/ZMdU/+ktvL/HS0spEtjZA4SS41fPdmtfn" +
                "O+fRXyr0jLVnV/v8ZmY9s/PmNMl4xFdcFplFXezt9cP8WP0ak6" +
                "jc+feyd8/vze7n0fwtqm8T6Efn80upHXc/WUu56RptVnOq3nI7" +
                "t4PR+Z5nrW66T++nHutno9r4vW89m4jnM/Oj0z9Q0qt5Z/PbRl" +
                "l/lj70fZ5bkFDwdkcj3/asfUP8Frt6ap8Wg4aDEyB4mlxou2tv" +
                "rN+XgVoivtV6Vlqzo+b7bZ2G6utrWMRbXh1m+xKKu8nb+5foof" +
                "o9vqLL+1/rb48VrzZ1zkB1uPre82vDv6RLfjc/lIB8zblP3O8t" +
                "Ph/aJoW2553lmeb8weXr7fvcr05/rcci8x9tfnJaIx2JHTmLl+" +
                "9jh8VASn5ohzUAZVlhVVPV2aGxolkj0ezlhXgIKQ2fJZ1WpWg7" +
                "lB2Q+krzID8ddvk4jGYEdOY+wYVBzf0TYsUudEgVURs6Kqp0tz" +
                "Q4tEvrw3nLGuAAXxnPz5a+USyWf5M77SvyTRUXn97P1QXT0qXD" +
                "67lXvd1tDdx5EujZE5SCw1XuYT5vU82/lTq9Df19/HLfUSEX/9" +
                "UH9f7weIAMsWxuNjrarKXbp+yGlzGhPyIFLNP6ELzFaR8NI4m2" +
                "eb62kFltmq0jGLd661+P/NL0ZPKc90ObZ4nPM8f2rnnvFGt7Q8" +
                "T51s0tbp+XyS3xf/H7PNc3U=");
            
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
            final int compressedBytes = 1968;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0mTFEUUbtzBUHZPCAyLjCwxLmFMKG7d03VQMIgYV3ZFUX" +
                "+AnPQA3ZkwE90RBgHqQQ+KYOjBkzcBxSVEVBZx3GVQCVFx3MAQ" +
                "cRTsrqys915mVnV2TXbVhDVRWZnvvXzf97Iys7KyenI5/Sh9J3" +
                "Msn7M4ygO5Jo7C2zkHR3IvdjV7TuacHevWhNjrE3Jenxu2R2u5" +
                "FSYVJolUXKUsKHdSCdhia1xfyqEe1AdM6gXrMC+MA2Xsx8SLIg" +
                "KrkF2n2TdmDyx1pioT1V4/2K1hboHb8c4W5nJdS9Pph+wWs9wO" +
                "v3dkUtzSvNLcUq1dS9f4pRl4vPcctfIwPULeqUna3bVXaUoD/V" +
                "UR7cwj7GdqkqubZHRdxBywBeeg1MQsssVWl8T70GpG1UvOhNyt" +
                "1WwxW87uJrKVYf88ltBrxJhiq2rn/Y7G85IG+hXN9U/FapnDme" +
                "dhmaus0XQTrTxc4s9Ua43z19psn+92+HUrF0zLu8h6SZ9TPmpi" +
                "PvHnz/Kroe83Sn1BTlsDlnf46WsGRrtr516DfFtkDG/56AeC0v" +
                "bg+qaf7lFjKu80eHjPdv4sv1M73y2/H9sO54Tt2Z773x3px1R4" +
                "UM+59pydL9cxWYzTR8JV2PTh289KGxI+HXamzRTmT/aY4zZgw6" +
                "A9H235+H5BpPVrPRVlLJE24irzpjKtif1jjYqt8sAszHwxNx0R" +
                "fOk4UZGUtqgRmJjqeCqS4Q6uC9dLPe7vXu+MbOeJDSdaPA/FvG" +
                "9WnnP/vtl7Wbbvm70zW/m+2XWiy79f8irzolx5QmqlFGulrjwg" +
                "S1iOvcoafjyzqAzryIqa4MiSylPnJfWUI0g2KLWoB4yPT7UddN" +
                "Y0cq/b64a8OGuxk7WasJEptjbl1Zom/3GHsJB2JnvwKXlhz7i+" +
                "TOtXtgfzoezVWPBJcait6chvlqnI+eP9aarV81H7daqNjYZa2d" +
                "hhK8q8MX68/yh8qG32U1xWXCZScZUyUa48KyXYBv6Erjwg61K5" +
                "9A51JSa21HXADFhRVPBq4oWxgYuU9GxXI8YegIEekzl+GpVxpJ" +
                "0Kn/SbhutqPv97I4u1kyOeR7OzW89Xnh+27Xki/ZpDXy8VFrpf" +
                "LznjnXC9FBUTXS+xZ5Ktl7x+r1+k4uqP935ZrmyVEmwDf6DDNr" +
                "QOeAxmk9HUEuskA8pCRwWvJl4YG7iEktFqxNgDMNBjMsePmVOJ" +
                "Ybxvcz8ein9mO1O0Gj/2/eh19+PdOz/b8R6F7+p7XGledP/svX" +
                "JIEXcadwdedtOebGsD/UsRz/crrObPF5tvT4v+uasF/XN8xv1z" +
                "fKP2ZK8k75+FtkKbSMXVfwK2yXJlN5WALbbG9aUc6kH9IJ4xqh" +
                "fQoadwG8WBMthSGdWrdYFNHd/kG7OHuHSmKhPV3jBSwl5Z2eui" +
                "J1X2kf5xYbbPo9bis/2186sgf7R2fsi+l+O9fKBy0Jf/iPp0+H" +
                "2THa+dfnuz4Esi83Xsh6D0qxjvLPz2x/pKfey3Ym09zT7QeHzG" +
                "Pmdfsm/YIXaYHWMHkean2knehtgn7GP1+yb7FOW/YN+K75vsCJ" +
                "L6vxpkPxeV9TzbFzl+Lcc7G2C/hHdr0BsUqTdYOVzPy1I9X/la" +
                "SqQUtKIkddiG1gGPwXplkFpinagFzKSFigpeTbwwNnCRkqJSi3" +
                "oABnpM5vgxcyoxrD8Pafes6e/vcPBx9e/vxT+amiv6ozTlBmtj" +
                "+f1dW39a49v2T34xH2P5/n7Kxp/97+vq7emd5WamStqervAT7Y" +
                "ectumfUe0Z0T//yrY97fGT9c+49Wd1hPv1Z/F0tuvPKHzXv1fM" +
                "b5SpyPn98wzV6vmo/qna2GiolY0dtqLMG+PH+4/Ch9pmP7z29s" +
                "XRneRT+CzUP8/zZZfbjXdOVrN8ruif/NJQMq3UxzuKZyJGjf/d" +
                "k/tvuHxqs+Odo97F203jnfujiM9R8fnkIY/32XxenL6wWc81d8" +
                "TV8y7KeD2fOj78fqmwMWF7xtTzRmXcnqOya0/emdDDkZj9x38y" +
                "3v9MHb/wuJ5L6sEQz98Zt2fq+IWn9FxSD8Pv4NemjQj7S1WrvX" +
                "R9vcTnx8xfZ2c8f6aOX3hSzyX10MK7fmfC/nl9S2eTfDEvUnGV" +
                "MlGujpQSbAN/oMM2tA54DPrHBdQS6yQDykJHBa8mXhgbuEgJxt" +
                "cjAwZ6TOb4MfNi3uvwOmoYHeLqx9sh/2rtOQa0UoZzoiRz4qQ5" +
                "uIb+J1BMrEOjskPHAYlqQy0wIjAKuU6g6JQJZkCRVUSQUXvDDL" +
                "M9bPOb3O4v+T5PZjt/po/v7Q+fR1Nb4H1itu2ZPn55TtiebS78" +
                "VaeReMZl3J4p4ec3yTQf/qquOotq9Xy8t+Y01ErYVdttrHTmjf" +
                "HjeWDP5tp2fuoH2l/qdH/fiv86Wpff1kC/oLX4EaiL8H4yv0PZ" +
                "T75Zs7/d8Lbe3O8ZXO0yJP09Q5Q92U/m3e7+/70U7hhUCThf5f" +
                "de5RnFlzR+vvN7UDwj+L32HNcFttzwX958sUV/eYiU7qvj+7nV" +
                "gQS9FfDlfKVS+4GAxaKgfJefLuUrmnoehW9k1fnu10veuRk/j1" +
                "LHh/266g0tWH+eyrY908dH8+eNLeifYzPun2MzbM8ure8O/Xvx" +
                "cUer5KTfi63xE30v/g/5cI/u");
            
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
            final int compressedBytes = 2375;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IJVcVfhoZEP9RA6I4NOPgz0ycRqd7dIjdPamq6Yga1K" +
                "E7KiqGjqIEdAgRwYzO1Hv9knn6DOLGhW46RjRRJtIIuhAki4AJ" +
                "WYwY6YVGpR03prcuZqW36tSp7zv33nrdGV+9IqlL3Z/vnHu+79" +
                "xXdev9dPd64TF8UHvj23tTP7JX9zo9Zs/fv69ez4/1eoM3TDd6" +
                "+p9u17Nd/vRUekpqaRWT8fiTirAPCmzsY+cgYnV9vMJ6sk0VWB" +
                "UhK6LGdDE3tCjC/GFmUBDmFM+flVskvD6Ho+lfn10fg5nf74Ob" +
                "6/t9rYX965Ud75+t8qfn0nNSS6uYjMf3KMI+KGIb7Olci2t0zC" +
                "3zea31ZJsqsCpCVkSN6WJuaFGE+cPMoCDMKZ4/K3fYRrrh2g1t" +
                "S8uGjsffVIR9UGBjHzvHRbkfqMvnkPVkmyqwKkJW6IzpYm5oUY" +
                "T5w8ygIMwpnj8rt4ge+de1N1rsveSO0UKr9/tquiq1tIrJOFtV" +
                "hH1QYGMfOwcRwcmeoQ3KoMqyImpMF3NDiyJZkDFHgIIwp3j+Ni" +
                "v3NL+lf7z/NteWr1v/HXx9ZheD9/o/ibwfONLwPjZ499B/1xTf" +
                "JR/ex/6+hufRxQb/owHy/heo6LRb0fPpeamlLVf6vI5HpxRhHx" +
                "TY2MfOQcT6lTSeoU17rMqyImpMF3NDiyKb1/2MOQIUhDnF87dZ" +
                "Td4/xxda2L9Od7t/Xv5hq/vnofSQ1NIqJuPs+4qwDwps7GPnIC" +
                "I42TO0QRlUWVZEjelibmhRRHIKY9sZsZzi+dusomv8qrA3xVfw" +
                "3x1/fm+Zf9LzaHTrgSK8qJ5Hm/89yPNoeOXGnkeT98/h4y1cH3" +
                "sdX58z5+//q17P7Rbyeb7j9Xy+w/X8dQvP99WOn+9f63A9f9PC" +
                "9fHdjq/PVvmztWxN6mwNiI5HH4JVMe5hPk7bQ5utxTmtjX18Hi" +
                "C+j/VgRihS3s2hZbdKWIFl9hmBsX+2nq27dl3aEl/XkpxOTsOq" +
                "mPYKXGzak7OoZZ74IqbEVxRRYKNs1kMeIL6P9WBGHSkm/NbOEV" +
                "iBzd5nBKb+5ZydZMdx7GhbZrxTlSVXKqRGl2rrDtuMj/Mq+0vw" +
                "0Z7rL7GntakCVuFF9nSy9nAGPMl/yc+YI0CB6PeV+kpwVLbdZN" +
                "e/NgRz9bIru4LomSzrWGtrl9mV13I0+jJYav/d2F6kuNZa4l5Q" +
                "4OOMiiqLNilQpX6EOGbZY8+jwdXh71p4Hnyv4+dRq/zJteSa1N" +
                "IqVpZjrlRIjR6rrdfYZnycV9k/Bp+6V8VEBNg0GpRBlWVFVNYe" +
                "zoAn+R/zM+YIUCD6faW+El5JOZs/bw6ujn80/c+b6eVuP2828U" +
                "/n+8/YMfy9rmeSBZ9+33yQyJvlL6Th7GZ0dsfB+aellPbPJ1rY" +
                "v77V8f7ZKn92MjspddEqIiU5mhzNTm7eC6TAtFd4Yj7Ooi683O" +
                "t7VKMivsRkTua2unweIOXn1rMWs3aei1pVWbv0+w9bHZpFGNVn" +
                "Uv/SvpC5fbOos+p3P+kXxe2fD8GqPtyT0WAPI4mAHlqNn160nG" +
                "yjbBZCHiC+j/VgRihSNQW/tXMEVmCZfUZg1r84Vg4XRSMVPRmN" +
                "f2AxrmEZ7OnYxsDB8dMLK4f3v2/UX31jcxBVdUGZbwGWXojFZ8" +
                "yuAmcrHFomKJsrCkZyuuvzxxbjmi22tj0/fjq2tob1nLMRY3MQ" +
                "VXVBmW8Blo5j8Rmzq8DZCoeWZmWNz6MnW3geXOr4edQqf/ZnlG" +
                "LEiLs+H7YIj+Dt9k9CFNfoYKny+Q7HsbaYLl8VfOO6LKNaoKbg" +
                "D+3w0/g+s+VjJvbPnkVx4/LEePyoRXgEb7eehCguPbTSc/nkHM" +
                "faaD0bVcE3rssyqgVqCv7QDj+N7zNbPmay/hPv96dbuN8e7Ph+" +
                "nzk/reczLeQz7Hg9W+VfWVhZkHplAYiOx4/Aqhj3ZDTYw0gioI" +
                "dW46f3W062WV0+DxDfx3owIxSpmoLf2jkCK7DMPiMw9l9ZXFl0" +
                "7aK0Jb6oxT2Pfgqr+nBPRm4965FEQA+txk9HlpNtlM1iyAPE97" +
                "EezAhFqqbgt3aOwAoss88IjP2TrWTLfQbbQqujZMut58+KcYFY" +
                "Hy0yGuxJDzaeI33g6ab1hEVnaQ8eIat6xHQxN7QowvxhZlAQ5h" +
                "TLn7492WL1xnJFe+PHmizNSByj/Wswo++RrnTDP/H7z1+08P3n" +
                "Ax1///nArL//pOf7H1t4vj7U8fN95vy0nn9qIZ/+dOIMn+2Wv2" +
                "GX2U62pdZWR8m2u99/mWyLl/XRghnsY+coi/bTb1tPWHQWlHF0" +
                "yxrX7s+AJ/yZP8wMCsKcYvnzSrJ6PujvFT/ae8kdmy1/nujf0r" +
                "yet33i/4p8qtV1eWQf+6NxvCkn+zza/PkLfx6Vd9/x9Lj2LFac" +
                "ozsE1bH15Rls9yM1M8sc9mJ/xbXWMikD1huPOlz2M45FDdU2r5" +
                "zP7h+Dm+rX8s7ok+Cfs7gvh7sH8Pkr9f/mzmv7z9Gchn/fJ/Zf" +
                "Jlr/0bDuR9Ij2rNYcY7OCapj68sz2O5HamaWOezF/oprrWVSBq" +
                "w3HnV41s84FjVU27xyzJ7fl98U7p/561z9xrJf/t6e3+zOt7rz" +
                "G5VHudfk76yu5738PW7krvX8vSU+X0dayD/gRx99qrKdzW/PV1" +
                "374fwj8Rzyz+WfL9u7yvru/Mv5V/KX5S/3vF6Tv77qvcmdbyFL" +
                "/Y4/fzfx35lX7/TzD1btrflSftvk9czviGCfNaMNd34xPZGecO" +
                "t6QttypU/oePy4IuyDIrbBns61uIwRscrnM9aTbarAqghZETWm" +
                "i7mhRZHRp/2MOQIUhDnF82flFgn3zzN/uLH9c3SX8f/qhH3onh" +
                "vdP0dfuLH9U3NqZ//MrmfXq/Wds1hxnnlOrDpWX6Byv1u74hw9" +
                "zixzsuuXPl6/ynOhNq21NGUABUWUS29nHbSez9ksoGTi72yBPZ" +
                "2zmLCn86nb7Ypa2tJzXsfjXynCPihic/f7PPuk86O7MUbEWsk8" +
                "c4Q27bEqy4qoMV3MDf2KXL7qZ8wRoMBntnxWNftP+n+uA75vf1" +
                "H9P9eZA32/VHhN7f+5fltHbeH3966PdnNKnkqeklpbHRW90ZeK" +
                "ukCsDzzUxj52jrJIz0bjuLBBmXr4rIga08Xc0KLI8F47K8yeVf" +
                "pKw/xtVr3/Adszzto=");
            
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
            final int compressedBytes = 1720;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2IHEUUHtSDePFnDUY2+JcIHgwRFxU9Tc8PgghectCTQZ" +
                "OTnpwIya6i0zM7mwYVyTHsJesqBqIiCAEFIWAU9hAEA14k6CG3" +
                "HScSFkWM2r01b773qqp7enqruyfTzXR1vfe9975XU1XTVb1spW" +
                "Ie/hW6W+5XnB/eLZVSj7zj+/t1SftNR56fskmD1914766P0Z+x" +
                "y4PXYtg+LKw/DSULk7Oq9+t9dVUlyVQdEo7BCR3HSBt4VBGr1y" +
                "WS6yieZGFGhVcbLx5bZhGVPL6ZGRiYOdnz58zr1vHMxvsflUrt" +
                "itvx4MpfVj9xdm54Nb/DGdW4ROlNjI7ubHIJyck7ovCYkEmdyc" +
                "vGwc5d8pa2iBu8JaNLJsQcWdgi6C0ks07on9fcz9fB2+X+Hp04" +
                "m6f35gWcUY1LwjHgSQmvAU0yJSE5eUcUHpPbcJ3JS2cl/Zi8ZE" +
                "RwpJLnxH2DF3mXkWU8Honjq/1qNEf3qdz+xehTvfaslADL0dye" +
                "5LCDPbWD7oXrgJFxUOd+bLxkRLCiMsrJ5puzB0uTqc5Ex5tH5/" +
                "3KDB/LW2lQnQ8dRvxrNNe9M4Pt+UEq1N8OI16/Idrl34x2/6RC" +
                "/ZfNe/Vq9aq6qpJkqq5LgOVobk9y2MF+2Oc7uhfoJC8eB3VgpU" +
                "zqdVuwWfnT7puzR14mU52JxFcH1UFYDqjc1gyorkuA5WhuT3LY" +
                "wX7Ynid0L9Cx9hzIOKgDK2VSr9uCTdieVt+cPfIymepMdLxlLr" +
                "6Z7no3pZq7NycZEY07yp0n8o3vLXgL6qpKkg3rLSkBlqM7m1yn" +
                "5LCDPWJKL1zHefE4qHM/Nl4yIliN2LXsvjl7sDSZ6kx0/GhX6d" +
                "bR3spjI8ZHbd9B+6GdfIO93elw7ZdSYG5j93eFn3tS9KBhTu0n" +
                "x/h+LlH7Yjv1PhnWm7356f19Dz7KOCIPuUNZ9uu26lvqqkqSqX" +
                "rwHkk4Bid0HCNt4BExOdLUgRlYyajwauPFY4MLSZY/0TPmHsDA" +
                "zMmev8xqtBZuNVvRVd3TnbamHmKkniNtVmRpt0jYWWhxbDwfMJ" +
                "bMuD1do9I7zG0le9S8w+SLtwvicOzYGe6+WV5veq+6Q008fz44" +
                "g+35ijtUyv65d3S3b/baM1hPhfrYYf88OtPj/WV3qJRPfrm93/" +
                "QvuWPZGdOD/B9jWupIqvY8ku39ZnJ7esZ493+avD17c/Ht2buz" +
                "8PZMPYe5ak+Md++A+/V76eP9QNERXY13yzd1e5iPozWX/3zG36" +
                "Mvi2jD5sHmQdyrjx1DV4623euWpPP2xPnXbbjHeD5gLJlxe7pG" +
                "ZfR+E3wkez0X/pFxJHZM/xztz3s5PNn3ni55vBe+WkF7Bl/N3v" +
                "NS7+48vdd21XapqypJpuq6BFiO5vYkhx3sEVN64TrOi8dBnfux" +
                "8ZIRwYrK4Bu7b84eLE2mOhMdn9Q/8zgav5TbP4uI31yITtTURy" +
                "IUCldoouclVZc+7P4bl5sL6RhxPzYbeCVeYCbtuaxx2eafy2Qr" +
                "8GxVDDptzKqtaktdVUkyVdclwHI0tyc57GA/nJO/1b1AJ3nxOK" +
                "gDK2VSr9uCzco1u2/OHnmZTHUmOr7Q589u2D9OOlpv/pxxvJ/M" +
                "dTZZbayqqypJpuqQcAxOpetskq1uiXu6C59XPpNIrqN4koUZFV" +
                "5tvHhsmUVU8vhmZmBg5mTPnzOXkmL2l7wzJT9/5hzf3+8/6u8J" +
                "yye2a/smH+/+3tT7S4+E+XzhiPf9Y/SPx7RnTHz59/NZ9kP8Z8" +
                "pYvwffl7x+vzBj+yGfl9ueruJPRXuGv+/e2UqpR/Hx8+2fjfVy" +
                "+6er+FPTnqdLbs/TebdfbcDL2qDb5vXu7trAxNrsdYkdK6XBD6" +
                "aVjrF7796bzEFKbXfy3l9Lzis58yTGSf3Tf2HH/XOtmP75bsw+" +
                "p6v4WcZ7Y8P9eHfmK+t43yirPetz9blwRF4UWWR4v8kklyKvjt" +
                "bvGd9vxsU3x7v798XNY052A49NU/90k9NETIfr9/p8fT6c999w" +
                "673u6P1msOE2vr+Wd3uG3+Wik/65WJmiI2821a+jMypVje62Yx" +
                "9P5yFZDi/S+055Z9PH5WTiXTFl8+eSkx6xNFXz51LRI2KS9ZE3" +
                "0eiJ1u+OVuGLWffnvVzHe+NU45S6qpJkqg4Jx+CEjmOkDTwOVx" +
                "IXJZLrKJ5kYUaFVxsvHltmEZU8vpkZGJg52fPnzKUk6/o9/d+D" +
                "ueuftfNZ+2ftfKqnh19vlP0QN8fKA5n3k38rd/6snXO8+3KuXD" +
                "9xdq54Ff3+qHfIEe+M74+C32Pw8v8vrWZ8f/Q/0NgDzg==");
            
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
            final int compressedBytes = 1706;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWztvHFUU3poOMFS2IhQsIfOSQIBEtd7JlP4PlDykiCABAi" +
                "rW9q53HdGmMA1BBFMCJhQJjhNwRExBGVlyE0G6ICEBDUgRPnP2" +
                "7LnPnXPPzO4YMVe65znf+e6dx52ZtVstf+udbU1xG/zSanTrn5" +
                "p1xd6b00TvbDY7n9Ovnz2APUjo0SZP7xz6OZf0kG3u6fspUsbD" +
                "ZBHOMzmHKlLctk0txpDx7KiNyPXcSv724dtTvd7vNnt+Dn6dLn" +
                "736e5T3flj+UJhPZ4+n93TEf9LnueJVivfrol3yX2w+1zYH6vf" +
                "XTSttY+PPc8nMnp5dEf5G3uQ0KNNHoyzj/WQbe7p+xkzeGcb8z" +
                "BZhPNMzqGKzNy0Tc1l2L3o8rQZ2Ihcz60UWI/eTTsuq/eSVrsP" +
                "ZrSqvhO53n+b+nr0IvYgoUebPBhnH+sh29zT9zPmZB4mi3CeyT" +
                "lUkZmbtqnFGDKeHbURuZ5bqf0jNJBokSbfYvm+X4OeWrUsng8l" +
                "+ZClm4dZr0e1raPa9agnWY8gS7cetW9DA4kWaQnH5bbUn4qsqV" +
                "oWH/wpyR/8pZuH/Cg/wh5lcWyOyGaPmcONY2aOvQ8jjo+9lenH" +
                "SDNZ2VUZNcTLrG2PovBsuiM2Eca5m/6YwuO3R/U/vN7Pi6738/" +
                "rnz6rPSydziz0v5RuSvWVZqe9Hg39O7vm5+rvu/Nz4XHJ+Fh7t" +
                "+9En2IOEHm3yYJx9rIdsc0/fz5jB954xD5NFOM/kHKrIzE3b1G" +
                "IMGc+O2ohcz63UvgwNJFqkFVmiI8T5kkx5drWqsXhsTLXx+hYa" +
                "SLRIS0FIyUxF11aNxYeVR9HU96VA5vH9c3C/2fW9/4f0/qk8zj" +
                "9AA4kWaSkInmc74legy6vK4u1t8ShUTNvfQwOJFmkpCJ7ni4hf" +
                "gS6vKosjN9EoVEzb30EDiRZpKQhSvwY9tWpZvHMuha2C101oIN" +
                "EirajdlSF4nLthv41ecT5v6uKxMYXZKnjtQQOJFmlF7RUZgtRv" +
                "o1eczz1dPDamMFsFr11oINEirXi2eF2GIPXb6BXnc1cXH76Wwv" +
                "ZkPC/li/H3zbym5xL195BF0feQxfT3zSnO58KE+VxoeD4XRPO5" +
                "oJ3P9g1oINEirbjXPCRDkPpt9IrX+w1dPDamMFsFryvQQKJFWl" +
                "F7ToYg9dvoFefzii4eG1OYrYLXdWgg0SINtuWfvbv5eyGEGDLK" +
                "9bOmx85ef0M9n9d1cX9MsfyyChGca9BAokVaUfuWDEHqt9Ernp" +
                "/XdPHYmMJsFbyuQgOJFmlF7dsyBKnfRq84n1d18diYwmwVvPah" +
                "gUSLtOLqlr0f7Uv9NnrF+dzXxdffSmFbx1b996N8Xherc4v+fj" +
                "RfdQQlx/EWNJBokZaCIPVr0FOrlrK6L8mHLN08TOV5fmnC8/xS" +
                "w8/zS6Ln+SXt73Gz/j5/ZqfZ+YzVr+37/NfQQKJFWvHsm8kQ5L" +
                "VSsqtVjcVjY6qL1zTWo0nbma+aXY9mVT9boT5bmeTz9Wwlhhav" +
                "U8YFmoQtZaHmMp9UvwzfjfNcuJUbPj+/bPj8nHL99k/QQKJFWv" +
                "Eu8aoMQeq30avy1sVjYwqzTWfUPoAGEi3SivejSyKcA6nfRq84" +
                "nwe6+MaTKWzTGc367xWHjzX7vBT7f8P6/h4sW8YeJPRokwfj5F" +
                "t7hHU3xtvao2E/V4tHkAGzCOeZnE32LpZflfeYzMVlYCNyPbfS" +
                "rJ/nh6f/K+en8j60Aw0kWqSlIKRktnfq4q2LL89J8pfn1Ly+gQ" +
                "YSLdIK1Gfc7OHlEELJk8vDZqad3XtQz1sX98ckw0t4kh/12ajP" +
                "xn7Qeh9l45xWy9RDdsvCakUiZTyy0rzMQXMrZkYf1mIMWwaLED" +
                "u3nlvJ31Lvn2n/b9j01nt/uviT1qNl0dqR9rzUe6XZ9Sg2pnr+" +
                "v7jT7/SxR1l8gemP7a1Of3iKPZxrZpv7j/otRmLE0fedLReFY8" +
                "ZXoL5dh23KXb1r++y4uy+zgfoh7O5Fc1QU8Zm6TNz88XG7Qz1q" +
                "zlG9E9Zj13sIoyxiZ0nyzCybeXn9yfix+rx3DGfW70edvTqu9s" +
                "6e9nqP1a/n/Si/kF/AHiX40SKdes7hhrHVe7SvuyfrpB2PZ9fO" +
                "NGNUz2bhV2XUEC+ztj0KkGZ9f2SUS1kuU3/8JnPbM5v1vVPL39" +
                "PqUWR7Dp9VPj1cSrvee59VXt8/bXh970iud8hSXe+H+SH2KIsz" +
                "95Bs18O5Zsbx9e7ETFzef3R+HPiZFKN6bkW3an7YOfB9vs77Ei" +
                "bWD2EzZ2YSYuqP38r/F7+vnpc=");
            
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
            final int compressedBytes = 2177;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uII1UULff+xr+OIDgKMo40o7SiiE5XJYLoQhczPY5OC2" +
                "rrwh+ICkK32Ol0WqOCbly5EmchNO1KHEHd60pQQVAQRBFhRFRU" +
                "EBXr5ebmnPveq6SSVLqgU+R97j33nnNfv6qkajJJEr7W/k0KX5" +
                "2tZMpX97qk1tfmZbPN37qmdaB1ad7P92ZX5Ov5zJgZ9hXYbwgs" +
                "VyVJ87tqdHe2R6i6Nm4v4m9dGVjG/Mu3bkqSxruNd6WV3tllpm" +
                "NtgcEBH2PUvv4XxjpKkuwBi2Sf8lkVISuyxnQhgvWrhfnDyhTb" +
                "nc/1/xkqDetn5dYyON+H7M9svcxfSlAhtnVW2QwldsOdk8WVr6" +
                "AqpZ33k1386rxXCnVysuyN2xq3SSu92mQOi1qzl9XLCLwV5caC" +
                "Rcbe3/1lZ2UO+MBnVTDe18nawwjWrxbm59w2wqFiSn0lvJI93+" +
                "HG4bw/rH3Pc1jnsKg121AvI/BWlBsLFhn7Z9IGI61P+awKxvs6" +
                "WXsYwfrVwvyc20Y4VEypr4TWU3yLjcW8X9S+51nUOSxqbZ+vXk" +
                "bg3Udd4MbZGjA6ypWuMdL6lM+qYLyvk7WHEaxfLczPuW2EQ8WU" +
                "+kpoPcV3pHEk749o3/Mc0TksjMEhvvVTGutHYqyjXOmSRbJP+a" +
                "yKkBVZY7qY21bheuYPK1Ns9+awpnj9rNxaBp+cx8a7Bq+fGuvz" +
                "9Z56P49my5/OpXPSSq82mffG27C4GbyK4Ph+u93rtymLjrb7vj" +
                "mOEZ+vi3lIQR8LJZzHZ4SqgbrtgtzbpqrtQRURBsukijGebn+O" +
                "eb95aDffbzayRiat9GqTOSyMwQEfY2wMMvbPt/stkn3KZ1WErM" +
                "ga08XctgrXM39YGRSENcXrZ+XWUub+qPX5GHcw3v37xjmtL/L9" +
                "eXs1O2H9xAj2z+L2F58srb/k/fvGmRtnD54OHGwelNb1asG8eT" +
                "DdgoVnzod4vDUmv5psIYuO0i21Igu46anFwZCHdWl2RlndiEWr" +
                "qqyfKxtcv7e0ijCrz6SKMfb+Lg/O9Pp5R73Xz/XHd5pxtuvZ+L" +
                "Xe9Zwtf/P15uvSSq82mfsWYBnN8WpHHOL79fzkZ4HP6mIezIG1" +
                "Nuv3Y6HG8cdys3rUFSr1lVh868LI/jx3ol19Ub8/Z8T++LHis+" +
                "m8Mffnj7Pcn+nedK+00qvNHdlcNpfubT8Fi7PpCGiOl9ah8m8m" +
                "czKH39nEx1nU5+tiHswV233M2qzfj0WtqMDmbr3FVaGKGINlUr" +
                "x7D9uf2b5sX67bfL/I9pXZn4KKYSVnJZ8rP0y2P4v4W2+VRQ7d" +
                "/UcbR6WVXm0yh4UxOOBjjI1Bxj7jcxbJPuWzKkJWZI3pYm5bRc" +
                "/ynF8xZ4CCsKZ4/ay8cTRdTVfzfbqqfW/nrurctwDL6PVT7BM7" +
                "4hDfPy8e8rPAR+f7quXBHFhrs34/Fmocfyw3q9fKY0p9JRafrq" +
                "Qreb+ifc+zonPfAiyjOV7tiEN8n/dBPwt8tJ4rlgdzYK3N+v1Y" +
                "qHH8sdysHnWFSn0lFp99k30TXDd6NvdOj4lX54qFFXO8OSbMnh" +
                "4Di2JCFHSATQ9mZg7r4ayM15q4miIFzDLK5uuiK/PTA+4ZPCtM" +
                "a37+WcTffaqS7PvT/dJKrzaZL3xqLen+7vPwqo/jpe2+gLgk2X" +
                "hC/cqJjBuPWh/rYh7MOY9Fsd+PhRpXUyx3d5WrUg+vBvMxk8WP" +
                "9/uQzleR/TzW70O6L1X0PX7Ec8yi34cUPf+0vw/pfDTZ70OGn+" +
                "+9zG9UfL59WdFz4W+r5Q+/f07yai+3g+tJ+/6pnwoU7Nj2A8W+" +
                "sZWP+FeEztfjaZsEFV/P9vH2YrXr2b63eD3Tb3dmPdtLBfuzFH" +
                "/7vgl30tVyWNvU+/PqYvv02cupHKZh+uyFcQfksLapaz1QbJ8+" +
                "ezmVwzRMn31nr5+FbPn53n1lZ873ws+xq2b57XY218/mxUM+jy" +
                "6u9/pZjr8qleH3pfT7CTKsDPm+8n01KifNUxRXja70xvRGaaVX" +
                "m8x9C7CM5ni1Iw7x/fu6V/0s8FldzIM5sNZm/X4s1GxeH8/N6l" +
                "FXqNRX4uNH7c+JdviQ/dlYrvf+fef5x/3/COV/D9Zq5/U8XNFV" +
                "aXPC9Xx4lmu3eVrnV3f/3vmlNd/5T+7fO79XlDu8Z/kjr+euar" +
                "J3fhvh/6dgPUfyd/7u9//Wsz/He3XfqPd83/x9tvl3+v8fNc6o" +
                "9/lSEb99vtR+s7rnS+kj4aii7ziP1J+r6ppq/zzaU/Pn0Z5Zrl" +
                "26nC5LK73aZL5wubWky90PMFYfx/NI2865QNuIdLmzJ/RZRouP" +
                "6bTcvg5Vob2rKZa7e5Jzq8evyR5W8UDXO9K63rUyZ4tipNdxbG" +
                "4jgbEeG2/HosDH+TGsLWRErpBnWCVWZ0xpyOczzeL7/Obps78q" +
                "dT+cLG7hUHWoyN96LV2TVnq1ydy3AMtojlc74hAPTpuFfayLeT" +
                "DnPDFdlhGqtF+4NZ6b1QsqrtRXonoQGzyZWUt28WvhlupQJc/W" +
                "Pbt5PbsflUJ9POH5Pp/OSyu92mTuW4BlNMerHXGIB6fNwj7WxT" +
                "yYc56YLssIVdovZPHcrF5QcaW+EtWD2ODz6LXBU9Vnd9/+3Jma" +
                "mnc378ZY3nGMtoyOjf1I9TVPFuX3YzhjsR4otso4Xtue/yTrse" +
                "r9WvhteSy2zvv37if13r+X+31IzzLR/Xu4npvnzXI9m2/Xu55F" +
                "/NWsZ7qULh064Vrpk0Rnbu78h07AAqxDpr1/8YJNsqlFMwlGR5" +
                "JNLelS82f2KYIZhQdz0cU6rS5mBJfUYutjv2bQqlBFjMGukKpB" +
                "bPzzKLskuyQ/Pz+t9krtctb5mi1/43jjuLTSq03msDAGB3yMsT" +
                "HI2D/fTlgk+5TPqghZkTWmi7ltFa5n/rAyKAhritfPyvP3/2S9" +
                "oQo=");
            
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
            final int compressedBytes = 1267;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXM1LVFEUvxSIRBDtwyktaFEQiURtsjtvFll7pcWI/4CrCq" +
                "NNzatGx39BEIyM0AGxlbaUQNEoP4iittXGVbloEzTP5/Wc+/V8" +
                "8+a+e9+bO8y79557Pn7nN+d9OqM37A0T4g17Bz0hbMbGbAs68A" +
                "rX/F1mK1rCmI0IKc3ymniNxeNRyFHBqwoXjs1nEfQ4vpwZIJBz" +
                "UuePkTdkZa/c6Mus318pszlIsA68YA3r8DbgMYxb+8Br4jUWj0" +
                "chRwWvKlw4Np9F0FcLYsbYAyCQc1Lnj5F75eJGcYOQYMt6NgtG" +
                "wZhtsY6oAW9RzsZs1OBzi9fEaywe2IieQcKjxRo4Np9F0FcLvJ" +
                "WcPSCRkcr5Y+QN2XpxvdGvQ89mwSgY06Vgy+vwGvgtytmYjQih" +
                "S7wmXgut2Ag0xKjBO/CjxoVjAxYmwfGxb/AHSGSkcv4YOS9h7e" +
                "kDkmKj28Rpsxu/uBy+odW+86vxfETks2kvk+j4Qp7fzMStXK5c" +
                "qpxp9H37s/O4PmktlocejfyaJLlojq9K4Yj1qxo+NTlVLkiS3i" +
                "YR3VDL093fSzNu9/e040fVZ0wPTdVnad5tferiQ30+e9tKfdrm" +
                "k64YOausJN7fV+Ls79XuvPBZ2nRcn5vpHj9t81m855ZPXXyhPq" +
                "+kwaf3qRU+FZoZOL/rcpLr0xjS+4ef0k1C/NMkl+1Jl1run7SN" +
                "BPiM1/xdyUM94nrljePrpVTj027aHW7DnsnCufeRl4Au1vZ38V" +
                "ooBzuwh5i8F7yGceE4MMd+VLj4iICK9UFOKt8YPaCUkYpIRP2o" +
                "+vS2zNen6xYvp8TeR7yRcBv2THYw32YSrAMvWMM6vA14hJhcDG" +
                "kNkAEqPip4VeHCsQHLoWRbzBh7AARyTur8+ayi7zdrP0nbtfHX" +
                "7u43b10XtZ+/zPvzEDknk9fz3qg3Gm7Dfr9yR9kcJFgHXrCGdX" +
                "gb8Hi4Z3Ca8hobYVR8VPCqwoVj81kE/cSemDH2wHRrv+Sc1Pnz" +
                "Wdl/vkRPuN3f7cdvlk/5/B6ZT4djPjvcHT/l9uJLy8+Xjrs9fu" +
                "riC/fvg3l5nkwdn9/TjU8H6EC4DXsmC+eiBHSxNrZncrAD+4OI" +
                "naIXWONx4TgwB11exq+LtijXTrVvjB7ykpGKSET91u/fm/wEjz" +
                "muT+vxW+ezVEi2ZuV5SMGcli0+o1rtt1s+J97nrz7pw+zeb9rH" +
                "ZoDPxxnm0wq28R2TfPbPZpdPO9jGPxvl82uG+bSOrfXrefpIcy" +
                "Q5ZfCodNcstiwfP/vnM1yf8/njk45l+Hw0lsP6nMtwfc6545NO" +
                "N66//ySogelka800/0fC+px2yOdUQsxTydas7O9Tbvd3/P1kI/" +
                "m8c8ynJn5lJqd8Ljvmc9kdn9RPiNknmW32saX8fOmfWz6rhazz" +
                "2eTf41aNVNmqfcuM8rlmhM81+5bZ5LM44nZ/tx8/ZT4HHfM52G" +
                "Z8Djnmc8gtn3RRP9MeoxZ1unQxnoe4MczZyXmaQirwuaCfaTEv" +
                "6HTpQjwPcWOYs5PzTBbB9u9lJrsMfeoJv29T1TzfSe/3m0J91v" +
                "UzbQ3Udbq0Tg19FzypH50df78ZaJlCmu79kZl2a8++Zet8Tp4j" +
                "bdcmzzrks6f9+Jzosx0RzkelvxLXTfy3CMX5aMccSv/VEdE1vy" +
                "uUc9J66DVdn6U77Vef9nNCfN5uQz7Tzek/WTNs0g==");
            
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
            final int compressedBytes = 1459;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWzuPHEUQHkRg4YQEn4SEZCEOcMCBBUIIAmB6d5ADIHHGw7" +
                "rAkYEAxCNxYO8ePh+NLSdIIBFYSAjJJkPOsMCRZRFgCcecHCA5" +
                "uB9AcCfYubrar6q7d292rmd6b+QZbfd01VdVX9V07zxuL8v8bf" +
                "ApHxX/ZhE2+1Q2R1ucnGbZUE97JOvcZp9OV8/i7e7Vs/2cRD3f" +
                "6mA9W8/p7Gfj2Mc6WM9Gc1pdCciGe/I42OX769m09Vw93OjZyo" +
                "ucWupZVu69pd5SkdvnICllfAS0tKe2RGUZYeGxPCKf8Ch1Li8Z" +
                "B2PGDv/RMq13bZErMtC+wZ4ZhJm6TIDHsXMGV6nvLfYWXZ0vCW" +
                "2ECmFDPtvdqsevw7Q4XZymlnqW0diVACvR0p7lsIM9YmovUid5" +
                "yTgYSz8hXjoiWHGf/x72LdkTKszUZcJ8YOtd33/NOrzlv8RDBe" +
                "bnreIWtdSzjMaQSIyL1h9u4R22LJUyrdPMEIdH2k+IF+tdjizJ" +
                "r7kZSw/jel7Tebl18Fkjmne/tDX2etvVnft5z9fXtcTz83a6+/" +
                "naz3Qvz+96P99+PW+Mz+UfHfz+bDSn4kJxgVrqWUbj/G8tAVai" +
                "pT3LYQd7xNRepE7yknEwln5CvHREsOK+zCnkW7IHS5+py8TFT5" +
                "ufnby+PxgP5W9m2SxTSz3LaOxKgJVoac9y2MEeMbUXqZO8ZByM" +
                "pZ8QLx0RrLjPHwj7luwJFWbqMmE+sPXm56Um58frm2nnZ7Pxi8" +
                "1ik1rqWUZjSCQGO3QSo23gcefa/5pGSh3H0yz8qPAa4iVj6yzK" +
                "fu0DN2PpAQz8nML5S+Yj2VYxutssW+q3NVs8Lo/NPZIDg50Q8u" +
                "PKpceddXFPI6WOGWgWftTyU/oJ85KxwYUlMr70DX9g4jP185fM" +
                "tcR//9l/tcocH25UvhO7E29lDX/cJdaE+0z7Rrr3ySbyu9d5qK" +
                "c5lq6e0Z8U5qCe+YlK90snmqinfcdj+dcM1XtpHut57pHK/F+Y" +
                "l+f3ub6ffzQeatb5mR9ser3bd1tf7webPFv9u/271FLPMhrb91" +
                "kiMdihkxhtA4+IKZG+DszASkeF1xAvGRtcWGLfczOWHsDAzymc" +
                "v85ql/n5UAfXe8M5DZYGzwweG/Uvbo8WVT2XKnl4ovL16Mjoer" +
                "Acifcuf/cdPD+hnhNyGjwpRyvfz349GrwymqHr/XVqqd+eues8" +
                "hkRisEMnMdoGHscrQyF9HR9JVjoqvIZ4ydg6i7LPF9yMpQfGMs" +
                "pl6uevs5p+fc8f7+B6bz0nsd6PRv5ueTiirzdr1vNounpG/66e" +
                "g3p+db0Kyp7cL/XMrySen1e6NT/zq4nrebVb9bSn0taz2d/XTa" +
                "tn71Dv0Oj75nDcepY+U9ZzUvzBD/t0fn7U7fk57fmoooeZno/s" +
                "x2mfjybVUz8fbUtqPB/FmJ8z/L0j2v2SuVF3fppKvy6wn++X9W" +
                "6+TrveY8WPU8+9v58vIt3/1X3/WT1+rPfzza73tTtp52f/z/vP" +
                "mzHrWazGQ1VkuvN+qbfQW8iylU/i1rT0GWOzH8aN38b9pz3Tvf" +
                "l5/qf76z1mPe3ZbtXTHEh8v3SgW/XM/0tbz1jxJ5yt4+Y4tdSz" +
                "jMauBFiJlvbU5qdgB3vE1F6kTvKScTBmrP1Sy7TetQWbklvIN9" +
                "hLlj5Tl4mLD5z58e9pzeWaZ+lyPV0bW/vxzTfyyH6xFw8B3fXE" +
                "9Ww0vrloLlJLPcto7EqAlejhhtSRHHawR0ztReokLxkHY+knxE" +
                "tHBCvu7WrYt2QPlj5Tl4mLD9T4kjyqNT+n/ALfJP7v0Pbjm+/8" +
                "o7oeAs8nv6WtZ7X4NuJ/ReL3DGalZj1Xsrnd2udmvvWPZnu/NM" +
                "3O3IzC8Wb7lpXeXpm+oZZ6ltEYEonBTrrhBtu6ljjmo9F6O6mR" +
                "UsfxNAs/KryGeMnYOouyl/H9zMDAzymcv2Q++vwPG+0Nlg==");
            
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
            final int compressedBytes = 2282;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWk2LHFUUHXHAEXWhg+IiIUJiEjQThpEZQRTT9QGOIgElTh" +
                "hx4VICLgR/gD1O56PnVwTBZQxIVuYP+IFfm/ixcUJnlURU3Iga" +
                "69Xt2+fc915117TVk6SrqHrv3XveOee+qequ7ul8MV+cmckX87" +
                "J1m/R1jKzGuCejjesYYY5ygVP40zdyo8k5bFbR9+BjLIIV4Ujd" +
                "OH2bZwZ2YJV9RV4hW7W/ffA+j5KL1aOqTVAxbHKxHkNdjebmhX" +
                "WOo5AcT47LWVqN9cfzNgIso3m+xjEP86FpWTjHvlgHY+aJ+bKK" +
                "cDVwNx/nZvdwGTr1nfj4Yddn95OZqdvOvbPbiu3fJ8nevXh71/" +
                "P0vkmyJyeSE3KWVmNuTw+mB5MT5/Yh4mLaA5rny9mhilf+gzJG" +
                "3sUkxyya832xDsaK3ejZmM37c1ErKrDc7fNcFaqIKVglxaMfvz" +
                "7To+lRPxdGYpugYtgY5+5u9fXHc9peaB9p7yna5XJ0gF8/W9/6" +
                "6M6VCMP+CuZnpd18dxA57GM2T439qjTivm0vxeNhTX38k+ZVYa" +
                "2IPLNDR8+NXM+fpnA9f6qznp3L467nQOeGnqWHmO3ZfoXnG9X4" +
                "YaMqhjjCoqqcx+aNrqJKH7NH8bS29Sy9WDbsV/rZ3nnGouqpAG" +
                "Wdj9Yfzl+lj9lVPEPv9x+C+/P1nd/vFDnc4FPduPf7D3Xu983X" +
                "/vf9flPP0kPM9my/wvPNavywURVDHGFRVc5j80ZXUaWP2VU8Q6" +
                "/PL2v9XYLrs/vZHfx+9GWd67OMNP7+Hm717vfu5Tv3fj/zzWTv" +
                "92Hr2f2iieelO2s9u59P9vlz2Pchrbfu3O81Nl8dkX+l4n6fcE" +
                "1Dr88xXz/v/utzMq+frSNN3O+d+er17Dy86+9HRyb5+ej0PZ3f" +
                "3Hp2fm0vd/6V9ez80dA3Y0Gk82dz12dnxLeKnb8r1vPtkcx/Ca" +
                "rzT0N3Ut9JejgtrqcP3zPfudS6YwUVwwpnE1v30zG/X6rQb5+v" +
                "ixz63LrR2pCztBqTsR8BltE8X+OYh/nQtCycY1+sgzHzxHxZRb" +
                "jS9uxHcW52X/y1vvaVrZ6/Qlx11fVZ5J9o5M6cH5J7eGaXt2Zq" +
                "2tHr3o8zU7y1XmgOVW/buHeH+Ot303p2f5kke9bLenKWVmNuT/" +
                "eke7Ke+/+RxWAXNB9+nBn7r/J7LJJz6oBdWGbfZ8wXa8OLRlif" +
                "udvneUaspnj97LyILWfFc6c7S1tmlmVP96Z7s2X3/m4x2AXNhx" +
                "9nxv567rVIzqkDdmGZEelu+97DGahIGa0+cxfrSTNiNcXrZ+dF" +
                "bCVbKdoVbcvMio4RYQx2yW1c17n+TPS1Z9mYFzntsSurCtaYL9" +
                "a2Vbi22/MrZgY4CGuK12+r2un3S3f/5838bJ3PRw41ge+Xfp2+" +
                "9Tzz8WQ/v0ferx+b6vf3a5NkT15MXpSztBqTcdm/hAiwjOb5Gg" +
                "cTGPuKl3wW5Kwv1sGYeWK+rCJcDdxdquC+xFWhipiCVWJ8kid5" +
                "0ebalplcx63nbQRYRvN8jWMe5g/WymPhHDBWB2PmifmyinClra" +
                "spxs3u4TJ06jvx8f//ft/ZtnWb7/fJ/h4seyl7Sc7SakzGrp9c" +
                "kLjEkguaBYIPRclMYLSXXHBR1kBO2eAMrqyqO5Td8tgZqAi1sn" +
                "5YmWLdKObUd8IrWeZWs9WiXdW2zKzqGBHGYEeOMXYOGPvX55xF" +
                "ck71rItQFawxX6xtq3Dt6X1+xcwAB2FN8frZebaa38pvFU9bt7" +
                "Qtn71u6XjrQY0wBjtyjLFzwDh4tjPIMKc9dmVVwRrzxdrwopGz" +
                "y37FzAAHYU3x+r2qFvKFol2Qtp9Z0PHWQxJlDLKcw6FnZQIjoh" +
                "yzOfJndHTk+wx9ad73qJHuITvLMrC+VbbrELpW/LDn+fz4FH4+" +
                "Or7rz/NPD7Rfnr7vP3e/Jl3PdDFdnJk5Z66DdLHW/2gWq7DC2Y" +
                "jL3pj/P1qs/f+jMZxm92X3yVlajbk9PZQe0ohGXQQ7coxxKNcX" +
                "LBhLj31OMCCnbHAGV1YVrOw9nAEk8KzP3HaG+ved+k54JeUY9v" +
                "qZvNn862c9zsm9flbpN/P6mc1ms8E1W8ay2XQpXcpmy++TZ/VI" +
                "lwRhZyIvI0G52T6744SK8lgm6wOsuvefY+ctyjoI/cEB48Ecud" +
                "+X2Evk3p71RwXPXDZXtHPalpk5Hbe+0whjsCPHGDsHjANlgwxz" +
                "2mNXVhWsMV+sDS8akZpCbjsjVlO8fltVvp6vF+9569KW73/ruh" +
                "faPyOrMe7JSHty2B5a5cecUJvehddDHUR8jEWwIhypLtfE3HCr" +
                "7FbZV0TM4iOvA4N3zjP3T+D7kMdv7/PS2VOTZM9P5ifl7FqN6L" +
                "j1PbKybz2KrOa0J4ftuXbzXea3mpunbI4xvg4iPsYikGNHqss1" +
                "MbfmwW6VfUXEGJ+v5WtFuyZtGV/TvdD+ClmNcU9G2pPD9tAqP+" +
                "aE2lTNWqiDiI+xCFaEI9XlmpgbbpXdKvuKiDE+vZpeDd7Xypg7" +
                "Wlckq2PFIooxDo6H7JxTfBylcT3rHkdZh74P+uXHFVvFMAesMi" +
                "qm6kN//zk3fZ/fq2pq5vkzvZZek7O05Upf07HrJ1clDgx2QfDh" +
                "x5mx/zx91SI5pw6si1DVHY4n7ou14UUjrM/c4IOT0GlYPzsvYr" +
                "3U/Qalp22Z6enY9ZNtiQODXRB8+HFm7K/ntkVyTh1YF6GqOxxP" +
                "3Bdrw4tGWJ+5wQcnodOwfnae9vJn8+KudOe8f3dKX8atB5DVGP" +
                "dktHEdI2FAD20+uPutps0xxtdBxMdYBCvCkepyTcwNt8pulX1F" +
                "xBifr+QrRbsibRlf0b3QfhpZjXFPRtqTw/bQ5oNfp1hNm2OMr4" +
                "OIj7EIVoQj1eWamBtuld0q+4qIWbzbjh1wuzK5noxaT9nYsQNb" +
                "+wVp0fZse+73tCH/IFfxe1rFK5bnMAYoHWnEZhCTmnx+jtlV4G" +
                "pFQ/chzva7HSM5Cu19NsZnztiz7cX5R22KV2xsDljVF5z5GcSk" +
                "Jp+fY3YVuFrR0L1uNfR585Hp+36+1WoOFdn+A4FZljs=");
            
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
            final int compressedBytes = 2406;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWU2IZFcVrr0LCTLMZpheaNCAg5QZ7TEbu+pVrRKJMoEgCM" +
                "b4Q0NgFqKoixCtjtOdTGVphtnEMJtANglWFqJGxr+0tggiYyJZ" +
                "iC4SN0M32fQsAuK9derU951zz6t+3XndlYnvUfee3+/7zuuqmn" +
                "pvhq8P52enIy/447tthD1Ua0wiGhcLu1iIWryOO+pVWZxSl2WE" +
                "Rt23XvCKGUGVY4qIwTJx/fANnMmfvuD7CHvIaUwiGhcLu1jpb/" +
                "Qp34McXc9aVaiNdVlGaNR9c8UrZgRVjikiBn+FUL92Ye1Cp5PX" +
                "vOdDbPWR1Rhb4qklL2thV/zxvZaTczgso9fga2wFM0KRqtlcse" +
                "xWCSuwzJ6RrxBdqdW11bSvyj6Nr+opXv/niKBWLPHUkpe1sCu+" +
                "4CHGOZpmteRhXYpT6rKMUKRqcp/Ni626oMAyW1Ucs/XlUb3UqT" +
                "nKTFRb3z+9Dn/rnMhRp+J4+atXqldk1V29bGVbV65BRV43bolV" +
                "dsJG3FdyRvlsT8Qaa/cdrF8j4/O2q5yeVXqlXglfSVZvMi/XXv" +
                "2Xo8jGrab9J3nUqRh/5nh5R+dGnxydSfuUZ/SxTudH351zf7YR" +
                "wkdr4sW3yegTLepeOSD/6Ti+9UJN/d1F5N5DKrov/RUn1URW3d" +
                "XLVrZ15RpU6Mo1tkdsxMcXbCUy4LM9EatWRLqY206R96f+YLvK" +
                "6aGkVOqV0Odhwur5wPuz2eE/74uP8X3L/R4Yf+440QfnBufUsj" +
                "HxsSJmc/l62rxHct9rN5CTHq7ieo3rqueiCVhvjFrd8BNHqOV1" +
                "qr9yzD7oDrpp7+o+zXTVR4RrcCLHNbYHiLP3x+dtJeeUz6ooWY" +
                "Ea6WJuO0XeN1f8xIwABeVM8fysPMVODU4VV34ak/jgVH8itrz6" +
                "E4liVcv3pF96kxK9P+lPwCI9kQboAKqeimSrWIGNc1RU2aig9i" +
                "dWJxg8QhwT9mqn2kmfgR3s6mUr27pyja/Ay8fVViu9Pwe2knPK" +
                "hx6PjIhVyxXMbafI++aK7Sqnh5JSaTk/K0+x7Wo77dvY1cvW8P" +
                "t5zRFbgwrNcY3tSVfwAUQtGuMipxYqPKsyxLqYG1o0IjOV2LYj" +
                "mime3021W+2mfVf3aWZ3dnbTOYvMo915dpdzpiZVTe0uaubWDB" +
                "MIyCmaWqzKsgKVtZcdqKT6rp+YEaBA9HulXgldT8ntVXtp39N9" +
                "mtlTHxGuwYkc19geIM4+7w/aSs4pn1VRsgI10sXcdoq8p8+7m5" +
                "gRoKCcKZ6flVd7w22c6bMwfcHv3bQR9lCtMYloXCzsw/mnw/dw" +
                "jmtiVRan1GUZoVF3nomxoUvRLbPlYyauH76GM/nTF3wfYQ85jU" +
                "kkr+MvqYVdLEQtXnE9a1VZnFKXZYRG3bf+6hUzgs6gGTuvP7WO" +
                "64M7XHPf3f9Pu3cQbeEdFaeurx1da++svSOr7BoTP9v9txFBLV" +
                "dzv8aBBMSZ7rc9CnJWF/PAZ5xIl2WEKt25j7ElrsoxRcRgmbi+" +
                "d753Pn2nnNc9Z8TL9viijaCWq7lf4+hDv14rj8I51Fge+IwT6b" +
                "KMUKX7lZ0Ym9VDZanUK/H1C5/XPeTnvPyPO/153ZWdJs/rNh8+" +
                "2vO6xc+Xel/pvG+PH3/hgPz9cfy4Z1r0/uzd88F7nlw3UzvPkw" +
                "/4vH/5//X5/OVX2/u8bzxzrM+TH1ny8+SvnjTjYa/nIef52nKv" +
                "5+bKcaL3ur2urLJrLJ/V6ep0r/v0CiKo5Wru1ziQgDi7zz3tUZ" +
                "CzupgHPmptzOZ9L9Rk/gh7dJ2nwhQRg2Xi+up2dTtx3NZ9OvFt" +
                "9RHhGpyS27ilvb4Ttlrp/fl1W8k55bMqSlagRrqY206R9/Gjfm" +
                "JGgIJypnh+Vp5i+9V+2vd1n2b21UeEa3BKLl3Pfa6xPUCcXc9v" +
                "2krOKZ9VUbICNdLF3HaKvI+/4SdmBCgoZ4rnZ+XV/nB9uJ7u49" +
                "d1n97Zr6vvI6jlau7XOPrQP7ue3/IoyNHzkHXLAx+1Nmbzvhdq" +
                "NldibFaPuUqlXomvD/7df2v+2+KLR/xWfuz9ex9wvNoGZwZnZJ" +
                "VdY+IjwjU4keOawZn+JfhABCdzlDkogyrLCtRIF3PbKfLev+Qn" +
                "ZgQo8MyWz6rm+vL3/Mbz89823/ng/Z6/8vHl3R81xDnU9Rx/b7" +
                "nXs+73Z1vXs/55SHVPle51nzb8VaM7eqmKagWzlbuOt47WV8c/" +
                "ut60sskxvDi8CFte6bv772WNrlwd2fSk51LuifBT7rF6RYwYIQ" +
                "NTdTEy9+ua9zwT9Fj1fhZ+WR5b2/j50pvF04RH7/jnS282+bxv" +
                "PfJeP++9q7qK5VRcje3aXyVXD5+xVc1YUGWVH8y/GL+OH90H4f" +
                "Se1VWsKFvatXqePXzGVjVjQZVVfjD/Yvw6fnQ3w5n+knqutFr6" +
                "tfvc8rHanqkB409n+4tqtY28TKy2Z2rA+HxptY28PKy2ZzrgV9" +
                "0N8+978e/x6MFDI/7i8kdm1m9GH55Zv5//v8Jds8ivpuuvg/7t" +
                "9PpL8K/pA7WMv8vrD8/OvF/O9t9O1z/5mTZeDRB2Gk/3Wnr9ce" +
                "PPi2pwPQc32/1rjZ5s8S//xhHfnzeP9bN9bXBNVtk1ls/qbHV2" +
                "cC3fH9kanFLNLx9nxNldx1lbyTlVwCosstcZ6WJuaNEI8zP26D" +
                "p3RDPF87NyGynfnw1+sTyxpKduT5x8Z+N7zZ/Imve8is8RrZFd" +
                "7ci3nRFXFPU6WEVcx9oiRsWyPlvRJFanj9hu7fdM7/H9+fiS3p" +
                "+Pn3xn06P/rqx5z6v4Gnny2xJHrdqRz51lXDMH6WAVcV3/3a0f" +
                "MJpnVCzrs+UV2udLpQKLCD7PlL6hPyRr3vMqvkYkjxjsyOfOMg" +
                "7M8EnaXAeriOtYc8QI5eyzVacQeDZrEcHnmTqdtX/Jmve8iq8R" +
                "ySMGO/K5s4wDMzqgg1XEdaw5YoRy9tmqUwg8m7WI4PNMyf6ZrH" +
                "nPq/gakTxisCOfO8s4MMMZ5jpYRVzHmiNGKGefrTqFwLNZiwg+" +
                "z5Tsf8qa97yKrxHJIwY78rmzjAMznGGug1XEdaw5YoRy9tmqUw" +
                "g8m7WI4PNMyf63rHnPq/gakTxisCOfO8s4MMMZ5jpYRVzHmiNG" +
                "KGefrTqFwLNZiwg+z7Tw+fzrje4i76zn8zUztff/cf3/ypr3vI" +
                "qvEckjBjvyubOMAzP8HTTXwSriOtYcMUI5+2zVKQSezVpE8Dmm" +
                "/wGBhlOS");
            
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
            final int compressedBytes = 1887;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWk2LHFUU7YUbV+rCbBRdqCiDbpKIIi6c/iALhSgSJDN+gC" +
                "BkF5CIrsKku0g6IT8gAVduRBfuRNAofqFREdxoIigKQ0DFhWSh" +
                "zM56ffvWOfe9+6prOt3pydBV5L779c45t1JT3VNJq6VH56Ba8X" +
                "K51OfY62la4Z5w1ndoH3akyuv4J+HHdVyLmDmDcEiteMhZL/U5" +
                "9nqaVnAUt3UO1fepWqvQ1+vzT8KP68Wtdnce54nfxIY1WIk1I3" +
                "Xk4Hsx70zzwPQO6GAVfh9r9hihnGP2cgqBZ6sWEXwxU/+h/oP9" +
                "O8v14VF0b6t14jWtrd7fanD078nkH0kyD7Rmdgz+maBqr5/Pzd" +
                "S/L8ns256i/mNjhjfEhjVYiTUjdeTgezHvTPPAdCetdLAKv481" +
                "e4xQzjF7OYXAs1WLCL6YqXwCtMWGNViJNSN1zRW3w49r9KzZ4+" +
                "fBlq+IAqjw+1gzq4+xUlbsqNcSK7CI4IuZSv+A2LAGK7FmpK45" +
                "9MPnmPOD/1ylB7IzVDpYhd/Hmll9jJWyYke9ls6Bwb+eupgvZq" +
                "r6n1QrXnk9fkhz6ITPsdfTtMI94azv0D7sYOzJ/JPw4zquRcyc" +
                "fJb9KTaswUqsGakjB9+LeWeaB6b7mVrpYBV+H2v2GKGcY/ZyCo" +
                "FnqxYRfDFT+Sx9XWxYg5VYM1JHDr4X8840D0z3M6DSwSr8Ptbs" +
                "MUI5x+zlFALPVi0i+GKm6n5+Rq14yA3+4lzqc+z1NK1wTzjrO7" +
                "QPO1LldfyT8OM6rk/MnOx9VGxYg5VYM1JHDr4X8840D7Z8RRRA" +
                "hd/Hmj1GKI9Zc5OkE9iqRQRfzFT+7P8qNqzBSqwZqSMH34t5Z5" +
                "oHpvvMqnSwCr+PNXuMUM4xezmFwLNViwi+mKn0/xAb1mAl1ozU" +
                "kYPvxbwzzQPTnaHSwSr8PtbsMUI5x+zlFALPVi0i+GKm6n5+Vq" +
                "14yFkv9Tn2eppWuCec9R3aN/4N4uac8hz/JPy4jutj6ylO53Gx" +
                "YQ1WYs1IHTn4Xsw70zzY8hVRABV+H2v2GKE8Zs1Nkk5gqxYRfD" +
                "FTeuB9SMM3E39vp/vssLXQ4+ypeTO0r4oNa7ASa6Z4VfLoVd+L" +
                "eWea18okHazC72tfHW4wWsyoWDZmL1bYfyvWaRVYRPDZ/HDgvH" +
                "c6Ntf788yC78/T88Wve//Z/SnuHp7eKe8/+3dP9/4zncl7/1m8" +
                "Of37z9zzs7OvUyKeMbo7jTiky+sVzJm8T74y3b4cv/15bz7rtu" +
                "7Pyzv3/fzU9+flhd2fK52V5O9spdE9sOL39gsP8/oezfmvTenq" +
                "72rF86qpX4+2vYrtasaCLqt8Mn89fo4fu32c3rneObGyak5iZL" +
                "gHJ2rc0zvXPooYiOBkjrQGZVBlWYHq6WJuO0VY20fjiRkBCmJm" +
                "y2dVc3/d9/neU61dd8x3pt753nmxsmounJ39nf2a0WzI4ESNe0" +
                "JX8KUXiKNn0hgTCKgpGpRBlWUFKmtPd6AT/czP2HaH6o+Vxkr4" +
                "SrJ6//7s/rj77s95z1T3fanBJ8LGYr4vrW6cfG+670tB8eTvS6" +
                "PMjL4vnfygtYuP4TuNut6d8u4vD7Gyak5iZLgHR/m3fVz3cY33" +
                "ABGc3JnWoAyqLGu3u3o81Z7uYP2aUcUxtt3hzeTNz1dyVNvsbp" +
                "brpq6jymYVX9IM9+BEjXvsHiBWzKYzranHqiwrUD1dzA0tVeZS" +
                "PDEjQEE6kz+/naq3t1c+Z4LtjZ834kvc/RlVzbEnkXryx3pYe9" +
                "XzzHLaGvfEPMjEPbaDGaFIeXkmxoZaRbfMMSNytr/2++fTu/D7" +
                "51xn6h7uHhYrq+YkRoZ7cKLGPXYPEMHJnWkNyqDKsgLV08Xcdg" +
                "pvGosABelM/vzRVOvd9XJd13VUWdcYGe7BiRr32D1ArJhNZ1pT" +
                "j1VZVqB6upjbTuFNYxGgIJ3Jnz+aaq27Vq5ruo4qaxojwz04Ue" +
                "MeuweIFbPpTGvqsSrLClRPF3PbKbxpLAIUpDP589upyufJVvKE" +
                "2Qo5ycMih+zwfcTAYcQUnVmYw++I+23O61Jv4648t+31UBs9ib" +
                "fiqLdVvFIcjvuKl+b1tC5eniHW2k78tAvXs3iheG6217NYX/z1" +
                "LF68JvTnd9L17N2Rv5652vW6ns34p1XZ29PbI1ZWzUkcZ9BrO+" +
                "Ia42I/OONOW0sZY9ZYle2w6FZFHhuaWWWq1Juf++veLw0v7tx/" +
                "j5v2/dLwm+v5fmnwaf37uv7B7c49+LDyPuvfMva+TLoujOwnzv" +
                "6vyz/fO0qy79kHX8jn+zj6aLx+PrLJ1Rx87CB823i6r8o/Fwff" +
                "1V7nm1rLY5ZvmJfXc2Hv5xf3/Jz2/zNk++f2fn6713N52KP9tt" +
                "iwBisxZ7RHVvW92O5kfK7E3LEOVuHrZW0pI7BSnrpJrE5PacoX" +
                "My1/3r2f91n+/6XhLzfEc//UDtW1vD/nfH+2j6TejJ7URxaPNe" +
                "uZGrxpOLH8jJ7pE+DY8hrM8sB75+GVG3cK/f19+Xm0234/6lxI" +
                "vRvvWJD2/wExA8It");
            
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
            final int compressedBytes = 1000;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWzlsE0EUnZKCho4CCPeVWFxBkUhlr3tKKiSaFFBRhIYu61" +
                "0nllIh0VGCFGoaGhqk9EhQUNDTpEKhIxLenXz/Y2bX3vX62PhP" +
                "5L8z/3z/Z3YOKzEmbIQr4Tljwoem38Krxmy9MoVaeCWDv+Zwbp" +
                "rKWrg0RH6/oL9rDudBQQ+PUjrlerb2q6hma79sPbPi83ru/K1L" +
                "PesxP6N3ZevZ/439sjR5JtSOKQd07BP6vjG3pP6phNvzvkUg9a" +
                "QNxeZGRF9unLxMOE4fUjeejKTzs97rp9ZT10+t5zj1jD6Vn5/B" +
                "RrBhKTxhlPSSPlCqgxpAqQ63sX3kS00qgXjcxhcVNHy4aGyehS" +
                "8bN3uKUiKVSLCBftiQdS46PzN/X2tmgi16P0T+cZz3feeo+Pyc" +
                "9P7u0azVflQSl9ZT67mQ9fTv78HT2dYzKz6vZ++Unpfm4zzvtq" +
                "r298VsOj/1fqT398WZn7p+1nv97D47yfMz2A12LYUnjJJe0gdK" +
                "daQGfiQf+tDj3qhflEEPNWRU9OrDRWPzLHzZuNlTlBKpm7/I6j" +
                "A4TEfHT+gjHyjwUEplqAN86hX9UZ/Un+fczeLASOJ0cYFcYqRo" +
                "qBX3QOPLvLKRENlBcJCOjp/QRz5Q4KGUyvADlHpFf9wb9evUk8" +
                "WBkcTp4gK5xEjRUCvugcanH2nporay9np73ZiE2mcisSPoA0Ud" +
                "/EEZ1eE26BEic01XBj2KikdFrz5cNDbPwpcN94AI3Jz8+fOsvO" +
                "vq5uAWe9aYzpmTtf92Tk874tZnPfXofVPvR1pPrWep++aRroK9" +
                "ZoX7kd7fS7f4bv9D3ox4Kb4h6xnfInP6e46vS2y0Yt/3+PyAcz" +
                "n8Ed/JtL6e0nspvTjCuecDsyZva5yuKuE34T9dleJlJ+6FzPd3" +
                "xPc9vh03cteBTZ1nlZ6gBvVsvSnnoazdCV2LH2sN9Dw/v+elzr" +
                "LOsvFb8zdQ2/NJ3X6+t2ISrjVaFNTiyIfHz/efFR+t/X6aUTOy" +
                "1D6BZ8eSg7pUm9oDH+3QHmNyL1RGcdE4OKZ+fLh4REQl0UjfFD" +
                "2idJFKJFK/2PrZ/bkY6+f2k+nc3xelnt0v1e1H4T/dTSo9fz7X" +
                "GlRazxdagwrOS2+B2p5P6vbzvRWTcK3RoqAWRz48fr7/rPhone" +
                "VH70e+/ajs34f0Xur3S5Nu+n2y1nOevl+aVD3r/P9c0V7Z/+fy" +
                "nJde6yyr9OT0dfYeZp1d2Qzaq+1VS+0TeHaMHKqDPyijOtwGPW" +
                "JMqunKEBmi4lHRqw8Xjc2z8GXDPSACNyd//jwr70ryR99R3d/n" +
                "Z3/X+5G7v6cc/fulEerZ255oPf8DyeK97Q==");
            
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
            final int compressedBytes = 1971;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXM9rHWUUfVC6iBQElerCKlRTXOimIiaukrz3/gYJzT/QLC" +
                "V232dbySobkWDqQkFxU6GLLt1nLYgle+k2y2wEZ+Z655xz703b" +
                "PJ81yMww3/3ur3POHed1XtPgaJSP25+PhmPOY/3W+i1bzXrM/B" +
                "hBLVdzv8fRh35wKgrnWBfzwGecSpcyQlVUE7FZPVRmpVFJrJ99" +
                "MHt/9mZjP2qjs3fP/nzO3jkl/nGKvLe4J2H29jPy18+It8ze3Z" +
                "+byIdnRPikjk9eyrvheO67+oKfz8nL/+3zeRq/Pp9dZI7nc3Jx" +
                "cjExdjGLY0XM9+jUPMczOuccR5Fit69+VmpZRa2PO6J9ms4K4T" +
                "TUBmdpstTYJbddZsl9RLgGJ3Jcoz1A7JmlMud8x6qUFaiVLubW" +
                "KappFAEK8kz1/DrV9Mb0xmjUrq1tD9u7j6zHeGee7+zSHazjoy" +
                "dz41DGqCHWaAUzQhGr0TwjsAJljoyMRqhb063Gbpnt4lt+mscR" +
                "9pDznV26g3V89GRummYr87AurdEKZoQiVqN5RmAFyhwZGY1QN6" +
                "ebjd0028U3/TSPI+wh5zu7dAfr+OjJ3DTNZuZhXVqjFcwIRaxG" +
                "84zACpQ5MjIa6sdPxk9Go3Y122bM872vqMGJHNdoDxBdqVbmnO" +
                "9YlbICtdLF3DpFNY0iQEGeqZ5fp1q7unZ1NGrX1tphnvlYPYYs" +
                "53D56khARJRjmsOhPO5FnVmX56NGVsNdigD1OlfszKotN12Zrj" +
                "TP6YrZ7rld8dM8jrCHnO/s0h2s46Mnc9OnbSXzsC6t0QpmhCJW" +
                "o3lGYAXKHBkZTadu7utye/Z3etkuufvLVoWVM7rqrsZ/1uH1Xl" +
                "v1ANV1QVnMcKzCj3ncBZ7WOPx8irJr7QnPLq2wKqyc0VV3Nf4z" +
                "7+c1Rax6gOq6oCxmOFbhxzzuAk9rHH5WyqaPcbYeRyyfa2K1xy" +
                "zicUcHC3NyD+eyrkpDrV11R41sc55ncXRlVj69Q6hfW11bbe7x" +
                "qtnufq/6aR5H2EPOd3bpDtbx0ZO56elYzTysS2u0ghmhiNVonh" +
                "FYgTJHRkbTqdPf6//w3e5Xw8+LznaMH40f2erWvXbX7n3lGlT4" +
                "yjXa4yy+j5WccT7tqVhr7bGD9bMa7srTs8qoNCrhO8nqJfPw1L" +
                "v/8HlqT+9/oU/Kw/PyzO5+0+8Ohk/wAu7n/X737XA3zvipOBwf" +
                "2urWvXbX7n3lmliBK8Z97ztFY1zkoMwrIitQK13MrVNU0+TpWW" +
                "VUmucPUx2Pjxt77LbLHLuPCNfgRI5rtAeIPbNU5pzvWJWyArXS" +
                "xdw6RTWNIkBBnqmeX6eaHuJsvo92F/wYYQ85j1nE47aDnfb/NW" +
                "MP57imVqU4WZcyQiPbnOdZHF2ZlU/vEOpnrxffP1+d69/H3vjb" +
                "vvKC/z3xtXP/PvpueK8s8rh7MtyDhT6fPw73YKH386fhHsx7rF" +
                "9fv26rWY+ZHyOo5Wru9zj60A9OReEc62Ie+IxT6VJGqIpqIjar" +
                "h8qsNCqJ9Wf7fZt7j4s37P/w98G+/HTe3wcbfr+uup/3fpnvfo" +
                "5Pxie2mu2+6Z+4jwjX4ESOa7QHiP3fJKQy53zHqpQVqJUu5tYp" +
                "qmkUAQryTPX8OlX5mX5reK/Me0xvTm/aatZj5scIarma+z2OPv" +
                "SDU1E4x7qYBz7jVLqUEaqimojN6qEyK41KtH66Pd1u7LbbLrPt" +
                "foyglqu53+PoQ39/HwIK51CjPPAZp9KljFAV1URsVg+VWWlUov" +
                "WTK5Mro1G7mm0z5vneV9TgRI5rtAeIfh+0Mud8x6qUFaiVLubW" +
                "KappFAEK8kz1/GGqo8lRY4/cdpkj9xHhGpzIcY32ALFnlsqc8x" +
                "2rUlagVrqYW6eoplEEKMgz1fOHqXYmO43dcdtldtxHhGtwIsc1" +
                "2gPEnlkqc853rEpZgVrpYm6doppGEaAgz1TPH6bam+w1ds9tl9" +
                "lzHxGuwYkc12gPEHtmqcw537EqZQVqpYu5dYpqGkWAgjxTPb9O" +
                "Vb3zb/85fO+Z+/vSg+kDW1uLmPtYPYYs53D5CvSMzzHNqTLwuB" +
                "d1Zl2ejxpZDXcpAvPzpWtUgvqNyxuXR6N2NdtmzPM9R1DL1dzv" +
                "cfSh35kjCudQozzwGafSpYxQFdVEbFYPlVlpVKL1k4PJQfO5P3" +
                "Db/Ulw4D4iXIMTOa7RHiD2f9JIZc75jlUpK1ArXcytU1TTKAIU" +
                "5Jnq+cNU+5P9xu677TL77iPCNTiR4xrtAWLPLJU55ztWpaxArX" +
                "Qxt05RTaMIUJBnqufXqcqfJx/+a/92dmdxWF/8fj7fSMPP66qf" +
                "1925P/z883zcz+L75/D/E/hn30G/trW17Wo+R7zGrO8rXzsrri" +
                "oadbCKuo61VYyOpT7vqklUZ4xot/dHpuHzXn3eu8icn/fxJVtb" +
                "267me+TOZxZHre8rnztz3DP1AR2soq4bX9r9ldEio2Opz7uocP" +
                "Z91KkKFBF8kWl4Phf9Ptr4wdbWtqv5HrE8YthXPnfmODC5X/em" +
                "INbFHtZcMUJ55HnaJKqzUpr5IlPxff634R29yO/zZ/370fA+Cu" +
                "+jC7a2tl3N90jzPrrgNWZ9X/ncmeOeOeU90+tgFXXd+MLuY0aL" +
                "jI6lPu+iwvA+SgoUEXyB6S9PEfF9");
            
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
            final int compressedBytes = 1684;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXL9vHFUQXgcKClCQEEQxvwpAIihxAYmwEELK3h0gUVEhEA" +
                "5RIBJpTwHqPS6b+KrIRYqICikUCLuhoOI/yF+AkKiQAg0BRBOR" +
                "iN19nvu+mTfn8573fCjed7r33sx8M/PN89u73ec4STI4MTg+eC" +
                "pJBqeSog2eT5Lss6RWGzw3Qf9qpHkxSdZ/Shppg2en2F+uGe8F" +
                "loZfFZpXakZ4LYzp7dCXY9kHWTTrPwc9sDL3ZPaM9WLxG3gwCx" +
                "+X3v7yV45mM0osLfPMMhx8bXlqBjoi8tlMcau7P2v8BIfJfd7S" +
                "m+nN0IdRdEGGhjF4wcYY7YOIyMnI2AZmYKWzIqrHi3PrKrxqdA" +
                "QwiGvy69dV7e/+PEjt9C/Sh5lnjec7R6tn0ajdZQFKM5+ef+f4" +
                "k/LD24/Tu967Hvowii7I0DAGL9gYo30QETkZGdvADKx0VkT1eH" +
                "FuXYVXjY4ABnFNfv26qvZ6b7bl39S7/8xv7PX+s7F7hbnef1aa" +
                "Ge4/13+vt57rv7XrOe1+Prre7+6wP79rr+mdWneruxX6MIouyN" +
                "AwBi/YGKN9EBE5GRnbwAysdFZE9Xhxbl2FV42OAAZxTX79uqpk" +
                "35/fG/vk3/p/Xu/tes778zP/of0cbPQeql3PmVvvUu9S6MMoui" +
                "BbDbCMZn/Rww/+yKmjsI15cR7IHMfjpTOClWVjYzN7sIyZWiYa" +
                "n66kK0lS9mGsTkpWRLYaYBnN/qKHH/zHJzEmCtuA0XkgcxyPl8" +
                "4IVpaNjc3swTJmaplofLff7Rff830Zq2/+vsjQMAYv2BijfRBx" +
                "fGehkLFNZsxKZ0VUjxfn1lV41egIYBDX5Nevq+pt9DaKfbohY7" +
                "VzN0S2GmAZzf6ihx/8x9epicI2YHQeyBzH46UzgpVlY2Mze7CM" +
                "mVomGp8up8vFPl2Wsdq5yyJbDbCMZn/Rww/+4+vURGEbMDoPZI" +
                "7j8dIZwcqysbGZPVjGTC0Tje/c6txKkrIPY2kJksylBwYv2Bij" +
                "fRBR1kEjY5vMmJXOiqgeL86tq/Cq0RHAIK7Jr19XVezRO9G1dq" +
                "fUBT166LSN7WyN53EWG8fjofFa56Gm5e6+a+1e1F3dG92xUvnO" +
                "HojPP7PDRf9YNX+86p8o3k8myWgbm1XPEqOlscdLxfu4SKNDFG" +
                "t14m/93szezt6qZu9MQKxlZ0j6JPs0u5AtZYcM6pHsUZKO0nzK" +
                "CdTowW3c69kbWVp/PbMPlfRx8T5f93lz9NBenzdP31vsed2k/A" +
                "f9+X2+55+jh/dnPUeH2/Pkdn/WX8/8x+bOl9IT8axem9VvP9p8" +
                "uaXH0mOhD6Pogmw1wDKa/UUPP/gjp47CNubFeSBzHI+XzghWlo" +
                "2NzezBMmZqmWj88Pzw/eHa8D2uZfjRnk+tnvH1w3OTbXXb8IMp" +
                "9jP1uM2CcvyO9I6EPoyiC7LVAKsR1sZx4Y+cFqltcUab1bLSCB" +
                "1ds5gcG5yZZczUqx/4dDUt7rjLPozVzl0V2WqAZTT7ix5+8B9f" +
                "pyYK24DReSBzHI+XzghWlo2NzezBMmZqmWh8d7O7WTyDbcpYPZ" +
                "FtigwNY/CCjTHaBxHHT3wKGdtkxqx0VkT1eHFuXYVXjY4ABnFN" +
                "fv26qvb3cYu9/7xy7mDcf145O9t6Xl7K/yzXM/9jcCq/F9Yz/7" +
                "uZii/He+qfBvfnX1Ps/84cefvcKL/b0D3phXjWdOTFxWq6JrOH" +
                "7tv9OTq6iP3Zruf8r/f8i3ldDaOn9+85Pf98YScE34a+HMs+yK" +
                "wRTBhl7snak+Ozxea2PJiFz5e5xRkRK86zUyWap8c0zmczOd/7" +
                "F5O2zdg61zrXQi+jSOWsnEvPGCCkZ4z2kSwyt0i2SD7t42X1uV" +
                "sP5s9s2CuunllappYJrySz59b+PULDe/bG7i0edpL/5LjNMZxH" +
                "tlrfRSfTk6EPo+iCbDXAMpr9RQ8/+COnjsI25sV5IHMcj5fOCF" +
                "aWjY3N7MEyZmqZWHz7+6Mmz0PStXQt9GGsVnpNZKsBltHsL3r4" +
                "wX+8r0wUtgGj80DmOB4vnRGsLBsbm9mDZczUMtH4Tr/TLz5r+h" +
                "hF6lT/Gg89Y4CQnjHaJ8yht0i2SD7t42UVhMeLc+sqvGri6pml" +
                "ZWqZ0Of1Nr693hd5/nlQ1nP2/++iXU+7nsPvZ9+fnaudq6GXUa" +
                "RyVs6lZ4xF4G31MpeZjsZxYZMZEDYronq8OLeuwqsmrp5ZWqZx" +
                "/bqqdn+2n5/tevJ6ds4udj0n5Z/j32u35yGzt/8ACif3OA==");
            
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
            final int compressedBytes = 1738;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWk1rJFUUreDGHyDoQnARBRe6UQaZwARSU72YhbMI2QiDMf" +
                "6GkayysGboQBbZzNbf4B8Y/AP5BSI0hMBkMUwQR9yIBiv1cuuc" +
                "c9+tdLqnM8SkXpH33r333HPPfd3pj0qKov68/qz+sCjqO0Uz6o" +
                "+L4scfiplGvdzj/yrzfFosbNQfTYl/MSPfJ5nnyxkZVtp5OM/h" +
                "PK/xeeZj1vMchpzq8Pwcnp//qzGc5/yjulvdTXNazZdseBiDCz" +
                "HGaA4YUZOReQzKoEqrgjXSxbW1i6gbZYCCvKe4f9fVStX81p/N" +
                "aW0jK2bDwxhciDFGc8DYVRZkHrMdq9KqYI10cW3tIupGGaAg7y" +
                "nuX7sa3o+u/vPn3tfDec57nvcn9yfen3zJjxk+jfm4MfI+Gmcx" +
                "w8QoZjIca1OUKtTayq9dXKQgz+n3eV3n3ufpp4f1+WUep8uhrn" +
                "pMV3EVOsvlcjnNaTVfsr0HWEZzvvmRh3zUVBaOsS6uA5t5Il1a" +
                "Eaq8Gs/N6qEyV+qVePzw+XOhz88H5YM0p9V8yfYeYBnN+eZHHv" +
                "JRU1k4xrq4DmzmiXRpRajyajw3q4fKXKlX4vGzfV7ae3d4f7+Z" +
                "9+vGP1/H86xOqpM0p7X9pH9iNjyMwYUYYzQHjN03CUHmMduxKq" +
                "0K1kgX19Yuom6UAQrynuL+tavh/WixY++b3Lf75E0Yd+vbfJ6j" +
                "e6N7aU6r+ZLtPcAymvPNjzzko6aycIx1cR3YzBPp0opQ5dV4bl" +
                "YPlblSr8Tjg+fso+H3du5PHe8H5/ldt/t+BqYPznO+fcsdvHfd" +
                "z3h4/XyD189yVKY5reZLtvcAy2jONz/ykI+aysIx1sV1YDNPpE" +
                "srQpVX47lZPVTmSr0SxY92RjvNumNrG9kx23uAZTTnmx95yO/O" +
                "wbFwDBitA5t5Il1aEaq8Gs/N6qEyV+qVKH50MDpo1gNb28iB2f" +
                "AwxqP1x2ZjAiO87NMYnajUMUt5Il0W9xpZjXbMDFyff3xmrvo8" +
                "djo6bdZTW9vIqdnwMAYXYozRHDB2lQWZx2zHqrQqWCNdXFu7iL" +
                "pRBijIe4r7d13tj/abdd/WNrJvtvcAy2jONz/ykN9VdiwcA0br" +
                "wGaeSJdWhCqvxnOzeqjMlXolii83y82iOJvT2t552jTbe4BlNO" +
                "ebH3nI7+6zORaOAaN1YDNPpEsrQpVX47lZPVTmSr0SxVdH1VHz" +
                "Pf7I1vab/ZHZ8DAGF2KM0RwwdncOBJnHbMeqtCpYI11cW7uIul" +
                "EGKMh7ivvXroa/F8f3657+NOf9usPqMM1pbU/60Gx4GIMLMcZo" +
                "Dhi7R1KQecx2rEqrgjXSxbW1i6gbZYCCvKe4f9fVUrXkzzn5kh" +
                "8zfLZHpsbZn7NzzHiUyWfbbFekllXE+jjDrxfpjBj6WBue7Wq7" +
                "WbdtbSPbZsPDGFyIMUZzwNhVFmQesx2r0qpgjXRxbe0i6kYZoC" +
                "DvKe5fuyrXy/XmfWnd1vadat1s7wGW0ZxvfuQhv3tfdiwcA0br" +
                "wGaeSJdWhCqvxnOzeqjMlXolHh+8rj7uFD+b86+mz4pbOsqH5c" +
                "M0p9V8yfYeYBnN+eZHHvJRU1k4xrq4DmzmiXRpRajyajw3q4fK" +
                "XKlXovhyq9xq1i1b28iW2d4DLKM53/zIQ353Do6FY8BoHdjME+" +
                "nSilDl1XhuVg+VuVKvRPHlRrnRrBu2tpENs70HWEZzvvmRh/zu" +
                "HBwLx4DROrCZJ9KlFaHKq/HcrB4qc6VeicfbWHtlc9rBpzvdx2" +
                "PtVT/+IquPIUYoqk95lDe9i776yJ7Gs/bC5rSLovm+V8+L2SOK" +
                "ulwVoFT59PoX8/fVR3bMs/d4+L65yP8PoZM/tjnt3ONyHO97H9" +
                "/j2SOKulwVoFT59PoX8/fVR3Yfz2zPz/Gvt+P5Of5lvufn7tL4" +
                "j7PzHP9e3xmfpvMc/7mgv5Pmj8ZfizvP8esp8X/mZv77fP139t" +
                "y1eq1Oc1rNl2zvAZbRnG9+5CEfNZWFY6yL68BmnkiXVoQqr8Zz" +
                "s3qozJV6JR6fjyfvFMNY5P8z/DacwXCe13dc3f9/1k9v/P2ll+" +
                "XLNKfVfMmGhzG4EGOM5oARNRmZx6AMqrQqWCNdXFu7iLpRBijI" +
                "e4r7d11NykmzTmxtIxOz4WEMLsQYozlg7CoLMo/ZjlVpVbBGur" +
                "i2dhF1owxQkPcU969dvd3f95s/quPqOM1pNV+y4WEMLsQYozlg" +
                "RE1G5jEogyqtCtZIF9fWLqJulAEK8p7i/rWr4e/vi70fUq6Wq2" +
                "lOa/tKsGq29wDLaM43P/KQ373SOBaOAaN1YDNPpEsrQpVX47lZ" +
                "PVTmSr0Sh/8PjqeZMA==");
            
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
            final int compressedBytes = 1445;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW8tuHFUQ7Si/gcQCEFYEG6MIiZXl6fmMSMisYi+8gQ9gEq" +
                "Ewkj8AKXskMFhiw4odkdgiFgiv4w+IEPtMT03NOadutT3vOObe" +
                "1tSt1z116np6+hGlPWqPmqY9amdz07jlukvk4ECMc3QNEJvZ0M" +
                "wy5hqz0qpAzXhxbe0i60YRwKDsKe9fu2qa0cejj0bvTOaHnXf0" +
                "ftN8/VWz1Bi91+P/tPDsNRsbo3dviO8vifdB4flkSYTPJjt63B" +
                "6btHm608duw8M5OBDjHF0DxPlfUjLLmGvMSqsCNePFtbWLrBtF" +
                "AIOyp7z/0NVpezqZT32eRk7dhodzcCDGOboGiPPKklnGXGNWWh" +
                "WoGS+urV1k3SgCGJQ95f1rV8NHw0dN08lu7obpbiPqPtbMcs0+" +
                "qmF2fKwpa2Noxcgh5mgGVwQjZqNxRmAGWjlWZDTtuvgduHJt/F" +
                "1Txwrj4MPugGUfzbAsSI6oVC3HX4QR52ZrgOq8wCxG2Jfhxzh2" +
                "gbu1Gn4s2s3gh8UjWW7f+n7cZcciSJurtvadCM735/XcXX8se/" +
                "9ZR93PbY32QfvANfWZDQmfxsp4ROqvbGs4i/Pd79KP6zpgvjlq" +
                "7CIy6GPbv3Ncvd1rZ8+A7fxZ0HxmQ8KnsTIekXoY7nkdzuJ897" +
                "v0o8RRFq7lqLGLyKCPbcY/ZuQo4+/rebvJMT6/0939uFDWT5ur" +
                "+OSsfqc2+hf8pe7BGnfv9f3nBt9/1v3c7H4OT4YnJm2evnk6cT" +
                "t6kMvZvN79WIf18zdhAYVjyNE6sBkn46UVwSqyidjMHixLppGJ" +
                "5g8vhheT+cLmWeQCNqT7EOUYPi4dCYjwsk9jtKNSx63Is+Tl8c" +
                "iR2fAqReD6/FEZmXB+fd7c+vX997oHu7q+Pzv6f1yPnn2+m+t7" +
                "3c96v7TKfj59vup+1utRfT663d/PgyuT3dzJgyv3e8RzVM9sXl" +
                "n6gZkN8GAWeR5zziqCOdus9TEEnkYVEfVipV1/P8d/3vXv5+G/" +
                "Jru5k2a7x+LwQc9sXln6gZkN8GAWeR5zziqCOdus9TEEnkYVEf" +
                "VipaYZ3DPZzZ002z3jv8yPXNczm1eWfo/kAzyYRZ43uPfkitFi" +
                "RcdSm7U+hsDTqCKiXqxUr++bHoevTHZzJ812j8Xhg57ZvLL0A/" +
                "N6Hswiz2POWUUwZ5u1PobA06giol6stNz1aPz37bhfOnyx6vXo" +
                "8MVCz0f/7eb5aPxPvf+8Pc9Ho6d3/bdzeDm8NGmz+8yOHuRqRo" +
                "wxLtajZszUWFkxVo2sNEPRlUU/Njgzy5Jp1j/nl+f7t3+8Dc+b" +
                "3/y8zfN96tnQ72fdz3X2M2H6a72HXPn3czQcmbTZfWZHD3I5m9" +
                "e7H+uwHjUVhWPMi+vAZpyMl1YEq8gmYjN7sCyZRiaa375sXzZN" +
                "J23uIma57hI5OBDjHF0DRN8HzSxjrjErrQrUjBfX1i6ybhQBDM" +
                "qe8v61q8Hl4LJ4dp36zA8Jn8Zi3BFZT5+QL71OXxYjeR5z0yxl" +
                "qLUVX7u4jkG5pt9n1dvz9nyyr+c+T3f63G14OAcHYpyja4A4/0" +
                "tKZhlzjVlpVaBmvLi2dpF1owhgUPaU969dzd6O7nfH/M3pvn3k" +
                "beq+ZUFypNR1fYl/0/B8z83WANV5gVmMsC/Dj3HsAndrNfxYtJ" +
                "v6fmmtZ8D670dbvv8cfVm/ZauO9n57P/eZHxI+17FS4+wv0Tnm" +
                "OIoUV7v0o68DZdCHql0wk+v36SYf70+4D/it1N6+8aa419/Pbb" +
                "8Pqfu5+n4OHg8em/TZrU7rdJecgwyXnKNrTIc/ZnLE6+marKpn" +
                "ZLy4tnaRdVN2zywj08iEfl8sdjY4m8xnmN0aTP/XDCTnxAx8ot" +
                "911xSNcRFzDRmxKlAzXlxbu8i6KbtnlpFp2b92tfvzffDFmz3f" +
                "++pv7/6zPh/V6/utub6/Bv20BaM=");
            
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
            final int compressedBytes = 1397;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWk1vW0UUnUj8gAqQygKpC0AiKmxAFRW72s+WKpoFHxJiFz" +
                "Yom6wqfkAexJXIX2DFNvyT/gFW7aprBA0bZBB+nlyfc+7c19Tm" +
                "1YRm5inzce+55547Gj/bL06pfb99r30zpfZWWrT27ZSOvk1rtf" +
                "atHvtHheXdNFhrb1zg/2BNvncKy4drMnyc0uTx5HHu89jZ88rm" +
                "1gODCz7GaAwYLa8iS5/NWJVmBWuki3NrFVE1ygAFZU1x/VpVtM" +
                "/rns/a5JTW1/uAr/e6n8PuZ3O7uZ37PHb2vLK59cDggo8xGgNG" +
                "y6vI0mczVqVZwRrp4txaRVSNMkBBWVNcv6tqv9lfjPs2Lj37to" +
                "aFMbjgY4zGgHGVWZClz2asSrOCNdLFubWKqBplgIKyprh+V9VB" +
                "c7AYD2xceg5sDQtjcMHHGI0B4yqzIEufzViVZgVrpItzaxVRNc" +
                "oABWVNcf2uqsPmcDEe2rj0HNoaFsbggo8xGgPGVWZBlj6bsSrN" +
                "CtZIF+fWKqJqlAEKypri+rWqyXgyXnxuGtu4/CQ1trW3AMtojj" +
                "c74hC/+qTmWNgHjObBmnkiXZoRqrwaz83qobJU6pV4fNlOduun" +
                "yE3b5OHkYe7zaLa8hoUxHq1/1oMdsWZlm/pUGfLYSnkiXeb3Gl" +
                "mNVswMnJ//fGSpGtmK70d/1XO2aRtdG13LfR7NltfeAiyjOd7s" +
                "iEM8cioL+1gX58GaeSJdmhGqvBrPzeqhslTqlXh8PZ/1+/uL/7" +
                "75/Y+bfn+vz5f+y/M5++VqnM8HX27n+dLx51djP48/q8/rLsfz" +
                "z//r/fN47wL/J/X++TLcP+v5HLI92Jn91p3P2a/trdnf+XzOng" +
                "7EXZ7uP4ZTPvv9Av98Y+Y/z8cNvtfcObpzlPs8mi2vvQVYRnO8" +
                "2RGHeORUFvaxLs6DNfNEujQjVHk1npvVQ2Wp1Cvx+Pr+PuT7+3" +
                "RnupP7PHb2vLK59cDggo8xGgNGy6vI0mczVqVZwRrp4txaRVSN" +
                "MkBBWVNcv1bV3GvupdT1eew8eWVz64HBBR9jNAaMllmRpc9mrE" +
                "qzgjXSxbm1iqgaZYCCsqa4flfVXrN4n+z6PC49e7aGhTG44GOM" +
                "xoBxlVmQpc9mrEqzgjXSxbm1iqgaZYCCsqa4fq1qmqbdWU02pt" +
                "XK5tYDMy0QU8FoTKLY5NimoS85FWVWsEa6OLdWEVWjDFAwDZWW" +
                "9SeHD+6rT2x2crc+L1qvjR+NH8W2bEcPm/q83xh53pfZMDGKmQ" +
                "zH2nwF6mFW5dcqnqWgjOm3eV3kOe1lPX0ebF98P+/ap+B0GMyW" +
                "zuzPz++JsH3x/bzDKXwR2f5tq8/n635eltbcbG7aTG15jR429Z" +
                "V+z9SfOccwivFmt96uZ1XAemNWX4VX0Ke2f+c4e7PbnP+6pln9" +
                "yibb8ho9bOor/Z6pR+Gu5WEU481uvV0lj6qwWczqq/AK+tRG+j" +
                "2i2W2vB58/X9vo+cQb5+OrW34e/vplvwecfFrvg3U/L/F+flH3" +
                "YND9/KruwdX7/Fn/X1z/X1zP5yD3z6/rPW/j75v3m/u5z6PZ8h" +
                "oWxuCCjzEaA0bkZGTpgzKo0qxgjXRxbq0iqkYZoKCsKa5fq6rP" +
                "Q7bwev+m7sG23t9Dhvp7hpfg/f3yttE8993Y9aO52bvZDzeyHd" +
                "jRXGNH85IxtpvnIh2sIsaN5t89YTaf0bh0zTOvsP3J61QFyoh8" +
                "PlN9vQ/7eq/7OfT9c/xK7rux6/PaLNkPG+bRmiNLOzijBh2sIs" +
                "ax5igjlPOaZ30KwadeZUQ+n2nx2j/LfTd2fV6bZXH/PDNMHm0e" +
                "rTmytJun57640sEqYtzobHn/dFoRY1y65plX6O6fhQJlRD6fqb" +
                "6/D9r+AR1hC38=");
            
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
            final int compressedBytes = 1396;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXD1vHGUQXtGnswQFIgUgUUAThKJQRMK79y8QoorkMghSw+" +
                "aUgK9yT4UQAlkREhT8DWhpfAVpXUaiw7tzc88zH7u+Pa9IZL/v" +
                "iXnn45mZZ17t3e1tkKuqqurXql52eyfFVo/E4YOe2ZwZ/aiZLf" +
                "BgFjmOOWcdwZxt1oYYop6N2oro5ztV1eELkd3eSbHVs/xC/MCq" +
                "ntmcGf0ayRd4MIscd/hi9SVX8x21lrVZ8wzbHzxPy8BWRD/fqf" +
                "2gfb9982L/qLfeqapvvqoG17ffRV/7do5t7wbPe9Vsq719SfzO" +
                "xHrvsrX8/sLz4cQKH+f+sfMsa/p68kc5g33XYrlYipRdfWJ7D7" +
                "CM5nz1Iw/56GmrcIx5cR/YXCfjZTuClWfjazN7sIxMPROLbx42" +
                "D6uqk7J3EbFUVwkMXogxxuagop6DRcaYaszKdkXVjBf3tlNk09" +
                "gKYBBnyue3U039Pko/h6/h91Hv2ev7qJznvOeZfKb+ex2+Gb5+" +
                "6+X0nXZ9rpbl+hy7PpvT5lSk7P0n66na8DAGL8QYY3NQcfvJbZ" +
                "Axphqzsl1RNePFve0U2TS2AhjEmfL57VTlfn7+dfizyG7vpNjs" +
                "UYzsqme2zeT6HPG9PQ9mkfNlbrEjasU+Y5NYnhnT2M/660f1I5" +
                "G6q9Vpna6SMUCoZIzN0S6qeyRHtJ/Nybrm3H0G82c2nBWnZ5ae" +
                "qWdCz58kdlKfXOwn2NXqtE5XyRiPwH/er7pqthrXRUw1IHxXVM" +
                "14cW87RTZNnJ5ZeqZxfjtVuf8s9/PlPMt53vTz3P95cjlPf57L" +
                "3/e/PhfrxVqk7P3v97Xa8DAGL8QYY3NQcft8wCBjTDVmZbuias" +
                "aLe9spsmlsBTCIM+Xz26nK76OX+zykfB+Nv9/rB/UDkbqr1Wmd" +
                "rpIxQKhkjM0RHX6P5Ij2szlZV0VkvLi3nSKbJk7PLD1Tz4Tu5z" +
                "f4cn2W+6Vyv3RT7peag+bA+8Unfkj4bCzG2R+rc0xyhlDqV6mv" +
                "HGUZ2t42x04xxiDmDPu0e7k+5/38jKv+cSgjRjLsUP5w3alrl0" +
                "rzdZuymotX7ms2ukr4VG8oowo5Q9U51tBeuVxkN4SPFRvHaohf" +
                "FazGMRk/p8t8m57nzXnVS9n7yLna8DAGL8QYY3NQcdvZIGNMNW" +
                "Zlu6Jqxot72ymyaWwFMIgz5fPbqVY/xXN/+vgqV/zT9ib/2lzc" +
                "W9wTKbv6xPYeYBnN+epHHvLR01bhGPPiPrC5TsbLdgQrz8bXZv" +
                "ZgGZl6Jhbfvh7P+Phz1Va/TPh2e2OT/dn//Pzh4JW6Pu8v7ouU" +
                "XX1iew+wjOZ89SMP+ehpq3CMeXEf2Fwn42U7gpVn42sze7CMTD" +
                "0Tj0+uz0/LU7e93y2j7/dJlcr7vbtKm0UjUnb1ie09wDKa89WP" +
                "POSjp63CMebFfWBznYyX7QhWno2vzezBMjL1TDw+rtWv5X075y" +
                "r/3jHH+uQflaJl0aiPV5sWsajdugBlmV/ef7z+UH9k71an/4Xz" +
                "d7m6yvu9nOc1vQMtz5MFP8//P3/UHImUvX/ydKQ2PIzBCzHG2B" +
                "xU3D7ZMsgYU41Z2a6omvHi3naKbBpbAQziTPn8dqp6Xa/Dk+3e" +
                "J35I+GzMx7Ui69nqYorJUVxJcczNoixD29vWt1OMMYg5w77NLG" +
                "f1WcD1PvFDwmdjPq4VWU8ZnmmfIRRXUhxzsyjL0Pa29e0UYwxi" +
                "zrDP86LIs8Gqz3bBDuUP1526dqk0X7errtWf5Vt61vP8q5xBuV" +
                "96Ne6XsnX8W7nK5lzl712U9/vc7/er/H2b+pbIbu+k2Oo5vi1+" +
                "YFXPbM6Mfo0M3OFseTCLHFffevycq/mOWsvarHmG9u8vRQa2Iv" +
                "q5Tv8BGDKq6w==");
            
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
            final int compressedBytes = 1518;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrFuXFUQfVgK6ZDcQ1IAUgpoQBESTeR93g/gDxCNUxPR47" +
                "W1sTCfQEWLFMmi4ANQKn9CqihCSmgQQS6MCGLvjmfPOXPnrb3L" +
                "RongXmvn3pk5c+bcm/uy6026rutGL7q5LXOx5nvE8ohhnflcWc" +
                "fBmQ3oYBU5jjVnHaGcfV4NKQSfZpUR/WKnruuvmS1zseZ75Jub" +
                "FgfW15nPlXXcM/mADlaR4/prB78wW+zoXOrzKiqcfB91qgJlRL" +
                "/YaXa2Z2bLXKz5HrE8YlhnPlfWcXCmd2Khg1XkONacdYRy9nk1" +
                "pBB8mlVG9IudZmd73WyZizXfI4dfWhxYX2c+V9Zxzwzcu4UOVp" +
                "Hj+uvHT5ktdnQu9XkVFYb7WSlQRvSLnSYfTj6YvD2bb8+997pu" +
                "/6tupTF5dyD+SRW51W1sTG5ekv9oRb732Tv8bhb5eEWGT2f39H" +
                "R0atbm+d09dR8RxuAHOcZoDRgXT44g65yvWJV2BWumi3vrLrLd" +
                "KAMU1HvK96+7WvV+Hv/6etzP0cN17+fo4VXu59HZevczG6s+72" +
                "3Iqba/P5P7OY+sdT/beW72POsx/ak9teuO8f5436zNHjM/RoBl" +
                "NNd7HHWoR09l4Rzr4j7wmSfTpR2hKqqJ3KweKmulUUnEr/a8H3" +
                "3x/3jejz5f/3kf/WC2zMWazxHH2OzrzNdK5ueM1uvaFERcrGFt" +
                "dUdw1X2W7UR1ZkrrfrFTez9q7+/tPNt5tvNs59nO87/2fV07z/" +
                "o8D39c/372d/u7Zn12r6zK2i1jgHDLGK2xNeIRyRnvpzVZV0dk" +
                "uri37iLbTb17VhmVRiUYXNu+r2vf17X3o/Z+1N6PVn4/utffM+" +
                "uze2VV1m4ZA4RbxmiNrRGPSM54P63Jujoi08W9dRfZburds8qo" +
                "NCqh9yPL7fV7s3kPs3tlVdZuGQOEW8Zoja0Rj0jOeD+tybo6It" +
                "PFvXUX2W7q3bPKqDQqofOc53a3drfivbWYxWER8zUqNc/xmp1z" +
                "zqNMsdqt/2RqWUWujyvivExnxjDEurvVP+ofRZzFLA6LmOZi3h" +
                "l5nY2Sc0yOYibHsTZFqULtrfy6i2UK6prhmHXf3dud3dNibZ6f" +
                "9J77iDAGP8gxRmvAuPiTFGSd8xWr0q5gzXRxb91FthtlgIJ6T/" +
                "n+dVfjO+M7XVeszSVjnq85Aiyjud7jqEO9d44snANG+8BnnkyX" +
                "doSqqCZys3qorJVGJYofbY+2u65Ym+ff3W+7HyPAMprrPY461C" +
                "/+5SCwcA4Y7QOfeTJd2hGqoprIzeqhslYalUR8+/y52c/zPnae" +
                "urWVDo5l+WX4q2YUdbUuQKnyy/sv5x/qj+rLeHaeuLVVlq3Xg3" +
                "qerJ5R1NW6AKXKL++/nH+oP6pznvtvTH8vz/v0t8nt6d/2vE//" +
                "2MwTeb+KTM8297xPn1+S/2tt5j8v5her146ejZ6Ztdlj5iPCGP" +
                "wgxxitASN6MrLOQRlUaVewZrq4t+4i240yQEG9p3z/uqv2/efL" +
                "H+08Nzt2fn71DK96d+vuoH/cP85jFodFTHMx74y8HursmBzFTI" +
                "5jbXEHmmFW5dddLFNQ1wzHoi6M48/aM7rJ0c5zs+Pbd9oZbPQ8" +
                "b7Qz2ORo/39+/TF+MH5gtsyIuQ/rMWQ5h5dbsNf8HNOcKkMf96" +
                "LOWpfno0ZWw1XKwP35pTYqIdaT8clsPrH5InMCH9ZjyHIOL7fO" +
                "BEZEOaY50id93Is6a12ejxpZDVcpA/fnl9qohPHt+88LfPv/IS" +
                "/xPP/N98n9W2bLXKz5HrE8YlhnPlfWcXCmv4MsdLCKHMeas45Q" +
                "zj6vhhSCT7PKiH6xU7uf7Xl/nc9zdlffNFvmYs33iOURwzrzub" +
                "KOgzN9xhY6WEWOY81ZRyhnn1dDCsGnWWVEv9ip3c9Nvh+ND8YH" +
                "Zm2ef5I6cD9GgGU013scdahffFILLJwDRvvAZ55Ml3aEqqgmcr" +
                "N6qKyVRiUV/rzay3mJWRwWMc1xnrP1uu4SeTIditdYhvLV1zeG" +
                "eys2Y73S75bn0Zu9/gGSg019");
            
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
            final int compressedBytes = 1652;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWT1vHVUQ3TL0SFAgXAASBTQghGS7sLzZBkSTIlQg6tiSUz" +
                "hKIlnIGGiiyJbsgoaKX5AfYv8MfkOgY++7Pj5nzh1jOX4iFrkb" +
                "7XyemTlz9V7e2+dhaK/p7+F/cO2//3rmHnx68MnBe7P+YuF9OA" +
                "w/Pbpmhw8uiX/ZRD5eIu+VK/KfXbPfR03k82t2WB2G8cH4oEpo" +
                "eMUqNqRiiIBUTKypNuOO1AzmxZpsKhAZL50dt8i2abdXls7Umf" +
                "A6zz0a51djkdDwxsWrlFIxREAqJtZUm3FHagbzYk02FYiMl86O" +
                "W2TbtNsrS2fqTOQ8a25n3Jn1DjW8YhUbUjFEQCom1lSbcUdqBv" +
                "NiTTYViIyXzo5bZNu02ytLZ+pM5Dxr7mg8mvURNbxiFRtSMY7g" +
                "7XHYsGI37cscLCJ8KrtmvHR23CLbpt1eWTrTdn/banfcnfUuNb" +
                "xiFRtSMURAKibWVJtxR2oG82JNNhWIjJfOjltk27TbK0tn6kzk" +
                "PGtue9ye9TY1vGIVG1IxREAqJtZUm3FHagbzYk02FYiMl86OW2" +
                "TbtNsrS2fqTOQ8a+5wPJz1ITW8YhUbUjGO4O1x2LBiN+3LHCwi" +
                "fCq7Zrx0dtwi26bdXlk603b/uNXBO+33qGc/wHr+1TW+fb17Xv" +
                "39f/z9+e3b9BQxrU6rVVaNWPU9QqyitR5x1rGeM2MXzSkvnUNf" +
                "+2S84kSycjbeW9mTZcvUmUT8tDFtzHoDepHZgO8RYhWt9Yizjv" +
                "UX52BdNEdMnENf+2S84kSycjbeW9mTZcvUmUT8dDqdzvoUepE5" +
                "hc+IYhwdb0h0YkdGNRZzcqJhDrzYJ+OFvHNUNnFj7aDz9fbKlj" +
                "Ww/fk9e37/5fdXe37Pruue5+u5fv3mivzXt4Xp8/tDv5Z5nt/2" +
                "M1jm1d/vN/j++WJ6UWXRjMGnRIxZzfGGZPe2v8ZiLjLjHHjOs+" +
                "WFvHNUNloVO+h8vaN0JopP3u/f9ffom/d+v73X5ssqiy6y+og8" +
                "W6lxYmFnvla2cWSu4qEsctzmy5//1G4+Eb2ir5YzPPjDeUYGsS" +
                "Pn+aT+fX6Zf4/r57nc85z2p/0qq158Uu3D9wixitZ6xFnH+otP" +
                "QuuiOWLiHPraJ+MVJ5KVs/Heyp4sW6bOJOLv3rt7bxiKrLpkqg" +
                "cbkhj+Y04xsYYdcQ4R2eZgKas4lV0zXjo7bpFtEzuQQbtTvn/c" +
                "qr/fl/t+Hx+Pj6uEhlesYkMqhghIxcSaajPuSM1gXqzJpgKR8d" +
                "LZcYtsm3Z7ZelMnYn8vaPmtsatWW9RwytWsSEVQwSkYmJNtRl3" +
                "pGYwL9ZkU4HIeOnsuEW2Tbu9snSmzkTOs+b2xr1Z71HDK1axIR" +
                "VDBKRiYk21GXekZjAv1mRTgch46ey4RbZNu72ydKbORM6z5k7G" +
                "k1mfUMMrVrEhFeMI3h6HDSt2077MwSLCp7Jrxktnxy2ybdrtla" +
                "UzbfePW/XPoyV/Hj0dn1YJDa9YxYZUDBGQiok11WbckZrBvFiT" +
                "TQUi46Wz4xbZNu32ytKZOhN5fdbc8Xg862NqeMUqNqRiHMHb47" +
                "BhxW7alzlYRPhUds146ey4RbZNu72ydKbt/nGr/n5f8vPm2rRW" +
                "ZdWLJ6c1+B4hVtFajzjrWH/xnGhdNEdMnENf+2S84kSycjbeW9" +
                "mTZcvUmUT8dDadzfoMepE5g8+IYhwdb0h0YkdGNRZzcqJhDrzY" +
                "J+OFvHNUNnFj7aDz9fbKljWnNb/P/9h/E+6/z9/e6/l+P4MbfE" +
                "72z/f++3w/zzfmPOdv9XeqLLrI6iPybKXGiYWd+VrZxpHJL/JQ" +
                "FjluvFP+vulcWYNe0VfLGca/b7YMYkfO80nDsPlXlUUXWX1Eap" +
                "4x2pmvlW2cPbOLPJRFjlPO2UQyV1+tyxiyX8zGjpwX4+OT8UmV" +
                "0PCKVWxIxRABqZhYgymwHakZzIs12dScu1cof2WjVe32ytKZOh" +
                "N5/Z7j+/+f/fOon2c/z36er/R73fq0XmXVi19K1uF7hFhFaz3i" +
                "rGP9xS8x1kVzxMQ59LVPxitOJCtn472VPVm2TJ2J45Pn99/6U/" +
                "hNrvGtKosusvqI1DxjtDNfK9s4e/47D2WR45RzNpHM1VfrMobs" +
                "F7OxI+f5pP7/Z/886uf5ppzn+HB8WCU0vGIVG1IxREAqJtZUm3" +
                "FHagbzYk02FYiMl86OW2TbtNsrS2fqTOT/13N8f30u9f3+D0A/" +
                "LP0=");
            
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
            final int rows = 12;
            final int cols = 84;
            final int compressedBytes = 602;
            final int uncompressedBytes = 4033;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNptlG1uajEMRNlbdJcDEn+LYFFQWF+TZ+bNsWOk+GvG9hi1jP" +
                "M4n07jPP57ZStasSw5ZsiSk3sidr0yiWhf7um2itHp4u58RXfN" +
                "fj1VVqVViT+BHb/H7+m07PLxiSxyW9WMEvOT1SRPdJW1jPmT9y" +
                "irOnddwqtGqmFXnsD9fNlWJZj6PJ7TP8N/kadzW9WMEvOT1SRP" +
                "dJW1jEFf2qOs6tx1Ca8aqYZdeQL382VblZg/7uM+/07v9spWtG" +
                "JZcsyQJSf3ROx6ZRLRvtzTbRWj08Xd+Yrumv16qqxKqxL8vwd2" +
                "Hdfpr/bKVrRiWXLMkCUn90TsemUS0b7c020Vo9PF3fmK7pr9eq" +
                "qsSqsSfJ+B/Yyf6X/sla1oxbLkmCFLTu6J2PXKJKJ9uafbKkan" +
                "i7vzFd01+/VUWZVWJfg+A7uMy/QXe2UrWrEsOWbIkpN7Ina9Mo" +
                "loX+7ptorR6eLufEV3zX49VValVQm+z3/Y8Tk+83f0E/77y/px" +
                "bquaUWJ+sprkia6yljH8vqc9yqrOXZfwqpFq2JUncD9ftlUJpr" +
                "6O1/Sv8F/k5dxWNaPE/GQ1yRNdZS1j0Jf2KKs6d13Cq0aqYVee" +
                "wP182VYlmPo+3tO/w3+Rt3Nb1YwS85PVJE90lbWMQV/ao6zq3H" +
                "UJrxqphl15AvfzZVuVmD8e4zH/7x/2yla0YllyzJAlJ/dE7Hpl" +
                "EtG+3NNtFaPTxd35iu6a/XqqrEqrEvx+BnYbt+lv9spWtGJZcs" +
                "yQJSf3ROx6ZRLRvtzTbRWj08Xd+Yrumv16qqxKqxJ8n8H/A3gr" +
                "sOw=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 35, 0, 4, 0, 36, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 6, 0, 0, 38, 7, 0, 0, 39, 40, 8, 0, 0, 0, 41, 42, 0, 43, 0, 44, 0, 0, 9, 0, 45, 0, 10, 46, 11, 0, 47, 0, 0, 0, 48, 49, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 0, 11, 0, 0, 0, 0, 0, 0, 51, 1, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 2, 0, 14, 0, 15, 0, 0, 52, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 0, 1, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 53, 0, 0, 0, 20, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 55, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 56, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 57, 0, 58, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 59, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 60, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 11, 0, 2, 0, 12, 0, 0, 0, 0, 0, 13, 0, 0, 61, 14, 0, 62, 0, 0, 0, 63, 0, 0, 0, 64, 65, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 14, 0, 0, 66, 15, 0, 0, 16, 0, 0, 67, 17, 0, 0, 0, 0, 0, 24, 25, 26, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 28, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 29, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 2, 0, 32, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 34, 0, 0, 0, 5, 4, 0, 0, 35, 0, 36, 37, 0, 0, 0, 0, 0, 0, 0, 38, 3, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 39, 16, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 1, 0, 0, 0, 1, 6, 0, 5, 0, 43, 0, 7, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 68, 45, 0, 46, 0, 47, 48, 49, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 54, 0, 1, 55, 0, 0, 0, 8, 56, 0, 57, 0, 58, 0, 0, 0, 6, 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 59, 60, 9, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 8, 61, 62, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 63, 0, 0, 0, 0, 69, 0, 0, 0, 64, 0, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 66, 67, 17, 18, 0, 19, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 68, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 69, 25, 26, 0, 0, 0, 70, 71, 0, 0, 0, 4, 0, 72, 0, 5, 70, 0, 0, 73, 1, 0, 0, 0, 27, 74, 0, 0, 0, 28, 0, 0, 29, 0, 0, 0, 1, 0, 71, 0, 0, 0, 0, 0, 0, 72, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 0, 0, 19, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 11, 0, 75, 76, 12, 0, 73, 77, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 78, 0, 13, 79, 80, 81, 82, 0, 83, 74, 84, 1, 85, 0, 75, 86, 87, 88, 76, 14, 2, 15, 0, 0, 0, 89, 90, 0, 0, 0, 0, 91, 0, 92, 0, 93, 94, 5, 0, 0, 0, 0, 0, 0, 95, 96, 9, 0, 0, 2, 0, 97, 0, 0, 98, 1, 99, 0, 3, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 101, 102, 0, 0, 0, 0, 0, 0, 2, 0, 0, 103, 104, 0, 3, 0, 4, 0, 0, 105, 1, 106, 0, 0, 0, 107, 108, 0, 0, 10, 0, 1, 0, 0, 0, 4, 109, 5, 0, 1, 110, 111, 0, 0, 4, 112, 0, 6, 113, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 114, 0, 0, 0, 0, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 6, 0, 16, 0, 115, 17, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 0, 0, 116, 7, 0, 117, 118, 0, 11, 0, 0, 0, 12, 0, 119, 0, 0, 20, 0, 2, 0, 0, 7, 0, 0, 0, 4, 0, 120, 121, 0, 5, 0, 0, 0, 0, 0, 122, 0, 0, 0, 123, 124, 125, 0, 8, 0, 126, 0, 13, 9, 0, 0, 2, 0, 127, 0, 2, 3, 128, 0, 14, 0, 129, 0, 0, 0, 15, 10, 0, 0, 0, 0, 77, 0, 0, 1, 0, 1, 0, 21, 0, 0, 0, 22, 0, 130, 131, 0, 132, 133, 134, 135, 0, 23, 24, 25, 26, 27, 28, 29, 136, 30, 78, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 137, 46, 47, 48, 49, 138, 50, 51, 52, 55, 56, 57, 0, 0, 0, 139, 0, 0, 0, 0, 1, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 140, 141, 142, 0, 143, 0, 59, 4, 79, 0, 144, 7, 0, 0, 145, 146, 0, 0, 11, 60, 147, 148, 149, 150, 80, 151, 0, 152, 153, 154, 155, 156, 157, 158, 159, 61, 0, 160, 161, 162, 163, 0, 0, 7, 0, 0, 0, 0, 0, 62, 0, 0, 0, 164, 0, 165, 0, 0, 0, 0, 1, 0, 2, 166, 167, 0, 0, 168, 0, 169, 12, 0, 0, 0, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 171, 172, 173, 0, 174, 0, 8, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 175, 176, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 82, 0, 13, 0, 0, 0, 177, 2, 0, 3, 0, 0, 0, 14, 0, 178, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 179, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 180, 0, 181, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 182, 0, 183, 184, 185, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 186, 0, 187, 188, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 189, 0, 0, 0, 0, 0, 0, 17, 9, 10, 0, 11, 0, 12, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 0, 190, 0, 0, 191, 0, 0, 0, 192, 22, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 193, 24, 18, 0, 0, 0, 0, 0, 0, 194, 0, 0, 1, 0, 0, 19, 195, 0, 3, 7, 15, 0, 0, 1, 0, 0, 0, 1, 0, 196, 25, 0, 63, 0, 0, 197, 0, 198, 0, 199, 0, 200, 20, 0, 0, 201, 0, 0, 21, 0, 0, 0, 83, 0, 26, 0, 202, 0, 0, 0, 0, 203, 0, 22, 0, 0, 0, 0, 18, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 204, 0, 0, 0, 0, 0, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 0, 0, 0, 1, 205, 206, 2, 3, 0, 0, 0, 0, 0, 0, 207, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 208, 0, 0, 0, 209, 64, 0, 210, 0, 0, 3, 0, 0, 65, 86, 0, 0, 24, 0, 0, 0, 27, 211, 0, 212, 25, 28, 0, 213, 214, 0, 26, 215, 0, 216, 217, 218, 0, 219, 29, 220, 27, 221, 222, 223, 28, 224, 0, 225, 226, 6, 227, 228, 30, 0, 229, 230, 0, 0, 0, 0, 0, 66, 0, 2, 0, 0, 231, 0, 232, 0, 233, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 234, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 235, 0, 23, 24, 30, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 236, 4, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 237, 238, 1, 0, 1, 32, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 239, 69, 0, 0, 240, 0, 0, 241, 242, 0, 0, 0, 0, 33, 34, 0, 0, 3, 0, 0, 243, 0, 244, 0, 87, 245, 0, 246, 0, 0, 35, 0, 0, 0, 247, 0, 248, 36, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 249, 0, 250, 0, 1, 38, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 7, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 251, 37, 252, 253, 38, 254, 0, 255, 0, 0, 0, 0, 39, 256, 0, 41, 0, 257, 0, 40, 258, 41, 0, 259, 0, 260, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 261, 262, 0, 0, 263, 0, 8, 0, 43, 0, 0, 264, 265, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 267, 268, 269, 270, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 271, 0, 0, 0, 0, 272, 44, 10, 0, 0, 12, 0, 13, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 70, 0, 0, 273, 0, 0, 0, 274, 0, 0, 0, 0, 45, 0, 0, 275, 276, 277, 0, 46, 278, 0, 279, 47, 48, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 0, 8, 49, 283, 284, 50, 285, 0, 0, 51, 0, 3, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 0, 289, 290, 52, 0, 0, 0, 53, 0, 0, 291, 0, 0, 0, 292, 0, 0, 293, 0, 0, 294, 1, 0, 0, 0, 5, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 295, 44, 0, 0, 0, 0, 0, 71, 0, 0, 54, 0, 0, 0, 0, 0, 0, 0, 0, 296, 0, 0, 0, 0, 2, 0, 297, 14, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 298, 45, 0, 0, 0, 299, 0, 0, 0, 0, 0, 0, 300, 0, 0, 0, 0, 0, 55, 0, 0, 56, 0, 301, 0, 0, 0, 0, 0, 0, 57, 0, 0, 36, 0, 0, 0, 37, 5, 302, 6, 303, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 304, 305, 3, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 306, 0, 307, 0, 308, 0, 0, 309, 0, 0, 0, 310, 0, 0, 58, 311, 0, 0, 0, 0, 0, 312, 0, 0, 7, 313, 0, 0, 0, 314, 315, 0, 46, 316, 0, 0, 0, 59, 88, 0, 0, 0, 317, 318, 60, 0, 61, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 89, 0, 0, 0, 2, 47, 62, 0, 0, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 319, 0, 48, 320, 49, 72, 0, 50, 0, 0, 0, 0, 321, 64, 0, 0, 322, 65, 66, 0, 51, 0, 323, 67, 324, 0, 52, 68, 325, 326, 69, 70, 0, 53, 0, 327, 328, 0, 71, 54, 329, 0, 55, 0, 0, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 330, 0, 9, 331, 0, 0, 332, 333, 334, 73, 0, 0, 0, 335, 0, 0, 0, 336, 337, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 338, 74, 0, 0, 0, 0, 75, 0, 0, 38, 0, 0, 0, 0, 0, 339, 59, 340, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 341, 0, 342, 343, 0, 0, 0, 28, 0, 0, 0, 344, 0, 0, 0, 0, 0, 345, 0, 61, 346, 62, 0, 63, 347, 348, 0, 0, 64, 349, 0, 65, 0, 0, 76, 0, 0, 350, 351, 0, 0, 77, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 352, 353, 90, 0, 354, 0, 0, 0, 355, 0, 0, 0, 78, 0, 0, 0, 0, 0, 66, 0, 79, 0, 356, 0, 80, 67, 357, 358, 359, 360, 0, 81, 82, 0, 361, 68, 83, 362, 0, 363, 364, 365, 84, 0, 0, 0, 366, 0, 0, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 0, 0, 0, 0, 69, 367, 0, 70, 0, 0, 0, 85, 0, 4, 5, 0, 0, 6, 0, 0, 3, 0, 0, 0, 368, 0, 369, 86, 370, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 371, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 87, 73, 74, 372, 75, 0, 88, 89, 76, 0, 77, 373, 0, 374, 375, 0, 0, 376, 377, 0, 0, 0, 7, 0, 91, 90, 0, 0, 378, 0, 379, 0, 380, 381, 0, 91, 382, 383, 384, 385, 92, 93, 0, 0, 0, 386, 0, 387, 388, 389, 0, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 390, 0, 0, 0, 0, 0, 7, 0, 16, 0, 0, 0, 0, 391, 0, 392, 0, 0, 96, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 97, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 393, 394, 0, 0, 395, 396, 0, 397, 0, 0, 0, 0, 98, 99, 0, 0, 0, 92, 93, 0, 100, 101, 102, 398, 0, 103, 104, 0, 0, 0, 0, 80, 0, 0, 105, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 399, 0, 400, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 401, 0, 0, 0, 0, 0, 0, 0, 0, 402, 106, 0, 82, 107, 108, 0, 83, 403, 404, 0, 0, 0, 405, 0, 0, 109, 0, 0, 84, 0, 406, 0, 0, 85, 0, 407, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 408, 0, 0, 0, 409, 0, 87, 410, 0, 411, 0, 88, 0, 110, 111, 112, 0, 412, 0, 113, 413, 414, 0, 114, 415, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 116, 117, 0, 416, 0, 417, 0, 0, 118, 418, 0, 119, 120, 419, 0, 121, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 122, 123, 0, 124, 0, 0, 125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 3, 0, 4, 5, 2, 6, 3, 2, 0, 3, 3, 7, 8, 1, 9, 1, 2, 0, 4, 10, 1, 6, 9, 6, 1, 0, 11, 9, 12, 5, 11, 1, 13, 3, 9, 1, 3, 7, 0, 14, 15, 16, 17, 9, 11, 18, 16, 2, 16, 19, 3, 6, 11, 20, 4, 17, 7, 21, 22, 23, 24, 1, 0, 25, 26, 2, 27, 28, 1, 29, 30, 0, 3, 31, 16, 2, 32, 0, 11, 33, 34, 9, 1, 0, 8, 35, 36, 16, 1, 37, 38, 4, 1, 39, 1, 5, 6, 40, 41, 6, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 1, 48, 49, 3, 3, 50, 9, 51, 52, 53, 54, 1, 1, 3, 1, 55, 56, 1, 4, 5, 0, 57, 0, 58, 59, 18, 7, 60, 61, 62, 63, 2, 18, 15, 64, 65, 66, 9, 67, 20, 68, 2, 69, 3, 70, 0, 71, 72, 73, 0, 0, 1, 20, 74, 2, 75, 76, 77, 21, 4, 78, 20, 79, 80, 81, 3, 82, 83, 3, 6, 7, 2, 84, 3, 85, 86, 5, 87, 1, 88, 1, 89, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 1, 1, 99, 7, 2, 100, 101, 102, 103, 25, 104, 105, 106, 0, 107, 108, 4, 109, 0, 110, 16, 7, 8, 3, 27, 111, 112, 9, 5, 113, 3, 3, 1, 114, 2, 10, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 9, 21, 0, 125, 9, 1, 1, 126, 127, 2, 29, 0, 4, 0, 128, 10, 2, 11, 129, 30, 130, 131, 132, 3, 8, 29, 1, 133, 11, 1, 134, 5, 12, 5, 2, 135, 17, 14, 9, 136, 137, 138, 21, 22, 14, 5, 10, 139, 1, 8, 140, 141, 21, 142, 4, 143, 144, 5, 145, 146, 147, 148, 149, 150, 22, 31, 151, 152, 9, 10, 153, 25, 24, 8, 154, 155, 4, 156, 3, 157, 158, 159, 160, 17, 161, 2, 162, 163, 164, 33, 14, 165, 166, 167, 35, 168, 2, 7, 5, 169, 170, 9, 36, 171, 172, 2, 173, 174, 175, 39, 29, 40, 176, 177, 4, 178, 43, 9, 10, 179, 180, 13, 44, 181, 182, 183, 184, 185, 186, 15, 4, 187, 188, 21, 6, 9, 18, 189, 190, 191, 11, 192, 193, 12, 1, 194, 195, 196, 18, 0, 10, 20, 197, 28, 18, 198, 2, 11, 17, 7, 3, 9, 23, 0, 199, 12, 200, 201, 0, 8, 12, 202, 203, 204, 9, 205, 206, 13, 207, 22, 208, 209, 2, 6, 210, 211, 212, 31, 9, 11, 11, 2, 1, 213, 9, 30, 11, 214, 215, 11, 216, 217, 45, 218, 14, 219, 220, 221, 2, 222, 223, 224, 10, 24, 47, 3, 13, 225, 18, 14, 226, 227, 6, 228, 229, 43, 230, 56, 231, 232, 233, 1, 8, 234, 235, 236, 237, 238, 3 };

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
            final int compressedBytes = 1582;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2O3DYQHnLn1spmC65xhe2KiRepUhyCPABjXBEYKbZIkf" +
                "KAS5fGj0AfEiBIUvgRzn2KPMICyQPkEfwILvIAobRarbSi/lYS" +
                "RUkzgNenNU8i5+eb4cxQvmdv4dcX+sp8Ll/p5z+oJyC/UT9+Kf" +
                "6B7zW8++9z3OGCf7rG139/9fTN43O5ZfIaNvqXPz/8/t1fQDQF" +
                "4rlvVuYPFo5R4YcGkMS6iZLIyT9F7PiDB/J/afQwCAAMfiHXzw" +
                "QsQTL1RTizW4AbxfFf9gms1qgWkgvYwFbIW/PX1R93ek34NTjd" +
                "h/7nGYSfSx76n2Xof7bb2P984LgDxkP5Gf+zC/0PxP7n7jfyPw" +
                "aK3zLQIjTJwCA0U+oAzyj2gBr0Y2jFmofGvH8q1GMgD8b9TktY" +
                "KcLvBD/eG/xYJfixNZx5NPghDH7sGMMIP+4O+AHbKeGHYgujGY" +
                "n+fKuMdqgA1ptIf/aR/oS+H3miP0bd4GctNemPH/gpC/Dz1uDn" +
                "xxA/N3b8pPi92v98zPifN8S/HvHnwSDvGf7oIvzRFvwJ4rj1GM" +
                "Liaeei43/A8ErGw2RA+HUirBwQEJOIiIiIiCYV/5Xnv2ERxn+v" +
                "ivPfKccorB41GsCOKVR5iEZuoIv44/4qE7/ubPF/Wf7kJ/Yeqv" +
                "YPjPYPRO7punJEl/E7i4w3SBlzZL8ivg5EdqMR/YL0BL9i+11c" +
                "YP9eEI8/EGPQZPkdCe9Z/gNRUr8Q6fpFmH+EU/4xql+8jOsX4G" +
                "H9Ii0qzo/yMo7uVCvKbdUzvpKIqHtMqat/3DrAcf1bpS9EnYxM" +
                "F8+310+P+HMT4Q/VT4nc+Y8B3Pvo+18s/reafxH+KUb14zNl4D" +
                "UHdhK/DF1/TuP/yhZ/bo/xZ/J82cfz8+uX5+uX/tbfB8YvotlS" +
                "WD/Vzfs3dH/9G6f9X534215//9qav3hhyT+W1O9vj/1jm3H2j2" +
                "Glo5Ht/X+H+veQ1z89gP651d+zL235/2z/AVD/gZ/ESvpHzq3u" +
                "MKw7+aEPDJhP/4yPmUcchY605zF1EfViuBauF9afocP680iMx1" +
                "uqqN/XPP8wXPzXev3l/Qc11i8vVwo5fv51F/4U1L8tsJ2pfwd5" +
                "DsY/XxWgUT8Jkqr5O6jfi4v1b75RVEX/gqPzX23y55/l8+eYzZ" +
                "9DnD/fnfLnujJ/3nz/2WLLe2QDwrJIfyfaf1Gn/iGO8tvn5Vde" +
                "Px1j/8akxEvkWqTSihXcN9128Ix1gt0M44AjGska314XO12Mvk" +
                "LjEWQiAC6TkMiAWXTBHsxdcJVqMjFOyqV9qy5zABdPnLeYtHP9" +
                "QZZMAbMj8QId6hUFxrv/rilrSf6wlWE5syRrULwf7aadlaij6N" +
                "QiZfFGY0BxtqKgUFWVZ0K+tk4WLYLRoq4io08KOzLSo43imadW" +
                "1+4eqpnkdBU3urZ/bGpvQffPd+vQHSwTG69PtbftEeIXz/BHZN" +
                "ei03iBVl61/f1JZTHYRQKQRUpWGj9pF5NzwhzsTU6sYag/BZJV" +
                "3BsFSNX2IMqGKKps4W2ck/KRUSPD35r+W6TwgZUJQecvgvMYwm" +
                "udx55uigX3V15NlKgb3WksHEXcmxX1hoEDN2W2O3/tNn4g8lB/" +
                "h6PK85NJ/8eipP+D+i9moX+MROAb/+ucHyw4f9j3+UGs9NLSNn" +
                "Sm/ctkv7T+Yank/Oe51U7y/OD09efi+U93o16//3nhcf/zyIg8" +
                "N/nPMeIPxQ/E//qw1vf5uW5QNCicrNvzj0NGvXXWDxNdf8q2Lj" +
                "o/O5z+E5UTz+7dOgq61Iw56qH/sZ7fBfv5XVHr/K7b90f2qv3a" +
                "//d3Nzm/Oyv5jYTy5xfvkvOLi0jA0UXF+UVsqemZ/iudhiud9r" +
                "OYcgilOK5JsL5mRBQxi4ioBP8gi3/8rP9UurOj2m1JmB+b+k/q" +
                "bd6Biy6fT0SgS9TfTnHq+oNZqJFpJsgCPDrEv52+/6Onvkg5m/" +
                "0Lr+B/oIIy/ntrf26ps91T0L9z90P+7fFx3smMNH6CTX6hTob4" +
                "CTAO+3UMAVn+2fxXaNMZ/kHf/PMpeFcNmM5qWKfyzn6K2S3tMx" +
                "edPblV/OPt+9Mk7aWc6m8BAirbyLrpZsofXM58ClmIiFzsytq9" +
                "f+B/Bj+9DA==");
            
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
            final int compressedBytes = 1153;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTtu3DAQJRnaYAwXDOAiJRfxAXKECeDCZY6QozBBihQucp" +
                "QcIUfIkaK117K0S0n8f8R5xRprL0VyZjjvDSVzCXEAXfoDJwgE" +
                "oj8MK18eXwVRp98owtQkXcDzG/qDEM1vCIwNgY8tEJV5dA41vp" +
                "zA0EgxwWY2l3O61VPj89c3QxPw9WcXXE3jXg5yLLn1MeuX4NAJ" +
                "xkOPAxBLw5pnaS0v4zQ9IGdnIjL/AfIfAoGwgFwmagM9jCmnHK" +
                "cb8t+drf4/pNOs1paOUb/ckSsxZemZx9hwfWkgM+AX/Wusn8oD" +
                "0ASICus/9ZrmRIw4xShHIKqVfojy1hOOOwHeI5/kf22x/5c+g+" +
                "sQ/S0NFYvyl+n7F5Vl/V/B/ZsL/SMt59/BfQixFk96NKAKao/1" +
                "FwKBEsp5SeJarQTcJum78z+14n+YRwNjy6FBY9+YzcK2CAcT6i" +
                "ZVmX35mCrnMZfhwer6n35KbAZ3ffVneqdC9v4pEieiJCiaYJn1" +
                "uUdORtiLUr5by77q58dy+2cYv4hy8S9X6kduG/+ZbgzQwu2Rwx" +
                "E7839T//+D6wfjH9EvfyJ88jdEy98YP9ulsi0mm4shz5+3Dnlh" +
                "jQ1APv+pBub/aQgZMUTPd/KLM/1RkmuiKNwfh/4wXBYY/0opub" +
                "nl8E4xST6Qe6Iehh9XT9/07Z/a8peKpz/xcUzr/Ntz/umKv/rS" +
                "b4zMdlATJQSxyCBXm10mIBhFEIFcbC/qWjQ2JLmmmi+7LWjk7K" +
                "6LH09v4/6Do11Ypn4QXXMyt/2UTERCLucvXQyBBbavYP5bRuFe" +
                "DqRk4UlFPC7SYbHTvqSOykFEG/2zgv37LlWxFDbKuNyYTJMpXP" +
                "NXnPvHofknQnvhyN6SJHdAYP4tlf9FpPnzt/75ef80z6LMYr9w" +
                "/VE6fiLrH92c/qxTgNOlT9rkv5z8swf7F7VfqP/zCAgTfw4tf9" +
                "ufPwcz/XAYrwQvl2PT/PHexEQ6iKYQRTcQFuLHW3/an18Yyheq" +
                "aA2o0l7+zP7EZH9ysj8x6/+ikecSP57jX/a/W/8CSthPYYJKHo" +
                "XT9UOm/j+9ORLXLP4Inh/amP9W8sdK/rRqj6gbZfnfjf98+Nsu" +
                "f5Fi+au0/Zv3v4f+cTvFpu74qbr+Ca2fC6dGmziButzV2daBil" +
                "c/edUvda1ziLN+DfZT1vbjVek/NX8DvvMnzzt4Z/P/shQ/1+nn" +
                "LxxmYUC+/dfRDVgSdAJxHp/guE5zpMWzrusMzv2en5YRLCg6ah" +
                "eAf/PVbLJw/whrxSObGm1VlVK85bxYf4iX/M3/venHnzdvB6c/" +
                "HvWz2DRSkvGz2HsBafciLri792PJ8Fi2sjmMFfd+G+JPND7+Vk" +
                "Adnh/WZ0JOg8FXh/lHDdSjjlf9nC7nh/Bv3u8PtRI3O473CuZf" +
                "//ePJ9nsSCXURJchuhsuyK7Wg+6/JDt/pxH7IWqoOYvOPNn/r6" +
                "3C8Pyy9u6/3fzdxvmzUK39clePe/V/TDmqMf6dowCff8b8U5s0" +
                "kJNYa+37n8opugrKV71VTcBug1a7zv8/Ouehiw==");
            
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
            final int compressedBytes = 957;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtu2zAQpQiiIIqgUIEcgAZ6gBxhsssyR+hR2KKLLnuUHq" +
                "VHquVYsp2I1oe/IfleAMNRRA+HnM8baawI4Q0thJr/i1wefBzZ" +
                "D68vwpyPGCHPb7vhhU6/dD+FsOqzoGkgqWlEzvlPWrg+wylzHE" +
                "kn/fWt/jSccdL/9XT8x6D/r1F/e1T/Rv/N8sNCrT2rHzcP4zmN" +
                "d3xA5zpTBd7/RatSsD+Mj2j/meNf6/G3YP1H/qJ9+Evb/idvPq" +
                "C/zT/2bUk/yiHEr2r8NzvScOduV21RJlKqZ25/SWCcPiJs5mnZ" +
                "dDYOlBf/+jt7qRKMF3vG+4YqT/7BrH6wwWel8tqf2TMNfXWVrU" +
                "L+0U8j/7yrPx7d9Qfd1B+HQlbHRPcfz+tfxY/39f8o8r39J8X6" +
                "Ban/8+5/gPyX2X6z16+IH1yA+oNVhtb3ClVbjf6Kp3wDy/SwZs" +
                "NWfne98379A07+ch6v/l34y/X4l2G8ZuF5bv7Viej9I2H4n2f9" +
                "U3bkzLp/AAAA5cbPUP2Di6Q9W/7bJT8Rf7E0s2aHO+XN2/lmmN" +
                "XT7AlSJq5vafXBONU1zYlMKB/ID1p5nmQ3UxNSvXzFJu/5Ezyk" +
                "xfpvBX+Izz80y9SjtxK1uySGk2a2iPUvw7Xm+b2ovvsQ6785xT" +
                "eaPxwblpCWqMzy+cMUYj8Ac4BDwP6RPwHEHwAoJf9Q/Pyzv/8e" +
                "RL3BhGpa0F/CfmDuAMAfdO1ivnELl8bz0kGJDQIAIFHACdE/Gb" +
                "b+TNl/udQ/eT3+pP/5S6GJvv8hdxbcoWTnlA8AvMDw+vXH+PWd" +
                "R//5evkBnj8c++IE1St/BrZsLy18+juJEBA0fhl3/Oqn+KWwfi" +
                "XA5PK/3P7bunxe1oT9B+JlsZmDl+cXXffYmuGlYzd/7fa3Oe1k" +
                "X/3+3Qf6LxIC6wcAiJ/VM0bU/9X0/9fTbG1W759c2D+dcP/2Fz" +
                "+21fqr2c6W3PyypvqjWzFZPnx+il92yj+Pa/PPIef+x8u/efTX" +
                "67IPjUc22D9//lf3jYxG+bfis78E+y21/nl2xd9PyfwK9fMW4r" +
                "xIonFXxm1mMDUgNfL2vwFNY6yh7PFHWt2LY1rv6NtEDaR4Pb79" +
                "8iCoG+65fh0jpPpN9uEv1g/8B/Bha6lqIZM5rayXj1qupWj2rv" +
                "6U++rPmP1PKoo/AjyA578BlRSRRRPGvPNvN0YHuf+mLqdfQDPS" +
                "+nNvA/GaP2NXqBPkcrp+k1MWEufAE8iHf9Gs/z4n+/6yFtn/fy" +
                "VF25kIz8/oucVfE5P//wfVMak8");
            
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
                "eNrtnUuS2yAQhhGlTFFesfAByA1yhM4uyxxJSeUAOVqOlEjReG" +
                "QbZKARL/3fwjV2uYdHP5EQFoLHKISeX5Uw6ydGyPXPYX6h5c3w" +
                "U4hpvAi6CdK4SFjkr77ynx/kp4j2U3AVo7J9vrQv//VP331M7+" +
                "3vMAiLyJ4W1HY02/Gvb5a5udzL+I7feFgBr30/+dEiz7U/7vhz" +
                "tO/lhen8N8j/3KhS8UMn6H+g/xXV329//VGA/gAX7TQoytt4uF" +
                "tmo3T89GifCrefIX8AAABIisIUAAC8LqJg6AAAAEA+5sssE5JV" +
                "CiZMQTfzrwLWrxH3L3TIlwmqjVThqeXzzJ+8s39tSSzG4ia0CP" +
                "atf25KPYf9oP/Nz58J7AH5ZmuufPUkvP+pgxtHWdEEPS/JRkE2" +
                "+6fZ8xf7/758/mO2/1+XD8cnerH/bZUf/3z4z1b+2yyveO5TXb" +
                "45of0kgGzR0B4ahwaWPBIaBZz6I9H+173YSvXNgiMwqFa0yOAq" +
                "Pr0Pc3qKdnH790HC/GvaHG9D3XbYsszScq37X0ef+OLq/5Sv/6" +
                "qk/rD+YxqZrn7/O7t9rL/gfwCwGc46cGLOVHZ5ACqKFCe3381V" +
                "DV15V1FrAMApD/Iu+rn3Lw9bv6rn9evS/rop6/n+ZwIOCq6IiW" +
                "dCDjFej3UZQE3X42TBuWB/XRWooDtGy0YRunmf7e6kTPT8T8rz" +
                "I1d5nf78NHo9f4X8COdPlY1/efaflcifxLQ/qsj+susv//mTKl" +
                "D7OnH7qP/AmeunBPnbNJi/a6g/TOXBwtRtuZH6e2Oe3/2GuA4e" +
                "3cS06WrS8beVfk6VxPmYoOP6zfhJRuU/6/O/g6f8JPZ+P8EpH/" +
                "T8sU/78P+oxIQnXLuKHFv/kU/+8xX1b0z8NO74o2/rxzHc9x6Z" +
                "yFLK7F2Y/f99M//PLxXMX2z/RaL+52nfrkHdr1tg/3PXkLvorK" +
                "j+PV/+MQL3T4+tfVupP3z138PvX3L018Dvf7HbNx3bv7Wmq2z+" +
                "+fXTAflPVz3+Dkh3cenI+FVH/Om7fivgJ0NA5Eig/6PPX0f9D3" +
                "LEv2PWT2fPk5WPv/vzq47wH6pz/1aj9y/y5K+hYtuWTEHpuRzU" +
                "xTsMAGgLCsmfDT5/Ady1odseJtTf4OTrZ/A6WhTRn+fvr+zsf5" +
                "l2Qp99/xD2U7ZcyrSAbHxW5WFRIPb5WdO/0XjNH0D9Atpbf/wF" +
                "4UzIBQ==");
            
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
            final int compressedBytes = 770;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXVuO2zAMpA2hcPuljx5Ae4MegUfokdzFHrx1us0mrV0/JI" +
                "oUNfMRZBdgJFEUyREZh0geSfCzA1FcXqf7KInG97fD8sK3P4ZX" +
                "ojl8Ib4LchCdF9CG/VTF0PlGDlKaCufnEk2PDwAa+GXII7RgAy" +
                "M9bUZFhxGVvFVUMfhL+SO/549n5N/+yM8f8ogJQGG3cTR3YejK" +
                "UdjWG/ms/4x3/h1UxqeS4wvM/+vR+b/0bL8NxcqE/bdseR71j/" +
                "uLGvFjXT7asx/e18J0zHtxh1wlZQe14FY32+eHd+z/0w5/PiYv" +
                "uH/a9lPRIY8b74HKZpKgBoukJHS07EngMzj7YPTuGAAAADqB/P" +
                "1FVbDY/UMZ/n9pPcGC/pCk7trPlGs/qe31k/b6FfQ31Do/NvRv" +
                "/f5ajv8I3F/j/FSPP9ftt4X+9ySo/zb6/9vOX3D/AAAAAACrmB" +
                "WD5ZAzvtH+p276lwLxWv7KSwntpoTvt///2Pr+wH/z38EH/7OP" +
                "Z/ubqB/k2q+2/mZeGfPlL5/+L5VLy9y/Ie4BHfD//PnzmfwD8c" +
                "cY3OhvwF7WyH/K599sfPxm2Gk+7ZmV5YGdg7Dbv40GbwD82Ql/" +
                "tYfD7oW92i9QVfMO6v/u8tfD/Wct9C/pM1Vt+5G2f9swbCcf9j" +
                "/f7f9C/c53/EoEHFQTVNVZ/nNAnk/Ez7gdlAaRg5n7/D7wjz4c" +
                "G2A5f213/9F/r8vfcvvXHuVnp/snP0/p+MNi69c+vz3Yv+f4+6" +
                "z/RPj9kU6SmlJV1HH34+YtgmUEcfHP8WERvyf8ea0Q9rCWiYqo" +
                "czwnj0ese+QPuP8G/6b8wnvrhft253/0gDjtXQqux7fMv4Aa8Y" +
                "uhQgAQv38o5D+1SJI2OWubHCZCqcsp/5GLv0Lf/3dV/7sgb2r9" +
                "CvtfOX612/+A/le38a/Y7wekBu03UQO//4fzq35/g/p32+e/uK" +
                "tsP/6i/6g7+wUAAAD/BzKgUL/H8+/azl9tjG87/yx7YJTlAcAr" +
                "/4rdbxOocnuRX/X+SB0/AQ8m04E=");
            
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
            final int rows = 205;
            final int cols = 16;
            final int compressedBytes = 294;
            final int uncompressedBytes = 13121;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmlEKwjAMhv+WIlNEJniACh4kR/BIxSeP4xGd0wnDiUPdlq" +
                "z/N+jWh2xpkzZpWABKIKBAxJ0I/3h0t0bqjjsBKawgaJBQS4Qf" +
                "5X+l4/u7vt/fg/QhTiyvmcaHUnX5VJRYIDo5PF3T41g9btYQB1" +
                "9i20xGOEtaX15eV7ZdW8fERsXyU+8/XD8qce2uZL3/0v6K7e9b" +
                "ty7SuwBB7KMjf22nGgXNQsi4uwDXH/mT/8wv/5RJ9Z9g/L3z12" +
                "C8fqcw/5rd+A3aabTz20ztz/kjlvnOf5k4543Z4ljI1mQ24oeo" +
                "1V/p/Kmqv0fj8tbXTxxQ/9zHz/yPkHnD+JVd/sT4M5L/5jD+z6" +
                "dmuR+dHz+hpLpddtVTEosthPHTlv6g/oQMwxW+QER4");
            
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
            final int compressedBytes = 4277;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWwm8FVUd/ubMzJl75z4IeArJg2JTgseiQoYIgqaElRpkEc" +
                "ZmrIqGG7IJKgYhYmSIYKaIWqKoaSbKkmCILWolCZqRiFua+lIE" +
                "lVKm/1lm7sy9M/fN5d0X1vx+c2fmzMyZM+eb7/sv51yzGwxYZi" +
                "u4yLGP0QLVvC1q0BsdcDi6o4fZCz0t1+jr/BV92QT0x/FWjnV2" +
                "R2IQG86m4gmcalsYyjy+FqMxDuPNLpiM89iv+afsc803syfiEk" +
                "zDbFzG14Ahg6b2PrbY2mjsRyt8Gp81D2ErrHFGx8wwHGHNNWrN" +
                "D1HL9uIo9Mo86/7S+IgtQR8cy27DAJyEL2GwuZf9G183D2MXsf" +
                "UYjm9bRxjrMMoYmt1uHcIWsQ+sFpjC3sfFmG7+AJcaEwyHDTaf" +
                "RhUOYRv5Z3EoWuIwtGaXoI25CO3Q3uiEzuiCrmY1M61v4UizDs" +
                "fgC+xQ9GMr+XScgC/iZDaOfdt9AF/BVw2PDbTGYAiGZbtjBMaY" +
                "h2IsvmMuxu8xEZPwXVyAP+BCNgKzMMeZBxM2OHsNDrJskGkY/0" +
                "ITNDMPd3agOdqyw3JfMy10NNaiE9tsdclMxOfQDUfj89ZZ7ETj" +
                "fjaA7WfvsWk4zjgBA3GitRen8PX4Mk7D6TgD38Q3nPn4Fs7ESJ" +
                "xlnIkJOBvn4Fw+A+fjIkw1u2IGZvINnmdPZefYJznv2tPxPfYx" +
                "m8mPyW6wvsu/gMs9udgnK3zZLDaJPy5LXNY5d7fnseGeZ6zxvO" +
                "xI2vcyq73Ikv15djThe5J/zNfo8lvZDXZf3scvdwbznfYMecVg" +
                "qvsQKtkkjjLP5Z7xPPPufI24go4JX3nXIGeBbt8b1IqhnmfRnZ" +
                "kOVgtqy/t0LeFL5YQv3fMXL2YxF4lfo5N/bJ0nS+vEL5tIe6P4" +
                "HHWGjfNiF4yxr5X3LDYeDJfzR8Sve6a++3X9PMM/786X5efmhp" +
                "rX6j5YxFdkHg/u38GGmGfzG9h+uoraYJwQ9NU/w88hfOeqPWNy" +
                "cO8M3StfxJV0RPhiPrub3Ynvs7uoto9je+IXVENfdwH/rewHON" +
                "10y4fr9r4kcftAP3W//L3GfcidGXnrNf4e20L17Q/e9m3BX3lP" +
                "U6NW4Mv2eomL28d5JlpirNPP7RrUL/GV5ybI9nvJ+AZ9VZd5kr" +
                "8Yxpf4O1+3/Dd8o97rT+8/JlTHofJL/FK0ZjYi8pyehfg6OxS+" +
                "VTXmfdRvNbS/OXwH8Xe5vvJRdm+Jvhgf9MGsQnyJv/Oo7o+oDo" +
                "//DhYWKH1mL6Ka7cJV+AGuJX1egh5spz0b1xh9c4+wSdRvP8Tx" +
                "+usdpLen0nq1s41+R8ualT6/4K5zL5PnSZ8l/h+C6fffIfAV+k" +
                "xP301t2inwxRGZF41apztdOUfos2ytfBaOpZX0Wb7TSYq/xYvg" +
                "r7x6isI3M9jH16Iehn+2pWzBSwpftKd1kdLnzFM+f0mfBX+f5w" +
                "uFPgdslfos9xZiWBhfczGVLRb6HOL2nNwdcsvNY/P4oonCF80l" +
                "vm3M+yDePNAQEHPweb6LPR98K6/gOCobGFwh9Zm2Up9pq/Q5hG" +
                "+gzz+C5Jf9N2F/rZq8/bVao4bf5dvf7BB7F/XUPfweqw3V1z8W" +
                "36HZ8e7rwv7KEsLX83Lbcg/7+Ab29zo0pfcjDeWrFb76+5P40p" +
                "b4i1ra1/jqJwT4kv19TdhfXU72V26XCn22cz6+tF6P6e59uJRq" +
                "JH22q4T9leek/c3zN29/A7ZJfLMr0M98mV9diK/swZeF/Q1Kpf" +
                "2lrba/tHchrWR/6deU+PYT9tfHF83kXWR/s7dQWRG+yv5m2gRf" +
                "bVvf/tLZU3x8lf2Vd4TsLx0F9pf0eZnG91XCd7k72pqGG/m5aG" +
                "FNxw0KXz4e3a2Lran2Ln4e+VcLZX39+ThOX7c7ltazgnZR/+Zm" +
                "0zawVNYluWdza7m2CwpfuSfwbSnwjWhsR72t5ZMEvkkLBrtktw" +
                "vx1V/u2fL3HH2O9Fn4V7JsLKp0KfENPy6os3NETet0aT+rFV+Y" +
                "0IohoX3CN3QU4CuPpGEwByp89RXN9LatO07gW1T30aT3GzPtgn" +
                "7Ueuvjq4/OKLBRs/L4Btf8RG9vxgpYtCX+YiVuQXX2eNyUHSD1" +
                "+Vb0oDO3UQ3aAuH2eH0mfP+i9Fnx1z6mqlNufVifBX+1VSHPJz" +
                "swz9/Mo2H+ynsS+Cv0Oa7Hff/K569vfxV/4/VZHgn+Lirmb+40" +
                "uktgFcPfzKa8PofaWKTPesvNQaHSJnpL+pwjleVGnD4Tf9uH9V" +
                "nwN06fw/wNPUPyl7Y/VfzlbxN/73BH4y78nO9Bi+xGuw/FR3ei" +
                "A13THXeznegp/Cteh1/gXpCHwT+k+Ggs7sut4h/w9wP+vhPmL3" +
                "uhanZVy+CZmr/4GZryfc5DVEefeP6K+EjzsZg3q4i/zyTx19fn" +
                "Yv4KfQ7xd3Ue35L8nR3P3sxmn78Yk4q/pyXzl78Tx1/Ct2Me31" +
                "L8xT1F/A28E9yv+1Hga+XuEfwV+KIav0RN9k3f/tqX40GFr32Z" +
                "sr/O0xJf+i4Fvsr+el7VEVH7SyUziuxvRuArn5pgf53uvv3lsf" +
                "bX3VOML0ZF/atCfK2eUfuLB4KahX8VY38FvnareP66zQvtr9wm" +
                "298z8viG7a/CN97+hrzGtsq/qsf+SnyxRuAb8Pch6PhF4Ev8Df" +
                "wrKqnJ1Qb4UpxpTzD66mu1fyXwrTol71/Zw4T/HMVX36HxpT2J" +
                "r46CB4bxtScpfG2KLEr7V7kjY/yrUREGCP/q4pL4tk6DL58aj6" +
                "+wv2nxxcOE77B4/0rg67wQh29ET1qkwneHn98I6fOyvD7zpXwZ" +
                "6fNayd91uW7E3wGE73qhz2azqD7b5HUw0jSQb+C2zOuz3CZlAi" +
                "S+vj4rfOP1WeGbrM9yG6vPBdcW4BvV5zT+FV+SWHd5+jwiXp/l" +
                "O8bqcxTfA9bnDVrh22ELfuXY3MZm1pu7eAy/waPsKGziDn5Lba" +
                "Bvm/Vh/Rn1O5OxOuvHOjPiAGvLurAjaUtX8O7sOHmuhn1GtryW" +
                "tWfEO9Yh1Hbpo7EexT3GmxSXsWO81IsxNA7fsH+VeGengv6sC9" +
                "q0xKvIYk4pkaG4sqDVjxT1TDfhX9X7/pNDdfxabx8P218nI/TZ" +
                "bS30mZ0fzj/zrujpbGVnEH/7silKnyV/pV/o219+pNJnjFf6zG" +
                "uT9Fnxl45k/jlZn33+YquvzyL/HMffZH3WNixGnyFi+XZx+izy" +
                "zz5/S+tzmL8J+myTPl8cb38Vf8P67OefC/FN1mc//8wu8PXZ/X" +
                "SgzzN8fWYL2VVsPvH3ebcNd9lcPIvnGH1Z2M7msSu4fHd2Na8L" +
                "cUtrGlug9JlqGVjw3cVEsdgmz+xLx1/eINb4/PXzV0kLezuJv0" +
                "7HMp8YyZtiR1DjzOR7CvUZf47jbz3PHRnmL+Grs+34a5i/+Fve" +
                "f2bkP7M1ef7KfnhQ4Sv4617q8zfvP/MhUf8qnr8ivxHmbxBzdF" +
                "H8leUl/ed0/PXtbwJ/X0jjXzlHNdy/kv7z0rT8jfOvSvPX96/M" +
                "2Xn7G/B3Zyj+tfBiKD7ahRo6H8V3XR7fXL8YfL+eBt9CfS6Ojx" +
                "oDX9MrwPelVPj2qQy+Vr/Gx1fHRy9H8H3FxzdDsQhfJvf3ZLT/" +
                "xrZFVOwPbDuvY08zmYOvakX+1RNU+qSf30jQvq2xmhSjz5neDd" +
                "PnQv8Kr5b2r/gf/3v+lTUwvT7H9OG08Phg/f5VyH9+TW/fpPUt" +
                "5yW8jr/T3j9ofRvvsudQh3+G8aVfja8sUfg+X7JtRfjijYT2he" +
                "KjA12K81fBmaqill2S6O1WHF/7/obczaal+r5nxdhl/e1YRwt9" +
                "ztwi88/vSfv7D9RkVvr6rJ/zFn8lnN8oHD+Se/XmN9T4Qoz9vb" +
                "3Y/nqNZ393p9LnVyujz/YDyfpMe/XoM5uTRp+tXr79DeU39mh9" +
                "Pgwf4l/4IDsR/8Y+rlltuqEe+0jwl80q5m89397W1N/4DK/CS1" +
                "r/OZm/9fvP2Jvq3dY0iL9zUtmAXjGt05YTH9NqObbgr5PL3iny" +
                "k7L8WhxOv2L8aD+9t4xU+VPwksaP/PH9OP7qPRZ5foi/Ij7y+V" +
                "t6/CgFslPi8I2OH0X4uyiOv0qfE/hb1viRvT5+/Ciev2L8KDKv" +
                "pEny+H6ev+yC4vEjA4q/2bVUy5edjJifE9H0ACP0zD7s558bZ8" +
                "muS4XcFeXyNz9/o3z+1m9/MSYVfzc1iL/3lmzB3BLndG7MmOJ8" +
                "ld1pXFCIbwHajYpvJfyrSuNbbn4jEd/HGg/fUv6VcX6oN7R/Jf" +
                "fF+NFV2X1Cn42LVPxrXGjoTHbi/KvhUX2Oxr/16XPYv/qk6LPV" +
                "qkL6/LuG6LMf/5bW50hLfP9Kz79i7dxV5krzZkJxJgviUHaUPz" +
                "9W5J/rH19wziwcXzBXFI4v6Jp7pIt/i8cXytfnRvav0unzEw3h" +
                "L+/WUH12N0LP3DTEDKoWtL1U5a/E/A01fpR92LxV39U/tq6hjo" +
                "pUxsXnn/3xwXB8lGR/S8+/iouPkvANx0e6tIz5V8n4ljf/yv5T" +
                "8vhgTN1Hx+Nbev4VlQTxUVASzL8y5sJyX87PvzJX4SZztT//iv" +
                "h7Wz6/4c+/Yk9gkIqPtD6PKNZnFR+l02fz9v9bfd56kPRZz78y" +
                "rsz0gmVuEfga38t9Q8VH5h+l/Z2n7K/C13yyBL4jG4Rv70+e/W" +
                "UrK4TvW+nwNTdWFl9jvsIXN5vPEL7bJb4LJH//bHzffE7gyyS+" +
                "xlW577CrtYYvDPtXbEGA76iG+FdCnz9p+Ap9rgi+7xxc/or5V4" +
                "aYoS3jI8j5IHn7a/xQ+FeEJNldf/6kGP9lrcX8HNYlsL+jlf01" +
                "X4/aX+VfhefnxPlXkfbVJvpXqzA4yxpsf1POz8l49dnfdPNz7N" +
                "2NZ3/9+Tmx9lfnvVk7kX82lsr5db1F/tlYjhryn0Pjg3n/WeSf" +
                "lf9M+iz9Z5zKuhK+Z7HjRP5Z+M+Kv+Ybyn8uHB/08Q3zVx4X5J" +
                "/z+Ibzz9nqhuefjevS5J+zzSqTf+atG5J/Tjc+qPA1rg/nn41l" +
                "fnwk/x+6J4+v+W4UX+MGha/5Xgl8xxbhuzsG3+saim/s+MLS8v" +
                "At0Oek8YWOFcK33X8L34LxhYd8fOV77ZH7Mg413xXxb/CErsaN" +
                "Cl8rUyL+HVcU/+5OH//GZm7KmF93cOLfdEsm03jxb8mcVhAHqv" +
                "hX2F/x/yPjZmF/BX9zq337y88T+Cr7K/5/pOyv1dTHl/g7XuCr" +
                "+OvbX3Ysn1yu/c3//6hc+xv3/yN9ZuyB2N/s9ZWxv5nmB8n+bg" +
                "iQvk1vb+WC348ZtxsrBX+rVvnzYwW+wRM1vhIDia/82idY1VH+" +
                "yt/J6ebHhvEtzd9sdeKdRfgGZ8YeCH+zyyvE39PLUJ3Y+bFlKl" +
                "fB/FhjFbYYP7XaW+2MnxVp5DyFr5g/GauhCwI1m1jQrrj5kxsr" +
                "MNLEytfncpY8vpnKwOtllxwcfI07Av95C34lz2wW9tef327cS/" +
                "up57c7kwrtr9Uh/fz2xrW/B4avyF9VYnGXH1z+suC/psYGf3zB" +
                "WC/0GduVf5UK37OL8O1UhO+2/yV8KzX/2b2xjDoOcP5z5Lhg/r" +
                "PVQ8a/m2R8dJqcf/WVmPmxzf3xBZ2f5NH5V845aec/x+Wv4uKj" +
                "uPzVgcVHnncg8VH2qcrER7nBByk+2hn+/6D6f7fIX6Ha2Iwaa0" +
                "Akv5EfX+gfm38e6kwuxjeffw7Hv/8r+FYq/qX14OCr5z/jP66O" +
                "k7s=");
            
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
            final int compressedBytes = 2028;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXGlwFEUUft3Ts5PsJkASQTlCBSRSXEEO8eAyYlEIBAkill" +
                "qKB+IBCZeAcoTiCFrEKkXBiKCSIKRK8OKff9Cy9IdySaKAWKCF" +
                "URQRtEotr6r4pqend2Z3ZjfZbGZ3Yraqp2d6juz0N9/73nvzNk" +
                "1NkR/ysXUrMFBfspuaYn60+fbtwKCmlH7gicTOUy7KO7qyKQ0+" +
                "gcGJnkk+MnpagLNxgY98QkfCeew/JYfoMLgIl0x86XV0LEXE6F" +
                "B+xhjan/bAPp8OoFdjj0doZXQ039eT9jbwZaX0etzua5v1H3Gk" +
                "qHnfj45yGs087RW+mV/5HN+Dom8gh0k9OUaOks/YPHMvm2/lLz" +
                "1Cj+PyGD0sZx/Ppocs/C2P5i+tT/79Bgu8wjfY21sk2WNJxveI" +
                "xGoTXKAbyRfkc5yX82wFOQ6/0vU6f2klXWvYZ1rlyLGnJb4Lmm" +
                "Ofdf522Gdv+UsL2AYxcoKOlMgNs/E3vn1eHGWf10fbZ35Eq+xz" +
                "cKFn/C33Ob4nhV3YAgRYoBqCECJfQy7k4ez0xNYXCmEIFAn/aq" +
                "uYt7EwTqxNFH0JthnaElzOhjl8ZB6UhfkLy2A5rIY1uEaxdZLj" +
                "l8MVNkSuEj2eCcNghGUPPicwHm7G5SS+fZsYvxPu5v1W23UWmP" +
                "jCKjmWBZfxvit0g+6WY/tg6w8DYKAVX7gW25jAC7gshgny2Ckw" +
                "VayVwh1y9EF4gPcPwyNQDotwbTG2lVCBSwVbAJsGmeLobOjC+x" +
                "zIFyNoJaCfvNpgGA7X2PEF5A3cCOjfwi3YJsM0uBVmwiy4nZ9x" +
                "F9wD92M/Fx7FJaoqLIQlsBT732CFwV8gbBvbTr4njehf5bJXyF" +
                "noif4VMg+GRPIXxjrxF/FdqvMX5lj4+3KYvwa+fK1TPP7CIHf+" +
                "OuPrxl8rvqLvis/1txFH9nfiL4wJnnK9dqllXeArtiS+fEsRox" +
                "JfXO8i+nyXaw935q+Jr9iaGXWexFfy9zvRo8cMfxNURvgnSvNf" +
                "1fGFf9301+JfLWvL+Iju8l5/aU3cv/F7WtvnH0z9BUL4XZGfdP" +
                "7yb27jL/m5GfxdHsXf13zO3z98zt8LkqW1wFidrr+shu2GPPY2" +
                "e4Ptgs1QyPbo+ksusbfYO+KsXwz9ZTtt+lul6fM5W4wI/WX7sO" +
                "3V9VeMU9s3ctFfvu6iv81i7gIn/hr6i30326iuv8846S/6V3+6" +
                "6u+msP7Kvc/q+mvZrhB9wHZUtuhz5EiE/mJz1F95BNdf7CP01/" +
                "I3hP4SEPqbSYM0RDWKTxbNcrAQRnyUE2ExAxH2eUV65a9odmvt" +
                "s3Kvv/1nORMldDLvp2ObxtemJIDvSm/wDVGv8A1BO8G3gO3n/Q" +
                "fYEo9/V0XFv++2RfzrHX/9nt+Q6tso7qyTbfSMvOMu9vyk0tkp" +
                "P+k4x/Wpm5ck8Dcr5qx94819KLmtvkKByudC6WMdVfNss8XxVX" +
                "Pc889u+Kqd/YpvevBXabNvQcpM+wxDDXwRra5u+Gqr215/Ya2X" +
                "8W+oW9y/cV+q7TOsi7FvveRqD2C4jfGRovvieQZ/9fgIt4rC74" +
                "90/pJNRnxED8JEA18RH1VEx0cGf30bH53yeXy0MZyfNPHV8xuQ" +
                "p0wy8hvh/LM1vwHjDP8K8eX+FZTw/MYaOlrPP+v+lYGv2svwr6" +
                "z5Z9gSzm/Eyz+H/avU5J9Dxf7OP5NqA1+1nxrh0SoYL6kjovU3" +
                "1vvfdPOvkmCfJ6SD/mpVCSu3yHapU4nI7iqzHBWAP9vqtDjfoz" +
                "q+/sL7fsI3Ff4VHIjWXxjdwmt8KPynOjP+VUvD9tnUX7TPVv2V" +
                "9ll5yNE+V2k1un024l9hn6eH7bNVf+PZZ1N/neyzl/qr4+tn/V" +
                "XmCvu8Tl2rcl9LWRWDvxvi8Hd/euUnk2CffZ6/Mt/vkwa1ktQr" +
                "a/T6K6f4V3svkfiXnklt/BsbX3rWL/EvbUxYfyskvi+SenUrOa" +
                "oIjmY4RFUq6ivrlfr7DZX/v/jL8luLL+pvrV4fm3FGqTTqY5WN" +
                "4fpY9jhb2pz8My5t+We2TK1pi/rY0GLnozNOt1N8n0zYPh+U3H" +
                "wdG8aQylMZrnZLrW7y1ae91E9q2Qnje9LkLxD1TXUvX+f1G0q1" +
                "tX6Dv1+w5DcyuzvVbxj8Dddv4BOxry3qN0JVra3fUF6KONKlfs" +
                "Odv17Wb5jvFxKvv8JRYjlO1Ofo9ZMGvpklyg5ES9Rv6PU5Dt9q" +
                "hujnWP0ry34LvnGZF8PzTkZ9jo6v1b9yw9edv17ia/pXidfnUM" +
                "svApze//KtFuuvOko9EKm/bLM1/k1Mf9PXf2bPpZf/zJ6X/tVR" +
                "dTsisiMevpmLmo8vXr9N6ttD+5t5djup30jcf7Y97SJ/FSw081" +
                "fYb4ZC9QQINMz6WD1/Jfbb6uv42mwxMg/KLNf26fuj9Mhfadmt" +
                "zV+RBlZL6lmdHv+qJ9luZPKXev0kf3r2iKdI1k/K52pn7FlWR5" +
                "n1kx35jdTmNyy5MAj+5ept3+DH+IjM7YiPUH95pXtgRDL9K2Tw" +
                "OX/X16WH/iZFw2ux1QVQT9WLun026ttbZ5/5MSm0z+2l/ip5n8" +
                "DUGLPF3y8EilL/fj/U2MHfBPGdbtsaGo1vOtRvhM514NtCXIXm" +
                "ajc6/35B4RmX8O8XtGK+HUjvu2o39bG5SUB4PF7nuCbzbEoDNu" +
                "590Uo5X3H+P4NXn6zcDv628EkX+UltW3L950Bxx+9T0iJS/A9D" +
                "pVrr");
            
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
            final int compressedBytes = 862;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm0toE0EYx4fJ5suaKFIUFEWpCkoV1GARxRe2imht66lCi0" +
                "q9KEhBrWIRn7SR+sIqItQHqIgefVDUgxfPWrGtpnrxoicRwYvg" +
                "aZ1MNttMsk130m0zs/kGdme+2SFk55f/fx7ZhRqLp8hjy6INqR" +
                "Kts/ISrciJwSpJmlrjrR2dVtznh35nSjFiBSJBLWP717LCd7Xg" +
                "uw35yqfIP9iS4pvXSwmndNm1F7tVvSPk6+h3azo3b2qh3wPIV5" +
                "IvczxiML4vScwLX7JR5Evqc9uSQ6RNiE/YORVqZ5HZQrw4qxwn" +
                "q0Yis4/VbLL5tnm7K/NVznea6daKLODnKrLUnS/ZTGqdtnVkp1" +
                "Pe7fJZB4XojJ0LOiD2r46M9OYiOVpkBzsa2dFkx83saBVaHCbt" +
                "5DjrgdfkJCdVD9t5vksL/R5B/UrqN0t/E8vX/O4D3w6Pv/OOcu" +
                "dr/hDjKV0C9QaB5geaZOcB2u/UvGPH+zE0NIjzKwX025jOo9+0" +
                "8OcLyLe4FP2Z4QtNCvO9hXylFcxmg6Fk1j0OseOL/+tf4/rk3d" +
                "P4x1/ZZPSoRdW44fBtTvGN/ZlovnqNv0FK4voXWtRd/3qePY57" +
                "/cuvBGD9W9C59xTW7+Svj9T1Z1XXR7QSWo1e2Ad7Wbma+fY9Xh" +
                "sX1LsG9tNlLF/Bo/V0CZ3D8nm0iq5kOf/103X8PJfOt/3/Nl3L" +
                "4oV5TrDco8OuLkWvBNOfR9XvUT/1q0MKHl9amVVm+oVjbvqlG2" +
                "T1C+2oX+VYVzuleM4Vztcmd8oLX1ZGvgrq140vnJbXL/JVlS+c" +
                "9cWfzyFfhann+bMQcb4s53xtnp25+uUx8tWeL3ShP+vPFxKj8c" +
                "XxV+k1brfs/BnnVwGgflF2/LWv4Pir4fooo1+4gvoNiH6von7L" +
                "gPI1p7dx/1lbf+b/H/Xg/0flO/4WtX91B/lq5NT30Z+19edH8H" +
                "As/cITaf0+QP0qwvcpvIDnvvvzM+SrHOv083V9LlcUef4Z+RYx" +
                "tr7NrwvPEGjy91PCFfLvp4SnI9+S+/NXGIAhGEz5MwzDR/jsiz" +
                "9/Qn/2XYlJyfZv+PkX603X93pogp4vtL+h7vPtqF+5FAnh+kjf" +
                "/Q2jN2Jk5s/u+1fy/oz7V8oQvpTtz5Eor+tEfw5GIv8Bp2aIGQ" +
                "==");
            
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
            final int compressedBytes = 498;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmstKw0AUhuXYZNYiIlgr0Y1owVooomifQLxfcCXqM7hx6b" +
                "orFRUVBW8I3gv6FGpB8fII7nyFcUxLSELRCg3NnPwHmskMyaLn" +
                "4zs9mZQs6QRlnLO0dAX1U5aSakzZsyHqpBY1JqiLetXYba8O2s" +
                "c4tRXvEY00oOYd0hfUIysK6pM1iPovySzIEk1B8DU+wVefEM3R" +
                "+J4c/Q2oPsfhb+itbXWynYO/2v7+JoLwN5aHv6zrc7uOfPlFYH" +
                "yT4Muabwp8Q0w97ZtnPTObrxptviWeGS/f0pXgW/sueURKY/P3" +
                "a4xt5Enb/nk0iPpsHKI+h8TfMfgbKaPvFPNx39o98qJtfZ4V02" +
                "JKTIoZypgfYsJ8q0Z9Nl9Rn0Nar+fKZBv+6kpzwUXR7rRouAzf" +
                "Bt/cROYivr+xjPocYqtXPLku0Ls6PtOTs/KgPo9/EHpBHrXhvY" +
                "ocMKa79uOvx80K/EVow3cdfJl3YWX4ig3kRVtjt9BfRcjeQtFf" +
                "sYf6zMrifYdnDtnQ1E3LXIztiANzvri/EbOZ+vc3zKV//79uF/" +
                "sboXD0yJVT7E/y43sMvqz5noAva76n4Mua7xn4suZ7Dr6s+V6A" +
                "L+/nX9d5Nd/vX+L5NxT+XsFf1nyvwTeC1G88NPH+SF+St/CXNd" +
                "88+HKOum8NuPkU");
            
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
            final int compressedBytes = 527;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmzlLA0EYhmWymRkQCzsPFLERFTwgiKLiXYkhxqRVRAtF7A" +
                "VBC1GDqUUUxB/hUaiVBzbeV/wRsdBe1mFD1lzGBDcwM7xfsTOz" +
                "TA6+h/edb2ZZtm/aQbzWddhMC1KaMqYmQolgB+CrNd9D8NWa7x" +
                "H4ItgxcqANy5MM+o0iL2oGqWFnLMDGmJ8FiYdG2Ch9EXdbk+a0" +
                "k27SKNpma9RF6kiFaKtIPWkRbYN1t9O6VpLq2GfoM+kQ49q032" +
                "vK8X+1gY1DfM/pA32ij6Iv+NJ78JU16Gue808tvhd2Tj127998" +
                "3T7wlUO/CX0H+bJL8JXEn6+gX635XqO+0ng3dJPLLLcXmcL6m7" +
                "T+3kK/kvjzHfZHOu+PCqTfKPhq7c/v4Ks13y/w1ZkvN8BXa77F" +
                "4CsHX15WCL7GCvjKHrzcznYY2VBWvz04n9RYo725zML5JOqrlP" +
                "qqD/qVxJ8HBY2BGF8a4f3wZ2m9dii/+fHzSe53Xr+uPfDV2p8D" +
                "4CuJ6oOiftr8o77aQp6UXX8n+QSfyq5fPp23fsehXyn4jvBZq/" +
                "Xp9H4KKVHORWcK+e2uhCfHfE6M36wsrdr5ynh+RULQh9Ir83x2" +
                "vgjF+S6AL/ZHv9RXi6iv5OHLl374uj4cOd/4BF/p/XkZ/qzB/j" +
                "dkbPP1uH6NXYf8eQ36VUjJG8iBuuuv0G/Yaf0aO9CvDFH0DaeP" +
                "WOs=");
            
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
            final int compressedBytes = 580;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm8srBVEcx3Wce2ZGHlmwIMJGKI9IxF3aEHmuSPEP+COkFL" +
                "JClBKxwOJea3vl/ZYs/AEeK8kr4zhdo7lX3Fszdc7x/dXMecy5" +
                "r99nvr/fb+bea07Y3GihOW7HGBlxemP2D0ZGbZgWZk7CB1rznY" +
                "YP1DRSwOnNsbDo1zizVa41dSRIynhbIUaNpJjk8DaPlJBK3paK" +
                "2QaxzyX5kTNiltTzcVHM65XH+b5qwcYjbYZsOzD1+5rADPykqn" +
                "7NLbbhvX5ZCPqVgm+ruSPadr61iV7LD6syo8ZM8k+VplwU3fYv" +
                "/zp9L/PvAfSr0Nl1CR+oWz/TWfPqS7903hv90jnoVyK+1+Crbf" +
                "18Y956n3/pMPhKwvfel/rqDnxlic++8H0AX1n4mo/ffJF/od+4" +
                "9PsEvlrzfQZfia6PXhCfta2fX/3QLw2DrxxGF3k0fYv0l8V+lS" +
                "6JdiUSa99pKOoxC3E87zrf1uBfGc1KjlJT0DUS+uWt0K/riKPf" +
                "yBj6lZNvAHy1zspVifK1GPj+p+sjy0B9pTXfFPBVKB+nwgcq39" +
                "+w0nB/A/E5oficDr6y8LUyvvlCv/8y/2bBB6rqlw3w/JvN+n/T" +
                "LxuEfpF/Xfk3H3zl4GsV+MEXv6/TXL9F4Ct9VdXkeHsM3lCWYn" +
                "PsXKDapaV9cs73R2TPmdnh2+4fCjyGbzWOz+2Iz5LUV71Gt9Fl" +
                "dBo9pIZdGB3szJP//56CryR8+9gRO2HHn/plF+wQfGU1dp7g+k" +
                "3n+4Whr/iM+5N6WdIH38D0aA==");
            
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
            final int compressedBytes = 587;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmklLHEEUx01ZXd1VogjBixKRHNxiXEBEUS+KH8DtqqDixa" +
                "MQA16CqERCvIkoCC6oEBfUs19AFFRcRi96U2+JHjwJY9FxZMZl" +
                "xsFSqor/g+6qmq7pad5v/u/V626nj3cHY5rTH4RZbPwbfGCq0W" +
                "m5zfsUe+is3H+nf+iMf2TubsYyXXnwnckXnHdRbgvwrxb6/BEj" +
                "Po/CR2YayQrrl973SiLmlJNq8kW2Rf6oiuSQdNl+InmkWLb5/q" +
                "eV/j6DZN79Y/pIhRx/fvR7hS+8rjKwUcOXD74J3wHw1SQ2D8n4" +
                "O4L4bG985r/V69dZhH6tzr/D4KtJ/l1wm9xGt8FtJqUs4NazAx" +
                "V82T74asJ3KRj0av/rlwW8GvDV1by6+Oaz9VAv8TDsLF1yfOT7" +
                "eODe27+eZPBTmzXiBug/n3/pGN8M5V86oSb/0nHoVxe+3rVUwF" +
                "Y0vqwNfA2vgrejHt2Bh1AfRdRHu9CvRvl3D/kX+o2Hr9f7nnx5" +
                "ACSj6vcY+rV6dXXypI+NqX9hr+R/Ch9YzfcMPjA1//Lz2OsrNh" +
                "/3/ecp5F+r698L8NWHL/+nnO9f8NWI75Vyvpfga44Jx8Sr/tAJ" +
                "co/zr3BV6Fcw6FcPviIpnK/gauKzEOCrn35FikjG+zk2x2eRCv" +
                "3aYvzGj88f2RpbVb1+Zivgq0n+TfNavfbofL2OuJ8PtoCvQfVR" +
                "NnxgrH5zI+sjNfHZOYd+9ax/Fa2vCsBXF750THwN8cX7G9bF58" +
                "K30C9dBV+r43Mx+FrNtwR8dbCEW2fz57g=");
            
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
            final int compressedBytes = 484;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmr0vBFEUxeVm9pm9hT+AEDSCxEciG4slkRCFaBRUGlqlSi" +
                "0RCg2RlRB/AmqdEAnF+lg0gkqLoB1jZIeRycTG2817b84t9s1s" +
                "Zpvzyzn3vjdL9Y5f1OVfdTo/iropQ63u2u7d9VETVbtrLTVTh7" +
                "u2eN/2ep81VPf1G05R2r1vdH4VtTl/Kko5qBIXp321l6GGnkWl" +
                "8m8P/Gs03wz4Gs23H3w16sQD0AD+Dfh3CP5V3rXDmJ9jQnoEGi" +
                "CfA/k8inxWgy+PlYJv4hF8FcnecZfGWvQziXXoZEo+c6WUfBbw" +
                "ryL5PCH2xK7sfBY74KtRhk9CA33z2crybMG/1qYc/1ob8K/R+6" +
                "M58FWk/86LM3Ehzj/5imuRE3kp/fcSfGWXuCry+X2/vy6Farzg" +
                "X4WeT9IiNDdkvsL5M/pvsP+uIJ+N5rsKvsqn8lZ0/0VpzncbfM" +
                "3LZ5w/m8uXq+xB7H9jmNQH0EBf//KhPR09P9szxfrXnoJ/1cln" +
                "PpK9PwJfdfhaWT4u8MX7oxh13RPsjwzx7yn8azTfHPjGMJ/zyG" +
                "ezzjckzs+v8K8qfO33b76y8plvwFedshr4NkRj/H/DkHxOPsnO" +
                "Z74rp3/5HiQj+b7I5pt8LivfB5D81yT9Bg30rIoPwFJ//A==");
            
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
            final int compressedBytes = 421;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmz1Lw1AUhuV4m5Rc0MlFUYqLqPhRrJ/V0U3Fz1VdHMRBwU" +
                "VXC3XQRZ0q+K8Uf4Wzg0MMkQYjFIokcHJ5ztDblHY5D8957w2N" +
                "VMKkpJa8q4a/SpZkTSajdSa+WpUxGYzWYRmX2WidiD+tx69DMv" +
                "Lzm+BTlqPr0fBPyXTYVclCSGVQUgm+8uBb/oCvDr45+RvC12W+" +
                "the+2suapNt3dAN/U/4G+Os0Xwtf9fO5j/mMvx387cdfp/kOwF" +
                "cLX9OyU22+5iUbvuYZvurzt0r+Ft9fO+fv+3v+rn8gNe/d3/He" +
                "svDXe8VfJXxreeRvaRu+Tu+v5uGrImMXu/lWaYtOFXY+r5C/Dv" +
                "tbx1/y9x/5u46/6t3e4PzrNN9N+Bae4SH56/j++Yj9s8P+HuMv" +
                "ZU/oAeej1PnolPms3toz9s/428Hfc/x1mu8FfNXP50vmswPn3y" +
                "vTKt+2/c3q/1flJv4WyORrekD+pvL3Bn+VzOdGHnxNA746/Q38" +
                "TJ7/9eBboPxt0gPyN5W/9/jrNN8H+DrN9xG+Wvialn3K+v4Gz5" +
                "fpqJ5v7PxU+A==");
            
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
            final int compressedBytes = 392;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmkFKw1AQhmV8hcDL0o0VpbgpKtZWRBTrFTxCXeht3ApSsQ" +
                "reQLyBiBfwCC5ERFdVoSoxRFsSF2Kglcn4/Yu8l0A28/G/fyZE" +
                "Kn4/+pKsDHaNKCVZk01ZiNel5K4pVSnH67TMST1e55OnG8l1Sm" +
                "Y+33Fnsh7fz0bfJLXoV5LVCP2Z/AE1KKakktoP0b/+CP+a5tuB" +
                "rxa+ru2P+3xdZ0j5ewhfHXz9Cf2Vab6no+BbuoWv+q75fFDtPa" +
                "phmvQFNTDN95IaMB9l5qMr8tc032v4Kpp/b5h/TfO9gy/ncx6+" +
                "QRe+Beqf76kB/s30Vw/41zTfR/jq4Ou7wXaw8zPfYDd3/rbga9" +
                "q/T/A1zfcZvqb5vsDXNN8efE3zfYWvFr6u7d/6fPk+aW4+eh+F" +
                "f/n/qkgKhRqQv2n/huP4V0/+ho78tco3LJG/5v07gX//dX81SQ" +
                "1M8y1TA/rnTP+8yPmsKH9r5C/+zeXfOnxN823AV9H5vMz5jH9z" +
                "+bcJX0X+3cK/NjX2AaNFkAE=");
            
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
            final int rows = 20;
            final int cols = 120;
            final int compressedBytes = 204;
            final int uncompressedBytes = 9601;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrjCfhPBGD1+z8KhiTgiRiN31HAEzcaBkMTMCkgsU3gLEMUNR" +
                "ZMtkzaQFofzLNhUmeSAtKyTJpMBkBaCyxqDSalmeSgKSKDyRLI" +
                "V8KwT49Id5mNxs2gjt/M0fgd1vGbNRq/wzp+80bjd1jHb9Fo/A" +
                "6h9nP9aBiM5l+U/Ns8mn+Hdfy2jMbvsI7f1tH4Hdbx2zMav8M6" +
                "fvtH43dYx++E0fgd1vE7eTR+h3X8LhiN32EdvwtH43dYx+/i0f" +
                "gd1vG7bDR+BwNgAAAocNu1");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 23, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 38, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 131;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3ckNwCAMBED6L5qlAszLEoiZAvLBJ4mUpDIHsBf5g/iUPw" +
                "CX1b/T89Vf8QUAYL4EsN/ZL3H+APzWn7w/Fz/uN5y/80V8gPxV" +
                "HxBf+ifID8QPAOiv+isAYP4AAADzL4D6CAAAAPZ3+zsAAAAAAA" +
                "AAAAAAANS6/x/5/Pf9C0PYON0=");
            
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
            final int compressedBytes = 70;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt1DEBAAAIw7D5Fw2cfBggkdCjCQAAAAAAAAAAAAAAAAAAAA" +
                "AAAKw+lUAA/gsAAAAAAAAAAAAAAAAAAAAAAH8MzqI9wQ==");
            
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
            final int rows = 814;
            final int cols = 8;
            final int compressedBytes = 60;
            final int uncompressedBytes = 26049;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt1DENAAAIBLH3LxpwwEpCWgO3XQIAAABc61W97wP+BAAAAA" +
                "AAAAAAAAAAAAAAAAAAAABwZgA3Jlyh");
            
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
                "eNrt2ksOgjAARdEO/P9QBN0R6+rS3YCJMmzfyZ0SBu8EEhpqqT" +
                "9aNrWoj8of1/DmLd7iLd7iLd7iLd7iLd7izfuL97DunsvZrk17" +
                "z3aK8t7ZKcr7aKco74Odorwfdory3topynuyk+db3Xrf7BTlfb" +
                "GT83P5/hZvNef9slOU995OUd5PO0V5v+0U5X21U5L32vzP1Pjz" +
                "fbJTlPfdTs5b1K33aKco78FO3udq07t8AFd5ygA=");
            
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
                "eNprYGggAN04GhhG4fCAQAAAxQN7Tw==");
            
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
