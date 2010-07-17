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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 1, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 18, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 109, 189, 190, 0, 191, 192, 102, 36, 30, 1, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 58, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 3, 58, 1, 8, 123, 4, 124, 15, 127, 5, 221, 237, 125, 6, 128, 7, 126, 0, 173, 238, 206, 8, 212, 214, 239, 215, 88, 30, 9, 216, 217, 219, 218, 102, 30, 114, 10, 220, 11, 240, 222, 12, 227, 13, 0, 14, 228, 2, 129, 230, 150, 231, 241, 242, 15, 16, 243, 30, 244, 245, 17, 246, 247, 29, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 137, 259, 260, 261, 262, 263, 22, 23, 264, 265, 24, 266, 267, 3, 25, 268, 269, 270, 26, 27, 152, 154, 28, 243, 271, 272, 237, 241, 273, 274, 4, 31, 275, 39, 29, 39, 244, 276, 277, 278, 0, 88, 39, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 56, 290, 30, 291, 292, 156, 6, 293, 294, 295, 245, 296, 297, 298, 238, 299, 300, 103, 301, 7, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 30, 39, 312, 32, 313, 314, 33, 315, 5, 316, 317, 318, 319, 34, 0, 1, 2, 320, 321, 322, 30, 35, 323, 239, 324, 144, 325, 326, 327, 58, 8, 328, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 329, 255, 256, 330, 242, 9, 173, 10, 331, 332, 36, 333, 88, 334, 257, 335, 336, 337, 258, 180, 250, 259, 338, 339, 340, 263, 265, 341, 342, 102, 343, 344, 345, 346, 347, 348, 11, 37, 38, 349, 12, 13, 14, 15, 0, 350, 351, 16, 17, 18, 19, 20, 352, 353, 0, 354, 355, 21, 22, 23, 24, 40, 41, 26, 28, 33, 356, 357, 34, 32, 36, 37, 358, 359, 360, 38, 42, 43, 44, 361, 45, 46, 47, 48, 49, 50, 51, 362, 363, 52, 53, 364, 54, 365, 366, 55, 56, 57, 39, 59, 43, 35, 267, 44, 45, 60, 367, 368, 369, 61, 62, 370, 63, 64, 371, 65, 66, 67, 68, 372, 69, 70, 71, 72, 373, 374, 375, 73, 74, 75, 376, 76, 77, 78, 79, 80, 1, 377, 378, 379, 380, 381, 81, 82, 2, 83, 84, 85, 382, 86, 3, 87, 383, 88, 89, 90, 0, 384, 385, 91, 92, 4, 46, 386, 93, 94, 387, 6, 95, 388, 3, 389, 4, 47, 96, 97, 390, 5, 391, 6, 98, 392, 99, 393, 394, 100, 101, 7, 395, 104, 105, 396, 48, 49, 106, 8, 397, 107, 108, 398, 109, 399, 400, 1, 401, 402, 403, 404, 405, 406, 123, 110, 111, 407, 112, 408, 113, 9, 114, 50, 116, 10, 117, 0, 118, 8, 11, 119, 409, 120, 121, 12, 122, 123, 1, 124, 126, 127, 13, 129, 14, 0, 128, 410, 130, 131, 132, 133, 134, 411, 135, 136, 412, 137, 138, 139, 140, 413, 141, 414, 415, 416, 142, 15, 417, 418, 419, 420, 421, 422, 423, 143, 145, 424, 146, 425, 147, 18, 148, 181, 426, 427, 8, 428, 149, 150, 19, 151, 152, 429, 143, 430, 431, 153, 154, 155, 25, 156, 157, 20, 15, 158, 159, 432, 21, 433, 434, 435, 160, 436, 437, 438, 439, 161, 162, 0, 51, 163, 164, 165, 166, 167, 440, 168, 22, 441, 442, 443, 444, 169, 52, 170, 116, 171, 172, 173, 445, 446, 447, 174, 175, 176, 177, 23, 8, 178, 448, 449, 450, 451, 452, 453, 102, 454, 455, 179, 456, 457, 180, 53, 458, 459, 181, 460, 461, 462, 463, 464, 182, 465, 466, 251, 467, 468, 184, 183, 185, 469, 470, 471, 472, 473, 186, 474, 475, 187, 476, 477, 478, 479, 188, 480, 2, 481, 482, 56, 189, 483, 484, 485, 486, 487, 488, 190, 489, 490, 491, 492, 191, 192, 493, 494, 495, 102, 194, 496, 497, 193, 498, 195, 499, 500, 501, 502, 15, 260, 27, 503, 196, 504, 261, 505, 262, 506, 268, 507, 270, 18, 197, 198, 199, 200, 24, 201, 508, 509, 510, 202, 511, 271, 512, 513, 514, 15, 275, 515, 7, 8, 58, 9, 10, 516, 11, 517, 518, 519, 16, 148, 520, 18, 203, 521, 276, 522, 59, 0, 3, 523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 533, 534, 30, 535, 168, 536, 537, 538, 32, 174, 539, 540, 541, 179, 542, 35, 543, 39, 17, 544, 545, 546, 204, 205, 547, 207, 548, 208, 549, 209, 550, 551, 210, 552, 553, 211, 554, 213, 60, 555, 556, 557, 558, 559, 560, 561, 63, 64, 65, 66, 214, 562, 563, 564, 73, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578, 579, 580, 581, 582, 583, 584, 221, 585, 586, 587, 588, 223, 589, 590, 591, 224, 592, 593, 1, 594, 279, 3, 595, 284, 74, 75, 76, 88, 596, 4, 54, 597, 102, 103, 598, 215, 599, 600, 206, 601, 602, 603, 604, 5, 605, 6, 606, 12, 14, 607, 608, 609, 610, 29, 611, 612, 613, 217, 614, 615, 219, 220, 616, 104, 617, 618, 619, 620, 621, 622, 222, 225, 623, 226, 624, 182, 625, 227, 15, 626, 627, 628, 629, 630, 105, 107, 631, 632, 633, 108, 634, 109, 114, 115, 116, 228, 635, 122, 636, 637, 2, 638, 123, 125, 130, 639, 640, 229, 641, 642, 139, 141, 143, 144, 145, 55, 643, 644, 645, 232, 61, 20, 646, 647, 648, 146, 7, 21, 22, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662, 663, 159, 4, 664, 665, 666, 160, 161, 158, 667, 162, 231, 56, 169, 170, 172, 173, 668, 178, 179, 180, 669, 181, 182, 183, 670, 6, 184, 185, 186, 233, 234, 60, 235, 236, 671, 184, 61, 62, 67, 68, 69, 672, 673, 8, 9, 674, 675, 676, 677, 678, 679, 680, 681, 682, 683, 30, 39, 40, 684, 685, 686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 207, 696, 697, 698, 699, 700, 701, 702, 703, 704, 187, 705, 188, 706, 707, 708, 189, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 731, 732, 733, 24, 25, 27, 62, 734, 735, 736, 737, 738, 190, 739, 191, 740, 192, 210, 193, 741, 240, 742, 244, 743, 744, 194, 745, 63, 746, 747, 748, 749, 750, 225, 751, 195, 752, 753, 754, 755, 756, 757, 758, 759, 760, 196, 761, 762, 763, 764, 201, 765, 766, 767, 768, 769, 10, 770, 771, 772, 773, 774, 775, 776, 777, 70, 7, 202, 203, 778, 779, 780, 781, 782, 783, 784, 785, 786, 212, 64, 215, 217, 787, 225, 226, 238, 1, 230, 71, 233, 234, 235, 236, 237, 72, 239, 242, 247, 250, 251, 254, 256, 257, 788, 289, 246, 789, 790, 0, 791, 36, 58, 792, 793, 794, 264, 265, 73, 268, 269, 74, 290, 795, 65, 796, 218, 220, 222, 228, 231, 243, 248, 797, 249, 241, 798, 245, 799, 800, 801, 802, 803, 59, 253, 75, 804, 805, 258, 259, 8, 806, 293, 807, 267, 270, 79, 808, 296, 809, 271, 272, 273, 274, 810, 811, 300, 812, 247, 813, 275, 277, 278, 814, 815, 248, 249, 816, 250, 817, 818, 251, 819, 820, 821, 822, 252, 823, 824, 66, 253, 254, 825, 826, 258, 259, 827, 828, 829, 830, 260, 831, 261, 832, 833, 834, 67, 262, 835, 263, 836, 837, 838, 839, 80, 279, 280, 840, 81, 36, 73, 82, 83, 77, 78, 84, 85, 79, 841, 80, 281, 282, 283, 842, 843, 264, 844, 284, 265, 845, 846, 847, 267, 848, 58, 88, 60, 285, 286, 62, 302, 102, 269, 849, 63, 850, 270, 851, 64, 287, 288, 2, 65, 289, 86, 290, 291, 66, 292, 852, 309, 294, 853, 854, 1, 855, 311, 856, 857, 293, 86, 271, 858, 859, 295, 296, 297, 272, 860, 861, 273, 862, 863, 274, 864, 865, 276, 87, 298, 299, 300, 87, 301, 302, 0, 277, 303, 304, 866, 305, 306, 307, 283, 867, 868, 869, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320, 1, 870, 321, 322, 323, 324, 325, 326, 871, 327, 872, 873, 328, 329, 874, 875, 330, 333, 876, 335, 332, 334, 336, 337, 338, 877, 339, 67, 89, 340, 341, 342, 344, 347, 878, 348, 345, 349, 356, 343, 346, 879, 358, 360, 367, 370, 352, 371, 372, 375, 376, 379, 378, 381, 880, 382, 357, 90, 91, 92, 93, 94, 95, 99, 100, 101, 102, 106, 110, 111, 112, 881, 278, 0, 882, 359, 377, 883, 884, 113, 885, 361, 886, 887, 888, 280, 281, 380, 362, 282, 363, 312, 364, 383, 889, 890, 365, 369, 384, 387, 284, 891, 374, 366, 386, 388, 390, 392, 109, 393, 68, 394, 892, 395, 396, 398, 400, 401, 893, 399, 894, 895, 896, 287, 402, 403, 404, 405, 897, 898, 899, 406, 900, 407, 408, 69, 409, 117, 410, 411, 412, 413, 414, 118, 119, 901, 416, 902, 903, 288, 424, 425, 904, 426, 421, 427, 428, 2, 905, 906, 430, 431, 432, 433, 89, 434, 907, 436, 435, 437, 438, 440, 441, 442, 90, 443, 444, 314, 445, 446, 319, 447, 908, 448, 417, 418, 909, 910, 419, 911, 912, 913, 914, 915, 450, 455, 11, 916, 917, 456, 457, 120, 121, 124, 458, 91, 918, 919, 920, 92, 291, 292, 921, 922, 460, 420, 923, 924, 3, 925, 926, 927, 928, 93, 929, 126, 930, 931, 932, 449, 933, 4, 934, 935, 451, 936, 937, 94, 6, 938, 939, 940, 127, 941, 942, 943, 944, 298, 95, 97, 945, 946, 947, 299, 948, 461, 462, 464, 452, 453, 454, 465, 70, 0, 459, 1, 463, 2, 466, 467, 71, 469, 99, 2, 72, 470, 473, 468, 471, 472, 474, 129, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492, 3, 301, 493, 494, 495, 496, 497, 498, 499, 500, 501, 503, 505, 506, 507, 508, 509, 511, 305, 510, 303, 512, 513, 516, 949, 139, 519, 520, 521, 4, 304, 514, 515, 523, 517, 5, 524, 950, 525, 518, 306, 307, 526, 527, 528, 529, 530, 531, 532, 6, 313, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543, 545, 546, 522, 951, 952, 544, 547, 953, 954, 955, 315, 548, 549, 3, 141, 142, 550, 956, 552, 957, 958, 959, 1, 4, 554, 558, 143, 100, 12, 551, 553, 555, 960, 556, 557, 114, 73, 961, 962, 559, 560, 561, 963, 316, 964, 965, 317, 579, 966, 318, 7, 967, 968, 320, 969, 970, 971, 584, 144, 562, 564, 972, 563, 565, 973, 321, 974, 570, 322, 975, 572, 976, 323, 324, 585, 566, 567, 977, 978, 979, 980, 592, 981, 982, 983, 326, 984, 985, 145, 986, 0, 987, 988, 989, 330, 990, 991, 992, 993, 994, 995, 147, 101, 102, 103, 148, 150, 152, 996, 153, 154, 155, 156, 997, 998, 104, 999, 1000, 74, 1001, 1002, 325, 1003, 594, 568, 569, 571, 573, 574, 575, 327, 1004, 157, 1005, 1006, 123, 75, 1007, 76, 1008, 5, 576, 595, 77, 163, 581, 582, 105, 577, 578, 124, 580, 1009, 1010, 331, 1011, 331, 1012, 1013, 588, 1014, 597, 589, 1015, 596, 1016, 1017, 343, 106, 1018, 107, 598, 599, 601, 602, 603, 604, 605, 1019, 1020, 607, 608, 609, 1021, 583, 1022, 610, 1023, 1024, 611, 1025, 612, 1026, 613, 1027, 614, 164, 1028, 1029, 586, 616, 1030, 617, 587, 590, 591, 593, 615, 1031, 1032, 1033, 618, 600, 6, 7, 619, 620, 1034, 621, 622, 337, 1035, 1036, 1037, 350, 623, 1038, 351, 1039, 338, 1040, 624, 625, 1041, 1042, 108, 626, 627, 628, 634, 636, 637, 2, 1043, 1044, 1045, 125, 78, 629, 79, 631, 1046, 357, 638, 1047, 1048, 1049, 1050, 359, 639, 1051, 1052, 1053, 1054, 1055, 1056, 1057, 1058, 640, 641, 1059, 644, 1060, 645, 1061, 642, 362, 1062, 1063, 646, 647, 1064, 648, 1065, 1066, 165, 1067, 1, 1068, 1069, 649, 650, 651, 652, 653, 109, 9, 654, 655, 166, 363, 13, 1070, 656, 1071, 1072, 1073, 1074, 364, 1075, 365, 1076, 167, 168, 657, 80, 1077, 1078, 1079, 1080, 1081, 658, 1082, 659, 1083, 660, 368, 661, 369, 662, 1084, 663, 110, 1085, 1086, 10, 664, 670, 673, 675, 672, 1087, 1088, 674, 1089, 676, 665, 373, 666, 111, 1090, 1091, 11, 1092, 678, 668, 384, 1093, 387, 1094, 1095, 1096, 677, 171, 174, 1097, 175, 1098, 395, 1099, 396, 397, 1100, 1101, 81, 679, 1102, 1103, 1104, 0, 1105, 1106, 1107, 1108, 1109, 680, 1110, 1111, 1112, 112, 409, 1113, 1114, 1115, 681, 682, 683, 82, 684, 1116, 685, 686, 1117, 687, 1118, 1119, 688, 1120, 1121, 1122, 1123, 176, 689, 690, 1124, 1125, 691, 692, 1126, 0, 1127, 1128, 1129, 8, 177, 193, 693, 694, 1130, 695, 197, 696, 697, 1131, 698, 1132, 198, 199, 1133, 398, 332, 1134, 699, 1135, 700, 1136, 701, 1137, 1138, 702, 703, 704, 1139, 12, 1140, 410, 705, 200, 1141, 706, 1142, 707, 411, 708, 412, 413, 1143, 414, 709, 1144, 1145, 334, 710, 712, 1146, 1, 1147, 1148, 415, 1149, 1150, 113, 1151, 115, 1152, 416, 1153, 424, 1154, 83, 3, 4, 711, 713, 1155, 126, 84, 425, 1156, 426, 714, 1157, 9, 1158, 204, 715, 716, 1159, 717, 1160, 205, 344, 718, 719, 720, 721, 722, 723, 127, 724, 1161, 429, 725, 117, 1162, 118, 1163, 1164, 1165, 207, 1166, 726, 13, 1167, 728, 729, 730, 1168, 731, 14, 732, 1169, 733, 734, 15, 17, 18, 1170, 735, 736, 740, 1171, 737, 208, 742, 1172, 1173, 738, 739, 1174, 727, 431, 741, 743, 337, 745, 746, 1175, 1176, 1177, 747, 744, 748, 749, 2, 128, 85, 119, 750, 751, 752, 1178, 1179, 753, 1180, 430, 1181, 340, 120, 124, 0, 125, 126, 755, 754, 216, 86, 87, 756, 757, 88, 758, 225, 89, 759, 1182, 432, 760, 761, 762, 763, 764, 765, 766, 768, 1183, 770, 767, 1184, 769, 1185, 1186, 773, 127, 1187, 226, 187, 1188, 1189, 433, 771, 434, 1190, 772, 774, 1191, 129, 1192, 1193, 775, 1194, 19, 435, 130, 1195, 1196, 776, 777, 778, 8, 1197, 1198, 1199, 20, 436, 131, 1200, 779, 780, 1201, 227, 230, 233, 437, 2, 438, 440, 1202, 781, 1203, 1204, 782, 785, 90, 786, 234, 787, 788, 132, 789, 790, 791, 1205, 792, 793, 794, 441, 1206, 1207, 133, 1208, 1209, 1210, 1211, 795, 796, 1212, 797, 442, 1213, 1214, 1215, 235, 798, 799, 800, 346, 801, 236, 1216, 349, 802, 803, 1217, 443, 804, 805, 806, 353, 807, 9, 237, 808, 10, 11, 1218, 809, 810, 1219, 1220, 1221, 444, 1222, 445, 1223, 446, 1224, 1225, 447, 1226, 1227, 134, 1228, 135, 1229, 1230, 1231, 1232, 1233, 350, 238, 811, 1234, 351, 129, 466, 91, 354, 1235, 812, 813, 814, 815, 816, 817, 355, 819, 1236, 367, 820, 821, 1237, 130, 92, 818, 1238, 1239, 1240, 239, 242, 822, 823, 1241, 824, 825, 1242, 826, 827, 828, 1243, 1244, 829, 830, 831, 1245, 832, 833, 474, 10, 834, 11, 12, 1246, 1247, 835, 836, 838, 21, 22, 245, 837, 1248, 246, 1249, 93, 839, 1250, 840, 1251, 1252, 1253, 841, 1254, 842, 1255, 843, 1256, 844, 845, 846, 847, 848, 849, 475, 850, 1257, 136, 1258, 851, 13, 1259, 23, 855, 137, 1260, 1261, 1262, 1263, 1264, 476, 856, 14, 1265, 480, 138, 1266, 1267, 1268, 1269, 1270, 481, 857, 477, 1271, 482, 1272, 483, 1273, 1274, 484, 1275, 1276, 1277, 1278, 6, 14, 1279, 1280, 1281, 1282, 247, 1283, 852, 853, 854, 858, 1284, 859, 860, 368, 12, 250, 251, 1285, 862, 863, 13, 864, 370, 1286, 485, 486, 15, 1287, 17, 1288, 253, 1289, 1290, 487, 1291, 1292, 1293, 139, 142, 7, 8, 865, 866, 867, 861, 488, 873, 372, 1294, 1295, 489, 377, 1296, 1297, 380, 874, 14, 875, 254, 876, 1298, 94, 255, 256, 490, 491, 877, 880, 881, 1299, 883, 886, 887, 888, 889, 890, 893, 1300, 1301, 1302, 1303, 15, 894, 1304, 1305, 878, 879, 882, 1306, 1307, 373, 257, 262, 263, 1308, 895, 1309, 188, 1310, 1311, 24, 492, 1312, 1313, 1314, 1315, 493, 496, 884, 495, 1316, 1317, 896, 1318, 1319, 1320, 1321, 498, 499, 891, 501, 1322, 1323, 264, 190, 1324, 1325, 95, 897, 898, 1326, 0, 265, 899, 900, 502, 266, 1327, 904, 901, 908, 1328, 902, 1329, 1330, 906, 907, 909, 910, 911, 381, 1331, 1332, 913, 1333, 914, 1334, 503, 1335, 1336, 1337, 1338, 374, 385, 387, 1339, 96, 504, 505, 389, 915, 916, 918, 920, 922, 917, 923, 1340, 517, 18, 1341, 144, 145, 1342, 1343, 1344, 919, 924, 1345, 1346, 934, 1347, 925, 16, 938, 926, 928, 940, 1348, 927, 506, 1349, 1350, 929, 941, 942, 508, 1351, 1352, 518, 526, 930, 527, 1353, 1354, 146, 1355, 943, 528, 931, 529, 1356, 1357, 147, 1358, 530, 1359, 1360, 1361, 148, 933, 1362, 531, 945, 1363, 935, 392, 936, 532, 949, 950, 937, 939, 946, 533, 1364, 390, 391, 1365, 947, 393, 400, 948, 1366, 150, 152, 153, 1367, 1368, 951, 952, 953, 955, 957, 954, 960, 1369, 1370, 1371, 1372, 958, 1373, 959, 1374, 534, 1375, 1376, 156, 1377, 1378, 25, 1379, 157, 1380, 1381, 26, 194, 961, 1382, 2, 1, 1383, 962, 965, 964, 403, 397, 408, 415, 535, 536, 966, 417, 1384, 1385, 1386, 268, 269, 1387, 967, 969, 1388, 968, 1389, 971, 972, 973, 976, 977, 276, 1390, 1391, 27, 537, 1392, 1393, 28, 538, 1394, 1395, 285, 160, 981, 982, 974, 418, 1396, 419, 978, 286, 287, 288, 539, 540, 289, 290, 292, 983, 1397, 984, 1398, 985, 979, 541, 1399, 1400, 542, 543, 1401, 1402, 545, 986, 16, 987, 546, 550, 551, 552, 1403, 1404, 988, 989, 990, 293, 294, 1405, 560, 1406, 1407, 579, 1408, 291, 420, 1409, 1410, 1411, 991, 992, 1412, 1413, 994 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 6, 1, 21, 2, 22, 8, 23, 24, 25, 2, 2, 7, 26, 0, 27, 28, 29, 30, 29, 31, 8, 32, 33, 0, 34, 35, 36, 37, 38, 39, 40, 3, 6, 9, 41, 14, 35, 42, 43, 31, 44, 38, 45, 46, 47, 18, 48, 49, 50, 20, 1, 50, 51, 8, 52, 33, 53, 54, 36, 55, 41, 56, 57, 58, 59, 40, 60, 61, 0, 62, 63, 64, 2, 65, 3, 66, 67, 49, 45, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 40, 79, 80, 44, 55, 81, 45, 82, 83, 6, 84, 70, 85, 51, 86, 87, 88, 89, 49, 4, 90, 0, 91, 92, 2, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 71, 57, 68, 105, 106, 107, 5, 69, 108, 109, 72, 110, 73, 4, 90, 2, 80, 111, 112, 9, 113, 114, 3, 115, 67, 3, 74, 116, 117, 118, 119, 120, 6, 121, 122, 123, 124, 125, 126, 127, 2, 128, 6, 75, 11, 129, 130, 89, 95, 131, 132, 133, 99, 134, 106, 1, 105, 135, 136, 137, 138, 139, 0, 140, 141, 142, 143, 144, 145, 146, 147, 109, 148, 2, 111, 82, 149, 150, 151, 152, 1, 153, 3, 154, 155, 0, 156, 157, 158, 159, 160, 6, 3, 161, 162, 0, 163 };

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
            final int compressedBytes = 3279;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXb1vHccR37c6KicmgVeEPohUR8FWGECFnMZJQWgp27EcIA" +
                "hBiUCMgAiQIlHBKnBJAUtGCMhULFSofFAhwJ3/BMJQIbhSESOG" +
                "/prc13vvPnb3N7tzx8ecLUrEvN2dnZmdz5177z/4296tb+5de7" +
                "JydO/bn7zaP39w+Gv9/MsfrpvH3299tibe//Hu3q3Tp1/eWDk6" +
                "yOF7BXz7+R9+uH72+D0N/uHe7dPNP70o4J++2nvzyeFuOf/Z4x" +
                "+3NAH+wV2AH5rfj5/M/1NCTETxXM3/rO/cz38aIbRcScT4+2PO" +
                "Xz6ZcD5o/tnWG49qj+fhf/7zDz9bOdpMbwhx79vt45vnVzd3hU" +
                "k/zxfR4gLoMwT81unmo3L9K/n6nxyuteWzhKc3Cvlsws3jtzT8" +
                "ARzK/9/3b58e/Pk/H/3z4NUXr//y5sHhXz/91/5/i/HPcvjqqr" +
                "i5lqZCrBxpITMtPv5a6eefn03M7slWwd+RzxeX/oX8JEeb6y8K" +
                "+Zkc33xzdXNtSPn5xarM6fNoktMnP/85fX779X39fLugz7+3Er" +
                "H08z0Efe3yufuWpj9Gxg+en5HtB1P+K/lJW/Kz3ZIfoH9TkX8q" +
                "/1k/+S9K1v/WYnz7I5vM1o2ftWVY9vlnyyfk75L9myXDe48J8x" +
                "/Gh49MH7Z8MM8HWL+tH5KZf6ab8h/nn+yW8O8X8AObfeCf/7H9" +
                "H8wfL33G988Tu39d+d35J1bmDncZjDxR+c8i/hArgu9/1Px95L" +
                "L/48dPY8d3JP+xij8mZfzR8R9/WfvPRwfTuf9c7e/ZTP5OK/ym" +
                "FX53hvWPx9aPgH9aJlb+qe21gn9seF8+sqZ8oPGW+LTzTBJf/I" +
                "rgKD5F8O8qeBWfbJfxyRw+GQDOjZ+584eMn1bjH4bgR9EffvuG" +
                "/PuF/ZtW42UzvuDGx2h8BFy01h85Pp6Nd+vHi1nfNb4Jn1Zw2Y" +
                "Tb4vdW/MWM/9D8/49wzRuftfIjLv/ybOFf+PIPpX365qBF/+1K" +
                "Phb2zwPPpSBB+Uv7+a/XJ/E/HVm+WvNnzflRfoALb5/P1+X5fF" +
                "ifz2d6jT0+dH7RmF/E7U8MuT9rUKzp+Yfvmv5nx75OyvW117tB" +
                "+RmH0+MP6kOe1A/mxq9jw238b8VnF4+fCMOfpx9R/O+OT6u/3f" +
                "7Tbkj86cwvNOEx+tmCf+t0oPgHxd/1RHKRg0xaRw2NHxve2b/s" +
                "qoKI+UXI+tz8QnO8jf8N/1gs/OfSPr7cEoT4Usif+eILBCfkP1" +
                "jzr/7eub/S/8f5C17+owmfVvCWfx1sSoLhGhgZv31sG8hw+wXx" +
                "SyM2OlngLVO/fM6Pq0plc9p08Q/ZQsR0TH2qjUgK9VTBUyPWi7" +
                "83S4WV1v5FtpihpS0TuK0Sf1XgLyv8Xxb4q/xT5zX+sfQjsjKF" +
                "/PXSh4+/5snXfHxqd9WAfCH9tjgfqtxtuHzb5EPVH0wwfl36rn" +
                "fo65S/an68P78CaBm88ped+vDM7eOw5xuNN1b+Jvz5U47+TcbT" +
                "fyh+AfxF58uTAZ5Qzg86/+h8QvrO9Ldw6G8N+Z841pUU+lvhKl" +
                "5+SfN7HzXM/ET8BdSvsrpC0LUPGQ2+NPtogP6g6m9kv2LtG80+" +
                "EfQ7FCEv/aF+4dmvHC59/LX7d039Yls24Hxi+UHyzfR/tYU5jR" +
                "Rd6hpPsw89ONf/MlT9IwL1S5z8Bct/d35UHwXyTbj/m3Dqq9z6" +
                "JJKPDfWjzNVYckddlZPf5L8bMxGZylIxzURmSP5Dwz+m8V/TNV" +
                "Icfxbnn1tfpAl66tmu9p9SQN+x72eh+mQTbstfXBR+rvplST+S" +
                "TnfIJ1m+Hff3mPXNvv564og/HaKG5AfUDxF+zvy16s/fyi8S54" +
                "+oP/bvT/rGQ/4+9OovnB/X/vAv6Hzz63vc+mO3PhgxXoSN1xT/" +
                "DGepYv3vdnajF18G4x+oP2D9UvDsA5of+g/M/RELqB2pUWQ4v/" +
                "4YuL+e0+rPT0D95FYeijZeu/VXwvcv0HgD/T/tPafYfw6uv4nA" +
                "+p7w1R8t9Ulb7m8Gl5bxS4UPUJ/10j+Cf2H1J5i3jMmvNPIDXP" +
                "+P3P/RPB8p2T4x6psvO/cfqXDRqf+i+qvXf7WMR/Cg+jCq71rg" +
                "ognn16959eGmfChrfCJA/RHX7woJ28hH9PJXV7D8ofsltX9/RX" +
                "zkih8G8h9Afo6c/+r5Fxr4H9TxQc5OSH7UX/8j5/9kVH0pX4dH" +
                "n81qYC5/JjE53kaqtEgvTfJt/NRSHzLd+Q0xv+4Yn+qGcbOFOO" +
                "dM/jLpI961x9f4p938XM58W35uQ72t4GkNFxVcdOHCDhfiUWmh" +
                "S+qkhaIzM/uYLRxZOafvQv8Q9ROav6BP4stP+vFH++fij/Q7Gb" +
                "8O/6j8yeElfe7ke3Duz4P/bLzsjTcDyY/28pdOHxnP3ySWv4K/" +
                "f7R+jz6if75I+K/68XfJBzh/SL+Q6zcqrr4Ycb/hfkh9h7s+8q" +
                "+gfT5B+pHpX6QI0R2w/52w+nm3/ofqi6HwUPsK7+8g+0y133Fw" +
                "7N/NPi0X96sEvT85uP4bCB98fQPk2wTKP9P/JtS/vfV/3J/B0y" +
                "+j7589vx+O80Mj4w/vn+nx6a+8/Afx/U6dQ5PSFh+MLx8w/i7+" +
                "LzJR7fORmMrVGMy+hT5E+UDxqTXpq+jxJxoPyT7nv/DwP8H20c" +
                "vW0faH75eQ9F8SLR/pyP4XYCTq3+HaZ/r9lSH94+HyW0v3T6z3" +
                "70Lsc6f+R5a/6Ux5svK7uH5XMSJb6A9VoTX14yezRRXTiLQYdL" +
                "aoX+hL4x+w/adA+8E9P0YQH8PCPw3f/ySsIke3r0ZMpr86fpoP" +
                "+cqsmcmLqZqmv9vMEnNlCf4tgz9yFP9wbP+dIqiWz+vkguLD84" +
                "KsSVYahyY+WSBcxsHn8Z2jPgbX16KlX/vzd/0nheszKoA+iL7v" +
                "2uPn/iN1/zB+9dovaP9z/ZCvMN0QuX4Q17J8tjOTu4d607d+cH" +
                "wP+jvGhIuI8coNJ+VHLs5/ot2PHE+/xfenxMSXGSN/443fBqBP" +
                "3PzYPwf53xNxnPn92+slKClYlMcnWYHRtpoYdbJOs4+l/mrfr2" +
                "vtD8BdPvtQ+S8/f9JWe2VmeemNjX8B/gk3P8I9H3z7EQOX9PjH" +
                "yj85lP6E7iE7vg6EGyb8guMr9vuVufEx+36SDj14hPrZcPY9p8" +
                "8x537DgPUJyYQPk7/uWXOmf4TrOyD/E5yfCoOjQwz7h2L8O/r4" +
                "sc8n6m/D9OPKB/f+5nL5E9Ef1jqicDyZ/rP7093lHjL5d3cfvP" +
                "9tn9VfdqnOl6V/CdsP9P5IwvuRAuhjOOfPtj/l988o9aNm5NfN" +
                "n8NZ/Pjj91da3r9T3w9JCOPR+8fY79fk60fv/tj6c9D8yOs4+x" +
                "8uP5qEXyL4+ompH8f2nwjf7wPi+1qysgbRelT09lf552e+v4nw" +
                "/hJWf5dg9m/B/jDAP0J/2cdVfqhHrJPq1GhZk9yX//HBl1B/od" +
                "MX3f8b2z/l9pdx/VeUNUH40fv3PP3FiRu+oV6W91cyUd1fyeb3" +
                "V7LO/ei4/mV6/s4/Pvr9W9z10eEhv18vpH9m8aD7L/D+eEpN38" +
                "kA/APgMH8VY/8WD/7+Rer8dv6x80dk/y4F+W8iPPJ8kd8PGSr/" +
                "zPevjn4+yfqDSx+Uq0P19cH0V5f/3v4btnyy70/F5M/UcPjB8e" +
                "949s9J/yp/2utf6vdnsfKzrv4vz/3VlnDh/jm//+GXP9L9QcPM" +
                "/4fG3ypIv3ekY+Lwz7zwxuOCu8bT+/u69G/vD8Nd/YE27yBI/1" +
                "wSeLrk8ZcWTjwfvPON+kc98gmCX9gfPtD+4vYP+1/59PHCh7M/" +
                "Lvt2UsefSTl/Vs5fbL6IP09wfyq9fzYqP06fPzI+nv/bdb8JwC" +
                "n2J6HEb8IR/8D8GzG/RHm/mc2/C+zf7fP3OPWdv+78lvj09unT" +
                "r0r8vsjxe3D4j078KDnxH96/IcbXIfkHRD/b+6Xj4v9L9/3kXU" +
                "EaOT8xQP2Id/+Mfb+Vmd8bhL4NU9z5/hnu+KXfL4bfD+WuH66I" +
                "gfqPk+Xl79z3t6bz+Kvff6uK/ltHBGXVX274fP5O/mfW3+tenw" +
                "anrd/X34kYZn9sOK7vcfcvPP0DRP8jvj+B7b8x86tppeIzvZD/" +
                "Atms7E/UotcfHZ1fcpwv6L8pi9mk3D8fCq78Zhu+Hw3l9/3+He" +
                "zvdvo/RP+JK/+0/HAC5DdBoy9r/Qf2h7Pzhzz8Ltb/fR0MJ+gP" +
                "//ul6fWH+PtpMfyTWWdy6c2/9uGGln/t5Deulesri351ra/B6X" +
                "O9f7s1saV/ckrlj6N/dEqbH+cfePM762NTT36pRX80P4ArR3/p" +
                "NCj/LQg3QSLzR378oHxpEfSY0PHIf9QWgW9fwfV+fx/Eb8nwsf" +
                "1bpv87wP0GXv/N/wA3eC5z");
            
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
            final int compressedBytes = 2866;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2PXLcR56N556dLAlOHyBZSMYERbKHCcWOnEPxOsRy5CC" +
                "ycLCAuDLh0oSpwqQA84QrJlQoXKhcqDLjTn7AJUhxUqTFg6K/x" +
                "+9jb3bdL8jfkkLsnRQtBJ93s8A2Hw/kk54lahD8Y/kAoKcz8i7" +
                "UVV7ufEzEdkAH+yy/+fPfdR99+/vu9k3vP3n56d/bJ/Q+PTv/x" +
                "8+XHd15e//QwgGkBYUR4xPxrBwDSz+fvKwKv3WxH+Jsf/Xrypx" +
                "B88ZFuFpbG3zX9GG7d+smQ9NMW4P3EzXL/zCdv/i/kl6s/X37x" +
                "fos/qVv8a8/eavE/un/YnH7+82V7fHa9OVzwdyxfF4l+pv1w7p" +
                "8twJsl/997NPnnDx39f3t6938f3T/u+f/4zi8d/xHcYT80sC9x" +
                "cKxfwvsvk341G/iGZv/QV+D8Z92Dle/5cH3Eizn+vpt+sv49cP" +
                "uHgL+Yvo66HPsb+S+F4GT/B+FLqnzE0k+0n8njE+0fl/9c/2PX" +
                "8uHbH9z4o1nsoDT7FMYXKzs0bX+C8SH9wD69087vp2uXvuz8h7" +
                "efft3Nb/Af7jzfrv2GYatMkl+m/coQ36L8QBAu6xtCa1EJLWVv" +
                "gp7cbhWDtp1d22tXXdZygHfz6+BXx3DC88P6zWlfdPzypK9PmH" +
                "6vCbHAxNixIxfpgtTQhNm0tEw0/VuGv+r054bHyueGf9og/xb5" +
                "Hwn7Y2W71YMLYpolvuq9D3NO2+V+GFV1v226XzfiSFdWP7yaIb" +
                "+Y3X5cdPle+3U7wz29XJNWmX+p279tx+W9ZP8txv/TY3w7/K7O" +
                "pb/Z4/PwSfFT8PkNzL+Ex2fi4/3Pyu+VqT9oMlqZ/I9OgLs3EF" +
                "1+3IPz5Y9Mv4e/TPrf8eUX75z58w+6wPrIVPsIPpWi5R98H65+" +
                "IMzfyV+bhT+z373/6d5Ju75CXHt29ODKbH9yLGx9U3cTI+iv4v" +
                "oRxqdM+sD4CN7xT51Mrvb8q3r+HY74B+MrHv/Y+UGxW/5tjq+i" +
                "nv+HA3nlsL5Vib2TNv5t/dO/fvfB0enR48oef39dYTiSn9Y+ff" +
                "3uT/dG9B8N/H3ppj8zPpe/tVBrWkzLqPRKjHz+2Mvfjbn8/Zui" +
                "PxB9YP/A55P93zkDvGFLmn3676AfnPq1cul/SvxRudaFmH+xTH" +
                "h8ek6WfH6afRUE+5pn/Wer6++wDy75TY8ffiwQ34T1F4YXjo8W" +
                "+YmNyTxc3S3xkT41P5iWP1x7vFyWQ5VeE81q/Ulz78+OfqsT6w" +
                "uAPhwfQvkpFF9y4BS2XOT8aAR9u89PWm5+MvTh5s/S5DOm/lHa" +
                "v+fqpxR4TP6mcPzIjK/4+YOGsNcpafFS+dPhYzbk02TLzzS9kT" +
                "WbKG9R+IPGt0H4wYFo47uWG3snjZBtfPeX73RzerOL7x5e30b+" +
                "aTQ/s/ldWN8N129hfbf4/k/NT9HWD8PFBccHcHPuhBqrbLduUt" +
                "Ta1L1X95vV/EM92t+vzfzrGW9+iD+TAbESk4G/VuoRf7nyD84n" +
                "JeTXE5/v+7zIYh988D/qX2S7v9WfWuVTfdz+39pWlDsGT00r0g" +
                "T8Jz2+Efuywzfr+GB+pOcrin8sPeOfjccXw/hiPv6u4cg/gfji" +
                "Vs+fzjXuhqh6vvTxgVnI3wguRnA8PsHnUen1CTr/ZCr/e/p8+E" +
                "KcBfnH5X8G+sHzG9bzs+zPaP0j3sBfFXiNUCPjo83xifmLxPgp" +
                "+f7UyvMD9TW2/w4Ipd8v8X1ux8Ft7vw5ej5VfyBgrH9iev3F52" +
                "/p+JOX3yflT1TJ+HIG6J8lzG/En2rlRzOPx1YSpK78eUz+PsV/" +
                "jzi/UWT96XDu+ZXS9w/Szi/orcG5+V1u/rfp6o//MXW7ha+Zyl" +
                "6Z7dcd0s3+G1nyx9T6RMH7A5zzUZnhVYL/VLvyC2p9htJX3+n+" +
                "qI37j8oOrv4CfrAC10s4LT+rAH+98K4+8N6jb7/q5ffvrfx+cv" +
                "9fjvpKYPzC+xfn5xz2US/tI8w/uTIU2ulBFYSH/bcQPr4f1N+P" +
                "Xehpu5P57ZJ/4/lv4iP4WB3M968pRH/0+NT4Xw/+c3z876qPje" +
                "IzG9VfxwJbFT0+yD8X7w/SgPVtEuQjhr4GMDSD/6BC+dl5fKaH" +
                "/NIQn4lFfIb9B/R8ff7Dff4P1ocBf6D+BM93xl8R/u9a/4FLC/" +
                "u12D9B+Sftb7Xu/4z8E975TZwf4p0/8cHlCv6qvpzrz7rHHy13" +
                "UH7ExvNp8g/xifBx/wkN8lf09SOsDze/5unfMc2z/vD8b675uf" +
                "t/pJ3fXH6eD/dzbvn6/zxf3t+554Lj/JZTf0p6/E3yP/1wfn+r" +
                "sueDLsL5i6L5obiFrEhC4B8f4VfZ8bnzj8S30SxhPn/X8rWF/E" +
                "8Qbi/g/N7AtwY3nQRU00sPvmn/8ZXVojqd6mn92UQom+V8Ymp+" +
                "8s36XAz7FgM3CfKB5Kuw/1BTDZFP/4b75/HhAd5uZX/Owv39yO" +
                "P76Afzx+f7xvQt8lemoPzF8NfuGM6kz/QcmFbimy470jHVWm3q" +
                "ZkKVvwz0X+jz07senwtP1Y+j/Kcqpr9L928ubn+o8Rfj/Flgf+" +
                "DzVWH5JPSfYfXv8/KrUUT+NY788Nr6qNj4Ljqsx/0tw/Utcv3L" +
                "kX8sKr/1iOVm9euWxKjS/d+Z8Ojzh5aqf6ZL/ZPl/Gfi+Uz++V" +
                "Ksf0Uo/99wx3fsr9XzE9T+LiLCv4jZP0D+Cu9P9vpT+puS9pcH" +
                "6Bxf08cvYr9FhH3grQ/BfhLr62j9Dvz8DfgHqL8V+3xi8f5I4f" +
                "1L0g9NiD5Pf7fH8/oQoA/2V2L2b4L9RZj9wQj9g5j9kyLri354" +
                "RH+ijP43vL9f+PlM+nB9Ncx/fn8biv9olv9Ua4Yb+49KLPvHyP" +
                "X+MWn911+r/s2Z+JPWX5fQXwT1L0Lyk9b/GZ7vcOPD+FSkxqep" +
                "+elc8ZMv6uP5R2rMsmYFaikuJvf+kRTyt/qchiH++2B4dtPHf6" +
                "X7O5W+P4LzpxnG5+BH+fcGy7ol+m/22G1/bFx+rXR+AsHR/Txu" +
                "fm97/ZlT5Y8aH/ven/MC5VfSzkcu7MML5/yo+Rt4fxz2B0jFn4" +
                "q1/gJy9f6773xrUv6IkH+QI/uqE/hz4Jkfup/fOERWu+x7ITg1" +
                "P1Lq/vPY566Af5nn/FlE/pOd3+Xqr/D9bzb/W/+1Du4P5v3w2P" +
                "E389+Sk3/L57949T/z/Cup/vMqx39hcSn9/lN4/xw5etT8fBo+" +
                "1//n+jfYP+P6b1T63fzp70+fjO5PtxPVi/vTbvjyfjW9vuzJb2" +
                "P/7AHv/kvjqT+tW8i0+7/4/nEYn3t/mN7ftdD7f7LVnxjvx1Ph" +
                "+MH5/q/+/lOT2J9Q0MdHihbSt4Gvwx6eT76FJ/5E999uj+Uv0n" +
                "+j5/epBzhyxKd0/0Z4JIyeXw3rH/b7HdbqN2qlftPksH/s97uk" +
                "6KfI+gmpP0Zsfphsn4L3K9H6ku7HMe6frvOvivWvvc8P5b9j8t" +
                "c0/wXfjwzUz2n6yRcfMc8nQgEO3/9cwPcBXKbCefdjue/Xxe+v" +
                "8ZwPn9LliyWfbvu1NfnYBn9Y8X1h/zGHf6kY9mVkX9X54A11/A" +
                "z2N6K+kpK/ST0fbASp/so/H+75ak33LxSDv318YJZfDZxfqNP4" +
                "y5Jvfv7BW187o9XXXPU9kZG+lP1T4n46YkSp9/OC+1G4fhXER/" +
                "Th92eWlU90PyVLfMGqr+eSX2Sf3fVHWd8Y6iNayn6Jn2zWR0zI" +
                "6qH6ysOwfqLXZ1D9KDX+a+ScJTJpf8P8knt8+vlEnv5Jf3+TJf" +
                "oPTP4z/ZMy74en11dRfXL352fKvr8+4nynLJEfSrufwdX/Oc9f" +
                "8epH+fqfpfIf0PcrmpgxqA==");
            
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
            final int rows = 798;
            final int cols = 8;
            final int compressedBytes = 1945;
            final int uncompressedBytes = 25537;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXL1vHEUUf7vsOZtIKIMVE4tqjSLkwgWiCRRW1pAIUqBYTi" +
                "xBQZ+CClEaaWy5iKlSpKC0XCDR+U+wUIqIyg1SdH8N+3GJb+9m" +
                "5vdm3ozvxDZR/G5m3ryZ9/V7b3f85N7+3ZPNH16NDn8+/+Zs//" +
                "X9g736+PG/H718+na7XqUxoBNlBQ0fRcYnt/+ksNPHtz/b//iv" +
                "rZvPRodb5zfOfrp4cPBFt75++s/2w9X+RzXZn9JFJPrkVr62Wn" +
                "6X0eiQaKWq6atfP9853nmZ6b3ftws0eTt/x305tRX1bic1Y/05" +
                "Wei5+bnj87Dxbv7+/vDew+Jws7xDtHW+c7R2sbK5R7p81KxaZ9" +
                "35S8fbHs26VZhegx8DOpBfc97Z7Eyq5167R2qW/M3rq/nxSD62" +
                "8SV5PRn53i+T/g/OJ5+wlAeej6Yip2rCSKlpvf13s/lLz5x5/p" +
                "I93s1/c4NHUxqYk3rWrqRbvkfWY1fkvh7LQy+GR15PSV9zzmf8" +
                "pLHfJ88f32n9x42z/dZ+7xx/3/qPcW+/64D7o9hmY3z7XrN+Y3" +
                "9a//FBs/79g9Xef+xN/BfJ7C+mH5nv1ynrfqHxG+qPvDmkoqKV" +
                "PPuSqNI6o0pVJVVV8x+sgNb77+JPeZzPpZseZD+Uj4FxxhfS/W" +
                "+oN538P6UVauW/Qb38iU558pfS3/N/Kwn/G+rtkK5n9gfOjy+f" +
                "HDj2fGA/S4t+3Oz2r672H6R/yr7+vP8AFxnFH7tu+yOMn/mRUp" +
                "h+t/b97snzH7v84NsmP3hw8MvAviP/iv33ZH+B8Zt4vEUedTET" +
                "EmX9ZBXLwdrJOnL8Koz/sP9G9wPlj27/f9HnD+uv2vwhO1p7vb" +
                "K5+i5/IHP+oELEjunMAFtbxqlhflZ66y+PnrEjeT2zPtqIlL84" +
                "8wftr9thbrz/0vvJ5L8Ad2mJ6btD+Wnf+K22yJ/tX4F/cI/v8Z" +
                "3Sju+w+SfAv1m/W/s1suAfQ/uV46CvHhr8zIwvKV587/KvPvcb" +
                "4hc4vy5m9dse32RO//Jnp79fT/T3t06+7vMxjR/ovzG+Vlb+w/" +
                "MbWwCMzgf4F3H8xfBPTnoHh7b56Uz8fur0FSU3fsD7W275Y/k4" +
                "6dh/mc5nOj+b4Aeqz896/IAM+EEehB/jn9SSwbz9C+JT/vxksV" +
                "8h8TnChxX/+sbOH6LnJyb8Woofqvj8BeJ/7f2x4I9vYuCPUD4Y" +
                "nxTif5dG+0STRBjb34vheJoZj+XvxncwPcD/oSchPq9nlVNoH9" +
                "j4WOj6kesDOpX8zfebX99B+Q1S60D8E84P6InxN6l/ZeXnhSQ+" +
                "QPZFFp9A/Ar1D4j7Dzi6Ys+PpONR/R/2B3QTVVdzFjOJh7Q+18" +
                "+YT5Zo9lAw8HEP+yNcH/cX1F7wpMGQuvcf1D/igT9fjQ+r/4vX" +
                "F/q3BftPn/4UMtbvQ4ByD/xUrp9M/fOhe+DXQf0huP7Nt6+XwP" +
                "+717fVb8ur+q0cH3KuH1ZfLiPVl+H8bHyf7PWHwpT/a+Z4Jc1v" +
                "YtQnKEJ9Waigyfyrc35+fTMMn/HFj7ztjz/+6Vf/t94/nv4z91" +
                "8MxRqvvvRe/1XfHzWv/4p5/wnkd876SBGML1nlq6+n/gHqLwz7" +
                "xfQfgf1bQvw9Qn1BB+FLuSu/Dth/cHyVtr4B7+fC5YvqL7Xwfr" +
                "PwgT7/zbr8d6a/RZb/4/ozH98uDQSMP9fO/J3tf5Ut/nsx6X8t" +
                "+voV9fUr6upXLyB+IMXPw+pPMfPrEPwX4/dlNPwezD8fESvv+B" +
                "vFjw78Pi9zUqqdtqG3LK7vNowp3e5rVAzwFTc+YaUj/t3jIf4s" +
                "fL8H06X+RToe1F+k/l2I38rxXYC/QroH5JXEvkTC/0LfX5Lil0" +
                "L9YeinqD8F15/S2p/ry88R/hgKL0yfX8U839wj/wP4HsLvQHyL" +
                "8CvW+xUF0E8HXbp+OP54ys3P0PsfQnxPBZ6fL34K6yNJ6gMB+J" +
                "fy8s+e+pt54p8Q3wHyx/210c43rL9Qjv8lzp/rpP3DDPwA+SKP" +
                "/sllxCcWK9/U/esofg7Dzyma/fSxj6U4PokVf8esf6D8XZj/w/" +
                "7D2nm/Uf7Ozu/D5S/7fgHQXxT/i+vvOL6JMb5wxjdFeP4d4f18" +
                "Dvrg6g+S0cX5ERPfUaH7q+Xv316L/gTGz175fwV+W83psvj7E4" +
                "AO55fGL8A+wfxTGL+Gz8/jX5z/ceYX5N9p3n+Yonb2tSobFraq" +
                "TDfxXdlO+qibwWxfo37fC9Nl+RGujy93/J2+vs+of/6v9w/8p7" +
                "B+6Y2vaI7/HvbXuN8vQvEBiN8j29c4/a8+9hnRU9c/0P1KHH/G" +
                "7t+eOT92fcLm3xLHR5Aeoz9Z9P0OVD+Q+k8Uv6XtD8b20bM/cD" +
                "5+5+CvhT2+ksUHC/dfQvx52fmD+HwM/I4V35hNAf/93sD+G2l/" +
                "SGr8AuILbHwiTf/Lot8/CeoP9Hp/hKVf+UBn/N9vsY2PV5+OmT" +
                "961Y+Wu36XeP2w77v5xF/S+gGMT6X4Ks++eeH3Ee2HuH+PcH1e" +
                "QBfjf/LvO3POx/3+xTLjS9exvkN+sL9a+v2OxHSUX+L8k6f/Vv" +
                "/KoQv0b/H5i2x+hn102mfp+crPH+UfEeTrWv8/KyQSfQ==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 5117;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqFXA+UFeV1f/wTpCwVxeSA0SBR0xTTUmyQHhv5eG+fQjwEjW" +
                "2OtsIqKa3rAgs9hiBKuzPfzHu77y2t+YOyLcfClm3Sc1qTJvVP" +
                "tElso1XTdm0SY1wWjbVkAduALmg4GLH3fve7c+8386TMme/e+7" +
                "v3/u795s2b+d68t1THq+P2K9Vx3KKNja5kZfRVsgAf7plZHe+N" +
                "SiVGquOi9zWr42ZSdbzxKcj8GuLpJMTjNbA3Jba/ZPfaOzzjl+" +
                "MppVJtsfCkdRx7ro+vQaTk/4X1tE0xy17v+X6IiV7/HvBdJJnM" +
                "VyrFRyVS8yZPuk5cZfsNwtOJup6uJZjdZ4eYxyw3y60bQZ6E/d" +
                "v2HbJAH45+bpYnX7CvMgJWG2uNHTBONstrkVkePYJIOgly/sVF" +
                "zQTtMXsM9f4/tI8mX/KM32mcm0y26xXfF708Zd+y/2T/2b5tTy" +
                "Vni98+Bfu/iQ3WcfvNZT+LxjWWTBC9t+zjTnv5rn3Cyadtl2J5" +
                "E/bHSa9/13VfAuSZZAmMR8GamHFP05V89gn7JIz/ap+1vzDL40" +
                "WAdZmuUglHkvbbbKEd32u60u8LIrGmqzkLxommq38do3Acu+A4" +
                "glZ7BbTHCIXX+NG+BZRjv4OvZPsvCx8cR88LcXAc6XwUPxzHLj" +
                "iOXboD+82+y6LjISZ63+shBvFPOKandV0318dJ713iukfkmcSQ" +
                "D45jC27Vw5MQDccRvS4iNnHyMo4gD8P+w+QIWaD/j51j4trbyQ" +
                "nQnyMMcmLZngAr+YGL/ZHPeQ3H8u3JG8k4xSbDyX80/sJ7XyiV" +
                "kv21K4Wh1vCe0eSl5L9B/iT5r+RnyfNZD/8L+3FdM/mxiftWRS" +
                "ec/qLHRoKIA248CPtPsePkkLOPlW+X/lEm/6nnhCPEVlljNvHK" +
                "Rq92cjR5PYsdNIMgB1m6s2GQ7egtM9g3lXCJkQ0tezDPYQaTO/" +
                "x5NUiMjb8kbzoTrdpOYaifz3kUz1dIXaFYs+/m6O1iVR0hmdJH" +
                "0pmfazE+uSFfU7y6Av5Lp6czstg20wayjaSfQxttdtiuAzkkiM" +
                "TqLMpkBMfyH0mE8w1RbDrT3WceVDxDUrvYAbOLTTF9X+G8fFf5" +
                "vpgZJXaV90oPvvJQvmbYj8b8caTYftOf/hqOIC+H/YL0Q2SBfq" +
                "mtmP6+cyCuH3fcWIpFSHqh9pfXpb+SLiAdXrc5zQc840Xubnul" +
                "MNQXes9811t/+lH3Sn+gWEEjfQfiq1G3ZZ99sY5IL9GZwPZBh3" +
                "64vC6cRzpX90yxybp8TfHqHlyXl6UfyWK7TTfIbpZuNt1s2xtM" +
                "d1+3IKZbe8OdRns9avXpwoSy+RjnuPf1xcJXf0R7svOxO19FI3" +
                "BmvRBXQqyoi+Q+avNa80r3cBxtvmZYXXNyr7hHd0aTkmdoPcSz" +
                "iD4bzYxORufB/bojmt2cBMj7YI14AcjPwbHtAf1S0LdGH7Z/Av" +
                "bB6Fdd1uWwu7MpWlivElv0sWgJRPxp83F9lNpfcL5rouXRtfU3" +
                "ohXRdYTHt0Z3yfoxWh2tSa+GuNtc9B9Et0ed0YRoorveboxv8F" +
                "Ft0TmUEZ0P+xwbRVtw/Rh9EI7J05D3EV03WuRjf8vP9LejpVE5" +
                "3iMxSa8cBxexijtK92Wd3aKPV/QZ2NeZTtMJx7OTpTvCnWzbmu" +
                "ls/EAQieVo2bVWfVmY/KvWKXr788LXmKM92fnYma+iETiOX45v" +
                "C7GirmuS1HU1b7xHkORb+Zphdc3JveLevqAdrmM4knTzXMC27W" +
                "1f0FxMuMTIJp7QX/+6P2ILiFG87n19iTA0rtAelrqHVjXLP4o3" +
                "F6vqCMmUPmofys+V9HiPIMl38zXD+elupef2BSY18IEIR5LuCK" +
                "ds278zaXMJ4RIjm3hCf+9L/rVKiVG8rnJDGBpLtYel7qFVzfLz" +
                "8eeKVXWEZEofWDecK+lwPmZI8r18zXB+ulvpGbAO0wGyg6XzdL" +
                "Btv246ml2CSCxHy661vjXC5Kt1iF57SPgaifZkvXXkq2gE7lt/" +
                "G28PsaKua/rZ/ENrXt198vN8zbC65uReXcQuswvkLpbOs4vt6i" +
                "tmV+MI4RIjm3hCf+MDvsYuYhSvu0K/IgyNb2kPS91Dq5rNB+O+" +
                "YlUdIZnSB9YN51qMT97J1wznp7uVngHbYDbYcRxBnjQb4FV7hy" +
                "ywh8uzzYZmw77KiNmQtLEG2W6vRSQFgaiZjukYoa7aBtaTycuW" +
                "CF9vN3vsW+4zNj2n2JCvIps9DufjM3G/xpIJYYaLO+3lu9wdzi" +
                "ZjeVMidffJu+A7qmsm03Qln32CoqHfX5gN8SKzAdc96Wfjienm" +
                "nmezOzmse2oX4rqnfC6se76B6x7Ycd0Da85oHuyXurhs3RPtwH" +
                "VP9Ge87slWDX7dE75ra8/Kuqc6JuueUqlnWK04cN3zx63WPVGz" +
                "+XL8gI/y6x7Qzocd1j09/w48bt3jULXugdks8miw7tG9pb8RT1" +
                "A9fJLXPfKv5zla92QxtO4ZMANwPg44edIMuPNxwNvD9h/NQDwI" +
                "5yPghMH5OCAbonA+DoifZOI+R8P56BBYGQ6xF8/H2keFId7LHj" +
                "wfoaY7H5mN+MRyXR03A+Ufxvt0VTgfc11B3GnS3Pno0NrlkgHn" +
                "Y6bDfSarFP8N+I7qmnA+DhR6OMFHEc7HATgfB0zVVCGmytK9B6" +
                "tsVw+bavMhQSSWo2UvaqwLp78+Hha+xpPak11zqvkqGnGvzKMh" +
                "VtR1TZK161vz6p6xt7BmWF1zcq+4uyfBp2tT4dyc1j9XPk3Qet" +
                "0+xM9+o7nV8doktKJ50Xx+lgy9TZZnzlqLrhAm915W75b2PnnG" +
                "3HiKnodzZf9e+XR0s3uWfgFhtbPCZ9FwHJ+CqOnRHYxEs6DH91" +
                "MP0YX0PDz8lIZ1feyV4hVG/77+ePQJ/b4mb/pudKNHborWe5YO" +
                "qVAdN3vNXjiee1m6I7yXtsp5lfMY0TGyiSf0Z+dVprMX/yErb4" +
                "23tSfMDCuESPzjYlUdIZnSB82myKvj0/Z8zXB+ulvpGbCygess" +
                "jiSdp8y2fSREJJajZS9qrAsD6fUpwtc8S3uy3sr5Khpx5+NIiB" +
                "V1XdPP5uHWvLrntDtfM6yuOblXF7HFbAG5haXzbKHNDrfvYUTH" +
                "yIaWPZjnoBxmEo31yj5haG7UnjBTesrXhON4rFhVR0im9EGzKf" +
                "Lq+PSufE3x6grqNSffTrMT5E6WzrOT7fa/YkTHyCae0J/VyHT2" +
                "uvPxl4Sh2a09YWZYIUTslGJVHSGZ0gfNpsir49M/z9cM56e7lZ" +
                "4Bq5s6yDpL56nTVplRmcGIjpFNPKE/q5Hp7HXn4wxhaEbaE2aG" +
                "FULEnlWsqiMkU/qg2RR5dXz61/ma4fx0t9IzYLPNbJCzWTrPbL" +
                "bbH2BEx+honSVxWY1M5xz3PHycOczs5mvao7rLVZHNr9vOzneu" +
                "/WF/bNNsirw6vjYrX1O8+R64U9z1t+j5b+Gh27l6TRN+4y4rnV" +
                "brHr2Cyq8w6n3C09+h1z3y78y/A4DOLgg7D/2tfweQdrX+HYDu" +
                "M12frxnOv7hS8uueW2Db70Yn7U8yC23Q4WgLIrGIu93HaOSWIG" +
                "+/e9V8jkMVX/9nMrb9mVf5FWfWAUbZS0IszAhnlXWXm4d0pSrt" +
                "D30hd9ADz4ZiN5vNIDezdL7NbNtLQ0RiOVr2osa6MPj39XHh69" +
                "+uPdl7ZXO+ikbc+XhZiBV1XZNk+kZrXt1zOp6vGVbXnNyri5hq" +
                "plYW4ojS3QW8hTbqGpFYxAVxfB7BkaJ4JC/5kI0yCOmP2MO1yK" +
                "+rcKx0WVloJ4VYmCF9UbyuEHolw98DF4Y+yc/3wLPxsS/mN/vr" +
                "rIHfjSQ1prciIplFf8imvVKtFWOYAefjwjP10HORYMxXrCsRgt" +
                "QeCX3vNT+KYWlejL8afy28I+jrc6yeU8fr5ZocP9jq8ytZ8Rp7" +
                "U/b59OHwvuE/5+7OX6v5+8KQyfOtzldx72uj72Hh3QPvM/k7H8" +
                "r23Rxnu0NOxXV18Q6av8+UWvwzM2AbcuMM/OWE+0XBDLadPqQQ" +
                "iZ3hcx1CTIS4cUjFDgmn5x8Ks33WkB+DesIrHTBHq67yffl4XS" +
                "H0ZtnZ7ylCX5Zf6IFnQ7E7zA6QO1g63w627U/NDruacImRDS34" +
                "XLgj789epUxnr3v2c40wxHu1J8yUnvI1obM1xao6QjKlj1o1P1" +
                "ffwx7F25GvKWy6gjoTybfRbAS5kaXzbGS7ci2PgmkNd4rRSMhU" +
                "uVY4/bW8BZ/uLO+XmhxZudau1VhrRomX2bTi1T3zbKSmztI9cM" +
                "8+dp/ZB3IfS+fZx3blOhztq2GMbGhhTN6fvVb70C+c/jhdFzKI" +
                "hyRmaH+rmrZTZ2tG6SGcEc+myKvjOUZqhvPT3UrPgG0z20BuY+" +
                "k829hOZptt8SDhEiObeEK/vxccYz0eYq97X68UBnhfKw9L3UPr" +
                "mnZTsaqOkExmg/vMxvxcfQ97FO9ms80e1Yzh/HS30rPZhvef8p" +
                "HeuXCPe7Z8xH0bdITvfOUjqBPifuGyGjH8nkvupunvSrx8itJM" +
                "pKc3Zt82AWv6++FnMvxciHE9w+ilDPdLmlt9lTVyt0TW8hH7+f" +
                "R35E6LGXx/pe+5MCZdK72kt/A80lsJTa+XHtKb0t/j7nrngK9D" +
                "nodjfnqD4/h0ejN+z8WcPEf3ufB+2Ebd6KQ9lFlogw5Hm8b7g1" +
                "i3OXQ000SOuldqVOne69DRHAPJUc826rXML1bWwWj1oM5u0dX9" +
                "WdeerTiPsGdfaTT05efHWHY2cmwFtgNudBIwttDOIxKLuNt9jE" +
                "ZCpgOuns9xqOITj487UPRzDHeAUfbvQ6zIKLPKusvNQ9WWSgdC" +
                "X8gd9MCzcbH2N4vf98gK1C4OMT2G63C1bl5jb3Z/FfExyF/Uah" +
                "1eW9V6LVtcF7/3Ojw5t9XTGNbfYx3enedusQ5f2nodXnzaE/Zk" +
                "dpvdNJJkDLdKW6XN7LYjhEuMbOIJ/SG7cPr7dVvIoKN0ZlghRO" +
                "z+YtWQkzOlD6wbzpV0uM9kSP3GfM1wfrpb6RmwHtNTPoEjSbhy" +
                "egtt1O0BwiVGNkQJYT/JsvuenDyksxdRzSEejKOapIlfLO6gfM" +
                "K+pLOLXfFsqD5XCDNYh+OYVSJ2XTOcH2PZfYZj15q15UM4kgSP" +
                "t9DOIxKLOO0Uo5GQqXzIHUefg6jmEw/FoTfv5xjuAKPgOK5t1Z" +
                "VEy6y4u/w8pLZUQlz7Qm7dA8/Gx24328uncCQJHm+hLQjEbtex" +
                "tCFKCPtJlk+5GqdEZy+imkM8GEc1SRO/WNxB+VRyns4udsWzof" +
                "pcIcwIe6ZKxK5rhvNjLDsfOXapWVpZhiNJuIp4C+08gpbWcKcY" +
                "jbAG63DH4o6jz3HXxxZ8iJKPquaryIZxlWXJ+SFWZJRZcU86Sr" +
                "oOe0Yc1uGqps7SPdBsMp6tZqsdxxHkSbPV/U5qq7eHK1VEaCSM" +
                "pVjozfuza/BW9LPGOGWEfP53Ulvxd1KYof1hTXscOZP36WzNyK" +
                "g9TZr7ndTW/Dzsm8WedYzUDOfns/XvpJwP7+PmEN3PUSYlttBG" +
                "vVSiUWLCNQZ5w1WGoWviIcoI1z3Mmv88wzm4nel7LvSaQ8mn9K" +
                "co6Uq+5+IqzFacR6vvuRBnH+GUX+xB4l2V+8x95TEcScI73lto" +
                "CwIZ9+lY2hAlhP0ky2Pu2jEmOnsR1RziwTiqSZr4xeIOymNwn7" +
                "nvTF3xbKg+Vwgzwp6pErHrmuH8GMuujxy7yWwCuYmlm/Mmtisr" +
                "Q0RiOdpsohiNhEyVlcLg39cri3zBc7OcX9enyMrKJNVYMqEVo8" +
                "TLbFrx6p55NlJTZ+keuGcfe6+5t7IIR5LA5C20BYHYe3UsbYgS" +
                "wn6SFfc5hjyksxd/R6o5xINxVJM08YvFHVQWJTWdXeyKZ0P1uU" +
                "KYEfZMlYhd1wznx1j2mvtYfLdXFtO7vuI+BbKFdsX95X5F/f2+" +
                "ePmqQd7w+qiZSNefqDSfXB85B7czXR/RW1mcPKGvj9KVXB+5Cr" +
                "MV59Hq+oi49nF+sYdwLuYe2Ebc6GQyJbPQBh2ONo33BLFuc+hI" +
                "pokccefgiNK916EjOQaSI55txGuZX6ysg5G0V2e36OqerGvPVp" +
                "xH2LOvNBL68vNjLLvPcOzdsI250clkamahDTpE0Xh3EOs2h45l" +
                "mki6t4wp3XsdOpZjIDnm2ca8lvnFyjoYS7+ks1t0dXfWtWcrzi" +
                "Ps2VcaC335+TGWHUeOnW6mlw/jiNLdgbyFNuoakVjEBXGMHsGR" +
                "ongkL/mQjTJCFqpCXl1PeKUDjEofCrEwQ/qieF0h9EqGv/8eDn" +
                "2Sn++BZ+Njp5gpIKeQ9Md4CtuVVYQyIrGiSQwhoYZ+YfDXlFW6" +
                "pnjYn5wd1tP1KaayKq6EWFHnTK5erBv2EPYmjMm0sH6+Wx97p7" +
                "mzchWOJIHJW2ijXvsC4YQlbezFDVGKZz9J+rsP8lSuctW8V3Ad" +
                "T3WJDTdGyS8Wd1m5qj5RZ2tGRrlrZqO6OoP1eI9UInZdM5wfY9" +
                "l95irOPNNz3MoKODvW49jqOa79uLsfrsj/niJ5WD/lRH99fnC/" +
                "XiHe+rziU1z0n/k5bmVF/WL9HJcyis9x9f+/Q520eo4b78n3pn" +
                "ea/f/7HPc2c1v5NRxJwjveW2ijnpwjiMRiJO2UpZGQqfwa1zHu" +
                "L7OIVbOQpDj05v0cwx1gVDIrxIqMMivuLj8PjoTzMauEuPaF3L" +
                "oHng3F2rvwtxXuu7Fp/LuU7LPmUPQW/QYEvHNLJfz7GcDmRfPV" +
                "37hNpiz//dqbeD66qCuYN//3M8SX4fMFz361MoR/P8Overy6Ol" +
                "47S73yrj/X0/ToUNbpLLDf77u4EM9HriLVWIuuDHvTev256BNi" +
                "R58kb/j3M756R5D7f4AYna0=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 4005;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqlWmuMXVUVHhRNHxQZtJRS7QN5liJCKRV87bmdS22LgPEBRB" +
                "kr06ZjU1TGNM3Q2nbPPdy59xbb9EGE1rZTqhhRE6MptSbyMD6I" +
                "QRSFRAn8MQV/KBKnBn8o0X32Pt/51tr79BJgTs6stdf61vette" +
                "ec+zitWWqW9vSYpaawPT1Y5X79BR0hFmieqQc/8MDv6en7TsrX" +
                "o350Xq6BbC7SsSpG4jlNFa/suXk01tTqkhO9esQ+s8/ZfbA+sw" +
                "/rbAYiEsMjXzWOxxyhBkz04DeHNINEyUr2FGv29IwtTlU1JyrZ" +
                "R3NNPGuKb10QazIrFcTfPOQykzmbwfpMhnWtgYjE8GBG50uN0k" +
                "fW+ws1g0TJSq2gI2MfTVU1JyrZR5gm5ZX4sVNjTT2f7JY9u9hh" +
                "c9jZw7A+cxjrbA4iEsODGZ0vNUofWX9f/1MzSJSs1Ao6MmZSVc" +
                "2JSvaRzY5nlfixTu61Lo419XyyW/bsYqvNamdXw/rMaqzrL5vV" +
                "9ZcZIRZos9rd10UUEc3Ut4mcQXlsb8oXvT6ujlVkxP0lNuWdVX" +
                "WlpwE+2L5N1byy59aVsaZWl5zo1SM2mA3OboD1mQ1YZ+eaDdl5" +
                "IU4Mj3zl9nFDnC/3w/l9G8lZ7OOjmoEZXcmeYs2+jdn5qarmRC" +
                "XY3D5ujGdN8X2nxZrMSgXxNw+5LWaLs1tgfWYL1vW/ICIxPJjR" +
                "+VKj9JH1+/hnzSBRslIryEjzy3ln3bpiJftofimeNcW3ro419X" +
                "yyW/bsYpvNZmc3w/rMZqyzCxGRGB75yl2Pm+N8qeH8vhFyhmhr" +
                "smZgRleyp1izbyS7KFXVnKgEm7vWRuJZU3zftFiTWakg9jHkJp" +
                "lJzk4KtshMwjqbH6KIEEvP7WOBCRHtgVUqtHqlJjNpB5KNEX9N" +
                "9+lY6qMSfG6ai9Ns3OfYzFhT95N2W2BXmVXOroL1mVXhWOJ+dI" +
                "RYoHmmHnwyBD9njfmi95lVsYqM5D+jB3SsipH4YGNdjQyR1v2x" +
                "plaXnOjVI5ab5c4uh/WZ5eFoPNn/qI4QCzTP1INPhuDnrDFftI" +
                "/LYxUZ8dfNEh2rYiQ+2FhXI0Ok9VCsqdUlJ3r1iD1mj7N7YH1m" +
                "TziWTFkyBRGJ4cGMzpf7UfrI+utximaQKFmpFXRk7KFUVXOikn" +
                "2EaVJeiW+fF2vq+WS37NnFmqbpbBPWZ5pY9z+GiMTwYEbnS43S" +
                "R9Zfj49pBomSlVpBR8Z+kapqTlSyjzBNyivx7QtjTT2f7JY9u9" +
                "hus9vZ3bA+sxvr/kcQkRgezOh8qVH6yPp9fEQzSJSs1Ao6MvbL" +
                "VFVzopJ9hGlSXolvL4g19XyyW/Zsdvcv6F/gFBbA+jkXYJ1djo" +
                "jE8GBG56GR+807yFm8X8/SDMzoSq2gI2NPp6qaE5Vgyz8/xrOm" +
                "+PZtsaaeT3bLnvsXmAPmgNvPA7B+hw9gnV2BiMTwYEbny79V6S" +
                "Ob/9Qe1wwSJSu1go6MPZOqak5Uso9cV8+a4tv3xpp6Ptkte3ax" +
                "lmk524L1mRbW2UJEJIYHMzpfapQ+sv652dc1g0TJSq2gI42zUl" +
                "XNiUr20bw7njXFt78Va+r5ZLfs2cX6Tb+z/bA+0491dqXpb+5i" +
                "hFigeaYefHIW1+NvUr7oc09/rCIj/m+xW8eqGIkPNtYFormDkf" +
                "YDsaZWl5zo1SMGzaCzg7A+M4h17VIdIRZonqkHnwzFc7O9KV+0" +
                "j4OxiozkP9kiHatiJJ7TVPHKntsPxppaXXKiV49Ya9Y6uxbWZ9" +
                "ZiXX9OR4gFmmfqwSdDsY8PpHzRPq6NVWTE/0vFczpWxUg8p6ni" +
                "lT23/xVranXJiV49YplZ5uwyWJ9ZhnW2WEeIBZpn6sEnQ7GP+1" +
                "K+aB+XxSoy4vfxPzpWxUh8Mc1V1byy5+bRWFOrS0706hEjZsTZ" +
                "EVifGcG6dokZydaFODE8mNH5cj9KH1n/uWcuGcbmyoyu1Ao6Mj" +
                "YvVZUIVrKPfBo9a/BHxxnpvDfW1PPJbtmzi3VMx9kOrM90sK7/" +
                "FRGJ4cGMzpcapY+svx6/qRkkSlZqBR0ZNamq5kQl+wjTpLwS37" +
                "k+1tTzyW7Zs4u1TdvZNqzPtLGu342IxPBgRudLjdJH1u/jvZpB" +
                "omSlVtARt4/tbl2xkn2EaVJeie8Mxpp6Ptkte3axyWays5ODLT" +
                "KTsc5qIYoIsbIKmBDRHlilQt9+XY1M2oFkY8Q/NzM6lvqoBJ+b" +
                "pi/Nxn127os1dT9ptwV2pVnp7EpYn1mJde0cHTErR19hVp+pB5" +
                "8MxT4eIJ/OlPu4MlaREb+P/9axKkbiOU0Vr+y5czDW1OqSE716" +
                "hDXWWQvrMxbr2tmISAwPZnS+3I/SR9bv432aQaJkpVbQEXc92m" +
                "5dsZJ9hGlSXonvPBtr6vlkt+zZxQbMgLMDsD4zgHXWryPEAs0z" +
                "9eCToXi/vjbli67HgVhFRjzHNTpWxUh8Mc2Sal7Zc+d4rKnVJS" +
                "d6zc/6RH3CvQJPBFt8NpvAujarPpF/L0SE2OCZU+oTjeOhEjl6" +
                "jZfJKhXGdknN/PfWG8GgO5BsjPjvhf/TMe1vnc1K8IVppC780X" +
                "Hy1lys8Q89p9ZnDL3Kzp1/jFPAy5Y75qeYzTN2LpHxT2Ohu+MG" +
                "PHMlJjAHxm4/9WOsH721CpFzSFTuYVU/tnV2dU1jV8E+EXPhp/" +
                "OC7NxeH+8ONfWE5kxzZvgdLGJhXRtBRGIkWlYRp9nJWdyTw+CQ" +
                "vNGdHanICs/xeNy5zOv+sA7TpLwS33kx1tR7lHYbct3v6+w6fc" +
                "/G97W+d6RHpvS+Nr+tvq/1ldv9vm49oTvX+er7OltRfV/LPrfN" +
                "0JPp37oHeV/LV4CKfbzRvXrc/+b3cfTbah+fYkejh97YPja+8g" +
                "b28YbXfn3cdl26jyfrQe6j2WF2uMl2wPo5d2Bdu8Ds8N+vFYYH" +
                "MzpfXvOF33oJWb+6kwytv8mMrtQKOtL6e6oqEaxkH/k0etbgu+" +
                "/XZWTbTbGmnk92y55dbJ3J92kdrM+swzobNOtarzJCLNA8Uw8+" +
                "OQv/CfK1/iszZW/rYhUZ8dfj8zqW+lKzmOa2al63j2Vk21djTa" +
                "0uOdGrR7gfZw2szxisa4vd19jTGSEWaJ6pB5+chf8k+drTZKbc" +
                "RxOryEixj6aqKz0N8JymitftYxnZ9kSsqdUlJ3r1iBVmhbMrYH" +
                "1mBda1RTpCLNA8Uw8+GQr/9ylftI8rYhUZyX/aM3SsipF4TlPF" +
                "K3ve9mqsqdUlJ3r1iKlmqrNTgy0yU7Gu3WOm+tfHqYzFXm5DVY" +
                "jkv/M6rslZfF67R1cjk3YAdq6ByW7XsdRHJfhSXfjueix57z4j" +
                "1tT9pN0W2GEz7OwwrM8MY127V0eIBZqn9GRdoTZMP8/GfNH1OB" +
                "yryIi/Hs/WsSpG4jlNFa/sXk8Uc8seOJlHTDfTa4fy38E6tWKV" +
                "r3M//HbY6RIbIvkZslgFXO2Q369D8FEVouQgXx5HNvdiFVlBjr" +
                "Qr9IK+Ah4KugK+ux5LpTCx1NRK7KG4JoDdZfJ/V90F67vYhXW2" +
                "ERGJ4cGMzmebir/VrlDPrPd/pxkkqrwiEwUdac9PVTUnKtlHdm" +
                "c8a4pnVcomFcS9E3JrzBpn18D6zBqss1EdIRZonqkHnwyF/0zK" +
                "F93Xa2IVGfH39a06VsVIfDGNreaNe9aaWl1yolePGDfjzo7D+s" +
                "w41rXLzbh/n1EYHszofLkfpY+s9/9IBvc5XGZUpVbQEfc5fLxb" +
                "V6xkH/k0etbgj0bTa009n+yWPYdI69LWgtb8+pHWJcVTjh+Vzz" +
                "t+7P7yO/23nyPiec+Rkz2nueuH+fOe7CHgq3/MH5LnRPuib4VH" +
                "WF/9vKfxvEblHlb1I9XPe5rfQD47KuOj41qb3/jE854jcWdVE+" +
                "pnaPYnZbeOp+3/l1rjBv2M8eQ/owMne15WvjaL/x/TnpbmdaTL" +
                "Pk6kE+D7deVzs1ZVN8k+dp3uZNObvWZv+B0sYmGdZWav/16oMD" +
                "yY0XnNTs7iunhUM0iUrNQKOuK+z+zt1hUr2UfzkXjW4Lv7Wk2v" +
                "NfV8slv27GLrzXpn18P6zHqs688jIjE8mNH57KZCY32oZ9bv47" +
                "OaQaLK7hIFHWn/IFXVnKhkH80/xbOm+OzmWFPPJ7tlz2b9azw3" +
                "G3v9zx9jpornj0+/+eeP7Z+9gedm9rWfP+qpXsdzs/1mv5tsP6" +
                "yfcz/WWRsRieHBjM7X5xQ7tj/UM+uV52gGiSr/yomCjrQfTlU1" +
                "JyrZR66rZ03xwOgOUgVxPYbckBlydgjWZ4awzjo6QizQPKVXn8" +
                "W6Qm2Ifp6N+aJX7aFYRUb89fiSjlUxEh9srKuRIRI6j+eMj8CJ" +
                "Xunr9ylb3v/ZNvcut59Z+6D/3DPR/V+p7Pvr87ph6vNa5St1/c" +
                "Xu74r2++78bkv9T2L7vbyr1r6quzH41e/X9Xly0tbB8n18v8TI" +
                "zsXnnon0lUr95XeaneF3sIiFdW2O2dm5LMSJ4cGMzmt2chbPwz" +
                "dqBomSlVpBR9z79c5uXbGSfeTT6FlTPKtSNqkgd9Dnek2vs73B" +
                "FplerGvnmt7OBxkhVlaFSkS0B1ap0NrE6s41zKQdSDZGiue4vV" +
                "VdxX2BGdPE2bhPVkk9qRV3W2AbpuFsA9ZnGlhnP0VEYngwo/Nl" +
                "T6WPrN/Hr2kGiZKVWkFHOgOpquZEJfvIjsWzpnhWpWxSQVyPIX" +
                "fQuNeJ/HewPnMQ69o8c7CzO8SJ4ZGvGsdjDlO+8tBH1u9jSzNI" +
                "lKxkT7Gmvx4PduuKlewjn0bPmuJZlbJJBbGPPpe/bt61fvT2u+" +
                "6wk/SrvP/E9XD82TB/n7Hnpv8zI/q01tvtW1TIdv/8aG/xnQ3L" +
                "T4Dys1v2iMNMoYLtdd+GZyAbPj/G7wb13gK7uOpTLTB2uXyfif" +
                "u3N9t1lf9+faJ+wtkTPLhy3zx/XT/RuUfmNbY64o6ZzOj86ONu" +
                "PZPrzjVgARreyY48m1+P3XoY/RVjZJa6gUVWF5GZOlc9X+BEr/" +
                "lpT7Vvdbv8Nvt2Xo/2NHu6fYd9p/urP2bf5SNn2XPsLPtuOztc" +
                "j/Z8e4HzLnTnRe6c7zEL3HmpfZ//3DPXLrRXOm+R/UDB+WFr7E" +
                "cK5bm27iLX2o/Zpc4us8W/GNnyKaT9jL3F3moH7Ernf8Gdt9lV" +
                "dsh+0Z5i31Jcjz+3k+0UO9VOs2fYM/Pr0U63M+xMe7bz37N1tn" +
                "XfBKz7nGMv9nyX2cu97hXOv8outlc7694/7YfsR23N2SW2v+zN" +
                "XY/2Ond+3N5or7f++Yz9hP2k/ZT9tL3JXY+ftZ/zsc8XvQ66c7" +
                "XZbra7+3s7rL/jt2NduxYRieHBjM6Xrx2lj6z//4+vaAaJkpVa" +
                "QUc6R1NVzYlK9hGmSXklnlUpm1QQr48B/38AbyNw");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 4082;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFW1uQnEUVHuQaShIkXDebiFERAxHESAK7hJ75MxsumwuJSQ" +
                "ghiAgICKFKuVTxgOmZ+bOzOwuUIFUKCgHZB0uqNN52DYtPSiUV" +
                "4SWPlg9AfCAqUlNZE0oBu/v0+c853T0zEbXcrr5955zvnO7pv/" +
                "/rqtvV7aWSul35ulTCnm03XpEI6aI25biFbWKAduXEmK8k/qSc" +
                "90GzcuLYlMRSjKTvR/O7NG8Ys/QpvXNOjNVpbFVbTb0VayfZiv" +
                "3G7yVCuqhNOW5hmxig3dwd8wXzuDX0whH7N/YHiaUYSd+PZl+a" +
                "N4xZ+pTeOSfG6jSG1JCph7B2kiHsN/4uEdJFbTVUP4AoIpLJex" +
                "uidvPNmC+Yx6HQC0fcPL4usRQj6fvRzKR5w5ilT+mdc2KsTqNl" +
                "0oQrXW0w7Jl+9UGHAt4Sui45dKJoUT3hPHiJmbkDKHXoRMAA9Y" +
                "Rnm/CtQk69MMouUbWKqD1bqWRHwy2kNXjCETGf0lOLxodjdLKy" +
                "Kpu6jLWTlbFffVsipIvalHmrupDsvL8yta005AvWYzn0whHH8b" +
                "bEUoyk70ezMM3Lo4fIw3GGCTgxVpsbu4rY10MulfS7iDWOcOmO" +
                "Y22pzwUtI/2lwY4rRX+NXyFT4+eI7TiG5CteFNpTJv8imMf16M" +
                "PIfmasT4h8vCu1KHqJ878VLzZ+CvLGZEoOVpxTr4Y6/yDmjn2M" +
                "GvbRn6gNRrbBzeN7KKm+WXBuwNLM44ZSz79uOs0/9baW9qO7Qo" +
                "2xN6QWRR9bh6PpFJvFayXO6ecxyS1ZagOF1UbIBltaSC/nUijN" +
                "PG5MRVEbFDEVOrVLQ80dbzaf6jGPG6WP5jORt2eaT1ut2rIw+t" +
                "i68PsGj635bOxVRq42+nlMcsc+moubFzYXVdvNC9Q6F+OK4uj5" +
                "p+k9737LtrNdZ+cR2vhXP5D87YVObUKsLi55Lm0t7RO/2gtSy7" +
                "awV23DOKK94B+p2AzXTj8z66RUrfPzuI5zUyt9XFenUVIr9rxq" +
                "08yTu06qTqOtmcfp3sd1N53RuT3ncVrax8d1/SapZVvYq05vX5" +
                "BkbaZjw3mMpTCPHCWfnUZYHilvsyVk+MuPI6krt9l5JHnnv246" +
                "5ZFex7WJZaTHca2aT3MtHjeMpHNMUMfHdeyZ5pFxj6RGaNepP6" +
                "6nmhfwtWtz4/1qe+xMh0whZtajb8sVTkeWa011Pj5HL0KZYT9s" +
                "y+1r5bFiPEyhLZaY/Hr8Y6A1RTFWp7YvSB19DU26NNJqO1iPU3" +
                "weKSIemUCcz+xwdhiOa30SWlvMlmY9Hp8dbtxt27W/IqbP1Quh" +
                "bUvQx5Ja+gvEZM6uH4jrjx+hLDs89h7UnMlYb9SbEbXl6C7QIs" +
                "2x943WyfouRPTHssP6LIhBz48Z3XnmgNddSlKb5Dzqa/g8FhH5" +
                "qxx9vb4bsNpfyEN2WK1Ra/yuuqbYXx2m1mT9Wb9aY+cR+oChLr" +
                "fEPiCoZa0BaZ1MsVlWtFJrWieRHY+B+LHEBEhrltTiMYYoeQ5R" +
                "YJTzaPV4FNFZfY3EvL+1yh9TWNuWbau12ZJsCaDQB4z6ZIl91A" +
                "Yta02sxdF1HHKEdjwG4scSEyCtPqnFYwzRYn6WhCj3zfV4FNE8" +
                "Bhbe32rl91JV7KmAqdX117LlgELf5sYp1CdL7KN2Y7aLZznpiD" +
                "iXo5W04zEQP5aYAGmdK7V4jCFKfkOU+w7ji+1Dj9xvNpPN+P3y" +
                "pYLHYaZcZtKM2x9nqi8hZjV9ewb1scRWtsyyWWvUEXEuQy/ZjN" +
                "kfZ0Im68H6IF7UJk27P3It28K4CCVG8Eu6NNJsJjjPvJQtYzbB" +
                "7IBPjvi5aGfmfGNLqJ2k7VOfSW03j4WORSiRhHNYLcfTBwhxep" +
                "RxtE4iCfrnMQjfDGnNSnht8x5aIhvEI8cK7drOYPR9nFGOj0dL" +
                "MWdttUqt8utzVbFSHWbzim1qlb3uwT7X5ZbYBwTx+t9IR5yvt6" +
                "GVWtV6mOx4DMSPJSZA7HUP15IxphntaCQKjOF5pv42jyI6roMx" +
                "gb/sSOaf6VR3FzPsMJtX3JsdcevxSHU3YlYT2mgJNcltCWyo02" +
                "qIebwXZdmRVg1qzmStrQ9iQ23SbNWllm1hXIQSI/jl8RNrcFzv" +
                "FqstmB3wyRHwqYbVsJ/X4WKGHaaGs3nZPDXsrnuGMWfzUJdbYh" +
                "8Q1LLWgLQeY5HNQw6bWo+QHY+B+LHEBEjrUanFYwxR8hyiwBhd" +
                "98zjUUTrcVhifl4OZYeM5SGsHc8h7OenZYfcehQ6lEgi5UVERR" +
                "ul7o6KMZj1eIhrcUvpQSKteuyVa5AlxVHZFo4V2mZ/FKOXPuX4" +
                "eLQUM0foDkrf7zXmZ/PNXJ5OUv0bi/R6FqMvzuZbHasbSF5G1h" +
                "5POXo+78nP6Pa8J/2cAv1W2/qyLs8p2qn4ej3vUXPVXCihRgz6" +
                "+Vlq7o7HAScdrs2tSE+yE6e/v16LHJw3OHoCL9zC3eN9EEbO5T" +
                "I+7FfuDccK7dpOOXrpU85RHK2XzVFzTD0Hai+Zg/38YjXH7Y9z" +
                "CAtbtgYrQGQLWbmH0evI2uyPhSSOgLMR4nbcRyUWt9ES+exzil" +
                "hqk5lHMXrpU8YTRwu6+j59rDnejtcn0HMK/VE9W8/RZp6rr2t3" +
                "VGtz7aPn6X69AJ5T6E/r80zrMyafb/Iip3OhyYvhuC6Yvqj9Na" +
                "2+Qivt7xNGb9RVgwzpq/RKU1+tr03sDZv1Vn2Tvtm0vmLyLfpW" +
                "fYe+Ux+jP+KP66v1LG3u2vUp+lR9mn1Ooc/QZ+lz9NlOulJ/3G" +
                "CfMPmzju0i/XmzHr+uLzHtS/VSe1zrAZMH9ZW6YupMr+DPKbQ5" +
                "d+hVeq1erd29tL7O5C/pDXqTvl5v0TeKSL9q8m2pHYn2mvo341" +
                "2LnqoBxp+HW7R2U6cdD21GH+i9/9Ezs9rW1I483i93KuQHDPZH" +
                "/iTP1pV7eBycMzV6OcrEk7v7xNE9oAagVAMcg35+GaCIkC616g" +
                "dQh9g4L+f08zjGfZIkjoCzEeLmcb7E4jZa0ihHSrE0jJOsuD/u" +
                "K4yWvLjecrWc2pDNPF5u7mTPlDpYki77VXyv+lZ03lhOHqpvUX" +
                "v8kg5vupZzPukFnuMSB0ZFcVGJLVvbqCgO4jT7Y2L04SjBkmsm" +
                "4x5Ug9SGbOZxUMzjIEhITnWqF/KjdPQxao8v7aTP+WJeM4+DXI" +
                "r83I73bZ0P8DiI08xjYvThKMGSayav+fh55mGT3dmoIs4A5jyz" +
                "Hc4zpjzPY+cLjcUd+dl5pvxDXdXf6nENutmVN/veLdH+OAjnGS" +
                "ctzjOmPpv2x+g8Y0ajtT3POGyAeUueZ+j9jDzPJK9NX81ehRJq" +
                "xKBf2YQI16FEEimX7MTpv9vbJBm4FreUHiQCHJ2jIkuKA0YT8x" +
                "IXjVVGEHvgM2hz6sxJZ748k++w+Pmr9h15jZ86E6bPy+Xn+Bmc" +
                "v+dK3dPE77lsHr8mdX+B7fR7rrwSovz82+26Qdz9PCkx/55rT7" +
                "bHzOcerN0M78F+ZT0iXIcSSaS8+K2KNkrdelwvGbgWt5QeJJIP" +
                "xV4lJ1pSHDCamDevytFLn3J8PFqKOdvDfs/peD1WritWwzS7Gp" +
                "uW64702eqd7rIen++1Hu3b9u7vXcevDbSmMUb4DiC1HovRTHdb" +
                "j/L9fjg74FMgsB73ZnvNfO7F2s3wXuyXf4sI16FEEikvfquijV" +
                "I3j89KBq7FLaUHiYwPx14lJ1pSHDCamJfrk1XMxj2w9ehkbD1O" +
                "xuuxerBYDZN+7Ry0mun1SCvI2E2KPe6gWI8TbIUdTK7HSeuj2/" +
                "7o4yCtSYzRttPrsRjNpFyPMrrqZHI9TvLIBALrcV+2z8znPqzd" +
                "DO/DfvkVRBDNb0MpWYQcRusu/1vtA/v8TpS6eXyBGPKvcQnWPA" +
                "bi5kh+R+yVa5AlxQGjiXnzW+XopU85Ph4txWyw/dn+cF8AzOZ8" +
                "W7bfXodn+6vTiFlNqNES9ZHLlsBG7GI97kRZtn/8RrKT+yNnQ2" +
                "2KzH2XwrRsy+Z6GdoUGztf3yPjR9bO30mRPd8fkYU0UEttsqm4" +
                "ot8E2Yz4NbyfAYyXpEu15BD3J8zD6HMkHb85eTezSfKFbO5+Zh" +
                "PXwh5FyH1CbUcTc7v7mcTo5Sh5SsWkFqlFUKpFHIN+9R1AESFd" +
                "boU6xMZ5Oaf/Td+R1iiJI+BshJRKI/MsRyqqMC6K31j1xdIwTr" +
                "Li/rivMFqve74yd3a2VMUdHvRsP38AUERIl1uhDiCyhazcw8jp" +
                "0holcQScjRB3jD4osbiNlshn/M6NpWGcZMX9cV9htOTF1DfYVD" +
                "DdANlEqyXGS8Kplhxi3TMPld2hNDqub5B8oX5l93hOjBgVxkU4" +
                "b1mrFDfH+OjlKHlKx+SwzTZRD7KZxx0S4yXi8BwX+pKjk4fKdC" +
                "hNxcNZQv3KNGfEqDAuwnkLrGJujpk1ew6xcP88pWNy2GKbqAe5" +
                "VFpxqcR4STjVkqOTh8rLanGPeVws+UL9ysucEaPCuAjnLbCKuT" +
                "nGRy9HyVM6JoddaBP1IBvPAxLjJeFUS45OHkZ/HEpT8cTsnRgx" +
                "KoyLcN7C0YTcHOOjl6PkqUtMW2yiHmRzXI9JjJeI++N6C5WyFX" +
                "sYfU1t6TGPWyRfSp8YMSqMi3DeMqMZTXFzjI9ejpKnLjFdbxP1" +
                "IJtf8AqJ8ZJwqiVHJw9jp4bSVDwxu/gOoEWMGBXGRThv4WhCbo" +
                "7x0ctR8tQppvC5Bz25qVwpsdT7wvD9Xvq5HfeglvR+WxjewYca" +
                "lSvl805x793u8L7wyhT30b0v5KlTTGZkn7OJepCN57LEeEk41Z" +
                "Kjk4fs16E0FU/M3okRo8K4COctHE3IzTE+ejlKnjrHFHwHULyf" +
                "yR8tsD73fubxTu9n9BPy/Qz8v0L6/czYQvsdQNe3Mxt7vZ8x2C" +
                "x9sv526v2Mnp9+P5M/4tof6v2MXh++n9FfTr/bl28DoJ8/ER45" +
                "WR48v2qn/n8my926y/lRgO0st3iv9wqhl/CoCiOXcnhuFh7XoV" +
                "9pDYjVQT25B6RiiHcVvuuxeXwytQP1nsd43+H+Rz71/5nHkU92" +
                "n8fU/ni086j6lDlibQm1O/L7sJ9/TyKkS636AWxTKfvE4I/rld" +
                "InSlAe+uN91MmfkljclsxuNN8Nx8o1afTSp/QeR+s1+lW/qfux" +
                "dpJ+7Ff/LBHV3ziFpNyKEFs2ZttW9hD0icGfZx6S1ihRxf//kD" +
                "9iJyTUkRppZluHfqUmIFaH64Xe42hp3sKn6LWVxVXa06E8q5eO" +
                "4g+00rpZ/eg4/pO/Dt+R1o8u8u569eR/oMrv9vT35Xd75fuD7/" +
                "Z+8N/4bm/sqv/8uz04X3f6bm/7gtR3e2Y0/9Z3e/Z8fbTf7anZ" +
                "arZZl7Oh9it1NvbL3wAUEdKlltkfvQ4gsoWs3MPY1WSd7yRJHA" +
                "FnIyTUkRrkCzPGAaOJeXmcZMX9cV9htOSl8/2M2nW09zNhr9v9" +
                "zNid7H/ud324+5nw+430/Qx7g9lGX/+j+5l/AT0jYMI=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 3383;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W2mMHFcRHo61vJa968Qm3uCsbSwlEJSw2UAcIzZhenoGjI" +
                "EcJLCsfHAFKSAE2CRCigXx692ZscYSFl4iY8kRPtYSEr8srBzC" +
                "iXEuBwkJ/vAjcUJsKwlg4wStCH/8h/e6urqq3tE942ta/Y6qr7" +
                "6qetOvjzc91dHqaKVSHa2mNXygZ/rN/SBFCWG5FWJAIlvIyj10" +
                "fiStUeNGwNlIYmMkQsZF8ets9rlaO06y4v64LzvaDDtSHdH1CN" +
                "SZZgT7zYMgRUl1JP4ZaakGDEhMCSgoOSe0jZxbo8aNANmp78NI" +
                "hIyL4nf9yhgoNo6z83ejJS+ViupTc9RcjFD9XO+DptX8fS77sN" +
                "6vU7+JH1Ur1ErdviGTf6zCPurmLJpH1SdNyTR3qKq607SmPqi1" +
                "jUrhR31NTaT1N7P+tz2YfjVP/TJtXa33q9SH1BJdD+l9eOuyVP" +
                "4RtVfdmLZG1CjGo1ap29P6M4wrVvU88rV6/5KWfVnvd2X6e9V9" +
                "6n71VTWuvq7WZbJv+GNvHMlbs9hqHpZa0BAywDQLmMl3/fp4vF" +
                "LyaRxBHynXrMsvUeAv9bnL1DCOIb9F8bdvbByZfCc8OiyyAEvj" +
                "idzfgXwcn5La+ECccGRgnDTKYAzWo038chkL+jCxUDwyPkJBTN" +
                "hrPGFboGc7Ux9GWyfh0SGfIRaa1/G+fByfsbzsi5VGriwZxxSl" +
                "a+XVKr/cimaCYqF4ZHxqHsNfpfclbgbSc4a9vSB2ped1QXx6Xv" +
                "/Aa7cz3gkl1CiD/uRd8c72zUYyeXcmnUItWdgcBpXyTIFEcmrp" +
                "lGTgKB6XfwNU+xMer4ITY2NZTtm5QjvZyyRTkKOI2OOBjyCPW2" +
                "j258fjsZBGZ3KH/xtLNnKUd9a+Wun6E+9PNth8RfzN58KIXvwW" +
                "nEHv9Ma5K94FJdQog37zeZRwDG2kkXrJLjl1JLdKBo7ilv6NRx" +
                "mOimIjbHvUztXFk5XLxj3wEUx10/G0rqexTjXT2G++HE+3PwVy" +
                "kDXOoZYsbA6DSr//cyCRnI1znIPsAJVHNx3aANW+zfUqOTE2ZI" +
                "N4ZK7Q1vM6lxgMx9n58WgpZi6pr/Dc9/zJf8dRft9T9Kn/M6hZ" +
                "0SuXn8N/3xP22yt/oX55PsKP5+P4V+ss8HhX57QSVP1f5TFwLp" +
                "uvmN9w+BFhvz2N43Jvzo/Fj0EJNcqg3/wbSjiGNtJIvWSXnHpO" +
                "bpYMHMUt/RuPMhwVxUbY9iY7VxdPVi4b98BHkMetx3mZZ16fvB" +
                "zzur0j+F0v8z4bzfbCbzj887r9i0tyPPrPGUP1ISihRhn0m2+h" +
                "hGNoI43US3bizNqnJANHcUvpQUp8XiUnWjLsKTtXF09WLhv3wE" +
                "cw1S2tL836S3NNKjN7822QYl9i3Z6UT75L7OK42IlewMYXA0ZB" +
                "nLhZx4Gwpd3L+ENb6mPUcb/D+Z0jz7IAf9U51TmViilNna0Ezc" +
                "F+898gRQlhuRViQCJbyMo9dH4srVHjRsDZSGJjJELGRfHr7+9X" +
                "rtaOk6y4P+7Ljpa8ZOM6nJ85j+bfzE+sq9LRrq7XR4s17d3Bc8" +
                "/wJTh/DYcioPNjd3kUxzj5UOk45KsTzXMhTXf2fk37QOUyf/wR" +
                "tPf3lsdFR5GvrDX/F9J0Z+/X1DdfuQzEkbS5tzwuOoo/Yiu6wd" +
                "a05vVij5/WANd0fso08y8sxtaC3iKQ2Uh9q/8SjNmh+BCUUKMM" +
                "+o03UIJSI6GNNJwDbIgdeFCvx/ERYkAN4rml9CAlYCe9cgRZUh" +
                "yQjctLXJSrjMD1wEeQx+2/z20tvLT34aDpbLncM8p/H94avPCn" +
                "9gs6Rp/NZ8JwSNOdvV/TaV25DMS8Hu4tj4uOIv+VsHV1SNOdvV" +
                "9T33TlMhDXmU295XHRUeSr4K0lIU139n5N++krl4G473mqtzx6" +
                "+VSvqV4DJdQog37rFikhrG1FErdPDNCubZDWqEG97Y/3fRiJ8D" +
                "Ob2vYrkZS99Ck9udFmiKGqfs42JdSpZgj79eekhLC2FUncPjFk" +
                "z4W/k9aoqebP/9If7/swEuFnpmxcXh43yt087RgoM7M3ZmFlCm" +
                "q8mmG/dX/2yz/DkJbvdouYOGc2jodsvq33uFd26YU2yW1HBfqt" +
                "y+zY0mzukxbS2l6pI7ntQV7t7ZW9aBuW0ErPj/oJrjVDWsCTPv" +
                "wpwpTbR9vKMElVonjc0Tb/fQ/pe/NssiZbP4t6SH3Aeb9nvhpQ" +
                "g2qRngkvqMWpRJ8D1FJ1nXpe1yvUSnW9eb9HfRTe71EfTzE34f" +
                "s96pac6Ta1Omvl7/foCH5r3u9Rn1Nr1Od1/QX1RTcfNaE2qI3m" +
                "/R71LfN+j3pAPai+p96n3p8j+s17AGqBWkjv96hr1RAcj2q5eb" +
                "9H7+z9Hp3NrfB+j/o0vN+jxtRnVY2/36Pba/Wev9+j7tblC+pe" +
                "Xebv96j1ItLv6P278en4tL5+ncY6vZqdxn79RZRwDG2kkfr8up" +
                "i3UZvO6z9LBo7iltKDlPi8Sk60JCxk4/JyPFm5bNwDuzdIdTjb" +
                "fefHeHG8uDHbGr/U50fDylmu1PnR+L2Q82M4BtKZeR336SNTz2" +
                "tTa299OK/jPt1e3NqoS5zXx3Xbmtda4szrnEnPa2jTvE5ZG+ol" +
                "e16jjdmK5rXRpgg2rzWjM6+1LJ3XKRbe2+ujea3b3nmt5Wv1zu" +
                "Z1am/Na4g1w6fzurqwurD2nilNnd7dZT3TN20uIayRkyS9/mcS" +
                "UwIKS9CCzrCBhWQBL6Dl/oiXIjAoWyYtKC7Acw9Sa7ZkL8YGGK" +
                "4jezsGzAZj9JzjH86vil+xNC/La7xpT77Z67pF9Nble5LBee29" +
                "FvfoV93Tw/PMmeqZeNSUUOsjNeuZvi0hrJHDDhgukUzxKPoxMi" +
                "PlfKQBnNHaesRgBICVMpeRssLo7DzIN3kycq6T3DwGzAYt9bG5" +
                "B86VtT1Z73h2jO9BCX3bBiuPL9LaMiq5BbLaRxGiQR9e2TJawB" +
                "IGM+DHI3ohb9gibz52HAd/fpC9y5kdlf+FPZvNE/l9+K/1vtvG" +
                "EbLro15YRP8o1vsl8jN1vcRgBiDzW6PfIu5kfbHnUPbRWffZBm" +
                "Syxa/x/mPFxnK5dQTPLV/5LV5pBYb0OeOse7cSPD+eLeeOzhZ7" +
                "DmWfjfJ7sGffSf7vgNYffDhCdn08Covo7WK9X1LMihmAzG+Nfo" +
                "u4kweKPYeyT9J/P0QDsGey/P3p5EE9luydXcARUjCNBSJbxbnT" +
                "o+kvToYD5RJrLfEEYJLVPDK081ujX1ub7GXt7xd7DmXPEB0soZ" +
                "Uej89IbbZO0elinaITnqXl9lGnOx+E4nFHncC87pT7D3km2yKW" +
                "aBD27DvJf+dtPevDEbLbj7Rov16s90uKWTEDkPmt26+VcycPF3" +
                "suzl718f990H14vIbKVPNqMqBOyP99kDawxq/1Kh+5ZNBnoV5x" +
                "LCaKOZO5/H8f8Rr834f6e/g6g34L//exxqz3hPML/e8jO3vOwu" +
                "4cjy/6cNXSXy2TR1w7dp56xee/TOKP2s4AZH5r9FvEnWwp9hzK" +
                "Pl3XGaNaj/9Yvt4zZtogwfWOqQX2Sgxo5RrJ1AAxAatY7xnzrY" +
                "ejd7MVrfcYLSJQQzHTeg96ISz368ZAsaGOj44bg8wlPhOfqZ03" +
                "JdT6W8t6pm/arS0gJwxtRgoS1ENdO59+/+epjVoj5RykMTjwCS" +
                "3SUw8jQEQ4KsyGY+08ZMzgCdi5T5kfyvJjPMPCejism7nr4a2X" +
                "rPXwk+Xr4epU2Xp4rf9i18Nr/ReyHh49Xb4erjHOeri7buauh/" +
                "vu0+m5oHXcvZen2ebe0RS9kcJ+5/pP+XshckXYh+drxHJ1Fq8z" +
                "9jpudIzbyXUr7EXHfCvU7kpxQeQTWNL1urbF1WKJH/+6WSNwvU" +
                "1UpbJ9QekoTjQmSq5kUxJlWtgLWWM2xdx+rcsdYqFvKcnfW229" +
                "js8z/Dvs5i2jIkzttXLr0v88nAjNIVP773vQr80tnmd2NGaL8p" +
                "GzgOnHG+NQQo0y6Nd2N8Zb50lCWETTzlvGjtiJM8tnt8tnRT1u" +
                "e+ESGyMRPBvEUzY+Xh69zMjm5jFQZtiuHkz7601tyiR7otataV" +
                "OCnGPEHdVBz13WQYMhzfYBrvNZINpojbX04cOZOBAF/rAHcvRC" +
                "3qInUS+j5u3oScjR1rrcIHFzaazDkuZ1e5mrxbLk/LiuYE6eLJ" +
                "3X64rsfSjTwl7Iuv1GeWzJLr/W5XZxtRmY97WZdK13Bs81tRmj" +
                "gRLPDUYrz0Wo5ecMkFEJVqA1fGwle0a2jBbiCV2vMSZ+piJ+th" +
                "6e5YHZoQds8xiIy7RpBNDKvl7bUfvP6BRRvMN+VnHvSibf7Ob3" +
                "a/7ZPsjvhPy/X9v3J6G7DvvuB9rw+7X9DUSHbKnLGB0qv176fr" +
                "+uz6/Pr1RMCbXRQM+026tQwjG0kUbq0Qe1UZuO47WSgaO4pfQg" +
                "JT6vkhMtCRsdtXN18YiREbge6JPpFtUX2eMNMrO3V4MW+4glqT" +
                "keJYZQyOQcjzehDmz8KJRiiZsfZccnI82OtcO21GWMDtv8vsjs" +
                "nt7/D9sOm6M=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 2909;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFW32MHVUVf21tIYvAwtKWzwrUprYFFLTyIU3f9L1xt91o/a" +
                "h8aKlrpLQ1rVGif/nPzkw3b+q+4j9giopR/1NCghiFQIXYkJhs" +
                "ynYX8Q9tNEa2NhJjcdcsxKD1ztw5c865n/P69tH3MnfOPed3fu" +
                "fc8+bNm3vnTfSNaElNeUXfjC4R7UCt1rozuiLXrBDbNWL7V4FY" +
                "k7drw9l8vz5vbxLbzbn0oZJpY3RHrSZR+Bq/Jbd9PBqKBsV+az" +
                "RcM7yiB6Jd+f5LeftgtDfaFy2KFiuoi6P+QloutqtkrNFV0fsK" +
                "7TrEBs9HtxXaO4v93dHmaAvlC55X+LcbMtvJel8W2+5aLV4ttj" +
                "XxQHxz9FWoY3yNlOJVnCG5SWeNr61ZXvEHDbrrRR0/Uuv6Fa90" +
                "WtebtMGLFvR1HBNv6DCX27RK/0DWEfqtY0odh+NPdjn+S0UdBx" +
                "egjsMu6+gqk3bsQO1dfcWfK6V7lDo+3DX3vaKOX+j5CHYYj8cX" +
                "/J4mTPRy1bjRUrFdCL1mo6jaZGOzgvvjwYujk9GNPr6Dl2Qtek" +
                "d/olaVVdj/oGnud0fIcoz6CP4ysYnvevRnOgL+grjR7c5abNPz" +
                "I9b7ov2eT6IdHMhauRXZbkRr3h5A2cd2brYyFw8mrnMUzVuOxB" +
                "7XnZvZSrjbVUcRf6+Uvq8ej+f2PcPjMRZn4PGdPf9eP26sxTMV" +
                "Pv9nuo1d3yE3kUV5Fox/Kn5r/gpW3jrH8aQLs+WkPxdfjNbrHI" +
                "XZ270hrmqNf4T6+CnOaee2ZZhdedU/TX+vG2sba0XG3wVrZs80" +
                "6vWg4UyUX1lmWJPNrOe5+GNwVCZBL5yV49AjI9b46Qmv+OfhLO" +
                "CAJdsjN0rebyFe95zWa939a/xrvf5em6974l/6PheB+dUCnl3K" +
                "a/rgtwtTR+43/vD5qaM6GuPYX+jq3Dgnt4LraHk8vmPCIbITfl" +
                "LHn7jtZo2bFUYgdWbv4Dk/d/CcO3Ino49fAildpETZ6u5bctvK" +
                "ca2Z3h2JMpI5r9YjFcZ+bAG/12W8VFkVCIbcfcvYhjiu9bce1n" +
                "HInlfrVIWxv9xtBnI+kxyhvzNpuRYUXZ1bH4+nhXxjBbYPW9Yp" +
                "XhXf6595ve/xzWfiV7L5THRanc/k8nXxpLHGR/3zmeBotM20Th" +
                "F9Vp3PRF+s/nudLlNm+i9mdVTm3DMdrfdkdXxyAb4zv3dazXX8" +
                "tZsz2Soxna73KFHGoZVSXscLFeswyti3swGK46jN5u3D5PNCgo" +
                "K8s0hCP+zKycVti4y+VVi082Ofwhbq1zOJ91cjnOV+48/27vwo" +
                "r7DUPIvz4z8qHOcnFy4XnBemFyl13Obulwx/4SiOG5/o4e/MNn" +
                "tewVgF/7FzjPuGOqsCHZfo7Mt8Na5iqV65Dn/FfZ1e9Yo/w0BU" +
                "GAHcV3Bl6OIO3nBHrjZjLY6m10vpVE/mM5PnaT5TYc4XdD0vDP" +
                "dCS+p4WrdC6/695hhlfu0/o+51+ZtQmQQ9m3fwtD+34GmzVee2" +
                "sYS7ocXrnrHf6FZoPWN0YJoP+L2rxUBUJmXb2DG7N8R1ccd/N1" +
                "tBi9w2lvDz0MZvlr8z79Wt0HrG6MC03vR7V4uBqEyCns27dcaf" +
                "WzxrturcNpbgELRSyut4qWJtouw+XyImv+5p2mw2bz+GoyDv/L" +
                "fnEI+nxnVx2yKjr48lOAytlPI69ivWwXie2gPrHVTEZCiOozab" +
                "tw8Tv8VRkHcWSegHXTm5uG2R0dfHEpZXx/F/yjpezq3y+AsrXE" +
                "dzjPJ7fcrvXS0GojIJeuGzlnXct025KZizZityo2TJ6ivQJuVq" +
                "WTqgW6H1jNGB2dLn93bH2NKXLOGoTIKezTupkFvyHrNV5/ZXga" +
                "xT3KB9XtMdzIts6xTvdH+FmFzQ+TpF61U/b+t33a5ThA9BG/6i" +
                "rONq3QptFTbzq73I710tBqLyvB9yeweL/bkFi81WnVvHJf22+X" +
                "VyUS9mG+0l52c+k/TV3oVXWK5LJleUx+P7dSuX/WyG786//d6+" +
                "GMkKjsok6Nm8W3P+3JIrzVad24QLlslNPT8m15pwwbKOV0OYR/" +
                "vrbrtZ42aFEUid2bt+1s9dP+uO3MnoybrZrQtzrKfsH4LtBbjv" +
                "mlx/DusUVT7x7lbtlsqtyLG8/5JuNOEQ2Qk/mecOue1mjZsVRi" +
                "B1Zm+I6+b2WX2ZhXugTcr/AqeDuhVaz3rPHsc6xYPe8+Mel78J" +
                "lUnQs3lDXBd3ssFs1bk5Ltgf7Jet3INO9tNtXINYQOOmSyAjQ3" +
                "G+f0vnUz71/WoUqlExHEFHA3i5b82bedWceUweiXJCHtnWmG/M" +
                "q7WXumxLPyGt0AeslGmP2pHRxC6Oi91gAx7OxH2hhbcpVzVHM2" +
                "Nzt6o1Mar8Pl0Rb64hrgayVu5zyxz00+2goRh8o4XbyxilDNb8" +
                "eHybM1AU9eQRuMYUlXOCJ2JbZ9Sx6nj00tloBFLHOZq3+L7vgx" +
                "bXw5NbdCu0nnOXA9P6n9+7WgxEZRL0bN6t//pzs1l1bhtLOGJY" +
                "7/mMbuWy9XdmxDGf+Za3QiMufxMqk6Bn8y7Xe0ac6z0jrvEgN8" +
                "fV76rfJdtsjzrZT3dILWgQi1IyAxhko7yUszhPXUBjokXPgLKh" +
                "RsVwBGUGTtlX4/IcimP2rBpTjaVmi1Hy3qb6JpTlJuo4ymq+SV" +
                "rQjntTT/UFa3MpjWbDUz4TGjkgK8wLW5CyfXMpzYNymkavjlJ6" +
                "+jMPy7tP5Px4h27lsp/NMLtZ4feuFgNRmQQ9m3drdZX8zVadm+" +
                "MaJxonZCv3oJP9tN04kR6WesTgGy3cztmRs6jjSs5AUdSTR+Aa" +
                "U1TOCZ6Ibd2qjlXHo5fORiPQCua2qcaU2E/BPrdMQT98tDEVPi" +
                "r1iMF31ktmVA7JAkwogdy8nDNQFPXEnNSYMjNXVuiJeWRx+Vh1" +
                "PHrpbDQCqaO0TTemxX4a9rllGvrhkcZ0eETqEYNvtHB7GaOUwZ" +
                "rX8TLOQFHUk0fgGlNUzgmeiM3i8rHqePTS2WgEUsfcRv+vkt4L" +
                "d6XgnzLpfcWdrVn1yQp69wrxXG/7N0yzH2zwf6HRTxnOSEKf3o" +
                "+c8NZRmBPIo6tMT2c0+1WtiVHl9+lk3Kh8/jrcheuP8Px1+hg8" +
                "fx3uyp+/bkskPH9d4NdHh9XnryWbfP5a+53J15Ncz1+Hu0QM7/" +
                "PXOap4/lpIy8V2Vak3PH/dWi2fvxZW6/PXtA45YjvX5tw7qUY+" +
                "f92YbEyK43IS9vmROgn9YB9oKAbfaOH28pgvZbDm1+EHOQNFUU" +
                "8egWtMUTkneCJWjkbnpXj00tloBPK9lraJxoTYT8A+t0xAP/0A" +
                "aCgG32jh9jJGKYM1r2PKGSiKevIIXGOKyjnBE7HpWnWsOh69dD" +
                "YagdRR2o43jov9cdjnluPQT58CDcXgGy3cXsYoZbDmdTzEGSiK" +
                "evIIXGOKyjnBE7HB1epYdTxgeAZ6BFJHaXut8Zq2LpTrsi19SV" +
                "qhD1jUUjxwIaOJXdTx22DjfqYsMBa8KT+NwPPjmRbn5Y+qWj12" +
                "eruahSkztWdCsfnM3c57JB+zr1M477sernX9GnM+ATVm/n/Pyg" +
                "r3KlZ2k1V9Xm5aHTebcPV5/3qP7kfq+Ijbbta4WWEEUmf2Dgb8" +
                "3MGAO7I6+lK/rr5OtvV1VCf76T+lFjSIRSmZAQyyUV7KWdTxxz" +
                "QmWvQMKBtqVAxHUGbglP1guW5V8wQMHSePpWaLUcR+Q/YuM9wg" +
                "N1HHM1xHW9DL41H2OQf7vEiE9nfqnn/QABajmDGIgh5myBHZPr" +
                "jSxE11GQZZaHz6tudkPT+GFe4kd35+fKLX/w0x379Ow95FxLkZ" +
                "n83JfrCGzgspFmeCyQyfO5rnhTRC+4cqnz4vVKPo80KeObfLeS" +
                "HNCEej89KZLZ8DA58tB4L/P5OC8qw=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 3223;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGusXFUVHjA2IGq5tHJp2lurBopoQ0FFDYrn3jNTaGm1pg" +
                "IlqGmrttU+BAVjYmK659zWmYhD01xvKJAKotGoEb1WeeMj+Iqm" +
                "if7ozwbSxh+9pD/6o+b+c++zznfWWnvvc2Zuy8js7Nda3/rWWv" +
                "uc3TmPO2003CdZ7kqj+LgRzVp7tUy2LOdec8hP6KHuA6zvJc6I" +
                "qBAXy+UI2fjcUiaz11nKEovJ3G/eZNs3mwXmklL2VvN2s9Assp" +
                "7vNYtzyZW2LjXLsnW2X2Heba4219jRSluvtfW6HPN+W1flo9Ul" +
                "04fMR4rRx01ibinlLVvXmNvMrbZfa24PV8ncbT5nPm+22NFWW7" +
                "eZL5qd5svmInNxibjUvMW2bzOXmytsP2LeYUbNEnOV0+1bbt5p" +
                "Ze+y9b059npzQ57NjXZ8k/mw+ajtb7b1Y+YTZsL2qWk2GtnaHO" +
                "tyXG/rBls/aT6Vyz5t62fMHeYus9ncYz6rIv2CrV8q1naFK+UR" +
                "WUG10eis1TLZspx7zaHOnsBD7fm4QvPF8MyIqBAXy+XIZnNbjF" +
                "vKZPY6S1liMSVLk6XUUg8ZzbtXagljfSuWhHNmoPHED7Q1NND7" +
                "/uQ8htGIOLPrfb8aydlrn9pTGG2BGEvGbD+GPteMYd5dlowdOM" +
                "QSxvpWLAnnzFms4xFtDQ30vj85B+Y7F2tZONbMrvf9Ytx+XGev" +
                "fWrvYbRsic/4vdS3T0KSbdB7CYgL+zQPNob8icc5TL/J6mQ1tc" +
                "lqKaN59waSQsJYaQUMs0leyVnk86C2hiaMQLKxxMdohI6L4w/9" +
                "6hiYV/v0ffnRshf/I87HTX2O+5qBzg4P1XxoiGfimuq4huk3fT" +
                "h9mFrqIaN59yZIJIYLa7Res2tOm8/3NINESct4kVFWR8WxMdb5" +
                "1bmGeLYK2aQHuYK5biqdsv0U+lwzRSU71r0FEonhwhqtL32UY7" +
                "aw+fQ0g0RJy3iRUVZHxbEx1vnVuYZ4tgrZpAexjlMybqU5glH3" +
                "A1Wa2nO8D6r1q0F3S/eDjsvn6x9FHDG43/PY19PpNLXUQ0bzbg" +
                "qJxHBhjdZrds1pr4fv1AwSJS3jRUZZHRXHxtjOHX6uIZ6tQjbp" +
                "Qa6gjFsdt7Pl+dCs0tSeb31QrZnBj/NgHv3PvuUX6nfe5+Oj6a" +
                "PUUg8ZzScWQSIxXFij9Zpdc8qZtIMfH+EXGWV1VBwbYykbP3KN" +
                "Z6uQTXqQKyjjVnfeD2DU2eppfjbIsTGrX7/jbH4Rl+9fW2e1/9" +
                "aYNPv1EM/HR9JHqKUeMpp3tkEiMVxYo/WaXXPKmbSDHx/hFxll" +
                "dVQcG2MPbPJzDfFsFbJJD3IFOe7xK6gG1+F3ele5OY6RA18dK4" +
                "vm8/X6uMQ7t+7SGGRAsrg1/NZz99MOnr1Yx3v6o7NT876/HuL1" +
                "R933zDD9pofTw9RSD5kr9jp8DSQSw4U1Wq/ZJWeez281g0RJy3" +
                "iRUVZHxbEx1vnVuYZ4tgrZpAe5gjLu9iJxhm1p58/WsmPZ1mEc" +
                "u+ZLF85xYLZ2R103LL/V12e4Qmsu0TJXuxtIizmwLNUzLZfsKp" +
                "/fQUc2rbP7NvoxsC1alKr44d9x7FuuY4NfXxpj7Hf92lyiZeS3" +
                "ubK50upWos+RK6nYfb0DEonhwhqtL32WY2jz8TOaQaKkpfagJd" +
                "2doVfNCUuOw/nVuYZ4tgrZpAexrqRb2iyejzfLJ/Ykc7W7m6SY" +
                "a2w403IwBUd0BjqyicXAtmhRPC5lyzXKOONLY4w+fyjRssLfaH" +
                "PU9qPoc80oFXs+7oVEYriwRutLH+UY2nz8omaQKGmpPWhJzKvm" +
                "hKXAvujnGuLZKmSTHsQ6jsq41ffMjnax4tnOoXzPHH2DvmeODv" +
                "G6ZyadoZZ6yGje/TokEsOFNVqv2TWnzeeXmkGipKX2oCUxr5oT" +
                "lox1fnWuIZ6tQjbpQa6gjFtpyuPW/VaVpvbYHK3XNId4n1sXAf" +
                "tNjzb+Dx/cz9jrx71D2dcvvEH3M0P0m4wmo9RSDxnNu5Nawljf" +
                "iiXhnBmKfJ7S1tAkozIqn50lPkYj4syu9/1qJGevfWpPYbQFYk" +
                "lir1tdS32uWULFfl/v1xLG+lYsCefMUKzjs9oamqS8Ctf+5DyG" +
                "0Yg4s+t9vxrJ2Wuf2lMYLVvWPKf4hve0Y0f9vOIZyQ6Naz49xP" +
                "euO6rjGqbf9GR6klrqIaN5twuJxHBhjdZrduYs8vmNZpAoaak9" +
                "aEnMq+aEJWOdX51riGerkE16kCso4644H7/pHe319fOKc2S9xj" +
                "WfG+L5uL46rmH6TV5LXqOWesho3vyhljAWaK7hCGNmkF41XxiV" +
                "9iIlPkYjZDbAczYxXj9m7VN7kpyII0ecSc7Y/gz6XHMG8852LW" +
                "Es0FzDEcbMUK5BwOet4xnfi5T4GI2Q2QBP/YFNcV4/Zu1Te5Kc" +
                "iCNHzCb2Dsu11OeaWcy7D2oJY4HmGo4wZoZyDQI+bx1nfS9S4m" +
                "M0QmYDPPWdnXFeP2btU3uSnIiDx/lztD1os++W9zNPh1q0fZ5r" +
                "7rmQf21aewbzwSg3wqzKOuv2jy3rxbUhdwyX/JdqwXVROToYwz" +
                "Fy4H+FlUXrsXp9XFLPigxIFreG33ruftq4Pp1NZ6mlHjKaN5+A" +
                "RGK4sEbrNTtzSrnmg9xHSN9hlNVRsSVjKZuQV+LZKmSTHuQKyr" +
                "jzq4X7guse7++ogLiwT2f3sO+v43F2dg3/2URrV2uXa2lMNV2Y" +
                "Lmw0Jnf7GEZyH5v5/NCmC1u7+scj+UJ85yvMh6h0BpwHYnbZYC" +
                "w5248zF1v51uRBIoOYe8Ubnx7NyrdavVzTIzkw/BaskPXCt0Jg" +
                "Em35Rsnxxd4tld7Ja+RNmoi2hzgQA781w/Me5IHsZCTQglFw9Y" +
                "hNvNvr8fu40ktPR80f9Tukb9tKx26aaiFfln2Nfodk22sK2bWK" +
                "ZVXVsZK/Q+rsdb9Dqv+Yu/N2SzHbFqz6NP0OKdeWv0Oy/VX83M" +
                "z/HVIn/4Z1v0PK+5uFt/x3SMVY/Q4pl7ysf4cUPRc3tzZPnHYt" +
                "9Y0GZm7e/U9r8+TlLGGsQ1IlKynRTOyHxhOnJV/nq9BMnIZW6p" +
                "mTI3C4yREt0xY6K0Tn5wGk3delJyeXOs0tY0A2sLTjc3TWTpxz" +
                "5yy1NHKayd2uxY5wWr2voZX7j2Sy5X3tWGP7GmjyWr2vnXbiXO" +
                "c+ua8pKr2vkQeyk5FAW/77eJYjxzrwKmDM+5pzltmnC2hfpwvM" +
                "Y26GfZ0ucBrSF/v6dqv19jW0cl9LO2p5XzvW2L4GmrxW72unzR" +
                "FiX1vGYF9bWb6vc2y+r4tI8n1ttdF9beXrbBX7Orf39jXnjFHz" +
                "suZlySuupd5eaRYzN2cJIRlLxUlJAj0Y82vWV3gMrZNKDtY4HP" +
                "mkEet5hgiAqI4K2Uisn4eOmTwRu/Sp84OsvC4HdnFzcVK87aI+" +
                "mXEyq7NyNyaJm5MsX5XF3JIWehpJpsR7l+ZYCQU/1MNG6qUvFK" +
                "dlTrZl/yQFC2MRqcwDXhgj2bV9TMZr036PrVe3F7VXdYvnc9kR" +
                "vL/uPj/AW7FllZrrI7IV87mq7Va+mWqPzv/9def+CvSYN3/fPN" +
                "8L3lis6YnxEXsPMJKcoBn+rjk5AUl5nzDisA4tjuCJyF3oCd1K" +
                "C7Cq+48RRpNe+/C5CcsYZAAZ88n4MJLakB3rEM+Psg858/Pvp9" +
                "61W/n34ek6bou1/1fw/bCu/nxM12VPlJ5+0v53nUU/RkZkT9ZZ" +
                "tI+dHy9h9PlYbZX9KPtx7Xl6qET+vL/n+f897oHX4a9dzufv7D" +
                "sPDPE916n0VPIq9/ZMfdWNae7GJCGEk0HrJK6SFjMaSSYaw8ZJ" +
                "mYN5nRxaxCO9cHFaIMKoEAHi4uzCPNg37AgDnVydMAbGI++Jud" +
                "ZxVybmMLNXncfdCBKaE8a1NIeERtyCQ/ORHbECp67D56CFD+aT" +
                "eIejiBiFDDhOnRUkiBlRlfcPx2XkvAKwIiYUXhFwVuzU8jluuj" +
                "HQPRWc0Rv7nPEb52sxeWnfXbTxfPZ1P79xzOQl83xmdwjtePnv" +
                "Y+sw1VK7ncc8r2YDSuOkrsq6P0ajELfzZOXb62Kq467yzLb9WM" +
                "YPoh0v3ya0vk+11G7gMc+r2YDSuPG+/7/B+MF+GBeVRCFu58nK" +
                "N9TFVMdd5ZltB2Gx+/Ylvu7JjqX57+pT8ev67J/BTlC/vc/+VI" +
                "6eK6571mTPlLLf59c9Ff+LQPZC9sfA359t/YeHe9ZyPlkdg3/d" +
                "k/0hb//qo7LgLiP7m8N41z3RaLOXbf1L9nfv6nOOanHfMx2OJC" +
                "6Zm/d7rrkqRl9fLfHuzaY1BhmQLG4N3/Xc/bQV+v8Bazrt8g==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 2569;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXGusHVUVPiQEDVWE+OPGcFGUmCYVJZGEBhqaMzPniNAX6U" +
                "Pbmqgk/JPExKT2SkLiPWc3HE/ivRr7A01MasBENJFfRqrEB0EQ" +
                "RZRSCyqXRiixrb2gWGOtP3T2rFl7rbVfM+fcmV5kJntm77W+9a" +
                "219tmz53lvp+Mu8/uwNnxWapKt8bZ/SbYSbjDfaWQZ/j7kKxzX" +
                "8HedVpfebaG2rWnH3zSeDtwa1d5S129zy+Ca7vHB+wfvHHywe1" +
                "y3u8fn9w2uhBpKCM3rIQnKXOvc29XIGrIJ6Tkuj3gmFsNgHclI" +
                "F8pjcJXNXi8/WRtck5eiH+m4hn7sdL70rxq/w2xQc51HdnWnM/" +
                "pCA7/+TFS7zicdfSaAvspqf2DCWD5c9OixZK57TK/JXDG3FC2o" +
                "oYTNP3POjDTnmaXm7K35/Y4ha8gmpOc4HWMsBq1FGelCeXAuYK" +
                "+Xn6xFx+O/2xiP6a8bOM+cnHw8hvw2MR77R/tHs5v1FvadDrZ0" +
                "mySA7B9Vb0etXrUU8KiHvbpM84Mmu1nXUUtyjgcvwKZXlIKeWh" +
                "glIlDDGVGKUROW58HrxAUYrrPzQxn2IWLj4zFVbYzHRq57phmP" +
                "qtX58WFd9GrmC1Mfv62ahezqLBo9Wlx5P8a9+rWjhSYyCF/3hM" +
                "fj+Mo2xmP/G6tzvg75Xel4LH+Nw7rolSRYH7+nnv1k3voPNDAe" +
                "D0+uDfmdLIOpxuN7VzIevWg9Hp9cpfH4ZL3xOOUv+4QueiUJ1n" +
                "u/qmdfXwPemol6Um0oGxs9bXzJfr5P9g8+h22UuNiYhJh8emQN" +
                "2YT0HJfsl/fXtoW+v5ZZVefB2evlF+iPheSzegulPK7XkZYjxR" +
                "XICW88C5GeWKj8ZReqMRLF44ZMwn7jsfm1jNvLomaj8+O1rZyv" +
                "n1ql+fGpNq8f82vytHsunxXOZSm08PljlqLEzBwFTm9pIa1zrf" +
                "wa6slCP390LbQefYFX6cP2B1jCYAYoIz7uDWtc67J3zw1fDecH" +
                "2buche4U33dP9XrYRomLjUmIyadH1pBNSM9xFKM/Bq2VWVXnwd" +
                "nr5RfjgePa9PbmYly9ZGaJ7dassR0x4SXbnGwnu8E76lhUHUnZ" +
                "5sEmvwV4suOsy+vD1LESfbKI28Tct41vlFq3HjzPLFZ5iseSLN" +
                "aJmFA87mTxi++O+Y3H5teSbRXL/FvNuceccXqPW2yfKJDv421/" +
                "P0orsDP8H6ruo/k9NTCXsvoVeZnBCKU/c399X4ldH2WNvn2Y3z" +
                "1/l/fadGNvY3a93sI+H8dlS7dJAkjCwqqlIEE9MhZHxPVURy3J" +
                "OR68AJteUQp6amEEiAhHhdlwrJ2HjBk8ATv3KfNDmTnqEbult6" +
                "W7pLewz2fOsqXbJMmxWzgWVi0FCeph310q5uAlqqNWSzkHaTQO" +
                "fEKN9NTCCBARjgqz4Vg7DxkzeAJ27lPmhzJznllCy+I4uBxKeS" +
                "542RyBN3U66RF2hBY4Qtaee4UFZ/Tp/RLrWeIRicEMQOa3Rr+2" +
                "VsYT9zxJ9ux87bwHHjzjnM+2xq/DpX5wJGZRxUgItUHMuBdZnp" +
                "+ejhcw8jo8q/WOvr+2v7Z7Um/1vhipZUu3dZ1LCKvlJAEmkOgt" +
                "oHCLfgCHrJIFvICW+yNeikCjbJm0oLgAzz1ILVmUx+lJqSN7Ow" +
                "bMBmMsRutboDjH9UZrVBc4Qoave1w79tsdiuv9kjgrZgAyvzX6" +
                "jXNXacP6bH33fN6z57P10DL3hetRYmbV8xqr0dzaxyi3VRZaj2" +
                "jwKi1sa8ASBjNAGfFxb1jjWl9fSJ1sQfYuZzqTr8vFttjnEmzp" +
                "dl5XdzEJYTWyKKUVl0imZeOneLYArJyl3C8braVHDEagUaM5KX" +
                "MZKSsTnZUHIgeHmKdlqZPcIgbMZsbUT5d9ehq26hZsq4/mx/bl" +
                "qOfYmISYXI26NT3N5aOv2jZSH+JWV8RiUB+xs7JRvD44FMsonJ" +
                "+sxZ6bZV9p5blZA98BTPXcrMXvALy/+Dd5jVr5LGvdrSV7qhgA" +
                "JXFcK5H1l5AdePLHFbKZNgZxBNyhdqk9aoeQmbvT3pRvKtTHg5" +
                "pP5uVTDcT9sah2r/f++us1eHd3GlvUneZ5j/W0M7k93g6Mkdsl" +
                "bnSw09oCnvxxjb7WuWDL8Cd0P5OtydZU389Y9o+a2o9wfhya7x" +
                "WGPy3uZ9YEbB8Z/tyR/SIv1rw2PJyX+6MzlrifGf6s2D5h+x3+" +
                "2PH2y7rz4/CxvDw+jL5BTu9wa+WvvS3eDoyRbYSzGaddQjzgyR" +
                "9XU75rn3/K965ZL+tZUe6ItwO57ZA4m3WqGFXIVziuJvxOstBz" +
                "CvV5K8qd8XYgt52Ea+o7+2A/7gzHpfZd4PF4sYnqps4bdJnmO/" +
                "sLnY26x3je0Azj6FtviH7c0Oroi9zPjGp8GbY635FO9Z39/e3d" +
                "z2RnsjPFHFzusQ7t8W2oRSnX8sJbkolzlvk86PI55wTLC62S24" +
                "6KZ8Nj0/vRd6SFtCZeN5dQDBxf3ntu6m+iOpS8H8W7W8DglrAc" +
                "EXxevIk8pJdwbyE85/OhiQOjorhoizW9Ty/hcXBOX/Z2lmBZHX" +
                "lyELeJud8Yb7G0e6lO7TAboiQuqbyfSQ4mNe55OArj1p5y+d5Y" +
                "TDHukGey9bP0dvV2wRb2KIP2+B6UcAytpJF6yU6c5Xi8WDJwFL" +
                "eUHqTE51VyoiVhtV+Zq4snK5eNe+A9yOMWR8zr5sy92MaZbfS9" +
                "tq84/N9T3LvQtt/kBij2dXh6rQ9HyEn4J9PX8cExmAHI/NaYTZ" +
                "y7ShvSR697vvtmuu5RN1Rf96j7pn2OG+3H77+p+jFp7/oRlu5/" +
                "oNjHdf/L0gJwhKy7VFm4+jo+OAYzAJnfGrOJc1dpA9zP9Z+DLe" +
                "xRBu30XSjhGFpJI/WSnTi5XPKh3EZw326U4ajIkrCQjcvL8WTl" +
                "snEPvAeh6OM69Lxn9NCKjr3rAvfvDzbwDOBQVPttrzSrMT8+MO" +
                "3fc0X/TvPu/7/5UT0U/DvNu9ubH9PZdBa2sC/8zZr2WikhLKKp" +
                "uDWsE4PJyOGzMp61vXCJjZEIng3iKRsfrx2z9Ck9cU6Mg+rWL/" +
                "qIOa5/YF097Y63A9dcu5MG38JV+QrHpba151c9k5cXy/rJvDyr" +
                "TuH8qP46Lt6QqrN5+W05hp33XKr4elCV/wdGld8XqL+r1+G4Vk" +
                "+r3xjsscER9cfE818v1J/Ukno53x9Xf1bLqvyL9/Gn1Zlc9g+B" +
                "LGZ5eM+lni9lfxCIF/R7LnUiL68U7b8U29dGP7R8Bv4PTd3jWr" +
                "2q/mbONmf7Z2ELe5RBu3cjSjiGVtJIvWQnznLM/FMycBS3lB6k" +
                "xOdVcqIlYUcP27m6eLJy2bgHdr4+y+P2n6/H9zq/lTMew9/tue" +
                "eZA5fp967pRSs/jiZ572pm1Bp+0wnG44FLD0T/zp/146hGRicm" +
                "Orfp75H/uzr92ITf4PI/sQaPwA==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 2227;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW11oHFUUXkFRipZiBTH9SdNY3VoTa6UqFXSy2aFQa2LVpl" +
                "XjDz4IeZNCn/rSbPba3WxBqw/1SaT+FIWaUkStFKwPBVESNQi1" +
                "lKLUoi9iRX0QEerMnDlzzrl37mQym9ldMsPen3O+c77v3szvnb" +
                "ZQMLfxvdiarOu+yneF1FvlLt2illZmC4WBfwpNbxNvJzLPxFlr" +
                "J1Pq3pAOp5ao61lUr/dbV1le6aN5rKwI57GRgnVl+nn0bGu8ef" +
                "yv+Xms3JzovSPOauOtrMo2jxF+k2A5hCW0gnl8VfPuoDb1YzUf" +
                "Yu0dEsd9tui5MRKFun0mz74jSVNSbhszxdqzqP6wvtP7rVC9eF" +
                "6rdZOH057XSvxN3VWqqDbg8ahuiVCr/fPaXRUTvzasg7NCrZzf" +
                "ea16RO9WeV6r7qC8XedVXWZeH5P6vL5NrddmezP8YCu9YrY4jp" +
                "C2LS6OXae+N/7am+e2xBwjm80RgC0++kBXmtxzedMoC4+5PdF8" +
                "HPR+x1mWES3riGUeD0qUDZd946pMRfF8thibvelreDSP6h6PZY" +
                "qp3Kmp3mnJ8JNE2XBNzOOUZR532nXZYmz2pjVOmq1Q5S6zP3Ep" +
                "KQOgZFzp86YVWjMAk64zPW/tr4Wbx+qpiPn+bBnUvYUO3LKOJj" +
                "Pfa2Yra4ZO2tR9LZ7HQ2Yra4a8tupjGeYxx+OxvKW8BUqo0Qb9" +
                "yTfQwjG0k0f6q7t4dsoZXoNOywwcxXVJBmmJY5U5MZKwL/+gj9" +
                "XEV3frnJKJqyXN5S1u0S16T59FqMNn0SL2J98CK1oIy6MQA5ag" +
                "7CIEzxm2u2Q0ekwFmJ36cRiJkLpIv8krNUhtfJySS1dLLMYz/U" +
                "fRe+E72Y5yd7nsT76b5G3VloZ3IbW5X0bXuY6872a+7rd4NBPR" +
                "W2Pti8U0j9UXWsEy8DqW0ArOx4+l12wnZ5u/DxHpOAjFddti5/" +
                "InMVNsfBa1ja8/qmFt/fFH49lhKM36Y+nFpPXHBXl3zbD+CKqS" +
                "1x/VI82uPzL7v9GV8jD8Qo5nglkWM6Hi32T3GvP/uOg9nU7h+P" +
                "Mh/lnjGn5YPTHn8yI7j9UoqlKY81Hm3a2eEpHPhfzhUaOC9Uw1" +
                "op6c1/Xxqui8/jXb0eJ82InXx1arovWeyT9TzHrMOoVzqSPnsc" +
                "Wq2HeuvzMqvtiR83ixbfNofBkyvyvErpvtib/PwPfCBXk2y/C9" +
                "sLQn5fmY6Xuh9jc74fkr2GtcG+dPcV6fyP3d5I8Mx2POqiaW2a" +
                "6PjWUZz6APOvK8zlFVeWN5I5RQow36jRvRwjG0k0f6nSmenXJy" +
                "VpkP7TqCc5sq7aookrDOlD5WE09RZjbOwGcQI+3Ho6oupvfCiS" +
                "Ut5rspOq8zrn6UXurIdYocVZW3l7dDCTXaoN9YjRaOoZ080l/7" +
                "mmennJxV5kO7juDcpkq7KookbO0rfawmnqLMbJyBz2DgGy17b0" +
                "x+CXXgGcV+o4gWjqGdPNLvHAs5RiGevLwn45BHIji3qdKuiiIJ" +
                "6xzTx2riKcrMxhnYPI5y3dFK2Qx/fmzcbfinU90Z38v7HG1syn" +
                "C/zlFV2Sk7UEJd+xZ7QX+rX/oWtNa+QS9GTFzSc5Qd5xzPji1s" +
                "EwOPQx6J4NzcYirXc2IkYZ1zMkJGE1ZySiY5Do5P+nd7jQeMZ/" +
                "g306ybOT934roZqEpeN5vP+wxfNyuPlceghDqY4THsNx5EC8fQ" +
                "Th7pd46Gf6sxiCcv78k45JEIzm2qtKuiSMI6R/WxmniKMrNxBn" +
                "Y8jnHd8e/XjYcyvjkcL3TglqeqwSuDV0pH/BJq7ykr7Pl9vw0l" +
                "IAkLu28FC/oxY/C8dgTbGAVWnoM8Pg7YoEX5qIcRmMOuCkcD/M" +
                "ggI2Q0MEF2zimZ0BY9kx7ByJh7z9Vma37rj3P87Ubaczym4a1f" +
                "k/mKnXCfKe1PER/3fWZ/J95nbKMR32feX7jvM3R9VMYKSfWGNJ" +
                "mrS4O//z7LcbGvTcfjvnSYhdJXiZ4P1LE8xlPvbs88HrjcWj42" +
                "j7n8S9V6T3vmsb6mbfOYywpyfe3iOx7dIXcISneI26BfGgcrWg" +
                "jLoxBD2fw46lMGYuDR6DEVYHbqx2EkQuoi/TgaMy/XSVGcj3Pp" +
                "akPssDvs1cNQh55h7Jc2gRUthOVRiAGLbGFWnYFHo8dUwLORRc" +
                "dIhNRF+nE0Zl6uk6I4H+fS1QJ2cHZw1rtDzWId3LNmsV+7iBaO" +
                "oZ080h/d/aI2enlPxiGPRHBuU6VdFUUStjquj9XEU5SZjTOwOz" +
                "z4zg+eN+79gc3/1X4BL/YRS1aJl3b0WJ4wzmNcPAqtWOIej9L1" +
                "SaXh09i4brVx8/xz2Tiv9T7zyWL6PlP7NMdn0wuDF0pn/RJq7y" +
                "oS9vy+34YSkISF3beCBf2YMXh/OIttjAIrz0EeHwds0KJ81MMI" +
                "zGFXhaMBfmSQETIamCA755RMaIvekc5iZOJ7YYonrvn/v+G2vR" +
                "euyW/9MW5Tn0XMvYvpvG71aNj18VRnzsjAb5me/9fl+Bze5/ZB" +
                "6fZxG/Trq8GKFsLyKMRQNp6X5yQGHo0eUwHPRhYdIxFSF+n37t" +
                "cnTa+uk6I4H+fS1YbYfrffq/uhDj392K/3ghUthOVRiAGLbGFW" +
                "nYFHo8dUwLORRcdIhNRF+r15PGN6dZ0Uxfk4l66WWAoFp9vfo/" +
                "X3bvgVCo1t0sZLslMtc9DWeNhkSPwC0C3z6fiB37lmVIW6yM5b" +
                "EGXm5jY+ejlKvtvH4PT4O/Xg512Zt0obL8lOtcyRzJA4jz0yXx" +
                "yeMqIq1EV23sLR6Lm5jY9ejpLv6cYg7jOnc7kmX9eeu1OevO6M" +
                "OwMl1tjzW/X17gyiOEaP4pFmnzIQq+REFq5Kz04WHSMR8ZmD0R" +
                "TjVEvdaDfHqWugkQWIaXfaq6epxp7fqve7wb9DQYv0Us0jzT5l" +
                "iOZA40QW8nMW3o/DSER85mA0fXGqpW60m+PUNdDIuHLLeX0mj3" +
                "OgfLk953WuvP8DHiWORg==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 2348;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW01sFVUUfoC/aLGKgmCVRkMtiqJgCyUmvnbeiEqILjVRE6" +
                "MmElmJIAsXzHstoZnGjRI1YUGiLlyw0JUuQBckbkwREjVq1AW1" +
                "MUFoKaL8iDp3zpw559yf96Z9nTcGZ3L/zv3O951738y7M7evpZ" +
                "J5VI9hbeiLUg6HXyrmyFO33FPugVyVZIO2XwYrWgjLvRBDbJyX" +
                "c5IC98YeMwLORhYdIxEyLoofR2Py8jjJi+txLT3aBNtb7o3KXi" +
                "iTnl5sh5vBihbCci/EgEXWkJUr+NdKb+wxI+BsZNExEiHjovhN" +
                "XRkD8UpNXUuPFrDeXm9vqaRyKFUPtFQ9fB0tHEMn9ch+jInq5B" +
                "GN52rJwFHc037yKN1RUWyEVbpyrCaevEw2rkAHeeqH9yHWwmFX" +
                "Dx21Mbe/43vqsuzfO424puM1Hd3pH9U7orS8urB6D1qCbdVb4h" +
                "kaDfdk8O9w9qyy2DpLpcpvsxD14rq9d9msLt3qrVr77mnGstph" +
                "p/X6yzw+ucrvxazXrdZl8ziay3hONM8xdLgYXec3yX5vP+RQog" +
                "3a4QhaOIZO1aqN6RzgQ+ySMxrPKcnAUdzTfvIo3VEhjmOVrhyr" +
                "iScvk40r8BnkcfMj2JauM281/iTMdabhdXGhoPs6V121zjjn8e" +
                "2mmFc5xvNn81EP7qvb+8F0dPk6M/j+9NeZmHt5ZTnWpE2l8B2w" +
                "YltizZa0c3aheRb7wMcWA/lijme9+ClZGc/qVhujzu+eMalruV" +
                "PnpNfju9bv+J+bfM+9IsM68mMGzLes/l2UfsqmO/R9XdZv6vb+" +
                "4JjvjkoH1qRNpfAjsGJbYs2WtHN2MZ552Ac+thjIF3M868VPyc" +
                "boz9OtNkad3z1jXDd4NZhnfj8GC6J8YTSPHwc3xpZFUYqezYPX" +
                "EkR8HQd3Jq34qTdYGaX4WT64L2XqCdZZYjsT9z0cPBJsiMpHg4" +
                "3W1e6Z4Nm4fC7OXwheCjYHc4K5GqotaE9qN0VpSWpflpQruG6Q" +
                "PDEHfUn5YPBQMFBvHoPHLbanRev5KL1Y6a50RwrdWMZ63dgOP0" +
                "ULx9BJPbI/jTytY29cPy0ZOIp7SgVpsalKTvRk2NP6WE08eZls" +
                "XIF9OnGfP+VPJe2l6fUf21QKP4NebCOWrLIl7cgU8XwulP/CPv" +
                "Dxp3Y+ocdAvpjjqX1HTPEoVFIcO2+TsaGubrUx6vzG3bRU2kC3" +
                "sqISXfkqhzJGrsB2eAgtHEMn9cj+VDOtY2+sPEcycBT3lArSYl" +
                "OVnOhJWKUrx2riyctk4wpsXlfwuB3r9eFc1uu2rOt1+NWsrtdt" +
                "+a3XfL8nPCL3e6JxHM1lv2eyoP2eydbu9wSfpNfj8TzeC/0FBf" +
                "19Jkdd75B3CHIo0QbtcBItHEOnatXGdA7wIXbJGY3nGsnAUdyT" +
                "YtI1bZHrnOhJWKUrx2riyctk4wp8Bnncjn2zX3PZL/ijoH2KXH" +
                "Xr7lOcbua+du5TnC9on+J8fvsU/gH/AORYYkvVwlMqJ4vshbI2" +
                "Jj1VLtvEkIznnK6JKjwq6udtG0Yi7Myq1HXNcWMUtnHqMdDIeO" +
                "SO+/p4LvfX3wXd1znq+gf9g5BjiS1VC8+qnCyyF8roehSeKpdt" +
                "Ykjq83VNVOFRUT9v2zASYWeO25quOW6MwjZOPQYamUreuDcerT" +
                "fjWMYr0Di2w3No4Rg6VStar8f1/nQtS+vYG18XU5KBo7gnxaRr" +
                "ErcrKvIkrNKVYzXx5GWycQW2Xo/zuB339UQu99fxgu7rHHXLJ8" +
                "onIIcSbdAOL0gLYRFNyaxhnRiSO+Fyk8+MSqpwi46RCD4axEOp" +
                "60okxSw1pRLnxDiorj3JhFgbmVu6hI6hDO+jtTdmUS99aqysy+" +
                "O9sHKyoPs6g+7QmVmcx7Pp9Xj9f/baOjcDnwx/pxya4TtCebI8" +
                "CTmUaIP2yA3SQlhEUzJrWI94FqJPfF1cNPnMqKQKt+gYieCjQT" +
                "yUuq5EUsxSUypxTowjRkyUozVZ5VDGPRPYHumWFsIimpJZwzox" +
                "JOvMXJNPm8cJXYVbdIxE8NEgHkpdVyIpZqkplTgnxkF11z7u0M" +
                "Vcnv2vLGjfLEfd/jX9ayCHEm3QHlkpLYRFdP+a2hha0SKZSAfr" +
                "lX9MPjMqqcItOkYi+GgQD6WuK5EUs9SUSpwT46B6vMtzVbrnkf" +
                "4S0t9h3ee9vbnPbtfCxpjgqQyY+awerYjB4gbXYzKaYG1d1sfq" +
                "9j4ZbMm8k5a+z+xalMc9MNxTzH29+80c98NPeichhxJt0B5ejR" +
                "aOoZN6ZL9kJ05ul3xo1xFc24zSHRV5EnbwjD5WE09eJhtX4DPI" +
                "4/a3+FtUDvW0Jn4fjhjqp9LW0n2p1294b1AkLjzxYVRyBGIc0D" +
                "fM/TinHhtPfNxyFA2/v9JfdwyvvZTeCwdb/LtL9v3Ykcv72bGC" +
                "3gtbrLvrtrS2LJfx/FLQPLZYt/pKel+vu6Tu66C1evT3wpGbm/" +
                "o8bL+nOBpdF5l3iEaWOHeZ3qurbP0/i6y6M/l7YYN5vNfQOJJ9" +
                "38w1j8MDzcc4k3nc/XJr5xHva6/HM56YvUzP0IByYb2CnsOz6s" +
                "5WfHg9en1en6HRlymSPhe2el1Wjgaf9cYZzGPf7OKsz72b/E1U" +
                "h+St99brGG89IBFNXrwFftxbeVEvV3PFw/lsaOKAeHhclGMNxs" +
                "Pj4Jxy9Co+7s0VGkfOrsd0t2x4Qy7PVQ/8T9brZB69Lq/LuOa7" +
                "Mt0ZXW6sjbVF349ds4vjx0D7QDvkUKJNnbVRrywtA+2DbVgDNC" +
                "VeG1ygasp7oJ10sA52YqEeHpVU4RYdIxF8NIiHUteVSBo9jNrG" +
                "zWOgkVHdcT12ep3R3SDey5Ulwyfa6cYCa9N7b/0zuB4dutV92X" +
                "AZVpmV6qQWpOj5cZW08Rzt8PwIbcnhUqgM6r22eDiLDU+MGBXG" +
                "Je3EVxm0cXMb95Kj5KctpvLW8lbIoUSbOmujI/dLC2ERTcmsYZ" +
                "0YknncZfLJOZL9vE1IabMxEh5KXVciKWapKZU4J8ZBdfvzYzSP" +
                "GVbW6f0OoBqtmZVZWDdrX89gnyLH9bqyp7IHcijRps5oHnvRwj" +
                "F0Uo/sl+zEmdS3SwaO4p5SQVpsqpITPRl2uz5WE09eJhtX4DPI" +
                "47a9F0bz2JfLZ7ejoH2zPHX/BWRk+hM=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1432;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXDuMHEUQncAhwgEBgY1lCUiwEBIpCM2sbgMEcgAmcgDCNo" +
                "hfANzJurV9vpv17nnERhjEVxiEhAgQCeJA+P856zikEyCtTiYi" +
                "cEBG4NgSW+7rqfn1TFVv9TS6KfVebb2qrura6ume7dUFQfxgEM" +
                "QPx/fFjwZb19JcvEtxo6eCxivebUQeq5DtDYKZuWDqK76/Fn2k" +
                "SmryGz9QeL+PGcvj1fKlLX/9jVEnEL7inZPxDAT6eYZvI+GXc2" +
                "Xy2HWSx3lPeZz3lseDzdr9W6zxL03G0w+8XG37XUrvI6PXZfI4" +
                "eiNXjz1P9djzVo9vOpnXi57yuNhuHk+dSPO4ID2v745nwdO8Xv" +
                "A2r+NSHeyf8v4I9Tjruh4X91TmcdbbvH7PybyOPc3r2FseR6X4" +
                "/2CMtWof/udkPCemj7H/da3njco8Ev1y9+GEef2++L4H6vG4p3" +
                "o83m49xu+k9fiBk3XmpKd15qS3PH7oJI/LnvLo1G/4CzQglGi+" +
                "8yzNno4obzJRc1HTaIraEvEV1pmPnNwfj3m6Px5rt/4zefxE/J" +
                "4Bz9dHBdbrsUUejzq8Z5yZOaNe1V8tU++Tp7Ukq4OESB7P9459" +
                "ZuX5/rS8qJH1XY7SHBVaou7gieJYy/poVe4t6yGbwWzc1fuezn" +
                "Mu6lHisqlH+dFQ94/JfhfPMyL9WNwfBx1feRx96mLfkzzvZ9+T" +
                "OK3H/PnM6Y3c+cxnBPuK85nwG+Nz4d4gGB4UiNrifEZFVaGdOZ" +
                "859bH8+cwkj587qYsXPNXjgRbr8fdsPZLsq+rxZl09ikRtU483" +
                "263H4WHNRWsyox68ZkRetayt2nvD8FCVVGo0hqeiNWhAKAnXeP" +
                "Z0RHmTiZqLRsNmbdDhxqf0634HEK3azest5H82r02jkfkdQLgJ" +
                "DSjNb8onLxE+jU0OYtZm1sAmHx28RdPmxgj63XF3rF7VX5Crd8" +
                "BHf2lJVgcJ3vVvFfvops8ayGs0+y5vp/3kNbK+y1Gao0JL1FWj" +
                "Kfeb1Uercm9ZD3hp/bp5bbte18/raIeneb3D5byuW69HZ508z7" +
                "zieqdYvV4nR9rbP7ZRj+Edge8p/rHYP95xus6sQANK/aV850nC" +
                "XXaFdU9e4VrYeK1GTaORiGfSy0/QgFCi+U6HZs/1JhM1FzWNRi" +
                "Iel/O6UnsyrxOB76Vt1pnT39LmteUnew0aEEo0n8zT7OmI8iYT" +
                "NRcdJDRtu/jC69CAUKL5aJZmT0eUN4E8XuejptEUte3iCy9AA0" +
                "KJ5pMhzZ6OKG8CebzARwdf0bTt4gtvQANCCfI0ezqivAnk8QYf" +
                "jQ43a4OOXXzhZWhAKEGeZk9HlDeBPF7mo9GhZm3QsYuv/X14Iv" +
                "B7M5v1evm2m3341qdxERoQSpCn2dMR5U2gHi/y0ejlZm3QsYvP" +
                "Qz3G27Ier0IDQgnyNHs6orwJ1ONVPpp80aydnLWNLzwHDQglyN" +
                "Ps6YjyJpDHc3w0erFZG3Ts4gsvQQNCCfI0ezqivAnk8RIfjY40" +
                "a4OOXXzhFWhAKNF89BDNvgkZ3pv3hjrDeyzzeIWPmkZT1K7v2+" +
                "jzPDQglGg+2kOzpyPKm0A9nuejptEUte3iC1ehAaFE88n3NHs6" +
                "orwJ5HGVjw530rQl4rvrD88VvgyCzt/12vxzhaYep++j+lzBZC" +
                "MRzyT7v0IDQgnyNHs6orzJRM1Fkx9p2tz4lL6H88Jd0+fRZh+e" +
                "fOdmH67OZ9r/Pjza7SePjr8P/wEaEEo0P3ONZs/1JhM1FzWNRi" +
                "Ke+nWGcvHXGfeX4fx6ruUo8Hd7726nPFJGM8UM+Q0aEEqQp9nX" +
                "IVGv7G3amKNefR/VaNSjaXPjU/rhOjSgVJ7y3bcJvaw3I9iP8i" +
                "bw6a/zUdNoitrc+JR++/uemfH0ebT6P0jj7bXvSX72k8flfx3u" +
                "e/4DsTwz+A==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 2150;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXE2I3VQUfisHRapWHMGqCO1sRrQgRQULdTLJSpiZIv5UQc" +
                "ZudNFVVeYhFWleJmPaRWcpLZVXClJrq1gVN4IKLhREcJAKSu1m" +
                "wDKVdiH1p6CYm5OTc869N5kkL5k31oTknnvOd7/vnPtukvfevL" +
                "bTefSjTnyovZNuZHsvdVbdaFz+Rjyg1hl4K+awR/OqaSKfmOVj" +
                "daidPGh7e8uNX3Ue90q1ZrKuGs2rpol8bNv+V6qge8tV+d3rO0" +
                "PZ2tX1N8fHmH+rfx/No78pfQVfLjH+ztzI1mwlZDz+PZ1O+GwD" +
                "Wd9eGB23rsecavy7yA7ejPv3VszlAbs/jDrrfouOVh8zMdsMpu" +
                "wWHsxYt7ZxXQ9rK1NNO9d1qfElrmvmi69r9+Bwrus8XX5dJ/0a" +
                "17XjOz6coVV+6KHNPYRFtOP3ltGLHskEWsQY1/ODySczk3HeJ6" +
                "T02Rh5FarVdSWScpaaUkmfGcR6i95ifAdexDa5Hy9inzwcQ7vq" +
                "9ZZ1DhiDTGShPXFaMnAUH0k56ZrEnZcVjSSs0pW1mngaZbJxBf" +
                "b0WuR513/fU+M+9c6Q7o8t6obHqt0fw/7g98fwrUGzXthU5/4Y" +
                "/dTe/dFb8pbgDG2yUpewr3sIy0foHsmUrv4lsifeN/kQxdH5Ch" +
                "KjI0xmaHVdXjfnlZp2bp4r2dp1/XfB+n27gfd+nw/pPedn19b7" +
                "non3Grgfnax+XefpNnFduyfcE86KOkMbP8PTnuqreHAz+AlDu/" +
                "KCB+PImLwjSCPJe50TaDsrnCP6AiOgpqJgER/1KMvgFq5qZoU4" +
                "rAIr4yPQ9vukhBWRpqyPZiZ917NCI9fyee3fFK+LUw3wPFbjeX" +
                "1qbe8j4ZnONbmFH5TAfFjzu6Qd7g44Q4s+6JOHY2iniIxLduJM" +
                "7aOSgaP4SKkgPTZVyYkjGfaoXquJp1EmG1fgM5jEptypuJ3CNo" +
                "lMYZ88HEM7RWQ808hsYoztw5KBo/hIqSA9NlXJiSMZ9rBeq4mn" +
                "USYbV2DzCLEZdyZuZ7BNIjPYJw/H0E4RGc80MpsYY/uIZOAoPl" +
                "IqSI9NVXLiSIY9otdq4mmUycYV2DxCbNqdjttpbJPINOyTo5Oj" +
                "7nSwB/yEoZ0iMp5pZDZG1aZYOQNH8ZFSQXqiL01VyYkjKQ+lK2" +
                "sFO35ei+qlpqyPZ0s5S0/2LNyV1rthckOc8VdN3+8V6zC2PF2/" +
                "Pzi3M+6Me4fUGdr4M07aU31lw5l8aKmx6oBR3COZvEOo4yTvji" +
                "UfRQCnonocMZiBzsGzIjRVhdnpdUgkKEHFXFMqcU7IF0fGr9NI" +
                "+nqNwDlbjyPKE+zBOMcWeYjJFgfW/DF5cY6bHIm+KZcDj+XVwd" +
                "cjsJflJsvd7m53+uoMbTy3aU/1yQNIwsKuvODBODIma7BPNkaV" +
                "l3OgrfzApnbEAx/1cAQi8rPCajhWr0OOBiVg55qyPvRl13MfR1" +
                "b7PON/V+Ezh/H5en6DvxRrfz34/ah3vFD5W+s9rKRu2c/X8zfM" +
                "35h9mzvujTv/qLNqE7W0p/rKDvaQh7DeOIwFDzCBR50BhWfUAR" +
                "ywkgdb8Koo1yNeykChoiXpkyMoL8BzBRlVu9/H3ADDYzRezwGr" +
                "wRxj+2LKcRHO/m7sK090FuMcW+QhJlscWPPH5MU5zrnYO1cuBx" +
                "7Lq4PfH4G9LLc9mvHurnSFDeHvrs6nbY2KfqyXkXfAOwBnaNEH" +
                "fWU7Z8hDWETTYVpoE2dazxmTz8xKqnCPziERvBqqQrW6LiLATz" +
                "lLTanOOTFXdfiPWNbjQ7W+ydqe43/QeM3PNfg93MMV/qpzR4tX" +
                "yKgzCmdo0Qd93UNYRNNhWmgTQ2qHJp+ZlVThHh0jEbwaqiLph3" +
                "ZePWepKZX0mUHsUNbj+eGsx+jn9taju9PdCWdo0Qd98nAM7RSR" +
                "cclOnOkd5Zhk4Cg+UipIj01VcuJIwipdWauJp1EmG1fgM6gOp+" +
                "t043XZxTZZqV3s6x7CItrp9pbRix7JlK7+Ltnhiyafdl13dRXu" +
                "0TESwauhKlQb/WLn1XOWmlJJn5kMO+fMxe0ctklkDvu6h7CIps" +
                "O00CaGbA4MPm0e53QV7tExEsGroSpUG12w8+o5S02ppM8M4S33" +
                "nL2da3Lzes1grM/rMWcMztCiD/q6h7CIpsO00CYGrir5zKykCv" +
                "foGIng1VAVqvVet/PynBVGakolfWZoBv4/6zG6VAJzuY3fU0RX" +
                "jL9Lfj/47ykaeb9T43dSbxxf/fcU4Sf1fk8RPB88ofuDgX8H7+" +
                "XObfBcUbT8FjxZ+Ffos9Wyqoax5hPPY7AreNw+j879NVmfKprH" +
                "JrbieQyesT4LSlQTPF1z5WyBXfoGXo9biiKD86/GYY+W0a2bmz" +
                "cGu/QNXOVYUWRw/tU47NEyunVza+f+WKC3Jtd1zhwFzWCq3x9r" +
                "r5aNhc+ZjcO5P5bRbSK35t4/+q+uy88z881grM+wbc42OEOLPu" +
                "jrHsIimg7TQpsYuKrkM7OSKtyjYySCV0NVqNbbb+flOSuM1JRK" +
                "+szQDKz9eoz+HM56XLi8tnqt/h53f0M8NZ4H0dX26gqvhivhBf" +
                "hcGP6R+n6t8jt7613m7oTnL4veJYwOmHfu3zvD3+LjSn5Wq2ce" +
                "/r5e1qM3Wj/a4nNmtBlM9e8pSo2v/u9dTw/ne4o83bb+Hbuz27" +
                "Sa2ppirMPTfDWtX9e3FT1n8qPtPmfK6NbNzZl1ZuEMLfqgr3sI" +
                "y0foHmfW20zjSAdtFdX5EMXR+QoSoyNMZmh1XV43eiBzzmjn5r" +
                "mS3fT7x4XriqLuu8N5zrSp6+xz9sEZWvRBX/cQFtF0mBbaxJDW" +
                "c9LkM7OSKtyjYySCV0NVqFbXlUjKWWpKJX1mCG/5lP9alVfiv/" +
                "P/fIQvtEj+LxdK5rw=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1613;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWs1rJEUU77O4RhBUohOERYQJ4ioeJAdJqntE1pNgPsEsih" +
                "DU9eBHwIMbsz3JjPRhDx72KLmEGDx48iALgT3sLLsXPQjCLsuK" +
                "5ORB/wOxq2vevPeqqiednq6uzNjD1MerX/1+773+mOpigsA8uo" +
                "8GIx7z3wdn8HDplbggLqhS1WBTfd2CWEDjl7bmD3Ee6kDbxmd6" +
                "xVWoRcdwBI0Go5C19MrGS73nEencZmYQn5b7fcu+KuNr0JcWVQ" +
                "683zfOxL7l7OzzkjPY+QDNx+3cOibfB1Ph5HZxbnO0dbF1Edvq" +
                "m6KuB0HyJMdAiVjK0me/rivLWThK1eyHGreryKP9ADnAK/QLS2" +
                "jJWnqFfiBnvGeLXo9Szcz3PD6ffp+Pn4hfBMvVzfiZ4k+G+Nnc" +
                "kZcstufSM/P06M+j+Kmho02btTudg25o/dlT+vKKPY/dKbd5rO" +
                "Iok0dx2V0exapYVaWqM71V6OsWxAIav2YL2sig2sm0yadFvKqr" +
                "UIuO4QgaDUYh6+60nVf3mWtyJT0ziLfk99rA1yslVwFXgjN4uP" +
                "QqWoqWVKlqsKk+WigGP7LXPtY51BxkR85+PD9zBoqiM9EnXdPm" +
                "uc4JMxErdXmsJh4w3ANTgWYwG1uJVtJ6BepsZAX6aKEY/Mhems" +
                "cVfXygMWgjY+rrTc5AUXQm+qRripvAnecVzkQ/pC6P1cQnL+ia" +
                "XIl6iz6ntuVoOa2Xoc5GlqGPForBD47w8YHGoI2MaTw3OANF0Z" +
                "lcgVrEDZsq54SZ6IfU5bGaeJxlslEFksdsLLwd3g4CWapajqge" +
                "tKFEDH5kr32sc6g5wIQt6IlbnIGi6Ez0SdcUt4A7zyuciX5IXR" +
                "6ricdZJhtVwKM/1gt7ad2DOhvpQR8tFIMfHOHjA41Bm3Ims5yB" +
                "ouhMrsAtNlXOCTMR253WYzXxOMtkowokjz3qNz2ublbzG9b+x2" +
                "4P3/Xze11EN0lX0e2/K9L76fQjp5nfuu88Y1YPULdYHKMew67H" +
                "cKtQHFvDsMU4HGR3qximnH8e3q9f+3+8X7vOY7jmMo87P+blMU" +
                "93XPMojkZ+wzsqtU9xdHIeu43xyWOy4Oe+/ubjCbuvF/3kMU+X" +
                "XY+z9eQx7BTJo0KZWJnHsGOz15LHzsl5lJiy/rUf13g/G5yduW" +
                "Asj+0Za5yP1OsF5rHQWTi2MPwwdI226mn9WEA3ebXkL19DNFSp" +
                "arCpvm4RjXAbR/mXtiQq9XsbWFAhi2cb+fgI9YqrUIuO4QgaDU" +
                "aB/pi81HuJUd7buM3MYAY8XI/veLoeHepGa9GaKlUNNtVHC1h3" +
                "zsEoztA5UtRjmd9fAQuOop3iEUX94grcYlPlnDATsVKXx2riJY" +
                "bi9Piot+hztHby+7U4zO/lPi0O87HisBhHEYVq5lC7bJfz73Tr" +
                "nt3vKliHv+Fn3dN2uA6PNqINVao6u1I3oC/b4kDZlU0cwCjO0D" +
                "nUnPS8HgALjkor5aAjoM99QAy1ADdnoT2YCWxqDo+VxoS8FKfH" +
                "R71Fn7nFvK/Fm8EEHXVHU9V+eO5v219+8uhWd9jzMXlfR3d+G/" +
                "35WInXJZ6PyXsF3q/fLvt+PfS+PjdR97XDaERLtFSparD1+1Pc" +
                "glhA49dsQRsZqCrnM73iKtSiYziCRgN4jMbGq/vMNbkS5QQ/sF" +
                "3z8/FPT89Hh7rdZYttqVIFY9cv+sNTHh3qtuZac6pUNdjkJ2yG" +
                "zdbczmW0SBu0FBq/tCVR6TtWU1mRM3srbCIH1da94irUkv1mfM" +
                "BtNkbEq1rXBUS8x6NX3tu4qQ8YGbbt+xThTDiTeszWmOFMoR2B" +
                "mXysYh31aD8osU+RoxvvFcO53X8scX997um+rll39H2zE9YIlz" +
                "ytey6d5TyWiGfdUx7XfeVRLDqJZ9FTHhe95dHJDrLwtB+ep5t8" +
                "5DqPC78Ymp+WYdz9MHdko748mtHUtd+zcKfyczRVEc9bJfJ4x9" +
                "d9vXDXuB43x/h6vJtzX3/hPI+/T9J+T93RYB6TLycpj7ufTNb6" +
                "0dcx/281mLOSx+RrP3nM+z9uPddj59vK13G/VpCT+9Xp6vsUY/" +
                "NeeM/TOvyez+tRPKw8nod+OPLmVB9hTc/H2NPz8fxk/V4nbU95" +
                "fNnbPsXrk7TuqTsa5/vhnvYfo/WznMfT74dHnvbDner+B52aeE" +
                "o=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1407;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW82LHEUU7yB6CKIHDUpUkCwiGIzgzYuZqZ0+iJB/YP8Bb6" +
                "LJ5L4zOps04lnQGEhYLy6KyZ68ROJlY5aAe1qCuSXiYRhP2Ut2" +
                "xal5+/q9V9U16e6tmpqPbqar6r1f/d5Hv+6enu5JEnvpfJ4EXV" +
                "ofJVGWSdsNncfsqzh5XPt3vvLYei5SPQa121kaft7qvNR5FyWr" +
                "7c5r0Gt+UmL+607NewWyNz15/cpY7TtFUlc0nTeM8emKvrzvox" +
                "67DyvXxfFI9Xg81nGdfZfM0XLp7fk6P8Zami/7wVTPY/OMda3d" +
                "qMPYe8GpeX6CeTwz2f222s4tL1k5/rPC/ii6zuz48bF7fazle4" +
                "V5XCrp92nv9XjKqsefZ7geTzm+z/60uOfH7EaNPDb8YBb+OqP8" +
                "YBY+j2f9YBY+jx/6wSx6HrObJTCb/uytrc5pPS77wRQtaTttwx" +
                "ZalB2OP5USwiKaPnYP+8TArUo+2ytphUtMjETwaBBP0RTxmj5L" +
                "m9IS50Q/qG8c17+GrYv0lzj1GNJueju93VjXW2iTBEd6rPWNdZ" +
                "ATBlYtAb1ucQQ9PROZyA70JV/6N2pwjl5zbW6FVslteoUeoF8Y" +
                "BUbGZ8jZlBHUIY/LB/QD47buCw+wp7607j9+8HD+7capRzua2f" +
                "69J7sVJ49rJyecx8BxplcinR8D2k17aQ+20KJMr8snlk+kvew3" +
                "khAW0fSxe9gnTuhrVpPP9kpa4RITIxE8GsRDa9qVSPJZ2pSWOC" +
                "f6Qf0J1+P3keoxoF21olZgCy3KYGxKCItotdJ9iFKUSCayg/30" +
                "qs1neyWtcImJkQgeDUWhW9OuRJLP0qa0ZGaG8AX1+HXg68zv83" +
                "edSR+njxuP9Bba4Teiw5EekwSQhIVVS0GCemQcfbd6RH3Uainn" +
                "II3GgU3okZ5G6AEi3F5hNBxrxiF9BkvAzm3K+FCWf39E7F66N2" +
                "z3sB3FvIdjknAMraSR+nwv5X1iHNbjlmTgKD5TWpCSIquSE2cS" +
                "du2kGauNp1k2G7fAKjGfqQaHR/oAtvh8Rg1Qws5cA+sMOyg46w" +
                "7MbX7O2EFW1xyXnuPUQD6fMWfo5zMyqqfHwdnLxWdx9nmr+nke" +
                "+yixseMkxGTrh3nsF80gtEvPcapv5LFfkMe+qRsfB2cvF984nt" +
                "F9YTvgd/ydoe17R+ep87zQh90qi8xjJp6U+njuqrYj5dFhNzNi" +
                "qvvcVe3zVu3j/bXa15LsDuo5dpyEmIr0wOqe49JznNrvPijnA9" +
                "eNj4Ozl+V280ziuE6/jVOPPuy6ltb91n3YQosyGOu+2gQ5YWgl" +
                "jdRLduI83IebkoGj+ExpQUqAw+0VzSQ/9BwZK/SJC33gjDI+7i" +
                "35LCX16rH6e3vptUj310HtjnuPtHWnxPzK75H2Vjx4XeM9Ulc0" +
                "/D3SL76p9x5pa7e1C1toR/Z2cUwSjqGVNFKfe573iXFYF+uSga" +
                "P4TGlBSoqsSk6cSVhtV8Zq42mWzcYtsL2zy/2e5O/h6R+RjusJ" +
                "26XzY2vL9/mx86Kns9HHNa6pW9HyeHeu8ng3Wh63fXNHzeN2rD" +
                "xmf81THi9dj5XH5gfzlEf/0VT5nWJ+8nh58HRM9mB28qiejZNH" +
                "H3br3c+Uml/5fqb5X5z7GZfdUP+LC3l/ParHY5Hq8Vgy0SX0cd" +
                "18Euk682R68ujl9/Bnju5jrd/DS9r19T+k4PV4EKkeD2btuE5f" +
                "HZdHtzZsHsvY9eFb7mPo9x//SaIsl2/NWj2qz6bxe7jbq6nN48" +
                "WpzOPFmcvjhanM44VZy2NjYxrz2NgIlzN1Tp2DLbQog7EpISyi" +
                "6cN76jzNIzvYL+KzvZJWuMTESASPhqIYjc8X83LvZUQmt50Zwh" +
                "fs6yO/R9r4MZnCJahX/wMnstpM");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 2101;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWs+LHFUQ7kU8mIsBNWoSiaIJiZLNogchuWz3dCsEjLPuxY" +
                "Pk5j8geBXcIbPuzkVQCOSUY8iSU4heheQmQhDxYrI5ZA+uilEE" +
                "jcGA3V1T/X313uuZ2cn8SKabea9e1VffV/XS09Mzmyjyj+Rztt" +
                "a/j3Z8gCEY/SqaylGnu3J+BNxryZqMMquvuz6XrK1vwwOsovFi" +
                "q8gDOzi79jmfz6/KqrCnOFo3rS/ECDy6CfFy9bYjl5trQGewnS" +
                "46bA11PXZ6Rq9M6Xq8Mr7rMcj7UaV8dsiKzw4fHeM+Tlg3+dK3" +
                "hmUIHet/TWcfV/dOeB+/8K36o7XViyHIf/WBK7w6uazBjvREek" +
                "JGmdUn6+QT9TAGJyI2nnzC7OBkVcunfhfB2n6V9VUhE1jpxudl" +
                "PLJ8NlbgHSxe2ZHsSBQVYzHLIStZ6wifa7W2gFQ/LGVlheQaa7" +
                "pavh6vBZNcc32+rZnK5+vaGqBtNW09frVQcY9PP97JFR16X/c+" +
                "Gh9M5/44Tt3kZHJSRpnVJ2vXA6yik5OtLfWqxzJBR+3Gez6fX5" +
                "VVYY+LsQjuBl0Us6trkajZalold2eAn8L1uDyl63HCuit/mGvj" +
                "Uv2q9iq/VI9NLg3GMYjCaHLYX9hDcTeTpowyq0/Wpb0BT7FC1L" +
                "7YKlC5taEsUCi9G+DjiK3KqrCny9L0WdxuqIumzTG8G+TZqKoP" +
                "cNud0Vph97weL9Svav91LtRjkwuDcQyiMJoc/l5YYIarb+Xl/H" +
                "Vw5amVo7g/ruyrQ7d/8PL31zIfC/hejKLO0yO49zzbM/pq8PvM" +
                "nRr0C4RZytev7bCW16ezjw9+xHeG2cd4gH1sfz3sPlYq2zqKBZ" +
                "+1rF3b6fZgqzBXvN1PQ+q09emqLrdfvJcycvuqbOooVijq27X1" +
                "bA4XU8RgGkBx3XW5/eK9lJFbx9Lrfd3Z4/KdOfUwvK+Huz92nu" +
                "n/vj7zzjjuj53nBmB5ZPZx7Wj/fSzXI9/H7M9Zuh79biZ1PWbp" +
                "w/l5PeQ+ptN67smSWXpf13Uz/vd1Z/8sXY+dfeN7fmzfa//S/l" +
                "n2sf1Pl+u3+vvjTo72v1UHVaXt30ezj+1fe0VXv9vJ/dHj/ntE" +
                "/9b3KuW3oxk6xtlN3IpbMsqsPllnb1kPsIrGy7fUBgOrWj6/Kq" +
                "vCHhdjEdyN4tFNiNet2WpaJebUOmDXXo/xTF2PE+5m9cfx8nde" +
                "mc4+Tvrv1605mRuHG4fzrg9yrPD0PwQVxgrrA9d4c+c5g+oOU1" +
                "+6mW7KKLP6irNxoHFAPeotPDgRYY4CVdZzQDzgFC9zcET1uQbW" +
                "9qu0qpZTM4GVbnxexhcj49z+uFrUnPvm0/l8nte5jMzrGh7G4E" +
                "TExiuNygYjK3Ae1GwNYc2QquXUTGDjb9xefbxibAW+Au2jxI6l" +
                "+dNyMcpcRo7pGh7G4ETExiuNygYjK3Ae1GwNYc2QquXUTGDjy2" +
                "6vPl4xtgJfgfaxi9/Z7+GP8vfC+Er/7zMFZvTfC+PrM7WP18f5" +
                "/TrwWVj9HSr+dpaeH8fZTXI8OS6jzOqTdfyT9QCraLx8S20wsK" +
                "rl86uyKuxxMRbB3Sge3YR43ZqtplViTq2jRCwmi/m8qHMZWdS1" +
                "6wFW0Xj5ltpgqPbA43P2cdFVYY+LsQjuBl0Uc/xYmJdrLjBW0y" +
                "q5O4Md6PW+nq0jnhsNJvgcHqexjDKrT9bwMAYnIjZu2cEp9uJ/" +
                "loFRnGkVrCekajk1E9hC1/bq45Hls7EC72AZS9IknxOdy0iia3" +
                "gYgxMRG680KhuM+TfNNywDozjTKlhPSNVyaiawax+6vfp4ZPls" +
                "rED7WMayu9ndKCpGmcvfRe7qurCT2+IHBiciNl79wlLZYMzvJr" +
                "ctA6M40ypYj3DUV4VM1FHk2F7FBpfWwIy2P64WNee+Q9mhfD4k" +
                "czdySNfpm+JlDKLyam0xRi0wMSf8Lp/3G5ejgtNyu1VpBcjEun" +
                "PcZthsWxtXUF8D43s9hyeNWXoOr+tmbM/hR2b08/r0aDAD/7ZX" +
                "7WPn1CztY/vJ8XGnUdodZY6qldo6pgbLGakXt+wRRSOjkDoRHx" +
                "HVaIZUU6cGFxs/7/bq4xUTBdhSszMR1Zz2+51i17jvj513J/g7" +
                "xa7x3R8b9xv3vd/VS1/x6ixJVNeKFZtXHAdjiJ1jymOZbK6Oeo" +
                "ZqdWsMM3aarjfE6PL384leOpfm34SKUebySp3TdfyEehiDExEb" +
                "r675ytYor2ye6lgEa/tV1leFTGClG5+X8cjy2ViB3tdlLFvOlv" +
                "Pnn2WZu09Ey1jrCJ9rASMeaymrq8DZVsvX43UIYxG2LtSf7+Me" +
                "P+rWqRh3F+zJ1ULFe7/fVuuzx2fp83r98gB/E35/OO5sKVuSsZ" +
                "jhk3V8VLzqAZazFAM25mVOKHC2RvwKmA0eF2MRti7Ur934vFwn" +
                "sliPtdxqu9hm1sznpszdSFPX8UviVQ+wnKUY8VhLWV0FztaIXw" +
                "GzweNiLMLWhfq1G5+X60QW67GWW61gGzcaN7zPo9JXvOIFiepa" +
                "sfBavPVrpOaT8IbmhVHq1VHPMMqtz1bavfctuN46bebv51Pdyf" +
                "+9ML44pb9zXRzj8+Otxi0ZZS53+Jau4WEMTkRsvPq3qmww5v1s" +
                "WAZGcaZVsJ6QquXUTGALXdurj0eWz8YKdD1KbLNR/I+NTZ3LyK" +
                "au4WEMTkRsvNKobDDmn4mnLQOjONMqWE9I1XJqJrCre91efTyy" +
                "fDZWoH0sY9lCtpDfJxdk7t45F+Rs7G7szhbW98ADLGdJpnqspa" +
                "ysULBytkb8CpgNHhdjEbYuZS5mV1ftlfOW12q6Wm61Xfz/HamF" +
                "CQ==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 2443;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXEuIHFUUbVxlIRERk+APw6gwURnUgbhQmK6aWhmE7P3NQu" +
                "JvJWYhIibdmSSmdy6MIshAFMRIslRjzGdw/KD4QQU3WYiBmd1M" +
                "3ASEEN+tW7fOve+96l+q02asR793373nnnPf6+rumupOsqlsqt" +
                "HIprJ85INn2VR6Y3pjNrXvJXiA1VmcKR5rCatWIFadLZGwAs0G" +
                "Dx2dZ6wvtCVT+EJdsVsLdvVW09YTVguVRmPmLmrCRBZm2qd7+D" +
                "FaDpsPhc6cH/UPwfoqcUapSuqCX1uNxoFbYtzap1dvV6lbVU3J" +
                "xZkJaslFmTnUBFnkObSJevYRhnqe52xFFs/FyxzCxxHOY1bB2S" +
                "okKhpaxeKSi+1zYJSqpC629KrEI5VIhXy0FoSJ/dgByWImaeJD" +
                "1fGj9ZdYBzc2RnB0nm2M5eDzcTRH9lP2U3KZehllRhbZbs9Lj4" +
                "5yLnvYRq+ZksuiQzFiQ4YeRYvjWkUwokco32cz7KqkLq0b1sBK" +
                "5Ncxy61rkNUU2J+zn5NV6mWUGVlku9d16dFRl1162EavmZLVfB" +
                "+LHKe8igw9speiWo/ighE9QrXPWZ/NsKuSurSutt3nTKlEfh2z" +
                "3LoGWQ1jZx6eeTi5RD2NeaSY0Zxs7QGW/PDk7zuFh3pGSc9Rjh" +
                "EbZ1gWVuGo1gMvKiCU77MZqIvxWsFGkVG8P16yMeT7NchqCuz2" +
                "me3JBeppzCPFjOZk73sJHmBntrvs0pPvVOGhnlHSc5RjxM8Zlo" +
                "V1Oar1wIsKCNV5zvpsBupivFawUWruc6aojTE6hny/BlmN1Bge" +
                "6cdV76axSPt8//mDH3Gu5OQosvLPwBeGrPN4epx7HsXHc3g0Bg" +
                "0RG7fsPmdywDJolM6MN11ldVWoDVjStWsN8cgK2bSC3kFdt4kc" +
                "rdz1o309Nz1Q2ZEBnuejQ50dR69Ud/Cj5a4nW3e3bmrdL569u1" +
                "u3DpB/W2VkKuK7s6aqN3eNbovu454K9O0W07p3wFoedM/csfQY" +
                "9zzmz+YxmcOjMWg0a5/3OThHmGDJ7M0fLYNG6cx401VWVyU4jZ" +
                "391l9riEdWyKYV1Ll/TNetj727y3fdVxrr6Dg4wtf17D2z94hl" +
                "fTxHD5+N+ZiQqVqZc2I1IFd6ad3qxyPG2Nnte2OMPn/1jmnd2W" +
                "2z7r2Eeh7zyDaZw6MxaIjYeKlR2mB0n5vXWQaN0plWwXpiqpZT" +
                "MoElXbvWEI+skE0rqH3k2MbZ4F4E+9iPHj4b8zHwC5N/NP+RGO" +
                "fEUeKVXloc5ddnaxNd31ulrfl7+Vg3XUqX3Pvkkoz5O+eSzOHR" +
                "GDREbLxZXM0Ki+XUHMiDmsXHNTuvhqqWUzJRR/MFf60hHlkhm1" +
                "ZQnzMcW0wX3bgoYx5ZlHnynng0Bg0RG0/eKzQWOV9zMqtm0Kiy" +
                "ukDBemKqllMygeXVhLwaj6yQTSuofcxj2ZnsTHKEehllRhbZ3M" +
                "MnlrvWKj1so9dMifuUlKx8F48gA3yM4ajWI5xgJUM4YlWFzITX" +
                "Cn7VgmQlXrHWtEqak1dTYE9np914GqPMyCK78yI8NopRZ4Zz5o" +
                "GdfOZrilZ5NWz09DyGsYg4M42+brhuqSK2Tr8GrIwe6XK67M7L" +
                "ZRnzM3VZ5vBoDBrN3HX4sh8vz/nSBqNW0HlQszXENMFdVRUyge" +
                "285q81xCMrZNMK6nXNsZV0xY0rMuaRFZmTTeejxaAhYuOlRmmD" +
                "0Z0Xn1oGjdKZVsF6YqqWUzKBJV271hCPrJBNK6h9XNF1m+v+h8" +
                "o7TW8Md3U/bN5oj1FWNXNh5gL3PIqP574HWEHjEVpig6F4Bk+E" +
                "fGFVVkV7fIxF6NVgFTT6uhbJns7rvqZV8ndGsM3p5rS7spqWMb" +
                "9enZa57wFW0M3p9nnxiscyFdfA07CTxZDPu1Kf9lW0hxh8X4xR" +
                "r4JGX9ciUbPVtEr+zgDf7b5ZElzN7//t2r1vlmzsfd/swM7h7p" +
                "t138fOB+vp/uOhud77mM9r38fR3MftfDiefaz6HYDex/2fDruP" +
                "kW+uOoOwhN9z9bzr//R4Pq+vtu7I93HnmPZxhLrNB5oPcM+j+H" +
                "hOdvIJPDRD1D60RSj3vv6JsECBvODQ2n5VVkV7mMX6Yox6Fagn" +
                "5JU6BcPVx7jtzkit9EjX0jX3TK3JmD9va0Xb6traoU3sL31raI" +
                "hoDkLlPFvZA87Cu9UyaFR57gQK1tM+F1E1nJIpbFyPXSvbrQVv" +
                "9Vs1o12frhY1O99quurGVRnzyGrRJl0rPKV3soyWGT4HofKsSf" +
                "aAs/BOWgaNKqsLFKwnqmo4JVNhJ/21hvjcntSMVklXi5rT1Wwu" +
                "c1cD1POY38GY45ZOpBPZXP573Dn4xGI0HtoilGOfYC84c90JcG" +
                "ht8w3pnK+iPfkV2UfWF2MEnkdfVxCtBbt6rj7GrWvAymB7n+P4" +
                "/eOO9fR9YXOuHkz0+4ebZ2/mnkfx8bz5i3g0Bg0RG7fsjcb88x" +
                "KFn9v8Lh0JEVo7rLK6KmQCy6sJeTUeWSGbVtA7yI/wOrz9vlyH" +
                "N/+o5zp8/vmqv2fmd1296/Cq1Yz+75nmD+tqH38Y5T52/T3Fl+" +
                "vp/bFzcvQa2Y5sB2x+uGfwyRAjPbAa0Y1fK2Q7etWj+WJocEhV" +
                "qAu9WDTSalCH5oyt3l8lZ1ZX3vV+z6ngXvlT1+79nqrzUb+uDz" +
                "5xpa/r5mHp2fLeWw7H7crrh8PDxQTRnwZQuu6q3F7xbsrI7any" +
                "tvRsxaKhXVnP28PFBNGfBlC67qrcXvFuysjth6W4IjocWnUddT" +
                "EOw1P/anrovVOMR8Sqn3scPPWvpofeu6FVP/fV56l/NV3uyp7S" +
                "14+Rz6bHe+SXvyFqnyitz0rrdOuGLrkn22cD35J7fO/5Pnd1PN" +
                "atij13GPyZvP/mzfc9ni8CtW87Z/vcp6/c4+v2d90w2MfmffU8" +
                "O/vLb+pae73I9UM+378PcZ/ivhG+ht+afYt7HsXH8+ad4tEYNE" +
                "Rs3LLTPkoUfm77r9eREKG1wyqrq0ImsLyakFfjkRWyaQW9g7ru" +
                "+Pm4vo7mo/Vg+j0G28fBv+ca29/Xv46S/X/075Bavf8uJMwIfk" +
                "/x5391H9srg+9j1b+fGfV9s3Rzupn+f4q6XwHp5vG8rqt0WwtX" +
                "zn0w8pvA1suF7pZ0S6NB33PVvJ4tNbzX/Vafbh372ON++CPBvs" +
                "9fu++P4WrC1/W+d4Z/Xacb9JhuKF/XG8hzaJPENbabB0yxOLNW" +
                "51TFNS7d0D7XXw06VrUOfT4ye7/c2hrD783+Hs/52M/vzYY+H/" +
                "8FtFgIpQ==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1962;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW1toHFUYnoSKLop5UCNYbUu1ilmCIvUx4O7sQEHwoYm1Wi" +
                "gRERH6ouCj0M5Ok013X9onHwNiH/JQqT54abFRvCJ9scUbSUFI" +
                "HwsxT9UHdc7888//n9vs7GRmN7t4hj3nP//l+y975swlG8fBVl" +
                "vHHii5cZ5Jnqbfiww1svkgLR63zbabPM0z2dpR6rf5WL996h2c" +
                "I0fXTeMQki73TyGqzcYm53qqhj0GLkvPw4aehk2Ud9Y7Cz2MyI" +
                "M5cbgOHWLW3FAxwIbQCZPzZTzkqxrctx6lPSqyJN32L2quuj5Z" +
                "6WjcAzWyVBuux2ytueEMSWv/Wh62d84750Y9jI6DMzEX9JlJ4J" +
                "MOHYILHJQjomguo1EquByDJK4DaG5MkdyRfLqRVnOdW+tRYTbg" +
                "Hz3IFkj7y+TJdWSZmh/ysKFudI5vxuf6JvTJ/riJHLYzbGp7xa" +
                "Zh/9hUey4zWZC2Tc71dEwbHpel52FDT8PmlP9o+Dng3+dP03nt" +
                "786+ov2HrZKnDLx94fn19/bPo4WV1JimTNzFhyzajyjzam+x+M" +
                "9YYvzIGcnW+iCDzvl82I2Zxow7LnoYwzM+nom5oIMTwCcdOgQX" +
                "OChHxGjvGCcapYLLMUgi9MAnUCSnGUbgjrd/59Z6VJgN+EcPsg" +
                "XS/jJ5AnTuU84Pecn+iLprjbVwXMMxynkN54KuXwQ+6dBBElme" +
                "fEsJTYjhnnJRRuBa3FL2IHMAwx4VWVIcwkbOFWjCwhg4opwfj5" +
                "Zibqx5Va9aPy96MUZZxjMxFzT0xEMqvEolnOiKHHNED1rYEybg" +
                "g4WMBzog5f4IlyJADFNUalygzz3IUrKOv+Pzsozs1RgwG4yx2/" +
                "1jfcU+s15TV+y69ZVsGFk8FGPD+YLOg9043DgMPYzIg3n7H+Rw" +
                "HTpIIstldMLkfBkP+aoG961HaY+KLEl3aVrNVdcnKx2Ne+AVjG" +
                "RzjblwnMMxkszhvP0vcrgOHSSR5YmPhEYpn8l26EfW4L71KO1R" +
                "kSXptneruer6ZKWjcQ+sjiCbbcyG4yyOkWQW556LHK5DB0lkee" +
                "IjoVHKZ7Id+pE1uG89SntUZEm6kI2Oy/XJSkfjHlgdY31vS3tW" +
                "3BK86HMIpMl8CzXCO62LfMZxOKKOzr2olnoUsrbMM2nBeHKPCd" +
                "M7pOqZEDM8S2+pM28reDV4UdULjsX3Rvvd/apM55gaaJl0g+Mm" +
                "1N5bcKR3m6x+88Qn6hi8HMya6hheuY7kzPIlq+R4ftTsdQxeMV" +
                "6XM/gNjuaMJ7WOud8i3Z9WR7u03Dpm8Zs3Nm/Cm4AeRuSJw626" +
                "VW/izCRxBI9otFA5oBWeH1XgEGZ01lQ5Bpegf45o9uCI92YTOo" +
                "qJR1mqflHDX5azh+jTsXms8El739P6uoz3PUW0PO97Wt/0731P" +
                "8wv+PFP7SbN5ocvb8a8S6vOE+jShrvgTKbaXm19qvDD35o8K77" +
                "MwjufToji5R9Jfjfrv1GyalzRv32f+K0C4wprfNn9glUlZj7Xf" +
                "dup69B/sfT3asinr/SOuR3faDWsbnJDuCaYz3TlM23UBdbutc0" +
                "eO+x6LX385m16Xaqatx2ta/HfmWY+n37Stx9Nv9HE9Xuu+HoP3" +
                "iluPrZ8Tz1d36rttP+jdptxs+v/3mU5lMPvj0uv9XY/113SqqF" +
                "YUYh6c4rPpUt9dkvcbhdfyxmAwbDbFZ2is43rhdVwfDIbNpvgM" +
                "4z3i3WRnvjvDHenQ/C4lSzaFrse3E893OSPU+p0Nvads/TGcFZ" +
                "OfC5M61jLUupZ79aXdh2e4Axii58Jqf58L3U90qsjW2Vf2ejTH" +
                "bft9T/n34TXte+08kGc9LtxrW48L9/RxPU6VuR77/zxTX932Xc" +
                "9qnjra/PI6tv4so461vSO1P+4t87kwrY6dyVGqY+v98s5rdz48" +
                "KlEfjSEHZ2KecGLNeSadj7mVhKIxehMR6wJNFhWOwewqMVolpm" +
                "xHBTC5tSGq+SRq0lXykGMWfRwz11PyQ15yRaug5QDe9zw2mPVY" +
                "7u9xB3CduTqYOtr89mF/fGL49sfgw0Hsj1BHuRX1/zOmOhb2Lm" +
                "U5VWr8JXjnQIb7nr96r2P3+/DtnddGbbE/PjmY83rpSrb1WPzz" +
                "TEl1fHpnX2eGpo7P/l/HIq4z7tHB1NHml9dxaVcZ12vvrWLq2J" +
                "nZCc8ztmyKem/mjvHRHUt+TzGGHPadjmnf8pjhmx9Tey4zWZC2" +
                "Tc71dEwbHpel52FDT8Pm1ACeZ57buftjOffhtesj9b7n+qCeCz" +
                "vuKNWxfbnM9ag35f+5Pi76WaQIxDwYNptiMkxdj40y1uPCsdFb" +
                "j+6iuwg9jNE1aBHnxOE6dIhZc0PFABtEIkqfcTvyJuubfBK2LS" +
                "qyJN3O42quuj5Z6WjcA7t2g+yWeyuaxSPSxMceeSSFT1hHpqMj" +
                "ccx4lVd1PO3OQvFCh4ytRsWzwQ/OO1OyhWxNuHouthiY7KZ7M5" +
                "rFI9LExx55JIVPWEemQzYyKiGG9CXE4LhKHRUv3IJjq1HxbHhE" +
                "EX1JtpCtCZd/1Pz1aEHWONg46Diih1FIYIZ0/QLwgVe/gFKyUD" +
                "HAJty/LyAKSQWXY3AJ+pdjIB3OQWwZhc/QEtHARs6V50S4XE/N" +
                "j0dLMYef/wAEMk0n");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1984;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXD2MG1UQtu5SIIQQCigoBCQUzgmB5KKjAE5QnL12ERrIka" +
                "OiAwkl6RAJP1II8TkJDoaKABINElAgXZEO2pCanyBCh3SBNNeh" +
                "pIvECT+PZ+ebee/trZ21fVi3K+97b+ab75t5u96364tSKpVKy/" +
                "eV1NZ4k3utB0oTtC3fPVq9Mz9Qm+xP9lufbwlthApjQ6yj2fLq" +
                "DpZf47HOp9y4v3EgnccTjV3Uq7yaI/7hqOdgwPZoMXPSeDDT+0" +
                "TIGqum8YgZP9lnLk9tOI/7Jmoe941rHtuHJmkeP/x6mPMYuD/+" +
                "i6PqX0Xfp4pgHIQjFoP21vMFrjMnlMr1wufx+ng4YjHFV2ife3" +
                "I9Tdzol7/90njW6/aLo9WTeax+PODVkhnXPjyeeTz/0Gj1Wi/0" +
                "nqO2J9tLpQs7Cn+O2z6m58eIbuOrQq6+jPU6V3zf63X7lfGs17" +
                "HrcVjr9fK+flgGuD++PKb74+IoVCp/85F6Ia/fz2br38eIfBqC" +
                "wrxjsRv5s5QlNsxSaVaadKSWbTS2FsEyutJcvsFWtmgm0eF+e8" +
                "nnM1k3rQpaLEYjsBqpwrXtI2Fem7PW1Ep2ZgTf3/3x3O957o+V" +
                "45vyvfD4xvfH84eH8V446DxWj27GeaSssufx3PeDrzPVm9hWbz" +
                "Zu89hZLuxgP2KzLMIU8hNrPCbmR1z15vKf+XJAX6wOfO4h9rzc" +
                "YW/6/PhausK9Xpqg7aNdo9VrDXn22m9M8vtM5SIfqRfy+v1stv" +
                "59jMinISjMOxa7kT9LWWJjLJm/414r5n3m7NHYOnN2wGtzoPX6" +
                "2sbrTPOLwdaZ1rH47xTJTDJj36+dJceb7EwcS6x3uul1Juf79U" +
                "y+9+si8qPrMT2DjxfDeO7eqOee0d0fi6qm/3ms/TRJ6/Vwq3H3" +
                "x9g83iFz9/5YOT2EK+t0M/O3rua34agc98dv+r8/Rp973urdJ3" +
                "YmO4fy++PO8VyPMd1ifn8MnLcfx88wrroGzbx2oHaAjtSyjcZi" +
                "QYzs4tF+ZK+cEk60az62WwRqi8Vx+qqakyOlysopW6uPlyifDR" +
                "VwBjkyme5d99N0bPzDY2e5sIP9iM2yCFPIT6zxmJgfccm0fu6J" +
                "54C+WB34vSb2vNzaW72FbfUWrzPVW2zxsVkWYQr5mTUWE/Mjzu" +
                "eM8aEvu44YexY39jLfZ/4o5n1mc/xuFqtm+P8uJRfL/2YeW2eG" +
                "OY9Zz+G53tD6/zvXO+NZr9tvD/FZ4GTlJB2pZRuNrUWwjK6cXL" +
                "7BVrZoJtHhfvs9n8/PSqugxWI0AquRKlzbfjfMa3PWmlrJzozg" +
                "M++Peyfq/rh34+/12e+K+17Xpv1ef1v12GZ8Dh9uVqNfZ2p3je" +
                "d6jOkWsc4k68m695TZtXWOs5193b1f05hshMBIahkDqNkg+yxz" +
                "pDqGCbMAznVE8nO4joU8Q4yz1kqM3u+Ps5hF4Cl83Y6S9dpUba" +
                "pzpqa47Z63KdqTclJmC1udRXbxIIdDdRXKZBFOsiIHelgfc0Bt" +
                "P0utqjk5UrBUjc+LeHdEnK0Ps5Wca1P1I/UjyTZ3dG23zt7IjV" +
                "0fLYJ1drG4OLa4I6H4SF7yOTaK0CykQl7UE17JwKGsTUdIXoRH" +
                "Be2ViN7VtU37JN7mwNX0sEv1pU67RC1tNHLj2i9kZYtgMYoxZN" +
                "E9ZrUKGM0ePwNkE4vFaITOS/LnanxezFOiUA+1bLY97GJ9sdMu" +
                "UtvzLPK4dpWsbBEsRjGGLLrHrFYBo9njZ4BsYrEYjdB5Sf5cjc" +
                "+LeUoU6qGWzZawyWqy2rk6V7ntXqurPK7X2YIY2cWj/ek9OO2z" +
                "F0c6jnU0ArX9LONZSaRgqRqfF/ES5bOhAqwzXd/C7oXdpZI7up" +
                "Y2Gi3sTg4mB8kqGGdhNH50L+k+8RAWOclKrMKC2jYH1PaztCzs" +
                "1/nx2OrqaMwe/bp+P1vy1efqc50zNUdt70qd43HtZ7KyRbAYxR" +
                "iy6B6zWgWMZo+fAbKJxWI0Qucl+XM1Pi/mKVGoh1o2W1HpzGfZ" +
                "7ekMl+mjZr1MKDmKXVrN0Xsb+yCskLUx1qogpzByVpyX2LGHmV" +
                "jOUPW6StzjNSzscbuM6KMRhJKj2KXVHL2a3w8rZM7jHs1n8Y5T" +
                "GDkrzkvs2MNMLGeoel0l7qGc6lfrV+nILY9cr/arO4pFe6XFSH" +
                "8sDKKqNVkFs7LsYrEYjQgzSzU+L+bNdr9Om4NU5j4L8wvznbmd" +
                "p7Y30/M8rv1GVrYIFqMYQxbdY1argNHs8TNANrFYjEbovCR/rs" +
                "bnxTwlCvVQy2YrKt57d/ovyFuflLa2fH8bv5RcoiO1bKNx/RBb" +
                "ECO7eLRfs2tOHGEc61iE3THLeFaSm2CpGpu5xkuUz4YKOIOYt/" +
                "KsRGd9Jde5WSnwPK+MLqrorfVp2ru49Y29g3n8LO19vjUbOb8B" +
                "V5IrdKSWbTQWC2JkF4/2a3bLiRwSJ2oaH9YMqWpOjsQ8bK0+Xq" +
                "J8NlTAGXSf+uX65c4d+LK0PHI916ej2GyPMDpej4UhffYzmqjl" +
                "6+E4hNGIMLMo+LyYN9v9Om0OUpn7JGvJWmc+17jtzvAaj8WCGN" +
                "nFo/3puUr7wogKGCdqOoewZkhVc3Ik5mFr9fES5bOhAlyPXV/j" +
                "ucDz4zMD/eUp8j87NJ4e6l/pnt3E68yXW+tG39t/x2OYVQ==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1785;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW8+LHFUQHiMIHpQNcSOiouAGXH8gZC8eBNnZaRU9iSj4K2" +
                "BWQVn2mGugx3XdyZ6W7MFj9JCARy/ec8nRw8abl6D/QMhJ/IE9" +
                "/bb6+76qN5N0dmI2obvp9+pVfVVfVe28mZ5OpteLxzc3et0xg2" +
                "N0oevBTPr4Q9eDtsfyyeWTaUyz6dLaa4A1NK4omYwIzKrxYlbK" +
                "whqPUQRXgypydSgSOSunMvnOAD98rrpODI8NX7YMyzPDJyf1ff" +
                "Oq1wyfmoQdvpLRPTubv//w8anWF1rFehryt+9W6xdb5lJ3tF0f" +
                "M1Huqz5u/nx7fVy5vnI9jWke69PKZBuBwQmL2o0BMiIyA/uBTX" +
                "PIc+ZYNaZ5ch6+1oiHV4zGDDjgGfbuE93nRtujOF2cTmOaTZfW" +
                "XgOsoXFFyWREYFaNF7NSFtZ4jCK4GlSRq0ORyFk5lcl3psGuFq" +
                "vVvGpzbVm1tdcAa2hcUTIZEZoehHiuj6uehTUeowiuBlXk6lAk" +
                "clZOZfKdMezg+OB4rzce0zy2pJXJNgKDExa1W62QEZEZ2A9smk" +
                "OeM8eqMc2T8/C1Rjy8YjRmwLFv2xvsVfOezbVlz9bQMAYnLGpv" +
                "OBoZEZmB/cCmOeQ5c6wa0zw5D19rxMMrRmMG6mOyrQ/Wq3nd5t" +
                "qybmtoGIMTFrU3HI2MiMzAfmDTHPKcOVaNaZ6ch6814uEVozED" +
                "9THZtgZb1bxlc23ZsjU0jMEJi9objkZGRGZgP7BpDnnOHKvGNE" +
                "/Ow9ca8fCK0ZiB+rjFefNR/tPdx7S+77lYXEzjeIbO1hhNBytf" +
                "XuLoiAm9j5fLi1lw5jNXu+aHPNRDvTU32JUpZpts/aP9o73eeE" +
                "zz2JJWJrMGWEPjipLJiGDcMZ52Ue28zsVQBFeDKnJ1KBI5K6cy" +
                "+c4YdrA72K32967N9Y7ftTU0jMEJi9qb945GRkRmYD+waQ55zh" +
                "yrxjRPzsPXGvHwitGYgd4fk21nsFPNOzbXlh1bQ8MYnLCoveFo" +
                "ZERkBvYDm+aQ58yxakzz5Dx8rREPrxiNGaiPO5y3PMe9fOfej4" +
                "flbOJ8/eth+6Tpnpvxc7ON77rnj3e3j5n7xzPd/WC3rw/2eqzX" +
                "3b4+VPt69Eu3S2exr0eXu319L+zr0dXu8/qw3IcftqN/pX8ljW" +
                "k2XVpDwxicsKhdoyMm6zWe6T2CuWOWk7OCJ+fha414eMVozMAd" +
                "5Ly7+8dZHcu/2ZiknDXK06O1txni1jiA4rwn+d7MPo0Zvvkoxf" +
                "nifBrTbLq0hoYxOGFRu0ZHTNZrPNN7BHPHLCdnBU/Ow9ca8fCK" +
                "0ZiBO8h5d/v6YMfm9y3/v9mF7v4xd98z+qNdH0e/d3285fvHKf" +
                "9euHmx28O5Y3BpcCmNaTZdWkPDGJywqF2jIybrNZ7pPYK5Y5aT" +
                "s4In5+FrjXh4xWjMwB1M1735vGfzx+779f3Zx0yOP3XvdzP5W3" +
                "d9bH0UZ4uzaUyz6dLaa4A1NK4omYwIzKrxYlbKwhqPUQRXgypy" +
                "dSgSOSunMvnOGLa/2F/s9cZjmusnGIu29hpgDY0rSiYjQvOMJM" +
                "RzT6EWPQtrPEYRXA2qyNWhSOSsnMrkO2PYwdpgrfrcXrO5/iRf" +
                "szU0jMEJi9qbe4JGRkRmYD+waQ55zhyrxjRPzsPXGvHwitGYge" +
                "57aluxXWxXr8ttm+tX6ratvQZYQ+OKksmI0OzJEM/t623PwhqP" +
                "UQRXgypydSgSOSunMvnOGLY/35+vXpfzNtev1Hlbew2whsYVJZ" +
                "MRodmTIZ7b1/OehTUeowiuBlXk6lAkclZOZfKdMezKtZVrvd54" +
                "TPPYklYm2wgMTljUbrVCRkRmYD+waQ55zhyrxjRPzsPXGvHwit" +
                "GYAYfhi/Br6+LGWJf0GKFTm19pxGLib7nHLN4zZqFo1eVQ03gH" +
                "b3trLuIt3OPc8KvxVT4Ynz+Wj1bjsVp+rB6PV1f1HWe0/wSjPF" +
                "E/+/lrf1V/eyhfap4J/U2xXp2UTflG+Vb5Zi29k7V/Wp6i1efl" +
                "l+VX5QPlEYd6pJyjVfNLtPKZaZ0Y/buPeq18vey37WP5iaxWq+" +
                "uL/kJ/odrfCzbXO37B1l4DrKFxRclkRGje20I89/644FlY4zGK" +
                "4GpQRa4ORSJn5VQm3xng232/Pnfk4N+vl/+8O88fJ/F2/79n9s" +
                "8pzj30//Tx3MPd8/Du9XjzPt7u79hzR//5KLU7btfvzh53MquN" +
                "zzbe3/hw4z3RfXzgp0gTXy0bp6ZZW+T9wVTrR+2yaofJ+s0Vc2" +
                "lMs+nS2muAZQ+v0UjgKeaYVeOZPmfPceZY9eTIPMe4PmflzMfm" +
                "XMdXf6m/VL3il2yuX/9LtvYaYA2NK0omI0Kzw0I8twOXPAtrPE" +
                "YRXA2qyNWhSOSsnMrkO9Pg/wOYNshg");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1519;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW8FqJFUULfwFBRfqIIgbRQIJDEJcdVfvBVFiFDe6DQRhej" +
                "+NdA/JanYuXWXhxp0/kLUfELIPg4ob0WBWtvX6vnPOva+TdKY6" +
                "iTOviqr33r3nnnPvzeuuTmemPWqPmqY9ahdj09jK5nYHBic86m" +
                "8WB+ZgZAWOg5rmUNYsqSqnRXIevtaIR1RkYwUchp+8M7/enbw6" +
                "+cA8jx9N3miufUzeXOrZKNjebno5pj9emtN7q3BN3nLr91fLZb" +
                "LZ3Vfq45Ov7kcfJ6+vp49PvrxZH6cX01+nz1Ifp+eLn/Xvq+zH" +
                "S/bMPwXbHz3tx9+Wev6cX389F/ff/eQ4+CbO+jr6YrwJT//VUO" +
                "9fyP04++W292Pt4/pe19M17v+D15pbOKZfN/fgmHzb1GPFY/h0" +
                "+DTd02i2tIaFMTjhUb+ye07mQBzUFOFPznJ5VsiN8/C1RjyiIh" +
                "srcAc5bz4eP6r7q5c9+sPqntVR18/F813N32cG1/xMtTHYSPc0" +
                "mi2tvQVYQ+OKM5uDgVWVL2alKmzxGEVwNaiiVIcikbNqqpLvDP" +
                "D/z9+v1/V74U1/vx7sDHbSPY1dh3ds7S3AGhpXnNkcDHkvBT63" +
                "H3e8Cls8RhFcDaoo1aFI5KyaquQ7A3zdj3fxfc+L3sfvvq99XH" +
                "Tip7vYj8O94V66p7H71LBna1gYgxMe9efPH3nOnMyBOKgpwp+c" +
                "5fKskBvn4WuNeERFNlagz1jJdzg8nI+HNnaeQ1vDwhic8Kg/a+" +
                "Q5czIH4qCm+LJmSVU5LZLz8LVGPKIiGytQHxf4+v5YnzMvdx+H" +
                "X9xNH5fp1v14n/Zj/b6np59s3Y/1dV1f1/V1Xfdj3Y+3etQ+rn" +
                "60m+1muqfRbGkNC2NwwqN+ZQcn25XP7B7B2jHL5VkhkvPwtUY8" +
                "oiIbK3AHO99WuzUft2zsPFu2hoUxOOFRf9bIczCyAsdBTXMoa5" +
                "ZUldMiOQ9fa8QjKrKxAvVxga/PmXV9fjxoax/7eF4Pf14WsdzT" +
                "Z/zzH1fp9J/H4MHgQbqn0Wxp7S3AGhpXnNkcDKyqfDErVWGLxy" +
                "iCq0EVpToUiZxVU5V8Z4Cvn3t62Y/toE33NJotrb0FWEPjijOb" +
                "g4FVlS9mpSps8RhFcDWoolSHIpGzaqqS7wzwqz2vD16pz5n6e+" +
                "H6joOPo232WZ8Ks09fhj6OHo4epnsazZbW3gKsoXHFmc3BwKrK" +
                "F7NSFbZ4jCK4GlRRqkORyFk1Vcl3BvjCHv2kvk5Xfr/eLvQx/5" +
                "uug89XYPpoyTvHWl/Xkw/vb2/r++MN3h+3R9vpnkazpbW3AGto" +
                "XHFmczCwqvLFrFSFLR6jCK4GVZTqUCRyVk1V8p0Bvv5/1z4+P7" +
                "Zn7Vm6p7H7Ru3M1rAwBic86s/fzeU5GFmB46CmOZQ1S6rKaZGc" +
                "h6814hEV2ViBvn/sfKPxaDzfl2Mbu506trW3AGtoXHFmczDk12" +
                "Tgc6/rsVdhi8cogqtBFaU6FImcVVOVfGcy9nh0PB+Pbew8x7aG" +
                "hTGM5iieLTSObQ7GMl9433YqOJXbZ4UKNKNSHRqtucGvcTHbhe" +
                "98dD4fz23sPOe2hoUxOOFRf9bIczCyAsdBTXMoa5ZUldMiOQ9f" +
                "a8QjKrKxAvUx+Waj2Xyc2dh5Zrb2FmANjSvObA6GrB343G6ceR" +
                "W2eIwiuBpUUapDkchZNVXJd8awg93BbtP8d09j903Qrq29BVhD" +
                "44ozm4Mhf+cV+Nz3ZrtehS0eowiuBlWU6lAkclZNVfKdMWx72p" +
                "7OnzenNnZPoFNbw8IYnPCoPz/L8hyMrMBxUNMcypolVeW0SM7D" +
                "1xrxiIpsrEDP6wW+/r2wj//30Z60J+mexq7DJ7aGhTE44VF//l" +
                "nlORhZgeOgpjmUNUuqymmRnIevNeIRFdlYgfZj5xteDC/CX9M6" +
                "W7LjDpvNecV+MJbY2Wc8yqSxdrezlKvPscyo+XMOl/xl8eIqW9" +
                "Jr99v9eT/3bew6vG9rWBiDEx71559VnoORFTgOappDWbOkqpwW" +
                "yXn4WiMeUZGNFWg/Jvy/j6Fe8A==");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1463;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7+LJFUQbv8GAwMVXBYRFBHMNnPbmeCCZbJLdjDZDTa4SL" +
                "joou2T9RxBwcjEQEU4DE0Eo8PESAxOjsmOWzm41P/Ama6t99VX" +
                "9Xp+Xe8yDu81/V69qu99X9Wb7umZudv6qD6qqvqovhqrSmdqWw" +
                "+wisYZLbXBUFV2VpO2bRy38xwHI2w1qCJXByORM2uykt8Z4GNr" +
                "Pk25flVt1DZd9/9t9a36lvQyqk/m3gOsonFGS20wWFXmi1mxiv" +
                "V4DCNsNagiVwcjkTNrspLfmYQd1+PZONaxjYx17j3AKhpntNQG" +
                "Q9qDwOf2cexVrMdjGGGrQRW5OhiJnFmTlfzOJOyoHs3GkY5tZK" +
                "Rz7wFW0TijpTYY0h4EPrePI69iPR7DCFsNqsjVwUjkzJqs5HcG" +
                "eG2HL7QXCz622O5qjOme5bkOXyzTkDw5P511rV0WX6SMtUtVnm" +
                "kvVi4a7c58nm0WU8RqGkDZvLvWLosvUsbaPMvkTrM/e0K/3bza" +
                "vK++87vN66s/qZo3OiMfZHxv9fN8bF5bGH13La433fy9NXP5kH" +
                "b8Unux3Otxmbc7X9fLzWKKWE0DKJt319pl8UXKWNvFst71ePH3" +
                "bl+PF79udj0eNoeN9DK2O93o3HuAVTTOaKkNhvTqBj736jdexX" +
                "o8hhG2GlSRq4ORyJk1WcnvDPCx3X+lKq2H9vmTsgdlH7ennd+9" +
                "Pu7mfEd/p3heP5deRvXJHB6LwYEIx5kdnNbPfOr3CKsds+zOCi" +
                "ttHr7WiMeqyGYV7A62sWk9nY1THdvIVOfwWAwORDieNJINRqtg" +
                "10GNc8hr5lSZU1faPHytEY9Vkc0qmH2c2rxv6r7e1TZ4OngqvY" +
                "zqkzk8FoMDEY4zOzitn/nU7xFWO2bZnRVW2jx8rRGPVZHNKtgd" +
                "lLN8v+7j+3V9UB9IL2N7xx/o3HuAVTTOaKkNhvSeEvjc0+/Aq1" +
                "iPxzDCVoMqcnUwEjmzJiv5nVHs4HhwPLsuj3Vsr9RjncNjMTgQ" +
                "4Xi65pMNRqtg10GNc8hr5lSZU1faPHytEY9Vkc0qmPv6Cl/u6z" +
                "7u68HJ4ER6GdsdPtE5PBaDAxGOp9cq2WC0CnYd1DiHvGZOlTl1" +
                "pc3D1xrxWBXZrIK5HiV2NjibjWc6tpEzncNjMTgQ4XjSSDYYrY" +
                "JdBzXOIa+ZU2VOXWnz8LVGPFZFNqtg9rGNDW8Pb1fVvJ+P0mQm" +
                "c+3h8xYw4mFLWb2CXc1aUc/OcxhGcF7IP+pyDpyb3wU+bLZQCf" +
                "f7P+lfHb4un7DXaR+9Mz8wk5MRgkIPP0bmWKywLJ/I3sWoWWle" +
                "8FurizvG+QSTHqvVUFUf/7R+ZH3Uam3O5fmW8/eZwUt8psB9/U" +
                "25V7fz98eyj6Ut/L1nf7CvFvtkjh4+jnlMZOpWljW5HLBWez0W" +
                "5Y8zx+gzZu2uLLt3zOoO9gZ7V/O9FGl9MkcPH8c8JjJ1ZLinOr" +
                "kcsFZ7PSIL1uLMMfqMWbsry1zeHpHnmHxX7tM+2uSHHa3r+xUw" +
                "P/and//Lci318ro9LHuwwafu8vtjL/9vr+xjH/s4PB2eSi9j+0" +
                "vQqc69B1hF44yW2mBIv00FPs6M43ae42CErQZV5OpgJHJmTVby" +
                "O5OwD4ezd8N5P0zvijKTOXr1IWpPb4HJcsLv+fxr7FVwMLfPSj" +
                "PwueXq4NWcG+KsFLOFSvleeE3P69/KHlz38/rBJ7v9nHkwvpnn" +
                "ddnH8rlnlX387NuX/buP8pwp32e24Xq8+X2c/FHu69L6ua8nf2" +
                "7DfV0/2uR6rB+t8Lz+92bu68lf5f1xG+7rXf27j+Hj4WPpZVSf" +
                "zL0HWLvCe5gJOmrn+NSfi+c0c6p8WGY7Rl6fM2vmuW2ucsb7+o" +
                "vft/9zz8XP2/X7Y9nHvv+O3eT4S/kUs/b7473hPellVJ/MvQdY" +
                "ReOMltpgsKrMF7NiFevxGEbYalBFrg5GImfWZCW/Mwn/H8xsZg" +
                "8=");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1509;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWU1uHEUYnX2WLFgAChrCBoSiZAFIGImkuy02QWLBwhskS5" +
                "ZXWSFxAI8IWMoFuMCYK3CAHAEr2XjFKcYKEjNd89V7r76vxx7T" +
                "xLFVVer6+X7ee1XTPdPd0561Z5NJe9au+8nEZja2FjGo8Kh/si" +
                "4YA5EZOA9sqiHmjFgV0zJZR7lWH48sj8YMKMnXnDank6IkW7Kj" +
                "hU19iDcsIEbo7NO8SAW4rDI+M6g+VYrost+k0OcPYTan7bydL/" +
                "dzbn2/w3Obw8IxqPCoP39WeQxEZuA8sKmGmDNiVUzLZB3lWn08" +
                "sjwaM9D5OGfdXI5+ntSydZl9tDw+nr0z+wz7OHtvi/z3Bz33A9" +
                "uHI6l+d6P3k62wPijmn26p5aGNvnm4qpilg0uycQu7RkX2iGFT" +
                "sdiYpUQ0VaYLdh4NYXu/HkCyerk1LPf3p3qVblua183r2JbsaG" +
                "GzMc/YD8QInX2Go0iaa63VIf2sMUZU/axh0/5cZON9KeL+9KMb" +
                "dnZcg+76OzPO70zdxzH2sTloDlKb+v6qOLA5LByDCo/68/WVx4" +
                "zJGMgDm0aUlVUOq4I21lGu1ccjy6MxA32HJN9xc7zsj63vPcc2" +
                "h4VjUOFRf+bIY8ZkDOSBTeNjzohVMS2TdZRr9fHI8mjMQPu4jn" +
                "/z13Xz4/Vc10O8492H1+fC+ntdf69v0z52r7pXqU39yp5mNrYW" +
                "MajwqN8YMAYiM3Ae2FRDzBmxKqZlso5yrT4eWR6NGVAsvp6P9X" +
                "em/s7ctvOxfdA+SG3qV/Y0s7G1iEGFR/3GgDEQmYHzwKYaYs6I" +
                "VTEtk3WUa/XxyPJozICy9u21e8t+z/res2dzWDgGFR71Z448Bi" +
                "IzcB7YVEPMGbEqpmWyjnKtPh5ZHo0ZaB+Tb7/dX/b71veefZvD" +
                "wjGo8Kg/c+QxEJmB88CmGmLOiFUxLZN1lGv18cjyaMxA+5h8h+" +
                "3hsj+0vvcc2hwWjkGFR/2ZI4+ByAycBzbVEHNGrIppmayjXKuP" +
                "R5ZHYwbax97X7XQ7y/ufHev7O6Idm5cWxFo0Dj+yMRDyPZfD02" +
                "9u9fM8wtAIXg1WEa1DI6FZOZWp3BnE+/L8br2L2bZ0L7oXqU29" +
                "2dIcFo7haM7iEaMDE/YSL9LFLKhDylV1qS1ah2arNvg1z6sFi7" +
                "sP/6eeX9uWx3ce30lt6s2W5qUFsRaNw49sDARmVTyvSlnYUsZo" +
                "BK8Gq4jWoZHQrJzKVO4M4uv5WJ8Lx38u/OX3+p7iJp2Pv/51u8" +
                "/H375/M+/Dn313u/fx2ZP6v8JN+n+mXtfxPj46enSU2tSv7Glm" +
                "Y7Yg1qJx+JGNgWDMHk+VqZ/nEYZG8GqwimgdGgnNyqlM5c4gvl" +
                "7Xo/xfeN6dpzb1/RPjuc1h4RhUeNSfnz3zGIjMwHlgUw0xZ8Sq" +
                "mJbJOsq1+nhkeTRmoOfr3td2bTeZrNrU92/UOpvDwjGo8Kg/v5" +
                "vLYyAyA+eBTTXEnBGrYlom6yjX6uOR5dGYgd4/Jt9uu7vsd63v" +
                "Pbs2h4VjUOFRf+bIYyAyA+eBTTXEnBGrYlom6yjX6uOR5dGYgf" +
                "ax93WLbrE8LxfW92fqwuawcAwqPOrP53weA5EZOA9sqiHmjFgV" +
                "0zJZR7lWH48sj8YMdF0vWLd8b/6d3+h+XZ/3Lleal83L2JbsaG" +
                "FTH+INC4gROvs0L1ZmrVXGZwbVp0oRXfabFPr8IcwYozkZRD25" +
                "1GdzMuLnfOLxLsYfU8F/0P7H9p7toy6vpcS7GH9MBVcv9b1Z3c" +
                "frKu299p6N1JbmaGFTXxnjkYaZU06kAbnWWt2kH0eEWCpW7iGV" +
                "wzvGvO20na7n0+zpbWmOFjb1lTEeaUDh1HgiDci11qpHQS6OCL" +
                "FUrNxDKiPdZUQ7nX0V3D9+caU3BzsD9s//17f5X7691/rzrn7f" +
                "1X18i/bx27oHo+zjk7oHV/imrv/PjPK/a3A+/lDPrq3vw5+2T1" +
                "OberOlOSwcgwqP+hUdmGxXPLOXEcztVQ6rQibrKNfq45Hl0ZiB" +
                "d7A//gWhH/6b");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1182;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWbuOFFcQnR8gckDgh5CQEyzL0iZYAgKrp78AGVnOdkMyAi" +
                "QnSGYExtrZD7AsoQ3YYDNLfIL9DxaTOSMnI2O3a6vPqUfPTptG" +
                "zC51r+Y+qk6dU/dye+ZuM5vF8vjhrMoEZflz7cH4srh+8vl68d" +
                "niW5zHxecj4r8Y9HyX2K5NlPXVtd4bo7i+dPNvRuayk9vrua7z" +
                "+PHOY+1jPdcX97n+/enlPo9P/qjzuG3lt5e1B2NL+6h9JK30ap" +
                "O5twCraHziSMdgYFXLF7OyKmzxGIvg1WAV2TosEjlbTavkd0ax" +
                "8/vz+7PZaSv9qUdmOtYWGFR4rF/XijEYWYHjoGZzyDUzVcupkZ" +
                "yHX2vEIyqysQKK4uveU/fH7b4/tm8u+rf+r19t+/1x+Uudx+w8" +
                "zo/mR9JK331zHukcFsagwmP9/XdwPwYjK3Ac1GwOuWamajk1kv" +
                "Pwa414REU2VqDfmSPOu+7h71eaB80DaaVXm8xhYQwqPNZv2T0n" +
                "cyAOahbhK2c5nBVy4zz8WiMeUZGNFXgHO99Bc3DSH2jfeQ50Dg" +
                "tjUOGx/l6jHzMncyAOahafa2aqllMjOQ+/1ohHVGRjBdrHM3zd" +
                "e+r+uD372K7albTSd/fHlc5hYQwqPNbf30T7MRhZgeOgZnPINT" +
                "NVy6mRnIdfa8QjKrKxAt22z/B1Huu53qb3uJdtH5/8Vf8/c5FL" +
                "PddTnMdmt9mVVvruZrmrc1gYgwqP9fd31H7MnMyBOKhZhK+c5X" +
                "BWyI3z8GuNeERFNlage/gZvs5j/V7X7/Xl+72u8zjJe9wr8yve" +
                "Ljaxo4XN+jwG9pydfRKTo9SqrdYc5fOzuQHt+3UZxvghzpyjOR" +
                "x8W3m40TvNw9lk5ZTL853PP2UGG+b5tnmb28SOFjYd84z9YMzY" +
                "2ac8lsnGaqt1KH/OMWe0+XMO6/bnPJvoLf+MuGf3pvyXevbjp/" +
                "DXTLvT7kgrvdpk7i3AKhqfONIxGFjV8sWsrApbPMYieDVYRbYO" +
                "i0TOVtMq+Z1R7OJW3Nv9n3S0fD7iV+t2bt//oOdx8f2WnMeb7U" +
                "1ppVebzL0FWEXjE0c6BgOrWr6YlVVhi8dYBK8Gq8jWYZHI2Wpa" +
                "Jb8zil1/HkedjE/7PN5p70grvdpk7i3AKhqfONIxGFjV8sWsrA" +
                "pbPMYieDVYRbYOi0TOVtMq+Z0BPjlBd+tt4tgyfz1/La30apM5" +
                "LIxBhcf6LTs42W751O4RrB2zHM4KkZyHX2vEIyqysQLvIOfNZf" +
                "miztcUpf5f4X3KD/9pK6PMG8fr2cb7FLGZBlCc91Dsef51yojd" +
                "hOXsL7l/61TVc137eLFLvQ+f5H343nxPWum7G9GezmFhDCo81t" +
                "/frfoxGFmB46Bmc8g1M1XLqZGch19rxCMqsrEC3R87X7NqVuE9" +
                "ZWcTO1rYrA945QJjxs4+G5dlAS2tzM8KNj+bKdC+X5dhjB/iPG" +
                "F51bwKuM4mdrSwWR/wygXGjJ19Ni7LAlpamZ8VbH42U6B9vy7D" +
                "GD/EmXM0x4Osx5t8X2yG2qyccnm+8/mnzOD/l+Xf9es7yT7+U3" +
                "tQ956Pc+/Jyv5xna7R5R0IsDey");
            
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
            final int rows = 59;
            final int cols = 82;
            final int compressedBytes = 1372;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWr1qHGcUnQcwcZEiRRITCGkSTCCVWu943iIEI6NGhaXGjV" +
                "FhCxOEnAfIGxjUBPIEhoAfQYUJuNELRK2a7MzVnXPOvd/sauXB" +
                "ssU3H/v93HvuOef72N1Zj9w0+fr976ZeG1+H3y9fPxx+eXjfIy" +
                "+eHn69Qf03k5mfC7HvZnL91crsjxtxfYv5yz+X65829PJLPce5" +
                "znHxdvHWehv7uK187j0waMho3hUwByMrcB3U1ENZs6SqnF7JPu" +
                "JeMx5VmY0VcDl+s/fjq38/hffj4s113o+LN+vfj0f/Xfdzna8X" +
                "T+tdo95nPuz7cVjX+8wNnmP9/TjH1T3rnllvo8dsHSPAOhqvPP" +
                "M5GFhV+bIrVeFIxCiCd4NdlPahSHhWTVWKJwP8Zp/ro99u9+f6" +
                "6Nf6/VjvM/Uc6znWc6znWM+xPjf7XM7x5V838X5sH7ePrbexj9" +
                "vK594Dg4aM5l0Bc+ZkDtRBTRGxsctpV/DGPuJeMx5VmY0VcKGy" +
                "Pu/58Kt90j6x3kaP2RoRxqAho3llj5zMgTqoKSI2djntCt7YR9" +
                "xrxqMqs7ECn6C96n2m3q/r/frW3a+3223rbRw+8du+RoQxaMho" +
                "fvzuGOfMyRyog5oiYmOX067gjX3EvWY8qjIbK9D3o+Uu2ot07x" +
                "liFkePmM95xXkwltg55zzKpLXeeyt5jR7LjOqfPay4D1+si13q" +
                "nbanCTfELI4eMc0B71xgLLFzTutKLqDljflZQf2pU6DjuMphrp" +
                "/ibE8fPnr4qGn63sY+Yyufew8MGjKadw3MwcgKXAc19VDWLKkq" +
                "p1eyj7jXjEdVZmMFXJbrtrqtpul7G4cn5Vu+jhFgHY1XnvkcDO" +
                "PfBBJf+LvCVlThSMQogneDXZT2oUh4Vk1Viifj2MWdxZ2m6Xsb" +
                "+4ytfM4RYB2NV575HAx+BplPz1HzvC5xKIJ3g12U9qFIeFZNVY" +
                "onA3z9/TjP70e/Hpx5bzO9OFbKr8JvknPE1TSAYt9Ttevyq5RR" +
                "u1blvfc2K2XzfNLP++vlHHE1DaDY91TtuvwqZdSWWRZnizPrbf" +
                "SYrRFhDBoymld2cHJc+TweEaydXU67QiX7iHvNeFRlNlbgE2Tf" +
                "9bnZ/Fc9x3muB//cPMNN7eu6ztt37btyzOLoEdMc8M4FxhI757" +
                "Su7Mx7b8zPCupPnQIdx1UOc/0UZ5njVVc/k3Nc9Rznuf74op7B" +
                "LOd4t57BHFf9/7ibX93r7rX1/YiYr9F7DFl+xRmzgxPxyFfyxS" +
                "poZeeaV3/woRVard6QV6Xs9jJ30p0sxxMbLzMnWKP3GLL8ijMw" +
                "MSfikS+dY1BBU+7oyh1Eb6V9aLV6Q16VslvH1udm9e/Xn9Lzx3" +
                "qO9f1Yz/G2fa67g+7AehuHO9CBr2MEWEfjlWc+B8N4j0t84W59" +
                "EFU4EjGK4N1gF6V9KBKeVVOV4skQ/jz95jjvYxZHj5jm4koZMz" +
                "urxMrsQtEaK6FsfH5vWledZ8Yr/OY+j6upHXzu/7p4fu/ja9bv" +
                "x1n+v9lOu2O9jcOTyR1fI8IYNGQ0Pz7jHOfMyRyog5oiYmOX06" +
                "7gjX3EvWY8qjIbK9BzXMvtt/vLcd/HIbPva0QYg4aM5keNcc6c" +
                "zIE6qCkiNnY57Qre2Efca8ajKrOxAp2j5Xbb3eW46+OQ2fU1Io" +
                "xBQ0bzo8Y4Z07mQB3UFBEbu5x2BW/sI+4141GV2ViBztFyx+3x" +
                "cjz2ccgc+xoRxqAho/lRY5wzJ3OgDmqKL2uWVJXTK9lH3GvGoy" +
                "qzsQKdo+X22r3luOfjkNnzNSKMQUNG86PGOGdO5kAd1BQRG7uc" +
                "dgVv7CPuNeNRldlYgc7R8P8DaODanw==");
            
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
            final int rows = 57;
            final int cols = 82;
            final int compressedBytes = 1762;
            final int uncompressedBytes = 18697;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmc1qHUcQhe87ZJFFYgiEbBJCIJYgItmNJ1mYLAN5AsnCIg" +
                "Tb+rlLi0CQsbTOKxj9PYX0XBlPT/V3qqonIHMDXsxt7lR31alz" +
                "Trfvj+a62+v2Vqtur5viamUrm9sVDIOKr6+mB3PlVA76UPOION" +
                "TlvCu8qY+414ynK7OpAo+pdtadDfHM4lg5szUZxTCo+HrVqHPl" +
                "VA76UPP4tmZL1XNap/qIe814ujKbKsg5jrXTH1fp8eZ3m71tVO" +
                "cepz+1829+W/2Pj9MfVh/Fo3/cPy7XEi1X1jED1tA888zmMKiq" +
                "58uuvIpmIsYjdDfsorUPj8Sz1/RK8WQqdqffGeKOxbGyY+uYAW" +
                "tonnlmcxjqGSS+cI47UUUzEeMRuht20dqHR+LZa3qleDIVe9ff" +
                "DfHO4li5szUZxShau3Q2adzZHMY2X3qfBBWG546u2IF31NqH7/" +
                "beqPu+7Nawp18Oz69OPzn91iqvD08/e8An1Oezle8auS829Ln4" +
                "6X9Wv34Q1yPmf/0zrL95oJfv2/m3P6+WxwYeb39ZzuDB39fv+n" +
                "fl+j6SszVXy1HVZ5wpO5zkI1/Ll6ow2s593fvDh+/w3d4bda+U" +
                "3aKSXo+/Lq+vTTxeHy5n8AHffMv39aOw/qDv6+UcN3GO/bpfl2" +
                "uJ4yfn2tYxA9bQPPPM5jDUz+bEF75l1lFFMxHjEbobdtHah0fi" +
                "2Wt6pXgyhn3y9MnT1er9tcT3lbKyuV3BMKj4uu2VOYyqoH2oeQ" +
                "9tzZaq57RO9RH3mvF0ZTZV4GH45X29ifd196J7Ua4ljr+ovbA1" +
                "GcUwqPh6/W2uzpVTOehDzSPiUJfzrvCmPuJeM56uzKYK8vtjqe" +
                "12u0PctThWdm1NRjEMKr5eNepcOZWDPtQ8Ig51Oe8Kb+oj7jXj" +
                "6cpsqiDnWGrH3fEQjy2OlWNbk1EMg4qvV406V07loA81j4hDXc" +
                "67wpv6iHvNeLoymyrIOZbaRXcxxAuLY+XC1mQUw6Di61WjzpVT" +
                "OehDzePbmi1Vz2md6iPuNePpymyqIOc44ZfvmY18z7zqXpVrie" +
                "MJv7I1GcUwqPh6/beqc+VUDvpQ84g41OW8K7ypj7jXjKcrs6mC" +
                "vB5L7bw7H+K5xbFybmsyimFQ8fWqUefKqRz0oebxbc2Wque0Tv" +
                "UR95rxdGU2VZBznPDL+3oj94Vb/Va5ljje6WzZOmbAGppnntkc" +
                "hnpPl/jCfeFWVNFMxHiE7oZdtPbhkXj2ml4pnkzF3vf3Q7y3OF" +
                "bubU1GMYrWLp1NGvc2h7HNl37HDSoMzx1dsQPvqLUP3+29Ufd9" +
                "2S0q6XfcP5ZfE5ffcT+i/+f6czmD5ffw5ffw5RyXc+xedi/Ltc" +
                "TxL/SXtiajGAYVX69/69e5cioHfah5RBzqct4V3tRH3GvG05XZ" +
                "VEHuZyb88npc3tfLOS7nuJxj83eK7X67XEsc7xi3bR0zYA3NM8" +
                "9sDkO9J0184e56O6poJmI8QnfDLlr78Eg8e02vFE8GfON+5u/l" +
                "7mS5n1k+H5dzXM6xe949L9cSx7/Qn9uajGIYVHy9/q1f58qpHP" +
                "Sh5hFxqMt5V3hTH3GvGU9XZlMFuZ+Z8MvrcSOvx2fds3ItcTzh" +
                "Z7YmoxgGFV+v/1Z1rpzKQR9qHhGHupx3hTf1Efea8XRlNlWQ1+" +
                "NY66/76+Hvn+sSp7+IrllztRxVfcYZTMpJPvLFf+OowvDc0ZU5" +
                "iN5a+/Dd3ht1r5TdTrXL/nKIlyVOlUvWXC1HVZ9xBpNyko986R" +
                "yDCsNzR1fmIHpr7cN3e2/UvVJ2W2rdSXcyvC5PLI6v1BNbk1EM" +
                "g4qv19d8nSunctCHmkfEoS7nXeFNfcS9ZjxdmU0V5H1dagfdwR" +
                "APLI6VA1uTUQyDiq9XjTpXTuWgDzWPiENdzrvCm/qIe814ujKb" +
                "Ksg5ltphdzjEQ4tj5dDWZBTDoOLrVaPOlVM56EPNI+JQl/Ou8K" +
                "Y+4l4znq7MpgpyjqW23+0Pcd/iWNm3NRnFMKj4etWoc+VUDvpQ" +
                "84g41OW8K7ypj7jXjKcrs6mCnONY62/72+Fz8rbE6ZPzljVXy1" +
                "HVZ5zBpJzkI1/6ngkqDM8dXZmD6K21D9/tvVH3StntVLvqr4Z4" +
                "VeJUuWLN1XJU9RlnMCkn+ciXzjGoMDx3dGUOorfWPny390bdK2" +
                "W3U+2mvxniTYlT5YY1V8tR1WecwaSc5CNfOsegwvDc0ZU5iN5a" +
                "+/Dd3ht1r5Tdllq37tbD+3ttcXzHr21NRjEMKr5ePzvqXDmVgz" +
                "7UPCIOdTnvCm/qI+414+nKbKogn4+ldtQdDfHI4lg5sjUZxTCo" +
                "+HrVqHPlVA76UPOIONTlvCu8qY+414ynK7Opgpxjwf8LGON3qQ" +
                "==");
            
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

    protected static int lookupValue(int row, int col)
    {
        if (row <= 58)
            return value[row][col];
        else if (row >= 59 && row <= 117)
            return value1[row-59][col];
        else if (row >= 118 && row <= 176)
            return value2[row-118][col];
        else if (row >= 177 && row <= 235)
            return value3[row-177][col];
        else if (row >= 236 && row <= 294)
            return value4[row-236][col];
        else if (row >= 295 && row <= 353)
            return value5[row-295][col];
        else if (row >= 354 && row <= 412)
            return value6[row-354][col];
        else if (row >= 413 && row <= 471)
            return value7[row-413][col];
        else if (row >= 472 && row <= 530)
            return value8[row-472][col];
        else if (row >= 531 && row <= 589)
            return value9[row-531][col];
        else if (row >= 590 && row <= 648)
            return value10[row-590][col];
        else if (row >= 649 && row <= 707)
            return value11[row-649][col];
        else if (row >= 708 && row <= 766)
            return value12[row-708][col];
        else if (row >= 767 && row <= 825)
            return value13[row-767][col];
        else if (row >= 826 && row <= 884)
            return value14[row-826][col];
        else if (row >= 885 && row <= 943)
            return value15[row-885][col];
        else if (row >= 944 && row <= 1002)
            return value16[row-944][col];
        else if (row >= 1003 && row <= 1061)
            return value17[row-1003][col];
        else if (row >= 1062 && row <= 1120)
            return value18[row-1062][col];
        else if (row >= 1121 && row <= 1179)
            return value19[row-1121][col];
        else if (row >= 1180 && row <= 1238)
            return value20[row-1180][col];
        else if (row >= 1239 && row <= 1297)
            return value21[row-1239][col];
        else if (row >= 1298 && row <= 1356)
            return value22[row-1298][col];
        else if (row >= 1357)
            return value23[row-1357][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value23 lookup");
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 6, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 18, 0, 0, 19, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 35, 0, 4, 0, 36, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 6, 0, 0, 0, 38, 39, 7, 0, 40, 8, 0, 0, 0, 41, 42, 0, 43, 0, 0, 44, 0, 9, 0, 45, 0, 10, 46, 11, 0, 47, 0, 0, 0, 48, 49, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 11, 0, 0, 0, 0, 0, 0, 0, 51, 1, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 14, 15, 0, 0, 0, 52, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 0, 1, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 53, 0, 0, 0, 20, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 55, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 56, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 57, 0, 58, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 59, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 60, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 2, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 61, 14, 0, 62, 0, 0, 0, 0, 63, 0, 0, 64, 65, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 14, 0, 0, 66, 15, 0, 0, 16, 0, 0, 67, 17, 0, 0, 0, 0, 0, 24, 25, 26, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 28, 0, 1, 0, 0, 0, 3, 4, 0, 0, 0, 29, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 32, 2, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 34, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 35, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 38, 0, 16, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 1, 6, 0, 5, 0, 42, 0, 7, 1, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 68, 44, 0, 45, 46, 47, 0, 48, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 53, 0, 1, 54, 0, 0, 0, 8, 55, 0, 56, 0, 57, 0, 0, 0, 6, 7, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 58, 9, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 8, 59, 60, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 61, 0, 0, 0, 0, 69, 0, 0, 0, 62, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 65, 17, 18, 0, 19, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 66, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 67, 25, 26, 0, 0, 0, 68, 69, 0, 0, 0, 4, 0, 70, 0, 70, 0, 0, 5, 71, 1, 0, 0, 0, 27, 72, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 1, 0, 71, 0, 0, 0, 0, 0, 0, 72, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 0, 0, 19, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 11, 0, 73, 74, 12, 0, 73, 75, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 76, 0, 13, 77, 78, 79, 80, 0, 81, 74, 82, 1, 83, 0, 75, 84, 85, 86, 76, 14, 2, 15, 0, 0, 0, 87, 88, 0, 0, 0, 0, 89, 0, 90, 0, 91, 92, 0, 93, 94, 9, 0, 0, 2, 0, 95, 0, 0, 96, 1, 97, 0, 3, 0, 0, 0, 0, 0, 98, 0, 0, 0, 0, 0, 0, 99, 0, 100, 0, 0, 0, 0, 0, 0, 2, 0, 101, 102, 0, 3, 0, 4, 0, 0, 103, 1, 104, 0, 0, 0, 105, 106, 5, 0, 0, 0, 0, 0, 0, 0, 10, 0, 107, 4, 1, 0, 0, 0, 0, 1, 108, 109, 0, 0, 4, 110, 0, 6, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 112, 0, 0, 0, 0, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 5, 16, 0, 17, 113, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 114, 7, 0, 115, 116, 0, 11, 0, 0, 0, 12, 0, 117, 0, 0, 0, 0, 20, 0, 2, 0, 0, 6, 0, 0, 0, 4, 0, 118, 119, 0, 5, 0, 0, 0, 0, 0, 120, 0, 0, 0, 121, 122, 123, 0, 7, 0, 124, 0, 8, 13, 0, 0, 2, 0, 125, 0, 2, 3, 126, 0, 0, 14, 127, 0, 0, 0, 15, 9, 0, 0, 0, 0, 77, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 128, 129, 0, 130, 131, 132, 133, 0, 0, 0, 0, 134, 0, 0, 23, 24, 25, 26, 27, 28, 29, 135, 30, 78, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 136, 46, 47, 48, 49, 137, 50, 51, 52, 55, 56, 57, 0, 0, 1, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 138, 139, 140, 0, 141, 0, 59, 4, 79, 0, 142, 7, 0, 0, 143, 144, 0, 0, 10, 60, 145, 146, 147, 148, 80, 149, 0, 150, 151, 152, 153, 154, 155, 156, 157, 61, 0, 158, 159, 160, 161, 0, 0, 7, 0, 0, 0, 0, 62, 0, 0, 0, 0, 162, 0, 163, 0, 0, 0, 0, 1, 0, 2, 164, 165, 0, 0, 166, 167, 11, 0, 0, 0, 168, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 169, 1, 0, 170, 171, 0, 8, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 172, 173, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 82, 0, 12, 0, 0, 0, 174, 2, 0, 3, 0, 0, 0, 13, 0, 175, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 176, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 177, 0, 178, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 7, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 179, 2, 0, 180, 181, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 182, 0, 183, 184, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 185, 0, 0, 0, 0, 0, 0, 16, 9, 10, 0, 11, 0, 12, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 0, 186, 0, 0, 187, 0, 0, 0, 188, 22, 0, 0, 0, 0, 23, 189, 24, 17, 0, 0, 0, 0, 0, 0, 190, 0, 0, 1, 0, 0, 18, 191, 0, 3, 0, 7, 15, 0, 1, 0, 0, 0, 1, 0, 192, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, 0, 0, 193, 0, 0, 194, 195, 19, 0, 0, 196, 0, 197, 0, 0, 20, 0, 0, 0, 84, 0, 26, 0, 198, 0, 0, 0, 0, 0, 199, 21, 0, 0, 0, 0, 0, 18, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 200, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 0, 0, 1, 201, 202, 0, 0, 0, 0, 0, 0, 203, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 204, 0, 0, 0, 205, 64, 0, 206, 0, 2, 3, 3, 0, 0, 0, 65, 86, 0, 0, 23, 0, 0, 0, 27, 207, 0, 208, 28, 24, 0, 209, 210, 0, 25, 211, 0, 0, 212, 213, 214, 215, 29, 216, 26, 217, 218, 219, 27, 220, 0, 221, 222, 6, 223, 224, 30, 0, 225, 226, 0, 0, 0, 0, 0, 66, 0, 0, 0, 2, 227, 228, 0, 229, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 230, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 29, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 231, 4, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 232, 233, 1, 0, 1, 31, 0, 0, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 234, 69, 0, 0, 235, 0, 0, 236, 237, 0, 0, 0, 0, 32, 33, 0, 0, 3, 0, 0, 238, 0, 239, 0, 87, 240, 0, 241, 0, 0, 34, 0, 0, 0, 242, 0, 243, 35, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 244, 0, 245, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 7, 0, 0, 0, 0, 39, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 246, 37, 247, 248, 38, 249, 0, 250, 39, 251, 0, 40, 0, 252, 0, 40, 253, 41, 0, 0, 0, 0, 0, 254, 0, 255, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 257, 0, 0, 258, 0, 8, 0, 0, 42, 0, 259, 260, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 261, 0, 262, 263, 264, 265, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 267, 43, 10, 0, 0, 12, 0, 13, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 70, 0, 0, 268, 0, 0, 0, 269, 0, 0, 0, 0, 44, 0, 270, 271, 272, 0, 0, 45, 273, 0, 274, 46, 47, 0, 0, 8, 275, 0, 2, 276, 277, 0, 0, 0, 0, 8, 48, 278, 279, 49, 280, 0, 0, 50, 0, 3, 281, 282, 0, 283, 0, 0, 0, 0, 0, 0, 284, 285, 0, 51, 0, 0, 52, 0, 0, 286, 0, 0, 0, 287, 0, 0, 288, 1, 0, 0, 0, 5, 2, 0, 289, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 290, 44, 0, 0, 0, 0, 0, 71, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 291, 0, 0, 0, 0, 2, 0, 292, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 293, 45, 0, 0, 0, 294, 0, 0, 0, 0, 0, 295, 0, 0, 0, 0, 0, 0, 54, 0, 0, 55, 0, 296, 0, 0, 0, 0, 0, 0, 56, 0, 0, 36, 0, 0, 0, 37, 5, 297, 6, 298, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 299, 3, 300, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 301, 0, 302, 0, 303, 0, 0, 304, 0, 0, 0, 305, 0, 0, 57, 306, 0, 0, 0, 0, 0, 307, 0, 0, 7, 308, 0, 0, 0, 309, 310, 0, 46, 311, 0, 0, 0, 58, 88, 0, 0, 0, 312, 313, 59, 0, 60, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 89, 0, 0, 0, 2, 47, 61, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 314, 0, 48, 315, 49, 0, 72, 0, 50, 0, 0, 0, 0, 316, 63, 0, 0, 317, 64, 65, 0, 51, 0, 318, 66, 319, 0, 67, 52, 320, 321, 68, 69, 0, 53, 0, 322, 323, 0, 70, 54, 324, 0, 55, 0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 325, 0, 9, 326, 0, 0, 327, 328, 329, 72, 0, 0, 0, 3, 0, 0, 330, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 331, 73, 0, 0, 0, 0, 74, 0, 0, 38, 0, 0, 0, 0, 0, 332, 59, 333, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 334, 6, 0, 0, 0, 28, 0, 0, 0, 335, 0, 0, 0, 0, 0, 336, 0, 61, 337, 62, 0, 63, 338, 339, 0, 0, 64, 340, 0, 65, 0, 0, 75, 0, 0, 341, 342, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 343, 344, 90, 0, 345, 0, 0, 0, 346, 0, 0, 0, 77, 0, 0, 0, 0, 0, 66, 0, 78, 0, 347, 0, 79, 67, 348, 0, 349, 350, 351, 80, 81, 0, 352, 82, 68, 353, 0, 354, 355, 356, 83, 0, 0, 357, 0, 0, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 0, 0, 0, 0, 0, 69, 358, 0, 70, 0, 0, 0, 84, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 359, 0, 360, 85, 361, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 362, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 86, 73, 74, 363, 75, 0, 87, 88, 76, 0, 77, 364, 0, 365, 366, 0, 0, 367, 368, 0, 0, 0, 7, 0, 91, 89, 0, 0, 369, 0, 370, 0, 371, 372, 0, 90, 373, 374, 375, 376, 91, 92, 0, 0, 0, 377, 0, 0, 378, 379, 380, 93, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 381, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 382, 0, 383, 0, 0, 95, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 384, 385, 0, 0, 386, 387, 0, 388, 0, 0, 0, 0, 97, 98, 0, 0, 0, 92, 93, 0, 99, 100, 101, 389, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 390, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 391, 105, 0, 82, 106, 107, 0, 83, 392, 393, 0, 0, 0, 394, 0, 0, 108, 0, 0, 84, 0, 395, 0, 0, 85, 0, 396, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 397, 0, 0, 0, 398, 0, 87, 399, 0, 400, 0, 88, 0, 109, 110, 111, 0, 401, 0, 112, 402, 403, 0, 113, 404, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 115, 116, 0, 405, 0, 406, 0, 0, 117, 407, 0, 118, 119, 408, 0, 120, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 1, 1, 5, 6, 7, 8, 1, 5, 0, 2, 0, 3, 9, 1, 5, 5, 10, 0, 0, 10, 10, 11, 4, 12, 0, 13, 2, 10, 2, 6, 1, 0, 14, 15, 16, 12, 10, 17, 18, 16, 3, 16, 19, 5, 3, 16, 20, 4, 17, 4, 21, 22, 23, 24, 2, 0, 25, 26, 3, 27, 28, 1, 29, 30, 0, 6, 31, 10, 2, 32, 0, 17, 33, 34, 6, 1, 1, 8, 35, 36, 16, 2, 37, 38, 3, 1, 39, 1, 5, 1, 40, 41, 12, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 2, 48, 49, 3, 5, 50, 6, 51, 52, 53, 54, 5, 1, 6, 1, 55, 56, 1, 4, 5, 0, 57, 0, 58, 59, 17, 4, 60, 61, 62, 63, 1, 18, 15, 64, 65, 66, 10, 67, 20, 68, 5, 69, 20, 70, 0, 71, 72, 73, 0, 74, 0, 21, 75, 2, 76, 77, 78, 20, 2, 79, 18, 80, 81, 82, 5, 83, 84, 6, 6, 7, 2, 85, 3, 86, 87, 2, 88, 1, 89, 1, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 5, 6, 99, 7, 3, 100, 101, 102, 103, 2, 104, 105, 106, 0, 107, 108, 4, 109, 0, 110, 25, 7, 7, 3, 27, 111, 112, 6, 5, 113, 6, 2, 1, 114, 8, 9, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 10, 18, 0, 125, 7, 1, 1, 126, 127, 2, 29, 0, 4, 0, 128, 8, 2, 11, 129, 30, 130, 131, 132, 6, 13, 21, 3, 133, 10, 1, 134, 5, 12, 4, 3, 135, 20, 14, 9, 136, 137, 138, 21, 20, 12, 8, 11, 139, 1, 7, 140, 141, 21, 142, 7, 143, 144, 5, 145, 146, 147, 148, 149, 150, 22, 31, 151, 152, 8, 8, 153, 33, 24, 5, 154, 155, 1, 156, 8, 157, 158, 159, 160, 5, 161, 3, 162, 163, 164, 35, 9, 165, 166, 167, 36, 168, 2, 6, 13, 169, 170, 6, 39, 171, 172, 5, 173, 174, 175, 40, 21, 43, 176, 177, 3, 178, 56, 6, 9, 179, 180, 13, 44, 181, 182, 183, 184, 185, 186, 15, 0, 187, 188, 12, 3, 14, 20, 189, 190, 191, 9, 192, 193, 11, 1, 194, 195, 196, 18, 0, 8, 30, 197, 28, 15, 198, 2, 10, 22, 7, 4, 10, 23, 1, 199, 9, 200, 201, 0, 7, 9, 202, 203, 204, 1, 205, 206, 12, 207, 22, 208, 209, 2, 0, 210, 211, 212, 29, 8, 10, 17, 5, 2, 213, 8, 30, 11, 214, 215, 10, 216, 217, 45, 218, 14, 219, 220, 221, 1, 222, 223, 224, 11, 24, 47, 3, 11, 225, 17, 15, 226, 227, 7, 228, 229, 25, 230, 58, 231, 232, 233, 234, 25, 235, 236, 237, 238, 239, 3 };

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
            final int compressedBytes = 1573;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXc1u20YQnl2v1bXTA5MKgdLTxhGKHHoq8gDrogh0zDG9CX" +
                "BufYlVgBbOA/TQm33sW9hvktz6GF3+iObPklyRXHJJzgCSZZKy" +
                "lvPzzez8yDfkALc/qnP9vPpVvfoovwNxLT/9/PwW/gXy+r/f+B" +
                "1fsZcb/vdfn7bf5C9yR+VbeEOe/f748p89IM2BaOnIpX7w3BGW" +
                "ea30g+iHRNbNlIKS/E3q4oH832hl5HqlGr8YVZsAViCI/EnoU7" +
                "cAe8Uu7ugLWG+4Wkkm4Ap2Qt7qH882j/AK8Wt0ugn9zwbC5xUN" +
                "/c8q9D/bbeJ/Hhi/A8q0/A7a/9yH/gdi//PxcY3+R5vggYAKQj" +
                "Tm2iyJlLFZsucKLoAQqa2YhNjNidqKgwzC80IzVl+0VojfveHP" +
                "vcafyxR/tprFGmZAKK2/lPIIfx5i/IGdT/gjyRk8vEj1Zyc19E" +
                "sO3/8Q6c9BRl4g1J/zo/6QUH8u4Br1xxP8FBX4+UWb+dcQP6/M" +
                "+Inxe7P/+ZrzP9+Qfw7x57NGzgL+kCr8IQ7whydxb0KCpzsfEv" +
                "rM5BIWutfot9iZzoVYI3sC1FYkJCQkpFnFf/X5b1iF8d+f1fnv" +
                "TKIsKMQTmaP06GKv4/hhD33ED3+Qe2iK/2lN/H9znot/P5zwfs" +
                "y/IPlMb5s3H5nXJDJTnrHa6JVILDoQRSOnXuS/s/vXs6naL02e" +
                "GEswkhRgtWJ/IlHJ0/pHkK1/bENtfco/RvWP90n9AxzUP7LCof" +
                "R46BpMpcXUggRKD8llPoPY6l/5Ig6D179lcYH8tLd0xA8w4cc+" +
                "wg+snyI5N9Yx3fvk+18M/rd5eRH+SYL144IyUMsLe4lfxq4fZ/" +
                "H/0hQ/7o7xY/r50sXnl+4fSvcvfayfe4FfSIulsH6qTu/fIO76" +
                "N572fzbxt7n+/s6Yv3hnyl9W1++/HPvHrqaZv+SNjkZ29/896t" +
                "/nsv6REfRvWP0tyMuU/8/3D8BM+wcmT6S6/6NkdbE0+5Mf8wVv" +
                "ltH/Ihz8TWbD3gVRFY+xi8jfQMOufi37rF/PCD9HoZv6+r/F/I" +
                "RoD1gexJ8N/QuW8yPjrd+f8Kei/m0Ahlz9m5c5mLy+zHGIO06Q" +
                "NK1fVKzfIwpa2990o6iG/oWB5r+65M9fl/PnLJc/h2P+/O4pf0" +
                "4a8+en7z87bHmPn8lgVaV/M+2/sKh/gEjkd2+QX339dKj+Czfx" +
                "E6YnZkfuRSqMoDVigM7Hsp9NygbKU5TlBe6wCMyZPi5SBlKRge" +
                "XYSZyFSRi+jirPMSluLUziW9DaQgtVLzmEgdVQdVpX6JCSP1EI" +
                "ajlQv1BgBvtv2XwW/WFjBNnZEnreQ8X4pya7aac16tjvrQhrdg" +
                "4pzt51IcaqftdPYG0MM3gZWEjGzc8g7qmPNaZL4+n3IPyTvusd" +
                "by0PlbucDiRfdmoMFPgeP/WzoqAnJeD96TWZHX7RnLSC3AmS4w" +
                "s3mkDX988qi0FaCUBUAUxd/ETG0BQ372fO5ERPjViRjoyRzvSg" +
                "Xw8iTdoi6xbexTn5t3WZHv5a+m+R8d+0Tgiq/AsvMoT6fKtO4j" +
                "f+xAXelxIvrAltCsTaa5FC7i2KnGEg9+2+Tpm/HjZ+QPJQf8ej" +
                "xvnJtP9jVdP/gf0Xi9A/iiLwjf8284MV84eu5wdLTjmoBAjsX0" +
                "b7xfsfO98BVfOfJauNTW9m84Pz15/W65/vRt2+/3nlcf/ziYSe" +
                "E2lS+KeQ97h/9J4Gnn/rB8V5ZQQ27PzjlKNmG/6B5/xrOz87nv" +
                "4jNW+qWO9B35JdsYf+yzi/C+b5XWE1vzvs90e61P7B5o87yc9+" +
                "fndR8psOyBbmH/fp/GM0i241/8g6olKu/0plTpT6r5gVjuN+a9" +
                "ytA4oGCakl/kEe/1ih/1QOZ0fWVs3Ku6VMFdS0kWLCp/XPnU5W" +
                "FrIs0GZ5HRVZFZYV+hTHP71+f4Sj/ZmcteSy/KcN/A9UUMd/f4" +
                "hMBS2c8j+YCrh316Rlu6osfoJJfwjE+AkwDfvtJyxsyT+D/4r/" +
                "od+67L+WEXzhphPJ1u+aXK9wpk9DfP/YIFEYt/ONqoqlg7lkxA" +
                "In8q/wYEVu8/QJgyKnzG8mgaxDWhTRqqMTmf83rf9/8lGt1w==");
            
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
            final int compressedBytes = 1154;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUt2mzAUfVJpjvoZ6PR4kM7Uz0J0MsoyshS1owyzhGylOy" +
                "vEsQ0YGYH+cO/AiROwpPee7r0CDEQdGgIAYH9oZ77sXgWp978o" +
                "4u+/8u7F6O71Q0OMxIHMeUcjSCN8cTIyhD6/2LYAfMAHEZWDfz" +
                "DqB1+c3rTbm03HhIX9OJNiyrD5ASkWoz+s64C4/rug8cxtN73Q" +
                "bEL4jFfMvA8OsTD1c/ploF8AADhA2nmIX299IhCVz5FN8N/B1b" +
                "8/BPP/K8YfUP8O9FFMCR0/dk2N2jKn/o/jZ7D+yQ8EFSgfJa3+" +
                "3Nav+qRosrwRAEBR1g/IHz2ZKCWyx5/G4fhffAVY/tFW/idr/y" +
                "HXReS/gPMvV/5BOo5/B+ch5I16YpcAKq/9sX4CAFioxeqFNVyR" +
                "WGKzbuo/d9J/PWT+t70sMsCnjmLXGThgOoSG19ht95VeLM67GT" +
                "Y104nG2lU5O8Ty1p+hoJIn0d4+h3ACOcERArvqN1ZzBIRwU2Kz" +
                "kT3558d8x89Qv0C++pc31o+C4l2/k0MDc2soNBwerlD9q+L7O5" +
                "g/qH9gv/oJrOFvE4y/UT8UbKGmLr/6XH9eO+TghwNMuvyl8De/" +
                "2gGJNpV/6Lnh5l7SHSmmf3cZf26LxDTilXM63AtzpxtFP+mR9H" +
                "P748v9P/r+VH/+hvylwvlPBW1w5d898w/0a7Pxa4bEHokQhFVB" +
                "Ps82GUFgQHveWjbKbNr8RUcM/6T7ZecwyZlbAoCtLn5WUhX0d9" +
                "l8Cn1eBvFfSY9ggKOeqkgitOT+S1dd4J77FzD+W87Uxco01ulu" +
                "pj4U19kuUDW+L5+uUgjRTPs8Y/tr+UvYykZPTrdGhW1+LX/FvP" +
                "+LO/8E2F8sVG9F0RPgyb+5+F8EGr+4tC/G7adwoqni5+8/ctdP" +
                "YP9jEvLXlg04tyXVhf9S6s8W4p81fr75T2MgpvSz3fPF/f5zZu" +
                "AfHsro/2r/495/X77SWdcgkY9/jOJPU/Fn7/Gnaf+ZlTmW1M/K" +
                "/tvzv6x9aXLEr4Q8bV2/+vOH+vk/Rv/Nhw7qj3D/yrryF5P/bv" +
                "BvkvaBHAysglROCP124y/Kxl95/Vf+9r3zv8L/yEW9KLt+8hw/" +
                "SLR+k12MZW/Betzo20QK2cr8AsVCuRyMMAn5J/z334LzBErfAy" +
                "YM/03Uj3KuH1GU/1XDN9pj/vCr8f+1zZ+v8ccvF4xiAumOn57T" +
                "oDA/9wE5rk+HyRB1vuj5/5bpd7d7/7OEmDuf7hutrNFmJt2aV2" +
                "ZuH3AmO1VVb4M6oAjTUa1r37r+kkdS+vR69o/s5B87Un/p/PPK" +
                "dVAT+lhI6BXpjPbu/bZguC1aXg5qsme/DvMmK++/s79yv36Wjf" +
                "XC6GtrTj+Gm05Ih+52eyqAUXyfnxlJd0uy33FRwPjLf3731mCu" +
                "KCMgNvftJr2jymCJ2/M9fxTt/jeVxA/AiibX9dfj+WdW7F87f9" +
                "dw/1f/dmr0vzLVzCsi/yFHazZW//Ejguu/Y/LHnvx3yHWY6tUa" +
                "nr9U0fL1nB6+IT1eMPS04/d6fkz259/8B/cXtzI=");
            
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
            final int compressedBytes = 962;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXV1u2zAMpjVjcIE96CEPG7AHd9hBhJ6kR+F6gh4hR12cpk" +
                "6cWo5sSSZlfx/QID+mKVEU+VGWXSJqiGoahefr8QODD+5gr5LO" +
                "dq8NtZdvWjItue4I0308utNL9a2mqmoOxN1X1emPuSG3XD+wbK" +
                "CjYHKpDDtXQ72PMeQ1yXtOYEaObPqXlPp9x4+8hf9BPrn/C8e/" +
                "vcffgvtfn7nUF/50zbd8Zkkdf6JP/vRxihv+tO/5ZwYnsIMfqv" +
                "MbN5IGGPFrM/O3WEb9OeMHH1x+jRH2rzI2ywnrz396IPdstZ56" +
                "dbIMSChPS+Rjy/LI/KeMvyZqWpPb8MH+45Y0ww7Otbls2faS73" +
                "f89+Dnvzzgvy+FWMdlnz+R6y/Fy8fO/yz6o+fPGvZLUn/Kjn+C" +
                "/Cfsv+L1E+KHFqD+iK6g70iUl2HC1CKrHTP0O4xMxGxwavWb25" +
                "GP2z/g5S8X+adjz18G8u+dvFUx8/z8yzzmX2n4W2T9Unbki7I/" +
                "AADAfpljiv1/3oUb2fgfkD+l+QeP2ez5ocC5Ua/j5MxM6TPph8" +
                "CFcw6TwwXcmMoV9QPycOHhQhgcuvQxJaSvWNXdfsYM2WP9F5P/" +
                "k9XfVmXqsTdN29b04iLsXwCmrr/iZp8d2d9t2Mgp4//sQeFEPR" +
                "DVv53iQNp/AOUAh4D/I38CiD8AUEr+4fz5Z/n+eRD1HSZUt4f+" +
                "1xh/dBcASphgbTqSDZIum3RMwDEAAAApAg72T07tn7yR/+i/Wf" +
                "X+jXphwZ1Kt6R+ANAFhdT4a/x6XfX6VYLrZwmeHxyBoLDG29Uv" +
                "rg3N98ISIMQfW3/8avv41cB+JcBJzT/p+bt3/bq8CeMP5F8muK" +
                "K5Pn/omqjOrLvRVsnUE4tGbvSnut38+CWoG7ZHiXWWFLrHz2EA" +
                "i6s/iErff7yd68guePzMg/GzK46fHPniUruw260P0pEE/AcAgB" +
                "SoAoKFnnjQ8wfu+d8hlP+9aOpFOv6bvP/665/p9Y97z3Wz81++" +
                "8QO7UYsV//+INP/Clli16xdvvvj9A8//VQMzpwje6lWdlrDJER" +
                "DJ04L734Bd47lnaEyGG0vfqa3c34tfticXPJ7e/v5JbLprrn8+" +
                "k/GT/Ue/XmE/1D+ArmqlfugDdaQP1QVbB9AVje7qR7Osfix9/x" +
                "PWD/D8NaDwIm64koD2I+cvi3/zr399mO/uwiePaGsr/2+C7V/P" +
                "s9eQx11BD8E+O9lZdiyEAICncAz/49H48bba/cs2nn+yWv/J8P" +
                "wMq23/ittw/ZFh/Gbl71t5XmK//8kKxlo=");
            
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
            final int compressedBytes = 866;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnW1u4yAQhsdeVmKl/vCPHIDeBO1J9ihoT9Aj9KiNkyZ1HO" +
                "zwzYDfp1LUKh3zMcMwYzAmIjqRkGRhnD8EqfPPEnP9tIrcGMgi" +
                "so0guSxELcon/XO901JGXr9x4PX/xZbvJi8t8oJoepRXNC7lzU" +
                "X+lzhf4iz/05EmTftLlE8O/R8hua7/ybX+f3evPIWW79V/Fvkp" +
                "Qf09x19V/X2468946A/EotZzQTF7Ytb4HbeQYfyv5U1O/8vD/w" +
                "MAAADAMwUBAPSORtMBAKAz5nu15v6XQoeEY9AF3fT/5JH/BKw/" +
                "KNgV+/y1dXnm+b/oXf+x+cPR7efo9W+m/5RnDcwt7gxsgbM8ex" +
                "Kuf/prG2FFE+iO2yZI2+xfz9Z8sf/PufXDbP/Dzf4vCeur/Qvf" +
                "8n8+7+PnQf5jlp84TTY0b8mD/Rx3eOvXOhvZN0Y2UEfAOv5JtH" +
                "92Lz41/HphIzybWtFiBCf6LW2Kidj/DxLO/63FD6q5aoscw6p0" +
                "/pkxGpbe9Tfl6i9r6g/5Z6T9T3Xtv/bzVwDjDwAnDpvae+eLQ2" +
                "V5AGoC+93oDNVG7gwACArQywZJseun2fLX6Sl/vZY/bq2/JmAq" +
                "oF7Qe5Ljuq+IT1AzQGsA/itPAIz9DLC/2hh0AdiPwuWWzWjr6q" +
                "lQaewvdv+O3/lzoeevmdf9V2kclll/z6c/vvOHiex/w+j8seL6" +
                "w/3b6v77hfWrxOUj/gNHHn8J5m+Vbf7O2vLq+/9UNr/Wf/4brr" +
                "+3yPO/3+DXwXrkqpSuoBzjxu9W+jkVEec7go7jN12m5KD50/r8" +
                "8uAmPyziL4/y/Z6fdin/QP5DF8uWQcX8Z3yy//+849+t8au2x6" +
                "+652/S3/bXmO8vHy71vidwveQs9o9B/4XWnxLVv0z5mlOwXQLc" +
                "vwY5vcbTkOUY/x5v/laE9wfljX255E87+U/w/gMTUH7r+qt9/7" +
                "+F559b8xOGWf/Hx2+565/7/OYjku7mUE7/xcP/9B2/VfCfB/Mf" +
                "uCPZR/4S67945j+t51nM69/9+VM5xo9hlb+VUdTI2DbGSMHRMR" +
                "RQzFTW+voTAIA7xmf+xPMj/dDF+1Mwz4Fc+TN4zCTZ6M/7/S3E" +
                "akUeeIcijbibpqdIka1bQp9fVf0bTbtmhfgB8UP7aNS/X/v/Au" +
                "fvzW8=");
            
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
            final int compressedBytes = 776;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUGO2zAMpFSjUIEehGIPLdCDWvQhQl/Sp7A/2ac22WzSAG" +
                "vH9soSKWrmEBgIZJnUiORIikMErCFbMcQPPpDb7Xf7bjztf5ak" +
                "un8AkMCEIKVpLO4DS8OAEYWiVRRxcjx/hpuJiXy6S1f8Un18mE" +
                "4pITwRX9sxh7d1Sazav7v275b6BwDyW2sXhq8spQqxnvfGz8u3" +
                "5/gV5OL3Uf1XeP6nrc//e2T+dqSfMsZfM/Ms+h/rFy3yx3z7qI" +
                "8/vO6FsC16sbHxL43fDxBmL+3Hz9f5wyv8/7w0f3a1rzh+0vxp" +
                "uHjiF66BhkiEXQFNGCJ+PzT7sHtw0d3y4ETMmIsAAGjS/03B1f" +
                "T7MfpZxB9Bg/9Hxwb+xVL+5b7tJ2n7BfznWs0fHf7XvX5cPv71" +
                "8g/mj2z+Ocb/4H/d+qvv+gX6HQAAAAAqJ/jUvG9X0r/S80fDnB" +
                "+aKM/Vr/k8Ki/+fz773y2ev39Y/3ob+q839LRbW8q/3u0HxkrP" +
                "Yz4/78n/iP/KYMZ/ONpXLYvXrX9Zef+64Q6UHU64PbA2EXyFyQ" +
                "oANvUr9KdYkcV2bEZIFfS8gf13c/Xr5vNfPZwfEi7ZFfCnNv91" +
                "QzFP/vOfb/x/x/6Z7fyVCdgYERLcMFb9s6E978ifcVkG+SoT89" +
                "D310F/IP4DAvVrv+Pfg/4o74fV2l8h/+2qn+/bcyX7s2L+t8g/" +
                "4O/Y/G1XPycr60fADHyNkOUX1c1sKaRQYEe6bHfdjLiQ/MvMK7" +
                "XcW6m3aj81sR9He0bWD1j/hv6m8o136Y37oH6IjC40Tab7z52v" +
                "XwAs3B4AgGbxMwoZE4WdGbumQiLsnxvVH/Xyb6Xfv5vaf3tHe1" +
                "X2C4x/4/zVryzE+VOz+e+w99fnDvmbqIP/n8P8FV+/wf5z3/P/" +
                "sCiC/A3+AgAA9A/of6BIMQI91Y/4/aJQYG3eHgDM6p849DAlwv" +
                "5pj5lfdP3FoP3S6+fQH8ryTzbsmx83hczkOUT6SMnlX69qLJ2Y" +
                "+Xy6/P6V2NOU6OfVGZ/iX/r253TxD7hS6rc=");
            
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
            final int rows = 189;
            final int cols = 16;
            final int compressedBytes = 239;
            final int uncompressedBytes = 12097;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmN0KgzAMhdOuF72U4QP0UcqerI++6WRYcFQ2TJvkfBdahW" +
                "B+WpIjUcW0W0dqkYmFPLB9WHMWXslK25tEflv65VJW61sgR3Gm" +
                "8jEskSt93fNnEFc/FtHBoP6K6++rWzOUhNqq4qB/zWf71+Miny" +
                "LKAkA3cP4MdgHV82Pp6n+H+E/Pn//qVwn6l3l+Uhe/wDqx6S+l" +
                "9Uf+gGR+278YfGUzYf7G/MSrH3r7P2j+hvr/nYXbSz8/+UL/rc" +
                "eP+Q0A3aB/mZuf0H+Y9q+F+NuqOb+ls9932/uBnnYaxDZA/xzp" +
                "/A3/fev+A/CVJ4GnPcE=");
            
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
            final int compressedBytes = 4188;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWweYFEUa/au6u3rCcgssIEEPyegSlnTKAiKKHCpy54KKS1" +
                "RgEUFBQLIrkkVAMCAqSVHSAYKHKCAGQFHQM5xKEFEMoCAiwQju" +
                "3F/VeaZ7pmdnVjz7+6ZDdXd1eP3e++uvGulCICBDCML0tFQeyk" +
                "IWVGEVoTo0hJpQHxpIjaCpTEmuugtyaV9oBZfIEq0a6gztaBc6" +
                "FHZCR4VAHv2NrYNe0Af6SnXgVriNvsLCSoH0RbAVjIDhcBfczd" +
                "aABAHIVE7RGfImUgTnQEWoKpWl8+VepEagE9SWC0m29D1k01NK" +
                "e2gETQLvhVaQM3Q2XAzN6WJoDZfD36G9dJT+Ap2lc+gGuBG6yj" +
                "XIRuhB8oLvyhl0Gv1BDsEgehKGwUhpOowlBUSl7aV3IAPK0c2s" +
                "MpSHClAJKtNhcJ40Dc6HaqQm1IEL4EKpDJXkzpAjHYSL4G+0Ob" +
                "SgC1lfaANt4Qram+aHlsPV0IFE6GVyN7gWbgjmQDfoKWVBb7hZ" +
                "mgE7oB/cArfDHfA2DKFdYQwUql2AggKMfgkqBOkVtIj8CqWgtF" +
                "RD3Q1l4FzaKtxWolCDbIBa9FW5dqA31IV60Biayd1pa7KWtqFn" +
                "6HE6HFqSNnApXCa/CVey9XAVXAP/gE5wPVyn3ghdIB+6w00kHw" +
                "qgPwyAgawABsPQ4BVwp1QXRsFo9oIymPaHicqlylB6Wj1KR8qj" +
                "YFxwPWsMDVmjCE5Km0iE4xuJ0H7sVVHCaNXwE7jdhY4i6yKRYB" +
                "dc/y2wNOKYgsuD+Yhva2ObrdHL59E5SjPWxChXL2N7lGHiiMux" +
                "7tJYslHdzrcD74dfj0SkFVadcA9uI77ivPFamfJlJELy8B4z8I" +
                "y/yiG8l5N4JOKL5YgvnrEr4jJJ0/ic1DS25ZGi9CCf0wJc68YG" +
                "aHto74jrBD2V6eKcGeQ5eznbwOeha/WzD+rLImN/aKzYHhBuJ9" +
                "2vv4PJ7LHAFvP8XfSfUgGbQ8/gUXgsaWO+qyP26yC++jsgA8xz" +
                "C4w1pTVMwO0XYDJdQZfCFLqMnnZ/DgmRIbmhQrZVbP2i1tHvuI" +
                "t+v7sFbsf1q/4q5lNCq0JDHE+9xlijWA+xnvYLzl9xTpBkc3zp" +
                "qUDLiOcUylHftW+Tjfp1a5n1C3zFPvG00k/e+Jrv6nBgL/vYji" +
                "/yd5h+51uYfg12MX4H3Wx1ZInv8HJnzbSr4zr1o/FVd2v4ZmRI" +
                "q/G9VcL1V+1nIH/n6kduoivjvAnzTsiYWHyRv5Ow9h/V02yboc" +
                "8wle5Hfb6XfgozYTbq84PQgH6ijIQZJDe8hvbD9zYLLqH7xNfb" +
                "Tv+KO+LvPvU9nPcS9Qp9xquvCQ0X+1GfBf4nQNKfdC/Hl+szPs" +
                "F3eE97OL5QO7CPZKt18ciR4fpcn8X9Nhd14Jzrs3iqlhp/oyfO" +
                "X3HsIA3fQFsDX7kebpfT91YQd3BAwxeq4W+6ps+Bjw3+oj5z/u" +
                "5mI7g+m2wV+izWpsENdnylGVh2P9dnG7cLw4+LJZMusvCFUhq+" +
                "UEbgW0paDfzJzS8T+J02Y/vobvNb+RzwW4dLzSOEPuNS6DMuNX" +
                "224cv1Gcu5Pj8Ao5HHuy3/lStp/iuj/7KnNf8NdlAQT7KKLZMr" +
                "43nov/qV7PjmBXuG9nP/FSUC3/D28EoDX9N/H4JMfD58SrZUw1" +
                "f//gS+uET+Qjaem2Pgq1/DxBf9dz/3X1GK/iuWD3N9VlQDX/zN" +
                "gZGhJcC1DUuVAPdfsU/4r8Vfy3/Nr17gG3wUWkgfs5HR+Io3uJ" +
                "/7r1kq/BeXuv/iGqoW91+cU4FvLvdfA18oLc5C/w0+hleLwVfz" +
                "34D5ZuQqhv/i3isNfDX/FWfY/Be3Blr4oj4/IvA9gPjOlQfD4/" +
                "KQ0PWI76MYX03h+LJxUF++XR6k7GNTMb6aGLqB48vuZqj6oXyH" +
                "E+H7DfNn62OVhd8Mr2KT9f0CX7GWKd+hluH4OjS2hr7MZhMEvn" +
                "lemgTtQzk4d+Crf7kTxXySfhzqM4+vRFkhGNwuj7/Homqs41BT" +
                "3S2hhZzJRnjcw7W2dcTXtmXiK7aoqPESDV/9iNL68txQV45vTN" +
                "2NUe83Bc418R2ql+v46ludnGeRMRa+trrmifkCeELjL64vhEXI" +
                "3yeDzWE+12csaRDMFTW8LI5dDE/BJUrjWH1GTHZq+mzwN6Nc+B" +
                "m7PnP+6q4yFTW4hcXfwGY7f8W30dmLv1yfY9+4EV8Z/DX8V+Ov" +
                "uz6LLc7f6bH8DePblDFmceNv4EVLn213GKPP+pJJbW2lpfQlfu" +
                "FhHk2ecdNn5O95dn3m/HXTZzt/bdfQ+Yu/pzl/2UEg7DisgGfY" +
                "UcHfpVBFacrbR8ENqM8r6SewjB0iufBseB6shlZ4zX3YPsqHNe" +
                "wY+97G38+d/M24LSNgXlPnLywR+ryWnWBN3PnL20d4rogY2c8x" +
                "b3E58neHO38NfY7lL9dnG3//ZeEbl78e7A28ZPAXevribwdv/r" +
                "Jv3PiL+Fa18I3HX1gVw99TtrrWCjbtNPwX1oUXaf6rjIHqwa80" +
                "/xVvmuP7nDLa8F+Bbzt2zPJfxOQcp/9iSf8Y/w1wfLG+E17+q9" +
                "YV+Ir2r4Wv3X9Dh6PxhR7O+CoaX7me03/h32a9PL5y8V+Or5Ll" +
                "zt9QONp/xdLbf/MsfO3+q+Hr7r+2qLGKFl8l8F+BL6yHgepbNv" +
                "4+z/mLSzO+EvxFfLGsOv4EvuFqAoFc5aaMXA1fZQL3X2d8peTx" +
                "+NmJr75fxxfXTHyd+owM663hq3CHzxZnecZX4dox8VUPBwN4fD" +
                "UsLr6V/eDLbnbHl/uvX3zhBcT3Ovf4iuOr7nXD16Enmb7w3Wfk" +
                "Nxz6/Iipz7NQnzewo2w24rsRqoSri/zVJq7P3H81fcYzUJ+VSd" +
                "JfKOoH1+dQpqXPYtnHIyIR+Br6zPkbbOGuzwa+3vosli76HHVk" +
                "FL5OffYTX7GZnnUnp8/57vosntBVn534pqDPLwqFr8gUeA02M4" +
                "CtrAi2wXbYQuvBK0zcHbzBatELaA7Fr4rWpEZmoyoti/PytBrF" +
                "9iptir/qrK7Y04QKptAKtBJthMvKtuuJGI0hT2lt51OwgNt7pA" +
                "0iPieS54avPb7yPLNm1Ps8aN7TzEhaJum2OBmKUVF3/VL0EawO" +
                "j68SPv8AWx1mrgReFzUcNPQ5VEYlmj7TQVb+mdWCpuwQ7aT+B3" +
                "Lp7Yb/8vjZ6b8sW8s/J9Jnji87gVsi/xxPnw3+wvuGPvP8cyx/" +
                "vfVZ9zAXfQaMUHn+OVafef7Z4G98fbbz10OfFdTnO939V+OvXZ" +
                "+N/HM0vt76bOSf6WBDnx3+O0rTZ6bQKXQyg1AWnUTvhr10HOyG" +
                "Xbj/IzqeFrJaXJ/pVAezBC7sGJ1gu5MWib4y+NDgL8c3MX9j9d" +
                "n/ZPDXyF95TfSIF3/Vqkle8QPH1j6zxhHe50TrM/zXjb8Jrtvd" +
                "zl/Ed4+5R2Tk4BMzft5Pn9X5u86Kr5j4uqgec3rlr/BOOviLr5" +
                "iuzfb4KlBL468oTxBf+eGv4b8e/P3UT3yl1ks9vhLx84Pe/MW1" +
                "BPFVfP4a8ZWkxc+fRcVXB5zxM/AWbEz8rOP7fEJ8OybGl+ev3P" +
                "C12kclga/0UxS+X/jCt0l68JWblzy+VnzlwPdLDV+TRQ3Z7ICI" +
                "aahDJ+hb9B36Ad1Jd9jK3kysWPRdv9oWyEkthomOr+Cr+PEV2/" +
                "H7xVdyq1TOpsPt/YN+4ivbWxDPAkfga/gWl4fhkLofvoPjcJR+" +
                "BMf84Mt2JocvfONxf7b2UXGn2PyVuSfG3ekwz2g37fgqK1PF18" +
                "f3PcYVX5FdlBsa+hyYR7/W9DkwH+Pnk1b+Cq9z2E2f2aeWPvvJ" +
                "b3B9Nsvt/rvod/XfE770+UB69FlZnYo+07F+9FnOcdXnUyJ+Lg" +
                "c/w6/wU/AmOA2/MJGZlfCLhzOJ+UtHpUeflWGRNE9+42dv/iaO" +
                "n+EHX8+2NiX+jvXlAa7+Bj+K+W/BxXp8VcSKVBWy9L16/5H+3K" +
                "JHg70BEdf46j6Nv/b+Izt/9TXJcXUbf3n7CBw5D+/+o4RvfZAb" +
                "vs7+Iwd/p7vxV9NnD/4m1X+krHfvP3LnL+8/cowrCXj371v8pY" +
                "Pd+o8IcP4GseWj4lPQZbgUAxSIQAcaGmcEn+X550gJTsF1Pll5" +
                "T3L8tcZvJM/fxP7L8xs++LspJf7GdW9j/JXHXpGdIIPUdnQpuY" +
                "PjG8fBSxTfdMRX6cY32fyGJ74vlxy+8eMrYrLaiK/EehbcGzyp" +
                "6TMZqrV/yRCiZ7L5+Ct3fVbz/emza35j0R9Pn+XMNOnztlT02W" +
                "j/xtdnx51Y8ZUYf0UrSgvJaAmxDS3QjxhH61n67Ld/Qe3q7F+Q" +
                "5kf3L+j1ufQvuLd/Y/sXktXnEo6v/Onz66nwl9VJVZ9Dz4Hmun" +
                "eJsrJkrJG/wnl9ji/3X2mR0T5yrQnbR2q3xP2D9vyVl/9CXI1O" +
                "rn/Qah/ppUmMv/LGN7nxV8pO7/5Bl7obu+Mbf/wVlpjtI1tZ1P" +
                "ir0B4yXht/JS3Xxl9JS7g+W+0jbfyV1j6K0efu0fqstY/86bP0" +
                "5J9Wn98+a/osxl+RCWSi7r8Npa3hKyGLTJLettpHHF9ph9H+jY" +
                "NvjxTwzfnj+S9dmCZ8D/nDV9qcbnzJZI4vLCBTNXyl96UPOH+l" +
                "96SPyBSOLx3P8Q1fj08r+gfJvWSaPb7i/YMmvj2LH19xff6j4c" +
                "v1OS34Hj67/OX9R/jT/+WD/nu/5r88voL6ZJbef9TMGJ9jxFew" +
                "BpfVbP7bS/Nf6TNHjFTZ7r98fI5bfOW4v+w48dVyaB8oStF/fY" +
                "7PCZxO5L/+xuco35Wc/xrjczz8V2TOaEUj/0weJnP1/t969v5B" +
                "e/xsjN9AfNtp8TN0pE0hD+Pnm3n+mcfP4v+DFaQDWvzs1v/L8b" +
                "XzV9xNTP7Zwteefw5mppp/Jg/5yT8HM9KTf2blS75/UMOXzHHm" +
                "n8kjWvvIwFf6VvrGwlc64sSXPJoA3z5R+B52wfeh1PF16V94OD" +
                "l8o/TZq3+haprwrfJ74RvTv/C8hq/5bIivrozoA9JxZ/uXPJ6g" +
                "/ds3qv172H/71yN343t83dlp//qbAnLJtX8TZLX0lmCU/y7Q/n" +
                "8EDcMLNf+lF7Cp3H9pTe6//P9Hmv/KTMOX+y/iW8D918IXf43Y" +
                "5GT9V/v/UXH81+3/R/qewuL4b3BWevw3UOqs+e+LJtKLxfxJLN" +
                "tGniJPsCl8O2OuMT6W42teUcdXIKDjK773foIrYQtfPHayv/Gx" +
                "dnwT8TeY6XFmDL7mnsLi8Df4QJr4e3USquM6PjZJ5YoaH0srkm" +
                "XwGnka8V1ivlmbovnOT97i1Gf5PP/jn5PR50BRsvqczGTh6x0/" +
                "J9k3NuNs4UvEfzTl87HsNeCt661yNW18O+4T/1zE9u8bevtoqi" +
                "sCNrap/RNe+WV7+7dkp9TxlTPTcyehmWeXv3I2vQqRusZEXag2" +
                "2cTHP2v8FUiWiUKWRder3prwyh/+P+GbrvHPodlJ1FHM8c+O7a" +
                "jxz9b4WPKKMT5Wbulo/9rG5xjjr6Lykzy/MSB6/JWVn4xuH0Xn" +
                "r9zbR275q+K0j3CtGO2j4Pb0tI/CLc9a++iAc3ws2eIat4iry6" +
                "0TfXvqQJ8R/5+UvyUzpdQ+EkoN/wMd6IDF");
            
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
                "eNrtWmtsFEUc/+/s7F57Ry0FWiqgSAUq0IIU0PAoihrej/BQ4y" +
                "siNAUfUBAEBfoQRA0gggpEXg00oQTQL37yC8YPfgCqFCgPE+MD" +
                "kAgW0ESNEZPzv7Nze7t7s3fX27K9rZ1kZnb+Ozt3s7/9P+dP8k" +
                "ECCkEISV+Fw9AFukJPUgQFeN0XimGw2o8MIPeTEeEw6UsKoRTG" +
                "4lVv0gXGk1zSh9wH08lwmEUKAotgLpSRYaQbvASLSB6dTobizB" +
                "6wElZAJVTjejLWbLU/UrFCd8gPmwr05/0g1g6FYWSwcW8k1ofg" +
                "UWwnsvEc1j4Fz7B+m2WdCta+jnWNQesE3VifC3lwp2luH6yFMA" +
                "AGRijyFaQ8gHV0oDe24+AxY+4UmMqvZsKTBnU+zGP9AlgIi2Ep" +
                "Xr2KdTVUYUuwqlgDkMlnZ0Fn1udAL065F2s/Y7UiKIER5t2ohT" +
                "AG6Q/DI9hOwjoZpsEMmA1PwOPsiafhOXgB+3J4EdtXsC6BZdi+" +
                "hvUyrAqH6QK8asZ6TToWDkvHpQa4ThfCTeMX+pEGcpI0kRPkeP" +
                "R3ybGwrQQW2ymk0U6Bq7he/7Crknk+mVkavqkUDV/+O2fDnhZa" +
                "LqKqhamvKJ1g7dfSGfKOdEo6KTWS9XS5dJrUGAitJVUq+7rIu6" +
                "IVyDoTvhXJ/apbfIPdvcE3mBtOg+IK328YRvnSWWimNci/TUg7" +
                "B7/DdZTPZv41yWeOK8pnbJl8xn441oLAEnYH5TPr82i1Lp9F/E" +
                "uSwjgqn1te3OOryWef48v4l24xxkLJp/Mv3ZpotcAyj/h3oUf8" +
                "W+57fC/o/Buxr+g2dWvUvpJ+sNpXdEfEvqIfC+2r5Tb7aru/7S" +
                "t1s+/tqz80+0rDV2JfrXRRs5+xcvtZugzFdvkMpSL5zPBdgU+W" +
                "meTzzqh81vFlV9mJ5LOOr5N8FuHrxL9mfHmPOlW6ZJtZKOJfGB" +
                "1sdFx7puma48tHBr5sRDjVwBevO/O+l8PaJWL+jeDLR7NjnjPw" +
                "NfHvz6y9hvR/JNSMcMvyxL8olXdF5LPYvrLI55W3Uz6TOq/1L6" +
                "lN+Bt/prl8/oVp1r0g0Tq6h+6TbtDD0IXWQ09aCwV0v/QrFNND" +
                "XP8eka6zPZVya363ZaezEF+NY8osFv9BrAdE/Bv3rQ2Ke9c7/m" +
                "32Pf826/qXfsrjG8xmhq5IK8J+K/TF1hbfkH4Txjc2onx+A+bq" +
                "9jPXv59E9S//D3KEf+PpXz5y0L9JcG6FiH91/Yt9noWq6d9NIv" +
                "2L9tUNR/27Iap/jbvva/rXNK7ivWqZlcX7HINi079YhfrXmMH0" +
                "L/Y2/Wv6DUP/SsD0byYJkhDJIgGGdmex/UxybPJSjZHPq72xn5" +
                "OU553cymf5Wb/bz/xNTCWTySQyjY9mpIzvGo/8o7+9wTf4V/vA" +
                "l16xvJejjPajGd8k45OVieOTfuLf+PENetEv+CqZlv2x96JkW9" +
                "5Vg5IVxVfOku8Q4St4x4ivEmqb7949viGSDvwrZ7te4W7LSPDV" +
                "kgasTUpOfP5Nr3K7+dczfPu45t+8GL9pkca/MCQqn8386yifq5" +
                "LjXzfyGd70zv8NJfxC4Pm2ls+wNu7ddbp/JI/U/SNOje8fbXD0" +
                "j6qt/pGS73P/qNH3/tHbmn+k3BOJP+PuxuvxZ2WI5XzfZF/p5/" +
                "sa/8J4vt50Ht+o0eLPjIL4Ru0rc/wZPorGNxLHn0332iD+HBrh" +
                "3/izMpDhu0OPPytTjd1N5JK1yCJnkzof5HeM+LMyJfZ80FjRB+" +
                "eDoQfTQf8G1rvQ3ROY/q1U1kh1Cn5v8hyhBmBfl1Kd8J8kPEGE" +
                "L7zzj/x5/gtHY/Wvxr8tWuNLw46qZ8jVIM2mf5l9FdW/JvtKni" +
                "+UzxvxfezS9K9dPishkf6NJ5+j+lckn73Tv3Hyr3yif+Uypn+R" +
                "7xR2xi+visO/HyTk3yPpFJ9shfN938evIuf70hllj55/xXZYad" +
                "W/gc/s+pd8l5T+3Z2++pf85Bf/l7iIk8nMvlRqEd8tGr6y4U1l" +
                "xPiyGv/SHumw49C8/xP/0p5u8UX7+SA0Z1yAa/I65YD8lpYfG8" +
                "2vo4tpRYr2c704v44udce/IWFeVMb5dorvstSf1fPrTHtDWzzj" +
                "e4co1+Gwr0p7yZ8MZLjXvyDxvW2z5l/hVbHd/4XSzFz5DlH+FZ" +
                "tvyr9SPhfnXyXSv4nyr0LV7vI35O22mU75G47862X+RuR8IfX8" +
                "Kx1fhceWY/HNnKjjK++M5NfF2lcifM35z62Jb2z8quX4mu0rJ3" +
                "yd+ddLfCP2Ver5OfGKctr4HVN+nVKSoi55L13k8+2wn+mmdLOf" +
                "6WbW7sVap5yh+5TtSpOyg9bjuBbrfnb3EJ+L3m3my6Zndyexvp" +
                "Ff16r2c1Irtpf8DTf2s+lrN+JXwbu0+BWj8fgV5/VRfP88foV3" +
                "bPErbG3xKz6yxa8MugfxK6nc7/GrQIbb+BWLb1yy+r/xzxeUkq" +
                "T8o4sd8Y10iG9w+XwV5fMtejh401k+W+RGm8nnDv+oZf6RLd4p" +
                "4BnSoA5InL8heK6xDb/7dqJ/W7uo41g70IpvMvmT7Q3f9ODfVk" +
                "B0CJmM74PnP6sTHN5Xwvxnr0roXAf/poj0KLlJnYJ7PCXzd0jW" +
                "ivxf01tc1wb4ftuBb4skmcVLCYxOZD8HxsTGJ0X2szqmLe3ndp" +
                "Mf6zr/WS01y+fA/HSXz51oB/+2CN+xFnw/THd8O+yrFnqK/wGq" +
                "Epqv");
            
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
            final int compressedBytes = 920;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm+9vDEEYxyfP7T13eq1fqWq8kSKlIo2mmnilSF9wqkKitN" +
                "HQd9Vo0mja0NBIBX2BeOFHBCHeCULwwgv/QDl6pbzVEP4ACa/I" +
                "mpvs7t3c7V13r+tudj2T7M7OdG5zM5/9fmee2auupxO2Ru5lFH" +
                "UYt64mdJsEZ/WSp8pmJ62gsri7h76bVxW/9EAk3ApxPh67dT3y" +
                "Q9fDN/KM15KsMpbr+1ZuJr6u+G63+P70A19nifhaI9EOcWxL8R" +
                "WlPcrr9wDxdaXfHaZ+Uyl6RXm+PcTXbWIaP2KC7zPBfKc0VgmY" +
                "ghl4A69F2y2ibtL6bEfO3frZgGiTNMojRh6SWi1ntVK5Xio1sY" +
                "x1VPQpr2k1+PY66VH0edZ3qrbtd504N7D19nzZNtZmtd3F2q1r" +
                "Gw9hfVJpzMglHbAqI7fUwla7JMW1yLjHsk6j3M0PaUTYIBvi52" +
                "E+Bi/YKGcZz9RvXj14oN/oZ0/02+doHE6QfqOzhkPvTfNdMEbr" +
                "q+D5s8F5X2imYob3cTr0SeH4d4T4Fsl3P+c7qzzf88TXJdfO0s" +
                "y/2qUSrhjnPf+67t1F1bhql3PrYl8l8gcL6zfI829QEtTiIaZh" +
                "F4tp18z4CDZILRpgI/ZgN6yBtan4SLsJK2Epr18GdbCOdcAmfr" +
                "3KaNsM1an4CGq069DEyyvs4iOodxIfQaNdfORw7Tjv+Ej8JQDx" +
                "UUHnPuylfr2Jj1T1Z1XjI6i10XSOfqGF50K/nHpvpn55nqVfkd" +
                "fgEVO/Nvevd+QsjaUfk+D5s+3IZvHlh+Br1Uh88agdX34QXyXm" +
                "37n5Yn+mfrP52uuX+KrHF4858WdnfHGA+PrkCTD8OaNG8OW54C" +
                "utyAbT+hU58Q0EXzxO/ux3vjiUny/Nv6omHJ7v+pnWVz5/Akac" +
                "z7+8juZf38VHpn7xJOk3gPodJf0GlOwpa6wn/o8eB/P9ESfZpe" +
                "up90f59jewB0+b+xvy+6N8/px+f0T6VX3+LWr/6irx9ZFT3yJ/" +
                "9q0/38U7c+kX77vW723SryJ8H+BjfOS5Pz8kvop475PS/L6O/L" +
                "ksdF/l1oUXSSwT4ar0/6cYdZMO9Jfkd4oR37L780d8i1P4jrP+" +
                "gAmc9sifk+TPnmvxvetPvBTnb8aInssa4XEYK7S/UY7ft5N+/8" +
                "mT85viI7/vbxTav4IW/EP7V75lfMH050iIn8+QPwdRv97Gv5GF" +
                "pF9V+EYWe883/IX4qpDYX6tSRHg=");
            
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
            final int compressedBytes = 485;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmttKw0AQhmVMd68VrUUEUaRaEQ9on8FjVcQbQS98CG98go" +
                "IKRagHaBXBs9QLfQq1YMU+g6+xriUVY1MaIcXs5B9YJpkEEubL" +
                "P5vZRCkvJttVKKz1Q4XIZLS6RWnwNdMo5hIbcewlaJyS2g/QoB" +
                "3pJa1n6qQ+GtJ+So9++8gkdVR8VHbRhPbdrteMe7qzUfD1g6+M" +
                "NYOvVQBf1vrtAV/WfOMm8uVnTeM7DL6GPAEJPZKOSIWv9hW+jj" +
                "fusSpf+0zw/e8eaFqpSKbRWZF9ZMrc+ixn/K/PkRzqcyD0Owv9" +
                "hkbLDzbzuZojj8iOsesby3JRpuSCXBLvcl68+VOfRQn1OXC1eq" +
                "VurqFf3uRXkQNDyW2Snm0p1aBWtv3aF8icOf1RTcyP9astzL+B" +
                "1fS2I89FeqUyvdDzj9iTBz4lZNII2juaVVmPP/KFGcJ3F3zZz9" +
                "Kar9wDXyaKzWD+DZF2i1/6lVnUZ3Y6PvzmmUY2TO1/xbpSYk0p" +
                "K1u//xUb8qja/1rHnv6vO0D/GwiN5rB+xZpvHnxZ8z0BX9Z8T8" +
                "GXNd8z8GXN9xx8WfO9AF/e/a9LzI/vv5fofwOh3yvolzXfa/AN" +
                "HfMbB0t8PzKZ5S30y5rvHfiy5lsAX9Z878GXs7V8As79MVg=");
            
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
            final int compressedBytes = 536;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrtKA0EUhmWymZnCRklEbCQiXkBEUSvx1uaqrSJaKGInCF" +
                "oKiXhtRFCx8A0ELRQsvGAjCt4jPoPJG1iNa0wCxmSThQ3MjP+B" +
                "ZJLNLBPOl/8/syfLjohfCBISlkGqct5TgVAi2DH4ItgJcqABxd" +
                "OC+k0iO2oGqWXnLMxCLMgiNM4C9MU81vZrRivpID3m2Eia00fq" +
                "SbX57CU+0mKO3eajIf1JF/Gkxhr6TDrNsS7vmk0lfbN20HGE7w" +
                "V9oE/0UQgap/fgK2/QV9tnnJl8L/9k1gG+7gD4yqHfPMcc4Muu" +
                "wFcSf76GfrXme4P9FfzZtj/fgq8k+r3D/lnn/XOZ9PsBvlr7cw" +
                "J8teb7Cb468+UV4Ks1Xwa+8vDlHqf5GkvgK3twbzbXa8iGqvrl" +
                "vehfof7arr994CuJfgdMJx5MdTvivB/6lbheDtk947t/xcPl0K" +
                "/rAHy19udh8JVC8yNCuLeKzXJvI1PK1t9xPsYnrfXLJ2zrdxT6" +
                "lYJvkPj5dOb+dhIpMEux+9tJpXI+OlX+NVxvqZVmXO/pLEWt+x" +
                "skBn0oXZtnrfkiFOc7B764Piqwv5rH/kouvnzBlXCwv5EEX+n9" +
                "eRH+rMH17/LPK2PHyp95LKNfY78kf45CvwopeQU5UL3+FtHvqj" +
                "39GrvQrxTKXOcbWcK+nAyjv/FffgWbyIHWfPH/kcL1l+/RQ8f7" +
                "G6i/UkTFF9nAJ+U=");
            
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
            final int compressedBytes = 565;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnMtKAzEUhjVOM4m6qahIRUQRtSCiaPeubWt3iqAP4EJQl6" +
                "J061IEBUEUFVS03bsXFMF7ceETeFmJeMPNmJapdIbajjCFZPwP" +
                "pGnSTC/n659zEtKyhGH4Fo0i5lsyYEoaaWBHNEkTlr4uSytIek" +
                "hI1G2kw+xpJjXito60kE5R94vSaj7SR2ozdT09IL2iDuR9zXZH" +
                "76wbdFzgGyGD7IREzVbsl1F+W5tK/qmqVePAjkul3zx9LuiXnU" +
                "K/Cn27UvCBqvE3e09bLqRfdpvVr7bqRL/aCvSrFN878FU0f34Q" +
                "8++92/FXi4OvJHyfSpJfPYKvXPOzy3yfwVcuvuzFGV/EX+hX6P" +
                "cVfD3N9w18lVofvWN+VjR//iiFfrUk+Mpg2rrIrD5FvWm2d0XZ" +
                "EGUrk3N9mb0J21VrDp55T5QdeFh2Y2kthSzKyuhX1Bn92lRn6t" +
                "dsQb/SGy8HX09H6KBzvpyA7/9YH/EK5Fee5kvBV6EYzOADb+9v" +
                "cI79DczPOfNzJfjKxZdXOeML/Xow/vrhA1X1S8cMg44Wjr90nN" +
                "dAv4i/OfE3AL7y8OWNbvPF+TqP67cJfKXPqgZ+fL0Ab0C/Fv2G" +
                "oV85+PJhfUiP6hE9RlN6mF679PvQK/CVhO8IPaeX9EKsklL0DH" +
                "zlNXrz5ysOHe8/T2D9q6L55vmkg1FxeMrTmfQUfKCmpc9Patt8" +
                "On1+ks/Yz09q++YonJ9UWZ2zRedn/H8O1r/W9e8c8isZrOwb0I" +
                "DhTA==");
            
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
            final int compressedBytes = 543;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmzlLA0EYhs1kM7szoIWoBAsvYkxAglFjKVp79YpopT/AH6" +
                "CYwpQiKAhWgqTQ9PaioCIYIqKd2nlUFrbrGmIuYg4ykZnh/SCZ" +
                "7OxuCN/D+x2zE+JlG3aRkcGCoyAZIhFn9JGBzEw3aXXe20kvCT" +
                "jjqPPqy5wZIW3psYOtk7AzdtoljPjtKoyEbFjdxqK27dmudJVn" +
                "B55S04iXbTVCv5449CsH3xJzIuJzDHwl0e+ROWvOmNPmHE2ZUz" +
                "Qphi+9BV9J+MZt25r4+UxT1jj4ymvWZK130NP8I/dd+ltW3fcZ" +
                "D29mfR0rSSAqTZV4BvqV8q+xWy7/svNf/Rr71ejX2IN+5eHLLh" +
                "zdfv7Nly7m4jP4KtgFX1Y4fwUfoT8q6I+uoV+l8u8N4jP0m+Nr" +
                "rf0nX5YEyTr1m4J+la+yHoo8rEz/C6uT/CN8oDXfJ/hA1fzLni" +
                "vXV/Sw5vXnA+RfrfvfF/CVhy97E873FXwl4vshnO87+CpUXyn5" +
                "q10rIFecf7lLjH55E/QrB1/uyaPrFhWfuQG+sumXW9zE/hyd4z" +
                "Pn0K9WddOXE5+baYKeiK6f6TH4SpJ/W6wFa7k8X2up5ueD8+Cr" +
                "jvEu+EDd/Mt7iuZE/D8F65NS9r/C6isf+MrFt/z+Dd6P/RuK1l" +
                "f+RujXSICv1vE5AL5a8w2Cr9Z8Q+CrNd8w+Eq/qjGc9XUM3oB+" +
                "C/QbgX615jsGvjJY0zct33IJ");
            
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
            final int compressedBytes = 494;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmj1LA0EQhuNyt1ymFBWxkYj4ASKKipVBECsLe0G0sbTyD/" +
                "iBIFYiGvA3qL2iCFrYqPEjqL2dRkEFC23WQ2LkwnG54CbsTd6B" +
                "ZC93CQnz8L4zsxelwgQNKkQkQzT6nOvyvOoUPWLAXVtFe+5Ms6" +
                "h1n+tFQnS4a7/7aMld6RN1P2sDDYled23y/c62UL+sG3TKG5TM" +
                "53oF2WBNehg5gD97/HkE/mwKXxrVz9d+AF8jnHdMKXut2LvsdW" +
                "SKhz9TjSZ/jkG/EVL5OHIQVf3SpNyVO7rrr9yGfs3yZ2sziC/N" +
                "/PK1tsLwtVLgy3o+mgVfQ/x5Tl7IS5lWSmbkubzW5M9X4Ks75E" +
                "3Jn9jzdFHzBRlezB/57k+KJeScSf+8gByg/nrq7zL8mTXfVfA1" +
                "3pU3gusvIuJ8U+DLzZ+x/8yXLzlOEvNvVTr1PnIQXf3SgTMd3D" +
                "87U6Xq15mAfs3xZzrUPR+Br2n1t8j9oyPcP2JXdY8xH1WNfk+g" +
                "X9Z8T8GXnT+fwZ857W9o7Z9foV+z+Drv4fiG82dKg68RHpz5O7" +
                "YSBRnG/zeY6Df+pNuf6baS+qU7kAzk+6ybbzxbUb73IPkvF88i" +
                "B+ifPf78gv7KDL70Vpb56BF8Wev3A3xZ8/0EX+O7qq/g+Rdhfs" +
                "S+AQlWcFQ=");
            
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
            final int compressedBytes = 426;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrtKA0EUhpPD7G7IdKIiNuIFY1S8oBZ2WsdLYWUhFgo22i" +
                "rYKIoaSOMFAz6BlZ0PovgGvsi6aAwMakxwAzPjdyCZXLrz8Z3/" +
                "hIz0xF9Kxo13RZmSueQckkLtkz7pSJ67pF9GknM2eQzUvpmRzv" +
                "ezW2dkOjl7429KhuMmSiZi6s8l7eKbha/XfBV8bS8d1Htdphv4" +
                "a/ir8dcuvuq2EV89+MlX3TXDV1Xha/18LjCf3fdXF6OVaDlail" +
                "bDl6gUPqczn8Mn/LWE72g78jcowdfr/WoMvpb4O8l8xt+W/Z2H" +
                "r/X78wL7s9d8F+HrQf6ukb//3uR1esB+ZexXG/hrvbWb5C/+/u" +
                "DvFv56zXcbvtbP5x3mswe/j3Y/XjX+/zd32tr/v7kT/HXI5D16" +
                "QP4a+buPv/bw1Qdp81VH8LXR33w2Hb557re7lL+H9ID8NfL3GH" +
                "+95nsGX6/5nsPXLr6/3G+/4H67m3x1uR3+qgf4OrQ/V+gB+Wvk" +
                "7yX+es33Cr5O7VfX7FeO7lc37Fd++6ur6fMNXuFr/dZ8X+819z" +
                "f8Jv1ID9yszBsyNPWN");
            
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
            final int compressedBytes = 374;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2ktKA0EQxnEpBhnsWYmKuDAkig9QMWjW3iDrREHFC7jQqy" +
                "h4CLfiBTyCKxFd+QBfCzXLsZGI0xDiZCF0Ff+CpMlkMaF/fFUz" +
                "Q/K8TLmznFJZMtnj2FLwaVFWpeHXWZnvHqnIqH8fl6os+HXdv2" +
                "rdb9Zk7HudcOdS9+tUz3POlfplK+hE7HuBb1y+yVE/X3f545uc" +
                "lPFNjvFV5XuFL/351zd9w1dPuWv2gPwG11c35Ne07y2+cfi6u3" +
                "Qr3e3vm+4MPH838TWd33t8Tfs+4Gva9xFf075P+Jr2fcY3Lt8/" +
                "nk++8HxS6f3R63/kNznFV0+5d/aA+RvM3w/yq2r+fjJ/lc7fDv" +
                "OX/GbD5NduZSPsgWlfxx5w/Vzsz9k0/VnV/K0wf8lvIb9VfE37" +
                "1vBV1Z9n6M/kt5DfZXxV5XeD/Bq+/22yB/TnoD+3yK9p3za+pn" +
                "35f7tt3218Tfvu4avo+nmfPSC/QX4PyK9p30N8Y6ihLzXnXWE=");
            
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
            final int rows = 9;
            final int cols = 120;
            final int compressedBytes = 129;
            final int uncompressedBytes = 4321;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjkviPAZh0UHiaTAZMpkBahUkdKiLPJAQkRZkUmTSAtAkQK0" +
                "FljJlEwLQYTymTIZCW+o8FMKn9JwIw6f0fBRQDJlrFb81o/A7r" +
                "+K0fjd9hHb8No/E7rOO3eTR+h3X8ThiN32EdvxNH43dYx+/k0f" +
                "gd1vE7bTR+BwNgAAA8umcf");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
                "eNrt3UsKwCAMBUDvf2jjCdRFCEScOUA3vnwshUaczAHshfpBPt" +
                "UPQLP+d3u+/itfAAD2SwD3P/dPnC8Av80n+5P8eL/h/J0vgP4O" +
                "XfL1en7VH6gP5AcAzFcAAPsHAAD2VwD9EQAwvwEA8x8AAAAAAA" +
                "AAAAAAAOpU/7/R9/lJCxfmGf0=");
            
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
                "eNrt1DERAAAIxLD3LxpwwMgxJBI6NAEAAAAAAAAAAAAAAAAAAA" +
                "AAAOBSr0ogAH8FAAAAAAAAAAAAAAAAAAAAAAC+GEUnPcE=");
            
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
            final int rows = 798;
            final int cols = 8;
            final int compressedBytes = 59;
            final int uncompressedBytes = 25537;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0zENAAAIBLH3LxpwwMLC0Eq45BIAAADgqlclEPgbAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAPhsALF/PcE=");
            
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
            final int compressedBytes = 167;
            final int uncompressedBytes = 18849;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2LkRgzAARFHhC+MLmoK61DhHAQ7s0Gbf/JRo34gB1VI/NM" +
                "61aB+VL57hzVu8xVu8xVu8xVu8xVu8xZv3e9PJTlHeLztFvc9X" +
                "O0Wd74OdorwbO0V53+wU5f20U5T31U5R3hc7uV+T73Px1t9593" +
                "aK8rZTlvfDTlHeg52i/scWO0Wd79ZOUd5nO0V5H+0U5d3Zyf2a" +
                "dut9t5PzLd76fe+yAX2YXcM=");
            
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
