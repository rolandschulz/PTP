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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 19, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 8, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 110, 189, 190, 0, 191, 192, 101, 36, 1, 30, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 57, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 57, 3, 1, 8, 123, 4, 124, 0, 127, 5, 125, 221, 237, 6, 7, 128, 126, 0, 173, 238, 209, 212, 8, 214, 239, 215, 88, 30, 9, 216, 217, 219, 218, 101, 30, 114, 10, 220, 11, 240, 222, 12, 227, 13, 0, 14, 228, 2, 129, 230, 150, 231, 241, 242, 15, 16, 243, 30, 244, 245, 246, 17, 247, 29, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 137, 259, 260, 261, 262, 22, 23, 263, 264, 24, 265, 266, 25, 3, 267, 268, 269, 26, 27, 152, 154, 28, 243, 270, 271, 237, 241, 272, 273, 4, 31, 274, 39, 29, 39, 244, 275, 276, 277, 0, 88, 39, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 56, 289, 30, 290, 291, 156, 6, 292, 293, 294, 245, 295, 296, 297, 238, 298, 299, 103, 300, 7, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 30, 39, 311, 32, 312, 313, 33, 314, 315, 316, 5, 317, 318, 319, 34, 320, 0, 1, 2, 321, 322, 323, 30, 35, 324, 325, 57, 326, 239, 327, 144, 328, 8, 329, 246, 240, 247, 236, 8, 248, 249, 252, 253, 254, 330, 255, 256, 331, 242, 9, 173, 10, 332, 36, 333, 334, 88, 335, 257, 336, 337, 338, 258, 180, 250, 259, 339, 340, 341, 262, 264, 342, 343, 101, 344, 345, 346, 347, 348, 349, 11, 37, 38, 350, 12, 13, 14, 15, 0, 351, 16, 17, 18, 39, 19, 40, 20, 266, 41, 42, 21, 43, 22, 23, 24, 352, 353, 26, 28, 33, 34, 354, 355, 356, 44, 36, 37, 38, 40, 41, 357, 42, 45, 46, 47, 48, 358, 49, 359, 50, 360, 51, 361, 362, 52, 53, 54, 32, 35, 363, 364, 0, 365, 55, 366, 56, 58, 59, 60, 367, 61, 62, 368, 369, 370, 63, 64, 371, 65, 66, 67, 68, 372, 69, 70, 71, 72, 373, 374, 375, 73, 74, 75, 376, 76, 77, 78, 79, 80, 1, 377, 378, 379, 380, 381, 81, 82, 2, 83, 84, 85, 382, 86, 3, 87, 383, 88, 89, 90, 0, 384, 385, 91, 92, 45, 4, 386, 93, 94, 387, 95, 6, 388, 3, 389, 4, 46, 96, 97, 5, 390, 391, 6, 98, 392, 99, 393, 394, 100, 102, 7, 395, 104, 105, 396, 47, 48, 106, 8, 397, 107, 108, 398, 109, 399, 400, 1, 401, 402, 403, 404, 405, 406, 123, 110, 111, 112, 407, 408, 9, 113, 114, 49, 116, 117, 10, 0, 118, 119, 120, 11, 8, 12, 121, 409, 122, 123, 1, 124, 126, 127, 13, 129, 14, 0, 128, 410, 130, 131, 132, 133, 134, 411, 135, 136, 412, 137, 138, 139, 140, 413, 141, 414, 415, 416, 142, 15, 417, 418, 419, 420, 421, 422, 423, 143, 145, 424, 146, 425, 147, 18, 148, 181, 426, 427, 8, 428, 149, 150, 19, 151, 152, 429, 117, 430, 15, 153, 154, 155, 20, 156, 21, 157, 19, 158, 159, 431, 25, 432, 433, 434, 160, 435, 436, 437, 438, 161, 162, 0, 50, 163, 164, 165, 166, 167, 439, 168, 27, 440, 441, 442, 443, 169, 51, 143, 170, 171, 172, 173, 444, 445, 446, 174, 175, 176, 177, 28, 8, 178, 447, 448, 449, 450, 451, 452, 101, 453, 454, 179, 455, 456, 180, 52, 457, 458, 181, 459, 460, 461, 462, 463, 182, 464, 465, 251, 466, 467, 184, 183, 185, 468, 469, 470, 471, 472, 186, 473, 474, 187, 475, 476, 477, 478, 188, 479, 2, 480, 481, 56, 189, 482, 483, 484, 485, 486, 487, 190, 488, 489, 490, 191, 192, 491, 492, 493, 101, 193, 494, 495, 194, 496, 497, 195, 498, 499, 500, 15, 260, 30, 501, 196, 502, 261, 503, 267, 504, 269, 505, 270, 36, 197, 198, 199, 200, 29, 506, 201, 507, 508, 202, 509, 274, 510, 511, 512, 15, 275, 513, 7, 8, 57, 9, 10, 203, 514, 515, 11, 516, 517, 518, 16, 148, 519, 18, 278, 520, 55, 0, 3, 521, 522, 523, 524, 525, 1, 526, 283, 3, 527, 288, 204, 528, 529, 205, 530, 206, 21, 531, 532, 533, 534, 535, 536, 17, 537, 25, 27, 28, 207, 538, 539, 540, 541, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 553, 554, 555, 556, 39, 557, 558, 559, 560, 561, 562, 208, 563, 564, 565, 210, 566, 567, 568, 211, 569, 570, 571, 572, 573, 574, 575, 576, 577, 63, 578, 168, 579, 580, 581, 64, 174, 582, 583, 584, 179, 585, 65, 586, 66, 73, 587, 212, 213, 588, 215, 589, 217, 590, 220, 591, 592, 74, 75, 76, 88, 53, 593, 101, 103, 594, 4, 595, 219, 596, 597, 209, 598, 599, 600, 601, 5, 602, 6, 603, 12, 14, 604, 605, 606, 607, 39, 608, 609, 610, 222, 611, 612, 223, 224, 613, 104, 614, 615, 616, 617, 618, 619, 225, 226, 620, 227, 621, 182, 622, 228, 15, 623, 624, 625, 626, 627, 105, 107, 628, 629, 630, 108, 631, 109, 114, 115, 116, 229, 632, 122, 633, 634, 2, 635, 123, 125, 130, 636, 637, 231, 638, 639, 139, 141, 143, 144, 145, 56, 640, 641, 642, 232, 56, 19, 643, 644, 645, 146, 7, 20, 25, 646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 159, 4, 661, 662, 663, 160, 161, 158, 664, 162, 233, 58, 169, 170, 172, 173, 665, 178, 179, 180, 666, 181, 182, 183, 667, 6, 184, 185, 186, 234, 235, 59, 236, 237, 668, 184, 61, 62, 67, 68, 69, 669, 670, 8, 9, 671, 672, 673, 674, 675, 676, 677, 678, 679, 680, 681, 682, 683, 55, 57, 58, 684, 685, 686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 205, 696, 697, 698, 699, 700, 701, 702, 703, 704, 187, 705, 188, 706, 707, 708, 189, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 27, 28, 30, 35, 731, 732, 733, 734, 735, 190, 736, 191, 737, 192, 210, 193, 738, 240, 739, 244, 740, 741, 194, 742, 39, 743, 744, 745, 746, 747, 225, 748, 195, 749, 750, 751, 752, 753, 754, 755, 756, 757, 196, 758, 759, 760, 761, 201, 762, 763, 764, 765, 766, 767, 10, 768, 769, 770, 771, 772, 773, 774, 70, 7, 202, 203, 775, 776, 777, 778, 779, 780, 781, 782, 783, 214, 59, 218, 221, 784, 71, 223, 224, 225, 1, 226, 227, 72, 229, 230, 233, 234, 235, 236, 237, 238, 239, 242, 247, 785, 289, 786, 245, 787, 0, 0, 57, 59, 788, 789, 790, 250, 251, 254, 73, 256, 74, 292, 791, 61, 792, 222, 243, 248, 249, 253, 257, 258, 793, 259, 238, 794, 241, 795, 796, 797, 798, 799, 60, 263, 75, 800, 801, 264, 266, 8, 802, 295, 267, 268, 803, 79, 804, 299, 805, 269, 270, 271, 272, 806, 807, 301, 808, 246, 809, 273, 274, 276, 810, 811, 247, 248, 812, 249, 813, 814, 250, 815, 816, 817, 818, 251, 819, 820, 62, 252, 253, 821, 822, 258, 254, 823, 824, 825, 826, 259, 827, 260, 828, 829, 830, 67, 261, 831, 262, 832, 833, 834, 835, 80, 277, 278, 836, 1, 81, 73, 82, 83, 77, 84, 78, 85, 79, 837, 279, 280, 281, 838, 839, 263, 840, 282, 841, 264, 842, 266, 843, 844, 88, 101, 61, 283, 284, 62, 308, 110, 268, 845, 63, 846, 269, 847, 64, 285, 286, 2, 65, 287, 86, 288, 289, 66, 290, 848, 311, 291, 849, 850, 1, 851, 310, 852, 292, 80, 853, 270, 854, 855, 293, 294, 295, 271, 856, 857, 272, 858, 859, 273, 860, 861, 275, 862, 87, 296, 297, 298, 86, 299, 300, 0, 276, 301, 302, 303, 304, 305, 282, 863, 864, 865, 306, 307, 87, 89, 90, 91, 92, 93, 94, 95, 99, 100, 101, 102, 106, 110, 866, 308, 67, 111, 309, 312, 314, 315, 316, 867, 317, 318, 319, 321, 310, 311, 868, 322, 324, 326, 327, 329, 330, 331, 334, 336, 337, 338, 339, 869, 340, 313, 320, 341, 323, 325, 342, 328, 333, 335, 343, 344, 345, 346, 347, 348, 349, 1, 870, 350, 352, 353, 354, 355, 356, 871, 357, 872, 873, 358, 874, 875, 359, 360, 876, 367, 361, 877, 277, 0, 878, 363, 368, 879, 880, 362, 881, 112, 882, 883, 884, 279, 280, 364, 365, 281, 370, 313, 371, 375, 885, 886, 372, 376, 377, 378, 285, 887, 374, 379, 381, 382, 384, 386, 114, 387, 68, 388, 888, 380, 383, 390, 392, 393, 889, 394, 890, 891, 892, 286, 395, 396, 398, 400, 893, 894, 895, 401, 896, 402, 69, 399, 113, 403, 404, 406, 407, 408, 117, 118, 405, 897, 409, 898, 287, 899, 410, 411, 900, 412, 413, 414, 416, 2, 901, 902, 424, 425, 426, 427, 89, 428, 903, 431, 430, 432, 433, 434, 435, 90, 436, 437, 439, 316, 440, 441, 320, 442, 904, 443, 417, 418, 905, 906, 419, 907, 908, 909, 910, 911, 444, 445, 11, 912, 913, 421, 446, 119, 120, 121, 447, 91, 914, 915, 916, 92, 290, 296, 917, 918, 454, 422, 919, 920, 3, 921, 922, 923, 924, 93, 925, 124, 926, 927, 928, 448, 929, 4, 930, 931, 450, 932, 933, 94, 6, 934, 935, 936, 126, 937, 938, 939, 940, 297, 95, 97, 941, 942, 943, 298, 944, 449, 455, 456, 451, 452, 453, 457, 70, 0, 458, 1, 459, 2, 460, 461, 463, 464, 71, 465, 99, 2, 72, 466, 468, 462, 467, 469, 470, 127, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 3, 300, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 501, 503, 504, 304, 505, 302, 506, 507, 509, 945, 129, 510, 511, 514, 4, 303, 508, 512, 515, 513, 5, 518, 946, 521, 516, 305, 332, 517, 519, 522, 523, 524, 525, 526, 6, 343, 527, 528, 530, 529, 532, 533, 534, 535, 536, 537, 540, 541, 548, 520, 947, 948, 531, 538, 949, 950, 951, 344, 539, 542, 3, 139, 141, 561, 952, 549, 1, 953, 954, 4, 562, 569, 142, 100, 12, 543, 955, 544, 545, 123, 73, 956, 957, 558, 559, 565, 958, 345, 959, 960, 347, 572, 961, 348, 7, 962, 963, 349, 964, 965, 966, 571, 143, 546, 566, 967, 547, 550, 968, 350, 969, 573, 323, 970, 574, 971, 351, 352, 575, 551, 552, 972, 973, 974, 975, 576, 976, 977, 978, 353, 979, 980, 144, 981, 0, 982, 983, 984, 354, 985, 986, 987, 988, 989, 990, 145, 101, 102, 103, 147, 148, 991, 150, 151, 153, 154, 992, 993, 104, 994, 995, 74, 996, 997, 325, 998, 577, 553, 554, 555, 556, 557, 560, 328, 999, 155, 1000, 1001, 124, 75, 1002, 76, 1003, 5, 563, 578, 77, 156, 579, 580, 105, 564, 567, 125, 568, 1004, 1005, 332, 1006, 355, 1007, 1008, 581, 1009, 582, 583, 1010, 584, 1011, 1012, 357, 106, 1013, 107, 585, 586, 587, 588, 589, 590, 591, 1014, 570, 592, 593, 594, 595, 1015, 596, 157, 1016, 1017, 597, 598, 1018, 1019, 601, 1020, 604, 1021, 1022, 599, 600, 602, 605, 1023, 606, 1024, 607, 608, 1025, 1026, 1027, 609, 610, 6, 7, 611, 612, 613, 614, 1028, 356, 1029, 1030, 1031, 359, 615, 1032, 369, 1033, 361, 1034, 616, 617, 1035, 1036, 618, 108, 619, 620, 621, 622, 623, 2, 1037, 1038, 1039, 126, 78, 624, 79, 625, 1040, 363, 626, 1041, 1042, 1043, 1044, 368, 628, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 1052, 631, 634, 1053, 1054, 636, 637, 1055, 633, 370, 1056, 635, 638, 1057, 641, 1058, 1059, 163, 1060, 1, 1061, 1062, 639, 642, 644, 645, 643, 109, 9, 646, 647, 164, 371, 13, 1063, 648, 1064, 1065, 1066, 1067, 372, 1068, 373, 1069, 165, 166, 649, 80, 1070, 1071, 1072, 1073, 1074, 652, 1075, 650, 1076, 653, 376, 651, 375, 654, 1077, 655, 110, 1078, 1079, 10, 656, 657, 658, 659, 1080, 660, 1081, 661, 1082, 670, 662, 378, 663, 111, 1083, 1084, 11, 1085, 672, 665, 390, 1086, 394, 1087, 1088, 1089, 667, 167, 168, 1090, 171, 1091, 397, 1092, 403, 404, 1093, 1094, 81, 669, 1095, 1096, 1097, 1098, 0, 1099, 1100, 1101, 1102, 1103, 671, 1104, 1105, 112, 406, 1106, 1107, 1108, 673, 674, 675, 82, 676, 1109, 677, 679, 1110, 680, 1111, 1112, 678, 1113, 1114, 1115, 1116, 174, 681, 682, 1117, 1118, 683, 684, 1119, 0, 1120, 1121, 1122, 175, 8, 176, 685, 686, 1123, 687, 177, 688, 689, 1124, 690, 1125, 193, 197, 1126, 407, 333, 1127, 691, 1128, 692, 1129, 693, 1130, 1131, 695, 694, 696, 1132, 12, 1133, 408, 697, 198, 1134, 698, 1135, 699, 409, 700, 410, 411, 1136, 412, 701, 1137, 1138, 335, 702, 703, 1139, 1, 1140, 1141, 415, 1142, 1143, 113, 1144, 115, 1145, 424, 1146, 425, 1147, 83, 3, 4, 704, 705, 1148, 127, 84, 426, 1149, 427, 706, 1150, 9, 1151, 199, 707, 708, 1152, 709, 1153, 200, 345, 710, 711, 712, 713, 714, 715, 128, 716, 1154, 428, 717, 118, 1155, 119, 1156, 1157, 1158, 212, 1159, 1160, 718, 719, 13, 14, 720, 15, 1161, 721, 722, 723, 725, 1162, 726, 727, 17, 728, 18, 1163, 729, 730, 1164, 213, 731, 1165, 1166, 732, 733, 1167, 724, 429, 734, 735, 338, 736, 737, 1168, 1169, 1170, 738, 739, 740, 741, 2, 129, 85, 121, 742, 743, 744, 1171, 1172, 745, 1173, 430, 1174, 341, 124, 125, 0, 126, 127, 746, 747, 215, 86, 87, 748, 749, 88, 750, 216, 89, 751, 1175, 752, 753, 754, 755, 756, 757, 758, 759, 760, 762, 1176, 761, 763, 1177, 764, 1178, 1179, 768, 129, 1180, 217, 187, 1181, 1182, 431, 765, 432, 1183, 766, 767, 1184, 130, 1185, 1186, 769, 1187, 19, 433, 131, 1188, 1189, 770, 771, 772, 8, 1190, 1191, 1192, 20, 434, 132, 1193, 773, 774, 1194, 223, 224, 225, 435, 2, 436, 437, 1195, 775, 1196, 1197, 776, 777, 90, 778, 226, 779, 782, 133, 783, 784, 785, 1198, 786, 787, 788, 439, 1199, 1200, 134, 1201, 1202, 1203, 1204, 789, 790, 1205, 791, 440, 1206, 1207, 1208, 227, 792, 793, 794, 346, 795, 228, 1209, 350, 796, 797, 1210, 441, 798, 799, 800, 364, 801, 9, 229, 802, 10, 11, 1211, 803, 804, 1212, 1213, 1214, 442, 1215, 459, 1216, 460, 1217, 1218, 469, 1219, 1220, 135, 1221, 136, 1222, 1223, 1224, 1225, 1226, 351, 230, 805, 1227, 365, 130, 472, 91, 366, 1228, 806, 807, 808, 809, 810, 811, 812, 373, 1229, 813, 374, 815, 1230, 131, 92, 814, 1231, 1232, 1233, 233, 234, 816, 817, 818, 819, 1234, 1235, 820, 821, 822, 1236, 823, 824, 1237, 825, 826, 1238, 827, 473, 10, 828, 11, 12, 1239, 1240, 829, 830, 831, 21, 22, 235, 832, 1241, 236, 1242, 93, 833, 1243, 834, 1244, 1245, 1246, 835, 1247, 836, 837, 1248, 838, 839, 840, 841, 842, 843, 474, 844, 1249, 137, 1250, 845, 13, 1251, 23, 846, 138, 1252, 1253, 1254, 1255, 1256, 475, 847, 14, 1257, 139, 479, 1258, 1259, 1260, 1261, 1262, 480, 851, 476, 1263, 481, 1264, 482, 1265, 1266, 483, 1267, 1268, 1269, 1270, 6, 14, 1271, 1272, 1273, 1274, 237, 1275, 848, 849, 850, 852, 1276, 853, 854, 367, 12, 238, 239, 1277, 855, 856, 13, 858, 368, 1278, 484, 485, 15, 1279, 17, 1280, 242, 1281, 1282, 486, 1283, 1284, 1285, 142, 143, 7, 8, 859, 860, 861, 857, 487, 862, 369, 1286, 1287, 488, 372, 1288, 1289, 377, 863, 14, 866, 245, 867, 1290, 94, 246, 247, 489, 490, 869, 874, 870, 871, 875, 876, 877, 1291, 872, 873, 879, 1292, 1293, 1294, 1295, 15, 881, 1296, 1297, 868, 878, 880, 1298, 1299, 380, 250, 251, 254, 1300, 883, 1301, 188, 1302, 1303, 24, 493, 1304, 1305, 1306, 1307, 494, 496, 884, 491, 1308, 1309, 885, 1310, 1311, 1312, 1313, 498, 499, 886, 500, 1314, 1315, 255, 190, 1316, 1317, 95, 887, 889, 1318, 0, 256, 890, 891, 501, 261, 1319, 892, 893, 894, 1320, 895, 1321, 1322, 896, 897, 898, 900, 901, 381, 1323, 1324, 904, 1325, 909, 1326, 502, 1327, 1328, 1329, 1330, 385, 387, 389, 1331, 96, 503, 504, 390, 902, 903, 905, 906, 907, 910, 911, 1332, 516, 36, 1333, 144, 145, 1334, 912, 1335, 1336, 920, 1337, 913, 1338, 1339, 914, 16, 915, 916, 924, 930, 1340, 918, 517, 1341, 1342, 919, 934, 936, 519, 1343, 1344, 522, 523, 921, 524, 1345, 1346, 146, 1347, 937, 525, 922, 526, 1348, 1349, 147, 1350, 527, 1351, 1352, 1353, 148, 923, 1354, 528, 925, 1355, 926, 392, 928, 529, 938, 939, 929, 931, 932, 530, 1356, 391, 393, 1357, 933, 397, 400, 935, 1358, 150, 151, 153, 1359, 1360, 941, 945, 942, 946, 947, 943, 949, 1361, 1362, 1363, 1364, 944, 1365, 950, 1366, 534, 1367, 1368, 156, 1369, 1370, 25, 1371, 157, 1372, 1373, 26, 191, 951, 1374, 2, 1, 1375, 948, 955, 953, 403, 408, 415, 417, 532, 533, 954, 418, 1376, 1377, 1378, 262, 263, 1379, 956, 1380, 957, 960, 961, 1381, 959, 962, 963, 964, 265, 1382, 1383, 27, 536, 1384, 1385, 28, 541, 1386, 1387, 275, 160, 966, 967, 968, 419, 1388, 420, 969, 283, 284, 285, 535, 537, 286, 287, 971, 288, 1389, 972, 1390, 973, 974, 572, 1391, 1392, 573, 577, 1393, 1394, 579, 976, 16, 977, 540, 543, 548, 549, 1395, 1396, 978, 979, 980, 289, 291, 1397, 580, 1398, 1399, 581, 1400, 290, 421, 1401, 1402, 1403, 981, 982, 1404, 1405, 983 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 6, 1, 21, 3, 22, 2, 23, 24, 25, 2, 2, 7, 26, 0, 27, 28, 29, 30, 29, 31, 8, 32, 33, 0, 34, 35, 36, 37, 38, 39, 40, 9, 6, 9, 41, 14, 35, 42, 43, 31, 44, 38, 45, 46, 47, 18, 48, 8, 49, 20, 1, 49, 50, 17, 51, 33, 52, 53, 36, 54, 41, 55, 56, 57, 58, 40, 59, 60, 0, 61, 62, 63, 2, 64, 3, 65, 66, 67, 45, 68, 69, 70, 67, 71, 72, 73, 74, 75, 76, 77, 40, 78, 79, 44, 54, 80, 45, 81, 82, 6, 83, 70, 84, 66, 85, 86, 87, 88, 56, 4, 89, 0, 90, 91, 2, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 89, 68, 69, 104, 105, 106, 18, 71, 107, 108, 72, 109, 73, 4, 104, 2, 50, 110, 111, 24, 112, 113, 2, 114, 8, 115, 74, 116, 117, 118, 119, 120, 7, 121, 122, 123, 124, 125, 126, 127, 9, 128, 7, 88, 12, 129, 130, 94, 98, 131, 132, 133, 105, 134, 108, 1, 135, 136, 137, 138, 139, 140, 0, 141, 142, 143, 144, 145, 146, 147, 148, 110, 149, 2, 112, 79, 150, 151, 152, 1, 153, 3, 154, 155, 0, 156, 157, 158, 159, 160, 3, 4, 161, 162, 0, 163 };

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
            final int compressedBytes = 3302;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXbtvHscRn1ufmBORwCtCL6Q6CrLCACpkN07U6CjHsRwgiE" +
                "BJQIxAMJAiUcEqcCWwWNJCQKZioULlBxUC3PlPIAwVhisVMWLo" +
                "r8k9vsc9dvc3u3NHKjlJlD4N9zmv38zsHt++/7dHl7+5ef7huf" +
                "2b3/7s5eOTO3sfFc8+//GCefDD7cPr9PZPNx5dPnry+cVz+7sl" +
                "/VFF3372xx8vHD94y6Nff3TlaOvPzyv6Jy8fvf54b6fu//jBT7" +
                "cNg/7+DTA/1L9/fqr8pYkSqp618s/V+7fKr4aoUCqj6dcn7t/f" +
                "vn5y8jxJ/z90+8PU8z/5xfXfndvfyi4S3fx2++DSydrWDpns03" +
                "ISBZ3K/mH65aOte3X/75X9f7y30ZXPmp5drOSzTTcPvmfND8o3" +
                "Wt/fH1852v3Lvz/4evflZ6++fH1n76+f/PPxfyr605K+vk6XNr" +
                "JSkM/tF6Tygj78ShfPPj1OzM7h7Yq/E+uXdP8r+Uj3t64+r+Qj" +
                "Obj0em1rY0z5+OW6KvfnXlLuT6n/5f789qtbxbPtan/+dTujd1" +
                "6/mfJrkc+dWj6RfZh8flB/JvYPwvaN/GQd+dluyQ9llNZf50/5" +
                "Qav5vwuiM/evqs3sovW1fBTRqdhXH10sn2L7erbrP5X9bT+mv7" +
                "/S/XnH93dy/yfz/137kS7wWdGWfx8++WFlX3dt9t9tf3dG8m8T" +
                "0xn88+I3Ob5O7fg5YdmnjRKZ6ZXBLYORh7r8WsUfc/srk+85/+" +
                "+5/P/U/h3GV2L9Y+HHJr5I6viihx9/NcfP+7uzJX5u1veUix+O" +
                "mvnPmvlfG9e/TIsfVaFSK3/09kbFHzF9yP+8zX/U3hKf9uPX1B" +
                "e/IjqKPxH9u4bexCfbdXyypKsR6NL4WNp/SPtZ0/5uyPxw/gXF" +
                "38B+tPzbrGmv2vGFND5G7SPo1Bl/4vh40d5tH09nfFf7Nn3W0F" +
                "Wbbovf2/EX4t/U7c+CXkzbf97Jj7jw4/EKX/jyD7V/+ma3s7/b" +
                "jXys/J+HXkpBivKXdv1f4F8Of7No+YjoPw/JH0jpXf18Vevn3b" +
                "l+PjXXxe2l/ffp1KKTfX005vqsQXHRxQ9+fAni76zwohuUn7HO" +
                "z/JoinwyP1kav5413SYfXf6MPj7Fz88Z3znj+zbdav+E9hflD9" +
                "zxcfO3G5/tsPCdpX/qxt/++AfF3/OO1CoHmXZUCbWfmt5bv+qr" +
                "ekT/FDK+NL/Qbm+TzxY+phV+rv3ji9vEiC9J/dwXXyA6o34s6n" +
                "/9D8711fhf6l9Q+zZ91tA7+BrlhzG9wE4Etuc6yHD/BcfP2BO1" +
                "ltRV5pfPpbrqTLW7zZb/KJYFkGxg7bKKbiitzFMz0czQ1ervrb" +
                "qNrr5FdRZiABQI5G+9Pl2tTzXre1GtT5ffddKsL2D/VQz/wPrG" +
                "nJ9dPoB8LumZHYoB+UL2baUful5t3Pz78qXn35jh+fX392p/f1" +
                "3ya+bMQ+vzG4COw6s/3J8rT2GXJ6l+o/bGyt9U3n8WaX/Jg5HG" +
                "2B8UvwD+Iv3xZIAVR3+g/gP9hPu7sN/ksd9+/qeOcVPO/kfRdb" +
                "x84yfIvon1j6B9Vc0Rgr5/yHl0qf+T6lcG9Bfab65/CsVPPP/E" +
                "sO9h2QoTal+gfVA+/tnxmwc/mED9wvKB5Hds/KI7KbbM1d5h/0" +
                "Pxe6j8Ga59iYwvAuUrWL77/aP6J5BffD4hSSX1U2n9EfF/U/+k" +
                "SpuVXtNrKvlN+dmYhHKdZzTLaZuHD1r4l8f/gm9x4viz0n9p/Z" +
                "An6JlnuYVfC8H+Tn3+CtUf23RbfuK05ueqT9b7x7LpDvlky7fj" +
                "fJ6wPjm0Xw8d8aVD1JD8gPogmp8zf6yH/Xfyh8z+I+qLw/ORvv" +
                "aQv3e99gvnvwt/eBek3+PX70Lb9+t7Ee0prD3CL8wsVCy+6WYv" +
                "BvFj8PwD7cd37fxwz78vzif56Dh/LPMv4vqqNH/HLqAq8M12ur" +
                "z+GMj/Aej15y+k9WsD8Bm0n27jpnntC/f4rPxT4dXzwPPBnPoc" +
                "Bdb/yFeftNQvbWmmBV1Z2p8pfYT6rXf/I/gXFt9CixGTfwnJ38" +
                "nsrz2+ytj9C+qfL3rnI7l06tWHUX3Wi38t7RE9qH6M6r8WOrXp" +
                "8vq2zP+25UNb4xtC9UlRfgidf6nyZ1UPm+WIg/zZ2vJ843v0gS" +
                "v+kOErbn6P6X8s+KIA+IPbPgjs8Ot/xPWvKqq+xM7v6bj8ckR9" +
                "61ZI/k86/jJ/Vg5uy59t6u8bejanU0OnPp3sdKJ7tQes1lnpal" +
                "JPsPY/+QooqqVPWOk3U/9R/9X6U9/6/fNH65fOH9nPwfxM2P4z" +
                "6PX+XCvXELP+RXs1aG9W++NZP5afwstfKf9W7ZWbv6mbvyz5cK" +
                "6foT9g/OH+0FC/Uo58roP5qSj9RvaltPMy/7PVNCz9s0lN6TeM" +
                "0lnVfVIOf9lSnzeBBhW2P/HPLytalt6WokL+U7g/9Kbbfp5Lzt" +
                "j1L6H/BwNB/3+I7P99wM77YfXzwfm2wPqkCd0/wF94fgfJB1d+" +
                "4uiIf9UfvWi/OF9F/PvJfPwXRx99fAP0w4Tqrwxf4/oKvH8Bzg" +
                "/E5B/o9NYv7t9Px/mfiecPz58V0++/9vIfxO/35zkypaz+aWz5" +
                "MMR8lons6nc5v/Wu/KcmSUb1j7H8AfNH+Mia1NUA/9iPGsbt/5" +
                "L/5OF/CuojKXt/DQff8fvH509Y/E2j5QPGx9PaV3R/R+qf+edb" +
                "xsyPnEX+aiJ8Yj2fF+Kfe/U9tvzNFvlZUf4W1w8bRuQr+6Gbac" +
                "3881PN+KZpk1WNjlf1iWI8/yjED+Hnp2X4R6o/I+sXhpXT8idg" +
                "fgkXcQXxp/TfhpLZrw+elE2+MBsmeT7Ts+z3W3lq1saIT9u56C" +
                "nw4dT4ncMfy/cX6SnFhyfVtqZ57Rza88kD6SqOvozf7PUrPH5B" +
                "Hfs67L+PnzSuz+iA/UH7+6bbfokfueuH8avXf0H/X+pvOcJsk0" +
                "r9pfN52duxKeFhseUbPzi+B/c7xqYP8Ol4/bPyIwH4iXX+cWz9" +
                "H8++xd9P4eK79pML8jfe+O0U8jf28TE+B/nfQzrIffiW6EJNSi" +
                "sWlfFJXs1oWydGH17lzb+2X93zc539A3QXZjensv9Z53plbnnp" +
                "jY0/AfhEin+l+iH3HzF0xY9/rPxRY9lPCA/F8XVMfDJifn3i/B" +
                "sjfhX5Hzk+Dc6PyvLLI/v3cv8OJOcrRqxPKCF91Ph45c2F+AbX" +
                "d0D+Jzg/FUZHSiytfwnx5dT6ie6/BZ5Pj8u/TXg+c2r+RNwf66" +
                "gobM/e/8X56P5wd4X8u/EYvJ/tsej+2TulX5b7Tdh/oPdDAvkO" +
                "3B8j0T/b+rQfn3HqR+3Ir58/h7345w/Pf2eW9+/Mz4dUD7o/xn" +
                "l/ta89Gv/M7eOo9aNXcf49XD6K8fyLYP0Zye2b3L5G1HcG93Mk" +
                "96/8/Qvfz8R4f4nofhYJ71/B+12A/4z7YR82+Z/BZh02Hws133" +
                "JffsdHn7S+gs7nMeQfyPe0+FN6P0yKT1FWBM2Pf//Wc/4lddM3" +
                "9Yv6fEpOzfmUfHk+JZ+fv2b076X75w/Pb7Dze/7zsdHv55KOj5" +
                "SP/f49xc3/37Ln/xVv/iZSkF10mL+SnW/AP1/R3/+mPpzLf1rf" +
                "P8jr+wsV8yv5x/gr4H5WFP5G/Q/uTwzvh4jyM677J57zax35xv" +
                "d3/PbJf/+EdX7ICPN/ofhc+7Qjca7PdT6POqcbE2BfXHRXe/79" +
                "ov7+d+eP6c77Rcg+eu8f/e/QMyF9/f+TbjWO4+s/ut/mkV+m/3" +
                "bpP+N+K2t9ceuX3l+U6v94/mndUT/w4ydxfYdt/x34LZQeiW/Z" +
                "728OxZ/C96NPjo/Z+F26P6iWhs6/jRY/BPJflh8Qn2+Szg+2f8" +
                "PUj0j7Io5/tSW+audvEN1f32Pkx5j5H4S//fjUFd5g+Qm8fzvk" +
                "30Hm84/9/i3x55WjJ1/U8/usnN+dvX/04k8l8Q94/YYZP4fkB9" +
                "D+2d7vFBffv3M/X7wvSBPnH0ao/8jOj4nPt5LUfo6wv/mqq97P" +
                "n5G2P+v7VfjnO7nri4pGuj+cTpqfOwD3u6D8DO/P6ur+bG8Fym" +
                "e/3PRl/z18uLif6x6fR+eNP7TfKY2zPjG9iOUPe/3kOd/PxC8p" +
                "sD+Mnz8Ti9+E8VfWmPi8WOlHNdm8vl9oaHC/ORq/ZgC/EsCnxM" +
                "OfofFJ1PjtbS0s+8vff4Tv4P1sJ/5h4iep/PPix8D778T3D2dc" +
                "32HUB4X5f9n8Thf/vgqmM+yH//3P/PxE/PmyGP6p3BqfJwD/JM" +
                "74B9VnGv93vh5fO/MDiS2/kjJs4eD92J2OLfcfZ1z+OO5/znj9" +
                "4/gf9e/Ij4WO347vO/uP9gfQteN+6IxXv2IH0NH5I//8oHwVFP" +
                "SY0PYIPxYWge8eoU1CPI4Jnf/E9KnxrRD/jlD/mPb+DKL/F65e" +
                "gB0=");
            
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
            final int compressedBytes = 2887;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb1vHMcVn50s6RWRwCMikgVX60AIWKhw0jhR46US2XIRWK" +
                "AsIC4MuHShKnBlqBgSLKhUKlKoPKgw4E5/wgVIQahSY8DgX+Pd" +
                "veXd7d3M/N6bN3NHKDoYlsTH+Xrz5n38Zt47VakTVWpVq0p1n8" +
                "qqW92fB2rS/t+o4cfez8WXf3x889l3X/x+5/jJq/dePp5++vTP" +
                "h6f/+Pn680cXd89uZ2/v/9g0dDR+5fzr/F/i9b019MrNdv7+mr" +
                "eTP5no8492szB3+23PH9OtW//VJP23AXq/8HpxfobF12+FfF58" +
                "ebvVjwdVqx/vvPpNqx8/ebrfnH7x83V7dH7X3p6vf7z/Kce/Kv" +
                "ZLb5beLPj/wbODf/6nm//fXj7+3ydPj3r+P3/0S8d/RHfodwP0" +
                "P4+Oz3/4fCTSf/Va+5pmn9CvwPVPu4FL3/hwf9Sbof2ue/5k/b" +
                "jn9g8Bf/H8utmlON/Iv8hEJ/snqL2mygd3/kT7Ft0/0T5J+S/1" +
                "D7YtH77zkdv+NPMT5j4/QvrSCY47v+H+L95v1/fTnWtfdf7Bey" +
                "+/6dY38w8eve7WR7dPG4o/bayB1sCExNmvBPEnwgeCdF3dU8ao" +
                "QhmtexP04mGrGIzt7Jpum+hKz+jd+jr6rTGdMH5Yvznti+FvT/" +
                "z+hOfPhSXm/duMdCQWgv6vxPr+j+h8+TMs/+EykPD7t7zxncdR" +
                "h0Tzev+Tsuh8jqabVqMOTWHN2a0E+F4efDLj/kvP18qP95XaMY" +
                "ttaJX1V61S736rme2KNL6tKHNdam9nP6tS6Wdx/7L2pPgoOH4D" +
                "8ZVw/8L2OL4Q4Wvw/EU4YkG9YzeB75gIuvsA0eUHLT5ufuzxbd" +
                "r5X7zvww8fnfvxBZNhf7z+nwxfVkVJwxd8H6l+IKzfyV+bhD/T" +
                "393++85xu79K3Xl1eHJjuntwpGx133QLI+iv7PoRxKdDfBs/Px" +
                "T/AnrHv/L44FbPv6Ln3/6If1H+HZ1/YvxPbZd/6/2XrPE/3NM3" +
                "9qsHhdo5buPb1j/96/cfH54ePi/s0b/vVpiO5Ke1T9/c/OnJaP" +
                "6HM/5euOefuL2Uv1XP0GUtZjQLPuHI54+9/N0b5O8Hiv5A8wPn" +
                "B45P9n8HBnjDljj7NOiHyqsfnCYpsD8k/FPQnt+/3uj8YuxrRv" +
                "8E7a9LPuPjgx/Z/kP2+ESo/7B+vMQf1pTRWei04EjdBvQbiv8I" +
                "V5Dj4fXiOrM0K9auWB1p8O7s6KcmEv8H88PxH9zfTPFjTnxVMP" +
                "5G8FXG/KT4I8YXI/EHIv4INiodPqYY+p0j37n9d6l+iqFz8JnM" +
                "8aEwfpLjA+H+9/ZUG1+0newctwLZxhd/+t40p/e7+OLsrkvW2P" +
                "5lGny2XpPvOhk+0/RGuF5vskuJL1H/Ni9+xMJ/6/Xfhfez4ftX" +
                "eD+bzD/Z8Pu3d/Q0dKCfPjK/6NaJLP/QCk/xl/bf1rZH0dSVmt" +
                "Tq0AOpOP0L7en/fNy/mvWvhv63TUf6HbZXD3r+dK5F10XR86X3" +
                "r+r5+RrR1YiO+yf4H2U8fkvnn47lfz8/X3ulzoP8k/I/wfzB+I" +
                "1o/CTnU2CfpP1/ZF707Wu1q7v29Vr7GPvCkG9o36X2f9vtAb2+" +
                "BDFqW9rO7mtVdezvFM3NZXw68n3rVV9/NZWtD/HnYNawUAcz/l" +
                "ptRvzF8W+w/4j7VZ7/BuOTN7L4oEKuCfLfDYq/iPhEZPwTnX80" +
                "UcT3VXigwP0a8u+l72uVesijW8r8OecbjU/VD4jItV91b7/k/M" +
                "0dX3Lizwh8Y/Bv8uFHUzD/acT6Rvwplv5oBnu5BIC68HEOPh+j" +
                "fxnvL7LsP50ufX8C8Ut8/yzC17aDfzLWnxnfbbr7xf/WVXuE79" +
                "SFvTHdrfbbH9/vf4OED1PvFzK+35e8X0pML9j4DNYPlcs/LFc5" +
                "oH33N91/5Vp+YmmLgqb/h/Z7S+3NUnswPqB3+P8Hz777upffz1" +
                "v5/fTpvxz3e4H+M59fHD+F7SOMD1wImnF6UBnpYf8t1B7n7/T5" +
                "q3M9bbeyvu3xZ3X9fPr4OA7nt860Pnb/VHzKzPxnPj7lut8axW" +
                "eWVf/GAlvG7t+FD2yyvkYD9reJkA/O/BrAUKl+Dse/8/jMzPDJ" +
                "WXym5vGZ2P5e/t143u/B+1/AH6g/wfjO+Ivh/67UB7jWj28I+Z" +
                "mT4X6AcL7LVf9o5J/I3l9ifEj2vsRH10vtl/XloD+rvv1ou4Py" +
                "o9bGp8k/bO+s/7C+v7zx6ftH2B8hvqY89TUmafYfvt9NtT53fY" +
                "6495mLz+tZfs0DX32e14v8mycuOsa3nPpT0+Nvkn/lp8vrQ+V9" +
                "/5MdX9g2PsTbyIIkBP7+UfsieftN7V8kfkg9P0apuIN41eVzA/" +
                "hSkG6v4Pre0TdGrzsJKCbXTr5t//K1Nao4nZhJ9dmBKm2S94mx" +
                "+Oe7/dm8fawj9hfJR2b7U1ENiU9/huvbyekB3m7kfE3D9ffI/f" +
                "vmD9aP32eM5zfHr+qM8sfhr90yXTi/uufApFDfduhIx1RrTV01" +
                "B1T5SzD/K/2+bdv9S+mx+nGEf5bZ9Hfu+sfZ7Q81/hK8bw+cD5" +
                "z/QNHfjvU05SX/RfX1MH8aB/67wv+SG3+xw/ZSsT5GkevvC+Wr" +
                "GrGsXv51S1po7vrmQjo7f8cy8dNk7zMj30/K339i/ahC+Hwj7d" +
                "9xPpbfN1DrByiG/eecHyB/mc+neP8p9UFJ58tDdPZv6P1nsa+K" +
                "od9l+0Ooj0a8/0b7t+fnb8B+o/pR4veD2esPhc8vST80ofl56q" +
                "c9H+5vwPxg/SJhfSRYn0NYf4tQn0dYn4h5/+enM+r/MOYH8+MT" +
                "+NcZ54fvN8P8xe9jYf0Xgn9YL/5arhh/7B+WalGfRa/WZ4mrT/" +
                "5W1T9OxJ+4/GZCfRlUHwjJT1z9ZPi+IjJ+VLHxYyw+nCo+8kV1" +
                "Mv+nHLOsWaJaigspzf/RSv/WXM5hFt99PBu7GfK38tZPyp2/gf" +
                "HLBP1L2rP89xrLuiX6Z/aIVF9Xyr9t03H+HFG+DTjfmegw/x2+" +
                "j5zXB9DL+f2+95FR+AYhPtYj+xCzvj0ffkR7H+rvP/iJ5/9s/U" +
                "nqI4voVHzEl5/+BuFrwD9445wfK387IIhJ8l/LVf/F7z+med/F" +
                "wC+3ju+G86vF/G/90yqoP4T519z+1/FrLcHP0vkn3vMrfF/KjJ" +
                "8s8/7lStIVuT5jgvgN5HcjR46Kr8e1l/r3Uv8XxwfS+IE6fzd/" +
                "+vzk41F+crtQM89PdtOX8pfn8bcHf8b3Yyey/JHGcz9Elt+hfV" +
                "x+Lcrfleffhun0+r25vh9HaL8p+E0Z9u86E1Y3i/bdr9d9/pDF" +
                "+YMEfCvYP1KkcH5r7U3Yg3P+ipeO88cejuWL7Z9R8XfqA4kU/h" +
                "/n/s8tYXR8FOkf4fcbrNyvlEv3K00K+yb+fpMY/cO8/yDVn+Di" +
                "u2T7FMxPRPtLyi8T5G+u8q/g+s/e8UP4NQd/pvknOL8wcL9N00" +
                "+++Ef4vg8KcDh/ck7fBXRve2H+qDC/E8V3+PtbPO+rJ3T5Esmn" +
                "235tTD42wR+Rfydsnyx+57zfZhzPkX0tldPoBd+XiO0v434kBp" +
                "+B749O+PhJwvibgU9UDgLJ/ygF/O/jh3rxq4H3CVUc/0XyT6lf" +
                "5rkfO6fdj7nu5xRjfBn+sf38YBn/sf4C+UX4/iHYHs0Pf39kXv" +
                "wL5XckiS9E9+Op5BfZZ/f9ka7uze4vjNb9Fr9Yz6+oQ1YP3X+c" +
                "hfVPXH4H534Ixu96YImOvP8D+JK7f/r7QZn+ifv+LU7+hzD+Fv" +
                "oneb4fnVPfOXz/uP33L3nvRxn1zXUOfEj6/TZSfFt+Pym7H0pX" +
                "PyyW/3L/Mcw/8P3bUfHb0vwo7/8b5L/7P9L3+QifVb8CVzVlwg" +
                "==");
            
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
            final int compressedBytes = 1892;
            final int uncompressedBytes = 25185;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXD9vHUUQnzvOZomE2FgJWFQXZCEXLhBNwE3OQIRSICwHS/" +
                "ANUlAhKuRibblIUrlIQWmloPZHcJEiZZpIkT8Nd/cese/e7vxm" +
                "d/b8LF5j2ePZv/P3N7OPTEP8xw5/dSOyISl/mcbPr+/8440fqs" +
                "NNc4do66w4unu+urlGzjy0HaPbyMAf+jj2mNAx8vuzcjo4v4qo" +
                "GI9kZ6t3PKcTnb9/frvIj84nxG8o6lNQrHwNxdNzP+V8SWXi/T" +
                "iqSqrnCzGO1rufm+1fZv/nH9+I+fn1rxGtXNHAkuwv3UyuW3cZ" +
                "vHZLvHjcHHo1vPLmyuk7yf1c/Pzl/qfPnjy6s3L4+9mHL/fPHx" +
                "x8vXP809vbJ48vtp9i++E/fys2GxefbLTzt/Zn5XDr7IN2/vsH" +
                "a83xo7e33d67bZ/9ibWfmH7kl69TkXxB/qD8cuNf/v2efV22l1" +
                "x9QatUfNP+Tq6g2rbCf1rTjkiBWfo9+3c/fk2rZTd+7WbjG6pl" +
                "47/f362J9xfifzfkdyN+cP/a+UXnXyn4lfJD9Ianq/2DHdLdgv" +
                "0e6MdH/frtVf040u3PeulmYX8mMf7Y5e2PRD6qCPsYJCL+Mmjf" +
                "P3v25LcXnX3/8eX+qwcHfwzsO/Kv2H/P95cYv6n5A+fRVKOQqJ" +
                "gNVoscbJjsMsevyvgP+28kHxutfGz+2svH96183D/Y6/3vyeO5" +
                "/xXlD+sv5vnDK5w/2JRjx3RhgO0CfHaYn5lo/ZXRC3Ek70bzo4" +
                "1o15dn/KT99TssvfKP5VO0vgrICqYbJZ2WRd8dnq+L9f9N4H7E" +
                "/hf4D57/81vl3TVjClo5JFqtG/r2z692jndOCrf3fNvErJ/A+v" +
                "3639m3lf/wkZ0eH9nz27cSR03N0CH0tEX8ycriP87/xsQPEN/A" +
                "+Xc11v9w/FOw/uefXr+/m+v3X/358vfj4x/YB29+YoPrT4+PQw" +
                "Eyuh/gf9TxmcB/sXTbEbr8cBTfn7K+xEjjC5yfC/GfJZ0/Ph+W" +
                "LvJvVXh77/N3O8tfZ/k7efL3Mglfxv/SaJjV/h3xy8engP1Kid" +
                "8Rfmzl5iN3fpE9f/Hh21p80eZfXyI+2MlPAJ98nQOfhOeTbB/F" +
                "+JDXPtE8Ucb+73zITyN+fP4IP03EVzn/hz4T4vdurJxK+yDGz1" +
                "Lnz1w/cFOdv1++5fUflN8gtU7ER+H4gD4xPqf1ryJ8qdLEB8i+" +
                "6OITiG+ZfvXmSihtS3+6F9Jfnj+xvwDC3hH5L9cfAPsH+oHqyz" +
                "GrkRRr63ezEcv5FO0eKgF+HmF/lPPj/oMmCr70GFJ+/0n9JRH4" +
                "9AjOyB+/ieeXxr82zn8qx8/Yv0Le+n4KkB6Br+r1U6h/MXSbjR" +
                "6qH5vL+rEef2Hn5+vPgvWxnwzjq/YXqj/L/dMbED81UVazCFnw" +
                "hfzfCfmtNr9J0J9I+df0H4jpk/lXdnx5/TMNn4nFjzz2XWc/jF" +
                "Tggvj1kU5/RxriYvHxZs4P7K+d9R8t2J9AfSikv+H8ja1/VMn4" +
                "UfD83PXUN0b6XQjz13z4OqwfqPB1QXySqF8APyq5/Dlh/8nxE+" +
                "pPAOuLOl+PfC79fFF9pVHKtyj/T+6vR/k9ri/L8WvjIWB8uWHz" +
                "c7F/taH48+m8v7Sa1adoVp+ivj5lIT6gxcfT6ks58+cUfBfj8y" +
                "YbPg/GX4x4bXR8jeJDBp8vTUnWdsO29G6J67vtwqzr9lWaAX7C" +
                "4w9BOlo/zw/xZeX7HkzP5F80+X3F6o/OvyvxWT1+C/BVSI+AtC" +
                "axL5nwtdT3S1p8Uqk/Av2Mzb/ixp/Y/lxf/o3wxVT44Or91cL7" +
                "LcX4neh9RwX0g6Fr50/H305pWN+mNPwN4WtgfkF+ht5/KPE7m7" +
                "j+WPw2tf6gw/8T8C0b5Z8j9beIxDchvgPOH/fPZrvftP5BPb43" +
                "cf7cTNofLMAPkC/i8/uJzw/jczf7fKfuT0fxcxo+TtnsZ4x9NO" +
                "r4JFf8nbO+gfJ3Zf4P44+GlW+Uv4vz+/Tz131/AdBfFP+r6+s4" +
                "vsnBX7HxTZWef2d4ny9BH7j+Hx1dnR8J8R2bur9G//72WvQnMX" +
                "6Oyv9r8L/1gi6rv38C0NH4MD9Vxpfp45+SqD6sjc9gfKTM/yTj" +
                "K/L/ad43XKH29rU27RK26sK18Z1pXQ497Efw29f47w9QvQ9rpq" +
                "0/3vD4e/r6vqD++b/eP/CfyvplNL7iJP572D/Evx9C8QGI3zPb" +
                "1zzvQ2LsM6JPXf9A8jVx/Jm7P3t0f+L6RMi/TRwfQXqO/mOVf+" +
                "P1B+L3yvEF/bOq+BDbRxG+UzHxuwR/rcLxlS4+WLr/UuLPN319" +
                "EJ/Pgd+J4hu/KZC/303sv9H2h0yNX0B8QYxPTNP/suz3JUL5Lw" +
                "f5p83Gr65fJ/U3xtSHM/jPisO3lf2ny67fTTx/2ve7xcRf2voB" +
                "jE+1+KrMvkXh9xnth7p/j3B9nv3+F2l/S6r9g/is0v6A+PGm40" +
                "vXMT9zfrC/Wvv9HBPTUX4pzT+D/hnSBfKp0M/l5y+68QX2kbXP" +
                "2vvF96/Df7KcLzf/v1ug8Ow=");
            
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
            final int compressedBytes = 5098;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqlXAuUVtV1/kURygJxWVIf+EoaSZWgUSvqqo1n/n+GTs0qYs" +
                "RUIzI8bCNOeJhQVB7N3Hvu/Yf5Z+jCdGmgy6RAnaykj2jNs01N" +
                "WxdZiastBBHJMOOgKDMiEZIoPirG7n322Xfvc+6P6Wq5656zz7" +
                "e//e19zn/nv+d/0Xa47bD9etthPJKljYXZJ9ILaAT4cNfktsPr" +
                "701/hxHgfIOtnq62w+bktsONdkAfrVTaDucnIZ52wNmLFmKgss" +
                "0u8XpfS8dWKvUr2AMRGbZdc3qPpbMQq7h/ko0wGROj5XDXrhAL" +
                "7a7zJZL00jFopUeEq5WzJ137XqXSvb1SsY+TJ6/onDofI7bffp" +
                "V1TLtpt7uxhX4vnO/YIRqBPZy8ZdqzjfZJRgA7wlbDQnuKaa9f" +
                "a9qT7yKSnwT+9xzrKJxv2p3OPpZ90cceb4y3B22nqGUPUN93e/" +
                "aWfda+bffZQfuK8o+Bc7yMQeNp+6uWl5PXAuyAst/w/X7fP2/f" +
                "Ne3rTwedU+zdircHzrfI7v6BywWVr782G5vNBM8OmE2l4B7S2R" +
                "zyTAZzzU7OTrXPmfb0UsA6TWelgi319h0e4TjdaDrzHYII13T2" +
                "ngrtGNPZ92lGYR07YR3Bqg+C9Sah9ljPBynCHsfHsXWCqME6el" +
                "X8Z9+mXvlBH9axU+e3v+qZmrweYmLDOnaGc4J1dKPsFJ3ZzfUt" +
                "stdf5nJB5T0HYR0/Tt680kyfEVjHTlxH9Dn/NrMt248t9IfgfC" +
                "Z7hUZg/8xONdvqb2THwP4JYRCzTY5/g1G223Gf9TGHsa0uzn6R" +
                "vUbcbEdjo/fthfw761dKfL1a5BrMhrKXsuHs+exA9mqBHoHzdZ" +
                "0x22O29bQmbzp7wGM/DRj7XPscnAfhHMlepqqzn1cXS/0O2aXn" +
                "hC0+ilmNbVYUvyCOeTT7ZcFMTQp9yr27HlIeJ2+YdP1xwoUjB4" +
                "7s9ljDpNld/spySOMB8uUTEat/UeK7Txcl/U/rlzP2zE6Ol3Nq" +
                "hkSSGp3ZZ+K5liOANSfOK/6w2nx8PqFgTjKToJ9EvZ/FJDrssL" +
                "0T+n5BhKujKJIRbKuLhAFtPzHzie4+83Wl0q8zS69z6DExer7M" +
                "cXFNcVW6skoFq4r9UkUx+/44b1iTIH4didln+vKPYgv9JXCenX" +
                "+QRmD/tq2Zvh6Yu+nDEw/uZURIPlX7qwvyafnF3nNm7196vXPd" +
                "3fZqie+eLkr5he4xnu5qLOkXNZ0DFe1KP462rTarKb/AtR/i6P" +
                "w8svKLqgvCeeRn6Xhiw/W4OM4rfkFcrR/OP1Iwl5vl0C/n3s1i" +
                "OY/tTWZ5z52CCJfZclJr56DVPVaUKpXeRznC/V2fJ2rdj2tPcU" +
                "Usj3NoBK6rXWlLiJVt6aXG+rnNlaV6t45JnDesQKuKevL55JTs" +
                "KdoP8TySlclpydvJb8L9en4ypfEWIL8Fe8Sp0K+Ctf0C2BeBfW" +
                "/yEbsOxtuTS1zUR+Gc4ayPdRtSS65KrrF/3vuYXqXWnc4zK2lP" +
                "/qD7UPKHySdgdB9i6QLZPybzkjvy68DjsGRR8pnkruSkZAyOGn" +
                "+S3uRZk5PTKSL5AJzn2K7kz3D/mFwA6/FjiLs4zJxc4dnX+ple" +
                "l5ikmm7RrKwuK+E4c7iqfKuPvl2vV7IYzjvNErME1nMJ926Fl/" +
                "DY5mZJ48eCCJfZcmqrba8oiaKfzX+JWmOy9hTX45I4h0ZgHb+c" +
                "Lgyxsq1zso7OrJXTLXr22ffjvGEFWlXUW6e1ToMM07h3M53GY9" +
                "vdOq13OuHCkUM8ob/7H/yaTdOK/u/6QolvXCxKwZVT0tdI9Sfp" +
                "PeWcmiGRpEZn/YJ4rmSnW/Tss3+P84YzjKslj8lNDuuZc+9WOO" +
                "ex/TuT984gXDhyiCf0r3/aP1a5VvR5VXzjKlEKrsiSvkaqO9N7" +
                "yzk1QyJJjc7W0lzJhutRzT77UZw3nGFcrfd0mA7oO7h3ng4e23" +
                "80Hb0LBBEus+XUVs/NoiSK/np8TNQaq7WnqKwjzqERuG99JV0X" +
                "YmVb52Qd+1hzZV09rON/xHnDCrSqqJtNZhP0m7h3nk08bhswmx" +
                "rDhAtHDvGE/sYZPscmreifrwckvm2/KAUrWdLXSO/fpD3lnJoh" +
                "kaRGJ2YO51qOgHV8J84bzjCu1nuWmqV2N7bQ7zVL4VEbohGMh6" +
                "uTzdLeL9gnGQHsCFsQ7c76tdQLAqyjTmknoYSRZQ+2XCNqjW8r" +
                "z7Nw0vsUS+McKj88Y/Q+kfYF2IEwwmH7ff88a8B+abKK2SNcXT" +
                "2s47vg3aHz2kM6m0OeYbZ9zixNLzVLcd9D69r1VHEnh31P/Wzc" +
                "91QnJVN6v4b7Hjin5jcnvdDDfhn3PdDCvie/Bfc9SR/ue5INvO" +
                "8p9gxu3wM71k/K41ffns/mfU/jO7Tv6dpZif7hvifpoH1P/im9" +
                "70kavbvTr+RzHcvve8D6AJyw7+n6T1Bz+568Q+978pvcbGDfk9" +
                "8GOOx78ht53wP27fn8gnmZa//Y686mfU9O7a35vK4dtO8pKqV9" +
                "z2azGa7Hza7faza763GzHw/bb5rN6Ta4HgH32BH24oEoXI+bxU" +
                "+9Peoeq52EpP3sw+uxfonEp1tFSa5H1iI1GTnlp83m6o70EZ0T" +
                "rseoJsD2k+WuR69Tv1jF7BEb7jNFLqj2q+DdofPC9bg5rAOvR/" +
                "8e1XMQfylgbaYNGG3ct6zlEY7bXjBtvX8riHDJ0mfZYlv03fPj" +
                "C6K2/m7tKZ5x2uIcGoFXlmvT74dY2dY5WadlbXPlsGasL8wbVq" +
                "BVRd1sNbBLx5Z659nKY/stRjRHDvGE/mJFtmpFv+/JJL7xhCgF" +
                "K1nSD5H0h+WcmiGRUj1lDudajoC/3d+L84YzjKslj3t3/3MQvS" +
                "wZzx71vvswv4eenMVYcmHyIX5PnvncipVcqZSGdd5u9dlB4+f0" +
                "uUL0SuxTya2urhWiqN/Rh3XcC6wJyRJGkjOgwjOpguQ8+lwhfK" +
                "3rRn42ydXhJxjhY5hXkxtULbOLu8bNvu/0KvMlR9ths8rAa2Zs" +
                "qXcrvIrH9ruMaI4cOLLbYw2KYSVRJKz21xLfeEeUgsd4la4ozg" +
                "jrOFDOqRkSKdXDCt0Vz7UcAawlcV7xN6vWe6oG7lfYUu88VR7b" +
                "fw4R4TLbVGEdPcpIqCTx/n49W9R6F2tPUVk1zqERdz0eDbGyrX" +
                "OyTv2PmiuHNeefjfOGFWhVUTcPwrHPtdTvK0betv+KLXAfDLju" +
                "cOi+wpJ+n8vgPeJzmFLovVMp7fPefUUupyYjqdGO1Tmb1PRgUf" +
                "M+1qB6dExYM3F81fuCvMEMZX7Mdp5u012bji31tek8Yhv+El0r" +
                "HDkQJYT91Nfcu7HsER9iWqF3lShhDHqx1/oykhrtqTpnuSbCqD" +
                "rWKM8jrJk4xNLeeIYyP2Y7zxQzBfop3NeqPMJxrSqtcMSiCOx5" +
                "RP7i0ZrCPkGRz0fvfu1hfzmHHMSwv6Fr0YqSlU+x4nmEc2I2sy" +
                "Qv6es6gmch59Gfo7u92ZqK+vzbnq3vxeFn7oza7c3u1/rOH94X" +
                "u08Vlb65dL+OX838mu8BrLHnhHWH/vL3AOhsWdP8ewB67wHPj6" +
                "vivOEKxHd4f7+eZ+bBes7jvjaLRziuzcIWXs/ME0xbFMEoI2yx" +
                "Lfru8Z4lan23ak/ZL4qCIMN+WGNxRNizxomVw5p5PpJXx4Wqom" +
                "7GmXHQj6Pee8bx2F5EKCPC1VHMISS0dLy/HjOJ7fuc9kivc+gx" +
                "Meq32GkhVrY5UiqDuLllf1inux774rxhTYJI1YDdY+6B/h7uW+" +
                "7nEY7tD7EVRLhk4Qn7Ho8ywhbbou/+Kl8Vtb5V2lM8jvfEOTQC" +
                "Cvfbk0OsbOucrNNyf3PlsOZ8S5w3rECrirr56YkP8LuWejrsZc" +
                "1ZzSPLeIjpEdq+svfRJ5b92PtV0HW+YKTHZ6wTV+uu9kkx/0Tz" +
                "4GrxTB9NH9N3hPCOkc6XqyT9rDwnp984wSsv/FZXh721eFX1vf" +
                "Lrr5Yj5ddiPsMCrePV7ohzuHdZTPiqVN878D5Tvu/hmb/KTLss" +
                "VA3uM0f0XaqcpXntZiIc/a6d6L450V+MJnpbI8Kd6GMdQkqEuL" +
                "ZfuE7TR7C+jlUeyqWyiarkj2oMaoqr8nzW6W/iV3UW36cIvV6j" +
                "VEVRtfNuMBug38C9822go3Za7TSzwd5BuHDkEE/oL2raoBX9/f" +
                "g0iU+3ilLw6Jb0Q8TOL+fUDIkkNToxczhXX8UWPXvbEecNZxhX" +
                "6z3LzLLqS9hSX32JR2xXKoIIF3E6iaMRxojrci1jS+szmz3kjf" +
                "3M4fzIsotCrKwoc2KN8jwku+QilvaG+qEqsR3zETgGXUv9YDHy" +
                "tv0RtsB9JOC6w6GDhSX9oFs97xGfwwajeLYGvXcwQBVHarR/Gk" +
                "SXa3qkqHmQNageHRPWTBxf9WCQdzCslefHbOdZbVZDv5p751vN" +
                "Y/u6WZ1uI1w4cogn9BdZnJ32s8/dZ34m8fB3vVr7dFyoHyJ2eT" +
                "mnZkgkqfmo1+K5+iq2YNudE9uuiPOGM4yrJQ/ef8xQ/t/1k7ue" +
                "MkN9k82QGeJ7H9nAGvLfmH4PMfycS+6D9Yrw5TWUGXIZhhjJ3y" +
                "2yDpFPvyLrmoOfcyGOXvH3ne5zjJFXZKhphuxf6PssRvC9lT7n" +
                "4gq459xy9w2rCNZliE55HxdH+XFGunZoVV4b8yXzpeoxbKmvHu" +
                "MR2/Ac4FrhyIEoIeynvnrMPXd4j/gQ0wrsoSzkxV77ZSQ1tj2n" +
                "o8s1EUbVsUZ5HmHNxCGW9sYzlPkx23lqpla7Dlvqa9fxiG24zx" +
                "UIjrSFJ3E0wpbdSTbH+Pt1EzXC8dT5taLUhCz79yFWVpQ5sUaY" +
                "WaoOayaW3aHz6rhQldho298Nr+/6p/X+084M9+a6Zdxuj3bOHf" +
                "Y2h18F8VeU9+HdDzbfyZZ3xe+zD/92s3di2D7RPrw+N1Zvtg+v" +
                "f7P5Pjx+tyesyjwMx6hrqR8tRt62u7D1zIeV92GPjhaW9KNO23" +
                "vE57DRKJ6tUe8dDVDFkRrtd4Lock0PFzWPsgbVo2PCmonjqx4N" +
                "8o6GtfL8mO08XabL7sYW+r2mq2bsEI1gPFwzpguuXdcSxr2M0B" +
                "v7iyzeZp/7SzBhPFv8PYCa0Wic0T7tavyejtaKjNr9ZLnvAXgd" +
                "PQ+7p1wzVcgsyRvO0EXr7wGQZ5FZVH0BW+qrL/CIbXguLRDhIk" +
                "4ncTTCGHHdOi5iS+szmz3kjf3M4fzIsv8UYmVFmRNrlOch2SUX" +
                "sbQ31A9Vie2Y68y66lFsqa8e5RHb9l+wBe46zaUDUULYT33VfS" +
                "+FPeJDTCuwBy2MQS/22i8jqdE+oaPLNRFG1bEG1aNj2Ib9Y5GL" +
                "WNobz1Dmx2znud5cXz2ALfXVAzxiG7gFIlzE6SSORhgjrlvH69" +
                "nS+sxmD3ljP3M4P7JgHa9vVpOwZU6sUZ6HZJdcxNLeUD9UJbZj" +
                "3mfug/4+7mvtPMJxrd24b8CHHDkoItagGIrjeEGJH6upzxWCCp" +
                "pnzG7U0fZAzJBIqZ5nU1bWEcKSvOEMw2qZiffx2gy6n2Nfm8Ej" +
                "tkF5huwtxMv7AvKG+4ya+/4eccN9jz2o1fj1DLHxxOP9Puciln" +
                "1Cv4aSmuRzLs5CanTG82j+ORetgs4tGroKYbvXMw/BMeBa6geK" +
                "kbftCLaw5g8FXHc4dKCwpB9wj5T3iM9hA1E8WwPeOxCgiiM1Zj" +
                "aILtf0UFHzAGtQPTomrJk4vuqBIO9AWCvPj9nOs8KsqF2OLfW1" +
                "y3nENqx5gQgXcTqJoxHGiOuyrWArVNOe2uV46vxaUfIjK8tCrK" +
                "woc2KN8jwku+QilvaG+qEqsR1zIxwjrqV+pBh5245iC9yNAdcd" +
                "Dh0pLOlH3Op5j/gcNhLFszXivSMBqjhSY/aDILpc08ai5hHWoH" +
                "p0TFgzcXzVI0HekbBWnh+z0YN/7dUX6a+++iLcf17kEY7RRkye" +
                "HcTLzxnkDZ8fUYni4udH1iw/P3J27E/8/EisPNfPj1KTPD9yFl" +
                "KjM55H8+dH9Giv1tCIsN3z41qD38Bdy33tBh7huHYDtohojhwU" +
                "EWtQDMVxvKDEj9XUa+dXNNo8Y/5AOWeoyZFSPc+mrKwjhCWq9l" +
                "CcJbhfk2eCmVCbiS32oDKTRzhGGzFGzAR7hC2InsDRTs8j2Nqj" +
                "oks+tkhNEPEgjhl1NlGVmpCZPx5iYYRURXzW0ZnLVRR34Jmhlz" +
                "XiKqRq511j1kC/hvtaK49wXGuVVjhyUESsga1/rLzNPpe5NYzX" +
                "PvJrtHnGtEVHa0WpgE+x4nnENRObWZI3nGFYbcEca8ZWD2KLPf" +
                "y9H+QRjtG2vxBEuAZ/lV0gTs8j2BKLWvKxRZqCaA9l1NlEVfIj" +
                "0/4yxMIIqYr4rKMzaxteF/oapA6dlzTiKqRq511pVkK/kns365" +
                "V02OHWv2JEc+QQT+gvHquVWtF/PrMtjNc+HRfqh0j98nLOUJMj" +
                "pfpKhWZTVtYRcM+4Os4bzjCuViLjzynkztuKvzrZcKL3ce3vN/" +
                "8+RfatMi765sr///u49Xf/N+/jhgyaTbP3cdMtwfu47/1f3se1" +
                "n8fvBLhPdMZTb/qLGfcnb9C3F8B7VqWS3wz39378vYL6dv8tFO" +
                "U/FTqG6+hYV7Ku+4Tsk+rbCv34ey5f8/nqceVvSvTj7xWcGv6P" +
                "Snfg77k0y32y1Z/PTSYkLxd1ngHsM30N5+E6AqODVfn3XGTnty" +
                "VXO/+Noql/z9V2TXIDIMXvuSgun4O/V8DfcyWdhNDvFVjfLDQL" +
                "qaWeMTxq42rjzMLsbkGEy2w5yxbb3edxhHseHydq3VO1p5jXwj" +
                "iHRtxnE+eGWNmWXmrUmbUyPD+qmrvnxHnDCrSqUv8f5TB9xA==");
            
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
            final int compressedBytes = 4002;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqlW32QlVUZXzEavqxAUEExxBQrS0DNSsXD3b0hoNiH3x+wBs" +
                "S20jRDMkRNLOfed3fvvSiwgIYssKtMM5UTM4FNfsSYk2V/4Fdq" +
                "f2RT6l826z86Tc30MXXec87zPs/vOe8uZPeds885v9/z/J7nOf" +
                "d97773XTBLzJK2NrPERNvWRqt83rEXEfYlbx7pjOasn7/6n0rV" +
                "2uCFvFwXGgsRK1Nkf9LJuylTxpr7n9N5sQKpyupm0Aw6O0jWM4" +
                "PhaJ/QPsEMZtMDzj58MIN8sSODUjFguaaMl5yMQ31EshlpTtSk" +
                "yKAWRp4Zew3z2rDsvu+ozosd6mojk5nM2YysZzJaV7YQIn34YA" +
                "b5IkcmFeO59DLGS07GoT4i/Z9Pc6ImRXL11E2qLCPa2hqzdV7s" +
                "UFcbmbVmrbNryXpmLa2zsxFhX/I2a+vPEkoIKnF8yNs4JVVTu7" +
                "hW55CIfy+uRKxMkf1JJ5tVrow1N+bovFiBVGV1c8gccvYQWc8c" +
                "onX1bXOo+nbA2YcPZpAvduSQVAxYx4MYLzkZh/qI5HWNVRNHBr" +
                "Uw8szYaxrh9vGTOi92qKuNTI/pcbaHrGd6aJ3NMT3Z3ICzDx/M" +
                "IF/k6JGKAeu7F+MlJ+NQH5Hs/DQnalJkUAuj71u61zSirW3xFJ" +
                "0XO9TVRmaT2eTsJrKe2UTr6uuESB8+mEE+ZKi/SHPi/D5+F+Ml" +
                "V9SW6COS1zVWTRzJ1bvz8eu61zTCnY/zzab6C1ibzoK1emaL2e" +
                "LsFrKe2ULr7AJCpA8fzCBf5NgiFeM+fg/jJSfjUB+R7MI0J2pS" +
                "JFfv9rFb95pGuPPxNJ0XO9TVRmaCmeDshGAjM4HW2UUBJYR9ee" +
                "Z+z0SfgOBMxsffMxNlRsmwlTnkmjz6r0IsnVMkV+a6mZfyWKfX" +
                "nq7zYk2McNUOW2PWOLuGrGfWhKN9UfsiRNiXvHmkM5qzvr9/XJ" +
                "SqqbNxjc4hkfxVO4hYmSL7k47OjL5Uc2OvzosVSFVWN8vMMmeX" +
                "kfXMsnDU/9TxBCLsS9480hnNWd//vn4iVVP7uEznkIg/ZxYhVq" +
                "bI/qSjM6Mv1dx4VOfFCqQqq5s9Zo+ze8h6Zk842se3jydE+vDB" +
                "DPLFjuyRivF8HI/xkpNxqI9I/0/TnKhJkVx9yIy9phFtbc2ZOi" +
                "92qKuNTJ/pc7aPrGf6aN3xJCHShw9mkC9y9EnFeD4+ifGSk3Go" +
                "j0j/sTQnalIkV0/dpMoywu3jbJ0XO9TVRma32e3sbrKe2U3rjs" +
                "cJkT58MIN8kWO3VIz7+DjGS07GoT4i/U+nOVGTIrl66iZVlhFu" +
                "H+fqvNihrjYyB81BZw+S9cxBWmeXECJ9+GAG+SLHQakYf19Pw3" +
                "jJyTjUR6T/eJoTNSmSq3d3XFt0r2mE28dbdF7sUFcbmI55HfPc" +
                "OzWPrD9j5tE6m0+I9OGDGeQpB82Jy1+VX2G85GQc6iPS/3yaEz" +
                "UpkqsPmbHXNMLt4/06L3aoqw2MaZiG288GWb/DDVpnCwiRPnww" +
                "g3zxXjWkYrwPV/GSk3Goj0j9jDQnalIkV+8y9+te0wi3jw/pvN" +
                "ihrjYyHabD2Q6ynumgdbbQdPTdzwj7kjePdEZz1vfn469TNbWL" +
                "HTqHRPx7sR2xMkX2Jx2dmXz6WrLm5qDOixVIVVY3S81SZ5eS9c" +
                "xSWlcuQoR9yZtHOqM56/vvXQ+namofl+ocEslf2aWIlSmyP+nk" +
                "3ZQpY83NIZ0XK5CqrG66Tbez3WQ9003r6muIsC9580hnNGd9v4" +
                "/fT9XUPnbrHBLJX3ldZTVhL5w9DBkllbHm5l90XqxAqrK6WW1W" +
                "O7uarGdW0zq7HBH2JW8e6YzmrO9/X5+Zqql9XK1zSMTv418RK1" +
                "Nkf9LJLitXxpr7juq8WIFUZXWz2Wx2djNZz2ymdeVCsznrDjj7" +
                "8MEM8sWObJaKcR+v5vj+s1kJdjLRR6T/nDSn9ODIoBZG3g32Gu" +
                "a1Ydl9a6bOix3qaiPTMi1nW2Q906J1tZcQ6cMHM8gXOVpSMV7X" +
                "ezFecjIO9RGptac5UZMiuXrqJlWWEW4fO3Re7FBXG5mmaTrbJO" +
                "uZJq2rbxAiffhgBvkiR1Mqxn18AOMlJ+NQHxG3j82xauJIrp66" +
                "SZVlhNvH23Re7FBXG5mJZqKzE4ONzERaZyaghLCvjCKfgOBMxs" +
                "d9fAhjmWErc8g1ebh9nFhWk65KVua6uSblsU6/jzt0XqyJEa7a" +
                "YdZYZy1Zz1haV2YQQmjtb8RyhNYIMaTEinEf92G85GQc6iNS+3" +
                "uaEzUpkqunblJlGeH28QGdFzvU1Uam03Q620nWM520rpyOCPuS" +
                "N490RnPW9/v4YKqmdrFT55BIPB87y2rCXjh7GHk3ZcpYc+slnR" +
                "crkKqsblaalc6uJOuZlbTOKoiwL3nzSGf1F8Oc9f0z2PtTNbWP" +
                "K3UOifjf+QsQK1Nkf9LJFpcry5rdPv7BrKy/oDvFg1RZvTpSHX" +
                "GfwCPBxruzEVpXzqiO5N8LCWHfMDOnVEfqz4ZI4nBGHOub52XG" +
                "/OfWL1JezC+1GPH3eP9ADOdbz+VIrix0IzPTvDYsu1/8T50Xa2" +
                "KEq+ZoNz/C+0iz7FqX/Tixvf/JGTuHPV1HcCbVL3XX3CqvfETi" +
                "vf9m3aA31qt6hKNrd7kc47RHriG98hmtqke2nluuWzleH4j6I1" +
                "qNX63XZe12RezgX6yv98ufHdPMtPAzWMLCunIvIdJHesso9kN1" +
                "jvXzl0lBqqorW+WQEf66PqbrljxWx7PQTaosI9w+/lHnxV3S9Q" +
                "ZGnrfpdZ0tw2tWX9dlV488n8uu68Zt+oqk61qckSe4rhvP4OcR" +
                "8qNd19nS8usau982BXvTOyB9y/emZB9XuE+PR/7ffaz9AD4fj4" +
                "tPpoff3z7Wv/m+9vH6k/l83LY43ceyLLiPZofZ4XrbQdZ3uoPW" +
                "lfPMDv/9Gnz4YAb54pz388ZbxHnsBY5vvMFKcK0k+og03kxzSg" +
                "+ODGph5N1gr2Huvl+L7rddp/Nih7rayKw3651dT9Yz62md3W3W" +
                "N95jhH3Jm0c6oznr+/lLrNZ4VzJFZet1Don48/EYYulc5iSdrL" +
                "Nc2e2jqHlbl86LFUhVVjfu5awh6xlD68pCY5qnMsK+5M0jndGc" +
                "9f38RVZrjpNMsY9G55BI3EdTVhP2wtnDyLspU3b7KGre9ozOix" +
                "VIVVY3y81yZ5eT9cxyWlfmI8K+5M0jndGc9f38tVRN7eNynUMi" +
                "/lnrFMTKFNmfdPJuypSx5m3v6bxYgVRldbPBbHB2A1nPbKB1Za" +
                "fZ4D8fNzAmZ3LIWR5HSqwY79Z2pmpqHzfoHBLx97X3IFamyP6k" +
                "ozOTjzsfRff3jdd5sQKpyupmspns7ORgIzOZ1pXdASWEfWUU+Q" +
                "Qk/5nH8Rr1g6bWIIU0m1yTR/M0xNI5RXJlaWasQryPKi/WxIj0" +
                "NjPMjMr+/Gewlf20onn46XxnSN+A5COwtAp+lf1+x/YHP4oJGC" +
                "uwWmACm1udQ0awRloTVUJVBX/S0X3Q3J2PRa7gJVnSl3WE/sjb" +
                "M7vMLmd3kfV17KJ1tpkQ6cMHM8hn34nv1S6pGLFXMF5yxRmR6C" +
                "PSnJPmRE2K5OpdN9/WvaYR0j9VLKs2MuvMOmfXkfXMOlpnWxFh" +
                "X/Lmkc5ozvp+/rtUTe3iOp1DIv66/jJiZYrsTzpZT7ky1ow9aX" +
                "1Uld7Fffnh4rv50WL2mMvezWx8TnF4tCcMvT/Jn1Nkj0m15Lvz" +
                "qzxvvOHvYvapbzOHOTp/TpG+Gm+iVz6jVfXwaM8p+naSR/Yzid" +
                "eGdX7+rkLPKaS+9vJdDZvh8DNYwsK6crEZbvYFnH34YAZ5VGfO" +
                "78HtGC85GYf6iLj7x+GxauLIoBZG3g32GuY16J79U8WyamUkfh" +
                "e1TxTPwdy70fT/mqV+Az5jHP1VWzX2U7FKnefNcfh9PH1yV34+" +
                "1o+pZ37wfXrU52Z1rT7K+Thmf+Ws2Wf2hZ/BEhbWWc3s898LwY" +
                "cPZpBHdeb81fUUxktOxqE+Iu583DdWTRwZ1MLoe1L3GubufBTd" +
                "s3+qWFZtZDaajc5uJOuZjbSu/p4Q6cMHM8hnN8UcG6Vi3MdXMV" +
                "5yRW2JPiLNR9KcqEmRXL3L/IruNY1w587NOi92qKsNzAmeP/b+" +
                "788ftVLy/PGO0f6ucPLPzZpH3tdzs56Tef6IfZ3s80dzwBxw+3" +
                "mArN/hA7TO+gmRPnwwg3z1rPheHZCKMe9ZGC+54j1O9BFpHk1z" +
                "oiZFcvUhM/aaRrAXVoFZsFbPdJkuZ7vIeqaL1lkDEfYlbx5yVp" +
                "3OcRwf93F6qqZ2sUvnkIg/H99CrEyR/UlHZ0Zf6j7UrjvFg1RZ" +
                "3QyYAWcHyHpmgNZZ0wxU9gacffhgBvnqrJhjQCrGfZzF8dU/sx" +
                "LsZKKPSGVvmlN6cGRQCyPPjL2GOatRfZgXO9TVcmTjY43zG+fl" +
                "13ljrr/7vre4U3D70bqAf9vbH/n78JGx72zsfLw7sI/CPfSdJ/" +
                "x7YfEJZH9Yov5juu+Rn8Py76Cj3vfMOpn7GlX7Co1qG3dzqpka" +
                "fuaWsbCuzDZTW/MZYV8ZFSIJwRlxrN9YxbGtBZJhK3PINXm4+5" +
                "6pZTXpqmRloRvNY51cB3aKNbCq6Ltu3N1p/jNYz9RpnT1OiPTh" +
                "I1/Vn9UaprjTpjlxfh+3YrzkZBxXpDO6a+TGNCdqUiRX77r5ue" +
                "41jZD+qWJZtZEZMkPODpH1zBCtK+eYoVYz4OzDBzPIFzmGpGL8" +
                "9wrvYrzkZBzqI+LOx6GxauLIoBZG3g32mkZI/1SxrNrA+L87fs" +
                "N/EkzAzyd/x/ULfW+Yfz7auem/zMD7teok/UknPn0mNQZOdP9o" +
                "b5X3bY09+s4tO9bYaSexvp1WHbFnEhvuH9PPsOqk6H2FV31A/7" +
                "2ZvOwy+fmoeXtP6d+v39GHQ4tZ7bnqO61tjEh2dMQd08qZ2m8d" +
                "No3XrQWsknvHysbQD171Y2NVUPsNY6TrxzStI+Mpf+4l2fKeZb" +
                "X5MNvNdndebifrz9TttM6eJkT68MEM8tWZ8ZzfLhVj3pkYL7ni" +
                "Wkn0Ecl+meZETYrk6kNm7DWNYC+sArNgrfmwp9oPuHN1vP0gX9" +
                "d2iv2QPc2e7j5RFtvpHjnDnmXPtufYc8N1bS+wF7rZPDcucuMT" +
                "3udiNz5lPx3ue+yl9jL383L7Wc9dbRfZa+LdxzhbdcgX7LV2ib" +
                "NLbfHXQvu1aG+2t9o77V12lZt3unG3/apdZ7vsKXZcUOhdYifa" +
                "SXay/bD9iJ2aX9d2hj3TzrLu/bOze6v2o86e58bHvd4lbizw3S" +
                "x0s8/YK+znnL3SjaussRVn2634v1D5dW2vc+N6e4NdYf2njv2S" +
                "/Yq90d5kb7G32zs8Ev/dnl3txpq2/wLnR9VN");
            
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
            final int compressedBytes = 4079;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFW32MXUUVfygi1NIFyrallFJQoDYtFatAoZR57/YVP7rd0t" +
                "rtBy7Ih4WWhBCgiiaWnffe3e6+tyZgCprwYSHAf8KudAX/UGK6" +
                "NphojX+JRDGhGkKW0qb1IyIfzsy5555zZua+t2CM72a+fud3fn" +
                "Nmdu7cd+99q7apbaWS2qayslTClq03JiRCXGRTCmtYJ337GXo2" +
                "VCuJj7TzNjKbP5JYTJH4qNPYH1eWMcsx+fpSldiqX/Wbsh9LZ+" +
                "nHduOARIiLbNVfn0AUEalE/tk8vhyqefPY7/fBETePz0gspkh8" +
                "1Gn8Mq4sYyZ+TF+qElutNsdTLnelwbBl2tU7HUoIcS3TpcyLI0" +
                "LJzNwf0MNhTC1jZ5bM6tmRg/1nKqtjMTH2U8RHHTea1WKsjAsc" +
                "py6tq73emCpE4pgt1TJlC0tna2G78RtEOIcOskh7vrJaXDFbj6" +
                "9If27jflJfIs3fhX1KTfSk6M1ofu2PNfTg/FAxFm1mKauyKctY" +
                "OksZ29XXJUJcZFPiterZ5Ef+0K+1+WreLJb9PjjiNF6XWEyR+K" +
                "jj9yy5OHqI3R+pPFCV1BvPlkrpXSbdoTaY9gZr0W/jiBr/yMe2" +
                "AXO9AOrG+lwp+mnsQ35jLLSuekpwf2LSj7153ADe6Z3GNhrt4Z" +
                "/EwriwxXG/58YzwGiMx+ykQIheG+r7LE9lIyQzU+8hVv0jt6bv" +
                "29zM40byGizVJ4rU6JO+S/Whv5c6fNRG6T34EZ/R/L1kUeyhN3" +
                "1wNEV2wGsncN1sHjem75B+TKX2iVJp2KyQ4SdVn7H1OezjuXVa" +
                "rtiHuZnHvlgUtekipr6iWRp8NeJ7kvSV3sNPB/P4MrBqJ1Nv6O" +
                "N7s57/1D42UiAkm8e+0FqkUp00tvVuVKvy8+dtbgW7XgD19p9i" +
                "zvDJQw908pXeQ3uCeX9s6H7OsjVsVSdhFJHd4F88tqEHg3lc78" +
                "eu1mfzuJ7rF41w6FNDnxw6rzpenRw630V5Yh6fNq0nXG0cfc08" +
                "jk9hHhmnJnbE4QuY5fG4b6ceak9Klq1hqzo+ML8gJh3G5tT2Fs" +
                "cO88hRv2Qjc+d1ebB8R6lUHoSU7Wv5zgSYZegFZC/+FHM6e5tI" +
                "Btuf17VEsnjUMI52PbePQPZO8+h7SxU6O6pjErOp8U51st7vkD" +
                "HEzHrM6nKF07nlamOxM9T1f8Se19ZSnaw9bvOBdZJl9MfQ0+ZD" +
                "e4BNavXrzXnNWWMUYXVsYH783GvcR2w4r7Mo/PU4xufRnx/wZm" +
                "3Xa3IsOQbntTZ7NpzXFrO5WY8fTY41p9t67TBieoE+H+o2Bz7m" +
                "VNPLSMn79vEEWoz2m1BKlu7TmxHDXPLqPzOsaXo7IvqM5JieDR" +
                "Hoc/zI8uvMnzP2ZWS3hz+P+kt8HpGrs+85+nZAam9SH8kx1at6" +
                "s+t1b7679tq66k1mJ7NVb2OHrWNKZusFWEcP5CNiWa6H2YA0/0" +
                "ZxWU30Ub3NY1BS3zIG6Gn4aWRhHM3jksUj9FHet4+Dpj+Plknz" +
                "6MfmR5v1uE6ty9rrcovD1LpkabJUrXPzuA5TshS53BPbgCDLel" +
                "uk+T71Wv8rKtij+S6U1LeMAaNCFsbRfE+yeIQ+ymZnqY+DZmQe" +
                "l/JI4pFxu8HWqmwnVfmOCphaW381WQ4otB32FrXJE9vIrh9x0S" +
                "wnDotxOfqQV8giLWKjR6nU6pIsHqGP8p59PN43xhjT4Oq85+Ro" +
                "cjTbL/flKg4z+TJzHAWkug8xy8zqR5GPOdaSZVbNeiOHxbgM+0" +
                "AVn1XdZ3sgVWIhr9UtWbaGURGK3tQzsWmkYYTGvox5efOD3tjO" +
                "RnE4OWzKw1g6y+Hs6DbHYXteE8cidJCFa1iW0+kGhGwOYwrNY6" +
                "QkRhzoS6R5PNLnYd5CT1DLUrc/VqjX9vLRO7Sbq8oR+tGCRfWo" +
                "nmx99uQr1WE2rdquetz+2EMJudwT24AgXv8tcdj1ejv6qB6zP/" +
                "aQnzhbhBayMIZ8f+yREYWRyZ59HDTD/bF+kEcSj4zbVU9yPMmu" +
                "fdX86Q1gNq3akRx333uOV59DzDKhjp5Qkt3moEYcNpodaEmOt+" +
                "6GUrKqz9keSAvZxLPfezjL1jAqQtGbeubxk27w/VE8x/LnB72x" +
                "Db2qNWpNNq9r8hl2mFqTzEpmqTVuPa7BlMxCLvfENiDIst4WaX" +
                "2LRTULFezR+iaU1LeMAaNCFsbRuleyeIQ+yvv2cdCMXK9n8Uji" +
                "kXG7GfGRxFxbbQ6lUzmC7fS05IjbHwWHDrJIex6Pq7fqaHN3eV" +
                "Xyb2lSEuMI9CXSqoV9cgZ5ghqkynZ/rFA3+yMbPfFDxVi03FPe" +
                "Q+l7MsacZI6ZyzPIql+0SKfnPfozyRzLsVyB/xw1P9jznvCTzp" +
                "Qs+byn6DkF9lyd1MvbPqeYjMXY/nmPmqlmQg4lYtBOu9XMwe8C" +
                "ThzO5l7Ek+rk69bjalTgqt654/XBPdwd3tt+3Nwuo6NaZYc/Vq" +
                "jX9vLRy5GQvt8LxeosXcp8p7W56sotXdhOL1Zdbn/sIsyv2RK8" +
                "AJE1tJH+8LXka/ZHZqGS98HbyGjdK7Gwjp4UmX1OEdrtYeaRjZ" +
                "74vE/eH6kSW9+tTzRn3Mf0Sfrk/PybrmfoU7U9d17RZ9p51Gbf" +
                "1WfreXo+PKfQF+gLTe0ikxbWJ/Qi57XYpCVwXudKn9eXu/IqvV" +
                "Jfnc3j7bpqkNX6C/qaltZf1F+O7Ayb9Vd1v77B1L5m0o36Jn2r" +
                "vk2foLPneK2aPkVPM5YufZo+3T6n0N16tp6rz7LWgfn6XIOdZ9" +
                "KnnZq5y9OXmPW4TX/W1C7Vl9nzWl9p0gqtdKW2Vyd6lXxOoc3V" +
                "Q/foXr1Wu7tAfa1JX9Eb9Sa9VV8nYr3ZpFtiexLtNvWd4b5FT9" +
                "UA4+9nLFq7Ib7jocfwfZ33P3pmVuuP7ccjp8undqgPGOyP3nM8" +
                "kyq38Ui4qowI+XKc6Js/u/uGOLuvVFdCbkvCoJ1eBigixOVeyC" +
                "E1rkv+2Tw2pC9ZqOR98DYyRs6QWFhHT4rM7Kr/Du0yTopDjlTG" +
                "QKqcnbVWqpVUh2Tm0ZyVzemSgzlxWQRZq/qad91YSfrV16g+cl" +
                "HBG6eVXE32Ad/DSQNjoqgolzUbFUVCqmZ/XMn7QL4/TvANoxGR" +
                "r1ArqA7JzOMVYh5XgIXsVMZaUh1t5UepPrK4iM/VQlUzjyu4Ff" +
                "W5n2zblC7nkZCqmccVvA/k++ME36IxZrslv858x6RTba2yWnDm" +
                "6fvgOmPyCzNsoWAsKVBn15nyY7qqd3X4BrrZ5dlOq28M9sdL4D" +
                "rjrPl1xpRn0f4Yuc6Y0egBe51xKDuTi64z9H6GX2ei301fSl6C" +
                "HErEoF1Zjwjn0EEWaZfqZHN/m/XSn9u4n9SXCGgUx0SeFD2OJl" +
                "QmNexd9itH6EcLlti1k659aVle8fjVq7aHWvyaya+E0fdcD/Mr" +
                "OLznKr6nwZyulTaNXB28pWPRFb3nSlV4TzLV7w7s7udB760cvO" +
                "fan+w387kfSzfD+7FdWYMI59BBFmnP/1b7uWI2j3ulP7dxP6kv" +
                "kREV9ik10ZOix9GEytyD80PFWLRgYX/R8XA9Vtbm62GcfR8bl+" +
                "uO+Gz1jhetx8raTuvRvuEvXo9uZa3yWOMYIfwOIL4e89GMy/WY" +
                "JsW/A2CjzN//o4pYjxOJ+R5tcyjdDE9gu/wiIpxDB1mkPf9bTX" +
                "DFbD3+UPpzG/eT+hIZKYd9Sk30pOhxNKEy9+D8UDEWLVjYehwN" +
                "12P1UL4eRrPVc8gy4+uR1pDxG2WWQ2J/fIStsEPR9Thqe2i3P2" +
                "ZREGsUI7T1ovWYj2ZUrkcZH58JMcrRPLpJ0Yb1eCA5YObzAJZu" +
                "hg9gu/wLRBBNb0IrefgahnVr9rdySLoNbe5+5iHyT28mJfE3Dv" +
                "Qlkn497JMzyJOix9GEyumNfPTEDxVj0WaWg8lBf1cAzKb09uSg" +
                "e69wsDqOmGVCiZ7IRy2bgxqps1/r7EVLcnDkWvKS+yPXQjbF5d" +
                "4rMJat2VQvQ50iE9frHTJ+1G33OynSoP0RVciOHLXJHvn3+U2Q" +
                "zF/wV3g/AxjPkQvPKaAtNdgdAtMf/inZRqK/6EQu9RG5n9nEWd" +
                "ii+HifaLGjCdXd/cwm3gcp8RjoiI5wkVoEuVrEMWhX3wAUEeJy" +
                "L+SQGtcl/+zv+Yb0JQuVvA/eBsbuWVYjFpMfFY/M+HWHdhknxS" +
                "FHKmMgVc529a32yHW2QjJnwt0S4znhVEoNFhnT392ltnb4XfNW" +
                "qRby03tIEWPCqAiXDNPzjJg6x+T45TjpiI5woVoIuVrIMWinuw" +
                "BFhLjcCzmkxnXJP/sWNy59yUIl74O3gVEZH/m2xMI6elJkYc8y" +
                "Cj4nsl8ZEyGS7epb7JHrbIFk5rEuMZ4jnu2PWyiXNV+/8ry0Rd" +
                "bjFqnm8yvPc0WMCaMiXDLAL1TnmDtfziQlHgMd8agctsQe1IJU" +
                "Kq26RGI8J5xKqRHXr7yglnSYxyVSzedXXuCKGBNGRbhkgF+ozj" +
                "E5fjlOOuJROWyxPagFyfR8qcR4TjiVUiOu3zxRLe4wj4ulWoxP" +
                "ihgTRkW4ZOBofHWOyfHLcdLRJqrr7EEtSOa83i0xnhNOpdSI66" +
                "vPSVs8mlC7KGKMCaMiXDLMaAZj6hyT45fjpKM4Kvn8iJ44VK6Q" +
                "WOw9l/9mKnpHxvSTfZ2fVPn3ncGzjivkUzpxzzhZ+J7ripj6VN" +
                "9z+c9KYqNQm+1BLUim58slxnPCqZQacf3mHGmLRxNqi+89Q6SI" +
                "MWFUhEsGjsZX55gcvxwnHUVRGexie1ALkul5hcR4jnh2vb6Ycl" +
                "nz9ZtXSVs8Gq4R45MixoRRES4ZOBpfnWNy/HKcdBRH5f0OIH8/" +
                "k+b/IavnuPczDxS9n9Hfk+9n9LKi9zPNlfZ3AG3fzvR1ej9jsF" +
                "P0NH1/7P2MPqfo/UzadK0P+X5Gb5DvZ/T18Xf78m0AtNP7/T0o" +
                "GfCeYE3G/n8mGXA74QB7kpbVkgGLdnqv4Pfh701+3NIOz83CyP" +
                "yepT+yLQuZcjcNowj3Z67E5nFPbC/vPI/hFYN6333u/2sed89v" +
                "P4/x68zU5lHNVXPNuT4XS3fmz8V2+pBEiEu1+gTWKZdt0nfn9d" +
                "WyR7JQyfvgbWSk35dYWOfKqJM+6I+Vc3n/sl8ZAVcltpqn5ply" +
                "HpbOMg/b1b9IRM2rv0VW7kWIzetHbC3ZCW1iuvNmp/QlC5W8D9" +
                "5Gho+Fda6MOn7Pkov9WxZn+hFwVa4ePkOvXZN/v/hB8AvLXaUp" +
                "fIAV4ya7pqbw33wKf0c65djbMevR//KUv9vTD8vf7ZV36jMdgr" +
                "/beyT83Z5JH+h3e80b6Hd7pvxQv9uD6/UH/d2eGU30d3vtr9dT" +
                "+92emqFmmHU5A8pspc7AthoDFBHici/kACJr3D+bx1uYyhi3UM" +
                "n74O0wRhmTHxWPDEcTKlOcFIccqeyPVDm73X1h+a6p3hf6reL7" +
                "wuY2sqWPfrj7Qv/XG/H7QsnA0fyP7gv/A/EwHas=");
            
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
            final int compressedBytes = 3425;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W2usFVcVPrZwoZQC7a30UvoC0gdKBTR6bVr1nDPn+EBtLw" +
                "iK2lpra6xafEuMgcCe03sOvYRb0hLhR3moafyDora0QFtExGdi" +
                "m9g/pIoWEu6P3kQJUX67916zZq21HzPnXB5zsl9rfWutb++Z2T" +
                "Ozz0x1aXVppVJdWrUlbNAy7eHdIEUJYbkVYkAia9wevI+skbak" +
                "oZLH4G2fo+TksuLMdG92+XrJk3jInsp45JX1e3F1sS4XQ5lpFm" +
                "N7+CcgRUl1cfID0lIJGJCYHFCQkz14N1JuSxoqeQze9jlyLz4r" +
                "zsyPLFmgZ4PiSHcESEKssa4mqz41FT2p9TpdZWrDv8xlAzrdoH" +
                "bp/BY1X+e3ZfI7KmxTd+a191Qqrdfy1gfUB9WHMp4rVLNSuKlP" +
                "q9W2fDBrPxTAXKGmqa22drVO16i3q+t0OUenGzfeZOXz1G610N" +
                "Z0L9VSE9m23qcGbXk385aoBtY7C9Sy1qvqE1r6SZ3uzRDL1afU" +
                "SrVKfUZ9LpN8Icy+uT+vjWNt+FdS2xxPfsiREU8aZTAGK/3psT" +
                "2N0gL7/RiBW7rsCAWMTGr9yJQwjv6W8ynkb7iHOOac9hd7ae7L" +
                "Pe3Kx3G/1Ca7kvUcGWGiUQZjsNKfkaG0YBz3YQRu6bIjFDDCVn" +
                "NfyApiuz0No7SH9fHxcUvvTMnP6+SZfBwPOTGeSdZp5PyScbQo" +
                "Xa5z/RkZSgvP7NW+pctOTWP4a3S6zufvWGaR4byOsl+nlhVxVF" +
                "8PWm1JtkAOJcqg3bo32dK5w0ha92XSDaglC9eHQVk/G7gmq2/g" +
                "Hngsl5WPoYidhYGYAo/MwFuWNrh9hXq6R/R+A/SSc/P7wbmipT" +
                "e2O/Pj8XBMo/vy/vD+Sh/kKNdKnw+vV7rekp3pA3F2/jb8myJE" +
                "L5HjW+euINOnkqcghxJl0B4+ghKOoR9ppD7kHeWdO6U9aSQrH+" +
                "NyjHMiZsReR17k9tW34HjfY4htphlNRnU5iqXVjGJ7+PfJaGcx" +
                "yEHWHEMtWbg+DMru/zGugXpzjHvgsQS30RCGInaW+DElHpmBN0" +
                "gmsuwr1PV5zXpvUBzp9tBlS5awNebmmh35efIHp387ujobdxS1" +
                "Gyd9C4pdHq+Yg/EUQ4Qi977FuOb66wP3j38N37uV3T8WtRunim" +
                "Kfdy+1p9j9YyjyxCIE9u7WZCvkUKIM2sN/QwnH0I80Uh/yjvLO" +
                "N6U9aSQrH+NyjHMiZsReR/6G21ffguN9jyG23NKO85zA8Xjiwh" +
                "+PnZHAPp7TnadyDsZT7HjsPHFBjsc5YabIqzEgZSYNnwQttvNn" +
                "snHxhCYwvKfknfF4AzVg0RzfOESxXUvM8Rdjj9GNp403SWYU2Z" +
                "WHfJbtt8aAlEDkRn+jX+v6sbTIfmwPn0IJx9CPNFKfPUu/hnXU" +
                "2aNii7Tnupyr519KQjGlT7Qk9jp91e2rbwH41quSmxtFcjWp2l" +
                "ftq1RMbspsJagP28NjIEUJYbkVYkAia9w+W3/8lrQlDZU8Bm/7" +
                "HCUnlxVnpvfgk75e8iQesqcyHnnl6MC1J3+qbn0/pim87zlUJO" +
                "9sq1zkLRaf5sfu+lG8tdaW8jiQX2fGY5ru7EPyzq6LPo6R+J2d" +
                "vfXjvHm8mI/j2ZimO/uQvLHm0vF3Zt01vfXjvHm8hLXaba6m3d" +
                "eLPWztaVw+8m2mmToxhu0reonv90Yi2pMvzjiy+5bj3n3G8Qnc" +
                "Px7n8pF1vqZnhoV2sftHsnLuXI5fgGNvb7IXcihRBu32dJRwDP" +
                "1II/XSO/eox/HH0p40kpX0LyWhmNInWhJ73Zsr3b76Fhzvewyx" +
                "5ZZOLw7mZ8KNMU3hvjlYJB/52UWflyLxqTfd9eO8efwinzlmxT" +
                "Td2YfkjccuHX/nOvNYb/04bx4v5+PYH9N0Zx+Sd56/dPyd+57n" +
                "eutHTzFPJCcghxJl0G7PQwnH0I80Ui+9k85s9dXSnuu4nfQvJa" +
                "GY0idaEnuILPvqW3C87zHEFjTmOT3JnthNmegfrglAvd1IKrii" +
                "YGS0YgApCaxTJBCBrTZgjfundQrQgDZh6xIVFhnjV3KEzwlimH" +
                "WKJGdHzNx+yNWSfFwqpKVxkTySCkcbjfqumlQ/U6mY9ylMWT9T" +
                "P6OmqxnqKtVv69camZqtEXPVDep3un6Lmq9uNe9TqNvN+xRa8g" +
                "77f+QifJ9CLTGerN17lf03jN6nsD6b6qj6sPqo+ojWfEx9PDtO" +
                "s+iWzWp1v3rAvE+hvmjep1BfUl9Rj6q3qcsABe9T6HymmqWu1h" +
                "7t+xTqejUHxlHdrGXztH6hxWbvU2jZu+F9CnWXrt+t6/eoqqqn" +
                "e/j7FFqzTKf8fQo1ZH0s1yl7n0J9HvqXoR/Wmkeqs6uzk0Umhz" +
                "JZhC2s6zHPJYQ1cpJAnXJAQW6f5WdjDX1KH6CBiDya0SMGoxmk" +
                "K5MWsk/oAyL4rDlv4sHjcv/SK/M5UB2obzc5lPXt2MK6HvNcQl" +
                "gjJwnUKQcU5HYcB7CGPqUP0EBEHs3oEYPRDNKVSQvZJ/QBEXzW" +
                "nDfx4HG5f+mVfPJ5xo5vwmenJKGczyVifkzK13H5eqnvDebHfM" +
                "ZJiuZHjsjnusSfH10+oX5E5seEW5P/2GoyX9+2d6kdzKFm73t2" +
                "6bSbtIAnfXyLY8qta50yTJpIFGdd68SeCwnRa3TTb9daosx1Jh" +
                "mE9/aSQb03BnUru87Y+rVGll9n/tj6t3ud0VrvOtP6j92zg/51" +
                "xkqbWuJdZzC6KePXGUDJ64z26F1ntMxeZywW39sbpOuMrufXGf" +
                "nentYs04ldZ3TrT/I6kwwyNFxnZlVn1c+Z3JT6fD+HLdM29fZD" +
                "JCFsVT/pkMTOgZnE5ICCHHRYA58k4RqIyKORV4pvkK5MWhArwK" +
                "MfHtlnkV+Bz0kt+nBZEGuse0fxkfxJylmBUn+Wc0E3/x+6mNpP" +
                "L96TDM6PkefCl3r1p+7rFll9q/oW5FCiDNptJSWERTQlv4Z18m" +
                "/XKY763nxOMgaXuBiJ4H2h6JDaG8OeJWfZJ9e/9MrR/rVWfS+f" +
                "z1f4+9u9XrWO9baPa2+W/yPd63Eur8/R68yb3Xkv1hdpq/+FBH" +
                "n62fxMeCWEA+REt/rloehlEt8DxyB/6kPwvH6lG+/p/cX6sLY2" +
                "5h+PICNt8fEYw0ZHYZK/h0Nxyjxwpl0ej2PdeK+Nncfx+D9IkK" +
                "cP53vwcAgHyIluI6dD0cskYc4uf+pD8Hg83I339JFifVibXmn3" +
                "wQxIkKdTcu2jzr6yOEB6nqZ3OT96b33VZqR9fpxSP4bpVM6L96" +
                "EocrH39GvF+jJutU2YQ80+zzyt0zbSZs8zm7ro5abuNd6d0aay" +
                "CI8vkCjOurYpel5vKuNWFN21DqFqMyFBnn4nu1efmehW+3EXB8" +
                "gJrxzPDEUvk4Q5u/ypD0WRi72na4v1MW1qnp8mpzPhuw/1Rnqz" +
                "ue9J7VcUqX1nMs2/2VJ/1+kf8ruPdCDK6J2eZK7m4b0Vq/7lSV" +
                "aXPF/3h7/7UP+02lsjI5BFdr/7SJ038dSy9PaCe3Pvu4/0Xfnc" +
                "eRYS5Hg86mPxBZ3YNw2Aq54tO1bSgm+26n/xZu6z5RLnn5bXJQ" +
                "b5Ux+KIrv6dI9orSuOHtbylS0ug3b7AF95Cq+btY6570gWr5tt" +
                "vqx43axSKVs3qzj/K1Qq3a2b1Z7vZt0MUbI3ZetmyenkdPVZk0" +
                "OpxztrmXb7oMlBThj6GSlIUA9l9Vm75zLN5stRZ2TcA1pBXNCa" +
                "kuup5XKMc0IcMEE/tZeljbQHDKC41u0h9Q/7aDRm3cye9fx712" +
                "zdTB+PR8y6mZbgutlJ+N6Vr5vpJNbN1Cm1JPcUWDfbPCm8bibn" +
                "x9i6WY5g62b4vStfNzPfu5p1M4vN1s1qL9C6GXzvGlo306hlWs" +
                "LWzawPsW4muNp1M/cMlE837d/6cwCdFYE3ZcZjzwDsvJ5R/vQc" +
                "em86PB+55yc9zwTO65/LuSbkzaDkee3OInH2zRWU2xHO1ynaR0" +
                "OY5orydYrmiuD1R+lxnFc6iivC1szPsESZGrbi1rVfF3EjVFhP" +
                "/mNe3P2T5l8n1deGMBNZdaRt84Jy2+4ihM4gU8buw7E3xd7TJ8" +
                "N6/x19oR1qDkEOJcqg3T4uJYRFNCW/hnXyb8dxoe/NYTzkxuAS" +
                "FyMRvC8UHVLtubBnydmgZFwZjXvl3nW+inK7R/LvENOnc96rQs" +
                "iCoyWK2XxPuW13EQhlatiKW9deLOMGqLCe/Me8NFdS7pzX+tm6" +
                "fc7FEDJ6r70tjqmXvmffXFkeQaJMDVtxa4xc7D3dHtaT/5iX+m" +
                "6Ybeq7TTI5nP2mjjnOSEYrZyPU8jkDZKgBG9Chf46jutECm9hM" +
                "hCh+vSb/dL3GXqA36A/Nq5IFn2thFPj1mnxgHOLNxyY8g5vUmV" +
                "P+PZf7PFP2PdfmIX4nFH6emfj3XPQ8418Lanu7+Z6rtre3fz2y" +
                "77mmNKZUKiaH0q53TsF2MoISjqGfabWOuT4a+fol1lFnx3G5tO" +
                "c6bkeM3IjkOcaJLIm9HqEDbl99C0JJFjKK5Go1MxreuiTITOos" +
                "BS22EUtS2ZJy9OQcj19GDViEMGSJOf7CKJedZJYfaYdcechn7Z" +
                "Abw+fmtnT6Px9HcJ0=");
            
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
            final int compressedBytes = 2873;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFW32sHUUVvxQqpFUg1tJa9DVpQrC8QohgxNLivd239OMlWA" +
                "O+YtD2WYy1kSZGiYl/qN1d6i6+V5Nq4hcmmvifJeEj+EE0ktQ2" +
                "z/Y9JSqKtlhMsAFTUz7UgpqAszt79pwzc2Zme+99sJuZnTnnd3" +
                "7nzLmze3f23k0+m1zQMbbk7uRiVS/pdPLrkrdVkstUuVyVl2rE" +
                "FVV9ZXy6Ol5V1WtUubpqXdswvSe5QWNwm9pVaW5ONiUb1XFzMt" +
                "4RtuQjyUer42RV70x2JZ9MzksWGKhLkkvr1lJVVmhfe0eSlbV0" +
                "NUX3HkjeXcvfVx/XJd1kA+fsPWD42GrFdgfr3anKx5lkD+QRJP" +
                "n1nCFbk67tDLClN6o8frMz8JZe79PuHZHlvR+14W6H8m/JfTyP" +
                "Y9caeRxPbxk4j98bQh7H+8rjPa3yeE9nSFv6oaY1YeTxbhudHT" +
                "4H5m0qj9/vzPOW3ubI0KOt8iigkl+1nIcLVbmoah1Xs/CmOkMn" +
                "I+MsTk5kZ5KnklUhvuz5so7Ea4AtTZ62JLf7+csIk0UE/1ZVlq" +
                "lyErQ+z8l7A9nYEnmuXsmnAp/DV3p7ylqX+vr4NGqreg+2Q2zn" +
                "riGRBDBpxFE0aj0On2c/u8u7aR0eR/qdpnWfOR8HPK+/q87r+8" +
                "PzccDz+tuODD3U6rx+aDDv3dt0UXF8ponoh1TLa+9IDrox+R/C" +
                "kYQ9cBTG7rPOn6BjkTOgYr+f89r8fpby3qv7Qfp9Ha2K1JUwPw" +
                "DaUl9KzDtC4Uq0qsREqyRNFLy6xqfDHjiqbEEvPq1HIflGtJjH" +
                "yi59OD4NSGAqj8jvZyGz6cfNHD9k53rQbeqx+f6+dt73HAp9Mt" +
                "XofzKsOMh9+L/mIY+H5PjnP4/pT1tdXR8d4Nr4T110nf68yeN/" +
                "JJxG9p3HX0reQxI5ZjN+HIM4Hw+2Ye8d9Ovbjz5tzrz8v4aPzf" +
                "6+GJeB2fBECNH/pplcfLZncfSHhnbf8NUmj//rdL68n8S5yYh7" +
                "U4uxGZgNT4YQ4S3/vc+Xi8/2XI+XrbDSw/3mLaueHOj1jHF9fL" +
                "WRLa+Q30p/q9qrwvePyXUqolEr4t+p8/rPwevzRGg9k/66XM8k" +
                "z5nrmar9zvQ3juwfa7OeyWeTLemVQlS38vVMsr3990xhaPb9rM" +
                "xj63kt5/H4EM4Y7z2oK4+9BYH5tBlQUh5bX1vuhVq3qjwuMLQx" +
                "tv3ftG4MahpJbCJsjLAuJCiIuvSl5LE/Jj+7y7tpHYqRXh+L8w" +
                "2ucX9f9G9g8j+FEAPMhXEfX/5kq7l+YmjfM826sDCelfe2+PsN" +
                "w1/dmA0LrdFvObf4bAbO5OLrvtbqHvC1vj7BU3xdVZ0Zp7iW3z" +
                "G7Vm0m1nn/+KJ9Hy75Ca8L0SfED78r+MYaYu+d8uvbrxrSZ5rW" +
                "qX45PHk8+0atZ3rtZlbffuOPYW3k8Vl1VTluYhAZ4hTPyj+GbU" +
                "Me8hMcVbag57YGz6ae3z+6Ykd+FyrejjW/79n3CwkTb2+RRycm" +
                "fyps284DospWWfY95rPO7w3FVmX177IepOaRIG7FumJ6ofmeuV" +
                "DCINIzSicmPxm2becBUWULem7r/C+h2KrRvyTrkd/P0puCWreq" +
                "PF5kaMfSs1TvuX90YlDTSMZMhI0xxvoyR0HU1XfPlMlnevazu7" +
                "yb1i6W+MEmyubpRLGIa3sbOVL3/WwmZuoVK8KNpi1ae+YjQZWt" +
                "spRM8YOuqNJXzNhE1KuyHqTmkSDuxLpaH53X5HGxhEGkZ5ROzP" +
                "QnwrYhD9n5HFW2oOe2zoKxVagLZD3yt2Ex1tfLrU9r4PX19K7B" +
                "72yyC/tZX+d/ax15X+vreAfWVav5Ra9YIWEQGeKUtrHbw7btPJ" +
                "CId6BHtzV4DrHLeuRvx5JdiuvCbHGLuXGOz+umP92Z5811H54t" +
                "6rwOW9zMk2xJMx8vt7W8HWazzq7nwrYhD9llHFW2oOe2zp9tE3" +
                "+2XNYjf9ss4PUxe8fwV3D5P96odWF+uvO6buR5z7uGwVewfyGO" +
                "RUP4nlnZTx6H4TmwNn+TLrrOmt8NijUSTiP7vT6O7ZC8hyRyzG" +
                "b8OAaf5xC7X+/T9hbqouusmYXFTRKut3Cg8/p5yXtIIsdsxo9j" +
                "ED2facOejfr1srZ3V+8uXesjyHS/6HEJYgGNxW5BG/mrWTFps9" +
                "kxcR9UYmI4go4FvetieuZYiJmPyeTnrIiOXoys56paVpZiTGuh" +
                "D1jdpj2qR0aJPX8BNMDijgEZZRyPAQuPDL+vTbns2/Thl9Qez0" +
                "Rqtpe1PlaaM9AvbgYJxeCOGq5vfJyhjPVoznJ7qqN2nJ9LJJ+c" +
                "EywxeuX53+ZYbQuKtxmlaNEynsSaP8fNrmnuTSYlpPt7hmPY1X" +
                "5B8P5x0m0to8oW9NzW4NnPnr8s65HfxRLvxNp43rNZwiDSM0on" +
                "ZvrzYdt2HhBVtqDntm6e9+wMPO/Z6RuRedRbd213ra67a6lM94" +
                "txLQUJYqkVYJCN8qJ9PSs63BY1eKQ+aN+OkcdkRkUjsz3zKGhO" +
                "uF/TH7JSdN1b312PbV1UHj/Hcr5ea1CPR6nHLUFXXEx9ufCUTU" +
                "IjB8SEUWHNW/kIjYSy8ogAb45T24Zijz8sXB9vsLW8HWazVjeX" +
                "hG3beUBU2YKe2zpf0y5+WY/8Eio6Fh3TtT6CTPeLNDpWZFqOGN" +
                "zLXnbY5IiOcXbUVef1W7g91VE7jMj0KMVtcoKlxupSeuZjtS0o" +
                "3maUoq01s9GsOs7CsdLMQj/eH83G+7UcMbijhusbH7OUsc7jm7" +
                "k91VE7zs8lZVy+mNBSs+lSeuZjtS0o3maUoq01c9GcOs7BsdLM" +
                "QT8+EM3FB7QcMbijhusbH3OUsc7jYm5PddSO83OJ5JNzgqXG6l" +
                "J65mO1LSjeZpSi1Rr+L5PiFv0cC/7jUXwA/63C3wmgz7sQz+Xy" +
                "f1iKt4MG/uWyd6twNVLyYisywm6jMCJo7x2R3ynIR+x3DsLvRt" +
                "h6LtGek+a94XgCnz/Ce8O93fDecDxRvTc8pZHw3nCNvyqZNt8b" +
                "1mzle8PW/eMXKo3nveF4QnkIvjdcoer3hlVrqSorGrn83vBu/d" +
                "6w0nveG6aZqDBbuVRZ30H7+r3h6Gh0VM3Lo3CsZupR6BcrQUIx" +
                "uKOG65s5f5Qy1nn8IrenOmrH+blE8sk5wRKjV6MZMcdqW1C8zS" +
                "hFW2uOREfU8QgcK80R6BdTIKEY3FHD9Y2PI5SxzuOXuD3VUTvO" +
                "zyWST84Jlhi9Gs3V5lhtC4W6xvTLvZnR1pqZaEYdZ+BYaWagX/" +
                "wAJBSDe9lT9z0zpr7xMUMZ6zym3J7qqB1GZHpEZldMaInRq/N6" +
                "hTlW2wJRPAruhcdaaR6PHreeC1WyshSPaC30AYvS8jkF4IELGS" +
                "X26Qw03EqKAT3BTtkpP4+Ox9lcH5eZcsl7b5kZiR2b2aOSeJuw" +
                "nllna7Gd3eh53rPN+Zwi+L59vM1tLaPKFvTc1r0lodg0StYjfx" +
                "sWI4/vn4ffr782+O9J+zZ6ta7/2S9t9VvV0n4i6q7urtZ1dzWV" +
                "6X7xjJaCBLHYyg4DBtkoL9rXefwG9Ug1eKQ+aN+OkcfEmYERWr" +
                "3ltp7HWeVxuenX9IeslL1qj5Z7E+OoLorzCi6jNcj1fNR9zkE+" +
                "LcI//fXuaOCzHeVsEh4ZISaICuUcAaMx2amMj5+PE3dPVGd10T" +
                "U5r2MJ1z073F99bb42HigG4scxiE+a1rVj9+tlLa7O+Gqulu+i" +
                "60KKpStBvnaU1oUmf2hdaPqw14U8bq7X60I7Mj0am5mubM1VMI" +
                "7AtTqllt2Rcm8yPqKL0u/mMlqjHI+cg3yGFr/3Ex/hbBIeGSEm" +
                "iArlHAGjMdmpjI+fjxN3Mar/A2k0t7U=");
            
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
            final int compressedBytes = 3383;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFW2+sFUcVv89U4r/yyp/ypBRoSShg9YkY9RLE7r27lyeFNh" +
                "D/lAjY0uIDa0zUEosmGuauwr01ldaWJmiaUOMfKEGUWP1u/OCn" +
                "Ev3eS58Kec9HMBgSP5k4M2fOnnNmZvc+eO+W3czMmXN+8zvn7N" +
                "3ZO7t7r3pG3VGrqXereeo9NbepD6j56k61qFbrJGqx1SzRZZm6" +
                "N39Yt/epVWq1ekBLa3RZq8uHLObDunzESusLpk+oum03q8+ohw" +
                "ptS5ct6rNqTLdb1bZasKldao/aqx7X0hO67FNPqgPqoBpS7yoQ" +
                "71Xv0/Wwukst0O1CdbcaUfeopcZ2ZIVaqXX367LOYj+qy8d0Ng" +
                "+pDVr6pPqU2qjbTbp8WiWqqdtUZbVavtVxmyy36/KILo+qHVa3" +
                "U5fPqy+ox9SX1G4R61O67Ac5uc/saDES9Dr/lDpek55ayUEb52" +
                "/+VNrCDbG+jzgjxoRRkV4ijOcYO9fJ/GWetEczXJYsgxpa1EG/" +
                "+/5k2dHnSUNYfxRpwj4h7XE8KceShVrug/cRcfR/UhfKnBl5fM" +
                "8ot0/x7Akf45eshE6WJ8t1uxxba1mO/e4CqSGsP4o0YZ+QZst+" +
                "JMeShVrug/cR4etCmTMjj+9ZYrl/6Vd646wcHW7tf6CUP1Kb8y" +
                "07VhvwdmTFO+85WZ+shzpZz3XQ764BLWoIy0chhtg4L4132Twn" +
                "x5KFWu6D98MYZUx+VDyy0LOMgh8T6df3R6wcTVvjG8H5+Dl51B" +
                "Exq/OxM+jzsSzKQXpOX0pfghpa1Jk973UfRA3H0E4WaY+xoz7r" +
                "yvFkkVGFGD/G8pgoMooePMtcwxEcHzLGonWW4+lx3R7H1lqOY7" +
                "/7cdRwDO1kkfbChzfC6Fpn5HiysNiOxzB+jOUxUWQUPXiWuYYj" +
                "OD5kjEXLR3pZnESpu7bMUnmOn6zqd2b83dVdF/PXP4YyRGf7AO" +
                "f1i+mLUEOLOuh366jhGNrJIu0xdtS3zsrxZJFRhRg/xvKYKDKK" +
                "HjzLXMMRHB8yxqLlI+XWmkapOb/MUrX5qJmNGvy6x89mTs/HV9" +
                "JXoIYWddDv7EINx9BOFmmPsZNejucWPi7E+DGWx0SRUfR69fFb" +
                "P9dwBMeHjLFoaWTzuvu8rpvSvK4OYd9ofvA02vX9+BmS7QpjC4" +
                "1md+7rQQd1Y4s6y86J6yFeei+zc1Tny76O4pFRcbYyuX0q9BDn" +
                "DzUopSf0PmRraIeKnpM1BuoTAmt3qx0qJGqHLLewOJnzM5uxOO" +
                "sQHyHHY4ygq4jpRBGzxbri5SFjBoyLekj4DfKA/BCNI/UnuBAK" +
                "1Gwd/kVvhWtxgJzDdfPC/hq55Y9JDMZPOUS/r/fNjL3aPvPs2X" +
                "Hc3R+d//nmjtrRR2d/5H84VmltlRzHJwf4PfNy+nLytqmhTd7G" +
                "Hsr67tHWhKHdaEGDdmS0d57CAjLn5zZjAatpfR+0Q4ygK48JdB" +
                "CdqaH4eciYAQMobvUzpPwQjSODM+yJQto3959c68TtWvcM0nNr" +
                "ujWdXLD3vYtNm1xILhidsYBsdNAHHa0IoQYr2kECFDKJ50sXkA" +
                "G9tKaP7MgWGwtYyc494Q4oHj300bthOrKC4iQsRcrziEXIPfgc" +
                "xgPX4LHJlmRLkp612zbpJT2jg76RjQ76iAEZa7CinZjAAhoWZQ" +
                "8Z0AtK6J3s3BPugCpyKkZShKDlOZFvjIbssQi5B5+DsxNntkQ9" +
                "o+5IJvR3y7DWTZiSTKhDan57RC2ysjlTJuA9ly7fNhjdrrb1Gs" +
                "s0Eb7nApS2uPdcLMoJxxC850Lvzr5H7bWtvcbQey5AHb3iVqrD" +
                "6i4Yqe7W5R73PblarXQs66BFD+Y9l9VvdHb3nktGCKVYDe/gPa" +
                "vZTRrNad9zZauyVc0bpoa2eQN7KOu1pq0JQ7vRggbt0DZv2DWq" +
                "s5DN6DgDWsALWE3L7dSjGEFXHhPoIDrkCPOQMQMGUNzqZ0j5Id" +
                "pY/OtD2sKrjilpi2q6/snrI1jl9VFev7wVQiu8PsbtsesjIQgF" +
                "MZNfc32M3dljpDyPMMK05X+DhBq/p6+PCzO9rjQ1tOk27Jl+ui" +
                "2zq06JoR1G+BxZsVJFGW02zm1yPLfZNcK/uDbuEZnLYqKRFD1m" +
                "EzLzEYQi1nzK9yKul8VIe00ZZpkc0P0RKx2c+xVC58DsOfD6WH" +
                "IfsbrE8/gA1+Hn0nPJJVNDm1zCHsr6WmprwtButKBBOzLaa7Cz" +
                "oA24OIO0gNW03E49ihF05TGBDqJDjjAPGTNgAMWtfoaUH6JxZO" +
                "Owu3M8bErjcHoe+2BBu8afJ7m43ww0iPIthhc5I/etznuZnaOq" +
                "IwA/oa06j3IPVTlXRcvur78+gHn91dt1P9M5OMB53dN73dbQ1o" +
                "uekzUGaqvLrxXWntPWC6lo839bbmchG2k52kl1a6vbltmpRzGC" +
                "rrDUg5h6Rcx15JB5cJnYivjqwm9dxgqslA2ObF52a6HLUOP52L" +
                "ycf0ufk9fRrs/iAyRDn0aL55yXqW4ckBaO7zxNCKM31hifz5z/" +
                "J9QRE4+Ks5XJ3nPcy35OYUSkQSkZ0fuUraGdKnpO1hjSEHbEjR" +
                "1xmBFRTxG28ALSFOF567xMCf/WXvhx3gzS18kRMifkAA9h1Dxu" +
                "Fgf3OyW8MVbGuTRZmm42NbTpZuyhrM/aQmN6vgQY0pgapPwi9B" +
                "HpZsJmGsst6N33Rh7Bm0H6ujA+ygk5pOcwCsAAKn+T++X8kpU4" +
                "+3zP+N+M26v70e8/D5P9vh9iFk/Wt1fxhZ7n8HdSV5OrUEObNr" +
                "CXXM17acNoTY07WY0EI1CLGpRQJn77CUbYxDefZyePiOQxmj3O" +
                "SHjkKWOWMWM+5IOPk6zEnlxLrjWeNTW0jWexh7L+fAsNYY0eCm" +
                "C4BnWAtb6uocT5EY0WsPp2xKB/QEpdyEg5IUeYB3knX4DiVskv" +
                "WQFtkdOJeRI6jW06hj3TT8dMnf8JNYQFCUagFjUooUz89vMeC9" +
                "nE+ejZySMieYxmjzMSHnnKmGXMmA/54OMkK2ev1VrjVNur4nPF" +
                "L2w2Fk81xmPIiufss7iXbY3PzAOhjIS98tF5Zyax5c/H7cRfzp" +
                "L8FwrUefF2Nn8hhgPkrb4vzM7EvPfTxGP246ccqjz3Y6+2x63p" +
                "Fb2P2hra0aLnZI2B+orA2t1qRwuJ2lHL7SxkI61AgzRqbaO2ZX" +
                "bqUYygq4jpShHzKHKEeciYAePiGxV+R2WsmB+icWSxavimXPfk" +
                "vdz7fSciZrNlvxn0/XVZlIP2nLylZ/v+1v7kLVNMbfpGbyxgt1" +
                "cEh0E76NAKPWKkcTASbMgfj8JYIRrOxvGAAkaKAPlpHGaBbKDD" +
                "WNDO4yMPwMjHITv6ocx49smke/86aeRkEp/RGxntxTvaSXrr4N" +
                "5KToZP3OU4/vwe+SPH0XmHaOTTfx/F31hgTBSVed6DWSAb5MPe" +
                "1Yoo+NsFY6FjgFh8B4KjeTziHZj7X1y6Vcvf0+VO7DHMvfkh+F" +
                "+crh9wurUcA+8Lw43/Ly7dqlqVT6BMBLvsqMfd6H0RhP1fnLUu" +
                "yP8O/4vT8lJ6bhb+Lw4iNf+Ls+0mFp/9X1wRn/hfnNX8hf8vLv" +
                "ptvrO1M91gamjTDdhDWfMUGsIaPRTAcA3qAIteQJJs3JJuMIX7" +
                "54zkH5BSFzJSTsgR5kHeyReguFXyS1ZAo9x086Q5aUqzOKeNbH" +
                "//yOZ1M5jXzci8Bl0zMq+RP/JEbBKszcp5DajOV/i8bkbmNWbR" +
                "ZPO6yeY1j6J9is9rOAp8XhMH+qHMRPbHYF63jqmftY7p2s1rI2" +
                "Pt5vU2bfXmdetYOK9BRxaa15Y/Oq/Rl42mdF4jis9r3QvmtdbZ" +
                "eW3Z3LyGeGBea/sm4uTzWvce1oXNa8sh5jVlhlI2L5sHNbSog3" +
                "7na6jhGNrJIu3FSmMeZ+Ran81boQT8UhPzKTlxJEVfqx094uca" +
                "juD4kDEWrbMMZ8PBOsvqsuG8190EVuhDwX6sJ/XI5LH/ES0wIo" +
                "ahkVjjHkf50cnIyLOvL/MufVRr0HP7fvn+ur0yfxXfX3ftb1/a" +
                "ayvfFn+w1PJgoFmmfZ6f/Yq3vehW3l+XeW4HT2Hba24qmuLupr" +
                "EACtT4O3t9Pu7x7hQsDpC3fD/zu+D+Y0F/jdy6eyUG46ccqjz3" +
                "Y6+2zzz79k+KZxCvzf3dU3Z29hy39rvmufBcuuq9lF7iLcpm1+" +
                "fjPrSilqyoheJLITtqsz8gA2cNo+I++AjO7MfEcwkjM575GDme" +
                "e+fFPwJ+vOQn955k4bzW8+ep4Fz9601cN+z1Mf95cXafbv9NZ3" +
                "Nu9p9+/otKv2+WnI8z9jyz62P+y/xX3trtotmpB0Ufx4NSx2sc" +
                "R3asQ4nzZ78mW8kT2ouSLYYnRs5P8YUI4znGLvPgeMoT2Xge/b" +
                "Lgz3G7wT/C81k/e8pev23Xx9dr78DW+DHWINnj+B3POk4y9cvZ" +
                "YhiylCNCTMwD8+GiNkxaP14dUzV7mXd/tI8K14/m+mjWj/p75r" +
                "v91483tdIy68c3btv68Y2Zrh9vKabS46jPx+8PYB1++rYdx9MD" +
                "XIf/HyzcpuU=");
            
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
            final int compressedBytes = 2357;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXFuMFEUUHRQXJEQRlNWYIG5iMNHoj1FWTHZnpluJBkReuo" +
                "Ax8cMP/dAAfhkfO1MLs6OwoIniGxXjDyhKYtAfDawaP0BcfKNo" +
                "VMQHIA8REBS7+nb1rXf39FTvGntT1VX3nnvuvd3V1dXdA4VC52" +
                "eFYIMaWrRNS3Vnb7eoa3ajTN4bbnga15o8u8mvcmFYnxn3L+he" +
                "HNSttN27NpRcbLU/16i5RJGcH2SzvvmYqx9aI7rIcBwNnivnKZ" +
                "JJDR3BywoFf4u/BWrYUzn0wn5fsO8DOWLwDzWinnlgbaYLs1kr" +
                "2vM63k7klyR9qk+Rk1kCGxTqWcxVteDxKqMuWobmx2Pva9J4XJ" +
                "/DeNyQ9jz3vu54PG7IbzwGs8NGrKFF29D3nhF12tllYwPz2kZH" +
                "82ymiCCbvKKyzY/+o7S4HY9OYh6XZTzW7slnPEZn4y2soUXb0P" +
                "eeFnVm+0Z8ORiPmSKCbPKKynq/3tHM/KjBBuOxtmjIxuMdacdj" +
                "pnP7AdbQom3olx4UdWb7dBpkdhFzo1rIJm2cTa7MjhcKdDxGd8" +
                "w9sp6cnmB/Mm4dgfmxejiWnKgMBNm8b7Q9png7JSgjJdQ/QVlj" +
                "5PgzGF9bBcnfEddw2XP1qGpPTks3P5JhQTmVtEjH/xgUqL0V8Y" +
                "yyQofrFPKt9qc/S5TPf1I5+8eSJSoPj2HxYw66jXlOYrfrk6xx" +
                "w/FIzhA1xWn2vm7jMZXgObPWY0Ok28gYmy8TX40UBnHzfM/Hth" +
                "TndHtfm9t0mV+HUKVZNvBlisqNjxRR3B2NmoWwL48qjwrO/Z1u" +
                "vVDOZrfawjR5pPVcWe3sPcxRKFBz95l9wX1hi4zrPJp5vdIt8i" +
                "FrkkS6824RMSx+zMFkpdPLEdm9J8VWfITV0Apn5uVQYu1MbGPf" +
                "zKbDoMaMUDHSHWO5iGJRU6ZAPtMek53d5F22FlFkgm0dXh/m/j" +
                "2F/9hQrcNNnt28p9DMxx2sVT/L/SzsP+vgXtiRRevCc8I1PQYK" +
                "1Dg/1s/R4YpjMo+jKUE2m3TekyQSzw0ihsWPOWiP46Z07HZ9ov" +
                "UyVkMrPI6tolZt29ka0yAiCdNznYjioy4u6/Htnu3sJu+ytYml" +
                "O37+IvG85r2jRbZlfZ4J2S9N8SRwS/Jx7OZWMd1jgxKfc9NxrC" +
                "2P0FcleL/eqr1LO5e0e+2lXbSGfbAKiHq0X2/z2qsHQI4Y/KNS" +
                "kDA9YwzXE5EGdVTGM9SWIhO1oVq65/mxhzFWD/I+1ZgYDiJhPH" +
                "IerF1Zjb4gWl4rZ4j5sRxDzVRvarCfyvYhz1TWr09kEh6Df7RX" +
                "7Zc5wIYxIWM0S/WL9ryOt8OIZI/IbIoJLTF68CzmqlrweJVRFy" +
                "1vWRwBBerKD/E1OFmaI0IcIDPfr99TZp4RyRLNfDVCjovPweY5" +
                "id2u12v9if5EqOkeZdCvXw5SJkEstqr9DINsPC/aR7NUnffIa3" +
                "DP++D7aoxiTCIzY2StWq+qF+PEOMRMRX/IyqNt73u8zcqK4+MG" +
                "VjnqOnzAzTqNtFv9bjWsKzenjnySiyi56/oa92vV2vN5r4Yfmm" +
                "Dw/Fzenjv/ggI1Nx7f1eEA6dZ7ksRuxeLHHLTHsS8du12v15bG" +
                "lsZCDXsmo3/llnKLKEEsQ2NRW6yN/OHbqxaVTY1J9MFLZIyI4H" +
                "NB71BkzyKWxSzmJPOLrDxaM/vE61hyrduRR+hzyMm8ry7iGe60" +
                "uXo2v+8pd5Q73L/vKXcM1fsek+e83veUVvEt7AVndL50hucnMa" +
                "gYXgdtE4vlPe4qw4ibnzaqNPKGroHbyCxyM7lJkHXFcV2dkXWO" +
                "Qb4gKLc6iHq2VWt4Ok+XDZnrbH65PfY8RYqky97XRt9VeyIJ4e" +
                "z9X5eNT84mz038HUDtqUbX4cm/A7DY5vw7gJr05dz97wCk+WKB" +
                "2orO6Ax7XzsKZuiYG2UxRahjMvGZrHK7g8dfNcsPS3HeaO9rc5" +
                "MwpWFJiBQR9th8mfhUz/lu3O8pFktxzrL3tblxmEp3EqLJ4zjL" +
                "xkcWDfJ4HB6fwUmF/+RG34dbtIb34YOdDbkvvs+sls73bHtfO0" +
                "ZmN49Ifb+ebeMjVwzN80yhUJ/s/nmm9sJQPc8s7c3veaa8u7w7" +
                "vKNEe9aGfm0N0zIpr+UL3xOZkBGlMpvyBCf5wD+RWY6Jz0WNjH" +
                "SINqI9713NRvUio2HzPd/DNpSgXsJbAIbViOUR+jNGbXh+E07k" +
                "0fuQOVhMXNSelEXUotlgJDyrGFGMl/IE26TYiytZDa3wuu4UtW" +
                "rbztaYBhFJmPD7NYfioy6uNH6/XpkmApN32VpEeTO8GVDDnsmg" +
                "XzqbSXgM/qFG1IvsqEOpzCbGrPKLEp1PkZNZYvQsG5WZt+DxKq" +
                "MuWt5SumZ+i+9wy9zf14oH815vGL8rrM3bc/FKKFBzz9cv63CA" +
                "dOs9SWK3YvFjDtp1TzEdu11v0trWPaV73a97nKzVMq17IJukdQ" +
                "95PNt7XOtxbPtfHce2/NaPsHUehwI197u9kmgBOEBmvor3yxKV" +
                "L40HHsPixxy08+O6dOx2vV7rb/O3QQ17JoN+vcwkPAb/aK/aL3" +
                "OADbKjLjyOJ0R7XsfbYUSyR13cMiezxOjBs5irahGgjst+RW9y" +
                "tAwN17X+fU+tqX+7r17X4fzzioM3ANYv+uQlg3xaqvnxxWy/p7" +
                "A+X89xPz8WU/+KoD7X7fxo8ozHkazLOj+WWkutUMM+nI9bWb++" +
                "SJQglqGxqC3WRv4wm0Mqm3Q/aJV98BIZIyL4XNA7lNqremYxZj" +
                "EnmV9k5dGa8f92vFaX3jQV59n72lEg2xxJQjRxD5tn41M9O3zX" +
                "OBCUb6P2L0H5hPzK5keyp35/uD8clGhuVb9zke1hHf3fLSR8Fv" +
                "LHkwPkEFzXJP7qRD6vDJCP/PHaOHaQr8mPZCf5jnxP9sbSfUH5" +
                "Q8B9Smv4zkW+jGRfCIiv6Hcu8k1QdgXlJ/JzJN8veybab3YUle" +
                "66Jr+T+NnM3+vvhRr2TAb9+gNMwmPwj/aC+/VeWc++X4MEdeG9" +
                "603RntfxUWFEskdd3DIns8ToC4Ul2+VcVQv6/Vr2K3qTo0VL8/" +
                "26rvzL73S/f/TH6e4zPaPpd1d/XPNXkfm7a+h3qy2qNLGnG489" +
                "I3us/1aSO47d2fL0RxsyHDDrBuE4jm4m9oTtX4xZmOY=");
            
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
            final int compressedBytes = 2098;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW1+IVFUYHwNJIigw8782qWu1FYoZiC+zd+5lFyrCB10fE/" +
                "ZNBIvwRUGc9bbzJ8unHhLfwmDVFAkkISpdjR42cCEzeokUdF+K" +
                "HpOg7plvvvt93znnnrlzd+7MtPcy58/3/b7f9ztn7rn/ZrdQML" +
                "cT72PL26H7KrcLqbfKsOyHT1bmCl3ZJj9z5v3RbjdHk8iwNQ0q" +
                "XBY+wWKKzfKpuL9RzWNlZTPz9qblBWfOVWnnMbKs7c48VpY7vV" +
                "sS5nF7An51tnmM0a/y3sjHWEJLbfUPNO9+alPftiVjyJOMMDG2" +
                "DCxHS7Viiuz73Zrc7EnZ9WgTFb7cql+KPqvDIq7rcFP1atp1HW" +
                "pHWjgUvgjHY7gytq1zrevwuWY53GYtrWm3rsONal2Hz8f99a16" +
                "y8l3NOSqBa7rzaET59XNVus72OPu2+J0TPUH45veU+jSBkxJfF" +
                "MrCjlvIzvhA2XlvXjGt0Vzcl7HAdJ5xvit0+ztLNr3dF5iUD+N" +
                "ISnK5ucjbJ+9/ejjWYjn0ZuKPtOMY5/GuS9B8VR7TCcIg3/axZ" +
                "Soaroz+0I375TZaunc6+7b4pIwnSBSH9l7XXzhjkJPt5PX4vl4" +
                "RdM57u7Hine2x3SCSD2P4y4+fTR5b15othz3xDMuBovv2oL1Xc" +
                "szrvqwa/P4odnKypDTink7S1T4en6KgmJQhDIochv066fAihbC" +
                "8ijEEJt3gBAUT/w8ljxU8xy8b2qUmnRVXFn07R4w/VIn18dHKv" +
                "MRK6H9nX50LlMl1MoDPdWuf4QWjqGdPNLvHYYcYCEfWXU2+e2a" +
                "/NJiyyk5MZLUR/N4WB+rGcHxJqNNLY/UjtGL8XPhJ9mO8tIXhQ" +
                "Hceq0q+C6ex08zKr7Pe/UzAzKP93ubbzJ+v1O/kFHx7wN5PPZE" +
                "1chpLKHVPDJPw4fbZNvN1pmHEO0wwWmJ4qrbZ3azJ2XXoyUqHO" +
                "PvH8O35PvH+k/6+8fwzTTvH71DA/n+8VCa94/hGwt7/8jW9ZJ4" +
                "XT+IVsMVsTaupLkPN1FdPoPPZ1rXV3p7HqH3FPX5jGei6cIAbr" +
                "1WRb/P1P/K9lxYujSQ83ipb/P4j3Gs3k71fH3Qdn7s++9cB1Ov" +
                "yAy/c1n8x7HVWJptnNVvB/F4rH7T07vHp+n82Hg84wq6OJDrOk" +
                "dV/rA/DCXUaIN+Y5k/XL0BdrBVr6OXInQOf7h0jrOTj6wczX0S" +
                "wTObGmVOyYmRgIVP6Zw+VjOC401Gm1oemXQ8hicKi2ibfKLH+e" +
                "K73EbUqt4U55iZVCvol4Fc1zmq8sf9cSihRhv0G+vRwjG0k0f6" +
                "S/c4O/nIqrOZqiS/tNhySk6MJPXRPN7Tx2pGcLzJaFPLI5Puex" +
                "pDGb/5z/M9shpbB02VP+qPQgk12lp9Dy0cQ7vqTc7oHP5o6TJn" +
                "Jx9ZdTZTFSnSM9p065wYSeqjebysj9WM4HiT0aa25dnt7/bOqh" +
                "Jq7yz2sA1lhN3NsbArK1jQD7V3tnnf2/JgDNg4A7WVB7yq5vzU" +
                "0zUmawIbqEMO0MFjZDxgAMW9+ghpfIiGSOffmx013vecSfW+5+" +
                "hAvu85muZ9T/rnGf6+x5/wJ6CEunmkTmDfO4YWjqGdPNLvHWsd" +
                "8xOcMV4JIl76JIJnNjUma6JIUo+jMZl5BMebjDa1PDLpOlP9eT" +
                "HdP548nh93+VH5EZRQow361V/RwjG0k0f6JTv5yKqzmaokv7TY" +
                "ckpOjCT1ah71sZoRHG8y2tTySG0U/5qtzt6bOb+5Pf06HtNlrj" +
                "7KeM52XWfW6tcZS/z/5++a16a5zoTnuvW7Ap0fQ+POdfKPVE+W" +
                "fza//yPWo+JI347HI2lR3dFYuRfPYw5v4mvP9Gsepx70Nh+bxw" +
                "s5zOOz/ZrH2oq+zWMOv2jUVi7G4zEYDUa9OVWqOjoXz2FP9VWb" +
                "WwgbjEIsWIAJLKoEFJSKEyOQn8eSB3LxbMRK+aVGqUlXBXjk0T" +
                "NLFfHVaE56kUNXQaqb3rFgLKrHoG7N7Rj2vQ1gRQtheRRiwCJb" +
                "PJ74eSx5qOY5eN/UKDXpqrgyHI3JTDq5Pj5SmY9YCV2eLc9G16" +
                "dZrJtXrFns15ajhWNoJ4/0x9e+Wc7IrTqbds00+KXFllNyYiSp" +
                "j+7Dr+pjNSM43mS0qW157pTvGFf+pk19aqvAi33EklXipR091r" +
                "uLOxhlx6AVS9ztKF2d1Bk/F97Q7UnZZQ63hWdOvM58uZier6s5" +
                "/l5Yvlu+CyXUaIN+YxtaOIZ28ki/ZCcfWXU2U5Xk55aRB7ackh" +
                "MjSX20utboYzUjON5ktKlFtOu5sLFjEJ8LR+azPReOzOf3/tG2" +
                "hfF/Z3reYlrXvR4NOz9+lcMd65J+zWOemYOhYAjKYIjbWvZ3wY" +
                "oWwvIoxBAb56V44uex5KGa5+B9U6PUpKviynA0JjPp5Pr4SGU+" +
                "YmWj2RpE5wJVBvE5AXqqX1sHVrQQlkchBiyyxeOJn8eSh2qeg/" +
                "dNjVKTroori+57bpp+qZPr4yOV+YiVo9VW2qh25FEt6NU0Gy/J" +
                "TrXkoM3kd22I1XPYGVETqiK7RESj2WBj5zY5fjlO2h2qimqnHn" +
                "yizJuljZdkp1pyuPid81iUbDY8MaImVEV2iYhGs8nGzm1y/HKc" +
                "tKcbhbjOfN39M7L/sF/XmTwzB9/TrnrcFrV2SQtgZAuiyGL2CY" +
                "k5eSx5qJaKpEa7bpsqyt767LKplroF3sIvWdlobtEe9W81PXG/" +
                "8Zq0AEa2IIosZp+QzaPibxlLHqqlIqlRx0iEjRl59MwSy/PLvD" +
                "IbZ+Vo57q+nsMKWNqvdd3rzGweZ3IYzWN9m8c8M/8HsypCVg==");
            
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
            final int compressedBytes = 2162;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXEuMFFUUbaMBIlETDBmUqIwDoqJkkvmQiIkF1U2ibly6co" +
                "cL3YiDKw0w0zMTh2pc6s4YJcrKlZ+wYtNqjMbETzT+JhIBGYdh" +
                "BhEQxU9V3b5173nvVXcxXdWV2JV6n3vPPee+11WvPj3gjXgjlY" +
                "o34sU1fagX9YOHyMoWweooxpAFWzGLp/mrf2OseKTWGrpv54g5" +
                "mVnpzGxlzKJSEW7UNfWEVY171BsN61GqW55R2uqzwRNkZYtgdR" +
                "RjyIItHd+ax5MYKx6ptYbu2zliTmZWOjNbGbNQ82jomnrCKmj/" +
                "Ff+VSiUqqY481IvawR62aIxs4kE/Z2RGxPO4gPHikY+pYSq6NB" +
                "HPmUn2pIxjtSM03mZ0ZasjjVEc5lawL80jn3ozPd7Vr56qFPxx" +
                "ZVm88kR/XN6U9O8Y3xuWffE8Howtd7eNX5fq2WJZ1oej+T2HnG" +
                "9u692UMo8pyhO3WJbNV5XN1hT7z9ya+jj/7636a6WkT6+V1Tx+" +
                "UsBolrrnmPq0LOXUteSIf4RKqtlG/WCSLRojW9SrN00OijHZ2V" +
                "49h/HiwaxsjJljek6M09mTMo7VjtB4m9GVrY7ET7Q+tq4zM52/" +
                "Cfs60+Go+Le087pQZbrOpMxjoyvmLc7RXOk+58lX23rfSJnHK1" +
                "muM5OvX+11JubeUN3gtkV7cIi83GesWLGH9hT2s+yhCBdGIrnk" +
                "LS17zA4zE2XTnqaOGu0tWtk4U69JjseXnKv8d12dXWcyXEe+zY" +
                "D5UrW/DvfvsypPfdOB+atljKqv2scttEV78CZZuY9Yu4d2za4U" +
                "L7CHIrjlzotLNw6zl90eEymbdre2qZGmqZWrA9WBsB7gOvYMcD" +
                "94iy0aI5t40J9oDGjGlu1PjNc+HYf8aHFpIidHSvakjGO1IwBv" +
                "MbqyJU9tvjbf6q9lD9miPXibvNxnrFixh3Zmwk9tBXsoojZ/4D" +
                "HRNiO55M2NEvWI6cDtmJkom3YXp52J8R2vNVjnbYy1Pr6Tx/oY" +
                "vAt5zJW2Ps4VuD5urG6kkmq2UT84yhaNkU086Ed28cXf30qM1z" +
                "4dh/xocWkiJ0dK9qSMY7UjNN5mdGXL6LbvKZr5v6eoXZf5OP4g" +
                "3/cUacr5v6cIPozvw48m/Y8KeKpYLO15pkBl/5h/jEqq2Ub94D" +
                "RbNEa2qBc+Xx8z/ciuGcPRnMd48WBWkpGp6Mrb5ORIyZ6Ucax2" +
                "hMbbjK5sdWTq+54TBRwVv5V2PBaq3Pb5+qdu3lOkPF9fKu35+l" +
                "Jxz9f+cf84lVSzjfrBSbZojGxRLzyvj5t+ZBdfPJp5jNc+HScZ" +
                "mYquvE1OjpTsSRnHakdovM3oylZHpp7Xp/M//mvXlnVeF6lce0" +
                "+2qKdt4fF4Ci2EwVa9yW0psc/I1vF4QmLRIzVmhDm68zbbmpl5" +
                "TGXEan3URTXNqtFtj8cCftGo/lPadaZA5dr7skU9bQuPxwW0EA" +
                "Zb4fHYakuJfUa2RnNRYtEjNWaEObrzNtuamXlMZcRqfdRFNc0q" +
                "aG/BW6hUopLqyEO9qB2cRYtgGe0t1JtsZQsySXxrHn+x2fC7Rb" +
                "/uCxJtLkbBM4+pjFjOWfAufmTVaMedzMHkvmep8j/6TJ3Jgqof" +
                "yk0veX4KLhWwSv1V2vqYSXnqXG7zmPzdQXWom/vwAo+sC8uKOp" +
                "8JdXE53N6St0Ql1WyjfmMFWgTLaNntFreFP16XHWx2TqihLSYG" +
                "EXosok57LYUZc8YxmfzIKmhv0QvP4qikOvYscr+xEi2CZbTsdo" +
                "vbjVUcER/jf9hsxjwumhraYmIQocfCeOYxlRHLOeOYTH5k1eh2" +
                "78OnLud/Ts7cW9b6+OJML1TGVyVP6sn7hdqEE3lnNzrTN2bI5f" +
                "EMmOtVe02493W8V26NZnxbB+ZH2nqfdll3DO0YopJqtlF/5j60" +
                "CJbRststbgu/aCKbnRNqaIuJQYQei6jTPnnZzYw545hMfmTV6H" +
                "bPhdNrCrj7+KG0+54Clf05f45KqtlG/Zn72aIxsokH/cguPrGa" +
                "bHZWyI8WlyZycqRkHx6P+82x2hEabzO6stWR4dqxu7Y7KqlNuz" +
                "/oD8L6EmL8QY2UGnsUJ9FRjPgwxrmS7dZsLrzwUTZm/jIKPR4d" +
                "qVmFjXqUo2YQlc7ZJ2dz61d5f9gfto7f4UxH+XA6NhtDIefecP" +
                "7Itr80yPq4roBVara09bHHytO3Jq31BYzmx9LmsQfKtV21XdKm" +
                "3R/xR0yMP0JIRkuU7lGcREcx4tNaadloNhdaOCgbnZWU2IrykU" +
                "w0K2ZEo9QMWqVT7uq8frY14n7f+i3RtjjXl/40rIuzZ+tjf/7I" +
                "tnfsye+ujRu6Wmftv0v5IryX2pY1vpH65FM/3Fb3s5TnwiczZ7" +
                "4553m8zVL4vNt5rO7vPsPlzWN25Xzmkc/r+qy/3RrB2UzjjN9/" +
                "2NHp1p6c19vzR2Y5HuuzjQzrxNW8x50I86uO5/BNP7qs6/V4b7" +
                "+38eT3yZkHC7irGizreJzcX848+uv98P5x8ik44jPdURLKhSXO" +
                "rt9hji7rvE5RnngtKzL9s3P1ztVUUs22aAvP6wG0CJbRIarJVr" +
                "Ygk8S3zq4XbDY7J9TQFhODCD0WUafdVEYs5yx4Fz+yanT68RjO" +
                "48Z818d4HveU9jxTuHJtU7RJj/ZwHu9Cmy7FLjVyuPmrY7VNnb" +
                "OxudMy5pw4K7TrVnXMxa5tOH4cp2yurLwxb4xKqtlG/cY9aBEs" +
                "o72xepOtbEEmiW/N4zM2G84R+nVfkGhzMQqeeUxlxHLOgnfxI6" +
                "tGO66oO5LzeksBZ9eB0s7rHitP+8k8bi1gNHtLm8ceK6v78KEC" +
                "RrOvtHksVDn938+E8xi/U8j5//l4Poecl/f/fKQoG38f/nJe/8" +
                "/HdPL7beOBAu57nit8XXq4BOX/AGFVb4c=");
            
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
            final int compressedBytes = 1498;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW82LHEUU70MOogcFDyoRRVAiCOYoIkr3OENMRMnFix4SjX" +
                "oYRGGzmJggZHp2em2ZuH5EjwExB13wZLKCn9Gs4kZYoiA5BLx6" +
                "8G8QnJq3va+ru1/n1auqrtUeePOrel+/elPVVT2zG0XxL9HsAg" +
                "lIYWj39um6povW1jUY2e6SMYLRcHmasknvmcubi9707tHiTN4W" +
                "ReM/p4fmPfe3RUlvJzUPwPv08HbP7ijqv2Ffx/TWVu19zf1U5v" +
                "SOWs8eIzYPzqt5FSUghaE9fV7XNX4aV/ka1dN/zcF8FDGiMjfz" +
                "NGMz2BxsgoR31Q+tweZsPh4seso2+FKt8Xo1BvgUkTDi1mje1P" +
                "3LurIfMqpmxMgUJ/RE9pBZH2vdo2xfj9jEtrBuXdenXKzrUo9a" +
                "18eDrevjPtd1/coOFGh69PpRxutmNegfizxf2f4QmVvn48jDfD" +
                "xhz3m8IZqPJ7zuM2soASkM7fwxXdd4l10zuCOvmdnLclLaycP2" +
                "I2jJ+iVKQApDu/eUrqP9+blM7GU5KS2Mxm4EwnX9js26brBV6z" +
                "oNts+k3HXt6D69tc/M6si4MxvvM68H22e8Zo7XUQJSGNr547qO" +
                "9udpMLILzqbaSWzC05jTJZSAFIb2dEXX0f48DUa2rKOIUTLh2C" +
                "srCcf4W5SAFIZ2clHX0f48DUa2rKOIEYyGy9OY088oASkM7fwZ" +
                "XUf78zQY2bKOIkaToQlPY04XUQJSGNq9h3Qd7c/TYGTLOooYwW" +
                "i4PI05fYcSkMLQ7j2q62h/ngYjW9ZRxAhGw+Xp9Pz4rofnwoVg" +
                "58cFP8+F16vjbL9+z30d8wOh6pjv91nH+EeUgBSGdv6qrqP9eR" +
                "qMbLmuRYwmmQlPY05fowSkMLSTRV1H+/M0GNmyjiJGMBouT2NO" +
                "P6AEpDC085O6jvbnaTCyZR1FjCZnTXgac/oeJSCFsa1jyp+nqU" +
                "YW11HEKHmRY6+sJBzjr1ACUhjbOqb8eZpqZHEdRYySIxx7ZSXh" +
                "GH+DEpDC2NYx5c/TVCOL6yhilLzAsVdWEo7xTygBKYxtHVP+PE" +
                "01sriOIkb5Bxz7/IyMY7yBEpDC8UbVot2fp6lGFtdRxCg5xLFX" +
                "VmYcwbrtHL7V4/QcnuwKdQ6nMrv5nctfHRts1fPMc8GeZ57l1l" +
                "G0Rr5ACUhhbOuY8ufnMrGX5aS0yUsce8rK/MqMnoBNf5/Jn4g8" +
                "X9TvM/m+qNML65hcchFv8jKpOSKLmB+U1NHNaMhV8CtKQApDO7" +
                "lX19H+bZrsxnJP2T67wYazqRZGw7Fvj09U8DJKQApDO7lL1zVG" +
                "uczXYGTLz17ECEbD5WnKputzT/xPqP2ayvwfPfe8EqqOb531eu" +
                "45jxKQwtDOP9F1tD8/l4m9LCelzW6yH0FL1gsoASmMbR1T/vxc" +
                "JvaynJQ2X7Ufgck12v7eOPnD/fkx3MUZjcW9puX+OBi62GcGw/" +
                "L9MXPwvCC7Pw6GnPvj0keu/q55tOjreSYdufnsl24Rnd6H/mZj" +
                "f6W/AhLeiz5oT5f7K71r0I82+FKt8Xo1BkTB6KhTV++a7l/Wlf" +
                "2QUTVjEYPmhJ4QDb30sQLGaEV2Pa9epSrbsmdl1mz/Xj7NPTxf" +
                "L4S6Py7/Her+6OMcnh8LdX+k6ujr/z5K+7Xj//tIH3H02T8p2q" +
                "+Pdjv/Pe4zszr2d4Wqo4vMsjpO396Z+3X+vmg+HnZnZXgO3/u/" +
                "Wtd7g83Hqfs6JrtD1TH/uNs6Lp3cruMZ9+ee5M5Q5x7q+54O5u" +
                "OHtZnwtPU+s+l7Pp4ivq91kXkH7ddXgu3XV3ZOHdPfDEZbP4f/" +
                "PrtLfW7PcHyuNS8x75b/YjPf0/18NJpHaj5mweZj1u18TBd8re" +
                "v5PvNpqH2m68yp128SknPB6thxZr/rOlkNdg5f9Vez/un+aZDw" +
                "XvRBG3vKNvhCja7Xo6Nufhq+oPuXdWU/Pb7e05RTj1l4IvtZ5v" +
                "PVsdY9yvb1iE1sy56dzcfZ83XymYMnBdH34S4yk9e/rX130Q==");
            
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
            final int compressedBytes = 2177;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXEuMFFUUbVkIOAScGESjIIZM1JDIkgQ3UFO1mBk0Mf6DxI" +
                "UxJiibGRPI6JjQ3VNDuTAx8RMcF43iLxE3JrIRN7IxGBRwqSsE" +
                "oglRiC5mY6xXt27fz3tV012fboJdqfc599xz73v1XldXT0OjYb" +
                "8OvdKo6dV8qNHY9WkFOlNFvKqInPfyLvLau4jzCP3WVbRzbh5C" +
                "Si4LR6OvpY+xury0cutabxlIW1a72ckfU96YqdW8NynXdfv3mH" +
                "lsbjDt4OUEuT93bdyRadkKNagkyF2NRvhCBevxtlzrmBunPBT/" +
                "Tt5rvxsj9/WVzYPxfM55c1BCnczxHPaDlyRCXGTTabewbVSglV" +
                "5DS01d7TkdgyOaIxl8LMhHHcxDK8uc5Zi0vlTl7Nz1uL+S9bif" +
                "r8dK3meLrcf9vazHBOl7PQZREI2vMCXU4yuwh+32PlMCk7hwGB" +
                "QQtKOi0UcL2QzGFdBiWsbHWE3N7dSjHKMT3NvOCTDIDjUgH+6D" +
                "7WaHYgGLW/UIaXzIRk9vMV2hi+b0Frv3mUWwoF23sxDE3JYsNY" +
                "zu8tKsXjPIzj17THaEvDFjK/xA7utds3xfp+uW7etwsf99zRBz" +
                "nzlSdlfHORba17tma9zXp4PT3pIpofaWsIft9j5CiAuH8bcRwO" +
                "JrtQQIYNDi+siGlrcEVm0nDuUYfasxfdCYUAPzITsqx/u6GwtY" +
                "3OrSR1VgY9v6HP5v9tyHH5ZdTf57jSG96o2cd7+293WR+7Xc1/" +
                "7b5XMOPy6yr7MiV7Gv/aP+Ue+SKaH2LmEP29F3pgQmceEwKCBo" +
                "R8VkzacWshmMK6DFtIyPsZqa26lHObZOcm87J8AgO9SAfLgPtp" +
                "sdigUsbtUjpPEhGz0H+3ztvzOs5+sqIve1a443bshX+EVPrC8L" +
                "XaMd/o7gsCmhDg5jD9tQApO4cBgUELSjYnKXTi1kMxhXQItpGR" +
                "9jNTW3U0/nmJ0TYJAdakA+3Ef6AwdY3KpHSONDdmKZ8CfiegLr" +
                "ZNQT3f43iHAOHWSR9u5VmuCKHNVq6upa+hJxxZSa6EnZ42hsZe" +
                "4h+JaiK9vUMuXH7zWmhDqxTMExPjo+6k+19wFOHDrIIu3dGFNc" +
                "MX2SGpX+3Mb9pL5Eou/tmFITPUENThNZjhXa8X2GjZ74tqIr29" +
                "Qy6U/G9STWiWUSjvHV46v9yegHwIlDB1mkvRtjkium87ha+nMb" +
                "95P6EnHFlJroCVw4TWQ5VmjH88iUiW8rurLlnupu+Eyt3xqfGt" +
                "Z9ps7I3pg3BiXUiEFfI8RFtjfWOoUoIlKJ/HlMqWbnJGNwJO6f" +
                "0JhLUY4hOU+4lWXO0Y86rowmZ4bUB70ey78Wbi/mF/1U4zPndn" +
                "87lFAjBn3T9o4DThw6yCLtUp1syfU7Lv25jftJfYmARnZO5Alq" +
                "5CXHCm1Sw+gyrhyhzpZ79v480zzbx1OHer6eX9M8F6+Kc+Wvfu" +
                "tYbtwzGet4fc+Z9/R8Pb9q/pbu58gtwRYoTU0Y9rEkTLeIQ2pc" +
                "l/zT9XhI+pKFah6D9+0cZU46K56ZHVlmwf7+oOLqeHxmeBzraj" +
                "xf57tb9POw3lej84ONV+88Bu8Pax7rjBy0ghaUUCMGfY0QF9lB" +
                "q3UKUUSkEvmn3wM8YqvZOckYHNEcyeBj4WMwZ/SLW1nmTHyXvp" +
                "4ZZDZHHOtxZaFVvKbEDri5oN+qvvb1rzV+Dh/1RqGEGjHoa4S4" +
                "yKbTbmGb9CmmVLNzkjE4ojmSwcfCx2DO4DW3sszZsGRcGU3ODD" +
                "L/T+sxOFgdy/ocvtvfDSXUiEGfEM6hgyzSLtXJRqhWs7OS+hJx" +
                "xZSa6EnZx/v6gh6r7RGzftNxZTSdLVi8GW8mXpczWCcrdQb7Gi" +
                "Eusum0W9gm/XQnWGpqX8/oGBzRHMngY+FjMGcw51aWORuWjCuj" +
                "yZnpMqe96biexjqxTGNfI8RFNp12C9ukn0a21NQ8TusYHNEcye" +
                "Bj4WMwZ/C6W1nmbFgyrowmZ4bUHe860zfm32eC2epY1v16s7cZ" +
                "SqgRg370h0SIi2w67Ra2SZ9iSjU7JxmDI5ojGXwsFB3Ow0fcyj" +
                "JnOSatL1U5O+93AN4D+ncA4ZmyvwOo5Imr0O97YDTL/Q4g/KrI" +
                "7wAGv6+jK0P73vJyfdrt59qPWVjp73GDjG+o2s9m2/rK+vHcv0" +
                "Cf7S+rIizXPLafaj9a7Ty2n8iex0qufu48tp/O2Al/9qT9ZKGV" +
                "sxEOiZVejxuz8fLqy2nkRS+rnem1CQ6JlR7npmy8vPpyGnnRy2" +
                "oP+P1xQ87744a693W/WRVhDeb9MRjJmceRYb0/9ha5ivyq+tzT" +
                "zHkm8D8Z1ueeOiN727xtUEKNGPQ1Qlxk08lbwTryI//0eq+z1e" +
                "ycZAyOaI5k8LHwMZhTR5ZcHCvkrkcqD5oZUh/4ejw2tPU44Mjl" +
                "f48brM2Y30PZtr6u03yVWRVhWZ/9/wkvhhfCa/z5Orzcz+/snb" +
                "ncnWj/bUX7HW3lXuGlXOtfeVnlel4BVnj1+liPubvro6Ht61oj" +
                "1/HvPnZ+dj1+3wNZLfd9T7F/N+y8h++1WxV9Otg7TJ2qR1Ptvm" +
                "718etMc5+pZD0Wus+ED9f4+XGPtwdKqBGDvkaIyz004u3Z+Tn5" +
                "kT+PqdV0Tnn6kqMZEsMxmNNk5VKmWcD4Mq6tTzND7Do+Py7clG" +
                "17Y21jSK+F9TWuxwPx0UlKqDvdHrY5QtwDxjfx76Q1IYh1UrSD" +
                "HknboQaW1MrsTLHrYWkc0B5yTKhBebAMGRc4Kasj4spoTBXY2L" +
                "ae81/tzvJb8fop8DsJ41fE1vN78slCayYjsv3/pVS0/lc2bsiX" +
                "92KN2lu9rVBCjRj0NUJcZNNpt7BN+sn74622mp2TjMERzZEMPh" +
                "Y+BnMurHcry5zlmLS+nhliO9b5m91sDxa8Ogevy/VYZ1b/Aa3U" +
                "1U4=");
            
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
            final int compressedBytes = 1562;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW81rJEUUnz0shFwUVJwgRhaJikoui4joIanuOSmazExwd0" +
                "GQCKtEyEGUGPayM7PJLA168OpliXMTwcOyEFhhE+JFYQU/Drse" +
                "PXjT/0Ds7jev33tV1UP3THdX0jV0fbyP3/uY1x/VmTQa0Fpey2" +
                "skczj0BjLYI21wwiUa1hbpIE/d5rbS5Dlaui/kLfeKejlTt7kn" +
                "HJV7FPlHOqaVNN/7F+L+kWT9TO/TsH+SSbwwKeZ+M5XzkkF5Kv" +
                "TzTmPGpu70H5vo0VKaXor8gkF5Po8//WV7HofzZeYxuNCYuU2X" +
                "x+ET5eVRdVUXehjj762La51CsiituoMTpCJFIpH+uCrummha3X" +
                "R1G5wSIeg0G6KMIT7u2pGlz8HTul1pTWaG0C35/TLPt8Gvj5nO" +
                "ynszn9f3qtXL0vx1fx16GJEGa6JwGfoQR/LHGf6FUIjqXZL6nM" +
                "e9kviSYrMpMVGTvAfLMlZTI6zHZ/31wX3pm25F+hpzOn4nHDs4" +
                "xpwOronCZehDHMlPbHQ44vjO97vU5zyuJ/ElxWZTYqImeQ+WZa" +
                "ymBpc3EW3ejjltvx2ObRxjThvXREGqt4tc0tAxIqm48nYJBWdI" +
                "5dLES3wz8CXFZlNioiZ5D5ZlrKYGSHFJPULdW+B4x95xqHuMY4" +
                "x0jGuiJNQecklDx4ikYq0eoSTcHkeQtqiZ+JJitSkwUZO8B39k" +
                "rKbG2Oue9E23In2NOUfeUTge4RhzjnBNFKQO/kEuaegYodS/sd" +
                "YOoTDEHalPHOabgS8pNpsSEzXJe7AsYzU1xv7tSN90K9JX1NRb" +
                "9Bxe3v3a6zYctWyWgxcLs/d9Ose7lkUfpHRZwM2CUI7/ZDk9Qp" +
                "AqxseS6/EdZ/VYquWq99fBch33hdXn0dtwlcc0y5THG9+dnTyq" +
                "w5n3hYdTvu85zFKPw+bZyGPwqqt6vHm1Vuf1mrPzei1TPT532v" +
                "JokY3q8Q1XeRxsZa3HItrg0f4nyXfzSuMMtuuLKZHNV+sH5VF5" +
                "UyJ8exrzO200mbCbqgk9jEiDdTwfESVaEVcefBZJhbMRQ8HZiB" +
                "C4Zd0naYNTxihNE0WPhayPj1EK8khEP0r8t+BLVI4+qR6nrugJ" +
                "9ej/5aoey7Tsb/gb0MOINFhHc3UAdKCpA+SSho4BOuG3dEAoOF" +
                "MHHEHakl5JfElBZB2FVqgJaKQlY+UxoXS04pJ6hLq3pJm+vw4u" +
                "NWrUgndd7a/VnP7cs/f1rM+Phfg83X5mrrzncH/T34QexrhSN3" +
                "Gt5pHCZehDHMlPan6TI3Kqjqad1wa+pNhsSkzUJO8xGhOZa3B5" +
                "E9HmLdec5b1Z7ivyn87uMxVbLjmPD5zl8YGr66O5L9y/P+v10d" +
                "92dX1Ms6ztr9+abn+dXo/ekhd6dEPsSr2lLMggZZMFzJnvvFem" +
                "0Uqz3L+VVXLCfmZFrUAPI9Kij7fgLaiV4D2iRDScgTQdfBZJhd" +
                "4sAJXwIxohcMu6T9IGp8Q75R8kzYZI8oijW0aZ/i0efTSC/zZ8" +
                "icrRzTZ8O9d7jby/77ni6vpYpuXWdmsbehiRBmudQrIoTYc5wz" +
                "nhx9FcNtFMn6QNTtFlpASPhccQHbplKYs+y5h0fD0zJG25XiS/" +
                "t1RrJVTFmrN6rNgyPfeoEv5SqZz93TXNcvD+7NhDy28MhsnvqV" +
                "Z/Mmx+mN/G3tVUzgdji+0q8mhGU+D18WLrIvQwIg3WqyeSQrIo" +
                "TYc5wznhk02JZvokbXCKLiMleCxkHY4oGhuy9FnGpONLVC496f" +
                "3j6o9GPX5cRj1W08xoxjFtlbTDoTz+Wqf3ZlVHQ3kMtuuUx70t" +
                "V3msV1v5rzgp93kMPnOVx7TfSVVTj/tfFPwU93MBGfmtSMvme4" +
                "qzUI/qD2fP4RVbLu89bv/18Lz+vACcN6eq4kWX57V6WHBVPHSF" +
                "kaZVdIQV3Wd2nd1nXnZVj+q1Oj33VB1NufXoX3aVx6ot58tj3v" +
                "fhvrP34VVbLrkefWd59OuUx6Dv7D7zd63q8Zyzejx3mvOY+/p4" +
                "3lkez7vK4+pHdXruqToa9v7xqzrl8ebiaT6vz1A9Pl6cVM7z2v" +
                "gFUPBNfrz91P+32J+rNI/LJYL/DzcCS+0=");
            
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
            final int compressedBytes = 1452;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW82LFFcQn2PIRUMQDTmEGMIKgdwlkMy+nT4sufsfBHIISk" +
                "LOWTMu66YDHhZXEwQR0cWooAEviegmumK8CLN78pDkEJbNXpyb" +
                "qxCWTPebmqp69V7b3fN6Xs8kPXS/6vr4VdWvX09/7GyjIZf2Fy" +
                "BNv9OYoGXU3RAeD5q2eKU43sKrTssrI+XxoF0fX6qax7ou8ZVS" +
                "PDb9eVlYezvd7hnsv/X1l73tfuJxKDP+gNPyntC86elIv55pfd" +
                "fBkHL4v2F6tacKVfP+uM7Hkuf1R/68JuO8Lsnjh/68/ss8xj/k" +
                "8rr6P48vmWkz/rzyLMl1Ri/Rp4LjToHjIa8z634qPJF5p9J+bN" +
                "fLbpwIU/Wfj9HVUPNx1Jmr5TG+FYrHk/uqxW9u4tjcbG4Cj4lM" +
                "7abs0oDObrFpaXaXnXrlrcBduzuLzJDVM8O8TMfm5QGP/X2wU9" +
                "8sDUbaLFQb/cljEqstKhvZXQG35ZFtmix7VrUVn9c/Tep5rbZx" +
                "VNtqG3hMZGo3ZZcGdHaLTUuzu+zUK28F7trdWWSGrJ5Bim5Ht/" +
                "VWj6DT+6ihPtSbRlGJomNsKp+RaOLKauTAD0c2a8JstsrMzDye" +
                "ZqeryYBZL+YR94+7A7aPizddF4c+u74KdV7Lbjzj7+CodtTO4L" +
                "zeSTTxz2Cnvlka0NktNi3N7rJTrxN38lXAbdl98AzZ+KhhmFs4" +
                "qi211V6FfW0Buym7NKCzW2xamt1lp155K3DX7s4iM2T1DFI0F8" +
                "3prR5Bp/dNDfqCN65SAhnxU/msRDO+HefMHFRj+nAP2gvtIV3P" +
                "2pF5zbwnE99kBr0t9z2rhZ511wo+nX0X7LmwwszqiDqit3oEnd" +
                "43NegL3rhKCWTET+8fVyWarInnoBrTh3vQXmgPyXpynx2Z18x7" +
                "MvFNZtDbMh9P6XFm78zeXt+/+D1yCWaYpcrM0dPoqd7qEXR6P5" +
                "HVDa1HH/yghds5OtrS43eDx1MbjeP4XKMx3DVhpEbDKN6rlhEN" +
                "svO8vEOz2r6lG3V7YxfG1NKFfdRQH/yghdsHOboUsc/jIx5PbT" +
                "SO43ONLSfHhEisXmfmvcoI6i8RbdXSSNd73PT77DePb+TWe908" +
                "DPUe15U5fljNuc559Ppms8djdDrY+/DTo/0+LsZjkfuelMdzwX" +
                "g8Vx8eh//7THQ+GI+5M4/F32fuB7sPvx9qPrZ+9X9e+1jKzcc8" +
                "3ZRdWhutDb3VI+j6+/dAQ33wgxZu5+hoQ62JJqvi+Fxjy8kxIR" +
                "Krh24kMo1g/gLRVi2NdM7HNb/zMezSqrTSrN+btR6kGq+/N1v4" +
                "xEPNpX5vprux+LPfm82fKfN7s1an1dFbPab5OrAfr4OG+uAHLd" +
                "w+qLxDEanWRDM6FvhcY8vJMSESq280Fr83e5UR1F8i2qqlka7r" +
                "9fThxgQto+6mwueZDzzhfFwm6pu/8njFG+PBowrGo2rUeT4Wun" +
                "/s8Tj9TygefWSuz3ycfhGMxxcTxeOzYDw+myged4PxuDtRPD4P" +
                "xuPzKlmr4v9notfczzPaFuJ5xpWZP88kXn7+f6ba+Rj/Huz+8d" +
                "a4ndfqszreh7uqCsFjvvfh6nPHeR30/aOuKtdxmvLP42KJt8jN" +
                "lTrOx+ZKyPm4+KBExU9qyeOT0fI4/N9n1LFGDZdRVzX8daZ5vZ" +
                "bz8frYXa+P1vJ6fXTs5uO1Ws7Ha6F4VEsl5+OSm8eymB7m41J1" +
                "nKlZNau3egRdf39ZzcZ/oAZ9wRtXKiVxiI74qbws0WRNPAfVpP" +
                "ePd7jOhoj+A5xlOzKtnvlb8Dkq9bYc7VN0z+fvzdK8Pwa7Xjsy" +
                "ty94wJ5X83qrR9Dp/UROeAQN+oI3rlICGfFT+aZEkzXxHFRj+n" +
                "AP2gtm76837cjtC7Rm3pOJz1Gpt4XbBSkVPDoZcfHfoeZj1f9f" +
                "KFj4VkoZz7prWQgW292h67s72rhcy79s49fu");
            
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
            final int compressedBytes = 2210;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWc+LHFUQ7iAISRR/hESzHpZkI+6u+AMhOThZs909LWyy8e" +
                "BB0UD+AI/iJUd3TGIyoP/CQnCJFxE8SvCyeBE8ZpkccjEgLDm4" +
                "OS2bFezX1dVfVb3Xs7Ob6R0z3fR7VV9VffW9t9MzPbNR5B/J97" +
                "5Vf3RW+zEEYr9FT3jsleHJO+/uWPqy6nxlj4qvRP/DY79VJd/5" +
                "1i5fj33q0k9HtY9Nds5OZidpdDMw9nkEZq3OKjIZl7yoL1dzUX" +
                "aUEcyyh/R9jVqTZoZ6umxnrULuie5r+8md4cz2mfaZKHIjzS5C" +
                "Hts8Igen8zqrloNqmAmM5T5+pOtlTNZBke0I5jpNqIR66qzX6l" +
                "fIfJ8xpFZW6uPrr9Q9erveq31nuF2Xm9wejGEQ/mFV+SvcPX+S" +
                "JRmNNDNGfmGvAHEeovqSlsvKrRXBwtYKGGRnq0n3kEjJkvksdi" +
                "3oXl4rNcwravUrlf4Av2aV7Du+Hm/Ve7V/nVt1ucmtwRgG4R9W" +
                "1dKyzRqGxpz3nyY/NbsHR/V5ff3vBp9xLiQXaKSZMfItglzOxu" +
                "VbbIMfPTWbr0n3kEgUxesWCzHqNbgrXg8za816TZbf7gyy+70e" +
                "u4ejMTq6hxr99nKiGF+o/En3/rj0StH5+QKZRva1P736V2uZ3/" +
                "SQ14ak+Ujf6Oth/MZUTf5xdfcv5sgbu1Lz9k77mK2P0z7Sanba" +
                "x2u/7G0f+Ygf8EgWMG1pu+6oz9+ZKX6wUwdSqdWxV1+7c0a/7r" +
                "a6jiXu8UhWKOrbtWp6u48gY7AOyJKqd+7cn72uu622WX3v6zl7" +
                "X19dfIrv67lB7uurFxp4f2zZfQywPD372BpkHwtkyPvYfWmcXo" +
                "/dF0f2ehzK53X35afp87qR5575sbqv50d1X2fnxur58dx+PD8K" +
                "fKvq/P44fS9scjVxJ+7QSDNj5FsEuZyNy7fYBn/xbnncZ/M16R" +
                "4SsTk6Q65FrsFd14+GmbVmvSbLb3cG2fWvx3Qqzb+RdidkLJ0a" +
                "5K9DWaFc4nzSo3NnL1WDdx6GxvzvVr4HphPphNdhYiAdE3W5Ic" +
                "79OgbvPByNnQPRWB7xz8PLskd7rb1GI82MkQ9E5uBERMc1O2JA" +
                "LZuvSvNrJNRTc3Il1Oc79Ktdq1+BLK1Cd9Fai8hMeyafZ3guIj" +
                "PsA5E5OBHR8arHjGSUqGUz++jxayTUU3NyJdTnO/STXatfgSyt" +
                "QnfRWovIbHs2n2d5LiKz7Md/MCJzcCKi41WPWckoUctm9tHj10" +
                "iop+bkSqjn1fjMskLm+4whtZzd7zk8/n2cvs/QakbxfSa+O1b7" +
                "eLfJfQx8Xh8Z08/rZ4aXZY/kdHKaRpoZI98iyOVsXL7FNvjRU7" +
                "P5mnQPidgcnSHXItfgrvhAmFlrdlm6r+6md6bKbCWtfG7xXERa" +
                "7FsEuZyNy7fYBr875rd9NrOPLdtDIjZHZ8i1yDW4y3bWuaxZr8" +
                "ny251B9n7f193pUd3XNz5vjrt9tn2WRpoZI9/ZyX3CkYMTER3X" +
                "7IgVf7/7ul7GZJ3m1whx1GtCJbGhSq+VbLBxd91Xr9CqLSNz7b" +
                "l8nuO5iMxV/nuMyByczuusWo529f8ktjkG1LKZfZyTimxHMNdp" +
                "QiXU53fCO3atfoXM9xlDaimSbWQbUeRGmovf6TbYTz5gRObgRE" +
                "THq1/8NiSjRC2b+aXQ49dIqKfm5Eqo59X4zLJC5vuMIbVlZDKb" +
                "zOdJmsvIJHyMjCEqL2uBCYxALZv3i6vpgVMzW03oFlIWX9Y1ur" +
                "567rls+2rNVi9n9/1/YTpOz+HXDu/zc/jIPlEbfg4/PrysgX5/" +
                "rPYxPtTsyrrtfd3HBleTPk4f00gzY+R3P2RE5uBERMc1O2JALZ" +
                "uvSvNrJNRTc3Il1Oeryexa/QqZ7zOG1HJ2398pDo7V7xQHm3t/" +
                "TDfTTe81WmCEYwTGtvRkHIwhdkSYpV4DGMN5WgMurazax2MWD3" +
                "HGx2wPX5v1cpbtdDuft3kuItvsx28xInNwIqLjVY9tyShRy2aU" +
                "evwaCfXUnFwJ9bwan1lWyHyfMaSWItlitpg//yzSXD4RLbIfny" +
                "CUEeTKKs4hRFuyHvyyFhHMsof0fY1ak1UllfFqfGbolPrkSnU/" +
                "sMrswP3+V/Uf2H/H6bnn5o8DPUNc3At3dj47T6ObgZEfv0soI8" +
                "iVVZwDNsmLevDLWkQwyx7S9zVqTVaVVMar8ZmhU+qTK9X9wCpW" +
                "s5At5PMCzWVkAT6PwKyFHEK0JevL9/EfdC0imGUP6fsatSarSi" +
                "rzO2sVYh9NX9tP7gxnpmvpmvd5VGCEYwSmYzJf4xwxn4crHKGK" +
                "UA4qeeQznGXVaZ3obPG67rpHf4Q793t+LJGhPj92Px7V8+P1ow" +
                "0+P95L7yWP3Ehz8og9tm8+50bKRC6dDiWE48xY/GJbRhBzmGTg" +
                "iLNcjYu6WcbhQWPnjqz2NRFG6piD9MgatpeW0YuyZNSuEOvj7C" +
                "LSS3v53OO5WHWPfSAyByciOl695nuSsfw8/ETXy5is0/waCfXU" +
                "nFwJ9e71aNfqV8h8nzGkliLZTDaTbLnRzfn+brHnfGc7jBHkOh" +
                "xI8X5bIm6kLBopxhZzag6KUEfZDazo7zItpiugivKZR3b2VVSv" +
                "sC0dZQ6rAqqL6HQ2nay70c05vs6e852d39cVgtwsf88EUuxVib" +
                "iRsmikGFvECURGqKPsBlb0d5mdOxrTFVBF+cwjO0t7aZk1QIfs" +
                "SxxWBVRX9ub8KXcmm852XhS5y9kUJ59y3Eh+8V/UU5RDPqPEkV" +
                "TfQ4mfIhQL/Ce97M4dZA+bJRlZE6siS64J62EtrFDqQwdi1HXE" +
                "xn2YVa4xtx/OT7kzeehs5+VZU2RH0TdfuJEwl+NG8gsFRRX7jB" +
                "IHRQinKvAH9rHszh1kD5vV/QyMrIlVkSXXhPWwFlZYft4uMxdr" +
                "xx5wHbFxH2aVa+z3vfDbZxv47fbSqL4X1j33DOX4D/KvjZk=");
            
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
            final int compressedBytes = 2461;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHNcRHluxseIFC4uQBIt1BBIxjgkYFmSIAzvd0yebXE" +
                "IwLBg7CATegwkmPsS+RDOz8nrHF3uFwCES8sESMrnEIrF1WJBh" +
                "wd5gnEMg+Lx7MGhOQjiJUeSkX1dXf1X13uvt2e3dRduPfj9VX3" +
                "31vTfdM909M9lnWVU6Hbe7mkr6YPpg9tnSFCyE0T2KgsUfA+k2" +
                "xylj4UGrFWVKo8VoRIiZeWxm7vcvytkDH+LXrEBnayj5eK3wlO" +
                "N0Kp3K1obzsBBG9ygKFn8MZLGOUzoWHrRakdbottHzVndIFbLT" +
                "bjNzP19HMXvgQ/yaFejZp2af6nRc7VraaERjrmGzvcEqkGxHT8" +
                "aDHxbpQStzyDEhko+sze9zJPK5ON+vdRav0Qs2r9akV6ZCnpg9" +
                "kbcnqC09JzDmGjbbA4YsuifjiT3p61h40Moccuxr1JqsKqnMz6" +
                "xVdDrg1nltPrkyMo/d0vc7kS3uqUPpcfbHzg5vMZU7mTn9IP2A" +
                "amrZRmNYJAbFjQarloNiLDvb37ym4+HRqnyM1RjXxDipvtPpfW" +
                "Ln6kdIvM8YUisjzSwuRVf9UqPX5tJWolo8NiL5st81iW6G8rf+" +
                "0aJ+qBo/evq3ef394j33pcLyWG38D6Ken3iWR9pZqf7hWu/xsH" +
                "3x3Qj+h57lxxOp+Wn+2l1Jr1BNbfF6XuExLBKDAo/2V0eGiYDV" +
                "spkj6koIYzXGNUEZ1OdHxbydqx8h8T5jSK2M1Js7Hndu697p7N" +
                "G2k5l7R3tHwzayo4ZN+ywG9jB792v2UEQIg0iuucTUa3VaGTJb" +
                "eyy7zlFvKWdxvJe/m7ia2sJznMewSAwKPNrfnS9zHJeMVWYVr3" +
                "0aITNLy+hlP6fm5Eioz9dx3s7Vj5B4nzGktvQc7B301ruwuT05" +
                "R14eMxZWPeJeck4y6Y18zkMRIQwiueYSRll1WhkyW3ssu85Rb6" +
                "HM6fX0enLB1dQmF3jEfarz99LrEkvFWcnCfmqTC4Xu0oMY15cM" +
                "2kNe10p+jKzGuCaykTrmIB0yRscThlDSa2eI+TG68KykK3m7wm" +
                "3xCbTCY9cfvUh2YFDg0f7qs2zFZ0yu6nh4xGegx68toZyakyMJ" +
                "S7vLrOfqR0i8zxhSW3rW0/W8Xee28KzzGBaJQXGj/H5m3fqrHO" +
                "uSUVotm1nHdanIZgRzTBMioT5/V/2NnasfIfE+Y0ht6dlIN/J2" +
                "g9vCs8Fj1y+OR4VBgUf7qxwbkrE8Ez7U8dIn4zS/toRyak6OJC" +
                "ztLrOeqx8h8T5jSC15smso+T3RteLOqBonr2kLYXSPomBxtYvD" +
                "GMjyzkvFwoNWK9IaLUYjQszMQ7PxmaFb6vNnqlVodPYxSj7+uP" +
                "BUY2shjO4RBhZ/DGTx+v1Fx8KDVivSGi1GI0LMzGMzayznH71i" +
                "8+psemXAHrgPfXySK/nB6mRX/snKdu8dtsqw/czxbfbm7E2qqW" +
                "Ubja0FWEbP3hysspUtmgnx5fE457P5mnQOabEYjZBzkXNwu82s" +
                "sawZ+BC/XRlGdme6M/mV/gy3xf3TDI+tBVhGd2cGq2xli2ZCfL" +
                "mOz/ps5s5xxuaQFovRCDkXOQe328way5qBD/HblQG67rlZctA+" +
                "Nzvzxd373Cw52OS52RvPbuW52SbPH9/dT88fl57bueeP9etYWl" +
                "pdx9Ef9mod3/hek3U8c3Vr6xj4BF5SZ8Pl+Cj6yXg5hk0uN2No" +
                "wt9WlD/DNjRiHdMj6ZH8bJhSV+9HGn1DciSGJc5tK9zSFUwsc/" +
                "9iU2TNM+Inu09STS3bXEmPpce0xdm4R2jsskdxhEV8ofAYGGRm" +
                "q0nnkBaL0Qg5F2Sn3WbWWJ69a0l/iF+zgj0dp+M8w5jbYq7jsk" +
                "znZex+lwKMs6DAIzkcquCZJgt8hW1ax8MnjgePX1tG5wM5FSdH" +
                "Elu5T9u5Ur9/Uc6+sE5rbTaL1lp4bqQ38vYGt4XnBo+7n7NFYl" +
                "Dg0f4qR9FfOMU+WKksnAST0ubxa0sop0QgEup5Nj6zjJB4nzGk" +
                "ljzZXDaX3x/OcVvcMc7xuPsPbQGW0dj9HvcXTnFEeUdasS2c1B" +
                "7rByMsFqMRci6MZx43mxCz1qznZPk1q0QH3ner50KLaWcfbd1f" +
                "t4fyvms41DtENbVso3H3U7ZIDAo82q/ZF06xD1YqCyfBZFVpfm" +
                "0J5ZQIREI9z8ZnlhES7zOG1DLavw4fnK/uZz7cT/czoz/v1f1M" +
                "9/n9tI40m51ax7rfU4yu7qf3x9jx2OaW5Rv6tIcxXAMrEXF2yR" +
                "/DaZ5wDsvBmqAKte69uSyVSFapaPRXxtt5Zpmehzkq687rJ+x5" +
                "vfiryc/rM9+NnddnHtjV8/qJJuf14i+3d153z3JNPaPhbLgfvX" +
                "o4O7kHiGYZgJKqN89czx7LbqNjLN1lrqkX8vr9qJrlyT1ANMsA" +
                "lFS9eeZ69lh2G725RnzOdH/UxnsvzmvP80BnF7d2ZhM9u3f5Oe" +
                "4eXvf8vMn7o0PdDc/DW3lCuraVdcxeb7KODrW1z5n0ANr0QHqA" +
                "z2vXd89x2S+xdRa2hT0hq8we80uUfo4bV6B9sb5+jksZ6vlhqV" +
                "Pbe9vv1RwbE/wOoAnfzvGM1nb3ur/3Ttme515r6/hOuwonXMe/" +
                "7fI6Lvu9ltZxuW2FE63jF7u3hoPbuO5JH04ftt9z9X+xSfz/qt" +
                "6/y/bryvLf/s8cazT2G2sZ3pvv5tpo8G2u4pkox786nd9PK0v5" +
                "L4Xhd2zmwX/898fhfc3WaXhPvh8Y3i9tyS20ya3kFq+j60u/7c" +
                "csbPM9/dPMGY5x3phfooaHmimIa49noQz1/LBwr/dW763ktqup" +
                "TW7ziPtLU64mJLBUnJUs7GfGIkfpgc/ZJAN7XM/FOK9rpR8jaB" +
                "ysyGhfE9lIHXOQHhnD/f5F5CKU9NoZYn6MriLL779cm4yTcXU8" +
                "jsmTiO/HkrH32owDr+k47glZZfaYX6KaKohrj2fxM9TNmXuLr+" +
                "b98h3Etcmd5E7/FR47y3A+Ef+DSrz/RCWBf0mRLexJIv+q4uxJ" +
                "7b+uCDX6vJkC7Yv19fUjZajnhwW92t9J/dN7bvb63Xtf2Ox/ms" +
                "NzbT8PTw+nh93x2O4VQXp4+xyjv7eZ2f9dStvPKbpP76vnFE/v" +
                "zfFYvgP8qcVnS6fb4dsaRyyqzRnmr9aXXFMv5PX79WyTeYBolg" +
                "EoqXrzzPXssew2WqOypWwpec/V1Cbv8Yj7VBMSWCrOShb2M2Px" +
                "OpcejiGbZEDfecjrWsmPkdUY10Q2UsccpEPG6HjCEEp67QwxP0" +
                "ZzZP153fY2+qqzR1vs97itbP8H7TnOiA==");
            
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
            final int compressedBytes = 1746;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW11oHFUUHnWVtVUsiD81plFMmzQBC4IQUYSZ2QFBm1Dif0" +
                "N9EH3oo/jmi91s4sbdZ0EUKUiQPrSgiKDBtz4UpPXFSIUW+iSF" +
                "Qn3wr2r82Ttnzp5z7p07mWzu7GzXGWbu3O+c852f3Jm5c5NEra" +
                "jleVErSlrPwx5eB6uAAxasopQsIo0DbDwPdIkRMM4gfdFm8ksE" +
                "mXUW6qElsJGVzJXnhNqqxzX1DPVoyVLfjr3pFbi1fvJK2t65q0" +
                "j2+oPx+Y5uf0zVsX5PnPPPMTKZaX+vVTJtICNuYl5ezYxobzq+" +
                "8pBFf7eBTGypgg9bojzpDeXW/CCX1oe9cNdmajNwhhYx6Ld+QY" +
                "Tr0E4SKZfsJCNUZzOjkvwSSfMpOdGSou9ks0vP1bTg+iZjWrSJ" +
                "ZL223mnXsY0l69iPnkCE69BOEinv+ljnjBzV2bQ6GvwSSfMpOd" +
                "GSosdsTGZuwfVNxrRoQRKNR+MdD+PQJm+gcexHPqCIkC63Qh1A" +
                "5BW3J35uSxJquQ/eN2OUMelR8cgwG5OZ4uTx8UylP2Ll2vb3db" +
                "gv3Od5797GZQrZfAOtNF3g3O62+HUvVjbP9eN5NTOejwdrB+EM" +
                "LWJqD0fDUUQQVQjtJOEcSiuOZhQQkimMM5AvMyrJL5E0n5ITLS" +
                "l6iEfmalqAPtfUM9SjTSRztblOO4dtLJmDPZwMJ2tzjaOAI4ZS" +
                "stA5lFZcs0lASKYwzkC+tDoa/BJp/Wn6lJxoCWxwKM8yV7iuH+" +
                "fZq5Zr6hnq0SaS2dpsp53FNpbMYj84hAjXoZ0kUt71McsZAVOc" +
                "3J7LuJ3kl0iaT8mJlhQ9ZmMycwuubzKmRYva0RX9Xo+uKEwd/l" +
                "mQYh91Vdv8hPc4D2c02cmHbmfGILUllqYF7dt70jn9s7pmGufm" +
                "m7SAujReaczreo2Xup6/czsTbiw44nm2FyvX2YiIOnVsvNA4ZK" +
                "njOePreCMX63O8t/S6rY5LrxZRx8aLljqey8X9vPs69rpFt9vH" +
                "o01WfB3zee4tvqgaVeEMLWLQ989IhHS5hY5IJrLnPnU2PaYsfq" +
                "mja0gMdOFQ2aQxy5hJ386PWlw7a72n+ZX79Z7WP6Wt97zcv/We" +
                "xb/4+qO/07CZNSxOi96/3avfk/bXLrJRfzzzK+UP4w69sXNUNa" +
                "3Oz6H+tJXjN/W+FsjfCVdFz2bxWsoz4eacT5YbOsdNjVvyrT/6" +
                "1cFcf6zf2ct49Kv9XX+U6+HBD25nCC74euOwWbnJMHM8Tg/VeJ" +
                "zOMx4b77kaj81vup6ndFl7Rw9vBavNcrXHOi71NA+fKuv3M/7Y" +
                "UI3Hsf6Ox+CIeeXo2XikTB7X2Wxa34rwft5xLc+XxWGzcp1heh" +
                "2HZ/N9d1q55phvdd8qO4epjs33i+UPK9SGlbBSfwP7IAkrpm4W" +
                "gli6JKzYo1DSsLJZrLqGPQJ77HYvpoesnLOipXXK5vfuf2rtu4" +
                "sed/K7kLby/i4lQZzOe9q7y5r32OpY1Hdh+Bm/Ck45fl+fKpqD" +
                "4s9n5SbDfo/H4ExZ49Hm2c147Hsd17Y9Gtd6rONanjo2f3Rfx/" +
                "b9w/RdaJv3uPku7Pt7Zk9ZdVz5vLj7OjwcHg421BnaYAN7eN25" +
                "H+Iz6dCuUEBQjozxfSQkcM35uUxJQKpa3QftECNg9pgAg+jUGQ" +
                "49Dxkz6IAWl+oZUn6oDZZ9H497h3Pe0/c6Tg12HYt4PkZHXdSx" +
                "/chgvGcgm6w6Nk5udzzybWv/ryB/X5g9Hp2tpHyUKf3Y8kV6X6" +
                "55z9Wt1nGzeXg4EU7o4zGcyDMeQStFdwQ5yxiPNs/m8zF0Wsft" +
                "Ph9TdNXz8dHBf89cD9+F4Xxp43E+1319zf17xv/W/Xgs7z0D2Z" +
                "QxHtszw/Rd2Pq0v+tmrt7Xg7a1R4pkzxyPj/3/xmMR83B/YZjq" +
                "6C+U9V3o7x+qOu4vcjz2+/nYfqC05+NYac/HJ92Px+XXyhqPRa" +
                "7jhvWwDmdo4/lqHfuEcB3aVW/xtM4BNshkMH4h7UnCZsx1HpHu" +
                "kZhtMZElRQ+eZa6mhdA3GNOiTSSXw8txL2nxmvDghDojGpwgKT" +
                "94D2w8D3SJETCFEkISEZvmg3aQI7NkkbmYkemeZU7oW/VQk+pi" +
                "eqFYY8ml8FLcS1q8Jjy4qM6Icik/9CtiIsYkzovIwFm1Omo+uA" +
                "VxmDHxXMzIlBW3wWtkI+/84BwyCq5dO1A74HnqDK2SQA+vgwuA" +
                "kw7tJJFy9EEshAYXpD2XcTvJLxHgsMdElsBGVjJXuCY29C79yg" +
                "z1aLmleP/uwr/v8byVW70h2hZ39NffsS8LnfdEpc17amXNe4pY" +
                "N2s/Nfjrj9fF77meGco6/gcDNHQl");
            
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
            final int compressedBytes = 1935;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW01oHVUUflDQohYFIyouEgtp0ycIdpeN5s28p2Bs1wqCC3" +
                "9A/AMp2fnTlyY17VuIoO6kKJhFl+4K7ar4U0HqOuDGbBvdVkji" +
                "3HfmzHfOufdO5728P8K7w8y99zvnfOe7tzNzJzdprdZ+upaV9s" +
                "O1vLRnz5/Lro/XitJeqJWU9hM+1niva3nG832qNpDSfrTUOh/G" +
                "SVXA/0kPOdmTmmfD+Pm9XlhWb/Y2B52XamMqnRereG081y9/cg" +
                "d1cie54+5H6jvk0kNsl75lCGNhSwiV2WN26bV6vZoCbYu121f8" +
                "DOX8QMrUtj8e6v24PLb78eXR5sM8Jl/0eYeXxHVeH9c8XnxstP" +
                "k2XqA6PZYec8/1YNkd53hKLLN+rvu++4awXidvTeJ6TapGt16v" +
                "FvPWea12iMrlR0aRpfEXX6kVsvrtcrbeLPColgFeUvW9M5ezx7" +
                "LbaOtV9lw3btvnev2PKs91bsmf67V3Ys/12psj/Q6/XeW5vvhK" +
                "f8916TwuDGIe1x+IzeP60ZHO40KVeVz/aVDvx/Z/+fo2m87a9d" +
                "ohFVbG2ZgvcR606O/wyuv1bNX1+uAaG59l3z2vTvZ64TSOMq6v" +
                "XJ9k8zjhq7TTOMq4HrN8xVdqhax+u5ytNws8qmWAl1R978zl7L" +
                "HsNtp6la0zzZ/7+w7X68ykfIfTaO61zlz4pp91ZuON7Ds/3ydz" +
                "dbKX7PHP167d3e8R+2iJt6eWBHbZCAtbksiuHGdPSnftyMvs90" +
                "QVaFusbfZ79ixj2ZgV5w7qZCfZKfbNdsjCdtuOIYyFLSFUZo/Z" +
                "pVdVBXHt8Sx+hrIxl6nleUxn0pkh7FPMjG2fYmb4+xSheaz4LX" +
                "ez+vux+zP7BwfXfOG7UusPYbzzfqX34/e9vh+j+2bvDmYeI6P5" +
                "aFz3Y+fDEX+fXR8/w/jG1Z/25kJzga5UM+aO9FR6qrng3o/wcQ" +
                "gOWCSH8+q+j04RApvDJANy+ao0v0ZWr/s5NSdHEhudLrMeK7Xb" +
                "V+ToXS097QitWhlp3hf/5u/luXTOe1fPVXqjz8V8Q5wjW2fmBu" +
                "9ZVrDONP6sHaIy3NGU7pudPEw/zzROVlmvu8iA9x9z5JDM48bK" +
                "MOcx+ybfR53sJ/vFzzP7ZGG7bccQxsKWECqzx+zSq6qCuPZ4Fj" +
                "9D2Zi51VhprNCVasao3/xVI/Blb5x+i9vgR07NZp6/FZtDItZH" +
                "e8ixIDudbjQhZq1Zj8nya1bpXbrfc+tQ7ffcqvJcr/04qN8rpH" +
                "t+q7eSvD2J6/VwVY16nWkeGdv9eGR460x6N73r3Y9dzJ2t58nK" +
                "ffaltuxJOxhD7LAwS1wDGMN+WgNOrYwLj0aOI64QOcqRPONuup" +
                "vVu1x3Lbv5Uc+OHCnQemEtIiyH8+pG1QmBrYvVdTxsQpvHr5Fg" +
                "TsXJkVBPevRY/Yicu6612SxaqztbZ1pnsn+pM1RnK8/n3KM+X4" +
                "HZFnwI0S0ZX9wXKhYW1DKH7PsatSarSipzcb5d65T67DxYFVJ1" +
                "hp1tnc3qs1RnuT7lHvX5Csy24EOIbsn4QqeKhQW1zCH7vkatya" +
                "qSylycb9c6pT47D1aFVJ1hy63lrF6mOrcsc7/5C6GMwFdGsQ8h" +
                "uiXjwS9jYUEtc8i+r1FrsqqkMh6NzwydUp8cqc4HVninW+lW9n" +
                "xvcd194re43/yNEemDAxZtL94dW5JRopbNvB89fo2EcmpOjoR6" +
                "Ho3PLCOkv88YUkuWpeNLx2s1d3U1Feq5fvN3QqUPrPK0LTCBEa" +
                "hlsyuizYFDM1tNyBZSRqPxmWWE9IfF6tBa3dmqt7K11V1b9eK+" +
                "rnO/lRDKCHxlFPsQolsyHvwyFhbUMofs+xq1JqtKKuPR+MzQKf" +
                "XJkep8YJXe3Tmdd0cxw/N0qlmfJy9cgaPWHDLa8pcV9rU5woys" +
                "iVUB1x4xdonp8etx4ihRdcId6NGpPcgLV+CoNUcZf+k8ntBsIX" +
                "8wsiZWBVx7xNglpsevx4kjpKp1C4frSYza2ktaZRQQvw9Pzilj" +
                "YUGtFWmNYd0hVXIMsmWZpW7pH+K3M8OeS4tLi9ncLlKdz/Qi+n" +
                "wFZlvwIUS3ZDz4ZSwsqGUO2fc1ak1WlVTmZ9YqxP1o8tp8cmYk" +
                "u/dz99/FDvJqbVqq/WbsanqVrlQzRn0g0gcHLNoeYgeu46VFxv" +
                "k+VmNcE5RBvT8Oqxn6pNWO0KqVkWYUm9FZ36z0b7PZT9QA743N" +
                "ybhHNzaK1qXpE3uAebxctDrT2aj4DNxIb9CVasaoD0T64IBF2z" +
                "W7ZpQM1iLjNL9GQjk1J0dCvT8Oqxn6dF6dzarNLdvpdlZvc921" +
                "bHMfiPTBAYu2Fzm2JaNELZuZR49fI6GcmpMjod4fh9XMKmxenc" +
                "2qJUvrGo7se/Ja98uy6FuEfHSLfID4fXjmX64qFhbUWpHWaH20" +
                "R4hZjsxXrXVL/xC/nRn2bD8Y+H68v6/fPR3g73bb9/UZd3Si15" +
                "kvp+vGIMraP9M5GMj9+PV0DgYyj99O56DX0jjdOE1XqhmjvkXg" +
                "y944/Ra3wY+cms3XpHNIxPpoDzkWOYbQOLQv++gxWX47M/Du7e" +
                "9S+vn/1wIZ699JRf0H8f/Y/weBXb5q");
            
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
            final int compressedBytes = 1784;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW8+L3VQUjiJCodgp/tbFII4oKrN24cJmEhVcSRWVPimojP" +
                "MHCF1nJoQ+KbN0o/CK4NKVO+lCup69Ct2JijBCWxUXQjHJzXnn" +
                "fN+57zl586atbW7I/fGd737nnDtJ5ibTJsn2U0ldtk8kXdleLT" +
                "6p60eTadl+LplTth+baXnBIU8mSynbD861PtNT7XE7qr6pkWd7" +
                "zV9Pko39jf1Qh7bBw0j6UitHD7WgXTyoCqOshpF5fURiPlFTZm" +
                "r0Pg+OWaJgv+iNo7UzsVQPJ0PpWfIz+ZlQh1awMGZEucLW0/ek" +
                "r/rqE9V8TOjDIsxBhs3F5hDLA7nCwZxYn1dmyhzlo7odSdtaRj" +
                "JmRLnC1tP3pK/6nWenRus4Yh8WYQ4ybC42h1geyBUO5sT6vDLC" +
                "zE5mJ5OkqUPbWMJI+lIrRw+1oF2yVRVGWQ1X0usjEvOJmjJTo/" +
                "d5cMwSBftFbxxtZ9nL9up2T9rWsidjRSxHD7WgfepjzypalNVo" +
                "HZ0+IjGfqCkzNXqfB8csUbBf9MbRdpatbKtut6RtLVsyVsRy9F" +
                "AL2qc+tqyiRVmN1tHpIxLziZoyU6P3eXDMEgX7RW8cbWfZyXbq" +
                "dkfa1rIjY0UsRw+1oH3qY8cqWpTVaB2dPiIxn6gpMzV6nwfHLF" +
                "GwX/TG0dqZWIobwz6m975nkk9C3bSKyVhrwdRqT+5Zda/ParGo" +
                "rA894nGjHaPzPVbWOG18ykBvHG+wpMfT40nS1KFtLGEkfYsoV9" +
                "h6+p70VT8Ur4ariHY7jmkgw+Zic4jlgVzhYE6szyuj7OH9Gt+v" +
                "y88Web+OlfG3w/NugZ+tux7Hl+7m67FFFrgeb/Z9Pf7uzryvh+" +
                "fjUT0fm3UcyuGfj3f39bjY8zHbzXZDHdr2TWdXxopYjh5qQfv0" +
                "nWnXKlqU1ei90OkjEvOJmjJTo/d5cMwSBftFbxytnUn7nktHdu" +
                "0Xy9EpV4b7evg9c/tcj7dfSS+nl0MdWsHCWBHL0UMtaEd1tSnK" +
                "aj4q1Eck5hM1ZaZG7/PgmCUK9oveOFo7c9j3LK+c+l7q0ItZfX" +
                "++Wj+LMg7mQVk26v/2PF99lneejaz8Qn4h1KEVLIwVsRw91IJ2" +
                "VFeboqxG3x+dPiIxn6gpMzV6nwfHLFGwX/TG0dqZw3192FJ90W" +
                "/fU30+vM/E9j3jH/ut4/iHYR0PvH+c83eu6svhHo6V7GJ2MdSh" +
                "FSyMFbEcPdSCdlRXm6Ks5qNCfURiPlFTZmr0Pg+OWaJgv+iNox" +
                "X2//G9sPrq9ruvh3U8qudj9fXwvFvKT3tYx94lP5efC3VoBQtj" +
                "RpQrbD19T/qqrz5RzceEPizCHGTYXGwOsTyQKxzMifV5ZYSZrq" +
                "VrSdLUoW2/YKzJmBHlCltP35O+6ndfSJwafe1ZYx8WYQ4ybC42" +
                "h1geyBUO5sT6vDLCzDazzfr39qa07W/yTRkrYjl6qAXt0z3Bpl" +
                "W0KKvRvsfpIxLziZoyU6P3eXDMEgX7RW8cbbDkZV7W12UpbXul" +
                "ljJmRLnC1tP3pK/63Z3g1Oi+LtmHRZiDDJuLzSGWB3KFgzmxPq" +
                "+MMNOVdKW+Llekba/UFRkzolxh6+l70lf97k5wanRfr7APizAH" +
                "GTYXm0MsD+QKB3NifV4ZYW5c2biSJE0d2sYSRtKXWjl6qAXtkq" +
                "2qMMpquJJeH5GYT9SUmRq9z4NjlijYL3rjaIWdu/9Dk+83WMC1" +
                "VgxtPELFfH/GPmE/eJnFEKtlIxZjzfea5WyPaR5gj7PPo+Ys7v" +
                "PfH4sH6rr9plI81NaP1Gfzb3Oud4z2jWF8tRs939YvTr8JXTNa" +
                "L834FvJq8XrxWtt7I2ofFe+b0QfFx8VWcU9xL7FOFOaZUDwx7a" +
                "3OX4nxHx3v5eKVIu27jgX8f5/iw/r8qO974fjvw74XnvrzVn03" +
                "m+X57n2/Psrvj+N/bsY6jm8M33GH6/Eg67jY/xuOlfRp3+tXFp" +
                "13tOUoo0pX09VQh1awMGZEucLW0/ekr/rqE9V8TOjDIsxBhs3F" +
                "5hDLA7nCwZxYn1dGmOXZ8nT5TvmmzaN879BfkWb8e7ByNNvWp5" +
                "RvzbW+2y+qRVhu1rH8WKhDK1gYM6JcO4MRVNL51iercUzz9JHD" +
                "DMQkh1geNm/lKH+2vq6MMNP1tH5KNnVo2yt1XcaMKFfYevqe9F" +
                "W/uxOcGt3X6+zDIsxBhs3F5hDLA7nCwZxYn1dGmNkkm9RvTBNp" +
                "2/eniYwVsRw91IL26ZvYxCpalNXoDc7pIxLziZoyU6P3eXDMEg" +
                "X7RW8crbD77XvOv32n73vOn15k31P9Vf1c/VRdt+tY/dpn/zjn" +
                "7zzuPaz6bUl/QfplrvXqwrq/d+21peyFzvreknZZZ2+lzrKzgZ" +
                "/AHXg9fnr/zb8eh3Vc0n39L79BjBI=");
            
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
            final int compressedBytes = 1646;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXLFuHFUU3RIBEgUFRBSIAoGC4jSpaKLMziLHSCBhGf+CJR" +
                "eREB9gomijRKlpqCkp6P0VlstILvIDbiJa1vP2zLnn3De2116C" +
                "o8yM5r37zj3v3Hvfvp1ZjZVMJvmYb03+o+P5+5M3cMwfTm7E8f" +
                "iXyXiseExfTF+UtvTAyphI5PCkR/01deI6P3rivMzxHIdzYmbM" +
                "PtfhOTO/6PUKPds4U4/ffh3311r26B+re85jXW7WZeNdrHb1eF" +
                "c/mtvN7dKWHlgZO0Iu2LyyBZv6jKlqOSeNERHnKCPWEmuo1aFc" +
                "cLQm1/eVIfvxF92z5aP+KfP52ff68SfhufP1uU+lTwc93yTksz" +
                "U9CT8+1/vlimq3EvLVSvM3Fiu53WyXtvTdCm9j7Ai5YPPKFmzq" +
                "Lz/BpGb7cdtjRMQ5yoi1xBpqdSgXHK3J9X1lyB734zr247iOvo" +
                "5Pfh/X8brr+OSvq+7H6d50r7Sl73437GFMJHJ40qP+/heIzSDq" +
                "avbLZa/G8RyHc2JmzD7X4Tkzv+j1Cj3bpWc+nS/6OfrOM8eYSO" +
                "TwpEf9fYx5VowK7onzVF+RWkzVxExmn+vwnJmfxtVoni3Y4/d6" +
                "fM68y+s4/fn/WsehyON+vEn7cXzfs6bPdtyP4/d6/F6P3+txP4" +
                "778Y0e4zqufrR32julLT2wMiYSOTzpUb+q00fU1XJWqq9ILaZq" +
                "Yiazz3V4zsjC42o0z3bp2WgX3+6ztvSdZwNjIpHDkx719zE2om" +
                "JEXc3WMekrUoupmpjJ7HMdnjOy8LgazbMFe3zO/Fe/H59/O67j" +
                "Op7X07+HZgx7LsO63OzrHxfHWX8mza3mVmlLD6yMHSEXbF7Zgk" +
                "19xlS1nJPGiIhzlBFriTXU6lAuOFqT6/vKRPXxd88a9uP95n5p" +
                "Sw+sjB0hF2xe2YJNfcZUtZyTxoiIc5QRa4k11OpQLjhak+v7yp" +
                "C92vP62T/jc6b2nJk9mj0qbenP8DKCHRFyweaVLdjUL0dW07zU" +
                "H8c1DWXEWmINtTqUC47W5Pq+MlF9vD9e/3j+Xcaebr/pLJ7+9L" +
                "av4+zu7G5pSw+sjB0hF2xe2YJNfcZUtZyTxoiIc5QRa4k11OpQ" +
                "Ljhak+v7ykT1tEcfjt/TlZ98H1TW8Yfe+nEFpQ+vcXf5/orZv3" +
                "eT13a8P17h/nhvdq+0pQdWxo6QCzavbMGmPmOqWs5JY0TEOcqI" +
                "tcQaanUoFxytyfV9Zch+G9+bzf+8ab/D25P2pLSl795MnmBMJH" +
                "J40qP+/h3nSVSMqKvZe9ykr0gtpmpiJrPPdXjOyMLjajTPtnhm" +
                "h7PDxb48RN/t1EOMiUROZMdZ0Vru+cOoSNTV0t3GYvBUZc+J0Y" +
                "Yzy8rMM+ZHhs70fJee09npoj9F33lOMSYSOTzpUX8f4zQqRtTV" +
                "bB2TviK1mKqJmcw+1+E5IwuPq9E826XnYHaw6A/Qd54DjB0hF2" +
                "xe2YJN/WXkpGareOAxIuIcZcRaYg21OpQLjtbk+r4yYDY7zc5k" +
                "ctaWvnsTtIOxI+SCzStbsKm/fNOU1Oy92Y7HiIhzlBFriTXU6l" +
                "AuOFqT6/vKgNket8eL++Qx+u7OeYwxkcjhSY/6+3vwcVSMqKvZ" +
                "cybpK1KLqZqYyexzHZ4zsvC4Gs2zBXv8e+E6/t1He9Qelbb03Q" +
                "ofYUwkcnjSo/7+szqKihF1NduPSV+RWkzVxExmn+vwnJGFx9Vo" +
                "nm3xTF9PX6e/pnVYwdkSgx1H0U/Fmjo9UBnOgYp1nubASzOLfO" +
                "+HM/Q5Q0iJ2O63+4v13EffrfA+xkQihyc96u8/q/2oGFFXs/2Y" +
                "9BWpxVRNzGT2uQ7PGVl4XI3m2RZPs9lsLp43m+i7J9Amxo6QCz" +
                "avbMGm/vIJl9Tseb3pMSLiHGXEWmINtTqUC47W5Pq+MlE93Tf7" +
                "/5+ieXbFv0I+m7xjR9M2bWlLD6yMHSEXbF7Zgk19xlS1nJPGiI" +
                "hzlBFriTXU6lAuOFqT6/vK9MzdZnfR76LvPLsYO0Iu2LyyBZv6" +
                "y8hJzdZx12NExDnKiLXEGmp1KBccrcn1fWV65laztei30HeeLY" +
                "wdIRdsXtmCTf1l5KRm67jlMSLiHGXEWmINtTqUC47W5Pq+MlG9" +
                "HA9eoS0WMbXUHjqG+RcrPXh1UYSSpWaH0fDcixnnRffZQyoPXq" +
                "ItVs2b7cFsXq7uIeNyEciKWV8c+Xz1oeg+21j/AtbKios=");
            
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
            final int compressedBytes = 1372;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW02LI1UULX+BCxcqNiMuZARhFtI/YDpdWc9CzK57Yf6B9B" +
                "+YGm3bbAU/NsFeuBQEN8K4E3fDMBAYZiMMDPg7TOrm5txz3qt0" +
                "0lT3xPhekffqnXveuee+VHUlYWbyefNBNW/Nm9WyNe8/PJv3b1" +
                "er1nxUrWnNO52RjxPkvaqX1ry1NvrhlmrvJsjdrdbfi7Ojv723" +
                "M24Ry8XX8TeNgLFZBrCi66szr1fvyq6rlbXd9Xj+dN+vx/Pfrn" +
                "M9HjVHjfU2tjvd+FwRcJ2NV3rm59BfvpOJmrzzjeaIiHKYEWuJ" +
                "NeTqYK5zuCbV152J6toevVGV1kP7+mnZg7KPu9MWz5mbac3Dfd" +
                "2zwcvBS+ttdMzmQCIHByIcZ3XEgKpa6or1GcnlZE1fCfdpHerZ" +
                "XWhezqZul5HZYDYfZz62kZnPgUQODkQ4vsoxi4oRVTXZx0SfkV" +
                "xO1vSVcJ/WoZ7dheblbOo2rryt+3p/W/2ifmG9jY7ZHEjk4ECE" +
                "46yOGFBVS12xPiO5nKzpK+E+rUM9uwvNy9nUrbPL9+s+vl8PDg" +
                "eH1tvY3vGHPlcEXGfjlZ75OfSXf1ESNfnreKg5IqIcZsRaYg25" +
                "OpjrHK5J9XVnnFmP6tH8uhz52F6pI58DiRwciHB8dc2PomJEVU" +
                "3u60SfkVxO1vSVcJ/WoZ7dheblbOrW2eW+7uO+rk/rU+ttbHf4" +
                "1OdAIgcHIhxfvVenUTGiqibXY6LPSC4na/pKuE/rUM/uQvNyNn" +
                "W7jIzr8Xwc+9hGxj4HEjk4EOH4Ksc4KkZU1WQfE31GcjlZ01fC" +
                "fVqHenYXmpezqVuLDB8MH1TVol+M1mxmc++B6Rk4hvBZXA/9uB" +
                "YRjDFHnKce2ZO6is7SzOwCO6N5NV/cmaie3O+v/GzyVfmEvU27" +
                "f3dxYGYvZhgLPXCMrLFO/yo3qXaXY/fkroAzo0s9Ylw/14ljsy" +
                "qq6vin7SPrWJut2jTf1WrXz9fzr1u4ry/KvbqLvz+WfSztit97" +
                "7tR38pjh6IFxTDnA8+qI2Io8x1Hv/ehyz+7YWeTr2O1Q13Qhyy" +
                "oO6oOE12KGowfGMeUAd6WsuwPPkuc46r0feZa6Y2eRr2O3Q13T" +
                "hcTM3Cbflvu0jzb5YU/r+n4j1o995Xs0KddSL+/btOzBNT51l9" +
                "8fe/l3e2Uf+9jH4cnwxHob21+CTnyuCLjOxis983PoL39pStTY" +
                "F8fjPKfBjFhLrCFXB3OdwzWpvu7Mink5vJyPlzYuI5eYo3cM0f" +
                "jSMyhBEaiq6TusOXCwsnpCtm5nqTJ8Rn9gcDb1C/XyvfCGnte/" +
                "lj246ef1xWf7/py5+PQ2ntdlH8vnns328cvv/hv7OPl9P/exPK" +
                "/L98JyPf7fr8fJn6//ehw8vt71OHi80fP6n9t4zkz+Kvf167+v" +
                "9/f/fQyfDJ9Yb6NjNlcE3LhCEVbC+phT1dTTOn3mKIMxryFXR6" +
                "wbHPC79bEzYKf39Td/7Prz+vznXfv9sexj3/+PPbj8pXyK2frv" +
                "49nwzHobHbO5IuA6G6/0zM+hj5yslnriHBFRDjNiLbGGXB3MdQ" +
                "7XpPq6M86sn9fPq2rR27iI2MzPvQcHByIc92qhoqiq8U6m+ozk" +
                "crKmr4T7tA717C40L2dTtxY5fnb8TK9RwwxHD4xj4LsWFHPqiP" +
                "CqnAdk8iOqR312xz4jX8duh7qmC7HM9bSezvdz6mO7w1OfA4kc" +
                "HIhwfPVeTaNiRFVNrsdEn5FcTtb0lXCf1qGe3YXm5WzqNq4s36" +
                "/L7z279Lnn/ieLAzN7xWZY7IEzK4+r/rrm3HwOVXRP7go4M7rU" +
                "I8b1c504Nqtivr9flLt06/Yvg4wqEA==");
            
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
            final int compressedBytes = 1331;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWsFuI0UUzCkggcQBJEAoijggkFhxQPIFCSnOjC+wEhdfkC" +
                "xFPu0P8ANZs3DYO/fwC5z3F5B2f4E/cA57Q9jTrqlX1T1Z23g3" +
                "m6in5e7peu/Vq9eZGc+M07xsXh7Z1nRYwtkTw36cRTsZmwI7LW" +
                "BpBjWQseynGvhRZdHfx2GFHjOExHUxv7/yvbu03Y7qx593/Qf9" +
                "/PTyl1X/cfD46sb4TwYtX2fIZwfS/OGN1i92ZPs0Q77cKf6buo" +
                "6HWsfmorlIfRq78+ICcyLRh40WtfdnmEUQdTY7My9KPq5xWBOV" +
                "UX1eh2umvmj1Cl3txrJoFqtxgbGzLDAnEn3YaFF7n2ORM0YGt8" +
                "Q45VeklFM5EUn1eR2umfo0r2ZztfB+0+d18/NtnddDmQ9zfcy3" +
                "9TrWrX5f1+/ru7uOk+eT56lP4xpPM+yjpw8bLWpHBrI46myqLO" +
                "dXpJRTORFJ9XkdrhkqPK9mc7Uxsl4f6/WxXh/v0zq2D9oHqU/j" +
                "Gk8z7KOnDxstakcGsjjqbKos51eklFM5EUn1eR2uGSo8r2ZztR" +
                "vLtJ2uxinGzjLFnEj0YaNF7X2OaWSMqLPZOmb8ipRyKiciqT6v" +
                "wzVDhefVbK52Y5m1s9U4w9hZZpgTiT5stKi9zzGLjBF1NlvHjF" +
                "+RUk7lRCTV53W4ZqjwvJrN1W4s83a+GucYO8sccyLRh40Wtfc5" +
                "5pExos5m65jxK1LKqZyIpPq8DtcMFZ5Xs7naZJmMJqPV/c8IY3" +
                "dHNMLcEfrCm598D/vk39xxZWx29zjyHBFxH/WItcQaSnWoL3y0" +
                "Juf3lYnsvj39qN4N7rpNnk2epT6NwNKcSPSJ3jEq7kV2xgJ1tp" +
                "KqmINtSLdqvklZzkydUR89NNL1kj17nvm3Hl+7bufH58epTyOw" +
                "NHeEvvDmJ9/DPvmZU9lyTZojIu6jHrGWWEOpDvWFj9bk/L4ykb" +
                "0ej/X5+vDPhb/+Ud+H353j8be/7/vx+PvDN/G+58nD+76OT36s" +
                "783uzvvHel6X13F8Ob5MfRrXeJphPyL0hTc/+R72yZ+2nE11qT" +
                "3OSxzqEWuJNZTqUF/4aE3O7ytD73peH+T3wuvJderT2D0xXmNO" +
                "JPqw0aL2/tnzOjJG1Nns+TrjV6SUUzkRSfV5Ha4ZKjyvZnO1yd" +
                "KetWdHR+s+jd0btTPMiUQfNlrU3r+bO4uMEXU2e/+Y8StSyqmc" +
                "iKT6vA7XDBWeV7O52o1l3I5X4xhjZxljTiT6sNGi9j7HODJG1N" +
                "lsHTN+RUo5lRORVJ/X4ZqhwvNqNlebLJPlZLk6LpcYuyN1iTmR" +
                "6MNGi9r7Y34ZGSPqbHZeZ/yKlHIqJyKpPq/DNUOF59VsrjZG2n" +
                "Xzn/6N7rf1eW+7rXnRvChjCWdPTG30BxcZS+y0aFRZF3q0yB75" +
                "VZ3qjP4+Div0mCHENQXL1SDn1VZ/m6t9orbN92q2/fMd+Bj9c3" +
                "fLTV7bRW2b79Vs++c77Fbfm9V1vK2tPW1Py1jC2RNTm/sQL7PT" +
                "kiLKPkDRow2pV3WqLPr7OKzQY4aQTRUn7Unm12EJZ09Mbe5DHE" +
                "xFdSfIUvYBih6t7OXqVFn093FYoccMISnz4/cK94/v7PXu4P3/" +
                "8d7heM+4d9/mc/3pd/V6V9fxLVrH7+saHGQdz+sa7HGtrr/PHO" +
                "R318Lx+EM9una+D3/UPkp9GoGlOZHow0aL2pWdNqLOlqtSfkVK" +
                "OZUTkVSf1+GaocLzajZXGyPrc+Fr+Z75qa7B6/6eKcTX313r/6" +
                "UccB3r9bGe1/V4vHfbfzrxwak=");
            
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
            final int compressedBytes = 1217;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7FuVEcUfaUlIqWggAgpiCJKJEvIFT1+uwVCokhau6SDDr" +
                "lJk6xQFnmLSCki5IKGmhokKn4gUow7Gqo0afmA2Ht999xzZp69" +
                "b/0S2+TO087MO/fMuWeG2X3DwjbN5FZzWCZfNsdlcvPnJ4f1tW" +
                "ZRJt+h/+zHRsrketNRJusFcqMZpEyunhj9pqfaV/Hu6e+HyLe9" +
                "xt+u40frmOXs5ZdXuQZ9y3hnvGO1tY7ZvSLgOhuvsud96CMnq5" +
                "WeOEdElMOMOJc4h9o8mOscnpPq68o4c/Rw9LBpjmprjyJ2532v" +
                "wcGFCMd9tlBRVNV4JUt9Rmo5WdNHwn05D/XsLjQvZ1O3zu73nK" +
                "l8yn5mz5k5ssJzJtdxmHWsfGb+fbk/83/6+jyy9tuPs8e5H/P8" +
                "+O+V9lH7yGprHbN7IJGDCxGO19SB8/gYieNKjnrs9gRncF/OQz" +
                "3DX4zqDNXtcWTaTg/bqbfzyNTvgUQOLkQ4vsgxLRWjgkbiONZn" +
                "pJaTNX0k3JfzUM/wx3k5m7p1dj6v89xzcdZxvD/et9ra+bln3+" +
                "+BRA4uRDi+OEHtR8WIqpqcvAp9Rmo5WdNHwn05D/XsLjQvZ1O3" +
                "zs79mO/ri/T9Y64j1vHpqzyHn3fJ/TjE52O71W5Zbe38ZLnl90" +
                "AiBxciHF+cUWUEUFWTc/hWjaMeuz3BGdyX81DP8BejOkN16+zc" +
                "j/m8zuf15/e8znUc4n09WhutKW6Y4aiBcUw5wOvqiNiIOsdRr/" +
                "2qs9QdO4t8bbsd6pguJGaWp+Ve57eVe0t9p7m3yqhl852utnq+" +
                "M3yP+6n9VMcMRw3M+/EuxqFYU0fEVbo9QLHOYw94sbPI17bboY" +
                "7pQizj7Ndy5PSH//pPc/r9Zf/bzHh9vG61tY7ZvSLgOhuvsud9" +
                "6CMnq5WeOEdElMOMOJc4h9o8mOscnpPq68o4c3KlXNvdB96b/d" +
                "bjqfXF6n+au/dXfG6vXZj9uDHesNpax+xeEXCdjVfZ8z70kZPV" +
                "Sk+cIyLKYUacS5xDbR7MdQ7PSfV1ZZx58n7stTf+3/vxzviO1d" +
                "Y6ZveKgOtsvMqe96GPnKxWeuIcEVEOM+Jc4hxq82Cuc3hOqq8r" +
                "E9WL3XEvv03sW0YfRx+tttYxuwcSObgQ4TirIwZU1UpXrM9ILS" +
                "dr+ki4L+ehnt2F5uVs6jaO5DJ7nvtriJL/rnCWcveD19arRcv+" +
                "yWr9ImAslwGs6Pr0zCerd2XX0ad7nP6Ruyrf17mOl7vk9+GDfB" +
                "++Pdq22tr5iWjb74FEDi5EOL44W21HxYiqmpwfC31GajlZ00fC" +
                "fTkP9ewuNC9nU7cWaQ/ag+J7yjlmOGpgHAPftaBYU0eER9U8IJ" +
                "NfUT3qszv2GfnadjvUMV3I8Szet+8L3hwzHDUwjoHvWlCsqSPC" +
                "o2oekMmvqB712R37jHxtux3qmC5EPYXIy07Nl8t8XihruVHL5j" +
                "tdbfV8w5bZ63z6DrKOb3IN8txzcX6HtPsid9cQJX/Hnu/rs7+v" +
                "8/+bnec6br7bfGe1tUe43Xnfa3BwIcJxzwAVRVWNnZX6jNRysq" +
                "aPhPtyHurZXWhezqZund3zd8N/nv9+3Hy72n7cfLvMfnz2V/5e" +
                "IZ8zl//8mOs4yDr+A73rKuQ=");
            
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
            final int compressedBytes = 1558;
            final int uncompressedBytes = 19353;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrFqXEcUXUjAgQRSpEhCwCFFSCAmRCqTSnp+VSBNSGXWCM" +
                "EW0paOemdZBeO4Tm1Q7V/Yf3Dhxo0+QL3rvH3z7jv3nDsja5U1" +
                "ss28x86duefMueeOVtrVSpNJvP5+NqnXhlf7oH2QxhQtl9aaAd" +
                "fYeMSZzaGPmqwWPXENn1EOM3wvvodcH8w1Dvek+noyYC++Wa8W" +
                "n5rDxdd//dmNn6OvxfeYP/pDvxKLL0pfo8UPIfPVdr76i88uRb" +
                "/dUO1Lv3r0e5f5bqP9P/bjRueYUXnPzrHP1HOs51jPsZ5jPcd6" +
                "jm/oHJf/1nP8v+e4fHbd52Nzv7mfxhTX+bSyuY3g4AbCuFXQHc" +
                "iqGjvTGloxV5P55gzuYx/qGf48qh2qW7+Tr/XzsV4bf4/U7+v6" +
                "OlNfZ96315mj5iiNKfY/OY9sjYzn4AbC+PgzWHYgq2ryOnOU46" +
                "jHsic4g/vYh3qGP49qh+p2QKbNtItTiz0ytTUynoMbCONjDdmB" +
                "rKrJOU5zHPVY9gRncB/7UM/w51HtUN0OyKvmlT5PUy7lMSJnc7" +
                "/yOBRz6kBMpewBinkee8CDnXm+xrJD3VPKDBWfN88Dr8+lPEbk" +
                "GAPftKCYUwfCu3IeUMlur+712R379HyNZYe6p5RJle/eu3tvMl" +
                "mPKa6RtLK5jeDgBsK41YCKZlWNnUZ9zuRqsqbthPvYh3o2F1qX" +
                "q6nbhLQ77c5ksh5T7D8p37G1ZsA1Nh5xZnPoD5/EBzX5u8KO1v" +
                "AZ5TDD9+J7yPXBXONwT6qvJ2PM/Vv7tyaT9ZjiGkkrm/sMuMbG" +
                "I85sDv10RTU+R8b9OqfBDN+L7yHXB3ONwz2pvp4M2PX943beh9" +
                "u1d25jmvHlczn8Mv5VETCuVgEs7/r1lS9XL1XX3SWVvZc2plkO" +
                "jfOim5ebI2BcrQJY3vXrK1+uXqquu5m1f75/nsYULZfWyHgObi" +
                "CMszowZFVNfkIGfc7karKm7YT72Id6Nhdal6upW7+zfm72Jq56" +
                "jtu59lY3r3BzfV3Pe/OieZHPpTxG5BgD37SgmFMHwrvyvmy026" +
                "t7fXbHPj1fY9mh7ill1BOuf36u35PbuOo5bud68kE9g62c44f1" +
                "DLZx1f/H3fxqn7ZP07iOyNkao+WA+ofOvHrUV7WcK18Dd9434+" +
                "wuzlQZPr0/MLia+h2Qs/asi2cpDsgZ1hgtB9Q/dAYlKCKrauEc" +
                "pQZuVlZPqFZ2FpXh0/sDg6upX2PWz83q/wHUc3x7z7H+P0V9Pr" +
                "77z8f2pD1JY4r9K9CJrTUDrrHxiDObQ394hQtq8mp9ojV8RjnM" +
                "8L34HnJ9MNc43JPq68k49kV413GxzqU8RuQY0xUrRnXU0H3RA7" +
                "M5l2Ol+PB2qaoyc5pXeM99oatSD+/27xYPb99E1frzcRuvM81B" +
                "c5DGFPtPJg9sjYzn4AbC+PgZp+xAVtXYmdbQirmazDdncB/7UM" +
                "/w51HtUN0OyLyZd3FusUfmtkbGc3ADYXysITuQVTU5x3mOox7L" +
                "nuAM7mMf6hn+PKodqtsBmTWzLs4s9sjM1sh4Dm4gjI81ZAeyqi" +
                "bnOMtx1GPZE5zBfexDPcOfR7VDdTsgp81pF08t9siprZHxHNxA" +
                "GB9rnEZFr6CI38f6nMnVZE3bCfexD/UMf1yXq6nbATlujrt4bL" +
                "FHjm2NjOfgBsL4WEN2IKtqco7HOY56LHuCM7iPfahn+POodqhu" +
                "B+SwOeziocUeObQ1Mp6DGwjjYw3ZgayqyTke5jjqsewJzuA+9q" +
                "Ge4c+j2qG6HZBls+zi0mKPLG2NjOfgBsL4WGMZFb2CIn4f63Mm" +
                "V5M1bSfcxz7UM/xxXa6mbhOy+Di+H3r82/iXmp82eBf1yfXfDz" +
                "7+9ZrvIz96a/6ucKe9k8YULZfWmgHX2HjEmc2hj5qsFj1xDZ9R" +
                "DjN8L76HXB/MNQ73pPp6MiNzt93t4q7FHtm1tWbANTYecWZz6A" +
                "+Vg5qc467W8BnlMMP34nvI9cFc43BPqq8nMzJX7aqLK4s9srI1" +
                "Mp7j2X6Xnw01Vl4RWVUL3yVSAzcrqydUKzuLyvDp/YHBO9WvMe" +
                "vv19v9v2ZcT36pf4/e+PoP3O9Umg==");
            
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
            final int rows = 49;
            final int cols = 82;
            final int compressedBytes = 1427;
            final int uncompressedBytes = 16073;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmD1uI0cQRnUDBw5sw4GxgWEDhnQCR56dvYcgWYpWEimFTn" +
                "YjEdhTLPR3C13Nw+mueVVf9SwggMEGzQa7puurrnrVIikOj47y" +
                "48vfR/3xxsf4dfxa5r3FZ2tm86H6p1757Dm/ZmtR+RqMNnfUI1" +
                "2+0sxwej4iYjXlJXt6Pf7TX1+HeHy662fw9sfnd/P8w7L+bX+O" +
                "n39yEX9+c//Pq8pfyfPrgZh//Kb6+xuz/ZI8f7xp/0k/x0Od47" +
                "gdt2Uudv7k3NpaPcRaNM98Zdfkr5/MKZv8l9lqDe/RmBjhe/E9" +
                "tPqIsRYTe9L8ejIW+f7D+w9HR/u52L1SVnZtMzEMlKhbt2RRr2" +
                "aLJ5nzR0+rZsxpO6HPfSizUWjdWE1pLbq/rw/xvh4+Dh/LXOze" +
                "X1Z2bTMxDJSoWwXdgVezRTKtoRVbNWO8kUGf+1Bm+LyqHSptVU" +
                "6H08memp2VU1vj8TEMlKgvNWQHXs0m53jailHGdSbIoM99KDN8" +
                "XtUOlbYq22H6nNzPxc7K1tZ4fAwDJepLDdmBV7PJOW5bMcq4zg" +
                "QZ9LkPZYbPq9qh0lZlN+wmuzM7Kztb4/ExDJSoLzV2OaPPoIrf" +
                "F/NHT6tmzGk7oc99KDN8sW6sprQW3f/PHOT/zPVwXeZi5xO+tj" +
                "UeH8NAifryt5IdeDWbvB6vWzHKuM4EGfS5D2WGz6vaodJW5X64" +
                "n+y92Vm5tzUeH8NAifpS4z5n9BlU8fti/uhp1Yw5bSf0uQ9lhi" +
                "/WjdWU1qL7+/og94XH43GZi53vdI5trR5iLZpnvrJr8tc7qZRN" +
                "7guPtYb3aEyM8L34Hlp9xFiLiT1pfj2ZJfJ1fJ3sq9lZebU1Hh" +
                "/jo/0uf1VrvPqMeDVb+h1XajBiZmWi2jpZzgyn5yMi7lResqff" +
                "cc/6r4n9d9zv5/Hl334G/ffw/nt4P8d+jsPVcFXmYudv6Fe2xu" +
                "NjGChRX77ryw68mk3uZ65aMcq4zgQZ9LkPZYbPq9qh0lp0fz32" +
                "93U/x36O/Rybv1OcjHUudr5jPLG1eoi1aJ75yq7JX+9IUza5uz" +
                "7RGt6jMTHC9+J7aPURYy0m9qT59WR89nQ/81+/O+n3M/3zsZ9j" +
                "P8fhYrgoc7HzN/QLW+PxMQyUqC/f9WUHXs0m9zMXrRhlXGeCDP" +
                "rchzLD51XtUGktur8eD/J6PBvOylzsfMJntsbjYxgoUV/+VrID" +
                "r2aT1+NZK0YZ15kggz73oczweVU7VNqijE/j0/T956nY+o3oiT" +
                "Wz+VD9U6/IREa8mk3/wlqDETMrE9XWyXJmOD0fEbGa8lblYXyY" +
                "7EOxVXlgzWw+VP/UKzKREa9mS+coNRgxszJRbZ0sZ4bT8xERqy" +
                "lvUYbb4XZ6Xd6anV+pt7bG42MYKFFfXvOyA69mk/f1bStGGdeZ" +
                "IIM+96HM8HlVO1TaqlwOl5O9NDsrl7bG42MYKFFfasgOvJpNzv" +
                "GyFaOM60yQQZ/7UGb4vKodKm1Vboabyd6YnZUbW+PxMQyUqC81" +
                "ZAdezSbneNOKUcZ1Jsigz30oM3xe1Q6Vtirnw/lkz83Oyrmt8f" +
                "gYBkrUlxqyA69mk3M8b8Uo4zoTZNDnPpQZPq9qh0pblPFlfJk+" +
                "J1+KrZ+cL6yZzYfqn3pFJjLi1Wzp/4zUYMTMykS1dbKcGU7PR0" +
                "SsprxVeRwfJ/tYbFUeWTObD9U/9YpMZMSr2dI5Sg1GzKxMVFsn" +
                "y5nh9HxExGrKW5Xn8Xmyz8VW5Zk1s/lQ/VOvyERGvJotnaPUYM" +
                "TMykS1dbKcGU7PR0SsprxFGe6G6S5wPxc7v+PvbI3HxzBQor58" +
                "dsgOvJpNPh/vWjHKuM4EGfS5D2WGz6vaodJWZTNsJrsxOysbW+" +
                "PxMQyUqC81ZAdezSbnuGnFKOM6E2TQ5z6UGT6vaodKW6P/B5VD" +
                "xdE=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 6, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 0, 35, 4, 0, 36, 0, 37, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 6, 0, 0, 39, 7, 0, 0, 40, 41, 8, 0, 0, 0, 42, 43, 0, 44, 0, 0, 45, 0, 9, 0, 46, 0, 10, 47, 11, 0, 48, 0, 0, 0, 49, 50, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 51, 0, 0, 0, 0, 0, 0, 52, 1, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 2, 0, 14, 0, 15, 0, 0, 53, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 0, 1, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 54, 0, 0, 0, 20, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 56, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 57, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 58, 0, 59, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 60, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 61, 0, 24, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 2, 0, 12, 0, 0, 0, 0, 0, 13, 0, 0, 63, 14, 0, 64, 0, 0, 0, 65, 0, 0, 0, 66, 67, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 14, 0, 0, 68, 15, 0, 0, 16, 0, 0, 69, 17, 0, 0, 0, 0, 0, 25, 26, 1, 0, 27, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 28, 29, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 33, 0, 0, 0, 4, 0, 0, 34, 0, 1, 35, 2, 0, 0, 0, 0, 5, 4, 0, 0, 36, 0, 37, 0, 0, 0, 0, 0, 0, 0, 38, 3, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 39, 16, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 1, 6, 0, 0, 5, 43, 0, 7, 1, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 70, 45, 0, 46, 47, 48, 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 54, 0, 1, 55, 0, 0, 0, 8, 56, 0, 57, 0, 58, 0, 0, 0, 6, 7, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 59, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 3, 0, 8, 60, 61, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 62, 0, 0, 0, 0, 71, 0, 0, 0, 0, 63, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65, 66, 17, 18, 0, 19, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 67, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 68, 25, 26, 0, 0, 0, 69, 70, 0, 0, 0, 4, 0, 0, 72, 5, 0, 0, 71, 1, 0, 0, 0, 27, 72, 0, 0, 0, 28, 0, 0, 29, 0, 0, 0, 1, 0, 73, 0, 0, 0, 0, 0, 0, 74, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 19, 0, 0, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 11, 0, 73, 74, 12, 0, 75, 75, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 5, 0, 0, 0, 0, 0, 0, 76, 77, 9, 0, 0, 2, 0, 78, 0, 0, 79, 1, 0, 80, 3, 0, 0, 0, 0, 0, 81, 2, 0, 0, 0, 0, 0, 0, 82, 83, 0, 0, 0, 0, 0, 0, 0, 0, 0, 84, 85, 0, 3, 4, 0, 0, 0, 86, 1, 87, 0, 0, 0, 88, 89, 90, 0, 13, 91, 92, 93, 94, 0, 95, 76, 96, 1, 97, 0, 77, 98, 99, 100, 78, 14, 2, 15, 0, 0, 101, 0, 0, 0, 0, 102, 0, 103, 0, 104, 105, 0, 0, 10, 0, 1, 0, 0, 0, 106, 4, 0, 1, 107, 108, 0, 0, 4, 109, 0, 6, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 111, 0, 0, 0, 0, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 5, 16, 0, 112, 17, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 113, 7, 0, 114, 115, 0, 11, 0, 0, 0, 12, 0, 116, 0, 0, 0, 0, 20, 0, 2, 0, 0, 6, 0, 0, 0, 4, 0, 117, 118, 0, 5, 0, 0, 0, 0, 0, 119, 0, 0, 0, 120, 121, 122, 0, 7, 0, 123, 0, 8, 13, 0, 0, 2, 0, 124, 0, 3, 2, 125, 0, 0, 14, 126, 0, 0, 0, 15, 9, 0, 0, 0, 0, 79, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 127, 128, 0, 129, 130, 131, 132, 0, 0, 0, 1, 0, 0, 0, 133, 0, 0, 23, 24, 25, 26, 27, 28, 29, 134, 30, 80, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 135, 46, 47, 48, 49, 136, 50, 51, 52, 53, 56, 57, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 137, 138, 139, 0, 140, 0, 59, 4, 81, 0, 141, 7, 0, 0, 142, 143, 0, 0, 10, 60, 144, 145, 146, 147, 82, 148, 0, 149, 150, 151, 152, 153, 154, 155, 156, 61, 0, 157, 158, 159, 160, 0, 0, 7, 0, 0, 0, 0, 0, 62, 0, 0, 0, 161, 0, 162, 0, 0, 0, 0, 1, 0, 2, 163, 164, 0, 0, 165, 166, 11, 0, 0, 0, 167, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 168, 1, 169, 0, 170, 0, 0, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 171, 172, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 83, 0, 0, 0, 0, 0, 84, 0, 12, 0, 0, 0, 173, 2, 0, 3, 0, 0, 0, 13, 0, 174, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 175, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 2, 0, 179, 180, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 181, 0, 182, 183, 0, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 184, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 185, 22, 16, 0, 0, 0, 0, 0, 0, 186, 0, 0, 1, 0, 0, 17, 187, 0, 3, 0, 7, 9, 0, 1, 0, 0, 0, 1, 0, 188, 23, 0, 0, 0, 0, 24, 0, 0, 18, 10, 11, 0, 12, 0, 13, 0, 0, 0, 0, 0, 14, 0, 15, 0, 0, 0, 0, 0, 189, 0, 190, 0, 0, 0, 191, 25, 0, 63, 0, 0, 192, 0, 0, 193, 194, 0, 195, 19, 0, 0, 196, 0, 0, 20, 0, 0, 0, 85, 0, 26, 0, 197, 0, 0, 0, 0, 0, 198, 21, 0, 0, 0, 0, 0, 18, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 199, 0, 0, 0, 0, 0, 0, 0, 0, 0, 86, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 0, 0, 0, 1, 200, 201, 0, 0, 0, 0, 0, 0, 202, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 203, 0, 0, 0, 204, 64, 0, 205, 0, 2, 3, 3, 0, 0, 0, 65, 87, 0, 0, 23, 0, 0, 0, 27, 206, 0, 207, 28, 24, 0, 208, 209, 0, 25, 210, 0, 0, 211, 212, 213, 214, 29, 215, 26, 216, 217, 218, 27, 219, 0, 220, 221, 6, 222, 223, 30, 0, 224, 225, 0, 0, 0, 0, 0, 66, 0, 0, 0, 2, 226, 227, 0, 228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 229, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 29, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 230, 4, 0, 0, 0, 0, 0, 0, 30, 0, 0, 1, 231, 232, 0, 1, 31, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 233, 69, 0, 0, 234, 0, 0, 235, 236, 0, 0, 0, 0, 32, 33, 0, 0, 3, 0, 0, 237, 0, 238, 0, 88, 239, 0, 240, 0, 0, 34, 0, 0, 0, 241, 0, 242, 35, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 243, 0, 244, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 7, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 37, 245, 0, 40, 0, 246, 0, 38, 247, 248, 39, 249, 0, 250, 0, 0, 0, 0, 0, 251, 40, 252, 41, 0, 253, 0, 254, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 256, 0, 0, 257, 0, 8, 0, 0, 42, 0, 258, 259, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 260, 261, 262, 263, 264, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 265, 43, 9, 0, 0, 10, 0, 12, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 70, 0, 0, 266, 0, 0, 0, 267, 0, 0, 0, 0, 44, 0, 268, 269, 270, 0, 0, 45, 271, 0, 272, 46, 47, 0, 0, 8, 273, 0, 2, 274, 275, 0, 0, 0, 8, 48, 276, 0, 277, 49, 278, 0, 0, 50, 0, 4, 279, 280, 0, 281, 0, 0, 0, 0, 0, 0, 282, 283, 0, 51, 0, 0, 52, 0, 0, 284, 0, 0, 0, 285, 0, 0, 0, 286, 1, 0, 0, 0, 5, 2, 0, 287, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 288, 44, 0, 0, 0, 0, 0, 71, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 289, 0, 0, 0, 0, 2, 0, 290, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 291, 45, 0, 0, 0, 292, 0, 0, 0, 0, 0, 293, 0, 0, 0, 0, 0, 0, 54, 0, 0, 55, 0, 294, 0, 0, 0, 0, 0, 0, 56, 0, 0, 36, 0, 0, 0, 37, 5, 295, 6, 296, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 297, 3, 298, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 299, 0, 300, 0, 301, 302, 0, 0, 0, 0, 0, 0, 303, 0, 0, 0, 7, 304, 0, 0, 0, 57, 0, 305, 0, 0, 306, 0, 0, 307, 308, 0, 46, 309, 0, 0, 0, 58, 89, 0, 0, 0, 310, 311, 59, 0, 60, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 90, 0, 0, 0, 2, 47, 61, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 312, 0, 48, 313, 49, 0, 72, 0, 50, 0, 0, 0, 0, 314, 63, 0, 0, 315, 64, 65, 0, 51, 0, 316, 66, 317, 0, 67, 52, 318, 319, 68, 69, 0, 53, 0, 320, 321, 0, 70, 54, 322, 0, 55, 0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 323, 0, 9, 324, 0, 0, 325, 326, 327, 72, 0, 0, 0, 3, 0, 0, 328, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 329, 73, 0, 0, 0, 0, 74, 0, 0, 38, 0, 0, 0, 0, 0, 330, 59, 331, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 332, 0, 6, 0, 0, 28, 0, 0, 0, 333, 0, 0, 0, 0, 0, 0, 61, 334, 335, 0, 0, 62, 336, 0, 63, 337, 0, 64, 338, 65, 0, 0, 75, 0, 0, 339, 340, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 341, 342, 91, 0, 343, 0, 0, 344, 0, 0, 0, 77, 0, 0, 0, 0, 0, 66, 0, 78, 0, 345, 0, 79, 67, 346, 0, 347, 348, 349, 80, 81, 0, 350, 68, 82, 351, 0, 352, 353, 354, 83, 0, 0, 355, 0, 0, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 0, 0, 0, 0, 0, 69, 356, 0, 70, 0, 0, 0, 84, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 357, 0, 358, 85, 359, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 360, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 361, 88, 76, 77, 362, 0, 363, 364, 0, 0, 365, 366, 0, 0, 0, 7, 0, 92, 89, 0, 0, 367, 0, 368, 0, 369, 370, 0, 90, 371, 372, 373, 374, 91, 92, 0, 0, 0, 375, 0, 376, 377, 378, 0, 93, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 379, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 380, 0, 381, 0, 0, 95, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 382, 383, 0, 384, 0, 385, 386, 0, 0, 0, 0, 97, 98, 0, 0, 0, 93, 94, 0, 99, 100, 101, 387, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 388, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 389, 105, 106, 0, 82, 107, 0, 83, 390, 391, 0, 0, 0, 392, 0, 0, 108, 0, 0, 84, 0, 393, 0, 0, 85, 0, 394, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 395, 0, 0, 0, 396, 0, 397, 0, 87, 0, 398, 0, 88, 109, 110, 111, 0, 399, 0, 112, 400, 401, 0, 113, 402, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 115, 0, 116, 403, 0, 404, 0, 0, 117, 405, 0, 118, 119, 406, 0, 120, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 1, 2, 0, 6, 7, 8, 1, 5, 0, 2, 0, 3, 9, 1, 5, 0, 10, 0, 0, 5, 10, 11, 4, 12, 0, 13, 5, 1, 0, 6, 1, 0, 14, 15, 16, 12, 10, 17, 18, 6, 2, 16, 19, 5, 6, 16, 20, 3, 17, 10, 21, 22, 23, 24, 1, 0, 25, 26, 3, 27, 28, 1, 29, 30, 0, 4, 31, 5, 1, 32, 0, 17, 33, 34, 5, 1, 0, 8, 35, 36, 16, 2, 37, 38, 4, 1, 39, 1, 2, 7, 40, 41, 6, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 1, 48, 49, 3, 5, 50, 12, 51, 52, 53, 54, 0, 1, 6, 1, 55, 56, 7, 10, 4, 0, 57, 12, 58, 59, 17, 3, 60, 61, 62, 63, 2, 18, 15, 64, 65, 66, 20, 67, 21, 68, 5, 69, 12, 70, 3, 71, 72, 73, 0, 74, 1, 21, 75, 2, 76, 77, 78, 20, 2, 79, 18, 80, 81, 82, 6, 83, 84, 9, 6, 10, 2, 85, 3, 86, 87, 1, 88, 1, 89, 1, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 1, 9, 99, 11, 5, 100, 101, 102, 103, 2, 104, 105, 106, 0, 107, 108, 5, 109, 0, 110, 25, 8, 6, 4, 27, 111, 112, 10, 7, 113, 4, 1, 1, 114, 8, 11, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 10, 20, 0, 125, 4, 1, 1, 126, 127, 2, 29, 1, 4, 0, 128, 17, 2, 12, 129, 30, 130, 131, 132, 0, 13, 29, 6, 133, 18, 1, 134, 7, 14, 5, 0, 135, 20, 18, 3, 136, 137, 138, 21, 21, 21, 5, 12, 139, 1, 8, 140, 141, 22, 142, 6, 143, 144, 5, 145, 146, 147, 148, 149, 150, 31, 33, 151, 152, 9, 7, 153, 35, 24, 3, 154, 155, 8, 156, 8, 157, 158, 159, 160, 5, 161, 3, 162, 163, 164, 36, 18, 165, 166, 167, 40, 168, 2, 6, 3, 169, 170, 12, 39, 171, 172, 2, 173, 174, 175, 43, 22, 44, 176, 177, 2, 178, 56, 22, 11, 179, 180, 13, 25, 181, 182, 183, 184, 185, 186, 15, 0, 187, 188, 45, 3, 6, 20, 189, 190, 191, 9, 192, 193, 12, 1, 194, 195, 196, 22, 0, 2, 30, 197, 28, 22, 198, 2, 12, 17, 9, 4, 7, 23, 1, 199, 9, 200, 201, 0, 7, 9, 202, 5, 203, 1, 204, 205, 4, 206, 22, 207, 208, 2, 0, 209, 210, 211, 30, 8, 6, 10, 2, 2, 212, 8, 17, 3, 213, 214, 8, 215, 216, 52, 217, 10, 218, 219, 220, 1, 221, 222, 223, 8, 224, 47, 3, 11, 225, 20, 14, 226, 227, 9, 228, 229, 46, 230, 231, 232, 233, 234, 235, 20, 236, 237, 238, 239, 240, 3 };

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
            final int compressedBytes = 1592;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXctuGzcUvaRplXa6mKRCoHTFOEKRRVdFPoAuikDLLNOdAG" +
                "fXn6ADtHA+oIvu7GX/wv6TZNfPKOepeXAemuHMcKh7AcmyZqwZ" +
                "3se5L175htzC3Y/qXD+vflWvPsrvQFzLTz8/v4N/gbz+7zd+z1" +
                "fs5Yb//den7Tf5i9xR+RbekGe/P738Zw9IPhCtvHOpH7zwDsu9" +
                "VvpB9EMi6zyloCJ/k7p4IP83Wpm5XqnGP0bVJoAVCCJ/EvrQHc" +
                "BesYt7+gLWG65Wkgm4gp2Qd/rHs80TvEL8G0w3of/ZQPi8oqH/" +
                "WYX+Z7tN/M8j4/dAmeb/rfY/D6H/gdj/fHxao//RJnhLQAUhGn" +
                "NtlkTK2CzZcwUXQIjUVkxC7OZEbcWtDMLjQjNWn7RWiN/W8ONB" +
                "48dlhh9bzWINEyCU1l9KeYQfjzF+wM4l/JDkDB5fZPqzkxr6JY" +
                "fvf4j051ZGXiDUn/NUf0ioPxdwjfrjCH6KGvz8os38a4ifV2b8" +
                "xPi93f98Lfifb8i/EfHns0bOEv6QOvwhBvzhSdyakOBZ5kJCn5" +
                "ecwkL3GP0WO0Okan5nJB4gk5CQkJCQvIr/muvfsArjvz/r69+5" +
                "QllQikdy79LUxV7H8ccebMQfN+eF+PWDKf5vqp/8QR6gLX+gmD" +
                "8gTU9v25MHi1cjkZnynNVGr0Ri0YEoGzl1ov6dz1/Peti/F0ST" +
                "J8YSjCUlWK7JbzzI/7L+RZDvX2xDbT3UH6P+xfukfwEO9i/ywq" +
                "E0fesaTK3JzAIFQiRSk0qRrvpTPYnD5P1vWb5BPoX/M/c/U/zY" +
                "R/iB/U+k6fB/Bve8+P0vBv/ZfnsR/kmC/eOSMtCOJ1qJP+buH+" +
                "fx/9IUP+7S+DG7vhzj+pX1Q2X90sX+uRP4hbRYCvuf6vj9F8Td" +
                "/ReH/K1L/G3uv78z1i/eGeqPDf37L+n+satl1j94q6ORw/2/Rf" +
                "39XNVf4r3+luRlqv8X9x8A7j/wk0j9/pOK1cbaEMjm+GEGvJl8" +
                "/0xz/SFfv1wd6pfCwfoD68LeE6I674K7iMYLFLr1n6XN/vNCjM" +
                "9Zaunfd5x/mC/+G7z+5v0HHdYv+gOuXD7/7IUvNf1vA7AU+t+8" +
                "ysHk9WWBQ3zkAknb/Yua+3eIgt7665SHP9L+G/cvTDT/NaR+/r" +
                "paP2eF+jmk9fP7Q/2ctNbPj88/B6S86TWZDr9r9M/T/RMd+h8g" +
                "Evk9GOTXPX9Zyv4Lr8SLNLVIhRG0ZgzQ58+7SYPTZBGYM32XIj" +
                "uDihwsx07iLCzC8HXUeY5J8ZwwjezdZGKgPPs4XudSRiZlswbQ" +
                "W4vZjIs++r5Ch5R8RCmo5QNl6BywO5C/y/aj6A9bI8jBlmA5h4" +
                "rxVy02aacN6mh3KaIzO6cUp3VdiLHG7v0TWBvDDF4FFpJz817G" +
                "PcQLOJtPvyfhn3Rd73hveajC6XQi+bJjY5igyycuKn7qt8yOSs" +
                "Dt6TXxDr9oQf5B4QAp8IUbTWDo33tVxSC9BCDqAKYpfiJzaMo4" +
                "f89GkxM9NmJFShkjh+kBO9YaZbvo6XB35V7qsTz87Oh/Rc7/0i" +
                "YhqOovvMwQ6vJSR4mf+IEL3JYSn9gmsiUQ669FCrl3UjQaBnLX" +
                "1nXM/PS08QOSg/o7H7XOP5r2n1f2b+D+iZPQP4oicI3/Xeb/au" +
                "YHx57/qzjloBYgcP8x2i+uf+56B9TNb1asNjY9e/MzDPXH6fv3" +
                "N1HvNX9pff/ycuNnb+ZXkRB/Xcc/jF+Q/w008fycnfyL13rAae" +
                "cn54y6u6wfPF1/Sn3nb+fTf6T2oJbZhgtfXLkv/sc4/wvm+V/R" +
                "af532u+fHFP7J5tfHiS/7vO/JyW/5YBsaX5yn81PRrPs/ecnlw" +
                "Ky5f1nKnegsv+M+eAHRk8dPPWfSEhIC8kGTwl0WDFZFflwX9ag" +
                "dOy/7Xx/wrj5hfRacnn+0xb+Bypo4v9o9rMscsbag/aDbshfjr" +
                "tUpA74fUhnDfInEOMvwDT2byet6rl+g/+K/yHduuq/XEhpTgjR" +
                "uuTPUMyfWWl+S063pKC7/lWq/bldhKZGABMIig7qPzGHLsJxe2" +
                "IN3SZpvnNh7cr24v9Jgn6F5jCG/tV48DK3efaE+Dcq89tJIOuQ" +
                "ToroWJ860fcPmO7/f0c+v5M=");
            
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
            final int compressedBytes = 1144;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUl22zAMBVk1jx0WfH1epDt2OAhfVjlGjoJ2lWWOkKvkZp" +
                "Xq2NZEkRJnCX/hxIlokgD48UHZNECHBggEwvHQrnzZPQpQ739R" +
                "wN9/5d0D6u7xQwMMxAnw2hAFaDJfHI8Moa8PpisIPuADi8rBPx" +
                "j0jS8uT9rrcdc2YWFfDmtYcsxuEMXm5sO6FxDTNgLGK7e99Eaz" +
                "CeFjf2F5Hhxipets+QspfxEIBAdIMw/x6dUXAlH5FNkM/51c9f" +
                "tDMP2/Yf4B898JPoq5RMfPQ1OjvvAy/rH9kOqf/CCjEgglFwR5" +
                "6m99yciyipKSULP0I+S3nkzkEtnjH3TY/4vPoOtf2sifYBw/pb" +
                "si/F/A/ZdJ/pWO89/LfQhuLkblQjy57JH7tqf6iUA4uITCGqo4" +
                "whVrZNZi/udb8j/nlmS/D8MR5k2IvMZhu1d6sThv0WzKMojGOF" +
                "RpnWJ59WcoqORONPfPKXES8haZO5Y7jVHcEEKoIUGWtejnx3z7" +
                "ZxT/hHzxLxfqRwHx3r+TIwfmzqGc4o00XJn5r4rP79D6ofgnHD" +
                "d/ErbwNwbjb4ofCFaoqduvPu8/rx1y8MMBWNb4f7UDEq0r/sBz" +
                "w/Fewh0opn93HntunYyNeOUcTvcC73Sj4Cc8gn5uf3y5f4PvT/" +
                "XPf8g/Kpx+VMTtrvx5ZP44VP45lv5qhok1EiEIYwb/bO0yQgFM" +
                "tOetJUaeTeu/6IiR/3U/7BxIhrk5gBCreKiUKmj/YN16Cn1fhe" +
                "y/kR4JBTCguFEexhvA+vfPcc/2Bcx/SRm7SKnGSDc496Ill57K" +
                "jTR3q9NVikRk6Z9n7H9T2aHPy573V+e3GT3MSCzvRYKra8vN98" +
                "8fkhV9OiuH6ejrr29/mLM/e7c/9O2vy4i8NfGzcfzx/L9u/BK3" +
                "jJ/0d/wo7K8f6PvvbP3/iWsQf1BG/BFS+X+Jfxb416k9gRAq/2" +
                "zJ/278B5v5L46CUsnaV+//DfpJrhpF2fFTdP304n7+KBrqp5nz" +
                "s3uHF81tNzUq+fRn9+9inp/qvv8WoL1Yqd5V/Pjz3H/Mtf8pAs" +
                "1f3PoX4/5T3AlKZT///ffc8RN4/x8T8lfJ+YebnELnhNj5O2X+" +
                "zO6/3PolI5SLMQtb73JXKy/65+/KqjMwzPqbsZ9ytp8oav9JDZ" +
                "9oj/jhk/n/NcXP1/jzlytmkT9/K6At6cNAjuPTYTFEXS/a/l9d" +
                "piV3e/5YQtjqcV9rZbU2Q0OuS5RyUvZPcCY7VdVogyqgcMvRWD" +
                "/IM6l8er3qP3bRfx0pv3T6V4JtYUQZfxO6Fo+7FzDJvUc/louO" +
                "JcvLQU1279ch3mTl43fWV+7339iY71FPpTn8GF46Q/26a/ZUAK" +
                "P4fn9lkF2DsuV3XBQw//K/P3tvwAllBMTu7s7pA0UGS9yf7/2T" +
                "aOfXVGI/AlU0uT7/NF5/uKF97fxdw/mr/v3UqH9lqpVXnP8xob" +
                "WqOn84mkVjf/5qF/UG6e+0EKoXa/T9RxWVr1f38B3l4xVTTzt/" +
                "r+9v2cf3zzRra4ibXPgHHfK12g==");
            
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
                "eNrtXUtu2zAQHbFEoQBdcOFFC3TBFD0IkZPkKNOcIEfwUStbtm" +
                "Q5oiyLpIaU3gMS2LFHQw7n84aiHU3OEGmqyVILS8qSo+av6vT0" +
                "6Jpf1TdNVVUfiE9/qpof5pocXaEvP8CtRTDbuFCpVM67Vk1djD" +
                "Dkc5L3XECNvLPufsXU73v/yEP4H+Sj+79w/tt7/i14/o3oGP/r" +
                "6y2fWd6J/9GV/7WXuOF/+44/NbiAGbxQnR+4kTLAyF9biV87fO" +
                "LSM+GA8VcJh+WE9ae/PJC6fzSefm+SRkeUpyXyoW1tYP3IjP9F" +
                "Glqd2vCz/cctGYYZXGtz+ze2k/y8448HP3/kAX98K8Q6Lnn8BO" +
                "5fFC8fGv9J9AfHzxr2i9K/ya5/hPon7L/S+wfIH9kA/YcE7AQJ" +
                "8zLUDS2VzlO/g2cGeLPLVr+6Xfmw8wNe/nKRfzl2/GUg/3mSN1" +
                "lEnp9/qcf8Kw5/C+xfys58QfYHAAAoN//FOL/n3XgJy7+R6tci" +
                "/QXwh/H5KzUlo1YahltLe6/KCekH5OHmh7sweO7Ww5RQCVsnOY" +
                "2fESF77N9m8If0/MNkWXrMzdC2FV5chP0LwNT9U3xYZ0f2dxs2" +
                "csz8//SicKQZiOrfTnMg7T9A5gCHgP+jfgLIPwBQSv3h9PVn+f" +
                "l3EPUdFmRXwvg11g/8DwBKCDAbjySDZMvSOTXjPQAAADESjuz5" +
                "xxX6X6Il5x/MF/l2/mrV8496YcMcS7ekfgDICxlS46/56z2P8+" +
                "Pz9ddeSlsC052VFnm7+sW1YfheGAIW5i/rz1+2y1817AcAQAZZ" +
                "LAlviFMSHRaouPq3Vv+wq2YtQfwM1089WD+z4vrJkU8udQq7vf" +
                "UmnUlwfgkteYbeHZp/pPv/vevfZK7E+gOz6mfdf/9ov1F2Zp11" +
                "bKajJ246u9GXtN28/QvlL9WMwXJOlm/7L+7658Pc/vmtZP/x7x" +
                "8IzH86/u89xz3p/26L8Q/GkztW/P8d0vkfR1rF65dv//fDl79/" +
                "4Ptzs4HyPN5VV2QJhxQBkTpd8Pk1oGi8dgyNSXFt6DvZyv29+K" +
                "VtXPDYPPz9k1id9jz+XIvxi/lHv95hP/QvQF7din7oQ1rYhzV6" +
                "OWSz8f5RLesfpe8fAMn2D/D9Z0AZTdxwJwHjz4YRFZb/nr//1Z" +
                "rv7sYnj2izlf81wfGv59lryONTPQ/BPjuZp+xYCAEAT+EQ/sej" +
                "+eNjtc8fm3D+ydn6T4LvvzC5nV9xG+4/EqzfU/X7Vp4X2u9Aej" +
                "TXq3aC9o6P8lV+AtVz8Xc7/57/qt57qstAB+XJRYvfefrrMf3/" +
                "AYypyLY=");
            
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
            final int compressedBytes = 854;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnV2OozAMx0MmK7HSPuShB8jeBO1J9ijWnmCOMEcdUVqWtk" +
                "DzjR3+v4eq1YzBtR3HhpAapaxSRvXKqQmn9O2tHl9oGF8/jOpU" +
                "f1Gk7lCvhvnDUv56iP/y039117cXtWApv8/e/5ks+qdiUiSf9b" +
                "/46v9n98g29vxB9luRtxn07x4/kipLkv8+/f1HAf4DqSwy0i0X" +
                "1YonZl9+Jy0UGP/P8lQy//LI/wAAAABgg4UJAABKnbnYR58DAC" +
                "jEeK2W5k8OBomHYIJm7G8D+o+I+w8OccW+f5Quz7z/Nq37P7V+" +
                "P3v8nF1/MfZzgRrQve6M/Abe8uzJeP8z3NsoK0QwiNDSRsb/sB" +
                "b/w3i4a/x/jd++G+O/u8f/tWF9t37hJv/zax4/D/Kfo3zu/Nif" +
                "2f+HD4/hvc00+y/TC9ARsK4fMq0/3avviJ8VNsobK8WLCVzUj3" +
                "7NMXo6tHtyH939D+rMv9LmbydObVNiWNXu3/KXEh6F6Zb+VE//" +
                "/kj/oX9LjH97bPzXGH/ovzD+AEjmtK19cL/YHSwPQAqIvw1jOB" +
                "m9LwAgqsCuW+Sk3j8s1n/al/5zOr8ueP/RVnAvaL1J8V1Xw6eo" +
                "6eA1gPxVpgDGegTEn3QIJmBq/zrrV44Yv34xt33/ihjtf1TOf2" +
                "H7z3nvvxZ4/c0clkew/xUHzMoSCZpH/9rqCeMk5T86wH5vsqfL" +
                "fH7Ub+DM+SvD/OMEzj8c5k9XLK+137/G+++X7/6lu/IALEauy5" +
                "kK6qE33q/Szq562B8QNFy/DX6SUfPf6vOznZ98t6ifAs4f9vyu" +
                "z/kx/qNqOANzgML1u9sev27uv/rw2H2Gbn98ONTvPYHpkKPYXw" +
                "b2i9VfZdK/zvkHTsVyDbD+F5TMGi9DlmP9er75zyncvytbu9aq" +
                "X3z9l/P+N0WcX7r/jr5+L+H5YWl5gpjZP71+K61/6f2DJWKRf6" +
                "rln7brtwPy58nyB64ottG/pNa/PPsf6X0Wc/2b37+pxPihpvr/" +
                "Oo7WjGNLJwpqz1LCMXPZU/zrl/j/t5X/sf4KgHNAIfNnjucfcP" +
                "+NB038fgfmKVCqfwaPnSQb/8X8fgi2VJdciqDRLj9FmmJmiX3+" +
                "1LUfNHLDCvUD6gf5DNAf8Y/4l0+LW6F9A3oJxuw=");
            
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
            final int compressedBytes = 797;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXV2OEzEMdsIIDRIPEdoHkHgIiINEnISjmJvsUWnptlRipj" +
                "PpxImdfN9DVWk367Hjv89Ju0QZ8Lt/01EWJspGVC0fAFpgyglS" +
                "QHov7hNLxYQRGmWr0MTI4fw631SM5ONdueJ0fn03nUrC/EJ8Xc" +
                "c8Uyrw/Bny3VW+W5MPAOT39i4MW/VUKppJzs2fl5+e89fcLn9f" +
                "5S+sf9m7/iccT4PnY//AX0WR4L/d+s+h+rG8PujbP962wrzP+x" +
                "nxvxfz4tv+6+9b/PCG/39ci5+s9a3ztx75R4YnfuU9UBGRcCqg" +
                "CUPk74dqF/sbfOivpcEdMSEWAQCoB/n5UVWwGP8vw7+b2GPWYP" +
                "/RscP/wlH/S7b1p9b6N7CfqxU/Ouyve358fP/l6g/iR0P9ed5/" +
                "6/Qf/fr/CP0L+D8AAAAACBf4WF22OyJf6f2hYe6fTZSW+td03p" +
                "W/9n8929+t3t9/2P/6PvifNVg67T3qf9b1B8Yqz2M+P+fUf+R/" +
                "ZejGfrgaKFbFZftfVi5fN1xB2uEarwe2AsELBCsA9Ml/JfjnSP" +
                "z9QJPF/XgcUmpn2H1/ycL9F832q9O/H0Xb+YXk84M/bUNxnP3z" +
                "P7753xPnZ33Xr0TAzoiOMIO5zlO6/+CM+hXWaYwXCcyi338H/o" +
                "D8DTzRv9vdf9xfb6u/QP3K6n/v17OQ/kmx/9epPyymP/xXv//W" +
                "638jdTI/AxbgJVKWX2Uni62QQoIc6HJcdVPi4uSfFr5Sy/1P1T" +
                "b1pyr642rOyPzB7vwf/Lsg5sbryaz8vVvU6aBp6lq+Zv4D1Kgf" +
                "DBMCgPj8oFD+DI2UCY2NGUy7QiScfxvlj7r5TzKuf+b5UfHP7y" +
                "fB57egv3z9sksLcf+22/pX7Pvrk0H/jWTg/88hfpvPb3D+bDv+" +
                "i2WRfuqvOv8zyB8AAAAw/+lg/gHkM0bARv+oQ77u/k8wsVZfDw" +
                "Dd8p8w9DZFwvnpiJ2D6fmt9fM78A/99Sd1bJtvN4bM5HkO9J6i" +
                "Sz/e2Fg8eebr6e3Xz8Sepkjfr8b4EH7Tl18PK+isxbBJ8foRPr" +
                "+sOn7+AHQy7r0=");
            
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
            final int compressedBytes = 214;
            final int uncompressedBytes = 11393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmEsKwzAMRBU3Cy9DyQF8FNOT+ehtWigOFPrBqSX5vUVCFg" +
                "HLkjMzEVHJtH8sYpks4Lb/YXd7W0qit66YRZbtGp+dTWtI1UyU" +
                "++k/zbc5iGs1yCXK5aA1RdoC0A3O34AqoNg/dvCfTf1bf/9cvt" +
                "F/+VT/c4P3Hfond/Ub7NPf8pfT/rN/YJnf5hfja5sF/41/cqE/" +
                "6K+i/KU5v1roXz5w/aPXj38D8I1m/bH+/Rv9/511/aT+Fqk5P6" +
                "JzqNX2/CJPTx7CNqCfbvRzhP0HUMsVO5c5eQ==");
            
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
            final int compressedBytes = 4225;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWwmY1EQWflVJKp3uAQEHUI5Vh0vAYQQRdJDLVUSOVRAFQR" +
                "AR8OJUUK5VHA9EUEQB8QQEBEVURFwVUBFFRFxZFQTWi8MVxQNF" +
                "d3UBe19VqnJ0J33M9IBuvi+dSiWpVPLn/9+rV6+1RkBAgyjE6A" +
                "GtKlSBfKjJ8qEAiqAuFEITrSk013aRYvM9KKaDoDW00XbTWtFO" +
                "0IH2otfBBuiq/wY96CH2DFwKA2Gw1gCugaH0NWYaA7W1VjO4Aa" +
                "6Hv8JNbAnoEIGKxj56l/4SiUN1OAaO06rQR/R+pE6kE9TXJ5DG" +
                "2iZoTPcbHaApnBJZF51LDtHpcBqcTudDWzgLzoGO2rv0V+iuVa" +
                "croDf00U8gK6Ef6W6t1L6ld9Kftb0wnP4Eo2CMNgUmkEHEpB21" +
                "rZAHR9PVrBpUg6pwLNSgo6CWNhmOhxNIXWgADaGRVplqenc4WV" +
                "sHLaAlteAMOod1hjPhz3A2HUB7R+dAZ+hCgbbTe0M36GnVh0ug" +
                "v5YPl8MAbSq8A1fAlTAMRsK7cB3tA+PhRvNCoGAAo7vBBIueTe" +
                "PkAFSASlodcxNUhtq0Qux0jUId8jLUo2v0upEhcCKcBM3gVL0P" +
                "PZO2pQfpD3Q0tCLtoR201zfAuWwZdIK/wHlwAVwEF5oXQS+4GP" +
                "pq2+AycjEMgqvgahhivsm6wAi41iqG0VpDGAdj2XPxuNEGbtVv" +
                "YE3icXrA/IJeRcfARGtJPA5FrDCOi9E2HucYG33pFWyVqNFprd" +
                "gsPL8XHUtWxONWZywfijwS9y3Wo9Z5iHFztc+WyPrpdJbRnBWp" +
                "erM122z0F2eMNHobFbFmhbmG70feiq3Ee89124SbcR8xFtedZ9" +
                "cZn8fjpDvWf4tXVNX2Yl9+wjMRY6xHjONxPRYPWLTJ/JfUVfv6" +
                "9aJ2Hf+lg7F0CetmH6ED4oEL9DdEG9pU8oK3nj3Pf6PnyKv/Jb" +
                "fO8ei1Yv+aWCttmnwHN7OZkRed69+n57Np9CCeMx572N55U7u8" +
                "d0GMS1SZXO25exdVMlrDLbiPGMPt9Am6CCbRxfRA8LNoz2Arxd" +
                "FR7BXZunwvtJfs898FdvJZzB/Fb0n0seiVvidfokp0LbbnPvE2" +
                "zmNxjU4ac4zp/sgp8dAlWt9c790nK+V9j3faFxiLY4MEelXCMX" +
                "be2M7Ie2yLF2PkcU/Z89VshSzhF6v39rSRL77FYn/LtI/vPoWJ" +
                "GJubbIzziLYU3xt/4jXeK5DHs8V5L9ClKd5Dd887GBeEMfL4Nm" +
                "wfv3pzP92htJp+yl5Frb6DfgKT4W7U6umA/DZGw32kOLaQXgF3" +
                "wT3QRn7FHeS2K65TzLfx91LRstBq7MPC6BD6MdaiVotv4EvQ5d" +
                "Nu5xhzrca745fBNnOMoX7kH6SxWYBnDo0VcK0WPT5V3ON0XFGr" +
                "Rfuv2DxOwuxb2Z/hNsaRVg7GjXD/aHm0qujBZzbGcAKuU22tjm" +
                "xSPEat5jxeyfpyrXZYK7RalO6Enl6MtalYN41rtYfjN8buEVum" +
                "tXQxhgo2xlBZYEy1pcCfvJ5z1Um4nso+olvlNTsBnwLaOceFVu" +
                "NWaDVuUavx9zI/xlyrsZZr9b0wVjB6s2uP9eq2PcYzCthc2x5b" +
                "7Q28I3lKr8bm68dwexyAcQ+rR/RDbo9FjcA4tio2T+x57fEM4F" +
                "ocwZ7MszGW36HAGLfIY2iM19ZTGMt7OBhDx2gzbo9FLdpjsZ3J" +
                "tdowFMa4zoIx0YdhAraIWm0wbo/FMWGPXR679tj5UgTG1t1whr" +
                "aP9UvEWLzDbdweO7XCHuNW2mMsXYcr2mP8pQLjYm6PFcZQSVyF" +
                "9thCnU7G2LbHkcqS0ccqe4zHzlUY2/ZYnK8wFvYYS0O8GKNW3y" +
                "8x/hgxnq2P0IfBQ9EuiPED6HO15Riz06BQH6IPNbaydgT1yLwJ" +
                "r2yNrWA5ej6uXZ2+9UBc8Nu3MbaX2Cuxx5j6HgTGolRRH27mcY" +
                "x9eltHbhuzVgLjc8IUCjHGb8GPsfx+zxC/reV5qNXc5xJ1LSBP" +
                "1lbD9cGEFhv41GCdrD1DP5r1DelDN08ZMfbsORiLPSHPWhsbY3" +
                "lGJbmtHe3GMU5quxlq/7KIVB19pKyVGMu9C3w2sCXXai/GntYe" +
                "lttHjaY2j/Fb+xTmIY/nWE1gLjxia7VVBPPhMfKaPHtBsFYjLq" +
                "/ZWq14nGfFFni1mvNY2iP0BKyTXR5Hlnt5LL6Pc8N4jF9Ws+S3" +
                "rnwuxWNlj20eB2u12OM8nprM4xi+N52/swAeR55ztdrTwyStll" +
                "umneWprSC3yNPYacivX4K0Gnmc72o153GQVvt57OuL5DGuC20e" +
                "sx1A2DfwJDzN9ggeL4Ka8AQfO1lPo1Y/hWfsZTtJMXD/7BkQPM" +
                "Gx0/nwbGw6+9rD44/8PM4bEDtkNPPzGB4XWs3bLArmMR874bUt" +
                "RM/2J73JxcjjlcE8VlqdzGOu1R4eL3ExTsnj0cEsjqxQPIb+Gf" +
                "G4SziP2e4gHiPG1RTGqXgMS12fS/GYfce+97S2TG6fV/bYGBOb" +
                "6dpja7ttj2UrxYC+pXGDsseIsc8eIy55fnuMNf2S7HEEKkqFCb" +
                "HHZoHAuHUKe/xZIsbQz+9zJWKsN/LbY1jutMt9rgB7zDE2Kgfz" +
                "OAqJ9lhsw+1xdxdjrz3mGJvvB9tjx8M+1va50tpjgTG8wLXax+" +
                "O/uTy2MZY8RoyN4VBgDLUxjlWLapzHsheIsTFR8LhDXhHnscKY" +
                "+9V+jOUVEmMseTD2ajV+OyNtjMX5jcUvYuzy2ItxrGYwxs653O" +
                "ca5cXYjoF4MK6RCcasYzDG3B5nijG8iBhfGI4x2x2EsUdVjsoQ" +
                "409UDITz2IPx/R6tnoJa/VLUZFMR45ehZqy6iHOt5Fptoap6td" +
                "oo0SrSWlh+Fus9Wi22A0O8FIGx0mob42CtVhiHa7XYBmh1wpkJ" +
                "PPZrdSY+F5sc2nZ2Wt07WKvFEwZqtRfj7LU66rFZsEpqfj4j8I" +
                "ZxwPgNVrNfYS28CW/RhvA6oJcF67EfBbQ+PYk2xTNllIFWpbUo" +
                "H7lXprUpokPr4noy/ZM4VsTEV0krURF5oNWcyEAhvCqeq07yW2" +
                "N60LukjeIZLqR7EMZenyv0yroJo+x1Tp8mx3OyaENSxDGGJ/T6" +
                "lYT3Upf7XBk8vxPnogXYihNRAfk0rlZHLXbQ1mo63I1XswJozn" +
                "bSC8x1UEyH2fZY8LiDzWO/VsNgdmJqrVYY2/FqqdX9ba02egdp" +
                "NbyvtJrHq5N5HK7V0qo1StZq4E9wfJBW83i14nFqrfbyOESrDd" +
                "TqkcFabfPYq9UqXu3HOFyr7Xg112o6wtXqqOHR6nG2VtNJjNDb" +
                "jQP0VthOb4KPYCudSEUkFLbg0RtZgTjrjkCm3RLAyatCNW5zdj" +
                "wuy6J4rOJcYQvdG8Zjs0aWd/zQt/ex0+INWbTxQTKP01zRNzFe" +
                "jXXbnNI/5fYTJ861jD7v+tW4Sh6Ld7Hc9bmC4lyuz8XGhfOYx0" +
                "C8PHbGI7WCfK5gvzoTHit7HMLjTzPxucwGZfe5hF99bziPsZTS" +
                "50rNY9fn0jwxEJ9f/ZnNY/hcYQw7cC8M45czxnhC5lqdPHY6LB" +
                "jvzAjjJrnBWG9Z/hjLsdOuJIx32xhH6ju69QGbKmrqJejZRlw/" +
                "pO/Rd+gGp+7tbNWTbgo/FmlQNmVO9Lngi9Q+F3vz8PlceqvSX0" +
                "tHe+cWM/G5Et6DnAuEvfANfAlfw3d0i7kFfoA98C3Wfp8JxmxT" +
                "dhjDVyF99IydSrskx7mcIyypL6NCveCcY2w8UTaMM/rGx4VgvE" +
                "9+Z4VKqyP3wn7pV++Bgsh93jgX/TpIq9n27GIg3B479V57POuw" +
                "2uMfM9LqbbnRamNJ6bWajs9Mq/UmIfb4JxkDOQp+gf/Cf+CAFo" +
                "FfcR89QovPTR5Mz2M6NjdabYw8Un51OI/T+9Xwc0Y8froMPB6f" +
                "oT1oEtLDf8vtIWu29Ll+Y4gx5JsyZUHlCIhnt8dzayEeNu+kcg" +
                "SCeCxLvhGSl8d87AT1fUdD553SvvnhQRj75518PJ4axGNbq0N4" +
                "nNW8k7E8eN4pmMd83smTnaKF5wh4eUxHBM87EQCHhWZbazFdjM" +
                "8luEtsxvtnhoqtRfFyW6wMbRbP58qGx24eSPY8Tm+PoX9GPH6x" +
                "DDxemvL+JWl7KOMXZJjZni4iIzjGKax6cbwcl1z4XLnGONsYSC" +
                "jGq8oL4/Q+F/HESpXPJcpifhomW3tsrebjY3IteUpcMzIsn8vV" +
                "arNnaq0OjIHM+v1pNcc4J1q9pvRarcbH6bQ6oS+uzyXzuaIzSI" +
                "LnBBO9Wm3HQDLnsdkr9fGgWGam4+PDqdXavhxp9drS85jVzY1W" +
                "03wg2qPRJdpDZAKPc5HxOHZqCAJZKHTnnaxFfN6J54FoDyfOO0" +
                "EPe94JBtIi8+LkeSc+dqIiB9WNc4XZY3duMWjeKbu5RXfsJGuz" +
                "yOcK1+rs8rmM9eFziwFtNwvCOFU+l6yTYyda4Kt18rmUVkffw9" +
                "I8yCfIFjefixXwfC5tjjx7QVqt7v3/odWcxznR6g1HUKsXqlgm" +
                "KbEx1p7UFpFbID/Wxjt2Qh5vJLeq8bHCmL4dinGfYIzpphQYN9" +
                "Dm/94wpnNyhPGuI4cxuU3OSTg8FhZskvS55pLbFcbkDnziprHO" +
                "4vhke/6YVuQY09oBGF/CMdZkHietlgmPuVYnY+xq9R/a5/rycG" +
                "IscgSSeMztMe49KVupQu627TH3uaCQTFP2GJ6jx9t5mdoagfGz" +
                "Ifa4b7g95rk+QfbY97ZS2GOe6xPZX0Z7nGGuT2RfOnucWa6P8V" +
                "V52WNYmtYeL3N9LqnVG8kMGa9GjMn9am7RzfXR1ov/LW6weWxj" +
                "DF0VxjxejRhfKv63iBhr7yge2xiHzy2K/aR4dRCPoaOllzVeTe" +
                "7LJF5tkdzEq1nl8p9btDEmM22MPVo9y+WxwljNH9t+tR9jMjtD" +
                "jC9zMN6RhPGMsmMcMCcxMzuME7Q6bE6iRo4wrn64MFY8Ts69TT" +
                "tGQ4y5PSYPUvefgWLspO2hoqdUKIrK2TMHJGu1KBWGj49DojxZ" +
                "jGuPxJxEhvGCg+U1Pg58Zz6tJg8478Nvjx+x/+8ERbEZyh6zdt" +
                "weq/9JsGLbHts+l22P5dUDtW/My12MWRvXHottWnts/9+pNPY4" +
                "6P9O8kiL0thj67bc2OOIUb72mLVMYY9lHFUHxPsxUYpiaR68Se" +
                "aztmRuPJ53l8y93YgYf6ibam6R/6ctPA/E9ORYq/+0ybHTq+l5" +
                "zNJmTVgh+X3JGDtHWpSGx9ak3PA4cmaGd/5vcO5tWq63TPpOkn" +
                "JvySK9Aryh5+lHkQW4t1Dn/40t8Wq1QCjjvExzUOj3/2p2Wh3y" +
                "zvZnq9XZLC7GkX25wdgqyfzc0mAc0IqDMXlc8pjnRL6B29qwGk" +
                "t2Dn0JvE6Wqhz6LDEeXL4YZ2+PS4exliOMo7ceOYwVj2m+ioGQ" +
                "Z9W8E23oi3N5/idBlkEb/bgkv3qK7VcLn+sK5Vf7YyDcrw6LgX" +
                "j96tzHQMT+EYuBRCcduRgIec72q/WGtBMe64o1qwW/nflOnkPv" +
                "8Lh1AoObhdrjK0Pf/uY/Eo9zlUMfa5pFG6XMoU+oS8qhJ2vU+F" +
                "gvDs6v9uZzqZy9hHi1J2fPvCo4Z8+OV2czPg6KV0NH641sx8dY" +
                "KsX42Ho9N+NjXA/j+DhhTkLl0P8Pmn6q8g==");
            
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
            final int compressedBytes = 2142;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW3tsFEUY/3Z2do/e0fIqKCoESgWBVqQEkEjxXaCCiFSJio" +
                "oCvgB5IwVaeRl5FIjyUKAGEKigxkT/8E+MiVahIEXwlRCRl4RH" +
                "RRKN8ZGc387Ozs3d7d6jd93eXdlkdmZn92b35ne/7/fNt9/5/a" +
                "CACl7wKV/4/dAecuFGrPOw5EMh3Kpjy++nxX62QTEM460SXo/G" +
                "UsZaT8Nkv9/zEkyF6X6xwSswHyrgVWxR2Ag5eg/efx1c75c26M" +
                "nrvmzfHwZI54ZguRPuw/0Idvww2z8OT7B6U9A4M9h+AZbFoq81" +
                "dGB1J+gInaVru2PpBb2hj9Wj1mLPYCxDPTfg/h64V1z7AIzirb" +
                "EwXvROgmdZ/Ty8AC/DLGzNwbIIKnFPsOhYPJDFr86GtqxuB114" +
                "D84G3CxGK4AiGBh4Pj0f7sDeu+Bu3I/EUgoPwhgYB4/CI+z6x+" +
                "BJ3D+DZQq8iPtprHcmzMb9PCxnodzoIbnWiEqt1SK9pfvkkZ6k" +
                "gPTH3m5mDx1FbiI5eNyOdCH4jCQfy22kK/tkP89UVrcl7VndSY" +
                "xZyMfr4Y9xI338jd4MjBuzqWIOsur8zb7p+fF+guTJR8qXHLEp" +
                "cFk5CBeVOvoc9h7G+WnAckXC+BB+9jg5QurIQTHWAae7eKY53L" +
                "0eR70QD8b2mzfbHYy9PnfxpJOTgXHwphzic/+68o1yXDlGVij1" +
                "dLZyVPmWLCHLBDKVpq0mK21xW26D8fQov80ebsxY4hgbtjr9eB" +
                "yC8RGOUy7y+Dvk8Q/4W6qEq8r30EB6B/E41FZXRLTVM51tdXw8" +
                "trfV3qdc4vGEDLDVnMd0HbZ/5GNW2dzH9LnWx3oXz6zM4LG+Kg" +
                "N4/JPgMfer6QblF9OvRh7n0U2WX23ymL5l8Njwq+lGg8dQYvIY" +
                "RpN8KDN4bPjVyOM5hl8d4LHlV5NCoPj5OPzqAI+bw6/21qajX2" +
                "3wWPKr/7D8alCU88o5RP2MsXbCwjBmYxSG2mrANRR9O9RWmxjj" +
                "WQPjueG22sSYtXKi8djEONRWk138rA3GTjyWMeY1Po1yNuTKXn" +
                "Y8hqFku+PYY6U2x5gfCYzZEeG9AmNst+V1F4exi+x4bGHMj8bZ" +
                "fE7CWOLxr7y+hOf+US7Cv5KPtwX7/pNttb3PZWur52WGrfaein" +
                "qPP1PeVl/geFYjj68ol7G1g24zeUy3o63eZ/GY7qHv0PeV3+hO" +
                "uhd4LIRuDfq2Zbw2YiDzg1YEuy0e05p4eWx71jUee8+kI49DMG" +
                "6w9Jh+yONcv7Orc60YCOrxGwE9Nm21chX1+IMwPV5j6jHzqxeE" +
                "67HpVxt6HOCxkx7zIwc9joHBM+x4bOox1h2Deg09rrLTY2xNcN" +
                "Tj1QE9FmfXG3osHVfyWg+6iq/toZ3oCdFjLDZ6LM4zPcY6TI9l" +
                "vzqgxwpwPR5IBpHB5HYyANtDnfxqUhzioxc52uryprXV3oaY1h" +
                "BDErbVl9LfrxazMYqUkpFkNGuPSQLGC1NBjxPHOBNiIEIxz4vW" +
                "KbLfDuM4Y5mLnGOZ6YSx96+Is3YmnTDWPKKVrba2nS+GseYzMV" +
                "az1ZxIGIeMnhWKsTtbZvBYbZOkcbpK7W7OGCNebWLhcWpsiWPs" +
                "U1IA4+5J4nFugMfc4zbfQvYLttUWj6Pa6sWReZyIrYalLq6Pa6" +
                "PeY2Lz2mpYFvUJl1trJ2yztRP7jkOirJ1WwTCtU8S1U0VmrJ18" +
                "fTJg7fSauXbSulrxaq1QzgNRS8x4tWWrTZ/LygMhB5zyQLC2zQ" +
                "Mh9WYeiOhP8TwQX0Hzx6s9SxqbB6LdwjDebMWrtdIwNUMeqyOE" +
                "rQ55t6iNjPhusSo8Xm3yOF5b3bx5IOm5dgqOV6vDOWILtXLlXY" +
                "2veNQy+/WxtjjWu3g2O877Z+6tndITY9gfirHB47hH+VzEMms4" +
                "xhWWHps+FwgfTOix5HOpk2xt9RrTVrO5eU+21VqW0OP6ePXYzl" +
                "ajTo5zR4+NOFf66bFlq7kef8L1eJ221sRYneL5KBxjjhXPEVA+" +
                "ts3LlDCW9ZicMPWY99OgJ2ksxuMTx5icTA+MyenkYIx6vFUtD9" +
                "dj6Yjpcau5pBtlqGhbIukx/4yjHsehLS1ej2nnxPTYygPRqrW1" +
                "Rs5eq8MKrmCVo2qQ8tJpdLrM4yQ894ymmI1Wh5K+Pk6BdxJ0Vo" +
                "IxlEXW+ljbY2Cs7TIwVpfifB2z47HkV+++xmN3No+aoF8t3h3A" +
                "ZXUZXFRZTCQ8v5rjutdqZXnVnEbP/IXEv7dvrktxrhTgcaLxap" +
                "FfjT6WuoLjV+ysxxKPP42Vx+REavLY8rlSncfkdHL02MjZ499v" +
                "o/ZVeM5eMMYsZ68iUs6ejLFWFJ6zFxWdvpEwTjzXR90UjLFTro" +
                "8zxu7l+lgYJ5qzh2ekNywmxtoRGWN2t5XqFuT5RCi2fTIpn8v2" +
                "PPtPW7wY254d4duWOMZBVzrlc11qfowtvzrRfC5aLUbcobF3Q9" +
                "4OdDty8KjoZzl7WO+kQpWDc/YieIa7RauGrnExYuS6z0VXp4LP" +
                "FfJMVcKvPqm9yZV2Q0x6/HOsemzZavf1OPL745aixwLraiuWiT" +
                "w+Z8S5vGcNHtN9VpxL5rEVy6RbY4lzGTy24ly0JjlxrqgMTlIs" +
                "M8L/j12Lc3nU5MS5mN3G9THu+fo47LfB4tV6z9hyBBx/YUnK9f" +
                "F93XLWTgmvvaTcOo3lp+m9HNCJK2evyTGuu4ZxzOtjvnbSC0gp" +
                "IsByb3VHnzb23NvU2DIjZ68pNp3HkvX7w+ZsZXp9k8zIoU/abO" +
                "RKGM9z9qv14ZZfrZekfry6pfLY3q8O4nG5PcZYpLUTtgTG7Agx" +
                "1se4hbHv72s8jhdjmceeQfYY6w8FMPYMVnOu8dglv7hNcjCG/w" +
                "H5FJMS");
            
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
            final int compressedBytes = 944;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms9PE0EUxyeP7WuhoKJEPYhBIY0QiaInD2qiJ5GGBBPUqB" +
                "F/RA2oIRgRY4IG/BU1JgYjRuPNIHL04n+A8sPgAY0n41njWU/W" +
                "2e3udrbd3e5uoWWH95Lp/OjMpt1Pv2/mvS3UpEyLdqVraEwJBg" +
                "nYCi28rsMOtY+HYAOs4P1qqIV6Xjfwsh02anO36WtWwWqtXmte" +
                "pTnl06DJbrRyi6e1u1KBrGzSaFX8SoXQYHO+GdHHtusSvLRYRj" +
                "TGvK7VXhsy72CnE+MAn9eecTMxLojxy+CM8UiRdHyAGAdhHP2Z" +
                "riMjbozxqBvjYvlqj2uJsWB4jDP+bWWcvR+nGUOdlbG6HxuMi7" +
                "gfdxDjQnx17K7tr+C49/3YnLF4+3EnMfbLGPi5mpXxEueMx/Pr" +
                "mO0RdYwnWTJbx6yHXRIZs/60jpkiXpWtY+st/YSl18J2ZhjHxv" +
                "jIXp3xCS/fNfbW2mdr7GaxTdprI2uyZ8z2sf3m3IOszWwftrnW" +
                "BUtvUK/RMlql19XmSL0fgqyVl3Ze9N8503ZPdsrKmPWyPv56ld" +
                "+FCXbd25Wxa2F0HJtfEB13e7obA6Rjfse/5M4ov6JTPZ2z9n7I" +
                "vusy9dUOd6MGz+EZtaWMVExnfDWeFXMgeF4ZNc5cytOS5kBuEG" +
                "P/Zy7kO4lSqazk3+srb/G9Aoa86RiG5dWxTAY1NmMOuUz9N9Ed" +
                "TMfKw+LFx4Xvx35NebD0dKw80on1AD+tQVJtx785rt2d1d8h+3" +
                "4sk+FFaGVlkBRjJzfGauwkMmbJHA3x2MnS79drn7GTcD4UYidv" +
                "VnjspL0TuthJ6zvGTihwWZz92G/sVIgV31cvRTNiJ7yc8dUuvi" +
                "9kvjqoSeqre4mx9OfqNmjFvjRjaKczl5Q67icdy85YoH3Nge8s" +
                "L/MwBzMwbY5N+VbWZ2JcMrIDpGP5dYw3ibH0jIeMfLVqcuSriX" +
                "EW42HS8XI5cwnU73jTMTEOSXwsPD82x9Tnx+Z/u5bW82Ni7HrP" +
                "HP/PlTPm/mzxCTEOF2OP3vsZ+epQ++oX+DyfjvGVoONR0nHYfD" +
                "W+xnF848NXjxHjsPlqnKDYSW5fje+NVqTKga+Wr47Eg+SrI+Xm" +
                "CspXl3I/nsMPOIUfOe9POIkzeX31NPnqkqhxNoivxne8/NDfvS" +
                "3MNHOZMOiWA6FcplQe/Q/FTmH21UYrK8/11+qrKc8V4tjpXsZX" +
                "4z+tdYt8taw69hofR8tJx2HLgUTj/hhHvhPjsOVAvFi0is5c4T" +
                "X2H555DHg=");
            
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
            final int compressedBytes = 495;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmktLAlEUx+Okc3aRFrXJKDMhKcr2bXpnuOgLtAlq36ZvUE" +
                "JEkYEZQe/osYq+Q5Aatej7TNdJxMeMMtbCe+Z/4Xq9R4+j5zf/" +
                "c++Z0TTrGwdqLZQyPdE63yT+Kuq1sY1VzaI0TnE1DpXOgCANUJ" +
                "eaByhEI2qMqD5Fg9Z7J0s+3RS0xr7yp0y4/mYxMHYds7A9Y+5x" +
                "x9j3DMa6MXat434wFs94GIzFMx6Vy1j73VW4Rb+o6vEqi8VYjS" +
                "HrMVKxI485MW7huGD8T4x51n/Q3Nd/iPjpXDvxnLtc7c8iV2um" +
                "43no2EPnwAsvOL72ivhonatXeYWXeYkTxgcvGvlmudrIIVfruq" +
                "/mZANf6Fh84zXEQGN665RQOk02yQEzNfNpRE6n9djG1vg61zbW" +
                "Y23X4x0Hv4Lq3/RJecqVbe+uj/4FAm2czXeLjC1Of2CM1taM98" +
                "DYE3neYswpMNZcr/tYjz2r4cKvjvkIuVqQoo/rOKcQFZ3rY2PT" +
                "2Cg+86Ur62NOV9bHxpYvU/7P3gnqY73qY87gOpf4vHwKxuIZZ8" +
                "FYPOMzMBbP+ByMxTO+AGPxjC/BWH59bGNrfP/4CvWxZvXxNXQs" +
                "PlffgLGHaN868MV9J93J3kHH4hnfg7F4xg9gLJ7xIxiLZ/wExt" +
                "Jbxw8BZMpm");
            
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
            final int compressedBytes = 551;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7tLY0EUxmVyM3MK1xdhK5VFF0FRXIPPwnJ9BJdFEa0EYW" +
                "1V7FTMLrqNhWKhYGNlY7GFYuMDC1FwfeV/0NbOzu56ucmGPK/e" +
                "gOA5+x0IkxlmknB+fGe+mXDNHxWxbfXN9gzVndEP2whhYXaRA2" +
                "FE97J0/ICs8A0VMgem3/SZXhPRMdOjb5yx+rQZdapRtTjtp3hf" +
                "X6tKVeL0y1WVqnXaz87ri6p25zYn1pSpCrf9mPyUJt+/rAF0fO" +
                "esJg/jQ/1XX+lLh15MX4Dxew19Wwhjve8yPsqa6ck42A/G3HSc" +
                "Y8yTsTkGY3a1+gQ6Fs/4FJ4LtTqjVp+BMTsdn8NXS/fVvnV8B8" +
                "bia/U9GItn/AjG4hk/gbF4xjYY82VMxa9hbEXBmBfjXEEfstYu" +
                "I3+MyYcojHsu7MfpjKkVjLkxpnaHW4er0Bi1QcfvNaizEMb/7r" +
                "noqx8dB7bAWHyt7gVjXoypL7j68trgGvLH2lcP0xCNeOuYRlN0" +
                "PAgdCzgfj+F8LEvHvvfjceiYq+eiHy8xDly6vhpnJwm1egK1Wh" +
                "T5ARWhqfgzbep73lnMnmlTXexuOCbf/BumrdL4O6vcydDS63Ss" +
                "fkMjXINmUKuFnY/nEwpeT/VcNJfuuazNpK+ehefi77lyKHsB+Z" +
                "NwPs7QcTSfjq0N6JiXjukn/XLJVaXMhOf6H13YInIgnjF8NfP9" +
                "mFb1TtqY9331CvZjZvvxNv4/lh5Fz1+awiM=");
            
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
            final int compressedBytes = 594;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm0tLXDEUxyXE3GRjrYgrp4iVAcVSFVy4KHTVqij9Bi4Ux6" +
                "UIuil1764LBUEUFXQq7a67orj0Wf0EPlYiaosbUYroNFw1zu08" +
                "zJUKc3L/B2by4GaGOb/8z0lChpXLH2JBfEmlGasNtOKsnjXqsu" +
                "qmLZKskpXo9nMWYy91WaNfDeyF/+zr2zGlrMwvK8ynvEqFNFaX" +
                "goX1WXXW3g7WLpdYp1//kHPsm3/aTQX+W1uo0ZGLT+iN8ix9eX" +
                "Usl6FjWjq2nGVr8B9h8kbHfDRdx3IjqGM+fqdjPgYdE8vHuRhv" +
                "g7E7jOWuicg7NvmYD4MxOcb7Iddce2BMNVZbMz4AY6qM5WE+xs" +
                "jHkdLxERg7z/gYjB3ZO50gVju0rv4VTsf8GxjTYswnzZrrt+mb" +
                "Djwxz6f4V13OylPTN2H3nXzO1JIgULgmz/T8aAzMFl/Huoz57z" +
                "VZ5lOGjh8xJ6Hj/6Rji3Fxe8byHIydWFdfIB+7pmP5B7E6cjq+" +
                "hI5d07HP9Qr+I0w+1xnINc5AIhyrU2BMlbEqyscYOnY9HysB/1" +
                "HOxyIhejLzsfLSdSz6oOMo5WP1DIzpMlalNoxxZy8COi4DY/pr" +
                "LtWcMXYE/nPhDMRax2+hY2qxWnV4bV6r995rF1veO7H54H9TN8" +
                "CYHONOsSrWxZqmtyVWwLhQTfx8DGPxPfd5teoKMsb+mK6Oiz+q" +
                "7ofHFn+C/1w31Qsf0LX7e5l8RiVua9Oaap/pN/cy+YLpw71MWh" +
                "rtt4jVn+GnSO2PB7DmIrd3GgrJeBCMaTEu+gtxo1cR");
            
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
            final int compressedBytes = 551;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm70vBEEYxpns7e5sgZPLFZwjXHzeBRHhL/DVUdBqRKlV0N" +
                "BoREFyidDpdCqNiMJHTk6Hzkel0CIIWWtzZPd23d6I4t7xTHLZ" +
                "7GRms/v+8jzzzps5PhtaNgNbaMVEI9tYhM95+tpcd80sybqta0" +
                "OO9xaLsQrrPszqWJN1TVi/Lha3x3bm5lSxavsa/X5KSvjN2kFH" +
                "OGaN/ox9+goy5vNgTI0xX9eGtSFtUBtRs9qAehrEWM2AMTnGG6" +
                "ap99n0snovGJdq0/t/w1jdcXq1sur0ar7rZqykvxgra2As+3qs" +
                "T5YeY74HvsUwztPxPnQsE2N+YOvz7mcdq9NgTJex7XSHgV54hP" +
                "hRroEI74+PoWN6OrZ8ekapzDlx2Bq54Jm75PvExVL5Nn4CvsXo" +
                "OC/nyiDnkkvHFtOsYyQ5HaP9gReeIQbSMz5HDCivx/wiOK9WNx" +
                "316jTWY3o1EMG90yUYU2TMrwUYX4ExSca3AoxvwJjm3ikg53qg" +
                "+MXlU6Dutx7zp0AdP0LH1LyavzoIPxfh1S9gTDev5u/8DWd9ZN" +
                "87cRM6losxv/9kbDB9TB8vnFfrE46zPqNgLGNebUQQP8p5tRH1" +
                "9BX+nwRqIKTX42IYGzVgTJWx+4yAUetmjDMClBkbMTEdK9tgLL" +
                "1Xx8FYesb1YCw94wQYS8+4BYzp10CMVs/cJcSPcg1EWMcd0LH0" +
                "Xp0EY/pe7ePeKcTvX3l1D3RMS8dlH3ilOKY=");
            
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
            final int compressedBytes = 518;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm0ErBGEYx/W2+zbzHITkZCWkiFAoOTn4AFIcHJ18AQ4u4i" +
                "KRA+Ub4Cbr4CKlhLW7X4GlUA6c7E1j2nbt7O60846LfZ79P7XN" +
                "zjQzbc+v//95nndmHacyaLT8iNp0EMKDxpEDvqFafY71lez1qg" +
                "E14m4787wnVLtqdPebVUx1u9se9zOsOnLnDuWvaVItuW3b710G" +
                "Q/+yftAJnbMuf8Y0GY5x9AGMeTGmqehO8LXRXeRPjlfb2SAd21" +
                "/QMS8dG/Zc08gfZx3TrD7WR+b1WB9Cx9x6rsK3yJ6XMS2UMo4c" +
                "FBhH9sGYK2Pj2WkRjNnNTkv6Vif0nevCaX2jk4FefQ/G/xE69R" +
                "fGOu7pqpY9Z65XXOu7lqk2kHk5QSvIgZz52KAer8Krxfdca2DM" +
                "fw2EtszqMYJx9d0GY8n1GOvVsuux/W2NYT6uj3rs494nyB9vr6" +
                "ZTa656X23NF3VszUDHHL2a4uazExjzrcdlz53OShnjuZNAxudg" +
                "LHoN5ALzscz5uEzHl9CxaB1fQccydWzcV79Ax1x7LuutGuOiV9" +
                "M1GPNiTIl8lY15zsR7IAK92n4092pK1p6OKQWSQV5tP5kztjM1" +
                "yDgNvuH6ap8cZpC/euqr6Rk9FzevpteQsxP+m8p2djLW8TsYi2" +
                "f8Acb8ey76NJuPEWJ7LryzJ9+rs2DMi3HDDy5j5MQ=");
            
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
            final int compressedBytes = 445;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrtKA1EQhsNwsps4ICoSLGIMGgTFSyz0ETSxsFIQsdFWwU" +
                "rwFrBRA5YKNr6ABF9E8RV8CwVt1iXGy25WkgM2M+cfCCe7ZEOY" +
                "L//MP7snCNqj5y1+huoBQmzQYMK5icjROE3RXLgWW/+Ad8pTb3" +
                "jcT8M0Fq6l8FWmQvOzs61r+miguea+v2Xa+pdNgo51zkb/hzGn" +
                "wFgqY3P1mzEPRRmbmy/G5hqMZTFOCs6jH+vqx1zwq37FX/KXvS" +
                "d/0XvsVKu9B+hYWq3mEbt+nK6CsXrPVQRjcTouoVZDxzEdz4Cx" +
                "Al89D1+tPXgBjJXNThX0Y/dqdYKyV5A/yTq29lyr0LF6X70Gxg" +
                "p89To8l+O1egM6VqDjTehY2ey09fku+vw4cxjV8c/z48wBdKxy" +
                "dtpG/pzqxzvQsdzZiXe7YWxqYCx7Ps6+dmKcfQFjlf14D/lzqh" +
                "/vQ8eya3UXjI/AWD3jYzCWyji2h/7kr3sg2EMvjzHX7HRsGmCs" +
                "0lefIn9O+eoz6Fi95zoHYyWe6wKeS5HnqsNzqWd8acc4/QzG8n" +
                "0137Zdi30g6oPvkAP1jBvIgVPz8T36sax+nPoAh3u2Eg==");
            
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
            final int compressedBytes = 383;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2ElKA0EUxvHw6EVDNeIQh4WKRBEURYXcwLWKCuYCLhRXnk" +
                "XBQ3gD7yCI6MoBnBYON3DRNlFDWkW6lvXVvyA0FZJevB9fvddt" +
                "9fzXspnSbtrmbKm4Tnzu3YmNWk+x77MxmyyuU8Vn0cbbv134+k" +
                "+v9bevQ527zOeey2Zzlm/NGn9+2zFODrqN3WnZODn6Nk4OMRYx" +
                "PsNYz7jqWZ0+YRyWcbXlzqlfwPL+M9cFOVY/q90lxqEZu6t0I9" +
                "363zhtdfXjdYzlc3yNsbzxDcbyxrcYyxvfYSxvfI9xqMY/3mU+" +
                "lI15lxn0s9OjX46TY4zDMq623DP1C1jevx+/kGORfvxKPxbqx2" +
                "/040hz/E6OI5u5KJ/8ymrUIKa5OhvgrA61H1c2rmOsMXNlg8xc" +
                "Eed4GGORHI+Q44hz3MBYJMdNchzXO5BsmfpF9Xy8Qo7l+/Eqxv" +
                "LGaxjLG29iLG/cwlhyrt6mflHN1TvkWP6s3sVY3ngPY3njfYzD" +
                "Mq59ANoByss=");
            
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
            final int compressedBytes = 116;
            final int uncompressedBytes = 3389;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjEvmPAZg0UXhqTDpMhkBaAcLnKWGSYeID8gWZZJmUgbQKEB" +
                "swyYHV6kP1CDAJgWkxuCm6/0kETFr/RwGpYaaEVZT0OC4djeNh" +
                "H8cVo3E87OO4dTSOh30ct43G8bCP447ROB72cdw9GsdDK44ZAE" +
                "GSFGE=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 18, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 111;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3bEJACAMBMDsP7OgG1j5JOBdL4iGTwrBKgAAAAAAAAAAgJ" +
                "n21Yqv794/2fN3P/IDAPmN+npZX+oX5AeoPwDQXwEA84X5AgAA" +
                "8yuAfAQA9G8AQP8HAAAAAAAAAAAAAOBf6f8Xva9vdgBo52l3");
            
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
                "AAAIBfelUCAfgrAAAAAAAAAAAAAAAAAAAAAADAzQCvBj29");
            
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
            final int rows = 24;
            final int cols = 123;
            final int compressedBytes = 130;
            final int uncompressedBytes = 11809;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmMcRgDAMwEKfLMzlpel1BL6AdNpAOju+RIoH8hZJGPSNDi" +
                "jkUQeY1osOMK1nHWDe60IHmNa1DjA7/NIBpvWhA28zsbV89jZr" +
                "dYCZ60kHmLmudIBp3ekAs8MHHWBanzrAtN51gGm96gBzm+nAfz" +
                "P531yXOnCuxdbyTtINuBaS2A==");
            
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
