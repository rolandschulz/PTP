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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 18, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 7, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 110, 189, 190, 0, 191, 192, 101, 1, 30, 36, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 58, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 58, 3, 1, 7, 123, 4, 124, 0, 127, 5, 221, 125, 237, 6, 128, 7, 126, 173, 0, 238, 209, 212, 214, 8, 239, 215, 88, 30, 9, 216, 217, 219, 218, 101, 30, 114, 10, 220, 11, 240, 222, 12, 227, 13, 0, 14, 228, 2, 129, 230, 150, 231, 241, 242, 15, 16, 243, 30, 244, 245, 246, 17, 247, 29, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 259, 137, 260, 261, 262, 22, 23, 263, 264, 24, 265, 266, 25, 3, 267, 268, 269, 26, 27, 152, 154, 28, 243, 270, 271, 237, 241, 272, 273, 4, 31, 274, 39, 29, 39, 244, 275, 276, 277, 0, 88, 39, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 56, 289, 30, 290, 291, 156, 6, 292, 293, 294, 245, 295, 296, 297, 238, 298, 299, 103, 300, 7, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 30, 39, 311, 32, 312, 313, 33, 314, 315, 316, 5, 317, 34, 318, 319, 320, 0, 1, 2, 321, 322, 323, 30, 35, 324, 325, 58, 326, 239, 327, 144, 328, 8, 329, 246, 240, 247, 236, 7, 248, 249, 252, 253, 254, 330, 255, 256, 331, 242, 9, 173, 10, 332, 333, 36, 334, 88, 335, 257, 336, 337, 338, 258, 180, 250, 259, 339, 340, 341, 262, 264, 342, 343, 101, 344, 345, 346, 347, 348, 349, 11, 37, 38, 350, 12, 13, 14, 15, 0, 351, 16, 17, 18, 19, 20, 352, 353, 0, 354, 21, 355, 22, 23, 24, 40, 39, 26, 41, 28, 266, 42, 43, 32, 44, 33, 34, 36, 356, 357, 37, 35, 38, 358, 41, 359, 360, 42, 45, 43, 46, 47, 48, 361, 49, 50, 51, 52, 53, 362, 363, 54, 55, 56, 364, 365, 366, 57, 59, 60, 367, 368, 369, 61, 62, 370, 63, 64, 65, 66, 371, 67, 68, 372, 69, 70, 71, 72, 373, 374, 73, 375, 74, 75, 376, 76, 77, 78, 79, 80, 1, 377, 378, 379, 380, 381, 81, 82, 83, 2, 84, 85, 382, 86, 3, 87, 383, 88, 89, 90, 0, 384, 385, 91, 92, 4, 46, 386, 93, 94, 387, 95, 6, 388, 3, 389, 4, 47, 96, 390, 97, 5, 391, 6, 98, 392, 99, 393, 394, 100, 102, 7, 395, 104, 105, 396, 48, 49, 106, 8, 397, 107, 108, 398, 109, 399, 400, 1, 401, 402, 403, 404, 405, 406, 123, 110, 407, 111, 112, 408, 9, 113, 114, 50, 116, 117, 10, 0, 118, 7, 11, 119, 120, 121, 12, 409, 122, 123, 1, 124, 126, 127, 13, 129, 14, 0, 128, 410, 130, 131, 132, 133, 134, 411, 135, 136, 412, 137, 138, 139, 140, 413, 141, 414, 415, 416, 142, 15, 417, 418, 419, 420, 421, 422, 423, 143, 145, 424, 146, 425, 147, 18, 148, 181, 426, 427, 7, 428, 149, 150, 19, 151, 152, 429, 143, 430, 15, 153, 154, 155, 25, 156, 20, 157, 18, 158, 159, 431, 21, 432, 433, 434, 160, 435, 436, 437, 438, 161, 162, 51, 0, 163, 164, 165, 166, 167, 439, 168, 22, 440, 441, 442, 443, 169, 52, 116, 170, 171, 172, 173, 444, 445, 446, 174, 175, 176, 177, 23, 7, 178, 447, 448, 449, 450, 451, 452, 101, 453, 454, 179, 455, 456, 180, 53, 457, 458, 181, 459, 460, 461, 462, 463, 182, 464, 465, 251, 466, 467, 185, 183, 184, 468, 469, 470, 471, 472, 186, 473, 474, 187, 475, 476, 477, 478, 188, 479, 2, 480, 481, 56, 189, 482, 483, 484, 485, 486, 190, 487, 488, 489, 490, 191, 192, 491, 492, 493, 101, 193, 494, 495, 194, 496, 497, 195, 498, 499, 500, 15, 260, 27, 501, 196, 502, 261, 503, 267, 504, 269, 505, 270, 197, 36, 198, 199, 200, 24, 201, 506, 507, 508, 202, 509, 274, 510, 511, 512, 15, 275, 513, 7, 8, 58, 9, 10, 203, 514, 515, 11, 516, 517, 518, 16, 148, 519, 18, 278, 520, 61, 0, 3, 521, 522, 523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 28, 533, 168, 534, 535, 536, 30, 174, 537, 538, 539, 179, 540, 32, 541, 35, 17, 542, 543, 204, 205, 544, 545, 206, 207, 546, 208, 547, 548, 1, 549, 283, 3, 550, 288, 210, 551, 552, 211, 553, 213, 39, 554, 555, 556, 557, 558, 559, 63, 560, 64, 65, 66, 214, 561, 562, 563, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578, 73, 579, 580, 581, 582, 583, 221, 584, 585, 586, 587, 223, 588, 589, 590, 224, 591, 592, 74, 75, 76, 88, 593, 4, 54, 594, 101, 103, 595, 215, 596, 597, 209, 598, 599, 600, 601, 5, 602, 603, 6, 604, 12, 14, 605, 606, 607, 26, 608, 609, 610, 217, 611, 612, 219, 220, 613, 104, 614, 615, 616, 617, 618, 619, 222, 225, 620, 226, 621, 182, 622, 227, 15, 623, 624, 625, 626, 627, 105, 107, 628, 629, 630, 108, 631, 109, 114, 115, 116, 632, 228, 122, 633, 634, 2, 635, 123, 125, 130, 636, 637, 229, 638, 639, 139, 141, 143, 144, 145, 55, 640, 641, 642, 232, 62, 20, 643, 644, 645, 146, 7, 21, 22, 646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 159, 4, 661, 662, 663, 160, 161, 158, 664, 162, 56, 231, 169, 170, 172, 173, 665, 178, 179, 180, 666, 181, 182, 183, 667, 6, 184, 185, 186, 233, 234, 57, 235, 236, 668, 59, 185, 62, 67, 68, 69, 669, 670, 8, 9, 671, 672, 673, 674, 675, 676, 677, 678, 679, 680, 28, 29, 30, 681, 682, 683, 684, 685, 686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 205, 696, 697, 698, 699, 700, 701, 702, 703, 704, 187, 705, 188, 706, 707, 708, 189, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 24, 25, 26, 27, 731, 732, 733, 734, 735, 190, 736, 191, 737, 192, 210, 193, 738, 240, 739, 244, 740, 741, 194, 742, 63, 743, 744, 745, 746, 747, 225, 748, 195, 749, 750, 751, 752, 753, 754, 755, 756, 757, 196, 758, 759, 760, 761, 201, 762, 763, 764, 765, 766, 10, 767, 768, 769, 770, 771, 772, 773, 774, 70, 7, 202, 203, 775, 776, 777, 778, 779, 780, 781, 782, 783, 212, 64, 215, 217, 784, 71, 225, 226, 230, 1, 233, 234, 72, 235, 236, 237, 238, 239, 242, 247, 250, 251, 254, 255, 785, 289, 246, 786, 787, 0, 0, 58, 39, 788, 789, 790, 256, 263, 73, 264, 267, 74, 292, 791, 65, 792, 218, 220, 222, 228, 231, 243, 248, 793, 249, 238, 794, 241, 795, 796, 797, 798, 799, 40, 253, 75, 800, 801, 258, 259, 8, 802, 295, 266, 268, 803, 79, 804, 299, 805, 269, 270, 271, 272, 806, 807, 301, 808, 245, 809, 273, 274, 276, 810, 811, 247, 248, 812, 249, 813, 814, 250, 815, 816, 817, 818, 251, 819, 820, 66, 252, 253, 821, 822, 258, 254, 823, 824, 825, 826, 259, 827, 260, 828, 829, 830, 68, 261, 831, 262, 832, 833, 834, 835, 80, 277, 278, 836, 1, 81, 73, 82, 83, 77, 84, 78, 85, 79, 837, 279, 280, 281, 838, 839, 263, 840, 282, 841, 264, 842, 266, 843, 844, 88, 101, 58, 283, 284, 61, 308, 110, 268, 845, 63, 846, 269, 847, 848, 285, 311, 849, 850, 1, 64, 286, 287, 65, 2, 288, 86, 289, 290, 66, 291, 851, 310, 852, 853, 292, 80, 270, 854, 855, 293, 294, 295, 271, 856, 857, 272, 858, 859, 273, 860, 861, 275, 862, 87, 296, 297, 298, 86, 299, 300, 0, 276, 301, 302, 303, 304, 305, 282, 863, 864, 865, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 1, 866, 319, 320, 321, 322, 323, 324, 867, 325, 868, 869, 326, 870, 871, 327, 329, 872, 330, 328, 331, 334, 333, 335, 87, 89, 90, 91, 92, 93, 94, 95, 99, 100, 101, 102, 106, 110, 873, 336, 67, 111, 337, 338, 339, 340, 341, 874, 342, 343, 345, 347, 344, 346, 875, 349, 356, 358, 350, 348, 360, 352, 367, 370, 371, 372, 374, 876, 376, 353, 877, 277, 0, 878, 354, 879, 377, 880, 112, 881, 357, 882, 883, 884, 279, 280, 378, 359, 281, 361, 313, 362, 379, 885, 886, 363, 364, 366, 369, 286, 887, 375, 365, 381, 382, 384, 386, 114, 387, 68, 388, 888, 380, 383, 390, 392, 393, 889, 394, 890, 891, 892, 287, 395, 396, 398, 400, 893, 894, 895, 401, 896, 402, 403, 69, 399, 113, 404, 407, 406, 408, 409, 117, 118, 897, 410, 898, 899, 290, 411, 412, 413, 900, 405, 414, 416, 2, 901, 902, 424, 425, 426, 427, 89, 428, 903, 431, 430, 432, 433, 434, 435, 436, 90, 437, 439, 316, 440, 441, 318, 442, 904, 443, 417, 418, 905, 906, 419, 907, 908, 909, 910, 444, 911, 445, 11, 912, 913, 420, 446, 119, 120, 121, 447, 91, 914, 915, 916, 291, 92, 296, 917, 918, 454, 422, 919, 3, 920, 921, 922, 923, 924, 93, 925, 124, 926, 927, 928, 448, 929, 4, 930, 931, 450, 932, 933, 94, 6, 934, 935, 936, 126, 937, 938, 939, 940, 297, 941, 95, 97, 942, 943, 298, 944, 449, 455, 456, 451, 452, 453, 457, 70, 0, 458, 1, 459, 2, 460, 461, 71, 463, 99, 2, 72, 464, 465, 466, 468, 462, 467, 469, 470, 127, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 3, 300, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 501, 503, 504, 304, 505, 302, 506, 507, 509, 945, 129, 510, 511, 514, 4, 303, 508, 512, 515, 513, 5, 518, 946, 521, 516, 305, 309, 517, 519, 522, 523, 524, 525, 526, 6, 312, 527, 528, 530, 529, 531, 532, 533, 534, 535, 536, 537, 538, 539, 520, 947, 948, 540, 541, 949, 950, 951, 314, 542, 543, 3, 139, 141, 544, 952, 545, 1, 953, 954, 4, 547, 546, 142, 100, 12, 549, 955, 548, 550, 123, 73, 956, 957, 551, 552, 553, 958, 315, 959, 960, 316, 557, 961, 317, 7, 962, 963, 319, 964, 965, 966, 143, 559, 554, 555, 967, 556, 558, 968, 320, 969, 560, 323, 970, 563, 971, 321, 322, 564, 561, 562, 972, 973, 974, 975, 583, 976, 977, 978, 324, 979, 980, 144, 981, 0, 982, 983, 984, 327, 985, 986, 987, 988, 989, 990, 145, 101, 102, 103, 147, 148, 991, 150, 151, 153, 154, 992, 993, 104, 994, 995, 74, 996, 997, 325, 998, 584, 565, 566, 567, 568, 569, 570, 328, 999, 155, 1000, 1001, 5, 571, 591, 75, 156, 572, 580, 105, 124, 76, 77, 1002, 1003, 573, 574, 125, 575, 1004, 1005, 332, 1006, 331, 1007, 1008, 581, 1009, 594, 587, 1010, 588, 1011, 1012, 332, 106, 1013, 107, 595, 593, 596, 598, 599, 600, 601, 1014, 1015, 602, 603, 605, 576, 1016, 606, 1017, 1018, 607, 1019, 608, 1020, 577, 578, 579, 582, 609, 1021, 610, 157, 1022, 1023, 585, 611, 1024, 613, 612, 1025, 1026, 1027, 614, 586, 6, 7, 615, 616, 1028, 617, 618, 351, 1029, 1030, 1031, 344, 619, 1032, 368, 1033, 359, 1034, 620, 589, 1035, 1036, 590, 108, 597, 621, 622, 623, 624, 2, 1037, 1038, 1039, 126, 78, 592, 79, 625, 1040, 361, 626, 1041, 1042, 1043, 1044, 362, 628, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 1052, 631, 634, 1053, 636, 1054, 637, 1055, 633, 363, 1056, 635, 638, 1057, 641, 1058, 1059, 163, 1060, 1, 1061, 1062, 639, 642, 644, 645, 643, 109, 9, 646, 647, 164, 364, 13, 1063, 648, 1064, 1065, 1066, 1067, 366, 1068, 369, 1069, 165, 166, 649, 80, 1070, 1071, 1072, 1073, 1074, 652, 1075, 650, 1076, 653, 373, 651, 378, 654, 1077, 655, 110, 1078, 1079, 10, 656, 657, 658, 1080, 659, 660, 1081, 662, 1082, 670, 661, 390, 663, 111, 1083, 1084, 11, 1085, 672, 665, 379, 1086, 394, 1087, 667, 1088, 1089, 167, 168, 1090, 171, 1091, 397, 1092, 404, 406, 1093, 1094, 81, 669, 1095, 1096, 1097, 0, 1098, 1099, 1100, 1101, 1102, 1103, 671, 1104, 1105, 112, 408, 1106, 1107, 1108, 673, 674, 675, 82, 676, 1109, 677, 679, 1110, 680, 1111, 1112, 678, 1113, 1114, 1115, 1116, 174, 681, 682, 1117, 1118, 683, 684, 1119, 0, 1120, 1121, 1122, 175, 8, 176, 685, 686, 1123, 687, 177, 688, 689, 1124, 690, 1125, 193, 197, 1126, 407, 333, 1127, 691, 1128, 692, 1129, 693, 1130, 1131, 695, 694, 696, 1132, 12, 409, 1133, 697, 198, 1134, 698, 1135, 699, 410, 700, 411, 412, 1136, 413, 701, 1137, 1138, 335, 702, 703, 1139, 1, 1140, 1141, 415, 1142, 1143, 113, 1144, 115, 1145, 424, 1146, 425, 1147, 83, 3, 4, 704, 705, 1148, 127, 84, 426, 1149, 427, 706, 707, 1150, 708, 1151, 199, 709, 1152, 1153, 9, 200, 345, 710, 711, 712, 713, 714, 715, 128, 716, 1154, 428, 717, 117, 1155, 118, 1156, 1157, 1158, 204, 1159, 718, 13, 1160, 719, 720, 721, 1161, 722, 14, 723, 1162, 725, 726, 15, 17, 18, 1163, 727, 728, 729, 1164, 730, 205, 731, 1165, 1166, 732, 733, 1167, 724, 429, 734, 735, 338, 736, 737, 1168, 1169, 1170, 738, 739, 740, 741, 2, 129, 85, 119, 742, 743, 744, 1171, 1172, 745, 1173, 430, 1174, 341, 121, 124, 0, 125, 126, 746, 747, 206, 86, 87, 748, 749, 88, 750, 207, 89, 751, 1175, 752, 753, 754, 755, 756, 757, 758, 759, 760, 762, 1176, 761, 763, 1177, 764, 1178, 1179, 765, 127, 1180, 216, 187, 1181, 1182, 431, 766, 432, 1183, 767, 768, 1184, 129, 1185, 1186, 769, 1187, 19, 130, 433, 1188, 1189, 770, 771, 772, 8, 1190, 1191, 1192, 20, 434, 131, 1193, 773, 774, 1194, 435, 225, 226, 227, 2, 436, 437, 1195, 775, 1196, 1197, 776, 777, 90, 778, 230, 779, 782, 132, 783, 784, 785, 1198, 786, 787, 788, 439, 1199, 1200, 133, 1201, 1202, 1203, 1204, 789, 790, 1205, 791, 440, 1206, 1207, 1208, 234, 792, 793, 794, 346, 795, 233, 1209, 350, 796, 797, 1210, 441, 798, 799, 800, 353, 801, 9, 235, 802, 10, 11, 1211, 803, 804, 1212, 1213, 1214, 442, 1215, 459, 1216, 460, 1217, 1218, 469, 1219, 1220, 134, 1221, 135, 1222, 1223, 1224, 1225, 1226, 351, 236, 805, 1227, 354, 130, 472, 91, 355, 1228, 806, 367, 1229, 370, 807, 808, 809, 810, 811, 812, 813, 814, 1230, 131, 92, 815, 1231, 1232, 1233, 237, 238, 816, 817, 1234, 818, 819, 1235, 820, 821, 822, 1236, 1237, 823, 824, 826, 1238, 827, 828, 473, 10, 825, 11, 12, 1239, 1240, 829, 830, 831, 21, 22, 239, 832, 1241, 242, 1242, 93, 833, 1243, 834, 1244, 1245, 1246, 835, 1247, 836, 837, 1248, 838, 839, 840, 841, 842, 843, 474, 844, 1249, 136, 1250, 845, 13, 1251, 23, 846, 137, 1252, 1253, 1254, 1255, 1256, 475, 847, 14, 1257, 138, 479, 1258, 1259, 1260, 1261, 1262, 480, 851, 1263, 476, 1264, 481, 482, 1265, 1266, 483, 1267, 1268, 1269, 1270, 6, 14, 1271, 1272, 1273, 1274, 245, 1275, 848, 849, 850, 852, 1276, 853, 854, 368, 12, 246, 247, 1277, 855, 856, 13, 858, 372, 1278, 484, 485, 15, 1279, 17, 1280, 250, 1281, 1282, 486, 1283, 1284, 1285, 139, 142, 7, 8, 859, 860, 861, 857, 487, 862, 373, 1286, 1287, 488, 863, 14, 869, 377, 1288, 1289, 380, 251, 866, 1290, 94, 253, 254, 489, 490, 867, 870, 871, 1291, 872, 873, 868, 876, 877, 879, 882, 1292, 1293, 1294, 1295, 15, 883, 1296, 1297, 874, 875, 878, 1298, 1299, 375, 255, 256, 257, 1300, 884, 1301, 188, 1302, 1303, 24, 493, 1304, 1305, 1306, 1307, 494, 496, 880, 491, 1308, 1309, 885, 1310, 1311, 1312, 1313, 498, 499, 886, 500, 1314, 1315, 261, 190, 1316, 1317, 95, 887, 889, 1318, 0, 262, 890, 891, 501, 263, 1319, 892, 893, 894, 1320, 895, 1321, 1322, 896, 897, 898, 900, 901, 381, 1323, 1324, 904, 1325, 909, 1326, 502, 1327, 1328, 1329, 1330, 385, 387, 389, 1331, 96, 503, 504, 390, 902, 903, 905, 906, 907, 910, 911, 1332, 516, 36, 1333, 144, 145, 1334, 1335, 1336, 912, 923, 1337, 1338, 930, 1339, 913, 16, 915, 914, 924, 934, 1340, 916, 517, 1341, 1342, 918, 936, 937, 519, 1343, 1344, 522, 523, 919, 524, 1345, 1346, 146, 1347, 938, 525, 920, 526, 1348, 1349, 147, 1350, 527, 1351, 1352, 1353, 148, 921, 1354, 528, 925, 1355, 922, 392, 927, 529, 939, 941, 928, 929, 931, 530, 1356, 391, 393, 1357, 932, 397, 400, 933, 1358, 150, 151, 153, 1359, 1360, 945, 942, 946, 947, 948, 943, 949, 1361, 1362, 1363, 1364, 935, 1365, 944, 1366, 531, 1367, 1368, 156, 1369, 1370, 25, 1371, 157, 1372, 1373, 26, 191, 950, 1374, 2, 1, 1375, 951, 955, 953, 402, 408, 415, 417, 532, 533, 954, 418, 1376, 1377, 1378, 264, 265, 1379, 956, 957, 1380, 959, 1381, 960, 961, 962, 963, 964, 267, 1382, 1383, 27, 534, 1384, 1385, 28, 535, 1386, 1387, 275, 160, 966, 967, 968, 419, 1388, 420, 969, 283, 284, 285, 536, 537, 286, 287, 288, 971, 1389, 972, 1390, 973, 974, 538, 1391, 1392, 539, 545, 1393, 1394, 549, 976, 16, 977, 544, 551, 552, 553, 1395, 1396, 978, 979, 980, 289, 290, 1397, 557, 1398, 1399, 584, 1400, 291, 421, 1401, 1402, 1403, 981, 982, 1404, 1405, 983 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 8, 1, 21, 2, 22, 23, 24, 25, 26, 2, 2, 6, 27, 0, 28, 29, 30, 31, 23, 32, 7, 33, 34, 0, 35, 30, 36, 37, 38, 39, 40, 36, 6, 9, 41, 14, 42, 43, 44, 32, 45, 46, 47, 48, 49, 18, 50, 38, 51, 23, 1, 51, 52, 8, 53, 30, 54, 55, 34, 56, 41, 57, 58, 59, 60, 40, 61, 62, 0, 63, 64, 65, 2, 66, 3, 67, 68, 42, 47, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 38, 80, 81, 40, 56, 82, 47, 83, 84, 6, 85, 71, 86, 68, 87, 88, 89, 90, 42, 3, 91, 0, 92, 93, 2, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 46, 58, 107, 108, 109, 46, 69, 110, 111, 70, 112, 72, 4, 113, 2, 52, 114, 115, 9, 116, 117, 2, 118, 81, 72, 73, 119, 120, 121, 122, 123, 3, 124, 125, 126, 127, 128, 129, 130, 2, 131, 6, 74, 4, 132, 133, 75, 90, 134, 135, 136, 100, 137, 106, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 91, 152, 2, 107, 83, 153, 154, 155, 1, 156, 3, 157, 158, 0, 159, 160, 161, 162, 163, 5, 4, 164, 165, 0, 166 };

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
            final int compressedBytes = 3321;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXbtvHscR31ufmBWRwCtCDyLVUZAVBlAhu3HCRkc5juUAQQ" +
                "hKBGIEhIEUiQpWgSuBxZIWAjIVCxUqP6gQ4M5/AmGoMFypiBFD" +
                "f03u8T3usbu/2Z27j/TZokQOd3d2dt6zc9+79/+xd/Obe1efXD" +
                "m69+0vXu2fPzj8KH/++Y/XzOMftk7uiHd/ubt38/Tp59evHB0U" +
                "8L0Svv38zz9eO3v8robf2bt1uvnXFyX8k1d7bz4+3K3Gnz3+ac" +
                "sMMP79uwA//3hZ/KeFSET5rBR/1nfuF1+NELmUShDw4+6fC+fh" +
                "Vz2Z8DxJ9we6+Q0Xv/Nf3fnDlaNNdV2Ie99uH984X9ncFUZ9Wi" +
                "ySC7ME/hkAfvN081G1/nvF+h8frrX5s4Kr6yV/NuHm8fdDnB/m" +
                "/3/u3zo9+Nt/P/j64NVnr7988+Dw75/8e/9/5fhnBXx1VdxYUw" +
                "WjXznKhcxy8eFXOn/+6Vlidk+2yvNlytfY51PyT3q0uf6i5J/k" +
                "+Mablc21Ifnn16uyoM+jpKBPoR8K+vz+q/v58+2SPv/ZUmIJ8j" +
                "s+fe38uVvxJ9IP4+t3JD9M+iP+ZvJ/zT+qxT/bLf4B+lmJVJRf" +
                "p0/xjZbTf+dCjC5fsnnYeeNr8chlnD+As/kTnu9lt//jwnuP6d" +
                "KXS59LTl82fzDlA6zf1g/pzD/Lm/zv809+WOjXA5v+d+vf3aX4" +
                "p+P7R8B/Y+5vdih9/zkh6ae1wjPTC4VbBCtPdPG1jE+I+tcPn5" +
                "7/I5f9H1u/w/iL7X+S/Mc6/kiq+KPjP/5m6j8fHUzm/nO9v2cz" +
                "/jqt8ZvU+N0e1j8eWz/6x8tcptbz0dtr5fmw4f3zz5rnj8Zb4t" +
                "Nu/Jr64lcER/Epgn9Xw+v4ZLuKT+ZwOQCcGz9z5w8ZP6nHPwzB" +
                "D+dnUPyN/PuFfZvU42UzvuDGx2h8BFy01h85Pp6Nd+vH5azvGt" +
                "+ET2q4bMJt8Xsr/mLGf2j+ywjPx50/a+VHXP7j2cK/8OUfKvv0" +
                "zUGL/ts1fyzsnwdecEGK8pd2+Z/5v5TzVyPzV2v+rDk/yg9w4W" +
                "35fF3J58OpfD7ryW8fzp2/O140xou4/QnO+r35bUFx3vYf/P4l" +
                "iL9VLvBj4G9ox89Rfgc+yg/mxq9jw23n36b/0vET8fg74ztnfN" +
                "+EW/UfU/+i/IE7Pq7/dvtnuyT/zjK/aMff/vgHxd/TieQiB5m2" +
                "RA2NHxve2b/sqoKI+UXI+tz8QnO8jT8b/rFY+M+VfXy5JQjxpZ" +
                "C/9MUXCA7jZ+b8q39y7q/y/7n2BY1vwic1vOVfB5uiHjzHRgSO" +
                "9zySZ7/g+irCJjdSFlL5+XMurlrJ5rRq8Q/ZQsR0TH3lP2SL30" +
                "g7yKvciLRUX/V4ZcR6+fdmNUbDbVX46xJ/WeP/ssRfF791PsU/" +
                "ln7h5yPt4730gfgj/4vNP3l3Ih3CX0i/LeRDV7uNw6/LP3r6iw" +
                "rj16Xvuo2+Nv4008ND+/MrgJbBq77ZmQpPbucXrnyj8cZ6vil/" +
                "fhWpf4XHRxqCPjmQWXC+hPgn8emDUP50yr9CoZSDvjP9LRz6O4" +
                "fnnzrWTSn0V65o0IzD3/gJ0m9s/ATUr7K+QtC1DxkNfmH20QD9" +
                "0bUvHDhD/2P/G+n3gGxGOP0gfkpK3/nZ/bem/rAtqwP4F+4P8e" +
                "9A/kmT+I0UnHKNd+j/UP891P8yVP0SGV+YMPsVzN/d+VH9E/A3" +
                "vp+QpJz6Kbf+iM5/Q/8kC52T3tYrMvld8b0xich0psQkE9uCmB" +
                "+d+7+0888D8qdR57OQf279kMboyrPd3C+FgL5j379C9ccm3Jaf" +
                "WBZ+rvpkRT+STnfwJ5m/HffzmPXLvv564ogvHayG+AfUBxF+zv" +
                "yx7s/fyh8S54+oL/bvR/rGw/N96NVfOP+d+8O7IPm+fPW/iPEi" +
                "bDzyX5hZLHj+uo12J34Mxj9Qf3zXzA937PvsfpIPjvPHPPsS4b" +
                "+Krv/Cqs/SsgIdrtNkOL8+GXj+PafXn7/g1q8N8M+g/nQrN00b" +
                "n7vXJ+Wfcq+cB94PptTnRGD9T/jqk5b6pS3NNINLy/gLhQ9Qv/" +
                "XSP+L8wuJbqDFi6g/N/B7TfyTr36b8KLL+ZdQ/X3buR1LholMf" +
                "RvVZr/9rGY/gQfVjVP+1wEUTzq9v8+rHTf7Q1vhG+OuP0LPy8x" +
                "e6/1Lmz8oZNooVe/mzlfn9xvfEB674gydf1Pwe0f5Y/Isc+B/U" +
                "8cT8a2j9T1Dtq4yqLxXr8Pa/WQ8s+MOkpsDbSK3K9FNSbOOmpT" +
                "5kAg8cjj/346fyhnGzhUjo/Jj0EW/b46e5DHrZJrewVEB+P7y+" +
                "eD8k/8pdf56/LBa35S839Pc1XE3hooaLLlzY4UI8qjyQ6vRVuV" +
                "Ezs//ZwlGXc/5a6Fei/kXzl/tPffsH+AP8EH0Q/sh+Ifx6cBN2" +
                "PgW8os/tYo/e+ZV/vOyNN6T9Y/7JvedLp0/c/mb4x/HnEPLT3b" +
                "/oyw8Jv1Urfmg8oh+SP6RfkP8E7fMJ0g9M/0GhFNkO0L87Yfo5" +
                "FI7qi1z7Ce/voPmp68fBEX9w63vlHHq2/ux+lmjG50H5l4j8Ye" +
                "D8BpyvCTx/pn9NqI977wfg/oyY/IMYcP9M+jDnx/mfkfGD98/y" +
                "kekPtgPj951pjkxKe3ww9vnD+Lr8v8BvtS0fqUmSIfBjywfYH4" +
                "pPrUldDeJP+1XDuPhsfv72+FCR4qsU1E9G2x++f0I63zSaP9TI" +
                "/hc4SNS/w7XP9PstQ8bnF5G/Gsk/sd7PC7HPnfoemf8mpPwsrg" +
                "/C/K+pvRXZ0s+qWt+Dn6zHm3qMKgedLeoT+fLsP9c+DOzfcOVn" +
                "YPnCbiV5/iSs4jaMfziE/TYimfz2+Gnxoy/MmkleTPRE/XEzS8" +
                "3KAPFrKxf6c/TPKfxj+f08XVJ8eF6SNc0q49DEJwuEyzj4PL6z" +
                "16/w+rlo6df+/F3/SeP6jA6gD6Lv2/b4uf9I3T+MX732B9r/Qn" +
                "6LFSYbopBfcTUrZjszhXuYb/rWD47vUX8HAy4ixuv49Un5kQD/" +
                "iXT/cWj5H06/4f43GB8EPBkjf+Oq7y0rP2NfH/vnIH97Io4zn3" +
                "8rxLUKlJZHVMQnWYnRtk6MPlmn4V/pr/b9uRb9ANzls5ul0F+1" +
                "2iszy0tvbOcT4J9w/V+ufPDtRwxc0uMj6/nIofRngFsul5LfNU" +
                "z4kvNvhPiWZX/4/mlg/oSbXx7Yvhf0O+bU9wesT0gmfND4eGHN" +
                "mf4Nru+A/E9wfioMjoQY9hcx/UPm+4e58on63wLvp4+Rn2Od79" +
                "jnE9E/1hJROJ5M/9n96O5yD5nnd3cfvJ9tn9V/dqnky9LfhO0H" +
                "en8k4O9A+hiO/Nn2p/3+F6V+1Iz8uvlzOIsff3j/W1nevzO931" +
                "E+qH+MYt9983P1H8KPrR8HrR+9jrPv4fyRD2dfGPtXgq/f+PoV" +
                "fX4PiN+nnDs/gOD+K//8zPczEd5fwurPEsz+K9jfBc4f94flck" +
                "pSV37mwzo/1CPmSf0tGn8R9RU6feH9vJH9T25/GNc/Rd03CD96" +
                "/53nfkvqhm/ol9X9lEzU91Oy+f2UbHp/mDC/F+7HH97fIOf3pD" +
                "d/E/1+Lu76SPjI79+T1Pz/fXv+X9LwN5GM7ILD/FWM/Vs8+PMV" +
                "qfPb6c/OD5H9OwXy20R4pHyQ3/8Yyr/E/Lm7PjyyfJHln0sflI" +
                "tD9fPB9E/3/FH/Cdg/z/+gy9c46+P53xLlB8n/qjX/uaFPpvY3" +
                "rfpvsqp/pyR+aX9x/BfQnxgV/6P5e/1D/f4sVn7Y1X/luT/bYn" +
                "7cv+X3j/zyoWn3D3n1h9D8gPZph8S5P8/7u1PP+M7jgrvGk/o7" +
                "q/63Lv3b+GO4s/8O6W9Sf97lhysm/Ge6f6vyG16+UX+nhz+B/M" +
                "L+bAb/avb+Yf8pnz5e+HD2x26f+fGvtsRXzfwNgBP1M/HTloI/" +
                "f5ni4vr2j+uboP8Wn9+x8vFvd35L/Hnr9OkXFX6fFfg9OPxXJz" +
                "6UnPgO798Q4+fI+DQqPre9Xzou/l/654t3GWnk/MMA9XHe/TH2" +
                "/VZqfmAk+qlFrG37fBna/O7xF91fhT/fyV1/lGKg/uH04vJz7v" +
                "tXk3n80u+f1WX/bGcH0qef3PD5/J38zqw/170+DU5bv6+/UzHM" +
                "/gajj2t/pPqDd37hud9P9D/i+w/Y/hszf6pqFZ/lC/4vkc2q/k" +
                "Ijev3N0fklBfxXAfxT4fBPqf5fLFz7zTbqv4b+kd+/g/3ZTv+G" +
                "6B9x+Z+W/w3sbxd0+3DB9R1CfZCZf+PhN7Z/i+7nEN7vjPSH//" +
                "3P9PpCYP+3M39JGy8za3yeAP8nccY3KD9a28er1framR9IXPqt" +
                "9/7rhv+ZQsGz9D9OqOfj6P+c0ObH+Qc0v6O+Fbp+M75v0R/RB8" +
                "C1oz90QssfkwPk6PyRHz/IPyT/zW3W2P0XucX4a0lfH3w+H2H8" +
                "qPCx/Vum/zvA/YVx+2cQ/P+DsoAd");
            
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
            final int compressedBytes = 2884;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb+PXLcR5mPenZ8OCUwdIllwxQRCsIUK240cNX6nWLZcGB" +
                "ZOFhAXBly6UBW4MlTwhCtOqVSkULlQIUCd/oQNkOKgSo0B4/4a" +
                "v1+3u2+X5DfkkLsXWVv45J0l33A4HM58w+ETlXgsSim0qET7qY" +
                "y41v6diGnzXyWGr52fs2/+9uDqkx+/+vPO0cNX7z1/MPvs0ScH" +
                "x1//cvnp/bNbJ9c9LY2/Yyod8LdMrywEyD93/AH8vd30yj6tqP" +
                "36R/0+5RdJn3+kXYS522+bf0w3dvunSfZvA/Ru4HqxfobB67dC" +
                "P8++ud7Yz0nV2M8br/7Q2M+bj/br469+uWwOT2+Z6/Pxj+c/5f" +
                "O3bP+t+r0Ber2Q/wdPJv/8T8v/P54/+N/NR4ed/J/e/7WVP6Jb" +
                "7LsC9j+Mjte/f30ksn96rb2m7U/oJ3D8s/bBpev5cH7Em6H9rp" +
                "1/sn3cs/uHQL6Yv5a7FOsb+ReZ6GT/BLWXVP0I5Z+4v0X3T9yf" +
                "uPLn+gfb1g/X+uDGD/V8BcXtT/72YmmFxq1P0D/i//2G/5c3Ln" +
                "3b+gfvPf++5b/3D+6/bvmn70+5928Ytsoo/WXuXwniU4QPeOmy" +
                "ui2UEoVQUnZb0LN7jWFQpt3XZNNEVrKnt+Nr6dfGdMLz/fbNur" +
                "+o8OmJnx8//6Gwx7x/k5GO1ILR/4UY3++IHq5/yhoouP1X5F+E" +
                "Pd+6HKVPNS9335RF63PU7WNrcaAKo06uJcAPk+8PF339rHy9L8" +
                "SOWkxDY6y/bYx6+6u6nxVufFtRxrLU3vTfVansM7t/XntSfOR9" +
                "fg3xFX//zPZ4/bPwtTz5BUVulgffURF0+wKi6w8afBx/wc83af" +
                "k/e9+FH94/deMLKsP8yNj9EXyKkoYvuD5c+0AYv1W+Jol8Zn+6" +
                "/vnOUTO/Qtx4dfD4ymx3cihMdUe1AyPYr+z2EcSnQ3wbzx+Kfw" +
                "G9lV95NLnWya/o5Lc/kh+Mn3jyY+N/YrvyW++/DHr+h3vyyn51" +
                "txA7R0182/inf//po4Pjg6eFOfz3rQrTkf40+9P3V18+HPF/0M" +
                "v3zM5/4vZc+VadQJetmJJB8EmIfr7o9O/2oH8/U+wH4g+sH/h8" +
                "sv87CMAZtsTtT4N9qJz2wboleeaHhH8y2of3LzfKX8z+mtE/Qf" +
                "Nr08/4+OBFsP+QPT5h2j9sH8/xhzVjdOJbLTiSNzT8BdJJgY5c" +
                "pDNLtbLbFatPGrw7M/pWReYHAH84/oPzi+I3gE8Fync9vx0ZH4" +
                "fgYzHxLw8fzoYfbwQfDpAP0F8uPhaHb4TkL3L771z7FEMPWd+Z" +
                "40Nm/MTHB/z97+2JJr5oOtk5ahSyiS8+/knVx3fa+OLklk2Xgv" +
                "3LNPisXtNfnQyfqbtNWK832aXEl6h/kxc/CsJ/9fpvYX7Wn3+F" +
                "+dns6z8Wf6LOD3f+tt0e0PW5E6pNadp5k6JSuuq8uqvL+ELk+a" +
                "SLPv5qxhsfks+kb1iISS9fI9VIvjh/6u0/Ah8PW19wf3mTxL67" +
                "6H9Rv8pm/ZZ/bYxL8Wnz/8Y0qtoKcKrFAaX9s669Fruyba9J7R" +
                "nx/zv6ZulAP0n6U1L8f+no/3Tcv+j7F0P/26Yj/xG2F3c7+bSh" +
                "S9tF0cmli3/03H6M6GJEx/0T4pcyPj9El5+MlX/Hn6u9EKde+X" +
                "Hln4B/8Pya9fwk69O7/yLTgfx3heIv4vmk2PoBEB9R8B9Pfozt" +
                "n1f+gXDP1wpxL4xu889Y+Dh6PtV/QcRQ/0R3+s+Xb+74MiT+jM" +
                "A3BvuTL36cAf5nEeMbyadY+lMP8dYSgGjDx0Pw+Rj/PeD8RZb5" +
                "p9O5508gfonzzyx8bTv4Z8D4M+O7dZtf/K+umiV8Qxfmymy32m" +
                "++vtP9Igk+TMXvM57v55xvSkwvIuKnyoYvlKsjlM78R9XRV+q7" +
                "SlMUI/reEl0t6DR8tQTyjaa3+YEPnvz4XaffXzb6/dmjf+H8Yc" +
                "L1i/E3//4I8SVbhKusHlRGut9/87XH9Ttd/ercTputjG+b8huP" +
                "f709oo/NwbB+dSb+g/unxo+q95+D40drfisg/qrQ/HD7t+HLCe" +
                "+36O1TtPxhew/CRuO/BgJN4D+UPnxviM9Uj0/08ZmYx2fYf0DP" +
                "V+d/7Of3YP4XyCcqvhJ0/sjxXb//X+qerwj1mdMBvyOs73LV/x" +
                "n5J2D8ish/5PkcF1327QnnP83IXg72s+r6H023V3/EGn80/bff" +
                "77A+f87+o56fdP4i53c68i/W79eYptEPeH431fjs93PEnc9cfF" +
                "739TV3XffzvF7U3zy00TG+ZbWfMgBfCvuYQDq3vi0FPpYVX9g2" +
                "PhQ2UQVpkt39o/ZF8vbc8Qe2N8EiYT7//10/N4AfeenmAo7vHX" +
                "1jdN1qQDG99PiH5h/fGSWK46maVl9MRGmSnE+MxTffzc/m90cd" +
                "Mb9IPzLb54q6Ebnsp/9+Oz7dI9uNrK+Z//49cv8u/sH48fm+MX" +
                "9z/Epn1L8Q+Zot05n86U4C00L80KIjrVCNUbqqJ1T9S8D/hT4f" +
                "ve3+ufRY+zjCP8ts9jv3/cfZ9x9q/MU4f+pZH/h8FcV+W8ZTl+" +
                "fyZ92vh+VTW/DdFfmXofFXcNheiqAPvf6Oq1/VSGR6+eeGNNDc" +
                "95sz6cHnA8n413RhH5Kcz3Sej2TanwT4qvDh8zW3f8v6WD6/wL" +
                "0/RTDP1wH9y7w+2fNPuR+UtL4cRGv/it5/lv1VBNh33vwQ6uuJ" +
                "+W80f3tu+Xr2b3R/FPv8YPb7h/zrm2Qfah9/jvvTng75G8AfvL" +
                "+IeT8SvJ+Def8WIT/LvJ8oMP/npgfc/xPAH6yPT+BfZ+QP5zf9" +
                "8sXnY+H9LwT/UC/+Wa5s/tg/LMXifha5ej9LXP7zrbr/OJF84u" +
                "oPQ+/vgPGZiI3PYvHXVPGHM2pC9yMFBZKW9RV5P/x5/Mzzf8rx" +
                "lNZLVENxIbn1P1LIP6pzHvr47qP+2XUX3+W+Pyl3/QbGLxP0z2" +
                "kf5L9rvBYN0T8zh6T7dbny2zYd18/l1U8+nRrfuu6neIPwEcOz" +
                "f2+s/FPxF1jfSz3fpID9zUSP57+X31J9tVyuj3adX43Cnwj4hR" +
                "z5FzHj23OML0H9tmegSepfy1X/yu0/pjnfFYBfsvFZrv3x11ez" +
                "5d/4X5VXP5n116H9r+PXkoOfpfNPnPafeb40MH4ylLm46PHfkr" +
                "hzv/8T1ncjR46Kr8e15/r3XP8X+19c/4zKv10+XX3y0ag+uRmo" +
                "mtcn2+mL+mX6/bEOfBr7Z4959Se1I3+0ukNG1tfC+l5/e1x/i9" +
                "vT8JVc78dh7r+U/H3pjw/aLUzXi/btz3VXX2Rw/SAB3/L2jwwp" +
                "5G+tvfJ7cC79Bf67s37s3li/Av0zOv5OPSCRIr4Myf/ZNYyOj/" +
                "rtC/v9Biv5lXIpv1Kn2N/Y7zeJsT+B+Q/S/RJ2/4BUX7jh+kaf" +
                "fxxevzmWXxHqPzuf78PXQ/Bxmn+C6ws9+W2afXLFP0nwBY8C++" +
                "sn5/RdF51ZH0p9fmR9J4rv8P1vjvPVU7p+sfTTvn8FxJ+8BNom" +
                "5MPy7zL7hyni+5Kxv4z211JYNz3v+RL2/huQH4nBZ2Lf76FBHE" +
                "6M3wPwh8pCIPkXJUO+XXygFz/1nD+oovJ/see/aPsb5f4yR37s" +
                "lJYfs+XnRMDzmfLZWP0sEkSu99OC+iKcn/K2R/zh90fmxb9QfU" +
                "eS+IKVH0+lv2h/tucXZXW7z38oKbspfrae/9C+XQ/lT0789icu" +
                "/xKSH4LxuxxEIqPWN35/lbV/+vlBnv2Jf7+RIfoPzPib6Z/keT" +
                "86PX+K8o/bP/+S9/3tAe/HkFH+J/t+Fv/jufg2Pz/Jyw/h+DQv" +
                "PpfCf/TLD7x/m6K/HvmTzv/XyL93f7jn8xE+K34DPLllwg==");
            
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
            final int rows = 787;
            final int cols = 8;
            final int compressedBytes = 1893;
            final int uncompressedBytes = 25185;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXDFvHUUQnjvWZomE2FgJWFRnZCEXLhBNwE3OQIRSICwHS/" +
                "APUlAhKuRibblIUrlIQWmloPZPcJEipZtIkX8N7+694Hf3dveb" +
                "3dm1n3hNlEx2b3d2Z+abb+aOdEvhnxn+1Y7Emrjj67Tx4fVdfL" +
                "z5gzra0veIts+r4/sXq1trZPUj0w20mxnG+342qCakxvD+DF8O" +
                "9KeIqvFMZrp6Gx5pWfp3P98sjvfvv57J68T9W1I1NbOFaEvr3Z" +
                "9bk3+Z/j/3/Jo9fnh9eb+5/aP7rSnqV3kl7vWtEa3MWWBN5pdO" +
                "E7bTa+09dkPh67E8cjVUSTt3Oyzn/lz9/OXBp8+fPr63cvT7+Y" +
                "evDi4eHn69e/LT27unT652nmH/4da/YbuNq082J8+f+J+Vo+3z" +
                "DybPf3C41p48fnvX7r/bcfmfWP+J5cfu+3/Gu/9g/Ib5u54ckm" +
                "pota6+IWqsragxjaamoV3GBfbaZ2h9JuJ8LsNytn3C869DTjrw" +
                "fNH+N8ybXv9f0Cp1+t+gqf6Jznj6l8r/W/+dIuvfMO+GcjvaHz" +
                "g/vn5qENjrgf/UHvv4qN+/ud5/kv0Z//MX4xu4yAh/7IX9D+f+" +
                "qwj/6BWm2Xfn3z97/vS3l51///HVweuHh38M/DuK/xhfzPaXiN" +
                "/E4z36aNUIMlTTyRpWgPWLbWb8KsRPOH6j+7E5uR9bv/b34/vJ" +
                "/XhwuN/H39Mns/jLyh/WX87yh9c4fzApasdypFPfeEMDfOxSu0" +
                "q3v7G8YiNdO3o+2oh0fXnmT9pfv8Paef/x/WStT4G7guVaKKfb" +
                "ku8N9Wtj8V3rOR92/AXxIzz+8zv1/TWtK1o5IlptWvr2z692T3" +
                "ZPK7v/YkfHrJ/A+t323/m3lff8yG7Pj+y7/VuNQWE7DAi9bJF/" +
                "Mjz8H4q/MfgB8huYH1Bj+/fjnyoYf/7p7fu7mX3/1es3fD6u8Q" +
                "P/4MTfxrv+9PzHB5BRfAHnJ8ZnjPgVlJtO0OWvI3x/Fowlmosv" +
                "cH7O5Kd8+kf2Ac4H+y+kn6Bcir/c5zef3834BzPN76b8Azn4hz" +
                "qRgm45/jeAX2XxHY3nz08e/5WC3xF/bPjuI3d+kT1/cfHbUn7R" +
                "5F9fIj/Y3R8PP/kmBz8J9ZPsH7n8yaXTP9EsUcbx72I4nkbjsf" +
                "7D/A+WJ8S/iPpFbrkdG6fQP7D5s9TnZ64f2FL6d99vfv0H5TfI" +
                "rBP5UTg/kBfm56TxlYVvlAQfIP8iwyeQ39L96vUcFDO1O93z2W" +
                "94fGJ/AaS9I/LfUH8A7B/oJ2qu51SjWyyvD6t+7c37PSgGfx7h" +
                "f4TPx/0HbRR96XCk4f0n9ZcE8aNZpvgTIa/LzC+MXxH9K+Ss76" +
                "cQ6RH8arb+jZzl1Qh+O6m/BdfH+f71EsT/8PN99V19Xd+V80PB" +
                "56fVn3Wm+jOcn92fQv76hHLl/5Y53kjzmwT7iazv8OrPQgMtFl" +
                "+D8/Prn2n8TCx/FO1/sJx54bz+71jm/0YWYmP58XY2Hvg3M+2P" +
                "WrRvw7zfBPK3YP1DJfNHXv3Zm6lvjOy7Yuav+fh1WD+Q8evJcs" +
                "AP1dPx0vyUvf9k/FS2fgTvZ2H9M+ozwfqJtP+dxZ8I+AOU3+P6" +
                "Mp+/1g4B5pfbYH7Ojq/Gh++ezfpf1bT+RNP6E/X1JwP5ASk/nl" +
                "Zfypk/p/C7mJ/X2fh5MP8i4jXR+BrhwwA/X+uajOmmnci7Ja7v" +
                "TRZmbLevWg/4k3B+75Wj9YfHQ35Z+H4PlmeKL5L8XgXtJyL+5O" +
                "8/lPO3gF+F8gjKqIh/kfJXKfykycdP5rKf27P/2Pxu8EurX5XI" +
                "vxG/mEofzJ9fwzzfOiL/A/wd4ucAvkX8FOv9CgXsMyCXPj+dXz" +
                "zj+W/8/oeQvzOJ5xfLjwL/mOp/s/X3B5ybktjnUF5F8puQ3wH6" +
                "x/2z2c43rX/wlvkLnD+3RfuDGfwBikXh/skb5X9c/Nxy67d0fz" +
                "rCz2n8OGXznzH+UYvxSS78nbO+gfJ3Yf4P+wvb4P1G+Ts7v0/X" +
                "v+z7CsB+ET4X19cxvskxXgXxjUrPvzO8n89hH0L9PzK5OD9i56" +
                "eJ+yt9/9sM7+cK8HNU/t+A/9ss2LL4+xNADueX4hvgn2D+KcSv" +
                "6fPz1i/O/zjzC/LvMu83zEl7/9royRK2m8pO8J2ehBx61M/g9q" +
                "/x3w8QvR/Wlq0/Ljn+Ll/fR++X/d/3D+KnsH4Zza9YTvwe9g+F" +
                "3x9C+ADg98z+NU9/box/RvLS9Q90vwrjz8L92bh+APLrwvgIyn" +
                "P0H4u+34HqB9L4ifBb2f5f7B9Z/I4K4HcO/6r8+EqGD249fgn5" +
                "52VfH+Tnc/B3LHzjdgX893cT+2+k9eHS/AX+/iiXnyjT/3Lb75" +
                "ck9f9FvR/Csq96kN/Gv7/iG5+vPp0zf4yqHy13/a7w89O+7xaD" +
                "v6T1A4hPpfyqkP8U+oek+lCMnHB9XiAX83/y709zzkfF8c9LxC" +
                "/dxPMD+oP91dLvcxSWo/wS5588+/fGV45cYH+3n7/I5mf4x6B/" +
                "lp6v/PxR/pFBv6Hn/wsBL/Ds");
            
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
            final int compressedBytes = 4984;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWw2UVdV1foAjI4WglDRRNNIkkqWEFWMi6iptDu/NsxPsUl" +
                "LTxlQURF0RCX+pRRZKM/eee98w7w1rYbpEpsuVADqupD/Rkn9Z" +
                "saYxxnZ1NDZtdWZQgxrATpQUAmJIavc5++y79z7vDjJnnbPP/v" +
                "v2dw73vXvefY/6WH3Mfq0+5lrzpuyqZFXyddTAPtQzvT62+c5K" +
                "hSx9PfUx1pKH62NmEuR118fSufWxfIKzpje2jsLYgn4pRdtddk" +
                "XA/GraUak0LmGcPHdjz+K021nSiRX/RzUwTupOq1QWjvU8q23a" +
                "D4jvo1wenUzf4GiJnf2LH9+uVHqfqFTsbvTkFY0a84AVDdqHCK" +
                "dSMd2m2x5zI8gT0L+fVVCD+VBy3HRn99j9ZGla0M8iLfkWjKeZ" +
                "7sYS1PMJkPMD091/PUTNMLA/9nv2iEf6TvalgPlYszPrtCsJAy" +
                "K3Bnnc/tru2Xym/T/7djaN/fYJ6P/GOmhvwn4eTI5IW9Yh/I9X" +
                "whqyiaB9188m2X/2vift7SLyLeiP4rz3MR/3tune/HH742w+eA" +
                "7DmordyKbKej77uP0hjD+yT2Ww8vQKt2Kzy+zCEaX9PmlOT7ea" +
                "XfnTzoLW1ukcHdpEs6v/L9AK+7kL9tPbG6Me7XsYbb/TNxvt9j" +
                "H3r9o1hRFgP0M12P89KN0f+WE/d8F+iprO1zcr+VXMvch4nHTP" +
                "4bvIH/bT+Z50tdWKH8X55ss9m7cB++ewnyasqSKr6jqA+UPosJ" +
                "+0Q9BXmpXZS24E+Rr0/8z+BzWY/8LOMisbx7KjheUnZiXmeO2n" +
                "ZuXjoGX/HfQx6IehP2NWVm8GOZodCRWebm4NMc9VKtnexscIA/" +
                "BvDZ4Xs59lr2YvZ29k/5U9z/7sdei/Yh204eznfV3Jm8o2IuYv" +
                "ZPtx5qojA6h60Pt+6ZhRc77sWZ5TBkTXZDb7ZfP/6iuzQ9n/Ui" +
                "T01KQ4ovQxKenJMZNu/g3aOYL8ZLEH2UPe7DaJVqk070F7PtXZ" +
                "GlsZofdMQsLo4vpMJau4Zt/VyW9i7u0ZzIGZZJ+LVywRKDa7Jq" +
                "6s94oZ5535FLH6aWYajk6GlUzDZofsLSAH2eJmWpNWQnOyulzb" +
                "zCDG5lP9/ehrAmeQq7dziGtSZN92ytNcGIlzeXSSmLVhD6o9GG" +
                "xfrWZBlrCfFN1v+vMPuxHkRdDPzn8fNZh/wNZMf1+Hi8JInlFz" +
                "Wj6LPfkctFeXee3CUOE9rb8JmOfCv+jshZcxQu9cQsrnIkp+jm" +
                "cqKrTX7Hs2/SM3t9WAez778/drvgH1PO+7oLqM8TzH9/KcM7Kb" +
                "48rsZ4u/Pj+Yf4gi4RX9l8lp2ZN8pnB/yR3J9OSt5HfhvfZTyb" +
                "v7bgHL78HpaJb3rQfrF0G7AOZ3Jh+ym7z1Iugfhj7PLvb6xb0d" +
                "hJdcmlxu/7r1cEX8Nc7zniuT7uSPe3cnn0yuKmpv4PNSsiS5IV" +
                "8AtmXeszz5XHJbMiHxJ6rqs2ktRE1LzsSMZCb099oeiP0rd15K" +
                "zod9+ZHPvVDUPje5JMRfEWouSExSrai/LOH98DHXEK98Z8i+Xu" +
                "5acjN02CmzxqzBEaX/11xDerrUrGkelxaOZg3eP5Xueq9BLKrQ" +
                "eoRi/P39GcKAyNfYky4rXm9rymtSZPPW9Fpt0/44l5nI2hI73S" +
                "H3IGuUrVY3xCS+oa8wK3BE6X0rSLe5WdF8Slo4mjWdj73+HGJR" +
                "BY7x+zlEGIA/XXqK/VxRXpMimw+lN2mb9se5zETWltiwn2IPsj" +
                "1lq9UNMYlv6LnJcUTpfTnpttfkrblo5wjyS4vsrvX+o0Rj1PCa" +
                "m80IzQulh6RkUVaz+pN0Xcy9PYM5MJPG+fGKcQ77KTKyH8SV9V" +
                "4xY+btetecrjk4ovRXzxzS7d93zWnNQztHkF9aZHdt839INEYN" +
                "12fOCM1LlWcOMYwr6JrVZ9I7Y+7tGcyBmbjaesU4T3fIjOypuL" +
                "LeK2bMvF032812HFH6vd5Ouv0ns721DO0cQX5pkd21vmslGqOG" +
                "6/MRRmhulB6SkkVZzdaX000x9/YM5sBM7CPxiiUCxWZDcWW9V8" +
                "yYefu+1CzFEaX3LSW9PmyWNl+UFo5mTedjb85ALKrAMf6uOEwY" +
                "EPkN6Sn4LS2vSZGtB9I+bdP+OJeZyNoxe96D7ETZanVDTOIb+i" +
                "qzyh5zI8gTZhUgVVADfag63axqfdHuJ4uLz86SmuuNJVr3UTOc" +
                "hKvhCHoqFYqBCp0LLycMs2rzreSxv/YZ+DxkVVyFm3se0tqT9k" +
                "tb1iEzKD6bSLnZJJRw0poukN5ibPIHjr8F72FZOZsq6/ns40X0" +
                "BLMqvcLN3fkTd7jnX+k85c6fjbPd+bM6LXl366vu/Al9Vg6v4a" +
                "QFfTb0C/ypC86f+Z/Zg3z+9Nb+5GIvt/D5E869fyrOgE/kV9P5" +
                "s/6SPH/2PC3OfHD+TJbi+TP/jDx/Js3WT9Mv55/2UeH8CbOZ0P" +
                "35s+ffofvzZ75Unj/zT/k1wfkz/yxY4fyZX8Pnz/z6/EaqnX/E" +
                "j38ekBfj+TP3p+v8uvyGniE8fxZc6fw5YAbg+hzw8oQZ8NfnQN" +
                "CH7DfMQLoTrs8BjHQjXJ8D3JwFrk8vqbuWzUANrk8flQ5SvLs+" +
                "GxcxQvoAedz1CVX99VmpyAqseV5vmoHq0+mD5PX1OjQntMP16S" +
                "vCbBKtoHEhZ8H1Wczh/l7kAt+HwHtYVobrc0AzcddnOP1PgPwr" +
                "vLdu6jii9NXrpNf3mXrr76SFo1nT+bITGqOG9899hGHqzUelp3" +
                "j/rJfX5Mh0j7Zpf5zLTBp/Uo7Ne0EM21erG2ISX+z+CfkX4Bpe" +
                "nXTSWvjJtf0mPYVOzuYn3cns5P30XJsyuBdRH5No+q8r46fdzT" +
                "34fJ4z/evnM8l1ntsa+XSdn4vDfsInyWRKsoIsyVnA8j0F3/Pw" +
                "+bxGxdoh/jJm284x/4NkkXg1Ly54XRvkyoByo94Bs9PsxBGl3+" +
                "ud2Grvqr2LLBzJfmmRHe0SjVGxusOl1vyl9JDkKuPVTJ+Lubdn" +
                "MAdmgmtqx9YZeTWurPeKGTNv36umiiNK76uSbr8dWziaNZ0vO+" +
                "UyBtp6CwxTbZ6QnoJftbwmR6bD2qb9cS4zsd8qx+a98Pv5+bLV" +
                "6oaYxDf09WY9jii9bz02O9T1t2ThSPaTxR5kD2NJNEYN1+dXGK" +
                "F1s/SQ5CrlNWE/D8Xc2zOYAzPBNbVj64y8rbLeK2bMvH2/19yL" +
                "I0rvu5f0rgGycCT7pUV2tEs0Rg3X5+mM0LpFekhKFuU1bUfMvT" +
                "2DOTATXFM7ts7I++PKeq+YMfP2vdf04ojS+3qx1SbXJpOFI9kv" +
                "LbKjXaIxarg+JzNCa730kOQq49W0p8fc2zOYAzPBNbVj64x8R1" +
                "xZ7xUzZt6+zzQzcUTpfTNJ79pOFo6U/pP1otJMiRquz4wwzMzW" +
                "S9Kj89prcqQ9I+au/XpFbME1tWPrjMa0stWW1aFq2OVvAeR5CX" +
                "UrTkntvy3Q56T4vKRPX7rGwtcZp//T8ryk88b/PQNwO0dzj/3j" +
                "/Z4hv6389wz6ZJevaF9tzESzDeeldWYdjij9Xq8jvf5ibOFo1n" +
                "S+7JSLSDjz+/kGYZh1/ddJT/Hvva68JkfaD2qb9se57UxibN4L" +
                "f33uLlutbohJfENfYpbgiNL7lpBuL4gtHM2azpedchkj7OcvCM" +
                "Ms6f+C9BT7uaS8JkfaOdqm/XEuM8lfL8dG2ZtjbP5G2Wp1Q0zi" +
                "G/pkMxlHJ4NvMul2FK1s4WiZJz0YX2bjGl3bOLt/PXvaOcQ1Gc" +
                "NO0jbtl7k8OilrS2xm6l+/l7WvVrPQjIvo57mhZj+idR/dFlVm" +
                "kZ4yG1nGw+N67VlxBuznxeOxwnnP+yhSVm2vrZmiBfbzcu0dfy" +
                "XEF3v6cPpI/Cmb38dT8ZQ3/bx8h06/Xn7vgTj4RGuvE599v93+" +
                "KblRLftUL7/flHeS9AZdJVyfJn5+wHcYvB9pFFpjYyHF2tXld0" +
                "5/Arl6vPts2d1IvE9NhTbox6nulyD+lxFTSffzQbb4eK2FfPYE" +
                "1MhGWaHCoM7mmOBlDj6uiC146hjJqmBQMEMeaCuqaOZTJftiFw" +
                "ZV5cGIRbBQdOC5xWzBEaX3biHdvmi22BvQzhHklxbZ0Y6aPcIa" +
                "zeAa+SQjpA9ID0nJorymvTHm3p7BKyJEeP9cEa848NghM+xSWP" +
                "lhiav3ihkzb99XQxvxo5f2lUJzOswhprD4eKUpy4i2+TojGBkq" +
                "kY1rrGaPGSm8kZ9iqI6Lssu1TWYU8SNF7ghKuRrlX134mfeIqj" +
                "yi6hWYFB14PmgerM11I0r4PBY0p7MFI93IfmmREnPdjNB8zQdp" +
                "hrgSASXmOC/O2M8a8azNtbfK7BiRV4a1CVGuRjPmFSJH7aUKko" +
                "nDDJ+f5xZVN5qNOKL01TeSbl8zG9OdaOcI8pPFHmQPY0k0/31H" +
                "YYfX+1WMAK934SEpWZTVBG5rYu7tGcyBmXTdHq848NghM+zauL" +
                "LeK2bMvF13d6pGpX964wz8Pk4/n69VeYS4Dno+L++bjdPJj1J+" +
                "wpRa4zSuXqvKO778vInfx7G//8xQpVP/Vr5WtVslW5fBd2L8Pq" +
                "78+TyxlHft9vt1TT2Pdc/nnaUxofjecGicz5v3mftwROn3+j7S" +
                "a1e60e6nGIwgP1kwiiXaJRqjBrZXagT2oKxdKVmU16y/ILNjRM" +
                "lXM9Es9ZwzKIorYwXJRF2fFF0zteqrbkRZqZDm9Nji4rUmLRjN" +
                "Nv+7wlcxEiuRjTEwGmX1VfLGfoqhOi7K/oO2yQyKJ0aYQ1V0Ft" +
                "fnahglvboC8+BonNuPt1/x/Lqx82ObftIU/sfIQXX6duf5z4bv" +
                "8y6FfknJeX7deOfh9qdS457nh8ue+zCTcc/za9vfk0rO8z8uP8" +
                "/HT5diXuZ+cz+OKMnmWq2z1mnut8+hnSPILy2yo12iMWp4vXdq" +
                "BPaQ5Crj1bTPx9zbM5gDM3G19YpxDvcjkdHYHVfWe8WMmbfvPd" +
                "BG/eilHSs0p8McYnDsCSP7hUVJn+tmmOsk2f1sVGKwB3O8d7TA" +
                "6Qk4oqbPHrUjMjtG5JWF2qPFCkZ1lqhf5Abeo6ryqKoX1hh2c7" +
                "SouhzaXj96CRbSnB5ZfLzSlGWvtvlKezEyVCIb11jOHrO38EZ+" +
                "iqE6Lsru1TaZUcTvLXL3olTrk/7lhZ9571WVdb0Ck6IDz01mU/" +
                "WoG1HCe2vQnM4WjHQj+6VFSsx1M0LzNTfRDHElAkrMcV6csZ81" +
                "4lk9al+Q2TEirwxrE6JcjWbMK0SO2ksVJBOHiX8UCf0T5hPVQ2" +
                "5ECb6gOd3N7c/Y4uK1Ji2YzzZf6RBGYiWyMQZGo6weIm/spxiq" +
                "46LsPm2TGRRPjDCHqugsioX3z6IaRkmvrsA8ODrw3GA2VF92I0" +
                "rwBc3pbMFIN7JfWqTEXDcjNL/CDTRDXImAEnOcF2fsZ414Vl+G" +
                "/dwg62lEXhnWJkS5Gs2YV4gctZcqSCYOM1yfL1Omu/PXFuAJwE" +
                "m4/y2gE0FtAVtO9n0c+VHqU4Q9UvZ9HCPKz0e1Behz7WTfxzlv" +
                "bUG2WH4Px6zl93HECHPCJ6kFOqv8+zjnsYdldUSQTJCvXA/s6T" +
                "ZoB/zopT1aaE6HOcTguC2M7BcWJX2um2Guk2T3swMSgz2Y470H" +
                "CpxtAUfU9NkH4PrcJutpRF5ZqH2gWMEBnSXqF7mB9wFV+YCqF9" +
                "YY7u8HiqprzVp7zI0gT5i1/vefa4M+VDPO4kZsLr5SiTXyY7Ts" +
                "vtZanFGO//csEBlP/j459nNNz+tNh5BZadMZFJ9NpFz/+8+1ej" +
                "WA9Jasz9UwSleWeSH7OEVnE4rorWZrdZ8bUcJ7QdCczhaMdCP7" +
                "pUVKzHUzQvMr3EozxJUIKDHHeXHGftaIZ3VflsnsGJFXhrUJUa" +
                "5GM+YVIkftpQqSicMM75/7KPPkv2eodZ/K7xkwiuSp/J6BMtqf" +
                "L5H/nX7PUOvOHpPcs45T/T1DXHuc98/uuDJyesffM9xt7q7Ncy" +
                "NKQAqa09mCkW5kv7RIibluRmj+GrmbZlmnxGAP5riqOGM/a8Sz" +
                "Ni/PZHaMyCvD2oQoV6MZ8wr9fs7TXqogmdTmFbs/r6h6l7mr9l" +
                "E3ogRf0JzOFox0I/ulRUrMdTNC82u6i2ayBmc4K6K4Rlb0s0Y8" +
                "ax/N75HZMSKvDGsTolyNZswrJIbSSxUkE4fJ6wlVp0Ab9iNIuH" +
                "bOKDRnhznEFBYfr7WQzx7Pv9xGs+HCO0VJqjYsOHh/UangaYbz" +
                "3domMzQz5IG2oopmXvAo7tjD2ssIEQ9aEfHsgLbfjyBhP3+n0J" +
                "wd5hBTWHy81kI+ezx+uY1m+wtvh5JUbb/g4P1FpYKn2Z/WtE1m" +
                "aGbIA21FFc284FHs537tZYSIB62IeN5h7qi+4kaUcK8KmtPdvN" +
                "GPdmdxI/ulRUrMdTNC8zXvoBniSgSUmOO8OGM/a8Sz+krjtzI7" +
                "RuSVYW1CREYyi+bwebPIxSjppQqSicMM9/dXOPNkz5Nri+CKvX" +
                "3858n2D/19PfxPiNoiep6cfTN+0tp7rrq/LxJPbs9pf5qcTXun" +
                "58m1Rb2zTvV5sjxh4JrKnienO9T9fVH8PDmbekrPk28yN9Xmux" +
                "ElIAXN6bHFxWdnSc11nR+iZjhJuVQJZxIRo1HW5qMP68ZVuLm4" +
                "2vzeS7RNZlA8McIcqqKzuD5Xwyjp1RWYB0eH+f8D28748Q==");
            
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
            final int compressedBytes = 3875;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtXGuMXVUVvqA1bQGTYgu00lqKUBL80VINGrXuezs32KlCRZ" +
                "4xDDVtbS1jTEZIGcG2594z91Vw6EvrQNvBhsQniQUjD4khxseP" +
                "oiDiDzHRYkjGDH+oion+0LP32ut+a62zZwrEe7L2Ouv1rW+vnn" +
                "s599wJza9UKu6RSvHK5pJ2j9SnKxU6y97wq/dniyuVsc9WKvXp" +
                "wrs8W1Hpv8ZupLqA8c9KpTEUs9YQBkXq02PXc5ZHHLuWrdYy+D" +
                "laVN+c3RIQby8Qbyuqb0ZHYlhg3JDNz6b6XBcU2Rf2mSzdsyzk" +
                "3C6xxz7TP7s1uzrErwPq2OfGhtjqLM0GC89NEW0j1Y1tzIoZjN" +
                "0ydlt2B3myIbnL4uwadw2tpNlH9roBXmWGtThO2VLQAT38C4iy" +
                "O142jp6cuW6g/WHp0xV6R9I3MzZmwQx0Z1kn+/AuozzoHqy96l" +
                "fSlQpb3vbn+ULye49fEZceqanWnzFa6PggnxGuRCBNNT5KZ4jD" +
                "Yp61V/NFstoiYmfUmxGJkazi88YkailLRrmDZOIx6cWZheQup5" +
                "V06J6zXdvFHmQiLj1SyC/RgEr9q9/WCIiwlizSPVurLfdyBTiA" +
                "Ce2pjK0rOsttZz0rMAbvIMfdcVpJh9hxtvOl7EEm4tIjhfwSDa" +
                "ix+xqNICLH+wxNB9uz/VHLvVwBDmCSX2x3LBE4t/Vf21nPCozB" +
                "O8hWt5VW0iG2le36lNtan5IeZMNqTpFdvRc+xuIO6OFf7UOMIb" +
                "srflvTPTmzem99Svt03NaCiWeZwsYswvV5ZWq3+iBM5htlt9tN" +
                "K+kQ2812vtztzleQHxkcZ08xz6Cr90gsiQbUOM+nNQIirCWLVM" +
                "/qPfmllnu5AhzARLO0CJxbPdd21rMCY/AOstPtpJV0iO1ku/4y" +
                "e5CJuPRIIb9EA2qc5wsaARHWkkWqZ+tuz01zL1eAA5i0SjuWCJ" +
                "zbudp21rMCY/AOssvtopV0iO1iO7+MPchEnD3F9Rl0dVRiSTSg" +
                "xru7szQCIqwli1TP6mh+ueVergAHMNEsLQLnVs+znfWswBi8g8" +
                "x1c2n1Osbm0rFu7bq15GWPP9OWP4p5igjlS5EY8f5yreyKSJmD" +
                "7QmM9se0T8dlLVavbW/Ng/u3F5Z3q1loxv3sLW4LraRDbAvb+R" +
                "XWg2xYul4K1wIjXp/zGENWq3/vLemeyGwc1T4dt7Vgkq9MY2MW" +
                "geHh1G71QZjMN8qgG6SVdIgN0tF8buBJ7aEMa+l6KVwLDPJ5XI" +
                "mCSH+eg+meyGyv1T4dt7VgYnvLXPDu/CC1W30QJvONctAdpJV0" +
                "iB2kY92cdXPYg0zEpUcK+SUaUOP7fY5GQIQ1uszUs/0jy71cAQ" +
                "5gQnsqY+uK7mLbWc8KjME7SMu1aCUdYi22B55iDzIRlx4p5Jdo" +
                "QI3X51MaARHWkkW6Z/sZy71cAQ5gQnsqY+uK7sW2s54VGIN3kA" +
                "PuAK2kQ+wA2wNPsAeZiEuPFPJLNKDGeT6hERBhLVmke7Z/ZrmX" +
                "K8ABTGhPZWxd0b3EdtazAmPwDnLUHaWVdIgdZTu/kj3IRFx6vL" +
                "S+JrEkGlDjf4/O1wiIsJYs0j3bJy33cgU4gElx/2l2LBE4t3uz" +
                "7axnBcbg7WVg5cBKWkmHq2cl2/kq9iATcemRQn6JBtT4PObnGg" +
                "ER1pJFumf7Ocu9XAEOYOJ76x1LBM7t3m8761mBMXh7cR3XoZV0" +
                "mHWH7Xw1e5CJuPRIIb9EA2p8hmwQEGEtWaR7Ni+w3MsV4AAmrb" +
                "bdsUTg3O6E7axnBcbgHWTADdBKOsQG2M6vcgOt+6QH2bB0vRRG" +
                "A2q8Pn/BGLJa8RtI90Rm6+vap+O2Fkxsb85t9eQMusdSu9UHYT" +
                "LfKDvcDlpJh9gOtmtXWA+yYel6KVwLjPj882HGkNVqnjvSPZGZ" +
                "r9E+Hbe1YOL3lMLGLMI8J1O71QdhMt8om91mWkmH2Ga26y9ZD7" +
                "Jh6XopXAuMOM9vMoasVvPcnO6JTM+tjGK7cQRMZJ1lD97dv6V2" +
                "qw/CZL5R1rv1tJIOsfVs5x+yHmTD0vVSuBYY8b/vFzKGrFbzXJ" +
                "/uicz6P7RPx20tmOQfTGNjFuHT5ERqt/ogTOYbZdSN0ko6xEbZ" +
                "rl3uRvMd5EcGx6VHCvklGlDjPD8OhPYSGWEtWaR7tt9ruZcrwA" +
                "FM/J70jum8MSkreottZz0rMAbvID3Xo5V0iPXYro+xB5mIS48U" +
                "8ks0oMb3+2GNgAhrySLds+Es93IFOIAJ7amMrSt6A7aznhUYg3" +
                "eQruvSSjrEumzX/8IeZCIuPVLIL9GAGud5SCMgwlqySPcs5mm4" +
                "lyvAAUxoT2VsXdG71XbWswJj8A4yz82j1esYm8d2/gnywoNsWS" +
                "cjlJ/yoUf1W7pa1mkOticwinnO01kyLmuxhj2tLTMHAvfvjZd3" +
                "q1loxv3sTW4TraRDbBPbtUXW4zY13pAWi7UZizsgJ8xzgjFktf" +
                "r33mS7SE+Y57+0T8dtLZj4PaWwMYswz0O2s+YATOYbJXMZraRD" +
                "LGO79h72IBNx6ZFCfokG1DjPb2gERFhLFumexfVpuJcrwAFMaE" +
                "9lbF3Re9521rMCY/AOMuSGaCUdYkNs5zXrQTYsXS+l+Fb4d4og" +
                "JzwLvp8xZLXiN5TuiczOR7RPx20t+4o9VdPYmEWY5x/dUPO03a" +
                "0+CJP5ktSn/d9W+ZX/Cowtb9cuqE/775vwIJvO3Fn16eaUjBCC" +
                "FPahh3tOdvXrno1cpznYnsBo/Vv7dLxAXMa1WHlPsjefNyblDK" +
                "r/Ke9Ws5DI2C1e9RPYC5/lnyz6n+Roaw7FsuXILbzvwnlzDf99" +
                "ncSLee8ENmHO9qqfQH3jtqJ6rs3wGDLLn0lrz7I0cu1kc1/sMG" +
                "3x8Oq9LPlnG+MezmKv1f331fnufFpJs4/s2p3sQaaMzya6A3qE" +
                "8xcYQ3YvMyv3RGbnGctdx/WO4KE9lbF1Re9Pqd2m+nA3Enkll9" +
                "/v+aB8/6be72XRV3j6/d75gn2n7tlYukLP8H7vPKs/q2x8pvd7" +
                "vj79ftcz2Htu+jOr3Ee/32f//MyvLT5XHv5/zLPxiPr8PCk+t4" +
                "6/vXk2v/w25/npN/P5ubdanme6j56nG3fjtJIOux1nu3aJGw/f" +
                "38eRibj0SCG/RCuupr/CX/h+A4TOKRUZ77+DTAfbs/OK5V6uAA" +
                "cw8XvSO6bz4vu7qNj7KdtZzwqMwTtI8aKVdIg5tvPPO9d5XXqQ" +
                "DUvXS2E0oEbf867/6pyWkT4/l+6JzOYp7dNxWwsm+aY0djFPMY" +
                "O921O71S/CZL5Rht0wraRDbJjt2lVuuPsO6UE2LF0vhdGAGn2/" +
                "ZYwC/2wZ6c9zON0TmcU8h3WWjNtaMPF7SmEX8xQz2Ptsarf6IE" +
                "zmG2WD20Ar6RDbwHZtlfUgG5aul8K1wIi+lxhDVqt5bkj3RGb3" +
                "PO3TcVsLJn5PKWzMIszzdGq3+iBM5htlxI3QSjrERtiuPeBGwu" +
                "fniMywlq4n8ZWx04hEjfd6DzCGrFbzHEn3RGZ+h/bpuK0FE9ub" +
                "c4vrU8zgvjmp3eqDMJlvlHPcObR6HWPnsF07QF54kC3rZITyqV" +
                "L70IOjtnuZg+0JjO67tU/HZS1W7KmMDabMwO5Ws9CM+9mL3KLa" +
                "Q34lXfSLlrf9Oa2U6VfEvcUeqek8Tu4hyuQq8gEDeN7PUX9mu8" +
                "gKYHBEIsYd9nfEfMBEVvF5cX32u1GWjHIHyYT3CMxC9rv9tJIO" +
                "3feznY+yB5mIS48Uf+RflWhAjXt9USOIyP7+9WE62J7d91nu5Q" +
                "pwAJP8brtjiQAGtrOeFRib7G1uG62kQ2wb2/ke60E2LF0vhWuB" +
                "EX2/YwxZrfhtS/dEZvd67dNxWwsm+e40NmbBDMq71QdnyWxxh/" +
                "9o/9v/Y/2zx4v+O2Q0Pg95NP2sYeyH9Dwkf1zilV/u9zjvnAp3" +
                "PxPm29GjqPfPQ8qvzis6y59Ja6bnIa39nJH/WPobk5YBvvtkG6" +
                "3X6v7OJt0kraTZR3btA26y2yI/MjguPVLIL9GAGmexTSMg4iYl" +
                "s9l6Fvefhnu5AhzAxO9J75jOG2oGdj8SER45R8ldP73Lnuw/f7" +
                "u2eF+Fv9NpXicz9XdX/WoMVc74qjVx3j3bfuPXzwCKb9bJ67N5" +
                "yj4n0PuY8Xlds/yMIXl9Ts/6PHGGqJtwE7SSZh/ZecNNhO+bE8" +
                "hEXHqkkF+iATW+557WCIi4Cclstp7F9Wm4lyvAAUxaT9kd03lx" +
                "fYoKux+JCI+cY5S73F20kg6xu9iu/4E9yERceqT4I79RogE1zv" +
                "MljYAIa8ki3bN7xHIvV4ADmLRetDuWCJyb32Q761mBMXiHic3+" +
                "/HPsrT//tE/+k88/t6d/73grz+u6J97m87rdb+b550y/2Zzxed" +
                "0Rd4RW0mHWR9jO2+xBJuLSI8Uf9YskGlBj94s0AiKsJYt0z+5j" +
                "lnu5AhzAxPfWO5YInMtZmoftI65Pzt7uttNKOsS2s513rAfZsH" +
                "Q9SX0hVXIH5AS2CxlDVit+29M9kdl9Rft03NaCie0tczED4m93" +
                "qw/CZL5R9rl9tJIOsX1s5123r3aY/MjguPRI8Ud9iUQDapznEi" +
                "DU/ywjrCWLdM/aYcu9XAEOYOJ76x3TOfCYoe6sZwXG4M2ezvs7" +
                "l3YuqU934v8VJLuzf29RTKV3mbw/yL4b7uenZ78fylbZO4rs++" +
                "pe/Itn/H1TfT5l3zH43+P7Jfmpr39vnvF+acmbuR/ScXE/P53W" +
                "/bkucAto9Ro+smtL3YLeKulBtqyTEUJI+dCjM4zq3mpEyhxsT2" +
                "AU90sLdJaMy1qsvCfLHAhyMna3moVm3M9uuiatpEOsyXb+BHuQ" +
                "iTh7mlOIAEuiATXOc5dGQMT17/RlvNyzeN/cYLmXK8ABTPKf2B" +
                "1LBDCwnfWswNhkH3PHaCUdYsfYrl3sjvW65EcGx6VHCvklGlDj" +
                "X1+8rhEQYS1ZpHsW16fhXq4ABzDxe9I7lghgYDvrWYGxzg6/lH" +
                "6J/v9L+rMr3Kv9VP/9DD8PyVak/9oEdn1++pMw+uZ39p3p/jO7" +
                "Rd71dQ7a+778mc4D2XzgZwvq09mF6EX3n4nfr+fH/KsD7iH7az" +
                "lnZYPy89PGsztm+P39tfprtLJmy581flV/rbcXHh23HhVZPFN2" +
                "49eFbzHs3mrO5P58NtPho/76nIkVnTd+yZkaXfZGXO4+5C7W0f" +
                "S+OZszi5P/AVXkp0Y=");
            
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
            final int compressedBytes = 4100;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuMXVUVvpRHKiJDW/qglFJQoJI+wCJQKOXMPb3Fx7RTWj" +
                "uFYosgFloSNAKKJrazZ+6dztyZEDAFTVCsSvknzkhH+CWaNgR8" +
                "NPGXSHwEakJNaVrbqBEF3Xuvs8631t77zp3oD+/J3vvstb611r" +
                "fWnLPPveec1pxuzqhUzJnmLDO1UnzMOabDnGtmVCr1l8xML5ll" +
                "LjAXmnl2b75tC8yl5jJzud27wraFtl1p2yLbFpslHn9VbbpZZs" +
                "drbPuoud7LbjI3m5UUwWprVrLafMzcYsePm08WkbeVHDaZ28yn" +
                "zRZzp93/jG13mbvNveY+c5qZQoj6T837zNnm/eYD5jwz3SKmmf" +
                "PNbDPHzPX2F/XONxfb8RI/+7Dvl5qrfeyP2P1rzXVmuR1vtG2F" +
                "yUzVjrlZxfEt6hNW0mXbGrPWrDPd3sOtZoP5lNloesxmc4eXbC" +
                "34fta2eyqV7NHsUeppdDqauf1qJ0uAhF5KZCO59AavFL06RXuA" +
                "hkfJIhWzOqX5g5B7bAEOYEI5xb61RZiP1EOCT4nelm2jnkav28" +
                "bz+sFQAjRm2l42toUPkg2Osg9prfhtS8cEsvmclml9aAsm9QNp" +
                "36gFM4iz1RujFHpLtoV6Gr1uC8/rPw8lQGPWf0TP0dgWPop6vs" +
                "Y+ZHRVzy3pmEA2f61lWh/agkn91bRv1IIZxNnqjVEKvdpu+3zv" +
                "RyvhmZ3Xf+mlpcTj1UxKeM4yH2cfaQZ/yxgvQ4zVBQcfudQGes" +
                "YwL3iGTFqU+H2l7T4abT1/oa0klqMVHvapyDpe6ZO5FDyHs2Hq" +
                "afTaYZ7XvsASIKGXEtlILr3Ba3F8vq49QMOjZJGO2fxNyD22AA" +
                "cwoZxi39oizEfqIRHnE6M7s07qafS6Tp7X3golQGOm7anVLiRL" +
                "jgCMv3peyD6kteLXmY4pfLylZVof2oJJGFtiUQPiH2arN/LJfK" +
                "nVf1ipNL5o2wPZBivZ4K/973BW9b+V+W3AaBbwrP58Jfmp74dF" +
                "/UexftU+hf6xbWNBPTeQfePzVjeajPF3oJiZnGE/jF1/jhD18Z" +
                "ReZsp7Zl0YIUZFfjZSs/V6r/wW9jupHTiTRlvPjbAbOKv/SCt/" +
                "8jNwBvYH/1pp88k2avuBqSGi+ZpGgX/KHh/OqZWe5H2nSV9FPT" +
                "cOlNJw5E/f7EplyB4tQ89kPVbb42UzSu2s0mcPRlvPnphH35xy" +
                "b7q0SH0G/tC2nj3afujZKN7ThOo7H8xgE9qL2L/XGcWRQ33WU9" +
                "SzJ9a2zrJ21GrXe6blr4S6PfP7vstab7/e1ZNnE300pk+d4UNT" +
                "Bx8vNd9PW2v7wT1RPZ8ZfEyi3J6c9c5vsRb9Q/IbfKL0txcZav" +
                "7Z+qKe6yENR8X1Q4MfHLykNj54Ken7yjOzZiqVfv8NqzYOe1vP" +
                "8UnUcwLM0GWiLul6jreL0b9Vo9yenLWqp8spxY/rmeJP9VR1GG" +
                "+dJZ3vnQOVSudA5wOuZ01jCu+xzOnNAiBafybCtLe3TAbanO+Z" +
                "RhF/Yf/AxLEn5qDjo56hdegFZ0xtTMtcq/+rdrR5jpeMEa44Ps" +
                "cIIY96eb75/bH0metZHHfnu9NY/2+7vnddcB6NuRiIObiH0PDW" +
                "/4Y93yVqTLKsjfXO12tBeb7vAp7Od/IaHZ9jsp5hlcpIY7oC+c" +
                "n8JJ3vZqo7393HyVxvj8/T85P1HW6/75ibk9TdXyIEyUjKrUQt" +
                "k96ap9R3lu+xJj/Z/AuNsOT7Syzlnrfi+u5inG22s8RMy0+a2Y" +
                "w1FwEJr/569McCfx1iui2sp7u/hHqWvIrvR+Z+kvS9rSuQdWfd" +
                "xfW9u1x/u91+1p3Pzmdn3a6eNCddPtssYAnbsJ4xDkUz58GNzX" +
                "+DnfPLuKy7+S6N8KR5kGzoWUIxN+vxPY3SDKRce3WMtJS8hvV0" +
                "SNRTetBRgwqsy4ozLSvXCJJl6/Kl+VKS0pz28qWQSFuJIZSbOQ" +
                "/wXJxxU9kHR0qxgH94hUWlMtyhUZqBlGuvnBOkMr6q51LJJahn" +
                "YFFWYG22tpCsLXVelq3t/1W+nKQ0p736NEikrcRY1HSaOQ/wXD" +
                "Jdzjj2E7OAf3iFha3nTI3SDKRce+WcIJXxY5axBx1Vx85P5CeK" +
                "FXV/6cfLbL/Mbif8+nmitp9wTuqwBeIEW6Axivw5DywVTJdxnP" +
                "yEXT9PaE/ExsXgmEAD6dZPiXJ7YAm59MqMGI988xPR9Wh/vkxY" +
                "BVVia56XFTiWH6OeRq87Vmwz7XbM1/OYQM4s9aWNbmRbzGYSCj" +
                "G8TPhovgsNM5AslFchab4Xco8tkBF7JEY6Y9rv2ystOFPNI4wj" +
                "6l1YZmuyNcURu6Y8dr3MtVXbszXu+xLPgSKJngEDbP8poMT1fT" +
                "vjsjXDD9IYstB+GA1u/W9oVMhSxpVeXU5aSl7j61H/ScklON+D" +
                "rDh2fiovvsnUyntFJHNt1Y78lD8+T9WeJxz1NIMt4+Wc/RFq+C" +
                "uqnjtYk58a/jKNsKQILoaMSSgghx/RKLcHlpBLrxRb5gDP0fmu" +
                "7p2FVWJrZEkt68q6igp3lbX2sqwrn5XPyrr896Uu1jspo7WtxB" +
                "DKzZwHNw73C26z2Ifbhg2NIQv4h1dGeo99GqUZSLn26hhpKXlN" +
                "fF+aJbkEx2eXlnDs/Hh+nHoavZ/jPG+clx/3x+dxIKGXEtlILr" +
                "3Ba/HrsQYP9vgUGh4li3TM4UdC7rEFOIBJdXuYMe3b9VNYhPlI" +
                "j5CI2h+X3PVvMvNQgZmTz7E1nS615iUna3d/yVyVzyFMPifQ/I" +
                "T9trs71S5GY4ZGTfb+EseuHTXLJ7wfcjTFst39pWxGNoN6GllG" +
                "88bMbMbACMmBgH6ipiMghj8+V7MPGT1mFscEcuCdkLvW64wgqe" +
                "4IM6b9vr3SQuYiI8RxAnRH1kF91lHqOnjeWJJ1+PWzlAAt7aSG" +
                "PKRkiDF0K6zt+tkh7TSHMCZ8DPdpmdZLW/RurO+KmbvN1lPUQP" +
                "PXXrVEo82DE71vU3ud37exreX7Nv1H8L4Nn+/iDE+8bzO0I/2+" +
                "jVoZ2rxv07jFvW9jNcn3bay+ln7fpnrfZN63ofsh8fs2tqn3bU" +
                "p08b5Nar3CStT/cLh24D4dVhA8P/J3o7e2XhHZZmhX+/UR9+r6" +
                "tqRW7JFp4f1CzYvWT3hh3tVtkov0qznJe5BxnPKu4ZeCVerG7E" +
                "bq3QgZzRvXkRQSoKWd1BA+JUOMobq2lnaaQxgTPkama5nWS1v0" +
                "bhz4Z8wcHmRlwmw1C80YsYr5ymwl9qnZetoz1f0+0nLCMh52cr" +
                "/2ZnR9WYkYtTexP3JFi+diK6VHGYXvJ8MHs4p56d6Njhm4wK9d" +
                "P1eGucim48R8AvYrshXYp2breYOq5wpuQGAM91MRWN/5beyPLG" +
                "qFlx5jz7aeK6SW/WsL3buxsVxygV9bzxVhLrLpOBNl6tdTeT36" +
                "mm3n+vcKVyuMux7touuR7S8vpAsFYvEEEcT1qPNpUzM72zC6zf" +
                "d3FrO7ovXzaroeeW15PbLjXLl+Jq5HNifT665HXibO0VbXIzw/" +
                "ktejFt9uX8lfoZ5GltG8up4lQEIvJbKRXHqD1+L9z/XaAzQ8Sh" +
                "bpmORDco8twAFMKKfYN/yBifSrawXGGp26zuI62ejEVVBfD/11" +
                "d0/6eZy+diafxz0lr/jyeVzqNxL8yGv4yM3RE0X1C6nV87hGFv" +
                "/Gmey3DfFr6gktKZ/HHcgPUE+jr/UBnle7WAIk9FIiG8mlN3gt" +
                "6rlXe4CGR8kiHXMkC7nHFuAAJpRT7FtbhPlIPSTi+CzQ4q87Hh" +
                "+f1bXlsTGujpXx+GgNj0+8ARH/7atr2x2f7k2F1senP85WBahx" +
                "sKT3GdLHZ5nTuD4+G/lE72OEVSorMB4cnwfzg9TT6Gt9kOedL7" +
                "EESOilRDaSS2/wWhyf39EeoOFRskjHHOkMuccW4AAmlFPsW1uE" +
                "+Ug9JOL4LNDi+ByNj8/a4fLYGC33Djtsu+PTWo6qo/WwWj+/JY" +
                "64w8njc9TZT7R+FjyAGgVLN2t1fJY5jerjUzOU9VCZjpb8jqo5" +
                "H58v5y9TT6Ov9cs87/wZS4Bs3M162Ojmtsa90ps9O7cx3v8+eh" +
                "IeGvdIDY+SBbxKSeNzIffYAhzAhHKKfTfukhZhPtIjJOL4ZPSh" +
                "/FC4XpDMtcb9+SH3fT4/VBsnHPV6RiM3tgeGUeJtpKdYkx8auZ" +
                "XGeP1kKfww0jX3/Eii3J5r/Z08g0/pvbFD58CeJ3ofDB6wfrIX" +
                "XYHie/8mt2FGzf4tX+XfRyyjBoQb3f0QmsGH3g9jDB2AdiT5Xi" +
                "tjESXx+2iTRPGMJVoDmcsp9u9/H20K+cuGHGQmUZZXZldS70bI" +
                "aF77M0khAVraSQ3hUzLEIL+hF8JrDmFMRu6e5XxolNRLW/Ru3D" +
                "0zZg4PsjJhtpqFZlyiF2YLqc/K3400c/PGgySFBGhpJzWET8kQ" +
                "Y3eHtpZ2mkMYEz4aD2mZ1ktb9G7cfW7MHB5EPaNsNQvNGLH8bL" +
                "PbMKNmOe8MZdSA4FH2Ui7niFEdzza3uT+zWXsM8dXxka/CI7MC" +
                "r1ADWXU85V/KUhnLHGQm6Tyy292GGTVbz/5QRg0IN/r183ZGSY" +
                "tWMaovhNoUI+klxFdfkB6ZFXiFGsicZexfyvwZdL7OWOYgM0nn" +
                "kS12G2bUKpVVV4cyakDwKHspT8eovpgtblPPxdpjiK++KD0yK/" +
                "AKNZA5y9i/lKUyljnITNJ5ZIvchhk1G/vaUEYNCB5lL+XpGM0z" +
                "skVt6rlIe0zh4ZFZgVeogYxyCv1LWSpjGVFmks4ju8NtmFGz5/" +
                "vuUEYNCB5lL+XpGNk1oTbFSHpJ4eGRWYFXqIGsMZDyL2WpjGVE" +
                "lckd7Z6VyV+O1evTT6fCZ1Th3bzUHS4ZI9/f/v5Y/Is2eJ9hUD" +
                "9V08/j+H2GxPO461P+J/s8Lrwrk84ju81tmFGzsW8IZdSA4FH2" +
                "Up6O0ZwTalOMpJcYX70BHpkVeIUayCin0L+UpTKWOchM0nlkS9" +
                "yGGTUbe0UoowaEG/31fQmjpEWrGM2bQm2KkfSSwsMjswKvUAMZ" +
                "5RT6l7JUxjKizCSdR/A+Q/n8qFH+m2RzQfH86PH08yPz9fD5kV" +
                "nW+vlRc6V7n2HCp0eb2j0/sjL3/4c8ln5+5P7/kPTzo0bT7/+X" +
                "z4/w/4cUkq3pZxt6/eR54zG9Pvm3pHqD+2bJdSbvLVbKXqxUvJ" +
                "f3Omm75x1hlHDlCrmHerpfF6+fYWztgbEOxUi92sY8Uu966LW8" +
                "rOeeuJ5xpun3KOIrCzjsvvj/V8/d8yeuZ+sr8GTqmc3N5lJPo1" +
                "8T5vK88WQoARp7/UfknPCySR/F+X6zjirtinVpbjomfDS+oWVa" +
                "H9qyzNo9EWas2bP/VtmGcQL0vGwe9TR63Tye1/4USrJ59WlyFo" +
                "+Er0+nef4wZLxnz6aHU9YcX0ZkVpinMBoho2lWbgxjh+wJ61AS" +
                "GXIIGcMyvqvfV77F0PhmjMh3VibxYVQKne+cnI//7dPyfdpJ85" +
                "8I2d/qX9P+B3wADqs=");
            
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
            final int compressedBytes = 3270;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuQFFcVHuVRGOMC2YQsRNBgicSC2qCla4yPme0ZH6i8BE" +
                "VNgjHRRBN8RElZFtRyZ5gdsgihCCWpEhbUSvknZUwZIjExIpbP" +
                "Kv+YWIlRVKhiKxX+WET5Yfm6t0+fPo97b/cM7pLeun3vPec75z" +
                "vnTnff7js9a75splcqZoaZaWbZ+psVu5lLzWzTZ/orldoWc0Uq" +
                "mWfLVebVdn/Qlteaxeb1ZoltvcGWpba80ZZltiyvpJu5tpJv5i" +
                "3mbWn9TvNu8y6QjW0yDSt5j3mfea+t328+UPE2s9HcYG40m2zr" +
                "k7bcbD5lbjO3m5eZl+eIV5hL7P5VZo65zNZzzeXmSjNgFoB2+y" +
                "LzGiu9OkVek+4HzYo0pzfZ9lvNkLnO1tfb8g5TNcO2Tkyd8a+0" +
                "5YO2fMiWNWZ1Kltry3qzwXzEfNx8QkR7iy23VirVvmof7F0NG/" +
                "Rcv3YXSElCaG7HNYAPyYhj7Faybh8ijR+D5gzFyWPRbNwvz8n3" +
                "TZGid52tZJQRE5fbGmfdH/WgWMTDWgaFEFQXtTXH2GdISxwazb" +
                "1ob9IjRkVxNc5uX8Q15BP4tH8uC2XMGXkmOq7qiuoK2LuaZNBv" +
                "HwYpSQjN7bgG8CEZcYxtltbcTsagOUNxci+ajfvNchr3IycPfG" +
                "R0tpJRRpyjB6uDsHd1phvEfvvbICVJdTD5Ku9hjQXxDkVo8gEM" +
                "qNXsfgzcM0k0RiI4G/cLEs0t40DfDsWRchz8iIkrvZpm81HW22" +
                "ZLei1ofz+XzbfFzUfjMB/Z/ZJMs5RdlZez9pvdvvVi3mfzUbLO" +
                "zUdFm/mo2ZjWm7L+zQGMnY/M3rSVz0e2TucjsxDOdzcfmcN8Pk" +
                "rWpW07H6X19cwfm486i83K1jmajzLEWvNhnI8yyU2xDBpH/Stf" +
                "+2GtbZxNvsaxEV8WBRiH1teZHbNQWuDhKOfwr8U+yrVcaX0Dej" +
                "Ce/pZHVJiDiz8UZT4OR8u8NB7KfY3n43lUa5PxZBvHRqKxKMA4" +
                "tPTpZCgtGM+HOAe35n2Oci3e0zbErvMNo6yHbfFR0nXg7MnP9+" +
                "RgPp4/8ngOJlstdnHJeKaotLVV+3QylBae8Ru5P+0/w1zC8HNt" +
                "uTJuQ+wZfqgwg61mZVGU5o6I3e5kN+yhRhn0W6uS3Z2lTtJanS" +
                "NHUE82sqR/I1lvhDNk7RHug9iBm0embTln5xodu46JZ8RyHdEZ" +
                "Q7t5RFiMQKY6DsnDxzEr+5J9sIc61e3Dfvs4SghJei7hBeTcG7" +
                "dJz5KnpAeUo42MgvPpOHnsOiaOJLzjlhlzDxSBzjacCUXLY1fH" +
                "7KH8fH8yrrNHyHUh6+ZNEuXbWcvllR625o3an++ftvZPijGdZZ" +
                "VJ2Dpvj4zdnmQP7KFGGfTbP0/2dAZBjojGBOrJRhb315iAHqDJ" +
                "J8i4D2IHbh6ZtuWcnWt17DomnhHl6rhlxtC25zuzcCiO5B5Jws" +
                "eRx+62+lWB+6Vf+HdC3Xx6PkpK6qdilhTF/7M5L7H7pTh3rwwl" +
                "iAX5WN+fj+dvveP5/m7YfJSU1E+XR1HkrywK5yWGiXP3NJ4LIp" +
                "nvTfbCHmqUQb/9O5QQkvRcwgvIuTduk159viA9oBxtZBScT8fJ" +
                "Y9cxcSThO5/XGXMPFIHONpwJRctjT0d8fuB8Pzk153tnLPq5z+" +
                "/GX1kUzkvsfO/cMynH5/xY5hhbfUDKXGmf4uth/voZ78XX8Pzs" +
                "68/x9Ta3375GR6HX1PRKme+fInBeti+SK5DEraMMeS37ROsDUo" +
                "Lc9f56P+yhTrH92G+fRgkhSc8lvIAceq0XqYcte4zslh5IgzWP" +
                "Iszpx+5bEJLhP6sz5h4ognp/6xz3K/koYorblerM6kzYuzpbe5" +
                "qJ/fYESElCaG7HNYAPyYhj7IvSmtvJGDRnKE7uRbNxvyDp3OtH" +
                "Th7Y+qeXrWSUERNXYI46hq3WlriucH4/Vqzp7K9M+RaLga6f3e" +
                "VSvLXu7iKSfBWkfTau685DWNMZvwjjGYmhc6i3XCYhkh/m43ku" +
                "ruvOQ1hT33wxs1AzyebecpmESB7HVm2Jr2v/qxcP+SfzX64Z+x" +
                "LT/PvComz/p9cYdE4S0f7npIzdg8mDsIcaZdBvPIsSQjoZ/aGO" +
                "F5Bzb+CJNGNbyQNqMB4eGWfQnGDHY/ctKAYZicyYx0ERaGY5Vh" +
                "SxRBffa41eOln381Iz9q2pP8ti9/Ojr+wtl0k4Zh/Lz42FcV13" +
                "HsKase9ezCzU+b6wt1wmIZLv5Z9lX1zXnYewpn7nxcxCzUd39p" +
                "bLJETyRD6e/XFddx7Cms4jFzMLdb/0g95y6ZH1ZHIS9lCjDPqj" +
                "V6OEkKTnEl5Azr2RV2Ad3ig9kCY5ySMr4vRj9y0ISXjHLTPmHi" +
                "gCzSz5KGKJlu/28PWB9On+cfl+EH9TSL/fU7Yewlcdat/hqxt8" +
                "PUTa+Zw6TrmqwvWwHqJXVDAn33fx+0v6PSm9goJIk76vaGv+fk" +
                "j+vuLoau99xZ9NyvuKJ0zDnHhp3lccXTWl7yvOq86DPdTps/08" +
                "7Nd/rCWElnZUA54X7iM736eFrJGfM2pORA5P0zKp17Yow5x83z" +
                "QWGEE4W82j0APVAdhDneoGsF9/UksILe2oBjwv3Ec2ntND1sjP" +
                "GTUnIoena5nUa1uUYU6+bxoLjCCcreaRaHkNkleuZHYyu3F2dN" +
                "3kXz+d3/B68tRfPzV399fPMI9ey87vcztQcJ/eL43bcpjLwIYQ" +
                "8a0IU25f65RhmlWJovihF3s+Qkyx/xC/y11ba5Sbj5LsfcUEjp" +
                "18Pkpc+4rR2xI+H/0y8eajJJuPEjYfJXgfEZiPUr+B9+eRP0lb" +
                "8fkoybB8PkqC81GSz0dJPh9ZWT4fJdH5yGpWJmI+sr1fyfkoqT" +
                "A0zkdzqnOG/+b2rk6vTFnP9V2bSxxe9tCeNOnVJCjDFvqVXoAH" +
                "tMQBesRQnK5IGbeQkUEcIEMWGbn7ax5BWxwFriUPMg7MCOMMbe" +
                "Yr+Tm11tP92n9LvvV8r+sjtb9O3XMRXT8jZ3LP3GZV99jqC9UX" +
                "kmVuD7U9drOe62uJw8selwCaZOmZsAyQwIQy8gFoqJNlqNV6xC" +
                "APYKWMW6AOIwIbZJFWxE9sgOJayUBxEBrb9mg9ALPT8IGsdxzl" +
                "KOGfvkPL4w31HBeSkc6XOo/IBvr4LzucFrCkwxzk8Uk8fosYQ5" +
                "vzJ7WyJ621n+rfoWRn+cfy+f0+W/b7SMJe2Fbz3nnxPaIkxrXj" +
                "dVKHOZTZIXdxDs0bivVR7xP+3RPIZEveZ4VWYous9DZ2pnwFuu" +
                "iXR+IOZsK/Xyy8fk50s5pcmyjWF2ur/4CSfTa35Mfno2EkYS/w" +
                "+DwV8huWFHFxHeZQZofcxTk0P12sj2mb6RvntT4omSxf9Wzebs" +
                "f06yKaFFnrC3jKnxWbl5WN5/BvvCz7YpIQV7qO+RTompfzyMrt" +
                "kFvrm0dE73Mx+2LvDLETCu7T4/MY17Lno51dHIM7e9F5d1Q7u+" +
                "MgFMUPvej5vrM8vji/to6gZkPJPpu78vF8LIwk7IVtnd+H/IYl" +
                "RVxchzmU2XWeLvdrx2BLsb5Ya2bw38vQ/XyS0D7T/bHZZ/4kfy" +
                "+Deo4riHR2CGn+rD3i72XMc8HvFZLmLP57mSTB38uYvxTPR8hd" +
                "8nuZxK1/huwy6zsKr67noHjH5/EwsnqudNQKfwE3/HTIb1hSxM" +
                "V1mEOZHXIX59DcWqyPadN1uSGq7ecwlK/XDbk2SGi1asdcvZKG" +
                "eqxTVDYngTdvvW4otF6H/O6vaL3OaRGBGoqar9cBD8SWr9cNSa" +
                "vwep3T8HzQA48kGeJo0CZnkjPD590eavv5ZT3Xd+3RHSB3Ercn" +
                "PZfwGmxdC72lnGewBX65B6jBxmmhRXrqYZyIID7pkTIDbsLLKD" +
                "k/2QKKa5GBR+J8Zsf8ebQs+f7op973R6f8749az/Pvj8zp8u+P" +
                "dk17Cf/fxSPdfH9kURf0/VF6LXgAjmJXux4eu9UHRk+4PcjxaH" +
                "d6uRaPen7XgzKsd02nc9F5DD1fID8wxv8/A8XJY5D/nwHmI+SB" +
                "GEFfe4LW6nkc3J9DgU965qK8kYnypha7kq7z5/fhu6W2sQ7rLt" +
                "bNIpimsaM7o9y6jKPZkijX4r2YPeZU5j+sR6mui55Im/nvvEb/" +
                "gM9H8lv6bt6kKsIMP1NuXcbhno/C//UEerH7JeTW/tXz0b1hfv" +
                "+3DEq/prEG9lCjDPrD+xtrRs9zCaGpJ+2hOEvOQJg0p/3og1vr" +
                "uEKcHCllUq9tKRLNzbE0Bjo3HQP5lGiWwYb8s8l/J9u8T2obG7" +
                "Du4vgswOzqK7fujoNQrsV7Mfvao+XxOVRYj1JdB5Dr/fO9s1Bq" +
                "G+ux5pbh7zskRp2rz5aO1Poi+xDKtXgvZt95pjw+OwYHwnqU6j" +
                "rf/gdzi1HJ");
            
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
            final int compressedBytes = 2893;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXG2MFWcVvqVFGrBgBVtaFOIqWiya2opShHqH2SkfmxQICD" +
                "VVPhYVm5bEaGPiD7UzFzKzvVsT/NNqTWjiHxNoattgbTWSoATZ" +
                "pRJNNBFq+aEGDAbbqrTapDrvvHPmnPN+7967djbv1znPec55z5" +
                "2vd+7ObbVardUHW63kYnJx9cHkohiJWvaERtZik3KhB4SUgl5a" +
                "Ql9thZXUC0YdCT2hlRGBT9oCTnLQGIAfZA8uxIhkjFIvvUCfxo" +
                "F8coSZADvZB09q5MjLN4wrfghnZZqfrDsXqF919iYP3feBBiJ8" +
                "cGOrZY4DfeFMTCgegcgn9qlN9IQapYk1ekL1okenjkQZnDE4Q9" +
                "ayFTo5Ev38oyBBJOpB0rmAGuSibMha5/O9nAE10NIoTD6RmbNw" +
                "C0QiPnpanTFlACygeByqH9wa9OzB2WrmpUyU/DaphTHUphFisE" +
                "U2Zf9cAhppZcKgJdTwZ0bxCLhfahM9q0ZpYo2eVb3o0akjUdKv" +
                "plepyPSBdE5Zzy3z+bH0XZXkurIsqHqv1pjFVf1BudenHyrL0r" +
                "J8uNbeQtiWpcvVo6W7stLcma5N15TtunSoZdjSz6afq9qdVT2c" +
                "7km/lF6RTlNQ16TvqHvzyjIfz5/pogazhGTqmfTWWnp73a5M2+" +
                "lqJZ/PKF42aNHdw0a7y/J50cvKHGQLsvfX8r0in4DKPqCydJbq" +
                "887Ks2F2s0G+rCzLNelNZT43tnreskUe/a1meXTYgl+sorLbJh" +
                "TP7YY94jG5fzZ78S1aPoeyu3rMQ7nvdzf1IZ9Dbr24vhvz+ZMQ" +
                "9jBUYKSfbnpbtXw+0DP7tjKfX2hN+ZZtsWTq+aB8GlDpr0J9p9" +
                "PLcnWzV95R5+6FeIWGPLvv2vTFdMDHuO+dstUZzNL0JU1yd92e" +
                "MXsQUaYzCf7aslxflnN8Fjbf6Sc8OVlvjr3W3uf9RB4SJdore3" +
                "LLz1Ft1e6lIx/fZHSA8GGyNkfJ+In9XrdvN7/Nv2odkonse03v" +
                "MX3/7Pk4/H55vO/x7589+/muJVNPBh3vT/bqv71FljKSrzQx/b" +
                "DcR8+AFtv2Fu9sDrkwq3/vj8bnIz/LURi/2x58q/rscSrPDnNm" +
                "1YPamlZ3pXYTvb7HA3F5nswP0NVVe5OQ6asv7Sw1IDHxgEkXe8" +
                "+/plWqG8VXm+UKdqEtMtsKEmZY5fPp5CIg25uw1deqAVen5hqe" +
                "/9O9ap3s1n1k6q/vtnxmR3yfUYX6cR/PPM81Z5FjU5TPg1PDG3" +
                "T/eSwoB8/34rv9D1lqrp81++e/zUjEyudLE87n4yZes0TXmK1g" +
                "Dj67aJ+fV6DcerdW+WyONvn8j+ZnnXtsiU5B5S/6EJPfgMnGmI" +
                "8E5eBYH4/3bze+39CiXeseW+aooPKXfIge8rnWzZj/MSgHv+w1" +
                "DlwfkevRm43shurIfqTzaPabcjTgP97T6mmC4XnIb8vj/bA3mm" +
                "2wPrLO+AWxPkovqOujevSe7NeWfB8JWR9FR9L1puch6Wa+Pkq3" +
                "T+T6Xmi6/T8V+ZzAvm7O59E+HEW/8+ht+XzObddZB6iJPV/S/I" +
                "zIAnWVz2lUW7UJR9ivy9GI/eodaecwyUsR0UjAepOgIH7pKxqx" +
                "Xt9HbDGE+Feto4CzMZ4/iys1viH32BKdguoe8yF62C+G3Iz5n4" +
                "P2/rOtvm243iy0Z/fReve44TjnQnV/4ePtIZ/r3YzRU0EsT/US" +
                "A1+rZX9qen8xI/nxO4n7zz/47+dDVnT8e1Q+D+vxfihkBREdcu" +
                "snsv4g+Tw/ReujM2/d+igfC7HOxyd5dDR7YLJLl6naZBe0LibZ" +
                "M2GaZzzTvUfMLpc9MAgU+BV4tLHbU7wrL2Y9SNXWgNyu3y/t/z" +
                "nXJtuhDTiHODCDd/utw3wgSvRE2X/UbQ++3fzZX816kKqtAbm5" +
                "4Xq5uR7N4NpkM7QBc3Vg8gt+6zAfiBI9OrLZ5+f98ZU5eNWsB6" +
                "naGvbwrixQV/m8mmqrdjC7TBGO+8+u4/6zq6EHVYSOUWb8GkdB" +
                "/PX9Z9d6PeraYgjxr1rbWZIfNZE2T0GKmao2WsOxcuzm01HdV7" +
                "Q416jW1N7uA1GiJwpEaIsse12Pz4B606wHqdoakLub+58rmnzO" +
                "4tpkN7QBc3Vgupf91j4fnSs5SvToyGbfCYivRF1l1oNUbQPX7/" +
                "MNn1w/1u+v934/1JkxyfX7tKD7nmm9rt+THU2vefpT3Mi1yQ5o" +
                "J8Knb6Nf9FuH+SBR72BzsNq3/+uPT6DMepCqLfuk59jWm51ZrS" +
                "nZRve0pnyzXY86M1v/ly1p7gk785r9c4Gu1Uc+PsM9y9/81j4f" +
                "nes4SvToyGafXwyZQ2e+WQ9StQ07f3beHbIunPj6ffTLb916Mw" +
                "pbN/YxEvJ86aZ+cRZL2Bol7sP1aNHk8tkP395P422y1JE232YU" +
                "S81IxE5u/xzcYeI1S3SN2Qrm4LMD3y5ev96jnS5LnZ9mXyruMC" +
                "Oj6b19fvnfTbxmicsX1cEcfHb5JT9vmYOb3XqbNro/ul/WsgWZ" +
                "HBeRKkE0jrg9LegBMdU+shM4qLUal8knRXIZ16u2GInqm2J53P" +
                "ps+R+gKDp+JdbWflImSjEotTCGGiR8xLWcTdlHXgYN8LgsoYY/" +
                "U7R6lBRJbfLzqi8Tq54Tt6TJwKX4kqxlW+kuwbi4EySIRD2V0C" +
                "LllA1Z6zld5gyogZZGYfapx65bIBLx+b/UGVMGjED1zP1hxBxN" +
                "7lh2Ntf35vl85yNcm+yE1n894hjlmuBd84GncJTo0ZHNHny7+f" +
                "PXzHqQqq0BOWx4vrSOa5NhaAPm6sCMft1vHeYDUaJHRzb75vnS" +
                "sOf50rBrVmrbrFNXtFfIur2CyuS4GJJSlCCa2lGNxJtk6GOwxa" +
                "2pHY9B9WmKk7Ko3iivlKi+eRw0M+psuUceMfqqx6vaq7AvS5nP" +
                "r7Hcr4KCCGzVvvaEYRX6KGZTfzY8ZTShkQOi0uPitWjzhTQWHr" +
                "06F1q4H1/05Z77GcP5c7mu1Uc+PsNqaY7fOswHokSPjmz2+dKw" +
                "OZj1IFXb5ro0Fo/JWrYgk+Mii8eKjpQjAvQg6VxADXJRNmStj/" +
                "drOANq4jEamd2nKXbdApGIF775jCkDRqB65v4wYgU9Ho/LWraV" +
                "bhzGycPxePKwlCMC9FRCi5RTNmSt8/l2zoCauPneW/Wg+hSx8d" +
                "h1C4wBIxG++YwpA0ageua5wogV9Kn4lKxlW+lOwTg5EJ9KDkg5" +
                "IkBPJbRIOWVD1jqfszgDaqClUZh96rHrFohEvPDNZ0wZMALVM/" +
                "eHEXO0/N+Z4i7+7Az+b6XYEPJ7Aur/uvD/xzG9qVHcABrf7wkU" +
                "G/v9ewL5wpDfE/A/TzT/ngC+r51sxeef8L52dC+8r51srd/X7k" +
                "osvK9dW7D3tdNR8b428In3tbX7z29UGsf72snW0of3fe0KVb+v" +
                "XfbmlWU+0Zjf175Xvq9d6h3va9N8VJgNXFpa30PH8L52fDI+KW" +
                "vZVvvuSRgXi0CCSNRTCS1STtmQtc7nNzkDaqClUZh96rHrFohE" +
                "fLFQnTFlwAhUz9wfRqygj8fHZS3bSnccxtFikCAS9VRCi5RTNm" +
                "St8/ktzoAaaGkUZp967LoFIhEv56Rzcwt1PlSPEpJPQJ+IT8ha" +
                "tpXuBIyLLkgQiXqQlPdLjQa5KBuy1vnMOANqoKVRmHwiM2fhFo" +
                "hEfHSjOmPKAFhA8ThUPySfgD4dn9aeRFUyUYofSC2MoeajMp+n" +
                "OYZqTR5GO6CRViaMiQctdBSNQPVL8cUyPgez/+Ljaix6dOqIS5" +
                "JthvXRSl1LR51POp4vbXM8D9nnXflsc9mbUKJHRzb76Hp/fAJl" +
                "1oNUbZ3PVjCfnwr6Zmzi329+p/fvvPav8egTS6bmBn2jNneSYf" +
                "0PE9mi3w==");
            
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
            final int compressedBytes = 3238;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXO+vXFUVnRolWoFCKaVQKbUKtCKxQlAaUG7fnQGBohAVUU" +
                "AoVIoYlSoxaqLhvFfLDNQ0WCABNKmfFEoFqui/4Bf+hZaJQkhK" +
                "mjQh+snEc+6++6619z135pW+gZmcX3uvvdbet/e8OzN3psWmYl" +
                "OvV2wqqlEeskrr4WtihQVozBbeZo/guTGHKOzdz6rwtHPwmrk8" +
                "mcWrMa9Ytp5rtW0eyq0ortYq2oyhVa0uS0+spMXj+Ya3SQMijQ" +
                "tv6wocdu419j7jvf6hWKjkMUDpSi3WA9vWNTl+tlXHc42tmBW5" +
                "knwdxX+kyWP+XzpbGOSRwC7Vo82olkla7NMapsUNr53OO93f5R" +
                "0cGxyTPo2w1fadYlUMZryyTfFWoa0BS+ofvTWXWVvT52lZ2P/o" +
                "OmSEDLSmNjdXiAy5dp+JzZarLtalJ1bSIuL73iYNCB25Zzuvvc" +
                "bEf/91ljGHB6Nmhby8BzapyfOzLVcxK3IlPq/w0/Dh2H8knBY+" +
                "2thODyvCmeGcuDeKcG5lWR3b2vCJ+BfgpjhbHzaEi8MlcXZpbB" +
                "tj+0xsn43t8pphMylcFa6uxi+F68KXG2v8SxKuD18JN8TxxnBz" +
                "+2iFO8Jd4e5wT5zdG9v2cF/YGR4My8KHGsTHwvLYnxHOCivjeH" +
                "ZYFc4La8IFen6Gi6L1kxWyulKEz4XPx5quC1fE+RfCF8OWOF4T" +
                "27WhCHNxLEM/VnhjzZ4q3RbbLbHdGr5W2W6L7Rvhm+H28J1wp8" +
                "n2/th2NP8W69MTK2lR+9/eJg0IHblnO6+hMfec97bOvfWWMYcH" +
                "o2aFvLwHtrnncvxsy1XMilxJq8q1xVrpZVSbrEcfL9bu2csWoG" +
                "0cRmHgxqz18Xw2F636nIPXBMee/1mb9ftY1Oq1dT5/gI+Bzd8r" +
                "tDNu0BcWF0ovY+W7UNejs70FaBuHUfDcmEPU+0/kolWfFb0mOL" +
                "zN+n2s2traPnvl76rW61h07kGvl27pzeTRf6w388ej6z4I7WJz" +
                "sVn6YjPbZD26VKywAM1x7BF8zgaN/uM2muNsDl4zlyezeDXmFY" +
                "vXtnnwkfHVWkWbMbTsY+vD5vz8es6/BOfncPbnZ1ems9Uu95f7" +
                "pZdRbem58ProMrUACT9buImd2TimqmlkGdSuMZyF1fN5cu4+J0" +
                "YCn7RtxcyADHy1+UqQbd32lfukl7Hy7dP16Eq1AAk/W7iJndk4" +
                "pnqf8IJlULvG2CxYz+fJufucGAl80rYVMwMy8NXmK0G2nLs7Z5" +
                "/V2Whjt2/iWf/sZMtw0de50aYc32Ky6MIMt810vz9ZPim9jGqT" +
                "9ehqtQAJP1u4iZ3ZOKY6Pw9aBrVrjM2C9XyenLvPiZHAJ21bMT" +
                "MgA19tvhJky7n7TyJ0Nndmt2/So41aXNz78XqpXdOSnp/PlM9I" +
                "L6PaZD28Qy1Aws8WbmJnNo6BLhjYzpn52HaenLvPiZHAL7ziK2" +
                "YGZOCr7arEonOP8Ejzt+bbLd8L7lXJ9VmGzR4VDr5f56VmlM8s" +
                "vk/96kzPz6fLp6WXUW2yHm1RC5Dws4Wb2JmNY6rXS3+zDGrXGJ" +
                "sF6/k8OXefEyOBT9q2YmZABr7afCXIlnPfulJa6/3m7a1//QoJ" +
                "bIV6+6Rfz7+Q481b2p5a9VvWpzVMi1PtLn93Rov35t+/L7y+cO" +
                "eM3r//5dQ5fnPDFP9gdtoT9vtT5VPSy6i29Izvj65RC5Dws4Wb" +
                "2JmNY6qa/mEZ1K4xnIXV83ly7j4nRgKftG3FzIAMfLX5SpAt5+" +
                "520r3NbPuMzs+XP8DPl2aqjftK/VX+Plc8P+f4PhS/jmyvpt2P" +
                "MzW9ynfo+H4csrB3wbrugzE/Mkgscj+unU//VZ9ljnXa6+f+qv" +
                "z9uP7q/uoasbrBVrbURneJVddAtVfAMBcrkO2geiQqlwUitddn" +
                "/U70bs9vs2Rdw3rQazFr545a7dfWotpyP25+7fyn+fVnWBH7c+" +
                "L5uZ3vx1Wzn9WYi6v+0nq1iPtxRv3vlWfi/bhovytUxyxUf3/8" +
                "/bg9b9WoM8JZ9SyelWFNc1W9It2Pqz2bWDvdj6usW+qxvh836S" +
                "H344wlez+uv6G/QXoZK8UNuh7drxYg4WcLN7EzG1jrmg5ZBvJs" +
                "aOp2Cl6znXs7AkjCH/IVMwMy8MpWDxlbdP1vac7PhQea2c4ZXY" +
                "/+dOocen52vuq7Ynbai7ketb8zEs/PB2d0PXqx63qUy+3kr0dy" +
                "fe+4Hr24FNcjb2muRyv7K6WXsVJcKc/49/NhtQAJP1u4iZ3ZwF" +
                "rX9JplIE/zvsMreM127u0IIAn/mq+YGZCBV7Z6yNiiy0PlIell" +
                "rF6bHtL16BdqARJ+tnATO7NZ1qj8Z8ugdo2x+LxmO/d2BJDAJ2" +
                "1bMTMgA69s9ZCxRWffM72s7zdHv+zyTXnX9fJkT//w7F/Pd+UA" +
                "7fJ9eFfhPg/54YyuR698gO+PZqpdHimPSC+j2mQ9+rVagISfLd" +
                "zEzmxgrWt6yTLAoyNnkdds596OABL4pG0rZgZk4JWtHjK26Cnn" +
                "509an1PtnLzu+HTLofp/nYY4hc+Td05mbGsv5aM4rzhPehnVJu" +
                "v+770FaBuHUfDcmAO67WjVZ0WvaTksiv0+FrVKTW1uHAvOsF2t" +
                "13Ho84vzpZex8p2v61HwFqBtHEbBc2OO5lhkolWfFb2m5bAo9v" +
                "tYtfV6w+/6im32yt9Vrdex6Cn7/eet3bRt8rpjDzrU4OlpiFPY" +
                "79smM7a1l3S/v1O8I72MapN1/3lvARorG88NCsDAiij25Py8zn" +
                "FYBKupB5mkmnLcOBbK3q7WPhVl0MeL49LLWPmOy7NcXi4vju9+" +
                "CBZB+JWN56ZsYK2vhsuVg6PN8Tye1wRyeK+1Wb+PRSZeW7HzB/" +
                "gY+Np8DuB06GPFMellrHzHdD16yVuAxsrGc9NYcNTH4j7l4Ghz" +
                "PI/lNRlpbdbvY5HJcHueG8dC2dvV2qeiGF1/NhvfKQzwqdLjjb" +
                "157SvewQM6LuKTlgdO5e+QKo1eXQxK9XjVpb8wWkx+C7/N+9Xq" +
                "R3c2/FdazbWsme3LI4F9b4/hjhxv3jJJi31aw7S44f3Teaf7u7" +
                "zlW+Vbg8dSL2M84vUqrdNcekGmHn628CixaaZsqiQzYWQGGSUm" +
                "eWUGP1aap3JAzzKiMtFWRsmIoyyDxAqKveyHpTlrH0Nkrzf3Js" +
                "atu+be1NdLc28u7O/1dq9Sf/UqZBevbHzOlvexdfg9G5O8uSjP" +
                "vfvcyTls3cXW3MzO5w94Pss5qW6HPBGP8I7BjrkTgx1plXqZJc" +
                "/uh1Jf/StU9uRXhFjVL5Hg5FEUxJ94Bzsyx+mEeiUj1eRRcXMn" +
                "hj9QPmRl80JGkqP4RUXn4J0/AD7x4EgoVuaqhNowq3bDMvmMul" +
                "wmK/2suVymFv4MOvntZ/Hql0hw8sifgife3Ofhqia6ufsBwGnO" +
                "yIF/kamfL6mO5Ch+UdF5152E5MGRUKz99Spqw6z62/pGr5d+b5" +
                "jGtAq/iusz00wt9T289HvDR6J/fdgQV5fU1o3qj57LmZNH/r1h" +
                "4g2Zb8OomuiGO6qoe+ro7RZXIarfG1belZGx+r1hnF+A4xmt9e" +
                "8NK3z1e0NRSb83rPzX0D3K6veGTS03xdb83rCy/JN/b8i1YTa4" +
                "bXBbcTj1MkZfvUprb0l4u2KLoGGrlA4LUpTUBg5By1gcVq/3K0" +
                "Z1BGttHKE+zUhiVMVGQR9qgmKvVUAeQOs8ro7IWVwcSWex9DIr" +
                "jqgf+z357X5Xv0SCk0fe74k3t99VTXS793vyas7IIbffVUdyFL" +
                "+o6LxrvycPjoTG2f2O2grzyWcxrvf7ODyfVs1+HyeP+Jv9fnP0" +
                "+/0+Vh7a72M7mv0eebP7fazeKqPu/T6uc+b9Ps7u93Gz38fNfh" +
                "/Tfh937vdx3O9js9/H9vfFXBtm/dP6p829m3oZ47WqXqU1LIJM" +
                "Pfxs4VFi00zZVElmwssMMkpM8soMfqw0T0VAzzKiMtEG3mbJ+o" +
                "gVFHtVgTNJnPX1/d1GdUV/ResT7MqWWjlALzhF80r9MjIKbO5K" +
                "PlCPRHoM/KylTyCASmtk6XWZvZ1lW5/5FdO2+JVY5i9vfT/kD8" +
                "1flN9Jm3L/7lOxZf5fkvmrYmt9d2l+4xLdNbzovX0/ZPijDvzF" +
                "LcuVJ5XPFnovcbY0/T5Y86/U+p6WIIFlVHnzou8CZnnx2H0GLN" +
                "ZjGdinNUyOg3aXX1Hev/v07nynHOnmjFz442LwJ//9+T27Tv38" +
                "fK/f9x7+eKb3i4+WR4ujGOO16mhZP4ujaS4WQaQe/rRSC0ZhLe" +
                "pZUSMlvqwVwKHMoqPeorFCpaS8BMvRlhGVqTbwhYuCPmIFVRj1" +
                "opVJYdDi7f0fhrEpvQ==");
            
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
            final int compressedBytes = 2455;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNW1usHVMY3k0o8UIbIiGhcQkJraR1OcXRvffscYqgtC5Nhf" +
                "DggQdJW9HEA2fPVKeoEolbgroT0VethhZ1CalLH/oi50lwTlwi" +
                "vLg8MGv++ef/1+Vfa87eexzrZM1a6/+///v/tWatNZczu9VqtZ" +
                "I3Wlqa3AhlciDqqBKOmPpft4yEeh1XoscKphexnbzeP+hGuhh9" +
                "iORlv0X/i0G5EdVfVscueSV5VZd0NsVfqr/OJmy1Wip3NqEE2p" +
                "ABDS11BDvAg4xYwBYtwQ54CcnjQC36QC9UIg58Egr7wOOivmCM" +
                "IDP1mJAN46eRQDvwgZ6w38hZjfw4L6PxZB62UaKdj53WuRzXSw" +
                "39u/Psj8tS5dWt57hofPOEn3FzzKWumhQHapLf/B7cnNUZeQQy" +
                "Hoszth0yyTq36whouxJhbBTXSQgboycVF0dh/BihFFnVEy+/5N" +
                "+0DkWZvEv7Zz7uxRxIvtcQn1vnaYLK5H0Nu7vcP9+uJO8V++eE" +
                "4H2PzljI9uf5M9unsX9yi318/0x2leXe4vix6Tt5xxHHJwpl7J" +
                "+OmJMP8/xR8qkpb/8JGVLvUbumIwk7+6Q4sw0uXrdE8qV4uA77" +
                "ELLLbvXr5Yjqa93X93zsrWt550p/W1g9DNWfbLW6H4d4wyk56P" +
                "clMdq+R516sdQ2NXmUV/nbQh+v8ntUCFs2WEJfUmSj8iOl/uJo" +
                "rH9i/7R8hyj2umgM52c0hhKOTxdYe1mu75+l49KFuew807bwd6" +
                "ZLSn6UV7ee4/KYT7ZtNT9LudRV0+v9020ffg9uzv7iPBfjaa/3" +
                "7n21zsipMJ6W/Dy8nzfHcyTz4OSAfqmw3oU+6eNZSJbNKp7l5c" +
                "56qD2T50N5baZoz6h6WSsl2k48Y+3NM4pF17hkzN+Mc49H/4Je" +
                "466iFKPSvLtqRv2Q7SPcb1vnm59bv2lmfmabh5+fyc+Dzc8sbX" +
                "J+xgfiA5171BHKfB8vW6pNEkCqI+m5hJdgq2rIhp6gBrycAUqw" +
                "UVqokZ5aGCciyJ/OSD0D34TXo+T+yRZQXIseeCSKs7z23UOW3v" +
                "n5UzPzM3587uan5Htk++culdu72L5Q1bf+UocJGNxytzR+dvjx" +
                "dLOH9ZLvEF/9+yV5fj44r6H5+cHcXd8l38POz+q87FZZHbmkHM" +
                "8F9Rnccrc0/mgE83P3YHrJd4ivdlyfqKyOXFI+S3xYn6GuXEIP" +
                "EvcgeqlP7khHvN6PG3a9O6Rqve+fw/W+v+56HzRFS3gZLemvxz" +
                "ZKXGjJXpb5OLh/9OuLN1pivE+2LIr3yUtsvStKyUe9frt5Og+r" +
                "3LkLauW5fAQyl9kt4b3Ew4PpEBHCqLg4CuJn9nf5ffv5Jf+mtY" +
                "lKT/Cu9+Mbej56cO7We7a12fvPfN+d4mV7CsezPYUSbZeesvbt" +
                "KVvjkuH7T+R18yitW89x7Sl9vdsWar2T1FVzWek+6vXbQk7zsj" +
                "3dW4FtlLjQkr0sI51LSv4lPcdRlJIfpSepq+aOjvuo129b1/4D" +
                "srneo8ssjgJJWI6y0c51cbTEqzOiRNcQon8F16WHYx/8duRb0i" +
                "PK1Osx+62LnXUbZDwW++ciri3Ka3WEaifTEh9HSToJYWNcPlgk" +
                "ZfwYoclo9WRbXWaftYnqLe8tj5aqI5T5eShbqk0SQKoj6bmEl2" +
                "CrasiGnnrlns05SAM2yivUSE8tjBMR5E9npJ6Bb8LrUXL/ZAso" +
                "rkUPPJKouuIhko/s5JHV2jm7Gu297rM3eYr2pmd6ttfmdHEYM3" +
                "ljDcxRrJ4/G0+y+5H7TxKu7+V/zCcvCHBf7tXeKTx9reyt7E6r" +
                "I5StFrZUW9XTO0CuJOpIei7hJdiqGrKhJ6gBL2eAEmyUFmqkpx" +
                "bG2Z3O1nNrk5F6Br6RESLiVljv7yBbQHEteuCRdKt5hMhqVzgG" +
                "cnnF+LaaR/ndY/eAtn8USMIOlnRO5HVLJF+Kg+uwD3XsXHozJn" +
                "8fZzMC7H7+FMf12fqfvG+9O+7nD+bPNk8Pfz+fLg/cRwjf19X3" +
                "Pcj7unhRvKj7nTqqsjhPZUu1o4fiRemxJFF4vYX2qg5tYEUZoN" +
                "ET1LrfoVb9ZVtQA1KlJR+cmeIqxvM4XcYt9MggDoyO++b1/g60" +
                "xZHhWmLQ48AeYZzl7D0CsrXeL7bmeYEk7GApe97F65b4fHEd9i" +
                "Fklz0X5g3r/dr2X5DN9d7b50YSdlTJZkSJzxfXYR9Cdtn2MG9Y" +
                "L2m7C7sL4QglytRfND+ar0sAYbZ0e57JA2GK+7X5yMGtzbhcPj" +
                "lSl+l605YiMX1zrB633Vv9D1Ec7dzpqy980ktbI0/pRO55Xqvx" +
                "lPaEGdSwb/l9XbQiWtHM+7p6vM28r5N8j+59nXUGn+I1ahX78D" +
                "pjX14X4nChuBbqEo/nHvYp4UqxbjaRheWzXBW3pKvTG9JrNNna" +
                "KrYLB+a9TtTk/UxvHkHkawJ64Xm1Xp/S60e489xW+b7Iimatvy" +
                "30YW32ZAgxquiRSWK0+9Rk0r+fz56p83yk2df6fl603uOQOb+f" +
                "z2Uviyz7+POR+f18Zjwfub+fr7d/St/PGzvJTXatOrur/G1hTq" +
                "xysc+exx2lm0lilOwavOKvr57fz7Givdpuh9/XWVb/hBA1okz8" +
                "viRG23fTidZ7utGKZrW/LfSBodTvEfyIIcdztZ8x3fCfz8/Dqr" +
                "VxRut/mzZPBPTC7w7++z6l91bXox3W2V/jbwtzZs3wiNrX9zV+" +
                "xvTcuXo+yvfPsWaej7IX5u75aEuj3zNEP0Q/8BLr0I4fQC0isM" +
                "Zb7szZiJWkJNE1dhTcpxmnyUJ6sqUj9cnm1sfA7I8+DnbEZq/j" +
                "XtyjOuR8Hmn3eCgHLOLjno6QkrLiPnxY4nJ5sTkwKjsu/ajK9B" +
                "Ieix692ReedT+h6PP95jHIeCzWe5trdWwdvkF0iAhh1PWIoyh+" +
                "aEnXI8T4+SX/prWJ6q3qrYIjlCiDdvdYlBCS9FzCM8g5G7Fyv8" +
                "RAGhsh+bRjty0ISXjok82tW5j94XqS8HHksRvr6MfqWritmWtg" +
                "57fm71LE/7+/1bzvzvmQzet79oobSdhRRuCW+HxxHfYhZJd2wr" +
                "xhvaz1/n7z7mbul0Zynzfo7zfvrnO/lD4xzPvk9t+QrfG0/v8O" +
                "SMKOKtmMKPH54jrsQ8gO+xTqg18vaeOv4q/gCCXKoJ29iRJCkp" +
                "5LeAY5ZyNW7pcYSGMjJJ927LYFIQmfXmn2mDNQBKZn3R/vC0cH" +
                "no+6zaz3zq9zt96znU0+H8F4Su+XusuG7rXziTV9bQRvGZ4L6F" +
                "8S1vuyWvvniwP+Pu5fNXnsAg==");
            
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
            final int compressedBytes = 2183;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2MFEUUbrxICIlRYEFWUAQWcSXxhzUaLjM9M9lN1EQOsM" +
                "STJtwMCVHDBZQw2832zIhw8iDhZjBBEEKMRC7+oAcPICzyJyoG" +
                "UHbV9Q+MCZJo97yueq/+emt6p2fG7UlV9Xvve+99VVPVXd2z4D" +
                "jlFY5T7i4vcfix7RV2Vi04Fkd5cVh6Nfq+sDyhaB9wnPxNZ9JH" +
                "+d4J7I/q9abc5aXszDsQax5riM+TUe3OdedCDS3TgVx9WdYgGq" +
                "WhUVHGghkwR71Pt1gMmp0eop3KiBR1ol32RSZybopF3vl/dL0V" +
                "PxCTccFz+fCO8vm5Rvl2n0uWDXNC9ro+EYIzWNvovGWRTBGDg0" +
                "6GhzcSlkvx+VhYvvJ+Yuvd+6W4st7+xdEnw3l9ikun4/Zs3P4c" +
                "lj/D8mUsX/Suw3r3TnCfc+UR75v83xom33nfe1e9y96v3hnvPN" +
                "GPh+WGgLzg/eA4Q28Luq/J+bfejyHPE1L80Xr9u5zbO2VcwVbr" +
                "3fvN+wOl0nhpHGpomQ7k6qtMg0i0Uw0t9U8XjYZR46xdYgRiGa" +
                "fMknKq3FUPRBJ8l9xjGoFju+TMYj5kjLxFjfZ+9JrmW1O+z6FR" +
                "zbcWXynk+5E/sxyuiOCDya8scX5qeJ7Q67eftpp1a23npz/dn5" +
                "GMIOO5NW1vS7OM/RxJsmY/nna5m8FQM57bbOanls/MpPnJrG0Z" +
                "z5m2/NPNz6T9p/tINvvPZhxp95+mPuH+s7H7kbj/JPuMnVBYXZ" +
                "+f26m13q4TESBr9y07yfk6s82EUDG6HIRJzJ8xNDHjPdlpGznJ" +
                "W4fyH4rbB8Nyt7+IzU9/cXDEZr373eS8R7Ash/np852uf095xL" +
                "/PsHLiOe7Pb3y9+2S2+vfr1ru/oF4v9Z6X9PMmt1/yl/jLkhFu" +
                "VT3j38/qZFnvKaOCLyaKm/5gkUwRh+c4mR/5Pijx9/ISH5XhsL" +
                "yrIhFr/E6GG2eg15hyRbyojfXBxk9nF/tp9rexSvOcj6cfXrvd" +
                "fUIc6fkvb3geLF+yQTWCUMZlX3IkU0STn0nfhGdQ/vzurlDYrk" +
                "mW+Tex0gbVCMJ6pq9Jjqj2KevD3aGecbaDybLe04RqBGE9noPJ" +
                "Ef2+lo+nr54l3mlHk2JoMxydNMej2XoGo00cz9fVs/QxMrsqPZ" +
                "vOz388S1alRaVFUEct6kCu7gAtahBN/agF8O4Lqk7MQb0RI3OQ" +
                "c+p40ihyNhoXNIyZHBuZUoa0t2JGkTFDF/uKfVBDG9lAis6rbz" +
                "ANItFONbREH3cjjYZRGVsxAlpUhCmnyl31QCTi3Y1yj2kEZCBn" +
                "FvPRvlC0ds4e4M+bb6ad97n3nA49Ws+s9Akfz7dSs74qytXdHT" +
                "OeV1udcWg5H4X9qVlf7tj52SJm+V1QWF2fqbugUJ0qmeOlsTHE" +
                "RJjSLhGF/JMzMEtyfFN+2VtG+QP4/tN/Rn7/WT2j2W88bfP+09" +
                "3Qse8/N9i8//Sfmuz7z3g1HA7X+zQ+ntf0iIn38yqq6Vf5sZTr" +
                "/XCrrzD4PqQ6lvoqtc/p0KP1zMh6v5H2eTN3sGPH82Abx/OWZv" +
                "Za/V7svhijtb/HNWUXkvL3OMbMYp026fdin/9KXLstbW+Djzt1" +
                "fgYfZb7fvMN0/azdnnpVHejY9Z4ps2JvsRdqaINjTIrk2vRiL6" +
                "AQGXzK7Ewjl+iT2wsS88McmBcjoEVF0KhUo3JXPRCJ+Nxe0UuM" +
                "gAzkzGI+2heKTp6f/jZnyh1DM1p6dfk8zDibr3fNX50En1mtqg" +
                "sdu94zZVYcLA5CDS3TgVxbwDSIRDvV0BJ9cldoNIxK82IEtKgI" +
                "U06Vu+qBSMTnrsg9phGQgZxZzEf7QtHJ+6VaT+pZ8E7W86y2rB" +
                "OZFfuL/VBDy3Sx7DINItHONEOjaGHW3CEaDaPSvBgBLSpCn1PH" +
                "XfVAJOJzh+Qe0wjIQM4s5qN9EdCriqvcPVENbbjfjaVIjs6hBm" +
                "RUo51qaAu+0RmLhlHrO+o9NAZawCeywhlmQIl5sBiYT+SEPav3" +
                "dhWLCEyolxgBfAFFrdSOGv6MsAc9E/++brNmt7/b6v3S5o59v7" +
                "TZ5v2S/fOR+H6puL64HmpomQ5kdwvTIBLtVENL9HG30GgYla8P" +
                "IQJaVIQpp8pd9UAk4qFPamzRQ+4PtaOGrPf1lLvpfhScm3r7T2" +
                "9rltELNws3oYaW6UAOLjINItFONbSAnkbDqDQvRkCLijDlVLmr" +
                "HohEvLdV7jGNgAzkzGI+2heK1o7xv+pZo+/rJvgWV7dvftrlDl" +
                "L/C77E+1G3VYT/1997d9vcj/y9zfm9QxxPX7P39e60ukrdFc+G" +
                "Tdo5sqmN83OTLapZLMtX+Hhm9NtAZXb7xnP4WqszkvHcn9F4dr" +
                "VvPCtz2jieGf3WUpk7Vednqb/U745EddTWr9exFMnROdTwifCi" +
                "xPzRAlFlHfOCDMxKswMGrJgDcAyLPEUMZcUYIDPgATqWRWSOEf" +
                "hda0S0YgSRB+sR5zlQGoA6auMxHmCyuxC0qEE09aMWwOt0Yg7q" +
                "Tf1EDnJOHU8aRc5G49I+qbGRKWVIeytmFBkzdOF44TjU0Nbvbc" +
                "eZXJnFNIhEO9XQAnoaDaPy+6cQAS0qwpRT5a56IBLx3hG5xzQC" +
                "MpAzi/loXwT02cJZZa9Q10WlMg+sTGa1TqIYGlOXAS3gpccwLU" +
                "bVIVGDDOS81Mc7JvbBnF/MkqwRcxvvR+9Pvef3INO//yycL5yH" +
                "GlqmA7n2MNMgEu1UQwvoaTSMSvNiBLSoCH3O/DWVu+qBHJBJZb" +
                "7cYxoBGciZxXy0LxSd/LxZs3riasfzZn4s9f9vM5bl+0/d4fN/" +
                "Beu6U2+9t75P5Pr5YUa73mntG89sc5d6Sj1Ql3qoDuTKQtCiBt" +
                "HUj1oAr9OJOag39RM5yDl1PGkUORuNG/dpgcocI9CRkXsrZhQZ" +
                "81z/Af1vFDw=");
            
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
            final int compressedBytes = 2218;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uMFFUUbX9ATNRgQjASlWHGYBSSifMBgolNqouFbly6Mn" +
                "HBRlcY10J/htjW4JadMUqUlSuFsDAkRiEGMfETjd9IBJxhGGYQ" +
                "AUH8VPXtW/fcW6+6a6a7uhKr896rd++595z7uqq6q3rG3+hvLJ" +
                "X8jX5rpI1m0bw5TFaxCBrj0EN4l01zYDTGaQ2W06UTs1g2zNuu" +
                "aUNSuWQolbRCrFYzasXCFW3lh6KXzKiFiG3WRk0QPGKPdpxbjk" +
                "4bYzVLWkZWJbqsR2xUk82PNlfFyIiVuOsoD0UvmVELuXdbGzVB" +
                "8Ig92jtxdFzPIZ3RhZeMrEp0WY/YqCabH22uipERK+lWR7TVfu" +
                "W9qQ9LuWyVmVJhW77c/gn/BPU0so3mzRFtQbSOk5Hw2DCH8Caj" +
                "mV8zItbmcKnSbFZBqdQ4aSvW6jm/ZdZ8VnGMPu4fp57Glu84z4" +
                "NxbUG0jpOR8NgwR/sYue6KZn7NKFnEYjEagWxWQZLbquf8llnz" +
                "WcUY2eF8/yins+KO4s73wXPDen6cU023FrieuXKXJ8oT1Eej2G" +
                "gePElWsQga49BDeGsLyshR+VtHY5zWYDldOjGLZJJY6aPRcmsd" +
                "uDK2Ws2oFcfoyfIk9dHY9k3Sq34qeI6sbIn29AytnA3naBOOyl" +
                "kdjXFag+V06cQslg3zksVyax2wnolqNaNWzGjvgHeAehojH82i" +
                "/WA3WwQpfrRgIztmw5jWes7rDGznGK0C+axO1G41IVLwEbeuGD" +
                "OIAlutuxJRi9r15h3kveCVdJ9s9ZnuKG2pnMv/OulSOgju2uaw" +
                "rauNiKX6cryer2XKMBy2xxz28HpS25qwPhLWdL4PurvcsdYeT1" +
                "nPFO7awwnL2JL0bMvw+f5pTvcofxR4fzRwbljPkznVtNh7jqnP" +
                "iuPucJU55B2inka20TxosEWQ4mdLfUY8kguzYUyrpks6A9s5Rq" +
                "tAPqsTtWu/Rgo+4tYVYwZRYKt1VyJqUbve4PrZzPKe1Jf8hKHy" +
                "b4Hne87c0edR6npO95x9q7Omm73rbrzRxf92ynrezPJ51HhrqZ" +
                "9H7ezrK+vdtqgF+8nLc+5dM8HImMpwkT0U5cJIJPf8SqsAFWhe" +
                "peeiVZnGr1k6WzS3OX9viY/P11M+C77v8Zy7kOHz5rsMmK9g/5" +
                "uw/ZCde+rbLrm/XlZlaytreU/boha8Q1aeCyo5EwzmQgawXWEP" +
                "RblUSCT3/OpUAerFipDbcrmyJtcpjdWswHBlmHoaW75hngfvsk" +
                "WQ4kcLNrJjNsnaZr+hM4BnOFZoGCxnUnsyQpCAv2ErxgyiwDJr" +
                "PlGs0V3O9/fyOd/9FcWd78yd0/k+Uhmhnka20Tx4ny2CFD9asJ" +
                "Eds4WZPhB7aJvVGcAzgso6cSa1JyMECfhZWzFmEAWWWfOJYo32" +
                "5/y5tmVN/A62bFELjpKX59y7ZoKRUbKZY2QleyjKn9v7jFUhkd" +
                "zzy+Sasyooy94HBan0rLQqXVkTio2/skZbhLvj85BMz+eX/jzE" +
                "vz3r+RN80u/nIWnc/XweEqhfkqpHY/uJnO5RFgq8P8qV2zvmHa" +
                "OeRrbRPJhhiyDFz5b6jHgkF2bTWcOaLusMbOcYjXdxurQnIwQp" +
                "+IhbV4wZRIFl1nyiWKO7PF86k9Mx8nuBx2fO3B3v33/p9XlIyv" +
                "37tQLv36/lef/unfZOU08j22genGWLIMXPlvB8jz2SC7NJ1vZn" +
                "wm06g3h4RBUuTpf2ZIQgBR9x64oxgyiwzJpPFGt0l/M9p7/1qc" +
                "wVeL7nyu0f9g9TTyPbaB6c0xZEy159BueEx4Y52jWd0awYJ7pQ" +
                "lVZgMRqBbFZBklvrEA2WWfNZxRjZ4fg8n9Mx8k+Bx2eu3P4R/w" +
                "j1NLKN5sG8tiBa9sLjE+aEx4Y52jX9plkxTnShKq3AYjQC2ayC" +
                "JLfWIRoss+azihldni/PU09j5KNZtB9ctBZBy6w+o+fSOFZytN" +
                "fzKudAdty0H+eC1Dbtt7GixHIjVutOVqtfjEK08/tP/Kt7sFj6" +
                "321TF7Kg6vv7yBjfkQXXcrqG/VXg9TMT99SlPq5n/It/JdM32n" +
                "ohf709dWWZcZczoa4uL3t5sbxIPY1so/n0CmsRtMx0PDZhEEzr" +
                "6h3nwGiry8WJSG3TfhsrSvyU3LIWnD1ZrX4xSqEXygvU09jyLf" +
                "B8eqW1CFpmOh4bx4aZVjGmddz/yTkwWq3ngpsTkdqm/TZWlFhu" +
                "xGrdyWr1i1GI7vx7x9T1fM7V5qPFXT9fbeaZfcfYjjHqaWQbzf" +
                "2atQhaZjoemzAIRqwShR6XH+euHBqBbOwRJVFNrtyyFpw9Wa1+" +
                "MQrR8FRpVfxsZVN8HG1yvwvVDb29i/vu6Y6pPpsBcyfsrw7b2g" +
                "zPpdrnXHVLl9xPdfS+uIQnePH95r57c/rO8mOB35dy5fZmvVnq" +
                "aWQbzZub2SJI8aMFG9kxm2RFXskgniQijTOpPRkhSME39tiKMY" +
                "MosMyaD2tBdHwvvMvfFfW0T80b9UbVvX7L7o0iVka9z5GSIYpi" +
                "f5Sj65OFXZjRhZd8pMelS/dUFcZq9XpGKnFdhKe7fri6tX+z9c" +
                "a9cccRPZ7puB/vhM6WI6czcrz/yMzXz/tyuob9XOD1c+Dc++6P" +
                "99blVNNPBa7nQLj9nf5O2afmTXgTGhM1b4KwjJc43OdIyRBFsZ" +
                "9zdFaEGV1oyUF6XLp0T1WhFq1ezyKVshqap5t6db6/1K56yHP8" +
                "t7w3lOnaM5SOducd2PVzqP/ILt9k49+Lp+/q+Vqc/HubL8PvYV" +
                "uyxk/fnfpU62AX5s9T7jefz6x9rO/r+YCD5Yve17Oyp3eVy13P" +
                "7Nz9Wk8+3+unvO2O+7XVWXI02ndWrgxp1gGd79v7j8x2fNZPTW" +
                "e6gizteXItvHevVPvwrj+9zM/36qDfwWr8i2rziZy+kY0Wd3w2" +
                "9hS1nt46L/z+2XjBnAWZvpMyyoWmvL1uzcllnu8p3LU3syI7bv" +
                "8B/hYCOg==");
            
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
            final int compressedBytes = 1524;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW81uHEUQ3gO3PfAAkcIhBoEQHBKhECTQzLKrQBAoFzjAIf" +
                "wEDokhYDs4PwTZs9kNgyYYRMQJ5QInHiBIYCBgQHDAEkjIh7zA" +
                "SohH4GfKPT1VPdO9rilPb0eeVffufPVN1de1XdMzO3av2+t2Or" +
                "1ur3jvdPRerzv8LTtgIopR3RtOzH1s+lj0obD+O9oHjU430073" +
                "kWlipr16LCqpxqZcU3d9tOZLsyjbtq3+q97zfM51GNtw0mm49d" +
                "/qBNtmEXtwN7xwT7U8n/dUMdX0J20xe4rbY/QXq1abIurFxkeP" +
                "WhXqMo+nWH/R5p9ithHTiHQkVV3RYrSoevWuMbWf3VdFkI17w4" +
                "m5jw0jYIztfL6pfdDoVV22mJRpYqa9eiwqqcamXFN3fbTmS7Mo" +
                "21rvS2W9399+LSSr+ZhWdu9n+Iew3le8nk3W+muqV+8ag1eezw" +
                "c1gky0U4Q2hVNv6LWIumR6IJY1qmxazLr2+hHIJPyl6oipB1RQ" +
                "jWzGQ8Um2zqDFsr5ecjTt/huwPXIc+zkgbztS+as9X6Y5eFA3i" +
                "xnhuShvD1cQ+/Nx3SxBd137WA/6MinI3ZSW/GSRrMpOeKy6Hzm" +
                "69Ej7V8vJXfmYzrbQj6fEs7Ps7OuCDI/j3vK5/mA+TwfKp/5/D" +
                "zRls/sRWN9PxPw/Hkm4Px8ycP5GubnpYDz81LAfC56qvdzAfN5" +
                "btb5vFyugNmyp/v35YD1vhxwflruJZJnWqj3C/7n58p+Rz4vBM" +
                "znqqfzZxKw3pOA+bxiGcfvTerdcj2f33f3327h/v3zHfK96cgn" +
                "O3ZyqPV8pp7Wo4WA83Nh1vMzKSP2jnb23Db7MWE+08f2Xj5HR/" +
                "z6j76CBj1Fiu/yab4HLu5iS3RL7K4x2ZW2e7+ZPu5nfW/Fj/D8" +
                "OYq8Xt1e7V9VvXrXmNrPPtAIMtGukeEELeiLekOvxff0hOkBLf" +
                "qdqrDFtGmvH4FM5KdHqyOmHlBBNbIZDxWb7B1+D3nfw/qeX9Om" +
                "x1q4XhI+70ifDPf7Uvynn+ulkPXOG5OnfG60tqq+5rScFM6z48" +
                "J8bvjNnvt5Rx77JstD4+cd4+da0C183uEak/m84/In7T3vGJcz" +
                "Jsv8/L4UcotH7bF427iszfTZPXg9fyrc+TNb8zRH7gg4Pz3Hnn" +
                "b+zD70c/5MXwh3/kyfn/n58/Uynx95usf9x/88HM+HiR39Ag16" +
                "ihT3uof5Hri4iy3RLbG7xmRX2lzPtHpnzvTm9T4frt7fu86p92" +
                "1EVO/RFjToy0yXn3uPsr6ZLXrMTriNKZqfWzK7a0y7VarYg83B" +
                "purVu8bUfnZtsNm7pXBkaLtGhhO0oC/qDb0WY7plekDLoHxKQe" +
                "31mNoH1V4/AjWgEjjOHLH6jP5QCfVr5goVm+zp9Z6+4afeW7ku" +
                "Edb7aOy33qet78xfem7L+yPX+p6eDnf96W09auGZ2PBv2fy88p" +
                "fn9egGNOjLM2v5OWZlVnmw43x24/XohszuGlMbmrb9fAkNeooU" +
                "tXGR78GO89kS3RL76LrM3+1R7xYU6r2FvyGSrkf8em/tTF6uR7" +
                "GHv68LvR7xxrSLutmAFm2YSHMPXNzFluiW2OOTHD6wZCqjH6FB" +
                "T5HmHri4iy3RLbHHr3D4wJKpjL6BBj1Fmnvg4i62RLfEHr/M4Q" +
                "NLpjL6GRr0FGnugYu72BLdEnv6MYefXpOqjG5Cg54izT1wcRdb" +
                "oltij09w+MCSqYy+hQY9RZp74OIutkS3xB6/yuEDS6Zy6vNi1v" +
                "+/c+43R/+5fg8ZCX8vFz8vnvNzvxny/j3eF+56Pv3Ubz6jH6BB" +
                "T5Hiu9zP98DFXWyJbondNSa7UoGur6FBT5Hiu/yM74GLu9gS3R" +
                "L7uMvly1RG30ODniLNPXBxF1uiW2JPv+DyZSqj76BBTxG19df5" +
                "Hri4iy3RLbG7xmRXKtC1Di1aNxG1DU7xPUzH0ZOLLdEtsbvGVO" +
                "fLVEY/QYOeIkXseb6H6Th6crEluiV215jqfJnK6Fdo0FOkiH2a" +
                "72E6jp5cbIluid01pjq/mcqC/T8vprQb");
            
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
            final int compressedBytes = 2082;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW01oHVUUfhQX1ool2lrBthZqQREJKFLtqpm8WTTpRvwtLh" +
                "TcFbswcSHBCHnvdZoBV9IqNSItVKtiXGiruHAhCEID2laXuig1" +
                "bRT8RRfdiO++8877zrn3zryZ6UynZIb7951zvu/cm3l3Zl6SRq" +
                "N1f6PRurN1d2NwzL3UyHW0tnfLfR78oW552EHvaTTGNjeu+mjd" +
                "NcT+gB+ffyfBf4eDPJgrn0f6baXr6UG769n8tr71TNJ217PYsf" +
                "tTU0wtETrCA9kZ/LjLlORdJO8i9qQ5lZGT/4gO5PNvr+RVaN7Q" +
                "qPyIXqhPO2k9XztSzXqWccSvF1vPseeysGfz8l7nS6aYWiL5Gb" +
                "o5zPjxLGjeY2xmGEeS3c4y2T9fluy9+4wpph5YzuRczzP+GD+e" +
                "5J3vOHT7MI4ke3w2q3++LNm7jueleLG++/uhy6vweSmq8Xkpqv" +
                "h56ZQpppZIfgY/nt27SN5F7PFPxfgy53XaFFNLJD+DH8/uXSTv" +
                "IvbwlSz+SV5FDnzex0av3+elwk8Go9Xyp+2f8a/V3I+iJ2t8fz" +
                "+aZf/svHF93o8Svg85Ud96JmmXc38PZoNZqqlljMYuAm+M2it6" +
                "jAIFaPTusWeZQ6rLQ9vlGJ4a03Y7FpnY2tJX5+3OVp9YH3iHcR" +
                "hTTW1vL455DASesDPSXoEFXJINrP1r5H3NAAu3MgufJpg1i46Q" +
                "+TNitPWMJQMysJW1HjLW3un3o8ruCR/WeD+qVDt6O+/+GS2UcD" +
                "86Wt/+Gb6cZf80XkX2z3ApXKKaWsZo7CLwdhG7j1hw9J8bTmsG" +
                "WNAO03Qxt49YZBKfSs4e3GzRXPrU+aLvfN7/S70+j5fwmfugxs" +
                "97xdq1PC+dLGGnerfg89LJKp+Xmsebx4NlU1Pbvev3R2Zs7J0N" +
                "hBvE1LBLxLRcEMtsrES9YFlyxJ+xhWKMlXpQwAh5djayVc5BR5" +
                "AnsfIMMBtkbs7WMcRyjFTmmcg8Bk9uyzLy2t7fW+u7ym+WwDNZ" +
                "LK4M7RT2Xc1dVFPLGI2BwBN2ichix0rWvuphzSAsu2RmaZpu7m" +
                "6EzH+AHLZnLBmQga2s9ZCx9vbuTIuNVXxEH2Xy+rjg9bmnuYdq" +
                "ahmjMRB4wi4RWexYydpXPaIZhGWPzCxN083djZD5D5Aj9owlAz" +
                "KwlbUeMra8J5uTVFPbs03SOT4yPtKc7OwnHB5sl4gshEs2sJK6" +
                "4ZUMsDQHu6KtYGvGX9i5uxHIAZkYbT1j6nfvRyLCno9kBCLWk7" +
                "0nmhNUU9uzTdA5vnZ8bXMi/pJweLBdIrIQLtnA2l/PtZoBFm6h" +
                "kqTp5u5GwBP+RlvPmPrd9RQR9nwkIxCxniIynJdtON/ax2ODUC" +
                "3exOadN7h51+LDYPOh0E+yS7/hWWnU10vus0a2ectesCPYMb7G" +
                "1NR2f379kRmbfmc/EOOvRxKheGC9K3ENeZISY+Agb2rH17DVtr" +
                "MP6xiv+CuNyQj254wohlV0FPu2jkGNvKRVKyAPeHO/2y7INljg" +
                "6zNYYER9M+l8H6LjkzHYfJ7Q13ZfvMvrj7C5k7L0zzLrvGWvub" +
                "O5M7hiamq7tv7IjE2/s59wg5gadonIlmJNj9lYiXrEKxmopRhj" +
                "pR7sGHGewZX4GxltM2JmpM2MlJGM4n53/xzEkpe0soLMxHD21/" +
                "MKIvO+H7XO5XqLsd7fD97cOt/V//rqnyPbQ35n0kr4u8Ts2tne" +
                "3w/eePAmsaduD7cHl0xt2p5ef2TGph8vATH+esTxsBCrD+Me8Q" +
                "LhllBjhQbZ2Qd5BpfaFzQmI3RmlAdhrKIzN2frGMfyKkgrGHQe" +
                "PCPO0/tzeT7nlVLD74uDz6uNjL8rxh62wzbV1DJGY9MPFiUCb4" +
                "x0vCxQgE9vTovMIaPtvHyafg7tIdXYgkxsbfYlXObtzlaf7CW9" +
                "y7g+8x/x+fre3w9trPTbs00e7LZCTHcMercO/czNlTiDDTk/73" +
                "OV7kMjwQjV1DJGYxeBN0Y6XhYowKd3ff7AHDLazsunKT01pu12" +
                "LDKJv/dzYy2Y3Z2tPrE+8K7n+gzfqu/6LFPb833d3uZeqqlljM" +
                "ZA4Ak7I+0VWMAl2cDa/3bxRc0AC7cyC5+mL3c3QubPSPyjPWPJ" +
                "gAxsZa2HjLV3MB1MU01t79qd5rGLwBsjHS8Lx4Jj8FmdlizS4r" +
                "PLsY9De0g1tiCT+KKfG2vR25F+9s1Wn1gfMeupYIpqanu2KR67" +
                "CLwx0vGycCw4BmsxJVmkxWeXYx+H9pBqbEEm4ayfG2vR2xVmfb" +
                "PVJ9ZHz9jZkaZW8+87wlfL8/Lc37cF26imljEauwi8MdLxskAB" +
                "PkARJS0+uxz7OLSHVGMLMgln/NxYi956zvhmq0+sj55x8vUZ3L" +
                "v6rs+q55T778GcN9sC/y/zWwl5V/r/MtEnxf4+pPNs5zHb0tlX" +
                "wi6V+FbXeSbNmv3oPD7kN77n8maW38uTVXc9O091Hi17PTtPpK" +
                "1nGcew9ew8nfDdwR+Z2Av+hX+4hU6NlXB9bkmzVKuQbs+mXTTD" +
                "cCudGithtlvTLNUqpNuzaRfNsLL9c1Pq/rmp+s97/szye127/T" +
                "Ncl7qe6+rbP7Npl5Fhme9HrZnUb2Heq+/5s1rtYDQYpZpaxmjs" +
                "IvDGSMdTCdcHo1IBPr2f/nrmkNF2Xj5N6akxbbdjkYmtLX2xBp" +
                "S/PVt9Yn0w85quzxM1Xp/XXLuMv08Ob0lc6bk0a46fWLvszPJ7" +
                "ed4i/o2Wo4vmfTP6e4BdLjA7630z3Ezvm9E/juIvZL3aI7o0xP" +
                "5nwkoN1Y5+J6/orwJp/Q9ttFkI");
            
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
            final int compressedBytes = 1769;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHEUUHrwYghgVAhvEiMSIuOjBEDS3TfXMScHdzS7GhI" +
                "CGkECEJYiyCbk4M7s70oiQixcvw7oHRQQPIbCQQ7KYix6EqIfE" +
                "U/CSs97F6X7z+nuvqnp30tPdtZluqqrre+9972eqZ6Z6dhsN92" +
                "h/2qj4aH7bCHZU7bvz+qA933m5eD07hwZtWmMz3w2wo4P2tqP9" +
                "aklxv7iD/E0/nkTm1T8sZytfD5AjjxTPsTyJOeNelXeUxVmUp4" +
                "qctj8edX12Hz7S69guaX12i9mtXaqydua0OU09jYzR3EWg7SLy" +
                "euZ7MEgO6RdWkGDcyaeLudewRSQUmT96cLNEc+lTx4tr55X/ZP" +
                "zXqffEdtL46XCfR739la7P5cHZT/t0HCA8S+b9rE/PVF/NFNLX" +
                "WMrfH2r22Sa9ho9lSEw/kwp5xigsHI5lbZHp9zPbPo0qPylfzi" +
                "JF3H3lWfvLOFmbr91j5WpW7WuDtfRUodfpWnHpiO/aDwquoBzf" +
                "nX5ld8STjQk+zIVK2afNNPU0MkZzF4E2ZtpeNniATvr++QxzSG" +
                "s7Lp9PqakxLbdtEUlvv58btWB2N1t9oj46Y2flf5VFfLnw63R5" +
                "167PWiJrRa0I19RcHW5SoxXh+6fPSnpgubku/eXpS0Z/PFLK/N" +
                "pC98lorstYJK+MKYlQZmz7yY++mv1miufuN82NsVfZjaL7zTzf" +
                "er+ZIoX2m249e3urrmf8Urj9e973z3LqaRbMAvU0MkZzF4E2Zt" +
                "2Heo4GD/CRXt9kDuldraIFv8+M7aaNWXLLVuR608+NWqSfmC/4" +
                "stUn6qMzzv88qmL/nnq/Nfb9fqt+y5GeBs4156inkTGaA4Em5B" +
                "KRTdp2/8WMrxqN6KRmgIRHGYXfpxu7ayHjZyTxrTOWDKwbH2rO" +
                "df+RvNofIkbcaTvRPEE9jansBM+BQBNyichm20rW4efkH5oBEh" +
                "5lFH6fbuyuhYyfkcS3zlgyIALbs/aHiC3t+eY89TSmsnmeA4Fm" +
                "dIXlsNEtOaMrNCNtsA7X5xXNAAmPMgrouHHK2F0LGT8jiW+dsW" +
                "Rg3URLakq5rA+iHea7FW1RT2PKtcVzIEKzzXLY6Jae7eGsDabM" +
                "oi054B3+LH2wOnHK2F0LGX+GtO2MJUOm26ZMdRy2HxwZ1+3oNv" +
                "U0prLbPAcCTcgZ6T6EBFySzWFd0AyMs43W9/kEs2bRFjL+DFmw" +
                "M5YMrBu/ZnvW/hAx4tbIqN/nV54d5XNu5bmhH+8zrGg53H5zNN" +
                "+JVllRRj9tJ4uujsLAWrY2cY/CUVUW8L1tnleLRhlivxm/Man7" +
                "o/p/j0tf/fcC3u8V+w6xPqPFcOszzzfW58qP5T4PqeH50ubY+8" +
                "3Nws+XNke533tTj1M947fCrc8vPqr7eV0N9/tswPt9dqT1+cru" +
                "rKcHTdbnsXD17F4cdX0WPbr7LObs7xl6RxuP7fH5wZxs99YdCe" +
                "pposIcP+zWOhfPaST2KTNFPY2M0Ty93pBIMpczbs58Y2i/IZim" +
                "MizjkNZ2XJpVImC2WWxvLBG5buRwb6gabHAOPg/SD8eL6/z1Oc" +
                "Ya33Z9Nv8Otz6r9d1cbC5STyNjNE+uzTrhrGHWWQ4b3ZKTrUgb" +
                "rOlruS45pIQj0FFARyLMrFm0BTJCromdzpgjlhbJTGpKRlkfRC" +
                "tjz9tvxicbE3fE74fbb5o9rv7qN+N//ywl7qL7oz1Vfp9vnm2e" +
                "pZ5Gxmhu9jICTcglIhvhkg2s2f2hGCBxNfJ8urG7FtCEPuXkcm" +
                "sLOx8pByLu97My9vGeLxV41/4r4OdR7b5rqOe9gPW8F+7903es" +
                "/Tb++2dzKdz7Z55va//+bvnPk6PD0cDHirXfjUba57KWT5t4x/" +
                "6cPlXMLs+3+/fJRaI0M2aGehoZS87oQHTAzMRngCRXCSb1bXto" +
                "pTEdIAl0Egwc0tqOS7NKJN2HP9CYltu2yNX2zbqdvqwBcvB5kH" +
                "5YG9fjvn8W+PulU+HeP6v13Zv3YHNjcu74dNN8ELCelfpuHWkd" +
                "oZ5GxmhuZm0E2phpe9ngATppTrPMIa3tuHw+pabGtNy2RSS2b6" +
                "mr43az1SdrSW3v+sr2oaai3wJNwN8383zHH5ayPpdaS9TTyBjN" +
                "j/9iI62l+IKccbPnzMUeVs+zDlA6V89piS2XHvI4tAZksEUkSU" +
                "4+btSC2bVn7Q+cWtv7zSH7BfD4z5P3PKT+nPD88/gdz33xcRHO" +
                "1fO5knM11/NOzv1+sYZ63p3A9Xk33PqMlyavnqsXw9VzEo+Z/8" +
                "rT2i31jD8LV89q//9953qufVnBd8BfS6jL7+X6ru7/tatfn+bP" +
                "gN/na/dd7fPkzr7B/V7C//h23im4rg+Gvd/N/QrWyP1wHHl2Je" +
                "X5P5kBMZ0=");
            
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
            final int compressedBytes = 1279;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71rFFEQvxSpjaCVoGAUBDWFImijl81uZR/Qv8DCSmxsjF" +
                "7UxC0sRKISjAgG1MZGBREUv4g2QpJaRCQk15jOM6DRvX15NzPv" +
                "Y7N7vt25W/PC7ntvZt78Zn43+3W3qVT0VjtTybmF5ypsbWxf0Y" +
                "jAp3ekUrpWfE7516d/go/P4rGz8jm6lDmnk4x8nixhffqMfPrl" +
                "4zOsMV6PFktYnz2M9dnT6Xy2cf7sZeSzl4/PwVPlu18qPifgM7" +
                "xePj7HtzPW54DhWnKnHZ+X/1g1vwuuzwFGPvtLeLz3M/K501Cf" +
                "d7u8Pnda7uGmuvd+ibU+t7izKhOf4YM2+ay6szKytz/attV2ge" +
                "Ti2YweojNSba9BfijaDmvSPY4+9R3r6A9YmPIs9rtVq9rBTPEc" +
                "+U+P92PurDbOnxFTR91ZbfAZnXcfprJ6tMFnyvoccmeVrsH1KD" +
                "A869ZmM302+vVozk2Uo/fXQf5klgepn9+zXY846zN4xFef+WNX" +
                "p3FfnZZ8yrnUU+skiepT1WFp8IWuaWpNq1Tf60VFpaaRfWySJO" +
                "mT4y3g+/mnfPU5tpXv+T23Y26C8Xif4OJzqG+oL6qlZ+4Rmn65" +
                "WvHYBdTnTcb6zBU7eBG8EHvRS5mYgwQssV5so0swll5VBMCIx7" +
                "coKmjoOh0TW6qxUz3NCEluqRljDxgfb5QHPWKcvXb/uSpH3gVd" +
                "e+Weg2vCeb76NOXU7cd7+LzM1/fqAu6rC7VXci4lJmvbersMdC" +
                "Yp4Nv02E61SPJoG9lWmTHseeNRMBKMiL3opUzMdQlYw4yuxxsg" +
                "gE1cny+lD7yanI9GzJjYksqoXl0LkYxtNfsGLqR3PVv6B/zQjL" +
                "067r26rE+vLiXk/FPXzkh1XWOSgc4kBXybHtupFkkebSPbKjOG" +
                "PW8yGo7+GvE+7iOJnDXn0Th8BZLYnsyIpEFlsf+GsFxDkjLAGA" +
                "aN12hpFb20kThNq9GvVIZXtOwbrbUN0ZP8sH64pYe4GwS5QfBa" +
                "PqW1HEf9Iu69xdo1OZcS8qlo71PR9XYZ6DzjO1kSzabHdqpFkk" +
                "fbyLbKjGHPG4+C78F3sRe9lIl5c+w9FnKwkHoswRusBQQsj/Af" +
                "Uw+gkT2OwowpfODY9RUQA0TSXEczFmPwB5Fgv5QriFixXg6WxV" +
                "70sW5ZzkEClqDHErypa7HXNT4/Ug+gkT2Owoypx66vwPFLSROb" +
                "Zow9QAQqMsWDiKl18vfJ8bX4g+O727kopxm+75Nt2OFMJaeW9f" +
                "fi7HwGNxi/n79RKbhl5TPb+58xn5OMfE52Fp8ufj8Kphj5TI3d" +
                "Rb8fvWX8vu4tX336r/M53l20duszXU7tNn/enxd70UvZ2vyNlI" +
                "Al6LEEb0KOvYFXjAseQKNb2DD12PUVYIns36gZYw8QgYpM8XAu" +
                "2Hqd+nznvj65W7qc/uFsmfB+nf8+lYfM79ddGXYQd5vv19lyou" +
                "/XXZpo7/06f9afFXvRS5mYh3NSApagxxK8CTn2Bl5bWREPoNEt" +
                "bJh67PoKsAT78dtqxtgDRKAiUzycC7ZOvr4PlvD/i4vPKefno0" +
                "2O/Bxvb93Vb2mswvlu4tNj5NOrdHp9Zrz/jPgc/MXHpwvsTqvP" +
                "wRVGPldKyOcPRj5/lJDPVUY+V0vI509GPn/my14+/38UbE56Pm" +
                "pquZ6PbNj0+SiO39H/H+Vfn+FnxvvPJ914vHunO/V+3h6Zk/YX" +
                "HB8xsw==");
            
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
            final int compressedBytes = 1758;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWs9rHHUU3+DJFPEH9mA8BFOFphKUSouaxmZmd9WQ6lXw0D" +
                "9BUbz02N02bTNQIZC7EFkCLRK9Cu1BFEFQ9NDSS6BQMORkbsV6" +
                "MDtv33ze+77vzE42M7ub+S7f73fee5/3eT92ZnYmba1mx+Wvah" +
                "mj9Wctxwi/6KHfNv5/1QoZ7W+z7a3fszPrP1pvHT7L6z/162e+" +
                "sdhJzfLZYvrZWi46szLG9Z8L6ueDse3ng9qQR+vLw3OEn9fGdA" +
                "w/s0LOz9tje37ePor9DD8b136mZzbW5+etsT0/b42un+HawOfn" +
                "WlY/B+ct4PxcK7N34VK4RCvtrOvJ6+HS6rbSLFlJ+/fkdeLiCM" +
                "DEx+vMIb3dvHwxgWw/1Dptd31Fret+bvSCM7DV6g+jJNr7zd+U" +
                "0uqvJXyL34/w9z0lduub0iJG8miwfoLDa90aYT+3yuxneCW8Qi" +
                "vtrOvJl4zmipW0f0/ueXIEYKCFl7T47FL2cWiEjMYWUeslPzd6" +
                "wey2Wv1hlER7e7xijw78PWV6ru6M7vy8dnzc34/aB+5OeOfQ19" +
                "Wd4XsOHPFre3TQfmZ7hncPnePd4XsOHPGmPTpwPzM965+M7nov" +
                "N3ZzpjlDa3eHjmVeWQM0jto70sJeVocY9Y9kVDeajOjG9OUpWd" +
                "xobhU2ts5DdsatVkfUGTO6cbZxllbauzaS+JhXINjOmvYOLOCS" +
                "bGDt9fNjzQAL7zILX0wwaxbtIfNnTTe2rlgyIAM3so6HjDW6//" +
                "t7uOlcyZu57hmb6ehwMx9HvghF+Wl9VxokQtgMm7TSzjqS4+OO" +
                "1HRlKfE0cqfn3xFMzUSXcEhvNy/NKjVgdlncaGwRtXZSuDuqBx" +
                "2uwRdBxuF8cdzn/NxwKt3I9T1tpKPDjXwc+SIU5affj7qoIrKM" +
                "mf8p+zc2enqEz/N/l/pstBwu00o760i2GqAhaX85EQEYaOElLT" +
                "67lAkZ7Lo6bXd9kUmw6+dGLzgDW63+oD+64vTzMzpWq9yIJkt+" +
                "u5zbny+3XvXdP6NnLH7lD8NwYn++7mE+4/3395MF5T3dx37ar7" +
                "8xk4J/Td0VLhz0399b7/TvZ3O3ev301WT7ufLDoP3kETyiyWva" +
                "kZX8I8tDSn6u4FG/GJSvzjAtX1/sbP60+K53RpwHNHm11jQpnW" +
                "8QGyPyxQAK+WdHYEs2f1p819uiMq/3Bct49cIRv94X8lzvV5dL" +
                "uX/O52I6Wv2cz9PPWFN4P6Pnq3d+Rs+N8Pws7Pc9euFo/b6X9L" +
                "y0WMHrfXF013vzfAWfP88P5/lTWP5NYr9bvffNcmsK2kGbVtpZ" +
                "R7LVAA1J+8uJCMDEd9OXmEN6u3n5Ykqk1mm764tMrh33c6MXzG" +
                "6r1R/0R1ecdn7WT9T3r+RoSlu7uv6DUT408R52tB8O5pc/dhFZ" +
                "xr9tvf/JX5+qT3miTOXKZSod7ecd1sgfu6gs2xO1Co9gqziUHY" +
                "37jfu00s46kqEBEnapkdP1lawyLhhgsYi0mDZ36yHzZ03wo1ux" +
                "ZGAso3QebhzZx96cbczSSntsm2UZGiBhlxo5XV/JmsRXDLBYRF" +
                "pMm7v1kPmzJvjOrVgyMJZROg83jugno081TtFKe2w7xXLwG2uA" +
                "hF1q5CS9ZANrEl8xwGIRaTFt7tYDSOCpJsutPdx6pB0a0c/EM+" +
                "t5Pvileu9HaTUN4/0ouFfBft4rt5+e3/cXK/37/lRxKDvCM+EZ" +
                "WmlnHclWAzQk7S8nIgADLbykxWeXso9DI2Q0tiCTYMLPjV7E/Z" +
                "zwVas/6I+oej6cp5X22DbPstUADUn7y8m+4CDd4n/MIb1VP+f9" +
                "MSVS67Td9UUmbmyJ1XnbavUH/dEVD/96j06O7nq/8WmZ7I1zjX" +
                "O00s46krvH4TbpgWC71MgJX0SQ+v1vclszwMK7zMIfkzhk7tYD" +
                "OSCTrp+umI7Bh0wkr+4VMnbQC40FWmmPbQuJfJo1QMLOmvYOLO" +
                "CSbGBN4isGWCzCHxPMmkV7AAl89IZbsWRABm5kHU/WItHNveYe" +
                "rbTHfyHcYzl8jzVAwi41cpJesoE1+SukYoDFItJi2tytB5DAU0" +
                "2WW3u49Ug7NOKvqoyebk7T2kye50giGSsjpd034QU2sEILjbbY" +
                "LGRMN0+XBXb4Yu3uwUXtpRmS56WLtlo3E50tR8l8np+r4PP83N" +
                "Cf55M3/eCVCj7PD70m0c83K9jPUmuqP6k/oZV21pEcvc8aIGGX" +
                "GjlJL9nAKuOCARaLSItpc7ceQAK/csytWDIgAzeyjidrkeg+98" +
                "/J8u+f0QdDvn9Olnn/rD+uPzbnbKzrzmiJrCzzyhotaatm814b" +
                "jzmSZnI9eeWPL1ubpURKn+hDN5aP1eaarUli/w/GCuKx");
            
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
            final int compressedBytes = 2653;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWkFsHGcV3hNSFEvkAhIiMlRxpATKoRCUcEDyzHgOKYFLVZ" +
                "AsVYAUIeEDQohLe4Ldukm9J0gVqYhUvsRReiMVNAEZUhVoiipx" +
                "4FA5t8YXy+6llSqhKhXMP2/efN97/z+b9WbtgGc0/z/ve99733" +
                "u/Z3c9s1vcL+73esX9opl7PbXCeXZIEUFxBk58CM7ZkLXXbDYD" +
                "PDGjSxMV8WwjwARfeopz2wjfD/uBYFN2ea48J2OYZRNLbB0VAZ" +
                "vj2KNRMWY1ONqqsaLXTNXJWbya76Jaz8/FlSOD5laWXw1fh0ZA" +
                "K976m3p24ZPegduyz06PFW/lk+WTMoYZmNo6KgI2x7FHo2LMan" +
                "C0VWNFr5mqk7N4Nd9Fr7dyPa4cGTT38Ntxt1bRVtyyz5ZnZQxz" +
                "4zsLW0dFwOY49mhUjEEju2qjrRores1UnZzFq/kuYm1bB11pUb" +
                "dW0Vas7OLd4l1/zQomOEa2UxZzOGdKIVtTj0SlOIhE1hQTCCrw" +
                "uhyTrdkeuvWtymgE2v2vVMfn+3Pw/fLnu3vP6B+rji8n8K9Xx5" +
                "kIPVG9mp56+PfB/hce4P9qGr/wmQ7+8Qj52q7q+Ua9rneLuzLK" +
                "rFi9H672uyszgoPR+gnhQ3DOhqzNX/OwzUCeu+1f3Cl4zbj2OA" +
                "JM4h/2Hct5f5UjfD+cEQhdn8reKDZklLn2bTT7TLVvPL8kOBit" +
                "nxA+BOdsyNqoz9gM5NloK3QKXnP4tK89jkANqCRo247lvFpPiv" +
                "D9cEYgtJ4NuzxZnpQxzM1760nYOioCNsexR6NiDBrD79poq8aK" +
                "XjNVJ2fxar6L8HqPK0cG+jyKurWKtuKWfaI8kX8YxjAHj1rBDu" +
                "crM0AC31oaD0+dPYnpmeQForOgwQsN8SsHdeYfDt6zGEfYyqQO" +
                "wVTFVh72/qrG6iqwFxlsHdqR1inb/FzYYcnBm2JyMENmHhlnGx" +
                "rDRe/1m3KtSldGrQp1eQ8w+Tzy+RlLdcyK3Emqrvzj+WNhzz9W" +
                "q+IdC2eKiC2HsMWqMx5Tv0Qip8YqT+IkL5hch3pVg1UsTzTB0h" +
                "6UKefai9YomPdzhVw/VkLjREOVtG/ucvT95sVPjfOfwmBr/+8a" +
                "89f3NnL4/QnvN++Ud/LtMMpc6TVWsMN59f7ZIswOXIvILFkll8" +
                "X0TPIC0Vn1xc8qylGdwKreP5NVsRp0QgyreH95p3r/bNWExV6r" +
                "gDrAbup8u3w7/3cYZa58jRVsjzA7eCwic91Nk8tieqZ5bRbRES" +
                "8rBr9yVCewPMYRXJn2KLOqeL/OoiYs9loF1AG2nM+fmT+Tvx/G" +
                "MNe+xgp2OH9+CUjgW0vj4anfiZKYnkleIDoLGrzQEL9yUGf+/v" +
                "AZi3GErUzqEExVbOVh769qrK4Ce5HB1qEdtXWenj8tY5ib9+XT" +
                "sHVUBGyOY49GxRg08r6Ntmqs6DVTdXIWr+a7iLVtHfTpFHVrFW" +
                "3F0Erc+18d8Vzg6jjvyjHLIuVv9/4Tq6vSvdUuXi1elVFmxcQG" +
                "Aib8igy24EEuzsYxAXvxTZtBcY2xVbCer5Nrt37LBH/hDd8xZ0" +
                "AFvtt0J6iWa3drvDZi/dfG+iutTRY31SulQ7F8dqz/e56dVHfU" +
                "87rhj/fmed00tkmf1118eU+f110vrssos2JiAwETfkb48LE2pr" +
                "l6TAbG2+vL+b1mXLuviZngD5d8x5wBFfhuuzqx7NS22+fJE3wn" +
                "9gi/59tb7YXHFh5LY4JjZDtlgYO5SyH7SD0SleIgUkfduzrgCq" +
                "wux2Qf+Sq79K3KaKRdgeMLx2WUufYdVxsImPAzwkfYsyXOhqyt" +
                "vskAT8zo0hz+xNceR3D9imRLvmPOgAq8sl0r7sWwDy0cila+xs" +
                "KRXxav2jqmLHDCnF+22dwzicvqkagUB5E66p5m2QqsLsdoT1xt" +
                "Wt+qjEZUu7hd3M5fCaPMlV5jBTucyyjMMMLPCM8SG840G2cN55" +
                "xDzwMuWcKueSQjLI3QHNCzNaGz+tPitmaUSjjKZpBYYbGX/UDa" +
                "v9Errep6sS6jzLX6utrhfPgDwcFQPyN8IFaz+az5azaD4hpj+W" +
                "nNuPY4Akzwg7btmDOgAq9s9VCxY98r7skoc+27pzYQMOFXpLo/" +
                "aj3IxdmQtdU3GeCJGWlNZLZZbATXr8jwp75jzoAKvLLV414Me7" +
                "PYlFHm2repdjivr89NMOFnhA/EajZkbV4fN2wGeIr2WbZX8Jpx" +
                "7XEEmOAHbdsxZ0AFXtnqoWLLLm+Vt2SUub7XuqV2/pxFmG3jMA" +
                "tfIi2mZ2LF0apvFZnrc6Sqsmq+Au0pzo214Arjbr2OY98sb8oo" +
                "c+27qbZHmG3jMGsUDs7R/DV/n4pWNauILFxXd1VWzVcQa/vqhT" +
                "v8mVe2er5iRCbucL+0u3uC3X9/lK8/9PdH6/sfOc42/8H8BzLK" +
                "rJjYMQI2rMGWtXFAARr19bmoOVjd15XSZKbFrN/HohKvzVxbd9" +
                "yt3bE+YGenslMyylzfkZ1SO0bAhjXYsjYOjUWOZj3PaQ5WN3ek" +
                "p9KazLSY9ftYVOK1mWvrjru1O9aHOx71vC5P3DW88M//7+d1+a" +
                "FxntddODfZ87oHPP98+eA9/1z53l4+/3xEv//8zf/67z9fuDHp" +
                "eiY+r1fM6+Oae71cG+tT9Fo3O782Xo7xFKYVZ/FgTaNKXs/iaH" +
                "G0en3MuO9ljo717c3Rbrbkfegq35vwe6UO7f7quMyRT6ufyJ6Q" +
                "UWbFwl7MFXMWCWcBY76PB6uuaU484AQMOTja12WzMuI5lsFq6k" +
                "GvXpu5WAP0kFJgHWXLUewUOzLKXPe70+yz1b5T/z55h5izrb+N" +
                "sYfENtassKBRY7M2g3q0Aq7CZCVkeMXXHkegI80oFdmO5by/yh" +
                "Haqa3D69C1rLm2i20ZZa5922pn7ygCJvyM8CE4Z+v1ln8EnDWK" +
                "7eXz7IkZXZpx7XEEmOBLT3FuG+H7YT8QWs+GXS6WizLKXN+LLq" +
                "qd/csjYMOy8XxobFhP5QCVffm89Xi/1UznsAz4EItKQk+p3FgL" +
                "zR53a3dlMTv5zozffxa9A7dlP5weK/G9yJGFIzLKrJjY2VuKgA" +
                "k/I3wIztnC9QmcNRaOLJ9nT8zo0oxrjyPABF96inPbCN8P+4Hw" +
                "Ompk/P/84Ep7f3Tj4N0fDX/36O6PsmcO3np29TSt9Rz1+5Dhaw" +
                "fv/bPr+pzuVlYbzuWIOXowg5mpKFZgjVFc5EqpxDm0qrguO4b5" +
                "xUtci60e1vAP6NbmLEvbSXSdjnq9P554ev/0JK/35f90vd6XP9" +
                "nn1/vj47zeLz71sK/37CU5dIy9XVZH3S9N5lPGeBpgof7RCuoZ" +
                "nb9L30eP0Lkkh46xt8vqzjeJTxnjaYCF+kcrqGd0/i59Hz1OlQ" +
                "u/is+mt00r56R5sm9OjzVmpb9u5it69oAnZ1uTZJ9Wlbv+fL+z" +
                "3/9RLFyKz6a4npemXeUu1/Od/VzLwbr5fP9i4nPsOyPj3zBW8+" +
                "uJwev6+T74c//T3Z/vgz8lMv61Ov6RqONbnTXcDuMvZhur+ZXB" +
                "4C/1+Hff0+CPiQxvjblab1bH3wZ8xf8XWtycDQ==");
            
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
            final int compressedBytes = 1905;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW11oHFUUXkgegq0oLX3wJy1o2oSIL5VChL7szM6TNKHU/0" +
                "B9ER/yWPSpL3Z3026z+yyIIg0SfGn7III/wT/8i0obEFPiY19U" +
                "WLQPEQRr1L1z5sw55/7MTiczO0mcZebe+51zvvOzd2b2zu5WKu" +
                "Z27mVo/X3+vkplYW8l161+TjFvnafxQzY7l+/6pTyyq3VqHThC" +
                "i5h6+fv9/bVOcw5w0kA5R/gOOGcj1iin/ZKBJNiSF5fP9rd67K" +
                "YFxUCRKN8yY+jXL3ELPR/OSAivI1n6Q7z1h+L5OaSQhb0ol9om" +
                "wiU2jGQ2lPy75FzPH2rcTI5Korae7Mv5CT7S5a3LvA3eehtYT2" +
                "8DEZu2y96NkcyGkn+XnOvpGkmMrp7Lyu7DnbeU1R/t7Q/Ux8zr" +
                "Z+or4sO9/RELfqy3TxnoRE7X4UN95EfteHDWoX9Y16o/dkfxPN" +
                "6/nu0b27eejd+y1bP1epp6hkimerrv76kz+7Wyg7b2tSLZW6/0" +
                "zv7b0VXgNhzrZ3CskIW9KOdaNoRLbBjJbCj5d8m5nndb3o+SGF" +
                "092Zf3I/CRLm+NJ+F8rx63vANnd/b105aTeb43X8t+vntd3nrd" +
                "+P7eRUS8K13jfeqaEhtGMhtK/l1yrqdrJDG6ei4ruw933lJWxv" +
                "29/Ut58/PCgaLnZ3QerMOOR1PqGjnOq/VsMtRI54O0KP5kDyhJ" +
                "5nf5161tWt4mb73N+HzfVEhzDuVS22XvxnD9DryuOJTULud63m" +
                "Z7NTkqidp6sq/djzZ1TnfevBcsBAtwhBYxGKu+dxlw0kA5R/hO" +
                "tuSB4z3/lyUDSbDlUdh9AgeP3bSgGCgSZSczhj7xUSScV9aKIp" +
                "baYXaLvPUW4/m5qBA4sndl0XifFk2JjkmZTZP86x5t/vpHZeN2" +
                "RWkygI90efNe0A7acIQWMRirvrcEOGp4SygnG7mrF1qBNrGG3p" +
                "c4B5dgBDIK0uEIMksWaUEZUa7KTmaMEXMLNeKanJHXh6LlsW9t" +
                "fZRhjXKrvPWR6/6e2/PdpPX7xvZdv59fyvZ56eJDg16/n79S2c" +
                "Vb641UWm9mfD4/VZuCI7SIwbj9ByKkSXKO8B1wzkas3C8xkMTU" +
                "cPk0YzctSJP02/fqGXMGikD3LP3xXIT2Wm0NjtCGsjUcB8cRIU" +
                "2Sc4TvgHM2Yo39CwaSmBoun2bspgVpkj7kZHJLCz0fLieE1TPS" +
                "DsaCMTgG8RUURiFeBZQhY3LEUWTjY45JH9ya28kYdJ+2ODmL7o" +
                "3z8pxMboqUR8izlR5lxOTLfX/3j/hHzO83FdZ/Qy2bNvBu+Xny" +
                "zWx2Lt/m95tZoqydqJ2AI7SIqZc/6o8iQpoKoxfK+A62MAJtYg" +
                "3jHOUcXIIR8Cg4qxknj920IE3Sh5xMbmmBmco4dD+8jtE+U5uB" +
                "I7ShbAZe/oQ/UZsJvy+eIU2F0QtlfAdbGIE2sYb1nOAcXIIR8C" +
                "g4K0faf+mxmxaUETJCRDJj6NcvcQvMVMah+2H1RK7p2jQcoQ1l" +
                "0zj2TiJCmiTnCN8B52zEGq3PTkoGkmDLo7D7NGM3LUiT9CEnk1" +
                "ta6PlwOSGsnrFlYDwnDboKU3v1Gkhx3HoHtQnncmKQfI5vGrvg" +
                "yaWBUq4tMZsW9l49aGetXtN1baz9NzNHtTdfaJ7SNZvPxb5/zP" +
                "/zdHM2J54ns9kVkROLqlfP5jPNk456Xresvf9OxfuUHM+/5Krn" +
                "/IvF1LP5rKOe11OxP11EPbNvwd1J89MtLb6e6XxnjTAYCUbgCC" +
                "1iMK6u6Ahpm4jeJ1vi4H65Jkqo7efTxMw+2VIkKidX9MSNEskl" +
                "XzJe7Cc9X2p9VND3cf+U+Hzp+UE+X2p8LL5/t1VpOnHV8rkYfR" +
                "i178fIJ/V7EqyXLdiXvf07SxxPOFk+g/t7NPogaj8Nj1/rOTUs" +
                "c6bxTco12he9/avGStrnn9VJk6NzV5b5Of+v6/nn/GbG57ZZf8" +
                "8wOejnn/L5vPdT/p8q8uDMyuGyyyvPxPl5aPs+n888Pw+lmZ9b" +
                "/z0Dezb9fex7T6qrSCm/r6s3Mn6e31NwXEnzc2QXzs+RQc9P77" +
                "TZy/HaebpcniJy6lPpYeF/vYCarpfH4bIrIk9bPXfbVq3mp5Vy" +
                "ZRz/YrazZ/fV0/X7+QLP9zNFe+gcKK+eFwbum56Utm4UVM/7is" +
                "8C15uDrmcZv0/2Vsr7vOTyXdx603+X97yrBdzfrxbPQVmks8sr" +
                "z6T52Xlw932eL/r/XKWc78tbnp3Lmc/35TT1bP28k+rZOVje/L" +
                "z4XrHrzVLqebi8eqb7v8zOOt87k//PegZzedWzc3S73I9cOe3o" +
                "8/3Ydp6fzSvZ56c/23sNh8ew7SE4UuMYAc3wSHKGiDa0VT1ki9" +
                "HZCBvWGCI8YhmOerO6LcWJGuRPMlJmUZakPyytmP/YNop7WHg3" +
                "MvHj50aoifNTbnn+/92cn7k9tXmrj/xtx1r3/lSfl36/0/nZ//" +
                "O8P+6PW9Yd42nOd6WlzneL9oSdd1Dnu8u3eT/KFmUZ10//VIn1" +
                "PJVqfv5ZxP2ourrVelrQsr8/Wk07PzNt/wHqjWSg");
            
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
            final int compressedBytes = 1897;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW0uIHGUQHshJSdDDesphlmiS3REJKkK8xEx3j6+YHEUh4C" +
                "UKEh8gkpt59OS1yZw8CF4kKAg56kHw4NXnQT3HSHLbS4LXSHbW" +
                "/ae6ur76X+md7Z5OBrv5X1VffVX1px//37PpdPKnOp18Z/5Epz" +
                "zOfMy90f5OhSN/fKM86ZE/50UvdWo58u499M/45Ze/CeB31xRX" +
                "bD6f3+p85vsf3PnMn91UPOVcJXexTe7yfCZ3WYJ2euTah2Wi80" +
                "nFf0iPOBsRYwz1QlZ+H+G8tS52ffaPzt/1GcpJX59nP5v2+ozO" +
                "5/Iczudys/d79Pl5YP7m8+LnzV6f7iHzWe0YrnYeoGO0s1n+2P" +
                "VZkcFzffaPx67P86+3eL8fb/L6TPM0p5paltFYJIIUPUuGq6IR" +
                "LmSzWUeLmoHlbKPxPp++2F0LjJ8lo66dMTJIBLZn7U8ittCr6e" +
                "Ru5Zb7IueaJNzD0RD6UpBNWAvZd8KjNdrO9WnHabOAj1WJBHO0" +
                "fWsG9I9Fz4MbcYm8kd6YjIuW+yJPrpqaEWYsel8Rq8nK7Cp6oF" +
                "5yVTiYWby7UQgGkcysWUQvtlKb1tihFfclUmZnJM6S64e9Ucn2" +
                "ZfuoptboaMT95DrJBcF6lGARW2YT1iLa65pBNNxiFH6fxIGxux" +
                "YSg0Ri7HTG1Bc+iQR59VxJxBo9eT8/Yj1ZP+LeykOduTuGD8/a" +
                "45nv1a7qr/o91ME5LUfIrq48m1kvxdfzoxfbWy9deKy9/VFj83" +
                "no//nc3Hwmx+7X/aaJrNnvdc7zc9z0fnM0aHG/mVVBrTxd4/uo" +
                "uD7THemOTufS9vpzMrxtHSHf+ZXGdvQfNX59vtzi9fnSrD3KfC" +
                "YXp53PuOXo1Rbn85WmPSS3sE1urbzAYyO5tJ31Gh2yD8tE55OK" +
                "/5Aeccmt4c14VFrq6+m+vt/JR7W8LZ7Y9+Q35+978uVHZ/1+Hy" +
                "43fs8dbe9+D60/6z76f1Ph2tWGRmG+aXSMqOZDUBJ/3ANr4vwh" +
                "/7a1i4r+HveHy3j+92nu93PvhO73c8dme7/7cnLv9wuvNfL75l" +
                "Jt87kenM+1Gc/nUpX5PP9tfc/P/N9i5dtNu+56Pu1WWjV3w2ji" +
                "3eqh3++bWM93q67n64hystd6g3vZj525O2af00qxSkoX0oWG9p" +
                "sLLe43F2az3+x/SoVrVxsahfmm0TGimg9BSfxxD6yJ84f829Yu" +
                "qr7vdf1Ts1vP909N/T46VeV9NO3v7ytvbf17SBnpJ7O7a6f31X" +
                "yUyRjbZFz+veLYSDb272MfOmQflokuGYfjMNpkfK94k7G1fx/H" +
                "MvP3dN/av49tznDeDvI2tsntcj5vs8SHDtmHZaLzScV/SI84Gx" +
                "FjDPVCVn4f4by1zjw/9VHn34Pl3r8YH72/9bvq7Bf30H8V+Hbw" +
                "XqXn55ebfX5G1kvv1jefgZw+bG+9NPpg1h77P9wfHG1mN2382V" +
                "K2RDW1LDNnupwuZ0tmPc8YUxuZnKzDQrY0IrSwTlbUy8iBGo4A" +
                "o0BWlAxv2rG7FpKR5Gp864ypn19BC85Ux2H7wXnE2K0nyT/FTm" +
                "IxXfTsLxYr7UIWw2g/78z2R4v1I+MHfF/6c/72703nFP1et3cz" +
                "+6PY97q690db+F63t8r7fSJ5YP7eps35XDnR7HxurEbXsU3Wy/" +
                "X8OkvUKnbdWdeuuxqfTHQ+qfgP6RFnI2KMoV7Iyu8jnDf2+if6" +
                "J6imlmU0zn62JYKWkbbHIh4EI1KxQo1Pj2Mfh0agN9ZIJCYnH7" +
                "fMBbO72eqTUYiO3+/Zr/N3v4dy0vf7ua/r+70jHbu9TX9jeft+" +
                "fb83HVkb76NsW4vX57Ym30fpnfSOc31OZKYMDpCWx1yzRI+0Vr" +
                "N574M77Ekz2ZZc8+mL1o0SkWjDOWG04Rh9DD5JOQNr6RrV1E50" +
                "a8XZ2zgLCSB7pb600YVsi1GPUOJjIutpBtZwBBiFYnXixNhdC0" +
                "ECvmdnjAwSAWWq47D9wHwWloPDg8NUm7b4FzzM4+wnkopE0GiH" +
                "GsL7ZNoHWqOdjsH26YsTWWxvyIs5udwSKUaI2WqPOuISfWRwhG" +
                "rTFrojMuaaJYJGOyr902iFaOEQH2itvaFH2ycj+6dtmdajLWZh" +
                "LN3IhQHm08lWe9QRl+hDg0NUD8r/I0AjGnPNEkGjHZX+SbRCtH" +
                "CID7TW3tCj7ZOR/ZO2TOvRFrMwlm7kwgDz6WSrPeqIGZ1eS69R" +
                "Te3kWXCNx9kvLBGk6FGCheTIJqzl80YxiMZFhHy6sbsWghQ85e" +
                "Ryaws7H9SLBJ6fBfrgroO7qDYtHTQy4+w3kjJGejjShfHIJqwi" +
                "FYnWuFGgTztOm0X0Yiu15ORyaws7Lz0PbsSMHPQGPapNW1y7PR" +
                "4PEpKCpKdHKGU2HKNM+0BrtNMx2D59cSKL7Q15MSeXWyLFCDFb" +
                "7VFHLL4ms7vbnDKiouZ/NxZEUIs1ynFs+4gdjNVeQowclcRla1" +
                "Dm47f1dsboETPx53FwjzllREUjsCCCWqxRHvMRnc89mtGHF0aO" +
                "SuKyNSjz8dt6O2P0iJk4cf0HDq4osg==");
            
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
            final int compressedBytes = 1624;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj1vHFUU3R+QwuFDIFFQYCQkRGOERLp4d6bjH8S15R8QKb" +
                "XXxmjBsmiAAqSEArmIaGiQXKSI0mBZQqIBalxG4h8g7Lm+c865" +
                "943JrjawwJvVe2/vveeec96LxzvZpD1tT0ej9rS9Wkcjj/w9Z1" +
                "pCax9W78JgjtHVVep2NVVkbOQouVK16CBrR/fOH5VVLzp29O1b" +
                "t2/ZfLnaZZHFPnsGaO7jinflnGpwt6qxYtQs+WSWqBZ3kbXVx2" +
                "ikDuNpRB/eAa18TX/zd7O9Ub3muiYPJw9tttVzFiMDJOqc4RF7" +
                "tQe6YOA8O4u92Sd7j54YCby6jAxwEHc7tBNFF8/4+JrzP36mP6" +
                "XjxfqW+pNyvCo/s7NZ/+7jegcv4Tw/6d8d1tOY8654NHlks62e" +
                "sxgZIFHnDI/Ym1mZgzu8R/Flzew9d7B/dqI7ZgY4iMqqB8cBfT" +
                "45t9nWrnbuMTJAos4ZHrGXWXt9YUAlI4Y0s/fcwf7Zie6YGeAg" +
                "Kqse74XR7Ul7YrOt3bPpiccxw2jtw+pdGMzRP4sXul1NFRkbOU" +
                "quVC06yNrRvfNHZdWLjh09faXw/PniIr85pq/27174O39jTV9a" +
                "+c+jT+vnyjKvD3+vZ7DUn8/P6hks9Ty/qGew6LW5sblhs62esz" +
                "hngEak/TygAAyy6OJKqc5xiUMRrOaV7CRy4yycPe9WXzgf3vH0" +
                "nYvx2nQde9m9e92fwMGP6RP2jYvxduGT972L8X7KvrWkz/XX/6" +
                "K+MSffmxx99MFF5t25+q++rZv3PAtM/8HzPPhusfOcPJ08tdlW" +
                "z1mMDJCoc4ZH7GXW/u8TwoBKRgxpZu+5g/2zE90xM8BBVFY93g" +
                "uji3f0y/VzZdGrvdPesdlWz1mcM0Aj0n4eUAAGWXRxpVTnuMSh" +
                "CFbzSnYSuXEWzp53qy+cD6G32i2bbe1qWx7nDNCItJ+H94KjP4" +
                "stZuFKqc5xiUMRrOaV7CRy4yycPe9WXzgfoJubzU2bbb2sWeTv" +
                "fQbC65zhEXuZ1c9CGVDJiCHN7D13sH92ojtmBjiIyqrHexH0WX" +
                "Nms61d7cxjZIBEnTM8Yi+z9vrCgEpGDGlm77mD/bMT3TEzwEFU" +
                "Vj3ei6B3mh2bbe1qOx4jAyTqnOERe5m11xcGVDJiSDN7zx3sn5" +
                "3ojpkBDqKy6vFeBL3X7Nlsa1fb8xgZIFHnDI/Yy6y9vjCgkhFD" +
                "mtl77mD/7ER3zAxwEJVVj/fC6NK1+0d97ln4eel+e9/myxU5jz" +
                "E7kuulgS5WyBrIaCW7YM3oM7Kgjl7MuhvlRp0d6t5LOooe3xjf" +
                "sNnWy5pF/l4zQCPSfh7eCw7Xdw7u5kvrHJc4FMFqXslOIjfOwt" +
                "nzbvWF8wG6OWqObLa1+11w5DEyQKLOGR6xl1n73zfCgEpGDGlm" +
                "77mD/bMT3TEzwEFUVj3eC6OL339+/1z/vWd3OTx7P63mb9D6/V" +
                "Lp+6X9z+v3datxnoXnpbujetX7fak/n12m3u8reb/PntS7dpn3" +
                "+7zPS/V+/6fv99kP9fN91Z7nV/MaPx4/ttlWz1mMDJCoc4ZH7G" +
                "VW1gUDKhkxpJm95w72z050x8wAB1FZ9XgvjK7Pn8/r2vzZhs+5" +
                "OhQN8y1Sc8SzaQAF/9creOV6/iH92B1R7WF7aLOtnrMYGSBR5w" +
                "yP2MusrAsGVDJiSDN7zx3sn53ojpkBDqKy6vFeGF3v9+VeB1/N" +
                "/f/BvqzPn8PPS7Nf5z3P2S/1POd8/rz23+MOvq539fDVPGge2G" +
                "yr5yxGBkjUOcMj9jIr64IBlYwY0szecwf7Zye6Y2aAg6iserwX" +
                "Rv97v186+Kb+/f3/cJ4Fp9/W34NLfYaq57nw1d5r79lsq+cszh" +
                "mgEWk/DygAgyy6uFKqc1ziUASreSU7idw4C2fPu9UXzgfo8fp4" +
                "3WZbu+9K1j3OGaARaT8P7wVH/33MOrNwpVTnuMShCFbzSnYSuX" +
                "EWzp53qy+cD9DNdrNts63dZ/+2x8gAiTpneMReZu2fL4QBlYwY" +
                "0szecwf7Zye6Y2aAg6iserwXRrf77b7NtnY/u/se5wzQiLSfh/" +
                "eCo79X95mFK6U6xyUORbCaV7KTyI2zcPa8W33hfIAer43XbLa1" +
                "+9ld8zhngEak/Ty8Fxz9vbrGLFwp1TkucSiC1bySnURunIWz59" +
                "3qC+dDO/4Tr0sbpw==");
            
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
            final int compressedBytes = 1434;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE2LHFUULUWECGLAr4QsZhEDMjod0AiKQrSqOyoBNYyfRB" +
                "lQIbPQjeQPVNNU0k3WblzML3Chv2d2oiIESaLiQhB7+vbtc+67" +
                "r6a7a6pngvNe8d6re++5535MVXVXQ1LsFrtZVuwW0z3LVNJzXY" +
                "FQO2t4hr7Mmk2HZYDFI+piIiPerQfnz5nYipkBGYSRbTyuhdFZ" +
                "1rudBaN3e08neqwsxyTmsZw+AuJYnlgeFm11MdS8uN1eaI+xzh" +
                "++RtGUD4XI8nr52Hh9fHL+5GR9ajzP7J0N700x5ybSnam0Pp7P" +
                "g2F41/C9HM+ovFS+Vb45ObsctX9afkbS5+W1crt8oHwwQD1ani" +
                "TpFJ2v7d+R4R9T3Gvl62W+bD/Lq0b6Yjy/zLJ8LV+TVfY9m0h6" +
                "bjVAQ7L+PNUXHBpfOdibh7WzHOOwCI6mFp9JyI1eKLuv1h7oD1" +
                "fc3xjPM/1n+Prc96/6d6jpnx3P5zyy/9J4umuz/2yWvfFnduDR" +
                "n3P19V+I6+ti9885zYtL5fPKdF+ynxGmpfvZxmjaz1q86efwn8" +
                "Pq5/Df49DPdH2228/qx6b99CM/68+WHc09Vz1Wm9lga7A5+Ghw" +
                "xeg+OThv72RtxKv7WZfI/P059o+XzWx5VMTvRO+ErLKrTmSvAd" +
                "prwnP4goPjMlIt2OfF9Dp/Dl+fSV3GnEnIZQ+br8y8k3dklX1y" +
                "R3RU9hqgIVl/nuoLjtld12EWtsTsLMc4LIKjqcVnEnKjF8ruq7" +
                "UH+gN0d6e7I6vskzeyHZWhARJ21vAMfZl19tZnGGDxiLqYPnfv" +
                "wflzJrZiZkAGYWQbj2th9PKf7zc/OA6f7zc3m32+V39VP1c/7f" +
                "Wzujf7rvBrg+pq+lm5t5Hqt3b6Wf0yx36nMfPv0/1uS98otvxZ" +
                "i99Xto6WZxU10d/if3t9jh4+iusz9XP193v19SrviNEjh/MeVH" +
                "11v7yR9b/J0mg4ilvFLVllV53I0AAJO2t4hr7WB3HBwHrOLPT1" +
                "eXLuYU6MBN5mGTIgg7DaukosOvpL/vV0nbV6zX7XzLYfajG/Rf" +
                "kWYTtYxMbf4tbzdVllV53IXgM0JOvPExGAgRZebInZWY5xWARH" +
                "U4vPJORGL5TdV2sP9IcrTr8nx943J5oG75v5Zr4pq+yqE9lrgI" +
                "Zk/XkiAjDQwostMTvLMQ6L4Ghq8ZmE3OiFsvtq7YH+AF1sF9uy" +
                "yj558myrDA2QsLOGZ+hrfaZPN8PA+tnzL7CHMX3uYU6MBN5mGT" +
                "Igg7DaukosOt3v7d7vqZ/xfg6+Tf1sq5+D75tfn0VVVLLKrjqR" +
                "oQESdtbwDH09K3Owh/pYfDymz917cP6cia2YGZBBGNnGQ8YWna" +
                "7P9PxM/Qz7WXx4dP2si91WP9PvIS3/2pnu9/T8TP08Nv1Mz8/0" +
                "/EzX53EeqZ/NR3ejuyGr7KoTGRogYWcNz9CXWTkuGGDxiLqYPn" +
                "fvwflzJrZiZkAGYWQbj2sx6E63I6vsE1tHZWiAhJ01PENfZp3F" +
                "NwyweERdTJ+79+D8ORNbMTMggzCyjce1MDp9Hq3+++fo1dTPNj" +
                "/fix/qvfazLYJazL+NMT/SKnLJT+enZZVddSJ7DdCQrD9PRAAG" +
                "WnixJWZnOcZhERxNLT6TkBu9UHZfrT3QH1tx+r7U4vV5Mb8oq+" +
                "yqE9lrgIZk/XkiAjDQwostMTvLMQ6L4Ghq8ZmE3OiFsvtq7YH+" +
                "cMVH8e+L0/tmGouN0SWvu3HlYJw33ju+/eyd752XVXbView1QE" +
                "Oy/jwRARho4cWWmJ3lGIdFcDS1+ExCbvRC2X219kB/bMXumn07" +
                "3beNn+pPR/r5zuzs3SWYZv8TyujyoVbwxP3e4/T8PMDz80Lvgq" +
                "yyq05krwEakvXniQjAQAsvtsTsLMc4LIKjqcVnEnKjF8ruq7UH" +
                "+kMV/wd1eAHC");
            
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
            final int compressedBytes = 1736;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWT1vXFUQXX4HVigAgYRoIEjQ4fV7kuUmBbJdJQUuXVhC/A" +
                "DHAWPcIvHRYSF6RJ3fEKTYRQqiRMofcJMe75vMO+fM3Lve3SxW" +
                "4rz7dO/1zJw5Z+bu7vPzut1r90ajdq99sY9GbvnP6gEalubz9F" +
                "xwjEbwIosjpTjbJQ5FsJpHciWR2+OoO3erF86HOz74+GK+ffAe" +
                "ern77WiucfDuxfyo4P/sYn6evB+OljIO/7ykqk/m7OL95Pl0rv" +
                "wvJmvzpHliq+3uMxseIBFnD8+Yy6yurgyIZERNM9eeM7h+rkQ7" +
                "ZgZUEJVVj3thdHu/vW+r7d17977b8ADJ8dJElv/MrPCyKkc4L2" +
                "sqR2ZBXDvSSrRjZmB9njEzVtwjz9tzW23vYuduwwMk4uzhGXOZ" +
                "tdcXBkQyoqaZa88ZXD9Xoh0zAyqIyqrHvQh6v9231fYutu929g" +
                "ANS/N5ei44ev19ZuFIKc52iUMRrOaRXEnkxlk4e+5WL5wP0OPN" +
                "8aattk9iZvnP6gEalubz9Fxw+Fk4B2fz0DjbJQ5FsJpHciWRG2" +
                "fh7LlbvXA+QDdnzZmttnf31jO34QEScfbwjLnM2t+/hQGRjKhp" +
                "5tpzBtfPlWjHzIAKorLqcS+Mfn2flw7e+T+fl777ecHnpYfNQ1" +
                "ttd5/Z8ACJOHt4xlxm7V9PYUAkI2qaufacwfVzJdoxM6CCqKx6" +
                "3Auj156vPY8nbT7zY2XbPWppVNlKYxJxnmmZvvpVqjZXyUjNiV" +
                "ol1lzrdI9rN7vNrq22d2e96zY8QCLOHp4xl1n711MYEMmImmau" +
                "PWdw/VyJdswMqCAqqx73wujx+njdVtu731XrbmcP0LA0n6fngq" +
                "P/3bzOLBwpxdkucSiC1TySK4ncOAtnz93qhfPRjtOd9Zu+4h8X" +
                "/W2xeObrPcbNuLHVdveZnT1Aw9J8nlAABl5kcaQUZ7vEoQhW80" +
                "iuJHLjLJw9d6sXzofQ2+NtW23vYttuZw/QsDSfp+eCoz+LbWbh" +
                "SCnOdolDEazmkVxJ5MZZOHvuVi+cD6E3xhu22t7FNtzOHqBhaT" +
                "5PzwVHfxYbzMKRUpztEociWM0juZLIjbNw9tytXjgf7djG6jOb" +
                "vtZ+ylZ5TMtgq8y1+uwyDatXK6zVW9Kezl/Tj9lTdP616WuO1q" +
                "w63yIxR8ymARTqn67gken8Nf2YHVHHXw9/by7z+3k6+cc2fc3R" +
                "mlV5hR8vFnPEbBpAof7pCh6Zzl/Tj9kZNe/78/CfN+H9efj3Yu" +
                "/P1YPVA1ttd5/Z2QM0LM3nCQVg4EUWR0pxtkscimA1j+RKIjfO" +
                "wtlzt3rhfLTjOO69NRrGEscPD4YzGM7z1R3zPi/Ned+/e62/D3" +
                "k6fmqr7e4zGx4gEWcPz5jLrKwLBkQyoqaZa88ZXD9Xoh0zAyqI" +
                "yqrHvQj6dHxqq+1d7NRteIBEnD08Yy6z9vrCgEhG1DRz7TmD6+" +
                "dKtGNmQAVRWfW4F0Zf/ef9eo/mUfPIVtvdZzY8QCLOHp4xl1lZ" +
                "FwyIZERNM9eeM7h+rkQ7ZgZUEJVVj3th9PD/4uX+/T6+Ob5pq+" +
                "3uMzt7gIal+TyhAAy8yOJIKc52iUMRrOaRXEnkxlk4e+5WL5wP" +
                "0M1Ws2Wr7d17d8tteIBEnD08Yy6z9p8PYUAkI2qaufacwfVzJd" +
                "oxM6CCqKx63Aujh8/7cj/vzZ3mjq22u89seIBEnD08Yy6z9q+n" +
                "MCCSETXNXHvO4Pq5Eu2YGVBBVFY97kXQO82OrbZ3sR234QEScf" +
                "bwjLnM2usLAyIZUdPMtecMrp8r0Y6ZARVEZdXjXhjd3mpv2TrZ" +
                "bZhltq/uAZrzOOJZ2acanK1qrBg1S3UyS1SLXWRtrQMnlLtVRa" +
                "0YWoU7Qf8/puPvhyf0RcaXH0wuWDYVwZMRtvPK/mkal1XEuBIe" +
                "jF4V6ooR9pX4Yzx2zIrcyWV9TMba74vFpqFmy5uVbxa2l1Nc6j" +
                "dq+LwfDZ/dV/37z+E8hzHH90s3mhtln/mxsl2ygMFeU0DEssoY" +
                "9/rqV60DrkB1NSdWWdMv5dQ8/QmsNCsJ2fnMj5XtkgUMdrAVa1" +
                "xxpTLGvb76VUZpBaqrObHKmn4pp+ZRbR3HPw2f22WO41+vdXe/" +
                "zIT6beGno+H7pSV+vzSc57LPM497x8M9b6l3mD+GM1h0tLfb27" +
                "ba7j6zswdoWJrPEwrAwIssjpTibJc4FMFqHsmVRG6chbPnbvXC" +
                "+RD6pD2xdbK/iJ3AxupIjpcmssAGVnjh0UiugjVjnZEFceRi1W" +
                "6UG3GuUHsv6Sh6+HvzCu6ffw1n8BLPHXM+Lx1tvgnPS0dfXdXz" +
                "53CeU8/zP2x5lqM=");
            
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
            final int compressedBytes = 1418;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vXEUU3Sq/gI6CAiIhERqjlaDb9XuvIqUbJEuR/wLyH/" +
                "A6CWB6SneUVBQUkStEF4kkRf4AEj/AKdJFOHu575xz78wmu/FH" +
                "HM9bzczer3POnbxZ7z4pk8niy8lk8fHis8l4HexP1roWn56NLw" +
                "r+6dn4Onk/n5zLtfjkDfGtNfFus3X/lzPPV2vVf1OLrLuf7ZJ9" +
                "bffnud6fV7GfRyftvLfrYs770Z/vw3nffrTp/bn96G3uzx//va" +
                "zzfvRX+/x8n8774uBDPu3D4+Gxzba6z+zsQXb2xPeoBQbzcqZH" +
                "sL6JM/vye9RmJTXFrCRi6Uv1+vt83n/64zp8X3r460We96XnnD" +
                "4/236+y34WlP7WvvVs/Pm5P+zbbKv7zM4eZMPSeh5gQA68qOJI" +
                "Kc52CUMzmM0jWUnExl44eu5WX9gfZPfP++c22/o6Zpa/9xkZHm" +
                "cPj1jLqL4XioBIzqhxZu25gvWzEu2YEaAgMisf98LZ3ZPuSbxn" +
                "zWd+zGyXLM7R2swAHqsq52QcVOQsVhB5FV17qPOXamoe5+6P+2" +
                "ObbV3u9bHb8CATcfbwiLWMOv57CgIiOaPGmbXnCtbPSrRjRoCC" +
                "yKx83Atnt9/v7XndZfzefLfvS7Ot1y9YNvhynw3O4Mza+zLHqs" +
                "tzSywZ0VVBV4ywr4Qf47FjZuROZm/xr7b4vp3aTa/uZfey7DM/" +
                "Zrbdo5ZGVzEg4jirKn32V60DVcmZWhO5SqhZ62oPd58yf8/vru" +
                "GdckXa29+ji34e0vZz8/3s7nX3bLbVfWbDg0zE2cMj1mrN/6dR" +
                "ENg/ntcQj5xZe9TEmchXlREBCmK3tU5C9mF3aLOty9ih2/AgE3" +
                "H28Ii1GZUxuMJrNL/MmbXnCtbPSrRjRoCCyKx8UKzZV3Peu++u" +
                "7rzXuC/u+Wf7vdn+vre/7x/mfg5Ph6c22+o+s+FBJuLs4RFrGX" +
                "V8ti4IiOSMGmfWnitYPyvRjhkBCiKz8nEvnN0+P9vnZ/v8vDn7" +
                "2d/p79hsq/vMhgeZiLOHR6xlVGdXBERyRo0za88VrJ+VaMeMAA" +
                "WRWfm4F8ne6XdstnUZ23EbHmQizh4esZZRR35BQCRn1Diz9lzB" +
                "+lmJdswIUBCZlY97kezdftdmW5exXbfhQSbi7OERaxl15BcERH" +
                "JGjTNrzxWsn5Vox4wABZFZ+bgXyd7r92y2dRnbcxseZCLOHh6x" +
                "llFHfkFAJGfUOLP2XMH6WYl2zAhQEJmVj3vh7GE6TG22dfldau" +
                "p29iAbltbz8FpgjN/XpozCkVKc7RKGZjCbR7KSiI29cPTcrb6w" +
                "P9pxvH7+qH2L3PQaToYTm211n9nwIJPjpYEqZgAHMoDCEa7LnI" +
                "qRURDXjlSJdswIzM8jVkbF3H36ffSq3WebXtu3tm/ZbKv7zM4e" +
                "ZMPSeh5gQA68qOJIKc52CUMzmM0jWUnExl44eu5WX9gf7bjdn+" +
                "33e/v/He3+LF8P/74J9+cPdy/r+dKDuzdhPx98257XXc/nn+28" +
                "r9rP+cH8wGZb3Wd29iAbltbzAANy4EUVR0pxtksYmsFsHslKIj" +
                "b2wtFzt/rC/nDH7byf53kfXgwvbLbVfWbDg0zE2cMj1jLq+HtX" +
                "EBDJGTXOrD1XsH5Woh0zAhREZuXjXji7n/Uzm21dPsubuQ0PMh" +
                "FnD49Yy6jj80JBQCRn1Diz9lzB+lmJdswIUBCZlY97kex5P7fZ" +
                "1mVs7jY8yEScPTxiLaOO/IKASM6ocWbtuYL1sxLtmBGgIDIrH/" +
                "fC2cPpcGqzrct799RteJCJOHt4xFpGHc+HICCSM2qcWXuuYP2s" +
                "RDtmBCiIzMrHvXB28ZP1n/HJ8takXWtd3bPuWdlnfsxslyzO0d" +
                "rMAB6rKudkHFSUOtAIYyq69lDnL9XUPCP3f/3mU/g=");
            
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
            final int compressedBytes = 1118;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW01rFEEQzTFnFRQk5qCCEARRFBW8ODMHP/DjnmvIRW+Six" +
                "ddQlayB8GDSG56z28R8gty9+oPMKasfa+6usfJ7Lpukuqhu1Ov" +
                "Xn10pWenZ2EXFnyrvi4UW5uujdXNrqu/Lt4mizjNVn3rp2tjdb" +
                "Pr6q+Lt8kiTrO9f7MQLeo5F61erpfzmOAYWc5J4GAuRYBGrPIc" +
                "RXXUq7QCzsDGtTZplqX4OZsSMq7AUr3kmIeY4BhZzkngYIa3bI" +
                "5LGinPUVRHvfIsm4GNa23SLEvxczYlRGMPznvbwdk+O31wYfzX" +
                "mVneYYNz8/4ZMLofn4NRzzmu54OowVTr+TBqMMHn+fWDfnFwpe" +
                "v5c+u783D5oK9kPN8+6Hcdem1Kef/lhDO4eUR/V1kaPj1Abh3J" +
                "/l5xfz6OXdb7PL9er8sos2IiAwETeka4p7bslePCAzSeUYrpc/" +
                "cWnD9nYlfMHpBBGtnG47UwO943Z/A8eh41mN3zKOPhBD6PDpEp" +
                "PY/ifo/9OT/7M+oZ9/tJut8/vD0N+3Pzc+zPeW1bu1GDvq3ZaD" +
                "ZklFkxkT0CNiRrzx0RwAEKK9bk9CznfFgGR1ONzyT1jVqod79a" +
                "e6E+YNdr9ZqMMh++i66pDARM6Bnhntqy1/H7rvEAjWeUYvrcvQ" +
                "Xnz5nYFbMHZJBGtvF4LcyO81KcP4/b+bP5cRKeDO8uHY/z5+h1" +
                "7M84f86qVa+qVzLKrJjIQMCEnhHuqa21QVx4YJwzS219npx7mh" +
                "MzwbdZph6QQbra0koS9rAayijzoW6oMhAwoWeEe2rrvbIPtlAb" +
                "y8/H9Ll7C86fM7ErZg/III1s4yFjy47ne5yXop5Rz9Nez/7f10" +
                "U903pu7vbfn81esyejzIqJDARM6Bnhntqy1/E7mPEAjWeUYvrc" +
                "vQXnz5nYFbMHZJBGtvF4LcyO8/z/ft+M51Hb/V6tVqsyyqyYyE" +
                "DAhJ4R7qmttflz/jUeGB+fkBN9GtPnnubETPBtlqkHZJCutrQS" +
                "y479GeelOC+dnvNSvVgvphrBBMfIck4CB3MpAjRilecoqqNeeZ" +
                "bNwMa1NmmWpfg5mxKC2LE///X3ydVOy/elO52+Vd3pZ9fVXxdv" +
                "k0XsnenP6mceExwjy4pYyWrbIkCjftosddSrtAKbJTOtTRor59" +
                "Xn2o5o7NFHbzt8Odn/aPji9L4dNSvNiowyKyayR8CGZO25IwI4" +
                "QGHFmpye5ZwPy+BoqvGZpL5RC/XuV2sv1Afs3O9ht5/pX6NPR/" +
                "g8Hv8edvvJTN+X5+r3sM2N5oaMMismskfAhmTtuSMCOEBhxZqc" +
                "nuWcD8vgaKrxmaS+UQv17ldrL9QH7Pb9eaR9Evvzd13vNHdklF" +
                "kxkT0CNiRrzx0RwAEKK9bk9CznfFgGR1ONzyT1jVqod79ae6E+" +
                "dsVufz6KbzH7tnq/3pdRZsVEBgIm9IxwT23ZK8eFB2g8oxTT5+" +
                "4tOH/OxK6YPSCDNLKNx2thdq6NvsQ+691+AVSdX10=");
            
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
            final int compressedBytes = 1157;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrFqVFEQDSjkIywsVBDEIoqgdtmX/QKxCps+tf6AIWgw1t" +
                "aB1KkVAkJ+QAhJGi3S+QmpzWYye87M3Pvy8vKCC85d7n3OzJlz" +
                "Zoa3+5aNCwtxfXi/kOvGa/m3bD1jtGbV+frEFNFNAyjU366gkX" +
                "b+mr7P7lLlp595dw258v2e85yftfH0fN/beNh/nhsPzveTgv/F" +
                "+X4ZvI8Hqvv+FfFn1+R7FDzPr5X/anquTFYmcspVfWLDAyTi7O" +
                "Htc5lV1S0DIhFR04y1xwyunyuxHTMDKvDKVo97YXRz3Bz7SYtP" +
                "/DjZLlmMsblRATqSVcZEHmREFFfgdS277aGuX8qpeWYTOGqOAv" +
                "LCJ36cbJcsxtjcqAAdySpjIg8yIoor8LqW3fZQ1y/l1Dy+KhPb" +
                "beHd7fJZElHd8rrydWG7meKQa/tbPqUHnef3nEF+X5qP70ul9X" +
                "k377Ih18e9nEG+34d9v29+7ft+z3kOOc/RwehATrmqT2x4gESc" +
                "Pbx9LrOqumVAJCJqmrH2mMH1cyW2Y2ZABV7Z6nEvjL7+/bl9OA" +
                "/352i/7/052u9yf279Ge75nr/X5fNofr5/5jxv+/t8fv/sv8bv" +
                "xu/klKv6xI4eoGHZfN5QAAZeZHGkFGe7xGERrKaRWInnxiyUPX" +
                "ZrX5gPd3zd9/vW2//h/b71Jj8/83mU88x55jxznjnP/H0p51mb" +
                "5+Ze//uzWWvW5JSr+sSGB0jE2cPb59ocUbcM7Nf6fNxrxtp9TY" +
                "wE3lbpGVCB77bWiUXn70v5+1I+j/J5lM+jns+j9WZdTrmqT2x4" +
                "gEScPbx9rs25/Pw2DOyffcK7uNeMtfuaGAm8rdIzoALfba0Th5" +
                "40EznlehGbqA0PkIizh7fPtTmX+oaB/bMKXdxrxtp9TYwE3lbp" +
                "GVCB77bWiUOfNWf+zhWf+HGyrR5r2ahlK61pRHnaMvXUV6naWC" +
                "UjbY7XKrHGWts9swkcNuHvleITP062SxZjbG5UgI5klTGRBxkR" +
                "xRV4Xctue6jrl3JqHtVeWV1ZlVOu05hY+m89gdA4e3j7XGZVfc" +
                "uASETUNGPtMYPr50psx8yACryy1eNeGD1eGi/JKdeL3+6X1I4e" +
                "oGHZfN6aC47Z3yqWmIUjpTjbJQ6LYDWNxEo8N2ah7LFb+8J8gB" +
                "4tjhbllOs0Jpb+23qAhmXzeWsuOHQWysHZvGyc7RKHRbCaRmIl" +
                "nhuzUPbYrX1hPtxxfv8c9vu8ruVT2XrGaM0qrzbM1fnLp900gE" +
                "L97Qoaaeev6fvsFp1fsvWM0ZpV5+sTU0Q3DaBQf7uCRtr5a/o+" +
                "26NGp6NTOeWqPrHhARJx9vD2uczKumBAJCJqmrH2mMH1cyW2Y2" +
                "ZABV7Z6nEvjM7f625/5TyHXcs/5oPjX3bXt/7mpDkp+8SPk+2S" +
                "xRibGxWgI1llTORBRqkDG2FOy257qOuXcmoeXxWv7df5Hh1y5T" +
                "yHXV/u5AwGnefdnMGQK/9/cv813hnvyDm9wqc2TkVyvLSRxQpR" +
                "Ax4biVWwpq/TsyCOXJy2G8uNOFdoey/pWPTCX6vYMts=");
            
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
            final int compressedBytes = 1475;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWd1qXVUQPg8hKAj1QgXBpkhEUPTK3d0bwWdoIrlJaAvB3n" +
                "lRQwKJ5NoHaN4jeaC+hOmePfv7mbUNKQetss5hrZWZ75tvvllN" +
                "enJOxqvxarMZr8bpjEdEEWMfZybjrYUqqEEVWWQUqS64p/t0Fe" +
                "Coxa7TqDZwdqizt/ooe7M52bldH598Buz3Xzf3epx8eru+bOS/" +
                "uV3fluwXm608Tj65A9+9p97nJfP1veq/m89+n437PP2z32f//u" +
                "z32X/e//4+x5fjy9jjzFzENQM2Iq3nhQ7gIIsqRlo4xy0NZXC3" +
                "RKoT18ZdpHqdVp+4H554fFN+U3nzNhd57By3ItZRzdoBfVSn5U" +
                "PZmmux8qtXD9b6Orelevejzrg+5+Z/8Hj14N/p2///3Obr0fB0" +
                "eBp7nJmLGBkwgXOGl9dqTXRXBc6nP8e9Z/XunpgJvrp0BTjwad" +
                "cmMfbRcBR7nBN2lDEyYALnDC+v1Zq5vyhwfnFouPes3t0TM8FX" +
                "l64ABz7t2iTGPhgOYo9zwg4yRgZM4Jzh5bVaM/cXBc4vDg33nt" +
                "W7e2Im+OrSFeDAp12bxNhnw1nscU7YWcbIgAmcM7y8tqqyBldk" +
                "jfLbPav3WsH+2YlOzApw4J21Hxwb+3A4jD3OCTvMGBkwgXOGl9" +
                "dqzdxfFDi/ODTce1bv7omZ4KtLV4ADn3ZtEmPvD/uxxzlh+xkj" +
                "AyZwzvDyWq2Z+4sC5xeHhnvP6t09MRN8dekKcODTrk1i7NPhNP" +
                "Y4J+w0Y2TABM4ZXl5bVVmDK7JG+e2e1XutYP/sRCdmBTjwztoP" +
                "jpV98mH9TeqPn/Ory6/u8fvXR0v9T//o788fvE/vIsaH48PY48" +
                "xcxDUDNiKt54UO4CCLKkZaOMctDWVwt0SqE9fGXaR6nVafuB9i" +
                "7467scc5YbsZ1wzYiLSeV9ZCY7mLXVZhpIVz3NJQBndLpDpxbd" +
                "xFqtdp9Yn7Ifb1eB17nBN2nTEyYDLeWqjKr1kVWe7KCNfVnqpR" +
                "VYDrROpEJ2YF7s/LK90xevX379v9e0d9XH6/6Y8tPi5/6Hfwzq" +
                "/vr8fXsb89kcsYezIZby1UcYfaAxlFqgvu6T5dBThqses0qg2c" +
                "HersrT7Kbn5//ti/z7b5uO/rUX/0z+f734v/G/c5Ho/HsceZuY" +
                "hrBmxEWs8LHcBBFlWMtHCOWxrK4G6JVCeujbtI9TqtPnE/YD9+" +
                "8vhJ7HG+xSLKr3MHI3HO8PJaVs27UAUglbHWs3qvFeyfnejErA" +
                "AH3ln78SzM7j/vW/775rPhWexxZi5iZMAEzhleXqs18+eFosD5" +
                "5RNFw71n9e6emAm+unQFOPBp1yYx9t6wF3ucE7aXMTJgAucML6" +
                "/Vmrm/KHB+cWi496ze3RMzwVeXrgAHPu3aJMY+Ho5jj3PCjjNG" +
                "BkzgnOHltVoz9xcFzi8ODfee1bt7Yib46tIV4MCnXZvE2BfDRe" +
                "xxTthFxsiACZwzvLy2qrIGV2SN8ts9q/dawf7ZiU7MCnDgnbUf" +
                "HCu7vx5t+fXoxfAi9jgzFzEyYALnDC+v1Zr531MUOL/8ixvuPa" +
                "t398RM8NWlK8CBT7s2ibHPh/PY45yw84yRARM4Z3h5bVVlDa7I" +
                "GuW3e1bvtYL9sxOdmBXgwDtrPzhWdv953/L7zZ1xJ/Y4MxdxzY" +
                "CNSOt5oQM4yKKKkRbOcUtDGdwtkerEtXEXqV6n1Sfuh9g3403s" +
                "cU7YTcbIgMl4a6Eqv2ZVZLkrI1xXe6pGVQGuE6kTnZgVuD8vr3" +
                "THPH35PPmX/ilm/zz5Pf573EG/g/75fP98vt9nv887328+H57H" +
                "HmfmIkYGTOCc4eW1WjO/nxAFzi/vOAz3ntW7e2Im+OrSFeDAp1" +
                "2bRNn9+7P/vPf77PfZ7/MdPw95ND6KPc7MRVwzYCPSel7oAA6y" +
                "qGKkhXPc0lAGd0ukOnFt3EWq12n1ifvRicv7o9/6u5z+/qj//9" +
                "nvs9/nnff5F6MVvew=");
            
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
            final int rows = 14;
            final int cols = 84;
            final int compressedBytes = 679;
            final int uncompressedBytes = 4705;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtlE2OE0EUg/seLFgAEhJiA0KCdalPMlGyTZRcgJxl/o45SV" +
                "dXf7arW2gkItUryvbz82NmUvZlPwxlX+Z7GNqr/btVFI1XRE/2" +
                "es8w6AzvaD2eQudlTs2emVSJ3lOmAwly261NXD0M1++38+H6Ge" +
                "7vZXjX5/rpdr6t4L9u53eHfh3+y+f68R/8j3f6femQn+/q/zP9" +
                "vz6Uh1rr3bD6BkEJr4ie7PWe+edpDoovP/Hgc2afPTOpEr2nTA" +
                "cS5LZbm7h6fB6fa73f9VNf9U1tSuXXDl244QoK4kyfQmdmznSB" +
                "p5fq27g3vCb03dfmhPpxfKz1fs/cI29qUyq/dujCDVdQEGf6FD" +
                "ozc6YLPL1U38a94TWh7742x9XlXM611nv63T23NwhKeEX0ZK/3" +
                "zH8f5qD48hcUfM7ss2cmVaL3lOlAgtx2a5NQH8qh1npP3KG9QV" +
                "DCK6Ine71nnm8Oii8Jg8+ZffbMpEr0njIdSJDbbm0S6mM51lrv" +
                "iTu2NwhKeEX0ZK/3zPPNQfElYfA5s8+emVSJ3lOmAwly261NQr" +
                "0ru1rrPXG79gZBCa+Inuz1nnm+OSi+JAw+Z/bZM5Mq0XvKdCBB" +
                "bru1iavH1/G11vs9f7e+8qY2pfJrhy7ccAUFcaZPoTMzZ7rA00" +
                "v1bdwbXhP67mtzQv00PtV6v2fuiTe1KZVfO3ThhisoiDN9Cp2Z" +
                "OdMFnl6qb+Pe8JrQd1+bE+qX8aXW+z1zL7ypTan82qELN1xBQZ" +
                "zpU+jMzJku8PRSfRv3hteEvvvaHFeXS7nUWu/pu+DS3iAo4RXR" +
                "k73eM3/fmIPiyzdS8Dmzz56ZVIneU6YDCXLbrU1CfSqnWus9ca" +
                "f2BkEJr4ie7PWeeb45KL4kDD5n9tkzkyrRe8p0IEFuu7WJq4c3" +
                "Q5jAAw==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 6, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 35, 0, 4, 0, 36, 0, 37, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 6, 0, 0, 39, 7, 0, 0, 40, 41, 8, 0, 0, 0, 42, 43, 0, 44, 0, 0, 45, 0, 9, 0, 46, 0, 10, 47, 11, 0, 48, 0, 0, 0, 49, 50, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 0, 11, 0, 0, 0, 0, 0, 0, 52, 1, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 14, 15, 0, 0, 0, 53, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 1, 0, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 54, 0, 0, 0, 20, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 56, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 57, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 58, 0, 59, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 60, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 61, 0, 24, 0, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 11, 0, 2, 0, 12, 0, 0, 0, 0, 0, 13, 0, 0, 63, 14, 0, 64, 0, 0, 0, 65, 0, 0, 0, 66, 67, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 14, 0, 0, 68, 15, 0, 0, 16, 0, 0, 69, 17, 0, 0, 0, 0, 0, 25, 26, 1, 0, 27, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 29, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 30, 31, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 2, 33, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 35, 0, 0, 5, 4, 0, 36, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 38, 3, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 39, 0, 16, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 1, 6, 5, 0, 0, 43, 0, 7, 1, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 70, 45, 0, 46, 47, 0, 48, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 54, 0, 1, 55, 0, 0, 0, 8, 56, 0, 57, 0, 58, 0, 0, 0, 6, 7, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 59, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 3, 0, 8, 60, 61, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 62, 0, 0, 0, 0, 71, 0, 0, 0, 0, 63, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65, 66, 17, 18, 0, 19, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 67, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 68, 25, 26, 0, 0, 0, 69, 70, 0, 0, 0, 4, 0, 0, 5, 0, 0, 72, 71, 1, 0, 0, 0, 27, 72, 0, 0, 0, 28, 0, 0, 29, 0, 0, 0, 1, 0, 73, 0, 0, 0, 0, 0, 0, 74, 0, 0, 6, 0, 0, 11, 0, 0, 0, 0, 0, 0, 19, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 11, 0, 73, 74, 12, 0, 75, 75, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 76, 0, 13, 77, 78, 79, 80, 0, 81, 76, 82, 1, 83, 0, 77, 84, 85, 86, 78, 14, 2, 15, 0, 0, 0, 87, 0, 0, 0, 88, 0, 0, 89, 0, 90, 91, 5, 0, 0, 0, 0, 0, 0, 92, 93, 9, 0, 0, 2, 0, 94, 0, 0, 95, 1, 0, 96, 3, 0, 0, 0, 0, 0, 97, 2, 0, 0, 0, 0, 0, 0, 98, 99, 0, 0, 0, 0, 0, 0, 0, 0, 100, 101, 0, 3, 0, 4, 0, 0, 102, 1, 103, 0, 0, 0, 104, 105, 0, 0, 10, 0, 106, 4, 1, 0, 0, 0, 0, 1, 107, 108, 0, 0, 4, 109, 0, 6, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 111, 0, 0, 0, 0, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 5, 16, 0, 17, 112, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 18, 0, 0, 19, 0, 0, 0, 113, 7, 0, 114, 115, 0, 11, 0, 0, 0, 12, 0, 116, 0, 0, 0, 0, 20, 0, 2, 0, 0, 6, 0, 0, 0, 4, 0, 117, 118, 0, 5, 0, 0, 0, 0, 0, 119, 0, 0, 0, 120, 121, 122, 0, 7, 0, 123, 0, 13, 8, 0, 0, 2, 0, 124, 0, 3, 2, 125, 0, 0, 14, 126, 0, 0, 0, 15, 9, 0, 0, 0, 0, 79, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 127, 128, 0, 129, 130, 131, 132, 0, 0, 0, 0, 133, 0, 0, 0, 0, 1, 23, 24, 25, 26, 27, 28, 29, 134, 30, 80, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 135, 46, 47, 48, 49, 136, 50, 51, 52, 53, 56, 57, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 137, 138, 139, 0, 140, 0, 59, 4, 81, 0, 141, 7, 0, 0, 142, 143, 0, 0, 10, 60, 144, 145, 146, 147, 82, 148, 0, 149, 150, 151, 152, 153, 154, 155, 156, 61, 0, 157, 158, 159, 160, 0, 0, 7, 0, 0, 0, 0, 62, 0, 0, 0, 0, 161, 0, 162, 0, 0, 0, 0, 1, 0, 2, 163, 164, 0, 0, 165, 166, 11, 0, 0, 0, 167, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 168, 1, 0, 169, 170, 0, 0, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 171, 172, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 85, 0, 12, 0, 0, 0, 173, 2, 0, 3, 0, 0, 0, 13, 0, 174, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 175, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 178, 0, 2, 179, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 181, 0, 182, 183, 0, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 184, 0, 0, 0, 0, 0, 0, 16, 9, 10, 0, 11, 0, 12, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 22, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 188, 24, 17, 0, 0, 0, 0, 0, 0, 189, 0, 0, 1, 0, 0, 18, 190, 0, 3, 0, 7, 15, 0, 1, 0, 0, 0, 1, 0, 191, 25, 0, 63, 0, 0, 192, 0, 193, 0, 194, 19, 0, 0, 195, 0, 196, 0, 0, 20, 0, 0, 0, 86, 0, 26, 0, 197, 0, 0, 0, 0, 0, 198, 21, 0, 0, 0, 0, 0, 18, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 199, 0, 0, 0, 0, 0, 0, 0, 0, 0, 87, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 0, 0, 1, 200, 201, 0, 0, 0, 0, 0, 0, 202, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 203, 0, 0, 0, 204, 64, 0, 205, 0, 2, 3, 0, 3, 0, 0, 65, 88, 0, 0, 23, 0, 0, 0, 27, 206, 0, 207, 24, 28, 0, 208, 209, 0, 25, 210, 0, 211, 212, 213, 0, 214, 29, 215, 26, 216, 217, 218, 27, 219, 0, 220, 221, 6, 222, 223, 30, 0, 224, 225, 0, 0, 0, 0, 0, 66, 0, 2, 0, 0, 226, 227, 0, 228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 229, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 29, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 230, 4, 0, 0, 0, 0, 0, 0, 30, 0, 0, 1, 231, 232, 0, 1, 31, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 233, 69, 0, 0, 234, 0, 0, 235, 236, 0, 0, 0, 0, 32, 33, 0, 0, 3, 0, 0, 237, 0, 238, 0, 89, 239, 0, 240, 0, 0, 34, 0, 0, 0, 241, 0, 242, 35, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 37, 0, 0, 0, 0, 0, 20, 0, 0, 243, 244, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 7, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 245, 37, 246, 247, 38, 248, 0, 249, 0, 0, 0, 0, 39, 250, 0, 40, 0, 251, 0, 40, 252, 41, 0, 253, 0, 254, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 256, 0, 0, 257, 0, 8, 0, 0, 42, 0, 258, 259, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 260, 0, 261, 262, 263, 264, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 265, 43, 9, 0, 0, 10, 0, 12, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 70, 0, 0, 266, 0, 0, 0, 267, 0, 0, 0, 0, 44, 0, 0, 268, 269, 270, 0, 45, 271, 0, 272, 46, 47, 0, 0, 8, 273, 0, 2, 274, 275, 0, 0, 0, 48, 276, 8, 0, 277, 49, 278, 0, 0, 50, 0, 3, 279, 280, 0, 281, 0, 0, 0, 0, 0, 0, 0, 282, 283, 51, 0, 0, 52, 0, 0, 284, 0, 0, 0, 285, 0, 0, 286, 1, 0, 0, 0, 5, 2, 0, 0, 287, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 288, 44, 0, 0, 0, 0, 0, 71, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 289, 0, 0, 0, 0, 2, 0, 290, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 291, 45, 0, 0, 0, 292, 0, 0, 0, 0, 0, 0, 293, 0, 0, 0, 0, 0, 54, 0, 0, 55, 0, 294, 0, 0, 0, 0, 0, 0, 56, 0, 0, 36, 0, 0, 0, 37, 5, 295, 6, 296, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 2, 0, 297, 3, 298, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 299, 0, 300, 0, 301, 0, 0, 302, 0, 0, 0, 303, 0, 0, 57, 304, 0, 0, 0, 0, 0, 305, 0, 0, 7, 306, 0, 0, 0, 307, 308, 0, 46, 309, 0, 0, 0, 58, 90, 0, 0, 0, 310, 311, 59, 0, 60, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 91, 0, 0, 0, 2, 47, 61, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 312, 0, 48, 313, 49, 0, 72, 0, 50, 0, 0, 0, 0, 314, 63, 0, 0, 315, 64, 65, 0, 51, 0, 316, 66, 317, 0, 52, 67, 318, 319, 68, 69, 0, 53, 0, 320, 321, 0, 70, 54, 322, 0, 55, 0, 0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 323, 0, 9, 324, 0, 0, 325, 326, 327, 72, 0, 0, 0, 3, 0, 0, 328, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 329, 73, 0, 0, 0, 0, 74, 0, 0, 38, 0, 0, 0, 0, 0, 330, 59, 331, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 5, 332, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 333, 0, 0, 0, 0, 0, 334, 0, 61, 335, 62, 0, 63, 336, 337, 0, 0, 64, 338, 0, 65, 0, 0, 75, 0, 0, 339, 340, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 341, 342, 92, 0, 343, 0, 0, 344, 0, 0, 0, 77, 0, 0, 0, 0, 0, 66, 0, 78, 0, 345, 0, 79, 67, 346, 347, 348, 349, 0, 80, 81, 0, 350, 68, 82, 351, 0, 352, 353, 354, 83, 0, 0, 0, 355, 0, 0, 0, 0, 0, 3, 0, 7, 0, 0, 34, 8, 0, 1, 0, 0, 0, 0, 0, 0, 69, 356, 0, 70, 0, 0, 0, 84, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 357, 0, 358, 85, 359, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 360, 1, 0, 0, 0, 6, 0, 0, 0, 0, 0, 86, 73, 74, 361, 75, 0, 87, 88, 76, 0, 77, 362, 0, 363, 364, 0, 0, 365, 366, 0, 0, 0, 7, 0, 93, 89, 0, 0, 367, 0, 368, 0, 369, 370, 0, 90, 371, 372, 373, 374, 91, 92, 0, 0, 0, 375, 0, 376, 377, 378, 0, 93, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 379, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 380, 0, 381, 0, 0, 95, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 96, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 382, 383, 0, 0, 384, 385, 0, 386, 0, 0, 0, 0, 97, 98, 0, 0, 0, 94, 95, 0, 99, 100, 101, 387, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 388, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 389, 105, 0, 82, 106, 107, 0, 83, 390, 391, 0, 0, 0, 392, 0, 0, 108, 0, 0, 84, 0, 393, 0, 0, 85, 0, 394, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 395, 0, 0, 0, 396, 0, 87, 397, 0, 398, 0, 88, 0, 109, 110, 111, 0, 399, 0, 112, 400, 401, 0, 113, 402, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 115, 116, 0, 403, 0, 404, 0, 0, 117, 405, 0, 118, 119, 406, 0, 120, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 1, 2, 0, 6, 7, 8, 1, 5, 0, 2, 0, 3, 9, 1, 5, 0, 10, 0, 0, 5, 10, 11, 4, 12, 0, 13, 5, 10, 0, 6, 1, 0, 14, 15, 16, 12, 10, 17, 18, 16, 2, 16, 19, 5, 6, 16, 20, 3, 17, 10, 21, 22, 23, 24, 1, 0, 25, 26, 3, 27, 28, 1, 29, 30, 0, 5, 31, 5, 1, 32, 0, 17, 33, 34, 5, 1, 0, 8, 35, 36, 16, 2, 37, 38, 4, 1, 39, 1, 2, 1, 40, 41, 6, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 1, 48, 49, 3, 2, 50, 12, 51, 52, 53, 54, 0, 1, 6, 1, 55, 56, 1, 10, 4, 1, 57, 12, 58, 59, 17, 3, 60, 61, 62, 63, 2, 18, 15, 64, 65, 66, 20, 67, 21, 68, 5, 69, 12, 70, 3, 71, 72, 73, 0, 74, 1, 21, 75, 2, 76, 77, 78, 20, 0, 79, 18, 80, 81, 82, 0, 83, 84, 6, 6, 7, 2, 85, 3, 86, 87, 2, 88, 1, 89, 1, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 0, 6, 99, 7, 4, 100, 101, 102, 103, 2, 104, 105, 106, 0, 107, 108, 6, 109, 0, 110, 21, 7, 4, 4, 27, 111, 112, 10, 7, 113, 4, 1, 1, 114, 8, 9, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 10, 18, 0, 125, 4, 1, 1, 126, 127, 2, 29, 6, 4, 0, 128, 17, 2, 11, 129, 30, 130, 131, 132, 6, 13, 29, 6, 133, 18, 1, 134, 5, 14, 5, 0, 135, 20, 12, 2, 136, 137, 138, 21, 20, 21, 6, 9, 139, 1, 7, 140, 141, 22, 142, 3, 143, 144, 5, 145, 146, 147, 148, 149, 150, 25, 31, 151, 152, 8, 7, 153, 33, 24, 3, 154, 155, 8, 156, 8, 157, 158, 159, 160, 5, 161, 3, 162, 163, 164, 35, 11, 165, 166, 167, 36, 168, 2, 6, 2, 169, 170, 6, 39, 171, 172, 3, 173, 174, 175, 40, 22, 43, 176, 177, 2, 178, 56, 4, 9, 179, 180, 13, 44, 181, 182, 183, 184, 185, 186, 15, 0, 187, 188, 5, 5, 7, 20, 189, 190, 191, 9, 192, 193, 11, 1, 194, 195, 196, 18, 0, 3, 30, 197, 28, 22, 198, 2, 17, 12, 9, 4, 7, 23, 1, 199, 9, 200, 201, 0, 7, 8, 202, 6, 203, 1, 204, 205, 12, 206, 22, 207, 208, 2, 0, 209, 210, 211, 25, 8, 6, 17, 2, 2, 212, 8, 30, 4, 213, 214, 4, 215, 216, 45, 217, 10, 218, 219, 220, 1, 221, 222, 223, 8, 24, 47, 3, 11, 224, 46, 14, 225, 226, 9, 227, 228, 46, 229, 230, 231, 232, 233, 234, 20, 235, 236, 237, 238, 239, 3 };

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
            final int compressedBytes = 1591;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXU1y2zYUfoBhFXa6YFJNRukKcTSdLLrq5ABwp5PRMst0px" +
                "ln10vAmWnHOUAX3dnL3sK+SbLrMQr+iOIPQFIUQILUezOSZZEJ" +
                "wffzvT88+obcwt2P6ly/L35Vrz7K70Bcy08/P7+Df4G8/u83fs" +
                "8X7OWK//3Xp/U3+YvcUPkW3pBnvz+9/GcLSCEQrX1zqV+89A0r" +
                "fFb6RfRLIuuQjBTV9MekbgHozxutzFyvVOMXo2oVwQIEkT8Jfe" +
                "gOYKvYxT19AcsVVwvJBFzBRsg7/ePZ6gleIX6NTjex/1lB/L6g" +
                "sf9ZxP5nvc78zyPj90CZlt+t9j8Psf+B1P98fFqi/9EmeEtART" +
                "Gac22WRMrULNlzBRdAiNRWTGLs50Stxa2M4uNCM1aftFSI/87w" +
                "50Hjz2WOP2vNYg0zIJTWX0p5gj+PKf7AJiT8keQMHl/k+rORGv" +
                "olh+9/SPTnViZeINaf853+kFh/LuAa9ScQ/BQW/PyizfxrjJ9X" +
                "ZvzE+L3d/3wt+Z9vyD+P+PNZI2cFf4gNf4gH/OFZ3JuR4HnmRG" +
                "KfmZ3CYvea/JY607kQa2VPhNqKhISEhDSr+K+5/g2LOP77017/" +
                "LhTKoko8UfiW7lzsdRo/bMFF/HBzXopfP5ji/6b6yR/kAdryB4" +
                "r5A9Lw9LY9eXB4NZKYKS9YbfJJZBYdiaqR0yDq38X89ayH/c+C" +
                "aPbGWIaxpALLlvxmBvlb3v+Iiv2Pdayt+/pj0v94n/U/wEP/o8" +
                "hcSndfXYOpNZlbkECIQ/JZzyBd9a9+EofB++eyukA+hP8z9093" +
                "+LFN8AP7p0jejXVM9zz5/TMG/9u+vAT/JMH+cUUZaMcTncQvY/" +
                "ePi/h/aYofN7v4Mb++9HH92v1D7f5liP3zIPALabIU9z/V4fsv" +
                "SLj7L/b5X5f429x/f2esX7wz1B8b+vdfdvvHrqZZ/+CtjkYe7/" +
                "8d6u/nuv6S2etvRV6m+n95/wDMdP/A5InY93/UrC6Vpjv5sVDw" +
                "ZvD9L831h2L9crGvXwqsPxyuQnzY9di8C+4iCjfQ6Na/li771z" +
                "PCz17U0r/vOP8wXvx39P037z/ocP+iP+DJ6fPPXfhj6X8bgKHU" +
                "/+Z1DmafL0sc4p4LJG3rF5b1B0RRb/0dhYQb+2/cvzDQ/Ncx9f" +
                "PX9fo5K9XPYVc/v9/Xz0lr/fzw/POIlHd3TabDb4v+zXT/RIf+" +
                "B4hMfg8G+XXPX3zuv/ATP2F5YnbkX6TCCFojBuh8LPtZ5WygPE" +
                "dZboP05tDG6nRZ4gyY/n9FfgYVBVhPncxZXMThy6RznZLi49l3" +
                "jwsrJzUEdsRFB9ef2CFlS6gEtfxwHWrm/3Tz54F0UqI/7BRBjm" +
                "5JJmggarJJO21QR7e3Ijqzc0hxOteFFOvcrp/A0hhm8DqwkIKb" +
                "nkHc0xyrTJfG0+9B+CdD1zveWx6qdDodSL7s0Bgq8h1/hRHRRY" +
                "6UgLvTazI7/KIlaUelA6TEF240gWP//ayqGKSXAIQNYJriJ9JH" +
                "0l6jdNdoI50o92ER6wxItHGPDrEKdqg1ynbR0+OdU3ipx/Tws6" +
                "P/FQX/S5uEoOq/8CpDaMi36iVa4nsucFdKzAEpWN05WIsUcu+k" +
                "yBsG8tDu65D56WHjB6QA9Xc8ap1/NO0/r+3fwP0TJ6F/FEUQGv" +
                "+7zP9Z5gd9z//VnHJkBQjcf4z2i/c/dr0DbPObNatNTW9m83/z" +
                "15/e659vot5r/tLr8+MwfvYuP5yfRf89RfzD+AX530ADz8+5yb" +
                "+41QMOOz85ZtTd5f7nHiD0nb8dT/+R2oNa5houTrpnGKD/Mc7/" +
                "gnn+V3Sa/x32+ZM+tX+w+eWj5Nd9/vek5DcdkK3MP27z+cdklr" +
                "3T/KPr+tugIF3df6YKB2r7zxj6kQ6pA/pfpFOKqpr2r7LK/lU5" +
                "nB10tkpWz3YKXVRTIsSEU1RAQtBEcloNOSX9YWWMEkUIkxY8Su" +
                "NXp8//8JRfy5PJP2gL/yMVNfE/WPs7UW8RtR8MQ/7S762eQBWB" +
                "F/8cWF1+BFL8BJiG/cK0mqVl/hv8X/oHHZd1/zcNje6MaMQMvc" +
                "IbQk7y+WFd8j8LNiobSwcLejAX8iJ/CwJWuc3zN3SKsxZ+Owlk" +
                "HVIAIdl0nh9gWv//g+G/kw==");
            
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
            final int compressedBytes = 1136;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUl2nDAQLRTipwwLvbxeODtlOIieVz6Gj6Jk5WWO4KvkZo" +
                "H0BDRqhGbB/4sebEBSVenXL0EDUY+WAADYH7qZL/pXTvL0F0ns" +
                "9JH1L1r1r+9aaogfSF921JwUzBfHI2Ooy4tpC8AHbGRRMfpHQ0" +
                "Pj8/OXbnu9aZs0YQ+nbUK+We6QbKyOl9xY7Sk2xuA0nbndplea" +
                "TQgfe/GF78GxqgGL/KWRvwAAsIAw8xC73fpMIDKfIpvhv4Otfn" +
                "/yUZQyWv9X1x8Hes/nEh07NiAnfdXn/afta9Q/+QGjAuVDwwSJ" +
                "6291zsgCHgAiSz8gv/VEIpeIAf9oi/W/+Blg/aGN/EnG/iNdF+" +
                "H/As6/3ORfYTn+HZyHEHfiqbkaUHrtj/oHACChVmcv1EBFYo3M" +
                "upv/mUv+Z2wh2W/DcMC8CTWrsdv2lV4szrtrNrnQidbYVbE4xP" +
                "Lqz1CQyZ1obp8hcQI5wTY7Mm5cCcCFioHUEIdlF/Tzc771M8Q/" +
                "kC/+xZ36kdvGf6JAZZn3Rw4HNub/qn6/g/mD+Af2mz8BF/7Wwf" +
                "gb8UPBCjV5/ehz/XntEKM3C2j0PyR+dB3iXSj9oteW6UdBDyQb" +
                "9bOPuNcuSHXL3xijwyPXD6qV9J2eSb12b58e/9LXlyn/yHD6UY" +
                "Lbbflzz/yxq/yzL/3VjhNrJELgxgz+cbHJCAUwaM87F088m9Z/" +
                "0aHTGdAaahi2FiTVIFbrLl4cqQrrByvnQ+DzKrC/I70BBTAQv1" +
                "KOjteB9dfPMc/9Cxj/PWVsI6VaI93ouYOWXHpKO9LcrE6XKRLR" +
                "QvssY/uu/MFNYaNmw72VYZt35Y8w539953+A/fnK7C0pugM8+S" +
                "8X//JA4+fX9vm0/RRKNJX9/PN/7vgJrD90Qv7asgBmJqdubuk+" +
                "c/6s3n/u55+7Pf/Y3/9Nj/L/04Xx1JH22HB2fplZTxwudOB+Kb" +
                "UV4Ib4cdaPT9aN+fK9ylrDqbiHn9if5uzfnOxP8/o9a+StiR/H" +
                "/sfz/7r+C+3SfwWCih6Fw/lDQ/8drf8/cY3ij8qIPyCV/+/xzx" +
                "3+tdof2LHyCpC/7fiLnPkrjgKSu2nf2/8O+kes6kXZ8VN0/eNb" +
                "P+eFtFmM0AnjP/zvz4LHKZYuPKDDzL+Z+JHW8cOL0l9y/EV5zB" +
                "92M/7fpvnzOf74xYpRzKCy9W+Jkq4eiGl8WkyGqPNFLf+3TL21" +
                "3fuPFZ40Q4LFbL/R6Woukbl9wJrsZFW9DaqAIkxn6da+sf4SR1" +
                "L48HbRj81ZP/ak/qfXz451UBu6Fo+7FnCTe/d+Wy7clsyPQ9rq" +
                "vV+HeBOV999aX9lf/9pM84VWt9Kcvo03nUkdqt/tpQBGCfP8yu" +
                "B5N7F8z7zqkXn85T8/e5PlaKxVuM1d3ap2FBmpf9rte/4o2v1r" +
                "KrEfsO+KJubvv2x0Qw3Pz1bF2z9Jvt9Z/hMVzb/83habjv/YFo" +
                "19/fEm6g3o77TgchBreP5RReXrxT0synwqninSjt/r+S3beP5M" +
                "u7YGuMqFfzi2tdo=");
            
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
            final int compressedBytes = 983;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuO2zAMpVWj8ABdaJFFC3ShFj2IMCeZo7BzgjlCjlpnkv" +
                "iTWo5tfSjZ7wEJ8jFNmaLIR0lxarKaqKaGDF1hSBmy1H6qLm/P" +
                "tn2qvtRUVc2J+PJR1T6YG7J0R317ANuQxnYqvcoyLLrs4Ia6Mc" +
                "KQz0necQI1cWTTPYXU7zp+4iX8D/LB/T9JsMxX/9Ht7yHfik7x" +
                "v54v8CfLu/A/uvO/6ykG/O/Y40+NTqBHX1SfL+xEGmDEr72MXz" +
                "N+Y+OzW4/2VxGbZYX1xz89ELta0456dZZGB5SnLfK+Zbln/siM" +
                "/wVqWhPb8Iv9x25phh6da0ezJaOk10p+PPDHk5s/8og/vhZiHR" +
                "t9/HjOXxQv7zv+o+j3Hj8p7BekfpPt/wD5T9h/pecPED+yAeoP" +
                "7wr6gUQ5GSZM7cPfUui36BmP0WCz1a+GPe+3f8DJX27yL+eOv4" +
                "zkPy7yOouR5+Zf6jn/CsPfPOuXsiOfl/0BAADKjX8h9u85J15k" +
                "4/eC/Fcyf5i2v1JzMipRM2wq7b0qK6QfkIddHi6EwUunLuaESp" +
                "h6yan9jBFyxPrNJ/8Hq591lqlHD5q2r+HFRdi/AMytn+LHOgey" +
                "v92xkUPG/9WdwoGuQFT/fooDaf8BMgc4BPwf+RNA/AGAUvIPx8" +
                "8/2/e/g6gfMCHbEtpfo//A/wCghAFmwpFkkGxZOqcWHAPk02Hh" +
                "94/5olnncFH3jw3kr9ev8tp/DgB554NNBXMo3ZL6ASAvZEiN/8" +
                "+/b0nXnwKsfzVOSlsC010UFnm/+sW1oflOgF9vjl/GHb9MF78a" +
                "2K8EWKnxow8+/jW8Cf0PJJ0m6NH09//pE9Un625CVzL1zKSjnf" +
                "yqNru3fwDevj9KCkoPAEBJ8dOig4qr34lK33+/n30YdnH/qSf9" +
                "pxP2n1zxwqVewmG3DklHkj3VH9WCxubD57v4xV3+OS3NP6+S/R" +
                "8v/wa//vz51/z8x6Pn2tXjL9/+P0J0FULC/++Qjv/Y0ppt/fTu" +
                "it/fcP/cLGvFpyR8r6syhrBJERDJ0wXvXwOKxq+OoTEpbjR9JV" +
                "PZPze/NK0LntuXP78Tq8ua6+97Mn7Rf+nHG+yH+gfIq1qpn/pA" +
                "7elDdcHWAfKKRg/1o9pWP0rvXwKizR/g/mdAGUXceCYB7UfO3x" +
                "b/1q9/Xc33sPDJE9pM5f5OsP3pPFtAPn+F6cGu69Sr7FAIAQBP" +
                "YR/+x5Px4z3Z7481if//I0frmQj3b9F5xf81+wfT1x8R7L8q/w" +
                "7lWaj+OlE9GevV1UDmgY/yXf8MqnXjZ2i/nv+q3nuqW0NH6cnm" +
                "4H//AFE6ynM=");
            
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
            final int compressedBytes = 861;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnVturDAMhkMOlah0HniYBeTsBHUlXYrVFXQJXeopc0HAAJ" +
                "N7bPJ/D6MZTU1M7Dg2yaRKjbSqU0ZN3N/q6+swvjTjS3dRM7rb" +
                "Nw7yalf+mKO/a5Xql+0bpeft01X6T/urwq/+NAmSffuvaUMk1/" +
                "pfbPX/OLxy79u+U/9tyPcR9G+WH0mlJch+3/b2Iwf7gVBmEeke" +
                "i3L506px92GZjQTjfy1PKeMvj/gPAAAAADb06AIAgFI1J/uocw" +
                "AAiRif1dL0yaBD/CF0wWn6v3eoPzzWHwz8in39KF2eef3dnt3+" +
                "ofl77f5Tu/5i+s84akCPvNPzDqzl2RNx/dPd2kgrRDCI0LL39P" +
                "9hy/+H8XJX//8Z774Z/b95+P+1YH21f+Eu//4zjZ+F/PcoHzs+" +
                "djXbv/jwGF73mWZ/M50AHQHr/CHS/tOj/I749cJOetNLsWIAF/" +
                "XWbRlG3y5tVuajh/1BnvlX2vxtxKndphhWueu3+KmERWK6pz/l" +
                "078raT/Ub4H+35f1/xzjD/UXxh8AwVRb2jvXi01heQBCgP/tdI" +
                "aRUfsCAHKT6GEt8v+cBZasJDf1+vPs+cNNXidcf45TpNjuq+GT" +
                "1DQYggDxP00CjP0I8D/pELqAeRb2vERJ0+jZWr1sTRz7h+6fcT" +
                "s/zff8MHrdf4XGAc5vKhu/8uz/KjH/UaD/ESP/y26//Ocvdo7W" +
                "N5HbR/4Gas6fIszfRuD8zSH/MMni2vnrV3/7/bU9v/RQHoDZyD" +
                "UxQ0E+9M77Tc5zqh7OBwQnzt+GPC17zZ+b65+NnXwzy78c2ndb" +
                "f7Vpv6L4MWSrlkFlkWtz/Jn98Wem+qtz9901dP9ycal/RwK3S4" +
                "5inwz6z1d/FUn/PO0PnJLlHGD/L0gZNZ6GLMf8tb75zyisf6bN" +
                "XaXkP7b2j7n/gDzaL22/0s/vJfx+WFqcIGb9H56/JZj/DOv7Fx" +
                "P/0/VfnodDPOJPW63/lI4fceyf+vxx5P8gR/6bpn6SPk8y1//0" +
                "5zel8H9ilb/lMZRm7Bs6UFBbpgJGlslc/V8/+f/XXvzG/isA6o" +
                "Bc5k+Bv38Abg9RyKlGRv4Nzlr/gmUlycZ+Pv8/BEeqS05FhIQb" +
                "0VNkm6xbfH9/as7vNHLdCiD/kM8A/eG/8H/5nPEotP8hUcUv");
            
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
            final int compressedBytes = 796;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXV2O0zAQnpgIBYkHC+0DSDyYFQexOAlHGW6yR6Wl21KWpL" +
                "GT2DMef99DFanrTjyev2/sZIky4JL/cqAsjJSNoFo+AEhgzHFS" +
                "4A28yIL58+d0CyiBXLgLtxzPn+/GU0ibnoiv45gnigfcf4b84S" +
                "p/WJKvzhfuA3vYYweI9kAluNTahaErQ2lbTnJu/rl8e47/k1z+" +
                "O0p+gft/Sr3/Hz3bb0MZNWL9NVueRf2jf1Ejf8yP9/rsh9e1MK" +
                "VFL+6Q0WzkqdPspf34+eo/vGL/H5f8J2t8wfWTtp9M7Gk+uYVr" +
                "oCICoU+kCV3E74fTPuw3eNevxc4NMcIXAQCw1D+oCi7G/4/h3y" +
                "L6mDTov3ck2J/fa3+x7fmT9PwF9DfU8h8d+tfefy7HXwr0n+E/" +
                "1fPPdvutU39Ir590/dV3/YL+AQAAAACsJPhQXfawR77S80vdnD" +
                "8aKc7Vr/G8Kn/0/3LW/7D4/MPD+tfZ4H+toaXd3r321/r8gb7S" +
                "c5/3zzn5H/FfGczoD0cDi2XxsvUvK5evG8OBtGMQHg+sOYIr4K" +
                "xAKf4F/gPYQnKRxXbmjJAqqHkD++/m6tfk818tnB8SLtkV2E9p" +
                "+9cNxXby1/75Zv8b9s9s569IQGJECFBDX/VPwnjOyJ9+mYa6Io" +
                "556PsDwR8Qv4EN9We764/z67L8a+/5r/vxbHT9yt9n6fzDxeYv" +
                "7b892L/l/Puv/oOV/pFJuBIhyy2yk9lSSCFB9nTZLrxN4mKkn2" +
                "ZeqTX8T9VW509V5o+jOT3zB/Svwb9p/8EF6YMPk/olMtpoGk3L" +
                "18x/gBr5g6FCAEhgQr3JLxL/fZfWEwj730b5R7n8W+j5dVP7bx" +
                "vGq5q/wPpXjv/t0kKcHzWb/w57f31s0H4DNfD/5+C/4v0b7D+3" +
                "7f+HRRE7+Rfnf7qzXwAAAPB/4BjGCLRRP+qQr7v+KxhYq48HgG" +
                "Lw8H9BBML+aYuZX7R/Y3D+0v1z8A9l8dty/P92Y8hMjidP7ykM" +
                "8fsrGwsny3w5XX79TOxoDPR8VcYH/4u+/HyYwSctio2Kx/fw/L" +
                "Jq//kNJ0DuvQ==");
            
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
            final int rows = 178;
            final int cols = 16;
            final int compressedBytes = 217;
            final int uncompressedBytes = 11393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmE0KhDAMRmN10aUMHqBHKXOyHn3+QCo4KMPYNul7C8WFmD" +
                "QJ3xdFmmTYPibRTBQwW3+3uR2mEqitKSaR+XX1a2XD4kLWE+k9" +
                "/eP07AO/ZI2cvNwvislTFoBqMH8dqoBp/5iqxl8h/9P+c0f/5a" +
                "z+xz+8b9A/mctfYZ2K7V9G68/5gWZ+61+Mr25m/Df+qez+UDv+" +
                "Rs+vqf/fUfn72ucnXhh/7/nj3wBsg35155/Qn0L920P+x1tz/K" +
                "zOLlfb284+PVhYtgH9bGn+mv9+7/EDfOUBqhc5eQ==");
            
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
            final int compressedBytes = 4175;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW2mY1UQWvXWTVN7SMmCziKBig7SCgCKKNjaIC66M2rjRLC" +
                "K4izQogoKjgCigoKKAuyKLqICCuK+ggqJ+wijiMuKCOCDgis7o" +
                "APPmVqWyvqRfXi+0Pybfl5ekklQqOXXOvXXrPq0NMEBIQwq3a4" +
                "1gdyiE5rwhFEEHaAXtoL12MHTSOSsxV0EJng+l0FU3sTh1EvTA" +
                "c/BKeAd66v+FXriTPwnnwvlwoVYMl8HluNTYbgzSNiY7wkgYAX" +
                "+D6/l80MGEesZPOEV/gWWgCewB+2i74wN6P9YycRK01q9lbbVf" +
                "oS1uM3rAwXBIYkVqLtuJU+FwOAJnQTc4Fo6HE7Sf8Q84Q2uCz0" +
                "Nv6KPvy16C/qwsuVyvjzfjb3o9qMBfYThcrd0C17ILmInHa+/D" +
                "btAQX+GNoRE0hqawJw6HvbSJ0AL2Za2gGA6ANloD1PQyOEjbDJ" +
                "3hMGwEXfAhPgS6wzFwHA7E8tQcOBlOQcCj9XI4Hc5Otoa+MEAr" +
                "hIEwSJsM78JFcDEMgWGwCq7EPjAarjN7gQYGcNwACUjicZhh26" +
                "EA6mstzdXQAJph0/QRGkJL9iLsh8v0Voly2B8OhI5wqN4Xu2F3" +
                "3IE/4wg4knWHo+Bo/S04kS+Gk+CvcCqUwVlwpnkGnAPl0A/OY7" +
                "3hArgELoXB5nJeAUPhimQJXKXtD9fAKL7E6IqXwHh9JG+P281v" +
                "8WoYk8kk52cy0IG3y9BidMtkBL5GX7woIxdDx+L0jEyG8L2GPU" +
                "dXn0z7OxMPZHxL8p7kqYRvJ/uYz1flU3GGcQjvYJebpXyNMUBe" +
                "0c0oN+pRyTPmMnGceCv9aiajzXTrhLF0TPjK+85Srfkqk2Fl1M" +
                "b6dEcjne7HX+lKwpfKCV+646NMyKJNFL+slX2sj5Clm8UvXkh7" +
                "fflw1fKXM6ELDDAmyXsms+e95fxp8Zs63jrCf6qtcz51hTy+LN" +
                "1Fu1V9g7F8esKpg3+Ap/GpuIOuGU0t7O58qW+8TyF8x1l77FLP" +
                "syvsPaMUbqDjJXATPo7zYAI+htvD30N7kuooSQ3nr/JX6Ginqb" +
                "4JnqPa+77ETb2H+Yv8HZe6P3Wx763n23v4BtXnvu2ngr/yHp21" +
                "FfjitkT7TOSSam2+7T1mL6nntnDql/jKcxfI9u+Ixtf5WusTq/" +
                "haL77E31GqvoH8GfUO1Fv1ck8dhbIflvhrxj6+57QL4muutvAt" +
                "YNpC+m4FtL/Mewfx92553bO4sJLvUOZ8gVFh+BJ/b6Tat5jb+G" +
                "u2PsNE/IL0eRJ+DbfCnaTPU6E9rjOugimsJD1X8Bduh66q9/ZQ" +
                "25603mKupN9zZb1Sn+n5M1OD8XMqJX2W+G8EXb3pZwJfoc/0Dt" +
                "Qr+BqBL7RO/J21NWkveXm6SOizbO+h8hlH0Er6LN+ro8Xf4CL4" +
                "K6+tsPBNdLHx1dvQcUN1trFswVcWvrAvrZMtfU6stvlL+iz4u5" +
                "aPEfrssFXqs9y7Gc724qtNprLbhD57uH1d+na55VpnF18osPCF" +
                "BhJf1BaCePP9nLsOpPVQ/jF+ou5ZD0dSyVHOeanPtJX6TFtLn3" +
                "34Cn2mM0Kf7wA6Y3zk2l+9iWV/6XwRn2nZ32R3g57GFuiN+Wx9" +
                "D2F/Q/DtlTwjtUbYX1ki8U2/mH5YHnnt7zQQ+pugdsyy8FU9UO" +
                "JLW+IvtKV797PxVc9w8CX7u17YX1lK9ldupwt9NgwbX1pnwNWp" +
                "WXAt1Uj6bHBhf+U5aX9d/rr21+n1Et/krdBF+5KPDeIrv+Anwv" +
                "46pdL+0lbZX9q7klayv/SrSXxLhP218QXZC4X9Td5GT8vC17K/" +
                "iQaqzza17S+dO9HG17K/8nqP/aWjwV58SZ/vkvh+TvjerQ/VK+" +
                "C+1CmE7z3kXw0U+PI+0E4frF9ufMIHMdIg83q6r5SX837EotNo" +
                "7em0qxdhQn3ewtda0i+nZ/Hz1HmJr9yrpw8xCwS+Po1tqbZteX" +
                "+J7zFRqgQnpKgf+PFVPVeqBx+griN9Fv6VLOsNu6nSRrTeG6ix" +
                "2Kemm1VpF72Qj4low+mefcLXc+TgK480WWNXC191ha0yzVKnC3" +
                "yz6u5Ier84UajwHaZKFb7qqMx/DxvlxddT1/3y90F42OIv7T8E" +
                "M4m/DyTbwyyYbelzUnpCbKm6Z064PhMmSy19tvlbkEzPMQ5y9V" +
                "nwV9kfsvvJg1z+JpZ4+Sv7xnFR/BX6nP3Fbf/K5q9tfy3+huuz" +
                "PBL8nZzN3zRpqi6+WAh/E0+5+uxpYZY+qy3XjvWUFqgt8TN9OH" +
                "Hr9zB9Jv42dPVZ8DdMn7389bVE8ZfWuYK//GtgfCs8Dk/wTZK/" +
                "86C50VGMj5JPkD4vwHXwKF/PSuApuuNJKKU7NtP46DRYlJ7Kt3" +
                "j4+7GfvwUD0zuF/fXyFx6R+ryA6ugQzl8xPqJ7D5M9YlvWV3yM" +
                "+Ls0nL+2PmfzV+izh7/zXXwr5e9V4exNPG3zFwbE4u8p0fzlG8" +
                "L4S/g2tvGtjL+wMMhf/j3/0VPXYvn7tG1/javT0137m/zMsr+q" +
                "jhIgH9IYadtfwtdnfwmTAr/9pZL+WfbXhHpKTSPsr9lS4lsabX" +
                "9TXwbxhf5+/yqIr97Gb39hiVOv8K9C7K/A12gQzt8UBO2v3Ebb" +
                "3zIXX6/9FfiaH4TbX8dnbGr5Vznsr8QXnhX67OPvczZ/LXwVfw" +
                "lfYygUGZdb+KapLwn+ErpjhP2Froq/PQo6pDQXX+E/+/FVbVb4" +
                "0p4HX68+U83DLHzl9W3lL+Hr8teLb7pZOL7OtcK/Gl4pvnvGwZ" +
                "dfFo6vsL9x8YXnCd8zo/HlG8Lw9ahJ/Vj4rrPjG4K/HnzvcvT5" +
                "FtLnF1Imn0L4vgjN001k/Ooloc/C/lr6rNUT+myME/jS3Ys837" +
                "SX2p4f4ZFIfG19tvAN12cb32h9ltsQfQ5cGcDXr89x/Cs+KbLu" +
                "/PS5PFyf5RuG6rMX33z1OeWxUiDjMtiCm7CcI7wCb/A/4E14C5" +
                "ZhJ1jKBTffpjYU4cHYGcnu4oHKKnTAYiQO4F54ABJOSH0M23DZ" +
                "C7ELNpfbvXFfJM5jked5r8l3apn9xXgq7DviYZmYCysLw9frX0" +
                "Xe2SoQ9djstGlSpkYWbXAlMYqKQKtfDXyXVsK/yvn2TvwKyZrC" +
                "605tK2Qdjj6nknyHpc9Y4cafeRF04uuxzFwBJTjE1ufg+Jfq2d" +
                "+KP+fSZxtfK/6s9HmApc9GeZg+wwe2Pov4czZ/o/VZWbEQfYa9" +
                "aG0Rps8i/mzzt3J99vI3Qp8N0udh4fps8derz3b82Y9vtD7b8W" +
                "cc6upzyvDo8zVCn3ECN/Emjng93gifwSfwMY5BGdmEtXT2Oi4Z" +
                "iBNDGTbe05bOufoZfJQff6uz2Py141dRC26J4q/ZNM8nfug7+t" +
                "ypcWQedazJ5m+OO/r5489U8qmz9w/5u86JXy3Gp13/mVbFX/kd" +
                "lM8ZFb+itpyQ278S8Q0vf50xR/Mw/yrcf47DX9v+RvD3izj+lV" +
                "lcff9K+s93RPOX9ir1ryrnr+1faZ74hs9//lLwF76y8YWv6SgK" +
                "3+dy4ntSPP85DF93fFQb+Go7Aviuj4Vv+5rBV+9c2/iq8dE3Wf" +
                "huEPgmWjta9SGfItm0X0DD3sNVuAbfpb13nLKVudUGV8fVpURx" +
                "9dQ46F/Bt5X7V3z5rvOv9C5VvxdHeOcHc/tXga8gZ/RgC2yFjb" +
                "AZfsC15lr4GTbB91T6Yxx8+Yr88IXvIlroGR9VdcmOXzlneFbL" +
                "hkd6uzWOr/FY9fCN0btHReD7k+xf7W19TtwB25T/vMnVZ2LXnf" +
                "SkzWH6zD/LL74h7K9T7rW/M3ap/f0llj5/WjP6bMyvuj7j6Dj6" +
                "rHeIsL+/yvhGffgd/gP/hu1aAv7g0u9LnkVnd+TmL15TM/psDK" +
                "sr/zmav7n9Z/gt1rs9UQ3+jo5lATpEtO9f8ndn8kHlX/2X/0HH" +
                "haZKrrDmj9R77yU16w3IRM0f2fP7YfxVe7rv6R7+ivERtPadjZ" +
                "w/yvnVK8Lw9c8f+fg7OYy/lj5H8Dev+SNjSfj8UTh/xfyRJ6tE" +
                "i57fd/mLQ8PnjxiAUm6zW/JRJDvBibNMxVvAP8NTUpX+l5wX87" +
                "qYNkrkX+XDXzd/I3/+5ra/MCAWf5+vBn8XVvr8cTnad4P8BkPM" +
                "7jiPDcVKv3HV8I3t/9aAf1XT+OYb34jE9+XawjeXf8Wc6KftX8" +
                "n9QpA9l11h6TMvSm5iw9gCdeXtUfpsnh1Pn0PjGzP+fPqsF9aQ" +
                "Pi+ruj7b49/K9TnQEte/kvlXqWmEpcdPgjF+fbbiG7n5a54Tr0" +
                "+GxSfjjn93pT7H8K/i6fMbVecvb1V9fcYWwLQHU/O1+9i1In7F" +
                "RtP4qBNIVKFdcP5I5G9kzx9BL2xj9hbzg+78kXY/liTnWfNH9v" +
                "ygNz4ZZX/d+cGw+aP85gfd8ZEqzSP/Khrf/PKvjLej5wdD6u4Y" +
                "hm/l+VdUosZH6MsrDuZfpVaxsVb+FZU6+VcWf7WH1D2R+Vdm+f" +
                "/1OVSf36kzfZ5rxSfZDRa+2uNsnDYPCtOEIBtvj4+Iv57xr40v" +
                "rszCt08QX2v8GwvfYm32nw1ffKiG8P2mrvBlN8r5hQfZBIXve9" +
                "rrgr/sJmm7Jrr4aittfNmkSHz7ZuObPjkevkKf/2z4Cn2uEXw3" +
                "xsNXe7l2+KutJvu7Ch7X1mgfSvt7K9nfcWJ+X9hfdps9/+vmT6" +
                "p6FrnzvyI+afaLl58TZn9911YyRhL5OYlt1bS/MfNzEj/lsr/x" +
                "8nOM72rL/sLCoP0N1CXzJ9k0O/5M+3dFzg+WeuPPwr8S/MUD3P" +
                "izea4Vf9Y2eO0vFsWZH5THWfFn17/yxp+TenXjz+zOOPHnJKuZ" +
                "+DNvUNvzgxa+bLrlX3n0eYbM32jh4mvP/1r+s42v5T+zuy3/Gb" +
                "pa/jP0sPxn6Kn85/MEvsJ/lv8P3Vv71sq/8uE7rfr4hswvTM8P" +
                "34A+R80vNK0hfJvsGnxt/zmYH4st6K1Ii7StmjMniJ08z7Dxvb" +
                "fy/DpzoDznjo9+CObXRY9/I6I3sfPr6mZ+Id5i7qit8W/IF/ON" +
                "j9g9zvj3J7K/W3Vd2t8HLP669tfCF57CAy37q/i7KDD+HRQY/3" +
                "rwrZr9zca3avaX9qpgf5M31oz9TRi7zv4Gxr9OZJTJf3uxh6ns" +
                "TTabzeSSi+lpdn4sH+Tmx4r/l1n81VMWvhKLNub5fv7SWsLPi5" +
                "cf63mj/rn4m9Qj7pTeu/f/Zc6Z3lXhb3JCzfA3cXQeqhOSH5sv" +
                "f4P5sWwe7S2n7Rw216lX4lswxdZnC191zsFX+Ffo5KmbVizwLy" +
                "6+dG0t4JvYtmvwjfaf85xDG1dX+LJHLH228BX57YK/Vn47k9zP" +
                "J7/dvNDPX71Z/Pz22rW/+SwuvnphzeCbGl+3/NX3tueP9H3YIj" +
                "F/RNdZ+c+e+KSd/8wWe+OTYvzrxDcuqnp80us/13x8Qx7XWXwj" +
                "NaHO4ldPCf9ZPwCV5cae7BUqdayyyG+354+wQaCnZOWsmRfn/P" +
                "If5cff6izV528yz3nbqPz29MF51FGl/PZASSC/nS2zx796SXj+" +
                "sy/+XBoanxTxjUuC+XVu/DlXfnv4+DeMv1UZ/wr/ue7Gv7Tusv" +
                "FvID5p5bf/D8LbGDg=");
            
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
            final int compressedBytes = 2127;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW3lsFFUY/+bNm9l2l2KRQxAkpSAVaLlEkKNeGC5Bw6FERU" +
                "EpinKfchUBIQLliHIopYZDQFBjon/4JyZGMUChReqVEOUmFKqS" +
                "aIxHsn7z3pvZ2dmZzm6nzO62TPLmvXnz5s3s+83vu+bbcBgkIB" +
                "CCoPRVOAy3Q3Noh3Uulk5QAN1VbIXDtDDMNiiEB0RriKhHYRkb" +
                "DgdehYlQxHqmwYywscFrsBCK4XVsUdgKTdWOov8OaB02bdBZ1N" +
                "3YvhfcazrXH8uD8Cjuh7HjcWz/NDzL6m1R88xi+0VYlhl9WdCC" +
                "1S2hFbQxje2AJQ+6QFe9R67Gnr5YBgZwHDwMg42xj8FI0RoN44" +
                "3eF2Eyq1+GqTAT5mBrHpalsBz3MhYVSwZkitFNIJvVzaCt6MHV" +
                "gLuN2fKhN9wXeT61EwzC3ofgEdwPxzICHocnYAw8BU+y8c/Ac/" +
                "AC1lPgFdxPZ32zYS7uF2C5CEvCYZKjzyYd0Vukj+keuaQX6UcQ" +
                "X5IvzvYgeeROrO8iXUhPrPEZSNfANHZuIGnH6vZ0JBmAdW7Ysu" +
                "n4um+kr11vZnk812r41mXT8BX3ORZO+qZ2Smw86W4+kr5mzJwC" +
                "16VjUC0dpy9h3wlcmxosv5nwLScVpIocx+uN30yOWucOTI+5W2" +
                "XMql9NBF/7LZjlD77BkL9Y0iLv+EZvEmMCeVM6KVVJp+lcskaq" +
                "lE5J35IVZJWB0XIun8la2/dltQnfGXG+kx39WC3v+AbapB9/Lf" +
                "hWMIxykL/fIX9/wHdoOdyQvoca0ieKv/HJ59kW+VwcK58T46+9" +
                "fA4+7xN/J6S9fGb8pZuw9aOYb6PNPbh9tdlt7sCchsVfdV3a8/" +
                "cnwV9hP9Mt0lluPyN/c+k23X7m/KXvaPzV7GfOXxjC+QujyCAY" +
                "i/ydp9nPGn81+xn5u5Xz12w/Y0nAfo7wNxn2c/BI+tnPGn9N9v" +
                "Mfmv0sXQFJusTwvqD5R1ja0XeBsRYKdP7q+hcKbdmi+UfzsS5y" +
                "YBPDl7WauvGX4xsjefaKszb4OvHXjK+oW+KvvGgZmWfHXxhIdj" +
                "nOPdrUFviKIwNfdiSLXgNfbGeLuq3D3L3t+KvjK47GxFxl8o9M" +
                "/L3M+Stdg3+kavjXtJroH8F/CerfBRb9u+Pm+Ed+yefgOdd7/J" +
                "ni+vcq06xlyN/rEtpTdDfdKfi7C+XzIZ2/dD99j34o/Ur30IM6" +
                "f2lpDH8XWvlLD2DZVz/8Nc76xt/ghbTnbw3nL/1YxK9+Z2Ob6/" +
                "EN1L9vRfQv5690w1b/liB/F8FEzl+hfz+K6F/xFNTMXyf9K44c" +
                "9G8czJ1lx1+uf7FuFdWr6d8NdvoXWxMc9e/6iP41zm7W9K/peL" +
                "mo1ahRTUTdzOix6F8sNvrXOM/0L9YW/Wvmb0T/SsDiV5kkSEIk" +
                "mwRIE1sZwfVvM4skUGPs58X+2M/BmrgkVZZn+Xwt3e1nsRIjyX" +
                "AyguBbQUZ5wndJKvlH3vFN//iG0JJX2HocxtY5e3zjjE8udY9P" +
                "phO+wb9qXbUL6YKvEmC/C2Wz4rAmpFwJafjKTTV85Sx7fG2uQ3" +
                "yVzOS8+Q2Dv3J2PczRnu1zalmrcixVym218ze1Nu/4hqQUwLdD" +
                "PfC3hdFiayIJKxt6ROQz56+LfF4WH3+9yGdY6aP/e8T1HpOSK5" +
                "9hlcvzvcH9I7k/949Eb+3+0TpH/6g42j9SWqW3fxTqmvb+0RrN" +
                "P1La6/FnpUAeEpu/ocnniH2l52+Qo9b8Ddxb8je4fWXN3zCeNc" +
                "XzN0L5yY8/B1bULX9DuYfhu53nbygjjF83LBKfNNnP8cUnN0TH" +
                "J5Xh6R2fTEf/KDo+KQ9lWneJsljaq6B3I49z9n+VZW5zB7a7rv" +
                "kX/vlH6YkvHLbiq/E3wTm+NOKTBxhyxdjH5XOWrn/FSF3/muwr" +
                "ebKtfC7B9fhA079W+axk1l3/2sln//SvFr9KN/2ry2ehfz9j+n" +
                "eTslG3r+SpsfgGPmFvgfi+L31qmz9ZwvVvNL6I8Bmuf834uulf" +
                "P/AlZ93xrSV/0jd8yXnv+KL+LZUtsWOz/sUjpn8z5pN82tpZ/4" +
                "qxEf3r8H0wAW1iq39D433yj1Ig/kwT1BGW74Msf0MpUzZq+XUZ" +
                "J7CnUjolR2laOp3OMPO3Hp55prfrQ7bfBDPKGyS+c7xcLS/l/q" +
                "+yX8NXeV/DV16Ja3Xajr9u9nMMf/fdHP42Jvs5IHuyn8UXAbgu" +
                "r4JqmUU7YvOfRWzrII5nvmtm0MOqX/X+m0PzG4989hZ/FvnPOT" +
                "jPamf9m1lYR/5+TgaQM6nJX92+SnX+kvPe9a+WPyl+21blG56f" +
                "o+VPslUqsMpnLT8nFl8tf5KNL4rOf1Z6cnzN+TmuyHSrDV/v+T" +
                "nytmh8nfJznPH1Lz9Hxzfe/BwLvpfFWdOXEo6vUmHGl91prbwD" +
                "uTzJOX+S4+vw1HXC1/bssNBO7/hGjXTKv7qWfHx1+9lL/hUtM2" +
                "bbrbCIcbA53YUa95TRz/LrsN5DDxp9pXFYf0Z+nTgu8TES5Lt9" +
                "Rdcn376yPNEGYT//orytbHHzfxPWvz9H5LP/+rf277+NQ/8KlM" +
                "v0+CTy95IWvwpe1PhLD/H4VTR/9fgkLXWPX3H+pmL8Kp74ZCrE" +
                "rwKy9/gVk9To/7Ka+b8xb0W52tn9+77NdZXJe/Mbhn/k0bsyMu" +
                "IUlkum5jngFFd+XUPDNxXks7eN+0dqPkHLTO3mlB8rsHLNj/Vr" +
                "Cx33rn8bB3/5pqK2kSvUQrlKPm2sTkxmj/v/u33D9+QtfBOyuV" +
                "jmpDq4vu1n9f5kxp8bRn5swr/axn5W+zP5PDRt5PPft/ibkHwe" +
                "oOEb6OuOL/++EOiXXHwbE3/rI78d/gdJujGP");
            
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
            final int compressedBytes = 923;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm89rE0EUx4fX5CVN0oMgKKLFH6BW6o9iUbA/VPwB0RZ/Wx" +
                "EEvVWLIC0taiVK1VpU9FCxoniTWnv14J+gttUWjOJJPFc868lx" +
                "drPZzWyTdmebNLvb92AyP3Y37Mwn3zfzZrOcaxY5zzk2cMOgj9" +
                "sMBngegzt8wS2x0clZUOXu2yums6XYLx4Iw0ZICr6PxJi0zjJe" +
                "S2x1LNf9JjYTXyW+zTrfF37h68yIrzkSLZDEPXDEN/ptIb5K+t" +
                "0Pych0eNA3fI8TX1WL/Nb44oECYzUBk5CGcVEaM9s+OhjjqZLw" +
                "PU18lfR7UJt/o/2+0e854qtIuJVBdITFnPFlzTJfNuMq1sEuS/" +
                "UeIw9JrcvYcqm+XqrVse1WLTosWnar9Cn6xnZPS/OdxdbonzVs" +
                "U36+bC/bZ557mJkzP2vL813tUi1l5JIOWMLIrdFcp9IrdkgksV" +
                "Jihg9jZ0W6YDvnCusUn91iDEbZdUH3qOjVpEjpii+ljX+j6aLo" +
                "t8PRSFwl/Ua/Gvo9xnlll2/8cy/5Z3WLjUke+4Qz/QY5/g2KwW" +
                "psw5Oxb5yHBs22esH4lFmrg514JjQEtUZ9K2yAFSJfBTWwTeSN" +
                "IhkzGDTASj2vDj2BXSJfO48720F8FXu9xeGKq70U+g09WLiezn" +
                "/+Ve7dfa/RDj3M6Ff7jH+XaNZLNaFfaMJLavrFi6Rfr+g3Ex8V" +
                "5itSk0i12fgoly9rxU6ZrxYfQbUoG3zdxkcW33LER/oRn8VHGl" +
                "97fJTRr+13YOOLXRZfp/q1+LqNj+aj34X3z97TbyY+yvLF7tn9" +
                "sypf7Cmnf3Zri2N9ZbvK8M85LTpfket8pZXZNUu/ek58A8EXe+" +
                "f2z8TX23zxRmG+TuZf4lsOvpiae32Vu362+Kqur4ivN/WLN53P" +
                "v6KN5l9P8XUSH8n6xT7Sb6D0e5v0Gyy+eHfGlQN8UVhQnx9pJd" +
                "vzo/7c/Q2/PT8i/arMv672rx4TX3/Mv7rHfkr+2bf++Tk+m0u/" +
                "+FJZv0OkX2/4Z3yFI/i66P55mPh6wz/jqHE0QO8fkX826b7Lls" +
                "IF/vMAE+G42/cXwpXEt+zz7xR+wDEUxPAzvseJIvnncfLPRVbi" +
                "J3X/jG/xp3nU3NWw/t8Oqdn2N8rxfjfptyS/nT8UH/nVP2dLtv" +
                "2rv7J/pv0rn8ZH9wyF/sv6Z7hF/jmI+i1u/BupJP16Y38jEi8F" +
                "3/AP4uuN/Q0nFqnii8KC55/Zf0b9UA4=");
            
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
            final int compressedBytes = 496;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt1KAlEQx2PSnWvrIogKCyKSsIQoqLzp2yK6Ceqqh+imNy" +
                "ghosjAjKDv6OMqeopSqgufopfYjssW2qqpaO2Z/Q8ss+fsDsL8" +
                "+M85c1YKmg6j4YJRhEYpqvyAPR6kPmpXvpP6aUj5CXWF7Gfj1G" +
                "H5Lg7QmPI9Zs1GI+afW/OHqbFRuLL3uMURGTc9YXrzLco8yK2N" +
                "0K/vAfp1g36pUfW5DXxF8+0GX9F8e3Xkq/laG64pKqKuaMGMxV" +
                "d5i2/Bziz0xdd+E3z/mS9P+nd/j/TvIXva7p+nGlGf/SnUZ1fo" +
                "dxr69Qj9R54p+ewJ+dG2Pi9yjOd5jheMV541MvWpz0Ya9dld+2" +
                "deKhMJ/Yo2XkYONCW3TjFbo+X0G/gxNpA5XdbfInP1OL/awPrr" +
                "svV3s0RUht4oS2l19/I991zBr70j41pU8C3FKmsRq4ovTBO+2+" +
                "ArvrJbfDkOvhrrdAfrrye1mzvRyvI+6rMYJR84GMeRFV37X2PV" +
                "WMnd+RL5/S8n8vtfY82XrPL/dYfof93Q/3IS51eia/ER+Irmmw" +
                "Jf0XyPwVc03xPwFc33FHxF8z0DX9n9b5G5enz/PUf/64r+9wL6" +
                "FV2fL8HXI6SvSrDF9yOdqV5Dv6L53oCvaL634Cua7x34iuZ7D7" +
                "6SrekTW2Iwhw==");
            
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
            final int compressedBytes = 545;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrlLA0EUxmWymXmFFrbiEWxE8UhUFDxKTSKKKKKFir0gaK" +
                "di8MDCwmChYGNlY2GhYOGBhWjhnUb/AG1FBDu7dUiCGLMeSzYw" +
                "O34PwuxudkN4P77vvXmJ2GZhMx6sy/w2WP6Xc24iNAqxgxxoRH" +
                "M3Tb9PyIo7g/nEvgiLkAiKDh4T7fxGXqtPuSPAGlmrXCuT5zWs" +
                "jBXItYiVM79cW+SrIvleMyuMr8X8mjXJtTSDb9YAOjYzVm3J94" +
                "Bf8Ct+aZo8xs/BV9Xgt/b58j3J9zDtPgf4esPgq4Z+La45wFcc" +
                "ga8i/nwM/WrN9wT9FfzZtj+fgq8i+j1D/6xz/5wl/T6Ar9b+/A" +
                "i+WvN9BV+t+b6Br9Z8TfBVjS/lOsfXiICvCnytgvLSnlxC9lzK" +
                "3EdVmF+h/trlSzXgqwZfCkhHro1PO2Lkh35VDaqzzzcxv6I25/" +
                "Xr2QBfrf05CL4q8KWQN/r7k94VZM+1/XMf9VL/z/qlAdv67YF+" +
                "lfDnThamIdat1//bWZ7ruqfBLH72sGl67j13H9lZ+Nt8gy1CM6" +
                "7sxEf+xhfhUr6j4KtPf5Wl/dEY+itV9r80Luvvs8PzjRfwVYGv" +
                "pT9PwJ812v9OJ46M1c/6palU/RrrNv15EvpVVb8Wip5B9tzeX3" +
                "3RbyQz/Rpr0K8K+qVZmkvyLcF849/uiOeRA635on92cf2lKN9y" +
                "fL6xjPqrRP3dxO+/OkfOOya9O+o=");
            
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
            final int compressedBytes = 603;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm89rE0EUx8MwfTtzKG09ipbQS6kU24JWqAY89RcpXgQ9eR" +
                "Bsj6VgL0XvvfVQQZBKFGpa2mNPpcWrNWr+An+cRPxFL6VURLfD" +
                "sqy7+bFJZANvlu+D8HYmGULeZ77vzQwTkVW7tEkbbsjEpUhrRF" +
                "wROeMH/faQ6BdnjT8vBsSw8dfM64L/3lVxzvO9VBRjxve5/21i" +
                "1IW1FrGLNfryYkrtixvmaSZmZE9Fm5j/0k7b2Ki9NkUiW6MvAf" +
                "2ql9AvB/02ObsOED1LmQf6lath/apSVL/ycWv6lY+gXxb1tx7f" +
                "Mvimg6/6EGTh98nVX/kQfJnw/dSW9dVH8OWVnxPm+xl8efFVX+" +
                "L4ov5CvxH9fgXfVPP9Br6s90ffkZ9Tsn7+0Q79ym3w5cBXrgXr" +
                "q59BXyHyiRfyqdwy/rk6DPqeNP42WTSvdUSdu6kjMzNykXni6d" +
                "d4T78Vc8jXr9+CftO6vjpGfubAt4lRI83rV51Av7bxVb+Qn5Gf" +
                "q/Lzb+RnO/TrafgPomcp83rnG39xvoH8HJOfXfDlxVdn4vhCv2" +
                "muv5oQPVvrL92im9X1Vzth/dJt6Bf1N8xXd4EvN766Ozm+uF+X" +
                "cv2eAV+u6yt9uWrkMqJn+/lGwvq9Dv3yyM8670w5k86EM01lZ5" +
                "zeJvT/0Dfgy4TvDB1QiV67LpXpFfhyNXrXOl/aqXf+rO9E+WL/" +
                "a6d+O5b03cYjOx4gemk2fQ8xsNP+3Z+Uz/Ss/1QwROeC/uD+pN" +
                "wM+nB/0h5tzjeRn1cQJ+x/I/vfBayvmOyPFtvC9z74cuCbOQWH" +
                "GdKk");
            
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
            final int compressedBytes = 551;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmzssBEEYx5nb291va1Gccy4i8eY4IfFovToKNEqlXkEjCo" +
                "0oSK5SEJ1SIySIQiS4xqvzqBRaBCFrXY5Ye49sDJmZ/CfZzO7s" +
                "zuX2++X/PWbuaCI4Z+dtwXkbTcrGojTpGWtxXTWxNtbl9HXp60" +
                "ZWyUJOX8qqWczpO52jJn2vg4VTfSS4ytqdvvwX36wVdHxarCET" +
                "3wxjHPjSFPiKwZeWjT6j1+gx+vWk0a0f8eGrH4KvIHxXbNts/j" +
                "jXk2YMfEVtZtw/X309NXPUtgOngZOv56Y9M2czft6MKO9OG+Cf" +
                "Lb/6PNMWvsdf2nTrV0v406+2CP2Kkl/RVkrFt9nzK30EfOXkm/" +
                "Ju23n93w6sJ7t/5lwf7UK/YtW/P+LvHuIv1jey8zXH/5Mv7YOu" +
                "T/0eQL+q5FdpDRzLW/+i/XL9IAkbKM33DDaQtT6i8/z5lb7ke/" +
                "05gfircv5MF+ArDl+64s73EnwF4nvDne81+IpVH+XMr+5lfN/C" +
                "MTD/GX/pkZN+H6BfMfwzvXyj+8TNPz+Dr2j5M73RK36fo3J9RD" +
                "b0qxJfumNRi5mD5lDu/Nkc9r1/NAC+8uTPVhGsJ2v+bBV7xnj8" +
                "fwHrGwLGX358rRD4isXXvb9vlbj5Yn9fVr5W+C/0q62Br9L+OQ" +
                "K+SvMtA1+l+VaAr6j1r1XpmTkL68la//6RfqugX6X9cy34Ks23" +
                "DnxFjb8ZInI9rIf469JvHPoVQb8F74Wwt5w=");
            
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
            final int compressedBytes = 518;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7tKA1EQhuWQHHbnEcQbNqKIRhCFGAsLH0AELSytfAEtbE" +
                "QsRBQLBd9A7cQLaGEjCBpNfASNggoWWplO1kXikk2WmCUnYXb8" +
                "B8LJZRfCfPn/mTm7cZzyoMHSd9SqgxAcNIQcRDNUR8B7Pv2qAT" +
                "WsRt21t/C6X3WpZndtVd0q4a4p99FT+GxEtfysbZRUSXftrOGb" +
                "4TcVNmN9QXwpVQ++8Xvw5cCXxuIbf58Z30T2ZPiznTejX/sT+u" +
                "Wg3yr7q3FkL6r6pUm9r/dM11+9C/3y6K9+n8W2ivnSjJ9vbCcc" +
                "39g2+PLia3g+mgVfJvPRnL7WNzrtOPpOX+mMIX++BV+zobPh+e" +
                "rjoh5q3jtuuezMwP1JtYKsywhaQA5kzL/G6u8i/Fl0f7UEvlz3" +
                "N2ituvqLiGi1XQdfqfUX+89y66/9ZSUw/8qvvwGOfYDsRdef6d" +
                "Caqtw/W9Nh9WtNQL98/JmOTM9H4Mut/pZcPzrx88X1I0Hz7ynm" +
                "I3nzUYl+z6Bf0f58Dr5i/fkC/izPnw33z8/QLy9/tl4r8Q3rz3" +
                "QJvhz4Utqrv+3ecbh/Q5g/2w+m/Zkauv9MGZCs5M/2o2m+dq6h" +
                "fLOgW9P1hRyyh/7Z589P6K94+DO91GU+wv9Dmc1HhvX7Br6i+b" +
                "6DL9f+ij6qm38R/7i/wv11sv05D74c+DZ9A4w+Xqk=");
            
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
            final int compressedBytes = 449;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms9KAlEUxuUyOup9gQgziSCKsIIy6M8uKlu0iBYFbWpb0C" +
                "ron9CmEloWtOkFQnqSokfoLQpqY0O56KoQg06cOf0uyHVGBuT8" +
                "+M73nZmp11tX9r35jKnWWbFcptDm3KRzNGGmzXywjzaOx8yQ6Q" +
                "32PjNsxoN9LviMNH6bNbmvPZ/9MDPBPtDBPytBJ2TFin/H1ybg" +
                "K4uvd/2Tr+1x+Xq34fh6N/CVwLfdsjn8V4//2rxf9pf9JX8l9e" +
                "wvpp66059Tj+hXRn+2/VH4b7IMX9X5qgBfIfodpD+j39D6LcJX" +
                "bH6eIj9rXrYEX0Xz0QL++7/6cxtFr1K9uOo3ony1hn7F5qt1/B" +
                "f9/qLfDfSrev7dhK/Y/rxFf1Y0H21/f3Of/6aPXP2Gff6bPkS/" +
                "MZqPdqge+crx3130Ky1f2b3u8fUq8JWYnzNv3eGbeYVvjPx3n+" +
                "rhv47/HqBf1fc3juGrmu8JfGXxbXq//bSz+xu83y6Fr61EoV+v" +
                "Bt8Y5eczqkd+dvz3HP2qzlcX8BWdry7JV0ryVZV8pZrvVRR8ky" +
                "/wlZqf7V3Llby/oXrZe2qgmm+NGjD/OvPRA/4rwX8Tn/OTMPY=");
            
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
            final int compressedBytes = 389;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2EtKA0EQxvFQzGKgZxdREQ2SjSjiA4KCj5VrFRXMBVyoO8" +
                "+i4CE8gxdwJaIrH+Br4eMGLsZGgqRVQkYyUFPzbwjNTHb146vq" +
                "aRlNfy1pBE+zMi/Lfp9sPU/LmAz5fUTGZcbvS/430fpvUYa/9p" +
                "o7lQW/19N/L5lLWdkqNvXHu2/f6LDd152FvtFxNt/oCF/Vvuf4" +
                "2vLtbX+On/HV4NvdchdUr6DmeZ2vLsmv5f7srvDV4euu4814u7" +
                "Nv3Mw8fzfwNZ3fG3xN+97ia9r3Dl/Tvvf4mvZ9wFeX74/7ycfQ" +
                "l/vJwn4fPeWR3+gEXw2+3S33QvUKap7X/H0lv6rn7xvz18j8fW" +
                "f+ljC/H+S3ROcrimd6JRVqwPm5vT8nVfqz5vmb9DF/bfn2OL/9" +
                "+Jr2HcBXdX8epD+T3w75reOrOr8N8lue+41kherx/Rv051Xya3" +
                "r+ruFr2ncdX9O+W/ia9m3iW6Dz8w7V4/wc5HeX/Jruz3v4mvbd" +
                "x9e07wG+GnwrnxuxTHE=");
            
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
            final int compressedBytes = 114;
            final int uncompressedBytes = 3361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjUviPAZhMUHiGTOZMtkBaB8rXZ1JnkgLSskyaTAZA2gaIta" +
                "By1kwyYFqOp4TJCkgr/ScbMJn9HwWkhZgeFjFaxW/paPwO6/it" +
                "GI3fYR2/raPxO6zjt200fod1/HaMxu+wjt/u0fgdDPHLAAA6Wv" +
                "89");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 109;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3cEJACEMBMD0X7NgOvDlkjxm/oKcywbkwCoAAAAAAAAAAI" +
                "Cd7tOJr5/eP9nv73z0BwD6G/n6mS/5Bf0B8gcA5isAAAAAbOd+" +
                "DEA/AoD5DQCY/wAAAAAAAAAAAAAAkJN+f9H/9cMauFJKmQ==");
            
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
            final int compressedBytes = 72;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt1LEJAAAIBLHff2ZBNxBrSUa44hIAAAAAAAAAAAAAAAAAAA" +
                "AAAIBfelUCAfgvAAAAAAAAAAAAAAAAAAAAAADAzQDTCj29");
            
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
            final int rows = 787;
            final int cols = 8;
            final int compressedBytes = 58;
            final int uncompressedBytes = 25185;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt08EJAAAIxLDbf2ZBN/AtmIxQaAIAAMAHvSqBwP8AAAAAAA" +
                "AAAAAAAAAAAAAAAAAAwGUDisg9vQ==");
            
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
            final int rows = 23;
            final int cols = 123;
            final int compressedBytes = 125;
            final int uncompressedBytes = 11317;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmMkRgCAQwPDsDOvapr3wLMGvmkw6SAZ2IVI8kEskYTC0Oq" +
                "CQJx1gWi86wLSedYCZ1zrgtG50gLnDTx1gWu86cDcTW8tnd7NO" +
                "B5jWtQ4wrXsdYOb1qANM60MHmNabDjCtVx1gWl868C9Ffvfmqn" +
                "TguRZbyztJNznEGAY=");
            
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
