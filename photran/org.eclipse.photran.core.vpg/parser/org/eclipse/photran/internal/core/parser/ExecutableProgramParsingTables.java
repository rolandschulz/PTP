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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 18, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 110, 189, 190, 0, 191, 192, 101, 1, 29, 36, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 57, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 3, 57, 1, 8, 123, 4, 124, 15, 5, 127, 221, 237, 125, 6, 128, 7, 126, 0, 173, 238, 206, 212, 214, 8, 239, 215, 88, 29, 9, 216, 217, 218, 219, 101, 29, 113, 10, 220, 11, 240, 222, 12, 13, 227, 0, 14, 228, 2, 129, 230, 150, 241, 231, 242, 15, 16, 243, 29, 244, 245, 246, 17, 247, 30, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 259, 137, 260, 261, 262, 263, 22, 23, 264, 265, 24, 266, 267, 25, 3, 268, 269, 270, 26, 27, 152, 154, 28, 243, 271, 272, 237, 241, 273, 274, 4, 275, 276, 39, 29, 39, 244, 277, 278, 279, 0, 88, 39, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 56, 291, 30, 292, 293, 156, 6, 294, 295, 296, 245, 297, 298, 299, 238, 300, 301, 103, 302, 7, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 29, 39, 313, 31, 314, 315, 32, 316, 5, 317, 33, 318, 319, 320, 0, 1, 2, 321, 322, 323, 29, 34, 324, 239, 325, 144, 326, 327, 328, 57, 8, 329, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 330, 255, 256, 331, 242, 9, 173, 10, 332, 333, 35, 334, 88, 335, 257, 336, 337, 338, 258, 180, 250, 259, 339, 340, 341, 263, 265, 342, 343, 101, 344, 345, 346, 347, 348, 349, 11, 36, 37, 350, 12, 13, 14, 15, 0, 351, 352, 16, 17, 18, 353, 354, 355, 356, 357, 358, 359, 19, 360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 20, 21, 388, 389, 0, 390, 22, 391, 23, 24, 26, 38, 39, 28, 40, 31, 267, 41, 42, 32, 392, 393, 394, 395, 33, 34, 396, 43, 44, 397, 45, 46, 35, 36, 398, 37, 40, 41, 42, 399, 400, 47, 401, 48, 49, 402, 50, 51, 52, 53, 54, 1, 403, 404, 405, 406, 407, 55, 56, 2, 58, 59, 60, 408, 61, 3, 62, 409, 63, 64, 65, 0, 410, 411, 66, 67, 4, 47, 412, 68, 69, 413, 70, 6, 414, 415, 3, 416, 4, 48, 71, 5, 417, 72, 418, 6, 419, 73, 420, 421, 74, 75, 7, 422, 76, 77, 423, 49, 50, 424, 78, 8, 79, 80, 425, 81, 426, 427, 1, 428, 429, 430, 431, 432, 433, 123, 82, 434, 83, 84, 435, 9, 85, 86, 53, 87, 88, 10, 89, 0, 90, 91, 11, 8, 12, 92, 436, 93, 94, 1, 95, 96, 97, 13, 98, 14, 0, 99, 437, 100, 102, 104, 105, 106, 438, 107, 108, 109, 439, 110, 111, 112, 440, 113, 441, 442, 443, 114, 15, 444, 445, 446, 447, 448, 449, 450, 116, 117, 451, 118, 452, 119, 17, 120, 181, 453, 454, 8, 455, 121, 122, 19, 123, 124, 456, 457, 458, 459, 126, 127, 129, 25, 130, 131, 20, 15, 132, 133, 460, 21, 461, 462, 463, 128, 464, 465, 466, 467, 134, 135, 0, 54, 136, 137, 138, 139, 140, 468, 141, 22, 469, 470, 471, 472, 142, 55, 143, 117, 145, 146, 147, 473, 474, 475, 148, 149, 150, 151, 23, 8, 152, 476, 477, 478, 479, 480, 481, 101, 482, 483, 153, 484, 485, 154, 56, 486, 487, 155, 488, 489, 490, 491, 492, 156, 493, 494, 251, 495, 496, 173, 169, 157, 497, 498, 499, 500, 501, 158, 502, 503, 159, 504, 505, 506, 507, 160, 508, 2, 509, 510, 56, 161, 511, 512, 513, 514, 515, 516, 162, 517, 518, 519, 520, 163, 164, 521, 522, 523, 101, 170, 524, 525, 165, 526, 166, 527, 528, 529, 530, 15, 260, 27, 531, 167, 532, 261, 533, 262, 534, 268, 535, 270, 18, 168, 171, 172, 174, 24, 175, 536, 537, 538, 176, 539, 271, 540, 541, 542, 15, 276, 543, 7, 8, 57, 9, 10, 544, 11, 545, 546, 547, 16, 143, 548, 17, 177, 549, 277, 550, 58, 0, 3, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 29, 562, 563, 564, 565, 566, 567, 568, 18, 31, 19, 32, 569, 570, 571, 572, 39, 573, 574, 575, 576, 577, 578, 579, 580, 581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 595, 596, 597, 598, 599, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609, 610, 611, 612, 43, 613, 148, 614, 615, 616, 44, 168, 617, 618, 619, 174, 620, 45, 621, 46, 47, 622, 623, 178, 179, 624, 180, 625, 181, 626, 182, 627, 628, 1, 629, 280, 3, 630, 285, 48, 49, 63, 76, 631, 632, 4, 59, 633, 50, 77, 634, 183, 635, 636, 184, 637, 638, 639, 640, 5, 641, 642, 6, 643, 12, 14, 644, 645, 646, 26, 647, 648, 649, 185, 650, 651, 186, 187, 652, 79, 653, 654, 655, 656, 657, 658, 188, 189, 659, 190, 660, 182, 661, 191, 15, 662, 663, 664, 665, 666, 80, 81, 667, 668, 669, 86, 670, 87, 93, 94, 99, 671, 192, 100, 672, 673, 2, 674, 101, 103, 111, 28, 19, 675, 676, 193, 677, 678, 113, 115, 116, 117, 118, 60, 679, 680, 681, 682, 683, 684, 685, 125, 7, 21, 22, 686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 132, 4, 701, 702, 703, 134, 135, 133, 704, 142, 194, 61, 143, 144, 146, 147, 705, 152, 153, 154, 706, 155, 156, 157, 707, 6, 158, 159, 160, 195, 196, 62, 197, 198, 708, 64, 184, 65, 66, 67, 68, 709, 710, 8, 9, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 29, 30, 32, 724, 725, 726, 727, 728, 729, 730, 731, 732, 733, 734, 735, 179, 736, 737, 738, 739, 740, 741, 742, 743, 744, 161, 745, 162, 746, 747, 748, 163, 749, 750, 751, 752, 753, 754, 755, 756, 757, 758, 759, 760, 761, 762, 763, 764, 765, 766, 767, 768, 769, 770, 23, 25, 26, 27, 771, 772, 773, 774, 775, 164, 776, 165, 777, 166, 207, 167, 778, 199, 779, 200, 780, 781, 169, 782, 32, 783, 784, 785, 786, 787, 210, 788, 170, 789, 790, 791, 792, 793, 794, 795, 796, 797, 173, 798, 799, 800, 801, 175, 802, 803, 804, 805, 806, 807, 10, 808, 809, 810, 811, 812, 813, 814, 69, 7, 176, 177, 815, 816, 817, 818, 819, 820, 821, 822, 823, 824, 183, 31, 184, 185, 825, 186, 187, 201, 1, 188, 70, 189, 190, 192, 194, 195, 72, 196, 197, 198, 202, 203, 204, 205, 207, 826, 827, 828, 208, 829, 0, 830, 36, 33, 831, 832, 833, 209, 210, 73, 211, 212, 74, 290, 834, 33, 835, 213, 214, 215, 217, 219, 220, 221, 836, 222, 202, 837, 203, 838, 839, 840, 841, 842, 35, 223, 75, 843, 844, 224, 225, 8, 845, 225, 846, 226, 227, 77, 847, 275, 848, 228, 229, 230, 231, 849, 850, 291, 851, 204, 852, 232, 233, 234, 853, 854, 205, 206, 855, 207, 856, 857, 209, 858, 859, 860, 861, 210, 862, 863, 34, 211, 212, 864, 865, 218, 213, 866, 867, 868, 869, 215, 870, 217, 871, 872, 873, 35, 220, 874, 222, 875, 876, 877, 878, 78, 235, 236, 879, 82, 36, 39, 83, 84, 47, 50, 85, 51, 88, 52, 880, 237, 238, 239, 881, 882, 223, 883, 240, 224, 884, 885, 886, 225, 887, 57, 88, 36, 244, 245, 37, 294, 101, 226, 888, 38, 889, 227, 890, 39, 242, 247, 2, 40, 250, 79, 251, 254, 41, 256, 891, 248, 892, 893, 894, 1, 895, 301, 896, 897, 243, 53, 228, 898, 899, 249, 253, 258, 229, 900, 901, 231, 902, 903, 232, 904, 905, 233, 80, 257, 259, 264, 54, 265, 267, 0, 234, 268, 269, 906, 270, 271, 272, 235, 907, 908, 909, 273, 274, 910, 911, 42, 55, 912, 913, 914, 915, 916, 917, 918, 919, 920, 921, 276, 278, 922, 923, 924, 925, 926, 927, 928, 929, 930, 931, 932, 933, 934, 935, 936, 279, 280, 937, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, 1, 938, 294, 295, 296, 297, 298, 299, 939, 300, 940, 941, 302, 304, 942, 943, 301, 305, 944, 306, 303, 56, 59, 60, 61, 62, 64, 65, 66, 67, 68, 69, 70, 73, 74, 945, 236, 0, 946, 307, 308, 947, 948, 75, 949, 309, 950, 951, 952, 237, 246, 310, 311, 238, 312, 297, 313, 314, 953, 954, 315, 316, 317, 318, 240, 955, 319, 320, 321, 322, 324, 110, 325, 327, 43, 329, 956, 323, 326, 328, 330, 331, 957, 333, 958, 959, 960, 241, 334, 336, 337, 338, 961, 962, 963, 339, 964, 340, 341, 44, 335, 78, 342, 343, 344, 345, 346, 89, 90, 965, 347, 966, 967, 247, 348, 349, 350, 968, 357, 358, 361, 2, 969, 970, 971, 972, 363, 364, 369, 377, 81, 378, 973, 392, 382, 383, 384, 388, 394, 89, 395, 396, 397, 303, 398, 400, 310, 401, 974, 975, 402, 403, 976, 977, 404, 978, 979, 980, 405, 407, 11, 981, 982, 408, 410, 91, 92, 95, 412, 90, 983, 984, 985, 250, 91, 251, 986, 987, 988, 406, 989, 990, 3, 991, 992, 993, 994, 95, 995, 96, 996, 997, 998, 409, 999, 4, 1000, 1001, 413, 1002, 1003, 96, 6, 1004, 1005, 1006, 97, 1007, 1008, 1009, 1010, 252, 1011, 97, 98, 1012, 1013, 254, 1014, 414, 415, 417, 420, 421, 422, 423, 425, 45, 0, 426, 1, 427, 2, 428, 429, 430, 46, 431, 99, 2, 47, 432, 433, 434, 435, 436, 437, 98, 438, 439, 440, 441, 443, 444, 445, 446, 447, 449, 451, 452, 453, 454, 455, 457, 458, 459, 3, 259, 460, 461, 462, 463, 464, 465, 466, 468, 469, 470, 471, 472, 473, 474, 260, 475, 261, 476, 478, 483, 1015, 113, 484, 485, 486, 4, 262, 477, 479, 488, 480, 5, 490, 1016, 492, 481, 263, 264, 482, 487, 489, 491, 493, 494, 495, 1017, 265, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 1018, 1019, 510, 511, 1020, 1021, 1022, 267, 512, 513, 3, 114, 115, 514, 1023, 515, 1024, 1025, 1026, 1, 4, 516, 517, 116, 100, 12, 518, 519, 520, 1027, 521, 523, 113, 48, 1028, 1029, 522, 524, 525, 1030, 270, 1031, 1032, 269, 526, 1033, 271, 7, 1034, 1035, 272, 1036, 1037, 1038, 527, 117, 529, 528, 1039, 531, 533, 1040, 277, 1041, 534, 312, 1042, 535, 1043, 278, 279, 536, 537, 539, 1044, 1045, 1046, 1047, 538, 1048, 1049, 1050, 281, 1051, 1052, 118, 1053, 0, 1054, 1055, 1056, 282, 1057, 1058, 1059, 1060, 1061, 1062, 119, 101, 102, 103, 120, 122, 123, 1063, 126, 127, 129, 130, 1064, 1065, 104, 1066, 1067, 49, 1068, 1069, 313, 1070, 540, 541, 542, 543, 544, 545, 546, 315, 1071, 131, 1072, 1073, 123, 50, 1074, 51, 1075, 5, 547, 548, 52, 549, 136, 551, 105, 550, 552, 124, 553, 1076, 1077, 318, 1078, 284, 1079, 1080, 554, 1081, 555, 557, 1082, 558, 1083, 1084, 285, 106, 1085, 107, 560, 563, 564, 565, 566, 567, 573, 1086, 1087, 1088, 1089, 137, 1090, 1091, 562, 1092, 1093, 1094, 1095, 1096, 1097, 1098, 1099, 568, 572, 580, 1100, 570, 1101, 593, 1102, 595, 571, 574, 575, 576, 581, 1103, 1104, 1105, 604, 577, 6, 7, 607, 590, 1106, 591, 598, 286, 1107, 1108, 1109, 287, 600, 1110, 288, 1111, 289, 1112, 606, 578, 1113, 1114, 108, 579, 582, 583, 584, 585, 586, 2, 1115, 1116, 1117, 125, 53, 587, 54, 588, 1118, 292, 608, 1119, 1120, 1121, 1122, 293, 589, 1123, 1124, 1125, 1126, 1127, 1128, 1129, 1130, 609, 610, 1131, 611, 1132, 612, 1133, 613, 295, 1134, 1135, 614, 615, 138, 296, 1136, 616, 1137, 1138, 139, 1139, 1, 1140, 1141, 592, 596, 1142, 620, 617, 109, 9, 597, 618, 13, 1143, 601, 1144, 1145, 1146, 1147, 297, 1148, 298, 1149, 140, 141, 619, 55, 1150, 1151, 1152, 1153, 1154, 621, 1155, 622, 1156, 623, 299, 624, 300, 625, 1157, 626, 110, 1158, 1159, 10, 627, 629, 630, 633, 634, 1160, 1161, 635, 1162, 637, 632, 307, 638, 111, 1163, 1164, 11, 1165, 640, 639, 308, 1166, 310, 1167, 641, 1168, 1169, 145, 148, 1170, 149, 1171, 311, 1172, 314, 316, 1173, 1174, 56, 602, 1175, 1176, 1177, 1178, 0, 1179, 1180, 1181, 1182, 1183, 642, 1184, 1185, 113, 332, 1186, 1187, 1188, 605, 628, 636, 57, 644, 1189, 645, 646, 1190, 647, 1191, 1192, 648, 1193, 1194, 1195, 1196, 150, 649, 650, 1197, 1198, 651, 652, 1199, 0, 1200, 1201, 1202, 8, 151, 167, 653, 654, 1203, 655, 168, 656, 657, 1204, 658, 1205, 171, 172, 1206, 317, 323, 1207, 659, 1208, 660, 1209, 661, 1210, 1211, 662, 663, 664, 1212, 12, 1213, 342, 665, 174, 1214, 667, 1215, 670, 343, 672, 345, 344, 1216, 346, 673, 1217, 1218, 326, 674, 675, 1219, 1, 1220, 1221, 348, 1222, 1223, 114, 1224, 116, 1225, 349, 1226, 350, 1227, 58, 3, 4, 676, 677, 1228, 126, 59, 351, 1229, 352, 680, 1230, 9, 1231, 178, 678, 681, 1232, 1233, 683, 179, 328, 684, 685, 686, 687, 688, 689, 127, 690, 1234, 363, 691, 117, 1235, 118, 1236, 1237, 1238, 180, 1239, 1240, 692, 1241, 13, 14, 693, 15, 1242, 694, 1243, 695, 1244, 1245, 1246, 696, 17, 697, 18, 1247, 698, 699, 1248, 181, 700, 1249, 1250, 701, 702, 1251, 703, 364, 705, 709, 332, 707, 710, 1252, 1253, 1254, 711, 712, 713, 714, 2, 128, 60, 119, 715, 716, 717, 1255, 1256, 718, 1257, 369, 1258, 333, 120, 122, 0, 123, 125, 719, 720, 186, 61, 62, 721, 722, 63, 723, 187, 64, 724, 1259, 377, 1260, 725, 726, 727, 728, 729, 730, 731, 732, 1261, 733, 734, 1262, 735, 1263, 736, 126, 1264, 188, 187, 1265, 1266, 388, 737, 378, 1267, 738, 739, 1268, 130, 1269, 1270, 740, 1271, 19, 392, 131, 1272, 1273, 741, 742, 743, 8, 1274, 1275, 1276, 20, 134, 393, 1277, 744, 745, 1278, 382, 189, 190, 191, 2, 383, 384, 1279, 746, 1280, 1281, 747, 748, 65, 749, 192, 750, 751, 135, 752, 753, 754, 1282, 755, 756, 757, 394, 1283, 1284, 136, 1285, 1286, 1287, 1288, 758, 759, 1289, 760, 395, 1290, 1291, 1292, 196, 761, 762, 763, 1293, 764, 194, 1294, 1295, 765, 766, 1296, 396, 767, 768, 769, 335, 770, 9, 195, 771, 10, 11, 1297, 772, 773, 1298, 1299, 1300, 397, 1301, 398, 1302, 399, 1303, 1304, 400, 1305, 1306, 137, 1307, 140, 1308, 1309, 1310, 1311, 1312, 345, 197, 774, 1313, 347, 129, 401, 66, 350, 1314, 775, 776, 777, 778, 779, 780, 1315, 781, 1316, 1317, 782, 783, 1318, 130, 67, 784, 1319, 1320, 1321, 198, 201, 785, 786, 787, 1322, 1323, 1324, 788, 1325, 1326, 1327, 1328, 1329, 1330, 789, 790, 1331, 796, 402, 10, 792, 11, 12, 1332, 1333, 791, 793, 794, 21, 22, 202, 795, 1334, 203, 1335, 68, 797, 1336, 798, 1337, 1338, 1339, 799, 1340, 800, 1341, 802, 1342, 801, 1343, 803, 804, 805, 808, 404, 806, 1344, 141, 1345, 807, 13, 1346, 23, 809, 142, 1347, 1348, 1349, 1350, 1351, 413, 810, 14, 1352, 420, 143, 1353, 1354, 1355, 1356, 1357, 422, 811, 1358, 421, 1359, 424, 427, 1360, 1361, 428, 1362, 1363, 1364, 1365, 6, 14, 1366, 1367, 1368, 1369, 204, 1370, 812, 813, 814, 815, 1371, 816, 817, 338, 12, 205, 207, 1372, 818, 819, 13, 822, 389, 1373, 434, 435, 15, 1374, 17, 1375, 208, 1376, 1377, 436, 1378, 1379, 1380, 144, 146, 7, 8, 824, 825, 826, 827, 437, 828, 351, 1381, 1382, 438, 390, 1383, 1384, 391, 829, 14, 830, 209, 831, 1385, 69, 210, 211, 439, 440, 1386, 1387, 832, 833, 1388, 1389, 1390, 1391, 834, 835, 836, 1392, 1393, 1394, 1395, 15, 837, 1396, 1397, 838, 839, 840, 1398, 1399, 341, 212, 216, 223, 1400, 1401, 1402, 188, 1403, 1404, 24, 442, 1405, 1406, 1407, 1408, 443, 449, 841, 441, 1409, 1410, 842, 1411, 1412, 1413, 1414, 451, 452, 843, 453, 1415, 1416, 242, 190, 1417, 1418, 70, 844, 845, 1419, 0, 243, 846, 847, 454, 244, 1420, 848, 850, 851, 1421, 849, 1422, 1423, 852, 853, 854, 855, 856, 392, 1424, 1425, 857, 1426, 858, 1427, 455, 1428, 1429, 1430, 1431, 352, 353, 354, 1432, 71, 456, 457, 355, 859, 860, 861, 862, 863, 864, 865, 1433, 458, 18, 1434, 147, 148, 1435, 1436, 1437, 1438, 1439, 1440, 866, 1441, 1442, 867, 16, 868, 869, 870, 871, 1443, 872, 459, 1444, 1445, 873, 874, 875, 460, 1446, 1447, 461, 463, 876, 462, 1448, 1449, 151, 1450, 877, 464, 878, 465, 1451, 1452, 152, 1453, 466, 1454, 1455, 1456, 149, 879, 1457, 468, 880, 1458, 881, 1459, 882, 469, 883, 884, 885, 886, 887, 470, 1460, 356, 359, 1461, 888, 1462, 396, 889, 1463, 150, 153, 154, 1464, 1465, 890, 892, 891, 1466, 895, 893, 896, 1467, 1468, 1469, 1470, 894, 1471, 897, 1472, 471, 1473, 1474, 155, 1475, 1476, 25, 1477, 156, 1478, 1479, 26, 194, 898, 1480, 2, 1, 1481, 899, 900, 902, 398, 360, 362, 365, 472, 473, 903, 403, 1482, 1483, 1484, 245, 246, 1485, 901, 1486, 904, 1487, 1488, 1489, 905, 906, 910, 935, 247, 1490, 1491, 27, 474, 1492, 1493, 28, 475, 1494, 1495, 248, 158, 943, 942, 907, 366, 1496, 367, 917, 249, 250, 251, 489, 493, 253, 254, 1497, 255, 1498, 922, 1499, 941, 944, 494, 1500, 1501, 495, 497, 1502, 1503, 501, 945, 16, 947, 498, 502, 503, 504, 1504, 1505, 946, 948, 950, 256, 258, 1506, 508, 1507, 1508, 515, 1509, 262, 368, 1510, 1511, 1512, 951, 952, 1513, 1514, 953 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 6, 1, 21, 2, 22, 2, 23, 24, 25, 2, 2, 7, 26, 0, 27, 28, 29, 30, 31, 32, 8, 33, 34, 0, 35, 29, 36, 37, 38, 39, 9, 2, 6, 9, 40, 14, 41, 42, 43, 31, 44, 45, 18, 46, 47, 18, 48, 38, 49, 29, 1, 32, 50, 4, 51, 31, 52, 53, 38, 54, 40, 55, 56, 57, 58, 59, 60, 61, 0, 62, 63, 64, 2, 65, 3, 66, 67, 41, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 34, 80, 81, 41, 8, 82, 45, 83, 84, 0, 85, 59, 86, 49, 87, 88, 89, 90, 56, 6, 91, 0, 92, 93, 2, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 59, 68, 107, 108, 109, 8, 69, 110, 111, 70, 112, 72, 4, 113, 3, 38, 114, 115, 24, 116, 117, 3, 118, 14, 3, 73, 119, 120, 121, 122, 123, 3, 124, 125, 126, 127, 128, 129, 130, 17, 131, 6, 74, 9, 132, 133, 75, 90, 134, 135, 136, 91, 137, 100, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 106, 152, 2, 107, 54, 153, 154, 155, 156, 1, 157, 3, 158, 159, 0, 160, 161, 162, 163, 164, 4, 4, 165, 166, 0, 167 };

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
            final int compressedBytes = 3325;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb1vHMcVnx3tKSvmQyNCsohUS1pWGECFnMZJwWhJy7FsIA" +
                "hBiYCNgAiQIlHBKnBJAUtGCMhULFSwPKgQ4M5/AmGoMFSxiJFA" +
                "Vf6UzH7c3e7ezPzezNs9yshJoki+m5k3b973m7f39vpfdt/7+t" +
                "61J6PDe9/86OXe+YODX2XPP/v+Rv74zcbDZfH2D3d33zt5+tnN" +
                "0eG+hu8W8M3nv//+xunjtxX8zu7tk/UvXhTwj1/uvv7oYKccf/" +
                "r4PxtZD+Ov3wX48daX+o8SIhLF66r+t7J9X3/NhcjkKBY97J8L" +
                "R+u74eUrFY5X1P2Fav7Axe/8p3cejg7Xk5tC3Ptm8+jW+dX1HZ" +
                "Enn+hFMrEQ+jDnv35Hz6/xL/jvip7/o4Plmv++m4zX8Efl+k04" +
                "mX9557u0JG4tJ4kQo8NMyDQTH36lsuefnEb5zvFGeX5/3bt9sv" +
                "/Hf33w9/2Xn7760+sHB3/++B97/y7GP3s4vHxx6V/wT3y4vvKi" +
                "4J/o6Nbrq+vLfvzjxu/nS1LT71Gk6aflX9PvN1/dz55vFvT750" +
                "Ys3gH+5NPXzL87Jf8i/TC8fsfyxZNfwN9M/q/4J2nxz2aDf0Qi" +
                "qq/1S/+gZP19JgRb/rn0kc3Dzhpfa8tw2faPzZ8D69/h7f8C6N" +
                "t85V36cunzjtN3cPsH4GD9tv6IJ/5Z1uR/t/9h1f8l/M0Mvm+y" +
                "D4Pvf2D6Qfow9zc5lHn/OSLpJz3DaOpwl8HIE6W/FvGHGAm+/1" +
                "Gf7yOb/R/avsP4iil/tX9YxRdRGV94+oe/qP3jw/3x1D+u9veM" +
                "Gp+eVPiPK/zX+rUvw/qPMpOx8XzU5nJxPmz4/PmnzfNH4w3xaT" +
                "d+jV3xK4Kj+BTBv63gVXyyWcYnU3jUA5wbP3Pn9xk/rsZv+eCH" +
                "8y/M+Lph/8bVeNmML2D8DOZH4wPgoh2/DxsfT8aH6s++1reNb8" +
                "LHFVw24ab4vRl/ceM/NP8PEZ7xxqet/IjNvzzd+Y6Sfyjt09f7" +
                "LfpvVvwxs38OuOaCGOUvzfJfr086/2Rg/mrNn/rkD7jwtny+Ku" +
                "Vzq5bPZ9kye7zv/KIxvwjbn+hzf8agOKPnH75t+qcd+xqV62cC" +
                "v3L4DmX5PcrvwFfiBnPj18uGm/ijlR/pf33hhx9PP6L43x6/Vv" +
                "/b/acdn/jTml9owkP0swF/0Y6/3fEPir/rieQsBxm3RA2N58I7" +
                "+5NdUR9gfeEznptfaI43nX/DPxYz/7m0j2cbghBfCvkTV3yB4I" +
                "T6MWv+pc+t+yv9f25+A41vwscVvOVfe5sib3gGjAywj5JnvyB+" +
                "ScBGoybetfSoRDaHJfU3MnHzr/YPchEX6qdCJMnFSvH/eqmQku" +
                "KvbCGad1yB0r9IZ+9oacsYbkvjJ5Uq8JMVfmcFfkq/67zCL5h+" +
                "xKNM4Pk69w/xh/NnTP7JuhtRPvyF9Fubz2QShl+XP1T9xhjjV/" +
                "Bvk74rXfra+K+aH+/PrQBaBrH8YbsWrqn97Fe+0fjceL5x8Pzk" +
                "9RPX+Hg4/ZcBmYb6DaIUOX4fLt85TT7h+Uz0uzDq93p+5/nHln" +
                "Ulhf5BcBXO3/jlpd/Y8iegfpXVFYKufUhp8EuzjznQH1T9nfHs" +
                "G9M+EfS7RzbDn34QP2C/0Pma/TeH/sk95Q/vH/F3T/5L83AaKb" +
                "rENt5iH3JP/96XP3Oq/hGe+sUi/33zf3d+VB8F/I/vL0Qxp77K" +
                "rU+i839/5a1UN8VoTV2V0W+FWD27WBWpGifiv2ujO7mHf64IwW" +
                "JNtswjvxp0PjP559YXaYyeOLabuaUQ0Hfo+1moPtmEm/IXi8LP" +
                "Vr8s6UfS6Rb+ZMaf3PrmvP56Yok/LayG+AfUDxF+1vy1mp+/lV" +
                "8kzh9Qf5y/P+kaD893y6m/cH48c4d/XvLNr+9x64/d+mDAeOE3" +
                "HvkvxCxVcH5JtdHuxJfe+HvqD1i/FDz7EOB/es2P8SdF/R2uUW" +
                "Q4v77oeX5zTqs7PwH1k115KNr4zK6/Yv75ovE59P8yp5x63v+l" +
                "1OeEZ/1PuOqPhvqkKfc3gUvD+EuF91CfddI/4Pz84lOYtwzJry" +
                "hDfjLQ/yPr36Z8JGT9y6hvnnXuP1LholP/RfVXp/9qGI/gXvVh" +
                "VN81wEUTzq9f8+rDTf5QxvhEoPokrN8VHLaqR8zlr65g/kP3S2" +
                "r//or4wBY/MOt7xPwcyVQb/YsM+B/U8cT8qmd9D9b/IH2pcGnE" +
                "b5of0oub8kPvr7xpw1cquOjCEzNcJp8LDY8KPSJ+pgVt5WKyv7" +
                "U6v5NXuHX5nyYfcP5i/7ErP+beH8IP7R/hj/TLHH5nFvqj87HD" +
                "S/qt6T2G4I/2P5lfzs1/TsJPJlvO86XvXwbx72T/YfwpKPTx3P" +
                "/2vPwQ8MP4L7nxt9APyR/SL/p9PP28Xg3U9i+Pc71uLlWi0kRE" +
                "Go0fG+rTeXf+nFg/sY0/d+OXZA3n25SCQfaFSR9x0R5f408vG2" +
                "cGk+dRX/S/33Dfp77DXR/5V/h+DvDfjgGc638kaCPbgD7bfvXh" +
                "bn3Qtz5JOh8P/of3d5D8UOUrDI74h1vfK26sqcn6k/tbohnf8/" +
                "xHLn7m81fh8J79c1xfgf0Z4P4AT/8Mvn/2/G44zh8NjD+8n5YN" +
                "T3/lPH8Q/2/XOTYpzfZ7aP6A8Xnxt8hUtfk/zqPqvX3Zt9DzAf" +
                "uD/iMpqxGT+S834heT8Tfb7xj4dzGZvmb7HNvtL29/8H7KdP1A" +
                "/kgG9r/AQaH+Hq59pt9vCfOfw/JTXP29QP/EeD/Pxz536oNk/h" +
                "tPlCcr/4vrexWh05l+UBVaYzd+Mp1VOXORFINOZ/WNbHH2n2sf" +
                "evZ/uPGnd/zDxR+5nQvzHyKqx+W1P22/cxGNf3n0VA/5Ml/Oox" +
                "djbT5+t57G+ZU+4tNmrmII//Byzr+9UcP7s3hB8eF5QdY4LY1L" +
                "E5/UEy7D4NP4zVI/g+tnoqVfLXCP/IXyow+i70V7/NQ/pO4fxq" +
                "+gPgP8By2/eoXxqtDyK66lerbTXPvf2bprfe/4HvR/LBo+d/70" +
                "8aT8yOL8J9r9yeH0G+6PIxtCwitl5G9s8dmi8jfm9Qn5bXf+91" +
                "gcpW7/9kYJiosj0vFJWmC0qaJcHa/Q8C/1V/v+XYt+AG7z2fvK" +
                "f7nPJ2m1X6aGh+JQ/RNpzb+w/F+ufPDtRwhc0uMf4/nJvvQndA" +
                "994wOu/5gz4QvOvxHiV5b94vunIedHSRhwzsdj/UQcce6f9Fif" +
                "kEx4P/nrOWvO9I9wfQfkf7zzU35wJMTc+hfTPxxaPlH/G6Yflz" +
                "+49zsv93wC+sdaIgrHk+k/uV/dXW6LeX5398Dz4fZY/WfvlHwZ" +
                "+puw/UDPlwT87UmfnCN/pv0pt39GqR81I79u/hzO4sYfP9/S8H" +
                "ye+n5HTBiPnk9Gsf+u9bn6kbB/nv7sNT/yKsz++/NPRsIvFnz9" +
                "xNSPQ/tPsH5Eez4Pp7/LPT/z+U6E55uw+r8Es78L9o+B8yP0n3" +
                "1Y5YfmiHVcSU0ma5K78j8u+CXUX+j0hff3BvZPuf1n7Of/SQR2" +
                "40fv73P0H8d2+Ko6kxpe1E9k9GvNkXkeibS4IJSm+gfa/KT6Ty" +
                "D+ySSWDXw+F3d9JDy05+sJv/vtsxe6/wLvlyfU9J30wN8DDvNb" +
                "IfaPPh59vsFc/8Z8fwwr/2Hrf0ns87f4C/ZvAfl297+Q7ufkzP" +
                "yar3+raPRNW8w12X1k0X9OeONlg9vGk/rryv6kLv3b+8NwS3+T" +
                "Ufqc+tdCX3N/1A8HnjDhS/+HcLr84/47K/86+Y/QP8vg34S9f9" +
                "Qf2AN9nPD+7NOSJT/v9l/Y9ROy/rf4T77wQP8S+2+B/h/z+eSD" +
                "+6dk/5lLH1Srst5f69t/9zt/uH9e/B1WH1L94QfHX/DiO6B/Vt" +
                "VxHX/Goow/RRF/FodfxJ/HuH+V3l8blB+nzx8YH0+/t92fcsPL" +
                "+003Gf27xviBXn8k5OeI+ScZKB+e/b3z53+UuPyD7vyG+PP2yd" +
                "MvS/w+1fg9OPhbJ36VHPuI958T4+9A+x4U35ueXx2WP1j455t3" +
                "GWng/EUP9Xfe/TT2/VfBtQ890DedTdX5/Bru+Eu/fww/X8pevx" +
                "yJnvqT48vL79nvd83il/n+XFX051oyQEb9ZIdP5+/4x5P+X/v6" +
                "NDht/Xn9HYt+9tcbfWz7I9UvnPMLR/8A0f+Igf4hfP5NqH8XAh" +
                "d0+0mIXwoTkWYz+Sk2m5b9j5mY668O9u8t8gn9P2XYNuV+e19w" +
                "5Tb7qP8b+ldu/xB/fgGT/8n9/U7+jNHod7U+BPvH2fUPbv1qWP" +
                "8W3e8hPH8ayb/7+dT0/Ixnf7m1fkQbL9MO80ln/WgentPqR538" +
                "x7VyfWXQj7b1MyB9tud3tyY29FeOqedj6S8d0+bH+Qfe/Nb84d" +
                "iRf2rRH80P4MrSfzr2qt8Jwk2RwPySGz/IXyT/ze62sPs7MgPD" +
                "K0lf3/35gJTxg8KH9m+Z/i+7/vM/57JYMQ==");
            
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
            final int compressedBytes = 2898;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2PHDUU95jZwzm+TESSE5UTIrRFCqABipC5kEBAQkQXIk" +
                "GBRJkiFaIMku90RUKVgiLlKgUSXf6EBVFEV12DhK7iT8Ezs7e7" +
                "M2P7PfvZu8cpK0SSfWuP/fz8Pn7PfsME22MlZ4oJVn+EZlv1n2" +
                "PzjfkXO/r68t0Lj8bf/jravf/s06d3//rwwU61/8Xfbz6+88/V" +
                "6iybNXN/iPSjr9+9e/7RvS/eqp//8tO702sPPtje/6p+/tHVG8" +
                "3zHeOftJ3T+4/86DT0AP4JC4E8v1NDF3a2h6+vPJ38yUSff7id" +
                "hbnbr3v8MF379O8JWN9m4mqxf2aTV6dCPmv7dv7RWBj9eOXZS0" +
                "Y/fvjgbGPf9M7zmX3jw/VfqX1bkf3iq6VXC/77/Au8/9GXT6f+" +
                "D6PD+9+/PxLpPzVor3D2CfoJOP9p/eDS9XxwfdjhrP2Gffxo/b" +
                "hp9+8A/sLjq0eXYn9D/kUmOto/gdpzrHyEjh9p36L7R9qnSP9v" +
                "/fxLJB+u/UGND6r5DoqzT/72IH1pB8ftX2j8gH16w8zv9ytnvq" +
                "n9h5effl/Pr/Uf7hys1n6DYSWP8m+J9itB/AnF9146F9eZlKxg" +
                "kvPGBD25bRSD1LVdG5lV54K39Hp+NX2rS6f2D+o/q/2R4csXv3" +
                "5+/jpNjAZMkO46eoEuigBNnI6DVYLHv2L6/338qemh+N7Qvwf2" +
                "XwX5xxH7Y2m7idZFUdWifdl4J+r42W823ZRF/W1Vf12xbVlo+X" +
                "ArAT6YB988wfLd+9rMcCQXa2KU/TfS/F/XXB5F4z8h+JHsttft" +
                "dyKV/ib3T2uPiq+8z69AfMbfP7E9HJ+Q8Dlw/0U4cl77qVeBD8" +
                "kIun0D4eXH3jld/tDjd/CXOP43XPjjnedufEJmWB+nf0jDp1lR" +
                "4vAJ14eqHxDzt/JXJ+HP9LXLN0a7Zn0Zu/Jse+/cdGO8w7S4Ke" +
                "uJIfRXdv0Ixq/E8QH9Q/Saf+XueKvhX9Hw72yHf1H+XUB8T8UP" +
                "2Xr5N+y/DHr+25v83Flxq2CjXRMfG//045/e297fflzonV+ulj" +
                "Adkh9jn74///v9zvi3W/4e2cefuD2Vv4KVPS0meRD8EiKfvzXy" +
                "d30mfz9j9Ac0PmD/gM9H+78zBjjDljj79GerH6z6tWj3Jx/6t5" +
                "DJsq0LBxwjZ//I50fn16j9k9rH6cd0/sl0ef0t9sEmv/Hxw28Z" +
                "4hu//oLpmeOjOT4xmMzD5d0SHulj8cE4/LD3eL5Il5ayZw2L/p" +
                "Nm3p/ufCsj8wvA+OD4EJSfTPElhY5hy0nGRwPGB+KDIL5Iwy+d" +
                "dDw+6ftQ8bM4+QzJf+T276n6KYYegt9kjh+J8RUdP6gQex0Di+" +
                "fCT9uPGsinSobPVI2RVcMmL2H4A/WvvfTNTWbiO8ON0W7FuInv" +
                "3v9JVvs36/ju4dVV4E+d+anhb8H8rD+/C+Z/qfsbkO93to64fI" +
                "uNLpmHF58wdvHJ4UWm5ESwfy+NLmt7yG61T9zR/0G3/622fzbr" +
                "f910SD9A7bn4khl6UZsm9rrZJ1uHx+t3qV3f6z367Q4dHB9G55" +
                "Tx+CCefzzX+jT8cfXPxQHAXz//c49/uL5hz0+yPwn6jdr/RfmE" +
                "G/mrz1fy4iOjIbU2pkIqwSbK/CMgfuJx8g3YD5jOTnh7gK6Og2" +
                "ClS13LHWeiZn8dVb6yjH+Kjn9xauYvprT5QfwZtw0LNm75q7ns" +
                "8DeZfRap8ntJ/QPGDpP4pys/3/+CnoYuoKaB8dGwf+T5psj4Kf" +
                "r+09LzPfk1sv8ODJR8/5XdDqPbXE4Sfg49H6s/IGKof6Ia/4TO" +
                "39zxJw3fR+EnZU58aQqMH6If4+OzpJjqA6DI8clY/D7Gfgac38" +
                "iy/ng69fxK7vsJcecX5MroVHyXiv9Wdf7xDyXMFr6iCn1uuiHq" +
                "RjebXyTBj7H5iYz3ByjnoxLTiwj/Sdj8+7I/Q+7K79T/lYP7ka" +
                "WxY3qZvrlElws6Dp8tAf5G0+v8wYVH975r5PtzI9/XHvwI5xcT" +
                "7l84PrbYR7mwj1D8l+Z+fwnsv9x0v//naw/fL2ru3875oNcyv/" +
                "Xxpz//cHpXnGb7X2WaX3D/WPxSGv87Cr+05dc68Z0OvJ/jt3XB" +
                "/QP4Ec6/KQH9Fri+AfpnmL+Vaf0rf/w8j8/kBlvEZ2wen8H+A/" +
                "R8efyH/fwfmB8G+BMVXzH8+NDxXWv/z8zt11z+vfKL2p9l3//p" +
                "+Ce085swPkQ7f+Ki86X2y/pupv9E076z3F75YYPn4+QfbI+kd+" +
                "tTSAC/wq8fYn2o+JqjvsckzfqD539Tzc9eHyTu/Obic9Dez7nl" +
                "qg90sLi/c99Gh/Etq/7kAfhS2EcH0qn341LgY1nxhZOND4UuZI" +
                "ESAnf/UPsiefvc/Alsr4NZRnz+uuVzBfiRl65P4Pxe0FdGV7UE" +
                "FJMzez+Yv3ynJSv2J8b9/WzMSp3kfGMsvvlifVZvH1XE+kLykd" +
                "l/EFhD4tKf/vp6dLqHtyvZX1N//T90/67xA/OHz9d0xzfHn1RG" +
                "+Qvhr14znTi+MeOGA5OL7F6Njijz88daKlGNFVL+Eoz/VJ9fXD" +
                "c9Vj928M8ym/7OXX85u/3BxleE82ee/QGfr/LLb4L6/YjzaRZ+" +
                "VSWSf5UF/+2tTxkanwWH7XB9S39+Cp2/suCPWeVXdFiuln+uUY" +
                "zKXb+dSA8+f4jG1yYL/ZPk/Gfk+Uz6+VJY/zIf/l9R+7fsr+Xz" +
                "Edj6BSzAvwjZP4D8Zd6f5PXH1DdF7S8H0dq/xPefxX6zAPtAWx" +
                "+E/UTmx6H123Tz1+MfQPWtyOcTs9dH8u9flH6ofONz1Hd7PMsP" +
                "AeMD6ysR6zeB9UWI9cEQ9YOI9ZMC84tuekB9ooT+N3h/P/Pzie" +
                "OD86t+/tPr22D8R7X4a9kz3LD/WLJF/Rjerx8Tl389VfWbE/En" +
                "rr4uor4IVL8Ikp+4+s/g+Q57ezA+ZbHxaQg+nSP/54r6aP5R2W" +
                "VZtUTVGBeTev+IM/6qPB5DG/+91z67auK/3PWdct8fgfHTBP1T" +
                "2gf59wqWZY303/SO3f7oMHwtNz5BpcP39/zyCdY3AM9Hzus/8O" +
                "X6Da7zkVH4AyJ+5R39HFC/AXp/FfZ8qLt/7yee/+38V1dfO1Z/" +
                "YPEN1/uRDiF8DLDfh9bxi6H99tu3bHQsPpLr/nPX5y4A/zLN+b" +
                "IA/JOM71Ll33//O8H7l/eEV78R74eH9j/EvzkFf0vnvzj1B/H8" +
                "Kyr/83+O//zikvv9qOD9dMjRw+Lzce2p/j/VPsLxAzW+wI7fzp" +
                "/m/vRu5/60maic35+20xf3q/H5ZQe+Deff9mj3XypH/qlvISPv" +
                "/4L3j/3t4fvBcHtcfddM7/9Jln+KxT/A/Moerr6N23+1vj+suT" +
                "9VRdYXY/j+IUUNjm/QXvo9RNf+sNPh+3O3u/Ib6P/h8wPYAyBR" +
                "8hd/vxS4v0p+v0Mvf1Mu5W+qFPaP/H6XGP0UmD9B1cew+w8I/m" +
                "e9f4m6H0e4f9rnXxHqXzuf78O/Q/BpnP8C34/05M9x+sUVHxHP" +
                "J4IC7L//OadvAHQeS6fdj6W+Xxd+f43jfPgEL18k+bTbn9T4pl" +
                "M+VsEfUnyfAB/w4xsZ5C9ge3bsa3k8uMpvf0LsH2h/A/IrMfhN" +
                "7PlgxVD5V/r5KjR/hIWA8j9KAv8b/18tfuo53yDi+E+KrzD11x" +
                "z5tee4/Jotv8cCnk/DR1Z3Px1iRK738wL3o+D8iLc9ND74/Zl5" +
                "8THofkqS+IKUX08lv5B9tue3mvr7dX5Ect4s8ZNhfkT5rB6UX3" +
                "no1z/4/AyUP4qN/yo+YwmP2t/w+6Os/ePPJ9L0T/z7mzTSfyDy" +
                "n+if5Hk/PD6/CuUn139+Ju/76wPOd/Ic+FDc/Qyq/k95/oqWP4" +
                "LjU6D//wBhBXI0");
            
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
            final int rows = 801;
            final int cols = 8;
            final int compressedBytes = 1973;
            final int uncompressedBytes = 25633;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXL9vHUUQnjvuvWwioSxWTCyqc7CQCxcoTaAwOUMiSIESOb" +
                "EEBX0KKkRppLXlIqFKkSKl5QKJzn+ChVKgVGmQkCv+FO7uvdjv" +
                "7u3uN7uz52fBNf4xb3/vzHzzzdwjRYaKnEpS1DzK0Erzc73+D7" +
                "X/q4j35DO/67PfTh+u7dx8vv7dy9Hej8dfHe28vrO7XR08+OuD" +
                "F4//3qyWoJwoK3oD6ZDx26fwzO/6Jzsf/r5x9clob+P4ytEPJ3" +
                "d3b7fjm8dvNu8tTT7k2wLl35+PruXLS+qbjEZ7ROOyos9//nTr" +
                "YOtFZrZ/3SwI769qZ69mlqLfraRijD+3F2auf277PK69f35/vL" +
                "92r9hbVzeINo639pdPxuvbZNT9etQqa89f2t71GNatwvIKfBjI" +
                "wf7V5531e9KT2Rt/S8Paf/v4er69poBnpr2ioCej0Ptl0//O+e" +
                "TTKeWR5+O3j9h+WsdXzPnXN3g0o4E56SdNT6aZ98h57Jr81+Py" +
                "yIvukVczu28453P6sLbfz58+uNH4jytHO4393jr4tvEfpxP7Lf" +
                "NfyGycXl+rx6/tT+M/3qvHv7O7NPEf21P/RTL7i+X79vt32Guc" +
                "x7V33m9f/+f79/HKm1zfoNEtGlP2BdHqyttVKnXd+J9bozXDUW" +
                "CvfFW/yutLVJQ0zrPPiEpjsrr/UlFZ1n/g/s/Wd23g9bnan3bb" +
                "v+q1B+cvHZ+1/4WgvfD+EL31y8X+QXflpmefe/pxtZ2/ntWPfd" +
                "n6tFWu5tanIvHHI7/9EeJnPlJC7XOnfb/5/On3bXzwdR0f3N39" +
                "qWPf5f55ur5I/CZu79iPquhBomzSWclysG6xSYxfhfgP+29ZfI" +
                "n8/8kkflh52cQP2f7y6/H60rv4gezxg47ZdixnAmzjaKe78ZkK" +
                "1l+ePGMjedMbHy1EOr80/Uetr11hbr3/0vs54Q+Uhz9gra8Ad+" +
                "0Syx9199eE+v/KcT5s/wv8B9d/o/kRmJ9dvxv7NXLwH137lWNU" +
                "VHUNfmbnlzQP3/n8awg+gPwFGL9r/zKAbzKvf/mt1d8vp/r7S7" +
                "u//vOxte/ovzX+0M75x+NfFwBG5wP8ixh/MfyTV97SoU3818Pv" +
                "h15fobj4Aa9PuP/If4PzwfYJ7Y9XLvVf9vOzxO+6jl/P4neyxO" +
                "95JMVcceyvB5+i9cvwKb9/ctivGHyO+GHNv76p44fk8YmNv5by" +
                "hzr9/CL5v+b+OPjHP1Pwj3B/MD8p5X+s9ommgTC2vyfd9tRrj/" +
                "cf8aOR/KnP/6EnIT+v5u6HDtM/Mb8sHD9xfsAMtf/2+83P76D4" +
                "Bal1JP8J+wfygfk3qX9l4ZtCgg+QfZHhE8hfofoBcf0BR1fc8Z" +
                "G0Pcr/w/qAtqPyvM+iB1yl+blJj/l0iHoNBYMfD7A/wvFxfUEV" +
                "RE9aDKl//VH1IwH883n7uPx/av471L8t2H+G1KeQNX8fQ5QH8K" +
                "dy/WTqX4hcJ5O78sPqPD8s51+84/vzy4z5eZ8E/YvW58ov8/3T" +
                "W4CfqiCrmTn5k378b5jttTS+idCfwPsvqS9gywfzr97++fnNOH" +
                "4mlD+y2HeZ/VDcC5cH+jee/jLXX3S3NV3+6Mw+6XFutU+O/JBL" +
                "v93xnTc/UkTzS879NReT/wD5F4b9Ytr/2PomWf4jQX7BRPFLuS" +
                "++jlh/NL6qFptfEu4fvv9wfd78yslsfJq18Wmv/qQS3n8Zv4Di" +
                "f5x/5vPbyiLA/HPljd/Z/tdxzVf1s2l9aTHJT9EkP0VtfuoZ5A" +
                "+k/Hlc/illfB3D/2L+XiXj70H/84hYB+NvhB89/H2uctK66baW" +
                "N1NceVRPTJtmXaOiw6/4+QmnHM3f3x7yz8L3e7Bc6l+k7UH+Re" +
                "rfhfytnN8F/CuUB1Beg9iXRPxf7PtLUv5SqD8M/RTVp+D807D2" +
                "5+Lic8Q/xtILs+dXMs83Z/N7rPc7CqAfHrl0/Hh+7pC6+W+K4+" +
                "cQ/wbGZ8Rn6P0PIb+nI+cfyu/C/Mgg+YEI/ksH+edA/c0C+U/I" +
                "74D9x/W1yc43rr5zwfE1jp+rQeuHYXyP8WdA/aS1///1/g5dv4" +
                "7wcxx/TsnsZ4h9VGJ8kgp/p8x/oPhdGP9D/FF57zeK39nxffz+" +
                "y76/AOgvwv/i/DvGNynaF158U8TH3wnez+ewD776IJlcHB8x+R" +
                "0du75K/v7thehPJH4Oiv9L8NlyTpfF3z8B5Kh/GJ8K8WV8/4fE" +
                "qv+Q4i+Ij4TxH6d/Qfw/zPsPM9LWvpaqnsJGmZka36mm0/ttD3" +
                "b7mvT7vbBcFh/J84OLxd/D5/fR+2f/9fUD/ynMXwbzK4bjv7v1" +
                "Nf73ixA+APhdbr/760tQ/xpin5F86PwHul8D409pfhmcHzs/4f" +
                "JvA+MjKE9Rnyzyb379gfy9sH9Gfa0IH2L7GFgfGIoPQXspPli4" +
                "/xLyz5d9fpCfT8HfsfCN3RTw3++NrL+R1ocMzV9AfoHNTwxT/7" +
                "Lo90+Y9z/v3GmdrL04fx1VvxiSH07gPwsfvy3M7yw6fzfw+HHf" +
                "7xaCv6T5A8j/SflVnn0L4u8T2g9x/R7h/LxHzq9vibV/MP4Q2h" +
                "+AHy87v3QR43v2D9ZXS7+/Y2A5ii+58afTP0M5434K9HPx8Yus" +
                "f4Z99Npn6fni85fxP0n21zf+v0J+Nik=");
            
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
            final int compressedBytes = 4749;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtXA2UXVV1fgkZolmTAHUCtBKhVZKFEklNCaAuc+a9eTDaJf" +
                "6AFdqYSYIQxzCTTFaMIT917j33vjfz3hsaXeZn/GtnzIRaa5VW" +
                "XOiqGGDp0kREQpBAIBIUhKCFkBeCUrX7nHP323ufc4M0i5x17z" +
                "nn29/e+9tn7nv3byblZrmpv1Jumhb11/qT97mxafrA4Gnl5lBC" +
                "CHC+3hp9o9wcvlmdUm7WPlRuFgrlZtpWbsZLGoDEDYMYDKKM6x" +
                "VZvH+NTykUKpeZsbOBT83sB99fKMRXFNg/ymjGfO4idz4/eJ/E" +
                "pB1inou+tDd9/Cyxeezkh1bP5EKhek+hoL+Z1TRFRpXNYXpC78" +
                "RIqlt165+aPfT7YDuuH3YzGB+IjqvuZIu+AxHADuMo+qbqrm1W" +
                "U1R3JXFI2mb2jV5gPaO6QdMe63Ek2Zr5vlg7Ux/SfTB6CaMkn8" +
                "1sR5P/1ffrY+D1oN6vn2QZ/wCsKTQH5F5Yz2ej5wV2kI1/Vyhk" +
                "o0dgszz9qP6tzVfQNzLmXtiablz9ga1hsuoeKieTkreDZTfMW5" +
                "n1Ezxfht2nfw/7PyaT9UOqO54N2Lgah+zj2OvjODPz+NNqPN1n" +
                "EM6hVu9Qk9V442MOh/XM8MovyEcfGb7IofpF8/PsOgNGLyET1t" +
                "PZjhqbPma83D9kwHqOw3qyrMY2fEF01Nfe8vgdzm1FzzstsJ4m" +
                "n80vKm668dA7zB7Wc3z4KKxnMatpCs/q1w9Rfw8brGer3hVqRf" +
                "ILs4f+N7DtT/7HzWD8nD5HragWkt8iolaAD1r3wfbgLpgnz2bI" +
                "w3Z/TK0ofhz6446b7K19MbMfKBSS+ytvh9HPMUplJLM9ljye/D" +
                "J5KnkyOZI8lDxCGZPnYXuR5jB7NDk4/MGoKbBDbPx0cpjUOhWQ" +
                "+dfW1jTaeDXJA7wy5wHsK7i3rJz72whHkxda3FjF0MfYW0aM8+" +
                "gFFQ9PczhxqDm23oW+iCf9NC8Ual9yaHq64Ve2c2b1bIrkotGI" +
                "q5JZQdWi6CVfe+hBFWFEWKk+v2IeAbnJVX5mssv6C4W0PZ3R4k" +
                "5X06Gf7nqbdTo2fUDfAP0EIcR1IzfDkdvMvtiLI4tmEdLT7fno" +
                "VhFnguemfzIjnzvu8FdPpIsyc0WkxmnLiT3BdZiZzCxVcSxbT8" +
                "dtqAb0Dewto4Fz3aUaw69zOHGoOTbfXCsup3mhUB8jNpxJLuHM" +
                "6nxuQwVSRZgVVP08fpcZ61Koi+cmjYgUl/sV8wjITZb7mcku6+" +
                "fKAVupVkK/EntYww+4mR1/UK0cXo12zkW223Ob2Veno8309e+i" +
                "3X7e38SjVL/DbUzdSpmH5o5b3B93SUzafV9SU3ljfmxSb9cz9T" +
                "NLDTwTKjZbtCY6JfkJXaOZf9EnounRixEcl3FP1FE/FZAz4Xrz" +
                "9dB/EtY4gvH5MF4XzdGf0oOA7IreDPMLYXur9Z9X7XbRooujS2" +
                "E97+Ar1bUf4i4F2+VRd3RFtRm9O/pbskY30fVn9JFocaoAW2ot" +
                "10Ufi3qjSdFkM6sNxFdnrBnR6c4jmgnbn2vzvb/WXH9G58K6/N" +
                "j6XsDzR2/L+JdlOd8ZqagYj3GVSY3Ww3Leh7rSW1r6FvF1iz4K" +
                "2/WqV/XCuvZib1e6F+e6qnprDxBCXM7m/m5ffowiUcSsnp/xKL" +
                "VZ3MaOz16Zh+aOW/u3eJnEpN33JTUmf17seIxXlXzPzyw18Eyo" +
                "2DJSlUKfYm8tKc71sErrlzmcONQcm2+uVW+jOUXMPu9zOLO2gN" +
                "tQgVQRZlVp8cF4ta899KCKMCLkn+1X7Mawnswj+b6fmSLK+rly" +
                "lXbN7ZoLP7O52NsjaC7O9de65tbf4XDiUHNsvrk29BjNKWJ2fD" +
                "Y4s1bkNlQgVYRZu+YWfxav87WHHlQRRpT5eex4jHske/zMFFHW" +
                "z5V3zVXb1XZY1+3Y25XejnP9X2p7vd/hxKHm2HxzbXgpzSlidn" +
                "zezpm1KrehAqkizAqq/j3+R1976EEVYUSo6T/9inkE5Cb3+pnJ" +
                "LuvnygHrUT3Q92BvLT04Lz+uemq/JoS4nM393b52LkWiiNk3+u" +
                "M8SvkpbmPqemQemjtu/db4UxKTdt+X1Jj8ebFJvV3PP/qZpQae" +
                "CRVbRp/q0z81e+j3qT74+T3sZjA/UDxT9dVH9B2IAHYYR+DdZ+" +
                "P0VRJC3F4/YyPtcXOHu5E+1HkpMk2r3cVs99u9ex7SJ/PQHKLf" +
                "C+u5Jx4R2EHugXz9CPrqR1GdqanltZdik3p7zzMJrLt5Zv0Ez5" +
                "dh9yFfP6T64tmqz1wvpeviyemawR+1zvxwvVQ5z1wvFTvgeulb" +
                "5noJNnO9BNes0XmwnW95uddL0Ug0z1pvxusl+a/y4yyLvV6q3S" +
                "2vlwZ/wq5RzPXSJ/Kul6J6/VD8zxkru16C0UzY7PXSIORw10uZ" +
                "hV0vQU1vy1BxvSRVpvPjSUzJlXi9xJTe466XWhx3vTSqRuH4HL" +
                "X9PjVqj8/RbH5A36ZG4y/D8Ql4hh1Gq2n2SBiF49PakQOsZ8wY" +
                "jk/LiXcSWx+qzOPMeJzZ7rde9vgkhoxso9+rRosPxBNotdhBrg" +
                "pxOD4zjfb4tLbKRcxrL43h/N7yBc23gHW3qOkJni/D7sPVhONz" +
                "FI7PUVVWZeCUsbfZyzjX31Ll+u2EEJezuT/iFIkiZufBOo8ytJ" +
                "rb2PdnWeahOXLj/5aYtPu+pMbkz4vN1cPxudDPLDXwTKjYbOY6" +
                "vzKpMasyLXoNfzaePW8+jM+go7OBd6qZRedFf4VPte3ndyo+m+" +
                "7cSE/Do/kUCThT2JP3w/wJfG03Pp9HNPv0fDi61lgab3BY5bXe" +
                "8/mN8Q+ANS36OCLRGaDyrJbeWe75vIzqPDP+AkQpKlcZvYd/3r" +
                "M6JkdXZcg10Y1ZnB7KUW6qMQX3WWbvervSYzjXdyLCOdScTe9C" +
                "X4m7OUXMvj+f5szaH7gNFUgVYVY1Vrky3u9rDz2oIowI+d/rV8" +
                "wjIDe9PKwpv36uHLC1ai30a7G3lrWulWaWZiLCOdTIxjnShyK6" +
                "vC4mMuuv5TZUwFXIyITED/vaQw+qCCPK/GFlyE1XhzXl18+VA1" +
                "ZUcGYze9dbSxHn+jmJEJez4fhkNoeTH/ln67mTR6kPcBtTV5R5" +
                "aI7c+DmJSbvvS2r0s/mxuXpYz41+ZqmBZ0LFlrFFbYF+C/bWss" +
                "U1faBrHBHOoUY2zpE+FNHlrc7gzPpqbkMFXIWMTIg+1dceelBF" +
                "GBHOR+N+xTwCctPPhDXl18+VA1ZVVeir2FtLFeddY4hwDjWycY" +
                "70oYjZ+aDJmfWE21CBVBFmNZue6msPPagijIg1hbGlR7ozrCm/" +
                "fq5cVfm7a//dd2lGaQadsf333PzdNt/42VS+t84+7zM4Vv+NPL" +
                "+Hb+Dz37/Dek6T2n37id6/u/xhbFJrz5odfma5DuE1QXZ+71Ad" +
                "sK4d2NuV7sC5/gtEOMdn841wjEQRs897g0dpLOM28fMWeXBGXP" +
                "16X7u0+xoRSfv9inkE5KYr/cxk9zNhPmubquD60exNb/Gp2OCz" +
                "8S9kRYyP3AxHbpMj6jF+5zEep3EDtzF1U8M8hNj1PD9fl8xIil" +
                "ANr4nHJrX2+PyOn1mq4hgqtvYBNQD9APbWMoBzPVsixOVs7o84" +
                "+ZF/tp5HeZTGILex9RyQeWiOXD1HYtLu+5KatJkf2/XVEcdNj/" +
                "mZpQaeCRVbxiIFd/Vm73prWYTz8q8kQlzO5v6Ik5+L4kYWm8+j" +
                "NBJuY+u5SOahOXL1FIlJu+8bqvFjc/VwfD7lZ5YaeCZUbLb4G/" +
                "Gt/MzBzzDmfRxVGPfZ3+maZ/bx1zlb76JxvAQqvbZ1n3ZbeEdX" +
                "uQJYS/37u7zzEe7jxfKOMVvPv6aY/hnGnY9kFKyxcjly9SoZV6" +
                "yBCs+z/vkoT7Xa//9reR4h5hDEyR5mRKbpac9ZDvN9YD07T6TB" +
                "jQfP5b4YMy+Sr9meNa/xM+dXzhVbRrtqh77d9RZvx1YoJDNVu1" +
                "5MCHHdyM3g/siOOje4GGhzVoqZnd/v5nHicW5jP+f2MA8h8C28" +
                "Qffk65IZSRGqcSrD2PEY16GX+JmlKo6hYmsfUSPQj2BvLSM4T8" +
                "5GhHOoORusp8U7b5K441LEbD2/yKNIGyqQKsKsJpe+ztceelBF" +
                "GBHW8ya/Yh4BueVOPzPZZf1cOWD9yrxv68feWvpxXkwlQlzO5v" +
                "6Ikx/5Z+v5iB+FbExdv8xDc+Tq5RKTdt+X1Jia8mJz9aByr59Z" +
                "auCZULFl7FA7oN+BvbXswHnyl2pH/GWHE4eaY8PxuYNzEHdz+3" +
                "y+hRYKQ+2cCZ93ZkMFUkWY1Wx6wNceelBFGBFqOs+vOFMyxj30" +
                "6rCm/Pq5csDWq/XQr8feWtbjPLkQEc6hRjbOkT4UMVvPDsnkNl" +
                "QgVYRZzaY/42sPPagijAg1vcWvmEdALlzPBzXl18+Vq/XuvD80" +
                "C64vfuQ/ny+VS2V+35oucc/n5VVIeo1/Dy/vdS3n7yhvqZwu5t" +
                "dQ8v6dv4+zvyO0xEVKlwW/Uf9k+mF5lUNXNu59nPW7nj9RSHtM" +
                "fotfl8W9ipSk/5B+BHMPXTB0DiBL5fP59Gq7vzZdZN7H5d6/b1" +
                "PbYF23YW9Xeptr+kDXXYhwDjWycY70oYjZ+6O7JJPbUAFXISMT" +
                "ov/D1x56UEUYUeYPK0Pu0IVhTfn1c+WAlVQJ+hL21lJyrXRa6T" +
                "RVSs4ghLiczf0Rp0gUMTs+T/OjkI2pK8k8NEdu8mcSk3bfl9SY" +
                "/Hmx4fuTVTV0iZ9ZauCZULHZ9MXhGyn63OhL7H4B//TKuxD+2a" +
                "+8P7s/+vuW//zw/mjoDX/63kh+a+TfHyWvy3uORnpOeH/0Xj9+" +
                "3v1RZ3v+/VH4tE4qU19QX3B71yNmWqm91I4I51AjG+dIH4qYHZ" +
                "/tksltqICrkJEJSTp87aEHVUS18vxhZcgdemdYU379XDlgg2oQ" +
                "+kHsrWUQ5113I8I51MjGOdKHImbfn3dLJrehAqkizGq25Exfe+" +
                "hBFWFEmT+sDLmd08Oa8uvnygFbppZBvwx7a1mG8647JUJczobr" +
                "T2ZzOPmRf7aed/pRyMbULZN5aI7c5CyJSbvvS2pM/rzYXD1cz5" +
                "/jZ5YaeCZUbBmb1CboN2FvLZtwXj6iNpWPOJw41Bybbz7OI2br" +
                "+SXJ5DZUIFWEWVFbvi6em7QgwvOHlSF3aCKsKb9+rhywhWoh9A" +
                "uxt5aFOE/epBYmswkhLmdzf8QpEkXMntf1+VHIxtQtlHlojtxk" +
                "jsSk3fclNZUb82Nz9bCe3/YzSw08Eyq2jHVqHfTrsLeWdTgv/x" +
                "IRzqFGNs5BXO+hMY6gnjWSyW2oQKoIs6K2fF3kQRVhRDg+V/kV" +
                "8wjIHZ4D+neHSsP6uXK1zn9HKN8sJhe8+u83K2sldrLvN5M3n9" +
                "z7za6BV/J+c/gtL3fP9zLvN7eqrbCuW7G3K70V58lFiHAONbJx" +
                "jvShiNmV3eckk9tQgVQRZrXarva1hx5UEUaEn+cn/Yp5BOQOzw" +
                "tryq+fKwdslVoF/SrsrWUVzpO/kQhxOZv7I05+5J/9PutuPwrZ" +
                "mLpVMg/Nkau/LTFp931JjcmfF5urh/W83s8sNfBMqNgyNqvN0G" +
                "/G3lo24zy5GBHOoUY2zpE+FDH7vG+WTG5DBVJFmNVqq/raQw+q" +
                "CCNC/n/yK+YRkDv8+bCm/Pq5crX55X4/BDQvePW/Pzt3vkrfn0" +
                "Mn9/1Z+dwr+v685SR/P2Sj2gjruhF7u9IbcV68CBHOoUY2zpE+" +
                "FDFbz89LJrehAqkizGq25BJfe+hBFWFErCmMLT2GvxLWlF8/Vw" +
                "7YBrUB+g3YW8sGnJcPIsI51MjGOdKHImbPQ94omdyGCqSKMCtq" +
                "y9fFc5MWRFxNYWzpMfy1sKb8+rlywKYp8/8FTHO9xadhg2PgMr" +
                "IixkduhiO3yRH1GH/o3TIntzF108I8hNjP+135umRGUoRqkktl" +
                "dqmk9Xk/7meWqjiGiq29TbVB3+Z6i7dhg8/GXLIixkduhiO30Q" +
                "iu59sQofjVLTInt7H1bAvzEGKfktfzdcmMpAi18pp4bLS3fn+p" +
                "Da7nRb1SBWGo2NrXqDXQr8HeWtbgvHwzIpxDjWycI30oYnZ8Lp" +
                "dMbkMFUkWY1WzpNl976EEVYUSsKYwtPWqzw5ry6+fKETnR8+Ty" +
                "0xKTz5P1u8InsuZ5cnK7/5SVx1f3vDrPk9PbX+nzZH6VgTX9qe" +
                "fJtQ+c3PNkbd7KTdh3Tq9xvZpoVT4RvWD/Jw3Ao7PhM3AqXA9M" +
                "mL+XEX/vNtX52b9SORbb92fAmo9x6e9lTFwXjXmfz37CWX7TzN" +
                "/LtH5zarH5exnOc5Hs38v8qqX1DJif1frNqVlmPSlXOIoWcFSO" +
                "YT1viN5DSHSls8u/l8k09Ajv/wPz4GoV");
            
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
            final int compressedBytes = 3839;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdWl2MXedVHaCiNn7BDWlEEzc/NhIvmKpFASwVfb4/dW1Df4" +
                "AH2gdrItuyZVnItjDBgtq5956ZufcmqPJPE7BjTxwrIChCiD64" +
                "UUByKSJtKbQhIJCqCvHQJsBDjTSBClTg+zlr1tr7OzOtPEfn7m" +
                "/vvfbaa3++594zZxyeCE8sLIQnQmsXFuCldTOwEWIVrfWIs471" +
                "C/ln7+97Fub4YzuqD+zyz9iYzftaqmn63dyqfmFh/rzvbDVoJy" +
                "jOiH1hX7T7YHNmH/zeQzZCrKK1HnHWsb7t+5pnYU72c5/tQx/Y" +
                "8cDGbN7XUk2aqYtb1cf9fNF3thq0ExRnxLVwLdprsDlzDX7vXe" +
                "Ha8qdKnBgeBa2njytj6Tv9hEVqDgqsirprOlfe7rXXFZwIjGUm" +
                "O3FZj29qxfzr9Uzd86vyGGtCE20DmzMN/OYDoWlOlTgxPApaTx" +
                "9Prys/xmiMfVmRKzs1BwVWRd01s+7y2usKTgTGONPQT1zWcT+l" +
                "Yv5GPVP3/Ko8xo6Go9Eehc2Zo/B7O2yEWEVP7miuxFnH+rbv33" +
                "oW5kTdUduHPrAre2zM5n0t1aSZurhVfUR9v+9sNWgnKM6IW+FW" +
                "tLdgc+YW/Obnw618vRsMj4LW08eVsfTtfdkiNQcFVkXdNZ3Lz3" +
                "rtdQUnAqPtr9zLl7Ri/mY9U/f8qjzGLoQL0V6AzZkL8Hu/iYhi" +
                "eDCnGFtDxrbvVy1Sc1BgVdRd07kSvPa6ghOBETPV3LZi/q/1TN" +
                "3zq/IYezI8Ge2TsDnzJPzmQ4gohgdzirE1ZGz7fsUiNQcFVkXd" +
                "NZ0rfa+9ruBEYIwz/YKfWBmAffpd9Uzd86vyGDsfzkd7HjZnzs" +
                "NvfhERxfBgTjG2hoxt33+wSM1BgVVRd03nysBrrys4ERjjTB/1" +
                "EysDsE9/uJ6pe35VHmNbwpZotxSb41twxGvjx5lFTFfFw6qcdk" +
                "W7zv+67ak5Ubel7sNIvv9c7dZlO1IR1OhMyk21eT8/7jtbVRqD" +
                "4pw/Eo5EewQ2Z47Ab47aCLGK1nrEWcf6tu/feRbmZD+P2D70gV" +
                "35gI3ZvK+lmuZIN7eqj/v5676z1aCdoDgjDoQD0R6AzZkD8Hs/" +
                "ayPEKlrrEWcd69u+f+9ZmJP9PGD70Ad25WUbs3lfSzVppi5uVR" +
                "/38yu+s9WgnaA4I66EK9Fegc2ZK/B7P42IYngwpxhbQ8b296Pz" +
                "Fqk5KLAq6q7pXHnVa68rOBEYMVPNbSue6Zipe35VHmPLYTnaZd" +
                "icWYbfey4s59+PDIZHQetZjt5z9MnY3n8+p0ibgwKrou6azua0" +
                "115XcCIw2v7KHX8/kopn7vOdyWjnV+UxdjlcjvYybM5cht+7io" +
                "hieDCnmHC5d5U+Gdv9vKpIm4MCq6Lums6VL3jtdQUnAqPtX09G" +
                "Bb4z83Z+VR4uD3YPdi8spNdiU6Z4ad18AhHF8GBOMYPdzXn6ZG" +
                "yv9wuKtDkosCrqrulc+Uevva7gRGCMM/2Wn1gZqMB3Zt7Or8oH" +
                "u8ONcCPu6w3YvNM34DcNIorhwZxibA0Z2/18yiI1BwVWRd01nS" +
                "v/5LXXFZwIjHGmiZ9YGaignql7flUeY9MwjXYKmzNT+L33IaIY" +
                "HiU3uYNaGy8+Gdv9fNoiNQcFVkXdNZ2TB7z2uoITgREz1dy2Ah" +
                "mvtJ5flcfYIAyiHcDmzAB+sxwGvdcZIVbRWo84mcjYfn6+7lmY" +
                "E3UD24e+8nTpsh2pClbrlLvEoZxTdHWwnRQf9of90e6HzZn98I" +
                "f/bCPEKlrrEWcd69t9mHsW5mQ/99s+9IEdfsfGbN7XUk3q38Wt" +
                "6jlXrdQrsfhwOByO9jBszhyG38zC4Xy/dJgxXRW01iNOJjK2fz" +
                "/6trKs7NSc7Odh24c+sCu7bMzmfS3VxM/PTu54vyRTcYquDraT" +
                "4sOJcCLaE7A5cwJ+87SNEKtorUecdaxv/472ec/CnOznCduHPr" +
                "DT99uYzftaqln+i25uVc+5aqVeicWHc+FctOdgc+Yc/OaZcC4/" +
                "TzYYHgWtZzmaj9EnY7ufX1ekzUGBVVF3TefK27z2uoITgTH2/5" +
                "qfuKzj+1Mqmo/7zmS086vyGJuHebRz2JyZw29+GxHF8GBOMWE+" +
                "fJQ+GdvPvUcVaXNQYFXUXdM5/Ruvva7gRGC0/evJgAXKK63nV+" +
                "UxNguzaGewOTOD33sUEUTHAdmCQE4xtoaM7efn8xapOSiwKhTP" +
                "yHiv115XcCIwYqaa21Z0zeT7UKvMuzVsjXZrsTm+FUd8f/4Zs+" +
                "WI+7meRQ6rctoVLfj3/o7tqTlRt7Xuw0h+/rnXxmxea62a5hXb" +
                "3SpRBbazVaUxxYdRGEU7gs2ZEfzeLkQQjfs54sGcYmwNGdv9vG" +
                "6RmoMCq0LxjMT9dNrrCk4ERsxUc9uKrpl8H2qVeRfDYrSLsDmz" +
                "CL+300bC4vi/mEVO6xFnHevb/bzBCq11789F24c+sONv25jN+1" +
                "qqSTN1cat6zlUr9UosPhwKh6I9BJszh+A3d2yEWEVrfXkd7mAd" +
                "69vvox2ehTnZz0O2D31gm8/ZmM37WqpJ/bu4qV5V1kq9EuhpJ1" +
                "8brsXqtWIz0xqOqPnzzCKmq8jwfVjhzIid66s1crb8O23P9PrU" +
                "R/x+2o5eQ97Pv+zWhY5PPayKqKZoq7mpFiptZ6tKY1DMdfZfqa" +
                "O9D/rsOIweKeuun8lPjRdbjg0xe3934Xv4Gb5ChvGhLsR4r0Wl" +
                "lXpPPdzN3Pvg5HLbYW1zBVyPPuSj7GwnDe8I7yivxSJW/OZVRB" +
                "Tj0XoyTnbW5nm2ehbmvDLywQO2t3X6mteueauFKuJMf+UnVgaq" +
                "953tXtWqS66+3vUd3nwpviNv1VcFrxV7rdsrAFfQ+PeUf/rneu" +
                "2MX+y+3n2f+nqfnK4/NzS/0fXefLH7eh/f1D3wc9lX20nx+snQ" +
                "Mcm54Vp6HrL5fk7ubL6f0/8w+/lNZZl+6173c3r33vYzzXRv+7" +
                "mREsWHT8bjpfyabXzPwot+81qOlvgnDTYf+X2OOmJiBPEUnb4h" +
                "aMu2viq5tuqldrWOEGans0uXVLxEjSUS359ftVWWodTauXxela" +
                "xf7cCeDCejPQmbcyfhD+/aCLGK1vryOryPdaxv/x3v8yzMyafR" +
                "SduH/jrPXRuzeV9LNal/FzfVq8paqVcCPS0i/kQbYHMmwG/+20" +
                "aIVbTWl9fhY6xjfbsPj3kW5mQ/g+0Tgkby/dL/2JjN+1qqSf27" +
                "uKleVdZKvRLoaREHw8FoD8LmzEH4w2+EgzOJEKtorUecTGRsP/" +
                "feVJbp/2lO9vOg7UMf2MnLNmbzvpZq0kxd3OObOhWn6OpgOyk+" +
                "nAlnoj0DmzNn4Df/G87MfpgRYhU9uaO5EicTGUvf2Q8oy2y75m" +
                "Q/z9g+9IGN+3mmS5ftSFWwzXe6ueN+ylScoquD7aT4sC1si3Zb" +
                "sTm+DUf8t5wzi5iuiodVOe2KFvyzd9qempP93Fb3YSTzPNity3" +
                "akIqjRmZSbaqHAdraqNKb4cH+4P9r7YXPmfvhL6xHFeLSejIOJ" +
                "jO0+vM+zMGd21PSBJzwPee027zUi0kz8xMpA9b4z876T4sOlcC" +
                "naS7A5cwn+0g8iohgezCkmXBo+SJ+M7ffRg4q0OSiwKuqu6Zzt" +
                "9trrCk4ERtu/ngxYoLzSen5VHmPHwrFoj8HmzDH4S2+3EWIVrf" +
                "XldbiLdaxv93OXZ2FO1B2zfegDO1u0MZv3tVST+ndxU72qrJV6" +
                "JdDD2ul7pj85/Ynh7Wn7/3FGf7r+HOAz8fo4lflv43eA0SNl3f" +
                "Wz9Jn0PKT5LPBdP4M/Vm/6rfzd8nz129FtMnQ/D5netai0Um+j" +
                "5yHoP7zdvGyer9z0CvibjzwPue312UnDC+GF8losYunov7v/7v" +
                "DC7EqJI4ZsqUBOMQmV1gVLxrQqnERqDmxURlW2azrj97vTXldw" +
                "Is6q/ZU7fr9LBfR7pV6J7iRr7VO60WcRnXw4XlfP5nfQR/T314" +
                "2fco0X69/Drb/8TfVm2zd8Xra2+ftz8rJ73rhm59jo/bn8jW5d" +
                "1ftz86d5G+xCuBqultdiEUtH//H+4+Fqvp9fx6QIj4X1nGISKq" +
                "0Llox5F/4lRYnUHNiojKps13TG96fTXldwIs6q/ZU7vj+lAvq9" +
                "Uq9EdzLnzoaz0Z6FzZmz5Zh8rR8QQXTyb8iWCuQUE1H/ntapWh" +
                "lL38JJBs1BgaqwzIzM/sRrrys4ERhtf+W2FdDvlXolsp85t9nz" +
                "uv6e/h7/nCpFNnv+WV77e/LO7en6ewc5N/t7x3d/Xje7c2/P60" +
                "r/7/b3jjLFPTyvux6ux329Dpt3+no5+g/1H0IE0RThwZxiEiqt" +
                "C5aMeTdbTjIwBzasVJXtms7Z57z2uoITgTEp8BMrAxUU/V6pVy" +
                "Lvz5I7Ho5Hexw2Z47DH5yyEWIVrfWIp3Py18VnPt+vnPIszIm6" +
                "47YPfWBnd23M5n0tYqV/F7eqL+jJl7qUeiXEZ8TFcDHai7A5cx" +
                "H+4HS42FstcWJ4FLSePq6M7X6eVuTwTc1BgVVRd01nb9Vrrys4" +
                "ERhtf+UmH5XYmbrnV+UakfslfHPs6O9YWJi/l9nRH/Z3xPv5De" +
                "4kRn/U2vf0d+RPqh0u/weFc3r9e/j7pvt0mpq7mdGn0/f79Ib9" +
                "1Ld/adnofgmqCnb64sYKqF/u59e8PqszbA/by2vYzgj8pR8J2+" +
                "c/xwixZcV6nnZFC/7Bp5Vn/n7NWV2+DyPt87pOXbYjFUHN8hs6" +
                "sV2rDt/ZqtKY4sMkTKKdwObMpBz9h/sPh8nSO0scMWRLBXKKSa" +
                "i0Llgy5ndH5iRSc2DDSlXZrulcesBrrys4ERiTAj9xWcf7T6mA" +
                "fq/UK5H9z7n0vl36jfGvLv3aaIu91tK59KP1PdHokdFj9d8xN/" +
                "v7pvv/DP+p90Mb3y+NPpa1ndW7Hb1LmT8RMT/EHqPt8ffsB9iv" +
                "3C/V12TvVIt/3P6PmKr/Af3/DD4/+pXRyc77pdWQvm9WYfNOr8" +
                "Jfem9YnT9b4sTwKGg9fVwZ2+/lX7JIzUGBVVF3TWe83le7dWlv" +
                "akGkmfiJlYEK6pm651flYXX41mbH+AvDt5Y/pZH4b1Ch6liJIM" +
                "78+ItxP3+5C7n+r/wWrCJshxJZedtGGsp6/KrWgjO+P093c49v" +
                "MgIlyts9eYX/f7AfwKc=");
            
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
            final int compressedBytes = 4133;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuMXVUVviCgFGSAIuDY0qoBKmlLkVelr33nzC0Ibae29m" +
                "1bBC2ggJWHBoK0+9577p3OHSZtDUgiD6dQ4j8jIjhDGfsDMZmx" +
                "9Idp4g9jQsBEKVhDJm3axMS99zrrrLX23ufOoCbek/361rfX+v" +
                "a655w759GqATVQKqkBlbWlEo5sv/KuGuh7GnDi0AZsXnyceyy5" +
                "z66tnNm3iNtQgVQRRrWlNqQG4rp4bNKCSNd9/oq5B1IQrim+fq" +
                "5cDegz9CdKJX2mPkt/CnA9R5+jP6079NRSKV2gLzLIdfpifan+" +
                "nJ6mLzOjmfoL+nJ9held6fizagf1Vaadbcpch8wz5VrXu17Pd+" +
                "0ivVgvyfL5iK7ouwy2VN+ibzbtV/VtpeCj1+kNerPeom83/W+a" +
                "coe+U9+t79Gn6dOB0Tesz9ZT9Ln6PH2+vtAwLtCf0Zfoz+pON3" +
                "/6zhl6hmk/70ZfcvXV+hqTz+/pL5v+jfoG/RXTLjBloVa6y7SJ" +
                "7mYKbjVlmSnL9Uq9Qvc47Gt6tf66XqPX6vV6k/6Gw7Zm/G+Z8m" +
                "21TW0zed2Grcv0NhynCyVCXM6uHeQ2wGkezc/y+YTvhWzs294m" +
                "49AYuX2vS0za/bmkpnlm3DdXD2wZWWrgkThfbVabTbsZW2fZjO" +
                "N0sUSIy9l8PuI0j+Zn+dzreyEby+dmGYfGyO37i8Sk3Z9LatJF" +
                "cd9cPa0rVOorkXy1VC017VJsnWUpjruWS4S4nM3nI07zaD7ELb" +
                "/geyEby+dSGYfGyO17T2LS7s8lNXZNMd9cPa0rVOorkXzVUi3T" +
                "trB1lhaOuzYgwjm0kY1z5BzyCHHBJzG5DRVIFWFU1BbXxWOTFk" +
                "R4fO6b/JGSUGm4fq7cYGVVNm0ZW2cp4zitSIS4nM3nI07zaH62" +
                "fw76XsjG1JVlHBojt++vEpN2fy6pSbvjvrl6Wleo1Fci+fVf5S" +
                "tYDcX8Sp1ErGs1tzbOsrWeqTK0/pqtG5/0c1H/DXqqvwpI4wyy" +
                "lvd57GFTfh3kc7XKY9dfMR7O9hnV5yWL9Pvz5adrdf1lYNSHYn" +
                "acx33pFdA2TidryMPPLpPTXS+rNca2xuXzX3nsNbnPNVibfK4p" +
                "Tfgp5nRNYradLz3sesVnpLdIFumPzQ/j+/b0Zo5Xha8snywC+Z" +
                "d+qgvyWWuhGOzG3HqTKS+gFWqTz7UxndWFWXuDuQ44yjnVlziv" +
                "crT3WWbbV5DPtTJK72AQb3/vc5ZVne/rj83n8Ylv/Obxq4Mcl7" +
                "6yfK7l1pCXa53Xe3XvnMp471y1ynmu5Mfmm+Y6ZItTMe7mrrL5" +
                "hH77TzGn/LzIyr6i+RNFqW2VLNvjI1hLJP6bcYV5Plf5drUqy+" +
                "cqQilyqBOO98oIWqpn5qo+yHsjmZcPTD5HJszmB5xDXtx69ktm" +
                "gYcRGSU83u1MzrI9Pto5o0gbX1GxEm6HfHKUIhdlo9ws329rKN" +
                "k5pU5WV99fVXom2Ys/xZxykx/vxSzpIXK8l3uf4yypHFbTThm0" +
                "vfsmE5/ySbOlH35cZcf7cO9cvi/bUn4L9lmDDAOW3m32z6wfHm" +
                "vITu+rDJOn9F6h9EWKYpj32HrnSv88YWIMY4RcQ7Zl3/V3PdYw" +
                "qgS9O2dIfXn8t4gvlNzl7Z/DPJ+ki+sTiFt/cjI5Ccc73g8plS" +
                "xma6P5+8nJvk7br/4DMXs/BPrJSZpBJWNdS568b/5nFMV4PwUt" +
                "94T3QxC39a5XgEfcmvlrR0/R30FEX5Cc1JfgDD095tV9D9sz/g" +
                "2IgteqdwTY+yGUz1xX9teRXq/vBaz6Ic+B6lE92Vm3Jz//OsyW" +
                "8tuqp74d+liQSzOlHXHqt04XZ8IXKYrqaWUt9yR1IAY80tY6Tb" +
                "K4wpi+/Pt8218xeK0Otv37rSdEJJbFXKmyY03lxxxgtqSPAIpj" +
                "zqWZ0o64z8FP8xKKghzpSepADHhM26OSxRXG9OXxL/ZXLBUU5H" +
                "NliEgsi7lCZedalZ9zAbOl8hGgOOZcmintiPuc/Lz0EUVBjvQk" +
                "dSAGPJzTvIz7kSri+vJ8TvdXLBUU5HNFiEgMYiYnkhPZOt/Izz" +
                "YOsyXdlZywx3tyovIGYpYJfZwJONltDd6A0zpXXJ8coCjJidYU" +
                "aLkniGGjkD/kI9Z1oHWOZNkeKEO9oVeYyVdBnv3jnfIhdXF9HM" +
                "lyMp6Y3yVbQ+ss4zjuno8I59AGttpBnCtxGJPHbD0jksltqECq" +
                "CKMm410jrem+9nAGrQg9yvjhypDbnBauKb5+rjwZV8vV8mx/XZ" +
                "7vuQ6zpWsRoDjmXJop7Yj7nPz36BcUBTnSk9SBGPBIW+uLksUV" +
                "xvTl++cif8VSQcHxvjxEJAYxk1PJqWwPPpBn2mG2pE8kp9zxfq" +
                "pyADHLhD7ONPtnZgPE1uCNOCyfv6coyalW1lJsVGOjkD/kE9f+" +
                "vnOW7YEy1Bt6dX8v9fNVkOfgeD8g9j4vSxCZI7B+tUwty/K7LM" +
                "+0w2zpWgIojjmXZko74j4n515HUZAjPUkdiAGPtKUDksUVxvTl" +
                "++cSf8VSQcH+Ga7DmwMxk+PJcZPX49i6TB/HcVcZEc6hjWycI+" +
                "eQx+w+8EWSyW2oQKoIo6K2uC4em7QgAmsKfcsZsTXF18+VcwSu" +
                "nrT5tdIP599lUirZ4z23jrS/d5RdO8xrx0le56PWlP/0/pL7fW" +
                "9zf6nofohdEzDs8+Lw/lL7O2QT3V9SU9VUqKFFDMbpHjXVXm9K" +
                "js/mhXDyTnPd/nk599LS3OYrI384Im5tyNcu7b5GRNLd/oqhXx" +
                "2U6v3IMleh6szWoTpM2wGtwztwM7GfUh317YQQF3owwh4U2bNt" +
                "q8n9d/2W+2ml3MbUdYRxCLFeWo24LoosFaEaG1/aoW/yKXT4ka" +
                "UqjnG+fsh/P8SM6P2Qp/VF9njXF5vi3g9JmuH7IaZ474ck7h6W" +
                "4br3Q1p76f2QpGlQd78a3g9p7Y6/H2LsE7wf0tpj3w8xluj7Ia" +
                "VS0fshJn70/ZDqIH8/xLBuNSV4P8QU7/2QXK97PyR2vqIzUe0h" +
                "PH/y+3j+mYqfXaq3F50Nkde8ks9vpe3v8Nu6uiV2Nms15DkMYy" +
                "AG50//jp+JfwXXQ36rg/Kcze9BhnGIW3tYHFcL1AKo1QJCcJw+" +
                "Q1bEeA9GtYM0Ag/Uoxb9990qY3Kb1OXHIcT9HflsXJeMSIpQTf" +
                "pTGV0q4QpkZKmKY5KfjRarxdSHYr6LD/n+aTm194Ep2X6/5p55" +
                "JY/xmWhLHuPzW7sL/+JbzD3yKHi8k0/wKJXx+Vjb1qoiPeTXnD" +
                "9ZDqxKUhrG4dyo+oVqIfWhmO/yOZ+TpMCUbL+fpFTjTLAlqfUg" +
                "ZxUp4h5Dfv8M0gwepTI+H2vbWlWkR6qnkVVJSsM4nBs9+/Pfo8" +
                "dNcb9aZXFuML9HO+H+vKmvyNFZjDG3wDt7X7HvNvt7pHdMlE+9" +
                "wdXZ+VjfEeRzJvweOWv+e2TaTn7+DH+P7Jq0tr9HDmNHeNH7iv" +
                "T8SP4eRf+6PpwchhpaxGBcfgARzqENbOb6/TDnyDnkMcvnMs5M" +
                "X+A2VCBVhFFt6V/iaw9n0IporbCm0LecgRapI7Z+rjw5HLsOod" +
                "9J9Uv6FfR/56pPxp/H8V/OmPe+e/lvvo3An8fFrpHC53G29C+P" +
                "XalQvKLncbgmea0z0dVYaK8+JbHsedxoMmryOoqty/QojtP9ya" +
                "h7Hic4tAGbFx/nHrN8PsCZ/ddzGyqQKsKottSGfO3hDFoRejRr" +
                "etFfMfSrg3xGbE3x9XPlySj7dkfC/TP9eWXc5tMgI1n+d7hn+d" +
                "7fbv7+meyw3kwd+d6THXyv6L8pvn/a9wXaPy+2zzcFawRV4vsM" +
                "8f3TqkI+VyKv3+0Kkh2R6/YRrk8gkJ+xZMzMHsPW+RrDcfoqIj" +
                "mq0QoMtAmOYbm+Jg72au9YlMcgG3rDHlclo9rSv8LXHs6gFaFH" +
                "U2t/xdxDztWg31fqK2HfgLOx/XMosn++lu8bQ+x9haGJ9k/g1M" +
                "ai++cGedYq2D+HrIe2588ejzWEKmFUuH9ukKuInz+bsytDtdHI" +
                "/jnE9QkE9s9DySET4xC2LuIhHKevJ4fc+TPnJHW0AgNtgmNYrl" +
                "8nTt6rW5SY/VvJht6wx1XJqLaY86enPZxBK0KPToG3Yuib8yef" +
                "UQf9UkeohH1PYDuSHPHPn4DZkh5Mjrh8HjFnE4clNcuEOTgTel" +
                "Qsy3KSGnnPmTXrAaMkR/rvhDZ2/kQca9wAwfMnsmzPlloXjqQ+" +
                "piDncyXe+dOuoMbGR8LzJ/qROXB/Q6yzW/4XxTooJp9vmT1/Hm" +
                "FqXdIApmTL2vht2F7SIA76Txpgm/DqaJ30GM5pXkNeUR0p8y2E" +
                "gSrfv7veZDmwKkEpXy3EwC2mTF2lroLatojgOB0jK2K8R/OpyB" +
                "616L/yZxmT26QuPw4h7vqoN65LRiRFqMbGl3bugSuQkaUqjnG+" +
                "mqXM9aKtVXbdCH0Yp38gK2K8ByPsQZE9atF/83oZk9vYamaFcQ" +
                "jxOZLBI5IiVNO8TkaXSrgCGdmPSJjku/5Gu+WeNkIx+fyjiX8j" +
                "YWpj5RgwJVvWZh84ZnuVY8RB/5VjYJvweN8oPYZzmvPJK6ojZb" +
                "6FMFDl+3fHO8uBVQlK+WohBm5Fygy2wW40gmLy+SeJ8ZpbZC17" +
                "vv/u96WtIJ8bpMfYHPKKukiZbyGs+/2Yf47JLPDVQgzc2iibYz" +
                "caQTH5fE9ivOYWWcue77/7qLQV5HOO9BibQ15RFynzLYR1H435" +
                "55jMAl8txMCtjbLZdqMRFJPPv0mM19wia9nz/Td/KG0F+ZwtPc" +
                "bmkFfURcp8C2HNH8T8c0xmga8WYuDWRtkmu9EIisnn3yXGa26R" +
                "tez5/pt71aZJ5HOT9BibQ15RFynzLYQ198T8c0xmga8WYuDWRt" +
                "l6u9EIisnnMYnxmltkLXu+/+53pa0gn+ulx9gc8oq6SJlvIaz7" +
                "3Zh/jsks8NVCDNyKlckrWLpST/8pseLnm7Er+tpY9Pnmk5P790" +
                "vSY2yOfEopn2/i+yHh883u7TH/4TsplfHaaHjfl2/FytRcu9EI" +
                "isnnSYnxmltkLXu+/74H1dxJ7J9zpcfYHPKKukiZbyGs+ZOYf4" +
                "7JLPDVQgzcipV57zPQ8yN6SnSpe360t3Fe/PmR/rF8fgT/vgM+" +
                "jQvE86NHdYXZOgqeHq2b6PlR43z3/13siT8/sv/fRcHzI6c8fH" +
                "7UOHcyz4/4/3eRYVtjzzbk8Y7jWg8eFbWV/P6aPLLCf38UHrXs" +
                "eH9GYkXPO2Sc8Djztft2uF8XeZ9hj3924B78dxfiz3B8JeE5R2" +
                "Yoz+dDsfPTf5nPl/7P+dzfPp+TeSbGI+H7IWBTncq+P9eJLfZh" +
                "XHlHIqrTIj5bzkcceugFeu54/xGfI23s7NUZxiHE6X9HYtIeau" +
                "RqYisGf6icVhGLICNxvpqmppl2GrbYh3FjqkT4iNhyPuLQoxZ6" +
                "Jp+P+3PIxvI5LYxDiM/xdYcrIjWNC+MrprWgdxlZxuORJD98Kl" +
                "q9JX/X/M3JvGMqnqLePhGj+8HSx/xUt3wcdvo7/j7D/yJ+/FPr" +
                "jcL/BpXubX0=");
            
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
            final int compressedBytes = 3156;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXH2MFVcVf6JsLLTLQgVWBPlIhWJMV6wtNFSdefOeH2iV+l" +
                "lEoNZWbfQPk4p/2LThzsp7z90/tCmbSqjBpq76j40VrNuvZbG0" +
                "tCZG+4cmSsRW1xZXGhOKGvUPvHfunDnn3I+Z+3ZhmZf7dc7vnP" +
                "O7982dOzN734qvitfWamKu6BGvl+X9NXmI+eIysUBcXqvFK8Qb" +
                "MskSmd4klovvynKVWCPeItbK2rpMd6VMb5XpbTJdlUneXssPcY" +
                "3YlJXvEu8W79GyoW+KZiZ7r3i/eJ8sPyA+WHMcYpvYIXaKm2Xt" +
                "szLdIj4nvihuF68RcwrEJWKezHtFn1gky4VisVgq3iiWae2elW" +
                "KllK7OkOuzfEBsyPr0DlnfKK4V18lys0zXi0jUZZmIBom/RaYP" +
                "yXSDTB8WH8lkN8r0cfEJ8Ulxk9guPsP43irTbVFv1FurqVyV6t" +
                "B13W4tRi3IaE23oKYTr2EJ/hu7eUyqw4NHNDmYGI6gEZERsFHx" +
                "uZ56oAx4ZDMiyjheHc2z6gOeVE23Wsu4jOZUY9eplPtvP8F17g" +
                "PwgHXZoFfghcyaZ/es5LyhbD/u8k9lfBRob3UM+LiYRRuiDTpX" +
                "JUig3boatSCjNbTHxGtYgv/6Lh6T6jgvMw5KTAxH0IjICNio+F" +
                "xPPVAGPLIZEWUUHw1EA7Ic0GUmH4CPnBvHUAsyWtMtqOnEa1iC" +
                "/6GHeEyqI70ZsOOgxMRwBI2IjIAN7RP1jWzBO49sRkQZx2fX0n" +
                "w9ylt3y7Qgm+93FrJ+mZaLg3o9kvnaQnMluSZfVdSuZtdquh4d" +
                "1utR+SE+JbZl5c15+xYHRq5H4p6sVqxHssyuUWKFnu9qPRLfo+" +
                "tR6+tZXa5HWbmZ+CtZj3LZjeJjuB7lsl2eK9Y41JJRqDWe4Vqt" +
                "QSQ/wE6VLozWxz+qBRzNcV8UGomiVE2lwe/oFvaCH9CnMv9+PU" +
                "gxss9Pc8xeSxrHuTa/ho/51hByJR/z6Yd+HTSeY74oLBJBqRpt" +
                "6fPTMZ7Hzf5WjYdLipF9fnC+Jw/m5ZJE3ne27ibnRaZR89151j" +
                "zIS7de+Qw59Hwvj6TuPwv8QpmW2hjLNo+v53tJ/C2l2pvEl53e" +
                "9yX7dA4ltFStdSDZ17pfSTgGEao9OOGWgz/qUc73l3gM1IAVMg" +
                "OE6V2lb6yzuZsW4A9Z8Ph2z5CBGdkcHcqa4h1j/H2otX4g0w9d" +
                "GnkfF/vsKMrWx6dqMz58EUIw5yN+1v+6M+r+ZL/OoYSWqrXGVa" +
                "4kHIMI0PnkUEd5/DKPgRqwQmaAML2jVxcvGhu5gITGt3uGDMzI" +
                "5uhQ1qS/I8mILEewhJaqtSZUriQcgwjQ+eRQR3l9Ho+BGrCCGi" +
                "JM78lIfR5nSxE0NnIBSfwot7J7n33rj5qRzdGhrOk4FOveGnt9" +
                "bx11r6vV67v/qM8v0zbWhM22ai++9T0+GuK/GlXFtLG6GOmDxX" +
                "g+a1wdDpZesQ6Wo7R86NUwFiGRyrz4MPHPg8azEuVmmtyX3Kdz" +
                "KKGlaq3nVK4kHIMI1Zbru1MO/qjHWm14IY+BGrBCZoAwvaNXFy" +
                "8aG7mAJH6YW9m9z8bzYTOyOTqUNfY3e/LMniQaq3QZjcJ8jUZb" +
                "v1R5NIozOnI8dYAM5zt40u3hRain3lx+FIsyDDLkGN5qrNLzHa" +
                "VYi4+4rbgHhSqLoGPY/hvLGst0rkuQ6Xb9LpBQDH5QRzHcBj3m" +
                "o3s5R1IdMOAs7Kjo1cWLxkYuINF9sn1zC1ef3P2nzKVsRWNF3l" +
                "5RaDKZSq0XtRTaHEstqN70xI/h5ajTNhRF8SCHHD7GecJ6QPm6" +
                "vcaHzR67vMaHzSjW9dOw0TGjnqhHnq09uszO3B74yO/yQNTTJh" +
                "LE6ppuQU0nlSs78IU+85X5AI9JdWR29dhxUGJiOIJGhBay0dxs" +
                "38gWvPPIZkSUcXwxvtn9RXJUp/wZwFjDEuddxOBECKo4PzeUrp" +
                "orz8ezi/LiYxE/FsITUTNjmhzRKW/dY2sDxvNI6XhurM3K4WMR" +
                "j4fwjMfPE4txnfLz8zpbGzCepVyGB2ZpPD0s4sdDeCJqhix+oV" +
                "M+npttbZiPkvHcMkvj6WER/zSEJ6JmdvC/Xbav7+Z5Mww1vHN2" +
                "xtP7vPlECE9EdfktHkoO6RxKaCWHBk8ksWorCUgHp0CrsaCjGI" +
                "n6e1aPEQM1WY8pkuu0F2QGCGyhxMXdtEAkwcfcyu59Ed/B1GRC" +
                "R5KyL6QTOmX19cl6eY7up9pkfcB3lKNcWO1zVub7hEe+vgphI2" +
                "fA4mc65fP9aVsb5qNkvu+apfH0sNj7le56M0MWT+mU32NttLVh" +
                "PkrG845ZGk8Pi3gshGc8Nr2o0ZJoic51CXXdbk9xCW0hmtuDHL" +
                "2jPhvP3aYN6jgvNyvEunnxiMgRyvhJd4+xL9l4PmlG5vFoJGCT" +
                "Ifqjfln2Qwl13W6f4xLaQjS3B7muYalrcjxHTRvUkfHst+OgxM" +
                "SYvO0eIZt4r7vH2JdsPPeakXk8GgnYqIR7vOheLmh3Xufab2Xu" +
                "vDL3TXFP6DEfzx9zmcr3bHXdddE49j4suj+NI+B+yWSkdfEj5u" +
                "4x6qGY74/wfvHcZGLfaUoPHch1TR2duVzbPJtGVF9y/el0r+Go" +
                "KlwacxRnHne895+dEB6u+Nm30TH1HCfYftpcVuyn7fQY+2mfcu" +
                "2nHZzoZj/t8EMXeT/tT0L200qUtZ9WHKveT5tMJpNypZuEMlv3" +
                "JqEdHwcJxeBH6wYnwJbLdRs95uP5GEdSHTDgLOyo6NXFi8ZGLi" +
                "DRfbJ9cwvQmEzt/lPmySTfa2pdPy+5ANfPX5leZvf62f5tyPWz" +
                "/Tv7+ulngjo93+WZ6p7v8435fnzm++eHf6Pmu3jmos33OUHzfc" +
                "4098/3RX1yne/TZbbu98FHjudlqAUZrekW1HTiNSzB//DzPCbV" +
                "kfulPjsOSkwMR9CIyKjgeo5H50wK3+fMyGZElAEbrBsz7blifT" +
                "d+JyCede04H5zo5gli+OUL+1yE892zcgd5oSixtYvno6loSue6" +
                "BJludxZEU60RlCCWoqk9yNETesz/fvS86QV1nBeNg23Atk9yGd" +
                "ebtshGxXf5Th+gvcJeuCLwSBxv34uK3SBNP+p6Oxr2FtT3lrH9" +
                "wvTsqjDmbzF852f7T2H+q/YWlemjszrld8qfBmlnkQsXdTWeJr" +
                "p+Ynp2VRjoA8h89hC/3H+6o1zvG4X4Ffv81DIlgVrI+QlYlw27" +
                "fp46v+cnsg07Pym+5Pr5yozOz3/qlH83t4K0I++ZWiMmDpFB55" +
                "mBrp+cnp01b09yDPQBZD57iG/q0wdY67by+NWjEA9BrmvZu9dj" +
                "XKu/E9RXeXOdByHWChUWBVHAfO/TuuU9P4dCePjio7XbT5r9BT" +
                "Pu1SmX5W+R4970dsNbhot7nVec/G936bW2DTuv/hJ2frqj2Jh0" +
                "E+WGdj779p9D/KdfKtf7RiHXLtAp93UHSDuLZfy/mjhEBo2Lga" +
                "7/cXp21ri8xDHQB5D57CG+qTfm+9fK4/tHIV2a9ou56vcyqXzi" +
                "TFenfXC/JH6fGuu7+INMJ8zfy6Q+z1dkNmT8Unk9bgfez5f9Xi" +
                "bzNd/9exmRXx/TN3u+h3zPi/l7mXShEX9LWrKjxvV7mbT4zWB0" +
                "Rid+fkZnOsbfITQOkd6+3sltWH/+FrgenekOA30Amc++fSrEf3" +
                "pXud43ChXvl67o9v2S+Vtu+/1S/dLz8X6+fum03y/9I+T9kqtf" +
                "Ie+XktPJ6VpN5brM3uSdhnZnLUgoBj+ooxhugx7zv5nu4kiqAw" +
                "achR0Vvbp40djIBSQ0vt0zZGD3yd1/yjw5XfG+bp3xvu6Fqvd1" +
                "4sWq93XtVy/u+/n2mQv5/y5c9/s4TzrvtK8NYf+fgR6da9j/Z/" +
                "h3N7ugqv4/A39us5+PHPP9X/xK5Hvmcb0nd11XHLy2Y07fh8iR" +
                "2OTCILJkNJyYdFDOt6D9dc3tVVHSFkepGm357CF+lX+3HqTo38" +
                "aZ31T6rWI8t7gw5jfjev/p//Yanw87O7v5LyP2Owj4/yH++OX+" +
                "02+79fQ/ibj8NLc1t+lclyDT7c4NXIJYiqb2IEc7tM/n+39NL6" +
                "jjvGgcbCOWy7jetEU27f+4fVP22C+bqcnExMt8J+bZd3NvUSt+" +
                "f0wxzYAdnH5M4wtB5+fOsCiIUjXa8tlD/Cr/bj1I0b+Na+7A3J" +
                "jvW10YRJaw8WLa/wsazx1hURClarTls2+fqWKYjcF+tx6k6N/A" +
                "/R+fl/mh");
            
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
            final int compressedBytes = 2875;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXFtsHlcR/gPNpVwsSmoSB1UWhRZRwkWFFhIRyev9t06cSC" +
                "mXkhTcJjVFjRO39MoDD1V2V8S7yh+SVqINSamoBA9IIKEAahMp" +
                "YG5vlnFsKRIEeECIm0Kl0kYE8QBn9+zszJzbHv/+7YpdnbPnzH" +
                "wz883s5d/dONtqySV6LXqt6OUYRtmnWmQBDOoBbRqrlqDLVtlQ" +
                "qg31aLJBn8CLMjs8qHqR+ozZcvZqLrTxOBxr86HWNruLV5hnB7" +
                "N0mutprXXv7Wt5jYv+8J0ublgNUx5qVUF+eNC8z9vXqvvI5DV7" +
                "g2nvumQyeruv3Sdi9MG2jNgH82wPSCgGV9RRDLdBj1U+azmS6o" +
                "ABZ6FHRa8mXjQ2cqkla9WMqQdkoOdkzp8yF7L+dr+o2+eYppSV" +
                "8s8XvRxDgznOVD2V47je8wOokzYmFCCyMfQKq47iLPTY1GbqZp" +
                "6FiwGN0iST0ePH4zequPiJ+K2iXy9yT+PrS8k7RHunaK9UiJvK" +
                "/r1wnsS3iLZZtA+Wsw/Xnm6LP67Vc1OluyPeHo+I7Y54pzmH+J" +
                "743nJ7X9l/IT4QT8SrYuVMjPvit1UjkVE8gNfPeLDGvI/U86Px" +
                "rZV0S7X9RDwUD7vrGe82yMbY7H7RvsgkD0E969yPi3YCEekHkl" +
                "t1v+l0y3NJPiL24fWtnizJZrf+8KDlSOtRfJ8lfo7XMzolW125" +
                "XcnuJdWgqOf6HtVzV5f1XN9a4SX5bD3aoxyJjy3R816Rz9tXKI" +
                "u7LPXsOn78S2/katHW1RGjSvqb7KSC+61ol+IbF8Xi98r18yZP" +
                "u7sbfiEEy/hNBH+daBtE+wPPQl2mbq7wtzfEH3Vq98aTbvugEz" +
                "xY9LJVuf8AtWX/YDJE9S5vi9cobBpwScBRnLnMxhXf7d8WH639" +
                "/JRMT9ej5/Tjc9FnCTk+k2+2Wkf/u0Ln+ylLpTZ57c9NS40/9B" +
                "nZBJP6Kpl8l2p578zle3ZM5xo/Ns1ROAr5u+2z7TQjcx1EBt/n" +
                "ntUI6N/mp7hvG/ok/30PJvizldT7PH/bMZ01fk/vflH4MyWdSa" +
                "6GI2/CzVDaJT9EPXgqtvQZtSnT+uh6sd6X7/etkv/SeXxlznfb" +
                "/VJ2i8+RkLzUw7tQvP98Rral1FO16TzRG5aU12LqmZz1uvqeW9" +
                "K181XZKl/nQZr93IRDpM/zkYruPOnPaTEYyAFkNvtsi4//bKtb" +
                "r1fBsW/q+gTKvWKwQ52rEsP1SkF0ppb3PId4NmaB1/1v8ose3m" +
                "kcr8+phxUu29W5KjHwVxCdo8tcz+3muLacLDX4VfcM0m+UV811" +
                "+vUze7mWbSyRp5ILYnxj8/keiyf15D0aywVRT6/7+XhP0/NRMl" +
                "s8H8V/UZ+PqtkNya8t9d7g83wUbIhHE8MVOP60+nwU7/P/Pcr+" +
                "yTVf/VlRT+/j3FzPYz06iy426G31bHgfko4CKhlc0nlyFPqgPi" +
                "PzAUUblc+bR5vvEhETRDaNm00TrnzeJChgLhkJTeRm5vZvi4/W" +
                "fn6U6+ejired6ryQuH/fVZvOiWW+fu40x7XlZKnB73r4e1Q/b+" +
                "bKER+MqvNCotcz+aPdpvPUMtdz1By31vd7eenvMvpl/VlNygoJ" +
                "jPgznXqOy3oC1mTD6vl0d88BNgyy5XnY7ucp3lUXt97vebg6uv" +
                "5Uj/7sk6P7fNfq+a3X93kz2Oh1nG3sPnJ0EHulnn81YRDZ5NO0" +
                "DL/gx8kvCqKKEZ3Z7CF+k3+zHqToX8dFD2Cv3C/tMmEQaT8+VQ" +
                "w5Pv3q+YDdgxlVjOjMZp/tbGJYHkl/N+tBiv51XDSGfenrlfr3" +
                "6LZW68jXVUw05pGpFTPsdb5HY01RptZyVDGiM5s9xFf1CdvPya" +
                "tme5Cif1ucIIdejsp63q5o2zhuuP+sMdLGpHFev3KJyz/mg6LM" +
                "q/vPXI2rxnfzoJ7N1k1+gmPQB/UTTL5F0Y4k/6L6YCQYcXmTGJ" +
                "vGmc+xJlxylaOAuYwnNA3M3P5t8dG6yU9UvxdM/lPXM+Da6i+z" +
                "PN4gIkb7ff+21/l+1i8KiXOWRT1rff/5b5Wh8Z7FogcpRtZx0S" +
                "T2pa/6L3DyO0yYaNIj08nWkpZosslDeg1HFSM6s9mnq3wYpqvN" +
                "epCi/+ZM8fc9186Zpb8PaT/d6smSruvufYh//G7fh0QHsC9HP6" +
                "7rucOEQWSTT2M+X/Pj5BeFsD7AcrDaQ/wm/2Y9SNF/M098fk/f" +
                "Itp1rf/LxXb9TN+8MvGj++qI9buA/E5dy8fN3rTj4yk/Nk1R0g" +
                "0cVYzozGYP8d3+0wGzHqTo34QL1sjGr5/BmvQGEy5Ys5j9pKLb" +
                "x7uza8JADiCz2UP8Jv9u/WKqAOd7ein/Uu/Pg3aP3n+m7+rufG" +
                "+fWP5zPVgtW8X03SDND5pwiPT1zc6YM93ZqUt+iGMgB5DZ7CF+" +
                "k3+33lWFaAL7sp71X1Dkj5gwiHRc/6yY7G6v6+eEXxREFSM6s9" +
                "lne5sYljXYbNaDFP1zXDAZTMpebkEm5/mXuQSxFE3tQY52aF/F" +
                "/5HqBXWcF42Dc8RyGdertsimiG/yTdljXjpTlQnHh1fDq2ouUl" +
                "a04X6phTlg5RgtuZ7Kde9UB364J9UaelhNbCkLM7/6/VI/xVMm" +
                "rsWk57Iq5pXwithegW2puQLz7H6QUAyuqKMYboMe68gMqetgRF" +
                "nxqOjVxIvGRi4gSc+oGVMPyEDPyZw/z6oYDX1H9sW26OWcSgAj" +
                "tzCGheq4pSoHjW7LeVAW+kJZqVwRU/y+07mJrR6d8jQx1eNxeX" +
                "lFO4R9eb9U/3tH+iETBpGOX4pDrSUt0SG/KIgqRnRms88m/Bia" +
                "9SBF/zY/0bjhfd2TupaPrWzGl1jPcb8oiCpGdGazr9/XjTe8rx" +
                "t35YX+OW5o69BW2RdbkMA8n0ItyOhIztJpnEkPOMIt+D8ywmNS" +
                "HeelxkGJiuEIGhEZAZvsII/OmVAGPLIaEWUcX822DW3DsWyinh" +
                "nLcZvUoB7QprFqCbroeW5vvUpuox5NePQJvKhnag99sY2ep3w4" +
                "ezUX2ngcjjUeyfsM188tupaPm71p1y+vZ9hon18URBUjOrPZZw" +
                "/5ZWHWgxT9c1x4Ibwge7kFmZznHZBQDK6ooxhugx6rfB7hSKoD" +
                "BpyFHhW9mnjR2MgFJNnDasbUAzLQczLnT5kL2Xw4P3y56OVW3O" +
                "9Ws2KeHw/n039IOWJwLf3Mgx4whQTkgIHR8GXqLZzPHqU6aVWs" +
                "iOCekWf6MmhVXmghkdKrlMj8qBWMkxfQVqI4U8iGM6mfEgC7EC" +
                "6I7QJsy+gLMM8eAwnF4Io6iuE26LHekwyp62BEWfGo6NXEi8ZG" +
                "LiA58hU1Y+oBGeg5mfPnWbm/d5Gf8/nehfqtjYbvXZxH3evyvY" +
                "vzPt+70Lk0yWR0+T2BXOQY7cf3yfA9gfwn8D2BaH/5PYHyb7XF" +
                "uPqeQPWrNU2/JxAfK74nIL2ZvifQrt6Xub8nEO0XUe6J781/6v" +
                "qeQImqvicgRv2iDRCN8XsC7TPyewJC7/yeANajxOzm0tL/GJXI" +
                "7wmEc+GcOE7nYFseuXNyTS9FJ8O56KSUIwZXiaZNlVOPVT4vci" +
                "TVAQPKgntGScHNzIvGRi4gofH1zJCBnpM5f8pcyGbCGbGdgW2p" +
                "mZGrqOfpcCY6LeWIwVWiaVPl1GNVz2mOpDpgQFlwzypPEy8aG7" +
                "mAhMbXM0MGek7m/ClzIZsNZ8V2FralZhbm0bPhbPSslCMGV4mm" +
                "TZVTj1U9X+JIqgMGnIUeFbiZedHYyAUkNL6eGTLQczLnT5kL2c" +
                "VQ+38SUhZeTC/lf5NaOZcN5jjjevBIx+z6eQ6jACY0/l8N6glw" +
                "YKOjOEMem+Lb53gWLgY0SpOsiv4/fOkF/w==");
            
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
            final int compressedBytes = 2974;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW22MXGUVHmgkKFnbXbS1LtstBCtrtawmAtqid+feWRVaPh" +
                "JUQDRQlI8fWIgaghLTd25Tx5HYFLQSugTQil9oqNZGhOj+8IdG" +
                "wBj9x1/51e3GH41NiTG+733n3Oec8753ZnaYWaJzc9+P5zznnO" +
                "e8c+/snbl7azX3Sv7ld/9q/qPWQfMra+LlecTMd9T6eCGuf2XH" +
                "arVB/HpxqAbCqvwpf6/43e1YherX3L1yPfNX8iTOeD2v7Pe1VX" +
                "lVaR1t/mQmmfGt6wmh+Tdfg5UwPoI/djlCT/Gz4zInt0ldOg8Q" +
                "zZEMnhGKSI3LL+08AlcgM+uMwCS/GG91Wxlpq9/tev5bYrzlFt" +
                "nKkY6fPSttFe/zVhkx5oOopAvKtAVY9mwsPsfkKvBqfQ7aqpXJ" +
                "F53v9oyf73FOzc/Nr/h8/9WIz/N52a9u/sapxinfuh6Y2/JX2m" +
                "s9yjlk9SjZsFOL6DJ+9juJuXbvdXFlyEMzrTPURfa901qRt/n8" +
                "YWzEo+h81+sglXB+ccxuclt5pG/ye63WHpcYb7lFtnKk42dHpa" +
                "3ifN8kI8Z8EJV0QZm2AMuOxuJzTK4Cr9bnoC2mzHzZrLHtm8w5" +
                "5twSO8+MmbXmfHt8XmTeViDr7T5pLsivtv1mc5F5l9liR+8ubJ" +
                "fY/T12f6/dtxXIbBnpg+aKor/SfMR8tLOez5tGgc2bj5uP2f4T" +
                "5urYipqbzGfN58ytdnSb3Xeb281d5m5zljm7ZLzZvMW2bzXrzI" +
                "Ttx83bzQaz0byTjk8zbdELC2bxd8Jcat5f5P+AHV9uLjMfsv12" +
                "u+8wianbPjWZ/Yy7qhPd9man3XfZ/RpzbYFdb/cbzCfNp8yN5j" +
                "PmFqH383b/QmeNN7utfGc2+90enxdLjLfcIls50vGz56St4vjc" +
                "LCPGfBCVdEGZtgDLnovF55hcBV6tz0FbtNLJZNK3vqexn7ffJx" +
                "E+Aztf5AjhiI74xXr+WueETeqKqwI3rktmJAvUuPyxirEWFF1m" +
                "lvl4Js5PppIp209RT2M/b18qET4DW/oT7kfo/cjW84L2gY2t51" +
                "SYB4jmaN1hRVDj8scqRi0UXWaW+XgmyS+uLSb8Lq+X5ibya9S1" +
                "SMEDs6/rl4kBr3smVsahGgir8m890F/87vaqVUhmk1nfJrNAaJ" +
                "4dgZUwPoI/djlCn8zGc0ob5+g8QDRHMnhGKKK8vCYeG2opusys" +
                "MwKTfHYdX/yFb65j1/M3DP+Kt/XgcOLs/2ePbyUVV2WtL47yej" +
                "59LH3Mt9TTzI1a97vWIZIDBtmqcBoDl2zEhQ3KiKGjI2pMF88N" +
                "LYTs/6r0CqvnKrXSsE6u3GKH0kO2P4SeZm6U/cC1DpEcMMhWhd" +
                "MYuGQjLmw0AkNHR9SYLp4bWgjxNYWxpUdYl14drprzI8fsk51+" +
                "PLXfj/bdG1oqjvUnu7M8no4P4ayykVpf682J4hX5m08N5Xx/NH" +
                "3Ut9TTzI3aO1zrEMkBw83zxThO8XjEWq3+kswBC3lBGTF0dESN" +
                "6eK5oYUQnj+sDAp0Zr06XDXnx36JKDkP2/x/j1m6+XVn8Xiv59" +
                "Urzt7plfkNR1f6ePq4b6mnmRvV97vWIZIDBtmqcBoDl2zEhQ3K" +
                "iKGjI2pMF88NLYT4msLY0iOsS68OV8354fWS+Qoh7Yb6jeKnsf" +
                "ckX+xYn+n0s12P4u+/sddLw8pfcXwupAu+pZ5mbtTe5VqHSA4Y" +
                "ZKvCaQy89bDMAQt5QRkxdHREjeniuaGFkNZB6RVWDyWh0rBOrl" +
                "wikd/nPz2CX7AXhhMnv3Gwz89h5e//xdbzFvWt9U4910jkm65i" +
                "NJ4e8f2OO+N5Vyd/ejg9XF9yLfU0c6P2tenh/KRDPJNb3eYjcD" +
                "uxCCcO8etLkt36DiKRl9u4TxjdIfkyWbUueHimj+oRl0V60bj5" +
                "FHxjdVE1us7O1cES91Vn0m3laLd673fquUYix4tiNP484uNzZz" +
                "zv6uRn98km9X0u+/fouvCeG7+/pe9FVd2PExlf5vfb9P24bDLU" +
                "xu/fhRGBQIGL4u/HhVfEjZf1dXIsavU1OZTG7sdlM5m7xz9Dfc" +
                "GcoXl7LyGcgw02zpE+iNjJ+5JkchspkCrCrIga08VzQwshPH9Y" +
                "GRSENcXr58o90tiDtjjL7ypHd5ervyfG7PJ+VnJa3+vrvNnTXx" +
                "aw3IjPqvxbh3op7GYnFPElL9uYbfSt7wnrzP9ICOdg87Z8kXwl" +
                "TtHhSzlFjsAGZVAlsyJqTBfPDS2EtA7rinkEKAhritevqprK6L" +
                "5OeV/GY8X+okfLueByD27XkSr+T2OK8nAW5xNOLW1hHKmCRhVR" +
                "X9QVx6KGamP6NSObSo+lx+zf+WPoaeZG2Z/S4r/7NAcMsnGO9P" +
                "FjGsloPC5sNAJDZ0XUmC6eG1oIaS1Ir7B6rlIrDeuXVQXXo8f9" +
                "XozH0rHQ2sc1bVeWjjmyK+vjvfL3V80gr+S03+X3o+R0vifGA7" +
                "Pf2OLvwTOD+fXiUA2EVfm3ftZf/O72qlVINiQbfOt7GrstnU/n" +
                "JcJnYEt/whEd9uKomNc+sEldcVXgxnXJjNBIPa+Jx0YtFF1mlv" +
                "l4Js5PNib275JrfU9jP5/7sET4DGzpT7gfoU/Kv4bah9s4J65K" +
                "xgl1yYzQSD2viceGLoouM8t8PJPks29q9wW/h9yvvsvdN4Tf1c" +
                "9dnc/PKq2jzZ++mr7qW98T5udz2wnhHGywcY70sZ9YR4DKaDwu" +
                "bFAGVTIrdMZ08dzQQoivKYwtPWI1xeuXVXXey3G/y+Nzbjx/QL" +
                "3nBW9uRXcoNbt+zmB+vThUA2FV/pS/V/zu9qpVSJaSJd/6njA/" +
                "b/1IIuByNvcnHH7wR04Zhdu4Lp4Hcx4npktmhCrqW0/HY3P1UB" +
                "kq1UokP1lOlm2/TH1hWaZ5fZtEwOVs7k84/OBfroOKwm3gyDyY" +
                "8zgxXTIjVFHvaorF5uqhMlSqlUh+ciI5YfsT1BeWEzRv/Vwi4H" +
                "I29yccfvAv10FF4TZwZB7MeZyYLpkRqqjfd0U8NlcPlaFSrUTz" +
                "I78nP1Te39xde4Nf7dsH9ay6f5S3+7o7dWB4NeRrytHB4a9Q6x" +
                "er805Urec3Do4ya3oyPelb3xPm562jhHAONtg4R/ogInJyZmiD" +
                "MqiSWRE1povnhhZC9qW6Yh4BCsKa4vXLqjrn/hm/q+/vZ/Lvqk" +
                "+1ggdmX9/DFXvuzGB+vThUA2FV/q3f9he/u73XKjTuadzjWj8u" +
                "RwdiHNiJHRtrz7hHN0WcG/NBTNLFlXF/agv7Ae4r1eta+M7rl/" +
                "V0vy/H72vVJ0OOfIpM3es6VR097tHrDli3/zHTd+KkMv/5GT4f" +
                "52uS9/pidw9jz/3FnsOLv9zzXHOdX+fM1+2+tjg3fik4F+Rf8s" +
                "9z2XZLiV7CGNsqorPnueys0c96mpuK9tbOLHKl4Z/nKkbl81y2" +
                "7zzP5asJn+fa558mu9xcVvTbWbziea5yJp7nKpA/yOe5okfBzY" +
                "2bfet7wvy8/qBEwOVs7k84/OCPnDIKt3FdPA/mPE5Ml8wIVdS7" +
                "mmKxuXqoDJVqJZqv77mL833m//B8nxn0fNfPw/Y63227oM7336" +
                "jzfef/zvnu1zNyvl8/6Pmun9+M3rcby8Z863vCOvPthHAONtg4" +
                "R/ogYudz7bRkchspUCqCrIga08VzQwshred1xTwCFIQ1xevnyi" +
                "22PlsfrHGBub3d9laaExdo8S1qUdoRMRa9vgY27xNjcW9qaYuz" +
                "pEKZm/vU18gq4grqZ+ssMX165vbmhuY7Otfx9oxvXthclz9Rfn" +
                "t+qJ8zs7m2Ar84QM63Ss8azje75nk97BX/n1yVvxn8ntmcXqGi" +
                "LRWfXPh/72+p7zZ36LlD6P+9K3551T6vjfg+xx3xvOX3oxdG/c" +
                "tB/pPK9fx28B78dQXvV3F85uX/q+c/bv7N1vmfIak+0iP7XyrW" +
                "u+/8/R6f+Q/zrv+j23ykZKp75XO79FwjneuK6WqfxvSIj89d8b" +
                "wryz+wyv8C0UXk3Q==");
            
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
            final int compressedBytes = 2701;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNXF2IXVcVHv8wWIojovWhVioiBF80NiGIlfMzY6g2nY7ams" +
                "RXH/rQJ7X0Ueaeiw5zQYu+WH0SxUih/vRpBkGEFgIKpm3q2MlE" +
                "wZhOmkkUWm2sSbSefdZd51tr77X3OXMzV+Zezt7r59vr+/aec8" +
                "8+584k5Va5NVO/ynHPNvkr3+UsR2VW5nC4dngclVCRYstP+1Uk" +
                "N16ahz1fZ6iL875GjnxjU4/SFbj28ISel78OoWqwzczMv+jenH" +
                "EWeSvf1zHZ6nGyrfvbUc2v7+fsl66L3q7KurQyrbtVfrtVX8ag" +
                "Uh5g5HdcmX5Vb2Fr5Qc6k99X/UL7+X3marxHYuK5abyYz1bWl/" +
                "/mVebf4ZasZj1/5GU/B5t8isSqYYyVSavpg5MoVs4au5Sl68f4" +
                "MbqrTv4Yt2Q163nSy34JNvkUiVUjzMpP7UxyPo/1wUkUK2eNXc" +
                "rS9WP8GB2vU1yl1vWuJZ8j1SrFm7HHYZOfH4ePONcijOYCKq5D" +
                "qrBxxdVqTVYDI/EVV6Uyy9IzmZkZ/NDXqRWwOp9Px5ud6QC1rn" +
                "ct+RyhPFn5/bDJz++HjzjXIozaBQ8AFezorQ6pwsZJzZqR+MoD" +
                "Upll6ZmEHL4CrIXm03H3ys7WZ+GvsrOud63rKU4WxZu1WYBNfr" +
                "4AH3GuRRipCtXDF7PxO4UD1mckvuysVGZZeiYhh68Aa6H5dFys" +
                "/z3ckmXF8s8jSr6LsI+4xlg86Vd5j3unEYzDCIqyRp/X5++q7+" +
                "exFj6zxg1uG7xvbO2rjzsHs0uPtNeMT+zuvczg3btY65aO/B2R" +
                "q29kToN37ZKu1Hoe7lXhnZH4h/bkeh7uu56DD+xQ0YfV+XyUW7" +
                "IQq37NVv4FRMl3EfYR1xjvc3O0x+f9qHunEYzDCIqyRp/X5++q" +
                "7+exPj5z8BO7TK3rXUs+RyhPVn4CNvn5CfiIcy3CaC6g4jqkCh" +
                "snNWtG4isuS2WWpWcScvgKsBaaT8frnWk9W2eLPPZnZpaf2d3r" +
                "JypPv1YsP/zytLVlm/Vn+uls0/WudT3FyaJ4c64dg01+fgw+4l" +
                "yLMJoLKEsHK+jCAeszEl+2KZVZlp5JyOErwFpoPh1P70cra5Pt" +
                "R8VD09+Pqmcm3I8emuZ+NL8+v04t9c13K+ut/3jdP05xYPAmtD" +
                "zoXTwKHxXb727WFUeQY0uq0qytNlOX5IZGjhSP+jOWFaDAZ9Zr" +
                "pVVLfM1wiVrXu5Z8jlCerHwRNvn5InzEuRZh1NlxCajgzGl1SB" +
                "U2rrjUPL97Wt1BfMUlqcyy9ExCDl8B1kLz6Xjz2T9HretdSz5H" +
                "KE9W/iBs8vMH4SPOtQijrkLngAquWa0OqcLGSc2akfiyc1KZZe" +
                "mZhBy+AqyF5tPxZm1vUOt615LPEcqTlT8Am/z8AfiIcy3CqJ/y" +
                "DaCCc6LVIVXYOKlZMxJfcUMqsyw9k5DDV4C10Hw63tyRltS6vi" +
                "ypR4TyZOVfBJ58FwFe16x+hzE+m3kP3eqQKmyc1CzVQ6NUZlmo" +
                "YnOUZfVbS53Pp+Md+/v5yfb37MLe3d9J2zSfN7OLtbrfZxdd71" +
                "rXU5wsigPLdqvwou55pI+TNc2ZXmSEVGHjgLUZoTzUFp+J1qmz" +
                "uiL4fKZ6LZ9tztgj1QtNe7Y8Mo5vkuVajrGvPglHwriP6ZOpzj" +
                "TtH6sXyyPuHcWd5ipAkYUxZFXtLlH9yVJbPR9Xac9S81XPtUwb" +
                "4kr7MrWudy35HKE8YrD1eBnnWsZ1/mUr6uuQKmyc1GwxQnmoLT" +
                "4TrVNndUXw+Uzt+i9wWy7omLa0Lf0Upk9GYtw7jWDc+Pz7c0x5" +
                "jL+rvj1LjI7XKe+i1vWuJZ8jlEcMth4v41zL+Kz+xYr6OqQKGy" +
                "c1W4xQHmqLz0Ry+Ap0RfD5TLV9mFrXu7Y8zHHOMEbberyMcy1T" +
                "afQ7auiQKmyc1GwxQnmoLT4TyeEr0BXB5zPVe1N9La3OZxuud2" +
                "22wXGyKA5stuHthRu655E+TtY099QNRkgVNg5YmxHKQ23xmWid" +
                "Oqsrgi9gWs1W2SKP/fr+c7vX932rQeRnfZE38S3j6mT5aWtL3s" +
                "//a8L7+fN79/dH2flp3c+3DFv15/1CtuV612ZbHCeL4sBmW+F4" +
                "2fPIbMvisqJyPCnowgFrM0J5qC0+E61TZ3VF8PlM9b3TNrWud2" +
                "2xzXHOMEbberyMcy3jvm67iF5DoEOqsHFSs8UI5aG2+Ey0Tp3V" +
                "FcHnM7X71yK35aKOaUvb0k9h+mTEXdVWuZjGsVqt0NbbrdZC+f" +
                "nqJT06Vidby9bYIi9rf8sxmu11vQh+K1J8rS/yJvajtcny09aW" +
                "2o9Gvf6q1NiPnogg98J+9ETf/WjCn/Op7BRb5LFfr+d7+1UIIj" +
                "+PxE9Z0Ul1T5Ynbb1mMaHS8m5qXe9a8jlCeY5V27D1eBmvLssx" +
                "NjqlQ6qwcVKzVO/XClml2hSHr0BXBJ/P1OI/yy1ZsZi2pZ/C9M" +
                "lIjHunEYzDiFB5ir+rvj1LzWfXKa5Q63rXks+R6m8UB5ZtPV7G" +
                "uZaxG1yxor4OqcLGFVeqv8tqPiPX8jXGZ+L9fXKgQFcEn8/k3R" +
                "XUT0Ti+eg1Pz98mzHmN8GMv4Lno+qVFvf64MzYuh7U+EfTvmZU" +
                "/2/N+lYj/uPo3da1hv302Ht13P+70T/WJtD/DCsM39T3+aj6T3" +
                "28MXyzd+V9nQ56zX2Po2xpHJC99g2B9uv1HWe9XC2J4TlwLDYe" +
                "s7sZ/vQq5LN00IvPz3x2eIuFy2d3sp4SPagmG2e9hrdqDM+BY7" +
                "Hxw3396qfz6VXI307H+Cd4L0edxR5wQPZaFw8t66XifVgkhufA" +
                "sdh4zG5nuv1sKp9dy57KrrVn0Vc5uvKGhQMydv3MnpJjJry/7B" +
                "g3/6rGOJuP1HjW1lU/nQ9XIVjxb3FLVnM/f6vOhra9niGmT0aj" +
                "+uAkSivv5k/Xj/FjdKxO8nnzzsmeN5ef3bvffy6fntb3n+7vGY" +
                "Z3JNfzIxM+vz+5h79PfnKa6zk+Zw8tn8kPBfv7oZH3+78a93x+" +
                "CMjo1eUncsyE+3sHy+igxjibj9R41tZVP53vWoXlP4gd8NOtFf" +
                "ybp+UX+tzPx3/PtXsvqNxZ/v+hLbtOhz4/s+ujT1m47Hpnvb/q" +
                "Me35+fGdaUp+uu7VGJ4Dx2LjWVtX/XS+exXyb3NLVvOZynU2tN" +
                "PVdpbRqH4sQGnl3fzp+jF+jLbrzGVzGbXUc4z8UcERicEbOYmZ" +
                "y7KT8FERnJIjzEEZVGlWVLV0SW5o5Eh20p+xrAAFPrPm06olvv" +
                "2U72ufyD7anp/mvyFb+mDnZ1X9rxgj9T9PDD/W7/O+dLwH5h3C" +
                "rvfnpdt6XEfG2pY69qOlzySzx5YeNq/aC3ML1FLPMfJHD3NEYv" +
                "BGTmL0GFQc72gbGilzrECrCFlR1dIluaGFI998vz9jWQEKwjnZ" +
                "85fKOZIfpGN8pb/A0eEnZ2YK8exIOCB7XQkPTrZHdo1zuiSG58" +
                "Cx2Hiej5+X8+zmj63C/P75/dS6niPsj74+v7/4JSLAkoXxOLSF" +
                "nuv7nDonMT4PIs38I7o0IxQxrxun82RTPalAM2tVMqbxxrUB/1" +
                "57ENylPNfn3KJxxvPRmd27xxve3XE/dTqlrdfzziR/b/M/Ochf" +
                "BA==");
            
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
            final int compressedBytes = 1970;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXNuP1UQYP6IQIgoYrwRduYsgIgZjNDx4ymkiEoz3FQFBif" +
                "4DxgdJiHI6I5z0RLOBRIMmxEv0wScT9UVjVk00+kJkQUW8IhpB" +
                "XWAXluWqPZ1Ov+/rTHumpT1d6UmnM9/t95s5M9Pp9OxWKupR3y" +
                "dz7I6KweH0VlIc1ieVHI7sUcw8Nw1Vcjs2PB1iOxk5O5URexTL" +
                "zZpkTRKpuEpZUF5IJWCLrbG/lIMf+AMmjYJ1mBfGgTKOo+NFEY" +
                "FVyG6hPjZmDyxVplEmUXv1YEvC3F35jne2tFJZ/HBn+iG7Uy83" +
                "w2+MzYpbv7J+VZDzYtSn1ifCeN/0u1GECTHyGYrk0vzaqz6ujb" +
                "4rpp1jxnv9EkVybUpGs2LmgG04B6UUs8g2U12W6GfnGeeXnQn5" +
                "ttaybraCPUhkq8L+uT9j1OUx8tXe+XhO47nNuGUr0/XPiNUjOc" +
                "48T8qc21TmygMm86fzlz9TrdfOX+vLvb+b4bes8mDqHCPrpWnK" +
                "HPF1ivnEnz+dw2Hs4XpfkDup4A766VENozPeN3yBRv5mbB1O+O" +
                "jbg9JAcD3u9xWlTs4RTY86z3T+dE57579sVGI7jA7bc2blnDs6" +
                "XydrrZrLO3J5sfKuk8E4fSpchU0duf2s/nzGu8OHnWYK8ydbl3" +
                "MbOCOgPZ8pfHy/IdLWtZWKMpZIG3GVeeqP5aoP1ai+lAdmoeeL" +
                "uamIEEvFiatJ/TWKoWeq4kWRNN/gs+F66cX895ca08qdJzYOFD" +
                "wPJTxvui/n/7zZmFHu82ZjepHPm4v7F/f769fgKvOi7L4ktVKK" +
                "tVLn9MoSluOo0sOvz0wqwzqyoiY4shTlqfKSesoRJBsHqBeNgP" +
                "HxGW0HlTWtub3MXgZ5cXp1J0/4wkam2FqXj3rq4icdwkLa6ewh" +
                "puSFI2N/mbau7HPMh7KP1gWfFIfa6o7qFpmKnD/et1Ktmk+Olk" +
                "5DrcxQwIoyb4+fHD8OH7z1cWrdtW6RiquUibK1REqwDXxAh22o" +
                "D0QETGyp6oAZsKKoEFXHC2MDFykRdVJjUw9dnfT1p7XSjrTwiZ" +
                "b1/H+fK5+LuZtsKnE9774yUlur2p/VszG7xPXSq/mvl3Jqz0NZ" +
                "10vVQybrJbY16/58Unva60Zqe2Zff8bVqbj3HdCejTnn3n4d+7" +
                "LI6PZue7dIxVXKRLkxT0qwDXxAh22oD0QETGyp6oAZsKKoEFXH" +
                "C2MDFylp3BCtMY4ADNQ66etPa9Ua7wn98+azGpHaJ0v2Tk797P" +
                "U2+rdj7kcLjObPt9KP9/b3I9s6B+dPq117snezz59Wl9UlUnH1" +
                "9566ZNl9j0rAFltjfykHP/AP6nNhNAro0O5YF8WBMthSGdVHfY" +
                "FNC18XG7OHeqlMo0yi9pqR8nG4Xno//xm7drjc+1Gx+Owb7/wt" +
                "yP/jnd+xfjne2SH3I/96HPXp8P0m2+md3/q5g4Hkez8N3lGyY2" +
                "K8sx2hx556H+uzR3m5nxUev7C9bB/7k/3BDrPd7AekGfDOYWL7" +
                "I/sp+n6T/Yry+9kBeL8ZSv/20yN25F0k2xU7fg3HOxtk4a/x7C" +
                "F7SKTiKmWi7H4qJdgGPqDDNtTHi/IZSD3ZeGqJdZIBZaGiAk8d" +
                "L4wNXELJ+GiNcQRgoNZJX3/M3B5q87z5hfKdpX7/Dgef2Hr/bo" +
                "/LZ2TFv38P0LfH3I+M8U37J7+Ijzd8ft+R87Ntqz3HpPFw+3Jv" +
                "zzHFz9HuTl17OnvcXfn3z1p/uf3THD9b/0zcDzH61XO69Wctp1" +
                "9SZ11/xuHn/fxe7ZFpNdz1dPupVs37PaVXs4sTu3NaNdpTrfaY" +
                "2GEryrw9fnL8OHzw1sfhN3rnTah8DZ8p+ye/zh00Ge+yPbn/ff" +
                "K5QWme6J98chh7ij/eD2rHjGfL5wf5rrYjbHp0vPNZ7cY793/r" +
                "wq+P4vOrz3q8z+aJ+0ZWj5pLdyT51QZLXs93HB9+v2S9kLE9E/" +
                "xqp0tuz9PltSe/JWOEvQn1OV5ye3Yc32qquawRNPU5UXJ7dhzf" +
                "2qLmskYYeQe/tdOIaH/piNHKWlkv8dvire2x5bZn5/GtzWoua4" +
                "QCv/UHMvbP2wudTRbVFolUXKVMlN2TUoJt4CN0Tq/0pXIZHXz9" +
                "/jGaWmKdZEBZqKgQVccLYwMXKcH4as2AgVonff0x89oie47trU" +
                "VbqR2sSUVelN1ToJUynBMlpxdKIgLk4Crj145RTKxDo3KOigOS" +
                "qA21wIjASLJp4VM9joAZUOQoIsiovWaG+SCcP88UMCKOlnx/7z" +
                "i+/ZXMNUcXUJ8zJbdnx/GdsNfWjObudL/3ts8v+f7eIfzqZplW" +
                "w7tz8zKqVfPJ0dJpqJUZClhR5u3xk+PH4YO3WZzWAfufzcvz+K" +
                "aaV5D+MSGndfnSNvolMf1zQpG9ki/D+8n8Prqf3Jys2N+reVpP" +
                "t5+c6u8rmvG7aVn3k2Pw6X4yvye/34PVw79Wbc4lGGv83jsl0s" +
                "IG/++AP4T6x0T+qDnHDWuCCJq/8ubdBshPkNLqFr6fC/7qkN+P" +
                "tCv4qoj3YwGLu4Oy/wzBl/OVqe5H4YzdnF/A/ejiku9HHceH/b" +
                "rmgvzbszZc8nqp4/ho/lxYQHueKrk9T5XYnsrvFZPeH7W7HwXv" +
                "i0/mtErO+r7YGD/T++L/AISOeos=");
            
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
            final int compressedBytes = 2395;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXFuIJFcZnjXBC0aNaIxi2GWXaNZdcYfAzhpnhJ7pKo0X1g" +
                "x4e9CFoIJhkxiIupp42eqezk5jo09eEF1JhPVhlQ0BQQRxQAzB" +
                "l3iZPGziuCpJBCWQh3kNyan666/v+8851dMzdnVB6lDn8v3/+b" +
                "/vP1NVp7tmZ+fmwmMw0N7oxNwER39jbhdHOtfuMXv+3r3Vet7i" +
                "VuuN042ebLe7ns3yJzcnN0strWIyHi0pwj4osLGPnYOI5fVxjf" +
                "VkmyqwKkJWRI3pYm5oUYT5w8ygIMwpnj8rt0h4fQ7OTf/6bPvo" +
                "v3bmjNdV9/tyA3fEcy3f743yJ6eSU1JLq5iMR6ki7IMCG/vYOY" +
                "hY3u+vsp5sUwVWRciKqDFdzA0tijB/mBkUhDnF82flFtEj+0p1" +
                "fd7awE/wxZavz0b5k5PJSamlVUzGo1OKsA+K2PobOtfiGh1zi/" +
                "7z1pNtqsCqCFkRNaaLuaGlQp73M+YIUBDmFM+flTtsJVlx7Yq2" +
                "hWVFx6O7FGEfFNjYx85xUe4G6u63q60n21SBVRGyQmdMF3NDiy" +
                "LMH2YGBWFO8fxZeX72ru+9tdzXX+3Og71r6X7/UvBZ/8HI54E3" +
                "1HyOvTFA3uTyec2UPiXvsE/39td8nq/h7wWfY3oHdqnonW5FTy" +
                "enpZa2WOnTOh5lirAPitjc/X6afewcRCzz2Wc92aYKrIqQFVFj" +
                "upgbWhRh/jAzKAhziufPyi0S2Y8G0/++OXxvu/vR+g8a3Y/2Jf" +
                "ukllYxGaffUYR9UGBjHzsHEcHJnqENyqDKsiJqTBdzQ4siklMY" +
                "286I5RTP32YVXeNXhr0p/gSfafnzUsP84/aj4fsmirCr/Whquv" +
                "e4H629MMl+NPjl3vaj8c/Pwa8auD7+0/L1OXP+3tPVej7cQD7P" +
                "tryez7a4no9MP/qw5RfK63e3uJ6/buD6eKDl67NR/vS29Dap81" +
                "YRHQ8XYVWMe5iP0/bQanyf09rYx+cB4vtYD2aEIuVd61t2q4QV" +
                "WGafERj7p6vpqmtXpS3wVS3dE90TsCqmvRwXm/bkzGuZJ76IKf" +
                "EVRRTYKJvVkAeI72M9mFFHigm/tXMEVmCz9xmBqX8xZ7O76Tg2" +
                "tS0y3izLoislUqGLlXWTbcbHeRX9Rfhoz/UX2dPaVAGr8CJ7Ol" +
                "l7OAOe5L/oZ8wRoED0+0p9JThK21Z3y782BHP1kitbgujZXdKx" +
                "1tYus0uvpWj0JbBU/luxZ5HiWmuJe0GBjzMqqixap0CV+hHimG" +
                "WP7Uf9pwa/bWA/WG95P2qUv3ule0VqaRUrymFXSqRCD1fWK2wz" +
                "Ps6r6B+GT9UrYyICbBoNyqDKsiIqaw9nwJP8D/sZcwQoEP2+Ul" +
                "8Jr6Sc9d83+0+Nvjv975tJv93vm3X803n/GTsGv9P17K4E7+b+" +
                "O9EbvP8VP7GV6B2x0u79Pjn/tJTS8/P3DTy/vtby87NR/vRYek" +
                "zqvFVESvdQ91B6bO0eIDmmvdwT83Hmde7lfr6HNCriS0zmZG6r" +
                "y+cBUnxvTSxm7TwXtaqydun3HrQ6NIswqs+k/oV9Pp137by0BT" +
                "6vxT0/vwer+nBPRtqT0/bQavzkPsvJNspmPuQB4vtYD2aEIlWT" +
                "81s7R2AFltlnBGb986NzIC8aKe/JaPR9i3ENS/77DhnbGDg4fn" +
                "KmM8FzXv3VNzYHUVUXlPkWYMmZWHzG7CpwtsKhZYyyg3nBSE53" +
                "ff7QYlzDUqznQfWCfzx+cs7aatbzoI0Ym4OoqgvKfAuw5FwsPm" +
                "N2FThb4dBSr6x2P/pDA/vB/S3vR43yp39GyUeMuOvzRxbhEbz7" +
                "G4wortHBUuYz4DjWFtPlq4JvXJdlVAvU5PyhHX4a32e2fMzE/u" +
                "njKG5cnBiPfmIRHsHbrSchiksPrfRcPt/gONZG61mrCr5xXZZR" +
                "LVCT84d2+Gl8n9nyMZP1H3u/P9bA/dZr+X6fOT+t558ayOdsy+" +
                "vZKH/neOe41HmriI5HP4ZVMe7JqL+BkURAD63GT75qOdlmdfk8" +
                "QHwf68GMUKRqcn5r5wiswDL7jMDYv7PQWXDtgrQFvqDF7Uc/hV" +
                "V9uCcj7clpe2g1frJmOdlG2SyEPEB8H+vBjFCkanJ+a+cIrMAy" +
                "+4zA2L97vnvefQc7j1ZHeW9U1DlifeChtjpc+8CTb1kOWHSW9u" +
                "DhR0fUmC7mhhZFmD/MDAp8Zn91WDX7R96rXKze1/0sbql5H3Nx" +
                "Eq/k2zN6j3SxHf5x7z8njLC7959Zy+8/s1m//6T9/fEG9tdhy/" +
                "v7zPlpPf/SQD7fnE6cwV/b5a95ylzqXpJaWx11L7nn50P5OEes" +
                "jxax9TfiuMaDrcjn6+yNuKqHlamHHx1RY7qYG1oUYf4wMyjwmf" +
                "3VYdXsHx707xU/PPeyO9b6Dd/d19ev58rJ/yvyjY2uy0M72H8R" +
                "x+tysvvR2oXd70fF3XdTcpP2LJafw48KqmPryzPY7keqZ5Y57M" +
                "X+imutZVwGrDcedfB+P+NY1FBt/cr57P7Rv6r6WX48uhP8axb3" +
                "5eCfE/g8Sf2/u/PfO8/RnAZbO8S+PNb6j5p135/s157F8nO4Kq" +
                "iOrS/PYLsfqZ5Z5rAX+yuutZZxGbDeeNRB4mccixqqrV85Zs++" +
                "nF0VPj+z17m6+Lfu2ZuL+i3ufLs7z5Qe7yjq8urub2RH3Ojd7n" +
                "xPgVe/1cqOZ8Ffxww/Wdo+kN2afdC1H8o+Es8h+2xW/H1pdntR" +
                "fy77YnZHti97hef1+uzasnedO99Glur5l72L+D+RlX+lnt1Stk" +
                "tZJ9vhd+3ZxyLYZ8zo8+78QnIkcWuR19IWK31Ex6OfK8I+KGLr" +
                "b+hci8sYEct8Pm092aYKrIqQFVFjupgbWhQZfsrPmCNAQZhTPH" +
                "9WbpHw+bn82DSen4M7x9ju2Ovzc3hqb89Pzamh5+fR5KjU0iom" +
                "4+UnFWEfFLG56/Mo+9g5iAhO9gxtUAZVlhVRY7qYG1oUkZzC2H" +
                "aGWnylYf42q3Q7Lf5/kvW/JTdUv5cpMMFRA7O20J5udy5wpJrf" +
                "BG4rT7p9tvp31lCB2VprCeNYFRLl7AGrr3qvecFmASVjf28Z2J" +
                "MbLKbs8n5p/YnY33Pt9f1S53L9+6WpfQ/Z4/ulzuVZv1/KfjP3" +
                "Mj6WH5ieV+R9yKPdR6XWVkd5b/mPeZ0j1gceamMfO0dZpGejcV" +
                "zYoEw9fFZEjelibmhRRHIKY9sZsZzi+dus5l4CqTGmaA==");
            
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
            final int compressedBytes = 1933;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWk2IW1UUDihYdFFbwSpKS0txORUGdKW8l0w2InSrqxZrQU" +
                "GKkCoyuHBeTMYHFfdSWluKImrFzVSQ1uCMpTsRF6EbF+Ki0J8Z" +
                "FFrxB8zrycl3zn3nJTdv3sujk5B7z893zvnOzftParX0K/qdpc" +
                "71WuGv8P5apa+y60e7XMvSWyzFRzaVeX+ZvN8/O8H/mW3vvJnB" +
                "doeK/nRg2TM9q+b3eCeatJA/jXHRbCML2zk7qgy/o9fdGPgsXh" +
                "YHm7vm7XLkOX7NZSwzMHN0YVVwV0h3PWZ/v1X8dhX8V+3+Xm79" +
                "Zg/vRJMW8td/czEumm1kYTtnRxWqSPlkDHwWL5eVzpPmpSuCI8" +
                "9JXNrPfTJzdGFV0JUkvnGtca1WS0aaEw9pLPMIDN7ka/c41o2E" +
                "zJLOJvPCx5Jkpasiq8VL1tZdJHP8jtuxzAAG6Z7s/nVXE/b3P4" +
                "rfI+LFavf3D74o9WhyM7hJI81sI73+vLYAK9Eynu2IQzxq6izS" +
                "J3nJOtBlHouXrghWPCc9Wbkle7BMM3WZuPj0q32itoVfnT99UO" +
                "2PCqx4m6X6wtZbT7+eOn8VuJ7/3BPb2b854/72QuW8qgo2gg0a" +
                "aWYb6fG72gKsRMt4tiMO8aips0if5CXrQJd5LF66Iljx3Dlh55" +
                "bswTLN1GWi8cF6sD6Y13m+61ln3bUAK9Eynu2IQ/zw/P6emwU+" +
                "sZ7rug50YLVN+91YsFm+beeW7NFXmqnLxMUbx+L7WOqWsJ/GnW" +
                "qPE8u3S33aMh/O00gz24Z6S1uAleh2T/rIjjjEo6bOIn2Sl6wD" +
                "XeaxeOmKYDVi17JzS/ZgmWbqMnHxo6dK20bPVp4eMT5ufQdL+z" +
                "bzDXYf9cMtveyBeVDIOwafXR5b0LCnpWcm5H5hrPelpTe8n7ON" +
                "7o+6j3tdq/WmWc+Fh6rd38ut39hobNBIM9tIj2O2SAze8EmMjk" +
                "FG1JTItA/MwEpXRVaLl6wNLmzpnHM7lhnAIN2T3b/uavQ851jz" +
                "WDKSzJLzzGeI0X6JtKI40o4Y8+TrmMSmY+JPkJN5SWYynsdkDg" +
                "9DdtlDCw9zLrkuqCOxE49wT27l+83w1eJQUx8/92zB9TxSHMpz" +
                "+9w7kvZtwfV8pTiU5/bZ2sr7e3zGC3W2uIr4fXOT30vq983ol+" +
                "JYts9NqP5TxpZ3yGv7PJTv981p1zP6+Z5fz6Ne63m0uPXE/h4W" +
                "8gt6d8cY3/aZHz/3z7oits/wQLH3R9F8gUf5F3Ou54FZrGHzYP" +
                "MgZPrYGB4l2pLdSPaFT2Tld2Nkxmw+YKyZyXgekzk+L/lo9m4v" +
                "8qPraOyE7XP0bDrcXfw313224uul3TPf30v9B0VY8d1Xub8X13" +
                "fWd9JIM9tIj7/RFmAlWsazHXGIR02dRfokL1kHusxj8dIVwYrn" +
                "7k47t2QPlmmmLhMXP+vts/Lr+W/LzB60ghaNNLONdNcCrETLeL" +
                "YjDvHD5499Nwt8mpesAx1YbdN+NxZskvpWbskefaWZukxc/N3z" +
                "1lzyHp3Z5uijzqVzhMIIT3K9RLrOIaORf+Gqm9s8u8/pjFYMsj" +
                "IvMNPx0rZw1covbXoVZLdUg99ZzBoP0JjMyUg6W7rPkR1YlnW8" +
                "tHOu9Bogu+VjhGRh4yRnqyL7XY7jO9E8tVdnRD23Unn37xnHr+" +
                "+qPX4u/1Fu/mhX9NhQ2jb47I0ennY9o+3e9++PDPa3DwviPeF3" +
                "oCjjOjOrvvN/74+nv3+Pnhqe6e/QmMzJSDpbyA8b5NF1yx09I5" +
                "dxfXbHsro8JAsbJzlbFcE8zc3uJDrj8tQMdEbUcysZdzCNKZ/0" +
                "9KZBh1/NZr/u1quq35inMZmTsTHPdvYwRss6Xto5l1WrMT+Zh2" +
                "Rh4yRnqyKYp7lld6J5aq/OiHpupeBKcIUl0lj3voYd4sNF264t" +
                "02bP2MoWJ+XJ8oeLvvh86zDr81H4ebXno6z6Ueopbb7zUdAP+s" +
                "P17ZPGuvf30ve1J5bw64Lu7Pr5/Fn1bbbTMmr2m8OR5rtX/H3W" +
                "YZEYvOGTGB2DjMPrz1WNlD6up1mkqyKrxUvW1l0kc/yD27HMAA" +
                "bpnuz+JfPkM/P9/XzF+/v5cvf3zV8vTXkm+bLi66WS68/8/uh0" +
                "Mbzbaznvj06XvX3WT9GYzMlIOlvIDxvk0Z3DKT0jl3Fvc8qyuj" +
                "wkCxsnOVsVwTzNLbsTzVN7dUbUcysFK8EKS6SxPvguf/Q6r614" +
                "nwFXpkFvrmqWP6unwnhdCC6wRBrrg/NVyy/DhGeZLYmchPbnnc" +
                "+f1VNRvMo7fhrI5Ph5suLnSyd9j595X/Ubcq7f4PM76e2b7JdY" +
                "K9612FhtjdfSUS7Gzt6+NZ5Dcn6H1ZK0HJ0Z39f4ziEFa8EaS6" +
                "QFox6bXltqsDbejixUoaD9fS2fP6unND4f02A1WGWJNNYHtY/7" +
                "ZRhvRxaqUNB6rubzZ/WUxudjGlwMLrJEGuuD2m/7ZRhvRxaqUN" +
                "B6Xsznz+opjc/HNLgcXGaJNNb9M/jaqUIRq7m8e1KeLH/86zRs" +
                "c6xnL+ixRBrr/hl87VShoO2zl88fX5+GbQ5el4JLLJHGun8GXz" +
                "tVKGg9L+XzNzs++ASVi+n/T6yY9g==");
            
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
            final int compressedBytes = 1577;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHEUU7qMg4gQPu4iJEdSDBwnk4EEIZfe0QhC8eVDQi1" +
                "5FEK/upmeY3RnEw0JICHgICaLE4CGbg/vjZleWsDMHERcEYUU9" +
                "bk7mJMiAXfP2bVV1VfW8rqnuytrDVL+/ft9Xb6qrq3tmoiiby+" +
                "ajyZY9lr+fy1qXPo0qbdmTFvvzmuWpKGp/GXnZssen+M+Y7Tb8" +
                "7JRmedaJV8P1jEdh62nD91VPts7WUQINdXoGqh0Q/NRzWh6bP7" +
                "1EiedRbkzZDttBCTTU6RmodkDwVM8dN3//b0p8/6ErU7bFtlAC" +
                "DXV6BqodEDzVc8vNn/Yo8TzKjSnbZtsogYY6PQPVDgie6rnt5k" +
                "+XKPE8yo0pW2NrKIGGOj0D1Q4Inuq55uZPO5R4HuXGlG2wDZRA" +
                "Qz2fRf6hZaDaAcFTPTfc/MtfVWHrwGuX7aIEGur52uIcLQPVDg" +
                "ie6rnr5rf1yczWgdce20MJNNTpGah2QPBUzz03f39chW11Rk2v" +
                "5/t/hF3PL52uaz2fvVhvPQ2R/P5oOfD90TK1no7nzR12ByXQUK" +
                "dn0Cxf2yOrZqej0vxl3OrZegnsk/kkH7ndj/1mT+b95On/OqUX" +
                "cTX87Lrnfl7EFiSbTZVlvSyG4pFj+IvCVmCbmJfhT8tv7qWKR+" +
                "vP8fhsJa18HPzm+XNrRY1s1vHZqnd8shEboQQaG1XNoFm+sdhH" +
                "Jqsrbzc/cCP1wqkObMiGR/oQNNTJeYZUOyB4qufQzR9/VIVtdU" +
                "aNrz//Crte6v9Z7/ozn1EuQMv3vAUdLeBHW+dQyMcz0gV1n0c9" +
                "kI8xR9s8wECwMMfJnGX2xVw6qsy2DKPIQM0o8IpITa/n0xthx6" +
                "cN39t6fpWtogQa6vlcs0DLoM1RC/ZIPdqVt5u/jJsXXnfZXZRA" +
                "Qz3HvkjLQMeqEj0bqs1v65MvXjnCGFq+5208RjuXeu+AXcTGY9" +
                "Pxsh1zmbDi8XQeMgtznMzZhIj+Isfynqg8Va+aUeAVkfSt6vzZ" +
                "uRc1vsU/1Htk/8B5Xi+5Hg0+9L9e6r0X9no0+IByPepec18vNT" +
                "c+s05ezzf81LP7hONzlMM6z5v2lfYVaGGPNtC5HK+CXcSIF0TI" +
                "76Jdznh0vq2qkbIPGagsdFT+5nnMvGRswQUtMr6cW+QTTHSmev" +
                "9l5qrleBR9Uucn2P83CrotPaw3fx3r+fSsff4EX8D1/FnK/Mmj" +
                "Qs2fBs62Z7bn7b7K9XzTLzeXKNrmoZ5zJfWcC1zPOX9RjdXzBf" +
                "v13eZraqPh+2Q5ez2tYyofn+3vwo5PX/iPTD1vB67n7abr2V2o" +
                "M3v727Dne/P4ZeMze2um+6PzHsf6lPG5aFnt9F7/f53vg2fCnu" +
                "+239sEGp8/V+ixvp7/Ja/nGT8sOzenoP80az3d/o/Q6POQfHzG" +
                "62HHJw3fF0v1/j2+mo+npx04X3XzVfoUv3espwXf9+9t3J+HVH" +
                "ueHP/og6N7FtqRg5dOyvUo3vRSz03n830zanir93lyfN9LPe+7" +
                "Pk/2g29d3V5uX4YW9mgDXVjkGPESPjlGPUZkPDqTXlYjZR/iqS" +
                "x0VJHVxEvGVnvB90uniz2WMwgGep/M/ZeZqxbf53tnZBqfyfue" +
                "7uIcxycNf3Au5z88CfNneuApl+vzuoNHaf5MFkljYNEeS8tQ30" +
                "bvgRvTsu87YtIvk07W98W2PtX3fXHv85LPjfTvDIgyxSbLyXLg" +
                "8UnugS+mvS9KcDISm8weS8tQYz3JPZiN6Wu/YwuSyavLtvWnKc" +
                "c0jxpFiZOjVObT8cvz2/DF0bY8Tf8+efBq2Pmz3v/Hxd24Cy3s" +
                "JzN2F/WiJe52DoVXRAgLtJ0HfJ98JrKgBFYZQ/ZJV42uiiN0Ea" +
                "vaVH/xWLQhKz23YI8szUyLTJAPRKQr6Uq+JlvB/WSFtoK6sMgx" +
                "4gW+zj08tnikkFHKmb6rRso+xFNZ6Kgiq4mXjK32gu9lfL1nGD" +
                "t4Re+Tuf8yc9VS//37pJ5vB74e1Yrfu1m6/tTmlN6Nmdef18PO" +
                "n/GpOufPdD/dhxb2k5G7jzqX41vCwjUhY4R8vBwFrcg46c0tbp" +
                "UxhA+zyRyQlYrK30UmJlkciznV4+TcyBZ5gkVnauq/qEP0H/6Y" +
                "JGM=");
            
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
            final int compressedBytes = 2167;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXEuIHFUU7WXEYBINmE10/JCADC6iREGJ0zXVbswiM0GNM4" +
                "pJDGhEJWKCzgQi6fQnNIGs3LgJAROYgIiGKLrSjaCboC6CIEJC" +
                "Fi4C/haC/66+dXPOfe9VT3V3dTeZKvq9eueee++51a++00mp5C" +
                "9H/yllLo2l0oDL9LOlsS7Dzl+9vboh3VrV/txVXXv0UI8R1mTg" +
                "93rIbaVSKy5Gd+P9ZVTdEcaPv5bBX+chd/a4HzaVSvG5+Jy00i" +
                "e4jHQ7WhJcsGhJrWDwR1niCY5uRUsJyjlg02i6xaps1uSj0W0c" +
                "64GKNKL42Yq5MuUmo5BSVwkW+HrH+6GhHQsPtOfn4wXF2t6f37" +
                "FXhnm0x+W4LK30iskYCHOwwsYc64OIacZrlsk2zWdV+FkRNaSL" +
                "c9sqOsg1t2KOAAV+TeH6WblFrp+ZLpRW8NI4n4v1cZ/zc0e8Q1" +
                "rpFZMxEOZghY051gcR04xXLZNtms+q8LMiakgX57ZVdJCrbsUc" +
                "AQr8msL1s/I2NhvPtvtZ7TuWWR0DYQ5W2JhjfRAxzXvFMtmm+a" +
                "wKPyuihnRxbltFB7niVswRoMCvKVw/K29jM/FMu5/RvmOZkXV6" +
                "cnoynqkdEFwxtYqH2piTsJJt4SJi5+6vExNMtmk03WJVNmvyab" +
                "3oavc9UJFGTBS4Fct29TR7qH5XqauE9ucMqzdXzrn0zndieqKt" +
                "+2VzNzyR6555IosrMYtYjn3a5/18Rv7q6bzMbks0GU1KK71iMn" +
                "YRcJnN/orDD/7p93jQjQKb1cV5MAbXYtbu+kJNkj8Um9WjLl+p" +
                "q8TlZ8/P3DPl856+wT3jvb4PN3+8Ld4mrfSKyRgIc7DCxhzrg4" +
                "hpPbstk22az6rwsyJqSBfntlUkPef3K4MCv6Zw/azcIv7zUfS0" +
                "N3e/6eEJxnl+r6+tfheK2ef5871lsl/MmJ+58+d9fq+vrt+i25" +
                "XJyqS0lUkgOo6eglUx3oI/PnYLvcZPYnJOtmGxGV0NLscyOCMU" +
                "qRquiWO3XrU63MxuRmCWH/he9ulW2fuOWwd7n0n1Lk/M9f2jPn" +
                "+WL446I+3Pr1be8+Zwa6qcqJyQVnrFZFz+2iKVE623YFUb+ysO" +
                "v2R+ql1zImJ9v7WxLs6DMcexLLa7vlCT1BSK3XqTq0IVoQw2E/" +
                "OrjwTm50PXv8tLPczqR9N+64iPpod7nJ+Xhqkm2hBtkFZ6xWTc" +
                "OmwRcJnN/orDD/7IaaOwjXVxHow5TkiXzQhV2tcPhGOzeqj0lb" +
                "pKLL/7/Oxpntwg83Pq3+JYgfv5nfFOaaVXTMZAmIMVNuZYH0RM" +
                "7xCqlsk2zWdV+FkRNaSLc9sqkr650a2YI0CBX1O4flYe74wWo8" +
                "X2PF3UvjNzF3WcbDfeAQIus9lfcURCxPS4+NaNAhsd74s2D8bK" +
                "bf5oMWt3faEmyR+KXT3NVaGKUAabifnRQrTQ7he071gWdOwi4D" +
                "Kb/RWHH/zTvD+4UWCj/blg82AMrsWs3fWFmiR/KDarR12+UleJ" +
                "yw+ckd4wlV0u+Ap4ebxxsvyK0RVtjjZLK71iMnYRcJnN/orDD/" +
                "7p+bPuRoHN6uI8GINrMWt3faGmuSkcm9WjLl+pq8TlLzc/i15a" +
                "jfE+HzUfHPL9Rpffh0RT3t/9AnfDvf0+pDDdNy9jz/h9iF9Tyj" +
                "e/D2l81t/vQ2ov1Lz3V7XnBn6Kzail9ny2rdeltmuZv/h+35u2" +
                "flgBVe39WZuvPVns/qw9k70/492j2Z+1jN995ctfm+tzJm2S1W" +
                "IDz89N2fjg0fOp7KZh8OiZfptltdjAtW7OxgePnk9lNw2DR+92" +
                "vA96/sz/97jO8b5nNMd75hP2UP8eN5zzZ2V9l+vR+vGeP/PlL0" +
                "plMfef1cNd5scT473/HG7+aGu0VVrpFZOxi4DLbPZXHH7wT+/n" +
                "T7pRYLO6OA/G4FrM2l1fqGn+Fo7N6lGXr9RV4vJHPj9XjXl+jj" +
                "x/r79Pzn89qh5r17O6oLNSn8+tReXPeIr4r/FzdUPjWuNvPG82" +
                "flVr+aWBYnvvuBu/F6j8l2Xsf4bx5Wtq/JH2fxU9P1unSituOX" +
                "7f+N6HlO/33g590Pv7kMa67PchjTWjfh/i1xR6H1J7t7/3IcFr" +
                "/j5/q6C7iX3jj1V0TcVfj3r4Bo8VGKvP61F5fXGswLe1N9orrf" +
                "SKybh8j0XA1ZH4uDaOC3/kdJnW5md0s7qqLMNGtyqkplBsaGaV" +
                "vtJQ/cxvt2elTfqklTEjypFet6/vg7O2D/lYi+9rdbCKwBw4a7" +
                "X5GRHLz5NViasrrNTP52bqdj9fvtu7Hn2UZ9Y3bzLXnHVd7k3W" +
                "jPps5teUVvZhIWf1I9ERaaVXTMYuAi6z2V9x+MEfOW0UtrEuzo" +
                "OxclsXLGbtri/UlKfCsVm9sMJKXSWqB77em5m3Syt4KUfFsfIt" +
                "zTUren8+VhwrcLxvibZIK71iMnYRcJnN/orDD/7IaaOwjXVxHo" +
                "w5TkiXzQhV2pe3hWOzemGFlbpKVA98vevRyZU8P1u5/qVr65PB" +
                "slS2V7ZjWz5hjrbMDm27nsvHd304YrYeRLTK2F/bpC9Psx6rHq" +
                "PytMZyY4ovc/M/v1dez/WkckP9niGrpmL+v4vQ/mze2mOcnvZn" +
                "5fyY9+f5Ye7PaD6al1b6zpl1XscuAi6z2V9x+ME/PTN94UaBja" +
                "5H8zYPxuBazNpdX6hpbgzHZvWoy1fqKnH5pdLUGUGknzqj1yMd" +
                "q525vPiYeoa4Fq385Hu5nDzRwx5AQ1vZ2yGke+W6Fe+Kd0krvW" +
                "IyBsIcrLAxx/ogYjo/v7RMtmk+q8LPiqghXZzbVpH0zY1uxRwB" +
                "CvyawvWz8jY2F8+1+zntO5Y5HQNhDlbYmGN9EDGdkacsk22az6" +
                "rwsyJqSBfntlUkPef3K4MCv6Zw/ay8/fkfExtilw==");
            
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
            final int compressedBytes = 1306;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXM1rE1EQXyii4EHFQ7WiURCPgngSFHSzqxePVqkW7z0Xq8" +
                "eaqKvJXyBUsCAK1VK8WL0FQan2Aw+KR73rpQreRLN9eZ2Z95Fu" +
                "dt/HJi/kfcybmd9vprOb3W3aaCwaC4JoLOqMQcBXfM570IEX7G" +
                "EdagMeg40WP6aaeI/jURYyKnhV8cLYNIp0xPhyZMBAjkkdP2Ye" +
                "jVU/VD8EQdrzka/SWTrnPdahGvUWk8uWMOezdjyzVBPvcTywET" +
                "2DhLLFGhibRpGOGF+ODBjIManjx8zbsqXqUntcgpGv0lk65z3W" +
                "ETXgLcr5nM+CoLFCNfEexwMb0TNIKFusgbFpFOmYHKRWcvTARG" +
                "Yqx4+ZUwlvt290NIarw+34VwOjLfXps7nFry6yN7TwFd3N5kPf" +
                "sL8ibSs/OhZgp4+zSKsN1/Z1Zjva7yO13bw+M3vYpZEflSR727" +
                "w/GeK9c4v9Q5p8avBreyRJpUdGx9Ryms/GF7NHQLjm93jX4Tc+" +
                "26/PMDFfn6Za7vpMtqrPOy+L1Kfz471lpMpaufPZynK8J4f7JZ" +
                "/xI7/1qcM3df50nc/qFb/51OEL9Xm8fPlUaKb1+cxzfT7LWp/G" +
                "zvSTmz+lM0FQt4Zjt01rqqu+0zUTyGe2VpfO8LUXeu34id8828" +
                "UPK2GF9WzkMrYWJaCLtbE9l4Md2HfiWRG9wB7lhXFgDbpURvdF" +
                "W2CT4qt8Y/YQl8xUZCLqd6vP6KP5+vTdssWU2/t4NM56NnJZZ7" +
                "3MJVgHXrCHdagNeARMgiHtATNgRVHBq4oXxgYum5JlMWLsARjI" +
                "Manjp1F1v9+MVvPVZ5lbtGrXf7frpUi617072+/3m9Gazev5aC" +
                "KaYD0bNxAn+LrxjUuwDrxgD+tQG/C4GRHRlPf4DLOiqOBVxQtj" +
                "Axcuuf9UjBh7AAZyTOr4aVTdj/dzp4KBa+5j6vX+qL/ag/UsWo" +
                "3v/ZLPcLvn559W8cML4QXWs5HL2FqUgC7WrrfwHpODHdh3EIdE" +
                "L7BHeWEcWIMuldF90RbFOqT2jdnzyFVMRSaifm/PQ+59Lfz80/" +
                "fz5CDT86VRc7/vSC5b/Tz46/nzyCp+fDO+yXo2chlbixLQxdrY" +
                "nsvBDuw79bFN9AJ7lBfGgTXoUhndF22BTYqv8o3ZQ1wyU5GJqK" +
                "+o20Wr9fHPc306xy/++R6P5Ntz8rxuxJyW4lypuINPrhXhm1zd" +
                "4sruh+frz7dWf1qn49OsZyOXsbUoAV2sje1ZH06CHdgDJvWC9z" +
                "AvjANr7EfFiyICKz6m3FS+gT1mKTMVmYj6xZ/PK65JbpX3/sg9" +
                "NwP5nCpxPqf6L59nS/x83j03A/VZ4icq7rkZqM/nJa7P5/7yGc" +
                "60r29+5qiBmXx7vbT6m5z1OeMxnw9zcn6Yb8/J8e4c3973P2sn" +
                "2/G8NuTrYs58avBrs27On8a/T7vouT4X/eUznM7JeToobXPPrf" +
                "jne9f79z9+85kcLHs+e/s+Q/jOSJW9c29Z0ny+N5LP9+4ty5nP" +
                "6nW/x7t7fMv5vOQ5n5cGLJ+jnvM56jef4YJ+pT1HLeh0w4VsHr" +
                "JimLOT4zTFVMjnvH6l5Tyv0w3ns3nIimHOTo7TFFMhn3P6lZbz" +
                "nE43nMvmISuGOTt6f5RqmWJq93q+6fnvtZNfg3V/ZKadW3dvWT" +
                "yfzf3BwLXmPo/5PDB4+Xxwwl8+49+Dl0/bMXX7vmJ8PpOHvvp7" +
                "BF1M9v4/A6rPaADrM/J3vDcrg5fP5iGr7v8DFwCRGw==");
            
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
            final int compressedBytes = 1431;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWs2LHEUUH9GDJgd1A9EIGgdEJYsgCIKHhXT3NCSQQzTeJE" +
                "HBgx8kCCJ6EHF7w2bXRgRBD8IeAkbQwF4kwoqEPSwIejDibc01" +
                "f4GH7CFCuuftm997VTUzPZPqrtlOqun6eJ+/9/rV9Mdup2O37E" +
                "Oepf91PLT86c4MNT8xTdJEPo93Wteaj2nxo4HvY1au/57gujxj" +
                "Uf7xh3Lp+zHe/xqSz2OV8R/2Xp9JC+szCbff62j5c2HzeeHJUP" +
                "lMjiRHivifl7ySMr6RlEuWbHrZ7xvT6VX37wvpIJ/dpGv56FZC" +
                "0h0m67LZbKvu3xfSevd76BZt+pO6l88iUz/7k3Lc7bbSLeppZB" +
                "qtQZEyprQ+uYd16DJV0jRPI4MfXmk7LlzMNzEyJbpiRiwtDPJ5" +
                "Rcdl5sFGDW/W8+f/A6vXTN7yj3d8f10OXJ/Xwj0vRX+2cL83Hl" +
                "M2+C2O/m1hPmuNKV1JV6inkWm0NimQldJSn+nQgz58aiuSJ3FJ" +
                "P1hLOy5c2iNQ8Rjd77Yt0ZOUG6mJhPFAd3h9tvL+fp8/KbvFp+" +
                "PT1NPINFqbFMhKaanPdOhBnzwevWVaAU/jkn6whqymab6pCzSl" +
                "f5dtiR5x2UhNJKa8oz6/qvX9/eWw9bn6Tq2/nzfTm9TTyDRal/" +
                "P4BtEhg4Mk5GnSpcXd63hDS0oeI9AobK/lWdpx45K+gYUp0r+0" +
                "DXtAYiO145fIC9pOulOMOzz2OTu87r3CFCmDg3hLm6yr6bSGxY" +
                "FnJWnzeCZRaa+w6sIlfQMLU/IFM2JpAQjsmNzx66hGf0+OU89P" +
                "YjPwPdl3TOMb8un9yXYG8hmdqXR/P1NHPvNX25fP5blZqs+9//" +
                "ej6FCl+jxUx9+Pon11X7v8tcaf5/eFq8/89fbt9/xUnbnrXe9d" +
                "p55GptE6eogpUgYHeFJG68AifEpJmwdkQKW9wqoLl/QNLEyhmG" +
                "zbWsMVkzt+HVWz9/eZeH8/6E/KuS8eyx7fnT1YnN3sEeQzeqGS" +
                "hYcr348OFPdXT08i2f4x/KeGZGpITNmjcnX+u8nvR9mzRYVu97" +
                "app7Ffudu8jrpMkTI4wJMyWgcWBztDSdo8nklU2iusunBJ38DC" +
                "FIrJtq01XDG549dRjbm/v9jC/d54TDW+H73k0daJ6fS++KXSc9" +
                "wbeyWf0U9h8+nL/8zk83LgfF5uVz7zN8Pms/n/B6s5n2/fy6fX" +
                "fL57t+YzmUvmivvhE37zmcyFzecw/9lFT7hGvB9VtDDR+1G8Gv" +
                "b9aJh//X7Up0zxfuRjvy9tNv/8GV+dtj7jq5WeP8/uld/P9FLY" +
                "/e7Lv598ur7PD6tP9/f51d/9oJz2+2fvj8pX7PBeqM/Q75tp5k" +
                "+qIlL+//kDSXH/OP+B35yWNn20/H2//n3d30fVZ/5x++pz5Yd2" +
                "Pc+Hzmf+SbvyGT8QNp++/M9KPqNbYfPpy/80+YzXpqyBtRH1uR" +
                "a4PtfqzF18Mj5JPY1Mo7VJgayUlvrUR+9BD/rwqa1InsQl/WDN" +
                "svmnmqb5pi7QlNhctoFeorSRmkhMeceVV/9Pm5/zfAV/7QRtzf" +
                "uPv7Znk72/V9ML0/LPas1dHufU08g0Wpfzsj6ZAlkpLfWZDkuw" +
                "uOtxw7QCnsYl/WANWU3TfFNXxLrhti3RIy4bqYnElHfk+Et7Nu" +
                "FVGqGX/Ba2Pqv5zz/3WLPf2rMJ9/sIvXjLC8at5jXv9P298L04" +
                "JebF2f39bB5b/I09m7A+R+glbwXe77X67y30FqinkWm0BkXK4C" +
                "De0ibrmpqY86yI55SWlDz2p1HYXmHVhUv61lGUo/RvRwYEdkzu" +
                "+CXy3kI6n853OmVfjv0vf/N80CpeB0WuSh5J84xO1imqdB1WeB" +
                "avMxVW4Ft8f5y3/UhcbF1KadzQRc+oNF9GNthh6xyFbdX0xIj7" +
                "521dDK6u");
            
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
            final int compressedBytes = 2260;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXMFvFFUYr/RC1AQIMQIJoKHVBmja7bkm3Zmdg8hBS5p48U" +
                "DiQRIPXvwD3NIF3P4TvaCJvREOeLEmHjGKHIBEYZt4UhMlxguo" +
                "dWa+/fb3+957szsss610JjPvve/7fd/v972+mZ0dVsfG/O2Tj7" +
                "UX7R/bddtoa4rORmflLK3aZNz+zFqAZTTHqx1xiAenzcI+1sU8" +
                "GHOekC7LCFXafvp+ODerh0pfqavExfdbn6PY2p/v7Pq8dHS7GZ" +
                "sPpY1PxCfSv+cR9mWWwZugQljJWcW2fGO4uCL+5lpZZN/rfTFa" +
                "lLO0asv2+FR8yloym/aA5ng5S5xgEZ9r7ObkLOpzdTEPxsBam/" +
                "W7sagVFdjcUK/Zw0pdJcCjX7A+J+PJsbGLH5m/2WSpNTBZhJWc" +
                "ldw3vhhyfU6WXp9DKW2+3DzU7e1Nj1eb+3H/rN9y0a07gQz7Cj" +
                "JPSLvyYc9y0MWsXBj6rvTCAP+xsN2vqYs/YO6yS6nl+BMqem3g" +
                "fN7dhfN5t8x8tr4cdj57PL/oWXqw2Z7tF23F+H4jtg9iEaVWYV" +
                "hvWFn//EX8iB6Up/5Az9ILef1+oZ4HT+6xqHIsQFnlg/n75y/i" +
                "R3RRnr7X+03v+nznmb/eb5a53lfefurr/Vc9Sw8227P9wr9vIb" +
                "7fiO2DWESpVRjWG1bWP38RP6KL8vRbn+2NUn+XAeuTLAcr/NYx" +
                "5Ppsf1VmfeaW6j/f36viev9/zadfU7XXe9/1+XUVz0vP4voc/v" +
                "lzu9+HVLWtvDXA/2bB+jxf6vni/NB/5z7rs2SGZ2p9Xlkrsz7b" +
                "34zk/nm6iuu9daB4Plv7tv3+eXqU349aW63fm4dav7X+xny2Hv" +
                "a4X3maFdT6153P1p/VzWfrjwHvOb8vmM/SNbUeV3QlPR7bxVv9" +
                "jepQgbjl+rKcpVWbjF0LsIxe3mCf2BGHeHDaLOxjXcyDMecJ6b" +
                "KMUKVt+044N6uHSl+pq8TFP/36XN54ltZn++52M166t6vn88ft" +
                "ZlwelzY+HB92//3o6bcs505uRfzNtSqyNzqNjpylVVu2x0fiI4" +
                "1O9u8dFoNd0Hy4ds7YreeIRbJPFbAKmxmW9k+udj8CFaFW5ufc" +
                "zTWOCNUUrp+Vp7bZxmzazmqbe2Z1nPWja2IHBrsg+HDtnFF4o2" +
                "sWyT5VYFX4rNmR5QnrYm5oUQvzc27kgxJfqV8/K09ttUYtbWva" +
                "5p6ajmFhDHb4GGNjkLHHbJC+T3usyrIia0gXc9sqsja56FbMGR" +
                "SrKFepX7+tasD7kJ933/fNy1d38H3d/O6bz6KaqprPwOf7SyN9" +
                "XvlrZz/fR/t7m2g+mpeztGqTcd5fhyUbwasIju+e1/N2nbJob7" +
                "3rm+eYaD6ki3lIQRcLJZzHZYSqnrr1gtzrpqr1XhUBBsukinNE" +
                "uqVtpG3uiXTsWoBlNMerHXGI767Pf9ws8NF8RpYnitjiYiyCGa" +
                "FK20tHw7lZPerylbpKXHy/6331ud33PuRKbZTZG3EjlrO0apPx" +
                "6h61MAY7fIyxMcgITkb6PiiDKsuKrCFdzA0tamkfcyvmDFDg1x" +
                "Su36kq3dK2oW3uaeg4SdTCGGzwMcbGIGOP2SB9n/ZYlWVF1pAu" +
                "5oYWtUhNfm4bEaopXL+tKnmUPEo5HmmbP8s+6o3PqIUx2OFjjI" +
                "1BRmW2SN+nPVZlWZE1pIu5oaVnOeNWzBmgwK8pXL9T1VQylbZT" +
                "0nY9U7LH0/F0MpW9DwEmsyhaIrTHlng6/6Y+rVmRX3KyzXKTPs" +
                "OjI2CXb9h41+9qVIvw+7mba1aH1Mo6ipQoXo5+z/PRu9U/z5fL" +
                "Obrn+SL+ET7Pn+y+CZqIvRnxLcF3YhNF2FDObX5fN1E9csD7T5" +
                "3PmXjG+33yTCklM0VYyVnFtrp3yPmcKfv+cxiljT2NPXKWVm0y" +
                "rv+gFsZgh48xNgYZwclI3wdlUGVZkTWki7mhRS1Sk5/bRoRqCt" +
                "dvqxrwPuRe9ffPHX8fcm+U9894K97y1nluy476bfHqON5afR5j" +
                "RMKvI86E33+6LPHWygVpOZONRlbdQ2pZRVhfbz5vMx6ZB9wltg" +
                "bZhLMx3hhP1+m4tvnKHddx/Vu1MAY7fIyxMcjYuzIM0vdpj1VZ" +
                "VmQN6WJuaFGL1OTnthGhmsL126qSpWQpfW5akjZ/jlrSXUZs4R" +
                "F82pPD9tBq/tUXLSf76GluyedhXRZjEcwIRarmygeW3SphBZbZ" +
                "ZeQZslV794HeW/nL6R016lT8hrCzs3mK4qrRlZxLzsk5a9WCcX" +
                "IuegALsNJDPA7bQ6v5JR9s7LO6XB7WpXl8XZYRilRNFmf90ldd" +
                "UGCZrSq2MT5ZTBbTdlHa3L6oe3qveQFetXFPRssbGEkG9NAmvf" +
                "8Wz3JaH2NcHlhcjEUwIxQpL9fEuaFWs1tmlxE2xsf34/ve51Zu" +
                "y476XvHqWLGwYoyD7X529ik+jFK7nnUPo6xCVwd9vu+1VfRTwC" +
                "yDbMre9/nz1C58/jw10ufPzXhTztLmM72p4/pJtah19ZB6BaE+" +
                "xtiY7PefsNps8WZrn+/THquyrNDJ2v0IIIGXmvzcNiJUk8sDrV" +
                "RVJ85+g9LRNvd0dFw/rhbGYIePMTYGGXvMBun7tMeqLCuyhnQx" +
                "N7SoRWryc9uIUE3h+m1VyVwyl95H56TN76tzusuILTyCT3ty2B" +
                "5azY8Yn5s+DeZ8HtZlMRbBjFCkvPW6ZbdKeneFusvsMvIM0UzV" +
                "kuw3KDVpc3tN9/TZ+zC8auOejLQnh+2hTXr/DmY5rY8xLg8sLs" +
                "YimBGKlPfyVctulbACy+wywmbx2bYwke2aKestOJ8lYuMze+zZ" +
                "9tz8qxMLJd4rKl6xoRhkVV1Q5npgk98zuPnZZmeBqxUO3fsoO5" +
                "HtGMlhEYLCmT32bHtu/tXXF0r8fzkUr9hQDLKqLihzPbB15/OE" +
                "qxg2OwtcrXDoXqys8Ptm+gwRXa/4++b1nc1TFFeNruQW9mzEFv" +
                "H7GBetNrGoXbODpav7OzcGvpCukIawdqvb1ahtxu/7uRbNbpkt" +
                "n50hwv8Hb/TLMA==");
            
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
            final int compressedBytes = 2466;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWU2IZFcVruWAi0xwYMRhIoJMOjIOBBt7dlbVq1LEMMJoRE" +
                "UQaVA0LgSRDAlDsMvMtE6tM0vJJisDnewUJG1oGNCFKBp3icFR" +
                "6HbVswrMxnfr9Knv++49t6p6pro7mXrUued+5+f7zuv6efV6+L" +
                "fh9Oh07Il9jvAOsdE2I46bh9U8oNqvkz3qqrRPqUsZPQLe8Yqy" +
                "qxJXjikihvwMIb97uXu500k2relhvu8RdYw92422sUON90JPPw" +
                "/KqTHOyXlYl+ZoBjNCkfOOn1J2VcIKlDln5DNEZ2qtu9aua7ZO" +
                "8DU/Wu7PIuoYe7Zzz57qYfX+qCm5aZq1kgdInqMZzAhFzrv5mr" +
                "KrElagzDkjMM0vH81vO5VHPcLR2Vnji51jedRU/OrtI2V9o3nD" +
                "rK++S17y3XKOZoy2Y9z7acfxJeVABHxaU3ZH10gXc+sUaR1/Tq" +
                "vK6aGkVFrOycoVmUZer579MDLa1mi9fhL9wzG9Pl8/Gf6Nsxuf" +
                "OPBOtc9Pb5z+xc8P2eGxCv6ZAvl4+/r4/JJ0f2xO/IkYv3m+kv" +
                "94gXzqkIoutH+trWbLrK++S17y+xPE0f5WQ4dlcNyzrBI5nt+f" +
                "oIygk3dzDxna3ZD+Vqk9r/B+UGF1XKWTeW7/AMmV5krodb/F6v" +
                "lx2Nfn4R7jy50TfdRen8t5DFYGK+4plp7Dnxnqe83lCo7nncbd" +
                "GrPVQAPXQptbP2ZNwHrjrj4TT1N2LdXWzxyzDy4O2uuXZG2dRC" +
                "7a0VxqLjniaEJwIMY5KSv5louOk3fFQU90QMy7uceqlBVdWXtZ" +
                "gUzkMz/31grXnyvNldD5tNjpwenizE8ww2GBaayMM152HzeIWU" +
                "2UxdVu/YizVKFyc83N8zrFLAXMMg8z9uZOc6f9m93B6rvk9f6e" +
                "bEI0Bxke4xytMd897cZ9EXMPGTkruka6mBtaHLGZyt5aEc0Uz5" +
                "9NtdPstOsOVt8lL/n9u8lqjmbwM8fdd6/95ryrmRyzKveQkbOm" +
                "Z+oT62JuaHGE+bk3+kFJqbScn5U3O8O3cbSf1ZMn9mnX/3eek2" +
                "c7Zojj5mEdHvzOs35cgxh9a1RVaZ9SlzJCo6+proz7nK4cU0QM" +
                "ysT5wz/iaPeTJ/bjLynCO2Q7Zojj5mE1D6j2K85nVZX2KXUpIz" +
                "T6euutXDF3cOWYImJQJs5vdpvd9nW66+vklbvr+/GXHeEcHIhx" +
                "jtag4/SdIZllzD1WpazoGulibmhx5NZb+cTcAQrKmeL5s6n2mr" +
                "123fN1Etnzfe+7jnAODsQ4R2vQccosmWXMPValrOga6WJuaHHE" +
                "Zip7a0U0Uzy/ThVdG2x+wb3eU51H7nG0M3X3u/tmbXXM9uNnFE" +
                "EuZ3O946hDPTi1C8dYF/Ngz30iXcoIVb5uvhb3ZvVQWSrNlWh+" +
                "b7W32v7NVn2d/AVXfZ8jyOXs0TbHDEcd6g+u57+ed0GMXkWryo" +
                "M9chXTeF4LNeOrcW9W75NHSnMlef7h7tfd+OdD36/75kfhft3N" +
                "Zx/sft0y7i/5/eQF7y89u5zz+fJX58S/UuH/xiN2P/k7j/L95K" +
                "M5n73n6udzaa+DBzyfpm3e+bzx++W930fjI32/f/tkr5fG3zpu" +
                "Rj+fzZnmTKfz608u+f9kZ072fNb4N15dytXt072nzdrqmO1zBL" +
                "mczfVm+8+hDvXg1C4cY13Mgz33iXQpI1T5mrRFvaGeVZZKcyWa" +
                "39xr7rV/s3u+Tv6C93w//qEjnIMDMc7RGnScvkIks4y5x6qUFV" +
                "0jXcwNLY7ceiKfmDtAQTlTPH821X7TXtcna+sksu97IJyDAzHO" +
                "0Rp0PPj8+rFmcsz5VEXJiq6RLubWKdJ683w+MXeAgnKmeH5W3u" +
                "wP14frnU6ytk7uPK3b0VxoLgzX0+enIwlzD9lcbzZltd0v2B7x" +
                "hFmMu3hM7tetKw/2njv6nWIaz2sdc1Vl741XeSpMETEok+fDzz" +
                "6Z707vjHztUJ/FLx3jXY2Xjr9yof8Xnx2cNWurY7bv/cMRzsGB" +
                "GOdoTftL5SdAtdvg7Ms/KmNQBlXKCp2RLuaGFkdsprK3VkQzxf" +
                "PrVNH1/Og307/lyjJ+H914vH49f+OxY7+eX/mo/D7qXV/k9+aS" +
                "3u/XH/h8Xj/a8znrfsjgL4/e/eTjmWl4ZXgFvj2bc805/n1kOW" +
                "45O/Lz7h5rzml9XRF3jPLR03VxZ653a1OxHvRtv9+v5LPwU3k0" +
                "d9H7S713iv+FfP9h7y+d+P2QdxZ5v29+72Hf773bbs3LVNyO/e" +
                "rn2+3DRzRrMRZkqfL5/LP71/hRPa9P7xW35kXR0q/qeeXwEc1a" +
                "jAVZqnw+/+z+NX5UL9bnGO7Pv3jC9+teOGqG/gdm05qs7R355U" +
                "8NR677Ws+494q4IjTXwSrivP4Hmz/gbjmj98o11ifR+3WlAu0I" +
                "vpyp/cY7ZTatydreEYsDg6/1jHuv4O7jqQjNdbCKOI81R4xQXm" +
                "qrT6I6NaodwZczdTrdf5lNa7K2d8TiwOBrPePeq9SJ7lHMM1hF" +
                "nMeaI0YoL7XVJ1GdGtWO4MuZWv9Ns2lN1vaOWBwYfK1n3HsFOt" +
                "+M0FwHq4jzWHPECOWltvokqlOj2hF8OVPrv2s2rcna3hGLA4Ov" +
                "9Yx7r0DnuxGa62AVcR5rjhihvNRWn0R1alQ7gi9nav33zaY1Wd" +
                "s7YnFg8LWece8V6Hw/QnMdrCLOY80RI5SX2uqTqE6Nakfw5Uwz" +
                "fx89uYz7Sx+y30dPHvX9kP59s2lN1vaOWBwY/OmVxH1d0Su41r" +
                "kfobkOVhHnseaIEcpLbfVJVKdGtSP4cqbp9cAzbs2rYerzflbO" +
                "IhHOScfsDM9DRal8Fv+8/vGUyjerT3PVrXnA1FOf97NyFonQL6" +
                "7/Nldn57laVRjrna82ysrjo/9odb1P9z2zaU3W9o5YHBh8rWfc" +
                "ewWf8+9FaK6DVcR5rDlihPJSW30S1alR7Qi+nGnW99Hgrx/e76" +
                "PRzoN9H9VmWt73Ue+a2bQma3tHLA4M/vQb85qu6BV8u16L0FwH" +
                "q4jzWHPECOWltvokqlOj2hF8OVP7CfBFs2lN1vaOWNyx0R58rW" +
                "d89D+uibNrEVMAFXEea2b1ea+SldXO4sgVaEfw5UztuX3ebFqT" +
                "tb0jFgcGX+sZ917B3/35CM11sIo4jzVHjFBeaqtPojo1qh3Blz" +
                "O1n6W7ZtOabHfXcY94jvpaz7j3Cj7ndyM018Eq4jzWHDFCeamt" +
                "Ponq1Kh2BF/O1L5WB2bTmqztHbG4Y8gv93nO6M/hO2lQfY9Ndb" +
                "CKOI81s/q8V8nKamdxNIPRnyJ1OV/G9H+sSxG+");
            
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
            final int compressedBytes = 1685;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWjuIJFUULUVBENFBUDFwEMSJBnaQDTZaZ7oKhRU12Bn/GM" +
                "hmCsJiprNjdTE0soGwk6jRgoIGZhpsImYaipotM5HBZos64g8/" +
                "/fr27XPvu7eqq3uqu+fTVfT73HveOecWNVXVNZ0kvLWe5JZGSd" +
                "K+bmNA2nkVpk5GYsJejWAcVkju4frD+P0qtV4VT+tpbmmEWPsr" +
                "GdNjOa/C1MlITNirEYzDCuu8Sn8Yv1+l1vN5WqepDX1oac4Ryi" +
                "OGsV4v48zlOj1dWsPAh3Th46RnTxHOrbfySqRG7EAzQi9WSpLH" +
                "d6kNfWhpzhHKI4axXi/jzGV9gt3LMUK68HHSs6cI59ZbeSXap8" +
                "5qRujFSt3xDWpDH1qac4TyiGGs18s4czk+b3jR2Id04eOkZ08R" +
                "zq238kq0T53VjNCLlQbn87Pc0ggxPdJjOa/C1MlITNirEYzr3z" +
                "v3ypyX6Q/j96vE6nKe1hlqQx9amnOE8ohhrNfLOHO5Ts+U1jDw" +
                "IV34OOnZU4Rz6628EqkRO9CM0IuV7PbuW8lIW/vrUdCX30lmul" +
                "1+e9IKazepDX1oac6R4k2KA8tjvV7GmcvT8qKxD+nCx63d7Lwh" +
                "2WJF5oo9lleSX419ageaEXqxUpJk71Mb+tDSXEYYQz2P9XoZ12" +
                "s01ovGPqQLHye9eYrMFXssr0TrWQeaEXqxkt3SD+yoma1JvnG5" +
                "mq6phuKH/f4THjXNPEuupmuqofiRHTXNPDuupmsacq/+Xd7f0x" +
                "/ifP7MyIw/D0Z/5I/1R38b1K+99jdn/b/de+JtNp4/Var4V2i3" +
                "FvuzX/r9n6EtTE3tfctQ3FK7un+6n/+KW6swfDxbS60l81y2VE" +
                "eFUBabtz3O8bbirvHW1dcfx2m6k+5QSz3Hwt461TqV7rz3IMU5" +
                "xllawTmJCagwJiwYex57nEDKHLPBGVxp1fBpX4u92xWoCLVKfc" +
                "mdX5Ur2H/sNHYij6R0f5DneecJ8fXk0G6T9dbJnSvTxcEz1xPJ" +
                "sdsmXVN+f/5Af3RH9/Nwfk/V+dnpOAx3U7+6FcUfMch7m3K9up" +
                "XfOaSuh8pWluAX1NW5+1SVL454JB8dcj9aaa2Ya/RKrSv5ShnW" +
                "45zuVl9/PKejnZ8uA5+fm1M8PzfHPj83p3N+ru5yu+q8tZax1d" +
                "0a9e6OntGoeipAaefD9av5y/Sxuh6Pvr+n3zV8rW7P/n7UdE3R" +
                "3e5KdoVa6jlG8/R7jkgMduQkRq8BIzQl0ubgDK60Klg9X1IbXj" +
                "hCNVluvcKrya9fVzXk/Pzx+D0vTbambCfboZZ6jvXn5zgiMdiR" +
                "kxi9BozQVBomB2dwpVXB6vmS2vAyiJyLK5YMcGBr8uvXVU3m+9" +
                "FJ3pp7XjLxCT4vJcn25+M9L5XiF0ykoef57S+P8/nT+bQW6rMx" +
                "r85n07PUUs8xmiMiMdiRkxi9BozQlEibgzO40qpg9XxJbV2FV4" +
                "1mgANbk19/VNVeutft97jvZfZ4jojEYEdOYvQaMA6UFdLmeCRd" +
                "aVWwer6ktq7Cq0YzwIGtya9fV5UtZ8vd+9Iy9b371DLvNJMROU" +
                "OOR/TRI/TMjzVWW9wtl62O9KUxGiEV4Ui60XnJIB1o5VhRsumq" +
                "5/f3Bp9uz6fnqaWeYzRHRGKwIycxeg0YoSmRNgdncKVVwer5kt" +
                "q6Cq8azQAHtia//qiqjXSj229w38ts8BwRicGOnMToNWAcKCuk" +
                "zfFIutKqYPV8SW1dhVeNZoADW5Nff1TVerre7de572XWeY6IxG" +
                "BHTmL0GjAOlBXS5ngkXWlVsHq+pLauwqtGM8CBrcmvX1fVvYaa" +
                "/5dm+yFGcbSIIdr5AnPwSMZsv/Sb7j7plGM4L/E65qF4tLVYrq" +
                "2xHmut7+r78SzbL14rnotxxSuTuloXrzbI9fxhvB+F41m8VKw3" +
                "ezyLF2Z/PIuXD8T+4mE6ntl95cezLDet41lPf1yX2UK2QC31HK" +
                "N5HAFWI+Kc5MV6aMZInbOKsWrsSiM0u3ZRzg3P0qV16tUv8VXv" +
                "lzrfHL/3S51vp/l+Sf8ezFlz5H8PZtAT/z1Yfvv8e2Ojb5jnx/" +
                "MEvp8f9/cM038/P3+/dLBt7WNqQx9amssIY6jnsV4v43aNzti1" +
                "2od04fuV3qwiuKxOWSWxL9+p1YuV5n/v3t97k7+v61w/Etf97f" +
                "n96GSen2sX7KihK/WF2XM1XVONNw2X5vfoRq8AF+fHoMkN7507" +
                "Px3dKrYWZ3Q2zu9HE/5+1LpmR0dvm5X3+fnZ6Pn5PzUIPEY=");
            
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
            final int compressedBytes = 1009;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWztvE0EQvg6kFBBRBIlnRHgFganSouTcUtJQUKSAiipC1L" +
                "nchVxFRUOJENR0iNJ/ACEK5B4pZYREB0jc3Xo8M7uzZ99lnTj2" +
                "bHS7szOzM99M9vZh2VGULCXno6okp4tnOTm7/SJqVJIzHv6Kwz" +
                "kXRRu9KEDZ6CULI1Bd9o306C/S3t7vgnOlYR5uVPUR5zNUaZtP" +
                "rz7LZ/q2bT6L/1jf1GVb1qZPOaBjWqCH//E+b6UxXOKO5TgoCm" +
                "GG9Tk21yPacv34IrFxyUhdf7YnnZ/S/Kw4LeZnvBlvmhpa6JVU" +
                "SUNNdWwNPx9o5HNttIsyoFDDto5WJVzUN49CisaNnqK0kbpxUu" +
                "Tlo/Mz5PzUfIbejzSfdj7TT4ebnza/aT699leiCZb03Qj5x8PM" +
                "z72/zfM56f1d0DxR62dLXJpPzedc5lPej+LHx5tPn3+ez/yU7u" +
                "/Tcf50S6j9fT6Lzk89z+t9c37mp66fJ3v93H0yy/MzzuPc1NBC" +
                "r6RKGmqqY2vgY/OBBopbo3ZRBhRq2F7RqoSL+uZRSNG40VOUNl" +
                "I3fiuqg/ig6g1aoJEPNfBQSmWoA3xqFe1Rm9SecO5mfqBn43Rx" +
                "gdzGSNHQUdwC9W/H5UdCZPvxftUbtEAjH2rgoZTK8IGaWkV73B" +
                "q16+ST+YGejdPFBXIbI0VDR3EL1D997JEuaiPrrnXXoqisTVtK" +
                "TA9oqFEH/1BGdfgYtAieuaYrA4qi4l7RqoSL+uZRSNFwC4jAjU" +
                "mOn0clrqtbw1vsUhTtLM7W/ruzcNQetz/rqUfvm3o/0nxqPlvd" +
                "N//pKpg/CLgf6f29dcnuFc990r+UXYd8ZjeFOf2txlb1fmR3Br" +
                "275n3PLgzlV5Pv3rGFbtYZ0CPf1Oxace55zzjOu5Z8tcYsV/Vt" +
                "x9ZF7/s75vue3cpWa9eBLZ1nQU9Qw3xuvG5noe24GV2LH2oO9D" +
                "w/veelnVWdZYcv6z+hNpQkdel6a80kXGs8L6jFkY/2X2/f5x9H" +
                "y3bW0/XU1KYFnunbHNSl2nQ88HEcjkef3AqVUVzUD/apHQkX94" +
                "iobDS2bYoeUbpIbSS2frP1c/fHfKyfrx4dzf19XvK5+yXcfpT8" +
                "0d0k6PnzqeYgaD6faQ4CnJfeQG0oSerS9daaSbjWeF5QiyMf7b" +
                "/evs8/jvbZ0fuRtB+1/X5I/lw/X5p00c+TNZ/T9PnSpPJ5kn/P" +
                "lX5o+3su4bz0UmdZ0JNT7/gtHHd0bSPodrodU5sWeKaPHKqDfy" +
                "ijOnwMWkSfVNOVITJExb2iVQkX9c2jkKLhFhCBG5McP49KXEl+" +
                "6Tuq+/v07O96P3L394qj318aI595Otl86vsetPwHfQuRcw==");
            
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
            final int compressedBytes = 2065;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW89rHWUUnUUFq4hKF3YhisX2D2hADbpIMvP+g9j8A806bt" +
                "p9X39IuguFql0IVoQuJIvudCsFKSKIFPfFXciq4E5wZr7cOefc" +
                "e1/TvD7bUmaG+e797r3fOed+nZd57yVdvbh6sapWL64e2Kqymf" +
                "kcQS1X83qLYx3WVweHR+EcapQHc8bJdCkjVHk1HpvVQ2VU6pX4" +
                "+uk705MlMn21vT6YvnXpQnWkY/rmjPiHIXKiWtgxff2Q/HtHxH" +
                "ubZ1d/bCPvHxHhTB5vXoneeDzxrj7j+7N57fnen7P49f7sI3Pc" +
                "n017BsY+1hz4NiJmfkMrqrCmqnJ0zjVkK7cWqxuqj4iNUzVLXx" +
                "VmjVPyuKM5NHbAeaw5VvVjsX3mmM0R4RqcyHGNrgHiwCyVMWce" +
                "q1JWoGa6mFu7yLpRBCiIPeX9a1eTc5NzVdWNne2O4tscWYuxV2" +
                "bmlUs9WMPHmsiNQxm9Bl+jFcwIRaxG84zACpTZMzIaoW5MNlq7" +
                "UWwf37CzzDjCM+TMK5d6sIaPNZGbutmIPKxLa7SCGaGI1WieEV" +
                "iBMntGRiPU9cl6a9eL7ePrdpYZR3iGnHnlUg/W8LEmclM365GH" +
                "dWmNVjAjFLEazTMCK1Bmz8hoqK8f1g+rqhuL7TJlZr6NqMGJHN" +
                "foGiCaUq2MOfNYlbICNdPF3NpF1o0iQEHsKe9fu1o5tXKqqrqx" +
                "s+UoszLHaDFkOYfLRkMCIqIc0xwO5bGZ1xl1Wd5rZDW8ShGgXv" +
                "vyK6PqkpssTZba+3Sp2P6+XbKzzDjCM+TMK5d6sIaPNZGbXm1L" +
                "kYd1aY1WMCMUsRrNMwIrUGbPyGjadbuvp7tz2OnT5ZLdP12qMH" +
                "JGR/Vy/MMOq7fabA1QTReU+QzHMnyfxy5wt4XDzscoO9OdmJVL" +
                "K0oVRs7oqF6Of+h+nlHEbA1QTReU+QzHMnyfxy5wt4XDzkzZ5E" +
                "+c3YwjJR9rfLXFSsTihg4W5uQ1nIu6Mg25dtXtNbKNee7F0JVZ" +
                "+XSHUL+yvLLc7vFysf1+L9tZZhzhGXLmlUs9WMPHmshNd8dy5G" +
                "FdWqMVzAhFrEbzjMAKlNkzMpp2HT7X/23e9s74fdHRjvpufbeM" +
                "Zm3WeZ1vI9f4itlx8xHXauAiB2VW4dGBmulibu0i6yZ2zyq90t" +
                "gnK9fIkNmdufu7j/232X2Sqmd2p+y+KPfs9leD9/X4Cl7Afn4z" +
                "eLfG3Tjiq+Jefa+MZm3WeZ1vI9f4Clw+br55isa4yEGZVXhWoG" +
                "a6mFu7yLqJ3bNKrzT2r11NfsHZvn/qL8x9hGfIWaxELF482OIh" +
                "qnjh/edMVYoTdSkjNLKNee7F0JVZ+XSHUF/v1Xvtvu6Z7Xd6z+" +
                "aIcA1O5LhG1wBx+JeUypgzj1UpK1AzXcytXWTdKAIUxJ7y/rWr" +
                "6afJ+8+P5/r92GcH9qNn/PvET17459G343NlkcfVf8Y9WOj9+f" +
                "24Bwvdzx/GPZj3WD27eraMxVqszH0EtVzN6y2OdVgPTkXhHOti" +
                "HswZJ9OljFDl1XhsVg+VUalX4uuP9vc21/5KnrAv4d+Dffn5vH" +
                "8PNv59Xbaf136ebz/rR/WjMhbbvzN9ZHNEuAYnclyja4A4vPOV" +
                "ypgzj1UpK1AzXcytXWTdKAIUxJ7y/rWr9DX97vhcmfeYnJ+cL2" +
                "OxFitzH0EtV/N6i2Md1oNTUTjHupgHc8bJdCkjVHk1HpvVQ2VU" +
                "6pVo/WRzstnaTbN9ZtPmPoJarub1Fsc6rB/2waFwDjXKgznjZL" +
                "qUEaq8Go/N6qEyKvVKtL452bRPo24stsuUmfk2ogYnclyja4Bo" +
                "+6CVMWceq1JWoGa6mFu7yLpRBCiIPeX9u64eNA9a+8Bsn3lgc0" +
                "S4BidyXKNrgDgwS2XMmceqlBWomS7m1i6ybhQBCmJPef+uq61m" +
                "q7VbZvvMls0R4RqcyHGNrgHiwCyVMWceq1JWoGa6mFu7yLpRBC" +
                "iIPeX9u66uN9dbe91sn7luc0S4BidyXKNrgDgwS2XMmceqlBWo" +
                "mS7m1i6ybhQBCmJPef/aVfbMv/Tv+L5n7vdLdyZ3ythZxGyO0W" +
                "LIcg6XjUCP+BzTnCoDj828zqjL8l4jq+FVisD8fOnolaB+7cRa" +
                "+xmwG4vtMmVmPkdQy9W83uJYh/XG7FE4hxrlwZxxMl3KCFVejc" +
                "dm9VAZlXolvr79lHS8jJ3txjK3yJUvStxi8IdPWsfVAiv5ferx" +
                "LMrri4LD6urj2/cYzTMaltc4u5Ppd16nKlBE8Hmm8fuQ7PuQK7" +
                "fm//+Ga7fL2NluLHOLlDxi8IfX3W21wIqcQPdrWAeryDBUc8YI" +
                "5VFb3onXlSuNfJ4p+X7+1/E5/RSvm/B6374/vt77yJyv9/pgrA" +
                "/G2n4C9177PBpqqor9quL1HK/TOmRmPGeGivrQurra/q0KWuuA" +
                "VSXa8k7c8ygoqN3qWjDq6nk9j7Z/f7mfR+PzfeHP9/0ydrYby9" +
                "wi23+UOGrNH558+2qBlTyb97Oo18Eq8rq1/cs/MZpnNCyvcXYn" +
                "+nqPChQRfJ4p+fx+oRqPp/kMv1PGznbjZMfiFrGaYic72XqO6x" +
                "qtnewcroNV5HWsLWM0LK9xdifKFxUoIvg80/jzc5Hvl5qbzc0y" +
                "Ftt/k3fT5ohwDU7kuEbXAHH4plAqY848VqWsQM10Mbd2kXWjCF" +
                "AQe8r7d13daG609obZPnPD5ohwDU7kuEbXAHFglsqYM49VKStQ" +
                "M13MrV1k3SgCFMSe8v61q/Tz5v3/6yf19PLisK688UI+jP4DQD" +
                "D4eQ==");
            
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
            final int compressedBytes = 1608;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE2LHFUUrSiC4leM4kwQFEFnkbjJYsCFC1PdrYJuRRkzBo" +
                "mKLoZZBPeptimThl6FbFyq4MzChQs3/oPs/AHuAroRVGQQ/Eis" +
                "qpfb59z7bk2nOtXTMVPV1Pu499xzz3151VNVkyRJhivD1aQ6hv" +
                "cX57PDo9knSaNj+GiN/bnI8njS2jF8cIb/6YZ8j/Fs9HlheaYh" +
                "w5pvb7qeDTJ+mtzlR3o1vRra0IstzGFhDD7wMUbHgBE5GRn7oA" +
                "yqdFawero4t67Cq0YzQEFck1+/rupg9+dhOk7/KG0Yed54vD9b" +
                "M49G3VoWoLTy2fn356/Lj2ifZ3BlcCW0oRdbmMPCGHzgY4yOAS" +
                "NyMjL2QRlU6axg9XRxbl2FV41mgIK4Jr9+XVV3vbd75F81u1/K" +
                "vzwc90uVZY77pUs/N1vPSz9169n4/vPfffbnbndN73f0d/u7oQ" +
                "292MIcFsbgAx9jdAwYkZORsQ/KoEpnBauni3PrKrxqNAMUxDX5" +
                "9euqkv/t82b+zZ15vXfruejvz/y77nuw1Xuobj3nPgYXBhdCG3" +
                "qxhbm1AMtojhc74hCPnJqFfayL82DOPJ4unRGqrBrLzeqhMlZq" +
                "lWh8ejI9mSRlG/rqTclJmVsLsIzmeLEjDvHTNzGGhX3A6DyYM4" +
                "+nS2eEKqvGcrN6qIyVWiUa39/qbxU/57ekr37yb8kcFsbgAx9j" +
                "dAwYp3cWChn7ZMSqdFawero4t67Cq0YzQEFck1+/rmowGUyKfT" +
                "qRvtq5E5lbC7CM5nixIw7x0+vUsLAPGJ0Hc+bxdOmMUGXVWG5W" +
                "D5WxUqtE49OVdKXYpyvSVzt3RebWAiyjOV7siEP89Do1LOwDRu" +
                "fBnHk8XTojVFk1lpvVQ2Ws1CrR+N613rUkKdvQl54wk7G0wOAD" +
                "H2N0DBhlHTQy9smIVemsYPV0cW5dhVeNZoCCuCa/fl1VsUf3om" +
                "ttr7QFO1rYtI/97I3HcRbL4+nQeG3zULNy99+wfo/1lu6N9uys" +
                "PLN74/ef2cNFWz3LZE9U7ZPF+VSSjAXxfPWu6fo04kRxvjB9B3" +
                "WDuF6sU5O9kr2WvVqNXq9BbGbv0uxc9lH2cXYku8egHsmO0uw4" +
                "jWc834yP3MS9lL2cpc3XMzujZu8X5wfpWlo8JZVt6KtvgjWZWw" +
                "uwjOZ4sSMO8dPvPcPCPmB0HsyZx9OlM0KVVWO5WT1UxkqtEotv" +
                "9vw+vu92n99P/73c9591+Q/7+5DFvk8eP3Aw6zl+qHs/3+3P5u" +
                "uZf9/e+7r0RDxqdswbdxDHYrWNzo3eGr0zelPZNm/7rdXxmmxn" +
                "632Nlb89w3+mmbZ5UE7cscGx0IZebGFuLcBqhPUxL+KR0yK1L8" +
                "5os1pVGqHZtYp6bmhmlbFSr37g0/V0vbgG1qWvroh1mVsLsIzm" +
                "eLEjDvHTK86wsA8YnQdz5vF06YxQZdVYblYPlbFSq0Tj+zv9ne" +
                "IZbEf66olsR+awMAYf+BijY8A4feJTyNgnI1als4LV08W5dRVe" +
                "NZoBCuKa/Pp1Vd3v45Z7v3TxvcNxv3Tx7Hzrmd/Ifx2u5r/k/2" +
                "A9899b2kHXI8sfLe7P32b4/5qb+c+bfUtPxumH8aht5uVxtV2T" +
                "+bO4a/fnZz8sY39267n46z3vLepqGK8e3HNlniZLOtKvQ1v2ZR" +
                "vmbBFM6GWs49kex2hPHKt1sApfL2uLM4IrzlNXidXlK43z2UzO" +
                "z/3zSXfMefQu9y6HVnqZlaNyLC1jLKLeLmPYNRq88EGZICw7WD" +
                "1dnFtX4VUTV88qrdK4TlauLXJ0/x6h5T37RXMPe+tQ+0e3o3AR" +
                "2Rr9LDqVngpt6MUW5tYCLKM5XuyIQzxyahb2sS7OgznzeLp0Rq" +
                "iyaiw3q4fKWKlVYvHd7zvafB+SbqQboQ19tdIbMrcWYBnN8WJH" +
                "HOKn+8qwsA8YnQdz5vF06YxQZdVYblYPlbFSq0Tje9u97eK7Zh" +
                "u9zMpROZaWMRZRb5cx7BoNXvhkBIRlB6uni3PrKrxq4upZpVUa" +
                "18nKy7O73pf5/vOwrOf8/z9Dt552PUffzr8/e5PeJLTSy6xX/b" +
                "1btIyxCJzWLmMZaTbmhU9GQNisYPV0cW5dhVdNXD2rtErj+nVV" +
                "3f7svj+79eT17G0udz3r8nf7887cn937pVaP/wAJZ5jW");
            
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
            final int compressedBytes = 1758;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWsFqHFcQXAiBQA6JySE5JQ6EfIEFAsUHjWZNwAchAiEkkm" +
                "46BxQCvmYdZKSTz/mH3EPu+oYQCR1sHSx/gkCHjOe5p6q6e3a9" +
                "0soo1syg9153V1dXv52ZXa00Gk0+nXw2ao/JB83Pl5OPf/t1NN" +
                "cx+ajH/1XwfDJa2DH5cEb88zn57gTPF3MyfN2Ow34udD/jMe9+" +
                "Dsf0Y9jPK903w/0+3O839qjv1ffKWGbzFRsexuBEjDGaA0bUZG" +
                "SMQRlUaVWwZrq4tnaRdaMMUBB7yvt3XS3VS828ZHMbWTIbHsbg" +
                "RIwxmgPGrrIgY8xWrEqrgjXTxbW1i6wbZYCC2FPev3Y1PD+v//" +
                "PnwbfDfi7q/Wjtr/KTH/2R+VHXfcxWcR06q7vV3TKW2XzF9h5g" +
                "Gc355kce8lFTWTjGurgObObJdGlFqPJqPDerh8qo1Cvx+OHz0k" +
                "KvzwfVgzKW2XzF9h5gGc355kce8lFTWTjGurgObObJdGlFqPJq" +
                "PDerh8qo1Cvx+Pne3w/eH96Ppr0fjR+NH5WxzK/8xbI1e4BlNO" +
                "ebH3nIt7qehWPAaB3YzJPp0opQ5dV4blYPlVGpV+Lxw/NzkcfB" +
                "d9H35KerMD758Tbv53h5vFzGMpuv2N4DLKM53/zIQz5qKgvHWB" +
                "fXgc08mS6tCFVejedm9VAZlXolHp9cs98P9+2l3yVXkv3s7veD" +
                "zTmYvnmd88Nb7mD5pu/x8Py8wvPz/vh+GctsvmJ7D7CM5nzzIw" +
                "/5qKksHGNdXAc282S6tCJUeTWem9VDZVTqlXj8//X7ur0/b+Ln" +
                "+fqsPitjmdtvRs/MhocxOBFjjOaAsfvmVZAxZitWpVXBmuni2t" +
                "pF1o0yQEHsKe9fuxofjg+b6/TQ5vbKPTQbHsZ4tP7YaExghJd9" +
                "GqM7XuqYpTyZLot7jaxGO2YGrs8/PjOqfh07H58387nNbeTcbH" +
                "gYgxMxxmgOGLvKgowxW7EqrQrWTBfX1i6ybpQBCmJPef+uq/3x" +
                "fjPv29xG9s32HmAZzfnmRx7yu8qOhWPAaB3YzJPp0opQ5dV4bl" +
                "YPlVGpV6L4arNqPmO+GsvcfvO0abb3AMtozjc/8pDffc/mWDgG" +
                "jNaBzTyZLq0IVV6N52b1UBmVeiWKr0/qk+Y5emJz+2Q9MRsexu" +
                "BEjDGaA8buyS3IGLMVq9KqYM10cW3tIutGGaAg9pT3r10Nf9/M" +
                "Py/9/sclPy8d18dlLHO708dmw8MYnIgxRnPA2L2SgowxW7EqrQ" +
                "rWTBfX1i6ybpQBCmJPef/a1drF2kX4u1/rK36M8NkamRpnf2Tn" +
                "mPEok8+20c5MLavI9XGGn6fpzBj6WNcu6t16t9nXXZvbnd41Gx" +
                "7G4ESMMZoDxu6VFGSM2YpVaVWwZrq4tnaRdaMMUBB7yvvXrqr1" +
                "ar15X1q3uX2nWjfbe4BlNOebH3nI796XHQvHgNE6sJkn06UVoc" +
                "qr8dysHiqjUq/E45Pn6i+d4qeX/Kvp09EtPaqH1cMyltl8xfYe" +
                "YBnN+eZHHvJRU1k4xrq4DmzmyXRpRajyajw3q4fKqNQrUXy1XW" +
                "0387bNbWTbbO8BltGcb37kIb/bB8fCMWC0DmzmyXRpRajyajw3" +
                "q4fKqNQrUXy1UW0084bNbWTDbO8BltGcb37kIb/bB8fCMWC0Dm" +
                "zmyXRpRajyajw3q4fKqNQr8Xg7Vl/aWFbw6UrXfUc/fprF/llV" +
                "ilJVmOvNlU3n76uP7Fk8q89tLKssGte9ep7PH1HUm1UBSpXPrj" +
                "+dv68+snOeg5+H3zcX+f087fypjWXlXpfTfN37+p7OH1HUm1UB" +
                "SpXPrj+dv68+svt45rs+9/65Hdfn3t+Xuz5XJ6uTMpa53fGJ2d" +
                "4DLKM53/zIQ373GjsWjgGjdWAzT6ZLK0KVV+O5WT1URqVeicfH" +
                "4/F7w/95LPT/Gf4d9mDYz5t7XN//f04ev/Pfh7yoXpSxzOYrNj" +
                "yMwYkYYzQHjKjJyBiDMqjSqmDNdHFt7SLrRhmgIPaU9++6OqqO" +
                "mvnI5jZyZDY8jMGJGGM0B4xdZUHGmK1YlVYFa6aLa2sXWTfKAA" +
                "Wxp7x/7ert3u/v/lE/q5+VsczmKzY8jMGJGGM0B4yoycgYgzKo" +
                "0qpgzXRxbe0i60YZoCD2lPevXQ1/L17s7+/VSrVSxjK3T4IVs7" +
                "0HWEZzvvmRh/zuSeNYOAaM1oHNPJkurQhVXo3nZvVQGZV6JYqv" +
                "t+qt5jrdsrm9crfMhocxOBFjjOaAsbszBBljtmJVWhWsmS6urV" +
                "1k3SgDFMSe8v61q+F+X+z9Xu/UO2Usc7vTO2bDwxiciDFGc8DY" +
                "vZKCjDFbsSqtCtZMF9fWLrJulAEKYk95/9rV6D8LnMxf");
            
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
            final int compressedBytes = 1395;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrFuJEUQnRCJABABRCBkDnJ/wck78w+XAYkDp4jwAtYIHS" +
                "siJBIiAsCSESEBSCQnISQihHQ6ycklkCIC4hU7W1t+71XXrNfr" +
                "8frO1z2a6u6q169e9c14ZkfXHrVHTdMetau+aXzmY7fA4ECMMb" +
                "oGjM2qKbKM+YhVaVawZro4t1aRVaMMUFDWlNevVXX3untN09u+" +
                "75uNfY6o+3hkMx/ZqSP0zo81ZW40zRg1RIwiOCMUsRqNMwMr0M" +
                "wxI7Np1bFN//LR7Iumti3a3Xf7AzM7FWEoWI6o1VHOv4kixmZr" +
                "wOq6oCxG2Jfxxzh2gau1HH5sWs3k5PIRjg6h1q++TNuEabxsV2" +
                "10v39Z792rt48/qntQ9/PpaO2d9o6P1GdzWPg0VsYj03BmW8Mo" +
                "xrvfrR/rKmC9OWusIioYUju8c5y93Wv3VvO988jSZ3NY+DRWxi" +
                "PTgMI9z8MoxrvfrR8lj6rwUc4aq4gKhtRm+iMiZ5l9Xe/bMdvs" +
                "21td3Tcbob4bL+Px5/WaGvVf8Pu6B1d4e39t+vpq9MLifGv68m" +
                "Xfl6YvDfjfLjyvjqj7xQvib1yS75XC8+YlGd6p+zn2fnaH3aFZ" +
                "65dfng59Hj3AMprXux/rsP78S1hg4Rgwmgdz5sl0aUaoimoiN6" +
                "uHylJpVKL47rQ7XfSn1q8ip5jDug9RjuF060xghJd9GqMdlTw+" +
                "izpLXR6PGlkNr1IGzs+n2qiE8fX35rU/33+pe7Cr5/uDD56P59" +
                "GD93bzfK/7Wd+XttnPT77adj+b5u7fZvu+tzZ3j8Xhw9gbx3hl" +
                "xDFn1qCDVeQ41pxlhPJS23AlqlOjyoh8MdOur8/Zr7f9+jz412" +
                "zf99bm7rE4fBh74xivjDjmzBp0sIocx5qzjFBeahuuRHVqVBmR" +
                "L2ZajOdm+763NnfP7DfzA+tjXc9+50p0zjNv1MEqctzB/PgnZo" +
                "sZnStqXF+J6tSoMiJfzFTf58duB/+Y7fve2tw9FocPY13PfufK" +
                "cmXeqINV5DjWnGWE8lLbcCWqU6PKiHwxU31fGvt5VO/3m/u9Of" +
                "vj6bg+Dx5ue30ePNzo99F/u/l9NPuz3u9Pz/0+Pb7td3v3qHtk" +
                "1nr32Tx6gFVEjDEv1iNnRGqszBizRlWKUHZVMcwNzayyVJrVz/" +
                "jyfv/s92fh+f7pD9d5vy89I/39rPt5lf1MlP5Y33q2/vt5v7tv" +
                "1nr32Tx6gGU0r3c/1mE9cioLx1gX58GceTJdmhGqoprIzeqhsl" +
                "QalSi+fdI+aZreWt9HbOZjt8DgQIwxugaMvg+KLGM+YlWaFayZ" +
                "Ls6tVWTVKAMUlDXl9WtVk8eTx/HaMJ/5YeHTWIw7I4+z1scck6" +
                "OYyXGsTVGqUHMrv1axTkG5Zthn2duT9mSxryfeL3f6xOfwMAYH" +
                "YozRNWA8/5cUZBnzEavSrGDNdHFurSKrRhmgoKwpr1+rWn293+" +
                "+P8y/7+3bK1/59Q8FypBzr+pL/ouZ4x2ZrwOq6oCxG2Jfxxzh2" +
                "gau1HH5sWk39HrK77yHPy/e6Md8/px/Wq2zbNplP5rnP/LDw+R" +
                "grNc7+yXxdZudRprjarR9DFaiCIVatgpWs36eLfLw/AfdzOXoG" +
                "r5Qb0l7/fl7395C6n9vv5+RwcmjWe59Nlv/vFpYxETHs9zH8ig" +
                "YvYj4CIrKDNdPFubWKrJqyelYZlZZ1svKFbzaZLfoZep/1o37s" +
                "ljERgTP6fewjZWNexHwERMwK1kwX59YqsmrK6lllVFrWr1Xt/n" +
                "6fvH+z9/tQ/ut7/6y/j+rzvT7fb+d+dmfdmVnrl1+az3wOD2Nw" +
                "IMYYXQPG8+/qgixjPmJVmhWsmS7OrVVk1SgDFJQ15fVrVfX6HP" +
                "l+/x8GjDJE");
            
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
            final int compressedBytes = 1405;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW81uG2UUHXZIqALEAsQCBIryApUKEqiizlgx6qYsKlWKus" +
                "qCrCo2PEBMAxV5A3bswiPwAn2FqnteIMqqShB4fHPnnHO/O67s" +
                "uqnVfN8o38+95zvn3NF4bE+Spinb4c9NbSu36cfTTy5n785+vp" +
                "h+sOz5nL4/EN8qIh+t0fd7L8l/tiTfh0Xk8yUZtpumvdnetN7G" +
                "Lm4rn3sPDA7kGKN7wOi6iixzPmNXqgrWzBdraxVZNcoAB2VNef" +
                "2hqr12bzbu+TjP7PkaEcbgQI4xugeMvbIgy5zP2JWqgjXzxdpa" +
                "RVaNMsBBWVNef6hqv92fjfs+zjP7vkaEMTiQY4zuAWOvLMgy5z" +
                "N2papgzXyxtlaRVaMMcFDWlNcfqjpoD2bjgY/zzIGvEWEMDuQY" +
                "o3vA2CsLssz5jF2pKlgzX6ytVWTVKAMclDXl9WtV49vj203T9T" +
                "Z2GVv5nCPAMpr3exz7sN+VIwvngFEdrJkn86WKcBXdRG52D5el" +
                "0+gk4st2/GX91LNqGz8dP7XeRo/ZGhHGRLT+eA927PUoxzSnzq" +
                "DjK+XJfHk+emQ3WjEzsD7/xJ2la6gVn+f/rdfZqm10Y3TDehs9" +
                "ZusYAZbRvN/j2If90FQWzrEv1sGaeTJfqghX0U3kZvdwWTqNTi" +
                "K+Xp/1++br/775+I/Vvm829XnIG70+f312Pa7P3+6ven0udz6P" +
                "frge5/Po3tWcz/q87m29fx7dfUn++3r/fBvun/X6XGe7c3jn0H" +
                "obPWbrGAGW0bzf49iH/dBUFs6xL9bBmnkyX6oIV9FN5Gb3cFk6" +
                "jU4ivr4frfP9aHwxvrDexvmTkgtfI8IYHMgxRveAsX8SI8gy5z" +
                "N2papgzXyxtlaRVaMMcFDWlNevVbW77W7TdL2N8yfNu75GhDE4" +
                "kGOM7gFj/yRbkGXOZ+xKVcGa+WJtrSKrRhngoKwprz9UNWkns3" +
                "Hi4zwz8TUijMGBHGN0Dxh7ZUGWOZ+xK1UFa+aLtbWKrBplgIOy" +
                "prx+rWp8Pj6fXafnPs6v3HNfI8IYHMgxRveAsX9lCLLM+YxdqS" +
                "pYM1+srVVk1SgDHJQ15fVrVel99Z/+Nx/f1ecby7Wd5zvP85jF" +
                "0SOmuZh3Rp4PKTsmRzGT49hbrEAzzKr8WsUiB+We4Vj0RZmTQd" +
                "aThZoni1GLdy91FZysB3NF1+xfy2c4O4RavHs9Dl+H2qu2+jy5" +
                "ns9Nae12u+0zjdkaPWKaK/ORaVjZ9jCK8R733o9FFbDfnDVWER" +
                "0MuR0+c6zebrWX37Lb/tu2xWyNHjHNlfnINOBwy3UYxXiPe+9H" +
                "yaMufJazxiqigyG3mf+IaLem3ySfP79a6fnEt5fjrSt+fvv1pt" +
                "8Djif1PljP5wafz7v1HKz1fN6r5+D6ff6sv9+sv9+s1+da7p8P" +
                "6j1v5e+bj9pH1tvoMVsjwhgcyDFG94ARmowsc3AGV6oK1swXa2" +
                "sVWTXKAAdlTXn9WlV9HnIFr/eH9Ry8Shu9sL4bu97WHvn9U4sD" +
                "63Pdz3HnyrSyaPTBLnLc6MUvfzNbVHSu6HG4kumf0ac6UEboRa" +
                "X69yHZ56V5pP795wacz6bZecf6bux6W3vE8ohh7o1zvDPimDNr" +
                "8MEuchx7zhThvPQ2XIn61KwyQi8qzV77p9Z3Y9fb2iOz++epY2" +
                "z0ue7nuHMl977TLBp9sIscNzqd3z+DV3YQHWVuWSPcPwsHygi9" +
                "qFQ/L639/f0/67ux623tEcsjhrnu57hzZVpZNPpgFzmOPWeKcF" +
                "56G65EfWpWGaEXlWbzM+u7sett7ZHHP1kcWJ/rfo47V+LzLItG" +
                "H+wix43Ojn9ktqjoXNHjcCXh9V44UEboRaX6/r7u9/d6/3xzzz" +
                "+fPLke12f9/80Naf8D8WyZtw==");
            
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
            final int compressedBytes = 1477;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXLFuXFUQXVkuUCgMogBRECFFfEEKIpAi8nZ/ASEo7TIFUk" +
                "RFEVgsInklfwASEilwYSoKCj4A/gFvRZk2SkUa9u14fM6ZO/fZ" +
                "az8ZK9y38tx7z8ycOXN9d/ftxspkUl7f/zZp1yWv2ePZY7M2Om" +
                "briCCWoznfceQhHzWVhX2si+tgzTyZLq0IVVFN5Gb1UFkqjUo0" +
                "fvpw+nAy6a2NvcdWPneLGDzg4xjNAaPvg0aWPp+xKq0K1kwX19" +
                "Yusm6UAQrKnvL+tavJZP72/B1D5q+tft6fv/HtV5ud8flOBb9T" +
                "IG+N98yav36O/70N+d4skNsbMnywtm0/R93P5DX1xavwzvDN7f" +
                "+m7mbnc/F1O59D53N6ND0ya+P6lfXI10A4Bg/4OEZzwHj2yi2R" +
                "pc9nrEqrgjXTxbW1i6wbZYCCsqe8f+0q2+dNXz/bpdeDn832Y2" +
                "9tzYjH2OhzzWe8zFFPmas6WEWul7WVFcFV1ql1EnXlSst6ineP" +
                "ukdmffRVP+vnbjkmRtRxnwPXaPDCB2UeEdnBmuni2tpF1k3ZPa" +
                "uMSss+WfkKO+wOV+MhRl/1s37ulmNiBH4i7nOfKRvzwuczRMSq" +
                "YM10cW3tIuum7J5VRqVl/9pVu/9s9/M3eT9ny9nSrI3r+/mlr4" +
                "FwDB7wcYzmgPHs84JElj6fsSqtCtZMF9fWLrJulAEKyp7y/rWr" +
                "dj7b8/069nP/h7afY+3n/q/jfh/SPh9d3/ch7fWzPd/bfrb3o/" +
                "Z+ZPvZ7Xa7Zn30VT/r5245JkbUcZ8D12jwwuczRER2sGa6uLZ2" +
                "kXVTds8qo9KyT1be7U53psXpMsxwWGDqK/2MT3dqv83eZzm1KM" +
                "fd+iOPUoVaW3O0iyEFZU4d8+rt+T7u+1F5dU9rGXUPe2tRw9mb" +
                "XBdhGq/aRspedi9zzHBYYD5HpvoZL9nZ5zzKFLPd+qPWgSqosW" +
                "oXrGR4n87DrObixzLuyRdX+Q09+fz//Olodnd216yNjtk6Iojl" +
                "aM53HHnIR01lYR/r4jpYM0+mSytCVVQTuVk9VJZKoxKNn39U7v" +
                "HB2flc/LTBq/HHp9mfXfPn5Q9v1Pm8N7tn1kbHbB0RxHI05zuO" +
                "POSjprKwj3VxHayZJ9OlFaEqqoncrB4qS6VRicYPn8+Nzkk7n/" +
                "2u3p/dN2ujY7aOCGI5mvMdRx7yUVNZ2Me6uA7WzJPp0opQFdVE" +
                "blYPlaXSqCTGJ+fz0/Yt5mWv6bPpM7M2OmZrIByDB3wcozlgRE" +
                "2OLH1QBlVaFayZLq6tXWTdKAMUlD3l/WtX2R4vjto5G/Nq/94x" +
                "xvXJ325tlnnL+TDbZh6NulgVRKny8+sP89fqI/tiPOtPjH+109" +
                "We720/X82rfZ98Gj/O38/vTffM2ri+k9rzNRCOwQM+jtEcMJ7d" +
                "qUlk6fMZq9KqYM10cW3tIutGGaCg7CnvX7vqlt2y+J50jRkOC0" +
                "x90e+MPE+/oV16nVoUM3kca9MoVai1lV+7GFJQ5tSx015OupMi" +
                "bo0ZDgtMfdHvjDxPFZ54nVoUM3kca9MoVai1lV+7GFJQ5tSxqI" +
                "s8x1XW48Gax8NRw9mbXBdhGq/aVa/FH+1detT9/LPtQbtfuhn3" +
                "S9l18Es7ZWNe7f+7uOK75S2z/dhbWzty8K7hiPW55jPuXFmtDI" +
                "06WEUe19367ndmixWdK2qsdzJ/GnWqAmVEvVhpMnnwj9l+7K2t" +
                "HTE/MMw1n3HnKvcC7JnPI1hFHseas4pQXmqrd6I61auMqBcrrf" +
                "Z2y2w/9tbWjqzO55bH2OhzzWfcuZKztZWhUQeryOO6rfX5DFpZ" +
                "QVSUqeUa4XwWCpQR9WKl1d4+N9uPvbW1I+YHhrnmM+5cye/9eY" +
                "ZGHawij2PNWUUoL7XVO1Gd6lVG1IuVVnu7bbYfe2trR/a/NByx" +
                "Ptd8xp0rOVvbGRp1sIo8rttenDBbrOhcUWO9k3A+CwXKiHqxUr" +
                "v/zO4/L/338/8Czdnv0A==");
            
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
            final int compressedBytes = 1436;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWkFqXEcQ/aBNQKDEySJZRQRMDuGd5nsgtwgxNt5oYQTZGe" +
                "EMJgicE+QEMmiTRS7gbHwELQzxJj6Btob8nnbpvVdVPdJMJkjY" +
                "3cNUV1e9fvWq+X/me+RhWHy9+GZYjsVn0/u7xRe//DysNRafN+" +
                "J3Q+SrYWtjsXtF/ts1+e7w6vnvU2R/TYbvh2H2eva62jqXeF2Z" +
                "bxYYvJBjjO4Bo9VVZMyZx6q0KlgzXVxbu8i6UQYoiD3l/WtXw5" +
                "rX54u/b8f1OXu16fU5e3Wd6/PkYrPrMxvr3u99yKn2z8/k+lxG" +
                "Nro++3lu9zzj+PXPftduOuZP50+rrbPF6tpHgGU077c49mE/ai" +
                "oL51gX18GaeTJdWhGqvBrPzeqhMir1Sjx+vfv95KdP434/+XHz" +
                "+312Wm2Zi61rjhimzuZfPoGc6pzt0UzcqzpYRfLEc6raYkVwxT" +
                "qtTryuXGms5yv176P+/d7Ps59nP89+nv08P7bf6/p5xvN8/sfm" +
                "1+f4aHxUrc22Kl7xzTLGI9px8xFXNHiRMw8Izw7WTBfX1i6ybm" +
                "L3rNIrjX2yco303+v++xifjE+qtdlWxSu+WcZ4RDtuPuKKBi9y" +
                "UGYIzw7WTBfX1i6ybmL3rNIrjX2y8vLun5/9+71/v38y3+8Px4" +
                "fV2myr4hXfLGM8oh03H3FFgxc584Dw7GDNdHFt7SLrJnbPKr3S" +
                "2Ccrn2Lvx/fhO2oZq3FYxMzHTs1zPLJzzniUye82a69MLavI9f" +
                "EOP6/SmTG0WCee8/E84JaxGodFTHM+b4zspwrPrU4LxUyGY22K" +
                "UoVaW/m1i1UK4p52rFa//+D+g2Eots4lU1fmmwUGL+QYo3vAaJ" +
                "UVGXPmsSqtCtZMF9fWLrJulAEKYk95/9rV/N783jAUW+eSqSvz" +
                "OQIso3m/xbEP+62yZ+EcMFoHa+bJdGlFqPJqPDerh8qo1CtR/G" +
                "xvtjcMxdZ5+dv9nq19BFhG836LYx/2X/7lwLFwDhitgzXzZLq0" +
                "IlR5NZ6b1UNlVOqVeHx/Xtru86eNg3/MVk8Hx7L8Kvx1M4q6Xh" +
                "WgVPnV9Vfzt+pj91U8B2/NVi/LRr+p5+36GUVdrwpQqvzq+qv5" +
                "W/WxO+eZvZu9q7bOFqtrRBiDF3KM0T1gRE1GxhyUQZVWBWumi2" +
                "trF1k3ygAFsae8f+2q/173/49+ntsdB3/dPMNNd7dpB+Ob8U0e" +
                "q3FYxDTn88bIfquyYXIUMxmOtfkONMOsyq9drFIQ97RjXhfGix" +
                "/6PbrN0c9zu+O3O/0MtnqeX/Yz2Obo/9978zF/OX9ZbZkRszWs" +
                "xZDlHN5mwR75OaY5VYY6tvI6oy7Le42shncpA9fnt1qvhFjP5m" +
                "fTfFbnD5kzrGEthizn8DZrTGBElGOaI31Sx1ZeZ9Rlea+R1fAu" +
                "ZeD6/FbrlTB+emrarbbMxY4ffgkzr8YtBv/yyWtXZ3Alz2y7Y/" +
                "N3NuhgFTmONWcVoTxqa3eiOjWrjKjnK/XfP7PfP5eR/vf3W3Ce" +
                "07W6U22Zi61ri9Q8YvAv74QdncGV3Ec7WdTrYBU5jjVnFaE8am" +
                "t3ojo1q4yo5yv16zO7Pvv/t+mfnx/j9Tk/nh9XW+flk9SxrX0E" +
                "WEbzfotjH/ZfPqk5Fs4Bo3WwZp5Ml1aEKq/Gc7N6qIxKvZKAvw" +
                "i9XJRYjcMipjnOczb6sYrnyXQoXmMZyrxn++3ais1Yr/Vvywu/" +
                "ylk24b5949n+zdTtn5/b/D4aH4+Pq7XZVsUrvlnGeEQ7bj7iig" +
                "YvcuYB4dnBmuni2tpF1k3snlV6pbFPVj7FjsajaT7CbKviFd8s" +
                "YzyiHTcfcUWDFznzgPDsYM10cW3tIusmds8qvdLYJyufYofj4T" +
                "QfYrZV8YpvljEe0Y6bj7iiwYuceUB4drBmuri2dpF1E7tnlV5p" +
                "7JOVT+9/AR5+hEQ=");
            
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
            final int compressedBytes = 1727;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWU2LXFUU7E1EcKHiQslCEcR/kIkQcZGXt4gScCn4B5KBBA" +
                "aTQJKVrW4mA8naneswzNpfMPODkr339p2aqlP3POIkjQ7kdnjn" +
                "s26dOo/u6Y9MB9PBajUdTGceWY1qDKsYR/DyOmJEkU152UNEhE" +
                "8la6ZLZ8ctsm367VWlK+33t632pr3i9+iR1ajGsIpxxHIdMesR" +
                "TV72EBHh7GTNdOnsuEW2Tb+9qnSl/Z6qvNTuTHeKv0OPrEY1hl" +
                "WMI5briFmPaPKyh4gIZydrpktnxy2ybfrtVaUr7fdU5aX2dHpa" +
                "/FN6ZDWqMaxiHMHL64gRRTblZQ8RET6VrJkunR23yLbpt1eVrr" +
                "TfP261vrbqHvs/Izr4bvWvH+tvT0//tPpPH+tvVhfoMV+ZrzTb" +
                "PGot9wqxitbzqPMcz3NmZNGe6tI5zJUn0xUnUpWrcW5VT5W9Ul" +
                "cS8fO1uTxDq21+07mG3CvEKlrPo85zPH92H4xFe8TEOcyVJ9MV" +
                "J1KVq3FuVU+VvVJXEvHz8Xxc/DH8pnOMnBXFODpesGAiI6taiz" +
                "25o2EOssiT6ULfNaqauLEy6Hy9/GSvGtj1p+vPTv8SvV+uL9cf" +
                "/frgnH/BPlyof9VVPtni380PXtP//Jx8H2v2+5+l8sU5Gb7O6+" +
                "e9n//P448fXtO/eVGUHtxcjcc27+f34x5s8zFe72/x+fPF/KLZ" +
                "6llDTosau9rjBUv2nl9rsReVcQ4y19nrQt81qho9FRl0vl7Ruh" +
                "LFJ6/3H8dr9N17vV/cx/WXzVZfbctR2b/c6sQijue1Dq5sVlZ1" +
                "Haoix11/+dvfyuYTweUalzdZ/+U6o4LIyHk+aXyezz7Pbypv9H" +
                "l+3M9t3s/5yfyk2eY371RPkHuFWEXredR5jufP3gmNRXvExDnM" +
                "lSfTFSdSlatxblVPlb1SVxLxN27duLVaVdt87bQMMSwx/MeeYu" +
                "IZMuI+RGTfQ6Sq4lSyZrp0dtwi2yYyUEG/U75/3Gq83rf7ep/u" +
                "T/ebhUdWoxrDKsYRy3XErEc0edlDRISzkzXTpbPjFtk2/faq0p" +
                "X2e6ryUrs93S7+Nj2yGtUYVjGOWK4jZj2iycseIiKcnayZLp0d" +
                "t8i26bdXla6031OVl9qj6VHxj+iR1ajGsIpxxHIdMesRTV72EB" +
                "Hh7GTNdOnsuEW2Tb+9qnSl/Z6qvNSeT8+Lf06PrEY1hlWMI3h5" +
                "HTGiyKa87CEiwqeSNdOls+MW2Tb99qrSlfb7x63G+9GW348eTg" +
                "+bhUdWoxrDKsYRy3XErEc0edlDRISzkzXTpbPjFtk2/faq0pX2" +
                "e6ryUns2PSv+GT2yGtUYVjGO4OV1xIgim/Kyh4gIn0rWTJfOjl" +
                "tk2/Tbq0pX2u8ftxqv9y1/39yZd5ptfvPNaQe5V4hVtJ5Hned4" +
                "/ux7orFoj5g4h7nyZLriRKpyNc6t6qmyV+pKIn4+mU+KP4HfdE" +
                "6Qs6IYR8cLFkxkZFVrsSd3NMxBFnkyXei7RlUTN1YGna+Xn+xV" +
                "c1r3+/ze+E14/D5/cR8Hv4x78Bbvk+P9ffw+f4HvZ/kUeqnZ6q" +
                "ttOSr7l1udWMRnn2QvRU+ufibZsx4QqiLHTZfq/8e5VlXgijK1" +
                "OiP+f1yvIDJynk9ara6/arb6aluOSuuzxjie1zq4kv+bfJVVXY" +
                "eqyHGqOZtI5b225U2iztiNjJznk8brfau/hzyYHjQLj2za3FVa" +
                "xThiuY6Y9YgmL3uIiHB2sma6dHbcItum315VutJ+T1Ver/H8HO" +
                "/v436O+znu5xv9Xnd1vtps85tfSq4i9wqxitbzqPMcz5/9EmMs" +
                "2iMmzmGuPJmuOJGqXI1zq3qq7JW6Escn39/3x7fwt3lM7zVbfb" +
                "UtR6X1WWMcz2sdXNmsrOo6VEWOU83ZRCrvtS1vEnXGbmTkPJ80" +
                "/n6O96NxP9+Z75t3p7vNwiOrUY1hFeOI5Tpi1iOavOwhIsLZyZ" +
                "rp0tlxi2ybfntV6Ur7PVV5vcbzc6vPz91pt1l4ZDWqMaxiHLFc" +
                "R8x6RJOXPUREODtZM106O26RbdNvrypdab+nKp9256P5qHwOPW" +
                "r+9JPpEXNa1NjVHi9YMJGRVa3FnnyiD3OQuc5eF/quUdXoqcig" +
                "8/WK1pUI6+F8WPxh86edQ+a0qLGrPV6wYCIjq1qLPdEX5iBznb" +
                "0u9F2jqtFTkUHn6xWtKyF+ejw9Ls/Tx/TIalRjWMU4YrmOmPWI" +
                "Ji97iIhwdrJmunR23CLbpt9eVbrSfk9VXmr3pnvF36NHVqMawy" +
                "rGEct1xKxHNHnZQ0SEs5M106Wz4xbZNv32qtKV9nuq8nL9AxXv" +
                "lpM=");
            
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
            final int rows = 7;
            final int cols = 84;
            final int compressedBytes = 355;
            final int uncompressedBytes = 2353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNp1k+sNg0AMg9ntxDQMABI7Ibph70Tdz0mOSpeHHRznR9ve9m" +
                "Vpe/tndaMataLP5Il3XDV4nEYXThUTWR3VmS/fHa+YXVOvd5fZ" +
                "ab3TnXdsa1vPG1ndqEat6DN54h1XDR6n0YVTxURWR3Xmy3fHK2" +
                "bX1OvdZXZa73TnbVs/62dZRhz5+T3d0xOFwTrHU5QSiqCORY5f" +
                "3KMu+6y+xGeP7sa/igq+31+M2YmpXuvV8/XkH3PRE4XBOsdTlB" +
                "KKoI5FzvyFPeqyz+pLfPbobvyrqOD7/cWYnZjqvd4930/+MTc9" +
                "URisczxFKaEI6ljkzF/Yoy77rL7EZ4/uxr+KCr7fX4zZCfPtbG" +
                "f/359kdaMataLP5Il3XDV4nEYXThUTWR3VmS/fHa+YXVOvd5fZ" +
                "ab3TnXfsaEfPB1ndqEat6DN54h1XDR6n0YVTxURWR3Xmy3fHK2" +
                "bX1OvdZXZa73Tn/X0BNE8iFQ==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 6, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 19, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 0, 34, 0, 0, 0, 0, 0, 35, 0, 4, 0, 36, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 6, 0, 0, 0, 38, 39, 7, 0, 40, 8, 0, 0, 0, 41, 42, 0, 43, 0, 44, 0, 0, 9, 0, 45, 0, 10, 46, 11, 0, 47, 0, 0, 0, 48, 49, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 50, 0, 0, 0, 0, 0, 0, 0, 51, 1, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 2, 0, 14, 0, 15, 0, 0, 52, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 0, 1, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 53, 0, 0, 0, 20, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 55, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 56, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 57, 0, 58, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 59, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 60, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 11, 0, 2, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 61, 14, 0, 62, 0, 0, 0, 63, 0, 0, 0, 64, 65, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 14, 0, 0, 66, 15, 0, 0, 16, 0, 0, 67, 17, 0, 0, 0, 0, 0, 24, 25, 26, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 27, 28, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 32, 0, 0, 0, 4, 0, 0, 33, 0, 1, 34, 2, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 35, 36, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 38, 3, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 39, 16, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 1, 0, 0, 0, 1, 6, 0, 5, 0, 43, 0, 7, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 68, 45, 0, 46, 47, 48, 0, 49, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 54, 0, 1, 0, 55, 0, 0, 8, 56, 0, 57, 0, 58, 0, 0, 0, 6, 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 59, 60, 9, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 8, 61, 62, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 63, 0, 0, 0, 0, 0, 69, 0, 0, 0, 64, 0, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 66, 67, 17, 18, 0, 19, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 68, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 69, 25, 26, 0, 0, 0, 70, 71, 0, 0, 0, 4, 0, 72, 0, 5, 0, 0, 70, 73, 1, 0, 0, 0, 27, 74, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 1, 0, 71, 0, 0, 0, 0, 0, 0, 72, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 0, 0, 19, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 11, 0, 75, 76, 12, 0, 73, 77, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 0, 78, 79, 9, 0, 0, 2, 0, 80, 0, 0, 81, 1, 82, 0, 3, 0, 0, 0, 0, 0, 83, 0, 2, 0, 0, 0, 0, 0, 0, 84, 85, 0, 0, 0, 0, 0, 0, 0, 0, 86, 87, 0, 3, 0, 4, 0, 0, 88, 1, 89, 0, 0, 0, 90, 91, 92, 0, 13, 93, 94, 95, 96, 0, 97, 74, 98, 1, 99, 0, 75, 100, 101, 102, 76, 14, 2, 15, 0, 0, 103, 104, 0, 0, 0, 0, 105, 0, 106, 0, 107, 108, 5, 0, 0, 0, 0, 0, 0, 0, 10, 0, 4, 109, 5, 1, 0, 0, 0, 0, 1, 110, 111, 0, 0, 4, 112, 0, 6, 113, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 114, 0, 0, 0, 0, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 6, 16, 0, 17, 115, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 18, 0, 0, 19, 0, 0, 0, 0, 0, 116, 7, 0, 117, 118, 0, 11, 0, 0, 0, 12, 0, 119, 0, 0, 20, 0, 2, 0, 0, 7, 0, 0, 0, 4, 0, 120, 121, 0, 5, 0, 0, 0, 0, 0, 122, 0, 0, 0, 123, 124, 125, 0, 8, 0, 126, 0, 9, 13, 0, 0, 2, 0, 127, 0, 2, 3, 128, 0, 0, 14, 129, 0, 0, 0, 15, 10, 0, 0, 0, 0, 77, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 130, 131, 0, 132, 133, 134, 0, 135, 0, 0, 1, 0, 0, 0, 136, 0, 0, 23, 24, 25, 26, 27, 28, 29, 137, 30, 78, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 138, 46, 47, 48, 49, 139, 50, 51, 52, 55, 56, 57, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 140, 141, 142, 0, 143, 0, 59, 4, 79, 0, 144, 7, 0, 0, 145, 146, 0, 0, 11, 60, 147, 148, 149, 150, 80, 151, 0, 152, 153, 154, 155, 156, 157, 158, 61, 159, 0, 160, 161, 162, 163, 0, 0, 7, 0, 0, 0, 0, 0, 62, 0, 0, 0, 164, 0, 165, 0, 0, 0, 0, 1, 0, 2, 166, 167, 0, 0, 168, 0, 169, 12, 0, 0, 0, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 171, 172, 173, 0, 174, 0, 8, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 175, 176, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 82, 0, 13, 0, 0, 0, 177, 2, 0, 3, 0, 0, 0, 14, 0, 178, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 179, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 180, 0, 181, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 182, 0, 183, 184, 185, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 186, 0, 187, 188, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 189, 0, 0, 0, 0, 190, 22, 17, 0, 0, 0, 0, 0, 0, 191, 0, 0, 1, 0, 0, 18, 192, 0, 3, 0, 0, 7, 9, 1, 0, 0, 0, 1, 0, 193, 23, 0, 0, 0, 0, 24, 0, 0, 19, 10, 11, 0, 12, 0, 13, 0, 0, 0, 0, 0, 14, 0, 15, 0, 0, 0, 0, 0, 194, 0, 0, 195, 0, 0, 0, 196, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, 0, 0, 197, 0, 0, 198, 199, 20, 0, 0, 200, 0, 201, 0, 0, 21, 0, 0, 0, 83, 0, 26, 0, 202, 0, 0, 0, 0, 0, 203, 22, 0, 0, 0, 0, 18, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 204, 0, 0, 0, 0, 0, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 0, 0, 1, 205, 206, 2, 3, 0, 0, 0, 0, 0, 0, 207, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 208, 0, 0, 0, 209, 64, 0, 210, 0, 3, 0, 0, 0, 65, 85, 0, 0, 24, 0, 0, 0, 27, 211, 0, 212, 25, 28, 0, 213, 214, 0, 26, 215, 0, 0, 216, 217, 218, 219, 29, 220, 27, 221, 222, 223, 28, 224, 0, 225, 226, 6, 227, 228, 30, 0, 229, 230, 0, 0, 0, 0, 0, 66, 0, 2, 0, 0, 231, 232, 0, 233, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 234, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 235, 0, 23, 24, 30, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 236, 4, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 237, 238, 1, 0, 1, 32, 0, 0, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 239, 69, 0, 0, 240, 0, 0, 241, 242, 0, 0, 0, 0, 33, 34, 0, 0, 3, 0, 0, 243, 0, 244, 0, 86, 245, 0, 246, 0, 0, 35, 0, 0, 0, 247, 0, 248, 36, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 249, 0, 250, 0, 1, 38, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 7, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 36, 0, 37, 251, 0, 41, 0, 252, 0, 38, 253, 254, 39, 255, 0, 256, 0, 0, 0, 0, 0, 0, 257, 40, 258, 41, 0, 0, 0, 0, 0, 259, 0, 260, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 261, 262, 0, 0, 263, 0, 8, 0, 0, 43, 0, 264, 265, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 267, 268, 269, 270, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 271, 0, 0, 0, 0, 272, 44, 10, 0, 0, 12, 0, 13, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 70, 0, 0, 273, 0, 0, 0, 274, 0, 0, 0, 0, 45, 0, 0, 275, 276, 277, 0, 46, 278, 0, 279, 47, 48, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 0, 8, 49, 283, 284, 50, 285, 0, 0, 51, 0, 4, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 0, 289, 290, 52, 0, 0, 53, 0, 0, 291, 0, 0, 0, 292, 0, 0, 0, 293, 1, 0, 0, 0, 5, 2, 0, 294, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 295, 44, 0, 0, 0, 0, 0, 71, 0, 0, 54, 0, 0, 0, 0, 0, 0, 0, 0, 296, 0, 0, 0, 0, 2, 0, 297, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 298, 45, 0, 0, 0, 299, 0, 0, 0, 0, 0, 300, 0, 0, 0, 0, 0, 0, 55, 0, 0, 56, 0, 301, 0, 0, 0, 0, 0, 0, 57, 0, 0, 36, 0, 0, 0, 37, 5, 302, 6, 303, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 304, 305, 3, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 306, 0, 307, 0, 308, 309, 0, 0, 0, 0, 0, 0, 310, 0, 0, 0, 7, 311, 0, 0, 0, 58, 0, 312, 0, 0, 313, 0, 0, 314, 315, 0, 46, 316, 0, 0, 0, 59, 87, 0, 0, 0, 317, 318, 60, 0, 61, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 88, 0, 0, 0, 2, 47, 62, 0, 0, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 319, 0, 48, 320, 49, 72, 0, 50, 0, 0, 0, 0, 321, 64, 0, 0, 322, 65, 66, 0, 51, 0, 323, 67, 324, 0, 68, 52, 325, 326, 69, 70, 0, 53, 0, 327, 328, 0, 54, 71, 329, 0, 55, 0, 0, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 330, 0, 9, 331, 0, 0, 332, 333, 334, 73, 0, 0, 0, 335, 0, 0, 336, 337, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 338, 74, 0, 0, 0, 0, 75, 0, 0, 38, 0, 0, 0, 0, 0, 339, 59, 340, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 341, 0, 342, 343, 0, 0, 0, 28, 0, 0, 0, 344, 0, 0, 0, 0, 0, 0, 61, 345, 346, 0, 0, 62, 347, 0, 63, 348, 0, 64, 349, 65, 0, 0, 76, 0, 0, 350, 351, 0, 0, 77, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 352, 353, 89, 0, 354, 0, 0, 0, 355, 0, 0, 0, 78, 0, 0, 0, 0, 0, 66, 0, 79, 0, 356, 0, 80, 67, 357, 0, 358, 359, 360, 81, 82, 0, 361, 83, 68, 362, 363, 364, 365, 0, 84, 0, 0, 0, 366, 0, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 0, 0, 0, 0, 0, 69, 367, 0, 70, 0, 0, 0, 85, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 368, 0, 369, 86, 370, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 371, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 73, 0, 87, 88, 74, 0, 75, 372, 89, 76, 77, 373, 0, 374, 375, 0, 0, 376, 377, 0, 0, 0, 7, 0, 90, 90, 0, 0, 378, 0, 379, 0, 380, 381, 0, 91, 382, 383, 384, 385, 92, 93, 0, 0, 0, 386, 0, 0, 387, 388, 389, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 390, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 391, 0, 392, 0, 0, 96, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 97, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 393, 394, 0, 395, 0, 396, 397, 0, 0, 0, 0, 98, 99, 0, 0, 0, 91, 92, 0, 100, 101, 102, 398, 0, 103, 104, 0, 0, 0, 0, 80, 0, 0, 105, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 399, 0, 400, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 401, 0, 0, 0, 0, 0, 0, 0, 402, 106, 107, 0, 82, 108, 0, 83, 403, 404, 0, 0, 0, 405, 0, 0, 109, 0, 0, 84, 0, 406, 0, 0, 85, 0, 407, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 408, 0, 0, 0, 409, 0, 410, 0, 87, 0, 411, 0, 88, 110, 111, 112, 0, 412, 0, 113, 413, 414, 0, 114, 415, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 116, 0, 117, 416, 0, 417, 0, 0, 118, 418, 0, 119, 120, 419, 0, 121, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 122, 123, 0, 124, 0, 0, 125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 2, 0, 0, 6, 7, 8, 1, 5, 1, 2, 0, 3, 9, 1, 5, 5, 10, 1, 0, 11, 10, 12, 4, 11, 1, 13, 5, 10, 1, 6, 7, 0, 14, 15, 16, 17, 10, 11, 18, 16, 2, 16, 19, 5, 6, 11, 20, 3, 17, 7, 21, 22, 23, 24, 1, 1, 25, 26, 2, 27, 28, 1, 29, 30, 0, 4, 31, 10, 2, 32, 0, 11, 33, 34, 6, 1, 1, 8, 35, 36, 16, 2, 37, 38, 3, 1, 39, 1, 5, 6, 40, 41, 10, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 1, 48, 49, 3, 5, 50, 9, 51, 52, 53, 54, 1, 1, 6, 1, 55, 56, 1, 4, 5, 1, 57, 0, 58, 59, 20, 7, 60, 61, 62, 63, 2, 18, 15, 64, 65, 66, 10, 67, 20, 68, 3, 69, 6, 70, 0, 71, 72, 73, 0, 0, 1, 20, 74, 2, 75, 76, 77, 21, 2, 78, 18, 79, 80, 81, 6, 82, 83, 6, 6, 7, 2, 84, 3, 85, 86, 3, 87, 1, 88, 1, 89, 90, 91, 22, 92, 93, 94, 95, 3, 96, 97, 0, 0, 98, 7, 4, 99, 100, 101, 102, 2, 103, 104, 105, 0, 106, 107, 3, 108, 0, 109, 16, 7, 6, 4, 27, 110, 111, 6, 4, 112, 3, 6, 1, 113, 8, 9, 114, 115, 0, 116, 4, 117, 118, 119, 120, 121, 122, 123, 10, 26, 0, 124, 8, 1, 1, 125, 126, 2, 25, 0, 4, 0, 127, 10, 2, 11, 128, 29, 129, 130, 131, 0, 11, 29, 1, 132, 11, 1, 133, 5, 12, 4, 2, 134, 17, 14, 1, 135, 136, 137, 21, 29, 14, 8, 9, 138, 1, 8, 139, 140, 21, 141, 8, 142, 143, 5, 144, 145, 146, 147, 148, 149, 30, 31, 150, 151, 9, 9, 152, 33, 24, 5, 153, 154, 3, 155, 5, 156, 157, 158, 159, 17, 160, 2, 161, 162, 163, 35, 14, 164, 165, 166, 36, 167, 2, 6, 4, 168, 169, 17, 39, 170, 171, 0, 172, 173, 174, 40, 31, 43, 175, 176, 5, 177, 56, 44, 12, 178, 179, 13, 45, 180, 181, 182, 183, 184, 185, 15, 0, 186, 187, 46, 6, 10, 20, 188, 189, 190, 10, 191, 192, 1, 1, 193, 194, 195, 18, 0, 8, 30, 196, 28, 18, 197, 2, 11, 17, 5, 3, 9, 22, 1, 198, 12, 199, 200, 0, 7, 9, 201, 202, 203, 10, 204, 205, 13, 206, 25, 207, 208, 2, 6, 209, 210, 211, 30, 8, 10, 13, 2, 1, 212, 8, 20, 12, 213, 214, 3, 215, 216, 52, 217, 11, 218, 219, 220, 2, 221, 222, 223, 12, 224, 47, 3, 13, 225, 5, 14, 226, 227, 9, 228, 229, 46, 230, 58, 231, 232, 233, 1, 13, 234, 235, 236, 237, 238, 3 };

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
            final int compressedBytes = 1578;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXc2O2zYQHnK5LnfTg5IagdMTszGKHHoq8gDcogh8zDG9Gd" +
                "jc+hJ0gBabB+iht91j32L3TZJbH6PUj7X6oUTK+qOkGcCO1/KP" +
                "NPPNN8MZjnNDDnD7ozrX96tf1auP8jsQ1/LTz89v4V8gr//7jd" +
                "/xFXu54X//9Wn7Tf4id1S+hTfk2e+PL//ZA8ochJaeudQ3nnuG" +
                "ZR4rfSP6JlF1M5WgZH8TXDyw/xsNRq7PVPMXo2oTwAoEkT8Jfe" +
                "gWYK/YxR19AesNVyvJBFzBTshb/c+zzSO8Qv5qLTdh/NhAeL+i" +
                "YfxYhfFju03ixwPjd0CZ1v9Bx4/7MH5AHD8+Pq4xfmgXOhBQQc" +
                "imXLsVkTJ2K/ZcwQUQIrUXkpB7OVFbcZBBeFxoxeoXrRXyrzf8" +
                "c6/55zLln602kaYZEErjn1Ie8c9DzD+w84l/JDmDhxcp/nZSU7" +
                "/k8P0PEf4OMooCIf7Oj/gjIf4u4Brx5wn/igr+/aJp4mvIv1dm" +
                "/sX83R6/vubi1zfUX4/881kzZ4F/SBX/EAP/8CRvTUTwdOVCwp" +
                "iZvISF4TX6Kw6mKOX1nVF4gEpCQUFBQZlV/ldf/4ZVmP/9WV3/" +
                "zhTKgkI+knmWHkPsdZx/7KGL/OMPcg+2/J/W5P8357n890OD92" +
                "P9BqVPeWtfPHT4bSRyU57x2uiRSDw6EEUnp17Uv7Pr17Ol+i9N" +
                "7hhLOJYUaLlifeOB/dL+RZDtX2xDtD3VD6P+xfukfwEz7F9kjU" +
                "Pp8alrMLUmUw8USJEodZAirvgpv4jD4P1vWTxBPkT8M/dPj/yz" +
                "j/gH+6cow/H/COF58vtfDPHTfnoR/0mC/ecCGKjjCzvJP8buH2" +
                "f5/9KUf+6O+Wf6/bKP7y9dP5SuX/rYP/eCv1AWK2H/VDXfv0H6" +
                "27/xtH5zyb/N/fd3xvrFO1P9srp//+W4/+xqmvUPbg00sn387x" +
                "B/n8v4IyPgb1j8dmxvU/8gv38BcP9CP0Kq94+UvC62Rnf6Z77w" +
                "zTL2z4gZwpe5mNcDHeMuov4SBbf+s+yy/zwR8M9Wbur7/w7zF+" +
                "J0wvAg/7TsX3CcPxnv/P1Jfyr63wZiyfW/eVmDyePLnIZ4zwUS" +
                "2/mLivPvUIKT/We5WZRl/8JA82Nt6uevy/Vzlqufw7F+fvdUPy" +
                "fW+vmQ68/jlzAGqyr8stnhN6lfWPsfIBL73RvsV98/ncr+Daxf" +
                "L0T6N6kwktaEE3Ru85wgvOcpHQugIkOrMcmfhUUUvo46x7Eonj" +
                "GGUT2bVI2Upx/Hq0JCfWrkV9J6AgpVJzWEgWGoWp1XGJCSjygk" +
                "tbw5BvplgRmsv6X9KMZDawbZ2hM6XoPF/Kcmu2inNXDs9lKEsz" +
                "qHNGfnWIi5yq/zJ7A2phm8TEwkk2a0ylv6u5Y5iOf4lj7rn/fw" +
                "GaqZ5ZQtLHVtX9Y0BwpcPnH6+VfQEQh4a79wdZAJ8hfN2T/IHS" +
                "A5vXCjC7R9/6yqGOQkA4gqgqnLn8gYSOnn/aw3O9GmGesMRNi0" +
                "R6dwFaypN0s7dGj7cOdfajc9/nWM3yITv2mdEVT5D15UCJ0F1h" +
                "vmP7x1PuzFohDFCTuNUaRQe4uS3jiQ+3ZdTeavh80fUDzE73hi" +
                "nZ9M93+savZ/4P6LReCPogl807/L/GDF/GHf84OloBxUEgTuX0" +
                "b/xesfu94BVfObJa+NXW9m84Pzx8/J5z/fhbr7/ufVjH+/bmKC" +
                "kX9h/KdQ97h+9F4Gnp/rhgV5ZQY27PzjmFmvy/XDTK//KKfOz4" +
                "6HfxT7oop1njQtORR7GL+M87tgnt8VTvO7w/5+ZJ/oH/j3r060" +
                "n/v87qLsNx2SLcxP7tP5yWgW/fT5yQaslNt/pTIHSvuvmBOP43" +
                "pr3KUDmgZl6quJJYGW5Rc7Ipsuygovj/m/m/n7fvNTOWvLZfVP" +
                "LfoPVFCn/978Z1qipq9/h6gceOVhy/5Zyix/ggk/BGL+BBgSP5" +
                "MpNuT1Z4hf8X9Iti7HLx9SYr8YbQyg1M3fsML8jRzukgJ3/JWq" +
                "xZldYKZCMhMTg9CkBRedKK55qyl1Fb3hqe36qdP115irGO6WW6" +
                "oqkwy2aEQu6cX+FRlkUds8vcP4OWvj20Wg6lBmsKRt9/sD/wO5" +
                "m7CF");
            
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
            final int compressedBytes = 1141;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUmW2yAQLYjSjwwLXp4Xzo4MB+H1qo/RRyFZedlH6KvkZr" +
                "HabVuShQViKlD9hTuOLUMN/F+FbAnAA9z2QgcEAmF7OK582T8K" +
                "UO//o4CrAV0Y3T9+6ICB2IG5HGgEaHJfmoiMoS8PxNYpwEcela" +
                "MXGAydL85Pju83rh8vFp43CRb340yOJceWDVIsxXxYPwFhyxw9" +
                "fuuVpjPC5BxMeIZuSb8M6ReBQHCAtPPQTPN8JhBVriKb4b+da/" +
                "3+GK3+X2F/RP3awUcxJ1T8NDU1Gcuc5z/1n6H+pzzIqQRCUwV9" +
                "lP5bnxVZ4rOA0FjpRyjvPZkpJHLAP8Zh/y89g+anNiv/gtX+Zu" +
                "SubPwRnH+50V/paP8GzkPIO/nErg5UQcdT/0UgUAnlrV7UAyHB" +
                "WAmF35F2/edO+q/HzP12lIXGuf0rYBggKJOCXWh4jdN27/RScd" +
                "5dt6mFSXTWqcpFE/H1n7GgCne4yhpeEk5CZvBmLRPWnQD6oiKS" +
                "ako0G5lz/fxUbv+M8p9QLv/lnf5RQLrv75TQwNIayinfqIbDqX" +
                "9V/H6H1g/lP2G7+klYw98mGn9T/kC0Rk1d/xny/fPaIUd/HGDy" +
                "xU9vxv71+HWckDim4h84dNzsJTyAYvp3n7GHY5KbTrxyDru9MA" +
                "+6U/ATnkAfjn++7P/B9+cpf6l49acibXDl3y3zD+lXs/7rxsSe" +
                "iBCEVUE+Lw6ZQGCI9oK1eBLZvPFLjhT1gx6mncMiZ24BILTa/K" +
                "ykKtJfv/UU+7wM+X8lPRIDnPRUJRIhn+sv3UyBBx6PwP57lalL" +
                "KdNZl7uZ+1D6nq2HqvFt1ekqhxAtjM8Ljl9LCaYuR64+//uYre" +
                "nQRddQYv2e+B/m/M/e/Q9D/2scmeeTPyvnb4+/3/jSlPBf8/Uf" +
                "giwcrh8Yxv/k/bc6bpR/kHP9lOUvQoz8ucdfd/jb6XjCdjMvhv" +
                "678R9slv/w8q9j/FfUT9JrFrjzB3X/9OJ+/Uxj6Z9mrt88uHjO" +
                "3HZLp7KbP7t/lfL6n+77TxGOF57Vu0qff4H7b6X2/0Qk+8V1fD" +
                "EdP8eZiFz+C99/Lp0/kfe/TUb+wqw/3BYUF/7KqR8t6H9R/4XG" +
                "v+r6R/Y+lgODT2/6NlPCspX1LQEtlEsyl+Vr7dW/xf/9XPQ+i5" +
                "ZOAEwc/pzJH+WcPwLV/qEaP9Fr7T+ZPLH/r239fE1vv/Swoqh+" +
                "RwsjVYWVQE7z02ExJF0vevlVjdOTzV4/LSOW9mNCvVXU28xYtC" +
                "6T5OQcn+BMdqqq2UatgBIsR7VufGv/JU+k9On1Uj+yc/3Yk/pL" +
                "Xz+v7IO62HspUUH3/w2LH2GJQ7rqo19H8SYrn38tYO7nf9lUr4" +
                "y+bQ3gx/itM9Kl+8OeETBa6P0/E+k+pvI/LRDYj//+4bFhbpZs" +
                "RDT36ygN20HumyqFnj9Kdv2cSvxH2HZHU/r+4zXc/1uj9z92+2" +
                "usP2VF6w+Xtaax/E/vEey/v8PuP6q/U/RhapBrdP+mitrXS3h4" +
                "Q3rsYXpe+8PuP/Mf2+K0iA==");
            
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
            final int compressedBytes = 965;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtu2zAQHbFEoQBdcJFFC3TBBD0IkVWP4aOwOUGO4KM28l" +
                "+OKFPibyi+tzAUWNSQw/m8IWlFEikiSX9J0xGaxOlSDB/WDJ/f" +
                "JHXUP5OlM2xPhqKgH+RPQvo+Qi65eYAKbB8NksxB//1Y/2bo4U" +
                "H/+0HL3aD/7qz/blB/PP0nGldLyDNakUqk9PXTs41atOfU3vEA" +
                "MXFnf/mIKd91/8Ql7A/to9t/4fjXevytePzyxH/7EP7btv+J0Q" +
                "PU6IvucGEm0oBF/NqK/+rxHxnKkoD+dwm7ZQrLT/94IHX9qBz1" +
                "3iyNjtie1rQPLWsD8wcz/hepa31qxXvbj1nTDfVllW1T6zf60v" +
                "Ljjj8+u/mjHfHHt0q0Y5L7T+D6RfXtQ/0/ifxg/8mhvyj1W9n5" +
                "j5D/Sq//tWt/jONHEaD+KAE9Q8KcDHVDUyV5yjewzABrNmzli9" +
                "uZD9u/d/KXU/un/YW/jNp/DO0VC89z8y9Byc+PxOF/gfVP3ZGz" +
                "6PwBAADUGz9jnN+TroUfFvlvlfxM/MVO6ezlYYNDp3bT5E7MyR" +
                "PxTcj4cw6RwoTNlMiM8oHyMP7uXhjWd+ljrhG/Ypd3/y08pMX6" +
                "z4M/pOcfimXqUTdd25Z72Sr0XwHm9m8l1NOO/s2GlRwz/i+eFB" +
                "tpBEXlb6c4KG0/AHOAQ8D+kT8BxB8AqCX/2PT5Z/35exD1BhOq" +
                "aWH8EvYDcweAGhxMxyPZIOllk47wuAcAACBGwOHw/sNy5y8fnZ" +
                "+8aX8cv8j6+w+5suCOJbukfADgBYbU+Gv82vE4f+4vP8L7hwPg" +
                "FdbsduUXl4buO6EIyB4/j/FLu+OXvsSvHvpbHah1Ppc2mKDq/C" +
                "8Xf2mKLCbwn/H8iQfzpzLOX73J05ZSQbNL/6UjCc5foKRgaN2h" +
                "IbR0CG5d/iZjJeYf8Mqf/fX9jddC/cA6e25MR85sepnJr6Te/P" +
                "yB/wAAsBCdR7DgEw8u6yf23Me599eP17/eOI0idPyUbvz8Get8" +
                "/r+3XLM4/6WbP1QsbJHx/5eU5l84Els8f7n2b95d8fsH3v8bDc" +
                "JxjVWJhRWlhhqA/Hm64Pk3oGm8XBiaJWF7Rd9Jd+bPyS71pwnu" +
                "Py9//yQrhjXH13MyflL/6NcO+kP9A/CqVuRDG5CBNiQr1g7AKx" +
                "rd1Y9iXf2Ycv8vdO0Eh4SrXr/A+9+AOorIKzT6D86xOv4t3387" +
                "qu9u49VOSNOd+7uC/U9oysAUrEtPapEeK0nA4Ak2hH/ZSf99z/" +
                "b7ZUXF/3+lTTYzCd6fobidXzFb5v//Ab8gwKw=");
            
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
            final int compressedBytes = 891;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnVly2zAMhiGVnWFn8sAHH4C9iaYnyVEwPUGOkKO28pZIlm" +
                "zuBKn/e/AkcWBSxELApChFZIgUabJ0wdJ4/XGcX3iaX38oGkif" +
                "iOkGa5rfUY/yJ1f5Pyt5Dmh/5kRK0wZnefX/A+3iz3yTf8JAGy" +
                "L7fL/+8yV8tX/p5XDt6De++v+K1/8X276bvN6QV5H2E3v9Jdon" +
                "h/GPkIzyn31MaPux/m8S9N/T/6rq78Ndf+yhPxCLXc8Fxexp1b" +
                "i/WxYjg/+v5Tln/JUR/wEAAAAgBoMhAAAQHTnZR50DAMjE/F0t" +
                "33+zGJBwGEPQzfgbj/ojYP3Bwq7E14+ty5cZv3Fh/2bxxrBIYP" +
                "XSpFXv+o/N349hP/D/5vVvPXvAt7wz8Aqc5cWTcP3TX9tIK5pg" +
                "6vjaFE1b9j/N1ny2/8/56ofZ/oeb/Z8L1lf7F67yvz7v/rOQ/5" +
                "jljaTJapUfwX6O5t7Ta52N4i9GN9BHIDr/SbR/9ll+yvJGYSc9" +
                "M61oMYIT/dRbionY/w8Szv+t5Q+2uW6rHG5Vuv7MmA1r7/5zuf" +
                "7rmvpD/Rlp/6au/de+/wrA/wBw4rClvXe9OFSWB6AmsN+dwbBt" +
                "1M4AgKAEvWySFLt+mq1+NQ/166X9cW/9NQGmgHpB70WO674iOU" +
                "nNAK0BxK88CTD2M8D+WofFtl9m/0cN+3Ub8/31HxZ0/lE+/fmd" +
                "P+d8/pr3+Y+1/AjnX/WA2tiiwPfosbV7QdmW4idDxcXt58XsYx" +
                "O3j/wPHNn/EszftsH5W0L+YbPFtf7r33D9vUWe//2GuA7WnmtT" +
                "hoJyjDs/b9LPqYQ4XxF0nL9NZVoOmj83718e3OSHb/mXR/t+90" +
                "+7tH+g+DEVq5ZBxfpnfLD/v8h/Q+Kf3Y8f9l4/an/fW8PXNxcf" +
                "9fuZwOUjZ7F3AeMX2n9K1P8y7U+Skv0SYP9z1/CDy0jMf483/1" +
                "jC+mne3LeV/MNV/xKef1lTf7W//2/h/ufW4gQLG//4/Cl3/3Of" +
                "33xE0n05lDN+yYg/fedvFeKn5x0R4f7f+vOLQU/1T2z8y1M/tV" +
                "5nCe9/9+dP5bB/7qr+L6PoUbBtjZGCo2MqYYWpDOtXAIDnsM/8" +
                "meL+Eax/yaCL56dgngK56l+wrCTF6M/7+S0kakUeeKciKLTzT5" +
                "Eq27CE3r9q+zeads0K+QPyh/aZ0P99/gE2wtRG");
            
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
            final int compressedBytes = 745;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnUuS2yAQhhvCgkUWqtQcgKNQORlHyVFTjh/jqcgjMIJ+8H" +
                "8LlzcIAU13/4CkQLQRBYqU6Eoif/vrLz8lX35/BHIUP6jQnRIp" +
                "E9CAmXHyiw9kfftd24VD+70k0fUDwEGAk5I0Fs+OZaLD2Ji81c" +
                "bSyW/lj+WWPzaUd/fy7qk8d/uBuSSrNncp6CtLoYKt5lb/mR76" +
                "O/L573v9O+U/asv/huFJsHyMH/TrUDLs16z9hM71453ym7zxK8" +
                "e9EOusv2D+1xJ3/9qPv7f5Uw7s/+eBfq4rz+2/5dTfs3jgX/wH" +
                "E0mEXQFJLOG/v232adcoXVfLixtixlwEAMxj/PrRVMow/X+O/n" +
                "6rPVFC/61Ohf1svfaTdbefuNvP0H9u1vyR0f+y13/7x39c/MD8" +
                "4Y0/5/S/9P2Pcfo7qDh/rzt/gf4HAAAADgJ8ml6366lf6PmfZc" +
                "6PBcp7+Wu+jMq//v9z6X/38vz+t/mvt6H/5PPV/qKqO++zP+3t" +
                "B2uF5zXvv7TEf/h/YZjpPxztm5J/nJ//FuH1y8adKDscc3lwNB" +
                "H8gMkKgE39Cv3JlmQVq/YL1FN9fknD+RfJ/Tcnf++Fd/1i5P1D" +
                "Px0jeJ592l952N8b+2e241cmUDmjE7pBXeY5Ov8oDfFrey2D/J" +
                "CJ2fv+OugP+8D/S8//9Y6/hvy/vx65+mdA/GvKn5/Ll0Htz4Lt" +
                "f0b8kWy/K9gf4m+t/0mE72/IxY9wWf6lutlNhQQK7I2u212PRl" +
                "yN9NfOK7Xc/1LvsP00pf042rOyfrD9/Cn0dyWRuTyprb92iIwu" +
                "NAXT9Wfl6xegMJcHAEzzn1zfQeL+/pLu7z8lwv65Uv0oW/9k5e" +
                "1vXH8//fn/PPD+NbR/fPzSKwtxftds/Dvt/fVZof0mUvD9Ocxf" +
                "9vUbnJ/QPf9P8yKI37BfAADA+o+p9Q/QrhiBpvwRzy8yOdbp5Q" +
                "Ewq3+2pYcpEfZPV8wcVK/fat+/g/6QH3+Gxq+/s/roFg==");
            
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
            final int rows = 192;
            final int cols = 16;
            final int compressedBytes = 283;
            final int uncompressedBytes = 12289;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmUEOgjAQRT+1MXVHDAtNXFTjQRpP4lHqTTyqiJhAJEENlH" +
                "b6HwtKQ6Ez02bmA7Asx/bs60N5U2INW7jzs8sAFhr3unnYwSto" +
                "ixPc6/5NecP++vG4stM2oy93YWx0EY/Xjc907Szb9liotqmawD" +
                "SjVxoFTFUH6Y03ody3uP8ypOhf+qSNYfwFx1/1TqOmWMZWFAP5" +
                "q/o2f12mnMVPhQchBNx/ZIb4U39MW78tXz/72fRrCvo3cP0kzv" +
                "4E4xRMfwmNP/1HUua/9cvCN21K1t+sn0TkH+bfiPRXzPo19f+P" +
                "OehPl/z+k6s/CJH+/cVFPH/Wf8w/S67/HOwfV83uJZ1VN9tuB/" +
                "R0IUFsE+ZPMfkzB/8TEi0PJC5DKg==");
            
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
            final int compressedBytes = 4253;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNWwfY1EQa/maSTDa7v/gLP6AgihT94Rc4QTiligqKBR9A4R" +
                "ABQUQFFBAQKSpgoSk2ED2lCSKCCipFqsqJimA5bHeCInZUPPUA" +
                "yyF730xmUnaT3Sy7lDxPdpJJMknmzfu+35TVyoAAhQTE6T6tEp" +
                "SHClCNVYXToQbUgnpQX2sIDbTtpKn5MTSlfaEFtNQ+oyXxLtCW" +
                "dqVDYRO0NzToRPezFXAl9IG+Win0h+vpK+woo5/2knUW3ATD4R" +
                "a4jS0FHWJQzviVTtHXkv1QGY6FE7QKdKbei9SMdYaT9TGkTNsM" +
                "ZXQ3nAaNYh/GnyH76ANwBpxJ50ErOBfOg/O1jfQP6KgdS5fD5d" +
                "BNr01WQQ/S0dqo7aST6V7tGxhI98BQuFm7B0aTvsSk7bQP4Cgo" +
                "oetYNagEFaEKHEeHQVVtMlSHk0gtOAXqQF2tPNX1zvAXbT38FZ" +
                "rQM6E5nc3OhbPhHGhDr6Ld4k/DhXARSdJz9B7QAbpYp8MV0FMr" +
                "gaugtzYF3oRr4Fq4AQbDOzCEXgGj4FazO2hgAKNfgQkWbUuT5A" +
                "8ogmKtlrkVjoHjaYvEeRqFmmQl1Kbr9dLYUCiFU6EhNNavpK3I" +
                "c/Rs+if9hQ6HZqQ1nAWt9begHVsJF8DFcAlcCp3hMrMH/A26Qn" +
                "ftQ+hFLoer4TroBwNYGxgEN8IwrQ6MhBFsdTJpDKH9jLPhDuMm" +
                "us/8iY7QB8AYayU7nTVOisU4x8Y3maTXsldFToyWJObhflc6kq" +
                "xIJq1uuL0/tjDpW6ynre6I79lqny2V+bPow8YZrInKN9uwbcZw" +
                "cUY/LLs85qzle7GPEhuTSW2RWyKMxX3EV1zVRT7dN8kk6Yj5O/" +
                "GKkzTco3vwTMQX8xHfZFI3kwGLNpn/klpqX+8vctfzX3oNbnVn" +
                "F9hH6FXJwAV6GveKa6aQ5d58tob/xi+VV38jU+d4/DaxPyDRTr" +
                "tP1sBENiO2wbn+Y9pB68ceoX/iWaPxGVs7NfWj9z6I7zi1Tfo7" +
                "V7eR9dIabsc9xBfuogvpAhhPF9F9wW+iPYclNI2PZa/JkuvKZ+" +
                "4qn3irwG23PPqn+J0UXxIf5nvvpWqL4ldC9jvv+zXnr7imiJRx" +
                "fOnuZOgSb2S+590nq+RdS53SBb7iWF+B3NHh+Dp1tSu2iX3qxR" +
                "f520E+9wa2Vm41w9J6eMooEd9hW3/J9Arffeqn4mtutfEtOlpb" +
                "jLVWBbfXe69A/k6XZ66jz2aoiZ6eWhiVii/y904s/XcsZT97Xe" +
                "kzTKDbUZ8n0s9gCtyP+vwg1KefGqPgHtI08QK9FvCLg5by+5Xv" +
                "Be1xnWy+j79XipKFPuP9X4iPoJ9gLuqzwH8P6PJdt3J8uT7j3X" +
                "/GZ9rG8YWTY9tJmVmGZ47i+iyetrm4w5m4oj6L0lfZ/E3Da6d8" +
                "moE2vrHzHHyxhqFEHq0o7r/DxhdOwvVuW59jmxV/UZ85f1ewzl" +
                "yfHbYKfRZbk6CLF19tCubdy/XZw+1bEzNFyrQzXHyhyMYXjhH4" +
                "FmuLgb93beeqU3FtzLbTfzvfyheA3xSc5Zwh9BlToc+Yoj7jby" +
                "8vvo4+PwAjBJO3uv6rV7H9Vz+OLVD+a11ibMe6eoYt0qtiWS0C" +
                "8e1k9Y7v4P4rcgS+iTcTi8We13+nQjl8w2J8joU2vvL7E/hiiv" +
                "wFRFjhK+/g4Avnx1tx/xW56L8incb12bAUvrg+BDfHnwKubajN" +
                "Rpz7rzgm/Nflr+u/zlci8LUeg+baD6xLKr6i/j7j/uvkCv/FVP" +
                "ovbg3BFf0XfzWBbzPuvwpfKBZXof9aM/Buafja/hur6nD5eOW/" +
                "eLSdwtf2X3GFwlf4L245/ov6PF3i+wXi+7A+FB7Vb4p3RXwfwf" +
                "hqEseX3Q719Bv1IcZ2Nhnjq/GixBb4jsj8OJYbv9x5sk6I51BM" +
                "+7ioJDYnlrCJ8rjAV2yV04eZJRxfn8rWlGkZu4vjG7Ygvoi7H1" +
                "/53YqnYxPkeajPPL4SeWPhKJlbCde/p5R4ik8FpFJCc72YdQ55" +
                "hg6ebcTXs+fgK/Y0UWIrG195RrFMj4/34Pimld0Q9X5d7EQH3+" +
                "EyX+Ir9y71+d44rs8uvs5Zj8l0Fsy0+Yvbc2A28neG1dxqIfT5" +
                "ceDqNhdLeEmcOy9MnxHPt219Vvwtqpx43jjd1WfOX+kreLbV0u" +
                "Vv7GUvf0UJIfzFb6pVeo2r+ErxV/mvzd9gfRZ7nL93p/M3gXWk" +
                "cx0I4G/sJVefPU+Yps8yZVobT26RTFGfE1h7RjJIn5G/1b36zP" +
                "kbpM9+/nruIviL6RM2f9n3QNheWAiL2S+Cv09CNaOJtQb1eQHq" +
                "89P0U2jAdpGmbA88D89y/uJ9S+LdYUliFtvt4e9Xfv4WDSpKcP" +
                "/18hfmC31eyn5z42c/f3n7SH6Xf6bV4lPI3zeC+WtYvjM9/OX6" +
                "7OHvIhffjPwdFcze2CuKv9AzEn8vDucv+zGIv4hvDRffTPyFZ9" +
                "z4yuHv785Zz0k2va38F5Ym5tr+a9xqfav8F89cxvHFmrpF+a/A" +
                "F79Ajq/tv4hnFb//Ys6ANP+NcXzxut/C/NcsU/7r4uvz312p+E" +
                "IPf3yViq9e3++/8IJTLo+vAvyX42tUDOZvvFyq/4o03H87ufh6" +
                "/dfGN9h/PbH08XZ8ldV/Bb6wHAaY7zj8XWHzF7ec+ErwF/FFNt" +
                "ZS+Bp3CQSaGjZ2Mr7i+Ba1cOMr4zIeP/vxlaVLfHHLwdevz/jl" +
                "9LXxNVBnMsdXiTpp8VUPHwO4rg714mv3b3jwPS4Kvqx1ML7cf6" +
                "PiCy8ivp2D4yuOr/lJEL4+PSmOiO821b/h0efpHn1+EPV5JfuF" +
                "TUV8V0G1RG303xqwmuuzdrStz/EKtj4bEzh/8folNn+VPou0T0" +
                "hMIvBV+sz5a7UM1mcb33B9FmmAPqecmcJfvz5Hia/Y/aFl56bP" +
                "3YL1WbxhoD778c1Dn9dIjS+Cl2ED02Ad/MPUWAxehddpdVoN1j" +
                "MD0O1YKZ5Tk9ahzndPLVpCGaYVaVWKT0r5GZWYwIWeQkV/EcVo" +
                "jWK90nLOVbVBxGisDm5X8b8HS6TXI62RjLiQjkH4euOr0Ctrpd" +
                "Sn09MQjm9ui3ZDhh6K0SlPvTb1DFaXx1cRaqC/p5RXZCp7ozh/" +
                "bX2OVzSZrc90kNv/zEo5f2kn85/Q1NZnOlDwN8V/WX27/zmbPn" +
                "N82W+4J/qfw/VZ8Re2KH3m/c/p/A3XZ+lh9dP1GXhLvnqQPvP+" +
                "Z8XfzPrs5W+IPhuozzcF+6/NX68+q/7nVHzD9dnuf+b6TAcrff" +
                "b470ilz3QincA0eme8Mh2L/P0YPqLj6B3wL/gQWTSG8xfxneTj" +
                "lqgvOp55ehJZ72xfGXyg+MvxzcbfdH2Ovij+qv6rsIX+EMZfs0" +
                "aOd3zft7fNKfHm8GtS9RneC+Jvlvt29/MX8ZW9X7BVpp848fOn" +
                "9HnJX96WcfgramKZPDuk/wqfZUi0+IrjK/I98VWs1OavyM8YX0" +
                "Xhr/LfEP5ujxJfmQ3yj69E/Dw1nL/JZLb4KjN/3fhKGy34+5kv" +
                "vtph8xc+V/iKPRk/p+G7Oiu+w7Ljy/uvgvB120eHAN8vIuHbpD" +
                "D46s0OBb5ufOXB90vVPorJqC12GpvqKNf7KUr2AX3Ls/cufRN/" +
                "N+O6KaqC0S3hx2KN8olhUuMr+CpzfMXeOnTxld4qn6vpcO/4YL" +
                "T4yqmFr2X6Pa4/wLf4+425A76DXfAzlvwR/Aj/yYYvezs3fGFn" +
                "yPN52kcHtqT3XzlH4mlPMiw02i04vsbifPGN9IUH9LbBT/ILa6" +
                "j0OTaL7rT1Gf4bm+32X4k7fR+kz+xzV5+j9G9wfXbyvf479xD6" +
                "7y+R9PnLwuiz8Vw++kxHR9NnvVGAPu+W8XNl+A3+gF+tPvA/+J" +
                "19Kd/UwwfYF8ZfOrIw+mwMTxZ0iRo/h/M3e/wMeyLxd2le/B0d" +
                "0QUC/A32yhTbISq+MjVrPlQw7UjrfkB3Ev3P+/HNRUsV+NqSbQ" +
                "oaP1Lj+0H8lVu67/4e/vL2keJv5vGjrLU+MAhf//iRj793B/HX" +
                "1ucQ/uY0fmSsDB4/CuYvHz/yzStJhI/ve/lLB6ePHxGw+Wstx3" +
                "LOw29lkbXMZELNr095+ga8/zl5EBdrRSTsxubGX3f+Ru78ze6/" +
                "0DMSf9flxd9nMz7BuIxHb5fePNBsRxeQwXRRRgc/qPjmH18VGt" +
                "9c+zdC8V1/8PDNHF+RQZ76kPGV2K4AE5FRe7k+kyF2+5fcSERf" +
                "dvj8K6yRntH0ObB/Y+6Rps8c34Lo8+v56LNq/2bTZ9+zqPhKzr" +
                "+iRfE52mwyUpvhnDGGjy849yiFBlHGF8wrU8cXtJn+8QVZXsD4" +
                "QlD7N318IVd9zie+0n4okD5vzIe/rG7++hx/EcTEQHKLtYz3X5" +
                "HRqv8K13r2+BG+7xzVPgosC9tHZq/s44Pe/qsw/808/yq9fRSG" +
                "r7d9JHNzmH8Vrs+5zb8y3g4fHwwou2EwvpnmX8k8p33k5Mj5V2" +
                "Scmn8V36bmX2lPagvV/Cvk71zVPlLzr3j7CNra/VeOPvfOrM90" +
                "SyZ91uYeWfrM+VsQfX73sOmznH9Fbo81tPFNXETu4P4rz+P+e6" +
                "fT/yxQcfBFfUZ8K/r89yo5/3mNF19aLsh/uT778G2U7r+uPh8O" +
                "fOnsAuH73eHCl9yl/FfxN9GVTOD8JePF+L5s/7JSMlH5L5nE8b" +
                "X9l+PL/Rfa01KYjP7bh+PL/Vf8/4j771ovvrR2OL5cn48sfAsW" +
                "X+063PxFfNF/YSG+3RvCf++FagJf4b/kPje+UvMnJb5L3PgKOi" +
                "G+V3P/deMrbYP2mva6HV9x/+XxFZ+fExRf+Z6vLDS+egrOt0ie" +
                "/htxfk5sfzb/jTY/x/j54PmvOz8n0H/l/EmOr81f7Q0yTY7/Cn" +
                "zV+CB52I2fxfyNdP5yfK/h/c8ufwW+b9n8tfF1x39T+StKTul/" +
                "DuIv4ls+3/5nMjVK/7N1dGH6n9mxh2J80MaXPOTtfybTU+dPim" +
                "05/uvD9xHuvy6+2heu/3J8efyM+F7nx9frvw6+U/PFN2B8YVpu" +
                "+Kboc9j4Qo0C4XvCocM3ZXzBmR8b2HPiaf+SR1V85RyV7V+Or9" +
                "Pj00/U0Ldu+9fGN61Nl0Wfw9u/R9b4QrQlZh689m+WPi3ZEtT2" +
                "Kf+1+Utm8v8f6QxqJB5X/ssm8/aR8l92p65h+2iJO74v2r/909" +
                "u/3v8fifHf+cHtX88VGf5/lMl/g/5/JI+MPRD/tR4sjP/GiqP5" +
                "r7b3QP2XjQvxXzk/Vo/rFpmrl9OLyOMi/1Uyj02id5A5RY+q+b" +
                "GIr2d+Hf9/mWDYeN9Yx4AAtCb6auYlb/s3DN8sIxHlQ65Lw9c5" +
                "MvZA+GtNKxB/2+egOoHzY7NyPKUXK3V+LC0iC2AD8vkJMt/RRl" +
                "//ZLT5z+b1qf2TerWo85+j67NFctXnXBYX3/D4OceRsfsONr5p" +
                "pUh8yZMOvs9yfPn8dpu/an47bucwv928IQ3fEwuPb+7+e2D4Zu" +
                "9/jrbE7z9c+Cr+6rIedTVGJ3Qb9Xk1n/+s+p/9858D9Xlg1jt/" +
                "kF2fCzk+mA++hZr/HJ+aQxkHPP/Zl5My/1mvp9pH5GXaXrZ/L0" +
                "ybH9tYjS/Y44O0bur8WHNQhPnPU4Pnxwa1j4L6nw+kfYRbh7F9" +
                "lGh12NpHO1LbR8QzFq239PpvlPmx5uAservFbR/l4WdbDg1/rX" +
                "eTR8CSV/tI6fT/AQ3DYMw=");
            
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
            final int compressedBytes = 2078;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXGlwFEUUft3Ts5PsBggEkLPEAGICAUGwFCF4FXIjAlpqUY" +
                "KCgghoEIHiDocaLBENIKIQDZSAiv7ylxalVf6QQzERkCopSzkU" +
                "iCCW/lAs19c9PbMzs7NHdjbD7pqu6uljemYy/c173+vXb0O+AA" +
                "IUQhAMY4I2UARdRa0YekAZ9A3cwFvsjrBIMBTKZW24LMdhnhgO" +
                "axUwFaaLntkwJ2wmeA4WwjJYgTWGuWWgRPZfAx3ClgTXy7I35v" +
                "5wk+XMrZiHwd14HCHa94njQ/CwKDfZ7jJPHBdhXmr2tYC2omwP" +
                "7aCTZex1mHtBCZQaPcrn2HMz5iFaMR7vhLvMsaNhjKxNgAfM3s" +
                "fgUVE+ATNhLjyDtfmYl8ByPCqYA5g1yJejC6BQlK2hi+zpjrmn" +
                "ebc+MAAGWd8mUAq3Yf/tgLMPIzGPgrEwHibB/TBZXPEgTMHjNM" +
                "wzYBYen8L8NFTAAixPw2I+hhZgvYHXyJdYO08OkEO0G+0KF+GS" +
                "eAbiS7vTEtrReCrNp20p/uW0He1M8S+lfER7bb4414tqosSvhd" +
                "2LZUvzKnwPOIf3Q3xpp3DCRIvDKSeObyqJ46un/BPhDEiB0tSv" +
                "JQdlWU8OkzryNTlCvmGzjLPsSfMZQn7pUXrYMvdH6AE8HsJ80O" +
                "jTnk2AV528X4mXNw529gffYEd/kWQz047vV+bMvwgN5ChdS75l" +
                "i1B+j8FlWknX6PJLV0p8q1wQe97a0hYkMe/nvOLrl/xqxbkiv7" +
                "SAVcqe4yZyqJ+t8puUfl4UpZ9XOfWzIb+p6+fgbJ/kd1bW4/ud" +
                "1AyvRuyrQLVuX5Efouyr1xLaV4tzyb4KbMx6++oPw74CQs6yLe" +
                "Q0+YlthTZ4pquwr4rZNihzyi8MdZNfmIjyuwTPTrfJ7+uG/HJ8" +
                "dfnV8Y0nvxzfWPLrhm8s+bXiK8v2+FWfcozs5Sa/MCRYH/PeEy" +
                "x1ia9smfiKliJ7TXyxXijLLjHuPcBdfg18ZWuSy5Umvqb8njH0" +
                "MzkPfxNkRrjirp/hn6T089Io/fxmuvQz3ek3/9KahM/4M8P18y" +
                "+yvAhElBf09S+XX8TmLeArQCG/5NeIfcXl1+VduX5exuU3xlwI" +
                "/RyR37jz1jvOOf/k91LWy2+DaZvX6PzLdrEdrBaK2AdsD3sbNk" +
                "IPtpPzL7nE3mf7xDW/4T0E/7LtNv5dj/guh6myx8K/bDfnX1Hb" +
                "y/k3Yj/H4l9Rj8G/SUjuPDf51fkXy3a2Xs6/L7nxL9pXl2Pyb1" +
                "WEf82zGzj/WtrLZRmwjSqQZWuzx8G/mF351xwh+BfLKP61PEXy" +
                "LwHJv2W0L+1HhdTQGzEPdHxD+vpokEN7RmkObWWSOseH9RHt71" +
                "U/K1Oy3X4252IsHWXWURYp9zqNTgHfVX7gG7ziD77Bv3IG3wL2" +
                "ka29P8X1b2WUffVh+te/fslv9vs3TIa84HhHYY2wM2a7ld0/qb" +
                "R0908mnPM6/+bFO74hNe6c/ezPeyiFabjHtWrI1u7Gj2prx4zZ" +
                "/M9qq2TxVVtkJ76ZIb9Kk/wVZI7dvoJ+TnyxjfiqRbb9hdVNzb" +
                "+wKumRnte/oVYJn/HI1dbPUBn3rImH2sHwTyqDsb9Il1++PsJW" +
                "X+v+EanS10dcfmG4Lr/m+miN2/pIbWGsj2hdVq2P6rN+fbQu4p" +
                "907u/r/knD/6zcY/VPQrluXyG+wr6CcdI/uZb7n7l9xfHl9pXa" +
                "idtXhv+Z9gQG1RH/ZCL/c8S+uhr+59At2e5/Jlt0fNVitY8Lb1" +
                "oYTBmZzP5vJtlXadDPgzOBf7UXPHD3CIkkrn5JreiZ7MoBQj+r" +
                "YxL+LdUJZ32/X/6N7Nz/hU+j+ZfLbyPv8pm0ot411r/q+Ih+1v" +
                "lX6GcL/xr6WZnO+ddFP69H/byd869NP48z9LO+/o3wbzz9bPCv" +
                "m372j3/jxF9lCf8qM6R+XqmuUIVlqiyJI7+VCeV3nx/+q9A0f+" +
                "Q3+/1Xxv4+qVdX6/FXynIL/5rfmPZx6utfejLd69/QDO/40h+z" +
                "BV96ygP/LjPxreb4KrheUmWURl7USlNFK5V1DmdJypX4K9bFO7" +
                "7IvzXQkHcSe9bw+FhlrTU+llWw+cn5n+U5i/9Z3eEWH8sWpN//" +
                "nPd9bupnttCDfjY1q/qOfLt1uvzmuegvdVM4i1KuyK8W8s6/3L" +
                "+hvqfuwbfbbMRv6P4NPX6D7y9Y/Rv5Hdzjr8RZW/yVutct/kqc" +
                "TTn+KlTpLX5D2eIYGSt+I6b8+hm/YewveIm/wl5iGSfjc3j8pI" +
                "5v/mhlG2IlrePY8Tk6vk77SvZb4nMSyl6TxudwfK32VSx8Y8uv" +
                "n/ga9pWX+Bxa4MJ9lv1f0U6Nfz9RB1r5l20w61f19ynJ2M+N1c" +
                "/s5Uyzn9krpn11WN1qYdo3YuGbP7dx+LrHt3vDN7QnqXnJkfgN" +
                "L/az7Xu3+a+C3RDlo4b/Ss6YiJ/U/VdynCO+Do8u+0d6fLusMd" +
                "szM3r/KDP8V1rIu/+K1LMaff3LdqnHWC1ie5zHT4rvR8YdG/GT" +
                "li9rezKzrQ5ku80r9maK/dwU+jlz/RsWNP6Vuul3F2s7I/ZTGo" +
                "cvebx5fST496zN59k/bfbVmSbg32P/J/5NE4vL32Kgfm5gtYGZ" +
                "enx7OvQzj29Pr34OnWjG11sKzHWdMdv+QqBPrsdPZoZ+bhJ8K8" +
                "SxLB6+uR8fmzvyG7D4TPjvF7Ry++8XZPyzabdrw0S7NJPfKWfi" +
                "YwvTf09N/CeIQLljxqoyA7kCrVl+G/mtO/yT2mbDfg7c7s1+Dg" +
                "xLv/3czL+NXiv+B3X2onc=");
            
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
            final int compressedBytes = 896;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmU1oE0EUx8dx87KJsVKo3wpq8aO9FdGLUEEvamoqfpdW2u" +
                "I3QayCmIIfVRSpBxU8RBRUFG+ioqgHD4JHP7BWq55b8OBd0NM6" +
                "3cxudjbbZLPZZJPJe7A7O2+zIZnf/t+8eaNpooUfahpdQheLXt" +
                "pC55vXEdpEgbWz2bFI02ATa+fweytpWG+jvN9gPrXc8m0LtIJG" +
                "lzl5Y2s1F0bbNE82/Z1xFf2nSWI0xpj+yfZDt3P5wmZnvnThJF" +
                "+6qoJ81yFfD3z/5udLm4vnC3HoKAPfPci3NFNv6iOUE5+FnhCf" +
                "BaV3TqVf4XnPfF2+s8hXMELZwYmoL/PzJe0iX5LQuW7P6pckyT" +
                "ErX3I6o1+iWPmSuWSe8BtWWK7byOosX/UF86zn+u1z9Y6+sv2/" +
                "Jsd/vVQ/t5BWZ75kA9lofjZOOsxrhxhCjgi9c7wFwRvjbaPpaS" +
                "6S0xZ2dLJjN+936ed+4TMD5CQ5xcbgNRnMo4AcvrAjn37dzL/q" +
                "uA/x+YCrcUihftUJHlF3iv7IhWLz54rmV0mMz6VZ9IdOfZe3+Z" +
                "d5yjr/xlLIt/j8WeA74V6/sLfS+sX8yh+DLv/1q1z3Qb/D1Tn/" +
                "KteqjaByw9k/45fOt9s22ldrKiaVrF95jMagl1AlTaLQA/sy66" +
                "NMfFbuZOsb0GfEZ9Iu1jdIwh6fjfWRcsv/9ZHL3LHk9ZF+R4r1" +
                "EfQrEcaiQRHmYXqpsH7plSIz9vGKredTqF9jfWTPr3SfD/VJ9u" +
                "bsDyq/8moyxmewVAxogrfxnNFeY+u3yphVSsn3IPKVma/J+VC5" +
                "51/kGwjXw6hfqfkeRb5Sr3+3QtLKl26rfb5Y37Do9zjqtx7yKx" +
                "v1ARvPMfrJ0huh79n5Izs+uNbUKPINhOQJ1G+96NfgCynkK41+" +
                "BytVf0a+gfA9g3zrLj6fxfgsef58vrB+kW9NaDYGvSw+pxnTHh" +
                "jivjz7++73B439fX4H9wcD4uvg82f/N418ayZe38X4XLPx+QHc" +
                "L6RfeORBv/dQv1XB9zE8g6dliM9PkG+Vro+e4/pIorn1ba4v1G" +
                "jjKewvhGa53V8IzTS/AfcXgorPP2EERuELI/0dPsM33+LzV4zP" +
                "vupwrOgn3ujn3/p4XhZGl9cn6dDU9Q2sT8pj4Wm4Pqrl+sZk/Y" +
                "pRpNb82Vq/8hKfsX5VJYSHjfgcVrnnIsZnWYz8BwETaH8=");
            
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
            final int compressedBytes = 508;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt1KAlEQx2Ny91CIEGVgKkURZdBN0Ev0/XUTBHXVQ3TTvR" +
                "cVSCR5YUXQN0LQC3SpBVHZG/Qcp8U23T0qRbiyZ/Y/sI6zuy44" +
                "P/8zZ3alsKwzGqKkKx6hFMWqURf1kWn5KMUpYflxa+u3j42RqP" +
                "huKUWP5SPVT406rjcgfzUalm23zkfJzigser3ha3yCry4mokH4" +
                "ljz161l9jkG/vtWrQoDS0K+2/TfujX5DBeiXdX0e1I0vR/OQbw" +
                "p8WfOdBF/fUk8qccoV2XytLaGsy6ZqfCs+0vDq4NvOtfLMzzsj" +
                "0+wc4xB50nb9POtNfTbyqM++0O8c9BsQLd/bxOfrjjwgOwyUvN" +
                "CUPPjq23/XxIpYFkti1fwQi+Z7q/qv+Yb+q42y15EDTcltOzRj" +
                "12eqX1FPK/EEMqdLfW6wrzX3r3ZQn32r6l0l22V6dkQvVLRen6" +
                "yt9Off0Suy6mOVl8WeKy7avoTcMOG7D76c+SqxzVccIDda9t8M" +
                "+m+w9Ovk+61fkUV91la/xwrfNHKi6/xrbkoZOpLS3BA55/wbyt" +
                "XmX3PrH/+vy2L+9YFS846c4v4VP74n4Mua7yn4suZ7Br6s+Z6D" +
                "L2u+F+DLmu8l+PKefxvsa83z3yvMvz7Q7zX0y5rvDfgGkPqtwh" +
                "PPj3QleQf9suZbAF/O1vEF1FHmRw==");
            
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
            final int compressedBytes = 522;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmzlLA0EUx2V2MzMGSeV9gCji0VnYendqTIytIoogaKuIne" +
                "CBaUUUBD+DR6Fg4YEKHuCdLxE/grBOQlySjTFRNzgz/B+E2Zvw" +
                "fvzevFkStmvZQfyJsc9yBGlz7LdYCCWC7YGv1nz3wVdrvgfgi2" +
                "CHyIEGFI8ynSFRZEfNIEXshA2xIAuwEI2wQfoijtWSmpRr6kkz" +
                "qbD3CkkxoWIsIVWkWoxN4lOaONdIWHz0WhZ9FqPPvqsh6XmVOX" +
                "yvOrBxie8pfaBP9FEQidB78JU16OuP7ziO8z1Ly60rfD1+8JXB" +
                "3y+OucKXnYOvFPX5Av5qzfca/ZXGa6KbXK7y9CNTmH8d8+8t/J" +
                "WiPt9hfaTz+ihv/kbBV+v6/Aa+WvN9B1+d+XIDfLXm6wVfGfjy" +
                "svzwNRfBV9bg5Y5sh5ETZf1tx/tJjU3tyOUqvJ9Ef5XWX3XCXy" +
                "nqc49g0R3bphHehfosbaXt/ekdn+8neTAf/ho74Kt1fQ6BrxTe" +
                "D9td1HrG/moDeVJ2/h3jo3z8e3/5xC/8HYG/0jo9ifcbmH+z+D" +
                "sFf2Xiy6fd5GtcGlfgK219nkF91sTfAT5nb8d+sRxQ//+hpFU5" +
                "n2bz+Ox5yzJ9Zso8TJay+0tWYYey9XkB9Vmb9a/w0NyMU11J7q" +
                "/M7T/2z8vor5TxeQ05UHl9lPA37Ka/5hb8/f8o+ACZJTCz");
            
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
            final int compressedBytes = 583;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm0lLAzEUxyWkmYlLTyq4VMQiLjcPfgIvgli3kyLoB/BLCI" +
                "JQ1IuIG4iiB/Wg3r0L1n1pEQ9+AJeTiBsyhrEM7YyVqh1M4v9B" +
                "+iaZhfb9+n8vk3bMMXPcEkbDVoqRESvNSNTyGBm1YJqYOYEYaM" +
                "13CjFQ00ihoDfHNtPGqkkorR8mDaTM6XFSTJjwJaSCVApfL1pp" +
                "cl8dMWyfL646K3zQOas25XrlWbyvGrDJkTYdtoHJTMcEoF9l9W" +
                "vusC0/9Ms2oF8J+LabMWc7IlqnaG2eo5pd/UapP1OTcjl018/6" +
                "6xnLTf09hH6V+X5dIAbqzp8ti07bFC9T9Uvnf6dfOgP9Ssb3Cn" +
                "w1nD9fmzd+1F86DL5S8L3zaX51C76y5Gdf+N6Dryx8zQcvX9Rf" +
                "6PdL/T6Cr9Z8n8BXsvujZ+RnDefPL/7ol26CrwxGFz+8+Sq2l+" +
                "2RNbpk+5Xknje64TpnIctrrzpb64i0PMaJS00Nab2kfkWr9OjO" +
                "0a/tg59qE/r9a74UfLWuyqGf8eUB8P0v90ecYX6lNV8OvsrU4w" +
                "LEQP31DV6I9Q3k56zzcxH4ysKXB718od9/VH+LEQNV9csGPuov" +
                "6+clmfTLBqFf1F9X/Q2Brwx8eZU/fPH/Os31Ww2+0s6nWlzRji" +
                "ImyrJs9Y4FXE9okTg5SOkdk5h43RdtL+s8cYpIa5afI8jPUsyv" +
                "eo1uo8voNHpYwuhg5zl7/vcMfKXg28eO2Sk7EUQS7Ah8ZTUW//" +
                "YZ267fF4YyrW9gfVJVy3sHLu3HYg==");
            
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
            final int compressedBytes = 566;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm8srRUEcxxlz58yMRxLKqzwS4p8ge681iZKlQtmIEMVSop" +
                "RHKCTs7W0kC9wUO+w8VhZWjOu6nXtxXZzDzPT91W3Oo3M6/T73" +
                "+/395jQnMCx6nr6MwMgTwuIQvciBqUGXwuOa6KMrimU/3aDLoS" +
                "Or4TNbdDvmmoUE770e2dpEpv9dpYOf+vMMsmNmkLQPjhWToqj9" +
                "MlJF8iJ7gmQTpsYcUkAK1VipfrnhcxXECY1S/VuG1JgRuarcdb" +
                "/8BJ6rFGy84StGfeI7Ar5auPJ4xIWn4c82+rOY9EO/gU3o1+r6" +
                "OwW+WtTfdafZaXIanRYWdBrYiVd82TH4asFX+Sive9lmQV4Lvr" +
                "oGr//uFWzvzZ/pbKjT2nf7M53/HV86B74211/e/Vd8xQE4xucb" +
                "1u8h9GsnX/6g6B5F69fNl7WDr/HvOY7jnDtBflB/Y+a/p9CvFn" +
                "33gPLSDBrFmYzFZHvyAwIT2jhPEBQT6q/O0F9ZW33PzdUv4tf0" +
                "L5ADq/leIgem1l9x9XX/zNZ+8P55EfXX6vnRNfjqwlfc+sD3Bn" +
                "y14XvvA9878DUlJDHvmZO7wO19/ZXUK/3KFOhXB76Su+gy7/xZ" +
                "OuCrl35lqpRYn2OzP8t06NeeEI8hf85ku2zH+/6ZbYOvFvU3i7" +
                "fxjvh8eecP1l+1gq8x86MS5MBY/Za9O+bN9yl4P6nh/NfD/qoC" +
                "fHXh+7p+Q1a6+WL9hiX+XOWPfukO+Frtz9XgazXfGvD9/0h6Bt" +
                "7St60=");
            
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
            final int compressedBytes = 488;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmjtLA0EUheW6GXdHsVLBF4hB1FJQUCuDhdhY2AuijaWVf8" +
                "CAIliJRPE/qH1ELWxEUOMjqL2lsRDrdYkx7G7UJWQSZm7OhXCz" +
                "JJvifJxzZ2ZDLW5JUR/1Bq7jNEydxSuH2kh4vZ26qcfrQ96ro/" +
                "DZIDXlu3RdOeL11uJdA77f63Iji/pdlIKi6vEdBV9dS46F1N6C" +
                "JvBvyL/j8C9rvhPga0xeT0IDVjynMH+RzxH5nEA+G+PnaWgA/4" +
                "b8OwP/6sBXzlaHb+wVfLXI3rmfd7Gdv74T24VOPPJZWsryuRH+" +
                "Ncbj89DA2Pm7KI7Fkfr5Kw7hX13y2UrlXbri52sdVMbX2gNf1v" +
                "ujVfDVIp/XxK24ExkvUbPiRjwoy+d78FVZ4rHsO9K+FdR6QN1k" +
                "SO1fzp9pA5qzWT8noQHmb2j+biKfWfPdBl9t8zgVPX9RBvPdB1" +
                "+e+YzzZ758ZbOdwP63LvP6BBqY6195ai/9v362l8v3r70A/+qS" +
                "z/JM/f4IfPXhW3h+dO7ni+dH7PhegG+drKcusf9l6N8r+LdO/H" +
                "sN//I731C8fv6Af3Xha3+W8q00n2UGfDVI4ux3t+IBdfH/DVb+" +
                "dd7U57N8qpV/5TM4RvB9V8/XydWM7ws4VpjiOWhgZjV8AeyzVh" +
                "U=");
            
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
            final int compressedBytes = 422;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmz1Lw1AUhuWa5sakdFLBLxDFT1REVzcdxU/UxUlBxEVXBQ" +
                "cRi/YPKPizFH+Fg4ODgxJDqSWNQ8QmcHJ4DpRL6MdwHp7z5pZc" +
                "Uw5/lRk2Qy3Xo2bK9DWvuky3caO1xwyYwWidjF69jfcmjK2vfh" +
                "j6b9FaaX5rLPZ7/WFqmZGQyqBM2X/Ph6/3Cl8JfHPz9wO+qvl+" +
                "wldq+V+JbtfoCf62+ht04q9qvg58VfO18JVagUf+4m+KvxX8lc" +
                "LXua9bOx7n6zy2x9d5gK/Y+TzNfNbibzBjt+ym3bDb7otdd5+z" +
                "ms/uE/6K4DubT/6W1uCr+v5qDr4Cknb+L58qrdKpws7nBfJXsb" +
                "+L+Ev+/it/l/BXrNXL7H9V812BrxKSO+Sv8vvnXe6fFfu7h79U" +
                "sE8P2B8l9kcHzGfVfA/hK3YeH7E/wt8Uf4/xV6y/J/irZv97+v" +
                "P8lVeN+9vu81feDf4WxuczekD+JvL3HH9FzOeLfPg6V/CV56/v" +
                "ZHY+lPNlxcnfS3pA/iby9xp/VfO9ha9qvnfwlcK3cb6sluX/G5" +
                "wvk1Ad33wbKvY=");
            
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
                "eNrtmklKxFAQhqWImvY9e6WCE2g37bDzDN5DF3obBTfOeAM3Lk" +
                "W8gHcQRBQXDuC0cNg8Q9M06SAa1JZK9VcQHklIFv/H//6qEPFu" +
                "NWRKJmS85bwqszLcPCvJgPQk66CMyliyziTHUOPetPTW174Qoo" +
                "NkLTefqqXeNxK+LakE6t/KraFBMUv8J9f+xL9uHf+a5rsBXy18" +
                "o+36LryZ5hvt/TJ/d+Crga/bor8yzXe3PXy7r+Crtl/ez6i9gi" +
                "ameR+igWm+R2jAfJSZj47JX9N8T+CrbP49Zf41zfcMvuzPefnG" +
                "z/AtTP98jgb4N9NfXeBf03wv4auBr7uOF+Olr/nGyz/I3wX4mv" +
                "bvDXxN872Fr2m+d/A1zfcevqb5PsBXC9/G98nHNF++TxqZj57a" +
                "41/+vypOuRc0IH8z+fuKf5Xl7xv5azB/38nfzvCvL+Hfzizfjw" +
                "am+ZbRgP65dX/2k+zPpvlW4Kusv6rSX+Hf3P6twVeZf6fwL/7N" +
                "7d85+Crz7zz+tVZdH+uzXec=");
            
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
            final int compressedBytes = 200;
            final int uncompressedBytes = 9601;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt1k8LAUEYx3E9hWhyQvmzJRLehxsXZxwcvAGHlZOivAFSXu" +
                "2apA057I359f3V9DSzzbbNZ58aN04yJD9NSJBxE3yJm3EGYcbc" +
                "l7WORW/zno2skc5KVrWCrzVrWdvXoR/157OBFR+17P+Jua+VdF" +
                "f/5X3NDN/VxebPfRf4Svsu8ZX2XeEr7bvGN5j784YzoH8/+jem" +
                "f6V9t/hK++7wlfbd4yvte8BX2veIr7TvCV9p3zO+0r4XfKV9r/" +
                "hK+97w/X1yd1YCwI8=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
                "eNrt3ckNwCAMBED6LxrTAbyQbO1MAfn4hERK1c1ekKzUB/JXfQ" +
                "EM63+v5+u/8gsAwH4J4Pzn/In4ApA2n7w/lz/uN8RffAH0d+iS" +
                "X9PzV/2B+kD+AID5CgBg/wAAwH4LoD/qjwBgfgMA5j8AAAAAAA" +
                "AAAAAAAPT1+/+P8d/vH32dGf0=");
            
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
                "AAAPikVyUQgP8CAAAAAAAAAAAAAAAAAAAAAABXAz+SPcE=");
            
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
            final int rows = 801;
            final int cols = 8;
            final int compressedBytes = 58;
            final int uncompressedBytes = 25633;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt07ENAAAIw7D+fzTwAXMH+4RISQAAAIB2+xqBwN8AAAAAAA" +
                "AAAAAAAAAAAAAAAAAAQLMDsd89wQ==");
            
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
            final int rows = 38;
            final int cols = 124;
            final int compressedBytes = 170;
            final int uncompressedBytes = 18849;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2EkSgjAARNE4Kw4Id8Jz5eTAEXThQtOvepvVf5WCSi31za" +
                "a5Fmtj5YMz06ITb+NtTXj7frvfxtt4G2/jbbyNt/G2r3q/jjpF" +
                "eT91ivLe6BTlvdcpynunU5T3Taco716nKO9Opyjvs07e18z/uf" +
                "G2v/MedIry3uoU5f3QKcp71CnKW6cs74tOUd4nnaK8DzpFeV91" +
                "8r5mzXrfdXK/jbf9vndZAZ5lXA8=");
            
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

    protected static int lookupValue(int row, int col)
    {
        return value[row][col];
    }

    static
    {
        sigmapInit();
        sigmap1Init();
        sigmap2Init();
        valueInit();
    }
    }

}
