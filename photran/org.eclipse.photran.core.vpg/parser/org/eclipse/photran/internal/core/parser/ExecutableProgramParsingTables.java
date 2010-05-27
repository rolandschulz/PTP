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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 19, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 109, 189, 190, 0, 191, 192, 101, 35, 1, 29, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 58, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 3, 58, 1, 8, 123, 4, 124, 0, 5, 127, 125, 221, 237, 6, 7, 128, 126, 173, 0, 238, 206, 8, 212, 214, 239, 215, 88, 29, 9, 216, 217, 218, 219, 101, 29, 113, 10, 220, 11, 240, 222, 12, 227, 13, 0, 14, 228, 2, 129, 230, 150, 241, 231, 242, 15, 16, 243, 29, 244, 245, 17, 246, 247, 30, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 137, 259, 260, 261, 262, 22, 23, 263, 264, 24, 265, 266, 25, 3, 267, 268, 269, 26, 27, 152, 154, 28, 243, 270, 271, 237, 241, 272, 273, 4, 31, 274, 39, 29, 39, 244, 275, 276, 277, 0, 88, 278, 39, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 56, 289, 30, 290, 291, 156, 6, 292, 293, 294, 245, 295, 296, 297, 238, 298, 299, 103, 300, 7, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 29, 39, 311, 32, 312, 313, 33, 314, 5, 315, 316, 317, 34, 318, 0, 1, 2, 319, 320, 321, 29, 35, 322, 239, 323, 144, 324, 325, 326, 58, 8, 327, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 328, 255, 256, 329, 242, 9, 173, 10, 330, 331, 36, 332, 88, 333, 257, 334, 335, 336, 258, 180, 250, 259, 337, 338, 339, 262, 264, 340, 341, 101, 342, 343, 344, 345, 346, 347, 11, 37, 38, 348, 12, 13, 14, 15, 0, 349, 16, 17, 18, 39, 19, 40, 20, 266, 41, 42, 21, 22, 23, 350, 351, 0, 352, 353, 24, 26, 28, 32, 43, 44, 33, 34, 36, 354, 355, 37, 35, 38, 356, 40, 357, 358, 45, 41, 42, 46, 47, 359, 48, 49, 50, 51, 52, 53, 360, 361, 54, 55, 362, 56, 363, 364, 57, 59, 60, 365, 61, 62, 366, 367, 368, 63, 64, 65, 66, 369, 67, 68, 370, 69, 70, 71, 72, 371, 372, 373, 73, 74, 75, 374, 76, 77, 78, 79, 80, 1, 375, 376, 377, 378, 379, 81, 82, 83, 2, 84, 85, 380, 86, 3, 87, 381, 88, 89, 90, 0, 382, 383, 91, 92, 4, 46, 384, 93, 94, 385, 6, 95, 386, 3, 387, 4, 47, 96, 388, 97, 5, 389, 6, 98, 390, 99, 391, 392, 100, 102, 7, 393, 104, 105, 394, 48, 49, 395, 106, 8, 107, 108, 396, 109, 397, 398, 1, 399, 400, 401, 402, 403, 404, 123, 110, 111, 405, 112, 406, 9, 113, 114, 50, 116, 10, 117, 0, 118, 8, 11, 119, 407, 120, 121, 12, 122, 123, 1, 124, 126, 127, 13, 129, 14, 0, 128, 408, 130, 131, 132, 133, 134, 409, 135, 136, 410, 137, 138, 139, 140, 411, 141, 412, 413, 414, 142, 15, 415, 416, 417, 418, 419, 420, 421, 143, 145, 422, 146, 423, 147, 17, 148, 181, 424, 425, 8, 426, 149, 150, 19, 151, 152, 427, 117, 428, 15, 153, 154, 155, 20, 156, 157, 21, 19, 158, 159, 429, 22, 430, 431, 432, 160, 433, 434, 435, 436, 161, 162, 51, 0, 163, 164, 165, 166, 167, 437, 168, 23, 438, 439, 440, 441, 169, 52, 143, 170, 171, 172, 173, 442, 443, 444, 174, 175, 176, 177, 24, 8, 178, 445, 446, 447, 448, 449, 450, 179, 451, 101, 452, 453, 454, 180, 53, 455, 456, 181, 457, 458, 459, 460, 461, 182, 462, 463, 251, 464, 465, 185, 183, 184, 466, 467, 468, 469, 470, 186, 471, 472, 187, 473, 474, 475, 476, 188, 477, 2, 478, 479, 56, 189, 480, 481, 482, 483, 484, 485, 486, 190, 487, 488, 489, 191, 192, 490, 491, 492, 101, 194, 493, 494, 193, 495, 195, 496, 497, 498, 499, 15, 260, 25, 500, 196, 501, 261, 502, 267, 503, 269, 504, 270, 35, 197, 198, 199, 200, 26, 201, 505, 506, 507, 202, 508, 274, 509, 510, 511, 15, 275, 512, 7, 8, 58, 9, 10, 513, 11, 514, 515, 516, 16, 148, 517, 17, 203, 518, 278, 519, 61, 0, 3, 520, 521, 522, 523, 524, 1, 525, 283, 3, 526, 288, 527, 528, 529, 530, 531, 532, 533, 21, 534, 168, 535, 536, 537, 27, 174, 538, 539, 179, 540, 541, 29, 542, 35, 18, 543, 544, 204, 205, 545, 207, 546, 208, 547, 209, 548, 549, 210, 550, 551, 211, 552, 212, 39, 553, 554, 555, 556, 557, 558, 63, 559, 64, 65, 66, 213, 560, 561, 562, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 73, 577, 578, 579, 580, 581, 582, 221, 583, 584, 585, 586, 223, 587, 588, 589, 224, 590, 591, 74, 75, 76, 88, 54, 592, 101, 103, 593, 4, 594, 215, 595, 596, 206, 597, 598, 599, 600, 5, 601, 6, 602, 12, 14, 603, 604, 605, 606, 27, 607, 608, 609, 217, 610, 611, 218, 220, 612, 104, 613, 614, 615, 616, 617, 618, 222, 225, 619, 226, 620, 182, 621, 227, 15, 622, 623, 624, 625, 626, 105, 107, 627, 628, 629, 108, 630, 109, 114, 115, 116, 228, 631, 122, 632, 633, 2, 634, 123, 125, 130, 635, 636, 229, 637, 638, 139, 141, 143, 144, 145, 55, 639, 640, 641, 232, 62, 19, 642, 643, 644, 146, 7, 20, 23, 645, 646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 158, 4, 660, 661, 662, 160, 161, 159, 663, 162, 231, 56, 169, 170, 172, 173, 664, 178, 179, 180, 665, 181, 182, 183, 666, 6, 184, 185, 186, 233, 234, 57, 235, 236, 667, 59, 62, 185, 67, 68, 69, 668, 669, 8, 9, 670, 671, 672, 673, 674, 675, 676, 677, 678, 679, 28, 29, 30, 680, 681, 682, 683, 684, 685, 686, 687, 688, 689, 690, 691, 207, 692, 693, 694, 695, 696, 697, 698, 699, 700, 187, 701, 188, 702, 703, 704, 189, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 24, 25, 26, 32, 730, 731, 732, 733, 734, 190, 735, 191, 736, 192, 210, 193, 737, 240, 738, 244, 739, 740, 194, 741, 63, 742, 743, 744, 745, 746, 225, 747, 195, 748, 749, 750, 751, 752, 753, 754, 755, 756, 196, 757, 758, 759, 760, 201, 761, 762, 763, 764, 765, 10, 766, 767, 768, 769, 770, 771, 772, 773, 70, 7, 202, 203, 774, 775, 776, 777, 778, 779, 780, 781, 782, 214, 64, 215, 217, 783, 71, 225, 226, 230, 1, 233, 234, 72, 235, 236, 237, 238, 239, 242, 247, 250, 251, 254, 255, 784, 289, 246, 785, 786, 0, 0, 58, 39, 787, 788, 789, 256, 263, 73, 264, 267, 74, 292, 790, 65, 791, 219, 220, 222, 228, 231, 243, 248, 792, 249, 238, 793, 241, 794, 795, 796, 797, 798, 43, 253, 75, 799, 800, 258, 259, 8, 801, 295, 266, 268, 802, 79, 803, 299, 804, 269, 270, 271, 272, 805, 806, 301, 807, 245, 808, 273, 274, 276, 809, 810, 247, 248, 811, 249, 812, 813, 814, 250, 815, 816, 817, 251, 818, 819, 66, 252, 253, 820, 821, 258, 254, 822, 823, 824, 825, 259, 826, 260, 827, 828, 829, 67, 261, 830, 262, 831, 832, 833, 834, 80, 277, 278, 835, 1, 81, 73, 82, 83, 77, 84, 78, 85, 79, 836, 279, 280, 281, 837, 838, 263, 839, 282, 264, 840, 841, 842, 266, 843, 88, 101, 58, 283, 284, 61, 308, 109, 268, 844, 63, 845, 269, 846, 64, 285, 286, 2, 65, 287, 86, 288, 289, 66, 290, 847, 291, 311, 848, 849, 1, 850, 310, 851, 852, 292, 80, 270, 853, 854, 293, 294, 295, 271, 855, 856, 272, 857, 858, 273, 859, 860, 275, 87, 296, 297, 298, 86, 299, 300, 0, 276, 301, 302, 861, 303, 304, 305, 282, 862, 863, 864, 306, 307, 87, 89, 90, 91, 92, 93, 94, 95, 99, 100, 101, 102, 106, 110, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 1, 865, 319, 320, 321, 322, 323, 324, 866, 325, 867, 868, 327, 869, 870, 326, 328, 871, 329, 331, 332, 334, 333, 335, 872, 336, 67, 111, 337, 338, 339, 340, 341, 873, 343, 344, 346, 347, 342, 345, 874, 354, 356, 348, 350, 358, 365, 366, 369, 370, 373, 374, 377, 875, 379, 351, 876, 277, 0, 877, 352, 375, 878, 879, 355, 880, 112, 881, 882, 883, 279, 280, 376, 357, 281, 359, 313, 360, 378, 884, 885, 361, 362, 363, 368, 285, 886, 372, 364, 380, 382, 385, 113, 384, 386, 68, 388, 887, 381, 390, 391, 392, 393, 888, 394, 889, 890, 891, 286, 396, 398, 399, 400, 892, 893, 894, 401, 895, 402, 403, 69, 397, 113, 404, 405, 406, 407, 408, 117, 118, 896, 409, 897, 287, 898, 410, 411, 899, 412, 414, 419, 422, 2, 900, 901, 423, 424, 425, 426, 89, 428, 902, 429, 430, 431, 432, 433, 434, 435, 90, 437, 438, 318, 439, 440, 321, 441, 903, 443, 415, 416, 904, 905, 417, 906, 907, 908, 909, 442, 910, 444, 11, 911, 912, 445, 447, 119, 120, 121, 451, 91, 913, 914, 915, 92, 290, 296, 916, 917, 453, 418, 918, 919, 3, 920, 921, 922, 923, 93, 924, 124, 925, 926, 927, 446, 928, 4, 929, 930, 448, 931, 932, 94, 6, 933, 934, 935, 126, 936, 937, 938, 939, 297, 940, 941, 95, 97, 942, 298, 943, 454, 455, 457, 449, 450, 452, 458, 70, 0, 456, 1, 459, 2, 460, 461, 71, 462, 99, 2, 72, 463, 464, 465, 466, 467, 127, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 3, 300, 487, 488, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 500, 502, 503, 505, 304, 504, 302, 506, 508, 509, 944, 129, 510, 513, 516, 4, 303, 507, 511, 517, 512, 5, 518, 945, 520, 514, 305, 309, 515, 521, 522, 523, 524, 525, 526, 6, 312, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 519, 946, 947, 540, 541, 948, 949, 950, 314, 542, 543, 3, 139, 141, 544, 951, 545, 1, 952, 953, 4, 548, 546, 142, 100, 12, 547, 954, 549, 550, 123, 73, 955, 956, 551, 552, 554, 957, 315, 958, 959, 316, 556, 960, 317, 7, 961, 962, 319, 963, 964, 965, 143, 558, 553, 555, 966, 557, 559, 967, 320, 968, 562, 324, 969, 568, 970, 322, 323, 577, 560, 561, 971, 972, 973, 974, 582, 975, 976, 977, 325, 978, 979, 144, 980, 0, 981, 982, 983, 330, 984, 985, 986, 987, 988, 989, 145, 101, 102, 103, 147, 148, 990, 150, 151, 153, 154, 991, 992, 104, 993, 994, 74, 995, 996, 326, 997, 583, 563, 564, 565, 566, 567, 569, 330, 998, 155, 999, 1000, 124, 75, 1001, 76, 1002, 5, 570, 590, 77, 156, 579, 580, 105, 571, 572, 125, 573, 1003, 1004, 331, 1005, 332, 1006, 1007, 586, 1008, 592, 587, 1009, 593, 1010, 1011, 342, 106, 1012, 107, 594, 595, 597, 598, 599, 600, 603, 1013, 574, 575, 576, 578, 1014, 601, 604, 605, 581, 1015, 606, 1016, 1017, 607, 1018, 608, 1019, 609, 1020, 610, 157, 1021, 1022, 584, 612, 1023, 613, 611, 1024, 1025, 1026, 614, 585, 6, 7, 615, 616, 617, 618, 1027, 349, 1028, 1029, 1030, 357, 619, 1031, 367, 1032, 335, 1033, 620, 588, 1034, 1035, 589, 108, 596, 621, 622, 623, 624, 2, 1036, 1037, 1038, 126, 78, 591, 79, 625, 1039, 359, 627, 1040, 1041, 1042, 1043, 360, 630, 1044, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 633, 635, 1052, 1053, 636, 637, 1054, 632, 361, 1055, 634, 638, 1056, 640, 1057, 1058, 163, 1059, 1, 1060, 1061, 641, 643, 644, 645, 642, 109, 9, 646, 647, 164, 362, 13, 1062, 648, 1063, 1064, 1065, 1066, 363, 1067, 368, 1068, 165, 166, 651, 80, 1069, 1070, 1071, 1072, 1073, 652, 1074, 649, 1075, 653, 371, 650, 376, 654, 1076, 655, 110, 1077, 1078, 10, 656, 657, 658, 659, 1079, 660, 1080, 668, 1081, 669, 661, 390, 662, 111, 1082, 1083, 11, 1084, 671, 664, 391, 1085, 394, 1086, 167, 666, 1087, 1088, 168, 1089, 171, 1090, 395, 1091, 404, 405, 1092, 1093, 81, 670, 1094, 1095, 1096, 0, 1097, 1098, 1099, 1100, 1101, 672, 1102, 1103, 1104, 112, 406, 1105, 1106, 1107, 673, 674, 675, 82, 676, 1108, 677, 678, 1109, 679, 1110, 1111, 680, 1112, 1113, 1114, 1115, 174, 681, 682, 1116, 1117, 683, 684, 1118, 0, 1119, 1120, 1121, 175, 8, 176, 685, 686, 1122, 687, 177, 688, 689, 1123, 690, 1124, 193, 197, 1125, 407, 333, 1126, 691, 1127, 692, 1128, 693, 1129, 1130, 694, 695, 696, 1131, 12, 408, 1132, 697, 198, 1133, 698, 1134, 699, 409, 700, 410, 411, 1135, 412, 701, 1136, 1137, 343, 702, 703, 1138, 1, 1139, 1140, 413, 1141, 1142, 113, 1143, 115, 1144, 423, 1145, 424, 1146, 83, 3, 4, 704, 705, 1147, 127, 84, 425, 1148, 426, 706, 1149, 9, 1150, 199, 707, 708, 1151, 709, 1152, 200, 345, 710, 711, 712, 713, 714, 715, 128, 716, 1153, 427, 717, 118, 1154, 119, 1155, 1156, 1157, 204, 1158, 718, 13, 1159, 719, 720, 721, 1160, 722, 14, 724, 1161, 725, 726, 15, 17, 18, 1162, 727, 728, 729, 1163, 730, 205, 731, 1164, 1165, 732, 733, 1166, 723, 430, 734, 735, 336, 736, 737, 1167, 1168, 1169, 738, 739, 741, 740, 2, 129, 85, 121, 742, 743, 744, 1170, 1171, 745, 1172, 428, 1173, 339, 124, 125, 0, 126, 127, 746, 747, 207, 86, 87, 748, 749, 88, 750, 208, 89, 751, 1174, 752, 753, 754, 755, 756, 757, 758, 759, 760, 761, 1175, 762, 763, 1176, 764, 1177, 1178, 766, 129, 1179, 216, 187, 1180, 1181, 429, 765, 431, 1182, 767, 768, 1183, 130, 1184, 1185, 769, 1186, 19, 131, 432, 1187, 1188, 770, 771, 772, 8, 1189, 1190, 1191, 20, 433, 132, 1192, 773, 774, 1193, 2, 434, 225, 226, 227, 435, 437, 1194, 775, 1195, 1196, 776, 777, 90, 778, 230, 781, 782, 133, 783, 784, 785, 1197, 786, 787, 788, 438, 1198, 1199, 134, 1200, 1201, 1202, 1203, 789, 790, 1204, 791, 439, 1205, 1206, 1207, 234, 792, 793, 794, 348, 795, 233, 1208, 353, 796, 797, 1209, 440, 798, 799, 800, 351, 801, 9, 235, 802, 10, 11, 1210, 803, 804, 1211, 1212, 1213, 441, 1214, 459, 1215, 466, 1216, 1217, 467, 1218, 1219, 135, 1220, 136, 1221, 1222, 1223, 1224, 1225, 349, 236, 805, 1226, 352, 130, 470, 91, 365, 1227, 806, 807, 808, 809, 810, 811, 812, 366, 1228, 813, 371, 815, 1229, 131, 92, 814, 1230, 1231, 1232, 237, 238, 816, 817, 1233, 818, 819, 1234, 820, 821, 822, 1235, 1236, 823, 825, 826, 1237, 827, 828, 471, 10, 824, 11, 12, 1238, 1239, 829, 830, 831, 21, 22, 239, 832, 1240, 242, 1241, 93, 833, 1242, 834, 1243, 1244, 1245, 835, 1246, 836, 837, 1247, 838, 839, 840, 841, 842, 843, 472, 844, 1248, 137, 1249, 845, 13, 1250, 23, 846, 138, 1251, 1252, 1253, 1254, 1255, 473, 850, 14, 1256, 477, 139, 1257, 1258, 1259, 1260, 1261, 478, 851, 474, 1262, 479, 1263, 480, 1264, 1265, 481, 1266, 1267, 1268, 1269, 6, 14, 1270, 1271, 1272, 1273, 245, 1274, 847, 848, 849, 852, 1275, 853, 854, 367, 12, 246, 247, 1276, 855, 857, 13, 858, 370, 1277, 482, 483, 15, 1278, 17, 1279, 250, 1280, 1281, 484, 1282, 1283, 1284, 142, 143, 7, 8, 859, 860, 861, 856, 485, 865, 372, 1285, 1286, 486, 375, 1287, 1288, 378, 862, 14, 868, 251, 866, 1289, 94, 253, 254, 488, 487, 867, 869, 870, 1290, 871, 872, 875, 876, 878, 880, 882, 1291, 1292, 1293, 1294, 15, 883, 1295, 1296, 873, 874, 877, 1297, 1298, 379, 255, 256, 257, 1299, 884, 1300, 188, 1301, 1302, 24, 490, 1303, 1304, 1305, 1306, 492, 493, 879, 489, 1307, 1308, 885, 1309, 1310, 1311, 1312, 495, 496, 886, 498, 1313, 1314, 1315, 261, 190, 1316, 95, 888, 889, 1317, 0, 262, 890, 891, 499, 263, 1318, 892, 893, 894, 1319, 895, 1320, 1321, 896, 897, 899, 901, 900, 383, 1322, 1323, 903, 1324, 908, 1325, 500, 1326, 1327, 1328, 1329, 385, 387, 388, 1330, 96, 501, 502, 389, 905, 902, 904, 906, 909, 910, 911, 1331, 514, 35, 1332, 144, 145, 1333, 1334, 1335, 912, 919, 1336, 1337, 929, 1338, 913, 16, 914, 915, 923, 933, 1339, 917, 515, 1340, 1341, 918, 935, 936, 521, 1342, 1343, 522, 523, 920, 524, 1344, 1345, 146, 1346, 937, 525, 921, 526, 1347, 1348, 147, 1349, 527, 1350, 1351, 1352, 148, 922, 1353, 528, 924, 1354, 925, 390, 927, 529, 938, 941, 928, 930, 931, 530, 1355, 391, 395, 1356, 932, 398, 402, 934, 1357, 150, 151, 153, 1358, 1359, 944, 940, 945, 946, 947, 942, 948, 1360, 1361, 1362, 1363, 943, 1364, 949, 1365, 531, 1366, 1367, 156, 1368, 1369, 25, 1370, 157, 1371, 1372, 26, 194, 950, 1373, 2, 1, 1374, 952, 954, 953, 406, 413, 415, 416, 532, 533, 955, 417, 1375, 1376, 1377, 264, 265, 1378, 956, 959, 1379, 958, 1380, 960, 961, 963, 962, 965, 267, 1381, 1382, 27, 534, 1383, 1384, 28, 535, 1385, 1386, 275, 160, 966, 967, 968, 418, 1387, 419, 970, 283, 284, 285, 536, 537, 286, 287, 288, 971, 1388, 972, 1389, 975, 973, 538, 1390, 1391, 539, 545, 1392, 1393, 547, 976, 16, 977, 544, 551, 552, 554, 1394, 1395, 978, 979, 980, 289, 291, 1396, 556, 1397, 1398, 583, 1399, 290, 420, 1400, 1401, 1402, 981, 982, 1403, 1404, 983 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 8, 1, 21, 2, 22, 23, 24, 25, 26, 2, 2, 6, 27, 0, 28, 29, 30, 31, 23, 32, 7, 33, 34, 0, 35, 30, 36, 37, 38, 39, 40, 36, 6, 9, 41, 14, 42, 43, 44, 32, 45, 46, 47, 48, 49, 18, 50, 38, 51, 23, 1, 51, 52, 8, 53, 30, 54, 55, 34, 56, 41, 57, 58, 59, 60, 40, 61, 62, 0, 63, 64, 65, 2, 66, 3, 67, 68, 42, 47, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 38, 80, 81, 40, 56, 82, 47, 83, 84, 6, 85, 71, 86, 52, 87, 88, 89, 90, 42, 3, 91, 0, 92, 93, 2, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 46, 58, 107, 108, 109, 46, 69, 110, 111, 70, 112, 72, 4, 113, 2, 81, 114, 115, 9, 116, 117, 2, 118, 68, 72, 73, 119, 120, 121, 122, 123, 6, 124, 125, 126, 127, 128, 129, 130, 2, 131, 6, 74, 4, 132, 133, 75, 90, 134, 135, 136, 100, 137, 106, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 91, 152, 2, 107, 83, 153, 154, 155, 1, 156, 3, 157, 158, 0, 159, 160, 161, 162, 163, 5, 3, 164, 165, 0, 166 };

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
            final int compressedBytes = 3276;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXb1vHccR31ufmBWRwCvCkohUR0FWGECFnMYJGx3lOJYDBC" +
                "EoEYgRCAZSJCpYBa4EFUtGCKhULFSofFAhwJ3/BCJQIbhSESOG" +
                "/prc13vvPnb3N7tzx8ecLYrUcHZnZ2bnc/fe+w//enDt29uXH1" +
                "46uv3dT149Orv79Ff5sy9/uGIefL9zclO8/+Otg2vPH3/50aWj" +
                "wwJ+UMJ3n/3hhyunD97X8JsH159v/+lFCf/s1cGbT5/uV/inD3" +
                "7cMSPgf3gL0MebXxb/aSESUT5rxZ/NvTvFVyNELqUSI6x/Yv4A" +
                "/OrJhOdJ+v+g2z9MTf/Zz27+9tLRtvpIiNvf7R5fPVvb3hdGfV" +
                "4QkYtz4R/Sv5vF+AV9pf59UIz/6dONRv/ezvEL+P1q/jacSh/U" +
                "bz/++rq4uqEKRb10lAuZ5eKTb3T+7PPTxOyf7FTy+9uj688P//" +
                "yfj/9x+OqL11+/ufv0L5/989F/S/wnJ9PvL4r806PtzRel/JPj" +
                "q2/WtjfC5M+j7+frsuDf/aTgX7H/C/795ps7+bPdkn//2lHiwu" +
                "9vCn12/d2v9BfZh+ntO95fk/oHJn6tP6qjP7sd/QH2WYlUlF+b" +
                "p/hBy+b7XEzvf2Rb2Hnra/HI85A/gLP1k2lfp/f/q4UPHtPnL5" +
                "c/F5y/k/s/nv/v2od0Hp/lbf33xh9O+7pfwb9fwg9t/mHy9U/M" +
                "PxyfsePr1B4/JyT7tFFEbnppcItk5KEuvpb5B9H++uGNfO+7/P" +
                "/0+dO0+6+JH+v8Ianyh8D48RdNfHx0OFvEx/X6nlDjh+c1/bOa" +
                "/hvj+pdp40eZy9QqP727UcqPDR/qR9bWD4RvyU/7+Wvqy18RHO" +
                "WfCP7vGl7nL7tV/rKAyxHg3PyYO34I/qzGvxdCH8V++P0bsB8t" +
                "/zer8WU7v4D5Mxgf4UfARTd/nzZ/nuPH2s+x5nfht+GzGi7bcF" +
                "v+3sm/gPymxl8FPJ92/KxTH3HFl6f7byn1h8o/fXvY4e9urR9L" +
                "/+eBF1qQovqlff8385Pkq6L1I2L8rD0+qg9w4d39+bran/ea/f" +
                "lksH+HcO74fXzRwhdx6xOc+Qfj25LiJaDyj6nDf0pK/q1yb3SD" +
                "6jNW+iyPFpGP8oO5+euq4Tb96Mpn9PlFGH08+4jyf3f+Wv/tjp" +
                "/2Q/JPZ32hDY+xzxb6RTf/9uc/KP9uBpLLGmTa2UoInwvvrU/2" +
                "t/IE84sQfG59oY1vk38rPhbL+Lnyjy93BCG/FPKnvvwCwQn1D9" +
                "b46793rq+K/7n1DYTfhs9qeCe+RvVhPjwHTsbvH7sOMtx/QfpU" +
                "xEITiznVSrbRVPONVH79reODbElK2iNOzTmg5mR1Xb3KjUhL81" +
                "XDlRGb5d/b1ZiaLb+Kfl3SL2v6X5b06+K3zhr6x5OPtON71w/p" +
                "i5b/gr9EuLKHYkC/kH1b7g9drTZcv236pZtfVJi+Pn83+/J36a" +
                "9phIfW5zcAHYdY/bDXbK7cri/c/Y3wjVW+KX98xbG/6XT2D+Uv" +
                "FPsGKrwe2cfbH0Pbn5C/c/surPa9Gd8r/9Qxb0rhv3Jle2Yc+Y" +
                "b6z0D7xt5/AtpXWR8h6PuHjAbn+r9x+J/y/Y8PzrD/OP5G9j2s" +
                "WmFC7Qv0X9InP3t85rEvJnD/Yf1A+suMb3ML81slOOXCd9h/Ex" +
                "i/Q/0k+df49QP6R9fv/vio/wn0F59PSFJO/5Tbf0Ty39I/ysLm" +
                "pDf0mkx+XfxsTCIynSkxy8QuLT5oxb80+ed0ixMnn+X+5/YPaY" +
                "quoveX6ReVg89/8uCo/9iG2+oT50Wfqz9Z8Zdk0x36SdZvx/k8" +
                "Zn9yaL8eOvJLh6oB+lF/ENHnrE/r4fid+iFx/Ij+4vB8pA8fyv" +
                "ee137h+nfuT++QfC54/y8CX4Tho/iFVgWKjm+61YtB/hhMf6D9" +
                "QP1L5L8p549Y/dHw+DWyASrBL9vh/P5hoPwGQau//gDtk9t4aB" +
                "p+7rZfKtT+hPfHDYz/cu8+DTzfS+m/icD+nvD1Fy39R1uZaA6X" +
                "FvyVwkfov3r5HyG/sPwUWow8wjNoS/0xMv4j28/2/lBk+8noX7" +
                "7snW+kwkWvv4v6q9741YKP4EH9X9S/tcBFG87vT/P6v2390Nb8" +
                "RPj7hzgykqWGbRUYg/rVGtY/dH6kie8/EB+78gfe/qLW50iu2h" +
                "pf5CD+oOIHBTv0/h3kHxUuefV9HVcfjuhP3Qmp33HnL/jMk/92" +
                "jVjsL5OaQm5GalWWz5JCjNcs/S0TuCCEr/IWJ20p3BlTf5n8Ee" +
                "+6+E0tRvXrj4XwbfXHLf22Cxc1XPThyg4X4n4VgVTcUaWimbn/" +
                "z5aBulzwd2lfifYXjV/yJ/XVX/3rQ/TB9QP6kf8a0Gcc/Efycc" +
                "Mr/two1uiDywHckNaH+AP538jPJV/6+mWc/jbri9NPwV8/mn/A" +
                "HzHcXyT61/30O/iH9h+yL7h/xfT/yl+igv7/BNmfPeBO9sL8Yy" +
                "gc9Qe5/gWev0H+ieq/4uBIfuUfPcefn48S9PvF48V/kfWf0PEN" +
                "2B8mNH7gxdeE/ra3v4/vV8TUH0a0L1z+MMfH9Z+J6YPnx/KJ+Q" +
                "+WA/P3vaZGJqUtPp5e/jC/Lv8v6Fvv7o/UJMmo/jH0IeoHys+s" +
                "RV0N8i/7UcG4/Hghf2t+5D7/0+FvCvonk60Pnx8h2b80Wj9gfj" +
                "bt/kH3b7j+mX4+Ja4+Eld/4trvc4xPrOfrQvxzr79H1r/ZvD7L" +
                "yr9J+l9FK7Jjn1U1v4c+Wc9vahxVIp0u+xP5WP6RHR9M7R9g/h" +
                "NYXzRj04fCSjJ+EtZxo/tXI5LZL48fFyhfmQ2TvJjpmfrddpaa" +
                "tRXErwz5yP/L+JyiqJbfz9Nzyg/PSramWeVc2vRkgXAZB1/kd4" +
                "7+F5w/Fx37Ohy/Hz9p3H/RAfxB/H3XxV/Ej9T1w/zV679g/FDY" +
                "h2KG2ZYo7IO4nBWjnZoiPMy3ffMH5/fofsaEcBGBr91wUn3k/O" +
                "In2vnH6exb/P2SmPghY9RvHPnbudVn7PPj+B7Ub0/EceaLb4W4" +
                "UoHSUkRFfpKVFO3qxOiTTRr9lf3qnp/r8A/AXTH7OPUtJB/VuR" +
                "6ZWV5aQ41PpLP+wop/ufuD7z9i4JJsv6z113b/m2c/A/JreS71" +
                "XcOEB+dXvP3BzW/571ceTb7j1JdH9u8F/44nrS/Q9VMy4ePUrw" +
                "fenBkf4f4OqP8E16fC4Oz4DRgBxRt/6v2J7q+x+Yf3D/N85hTx" +
                "Nx0/4v5XZ4tCfDL/5+ej+9PdY8rv1iPw/rZHrPtjK5YfvJ+E/Q" +
                "d6/yPQ70D+GM7+s61P++Mvcv1c2OvncBQ//fB+l7K8P6c5H0LB" +
                "R+8P494PQ/St3H6OWh95Hef/w/UnJ9GnBN8+Me3j1PET4fN5QH" +
                "7faOZCAMH3s/zjM9+/RHg/Cev+lmDez4L3v4D8CPfHPqnrQwNm" +
                "ndQ/5rJhua/+44OvoP9C5y86/zd1fMq9P8Z+/55EYD999Pt5nv" +
                "MvqRu+pV9W51cyUZ9fyRbnV7LmfDFhfB+cXL/z40e/P4s7P9o8" +
                "5PfjhdwfWT7o/Au8P6SIiuiqv4pA/MD4M87/0fHR5xNs6ZNG/9" +
                "PqfHxWna8vlafUfxx/BdzfiorP6fK1698o52tI8akC9XsiPNI+" +
                "kN9PGbp/if0Bd/97YvsS+v7Asde/qDWi8wGj2d++/L33Z7B+8O" +
                "KnuPpeiP5y8d/x/LOTv3V9d3C/aHh/ilU/dt3P8pyv7SgPvt/m" +
                "j4/8+qUp9pF7/jG0PqB9uytxrs/zfu7Ug997XHAXPv3+XZ//Xf" +
                "oxPO7+3cWBqxXjX1C4Nbjy2jdNs29h+xvpl0c/wf6F97dHWl/c" +
                "+uH9VD5/vPDx/I/dv7Hz38X3rvNRCO7v/xHqY8T6D/J/fv/gSm" +
                "9wfATu52L5HSuf/vbHt+SP158//qqi74uCvrtP/97LPyUnP8Lr" +
                "N8T8OaS+gPhnez90XH5/4T4/vK9IE9cfRuj/8M6XjXL+IT4/YP" +
                "NPLXNV2+fL0MZ346/8/DD8/CZ3f1GKke4Xp6urz7nPZ80W+cvw" +
                "fq0u79f2ViB99ssNX4zfq4/M7++656fBafMP7XcqxlkfG477d9" +
                "z1C8/9AGL8En//gB2/MeuPzUdkZflS/0tis+r+oRGD+8/R9RkF" +
                "4lcB4lNBiz9D63NR87fZmlv4S+c/iu/g/W1n/EOMn7j6T6ufBt" +
                "5/F3T/sOL+Drz/za6/cftP5xn/vg6GE+yH//3Q9Pp8/PmzGPnJ" +
                "zJqfJyD+SZz5D6qP1v7vcjW/dtYHElv/ICXYwsH7szsDW+5Hzq" +
                "jycdwPndHGx/k/Gt/RHwqdv53fd/iP+APg2nF/dEarH5MT6Oj6" +
                "kZ8+qF+5CHpMKD6KH3OLwmtJnx/DkxCPFTE+oT41XXzLjH9H6P" +
                "8fT/z+Dl7++D+evX0i");
            
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
            final int compressedBytes = 2883;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2PXEUS79e8XZ5Xd6K9wsa6qO9koQkcAAl3Tnjrw2CCE9" +
                "YaSxAgERI4OhEhB72rDdYXObjA4cgBEpn/hAFdsHLkBAntX8P7" +
                "mJ2Z96a7f9Vd3TN74BESsDX9VV1dH7/q6hGV8H7OP3/34fUn33" +
                "729s7RoxdvPn84++jxBwcn//rl6tMH57dPb3paGn/HVDqY3yq9" +
                "shDg/AP6//+mV3a2o/brH/X75E8m+uIj7SzM3X7b88d0I0op9P" +
                "yLlRE32n9Pmr9042x/f7uF6+X5mS9e/yHkl6s/zz+/2bSfVE37" +
                "Wy/eaNp/+Hi/Pvnsl6vm8Oy2ubng71C+LtP8mfbDen42QK+X/H" +
                "/nyeTL/7bz/+fzh//78PFhx/+nD35t+Y/oFvuhgH0Jo2P94j9/" +
                "ifSrXmuvafYPfQWuf9YOXLrGh/sjXs3b79rnT9a/eyP9Ox0t1c" +
                "5fPL92dinON/JfMtHJ/g9qL6nyETp/ov2M7p9o/7j85/of25YP" +
                "1/ngxh/14gTF2Sd/e7FyQuPOJ+gfzh/Yp7ea9f1468oXrf/w5v" +
                "Ov2/X1/sODl5u13zBslVHyy7RfCeLbY7v+mF7oDy9dVneEUqIQ" +
                "SsrOBD273ygGZVq7JpsmspI9vV1fS78xpBPG9+s3q31R4dsTvz" +
                "/++YfCHov+TUY6EgtG/5difX8gerj8KWug4PZfkX8RNr71OEqf" +
                "aF7t/lIWrc9Rt8PW4kAVRp3eSIAfJrcPl/38jP68L8SOWm5Do6" +
                "y/aJR6+6263xVufFtR1rLS3vR/q1LpZ3b/vPak+Mg7fg3xFX//" +
                "zPb4/LPwuzz5BUVulgffURF0+wGiyw9afNz8gsc3aed//pYLP3" +
                "xw5sYXVIb9kbH2EXyKkoYvuD5c/UBYv5W/Jgl/Zn+++fHOUbO/" +
                "Qtx6cXB8bbY7ORSmuqvahRH0V3b9COJPHJ8y5w/6b/lXHk1udP" +
                "wrOv7tD/gH4yce/9j4n8jLH7g/a/2XQeP/ZU9e26/uFWLnqIlv" +
                "G//0H9+9d3By8LQwh/+5XWE6ko/GPn19/cdHg/kf9Pw9t88/cX" +
                "u2/HcMXdViSgbBJyHy+UMnf3fm8vc9RX+g+YHzA8cn+79zBjjD" +
                "ljj79HOvH6z6VRqHf1V49oeEfzLah/cvNzq/GPua0T+Zre6vRf" +
                "/b5DM+PvghQ/zi10+Ynjn+WeAPa4s59Z0WHMkbGv4C6aRARy7T" +
                "maUaWbtiPNLcuzODv6rI/ACYH47/oPxkih9z4quM8TeCrwbMD+" +
                "J/gfK7fn8gEn8g4o9go7j4WJx8huQvcvvvXP0UQw/BZzLHh8z4" +
                "iY8P1ISzToG9c+Gj/UevyadOhr/UnZHV6012KfxB/RsvfW9PNP" +
                "Fbw42do+bAN/Hb+9+p+uRuG7+d3t4EvjRYn17/LszP+vOvMD+b" +
                "zD/Z8P231/RE9Jj9peCHtPOH6eKStwd0fRFEaFOa9txJUSlddV" +
                "759VV8KPJ+We75c/uvZrz1If5M+oaFmPT8NVIN+Ivz397+I/Ib" +
                "YfoT+gevkthvF/2v6lfZnN/yb41xKP7e/L8xjai2DJxqcUBp/6" +
                "xrr8WubNvrcXuwPtL4JSV+kY7+z4b9i75/Me9/23TkP8L24l7H" +
                "nzZ0absoOr508ZteyN+ALgZ03D/BJy3j80N0/kkXvRvfRRfizM" +
                "sf9v4B/hPmzxy/Zo2f5Hx69TdyTQLjn/X4i3g/KTI+iq5vIuIj" +
                "lIE8+TXk39PrP1yf+2F0Q5l/iP+BxqfaP0QMtW+6k38+f3PHlz" +
                "z8noSPlDnxoxmYP6Jf4N9FP5geA5zE+alYfD7G/wu4f5Fl/+l0" +
                "7v2T3PUBcfcP1MboXPyWi+/WbX7xJ101R/iWLsy12W613/z5bv" +
                "eNJPgwNf+Q8X4/535TYnoRis9UFyI+ik/L8QqlK3/T/lOu1SeW" +
                "piho+n/efm+lvVppD8YH9Bb/f+fJt1918vtpI78fPf63JX/i6T" +
                "/z+cX4jd8+QnzCFuEqqweVke7333ztcf1OV7+60NNmK+vbJv+G" +
                "619vj+jD4zg/vzrT/IP7p8aPqvefg+NHa/5rEJ8ZUF8G9ofbvw" +
                "2fTPh+Rq+fovkP23sQNtr8a8DQBP5D6cP35vGZ6vGJPj4Ti/iM" +
                "n/9QF/+y39+D+V/AH6g/wfjW+CvA/x29D3ClG18R6jOnc/yOcL" +
                "7Lsf8z8E949y8xPsS7X+Kiy5X2q/pyrj+rrv1gu73yI9bGp8k/" +
                "bG99/2F9f8PGp+8fYX+Y+JpwvK8xTbP/8P5uqvXZ3+eIu5+5/L" +
                "zs62vuud7nebmsv3lko2N8y6o/ZTJ8KV39vp2e+/5Pdnxh2/hQ" +
                "2EYUpE1094/aF8nbc9cf2N4Es4Q5/rblKwG+w6KbLePvr+lbpe" +
                "tWAorpleNvmv/4yihRnEzVtPpkIkqT5P5hLD75en8uh30LoesI" +
                "+UDyldl/qKiGyKWf/e/b8eke3m7kfM787++R+3fNH6wf3w8bzm" +
                "+BX+mM8hfCX7NlOnN+uuPAtBDftOhIy1RjlK7qCVX+Esz/Ut+v" +
                "3Xb/XHqsfhzgn2U2/Z37feXs9ocafzHut3vOB65/oOhvy3rq8o" +
                "L/rPf1MH9qC/474n8ZGr8Fh+2lCPooQX7fnylf1YBlevXrhrTQ" +
                "3O+nM+nB9TtkfGu61A9J7mdG3p/k3//E+lH48Pma27/lfKzeb+" +
                "C+nyKY9+uA/GU+n+z9p7wPSjpfDqK1f0XvP4t9FQH6nbc/hPfR" +
                "iPlvtH97bv567Dd6P4p9fzD7+0P+803SD7Vvfo73057O8zdgfv" +
                "D9Iub7SPB9D+b7WoT3eZjvEwXm/9z0gPd/AuYH6+MT+NcZ54fz" +
                "m37+8t+PofiHevmf5cj4Y/+wFMv3WeT4fZa4/Obv6v3jRPyJe5" +
                "+W8H4Heh8IyU/c+8nwfkVk/Chi48dYfDhVfOSK6nj+TzlkWb1C" +
                "NRQXklv/I4X8k7qYQx/fvdePXc/rt/K+n5S7fgPjlwn657QP8t" +
                "81lnVD9M/MIel9XS7/tk3H9XNE+VbgfGeiJ3lfl0Wnxteu9xVe" +
                "IXwm7v7jwv68ss6fiv/A+mJYPx7bfipG9edytT7adX81Cn8i4B" +
                "dyYL9VBH/2HOtLUL/tWWiS+tdy7L+4/cc097sC8Mut47v++mo2" +
                "/xv/tPLKJ7P+OrT/dfxacvCzdP6JU//yfj8hNH4ygfmXS0kX5P" +
                "cZE8RvoP4bOXJUfD2uPde/5/q/OD7gxg/U+dv509UnHw3qk5uF" +
                "qkV9sp2+rF8m4NvHXnwa+0fHvPqS2pE/GlvIuPpaVL/Lr7/10+" +
                "nvo+b6fRym/abgN6XfP29NmK6X7duv665+yOD6QQK+5e0fKVI4" +
                "v7X2yu/BueRXOOJDVD92fyhfwf4ZFX+nXpBI4f+F5P/sEkbHR/" +
                "36hf37BqP8SrmSX6lT2Df275vE6J/A/AfpfYlQfJdsf7z1iWh/" +
                "SfVljPrNMf+KUP/ZOb4Pvw7Bn2n+Ca4v9OS3afrJFf8kwRc8Au" +
                "yvn1zQdwHd2Z5ZP8qs70TxHf79Fsf96ildvljyabdfAfEnL4G2" +
                "Cf6w/Lsc8X8QvpEo/pcE+1oKq9Hz3i9h29+A/EgMPkONz0Lwk4" +
                "D4PQB/qCwEkn9RMvjbxQd6+VXP/YMqjr+s+3V8fMGZHzuj5cds" +
                "+TmRcH4x5ydHfTdiRK7fpwX1RTg/5G2P5od/PzKvfKL6jiTxBS" +
                "s/nkp+kX225/dkdafPXygpuy1+tl5foX1WFeU/Tv36Ka6+IyQ/" +
                "BON3OWeJjDrf+PeVrP3T7w/y9E/87xsZov/AjL+Z/kme30cPed" +
                "/Zn3/c/v2XvPnRgPuZMsr/ZL/P4h+ei2/z85O8/FC698Mi389h" +
                "22/EP/D72xT59fCfdP+/Rv69+8O9n4/wWXh//DeK1mRe");
            
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
            final int rows = 786;
            final int cols = 8;
            final int compressedBytes = 1904;
            final int uncompressedBytes = 25153;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXD1vHkUQnjvOZomE2FgxWFRnZCEXLhBNwE3OQIRSICwHS/" +
                "APUlAhKuRibaVIqFykoLRSUPsnWChFSjeRIv8a7uMlfu98u8/s" +
                "zJ7fV7xNlEz2a3Y+n5k9Iku9n+v/lQyB37vxuWx8FaT+8+HWd8" +
                "XxtrlHtHO+d7J+sbp9QM48rFetcreVYLzv5zzHRGzgnM/y6YB/" +
                "BVE2nMl2u3fhkY7F//H17c3xiD++8YaifhnFyldfPEfuJ59tKR" +
                "fej6Mip3K2EeNoo/lzu/6X7v+Nz2/Y48P7XyNamdPAnOxPzUqu" +
                "2XfuvXZLYfFYHnrRv/JqjvuOcz9XP35++PHzJ4/urRz/ev7+y8" +
                "OLB0df7j394c3d08dXu8+w/Rjnv2WbjauPtur1a/uzcrxz/l69" +
                "/v2jterpozd33cHb3TH7E2s/Mf1kXL7OWPKFxm/av/L6koqSVv" +
                "PsK6LSuYxKWxoqS9pjKKBX/kP7sxH3cxmmi+yHjTEwrRAH1led" +
                "f9O+bvn/Ga1Sw/9N6vhPdMbjv5b+bv93Jtn/pn3bp7vB+cD98f" +
                "mTA8ee9+yn8ejHB+357fX5Rfpn/evf9B9AkFH8sR+2Pxz5LyLs" +
                "o5co0+/Gvn/y/MkvLxr7/v3Lw1cPjn7r2XfkX7H/np1PGL+px3" +
                "v4URWDkCjrJitZDtZPdonjV2X8h/03ko+tWj62f27l49taPu4f" +
                "HbT+9/TxzP+G/f9Flz9svGjyh+xk/dXq9tp/+QON5w9WwnZMZw" +
                "bYzjPO9vMzE62/PHrGjuTdYH10EO3+0swvOl97wnxU/rF8svZX" +
                "AFnBdKOk06Lo+33+utj4rvLcD9v/Av8RHv/pnXx9zZiMVo6JVs" +
                "uKvv79i72ne6eZO/hz18Tsn8D+x/W/sW8rHnyEvPLn+deq7xBa" +
                "mqnChkzkf2PiB4hv4Py7GOq/P/7Jgv7n71a/v5np9x8tf8P3Mz" +
                "a+Zx9G42/r3b88//EFyMi/gPtTx2cM/xWk24bQ5K+D+P4s6EsM" +
                "N77A51PyH+kHuB9svxB/gnSWf6vk+PD4/c7nfzN8wnb5X4dP0A" +
                "g+kQvXr0D8qvPvaDx/fvLYL0n8jvBjyxff1PlF8vxlDN/W4os2" +
                "/f6E+GAjPx588nUKfBLyB+OXSnzwctQ+0SxRxvb3oj+eBuMx/8" +
                "P4D6YL/B/6TYjfu6FyKu0DGz+Trp+4fuCm4v+4fPPrPyi/QWot" +
                "xEfh/IA+MT6n9a8sfKnQxAfIvujiE4hvmXb3Zi7Usvl4uufT3/" +
                "B4YX8BhL2Z41F/AOwfaCcqr+csBlKsrd91M+azJeozFAz8PML+" +
                "KNfH/QdVFHw5YkjD5xf1l0Tg0wM4I338xl6fG//aOP+pnD9h/w" +
                "qN1vclQHoEvqrXT6b+xdAj8G1R/wiuj/Pt6yXw/+H1ffVdc13f" +
                "1eNDwfVl9WeTqP4M52f3p5C/PlGM5f+OOd5q8xuB/kTWd3j1Z6" +
                "WCTuZfg/Pz658yfCYWP4q2P/H4Z1x/gFf+uPZvoCEuFh+vZuOB" +
                "fbNdf9RN/bZM+SaQvwXrH4UYP/Lyz91OfWOg3xkzf433j8L+LS" +
                "2+LqYDfCjvxmvzU/b5xfHTtPULKJ8T859xPlBfYeXvXX6atfnp" +
                "oD+lUuqHLr/H9WU+fm1GCBhfroL5Odu/Wl9892zW/1p09SXq6k" +
                "vU1pcsxAe0+LisvpQyf5bguxifN8nweTD/zYjXRsfXKD4M4PO5" +
                "ycnaZtqa3mxxY7/emHXNuXLTw0/C+IOXjvYfHg/xZeX7HkzX+h" +
                "fteFBfifI/6fsP9fgtwFchPQLSmsS+JMLXpO+XtPikUn8Y+qnq" +
                "P8H1pWntz+3l3whflMIH8/dXMu83j8j/AH6H8DkQ3yJ8ivW+og" +
                "D6GaBr15fji2c8+43ffyjxOyu8v1h8VFp/0OH/AnzLRvnnSP3N" +
                "IvFNiO8A/uP+2WT3K+sfXDB+gfPnatL+YAZ+gHxRRH/kMuITi+" +
                "UvJ/6dsn9dho9TMvsZYx+NOj5JFX+nrG+g/F2Z/8P+wioo3yh/" +
                "Z+f3cv7rvl8A9BfF/+r6Oo5vUowvgvFNIc+/E7zP56APof4fHV" +
                "2dHzHxHSs9X6V/f3sr+iOMn6Py/xL83/KGLqu/PwHocH5t/ALs" +
                "E8w/lfGrfH7e/tX5H2d+Rf49zfuGOWprX0tTb2GnzFwdv5na5d" +
                "DDdoZx+xr//QDV+zBdfoTr48sdf09f32fUP//X5wf+U1m/jMZX" +
                "HMd/9/uHwu+HUHwA4vfE9jXN+5AY+4zoU9c/kHxNHH+m7s8e3B" +
                "+7PuHzbxPHR5Ceov9Y9f0OVD/Q+k8Uv03b/4vtIwvfKQLxOwd/" +
                "LfzxlS4+WLj/UuLPy74/iM+nwO9Y8c24KeC/3xX232j7Q6bGLy" +
                "C+wMYnpul/WfT7ElF/X9T7EJZ+5b38Nv79im98uvp0yvwxqn60" +
                "3PW7ideXfd8tJv7S1g9gfKrFV3n2LQq/T2g/1P17hOvzCroa/9" +
                "N/35lzP+H3F8uML93G+gH+wf5q7fc5Jqaj/BLnnzz99/pXDl2h" +
                "f4vPX3TzM+xj0D5r71d//yj/SMDf0Pr/Ak3R8rQ=");
            
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
            final int compressedBytes = 4991;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWw2UVdV1fiiuoYAgLYk/2GBMJEaCS5JIaMMqx/dmyJQuwc" +
                "ZWTQQhmFaRhciqWKqSzL3n3jfMe0MlXUtkuigUdPqbajWNbawm" +
                "NkTzp6K0qZlhNEGRsUFDoSApsdB9zj777r3Pu4Nw1zn77L9vf+" +
                "fM9d7z7r12HOg4YP+q44A7Gkuz+cnK5GHUwL67a2LHgfV3VSpk" +
                "6flyxwHWkkc6DpgzIe8zHQfS6R0Hcu9JFzWPQN8D7RMUbbfZWw" +
                "NmfzqqUqnPZJw8c33XNek8Z0kr+I9qYJzUnVapXHWg6yVt035A" +
                "nEq53DuZvsXREjv7N9+fqFS6d1Yq9lH0ZCc1aswDZrTDPkg4lY" +
                "rpNJ32qOtBHof2bXsSNRjvTt4xndl99g2yNFLQzyEt+Tr0o01n" +
                "fTbqOaDZZ01n7+chapLpBFbftIc90pPZVwLmzkZb1mZvIwyI3B" +
                "jkMfsL+/T6ifb/7LvZePbb70PbxTpoR2A930wOSVs2WvifwXmB" +
                "1TF6yo9G2e9433N2uYg8Bu1bOO7GuBOmc/1s+8PsSvBAhaxYjW" +
                "ycrOez37Hfg/4H9nkLWelsN2OTmhR7lPbbpDk9/VOT5s87C1qb" +
                "ozg6HGeYtPdzaM1d/rNorw96tG9itH2yZyra7U73V20fywiwnq" +
                "EarP/Thk7QogqsZwrrKWo6X88FyeGYe5HxDOmew1PIH9bT+Z5z" +
                "tdWMv4Xj9Zd7NicAex+s51z0wnqmsoKsA5jfgwbrSSsEbYVZke" +
                "1xPcg3oL2Y7UcNxm/a882K+tHsUGF5zqzAHK/tMiueBi3bHfRh" +
                "aG9D+6FZUb0Z5MvZwVDhB437Qsy/VyrZj+sfJwzAvyp4BrOh7K" +
                "fZq9l/ZS9lP2J/9jNo/806aP+R7e2pJf+jbP8pxgPZazhy1ZEB" +
                "VN3nfW85ZnQ4X/YCjykDoqsym/3y8H/1FdmB7OcUCW2H2YE9Sh" +
                "+zg/TkqNmx/pdo5wjyk8UOs4e82S0SrVJpbER7Ps7Z6l9hhO6J" +
                "hITRxfm5Q7KKa/ZcnRyPubdmMAdmkv1hPGOJQLHZgriyXitmnL" +
                "flvyJmf7Y5G3snw0zOxsPutjeD7GeLG2lNWgnNyeoybTP9GJuP" +
                "8/ejvxE4/Vy9lUNckyJ7tlCe5sJInMu9k8SsBbtfrUF/62w1C7" +
                "KE9aToXtObT3c9yI9COy+/CDUYX2yvMr09o10URvKIDqflF7An" +
                "vwTt1aVeuzRUeH/zzwLmFPiLTr3qU4zQfRkh5ZchSn6+ZyoqtN" +
                "bseTGd48bWBNwPsD//oOYbUC/0vg9XlzKe53gujzkjWxZXZj9b" +
                "/Pn5oXwaRUJbZVZhj9JXX0W6XWhW9dwsLRzNmsy3C1B2j0Ysqt" +
                "B8mGL8f+8XEgZEPio9xfmxqrwmRVZfSudqm/bHucykPqUcm9fC" +
                "r2dX2Wz1gZjEF1vyR8mZ2TO8R3P/kjuTicmx5Nfg/r44eV/jGF" +
                "jeD7vNKd4Hu1H7JdAugfEfJx+x94I+nMCZlXwM2oyAcEX3XMJL" +
                "rkxm23XNR+Rqte/ynnlJZ/KZ7jeT305+B+3pTcla3n8mi5LF+a" +
                "chbqmPXpbcktyajErOcFrji+nCEDUhOQczksnQLrBfhtg1bv+Z" +
                "TIV1+Y7P/aisnXw8xP9GYDgnmZtU062SYZbzeviYBcQr/8uQfa" +
                "NctQTOuuSLsKbLzXLsUfq1Xk66zczyxnelhaNZ0/nYOl5GLKrA" +
                "MX5OzxMG4E+QnuL8XF5ekyIbW9Il2qb9cS4zkbUldrpVrkH2RN" +
                "ls9YGYxBdb+7T2adij9LOdRrqtt09rXoZ2jiC/tMjmju6vSjRG" +
                "Df+9T2WExqXSQ1KyKKtZfTFdFXNvzWAOzKT+gXjGOE63yozs6b" +
                "iyXitmzLxdM7nJsUfp1zon3f6dyZsfQztHkF9aZHPH+pckGqOG" +
                "6gKh8UnpISlZlNWs7krXxNxbM5gDM2lvmTGO4fwUGdl348p6rZ" +
                "gx8/ZtiVmCPUrvW0K6/UezpLlEWkyJpvOx9XwWsagCx/hf37MJ" +
                "wyxp/In0FPyWlNekyOZfpPdom/bHuczEPlKOzWvh1/NI2Wz1gZ" +
                "jEN7TNZjP2KL1vM+kdA2Zz4xW0cwT5pUU2dzQmSTRGDVf1AUZo" +
                "PCY9JCWLsprNHWl3zL01gzkwE1dbz1giUGx2PK6s14oZM2/fVp" +
                "qV9qjrQR43K+HvdxI10HdXJ5qVzS/ZN8ji4rNzpOZafbbWfdQk" +
                "JwHtMHqwEo6ytvojhGFWrr+VPPYXPgOfh6yMq/Dhnoc0n0wb0p" +
                "aNlhkUnxW52SiUsNOaKJCOMTb5A8d3wXtIVs7GyXo++x2KtifM" +
                "ynS2G7v9Eq5w1/fp/u/2S/Xz3H6penbyvuZfu/0StCn5Z6FvQr" +
                "sI2iV+lwD7pfz34v1S0ptc4eUG3i/Bvvd3+a9Z35lfTfuljp/w" +
                "fglY7BJ7FNgvJUtwv5RfJ/dLSaO5O92SX+ujwn4JRpOh+f1S13" +
                "PQ/H4pv0nul/Jr/Jxgv5TfAFbYL+ULaL8E48/ni6l2frnvfz8g" +
                "X4P7pXyh76/Pb+x6AfdLBVfaL/WZPjg/+7w8bvr8+dkX9N32Md" +
                "OXbofzsw8jXQ/nZx8fzgLnp5fU3JFNQg3OTx+VPkjx/vy8lBHS" +
                "beRx5ydU9ecn4WjUwOuI6au+kO4gr683WnNCe1bBijAaRTOof4" +
                "Sz4PwsxnA/KnKB70PgPSQrw/nZp5m48xP/wfnZl+IqdJgO7FH6" +
                "6h2k26+ZjubfSgtHs6bzZSM0Rg3394wwTEfjG9JTXI86ymtyZP" +
                "q4tml/nMtMZO2YPfPOf7NstvpATOIb2nazHXuU3red9I69ZOFI" +
                "9kuLbGiXaIwa7kd7GaHxhPSQlCzKa6Y7Y+6tGcyBmdQ/F89YIl" +
                "Aszlzi6rVixszbr5h747Aa/h4rkzEV8a4hPMF/lZ7qJ+fzm4Pk" +
                "ouRiek9AGdyKqE8otFdl9fpJfnvQOIjvOzjTX4+uS27w3G6Xby" +
                "v4PQOs548gamyynCzJrwLLcwu+v47vOzSqH4c5JbOYLaMW18+r" +
                "kvni6nhNwevaIFcElJv0Cpi7zF3Yo/RrfRfp9nGycCT7yWKH2c" +
                "NYEo1RsXptGyM0jksPScmirCas58sx99YM5sBM8lviGUsEis1v" +
                "jSvrtWLGzNu3qqlij9L7qqTbb8QWjmZN58tGuYwRrp+bCMNUm8" +
                "ukp+BXLa/Jkenb2qb9cS4zkbVj9sw7X1E2W30gJvEN7X5zP/Yo" +
                "ve9+PGoTahPIwpHslxbZ0C7RGDWcnxMYoXmz9JDkKiPVtGfG3F" +
                "szmAMzwTm1YuuMfE1cWa8VM2bevnWbbuxRel836fZfycKR7JcW" +
                "2dAu0RgVq3efxQjNNdJDUrIor2lHx9xbM5gDM7FPxDOWCBSbN+" +
                "PKeq2YMfN2Tb67rkTvvu3u9j/nu1Dru3B9H4rvR/rupmt0W8Zp" +
                "virvRzpv5PfvsC5tmnvsH+n9O86pFVveX2E9t7XONmai2Yb70W" +
                "QzGXuUfq0nk27PJQtHSv+pWvGXmyxRw/OQtwnDTO69Vnp0XmtN" +
                "jrTnxdy1X8+ILflb8YwlAsXmb5fNtqwOVQttkVmEPUrvW0R6e1" +
                "9s4WjWdL5slMsYYT1/ThhmUe/10lPwW1RekyPtxdqm/XEuM3Fz" +
                "KsPmtfA7uvFls9UHYhLf0NpMG/ZOBl8b6fZDaGULR8s86cH4Mh" +
                "vXqC/k7N7V7GnlENdkDPthbdN+mcu9k/UFrcwZodghP9Y6W81C" +
                "My6iV5vV2KP0vtV41NpqbdqCEbGm82WjXMYI9/c2wjCre9dIT7" +
                "Geq8trcqQdpW3aH+cyE1k7Zs+8Oz5VNlt9ICbxxZb+Q/qwvGsA" +
                "zxl8HU8X8wzT2+QVOv1q+b0H4uCst9cVTwoOJ4+3/gqpX9f6iw" +
                "Tfx2msgHijrhKQL49/n/EdBu9HGqW4Hy2nWLuy/M7px7PBf6js" +
                "Plt2NxLnwY/5QI1tpJdFlVmkp8xGlpHwuF5rVpwB6/lbI7HCcd" +
                "dUipRVW2trpmiBHcjV2jvyTIhvaOPh6Pf9ePcliP8yYjzpftzP" +
                "Fh+vtZDPHo/RYqOsUKFfZ3NM8DIHH1fEFjx1jGRVMCiYIQ+0FV" +
                "U08/GSfbEK/apyf8QiWCg68NxgNmCP0ns3kN6+2WywN6KdI8gv" +
                "LbKhXaIxari/v8UI6TbpISlZlNe0i2LurRnMgZm4OekZBx5bXd" +
                "+dYaxdHFfWa8WMmbdvt5vba9NdjxKu10Fzemxx8VqTFoxmm7/z" +
                "TMdIrEQ2xsBolLXp5I39FEN1XJRdqm0yg+KJEeZQFZ3F9bkaRk" +
                "mvrsA8ODrwfAiOQd97aV8rNKfDGGKwfyj07BcWJX2uG2Guk2T3" +
                "o0GJwR7M8d7BAuehgCNq+uxB+wcyO0bkmYXag8UMBnWWqF/kBt" +
                "6DqvKgqhfmGM7OQcr0zxj/t36Ge3/UOyF+/mnfhDv5drqv5Sfo" +
                "+ae8b+Yn7bC+D8pfZ8VX1g/m74rnnzfwHT/dJn9vyvdHzt47EX" +
                "Hqo+IvwO3t8V2ZGeD7o/Lnn+23xTuTwEN9b2PV91T0/DP/ZfGe" +
                "64URfm/ebe7GHqVf7btJr1W55wjyk4X8HEc4hMaoYf9Z1QjsQV" +
                "mrShblNe19MjtGlHw1E81SjzmDorgyVpBM1PWToh8wD2CP0vse" +
                "IL02z/X2DYrBCPKTBaNYol2iMWpYz3kagT0oa/Mki/KaHa/I7B" +
                "hR8tVMNEs95gyK4spYQTJR60nRNVOr7nM9ykqFNKfHFhevNWnB" +
                "aLb57wr3YSRWIhtjYDTK6j7yxn6KoTouyv69tskMiidGmENVdB" +
                "bX52oYJb26AvPgaBzbT7a+QeHrkJ0V2/STu/B/jAyrXzPu99H1" +
                "4Rp3JbQrSn4f3THS74vWp3wj/j76p7LnaMxkpN9H9VWt1/jW30" +
                "fdz5Y/h4yf1sW8zBY49vjeS/uzQnM6jCEG+y2hZ7+wKOlz3Qhz" +
                "nSS7H+2RGOzBHO/dU+BsCTiips/eY78us2NEnlmovaeYwR6dJe" +
                "oXuYH3HlV5j6oX5hhWcU9RtQuOId97CRbSnF5YMNL37BcWJX2u" +
                "GxGar9lVjIYkBnswx3uHCpyugCNq+uwh+88yO0bkmYXaQ8UMhn" +
                "SWqF/kBt5DqvKQqhfmGNZzqKi6zCyrHnU9SrgWBM3pscXFa01a" +
                "MJpt/spyFCOxEtkYA6NRVo+SN/ZTDNVxUfZftE1mUDwxwhyqor" +
                "O4PlfDKOnVFZgHRwee68w67FF6LuvwqI2pjTHr7BDaOYL80iIb" +
                "2iUao4b7+xiNwB6SXGWkmvaVmHtrBnNgJq62njGO4femyKg/Fl" +
                "fWa8WMmbdvc83c6kHXo4S1DprT3dj+hC0uXmvSgvls83+5gxiJ" +
                "lcjGGBiNsnqQvLGfYqiOi7I/1TaZQfHECHOois6iWFjPohpGSa" +
                "+uwDw4OvBca9ZWX3M9SvAFzelswUjXs19apMRcNyI0P8O1NEJc" +
                "iYASc5wXR+xnjXhWX4P1XCvraUSeGdYmRDkbzZhniBy1lypIJg" +
                "4T/1Ek7gBqc3AH4CT89zCHdgS1OWw51ftN8qPUuwh7uOz9JiOS" +
                "dL83a3PQ545Tvd903tqcbKF8r8ms5ftNYoQ56JO1W3kQb+eRz5" +
                "MJQTJBvnI+sKabzCZ71PUgj5tN/nvFTUHfXTPOgr2zuB5HdDiN" +
                "/BznJMZSnrQjokRA6b9X3OS+V6wZrtJa0x5xteD8FNkxItqzgo" +
                "P/XnGTng0gHZP1OZejuDJWkEzU94oUfQccw773Mjuz0JwOY4gp" +
                "LD5eacoyrG1+JsMYGSqRjWvcwR4zXHgjP8VQHReVpdomM4r44S" +
                "J3GKWcjfLfUfiZ97CqPKzqFZgUHXhuNBure12PEq4FQXM6WzDS" +
                "9eyXFikx140IzdfcSCPElQgoMcd5ccR+1ohndW9mZXaMyDPD2o" +
                "QoZ6MZ8wyRo/ZSBcnEYYbr517KPNX3IXDWd57O9yEYRfJ0vg+h" +
                "DH395H+1zvf6PqTWmT0luWejT/f7kLj2CNfPzrgycnrP70PuNf" +
                "fWZrgeJSAFzelswUjXs19apMRcNyI0f47cS6OsTWKwB3NcVRyx" +
                "nzXiWZuRW5kdI/LMsDYhytloxjxDv54ztJcqSCa1GcXqzyiqjj" +
                "VjazNd76T3Bc3pbiwtLl5rlM8ez7/URiNEZAtJqoZ1ZRWKYZ61" +
                "mflGbZMZmhnyQJuu3cqjWKGZ2ssImgdmUCS0e+AY8L2X2ZhCcz" +
                "qMIQb7e0LPfmFR0ue6EeY6SXY/GpAY7MEc7x0ocO4JOKKmzx7I" +
                "H5XZMSLPLNQeKGYwoLNE/SI38B5QlQdUvTDH8OtooKh6Fhz7fQ" +
                "8S/lscW2jODmOIKSw+Xmshnz0ev9xGo/2F9ywlqdp+wcH7i0oF" +
                "T7M/nattMkMzQx5oK6po5gWPYoX2ay8jRDxoRsTzTnNn9XXXo4" +
                "R7VdCc7sb1XrQ7i+vZLy1SYq4bEZqveSeNEFcioMQc58UR+1kj" +
                "ntXX6+/K7BiRZ4a1CREZySwaw+/NIhejpJcqSCYOM9zfX+fMUz" +
                "1Prs2HM3b5yM+T7af9fT18uV+bT8+Ts6/FT1q7L1T39/niye0F" +
                "rU+Ts/Hv9Ty5Nr97yuk+T5Y7DJxT2fPkdKu6v8+Pnydn407ref" +
                "IXzBdqs1yPEpCC5vTY4uKzc6Tmms4PUZOcpFyqhCOJiNEoa7PQ" +
                "h3XjKny4uNqs7pnaJjMonhhhDlXRWVyfq2GU9OoKzIOjw/j/AQ" +
                "lLBdc=");
            
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
            final int compressedBytes = 3855;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtXF2sXFUVnhpJ+iMmLQXaArZUpMYYW8CgidLsmXsn2Ba0yI" +
                "+V6qVJ27S2jQ83wLWBtMzMuTNnptVb2spPgVuwIVGJRKoRqMSQ" +
                "KL5YBUFMlAfKA0JyebAGHgwGPXuvvc631jr73iJ6TtZeZ6+fb3" +
                "1rcebv3IbOWK3mHq0VR2s2afdoc6pWo6vWO3719tbiWm38q7Va" +
                "c6qwLmstr5XH+A2UFzDertXa34hRVxBGGXcdX3nE8Wt5170Idv" +
                "YW2Te1NgTEW5pT7Y3NqfGbUJEYFhjXt+a2/lZyXVBEn18yueiu" +
                "pSFmRGKPry+vNrSuDP4vA3X85vFv8i6/sLW2sNwY0WLe+Fda1x" +
                "fr18Y3tnaSpTWiu3RXu6tpJc022g8N8yoj7I79FC0FFVDDH0CU" +
                "1XFYP2py5NBw73PSpjN0R9I2PTZmwQx0ZZkn63CXUR5wDzRe9y" +
                "vpWo13fu+vs3PI7i1+hV9apKZcf8VooeIDfEW4EoE05XgvXcGP" +
                "HfNsvJ4tlNkWEZ1RbUYkRjKLr9uTyKUo6eUKkonHpIMjC8lcRi" +
                "vpUD3jfWMPWxAJv7RIIbtEAyrVr/9AI8DDWrJI1+yustyrGeAA" +
                "JtRTFVtn5EttZT0rMAbvIMfcMVpJB98x3mdL2IJI+KVFCtklGl" +
                "Bj9Ss0gvAcKxmaCrZm7/OWezUDHMAkW2w7lggc233PVtazAmPw" +
                "DrLVbaWVdPBt5X3zTbe1+aa0IBq7zhu0r98JG2NxBdTwR+8wY8" +
                "jqit/WdE2OrN/puVVRbDX2gIlnmcLGLML9+alUt/okTOYbZcyN" +
                "0Uo6+MZ4ny11Y9nFZEcE+9lSzDPo+h0SS6IBNc7zhEaAh7Vkka" +
                "pZvyNbbrlXM8ABTDRLi8Cx9Y/YynpWYAzeQfa6vbSSDr69vG++" +
                "whZEwi8tUsgu0YAa5/mCRoCHtWSRqtkd89w092oGOIBJ93bbsU" +
                "Tg2HylraxnBcbgHWSP20Mr6eDbw/vsErYgEn62FPdn0PXdEkui" +
                "ATV+u6tpBHhYSxapmvXd2Scs92oGOICJZmkROLZ+tq2sZwXG4B" +
                "1ktptNq9fRN5v32QqywoJoXBXzFB6KlyIx4jxVVXiqHGxNYPS+" +
                "oG3aL3Oxhp4u1bU1D67fO6farWahGZfRa91aWkkH31o6h1YPrd" +
                "YWirA7nS+Fc4ERv6+vZgyZrf57r03XRGT7QW3TfpsLJra2jAXv" +
                "/N5Ut/okTOYbZYvbQivp4NtCZ+fF4ae1hSLsTudL4VxgkM3jSh" +
                "R4ynluSddEZO8qbdN+mwsmtraMBe/8sVS3+iRM5hvlsDtMK+ng" +
                "O0zn0FlDZ7EFkfBLixSySzSgxvvzLI0AD2tUma5m76eWezUDHM" +
                "CEeqpi64z+IltZzwqMwTtI13VpJR18Xd4Pn2ALIuGXFilkl2hA" +
                "jffnCY0AD2vJIl2z94zlXs0ABzChnqrYOqN/ga2sZwXG4B3kkD" +
                "tEK+ngO8T74afYgkj4pUUK2SUaUOM8n9II8LCWLNI1e7+y3KsZ" +
                "4AAm1FMVW2f0l9nKelZgDN4BfcXwClpJh25X8D77DFsQCb+0eO" +
                "neIbEkGlDj5/t8jQAPa8kiXbP3O8u9mgEOYNK93XYsETi2f5Ot" +
                "rGcFxuDtxU26SVpJh1lP8j5byRZEwi8tUsgu0YAan8f8WiPAw1" +
                "qySNfsnbTcqxngACa+tu5YInBsf7+trGcFxuAdJHc5raSDL+d9" +
                "tootiIRfWqSQXaIBNT5D7mkEeFhLFumanYWWezUDHMCk27UdSw" +
                "SO7d9nK+tZgTF4Bxl2w7SSDr5h3meXueHufmlBNHY6XwqjATXe" +
                "n88xhsxW/IbTNRHZ/a62ab/NBRNbm2O7fTmD/pFUt/okTOYbZY" +
                "1bQyvp4FvD+8YnrQXR2Ol8KZwLjPj88xHGkNlqnmvSNRGZXa5t" +
                "2m9zwcT3lMLGLMI8J1Pd6pMwmW+UHW4HraSDbwfvmy9bC6Kx0/" +
                "lSOBcYcZ73MobMVvPcka6JSM+timKrsQdMZJ5lD979N1Pd6pMw" +
                "mW+UzW4zraSDbzPvs89aC6Kx0/lSOBcY8fP9PMaQ2Wqem9M1Ed" +
                "l8W9u03+aCSXZFGhuzCO8mx1Pd6pMwmW+UgRvQSjr4BrxvXOoG" +
                "2bfIjgj2S4sUsks0oMZ5fhEIvSXSw1qySNfsXWC5VzPAAUx8T7" +
                "pjum5PyozBIltZzwqMwTvIbrebVtLBt5v3zVNsQST80iKF7BIN" +
                "qPH1fp9GgIe1ZJGu2a5b7tUMcAAT6qmKrTMGQ7aynhUYg3eQvu" +
                "vTSjr4+rxvjrMFkfBLixSySzSgxnl+XyPAw1qySNcs5mm4VzPA" +
                "AUyopyq2zhhssJX1rMAYvIPMcXNo9Tr65vA+W01WWBAt86SH4l" +
                "M21Kjfr7NlnuZgawKjmOccHSX9Mhdr6OmqKnMgcP3B96rdahaa" +
                "cRndci1aSQdfi/eNc9mCyPbb7EeOFrJLNKDGeR7RCPCwliwQIy" +
                "3tdyz3agY4gAn1VMXWGYPDtrKeFRiDd5BNbhOtpINvE+8b51gL" +
                "orHT+VI4FxhxnvcwhsxW/DalayKyuD83VVFsNfaAie8phY1ZhH" +
                "k+n+pWn4TJfKOMuBFaSQffCO+zurUgGjudL6X4VfgP8iAmPAve" +
                "zxgyW81zJF0TkfkqbdN+m8u2oieXxsYswjz/4kY6p223+iRM5k" +
                "vSnPL/tsqv/K/AeOf3jfOaU/73JiyIpis3qznVeUN6CEEK21DD" +
                "/V5W9etd6zlPc7A1gdH9p7Zpf4G4lHOxck+yNl+3J+UM6u9Wu9" +
                "UsJDK6xdE8jl74Kru6qH+SvePvka+1DLGF9d+47lzG/75O4sW4" +
                "fwGbMGc6mseR395YTG+WjfAYMspfyZ3/93Wpo3GycyBWmLJ4OA" +
                "Z/lfxb62MP77LV6vJ1tcAtoJU022jfuJUtiJT+mURXQI1w/UfG" +
                "kNWrzKo1EZk/Y7lrv+4IFuqpiq0zBq+kuk3V4WokM7/eszXy9Z" +
                "t6vVdF3+Hp13u+If16V3foGV7v+bOau/VP93rPvpR+vYOpP/bN" +
                "q75npevo17vsNjHPa4v3lUf+H/NsH1PvnyfBqn30g82z8+0POM" +
                "9r3s/75z6X/gw40zzdhJuglXTodoL3jYvdRPj9PoFI+KVFCtkl" +
                "WnE3vQZ7YfsDEPJXlWeifAWZCrZmfspyr2aAA5j4nnTHdF38fh" +
                "cZ+9bZynpWYAzeQXa5XbSSDr5dvM82uV3536UF0djpfCmMBtRo" +
                "e4ExCvzT0lPy25WuicjOKW3TfpsLJtktaexinmIG+7alutUnYT" +
                "LfKMVBK+ngc7xvXO5c/0PSgmjsdL4URgNqtD3vyqM/S3rKebp0" +
                "TUQW83Q6SvptLpj4nlLYxTzFDPY9m+pWH4TJfKOsc+toJR1863" +
                "jfWGUtiMZO50vhXGBE28uMIbPVPNelayKyf7a2ab/NBRPfUwob" +
                "swjzPJ3qVp+EyXyjjLpRWkkH3yjvGwfcaHj/HJURdqfzSXxmrD" +
                "QqUeN3vQOMIbPVPEfTNRGZ7dA27be5YGJrc2xxf4oZ7P9wqlt9" +
                "EibzjTLPzaPV6+ibx/vGIbLCgmiZJz0UT5nahhrstdWrHGxNYP" +
                "Q/qm3aL3OxoqcqNpgyA9utZqEZl9HnunMbD/qVdFEv7vzeX9NK" +
                "kX6F3+/YIjVdx8k9SJGcRTZgAM/b2euvbBWZAQz2SMTYYdkR8w" +
                "ETmcXXxf1ZVqMo6eUKkgn3CMxCDrqDtJIO1Q/yPvsOWxAJv7RI" +
                "8We2W6IBNfb6kkYQnoPl/WEq2Jr9j1nu1QxwAJNszHYsEcDAVt" +
                "azAmMTvc1to5V08G3jfbbXWhCNnc6XwrnAiLYXGUNmK37b0jUR" +
                "2b9O27Tf5oJJtieNjVkwg2q3+uQoGS2+4T9e/vp/orwqfu37zy" +
                "N44/OQx9PPGsZ/Qs9Dsp9JvOrh/oTr/NXw7ed+8+voceT75yHV" +
                "Iz+lo/yV3E33PKQ7wRHZz6W9PWkZ4LdPa721Wl129rB7mFbSbK" +
                "N949Pu4f442RHBfmmRQnaJBtQ4i69rBHhYSxbpmsX3T8O9mgEO" +
                "YOJ70h3TdfH+KTJsPxIRFjlHyV0/vWs9WT5/u6Z4XfXC1bUyUv" +
                "921Ye/PxPP3/SzsQ6u+7Oqfm1J35+dU/Y5ge5j2ud1nRSjxP05" +
                "NePzxGm87og7QitpttE+a7kj4ffmEUTCLy1SyC7RgBpfcyc0Aj" +
                "ysJYt0zeL+NNyrGeAAJt2nbcd0XdyfIsP2IxFhkXOMcpu7jVbS" +
                "wXcb75t/Zgsi4ZcWKf7MbpBoQI3zfEkjwMNaskjX7D9iuVczwA" +
                "FMui/ajiUCx2Y32sp6VmAM3mFiMz+vy/7755/2yX/y+efN//vz" +
                "z/4TH/B53Z738/xzur/ZnPF53UPuIVpJh1k/xPusyxZEwi8tUv" +
                "zZXCTRgBqrL9II8LCWLNI1+8ct92oGOICJr607lggcy1Gah60j" +
                "7k+O3u6200o6+LbzPutZC6Kx0/kkzYWUyRUQE9guZAyZrfhtT9" +
                "dEZP81bdN+mwsmtraMxQyIv+1Wn4TJfHGdfzxfni9rTuUXx8/3" +
                "W8u/x+XF5+F98vOs9aPw/XNq5r+mtVY1l8wU01wirl9Nf36WFR" +
                "8r5IcG/8fES79L6b+PTvf5zrUtP+oTUdIvvn9OpXV5H9zt7qaV" +
                "NNto31ji7h5cQnZEsF9apJBdogE1/hfcqBHgYS1ZpGsWn++Gez" +
                "UDHMDE96Q7lghgYCvrWYGxiZ7v5tPqdfTN533jIjd/sEpaEC3z" +
                "pIcQUjbUyHcie7ASnioHWxMYxTzn6yjpl7lYuSfLHAhiQpVuNQ" +
                "vNuIzuuA6tpIOvw/vsSbYgEn62dN6AB1gSDahxnns1Ajyu/KYv" +
                "/dWatdrgesu9mgEOYJL9wnYsEcDAVtazAmMdHf6yt5P+f0H6vS" +
                "v8W4oLm1ODXL430e/31vL0v46Y6e/F8qifzg+c6ftSa4P8lpIf" +
                "qvx981Q+0ZoL/NaC4nf2+ahF35eqfHxPIf7KgHvY/rW8rL9Wvn" +
                "9af2vnNN+XjrqjtJIOsz7K++yXbEEk/NIixZ/NuRINqLH6XI0A" +
                "D2vJIl0ze8Zyr2aAA5j42rpjicCxHKV52Dri/ozRzbeab9HKmn" +
                "f+qv1c863BABbttxblWTxddPu3hW0x9oOVHMn1+Wq603v9/Tkd" +
                "K7pu/4YjNbqsDb/sPsQu1t503xzNkcXFfwDrManP");
            
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
            final int compressedBytes = 4099;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuQHFUVHt4vIe8HISEBSoxICBgMCYRN73Qmvnaza0I2kB" +
                "AegpHEkhIQ0R/EvbPT+5jdCkhAf4iJgvtPZNewBT9USkJR8Cdl" +
                "+UPUQssiVqVwK5WQqAWC4L339OnzuHdmVv3hdN3XOd855zt3b9" +
                "+e6e7aZE+yp1RK9iR5WyrhyPVrv0QJSKlHmLC4ozKTeyOvJf+p" +
                "zJQeSIMtZxGPWXsxUdxDC+JATFxsmTH3gFhESR46Dn0QbU43p9" +
                "n6DHOmORt15jwzzZxvZpVK5XYzx0vmmgvNRWah7V1syxJzqfmo" +
                "udz2PmbLUluusOVKW5aZqzz+altW2HKtLZ8yq7zsRpOYNohQPt" +
                "VUrGS9+Yz5tG0/az6fR95RcOgxN5tbzXZzu+3fYcud5ovmy+Ye" +
                "c4o5FTxk68055lzzEXOBmW5mWMRMM9vMMwvMfG+/KFtnFtv2Ej" +
                "/6uK+Xm2t8Tp+0/evMSrPatjfYssasNWXbpmYdzZD5nC0dtnSa" +
                "LtNtNnjZF8wmc5PZbLaYrWabl9yWo++y5W47pzuSHVBD6+d6B4" +
                "5rB7WE0DSS9rygLfkA2eCz6INbi7/3jnhMQtZ/ImVSr22JSe2l" +
                "uG+aC2QQZisPRAn09mQ71NB63XYc117TEkLTqO+IHFNBW/KRz+" +
                "fr6INHF/O5PR6TkPVnpEzqtS0xqb0a901zgQzCbOWBKIFeb49R" +
                "X/vWSnBkx7WTXlpIPF6MuATHKPNxRkEz+DvEeBnFWJ9z8JELrd" +
                "IjBnmRZ5JxiwI/WtiOQmvn84S04liMlnsYFZFlvMIncsl5DifD" +
                "UEPrtcM4rnwNJYQkPZfwAnLujbzm6/P30gNpsOUs4jHrv9bcQw" +
                "viQEwgp9C3tND5cD1J2PmE6PakHWpova4dx5UjWkJoGkl7KJWL" +
                "wBIjEMZfPS9CH9xa8GuPx2Q+jkiZ1GtbYqJjcyzNAfDX2coDfC" +
                "JfKLWflkrZfbZ8NdlkJZv8teodzKr29yK/TdSaJTiqjZWin9oB" +
                "sqj9LNSvGxXoCVvG1XxuAvvsXqt7NhrjH4RCZnxEfR279gwgas" +
                "/F9DxT7JluHSFEBX42Q7Hz9T7KKm9wbfYBtHY+N5Nd9mHfkUb+" +
                "+Cd7n/qDfyu1+CSbpX3/KRpR/61EEf+YPX0wp0b6fA4+5L7y+d" +
                "ycvYdS3eKnek6pNPRjW55Oeqy2x8vOKLTFd1LQQGvnsyfkUT23" +
                "6J3OLWKf/j+2nM8eaT80Gszn64CqnknMyEbbs9hvyIzCyFqf9O" +
                "Tz2RNqG2dZmbTajX4+ysU59S7XevuNbj5x1OzTDDN01uCjrayl" +
                "/eDe4O/35OAjHOV6fAS5RPaJdzi/wceD+dyo+Scb8/ncSFLdCq" +
                "6XDV46uKQyMXgJ6KunFayMHf3I9ybI3s7nxBTmU2CqT4v5vIxp" +
                "9setW8WoPiVRrsdHvYsb8DIxftbfvsb8YT7FPEzEvfjs/Pne3l" +
                "8qtfe33+vqYs8rdi2UOb1ZQojGn2aY1vaWSX/z873aLlHAn9nf" +
                "2zx2cw4yPs2nttZe6IypjEuZK7X3KpN927xkHHD5+hwHBF/1/H" +
                "zz/fH4metZHHPnu9NUJqv7Xd3brc6jcReDYg7uBTR567vVnu8c" +
                "Nc5ZVsZ7F8u9oDjfdxMezvech16f43w+9SwVkcblDKQn0hNwvp" +
                "uzBy8BnZO52q7PU9MT9fNcvzrpxiB190MAATKQYilQK7g39Z3l" +
                "KdRY/5PQkiXeD0Ep1njk8/lnizrX7ESJmZmeMPMQaxbxuJxB/5" +
                "9y/EqK6Q49n+5+CM1nwSv/fmS+ApLqX+UMJF1JV3597yr23y7X" +
                "T7rSeem8pKu20/WhgNQsQQnaoB4xDgUj58G1dZaR84u4pKt+HF" +
                "ryJHmAbGgUUMjNenxboiQDLpdeHSMpBa96Ph2S5pN7kFHVDHQn" +
                "+ZmWFHsEyJLudHm6POn289mNeidFtLTlGEC5kfPg2voH7Iw7C3" +
                "24o/4+tJoF+SeviPQe/yVRkgGXS6+OkZSC18h8Ludc1Hx2S0kx" +
                "AxuSDblkQ6HzsmRD32/S1SCFMfRq00nCbTnGombAyHkgzwXT1Y" +
                "hDPyEL8k9eyaJUGr5AoiQDLpdeMSeS8vghy9CDjCpjp8fT4/mO" +
                "Wvz6ApmtV9jjOEgqz2EvXeGwOeI4WlBBFPhzHlDKmK7AOOiHew" +
                "I2LgbGJDQhh2dLlOsRS5Jzr8gI8ZRvyNHqVzArNUtojeNiBo6m" +
                "R6GG1uuO5sccexx15ztifD2n0Bc2soBtPpoDKIrhZcxH/ThpkA" +
                "FnIbwySf1tzT20oIzQIzCSGUO/uo9bYKaSh47D5ju3TDqTznzF" +
                "dhZr18tcWbcz6fT7ZyfqOVracgxh+04Qil3fdyIu6bT7Z2eMhf" +
                "SDaOJW7J8BC81OenU5SSl4DffPvrc5F3W+q6wwdnoyPZmv4APF" +
                "XHuZK+t2pSf996WTlQOAgxpGZIt4PkZ/hGI57UJNenL4fmjJEi" +
                "K4GDwmoAjpvi9xlOsRS5JzrxCb50Ceg++fB8T6U7OE1pQllKQj" +
                "6chnuKOYay9LOtK56dykw6/PDtQ7KaKlLccAyo2cB9cOf5Nxm4" +
                "s+3DH8DWg1C/JPXhHpPT4kUZIBl0uvjpGUgtfI9X0u56LWZ4eU" +
                "YOz0WHoMami9n2M4zqalx/z+eYyQpOcSXkDOvdns+0huv02uIw" +
                "/Dhmuw5SziMYermntoQRyISXmnzhj6dv9kFjof7pEkbO6Pce7y" +
                "N5n5eo6Zn863czqDa80vnKzV/SVzdTofMOl8pfk5+v3P7i+Fn2" +
                "ymROn7S43uh2DsyqRZ3fR+yGSMZav7S8msZBbU0KIMxtnsZFb/" +
                "CMgJQfpmRUagGH59VtAHjx4yC2MSsv9dzV3qZUYkKe/SGUO/uo" +
                "9b8Fx4hDCOQk9LpkHt2lw3DcfZsmSa3z8LCaG5HdeAh5iMYgx1" +
                "k7XdP6dxO8lBxyQfww9JmdRzW6pdW9sdMneHnU82B5K/9ColEm" +
                "0eaPZ+SOUPZo6bT2N35sbvh/QdofdD8HxnZ3jk/ZChnfR+yLCh" +
                "90PEztD0/RC3f7r3Q6wm+n5IqdS7uMH7IffE3w+p7pvK+yG2iP" +
                "dDCnT+fkhsv6KdqO8+vXfQfTraQej5kb8bfWvjHRFthna33h/p" +
                "Xl11W2zHHpmu7xdKXrB/khfkXd7BuXC/khO/BxnGKe4a3q92qR" +
                "uSG6B2LclgnK0EKUkIze24BvAxGcUY6pPW3E5y0DHJx8gMKZN6" +
                "bku1a/v/GTInD3xmdLaShWRMsfJxW9JGfSh2Pq+zv0PO03LAIp" +
                "7seL/yZnB9aaMYlTepP3J5g+dibdwjj4Lf58kHsgp5ydq1jhlx" +
                "Ib92/2zTufAi44R8FPs1yRrqQ7HzuVrM5xoshKBW92MRUN/+A+" +
                "qPfKIRnnsMPdv5XMO16F9ayNq12SrOhfza+Vyjc+FFxmmWqd9P" +
                "+fXoYVvO928FrhcYdz3qheuRrfN15d5XLBDLmkRg16P2faZidr" +
                "dgdLOvb89Hdwb759VwPfLa4npk2/l8/4xcj2xO5tvueuRl7Bxt" +
                "dD2i50f8etTg2+2r6atQQ4syGJc3ooSQpOcSXkDOvZHX/P3Pjd" +
                "IDabDlLOIxwQfnHloQB2ICOYW+yR8x4X7lXBFjiY5dZ+k6mSV0" +
                "FZTXQ3/dfSz+PE5eO6PP477Pr/j8eVzsNxL54dfwkbbgiaL4hd" +
                "ToeVy2NvyNM9VvG+zX1F4pKZ7HHUwPQg2tn+uDOC53oISQpOcS" +
                "XkDOvZHXfD5/KD2QBlvOIh5zZK3mHloQB2ICOYW+pYXOh+tJwt" +
                "ZnjmZ/3YlwfZY3FGtjQqyViXC16vVJb0CEf/vyhlbr072p0Hh9" +
                "+nWWKtQEsYT3GeLrs8hpQq7PrNzsfQw9S8UMTKj1+XL6MtTQ+r" +
                "l+GcftL6KEkKTnEl5Azr2R13x97pceSIMtZxGPOZJo7qEFcSAm" +
                "kFPoW1rofLieJGx95mi2PsfC9Vk5XKyNsaJ32GFbrU9rOSZW62" +
                "Gxfz7JVtzh6Pocc/bN9s+cB6HGiKUbNVqfRU5jcn1Khnw+RKZj" +
                "Bb9JMcb1+Ur6CtTQ+rl+Bcftv0IJIbM7UU82srgj28G92bPzS4" +
                "j3v4+eIA/ZXVyDLWdBXrkku1tzDy2IAzGBnELf2R3cQufDPZKE" +
                "rU9EH0oP6f0CZK5ku9JD/nnHocoE4KCWI2ixoD1hEMXeRtqPmv" +
                "TQSDe04f6JUvKDSFf88w6Gcj1X+tbiiHxy79lOmQN6bvY+GHmg" +
                "/RO9yBnIv/dvcQeNoNi/5Wv4+whlUAjhWnc/BEbkQ/Z1jKGXSD" +
                "tyU/TX0RbpUXvzv4+2cBSOUCI1JHM5hf7976Mtmj8vlAPPJMjy" +
                "iuQKqF1LMhhn94OUJITmdlwD+JiMYgxcIK25neSgY5KP7AEpk3" +
                "puS7VrB84PmZMHPjM6W8lCMqZYfrTVHTSCYlfzW1oGhRDY8prL" +
                "+ZhiVN5Ktra4n7BVetT4gTnOB0fhCCVSQ7KB2TH/XBbLmOfAMw" +
                "myXJoshTpZymUwzh4GKUkIze24BvAxGcUoT0hrbic56JiILE+M" +
                "fEvKpJ7bUu1aHVvy4DOjs5UsJGOK5Ue3uINGUOx8VrUMCiFc6/" +
                "fPWxDFLcRfjsUoP6+1wfq8RXrU+PLz3COyIl5aQzJnGfrnMr/+" +
                "Z8mMeQ48k3geyTJ30AhKqbTuGi2DQghsec3l8RjlF5JlLeZzmf" +
                "So8eUXuEdkRby0hmTOMvTPZbGMeQ48k3geyZXuoBEUG3ullkEh" +
                "BLa85vJ4jPppWhtjxL3E8OQRWREvrSEZ5KT9c1ksYx6RZxLPI9" +
                "nmDhpBsed7v5ZBIQS2vObyeIzkWq2NMeJeYnjyiKyIl9aQLMti" +
                "/rksljGPKDLZ1upZGf/lWF4Vfzqln1Hpu3mxO1w8Rvpc6/tj4S" +
                "9a9T7DgHyqJp/H4fsMkedxq2L+p/o8Tt+VieeR3OwOGkGxsa/X" +
                "MiiEwJbXXB6PUZ+ntTFG3EuIL19PHpEV8dIakkFO2j+XxTLmOf" +
                "BM4nkkV7mDRlBs7DVaBoUQrvXX96sQxS0axaiv0toYI+4lhieP" +
                "yIp4aQ3JICftn8tiGfOIPJN4Hup9huL5UVYvZBfmz4++E39+ZB" +
                "7Tz4/MisbPj+o3uvcZmj496mn1/MjK3P+7eDT+/MgsavT8KBvy" +
                "/f/y+RH9v4tcclv82YbcP3GcPSL3J/+WVK+6bxbdZ9LefKfspZ" +
                "0Ke2mvk7Z63qGj6J1Lc9d6uF8X7p86tvSAWIdCpNxtQx6xdz3k" +
                "Xl7M595wPsNM4+9RhFcW4jBw8f9vPgcWNZ/PxlfgqcxnsiBZAD" +
                "W0fk9YgOPsCS0hNPX6jvAx4HnhPvLzvU1G5Xb5vrQgHpN8ZN+V" +
                "MqnXtiizdo/rjCV79N8oWx1HoRcmC6GG1usW4rjyFy1JFtam81" +
                "HYAr42A8bpgyTDnj2bHoxZY3weEVnROIaRCB5NsnKtjq3ZA9ah" +
                "OFJz0IzJMryrXy125ex7ISLdXZrCB1ExdLp7aj7+t0/D92mnzL" +
                "8Zsi9roPg34b0SYw==");
            
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
            final int compressedBytes = 3284;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXH2QFEcVXwUuCSkOAoEcYAqlSoKGeKJlzhg/drd3/cBELw" +
                "iGChgS0RgLUmqiVlSoo3e5XTwChSRKqoRTy4p/UWo0mIR8q9Eq" +
                "y380KYMpKSElh5X7RyiTv+2eN2/ee/0xM3s5cLZ6uvu93/u993" +
                "qmZ2b7Zk/fo2dUKnqW7tMXm/qHFbPpS/VcPUcvqFRq39ALE8ki" +
                "U5bqt5j9IVPeqpfrt+sVpnWVKStNeacpq0y5ppJs+t2VbNPv0+" +
                "9P6g/pqv4wyMZu1U0j+aj+uP6YqT+hP1nxNr1Bb9Kf05tN6zZT" +
                "btef11/Sd+o36TdniEv0bLPv1/P0Zaaery/XV+glegC0O5fpZU" +
                "b6tgT5jmQ/qFcnOb3HtIf0tfo6U19vygf1R3Td1Eo3mP81ptxg" +
                "yo2mDOtPJbKbTFmn1+ub9S16o4h2iylfqFSq/dV+2NsaNugl8o" +
                "dByiT9sselyMb7XEY+xrYwnodJ48fg+gzFyWNxvXFenpPPTZEi" +
                "u5ut9CgjJl92a07aD/WgmGN5tyuDQgiq89quj7Evknb0UCWwIT" +
                "bkxWfEqCiu5uTOZVxDnJCTy89loYy5R56JG1d1dXU17G1NMuiP" +
                "joOUJITmdlwD+JCMfIxtk9bcTsbg+gzFyVlcb5w3zemwHzkx8J" +
                "Fxs5UeZcQZerA6CHtbp7pB7I/+BKQkqQ6qe3kPayyItyhCEwd4" +
                "QK3r3Y+BM5PExUgE98Z5QeL6lnEgt0VxpBwHP2LylVxN0/tR2t" +
                "thypxkLh7JZItNsfejcbgfmf2KVLOSXZWvYe332n37XNZn9yO1" +
                "1t6P8jb9Wb0hqTen/dsDGHM/0vuTVnY/MnVyP9JXwny39yP9I3" +
                "4/UmuTtrkfJfX1jI/dj7rL9Zr2WbofpYib9GfwfpRKbo1l0Dzq" +
                "X/lGf+lqm5Pq2xwb4TIowFi0u+26KCR1oyGPoWuxi4KYbGl/H3" +
                "ownv6GvvNzsPGHosyiOlrE0vxFxjWejecjrlaNqx0cG4nGoABj" +
                "0Z52R0jqRkMeeUQyQkJBTLzn2pB3N98wyjDsiI+SWwdmTzbfVX" +
                "bHHX3c83NIbTfY5QXjmaCS1vaAdntI6j9/UjTqkMufYmYz/HxT" +
                "rvAxvvcUf21uBtv1mrwo9daI3V61F/ZQowz67RvU3u5VVtK+MU" +
                "OOoJ5sZEk+I2lvBFCc1chGJANiMAKO9z+A6q50Y3dj4hmxXEfc" +
                "jKHdGhcWI5CpiNvzw8cxLQfUAdhDnegOYH/0GZQQkvRcwgvIOZ" +
                "tkNbPkBcmAcrTheP8jmSWLjJKQhLe+ZcacgSJws5X+KGKJDp6z" +
                "h7P5/lRcZ86Q60LWrU0SFdq6qyqlN3W4tdHly+MffTof0726Mg" +
                "1b9wORaPepfbCHGmXQH/292td9F8gR0ZxAPdnIYj/NCegBWrI2" +
                "JzgHeQcUjyz8AVR30I3djYlnRLla3zJjaJv5ziwsiiM5I0n4OP" +
                "LY7dZYGnheet5/Eipz9IpQjVNRzdKpefRZYs9Lcd+9eihALMnG" +
                "+sFsPP/snc8PlpqjBajGK8VRcC6Xr5B/SRwT993TeC6JZL5f7Y" +
                "c91CiD/uhfUEJI0nMJLyDnbJLVzNVtkgHlaMPx/kcySxYZJSEJ" +
                "393qZswZKAI3W+mPIpZoNuKLA/P9H+dnvne/Gz3ui0Ncse9HeS" +
                "yx+d7dPS3n5+KIfFFjEbakzJbRkyDFPqH8HmE4F/fAZC+jBqxC" +
                "UZAl7vGTlwGPl2fEfbu+Qqz+OMW8OiOwoLEA9lAnugXYHz2FEk" +
                "KSnkt4ATn02ueohy1zjtwnGUiDNY8i7NOP3bcgJMN/2c2YM1AE" +
                "jQXts5xX+qOIKW5bqn3VPtjbOl176sP+6BmQkoTQ3I5rAB+SkY" +
                "+xr0hrbidjcH2G4uQsrjfOC5LuPj9yYmDrn1620qOMmHxlIzyQ" +
                "XVuPYav9Ne/+dazU/f1YvqZ7f3ReDUzL1W0gHgNdP8vlkh9n++" +
                "4So/FYdj/6d1xXjiGs6R6unPctFkP3UG+5TEMkj2bjeTauK8cQ" +
                "1jTuupBZOGfWXb3lMg2RPIGt2gpf1+nrhQG3ziVcM/ZVprloal" +
                "F2Lu41BjcniejMPF/jyf7SeTzwN8XjU3n+RCvQjH3H1/QcZYFd" +
                "7PmT7GSUU43DOYpH1BHYQ40y6HcuRQkhSc8lvICcs0lWM54/lg" +
                "woRxuJD/v0Y/ctCEn4zmw3Y85AEbiepT+KWKKDY5ytyteujOty" +
                "j9Lj+Zqxn12Aq1YkBsqpXC7TEMnPs2vKnLiuHENY09h2IbNw7k" +
                "fbestlGiJ5MhvP+XFdOYawpvvrC5mF87z0q95y6W2rLqougj3U" +
                "KIN+52pXQmhpRzXgeeEc4LW+IWSN/rlH1ydxuDKpd20pV9e3Gz" +
                "3yx7J1/TjogeoA7KFOdAPYbzzhSggt7agGPC+cI72G/TRkjf65" +
                "R9cncbgyqXdtUYY5+dw0Fsgeztb1I9H4DpV8Hwz7nU3yfSv+5p" +
                "X7vlT8HTF6Gyt7H+w5+QZXc3LncODJbNL36cYpWbh+5zL+dhjh" +
                "Oxvdt8c4g79OyHMP+YmtKta6UHCfXD/N98LOOJel7911i68keZ" +
                "hi+1q3CNOqSRTFD73Y8ydi8vlD/pO387r5LFq8T5vKsvdpG095" +
                "79P+djrep63PeKPv09ZnTPV9WpPTFN+n1b8rfp9WnVAnYA91ct" +
                "87gf3G0yghJOm5hBeQczZiTcdzpmQgDdY8ipDP+kw/dt+CYqBI" +
                "ICefW1q4+XA9SdjTRIqW75rKK5eaq+Y2Jzubp//6aXk5y4W8fl" +
                "rfU7t+upGErp92vit7vpr5riDXbL4r217YuUPx+f4H5c13lc53" +
                "xea7wuMWmO8Jb1M/78539K+SVny+qxTL57sKzneVzXeVzXcjy+" +
                "a7is53o1mjxHxPGMR8VxWGxvfn51Xn1f9j97ZO5mLas33b5hKL" +
                "lz20J03y9BCUYQt5JQv4AS35AD1iKE5bpIxbyMggDpChFxm5/b" +
                "TG0RZHgWuJQcaBGWGcyeiu4rVa1XwW+yhh190/Gr0zK6R9iFPO" +
                "DeT1vtGsQq3KfR/Hal0E9Wm+c2mo5bOEfehPh7Ehzuqr1VfrB+" +
                "0eajPWac/2XYnFyx6XAJpkyZE7CEjwhDLiADTU9YOodfWIQT+A" +
                "lTJugTqMCGzQi7Qi/+QNUFwrPVAchMa2/zSqv5494w2H/xouz7" +
                "f2mV5XRGsnS6yYTvbG6t4To8+fJ8vx5+vztdX/QklH8ebsef6A" +
                "Kff7SMKWXicQFrVX8vVhidx2LZcYzAFlMXv0nc/fuiVfH2Wf8M" +
                "9PkMlW3vlZbOUez7F/Td/5id5Kn58TZfhrE2/o/HwNSnpsbsvO" +
                "z6NhJGFLn5/ConYqXx+W5LNiDiiL2aPvfP7Wlnx9cXS13VBwn4" +
                "zno1zLvr+XeAdIYpzr5+5i63I+CEXxQy96fu4uE0PMv2vtolrJ" +
                "3x1r/VBS2aysdYcZ0/sEX4Ks9QeOaPbbgFbgL4XSov6nfH1Y4q" +
                "wL/xUwrT4eGdnF7NG3q2+J34O07sz3X6CdCyXlyv6a23ksjCRs" +
                "2U1adF/M14cl+ayYA8pi9t0XyvC37snX52v1LP57GXpeUor2qe" +
                "7vrcv1y/L3MqjnOM9D9uZja2EIqY+7jPh7mcjztmr189/LKIW/" +
                "l9En8u9H6Lvg9zLKri+F7FLrrblX13NQvPPzmTCSsNHje69vx+" +
                "bciyHeIkk4bjcHlMXs0Xc+f+tb+fqYNllHGqLaHIehbH1pyLZB" +
                "Qqs5u+a5Kz+oxzpBXZYe1aHg+tJQaH0e/dtP3vqS1SICNRQ1X1" +
                "8CPxBbtr40JK3C60tWw/NBBh4JxMvzMa3T6nT9dbuH2hy/tGf7" +
                "tt1pg9xK7J70XMJrsLUtZEt8nsYW8HIGqMHGaqFFeuphnIggf5" +
                "KRMgPfhJdRcv9kCyiuRQ88EsuZnvOvoyWsz8N6nb8+33nWW5//" +
                "p78+3z7D1+f1yeL1+T0z/o//7+KRMuvzBjWl/3eRXAsegrPY1r" +
                "aH5271oc5zdg9yPNutXq4do54/ZaIM6z0zaS5axtD3C/QPHuP/" +
                "n4Hi5DHI/88A9yP0AzGm/5/hSVpb5nFwPosCTvrORXmjJ8qbWq" +
                "Enbrq/178ptaH/EtHrN7LWiBndWcXWRT5a2v2rgniTbTJ2f8ec" +
                "prYe4h7fOEtzbRZp9gTfeQmf50HbXIt1ifHMwdRfKrYu8mGf5z" +
                "nKtngvZo++Xb3zPL83bI9St870w81h2EONMujXH2gOd17jEkJT" +
                "T9pDsZbcA2GSnB5ADm7txhXyyZFSJvWuLUXi+uZYGgM3NzcG4p" +
                "RolsH67Nhkv0tqfU9qm+uxLnF+5mD2zCm2LueDULbFezH72m+K" +
                "47OosB6lbh1ArvPne3dAapvrsC5eT5YYZ64Wz/d1efYhlG3xXs" +
                "y++7fi+MwY/CCsR6lbZ9v/APLHJ2A=");
            
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
            final int compressedBytes = 2897;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXHuMHVUZv60pBbQVoaWyZi2uDx4tSaXBQHV1p7NDX1rA1h" +
                "aD9okPIo0mauIfajpz28zQuyT2H0BNaOI/BiopWAlKAtGapukj" +
                "qdHEUAQSQ+wmbqwlUHwkiHPmzDff45w55+zu3TKb8/q+3/d9v/" +
                "PdM/fOmXtnO51OZ+WBTieZSCZWHkgm1EjVuqc0ulaHlis9ILQU" +
                "9NoS+rJVVlqvPJpI6CmtZgQxaQs47YNyAP8g270YGWmOWq+jQJ" +
                "/yQH96hJkAO92HSJI5+uUH8or34axs89N1d5zGlbO3RegNgQYY" +
                "7r6z07HzwFg4ExuKM1D5xD61iZ6QLG1eoydkFJOdHKkyOnd0rq" +
                "51q3R6pPr5MpAgEvUg6Y6jBn1Rb+i1zue13ANqoKUsbDHRM/fC" +
                "LRCJ+OiXcsbUA2ABxXnIOHg06Pmj82XmtUyV/GathTHUthFisE" +
                "VvYn1eDxptZcOgJdTwZ0dxBjwutYmekSxtXqNnZBSTnRypkn47" +
                "fZdEpt9J31vWV5X5XJ4urCRXl+UDVe98jfloVV+nV316Y1mWlu" +
                "WmWruMeLslvVWeLb1PVprb09XpqrJdk67rWI70S+mXq3ZbVe9I" +
                "v5Z+PZ2Vzhao+ekVdW9BWQbw/TNd3GBuIJk6nN5cS2+r20+ln0" +
                "lXinweFlHWG+zuYaOdZblX9bIyF9lANlTLd6l8Air7iPTSvdGc" +
                "d/ahsiyxyJeX5RZDWr4SvTs60z6yQY9+mV0e/aIF/2GJyj4+KT" +
                "63WlbET/X6bFaxwam7JvvsNPNQrvjenX3I5xq3fvfilnz+OsR7" +
                "GCqQ6Yamt9HI57em7f0LZT7v7cz4kX2+JVPPBuXTgkp/Hxo7nV" +
                "OWS5tV+ek6d3+MVxjIM3uuSF9Mh3we97xPt+AhfYlqLX5fMCR3" +
                "uyMolunlBH9lWRaV5WU+C3k0jD7hyclakyXRfsP7iuxTJdqle/" +
                "rIX6Haqt1FRz5/U9EBwofJIo7S/In9Lndst/+2+NI6JBPZI03v" +
                "x+b6nNrZh+sz+0l5vn/1IpzvD7dk6lDQ+X5ouvFHNupSMmneI7" +
                "Ofl2v0DGixHdnonc1jLszKP/vZ+GLkL3IU8nfbQ2ypzx6l8uxx" +
                "7llGkK1td1dq76Kf7/FQXL5P5j+iu6uRu5TM3H0Z71JDGhMP2X" +
                "Sx9/3Xtkt1o/huM5nQc7FFb9tBwgyrfB5KJgAJnlRr7lUDzs3m" +
                "eil/3b1rnerRe2jmz/e266XssO81qlC/6uM7T3PtFR3pVz65Xe" +
                "/Rdy6f5pysOfjNtN47X9el9tVce+X/tiNH2LrtjodFIPk84Nbb" +
                "JW6vMAeQtdlHT4f4j55260PYNfl8rsnnf4w4a9zjFnZrOC7/y8" +
                "ytS4jUxiwvgnLw2z6e72NN7P8abFe7xy1zXM1x+cszmM/Vbmb5" +
                "S0E5ODJdHrg/Ip9HbzWya6oz+6Huw9npcjTkP9/T5RUv837IH8" +
                "rz/aCXzSbf/ig7qfZH6Vm5P6pHg9mplnzvCdkfRXvStbb7IekG" +
                "vj9Kt0zq8/1tqdv7rMrnJNa6PZ/P9+Es+pNH35ZPzydNdxWgJn" +
                "d/yYjzgC5Qq6OYRbVVu44jonUuf4jiOKprs/Zhqv0mQQF/YNjG" +
                "rJnJA1OJL639M6Hvn8Vsw18yleunZILb9X43c++fwEgybc65V4" +
                "NW/5n+McL9ZmHcu4/WuseNj1c4iuN6R2YunxCpjVn0VJCXp6b7" +
                "muJKy/7a9F61I/mqDLn+FNfzL/j3CWH7TXMO9Ptia6YOhviPDr" +
                "r1k9nXkHz+bYb2R2c6M3605TM/HmKdn5ji2dFkLNluyqQ22Q6t" +
                "y5Pu2TDNPZ453vxvd9mDB4WCuAqPNu32FO/Ki10PUtlakFvM66" +
                "W9z3FtsgXagBXpwIze7bcOi4Eo1VNl7/Nue4jt9p+N2/Ugla0F" +
                "id8anWs+jy7h2mQDtAFzdWDycb91WAxEqR4dtdnnZ/38yhyct+" +
                "tBKlvLCu/pAnWVz7lUW7Wj2RsU0f5+ipjqemm0Tddm7cNkFzgK" +
                "+GtGpWbUzcvtvy2+tG73kjzZMG3uKhWXSW20imP12O1PoTiud9" +
                "678p6k9iEo1VMFGLYxy940+VlQb9n1IJWtBbmzuf5pZMXlXJvs" +
                "hDZgrg5M74Lf2hejO4ujVI+O2uyz//n5ld5n2/UglW3Y/r1YZO" +
                "HTj/37v6Z/PdSdM8X9++yg657Z092/J1ubXvOLlOIark22QjsZ" +
                "f+Yx9hW/dVgMwnorm0Or/cjbfn4KZdeDVLbslZ7Xtt/sXjYz19" +
                "pjF+H74rbr+e6lnYtyJM01YffKZn0OmFpz5PNnuWaZ8Fv7YnQX" +
                "cJTq0VGbff73kDl0r7brQSrbsPfP7kDIfnPy+/exb75z6zMK2z" +
                "f2kQm5v3Rdv3wW17M9StyHz6PBqeWzH7G9r8YlutRMr22ysMSO" +
                "RGzo+uQWo1vdervE7RXmALI2e4jt8+/We7RzdKnz87Emn8N2ZD" +
                "Rn0q8Ys8jPufV2idsrzAFkbfb5P0L8d29w69u00f3R/brWLcj0" +
                "uBiREkTjiNvTghEQU62RbeCDWktetpgUyWVcL22RiYxNsZy3OV" +
                "v+ByiKjl+LX5M51jJVilhrYQw1SPiIa7k3sUb+CRrw47KEGv5s" +
                "bE2WFElt8rMyls2rmRO3pMnAuficrnVb6c7BuEhAgkjUUwktWk" +
                "69odd6The4B9TEzTuBjCBjmtxNC0QiPn9Dzph6QAYyMo+HjDma" +
                "XNFsaz7fm/vz3aVcm2yD1v95xDHiM8G754NI4SjVo6M2e4jt9p" +
                "+/adeDVLYW5A7L/aXVXJvsgDZgrg7M2Pf81mExEKV6dNRm39xf" +
                "2uG5v7TDNSvZNvvUFSMrdD2ygsr0uFirpShBNLWjGo23yTDGaI" +
                "dbUzvOQca08aReZDTqV0tkbM6DZkbOlkfkjDFWPR4eGca+LmU+" +
                "v8tyPwwFEdjKvnGHYRhjFPNovDY89WhDow9gZfLitWrzQcqFs5" +
                "dzoYXH8bEvV+4XLe+fy02tOfL5s+yW5vutw2IgSvXoqM0+XxI2" +
                "B7sepLJtPpdOxCd0rVuQ6XGRxieKTMsRAXqQdMdRg76oN/Ran+" +
                "/zuAfUxCcos/aYNu6mBSIRr2LzGVMPyEBG5vGQsUCfjE/qWreV" +
                "7iSMkwfjk8mDWo4I0FMJLVpOvaHXOp/v4R5QAy1lYY+puHHupg" +
                "VyQCYqNp8x9YAMZGSeK2Qs0KfiU7rWbaU7BeNkf3wq2a/liAA9" +
                "ldCi5dQbeq3z+W7uATVxc1ddRpAxTe6mBSIRr2LzGVMPyEBG5v" +
                "GQMUfT384Un4N7a/C7lWJ9yP8TkL914b/HsT2pUbwfNL7/J1Dc" +
                "0e//J5APhvw/Af99Svv/E8DntZNNeP8TnteO7oPntZNN9fPaPY" +
                "2F57VrC/a8djqmntcGf+p5beP68/uVxvG8drKpjOF9XrtC1c9r" +
                "l70FZRkgGvvz2vfp57VLveN5bZqPCrOeS0vre+gYnteOj8fHda" +
                "3bau0eh3HxQZAgEvVUQouWU2/otc7nD7gH1MTNr4tkBBnT5G5a" +
                "IBLxxaCcMfWADGRkHg8ZC/TR+KiudVvpjsK42AcSRKKeSmjRcu" +
                "oNvdb5/CH3gBpoKQt7TJO7aYFIxBdL5YypB8AWN8nIPB4yRt5V" +
                "ORYf07VuK90xGBc/AwkiUQ+S8nqp0aAv6g291vlMuQfUQEtZ2G" +
                "KiZ+6FWyAS8dGAnDH1AFhAcR4yDsknoE/HxvfpWqZKcVhrYQw1" +
                "H5X5PM0xVGuLMJaBRlvZMDY/aGGiKAMZl+KjRXwO9vjRIsnFZC" +
                "dHXJJstuyPVphaOure5ri/tNlxP6Tr3flsdtnbUKpHR2320VV+" +
                "fgpl14NUts57K5jP4U7AEfL9kcjn/ul/57X3do++7fdgC4O+n1" +
                "k4RVr/BwEapFw=");
            
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
            final int compressedBytes = 3226;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXN2PVVcVHzQSpbYUigMUKTiGFlDitKYWRdsz94O2zGhK0q" +
                "ZVqi3tyBTjB8TG6EMT9r1T5l4lYj9o0o9g1KhFRFr8+gN8NcH0" +
                "0QcomikP05CQEH3xwb3POuv81lp7n3sv0Fvuyf5a67d+v7XOnH" +
                "05c89csk3ZppGRbFOWj/SiVVh3/kVWWIDGrH1eeggvm+QghUNH" +
                "pCo8cQ5WM5WnZLFqkpcsE6u0ts6DuRklq9WKOmNo5atPhQMrap" +
                "51g7VRAyKM7fO8AoeeW41Dz1uvfTEWKmkMULxii/bARjVZfmlL" +
                "VSwVZSXpOrL/UKNX6xzP2vU0EthBX/0iYv8gGhLDNbCtKr6z7U" +
                "ozGsTbXGguUB9G2Ar7DFkZg5lc6cZ4rRBrwBL6Aw+kMos1bZ6a" +
                "RfoPrENGyIBrirllhchQ1m4z0dnKqrNbwoEVNY/Ya23UgOBR9t" +
                "Iu11aj58//Fs2YwoORs0Je1gMb1WT5pS1VsVSUldi83PfdB33/" +
                "IbfYfbi0XeeWuuvdTX5v3OM+lltGfVvjPu7fAe7zs/VuzG1wt/" +
                "rZbb5t9G2zb5/2bUvBMC4U7nRb8/FLLnN3l9amb9vdfe5eP97v" +
                "JuOz5R5xj7qvu8f87HHfdrsn3Ix7yi1yHygRH3FLfH+Du9Et8+" +
                "Nyt8KtdDe7VXx9unXe+okcmf9L4T7jbvc13e3u8PO73Ofc5/3o" +
                "d7/7orvH1fxYdw1f4b0F+w7fpnz7sm8PuK/ktp2+Pegecg+7r7" +
                "ldKtsnfZsufxbrw4EVNa/9b2ujBgSPspd2uYZG7RXrja699Zox" +
                "hQcjZ4W8rAe22ispfmlLVSwVZSVRlWuyNdTTyDZad5dkaw4ekh" +
                "agdRxGYpBNshbn8+VUNOvLHKwmOA7+T9u038aiVqvN89ZReQ50" +
                "/lYhzrhEr83WUk9j7lvL6+6N1gK0jsNIeNkkB6k3fpKKZn2paD" +
                "XBYW3ab2PZFmvb7Jm/qlqro9Gpl7hfmhwZyqsxNzL014F110I7" +
                "G8/Gqc/GpY3W3Q1khQVoGSc9hE/ZoNH4sY6WcToHq5nKU7JYNc" +
                "lLFqut85BnxlarFXXG0NKviX3q+tyZ8r8H12dn+NdnVabD1a6/" +
                "UH+BehrZFo72W93NbAESfmmRjeySTbLmNXU1A9s5RuLjQzNrFp" +
                "0lkMAHbV2xZEAGtlqth4wN+nD9MPU05r7DvO7ewRYg4ZcW2cgu" +
                "2TSrv6c+phnYzjESHx+aWbPoLIEEPmjriiUDMrDVaj1krNHJa/" +
                "ZlnnVvq/b1vOr7oDpTg+6f7sbAZfkGyaIK05kc6n5/rv4c9TSy" +
                "jdbdu9gCJPzSIhvZJZtm9dfIcc3Ado6R+PjQzJpFZwkk8EFbVy" +
                "wZkIGtVushY41Of0bCs9oN1b5er8FQg72ulKvqfimu6T29Pl+q" +
                "v0Q9jWyjdedhtgAJv7TIRnbJplnlSkdwjEbYQzNrFp0lkMC3T9" +
                "qKJQMysNVqPWSs0amXe7p8r3k08h0zdyXbkwzjg6AGe7njl3mf" +
                "tL235sFdQ70+j9SPUE8j22jd3coWIOGXFtnILtk0q79n+ZNmYD" +
                "vHSHx8aGbNorMEEvigrSuWDMjAVqv1kLFGFz/L5dSi3zcfjH76" +
                "ORLYHHV+gKtGRTSO9fanLfrVfkhjuAa2VcWzdj/+3v5+0anf39" +
                "tvtb86pN/f/3D1HM/2eQ95tjE87R77/cX6i9TTyLZw+N+PvsAW" +
                "IOGXFtnILtkka17TXzUD2zlG4uNDM2sWnSWQwAdtXbFkQAa2Wq" +
                "2HjDU6uZO+Uc4eG9L1efIafr40VG08V2qssM+5/PU5IZ9DybvB" +
                "eNXveZyq6U35hE4+j0MW+ilY1XMwyY8MAgs9j4vzabxps0yx9r" +
                "sLbqxIP49rjDXGqKcxx47xuruLLUDCLy2ykV2ygbXI5rhmEJ6x" +
                "MmOjYDW7j9rc4wjkgEyCtq5YMiADq6zPFTI26NHGaGEZLX25rT" +
                "Hqr8/HyUpriYpXwEguqSBsf2EPRaWyQCT3fBiuUZsFz2RFUttq" +
                "pVijjCtVzRlY3lhOPY25bzmvu0+wBUj4pUU2sks2sBbqJzSD8J" +
                "T3IVbBasa5xxFACvwJW7FkQAZWWeshY40u7pBubomrt72nnM0M" +
                "6d+j3149x8H5Pnd948PT7nG/dKJ+gnoa2Ubr7gxbgIRfWmQju2" +
                "TTrL6m32kGtnOMxqc149zjCCCBD9q6YsmADKyy1kPGGp08xyf5" +
                "fr77vSpfn5/Syd6exp+Hf79UlQO060O7c9L7Xfy++e0h7ffXr+" +
                "F+f32YP8VsZbaSehrZRuvuD60FaB2HkfCySY6ipjdS0awvFa0m" +
                "OKxN+20sarXaNnvmr6rW6hj06mw19TTmvtV0+P3+I20hhFzFI+" +
                "FlkxzF+TyVimZ9qWg1wWFt2m9j2RZr2+yZv6paq6PRvT4P8fv9" +
                "6SHt999fw983h6pdP1M/Qz2NbKN19xm2AAm/tMhGdskG1qKmP2" +
                "oGeHiUWaQ149zjCCCBD9q6YsmADKyy1kPGGt3n+vxB9LnfTO91" +
                "xaeFMxMzI+/Ti5WqFDu7h/rv0bvZu9TTyDZaN16zFqCx0vGyQQ" +
                "EYWBElPSm/XKc4NEKqsQeZhJpS3DgXzB5Xqw9GKfSF7AL1NOa+" +
                "C7xuvGotQGOl42XjWHCU5+KCZJGelF+uUxwaIdXYg0xCTSlunA" +
                "tmj6vVB6MUeiFboJ7G3LdAR31JfUm2MLsXFkLYlY6XjdnAWrzb" +
                "LGEOGa3O50JaE8jOk9qm/TYWmVhtxraOynNga7M5gFOjk58nd8" +
                "u/zjgQvTtN9V5XvKdNTUy9b++fU70zaw/094rtQ1f9yTI+BQHr" +
                "T7W3uYfHy+FL+I70jx5MA6gwk6uqeNbux5/2s9WO5b/z79TfoZ" +
                "5GttG6e5wtQMIvLbKRXbKBtdir39QM8PAos0hrxrnHEUAC35m2" +
                "FUsGZGCVtR4y1uhi9/+XWnS/9Hx0P5AjgR34PkJFdGZ6+9OW3q" +
                "xcA9uq4jt7BuPv7e/lbfp3lOa0P+aa02GV9zSb4z7H5fbgZwRZ" +
                "S/9cc1pyyrFQmC5wAqnzKLxzzA8Vk+8c58E5ML+MYB3KsfDPcT" +
                "Y2Y/AVK5yJafhJB0y6yvCqzdMzqdo8rfjZUm2++8+RkdmbavPy" +
                "mVPw62dvtfn4qRXbpI+fetXmSaO4Xp7SMcFLGcXP/yRudoV8Ek" +
                "dZybzC75usQzmSn6J5Dt7WUflULnhwJhirvx2I2mrqs67axZGR" +
                "8H2uMIaVe8avrw+zYJndS3ZvC9/n2uf9692YX91aWDey33u2SE" +
                "45yu9zBd7wfS77Yn3SdY/kUcUTa7db42oXO9+i73Pl3mWeMf8+" +
                "l5+vEufzIn+fK2fMv89FKuH7XLl/G84nfZ+rrGWHb+X3uXLL3+" +
                "T3uWRtmDV3NnfWF4WeRv+uWqzC2loCXq+khdCw5e/TiwhJSmwD" +
                "B6FprC9ir/UzhnUIq20ygn2cEcWwio6CPtQIJb1aAXkAzXP/3v" +
                "o2XcXZ2+Eqpp5mwUN+7Pfg1/ud/RQJTjnK/R54U8+7WY10q/d7" +
                "8HLOyCG131mHciQ/qfC86i8FggdnguP0fkdtmOWrU7Tfs1Pu1b" +
                "Di/Z6dCh7yl/v9fu83+5393rNFcspR7vfAm9rvrEa61fs9eHOE" +
                "2O+eMbHfvbXY7zk+3++kQvvd+7eBV+5379nhm9jvOYPa76gNs8" +
                "bixuLsTOhp9L5iFdawEDL08EuLHCk2zJiNlWhGvJKBRooJXprB" +
                "jxXnyQjoaUZURtrA6yylPmIJJb2sIDPJys/nGOnb0sbSrLjfpD" +
                "E7F2zBk50Lc7KEdZ5ZgeYVR5CH1kCBTV2H55iDlWhkfemXWnwE" +
                "b1beIyMWWbKdUZhxtrqWVI5SQzNUcRJHazM/j+sWf8nXfq38/X" +
                "2gJ4Atv6daif/3ofVZ3+6MrBsu5/eA7huVqmuv7Hlc5zsV+E9G" +
                "ltsv65nm1vJf+0sYJ/bXLvHfe9cukYf9+acM++Uqju9lgy9lZW" +
                "zwpv0SZxFxxMR+aU3N0tlVaVTXbX3t32gc/n6+3kRf/hxOR89M" +
                "mnpU6Pz6bP+81Pp16x9pZIqxF6L9i94Rrb9fKTej9PVZFdf+Zf" +
                "tX0c9yGbUik5+V2GNpJLA5apC/91YRB/f19qctvVm5BrZVxXe+" +
                "Oxh/b3+Vt362flaOPKd1fZK9jOCZWk3KkfEphdI/yRzgk1nNfj" +
                "ShIiICAzKSo9AoY9GjppjbnINJeMkze53NRJ9HVJ2dbZ4OR3aW" +
                "V/6u9XSYsYXW1AhNq9BTHOGb5XsBz4mB8BRHvM3TifvPs+xlDV" +
                "bByDjSBIprkHmhFs6RbNZf3s+f1vnjTHAcabAS182c+ev/LMgY" +
                "aw==");
            
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
            final int compressedBytes = 2479;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW12sHVUVHqOhhqdaq01ImkKNISnJDT+hXCRNz8ycY1sj9I" +
                "YWFGlITAyJ8QFEDTe+lDtnwCnSKhpNfQCtGh8MvEMC2kDBH9p4" +
                "40PDSx98uLmXqInyVNFEZ8+aNWutvffaM2fuGS5xmv231rfWt/" +
                "Y+e+/ZM50bRVGUHoiqC8r0wDTCNkoidk1fjKxL2str+m7kuXxI" +
                "4kde7TLa9MCTnw17fHLMpb6aFgdqpv8MM/h94hWfgYS5uSbfg0" +
                "Sy+GGJgLbvIoxBSRzXadZtGBMXR2H8GKEWWdOTM334beu2KKev" +
                "RdHKN+v6n9O4Gv1YIFad3ymmcvqmwP4mu70qX2kkr2ertkeGPy" +
                "89VrI/lOlPLuf0nC+GyuINk2cX69ardXmhyi/a3NPfeuJ4y6Cy" +
                "WzSGBvf7Mv1xesmWj/4FCa7x992aRBK26yW9F4/pel0iL+OHY7" +
                "APKNPsi6908x/WzzICOD/Lsb/szPe7w21l9dxNuOxkFCVvRpu+" +
                "pm9rXOHI5sHd8ktPtLatGYqxD5e8H3n04+7c87yyffFydl22t/" +
                "xFl6vfdRnnZ7yMEvH7LzszopRkN0lNJbvNh84+jX59foDVr+e4" +
                "Mubd4aiym7nUV5P17FMuR3u/PX72lakaT3e9Jyc7/SI3wHg68n" +
                "I84X4kx3NO82B3i/5mZb0rfZLjWUlumSmexXpnvZwuji6Pyp0y" +
                "rSSmVd/RFlHC7fLtzr120XiRuPxjrgz5fFLiMax+PcdRlNxW5/" +
                "HVZN31J326DH6fofk5+QGkec/PIp/D/ehv/eZnMR1yfk4uTS7F" +
                "j5scynIvqFumTRJAmpz0XMJLsDU19IZMUAO/3AOUYGO0UCM9tT" +
                "BORBCf9Eg9A27Cyyg5P9kCimuRgUdifNb75+NkGZqfp/46zP45" +
                "+eHWzU+Ne27750smmbzZSZr6qb93Oqu/xG18fmzp5LnNj6ffe7" +
                "te427z1/28pM/Pp6OB5udrW3d/17g3Oz+b3+Vlk0zOJfV4bu/u" +
                "wS/3SydvzGF+vtxPr3G3+ZvL/Ny52fnpkZr5eWEL5+eFrvOz5+" +
                "/8O5NMziX1s9nr3T10lWvoPnH30Wt98kfaK7J3eDl6J3sU2yhh" +
                "z1j38JZp2/Y+n/E9Bkc66UPaGK1fz3E2gtrIxCPz11wvIQ4X6/" +
                "cZer+UHqpOJmvh3wNQVLasw5PtyHZP6SHr/fyhru9DukXporrZ" +
                "sZn2jEnxI1Cr95ozkLjMben++ugQ0YYxcXEUxM/sHwlzh/1r/L" +
                "a1jcp3B+9HnxzmvFSc2rr7UVEMe54vZ/ICL9OF8UFso0TM+wVn" +
                "JSy4Gp8s5IPzI6+6Fhd4lJpHoyepr6bFQRzd+m3rRldGV8v86u" +
                "gKtHB+lvJawrBXDdrgub0sfTKyyD6BfoWPq4g1WlOTHDbf6Ep2" +
                "hGOwDyQjjzI2HpEbB/cntVZL8c52htOQMK/W+x6ulVj2JL3u3W" +
                "lOB3an06375+k2jLkfcRTFDy3tfoSYsH+N37bWvax8tNlT9zVr" +
                "xvm/v/jeCruXt/3jKa3ArmG4qX0/WvliB8y1rL6jTLswQiqd/b" +
                "MegZX9Lb4/F9R+TTnd3jm+c7RhcijLuVu3TJskgDQ56bmEl2Br" +
                "augNmaAGfrkHKMHGaKFGemphnIggPumRegbchJdRcn6yBRTXIg" +
                "OPxPis1/tGw3p4fBhyKCv2w9hOj6CEkKRHCaCoBDn3Rl7r/fuI" +
                "9EAaKNMjPAo/p4w9/4jU83hlJDJKWScLRBEzxMwjEfOysazWxn" +
                "ZI9R3jL826vCOKEvG/9YCMt0czXtIiuRTW+yXyMj44BvuAMs0e" +
                "uW29HVOYf5YRYOfPGzznLOdbjdD+6Tl/rpZn8Z9s/vyZL7acBy" +
                "8qzwGdufu8r5tcP7k+vdXkpqxmet0ybVPnEoOXLbQnDXj1ybAG" +
                "HkmCJbIBL2dBDMVpkpRxCxkZxAEyye3G0Zwob5Va8iDjAAtE1j" +
                "N9Ld5WzuBtyRq0cL0na/ld5Tz4eMKe3w0q3mbwbKV4nu9RhiW3" +
                "SNa4RfEU6pEf9JLD9Z3v5BjsA8nIo4yRR8TjyJ63VvQ22S/Z0r" +
                "xXrQ1eJhu43pMNI8m/mmz40Jq9LiOdT0r8mp7jko3i6+GontjD" +
                "pb6arMvxBI5u/Ra1HckOyKFEGbTH520Joakl7XkiBsKQlKy4xq" +
                "fnbZ8PieBsqKFIijN+3zQW6N3trfyHKI727vTNk0U+wLc+eflc" +
                "UzwfDX7lqXKef25YXv39UnpNes0w75e6+R3m/ZLGPb/3S+X5/j" +
                "1I9dw9y2vUIiRh1T3urPV+4T1NC3XXYxuHseMY7APKNHvktvXh" +
                "iLtGlz+U35vfny8JWfPkHH+m92o7rmq+VKYTc1jPx1r0X1BO4p" +
                "36lN83x53ny/XKOJgedKJ5MNxW+vCgxLl+53chkxbZkNzuxb+f" +
                "L2O6q8vzkbBXvp8vflxLqu/nVevzHpn3+/lSdk71Evx+3u6T//" +
                "v5bvun9v28tZOccGvNCD8Qbitz5gHCJSfm88trfpBJi2xe/DPc" +
                "qR5tzhbOs268FG4rfVzqhpspyhWNKxxZcfb9Hk9a7/k3nGiPht" +
                "tKH48SLjs58HgeDUeWP/a+z88PN++XFjrtueuzMsT/3XyUff8e" +
                "YR7cM97fv93cC7/rRHMs3Fb6cEzikg8NeH8/Fo5sSO7w81HJfe" +
                "Mwz0dzibvv3yPcOOTzUbqervMS69AufopaRGCNt/yJeyOvJCWJ" +
                "1LhRcE47TtsL6cmWclPmt0kr6YHz8yTHwY3Y7vVkPBlTHVK5f9" +
                "4h3j2PMRGCSrvuvLkeE0fxM86n4blHH5p8YFRuXDI35XcKHouM" +
                "3u4LT5KnLfpyv3kWEubVeB7k2qo8LhHx8ZA/Qkkc12nW7RiJwv" +
                "gxQi2ypifP9uG3rW3UeGm8BDmUKIP25CmUEJL0XMITyLk38sp5" +
                "yQNpXITG6cbuWhCS8NAn17e0sPvD9STh48hjt9ZR81cd+TMD3Y" +
                "PfHf6U8sQe5Tz/wtad54tfRB/Yq+/5Mz+wheelTX8//wE8L+3s" +
                "cl7Kf9T3fXJoPItf/v+NZz4a8vw5WZ2sQg4lyqCdfAslhCQ9l/" +
                "AEcu6NvDZ7tPBAGhehcbqxuxaEJDz0yfUtLez+cD1J2D1nlcce" +
                "74fkrPe9zp2kQsb7Z74D7Z9V34WDY7APKNPssU+zR9RNm+0L3I" +
                "9+velVebt3zf1qDm8ZWv6fMv+5Iv98p/3zXN+/jwt+Px8Ps3/G" +
                "/9i6/bN4sW088xf675/JrmQX5FCiDNpPJ7aE0NSarss2JWIgjm" +
                "q1/Ad9cHaxKnf5OTlSyqTetqVIbG6Opbjjf/t6K/+BT4ylrv8P" +
                "/4rC8w==");
            
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
            final int compressedBytes = 2226;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02MFFUQnngQjZooqyD/LsiiEKMENjHhMtM7nSVqYkhYl3" +
                "AxJtyMCTGGi0hwtp87OwuBiwdJOHhQIpsFgpGoBw3owSjIuiIC" +
                "GiKILKj4Ew9GPGj3VFdX1Xuve3p7p2cn25v3ul/VV/V99eb1z/" +
                "RAoRBsxX+hweZ9gEe1jQVtAyRh024yonQz2U+WWl/arFgD2uI0" +
                "InejGpL9cV7vlN++C49/8tuYd/XVl8LxtdqL9f2fEfpkoVA5HY" +
                "3CI2883E/47YbfvgjH33q/V7rrR59HMV9Xxrxzpb8sSi5433s/" +
                "eBe9695X3jfM/rPf/hDIM96lQmHgTWE7y47Pe5d9nSe1/Ffq/a" +
                "/VQ5r9y7g5q6xOs1K8X7zfaOTecG9AD3u0wbi8Fi2EJD+38AZ2" +
                "no2yhmvkb5mBPLjnKuycpnYzgpCED7hlxTwDKdCZJR8plmjbhu" +
                "vTP9u2Wz6102k+NXdOiO6WdnVHZYy8U9nk+rToPJmsrLH+dOtT" +
                "zVK3JyPYfL6SqrIJy1mwMbZOfz6r703ffL42nuos7is0bWPzuS" +
                "Pz+uxIXJ8d07g+O9Lqz7Y+Kyv9Nr+y1DqfO9NkrHT6bZWm507f" +
                "tsacT9+yHLxT3SqLGvgfi5mpGO7KMkP/6knpeVx7jtgDDfv6fC" +
                "rure83SwSMrc8le9jxZonjvrjoxhiJQv2oME5ZVMmeLPx6tA2l" +
                "wrWlHvbb/eoBXJ9qqbMmzfmu5rPj5cLzEKxPFd0D1ILKmFoSc+" +
                "asDPfzJn++q8XsuNN2vquF9f5BvSY1d2rPS2qZ6kpGODXzKPp8" +
                "NiWP7ZGlTRJX/ayQ24ZMccoG7y20eKtsjWZl0G8jQu0GTf2GmP" +
                "kclKg4XPZN6jIVxSobmZw902faDU2fT+Vf052DJrLU3fAzuWgy" +
                "NFIwuYhAF8dgDWiLi8d6dL+ssxF/4xlgmZV5FOV5xhzbnj9lZO" +
                "kZGed8OOXVGZsBmXSlk+OuTjRvtdL7EMe4Ipf6ksfRyl4jUaW+" +
                "Vl2tkCmO0VldaPHm7DaPIrX9yWN7ZKk/DpfDfPYnK1NrWz6fu8" +
                "yj7Dny2ryns8Wp7jxVlbvL3dDDHm0wrh5DCyHJzy28gZ1no6yc" +
                "lzKQx0TEcZrazQhCEt57Vq+YZyAFOrPk47VwtNvpdkLvdkbftT" +
                "pxXNsFVrIQmsdxD+Cd50yb5ODRhNE16Jw2nTyLzsbzggWV6blJ" +
                "KVfIq5WMUjFxWb7hjkbfN3dnPt+3Ftp0a70y93g0n69nzVG80q" +
                "7z2XplA9H30dobmVVrvyrU9rXNfB5q0XPGXmjY12dhhHslNk2+" +
                "LD5EpOMgFOlPZkBPcv44fj1aR6leev+pnjLef56xPG88meb9Z/" +
                "Fy0vvPprxlyPj+M1DW+P2nemKq7z9DtqP++U5n6VU7Qrs+TKRB" +
                "Nf0qfz3j+X601VcYeh/i7oWW4S76Qu7zuTdbXP7K9I2d79cyX/" +
                "UPtu39/eA0zqfl3x2Y7+et5/vhEG39Pa4pTyEZf49DZSnO0yb9" +
                "XqyiXzVrqf5djfV93fNt+zyfu7KBu+Kun8O3ZM1Z/bhd57P6Ua" +
                "7vQ1aVV0EPe7TBePhWtBCS/NzCW/BXHOXZKCvnpQzkMRFxnKZ2" +
                "M4KQhC+O6hXzDKRAZ5Z8vBaOTl6famdhxm0Dt7X0bDjhM86Ozv" +
                "dZFsTxVHfRt9v2/p6rsnJ/uR962Fc/xVEwHp5d7gcUIaufoB8t" +
                "egv+iudhhHHEQbyUgTwmgmflFlO7GUFIwhfPyyiZgRTozJKP18" +
                "LRyc9Lwwszr4If23Z95qqs3FvuhR72aIPx8HK0EJL83MJb8Fc8" +
                "wLNRVs5LGchjIuI4h7t07WYEaSAlxQN6xTwDKdCZ5VzxWgR6XX" +
                "kd9LCv+9ZFYwcthCQ/WgYmyIPe4hGejbJG/CIDeUyEnZMyyywy" +
                "gpCELx7RK+YZSIHOLPl4LRyd/O/rnJctT/v70rxfCiLb8/2SrS" +
                "bz/VL670fy/VJ5S3mLsz/oYe/zhaNgHBxDD8igJz+38D3EBkeY" +
                "jbLWK9rPc5AHYgIvHBEDjTACcxCf1ESVQZWYEZTwKJkBYgHFvd" +
                "xPlugz2k+RSfcjZ3vmb3Xb2/V+lK+ynps9N6GHPdpg7DyCFkKS" +
                "n1t4AzvPRlk5L2Ugj4mI4zS1mxGEJDzUZOaWEXo93E8WPo9cuz" +
                "bH/5lHk30f0uBT3DB96zMdd/WfzNf1hPtR9WyqDJb7Ud3elvcj" +
                "b0ea+5F6qzm/d8j5VJbvZt7dabJ794SrYZt1jWybxvW5LS2qWS" +
                "orl6L5fCefmoY6pm8+B6+2mpHN50hO83nf9M3nUK7/3ttd766H" +
                "PtiTDcbVC2AlC6F5HPcA3maTHDyax0kNOqdNJ8+is/G8YPF2mM" +
                "opA58ZvVrJKBUTV+L6PJzTGpkzU893t9ftdcaDPtjXn3fDUTAO" +
                "jqGHvwAvRxhPHsiq2zAKGNDL2QEDXuIAHGJJp8RwVaiAlIEOsC" +
                "GLVE4Zoqf+cemlDFIHVoQ6e071nIIe9vV72ykcOwvQQkjycwtv" +
                "YOfZKGt0/xQZyGMi4jhN7WYEIQkPNZm5ZYReD/eThT0PIPpsj/" +
                "GMCbagOYvBi2PsbSOO4TltDOSBKDsGrZTVhiQLKdB5eQzWxNXH" +
                "a7RlsFkkd+z1892Z9/tRdTTXp9tzPeeghz3aYDw0Gy2EJD+38A" +
                "Z2no2ycl7KQB4TEcdpajcjCEl475heMc9ACnRmycdr4ejk75tD" +
                "c2fg980Teb7/tG3qWPT70aPtetaWMv9/q6F5rdbKrp/vt+18Xm" +
                "99ZKrnzy63C3q3i9tg7DhgJQuheRz3AN5mkxw8msdJDTqnTSfP" +
                "orPxvLwmMzcp5Qp5tZJRKo7QK9wV0Af70LcCx0OLwEoWQvM47g" +
                "G8zSY5eDSPkxp0TptOnkVn43nDmhaayikDm0+jWskoFRNXsBWX" +
                "BH80guZzL9Vt0AiBe95zOx/rHEkbYiVLXEZURbp0D9mGOm35uc" +
                "1WMWfklRi6/geLnC/3");
            
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
            final int compressedBytes = 2314;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXF1sFFUULiJgRBGV2kg1UAoaBGOh3UaDkcLMoGhifDRREq" +
                "M0anzBaHxTmW6XUKc+86QJRg0vPmnABxNiVDBqoqnRqPEnjWCt" +
                "pRRREBV/ZvbMmfMzd3Zvd3c6xpncv3O/c77v3O6d2ZlF29qiY6" +
                "ArOtviI+rByLtF26AQAlteczsfa45aB2IlS1ZEVEW69AzZICcd" +
                "n9tMGXNGnkm9PKJjaBx7lbfacjm8eW2FHflye0e9o1BDizYYBx" +
                "ulhaOlH7WA54XHAFb3R5M38ktGikIWjZEIzqYVpLm1eoyvmSWf" +
                "Vpygj3hHoIa2OncEx0GvtHC09KMW8LzwGPF6/m7yRn7JSFHIoj" +
                "ESwdm0gjS3Vo/xNbPk04q5Z439/nZOu2JBgft9zrnZer6TU04X" +
                "FLieuXIPlAZKUEct2WAc3AZWshCa+/EZwGtbsJlzuH9Jb+4nNW" +
                "hOk04ehSKRL9VRq7mlDr4yOlvJKBUn6P6BfqijNp7rh7M8FuwA" +
                "K1qinhxxK0bjY24jDve49OZ+UoPmNOnkUTQbjwsWzS11sPVMZS" +
                "sZpWJEO/ucfVBDG83BKOoHu9BCSJrnFl7AzqPJqGFO0zIC2tGH" +
                "49OnjCyjSJWEJHzELTPmEUiBzlbykWKJNh3Oy9gLns6eo6M8YY" +
                "Pih/uD/RWpXqzZ+s2Gu6G7zw1hWT60iiz+k8l6jlhFCJ8RhtYZ" +
                "7L1hKaWsa8Kcfm2B7mvrzPdkrGcG91B3yrJhVnputri/v5/P39" +
                "D9qbj7+9xzs/X8IKecTjUfo/Jhcdw1rjIHnANQQ4s2GAe70UJI" +
                "mkdLeYJmKBaPJqOGOf0sI6AdfTg+fcrIMopUSUjCR9wyYx6BFO" +
                "hsJR8plmjTwa6fe23+Jun7Ud3PyD8F7vecuaP7UeZ6Ptd09JIx" +
                "p/PN6x5+oc78Sxnred7mfjS8f7b3ozj6Sncl9qQtKsEoWHFMqP" +
                "SIMDwWZ2C2kzgDXiYV5Ik1nrUy4Hp5Rpxbc5miptcpi1VzG/Zv" +
                "0guez7gXfNnknjthcb/5wgIzxvqfheUre+7K53Vif9pQZh1uB/" +
                "akLSrBK2DFMaHSI8LwWJyB2c7gDHiZVJAn1njWyoDr5Rlxbs1l" +
                "ippepyxWye0/4c9PXz/9y8L6ynA9X/Xbq5arwtJZ7T0VY9ZU6+" +
                "vjUXgN9teH5cZ4zL5L+yU/9U3X/aM6s82/w789bLf7dxnvi/f7" +
                "O6rtA9X6Qf9h/xF/nq/eD/lL/KVxb1lYlrOZFUlvLef2N8bW+N" +
                "cc/1Z/s7+19nr6d6cs94nRQ2HZGUbvdruhhrbK2I3j4DW0EJLm" +
                "uYUXsPNoFDV+Z7ZQRqAZN7kraAbNmdae9iAk4SNumTGPQAo0s+" +
                "QjxRLtTXlTsaU9eUNYtUUleB1mcYy1aUQYaila8IZgn8QZ8PKm" +
                "dt+jVZA2rPFU7zOntAqIsnsFIbmPO6lVmqKm3pqqebddWnj2Ne" +
                "5Hb+ZzP/IWFXc/Qu6c7ker3dVQu6uD96I+jqJ+cAQthKR5buEF" +
                "7DBCP24Pc7pQRqAZbLkKM6fUzhFar8RH3NxLRiAFmlnykWKJrv" +
                "N+yer3jgbeL83Y/r2Dd1v+fmlmrt8v+YeSbKyeJBt43vylwOfN" +
                "XLmdw85hqKFFG4yD79BCSJpHS3mCZigWjyajhjmdlhHQjj4Sb+" +
                "I0aU97EJLwEbfMmEcgBZpZ8pFiia7zvu77nD4jvxX4+cyZu+b7" +
                "kGPN7veM9yFTBb4PmcrzfYh30DsINbRog3FwXFo4mnrlCT4GPC" +
                "88Rsw6X7JyP9LFVUkFGiMRnE0rSHNLHaRBM0s+rZh71tjvOf12" +
                "5R4rcL/nyu0d8g5BDS3aYByckBaOpl74+WRjwPPCY8Q5/S1ZuR" +
                "/p4qqkAo2RCM6mFaS5pQ7SoJkln1aMaGfcGYca2uq9ahzHwTRa" +
                "CEnzaAnv78kMxeLRKGq8nmdlBJpxkr3B59OcFFlGkR6EJHzELT" +
                "PmEUiBZpZ8pFii6+z3nH4LdCcK3O+5cg9MD0xDDS3aYBzMaAuh" +
                "aST9eSEGwlRz+hNjcG+ty8TJkdIm57UvKdHcHCt1p7OVJ6I42v" +
                "j9J/lXDMHZtv/dUbH6rlYebSHjyWRv9ObzvOkVuJ523JUW/kpf" +
                "SZ5wRxf8hz9nDf6bncppK9SZBq+fpwZOQQ0t2mA8ulBbCE0j6c" +
                "8LMYwuQkz1c38OY3BvrcvEyZHSJue1LynR3BwrdaezlSeiBHpm" +
                "YAZqaKtzMzj2HtcWQtNI+vOCvhQjWYsZHoXPmOb52BRDIjgbzp" +
                "CSKCdTbFoLjJ7OVp6I4uja7+cr5/LZqyNri7tO7N2bZ/QtvVt6" +
                "oYYWbTAeuU5bCE0j6c8LMRCGrOTFZ0zzfGyKIRGcDWdIyfBpc2" +
                "xaC4yezlaeiOJo9lbpouTdSvLGyRvKeOO8qrm/4p4l9TH+vRaY" +
                "i1n/irB0WNzf45z8/jqx76w5+9gs3uAlz0d7Ls/pGeXrAp+Pcu" +
                "V2Jp1JqKFFG4xH1qGFkDTPLbyAnUejqJyXItBMGpHFmdae9iAk" +
                "4YfP6Yx5BFKgmSUfz4Wjk10w6A1GNfSxN7Je7JRBLISgVvdT+2" +
                "wQ/OojOcLEIjWTMpMuWUft8DPcV6rXufBCectMLK5uy+I173EM" +
                "vxE6PVaf+55aaLsYOe3IntYjra+fHTldw74t8Po559x7rk56y3" +
                "PK6ZsC13POuYd2xZ/3PqfPsAv6rPZKXy20XYyc9ntf65F1vnkl" +
                "v2+OXtL03yb970M+Ce9z/bb+o5dmvtXaX4f5o4zno53W2je0fD" +
                "2vMbB83Px6us82r7LR9bTnbtV6Jvu95Bh+PXdKVnulVAttFyOn" +
                "/V5qPdLu8+l0OYb/24jTZaWlKws91G6OO+u/+vYG17Or9UjDd+" +
                "Nt3jbqQymPOZskJirDSwGLePLj/eH4SZUiRF4472zifFmKeEQT" +
                "mmKAHpMuWUdtpIi0SPVyFKmk1ZA89dSLz2fyX+iMbMrpG9lNxe" +
                "13eD6aywPX0+l0OkP+R9Uu6LTaK53ZaIjb7DHS4NUti3voRVtk" +
                "rWPr4q2LoYYWbdFZHhtdKS2A0KPyhBxTIQbiqN5jfYzB2bUuEy" +
                "dHSpuc176kRHNzrNSdzlaeiOLotn8BFcQFFw==");
            
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
            final int compressedBytes = 1478;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWluLHFUQnoeA6HteFBUTBVEEL6jxIdg9zKgBJS++6IMR1z" +
                "yYTYjJrJtkNZedybRpnHXF4GNAffMPRPAaXRUFWUWQFfIDBsSf" +
                "EMGuPX266nT36a2uPWdO3B7OmZmvvq76qqZOX+jpdKrb8g31Pv" +
                "pjsqfD2EbTTsut904n2OY3djSIBmpW7xqDV1bPvSaiGOVvo6n5" +
                "HQdGwBibOZ3QPmj0sq66mJRpYqa9vC8qKcemXFN3NVvzpVmUrb" +
                "b+ffDCb2pk9axgauhP2mLOFKffMUZvULaWN801o9g8alWoy9yf" +
                "Yr1BnX+K1WVMI9JMtsoDtiTSnyb3e1rvbwZc7zOPnXSL4+cDnn" +
                "K6ELCeM4+9vFDU8yH33ofns5wWtu9n9Jewngtef63V3qqa1bvG" +
                "4JXV81GNIBPtFKFD4dQbes2jnjM9EMsqVdYUs6q9ugcyCf9cOW" +
                "PqARWUI5vxULHJru2g4ggzecLT8fOtgOvdc+xhdowc3j7cU7ve" +
                "n2J5uCcbD9bgj2Xj8QoK59i3Hei+cwv7w5Z6WmIP91aQR1rp2c" +
                "c4fh5035/D3VlOZxzU84CwP8/MekUsF0fsySuufE4OGfU8HrCe" +
                "x0PVM+vPVz2d388GPH6eDVjPk57W++mA/Xk64Hpf9FTPUwHreW" +
                "rW9by4VPTneU9rbingel8KuN5r7s2Gz2+zr6A/h/7788LdlnoO" +
                "A9Yz8bTeFwOu98WA9bxUk8dvbepZcz3/e5bTye2rHH2yRb1/td" +
                "STHbvd9TznfNR91sP10m5HfoT96SOnLZQW9+/p/s6O28b7wtWz" +
                "+8LOq6fvnKIvYMBMkbw/u3wPXNzGluiW2MdPc/kuVJau51f8nN" +
                "9TB8cw6fEzfcbr1e1Kb0XN6l1j6vsk1Qgy0a6R0RQt6It6Q695" +
                "TgdMD2jprVBl9ph12qt7IBP56XPljKkHVFCObMZDxSa7uT/jPz" +
                "30p6N7LunzDl5Onu7f3/Oz3kNeL8Vjd6y29YzXnF2lHLZa5oTH" +
                "wYPCeq757Ub7844s9jWWh9bPO5I5B7qFzztsOZnPOy5+5P55R9" +
                "YDL+7A6/k3wvXn5H0//RnvCtifu2bdn8nRop6rfn7D9GX/fZjM" +
                "W2K/5Pn+6GcYMFMkvzd7ku+Bi9vYEt0Suy2neqXt9TSu9w/8rP" +
                "fo33Dr3Rbb1fP3aAMGzEXEjZadslG/Tz0OSDrvoD83ZPZLV7j8" +
                "dnVQ7P56f13N6l1j6vvkcn+9e13hyNB2jYymaEFf1Bt6zdfcdd" +
                "MDWvQ7VVEXU/ug2qt7oAZUAvuZGavP6A+VUL9mrVCxyW5e7939" +
                "fta7k+sS4Xq35eTv/zZ4fmfeSU87N+FmPb8fCXf9mR67eftz9I" +
                "+sP8djz+ejqzBgLo6sxeeY9d9T5aEe57Nbn4+uyuy2nFxo2vTz" +
                "OQyYKZL35xLfQz3OZ0t0S+zjKzJ/LtY700PDeq9Bs/Wengh3Pn" +
                "r3b+56F/7OP8CAmSLtPXBxG1uiW2KPX+fwgSVTGa3BiNZMpL0H" +
                "Lm5jS3RL7PEchw8smcroaxgwU6S9By5uY0t0S+zxaxw+sGQqo5" +
                "9gwEyR9h64uI0t0S2xpx9y+OllqcroGgyYKdLeAxe3sSW6Jfb4" +
                "EIcPLJlKv+f3+uv51MX/67yf32X/B4u+gQEzRdp74OI2tkS3xB" +
                "4f5vCBJVPZ+DzuXlf9mdxqu99Mbpltf9pyctafX8KAmSJ57Lv4" +
                "Hri4jS3RLbHbcqpXKtD1PQyYKZKf5T7le+DiNrZEt8Se3Mbly1" +
                "RG38KAmSLtPXBxG1uiW2JPP+PyZSqj72DATBG19Y/wPTTj6MnG" +
                "luiW2G05VfkyldFXMGCmSB77KN9DM46ebGyJbondllOVL1MZ/Q" +
                "gDZorksef5Hppx9GRjS3RL7LacqnwXKjeP2MUvGA84/P/X8w5e" +
                "Ttv4nX+BATNF8t/yGN9DM46ebGyJbondllOV306lZoe434zvCH" +
                "e/mX7s53o+f370H0TYnDg=");
            
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
            final int compressedBytes = 2084;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW0uMFFUU7Uz8BEk0EyEkBghKWKiL0RiDYcXUdC1mhoURFc" +
                "PCkLgiuiAzLggRzFQ3xVTixmSIBCKRhPghsBEILli4MC4GA6hL" +
                "XfhjRhO/0Q2JC/v17dvn3vdedVfVVFEEq/I+99zPufdNfZui0Y" +
                "geazSih6JHGv1t7vVGri16uNMe9+BPea23NBrj6xsr3qINQ/RP" +
                "+PH5d1PsNzdK2bafN830EqGteTl7BD+e3bpI3kX0aTWVkZN/i1" +
                "/LZ99azsvQvKtR+Ra/Wh932nq+tVDNepaxJW8XW8/xPVmiZ7Py" +
                "HudXTDO9RPJH6ORwwI9nQfNu4weGxUjT21mm2+fLkq23L5pm+r" +
                "5mMed6Lvp9/Hiadb7tyNphMdL0ybWs9vmyZOtq7+/R0777e/Nq" +
                "fff3NG73/h49mSufZ27Fevqfl5Kz9a3nkaWKn5cumGZ6ieSP4M" +
                "ezWxfJu4g++bZYvMx5XTTN9BLJH8GPZ7cukncRffhGFvs0qyIb" +
                "zvfxsdv3eanwk8FYtfEHXT+TX6q5H8Wv1Pi+eSzL9bN9tNj9KD" +
                "gYHKSeRsZIdhFYQ2otaxkNDODo3mOvcwzJLjetlzIsNab1ti8y" +
                "sbmlrc7brVbvWB9ZcS3PS3GNz0txlc9LYRIm1NPIGMlAYAk9I6" +
                "1laBBLRkPU3jXsQx0BGh5lFj5OX+6uh8yfEcOtK5YRkIHNrPmQ" +
                "sbYefD+q7J5wusb7UaXc8Ym853t8vIT70bH6zvdwf5bz3VgVOt" +
                "+vhFeop5Exkl0E1i5iz+GLGL1j5IyOAA3GYZwu5s7hi0xsbjtj" +
                "mYkdS+86X8yd8/3fgcfneyX81nahvvM9OV9t/Dru7+MflXClOl" +
                "3sfE/jLuf+3jzVPEU9jYyRDASW0EtENttXRu3V9IGOAA2PMgs/" +
                "p5u76yHzZ8Rw64plBGRgM2s+ZKytb/39PVrbYX6nhDiTxfzK4M" +
                "55Jp1t3MFbfCaT1bmCf61tzW3BDdPT2Hlr6klGNvr2g4QbxPTQ" +
                "S8SM3ODL0ZiJZsENGSO5yBryMVqagQES8myvYa2sQXuQJUXlCl" +
                "ANMjd7dBK+7COZuRKZR//NFysw2ZyknsZupEmWgcASeonIZvvK" +
                "qL1cF3QEoemfxTaDzenm7nrI/PvIgl2xjIAMbGbNh4wt6+nmNP" +
                "U0dnXTLAOBJfQSkc32lVF77Ed1BKGZ7mdoMdicbu6uh8y/jxy1" +
                "K5YRkIHNrPmQsWU91Zyinsaubor2idGJ0eZUey/hsGC9RGQjXE" +
                "ZDVGI3cWUEaHgESxpn8omdu+uBHJCJ4dYV07xzvgsPux4ZEYhY" +
                "zymZu3Xn3NWretXEqk7el8u/J5i4dW1p3NHJMqIHW4It4bzpae" +
                "y8NfUkI5s59bQbey1JhPyBdd/D5smSmBhDDLKmMZxnra1nG+ax" +
                "Y8isJBtnRD7Mor2kLbORldRqBuQBa553/mIjcpwY6R+fIwZp72" +
                "W9tnYRqfFh0PlQ8Kfppd3ESPLp4Kw06pvpuT4+iSNb3XLW3Nrc" +
                "GpwwPY2dVe5JRgZClqaHXiJyJF8z42jMRDOKCxbWkI/R0gwMkN" +
                "iDLcCnc0JlxA17naUdgXzJSmqlHkj/LD8Bz7zvR9G1XG8x1vv7" +
                "4dXR9Q7/Zyu/UrVODWH+IuUKl5k72/v74XsPiytyuDncHNw0vR" +
                "m7fD3JyGbe3gvE2GuJ/aGhqD6MZxQXCI+EGi04SM82yDO4mXyu" +
                "MemhM6M8CGMWnbnZo5Psy6sgtYig8+CKOM+utCTHYCnaw7JBks" +
                "VAfVERON9XaP90DLrA+40G86fppV2w1PpucFYa9c30XF8/iSNb" +
                "3XIWtsIW9TQyRrKLwBpSa1nLaGAAB1B4SY1PL2WyDC7ZmNbbvs" +
                "gkuOSPjbXo/kp61Vet3rE+umLnurFH/RXOlf8MWEbMojHS/Mqp" +
                "M/I83UZ3F4p0X3829Avg5MsSf/+7J5/9kTVVvi0Eo8Eo9TQyRr" +
                "KLwBqS9pcNDLDpzuc4hvS28/JxSkuNab3tK2qd88fGWnB0t1q9" +
                "Y31gXdPx+XV9x2fyVZXHZ3NHcwf1NDJGMhBYQi8R2WxfGbV39T" +
                "6uI0DDo8zCz+nm7nrI/Bkx3LpiGQEZ2MyaDxlr62A2mKWexu6x" +
                "O8uyi8AaUmtZy2jsixi9X7/3cQzJrs73WT+ntNSY1tu+yCT5xh" +
                "8ba8HR3Wr1jvUR1jPBDPU0dnUzLLsIrCFpf9nYFzH6azEjo0iN" +
                "Ty9lXwxtIdlYg0ySH/yxsRbdq8KPvmr1jvXRFTtXpH138r8fhQ" +
                "fLs/Lc3zcFm6inkTGSXQTWkLS/bGCADVB4SY1PL2VfDG0h2ViD" +
                "TMJD/thYi+56HvJVq3esj674f3Z8HijPKu/3IcGjnn9Jvbry70" +
                "NKeU4q+D2Yryb3+5D442Lfh7Rfbj9na9ovlXAUrE3TtHcP0mbf" +
                "2juH/Av69byZ5bfyZNVZz/aL7WfLXs/284PWM/m1+vVs70p531" +
                "zKFP2FgsfRBto1VsLxuWGQplqGwfps3EUzDDfSrrESqt04SFMt" +
                "w2B9Nu6iGVZ1/RzAuLukODuL+SW/V1pdRdfPcPXA+9Hq+q6f2b" +
                "jLyLDM589o/8BfYd6v7/mzWu5gLBijnkbGSHYRWEPS/tTCdcGY" +
                "ZIBN96+/jmNIbzsvH6e01JjW277IxOaWtlgDyt+uVu9YH1Re0/" +
                "FZ4/+XufXcZXyfHD6QutJvDtLm+IvNlZ1ZfivPW8Q/8U/x9+Z9" +
                "M/6rjy0VqM563wzvp/fN+G+H8WfSrnSLbwzR/5GyUkO549/IKv" +
                "7zNjo+1xfXVvx7yPryrDzbf29sTlE=");
            
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
            final int compressedBytes = 1731;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW71vHEUUXxoUQAiQQMSICAhyARESyICgc2bPBaLAn0AICC" +
                "kEUJD4KCKFIwXcneNDK5oUNBSRFVxgiYIiimQpRWyRBiSKiBQk" +
                "TSQK1/AHILz7/O733szs+W5vdye+Xc3Mzvv4vd97N7u3u/ZFUf" +
                "vZKGo/1j4c9bbW6Wiorf3UTjvikU/ttJcc6WQUNX6MRt7ah/bQ" +
                "P++X58VuPy1ny9/vSF4Yis8reRrznntU3lYWZlGcKnLqvw27Pj" +
                "vbQ32OX5fDst0q5rfyeZW1M8fNceppZBnNXQmsXYk8nv4JCBJD" +
                "xoUXNBj3iunK3GP4ggkx87MHNms0lt41Xxw7n/wXo39O3bv6aZ" +
                "P7o2Bb9+FK1+cZc4Z6GllGc1cCa8y0P7XpdfLkCLCBFF5S49PL" +
                "uQ9DW8horAGTlJkPG7VgdDdbvaM+sM6OV+VoVpe/4nkqoV5kuu" +
                "p8JquuxpZpnc8S8e2Ivnh7s/Jh57F0ESjGYHnnRe+dEXf37M7v" +
                "nJv3FVr354trB/wWvF3wjMyJ3b5Qyvl+xByhnkaW0dyVwBoz7S" +
                "8bIsAGUnhJjU8v5z4MbSGjsUbk+rEfG7VgdDdbvaM+OmPnk/qu" +
                "6u+E5IFx/T7ibSaeiXFMbafiX2obbrDAqI+1J0eQMaSfn5FE9F" +
                "kDg1m5vHSfjikzcNHs7Vxk03Hy2Yd43jSXR75OXS76vJkXWz9v" +
                "ZpJCz5tuPbv3VF3P5Mlwz+9553s59TSLZpF6GllGc1cCa8w623" +
                "qOhgiIkR1fYgwZXa2iRX9MaallWm/7ilwv+bFRC2boZqt31Edn" +
                "POr30XDP71n0KyOf71eq9UweL4bemGvMUU8jy2gOCSyhZ0lnGx" +
                "pgSTSg7uZ0VSNAw6Nk4YtprrrcXQ/JnyVpbJ2xRAADO7KOB8aW" +
                "9UJjgXoaM90CzyGBJfRSIpv07fyLGR9FUfy2RoCGR8nCH9Pl7n" +
                "pI/ixJY+uMJQLbJocbC51/JK6OB8bgnbX5xjz1NGa6eZ5DAkvo" +
                "pUQ221ei7t53/KkRoOFRsvDHdLm7HpI/S9LYOmOJAAZ2ZB0PjL" +
                "V1vBVvUU9jtnq2eA6JsGyyHj66ZXtzd9YEkkBtagSWczxtL1Ad" +
                "npK76yH59yRNO2OJ0LNtUqaahx0HWw9rM96knsZMt8lzSIRli/" +
                "Xw0S3bW7uzFpB6Hi2JgeiIZ9kD1eEpubsekn9P0rIzlgg92xZl" +
                "qnnYcUQ9NyX3Yd7PLz84yPfc8kO7cc74tH5pPdtgsVOrsljGvw" +
                "ync++X8hFIEy/WULccDojdL8/kmbr+fhSfHSibs/2sB8OoqM4D" +
                "8y/GMsTze/LcuD5vhqlnvBSunnmxUc/ln/dbPc3GyM+bG4XfL2" +
                "0Msj67j+6neiYvh1uf354cw/N9NuD5PjvQ+py8M+vpkabr89Vw" +
                "9eycGnR9Ft061n8X4P8Zui9G+3b75omcbA/UzWTY/w/xvf9sr/" +
                "e9u3sj4P1npbHNQXOQehpZRvPseE1K0rmccXPma7v+awLpYE/W" +
                "w5DeNi+NKiVAtlHsaKwRua7lYK+pGqxxDr4IMg7zxXH++jRx4T" +
                "W+fqdeB4rnNND7+aXGEvU0sozm6bG5SHK2MBdZDx/d0p29yBqo" +
                "WUYXJYbUMAPNAjZSwsgaRXsgI+Sa+umMmbH0SGfSUiLK+oCt5D" +
                "7M83spn+Lf4dZn1bH73S8lb7n2534Y/f6zFN4F75eSN6u8n2+c" +
                "aJygnkaW0dwcYAksoZcS2Ugu0YDaWyUKARrXIi+my931gCXsKS" +
                "cXW3vY+Ug9JGLVn5Dc8853c280dlv9OdVw/bwV8Pp5K9z107et" +
                "/DH69bPxV7jrZ15s6/n99fJ+L8P1jCfjndyXrefdeKDvE7byWR" +
                "PuqFtyrODzUU5s9/9pi7A002aaehpZlu7xRDxhppN3IUmPUpm0" +
                "t/1hlXGaIA1sUhkwpLfNS6NKSfaUe1vLtN72Ra52bLZtX5A1QA" +
                "6+CDIOW+M4wPXzs4DXz0pjd+c8siHfTtrvQ7p7vnEw7wT8fq80" +
                "9szUzBT1NLKM5q4E1phpf9kQATZZTscYQ3rbvHwxpaWWab3tCy" +
                "Z2bGmrebvZ6h310Rn3eR8yW9EamQ24PmuPLepZ0btCE/D9Z17s" +
                "5P3q63n0N0/cj4pgnvswV/NBvfX05VTX+jz66/g9b9afk6jnNc" +
                "/6/GSfr89rOef7qRrqeX0M1+f1cOsz+XT86nnuVN0Rq30+aj9S" +
                "Es5rxfym/yvPatj1WdWWnA63Puv5/WZ+PVcq+HWs+b2EulwvN3" +
                "Y5v9cOsz7NjYD38zfCrk9zs4KcbobDyPOrIs/arp8Bf4/QPVQp" +
                "/P+xyemE");
            
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
            final int compressedBytes = 1341;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW82L1FgQjwcRbwp6ETzsDF4URFkEvWh3JjnsPzCgVy8eRD" +
                "x4WBBRe2TGDV4Ev0AYXKXx4+JBBUEU/EYQdPQqKMI49MW5OSsM" +
                "sklXV6oq7yWTxLy8Nk6a5L1X9Xv1q6qufHXSjqMuncOO4SX427" +
                "G2TG2pm5Hy6e5yGrfUH5P5+vT22stn/dxF8zkxVzimfRbzua+B" +
                "9elZzKfXvHwGJy2ej740sD5XWKzPFcOezxLHz5UW87nSXj7bB5" +
                "p3vVR/TJTP4Gzz8nl6Y/OOn1brc111KG32Nofrhs4ISU4eia1u" +
                "1ZybryoW/ghXzV1c589w3QH9ydWxdJNETa4q6fcSddbZlpKprS" +
                "n4UUWyvZA/O3McP0cbWJ+j9vb39oimPrtlbFJ9KppVNedzJOWa" +
                "+NrvffwMrpfMZ6s61PL5KMyUWx0q30LnI2223xT6bnYokrfVeD" +
                "nx7xLMr1MytSdXPvcUPR/9tvW5uzrUcj7D4+6NXKiby/nMWZ9j" +
                "1aGK5tNv4P27b/H+3VhMtyzm0zh3q8vbVhfzieNWV4fOkiRtJn" +
                "Vc6n+UcyJtq7u0v0t5JaW6XnpfJ8nSZ/s7ddT4OeGuvfqcWme0" +
                "+g/5h2ALLcpgrEoITSM5n6/EQJh+/wLa4LOTfuk4OVLKpD45l8" +
                "V6QW+bcoHW1Wjlh/IjI1aOn/ehHVsztiaspXvVf4uRXVuLWW7/" +
                "gf8AttCiDMYkISTX61aaxRmIo9+/KFlJI+epnByZ9F3qZURMcj" +
                "EZMbfA+fkq86B6zKNX7jcXseee0PwedKWCY9gxe/Wpi2m4rpeK" +
                "P4/zL1m8Xqqdu/PQ+Pn9fmPP78f9463ZaAtteC01GEXjpCTCyx" +
                "GXAJpk/SuzWUACE8rIBqChbc2iNqlHDPIAVsr4DNShRzAHWeQs" +
                "4ic2QHGtZCA/CI19K/X5sKn16Y6Hn15/229DCY6icULSx4uRkP" +
                "SkrG+/B8gBE8qIY5w0bi/WJvSIQR7AShmfEet68dwBXsTH9eOx" +
                "nvzuCeae4IttIhr7YbvAW3ehcwbHkSR4hHqJViVco5ORTicl/j" +
                "Q9x7kLE5+yvZJSXS9tFnHki5v3/K/+V3cu2kIb6gajaEwSQEZb" +
                "0nMJb2Fu1ENryAQ9sMstQAtzIi30SE8j9BMRxCctUmTATXjpJe" +
                "enuYDiWmTgnrjxdQ0iw3Xen4cttH32eRxHffc2yAmBei7hK81F" +
                "a2R1wH9bWiANttwLPSfY4L6rM8gH8iSaJyOGPtkjT7hdmSvyWK" +
                "K11/NHjF6NvQ1jevXzdso+P6qCu9gi8xm8NJDPlxbzmcIdvKgn" +
                "nybq0z9nL59VcFeZT/V5cdb9pv55sX/ZYj5zc/86z4v9aYv379" +
                "PDVZ+V7O9PLdbnU5PRee+997CFFmWD8WOUEJL0KJmYIw3Z4tbI" +
                "KuclC6RREXpOne/qDEIy/ONkxNwCeZBklnw8Fo7Ork/vidO4xX" +
                "RMWe/Tes9zWcjxPi2TbnKcyf0V+F3yfdq0mOT7tKfOl3uf1pvx" +
                "ZmALLcoG42coISTpURLu77GGbHFrZDWOSlggjYrQc+p8V2cQku" +
                "GfJSPmFsiDJLPk47FwdPb5PZhp3v5+uvbf59n78wb+i9tZX5Gd" +
                "v8rNa++yl08jx2vL+fznc65fvN/9Svl0LebTdYa9Pos934zy2V" +
                "60uL8vNm9/b3+3mM/vDcznN4v5/NbAfP6wmM8fZrOXdX+U00Lh" +
                "+6P2f/buj9K4zf3fsIr69Ndm1We61nx95uOuwsP69vfgg8Xrzz" +
                "vDdfzM938u9+AAPXT/50LPcnxjZX6f/x/SG3pz");
            
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
            final int compressedBytes = 1790;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWs9rHGUY3tKLDYI/ULEBCU1QSQTRSoq4jWa+2TmsQa/ixT" +
                "+hVfDSo7ttYrOiUMi1l0BZaJHoVWhBpB5Ej5YeLD0EAzkIuQWa" +
                "g5l5953nfb/vm+lkd2Z/zfJ937y/nuf53sxsZjep1dzjm69rAx" +
                "/mq6xI6+VaKUerWbayKo5vfy2nn6u3xrWf2coq6edvx0q/LKGf" +
                "D2tjegxfWSn3++Wxvd8vT2I/V++M7f1+ZyKvz0tje31emsjr8/" +
                "bYXp+3q+ydaZomzbSyr2dvmebmP8rTdC1d37O3TFMyICc532IM" +
                "WW3r8nEis/1Y+3TcrhV73fJjoxeswN2tfnGWzPb+5L9Ld3Wj75" +
                "/Tjf6j1R7D5zYdebb5+2AY3uhPI+xnBnfrZinoV81VmmllH9nx" +
                "edxPeJANS9fLAQbkJOc7jCGrbV0+TpmpfTpu14q97vixWzdlD+" +
                "y92RqAqbO9PV53z078c8qt3Nwb3fW58dKwGfH5yFzpu59XamN6" +
                "DF+Z+cE9yzvae3kYXoa7A2u8O/zKvhm/d89O3M/cSnNvYI33hl" +
                "9Z5GhcaFygmVb2kQ0PMhFnT3sPEWBJNKASa/iZRkCEV6nCx+nT" +
                "7lZI/eyJufWOJQIU2MyaD4p1djQfzdMcr3SQRTbP7EE2ztp7Ms" +
                "JVrg8c4SeS1WaTjDanT6dEsdnsXbjcWgc65O5WM2rF4Br882b7" +
                "xL+tw09H9/uoWm4TmYhmWtlHdnLelZ7YlhYPx+726rsCKUp9KY" +
                "astnVpVOkBso1is3FE7LWbgd1VPejyHnwMkof14jz/+jTW99mm" +
                "0PfbnOXLNrdMCd+R94uRVaf9sWVK+ia/9Z9C3rZ4twup3s7ONt" +
                "vFMIoxlFWnP2/GWf0wmDWzRjOt7CPb9SAblq6XAwzIiY/OM4wh" +
                "q21dPk6ZqX06btdCyca/fmz0gtHd3eoX+qN3nH99jucR7A+/su" +
                "DdvXQ8ZlvzvvfPzoybv/6Xg3DueLzlQX7veCw73tdL0v3aU+Lv" +
                "+P2dMxn5C+pT/vG11nr3RHreL9DPZ6evn9fPFenn+s/99jO9D3" +
                "Zp8Jx15loZ99VuMcuPFew+jYP0aoVZen3c+fhZ/HZ1Ds9DGjy7" +
                "0SwrG6+fGGcU40AW9OczcCQfP4vfrnaz8u73yPPefW1tsu/3aL" +
                "/I/X7t4yreP6OVQkiT1c+VIv1MPOX3sz6F12d9dNdn5/kpfF56" +
                "bnTPS9F+Wfd754XJev+s6H5fnb7r07enKp4/ReQw5f6oNnVHtX" +
                "sK2kGbZlrZR3b0ge1BNixdLwcYkAMvqmTEF5e2D0NnSDaOQEm8" +
                "Jx82esHo7m71i7Nkdv71WdXReXV01+fw//6+8Set4UJ4/J7SOa" +
                "uj4UIRDM7yZRPuoEf7cX91xbnLUJkoZbzZcNbDMltIy2x2th93" +
                "WEdx7n5UNh40HtBMK/vIhgeZiEuPHHatRJW8QEDEzcjidLW7FV" +
                "I/e4Ide8cSgXM5S+uweWQfe2OxsUgzrUlskW14kIm49Mhh10rU" +
                "lF8hIOJmZHG62t0KqZ89wS/2jiUC53KW1mHziH5y9lJjiWZak9" +
                "gS2/AgE3HpkcOulagpv0JAxM3I4nS1uxVSP3uCH+0dSwTO5Syt" +
                "w+YR/Uwr857ngz+m7/uQrD0N4/NRcH8K+3m/2n56fr+/mHL/PX" +
                "2fj6rdk1k2yzTTyj6yXQ+yYel6OcCAHHhRJSO+uLR9GDpDsnEE" +
                "SoLTfmz0Iun6ad9u9Qv9EbuumzrNtCaxOtuuB9mwdL0cXAuMtB" +
                "d1iSIjvri0fRg6Q7JxBEqCU35s9CLp5ynfbvUL/dE7zr7fqzpW" +
                "j0Z3v1fL3bjYuEgzrewjGx5kIi49cti1ErX3+f1NjYAIr1KFn9" +
                "PV7lZI/ey5/rm9Y4kABTaz5oNiK3ulsUIzrUlshe343DwiPzI4" +
                "Lj1yoJbRgNq7Ox5pBEQa6V93bAabkzCkdrcCGqAkrtM7pnPgQY" +
                "nE1b2CYp0dHUQHNNOafEN4wHbjPHuQiTh72nuIAEuiATX9FlIh" +
                "IOJm+DmBrFF0BTKR33nb3rFEgAKbWfPJvajsuWiO5njtxebYNh" +
                "+Sl3NwJi09OF+iARVeeHTEVSE5bZ02CuKoxYw9udhQKhVKHbYS" +
                "rZZZ8p7nCz7JTtbz/BdFnufjrNKe599Iv/c10/c8vz4zbEb0cx" +
                "qP4Gx5WZ7vTJ+ET2imlX1kBzPsQSbi0iMH+SUaUCUvvTqhjLgZ" +
                "WZyudrcCmcinPbnYusLej4zDI/vIlbn/zxBN3/tnp1Hl9yHhYe" +
                "j89Y188QjOUJRtntmjLR3VaN5745CZNJJdyTO/fGpdlTJT1vCe" +
                "pNpsjT4EnyftwFF4RDOtSeyIbXiQibj0yGHXStSUXyEg4mZkcb" +
                "ra3Qqpnz3BK/aOJQLncpbWYfOIfnLl/3XiU2M=");
            
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
            final int compressedBytes = 2640;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uMHFcV7Q1Co8nCisIGGRPjseSBIMXCKN4gTVV1LRxmFQ" +
                "UkLD5CViQcCaFIbMgm0J3xr1c4URQkMztjkhUYIWyJARM+sSHA" +
                "gkWYrIy9sDTOJpaQIgQS9erW7XPufa96ajo9M8p06b1X795zzz" +
                "m3erp7ujwul8vlXq9cLutVHrIL++yzEkUEaK7jjOBTMavB1Vxn" +
                "PXjNlE9m8WrMyz3F3HDKDrlbq2gdj9EnyhMyh7XJndB9dlCiiA" +
                "DNdZwRfCpmNbia66wHr5nyySxejXm5p5gbTtkhd2sVrWNoxY/B" +
                "v/Ts7H97e+5x4addUKPl6djLJ8snZQ4rYrLPHpcoIkBzHWcEn4" +
                "pZDa7mOuvBa6Z8MotXY17uKeaGU3bI3VpF61jRxdvF2/4aS0zi" +
                "mHmf2jGGOVMK2WXNSFUKg0qwppCIwIHX5Zrssu2hXd+qTI5Ae/" +
                "Dpanx88CnkfvDdrf2MDw5W4zOJ+Oeq8fkoerjq6Scf/HU7+MQm" +
                "+cfT8TbtwaEocnRLfo7X1/Wd4h2ZZdWY7BEBEnmO8PC1zNq8Oz" +
                "1lGZDRlV2kNWPvcQX718jZR3zHzAAHXtnqwbFDrxfrMsta59ab" +
                "Y7461i/MSxyIcZ4iPCTObGBt1OctA2XWxw6dgteMvccVQBJ+3n" +
                "cs54NVrvD9MCMidD0bdLlYLsoc1ua9dVGO4qHioXLxxdOIhDO7" +
                "46iy8Z5j0Ai8XM111oPXBMfoaRuzea7FHFavreeDVb4G1r9ltR" +
                "GHPlIekTmsTe4I9jprBGiu44xWxTFojL5kq60aK3rNlE9m8Wq+" +
                "i/B6j52Dga5n1K1VtI6h1evlD5YWwpE/0F2vF0b+IEQuzIdZYw" +
                "EVsLILD6kTvMZwLpyCkzrhBRIP1Ze8IKBicfmD4W0wqiv4knPt" +
                "RT1KzOebz5NVZVP/uBJaJxqqpH1zl43bQ+HAToZF8GCErDxzPK" +
                "0x+srSocmfloq1Km2M6gq+fAaxs4+k+DmW6pgVuZPN+rDfj859" +
                "pMtvCsN7O/8tJ//V9laOvj7l96Ob5c38P2GWtdJrdmHvI4wOGR" +
                "uRVVil0sb0THkti+hIlhVDXjGqE1A+xhXsTHuUVVV8XldRExRn" +
                "rQJ8AN34vFXeyjfCLGuVa3ZhH86r989xhNEBayOy1t00XDamZ8" +
                "KLiK6qL3lWUYzqBNTwdtoVq0En1LCKz5e3qs/3sZqgOGsV4ANo" +
                "OV86vnQ8fz/MYa1zzS7swzlHAt7utB6Z+l0lGdMz5bUsoiNZaE" +
                "heMfAZho1xhXUmPiSmKtY5fIzfFd63WTBYH9rR2OcTS0/k74Y5" +
                "rHWu2YV9OH/xNCIBb3daj0ztPxnTM+FFRFeJhiw0JK8Y+MzfHX" +
                "3VxrjCOhMfElMV6zwc1ed7U6tXgbNgsD60I/WZ/O5/ecJ9gctd" +
                "3pU3Q+WD7u/w3RS7121FewrV14vXZZZVY7JHBEjkOcLD18as5S" +
                "XLoHGtYXx8WGbLYl2yf40EbdsxM8CB79bqwbFFJ6/xhLs/qVz8" +
                "+1Kxyf2j829s4Rmf8l5UW13/d9v7m9xu3K+bie8p79eV3+tyvy" +
                "6gprpf91rxmsyyakz2o29pBEjkOcJD4sxmWXlnK7TGIvxhmS2L" +
                "dQkk8Ode9R0zAxz4bq0eHFt06rHVn88P12N0ejvZ+wf7B/XMxm" +
                "SPmfepHTDMxQp0j/x/mpGqlAtU6qzHpA7YL3fE2l4rxRpfpzZV" +
                "dwUO9w/LLGudO6x7RIBEniM8fC2zNj392zIg0x+/u3oFrxl7jy" +
                "vYv0aCtu2YGeDAK1s9OHbouf5c9FzUMYlj5n1qB0xYs2ctW/I5" +
                "n1OlNEajOuvRvG6/7fnZgdVl9uxZ20O7vvUyOaLaxY3ihsyy1u" +
                "+tN3Sfv6IRIJHnCI9w5K8wm2UVXmbQuNZYfFoz9h5XAAm89BRz" +
                "2wrfD+e5E7htxlqxlq+GWdZKr9mFfTiXWZBhRp4jvEptOFM2Zg" +
                "3nzKHnIS4s4VAeYcROK5QDetYTOqu7XVNGccJVlkFqBcVZziMy" +
                "/s61qpXl9fK6zLLWv3td1304H30DEUbbOqxSxYNZG/1fpKpVnz" +
                "0wC/uyGItgNe8g1vbuld8rWz3veIy+Vl6TWdY6d033PsJonA3v" +
                "8V6rMJhjfC2MqlWzioz1HClXVs07qN53v+M7tu6V3ytbPe9Y0c" +
                "Wd4o7Mstavjju6D+fh51MxOAMmHqhVNrA2P59XLQMyurKLtGbs" +
                "Pa4AEvigbTtmBjjwylYPjh36bnFXZlnr3F3d589rBEjkOcIjHP" +
                "nzzAbWsb5hQCZGtGnG3uMKIIGXnmJuW+H74TwidD3vsnf7OLe4" +
                "3d9Ril/u3vejbtqj56ZjX3pv6T2ZZdWY7OMI0NgN79k9BhSgUb" +
                "/e15SD1b2vlKYi8zUfs3lfCydem7HWd9ytPXB9gM6OZcdklrX+" +
                "jfeY7uMI0NgN79k9htaCo/kZOakcrG6+kR5LazLSxmze18KJ12" +
                "as9R13aw9cH+540v26PPGt4czfPtz36/K5Lvfrzn5xuvt1k6/n" +
                "6NW9d//zwpe38+8Vd+d+8uhHu3c95e9DNrueZ65Oez0T/35xfm" +
                "udbf3vQ4rl3q49dl7bXs/8inv/udLpr1qutKPzK904uinMqs7G" +
                "w24ahexodlRmWTUWjmJ/sT87Gv6eViPhLMQY7+uBqn8a9ksGmB" +
                "ADB1d7X5aVI/WzftvGbN7XolevrdjBKl8D9JBSYB1FyyjuF/dl" +
                "lrXu935zLFRHEyHkwjg/rrFDapvdgqCgUccWLINm1AG7MKyRT/" +
                "YeVwBJ+AXfMTPAgXRqfXgdehdRro1iQ2ZZ69xGcxyojo3w98mK" +
                "qecD4/y4xg6pbXYHBAWNOnbAMmhGHbALw0qR0SXvPa5AR8oojm" +
                "zHcj5Y5Qrt1PrwOnQ9m8ryZHlSZlnreyUndZ+95SNAY2freWht" +
                "r7fyjGIQlWPllM34vNVMc1gEcqiFk9BTihvXQtnjbu2hKEYnP/" +
                "nx95/53vv3zeybs0Ml/l1kX3+fzLJqTPbZPzQCJPIc4SFxZgs/" +
                "n4izRn/fyinOxIg2zdh7XAEk8NJTzG0rfD+cR4Svo1bGv88PL4" +
                "2fpTdn9fv8yjNt349WTu3s7/NtPe3E96PRz/fe983Rz7b3ek76" +
                "+5Dsa3vw/XNHeiqrB85lVM/lVYvRAQRWf55SYI1JWHClVGIOdR" +
                "X7snNYw88nvFj3vhceVqfd/aTXe/ZY4u7909O83s/Mtb3ez3x0" +
                "h98/H+vyej/31Ad9vWcvy9A5zrbtWny/PF1OEd00gIL/yQqamc" +
                "zfpu+rJ+i8JEPnONu2a+ebJqeIbhpAwf9kBc1M5m/T99VdXPZ/" +
                "GJ/N7jErzml5zl/sghrN8F8M+41i/8f9i9twPS/uLk/2hdmhOj" +
                "p9KT6b3f3kbpzbxzN6s7eDj+Eb5vP90cTn2MT728M/md1vmvXX" +
                "+vk+/P3gY+2f78MbCcZb1fh7wseJVg9/DPP3P9ns1pr1D/X8lu" +
                "9p+NsEw186Xq2b1fjz8K+TUXo9i4eLh8P/157tMzZ4ITDP4Jn/" +
                "53R1bdqD1Zm0938sBSMN");
            
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
            final int compressedBytes = 1848;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWzlsHGUUXskr4WAhigSJQ1YUcGKTNESA5CLNzlEgEZtg7k" +
                "hpEEWoKKhCQbI7cTbebZEQCEtBmCpKARUKl7hkQo4GS06XBigW" +
                "KHCBkGWJ/efNm/f+a3Yynn/H3hnNf3zvve8d++/M7HgcdINurR" +
                "Z0g6Sv1XAWdP29/t6gG50GnDRQzhF+AM7ZiLUWb4KXM5AEe/Ji" +
                "89lZDZTYdQuKgSIRvuWMYdxc5hZqPpyRENq4pT/Ge3/s3Ns4F8" +
                "jSBMplbR3hEhNGMhNK/m1yruePte5kRyWjppE8bi7rPvLlrcq8" +
                "Dd57G1hPbwMRk7bN3o6RzISSf5uc66kaWYy2kc3K7MOetyxrHu" +
                "4fDzcfJQTrmXdrHugfRwz4k/3jaQ09WCtla04OkD9hxsN3LPqP" +
                "qVrNo3cVz+zgenbWdm49W38Wq2f7/Tz1jJFC9dS3u12frT9qu2" +
                "jr/OKSvW2oXfOtka7ndbf8Wd/3xjHDJ3Bmd58/TTnp3/foveLf" +
                "d2+T995men3fFMjSBMplbR3hEhNGMhNK/m1yrudtyvdLWYy2kT" +
                "yW75fAR768FZ4Kru+d36tbnxf2OV+fPd57vXR99hCRPpWe9jn1" +
                "dIkJS/y+i7xmHiE1y7meqpHFaBvZrMw+7HmbeRrrcGCrS20zy3" +
                "lqvZgMNfL5IC2KP9sDSrL5bf5Va1UrXAqXvC3RQt+vdTITczGO" +
                "TgMuENGSnCO8B1sxQjb0BCPg5QzQg42QwojkNMM4va3OTW6tMl" +
                "Jm4BsZISJuhePmMtmCFpeiBx6J4EzW5xZZDrr/9C6Xf09RBmdR" +
                "DptdOXmGnbDjXRIt9H3eZCbmYgwtaIqW5BzhPdiKEbIRaxz5Jc" +
                "5BErARUhiRB5qhBXKQPzkmygyyREaIhFvJDGALWlzK5YSkn8Ul" +
                "shy4PleUz3El1ypYsWt7K/k48nkoy07GxayYh0rul/7efj0XP3" +
                "F5v1Tm7/dFB2fMnbO1P8il9WEx9mA2mIUWesRg3vkHEdIkOUf4" +
                "AThnI1bulxhIomvYfOqx6xakSfoXD6gZcwaKQPUs++O5SNprwR" +
                "q00MeyNZx3NhAhTZJzhB+AczZiTf1LDCTRNWw+9dh1C9Ik/c79" +
                "asacgSJQPcv+eC5cO5wKp6AVfXLNn8J5eAxQhkzJM44iG59zTP" +
                "bBrbmdHIPq0xQnZ1G9cV6ek85NkfIIebayRzli8pV1fQ8bo3f+" +
                "dJtTcDw4Di30iIndP+QfCo4vTQCOGgKjHWX8AFuYgTaxihHwcg" +
                "aUYAQ8Cs7KkdYdNXbdgjKiXIVvOWMYN5e5BWYqx6H64XVMjvlg" +
                "HlroY9k87P6kP4kIaQqMdpTxA2xhBtrEGtdzknNwCUbAo+Csep" +
                "w8dt2CNEkfctK5ZQvMVI5D9cPqiVxzwRy00MeyOdj9GX8mmIv/" +
                "/j5HmgKjHWX8AFuYgTaxxvWc4RxcghHwKDgrRzr/qbHrFpQRMk" +
                "JEcsYw7q9PZoGZynGoflg9U8tQe9YU9gQmDu8ESHHe/hS1Cedy" +
                "YpD5tN8iJ9CPzGOKg8/Iwq6Fo7P7zazgW45eZ81xHtZyFEd0Kn" +
                "pe1YxeSZ9O3Sj/jB29VhLPQjE7FzmxqPr1jF6KnrPU89fCvC9U" +
                "Xc/oZUs9c+UUveiintu4K7kvq552qft65vNdNMJwPByHFnrEYN" +
                "64qSLheGcTZ2RjHpNtrXb+DcTJL+znX+cS6k0ezHHyHOQx2VIk" +
                "Iidb9MSNEplL3uV4cZz1fKn9haPnS1vVPV+6+Oowny+1vpP+/r" +
                "5qsHs2i7X1kzT7Oum/TJHvmw9kWH9rwH7uH7cMcTxjZfkRru/J" +
                "7Kuk/yFur6s5tb4xMOR8h6TV52pda93I+/yzMe5mfZaxFX6fYX" +
                "zYzz+V5/O3Hfz96HZ1HDa7svLMXJ9HRnB9HsmzPrfzPoP2bPpa" +
                "6nsi11mkkvfBmucK3s9POI4ra30e1vW7e4qsz8U9tvW5eM+Q1+" +
                "fhYa9P75Q+KvHceapaHhc5Dai0/Hb9uoOarlfHYbNzkadaz8b+" +
                "2shtw88pOlMb4a3RKE8r5/pM3/fu3jt69bT9P4K7jZ6UttfceO" +
                "juc5/FWcv3+oJj31W8b9N9sLr7+eG/b+N/xkfeFQfX9yvuOSiL" +
                "fHZl5VnF+vRWq1ufNt9lrc9K6nl126vzauF6Xs1Tz/ZvLurZfW" +
                "T0nofk+3/D4r83K7keTVZXz4ufu/y++yf7ez1u476P4EzMUwQ0" +
                "45bkDJH62FaMkC31dDLB6gpDgics9WRk2+vIz/3JjJRZ4pv067" +
                "IV85/aJnFzzZOYCY8jvfbVybKS9Tk1uvdLldTz8Z1eTzfnz/DN" +
                "surZPbpTrke2nKie0eXtrk95K/P/3/X1WdpTm48GyD+2/NZ9KN" +
                "f90l93W8/B9/P+tD9t+N0xnWd9Ci2xPg3aB828w1qfNt/6+bNY" +
                "lG7PnwZUnD+f2g3Xo93ze9NfqHB9LuT6vv/r4nrUuOVmfVZ5Pb" +
                "LlVNL6/B8sLGSX");
            
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
            final int compressedBytes = 1910;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWkuMFFUU7YSFGELczIpEZ8SAM+0CjDGygUx92t8Etppo3C" +
                "CJCWpYKDs+9sDAQK9MTNxoookLdsaFrtj6wcTIGjHsmA2sJRka" +
                "p+r2rXPvfe8V1T1V3W1bk/e799xz7ntTVa+qZlqtbrvV6u7p7m" +
                "0Vx2efcK/3SqvC0X12q7zgsb+0VV52rPtatRzdpx/jP+i3X/0+" +
                "gH/Osbw4VD6HuBdvyjbe5PWMN9ki4/TIjQ/b4PNZoR/yS5xFlD" +
                "GGeqEov0Z43tpXen4e+r+enxe+GPX8LFvP6N3ZW8/QnOq63kvX" +
                "c2kG13Op2fPTPbCe1Y7Vu63/0NHb0yx/6f3zcDPn59rxyZ2fl7" +
                "9s8vxMukmXamrZRmNYgISfLat34QGXZLOsvXnNwHaO0Xifpi93" +
                "N0Lmz5beM3bGkgEZWGWth4wNeiPZyMeDlvuwc00W7snR1noWfR" +
                "TJBtaB7UfwaI+OczVtnpZFaGwgEzlHq60ZpL4seh3cjAvkneRO" +
                "Ph603Ic9vpbVjMjG8PsKovIns2tSgXrxNXAwM9TdLICRSGbWLP" +
                "AjFnXWZnEyivvIlNkZKVfJ1WE1KumB9ADV1GY+GnE/vk12INgv" +
                "LbIgltnAOsj2tmaAh1uZhV+TOGTubgRyQCZZnJ4x9cGHTCSvXi" +
                "tkrNH5/rzb3FlPcW99Z2vmjtWdk9vfKzIMvb/3OpPb3y/NNf3+" +
                "7jx//qTeUv+q/3dYB+eoHKG4uuY5kfPzzdk9P5tZz+jk1L5vnh" +
                "z79b7Z9PtmL53g+2ZSBbV+sD5FPj+T3cnWzn9lV/1zSnZPbj1D" +
                "2t2vG9vxTzV+fr42wfPz1XErYj3jy6OuZ3lk740JrufrTSvE92" +
                "Qb31s/wuPMcmUX+zU6FB+2weezQj/kl7j43uqd8qy01dfTfX29" +
                "k0a1eRueRvb3+P1p3d+zzMa7v6/uL66Nt2fvffPqU+PRif6mwr" +
                "XrDY3CfKP4GFFNAyjkX67AnnL+kL6NdlHDXu9rf9TwfvTOtL8f" +
                "XVpp5O9xf9a1nhdPhO6fF4+Pdz19c3LXc+2H+u6f3X8GT77zyb" +
                "z7PJ/ZKjw1z4fRxLvdQ+/vQzzPz1d9nq8jy/xd663id7lY1915" +
                "7cmg54nx7kf1zanyeha7evrz7O3v45pT9DkVrl1vaBTmG8XHiG" +
                "oaQCH/cgX2lPOH9G20i6rveT46V3V/r+G3f27k/ehclf1o1L+/" +
                "r78Xfn9P5pK5hr4vzU3w+9LcuL8vDXt+Fr/5M2PcVc6MP7Ly95" +
                "C+bON+8f+f/cxyZRf7NToUH7bB57NCP+SXuLhvvof0y2bm7+m+" +
                "+R7St5zheRue9nbPz7Lvn+79M/8u8OH2z4ILXz3G/63f3qv094" +
                "4L3wx7/xTrfF+28f31D3jMFh86FB+2weezQj/klziLKGMM9UJR" +
                "fo3wvMt48jvM9Rrub9dbU3pUy2zU/NPFdJFqatlGY1iAhJ8tq3" +
                "fhAZdkA+vgmvtYM8CTLsrMwpq+3N0ImT9beh/ZGUsGZGCVtR4y" +
                "1mjvnWRw9iZLyZLn/b3S/ywzyocm3om9vy9Vfn9fqueKKP6+uZ" +
                "AsePJZqJT1Qhjt5x3b8+dC/ciq70fRzWHej8q+19X9frSN73U3" +
                "q+zvuWWK/j9ketdz/dNm17Ps/Sh6fva+LzU7p+h0dDp+lNXUbj" +
                "1LDUbZ2FoyvB5JC6Fhy5/MHhGSlNgGDkJTGz9ir/UzhnUIq20y" +
                "gn2cEcWwio6CPtQIJb1aAXkAzf2y6z39dfau99Cc9PV+8bv6rv" +
                "ek7/aGfuc6Ma3Xe9OZTWI/SndM8Pzc0eR+lDxIHjjnZ27LSnqD" +
                "vDzmmi16pL2azXsdPGAlzWQjueYfX7ZulhIpY3hOMttwjj4Gn6" +
                "VYgYfJQ6qpzX0Pedw5whYg4ZcWWcgu2cBa6CsGeFxESNPN3Y0A" +
                "Eniak8utI+x8pB8WsZ4DdOdo5yjVWUsHjTpHk3bSJitbsl5mw4" +
                "hbLoynSEaDI9dus9equzlIZlgsRiOkmuQli9XWeWCFMAfLqi2M" +
                "HpRjnWNUZ+3Ad4zH6S9khQVoGSc9hPfZtIaMlnE6B6vpy1OyWD" +
                "XJK+fkciNTmaGcrVbUGRfolc4K1Vk78K1gzDVbgJZxVKLzMkqi" +
                "wQENGa3VpKLVZGR03tq0X8bKWWSRbuZgEOvpzFYr6owZndxKbl" +
                "FNbX493uIxLEDCLy1Zic5KLskG1uJ+oxjgcRF+zeism7sbIfNn" +
                "i87SMiADq6z15Fwkennv8l6ql4snJhpl4/Q3sjIGPTnShfGSDa" +
                "ywwqI9bhZS0+ZpWeBHLGrMyeXWEXZeeh3cjBnZaXfaVHeKvyPR" +
                "KBunv5MVFqBlnPQQ3mfTGjJaxukcrKYvT8li1SSvnJPLjUxlhn" +
                "K2WlFnDK18dfdlPxhR2ULE1kYFCG5lLe1ybDXKDsZqlRAjZ4W8" +
                "rAc2mpPllzbfjKWinIl/Hsv7sx+MqGiELBJBraylvUyjdD33a0" +
                "YfHoycFfKyHmnz8Vu/nbFUlDOxeXVudG5QTS3baGwtEq3j0HIU" +
                "iuSArhvNalpRYi2HLyutZjNwtW32zG+VtZ7NuIj8F7JUi1g=");
            
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
            final int compressedBytes = 1622;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj1vHFUU3QKQQCkciQqJAnAaaCAICXf27k7FT4hryz8gUk" +
                "qUtTEsRKYBCijiAslFUlFQpEgRmhQWbVKkQXIZ8QsosOdy55xz" +
                "7xuTXU3CAm9W7729X+ece+3xDhs2NzY3RqPNjc32tMsss313D7" +
                "K5jiNelX3KwdXKxoyRs6STUSJb7CJzq47RSBXGaUQdXgGufM1+" +
                "83fz2aheC12TO5M7ttvpPrPhQSbi7OEVazMqY3CF12hGfCmyoq" +
                "hK1s9KtGNGgILYrfJBsWYXZ3x8wfyPn+mndDzgT/z4xdYNf82/" +
                "6N7N6x08wDy/7N59Vaex4F1xf3LfdjvdZzY8yEScPbxibUZlDK" +
                "7wGs0vc2btuYL1sxLtmBGgIDIrHxRrdnOvuWe7necxs/w9ezhb" +
                "63B6FRZjOH+p2tmUkXMjRkmVskUFmTuqd/zIrHxRsWdPTientt" +
                "vZzvrUbXiQiTh7eMVaRu1+noKASM7o48zacwXrZyXaMSNAQWRW" +
                "Pu6Fs2evFp4/X17mL8fste7dSy/yL9bslZX/PPq6fq4MeX32e5" +
                "3BoL+f39QZDDrP7+oMlr22rm5dtd1O95mdPciGpfW8wIAceFHF" +
                "kVKc7RKGZjCbR7KSiI1ZOHruVl+YD3c8e/dsvTF7G73cvH7RT+" +
                "Dg1/QJ+9bZeq/wyfvh2fooea8M9Ln+5t/E318Q7x22Pv/kzPPB" +
                "QvUf/3UuOM8C0n9wngc/LTfPydPJU9vtdJ/Z8CATcfbwirWM2j" +
                "3/CgIiOaOPM2vPFayflWjHjAAFkVn5uBfOLt7Rr9fPlWWv5lpz" +
                "zXY73Wd29iAbltbzAgNy4EUVR0pxtksYmsFsHslKIjZm4ei5W3" +
                "1hPpS93Wzbbmcb23Y7e5ANS+t5eS0wullsMwpHSnG2SxiawWwe" +
                "yUoiNmbh6LlbfWE+yJ5enl623c7zmFn+3ndkeJw9vGIto/osFA" +
                "GRnNHHmbXnCtbPSrRjRoCCyKx83Itkn0xPbLezjZ24DQ8yEWcP" +
                "r1jLqB2/ICCSM/o4s/ZcwfpZiXbMCFAQmZWPe5Hs3emu7Xa2sV" +
                "234UEm4uzhFWsZteMXBERyRh9n1p4rWD8r0Y4ZAQois/JxL5K9" +
                "N92z3c42tuc2PMhEnD28Yi2jdvyCgEjO6OPM2nMF62cl2jEjQE" +
                "FkVj7uhbNL180/6nPP0s9Lt5vbtp+f8LmN3TM5XlqoYobMAY9G" +
                "sgrmjDojCuKoxa7dKDbirFB7L/Fo9vjS+JLtdp7HzPL36kE2LK" +
                "3n5bXAcH7H4Gq+NM52CUMzmM0jWUnExiwcPXerL8wH2dPD6aHt" +
                "drZ/Cw7dhgeZiLOHV6xl1O7vjSAgkjP6OLP2XMH6WYl2zAhQEJ" +
                "mVj3vh7OL3nz8/13/v+XQYnL3Hq/kXtH6/VPp+af/b+n3dasyz" +
                "8Lx0fVSver8P+vvZeur9vpL3+/yXetcOeb8v+rxU7/d/+n6fP6" +
                "yf76v2PL+a1/jB+IHtdrrPbHiQiTh7eMVaRmVeICCSM/o4s/Zc" +
                "wfpZiXbMCFAQmZWPe+Hs+vz5vK6tR7Z8z9E+qx9vmZhnPBsHsq" +
                "D/YgaPXIzfxx+rY1Zzq7llu53uMxseZCLOHl6xllGZFwiI5Iw+" +
                "zqw9V7B+VqIdMwIURGbl4144u97vw14HPyz8/4N9X58/+5+X5o" +
                "8Xnef8UZ3ngs+fF/573MFRvav7r+nR9Mh2O91nNjzIRJw9vGIt" +
                "ozIvEBDJGX2cWXuuYP2sRDtmBCiIzMrHvXD2v/f7pYMf63+//x" +
                "/mWVB6t/4dHPQZqs5z6au50dyw3U73mZ09yIal9bzAgBx4UcWR" +
                "UpztEoZmMJtHspKIjVk4eu5WX5gPssfr43Xb7Wy/K1l3O3uQDU" +
                "vreXktMLrvY9YZhSOlONslDM1gNo9kJREbs3D03K2+MB9kT3em" +
                "O7bb2X7277gNDzIRZw+vWMuo3fOFICCSM/o4s/ZcwfpZiXbMCF" +
                "AQmZWPe+HsZr/Zt93O9nd33+3sQTYsrefltcDo7tV9RuFIKc52" +
                "CUMzmM0jWUnExiwcPXerL8wH2eO18Zrtdra/u2tuZw+yYWk9L6" +
                "8FRnevrjEKR0pxtksYmsFsHslKIjZm4ei5W31hPsiePJk8sd3O" +
                "85hZ/t53ZHicPbxiLaP6LBQBkZzRx5m15wrWz0q0Y0aAgsisfN" +
                "wLZ4/+BCr9Gd4=");
            
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
            final int compressedBytes = 1388;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW8+LHFUQHg0K60EDopCILBq8rGaimIAQYbFnRoOihqxE8V" +
                "dABRfiIYf8Az0MncyQa04e9uLRi/5HgoqHSBINHgTB2amp+b56" +
                "9Xpnurdnd3Xea957XVVfffVju2e6B7bV6t1pBaN3Z1cneqwsxy" +
                "TmsZw+AuJYnlgeFm11MdS8uN1eaI+xzh++RtHkx0Jkfj1/Yrw+" +
                "OTl/arI+PZ7P7J4N708xL0yku1NpYzxfAsPwnuF7LZ5R/mZ+IX" +
                "9rcvZO1P5p/hlJX+Rf59v5Q/nDAerx/DhJJ+l8fe+ODP+Y4l7P" +
                "N/Osaj/zT4z05Xh+1Wpl69m6rLLv2kTSc6sBGpL156m+4ND4ys" +
                "HePKyd5RiHRXA0tfhMQm70Qtl9tfZAf7ji/vja6p/sP8/X555/" +
                "1b9CTf+58XzRI/uvjuc5px1f2W88aO179J+dY385ri+L3T/lNK" +
                "9Uymd6F1btZ4Spcj+bGHX7WYo3/Rz+fVD9HP6zCv1M12ez/Sx+" +
                "rNtPP7JT/qzqqO+57LHczAafDy4NLg8uGt1H++ftHS+N+PFe1g" +
                "qZb82xf1g1s+qoiN9ab01W2VUnstcA7TXhOXzBwXEZqRbs82J6" +
                "nT+Hr8+kLGPOJOSyh81XZtbO2rLKPrkj2ip7DdCQrD9P9QXH7K" +
                "5rMwtbYnaWYxwWwdHU4jMJudELZffV2gP9Abq7092RVfbJG9mO" +
                "ytAACTtreIa+zDp76zMMsHhEWUyfu/fg/DkTWzEzIIMwso3HtT" +
                "C6+vf7zQ9W4fv95qV63+/Fg+Ln4qfdfhb3Z88Kv9aorqSfxZ+h" +
                "tvitmX4Wv8yx363N/Pt0v9fQE8UVf9bg88qVw+VZRk30t/jfXp" +
                "+jRw7j+kz9XP79XnyzzDtitHYw70HF1aPyRta/1kqj5ujc6tyS" +
                "VXbViQwNkLCzhmfo61mZgz3UxyLCwzJbFpsl58+Z2IqZARmE1d" +
                "p4yNiio7/kX0/XWaPX7Lf1bFVRi2cT8i3C32QOFZ7GNrINWWVX" +
                "ncheAzQk688TEYCBFl5sidlZjnFYBEdTi88k5EYvlN1Xaw/0hy" +
                "tOvyfH3jcnmhrvm9lWtiWr7KoT2WuAhmT9eSICMNDCiy0xO8sx" +
                "DovgaGrxmYTc6IWy+2rtgf5wxen6bPL6TP2M93NwO/WzqX4Ovq" +
                "9/fXa2O9uyyq46kaEBEnbW8Ax9PStzsIf6WER4WGbLYrPk/DkT" +
                "WzEzIIOwWhsPGQfoolPIKvvEVqgMDZCws4Zn6OtZmYM91Mfi4z" +
                "F97t6D8+dMbMXMgAzCyDYeMrbodL+n76PUz7CfncuH18+y2E31" +
                "M/0e0vCvnel+T5+fqZ8r08/0+Zk+P9P1ucoj9bP+6J7unpZVdt" +
                "WJDA2QsLOGZ+jLrBwXDLB4RFlMn7v34Pw5E1sxMyCDMLKNx7UY" +
                "dLvbllX2ia2tMjRAws4anqEvs87iGwZYPKIsps/de3D+nImtmB" +
                "mQQRjZxuNaGJ2+j5b//Dk6n/rZ5Pd754dyr71si6AW829izI+0" +
                "jFyyE9kJWWVXncheAzQk688TEYCBFl5sidlZjnFYBEdTi88k5E" +
                "YvlN1Xaw/0x1acnpcavD43s01ZZVedyF4DNCTrzxMRgIEWXmyJ" +
                "2VmOcVgER1OLzyTkRi+U3VdrD/SHKz6M/y9O75tpLDZGPa+7cX" +
                "F/nDfeX91+9s70zsgqu+pE9hqgIVl/nogADLTwYkvMznKMwyI4" +
                "mlp8JiE3eqHsvlp7oD+2YnfNXkj3be1P9ch/B4zenZ29V4HpsZ" +
                "nX2wdawaNHvcfp83Mfn59ne2dllV11InsN0JCsP09EAAZaeLEl" +
                "Zmc5xmERHE0tPpOQG71Qdl+tPdAfrvi/+vtS8d2RfP78F54Vm6" +
                "4=");
            
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
            final int compressedBytes = 1746;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXMtqHFcQ7e8IiiExIZBsEmJDllGrB4Q2hhBLKxuCllo4i+" +
                "yjjI0fWgby2ASiLwhkrV+IF5YWXsTIwT+gjffRdKn6nFN1u6UZ" +
                "D8KR+zb33qmqU+dU3ZlRz4zBzXFzXFXNcXO2V5Vb/thXIDzOHp" +
                "4xl1mrs6EMiGREnyYq4l0zuH6uRDtmBlQQlVWPe2H05N7knq22" +
                "z2Jm+WP1AA1L83l6Ljhc3zk4m4fG2S5xKILVPJIridw4C2fP3e" +
                "qF8yH0weTAVtvb2IHb8ADJ8dJElj9mVnhZlSOclzWVI7Mgrh1p" +
                "JdoxM7A+z5gZK+6QJ5MTW21vYyduwwMk4uzhGXOZtdMXBkQyok" +
                "8z154zuH6uRDtmBlQQlVWPexH07mTXVtvb2K7b2QM0LM3n6bng" +
                "6PR3mYUjpTjbJQ5FsJpHciWRG2fh7LlbvXA+QNe369u22j6Lme" +
                "WP1QM0LM3n6bng8LNwDs7moXG2SxyKYDWP5EoiN87C2XO3euF8" +
                "gG6OmiNbbW/vVUduwwMk4uzhGXOZtbsfCgMiGdGnmWvPGVw/V6" +
                "IdMwMqiMqqx70wuqqmn5zO96YfIvbj99VcY/rB6fy04P/idN5M" +
                "3o+qpYzp++fEP5uT7zpbD34+9Xw+V/6X7bk+a57Zarv7zIYHSM" +
                "TZwzPmMmv3fAoDIhnRp5lrzxlcP1eiHTMDKojKqse9MHrt9drr" +
                "eNLmMz9Wtt2jlkaVrTRmEecZyvTVr1K1uUpGak7UKrHmWoc9rt" +
                "3sNDu22t6e9Y7b8ACJOHt4xlxm7Z5PYUAkI/o0c+05g+vnSrRj" +
                "ZkAFUVn1uBdG1+v1uq22t/eqdbezB2hYms/Tc8HR3ZvXmYUjpT" +
                "jbJQ5FsJpHciWRG2fh7LlbvXA+2nH6y/pdV/GTRe8Wi2f+v0fd" +
                "1I2ttrvP7OwBGpbm84QCMPAiiyOlONslDkWwmkdyJZEbZ+HsuV" +
                "u9cD6E3qq3bLW9jW25nT1Aw9J8np4Lju4stpiFI6U42yUORbCa" +
                "R3IlkRtn4ey5W71wPoTeqDdstb2NbbidPUDD0nyenguO7iw2mI" +
                "UjpTjbJQ5FsJpHciWRG2fh7LlbvXA+2rGN1Vc2fe17lK3yGMpg" +
                "q8y1+uo8DatXK+yrt6Q9zN+nH7MHdP6x6WuO9ln9fIvEHHExDa" +
                "BQ/7CCR4b5+/RjdkTtfTt+3yx932w9C3zfpJN/YdPXHO2zep7h" +
                "F4vFHHExDaBQ/7CCR4b5+/RjdkbN+/p8+PRdeH0+/Gux1+fqdH" +
                "Vqq+3uMzt7gIal+TyhAAy8yOJIKc52iUMRrOaRXEnkxlk4e+5W" +
                "L5yPdhzH/WocyxyP/h7PYDzPt3fM+3lpzr/7P1zp30Ne1i9ttd" +
                "19ZsMDJOLs4RlzmZV1wYBIRvRp5tpzBtfPlWjHzIAKorLqcS+C" +
                "PqwPbbW9jR26DQ+QiLOHZ8xl1k5fGBDJiD7NXHvO4Pq5Eu2YGV" +
                "BBVFY97oXRl/9+v9qjed48t9V295kND5CIs4dnzGVW1gUDIhnR" +
                "p5lrzxlcP1eiHTMDKojKqse9MHr89+Llfn+vb9Q3bLXdfWZnD9" +
                "CwNJ8nFICBF1kcKcXZLnEogtU8kiuJ3DgLZ8/d6oXzAbrZbDZt" +
                "tb197W66DQ+QiLOHZ8xl1u79IQyIZESfZq49Z3D9XIl2zAyoIC" +
                "qrHvfC6PH9vtz3e3O3uWur7e4zGx4gEWcPz5jLrN3zKQyIZESf" +
                "Zq49Z3D9XIl2zAyoICqrHvci6O1m21bb29i22/AAiTh7eMZcZu" +
                "30hQGRjOjTzLXnDK6fK9GOmQEVRGXV414YPbk1uWXrbLdhltm+" +
                "ugdozuOIZ2WfanC2qrFi1CzVySxRLXaRtbUOnFDuVhW1YmgV/h" +
                "L82/1ryIPxE/oi46uPZxcsm4rgyQjbeWX/kMZ5FTGuhAejV4W6" +
                "YoR9Jf4Yjx2zIndyXh+zsfbHYrF5URcbM67IdxH+Zdbwhp9E8H" +
                "5/NL533/bfP8fzHMccvy9da675I/WZjZXtkgUMc7FCSduySlUg" +
                "01e/hjrgerkjzYh7f41lhtxVdwIrzcqZZ6WLtT6zsbJdsoBhLl" +
                "ZINa64UqkKZPrqV2bRKvwRd6QZce+vscyQu+Lu49j7aXzfLnPs" +
                "/Xqlu/vlQqjflqd4/8n4mlrqM/j7eAZv8Ol9/P1zib9/jue53P" +
                "Oc3JncsdV295mdPUDD0nyeUAAGXmRxpBRnu8ShCFbzSK4kcuMs" +
                "nD13qxfOh9D7k31bZ/tZbB82VkdyvDSRBTawwguPRnIVrBnrjC" +
                "yIIxerdqPciHOF2ntJR9Hj981LuL//OZ7B5d3fH3/zLtyPHn99" +
                "Wff38TzHz0vzn+ei/z9D9R84pJkg");
            
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
            final int compressedBytes = 1431;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vHFUUdZU/QIdEARENpCCglVJ6vTMNad0gWYr8DyjcI9" +
                "skgKlpLVmipEOiCWkQZYSSFOlT8AecIg0FzlzdOefc+97a3qy9" +
                "sf1m9N7b+3XOuc8z+zFR1tbysbuz1o6Fj73PTsaHe58svp97H5" +
                "+Mzwv+r07GJHk/XZLuj06Jf3FOvNtsff/LiefuuervrW4/D/68" +
                "vvvZ7vfV3u8Hf70P9/vG40Wvz43HZ7k+f/z3su73g7/b++f7dL" +
                "/vfXed7/b+af/UZlvdZ3b2IDt74mvUAoN5OdMjWE/jzL78GrVZ" +
                "SU0xK4lYeqpef53v95/+uArflx79epH3++BZ0vtn28932c+C0t" +
                "/at56F3z93+h2bbXWf2dmDbFhazwMMyIEXVRwpxdkuYWgGs3kk" +
                "K4nY2AtHz93qif1Bdveye2mzrW9jZvlrn5HhcfbwiLWM6nuhCI" +
                "jkjBpn1p4rWD8r0Y4ZAQois/JxL5w9ezZ7Fq9Z85kfM9sli3O0" +
                "NjOAx6rKORkHFTmLFUReRdce6vylmprHubvD7tBmW4e9PnQbHm" +
                "Qizh4esZZRx7+nICCSM2qcWXuuYP2sRDtmBCiIzMrHvXB2+/3e" +
                "ntddxu/Nd/u+tP7l2xOWDT7cZ4MzOLP2uswx7/DcEktGdFXQFS" +
                "PsK+HHeOyYGbmT0/oYdvrbdtcueszezN6UfebHzLZ71NLoPAZE" +
                "HGdepc9+1jpQlZypNZGrhJq1zvdw9ynz9/zqCl4pK9LePo8u+n" +
                "lI28/F93P2YPbAZlvdZzY8yEScPTxibUZlDK7wGs2IpyIriqpk" +
                "/axEO2YEKIjdKh8Uh+z92b7Ntg6xfbfhQSbi7OERazMqY3CF12" +
                "h+mTNrzxWsn5Vox4wABZFZ+aBYs1dzv8++Wd39XuO+uOef7fdm" +
                "+3xvn+/Xcz/75/1zm211n9nwIBNx9vCItYw6PlsXBERyRo0za8" +
                "8VrJ+VaMeMAAWRWfm4F85u12f7PGqfRzfn+uzudHdsttV9ZsOD" +
                "TMTZwyPWMqqzKwIiOaPGmbXnCtbPSrRjRoCCyKx83Itkb3abNt" +
                "s6xDbdhgeZiLOHR6xl1JFfEBDJGTXOrD1XsH5Woh0zAhREZuXj" +
                "XiR7q9uy2dYhtuU2PMhEnD08Yi2jjvyCgEjOqHFm7bmC9bMS7Z" +
                "gRoCAyKx/3Itnb3bbNtg6xbbfhQSbi7OERaxl15BcERHJGjTNr" +
                "zxWsn5Vox4wABZFZ+bgXzu4n/cRmW4fvUhO3swfZsLSeh9cCY/" +
                "y+NmEUjpTibJcwNIPZPJKVRGzshaPnbvXE/mjH8fj5g/atZ9Gj" +
                "f9I/sdlW95kNDzI5XhqoYgZwIAMoHOG6zKkYGQVx7UiVaMeMwP" +
                "w8YmVUzN2n7/P/tets0WPj1sYtm211n9nZg2xYWs8DDMiBF1Uc" +
                "KcXZLmFoBrN5JCuJ2NgLR8/d6on90Y7b9dl+b7b/39Guz/Lx6J" +
                "+bcH3+cP+yns8/vH8T9vPh1+3fO67mvx+1+33efk53p7s22+o+" +
                "s7MH2bC0ngcYkAMvqjhSirNdwtAMZvNIVhKxsReOnrvVE/vDHb" +
                "f7fZn3e/+6f22zre4zGx5kIs4eHrGWUcffu4KASM6ocWbtuYL1" +
                "sxLtmBGgIDIrH/fC2d16t26zrcOzvHW34UEm4uzhEWsZdXxeKA" +
                "iI5IwaZ9aeK1g/K9GOGQEKIrPycS+SPe2mNts6xKZuw4NMxNnD" +
                "I9Yy6sgvCIjkjBpn1p4rWD8r0Y4ZAQois/JxL5zdH/fHNts6XL" +
                "vHbsODTMTZwyPWMup4fwgCIjmjxpm15wrWz0q0Y0aAgsisfNwL" +
                "ZxffWV+NT5bvrrXjXMfsxexF2Wd+zGyXLM7R2swAHqsq52QcVJ" +
                "Q60AhjKrr2UOcv1dQ8UZXEjubgHp3pr3S0xL/4UcY7C/4yNZzj" +
                "+B+lbk1L");
            
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
            final int compressedBytes = 1116;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7tuE0EUdQEo8AlIVgSIhjRBiAgkGrS7Eg/x+IC0UZqUKA" +
                "UVWFGMYgmJAiGlpM63UOQL6GnzATi+XJ9z584ufizGie+sZsb3" +
                "de6Zm7F31pE7Hd+K753a1mSb1muydo6V4k2C3yaH+drHd51oUc" +
                "+laOV6ua6vrE5kjCznJPgwFmfI5ZaoHAtE6qhX0wqYL6/IRqRz" +
                "Pcc8gl/VuALdsvtH0x3bRjqRMbKck+DDWJzBcexqphwLROqol0" +
                "exLPQVr8hGpHM9xzyCX5Xm7l33sb2rs+z03o3xqyuLfIf1ri37" +
                "Z8DgcXwORj2XuJ5Pogat1vNp1GCOz/N7w36zd3vS8+fhD4dwa9" +
                "g3MsgPhv2h095tifdf7si9zSnx7rDUfznU3J8q/lHt/nweu2zm" +
                "8/xuuSujzKoTGRp4ws4a7mkso3JeIMDiPepyeu4+gvkzE7tiRg" +
                "CDNLPNx2th73jeXMD96HXUYHH3owzCJbwfjTQt3Y/i/R77c3n2" +
                "Z9Qz3u+X6f3+6f0q7M+Dr7E/l7UdnkQNZm3VfrUvo8yqE9lr4A" +
                "3JxnNHBvhAiyi25Ows5zCsB2dTi2eSYqMWiu5Xay/UB97lTrkj" +
                "o8yjZ9EdlaGBJ+ys4Z7GMur4edcgwOI96nJ67j6C+TMTu2JGAI" +
                "M0s83Ha2HvOC/F+fOinT+rX5fhzvBh/WKcPwd7sT/j/LmoVuwV" +
                "ezLKrDqRoYEn7KzhnsZ6VMbgCI2xHullkS2KZcn8mYldMSOAQb" +
                "pamw+ME+9+0ZdR5pGtrzI08ISdNdzTWI/KGByhMdY/n9Nz9xHM" +
                "n5nYFTMCGKSZbT4wtt5xf4/zUtQz6rnq9Zz9+7qoZ1rPg5PZ92" +
                "d1Wp3KKLPqRIYGnrCzhnsay6jjZzCDAIv3qMvpufsI5s9M7IoZ" +
                "AQzSzDYfr4W94zz/v583437U9H4vtottGWVWncjQwBN21nBPYz" +
                "0qY3CExliP9LLIFsWyZP7MxK6YEcAgXa3NB8bWO/ZnnJfivLQ6" +
                "56VyrVxLLaITPUaWcxJ8MNdlgEWi8j6q1VGvvJdlYPPamJRlXf" +
                "5cTJ0GuWN//uvvk4vjhu9Ljyf6VvW401o7x0rxJsFvk8MUbM+K" +
                "s7xO9BhZVo2VrLUpAyyK0xSpo151K7As2dPGpLlyqJ5rs0ZzDz" +
                "772P7b+f5G/Ter+3RUbVQbMsqsOpG9Bt6QbDx3ZIAPtIhiS87O" +
                "cg7DenA2tXgmKTZqoeh+tfZCfeCd+z3s0St9Nfgyxefx+PewRy" +
                "8W+ry8VL+HrTarTRllVp3IXgNvSDaeOzLAB1pEsSVnZzmHYT04" +
                "m1o8kxQbtVB0v1p7oT7wbt6fU+2T2J/ndd2qtmSUWXUiew28Id" +
                "l47sgAH2gRxZacneUchvXgbGrxTFJs1ELR/WrthfrYFbv9+Sy+" +
                "xZy1lT/LnzLKrDqRoYEn7KzhnsYyKucFAizeoy6n5+4jmD8zsS" +
                "tmBDBIM9t8vBb2zrXBt9hnbbb4f8cc7TcYnVqx");
            
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
            final int compressedBytes = 1208;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vVFcQtUjBf4hEESKa0LCKIoXS+7x/AKVDpqcmfyCWFS" +
                "yclhIJyS11UiAh8QOQQECTFK7yF1zH69H4nDNz7+N58xArMXd1" +
                "7/N8nXNmtOv3tIadHV+7/9j2U5f6WhnjFdNjnjGNA1nQP87gkX" +
                "H8Hn+snqLy8ZudWjOu336tGdQ8t2Ud/HC+vz24ufk8D74737cb" +
                "/h/P90/Je2sm3Tc+Eb9zRbzvk2dxpfqf1+fe/t6+nXZ1n9nwIB" +
                "Nx9vCOtYzq7IqASM7ocWbtuYL1sxLtmBGgIDIrH/fC2cOH4UOc" +
                "tPnMj5PtlsU5WpsZwGNV7ZyMg4qcxQoir6JrD33+Vk3PczmB98" +
                "P7lHnhMz9OtlsW52htZgCPVbVzMg4qchYriLyKrj30+Vs1PU9U" +
                "JbGTEdyTKb9LpmVNW2usiDcFf04N/28d/1l36Vnn+VfNoJ6Xtu" +
                "N5qbWePKt32Zzr9xc1g/q8z/t5P3y66ee95jnnPJevl6/ttKv7" +
                "zIYHmYizh3esZVRnVwREckaPM2vPFayflWjHjAAFkVn5uBfOvv" +
                "r78/jtNrw/ly83fX8uX055fx79O9/9vb6vq/vR9jx/1jw/9/N8" +
                "PX9uvlaPVo/stKv7zM4eZMPSet5gQA68qOJIK852C0MzmM0jWU" +
                "nExiwcPXerL8yHO77q5/3ol6/h8350r35/1v2o5lnzrHnWPGue" +
                "9f1SzbM3z8MXm78/hwfDAzvt6j6z4UEm4uzhHWszKmNwhddoRn" +
                "wpsqKoStbPSrRjRoCC2K3yQbFm1/dL867h4fDQTru6z2x4kIk4" +
                "e3jH2ozKGFzhNZoRX4qsKKqS9bMS7ZgRoCB2q3xQrNn1+7Pu73" +
                "V//4ru7/vDvp12dZ/Z8CATcfbwjrUZlTG4wms0I74UWVFUJetn" +
                "JdoxI0BB7Fb5oDhkvxvepXvUhc/8ONluWZyjtZkBPFbVzsk4qM" +
                "hZrCDyKrr20Odv1fQ8lxM4G85S5oXP/DjZdo9aGlW0psYzZ1Kk" +
                "WOmnv1pqs0rO1JrI1ULNWsc9zr13f+++nXZdx8zyn/1EhsfZwz" +
                "vWMqrzKwIiOaPHmbXnCtbPSrRjRoCCyKx83AtnrxarhZ12vfju" +
                "fuF29iAbltbz9lpgXP6tYsEoHGnF2W5haAazeSQridiYhaPnbv" +
                "WF+SB7eX153U67rmNm+c/qQTYsrefttcDwWTgGV/PSONstDM1g" +
                "No9kJREbs3D03K2+MB/uuJ6X5n3+9LV7atvPHO1Z7TWW8+n63d" +
                "NpHMiC/nEGj4zj9/hj9QjP37b9zNGe1cfbJOYZ0ziQBf3jDB4Z" +
                "x+/xx+qYtTxdntppV/eZDQ8yEWcP71jLqMwLBERyRo8za88VrJ" +
                "+VaMeMAAWRWfm4F86u7+s+/6p5zrt2X20HxpfsblP9w8fhY9tn" +
                "fpxstyzO0drMAB6raudkHFS0OtAIYyq69tDnb9X0PFEVr+O79R" +
                "mdc9U8511/XKsZzDrPb2oGc67698mbr9Xz1XM711f43MbpmRxv" +
                "bVQxQ+aARyNZBXNGnREFcdTi1G4UG3FWqL23eEL2yerEztXl/7" +
                "83y2ycnsnx1kYV0IAKLzwaySqYM+qMKIijFqd2o9iIs0LtvcWj" +
                "2Tv/AbRlfsg=");
            
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
            final int compressedBytes = 1468;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmcGOlUUQhe/KRB/BxBA1bhRQx5gYXZmfn4UJ7wATYQEBko" +
                "ms3OBkSMCw9QV4EHggXkL4y5rznarGyZAbRdP3T3dPVZ1z6lRn" +
                "huHe2e2OP9/tjj88/mR3+vrtl925Xscfv1pfDPLfvFrftuxnu7" +
                "28jj86o/7VOfU+bZmvz8X/7q9z3ufgPk/+mPc5vz/nfc6f97+/" +
                "z/XB+iD2ODMXcc8Ircj5XOogjLJisTKqMx5pOILdstKdVG3dRa" +
                "r3af3R/XDi9WW96fXl61zktTMeRdRxzd5BfVxn5MPRnhuh8quH" +
                "F97Ut2JHqme/+oxvnnP3P3g9vPDv9J3/fu7z99Fyfbkee5yZi1" +
                "gZIVVnhqtyuyo1yEiOI+rjyq7iLumfTnxiKshBndb7yXFB31nu" +
                "xB7nVruTsTJCqs4MV+V2VWqQkRxH1MeVXcVd0j+d+MRUkIM6rf" +
                "eT44K+udyMPc6tdjNjZYRUnRmuyu2q1CAjOY6ojyu7irukfzrx" +
                "iakgB3Va7yfHBf1oeRR7nFvtUcbKCKk6M1yV21WpQUZyHD/u2b" +
                "13Bv3TiU9MBTmonb2fHBf07eV27HFutdsZKyOk6sxwVW5XpQYZ" +
                "yXFEfVzZVdwl/dOJT0wFOajTej85LujD5TD2OLfaYcbKCKk6M1" +
                "yV21WpQUZyHFEfV3YVd0n/dOITU0EO6rTeT44L+mQ5iT3OrXaS" +
                "sTJCqs4MV+V2VWqQkRzHj3t2751B/3TiE1NBDmpn7yfHjj5+v/" +
                "9P6vdr+dXTL8/x/68PTvk//aP/f37vXXoXsV5cL8YeZ+Yi7hmh" +
                "FTmfSx2EUVYsVkZ1xiMNR7BbVrqTqq27SPU+rT+6H6AP1oPY49" +
                "xqBxn3jNCKnM+VXGmc3sUBVVgZ1RmPNBzBblnpTqq27iLV+7T+" +
                "6H6Afr4+jz3OrfY8Y2WEZH20xMqvqaosu7JCXu/pGl1FdZ/Inf" +
                "jEVGB/rsqsjtVrvn/f7987+uvp97v52uPr6Q/zDt769/uz9Vns" +
                "r0/lMtaeSNZHSyx26D2U8Up3wZ7VZ1VRXVztPo1rq06HPvuoj6" +
                "OH358/zu+zfb7O+/tovubn8/Pvxf+N+1yP1qPY48xcxD0jtCLn" +
                "c6mDMMqKxcqoznik4Qh2y0p3UrV1F6nep/VH9yP0latXrsYe5+" +
                "taRPl17kJknRmuyqVq3oUrqNIRb+rZvXcG/dOJT0wFOaidvR9n" +
                "IXr+vO/575t3l7uxx5m5iJURUnVmuCq3q1KDjOQ4oj6u7Crukv" +
                "7pxCemghzUab2fHBf0jeVG7HFutRsZKyOk6sxwVW5XpQYZyXFE" +
                "fVzZVdwl/dOJT0wFOajTej85Luij5Sj2OLfaUcbKCKk6M1yV21" +
                "WpQUZyHFEfV3YVd0n/dOITU0EO6rTeT44L+snyJPY4t9qTjJUR" +
                "UnVmuCq3q1KDjOQ4ftyze+8M+qcTn5gKclA7ez85dvT8fbTn30" +
                "f3l/uxx5m5iJURUnVmuCq3q1KDjOQ4oj6u7Crukv7pxCemghzU" +
                "ab2fHBf04+Vx7HFutccZKyOk6sxwVW5XpQYZyXH8uGf33hn0Ty" +
                "c+MRXkoHb2fnLs6Pnzvuf3m5fWS7HHmbmIe0ZoRc7nUgdhlBWL" +
                "lVGd8UjDEeyWle6kausuUr1P64/uB+gX64vY49xqLzJWRkjWR0" +
                "us/JqqyrIrK+T1nq7RVVT3idyJT0wF9ueqzOqY07fPkw/np5jz" +
                "8+R3+O9xP887mJ/Pz8/n533O+zzz/ea95V7scWYuYmWEVJ0Zrs" +
                "rtqtQgIzmOqI8ru4q7pH868YmpIAd1Wu8nx46e35/z533e57zP" +
                "eZ9v+XnI5fVy7HFmLuKeEVqR87nUQRhlxWJlVGc80nAEu2WlO6" +
                "nauotU79P6o/vxidv7o1/nu5z5/mj++znvc97nme+Pbi23Yo8z" +
                "cxErI6TqzHBVblelBhnJcUR9XNlV3CX904lPTAU5qNN6Pzl29O" +
                "5PcfKvZw==");
            
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
            final int rows = 13;
            final int cols = 84;
            final int compressedBytes = 628;
            final int uncompressedBytes = 4369;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtlN2KE0EQhec5BC9UvNEbRQTvm3mSDeQ2IXkB51mSTR7Tna" +
                "mU3znVDRIwQ1fTdX7qFCw7TcuXaVreLR+nv7/f5+mp3/Lh7Xwd" +
                "9H+8nZ9d9/P0X37L+3/g3570+9R1vj+l/7XW9tJeosadvXjTgQ" +
                "muHT1V27uqhypS44z6ubO7eErNr0l8Y3UgQd3W55HY2fPr/Bp1" +
                "veMXr3hTk6n46KDCDVe6dBzpU+jMmrO6gKOl+jbuDa4JfffRnM" +
                "K+zJeo6/3ALrypyVR8dFDhhitdOo70KXRmzVldwNFSfRv3BteE" +
                "vvtojrPbqZ2ixr397Z7yTQcmuHb0VG3vqh6qSI0z6ufO7uIpNb" +
                "8m8Y3VgQR1W59H4sLet33UuDdsn286MMG1o6dqe1f1UEVqnFE/" +
                "d3YXT6n5NYlvrA4kqNv6PBIX9qEdosa9YYd804EJrh09Vdu7qo" +
                "cqUuOM+rmzu3hKza9JfGN1IEHd1ueRuLB3bRc17g3b5ZsOTHDt" +
                "6Kna3lU9VJEaZ9TPnd3FU2p+TeIbqwMJ6rY+j8TOnu/zPep6P/" +
                "633nlTk6n46KDCDVe6dBzpU+jMmrO6gKOl+jbuDa4JfffRnMK+" +
                "zteo6/3ArrypyVR8dFDhhitdOo70KXRmzVldwNFSfRv3BteEvv" +
                "toTmHf5lvU9X5gN97UZCo+Oqhww5UuHUf6FDqz5qwu4Gipvo17" +
                "g2tC3300x9nt3M5R497+F5zzTQcmuHb0VG3vqh6qSI0z6ufO7u" +
                "IpNb8m8Y3VgQR1W59H4sI+tmPUuDfsmG86MMG1o6dqe1f1UEVq" +
                "nFE/d3YXT6n5NYlvrA4kqNv6PBI7e/oDOO19PQ==");
            
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
        else if (row >= 1392)
            return value24[row-1392][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value24 lookup");
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 35, 0, 4, 0, 36, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 6, 0, 0, 0, 38, 39, 7, 0, 40, 8, 0, 0, 0, 41, 42, 0, 43, 0, 44, 0, 0, 9, 0, 45, 0, 10, 46, 11, 47, 0, 0, 0, 0, 48, 49, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 11, 0, 0, 0, 0, 0, 0, 51, 1, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 2, 0, 14, 15, 0, 0, 0, 52, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 1, 0, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 53, 0, 0, 0, 20, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 55, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 56, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 57, 0, 58, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 59, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 60, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 2, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 61, 14, 0, 62, 0, 0, 0, 0, 63, 0, 0, 64, 65, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 14, 0, 0, 66, 15, 0, 0, 16, 0, 0, 67, 17, 0, 0, 0, 0, 0, 24, 25, 26, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 28, 0, 1, 0, 0, 0, 3, 4, 0, 0, 0, 29, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 2, 32, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 34, 0, 0, 0, 5, 4, 0, 0, 35, 0, 36, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 38, 0, 16, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 1, 6, 5, 0, 0, 42, 0, 7, 1, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 68, 44, 0, 45, 46, 0, 47, 48, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 53, 0, 1, 54, 0, 0, 0, 8, 55, 0, 56, 0, 57, 0, 0, 0, 6, 7, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 58, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 3, 0, 8, 59, 60, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 61, 0, 0, 0, 0, 69, 0, 0, 0, 0, 62, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 65, 17, 18, 0, 0, 0, 19, 0, 0, 0, 0, 0, 20, 0, 0, 66, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 67, 25, 26, 0, 0, 0, 68, 69, 0, 0, 0, 4, 0, 70, 0, 70, 5, 0, 0, 71, 1, 0, 0, 0, 27, 72, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 1, 0, 71, 0, 0, 0, 0, 0, 0, 72, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 0, 0, 19, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 11, 0, 73, 74, 12, 0, 73, 75, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 5, 0, 0, 0, 0, 0, 76, 0, 13, 77, 78, 79, 80, 0, 81, 74, 82, 1, 83, 0, 75, 84, 85, 76, 86, 14, 2, 15, 0, 0, 0, 87, 0, 0, 0, 0, 88, 0, 89, 0, 90, 91, 0, 92, 93, 9, 0, 0, 2, 0, 94, 0, 0, 95, 1, 0, 96, 3, 0, 0, 0, 0, 0, 97, 0, 0, 0, 0, 0, 98, 0, 99, 0, 0, 0, 0, 0, 0, 0, 2, 0, 100, 101, 0, 3, 0, 4, 0, 0, 102, 1, 103, 0, 0, 0, 104, 105, 0, 0, 10, 0, 1, 0, 0, 0, 106, 4, 0, 1, 107, 108, 0, 0, 4, 109, 0, 6, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 111, 0, 0, 0, 0, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 5, 16, 0, 17, 112, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 113, 7, 0, 114, 115, 0, 11, 0, 0, 0, 12, 0, 116, 0, 0, 0, 0, 20, 0, 2, 0, 0, 6, 0, 0, 0, 4, 0, 117, 118, 0, 5, 0, 0, 0, 0, 0, 119, 0, 0, 0, 120, 121, 122, 0, 7, 0, 123, 0, 8, 13, 0, 0, 2, 0, 124, 0, 3, 2, 125, 0, 0, 14, 126, 0, 0, 0, 15, 9, 0, 0, 0, 0, 77, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 127, 128, 0, 129, 130, 131, 132, 0, 0, 0, 0, 133, 0, 0, 23, 24, 25, 26, 27, 28, 29, 134, 30, 78, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 135, 46, 47, 48, 49, 136, 50, 51, 52, 55, 56, 57, 0, 0, 1, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 137, 138, 139, 0, 140, 0, 59, 4, 79, 0, 141, 7, 0, 0, 142, 143, 0, 0, 10, 60, 144, 145, 146, 147, 80, 148, 0, 149, 150, 151, 152, 153, 154, 155, 156, 61, 0, 157, 158, 159, 160, 0, 0, 7, 0, 0, 0, 0, 62, 0, 0, 0, 0, 161, 0, 162, 0, 0, 0, 0, 1, 0, 2, 163, 164, 0, 0, 165, 166, 11, 0, 0, 0, 167, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 168, 1, 0, 169, 170, 0, 0, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 171, 172, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 82, 0, 12, 0, 0, 0, 173, 2, 0, 3, 0, 0, 0, 13, 0, 174, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 175, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 0, 2, 179, 180, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 181, 0, 182, 183, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 184, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 9, 10, 0, 11, 0, 12, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 22, 0, 0, 0, 0, 23, 188, 24, 17, 0, 0, 0, 0, 0, 0, 189, 0, 0, 1, 0, 0, 18, 190, 0, 3, 7, 15, 0, 0, 1, 0, 0, 0, 1, 0, 191, 25, 0, 63, 0, 0, 192, 0, 0, 193, 194, 0, 195, 19, 0, 0, 196, 0, 0, 20, 0, 0, 0, 84, 0, 26, 0, 197, 0, 0, 0, 0, 0, 198, 21, 0, 0, 0, 0, 18, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 199, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 0, 0, 0, 1, 200, 201, 0, 0, 0, 0, 0, 0, 202, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 203, 0, 0, 0, 204, 64, 0, 205, 0, 2, 3, 0, 3, 0, 0, 65, 86, 0, 0, 23, 0, 0, 0, 27, 206, 0, 207, 28, 24, 0, 208, 209, 0, 25, 210, 0, 0, 211, 212, 213, 214, 29, 215, 26, 216, 217, 218, 27, 219, 0, 220, 221, 6, 222, 223, 30, 0, 224, 225, 0, 0, 0, 0, 0, 66, 0, 226, 2, 0, 0, 227, 0, 228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 229, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 29, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 230, 4, 0, 0, 0, 0, 0, 0, 30, 0, 0, 1, 231, 232, 0, 1, 31, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 233, 69, 0, 0, 234, 0, 0, 235, 236, 0, 0, 0, 0, 32, 33, 0, 0, 3, 0, 0, 237, 0, 238, 0, 87, 239, 0, 240, 0, 0, 34, 0, 0, 0, 241, 0, 242, 35, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 243, 0, 244, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 7, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 245, 37, 246, 247, 38, 248, 0, 249, 39, 250, 0, 40, 0, 251, 0, 40, 252, 41, 0, 253, 0, 254, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 256, 0, 0, 257, 0, 8, 0, 0, 42, 0, 258, 259, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 260, 0, 261, 262, 263, 264, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 265, 43, 9, 0, 0, 10, 0, 12, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 70, 0, 0, 266, 0, 0, 0, 267, 0, 0, 0, 0, 44, 0, 268, 269, 270, 0, 0, 45, 271, 0, 272, 46, 47, 0, 0, 8, 273, 0, 2, 274, 275, 0, 0, 0, 8, 48, 276, 0, 277, 49, 278, 0, 0, 50, 0, 3, 279, 280, 0, 281, 0, 0, 0, 0, 0, 0, 51, 0, 282, 283, 0, 0, 52, 0, 0, 284, 0, 0, 0, 285, 0, 0, 286, 1, 0, 0, 0, 5, 2, 0, 287, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 288, 44, 0, 0, 0, 0, 0, 71, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 289, 0, 0, 0, 0, 2, 0, 290, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 291, 45, 0, 0, 0, 292, 0, 0, 0, 0, 0, 0, 293, 0, 0, 0, 0, 0, 54, 0, 0, 55, 0, 294, 0, 0, 0, 0, 0, 0, 56, 0, 0, 36, 0, 0, 0, 37, 5, 295, 6, 296, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 297, 3, 298, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 299, 0, 300, 0, 301, 0, 0, 302, 0, 0, 0, 303, 0, 0, 57, 304, 0, 0, 0, 0, 0, 305, 0, 0, 7, 306, 0, 0, 0, 307, 308, 0, 46, 309, 0, 0, 0, 58, 88, 0, 0, 0, 310, 311, 59, 0, 60, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 89, 0, 0, 0, 2, 47, 61, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 312, 0, 48, 313, 49, 0, 72, 0, 50, 0, 0, 0, 0, 314, 63, 0, 0, 315, 64, 65, 0, 51, 0, 316, 66, 317, 0, 52, 67, 318, 319, 68, 69, 0, 53, 0, 320, 321, 0, 70, 54, 322, 0, 55, 0, 0, 0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 323, 0, 9, 324, 0, 0, 325, 326, 327, 72, 0, 0, 0, 3, 0, 0, 328, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 329, 73, 0, 0, 0, 0, 74, 0, 0, 38, 0, 0, 0, 0, 0, 330, 59, 331, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 332, 0, 6, 0, 0, 28, 0, 0, 0, 333, 0, 0, 0, 0, 0, 334, 0, 61, 335, 62, 0, 63, 336, 337, 0, 0, 64, 338, 0, 65, 0, 0, 75, 0, 0, 339, 340, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 341, 342, 90, 0, 343, 0, 0, 344, 0, 0, 0, 77, 0, 0, 0, 0, 0, 66, 0, 78, 0, 345, 0, 79, 67, 346, 347, 348, 349, 0, 80, 81, 0, 350, 82, 68, 351, 0, 352, 353, 354, 83, 0, 0, 0, 0, 355, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 0, 0, 0, 0, 0, 69, 356, 0, 70, 0, 0, 0, 84, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 357, 0, 358, 85, 359, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 360, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 86, 73, 74, 361, 75, 0, 87, 88, 76, 0, 77, 362, 0, 363, 364, 0, 0, 365, 366, 0, 0, 0, 7, 0, 91, 89, 0, 0, 367, 0, 368, 0, 369, 370, 0, 90, 371, 372, 373, 374, 91, 92, 0, 0, 0, 375, 0, 0, 376, 377, 378, 93, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 379, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 380, 0, 381, 0, 0, 95, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 382, 383, 0, 0, 384, 385, 0, 386, 0, 0, 0, 0, 97, 98, 0, 0, 0, 92, 93, 0, 99, 100, 101, 387, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 388, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 389, 105, 0, 82, 106, 107, 0, 83, 390, 391, 0, 0, 0, 392, 0, 0, 108, 0, 0, 84, 0, 393, 0, 0, 85, 0, 394, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 395, 0, 0, 0, 396, 0, 87, 397, 0, 398, 0, 88, 0, 109, 110, 111, 0, 399, 0, 112, 400, 401, 0, 113, 402, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 115, 116, 0, 403, 0, 404, 0, 0, 117, 405, 0, 118, 119, 406, 0, 120, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 1, 1, 5, 6, 7, 8, 1, 5, 0, 2, 0, 3, 9, 1, 5, 0, 10, 0, 0, 10, 10, 11, 4, 12, 0, 13, 2, 10, 0, 6, 1, 0, 14, 15, 16, 12, 10, 17, 18, 16, 2, 16, 19, 5, 3, 16, 20, 3, 17, 4, 21, 22, 23, 24, 2, 0, 25, 26, 3, 27, 28, 1, 29, 30, 0, 5, 31, 5, 2, 32, 0, 17, 33, 34, 6, 1, 0, 8, 35, 36, 16, 2, 37, 38, 3, 1, 39, 1, 5, 1, 40, 41, 12, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 2, 48, 49, 3, 5, 50, 6, 51, 52, 53, 54, 5, 1, 6, 1, 55, 56, 1, 17, 4, 0, 57, 10, 58, 59, 18, 4, 60, 61, 62, 63, 1, 18, 15, 64, 65, 66, 10, 67, 20, 68, 5, 69, 20, 70, 0, 71, 72, 73, 0, 74, 2, 21, 75, 2, 76, 77, 78, 20, 2, 79, 18, 80, 81, 82, 5, 83, 84, 6, 5, 7, 2, 85, 3, 86, 87, 2, 88, 1, 89, 1, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 6, 6, 99, 7, 4, 100, 101, 102, 103, 2, 104, 105, 106, 0, 107, 108, 4, 109, 0, 110, 25, 7, 6, 3, 27, 111, 112, 6, 7, 113, 6, 2, 1, 114, 8, 9, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 10, 20, 0, 125, 7, 1, 1, 126, 127, 2, 29, 0, 4, 0, 128, 8, 2, 11, 129, 30, 130, 131, 132, 6, 13, 21, 3, 133, 10, 1, 134, 7, 12, 4, 0, 135, 20, 14, 9, 136, 137, 138, 21, 21, 12, 4, 11, 139, 1, 5, 140, 141, 18, 142, 7, 143, 144, 5, 145, 146, 147, 148, 149, 150, 22, 31, 151, 152, 8, 8, 153, 33, 24, 5, 154, 155, 1, 156, 8, 157, 158, 159, 160, 5, 161, 3, 162, 163, 164, 35, 9, 165, 166, 167, 36, 168, 2, 6, 13, 169, 170, 6, 39, 171, 172, 5, 173, 174, 175, 40, 22, 43, 176, 177, 2, 178, 56, 6, 9, 179, 180, 13, 44, 181, 182, 183, 184, 185, 186, 15, 1, 187, 188, 12, 3, 14, 20, 189, 190, 191, 9, 192, 193, 11, 1, 194, 195, 196, 18, 0, 3, 30, 197, 28, 15, 198, 2, 10, 17, 7, 4, 7, 23, 0, 199, 9, 200, 201, 0, 7, 8, 202, 203, 204, 1, 205, 206, 12, 207, 22, 208, 209, 2, 0, 210, 211, 212, 29, 8, 10, 17, 5, 2, 213, 8, 30, 4, 214, 215, 10, 216, 217, 45, 218, 14, 219, 220, 221, 1, 222, 223, 224, 8, 24, 47, 3, 11, 225, 18, 14, 226, 227, 7, 228, 229, 25, 230, 231, 232, 233, 234, 235, 25, 236, 237, 238, 239, 240, 3 };

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
            final int compressedBytes = 1564;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXc1u3DYQHtL0lnZ6UNJFsOmJcRZFDj0VeQC6KII95pjeFn" +
                "BufQlugBbOA/TQm33sW9hvktz6GKV+VtYP9U9pKWoG2PVaWkfU" +
                "/HwznB/lhhzg9kd1rt9Xv6pXH+V3IK7lp5+f38K/QF7/9xu/4y" +
                "v2csP//uvT9pv8Re6ofAtvyLPfH1/+swckH4iWjlzqF88dYZnP" +
                "Sr+IfklknacUlORvUhcP5P9GKzPXd6rxj1G1CWAFgsifhD51C7" +
                "BX7OKOvoD1hquVZAKuYCfkrf7xbPMIrxD/4Cb0HxsI31c09B+r" +
                "0H9st4n/eGD8DijT/Dto/3Ef+g+I/cfHxzX6D21CBwIqCNGUa7" +
                "MiUsZmxZ4ruABCpLZCEmIvJ2orDjIIzwvNWP2ltUL8tWb/99r+" +
                "L1P732oWazMHobT+Usoj+3+I7R92Ptm/JGfw8CLVv53U0C85fP" +
                "9DpH8HGXmBUP/Oj/pHQv27gGvUP0fwV1Tg7xcNE19D/L0y4y/G" +
                "783+62vOf31D/o2IP5818hbwh1ThDxkBf3gS9yYkeLrzIaHPTb" +
                "7CQvcc/RY7Y1+INbInQG1FQkJCQvIq/qvPf8MqjP/+rM5/ZxJl" +
                "QSGeyBylRxd7HccPe7ARP/xB7qEp/qc18f/NeS7+/dDh7zF/gz" +
                "QmvW3ePFi8GonMlGesNvokEosORNHIqRP57+z+9Wyu9kuTN8YS" +
                "jCQFWK3Yn3iw/0rrD0G2/rANte0p/xjVH94n9QfwsP6QFS6lx0" +
                "PXYCpNphYoECKR6lSKtNWf8pc4TF7/lsUF8in8n7n+ecSffYQ/" +
                "WP9Emg7/T+DeZ9//YvCfzcuL8E8SrD8XlIG2/KKV+OPU9ecs/l" +
                "+a4s/dMf5Mry/HuH7p/qF0/9Ld+vuJ8QtpthTWP1X3/gvibv/F" +
                "0/6tTfxtrr+/M+Yv3pnyl9X1+y/H/rOreeYveaOjkcP9v0X9/V" +
                "zWX+K9/hbkZcr/5/sHwNP+gdkTqe7/KFldLE178mOu4M08+l+E" +
                "g+rD2rB3QVQlI+wiGi9QaFd/ljbrzzMxPm/ppr7+32L+QvQHLA" +
                "fiz4b+hZbzJ6dbvzvhT0X92wAsufo3L3Mw+XyZ4xAfOUHStH5R" +
                "sX6LFPS2H688fEf8qu1fmGh+bEj+/HU5f85y+XM45s/vnvLnpD" +
                "F/3qd+yRisqvTP0/6J4fmLxvoHiER+9wb51ddP59K/gfnrpaj7" +
                "JF6B+hSgD9+3k0anaWTPJmUj5SnK8wJ3WeQMmD4u0itQkXELsZ" +
                "M5C5M4fB1VrmNSfFL7VjZzAL0XzgYsenL8DR1SsoRCUMuBOo4C" +
                "89u/y+az6A8ribpiSSZoIWq2m3Zao452b0W0ZueU4rSuCzHW2F" +
                "0/gbUxTOBlYCEZN+183DI01pkvOa7f0mX+8xH+DdVNcqrJLdmW" +
                "L+sawwSub4/srCiwpAR8sF20NZAZ4hfNSSvInSA5vnCjCQz9e6" +
                "+yGKSXAEQVwNTFT6SPpEeN0m2jhbSi3N0iVqQjY+QwPWBdrVE2" +
                "i54Ody7uhWbzw8+W/ldk/C+tE4Iq/8KLDKEu3+oo8Rd/4gK3pc" +
                "QLayKbA7H+WqSQe4ui0TCQu3ZfXeanp40fUP+QMtQ4/5j2b6xq" +
                "+jewf2IR+ov24xz/28z/VcwPjj3/V3LKQSVAYP8x2i/e/6nzHV" +
                "A1v1my2tj0PJv/819/eq/f3416+/7llT/Pn0PPiTQr/FPIe9w/" +
                "Ok8Tz7/ZQXFeGYFNO78456i5Df/Acf71nX89nf4jNW+qmPWgb8" +
                "mu2EH/ZZy/BfP8rWg1fzvt8x/H1P6Oz686kfzaz98uSn7zAdnC" +
                "/OE+nT+MZtFbzR+ygaiU679SmROl/ivWCsdxv3XarQOKBgkJaS" +
                "lRdV3/MCv0D8vpcLA1KrPybjdTxTZthJlwaf2+U2dlIctyuiyv" +
                "oyKrwrJCn+L41erzN0baX8vF7D9oA/8DFdTx3x6RRaFFL/wNXJ" +
                "bf5Ba2bFeVxU8wyZ9AjJ8ATsp/pLCwJ/8M/iv+DxXXZf+1jOCr" +
                "NaIRM3QLx5MQrKbaJM0rF9au7Obzx+To3g0pqwUVCFbkNk/f0C" +
                "mOyvxmEsg6pEURrTo6k/l9ahsCLND/rwyt2w==");
            
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
            final int compressedBytes = 1150;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUly2zAQHCCMC1kOqJQOyg1ZHoLyyc/wU5CcfPQT/JX8LJ" +
                "IlSlxF7ACp7oMWmySAmcF0DwmRRAAAlEJDJI+vgtT5L4r4+SM/" +
                "vhh9fP3QECOxI3PZ0QjSMF8aj/ShLy9zWwAh4D2Lyt4/GHWNL9" +
                "ovh+3Npm3C4h7O2IQ8W+6QYlbHy26s5hwbfQgaztzDptc0mxEh" +
                "9hIL36PDqQEL/jLgLwAALCDn8xAfb90mEFVOkU3kv52tfn+sRv" +
                "972K/Dnzv6KKaIjp8OrQZca9r+D+1nUP+UB4wK1I+aChC7+lW3" +
                "jCZrLKEAoCLpB5S3nszkEtnJn8bi/F96BnA/9Gz+p9n+g66r8H" +
                "8F9ddIP0jL8d/BdQh5I57Y1YAqaH/UTwAACeXMXqjhqoSLzLrJ" +
                "/9yH/zlfIPttGA6YNqHha+y2faWXKufdNJta6EQz21W5OMT66s" +
                "9YUNmdON8+B3ECJcE3OzIxeyYACxUjqSEByy7o56dy588Q/0C5" +
                "+Jc36kdhG/+ZApUX3h8aAtiY/Vf1+x3EL+IfAH8CLvnbRMvfiB" +
                "+KVqip68eQ9edrh+y9WcDk818OffPrMCBxcOUfemm42Ut6IMX0" +
                "76PHXw5BYhrxxjnt9sI86EbRT3oi/XJ4+7L/R9+fa7FfrPyl4u" +
                "lPBW6wzb/3nH/AX5u1X9NP7IkSgphlkM+LTSYgGKS9YC0y8Gxe" +
                "/yVHCv7X3bCzmOTMzgHAVosfz1QF/nWbT7Gvy8D+nukRGeDEpy" +
                "oRCbncf2nUBR64fwXjv6VMbaRMMzvdzdRBsc7WgdX4fel0lYOI" +
                "FtrnBdv3Kjv0Ke3wbnb4NqGHGcRyKgITc9NWT6a7RsVt3pc/4l" +
                "y/D83/EfYXjupJUXIHBPJfKf4VkcYvru2LYfs5KoFc9gvXf6X9" +
                "H1l/moz5a8sFEJ8LCujnuvi3uP/81w8c9ny1v3+f6fH/Yx3999" +
                "Yv9v0PzTe6aA2nk9cfXfvTlP3Z2f40rR+LZg6X+PHsfzr/u/Vf" +
                "Gp/+1+CnrfNXd/5Q138n67/ryF78Ee7/mdf+KfPXjfyZpX2gbu" +
                "TkDx/+tstfVCx/ldVf5dsP9r+H/pFOvag7fsrU/+up3wKgbIrh" +
                "ys73bOjSSY7f79U1T02c+TdhP2VtP1GVflP9Lzogfvho/H/n4u" +
                "dr+vFLh1FMIPP5V0VYfns3kMP4tJgMSeeLXv5vnXpru/cvq5w0" +
                "Y4KnbJ+ZfDWXLNw+YJ3s1Kp6G1UBJZjOyq/92fpDnpLCp7eLfm" +
                "Stfjwm9dejfvasw5rYtXjacwEj7r3323rhtmZlc1BT3PvrEG9y" +
                "5f231lf26y/ZkC+MHktz+tHfdII69HG35woySujzLxPxbmb5Xh" +
                "AVjL/+529vshxNtQpyc6sr9R1FBsvcXuj1k2T3r1mJ/QBUNKXW" +
                "/w7nn/HYf+35ew33bw1vZ436V+aaedX532S01qruX5zMolh/nD" +
                "J/3JP+jlmHqU6s4flJKypfL+7hG+Jjh6HnHX/Q81+28fyaxrWG" +
                "kNc99RT/6/b6I71ff2Sj64+mHf9/PTC5/Q==");
            
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
            final int compressedBytes = 980;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXV1unDAQNi6qHKkPftiHVuqDU/UgVk6So0xzghxhj9qQZV" +
                "nYYBbwz9jm+6SsiNhhzHh+vjEOEaJD2/8A+5DGdjKWynXXUkKY" +
                "/pAgn5O84wJy5ptq+Aip3/X9mUP4H+SD+z9z/jt6/i34/j9Edf" +
                "ephnNGSDOqt2S7z2+taIQ6jQRJCYv46800uoCenGg+D+xMGSDk" +
                "r2rit1hGfY34yS82vkYP+zcRh2WZ9ce/PBA7WrWjX11sAwLKiz" +
                "3yvm25Z/3LjL8GGpqKbfjV/mP3DENPrlVdtTSD5Psd/z25+S9N" +
                "+O9LIdax0ePHc/2leHnf+I+i3zt+UtgvSP/JO/8B6h+z/7L3T8" +
                "gfuQD9h3cHfUeinAwTpmZZ7dig32JmPKLBZqtfjmfezvEP20Xu" +
                "59fO3WWajn80V/7RBS71/MPJX3r5p/PAXyby7528ziLy3PxLPu" +
                "ZfYfibZ/9Sdubzsj8AAEC5+c+v/j5YeOHN3yvqX8n8Yd7+Ui7J" +
                "yETDsKm031RZJv0AP+z6dMEMWrt0sSRUwtJLTuMnRMgR+zef+h" +
                "+sf9ZZlh49Glpd4UVF2L8ALD0/xR/rHMj+tmIjh8z/myeFAt0B" +
                "q/56mgNu/wEqR4UcBP5/7PoJIH8AQMb1h+LXn/3730HUD1iQbQ" +
                "njbzF/4H8AUEKAmXAkGSSbl87JFd8BAAAIkXDC73/0hdo0/rj7" +
                "H0fyl/uXSfc/tjsb5lC6OfUDQF7IkBp/zV+vSZ8/BXj+pZyUNg" +
                "XTXZXWqF797NowfCe0AJLnz0v+Mu78ZYb8pWA/AACAJezmmb71" +
                "j7t+Hl1/Xt4E+8N/4rHIr1C392/diOJn16tCryS0C4umdvZUa6" +
                "q3f4C+u76WMs+WPO/5s5jA4vp3IUrff1/PPgy7ev7kg/nTCedv" +
                "P/kh/iHAZQ/Vk9TEf5oVg82Hjw35i4b6c1pbf15yuotw9Zfh/p" +
                "f7n3vPsRv9P3/+V/eWwIPy74T/v4M7/2NLa7b905srf//A+3Oj" +
                "EOeHJBqrsm5Ga2AGIH2dZty/BhwazwNDIyFJafFdmMb+7f3SfL" +
                "jg+ePw909Bsnvm8+dajJ/0P/HrFfZD/wPk1a20D32gZfbBFr0c" +
                "stF8/yj39Y/c+yeAaOsHeP8ZUEYTN11JwPizYUSF5b/tz78u5r" +
                "t78Ekz2kzjPsc4/nSezSCfv8L0INd96k12KIQAgKeQD/+j2fzx" +
                "luzvj7U//6Rs/SfC+y90bvtXbMX9R4T521S/x/K0034n0c7men" +
                "m5QXPHR+kqv4BmW/yN7//Gf+XNe5p+oJPyZIPF7zr9aka/t//+" +
                "B5hyx4M=");
            
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
            final int compressedBytes = 853;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnV2OozAMxwPDSqy0D3noAbI3ifYkexRrTzBHmKPOAi2CFm" +
                "i+scP/9zBq1TExsePYTUiVGuhUr4yaub9sx792+NOML29qQT99" +
                "4sDR/3VK6XX7RrXL9mmU/uj+q9DfFM2C5N7+e7oYyWf9b676/z" +
                "m8sg5t36v/NuR1Av2b9VtSeYmy36e7/cjDfiCWRUS6x6L0/pQt" +
                "/pioYV0Mh/unnPGXR/wHAAAAANdcBQBwEha3DgAAlTF8V0vzO4" +
                "MOCYfQBdX0v/aofwLWHwz8in39Kl2eef3f1W7/2Prh6v5zdf3F" +
                "9J/x1IAeeWfgHTjLsyfh+qe/tZFWiMCK0FIH+r/d8n87XG70/6" +
                "/h7pvB/5uH/48F6/7+pWn83OV/fs3jZyX/Ocinjo/9le1/+vCw" +
                "7/usZX8zvQAdAev8IdH+06P8jvj1wk56o6VYMYKb+tFvGaadLm" +
                "2ezEcP+4My86+0+duIU7vLMaxK12/pUwmHxHRPfyqnf3+m/VC/" +
                "Rfq/Ptf/S4w/1F8YfwBEc9nS3rtebE6WByAG+N9OZxgZtS8AIC" +
                "jBLpvkxK4fZqs/9Uv9ObXfZlx/1AXMC2ovUlz31fBJahpYDSB+" +
                "5UmAsR8B/icdQhcAxnQbS7Q0R4+t1dvOpPH/2P1DfufHhZ6fRu" +
                "/776Q4UGb9P5/9+M5/FNn/xOj8sOL2K3/+Yu9pfZO4feRv4Mr5" +
                "Q4L5y2Sbv7Le+en770y2uFZ//Rpuv1/Hz/84ygOwGLkmZSgoR7" +
                "vzepN6TvXD+YSg4vzNlmk5aP7cfP62cZNvFvmXR/t+z/+6tH+h" +
                "+GGLVcvgYpFrc/yZ/fFn5vqr9/fdZ+j+4epSv48EpksOYn8Z9F" +
                "+o/iqR/mXat5yS5RJg/y/IGTVehizH/PV6859R+P2avLkrl/rn" +
                "oH4JXj+ngPal2+/s7+8lPD8sLU4Qs/6Pz98yzH+G9f0zQCP+FI" +
                "s/dedvJ4yTpqz9c58/jvwf+beE/LvO+ou5ntWf35TDf4mV/5Ux" +
                "VMvYN9pIwdYxFTDMTPbkv+2L//7bi5/YPwUAcIF85k+Bzz+AHa" +
                "r4/Q7McyBX/QrWlSQb+4X8fgiOVJeciggJN6KnyC5bt4Q+f2rq" +
                "dxq5bgWQf8jHQn/4L/xfPlc/Cm3r/r8BbmnEPg==");
            
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
            final int compressedBytes = 793;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXVuO2zAMpFSjcIH9EIr9aIF+qMUeROhJehT2JnvUxpvHLl" +
                "DbsSNLpKiZj8BAoMiiRiSHkh2iWTjahYF2I65+K90/AEjgRGQP" +
                "K6iZi4+OpaLDCELeKogYOUyf422Ikfzl8m0lcJo+Pw2nkDA+E1" +
                "/bMY+UDrj/Hf27a/9uqX8AIL81d2HYylKoEOt5r/88fzv5r1HO" +
                "fx/Vf4H7f956/7975m9D+ilh/jUzz6L9Ub+oET/m2wd9/OH7Vh" +
                "i3eS82Nv+5/nsF4+ylff95WT98h/9PS+tnV/uC8yfNn4rFE79w" +
                "DVREJOwKaEIX/nt12If9Bmf9WuqciAlrEQAAS/WDquBi+v8Y/S" +
                "1ij1GD/XvHBv6FXP6ltsdP0uMXsJ+rtX502F97/bmcfilQf8b6" +
                "qRh/jrG/bv6ngvavk38hf0H9AQAAAABWAnys3rfL6V/p+aVuzh" +
                "8NlOby1zTNypv9Xyf7u8Xz+6v5r7eh/1pDS7u9ufxrffxAX+G5" +
                "z/vnPfEf/l8ZzNgPRwOLRfGy+S8r71833IGywwm3B+4tBF9gsQ" +
                "KATf1bQn/2pN8zkiy2wzi4VEHLG9h/N5e/bj7/1cL5IeGUXQF/" +
                "SvNfNxTz5J3/fOP/A/tntuNXImCjR4gwQ1/5z4b2vCN+hmUZ44" +
                "sszEPffwf9AP8NPJB/tjv/LeiH/H706q/c818f27PR+St/n0O3" +
                "/FWYf6njP+Lv1vmPZKT+pBK+hMvyi+pkNhVSKJADnberboM4k+" +
                "zrzCu13P9S7e74qcr4cTSnZ/2A+jX0N+VvfEtvnI/qp8hooWkw" +
                "3X9qvH4BsHB7AACq+c8gNJggbMzQNBUiYf/bqP4oF38LPb9uav" +
                "/tgfaqxi8w/5XjV7uyEOdHzca/w95fnxrkb6QG/n8O61e8foP9" +
                "57bX/2FeBPEb/AUAAGgf0P9AlmIEWsof8fyhkGOt3h4AzOqf0P" +
                "U0RcL+aYuRX7T+YnD80vVz6A9l8ScZts3Pm0Jm8jwG+kzRpZeL" +
                "GosnZr6eLn98I/Y0RPp1NcaX8Je+/1mNoKMWwybF7Xt4ftny+q" +
                "F/zKzuuw==");
            
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
            final int rows = 177;
            final int cols = 16;
            final int compressedBytes = 217;
            final int uncompressedBytes = 11329;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmEkOgzAMRU3KIktUcYAcJerJcvROUhUkKlDVDHbeW4BYIO" +
                "zY1v9GpAjT9jGJZqKA2fq7ze0wlUBtTTGLLM+r/1Q2rC5kPZFe" +
                "03+ZH33g16yRk5dboZg8ZQFoBvM3oAqY9o+pafwN8j/tP3f0X8" +
                "7qf/zD+wb9k7n8Fdap2v5ltP6cH2jmt/7F+OpmwX/jn+ruD63j" +
                "7/T8uvr/HZW/r31+YsH4R88f/wZgG/RrOP+E/lTq3xHyP96a43" +
                "t1drnaXnf26cnCsg3oZ0/z1/33R48f4Ct3qdc5eQ==");
            
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
            final int cols = 121;
            final int compressedBytes = 4175;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWweYFEUWfvW6uyYtQZIiiICwwMGCIEGWICZO0EPJrIoIgo" +
                "mMiIKBqKIgJhDDoSDxADlEPAMq4oGo94mHAqKYQAyYULw7BcS5" +
                "V9XVaaZ7p2d3Fvzs75vu6uru6ur66//fq6o3WkNgoEMcEnhQqw" +
                "yVoArU4JWhLjSD+lAATbVm0FI3WGFkCxTiIOgAHXWO+fEu0Bn7" +
                "4Gh4E7rpR6AXHuar4HIYDFdq9WEoDMeXuWEM0r6INYdxcAPcAh" +
                "P5CkCIQnljP87Qn2NH4Hg4AWppFfFRvT87JdoF8vWbWGPtJ2iM" +
                "PxrnQnM4Lbop/jg7jPfA6dAW58MZcDb8GbpoP+DP0EOris/CxX" +
                "CJXps9D5exHrGNegWcjj/p5WAkHoCxcKN2F9zMhrAInqu9C3lQ" +
                "GdfxqlANqsKJUB3HQE1tOtSGOqweNIBG8CetAqLeHU7V9kFraI" +
                "MRaIfz+HA4E86Bc3Eg9os/BufDBew37KgXQXfoG6sHl8IA7Ti4" +
                "AgZpM+BfcBVcDSNgNLwFY7AIboJbIz1AAwM47oEIxPBs/JUdhH" +
                "JQUasTeRuOg5MwkTgdk3AKew7q4Xr9lGh/aAhNoAW00i/BTmw1" +
                "dsBDuB/HQnt2JnSCs/TX4Dy+GrrCX+BC6Am9oU+kJ/SDIugPA1" +
                "kRDIFr4FoYFtnIR8AouC7WFq7XGsAEGM+fSiaNDngVTNPH8QI8" +
                "GNmL42BSMhlbnkxCM94kSZvRMZkUGBuX4pCk3AwN8xNzkknC+A" +
                "b2NN3dldKHo39NerbYvFg3wvg065yvUPn34hyjBW9q5Ufa823G" +
                "AHnHBUaRUY5y1kZeEefR1xIvJJPaMqdMmEznhLF87mZVm0+SSd" +
                "aD6liBnqii0/N4gO4kjCmfMKYnPkj6bNp0sWf1rHP9epm7T+xx" +
                "MKUu5tepmq9L+m4wwJBlaDPYWnc+f1rs453NM9yrjr9a1+Oj5f" +
                "k1iULtbtUGk/js6LP281uxmzaYz8JDdNcEquOZdlvtcb+HMJ5i" +
                "pti1rrePsFJGe5hK54Qx3I7LcDHcgUvwoP+3aKuolML4dfwl/i" +
                "KdHY6comrdR9X5LYnd5+rNP8r95PiC+FWeL19hpXADlXfE/uKd" +
                "gsfyGY01Fhjjj9EWycAtXj+y2X3OnlfvPdkuX2Isr8leqR0Mxt" +
                "hur93RLXy7G2PisepFOJArDDn1WL3IVcZxsi+29ZaMRZ73NE7F" +
                "OPK2iXEeaCup3fIovd79BPF4trrzGVxRTEt0t9tggh/GxOPbqP" +
                "yvqZwD/GVLq2E6fkhafSd+BLPgPtLqB6Ap7jLGwt2sMLFI8Bju" +
                "hY4oeQGqn0I3+s2IvE77y2XJUqupBoviQ+V10mrZB74AVF+7U2" +
                "AstJreTj2DbxMYQ37036xxpA7dOSxRR2i1rHFLWQa1otBq+WXN" +
                "TR6nboLH8t6RJsbRQgtjvRGdV1ZXq8oafGJiDPQ2mGlqdfRti8" +
                "ek1YLHu/hEodU2a6VWy9Rd0NeNsTaD8u4RWu3i+K2Je+SRay0c" +
                "jKGciTEcJzFm2koQX25rCZByQiu+A3fY/eVTaE95new7pFbTUW" +
                "o1HU2t9mAstJquCK2+H8ZLRm9z7LFezbTHelWoyx837XGsk/Ge" +
                "fjxbyZ+Q7yB7rN7mxrhXrGf8XWGPZY7EOLEuMd/C2LbHs6E8fS" +
                "PpJ19gYqz6ocSYjsRjoH6fqGdhrN5hY0z2eLewxzKX7LE8zhFa" +
                "begWxvR7EG6MPwrEQ6HVhiHssbwm7bHDY8ce231fYhy7G9ppH/" +
                "NJqRjLNtwp7LGdK+0xHZU9ptQY+pE9pr0mMT5d2GMLY6gonyJ7" +
                "HJtFb0vD2LTH0Yp2zz3Bssd09TwLY9Meyydc9pjOhrkxJq2eqz" +
                "DeRRg/pI+AR/WR8fMJ44fJ5+ooMOZtoEAfqg8z3uNnkM91a/wC" +
                "gTFvzanF4xd6LFMvwkWwfLCTl3gpsYB3UNclxjJVXh8eSQiMPX" +
                "qr1J815oUS485B+gRd4vVp78FY9d92ct9e3UdaLXwumdcK8lRu" +
                "Nfo9klJiA4+y7lO57fRKfGJAHbq70oSx68zGWJ5pssT2JsbqDo" +
                "UenBS/SGCcVjaxPro6WsnGeJTKVxirs57ep9gEN8au0pSPBI/B" +
                "ApPHlH4c5hOPn4gVwDyh1ZTTNNYUFlIpL6u7F0FHo1m6VhMu60" +
                "2ttnicF00sdGu14LGyMuQpxJo5PI6ucfNY9o/zgngstDq91S2f" +
                "y+KxZY9NHvtrtTwTPJ6ZzuNEa3rqakr58Dj6lKPVrhqmabU6cu" +
                "0sV245dSStTrQhfv3sp9XE48purRY89tNqN489dVE8pt9ik8f8" +
                "U2D8G1gOq/iXksdLoYbRXIydYk+SVq/EXfA3vocVwprEvfB3IG" +
                "biBzR2uhBW8338axePd3h5nDcwYXsZFo9hidRqSjl+tZfHYuxE" +
                "z7aSNTuQ1pLLiMfr/XlsaXU6j4VWu3i8wsG4WB6P9WdxdK3FYx" +
                "gQisddg3nMP/PjMWFc1cG4OB7Dk6k85t/y712lPaWOay17bNyQ" +
                "mG3aY8qtG3vftMeylcYRxs+47TFh7LHHhEvCa48pp3+aPY4KjC" +
                "V+AfY4Ukdi3D7YHsc/TsUYLvP6XKkY64289hietssVPpePPRYY" +
                "GxX9eRxLptpjeQy2xxc5GLvtscA4stXfHrs8yRNMnyuDPZYYwz" +
                "+EVnt4/KzDYxNjxWPC2BgGdekOiXGiqjFS8JhavqmJsTFR8rgz" +
                "3xdHB2PhV3sxVvVWGFPKhbFbq6kPjTIxlvfLsYbA2OGxG+PEif" +
                "4Y2/cKn2tssRhXD4Mxv9Yf42QWGMNzhHHPYIz5Z34Ye3SlfCiM" +
                "d1lzIILHLoznurT6LtLq5+OczyCMX4AaiWpynmud0OoYIWtqNT" +
                "1DWm1M1vKQ8BBa7fa55HFwgJciMba02sTYX6stjIO1Wh59tDrl" +
                "zhSMvVodxufi0wPLzk6r+/lrtfxCX632YpytVsddNgteVIpfiw" +
                "NsMg7BS7CR/wL/hM2wAVvAK8YRuodGvbw2NsPWSF4rNsIC9Uw+" +
                "EgexBjZA6gFIHMeGXOKEbVGyBGviyUh8wNquN8qRPq+T3mpc82" +
                "tLbJkMubEefhi7fa7AJ+ultKndb4Mxzm7ThhYzizEipdYvp7VM" +
                "XeFzZfx+e54Lhdf2ql3ea6oUW6vjUX7Y1Goc7sxX89rQku/B7p" +
                "FNUIjDTHssedzZ5LGl1TzfnK/OpNUWxuZ8tdLqAaZWG0V+Wg3v" +
                "WFot5qvTeRys1cqm+Wg11KRfbT+tFvPVFo+L12o3jwO02iCtvs" +
                "5fq00eu7Xamq9OxThYq635ahzhaHVcd2n1BFOr8TYOOI14/AFO" +
                "xVvgPdiJlt+/AyfhzVwyEW/3ZdoUV23OydTbYHt2PC7NZvHYmu" +
                "cK2nBfEI8j1bN84zbP2Yd2iddnUca7fjzO8Ex/73w15bxvp3ap" +
                "40cWj+FjXK14vMbxuRTGlhceMM9Ftekdzudy89gej9Tw87n8/e" +
                "owPLbscQCPPwnjc0XyS+9zSb/6vmAeUyqDz1U8jy2fSxsvefxp" +
                "ml+92+QxpSyM91DaHjulYPxMRoz7ZsZYzHP5YeyMncoCY+1gCs" +
                "afhcK4IDcY663LHmP3PJcH470Wxjab6vMZUfk+3JqiaG/iO/gW" +
                "voGv2zmbMysPbgmrUdH80ilzqs8Fnxfvc/GNR8/n0gtL8zSOda" +
                "8tZva5UtrhC3X8Br6Cb+n4NXwZ2Q7fwY/wPW6D/WEw5puywxj2" +
                "BdTRNXYq6ZY+z2VfMdJqNibQC845xsay0mIcoo9PCMD4B9XPCi" +
                "ytjt6HX5haHb2f/OqfnHkuetdXflrN389uDkRotZ3vtsdzjqo9" +
                "PhBKq3fmRquN5aXRapwQRqv1JgFa/R81B1IBfoZD8AschoN0Jj" +
                "0xjdN1e7Y51juIx3hDbrTaGHWs/OpgHmf2q+G/ob5tVal4PCGU" +
                "PWgSUMP/qeOR2Fzlc/3Gf4kAVFH5at1JfXsN2R9ehaSvzzXD5L" +
                "F73cnNY5VCz/tdPBZjJ/DY5OB1p4wtP9IPY++6k4fHM/14bGp1" +
                "AI+zWncy1vivO/nzWKw7eeJTMDhGwOExjvBfd2Jg8ji2lEoS8x" +
                "pL6LsOU/4wdadrtjG2hBUmy3CLhbRZIp4rGx47cSDZ8zizPRZz" +
                "ICF4/GypeLyi2BpMyVDDqcpej4h0wsVslMC4GKtephjnwufKNc" +
                "bZzoEEYryu7DDO5HOxka4WUT6XTFeBO2NfmlrNxpjjYzaaqYgF" +
                "Ec/lr9WRXuG02ncOZM7vT6v1SjnS6ldKo9XW+Lh4rU6pi+NzqX" +
                "gurKXNY+O1R5LJ+APyjkmU18LR6rBrEpHe3jUJ7dHUNQlVXp2w" +
                "4+P0NYlstbqMfa5wWv1qaXjM62an1djET6vjy4FJXsuRBlRiN1" +
                "nzXLQvEBgLe6w9Zo2dfN9FY6dIn8xri+55riB7DMXqdXZri87Y" +
                "SeVmEc8VjHF28VzG5uC1RZ+yW/hjXHw8F+W4xk6u3LR4rvgWNt" +
                "mM59KWmvFc2iK57rTQGTuJeC5z7JSm1X1TtdocO4XTam3+H1ar" +
                "3ziGWq3iudgUNlXZ4/ra5kQHqMKmaVudsZPgscBY25IB436lwD" +
                "j/92ePcV6OMN4TDmNtQ+4xZreZGMNj7A4TY+09bbvgsbZNe5/d" +
                "LjDGSQJjNj3R1VpbZHe6fS6xtmhjXFRyn0to9e8NY6HVOcH482" +
                "PPY7HuRL/lKl2JzTLtsfC5oIDdo9ad2lmxPpbPBavp2MBljy82" +
                "7bHmiV8xfS53rI+fPfbUsHExPtcy6BI9UEp7HDLWJ7o/kz0OF+" +
                "tj7Cs7e2zF+jj2OMXnUivCWMuar2az2Vy1ftzCvbbo9qutOBDC" +
                "uLPpV0M3LIRe5FdfKuarhV8t/7dYU9tr+tVh1o/ledp8tYOxe7" +
                "46ppV2vpo9EGa+Oga5ma/mFct+bdHEmM0xMXZp9YPW2MnCWNuv" +
                "feNgrH3vxZg9lAHjy1Iw/s4H49mlx9hnTWJOdhinaHXQmkT1HG" +
                "Fc7WhhbPE4PfYWa9nfRxgrbotIbvSOj9kjGcbHA1LGx9+FHx8H" +
                "zPGEjtk7NuPjcFvkcNmNj33azKPV7GG7Pbz2eJ75fydolnjAtM" +
                "fYjJ8h7DE2EvZY/N/JtMd6zMRY2GPC+HJhjx2M6deGd8jWHpv/" +
                "dyqJPfb7v5O60qok9jg2LTf2OKofQ3v8ogtv8z+JGym1gC1k87" +
                "n0nfNmWrG3AmP7rQpjiYLCWPZX6d3p5R2M6d4O4WJv3Rhn4nEs" +
                "IL4vHWP7SquS8Dh2e254HD0zC/Xxjb3NlsfpsbdsqV4DNtFxkU" +
                "64sMWeJyaZ81xh4jIjgzJ+wfrstDqgzQ5kq9XZbA7GwX51lmtq" +
                "k48mxrIUG2O2xPKrKZcwFjH0tFcx9EyqQDYx9JErvPZYrxU+hr" +
                "5s7XHJMNYr5Qbj+NRjz2O9IZLO4/lMrYGxF+w7dpg+l3z2+JSy" +
                "8tJ8i8EZv2B7Lnh8tDDOVXx1PAvNL2l8dUpOWny1E3vL1luxt3" +
                "pbb+yta766g+9cppgDGZIaz+XMZZZs7OQ3z1WSsROlSjB2iq3L" +
                "zdgpcerRHDulzHPtTo29ZRt8fRnJYz2jYkSuDDka+IPyuGy2bM" +
                "dOKYiqtWv4P0w6IP4=");
            
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
            final int cols = 121;
            final int compressedBytes = 2038;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWnlsFFUY/+bNm512t1QotxxiBUqB0ogC4agXNwjKIUYI4S" +
                "oB5LbhvkswQkHDKZdAADk0/ul/RhMjCuW0okQTDgGjlgoxUWLU" +
                "ZP3m7ZvZmdm3nb06nV37knfMN2/ezLzffOd8pD1IQMEPAemLYB" +
                "DyoBm0IT0hH8edoAh6+DqQYtKb9A8GSSEpghJ4BkedSSsYQtqQ" +
                "AtIDRpN+MJ50UWfBVCglfUlrmAvzSVs6kvTBmR1gKSyDNbAO1y" +
                "NYc32PB1mBltAqaCrQmffdWPskPEWeNs71xfosDMR2ODsey9qJ" +
                "MIn1uy3rLGTtcqyrDVoONGV9C2gOj5rm4rNAARRCV50i/4oUfG" +
                "7or7bG9nkYZMwdCS/y0Rh41aDOgOmsnwWzYQEsxtEbWFfBWmxl" +
                "rD6sKmTz2Y2gMeubQDtOeQJrR2O17tATepnfxpcPA5D+HLyA7T" +
                "CsI2AUvATj4BWYwK54DSbDNOxnwhxs55Hu2C6CMmyXYL0LK7RZ" +
                "tBTHNVirpbPBoHROOg/36Ux4YNylAyJVSarIRXKOnNWp5Kugra" +
                "iz7RRyyU4B3EEd40RL9vlYZmkYJ1I0jPl9zgVdLXSGiOrLT2ZN" +
                "qZIjsUmqki5IV6RLZCNdLF2WviZrDZTWk9Uaxjh6U7QGKTdhPC" +
                "e2+yaLsT/HHYz9/qAHSpIYX+Q4tZeuQg1dg3z8DVK/hd/hPspq" +
                "Ex+bZTW/BmU1tkxWY98Paxf1dXYGZTXr29LVIVmdOB+HZXX8JX" +
                "mM1dbpiLEmqyP5mG4zKN8J78JQom87ra7Od4mPJ7vEx5MygI+v" +
                "6Xys21x0h68ibHNJN6w2F92l21x0j9DmWmCzuXamt83leysDbK" +
                "4/QjaXhrH0E0P9R82uxsrtaukOFNllNZSIZDXDeBFeWWqS1e+G" +
                "ZXUIYzbKdeLjEMbRZLUI42h8bMaY9y3wrW7bZhaI+Bj6+89EXX" +
                "uMacwx5kcGxuxI5lQDYxw35n27KGv3FPOxjjE/GhdxnQljEx/f" +
                "5b325f4t/QL/2K76FyX0Xl1Wi20ui6xeXJeymhxxWx+T9xzv8a" +
                "fnZfXPXB8fAIkepvvpIek3egry6HFoQw9CPj0iVUMRPcn18Wmp" +
                "hr1XCb9qn+VtxyPG2vdbavEG3sd6LHE+Fp51j49vpSMf2zC+p+" +
                "tj+iGPgdxns5shDe8E26ETtrYYiPRAGAOpQFldBlNDdjXXxx+E" +
                "9TF/DmLm42j6mB9F0ccxcPBCER+H9DH2zS1UTR9vFeljtLluR9" +
                "XHW8L62Dj7jqaPTcfc/9T0sYnaiPdNDIpNH2MV6mNjBtPH2Nv0" +
                "sdmuDutjCbg+foQ0Jk1IU8LuT1qI7WrS0iY7I7xUdYk7dnWMsj" +
                "0vWVktT0x/u9rYjRFkGBlKRvKjUQljvNQl36nGHYz91ZmDMb1j" +
                "2ZtPGO26FeOYYpnLnGOZ6cTHtcdA6M10wlixaAyZRfAUG5dqGC" +
                "v+EMZyQM4RYSzYZ8RYyaqf7z8FfPzQC3ws56ZklbaWo/bCHUOM" +
                "Ea/c2vjYW6Wu+dg1jB9LCR83tdnb80J8DMVWWa3zcVRZvTw2Pk" +
                "5GVsMG9/zjADjeY0p9y2ood3jCjbrvJPcJ+U6cXrvvtDmq77TC" +
                "6jspzdPcdzqTAb7TppDvpLTT49X4hoNC8WqluzlHwGxzhXIEND" +
                "6GIfyuo3kMZKUWr2YUxDhsc5nj1bArnni16Vw9xKsDhekYr2Y0" +
                "xFgpYBjv0ePVynDjDTluxBJtie3fIj9jxKuVYZH/FuPUqfX6bz" +
                "HQzQv6WF0X555Z4tXyYK6PVyjLpaPKSk4dK/aPlVWOT1PhuO+f" +
                "uec7pef/Y/g0Uh9rfBznKp8bttUJjjFKNLs+ZjaXoY/NNpc8TS" +
                "irEV91t6aP7bJayUpcH4tktXv6uJZ8Lg/rY11Wh/SxPJ3r421Y" +
                "t7L3E377nI+dcwROeCmWmYIcgQyIc4VzBKQqZZ+Wz8Wol+VVVn" +
                "2sfmTXx+SHmPTxXu/qY3IzXfxjcispfcw1sHIAMd6qYyyv19qs" +
                "MlvkrqUzH7tVAhP+T3xMWyUVQVmp87FyHGqyLkC1vEE5Kpdrub" +
                "fhnD06l85L0K4+Js7Zo/OT4+OA8H9Q1vkMxXhRMnys5+yZ3m8j" +
                "7lVVlGjYyWBalUzJy1RJavQxSPz9dlrzuXBUZPePoSQ7W84R5X" +
                "Ox+aZ8LuVjcT6XIzoO+VyBsuTyQORdtpnR8kCi8rGbeSD6P4nk" +
                "8rlCGCtf8rkRGGcPCGEs79Vz9iJtLhHG5vzqVGIcGeeKH2OzzR" +
                "UN4+h87CbGus2VIMb3nHldMf4Am3P2lOIEdcsWr8jqurCr6WYv" +
                "2NW2Z+JxKXoA62HlMj2kbFeuKDvocTw+iJVlQlKmh7On0NO2q/" +
                "fFcAcjZy+ldvX+mPYmQ/JAkrOrLV+9Eefy52lxLkbjcS7O9f34" +
                "HvA4F56xxbmwtcW5+JEtzmXQXYhzSTPTPc6lklTEuVgM5IbVP6" +
                "79n4RSHJPvdL0hBuKVGAiX1XdRVj+kp/x3RLJay6/2iqxu8J3i" +
                "951s8dFC4ddRSap8HWvPAxFcdakev/8M0cd1UXwjWNspEmOnvM" +
                "xMw9gbfJwiVLsS9Lv0/Grfy1H2zDG/2q0SqGzg48SLfM030Ife" +
                "tXxV/p7v1HqRf2zayfJ6wPhiA8ZxSzVLtq3ay8muVntHxjJFdr" +
                "VvcH3a1RmTextnfrXVrjZk9RCzrFYne15W/9XAx3Hr46EWjLd5" +
                "HeMGmysBT/I/Hpk9EQ==");
            
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
            final int cols = 121;
            final int compressedBytes = 917;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm99vDFEUxyfH7tm1rQc8lJCGSFqVIn6E0IRIPFRXSYUIQR" +
                "CNIhJKo21oqrQJXqSiQrxJVR+9+BNUSyuh0ngQz8QzT3Kd3s7O" +
                "7szOtnd2x+6d6TnJ3TP37p3Jzv3M99x77u4KkTKsFyL2TGQYdF" +
                "tHfcLFoEcU3cqrVXrBkvyuvuBH6ijxU4TGcB8QW0gSX7q/aH+O" +
                "Matw1MtL9XnLa5mxZ8ZJi/GvIDBWM2ZsG40GqMfGacay1qi9jp" +
                "PM2LOOm1I6nrZ4r/aMm5hxPmZEqJRJxkOS+yHHiI3BJxiHUXhH" +
                "/XbJlhHr3ANZV7tkXJZ9Jsz6DdODrVeFscxWr7LVNhlb0rX4IL" +
                "XsNhkfUbmj+CvHZ1rqet+r5WuNsc6dsbHH2Gv1TRr7reOjLtdq" +
                "sdW6TI+21kWmX2y1rPFIqoHKQSrmKBjHqJxx9LliXKPXNhqFYa" +
                "ND8jycqeOcuvBBx/HPvuj4hNJYtLOOacQnrWh9PM14YSuvucIZ" +
                "q837m8KTiVHykwu+apwfX2TGeay6TqV1nPii/Zqrkxn7Sv/07D" +
                "r2ZpH7RVxFFjwfe767e/rxizww6VXiOZQrs0i/EGVTsm2zTRMb" +
                "YRs241mogfWy3wBUAa2JYQVUwwbyO6msNfvugOXSr4w8gu3kVx" +
                "Uwp24t5Xwc0N2OWtXcydLx+Wwdz+ROGefmzJ2suq+5k+KKsuDc" +
                "Sb4TktwJKl2ehiwdQx15U8d4QUXH2OLUsdfcqRAdFz9W66jjdO" +
                "40N2MqkrHVYmOMV90YUylhrM47twhlrFbRMbZm6tjJ2F3HzFhP" +
                "xnhdJVarMcY2ZqzTmmuO88xYndEiGZOXjG2rtPa0jqVnxqFhjB" +
                "0cq8PAGDtzM+b5WGfGeKvQdTWvuYKvY+xSn4+pjefjQOZOKR3j" +
                "bdZxSHXcwzoOL2O8a53bJ+aFBZ1xDvK2751y7YFgM/YG6Xsn1r" +
                "HX+Tivfa6HzDg487GM2o85Vgc6Vj/FJ3PpGJ971vEA61ifWI0v" +
                "cAhf+h6rB5mxPrEah4vz+2qO1aUyfJPdFnUQnP6fRDQx8z8Js2" +
                "VE4ZmaoCvFmbEW8/FHHMFRJH44jm/xvU+xeoxjte96/OA9VuNr" +
                "Kt+t9+9kndENN2fbAynFb+hZx//tCfrNuVOQY3XqaLZ9LqjDP7" +
                "zPFdjcqTcdq/Gv9F0cq8OqY3/z41icdazPHogQsYT/jKPfmLE+" +
                "eyAqFgvpnsf8iNXGPwTv9AY=");
            
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
            final int cols = 121;
            final int compressedBytes = 499;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmstKw0AUhuXY5mxcqAsvKNKFqIVqQUTRrrzXC76AK0HXbn" +
                "wFK4gotlArgnfxstKnUCt10bfwKeI0pIXeaKspZE7+gXSSkw5t" +
                "z5f/nzlJTbN64/b8HsVMT7TWH4m/igYqxMaLjsI0SRHVB2nUjg" +
                "xRj3rto2EaU/2M2kbsM9PUa/X93EFTqg/845tNgHHDOQtVZsyd" +
                "zWDsewFj9zBuko67wFg84wAYi2c8qCNj7VdXoT+OC6stUhSxGK" +
                "veYly0Eg/mGdvvBGMXMOZZ0/Qf1RrrP0b+9K6deM55r/an4NWu" +
                "0fE8dOypq+DV5r5QduYN2dHZq3mVo7zMS7xiZHjR+HLGq400vN" +
                "qN62perzoWOhbfeAM50JjeJkWVTtdqeEB3yXEbMqdX7VQWc+I+" +
                "1y7mYzfOxwVt75WMTFOWMvRJH4XIex2f942ca+Tn+5S1qDXEGE" +
                "0rxjEw9oDPW4z5AIzF6PYQ87HnVJzO6ZhP4NUC9XxaYBpDNnSu" +
                "j41tYyu354tXr4+NHY7n62Nfsq7/7CVQH7ulPuYk7nOJ9+MzMB" +
                "bPOAXG4hmfg7F4xhdgLJ7xJRiLZ3wFxvLr4woxJ54fX6M+dk19" +
                "fAMdi/fqWzD2JPe7EqJ47qQ3z3voWDzjBzAWz/gRjMUzfgJj8Y" +
                "yfwVh6a/kFdhjORA==");
            
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
            final int cols = 121;
            final int compressedBytes = 553;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmjssBFEUhuXs7r0nodIgRFQeiUe8g5bdRSREKBGdREKHeE" +
                "QUFERBolFpFAqi8YhCKLy3FhWlRCQ6nTFZQ7x2GGblzvWfZPfu" +
                "zszNbM6X/7/nnh25QmHDoEbDNijl3fckA6FZyFXkQBOSazF1fI" +
                "3seDcoU27IsAzJoKwXEVknTs1jJW+uKKIKqjHHPCqwjmRTqvme" +
                "TjlUaI7V5ivXOlNFadExQ5xQpTlm/eKXlYGO45zlx2C8KQ7FsT" +
                "gyDBERB2Csbogz54zFepTx1ocrXWAcCIOxOjr+5JgLjOU2GCvk" +
                "1TvQsfaMd1Fzwat/5NV7YKyQjvdRV+teV8dJx5dgrL1XX4Gx9o" +
                "zvwFh7xvdgrD3jBzBWizEnus3YPwLGqjC2C355EoAmkT8Pk8/k" +
                "YvS5sB7/hDGXgrE6jLncdOWKaE8kwmXQsbrBlc4ZP/W5uDYeOv" +
                "YtgrH2Xh0EY1UYc8gwAjNfzQ3MIn+erqtbuYXb7HXM7Y513Awd" +
                "K+PVDRTmzudn6KkpxlyPPUNPyZ6rpzr+4i6+c+tuXb4LK1Pj9j" +
                "0QmoByPF+rd9szRmjAuAeM9ai54rR36kXNpd7+mPt8Ny72QG7B" +
                "WBXGtl7dD6/WZH889PTJP2fn1Tz4rGP/wre8egA69oKOXyl6GP" +
                "nToeb6QscjznTsn4eOVdExj/GoxTjjwwz0QP7TTnkcOdCe8RRy" +
                "4O31mGfEsus9kGmsx8qsx0v4/1j3SHgEcbDW0w==");
            
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
            final int cols = 121;
            final int compressedBytes = 595;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm8FLWzEcx0dI8xIUxF02UYon52BbYYiiPeykdlb/BA+Fds" +
                "chzMtwZ48eFARRdDBrmTdvoni1q9q/QN1JRN3YZSgim2/pI5b1" +
                "2a1RXiFpvz9IQ9KG0t/nfX+/X0JKwnydZdiK+5eRlyWjCOkmUd" +
                "k/Jc/VTAd5LF9byRPyQvZ9snWqd3pJi9e3sTTpkX27e28jXS7s" +
                "rj57VnZ2iMT4Jomr0cg/1j7yjRsN/60PbaPDN6rojXCZuQB0zL" +
                "egY1N0rPmUZeE/i8kXdUyn/6djnrvRMZ3V0TGdgY6Nyce6jPNg" +
                "bC9jfiBj8X7Q+Zh+AGODGH+tSs11CMbmxeqAGR+BsXmM+bEeY+" +
                "Rj6Fjp+ASMa57xKRhbt3c6Q6y2uK7+Vg0d01UwNoUxnZfV1nfZ" +
                "L6rxsmwLsn306rAfavazb9Vc5e+jadk+we82GP8pn49oydPi6V" +
                "j2no59T5LSsRpBxwboWGNdRJ8xPwfjOqqrL5CP7dExv0Ssho7L" +
                "6vgKOrZHx56Wf8F/FpPXPQP5jTMQxGpfrL4GY/MYc03G0HFt5m" +
                "MRgv9szscsyRKV8jFLCQYdIx+XMhYNYGwWY9EUNGPc2asDHTeD" +
                "sQ01lyh6mUzCf7VwBhKwjl9Bx+bEahF3Ys6gM+C8Znmnn+0G9N" +
                "/UHTA2iPEwy7Ic++K6LM+2wdhcY3t3Z8zW9M+rxSj2x7bqOPRe" +
                "JCqvDU3Af7VuIgkf2GuFe5l0SaQK9zLFG/+9TJpRn8K9TNtV+r" +
                "ZirJ6Cl7A/vrU/HkPNZdDeabwqjN+BsSmMH/wBHiFuBQ==");
            
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
            final int cols = 121;
            final int compressedBytes = 560;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmz8sA1Ecx3lt7+53q0hQRCJp/SmSpmmD2b+NgdnSzW5gEI" +
                "NJDCQSYbPZTEKCGESCbv7E4M9ksCIIca7NobSuLl7lvZfvS9p3" +
                "9y7v0vt9+v39eX2lUcsKTFsFWmDGQpO2sVoayxmLfjlrY3HWaf" +
                "eNrMUZCbEK+z3IwqzV7jvsV4NzpZ1VZvrqwDJL2H3dHz5ZDHQ8" +
                "2yySn3GeMQ6MaRyMxWFMC3qv3qN3631aSu/SDvgw1vbBWCDGi5" +
                "ZlxNPHWsqIgbG4zUh4Z6ytZp/5Tp07DfvOnBkTH3On8t5xUpSn" +
                "pzV8A9xyrvcj/6xbPKb1dx3753+jY/8cdCxSzkUbGf1e/8xYS3" +
                "76ajCWjXHG020W9IVbsJ8Kvppz7bQNHYtXHxeIxzvw1VgD+crY" +
                "GPlPxrQLvhx0vAcdy5xzOVo4zJkhTX2MxmGNIQUbKM/4CDaQuX" +
                "ai48I5l7bkeb16HvFY9byaTsBYLMZ0wZ3xORgLxviKO+NLMBav" +
                "dnLNuW5lfOLSJKjni8d0z0nHd9CxOL6anrIIP3Dz1Y9gLGJeTS" +
                "/0jL0+qtdO9Aodq8WYbtKMzVJjwBh0z6uNIc+/O/WDsVx5tVkG" +
                "+8mdV5vl38Z4/E8CayCCxmN+jM1KMBaPsfseAbMKewTkZWwGi6" +
                "Fj/woYK++ra8BYeca1YKw843owlqE+NkMfc6dgP7nr46LoOAwd" +
                "K++rm8BYecbNYCxDPM6KzBHYD/E4R8dR6FgUHZe8AUiUUbY=");
            
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
            final int cols = 121;
            final int compressedBytes = 521;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm79KA0EQxmVJlrt5AsUgVv4B/4BGxWhh4QOIoIWllS+ghY" +
                "1iIYhioeAbqJ0YBC1sBEGjiY+gUVDBQivTCeuhiSRccnfRNexN" +
                "voFkLxcOwvyYb77ZuyhVOai/cCRWFYJ50AByEN4QLWXO9ZV86h" +
                "WDYsRZO0V3/kybaHTem0W76HHWhPPqyH8zLJq+1hglxJCztv7h" +
                "l8VBp+qcdVViTKP6GUdvwdgUxjSmVHTD79roJvLHR6vtnJ46tt" +
                "9Rx6bUcUDPNY78hbmOaVLuyz3d/Vjuoo5N8lzfEdnyYkwzBcaR" +
                "nSCMI9tgbB5jzbPTLBibw5jm5KW8kiml5I28kGlNWn0NxrpDZq" +
                "pnLJMlvmredcXyz1HZvUyxgrzzCVpADvjMx9r68SK0mr3nWgLj" +
                "MOyB0Jp3P0Yw6MLrYMyxH2O/mnc/tj+sOObj+urHRap9gPyFW6" +
                "vp0Jry9tXWdLV1bE2gjs3Sakrqnp3A2MR+7HPf6Qj3nVjOx8eY" +
                "nXjNTj51fII6Zq/Vp2DMUqvPoNW8tFqzr35EHZun1dZzMMbBtJ" +
                "rOwdgcraZUvh/HXFfgORBGWm3f6dZqqul+NaVB0k+r7XvdjO1s" +
                "TRlnwPd3vrooh1nkD77apdUP8FzmaDU9/cvshP+mGjg7aa7jFz" +
                "Bmz/gVjMPguejNez5G1LnnwjN7/LU6B8amMG74BKEJ+UU=");
            
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
            final int cols = 121;
            final int compressedBytes = 450;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrtKA0EUhmXY7CaZB1CJRkHwAkZBoqLWmlhYiIWCjbYKVo" +
                "K3gI3Gwk7BxheQ4JMoPoJvoaDVumiMysLqhl2YM34DyeTCNOfj" +
                "P/9/dlcV/dBSEz++jaspNRfsI6rU/GVQdQXvBTWkxoJ9NngNN/" +
                "+ZUd3ve0/+RU0He7/f9lJlnxW3ZqNxT+RfW2fr1E8w+bR07KNj" +
                "U3T8xdi5iGKsOz8ZO1d/YexcwlhCr9YFerUdvVr3elWv4i14i+" +
                "6DN+/eJ9Or3Tt0bE6v1sU0/DhThbF5fpwsY90HY4N0PECvRsdt" +
                "6bgEYxG5ukyutn3pSRhbMjtV8OP/26u/KXqJ+knWcUqZaxkdi8" +
                "hcK/gxOo7Q8So6tn4+XoOxiF69Tq+2ZHba+PgUff84ux/v/nF2" +
                "Dx0Lm502qR+ZK+THW+jYrMylt5Nm7NRgbGquzj0nwzj3BGNhfr" +
                "xD/fDjkB/vomPrr4EcwNh6xocwNo/xL8/QH/EMvVzGupaGjp0G" +
                "jIXl6mPqR64O+fEJOrY+c53CWFzmqpO5BGeuMzKX/TrW58kzzj" +
                "zCWEKu1tetszwHYv3SN9TAesYNasB8HJqdbvFjU/y44w3y78xw");
            
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
            final int cols = 121;
            final int compressedBytes = 389;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2EtKA0EQxnEpZjHQs1J8oYhuooIPUFGiO9cqUTAXcKHuPI" +
                "uCh/AGHsGViK58gCYufNzAhWMToqAjcSIZ6Cr+DUmTya5+fFU9" +
                "LSNpZsnct1+zsigrfp+U6eaTkgz47yEZlxm/L/vPRPOfsgw29m" +
                "F3Kkt+H03/vWQhZbVbs6lfn34ZR4etjN3Zp3F0nMc4OsJYnfE5" +
                "xvqNO9ur40eMQzHOt9wF9VMsX9SZ65IcW+/V7grjcIzddbwZb7" +
                "c2jqttz+MKxuZzfIOxeeNbjM0b32Fs3vgeY/PGDxiHZ/zHXWaN" +
                "u0zF7071InIcnWAcinG+5Z6on2L5oubxMzlWN49fmMeK5/Er85" +
                "gcN3L8Ro6Nn7neqZ/15SgB5+pMr0666dXa5nHSwzzWb9zhHPdi" +
                "bN64D2N1vbqfXk2Of+R4DGN1OZ4nx7bvQJJV6sf7caZXr5Fj8/" +
                "N4HWPzxhsYmzfewti8cRVjZefqHerHuTqT411ybL5X72Fs3ngf" +
                "Y/PGBxiHYtz1AXne5tM=");
            
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
            final int rows = 7;
            final int cols = 121;
            final int compressedBytes = 114;
            final int uncompressedBytes = 3389;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjkvuPAZiMUHj6TGZM1kBak0kXKqLGJAEkpZnUmfSAtBUQa0" +
                "BlLJkkwbQMTzGTOZBW+E82YDL5PwpIDTMdrKK0iuOS0Tge9nFc" +
                "PhrHwz6OW0bjeNjHcetoHA/7OG4fjeNhH8ddo3E8WOKYAQDe2R" +
                "n8");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245 };

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
            final int compressedBytes = 132;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3ckNwCAMBED6rxnJ7gB+yMYzBeTD+iCKlIiTvWCyUB/Ir/" +
                "oCaNb/bs/Xf+ULAMB+CeD+5/6J8wVg2nyyP8mP9xvO3/kC6N/w" +
                "Kl/d86v+QH0gPwBgvgIA9gv7BQAA9lcA/REAML8BAPMfAAAAAA" +
                "AAAAAAAICfVf+/oe/vi0vLhhm5");
            
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
            final int compressedBytes = 73;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt1LEJAAAIBLHff2ZBN7CyEZIRrrgEAAAAAAAAAAAAAAAAAA" +
                "AAAADgl16VQAD+DAAAAAAAAAAAAAAAAAAAAAAAcGMArwY9vQ==");
            
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
            final int rows = 786;
            final int cols = 8;
            final int compressedBytes = 58;
            final int uncompressedBytes = 25153;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt08EJAAAIxLDbf2ZBN/AtkoxQaAIAAMAHvSqBwP8AAAAAAA" +
                "AAAAAAAAAAAAAAAAAAwGUDiqg9vQ==");
            
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
            final int cols = 123;
            final int compressedBytes = 160;
            final int uncompressedBytes = 18697;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MkNAjEAxdBhh8KgrlTNvpXAdYSf3IGtn0QZ0/jB8TwmNN" +
                "Baa2gNraE1tIbW0BpaQ2torTUSrV8cVDjtOMjs+spBpvWDg0zr" +
                "OweZ+3rFQab1loNM6wUHmfv6w4F/M3iHQ2vM/m225yCz6xsHmV" +
                "1vOMi0PnCQOcMvHGR2zUFn128OMq2fHGTO8CUH/s3wd7tec2DX" +
                "0BrzZPoCBc9VrQ==");
            
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
