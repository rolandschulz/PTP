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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 1, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 18, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 110, 189, 190, 0, 191, 192, 101, 30, 35, 1, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 57, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 57, 3, 1, 8, 123, 4, 124, 15, 127, 5, 221, 237, 125, 6, 128, 7, 126, 173, 0, 238, 206, 212, 214, 8, 239, 215, 88, 30, 9, 216, 217, 219, 218, 101, 30, 114, 10, 220, 11, 240, 222, 12, 13, 227, 0, 14, 228, 2, 129, 230, 150, 231, 241, 242, 15, 16, 243, 30, 244, 245, 17, 246, 247, 29, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 137, 259, 260, 261, 262, 263, 22, 23, 264, 265, 24, 266, 267, 25, 3, 268, 269, 270, 26, 27, 152, 154, 28, 243, 271, 272, 237, 241, 273, 274, 4, 31, 275, 39, 29, 39, 244, 276, 277, 278, 0, 88, 39, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 56, 290, 30, 291, 292, 156, 6, 293, 294, 295, 245, 296, 297, 298, 238, 299, 300, 103, 301, 7, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 30, 39, 312, 32, 313, 314, 33, 315, 5, 316, 317, 318, 319, 34, 0, 1, 2, 320, 321, 322, 30, 35, 323, 239, 324, 144, 325, 326, 327, 57, 8, 328, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 329, 255, 256, 330, 242, 9, 173, 10, 331, 36, 332, 333, 88, 334, 257, 335, 336, 337, 258, 180, 250, 259, 338, 339, 340, 263, 265, 341, 342, 101, 343, 344, 345, 346, 347, 348, 11, 37, 38, 349, 12, 13, 14, 15, 0, 350, 351, 16, 17, 18, 19, 20, 352, 0, 353, 354, 355, 21, 22, 23, 24, 40, 41, 26, 28, 33, 356, 357, 34, 32, 36, 358, 37, 359, 360, 38, 42, 43, 44, 361, 45, 46, 47, 48, 49, 50, 51, 362, 52, 363, 53, 364, 54, 365, 366, 55, 56, 58, 39, 59, 43, 35, 267, 44, 45, 60, 367, 368, 369, 61, 62, 370, 63, 64, 65, 66, 371, 67, 68, 372, 69, 70, 71, 72, 373, 374, 73, 375, 74, 75, 376, 76, 77, 78, 79, 80, 1, 377, 378, 379, 380, 381, 81, 82, 83, 2, 84, 85, 382, 86, 3, 87, 383, 88, 89, 90, 0, 384, 385, 91, 92, 4, 46, 386, 93, 94, 387, 95, 6, 388, 389, 3, 390, 4, 47, 96, 97, 5, 391, 392, 98, 6, 99, 393, 394, 100, 102, 7, 395, 104, 105, 396, 48, 49, 106, 8, 397, 107, 108, 398, 109, 399, 400, 1, 401, 402, 403, 404, 405, 406, 123, 110, 407, 111, 112, 408, 9, 113, 114, 50, 116, 117, 10, 0, 118, 8, 11, 119, 409, 120, 121, 12, 122, 123, 1, 124, 126, 127, 13, 129, 14, 0, 128, 410, 130, 131, 132, 133, 134, 411, 135, 136, 137, 412, 138, 139, 140, 413, 141, 414, 415, 416, 142, 15, 417, 418, 419, 420, 421, 422, 423, 143, 145, 424, 146, 425, 147, 18, 148, 181, 426, 427, 8, 428, 149, 150, 19, 151, 152, 429, 143, 430, 15, 153, 154, 155, 25, 156, 157, 20, 18, 158, 159, 431, 21, 432, 433, 434, 160, 435, 436, 437, 438, 161, 162, 51, 0, 163, 164, 165, 166, 167, 439, 168, 22, 440, 441, 442, 443, 169, 52, 116, 170, 171, 172, 173, 444, 445, 446, 174, 175, 176, 177, 23, 8, 178, 447, 448, 449, 450, 451, 452, 101, 453, 454, 179, 455, 456, 53, 180, 457, 458, 181, 459, 460, 461, 462, 463, 182, 464, 465, 251, 466, 467, 184, 183, 185, 468, 469, 470, 471, 472, 186, 473, 474, 187, 475, 476, 477, 478, 188, 479, 2, 480, 481, 56, 189, 482, 483, 484, 485, 486, 190, 487, 488, 489, 490, 491, 191, 192, 492, 493, 494, 101, 194, 495, 496, 193, 497, 195, 498, 499, 500, 501, 15, 260, 27, 502, 196, 503, 261, 504, 262, 505, 268, 506, 270, 35, 197, 198, 199, 200, 24, 507, 201, 508, 509, 202, 510, 271, 511, 512, 513, 15, 275, 514, 7, 8, 57, 9, 10, 203, 515, 516, 11, 517, 518, 519, 16, 148, 520, 18, 276, 521, 59, 0, 3, 522, 523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 533, 30, 534, 168, 535, 536, 537, 32, 174, 538, 539, 540, 179, 541, 35, 542, 39, 17, 543, 544, 204, 205, 545, 207, 546, 208, 547, 209, 548, 549, 210, 550, 551, 211, 552, 212, 60, 553, 554, 555, 556, 557, 558, 559, 63, 64, 65, 66, 213, 560, 561, 562, 73, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578, 579, 580, 581, 582, 583, 221, 584, 585, 586, 223, 587, 588, 589, 224, 590, 591, 1, 592, 279, 3, 593, 284, 74, 75, 76, 88, 594, 4, 54, 595, 101, 103, 596, 215, 597, 598, 206, 599, 600, 601, 602, 5, 603, 604, 6, 605, 12, 14, 606, 607, 608, 29, 609, 610, 611, 217, 612, 613, 219, 220, 614, 104, 615, 616, 617, 618, 619, 620, 222, 225, 621, 226, 622, 182, 623, 227, 15, 624, 625, 626, 627, 628, 105, 107, 629, 630, 631, 108, 632, 109, 114, 115, 116, 228, 633, 122, 634, 635, 2, 636, 123, 125, 130, 61, 20, 637, 638, 229, 639, 640, 139, 141, 143, 144, 145, 55, 641, 642, 643, 232, 644, 645, 646, 146, 7, 21, 22, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 158, 4, 662, 663, 664, 160, 161, 159, 665, 162, 231, 56, 169, 170, 172, 173, 666, 178, 179, 180, 667, 181, 182, 183, 668, 6, 184, 185, 186, 233, 234, 60, 235, 236, 669, 61, 62, 184, 67, 68, 69, 670, 671, 8, 9, 672, 673, 674, 675, 676, 677, 678, 679, 680, 681, 30, 39, 40, 682, 683, 684, 685, 686, 687, 688, 689, 690, 691, 692, 693, 207, 694, 695, 696, 697, 698, 699, 700, 701, 702, 187, 703, 188, 704, 705, 706, 189, 707, 708, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 731, 24, 25, 27, 62, 732, 733, 734, 735, 736, 190, 737, 191, 738, 192, 210, 193, 739, 240, 740, 244, 741, 742, 194, 743, 63, 744, 745, 746, 747, 748, 225, 749, 195, 750, 751, 752, 753, 754, 755, 756, 757, 758, 196, 759, 760, 761, 762, 201, 763, 764, 765, 766, 767, 768, 10, 769, 770, 771, 772, 773, 774, 775, 70, 7, 202, 203, 776, 777, 778, 779, 780, 781, 782, 783, 784, 214, 64, 215, 217, 785, 71, 225, 226, 230, 1, 233, 234, 72, 235, 236, 237, 238, 239, 242, 247, 250, 251, 254, 255, 786, 289, 787, 246, 788, 0, 789, 57, 57, 790, 791, 792, 257, 264, 73, 265, 268, 74, 290, 793, 65, 794, 218, 220, 222, 228, 231, 243, 248, 795, 249, 238, 796, 241, 797, 798, 799, 800, 801, 59, 253, 75, 802, 803, 258, 259, 8, 804, 293, 267, 269, 805, 79, 806, 296, 807, 270, 271, 272, 273, 808, 809, 300, 810, 245, 811, 274, 275, 277, 812, 813, 247, 248, 814, 249, 815, 816, 250, 817, 818, 819, 820, 251, 821, 822, 66, 252, 253, 823, 824, 258, 254, 825, 826, 827, 828, 259, 829, 260, 830, 831, 832, 68, 261, 833, 262, 834, 835, 836, 837, 80, 278, 279, 838, 81, 35, 73, 82, 83, 77, 78, 84, 79, 85, 839, 80, 280, 281, 282, 840, 841, 263, 842, 283, 264, 843, 844, 845, 265, 846, 88, 101, 60, 284, 285, 62, 302, 110, 267, 847, 63, 848, 269, 849, 64, 286, 287, 2, 65, 288, 86, 289, 290, 66, 291, 850, 292, 309, 851, 852, 1, 853, 311, 854, 293, 86, 855, 270, 856, 857, 294, 295, 296, 271, 858, 859, 272, 860, 861, 273, 862, 863, 274, 864, 87, 297, 298, 299, 87, 300, 301, 0, 276, 302, 303, 304, 305, 306, 283, 865, 866, 867, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 1, 868, 320, 321, 322, 323, 324, 325, 869, 326, 870, 871, 328, 872, 873, 327, 329, 874, 330, 332, 333, 335, 334, 336, 875, 337, 67, 89, 338, 339, 340, 341, 342, 876, 344, 346, 347, 348, 343, 345, 877, 356, 358, 360, 367, 349, 352, 370, 371, 372, 374, 376, 378, 878, 381, 353, 90, 91, 92, 93, 94, 95, 99, 100, 101, 102, 106, 110, 111, 112, 879, 277, 0, 880, 354, 881, 377, 882, 113, 883, 357, 884, 885, 886, 278, 280, 379, 359, 281, 361, 312, 362, 380, 887, 888, 363, 364, 365, 369, 282, 889, 375, 366, 382, 384, 387, 114, 386, 388, 68, 389, 890, 383, 391, 393, 394, 395, 891, 396, 892, 893, 894, 286, 398, 400, 401, 402, 895, 896, 897, 403, 898, 404, 405, 69, 399, 117, 406, 407, 408, 409, 410, 118, 119, 899, 411, 900, 901, 287, 412, 413, 902, 414, 416, 422, 424, 2, 903, 904, 905, 906, 425, 426, 427, 428, 89, 430, 907, 431, 432, 433, 434, 435, 436, 90, 437, 439, 440, 314, 441, 442, 317, 443, 908, 445, 417, 418, 909, 910, 419, 911, 912, 444, 913, 446, 11, 914, 915, 447, 449, 120, 121, 124, 454, 91, 916, 917, 918, 92, 288, 291, 919, 920, 455, 420, 921, 922, 3, 923, 924, 925, 926, 93, 927, 126, 928, 929, 930, 448, 931, 4, 932, 933, 450, 934, 935, 94, 6, 936, 937, 938, 127, 939, 940, 941, 942, 297, 943, 944, 95, 97, 945, 298, 946, 456, 457, 459, 451, 452, 453, 460, 70, 0, 458, 1, 461, 2, 462, 463, 71, 464, 99, 2, 72, 465, 466, 467, 468, 469, 470, 129, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 3, 299, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 502, 504, 505, 507, 305, 506, 301, 508, 510, 511, 947, 139, 512, 515, 516, 4, 303, 509, 513, 519, 514, 5, 520, 948, 522, 517, 304, 306, 518, 523, 524, 525, 526, 527, 528, 6, 310, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 521, 949, 950, 542, 543, 951, 952, 953, 313, 544, 545, 3, 141, 142, 546, 954, 547, 1, 955, 956, 4, 548, 550, 143, 100, 12, 549, 551, 552, 957, 553, 554, 123, 73, 958, 959, 555, 556, 557, 960, 315, 961, 962, 316, 558, 963, 318, 7, 964, 965, 319, 966, 967, 968, 144, 563, 559, 562, 969, 560, 561, 970, 320, 971, 570, 322, 972, 571, 973, 321, 323, 582, 564, 565, 974, 975, 976, 977, 583, 978, 979, 980, 324, 981, 982, 145, 983, 0, 984, 985, 986, 326, 987, 988, 989, 990, 991, 992, 147, 101, 102, 103, 148, 150, 151, 993, 153, 154, 155, 156, 994, 995, 104, 996, 997, 74, 998, 999, 325, 1000, 590, 566, 567, 568, 569, 572, 573, 327, 1001, 157, 1002, 1003, 124, 75, 1004, 76, 1005, 5, 574, 592, 77, 163, 579, 580, 105, 575, 576, 125, 577, 1006, 1007, 331, 1008, 331, 1009, 1010, 586, 1011, 593, 587, 1012, 594, 1013, 1014, 343, 106, 1015, 107, 595, 596, 597, 599, 600, 602, 601, 1016, 1017, 603, 604, 606, 578, 1018, 607, 1019, 1020, 608, 1021, 609, 1022, 610, 1023, 611, 164, 1024, 1025, 581, 612, 1026, 614, 584, 585, 588, 589, 613, 1027, 1028, 1029, 615, 591, 6, 7, 616, 617, 1030, 618, 619, 333, 1031, 1032, 1033, 350, 620, 1034, 351, 1035, 336, 1036, 621, 598, 1037, 1038, 622, 108, 623, 624, 625, 626, 632, 2, 1039, 1040, 1041, 126, 78, 627, 79, 629, 1042, 359, 634, 1043, 1044, 1045, 1046, 361, 635, 1047, 1048, 1049, 1050, 1051, 1052, 1053, 1054, 637, 638, 1055, 639, 1056, 642, 1057, 636, 362, 1058, 640, 643, 165, 363, 1059, 644, 1060, 1061, 166, 1062, 1, 1063, 1064, 645, 646, 647, 648, 649, 109, 9, 650, 651, 13, 1065, 652, 1066, 1067, 1068, 1069, 364, 1070, 365, 1071, 167, 168, 653, 80, 1072, 1073, 1074, 1075, 1076, 654, 1077, 655, 1078, 656, 368, 657, 369, 658, 1079, 659, 110, 1080, 1081, 10, 660, 661, 662, 670, 1082, 668, 1083, 671, 1084, 673, 663, 373, 664, 111, 1085, 1086, 11, 1087, 674, 666, 379, 1088, 391, 1089, 672, 171, 1090, 1091, 174, 1092, 175, 1093, 393, 1094, 396, 397, 1095, 1096, 81, 675, 1097, 1098, 1099, 0, 1100, 1101, 1102, 1103, 1104, 676, 1105, 1106, 1107, 112, 406, 1108, 1109, 1110, 677, 678, 679, 82, 680, 1111, 681, 682, 1112, 683, 1113, 1114, 684, 1115, 1116, 1117, 1118, 176, 685, 686, 1119, 1120, 687, 688, 1121, 0, 1122, 1123, 1124, 177, 8, 193, 689, 690, 1125, 691, 197, 692, 693, 1126, 694, 1127, 198, 199, 1128, 407, 332, 1129, 695, 1130, 696, 1131, 697, 1132, 1133, 698, 699, 700, 1134, 12, 408, 1135, 701, 200, 1136, 702, 1137, 703, 409, 704, 410, 411, 1138, 412, 705, 1139, 1140, 334, 706, 707, 1141, 1, 1142, 1143, 413, 1144, 1145, 113, 1146, 115, 1147, 414, 1148, 415, 1149, 83, 3, 4, 708, 709, 1150, 127, 84, 425, 1151, 426, 710, 1152, 9, 1153, 204, 711, 712, 1154, 713, 1155, 205, 344, 715, 714, 716, 717, 718, 719, 128, 720, 1156, 427, 721, 117, 1157, 118, 1158, 1159, 1160, 207, 1161, 722, 13, 1162, 723, 724, 726, 1163, 727, 14, 728, 1164, 729, 730, 15, 17, 18, 1165, 731, 732, 733, 1166, 734, 208, 738, 1167, 1168, 735, 736, 1169, 725, 428, 737, 739, 337, 740, 741, 1170, 1171, 1172, 743, 742, 744, 745, 2, 129, 85, 119, 746, 747, 748, 1173, 1174, 749, 1175, 429, 1176, 340, 120, 124, 0, 125, 126, 750, 751, 216, 86, 87, 752, 753, 88, 754, 225, 89, 755, 1177, 1178, 756, 757, 758, 759, 760, 761, 762, 763, 1179, 764, 765, 1180, 766, 1181, 769, 127, 1182, 226, 187, 1183, 1184, 431, 767, 430, 1185, 768, 770, 1186, 129, 1187, 1188, 771, 1189, 19, 130, 432, 1190, 1191, 772, 773, 774, 8, 1192, 1193, 1194, 20, 433, 131, 1195, 775, 776, 1196, 434, 2, 227, 230, 233, 435, 436, 1197, 777, 1198, 1199, 778, 779, 90, 780, 234, 783, 784, 132, 785, 786, 787, 1200, 788, 789, 790, 437, 1201, 1202, 133, 1203, 1204, 1205, 1206, 791, 792, 1207, 793, 439, 1208, 1209, 1210, 235, 794, 795, 796, 345, 797, 236, 1211, 349, 798, 799, 1212, 440, 800, 801, 802, 353, 803, 9, 237, 804, 10, 11, 1213, 805, 806, 1214, 1215, 1216, 441, 1217, 442, 1218, 443, 1219, 1220, 461, 1221, 1222, 134, 1223, 135, 1224, 1225, 1226, 1227, 1228, 350, 238, 807, 1229, 351, 130, 468, 91, 354, 1230, 808, 809, 810, 811, 812, 813, 814, 355, 1231, 367, 815, 817, 1232, 131, 92, 816, 1233, 1234, 1235, 239, 242, 818, 819, 1236, 820, 821, 1237, 822, 823, 824, 1238, 1239, 825, 826, 828, 1240, 829, 830, 469, 10, 827, 11, 12, 1241, 1242, 831, 832, 833, 21, 22, 245, 834, 1243, 246, 1244, 93, 835, 1245, 836, 1246, 1247, 1248, 837, 1249, 838, 839, 1250, 840, 841, 842, 843, 844, 845, 472, 846, 1251, 136, 1252, 847, 13, 1253, 23, 848, 137, 1254, 1255, 1256, 1257, 1258, 473, 849, 14, 1259, 474, 138, 1260, 1261, 1262, 1263, 1264, 475, 853, 1265, 476, 479, 1266, 480, 1267, 1268, 481, 1269, 1270, 1271, 1272, 6, 14, 1273, 1274, 1275, 1276, 247, 1277, 850, 851, 852, 854, 1278, 855, 856, 368, 12, 250, 251, 1279, 857, 858, 13, 860, 370, 1280, 482, 483, 15, 1281, 17, 1282, 253, 1283, 1284, 484, 1285, 1286, 1287, 139, 142, 7, 8, 861, 862, 863, 859, 485, 864, 372, 1288, 1289, 486, 377, 1290, 1291, 380, 865, 14, 871, 254, 868, 1292, 94, 255, 256, 488, 487, 869, 872, 873, 1293, 874, 875, 870, 878, 879, 881, 884, 1294, 1295, 1296, 1297, 15, 885, 1298, 1299, 876, 877, 880, 1300, 1301, 373, 257, 262, 263, 1302, 886, 1303, 188, 1304, 1305, 24, 489, 1306, 1307, 1308, 1309, 492, 494, 882, 490, 1310, 1311, 887, 1312, 1313, 1314, 1315, 495, 497, 888, 491, 1316, 1317, 1318, 264, 190, 1319, 95, 889, 891, 1320, 0, 265, 892, 893, 498, 266, 1321, 894, 895, 896, 1322, 897, 1323, 1324, 898, 899, 900, 902, 903, 381, 1325, 1326, 905, 1327, 906, 1328, 500, 1329, 1330, 1331, 1332, 375, 385, 387, 1333, 96, 501, 502, 389, 904, 907, 908, 909, 910, 913, 911, 1334, 503, 35, 1335, 144, 145, 1336, 1337, 1338, 914, 922, 1339, 1340, 932, 1341, 915, 16, 917, 916, 926, 936, 1342, 918, 504, 1343, 1344, 920, 938, 939, 517, 1345, 1346, 518, 523, 921, 524, 1347, 1348, 146, 1349, 940, 525, 923, 526, 1350, 1351, 147, 1352, 527, 1353, 1354, 1355, 148, 924, 1356, 528, 927, 1357, 925, 393, 928, 529, 941, 943, 930, 931, 933, 530, 1358, 390, 391, 1359, 397, 934, 392, 935, 1360, 150, 151, 153, 1361, 1362, 947, 944, 948, 949, 950, 945, 951, 1363, 1364, 1365, 1366, 937, 1367, 946, 1368, 532, 1369, 1370, 156, 1371, 1372, 25, 1373, 157, 1374, 1375, 26, 194, 952, 1376, 2, 1, 1377, 953, 957, 955, 400, 404, 408, 415, 531, 533, 956, 417, 1378, 1379, 1380, 268, 276, 1381, 958, 959, 1382, 961, 1383, 962, 963, 964, 965, 966, 284, 1384, 1385, 27, 534, 1386, 1387, 28, 535, 1388, 1389, 285, 160, 968, 969, 970, 418, 1390, 419, 971, 286, 287, 288, 536, 537, 289, 290, 291, 973, 1391, 974, 1392, 975, 976, 538, 1393, 1394, 539, 540, 1395, 1396, 541, 978, 16, 979, 546, 547, 549, 551, 1397, 1398, 980, 981, 982, 292, 293, 1399, 552, 1400, 1401, 556, 1402, 294, 420, 1403, 1404, 1405, 983, 984, 1406, 1407, 985 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 6, 1, 21, 2, 22, 8, 23, 24, 25, 2, 2, 7, 26, 0, 27, 28, 29, 30, 31, 32, 8, 33, 34, 0, 35, 29, 36, 37, 38, 39, 40, 3, 6, 9, 41, 14, 42, 43, 44, 31, 45, 46, 47, 48, 49, 18, 50, 38, 51, 20, 1, 32, 52, 8, 53, 31, 54, 55, 34, 56, 41, 57, 58, 59, 60, 40, 61, 62, 0, 63, 64, 65, 2, 66, 3, 67, 68, 42, 47, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 36, 80, 81, 38, 51, 82, 40, 83, 84, 6, 85, 71, 86, 52, 87, 88, 89, 90, 45, 4, 91, 0, 92, 93, 2, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 47, 58, 107, 108, 109, 5, 69, 110, 111, 70, 112, 73, 4, 113, 2, 56, 114, 115, 9, 116, 117, 2, 118, 68, 3, 74, 119, 120, 121, 122, 123, 6, 124, 125, 126, 127, 128, 129, 130, 2, 131, 7, 75, 11, 132, 133, 76, 90, 134, 135, 136, 100, 137, 106, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 96, 152, 2, 108, 81, 153, 154, 155, 156, 1, 157, 3, 158, 159, 0, 160, 161, 162, 163, 164, 6, 4, 165, 166, 0, 167 };

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
            final int compressedBytes = 3289;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXTtvHccVnjtaKismgUaEHkSqpWArN4AKOY2TgtBQtmM5QB" +
                "CCkoAYAREgRaKCVeCSAoaMEJCpWKhgeaFCgDv/BMJQIbhSESOG" +
                "fk32cR/7mJnvzJzde62sLUrk4Zk5c+a8z8zed1f/9uTm13evPF" +
                "47uvvNT17uX9w//LV+/sX318yj77Y/3RDv/njnyc3Tp19cXzs6" +
                "yOFPCvjO8z98f+3s0bsK/sGTW6fjP70o4J+8fPL648O9Ev/s0Q" +
                "/bmgIH41+9A+jjjS/z/5QQI1E8l/M/m7v38q9GCC3XErH69QP8" +
                "8smE80Hjz5Zee1QTn0f/xc8/+HTtaJxeF+LuNzvHNy4uj/eEST" +
                "/LJ9FCr55/FPjN0/HDcvxL+fgfH2404FdLeHq9kM863Dx6U8Hv" +
                "MPULwf++f+v04M//+fCfBy8/f/WX1/cP//rJv/b/W8Cf5fD1dX" +
                "FjI02FWDvSQmZafPSV0s8/OxuZvZPtYn8H1i+EX8hHcjTefFHI" +
                "x+j4xuvL441lyscv1mXOn4ejnD+5/uf8+e1X9/TznYI//95OxI" +
                "9APvnybZfPvTc0+zEwfUh/ID5Tfpn4lfykDfnZqcmPSEX1dfrk" +
                "3yg5/bcWogf958mnrG+2rn2deoZV21+2fA5uf/n+ZZXwzmPC4o" +
                "fh4QPzZ3D/x4tvm/YjmcVnui7/cfHJXgn/bgE/sPmHwdc/MP8g" +
                "f5jrm21KN34ekexTPsLaPOAuk5HHKv9a5B9iTfDjj+n+PnT5/6" +
                "HtO8yv2PpHii+r/GNU5h+t+PKX0/j56GAyj5+r9T2byddpRd+k" +
                "ou/2Uu3DwPGj1DKx7o/a2Sj2hw3v7n9W33+Eb8lPW88o8eWvCI" +
                "7yUwT/toJX+ctOmb/M4aMe4Nz8mTt+CP6kwn8QQh+uvyD/BuxH" +
                "zf9NKnxZzy+4+THCj4CLxvwD588zfLd9XM78Lvw6fFLBZR1uy9" +
                "/r+ReWDz8cjf8+wjUPP2vUR1zx5dkivvDVH0r/9PVBg/87lXws" +
                "/J8HnktBguqXdv3fI9XnUH7fj3w1xs9C6gdceFM/X5X6+WCqn8" +
                "/0BhufO34bLmpwYV+f6HN91qRY0+sP39bjz5Z/HZXza0F5lOPn" +
                "qH5jpT/kSf1gbv66arhNPhr1kf7nF2H08ewjyv/d+Wv1tzt+2g" +
                "vJP531hTo8xj5b6BfN/Nuf/6D8ezqQXNQgk4YqIvyh4a31y7ap" +
                "iBhfhMzPrS/U8W37X4uPxSJ+Lv3j+bYg5JdC/syXXyA4oX/MGn" +
                "/99871lfE/rl/w6h91+KSCN+JrVB/GcI2dCMT3PJLnv+D8KZnQ" +
                "lump6JapXz7n6qpSWR82XfxDNggxrVAg1UYkhXmq4KkRm8Xf49" +
                "JgpdP4IluM0LCWCVxWSb8q6JcV/ecF/Sr/rYsp/bH8C98facf3" +
                "8gfSD+nTTPnR7YWqEPlC9m2hH6pcbRx9bflQ019MMH1t/m62+e" +
                "uSv2p8vD6/AWg4vPKb3anyzP1jv/qN8I11f5Po8cnzpz78ZDj7" +
                "p4HOgv0l6NfI9/NQ+Ty3yadHP+H+zOy3cNhvDfc/ccwrKfxPXd" +
                "mi6Wd/Q/1noH1j65+A9lVWRwja/iGjwVfmHw2wH0T/BO07wuf5" +
                "J4J9D6h2WPgP7QvPf+Vw6dtfe3zniS9MoH5i+UHy3VP8Ut+cWg" +
                "kudeE7/ENofB8qn4ZqfyLzj0D5C5b/9vioPwrkm3D+N+H0V7n9" +
                "SbT/W+oHmZup5La6LEe/yb83ZiQylaVikonMBMTnipAsTtmmA+" +
                "qvUfuz0H9uf5Em6KlnudqvhYC/Q5/PQv3JOtxWv1gWfa7+Zck/" +
                "kk13yCcz/+T2N7v267Ej/3SIGpIf0D9E9Dnr16o7fqO+SBw/ov" +
                "/YPT/pw4f7+8Brv3B9XPvTvyD97r+/F4rf7v9F4IswfBS/MKtc" +
                "cP9Vk+xWfhlMf6D9gP1LwfMPXP+C6SPG1yqk+qjIcH5/MXD/Ok" +
                "Grvz4B7ZPbeCgavnbbr4QfXyB8A+M/7dXTwPO/lP6bCOzvCV//" +
                "0dKftNX+ZnBpwV8pvIf+rJf/EfsXlp/CumVMfSWkfsfUH6t+pO" +
                "TxGf3N89b5RypctPq/qP/qjV8t+Age1B9G/V0LXNTh/P41rz9c" +
                "lw9lzU8E6D/y+mvo/EhR/ypG2Mpn7NS/Ls3PL14SH7ryB2Z/j1" +
                "ifI7lqa3yhQfxBxQ8KdkLqo/7+H+SvdpheQ7S/Fzz+jCvEXH5M" +
                "YnK6jVRpUV4a5cv4KWHD2/0jEwuXtRXUU5wL5v4y+SPeNvGntY" +
                "q0XZ/LN99Wn9tSbyp4OoWLCi7acGGHC/Gw9NAld9LC0JmZf8wW" +
                "gayc83dhf4j2CY1f8Cfx1ScB/YA+xB9EP7LviL4O3ITtTw4v+X" +
                "M7X2MM/QT5mO9PE26I8qO9+0vmD5Rf//qH2X+i/Hnm7/JHdPUr" +
                "oejXOqBPRuk3si/k/o2K6y9GnG+4F9Lf4c6P4ivon0+QfWTGFy" +
                "kidBesfzeMP6Fw1D/k+k94PgeNT50/Do7kg9u/K06kqdn8s/NZ" +
                "op6/U+PD2Pw2cHwD9tcE7j8z/sb9E3j/ApwP4NkX/vqZ/GGOj+" +
                "tDA9MHz5/pgfkPlgPz+91pDU1KW34w/P7D/Lv4v6hENeU/MVWo" +
                "0Zt/i90fsD6Un9LOtyRk+TNW+hIy/Xb/nbjyxyl/EtBfScj8J+" +
                "Xfqpa/gfMlcP06jL5Q/RpYf9D9Ha5/pp9f6TM+7q++tfL4xHr+" +
                "LsQ/t/p/ZPmbkOoL/PpvtRHZwj6o6scTP30yWyAbkRZIZ4v+hV" +
                "6e/+f6h57jG67+9KxfOKwkjz8K68gR+Zf7VyNGk18dP81RvjQb" +
                "ZvRioibp78ZZYi6tIH4N3p96LeJ9jM8p8mP5fZ0sKT+8KNiaZK" +
                "VzqNOTBcJlHHyev9n7W3h+LRr21QF3xl/d+oUK4w/i79sm/jw+" +
                "pK4f5q9e/wT9f24f8hkmWyK3D+JKlo92ZvLwUI998wfn9+h+x5" +
                "Lhnf2n45PqI8uLn2jnI4ezb/H3U2Lih4xRv/HmZ0uoz9jnx/E5" +
                "qN+eiOPME9/mz7Xyr6TYojw/yQqKdtTIqJNNGv2l/Wqer2vwD8" +
                "BdMXs/9S20P2njemVmeekNNT6RzvoLK/7l6gfff8TAJdl+Weuv" +
                "9f4/z34GhOVyKfVdw4Qvuf5GyH9Z/osfnwbWT7j15Z79e86/Y8" +
                "75hh77E5IJ7zU/XnhzZnyE+zve+g/tfjEDjpSY2/9ixofc96ej" +
                "+dH9Nsw/rnwMe35z6P2JuB/WUFGIT+b/7Px0e7oHzP27sw/e/7" +
                "bPul/2o9Ivy/0l7D/Q+yEJ70cK4I/h6J9tfcoff5Hr58JeP4ej" +
                "+OnH76+0vH9ner4jIeCj94+x83/2+zn962Pbz17rI6/i/H+4/G" +
                "gSfYng2yemfRz+fiLoH9Hu93Dub/nHZ76/ifD+Etb9LsG8vwXv" +
                "h4H9I9wv+6iqD3WYdVJpjZZTlvvqPz74oPUd7vk7LN/Dxqfc+2" +
                "Xs9/tJBPbTR7+/6zn/krjhW+q8PL+Sier8SjY/v5K1zkfH3V9m" +
                "n+8g1//8/afo93dx50fKR3v/nnN8eH48JQqiq/4qAvED4884/7" +
                "d48OcvUse3859dPyLHdymofxPhkfqB5S9Sfon1dXd/eGD9Ius/" +
                "lz+oVufs3/dtf9r7771/g9fPiz/i8rMQ+eXivyXqD9L/dWt9tH" +
                "M/qXv/ilV/dd3vSt3jN4QH34/zxxd++Uop9tEw6/uh+bUKst8t" +
                "6Rg54i8vvPa44C58+v29Nv+b68Nw1/0/m/f36oei6Uf2nsFTJn" +
                "z9/xBO1290P9Qjn6H+t6VchPvhpPXFrZ97/5er3/35H5d/O5nm" +
                "l0k5flaOXyy+yC9P8P1T+v3YqPo3ffzY/FdZ8qt6/QbAKf4noe" +
                "RnwpHfwPoasX5EeX+ZLb4D93Ox/B2nPv1rj2/JP2+dPv2ypO/z" +
                "nL77h/9o5YeSk9/h9Rti/hyZn0bl57b3R8Xl/0v//PG2IA1cf+" +
                "ihf847X8Y+v0qtDwzEv3SRa9s+X4Y2vht/5eeH4ec/ufuDa6Kn" +
                "+8XJ6upz7vNZk3n+1b1fq4r7tY4Mymqf3PD5+K36zuz+rnt+Gp" +
                "w2f9d+J6Kf9bHhuH/HXb/wnP8nxh8J6C8SPp8mNn6LgQu6/yTU" +
                "TwsXkemF/hSLzcr7i1p07kdH16ec59uBfVGWZVPOp/cFV363j+" +
                "6nw/jKHx/izxdgyj/5fn7I/XdB9w8r7u8Q+oPM+iGPvqHjW3Q+" +
                "h/B+aKT//vdH0/sL8efPYvZPZq3Bpbf+2oUbWv21Vd+4Us6vLP" +
                "bRNb8G2ud6v3ZjYMv9yAl1fxz3Qye08XH9AY3v6G+Fzl/P7xv8" +
                "B/xRjvuhYfOj+rcgnPSIrB8B+pF8aRH0mFB8FD9qi8ArSZ8fw7" +
                "2f79fD+IT61HDxLTP+7eH8wrD3a/4HdfQtww==");
            
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
            final int compressedBytes = 2869;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2PXEUS79e8XZ6XO9FeYbAuak7oNIED7hIgsHjrw2CCE9" +
                "YaS0eAREjg6OTQSL2rDWwiBwQORw6QyPwnDIhg5cgJEtq/5t7H" +
                "7My8N939q+7qnlmMRwjbW9tf1dXVVb/qqhGV8H8qcSxKKbTof7" +
                "Ey4mr750RMO6IA7c+++Mfdtx99+/lbO0f3nr3+9O7s4wf/Ojj5" +
                "z2+XH985u/7JvqelARMj0vH6bH9d/AvOP6D/Pza9srMdtV//qJ" +
                "eTP5noi4+0szB3+23PH9ONXT9pkn7aAL1buF6en/ni9Z9Cfrn6" +
                "8+yL95r2k6ppf+3Za037Dx7s1yef/3bZHJ5er/cX/B3K10WaP/" +
                "P+sJ6fDdDrJf/feTT57w/t/P/99O6vHzw47Pj/+M7vLf8R3XJ/" +
                "KHC/hNGxfvGfv0T6Va+117T7D/0KXP+sHbh0jQ/3R7yYt9+1z5" +
                "+sf/fs9iHgL55fO7sU5xvZL5noZPsHtZdU+QidP/H+jO6feP9F" +
                "2pfb518i+XCdD67/US9OUNz95G8P6SsnOO78ovmD++nNZn0/Xb" +
                "v0ZWs/vP7063Z9vf1w5/lm72/otsoo+5l5fyXwbxE+4KXL6oZQ" +
                "ShRCSdldQU9uN4pBmfZe22l2XVayp7fra+lXh3TC+H79Zr1fVP" +
                "j2xO+Pf/6hsMeif5ORjsSC0f+FWN+fiB4ufyrIfjh3JNz2bdj4" +
                "4+NU9SaGrpfty8660OdjX+66KYv2p3X741ocqMKoh1cT4IfJ74" +
                "eLfn5GP25WuKOWe9Io6y9V83/TcnknGh8KwZfUsL3pf1al0s/s" +
                "/nntSf6Rd/wa4iv+/pntsX/Bwu/yxBcUuVkefEdF0O0HiC4/aP" +
                "Fx8wse36Sd/9mbLvzwzqkbX1AZ9sdp//Hwa1GUNHzB9eHqB8L6" +
                "rfw1Sfgz++t7n+wcNfsrxLVnB8dXZruTQ2Gqm6pdGEF/ZdeP0P" +
                "9kzg/0T/B/IX/Lo8nVjr9Fx9/9EP6y8WOE/4kt82+t/zJo/L/t" +
                "ySv71a1C7Bw1/m1jn350//2Dk4PHhTn8/nqJ6Uh+mvvp67d/uj" +
                "eY/0HP3zP7/BO358tnOdJiSgbBJyHy+WMnfzfm8vcdRX+g+YHz" +
                "Bccn279zBjjdlrj76Zf+/Fv1a2HT/xT/o7DtCxFfMUx6OPwmk/" +
                "ZvUtyvgnC/ptn/2er+W/S/TX7j/YcfM/g3fv2F6Zn9owU+sbaY" +
                "h6unJdzTNx7+IP4RQpTD4eUy3FmqkWgW45Hm1p8Z/FRFxgfA/L" +
                "B/COUnk3+ZE39ljL8R/DVgfhAfDJTfUPzSSafjk75POvxMBOj3" +
                "EPnObd9z9VMMPQS/yew/RuHnQfLLmv/enmj8j6aTnaNayMb/+O" +
                "d9VZ/cbP2Ph9dtshZsf6bBb/WafOtk+E3dXcJ6vclrFP8T9W/y" +
                "4ktB9pNe/10Yv/XHZ2H8Nvv5j8WnqPvD3b9ttwd0fW6EalOadt" +
                "+kqJSuOqvujVX8IfL9zUVffzXjrQ/xZ9I3LMSk56+RasBfHD/1" +
                "9h+Bn4edL3i/vEii3130d9Xvsjm/5d8b5VJ82PzbmEZUWwZOdS" +
                "OyhPZPuvZa7Mq2vR63B+sjjV9S7F/p6P902L/o+xfz/rdNR/YH" +
                "bC9udfxpTd+2i6LjS2f/64X8DehiQMf9E+zjMj7+QOefzLU/3f" +
                "xd/QtxCvjr53/u+a/vb9j4Sc5nsP4Rr+h/FHqFmiL7XSH/i/h+" +
                "KTa/APhHFHzCEz9j2+eVfyH0/BDX53YY3aTGx9H4VP2BiKH2ie" +
                "70F5+/uf1LHn5PwkfKnP7jDMwf0c/x73nQS48BTuL8VCw+H2O/" +
                "B7zPyLL/dDr3fUru/IG49wlqY3QufsvFd+s2vvizrpojfE0X5s" +
                "pst2ob3ex+Iwk+TI0/ZHz/z3n/lJheRNhPlQ1fKMcrlK74Tftf" +
                "uZa/WJre1F/Q91boakmn4asl4K+T3uL/7zz69qtOfj9r5PfjB/" +
                "+zxPc8/Wc+vxh/89+PCF9Kk99fgvOXm+63/3ztcX5Qlx+74IPZ" +
                "yvq2x5/x+sPpQ3Gan3+daX3B/VPxA9Xb3+H4gS0+FuC/VWj/uP" +
                "3b8Ok1/RTNP9jeg5CS+l+Pz6q09pXff174Z6rHl3r/TCz8M2w/" +
                "oPHV+R/2930w/gv4A/UfGN/qfwXYv6P6AZe68RUhf3M6x18J57" +
                "Mc2z8D+4T3PhPjQ7z3JS66XGm/qu/m+q/q2g+22ys/Ym18mvzD" +
                "9tb6EOv7GzY+ff8I+xO5f9OBfbBef2OaZv/h+95U67PX74h7n7" +
                "n8PO/zb2656vc8X+bn3LPRMb5l1Z8yQP8w8SeSfeWm537/cxHe" +
                "V2TFh8IM3YLkzrj7R+2L5O256w9sb4JZwhx/2/K1AfzHSzcXcH" +
                "2v6Buj61YCiuml42+av3xllChOpmpafToRpUnyvjAWn3y1Pxfj" +
                "fguh6wj5QPKV2X6oqBeRS//669/x6R7ebuR8zvz1+cj9u+YP1o" +
                "/f9w3nt8CfdEb5C+Gv2TKdOT/dcWBaiG9adKRlqjFKV/WEKn8J" +
                "5v9Sv4/eNj1WPw7wzzKb/s5dfzn7/UP1vxjvzzznA7+vouhvy3" +
                "rq8pz/rPp7mD+1Bf8d8b8M9d+C3XZcX9IffyLHpyz4Ylb5rAYs" +
                "16u/bkiMyl2fnUkPfl9IxsemS/2S5H2n830lU38lwGeFD9+vuf" +
                "1bztfq+whufRbBfJ8H5C/z+WTvPxk/jnw/au1f0fvPcj+LgPuB" +
                "tz+E+mvE+Dfavz03fz33P6pPxX5/mL2+kf98k/RD7Zufoz7b43" +
                "n8B8wP1kdi1l+C9UGY9b0I9X+Y9Y8C44duekB9oYT2Ncy/zzw+" +
                "c344furnP78+DcV+1Mu/lqOLG9uPpVjWf5Hj+i9x9dFfqvrLif" +
                "gTVx+XUL8G1R9C8hNXvxm+34j0T0WsfxqLP6fyn1xeH88+Kocs" +
                "q1eohmJicvOLpJB/Uedz6P2/9/ux687/y12fKXd+CMZHE/TPaR" +
                "9k32ss64Zov5lDUn1fLv9y01H+Hd6fvPLJp1P9X1f9ixcIP4l7" +
                "37jQ/y+s86fiMzD/G+b3x7afilF9ALmav+56nxqFDxHwBTm4P1" +
                "UEf/Yc60P59bVFJEPyG7l0Kv6RK395aFMXwH5M834sAN9k47dc" +
                "/ePP32bzv7FPK+/5YOZ3h/a/jm9LDr6Wzj5x6n/e9zeE+k+Gsh" +
                "cX3f8T5PqPCfw3kF+ODDkq/h7Xnmvfc+1fbH9x7TPq/O386fKf" +
                "jwb5z81C1SL/2U5f5kcT8O9jL36N7bNjXv5K7YgvjW/IuPxdnD" +
                "/sb8/N/6XXB871/Tw5/IOg+NYxrf6M27+wfn9Xl99UE/ITMT7m" +
                "7R8pYji/tfbKbwG65F84/E+U33Z7KJ+B9h0d36c+4IiSv/j8T5" +
                "Bfyv5+hVH8pVyJv9Qp7jdm/k+c/gmMf5DqV4Tiu+T7x5v/iPaX" +
                "lL/GyA8d868ItZ+d4/vw6xD8mWaf4PxFT/ybpl9c/k8SfMMjwP" +
                "78zAV9F9Cd7Zn5qcz8UeTf4fpyjvfbU7p8seTTfv8E+J+8ANom" +
                "+MOy7zZgHxLyu/n+vyTcr+X55Grq/BLcvwHxkRh8huqfheAnAf" +
                "57AP5QWQgk+6Jk8Lez7/XyVz3vD6o4/rLe3/HxBWd87JQWH7PF" +
                "50TC+cWcnxz544gRub4fF+Qv4fiUtz2aH/7+yrzyifJHkvgXrP" +
                "h4KvlF97M9viirG338Q0nZbfGT9fiH9hkdKH7y0K+f4uIvIfEh" +
                "iP/IOUtk1PnG399k7Z/+vpCnf+K+3yskP4TpfzPtkzzfz06Pn6" +
                "L44/bfv+T9/viA95kyEn9l1n/xD8/Ft/nxSV58KF19ssj6POz7" +
                "G/EPfP93lP+3Mj/0vv3/8xk61g==");
            
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
            final int rows = 790;
            final int cols = 8;
            final int compressedBytes = 1919;
            final int uncompressedBytes = 25281;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXDtvHUUUnl3mmiESYmLFYFGtkYVcuEA0gcLKGhJBCoTlYA" +
                "n+QQoqRGmkseUiSeUiBaWVgto/wUIpUrqJFPnXsC/w3b078505" +
                "Z9a+4hZJnON5nTnP75y5SlWfUvk/JkRU6tM7+dqq+T5TsyOlVo" +
                "pSff37F7snu6eZ23+xo9Hk9fxKN392n+oHm3f/LgnrV7/e/9Et" +
                "zE8dn/PGh/f394ebD/XRlrmn1Pb57vHaxcrWvnLmUbVqmZWrCc" +
                "b7Pi7IJsTG8PksnQ74V913NpzJtrt34ZGOxP/x9e3ieMQf33ij" +
                "oj6ZipWvvniO3E/ebSln3o9TOldFtxHj1Hr991b1P+3mxuc35P" +
                "Hh/VcSPJvTwFzZn+qVXL3vmffarQqLx/LQdf/KyznuO8r9XP34" +
                "+cHHz58+vjc7+vX8/VcHFw8Ov9w9+eHt3dMnVzsPsf0Y578lm4" +
                "2rjzar9Sv7MzvaPn+vWv/+4Wp58vjtXbf/bmfM/sTaT0w/Hpev" +
                "M5J8ofEb9s+8uiRdqJU8+0qpwrlMFbYwqiiqH7ACeuU/tD8bcT" +
                "+XYTrLftgYA9MIcWB90fk37JuG/5+pFVXzf0O1/FfqjMZ/Kf2/" +
                "/d+ZZP8b9l2f7gbnA/dH508OHHves5/Gox8fNOe31+dn6Z/1r7" +
                "/oP4Ago/hjL2x/KPKvI+yjl8jT79q+f/L86S8va/v+3auD1w8O" +
                "f+vZd+Rfsf/uzseM38TjPfwo9SAkytrJCpKD9ZNd4vhVGP9h/4" +
                "3kY7OSj62fG/n4tpKP+4f7jf89fdL537D/v2jzh/WXdf6QHa+9" +
                "Xtla/Td/UOP5g+WwHdOJAbbzjLP9/MxE6y+NnpEjeTdYHx1Eur" +
                "8087PO15wwH5V/JJ8tPmAC+ABp/xrI0hLT9/r8c7HxW+nhP9m/" +
                "Av9A9c9ofwrsb1x/a/s08+AbffuU46Cu7Bv0bBw/srT4PeQ/Y/" +
                "w/xCdw/qyH+uuPX7Kg//ir0c9vOv38o+Fv+H7Gxvf8z2j8bL37" +
                "5+cvvgAX+Qdwf+L4iuB/gvQG7qzzz0F8fhb0BYYaH+DzCfmP9A" +
                "PcD7ZPiD9BujR+oqjw4v3O528dvmDb/K3FF9QIvpAzFy9B/InO" +
                "L4s/6fMrj/3ixN8I/7V08U2dH3DzD8tbn4cP2oTn5+Dvffn04I" +
                "tvUuCLkD8YfxTie5ej9kl1iS62vxf98WowHvM/jN9gOsP/oc+E" +
                "+LsbKqfQPpDxL+76ifXbTcX/cfmm129Q/oLUmolvwvkBfWJ8Te" +
                "pfSfGNlsQHyL7I4hOIT6H+AHF/AUVX/PmRdDyq78P6fzNRcT2n" +
                "HgSm0vpbO2PeLVGdQRPw7wj7I1wf9w+UUfDjiCENn5/VHxKBL1" +
                "+P59X3061PjX/tMvnPmP4TNVqf5wDhdHw0gX4S9S+GHoFPs/o/" +
                "cH2bbl8vgf8Pr++rz5rr+qwcHwquz6sfm0T1Yzg/ub9E+esLei" +
                "z/d8TxVprfMPQnsj5Dqx8LFXQy/xqcn16/5OEzsfhRtP2Jxz/j" +
                "6vte+aPav4GGuFh8vOzGA/tm2/6mRf22RPlWIH8L1j80Gz/y8s" +
                "/dTH1joN8ZMX+N94/M/ispvs6mA3wob8dL81Py+dnx07T1Cyif" +
                "E/OfcL5g/eRiPv/Mmvxz0D8iy/9J+Iogv8f1ZTp+bUYIGF8ug/" +
                "k52b9aX3z3rOtf1W19SbX1JdXUl54R8wv+/nj1pZT5Mwffxfi8" +
                "SYbPg/kXI14bHV+j+DCAz+cmV9bW01b0eovre9XGrKvPNdM9/C" +
                "SMP3jpaP/h8RBfFr7PwXSpf5GOB/WVKP+Tvn9Qjt8CfBXSIyCt" +
                "SexLInyN+/5Iik8K9Yegn6L+E1xfmtb+3Fz+jfBFLnwwf38F8X" +
                "7ziPwP4HcInwPxLcKnSO8jNNDPAF26Ph9fPKPZb/x+Q4jfWeb9" +
                "xeKj3PqDDP9n4Fs2yj9H6m8WiW9CfAfwH/fPJrtfXv/gLeMXOH" +
                "8uJ+0PJuAHyBdF9EcuIz5xu/ylxL9T9q/z8HGVzH7G2Ecjjk9S" +
                "xd8p6xsofxfm/7C/sAzKN8rfyfk9n/+y7x8A+ovif3F9Hcc3Kc" +
                "brYHyj+fl3gvf1FPQh1P8jo4vzIyK+Y7nnK+XvZ29Ef5jxc1T+" +
                "X4DfLRZ0Wfz9EYAO55fGL8A+wfxTGL/y56ftX5z/UeYX5N/TvG" +
                "+Yozb2tTDVFraLzFXxm6knfdTMMG5f49//i96HyfIjXB9f7vh7" +
                "+vo+el/2fz8/8J/C+mU0vuIo/rvfPxR+P4TiAxC/J7avafpbY+" +
                "wzok9d/0DyNXH8GRtfRt4PuT7h828Tx0eQnqL/WPT9G6h+IPWf" +
                "KH6btv83Uf+EDsTvFPxV++MrWXxw6/5LiD8v+/4gPp8Cv9M002" +
                "g8/KO932X230j7Q6bGLyC+QMYnpul/ue33Jaz+vqj3IST9yns6" +
                "E/9+xTc+XX06Zf4YVT9a7vrdxOvzvp8tJv6S1g8g/ieNf2n2LQ" +
                "q/T2g/xP17CtfnBXQx/if/fmbK/YTfXywzvnQT6wf4B/urpd/P" +
                "MTEd5Zc4/6Tpv9e/UugC/bv9/EU2P8E+Bu2z9H7l94/yjwT8Da" +
                "3/D+nv4mU=");
            
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
            final int compressedBytes = 5110;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWw2UXVV1fkmIiWFCS34UpIRIjdaIEpEUSlFO3pvBMdAQii" +
                "IsyI+IYhImCasYAiR07j33vpd5M7ERS8i4qCsZMnS1VlFERUVA" +
                "q1AWGhaiQDLB1RiZodZkJCEg8pPuffbZd+9z7hvtnHXuOXvvb3" +
                "/7O3fu3Hfenfc6RjtG7b93jGJLVjevzi5M7iYL/Lu6j+sY3bSh" +
                "UmFPx6jMk691jPbkZkLHaPMC8ufj0Z8u6/09HHvJR3h7p13pOf" +
                "8tnVip1M8UpryOx+7FaSfh6SesqG3CLBjtfiL0hXFgnMW5csQx" +
                "PShozZ39pzserVQaD1cq9ht+TeNC1rCRz95l/5WZTKfptIfxCO" +
                "PL0B+wr5MF810JeLLP233sAauNZ8m3TWezbo4xnfVbyJOPh5wf" +
                "ms6+pYCbauD82O/ag47pvuwLnvPBZls2wV6rGG/14yv2JXs/5L" +
                "xq/5BNlrj9MfTHxAbrEJzP3yaHtC+rqPgPKt6yb0D/jpsdtQ+5" +
                "8RG7SiGPQP8ezRsunh01nZs+ZP8rOwsiB2BN44oKk3Q9n/+i/R" +
                "EcH7aP2tdMZ3oG+FKTQvWUR/sAW2inW0yaP44ejZHW+yYz3qR9" +
                "S8gP5zOF8+ki9Wcd23cJb+/rmUN++yD+TtuPEw44n54ZzuX9NO" +
                "IPx+F8pnA+VVWM9cxODsfai4wfsO00fIf0w/nE2CNYO1jx92i+" +
                "6Uyn5ihw/wbO5wK/pnG6arx+YP0RdDifxTkaMAPZs3iEcQT6z7" +
                "LnyYL5b+xJZqD+h+wwzHeRD3IGpGVPPAR29nPo/wP9d9BfoEj1" +
                "muynYO0mfPaT5hd8xi8qlWxPfb5w1Bs+sjf7ZfYrGP87+9/sye" +
                "zposZvoR8Kqj5lBno+krzk5vu97xkVH4L+a1brfjdwzIZd7GD1" +
                "GlkFjtnjemWckbXznFklLo1+89mBbLTArjKrYFzFo6u+iu3kiF" +
                "nVM048gtVou197qGerKEr4SqX5z4TJp6KvfqswNqZxtv4JK2qb" +
                "kD2XJK+GvjAe54qSbGVrbo4TNlscVw416Er5m/NjC+xUAyvEo5" +
                "larGUqNbvLfgrGQfEIlmZk8YwtHKufjhCDlJVPda9HX1ZMg1K9" +
                "rEGziAfO5wDnxbqYSXLliCMrK3EPBudgMK4cqtI+fz4J22f68v" +
                "fiEcb3QH9bfipZMH+HrZm+nimA68OOjUe20M7/gvz5X0GfS5Hq" +
                "1ZRF+PyE3q2e8+RKJX/7grOEo/Fe5spP45z8JKmjq4un56n0Qz" +
                "i3Vc87W+L5X3KGaADOWS72zurV4WryE/XKOCO7Oq4s8XD9wDwn" +
                "f1eBXWPWwLiGR1d9Ddv2YrOmZ4V4BKvROt8uprExiaIUq1R672" +
                "GM+3s/RRgb9+pIcX2sCeuITcjqL9Ja6Avjca4oqc9qzc1xwmZp" +
                "XDnUoCuxYuzJ9ckx2SOyR8Of5LPJccnvk+nw+r40mdl8FTxvgf" +
                "0mXDPJDXCOu2E+B+brk3fZW+w/gmd/MhdsuLqS93mGeY0q8yXz" +
                "k7PhfH5Dn632J13k/OTDSWfjQPKR5ALyp8uTG2X/mSxJluYfBN" +
                "wnHPqTyWeSFcm4ZDxazc+kl3jU1OTPKSOZAf1EmwB2He4/k1Pg" +
                "vDzsct+taydnePzfeIXnJucl1XS7Vpg15Hw4zCLWlQ8U+q7U5y" +
                "2Bv8jkU2aFwetvBY/uTK9g29bNiuZj4hGsRof51DuGKEoxYfVr" +
                "ekIYm9N1pLg+V4R1xCZkc3v6idAXxuNcUaJra+50u15Vdn9cOd" +
                "SgK7Fi7O1z2+HawiONbrVz2bab2uf2vo/8gpFG6Lhja9yt2YTV" +
                "/72fKhzN03SER62iXLV9bvXn6XWx9nKGaBAl9bfHK6Z5ul1nZD" +
                "+MKwtjuH7Ri93kJofzmvPoznTOtv0Pk/fOI79gpBE67tg2Pa3Z" +
                "hNXX7hGO5tk6wqNWUa5q8uqT6Q2x9nKGaBAlWDtcMc3h+lQZ2a" +
                "NxZWEM1y96XWyZWQbjMh5dZBnb9h6zrPeT4hGsRof51HsuoyjF" +
                "hNVfn/cIY3OjjhTqloV1xCZk7450Y+gL43GuKLFfb83NccJmL8" +
                "WVQw26Eit2iG1mG4zbeHSRbWx3PGu2NfeRXzDSCB13bM2Zmk1Y" +
                "/V39WeFofktHeNQqylXNtt670lti7eUM0SBKsHa4Ys3A2Oy1uL" +
                "LEw/WLXhfrMl32MB5hfNl0we/vdbLA3lWdbrp6U7uPPaYra+MZ" +
                "ZHc5nq76LdpDPZtKUXuQIujjWTZhwdnCuGklR+xLLoOeh3SFdc" +
                "QGXYfg+nwo7dO+rKIzGG/f4Fx7lEbYaU1XTEeEm+NeI2TaA7py" +
                "NknX8/kvMt6+ZrrSM0wX7pdgnwJ7kO5Hi1d+2C/VT8L9UvX4ZG" +
                "bvl3G/BP2k/KMJ7FmT2dDnOJzbL+WXxvulZHMyz42fk/0S7Hov" +
                "kd9l/eH8It4vdeyX/RKo+Knao8B+KVlO+6X8Mr1fSnp7n0q/lH" +
                "/Mofx+CWYzoLv9UvdPoLv9Ur5c75fyv3drgv1SfgV4Yb+UL+b9" +
                "EsyX5MVdJ5+Xwvvs/OOe+SLaL+UXu+Pl+VLgf5z2S4Va2i/1m3" +
                "64Pvvd+LLpd9dnv7d32XtNf3onXJ/gJx9cn/3S3JXQD9eni3N3" +
                "uKlkwfXpcOmgZGQT6qcJRzrAEbw+oaq7PpknZPW6Dpn+6s/SnR" +
                "x19SpaFfvh+vQa3fXpYvX3SBZcn8UcXo+KXNB7F0QP6MpwffaX" +
                "lLzIZxOuz364PvtNh+kATAePrnoH2x3PmY7er4hHsBod5uvObM" +
                "Lq75/PCWPz+zpS3I06wjpiMzK9L/SF8ThXlNQXtebmOGFRYVg5" +
                "1KArsWLs7gn5P8A1vCaZrJ+N077ffpOfQScnsC+ZnZzKT7UlQz" +
                "rbyQc0W/jTvkmedjcfoOfzkun+ei5NLnfa1uqn6/JUHM7njwE1" +
                "JVnJnuR4UPnWQu/J9Hw+ZKXaHn+WqC1rzM9NFqq/5YsKXf6ulV" +
                "yWXOt5lulzYHaYHXBed/DozvQOarVptWns0RhpEtNdR8gWVqqN" +
                "vNyah3WER6mjWbUnfTrWXs4QDaKE1lTmDjPyWlxZ4uH6Ra+LrT" +
                "PrYFzHo4usY9t+mz0aI01iuusI2cLqX4/2C0fzDR3hUasoV8We" +
                "7o61lzNEgyix34pXrBkYm3fFlSUerl/0uljVwCsbHml0kSo1u6" +
                "v9S6FHsBpt92uP7pwrHP76vFMYez+tI4W6alhHbEamo6EvjMe5" +
                "ogTX1Iqb44TNb4grhxp0JVbsELeZ22C8jUcXuY3t9n9hj8ZIk5" +
                "juOkK2sFLtxmTh6L1GR3jUKspVsduJsfZyhmgQJbSmMneYkW+O" +
                "K0s8XL/odbGGacDY4NFFGtRqx9aOZY/GSJOY7jpCtrD66/NY4e" +
                "i9UUd4lDqaVXvsm2Lt5QzRIEpoTWXuMCPfEVeWeLh+0etiMwzs" +
                "gvFIo4vMYLv9DvZoTIxu3Ys6MzQr+Ra8wCxmRu+vdCTMkzqCZ6" +
                "R9c6w9jMca2UNrKnOHGfU/iytLPK7E9bDrzwLEnyUAzSfK5wHi" +
                "zw3ozwq02i+Fu6+wRqMuTH0f1/ulMG/szzOAtreF2uP4WJ9nyF" +
                "e0/jxDuLPLV8aVw/NQ3mP5/dIkaHvcEUZQ+cvCQj/M4ayLR7Do" +
                "d518braH/Dwv+Xi2p4hC67ui4ONqe3RFVWeS6DR77DtCn84o8H" +
                "tED/mKKpMC5ZN4LK62PWFUGEpKeE2EvdLAu1A80uhiV7Jt54Qe" +
                "wWp0mK875wqH/3s/JIx9n9WRYj1XhnXEZqR9Z+gL43GuKMlHW3" +
                "NznLD57+LKoQZdiRU7xHXmutrpeKQR7tfeQjv2CBb92PEY5lOn" +
                "OecSjmfEy63vRo7UTueojjOj1oAoOyH06QzGsyLK4SphlijTq8" +
                "KIjoYVtBLBO+wzcbOn8wzi7kij9oWNfa1wZXzIpxFSL84qZ8D1" +
                "OW8sBM27ZzFSV23FFFYnbP3eMDrWyhnP2PTu9Gvxu2y5j6dL5S" +
                "8wvVbu2ulXW783dp9fw2fgl6n3vveW3yW3f7HVu3r8/2bI5RmX" +
                "hFX837sRjvgVhl6PQhZeY/sXGWvXtH7ldPO/Het1tvWrUXGfao" +
                "M26I5t+EkQ98mINrbdfFB5BNuGuZLvZoPk57n2cZ6vMFjktknE" +
                "ewfDiqpOm+iMVLWFGZqb9ZCvqNIWKG/T6ouzMBhUHoxUFD7Gu/" +
                "hmsxnGzTy62Ga27a/NZruE/IKRRmh4v7mZMYLTbMLq37+3C0c6" +
                "oCM8ahXlqk7b0lh7OUM0iJJ6LV6x17FdZ9hlcWVhDNcvel1stV" +
                "kN42oeXWQ127Xz+Sg+PSM0xwmtO7MJq39/1IJRa4vjUpWRtfPt" +
                "VdoXZoQr0r6xuTnO/HFlnRdW4nU67E6zE8adPLrITrZrC/Fo94" +
                "UYaYQmlIwSYTZh9edroebQERprC7WKclWsZVfo7JiR65aVhCrj" +
                "lYmCuDJVKK+/uD5dzD2zfW3TW+oTux/tmxY/T86mw2vPnXwfro" +
                "+j58nhXb4+Pr5v63cT9qD/lPVgflT9/+gCeYVKB/T7I/3/I/fu" +
                "aTrx1I+JPwFu18avIqKA/n80xvPkrvhdnNcRfN7GXrdppj0QP0" +
                "/OXxdE9+Mt3x/dZG6qPo9HGisVttAWD5z7mzSWmvu93MTxEIcz" +
                "ZiMcz4hXOHikHIzSTOJisYbq8/afdHbMSH5SRKzkKa9G6ksuoX" +
                "SUK8RK+GwW2NuhDbmjG+1wYaENc0DR8fYA65rTensRD3A4o9wC" +
                "x7MhzSIRynHRoYLnds+jqrrsoY59Ojtm9P4h0Uie8mpU/SLX6x" +
                "4KKg8F9YpV+r92xtag7XVHN4KPLbRjj2DRX3NMcX6NfS66l7DO" +
                "Vyt8ilEiZm8RjeKM4TqIsl8JfTqjwO8tcvfSWF6Nqq9XtTeMhh" +
                "UCJQUe5/bM8n+k5D5k/zr06WN5363285f7e9x86GeU9/P1v2u9" +
                "G271VGqs/Xx2fKvnPqJkzP386pi/5X7+3Nb7+fLTpVCZucPcQU" +
                "ca2Yet1lZrM3fYZ8gvGGmEjruOcAXth1fPNs2hIzxKHc2qPXZ3" +
                "rL2cIRpECdYOV0xz2H+qjMbiuHJ4rrRmUQ6+btNdPYxHGuHO6i" +
                "20cW6HyC8YaY6nm+MhDmfMRjieEa9w8Eg5GKWZxMViDdXDdq/O" +
                "jhnJT4qIlTykSGfxHM5nkUsoHeUKsZLi9YixV5mrqsN4pBEi3k" +
                "I79ggW/djxGOZTpznnEo5nxKt5aKwOczSOM4brIArO51WtdOlq" +
                "rIhyuEqYJfX1qjCio2EFrUTwDrvRbKy+gkcaIeIttMUD2I0aS8" +
                "0p38jxEIczZiMcz4hXOHikHIzSTOJisYbqK9k0nR0zkp8UESt5" +
                "yquR+pJLKB3lCrGS4vpk7HnmvJrBI41wf/EW2rEHLT1zWqN83d" +
                "3nl85jHM9aM6KXYlQ3rCM21kFUNiP06QzGsyLK4SphltTXq8KI" +
                "PaAr6zytRPAOux7aiDu60b5QWGjDHFB0XB9gXXPK1xfxAIczyi" +
                "1wPBvRLBKhHBcdKXjWex5V1WWPZDN1dszo/SOikTzl1aj6Ra7X" +
                "PRJUHgnqFav0r0Ye654DHvbPA192e5zX5fvFtXb00HHs/8dxnM" +
                "b/z//jyoz4flM+/1lr/2P/j8PPf9bas4u1HmQs/z/OvlF8B/go" +
                "K9G17ZGx/h/HKKlOmiIl6vN1/v3mVrO1+hweaYQ7gbfQFg+c+6" +
                "0aS839ZrZyPMThjNkIxzPiFQ4eKQejNJO4WKyh+hy8Hm1trUsy" +
                "SBGxkqe8GqkvuYTSUa4QKynun4xda9bCuJZHV30t27ULQ49gNR" +
                "pR2AitO+cKh//tX1hmDJ7XRXGtgJC1C7NM+7JKGI9zRclY3Bxn" +
                "/riyzgsr8ToddovZUns/HmkEJm+hLR7AbtFYao5nC8dDHM6YjX" +
                "A8yyZoFolQDlalmcTFYg2192e5zo4ZyU+KiJU85dVIfckllI5y" +
                "hVhJcQV4LN4FzG66G5jdsNZj2EIb54DaLfcNicr9k+M0ss//1n" +
                "aX75/MG98/KQejOBv7/olRszt7UN8/RZe+f7IiyqFYvJrW90+M" +
                "6CgzlJUI3tXZYDbU5uORRjjT3kJbPJCxQWOpOaYNHA9xOGM2wv" +
                "FM15AM9BILNvZSXCzWUJuf5zo7ZiQ/KSJW8pRXI/Ull1A6yhVi" +
                "JcX1ydgp0IbdEUa4PicWFvphDijxCBb9rpPPzYbJz/OSj2fDRX" +
                "RKMHK1YV1R1ZkiOs1wfmvo0xkFflj0kK+oMiVQXugorrfhMCoM" +
                "JSW8JsLebG6ujuCRRnil8hba4gHszRpLzTHdzPEQhzNmIxzPiF" +
                "c4eKQcjNJM4mKxhupIfo/OjhnJT4qIlTzl1Uh9ySWUjnKFWEnx" +
                "+s7YiWYijBNp9L+biWzXFpGXPYKlGVmEopHxumsO/9exSFeVCM" +
                "ezyWFFrYAwtUVpLfSFcZ0rR1lTmVuUaoXCm00KVcSaPfZ6c33t" +
                "HDzSCEzeQhvn9c+Rn3xZG0exOZ7rOR7g8PsyBRvheKZrSAZ6iQ" +
                "UbeykuFuusndMYp7NjRvKTImIlD9XWWTxPt0suoXSUK8RKivN/" +
                "juSO/Ty51gnXSlets/XzZPtB//6o03N28vPk7Jvxc9bGKcH7o0" +
                "6JNk4uP03G+B9/nlzrbMzSz5Mpo/XzZL3DoDW1ep6cbg/eH3XG" +
                "z5PpLPyp58n2Rvxsg/sf3mT+fAgjzGByhD6JAdETKpX8o7AfGM" +
                "TvywTfLLmU8hzHi3Q+He4DxFHgLuHqyIjfj/PP6mfLZzKKT48M" +
                "4vdlHOPyjlE8n7n6BA8pBI6PJVOSkULr8YB+a6HkZDyfgFmuuf" +
                "H7cX52RXKWiy8WVv39uMZjyULwFN+Po7z8Yvy+DH4/Dr8vQz76" +
                "vkyxzv8DCKQ/mA==");
            
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
            final int compressedBytes = 3837;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtWm2sXVURBaOhFQGLFtDQBpBPyw+0gIkmut/tu2naJuAv0Q" +
                "Rra9u0KQ9imtRSIW3vfYf7VaS2VIGWti9WNH7EREDEhGJMiGCi" +
                "WERiNCgJCIQEef0j+Edx7z1n3loz+7y2ovdk3zkza2bNmp1zb8" +
                "89r2F1WN16LawOtT3lFPWSn86rsxBBbspMK73bellyrmySp2fC" +
                "yzxiW68p6nHN0T4pq3q/jXGF5qsiqdEutkpzu1M8VUIYtR1YCf" +
                "Jz7tKwNNqlarOWpeq3X7QR5HK2reeltcIkZ+k19t2S8RTzsjj7" +
                "mtlbbGMW97WlEs+tuOT2HvadrQbupIpzxv6wP9r9ajOyX/3qHI" +
                "1wDg7BJl/SWs5jNrDWe7GOORhRyyrKrmn1r/baywpogJLeWj8x" +
                "M2ju4CLfGbidH3ozVoUq2kptRir1W12NcA4OYLwYER+sde/FzG" +
                "GQakZfxaps17T6n/TaywpogBKZqeS2Fb0XfWfgdn7ozdjhcDja" +
                "w2ozclj9aqFGOAcHMF6MiA/W+vN+jDkYUcsqyq5p9T/ltZcV0A" +
                "Al1QI/MTP0+5I7uMR3BqOdH3ozti6si3ad2oysU7/9eljXfh0R" +
                "5HJ2/LznyNjtGgUq+WCVWP+ektF9f66zfeBL5tjtSVuTLttNES" +
                "hJKpu4FZfcwVW+s9XAnVRxztgetke7XW1GtqtfXRi2Vx+ROHJw" +
                "SHbczxwfu43zmA2s9X4eYQ5G1LKKsmvqVV3stZcV0AAlVqVn0N" +
                "yx9/nOwO380JuxLWFLtFvUZmSL+u0XNMI5OIDxYkR8sNb7+Rxz" +
                "MKKWVZRdw5beRNLWrAsV0AAlvZv8xMyguYNrfGfgdn7ozdi2sC" +
                "3abWozsk396lKNcA4OweL1meNjWzmP2cAqvQfvYQ5G1LKKsmvq" +
                "VV3mtZcV0AAlVqVn0NyxM3xn4HZ+6M3YnDAn2jlia2SOHEtaS1" +
                "oS1Qhy5Uy8uJ/kST4v5pAOwmsZtc5qYBZE8jX+aRuzONfiPVnf" +
                "2+rQ/v1zfGerqtRc5y4Py6NdrjYjy9WvrrAR5HK2reelteCor8" +
                "8zSkb379Fy2we+ZnYP2pjFfS2UVJc3cysuuYMDvrPVwJ1Ucc5Y" +
                "G9ZGu1ZtRtbKMfn0+BEbQS5n23peWgsOiSVez+j2c63tA18z+8" +
                "HGLO5rocT35lzoHvzEd7YauJMqzhl7w95o96rNyF45lsxdMlcj" +
                "nIMDGC9GxAdr/XmfyxyMqEUfZuVI/yGvvayABiiRmUpuWzFc6D" +
                "sDt/NDb8Z6oRdtT21GeuqPP64RzsEBjBcj4oO1vj4fZw5G1LKK" +
                "smta/V947WUFNECJzFRy24rhhb4zcDs/9Gbs7nB3tHerzcjd6o" +
                "8/phHOwQGMFyPig7Xez8eYgxG1rKLsmlb/l157WQENUCIzldy2" +
                "Ynip7wzczg+9aY0vGl8UeyxSm6ddpH51pUY4BwewtHq3cB6zgb" +
                "X+9+hc5mBELasou6bVP+q1lxXQACW9m/zEzKC5wxt9Z+B2fuhN" +
                "KxwMB+O+HlSbd/qg+tXHNMI5OIDxYkR8sErv1pPMwYhaVlF2Ta" +
                "v/jNdeVkADlKTedmJm0NzhHt8ZuJ0fejM2CINoB2ozMlC/+rhG" +
                "OAcHMF6MiA/W+nndiDkYUcsqyq5pTZ7jtZcV0AAlvaGfmBk0d3" +
                "jQdwZu54fejI2H8WjH1WZkXP1qcRjv3YUIcjnb1vNSNrDW1+ev" +
                "S0Z3vzRu+8DXzN43bMzivhZKfG/N7d3JUw2nfGergTup4pyxJq" +
                "yJdo3ajKxRv3WljSCXs209L60FR/287oGS0e3nGtsHvmZWV9uY" +
                "xX0tlKSZmrgVl9zhd3xnq4E7qeKcsSwsi3aZ2owsU7/9JxtBLm" +
                "fbel5aC456P+8rGd1+LrN94Gtm0taky3ZTBEq4zquH7uEbvrPV" +
                "wJ1Ucc7YGDZGu1FtRjaqX11rI8jlbFvPS2vBUe/nvpLR7edG2w" +
                "e+Zrb/aWMW97VQUl3TzK245PYe9p2tBu6kinPG1rA12q1qM7JV" +
                "/dZHw9bqZokjB4dk+8WI+GCt75fOB0d/ASNqWUXZNa3+Qq+9rI" +
                "AGKEkz2YnlvDvFFaPzfWcw2vmhN2OjMIp2pDYjI/Xbf9MI5+AA" +
                "xosR8cFaX5/7mYMRtayi7JpWN3jtZQU0QInMVHLbitFS3xm4nR" +
                "96MzYMw2iHajMyVL891Ajn4ADGixHxwVrv5z3MwYhaVlF2TSvu" +
                "57BZFyqgAUraxcTMoLmjG31n4HZ+6M3Y3DA32rlia2Su+lWQqE" +
                "aQK2fi6Zl67HMMPcbu565ASg3Mgkh+XhdszOJci/c802dsb6tD" +
                "+4/2+M5WVam5zu2ETrQdtRnpqN/6kEY02n1TUclQjBcj4oO13s" +
                "8DzMGIWlaBHI503/LaywpogBKZqeS2FaN7fWfgdn7ozdiqsCra" +
                "VWozskr91rk2glzOtvW8tBYc9X7eWzK6f99X2T7wNTNen6uadN" +
                "luikBJmqmJW3HJHT3rO1sN3EkV54yVYWW0K9VmZKX61RIbQS5n" +
                "23peWguO+t/3Vsno9nOl7QN/huNaG7O4r4WSqtXMrbjkjv7iO1" +
                "sN3EkVp9Webk/H7+hpsfWd3bT6rQ+3p9PvTY0gV84iw6nt6cmX" +
                "1FMW+JNvIIYe/V3cNb3v+KzWWQ3MjEi+Q/y3jVk8Mi7UWrzrTN" +
                "xbz7tTvAdjb0ftf7fzWhU8K888M8EjmEXPqnjP3zqqaO/UhHQu" +
                "QGae6104n1wcP4dfqrkpK/l3vA1fOI/3aj+C+u4XY5d3+4zEwV" +
                "npjL0dC5uZW0cn99Qdpj0fXqO/sv7O9WLv+JdG0dnuRjg7nC3v" +
                "YjUmfutWjXCOz25etgPq82f1ZmVhZveJN32QP8PxhNduca9RIz" +
                "JTyW0rRi/4znavSs2CHf/zXq3gz2/T550/2/baZzb/eQ+/bf68" +
                "myv0BJ/3wVNWu8dn+7xXy5s/71CaXjvPsnPZd9uJP+88bcN+Xh" +
                "+/Vw7/P/az+4DZz2egqfvtd7afk195h/t53cl8f+4cL/dzNiW8" +
                "n2FX2BXn26U2T7tL/dYlYVf+/W5ycEi2X4yIH6+mVxGP3mZwDF" +
                "5mRC2rKLtmxle89rICGqAkzWQnlvP4+50qdl7vO4PRzg+9GYuv" +
                "aIPajAT1qy+HMHgTEeRytq3npWxgrWO/AePgLUZm1AXbJwSO5O" +
                "vzeRuzuK+Fkmp1M3fcT5pq502+s9XAnVRxzpgIE9FOqM3IhPqt" +
                "a8PEcA4iyOVsW89L2cBax54G4/A0Rmb2c8L2ga+ZcT8nmnTZbo" +
                "pASZqpiTvuJ0218wnf2WrgTqo4Z6wIK6JdoTYjK9RvXW0jyOVs" +
                "W89La8FRx46WjG4/V9g+8DVz+AEbs7ivhZI0UxO34pK78x++s9" +
                "XAnVRxztgUNkW7SW1GNqnf2hs25e/PTYjxmWTbelmpsu6ziVnr" +
                "e729JaPbz022D3zNrG6xMYv7WijxvTU3Xp801Z1zfGergTup4p" +
                "xxejg92tPF1sjp6rfukahGkCtn4umZeslKpY2hh6K+e6mBWRDJ" +
                "1+cHbcziXIt3zFRyQ6kqsJ2tqlJznTs/zG9NpXexsV/tJT+dy3" +
                "vMnc+5Esk88xVXK+f1zk1JrtZJDCxgTHFF05ntg3zhVo5Sl/SQ" +
                "uChSPVDCVXreneKpZHLubPtBCebM2J4Qf32ld7G5+x71q69phH" +
                "NwAOOVjuo2ZgNrPevvmMMge2aujz2synZNa3ix115WQAOUVFv9" +
                "xMwABb4zcDs/9GZsfVgf7Xq1GVmvftWxEeRytq3npbXgqGPPlY" +
                "zu+3O97QNfM4c32JjFfS2UVDuauRWHbtvZauBONn/m/v7BmV//" +
                "OHso9r8ZaP085MHZnmLc8WN5HlL9lPnKV3gW54OX893Pfvfr6E" +
                "HUp+ch5Wvwis1KZ+zN9jykt1czKvMkozvlFeCXjz4PQRSd7Zxh" +
                "KkzJu1iNid+6KkwNd0ocOTgk2y9GtAPH4/nvmcMgU6wMqmzXtO" +
                "L951SzLlRAA5SkmezEct41e+DnYUY7P/Si1j676zw68/ztuvi5" +
                "+no+u55/vx7vSVu6Phuev9lnY32cD08rcRtpvj4nn/fPCewcsz" +
                "6v6zcparg+jzvlbLsQ9oV98i5WY+JXk2Ff/r1pcnBItl+MaAeO" +
                "x8/cEeZgRC2rKLumFa/Pfc26UAENUNJ7zE8s5/H6pAo/DzPa+a" +
                "E3Y5vD5mg3q83IZvXbf9YI5+AAxisd1eeYDaz1fv6RORhRyyrK" +
                "rmkNv+e1lxXQACW95/zEzKC51Q2+M3A7P/SmdYLndb3//vln8d" +
                "y/6fnnH/7355/Dn73D53U7Tub5p5/npJ/XHQgH4nwH1OZpD6hf" +
                "DTTCOTiA8UpH+3xmA2vd+3zmYEQtqyi7pjV81GsvK6ABSlJvOz" +
                "EzaK5mWR1N80NvxjaEDdFuUJuRDepXQxtBLmfbelnt8wQVDBz1" +
                "fp5XMrrv9Q22D3zNHL5qYxb3tVDie3MuphL9fl5/6Py0D7vD7m" +
                "h3q83IbvWrUdjdul/iyMEh2X6lo72Q2cBa7+dCcLRfYkQtqyi7" +
                "ptW632svK6ABSlJvO7Gcg08V2s5gtPNDr9YOrhhcPrg0ff4Hl+" +
                "X7pZnv2VbcldHl+Abq/CDfz0+f6K+Tnatmu9/o/Ch3/OoJ/745" +
                "8+3U+X4Dyw/1fom/9e3fm2e9X1p4MvdDFqf7+Wmvz/KEeWGevC" +
                "eLmPjVz8O80dWIIFfOUM8e+xxDj8EWMI0WAyk1MAsi9fPkeU26" +
                "bDfmrWd6lCe25+hv9VtW34nzw2SYjHZSbUYm1W9dqBHOwQGMFy" +
                "Pig7Xez1uZgxG1rKLsmtboBq+9rIAGKJGZSm5b4edh3M4PvRk7" +
                "FA5Fe0htRg6p37ogHBrdKXHk4JDsyZe0lvOYDaz1fk4yByNqWU" +
                "XZNa14fR5q1oUKaICSNJOdmBmgwHcGbueH3rTyX0rzE4/OHPvd" +
                "le/VjpT3mJ0LOhfZ/2nS9Pfi9pnH+2XWPnPwrRPdf3a+wHd9g/" +
                "v8XV/1+OCbnfeCvzOvPd05F73k/rPh79dn1vmfyLz7/P8+0KzO" +
                "cv7+9Hjn852Jxr+/H2sfi/YYDnjxN+2T7WOjuxi3uTZmsAWz5X" +
                "efirEF8EeLNVNya3XHmjWpl67P2TLkvPsrzbTs3Bs48urcBRad" +
                "bXLN19xT/gNnQ+Us");
            
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
            final int compressedBytes = 4130;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuMXVUVngISRepQCqWdDqWYiKWBghYo0nY8c09vRZg+bO" +
                "m0UAsIWBBpMaGYICrdM/fOOySooGiCFagJ+EPQqe0ENBolbSr6" +
                "o/GXImCaUBPEpunQKgZx773OOutbe+9770R/eE/2Y33r9e0153" +
                "HvOac1p5rT2trM+8zp5v1txcecaT5k2s3MtrbaL825HpllZpu5" +
                "ptPMs/P55sPmI+YiO/uo1y2wbaFtl9i2yFzqscurs8xiO15h25" +
                "Xmao8tN5npogxWW7XISvMpc60dP22uLzLfWXLoNTeaLeZmc6ud" +
                "f86228zt5i7zBTPNnEIWtV+ZD5gzzAfNdHOWOdtazDDnmPPMHN" +
                "Ph/c/fOc9cYMcLvXSx7y8zH/O5P27nV5kl5hN2XGrbMvNJU7Fj" +
                "blZwfmt1nUV6bFtl1po1ZrWP8Bmz3txgNpiNZpPZbD7rsVsKxn" +
                "fY9vns4ezhtjbX0+g0JLl5pcoI2sgmOmyoIVmiUu7utzEGanhE" +
                "FnFW10aeC7nHHsJBmNCa4tjaI1wP6vX6ha/Xbc222nErj16zle" +
                "XaSxoRW7TW/tjYV2IQVjk9jtimPlqPMllWTh95XmNaH/oKk9pv" +
                "0rFZL7x1Zs0BM6F9tiXbYsctPHrNFpZrv9WI2KK19sfGvhKDsK" +
                "E9ccSgnlt0HpHZcuQPGtP60FeY1A6mY7NeeOvMmgNmQvtsZbbS" +
                "jit59JqVLNfe1ojYonX/YUSwsa/EKOr55zhiUM+VOo/IbDnyR4" +
                "1pfegrTGqT6disF946s+aAmdA+G7Xbbt/70WIsWbm6w6OEjypb" +
                "v/k47Fc2siuk3WQ39Cp47MYo7OHwItruYlbqRQp5pniBx27hSI" +
                "i90uzQXjoC+ep1hXpkUv792bY767ZjN49e181y9U2NiC1aa39q" +
                "1QtISzqJUVw9L4gjBvtnt84jchnjTY1pfegrTMLcaCurIv7hes" +
                "ON1y91qD3X1la/z7Z7s/VWXu+v/P/kVdVOlutbz72ZT3Or/Wlb" +
                "g09tD3vY+XisX/GMst5r20+Ceq4n//qXrO75ZI5/iBVzQ0nmYe" +
                "7aj8mi9rOUnv0wllkTZpD4jfJkG6jZir1bfgt7HbUD01xv67kB" +
                "/QZOaRwPP/X3ZD70WluLT7YhyHJaaDHyJ20l/FP+8uE1NdIT3q" +
                "diFfXcUH+XUYmv4/TZ77PDP7LtmazX6no9tqTUXlPG7OXe1rM3" +
                "xaNvWTm7SjzSn4HXW9azV/sPPxvle4Ks+q4WhuIT+kPu13BFqc" +
                "yhPust6tmL2tgu+Lsdtbp1nmn5K6H2Lys9xVrSm/k0l0//4Ubx" +
                "YPW7URp6Z+ixUvNk2lv7Dz0e1fPpoUfRys1QorUkzhPvIL+h75" +
                "bxdhX1WRfyz9YV9VwnqGQOq2FjXjy0YOii6kT16JD/BdlXHlvV" +
                "uq2W/35VnWBfW8+Jtil8mlkNT4e6pOs50SpL/83ays1Q2jmvAa" +
                "96mh/XM8Wf6omoZI550vHePdC93f4SHKBWnPNO5RlhzsLMF32z" +
                "TzOr1hEsm4EWx3umrTRzWk2z3M056PxST/FOx4H9do/GXKu9Wz" +
                "06cpZH9jBm989iHh9reAxwvPDI9dW52B3vTmPj+37nWn302Bx7" +
                "2NP1Q4+TtUTrf8Ue72i1h1kS353zNL/yeN8p9nS8U9Ro/9yD9Q" +
                "yrRJkV4tefn8hP0PHu7i/R8e4w19v987T8RG2bm/e9xZi7v0Rz" +
                "19OHdYx4u8U8d9LICfWd5YesyU+MTNIonnx/iVHueSuu729bqz" +
                "PM3YyYGfkJcx7bmvNDfuX16C+F/RLJ6bawnu7+ktSz5FV8OzKb" +
                "zD2E9f0Na5CtydYU1/dyn3aY6/O5+dxsjasnyYSZ+TxHD2mkye" +
                "eS5CK4cRS+Vbm4bJetGS1GiaR5EDb8LFkxNxtxmrbSDGJ+kj3M" +
                "RVHDejpLqSdGCPNi9mxtVhxpPLqZm2dr88X5YkJJJkxk9JBGGm" +
                "fnJBdB7Ioj7lSOwplSLCSDRBUPW8+ztZVmEPMrq7Q4zIX5VT0X" +
                "I5eonoFPkXN1trqQV5caj2Wr+3+fLyeUZNdqZ4qMHtJIU5tOko" +
                "sgdiXT5WzHkWIWkkGiioet52xtpRnE/CR3mAvzxyzjCGFezJ5P" +
                "5pPF+XRfGcdjtl9it0l//pys7mPMWRbzSfGQVkRYQvFcBLEr7Z" +
                "dwnnxyZJJGiURsXBaSuM+VpTt/opWbETPmq/lJbrGX9eaT0fVo" +
                "X74EvIIqUWZEipocy4/Z8RiPXnOs2ObY7ZivZ2njENnIOmxkV0" +
                "hzxK6czcEoo6BhBshCRQVkdFrIPfaQFXFEYqRXTPO+Xejh0TkY" +
                "VyLq9Qtf17JV2apif11V7rkec23FtmyV+77EMtqKZ6hnjev7/4" +
                "525fV9G9tlq0a/TGPIQsdha+HW/4q20gxifpI7zEVR4+tR/1vI" +
                "JTreg3VRzvxkXtxDqu4tK+0x11Zsz0/6/fNkdS9jzpLm+UnxkM" +
                "YyxyO70a+qem5nTX5y9Cs0iiflcFlI4p43shh9UFu5GTFjvpqf" +
                "5MZVSOToeN+r9r6gSpQZEVp/1pP1FPXtKSvtsawn78g7sh7/fa" +
                "mHW97BtuKJevZ3dk5yEdw4OgjcOjiK20brNIYsJINEZUsfcUBb" +
                "aQYxP8ke5qKoie9LHcgl2j97NFbU53h+3Hoe59HHOc5yfUZ+3O" +
                "+fykY2sg4bakiWqMVvNIhh90/Q8Igs4qyujT4Yco89hIMwqWwL" +
                "V0xze/4Ej3A9GFGvX/gie/xNZu4vLDrzTlvTmaI1v3BI/Psx/J" +
                "jL806ycvZW/rnK3Elo07spR1tlqZ+T/s3LUqP7IZy7etQ9f29y" +
                "P+RoimWr+0vZzGwm9TQyRnJ9VjZz4GHCxSa0TjedQfz97/frOA" +
                "pGDo4nlUfs2XLg3yF3rQ85MlLZHq6Y5n270APXghlSTNA+a8/a" +
                "7dhOY6FpZ7l+Wdbuz5/tguGMJJ6xhDJikmP4eolkz5/t6Kc5YB" +
                "RB/PVoQGNaj77Su7G2E1csc1tPqIHmr6OGmdDe7Gj2vk31VX7f" +
                "xrYpvW/Dxzsc+4n3bYZ70+/bqHNGi/dt6te6922sJvm+jdWvTL" +
                "9vU7l7Ku/b0P2Q+H0b24L3bUp7/75N6nwlZ6L+++JzmtzNY1Tu" +
                "zzu075bGZ0T2Gr639flR7tX1bUmds8fO0ecwjs8YnT8lCjOv3I" +
                "NcMK7mhPcg4zxw33CHOuqXZkupd6NgJNevJpQRsaUZSf2HUSJ7" +
                "bBijqGcNs4om5oBRBPH1PFdjWo++0rtx4D2dW/PAyujMmlXMWb" +
                "J5qSvrkjk1W097RLjfR2jDPVrH8+ob0fWlS3JU35D52MIGz8W6" +
                "MCJm4fvJEoN5ITP0596Njplwkbj2/NkVrgWbzqNtE+yXZctkTs" +
                "3Wc6mq5zLSiJ6tU/NUBtYPD8t87LJG9hgxjmzruQy1HF976N6N" +
                "9WuQi8S19VwWrgWbzqNtE2d/vB593TZ/1apcp2w6zU66Htn+oh" +
                "JdABaLmmSA61H3k6ZqHmrxHfZG399aSLdF588r6HrkteX1yI4d" +
                "eP5MXI/cdca465HH4AhvdD2S50f6epT8dvty/jL1NDJGcqWXEb" +
                "SRTXTYUMMZEKe4EgM1PCKLOCtzS/MSD+EgTGhNcWyJJ0wwrq4V" +
                "ckb71HVWrpP1ilwFw+tc37caPY/T185Uhu5deMXH53Gp30gSB6" +
                "/hY5XULxXJ1eh5XL07/GWDV+vm3zbU76lHNVY8j9uf77d13c+j" +
                "r/R+litrGUEb2USHDTUkS9Sinj/AGKjhEVnEWV0by0PusYdwEC" +
                "a0pji29gjXg3q9fuHrGvx1J+L9s7Ku3DcmYP+bCL+7pfbP8nl/" +
                "4m9fWddq/3TvCzTeP/1+Vg2sJpglv8+Q3j/LNU3o/bO+otn7GG" +
                "GVKLNCaP88kB+wdT3Ao6/0AZa7f80I2sgmOmyoIVmiFvvnExgD" +
                "NTwiizira2MrQu6xh3AQJrSmOLb2CNeDer1+4esa7J/j8f5ZPV" +
                "LuG+PFnnTEWbbeP63nuNpbj6jz59Owxx1J7p/jzr/Z+bNgIlbj" +
                "zJKkRvtnuaZxvX9qhlgPtX+OIz+F0P55MD9o63qQR1/pgyx3v8" +
                "QIo/XbWUsWrMPm7e7CaPbovFM8bNynJEb9DtTwiCwkKiL1rSH3" +
                "2EM4CBNaUxy7fht6hOvBiHr9wtfrDuWHwvMFYa7V78kPue/z+a" +
                "HqBGPOkkb2pJnoyV+sxK7cP7/PmvzQ2A00xudPRiUOW7rmnh+h" +
                "lZu51t/NkuZXXt+/qFfBkZu9DyYR8PzJcXQN/Pf+jW4rfxlspG" +
                "bX/Tv+fUQY9mite8RRlhzD3xPt2Kbkr6ONOmIYzf8+2ohWLDGi" +
                "NYK5NcXx/e+jjSF/bLIG3lLMsoXZQuqzhYiRXN9BKCNiSzPxRw" +
                "llxCTH4AzMKpqYA0YRxO9n92tM69FXejcOnqVzax5YGZ1Zs4o5" +
                "F7YLMvt70fVZ+buRJCdX3yKUEbGlGUk8YwllxCQHxdUR2U9zwC" +
                "iC2KrMdjFSvHQ2jEvI4Hk6t+YB9Qwya1YxZ8lmx5vcVka6iZrd" +
                "Bx7SGPao0T3iKEuOykSojY73m3TE0L4yMfY1ici8hFmoEawykY" +
                "qPmK4CrpZy8JZm5rEb3SYSNVvPmsawF427n0yyoHoe5qi8EGpT" +
                "jDBKaF95ASMyL2EWagRznnF8xPz+P4tj4WopB29pZh671G0iUW" +
                "trW3GlxrBHje4RT+eovBhqU4wwSmhfeREjMi9hFmoEc55xfMR0" +
                "FXC1lIO3NDOPXeI2kajZ3NdoDHvU6B7xdI7h3aE2xQijpOwlIv" +
                "MSZqFGMFpTGB8xXQVcLeXgrQmzzW4TiZo93oc0hr1o/PG+ma3Q" +
                "o1GO4QPZ5hb13KwjpuwlIvMSZqFGsPpgKj5iugq4WsrBWxNmm9" +
                "wmEjX7t1ymMexRo3vE0zmyK7JNLeq5SUeM7evDEpF5CbNQIxit" +
                "KYyPmK4CrpZy8NaIWXgHUH6JV7o0ln6+GfbpO4aYY+SM1vcbdc" +
                "TEPaou/ZRSP9/k90MSzze7UvGn+nwTt0bMbI0XuU0kajZ3pjHs" +
                "UaN7xNM58n3Zohb75yIdMWUvEZmXMAs1gtGawviI6SrgaikHb4" +
                "2ZBe8zlM+P6mMlNts/P/pGo+dH5pvh8yOzuPHzo5G57n2Gpk+P" +
                "els9P7KY+/9DHkk/P3L/f0j6+VHd/3vs//b5Ef7/IQV2S/odBT" +
                "yCWa4/Eh5PeU3fNWt0XOTFN1ey18d7XnNoq+cdwd256DgLuYd6" +
                "ul8XH+9hbh2BbZ0VW+qzQ4pJfM7RFSrr+Wjq/DSVeqbOhMJg8M" +
                "L/Xz0H5zevZ/r8OdV6Zh2Ze/O1g0d/Ruhguf5tjYgtW7tZ/2HU" +
                "kT02jFEc78t1VvQrzkodcR5B/LnoOxrT+tCXMev3WLhizZ7jh5" +
                "k1h5hzYdGZddqxk0ev6WS5+leNZJ21M0XLnujP9rXpJOcPCMYz" +
                "ezQ9EHqxTVnPzjiPIKGNtsBsmpUbw9whe7J1VmgZcog5i2/4Ha" +
                "ev/L9R6o9H75j2tU3pw3Yp+7xvqlH+l0/D92n7psq/uWX/YBL+" +
                "D5nITS8=");
            
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
            final int compressedBytes = 3325;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXG+MFVcVH5VSirDQIn9sgWq1WKthS421Zlt9780+UdQW4o" +
                "qQUlvRxtb4pyokpmlw71v2vbA1NmClTYqyVImJn4rEpoKltMXW" +
                "T/3ih/6hEgktkTWVxOCXfvLeOXPm/Ln3zszuAs7L/XfO75zzu3" +
                "fmztx3d96aLWZGkphLzEwzy5Z7EnuYOabPzDMLkqS51SzMJIts" +
                "usosNb+y5QfMNeZas8LWPpLprrPpeps+btPKJDvMDUlxmE+am7" +
                "PyVtMwnwHZWNO0reRzZrX5vC2/YL6YeIfZaO40Xzd329o3bNps" +
                "vmnuNfeZd5l3F4jLzGybzzXzzRW2vNy8zyw27zdXgnZ4ubnaSj" +
                "+YIT+a5f1mVdanG239JvMp82lbDth0i/msadkyNYMs/hqbvmTT" +
                "l2263dyWydbZNGS+ar5mNpg7zCbB91s23dPoa/QlictdCQe0Mv" +
                "kBkKKEsFCD1sgp3gI8T9xHPp4t5ukAaXwO3AtJNEYieDTul/fJ" +
                "901M0buMrCNqzhQtSdpn3Qc9uRq0mj+SMp5zTVVdxxjbTNrRXy" +
                "eBA7GI096kR+RFzNpnh5dL3lhCn7R/LpOjwHsLMfATYtZY1VgF" +
                "uStJBu3RcZCihLBQI3ve4m0uoxhj3+ZRSeNz4F5IojESwaNxv3" +
                "mf9srYkgcfGRlZR9Scc2x/o9+W/VDmmn5sj/4GpChp9KcPkhYt" +
                "sYYtVzocltwHRECtju5z4J5JojESwaNxvyDRsSUP9O1QHKnHwe" +
                "dM0eh5lLe22TQvm4sHCtkSm5aavfA8svmKQnMduyevZPVPuHzk" +
                "30WbPY/S9e55VHaY9WZjVt6dtzcHMPZ5ZHZmteJ5ZMvseWSWwX" +
                "x3zyMzzp9H6fqsbp9HWTnA/LHnUe9as2bkbXoe5Yh15iv0PMpl" +
                "d4V70P5TcW6eKO5tB6U2fSLdxpGxw+Fyi21akyTb36OlITYYhd" +
                "hILxIFrFwaeRRaITvOqLwXjn+IJVpR5Jif9kH/STL6lNS2z6aG" +
                "I6OjYXGAcnipcTItDbHBKOEnkUYBK96C6zMwUkb3N4xqHwyxLF" +
                "gRv4gfmu/peDGeh+2Vfz2LMp6OWOQ1lddnhstqI1rjZFoa5LNR" +
                "s5Feeh+zmNkMf7lNiyUmyC2PDfOdjs64RJk1ZSztfP9u0PvOdC" +
                "fkUKIM2iO3oWTk9lw6jFpAgI1MgMtbw+S7qA1zLxQdvREzjfF5" +
                "cu5SL5EMP6x7zD0U2GHoq+AdYMJHkrMXmn3F9flsTGOvj1vCZ6" +
                "5zl8SFPLRfSWof6b7OnXGG/jF6tBwzmdhlR+/WINvd6W7IoUQZ" +
                "tEefQwnH0Id0PHGNb5dx6ec+SI42nEUoKnkO8SIehCR8b6XuMf" +
                "dADKSW62X/iW+m25XusuUuLDPNLmyPvpju6q0COcjaE6gFBOp4" +
                "Ahy0EM+9tie4F4qO3rDmYzjP3o2au9TLHqFHYCR7DPXOOLdwKI" +
                "7kHmX/iS9nnySDVxeaPcXceUldzXtqztE95ZrBt2J6YlHmq4qH" +
                "8xLDxGNP7vCZKv3ywHrp5fA6pXq9VK4ZPF3NYlp9Xe72lyK60+" +
                "dpPIP+00fSRyCHEmXQHv0bSjiGPqTjiWt8u+z++QPug+Row1mE" +
                "opLnEC/iQUjC976ve8w9EAOp5XrZf+LL2dvxXha4Pk9cmOuz97" +
                "PoWV9Wx1cVD+cldn32HjpP1+eyoHTx4GLIoUQZtEdPoYRj6EM6" +
                "nrgGI3C5rZ/gPoRmMWdGrGRU7tnnRRaEZPgTusfcAzHQkWU8zp" +
                "njB68czPezsXQ1V3dp9E2QYltiuUVID/shFIFdIw9jHLAKsUAe" +
                "FA0/6ooQtpyv5FfUv6djhbxm3N/mUbwrUdlAzMbMxswkcbkr85" +
                "2nmdgenQApSggLNWhhDVu8zWUUY+w+HpU0PgfuhSQaIxE8GvcL" +
                "kt5OGVvyYPufKrKOqDlTtHx8lxZ31iPFGfqxenodqbleOlLe7u" +
                "2O3pWWnpd729I4V7p/1u1NOdORLZWj8efiefSvmKauh3C7N55c" +
                "8CPGtbd3sr2ZNpPDxXiei2nqegi3B394MXsRi123N9NmUuyCNF" +
                "doTfeyyXko7Obw9tj9VO/OnhrL7nsnx8Hvk0R0L71Q48n+dvmG" +
                "9xfFN6ay/tR2Y+y+XM9jIEaFXWz9SXaS5VR5qLP4ZPok5FCiDN" +
                "rdPpRwDH1IxxPXaLt8PLdwH1yDJWfhRyXPIV5kQUjCd+fqHnMP" +
                "xEBHlvE4Z4kPjPEzxdxYFtNUnKVnyttjP70Id60IV+pT3d5Mm8" +
                "kfinvKFTFNXQ/h9uD9F7MXsdh1ezNtJkeL8VwY09T1EG73nrqY" +
                "vVCx/zjZ3kzmaCxqLIIcSpRBu/shKSEsorU94nniPiBqa5O2Ij" +
                "vipeOQRGMkgkfTDPzYmj3615FlPJ9zjljSWGLLJVhmmiXYHnxW" +
                "SgiLaG2PeJ64j/z++VttRXY5uyV+HJJojETwaJoB9sn3TX1B7z" +
                "KyjOdzBgS948Xf8MJ2d13ofSv95lXovSmxVjpLXvPx/L32OLzW" +
                "X3PxOP57WPz9NInA9ZLkiPjuWv32GPcg13wcIeNJJhxfPPd6mE" +
                "Mtu3/a74XdfaTN37rr1bmTlKGqPTR7VZhOQ6Ik82Yvtv5ETLn/" +
                "UPzs7bye1kucEe/T5rLifdrB59T7tC+cn/dpm7/7/71Pa/s0xf" +
                "dpzbHq92nTk+lJ+6Q7iWX23DuJ7cHnUcIx9CEdT1wDbfKaz/e/" +
                "cB9cgyVn4Uflnn1eZEFIwkOffN/SQveH62X/ia9L8l1TeedKF6" +
                "QL2me7Q+f//un8cj8X8/7pYk/t/hlnQjo331M34+18d6WNNwPn" +
                "e2o1ZmH3DpvjfH/J1r35bmXZfLdlMd/BV+bNm++Z37Z5Uc93jJ" +
                "8Co+h8d9oMwea79RiY71aaz/cMD+8rzqD5buuR+W41a2xi8z3z" +
                "oOY79jLDw/vz8xvzW+dc7spsbZi3XNvVuYSwTg4JZK4GumztEJ" +
                "RhDf2SHygxGuj9OMTBobSMW6AOPAIPkGEUrod6ZxxtcRS4ljxo" +
                "JtgnZGrHNn96QJne0D6GbZQU99y/Wq03l6W9rHMZ6UJSih/Tc5" +
                "xGUJvmO5eGamF2fgyzNt4TXWtMNCZaj7scSjvSecu1tYSwTu6S" +
                "y6U9JKijLUZCGfdImtbjqNV6xGAcwEoZt0AdMgIbjCKtKD7vld" +
                "NwrYzAmRAe63otarYWa7x1/gpM34eTZOTUZHdEm29W/0W9zt/6" +
                "Y8+Psv1PjF3lv1zvj4L4Fn8OUj6KG4v1/C9t2q1xjXNT2CcQNs" +
                "3T5fqwRB7bPywx2AeUxewxdrn/zqZyfWwUmhP+9dmc8Gtl12eV" +
                "lX+2W5dO9/oED9k3lolJXp8Tdfw3J6Z1ff4XUn5uit9SdJ8O4Q" +
                "g5ietT2DTfKteHJeVesQ8oi9lj7HL/nXvK9bFR6GS/F2n2Qcpl" +
                "xZvlnXvtmP6ccclwzb6gp+J95c5NgV4Im9bL5fqwRO1jvgKYzs" +
                "2cG9nF7DG21sv35zvfKY8fGwWG2IE51LLr85DU5vshO2rth+wo" +
                "eR7tqLauE4WjJPPmjuh831GHQyw+WZf5ac6DlJ+b4leb3cMhHC" +
                "HrH9Km91q5Piwp94p9QFnMvvdqHf+dreX68lEwl/Dfy9B6KV1N" +
                "eaZ5vdNnjuvfy6CecIEIxZuPnXkhpHlNezQbS9f0qzuz+O9l0t" +
                "X4exnz9/LnEcbWv5fRKLe/FLLLrSO/l8nvrv+B5F2fz4dwhCw5" +
                "vw/4luwe9mqIQZUkzFv3AWUxe4xd7r/zYLk+NgrZPtIAlfY8DB" +
                "T7SwOuDhLcV9k+x99fQj2WLm2fm5/VgeD+0kBofx7ju0/Z/pLT" +
                "IgI1xJrvL0Ec4FZ8Ox6QVuH9Jafh/UEPPhPZo/RMeqb1jsuhtO" +
                "cvb7m2q3d/AnLC0CfzdAb1Eudq6A1wWAO/5ANLsHFaqJGeWsgB" +
                "ESFeZAGMwCvidW8oPtkCimsxgmZSXPU5FvbnYb/O35/vvqD25/" +
                "9RvT9vTlbvz7dmTXd/vjVryv/v4uk6+/MWNaX/dxFa79Nc7h7z" +
                "vxPU/f8M4f3hfH/+n9Xfzuv9fwb5vc3/fuTvJzePyl3u8Hee5l" +
                "F/nzy8bx3lvx5zer63HvC1mFfth2hUcafvJMlDle+ytdfH7As/" +
                "XYlyNd6K2WOfqvyH9Sgl/zE/dL46xTei7uv4/Uie5zorzjJU63" +
                "i1dVUU9/0oPKugFVsvYWztX30/ejgcn/8nkZCf9lB7CHIoUQbt" +
                "1mPtoe47JCEsR0t7SM6SRyBM1qfHfI+K95CMQ21CSpnUa1tiom" +
                "NzLPWK98WPICNJvF1L7c/aG13p8k7+K0Rb+4XLQc4xai22P7A+" +
                "2w8o1D00i+tCFoh1Wmfdjq7nEeeYIArikQ1oKA7VmocQIZlLTs" +
                "1D0FetxwjkX/aTXREbMKf53rvK12Jeef/cUDJXK99dbW8osw+h" +
                "XI23Yva949X87Bg8GtajlPwr3P8Awttsrg==");
            
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
            final int compressedBytes = 2930;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGusHVUVHmt5lAKlDykIfQRKFalVWlFsqNy5c6e37U0aSA" +
                "uobTXaUikp/vDxS390ZnozE++VSAyJoD+M/UOiSUVaCBTEECRp" +
                "mvuIxB+CoNgYaDS5WEB5ROres2fNWvu9zz3n4DnZe/Ze37e+tf" +
                "a6c+bMzOk0iqJo8HAUpTPpzODhdIbPeC9GHBE9fwEHcLACLjxh" +
                "rG65n8C5os6EEUdFRkIdokQR5QkNRFEfbAeXY0YiR4GLKDCmea" +
                "CemGElwE+M4a3nDrryC/NK7sVV0VzltRanzLg9wtg8QCDDg7dE" +
                "kTkPjIbrMLHkDHg96d8c+fHDapYm1fhhNYopP3WWzgzNH5ofRb" +
                "wXW46IGR+X68FCOfhGjDaKiDmqNvVcSDUoAluahR6VKut5oQcy" +
                "kR8/qq6YKgAXWHIepvVjvjW2aGiRWndh4628QaAwBy5axf5pxq" +
                "masn+uAkR4mTjoCT28zSw5Azku9YmPq1maVOPjahRTfupsaFH2" +
                "nWyuysu+m13M+sWsnp/NPlJbLmXtCtb+1TCuqfuPweck+wRra1" +
                "hb2+CfJmo3ZDdq9VxdI5uy4Wwz227JRiLDK9udfaXefq3u92R3" +
                "ZfuzD2VzFNZF2SXNaAlrl+PxM1vRcq4llXosW9dYP99sb8puzg" +
                "aVej6mRNlmyG+XNNvL2p1RlF/N2qp8cb4m+ybUM79SjPLlskKx" +
                "xrTu/IrI+so/pVlWsnquibp+5Us9+LVme/xrC3+Zysqv6zCjdV" +
                "rFfybq2e7Dn1HqOZJv67oOC1g9b+5BPUfc+MHllnr+NkQ9jBWY" +
                "6W3t6Halnt/qgfodrJ7bo76/8h2WSj0ZVE8DK3s2NHZ2Dmvnt3" +
                "tlcywpJpMvKLwXDl2YvZhd5Vc8dJHYgkL2MkVVXYb/SbN8yR2B" +
                "Z5ldQPgLWWPHgOwleRXqq83oc56abNWzJOgXswOev8hYfA/vRR" +
                "Ov8hSidX8Pjr1/4bHZYW02Hk4+ILPkzMVqXLHd+rb46B2mU2f6" +
                "YDv6qbp/zvbTh/tnzo7RYzs+gM/7A5ZKHQraGw51G39gh2gsk/" +
                "YomT/E9tGXAZV7z2p+6WINvuDPxhel/IvMwvzd/hBbxfOfU3v+" +
                "K1lZjYD6tjj8vG3gVvr9nqxO2Hli+WO8thq4lVtM117aUWq1YC" +
                "WrTZjJaruKDGWpdxXEWkzR7VeQYo11PX+TzgATlPiWXqO6dej5" +
                "fLsPvO2/ip3Na+yu/n/ebedL+THf36hmPdrDI88T7VHkuV7VU/" +
                "Ycu/v/V091TZYaHO/q2PmGaI1We+5V/tfEQ2ZnEUg9H3TjZotb" +
                "FdYANpt//EiIfvyIG++kCvnTbT3fV6JsUeeqxZqfxCv/2r/9Ei" +
                "LZMiuDzvbyZ3r4eb+3jX1WyXWzOlct1lVKvPJvfaznZnOubexX" +
                "gmrwbLd5iOuj4if0+6hq7+Vkl9XoA/k0G18VpLfecj/kD+zz/g" +
                "uv9+2+66N8gl8fZa+q10fNbFk+aan34yHXR/Hj2VbT/ZBsu3p9" +
                "lH01/Pu9Uu7ljT7J66m+ilMd3V/i9Tzcg0/RHz24rZ5Puf2KLc" +
                "Dq9P6SEucH0ItRXc9zFTTFse8sEVnoacbM3j5Ofb1JWJC5yIgh" +
                "qTsvt74tPnqH6cjHz+o8RW1EnXOLa/80e44d6ePxc8Sca3v8fC" +
                "1o73+xh99H7fVmdb6S61Z1rlpajVdcnmPP9LGeW825tvixIJVj" +
                "3eQgX6vl7f6W/93Em81ZvXI+/3s3HnrtIP+OKq/Dej5/JEQ/Pu" +
                "LGbVWIT7tsKuqKYfcy1PO5Hu6Ppzu8PjrdjWoHe9Cd0JP981Ud" +
                "hd73/a6ypHr+2Z+Ny9/E4iM6s/mXk/78GGvKjIMV9W066dehx/" +
                "Ol0ad1FHrvWh2soV1+75AolMVHvI3+zu0Psd36+WkzDlbUt+mk" +
                "d0Cfv95+H83XUei9a3Wwyn/6vUOiUBYf0ZnNv/yHPz9WgzNmHK" +
                "yob9OJK+jFqK7nhQo6hGPv+WclzYbsmNnbz5FZkHlz/lnJEfXY" +
                "bn1bfPT26cTj0ItRXc+LFXQ4/zfF4+F42JrRuDQbtmNmbx8n/4" +
                "/MgsxFJIYMu/Ny69vio7dPJz3aZvpuW88FMtr8y6yjQZ/3o47v" +
                "95f83iFRKIuP6Mx6P/ltU34a66wZBytGtumk34C+aO+CVMt0FH" +
                "rvWh2swXl+b3eUwXnFh2UWH9GZzb8IyI+x5ppxsKK+vxrkfsgK" +
                "7e823eG1lvl+yJvdn3cW583yfsicoPPPOd3eD0n3QJ+2/8KnWq" +
                "mj0IfpWc4/3/F7h0ShrDrzPX7/gbP+/DjLjIMV9XVescB2/V7M" +
                "78819th7Ud9ftuNncUH0gbzS9hy7WNLun5/UUXkcolcrrZXOAV" +
                "/3e/uiFJfKLD6iM5t/OROyiuIyMw5W1Dfx4nNFU4+fxZUmHjI7" +
                "uMqWfMb3u3Gzxa0KawCbzT8O0o99eAdVIPfrru/PZ2FouAffRy" +
                "tm93nvRWzv3/kc0ZpM29+HqsTEQ6brfojuSda0x42bLW5VWAPY" +
                "bP4Q26fvxvUqaEeGvdAXH2/ruUlHofceAR2s8g2/d0gUyuIjOr" +
                "P5l2f8+bEaXGfGwYr6Mi8+EB8QvdiCTcyrLbIFuZQt+9OGEZBT" +
                "7yN7dUXl739AjoNzZMo2GVd9MRM1NuXKecuR5Xg0EuUnbyVvqX" +
                "8DYeOtGhEozIErxuhpwmU1ZR95ExBQcnlCD29TtjQLc354f0mN" +
                "ZVLVa+KzNTHPJGz/573Y1sgZmFfbwEI5+EaMNoqIOao2a3qXal" +
                "AEtjQLPSpV1vNCD2Qiv3xHXTFVwAzUyHI8mrPMb48M+6DH+/PF" +
                "Wh2F3nt/fp/j+917rpHuc/mbWHxEZzZ/iO3WL98z42BFfZtOut" +
                "twf+kWHZXHjrU6WON3+71DolAWH9GZzb+9v7Tbc39pt2tVqC/z" +
                "BjYMbBA936JNzKvvCStYkCtG6E9ndE5tGGNoLo2KiJ4DVUGLyp" +
                "EZNBrVFRY1tpwHrYwcWY2o5ozR6tnGgY04Fo3Vs2StkjnQU7Zp" +
                "rN1h2IgxqkU0no1PFU1s1IC8aGbUH3q+LVfSXOTs1bXQJsfx55" +
                "9+ud3X8fh5o47K4xA9/VUt9nuHRKEsPqIzm3+5NmwVZhysqC/z" +
                "kslkUvRiCzYxT+9LJtP7hB05+Bbs4hT4Uh5VQ9Xm876QalAkma" +
                "SZYVZyVMjNnBd6YA6YCY8tr5gqYAZqZLlWNGfKT6aSKbadgm2N" +
                "TME8vT+ZSu8XduTgW7DVRhExR9WmnpdQDYrAlmahR6XKel7ogU" +
                "zk89jyiqkCZqBGluPRnCk/mU6m2XYatjUyDfPqR2ChHHwjRhtF" +
                "xBxVm3ouoBoUSdr7/xRXo1JlPS/0QCbyeWx5xVQBM1Ajy/Fozp" +
                "RPf/GttsNvZ/DvgKodIf8/g4rLvyKbflOulgPi+/8Zqtt6/f8z" +
                "lCtD/n8GPRefTUTH59/TnXj/E55/j/fD8+/pzvr593HBhOff4X" +
                "xefv49+yF//h30TM+/j3+7RhzPv6c7WRTv8+81q3n+nY2WsHY5" +
                "QczPv+8Xz78z3PH8O61HzdkmW2v9XdQinn9PJpIJtp9OwLbecy" +
                "dgXq0CC+XgGzHaKCLmqNrU8/tUgyKwpVnoUamynhd6IBP51dXq" +
                "iqkCZqBGluPRnCk/OZGcYNsTsK2REzCvHgIL5eAbMdooIuao2t" +
                "TzINWgCGxpFnpUqqznhR7IRH78UXXFVAG4wJLzMK0f862xk8lJ" +
                "tj0J2xo5CfPqCbBQDr4Ro40iYo6qTT0zqkER2NIs9KhUWc8LPZ" +
                "CJ/GqdumKqANxqvRpZjkdzxsyZ7fnkee0+VG3jrXpNoDAHLlrF" +
                "8VPGQVOeScfPHBDhZeKYdNBDZ8kZynEpP14qr8IcP16q5mLKT5" +
                "2ZWNL10U3Oe9gbwn/vUOo52v1vNKOeJ59GN1l+qVgcoh7GMrz+" +
                "B8eT4M8=");
            
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
            final int compressedBytes = 3244;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXN+zHEUZXSxRpDSQRPKDkBhRrmAwRKpULATm3tkNXAgGVE" +
                "QKoSAIuQSjYInlE9J3Y9itG0NpRAglmNJSSwXxJoCKrz5Tln+A" +
                "BeQpCRWe0DzaPd98e87X3bO7gWzYqZ6e/s75vnN67vTe2czdtF" +
                "rhVfxXmrzmD+tR95qWeQkPzJp1uDXyZXP27B6O5yPDq+ocNNaU" +
                "P33eOPWnzxuOp2ehjl9SXCL70CMm494RiWoEXDmSUfcwj4TPjW" +
                "vU5/MxVgWSeuAqiMQcy2A1riuR6VVW2/oYnM9VsXKsGHuGmu83" +
                "hG3gc4M0X/UiG+M9kHB9yhhRexxr7HkiRpOf8wZbMcdHRfUFZz" +
                "GCmMwprs8xexZ4tqKhW7Mz+6L13mmN8RpnvdvXnl+0Jv56dF0+" +
                "3puepGrnrc5bsg89YnX8AYkyh1DC0DRmFazGnv1xxUdvyjmDjo" +
                "5in6kvxR9dZz0O+A/YLFvBzoEZ9jxYJ8yvrtl1YRtc6eukeXyn" +
                "jfGeEbvnOI9jjaHrfZ2tmOOjovqCsxhBTOYU1+eYPQs8W9HQLe" +
                "fMfd+93+/PdB9wZw1iH3ZL3DluuV8bHVf9NnQrfFvjLuhe7/v1" +
                "7kJ3kZvyR5+qsIt9+7Rvl/q2sa6wiRQ+566o+qtc4a4eRP07id" +
                "vsrnXX+X7W3ZCeLXebu8Pd6e7yR3f7ts3d4+bc/e4M974B40Pu" +
                "bL//iDvXLfP9UvdRt9Ktdufr9ek+5qMfr5jV7wl3mfusn1PbXe" +
                "6PP+++4L7o+yt9+5K7xs34vnRt/+41W1cPc93i242+bXVfrmI3" +
                "+/Y1d4u71X3D3e6+afx+y7d763O8PmyDn8x6aa1W/1wb4z0jds" +
                "9xHkNj5tkYTa699bZijo+K6gvOYgSxmWdz9TlmzwLPVjR0yzkr" +
                "1hRrZC+9xmTcX1Gs2b0XEXCVHedrBW5ctT6fz8RZyIOvWAeR8H" +
                "rsDBuzeOpR+1hbj+cP8Dmw/mMFq8T8Ym2x1vdrta+QtTrub7AR" +
                "cJUd5yufG9cQ7fbjcRbyandrUx1EYo5lsFrsINWO3Wv9WNnqpZ" +
                "45t76HfTC5X7ox+tTw4Km5q2gvTP5+qcnrZLWLTcUm2RebOCbj" +
                "fh3VCLhyhHwe8Zhj0GjvZVUgqQeugkjMsQxW47oSibWtDz4zVj" +
                "lWjD1Dbcj9/FdHXAObpze/o+tzzwSvy822P53arVb5ZPmk7KXX" +
                "WNi6r/av0AhzsAHjxkiaV83pJ1wDcc1hFzlVVM75gg8wwQ/ads" +
                "ZcAQ4syridP/xW2L5yn+/3aV8h+3Tcv0ojzMEGjBsjaV71OeF5" +
                "roG45rCLnCoq53zBB5jgB207Y64ABxZl3M4fftm9QZ7Ro/5nmp" +
                "ARV/0zw5HeV8ZdP/2NuVrj+Gji9G6e6Hp/onxC9tJrTMb9qzXC" +
                "HGzAuDGS5lXX5wtcA3HNYRc5VVTO+YIPMMEP2nbGXAEOLMq4nT" +
                "/8svv4k319372sCRn1rywnj5yuf1+K53SKr8+ny6dlL73GZNy7" +
                "QyPMwQaMGyNpHnQtonF2FnNSnzlf8AEm+N3FeMZcAQ4syridP/" +
                "yye/PJ/uHBe82dEfLH8X5KblMj8typuhJ+fN0IvOF+afdNE70+" +
                "95f7ZS+9xmTcLzTCHGzAuDGS5lX3S3/jGohrDrvIqaJyzhd8gA" +
                "l+0LYz5gpwYFHG7fzhF7nTy6Ql9/Nfj+6VK970spP/93mb035+" +
                "OJ6PRKq3Wo7OQWNN+ao9qv5wPD0Lo593dF/t3j6ZVdE++N79Pp" +
                "qsdvlU+ZTspddY2Pzno2s1whxswLgxkuZVc/oH10Bcc9hFThWV" +
                "c77gA0zwg7adMVeAA4sybucPv+x+fjmtpLvmL6iP7p7Q9Xno3d" +
                "fYfXTEKrtkctqjnsdVOqvi51z++rw7febGz7fiZ1HDnseZOb3E" +
                "T+j4eRxc2Kdg6fO2/ra4PhyEKvI8LvXTfil2mT5bG+e+ub0q9z" +
                "yuPdWe8tiU9hVzSsf9+zTCHGzAuDEiY1StvbzANQwyNfA7xa6s" +
                "KldOfSEDTOK/EM+YK8BBrGz12LPlZ9b7fbLe/e+j7RNa76+8h+" +
                "v9lUmu9/aK9grZS68xGffnNMIcbMC4MaIKHPfHi1zDICvYGVxZ" +
                "Va6c+kIGmMRfjGfMFeAgVrZ67Jn57fPb9fNV7cNROA6tv1OiOr" +
                "ZczsjhXC36Kf5JEcnKuUCm7nWLaplc9mv9QTvWylVNHKdziHJE" +
                "s1wsF/3v+UXtq9/8izruP6wR5mADxo2ROK/W/jPXYER7dpGqon" +
                "LOFzLABD9o2xlzBTiIla0ee7b8zD3pIb2f7z+SR0be1R4aPm7/" +
                "ffL3801eoV0eap2WF33e/M6E3rWfm/wsGj8fTVS7WFmslL30Gg" +
                "ubvz5/ZCPgKjvOVz43rlHP6eU4C3nwFesgEnMsg9ViB6l27F7r" +
                "x8pWL/VcM1YXq32/WvsKWa3jftdGwFV2nK98blyjPp8vxlnIq9" +
                "2tTnUQiTmWwWqxg1Q7dq/1Y2Wrl3rm3Mb1/oPoX1W2x+PpMe9Q" +
                "La/9lwk+L96e93o6tP178hvlG7KXXmMy7u/WCHOwAePGiCpw3M" +
                "/pr1yDEe3ZRarKlVNfyAAT/KBtZ8wV4CBWtnrs2fKHXp8/jH72" +
                "W+JxHGm8ara0TtNLlZoUe/dM9PfRm8WbspdeYzJuH7ARcJlt87" +
                "lBARxEbcXUl63KkZhjGaymCJyEOeVq41xodats9ViJ+cXx4rjv" +
                "j2tfIcd13P6VjYDLbJvPTXNRY3AukorR+TxudTDO1bAMVlMETs" +
                "KccrUVh2+rbPVYifnFseKY749pXyHHZCuXlEuKY7t2IgIus20+" +
                "N62GqvV7zZK0YnQ+j1kdjJXZu9fGLB7nwkmsrdz5AzwrnkeqYJ" +
                "Usf/Dvdzt03x38RV9/MUV1P/JfVne8m/ehzo5x8pkVjnjUlN/t" +
                "j+OvuzePaxT1c7zif9LqWoO/UO/+NMcD8yTep01OZ/9wPB8ZXl" +
                "XnoLGmfNUeVX843nQWyqPlUdlLrzEZd/aVRzv7JA4ONmHHjRFV" +
                "4Lhfq9u5BiPlUXYGV1ZVveV9IQMe4KR3XzxjrgAHsbI9V+zZ8u" +
                "lO46Hkfunn0b3IQ6fmrqJ3/2m4b2rw2pubtHLHv1925vy20JkL" +
                "o2ovRwu6r3g1R/FBVPEFii7Yvs6eq3nEtD5qdEEVlMd8daX1xI" +
                "PW5wzVEY81vqBuYseoV49wJuaAiw7XsvNstWaOyDOp0IeRPtea" +
                "OdJ/rdXadY7ElWO/RRaOFDd/SXbE9vzUa+aIaNTXyw6bE1BxZJ" +
                "+ZWb/e17n8JE58sbPw70uqIx4Fl2w9Rt35A/xULiA4E8qNv4eH" +
                "2eHIH7/daoXvc4U+jNwjfnxOOAqRXTsl7mMXdL/n0fXuQn88eP" +
                "LkLlbcYxu5Jvf8fa5Q12W+Gar6outuq7LuqrO3Wd7M271vy/e5" +
                "KnSZr1h9n8sfn0/n8239PldVsfo+l6iE73NV+JU4n/J9rsFcrv" +
                "dt8H2uKvJP+30unp0edW7p3FKeGfbS+/fUehTGcQTcEA8t7G2+" +
                "NDnWXFXSGFcEUp6paIwrR3WEa2OcoZg6khxVsVnQ51kFhFGrwE" +
                "7A1+PiNbmKi9fCNSx7OQqI4LpGAhqvd8UlU49tz+s91M0971Y1" +
                "0W1e7wFVz/CQW++qIx4FFxU9bvpLgYDgTGhevN4xOxz544Oy3o" +
                "uD7pdhpOu9OFgcVLxe7zd4NFnvintsI9fkntd7qJtb76omus3r" +
                "PaAVg9a7r5hZ7z5ar/eKX613UZH17vErUZfXu0eu943We1UhWu" +
                "+YnR61z26fXfwn7KX3SD0KY0SECa5sUkFxywtHWk2V5Ejqoob2" +
                "khNQOQKOkXpQRs4XMsSRVFV+PBvoI1dYjKpC7GRwPpW7vL28eL" +
                "2OvS77EPOYj4djiYSxxCp/y7EHLr0gqCnVzHX4ulZRJelVn3FW" +
                "0y2gWl9ZYawuEVcWjtQtR1XfemQNWyEXwzma/4Rvn5xfPn9p/8" +
                "X6Lv5Z/Xuw/ktjPb9bMwS7LImsP5l74/7LjZVXvrO/D+l9t4G/" +
                "NolsOLn7+PnL69/1J6aX+s8TS2dOyEj/3nvmhEYGnzmWBm5gm/" +
                "ucE7bPxThH65pPM0uVG9BwFKvY2sIFR+eAGCpab+wo9cH1LGpH" +
                "chZy9TPn+WeDz5u/HufncvL/f8jue9/9p7p3+vfzvVP0nd6G50" +
                "eHy8Pc67GMy1naawyoZio+qzXKw3mFAT6rVbgy5czGOuDX1Wa1" +
                "ch2dtThQ+DFzSmpH52AWaM2YzTvBPFWnG31rA9/vKLcm1+6/Mj" +
                "+TrU1sff/s/mZwLf9h/t95Jr92nTXyOtja/e2Id7NXmzLHus62" +
                "xu+fuz7YuD5/1/39YPB/X78DBQ==");
            
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
            final int compressedBytes = 2528;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW12oHVcVHvy7JSitCEmxKmKiUGIbCBixpnjmzDnxappW0l" +
                "ibKFIQ+qQI1Zpb8MHMmer0am7RFPuiVqkR9UXxKdFS/9omrRJM" +
                "DHm7BOmDvfSqiCA1vjh71qxZa+291sycn0moc9iz917rW+v79j" +
                "57fu+5URRFg8vjP7vP4HJU9aLIlcFltEAfMG4PfbBAHODBSlkg" +
                "FjbAgo0jaUM28CMDRksccBIKx8CV0VhQI9h8P1fI9dNMYBxw4I" +
                "dmho8zipKY10k8eQ320ULYyc/CeZDxcpv8PYrsCM3qWHU/xyXx" +
                "w8vNGR/ex61ay9KBnsnf2jTrWWGLv4l7aJXf2Leh1N77qQ19sG" +
                "gboShS9+nR7RiJQuWo0VKGEc35LX6KbssTP4p7aJXz+S0otfcA" +
                "taEPFjsfIW2fHt2Gcbo4CpWjRksZRjTnt/gpulueydNRdOzBqn" +
                "0uGZdrecz8f1RW/Zjqye9Etl+mu8r6VG35dXpBZhT4p2TG0vZs" +
                "Uf4Qck5OahrKiN+6fXqu6p2u6t+U+zM+9+RXio6zDpXutBgY8p" +
                "miPDd53rsS/AcKbKPHwhbHEbL7JrPnX7T9tkVuLg/H4BjQZsXn" +
                "93fL3+yfZhZwfRYzf8Fb7Xf6fd9iHj81Lk2jaPhCNPc2udjMZC" +
                "lbBHfLN/0Rq+97+mKchUte3xX/vu7ci9zS7fFKuiN9S/reeKX8" +
                "XleOPZi+DVpoYd/+irIiVkKfZqv43ol59TzOq/s5rtC8TdPAeG" +
                "7mVq0l2+nbQ45mBj1rur0o5XzS8Q7zWRwbk07fyE0Nvl3hfC5o" +
                "HWxr8d9sHO/GmOR8lpadUyraXZ5ZLyV7B5fcJ9lbXsfKHrTQQj" +
                "HZG5Xr+15Zl7g3hbbqTH5JsxLWsep+jnMqNQ0Wj9aS7TCfzKlp" +
                "0rI2rc9H/tnP+sxXF3A9+uts6zN/pM/1OT4/Ph8fdXuoizNB1X" +
                "N9sgCSsPAp76vPo1/iXAuzAQ5bkJdyYA0xzgst8lMPNSBC00UR" +
                "oAiyIt4fDfFTLKC4Fxl8JfX58yjGNq7Pf/WzPsePX7v1aXEv7P" +
                "x5yhX3qc8kdXv1DZ3u1U9NdWdfoMc/mH8+21gtv8U93Sia7pfs" +
                "9bm61NP6fO7aXd8t7vnXZ/W9nHbFfciC7dUbu2aYjm/8/ALW5+" +
                "nZ/Bb3dKOYcX2+dd71qaDd+jx7Ddfn2a7rc8bv+Ywr7kMWbI/O" +
                "dM3Q3Qd8i9E9i98ak650JmUbvB5spA9gHy0h1o63beTTrMRv+T" +
                "lusCGf38MI9/xOVq2lq+MczQxW1uoO6nj8ebeHUh3v28nLkd6d" +
                "y4vqU/bxhifw463vo463YyRKKofRNHE357f4KVrPk93UeP58d0" +
                "/PR49eu/Nnvtbv/We5Wl+B4r//TPaX6+8vEkdIjqJaPX5e4e8/" +
                "Q6TM6fw+i8ZJmGQ/jgFtVjxyt+W3RonRdnyym9fJ7lGCfbSEWD" +
                "vetjVnIX7kbdJLKq2Mzk9WrWXpII42zXrWwTqvB+u4PgfraKnP" +
                "HgepDf34oB+v5XTIen1ej3mDNbqOrLqf4wbr6f4wljNxZXpLtj" +
                "UOnUGzhd54DfdxfYZZ3S29YbvherTWcLZfa73SrLVjJEoqj9e+" +
                "8o5m7ub8Fj9F63lGt49uH7zk9lAXM131XJ8sgCQsfCAD+iXOtT" +
                "AbMkEL8lIOrCHGeaFFfuqhBkRouigCFEFWxPujIX6KBRT3IoOv" +
                "pF6fL1FsdRW6rr7m31LP9u+9b+eTJfJd1HcWfX1qkTXDre0Rxw" +
                "53wGxh7TcXZRsxSUZ2fT9R4d/fkvujjd57j31WfVq4Y3QH7KFG" +
                "G/STA2jhGPqAD1BUk4cYuB3yUg7ugTo5wFWErI5Las8i6Sevr0" +
                "Sq9EdGCnxm0ByOn2YSY4vv8gYo1RWjXm/ZB6JoeJ6tshJHyO6b" +
                "jOE5Nb9ukZvLwTE4BrRZ8cjt+31NzfzWLIx3jHcke9ze1eU3U/" +
                "Vc37W5hbDODgVsrgU+yKrZsAUZyYI1sgFvyEMaHMq38Qj0QUbQ" +
                "ATbJHeqo73/2SC9l8JVADGLVcwPdzwfPMemflDflL071fHSh4P" +
                "3u/M9H2W0tTyvnjPdLnblne/9ZrPSNeKlYwUvDDejh8T7cyIrr" +
                "VXb9sH7Wd5h4KV7y47WcsuYxww0ekX8D/cgP/njJ1lvO5w0cg2" +
                "MgG2WUGrkiriP9vndEL8lxyR7Mgpa/aG8OrhRX/CvDTejh+hxu" +
                "Okv2ObCXdwVXHNahBdOmrDUbj4G83p3xFcQ6r2v5LDL3cDP/As" +
                "fgGMhGGaU2rojrkPPp8kmVsgezEGYdbh1uhT3UaIP+6BlpISxH" +
                "y3heiIEwZJUZvTnbKnmor+WQCM6GHlKSP6bnprnA7JJZ8nEmiV" +
                "fOTPUvf7IPRwvfsuViTD+Met8y4zeR+ZP98trv65ItyZZ+3td1" +
                "y9vP+zqLe5Hv67xj7Tu8Rb3iLOw9s8SH48NtObRI7vWRna+iRh" +
                "wydVU2rw7vqLgvO5Tdmx0UtiO1tttmznuP6flUUT69AOUfb/Eb" +
                "89ltTNknFnjm+Ux1ZBSbp+Vjft+3mM8TAufnXeSGTJayPrmVe/" +
                "On+f18/MEu9/Mi3vj9fF4dTfD7eTP6KcWm/n6+sJ00szT+ft4f" +
                "k/77+a7nT/33896cva6+Fj7hffd3+X3fYq6au67WikAmizH/Xn" +
                "SVt+F9YavSeLff9y3mKO+2cs6vUmeylC2Kf4rr/gP1+3nv2IgP" +
                "+X3fYs6nwA1fvwCVX21mspQtgrt1tHug+O9DsqMajpDTMVTzkD" +
                "b7bUs4nxyDY0CbFZ99qVv+Zv80s5B9uV6fPV0Lh6+dP8es/9+x" +
                "CO7Zno8K7k5PCK+y/0fY2efzUfJy8nJ5V1bV2IZ+fhK9aOVe7g" +
                "sLz0ZZyepnDO4TBQ/2fJ2hLvT7GtGSvU9GyQycnxd/HkLNctzj" +
                "5fEytaEUx7t4pwAY3HO01g7e4y4TR/4jzmfheUYNTTlQF1fG43" +
                "Hv6q99nWuR6v2x8CJ52vXHJ3Afn6jPn/ukN2y35Zveh4g2jDt/" +
                "cpRUHp+wzp+Iac5v8VO0nmd0aHQI9lCjDfrjVbRwDH3Ixwv3IA" +
                "O3cw4ZgTFSRcjKM4e6KIKQhIcxhbllhD8e7pfjJ71cvTiO/lFf" +
                "6df6uQbG/+7/HtD6fUj+i/65B/+F4t9/5j/RcIScjmE6fxcWjs" +
                "ExoM2Kzz40qyLptfyN90ud/h/hVXa/dGOX+6Xs8Rn/P+7i+CLs" +
                "oS6P94vYz3+KFo6hD/l44R5k4HbOISMwRqoIWXnmUBdFEJLw2d" +
                "AfMc9ACnxmySdHw/GN6/Oh/8P1+VCf9/PaRufP4Xu868gRv+9b" +
                "zCvQkegqbchkMfpj6uN505rP/OdzZ9+lXhN+PL/u7IkWv/E31O" +
                "zOTufPJ2f8fcj/AElBLjQ=");
            
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
            final int compressedBytes = 2221;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXFuM1FQYbjQ+CBJNNC6wIAiswqohXgB52ml3hsQsmvCwLI" +
                "YnH0x4MxrwgReX2VZmZxKzGF5M1IQHxWBY8JYQMT55iYrIroKA" +
                "q0GWeOGirprwYAK2/fv3//9z2m6nO52ZbJtz+y/f/53Tc9rTzo" +
                "JhlJcbRnlF+fby/UZw7NpRXgS16hYjxVHuTNCt1iRLDcP810h5" +
                "VJ+MRe6YhtWqaHnlvRj7xVizRwPJfUZdR/khw7A6rU7IofTk0P" +
                "Lq1eelhGy59dAkl/CEvoQRyCIQJTep522ylDKpV32JiRWDTWPh" +
                "2ZrX1cgyHo+EbKguD/so1oqPSo25RW2bW9JdO2lnXjNyOzBSHL" +
                "M8Y7tjd8JNPwb1X900bv+2a0fQvlgd9Mt/3HQ8mNMnhPeYn3/n" +
                "pt/d9JebpkLd1246A+vdPhZKT5bH7bPm1QgmE/ZP9nm3PGdfsr" +
                "+1vw/ll930t7A85eVDb/j1yUB2mul/cNOF8nEF/xc//6PyviL/" +
                "Jnb9plzv9hX7T6yXpkpTkEOJMmhXd6GE29AJuqFJ9OV2HI1Qgz" +
                "nyH8fgmtIUZ0asZFSOrPMiD7Ikey+27DFHIAZqZBmPc5b2+oHz" +
                "0x3PsnbNTqS7ZqWF0c8jZ155nLQzOWB+Jjwdjiczm55/2vnp3O" +
                "zMTdKz8bQz30c2x/bTHc/K0daN54snU/EfaNw9lY3ncOb52ZE4" +
                "PztaOD870vLPNj8T958jWfefpduS9p+oncmRdf8ZF5v2n2iVZf" +
                "/J9hEjmJvhKFb3KNpNVIc2SCL3JeJaSDtz2utkjpgpriW3QubI" +
                "MY4ZeiTjx8Un73gc54GgdK+Hs9BZhuvdWWGtS7PenWA2Oyvd1B" +
                "31fuTMD20Xl8edu2PWTbhCnM5617uzlNWXR6135y4/v0ftk7Ng" +
                "pvslp8u5Vxn1NZCCnf5Leo3bkWX8EeXJ3vm0q6JjponCbbAPKI" +
                "vz392RDj9Zn24UguvyXDgqNTcdYijKU9vcbMY8xz1PadnoNxHO" +
                "KypSLLND9ckbcKcPx9Px3vBHGdd+hXu/KgkxflYtGz6eozHj2Z" +
                "8cMc4vTt4ApsN6LeA4oLY9ydBkEkaUp/XxjDnGImAkc6BeT3FH" +
                "utLA8dyj17JjtNvhPNLsiPR9yVqTmfXadh3P7H3KHHFEr2XHyO" +
                "2qb8p4pdflyaq4vrgecihRBu3KRyjhNnSSjieuwQhczmNID/SR" +
                "LPSoHFnnRR5kSfb2U2qPOQIxUCPLeLI3ZF/qKnW5b1ddUAbvWl" +
                "3Yrr4CUpSQLdSghTVseaX1tC6TMUhCGp0DRyGJaiMteDSOCxJk" +
                "pmITU86Q91dGVDlTNO0N953wffO1zOt9u2xXX2+b++f2ZkcsfR" +
                "qOQuY9bmHUaNOj+cyGVoYjuxdSBtYXcr/qezOO54XmjKL5MuZQ" +
                "8+fnaanV69Ph1a9DizRRuJVkHu+NmmT8uPjkHY3jPMa/fzpPKN" +
                "8/L2u7jcfTff8snE/6/tmQt+KM3z+RWfL3T2fjTL9/BtHeddf7" +
                "DeH8vBil1+4Pk9E4Oa/3jG+E+TOL/x5SvZrqfhv1/v5s274fNZ" +
                "0Z+/3oetbxLBxo2+f7gdaNZ+0mbe6m/D2ucCj6/gm/xzVkF5Lx" +
                "97hC6j1go34vdgbD8ZyTeVU907brPXdmQ7fG3T9rc7NiVj5v1/" +
                "GsfJbr95DVxdWQQ4kyaNfuRAm3oZN0PHln4SBHI1QeVyKiXLXg" +
                "qDrPKF7kQZZkXzio9pgjEAM1sowne8Ptk+anUzZm3TE0p6mr4U" +
                "s34h3hetf23pUvUj6P3mzb53uuzIpbi1shh7JyDFteu7bKyz0J" +
                "SitfoRYsUMeTdxbOQAsjUAyKS1FJo1twVC6pdavcdQ/iQEwKZ6" +
                "SXRCAGamQ5VrI33D5pv1TckHn/Odm28zNXZsW+Yh/kUKIM2rWH" +
                "UcJt6CQdT95Z2M/RCJXHlYgoVy04qs4zihd5kCXZF/arPeYIxE" +
                "CNLOPJ3rAx6in2uGUPlr6mB9u1tSjhNnSSjifvLBzmaIQaRmcY" +
                "XKNbcFSdZxQv8iBLsi8cVnvMEYiBGlnGk73h9kl/X2e9oO31X0" +
                "33fQk92+/7kt6nqO9L9bwf8e9LxW3FbdY+L4fSjRe0vLZXhxws" +
                "yRZO/7psQ72082qIRqh+j/ZxFNKAj6eFGsoJlTwQI4oXeQAjQA" +
                "UJMOFeEgF8wYpruZ4zCa/SPvKNfx5Zg5nf6gbb9XmUL7Pea73X" +
                "IIcSZdC2HkQJt6GTdDxxDUbgch5DeqCPZKFH5cg6L/IgS7KHPu" +
                "nY0kPtD9fL/hNfzl7cWW/Ua/Xul6a5iv2tm5/pYleuZ76vJzyP" +
                "KhOpEOr+94atfB7Zg2meR87+xvzeIe+fzlsam1vSYdvzgtmwM3" +
                "KO7Gzh/NyZ1qpRLMvh+nXezqdPwwtaN567LzU7IhvPgzmNZ2fr" +
                "xnN4YQvH83BOfVo0W+dnqa/UB3mpj8ugXTkHUpSQLdTIn7d4m8" +
                "tkDJKQRufAUUii2kgLHo3jgsQelLElDz4yMrIaUeUc2G4sbbRO" +
                "eblX+vvdoOW1vTrkJMOa6+0nkHk10PnYmgz9IAJqOSLYgJZHpD" +
                "jEATGieCEDYgY8QIZRuJ4jhLv+U1JLCCoT7BPY9o71jrlPsjEs" +
                "/WfbGLatJSjhNnSSjieugTahhs9PhsE1ugVH1XlG8SIPsiR76J" +
                "OOLT3U/nC97D/x9XVne89qewVf5iVrGWixjbYkpTbpJWZUBNKA" +
                "V7QNSgk1ypIknKGMy32wT7w38RyjEKJlMnrM8+iD2ff7Udz/H9" +
                "Kg3e1E7wTkUKIM2sPzUcJt6CQdT1yDEbicx5Ae6CNZ6FE5ss6L" +
                "PMiS7O0jao85AjFQI8t4sjfcPul9c3jxLHzf/CTP759Rh3MEa7" +
                "V17bpqzcy7yOElLdzPf9i243ml+Z6p9vPdpW7IS91cBm1rA0hR" +
                "QrZQI3/e4m0ukzFIQhqdA0chiWojLXg0jsv7pGMTU86Q91dGVD" +
                "kH9v8DD98y1Q==");
            
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
            final int compressedBytes = 2342;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFWklsHFUQnYQ9ISxiiUIIZLGxwUAgcUIsgTJJT7MECXEECZ" +
                "AQi7DECRFxTXtsC6tNBOccOAASIC6cQIC4RAhwIEIIBGKRgJFR" +
                "QjBOCAkQ1v5TXV2v/u/uadvT7mn1/7+qXtV7/7u36bE/4A/Uav" +
                "6A3+7pQ5axJzaQlz2CpRFZPGILbfRpDvFIxNWAVcRjYzQC2bBu" +
                "PKf1mlvrqNW0QpyvZrQ1C1utVr/SbFzJjMia6Nc+bDGiW/SjbX" +
                "PkfRjLuDS8VGRdosyOiG+iL60++vQq4GyJg7ccZevMJhbt0Xrf" +
                "on3YYkS36M/jyF3PdbpiGl4qsi5RZkfER3Oy66NPrwLOljh4Kz" +
                "aTWm2kxaOxd2ulfPwzapV9yuX2D/gHqOWeLTMKd5pWPDpqZ7DF" +
                "EUSEHmPMp/GznSV5osvmEY+N0QiJoQLubW539lxdM2s+V3OMmP" +
                "Knon5KerbMKHzQtOLRUTuDLY4gQjDt9fzHzpK8WN2UyyMeG6MR" +
                "yGYrcLnd2XN1zaz5XM2Ym3m+7y/prDinwvN90blhPd8raU5nVr" +
                "iepXLXt9S3UGt68ZEdPkle9giWRpKPFtroEw5/CbJKxNWAVcRj" +
                "YzQC2bAueWxurQNXRjPbjLbmGLu1vjXqt1IfR7bS1jwYBuRlj2" +
                "BpRFazhRbhccca8fXzMLJKxNWAVcRjYzQC2bAueWxurQPW02K2" +
                "GW3NhPX2eftqNdNSbyJkmXE4yh7EyEaxZotzEaerYdVoTsewhv" +
                "g5B1WksUrlNF2iQ5CCN9x6xlhBFOgoxvX8RS+qV5GXeRROZkXw" +
                "02xlV0iPNH4q/zqZpaFs7pEN0d4zctHItewJdo9cHq/nM4UqrM" +
                "6JbXQ8a6M5/dEF3Ss7xK/OWM8M7pE1jmdgjoo2dby/T5XzN2z8" +
                "Ut39ffG5YT0/KmlOvy28xtjH1XHnXGVe816jlnr2kR3uZQ9iZJ" +
                "MY7hhx89pzOoE1xM85qCKNVSqn6RIdghS84dYzxgqiQEcxrucv" +
                "elE9foLdyf3o2ZKeqU+r8Hm+ZG5zP8pcz+cWXH1j6py6oHv0+Q" +
                "7xFzPWs1bkfjT6wtzvR+0zr6fRwyPtM3v4CnnZ1ljMSIvbDOD7" +
                "lSOUlaZCMrnlLW8GqFfrE26bK62qu07ZK2ezO8+TS5Pj89XUO8" +
                "E3C74fHS1wv/m6AOZzGH8R7d8W5x77skPtz3KjX2VUX91YzSPt" +
                "M3v4OnnZ1ljMSIvbDOD7kyOUlaZCMrnlLW8GqFfrE26bK62qu0" +
                "7ZK4fswZPB6e71Mzgvai+K1vON4JK259Joj/DBUzGit932JRnX" +
                "RHv0fSC4PrZvgGpbgm1RpTcV97/tyK3BbcHtUX9HcGfqffH+4I" +
                "F2/2C7fSh4LBgOlgRLLdSK4IJ4dHG0r4JI8stOcDVyB/FzdzAU" +
                "9zcH24Od+esZ3JXiu09ZD0f7I42+RrQqpqW+zdjHdvgOexAjm8" +
                "RwxwjZUjW+JyzDGhhpJH8jjNusWNnVJRmCFLzh1jPGCqLAZtZ8" +
                "qBnxjf5Gf9T3my38sNH+PZMsMw6n2MNeiZLFMdwxQrZUjblnsI" +
                "aKJL+oYtxmxcquLskQJOBndJauIApsZs2HmhHvz/qzsZ2cKeQz" +
                "e/g+RdlmrHjFduNYzXpmWc4RyvJn99xtqyB/+IGw8WbVmkVWGp" +
                "sqe67Q+oTbVplW1VHsxBurtA/nn3l/bxW5vzdbc36mPru6+ztz" +
                "l3N/7/B+6UhJ75eOV/h+6fhiv18KkntxWGjecz8+G79X+H6pVG" +
                "5vv7efWurZR3Z4iD2IkY1izRbnIk5XQ380p5NYAyPeflQmqjSr" +
                "VE7TJRmCFLzh1jPGCqLAZtZ8qFnjc9/X/VjSMfJXhcdnydy570" +
                "MOL/R8T38f0pit7n1IFnd33of4b/lvUcs9W2YUHjWteHSUrWZL" +
                "53NEEFIjZj3LZpU80WXziMfGaASy2Qpcbnf2XF0zaz5XM+Zmnu" +
                "+HSzrnDlV4vpfK7b/tv00t92yZUXjMtOLRUbai41Plc0QQUiNm" +
                "XWqzSp7osnnEY2M0AtlsBS63O3uurpk1n6uZEN60Nx3dl6a5b9" +
                "+pptkOT7EHMbJJDHeMkC1V42PkCNbACPeowmXFyq4uyRCk4A23" +
                "njFWEAU2s+ZDzRqfe77PlHTOnarwfC+Vuz5Tn6GWevaR3bhJew" +
                "SL6GYLPbgLg3C05/SfW9HVpauix8ZoBLJxRJTY3IjVujWz5kMm" +
                "jU95/gl5NFnh/xWW9Rkr9Pt7c28XGY8l67msWzUnl6u74OnVrW" +
                "cx7rHjXVzPk8kqXFD10TR5YabKeX4PHztRCDXP/wiqH60fpZZ6" +
                "9pE9uVZ7BItonY+7MAimff38263o6tJV0WNjNALZOCJKbG7Eat" +
                "2aWfMhE+Lrs/Xo+5dpqW9HZtn2d2uPYBGt83HnXKmRrIVT0VrP" +
                "Wc0jdloNjUA2jogSM6e02hwX3ZpZ8yGTxue9Tx4r6d3BxMbqri" +
                "BP710cniB5Zz56XTLva1OR6xfKNV7guhzcWwAD98sgutIGKwu8" +
                "R4n/Hyy4qUPtXbnRe4LH0/w7Nu/YTC317CPbH9cewSJa5+MuDI" +
                "IRr67o6tJV0WNjNALZOCJKzJzSastacHXNrPmQSePzvh+NX1zS" +
                "d5TvKvx+VCq3N+PNUEs9+8ieuJE9iJFNYrhjhBnQjxw6g3O0Cp" +
                "cVK7u6JEOQgh89Zc8YK4gCm1nz6dkgPn7KHfaHTUtjHk2oX5gY" +
                "I3FGp42d5+hhyuyMRATj0vBSj3WhMszn1vSjezBXq7fngjvOX8" +
                "+l490ivqp7g96gczwPFjzuB/PwRauUckYOdh/Z4RcQuX5eVtI1" +
                "7IcKr5+Lzj1+eTJaU9Kcvq9wPReF29/l75Ix7d42b5uN8bYRUq" +
                "PdMWdKBZPHca6SrwgrpqGlBinSyjCfW5oVatHqtWVUympons76" +
                "4Xx/Ip71kDfkXFOGCl57hvLwRauUcv0c6j6ywzeD5PfiyZ5C7w" +
                "rn9HvxyKfRc8PNC1fZfKnDUXEw4/vmo4WPq4Gur+dVDscn3VjP" +
                "RrO69SzO3a31TM73Xq/XOQd6C54rvdn4tLqLeL73dh9Z7PhsHv" +
                "S2O28Tzi1WY3RFrMmpMHJ+mncef/U757me27uP7LCe/yTvl3aU" +
                "9ES2qbrjk74flfb5H+lXQsM=");
            
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
            final int compressedBytes = 1582;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWs2LHEUU74MnUfbgwUM0CRiQxaDowUOC0D07A0I0IiKIl3" +
                "yKBxHU3U3MZpPszlfSMiLrCoknBVG8eVvBfG0+TUSCLMKypxz2" +
                "4NG/YMF+U6l5r6a7Zl6/qZoy6UdN17z36v1+9fZVV/f0RlH+WN" +
                "xS56kdUzuiqPkxtYGGc2i/In8Vd9Qj3SsbZ8Ouf8/1HHRUJioT" +
                "6lOdtQ6kca/zvKlBX+rd2KQa2hABMeCotvIR87zMqFTT72N6UD" +
                "RtQSb92NTX5G0im3gUyfS312eWz8nIy1FdiIId48CuTYLgN9Wy" +
                "fO42dfQTLY1N/b02SWPaMaqztcnhjGiUIn+MqHkhM3M81VVni+" +
                "JTnZkFOluFoaWIWTwdT6tPddY6kCyfL5oa9KXe5njaEAF9uvk8" +
                "no9o5sq00+/oaepMe/9YZNKPTX1N3iayiUeRTP+C9T6re52X3a" +
                "+Fej2b08zocRp/C9f7jNeryXJ1WX2qs9aBZPX5qtZQHxS00UYt" +
                "GoHqs36TxjAsy5QZsjJRaeQ8LxyBnsS/2T9jGgEZ9CObeJSz6V" +
                "9QQZ/19qM9nv6Knwfcjzxj15/L2q76U/XduN7rzzzI52usCNsG" +
                "2F7KaXZmc6o74P30ELtlx7Nh15/NaV4oyeiVYj25fh5mXck2h/" +
                "t0jvRQ4R7whIN87hPW54lxrwidz6w+33GVT5IHyOeZgPk8EzCf" +
                "Rz3t76cCXj9PhcpntkqPearP6YD1OR2wPuc85XMxYD4Xx53P5n" +
                "yvPhue1tzJgOv9ZMD6bOaqYv/IdQX1Oe+/Phe2W/I5HzCfX3ha" +
                "76cDrvfTAfPZyc3ir3L5LLifX8vm1Hbw/P7jkHzfs+STjV32fp" +
                "6Rzy891edcwPqcG3d9kuf3r9znszunY1GwY/zYOp9RVNkXPXKH" +
                "7znFv0EDQY3up1PcCHybwnPDW2Jv7SnD1O3zUeVtD9U/4SiOsM" +
                "58zIlcTZaqS+pTnbVOfU9f1xrqg4I22qhFI1A9xTBH6DEmizwq" +
                "jZznhSPQE/1bSf+MaQRk0I9s4pmzof4Dn9+XPOzv9ezv9KaD+y" +
                "Xh+470jYC/h3ztZ72nb4Vb7+n+cPlMfncVs/Wh1SL8TTB9VzbO" +
                "3Zwsf2fjfce5P/B9R1afy6wIBe874p+sz0c7o6j9ngPewvcdml" +
                "nO33jf0Tzv431HsuHnfj7kwZuTs/r806jPb8T1uTGoPp3wltbn" +
                "xrjrs32otx+df3jrs33QUp8tVhW3pLjxHWggqNH95CY3At+m8B" +
                "w8H92R2W1zKmZanpH9/Xu2h74vXe892/9uvbc+4qz3rka03uN1" +
                "aCC9PJM+6++yXsYGmuQxB/W5LrPbsIuZlmVUW6utqU91Br36Bv" +
                "3OBa2hPijK1tjUY6kfjYZRH9T9QRqDWvSZssij0sh5XjgCPdE/" +
                "PdA/YxoBGfQjm3iUs+k/aL0z67z0eo+3Au7vW37X+6D9vft7zH" +
                "0Pv0He9x/Dtr/bxrma56D6rOz1U58ujsY/svq0zcnZfrQCDaS3" +
                "Inr9SsK6Dq+UumqvlB0hQ7XZbXNywakb51doIKjR/XSaG6Esnh" +
                "veEnvrrItZyNb76PtRgXe23tNPw+1H537grndnT2qHSl7JHqrn" +
                "zfQTv7jxTWggqNH9ZIYbgW9TeG54S+y2ORUzFfC6AQ0ENbqfLn" +
                "Aj8G0Kz0E+b8jsre/KMBXwugwNBDXY50bg2xSeg3xeltmToxx/" +
                "8JKxjG9DA0EN9rkR+DaF5yCft2X25AjHH7xkLONVaCCowT43At" +
                "+m8Bzkc1VmTw5z/MFLxjK+Ag0ENdjnRuDbFJ6DfF6R2dMLHP/0" +
                "WynLEL+HpMfD3X+e/dfX82bAfM4/uvmMr0MDQQ32uRH4NoXnYL" +
                "1fl9mTAxx/8JKxjC9CA0EN9rkR+DaF5yCfF2X25AOOP3jJWMZX" +
                "oYGgRveTXdwIw2ztJ0w88lz4uDCfV2V225zy/sMQLHGuQQNBje" +
                "4n27kR+DaF56A+r8nstjkVMxXwugQNBDW6n/7MjcC3KTwH+bwk" +
                "s7efLMNUwOsWNBDUYJ8bgW9TeA7yeUtmT38pw1TA6y40ENTofn" +
                "WVG4FvU3gO8nlXZrfNqZhpeUYh7j+TbaPnU3r/mbL+v05y/9l9" +
                "f/QfavDLVA==");
            
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
                "eNrlXE1oHVUUfquiSIggoYoKthZKUClYUKFSmsnMSpoU8acVF6" +
                "3gsmotTaCImMnkpVOSdVuKTaEExf9qXWjBhQuRKrb+oYIb7cbY" +
                "hSBuA859Z85859yZeW9mMpOxdYY7995zvvN959535++9tJ2Of2" +
                "+n42/xb/Pv78TbzBH/rk6Jzb+zFPqeTmesFH8Oz8YB/tFs+7Fz" +
                "Ofi7O7Vsuz4yxeywoF2UwWxjR4vrrTXrsaODOPL8eVmuPae8rX" +
                "sg0T5cBD97tfMf3Lr7c+bz8LpncqAcvvx8uje1N59Na+/6yhSz" +
                "w8Jt74WiDIN8YCK9evKu4s8bUxpfNkvC77pkitkTe9L2XirEc2" +
                "mwD0ykV8N8XqrmzxtTGl82S8I3e3/3t2Xd393v2ru/52mn7+/+" +
                "fSUzerD5+cx+Xgo/bG8+5681/Lx0wRSzw8Jt78WiDIM2MJFePX" +
                "lX8eeNqY6cejwfm2J2WLjtHSrKMHA+D2m9evKu4s8bUx05ZW8z" +
                "RzrXwRaeqPgmsL8+VOZ1o4X7UXfvWmdz/vaq18/wxyLXz+BE1f" +
                "tRxpNwN/mUtl2/70e5K2/beit2jzX+fvRDe/PZtHYrz5/HW3z+" +
                "PN7k86cz48zQkWpjpx63pQVYiZ69Ki2ycCw44nPunTSjzk37ZR" +
                "9IbdN+OxaZ2NoSq/PWylrPniHGeoveYvQMsch174likfuwSAx2" +
                "+GSxYyVrPJ9vSg7p4VpmkVaVzOm8ECHzZ4vR1iOWDMjAVtZ6Mm" +
                "eNb+N5aey9Fu9HjWp3z5S7fnZfr+V56XR718/w9yavn95l7zId" +
                "qe6t3Mvcty3AaoSMlziOBUc8pk/TjPFZk9TSb6vaWWmEHEE6k/" +
                "CTbG7MBTKxuexdZ4y2db6v9lnP52o5595t8XxvWLuN56Wxt2u4" +
                "Ur1R7XzP067nfHeX3WVnxRypju75cc/0jT8YJjsw2HvPc8vs54" +
                "JYZiMct5wVyRJeZA/FGC+12A5WmWdwK3vtvBBBSGIlC2Uko7jt" +
                "n0WsHpdUsDNJnt1WZOx63t/94Ui3jvvRYxXfj043OTp3p7uTjl" +
                "SzjfqwSAx2+GSxYyVrrHpScijPTpkZstKqkjmdFyJk/onlpD1i" +
                "yYAMbGWtJ3PW+Iwr0/udG3jrFrofdT+ouD53u7vpSDXbqA+LxG" +
                "CHTxY7VrLGqqckh/LslpkhK60qmdN5IULmn1hO2SOWDMjAVtZ6" +
                "MmeJdyfdyaie5LrnmaR9fGR8xJ0MDpIdGOyEtov0UB+spG14wS" +
                "E9XENHskpL+JmdezoCOSATo61HTO3ofiQi7PFIRj1+5NvzTbgT" +
                "UT3Bdc8zQfv40PiQOxF+TnZgsBPaLtJDfbDG8zkkOaSHa+hI1n" +
                "SeWXkhAkjgjbYeMbWj+RQR9ngkox4/8uVYbyF+wl+go7+P+8ZC" +
                "x+StfyHjF5mFtC/LBl+WFfp5fomzMf0Y81r5bdYYpJBmdUad0f" +
                "EN5kh19PnFPdM37eAgLMAapDNKDDqeCrWZjZXYJhnhGd/AXtvP" +
                "GNYxqPALbZMRjOeMKIZVdBRj/bNyVMYjvVpBZgJ8MiNL8dwu0Z" +
                "HXp7PEFvG95FL6U9Lx+Tb4shlZTfuz9WxEdoTN3W80miGt0W/k" +
                "aLk73B3OqjlSHXninumbdnCQ7MBgJwb2a5xpMRsrUYt4wcE1xR" +
                "gvteBHj3NwVsOvZbTNiJFxjmShjGQUt6PrZxJLKOllBTuTZD5X" +
                "EVvm/ci/UvItxnp/nxvyv4vUv1z7c+Ts8gDlb3JWd2Htou/vcz" +
                "fP3ZJcR7d6W50/zdHUPb24Z/qmHV6BBVhvq4lFvGmRj1izbNwi" +
                "Xli4JqvxSkXoIAeDmv1V22QE44mR8iAbq0g/tf2zHMuzIL1gsD" +
                "PhMXGmGZ/Lc03/vrn2zbnYbGRY8VdQb96bpyPVbKO+aTvnYQFW" +
                "onW8LFAApjem82nGdF6aVVpsDo2QauxBJrY2Y8ku89bKOgeppP" +
                "FrX5/lt/Cn9t7f5zc2+u1Zxh3Jf7gS06NJ66GB59xcjSN4pOT5" +
                "PtfodWjEGaEj1Wyjvm0BVqJ1vCxQAKa3Pn9JM6bz0qzSYmM0Qq" +
                "qxB5mEP2dzYy6YXStrPXuGGNvO+vTOtLc+69TO+L5uj7uHjlSz" +
                "jfqwSAx28s1e5ViJk2xgjb9dfF5ySA/XMou0qmRO54UImT9bwt" +
                "/sEUsGZGAraz2Zs8Q70850tE6nue6t3Gnu2xZgJVrHy8Kx4EjO" +
                "1RSjdb5Pax30szg0QqqxB5l4r2Vzs5+wBqWVtZ49Qwl2ypmK6i" +
                "mue54p7tsWYCVax8vCseBI5iLFaM3nlNZBP4tDI6Qae5CJ90o2" +
                "N/sJa1BaWevZM6THbF2RXr6Rf+8I/yiEWql4f9/ibKEj1Wyjvm" +
                "0BVqJ1vCxQAAZWzZjOS7NKi43RCKnGHmTizWRzYy5663PGVtZ6" +
                "9gzpMf+f1qfn14cq+/ch4V+p3/2+rePvQ2p5Tqr677XPFPn7kO" +
                "6Fan8fEuwPnrDtwTM1rII78jzBs/28xbfgyQG/+H5fNrPyqIys" +
                "ovkM9gaPZ8+n80Dl0T7Vbz7r2AbNZ7Av545RaEzB0xXX0Sbata" +
                "2G9bmpn6dZhf7+YtpVM/Q2065tNYx2cz9Pswr9/cW0q2bY1PWz" +
                "j+I6ne+5M/Vqfaiy1881rJ7hvvej4faun8W068iwzudPv+//zR" +
                "L+3eL3ydca/f5zu7OdjlSzjfq2BViJ1vGyQAEYWDVjOi/NKi02" +
                "RiOkGnuQSfhPNjfmgtm1stazZ0iPeb3Xp/tWe+tz/bXr+PtkL/" +
                "edyff7eUt8Yt26MyuPytj+BSA5abY=");
            
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
            final int compressedBytes = 1773;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW0FoHFUY3oMXJbZihYImUrCIQSXoJSAe2jcTehA8mDRJtb" +
                "WKJYKhggVBSCW6m+zKHLwFLx56KDUiePJQCoEemmAvgpfioeIh" +
                "h6JSQVDRi7gzb//83/+/N7OT2ZmdJs4y7+37/+/93/f/82Z33r" +
                "Zp/9P+uX2nebR5qPlM+69GcrR/bY42BjymDvVi/a097bvsHeRo" +
                "/9LH/0e2soyZv1tU+89GKcfH7w8eY+rh4t5qj3zcxRU2n+ieyf" +
                "rkeu5ufTYfy/BNOJYjjUa4Pnhdmof7+Mf99jTu5hiOVj7rWp7e" +
                "paLn/XbzpvuuvKOsmEXjVJHTEO73I6lXsZnl3cVqaJetbPcoz9" +
                "U6a87a1vZks2NtYaxE4HzEMQPbk3vuCzci6WFdblS/Tq0LM3CV" +
                "aG6tGJXoWPolFfN7deUv7O6atLZdW+e+xj16tM9VGd0smSXb2p" +
                "5sdqwtjEW0nG/PY+vWSwyMYauM6OqSUdGiMRKBbORhJbEyX2yu" +
                "BUWXzJJPV4jx7rFycfDrdOzLe3V9Vq/MXMbeXO7cT+PYYluNTZ" +
                "/vt0mfPyLxS7+fT2P8M3TsNJVuBMvRj8GNaibMhG1tT7beeM1M" +
                "RA+xhbGIlvN74zUzgQyMSd6vuRGV0gnJw2NCtm5Lm/TruZDrmj" +
                "928xJmJXJxGCSTxHu+jz6t+o6IatwfdQ4PZRd2YuoEv7enH0Mt" +
                "on3vfbNxVhaWYxEqXQ8rlspwPrVxbxZRi1Svc8FT8qTrr2O/GT" +
                "1S334zbX3K/WZiKbTfdOvZGaF6motF60kz/fUs4yhaz7Scyqmn" +
                "mTfztrV9wjhPY21hLKJb22jBk+ZyjJ7tqhtRZT0veXjMSGmTfj" +
                "2XlWhuxEJWVzWz5NMVkjkP9n3k2x/1eUK7PvAz3vVqZ0YF76Lw" +
                "ZHjStrYnmx2zBTH8Yh+eei5G7eV0DWOgh3pU4bKGJ801V7s7A/" +
                "WTJeaWGWMEVqCZJR9qRnw4F851+znqE88cjdmCGH5ZX2ub5iIO" +
                "o3HUXj1vYAz0UI8qXNZwztygyK4unoH6yRJzy4wxAivQzJIPNS" +
                "M+nA1nu/0s9YlnlsZsQQy/2IennotRe3fTUYyBHupRhcuKkV1d" +
                "PAP1k6VzWGeMEViBZpZ8qBnxwVaw1WjEre1jjx3Re2oZwy/24Y" +
                "me1m9ynuUOTmMM4dkifejXrKzTp4tnoP4dy2mdMUYgbPRksNW6" +
                "i3ElH2pm5V3bZrDZ7TepTzybNGYLYvjFPjz1XMmRPBf/gDHQQz" +
                "2qcFk5sk8Xz0D9ZIm5ZcYYgRVoZsmHmiV+d7/PBx/m+54jnB+f" +
                "N0oVRz7uGFWWyuCbdE+wmi8C4WwvIwar+aJUkwVzp+dpUWWpzF" +
                "qfKyP5Yqw82NPl/YXVbx3S+lzKiyqmspb9+3P/p/179fUMTtVX" +
                "zzRurufK13utnmZj4P3mRuHfQzbyrM/Oo3upntEL9a3PTxb24f" +
                "0+U+P9PpNrfT41nHoGy/nqSTgXn9RzucZ6LuepZ4wqqrJ1UEXe" +
                "+f8MncnGnj0+ejwl2weGraSM/x/S/Crz6W6+xufPXNzRswW/J0" +
                "fNqG1tTzY71hbGIrq1jRY8mYE5kpym3YiuLhkVLRojEchGHlai" +
                "uRErdUtmyacrJHMebH16Y2Suz+jF+tZna7HK6OGp8JRtbU82O4" +
                "7fm3VrtzazTl5G6NPi7MjiOWpyJdcxCnpIgVTBGLRQZBlFzuCM" +
                "ONd4nswYMyNsPEIkRpT5s15Un75/N1fSRxmfI1fS8eZK3ih5GM" +
                "qa5+ZZjCHreck4/2a/+nkZz5+lfGsW3R+dqPJ5PlwIF2xr+2Tl" +
                "LtCYLYjhF/vw1HMxau/euIMxhGdn94J+zYqRXV08A/XvWO7ojD" +
                "ECK9DMkg81S3zW/R691th3R/TqsBm5nmZk/9Wz6pwyPz8PaHT7" +
                "+z3/+Xkg1/795fL+XqaMv+/o85T2U33rs1puE5rQtrYnmx1rC2" +
                "MRLefjyQyMSXL60Y3o6pJR0aIxEoFs5GElmhuxUrdklny6QjJn" +
                "//oMxoPuPbNyXux+x3PuksfT8TbuwN8rZwru31O4m5fyIrOOju" +
                "fXwM50L95Y0P1Mid4QHGM5Yr7COB/exh1433i7YD3HctezgMqp" +
                "yalJ29qebHasLYxFtJyPJzMwJrnfL7gRXV0yKlo0RiKQjTysRH" +
                "MjVuqWzJJPV0jmXP7vdX0+tV+v8Xlp6Nxl/L7UJ6czNdbzTH31" +
                "NDMV5TRTYz1naqzndEU5TddYzxTu6K3q63n8O4f1nWIxV99O9Z" +
                "wbbj3dnIa3Po9/u//278PPCep501mf7+759Xkz5X4/X/3vS8dv" +
                "VXC1DpYU56WC9bxV3/qM3tt/9/vqYn313I/HsX/LQ90r9Yw+qK" +
                "+eFf998X8huRKX");
            
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
            final int compressedBytes = 1333;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW8+LFUcQHkQPBiEHFzwkQtj1IgaF3HKIvtf7BgT/ARHcsw" +
                "dPC3uKSHzP7OocvIlBJQZlo+CPixcNgcXAalQ0YWGzBIKXRQgs" +
                "XtxbloW8fvXqVXX3zHNm7J7aHXYeM91dVV1f1WfN9MzOGEXu1p" +
                "7ko+lLkfdN/fHxPmaW/GK3f4oCbSafITb1dyS2VY9t8qneBMjp" +
                "jZyPrHkh8qyqPpNv5epz5gvZ+gzC5xlBPg/K8am+iWq3VZ9T+P" +
                "psnZTjs3rsonx2lgvnNCHI50QN6/OoIJ9H68dn8r3gerRSw/rc" +
                "IVifOzY6nyWunzsF+dwpx2fzVP3ul0Ln1B7r7vvau9tfouTcVP" +
                "vz/nXuci4Pnw3RHXIknp5P2ns+oN+fLr8wlmG/15EcKBjRV1LX" +
                "T9H6HPFnVfh8d57Nkp/L+Zzelan5pGI+BZ83m2M1rM8xQT5Hnf" +
                "q8s+nrczTjnvh2KMRzU0O4/rPgv427Hi34ibIz+wHk1xl8HsnF" +
                "+pHi65Hc/byXZ6y7Jeuz4c+qTnyWPt+VP6stPrtMHfZntcVn9z" +
                "pxL5fV/S0+c9bnuD+ronzGp+vHZ/U5zZwNntMDQT6DYseT8WRj" +
                "Vh+hjSIc6bHWN2ZJQrbaMp4EDygDCezgC70hEsq4x3gZNY1Z1H" +
                "I9euQxkGc3Lo4GUVAuZn5cjx4wK8ohDcFkCKOhudb5/kvwNeGR" +
                "XH3O7Alan3PxHByhRRmMScJtbGtzp1kcgeb3+lddj2mREQ6OuK" +
                "Udu6m3YxxIrtoZcw8cn+82D27MPH/reXMde+q887R9y0uNfCdX" +
                "n25OVf09ZHxkfKR7bj72jzA+Isdn9djtueBr7DXB9T0odtyJO3" +
                "CEFmUwtiVky607y1zCd0IgjF7/uuvRjcv0yiW2jWnB0VDDcr2e" +
                "7pu4QO8msolnM2TmXHV9Jr/WdX1XJ9SJxlt9hLZ7J9Uf6bEtIV" +
                "st17s+mvNhhz7ORSSUcY+kabxFra1HG8QBW1PGZ6AOI4I5iGLO" +
                "InyeldZwrYnAIyF77Dv1eSl4fT6p7f3n+/i9eqeP0HY57o/0mC" +
                "RgSbbwAw+oN+10D70hEvTAL/nAFuZoLfRITyOMAS3S4qIZEBF4" +
                "RXs7G8KnuWDFtYhgRzI4z9F2NV5Va/oIbVfTH+mx7ie/gZxs6N" +
                "eLdRX1pp3uoTewwx74JR/YwhythR7paYQxqLXOP3y27RHkEBF4" +
                "BQlExGcRPs0FK65FBDuSAZ9rbG7/Cylo1Qq+P1IrKGHX25Svqc" +
                "z52bL+1WQB/ab70Vo19JstrVUr5vujFJzXXJrWS8+GYwxHyPLq" +
                "PB9NBb27Xeiiv/p4P2Xfx/nALraZfCYvA/D5UpDPDOzkRTV8Wl" +
                "F6eV8c/yDHZ37sMO+L1cMAd70P5XxkzQuRZ7nrZ7HvP3v1eUOw" +
                "Pm9EAbfWYmsRjtCiDMYk4Tb0Ix3f7bncaz+nm9wH12DLo3BRuW" +
                "c3LprB40eJxjYz5h4oAhvZxOMxm/ZVr+89Pp8J/n0pMPaw72lb" +
                "T3N5KPw97fRxD3GX/J42Kyfze9rzV8p9T9taai3BEdoe4tJgPI" +
                "8SbkM/0HWWcS63497I6yAr5oNrXAvu1Y0zLS6aQZbMft7OmHug" +
                "CGxkE8/MhtsPW99bv/tfj6S3fDmFWd9bzwNcXz715OdYST6fy/" +
                "GZLNaPzws/yvHZ/Lp+fIbIye/9/Obi8+K/eaySvzYTn2q7HJ8+" +
                "sDfU86bmU7A+VVS/8725Lnj9XA/L3rDno5weCj8fNf+Tez7Kwg" +
                "73/w0ruH5uEzzftwUtz/8B4QiMCw==");
            
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
            final int compressedBytes = 1785;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWk2LHFUU7ehO/IIkBDEOQiRR4oyQhZAs7KnqbnWIY9CtEP" +
                "+CJGSZhd2kJ5kGBYXA7LKRQNIrGReuhGSlgh9Z5gM0szAaDG5G" +
                "VAKxu27fPue+96qmprv6Y7qaqlfv3nPPOe92VU91J6WSv318pj" +
                "TCrf5MqRQ9LIDn+GB1RWhvb8vqZ/2nfBy1vT38ax7DDWSH2Rpf" +
                "bNHvH7Kdbe2/fnj0/Szm+lz9ZXLX54Wvx3191k8NzxF/VJrSbf" +
                "zOirg+4zNZ12ch7/rxop1NdT9PT20/T+/Efi62p7Wf6c6m+vo8" +
                "NbXX56kdeX1endrr8+pI/9otx8tylFFjvflavLz6KyLAMtrW9+" +
                "ZrklUFYJLzNZ/R92VZOZI8f962MZt3a2mta2Fu9EIdWGXrgZUs" +
                "PvDOf9Jf1cWB36eLg2dH/Lw0du34Uz5b/X44jmD2qwn2M0W7fq" +
                "kQ9vPxeTnKqDGZd8+7/dQIsIy29bxDAZjkfN1n9H1ZVo64GItg" +
                "Nc3QWtfD3PVLvCpeh69glSw+0ONV/2zb71Nm5er9yV2fK/vGfk" +
                "d87p9l/jKxkcURVLg2tMdr468c/vt7fHZg12dLU7qN31n8mX+2" +
                "7eszszK+PrTH6+OvzLNVj1WPyVFGjckcEcbgJbnGhtYyjtnAKq" +
                "qVD5iDMzqyC1+VmX1fqGD/Gulq2xUzAxy4ylaPPTO+drB2sFTq" +
                "HrujbDKTuR4R4zOZNTZ4plV+DBqV91jVVfMVeR7CWASruavwta" +
                "0P9MdVdhVdz1Ab9vtm6H7P3irvT+7zc7Ta8VK8JEcZNSbz5LyN" +
                "SHeGrCJsfW/e7mXbxLTUj7XByBnry7JyBMwui6umGVprO4W7bV" +
                "bV1jWEFKySOsZ51vUZX0mfZbxPV9Lx8ZW8LHkUiqrz11mEy+R5" +
                "6S/DfDl9luH6cjo+vpyXJY9CUXX2+2YXNYhCfCI+IUcZNSZzNw" +
                "Iso20971AApru1nvQZfV+WlSMuxiJYTTNwsnI/zI1eKLtVtnpu" +
                "h+yas67P6dyiB+OvzHl3H+jsL9V311/F52d9f+86etpFN38OMD" +
                "yfwe7/+/uLBfne4nt4/ZVwvPVUCv4F8y3/3e3/+3v9yJb9fHb2" +
                "+nnhUJ5+NtcH7Wf/PrinRzlDzJ7Z84z76l6+WZgtureViji1Ds" +
                "N+w76y+dP0Ub0VT3RHj3IWyvrnGY7uDJZTRB4VRlnn6dWayeZP" +
                "00d1Gk/W/V7zPrvPLe/0+732IM/9fu6dUXx+1qJcPDurn1Gefi" +
                "aR4vtZnsHrszzB6/NBUX/fW7t31v0+ouelvbN3v7f2TPB+r87e" +
                "86e/piKfP5v/Nn9v/ib9bP7d47rf72dl2FU3//EifxbTz+Yf2f" +
                "mVb1P6mXtNzc2C3vn/+tpvlGZuG+2aokbUkKOMGpO5GwGW0bae" +
                "dygAk3yG7fcZfV+WlSMuxiJYTTNwsrIvzI1eKLtVtnpuh+yaw9" +
                "dn5VCl8123ZT5XKofyvU+KC+GFd9itcXuwuvzaRbhMPnlu9Pjm" +
                "KnOexlxOL3Pp+BDv+Lb82kW5bDxWmuEtWi8O5W/VW9VbcpRRYz" +
                "JHhDF4Ice7W8usrGsZNe4imNX3GfKFCvavkegbd8XMoFhFWR+h" +
                "9cNvkpuvznfGeR2TzLzOEWEMXsjx7tYya1+dODjjI5jV9xnyhQ" +
                "r2r5HoS3fFzKBYRVkfofXDb5JbqC50xgUdk8yCzqMfNcIYvJDj" +
                "nTMyB2tfnTg44yOY1fcZ8oUKIIGXNfnctsJdD+ft+uFX9qzvR9" +
                "F3s/d9M21N4/i+Gd2cwX7eHG0/A3/f98z03/fHi0P5W3w0PipH" +
                "GTUmczcCLKNtPe9QAAZRy+j7sqwccTEWwWqagZNoV5gbvUj6uc" +
                "tVtnpuh/rYclzujGUdk0xZ524EWEbbet61FhwSW3zoMzr9LFsd" +
                "zIG0MZt3a+HE1Was9W2VrZ7bIbvmcd/vrcOTu98vfDhK9upidV" +
                "GOMmpM5t3z+K7EgcFLEO7OGVXgeOd9vMscnNGRXfiq3V04Qr5Q" +
                "AQ9w0q2zK5Zz8MEJ89pesWfGV6Nq1BkjHZNM1J+/rhHG4CW5xo" +
                "bWMo7ZwNpXJw7O+Ahm9X2GfKECSOBbR9wVMwMcuMpWz64G+Npm" +
                "bbNU6h5lTH4h3NR5HGuEMXghxztnZA7W/q+QxMEZH8Gsvs+QL1" +
                "QACbysyee2Fe56OG/XD79J7kCt8/zZPXbHXuYA5jhqDFnOYUcV" +
                "2MCKqMvo/e5rdHTm+vR9wYP1qJHopK2yDP3npZOusnXte1Z85r" +
                "/HvTl7z/PNXP8frNDn+Zdn+nn+ueJQOX//7PczemL062u9NeZ+" +
                "jnRNlUeVR3KUUWMyby1phDF4Icc7Z1SB46xhK7TGuvBVmdn3hQ" +
                "oggW+97a6YGeDAVbZ6djWE/x/gUtKu");
            
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
            final int compressedBytes = 2676;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWk2IHNcR7hxjIiSIsA5JwJFssMgujsOCdAlsT09DfJDQKd" +
                "gGi/wIDCY6hKCjDZ71rmTNWSKIxKx9k2X55NiISIcFBQKWIDnl" +
                "YO9eFrS33fgSCAiTfl1d831V7/Vs72hGS3Ye/d6rqq/q+6p3pm" +
                "d6ZrNscCLLBs8Pvj+Yy5rHu5cGP5Rd/t2sw2PwgzGxlyLPc9lU" +
                "HoNju8RPpv1tPQ1+FHl+skdFP8uy4nHx2PvFJ37M8Okemam4rZ" +
                "Z6hIhWGpeps46UWlaR1jc6n896rlTV/FnPktLvreJxP+tX+36m" +
                "azaywj6fVw9j+oTIRnl9h+NqmfFnpoqNxIgswcqVY13IyBJKpK" +
                "e4ts3IIuaspf/M4Mtz5bmsnsMqD7GCnf9YvOoBVnZZxju12Gaf" +
                "5YAHkVgDV4HHYyyC2bgu9xTXhlJWyP1aRq8ZbNF1YFN3V76THb" +
                "jH1U+6oIbnJqteni3PyhxW+MTOfype9QArO+SzxTb7LAc8iMQa" +
                "uAo8HmMRzMZ1uae4NpSyQu7XMnrNDfZMeaZaz8jaRM7A1hk+3o" +
                "mlO7XYZh848pvM6tliRrZTGItgNt9FzG110Pl0zJ7Ra1b8uM9L" +
                "HT8n7PnzUv7xPn5e+nimn5e+Kr6KPgfUPvFjhs/G4ritmWIY/l" +
                "IjkpXCIBNVU0h4WKHl5ZzLx2wX7fyWZTdf08lGsVGtG7rWkY1m" +
                "HK7GxtUj4gcGQ9D+4IjYqNpwH+YaJrIx0rfBqiwrV451IQNIwh" +
                "/2Hct+sMoZvh+uaPuH3jq2XqxX67qudWS9GUeqsf7eRfEDgyFo" +
                "f3BEbFRtuI9wDRNZH+lbZ1WWNRzDV732OAMaoCRw245lX51Pyv" +
                "D9cEXbP/SGo5wv56vr6LyszZV1HrbO8PFOLN2pxTb7wDF8jVk9" +
                "W8zIdgpjEczmuwivd8ttddD7kWP2jF5zg50r53r/CXNYg1+tYI" +
                "f91SPwAFtW715yiC/sJFbXTvp0J3Xh0VW8IcqM4IGGgFr62vo4" +
                "Q/FSUXSIT1k4LvvBqubqWeAoKngl2pMqDY/F58PQWmEHi308c8" +
                "TO7GcbHMM3fNQ/FKu4FB4VVReU+Qh8l4+l6rPPngXuVjh0tCnr" +
                "fbt4Iozet2pVqBNhpx6xBRNmsZt6JzQumaipuYqTTKkLJOvQqL" +
                "KAh/EBJ5xAaQ+KlL32ohrF5+OskPXjTGiecOjAmeE+x95vPtPl" +
                "c9fS5tO/a+z9dbaZw99MeL/5oHzQ2w6zrmqFXdhX18+Rh6MhN5" +
                "UvVcNeV/XpTurCo6t4Q5QZwQMNAVVdPx+kdDEbeEIOs/h4+aC6" +
                "fo7YBMVRy8BKgK+xD8uHvcdh1lWtsAv7CjvycLTKrg+fX3fzUD" +
                "KtT3daF3VkVTaJxzzQEFDexxkaA4/ilcXHdRU2QXHUMrAS4MN+" +
                "8fTi6d6/wxzWOtJYwQ779y7CA+zi6erKUR/iCzuJ1VeipE93Uh" +
                "ceXcUboswIHmgIqOGvrI8zFC8VRYf4lIXjsh+saq6eBY6iglei" +
                "PTXYU4unqvWUrM11+RRsneHjnVi6U4tt9oGjt8Ksni1mZDuFsQ" +
                "hm813E3FYHvTs5Zs/oNYMtui+92XoXe7PbVXm3CuXq7N+x2jTM" +
                "lrv4tPhUZlnVJzY8jMGQ2NKm5jLOVrNV3/8714Bfc1hFihWVU7" +
                "qgg/Wrp/833zFXgAIb5bjtH3pZvYm0fptWdPye7ckrTOGZ0sJU" +
                "vt3pc8/bk/KO+/5z+LuD93vxlQ9m+v3n7eK2zLLWf9nbasPDGA" +
                "zE+PC5Nq959lAN6x89v27HmFhnShd0sH71DC/6jrkCFNgox23/" +
                "0Mvq+fHupZnf3ezj73yz5e6/0H9Bd9YnNmb4bKw97hnoN4f/ak" +
                "SyUiqQqbOOcR2wXqsP3J4rVTU+T+1njtn7J/vVdSbMstaRk2rD" +
                "wxgMxPgII3+Lq6HqiJ1qcCRGcFX2DH/vtccZrF89+Vu+Y64ABZ" +
                "4Zcds/9NaxQ/1D0ZmvfeHo3ZCo2oqFF7aN927Yau41d0MjkpXC" +
                "IFNnHWmUVWB5OUd7YrVpfsuym0/Yi/vF/d5HYZa14musYIe9zN" +
                "W19j5jZdTX4fsat7iwQzVd644+4iqISE6Iyk79qIoMrZHShQxR" +
                "JFXFI4o4y1aQXEFxlOOsZPRXUuxasVata7rW7Gtqh/3wt+IHBk" +
                "MQ/uCIz2vYv+AaHNGVVcSsqJzShQwggQ/ctmOuAAWe2fKxZsaX" +
                "98p71afXe1jVCruwlxk+3om1tGnzNQIEaow+LztWZosZ2U5hLI" +
                "LZvILquvsHyx13zwptv36w5gZxt7xbrXexqhV2YR+en+qxUZ+h" +
                "lkYYAUz9/PzcZyGvUXc35mFdFmMRzOYVxNxx91rdMlu+WLMgik" +
                "fFo+p5+kjX+pn7SO3eO+phDAZifITRe4eroero1UE1OBIjuGqs" +
                "M6ULGUACLz3FtW2G74fjtn/orWNbxVa1bulaR7bUhocxGIjx4X" +
                "O5asN9h2uYyNZI3xarsqxcOdaFDNY/8tzxHXMFxQ4veWbLx5qh" +
                "3HpGd7gv7e2eYO+/H/XWnvguZ+3pZ3Z5LH6z+I3MsqpPbO8Blt" +
                "FLm+zhAwzgqP+W5+OKsS5blT0eYxHMphEo8dyMtbots+XzZ0ix" +
                "+UK+UN0zLOha35EtqO09wDJ6aZM9fGguajTn81xc0d2RLlge2E" +
                "Ban437XCjx3Iy1ui2z5fNnCPhx39f1oruClX/+v39f1zvU5fu6" +
                "y2cn+75ul+8//3Twvv+8+vosv//cn/+nHf55/87n5WNdzufKZ5" +
                "Oez8T79dC8Pm61W2PeRW+143u3ulbpwjCtvLjPaajk81k8V1TP" +
                "pfD/tPQpq+OrVXEpvNR9YpVfT/i7Ugv3YLUrctwjfzl/WWZZ1R" +
                "dG8WLxovUEn+6AtvlyaKbgUUN8qMLZXpetyh6PsQhm0wh69dyM" +
                "RVfoIcVgmRRf5+wUOxXHjq51vzvNOF6Nnfr/k0eY4MEQtD8E11" +
                "jHgRvtjnMVjqgCVmGqkme46rXHGehIK4oi27HsB6ucUXuPc11U" +
                "tP1Dbx3bLrardVvXOrKtdv4P9TAGAzE+OCJ2li2/CT9zFNvLFz" +
                "gSI7hqrDOlCxlAAi89xbVthu+H47Z/6A1Heb48n2VhlrX+puS8" +
                "2vm/rAdYRtt8PjQ3nE/FwCtj+YKN+DgztNWwCMSQCyWhp1RtjU" +
                "O3ZbZ8zGTxiSsz/v/zF9mBe+S/nh4q8RvI0f5RmWVVn9j5l+ph" +
                "DAZifHBEGZbfhJ85+keXL3AkRnDVWGdKFzKABF56imvbDN8Px2" +
                "3/0CtH/Hl+6YPR/dFfDt790fCz/bs/yt84eOezradpnc9x/x8y" +
                "/PzgXT/bnp/TfZSvlK9gL0caozOjU/tUNmeNw6KWotr1QLFVxv" +
                "k6h/X9P7IWqx7W8I7W8jUlt13/2Nf7XPTd/euTvd5Xvtf2el95" +
                "5im/3ue6vN6vvPqkr/f8us6ycyqup/djPnFcnyymiC4sjLLK27" +
                "M1Mr5+Gz+yd6uTX9NZdqlovB+j6NpkMUV0YWGUVd6erZHx9dv4" +
                "kd2tTv1J6lq8m95jWjUnrZP/fHqojkqbZ3D/w36nV/beft/sVn" +
                "N2dYZfzvSN/X+dtp+U");
            
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
            final int compressedBytes = 1963;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0+I3GQUnwVhB120onQF+29RcO2uCILgoQdnMjkJQnet/x" +
                "BWiuCpHgpeRLBmp9tpZ2l7600ruEhBtPQgVsVV8S8WtMWDi+Op" +
                "B0EPAwruSdZ8eXl5731/MtlsMpndhOT7vvd+7/f+zJdkksnUau" +
                "bSumD20pbFm7VNLNk4y+Pp/lQb4rL4ea325qs4ahzQ9cGTA+y/" +
                "EqNP4vbjpYm4txrcIS2WbmX4zyyM34Tbj6Y8eMIZw5dqf2JfPL" +
                "oat19E++/0nBY/tTB8n7leX4fbt4s/pGGwnt5ub3etdmZXsZ9Y" +
                "ECjmAj75X/LZuXwHFws5as63zsMeWpSp1Zv0Jlvn28dAThhaAa" +
                "1vXIMeuDzMaZJzcA225Iezckn3mh67aUExUCTKt8wY+sFFbqHn" +
                "wxll/hQv2nrjcZbjsE/m57iSnNmFeo4Vn/i4qbPJSGeTkn+Xnu" +
                "O88cWeLQZ7ZPae7Mv5CT4GebCzNtd521zHejbXUWJi+SLt3TLS" +
                "2aTk36XnOB2RxujquazsPtIy573gvnC7P7grmKXzZ7BnU2fEe1" +
                "N0DxuSAwWdhycH6B+0y/03HPi9OiqY2WREjwyqZ3dtdOu5+Ee+" +
                "enbeylLPSJKrnu7re+bMbta20dL9uUz2zmvhsb8RnwM2YB8cx7" +
                "GSnNmFesJoZ5ENU2eTkc4mJf8uPcc1N+T1KI3R1ZN9eT0CH4M8" +
                "2FjTjvfGIaP+J7b7+dPMyXa8ty/kP96bfd42+8n1vY8S9on0LZ" +
                "9S39TZZKSzScm/S89xOiKN0dVzWdl9pGXOe1Vc37t/Vjc/T02W" +
                "PT/j46CH+0bPcpT07H33koYazNDoZfHCUTJytzVq0vld/snaze" +
                "ON8dYbS+6PxpSkfQz1HOu2d8vw/h14XTxKa9dznDfWvWGLwR6Z" +
                "vSf72v3RmM6Zljn1/LP+WdhDizIYq37zMsgJQysg9I1r0AOXh2" +
                "eby5yDa7DlUZhe1QYctrjIgmKgSJSdzBj6xEeRcF5ZKx6zxIce" +
                "VuIsV2CfXI9WlAT2yXl3xXJWXjF1ukzq7IzoX+rt/nSM3ULndk" +
                "VpMoCPQR5MVv+cfw720KIMxqrfvARykDUvoZYQ+gY4GAGeWCPf" +
                "lzgL12AEMgrCcAkySxZpQRlRrspOZswzQ6wacSRnlPlTvDz6rd" +
                "wf5bhH+bu6+6NTk+Xyp96//zu69+9L7+X7vnT6gWHfvy99WNvB" +
                "SyfTc/jOOzmfzx9qHYI9tCiDcXcdJRxDK+n4xjXogcu5D2mBNj" +
                "IK0ytnNuMiC0ISvnu3njFnoAh0z9KfzIbVaK21FrZr2EaaNRz7" +
                "Pko4hlbS8Y1rYEysiXfGwTUmgrOacdriIgtCEh5yMrmlhZ4P18" +
                "v8KV61+dP+dOhjGtr4SjWNY/9xkKKEsNCDEfZwxMdcJn2QhDRm" +
                "DJyFJDpGIrg3zstzMrkpUh4hz1d61GMmb67ru3fQO6j/vqkkWR" +
                "bE2fDAu+Xnyb18di7f5u+beaJsHW4dhj20KFOrN+VNoQSlSkIr" +
                "6fgGOBgBnlijOKc4C9dgBDwKzmrGyWM3LQhJeMjJ5JYWquVIrp" +
                "f5U7yRbr41H7bz2EaaeVi9WW+2NR/9XpxglIRWQOsb4GAEeGKN" +
                "6jnLWbgGI+BRcFYu6f6nx25aUEbICBHJjKEfXOQWquVIzijzp3" +
                "gj3VxrLmznsI00czhuHkEJx9BKOr5xDYyJNb47O8I5uAZbHoXp" +
                "lTObcZEFIQkPOZnc0kLPh+tl/hQvbL7xnNTvK5naGtdBi2PEor" +
                "TzPo05j+T0HU9tlR/it+t1tJTZUNg7sc/O2riuY22sWRYzS7/f" +
                "frH9lI5rP5/4/rX479PtFwriOZLProycWFRhPdvPtucc9bxh3H" +
                "lvZOR9Wo5Pvuyq58mXyqln+zlHPW9kYn+mjHrmX/w70+anW1t+" +
                "PbP5zhuhP+FPwB5alMG4cU1KCIsjsOH2HEceSE5+JSPKbXrdqx" +
                "6VRPAMzEhUTjZuqgVFonPpq4wYtrTnS53Vcp4vLY9V+HxpYZjP" +
                "l7T3aW8zbCzv0/L3Q1zv0yYS431agR/G+7RaTuW/TxvcIp7p/1" +
                "78VbAIzrwcLrsy8rTWs1dCPXvVcbjsisoz9X2b+ug+n8/9vk19" +
                "2M/n2flzprbjlrJzSp2fxhOr5Yl883NpwjU/+f87hjI/D2aZn1" +
                "t/34b9dpLcQTT2j+48C5Zyzs/91c3P5dt33vkz2/vJRc7P5lGz" +
                "V+C1/Wi1PGXkNODO9/XaDl4ajeJQGeft8bJzWr6nunqW/X6I5R" +
                "lJ8py081tJ9dxTfhZ4vznselbxfnLzWnXXI5fv8r7Pex/xXvNK" +
                "CdekK+VzUBbZ7IrKM/X70v5y5ufof1/KPz8rOd5Xtzw7V3Mf76" +
                "tZ6tn5azvVc3mquvl5+mq53+crqed0dfXM9n+ZvMe7txCu9Wgf" +
                "taEER2qcSGLkAtMueNEvB4ndgsSpHrElcpDVOQfJY5Z63DMwLI" +
                "YYYYsr9pxkBKwJXsuG+U9s47jrwnPdFkly9aujbSXz86GdOj/T" +
                "6+m/UlQ9lx8dleu7K6dtff58bJTnZ/uDrc5PuRT5/3eznoU9tX" +
                "l7gP5dx73u3kzfl/7ZfD0HfZ/3ZjyD0ZvJNj8RZ+KDAzbe4c1P" +
                "l2/z/Jkryv8Buqto2A==");
            
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
            final int compressedBytes = 1866;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW01oJEUU7iSHhUU0i0IC/iCZgU0wieCCFz1sd/X474bkog" +
                "d/LgsqC/4EzEXc7PZsJpvsHAVvuqDgaW97EG9e/UXU20Jcd485" +
                "mByFgFPz+uW9Vz89PbPd08lgDd1V9X6+972arqruniQIkloQJP" +
                "XkwWQ+SMvlj5NHoBX+HuQoycNBHyV5PCikJFM99HNuuS+n5NGC" +
                "eGWMZ06EjPFMnnSNp3qtuvH0xZbjefWgI3miT0ZP9RrP9jOjd3" +
                "1eu1nd9dl+tpzr83iM52DXZ2cGBLxWnfHEvhJ6s23KVA8Z6ZR7" +
                "Jh5qVSZv5cDNQvS1gow4dgyVEz9zP3pj9K5PX07y+rzyeRnrZz" +
                "g3guM5V+58z1w/w9Ebz6tflHt92gXXz7yleSc4RqX9WLn45dx/" +
                "hheyrs/W6xXO9wtlXp+qpVpwhrq7V7WwTxJuQx/QNe+gL7eTaF" +
                "zeuUZqHINrsOYs7KiE7OJFHpw/StozZsYcgRiYkWU8zpnbq121" +
                "2+2lNbZJjmeUkRZ1nfHcRRs6OBqhprLvbETrXkTEwZ7J0+Z1GE" +
                "NwZBIjtkTg8flhjoPNOdXdVXe7vbTGNsmjG/qM0ugGadFCHuSl" +
                "C9gTKsgIhZCt8RRxyB4tEVmikN7kiBLtx71kZhhd99CSRsfNBO" +
                "PpIz4TnwkCfYZaa6CH7eg2yMmGPmBhHlwDfUJN2d7mGFyDNWdh" +
                "R9UHYLh4kQdxICbaT2YMbcIjJhyXEGX+xBd9mw8Y6+oqtrbuD0" +
                "auNE8OO+Llb3kv+qv4CEVgDorh8ysqzyre17VfrO5+aXOquuej" +
                "0sbz3P/j2d94Ru8e1edNZFbe+zpr/Two+3mz/UKFz5vP57Haer" +
                "rA/Qjff55Sp4Jge7L4nDRuVcUXO7le2hP9aunX58sVXp8vDTsi" +
                "jWfUHnQ8sz3br1Y4nq+UHSHa43W0txVjX0u2J1HPbf3+fhnpXF" +
                "KK79Nzu2ivecvFwc3M3ZJtOd8hRq8ILtTM98kj+HvHtYeGvb83" +
                "Z0ufc29XN983p4YTJ/wbz9Byae12L7z+dWiRJwq3ksz93qjJxv" +
                "fFJ28fTubvcX+YeK3fBpvvG+/45vvG+eHOdzsn13zfPFfK75uz" +
                "RY1n6z7feLZODnk8Z/OMZ+tmcetn8m9651tTNfN+Xkty3TXX/P" +
                "aAe69F7u993M/X8t7PF8Gy+6z1JrbiH4KRK8PPaeut9PuZVtMl" +
                "PW9OV/i8OT2c583wMzxDy6W1273w+tehRZ4o3Eoy93ujJhvfF5" +
                "+8fTjFva8L14d3Px+uD7wfrefZjwb9/X3r/L2/Dzlk+unwZu3g" +
                "scpnqcZ5rcYP39eNa8n2JOq5rd/fLyOdS0rxfXpup8bl/p6F6G" +
                "vJtlw/IUavCG7UaJ/X0T6OZ7SPEttWvBXYt3UuGelcUorv03M7" +
                "0yIL0dfyebljZGXOW4l1l1Xk34PZ62f3+f3De59VV77sof/a8+" +
                "7gg1zr51f9r5/e+6X3ixtPT06rQWWl/dGwI4bfHw2MKrMblH88" +
                "H8/DGWqU6Y9aUAvxvL6fJxstoQ9YmwfYQQ/sCbW7di9wFK5BBp" +
                "wFR+WS5i2Tu+1BGVGuOrbMGNrJde6ha27JEWX+xJezFyvJP+me" +
                "VVd1a0er57xnqPvtXbhDfD6qF2+ZXWg/Cv8cvef3snPKfF93up" +
                "/no3z7e+X/j3A6z/7elRybv7epcjy3Pil3PDvrxASv1cTh89EE" +
                "StiaMuFYZyZsnUtGOpeU4vv03M60yEL0tXxe7hhZmVMrXAvX4A" +
                "w1yqAf/yQlZMutpT8/KALZkFQiGrNyTcahvgtDWvBoqCEmOicX" +
                "No0FosvIMh6PJO2z5nv8y+jNd19Ocr5vfFPc7x3xuN3qt0TvHd" +
                "X9vWxmVexH8YkKr88TZe5H6kBZf50IMn00ItBiH22hTZ4uvURz" +
                "rvkHGIlsXJ54xo+LLWfh5ocFc+Js/RxdCG4ZxIzH4rHOdzaGdf" +
                "cbHIOPWlSLKEGpltCHdPwAO+iBPaF2Iy9yFK5BBpwFR7V5cu62" +
                "B1mSPeRkY0sPXXNLrpf5E199NJYby53vbBnq9Btcxn78I0hRQr" +
                "bQgh62sMf7XCZjkIQ0NgeOQhLTRlrwaByX52RjE1POkOcrI5qc" +
                "U9uVxkqnXoE61axQH88k4y3oYSu8xL2khR2DJGY0OyLvg014yZ" +
                "RJPfflWWhPGVvyYONpRJasbM6p7VJjqVMvQZ1qlqiPZ5LxFvSw" +
                "FV7kXtLCjkESM5odkffBJrxoyqSe+/IstKeMLXmw8TQiS1Y2Z7" +
                "BVO2qns6bsYN1d33awH/+MEm5DH9Lxg2ugT6iHqzfD4BrbgqPa" +
                "PF28yIMsyR5ysrGlh5kP18v8ia8+zs6cnQkCfdY1FOjpfvwrSL" +
                "kNabmODpRxNEIlqYlo7p4yDvZMnjYv4iA5ogRysrGlh5mXOQ42" +
                "Z9A1FhudnVifdZ1euYvYbzwHUpSQLbSghy3s8T6XyRgkIY3Nga" +
                "OQxLSRFjwax+U52djElDPk+cqIJufU/j8r3HDH");
            
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
            final int compressedBytes = 1653;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrFuHFUU3U+IhKIUEAkJiIxAQkoBWFDYuzMtvxDXlttUqb" +
                "KAzTZgLCEkBDSOROHCTcrIRYpINJYj0SFoaFyaD0BiZp7vnHPu" +
                "fd5klwnawJvRe2/eveeec97Lzs5kk9GoPTbeas/R5dFeYcYx7j" +
                "mjPcd57jXmHYY1XA4PRvMFZz7DsRy/z2MXeLVJw845zm61J2ap" +
                "KSKh0HNGe47P05i7n7eUMYcHo/mCM5/hWI7f57ELvNqkYWfOWX" +
                "1an6beRpu1V+21oRhjV/l6yzACGNPVKlaLijzPYRTBat5B1I6r" +
                "Z4e6Xn+y54TYWN9Yb/Z4PY2XO76OufWI8VWa2ZXNeM4x1UDEq0" +
                "VFnucwimA1v4qorT7o8+mUvaL3DDV/TP+wq9kXo3IsdEyOJ8ep" +
                "T6PF0hwRxuBEjpuv1Troasbi7Mxjos+cL/hg/xbxq1EGONAs53" +
                "X98MvuJXN05e4fPeef0j9mGOCTcrQqn9nZl/3VV+UOHmA/9/ur" +
                "r8tuLHhXPJ48Tn0aLZbmiDAGJ3LcfK1qQDeXiQhmjT5zvlDB/i" +
                "3iV6MMcOCVVU9XA3x9Up80700nGG3WXrXXqUeMr7TCZpZhBDCX" +
                "b2uuitWiIs9zGEWwmncQtePq2aGu15/sOSEm55PzZl/Pbex2+t" +
                "zmiDAGJ3LcfC2z9n+axMGZiGDW6DPnCxXsn53oipkBDryy6ulq" +
                "gJ9+lHn//GCZb47px/3V+//mN9b0w5V/Hn1TnitDHp//WfZg0M" +
                "/nd2UPBt3P78seLHts3t68nfo0WizNfQRYRms9NygAg6gyRl/K" +
                "yhGPUQSrWSY68dzYC2NXZdXzOwT89I2mvTl9Zfqu+bx/d/raVf" +
                "u/+zTzhH11ztP3vRB5faDn+o1n5N9ekO8mz/Y+aSLvLMjQ7exi" +
                "+5nl+Q/u5+7D5fZzcjG5SH0auzfTC5sjwhicyHHztczav/0SB2" +
                "ciglmjz5wvVLB/dqIrZgY48Mqqp6thfOaevlGeK8se9Z36TurT" +
                "aLE09xFgGa313KAADKLKGH0pK0c8RhGsZpnoxHNjL4xdlVXP71" +
                "CP3aq3mnHLxi6zZXMfAZbRWs/NasHR70VgdPu5pTqY5zgUwWqW" +
                "iU48t+XhW5VVz++QYavr1fXRqO3T2GbSzK6tBwYnctx8LbPaXj" +
                "AHZyKCWaPPnC9UsH92oitmBjjwyqqnqyHWs+qsGc9s7DJnNkeE" +
                "MTiR4+ZrmbVXJw7ORASzRp85X6hg/+xEV8wMcOCVVU9XQ6w71U" +
                "4z7tjYZXZsjghjcCLHzdcya69OHJyJCGaNPnO+UMH+2YmumBng" +
                "wCurnq6GWPeqvWbcs7HL7NkcEcbgRI6br2XWXp04OBMRzBp95n" +
                "yhgv2zE10xM8CBV1Y9XQ3j43H/r/Les/T70mF9mPp2RMzm6C2G" +
                "LOfQUMUKUcMz5pxBx2beZ/QFD+qRnXCVMrA+N+29E+DH18bXRq" +
                "O2T2ObSTO75giwjNZ6blYLDlOPjLqbmud5jkMRrGaZ6MRzWx6+" +
                "VVn1/A4ZtjqoDpr7/sDG7pvgwOaIMAYnctx8LbP23zbEwZmIYN" +
                "boM+cLFeyfneiKmQEOvLLq6WoYn/n989EL/fee6TA8n/6ymt+g" +
                "5fel3O9Ln31bfq9bjf3MvC/dLe895X4f9vPZRcr9vpL3++znct" +
                "cOeb/PHpXP58t1v89Oy/N91d7nV/MYPxk/SX0aLZbmiDAGJ3Lc" +
                "fC2zsq4yWtwjmDX6zPlCBftnJ7piZoADr6x6uhrGl/fPF3Ns/m" +
                "p9uspl4/Wz+BbPGeJ5VBilzq+utsx8/qv0UZ3nqffr/dSn0WJp" +
                "jghjcCLHzdcyK+sqo8U9glmjz5wvVLB/dqIrZgY48Mqqp6thfL" +
                "nfhzx2f1zw/4P9UN4/570vzX5fbD9nv5X9XPj9c86/x+0elnt6" +
                "3lE9qB6kPo0WS3NEGIMTOW6+lllZVxkt7hHMGn3mfKGC/bMTXT" +
                "EzwIFXVj1dDeNf1t+Xdn8qf3//P+xnxulx+R4c9B2q7OfSR32v" +
                "vpf6NFoszX0EWEZrPTcoAIOoMkZfysoRj1EEq1kmOvHc2AtjV2" +
                "XV8ztk2PHaeG00avs0dr+UrNncR4BltNZzs1pw9L/GBEb3u9ea" +
                "6mCe41AEq1kmOvHclodvVVY9v0OGrbar7eY5v21j9+TftjkijM" +
                "GJHDdfy6z92wVxcCYimDX6zPlCBftnJ7piZoADr6x6uhrC/w2o" +
                "LAb6");
            
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
            final int compressedBytes = 1508;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW02LHFUULT+2E4SMisRBYRRRQySdTcBAsLpbDYpIVCYOUS" +
                "ITQWFWQn5ANU1PT6+yc+li9q70R/gvshMVIYLRCC7srte3zzn3" +
                "Vc9M9VTPBOe94r1X795zzz3vdlV31cB0h91hlnWH3emcZbayc7" +
                "YAy2iN526x4MgyWJUxk6Z+XldxKIKzmSdW4rnND92aWfP5Chk2" +
                "X81Xs2wyhnniCSs7ZwuwjNZ47hYLDqtFzKj1VD+vqzgUwdnMEy" +
                "vx3OaHbs2s+XyFDNu+176XZZMxzBNPWNm5jcDggI+7j2VWqwVz" +
                "sCdGMGuss0oXIlg/K9EdMwMU+MyaT3fD+O79zLXu/Ykt2DHCpj" +
                "7283reSvN4pliHotVWhToob+ea91exHqbFu5z04kmPK+4UZ8bj" +
                "2fL86XJ8ZtzPZdnu31PEy5Nx98Es4rVxPw+G3b+E73K1nuLt4p" +
                "3i3fLsvUr/Z8XntNoqviq+Lh4rHneoleIpWj1H5y/sX4/dh1Pc" +
                "leJqkdevZ3FTVrfH/ct8PV8f3/frNpffBOu29hZgGa3x3C0WHL" +
                "PvvojRfX+uax6sqzgUwdnMEyvx3OaHbs2s+XyFgO9N+ku9s73z" +
                "uD57z8/9TP+Nbb1z8z/D3huR5cUse+thduTWe/YA/6vV9nm5e2" +
                "uR5fWailrlWKuelTy169lEW7Sec/FSz9Ex1XP0xOmoZ7o+m63n" +
                "4IdF6xm3/JX4rG5bPHLZbbnK+rf6H/dv9K+LbfPovN3VuRlv7u" +
                "etofyTA/yf1lVWH1URt9JdCWOYzRbW3gKsIjieccgAO/Iqo9mr" +
                "/D6rV6UI3sF8JfMUsxLP5Q9VPOl5Kx/f9ZMxzOUd0bK1twDLaI" +
                "3nbrHgmN11EaO7K1uaB+sqDkVwNvPESjy3+aFbM2s+XyHDdvY6" +
                "e+N3sD2byzeyPVvDwhgc8HH3scw6e+sjDvbECGaNdVbpQgTrZy" +
                "W6Y2aAAp9Z8+luGF/v9324eTp+34c3Fvt9H/wz+GXwc6jnYPrW" +
                "Pfit3vPSfm0QvY0Mfm+I+dcD/H8uzPzHdH7Q0BPFVnzW4PPK1s" +
                "nyLGNP9Fn8b6/PnZ9O4vpM9Vz+/T74Ypl3xOjM8bwHDW49Km9k" +
                "vW+y1BZs7bvtu2EMs9nCGhbG4ICPu4/VOORVj9lZmcfEOqt0QQ" +
                "frN4vfjTJAgXrZr/uHXlbPrbiTrrNGr9nv6nuaZtg/4jAs9TM1" +
                "8hR3Ib8QxjCbLay9BVhGazx3ZAAGVmWMdSkrWzxGEZzNPLESz4" +
                "1aGLtm1ny+QsCnvydXvW+WlgXeN/ONfCOMYS4rvWFrbwGW0RrP" +
                "3WLBMbu2IkZ3fW5oHqyrOBTB2cwTK/HcqIWxa2bN5ysEfLo+m7" +
                "w+Uz2r69n/NtWzqXr2v1/8+mxvt7fDGObySWPb1rAwBgd83H2s" +
                "xk2fZohD7bPnne0YE+us0gUdrN8sfjfKAAXqZb/uH3pL36g9Gs" +
                "8jm0vPyNawMAYHfNx9rOaYZicO9sQIZo11VulCBOs3i9+NMkCB" +
                "z6z5dDeMT/d7+j1K9dR6tjdPrp7zcjdVz/T3kIb/2pnu9/T9me" +
                "p5auqZvj/T92e6Pk9zS/VcvHUudi6GMcxmC2tYGIMDPu4+llk5" +
                "rzKa3SOYNdZZpQsRrJ+V6I6ZAQp8Zs2nuyHWVmd810/GMJeelq" +
                "1hYQwO+Lj7WGadZScO9sQIZo11VulCBOtnJbpjZoACn1nz6W4Y" +
                "n36Plv38Obqa6tnk73v7x3kR8z374w4b12Q7OOcyVOVr+VoYw2" +
                "y2sPYWYBmt8dyRARhYlTHWpaxs8RhFcDbzxEo8N2ph7JpZ8/kK" +
                "6Z7T81Jj12c7b4cxzGYLa28BltEazx0ZgIFVGWNdysoWj1EEZz" +
                "NPrMRzoxbGrpk1n68Q8Cfx/8XpfTO1w7XRtdi289HROHeun956" +
                "di91L4UxzGYLa28BltEazx0ZgIFVGWNdysoWj1EEZzNPrMRzox" +
                "bGrpk1n6+Q7tlds++n+3bhb/U3K+r54eysxp3buzKL+uBYd3D5" +
                "kSrofyZOXvU=");
            
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
            final int compressedBytes = 1860;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7FuHFUUHfELFBSAZAUkhEBCotoSr2ekFJYLUsWubAm5tI" +
                "SVD8jEspERLUhAg4TFB8AH+BdIYbtIESuR8gNu3DE7z3fOOfe+" +
                "WXs3a5Q4M6P33rx7zz3nvLeznt0NFEU8Dh4Ur3UcfFu8s0c1qk" +
                "apT6PF0txHgGW01nODAjCIKmP0pawc8RhFsJplohPPjb0wdlVW" +
                "Pb9DwNefNO3T+v36S/P5+FH90SyvSf3hlNxXIbK0mDth/69rXH" +
                "0+G1/9cYh8MSPD10VRvihfpD6Nk3ia2bX1wOBEjpuvZVbTZg7O" +
                "RASzRp85X6hg/+xEV8wMcOCVVU9XA3y1W+029+muje2du2tzHw" +
                "GW0VrPzWrB0b1XA6N7v++qDuY5DkWwmmWiE89tefhWZdXzO9Rh" +
                "j6vjZjy2sc0c2xwRxni0NlTZNbMi6hnDX3bRsZly5HzBg3pkJ7" +
                "piZmB9br4yer7KXVQXzXhhY5u5sDkijMGJHDdfy6ydOnFwJiKY" +
                "NfrM+UIF+2cnumJmgAOvrHq6GmLdq/aacc/GNrNncx8BltFaz8" +
                "1qwdGpB0Z3d+6pDuY5DkWwmmWiE89tefhWZdXzO2TY8cPxw6KY" +
                "9GmcZNLMrjkCLKO1npvVgsP2IjLqfmqe5zkORbCaZaITz215+F" +
                "Zl1fM7ZNjypDxpnksnNrZPqhObI8IYnMhx87XM2j0NiYMzEcGs" +
                "0WfOFyrYPzvRFTMDHHhl1dPVMP5t/fxZf3Cbnz/3fpnz8+dZeZ" +
                "b6NLY7fWZzRBiDEzluvpZZu1eTODgTEcwafeZ8oYL9sxNdMTPA" +
                "gVdWPV0N8CuXK5d+n1MsxdEjZteozOWVLXdMMsY0rdJ6O3Nu2U" +
                "XeH1f4sd9jnqGPdeWy3Cl3mn3dsbHd6R2bI8IYnMhx87XM2r2a" +
                "xMGZiGDW6DPnCxXsn53oipkBDryy6ulqgB+vjleb59Kqje2Tat" +
                "XmPgIso7Wem9WCo3s2B0b3fF9VHcxzHIpgNctEJ57b8vCtyqrn" +
                "d0jX7P6uft85/mnep8X8lW/3Mb4/vp/6NFoszX0EWEZrPTcoAI" +
                "OoMkZfysoRj1EEq1kmOvHc2AtjV2XV8zvUYTfGG824YWOb2bC5" +
                "jwDLaK3nZrXg6PYiMLr93FAdzHMcimA1y0Qnntvy8K3Kqud3qM" +
                "Oujdeacc3GNrNmcx8BltFaz81qwdHtRWB0+7mmOpjnOBTBapaJ" +
                "Tjy35eFblVXP75CueXIsv7I+XSGmV3rdfyiqf5ZnW351nUpyqg" +
                "7zfvO+pvP36aP6Op7l59anq1w2Xk9x9Hy+nCFuosIodd5fbZnp" +
                "/H36qM7zHH43fN9c5L930M6fW5+u3Otynr+e8gqfz5czxE1UGK" +
                "XO+6stM52/Tx/VfTyz3Z/7/74b9+f+P/Pdn8v1cp36NLY7Xtvc" +
                "R4BltNZzs1pwdK9zYHT3Qa06mOc4FMFqlolOPDf2wthVWfX8Du" +
                "ma9XjyXjEcCzwOng57MOznm3s8fnSb7HV9p38PeTl+mfo0WizN" +
                "EWEMTuS4+VpmZV1ltLhHMGv0mfOFCvbPTnTFzAAHXln1dDXEej" +
                "o+bcZTG9vMqc0RYQxO5Lj5Wmbt1ImDMxHBrNFnzhcq2D870RUz" +
                "Axx4ZdXT1TD+/36/3+2jfFY+S30aLZbmiDAGJ3LcfC2zsq4yWt" +
                "wjmDX6zPlCBftnJ7piZoADr6x6uhrGD9/fF/n9fTwaj1KfxvYv" +
                "wcjmPgIso7Wem9WCo/trExjdc3KkOpjnOBTBapaJTjw39sLYVV" +
                "n1/A4Ztlwv15v7dN3G9s5dtzkijMGJHDdfy6zdu4M4OBMRzBp9" +
                "5nyhgv2zE10xM8CBV1Y9XQ3jh/f7It/v5Va5lfo0tju9ZXNEGI" +
                "MTOW6+llm7V5M4OBMRzBp95nyhgv2zE10xM8CBV1Y9XQ2xbpfb" +
                "zbhtY5vZtjkijMGJHDdfy6ydOnFwJiKYNfrM+UIF+2cnumJmgA" +
                "OvrHq6GuCrB9WDopj0Vfd/yaRZmluPGF+lmV3ZjOccUw1EvFpU" +
                "5HkOowhW86uI2uoD++OVvaL3DLXwd6D77+wPfxg+oc9zfPPZ5M" +
                "QsNUUkFHrOaM/xaRrXOWJcDg9G8wVnPsOxHL/PYxd4tUnDzput" +
                "pChW/pw9s2iG6RU3YZld6dZ+UcP7/cfhvfum//457OdwzPD70r" +
                "3ynl1pLM3RI6a5/rxXyGmnqpwLVFpv57QVsF/1xxV+7PeYZ8it" +
                "60pzqbz6/ld23wNTLM3RI6a5/jyzZT0umVLOBSqttzOyqAu7Un" +
                "9c4cd+j3mG3Lp4/Xoc/jy8bxd5HP52p1f3641Qvy9O8cnwKWmx" +
                "r+Afwx68xqf34ffPBf7+OeznYvez2qw2U5/G9penTZv7CLCM1n" +
                "puVguO7tewwKjeNM/zHIciWM0y0Ynnxl4Yuyqrnt+hDntUHTXj" +
                "URqvMkeYo7cYspxDQxXYwIqoZ/SvturYzPuMvuBBPbITrlIG1u" +
                "emvXfC+OH75q0/3/8e9mDu4z91kPHS");
            
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
            final int compressedBytes = 1438;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7FuHUUU3TotRQpAQoIGgUSUzl3i3emA+gkJyZLlH0CKlN" +
                "YrkgfmA+jdUFLSIIU0iDJIcZNfoLeLCIl9O757zrl31kkez3mO" +
                "PbOamTf3nnvuuZOd99aLaJr+46bpP+nf6z9vztvhg/6DZqb98E" +
                "209e83s63/Ilg+ajbS+tuv8H/6hnwfSp2LwfLZGzLcHce6n1vc" +
                "zyLPNdzP739edz9jO3zQ1Lb+v3O9Pzd6f25jP4/+qOe9tss470" +
                "d/Xo3zvvtk3ftz98lr/b7/83bO+9Ff9fvzap33vr/Opz09S8/y" +
                "mGez5bW3AKsIjmccMsCOvMpo9pLfZ/WqFMEVzCuZU8xKPJe/VH" +
                "Hu8bz/+Pu78Lz0+JfLPO+jZUPfn3U//89+FpT+Wp961v7+fJge" +
                "5jHPZstrbwGW0RrPHRmAgVUZoy5lZYvHKIKzmScq8dzYC2PXzJ" +
                "rP75Bhuxfdi6ZZjXleefLKPtsIDC74uPtYZrW9YA72RASzRp0l" +
                "XYhg/axEK2YGKPCZNZ9WA3z7vH3u79lsy3aMsKkv+o1TV7GtPD" +
                "mqjIk8iIgoVah5lV2rmM9fipm35ezdcXc87OuxzeNOH9saFsbg" +
                "go+7j2XW6V+TONgTEcwadZZ0IYL1sxKtmBmgwGfWfFoN41ft3t" +
                "3VZZ7Vp3vutyrbeGTPqz6Xc1zUDGu4Eh6MpgvKvIdtJX7vxy5w" +
                "tTmHXa9XSX0fUt9/Xu3nz/67epet29qX7cuyLdsxwmafEVnyX5" +
                "QBHmO6KNJGu+YqUNVeH0f4eV5jmWGOtczS/hY/vYN3ypa01+/P" +
                "y34fUvdz/f1s99v9POZ5PCn7toaFMbjg4+5jNe78NBKH2qfzuh" +
                "8xUWdJF3SwfrP4apQBCtTLfq0fekffsl0O89Lm0bO0NSyMwQUf" +
                "dx+rOc6zEwd7IoJZo86SLkSwfrP4apQBCnxmzafVMH4b5739dn" +
                "vnfS735T1/1r+P6u97/X2/nvuZTtJJHvM8vmk+sTUsjMEFH3cf" +
                "y6zTu3XiYE9EMGvUWdKFCNbPSrRiZoACn1nzaTWMr/dn/T2qv0" +
                "c35f7s7nR38pjnlT2v7LONwOCCj7uPZVbLzRzsiQhmjTpLuhDB" +
                "+lmJVswMUOAzaz6thlgX3WKYFzaPnoWtYWEMLvi4+1hmnbITB3" +
                "siglmjzpIuRLB+VqIVMwMU+MyaT6sh1r1ub5j3bB49e7aGhTG4" +
                "4OPuY5l1yk4c7IkIZo06S7oQwfpZiVbMDFDgM2s+rYZYD7qDYT" +
                "6wefQc2BoWxuCCj7uPZdYpO3GwJyKYNeos6UIE62clWjEzQIHP" +
                "rPm0GuDTTtoZnpt2bB6fpHZs7S3AMlrjuVssOKantcCo3+3q53" +
                "WJQxGczTxRiec2P3RrZs3nd0hr1vbT7frUs25LT9PTPObZbHkN" +
                "C2M8WjuiOAPigVDGkjLksZVylHRBg2pkJVoxM3B+7j4yaub63f" +
                "P8v/U+W7ft3tq9lcc8my2vvQVYRms8d2QABlZljLqUlS0eowjO" +
                "Zp6oxHNjL4xdM2s+v0Nac70/69+b9f/vqPdnbI//vhn35/Lrt/" +
                "N+/tFXN2M/H31Z/3vH1XifXM/7Js/7/cP7h3nM88qeV/aZLcAy" +
                "WuO5Wyw4LHdkVG3q53WJQxGczTxRiefGXhi7ZtZ8foeAr+d9k+" +
                "c9naWzPOZ5/Ev0zNawMAYXfNx9LLNOf+0SB3siglmjzpIuRLB+" +
                "VqIVMwMU+MyaT6sBvhta03SdzeObvM7WsDAGDT7uPpZZp7eFxM" +
                "GeiGDWqLOkCxGsn5VoxcwABT6z5tNqiDV1aZiTzaMn2RoWxuCC" +
                "j7uPZdYpO3GwJyKYNeos6UIE62clWjEzQIHPrPm0GuDTaTod7t" +
                "NTm8c799TWsDAGF3zcfSyzTqeDONgTEcwadZZ0IYL1sxKtmBmg" +
                "wGfWfFoN4f8DmelWPw==");
            
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
            final int compressedBytes = 1149;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7GOFEcQvQ8gI3CAQT7hxLKlkxAGBEayNDuZjRD+gUtPJJ" +
                "eQEMEKcYjJkBAZGQ7tb3GyP8AHkF7oY+tq33vVPcPO3t6x3NWM" +
                "pnv6VdWr1309Mz2j262tcpt+9LPu163cRm3NrJnVMcNRAlNbaX" +
                "dObdVzW1Tdp+RBRK0HamFOZdde9OevxfRjURdZPvSyfljyr3Ri" +
                "huGIZVjGZzq1Ofv3eMu6GYYjlmEZn+m0tudP8j6Y47kZ2+T65L" +
                "qfKWZtlMDU1m+PGWq5LaqmApFe+j7UA9ar+jgi1v0a6wy1fh3n" +
                "3J5sH7e3F5Y5Zm2UwNTWb2e2qsZtz1RTgUgvfS9ZVIWfqT6OiH" +
                "W/xjpDrV+Wc3qvsv68vcpMn/62OLt1llfY9M6m3wO6+3kfzPHc" +
                "4PH8PcdgrePZ5hic4H5+9Jyf/ji9PP0F68/p933eL/+rMFwZYN" +
                "8pkB/WpPu7L9h/Gsl3lVsHD46Qn0cy3OiZn3/mLFt5Pf948thK" +
                "qx2zNhD2wQ4bHzGWWTmvMjoePZi11FnThQjWz0q0x8wABTGz5t" +
                "PesH++b5768+hRjsFZPY+qDOfweTRH1vQ8yus95+fmzM8cz7ze" +
                "z8/1/urZxZifL97l/NzU7eW/OQarbu3T9qmVVjtm7YjAl701ng" +
                "9kgA9QZSx1KSsj0Uc9OJtbSiWRG2Ph7JpZ88URct/J3mTv6L1z" +
                "z+v5m+iet4GwD3bY+IixzLp42yUOtpQezFrqrOlCBOtnJdpjZo" +
                "CCmFnzaW/YP9dLuf78ttaf7afz8GR4du1bWH92+zk/c/15dluz" +
                "3+xbabVj1gbCPthh4yPGahzyqsVxVhZ9Sp01XdDB+h2JvVEGKF" +
                "Ar27X/0Du3dU13VHdezy2dt4GwD3bY+IixmuM4O3GwpfRg1lJn" +
                "TRciWL8jsTfKAAUxs+bT3rB/Pt9zvZTjmeN5scdz9e91OZ5xPF" +
                "/8s/r8bGftzEqr5+9HM28DYR/ssPERY5l18Q5GHGwpPZi11FnT" +
                "hQjWz0q0x8wABTGz5tPesH+u57/m+2Y+j4av92a32bXS6vnKdN" +
                "fbQNgHO2x8xFiNO179Eofii/XxbulT6qzpgg7W70jsjTJAgVrZ" +
                "rv2HXjtyfuZ6KddLF2W9NLk0uRRxwwxHCUxt/XZmq22fLRZV93" +
                "HUS9/rXqpA82pMVNmXvxbTj3n2nJ+n/T25ed/7tfT9kl9VT8ww" +
                "HLEMy/hMa/mefNgc1jHDUQLzc0TW7EMZYHGmoUgvfe/rgaqO+j" +
                "gi1v0a6wx9rM1h96b0O/jrZH+jgwv8/83tTrtjpdWOWTsi8GVv" +
                "jecDGeADVBlLXcrKSPRRD87mllJJ5MZYOLtm1nxxhNy39nvY1w" +
                "/9rHs74m68+D3s6wdn+r68Ub+HbW+2N6202jFrRwS+7K3xfCAD" +
                "fIAqY6lLWRmJPurB2dxSKoncGAtn18yaL46Q9jnMzz/yK9HKV8" +
                "vg9T6KKa/3z7P0bnvXSqsds3ZE4MveGs8HMsAHqDKWupSVkeij" +
                "HpzNLaWSyI2xcHbNrPniCC38/wcElD7L");
            
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
            final int compressedBytes = 1209;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWT2PFEcQncgEhA4IMBKSnSCQLFm60MHt7P0H5AQtOSEi9g" +
                "nByUdOQEREhnM7QELiD1g+HSQQ3GX8g4tgtrbuvdfVvbe7zIkT" +
                "VI+6e7rq1avXpfnS7vR4etx10+PpYu46X/m5j8DggI97Gcus3a" +
                "IxB3siglmjzpouRLB+VqI7ZgYoKDNrPt0N42Pbf9ZlG7H9eT9r" +
                "8OVt+4OPdlbzxvOz+Nb3OWKVLIxS5e1o9yznb+VH9Go8Q3v8X1" +
                "5deb9nPb/Ntvvz5/7L7o+7t1DP3Z/WYri6xPdrsFwfSfeVM/w3" +
                "1uS7Fiw312T47fMX02w6s9Hm+ZfUzNewMAYHfNzLWGY9/VojDv" +
                "ZEBLNGnTVdiGD9rER3zAxQUGbWfLob4Pu3/duyzmYzO0bY1Bf9" +
                "zqmr2AaPRdUxkQcREaUKNa+y6y7a+WsxbdtiJ4f9YcDNbWbHCJ" +
                "v6ot85dVXRc+iZ6pjIg4iIUoWaV9l1F+38tZi2rdRFnhdN1her" +
                "PUm+nGF5xCos62c6r7b/T76lR63nv1mD/F66GN9LtfbX87zKxm" +
                "yP/s4a5P0+7v3+8Omm93vWc8x6Tt5M3tho82C3lZ/7CAwO+LiX" +
                "sczquZmDPRHBrFFnTRciWD8r0R0zAxSUmTWf7obx612f+/9fjO" +
                "tz8mrT63PyapXrc+/jeO/3/L0u30cX5/sz63ne3/P5/bl523mw" +
                "88BGm91m69ICLKM1njsyAAOrMkZdysqWEqMIzuaeqKTkRi2cXT" +
                "NrvrJCwK93v+/98X3c73u38/mZ76OsZ9Yz65n1zHrm70tZz1Y9" +
                "H77c/Prs7/Z3bbR5sNvKz30EBgd83MtYjbPczKF2V6d5ypUjar" +
                "qgg/W7pdyNMkCBetmv+4deVp+/L43T+nv9PRttdputYWEMDvi4" +
                "l7Eah7zqcTsrKzFRZ00XdLB+t5S7UQYoUC/7df/Qaz2fn/l+z/" +
                "f7d/N+n/UzG22ePwlmvoaFMTjg417GatziaUMcaj99Hs0iJuqs" +
                "6YIO1u+WcjfKAAXqZb/uH3rnvoP+ILyj5jazY4RNfdHvnLqqvA" +
                "sPPFMdE3kQEVGqUPMqu+6inb8W07YtdnLSnwTc3GZ2jLD5OSJr" +
                "fmWrajzxTMDUIn30o6aWVdT1cUQ5tzXWGVqs/cn0zvRO1w2jzY" +
                "PHVn7uIzA44ONexjKrZ2cO9kQEs0adNV2IYP2sRHfMDFBQZtZ8" +
                "uhvgd7Z2trpuGG2e/3K/5evSAiyjNZ67x4Lj9L+KwFj837Gleb" +
                "CucSiCs7knKim53Q/dmlnzlRVy7OTy5HLXDaPNg8dWfs4WYBmt" +
                "8dw9Fhxei8io9VQ/r2sciuBs7olKSm73Q7dm1nxlhYDP76Vxvz" +
                "+9bR/5aGfa2FbzL49Yx+eIVbIwSpW3o92znL+VH9Fn8Wy/99HO" +
                "at54vkTR+818jlglC6NUeTvaPcv5W/kRXeeZHE2ObLTZbbaGhT" +
                "E44ONexjIr51VGt5cIZo06a7oQwfpZie6YGaCgzKz5dDeMz9/r" +
                "zrtlPcdt268vBsfX3N2m+vt3/bu6zewYYVNf9Dunruq5LaqOiT" +
                "yIqO1APcyp7LqLdv5aTNtW6kLb/z3v0TFb1nPc9uSHrMGo9byU" +
                "Ndi4fQJdPW7c");
            
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
            final int compressedBytes = 1625;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWctuHUUQ9T+wYAFESLBBIBEbyUICwWIymywQHxFb8caWbe" +
                "VKXpFYCNnIXvMDUbzkH+wPyi8w7p4z55yqdoKjK4iivqPuulWn" +
                "6tTpvo+5M3djIz/++HujP97xMb4cX9b51jIGnzNiRBXjYJV2yD" +
                "0iY0sZ+8CLOrMuanCNqkSrnEH76/A5KhHWq/FqslfVzsgVfc6I" +
                "EVWMg1VkIyujkTHtp/WBF3VmXdTgGlWJVjmD9tfhc1Si+adfTO" +
                "PL049OvwHy4tnpp/d5j59+8gbs2xT5fD2frNOP34J/dU++z1Lk" +
                "63sybJW572djP3//q+9nf3/2/eyf9zfv53gyntS52nKmOoEfI8" +
                "zVbK/XgVpyLGfDxBjO7ifeh36LwzO0G5CsJHJzL8Dunb1f3CHJ" +
                "f51+qby+jdU4Z8YcU1z9uzzvE5myDs/2WCsLz54/uKtvzG2x/q" +
                "vf7mmVd63jQ7hSef7g/+nbvz/XeT4angxP6lztbbx6eI6ZOTyI" +
                "6Yi1Xld7K4fHoc77RA8ZLV3UofoRiatxBipwVHFfP/UW7GA4mO" +
                "wBbEEO4DOiOTyI6Yi1Xjd3Fw6PL/oOck7W2dJFHaofkbgaZ6AC" +
                "RxX39VNvwfaGvcnuwRZkDz4jmsODmI5Y63Vzd+Hw+KJvL+dknS" +
                "1d1KH6EYmrcQYqcFRxXz/1Fux8OJ/sOWxBzuEzojk8iOmItd5j" +
                "7i4ciuQMZc06W7pYofoRiatxBiqInb2fr0ZY94f9ye7DFmQfPi" +
                "Oaw4OYjljrdXN34fD4om8/52SdLV3UofoRiatxBipwVHFfP/UW" +
                "bHfYnewubEF24TOiOTyI6Yi1Xjd3Fw6PL/p2c07W2dJFHaofkb" +
                "gaZ6ACRxX39VNvwc6Gs8mewRbkDD4jmsODmI5Y6z3m7sKhSM5Q" +
                "1qyzpYsVqh+RuBpnoILY2fv5aph/+kP+HfXnr3h28d09fn39uN" +
                "T/8p/+fv7+vfq/4+H4sM7VIlb9GGGuZnu9DnZgDqPOmHU5q0Zi" +
                "jmdoNyBZSeTmXoDdO3u/uENL7va4Pdlt2IJsw48R5mq21+tALT" +
                "mWvUiMYT+3vQ/9FodnaDcgWUnkBk7d3tn7xR1acq/H68lewxbk" +
                "Gj4jmhOzfbAKz5WV0ciYPjnWB55ztHRRg2tUJb5iZdD+OmJl1o" +
                "zcfv2+3v878uPip/4/+jofFz/3PXjn8/ur8VWdby1j8DkjRlQx" +
                "DlZph9wjMraUsQ+8qDProgbXqEq0yhm0vw6foxLNb7w/x/4+W+" +
                "fjxbO+B/3+fP+/+EPcz3E1rupcbflmXcGPEeZqttfrQC05lm/v" +
                "xBjORivvQ7/F4RnaDUhWErm5F2D3zt4v7hByHz1+9Hhj43au9h" +
                "apHp5jZg4PYjpirbJiL5RDkZyhrFlnSxcrVL8q8RUrAxXEzt7P" +
                "V6P5/fO+1v83D4fDOldb7uQdwmdEc3gQ0xFrvW6+WygcHl/uJx" +
                "7mnKyzpYs6VD8icTXOQAWOKu7rp96C7Qw7k92BLcgOfEY0hwcx" +
                "HbHW6+buwuHxRd9Ozsk6W7qoQ/UjElfjDFTgqOK+fuot2GqYvk" +
                "dv52oLsoLPiObwIKYj1nrd3F04PL7oW+WcrLOlizpUPyJxNc5A" +
                "BY4q7uun3oJdDpeTvYQtyCV8RjSHBzEdsdZ7zN2FQ5GcoaxZZ0" +
                "sXK1Q/InE1zkAFsbP389Vofj8frfV8dDwc17nastPH8BnRHB7E" +
                "dMRar5tfTeHw+PJ6H+ecrLOlizpUPyJxNc5ABY4q7uun3oJdDB" +
                "eTvYAtyAV8RjSHBzEdsdZ7zN2FQ5GcoaxZZ0sXK1Q/InE1zkAF" +
                "sbP389Vofv+8r/V6c3PcrHO15cppE36MMFezvV4HasmxXCsmxn" +
                "C9uel96Lc4PEO7AclKIjf3Auze2fvFHVpyb8abyd7AFuQGPiOa" +
                "E7N9sArPlZXRyJjuJ1sfeM7R0kUNrlGV+IqVQfvriJVZs64/3E" +
                "/e7Xcx+/3k9/j/uKd9D/r9+X5/vu9n38+3Xm8eDUd1rrb80j+C" +
                "z4jm8CCmI9Z63Xw1IRweX643jnJO1tnSRR2qH5G4GmegAkcV9/" +
                "VTbx39/dk/730/+372/Xyn+yFb4zxXW65Et+DHCHM12+t1oJYc" +
                "y9VuYgxX71veh36LwzO027jlCu7m5l6A3Tt7v7hDvuZwffRbv8" +
                "rp10fvyffnP+07LP4=");
            
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
            final int rows = 16;
            final int cols = 84;
            final int compressedBytes = 741;
            final int uncompressedBytes = 5377;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtlUGK20AQRXWHLLJIAoFkExII5ApCZ7EZb23sbXQae+xDjt" +
                "yt0vu/WiQYvByL6pqu3/X7tSxrum781nXj9/HD+KubP38P4+fu" +
                "gc/46R/a76bytXvKZ/z4H/3Hg35fmsrPBx3+lPH9fj7xfvYv/U" +
                "sda77X6yz+jpE1XGgaudf76t7q4fWg833yLFasccGh/FHJp3EH" +
                "CFxV3c8Pb4335/Opz+em39Sx5nKnNzGnomu40DRyr/fN36Z4eH" +
                "35vjftmpZzjQsO5Y9KPo07QOCq6n5+eO8xvA6vXXcf77l+6qzO" +
                "GaOGqhpBF264Us2O+dv2fWKWOVsuGJxRSbTLHXR/DR8zibieh/" +
                "OUzzXPypk5Y9RQVSPowg1XqtmxuZ+2T8wyZ8sFgzMqiXa5g+6v" +
                "4WMmYX1/7I/Tc3qMXJ7cY8yp6BouNI3c633zr0M8vL78fo7tmp" +
                "ZzjQsO5Y9KPo07QOCq6n5+eIu263dT3kUuyi7mVHQNF5pG7vW+" +
                "eXfx8PrCt2vXtJxrXHAof1TyadwBAldV9/PDW7R9v5/yPnJR9j" +
                "Gnomu40DRyr/fNu4uH1xe+fbum5VzjgkP5o5JP4w4QuKq6nx/e" +
                "om377ZS3kYuyjTkVXcOFppF7vW/eXTy8vvBt2zUt5xoXHMoflX" +
                "wad4DAVdX9/PDeY7gNt+k9eqt5frPemDNGDVU1gi7ccKWaHZv/" +
                "R7ZPzDJnywWDMyqJdrmD7q/hYyYR18twmfKl5lm5MGeMGqpqBF" +
                "244Uo1Ozb30/aJWeZsuWBwRiXRLnfQ/TV8zCTieh2uU77WPCtX" +
                "5oxRQ1WNoAs3XKlmx+Z+2j4xy5wtFwzOqCTa5Q66v4aPmYT1/a" +
                "k/Tb/7U+TyJjjFnIqu4ULTyL3eN79txMPry/vo1K5pOde44FD+" +
                "qOTTuAMErqru54e3aIf+MOVD5KIcYk5F13ChaeRe75t3Fw+vL3" +
                "yHdk3LucYFh/JHJZ/GHSBwVXU/P7wl3gCdeu/w");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 0, 34, 0, 0, 0, 0, 0, 0, 35, 4, 0, 36, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 6, 0, 0, 0, 38, 39, 7, 0, 40, 8, 0, 0, 0, 41, 42, 0, 43, 0, 0, 44, 0, 9, 0, 45, 0, 10, 46, 11, 0, 47, 0, 0, 0, 48, 49, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 11, 0, 0, 0, 0, 0, 0, 0, 51, 1, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 14, 15, 0, 0, 0, 52, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 0, 1, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 53, 0, 0, 0, 20, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 55, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 56, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 57, 0, 58, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 59, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 60, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 11, 0, 2, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 61, 14, 0, 62, 0, 0, 0, 63, 0, 0, 0, 64, 65, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 14, 0, 0, 66, 15, 0, 0, 16, 0, 0, 67, 17, 0, 0, 0, 0, 0, 24, 25, 1, 0, 26, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 28, 0, 1, 0, 0, 0, 3, 4, 0, 0, 0, 29, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 32, 0, 2, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 34, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 35, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 38, 16, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 1, 0, 0, 0, 1, 6, 0, 0, 5, 42, 7, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 68, 44, 0, 45, 0, 46, 47, 48, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 53, 0, 1, 0, 54, 0, 0, 8, 55, 0, 56, 0, 57, 0, 0, 0, 6, 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 58, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 3, 0, 8, 59, 60, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 61, 0, 0, 0, 0, 69, 0, 0, 0, 0, 62, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 65, 17, 18, 0, 19, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 66, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 67, 25, 26, 0, 0, 0, 68, 69, 0, 0, 0, 4, 0, 70, 0, 0, 0, 70, 5, 71, 1, 0, 0, 0, 27, 72, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 1, 0, 71, 0, 0, 0, 0, 0, 0, 72, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 19, 0, 0, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 11, 0, 73, 74, 12, 0, 73, 75, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 76, 0, 13, 77, 78, 79, 80, 0, 81, 74, 82, 1, 83, 0, 75, 84, 85, 86, 76, 14, 2, 15, 0, 0, 0, 87, 0, 0, 0, 0, 88, 0, 89, 0, 90, 91, 0, 92, 93, 9, 0, 0, 2, 0, 94, 0, 0, 95, 1, 96, 0, 3, 0, 0, 0, 0, 0, 97, 0, 2, 0, 0, 0, 0, 0, 0, 98, 99, 0, 0, 0, 0, 0, 0, 0, 100, 101, 0, 3, 4, 0, 0, 0, 102, 1, 103, 0, 0, 0, 104, 105, 5, 0, 0, 0, 0, 0, 0, 0, 10, 0, 106, 4, 1, 0, 0, 0, 0, 1, 107, 108, 0, 0, 4, 109, 0, 6, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 111, 0, 0, 0, 0, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 5, 16, 0, 17, 112, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 0, 0, 113, 7, 0, 114, 115, 0, 11, 0, 0, 0, 12, 0, 116, 0, 0, 20, 0, 2, 0, 0, 6, 0, 0, 0, 4, 0, 117, 118, 0, 5, 0, 0, 0, 0, 0, 119, 0, 0, 0, 120, 121, 122, 0, 7, 0, 123, 0, 8, 13, 0, 0, 2, 0, 124, 0, 3, 2, 125, 0, 14, 0, 126, 0, 0, 0, 15, 9, 0, 0, 0, 0, 77, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 127, 128, 0, 129, 130, 131, 132, 0, 0, 0, 0, 133, 0, 0, 23, 24, 25, 26, 27, 28, 29, 134, 30, 78, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 135, 46, 47, 48, 49, 136, 50, 51, 52, 55, 56, 57, 0, 0, 1, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 137, 138, 139, 0, 140, 0, 59, 4, 79, 0, 141, 7, 0, 0, 142, 143, 0, 0, 10, 60, 144, 145, 146, 147, 80, 148, 0, 149, 150, 151, 152, 153, 154, 155, 61, 156, 0, 157, 158, 159, 160, 0, 0, 7, 0, 0, 0, 0, 0, 62, 0, 0, 0, 161, 0, 162, 0, 0, 0, 0, 1, 0, 2, 163, 164, 0, 0, 165, 166, 11, 0, 0, 0, 167, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 168, 1, 169, 0, 170, 0, 8, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 171, 172, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 82, 0, 12, 0, 0, 0, 173, 2, 0, 3, 0, 0, 0, 13, 0, 174, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 175, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 0, 2, 179, 180, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 181, 0, 182, 183, 0, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 184, 0, 0, 0, 0, 0, 0, 16, 9, 10, 0, 11, 0, 12, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 22, 0, 0, 0, 0, 23, 188, 24, 17, 0, 0, 0, 0, 0, 0, 189, 0, 0, 1, 0, 0, 18, 190, 0, 3, 0, 0, 7, 15, 1, 0, 0, 0, 1, 0, 191, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, 0, 0, 192, 0, 193, 0, 194, 19, 0, 0, 195, 0, 196, 0, 0, 20, 0, 0, 0, 84, 0, 26, 0, 197, 0, 0, 0, 0, 0, 198, 21, 0, 0, 0, 0, 18, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 199, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 0, 0, 1, 200, 201, 2, 3, 0, 0, 0, 0, 0, 0, 202, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 203, 0, 0, 0, 204, 64, 0, 205, 0, 0, 3, 0, 0, 65, 86, 0, 0, 23, 0, 0, 0, 27, 206, 0, 207, 28, 24, 0, 208, 209, 0, 25, 210, 0, 0, 211, 212, 213, 214, 29, 215, 26, 216, 217, 218, 27, 219, 0, 220, 221, 6, 222, 223, 30, 0, 224, 225, 0, 0, 0, 0, 0, 66, 0, 2, 226, 0, 0, 227, 0, 228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 229, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 29, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 230, 4, 0, 0, 0, 0, 0, 0, 30, 0, 0, 1, 231, 232, 0, 1, 31, 0, 0, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 233, 69, 0, 0, 234, 0, 0, 235, 236, 0, 0, 0, 0, 32, 33, 0, 0, 3, 0, 0, 237, 0, 238, 0, 87, 239, 0, 240, 0, 0, 34, 0, 0, 0, 241, 0, 242, 35, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 243, 0, 244, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 7, 0, 0, 0, 0, 39, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 245, 37, 246, 247, 38, 248, 0, 249, 39, 250, 0, 40, 0, 251, 0, 40, 252, 41, 0, 0, 0, 0, 0, 253, 0, 254, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 256, 0, 0, 257, 0, 8, 0, 0, 42, 0, 258, 259, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 260, 0, 261, 262, 263, 264, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 265, 43, 10, 0, 0, 12, 0, 13, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 70, 0, 0, 266, 0, 0, 0, 267, 0, 0, 0, 0, 44, 0, 268, 269, 270, 0, 0, 45, 271, 0, 272, 46, 47, 0, 0, 8, 273, 0, 2, 274, 275, 0, 0, 0, 8, 48, 276, 0, 277, 49, 278, 0, 0, 50, 0, 3, 279, 280, 0, 281, 0, 0, 0, 0, 0, 0, 0, 51, 282, 283, 0, 0, 52, 0, 0, 284, 0, 0, 0, 285, 0, 0, 286, 1, 0, 0, 0, 5, 2, 0, 287, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 288, 44, 0, 0, 0, 0, 0, 71, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 289, 0, 0, 0, 0, 2, 0, 290, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 291, 45, 0, 0, 0, 292, 0, 0, 0, 0, 0, 0, 293, 0, 0, 0, 0, 0, 54, 0, 0, 55, 0, 294, 0, 0, 0, 0, 0, 0, 56, 0, 0, 36, 0, 0, 0, 37, 5, 295, 6, 296, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 297, 3, 298, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 299, 0, 300, 0, 301, 0, 0, 302, 0, 0, 0, 303, 0, 0, 57, 304, 0, 0, 0, 0, 0, 305, 0, 0, 7, 306, 0, 0, 0, 307, 308, 0, 46, 309, 0, 0, 0, 58, 88, 0, 0, 0, 310, 311, 59, 0, 60, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 89, 0, 0, 0, 2, 47, 61, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 312, 0, 48, 313, 49, 72, 0, 50, 0, 0, 0, 0, 314, 63, 0, 0, 315, 64, 65, 0, 51, 0, 316, 66, 317, 0, 52, 67, 318, 319, 68, 69, 0, 53, 0, 320, 321, 0, 70, 54, 322, 0, 55, 0, 0, 0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 323, 0, 9, 324, 0, 0, 325, 326, 327, 72, 0, 0, 0, 3, 0, 0, 328, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 329, 73, 0, 0, 0, 0, 74, 0, 0, 38, 0, 0, 0, 0, 0, 330, 59, 331, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 332, 6, 0, 0, 0, 28, 0, 0, 0, 333, 0, 0, 0, 0, 0, 334, 0, 61, 335, 62, 0, 63, 336, 337, 0, 0, 64, 338, 0, 65, 0, 0, 75, 0, 0, 339, 340, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 341, 342, 90, 0, 343, 0, 0, 344, 0, 0, 0, 77, 0, 0, 0, 0, 0, 66, 0, 78, 0, 345, 0, 79, 67, 346, 347, 348, 349, 0, 80, 81, 0, 350, 82, 68, 351, 0, 352, 353, 354, 83, 0, 0, 0, 0, 355, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 0, 0, 0, 0, 0, 69, 356, 0, 70, 0, 0, 0, 84, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 357, 0, 358, 85, 359, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 360, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 86, 73, 74, 361, 75, 0, 87, 88, 76, 0, 77, 362, 0, 363, 364, 0, 0, 365, 366, 0, 0, 0, 7, 0, 91, 89, 0, 0, 367, 0, 368, 0, 369, 370, 0, 90, 371, 372, 373, 374, 91, 92, 0, 0, 0, 375, 0, 0, 376, 377, 378, 93, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 379, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 380, 0, 381, 0, 0, 95, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 382, 383, 0, 0, 384, 385, 0, 386, 0, 0, 0, 0, 97, 98, 0, 0, 0, 92, 93, 0, 99, 100, 101, 387, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 388, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 389, 105, 0, 82, 106, 107, 0, 83, 390, 391, 0, 0, 0, 392, 0, 0, 108, 0, 0, 84, 0, 393, 0, 0, 85, 0, 394, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 395, 0, 0, 0, 396, 0, 87, 397, 0, 398, 0, 88, 0, 109, 110, 111, 0, 399, 0, 112, 400, 401, 0, 113, 402, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 115, 116, 0, 403, 0, 404, 0, 0, 117, 405, 0, 118, 119, 406, 0, 120, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 1, 1, 5, 6, 7, 8, 1, 5, 0, 2, 0, 3, 9, 1, 5, 5, 10, 0, 0, 10, 10, 11, 4, 12, 0, 13, 2, 10, 2, 6, 1, 0, 14, 15, 16, 12, 10, 17, 18, 16, 3, 16, 19, 5, 3, 16, 20, 4, 17, 4, 21, 22, 23, 24, 2, 0, 25, 26, 3, 27, 28, 1, 29, 30, 0, 6, 31, 10, 2, 32, 0, 17, 33, 34, 6, 1, 1, 8, 35, 36, 16, 2, 37, 38, 3, 1, 39, 1, 5, 1, 40, 41, 12, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 2, 48, 49, 3, 5, 50, 6, 51, 52, 53, 54, 5, 1, 6, 1, 55, 56, 1, 5, 4, 0, 57, 0, 58, 59, 17, 4, 60, 61, 62, 63, 1, 18, 15, 64, 65, 66, 10, 67, 20, 68, 5, 69, 20, 70, 0, 71, 72, 73, 0, 74, 0, 21, 75, 2, 76, 77, 78, 20, 2, 79, 18, 80, 81, 82, 5, 83, 84, 6, 6, 7, 2, 85, 3, 86, 87, 2, 88, 1, 89, 1, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 5, 6, 99, 7, 3, 100, 101, 102, 103, 2, 104, 105, 106, 0, 107, 108, 4, 109, 0, 110, 25, 7, 7, 3, 27, 111, 112, 6, 5, 113, 6, 2, 1, 114, 8, 9, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 10, 18, 0, 125, 7, 1, 1, 126, 127, 2, 29, 0, 4, 0, 128, 8, 2, 11, 129, 30, 130, 131, 132, 6, 13, 21, 3, 133, 10, 1, 134, 5, 12, 4, 0, 135, 20, 14, 9, 136, 137, 138, 21, 20, 12, 8, 11, 139, 1, 7, 140, 141, 21, 142, 7, 143, 144, 5, 145, 146, 147, 148, 149, 150, 22, 31, 151, 152, 8, 8, 153, 33, 24, 5, 154, 155, 1, 156, 8, 157, 158, 159, 160, 5, 161, 3, 162, 163, 164, 35, 9, 165, 166, 167, 36, 168, 2, 6, 13, 169, 170, 6, 39, 171, 172, 5, 173, 174, 175, 40, 21, 43, 176, 177, 2, 178, 56, 6, 9, 179, 180, 13, 44, 181, 182, 183, 184, 185, 186, 15, 0, 187, 188, 12, 3, 14, 20, 189, 190, 191, 9, 192, 193, 11, 1, 194, 195, 196, 18, 0, 3, 22, 197, 28, 15, 198, 2, 10, 22, 7, 4, 10, 23, 1, 199, 9, 200, 201, 0, 7, 8, 202, 203, 204, 1, 205, 206, 12, 207, 22, 208, 209, 2, 0, 210, 211, 212, 29, 8, 10, 17, 5, 2, 213, 8, 30, 4, 214, 215, 10, 216, 217, 45, 218, 14, 219, 220, 221, 1, 222, 223, 224, 11, 24, 47, 3, 11, 225, 17, 15, 226, 227, 7, 228, 229, 25, 230, 58, 231, 232, 233, 234, 25, 235, 236, 237, 238, 239, 3 };

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
            final int compressedBytes = 1569;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXU1u3DYUfqRplXa6UNJBMOmKcQZFFl0VOQBdFMEss0x3Az" +
                "i7XoIO0MI5QBfd2cvewr5JsusxSv2OfiiJoxElSvMeMJOxNLao" +
                "9/O9Xyo35BbuflTn+j34Vb36KL8DcS0//fz8Dv4F8vq/3/g9D9" +
                "jLNf/7r0+bb/IXuaXyLbwhz35/evnPDpCWQLR25FK/eOkIK3xW" +
                "+kX0SyLrFkphTf4mdfFA/m+0MnK9Uo1fjKp1CAEIIn8S+tQdwE" +
                "6xi3v6AlZrrgLJBFzBVsg7/c+z9RO8QvyanG4i/7OG6D2gkf8J" +
                "Iv+z2aT+55Hxe6BMy+9W+5+HyP9A4n8+Pq3Q/2gTvCWgwgiNuT" +
                "ZLImViluy5ggsgRGorJhF2c6I24laG0XmhGau/tFKI3zl+PGj8" +
                "uMzxY6NZpGEChNL6RymP8eMxwQ/YLgk/JDmDxxe5/mylhn7J4f" +
                "sfYv25lbEXiPTnPNMfEunPBVyj/niCn6IBP79oM/8a4eeVGT8x" +
                "fu/2P19L/ucb8s8h/nzWyFvBH9KEP8SAPzyNW1MSPM9cSOTz0q" +
                "+wyD3GPyXOEKme3xmJh8gkJCQkJKRFxX/t9W8Iovjvz+b6d6FQ" +
                "FlbikcJRmrnY6yT+2MEQ8ccf5AG64n/aEv/fnJfi3w8H/D7WX5" +
                "Bc0tvu5GHAq5HYTHnBauNPIrXoUFSNnHpR/y7mr2enar80fWMs" +
                "xVhSgeWG/GYB+V/e/wiL/Y9NpK37+mXc/3if9j/AQf+jyFxKs0" +
                "PXYGot5hYkEOKQXNYziK3+1b/EYfT+t6wukI/h/8z90ww/djF+" +
                "YP8UybmxTumeZz//YvC/3cuL8U8S7B9XlIFafnGQ+GXq/nMR/y" +
                "9N8eM2ix/z60sX16/dP9TuX/rbf58Yv5BOlqL+qTp8foO4m9/Y" +
                "53828be5//7OWL94Z6pfNvfvv2TzY1fzrH/wTkcjj/f/A+rf57" +
                "r+kQn0b1z9HVjepv5BeX4BcH7BTyLN8yc1q02kGcr2+GECvDmN" +
                "+Rnh4ZqYjXgWwGOcInIXKNj1n+WQ/eeZGM9i6aa9/2+xf0L0Bx" +
                "wP4s+O+QXL/SPTrd+f8KWh/20AllL/m9c5mH6+LHGIOy6QdK1f" +
                "NKzfIwp72998o6iO+YWR9n8dUz9/Xa+fs1L9HLL6+f2+fk466+" +
                "dj5p/ZRRiDoEn/Fjo/YdH/AJHK78Egv/b+6VjzF27iJywvLI7c" +
                "i1QYQWvCAJ1PZT/rnA2U5yjLK9xhMZgzfVzkDKSiAMuJkziLij" +
                "B8FXeeE1LcWpjEt6C1hxaqQWoI7IiLjq4/kUNKl1AJajlQv1Bg" +
                "Afm37D6L/rAzgpzckkzQQNRsk3baoo7D3oqwZueY4hxcFxKs8m" +
                "v9BFbGMIXXgYkUwgQP4p72WGO+pCbUhblnAdzB31CHSU51uaWh" +
                "5csOjYFC3+OnYVYUDqQEfDi9JovDL1qSVlg6QUp84UYTOPb3F1" +
                "XFIL0EIJoApi1+In0k7WeUz5zJiR4asSJljJHT6hE71Jplt+rQ" +
                "452Tf6nL/PDX0n+Lgv+mbUJQ9R94lSHU51t1Er/xPRf4UEo8sy" +
                "GyUyDWX4sUcu+kyBkGct/u65D91+PGD0ge6u901Ll/Mp//CFrm" +
                "P3D+4iT0j6IIfOO/zf7Bhv2HrvcP1pxy2AgQOL+M9ov3P3W9A5" +
                "r2b9asNjG94fbfMNQfr9e/3ETdfv458Hj+eWaEnhv9J+IP5o8L" +
                "o5H3vw2DorwxAht3/+OUUa/N/cNC7z+jvvtnp9N/pO6kinkXdM" +
                "3ZlXvov4z7d8G8f1dY7d8d9/mRLrV/5Odf9ZSf/f7dk5LffEC2" +
                "sv9xl+9/jPeiW+1/HLr+NSrIVue/VOFEbf6LYUpnkTpgKoyE1B" +
                "N/oIw/rDJ/Ku3taLRnvbF6tlTogpoSKTaG98Zn3fUFXXJaoM3K" +
                "OiqKKiwb9CmJfwZ9foSj/EwuWnJF/tMO/ocqbOO/P0TmghZe8z" +
                "8cyzlIt0tFssDvfUBh0D8CCf4CzMP+D3QG5fs3+K/kP+Rb1f3X" +
                "aQRfmHQi2fpdk+sVzvRpjOePTZL/NfhG1cTS0YJexAIn8m/wYF" +
                "Vu8/wNg6JFC7+bBLIOyduQ+qC/OtLzA0zr/x8C763X");
            
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
            final int compressedBytes = 1130;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUl22zAMBVk1jx0WfH1epDt2OAhfVjlGjoJ2lWWOkKvkZp" +
                "Xq2JZkUaI4k8ZfeEhEkQTA/wFJlgAGdEAgEG4P/cqXw6sA9f4X" +
                "Bfz9Ix9eUA+vHzpgIA6A54YoQJP54nhkCn1+MW1B8AGfWFRO/s" +
                "FgbHxx+tJvj7a7FxvfmwQLuzusYcmybYMotjQfNuxAmCJHTze9" +
                "0HQ79vNYIBb6haRfBALBAtLMQ/x66xOBqHwZ2QL/HWzz94di6g" +
                "cH+4307wAfxZLQ8eOu1Uwr8dT/fPxI9U9+kFEJ5aOkhNquftUn" +
                "RZMVlgQEQsrUj5DfejKRS+SIP9Hi+F98BUhPzUb9AOP8m5HrvP" +
                "4v4PzLVf4gLed/A+ch5Eo8sYsBlVd7qr8IBEqhdqsX1XBFYk+a" +
                "tKr/3EX/Od8Q+zYMR1g2IfIah21f6cXivFWzqY1BdMahys0pll" +
                "d/hoLKXOEqo3tJOAmJwZudmTAeCaALFQNlQ4Isu5E/P+Y7fkbx" +
                "T8gX/3KlfhQQ7/qbHBqYW0M5xRvlcGXqXxW/36H1Q/FPuF39JL" +
                "jwNwbjb4ofCFaoqctHn+vPa4ecvFkA0/lP3wB//OoNKvpQ+gPP" +
                "Hcd7CXegmP49RNxzH6TYiVfO4XAv8E53Cn7CI+jn/u3L/Rt8f/" +
                "L335S/VLj8U5E22PLvLfMP6Vez9uumxB6JEIRRQT5vdhlBYIj2" +
                "vHORmWfT+i86YuRPehx2Fouc2TmAEKv4qJQqSH/3rafQ52XI/o" +
                "70SCiAAcWF8jDeAPZff8c92xcw/7XM2CaV6ox0g0s7Lbn0VHak" +
                "2WyerlII0Ub/PGP/rvwhTGGjF8O9U2G7d+WPmPdvsV//AdqLne" +
                "qtILoDPPkvF/+KQPMXl/7FvP8UmWgq+/nrf+74CZx/YEL+ajkB" +
                "5ian0u+kytLf7P5zP3/dt3yxv38cTvT/oYzxO+cv9uP35RudtY" +
                "aIfPxiZn9Ysj97tz8s549ZmWNP/DiO3+z/ff1LzGG/EvzUun6N" +
                "1w+M/X+0/v88chJ/kHL95OUvQoj4WeOvFf62ak+oOHMOoN92/A" +
                "XV8le7/Gfpf4f8R+4aRdnxk6f+T1S/ycHGclSwHjf6tuBC5uhf" +
                "QrFQNgcjMCH/hP/9WnCeoND3AIbhv4X4UdbxI4rKX9X0i/ZYP/" +
                "xq/n9N6+dr/PnLHbNYQGXHvxWVxPVAzuPTYjFEXS96+79l5rvt" +
                "3r+scNEMCR6zf4bpamaZuX+CNdmpqkYbNAOKsJyVW//G+kseSe" +
                "HT6zl/ZKf8cSD1lyF/dqyDutDHQkJXpBvae+u39aLbmuXloC67" +
                "9+tI3mTl468FzP76WzbXK9TXpQH8mG66IF16aPZUAKPlfv6mXR" +
                "bVcLwXMP/4z+8ushyNdRSuuatbNdwOUj/UyPf8UbT711RiPwJV" +
                "NLmuv56vP3RoXzt/13D/Vv9+asw/ZaqVV5z/MaG16rh/MUaPgr" +
                "Kv/y6i3qD8Oy2EGsUaPT+povL17B7ekB7vmHra+Xs9/6X659f8" +
                "A/jMtgY=");
            
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
            final int compressedBytes = 999;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXV2O2yAQxhRVXqkPPOShlfrAVj0I2pPsUaZ7gj3CHrVxfu" +
                "w4MQ42YAb8fVIiZ2M8MMzPN5h4heigLi9v6OuBElZ3760wl78Y" +
                "IY2w3Rmy+/hlj2/NNyWapj0I6v7UHF9ErbBiuMpC+cAI2+hOph" +
                "Lpd61W9DZGaM+pveMCcuLMtn+LKd91/sQh7A/to9t/5vi39/hb" +
                "8PjViUs98Kch39KJJXX8SVz50/kSN/xp3/4nRxfQoy+a04GdSA" +
                "OE+FWN/xbLqK8eP/pg00sM0H+TsFs2s/z0lwdSe6t21KuzZUDE" +
                "9mJN+9CyPDD/MeOvkbrWpla8t/3YNd3Qo2tVly1N3/Lzjv8e3P" +
                "yXRvz3rRDt2OT+E7j+Unz7UP9PIj/Yf7bQX5T6M+/8R8h/udcv" +
                "92t/jONHFqD+yAEzQ8KcDLWiqVI85VtYZoA1W7by5e3Mh+0fcP" +
                "KXS/uXr56/jNp/du01C89z8y/5nH/F4W+B9UvZkS9I/wAAAOXG" +
                "vxj795wLL2HxN1L+WiV/I/5AUzp7fdrg1Kn3aXIl5+TJ+CZk/T" +
                "mDTGHCdkrkhvKB/LD+7p4Z5Lv0MNeohKUTTv0neMge6zcP/pCe" +
                "f2iWqUffdK0u96Ii9F8A5u6f4sc6O9K/rVjJMeP/4kmhSCPIKr" +
                "+e4iC3/QDMUSGHgP0ifwKIPwBQaf6i9Plr/f53EPUdJlS7h/Er" +
                "2A/MHQBKcDATj2SDpOdNOtLjHAAAgBgBJ/7+yVBsuf/y2f7Jm/" +
                "bn8ctNf3+hVhbcsWTnlA8AvMCQGj/Gr3ce+8/95bdOSlsC0/UK" +
                "i1Sv/OzS0H0ntABWxi/jjl+mj18t9FcCbC7/0Tv3fw1rwvwD25" +
                "TqD2iH5wcNierEutvYlYyaWbSxk18pU73+I/D2+igpT0rPe/4s" +
                "JrC4+kGI0vcf13Mf2HrPn3wyf3rD+SuXPFEuFex260LuSAL+Aw" +
                "BADDQewYJPPOj5A/X87+DL/95i9oIP/40+fv71z/z6x73l2sX5" +
                "j+/874HdZMKG//8jN//Clla26xcfrvj9A8/fjQa5pIjFXRV3RW" +
                "mgBmD7PF3w/jWgaLz2DI2EpFaL78I09u/FLs3RBL+Oh79/CpLd" +
                "Pdc/12T8ov+JX+/QH+ofgFe1op7agAq0IVWwdgBe0eiufpTr6s" +
                "eU+5dC106wybfo9Qs8/w0oo4gcYNB/cI7V8W/5/bez+u5uvNKE" +
                "NNO4v8vYf8auUCfINU69SA+FJGDwBArhXzTpvx+b/f5Yh/M/Ym" +
                "s/CZ5/obnFX1sx/08wf4vy5217Wqm/g1CTsV6eB2ju+CBd28+g" +
                "WeZ/t+Mf+KccrKe5dHSUnk7f/AflFMkk");
            
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
            final int compressedBytes = 852;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnVturDAMhkNKJY7UhzzMAtKdoK7kLMXqCrqELrVlLhQYLr" +
                "nHCf/3UA2ampjYcWwIGSGO6Q//oxWd0H+H94/yT7oZ/nSXqUx3" +
                "/aYVQs3ltZBTebrKv7S/p/iVp1GcuoleZu2LlfaF5/WH0d+X1k" +
                "dyqf/FVP+P3TMr1/at+m9FXgXQv5kfkoiLl/2+zO1HFvYDvkwi" +
                "0j0WpfKnReM7w5JH/BIh49dSf4qpf+n9BwAAAFSHQhcAAIQ4c7" +
                "KNOgMAUCnDvVoajzQ6xB1CF1TT/8qi/nF4/qDhV+zr19Llmdf/" +
                "be32960f4D8Y/0X0v7bUgB55p+MVGMuzJ+DzT3trI60ogr4ILZ" +
                "Wj//dr/t8Pp7v6//dw9c3g/83D/68FKx2sf7vL//sex89M/muQ" +
                "Dx0fuzPb//TDsz+2mWR/MV0BOgLW+Uug9a97+SXx64WN9EqVYk" +
                "UPLuK1WzOMvJ1aL8xHD/uDNPN/afmDLk7tNsawSl0/hk8lDBLj" +
                "Lf0pnf5dTvuhfvT0f5XX/1OMP9R/GH8AeHPa0t66XmwyywOQE/" +
                "jvRmfoMmpnAIBTgp42SfJ9/hmtflVP9eutfRnx+alKYF5Qe5Fj" +
                "ui6IT1LTwGoA8StOAoz1DPC/3BC6oGralUecNI6etaefrQ7jP7" +
                "7rb+z2f3Pd/4yO+y/TOErz/Dye/fjGf/Lsf2K0/1dy++H+a/b4" +
                "feD9OnD7yN/AmcdfgPlbR5u/o1559vV7Olpcq79+dbffm+f+3W" +
                "+I62A5cnXIUJAOufF5lXp2JcT+iqDi/K1P07LT/Ln6/nBjJt9M" +
                "8i+L9u3eXzZp/0Txo09WLQOQv/7T2+Nfj/VfZz92ltD9y9mp3v" +
                "cEbqccxP4z6D9X/UUg/dO033NK1lOA+99VQ09DhmP+er75Rwv8" +
                "/k7c3JVL/bNTvzivHyCH9ku3X+779yW8f1xanCBm/e+fP0WY/z" +
                "Tr62eAQvxJFn/qzt8yjJMmrf1j75+O/B/wjL9nqL+Y61n9/k8x" +
                "4jex8r80hpKMfUN6CkrDVEAzM9nCf+WT/35uxU+snwIAmEA28y" +
                "fe/6iHKn5/BPMciFW/gnklycZ+Lr9/gi3ZS05FCgk3RU+RbbRu" +
                "cX3/VNfvNOW6FUD+UT499If/wv/j8ANgKcgd");
            
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
            final int compressedBytes = 790;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuO2zAMpVSjcIEuhGIWLdCFWvQgQk/So7A3maM2aSaZLG" +
                "xHjiWRot5bBAYGtMyPSD5J9hA1hKexka+/23fjaf+zRNXjA4AE" +
                "JiQpTb64TywNE0YQylZBxMjh/DvfVIzk41254nT+/TCdSsL8Qn" +
                "yVY54pFXj+HeO76/hubXwAIJ/buzBsZalUiI28N39e/nrOX7Nc" +
                "/i41foXnf8l9/t8jx29H/CnB/5ojz6L9sX7Ron4sywd98cOPrT" +
                "DnZS825v+j+XsD8+Kl/fz5Nn/4Qfx/Xps/u+Qr+k86fhounviV" +
                "a6AhImFXQBOGyN+bahe7Bx+6Wxo8EBPmIgAAmvh/U3A1/l6GPz" +
                "+lz6zBfqMjI37C0fhJfetP0voL2M+1mj867K99/bcef6iw/ov5" +
                "07D+lLG/7vhPFe3fpn9C/wL+DwAAAAAbBT42H9sdGV/p+aFhzv" +
                "9MlJb613T2yn/7v57t71bPz2/2v94G/+sNPe22Ho2/3vUHxirP" +
                "Yz4/76n/yP/KYMZ+OJpXrYrX7X9Z+fi64QrSDicsDzyaCL7CZA" +
                "UAm/wX/FOsyWI7OiOlClrewP67uf41+/xXD+eHhFt2BfFTO/51" +
                "Q3GcvMc/3+L/if0z2/UrEZCZESLMMFb/kyHPO+pnWKdBvsrELP" +
                "r9OfAH5G/gif6zX//3wB+Oj6OXfx09/3Uvz0b9V/85p2HjV2H/" +
                "pS7+UX9z/R/JyPqTSvgaKcuvspPFVkghQQ502a66KXEJsi8Ln7" +
                "RyK1TtwNEYv08eCwzgD0X6j6aBxEM7s1mdmoXlqdvxc11kdKFp" +
                "Mj1+6nz9AmBheQAAmuXPIKRMEDZm6DoUIuipVf5Rr/5Wen/d1P" +
                "7bE/Kq9Bfwf+P61S8txPlRs/Wv2PfjU4fxG6mD//+G+Su+foP9" +
                "577nf7EsgvqN+AUAAOgf4P/AIcYI9NQ/4v1DocTaXB4AzPKfML" +
                "SbImH/tMfKL7r+YlB/6fVz8A9l9ScZts2PG0Nm8jwH+kjRpV9v" +
                "bCyeIvP1dPn9K7GnKdLPqzE+hb/07c9mBZ21GDYplu/+/eV/o3" +
                "zufw==");
            
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
            final int rows = 181;
            final int cols = 16;
            final int compressedBytes = 220;
            final int uncompressedBytes = 11585;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmNEKgzAMRWPnQx9l+AH9lLIv66dvbjAqOOrGrE16zoPig5" +
                "g0CfdGkTJR4EuG9WNSnQz1N1x/t7oVUwnU1hSjyLRc/buyYXYh" +
                "64n0nP7L+OgDP2eNnLzcDorJUxYA5gfqqYBp/5hOjf+E/Hf7zw" +
                "39l736H//wvkH/ZC5/hXWqtn8ZrT/nB5r5rX8xzrqZ8N/4p7r7" +
                "w9nxN3p+Tf3/jsrf1z4/8cD4e88f/wZgG/SrO/+E/lTq3x7yL2" +
                "/N8bU6u1xtrxv79GBh2Qb0s6X5a/77vccP8JE79Lw5uQ==");
            
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
            final int compressedBytes = 4190;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWwu8FdMa/9Y3M2v27H0qlVLJTXWKOo6KQkkPj6LreYgUkk" +
                "huKL3E0VVOidSVWyrXI1LeHVyPhNDDlbwuciglXCVK8rryqH2/" +
                "tea998zes8/eR675/fY81systWb+8/9/3/rWt5W2wECBOCTwF6" +
                "UB1IP60JQ3hBbQDlpBKRysdICOKmNd9HehC14IR0E3FbFZ/CTo" +
                "hf1wFLwGJ2sAZbiLPw7nw4UwRDkAhsFl+BKPaRcpnxqHw5UwFs" +
                "bDtbwSEGJQR/sWp6vPst2wDzSCZko9vFMdyFrGToHW6nhWonwN" +
                "Jfid1hs6wKGxN+L3sl/xFjgCOuM90B2Ohd5wgrINf4LTlX1wCf" +
                "SHAWoL9iycx8qM1WoCp+IPqgHD8XsYDeOUaXANG8J07K28BUWw" +
                "Ny7ljaAhNIDG0ARHw77KVNgfmrNiOADaQFulLirq6dBe2QSHwe" +
                "HYGbriPH4B9IRj4DgcjP3j8+HPcCJL4tHqADgNzjLawjkwUKkP" +
                "F8BgZTqshothKFwOV8CbMAoHQDn8Ve8LKmjA8TPQwcDjcDf7GW" +
                "rBXkpLfQ3Uhf3wqERXhUFLtgSKcZnaKjYQDoSD4BDopJ6L3dnj" +
                "2BN/xW9wLBzJekIPOFpdBcfzJ6APnASnwBlwJvTVz4R+cDacC4" +
                "NYf7gILoG/wKV8MIyAkUZ3GKO0gavgav6U1h0vgUnqJbw9/qJ/" +
                "geNgQjJpPJZMQjveLkmL1iOZFPhq5+DFeJUs0bBZYm4yif2SSf" +
                "Y0XX0q7e+KzU/6FuNe4wzCt7N9zCut8tk4W+vEO9jlendepQ2S" +
                "V/TU+mt1qGSxvkIcx95MvJBMKve4dcJEOiZ85X3XmmXap9SLMu" +
                "pjgu5oohrUl+/pSsKXyglfuqMqGbAoU8WaFdvH6lBZukmscQjt" +
                "ncOHWj1/IRm4wEBN1qFMZ095y/nTYh3vYx7hZmu72z4fHyOPhy" +
                "W6KTOsd1DB58SWOvevwVOVoXwm/kpXlVMfezrv6nNvO4TvdeYe" +
                "G+bcO9je07pBBR0/Bdfjg3g/TMGH8Jfg51AIbdYlfiV/ib9IRz" +
                "/pra0e97P6+7bEbavV6n/lelJ8YXyY76kr7T0k9Jj7tOsFf+U9" +
                "nJUIfPG72BHJ0CXeRn/de8yetdpt4dQv8ZXnhsj+7wzH13lXm2" +
                "PL+QdefIm/w636BvPF1jMcRt/BAE8d9eV32N1fMw7wtVOaiq++" +
                "xsS3SFXonRh1aX+Z9w7i71zrymdwUYY30dd5B+Xp+BJ/J1PtO/" +
                "Qf+TJbn+EG/Ij0+UbcCH+DW0ifZ8LBuEEbC9NZl8QDeDG9txnQ" +
                "DdfLr7eX9RWfTL+b9Ddofb6sV+oztf5gXL4foc8S/22A1pOuE/" +
                "gKfaYn+JL6VCXwhdaxKlaiE5+MkYlWQp9lfw+XdZAKCH2WT9XJ" +
                "5G/qIvgrrx1u4hvrYeOrltDx3tbZBrIHH5v4QnP6TTP1ObbC5i" +
                "/ps+Dv+3y00GeHrVKf5d5UOMuLrzKdym4W+uzh9l8TM+WWK4e7" +
                "+EItE1+oK/HVlEoQT+5oCBxEv058LX7gfCufwpFU1sO5Quozba" +
                "U+09bUZw++Qp+pXOjz3+Fq4nGVa3/VRqb9VfeBFny+aX+NXtpa" +
                "elOL+EK1Md1H9tdqyYtvmXF2/H1hf2WJxDfxYmKBja9jf2eB0N" +
                "849WOBia/1/Ul8aUv8BUIjcaCNr9WGgy/Z3w3C/spSsr9ye6vQ" +
                "Z43b+NJvNoyLz4NrqEbSZ00X9leek/bX5a9rf52vXuJrzISuyj" +
                "o+JhVf+QY/FPbXKZX2l7aW/aW9UfQj+0trVeJ7pLC/Nr6wl7yL" +
                "7K8xi1pLw9e0v7G9na+2iW1/6ezxNr6m/ZV3eOwvHV3q4kv6PE" +
                "fiu5HwnateAbero+KnEL63kX81SeDLx0OpOlwdoa3lk8m/mhg/" +
                "TeDLr+Fk+eKn+ywRvd+EqP9CtyyxLLGQV1jnJb5yr446Uq8t8P" +
                "VpbEtrW8InSHz7hGkSnBBvQ2sfvtaXO1GuLYsk9Fn4V7KsHIqs" +
                "0ob0+0dKjQf41HSTVdpVrc1Hh/ThNM8+4es5cvCVR6qssZuJr3" +
                "XFXtZ2v/gZAt+0ug8hvX865nz5qtUHG1/r6Az/XazcxddT1x1y" +
                "fRfcY/KX9ufB3cTf+cahcKfQZyo52Ogoa3hRXnsvLIBuWod0fS" +
                "ZMVpj6bPO3qFbifq8+C/5aVkXYhU4uf2PPePkrv40Tw/gr9Dn9" +
                "jdv+lc1f2/6a/A3WZ3kk+Dstnb+JLnTXpbQXwN/YYlefPT1M02" +
                "dry5VjPaW1rC3pc4J0V/s5SJ+Jv429+iz4G6TPXv562rD4S7+F" +
                "gr98EzC+Ax6CR/k2yd/7oal2qBgfGU+QPj+CG+ABvpl1gX8mZk" +
                "ElHEVtrqfx0enwGN/Ov/bwd52fv0UXFYHTpsVfuE/qcyX/1vWf" +
                "/fwV4yO6V3rd/Me0t/gg8XdFMH9tfU7nr9BnD38fdvHNyN9xwe" +
                "yNLbH5CwMj8ffEcP7yLUH8JXz3dfHNxF9YlMbf7zx1PS7Z9Ipt" +
                "f+HJxG2m/dWughbGRtP+yjct8H1KG2fbX4lvL77dtb+ESR2//a" +
                "WSQWn2NwaSn4RviP3ViyW+PQR/XXy99jf+aSq+cJ7fv0rFVy3x" +
                "2194wqlX+FcB9lfgq9UL5m9cS7W/chtuf8tcfL3218Q32P56vM" +
                "Ympn+Vxf5KfOFpuFRf5eHvYml/L7fx1a6Q/CV86RyNNkx8E1Ir" +
                "GClW0SEmvtpEYX/9/pXcS8HXOm/hS3sOvn59pj6MNPGV15fIda" +
                "h/lfhTML7OtcK/Gp0R3yZR8OUDg/EV9jcqvvAM4ds3HF+9Kghf" +
                "n57sFQnf9XZ8w6fPcxx9nk76vIRv4zcTvs9C00QTGb96TuizsL" +
                "+mPtMdpM9ahVIbm9E+6XPccPVZbi8M8UgkvrY+C/4anYL12cY3" +
                "XJ/lNkCfU65Mwdevz1H8K35TaN256XP/YH2WTxioz35889Dn56" +
                "XCN+IKrIQXtN2wgu+Cl+EVWI6l8BKX1hNW8WJsi+2R8MBWaL0L" +
                "bIakXtgAm+OBtKUvAFtweQ4PRckUbIiNkWwsNvG0J300Tl4btv" +
                "Y/BedB7xEPTkZcWFkQvl7/KvTO4pT3ucnp003JgizKZRkiFCNT" +
                "er009QreWvhXWZ/fE0kCJ1YC/5I1bLL1OZ7QwdRnHO7Gn3kxdO" +
                "SbsUxfDV3wctv+Cv/Zb395WzP+nE2fBb78WzqS8WdLnweZ+qz1" +
                "9+qzzV94x9ZnEX9O52+4Pls2LECfgTwYEX9O12cRf7b5m1mfvf" +
                "wN0WeN9HlMsD6b/PXqsx1/TsU3XJ/t+DOOsPXZZ3+vMvWZK3gj" +
                "TtF2x2vhZJwA63AivA8f0PkqrMBrebHQZ7zBxyyJC9+Okzw96Z" +
                "ztK4P3bP4KfLPzN12foy/g8X4z8Re3hvFXb5pji2t8R+udGq8M" +
                "vydVn+HdIP5mafdcL38J37XOmQ/leoPjP3+Ej1v8fdL1r7j8ut" +
                "DyOcPiV9STPtH8K4GvLPf4V7Hm0f2rKPy17W8IfzdG8a/0Nvn7" +
                "V9J/nhnOX9rL4l9l5q/tXymm//xxin/1ieAvbW18xcgjzX+28F" +
                "2cFd8Ts+Mr4ldB+Lrjo5rAV9mZgu9/IuHbvjD4qkfUPL6uf+XD" +
                "9zMTX4dFJfzmmHxS9OkEvo5v4Rp8DVd7yl7Nrlj4dlRtix2Unw" +
                "+T6l/Bpsz+FX/lt/Ov1K753I1jvfODUfwrz1uQM5OwFbbANtp+" +
                "CZ/r62A7fANfYRV8HQVfvio3fOGLkP55xkfVXdLjV84ZPa1no0" +
                "O93YLjqz2UL74Rvu/yQHx3yO+rna3PsTm4xdTn2Fzyn79z41fU" +
                "zpfymdeH63OU+IbQZ6fca39v/03t77eR9HlDYfRZeyQffcbyKP" +
                "qstg/U5++l/1wXdsLP8CP8YgyAn/hH8iljdPbXdP6a+RuF12dt" +
                "ZLLAS1T/OZy/2f1n+CHSsz2aF3/LI9mA9oH9k5kWsMu40/Kvdv" +
                "NdugL1rbPW/JH13OZ47WVIBvL3JpO/3vkjL3+tPfS17uGvGB+B" +
                "L+YRPn+U9a0PD8LXP3/k4++0IP6a+hzC35zmj7Qng+ePgvkr5o" +
                "98eSVa+Py+y18cETR/xEDw11hEtZAFR7ITOlCphQ20s+8wHjHj" +
                "zzW3GJURWTkxN/66+Ru58ze7/RXxjQj8XZIXfxdl7MF1Gc/K2X" +
                "c2XD8G72dXYEY/oGbxLYR/VWh8c41vhOK7tObwzexfMYfVtn8l" +
                "9+vDjcZXpj6zUeb4l41kVjsi/ypYn/WzoulzYHzj9t+fPqu1C6" +
                "TPy/PRZ3v8m1mffT1x/SuZf4WNlHnsauWOZDJu4juBykpdfY46" +
                "v6D3888vKHemzi9Y9QXMLwSPf9PnF3LV5xr2r6Lp88p8+Mtb56" +
                "vP8UqQaYFsvCyrx66x41e0LhX4Cvur3G3HrwJrovGRfnb2+UFv" +
                "/CrM/kJGNHKbH3THR1ZpDvlX4fjmln+lvRo+PxhQ9yHB+GbOv6" +
                "ISZ3zkKUvJv4q/w64z86+Uh838K+UBoc/u+MjMvzLHR2n63D9V" +
                "n83xUTR9Vhb8YfX5tT2mzzL/ilWwSZb9LVFWJHpCfTZZecMdHw" +
                "l8ldV2/CoDvgPywPeg35/9xXkFwndTNHyVpYXGl10v8IW72A0m" +
                "vsrbyruCv8q/lffYFIEvVgh8E4SgOT/IbmRTvf6VmB908D2n+v" +
                "6V0OffG75CnwuC75Y9y18xf0Q/a3RE9vdm0/4K/wpK2Qxr/qiT" +
                "nZ9j+1fwGG2be+zvuab9VTb6fKQmXvsr8nOC/Ctf/0oy+FcPwg" +
                "mxnXna34j5ObEfstnfaPk52raas7+wKKP9lfmT2MiOP7Nb2Vxr" +
                "/rfUOz/o9Z/t/A3Ct5fpP8PJ2BHKyH8eKOLPwn+W/x9sqHxs+s" +
                "9B878CXy9/ZW/S4s8uvt74s2HkG39ms6LEnw1emPgzr1/z84Mm" +
                "vmy2P/7M5pjjIxtfZauyxcVX+dKPL7stC76DUvD9IgDfWfnjGz" +
                "C/cGtu+Kboc9j8QtMC4dv4t8I3bX5hsYmv82yEr6WMNC5SdvjH" +
                "v+z2LOPfC1LGv19EH/+GxG4i59ftmfFvtCUGNTf+zRLVskaCKf" +
                "b3LvP/R9AuMce0v9iWTxb2F1sJ+yv+f2TaX5Wb+Ar7S/gOFvbX" +
                "xZd+HXhFrvbX/P9Rdexv0P+PrDPl1bG/xtTC2N+Yscfs7/MO0v" +
                "fK9Xwqe5ktYPdwmTdXNMPOjxX4Oi1a+EoELHzl9y5jG2rcxZeu" +
                "rYiWH+vFNxt/DSPkzjR8nTPl1eGvMa1A/O2dg+oE5sfmqFwp+b" +
                "HYiD0AK9lCwvc+582WelqIGp+8yK/PatPo+c+56HNsZ676nMvi" +
                "4hv7oTD4GlP2FL5M/r9PbUZlK+EFWq9Q9zfz2+mcjBjS+HeVNT" +
                "66IRABT36sPiRryy96x781u+SPr4hfFWKJT9mz/FXbIvmkeJKD" +
                "ulRt9pzIfzb5K5Gsm4JsWsayfnHWlt/7f8K3UPnP8ak51FHN/G" +
                "ffcUr+s5sfy16y82PVI33jX09+nZ1/lRKfFPGNoan5V258MnV8" +
                "lBq/Ch4fBcWvqjM+or1qjI+MZYUZHyU67rHx0Sf+/Fi2PNBvka" +
                "2r3bJ9e/olET3+Pyh/a2bJa3wklRr+Bzt+f+o=");
            
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
            final int compressedBytes = 2032;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW1lwFEUY/qenZzbZJSQcCUeBSiThMsFwlFwRRUW5C0Qt8Q" +
                "IJoAJyyGUgUBCwlCDBEhBBEFBAQKt880ktH6wyEIGQoDxYxSGH" +
                "YECs0geRqvWfnt7ZmdneIzvJZCemq7p7pme2J93f/N9/zB/SGS" +
                "SQwQ8B6ftgENpDB+hGCiAXj/OgAArVnqQvuZ8MDgZJHukFxfAg" +
                "Ht1N2sNokk16kN4wkQyCKSTX9xpMhxIykHSEuTCf5NAJpAjv7A" +
                "rLYRmUwRqcj2DNVPNwNB+POkHnoKlAPu/7sbYIBpJC49pQrCPh" +
                "UWzHsPMnWTsNnmP9Nss8C1i7AusqY6wNdGR9DmRDF9O9PbD2gj" +
                "7QNzQiX8aRB7CO8HXD9mF4xLh3HIznR5PhGWP0ZZjJ+jnwCrwO" +
                "i/DoDawrYTW2FKuK1Qfp/O4MyGJ9O+jOR+7F2tOY7T4YAIPNq1" +
                "HzYTiOPwSjsH0C61iYAJNgKjwNT7FfPAsvwAzsZ8Gr2M7DuhAW" +
                "Y7sU6yUoDQbpbDyqx3pdqgoGpWNSNdygc+AP4wk9STU5SerIcX" +
                "Is/FxSFbQV31z7CKmxj8A1nC8v6Kik1yRyl4ZvMkXDlz/nZNDV" +
                "QmeJRtX85GeUjrP2R6mWvCOdlk5Kp8gGulSqIWsNhMrJGpW9Xe" +
                "Rt0QxkvQnfeYk91Sm+/nbu4OvPDKZAcYTvCYZRZ+kM1NM1KL91" +
                "OPYT/Ak3kJ/N8mviZ44r8jO2jJ+xH4Q11zefXUF+Zn0OXa3zs0" +
                "h+SUJ/dZifG16c46vxs8fxZfJLK43zn4VPYPJLt8SbzbfQJfmd" +
                "4ZL8vuR5fM/q8huyr+hWtTJsX0nnrPYV3R6yr+gOoX21yGZfbf" +
                "O2faVWeN6++kuzrzR8pSsM74ua/YyV28/SJSiw8zMUi/iZ4aut" +
                "q8TEzx+G+VnHlx1lxuNnHd9o/CzCN5r8mvHlfQ6u6lfbnb1E8g" +
                "sj/FVR555sOub48jMDX3ZG+aiBLx5n8b57lLkHiOU3hC8/mxrx" +
                "OwNfk/yytUjXcfy2dA3+tf3iDrLyzhA/i+0rCz8vaUp+Jvvc1r" +
                "/k47jP+DvF+fk3pll3g0T30Y/oXukmPQLt6QHoRvdALt0v/Q4F" +
                "9DDXv0elG2xNxdya32VZ6RTEV2OEEovFfwjrQZH8xty1fjGvui" +
                "e/lzwvv/W6/qVf8PgGs5mhA44hL8N7gEjY4xvSLWF8owL5eRlM" +
                "1+1nrn8/D+tf/jeQkPzG0r/8LIr+TUByF4jkV9e/2GdbRjX9u0" +
                "mkf9G+uhJV/24M61/jaqWmf03nq3mvWu7K4L3h49n1L1ah/jXu" +
                "YPoXe5v+NT3D0L8SMP3rJ+kkQDKIj6GdJbafic3rJGoEP69oSn" +
                "5uMJ+3ccrP8vNet5/5TownY8hYMoGfTUoa3zdd8o9uuYOv/2bL" +
                "wJdetuzLN2zsnBnfBOOTpfHjk16S39jxDXrBK/gqaZb1sX1R2l" +
                "r2qlppE8ZXzpDbivAV7DHiq/ib571vBPm9nQryK2c5nuEuy9k9" +
                "gr2qxlqnZMWW39QqTS2/ruHbw7H8Ztu8pvm6/EL/MD+b5TcqP6" +
                "9MTH6d8DOsdc//DdC4z3Ahghmbn2FdzKvlun8kD9X9Iz4a2z/a" +
                "GNU/WmX1j5ROHvePqjzvH72l+UfK3aH4M65utB5/Vgot3/dN9p" +
                "X+fV+TXxjN55vI4xtlWvyZjSC+YfvKHH+GreH4Rvz4s+laM8Sf" +
                "AwXejT8rfRi+H+jxZ2WcsToeIdHk18SzCX0f5FeM+LMyNvL7oD" +
                "GjB74PBvqngv71rXOgux9n+nelUip9ouC7Lk8VagD2dillcf+S" +
                "zXH3/Fv3/CNvfv+FryP1rya/DZrjO8OSOsSQ05jEpn+ZfRXWvy" +
                "b7Sp4p5OcK3I8dmv6187PiF+nfWPwc1r8ifnZP/8bIv/KI/pVL" +
                "mP6txMpkTy6NIb/xv+9/lkrxyUb4vu/5+FXo+75Uq+zS86/YCs" +
                "us+tf3pV3/kl8S0r87U1f/kvNe8X/JRQf6l9mXym7Ed7OGr2zo" +
                "8rSIWLImv7RLKqw4MO3/JL+0q1N80X4+CPVpdXBdLlc+lddr+b" +
                "Hh/Dq6gC5M0n4+IM6vo4udyW/gRdFoWm0LxXdJ8r/V8+tMa9uA" +
                "+3Q2SpTrcNBTpaXkT/oU5/oXJL62bdb8KzwqsPu/UJyeKbcV5V" +
                "+x+035V8pX4vyrePo3Xv5VYLmz/A15u+3OaPkbUeXXzfyN0PeF" +
                "5POvdHyVH/h9Efimj9LxlXeG8usi7SsRvub858bENzJ+1XB8zf" +
                "ZVNHyjy6+b+Ibsq+Tzc2IV5ZTxHFN+nVKUpC7ZlCr83BT2M61I" +
                "NfuZvsva3Vj3KTV0r/K+clrZSg/g+R6s+9lVrnfpUZRk03/IWP" +
                "Prosxv5Nc1qv28J6F9aSH5G07sZ9PbbsSv/Dla/IqN8fgVl/Vh" +
                "fP08foVXbPErbG3xK35mi18Z4y7Er6TZXo9f+RSn8SsW37hg9X" +
                "9jf19QihLyj863xjdSIb7B+fkq8vM/9Ij/anR+tvBGs/Fzq3/U" +
                "MP/IFu8sELxD1Wrv+Pkbgt/VNON730L0b2MXdSRr+1jxTSR/sq" +
                "Xhmxry2wiIFhL0KUP5z+pjUfYrbv6zWyVwolV+k0R6iFyrItby" +
                "KfkM36Vykf9r2sX1zYBvTSu+DWIyi5fiGxLPfvYNjYxPiuxndV" +
                "hz2s8tJj/Wcf6zOtzMz77pKc/Pd1rlt0H4jrDguyXV8W21rxro" +
                "Kf4Hh5GaRg==");
            
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
            final int compressedBytes = 897;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm89rE0EUx4eX5G1MUkXxR/AiVaxaUVpqj9ZfoFarAaWWFh" +
                "W9+JNKrVhrDxWpQqV6ECpUUbyJvz158E9If9jWH4fqxYNeBc8K" +
                "63Sc7GY3m3Q32Sa763uwOzvT2SUzn/2+eW+SqqpuuFV5lFVVYU" +
                "C7GlQtDG6oZbfERju9IFHc00M/Mlexn2ogDJugmc/HAVVV+Igi" +
                "w3nma7GpjpX6vIk64uuI73aN7y8/8LVnxFebiRZoxp2zfEUt5X" +
                "n9poivI/3uzuh31qJDnufbSnydGgvxIy74vhLM9xjmahwm4ROM" +
                "wajo2yTa0tq9B3Oedp51ij7Tst4jSzD0WsGShnqNoVbPGvRa9A" +
                "Vv2Sb5ttsZUfS16TMttRx3tThvYLXWfNkOtkvru5+1aNdtFs86" +
                "Y6j1y9KgA1YlS00tbI1DUvv4wT0YOyLrfDbYSUOPLtbNz5f5HL" +
                "xhfZzl3mz95tWDC/qNzrii3xO25qGX9Bv9Ij10Sue7oJfiq+D5" +
                "Z8n5UOhjbIKPcSr02cP57wXi65DrYV2/sRnPx1f9xNc18q2F9e" +
                "vMwrfLGDGWvP46Ht2Q1+iF7whySWxHEY2Fh1U1/lW0bTJooRbq" +
                "sAPbYC2sE/1GYBUs4e3LoBrW83ILP1bLvg0gIlVYHr4H9bxcaa" +
                "muGlsa3FzJ9TdIZs6PNP0ezdXvv/wo6968+ZFWdzU/shk7lpwf" +
                "ib8EID+CpMXbn6NfaOSl1C8et6NfPGbWr54fzb9+y++fvZsfzc" +
                "2XH4Kv1mLgi6es+PKjgv65WAuef7ajXzydrV8zX2v9El/v8cWz" +
                "dvyzPb54jvj65A2Q/jmrRfDlpeBriMg6df2KkvgGgi92kX/2O1" +
                "+8mJ8vrb+e3aPqLjV+pvjK52/AJfvrL2+j9dd3+VFGv9hD+g2g" +
                "fq+QfgNK9qo214P/x4iD6J+zvz/Kt7+BHdjnp++PiK+T9beo/a" +
                "u7xNdHnnqE/LNv/fNDfDCXfvGxY/3eJ/16hO8TfI7PXPfPT4mv" +
                "R3zvy/L8/pn8c0Xovsttiyw0sByPJPT/X5BtaRv6m+ZPihHfiv" +
                "vnDziK4zjGWU9hGt+75J8nyD+7rsVJx3e8FefvckZvmmZ4AK4V" +
                "2t+oxO/bSb/z8ub8pvzI7/sbhfavoBH/0P6VbxnfyvhnBfj5Ov" +
                "nnIOrX3fxXiZN+vcJXqXKfb+Qb8fWPKYsovvKnsb/GMkpf");
            
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
            final int compressedBytes = 480;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2t1KAkEUB/A46ZzrSqO6iYoypKIo36AvK4lugrroIbrpFS" +
                "woIhTKIuibypvoKSr7Al+h59jGZY02FQ022jn7H1hnd3ZBPT/O" +
                "GWdXy6rduK28R2krEK35Q9o3oo4qY8OuoziNUkL3/RRzRrqpVb" +
                "9GqYcGdT+ht17nzDhF7L6dIzSm+66q7znQ0Ccbga8Xvhz9C99Q" +
                "Hr6i87cTvqJ9++Ar2jdmom8QG8X1lnCN2L66t31dv7iHyr7Olf" +
                "D958aTlhXeqXdVeBeRMrc+85T39TmcQ332Rf5OI38Dk8t3jvlM" +
                "xZl7RMfY+xspnuc5TvKCeuNZ9eJNfVbPqM++q9WLNWON/JUtv4" +
                "QYGCq3Rkmdn6k6tbLlx7FC5MxZH1WMeXH/ah3zr29zesMV5wK9" +
                "UpGe6PHb2EMDPu+IpBHaaW1V1NsvfdEM8d2Er/hZWvvyFnyFZO" +
                "w25t8A5W6hlL+8h/osLo8zX55pRMPU9a9aUculvVCm9vpXrXK2" +
                "vP4N7Tf0/7os1r++yNED3L8S7ZuDr2jfQ/iK9j2Cr2jfY/iK9j" +
                "2Br2jfU/jKXv9WGfPi+e8Z1r++yN9z5K9o3wv4Bs780mWJ50cm" +
                "W14hf0X7XsNXtO8NfEX73sJXtG8evpJb0ydRZioa");
            
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
            final int compressedBytes = 542;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms8rRFEUx3XezL13YYGQbPwoP1JESH7tzZjJjsQC2SnF3g" +
                "hDNoQoG2tZWFAWfmQhFn5PyX9AYW+nnmcMZcy88epN3Xt9T83c" +
                "mfvu607nM99zzjvv8W3ymSYFTVuj7LjvzIRpZHwHPtCA4m5S/b" +
                "7AO2oaFfB93sn93McDLMI72I01V/1jRRXVUqM1llFFbKaIcqz3" +
                "PCqhSmtssF6lsSP1lBsd89k11VljYcI9y//0y2pAxxW+B+yCXb" +
                "FL02QRdg6+8hq7dXzGnsX38JdnXeDr9YOvHPpNMOcCX34EvpLE" +
                "52PoV2u+J6ivEJ8dx+dT8JVEv2eon3Wun9Ok3wfw1To+P4Kv1n" +
                "xfwVdrvm/gqzNfYYCvPHxFltt8PSHwld3E9119moc3VNWvaEL/" +
                "CvnXcf5tBl9J9NtqReK2aLcjIlqgX4nzZbvTMz76V8KfDv0aG+" +
                "CrdXwOgK8Umg+apncx1SrvEjylbP7tFT2i316/os+xfruhXyn4" +
                "BsgnBr+eb6euJKsUe76dMpWLowPp38O4i+40ZNzHvDRj39+gMP" +
                "ShdG4etucLU5zvCPji+ihJfTWK+kouvmLMeHKxv/EMvtLH53HE" +
                "Zw2ufyc/P3lW7eKzCH3p17P+p/g8Af0qpOQp+ED1/JtCv9PO9O" +
                "tZg36lUGZYzH4TLo7zMPob/+VfMAcfaM13AT5QN/+KFbblen9j" +
                "GflXCmVu4v6vzpbxDlGvLCU=");
            
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
            final int compressedBytes = 569;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnMsrRFEcx6fjOPec2JDHZCPKI0XEbOyVYSQWiq2FwlIKsb" +
                "a0oJQI5W1rKxYW3mPhD/BYSV7ZiFzHdGnubXDlTp1zfX915nTO" +
                "3Ds1v898f7/fOZ07JMg32SpbMeOMlNtGZaSShGRfREqsmXySKV" +
                "+zSQEplX2NbIXWO9UkK9bnsGVSJfs8M4GRYtOFkQoT9mcjERLm" +
                "26TJGjV/cVWGY8wU/1bpunHgW0nyRDDBnAf65TvQr0a/rmP4QN" +
                "P4/KlfOv6dfvnJh37ppBv90gnoVyu+p+CrJ19+LuPvmdf5l46A" +
                "ryJ8L5NSX12Ar1rx2WO+V+CrFl9+7Y4v8i/0K/V7A76+5nsLvl" +
                "qtj+4QnzWtn++ToV+6Dr4qGJ2WldWD7Get8YJsM7LNx2quR2t2" +
                "zXHXlItPXpJtER5W3fiT1FLIpqyYfmUf069DdZZ+rRH0q34EL3" +
                "PPlz+D7z+pn1+QfzWJz6+Iz9CvU78iAP3qY4LAB/7e3xAp2N9A" +
                "fI6LzxR81eIrUt3xhX59mH/T4ANd9cvaWdtP+Zd1iHToF/k3Lv" +
                "9mgq86fEW213xxvs7n+s0FX+WrqtpPX4/CG9CvTb910K8afEWL" +
                "0Wg0GGEjwqJGPTvy6PnQQ/BVhG8r22MHbN80WZTtgq+6xn79lB" +
                "/bcL3/3In1r46WOiS6XFw1DE/5upLuhg/0tPfzk3RO9LyfnxS9" +
                "zvOT1PpfDpyf1FqdfT/G5zF4Cetf2/q3H/WVIuujwaTwHQBfFS" +
                "zwBhXnvZo=");
            
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
            final int compressedBytes = 533;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7lLA0EUxs1ks7szpXhEQUMCGokYIx6FaO9VK6iVhaX/gY" +
                "VHYSUWCloFCztbS1FQsZBIMNioheBRWdiq6LqGmGRjzEFGmRm+" +
                "B8nsTpLdzfvxvSOTpfOW5Vm1iphnzYJJacRLF37MdTj2QiRCeu" +
                "2xhQRTMz5SbT/XEj9ps8ce+xFIvdJNapJjnWeHdNljY95ztpZ0" +
                "ZWHQ4cE3zxwHvnQRfAXRb9QYNUaMYWNMjxtD+jkfvnoMfAXhu2" +
                "1Z5sDXth43+8FXXDMHy/2Evpe9504kjzLjvkx5eCnt65W8BJZF" +
                "+eZ0H/SL5V9tvVD+pQff+tU2S9GvtgH9isKXHiZ1+/Q7X30yE5" +
                "/BVzajR0XfcQwvoT9y9Ecn0K9U+fcU8Rn6zfA15/6TLz0DyQr1" +
                "G4N+pa+zLnI8LE3/C6uQfAI+UJrvFXwga/6l18XrKz1a9u/PW8" +
                "i/Sve/N+ArDl96z53vHfgKxPeRO98H8JWovnqR8apdsyCXm3/p" +
                "Gyf9vkK/gtTP2XTfucXnD/AVTb+MMBf+n6NyfGYa9KtU3fRMvM" +
                "w0J8zpwvWzOVX2+tE4+MpjrB4+kDc+s4acOR73L9xCvyL2R7z4" +
                "sibwFYtv4fV91oz1fTn5Mt9f6FfbBV+l47MffJXmGwBfpfkGwV" +
                "f4rjeU9vUKvAH9OvTbDv0qzTcMvkrz7QRfiTJxBD6Afh367YN+" +
                "RbCqT4OgoqU=");
            
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
            final int compressedBytes = 495;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrtKA1EQhuWwOexOKSpi4wW8ICiKimghFhY+QRrF0soX0M" +
                "ImFjZeCgUfQS3FIGhhI4gakzyCECMoWmjlpTgukigb1s0Gj+Hs" +
                "5B9IzmY3IWE+/n9mzkap34NGi0diVSEYB40hB9EM0exzrs/zql" +
                "cMiBF37RTdhTOtot59bhTtosddh91HR+HKkGj4WptoQgy6a4vv" +
                "d3aF+mX9oKOHL03q5xu7AV8jnHdKqdh6uXfFNpEpHv7sfOjRr/" +
                "MO/UZI5dPIQVT1S3G5L/d011+5C/2a5c/WVhBfmivytXbC8LW2" +
                "wZf1fDQPvob484K8lCl5pZTMyguZ1uTP1+CrO2Sm4k8kPV3UYk" +
                "mGE99HvvuTYgU5Z9I/LyEHqL+e+rsMf2bNNwG+xrvyWnD9RUSc" +
                "7wb4cvNn7D/z5UvCHsf8W5NOfYAcRFe/dGjPBvfP9kyl+rXj0K" +
                "85/kxJ3fMR+JpWf8vcPzrC/SN2VfcY81HN6PcE+mXN9xR82fnz" +
                "GfyZ0/6G1v75Afo1i6/9GI5vOH+mc/A1woNTP8dWW0mG8f8NJv" +
                "p1bnX7M6WrqV/KgGQg3zvdfJ18VflmQfJPLp5DDtA/e/w5j/7K" +
                "DL50/y/zUQ58Wev3CXxZ830GX+O7qpfg+RdRw/p9hX5Z830DXx" +
                "Oi7hPZTE3m");
            
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
            final int compressedBytes = 430;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmr1KA0EUheW6ySaZTlREi5CIRgTFoIK+gSY2FmKhYKOtgp" +
                "WNUbCJWtgo2PkCQXwRxUfwOfxhXTQGFjUmsAt3xu9CdpJNdz/O" +
                "PWeYkaHgW8lU5NekzMh8uI5JqfkmL33hc0AKMhGuc+Gn2PxnVv" +
                "o/1sHcq5TDdTj4oWQ86KBkOqASrdxbq9d1umFnSUL6Nb3oVxdf" +
                "77IdXzPyxde77oSvdwVf7WXyzGf79WsKftWv+Mv+SvrRX0o/xD" +
                "Of0/foVwnfYhL+m6rA1+l8NQpfJfotMZ/Rb9f6LcNXfX5eID87" +
                "zXcRvg74bxX//fdKXqUH5KtIvlpDv+pVu47/ot9f9LuBfp3muw" +
                "lf9fN5i/nswP5o+/Nb+/PfTK2789/MIfq1SMk79AD/jfjvLvrV" +
                "w9fsxc3Xq8FXo36zL/HwzT7D1yL/3acH+G/Efw/Qr9N82R+5zZ" +
                "d8pYzvH/fbj7jfbidfc5yEfr0GfC3Kzyf0AP+N+G8d/TrN9xS+" +
                "VuWrM/KVpfnqnHzltn7NRfx8U0/wVZ+ab1q95v6G26Qb9MBpvr" +
                "f0gP1RZH90h/9qqJ53KX0gXg==");
            
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
            final int compressedBytes = 375;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2z9KA1EQx3EZt1h4WwVNNBZBISpCxKCSG1irhYLaW6idR1" +
                "HwEF7BI1hJ0CpG8A+If27g+pAo+zDESBR2hu9A8kjSzYffzO5C" +
                "ZCz9VlILPs3Jgiz7syoznW8qUvDvozIps/5c8q+pzi+LMvJxFt" +
                "2Z1P1ZTruUTKd9lMyn1MAlX77RUS9fd/7pG5304xsd46vK9wJf" +
                "3b5/O5/jR3z1lGvSA/IbXF9dkl/Tvlf45sPXteLNeKe3b7z96/" +
                "27ga/p/F7ja9q3ja9p3xt8Tfve4mva9w7ffPn+8HzynueTSu+P" +
                "Hv4jv9EpvnrKPdED9m+wf5/Jr6r9+8L+Vbp/X9m/5Ne9kV+7lQ" +
                "g9MO07TA+4fs7O56TIfNa0f5MS+5f8ZvI7jq9p3zK+qubzBPOZ" +
                "/GbyW8VXVX4b5Nfw/e8KPWA+B/N5lfya9l3D17TvOr6mffn/gm" +
                "3fLXwVXT/v0gPyG+R3j/ya9t3H17TvAb6mfQ/xzUMNvQN9PzY1");
            
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
            final int cols = 120;
            final int compressedBytes = 115;
            final int uncompressedBytes = 3361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjkviPAZh0UXhaTAZMpkBalUkdKiLPJAQkRZkUmTSAtAkQK0" +
                "FljJlEwLQYTzmTIZCW+o8FMKn9JwIw6f0fBRQDJlrFb8Vo/A7r" +
                "+K0ejd9hHb8do/E7rOO3czR+h3X8do/G77CO377R+B0MgAEA1I" +
                "z6zw==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 130;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3ckNwCAMADD2H5owQeAVEYQ9QD85oZUasTMHkAv1g/xUPw" +
                "DN+t/p+fqv/AIAsF8COP85fyK+APw2n7w/lz/uN8RffAH0d+iS" +
                "X6/nr/oD9YH8AQDzFQDA/gEAgP0VQH8EAMxvAMB8BwAAAAAAAA" +
                "AAAACA3O3/n/g+v9gCiMcZ/Q==");
            
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
                "eNrt1DEBAAAIw7D5Fw28XBggkdCjCQAAAAAAAAAAAAAAAAAAAA" +
                "AAAFufSiAAfwYAAAAAAAAAAAAAAAAAAAAAAL4Ytgg9wQ==");
            
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
            final int rows = 790;
            final int cols = 8;
            final int compressedBytes = 58;
            final int uncompressedBytes = 25281;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0zENAAAIBLH3LxpwwEZYWgmXXAIAAAD/elUCgX8BAAAAAA" +
                "AAAAAAAAAAAAAAAAAAgEsDG9o9wQ==");
            
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
            final int compressedBytes = 169;
            final int uncompressedBytes = 18849;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2EsSgjAABNEoIqB4LDlXzs1HPYALXSrzqresumsSKrXUD9" +
                "znWrAPyhff6K039Ibe0Bt6Q2/oDb2hN/SG3nq/Mx14iup95Snq" +
                "PF95iur95Cmq94OnqPu74ymq94WnqN4tT1G9G568r8H/OfTG39" +
                "3fI09R+954itr3wFNU7xtPUef5wlPUvk88RfU+8hTVm6es3mee" +
                "vK9ht/vuebJv6I3f711eU6ZlSQ==");
            
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
