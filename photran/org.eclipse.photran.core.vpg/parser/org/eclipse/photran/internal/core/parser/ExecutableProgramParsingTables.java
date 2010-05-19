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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 19, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 110, 189, 190, 0, 191, 192, 101, 1, 29, 36, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 58, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 58, 3, 1, 8, 123, 4, 124, 0, 5, 127, 221, 125, 237, 6, 128, 7, 126, 0, 173, 238, 209, 212, 8, 214, 239, 215, 88, 29, 9, 216, 217, 219, 218, 101, 29, 114, 10, 220, 11, 240, 222, 12, 13, 227, 0, 14, 228, 2, 129, 230, 150, 241, 231, 242, 15, 16, 243, 29, 244, 245, 246, 17, 247, 30, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 137, 259, 260, 261, 262, 22, 23, 263, 264, 24, 265, 266, 25, 3, 267, 268, 269, 26, 27, 152, 154, 28, 243, 270, 271, 237, 241, 272, 273, 4, 31, 274, 39, 29, 39, 244, 275, 276, 277, 0, 88, 278, 39, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 56, 289, 30, 290, 291, 156, 6, 292, 293, 294, 245, 295, 296, 297, 238, 298, 299, 103, 300, 7, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 29, 39, 311, 32, 312, 313, 33, 314, 315, 316, 5, 317, 34, 318, 319, 320, 0, 1, 2, 321, 322, 323, 29, 35, 324, 325, 58, 326, 239, 327, 144, 328, 8, 329, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 330, 255, 256, 331, 242, 9, 173, 10, 332, 36, 333, 334, 88, 335, 257, 336, 337, 338, 258, 180, 250, 259, 339, 340, 341, 262, 264, 342, 343, 101, 344, 345, 346, 347, 348, 349, 11, 37, 38, 350, 12, 13, 14, 15, 0, 351, 16, 17, 18, 19, 20, 352, 353, 0, 354, 21, 355, 22, 23, 24, 40, 39, 26, 41, 28, 266, 42, 43, 32, 44, 33, 34, 36, 356, 357, 37, 35, 38, 358, 41, 359, 360, 42, 45, 43, 46, 361, 47, 48, 49, 50, 51, 52, 53, 362, 54, 363, 55, 56, 364, 365, 366, 57, 59, 60, 367, 368, 369, 61, 62, 370, 63, 64, 371, 65, 66, 67, 68, 372, 69, 70, 71, 72, 373, 374, 375, 73, 74, 75, 376, 76, 77, 78, 79, 80, 1, 377, 378, 379, 380, 381, 81, 82, 2, 83, 84, 85, 382, 86, 3, 87, 383, 88, 89, 90, 0, 384, 385, 91, 92, 4, 46, 386, 93, 94, 387, 95, 6, 388, 389, 3, 390, 4, 47, 96, 5, 97, 391, 392, 98, 6, 99, 393, 394, 100, 102, 7, 395, 104, 105, 396, 48, 49, 397, 106, 8, 107, 108, 398, 109, 399, 400, 1, 401, 402, 403, 404, 405, 406, 123, 110, 111, 112, 407, 408, 9, 113, 114, 50, 116, 10, 117, 118, 0, 8, 11, 119, 120, 121, 12, 409, 122, 123, 1, 124, 126, 127, 13, 129, 14, 0, 128, 410, 130, 131, 132, 133, 134, 411, 135, 136, 137, 412, 138, 139, 140, 413, 141, 414, 415, 416, 142, 15, 417, 418, 419, 420, 421, 422, 423, 143, 145, 424, 146, 425, 147, 17, 148, 181, 426, 427, 8, 428, 149, 150, 19, 151, 152, 429, 117, 430, 15, 153, 154, 155, 25, 156, 157, 20, 19, 158, 159, 431, 21, 432, 433, 434, 160, 435, 436, 437, 438, 161, 162, 51, 0, 163, 164, 165, 166, 167, 439, 168, 22, 440, 441, 442, 443, 169, 52, 170, 143, 171, 172, 173, 444, 445, 446, 174, 175, 176, 177, 23, 8, 178, 447, 448, 449, 450, 451, 452, 179, 453, 101, 454, 455, 456, 53, 180, 457, 458, 181, 459, 460, 461, 462, 463, 182, 464, 465, 251, 466, 467, 185, 183, 184, 468, 469, 470, 471, 472, 186, 473, 474, 187, 475, 476, 477, 478, 188, 479, 2, 480, 481, 56, 189, 482, 483, 484, 485, 486, 190, 487, 488, 489, 490, 191, 192, 491, 492, 493, 101, 193, 494, 495, 194, 496, 497, 195, 498, 499, 500, 15, 260, 27, 501, 196, 502, 261, 503, 267, 504, 269, 505, 270, 36, 197, 198, 199, 200, 24, 506, 201, 507, 508, 202, 509, 274, 510, 511, 512, 15, 275, 513, 7, 8, 58, 9, 10, 203, 514, 515, 11, 516, 517, 518, 16, 148, 519, 17, 278, 520, 61, 0, 3, 521, 522, 523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 28, 533, 168, 534, 535, 536, 29, 174, 537, 538, 179, 539, 540, 32, 541, 35, 18, 542, 543, 204, 205, 544, 206, 545, 207, 546, 208, 547, 548, 1, 549, 283, 3, 550, 288, 210, 551, 552, 211, 553, 212, 39, 554, 555, 556, 557, 558, 559, 560, 63, 64, 65, 66, 213, 561, 562, 563, 73, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578, 579, 580, 581, 582, 583, 584, 221, 585, 586, 587, 223, 588, 589, 590, 224, 591, 592, 74, 75, 76, 88, 593, 4, 54, 594, 101, 103, 595, 215, 596, 597, 209, 598, 599, 600, 601, 5, 602, 6, 603, 12, 14, 604, 605, 606, 607, 26, 608, 609, 610, 217, 611, 612, 219, 220, 613, 104, 614, 615, 616, 617, 618, 619, 222, 225, 620, 226, 621, 182, 622, 227, 15, 623, 624, 625, 626, 627, 105, 107, 628, 629, 630, 108, 631, 109, 114, 115, 116, 228, 632, 122, 633, 634, 2, 635, 123, 125, 130, 62, 20, 636, 637, 229, 638, 639, 139, 141, 143, 144, 145, 55, 640, 641, 642, 232, 643, 644, 645, 146, 7, 21, 22, 646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 158, 4, 661, 662, 663, 160, 161, 159, 664, 162, 231, 56, 169, 170, 172, 173, 665, 178, 179, 180, 666, 181, 182, 183, 667, 6, 184, 185, 186, 233, 234, 57, 235, 236, 668, 59, 185, 62, 67, 68, 69, 669, 670, 8, 9, 671, 672, 673, 674, 675, 676, 677, 678, 679, 680, 28, 29, 30, 681, 682, 683, 684, 685, 686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 205, 696, 697, 698, 699, 700, 701, 702, 703, 704, 187, 705, 188, 706, 707, 708, 189, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 24, 25, 26, 27, 731, 732, 733, 734, 735, 190, 736, 191, 737, 192, 210, 193, 738, 240, 739, 244, 740, 741, 194, 742, 63, 743, 744, 745, 746, 747, 225, 748, 195, 749, 750, 751, 752, 753, 754, 755, 756, 757, 196, 758, 759, 760, 761, 201, 762, 763, 764, 765, 766, 10, 767, 768, 769, 770, 771, 772, 773, 774, 70, 7, 202, 203, 775, 776, 777, 778, 779, 780, 781, 782, 783, 214, 64, 215, 217, 784, 71, 225, 226, 230, 1, 233, 234, 72, 235, 236, 237, 238, 239, 242, 247, 250, 251, 254, 255, 785, 289, 246, 786, 787, 0, 0, 58, 39, 788, 789, 790, 256, 263, 264, 73, 267, 74, 292, 791, 65, 792, 218, 220, 222, 228, 231, 243, 248, 793, 249, 238, 794, 241, 795, 796, 797, 798, 799, 40, 253, 75, 800, 801, 258, 259, 8, 802, 295, 803, 266, 268, 79, 804, 299, 805, 269, 270, 271, 272, 806, 807, 301, 808, 245, 809, 273, 274, 276, 810, 811, 247, 248, 812, 249, 813, 814, 815, 250, 816, 817, 818, 251, 819, 820, 66, 252, 253, 821, 822, 258, 254, 823, 824, 825, 826, 259, 827, 260, 828, 829, 830, 67, 261, 831, 262, 832, 833, 834, 835, 80, 277, 278, 836, 1, 81, 73, 82, 83, 77, 84, 78, 85, 79, 837, 279, 280, 281, 838, 839, 263, 840, 282, 841, 264, 842, 266, 843, 844, 88, 101, 58, 283, 284, 61, 308, 110, 268, 845, 63, 846, 269, 847, 64, 285, 286, 2, 65, 287, 86, 288, 289, 66, 290, 848, 291, 311, 849, 850, 1, 851, 310, 852, 292, 80, 853, 270, 854, 855, 293, 294, 295, 271, 856, 857, 272, 858, 859, 273, 860, 861, 275, 862, 87, 296, 297, 298, 86, 299, 300, 0, 276, 301, 302, 303, 304, 305, 282, 863, 864, 865, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 1, 866, 319, 320, 321, 322, 323, 324, 867, 325, 868, 869, 326, 870, 871, 327, 329, 872, 330, 328, 331, 334, 333, 335, 87, 89, 90, 91, 92, 93, 94, 95, 99, 100, 101, 102, 106, 110, 873, 336, 67, 111, 337, 338, 339, 340, 341, 874, 342, 343, 345, 347, 344, 346, 875, 349, 356, 358, 360, 350, 348, 352, 367, 370, 371, 372, 375, 876, 376, 353, 877, 277, 0, 878, 354, 879, 377, 880, 112, 881, 357, 882, 883, 884, 279, 280, 378, 359, 281, 361, 313, 362, 380, 885, 886, 363, 364, 365, 369, 285, 887, 374, 366, 379, 381, 382, 114, 384, 386, 68, 387, 888, 383, 388, 389, 391, 393, 889, 394, 890, 891, 892, 286, 395, 396, 398, 400, 893, 894, 895, 401, 896, 402, 403, 69, 399, 113, 404, 407, 406, 408, 409, 117, 118, 897, 410, 898, 899, 287, 411, 412, 900, 413, 405, 414, 416, 2, 901, 902, 903, 904, 424, 425, 426, 427, 89, 428, 905, 431, 430, 432, 433, 434, 435, 436, 90, 437, 439, 316, 440, 441, 318, 442, 906, 443, 417, 418, 907, 908, 419, 909, 910, 911, 444, 445, 11, 912, 913, 420, 446, 119, 120, 121, 447, 91, 914, 915, 916, 290, 92, 296, 917, 918, 453, 421, 919, 920, 3, 921, 922, 923, 924, 93, 925, 124, 926, 927, 928, 448, 929, 4, 930, 931, 450, 932, 933, 94, 6, 934, 935, 936, 126, 937, 938, 939, 940, 297, 941, 95, 97, 942, 943, 298, 944, 449, 455, 456, 451, 452, 454, 457, 459, 70, 0, 458, 1, 460, 2, 461, 71, 463, 99, 2, 72, 464, 465, 466, 468, 462, 467, 469, 470, 127, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 3, 300, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 501, 503, 504, 304, 505, 302, 506, 507, 509, 945, 129, 510, 511, 514, 4, 303, 508, 512, 515, 513, 5, 518, 946, 521, 516, 305, 309, 517, 519, 522, 523, 524, 525, 526, 6, 312, 527, 528, 530, 529, 531, 532, 533, 534, 535, 536, 537, 538, 539, 520, 947, 948, 540, 541, 949, 950, 951, 314, 542, 543, 3, 139, 141, 544, 952, 545, 1, 953, 954, 4, 547, 546, 142, 100, 12, 549, 955, 548, 550, 123, 73, 956, 957, 551, 552, 553, 958, 315, 959, 960, 316, 557, 961, 317, 7, 962, 963, 319, 964, 965, 966, 143, 559, 554, 555, 967, 556, 558, 968, 320, 969, 560, 323, 970, 563, 971, 321, 322, 564, 561, 562, 972, 973, 974, 975, 583, 976, 977, 978, 324, 979, 980, 144, 981, 0, 982, 983, 984, 327, 985, 986, 987, 988, 989, 990, 145, 101, 102, 103, 147, 148, 991, 150, 151, 153, 154, 992, 993, 104, 994, 995, 74, 996, 997, 325, 998, 584, 565, 566, 567, 568, 569, 570, 328, 999, 155, 1000, 1001, 124, 75, 1002, 76, 1003, 5, 571, 591, 77, 572, 156, 580, 105, 573, 574, 125, 575, 1004, 1005, 332, 1006, 331, 1007, 1008, 581, 1009, 594, 587, 1010, 588, 1011, 1012, 332, 106, 1013, 107, 595, 593, 596, 598, 599, 601, 600, 1014, 1015, 602, 604, 605, 576, 1016, 606, 1017, 1018, 607, 1019, 608, 1020, 577, 578, 579, 582, 609, 1021, 610, 157, 1022, 1023, 585, 611, 1024, 613, 612, 1025, 1026, 1027, 614, 586, 6, 7, 615, 616, 1028, 617, 618, 351, 1029, 1030, 1031, 344, 619, 1032, 368, 1033, 359, 1034, 620, 589, 1035, 1036, 108, 590, 597, 621, 622, 623, 624, 2, 1037, 1038, 1039, 126, 78, 592, 79, 625, 1040, 361, 626, 1041, 1042, 1043, 1044, 362, 628, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 1052, 631, 634, 1053, 636, 1054, 637, 1055, 633, 363, 1056, 635, 638, 163, 364, 1057, 641, 1058, 1059, 164, 1060, 1, 1061, 1062, 639, 642, 644, 645, 643, 109, 9, 646, 647, 13, 1063, 648, 1064, 1065, 1066, 1067, 365, 1068, 369, 1069, 165, 166, 649, 80, 1070, 1071, 1072, 1073, 1074, 652, 1075, 650, 1076, 653, 373, 651, 378, 654, 1077, 655, 110, 1078, 1079, 10, 656, 657, 658, 659, 660, 1080, 1081, 662, 1082, 670, 661, 388, 663, 111, 1083, 1084, 11, 1085, 672, 665, 389, 1086, 394, 1087, 667, 1088, 1089, 167, 168, 1090, 171, 1091, 397, 1092, 404, 406, 1093, 1094, 81, 669, 1095, 1096, 1097, 0, 1098, 1099, 1100, 1101, 1102, 1103, 671, 1104, 1105, 112, 408, 1106, 1107, 1108, 673, 674, 675, 82, 676, 1109, 677, 678, 1110, 679, 1111, 1112, 680, 1113, 1114, 1115, 1116, 174, 681, 682, 1117, 1118, 683, 684, 1119, 0, 1120, 1121, 1122, 175, 8, 176, 685, 686, 1123, 687, 177, 688, 689, 1124, 690, 1125, 193, 197, 1126, 407, 333, 1127, 691, 1128, 692, 1129, 693, 1130, 1131, 695, 694, 696, 1132, 12, 409, 1133, 697, 198, 1134, 698, 1135, 699, 410, 700, 411, 412, 1136, 413, 701, 1137, 1138, 335, 702, 703, 1139, 1, 1140, 1141, 415, 1142, 1143, 113, 1144, 115, 1145, 424, 1146, 425, 1147, 83, 3, 4, 704, 705, 1148, 127, 84, 426, 1149, 427, 706, 1150, 9, 1151, 199, 707, 708, 1152, 1153, 709, 200, 345, 710, 711, 712, 713, 714, 715, 128, 716, 1154, 428, 717, 118, 1155, 119, 1156, 1157, 1158, 204, 1159, 718, 13, 1160, 719, 720, 721, 1161, 722, 14, 723, 1162, 725, 726, 15, 17, 18, 1163, 727, 728, 729, 1164, 730, 205, 731, 1165, 1166, 732, 733, 1167, 724, 429, 734, 735, 338, 736, 737, 1168, 1169, 1170, 738, 739, 740, 741, 2, 129, 85, 121, 742, 743, 744, 1171, 1172, 745, 1173, 430, 1174, 341, 124, 125, 0, 126, 127, 746, 747, 206, 86, 87, 748, 749, 88, 750, 207, 89, 751, 1175, 1176, 752, 753, 754, 755, 756, 757, 758, 759, 760, 762, 1177, 761, 763, 1178, 764, 1179, 765, 129, 1180, 216, 187, 1181, 1182, 431, 766, 432, 1183, 767, 768, 1184, 130, 1185, 1186, 769, 1187, 19, 131, 433, 1188, 1189, 770, 771, 772, 8, 1190, 1191, 1192, 20, 434, 132, 1193, 773, 774, 1194, 435, 225, 226, 227, 2, 436, 437, 1195, 775, 1196, 1197, 776, 777, 90, 778, 230, 779, 782, 133, 783, 784, 785, 1198, 786, 787, 788, 439, 1199, 1200, 134, 1201, 1202, 1203, 1204, 789, 790, 1205, 791, 440, 1206, 1207, 1208, 234, 792, 793, 794, 346, 795, 233, 1209, 350, 796, 797, 1210, 441, 798, 799, 800, 353, 801, 9, 235, 802, 10, 11, 1211, 803, 804, 1212, 1213, 1214, 442, 1215, 460, 1216, 461, 1217, 1218, 469, 1219, 1220, 135, 1221, 136, 1222, 1223, 1224, 1225, 1226, 351, 236, 805, 1227, 354, 130, 472, 91, 355, 1228, 806, 807, 808, 809, 810, 811, 812, 367, 1229, 370, 813, 814, 1230, 131, 92, 815, 1231, 1232, 1233, 237, 238, 816, 817, 1234, 818, 819, 1235, 820, 821, 822, 1236, 1237, 823, 824, 826, 1238, 827, 828, 473, 10, 825, 11, 12, 1239, 1240, 829, 830, 831, 21, 22, 239, 832, 1241, 242, 1242, 93, 833, 1243, 834, 1244, 1245, 1246, 835, 1247, 836, 837, 1248, 838, 839, 840, 841, 842, 843, 474, 844, 1249, 137, 1250, 845, 13, 1251, 23, 846, 138, 1252, 1253, 1254, 1255, 1256, 475, 847, 14, 1257, 479, 139, 1258, 1259, 1260, 1261, 1262, 480, 851, 1263, 476, 1264, 481, 482, 1265, 1266, 483, 1267, 1268, 1269, 1270, 6, 14, 1271, 1272, 1273, 1274, 245, 1275, 848, 849, 850, 852, 1276, 853, 854, 368, 12, 246, 247, 1277, 855, 856, 13, 858, 372, 1278, 484, 485, 15, 1279, 17, 1280, 250, 1281, 1282, 486, 1283, 1284, 1285, 142, 143, 7, 8, 859, 860, 861, 857, 487, 862, 373, 1286, 1287, 488, 377, 1288, 1289, 380, 863, 14, 869, 251, 866, 1290, 94, 253, 254, 489, 490, 867, 870, 871, 1291, 872, 873, 868, 876, 877, 879, 882, 1292, 1293, 1294, 1295, 15, 883, 1296, 1297, 874, 875, 878, 1298, 1299, 374, 255, 256, 257, 1300, 884, 1301, 188, 1302, 1303, 24, 493, 1304, 1305, 1306, 1307, 494, 496, 880, 491, 1308, 1309, 885, 1310, 1311, 1312, 1313, 498, 499, 886, 500, 1314, 1315, 261, 190, 1316, 1317, 95, 887, 889, 1318, 0, 262, 890, 891, 501, 263, 1319, 892, 893, 894, 1320, 895, 1321, 1322, 896, 897, 898, 900, 901, 381, 1323, 1324, 903, 1325, 904, 1326, 502, 1327, 1328, 1329, 1330, 385, 387, 389, 1331, 96, 503, 504, 390, 902, 905, 906, 907, 908, 911, 909, 1332, 516, 36, 1333, 144, 145, 1334, 1335, 1336, 912, 920, 1337, 1338, 930, 1339, 913, 16, 915, 914, 924, 934, 1340, 916, 517, 1341, 1342, 918, 936, 937, 519, 1343, 1344, 522, 523, 919, 524, 1345, 1346, 146, 1347, 938, 525, 921, 526, 1348, 1349, 147, 1350, 527, 1351, 1352, 1353, 148, 922, 1354, 528, 925, 1355, 923, 393, 926, 529, 939, 941, 927, 929, 931, 530, 1356, 391, 392, 1357, 932, 397, 400, 933, 1358, 150, 151, 153, 1359, 1360, 945, 942, 946, 947, 948, 943, 949, 1361, 1362, 1363, 1364, 935, 1365, 944, 1366, 531, 1367, 1368, 156, 1369, 1370, 25, 1371, 157, 1372, 1373, 26, 192, 950, 1374, 2, 1, 1375, 951, 955, 953, 403, 408, 415, 417, 532, 533, 954, 418, 1376, 1377, 1378, 264, 265, 1379, 956, 957, 1380, 959, 1381, 960, 961, 962, 963, 964, 267, 1382, 1383, 27, 534, 1384, 1385, 28, 535, 1386, 1387, 275, 160, 966, 967, 968, 419, 1388, 420, 969, 283, 284, 285, 536, 537, 286, 287, 288, 971, 1389, 972, 1390, 973, 974, 538, 1391, 1392, 539, 545, 1393, 1394, 549, 976, 16, 977, 544, 551, 552, 553, 1395, 1396, 978, 979, 980, 289, 291, 1397, 557, 1398, 1399, 584, 1400, 290, 421, 1401, 1402, 1403, 981, 982, 1404, 1405, 983 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 8, 1, 21, 2, 22, 23, 24, 25, 26, 2, 2, 6, 27, 0, 28, 29, 30, 31, 23, 32, 7, 33, 34, 0, 35, 30, 36, 37, 38, 39, 40, 36, 6, 9, 41, 14, 42, 43, 44, 32, 45, 46, 47, 48, 49, 18, 50, 38, 51, 23, 1, 51, 52, 8, 53, 30, 54, 55, 34, 56, 41, 57, 58, 59, 60, 40, 61, 62, 0, 63, 64, 65, 2, 66, 3, 67, 68, 42, 47, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 38, 80, 81, 40, 56, 82, 47, 83, 84, 6, 85, 71, 86, 52, 87, 88, 89, 90, 42, 3, 91, 0, 92, 93, 2, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 46, 58, 107, 108, 109, 46, 69, 110, 111, 70, 112, 72, 4, 113, 2, 81, 114, 115, 9, 116, 117, 2, 118, 68, 72, 73, 119, 120, 121, 122, 123, 3, 124, 125, 126, 127, 128, 129, 130, 2, 131, 6, 74, 4, 132, 133, 75, 90, 134, 135, 136, 100, 137, 106, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 91, 152, 2, 107, 83, 153, 154, 155, 1, 156, 3, 157, 158, 0, 159, 160, 161, 162, 163, 5, 4, 164, 165, 0, 166 };

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
            final int compressedBytes = 3314;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXUtvHbcV5jBjlRZahBb8ELoaGY57C3jhdJNWG4+cpnEKFB" +
                "VkC2hQGAW6aL3QqsjK0IJSjULqSgsvvLzwwkB2+QlC4EWQlRcN" +
                "GvjXdB73MQ+S3yHPzJU6iWVL55I8JA/P4zs8o/cf/m3/5tf3rj" +
                "65cnTvm5+8fnr+4PBX+YsvfrhmHn+/fXJHvP/j3f2bp8++uH7l" +
                "6KCg75f0nRd/+OHa2eP3Nf3O/q3TyZ9elvRPX++//eRwr2p/9v" +
                "jHbTNA+w/vAv5448viPy1EIspnrfizuXu/+GqEyKVUYoD5j7w+" +
                "oH31ZMLzJN0f6OY3Y/N//rM7v71yNFHXhbj3zc7xjfO1yZ4w6r" +
                "OCiVysZP0w/ebp5FHV/wdF/58cbrTls6Kr66V8Nunm8Xc0+UX8" +
                "++nr6+LGhioE9cpRLmSWi4+/0vmLz84Ss3eyXe3f35/eOj3483" +
                "8++ufB68/f/OXtg8O/fvqvp/8t2z8/Gf98cde/lI/0aLL5spSP" +
                "5PjG27XJxpDy8fN1Wazfo6RYv+L8F+v3m6/u5y92yvX797YSl/" +
                "58E+XXIp97lXwi/TC+fgfnZ2z7wGxfy49qyc9OS36AflYiFeXX" +
                "2VN8o+Xs37kY3/7I5mbnja/FI4VYiX710dnyObL+HXv+K1nf5m" +
                "O668tdn0u+vqPbP0AH47f1Qzr3z/Km/Pv8k++X+vXApv/d+ndv" +
                "IPs2Mp2wf17/je9fp3b/OSHpp43Cc9NLhVsEI0908bWMP2b6ly" +
                "ffs/1/5LL/Y9t3GF+xzx/Jf6zji6SKLzr+4y9m/vHRwXThH9fz" +
                "e071H05r/qc1/7eHtS/j+o8yl6l1f/TORrk/bHp//7Pm/qP2lv" +
                "i0G7+mvvgV0VH8iejf1vQ6Ptmp4pMFXQ5A58bH3P5D2k/r9g9D" +
                "+MP4C4q/gf5o2Ldp3V424wsYP4P+UfsIumjH7+PGx/P2bv24mv" +
                "Fd7Zv0aU2XTbotfm/FX2D/xm5/EfR83P6zFj7i8h/Plv6FD3+o" +
                "7NPXB6313anlY2n/PPRCClKEX9rP/x4Jn4PxPdQ/wf1nzf4RPs" +
                "Clt8/nm+p8Ppydz+e989unc/sP5V806MI+PzHo+LagOKfjD8j/" +
                "FCoX+DHwE9rxc4TvwEf5ydz49aLpNvlo4SPDjy/i+XPGd874vk" +
                "m36j+m/kX4gTs+rv92+2d7JP/O0r9ox9/++AfF37OO5BKDTFtH" +
                "DbUfm96Zv+yqgoj+Rcj4XHyh2d4mnw3/WCz958o+vtoWhPhSyJ" +
                "/64gtEJ+SPWf2v/945v8r/x/gFz/406dOa3vKvg01Rj55jIwLb" +
                "ex7Js19wfBVhkxOLutNKNpup2T+k8stv8UHZYsR0TH3lP2TLT6" +
                "Qd5lVuRFqqr7q9MmKz/HtStdFwWhV/uuRP1vy9KvnTxafOa/6i" +
                "14+4lQrur3d9IP9s+QDyuaAru6sG5Avpt+X50NVs4/jvyo+efV" +
                "Bh/rrru9ldX5d8mtnmofn5FUDL4FXf7M4OV26XJ+75Ru2NdX/T" +
                "6P7J4ytf+3Q8/ZeDM430G45/Es/ex59vQzufcH/m+l1Y9fusf+" +
                "/+p45xU8r6K1c0aMaRb/wE6Tc2fwLqV1lfIejah4xGvzD7aID+" +
                "CLU/PjpD/2P/G+n3ADTDsv5Qv0D7JX37Z/fPPPrFdM4fX78j+R" +
                "3af9EtiE252jv0f6j/DuWTZF+Hiy8C5StYvrv9o/wnkF98PyFJ" +
                "OflTbv4R7f+W/lEWOie9rddk8uvie2MSkelMiWkmdgQRH134v7" +
                "T9zwPw06j9WZ5/bv6QJugq+nyZLigcfP9zmPu9AJ/YdOETq+LP" +
                "lZ+s1pek0x3ySZZvx/08Zn6yr7+eOOJLh6gB/lF+EPHnxI91v/" +
                "8WfkjsPyK/2L8f6WsP9/ehV39h/Dv3h3dofy5Z/q+b34toL8La" +
                "I/+FiELF+jdt9KIXPwbzH6g/vm3iwx37Pr+f5KNj/DhE/sLvJ4" +
                "fJd0R+loYKdKROk+n8/GPg/vecXj9+AfcH+hd+/wzqT7dy07T2" +
                "uXt8Ev6Ue8954P1gSn5OBOb/hC8/aclf2mCmOV1a2l8ofYD8rX" +
                "f9I/YvLL6FGiOPsCwB+IIaSv82z48i98/If77q3I+k0kUnP4zy" +
                "s17/19Ie0YPyxyj/a6GLJp2f3x7IPharqK3xjfDnH6Fn5ZcvdP" +
                "+lxM/KHraKEXv42drifuMH4iNX/MHzr6j4HtH+WPyLHPgf1PZE" +
                "/DU0/yeC7KuHLh0Lec6b/6RuWMiHSU3Bt5FalfBTUkzjpiU/ZA" +
                "I3HLY/9/On8oZxs4VIaP+Y6yPetdvPsAx62ia3iJQOOD/B+cX7" +
                "Ifgrd/wFflkMbsMvt/R3bbqo6aJLV3a6EI8qD6TafVVO1Mztf7" +
                "Z01OVCvpb6lah/Uf/l/FPf/P3zQ/zB+QP+kf0ir39n/wR9/6r1" +
                "uV3MMYZ/NP95/7LXvyHxN98/1/5y5XPZ3j//cfY/Zv6if35SfH" +
                "7w+VoH/Mmo8430C/KfoH0+QfqB6T8oBJHtAv27G5Y/7+YHQ/OT" +
                "JP0fYF/h/Rxkn6n2O46O5KP8o+ft5/erBL0+eTj/LzZ+DezfAP" +
                "k2gfLP9K9xfgXWX4D7AzH4g1jd/Nn9++kY/xmZf3j/LB9//bV3" +
                "/0H8vjvDyKS0xwdjyweMr8v/C/7W2/KfmiQZgj/2+QDzg/EpCZ" +
                "VIyfJnrPylZP7t9jsF8WMK1j/F9hXhn/b5wfsnZP4i5UON7H+B" +
                "jUL1O1z7TL/fEhefx+FPXP29Qv/Eej8vxD538ntk+ZvO8VkWfo" +
                "vzh/VCZ0v9oGu2pn7+ZD2+qduostHZMj+RD2cfmf5D+P1pnv/D" +
                "xbfMyPwRz59l2ZKwjNtq/EeK/TYimf7y+Fnxoy/NhkleTvVU/W" +
                "6SpWZtiPi0iYWO4R+O7b9TBNXy+TxdUXx4Xi5rmlXGpclPFkiX" +
                "cfRF/GbPX+Hxc9HSrw56AH6hw9YHre+7dvuFf0idP4xfvfYL+g" +
                "/F+S1GmG6J4vyKq1nR25kp3MN84hs/OL5H9R0Muhihf+2mk/CR" +
                "1flPtPuT4+m3+PoUqn/XfDIGfuOKz1aF39jHx/49wH9PxHHm82" +
                "+FuFaR0nKLivgkKzna0YnRJ5s0/iv91b4/11o/QHf57OP6V/P+" +
                "Vau8MrO89Ibqn0gn/sLyf7nng28/YuiSHv9Y908OpT8D4ms5Dr" +
                "5ok58B8fWR8TdC/MqyX3z/NDC+ww8hfzacfS/W75iT3x8wPyGZ" +
                "9GHw6541Z/pHOL8D8J9gfCqMzvbfgBJg5s/Y9Qlgfqj+jb1+FP" +
                "xtxPuZcf43vX1E/VjriML25PWf34/uDveQuX93n4L3sz1l1Z9d" +
                "8P7B+iZsP9D7IYF8B66P4Zw/2/y03/+i5I+akV8XP4e9kPST5/" +
                "2VlvfvzO6HlA+qH6PYd1//XP2H+GPrx0Hxjzdx9j1cPvLh7Atj" +
                "/krw9Rtfv0bkd3r1OZz6K3//zPczEd5fwqrPEsz6K1jfBfafUB" +
                "/2cY3/9BbrpP42l7Ml9+E7Pvqo+A26n0eQfyDf4/qf3Powrn+K" +
                "qm8Qf/T6W099cOqmb+lX1f2UTNT3U7LF/ZSseT9FxNYfs+9vkP" +
                "E9f34p+v1c3PHR4aO9X8/ZP6xPUURBdOGrIrB9oH/Jvd/A/f2L" +
                "aP3Z+BDZv1MA3ybSI88Hlr9I+WW+X3X08xX6fr6h57/A4pz5+a" +
                "H1T3f/vfUlWD54/kccPh0iv9z274jnB53/dSv+uaVPZvY3repv" +
                "sqp+p1z80v7i+C+gPjEq/kf99+qH+vVVLHzYVX/lqU9qCT+u3/" +
                "L7R/7zQbq/aJj5h1B8QAfZn45wJA7/0UtvPC66qz2pvrOqj+uu" +
                "f3t+mO6or7MeDu/51rTznV0yurrg9v+PdPr5xvWdTvkk+hcu40" +
                "6o3ybNL27+3Ppc7vkezv7Y7TM//tWW+KqJ3wA6RX+nlPhMOOIb" +
                "iK8R8SPK+8ds/l1g/W1//46VT367/Vvix1unz76s+Pu84O/B4T" +
                "868afkxHd4/oYYP4fgA2j9bO93iovvL93vF+8K0sj4wwD5cd79" +
                "sUHuN7DimwHWN1t21fn9Mtz2F34/GP5+J3f+UYqB6ofTi8Pn3P" +
                "evpov4pV8/q8v6WUcEYtVfbvqi/w6+M6/PdY9Po9PG7+vvVAwz" +
                "PzYd5++48xee+/9E/yO+/oDtv8XQBd2+EvDD0kRk+fL8lJPNqv" +
                "pEI3r1z9H4lAL+rwD+rXD4t1T/MZau/WYf1Z9D/N/vH+LfL8CU" +
                "f3L9vVc+U9T6suZ3CPlBJv7GzT+t0v99E0wnnH//+5/p+YXA+m" +
                "8nfklrLzNrfJ4A/ydxxj8O/LKDD1ytxtdOfCCx5T9Sgi7rvR+7" +
                "1bGl/nFK3R9H/eeU1j/GH3j9O/NfUw8+01p/0L921H+GzR/hx4" +
                "Jw0yMSPwL8I/nKRdBjQtsj/zG3CLyW9PH9v5+P0n5U+tj+LdP/" +
                "HeD+wrj1M4j+P/EVgB0=");
            
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
            final int compressedBytes = 2876;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2PXDUQ9zPvjpcTCOdEQkRlUIS2SAE0gTS8CwRCgYguRI" +
                "ICiZIiFaJCKXynKy5UKShSrlIg0eVPOCSKU6o0SCh/De9jb3ff" +
                "Ptu/scfeDSFPUe5uZ/01Ho9nfuPxE5XwPs++fu/Oxfs/fvnm1s" +
                "Hdx68+unPyyb0P946++vv8g9vPrh1fFqISh6KUQou+osqIS+3P" +
                "iZg2/ytPzcbfMJUO+r9MrywEwvi4/CH378WmV/ZpReXHj/p/8i" +
                "+SPn+knYW5y2+6/5hu7PpL9+1sfn67gevF+pkNXr8Q8vns68uN" +
                "/pxUjf688viVRn9evbdbH33593mzf3rNXJ6Pfzj/KdvfsP63yv" +
                "ca6PWC/2/dn3z7W9v/Tx/d+evqvf2O/w9u/9PyH9Et+l0B/R9G" +
                "x+vfvz4S6T89Kq9p+xP6Chz/Sdtw6Wofzo94Oiu/be8/WT/uAP" +
                "vOzl/cv7Z3KdY3si8y0cn2CSovqfIR2n/i/hZdP3F/4vKfax9s" +
                "Wj5c64PrP9TzFRS3P/nLi6UVGrc+Qf2w/2B/eqMZ3x9Xzn3T2g" +
                "+vPvq+HV9vP9x+st79G7qtMkp+mftXAv/U798DuqyuC6VEIZSU" +
                "3Rb08FajGJRp9zXZFJGV7Ont+Fr6pSGd0L5fv1n3FxU+PfHzE4" +
                "aPGIqQKfBFLh2JBaP+52J8/yN6uPwpq6Pgtl+RfRGHD0rnH6ui" +
                "eb77pCxam6Num63FniqMOr6UAD9Mvj/knn/u+lr5eFeILbWYhk" +
                "ZZf9Mo9fZbdT8rXP+2ovR1qbzpP6tS6Wd2/bzyJP/I234N8RV/" +
                "/czyeP2z8DW4/iIMMa/eMevAd1QE3b6A6PJjr5wvf+T+O/jL7P" +
                "8bLvzw9qkbX1AZ5kfG7o/gKUoavuB6uPqBMH4rf00S/py8fvmz" +
                "rYNmfoW48njv8MLJ9mRfmOqGagdG0F/Z9SPwP7F/yuw/9n8hf8" +
                "uDyaWOv0XH390Q/rLxY4T/ibz8gfwb1V8Gtf/2jrywW90sxNZB" +
                "49829unHP7+/d7T3oDD7v16rMB3JR7M/fX/xj7uD/u/1/H1m73" +
                "/i8mz57xi6rMWUDIJPQuTz907+rs/k7xeK/kD9A+sLtk+2f2cM" +
                "cLotcfvTbP1X7vVv25I880PCPxnlw+uXa+1fzP6a0T5B82uTz3" +
                "j/4Pdg+yG7f8LUf1g/nuEPI2V07Fst2FM3NPwF0kmGuFyEM0u1" +
                "stsVqy3NrDsz+FRFxgdA/7D/B+c3k/+YE19ltL8WfDWgfxD/C5" +
                "Tf8fmBSPyBiD+CiUqHj4kA/R4i37ntd65+iqGH4DOZ/UMYf4L2" +
                "JRMf8Ne/syMa/6KpZOugEcjGv/jgZ1Uf3Wj9i+NrNlkLti/T4L" +
                "N6JN86GT5Td5uwHhfZpviXqH6TFz8Kwn/1+LswPuuPv8L4bPb1" +
                "H4s/UeeHO3+bLg/o+swI1aY07bxJUSlddVbdxWV8IfJ8Unb5Z9" +
                "ZfnfDGh/gz6QsWYtLz10g14C+On3rrj8DHw9YX3F+eJtHvLvo7" +
                "6h/ZrN/y3Ua5FB81fxvTiGrLwKkWe5TyD7vyWmzLtrwmlWf4/y" +
                "/p66UD+STJT0nxX6Sj/tNh/aKvX8zq3zQd2Y+wvLjZ8ad1Xdoq" +
                "io4vnf+m5/pjQBcDOq6f4N+U8fEhOv9krvnp+u+qX4hTwF8//3" +
                "P3fzy/Ye0nWZ/e/RepDmS/K+R/Ec8nxeYPAP+Igk944mNs+7zy" +
                "D4Se/+F6boXRbfYZCx9H7VPtF0QMtU90J/98/ub2L0P8zwh8Y6" +
                "Z/8vmPJ6D/iH6Gfxd9Y3oV4CT2T8Xi8zH2e8D5iyzzT6dzz5/k" +
                "zg+IO3+g1kbn4rdcfLdu44t/6qpZwld0YS6cbFe7zcc3um8kwY" +
                "ep8YeM5/s555sS04sI/6my4Qvl6gilK37T/itH+YmlKYoBfWeJ" +
                "rhZ0Gr5aAv466S3+/9b9H7/r5PeLRn4/ufeTJb7nqT/z+sX4m3" +
                "9/RPhSmvz6Eqy/3HS//ecrj/N/uvzXOR/MRsa3Sf4Nxz8uj+hD" +
                "cZqtf52p/8H1U/1P1dvfwf6nNT4W4L9VaH649dvw6RD/tAbzU0" +
                "fMb4D+Gcdnw/KPKPZD6cP3Zv6Z6vGJ3j8Tc/8M2w+ofXX2w35+" +
                "D8Z/AX+i/CtB7x/Zv+v3/3Nd+4qQnzmd4XeE9Vmu2j8D+4R3/h" +
                "LjQ7zzJS66XCq/rO9m+q/qyg+m2ys/YtQ+Tf5heSJ9eD+EAvgV" +
                "ff4I8xM5f9OBfTC+X2OaZv7h+d1U47PfzxF3PnPxPOnza2667u" +
                "d5ssi/uWujY3zLqj9lAL4U9phAOje/LQU+lhVfeL7xodCJLEhC" +
                "4K4flS8ylw/G/6jyrUTkQtksvrV5fDsBfuSlm+dwfC/pa6PrVg" +
                "KK6bnDH5pfvjNKFEdTNa0+n4jSJDmfGItvvpyf9e+POmJ+kXxk" +
                "1s8VdSNy6U///XZ8uoe3a1lfJ/7798j1u/oPxo/P9w37N8efdE" +
                "b5C+Gv2TCd2T/dcWBaiB9adKRlqjFKV/WEKn8J+v9cn4/edP1c" +
                "eqx+HOCfZTb9nfv+4+z7D9W/Ypw/9awPfL6Kor8t46nLM/6z7t" +
                "fD/Kkt+O4K/8tQ/yvYLS9F0KME+X5/pnxVA5bp5a8b0kBz32/O" +
                "pAefDyTjX9OFfkhyPtN5PpKpfxLgq8KHz9fc+i3rY/l8A/f+FM" +
                "E8XwfkL/P6ZM8/5X5Q0vpyEK31K3r9WfZXEaDfefNDuB+NGL9G" +
                "87fj5q9n/0b3R7HPD2a/f8i/vkn6ofb1z3F/2oNZ/Ab0D95fxL" +
                "wfCd7Pwbxfi3A/D/N+osD4n5secP9PQP9gfnwC+zpj/3B8089f" +
                "fD4W3v9CsA/14tdyZfPH9mEpFvezyNX7WeLiny/U/ceJ+BOXf0" +
                "i4XwbdD4Tkh/d+tcD7T6D/KGL9xxB8OEf8zeXV8eyfcsiyeolq" +
                "KCYkN/9HCvmaOutD79+937ddd/5d7vuTcudvYPwyQf2c8kH2u8" +
                "aybIj2mdm35/+bMHwrN/6A6Cg/Ds9PXvnk06n+ret+iqcIH4k7" +
                "fzjX/0+t/afiLzC/l3q+SQH9m4ke3/+ef0v51XI5P9p1fjUKfy" +
                "LgF3Kwf8eMb8cxvgT5256BJsl/LVftF7f9mOb8VgB+ycZnufrH" +
                "n1/N5n9jn1Ze+WTmX4fWP8avJQc/S2efOPU/83wpKf7yX/bv/O" +
                "KS+/2fMP8bGXJUfD2uPNe+59q/2P7i2mfU/tv50+UnHwzyk5uB" +
                "qnl+sp2+yF+mv//cgU9j++yQl19SO+JHqztkXH4tzu/1l+fm59" +
                "Lv7831fpwc/kFQ/OqQdj+M279ot0BdL8q3X9dd/pHB+YMEfMxb" +
                "P1LEsH+j8spvAbrkH9j/zvyzW0P5DLTv6Pg99YBFlPzF52eC/E" +
                "/2+w1W4ivlUnylTrG/MfNz4vRPYPyDdL+E3T4g8D9r/iIpv4yR" +
                "v7nKvyLUfna278OvQ/Blmn2C8ws98W2afnH5P0nwBY8A+/Mn5/" +
                "RtQJexdF5+Kff9sfj+N8f56ildvljyad9/AvxPXgBtHfxh2XeZ" +
                "7cMs8hewvwz211JYNz3v+RL2/hsQH4nBZ6j+GRE/CfXfA/CHyk" +
                "Ig2Rclg7+dfa8XX/WcP6ii4n+x579o+xvl/jJHfOyUFh+zxedE" +
                "QPtM/qwtfxYxItf7aUF+EY5Pecuj/uH3R+bFv1B+RxL/ghUfTy" +
                "W/aH+2xxdldb2Pfygpuyl+OI5/aN+uh+Inx379Exd/CYkPQfxH" +
                "zlgio9Y3fr+StX76+UGe/ol7/1ZI/gfT/2baJ3nej06Pn6L44+" +
                "bPv+R9f3vA+zFkJP7KvJ/F3zwX3+bHJ3nxIeyf5sXnUtiPfv6B" +
                "929T5NfDf9L5/xrZ9+6Hez4f4bPiX9mmZcI=");
            
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
            final int compressedBytes = 1885;
            final int uncompressedBytes = 25185;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXL9vHUUQnjvOZomE2FgJWFRnZCEXLhBNwE3OQIRSICwHS/" +
                "AfpKBCVMjFOnKRULlIQWmloPaf4CJFSjeRIv813N078Lt7t/vN" +
                "zuz5PfGaKJns752Zb76ZPTIVhX+2/1c3EBvits9l7cPzu/xw+7" +
                "viZMfcI9q9yJ7dv1zf2SBnHtmmodtO0N73c8FtQtsYXp/ly8H+" +
                "FUTZsCc7m70Lt3Ss/R8f3y62R/vja28o6pdR7P3qX8+R88m7Ke" +
                "XC83FU5FR2EzGONps/d+p/af8fknvGN8z5bxCtzWlgTvanpifX" +
                "zDv3Hrul8PVYHXnRP/Jqbvcd53yuf/z86OMXTx/fWzv59eL9V0" +
                "eXD4+/3D/94e3dsyfXe8+x/Rjff8s2G9cfbdfj1/Zn7WT34r16" +
                "/AfHG9Xp47d33eG7vTH7E2s/sfzZ+P07H6wjF7Xfsn/l9SEVJa" +
                "3n2VdEpXMZlbY0VJa0z7jAXv0Izc9GnM9VWC6yHzbGwLSXODC+" +
                "av1b9k27/5/ROjX7v0Wz/Sc65+2/Vv7f/O9MMv8t+64vd4P1gf" +
                "Pj708OHHves5/Gox8ftOu3N+sX6Z/1j08L44OLjPDHQdj+cO5/" +
                "EWEfvUKZfjf2/ZMXT3952dj3718dvX54/FvPvuv9c7c+IX5Tt/" +
                "fsR1UMIFE266xkOVi/2CXGr0r8h/03uh/b9f3Y+bm9H9/W9+PB" +
                "8WHrf8+edP6XFT9svuzih9c4frCSbcdyJsB2nna2H5+ZaP3lyT" +
                "M2kneD8dFCtPNL079ofe0K89H7j+7np3fy+xvGZLR2QrReVvT1" +
                "71/sn+6fZe7wzz3Dnn8B7hKWG6WcliU/6O+/i8V/lef82P4Z+B" +
                "euf0fzIzC/cf1v7Nvav/zIfsuPHI7btxyDwqrvEFrZIv9kefg/" +
                "5H9j8APkN8D4ffuYAfyTBf3P361+f9Pp9x/t/obPZ6x9z3+N4m" +
                "/rnb88/vEBZORfwPmp8RnDfwXlthE08esA358HfYnh4gu8PuX+" +
                "I/0A54PtE9qfoFyLv8bPbz6+6/gHO4vvZvwDjfAPuZCCrjj2N4" +
                "Bf0fp1+JXfP3nslwS/I/7Y8q9v6vhCGr9Y2fgyftEmXL+Ev+/f" +
                "Tw8/+SYFPwn3B/OXSn7watQ+URcoY/t72W9Pg/Z4/8P8D5YL/B" +
                "/6Tcjfu6FyKu0Dmz+Tjp9Yv91U+z9+v/n5HxS/ILUW8qOwfyCf" +
                "mJ/T+lcWvik0+ADZFx0+gfyWaWdv5qCYzcfDPZ/+htsL6wv48T" +
                "GOf0P1AbB+oO2ovOmzGNxibf5u1mPeDVGvoWDw5xH2Rzk+rj+o" +
                "oujLEUMaXr+oviSCnx7QGenxG3t8Lv61q+Q/Y+pXaDS/LyHS+f" +
                "xqAv1k6l+MPILf5uDDMD5W8o9XwP+Hx/fld81NflfPDwXHl+Wf" +
                "TaL8M+yfXZ9C/vxEMRb/O2Z7q41vBPoTmd/h5Z+VCjqZfw32z8" +
                "9/yviZWP4o2v7E859x9QHe+8e1fwMNcbH8eNW1B/bNzuqjFvXb" +
                "Mu83gfgtmP8oxPyRd//c7eQ3BvqdMePXeP8orN/S8utiOeCH8l" +
                "l7bXzKXr8YP02bv4D3c+L9Z6wvmD/R1r+z+BMFf4Die5xf5vPX" +
                "ZkSA+eUqGJ+z/av14bvnXf1rMcs/0Sz/RG3+yTLjC/n8ZPmllP" +
                "GzhN/F/LxJxs+D/hcRr43G1wgfBvj53ORkbdNtLW+muHlQT8y6" +
                "Zl256fEnYf7BK0fzD7eH/LLyfQ+Wa/2Ltj3Ir0T5n/T1h3r+Fv" +
                "CrUB5BaU1iXxLxa9L3S1p+Uqk/DP1U1Z/g/NK09uf24m/EL0rp" +
                "g/nzK5nnm0fEf4C/Q/wcwLeIn2K9ryiAfgbk2vHl/OI5z37j9x" +
                "9K/s4Kzy+WH5XmH3T8v4DfslH+OVJ/s0h+E/I7YP9x/Wyy85XV" +
                "Dy6Zv8DxczVpfTCDP0C+KKI+chX5ieXu79T16Qg/y/hxSmY/Y+" +
                "yjUeOTVPg7ZX4Dxe/K+B/WF1bB+43id3Z8L99/3fcLgP4i/K/O" +
                "r2N8k6J9EcQ3hTz+TvA+n8M+hOp/dHJ1fMTkd6x0fZX+/e2t6I" +
                "8QP0fF/yX4v+WCLqu/PwHksH8tfgH2CcafSvwq7583f3X8x+lf" +
                "EX9P875hTtra19LUU9gtM1fjO1O7HHrU9jBuX+O/H6B6H6aLj3" +
                "B+fLXx9/T5ffS+7P++fuA/lfnLaH7Fcfx3v34o/H4I4QOA3xPb" +
                "1zT1rTH2Gcmnzn+g+zUx/kxdnz04P3Z+wuffJsZHUJ6i/lj1/Q" +
                "6UP9D6T4Tfpq3/TVQ/UQTwO4d/Lfz4SocPlu6/lPzzqs8P8vMp" +
                "+DsWvhk3Bfz3u8L6G219yNT8BeQX2PzENPUvy35fIqr/i3ofwt" +
                "KvvBffxr9f8bVPl59OGT9G5Y9WO3838fiy77vF4C9t/gDiUy2/" +
                "yrNvUfx9Qvuhrt8jnJ9XyNX8n/b7zrzzCb+/WGV+6TbGD+wfrK" +
                "/Wfp9jYjmKL3H8ydN/r3/lyBX6t/z4Rdc/wz4G7bP2fPXnj+KP" +
                "BPsbGv8fG8Xw7A==");
            
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
            final int compressedBytes = 4990;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWw2UVdV1fvIjyAJBSpoourBpJEuRakyDxtLk8N48OsEugz" +
                "VNbYIMoq5mRARSiyyUZO49975h3hu6MFki02VSoE5W0p9ozL8s" +
                "rVnRaLs6Gpu2ygxqUAPYiZJCjChh0X3OPvvuvc+7g3DXOfvsv2" +
                "9/58x9755776M+Wh+136iPuqN5Q3ZVsjr5JmpgH+qZXh/dfEel" +
                "Qpa+nvooa8kD9VEzHvI666PpxfXR/DRnTbtab0LfgvaHFG132e" +
                "6A+fV0fKXSuIxx8tz1PUvTxc6Sjqv4f1QD46TutEpl0WjPs9qm" +
                "/YA4h3K5dzJ9g6Mldva4709UKr2PVyr2IfTkFY0a84AZDdqvEU" +
                "6lYjpNp33L9SCPQ3skOw01GA8lR01ndrfdT5amBf0s0pLvQj/B" +
                "dDaWoZ5Dpv2R6ez/LETNNJ3A6of2iEf6fvalgPlo84xssl1FGB" +
                "C5Ncij9pjdvXlGVrEnsmnst49D+3fWQTsK63kwOSJt2UThfwzn" +
                "BdbxoP3AjybYf/W+J+0tIvIdaA/juPdRH3fCdG7+qP1JtgA8h2" +
                "FOlaLCVFnPZ79tfwz9E/apbJzpTK90MzapSbFHaR8hzenpVpPm" +
                "TzsLWlunc3Q4xpm0/zNohfVMYT29vTHi0X6I0fb7feej3T7q/q" +
                "odUxgB1jNUg/XfjdL9Iz+sZwrrKWo6X9/s5Ncx9yLjMdI9hx8g" +
                "f1hP53vS1VYzfhjHmy/xbE4A9i9gPT8W5lSRVXUdwPwxNFhPWi" +
                "Foq8yq7CXXg3wN2n9l/4sajH9pZ5tVjaPZm4Xlp2YV5njtZ2bV" +
                "Y6Bl/xP0UWj/B+0Zs6p6I8iR7Eio8HRza4h5rlLJ9jY+TBiAf3" +
                "PwvJj9PHs1ezl7PfvvbA/7szeg/Zp10J7PftHXkbylbMNi/EK2" +
                "H0euOjKAqge971eOGR3Olz3LY8qA6JrMZr88/F99VXYoO0yR0H" +
                "aZXdij9DG7SE9+Y3Zt/i3aOYL8ZLEH2UPe7BaJVqk070Z7PtXZ" +
                "GlsZoXcGIWF0cX7ukqzimn1XJ7+NubdnMAdmknXHM5YIFJt9Mq" +
                "6s14oZ55PzKWL208w07J0MM5mGhx2yN4EcZIsbaU1aCc3J6kpt" +
                "M4MYm0/116NvCJxBrt7OIa5JkX1foTzNhZE4l3sniVkb9qBag8" +
                "H22WoWZAnrSdH9pj+/2PUgL4J2dv57qMH4923N9PdNdFEYySM6" +
                "nJbPZk8+F+3VFV67MFR4b+vLAfNc+Iuev+hyRuidR0j5PETJz/" +
                "FMRYX2mn3Pph9zY1sNuHPYn79f8w2o53nfBdUVjOc5vo/HnJHd" +
                "GFdmP1v8+fmB/IMUCW2NWYM9Sl99Den2GrOm7yZp4WjWZL5dir" +
                "J3ImJRhdYDFOM/7+cRBkQ+JD3F+bGmvCZFVp9Na9qm/XEuM2mc" +
                "W47Na+HXMymbrT4Qk/hiS/46mZD9hPdo7l9yezI9eTv5Hbi+L0" +
                "9mNWF3kvwu7DZne996WOUvgnYBjO9IPmg3gX4wuQi0i6H9QUC4" +
                "tNcQXvKR5Ar7hdaDcrU6nvGexcmfJJ29ryWfSK5Ce7oi2cD7z2" +
                "RZcn2+EOJW+OiVyV8ln0tOS/wOtXlzek2ImpbMwIzkPdDOsT0Q" +
                "+zdu/5nMgXV5wudeKGsnl4X4jwaGCxOTVNMdkmHW4PXwMVcTr3" +
                "xnyP6sXLXkRmjuzOs23dij9GvdTbrNTXfzKWnhaNZ0Prb6c4hF" +
                "FTjGz2mIMAB/hvQU52d3eU2KbH4lvUHbtD/OZSaytsROd8g1yH" +
                "aXzVYfiEl8Q8tNjj1K78tJt70mb81DO0eQX1pkc0fvv0g0Rg2f" +
                "9/MZoXmh9JCULMpqVn+arou5t2cwB2bSmBPPGMewniIj+1FcWa" +
                "8VM2bernXM7ZiLPUp/9swl3f5Tx9zWfLRzBPmlRTZ3bP5Picao" +
                "4fzMGaH5EeWZW3wuowq6ZvWZ9I6Ye3sGc2AmrraeMY7THTIjey" +
                "qurNeKGTNv10yX6cIepV/rLtLtt0xXa4W0cDRrOh9b37WIRRU4" +
                "xp+fDxKG6WpulJ7i791VXpMiW19NN2mb9se5zMQ+WI7Na+HXc6" +
                "hstvpATOIb2nazHXuU3red9Poes735Ito5gvzSIps7mjMlGqOG" +
                "b/U9jND8tvSQlCzKarb+If1CzL09gzkwE1dbz1giUGx2LK6s14" +
                "oZM2/fVpvV9i3XgzxuVgPSaaiBPlSdbla3vmj3k8XFZ2dJzbXG" +
                "Mq37qJlOwtlwBD1YCUfZ5EVXEIZZvflm8thj3ovPQ1bHVfhwz0" +
                "Naj6T90pZNlBkUn42n3GwCSthpTRdI7zA2+QNHWAt7WFbOpsp6" +
                "PvvtInqcWZ1e6cZuv4Qr3PNvdP13+6XG2W6/VJ2WzGp93e2XoM" +
                "3Or4W+Be18aBf4XQLsl/I/j/dLSX9yqZdbeL8E+94/479m4/H8" +
                "atov1V/i/RKweFrsUWC/lHThfin/C7lfSpqtn6VfzT/lo8J+CU" +
                "bvgeb3Sz3/Ac3vl/IuuV/Kr/Fzgv1S/hmwwn4p/yTtl2C8LF9O" +
                "tfNLfP/pgLwU90v5Ut9fl1/fM4T7pYIr7ZcGzACcnwNeHjcD/v" +
                "wcCPqQ/bYZSHfB+TmAka6H83OAD2eB89NLau7IZqIG56ePSgcp" +
                "3p2fjYsYId1JHnd+QqY/PwlHowZeR81A9en0fvL6ehM1J7TD+e" +
                "krwmgCzaBxIWfB+VmM4fpe5ALfr4H3sKwM5+eAZuLOz7BbHQf5" +
                "V3pv3dSxR+mr10m33zH11j9KC0ezpvNlIzRGDVfDjDBMvfmw9B" +
                "TfR/XymhyZ7tY27Y9zmYmsHbNn3vkflc1WH4hJfEPbaXZij9L7" +
                "dpJe30cWjmS/tMiGdonGqOF6tI8Rmrulh6RkUV4zfSLm3p7BHJ" +
                "hJ40/jGUsEisWZS1y9VsyYefsVc28cPg9/j9uSyeSjNwG1M2tn" +
                "0lP95H385iA5P3k/vSegDG5F1Iclmv5XO5PfHjR/he87ONN/H3" +
                "06uc5zWyvfVvB7BljP5yBqStJNluQsYPnegu95+L5Do2LtEL+A" +
                "2bZzzKvJEvHtuLTgdW2QqwLKcr0CZr1Zjz1Kv9brSbffIwtHsl" +
                "9aZEO7RGPUcD3axwjNY9JDUrIor5nuibm3ZzAHZmK/G89YIlBs" +
                "fmtcWa8VM2bevlVNFXuU3lfFww51/J22YESs2YNa50a5jBHOkb" +
                "8nDFNt3Sg9Bb9qeU2OTA9pm/bHuczEzakMm9fCr+f6stnqAzGJ" +
                "b2j3mHuwR+l995DeMUAWjmS/tMiGdonGqFi993RGaN0kPSQli/" +
                "KadmLMvT2DOTATnFM7ts7I++PKeq2YMfP2rdf0Yo/S+3rxqE2q" +
                "TSILR7JfWmRDu0Rj1HB+TmKE1nrpIclVxqppT4+5t2cwB2aCc2" +
                "rH1hn5jriyXitmzLx9m2VmYY/S+2aR3rGdLBwp/SdrRaVZEjWc" +
                "nxlhmFmtl6RH57XX5Eh7Rsxd+/WM2IJzasfWGY1pZbMtq0PVsM" +
                "nfAsjrO+r2bL6qt/+2QF/X4+u73i3oGoteZ5z+T8nru84b+/cM" +
                "wO0czT32j/V7hvxz5b9n0DuRvLt9tjETzTZc3yeZSdg7GdZ6Eu" +
                "n1F9HKFo6WedKD8bHNIXGNRW9wdv917GnnENdkDPsBbdN+mcs9" +
                "z6kdm9n78/Oh9tlqFppxEb3MLMMepfctI91eEFs4mjWdLxvlMk" +
                "ZYz18ShlnW/3npKdZzWXlNjrRztU3741xmkr9ejo2yN8fY/I2y" +
                "2eoDMYlvaOvMOuxRet860u1IbOFo1nS+bJTLGOF+cxthmHX966" +
                "WnWM915TU50o7XNu2Pc5mJrB2zZ971y8tmqw/EJL7Y0gfSB/Vd" +
                "jL2Ev3fS5TzD9Fb5jZJ+s/y7EuLcM9vrxL3F99rvQhrVsrsm9z" +
                "5OYwXE63WVwPPS+P6MvxHx+1Oj0BwbiyjW3lb+Te/HV4x1XSj7" +
                "9hTnwfN8oMY20suiyizSU2Yjy1h4XK89K86A9TRjscJxzxyKlF" +
                "Xba2umaIEdyNXaO/ZMiG9oU+EY9P1U90sQ/8uIqaT78SBbfLzW" +
                "Qj57PEabjbJChUGdzTHByxx8XBFb8NQxklXBoGCGPNBWVNHMp0" +
                "r2xSoMqsqDEYtgoejAc4vZgj1K791Cun3RbLHXo50jyC8tsqEd" +
                "NXuENRrBZ+4TjJDulB6SkkV5Tbs85t6ewTMiRPj+7I5nHHjskB" +
                "m2C2Z+WOLqtWLGzNu328xttXmuRwn3D0Fzemxx8VqTFoxmm78T" +
                "moeRWIlsjIHRKGvzyBv7KYbquCi7UttkBsUTI8yhKjqL63M1jJ" +
                "JeXYF5cHTgeT8cw7730r5SaE6HMcRgf3/o2S8sSvpcN8JcJ8nu" +
                "R8MSgz2Y473DBc79AUfU9NnD9maZHSPyzELt4WIGwzpL1C9yA+" +
                "9hVXlY1QtzDGfncFF1o9mIPUrv3Ui6fc1sTHehnSPITxZ7kD2M" +
                "JdH8+47CDp/3qxgBPu/CQ1KyKKsJ3NbE3NszmAMz6bglnnHgsU" +
                "Nm2LVxZb1WzJh5u+af2b7TP70xHt/HyefJcB5XuYe4E/Q8We5D" +
                "GoUfpbzDlFp+XDzTrcodlLzfxPdx7O+fEaqM07+Vr1Xt30q2Lo" +
                "N3Nvg+boznydV4p1e2/6mp54fuebKz5MeK94ZDY9xv3mvuxR6l" +
                "X+t7Sa8tdr3dTzEYQX6yYBRLtEs0Rg1sF2sE9qCsLZYsymvWX5" +
                "DZMaLkq5lolnrMGRTFlbGCZKLOT4qumVr1VdejrFRIc3pscfFa" +
                "kxaMZpv/XeGrGImVyMYYGI2y+ip5Yz/FUB0XZf9Z22QGxRMjzK" +
                "EqOovrczWMkl5dgXlwNI7tgvYznj839orYpp80hf8xclDdzbj7" +
                "o7/Esb0c2mUl90frxrq/aH8qNeb90Z6y5z7MZMz7o7Xt30nt90" +
                "e9T5bfH8VPl2Je5j5zH/YoyeaO2uTaZHOffQ7tHEF+aZEN7RKN" +
                "UcPnfbJGYA9JrjJWTft8zL09gzkwE1dbzxjHcD0SGY2H4sp6rZ" +
                "gx8/atB44R33tpRwvN6TCGGOx7Qs9+YVHS57oR5jpJdj8akRjs" +
                "wRzvHSlwegKOqOmzR+ywzI4ReWah9kgxgxGdJeoXuYH3iKo8ou" +
                "qFOYbVHCmqroRjr++9BAtpTo8sPl5pyrJX23ylvRgZKpGNa6xk" +
                "j9lbeCM/xVAdF2X3apvMKOL3Frl7Uar5Sf/Kws+896rKul6BSd" +
                "GB5yazqfqm61HCd2vQnM4WjHQ9+6VFSsx1I0LzNTfRCHElAkrM" +
                "cV4csZ814ll9074gs2NEnhnWJkQ5G82YZ4gctZcqSCYOE/9RJL" +
                "SPm49XD7keJfiC5nQ3tj9ni4vXmrRgPtt8pUMYiZXIxhgYjbJ6" +
                "iLyxn2Kojouy+7RNZlA8McIcqqKzKBa+P4tqGCW9ugLz4OjAc4" +
                "PZUH3Z9SjBFzSnswUjXc9+aZESc92I0PwMN9AIcSUCSsxxXhyx" +
                "nzXiWX0Z1nODrKcReWZYmxDlbDRjniFy1F6qIJk4zHB+vkyZ7s" +
                "pfW4g7ACfh+reQdgS1hWw52fs48qPUuwh7pOx9HCPK+6PaQvS5" +
                "42Tv45y3tjBbKt/DMWv5Po4YYU64k1qos8rfxzmPPSyrI4Jkgn" +
                "zlfGBNt8FxwPde2t8UmtNhDDHYbws9+4VFSZ/rRpjrJNn96IDE" +
                "YA/meO+BAmdbwBE1ffYBOD+3yXoakWcWah8oZnBAZ4n6RW7gfU" +
                "BVPqDqhTmG6/uBoupas9a+5XqQx81a//vPtUEfqhlncT0eLh5z" +
                "pEZ+jJbN11qLI8rxf88CkfHk75NjP9f0vI46hMxKm86g+Gw85f" +
                "rff67VswGkd2R9roZRurLMC9lvU3Q2rojearZW97keJXwXBM3p" +
                "bMFI17NfWqTEXDciND/DrTRCXImAEnOcF0fsZ414VvdlmcyOEX" +
                "lmWJsQ5Ww0Y54hctReqiCZOMzw/bmPMk/+e4Za56n8ngGjSJ7K" +
                "7xkoo/35Evnf7fcMtc7sUck9m3iqv2eIa4/x/dkZV0ZO7/p7hr" +
                "vMXbX5rkcJSEFzOlsw0vXslxYpMdeNCM2fI3fRKJssMdiDOa4q" +
                "jtjPGvGszc8zmR0j8sywNiHK2WjGPEO/nvO1lypIJg4zrP78ou" +
                "oUOPb4HiTM9YxCc3YYQ0xh8fFaC/ns8fzLbTTaU3inKEnV9ggO" +
                "3l9UKniaPfnd2iYzNDPkgbaiimZe8CiuMHu0lxEiHjQj4nmnub" +
                "P2IdejhLUOmtPZgpGuZ7+0SIm5bkRovuadNJI1OMNZEcUdZEU/" +
                "a8Sz9qH8WzI7RuSZYW1ClLPRjHmGxFB6qYJk4jB5PqHqRDj2+x" +
                "4knJ9TCs3ZYQwxhcXHay3ks8fzL7fRaH/hnagkVdsvOHh/Uang" +
                "afanNW2TGZoZ8kBbUUUzL3gU5+d+7WWEiAfNiHjebm6vvuJ6lH" +
                "CtCprT3bjRj3ZncT37pUVKzHUjQvM1b6cR4koElJjjvDhiP2vE" +
                "s/pK44TMjhF5ZlibEJGRzKIx3G8WuRglvVRBMnGY4fr+Cmee7H" +
                "lybQmcsbeO/TzZ/rG/rodf7teW0PPk7Dvxk9be89T1fYl4cju7" +
                "/WlyNu3dnifXlvSee6rPk+UOA+dU9jw53aGu70vi58nZ1FN6nn" +
                "yDuaG2wPUoASloTo8tLj47S2qu6fwQNdNJyqVKOJKIGI2ytgB9" +
                "WDeuwoeLqy3ovUzbZAbFEyPMoSo6i+tzNYySXl2BeXB0GP8/hf" +
                "f55A==");
            
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
            final int compressedBytes = 3866;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtXG2MXGUVHjBN2gImxRYotlJQKIkmtlSJiVrfne4Eu1WoyJ" +
                "dGtjVtbS1LTFZIKZC2d+bO7J0puPRLyxbahYbErzSxYORDQgzx" +
                "40dREPGHmEgxJDXLH1DxBxq973veM8855767BeLcnPfc93w85z" +
                "mHO9M7dza0vl2ruUdq5SubTdo90piq1egse8uv3p5dUKu1v1yr" +
                "NaZK65Lsklr/1b6e8gLGP2u15roYtYIw+nHX8plHbF/Nu86HYG" +
                "dvmX1DdlNAXNeYat7cmGrfiIrEsMS4LpubnepznVdGn99nsnjX" +
                "RSFmncRuf6l/9tXsyuC/Bqjtr7WHeVcszoZKyw0RbS3ltddm5Q" +
                "zaN7Vvzm4hSzasu3RXuatoJc022q8a5FVG2B37KVoKKqCGfwFR" +
                "VsfL+lGTI1cNjn1K2nSG7kjapsfGLJiBrizzZB3uMsohd6j+ml" +
                "9J12q883t/ns8nu7f4FX5pkZpy/RmjhYqH+IxwJQJpyvFeOoMf" +
                "O+ZZfy1fILMtIjqj2oxIjGQWnzcnkUtR0ssVJBOPSS+OLCV3Oa" +
                "2kQ/Wc9/UdbEEk/NIihewSDahUf+BhjQAPa8kiXbOz3HKvZoAD" +
                "mFBPVWydUSyxlfWswBi8g2xym2glHXybeJ8vthZEY6fzpXAuMK" +
                "JtBWPIbMVvU7omIsc+rW3ab3PBJF+UxsYswqf6K6lu9UGYzDfK" +
                "UXeUVtLBd5T3jVPuaOMU2RHBfra0TpEeuFtiSTSgxlkc0AjwsJ" +
                "YsUjUH7vbcNPdqBjiAiWZpETi2+KitrGcFxuAdZJvbRivp4NvG" +
                "+3yJ25ZfQnZEsJ8t5TyDHrhLYkk0oMZ5PqUR4GEtWaRqDtyVf9" +
                "hyr2aAA5holhaBYwfOtpX1rMAYvIPsdDtpJR18O3nfeJktiIRf" +
                "WqSQXaIBNc7zBY0AD2vJIlWzc4fnprlXM8ABTDrbbMcSgWOLZb" +
                "aynhUYg3eQHW4HraSDbwfv80vZgkj42VJen0EPbJdYEg2o8e7u" +
                "DI0AD2vJIlVzYHt+meVezQAHMNEsLQLHDpxjK+tZgTF4B5ntZt" +
                "PqdfTN5n1+OVlhQTTOynkKD8VLkRhxnnNkVXiqHGxNYIx9Rtu0" +
                "X+ZiDT0t1bU1D64/Nr/arWahGfejh9wQraSDb4iOVStXrdQWir" +
                "A7nS+Fc4ER79dXMobMVv+9h9I1Edk8rG3ab3PBxNaWseBdHEx1" +
                "qw/CZL5RNrqNtJIOvo10tJ4bfEJbKMLudL4UzgUG2TyuRIGnP8" +
                "+N6ZqIHFupbdpvc8HE1pax4F38ONWtPgiT+UbZ7/bTSjr49tOx" +
                "ataqWWxBJPzSIoXsEg2o8fqcpRHgYY0q09Uc+4nlXs0ABzChnq" +
                "rYOqO70FbWswJj8A7ScR1aSQdfh/eDT7IFkfBLixSySzSgxuvz" +
                "SY0AD2vJIl1z7GnLvZoBDmBCPVWxdUZ3ka2sZwXG4B1kn9tHK+" +
                "ng28f7wcfZgkj4pUUK2SUaUOM8H9cI8LCWLNI1x56x3KsZ4AAm" +
                "1FMVW2d0L7aV9azAGLyDHHaHaSUdfId5n3+cLYiEX1q8dO6UWB" +
                "INqPHf93M1AjysJYt0zbETlns1AxzApLz/NB1LBI7t3mgr61mB" +
                "MXh7GVw6uJRW0uHqWcr7fBlbEAm/tEghu0QDanwe86xGgIe1ZJ" +
                "GuOfac5V7NAAcw8bV1xxKBY7v32sp6VmAM3l5c4QpaSYdZF7zP" +
                "l7MFkfBLixSySzSgxmfIBgEe1pJFumbrPMu9mgEOYNIZsx1LBI" +
                "7t3m8r61mBMXgHGXSDtJIOvkHe51e4wc490oJo7HS+FEYDarw+" +
                "f8kYMlvxG0zXRGTnO9qm/TYXTGxtju305Ay6h1Ld6oMwmW+UrW" +
                "4rraSDbyvv65dbC6Kx0/lSOBcY8fnnQ4whs9U8t6ZrIjJfoW3a" +
                "b3PBxPeUwsYswjyPpLrVB2Ey3ygb3AZaSQffBt43XrIWRGOn86" +
                "VwLjDiPL/HGDJbzXNDuiYiPbcqiq3GHjCReZY9eHf/lupWH4TJ" +
                "fKOsdqtpJR18q3mff9JaEI2dzpfCucCI/76fzxgyW81zdbomIh" +
                "v/0Dbtt7lgkn8ijY1ZhE+T46lu9UGYzDdKz/VoJR18Pd7XL3O9" +
                "fITsiGC/tEghu0QDapznZ4Ew9kHpYS1ZpGuOLbLcqxngACa+J9" +
                "0xnTcnZUZvoa2sZwXG4B1ku9tOK+ng2877RpstiIRfWqSQXaIB" +
                "Nb7fD2oEeFhLFumaTWe5VzPAAUyopyq2zugN2sp6VmAM3kG6rk" +
                "sr6eDr8r7xClsQCb+0SCG7RANqnOcBjQAPa8kiXbOcp+FezQAH" +
                "MKGeqtg6o/cVW1nPCozBO8gcN4dWr6NvDu9zR1ZYEC3zpIfiUz" +
                "bUGLhfZ8s8zcHWBEY5zzk6SvplLtbQ0+eqzIHA9Xvj1W41C824" +
                "H525jFbSwZfxvr6ALYhsvsV+5Gghu0QDapznhEaAh7VkgRhpaf" +
                "7Lcq9mgAOYUE9VbJ3RO2Ar61mBMXgHWe/W00o6+Nbzvv4Ba0E0" +
                "djpfCucCI87zu4whsxW/9emaiCyvz/VVFFuNPWDie0phYxZhns" +
                "+nutUHYTLfKMNumFbSwTfM+7xuLYjGTudLKb8V/p08iAnPgu9l" +
                "DJmt5jmcronIYrm2ab/NZVvZ00AaG7MI8/yTG269abvVB2EyX5" +
                "LGlP/bKr/yX4Hxzu/r5zWm/PdNWBBNZ+6MxlTrlPQQghS2oYZ7" +
                "Tlb16661nKc52JrA6Pxb27S/RLyIc7FyT7I2nzcn5QwG3q52q1" +
                "lIZHSLV+M4euGz/PNl/RPsbf+XfNkSxJZ9CYzWCv77OokX/77u" +
                "P8AmzJlejePIb95cVjnTRngMGeXP5M7/fV3qVT/R2hMrTFk8vH" +
                "ovS/7Z2tjD22y1uv++OtedSytpttG+fhtbECn9M4mugBrh/AXG" +
                "kNWrzKo1EVk8Y7lrv+4IFuqpiq0zen9OdZuqw9VI5JVcfb/nQ/" +
                "L9m3q/V0Vf4en3e/EN+07dtbZyhZ7m/V48qz+rrH+693u+Ov1+" +
                "1zPYfXb6M6taR7/fZ/78zK8uP1ce/n/Ms/mI+vw8IT63Hnpv82" +
                "x96z3O84vv5PNz90B1nuk6ep5u3I3TSjp0O877+sVuPHx/H0ck" +
                "/NIihewSrbya/gp7afstEIqTyjPefweZCrZm8arlXs0ABzDxPe" +
                "mO6bz8/i4ydn/BVtazAmPwDjLiRmglHXwjvM+/7kaKN6QF0djp" +
                "fCmMBtRoe54xSvw3pafPbyRdE5Gtk9qm/TYXTPL1aexynmIGu7" +
                "ekutUHYTLfKOWLVtLB53hfv6L8kvw+aUE0djpfCqMBNdp+5/qv" +
                "7pnS05+nS9dEZDlPp6Ok3+aCie8phV3OU8xg9y9S3eoXYTLfKG" +
                "vcGlpJB98a3teXWQuisdP5UjgXGNH2EmPIbDXPNemaiOyeo23a" +
                "b3PBxPeUwsYswjzfTHWrD8JkvlFG3SitpINvlPf1+9xo+PwclR" +
                "F2p/NJfGasNCpR473efYwhs9U8R9M1EZnfqm3ab3PBxNbm2PL6" +
                "FDO4Z1aqW30QJvONcpY7i1avo+8s3tf3kRUWRMs86aF4ytQ21G" +
                "CvrV7lYGsCo/t+bdN+mYsVPVWxwZQZ2G41C824H73ALag/4FfS" +
                "Zb2483t/TitF+hV+v2OL1HQeJ/cARXIW2YABPG9nrz+zVWQGMN" +
                "gjEWOH/Y6YD5jILD4vr89+NYqSXq4gmXCPwCxlr9tLK+lQfS/v" +
                "8+1sQST80iLFH/mdEg2osdcXNYLw7O1fH6aCrdm9yHKvZoADmO" +
                "R32I4lAhjYynpWYGyiN7vNtJIOvs28z3dZC6Kx0/lSOBcY0fZ7" +
                "xpDZit/mdE1Edq/VNu23uWCS70xjYxbMoNqtPjhKRos7/GP9b/" +
                "+P9s8eK+uPSG98HnIs/ayhfYyeh+SPSbzqy/0B58XJcPczYb4d" +
                "HUO+fx5SfRWv6ih/JnfTPQ/p7OWI/KfS3py0DPDdJ1trrVb3O5" +
                "t0k7SSZhvt6x9zk90O2RHBfmmRQnaJBtQ4i80aAR43KZnNVLO8" +
                "/zTcqxngACa+J90xnTfVDGw/EhEWOUfJXT+9y57oP3+7unxfhb" +
                "/TaV0jI/V3V/1qrks+f9PPxlo4755Z9WtL+vpsnbTPCXQf0z6v" +
                "a6UYJa7PqRmfJ07jdRNuglbSbKN93nQT4fvmBCLhlxYpZJdoQI" +
                "3vuac0AjxuQjKbqWZ5fRru1QxwAJPOk7ZjOi+vT5Fh+5GIsMg5" +
                "Rrnd3U4r6eC7nfeNP7IFkfBLixR/5NdLNKDGeb6kEeBhLVmka3" +
                "YfttyrGeAAJp0XbccSgWPzG2xlPSswBu8wsZmff7bf/fNP++Q/" +
                "+fxzS/r3jnfzvK57/D0+r9v5Tp5/TvebzWmf1z3oHqSVdJj1g7" +
                "zPx9iCSPilRYo/GhdINKDG6hdoBHhYSxbpmt1HLfdqBjiAia+t" +
                "O5YIHMtRmoetI65Pjt7ittBKOvi28D4vrAXR2Ol8ksZ8yuQKiA" +
                "ls5zOGzFb8tqRrIrL7qrZpv80FE1tbxmIGxN92qw/CZL5R9rg9" +
                "tJIOvj28z7tuT/0g2RHBfmmR4o/GhRINqHGeFwKh8RfpYS1ZpG" +
                "vWD1ru1QxwABNfW3dM58BjhrqynhUYgzdbio8UHy4ubkwV8f8K" +
                "kt3Wv7cop9K7VN4fZD8I9/NTM/86mS2b+X6j+OZpf9/sfz5lPy" +
                "rl+wb/h3y/JD/19e/N094vXfhO7oe0X9zPT6V1f67z3DxavYaN" +
                "9vXFbl5vubQgWuZJDyGkbKhRjCC7twyeKgdbExjl/dI8HSX9Mh" +
                "cr92SZA0FOxnarWWjG/eiWa9FKOvhavM8fZwsi4WdL6xQ8wJJo" +
                "QI3z3KER4HH9O33pr9Ys3zfXWe7VDHAAk/xntmOJAAa2sp4VGJ" +
                "voI+4IraSD7wjv64vckV6X7Ihgv7RIIbtEA2r864s3NAI8rCWL" +
                "dM3y+jTcqxngACa+J92xRAADW1nPCox1dPil9Fb6/y/pz65wr/" +
                "Zz/fcz/DwkuyT91ybYN+ZWPwnFZ9Pc4r7T3X9mN8m7vmK/ve/L" +
                "ny72ZHOBn81rTGXnoxbdfyZ+v54b468MuAfsr+UclQ3Jz0/rz2" +
                "6Z5vf31xuv08qad/6s+evG673dsGi/tSjPwumim78pbQux7y3j" +
                "SK7PZ9Md3uuvz+lY0XnzVxyp0WVt+GX3IXah9qb75miOLE/+B+" +
                "JxqhU=");
            
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
            final int compressedBytes = 4101;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuMXVUVvoCYisLQ91CKLShQSWnBItBSyp57eouPmc7Q2i" +
                "kUC4JYaDGECCiaWGbfuWc6rxAggCYqFkL5J85IR/glagkBH038" +
                "JRIfkZpQUpqWNmpEHu6911lnPfa+9476w3uyX2t9a61v7dlnn3" +
                "vPOa2539xfqZj7TdFWKjjy/cbzKAEp9QgTF3/UZnFv5LUSPrVZ" +
                "0gNpsOUs0jEbPzOKe2xBHIiJjy0z5h4QiyjJQ8ehD6LtSfYDrj" +
                "7ZftDOQJ39sO2wp9nZlUq1y84Jknm2055pF7reR11ZbM+x59rz" +
                "XO98V5a4coErS11ZZi8M+ItcWeHKJa58yl4eZFfaq+waiFA90d" +
                "acZJ292n7atZ+xnysibys59Ntr7RfsVnuj63/RlZvszfZWe5s9" +
                "wZ4IHvKr7YfsKfYj9lR7unV/PTvTzrXz7QJ7RrA/K6/ZRa49O4" +
                "w+Eerl9uKQ0ydd/zJ7qV3p2itcWW2Nrbo2s2tphuxnXel2pcf2" +
                "2j67PsiusRvt5+0mu9lusdcHyQ0F+kuu3OLmdJvZBjW0Ya634b" +
                "ixT0sITSNpzwvakg+QDU+gD24t/t7b0jEJOfpDKZN6bUtMGr9I" +
                "+6a5QAZxtvJAlEBvNVuhhjbotuK48UstITSNBg/KMRW0JR/FfL" +
                "6CPnh0MZ9b0zEJOfq0lEm9tiUmjZfTvmkukEGcrTwQJdDr3LEn" +
                "1KF1Ehy5cePXQVpKAl6MuATHKAtx9oBm+PeICTKKsa7gECKXWq" +
                "VHDPIizyTjFiV+T2m7B1o3n7+SVhyL0QoPe0RkGa/0iVwKnmNm" +
                "DGpog3YMx7U7UUJI0nMJLyDn3shrsT5flR5Igy1nkY45+lvNPb" +
                "YgDsQEcop9SwudD9eThJ1PiO4yXVBDG3RdOK69riWEppG0h1I7" +
                "EywxAmHC1fNM9MGtBb+udEzm43Upk3ptS0x0bI6lOQD+Olt5gE" +
                "/kC6Xxo0ol/6ord5iNTrIxXKvexqwafy/z20itXYyjxjOV5Kex" +
                "lywaP471a/cI9E9cmVTzuRHs8zudbiIZ4x+EQmZ8RH0du/E0IB" +
                "pTKT3PFHu2T0eIUZGfTVDcfL2HstofuDZ/H1o3n5vIbqgyeLCZ" +
                "P/7J36X+8N8qbT5mk7QfOlEjRn8nUcQ/ZU8fzKmZHuT1E7ivYj" +
                "435f9CqW7xU59fqYy41TLypOl32v4gm11q55U++6l189kf86h3" +
                "lr1Z3CL1GfpT2/nsl/YjT0Xz+Qqg6nOIGdloexb7jzKjOLLWm/" +
                "5iPvtjbfMsa4ecdkOYj/JbbeNtrg32G/x84qjVpxVmZMbwA+2s" +
                "pf3ww9Hf77HhBznK9/gIcknsE//k/IYfieZzg+ZvNhTzuYGkuh" +
                "VcPz78seGza1PD54C+fnLJyrrRE6E3RfZuPqemMZ8CUxc75si5" +
                "TPN42rpdjPqTEuV7fDSwqAkvm+Ln/O1uzh/mU8zDVNpLyC6c71" +
                "1DlUrXUNcdvi73vHLXQpnX28WEaP5phWlv75gMtT7f60aigD+z" +
                "v6N17NYcZHyaT22tvdAZU5uUMl8a79QODW4NkknAFetzEhB81f" +
                "PzLfQn02duYHHEn+9eUztUf9zXA33qPJr0MSjm8MOAJm+DN7jz" +
                "naMmOcva5MAiuReU5/t9hIfzveCh1+ckn089S2WkSTkD2bHsGJ" +
                "zvdoY/3/3Hy3zt1udJ2bHRU32/ftiPQervhwACZCDFUqJWcG/q" +
                "O8sTqHH+34SWLPF+CEqxxqOYz7841Cl2O0rszOyYnY9YexaPyx" +
                "kM/bnAX0ox/aHn098PofkseRXfj+ztIKm/KWfA9Jre4vreW+6/" +
                "vb5verP52XzT2/iK70MBqV2MErRBPWI8Ckbeg29HjxM77xdxpn" +
                "f0LWjJk+QBspGnAIXcnMdjEiUZcLn06hlJKXjV8+mRNJ/cg4yq" +
                "ZqDPFGeaKfcIkJm+bHm23PSF+exDvZciWtpyDKD8yHvw7ej77I" +
                "ybgT78MfoutJoF+SeviAwe35MoyYDLpVfPSErBa2I+l3Muaj77" +
                "pKScgfVmfSFZX+qCzKwf/E22EqQwhl5jJkm4Lcc41CwYeQ/kuW" +
                "S6EnHoJ2ZB/skrWVQqYx0SJRlwufSKOZGUx49Zxh5kVBk7O5od" +
                "LXbUvaWfIHP1CnccBUltL/ayFR5bII6iBRVEgT/vAaWM6QqMg3" +
                "64J2DjY2BMQhNybK5E+R6xJDn3iowQT/nGHJ1+BbNSs4TWOC5n" +
                "4HB2GGpog+5wccx1x2F/viMm1HNLfWkjC9gWo7mAohhBxnyMvk" +
                "UaZMBZCK9MMnpMc48tKCP0CIxkxtCv7+YWmKnkoeOw+S4sTY/p" +
                "KVZsT7l2g8yXtdtNT9g/e1DP0dKWYwg7eJxQ7Pq+HXGmx+2fPS" +
                "kW0g+iiVu5f0YsNDvp1eckpeA13j8Hj3Eu6nxXWWHs7HhWXHlr" +
                "5b0ikPmydkd2PHxfOl57BnBQw4hsEc/H6I9QLKcdqMmOj90FLV" +
                "lCBB+DxwQUIf33JY7yPWJJcu4VYvMcyHP0/VPcO9OzhNaUJRTT" +
                "bbqLGe4u5zrITHc2L5tnusP67Ea9lyJa2nIMoPzIe/Dt2DcYt3" +
                "nowx9jX4dWsyD/5BWRweO9EiUZcLn06hlJKXhNXN/ncS5qfXZL" +
                "CcbOjmRHoIY2+DmC4/z07EjYP48QkvRcwgvIuTeXfYPk7ttkjT" +
                "yM1bkGW84iHXNsUHOPLYgDMalu1xlD3+2fzELnwz2ShM39Ec5d" +
                "/iazdxeYzqzTzeksrrXPe1m7+0v2oqwTMFmn0vwU/f5n95fiTz" +
                "5bovT9pWb3QzB27ZBd2fJ+yKEUy3b3l8xsMxtqaFEG43yumT00" +
                "DnJCkL5VkREoRlif69AHjx4zi2MScugdzV3qZUYkqe7QGUO/vp" +
                "tb8Fx4hDiOQneYDqhNR6nrwHG+zHSE/bOUEJrbcQ14SMkoxsg1" +
                "ZO32zw5uJznomORj7F4pk3puS7VvG/fFzP3h5pPNgeQvvUqJRN" +
                "u7Wr0fUnvVzvHzad3O3Pz9kMGD9H4Inu/sDE+8HzKyg94PGavT" +
                "+yFiZ2j5fojfP/37IU6TfD+kUhlY1OT9kG3p90Pqu6fzfogr4v" +
                "2QEl28H5Lar2gnGrxH7x10n452EHp+FO5G39h8R0Sbkfva7490" +
                "r66+NbVjj8/U9wslL9g/yQvyrt7GuXC/khO/BxnHKe8afk3tUl" +
                "eYK6D2LclgnF8GUpIQmttxDeBTMoox0pDW3E5y0DHJx/gsKZN6" +
                "bku1b4fejZmTBz4zOlvJQjKmWMV4jVlDfShuPt2Z6r/PSzlgEU" +
                "92vF97Lbq+rKEYtdeoP35+k+dia7hHHgW/z5MPZBXzkrVvPTPi" +
                "Qn7d/rlG58KLjBPzUexXm9XUh+Lmc5WYz9VYCEGt7qcioL7r+9" +
                "QfX9oMzz3Gnt18ruZa9C8tZO3bfCXnQn7dfK7WufAi47TKNOyn" +
                "/Hr0LVdOC28FrhMYfz0agOuRq88rpEsYYlmLCOx61PWYrdmdbR" +
                "hdG+piN7Y3RfvnxXA9CtryeuTaM/j+mbgeuZzsff56FGTsHG12" +
                "PaLnR/x61OTb7UvZS1BDizIYVzeghJCk5xJeQM69kdfi/c8N0g" +
                "NpsOUs0jHBB+ceWxAHYgI5xb7JHzHhfuVcEWOJTl1n6TqZd9FV" +
                "UF4Pw3X34fTzOHntTD6P+y6/4vPncanfSOSHX8PHr4qeKIpfSM" +
                "2ex+Um/o0z3W8b7NfUI1JSPo/bl+2DGtow1/twXO1GCSFJzyW8" +
                "gJx7I6/FfO6WHkiDLWeRjjluNPfYgjgQE8gp9i0tdD5cTxK2Pg" +
                "s0++tOxeuzur5cG1NirUzFq1WvT3oDIv7bV9e3W5/+TYXm6zOs" +
                "s7UKNUUs4X2G9Posc5qS6zPPWr2PoWepnIEptT5fyF6AGtow1y" +
                "/guOt5lBCS9FzCC8i5N/JarM8fSA+kwZazSMcc79LcYwviQEwg" +
                "p9i3tND5cD1J2Pos0Gx9TsTrs3agXBsTZe+Ax7Zbn85yQqzWA2" +
                "L//B5bcQeS63PC27faPwsehJogln7UbH2WOU3I9SkZ8vkQmU6U" +
                "/A6JMa7PF7MXoYY2zPWLOO76OUoImd+MerKRxR/5rdybOzu3IT" +
                "78PnqUPOS3cA22nAV55ZL8y5p7bEEciAnkFPvOb+IWOh/ukSRs" +
                "fSJ6f7Zf7xcg8yW/Pdsfnnfsr00BDmo5ghYL2hMGUextpMdRk+" +
                "0fvwbaeP9EKflBpC/heQdD+Z4vg104Ip/ce75D5oCeW70PRh5o" +
                "/0QvcgaK7/2b/UEjKO5v+TL+PkIZFEL41t8PgRH5kH0dY2Qfac" +
                "eT77UilqIkfh9t5igcoURqSOZziv2H30ebNX9eKAeeSZTlBeYC" +
                "qH1LMhjnd4GUJITmdlwD+JSMYuzqkNbcTnLQMclHfreUST23pd" +
                "q3u06LmZMHPjM6W8lCMqZYYbTFHzSC4lbzG1oGhRDY8prL+Zhi" +
                "1N4wW9rcT9giPWr8rnneB0fhCCVSQ7Jdc1P+uSyVMc+BZxJluc" +
                "Qsgdos4TIY5ztBShJCczuuAXxKRjGqU9Ka20kOOiYiq1Pj35Qy" +
                "qee2VPtWx5Y8+MzobCULyZhihdF1/qARlEpl7cVaBoUQvg3753" +
                "WI4hbiL8diVJ/V2mh9Xic9anz1We4RWREvrSGZt4z9c1lY/3Nk" +
                "xjwHnkk6D3OhP2gExa3PQS2DQghsec3l6RjV57Q2xYh70fjqc9" +
                "wjsiJeWkMybxn757JUxjwHnkk6D7PUHzSC4mJfqmVQCIEtr7k8" +
                "HcNcYpa2mc+l0mMKTx6RFfHSGpJBTto/l6Uy5hFFJsk8zPX+oB" +
                "EUtz53aRkUQmDLay5Pxxg9WWtTjLiXFJ48IivipTUky4dS/rks" +
                "lTGPyDNJ5yHvWNEvx+qq9NMp/YxK381L3eHiMbK97e+Pxb9o1R" +
                "m/Sj5Vk8/j8H2GxPO4VSn/030ep+/KpPMw1/qDRlBc7Mu1DAoh" +
                "sOU1l6djjHZqbYoR9xLj82HyiKyIl9aQDHLS/rkslTHPgWeSzs" +
                "Ms8weNoLjYq7UMCiF8G67vyxDFLZrFGL3SLGszn8ukxxSePCIr" +
                "4qU1JIOctH8uS2XMI/JM0nmo9xnK50d5+W+SbWfx/OjB9PMj+5" +
                "B+fmRXNH9+NLrGv8/Q8ulRf7vnR07m/7+LB9LPj+xZzZ4f5aOh" +
                "/18+P6L/76KQ3JB+tiH3TxznD8j9KbwlNaDumyX3mWyg2CkHaK" +
                "fCXjbgpe2ed+goeufS3LUe7tfF+6eOLT0g1qMQKXfbmEfqXQ+5" +
                "l5fz+VA8n3Gm6fco4isLcdi16P83n7s+2no+m1+BpzOfZoFZAD" +
                "W0YU9YgOP8US0hNPUGD/Ix4HnhPorz/SoZldsV+9KCdEzykX9b" +
                "yqRe26LM2T2iM5bs0X+zbHUchV5oFkINbdAtxHHtr1piFjZm8l" +
                "HcAr4xC8bZPSTDnjub7klZY3weEVnROIWRCB5NsvKtjq3ZA9aj" +
                "OFJz0IzJMr6rX7+6/E7ynRiR7axM44OoFDrbOT0f/9un6fu00+" +
                "bfCjnY7F/T/hvwARGw");
            
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
            final int compressedBytes = 3266;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXH2MFVcVf8qHtNUFCtLdYoOtimiwWzSKtX68t7PPD1SgCI" +
                "rairXEVlv8qNIYsgTuW/Y9uggllAiJsKCG+E/VqKUWWiti/Ez8" +
                "R2pKULSQsGm6/xiqjX9675w5cz7uvTPzcKHzcufOPed3zvmd+2" +
                "bunblvds03zNRazUwz080MW3+3ZjdzjZlpesycWq2x0cxNJfNs" +
                "mW9eZ/cHbHm9ucm8ySy0R2+2ZZEtb7VlsS0319LN3FLLN/NO8+" +
                "60fp/5gHk/yEbXmaaVfNB8yHzY1h8xH615m1lr7jB3mnX26PO2" +
                "3GW+YO4x95pXmFfmiKvM1Xb/GjPLXGvr2ea15jpzvekD7dYFZo" +
                "GV3pgi35Lu+82SNKe32+Ol5l3mVlvfZst7Td0M2Doxgyz+Mls+" +
                "ZsvHbVlplqey221ZbdaYT5nPmM8Ktnfbsr5Wq/fUe2Dvatig5d" +
                "qNB0BKEkJzO64BfEhGMUbXk/XIQdL4HHTMEE/ORUfjfnlOvm9i" +
                "it51tjKiZEyx3NaccB9qQbGIn2oZFEJQXXSsY4x+kbQUQ6O5F+" +
                "1NekRWxKs5sXUB15BPiKf9c1koYx6RZ6J51ZfUl8De1SSD9sgh" +
                "kJKE0NyOawAfklGM0Q3SmttJDjpmiCf3oqNxv1lOYz5z8sB7Rm" +
                "crI0rGObq/3g97V2e6fmyPfB+kJKn3J9/iLayxIN6hCE0+IAJq" +
                "dXSfA/dMEo2RCB6N+wWJji15oG+H4kjZDz5jipWOptl8lLU225" +
                "KOBSM/ymW9trj5aCzZ5OYje7Qw0yxio/LNOZ9N5h1Q57p8Pto2" +
                "w2qbtcLNfNKsTet1WfuuAMbOR2Z3epTPR7ZO5yNzA1zvbj4yh/" +
                "h8BIzcfJTWtzF/bD6yDJfZks9HGeJ28wmcjzLJ52IZNI/6I9/I" +
                "z7Q2G3WOFvdGOuakmOEXQ/pkVa1kax7lEf2xWKMgnivD34EW9G" +
                "c8dnEOnTc2jw5fjPeSrgPIn+QRx/L+PKq1yViymWMjnC0KMA7t" +
                "aTeHpJoNReSMJENCASfe0jYUXecbRlkPm+O9pOvA1ZNf78mBvD" +
                "+P229qkYhzINlisTeV9GeKSo+2BLRbQlL//pPYECOSdew17O4/" +
                "c/xsW66TmCC3LDZc77S1DkuUvd4LWJr7It53JjthDzXKoD28HC" +
                "XDK3LkEOrJRpb0M5S1hgDFvVrZkPSAGGTA8f6H8+TcNSeOZPgh" +
                "nTH3kGOHIFPB24vD+zEre5I9sIc61e3B9sgJlBCS9FzCC8i5N+" +
                "nVXiWnpAeUow3H+x/pWXqRLAlJeBdbZsw9EAOdrYxHjCU6eM4e" +
                "zK/3p+M6e8XdGrJurZOo4Gj/tlrlLTnYulP7K/I/8qtiTGdxbR" +
                "K2znsibHclu2APNcqgPfLbZFenH+SIaI6jnmxkcZ/mOLQALb02" +
                "x7kPig4oziz8AVTnFs1dc+IZUa4utswYjluHuYVDcST3SBLej5" +
                "y72wbn57r9+Xf9O6//91c6p0pQg+eimvkhX9pfqf/5cUw8djeb" +
                "z9NDXB+4//xz6M6yfCtDDZ4vZ9FdRN9L7P4zHrvbCMEzaXeyG/" +
                "ZQowzaI39BCSFJzyW8gJx7k17ttfpV6QHlaMPx/kd6ll4kS0IS" +
                "vvMVnTH3QAx0tjIeMZZo1uN9gfPz7OU5Pzuj0e+9L/isNdHl+d" +
                "8XPz87D03K+dkXkc8ZnAN7qFEG7ZFzKCEk6bmEF5Bzb+Q1i3pG" +
                "emCaOZxZUUyfu29BSIY/ozPmHoiBjizjEWOJppW8wV65DujKyH" +
                "m+vuivR/JWeE10+EW5qpidIzv5+qXbb12pWeg1Sr3y6K9aEgPn" +
                "ZesCuaKbZ/4lzTLkNeV+MX5FDPZKCcauT69Ph72rs7Wn6dgeGQ" +
                "cpSQjN7bgG8CEZxRj9mrTmdpKDjhniyb3oaNwvSDoP+8zJA1v/" +
                "9LKVESVjihWYo47n39DGuK7wful4saazt3bZtxgHGj+r5VK8DT" +
                "9YgckT+Xw0EddV8xDWdMauQH9GOHQOdpfLJDD5Rd6fF+O6ah7C" +
                "msENVzILNfJt6C6XSWDyJB41Fvq69qu68YBb+2quGf0608y4NJ" +
                "btq7rloHOSiPa0y9WfbF48HfhN8fSl3H+iFWhGh3xN1yxL7GL3" +
                "n2SnZujTk3IuPpo8CnuoUQbt9qtRQkjScwkvIOfepFfbn9+THl" +
                "CONhIfjulz9y0ISfj2NTpj7oEY6MgyHjGW6GAfH8uvjRviusJv" +
                "6VixZvSHV2DUinCgnKrlMglMfpyPKbPiumoewprB+69kFmo+ur" +
                "+7XCaByVN5f86J66p5CGs6j13JLNT90s+7y6XLqGeTs7CHGmXQ" +
                "bt+IEkKSnkt4ATn3Rl4h6sBa6YE0yVnOrCimz923ICThXWyZMf" +
                "dADHRkGY8YS7R8t4c/z7r24JPy/SD+ppB+vyf+ThN//s7GsB/w" +
                "p3H+/C7t/Jiap1wF4Hp4ftcrAJiT77v4/SX9npR+4kekSd9XtD" +
                "V/PyR/X7G9wntf8TeT8r7iSdM0J1+e9xXbyy/r+4rz6vNgD3X6" +
                "bD8P24O/1BJCSzuqAc8L95Fd71NC1hifR9QxETkwRcukXtuiDH" +
                "PyfVNfIINwtjqOQvfWe2EPdarrxfbg01pCaGlHNeB54T6y/pwa" +
                "ssb4PKKOiciBqVom9doWZZiT75v6AhmEs9VxJFqOQXLkSmYmM5" +
                "sT7VWTP346v+H1z8s/furY1cfPcJzYrwiNDhTcp/dLY7Yc4jKw" +
                "IUR8K8KU2zc6ZZhWXaKIP7Riz5uIKfYfiu9y19Ya5eajJHtfMY" +
                "FzJ5+PEnc8t31Pwuej3yfefJRk81HC5qME7yMC81HqN/D+PMZP" +
                "0qP4fJRkWD4fJcH5KMnnoySfj6wsn4+S6HxkNcsSMR/Z1h/kfJ" +
                "TUGBrno1n1WQP/cntXpyNT1nJtd8wlDi9baE+adDQJyvAI/Uov" +
                "EAe0FAP0iCGerkgZt5DMgAfIMIpk7j6tw2iLvcC15EHywIyQZ9" +
                "q7i3mdLG6ewDZK2Pn8R6tXo4y0D/mUIwv69Z4tFqM2KXwnxmk1" +
                "gto0fnJp6Mj3Eo5hVoSxIZ/1F+ovDOxze6htX2ct19YSh5ctLg" +
                "E0ydJvbh8gIRLKyAegoR7Yh1qtRwzGAayUcQvUISOwwSjSiuJT" +
                "NEBxrYxAPAiNx/7sZL6Zj/mrwr+Gy/Nt+PluV0Qbz1VYMZ3ozq" +
                "u+x4jOR89V81+sL9bW/w0l68VP5/P7I7bs9ZGErbpJi8b5Yn1Y" +
                "Irdtb5AYzAFlMXuMXey/dUexPup93D8/QSaPis7Pciv9fY5emL" +
                "zzE6NVPj/Hq/hvjP9f5+d/oGTfzd35+fl4GEnYyuensGicK9aH" +
                "JcVeMQeUxewxdrH/1vpifUzbSt84b/RAyWT5qmfrXtun3xZsUm" +
                "SjJ+Apf1ZsXRvIQlgM/KlYH5aodcxTgGnN5czILmaPsbVevj/f" +
                "+nJx/DJ2FrEdCu7T8/MJrmXPR9srPB9tL5iPtpdbV4tBKOIPre" +
                "j1vr0Kh1h8bR1BzYSSfTcP5P15LIwkbNVNWnT+WqwPS4q9Yg4o" +
                "i9l3nqniv7WxWF+sNdP438vQ/VKS0D7T/a3VY/4u/14G9RznRf" +
                "hnznRmCGnOaI/49zKR++2kNYP/vUyS4N/LmH8Uz0cYW/+9jEa5" +
                "9c+QXWZ9X+HoehGKd36eCCPrF8vOnNYm346NYc+E/JZJwrx1Di" +
                "iL2WPsYv+toWJ9TJuuyy2l2n4PS/P1uqXuGCS0WrVttl5JQz3W" +
                "KSqbk8Cbt163NLReh/Hdp2i9zmkRgRpizdfrIA5wy9frlkqr8H" +
                "qd0/B80ANnAnx5PvboQnJh4CW3h9p+f1nLtd1xexvIncTtSc8l" +
                "vAZbd4Te0pgX8Aj8cg9Qg43TwhHpqYU8EUHxpEfKDGITXrLk8c" +
                "kWUFyLETgT5zM7519Cy5Lfj37t/X50zv/9aPh5/vuROV/++9GO" +
                "KS/j/7t4rMrvRxZ1Sb8fpWPBETiLXe1a+bu2R9on3R7keLY7vV" +
                "yLRz2/K0IZ1jum0rXoPIaeLzA+RIz/fwbiyTnI/88A8xHGAY6g" +
                "bzxFa/WcB/fnUOCTvXl8RK7PU278iI2kq/z5feBBqW2uwrrCs2" +
                "AE02rZ3p1Wbl0Wo9WWKHfEWzF7zKnMf1iPUl0XPZG28r/zap/G" +
                "5yP5K323az9eTs+WW5f+jcgp/daAeDNwIna/hLG1f/V89HA4vv" +
                "/uvdKvbK6EPdQog/bA3ubK9n+5hNDUkvZQnCWPQJg0p73og1tr" +
                "XqGYHCllUq9tiYmOzbHUBzo3zYF8SjTLYE3+3eR/J9t6RGqba7" +
                "CucH4WYHb0lFtXi0Eod8RbMfvG4+X8HCqsR6muA8jV/vXe6ZPa" +
                "5mqsy9eTJUZdq6XvrmKk6ih3xFsx+86z5fxsH+wP61Gq63z7H6" +
                "AfTSM=");
            
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
            final int compressedBytes = 2896;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXH2sFUcVv9IiDVhoRQrSCvq0WiyS1ramvkC9y94tHy9BCP" +
                "ioqfKp1saSGG1M/ENl90J26X01wX9arQlN/K/QtNhgrRpJqgT5" +
                "qBgTTYRa/lDDM89gW5VWTaw7O3vmnDMzOzPvvfvgvszXOb9zzm" +
                "/Ond27s/fua7VarZUHWq1kLBlbeSAZEyNRy57QyFq8pFzoASGl" +
                "oJeW0NdbYSX1wqOJhJ7QSkYQk7aAkz4oB/APst2LkZHkKPUyCv" +
                "QpD/QnR5gJsJN9iKQzR7/8hbziR3BWtvnJujtK4+qzt0XovR80" +
                "wHD3+lbLzgNj4UxsKM5A5BP71CZ6Wmdp8xo9rUcx2ekjUTozOj" +
                "NkLVuhkyPRz28HCSJRD5LuKGrQF/WGXut8vo97QA20lIUtJnrm" +
                "XrgFIhEf/VCfMfUAWEBxHnocfCn07M5sPfNSJkp+h9TCGGrbCD" +
                "HYojdtfS4BjbSyYdASavizozgDHpfaRM/rLG1eo+f1KCY7fSRK" +
                "+tX0ah2ZPpzOKeu5ZT7vTN9VSW4oy41V7/Uac3NVf0iu+vTDZV" +
                "lalmW19jbi7a70bv1o6S2vNPemq9LVZbsmHWpZXuln0s9W7baq" +
                "3pE+kH4xfVs6TUNdm15X9+aVZSGeP9PFCrOEZOq59KO19ON1uz" +
                "xtpyu1fD6nRVlnsLufjXaW5XOil91alpuyD9TyXSKfgMo+qHvp" +
                "LjXnnZVnw8wmv7MsdxvSW8p8rm9N+pUt9uhvt8ujQw34m3VUds" +
                "e4+AxaVsQTcn2qVXybkc+hbN0k81Cu/d6GPuRzyK3f3ZDv6Cch" +
                "3sNQgUw/pXrDRj4fnrT3zWU+P9+a8le2qSFTPw7KpwWV/io0dj" +
                "q9LNeoVXlPnbuXYmMNp+f2XJ++nA74PO55p2zBQ3qeai1+zxqS" +
                "+9wRBMt0JsFfX5b5ZXmFz0J/KUYf8+RkbTzo0H7J+448Ikq0S/" +
                "bkKz9PtVW7i458/iaiA4QPk7U5SvIn9rvcsd3+m+Lr1iGZyL6n" +
                "ek+Y63NiRx+uz+z75fH+wGU43r/bkKk9Qcf7nsnGb2+SpWTyFc" +
                "XpqXKNngUttu1N3tkcdGFW/t7PxhcjP8dRyN9tD7F1ffYklWeH" +
                "uGc9gt7adneldgP9fI8H4vI8me+nu6v2BiEzd1/GWWpAYuIBmy" +
                "72nn9tu1Q3iu82k7H2hiZmTTtImGGVz8PJGCDBk2jNvWrAsak+" +
                "w/N/unetE331Hpv6473peik74nuPKtSP+njmeUGdRV7sVz65Xe" +
                "/AlcunOSdrDiZ1/dn+hyy1r5+p9flvOxKx8v5SWASSzyfdervE" +
                "7RXmALIm++iZEP/RM259CDuVz6Mqn/8x4qxxjxvYreG4/OWpW5" +
                "cQqYlZvi8oBy/28Xj/tor9X4Ptave4YY6rOS5/ZQrzudrNLP9j" +
                "UA5+OVkeuD8in0f/U7IF1ZH9WPfx7DflaMB/vKfV3QTL/ZDfls" +
                "f7IS+bYd/+KHtJ7I/SUX1/VI/ek/26Id9HQvZH0ZF0re1+SLqR" +
                "74/SLeP5fC8M3d6finyOY63b83m0D0fR7zz6pny+4LbrrgHU+O" +
                "4vGXH2yQJ1lc9pVFu1QxwRDbn8IYrjon1+Nj5Mtd8kKOAPDJuY" +
                "qZnsm0h83ToKOBvj+bO4yvCXTOT6KRnjdr0+nu2brsx0puoc9u" +
                "eg1X+uf4xwv1kY9+6jte6x8nGeoziu94sp/Dxa62YWHQ7ycniy" +
                "7ymutOxPqvcXO5KvypDrT+16/g/+fULYftOcA/2+2JqpgyH+o4" +
                "Nu/Xj2NSSfF6Zof3T2yu2P8pMh1vmpCR4dagUm202Zrk22Q+vy" +
                "JHs2jLrHM92b/+0ue/AgUBBX4NGm2Z7iXXmx60GqtxbkFvN6ae" +
                "/PuTbZAm3AinRgOvf5rcNiIEr0RNl71G0Psd3+s7/a9SDVWwty" +
                "o/L1qvo8msG1yUZoA+bqwOSjfuuwGIgSPTpqss8v+PmVOXjdrg" +
                "ep3lpWeE8WqKt8XkO1VdvJLlFE8/kUMdX1UqdJ12Ttw2RvcBTw" +
                "l4xKTcfNy+2/Kb5u3ewleVYxVXdBipm6NlrFsXLs9idQHNd7zb" +
                "vynqX2ISjREwUYNjHL3jT5WVBv2fUg1VsLcqe6/lG7omIW1yY7" +
                "oQ2YqwPTu+S39sXoXsVRokdHTfbdAH4l6mq7HqR6G7h/X2B55/" +
                "qxf39z8tdD3RkT3L9PC7rumTbZ/XuyVfXU3Z9iIdcmW6Edjz/z" +
                "NfIFv3VYDMJ6K5tDo337LT8/gbLrQaq37J2e07Tf7M6ammvtkc" +
                "vwfXHT9Xx3ZuuyvBJ1Tdidq9bnjabWHPn8Wa5Z/ua39sXo3sBR" +
                "okdHTfb5WMgcugvsepDqbdj5s3tTyH5z/Pv3kS9fufUZhe0b+8" +
                "iE3F+6pV8+iyVsjxL34fPovRPLZz9ie9+Nt8tSM1XfZhRL7UjE" +
                "hq5PbtHZ6tbbJW6vMAeQNdlDbJ9/t96jnS5LnR+1lop77Mho+r" +
                "jfMWaR/92tt0vcXmEOIGuyzy+G+O/e6tY3aaOHoodkLVuQyXER" +
                "6RJE44jb04IREFOtkW3gg1rrvGwxKZLLuF63RSZ6bIrlvM3Z8j" +
                "9AUXT8Wmzs/aRMlKIjtTCGGiR8xLXcm7ZGXgUN+HFZQg1/NrYm" +
                "S4qkNvkFPZbNq5kTt0Rl4GJ8UdayrXQXYVzcCxJEop5KaJFy6g" +
                "291nO6xD2gBlrKwh7T5G5aIBLx+b/0GVMPyECPzOMhY44mVzTb" +
                "1Oe7uj/fXca1yTZo/Z9HHKN9Jnj3fBApHCV6dNRkD7Hd/vM37H" +
                "qQ6q0FucNyf2kN1yY7oA2YqwMz8nW/dVgMRIkeHTXZq/tLOzz3" +
                "l3a4ZqW3ap862B6UdXuQyuS4GJJSlCCa2lGNxNtkGKPT4tbUjn" +
                "PQY9p4Ui96NOpXSvTYnAfNjD5bHpEzxlj1eEV7BfZlKfP5NZb7" +
                "FVAQga3eN+4wrMAYxWwarwlPPdrQ6ANYmbx4Ldp8EeXC2etzoY" +
                "XH8bEvV+6nLefPu0ytOfL5s+yW5vitw2IgSvToqMk+Xxo2B7se" +
                "pHqrPpdOxidlLVuQyXGRxSeLrpQjAvQg6Y6iBn1Rb+i1Pt6v5R" +
                "5QE5+kzJpj2ribFohEvIjNZ0w9IAM9Mo+HjDX0qfiUrGVb6U7B" +
                "OHk0PpU8KuWIAD2V0CLl1Bt6rfP5Du4BNbH63luPoMcU3Dh30w" +
                "I5IBMRm8+YekAGemSeK2SsoU/Hp2Ut20p3GsbJ/vh0sl/KEQF6" +
                "KqFFyqk39Frncxb3gBpoKQt7TJO7aYFIxIvYfMbUAzLQI/N4yJ" +
                "ij5W9ninX83hr8bqX4ZMj/E9B/68J/j2N7UqN4N2h8/0+gWN/v" +
                "/yeQLwr5fwL++5T2/yeAz2snw3j/E57Xjh6E57WT4fp57Z7Ewv" +
                "PatQV7XjsdEc9rgz/xvLZx/fmNSuN4XjsZLmN4n9euUPXz2mVv" +
                "XlkWEo39ee0H5fPapd7xvDbNR4VZx6Wl9f10DM9rxyfiE7KWbb" +
                "V2T8C4WAwSRKKeSmiRcuoNvdb5/Cb3gBpoKQt7TJO7aYFIxBeL" +
                "9BlTD8hAj8zjIWMNfSw+JmvZVrpjMC56IEEk6qmEFimn3tBrnc" +
                "9vcQ+ogZaysMc0uZsWiER8tFCfMfUAWEBxHnockk9AH4+Py1q2" +
                "le44jIsfgASRqAdJeb2kNOiLekOvdT4z7gE10FIWtpjomXvhFo" +
                "hEfPERfcbUA2CLZXpkHg8ZI++qnInPGHeiKpkoxRGphTHUfFTm" +
                "8wzHUK0twkgXNNLKhrH5QQsTRRnocSk+ms/nYI8fzde5mOz0EZ" +
                "ckmy37o+Wmlo66g477S5sd90O8T+8mm132NpTo0VGTfTTXz0+g" +
                "7HqQ6q3z3grm8xNB34yNtsb5GvnO5L/z2rvKo2/4/Xw0L+j7mX" +
                "kTpPV/eKijtw==");
            
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
            final int compressedBytes = 3234;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFW/2vFGcVXowSpRbklg8ppeBVWpCmgvWjSLXD3V1qC21K/C" +
                "haaqFcuTVWLdoYNdHw3r1edisGK4WkrSb4k1KKLWj1X/CX/gvQ" +
                "jdKQ3IaEpNEf9X3nzJnnnPO+M7t8rOzk/TrnOc9zztyZu7Mzu9" +
                "n6bH2jka3P8pFetArr7ttkhQVozDoXpYfwskkOUjh8XKrCE+dg" +
                "NVN5SharJnnJsvWjWlvnwdyMktVqRZ0xtPLVhrBhRc2zrrU2ak" +
                "CEsXORV+DQc6tx+Kj12hdjoZLGAMUrtmgPbFST5Ze2VMVSUVaS" +
                "riP7NzV6Tf+TZ512GgnssK9BEbF/GA2J4RrYVhXfve9qMxrG25" +
                "5rz1EfRtgK+xRZGYOZXOnGeK0Qa8AS+oOPpjKLNW2emkX6D65G" +
                "RsiAa4q5ZYXIUNZuM9HZyqqz28OGFTWP+I61UQOCR9lLu1xbjd" +
                "q//+2aMYUHI2eFvKwHNqrJ8ktbqmKpKCuxebkfuff7/gNuvvtg" +
                "abvJLXIL3S3+3MjcktyyzLeV7jb/H+AhP1vjxt1ad4ef3enbOt" +
                "8+6dtdvt1dMGwUCp919+bjF9397kul1f8ncdvcA+7LfnzQbY/3" +
                "ltvldrsn3JN+tse3ve4pN+WedvPc+0rEh9wC39/sPuLG/LjYLX" +
                "XL3a1uBR+fbrW3fixH5u8U7lNuk6/pfvdpP/+8+5zb7Mctvt3n" +
                "Mjfhx6Zr+QofLNhDpTt8e9i3R90juW2nb191X3OPuW+6x1W2+3" +
                "ybLP8Wa8KGFTWv/S9rowYEj7KXdrmGxsTL1hsde2s0YwoPRs4K" +
                "eVkPbBMvp/ilLVWxVJSVRFWuzFZSTyPbaN27KVs5e1hagNZxGI" +
                "lBNsla7M+XUtGsL3OwmuCY/a+2ab+NRa1Wm+fTJ+Q+0PlbhTjj" +
                "Er0qW0U9jblvFa97i60FaB2HkfCySQ5Sb/0qFc36UtFqgsPatN" +
                "/Gsi3Wttkzf1W1VkejUy9xvfRwYySv1qHGyF8HV98I7WxjtpH6" +
                "bKO00bp3J1lhAVrGSQ/hUzZotJ7X0TJO52A1U3lKFqsmeclitX" +
                "Uecs/YarWizhha+rX1WXV8fiXlvw7HZ3f0x2dVpqPVbh5tHqWe" +
                "RraFrfNWbwNbgIRfWmQju2STrHlNPc3Ado6R+HjTzJpFZwkk8E" +
                "FbVywZkIGtVushY4M+0jxCPY257wive/ewBUj4pUU2sks2zeqv" +
                "qU9qBrZzjMTHm2bWLDpLIIEP2rpiyYAMbLVaDxlrdPKYfYlnvX" +
                "XVvtqjfgCqO/T7XG994LJ8w2RRhenuGOn5/kLzBeppZBute/ey" +
                "BUj4pUU2sks2zeqPkVOage0cI/Hxppk1i84SSOCDtq5YMiADW6" +
                "3WQ8Yanb5HwrOJhdW+utdwqOFeV8tVdb0U13Rdj8/jzePU08g2" +
                "Wnd3sQVI+KVFNrJLNs0qVzqCYzTCbppZs+gsgQS+84atWDIgA1" +
                "ut1kPGGp16uefK/zXfiHwnzVXJtiTDxmFQw73cqSu8TtpWrzn7" +
                "yEiPz2PNY9TTyDZa9zazBUj4pUU2sks2zeqvWf6qGdjOMRIfb5" +
                "pZs+gsgQQ+aOuKJQMysNVqPWSs0cXfcoxa9Hnz69FfP0cCm6Mu" +
                "DnHUqIjWyXp/2qJfncc0hmtgW1U8aw/ir/cPik59fu+81Xl8RJ" +
                "/f/3ztHL98YIC/PTrtmvP9xeaL1NPItrD5z0db2AIk/NIiG9kl" +
                "m2TNa/q7ZmA7x0h8vGlmzaKzBBL4oK0rlgzIwFar9ZCxRifPpD" +
                "3lbO+Ijs/Xb+D9pZFq47lSa4l9zuWPzwn5HEpeDcarQc/jVE1n" +
                "5BM6+TwOWeinYFXPwSQ/Mggs9Dwuzqd1xmaZYh10Fdxakn4e11" +
                "rWWlYglpXY3BZabzdZeQ1UvAJGckkFYTvFHopKZYFI7nkrPok+" +
                "Yfl1llJXsZ6yWpK18oxaZtfawtr0PG76tulPyOtPt8j3t/jjc6" +
                "98HpfPflxg8m8FhOdx+TjE8zil/rfcU/s8ztt3u3yfufz/j30e" +
                "N1tcVYTnccVsqW+3lu+qm1x5xrv1Ujs8j8utm4uxeB5XexUcXb" +
                "Wmn8e1xlvj1NOYK47zurePLUDCLy2ykV2ygbWo6bRmEJ7xsm6j" +
                "YDXj3OMIIAX+tK1YMiADq6z1kLFGF39LdXx29pezqRG9H/3x2j" +
                "lmB1z1Tm8anXZNZWOtMeppZBute0+zBUj4pUU2sks2sBaqr2oG" +
                "4RmTmdVpxrnHEUAK/Ku2YsmADKyy1kPGGp16ZxPv78+O6P39za" +
                "r399Q795W/v9P1UsX7+5vX4/3dWli7ebp5mnoa82vT07zu/ZQt" +
                "QMIvLbKRXbJpVl/TnzQD2zlG49Oace5xBJDAB21dsWRABlZZ6y" +
                "FjjU5+ZiqveHs/q/bVfup6vd7TemP01/NVOUC7+X/4VGE+v39v" +
                "RP+1z97Az0cj1W6ea56jnka20br3C7YACb+0yEZ2yQbWoqbXNA" +
                "M8PMos0ppx7nEEkMAHbV2xZEAGVlnrIWONrjs+/RH6w+g+1VT9" +
                "uuLu1pTGtf4ywufuU/WZjVK70ciWZ8upp5FttG79zlqA1nEYCS" +
                "+b5IBuHM36UtFqag6Nkn4bi1qpppgb+0JmGFdrdQx6RbaCehpz" +
                "3wpe95y1AK3jMBJeNslR7otENOtLRaupOTRK+m0s2xqN7rdsxT" +
                "p75q+q1upo9IDz/SfR2bSjfl1xDu7QuPaxEZ7vO+ozG6W236fv" +
                "Zu9STyPbaN16xVqAxkrHywYFYGBFlPSk/HKd4tAIqcYeZBJqSn" +
                "FjXzB7XK3eGKXQl7JL1NOY+y7R1lzQXJBdmnkGFkLYlY6XjdnA" +
                "WrwbLmAOGa3256W0JpDdPdqm/TYWmVhtxk6fkPvA1mZzAKdBz2" +
                "Vz1NOY++Z43XvNWoDGSsfLxrHgKPbFU8who9X+nEtrSqS2ab+N" +
                "RSbdvWlu7Atmj6vVG6Mkuvgc5D8ptHFX6fnSXn6WIG97P49D3P" +
                "nffy3/h1ipd2YYFOvJVZV+pztMfp1fp/1staM5Gv5DreAqf0HR" +
                "+U0aCezQ/6dVRHey3p+21LNyDWyriu/uG46/3l/lbb7TfKd9KP" +
                "Q0+j1erMI6zKknZOjhlxY5UmyYMRsr0YwYJQONFBO8NIMfK86T" +
                "OaCnGVEZaTMjZSSjNAPFEkp6pR+W8qg9hMhGY+ICxq0HJi7w9d" +
                "LEhc7RRmNmCfvzq5ADcqXjU7a0T1q739YxwZuKstwzS+tz2HpA" +
                "WlMzPZ8+Yfk0Z13dBnnZ7+HJ9uTE5fZkWIWeZsEz80zo879Cbg" +
                "9+RpCV/RQJTjmSAvkDb3sysZ8us5cyYk05Mm7icve7zIesdF7I" +
                "iHIkP6nwHLzTJ8BHHuwJxtKclVAbZvnZMI/uqYYxrMp7o/PYIu" +
                "+ZBr++d8x+igSnHOVd28Cbun/LaqSbun8NHOeMHOQvMvn+EutQ" +
                "juQnFZ5X3fkOHuwJxupfr6I2zPL/rW83GuH3hmEMK/dzv14YZm" +
                "wpnuGF3xs+5/1r3Lhf3VFY17Hfe+6WnHKUvzcMvC7xbRhWI123" +
                "K496sojeq3E5Iv+9Ye4d84z57w39fAX2p7cWvzfM8fnvDUkl/N" +
                "4w928Rzyjz3xuWtTzkW/l7w9zyD/l7Q1kbZu2d7Z3Z2dDT6H3F" +
                "KqytJeD1SloIDVuudJaQpMQ2cBCaxuwse62fMaxDWG2TEezjjC" +
                "iGVXQU9KFGKOnVCsgDaJ771Tk6irNz4SimnmbZOfbjfA9+fb6z" +
                "nyLBKUd5vgfe1PnOaqRbfb4HL+eMHFLnO+tQjuQnFZ5Xne/Bgz" +
                "3Bcfp8R22ZuvOZ9Yvzve9eCavyfO8HD/nL832799vzvc884nzv" +
                "61Gd7543eb732ZtnVH2+94uc5fneT57v/fJ875fne1+c7/3K87" +
                "3vz/e+Ot/7+vfFsjbMWvNb8yfeCz2N/r2qWIU1LIQMPfzSIkeK" +
                "DTNmYyWaEa9koJFigpdm8GPFeTICepoRlZE28DpLqY9YQkkvK8" +
                "hMAmfx/v5eqbqotSi6g53bQmu20ROO0XLFfholCmzmnbzNHoq0" +
                "GPilFm9AABXWyNLqSvY4y1hf8jMmttgVWaY3RN8P+X35H+W31A" +
                "Y8v/u4b3cl7J/xLfru0vS66/TUcPXVfT+k+/0K/NrIcs8V5fMF" +
                "8VliMTX+Plj5V4q+p0VIYCWqub3mLm8yoso/c7O1REf4dhvFNb" +
                "CtKp61B/Fb/8yHq/MdsKfLI7Lzh2Hww3x/3nz36MC1H59X+33v" +
                "7g9G+rz4fPN8dh6jf6863yy27HyYk4UQoYc/rNiCkVizYpYVSI" +
                "pvFgrgYGbSYW9WWqHSFHkRVkZrRlTG2sBnJgr6iCVUptSzKJNM" +
                "ocnb+B9dxSi9");
            
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
            final int compressedBytes = 2489;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW1+IHlcVHwttxBdtaBFEaIiKQkqk3RA21WW/75tv3FRJu5" +
                "q0GqqIPviiUIjWDfig+WbSflnbVBHUCmqsqX8Q+9qkoo22aUWJ" +
                "rhXyIvsksVssIvWltQ917pw5c8659547d2e/yar34/4753fO79" +
                "yZe+/82dkkSZL8p4lIJ74AdX4pHZoaSkyTtcRKqJe4Gj1feXoU" +
                "+/lPJs/7kT6PIUR+Nmwx+UNX34iazMXY5Y/lP5KS4fHsj+Y3PI" +
                "69JDF5eBwl0IcMaOiZEuwADzLyArZoCXbgl5A8DtQiB7JQjTjg" +
                "JBSOgcdFY8EYQWbrMaE3jJ+OBNoBBzLhuNFnc+QXeJ0u5NdgHy" +
                "XifDzunMsFWQv0v7xnf0GXGla/nuPShZNLYY8nMy71tbQ4UJO/" +
                "HGbw+2zOyGnIWFZn7GHIJBt+RiKg70uEMSiJ4zrNug1j4uIojB" +
                "8j1CJrRnK6C79t3RZl/kvaP8vjXs2B/G8C8XvnPC1Rnf9aYM/X" +
                "++cTjeRX1f65pLD/QnqsZM+U+Xcup7V/cosLfP/Mz9X1U1X5nM" +
                "2dP+mJ41mDsvZPT8z502W+mP/Wlg9ehQxp/DW3JZGEjU3S+/Tz" +
                "ul6XyGT8cAyOAWWa/fRTcf7D+s0cAZqf+Z+c+X4o3FdWzyHCTS" +
                "ZJMno22XLK/6xxhSObBXfLmc60vq0po7wj3FfGeIfEuX79si4J" +
                "mbTIZsWjpcmedH7y9sk7yx2i2uvSeZyf6TxKOL643tnLSv3kZo" +
                "krdpayfbZtxfcen5R4DKtfz3FlzDe5toLnFi71tWR78i6XI8zg" +
                "9znZU+bqeLrrffTlqDPyDjiejnwf3s/bx3Mm8+CmFv0tynpXxi" +
                "SPZyWZ21Q8t9U76+XhyuDy4HK5QlaqdbJi2tBCiVhPK84KWzFe" +
                "pMYnIz5XSp6N1q/nOIpSi0qy+1qy7fqLGberC83PU3/pZ35OT8" +
                "7gevRSt/k5Lfqcn9ml7NLgRVNCXZ6numf6JAGkKUnPJbwGW9NC" +
                "b8gELfDLPUANNkYLLdJTD+NEBPFJjzQy4Ca8jJLzky2guBYZeC" +
                "TGZz23XyTL4Px8qZ/5mX1j++anxj2z/fOcyYNzbCdp2qf+EXWv" +
                "fo7b+PzY0uy7Wz+efu/teo27zV/8/ZI+P1ff0NP8/M32Xd817q" +
                "3Oz+a8nDfZlFxSH89r4j345X5p9swM5uf5bnqNu83fTObnjVud" +
                "nx6pmZ8Xt3F+Xoydnx3P83Mmm5JL6mezp+M9xMo1dJe4u+i1Mf" +
                "kj7ZLSvbxO906OYR8lPrRmr8tCPjg/8obiTfda75Mdi+p98l5X" +
                "74tS44gbt9/P8EGTh/dCq14bpyFzmdtTnmIe7KZDRBvGxMVRED" +
                "+zvzfMHfav8dvWNqp4W3D/fGtPz0er27d/Tk/1e/9Z7hPrvB6s" +
                "4/EcrKNE7Crrzj6z7mp8Mnz/iX79fozWr+e4wbpc766FWe8k9b" +
                "V8VpIjbtwOcoPXg43xIvZR4kNr9rqMdD4p8Wt6jqMoNR6jJ6mv" +
                "5Y+Oc8SN29UNXoFsr/f0dsdHhSQsR7loaVfPzzdrfqVHKXGuBb" +
                "dPPsQxxbU4BpRp9sjd5t/Wy5jD1tXO+hBkLKv9cxfXVvVHJML0" +
                "8w3NH6HAzqfTomnHSBTGjxFKRpc77F/jt611Lyfe2JzrPc2aeU" +
                "rB7hZvJjY2ey0pbm7HnPhYBOZNrH19mdn18yvK9Wpa/4X3xP4W" +
                "3x8Maj+r3N0eGB9IbzUl1OW8rnumTxJAmpL0XMJrsDUt9IZM0O" +
                "IcZGGk4MX8UAp66mGciCA+6ZFGBtyEl1FyfrIFFNciA4/E+KTx" +
                "1KwHxwdHG6aEOkmwZ/qmXXwO5EZiStJzCa/B1rTQWzWmg9gCv9" +
                "wD1GBjtNAiPfUwztHG9Bi3tj3SyIAbPUJE3ArbkzNkCyiuRQYe" +
                "yahZl4hsdoW3QK6vGH9t1mV59zi6JPaPCknY2CQtpE9X75fIZH" +
                "xwDI4BZZo9ctt6O6Ywv6bNdmW7oMx2cRn0V3eDlCSEpla+wTWA" +
                "55n7qBke4aykcWOwOX1xci82G/dbSyxuGQc/MvZoJaOMmLg8e2" +
                "1zvzS+4HkOWNvUU4P7fPT8bN7jFAdamJXvFacPR8c+1y2y0ZXh" +
                "jnIG7xhdgR6u99GV4v1l3DeAvJ7nOwza4Lm9z6esucXoCreYPo" +
                "B65Ae95HB9FzdyDI6BZORRxsgj4nFMzlgreoccl+xp3uu7039D" +
                "lvMzvS69zrnvrpCEjX67Jiz8ftskYa84BpRp9sjd5j+s17Sjna" +
                "OdUEKNMvNLF9NFKQGE3ZP2PBMDYaoxLaIPbm3H5ePkSCmTetuW" +
                "IrG5OVbG7Y5W/hDF0d6dqfnCp/hAMvNULJVr/PtJ76kYK/vn9/" +
                "rlDb2vG97Wz/u6mcTd8X2dNqbZva9z9oBv8xb1qmjusaK7p80H" +
                "oCSOayVyE1dRxQ6Z4iLbehzWqvhkcbj4aPFhITvaxPa+zn7vsl" +
                "bZtxpNOc7iEzOI/EiLXnn+jxtTcfcMd55PN0fhO040R8N9ZQxH" +
                "43CzSMikMU4fSa5ikt/Pr7435n6ev1+K+35+pHwngd/PC5n3+/" +
                "lSdlYdwwV+P29/P29z+7+fj9s/te/nrSN2bXOv9lXn7C+H+8qc" +
                "WZa44es9zs/lcGR9cis788fdVhPNneG+MoY7Cef63GqU/oi0yG" +
                "bFv4kr/7GG+91OtIfDfWWMh+Nwm4ryfo0rHJk7pr4T7Z/FF51o" +
                "j4T7yhiPEM78f0evx/NIOLLivqt9PIsvNdfCM8l/bTq51KJX/o" +
                "+j2Ld9z0er8/08H01/sH3PRw/0+j1D+kL6Aq+xDf3pWdQiAlu8" +
                "58/cG3klKUmkxo2Cc9px2l5IT7ZUmrpYlFbSA+fnWR4HN2J71N" +
                "k4G1MbclmKPQrlgEV8NpYILRkrzhHCki8fi+sDo3LjkmWlv5/H" +
                "IqO3x8Kz5GmLvty/vw4Zy2q9D7hWYmP8ddEhog1j9k+Oovihp+" +
                "2fiAn71/htaxs1Xh4vQwk1yqA/ugElhCQ9l/AMcu6NvHJe8kAa" +
                "F6FxurG7FoQkPIzJ9S0t7PFwPUn4ceSxW+vo78218KF+roHDl/" +
                "u//qvfM/z8KrxD2A/Zvr5PH/Mjh/u7MGxOH8PBMTgGlGn2xbBr" +
                "RHHa4P9vRt37/o/9/+Z9MfdLxTe7vk8OHs/d/4fHc3ef95+QBq" +
                "9Bdtb7z2wrQBI2NrVZuPoYDo7BMaBMsy8OdY0oRputZWtQQo0y" +
                "6K+OUEJI0nMJzyDn3shrvfv8U3ogTbbGIwtxurG7FoQk/PRxe8" +
                "TcA0VgM0s+iliiYb1r70NGc1teld4n1uLHM3jL0PJ3yuKHynqf" +
                "i9o/H+34fch/AEgc6+8=");
            
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
            final int compressedBytes = 2189;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW12M1FQUrrxICIlRdEH+dAUWcTWRwBojLzudaZZETeQBlv" +
                "AkCW+GxKjhBZQw27KzMyI8+SDhzWCCIIQYibxB/HkwIC7yJyoG" +
                "UHbV9RdjgiTazunpOefe2063O50dt5N723vOd8733dt7O21n17" +
                "LK3ZZVnl9ebEXbjlfwqFq0UmzlRX551GBf6ZcnNevDllW4ZU14" +
                "Kz/QwL/cbI/jLi/BI/dQaFkxLj1PBbU9254NNezRBu3qy6qF0N" +
                "QaGJFtKsRAHPU+3cYcnJ1v0s/bhJQ26VdjSYnKzbGku/CPqbfy" +
                "AzlRCx2rm3s8mp9rtbO7IbkdMyc2SFzhz7RnvbpuvPMWmeKUVQ" +
                "5bOW7usF+uhMejfvnS/RHXu/tzaWV9/1eEPuPP6zNR62y4Px/u" +
                "f/LL7375PGxfdv+E9e6ejmIulIfdrwt/G5R8637nXnevumPuOf" +
                "cSs//il5sCedH93rIG3ha2r9jxN+4Pvs7TSv6Rev2byu1+EbuC" +
                "U61391f3D2o5Y84Y1LBHG7Srr6KFkOTnFl7qnw6ejbKGrB0yA/" +
                "OMcWVJnLp2PYKQDN+h9phniLAdKrPkI8WkW1qM30evGc7aGdUy" +
                "MGI4a+GVQv0+8maW/RVR+WDiK0vOT4PO02b7zrOpZt26tPPTm+" +
                "7NSEaw8dyetbfOrNh+Did58x/PdNzNUGgYzx1p5qdRz8yk+Yne" +
                "SRnPmWn1Z5ufSfef9vJ87j+bsWW9/4zrE91/ju/7SN5/svuM3V" +
                "Cwrs/Pndxb36+XCGgb71t2s+P1Esd9cdGNMRKF+lFhnLKoJ7uz" +
                "8KvRJpQXzi3vEb/c73Xi/PQWVY6lWe/ePHbcJTzLYH560Z2uN7" +
                "887D0Ys3K6w/3c8a93j81W7yHTevcW1Osl7vOKfc7E7pe8xd7S" +
                "ZIRd1Y+i87MmuW2OLKyRuMpn+d1RI1OcssH7rNy3Qg+U8Ly8FI" +
                "3KoF/e1ZGEjT0ngzpDIwXjiwh0cQz2AW1x8dgf1S/72Yi/8Qiw" +
                "eR6Np+dfu+0DIo/y/FeIeR4sX5GowrpmzwGpS1cUxxgXF2dvwj" +
                "No9PxuP6apXZvcjs7ESomKw+Ww1tYmK9P7lPdme/pRpLZfb5ue" +
                "j2RkoV/G2ccnrDE2AzKpSsfHXRlp4nju0o+y52i3zetp+fx8XT" +
                "/KniO3q9JzGcfziTxVlXpKPVDDHm3Qru5CCyHJzy28BB97I89G" +
                "WTkvZSCPjojj1LXrEYQkvL1R7THPQApUZsnH+8LRTqfTCXWwD5" +
                "9dO7FdfQOsZCE0j+MewNsv6jbJwaMJo2pQOU06eRaVjecFCypT" +
                "c5NSrpD3VjJKxcRleBdwKHrefDPrvO99r12vn61X5pyIxvOtzK" +
                "qvy3Z1b9uM5/VWMw4si0bhYGbVV9t2frZIWWEPFKzrM3UPFG7T" +
                "W/H5svgQ0Qjj7JEo0p/MgJ7k/HH8arSK8lbT+0/vWfX9Z/Wc4X" +
                "7jmTTvP+3Nbfv+c3Oa95/e0xN9/xmuhqP+ep8WjecNM0K5Poyk" +
                "QTX9Kj+acb0fbfUVht6HVEczX6UOWG26tV4ZW+83U31/mebn4b" +
                "Ydz8OTOJ63DbM31e/F9gsh2vh7XFPuQjL+HofKUqzTJv1e7EW/" +
                "EtemZe1t5WS7zs/KidzvN++Ku37W7sy8qg617XrPVVmpu9QNNe" +
                "wrH2MraNeml7oBRcjKR+hHi1qCT+9+aGEccRAvZSCPjuBZuUXX" +
                "rkcQkvC9+2WUzEAKVGbJx/vC0cnz09thTbltYEZLry6f+ozR35" +
                "rUDH91Uvkk1aq61LbrPVdlpf5SP9SwRxu0awvQQkjycwsvwaf3" +
                "Gs9GWTkvZSCPjojj1LXrEYQkfO81tcc8AylQmSUf7wtHJ98v1b" +
                "oyz4J38p5ntaXtqKzUV+qDGvZoC9s2WghJfrQMjJAHvb1HeDbK" +
                "ynkpA3l0hJnTpF2PICThe4+oPeYZSIHKLPl4XwR6VWmVvS+oYe" +
                "/f74atoB0cQw3IoCY/t/A9xAZHmI2y1u+o9/Ec5IGYwAtHxEAt" +
                "jMAcxCc1Uc/qvV2FGUEJj5IZIBZQ3Mv9ZImeEfZRZOLf12013O" +
                "3vTfV+aWvbvl/amub9UvrnI/l+qbSptAlq2KMN2vY2tBCS/NzC" +
                "S/Cxt/FslDVaHyIDeXREHKeuXY8gJOGhT3puGaH2h/vJwtb7Jq" +
                "497vuocmHq3X+62/PMXrxVvAU17NEG7cpltBCS/NzCC9h5NsrK" +
                "eSkDeXREHKeuXY8gJOHd7WqPeQZSoDJLPt4XjjaO8b/60Xjf1z" +
                "U4i2smb36m465k/g++xO+jeaky/L/+3ntemu8jb39zfu+Q4+kZ" +
                "7n3du1Ndpe4JZ8MW4xzZMonzc0taVLNUlq9F45nTbwND907eeA" +
                "7eaDUjG8+DOY1nx+SN51Cuf+/trHZW28NBHezr15ewFbSDY6jh" +
                "E+BlC+PJA1lVG0YBA3o5O2DASxyAQyzplBiuChWQMtABNmSRyi" +
                "lDdJUdll7KIHVgj1Bng/l5JKc5Mnuqrnenz+mD2unjNmjbC8FK" +
                "FkLzOO4BvMkmOXg0j5MaVE6TTp5FZeN5eZ/03KSUK+S9lYxSMa" +
                "KLp4qnoIZ9/bvtFLaHZqGFkOTnFl7AzrNR1uj7U2Qgj46I49S1" +
                "6xGEJLx7TO0xz0AKVGbJx/si0OeL57V7hbotKENzwIttrE0tju" +
                "E5TQzkgSgzBq2U1YQkCylQeXmMe1L2IZ5fsiRbJHfs9fP9qff8" +
                "Xsn17z+LF4sXoYY92qBt22ghJPm5hRew82yUlfNSBvLoiDhOXb" +
                "seQUjCQ5/03DJC7Q/3k4WPI0YmPW/WHm/X583CjazPm0Nz83z/" +
                "adq86L9gayvaddUWRlsf2YTnow9zuku7Y/LORL7cTpfTBbXTxW" +
                "3QHloIVrIQmsdxD+BNNsnBo3mc1KBymnTyLCobzxv2aYGunDLw" +
                "kVF7Kxml4ojrP2kJFD8=");
            
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
            final int compressedBytes = 2216;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uMFFUU7fgBYqIGE4IRDQwzBqOYjJmPKCY2qS4WunHpys" +
                "QFK91gXAv9GWJbg1t2xqhRVq4UwsKQGIUYxBg1Gr+RCDjjMMwg" +
                "AoL4qerbt869t15V10x3TSVW5b1X775zzznvTVV1VzX4W/wtlY" +
                "q/xe+0tFEv6reHKYoI0DJPjhDeFdMaMlvmaQ9W0+VTslg1ydud" +
                "0+akczBUKtqhnK1W1I6hFW3VjdGOHpUQ8YiNUQGCW1nLuOxbja" +
                "yNsVoljZFdwZcdQYzmZPllzDVjqShn4p5HdSja0aMSau+2MSpA" +
                "cCtrGc/SyFzPIc3owoORXcGXHUGM5mT5Zcw1Y6koZ9JrHtHW+I" +
                "WPpj6oFLLVZiqlbcVq+8f941RTyzHqt0d0RKJ1HlrCyyI5oJvM" +
                "Zn2tKLGWw+VKq1kHlUrrhJ2xds/8VlnrWccx+ph/jGpqO2PHuB" +
                "+M64hE6zy0hJdFcnTPkauubNbXimBBxGI0QqpZB0lt6575rbLW" +
                "s45lZsb1/mFBV8XN5V3vK68t1vOjguZ0Q4nrWah2daI6QXXUIk" +
                "b94HGKIgK0zJMjhLexoCo1an/rbJmnPVhNl0/JAibkoo5aq619" +
                "yJWxs9WK2nGMnqxOUh213bFJ2psng2coypHoSPdklNlkX8agUT" +
                "ujs2We9mA1XT4li1WTvBSx2tqHWM/EbLWidsxo74B3gGpqozHq" +
                "RcfBbo4AiXEZkYXikk2zhnOa1wwc5xyJT+6aWbNol0ACH2nrGU" +
                "sGOLCz1XpwrNGuzXuLj4KX0sewNWfyoNR3wLP570i9uJaatxTt" +
                "ZX36PBCWuxsjiNRfjNfzlVwMw2HZ6oiPh2VbInpfOKc/BuC7xx" +
                "Nr46GU9UzRbtybiIwtyc+jOT7fPynoGeW3Ep+PVlxbrOeJgua0" +
                "2D/H1KflaWfcZQ56B6mmlmPUD+ocARLjHGnOYARckk2zhnO6oB" +
                "k4zjkSn9w1s2bRLoEEPtLWM5YMcGBnq/XgWKNdm7h/tvP8TZpL" +
                "fsNQ+7fE671g7ejzKHU9p/tm3+ac0/X+fbde6zH+Zsp6Xs/zed" +
                "R6Y6mfR132TbVN7lhUgv00yn2uXT1g0KYqnOcRynJhkMk172kz" +
                "kA60rvJz3rpM09cq2RGtba7f+Ak3eDXls+C7Pq+5czk+b77Ngf" +
                "lSHH8dlu/za09904P7q2XNbH1tPR/pWFSCtynKfaCSPWAkl1QQ" +
                "sUs8QlkuF8jkmvesGUi/ckZS22q5WJPrlKZqVmC4Nkw1tZ2xYe" +
                "4H73AESIzLiCwUl2xg7apf0wxiZDh2aBSsZtJ7MgNIgb9mZywZ" +
                "4MAqaz041uge1/u7xVzv/qryrnfWLuh6H6mNUE0tx6gfvMcRID" +
                "EuI7JQXLKFTO8jHsZmNYMYGZHOsjST3pMZQAr8rJ2xZIADq6z1" +
                "4Fij/Tl/rhtZF/8FO7GoBEdolPtcu3rAoAWbOUdW8whl+XN7n7" +
                "IukMk174Zrzroglr0bgVR+VluXLtaEYzNeW6cj0M58H5Lr/fzS" +
                "34f4N+W9foKPB/0+JE17kO9DAvVLUv1IHD9e0DPKQonPR4Vqe0" +
                "e9o1RTyzHqBzMcARLjHGnOYARckk2zhnO6qBk4zjka79J0eU9m" +
                "AAl8pK1nLBngwCprPTjW6B7vl04XdI78XuL5WbB25vP7z/2+D0" +
                "l5fr9S4vP7lSKf371T3imqqeUY9YMzHAES4xwJr/d4BFySDazd" +
                "Oc1pBoxwK124NF3ekxlAAh9p6xlLBjiwyloPjjW6x/Ve0L/18W" +
                "8s73ovVts/5B+imlqOUT84qyMSjaPmjOwTXhbJ0T0/T2tVmQdf" +
                "0pV2YDEaIdWsg6S29gEPVlnrWccyM+P8LOi3lto/JX4eFartH/" +
                "YPU00tx6gfzOuIROMoPD9Fn/CySI7unC5rVZkHX9KVdmAxGiHV" +
                "rIOktvYBD1ZZ61nHjK7OV+eppjYao150HJy3EaDRa87oPgrngq" +
                "O7nr8yh1SXmx6XfSB1TI/bXDix2hKrfSdnq3dGSbTz+0/8q3uw" +
                "WPnfbVPn8qCa+weoGD+RBVcKuof9VeL9M5f21IUBrmf8Lyhqub" +
                "7RNkv519tTl5aZdzEX6vLy2KuL1UWqqeUY9adX2QjQ6Ol8WaAA" +
                "TOfuHXPIbOvLpSmROqbHbS6c+CncWAtmT85W74xS6IXqAtXUds" +
                "YWuD+92kaARk/ny8K5IdMaxnTO+z+ZQ2ar9Vxwa0qkjulxmwsn" +
                "Vltite/kbPXOKInO/r1j6mox12r7/vLuny+3V0anviZ+FxC/If" +
                "EbKdjN/Wntuy2Hn6dzYG4Rx2vDsj7HN+7unOqTPbifyBx9zh3f" +
                "MbZjjGpqOUb99lYbARo9nS8LFIBBFFlyxDUu+y4OjZBqPAInra" +
                "tubqwFsydnq3dGSXT28+a+Owr6zvJDid+XCtX2Zr1ZqqnlGPXb" +
                "D3IESIzLiCwUl2xglbpgwEgSkaaZ9J7MABL41h47Y8kAB1ZZ68" +
                "m5SHR8V9nl74pqOqbijXqj6s7TiXujEotWH3MmGKIsHo84et7n" +
                "dklGFx585MflS9c0K5mr3eseuZTrAp3e/sWnRfc3W2/cG3ec0e" +
                "O5zvvxLHQ+joKuyPHBI3v8AoL7550F3cN+KvH+ueLa++6KjzYU" +
                "NKcfS1zPFdH2d/o7cUzFm/AmNCYq3gRhGY88ecyZYIiyeJw5sh" +
                "1JRhcaHOTH5UvXNCvpRbvXvcglVkPr9HKvrvcXurMe8hz/W94b" +
                "ynXvGUpHu3lX7P45NHhkjyeD+Pfi6Vv7vhcn/73NF+H3sIfz5k" +
                "+nPkk1e/w/r8ZnKc+bz+b2Pjbw9bzHofJ5/+tZ29O/y+WuZ37t" +
                "Qa0nX+/Nk9725GhrbR6OVvfJysWQFl2h63374JH5zs/myelcd5" +
                "ClvU9u3B6eI/UB/NWfXObne32l/4L1+BfV9mMFfSMbLe/8bO0p" +
                "az29DV74/bP1vLkKcn0nZZQLTbz9bu3JZV7vKdqN1/MiM7f/AL" +
                "DCAjM=");
            
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
            final int compressedBytes = 1530;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWs2O3EQQ9oEDYg48AAgOWRAIhISCFIgE8gwzCj8C5QIHOI" +
                "SfwCFZSNjdsAkhaOPJTDByWBARxwgJTjxAkGAJgQUEB1YIhPaQ" +
                "F1gJXgGEa9t2VdvdM+Xa7unVetTt8Vefq75q9489nl6n14miXq" +
                "dX7KOoPOp1hr9l+3REMepHwy39GEt5LvpQWP/d0geNTjfdTo+R" +
                "qWO6vX4uKqnHplxddzNb/VOyKNu0nf9P7fP2nIsY23Ararn134" +
                "qCbX5jx4vxoqrVvsTgk7fn3TqiGPUj/XxaMAJytnNajBepF7RQ" +
                "XaaYlBkbvNSjlRZUUo9NubruZrb6p2RRttoGd8EHj1SJouzeOq" +
                "YKMmAP/VMdoQ/9ez1G/2TdWt9KLkYxc5BVHpUIPZ9i/ZMm/xQz" +
                "ZUwj0kym5bE93peq8X6f+xGRJHlOKzv3M/xTON5XvM4mq/1VVa" +
                "t9icEnb88HSgSZaKcILQqn3tBrEXVJ90Asq1TZpJhN7c0zkEn4" +
                "S/WMqQdUUI+sx0PFOtvYgxaq/rnf01V8L+B65Dl2ko/p5PZkzj" +
                "jeD7A87MvL/Qb8obw83EDvyXM660D3nVPsD1ra0xI7acyESave" +
                "lBycNn/m69FB9/dLya15TqcctOfTwv55atYjgvTPw57a80zA9j" +
                "wTqj3z/nnElc/sJW19PxFw/jwRsH++7GG+hv55LmD/PBewPRc9" +
                "jffTAdvz9Kzb80K1AmbLnp7flwOO9+WA/dPwLJE862C8v+O/f6" +
                "5Y7qdcxBa353lP82cScLwnAdvzoiGP39uMd8P9/B95Tm87eH7/" +
                "Ykp7b1jakx072e+8PVNP69FCwP65MOv+mVQRe4eiPbfNPidsz/" +
                "Sxvdeeo0f8+o+/hgI1RYpr+QzfAxe3sSW6JXZbTmalbp8308f9" +
                "rO9O/Ajnz1Hs9e72Uv+SqtW+xNRx9mGJIBPtJTLcQgv6ot7Qa3" +
                "GdntA9oKXcUxWmmCbtzTOQifz0UD1j6gEV1CPr8VCxzp7ye8gH" +
                "Htb3/P4vfcrB/ZLwfUf6ZLjfl7p/+blfCjneeTl5as91Z6vq61" +
                "bLUWE/Oyxsz3W/rWd/35HHvs7y0Pp9x/h5B7qF7ztsOenvOy58" +
                "6u59x7jqMVnm5/elkFt35I7F28bV2Eyf24P388fCzZ/Zqqc+cl" +
                "PA/uk59qT5M/vIz/yZvhhu/kxfmPn8+UbVnh97esb9138/HM+H" +
                "iR3/AgVqihTPugf4Hri4jS3RLbHbcjIrba9n0nhn9vT2430+3H" +
                "h//wpnvG8jovEeb0KBumrp6nvvUdaV2aTnTMNNTFH/3JTZbTnt" +
                "VKliDzYGG6pW+xJTx9nlwUbvhsKRUdpLZLiFFvRFvaHXIqcbug" +
                "e0DKq3FNTejFn6oNqbZ6AGVALn6Rmr7+gPlVC/eluhYp09ebyn" +
                "b/oZ707uS4TjfTT2O94nre/MX3p25fORbX1Pj4e7//S2Hjl4Jz" +
                "b8R9Y/L/7teT26CgXqamatvndZLas8mHE+u/V6dFVmt+XkQtO2" +
                "n6+gQE2RYmyc5Xsw43y2RLfEProi87c7xrsBhfHu4D9E0vWIP9" +
                "6dzeTVetT18P+60OsRL6cdjJt1KPG6jrT3wMVtbIluib17lMMH" +
                "lkxl/CMUqCnS3gMXt7EluiX27qscPrBkKuNvoUBNkfYeuLiNLd" +
                "EtsXdf4fCBJVMZ/wwFaoq098DFbWyJbok9/YTDTy9LVcbXoUBN" +
                "kfYeuLiNLdEtsXePcPjAkqkM8XwU8vm9+xrnfglYsv9/xtegQE" +
                "2Rwusc38NkfHyLjT2+Wdg/r8nstpya/GkRdk//7N4Wrn+mn/l5" +
                "fq+uwjdQoKZIcS3v4Hvg4ja2RLfEbsvJrFSg6wcoUFOkuJaf8z" +
                "1wcRtboltiH3e4fJnK+HsoUFOkvQcubmNLdEvs6Zdcvkxl/B0U" +
                "qCmitv4a3wMXt7EluiV2W05mpQJda1DiNR1R2+AY38NkHD3Z2B" +
                "LdErstpyZfpjL+CQrUFCliz/M9TMbRk40t0S2x23Jq8mUq41+h" +
                "QE2RIvZxvofJOHqysSW6JXZbTk1+O5UF+39kjLIv");
            
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
            final int compressedBytes = 2085;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHFUQHoIHo2JYTYyHJAZiQFFYRCGyp2zv9GF3cxF/gw" +
                "cFr5uDux5kcYX0TDrb4EkSJa5IItH4QzxoonjwIAhCFkyMHvUg" +
                "usmqiKKi5CLOm5qar+q91zPdvd3psN28v6+qvqpX8/p19+xsox" +
                "Hd02hE26I7G/3j4HONXEe0q1Pu9eAPdMqDDnpXozG+rbHmI7pj" +
                "iPw+P774Ror+bge5P1c8Y7220nx60E4+m1/Vl880324+ix17Pz" +
                "LF1BKhIzyQncGPu0xp2kXiLiJPm1MZMfmP+EA+/dZqXg/N6xqV" +
                "H/FMfb7T8vnS0WryWcaRvFwsn+NPZ2HPpuVd58ummFoi+Rk6Mc" +
                "z78Sxo3mN8fhhHmtyOMl0/X5SsvfecKabuS87lzOc5v40fT9PO" +
                "dxy+bRhHmjy5kFU/X5SsXcfzUnK6vvv74cvr8HkprvF5Ka74ee" +
                "mMKaaWSH4GP55du0jcReTJ98X4Msd11hRTSyQ/gx/Prl0k7iLy" +
                "8IUs+mlaRQ5c7+Oj1+7zUuEng9Fq+Qftn8kv1dyP4sdqfH8/lm" +
                "X/bL9ybd6PUr4POVlfPtN8l3N/DxaCBaqpZYzGLgJtjFqreowC" +
                "D/DRvcdeYA7pXR5aLsfQ1JiW27aIxPYtdXXc7mz1ifxAO0zChG" +
                "pqu3txwmMg0ISckdYqJOCSbGDtrZF3NAMk3MoofD7BrFm0hYyf" +
                "EeNbz1gyIALbs/aHiLX24PtRZfeE92q8H1XqO3497/4ZL5VwPz" +
                "pW3/4ZPp9l/zRaRfbPcDlcpppaxmjsItB2EbsPW3D0nhvOagZI" +
                "0A7z6WJuH7aIJDmTHj24WaK59KnjRd+53v8buD7fLOGae7fG67" +
                "1i37U8L50qYad6q+Dz0qkqn5eaJ5onghVTU9u56/dGZmzk7c2E" +
                "G8TUkEvEtFxgy2zsiXrBiuRIPmYJ2Rgp9eABI8TZ3sJSOQdtQZ" +
                "rEyjPAbBC5OaPjsGUb6ZlnIuPoP7mtSMure3+PNnU8v1oCz3Qx" +
                "uzJ857ySTjfW8RG/n0nrg4Kf1lhzjGpqGaMxEGhCLhFZbFvJ2v" +
                "N6RDMIyZiMbJBPN3bXQsbfR47YM5YMiMD2rP0hYkt7sjlJNbVd" +
                "2SSPgUATconIYttK1p73o5pBSCb7EVoebJ9u7K6FjL+PHLVnLB" +
                "kQge1Z+0PElvZ0c5pqaruyaTonRiZGmtPtGcKhwXKJyEK4ZAMr" +
                "eTe8kgGSZn9XtD3YPpNP7dhdC8SASIxvPWPqd+5HwsKej2QEIv" +
                "LJ2lPNKaqp7cqm6JzYOLGxOZV8Rjg0WC4RWQiXbGDt5XOjZoCE" +
                "W3hJ8+nG7lpAE/rGt54x9Tv5FBb2fCQjEJFPYRkuyjZcjPbz2C" +
                "BUizexRecNbtGV+DDIfCj8p8ml3vCoNOrrpffZR7Z5y16wO9g9" +
                "scHU1HY+v97IjE2/PQPE6OuRRMgeWHclbiBN8sQYOEib2okNLL" +
                "XlrMN+jFbyucakBetzRGTDXrQV60bH4Y20pFR7QBzQ5n6nXZJt" +
                "sMTrM1hiRH0z6Xwfou3TMch8mvCv5T57l9dvYXOnRemfZdZ5y1" +
                "5zT3NPcMXU1HZkvZEZm357hnCDmBpyiciWbE2P2dgT9YhXMlBL" +
                "NkZKPcgx4jiDK8mX0tpmxMzINzNSRNKK+539s29LWlLKHmQkhr" +
                "OXzyuwzPt+FF3I9RZjvb8fuim62PH/xdqfy1tD/mYSpfwuMbvv" +
                "bO/vh64/dIPYU3eFu4JLpjZt119vZMamnywDMfp6xPaQEKsP4x" +
                "7xAuGWUCOFD5KzDuIMLrV+0Ji00JFRHISxFx25OaPjbMtZkFIw" +
                "6Dh4Rhyn93N5JudKqeHvxcEn1Vom50v8xkLlM6jgbb4MzqIcaX" +
                "blzDNshS2qqWWMxi4CbYy0vSzwAJ3up3+ROaS1HZfPp9TUmJbb" +
                "tojk8BY/N3LB7O5s9Yn8QDva6lmftxZa1bf3e7cMXSMHS7yaNu" +
                "dcnwcr3YdGghGqqWWMxi4CbYy0vSzwAJ3u+vyWOaS1HZfPp9TU" +
                "mJbbtogk+cbPjVwwuztbfSI/0K5nfYav1bc+y/Tt+f5zX3Mf1d" +
                "QyRmMg0ISckdYqJOCSbGDtfVv7rGaAhFsZhc+nL3bXQsbPSPKd" +
                "PWPJgAhsz9ofItbawVwwRzW13bU7x2MXgTZG2l4WtgVH/1qdky" +
                "xS4pPLsY9Da0hvLEEkyY9+buSiuyP95JutPpEfMevZYJZqaruy" +
                "WR67CLQx0vaysC04+rmYlSxS4pPLsY9Da0hvLEEk4YKfG7no7g" +
                "oLvtnqE/nRM3Z2pNn1/Pej8MXytDz3953BTqqpZYzGLgJtjLS9" +
                "LPAAHaCwkhKfXI59HFpDemMJIgnn/dzIRTef877Z6hP50TNOX5" +
                "/B3etvfVY9p9y/Bzu/9t+HJL+VEHel/y8Tf1js9yHtp9oP25L2" +
                "/hJ2qS1pkvaTg6TZj/YjQ/6C/nXeyPJreaLq5LP9ePuhsvPZfn" +
                "RQPss4huWz/UTKN0e/Z2Iv+Av/cDudGithfW4fJKnWw2B5Nt9F" +
                "Iwx30KmxEma7Y5CkWg+D5dl8F42wsv1z68D9c2v113v+yPJrXb" +
                "39M7xxYD5vrG//zOa7jAjLfD+K5gd+C/N2fc+f1foORoNRqqll" +
                "jMYuAm2MtD2VcFMwKj1Ap/vpb2IOaW3H5fMpNTWm5bYtIrF9S1" +
                "3kgOK3Z6tP5Aczr2l9nqxxfV5132X8Pjm8OTXT0SBpjk8sLjuy" +
                "/Fqet4h/48vxinnfjP/uYz8XmJ31vhluo/fN+B/H468kXesRD/" +
                "krdfxnSqaG+o7/IK34rwJh/Q98AFkd");
            
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
            final int compressedBytes = 1771;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHEUUHk+GgIQIgQ34g8SIGBEkij+nTfXMScTdzQZjQk" +
                "BDSCDKIqJslrk4M7s70oiQixcvQ9yDIoKHEAjkkA3mogch6iHx" +
                "FLzkrHdxut68/t6r6t6d7enu2kw3VdXv73s/Uz3dVbPbaPhH59" +
                "NGxUfz20awo2rf3SPD9kT32eL17B4athc1b/a7Ie+VYXvd036+" +
                "pLif3kb+cjY/iSxT/7CkVr8eco7uKJ438yTmjH9V3lEWZlGcKn" +
                "La+tjp/Ow92NHn2C1pfq4Xs1v/uMramdPmNPU0Mo9onwNtnyOv" +
                "Z78HgsSQfmEFCcbtfPo8/xq2iIQiy44e2CzRWPrU8eLa++Q/mf" +
                "xz6j+ylTTeF+551D9Q6fxcHp4D29txyGEqoQdpb0+rryjFGWie" +
                "xR+MNAdsY6/hYxkSM0ilQp4iCgsPY1lbpPqD1HZAo8pPypfTSB" +
                "H3QHnW/lJM1uZr/1htp9W+PJxLjxX6nC4Xl475rX2/4AzK8d0d" +
                "VHZHPNqY4sNcqBT9iDlCPY3MI9rnQBuUtpcNHqBjvz/3M4a0du" +
                "PK8ik1NU/LXVtE0j+QjY1aMLqfrT5RH52xN/O/SiO+VPhzurRr" +
                "52ctkbWiVoRrar4ON6nRivD+mWUlPbDcXJX+8vQlYnY8Usr42k" +
                "L3yWiuylgkrowpiVBm7PrJj76a9abl5643zbWJZ9m1ouvNPN96" +
                "vWk5hdabfj37e6uuZ/xMuPV73vtnOfU0i2aRehqZR7TPgTao3g" +
                "NNo8EDfNjrG4whvatZtJjtM0W74fIcuWMrcr2RjY1a2Cfmk1nZ" +
                "6hP10RnnP4+qWL9b7zcnvt9v1m851m7gfHOeehqZRzQ40IRccm" +
                "STtr1/QfFVoxGd1AiQ8CijyPbpx+5byPiZk/jWGUsE1o0PNed7" +
                "/0hc7Q8RI27bjjePU0+jlR1nGhxoQi45srm2EnX0nPxDI0DCo4" +
                "wi26cfu28h42dO4ltnLBEQgetZ+0PEjvZCc4F6Gq1sgWlwoBmt" +
                "sBw2uiVntEIUaQN1ND9XNAIkPMoooOPHKWP3LWT8zEl864wlAu" +
                "smWlJTymV9EO0o31vRLepptFi3mAZHaHZYDhvd7NkZUR0gpRYd" +
                "iQHv8OfoA9WLU8buW8j4U07HzVgipLodylTH4frBkWJtRpvU02" +
                "hlm0yDIzTbLIeNbvZsj6g2kARqWyMwn/1pfYHqxSlj9y1k/Cmn" +
                "7WYsEVLdNmWq43D9iHpuyth3sj+/un+c59zq4yM/mXtY0XK49e" +
                "Z4vhOtsqKMftqZzH9fykcgSbRYQ91yYoDvrfKMXyi8zgiw3oxf" +
                "mtb1Uf2/x9mZ8U7A+71i3yHmZ3Qi3PzM8435ufpjufshNewvXZ" +
                "94vXm98P7S9XHu9/7Mw1TP+LVw8/OLD+rer6vhfp8LeL/PjTU/" +
                "n9ud9czgJvPzjXD17F0cd34WPXrOXxfg7xn6rzYe2uPznHr39t" +
                "YdCepposIYP+zWOhfPaSz0GTNDPY3MI9peb0hOQkuKm0dvjOw3" +
                "BNJMyksxpLUbl0aVHCC7KK43lohcN3KwN1QNNjiHLA/SD8eL6/" +
                "z5OcEc33J+Nv8ONz+r9d080TxBPY3MIzq5NleIzxrmCstho1ty" +
                "shVpA9V+llckhpRwBDoK6EgOI2sUbYGMkGtipzPmiKVFQklNiS" +
                "jrg2hl7HnrzfhkY+qO+N1w602zx9df+2by989S4i66PtpT5ft8" +
                "82zzLPU0Mo9os5c50IRccmQjvkQDanp/KARIfI08n37svgU0oU" +
                "85+djaws1HysER9/tZGftk+0sFvrX/Cvg8qt13DfW8G7Ced0PV" +
                "MzocDb9TVj9y1r9jrctYK0ubcCd+rpwquP+Z49v/e9oiUZpZM0" +
                "s9jcxLzuhgdNDMxmfASa4SntR37aFlYzpIEugkPGBIazcujSo5" +
                "dt14X/O03LVFrq5v1u0OZA2QQ5YH6Ye1+Xqn+yHrv03+fG8uhX" +
                "u+5/l29pfefph+7zCnwn1/Vuu7v5DBm58Qc9vdTfNewHpW6rt1" +
                "tHWUehqZR7SZcznQBqXtZYMH6Nic5hhDWrtxZfmUmpqn5a4tIn" +
                "F9S10dt5+tPllLamfOr3Qdair6LdAE/H0zz3f8finzc6m1RD2N" +
                "zCP62C8up7UUX5AUN5dmLPawdp51wKVz7ZyWuHLpIQ9Da0AGW0" +
                "SS5JSFjVowuvas/QFTa2c+qdJfAI/9PH37IfXnhP3PY7cz7osP" +
                "i2Cunc+VnKu5nrdz7veLNdTzzhTOzzvh5me8NH31XLsYrp7TeM" +
                "z+V57Wbqln/Fm4elb7/+/b13P9ywreAX8toS6/l+u7uv/Xrn5+" +
                "mj8Dvs/X7rva/eTuvuH9XsL/+HbfKjivnwp7v5t7FcyRe+Ew8u" +
                "xKyvN/Rvwxmw==");
            
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
            final int compressedBytes = 1283;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71rFFEQvxSC2KignSj4AYISEC3UQi+b3co+oH+BhZXY2B" +
                "jdSKJbWIhEJWChHqiNjQoiKH4RbUSTWkQkxGtM5ylIcG9f3s3M" +
                "+9jbPd/u3K15Yfe9NzNvfjO/m/2629RqegtP1wpu0dkaW5vaWz" +
                "Yi8OkdqlWulZ9T8fXpH+fjs3zsvHxOfM+d0wlGPk9UsD59Rj79" +
                "6vEZhYzXo8UK1ucQY30O9TufPZw/1zDyuYaPz5GT1btfKj8n4D" +
                "O6Wj0+L22t3vmTtT43ubPKfbwPG67Nd3rxObnOqllbMp/DjOfP" +
                "HRWszx2MfG431GdjwOtzu+We+O7/ff6M7vXIZ92dlZG9PfG2Jd" +
                "wJkgtncnqIj6DQ8C1YeCDeDmrS3Y4+9W1d9PssTHkW+12qVbg/" +
                "VzyH/9Pr+1F3Vqv3SzFTR9xZrfIZn3fvZ7J6sMpnxvocdWeVrc" +
                "H1KDA864Yfc302+vVozk2UE13ub8IPZnmQ+fk93/WIsz6DB3z1" +
                "WTx2vYH7ekPyKef1hsk6TaL6VHVYGnyha9raeqN7vN2iolLTyD" +
                "42SdL06fGW8P38Y776nNrM97xZ2DE3zXi8T3PxObphdENcS0/c" +
                "I7T9crXysUuoz+uM9VkodvAseCb2opcyMQcJWGK92Ca+w1h6VR" +
                "EAIxnfoKigoet0TGypxk71NCMkuaFmjD1gfLxRHvSIcfba/eey" +
                "HHnnde3kbQfXhHN89WnKadCP9+hpla/v9QXc1xfCF3IuJSZr23" +
                "q7DHQmKeDb9NhOtUjzaBvZVpkx7HnjUTAejIu96KVMzHUJWMOM" +
                "rscbIIBNUp/PpQ+8mpyPxs2Y2JLKqF5dC5FMbTb7Bi6kdz1b+g" +
                "f80Iy9Ju69pqxPrykl5PzT1M5ITV1jkoHOJAV8mx7bqRZpHm0j" +
                "2yozhj1vMhqL/1rJPuljiZy15/E4egGSxJ7MiKRFZYn/lrBcQZ" +
                "IywBgDjdfqaBW9tJE4bauJr1SGV3TsW521LdGT/LB+rKOHuFsE" +
                "uUXwOj6ltRzH/SLuvcXwipxLCflUtPep6Hq7DHSe8Z0siWbTYz" +
                "vVIs2jbWRbZcaw541HwY/gh9iLXsrEvD32Hgo5WEg9luAN1gIC" +
                "lsf4D6kH0MgeR2HGFD5w7PoKiAEiaa+jGYsx+INIsF/KFUSsWC" +
                "8FS2Iv+kS3JOcgAUvQYwne1LXY6wqf76kH0MgeR2HG1GPXV+D4" +
                "paSNTTPGHiACFZniQcTUOv375ORa/M7x3e1cnNMs3/fJNuxotl" +
                "ZQy/t7cX4+g2uM389fq5Xc8vKZ7/3PhM8ZRj5n+otPF78fBbcY" +
                "+cyMPUC/H71m/L7uNV99+i+LOd5dtF7rM1tOvTb/k/9J7EUvZS" +
                "vzV1IClqDHErwJOfYGXjEueACNbmHD1GPXV4Alsn+lZow9QAQq" +
                "MsXDuWDrLvX5xn19crdsOf3D2TLl/Tr/bSYPud+vmxxzEHeP79" +
                "fZcqLv112c7u39On/enxd70UuZmEdzUgKWoMcSvAk59gZeO1kR" +
                "D6DRLWyYeuz6CrAE+0s31YyxB4hARaZ4OBdsnX59H6ng/xeXn1" +
                "PBz0frHfk51tu6y9+yWEXzg8Snx8inV+v3+sx5/xnzOfKHj08X" +
                "2P1WnyO/Gfn8XUE+fzLy+bOCfC4z8rlcQT5/MfL5q1j2ivn/o2" +
                "Bj2vNRW8v1fGTDps9HSfyO/v+o+PqMPjPefz4axOPdO9Wv9/P2" +
                "yJy0v0qILfE=");
            
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
                "eNrdWt1rHFUU3+pTW8QvWmgQAq1CEwXxo0WaxmZmd4UQ66vgQ/" +
                "8ERfGlj+6maZuBCoG8C5El0CLRV6F98gMEFR9a8hIoFAx5Mm/B" +
                "+mB2zp75nXPPndnJZmZ3M3e59875+v3OOTuzM7tJrWbHV1/WMk" +
                "brz1qOEX7e837PxP9VK2S0v822t37Pzqz/aL1z+Cxv/dSvn/nG" +
                "XCc1y+eL6WdroejMyhi3fimon5tj28/N2pBH64vDY4Sf1cZ0DD" +
                "+zQs7Pe2N7ft47iv0MPx3XfqZnNtbn592xPT/vjq6f4crA5+dK" +
                "Vj8Hxy3g/Fwps3fhfDhPK+2s68mr4fzyltLMW0nH9+RVwmIG+M" +
                "THq4who928fJzwbD/WOm13Y0Wtq35s9IIzsNXqF3tJb+87f0dK" +
                "y7+W8C5+P8L7ewp365vSGCN5NFg/geG1boywnxtl9jNcDBdppZ" +
                "11Pfm60SxaScf35F4kM8AHWkRJi88uZR+G9pBsbBG1XvdjoxeM" +
                "bqvVL/aS3t4eL9mjA79PmZHL26M7P2+eGvfvR+0Ddye8f+jr6v" +
                "7wIwdm/NoeHbSf2ZHhg0Pn+GD4kQMz3rFHB+5nZmT949Fd7+Vy" +
                "Ny42LtJKO+tIhgaesLOmvQ0LsCQaUHs1XdUIsPAus/Bx+nK3ET" +
                "J/1nS5dcUSARm4zJoPGWvv5tnmWVq7Ow2SSOaVNfDGUXtbWjjK" +
                "6sBR/0iyumyS0eX05SlRXDa3Csut80CHbLWaUWcMrn7f38N150" +
                "pez/WZsZ7uHa7nw8jHUFSc1nelQRjCZtiklXbWkRwfd6SmK0uJ" +
                "p5E7vfiOQGomugRDRrt5aVSpAbKL4rKxRdTaScHuqB50uAYfg+" +
                "ThfHHc5/xccypdy/U+raV7h2v5MPIxFBWnvx91vYrIMkb+p+x7" +
                "bHR8hM/zf5f6bLQQLtBKO+tIthp4Q9LxcoIBPtAiSlp8dimTZ7" +
                "Dj6rTdjUUmwY4fG73gDGy1+oX+6IrTz8/oZK1yIzpR8rfL1/fn" +
                "K61XfZ+f0XPWf+kPg3Buf77hQX7X+/f38wXlPdnH/pZffzvlqa" +
                "b1mvpUuHrQv7+3LvXvZ3Onev301WT7ufTDoP3kETyhyWvakZX8" +
                "IytCSn6s4Ek/DspXZ5iWr487Gz+N343O4Nmkyau1pknpeIPY2C" +
                "MfB7yQfzYDW7Lx0/jdaOuVeb3PWsQbHx7x6302z/V+Y6GUz8+Z" +
                "XEhHq58zefoZa4ZyPxrs/IxeOFr3o3LOz+il6t3foxdH+Lw0V8" +
                "HrfW6E1/uVCj5/XhnO86ew/JtwX6re981yawraQZtW2llHstXA" +
                "G5KOlxMM8Ik/w84whox28/JxSk+t03Y3FpncPOXHRi8Y3VarX+" +
                "iPrjjt/Kyfq+9fydGEtnZ1/Qd7+bwJ97Cj/XiwuPzcRWQZ39t6" +
                "/8lfn6hPeFgmcuUyke7txx3WyM9dVJbtZ2oVHsFGcV52NB41Ht" +
                "FKO+tIhgaesEuNnG6sRJW8QIDFeqRx2txthMyfNcGPbsUSgX3Z" +
                "S+fh8sg+9uZUY4pW2mPbFMvQwBN2qZHTjZWoCb9CgMV6pHHa3G" +
                "2EzJ81wXduxRKBfdlL5+HyiH6y93RjmlbaY9s0y8FvrIEn7FIj" +
                "J+klGlATfoUAi/VI47S52wh4wp9qstg6wq1H2qER/Uwis57ng5" +
                "+r9/0oraZhfD8KHlawnw/L7afn/v5ype/vzxbnZUd4IbxAK+2s" +
                "I9lq4A1Jx8sJBvhAiyhp8dml7MPQHpKNLcgkOObHRi/ifh7zVa" +
                "tf6I+oeiacoZX22DbDstXAG5KOl5NjgUG6uf8YQ0arfs74OaWn" +
                "1mm7G4tMXG7pq/O21eoX+qMrHv71Hp0f3fV++5My0RuXG5dppZ" +
                "11JHePwy3Sw4PtUiMnYsEg9fvv5JZGgIV3mYWfkzBk7jYCOSCT" +
                "bpyumI6Bh0wkru4VMna8ZxuztNIe22YT+W3WwBN21rS3YQGWRA" +
                "Nqwq8QYLEefk4gaxQdAU/4R2+6FUsEZOAyaz5Zi/Ru7jZ3aaU9" +
                "/oVwl+XwfdbAE3apkZP0Eg2oya+QCgEW65HGaXO3EfCEP9VksX" +
                "WEW4+0QyN+VWXvyeYkrc3keY4kkrGyp7T7JqKABlRoodEWm4Xk" +
                "dPN0UWBHLNbuHlzTURoheV66Zqt1M9HZMkvm3+Pq1XueXzo59O" +
                "f5qUo/z58pzivn759JP4MT5dcXNYbcz1Jrqj+tP6WVdtaRHH3A" +
                "GnjCLjVykl6iAVXyAgEW65HGaXO3EfCEf9R0K5YIyMBl1nyyFu" +
                "nd5/eQ49X7/EyrqZjPz/pefc+cs7GO9FilzBotaatG814be8yk" +
                "kdxIXvnly9ZmKT1lTHDa5fKhBqddFpu9K8Wa/wHSn+IR");
            
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
                "eNrdWk1oHdcV1qrUdUy9yU64NZbBTlOoqcCmENCb0SycOt2EtC" +
                "AIbcEUakIppZt0lbwX/0Rv1ToYUuqgjW2cXRza2C1qHdI2Tgl0" +
                "0UWQdrE2QsomgUAILnTunDnzfefcO0+j5yeZaoa5d+453/m+74" +
                "zej2ak/EH+YGoqf5DX89SUrsJ579sakSjOgIkPiTMbWKfqzTIg" +
                "EyPaNOGIZ1sBJPDSU8xtK3w/nEcEm6KLM8UZGcMsm6zCundYoo" +
                "gAzXWcEXwqZjW4muusB6+Z8sksXo15uaeYG07ZIXdrFa3jBn26" +
                "OC1jmOvcaV33viNRRIDmOs4IPhWzGlzNddaD10z5ZBavxrzcU8" +
                "wNp+yQu7WK1jG04q2/pmcX/zu157bFm11Qw2fGYy+eLp6WMcyI" +
                "6VpHjQDNdZzRqjgGjd41W23VWNFrpnwyi1fzXcTa1gdfGd+tVb" +
                "SOodX/VnlM92fA9PKvt/cz6R8pjycT8dnyOBVFj5U9XX/411n/" +
                "G1vkT6Tjbdr9o1Hku9vy873qe+mj/COfkZjEMfI6tWIMc6YUhs" +
                "9qRqpSGFSCNYVEBA68LtdcfNz20K5vVUZHmiuwmq/KKHOVW633" +
                "/eW+unhA4kA0eYrwIXFmA2utvt8yUGa1cegUvGbsPa4AkvD7fc" +
                "dy3l/iCt8PMyJC11PRK/mKjDJXuZV6f6zcV155QeJANHmK8CFx" +
                "ZgNrrf6YZaDMSuPQKXjN4XPee1wBD3AStG3Hcl5eT6rw/TAjIn" +
                "Q9a3RxvDguY5jrz9bjWOuoEaC5jjNaFcegMfyhrbZqrOg1Uz6Z" +
                "xav5LsL7PXYOBvo+irq1itZxgz5WHMs+C2OYQ0ZXYR3OFw8gEv" +
                "B2pfXIVOzJmJ4JLyI6SzRkoSF5xcBn9tngYxvjCutMfEhMVazz" +
                "sPeXtFavAmfBYH1oR+pTtrmZsGMlB28ak4MRMvPIcV5DY7jgs3" +
                "5TrFVpY1RX8OUziF18PMXPsVTHrMidpHxlX84dCXv2pa5K3JFw" +
                "phFZyyFoWVWMRzQvleDUWsVJnfACyT40qxqsYnGiCZT2oEg511" +
                "7Uo8R8nh2yf1wJrRMNVdK+ucvR90eXvtLlN6/B+u7f5WTv7Gzl" +
                "8Mdj3h/dK+5lG2GUudSrV2EdzsvPzybC6IC1EZmFVbhsTM+EFx" +
                "GdVV/yrKIY1Qmo8vMz6YrVoBNqWMXni3vl52ejJijOWgX4ALr2" +
                "+UHxQfZFGGUuc/UqrH2E0SFjIzJX3dRcNqZnymtZREeyrBjyil" +
                "GdgPIxrmBn2qPMquLzOouaoDhrFeADaDmfOzV3KvskjGGucvUq" +
                "rMP5Ky8gEvB2pfXIVJ9EyZieCS8iOks0ZKEhecXAZ/bJ8Hkb4w" +
                "rrTHxITFWs87D3l7RWrwJnwWB9aEeNz5NzJ2UMc/25fBJrHTUC" +
                "NNdxRqviGDSyvq22aqzoNVM+mcWr+S5ibeuDvp2ibq2idQytxL" +
                "3qtRH3sde6fCpvhSr+0P0Tvpti97rtaI+h+mb+powya0zWiACJ" +
                "vEYG68iAi9k866vvWQaNaw3j490yWxbrkv1rZP5d3zEzwIHv1u" +
                "rBsUUnr/GIp2l5pydt+fUJ/sSvT7aueLHT7z0vjut31PPP4c93" +
                "5vnnJLZxn39een1Hn3/ezG/KKLPGZI0IkMhzhA9fG7MyB1dojU" +
                "X43TJbFuuS/WtkeM53zAxw4Lu1enBs0altu8/nt7/1HuHfpXZW" +
                "e/7w/OF0TOIYeZ1aAYO5TaH3uWakKoVBpY66t3XADqwu1/Q+9y" +
                "7b9K3K6EhzBY7OH5VR5ip3VNeIAIk8R/gIe+8cs4G10TcMyMSI" +
                "Ns3hL7z3uIL9a6R3znfMDHDgle214l4Met/8vujKV7FwZFckq2" +
                "sdUytgwpxdsWzumcQVzUhVCoNKHXVPo6wDq8s12hO7TetbldER" +
                "1c7v5nezN8Ioc6lXr8I6nMsoyDAizxGepTacKRuzhnPm0PMQF5" +
                "awK48wYqUVygE96wmdVd8Wd5VRnHCVZZBaQXGW84g0P6M3GtXl" +
                "fFlGmSv1ZV2H8+FPJA6E5jnCB2qVzbNmb1sGjWuNxac1Y+9xBZ" +
                "DAB23bMTPAgVe2enDs0Pfz+zLKXOXu6xoRIJHXSHl/1GTAxWxg" +
                "bfQNAzIxIq0JZstiK9i/Roa/9B0zAxx4ZavHvRj0Wr4mo8xVbk" +
                "3X4bx6fa4BiTxH+ECtsoG1fn/csgzI5M2zbK/gNWPvcQWQwAdt" +
                "2zEzwIFXtnpwbNHFneKOjDJX91p3dJ39xkYYbeswC14qbUzPZB" +
                "VXq75VZKznSLmyat6B9hRz41qww7hbr+PQt4vbMspc5W7r2kcY" +
                "beswaxUO5qh/mn9MVauaVQQL+2p3ZdW8g1jbuxfs8Fde2ep5x6" +
                "hM3OE+sb17gu3//Shbfui/Hy3vfmWXbe7TuU9llFljso4jQGM1" +
                "WLdrHFCARvX6XFAOVve+UpqMtDGb97Vw4rUZa33H3dod1wfo3m" +
                "xvVkaZqzuyWV3HEaCxGqzbNQ6tBUd9Pc8oB6ubO9LZtCYjbczm" +
                "fS2ceG3GWt9xt3bH9eGORz2vyxJ3DRf+/f/9vC7b1+V53cVnxn" +
                "tet8Xzz9f33vPPxR/t5PPPR/P/tMPfP7rrKf9vs9X1vHBr3OuZ" +
                "+L5eNO+PG+79cqPTt+iNdnR2oxtHN4VJ1dl4WE3CJV/PfDqfLt" +
                "8fB9zfZaY7/fVmuh0tvA/t8uMx/67Uot1f6ooc+bT6RO+EjDJr" +
                "LOz5TD5jI+EsxBjv64GqPM1IBpgQAwdXe1+WlSMeYxGsphn06r" +
                "UZi2uAHlIKrKNoOfLNfFNGmat+N+v9ULlvVv+fvEnIQ02+qbGH" +
                "1NarQ4KCRhU7ZBk0ow7YhWGlyPCq9x5XoCNlFEe2YznvL3GFdm" +
                "p9eB16LSvXRr4ho8xVbkPXvQ81AiTyHOFD4sw2NXX+Z4izRr5x" +
                "/ixnYkSbZuw9rgASeOkp5rYVvh/OI0LXs0YXC8WCjDJX96ILuu" +
                "79x0eAxsrW86G14XoqBlHZz5+1GZ+3mmkOi0AOtXASekpx41oo" +
                "e9yt3RXF6OQnM/7/M5/ac1vvp5NDJf4ucnD+oIwya0zWvfc1Ai" +
                "TyHOFD4swWXp+Is8b8wfNnORMj2jRj73EFkMBLTzG3rfD9cB4R" +
                "vo5aGf8+P7ja3B/d2nv3R8O3Ht39Ue/5vXc923qa1PUc9f8hw7" +
                "f33udn2+tzsltRbjiXI8bowQhGpqpYgTVGYcGVUok51FXsy45h" +
                "fvUye7HusRr+Cd1azqKwnUSv01Hv98S7+NJz47zfL3yt7f1+4a" +
                "u7/H5/ssv7/dKzD/t+770mh45xtm3V4vu18XKK6KYBFPyPVtDM" +
                "aP42fV89QueyHDrG2bZVO984OUV00wAK/kcraGY0f5u+r+7icv" +
                "638dnktklxjsvTe2pyqI5Of1fPV/Vsiydn6+OwT8rltr/f7+32" +
                "bxTzl+OzCV7Py5N2uc3r+eFuXsvBsvl+/2bie+wHI+vfNav6vy" +
                "cG7+j3++Cv/a+3f78P/pJg/Ht5/Cvh4/utHu6G8aX6+39Q/5fB" +
                "4G/V+L7vafDnBMM/O16t98rjHwN+xf8PLpSYzQ==");
            
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
            final int compressedBytes = 1895;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW0toHWUUvpAIoSWIDV0orQFNm9C68QVBurkzd1bShODbQN" +
                "2IC+mq6Kqbeu9tept714IokiDBTVtQBB/BF76qkGahLXHZjQpX" +
                "dBFX1oj3nzNnzjn/Y+507kwmiTPM/P//nXO+88g/75tKxVxeew" +
                "Va/4B/oFJZHK3kutTrinlwnsaP2excvutLeWRX69Q6sIcWMbX6" +
                "Y/5YrdM8DThpoJwjfAOcsxFrlNOYZCAJtuTF5bP9vR67aUExUC" +
                "TKt8wY+vUlbqHnwxkJ4XUkS3+It/5QPD+HFLI4inKpbSJcYsNI" +
                "ZkPJv0vO9fyhxs3kqCRq68m+nJ/gI13euszb5K23ifX0NhGxab" +
                "vs3RjJbCj5d8m5nq6RxOjquazsPtx5S1n9eG87VJ8wz5+pz4j3" +
                "97YHLPgjvW3aQKdyOg+P95E/aMeDsw79I7pW/eHbiuex/vVs39" +
                "i59Wz8nq2erTfS1DNEMtXTfX1PndlvlV20tNeKZG+92jv6b0Vn" +
                "gVuwr5/BsUIWR1HOtWwIl9gwktlQ8u+Scz3vlrweJTG6erIvr0" +
                "fgI13eGk/C8V49YfkLnN3d509bTubx3nw9+/HudXnrdePrexcR" +
                "8VfpGn+nrimxYSSzoeTfJed6ukYSo6vnsrL7cOctZWVc39u/lj" +
                "c/Lxwsen5Gx8EGbLg3pa6R47jayCZDjXQ+SIviT/aAkmR+l3/d" +
                "2qblbfHW24qP9y2FNE+jXGq77N0YPr8DrysOJbXLuZ631V5Pjk" +
                "qitp7sa9ejLZ3TnTfvBYvBIuyhRQzGqu9dApw0UM4RvpEteeB4" +
                "z/8lyUASbHkUdp/AwWM3LSgGikTZyYyhT3wUCeeVtaKIpXaY3T" +
                "JvveV4fi4rBPbsr7Js/J2WTYmOSZlNk/zrHm3++kdl43ZFaTKA" +
                "j3R5817QDtqwhxYxGKu+twI4angrKCcbuakVrUCbWEPvK5yDSz" +
                "ACGQXpcASZJYu0oIwoV2UnM8aIuYUacU3OyOtD0fLYB3s+yvCM" +
                "8md5z0eu63tu73eTnt83d+7z+8JKtvuli/dt9/P7wuXKHl5ab6" +
                "bSeivj+/np2jTsoUUMxu2/ECFNknOEb4BzNmLlfomBJKaGy6cZ" +
                "u2lBmqTfvkvPmDNQBLpn6Y/nIrSv167DHtpQdh3HQRUR0iQ5R/" +
                "gGOGcj1ti/YCCJqeHyacZuWpAm6UNOJre00PPhckJYPSPtYCKY" +
                "gH0Qn0FhFOInAGXIhBxxFNn4mGPSB7fmdjIG3actTs6ie+O8PC" +
                "eTmyLlEfJspUcZMflyX9/9o/5R8/umwvovqGXTBt6B3yffzGbn" +
                "8m1+38wSZe1k7STsoUVMrf5h/zAipKkwWlHGN7CFEWgTaxjnYc" +
                "7BJRgBj4KzmnHy2E0L0iR9yMnklhaYqYxD98PrGG2ztVnYQxvK" +
                "ZmH1p/yp2mz4vXiWNBVGK8r4BrYwAm1iDes5xTm4BCPgUXBWjr" +
                "T/1mM3LSgjZISIZMbQry9xC8xUxqH7YfVErpnaDOyhDWUzOPbm" +
                "ECFNknOEb4BzNmKNns/mJANJsOVR2H2asZsWpEn6kJPJLS30fL" +
                "icEFbP2DIw3pMGXYWprboGUhy33kVtwrmcGCSf40tjFzy5NFDK" +
                "tSVm08LeuXE7a3VN17Wx9l/MHNXWfKH5hK7ZfC72/VP+99PN+Z" +
                "x4nsxmV0ROLKpePZvPNOcc9bxmefb+JxXvU3J8/iVXPc+/WEw9" +
                "m8866nktFfvTRdQz+xKMJs3PYLT4+emqZzrfWSMMRoIR2EOLGI" +
                "yrV3WEtE1E75MtcXC/XBMl1PbzaWJmn2wpEpWTK3riRonkkquM" +
                "F/tJ75danxT0Pe7fEt8vPb+d75can4rv7/stdjOGDft9SONLIf" +
                "k4aj+Mkc/qdyY886xasK972w+WOB53snwB1/do9FHUfh7uv9Nz" +
                "aljmTOPblM9oX/W2bxpX+1T6DvFW/+f8r4J5cGblcNkVkae1nh" +
                "sF1HOjPA6XXV55Jv7eZmTnvp/P/Hubke1+P8/qeXzvvZ8vOqfE" +
                "+XnM1O/syzI/F/a55ufCyDbPz2Np5ufgv7dh307iJ9zq+M6dZ/" +
                "WFjPNzvLz52dm/986f6X6fnOf89E6ZvRyv7afK5Skipz5Pvmcr" +
                "e3ipVvPTSjlzzxSdU+dgefW8sO2+6U1p60ZB9by7+CzOjZdTzz" +
                "J+n+xdLe965PJd3P28/z7veVcKuCZdKZ6Dskhnl1eeifdLh/6v" +
                "90vZ52cpx/vqwLNzNfPxvpqmnq1fdlM9O/eWNz8vflDs/Xwp9T" +
                "xSXj3T/b9M1uPdn++tw+E+bHsIjtQ4RkAz3JOcIaINbVUP2WJP" +
                "8xE2rDFEeMQyHPVc6zDyc3+SkTKLfJP+sLRi/mPbKG6uOY+Z8D" +
                "jia98wWZYyP4/t1fmZXM/g5bzq2Xlop1zfXTnt6vPnozt5fjYv" +
                "Dzo/5ZLn/7+b9cztrc3bfeTvOJ5170l1v/TH7daz//28P+lPWp" +
                "47JtPMT6Wl5qdFe8rOu13z0+XbPH9mizLxe8f6oMe7BS37e9x6" +
                "2noW8Lw5vffq2X6v0Hr+B1jZX+E=");
            
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
            final int compressedBytes = 1908;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW0uIHFUULchGA4MI40qhh+gkM60Qgi7URUxVdfsLk40gCg" +
                "E3UZD4gRCyM5/uzCST9MqF4EaCgpClLgQXbv0u1HWMJLvZJLgd" +
                "SXqcV7du3XPfp9LdU9WVNL7i/e7n3E/qVb1XPYmi3tNR1Hui91" +
                "RUlHMno7FK78nt+oyH/tx2fd6hLkVR+ka049Jr3YN/wE8P2e4t" +
                "4uzi5jbl2bH8eTHvS/I5eKGefFZRJs3n5W9GyWdGmSifUZTcwT" +
                "65w/lM7jAF9fTM1Q/ThOejiv0QH+VsiTLE0Cik5bcRjlvzyu7P" +
                "+Ojs3Z+hmPT9ef6zOtZ7vDyD+Vyud72XPj8Pzl4+L35e7/3pln" +
                "Hf7/2N6AEqg8frxa9nvxQfL7s/195scL0fr/P+THtpj1rqmUZz" +
                "oYik8JnS3xCOYCGajTpY0AhMZx0t77Pp893VQP+ZMmjZESOCeG" +
                "Bb1vbEY0t6I81WK/c8Fjq3ROERzvowlopogprTvhMczdF6rk3b" +
                "TxsFbGyIJxijbVsjoH2sOg+ux4XkjfRGNs97Hgs9uWpaljBz4f" +
                "uqaGU7s6togUbJVcFgZLHueiEyKMnIGkX4oiut6Y0eavFYPGV0" +
                "lsQsuXbYGtXO/s5+aqk3PJrxOLlOdJFgPlKwii6jCWru7XWNIB" +
                "zu0Qu/TcJA310N8UE8MXo6YhoLnniCuDpX4rGWzt7Pj1hP1hM8" +
                "Wn84mrnS3z1ti+e+V6eqv6q3UAXmpBghvaribOL70uDl5vZLFx" +
                "5r7nxUWz4P/5/P8fKZHLtfz5vGs3q/1znPz2Hd581Bt8HzZmcU" +
                "qfUDFb6P8vsznUvnoujSXPUxpXPN5TNku3elthP9idrvz1cbvD" +
                "9fmbZFyWdycdJ8lmsOXm8wn6/VbSG5hX1ya/0lnhvKpTnma+mQ" +
                "fpgmPB9V7If4KJfc6t8s90pTfSM91uudbIwWt4VT9j357dn7nn" +
                "z50Wm/3/vLta+5o82t99D+s+oS/02VW5cbmoXxJuGxxGg2REr8" +
                "L7fAnHL8kH1b25Uq/T3uDxdx7fdJ1vvqe6H1vnpsuuvdF5O73i" +
                "+s1PL75lJV+VzbHcrn2kNTzufSKPlc+7a652fv33zn20pb7n4+" +
                "bY20a26FpQl3p0W/38fYz7dG3c9X4WV21nqLR50fo5kr049pPd" +
                "8lpfPpfE3nzfkGz5vz0zlvxp9S5dblhmZhvEl4LDGaDZES/8st" +
                "MKccP2Tf1nalqvteF5+Z3n4+PjPx++jMKO+jSX9/X39n599DCk" +
                "8/md6qndxW/V4mQ+yTYfH3ikND2T6/D33SIf0wTXjJMOyH4SbD" +
                "e/mbDK3z+7AsMv9Ij63z+9DGDMftSN7GPrld5PM2U3zSIf0wTX" +
                "g+qtgP8VHOlihDDI1CWn4b4bg1zzw/dany78Hc52d2fv9w56vq" +
                "/Bf34H8V+HbwwUjPzy/HfX6W7Jfery6fgZg+bm6/NPho2hbjH+" +
                "4PjCajm9T/zlJniVrqmWaudDld7iyZ/TzLmNbQ5GIeVtKlGUkL" +
                "arajXkYM5LAH6AWiIqV/0/bd1ZCIJFZjW0dM494V1OBItR+2Hc" +
                "wj+m49Sf7JTxIL6YLnfLEw0ilkISztx53a+WihesnyAt+X/py9" +
                "83vdMZV+r9s3zvmo7Htd1eejHXyv2zfK+z2jPDB/b9NkPtdP1Z" +
                "vP7d3oFvbJVrGf32KK2sVuOfvaLZfjownPRxX7IT7K2RJliKFR" +
                "SMtvIxw3juJT8SlqqWcazTs/2xSRlpnWxyoWREaoooUcHx/nPg" +
                "wtgdaYI56YmHzYkgtGd6PVF0uhdPl67/w6e+s9FJNe76tfV/d7" +
                "Rzp0R2N/Y3n3fn2/1+1ZE++jzq4G789ddb6P0s1007k/M5qp3Y" +
                "PE5Tm3TNEzzdVo3nWwyZY0kq3JLV8+b10vURJ1OCb0NuyjD8FH" +
                "KTJwN71LLfUZ725+tbevnAKS7YJf6OhKuvmsTVJiI6O1NQJz2A" +
                "P0QqE6fqLvroZIgnzbjhgRxAOKVPth24F85prdle4KtabP/wVX" +
                "eN75iahCEWnUQw7J+2jaBmqjnvbBtunzE1Fsa4iLMbnY4il6iN" +
                "Fqi9rjQvpI9wi1ps95R2TOLVNEGvWoxmdRC6UFQ2ygtraGFm2b" +
                "LBmftWmaj7oYhdF0PRcEyKcTrbaoPS6kD3cPU9st/o8AzWjOLV" +
                "NEGvWoxqdRC6UFQ2ygtraGFm2bLBmftmmaj7oYhdF0PRcEyKcT" +
                "rbaoPWbp9Fp6jVrqs2fBNZ53fmGKSAofKViJjmiCWjxvFIJwXI" +
                "mQTdd3V0MkRZ5icrG1hh0P8oUCz89c+tCeQ3uoNT0Vmpl55zei" +
                "soyMcKYryyOaoApVKJrjeoE2bT9tFOGLrrQSk4utNey4dB5cj1" +
                "my2+62qTV9fu+2ed5NiAqUtp4hldFwjjRtA7VRT/tg2/T5iSi2" +
                "NcTFmFxs8RQ9xGi1Re2x2Mqyu2gumVFV+V/EihLUY4t0nNs2yg" +
                "rLaishRPZK/LI5SPPh23w7YrSIkfjjOLTXXDKjqiWwogT12CK9" +
                "zEZpPvdqRJ+8ILJX4pfNQZoP3+bbEaNFjMTx6z+ZMCmW");
            
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
            final int compressedBytes = 1623;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj1vHFUU3R+QwgEiKJAoMBJSaAgUhCre3Sn5BXFt+QdESp" +
                "21MVqwLBqgACkREnIR0dAguUgR0WBZoqAAalxG4h8g7Lm+c865" +
                "943JriZhgfdG7729X+ec++zZHW/SHDfHo1Fz3Fzso5Fb/po9DW" +
                "VrHXavwmSM0cUoVTubMnJuxCipUraoIHNH9Y4fmZUvKvbsWzdv" +
                "3bT1fLdhltm+ugfZXMcRr8o+5eBqZWPGyFnSySiRLXaRuVXHaK" +
                "QK42lEHV4Brjxmv/ur+c6ojoXG5OHkoa22u89seJCJOHt4xtqM" +
                "yhhc4TWaES9FVhRVyfpZiXbMCFAQu1U+KNbs4hkfXnL+h0/1Uz" +
                "oc8Cd++Hzrhh/zeffq43oHD3Cen3Sv9utpLHhXPJo8stV295kN" +
                "DzIRZw/PWJtRGYMrvEbzy5xZe65g/axEO2YEKIjMygfFIft0cm" +
                "qr7W3s1G14kIk4e3jGWkbt+AUBkZzRx5m15wrWz0q0Y0aAgsis" +
                "fNwLZzdHzZGttrfPpkduRw9nax12r8JkjO5ZvFDtbMrIuRGjpE" +
                "rZooLMHdU7fmRWvqjYs2cvF54/X1zmnWP2Svfqhef5jjV7aeU/" +
                "jz6tnytDjg//qGcw6O/nZ/UMBj3PL+oZLDs2bmzcsNV295mdPc" +
                "iGpfU8wYAceFHFkVKc7RKGZjCbR7KSiI2zcPTcrV44H+54dv1s" +
                "vjpbRy/37lz2E9j7KX3Cvn423yp88r57Nt9L3jcH+lx/7W/iby" +
                "+I9wZbH31w5nlnofr3L/YFz7OA9B88z73vljvPyZPJE1ttd5/Z" +
                "8CATcfbwjLWM2v09IQiI5Iw+zqw9V7B+VqIdMwIURGbl4144u3" +
                "hHX6ufK8uO5nZz21bb3Wd29iAbltbzBANy4EUVR0pxtksYmsFs" +
                "HslKIjbOwtFzt3rhfCh7s9m01fY2tul29iAbltbz9FpgdGexyS" +
                "gcKcXZLmFoBrN5JCuJ2DgLR8/d6oXzQfb06vSqrbafx8zy174i" +
                "w+Ps4RlrGdXPQhEQyRl9nFl7rmD9rEQ7ZgQoiMzKx71I9sn0xF" +
                "bb29iJ2/AgE3H28Iy1jNrxCwIiOaOPM2vPFayflWjHjAAFkVn5" +
                "uBfJ3p5u22p7G9t2Gx5kIs4enrGWUTt+QUAkZ/RxZu25gvWzEu" +
                "2YEaAgMisf9yLZO9MdW21vYztuw4NMxNnDM9YyascvCIjkjD7O" +
                "rD1XsH5Woh0zAhREZuXjXji7NO79WZ97ln5eut/ct/V8h89trJ" +
                "7J8dJEFTNkDng0klUwZ9QZURBHLVbtRrERZ4Xae4lHs8dXxlds" +
                "tf08Zpa/Vg+yYWk9T68FhvM7Blfz0DjbJQzNYDaPZCURG2fh6L" +
                "lbvXA+yJ4eTA9stb19LzhwGx5kIs4enrGWUbv3G0FAJGf0cWbt" +
                "uYL1sxLtmBGgIDIrH/fC2cXvP79/pv/eMxsGZ+fn1XwHrd8vlb" +
                "5f2v28fl+3GudZeF66M6qj3u+D/n62nnq/r+T9Pv+h3rVD3u+L" +
                "Pi/V+/2fvt/nP9bP91V7nl/NMX48fmyr7e4zGx5kIs4enrGWUZ" +
                "kXCIjkjD7OrD1XsH5Woh0zAhREZuXjXji7Pn8+q7Hxi01fc7TP" +
                "6sdbJuYZT8eBLOi/nMEjl+P38cfqmNXsN/u22u4+s+FBJuLs4R" +
                "lrGZV5gYBIzujjzNpzBetnJdoxI0BBZFY+7oWz6/0+7Nj7auH/" +
                "D/Zlff7sf16a/7boec5/ree54PPnpf8et/d1vav7x/TB9IGttr" +
                "vPbHiQiTh7eMZaRmVeICCSM/o4s/ZcwfpZiXbMCFAQmZWPe+Hs" +
                "f+/3S3vf1L/f/w/nWVD6bX0fHPQZqp7n0qO529y11Xb3mZ09yI" +
                "al9TzBgBx4UcWRUpztEoZmMJtHspKIjbNw9NytXjgfZI/Xx+u2" +
                "2t5+V7LudvYgG5bW8/RaYHTfx6wzCkdKcbZLGJrBbB7JSiI2zs" +
                "LRc7d64XyQPd2abtlqe/vZv+U2PMhEnD08Yy2jds8XgoBIzujj" +
                "zNpzBetnJdoxI0BBZFY+7oWzm91m11bb29/dXbezB9mwtJ6n1w" +
                "Kju1d3GYUjpTjbJQzNYDaPZCURG2fh6LlbvXA+yB6vjddstb39" +
                "3V1zO3uQDUvreXotMLp7dY1ROFKKs13C0Axm80hWErFxFo6eu9" +
                "UL50Md/wWH3Bug");
            
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
            final int compressedBytes = 1431;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW02LHFUULUWQIGLAoEaFWcSAjEkHcRaiQrSqOypBNIyfRB" +
                "lQwVkkG8kfqKappIes3biYX+BCf89sRcUsNBGzEMSevn37nPvu" +
                "q+mpmuqZ4LxXvPfq3nvuuR9TVd3VMMVOsZNlxU4x27NMJT3XFQ" +
                "i1s4Zn6Mus2WxYBlg8oi4mMuLdenD+nImtmBmQQRjZxuNaGJ1l" +
                "gztZMAZ3dnWix8pyTGIey+kjII7lieVh0VYXQy2K2x+E9hjr4u" +
                "FrFE35SIgsb5RPTNYnp+enputTk/nc7tn47gxzdir9MZNWJ/Mc" +
                "GMZ/Gr5X4xmVl8q3y3emZ5ej9s/LL0j6svym3CwfKh8OUI+XJ0" +
                "l6ls5X9u7I+N4M90b5Zpk37Wd51UhfTebXWZav5Cuyyr5rE0nP" +
                "rQZoSNafp/qCQ+MrB3vzsHaWYxwWwdHU4jMJudELZffV2gP94Y" +
                "qHL03m88MX+Prc8696P9QMz0zmOY8crk2muzaHL2bZW39lBx7D" +
                "BVff8OW4vi728KzTvNIon9dme8N+Rpga97OL0baftXjTz/E/h9" +
                "XP8b/HoZ/p+uy2n9VPbfvpR37GnzUd7T2XPZab2WhjtD76ZHTF" +
                "6D47OO/gZG3Eq3tZG2T+4QL7p00za46K+J0YnJBVdtWJ7DVAe0" +
                "14Dl9wcFxGqgX7ophe58/h6zOpy5gzCbnsYfOVmffynqyyT++I" +
                "nspeAzQk689TfcExv+t6zMKWmJ3lGIdFcDS1+ExCbvRC2X219k" +
                "B/gO5v97dllX36RratMjRAws4anqEvs87f+gwDLB5RF9Pn7j04" +
                "f87EVswMyCCMbONxLYxu/vl+66Pj8Pl+a73d53t1v/ql+nm3n9" +
                "X8vaH6rUV1Nf2s/g611e/d9LP6dYH9bmvm2a8S1b2OvlFs+LMO" +
                "v69sHC3PMmqiv8X/9vrcevQors/Uz+Xf79X1Zd4RW48dzntQde" +
                "1BeSMbfpul0XIUt4vbssquOpGhARJ21vAMfT0rc7CH+lhEeFhm" +
                "y2Kz5Pw5E1sxMyCDsFobDxlbdPSX/BvpOuv0mv2+na0pav/ZhH" +
                "z74e8yhwbfxlbzVVllV53IXgM0JOvPExGAgRZebInZWY5xWARH" +
                "U4vPJORGL5TdV2sP9IcrTr8nx943p5oW75v5er4uq+yqE9lrgI" +
                "Zk/XkiAjDQwostMTvLMQ6L4Ghq8ZmE3OiFsvtq7YH+AF1sFpuy" +
                "yj598myqDA2QsLOGZ+jrWZmDPdTHIsLDMlsWmyXnz5nYipkBGY" +
                "TV2njI2KLT/d7t/Z76Ge/n6LvUz676Ofqh/fVZVEUlq+yqExka" +
                "IGFnDc/Q17MyB3uoj8XHY/rcvQfnz5nYipkBGYSRbTxkbNHp+k" +
                "zPz9TPsJ/Fx0fXz7rYXfUz/R7S8a+d6X5Pz8/Uz2PTz/T8TM/P" +
                "dH0e55H62X70z/fPyyq76kSGBkjYWcMz9GVWjgsGWDyiLqbP3X" +
                "tw/pyJrZgZkEEY2cbjWgy61+/JKvvU1lMZGiBhZw3P0JdZ5/EN" +
                "AyweURfT5+49OH/OxFbMDMggjGzjcS2MTp9Hy//+ufV66meXn+" +
                "/Fj/Vee9n2g9qffxdjcaRl5JKfzk/LKrvqRPYaoCFZf56IAAy0" +
                "8GJLzM5yjMMiOJpafCYhN3qh7L5ae6A/tuL0fanD6/NiflFW2V" +
                "UnstcADcn680QEYKCFF1tidpZjHBbB0dTiMwm50Qtl99XaA/3h" +
                "io/i/4vT+2Ya+xtbl7zu5pWDcd784Pj2c3BhcEFW2VUnstcADc" +
                "n680QEYKCFF1tidpZjHBbB0dTiMwm50Qtl99XaA/2xFbtr9t10" +
                "37Z+qj8d6ed787P3GzA9M/e6fKgVnHrQe5yenwd4fq4N1mSVXX" +
                "Uiew3QkKw/T0QABlp4sSVmZznGYREcTS0+k5AbvVB2X6090B+q" +
                "+D/cdQHz");
            
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
            final int compressedBytes = 1732;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWT1vXFUQXfE7rFAAAgkahAtEhdfvSZabFMh2lRS4dGEJ8Q" +
                "PMBoxxi8RHg3BBj6j9G0CyU6QgSqT8ATfp8b7JvHPOzL1r72ax" +
                "Eue9pzvXM3PmnJm7u971ut1v90ejdr99sY9G7vnPGgEantbz8l" +
                "pwjEaIooozpTz7JQ5FsJpncieR2/PoO0+rN86HJ558eLlWJu9i" +
                "lm++Hs11Td65XB8V4quX69MU/WC0lOvwjyu6+njOKd5LkU/mqv" +
                "9sapsnzROztnvMfESARJ4jvGIts7q6MiCTETXN3Huu4P65E52Y" +
                "GdBBVFY9noXR7Wl7atb27rl76j4iQHK+tFDlPzMroqzKGa7Lms" +
                "qRWZDXibQTnZgZWJ9XrIwd98iL9sKs7V3uwn1EgESeI7xiLbP2" +
                "+sKATEbUNHPvuYL75050YmZAB1FZ9XgWQR+0B2Zt73IH7ucI0P" +
                "C0npfXgqPXP2AWzpTy7Jc4FMFqnsmdRG6chbPnafXG+QA93hpv" +
                "mbV9mjPPf9YI0PC0npfXgsPPwjm4mi/Ns1/iUASreSZ3ErlxFs" +
                "6ep9Ub5wN0c9acmbW9+9165j4iQCLPEV6xlln739/CgExG1DRz" +
                "77mC++dOdGJmQAdRWfV4Fka/vp+XJm//n5+Xvv1pwc9LD5uHZm" +
                "33mPmIAIk8R3jFWmbtH09hQCYjapq591zB/XMnOjEzoIOorHo8" +
                "C6PXn68/jydtMYvDsu8R9TSrbKVrmnGeWZVu/S51m7tkpNZErR" +
                "Jr7nV2xLWbvWbPrO3dWe+5jwiQyHOEV6xl1v7xFAZkMqKmmXvP" +
                "Fdw/d6ITMwM6iMqqx7Mwerwx3jBre/deteF+jgANT+t5eS04+v" +
                "fmDWbhTCnPfolDEazmmdxJ5MZZOHueVm+cj06cfrN+1Xf8w6Lv" +
                "FotXvt7XuBk3Zm33mPk5AjQ8recFBWAQRRVnSnn2SxyKYDXP5E" +
                "4iN87C2fO0euN8CL0z3jFre5fbcT9HgIan9by8Fhz9WewwC2dK" +
                "efZLHIpgNc/kTiI3zsLZ87R643wIvTneNGt7l9t0P0eAhqf1vL" +
                "wWHP1ZbDILZ0p59kscimA1z+ROIjfOwtnztHrjfHRiu9ae2XJb" +
                "+yl75WtWBXtlrrVnV2lYv9phrd+S9mz+mn6snqHzry23OVvz6n" +
                "yL5BxxPQ2g0P9sBc/M5q/px+qIOv5y+Htzmd/P08k/tuU2Z2te" +
                "5RF+vFjOEdfTAAr9z1bwzGz+mn6szqh5n5+H/7wJz8/DvxZ7fq" +
                "5N1iZmbfeY+TkCNDyt5wUFYBBFFWdKefZLHIpgNc/kTiI3zsLZ" +
                "87R643x04ng9eGs0XEu8vv97OIPhPF/da97PS3P+3p/c6u9Dno" +
                "6fmrXdY+YjAiTyHOEVa5mVdcGATEbUNHPvuYL75050YmZAB1FZ" +
                "9XgWQZ+Pz83a3uXO3UcESOQ5wivWMmuvLwzIZERNM/eeK7h/7k" +
                "QnZgZ0EJVVj2dh9M2/3m/31TxqHpm13WPmIwIk8hzhFWuZlXXB" +
                "gExG1DRz77mC++dOdGJmQAdRWfV4FkYP/y9e7t/v49XxqlnbPW" +
                "Z+jgANT+t5QQEYRFHFmVKe/RKHIljNM7mTyI2zcPY8rd44H6Cb" +
                "7WbbrO3dc3fbfUSARJ4jvGIts/avD2FAJiNqmrn3XMH9cyc6MT" +
                "Ogg6isejwLo4fX+3Jf78395r5Z2z1mPiJAIs8RXrGWWfvHUxiQ" +
                "yYiaZu49V3D/3IlOzAzoICqrHs8i6N1m16ztXW7XfUSARJ4jvG" +
                "Its/b6woBMRtQ0c++5gvvnTnRiZkAHUVn1eBZGt3fbu2anu13m" +
                "me/WI0BzHWe8KsdUg6tVjRWjZqlPZolqcYqsrX3ghPK0qqgdQ6" +
                "vwm6D/H9Pxd8Mn9EWuz9+f3vBsKYIXI2xny/FZGld1xLgSHoze" +
                "FfqKGY6V+GM+TsyKPMlVc0yv9d8Xy82Lut415Yp81+FfZg8v+U" +
                "kEr/ej4bX7qn//OZzncM3x/dKd5k45ZnFY9kseMNhrCshYVRnj" +
                "Ubd+1ybgDlRXa2KXNf1STS3Sn8BKs5KQXczisOyXPGCwg63Y44" +
                "orlTEedet3GaUdqK7WxC5r+qWaWkS19Tr+cXjdLvM6/uVWT/fz" +
                "tVC/Lk/xwfHwnFrqI/jbcAYv8el9+P5zid9/Due53PNs77X3zN" +
                "ruMfNzBGh4Ws8LCsAgiirOlPLslzgUwWqeyZ1EbpyFs+dp9cb5" +
                "EPqkPTE73V/kTuDDOpLzpYUqsIEVUUQ0k7tgzdhnZEEetbA6jX" +
                "Ijzx3q7CUdRQ9/b97A+/ufwxnc3Pv70dab8H509MVNvb8P5znz" +
                "PP8Dqj6Wlg==");
            
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
            final int compressedBytes = 1414;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vHFUU3Sq/gBKJApCQcEPAEkrl9cxUQOcGyVLkv4D8B7" +
                "xOApie0h0lFQVF5ArRRSJJkT+AxA9winQRzlzdOefc+96aXezY" +
                "jt+M3nt7v84592VmPR4rs9ni09ls8f7io9l0HOzPVjoWH56NjY" +
                "L/i7PxZfJ+MruQY/HBOfHPVsT7mK0HP595Pl+p/l4tsup+tkP2" +
                "tV2fF3p9XsV+Hp20+70dl3O/H/1xHe737cfrXp/bj//L9fnDP2" +
                "/rfj/6s31/Xqf7fbF4l+/24cnwxGZb3Wd29iA7e+Jn1AKDeTnT" +
                "I1jP48y+/Bm1WUlNMSuJWHqqXv+c7/cff78Jz0uPfrnM+330XN" +
                "D3Z9vP/7OfBaW/tqeetb8/94d9m211n9nZg2xYWs8DDMiBF1Uc" +
                "KcXZLmFoBrN5JCuJ2NgLR8/d6on9QXb/on9hs61vYmb5Z5+R4X" +
                "H28Ii1jOp7oQiI5IwaZ9aeK1g/K9GOGQEKIrPycS+c3T3tnsZr" +
                "1nzmx8x2yeIcrc0M4LGqck7GQUXOYgWRV9G1hzp/qabmce7+uD" +
                "+22dZxr4/dhgeZiLOHR6xl1OnfUxAQyRk1zqw9V7B+VqIdMwIU" +
                "RGbl4144246tu29OWDb4cJ8NzuDM2ucyx7LDc0ssGdFVQVeMsK" +
                "+EH+OxY2bkTs7ro70Pae8/r/vz5+K7dpWte3Svuldln/kxs+0e" +
                "tTS6jAERx1lW6bOftQ5UJWdqTeQqoWatyz3cfcr8LX+6gVfKFW" +
                "lv35+X/T6k7ef6+9nd7+7bbKv7zIYHmYizh0eszaiMwRVeoxnx" +
                "VGRFUZWsn5Vox4wABbFb5YPikH3YHdps6xg7dBseZCLOHh6xNq" +
                "MyBld4jeaXObP2XMH6WYl2zAhQEJmVD4o1+2ru9+7bq7vfa9yX" +
                "9/zZfj9qP9/bz/d3cz+HZ8Mzm211n9nwIBNx9vCItYw6vVsXBE" +
                "RyRo0za88VrJ+VaMeMAAWRWfm4F85u12f7edR+Ht2e67Pf6Dds" +
                "ttV9ZsODTMTZwyPWMqqzKwIiOaPGmbXnCtbPSrRjRoCCyKx83I" +
                "tk7/Q7Nts6xnbchgeZiLOHR6xl1IlfEBDJGTXOrD1XsH5Woh0z" +
                "AhREZuXjXiR7t9+12dYxtus2PMhEnD08Yi2jTvyCgEjOqHFm7b" +
                "mC9bMS7ZgRoCAyKx/3Itl7/Z7Nto6xPbfhQSbi7OERaxl14hcE" +
                "RHJGjTNrzxWsn5Vox4wABZFZ+bgXzh42h02bbR2fpTbdzh5kw9" +
                "J6Hl4LjOl5bZNROFKKs13C0Axm80hWErGxF46eu9UT+6Mdx+On" +
                "99pTz7rHcDKc2Gyr+8yGB5kcLw1UMQM4kAEUjnBd5lSMjIK4dq" +
                "RKtGNGYH4esTIq5u7T8/zrdp2te2zf2b5js63uMzt7kA1L63mA" +
                "ATnwooojpTjbJQzNYDaPZCURG3vh6LlbPbE/2nG7Ptvvm+3/d7" +
                "Trs3w8+us2XJ/ff/O23s8//Po27OfDr9rfO27m34/a/b5sP+cH" +
                "8wObbXWf2dmDbFhazwMMyIEXVRwpxdkuYWgGs3kkK4nY2AtHz9" +
                "3qif3hjtv9fpH3+/ByeGmzre4zGx5kIs4eHrGWUaffdwUBkZxR" +
                "48zacwXrZyXaMSNAQWRWPu6Fs/utfstmW8d3eVtuw4NMxNnDI9" +
                "Yy6vS+UBAQyRk1zqw9V7B+VqIdMwIURGbl414ke97PbbZ1jM3d" +
                "hgeZiLOHR6xl1IlfEBDJGTXOrD1XsH5Woh0zAhREZuXjXjh7OB" +
                "1ObbZ1vHZP3YYHmYizh0esZdTp/hAERHJGjTNrzxWsn5Vox4wA" +
                "BZFZ+bgXzi5+s/49vVm+O2vHSkf3vHte9pkfM9sli3O0NjOAx6" +
                "rKORkHFaUONMKYiq491PlLNTXPxP0vGtFT5g==");
            
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
            final int compressedBytes = 1127;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vE0EQdZkakKAgSgFISKEIAkUIiYa7KwBFQJ82ShM6lI" +
                "YGrChGcYFEgVA66PNbkPIL0tPmB+B4NH5vdnaNvzBOPHfa3cyb" +
                "mTdvN3e+PUtutfxR/WgVj2G+caNGOy64Ur5R+GepYcoZ/JzMN2" +
                "7U6GpSvlH4Z6lhuuPT+1YcsZ4LcdRr9VoeExw92zkLMRhLFeCR" +
                "rHyMotrrWZoBK7B1bU6qslQ/l1NCBiuwWq+6yD4mOHq2cxZiMI" +
                "Itq3FVK+VjFNVez3yUVWDr2pxUZal+LqeEaO32TZ/bvj7Jld6+" +
                "Nfjr2jzvsPaNRf8M6D6Nz8FYzwVez2exBjNdz+exBlN8nq/32u" +
                "323VH3n4e/HMOdXnuQYX7ca08cen9Guv+yw2k/HJPvHludrR7y" +
                "aKz84qdk92VcZRPv53frXellVExsIIiEnxFuaS6zcl0wwOMjSj" +
                "W9dp/B+lmJnTEzQEFa2dbjuXB0vG/O4Xn0OtZgfs+jDMMVfB71" +
                "kRk9j+J+j+tzca7PWM+436/S/f75wzJcnwff4vpc1OPwJNZg0q" +
                "PZb/all1ExsT2CaFg2nxsqIAYostiT87Od47ARXE09XknKjbVQ" +
                "dj9be2J9EF3v1DvSy9h/F91RGwgi4WeEW5rLrIP3XcMAj48o1f" +
                "TafQbrZyV2xswABWllW4/nwtGxX4r952Xbfza/r8KT4ePa5dh/" +
                "dt/F9Rn7z3kd1V61J72MiokNBJHwM8ItzfWszMEZmmMj0tMyWx" +
                "arkvWzEjtjZoCCdLa2HhQn0Z2qI72MfV9HbSCIhJ8RbmmuZ2UO" +
                "ztAcG5+v6bX7DNbPSuyMmQEK0sq2HhTb6Hi+x34p1jPWc9nXc/" +
                "Lv62I90/U8OJn8+mxOm1PpZVRMbCCIhJ8Rbmkusw7ewQwDPD6i" +
                "VNNr9xmsn5XYGTMDFKSVbT2eC0fHfv5/v2/G82jY/V5tV9vSy6" +
                "iY2EAQCT8j3NJcz8ocnKE5NiI9LbNlsSpZPyuxM2YGKEhna+tB" +
                "sY2O6zP2S7FfWp79Ur1Sr6QewQRHz3bOQgzGUgV4JCsfo6j2eu" +
                "ajrAJb1+akKkv1czklBLXj+vzX3ydXx0O+Lz0e6VvV49bMjguu" +
                "lG8U/llqGEPteXWexwRHz7Yi1rLeYRXgUZ5hmdrrWZqBVcmRNi" +
                "etlWP1WocjWrv7xed23k73P+q8Wd63o2a9WZdeRsXE9giiYdl8" +
                "bqiAGKDIYk/Oz3aOw0ZwNfV4JSk31kLZ/WztifVBdO73sEdb+l" +
                "f36xifx4Pfwx69muv78kL9HrbZaDakl1ExsT2CaFg2nxsqIAYo" +
                "stiT87Od47ARXE09XknKjbVQdj9be2J97Izd9fkiviWa+G4Zer" +
                "+PxRT3+8V1utlsSi+jYmJ7BNGwbD43VEAMUGSxJ+dnO8dhI7ia" +
                "erySlBtroex+tvbE+iC6PqvPpJex/+50pjYQRMLPCLc0l1kH72" +
                "eGAR4fUarptfsM1s9K7IyZAQrSyrYez4Wjc0f3e3wOTnz8AYal" +
                "X0s=");
            
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
            final int compressedBytes = 1164;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj1rHFcUFTjgP2FIkRgMTrMkRXCnHe0vMKmM3Lt2/kCESY" +
                "Tl2mUwuHZtg8HgPxAwtpukUJefoDpaXa7OOfe+Nx6Nx2TB9y3v" +
                "je7XOedeZjXLSnt7ef32616tz177/9j2M0d7Vh9vTswzpnEgC/" +
                "rHGTwyjt/jj9VTVP7xV91dS656v9c8d2cd3T7fN46+nz/Po+/O" +
                "9w8N/0/n++fkvbWQ7m8/EV9dEe9m8vx4pfo72/Pg8ODQTru6z2" +
                "x4kIk4e3jHWkZ1dkVAJGf0OLP2XMH6WYl2zAhQEJmVj3vh7OHD" +
                "8CFO2nzmx8l2y+Icrc0M4LGqdk7GQUXOYgWRV9G1hz5/q6bnuZ" +
                "zA++F9yrzwmR8n2y2Lc7Q2M4DHqto5GQcVOYsVRF5F1x76/K2a" +
                "nieqktjzEdznU36XTMuatrZYEW8K/pIaPm+dvKyn9KLzfFUzqM" +
                "9Lu/F5qbUe/1l32ZLr9xc1g3q/L/t+f/R07vu95rnkPNdv12/t" +
                "tKv7zIYHmYizh3esZVRnVwREckaPM2vPFayflWjHjAAFkVn5uB" +
                "fOvvr9efJuF+7P9eu59+f69ZT78/jf5Z7v9X1dPY925/NnzfNL" +
                "f56vz5/z1+bh5qGddnWf2dmDbFhazxsMyIEXVRxpxdluYWgGs3" +
                "kkK4nYmIWj5271hflwx1d9vx//8jW834/v1u/Peh7VPGueNc+a" +
                "Z82zvl+qefbm+ejF/PtzuD/ct9Ou7jMbHmQizh7esTajMgZXeI" +
                "1mxJciK4qqZP2sRDtmBCiI3SofFGt2fb+07BoeDA/stKv7zIYH" +
                "mYizh3eszaiMwRVeoxnxpciKoipZPyvRjhkBCmK3ygfFml2/P+" +
                "v5Xs/3r+j5fjgc2mlX95kNDzIRZw/vWJtRGYMrvEYz4kuRFUVV" +
                "sn5Woh0zAhTEbpUPikP22XCWnlEXPvPjZNs9amlU0ZrPwjNnUq" +
                "RY6ae/WmqzSs7UmsjVQs1axz2XE3g3pL9Xms/8ONluWZyjtZkB" +
                "PFbVzsk4qMhZrCDyKrr20Odv1fQ8zn1w7+CenXbdxszyn/1Ehs" +
                "fZwzvWMqrzKwIiOaPHmbXnCtbPSrRjRoCCyKx83Atnb1ablZ12" +
                "vfjufuV29iAbltbz9lpgXP6tYsUoHGnF2W5haAazeSQridiYha" +
                "PnbvWF+SB7fX193U67bmNm+c/qQTYsrefttcDwWTgGV/PSONst" +
                "DM1gNo9kJREbs3D03K2+MB/uuD4vLfv509f+qW0/c7RntddYzq" +
                "fr90+ncSAL+scZPDKO3+OP1SM8f9v2M0d7Vh9vTswzpnEgC/rH" +
                "GTwyjt/jj9Uxa326PrXTru4zGx5kIs4e3rGWUZkXCIjkjB5n1p" +
                "4rWD8r0Y4ZAQois/JxL5xd39d9+VXzXHbtv9kNjP+zu7n6h4/D" +
                "x7bP/DjZblmco7WZATxW1c7JOKhodaARxlR07aHP36rpeaIqXi" +
                "d36j265Kp5LrueXKsZLDrPb2oGS676/+T5a/Ns88zO7RU+t3F6" +
                "JsdbG1XMkDng0UhWwZxRZ0RBHLU4tRvFRpwVau8tHs3e+w+BNz" +
                "LD");
            
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
            final int compressedBytes = 1470;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmcGOlUUQhe9DmOjChIWamAhERxdGXfnzszG+A2BmAwGSie" +
                "xc4GRIwLD2AZj3gAfiJYS/KM53qnqcDLlRNH3/dPdU1TmnTnVm" +
                "mLmX9XQ93e3W03U74xVRxNrXN0jWR0ssqUlVWWW80l2wZ/VZVV" +
                "QXV7tP49qq06HPPurj6N3u+ItX6+PjT1X7/dfdhV7Hn7xalwf5" +
                "b16tb1v2891eXseXzql/dUG9z1rm6wvxv3tzzvsc3OfJn/M+5/" +
                "fnvM/58/7397k+WB/EHmfmIu4ZoRU5n0sdhFFWLFZGdcYjDUew" +
                "W1a6k6qtu0j1Pq0/uh9OvL5sf6m8fJ2LvHbGo4g6rtk7qI/rjH" +
                "w42nMjVH718NJZfSt2pHr+q8949py7/8Hr4aV/p+/893Ofv4+W" +
                "G8uN2OPMXMTKCKk6M1yV21WpQUZyHFEfV3YVd0n/dOITU0EO6r" +
                "TeT44L+s5yJ/Y4t9qdjJURUnVmuCq3q1KDjOQ4oj6u7Crukv7p" +
                "xCemghzUab2fHBf04XIYe5xb7TBjZYRUnRmuyu2q1CAjOY6ojy" +
                "u7irukfzrxiakgB3Va7yfHBf1oeRR7nFvtUcbKCKk6M1yV21Wp" +
                "QUZyHD/u2b13Bv3TiU9MBTmonb2fHBf07eV27HFutdsZKyOk6s" +
                "xwVW5XpQYZyXFEfVzZVdwl/dOJT0wFOajTej85Luhby63Y49xq" +
                "tzJWRkjVmeGq3K5KDTKS44j6uLKruEv6pxOfmApyUKf1fnJc0C" +
                "fLSexxbrWTjJURUnVmuCq3q1KDjOQ4ftyze+8M+qcTn5gKclA7" +
                "ez85dvTxh/0vqT9+zq+efnmBv78+esv/6R/9+/mD9+ldxHp5vR" +
                "x7nJmLuGeEVuR8LnUQRlmxWBnVGY80HMFuWelOqrbuItX7tP7o" +
                "foA+WA9ij3OrHWTcM0Ircj5XcqXx9i4OqMLKqM54pOEIdstKd1" +
                "K1dRep3qf1R/cD9PP1eexxbrXnGSsjJOujJVZ+TVVl2ZUV8npP" +
                "1+gqqvtE7sQnpgL7c1Vmdaxe8/37fv+/o7+efr+brz2+nv4w7+" +
                "Cdf78/W5/F/vpULmPtiWR9tMRih95DGa90F+xZfVYV1cXV7tO4" +
                "tup06LOP+jh6+P354/w+2+fror+P5mt+Pj//v/i/cZ/r0XoUe5" +
                "yZi7hnhFbkfC51EEZZsVgZ1RmPNBzBblnpTqq27iLV+7T+6H6E" +
                "vnb92vXY43xdiyi/zl2IrDPDVblUzbtwBVU64qye3Xtn0D+d+M" +
                "RUkIPa2ftxFqLnz/ue/3/z7nI39jgzF7EyQqrODFfldlVqkJEc" +
                "R9THlV3FXdI/nfjEVJCDOq33k+OCvrncjD3OrXYzY2WEVJ0Zrs" +
                "rtqtQgIzmOqI8ru4q7pH868YmpIAd1Wu8nxwV9tBzFHudWO8pY" +
                "GSFVZ4arcrsqNchIjiPq48qu4i7pn058YirIQZ3W+8lxQT9Zns" +
                "Qe51Z7krEyQqrODFfldlVqkJEcx497du+dQf904hNTQQ5qZ+8n" +
                "x46ev4/2/Pvo/nI/9jgzF7EyQqrODFfldlVqkJEcR9THlV3FXd" +
                "I/nfjEVJCDOq33k+OCfrw8jj3OrfY4Y2WEVJ0ZrsrtqtQgIzmO" +
                "H/fs3juD/unEJ6aCHNTO3k+OHT1/3vf8fvPKeiX2ODMXcc8Irc" +
                "j5XOogjLJisTKqMx5pOILdstKdVG3dRar3af3R/QD9Yn0Re5xb" +
                "7UXGygjJ+miJlV9TVVl2ZYW83tM1uorqPpE78YmpwP5clVkdc/" +
                "r2efIv81PM+Xnye/z/cYfzDubn8/Pz+Xmf8z7Pfb95b7kXe5yZ" +
                "i1gZIVVnhqtyuyo1yEiOI+rjyq7iLumfTnxiKshBndb7ybGj5/" +
                "fn/Hmf9znvc97nO34ecnW9GnucmYu4Z4RW5HwudRBGWbFYGdUZ" +
                "jzQcwW5Z6U6qtu4i1fu0/uh+fOL2/ui3+S5nvj+a/37O+5z3ee" +
                "59/gWAz73X");
            
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
            final int compressedBytes = 676;
            final int uncompressedBytes = 4705;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtlE1qG0EQhecggSycQCDZmGQRsm3mJBbSVkK6QHQWydIxLU" +
                "2p+N6rbgiCeOjqdL2femWC27qtp6mt2+Oepnzlv7PCSFw7eqq2" +
                "d1UPVaTGGfVzZ3fxlJpfk/jG6kCCuq3PI7Gzp+n4/XY+Hb+A/T" +
                "1MT/0cX27nx6D/63Z+d91v03/5OX7+B/76pN/XrvPzKf2f5ff6" +
                "1t6ixp29eNOBCa4dPVXbu6qHKlLjjPq5s7t4Ss2vSXxjdSBB3d" +
                "bnkdjZ8/v8HvV+x0+84k1NpuKjgwo3XOnScaRPoTNrzuoCjpbq" +
                "27g3uCb03UdzCvs0n6Le7wd24k1NpuKjgwo3XOnScaRPoTNrzu" +
                "oCjpbq27g3uCb03UdznN32bR817uX/7j7fdGCCa0dP1fau6qGK" +
                "1Dijfu7sLp5S82sS31gdSFC39XkkLuxN20SNe8E2+aYDE1w7eq" +
                "q2d1UPVaTGGfVzZ3fxlJpfk/jG6kCCuq3PI3Fhb9s2atwLts03" +
                "HZjg2tFTtb2reqgiNc6onzu7i6fU/JrEN1YHEtRtfR6JC3vVVl" +
                "HjXrBVvunABNeOnqrtXdVDFalxRv3c2V08pebXJL6xOpCgbuvz" +
                "SOzs+Tpfo97vx9/WK29qMhUfHVS44UqXjiN9Cp1Zc1YXcLRU38" +
                "a9wTWh7z6aU9jn+Rz1fj+wM29qMhUfHVS44UqXjiN9Cp1Zc1YX" +
                "cLRU38a9wTWh7z6aU9iX+RL1fj+wC29qMhUfHVS44UqXjiN9Cp" +
                "1Zc1YXcLRU38a9wTWh7z6a4+x2aIeocS9/Cw75pgMTXDt6qrZ3" +
                "VQ9VpMYZ9XNnd/GUml+T+MbqQIK6rc8jcWHv2i5q3Au2yzcdmO" +
                "Da0VO1vat6qCI1zqifO7uLp9T8msQ3VgcS1G19HomdPX0A9cTA" +
                "Ag==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 6, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 0, 35, 4, 0, 36, 0, 0, 37, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 6, 0, 0, 39, 7, 0, 0, 40, 41, 8, 0, 0, 0, 42, 43, 0, 44, 0, 0, 45, 0, 9, 0, 46, 0, 10, 47, 11, 0, 48, 0, 0, 0, 49, 50, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 0, 11, 0, 0, 0, 0, 0, 0, 52, 1, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 2, 0, 14, 15, 0, 0, 0, 53, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 1, 0, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 54, 0, 0, 0, 20, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 56, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 57, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 58, 0, 59, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 60, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 61, 0, 24, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 0, 11, 0, 2, 0, 12, 0, 0, 0, 0, 0, 13, 0, 0, 63, 14, 0, 64, 0, 0, 0, 0, 65, 0, 0, 66, 67, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 14, 0, 0, 68, 15, 0, 0, 16, 0, 0, 69, 17, 0, 0, 0, 0, 0, 25, 26, 1, 0, 27, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 29, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 30, 31, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 33, 0, 2, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 35, 0, 0, 0, 5, 4, 0, 36, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 38, 3, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 39, 16, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 1, 0, 0, 0, 1, 6, 0, 0, 5, 43, 7, 0, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 70, 45, 0, 46, 47, 48, 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 54, 0, 1, 0, 55, 0, 0, 8, 56, 0, 57, 0, 58, 0, 0, 0, 6, 7, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 59, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 3, 0, 8, 60, 61, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 62, 0, 0, 0, 0, 0, 71, 0, 0, 0, 63, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65, 66, 17, 18, 0, 0, 0, 19, 0, 0, 0, 0, 0, 20, 0, 0, 67, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 68, 25, 26, 0, 0, 0, 69, 70, 0, 0, 0, 4, 0, 0, 5, 0, 0, 72, 71, 1, 0, 0, 0, 27, 72, 0, 0, 0, 28, 0, 0, 29, 0, 0, 0, 1, 0, 73, 0, 0, 0, 0, 0, 0, 74, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 19, 0, 0, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 11, 0, 73, 74, 12, 0, 75, 75, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 76, 0, 13, 77, 78, 79, 80, 0, 81, 76, 82, 1, 83, 0, 77, 84, 85, 78, 86, 14, 2, 15, 0, 0, 0, 87, 0, 0, 0, 0, 88, 0, 89, 0, 90, 91, 5, 0, 0, 0, 0, 0, 0, 92, 93, 9, 0, 0, 2, 0, 94, 0, 0, 95, 1, 96, 0, 3, 0, 0, 0, 0, 0, 97, 0, 2, 0, 0, 0, 0, 0, 98, 0, 99, 0, 0, 0, 0, 0, 0, 0, 100, 101, 0, 3, 4, 0, 0, 0, 102, 1, 103, 0, 0, 0, 104, 105, 0, 0, 10, 0, 106, 4, 1, 0, 0, 0, 0, 1, 107, 108, 0, 0, 4, 109, 0, 6, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 111, 0, 0, 0, 0, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 5, 16, 0, 17, 112, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 0, 0, 113, 7, 0, 114, 115, 0, 11, 0, 0, 0, 12, 0, 116, 0, 0, 20, 0, 2, 0, 0, 6, 0, 0, 0, 4, 0, 117, 118, 0, 5, 0, 0, 0, 0, 0, 119, 0, 0, 0, 120, 121, 122, 0, 7, 0, 123, 0, 8, 13, 0, 0, 2, 0, 124, 0, 2, 3, 125, 0, 14, 0, 126, 0, 0, 0, 15, 9, 0, 0, 0, 0, 79, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 127, 128, 0, 129, 130, 131, 0, 132, 0, 0, 0, 133, 0, 0, 0, 0, 1, 23, 24, 25, 26, 27, 28, 29, 134, 30, 80, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 135, 46, 47, 48, 49, 136, 50, 51, 52, 53, 56, 57, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 137, 138, 139, 0, 140, 0, 59, 4, 81, 0, 141, 7, 0, 0, 142, 143, 0, 0, 10, 60, 144, 145, 146, 147, 82, 148, 0, 149, 150, 151, 152, 153, 154, 155, 156, 61, 0, 157, 158, 159, 160, 0, 0, 7, 0, 0, 0, 0, 62, 0, 0, 0, 0, 161, 0, 162, 0, 0, 0, 0, 1, 0, 2, 163, 164, 0, 0, 165, 166, 11, 0, 0, 0, 167, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 168, 1, 0, 169, 170, 0, 0, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 171, 172, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 85, 0, 12, 0, 0, 0, 173, 2, 0, 3, 0, 0, 0, 13, 0, 174, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 175, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 0, 2, 179, 180, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 181, 0, 182, 183, 0, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 184, 0, 0, 0, 0, 0, 0, 16, 9, 10, 0, 11, 0, 12, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 22, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 188, 24, 17, 0, 0, 0, 0, 0, 0, 189, 0, 0, 1, 0, 0, 18, 190, 0, 3, 0, 0, 7, 15, 1, 0, 0, 0, 1, 0, 191, 25, 0, 63, 0, 0, 192, 0, 193, 0, 194, 19, 0, 0, 195, 0, 196, 0, 0, 20, 0, 0, 0, 86, 0, 26, 0, 197, 0, 0, 0, 0, 0, 198, 21, 0, 0, 0, 0, 18, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 199, 0, 0, 0, 0, 0, 0, 0, 0, 0, 87, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 0, 0, 1, 200, 201, 2, 3, 0, 0, 0, 0, 0, 0, 202, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 203, 0, 0, 0, 204, 64, 0, 205, 0, 3, 0, 0, 0, 65, 88, 0, 0, 23, 0, 0, 0, 27, 206, 0, 207, 24, 28, 0, 208, 209, 0, 25, 210, 0, 0, 211, 212, 213, 214, 29, 215, 26, 216, 217, 218, 27, 219, 0, 220, 221, 6, 222, 223, 30, 0, 224, 225, 0, 0, 0, 0, 0, 66, 0, 2, 0, 0, 226, 227, 0, 228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 229, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 29, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 230, 4, 0, 0, 0, 0, 0, 0, 30, 0, 0, 1, 231, 232, 0, 1, 31, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 233, 69, 0, 0, 234, 0, 0, 235, 236, 0, 0, 0, 0, 32, 33, 0, 0, 3, 0, 0, 237, 0, 238, 0, 89, 239, 0, 240, 0, 0, 34, 0, 0, 0, 241, 0, 242, 35, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 243, 0, 244, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 7, 0, 0, 0, 0, 39, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 245, 37, 246, 247, 38, 248, 0, 249, 0, 0, 0, 0, 39, 250, 0, 40, 0, 251, 0, 40, 252, 41, 0, 253, 0, 254, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 256, 0, 0, 257, 0, 8, 0, 0, 42, 0, 258, 259, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 260, 0, 261, 262, 263, 264, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 265, 43, 9, 0, 0, 10, 0, 12, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 70, 0, 0, 266, 0, 0, 0, 267, 0, 0, 0, 0, 44, 0, 0, 268, 269, 270, 0, 45, 271, 0, 272, 46, 47, 0, 0, 8, 273, 0, 2, 274, 275, 0, 0, 0, 0, 8, 48, 276, 277, 49, 278, 0, 0, 50, 0, 3, 279, 280, 0, 281, 0, 0, 0, 0, 0, 0, 0, 282, 283, 51, 0, 0, 52, 0, 0, 284, 0, 0, 0, 285, 0, 0, 286, 1, 0, 0, 0, 5, 2, 0, 0, 287, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 288, 44, 0, 0, 0, 0, 0, 71, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 289, 0, 0, 0, 0, 2, 0, 290, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 291, 45, 0, 0, 0, 292, 0, 0, 0, 0, 0, 0, 293, 0, 0, 0, 0, 0, 54, 0, 0, 55, 0, 294, 0, 0, 0, 0, 0, 0, 56, 0, 0, 36, 0, 0, 0, 37, 5, 295, 6, 296, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 297, 298, 3, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 299, 0, 300, 0, 301, 0, 0, 302, 0, 0, 0, 303, 0, 0, 57, 304, 0, 0, 0, 0, 0, 305, 0, 0, 7, 306, 0, 0, 0, 307, 308, 0, 46, 309, 0, 0, 0, 58, 90, 0, 0, 0, 310, 311, 59, 0, 60, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 91, 0, 0, 0, 2, 47, 61, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 312, 0, 48, 313, 49, 72, 0, 50, 0, 0, 0, 0, 314, 63, 0, 0, 315, 64, 65, 0, 51, 0, 316, 66, 317, 0, 52, 67, 318, 319, 68, 69, 0, 53, 0, 320, 321, 0, 70, 54, 322, 0, 55, 0, 0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 323, 0, 9, 324, 0, 0, 325, 326, 327, 72, 0, 0, 0, 3, 0, 0, 328, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 329, 73, 0, 0, 0, 0, 74, 0, 0, 38, 0, 0, 0, 0, 0, 330, 59, 331, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 332, 6, 0, 0, 0, 28, 0, 0, 0, 333, 0, 0, 0, 0, 0, 334, 0, 61, 335, 62, 0, 63, 336, 337, 0, 0, 64, 338, 0, 65, 0, 0, 75, 0, 0, 339, 340, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 341, 342, 92, 0, 343, 0, 0, 344, 0, 0, 0, 77, 0, 0, 0, 0, 0, 66, 0, 78, 0, 345, 0, 79, 67, 346, 347, 348, 349, 0, 80, 81, 0, 350, 82, 68, 351, 0, 352, 353, 354, 83, 0, 0, 0, 355, 0, 0, 0, 0, 0, 3, 0, 7, 0, 0, 34, 1, 8, 0, 0, 0, 0, 0, 0, 0, 69, 356, 0, 70, 0, 0, 0, 84, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 357, 0, 358, 85, 359, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 360, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 86, 73, 74, 361, 75, 0, 87, 88, 76, 0, 77, 362, 0, 363, 364, 0, 0, 365, 366, 0, 0, 0, 7, 0, 93, 89, 0, 0, 367, 0, 368, 0, 369, 370, 0, 90, 371, 372, 373, 374, 91, 92, 0, 0, 0, 375, 0, 0, 376, 377, 378, 93, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 379, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 380, 0, 381, 0, 0, 95, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 382, 383, 0, 0, 384, 385, 0, 386, 0, 0, 0, 0, 97, 98, 0, 0, 0, 94, 95, 0, 99, 100, 101, 387, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 388, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 389, 105, 0, 82, 106, 107, 0, 83, 390, 391, 0, 0, 0, 392, 0, 0, 108, 0, 0, 84, 0, 393, 0, 0, 85, 0, 394, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 395, 0, 0, 0, 396, 0, 87, 397, 0, 398, 0, 88, 0, 109, 110, 111, 0, 399, 0, 112, 400, 401, 0, 113, 402, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 115, 116, 0, 403, 0, 404, 0, 0, 117, 405, 0, 118, 119, 406, 0, 120, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 1, 2, 0, 6, 7, 8, 1, 5, 0, 2, 0, 3, 9, 1, 5, 0, 10, 0, 0, 5, 10, 11, 4, 12, 0, 13, 5, 10, 0, 6, 1, 0, 14, 15, 16, 12, 10, 17, 18, 16, 2, 16, 19, 5, 6, 16, 20, 3, 17, 10, 21, 22, 23, 24, 1, 0, 25, 26, 3, 27, 28, 1, 29, 30, 0, 5, 31, 5, 1, 32, 0, 17, 33, 34, 5, 1, 0, 8, 35, 36, 16, 2, 37, 38, 4, 1, 39, 1, 2, 1, 40, 41, 6, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 1, 48, 49, 3, 2, 50, 12, 51, 52, 53, 54, 0, 1, 6, 1, 55, 56, 1, 10, 4, 0, 57, 12, 58, 59, 17, 3, 60, 61, 62, 63, 2, 18, 15, 64, 65, 66, 20, 67, 21, 68, 5, 69, 12, 70, 3, 71, 72, 73, 0, 74, 1, 21, 75, 2, 76, 77, 78, 20, 2, 79, 18, 80, 81, 82, 0, 83, 84, 6, 6, 7, 2, 85, 3, 86, 87, 2, 88, 1, 89, 1, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 0, 6, 99, 7, 4, 100, 101, 102, 103, 2, 104, 105, 106, 0, 107, 108, 6, 109, 0, 110, 21, 7, 4, 4, 27, 111, 112, 10, 7, 113, 4, 1, 1, 114, 8, 9, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 10, 18, 0, 125, 4, 1, 1, 126, 127, 2, 29, 6, 4, 0, 128, 17, 2, 11, 129, 30, 130, 131, 132, 6, 13, 29, 6, 133, 18, 1, 134, 5, 14, 5, 0, 135, 20, 12, 2, 136, 137, 138, 21, 20, 21, 6, 9, 139, 1, 7, 140, 141, 22, 142, 3, 143, 144, 5, 145, 146, 147, 148, 149, 150, 25, 31, 151, 152, 8, 7, 153, 33, 24, 3, 154, 155, 8, 156, 8, 157, 158, 159, 160, 5, 161, 3, 162, 163, 164, 35, 11, 165, 166, 167, 36, 168, 2, 6, 3, 169, 170, 6, 39, 171, 172, 2, 173, 174, 175, 40, 22, 43, 176, 177, 2, 178, 56, 4, 9, 179, 180, 13, 44, 181, 182, 183, 184, 185, 186, 15, 0, 187, 188, 5, 5, 7, 20, 189, 190, 191, 9, 192, 193, 11, 1, 194, 195, 196, 18, 0, 3, 30, 197, 28, 22, 198, 2, 17, 12, 9, 4, 7, 23, 1, 199, 9, 200, 201, 0, 7, 8, 202, 6, 203, 1, 204, 205, 12, 206, 22, 207, 208, 2, 0, 209, 210, 211, 25, 8, 6, 17, 2, 2, 212, 8, 30, 4, 213, 214, 4, 215, 216, 45, 217, 10, 218, 219, 220, 1, 221, 222, 223, 8, 22, 47, 3, 11, 224, 46, 14, 225, 226, 9, 227, 228, 46, 229, 230, 231, 232, 233, 234, 20, 235, 236, 237, 238, 239, 3 };

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
            final int compressedBytes = 1589;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXU1uGzcUfqRplXa6mKRCoHTFOEKRRVdFDkAXReBllunOgL" +
                "PrJegALZwDdNGdvewt7Jskux6jnB+N5oecoTQcijN6D7AsS4o0" +
                "fD/f+1duyC3c/ahO9e3iV/Xqo/wOxKX89PPzO/gXyOv/fuP3fM" +
                "Fervjff31af5O/yCsq38Ib8uz3p5f/XAPSHIi2HjnXP7z2CKvc" +
                "V/qH6B+JrJspJS35m9RlBvJ/o5WZ65Nq/GNUrRJYgCDyJ6Gfug" +
                "O4Vuzsnr6A5YqrhWQCLuBKyDv969nqCV4h/g2mm9T/rCC9XdDU" +
                "/yxS/7NeF/7nkfF7oEzz/1b7n4fU/0Dufz4+Lefgf+QtAZWkaM" +
                "q1WREpc7NizxWcASFSWyFJsZcTtRa3MkmfF5ox+kVLhfgbDX48" +
                "aPw4L/FjrUWkYQKE0vpLKc/w4zHHD7iKCT8kOYHHF6X+XUkN/Z" +
                "LD9z9k+ncrMy+Q6t/pRv9Iqn9ncIn6Fwl+Cgt+ftEw8TXFzwsz" +
                "fmL83u9/vtb8zzfk34j481kjZwN/iA1/yAj4w4u4tyDBy8yHpD" +
                "63eAlL3XP2V+6M50Kslz0JaisSEhIS0qziv+76NyzS+O9Pe/27" +
                "UihLGvFE5VG6cbGXefxwDT7ih5vTWvz6wRT/d9VP/iAP0Jc/UM" +
                "wfkMLT2/7kweOnkcxMecVqs3uisOhENI2cRlH/ruavJ3vY/yyI" +
                "FjeMFRhLGrBsyW9mkL+V/Yuk2r9Yp9q6rT9m/Yv3Rf8CIuxfVI" +
                "VD6eahSzC1JksLFAiRSF0qRVz1p/0iDsH737J5gTyE/zP3Pzf4" +
                "cZ3hB/Y/kcLh/wHc8+TnXwz+s//yMvyTBPvPDWWgji/0En8cun" +
                "9cxf9zU/x4tYkfy8+XY3x+6/zQOr+MsX8eBX4hTZbS/qfaff6C" +
                "xDt/sc3fXOJvc//9nbF+8c5Qf+zo33/ZzI9dTLP+wXsdjRzu/z" +
                "3q7+e2/pLZ629DXqb6f31+AGY6P3D0ROzzIy2rzbUhkd3xwwHw" +
                "ZnLzL931i2r9c7Gtf4oZ1i+Yi3gDks274BTReIGCW/9Z+uw/T0" +
                "T5o6We/r3j/sPh4r/B5++eP3A4v9gf8OT0+ecvfLH0vw3AUut/" +
                "8zYHi/vnNQ7xkQskfdcvLNcfESV76+9BSPix/875hUD7X0Pq56" +
                "/b9XNWq5/Dpn5+v62fk976+e7554CUd/OZTIfPFv2b6fyEQ/8D" +
                "RCG/B4P83POPqcxfzEq8SKFFKoygNd0A3SlvNh5vVbKB8hJluQ" +
                "3Su0Mbq9NlmTNg+n1F+QoqKrCeO5mTtIjDl1nnOifFD2ffe3yw" +
                "8lJDCKyGatB1pQ6peItGUMt316G4gT0CeJD9z6I/7I0gB1uC5x" +
                "wqx0812aSddqij36MIZ3aGFKd3XcixJq7rJ7A0hhm8DUyk4uY9" +
                "xC1jnGUOFLl+y5j5z0d4D7Wb5FSfW/ItX7ZrDJOMHf/EkXAlnp" +
                "SAD7YLVwOZIH7RmrST2hOkxhduNIGh/35WVQyylwCEDWC64iey" +
                "j6RHjdJ9o430oty7RaxIG8bIYXrAdrVG2S96Otw5xReaTQ8/Hf" +
                "2vqPhf2iUE1f6DNxlCYz7qKNES33KB+1JiDkjR6s7OWqSQe0dF" +
                "o2Egj+1cu+xPh40fUP+QKtS7/2iaH2/Nb+D8xFHoL9pPdPx32f" +
                "+z7A+Ovf/XcsqJFSBw/hjtF89/6HoH2PY3W1abm56//RmG+hP1" +
                "9c83Ud9rf9L7/DLGz5OSv8Dv/zou/FVHfn6kKfA/8P6cn/yLWz" +
                "1g2P3JKUftLvyLPcDYd//2cPqP1B/UMt9wMRdXPhf/Y9z/BfP+" +
                "r3Da/w37/ZNjan+w/eVB8nPf/z0q+U0HZBv7j9fl/mO2y+60/+" +
                "i7/hYUZJvzZ6ryRGv+jM3BD4yeOszUfyIh9eMH1PGDNeZXZTg7" +
                "cLZK1s52Kl1UUyLEhFdUQELQRPJaDTkm/WF1jBJVCJMWPMrjV6" +
                "/f/zFSfi2PJv+gPfxPVNLF/2jtL15vETX/k1DBhRz3UpEc8Hsb" +
                "UBr0j0COvwDB9I8e7PwG/5X/h4zLtv+KIaWPCNGIGXrFaPH0JL" +
                "8/zCX/s2CjsrE0WNCDudAo8rcgYJPbvLxBpzgq8/tJIOuQjoro" +
                "WO8a6PsHTNf/P36Wv5M=");
            
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
            final int compressedBytes = 1133;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUl2nDAQLSnETxkWenm9cHbKcBA9r3wMH6WSlZc+gq/imw" +
                "XS7m6gUSM0S9Rf9GADUk2/qgQNAAM6IBAI+0Mf+XJ4FaDe/6KA" +
                "v3/kwwvq4fVDBwzEAfC8IwrQpL44FplCn19MWxB8wCcalZN/MB" +
                "grX5y+9Nuj7eHFyvcmwcIeDmsIWbauEMWW5GHDAYTJc/R00wtN" +
                "t6M/jwCxyF9I+YtAIFhAmnmIX299IhCVryJb4L+Dbf3+EKz+d5" +
                "BfhdPBAT6KpUTHj1NTs7HwNP+5/pD6n/wgpRLKB5IKEvff+pSR" +
                "JVmAELn0I+TXnkxkEjniH7RY/4ufAdJTm5F/wSh/M+k6r/0LOP" +
                "9ylX+lpfw7OA8hb/gTuyhQee1P/Q+BQCXU5uxFPVCR2FIm3cz/" +
                "3CX/c76S7NtQHGFZhchrnLZ9pxeL826qTa1MojNOVa6KWF7/GQ" +
                "oqc4erjOalxElIDN6sZMK4EkAXKhZSTYlmLXOqnx/zrZ+R/xPy" +
                "+b+80T8KiHf9To4cyHdubE7+Tvpfzn9V/H6H/Jf8n0D5k7CFvz" +
                "EYf5P/QLBGTV0++lx/Xjvk5M0CSPMPiV/9hETvSn/gueN4L+EO" +
                "FNO/B4977p0UO/HKORzuBd7pTsFPeAT93L99uX+D709z/lHh6k" +
                "dF3G7Ln3vmj13ln33VX900sUYiBGHM4J9Xh4zQABPteefimWXT" +
                "2i86YtQPeux2FiTD7AxAiNU8VEoVtH6wLZ5Cn1ch/TvSI6EABh" +
                "QXysN4E9h+/Rz33L8A+W9VxjalVGekG1w6aMmtp7IjzWbrdJUi" +
                "Ea2MzzOO78ofwuQ2etHdOxV2eFf+iHn/Fvv4D7C/2Ji9FUQ3gC" +
                "f/5eJfEUh+cRlfzMdPUYmm0p9//s9t/8D1Bybkr5YLYG5yCvqd" +
                "U1n5N7v93M8/93u+2N//DSf5/+HMePpIe3wcnd8W1gPHCx20WF" +
                "hbA27wH+f68cF6MF++11l7OB338DP9w5L+2bv+Ybl+z+p5W/zH" +
                "cf5m+28bX2IO/WkiqOheOI4fGNv/qP3/iWvif5AyfvLyF9l/sH" +
                "9M/rzB30nGJ5ST/1zytx1/QbX81S7/Wdrfof6RzvxTnv8U3f/4" +
                "9s95oWwWIzCh/4f//VlwP6WlCw9gmPhb8B9l7T+iqPpJTb9oj/" +
                "jhV/L/NcXP1/jyyw1SLCDx+reilmo/kHP/tAiGqPGi1/9bZr3V" +
                "7v3HEmLtfLivtrJqm2G6nk1mHp9gTXaqqtkGrYAihKNyG9/Yf8" +
                "kjKX16PdeP7FQ/DqT+MtTPjn1QF7oXj7sWcJV7935bLrotWV4O" +
                "6rJbv47iTVY+/1rA7K+/ZfN8hfq6NYAf000XUpcednsqgNF8n5" +
                "8ZKe+XVP7HRQHyx3/+dmnAq5ANiOaubtWwH7D01YjX+aNo96+p" +
                "RH+EfXc0uZ/fXcPzs3Xx+i9d/hrrT1lR/JUlLTbm//H9t/Trj4" +
                "voN6j+TguhRr5Gzz+qqH09m4c3lI83iJ5Wfq/nt7Tx/Jluaw9w" +
                "KTf+AXRJtdo=");
            
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
            final int compressedBytes = 978;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuO2zAMlVWj0ABdaJFFC3ShFj2IMCeZo7BzgjlCjlp7kt" +
                "hxajm29aX8HpAgH9OUKYp8lBRHiB7t9ZEerbC6f1bCXD8xQhph" +
                "Rfep7N+ebffUfGlF06iToP6jpnsQKWFFLUhje5leJQ+LrjtYic" +
                "FHCfIlyTtOIGeOVMNTSP2u42dewv8gH9z/kwTLcvUf3f4e8p3o" +
                "HP8a+QJ9sqyef4kb/7qc4o5/HXv8yckJ9OSL5vOFnUkDhPhVy/" +
                "g10zcJyhKP9jcRm2Uz649/eiB2taYd9eoijQ4oL/bI+5blnvmj" +
                "MP4XqGkqtuFX+4/d0ww9OVdFsyWTpNdJfjzwx5ObP9KEP74ysY" +
                "6NPn485y/Yy/uO/yj6vcdPCvsFqd/y9n+A/JfZf3PPHyB+FAPU" +
                "H94V9AOJcjJMmNqHv6XQb9EzHqPBFqtf3ve83/q9k79c5V/OA3" +
                "+ZyH/08rqIkefmX/I5/wrD3zzrF96Rz8v+AAAAfONf+P1zKmn7" +
                "/fIfZ/4wb38pl2RkombYVNpHVTaTfiA/7PpwkRm0dupiSYjD1E" +
                "tJ7SeMkCPWbz75P1j9rItMPfquaXUNL2JhfwZYWj9tYZ7j2N9W" +
                "bOSQ8X9zp1CgK8iqv57iILf/AJWjQA4C/0X+BBA/AKBS/k7x89" +
                "f+/e8g6gdMyJZD+1v0H/gfAHAYYCYcSQbJzkvn5IpjAAAAQgSc" +
                "8u4fqDa1P+7+xzv5y/XLpPsf250FcyjdOfUDQFlgsX71lnT9Ks" +
                "D6mXJSWg5Md1VYpHr1Z9eG5juhBbAzfhl3/DJD/FKwHwfYXONH" +
                "H3z8a3gT7A//SVOq/wc13v9nTFSfrFuFrmTahUkbO/tVa6q3fw" +
                "DeXh8lBaUHAIBT/LToIHb1uxDc9+/Xsw/Dru4/+aT/dML+41t8" +
                "UC4THHbrUO5IUlP90axobDl8fohfNOSf09r885qz/+Pl3+DXXz" +
                "7/Wp7/ePRcu3n8ldv/R4iumZDw/ztyx39saS22fnp3xe9vuH9u" +
                "lFrvKYnGqoib0RqYAUifpxnvXwNY49fA0EhIUlp8Faaxf65+aT" +
                "oXPHcvf34XJPs119+3ZPyi/4ofb7Af6h+grGqlfeoDracPtYyt" +
                "A5QVjR7qR7mvfoy5f8l37gSbfFnPX+D+bQCPInKEQfvBOXbHv+" +
                "3rbxfzPSy80ow207i/y9j+iK4MzIFcdtKb7MgkAYMnkA//otnx" +
                "+57s98daZP//R4rWMxHuf6Ezx99N9ufO/yP036b8eS9PO+13Eu" +
                "1srJeXCzQPfJBu8gtoto2f++sf+accvae5NnSSnsKNfw/9/wC6" +
                "6ciu");
            
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
            final int compressedBytes = 855;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnd2N2zAMx2XVBVzgHvSQAdRNjE7SUYhOcCPcqD07iRE7dq" +
                "JvkfL/9xBckKNFixRFRrKi1ESvBmXVwu1PPb+O00s3vQwX9cAw" +
                "f9IrZdbyVulHeZrlf/Tfl/iWp0WcrvIuvPq/Eu2/p4+R3Op/cd" +
                "X/z8srm9D2vfpvR94k0L9bvyWVlyj7fbrbjzzsB2J5iGi3WFbK" +
                "nzaN+w/LYmQY/1t5yhl/ecR/AAAAALDBoAsAAEqdOdlHnQMAyM" +
                "T0XS0t7yw6JBxCFzTT/8aj/ghYf7DwK/b1o3R55vV337r9Y/P3" +
                "s/vP2fUX03/WUwO6552Bd+Asz56E65/+1kZaIYJRhJYm0P/HPf" +
                "8fp8vN/v813X03+X939/+5YH23f+Em/+trGT8r+c9JPnV8HM5s" +
                "/+rDY3zfZ5r9zQwCdASs84dE+09f5XfErxcO0hsjxYoRXNTPYc" +
                "8w+nppuzEf3e0Pysy/0uZvK07tPsewKl2/pU8lHBLTI/2pnP5D" +
                "Tfuhfov0f1PX/2s/vwQw/gBw4rSlvXe92FWWByAG+N9BZ1gZtS" +
                "8AICjBLpvkxK4fZqs/zVP9eW1fZ1x/NAXMC1ovUlz31fBJajpY" +
                "DSB+5UmAsR8B/lcbQhc0Tb+zREnL6NlbvextGv+J3T/jd35a6P" +
                "lh9L7/Ko0jnN9UN/6V2f9VY/6jSP8jRv5X3H7lz18cPK1vE7eP" +
                "/A2cOX9KMH9bgfM3h/zDZotr7dev4fb7iDy/+gNxHWxHrk0ZCs" +
                "qhD/7epZ1T9XA+IGg4fxvLtBw0f+4+f9u5yXcP+ZdH+37P/7q0" +
                "f6L4MRarlgGoX//Z4/Fvl/pv8B87W+j24epSv18JXC85if1l0H" +
                "+h+qtE+pdpf+SUrJcA+4+bhp6GDMf89Xzzj1VY/8ybu0rJP1zt" +
                "n3L/AQW0X9t+tb+/l/D8sLQ4Qcz6Pz5/yjD/Wdb33wDpvtzJGb" +
                "94xJ+287cK46TziBwJ7J/7/HHk/6BE/MtTP0mfJ5nr3/z5TTn8" +
                "n1jlb2UMpRn7ho4U1I6pgJVlMl//10/+/+8ofmP/FQDngHzmT4" +
                "HPP4ADmvj9DsxTIFf9C9aVJBv7hfx+CI5Ul5yKCAk3oqfIPlu3" +
                "hD5/att3GrluBZB/yGeE/vBf+L98WjwK7T/6gcb0");
            
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
                "eNrtXdGu0zAMdUKFisRDhO4DSDwExIdEfAmfYv7kfioruxtD6t" +
                "Z0aWLHOedhmnSvl9pxbB8nbYl2wGf/p6NdmGg3ourxAUAC055F" +
                "CtSei9vA0jBgBKFoFUSMHJbP+apiJB9v0hWn5fPddEoJ8wvxRY" +
                "55prRP3l3k3Y28tP6ANfjc2oVhK0upQmzkvfHz/Ncl/s1y8fuo" +
                "8Stc/0vu9f8c2X874k8J86/Z8yzaH/2LFvljXT7o8x/etsKcF7" +
                "3Y2PyXxu8HmFe/2o+fb+uHN/z/4wZ/zpOvOH/S/tOweeLvfAca" +
                "IhJ2BTRhiPj9UO3DfoOLfi0N7ogJaxEAgH5Qv//QFFytf3AMf3" +
                "9Kn1mD/UZHhv+EUv9JfetP0voL2M+1Wj867K+9/1yPv1ToP2P9" +
                "NMw/x9hft/+nivZvUz+NXb+gfwAAAAAAGwk+Nh/blYyv9PzSMO" +
                "ePJkpr9WtaZuWv/V8X+7u75/8f1r/eBv/rDT3t9pb6X+/6A2Ol" +
                "5zGvn/fkf8R/ZTBjPxwNrJbF69a/rHx83XAH0g4nLA9sLQRfYb" +
                "ECgE3+C/4pVmSxHZ0RUgUtb2D/3Vz9mn3+q4fzQ8IluwL/qe3/" +
                "uqHYT/75P1/9/4n9M9v5KxGQGREizDBW/ZMhzzvyZ7hPg3yVhV" +
                "n6/DvwB8RvoLT+7Hf+e+AP5ePo5V+l579u5dno/NW/zmlY/1VY" +
                "f6nzf8v593/7Ryv9I5PwNUKWv8tOVkshhQQ50Hm76qrE2Uk/rT" +
                "xSy92hagVHY/w+eTQYwB8OqT+aOhIPPZnN8twsLE/djp87RUYb" +
                "TZPp8VPn/QuAheUBAGgWP6XegyT9/qW+3/8UQU+t8o96+bfS/e" +
                "um9t+ekFelv8D8N85f/dJCnB81m/8Oe3596tB/I3Xw/jmsX/H+" +
                "Dfaf+17/h0UR5G/4LwAAQP8A/weKGCPQU/2I+w+FAmtzeQAwy3" +
                "/C0NMUCfunPWZ+0f6LQf2l++fgH8ryTzJsm29XhszkeQ70nqJL" +
                "P97YWDx55uvp69fPxJ6mSN8vxvgQftOXXw8z6KzFsEmx/Aj3L6" +
                "teP38A5C7uvQ==");
            
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
            final int compressedBytes = 4171;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW3u8DtUafteamTXfZRO55VKH7bJDKJHsHdIpSemydUMkVC" +
                "rZbuV6yjUpKuXSzT1RyK2LClEpql9KKE6qg1JKEc6pY/Odd601" +
                "929mf/Pti90fZ36/+WbNmpk1a+aZ53nf9a73UxoAARXiEKPHlU" +
                "pwOlSEGqwiZEITqAONoLFyHjRTGcnWt0A2vR1aQWtVp1mxDtCO" +
                "3kwHwcfQUT0JnegJtgxuhd5wh5IF98C9dD1jWi9lf7QpDIH74R" +
                "/wIFsCFCJQVjtEJ6tvkgRUgTPgLOV0OlPtTmpHOkA9dSRpqByF" +
                "hvSI1g7Og/Mjm2JzyAk6BS6ElnQ+tIG/w+VwhXKY/gnXK1Xoau" +
                "gCXdVa5G3oTnKjG9Vy9BF6TC0LefQoDIahyqMwktxOdNpe+RQy" +
                "oAJdyypDZagEVaEaHQzVlYlQE2qROpAF9aGBUp6qai6cqxyAC6" +
                "AFrQQ5dDbrB5fApXAZ7Um7xGbDlXAVBXqJ2gWug5ui9eAW6KFU" +
                "hF7QU5kEH8Gd0Af6wQDYAoNoVxgBD+idQAENGN0HOkTpZfQkOQ" +
                "5loJxSW/8MysOZtGq8pUKgNnkL6tINap1IFzgbzoGm0Fy9hbah" +
                "bWk+PUzvh4tIW7gY2qofQnu2AjrA1XAN5MINcKN+PdwMnaEb3E" +
                "Y6w+1wF9wNffWNLA/6w8BoNtynnA3DYRhbpbWmd8E4tQ9rTI/r" +
                "39OhMCqRiC5OJKAJa5TARWuTSHB8ta70TrZG1Kg0Kz4tkUB8h5" +
                "HVePaVWD4RmZlwLdFZ0WsQ32bmPlti1E+hM7TzWROzXm/Ftmk9" +
                "xBlttM5aWax5Td/A9yOb428nEsocu00YjfuIr7juRlmnfZdIkF" +
                "zsYzm8opKK19OjeCbii/WIL16xPeGzKBP5L6lj7qt3itoD/Jfe" +
                "gaVb2GB5hPZM+C7QQxNtKJPIG8569ir/jV1uXP2DsT1pHo8NFP" +
                "v3xHOUx413MJpNi6y2rt9Kr2VTaD6eMwJ72NZ6U3udd0F8x8gS" +
                "udtx7zyzpLWCsbi/Ch6iL9GFMIEuosf9n0NZhm1kxwazdYroo5" +
                "5p9Phmo7+fCtyM59B/F79jYvNifVxPvcQs0fewPas+tpPzV1yj" +
                "koYcX3okcn4icInV0zc598nbxn1rWu0LfMWx20X/84Pxtd7Wns" +
                "hatsOJL/J3hNHztew1o4Rfq9rF0UZF8R1mu1umXV33aeTFV/9M" +
                "4ptBlKX43jKwvMF5BfJ3ujjvdbq0gPeQa72B4X74In/HY+s/60" +
                "fYO6Y+w8P0G9TnifQ7eAyeQn2eAo3pbu0+mEyy4wsofuHwBLQ2" +
                "vt52xrYjro/qm/H3VtGu0Ge8/4uxvvRrrEV9FvjvB2o86S6OL9" +
                "dnfAb8Ktg2ji/Ui3xOGupYit4bz+T6LPrbXNyjJa6oz+K5mkr+" +
                "ehfOX3FunsQ3kmPiqzbA/QrG0UqiB/+S+EItXCdJfY6sM/mL+s" +
                "z5u4ON4vpssVXosyg9Ajc58VUmYd3jXJ8d3H4g/oTYMqWZjS+U" +
                "kfhCeYEvVZYCf/K61lXn4NqcfUm/Mq7ZAxdhzcXWcaHPuBX6jF" +
                "upzy58uT7jEa7PT8Iw5PF22/6qVaT9xeOZbI60v9G2Gt6NLFEr" +
                "s/nqGdz++uDbKXp9bBu3v6JG4BtfE58r9pz2dypw/Y1gP+ZKfI" +
                "0vUOCLW+QvNMRr65r4Gvew8EX7u4fbX1GL9ldsp3F91jQTX1yn" +
                "w9DY8zASW0R91hi3v+KYsL82f237a331At/oY5CjfMtGe/EVb3" +
                "Ant79WrbC/uDXsL5YG4Yr2F38VgW8Ot78mviC+Qm5/o6jNyfhK" +
                "+xspb3yzVU37i8fam/hK+yvOd9hf3OvrxBf1eYbA92vE92l1oN" +
                "ofnotdhfg+g/5VT44v6wqN1H5qnvYV60VQg/QHWRdohW10QxZd" +
                "i+vVVr86ISb4zUt85RJfF5/HbjOOC3xFqaw6QM/g+Lo0traxbc" +
                "i6C3wvD1IluCKG34EbX+PLFerBehjnoT5z/0rUdYYMo7Yyrs96" +
                "WsxyqekBozZHrcBGBfThOkcZ8XXsWfiKPUW02Fria5xhqsyZse" +
                "s4vkltN0W9XxEx1EYdZNQa+Bp7ue5ryHAnvo62nhe/s2Cu5C+W" +
                "Z8Mc5O/MaGOYB/OlPkeFJ0TWG9e84K/PiMl6qc8mfzOi8Re0c2" +
                "195vw17A/a/ei5Nn8jq5z8Fd/GFUH85fqc/MZN/8rkr2l/JX/9" +
                "9Vnscf5OSuZvvAVedQ+WfPgbWWnrs6OHSfpsbJlyqaO2jLFFfs" +
                "YvRG794afPyN+Ktj5z/vrps5O/rp4Y/MV1Aecv2wuE/Qovwyvs" +
                "gODvQqihNeXjo+grqM9L6G54ie0j2bASr1gm+PsLjo+uheXxKe" +
                "ygg79fuvmb0TN+gttfJ3/hRaHPWLL9Zzd/+fgIr71AfBHHkt7i" +
                "IuTven/+mvqczF+uzw7+LrbxLZC/Q/zZG3nV5C/0CMXfq4L5y3" +
                "7w4y/iW9nEtyD+wlIvf9lv7LCjrRXi91XT/mpD49Nt+xvdJe2v" +
                "0UY2oA+pDTHtL+Lrsr+ISYbb/mJN9yT7G4GyhpoG2F+9tsC3Vb" +
                "D9jX3rxRe6u/0rL75qA7f9hVVWu9y/8rG/HF+tvD9/Y+C1v2Ib" +
                "bH9zbXyd9pfjq2/1t7+Wz1hV+lcp7K/AF17n+uzi7xsmfyW+Bn" +
                "8RXy0PMrV7Jb5x/JY4fxFdtEccX4O/7TKaxFQbX+4/u/E1+mzg" +
                "iyUHvk59xpYHSHzF+Q3FL+Jr89eJb7y6P77Wudy/GlwgvtXC4M" +
                "sC9Jnb37D4wmrE94ZgfNkPfvg61KRcKHx3m/ENzl8HvjMsfX4U" +
                "9fnNWIRNRnzfghrxKiJ+9TbXZ25/pT4rZbg+a2M4vnj1csc77W" +
                "Rsewd4JAJfU58lvv76bOIbrM9i66PPnjM9+Lr1OYx/xSYGtp2e" +
                "Pnfx12fxhL767MQ3XX2OMUdbIhpFazIC72v5sBbeY8dhI3wIG2" +
                "hzWK9xf34T9iGTNqUtKNpdao7Um9AsihygZ9L6FHGi+I3RBkx8" +
                "hTSHVhfbs2gtiqyjmY77vSOeqXbyG2Oq33ukFyRCLiTXD1+nfx" +
                "V4ZR1P1OOA1aeJiWJZlL4FxCjyPL1e53kvdbh/lfLprfgVRWsK" +
                "71qtfSDasPQ5FmMnpT7TPDv+zDKhGdtHO+kfQDbtZ+qzd/yL7Z" +
                "wt48+p9NnEV8afDX3uIfVZ6+ynz7DV1Gcef07mb7A+G1bMR5+B" +
                "q3xNP33m8WeTvwXrs5O/AfqsoT4P8NdnyV+nPpvxZze+wfpsxp" +
                "9pf1ufY8yhz8O5PtOJjNAJWj4dRcfBLvgSvqKj6Vhxvx149EEm" +
                "GEgf9mXYeEdfWqT6zmB7evwtymLy14xfBS305yD+6tXSvOM219" +
                "7XVotD0mjji2T+priimzv+jDU7rdI/xe9uK361kq6y/WdcDf6K" +
                "9/CqcU1A/Ar70j61f8XjG07+WmOOGn7+lb//HIa/pv0N4O83Yf" +
                "wrPavo/pXwn58M5i+WCvSvCuav6V8pjviGy3/+lvMXvjPxBRwV" +
                "BuL7Rkp8O4Tzn/3wtcdHJYGvku/Bd08ofBsXD75qi5LG1xgf7U" +
                "3Cdx/HN1LP0qov2GTBproeDfuEbqHb6MdY+siq25xabejnYXUp" +
                "klU0Nfb6V/B9wf4V23jq/Cs1p/DX0vud84Op/SvPWxDeOfwMv8" +
                "B+OAC/0h36DjgMP8JBrP0tDL7sg/TwhZ8CeugYHxV2SY5fWUdY" +
                "Us8GB3q7xY6v9lLR8A3xdQ8PwPeQ+L4amfoceRKOGP7zj7Y+I7" +
                "uewjsd8NNntiu9+Aa3v1a90/5OP6X29/dQ+ryzePRZW1x4faYj" +
                "wuiz2jjA/h4V8Y3T4A/4L/wHjisR+JMJvy/Kve/81Pylw4pHn3" +
                "n8qnT852D+pvaf4VioZ1tWBP6OCGUBGgf079/i90T0acO/OsmO" +
                "435F3ZjnkfNHxnPL8dp7kAiaPzLn9/34a5So6+4O/vLxEdRzHQ" +
                "2cP0r51vP88HXPH7n4O8mPv1KfA/ib1vyRtsp//sifv3z+yJFV" +
                "ogTP79v8pf39548IgME/vU10EV2Ez3QSa414C7hneLIL8/1FF4" +
                "Y8L6SN4vlX6fDXzt9In7+p7S/0CMXf1UXg79IC7z8mRf9EFIP0" +
                "09vShaQ/x7cAG56dKMGlOPyr4sY33fhGIL5rSgrfVP4VsaKfpn" +
                "8lyhVBZhYOlPrMMqM/kgHEyI4Lyr/C93FTOH32jW9M/+vps1qh" +
                "mPR5Q+H12Rz/FqzPnp7Y/pXIv4pNRSwdfhKMcuuzjG+k5q9+c7" +
                "hv0i8+GXb8eyr1OYR/FU6f3ys8f1mdouszrQlEmRVbrDxHRvL4" +
                "FRmB46PmIFCFRt75I56/kTx/BJ1oA53PDfS254+U52nL6EI5f2" +
                "TODzrjk0H2154f9Js/Sm9+0B4fGbVp5F8F45te/pW2KXh+0Kft" +
                "pn74Fpx/hTXG+Ii6PGlv/lVsCxkt86+w1sq/kvxVZhvXBOZf6V" +
                "3+r8+++vxRqenzAhmfJGMlvsrLZIyyECrGEUEyzhwfIX8d418T" +
                "X7o5Cd+uXnzl+DcUvlnK/L8avnR2MeG7t7TwJePF/MIsMsHA9x" +
                "PlXc5f8pCwXQ/b+CqbTXzJxEB8b0nGN35lOHy5Pv/V8OX6XCz4" +
                "7g+Hr7KmZPirfIb2dwu8rGxTtgr7+xja37F8fp/bX/K4Of9r50" +
                "8a7Sy35395fFLvFi4/x8/+us4tYIzE83MiR4pof0Pm50QOpbK/" +
                "4fJztJ9Kyv7CUq/99bQl8ifJVDP+jOUZgfODrZzxZ+5fcf7S+n" +
                "b8Wb9Vxp+VfU77SzPDzA+K/aT4s+1fOePPUbWo8WfyVJj4c5QU" +
                "T/yZlS/p+UGJL5km/SuHPk8X+Rs1bXzN+V/pP5v4Sv+ZPC39Z2" +
                "gt/WdoJ/1n6Gj4z7dxfLn/LP4fepbyvcy/cuE7tej4+swvTEsP" +
                "X48+B80vVCsmfKucGnxN/9mbH0tr4lOhFikHFWtOkDrvYeL7bM" +
                "H5dbp4Tsf46Fdvfl3w+DcgehM6v6505hfCLXp+SY1/fd6Ya3xE" +
                "nrHGv4fQ/h5Ujgn7O1Py17a/El9YSRtJ+2vwd7ln/NvLM/514F" +
                "s4+5uMb+HsL5YKYX+j44vH/ka0U2d/PeNfKzJK5onfuVi3kcwn" +
                "c5joc3yqmR/Lesn4BhP/fWXdJH/VuMRXYNFA7+3mL64t2W3h8m" +
                "MdT9Q9FX+jAbl4yf8vs450Lgx/oxOKh7+RS9JQHZ/82HT5682P" +
                "JQux9D5uXyALrHYFvhmTTX2W+GLZhS/3r6iVp67LWOBpNr54Zg" +
                "ngy/l7KvAN9p/TnEMbU1r4khelPkt8eX4756/MbyeC++nkt+t3" +
                "uPmrVg+f316y9jedxcZXrVA8+MbGlS5/1bPM+SP1b2Q5nz/C82" +
                "T+syM+aeY/kxXO+CQf/1rxjTsLH590+s/FH98Q+6UW34hNKLX4" +
                "1UruP6v1aQcD/Y5kLdZaVpnnt5vzR7S850tJylnT+6R889vT42" +
                "9RlqLzN5rmvG1Qfnv8vDTaKFR+u6fGk99ONpjjXzXbP//ZFX9u" +
                "5Ruf5PGNu7z5dXb8OVV+u//414+/hRn/cv+59Ma/uJ6y8a8nPi" +
                "nz2/8HJtobXg==");
            
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
            final int compressedBytes = 2120;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXGlsFVUUPnNnefQ9ikUWQQWhIJWlgBjACnXDVFtBw6JERU" +
                "EpirJLQbaCbAlQlhgKSqkBZBHUmOgPf2JiFANUSqFuCVHLIqFQ" +
                "kURjXJLnmXvvzJuZN9N5r1PmvWm5yV3mzp37Zu433znnnjltNA" +
                "oCSBCBsPBlNAo3Q0e4HetszL0hFwYq2IpGpfwoTZAP9/NWAa/H" +
                "YB4fjYZeg8lQTHtmwKyonuANWAilsAJbBMqhndKL998CXaKGBH" +
                "143Z+Wd8M9hnN5mB+AUVgW0uMJtHwWnqP1NtM8c2i5CPMyva8t" +
                "dKB1Z+gEXQ1je2LOgb7QT+sR67FnOOYRoVuxfBge0cc+DqN5ay" +
                "xM1Hunwku0fgWmw2yYh635mJfCcixFzArmEGTw0ZmQRev20I33" +
                "4GrAnfpsA2AIDI3dn9IbRmLvg/AQlo9hLoIn4EkYB0/B03T8M/" +
                "A8vIj1NHgVy5m0by68juUCzOdhcTRKemizCUe0FjH+RjYZQoYT" +
                "xJfk8rODSA7BpyfdSF8yGGu8B9IvNIOeG0Fuo3V3aTRBVEh21J" +
                "I0fN0TGWbXm1GVyLUqvk1JKr78d45FU56U3smNJwONR8JXlJnT" +
                "4IpwDOqF49LL2PcNrk0D5qsGfKtINaklx/F6/ZnJUevcoZlxv1" +
                "YTt+qXksHXPoXb+YNvuK2/WErF3vE1J4EygawXTgi1wimphKwR" +
                "Tgo1wmmykqzWMVrB5DNZZ/u+rDXgOyvBd7KXH6vlHV9VPgeNvx" +
                "Z8qylGPZC/3yJ/v8d3aDlcE76DBjLUxN/E5PNci3wujZfPyfHX" +
                "Xj6HX/CJv5MCL58pf6XN2PqBz7fJ5jeYfbXFbe7QvJbFX2V94P" +
                "n7I+cvt5+lrcIvzH5G/mZL2zT7mfFXelvlr2o/M/5CAeMvjCEj" +
                "YTzyd75qP6v8Ve1n5G8546/RfsachP0c428q7OfwkeDZzyp/Df" +
                "bzH8x+BkG4KFxAvM+p+yPMFF96fa5VPkO+nXym+Jbg2WKDfH4n" +
                "Jp8ZvrTVzo2/DF+rfCZ7+VkbfJ34a8SX153xKc9bRubY8RdGkF" +
                "2Oc481tDm+/EjHlx6JvFfHF9tZvO7mMPcQO/5q+PKjcXFXGfA1" +
                "8PdXWl7GM/8I9fCvwZbbgX3/GeWzvX1lks8LWpZ8Dte5/safaS" +
                "6fL1EsK5G/V4Ur2Noj7WT8lXahfD6k8VfaJ70rfSD8Ju2WDgL3" +
                "c0gVpidV/RsLVf6aLP4DmPc3nb+2Z33jb/hc8PhrwbeB6V/pI+" +
                "6/+p2O7aj5N1D/vhXTv0w+C9ds9W8ZyudFMJnZz1z/fhjTv/wu" +
                "iJG/TvqXHzno3wSYO8eOv0z/Yt3J1Kvq3412+hdbkxz174aY/t" +
                "XPblH1r+F4Oa8V06hMXrfXeyz6F7ON/tXPU/2LtUX/Gu3nmP4V" +
                "gOrfDBImEZJFQsR2R8/lc3uLJa7EyefF/sjncENCO4VMz/L5ct" +
                "DtZ74So0kRKST4VpAxnvBdkk761zu+wfdvcC15ka7HYWzV2eOb" +
                "oH9yqbt/Mkj4hv9qdNXOBQVfOUSfC2Wz7LAmpEqOqPiKmSq+Yj" +
                "t7fG2uQ3zljNS8+S2Dv2JWM8zRnZZ3NLJWVZhr5Zsa5296Je/4" +
                "RoQ0wLdnM/C3o96iayJwKxsGxeQz46+LfF6WGH+9yGdY6eP+94" +
                "jrb0xJrXyGVS73t5rtj8Q8tj/ivY3vj9Y77o9KzfsjuXOw90eR" +
                "foHfH61V90dyd83/LOeKBfHxG6p8jtlXWvwGOWqN38DSEr/B7C" +
                "tr/IZ+r2kevxEZkHr/c+jNpsVvyHdRfLcz/7NcpD8d95E0KX5j" +
                "Iz2n+5/lwusTv+GXfA7i/sjsfxYfpVp3ibxYeE/G3Y04wXn/Ky" +
                "9zmzu03XXNP/dvfxRMfOGwFV+Vv0nO8YXunzxAkSvFPiafMzX9" +
                "y0dq+tdgX4lTbeVzGa7H+6r+tcpnOaPp+tdOPvunf1X/VdD0ry" +
                "afuf79lOrfzfImzb4Sp8fjG/qYvgX8+77wiW38ZBnTv2Z8EeEz" +
                "TP8a8XXTv37gS+rc8W0kftI3fMlZ7/ii/q0QLb5jo/7FI6p/25" +
                "SQXKmLs/7lY2P6d4e9/k1Cm9jq38hEn/ZHaeB/lrp60b8sfkOu" +
                "lDep8XVtqrHnpFAjmjStNFuaY+RvM9zzPG/XR2y/CbY50SLxne" +
                "/JO7KU7X/l/Sq+8l4VX3ElrtVpO/662c9x/N13ffjbmuznkOjJ" +
                "fuZfBOCKuArqRertiI9/5r6tgzieao+MsIdVv+T9mSMlrUc+e/" +
                "M/8/jnHjjPGmf9m5HfRP5+RvLImfTkL6kLBn/JWe/6V42v489W" +
                "Ln8dH19nls/O8XV0fLE5/lkeHB9f54pM/8bw9R6fI24z4+sUn+" +
                "OMr3/xORq+XuLrsN/wpYThK1cb8aW/tE7cgVyeosVfWeYfz+ti" +
                "h7tuEr62ZwsjO73jaxrpFH91OfX4avazl/grqVKfbY98kmqeDt" +
                "Iu1Lj613kWX4f1bumg3leRgPWnx9fx4zIfPUG+21fShtTbV5Y7" +
                "2sjt55/lrXK52/43af37U0w++69/G//+2zr0L0e5UvNPIn8vqP" +
                "6r8HmVv9Ih5r8y81fzT0oV7v4rxt909F8l4p9MB/9VSPTuv6KS" +
                "Gve/tKb737i3okrp4/593+a6mtS9+S1jf+Rxd6VHxMk0lkzJcc" +
                "Apofi6loZvOshnb4ntj5QBpAjL/k7xsRwr1/hYv1LkuHf92zr4" +
                "y5KCVoxYreSLteIpfXVWx62X6993+4bviRv4JmVz0f+/oYxqbv" +
                "tZuTeV/ueWER+b9FPb2M9KHpXPBYGRz3/f4G9S8vk+Fd/QMHd8" +
                "2feF0PDU4tua+Nsc8e3wP4jWMdU=");
            
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
            final int compressedBytes = 927;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm8tr1EAcx4dfd3/ZbtuDeFAUiw9QK9QnVaQPRSpCbfFZqw" +
                "dBb9UiSEuLRW3RavGFHixWFG9Sa68e/BPUbbUqq3jy4LHiH6CX" +
                "cZLNJjvpPjL7TNLfD5J5JFky88n3N/ObbDjXTTvHOTZz02CMOw" +
                "zu8DQGt3nZrXazm7OgLr9fr1pI5qK/eSAMW6BD8H0o+qQrS38t" +
                "c5SxUvdb20h8lfi2GXyf+4WvOyO+Vk90QgfuhyO+0W8n8VXSbz" +
                "t0aAvhCd/wPU58VU37o/PFgxn6ag7mIQ6zIhez6j646OMvJeF7" +
                "ivgq6feQPv5Gxn2j37PEV5FwFwtFplnUHV/WJvNli65ifeySVB" +
                "4yU5BqV7CVUnmjVNrBdtmlyJSo2afSpshrxz0tT3cWW2fsG9iW" +
                "9HzZAdZunXuYWSM/60nzW71SacRMJR0w86ljdm9uUGkVE0pkYq" +
                "bEus3yGbGdd5xzmfWL/aDogxk2LOgeFa2aF1u86mtp499IvCj6" +
                "7XPVE1dIv5Fvpn6PcV494Bv/fJX8s7pFY5LHPuFOv0GOf4NisB" +
                "Z78GT0O+ehx1Zdk2DcbZV2wh48HZoEc+UItsEmWCXSNdAA20Xa" +
                "IjZzBINmWG2k9aEJ2CvS9QXc2W7iq9jqrS5nXL2l0G/ofvlaWv" +
                "j4q9y6e16jHXqQ0K++r/kh0WySSkK/0IoX1fSLF0i/XtFvIj7K" +
                "zFdsrWJrTMZHqXxZF/bLfPX4COpF3uSbb3xk861EfGQc8Vl8pP" +
                "N1xkcJ/TqeAwdfHLD5utWvzTff+KgQ/ZbfP3tPv4n4KMkXB7P7" +
                "Z1W+OFRJ/5yvLY35leMq0z+n1Bh8RWrwlWZmw7Z+jZT4BoIvXs" +
                "vtn4mvt/ni9cx83Yy/xLcSfHEk9/wqdf5s81WdXxFfb+oXR92P" +
                "v6KOxl9P8XUTH8n6xZuk30Dpd4z0Gyy+uOi9rt/eHy1NvmmZG+" +
                "+P9Jzj/dF46vqG394fkX5Vxt+81q8eEV9/jL+Gx35C/tm3/vkZ" +
                "Ps2lX3yhrN9J0q83/DO+xGl8VXT/PEV8veGfccY8GqDvj8g/W3" +
                "TfJnPhDP95gLlwTb7fL4SriW/Fx9/P+B5jKIjhJ3yHc0Xyz7Pk" +
                "n4usxI/q/hnf4C/r6C0rZ/2/HUazrW9U4vtu0m9Jnp2/FB/51T" +
                "8nc471q3+yf6b1K5/GR3cTqcaS/hlukH8Oon6LG/9q1aRfb6xv" +
                "aDWl4Bv+SXy9sb7hxrQ6viQseP6Z/QfCl082");
            
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
            final int compressedBytes = 500;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt1KAlEQx2PSnWvrIigKCyISshAqorzpOyu6Ceqqh+imNy" +
                "ghoshAjaDv6OMueolSisCn6CW247KF5kcqWntm/wPL7Dm7B2F+" +
                "zn92zi75zQKj4bxRiEYprPyAPR6kPmpXvpP6aUj5CXUE7Gvj1G" +
                "H5LvbRmPI9Zs1GI+afW/OHqbFRsLL7uKVgZdR0henNtyhzP7c2" +
                "In89D8hfJ+QvNUqf28BXNN9u8BXNt1dHvprX2mBNq0LqCOfNWH" +
                "yVt/jmPZkFvvjad4LvP/PlSe/e7yu9+4iets/PU43QZ28S+uyI" +
                "/J1G/rqE/iPPlLz2hPjobjxbhj346lt/lzjCCzzPi8Yrzxnp+t" +
                "RfI4X6q0d/ZOX2CqKnqSpvUMSmv1zmn+H7MTYQOV30uchcPfav" +
                "NqHPztJn3iqxKk1vlKGUOnv5nnuu4NfeEXEtFHxbscpYxKriC9" +
                "OE7w74ild2iy9HwVfjPN1F/XVl7mZ3PDJ8AH0Wk8mHBYyjiIqu" +
                "/a+xZqxmzzyx3P6XY7n9r7HuiVf5fd0R+l8n9L8cx/6VaC1OgK" +
                "9ovknwFc33GHxF8z0BX9F8T8FXNN8z8JXd/xaZq8f733P0v47o" +
                "fy+Qv6L1+RJ8XUL6qgRbvD/Smeo18lc03xvwFc33FnxF870DX9" +
                "F878FXsjV9Aqp9MKU=");
            
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
            final int compressedBytes = 544;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms8rRFEUx3Xnzb1nwcKW/MhGlB/JGMJSZgaJxALZK8UOmZ" +
                "CUhcmCsrGysbCgLPzIQiz8noX8AWwVZWf33GYmmZmHec2buu/6" +
                "nprue2/em6bz6fs9554ZsctCZixYj/ljsMKUc24iNAqxhxxoRH" +
                "M/Tb8vyIo7g5WLQxESQREQXTwqOvmdvOZLuqOB+Vm7XGsS53Ws" +
                "khXJtYRVsXq5tslXdeK9VlYcW0v5LWuRa0UW36wJdGxmrNaS7x" +
                "G/4jf82jR5lF+Cr6rB7+3z5QeS73HafQ7w9QbBVw39WlxzgK84" +
                "AV9F/PkU+tWa7xn6K/izbX8+B19F9HuB/lnn/jlH+n0CX639+R" +
                "l8teb7Dr5a8/0AX635muCrGl/Kd46vEQZfFfhaBRWkPbmC7LmU" +
                "eTk1YH6F+muXLzWCrxp8yS8dOZZLHiUf9KtqULN9vvH5FXU4r1" +
                "/PFvhq7c+d4KsCXwp4I38/6V1D9lzbPw9QPw39rl8atK3fPuhX" +
                "CX/uZiEaYb16/b+dFbiuexrO4WePmqbn0fPwlZ2lzOYbbBmacW" +
                "UnPpYZX4RL+Y6Drz79VY72RxPor1TZ/9KkrL+vDs833sBXBb6W" +
                "/jwFf9Zo/zsbPzLWv+uXZpL1a2za9Odp6FdV/Vooeg7Zc3t/la" +
                "LfcHb6NTagXxX0S/O0kOBbhvnGv90RLyIHWvNF/+zi+ksRvuP4" +
                "fGMV9VeJ+ruN3391jrxP3808Ng==");
            
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
                "eNrtm89LVFEUx+Vy57x7F5K1TIzBjRT4g4EpRAfalaPSJqiVi6" +
                "BchlCbyH27FgWCGCroNOTSlejCXWk29Af0YyViGW6kgqjXZXg8" +
                "582P54y8R+e+vgce59077zHM+dzvOfde7oi0WqMivXQrTGQDrY" +
                "y4InLG93rtftEjzhvfJS6KAeOHzXXJ+2xIdJb9BSqIQeO73VOb" +
                "uOzCWotYX52+MZFXG+KGuRsPefNsVZuY/9J229io9Zgika7TF4" +
                "F+1Sb0y0G/TY6ubUTPUua+fuWzSv2qnaB+5Uxr+pXPoV8W9bcR" +
                "3/fgmwy+6qOfhT9EV3/lNPgy4fs5lvnVJ/DllZ8j5rsLvrz4qr" +
                "0wvqi/0G9Av/vgm2i+X8CX9froK/JzQubPB3HoV66ALwe+cs6f" +
                "X33z++YDTyzJF/KV8Qvq0O+bPfnbZMFcy4g6d1NHZmTkAuOkrF" +
                "/jy/qtGkOefr0W9PuP9dsE3+/gm2S+ItM8X/UDfP+T9dFPzK8S" +
                "zfcX+NqRn8t59zeiZynzRvsbf7C/gfwckp9d8OXFV7eF8YV+k1" +
                "x/NSF6ttZfukU3a+uvdir1S7ehX9TfSr76DPhy46s7ouOL83UJ" +
                "1+858OU6v9LZmjefIHq2729ErN+r0C+P/KzHnLwz4lx3RqnkXK" +
                "OdiP4f+hZ8mfAdpze0TVuuSyV6Db5cjd61zpdWG+0/64kgX6x/" +
                "7dRv6pG+c/KbqceIXpJN30UM7LTj85NyUd/z7uYN0Um/3z8/KY" +
                "t+H85P2qPN+03k56eIE9a/gfXvFOZXTNZHD2Ph+wB8OfBt+wvW" +
                "09LI");
            
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
            final int compressedBytes = 552;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmzlIA0EUhnWS7O7bWiyixiBCvKKixAO19qq0UBtLS3sLrU" +
                "SwEQuFgGhnZ2kjCioWIqgpxKPzqCxsVVSUdVmiZHOyZJSZ4X8Q" +
                "JjvJhOz7+N8xk9BMYMkqaIFlCyalsTDNZszFXFdtrJP12WM0ed" +
                "3CIixoj1WsnrXaY6/9aEi+1sMqnDEU2GTd9lhTxDfrAB2PHmvO" +
                "xjfLHAe+NAe+YvClNX1IH9QH9GEtofdrZ3z4aqfgKwjfdcsyHF" +
                "9qCSMGvqKa0eWdr7btrJy0LN+l7+L3ffMZKxezft6CKPdOO+Cf" +
                "q776eeZfSc2/tOvWrz/uTb/+VehXlPqK9hwVP+aur7QJ8JWTrx" +
                "Pd9gvGvwN4T/b4zLk/OoR+xep/0/LvEfIv9jdy8zWm/5MvHYOu" +
                "R/2eQL+q1FdJDZzL2//Citw/SMAHSvO9gg9k7Y/ounB9pW143n" +
                "+OI/+qXD/TDfiKw5fuuPO9BV+B+D5w53sPvmL1R3nrq2cZ77d0" +
                "CszT8y+9ctLvC/QrRnymjxS6b9zi8zv4ilY/0xd94vc5KvdHZE" +
                "G/KvGlJxY2fcaoMZ6/fjbGPJ8fjYCvPPWzWQbvyVo/m+UZczz+" +
                "v4D9DQHzLz++ZhB8xeLrPt83K9x8cb4vK1+z8i/0698CX6Xjcw" +
                "h8leZbDb5K860FX1H7XzOSsXIR3pO1//0j/dZBv0rH50bwVZpv" +
                "E/iKmn+zZOQovIf869JvO/Qrgn5LvgExrbfE");
            
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
            final int compressedBytes = 519;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7tKA1EQhuWQHHbnERQv2IiCF8QoaiwsfAARtLC08gW0sB" +
                "FEEFEsFHwDtRMvEAsbQdBo4iNoFFSw0Mp0si4SQzZZYpachNnx" +
                "HwiT3exCmC/zz8w5G8cpNRooPqPWHJhgo0HEIJym2nzOxTxH/W" +
                "pIjbm+O3fcqzpUo+ubVafqc33cfXXlPhtVTT++hUbUsOvbq/hm" +
                "+E0FjViPH1+K14Jv9B58OfCl8ejm33dGtxA9GfpsZ83kr/2J/O" +
                "WQvxX2VxOIXljzl6b0gd43XX/1HvKXR3/1+y6yXciXZr18I7vB" +
                "+EZ2wJcXX8Pz0Rz4MpmP5vW1vtFJx9F3+kqnDOnzLfiaNZ0Ozl" +
                "efFPRQC/nrVkru9F2fVKuIugyjRcRAxvxrrP4uQZ9F91fL4Mt1" +
                "fYPWK6u/sJBW2w3wlVp/sf4st/7aX1YM86/8+uuj2IeIXnj1mY" +
                "6smfL9szUdNH+tSeQvH32mY9PzEfhyq79F+0enXr7YPxI0/yYw" +
                "H8mbj4ry9wz5K1qfz8FXrD5fQJ/l6bPh/vkZ+ctLn63XcnyD6j" +
                "Ndgi8HvpTM19/W/HV4fkOYPtsPpvWZ6rr+TCmQLKfP9qNpvnam" +
                "rnzToFvV/kIG0UP/7NHnJ/RXPPSZXmoyH+H/oczmI8P5+wa+ov" +
                "m+gy/X/oo+Kpt/Yf+4v8LzdbL1OQu+HPg2fAOQJ17M");
            
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
            final int compressedBytes = 453;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms1Kw0AUhcuQJm3nBcSfWkQQBasIrYi6tlVwIS4U3OhWwZ" +
                "XgX8GNWuhSwY0vIMUnUXwE30JBNzFoQccIGk3gZvwGyjS02dyP" +
                "c8+5yaiSH1qqYlxNqEk1G+yjnesxNaS6g71PDavxYJ8JPiOd36" +
                "ZVz+teLDypqWAf8H+9VNVnRatY+Wf/KzyH7mxSvZQyT0i/OoN+" +
                "Jej3na9z9pGv7jL5OhfR+Drn8JXan3Uv/dme/qyL3rxX92regn" +
                "vnzbm38fRn9wb9yujPuj8J/83W4SvLf2POVyX4CtHvIP0Z/UbW" +
                "bxm+YvNzhfxs89JV+Fo0H9Xw3//Vn79Q9CLVS6t+E8pXS+hXbL" +
                "5axn/R7zf6XUG/Vs+/q/AV25/X6M8WzUfrb9/M97+5PVO/Ud//" +
                "5nbRb4rmow2qR74y/HcT/UrLV3orPr5OA74S83P+MR6++Qf4ps" +
                "h/t6ke/mv47w76tfr5xj58reZ7AF9ZfD+dbz/82/MNzrdL4asb" +
                "SejXacM3Rfn5iOqRnw3/PUa/VuerE/iKzlen5CtL8lWTfGU131" +
                "YSfLP38JWan/Vl6E7Ob1i99BU1sJpvmxow/xrz0TX+K8F/My/G" +
                "2zEk");
            
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
            final int compressedBytes = 390;
            final int uncompressedBytes = 19201;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2EtKA0EQxvFQzGKgZxdRUQySjSj4QHwg6sq1igrmAi7UnW" +
                "dR8BDewCO4EtGVD/C18HEDF2MjQdIqISMZqKn5N4RmJrv68VX1" +
                "tAynv5bMBk/TMi/Lfh9vPk/KiAz4fUhGZcrvS/431vxvUQa/9p" +
                "o7lQW/19N/L5lLWdkqNvHHu2/f6LDV152FvtFxNt/oCF/Vvuf4" +
                "2vLtbn+On/HV4NvZchdUr6DmeZ2vLsmv5f7srvDV4euu48240d" +
                "433s48fzfwNZ3fG3xN+97ia9r3Dl/Tvvf4mvZ9wFeX74/7ycfQ" +
                "l/vJwn4fPeWR3+gEXw2+nS33QvUKap7X/H0lv6rn7xvz18j8fW" +
                "f+ljC/H+S3ROcrimd6JRVqwPm5tT8nVfqz5vmb9DB/bfl2Ob+9" +
                "+Jr27cNXdX/upz+T3zb5reOrOr8z5Lc89xvJCtXj+zfoz6vk1/" +
                "T8XcPXtO86vqZ9t/A17dvAt0Dn5x2qx/k5yO8u+TXdn/fwNe27" +
                "j69p3wN8NfhWPgFt6UyQ");
            
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
                "eNpjUviPAZhMUXhGTOZMtkBaF8rXZ1JnkgLSskyaTAZA2gaIta" +
                "By1kzSYFqOp4TJEkgr/ScbMJn9HwWkhZgeFjFaxW/paPwO6/it" +
                "GI3fYR2/raPxO6zjt200fod1/HaMxu+wjt/u0fgdDPHLAABw4P" +
                "9E");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 14, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 134;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3ckJwCAQAED7r1lYK1BhwQtnCshnT00gESO1AH2hfpCf6g" +
                "fgsv43e77+K78AAOyXAM53zpeIPwC/zSfvz+WP+w3xF18A/Rt2" +
                "5dfr+av+QH0g/wDAfDVfAQD7BQAA9lcA/VF/BADzGwAw3wEAAA" +
                "AAAAAAAAAAIG/1/xd9f39YA1P7Gbk=");
            
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
