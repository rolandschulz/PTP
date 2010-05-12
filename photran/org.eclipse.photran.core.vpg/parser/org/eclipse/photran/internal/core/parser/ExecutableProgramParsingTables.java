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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 2, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 15, 62, 63, 64, 65, 3, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 0, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 19, 126, 0, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 7, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 15, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 110, 189, 190, 0, 191, 192, 101, 1, 30, 35, 0, 103, 193, 194, 195, 196, 197, 198, 199, 200, 201, 140, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 212, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 57, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 1, 2, 3, 57, 1, 7, 123, 4, 124, 0, 5, 127, 221, 125, 237, 6, 7, 128, 126, 0, 173, 238, 206, 212, 214, 8, 239, 215, 88, 30, 9, 216, 217, 218, 219, 101, 30, 114, 10, 220, 11, 240, 222, 12, 227, 13, 0, 14, 228, 2, 129, 230, 150, 241, 231, 242, 15, 16, 243, 30, 244, 245, 246, 17, 247, 29, 248, 249, 18, 115, 250, 251, 19, 252, 20, 253, 254, 255, 256, 257, 258, 130, 134, 0, 21, 137, 259, 260, 261, 262, 22, 23, 263, 264, 24, 265, 266, 3, 25, 267, 268, 269, 26, 27, 152, 154, 28, 243, 270, 271, 237, 241, 272, 273, 4, 31, 274, 39, 29, 39, 244, 275, 276, 277, 0, 88, 278, 39, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 56, 289, 30, 290, 291, 156, 6, 292, 293, 294, 245, 295, 296, 297, 238, 298, 299, 103, 300, 7, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 30, 39, 311, 32, 312, 313, 33, 314, 315, 316, 5, 317, 34, 318, 319, 320, 0, 1, 2, 321, 322, 323, 30, 35, 324, 239, 325, 144, 326, 327, 328, 57, 8, 329, 246, 240, 247, 236, 7, 248, 249, 252, 253, 254, 330, 255, 256, 331, 242, 9, 173, 10, 332, 333, 36, 334, 88, 335, 257, 336, 337, 338, 258, 180, 250, 259, 339, 340, 341, 262, 264, 342, 343, 101, 344, 345, 346, 347, 348, 349, 11, 37, 38, 350, 12, 13, 14, 15, 0, 351, 16, 17, 18, 40, 19, 20, 21, 352, 353, 22, 23, 24, 354, 26, 355, 356, 28, 41, 33, 34, 357, 36, 37, 38, 42, 43, 44, 45, 358, 359, 46, 47, 360, 48, 361, 362, 49, 50, 51, 39, 32, 42, 35, 266, 43, 44, 52, 53, 54, 363, 364, 0, 365, 366, 55, 56, 58, 59, 60, 367, 61, 62, 368, 369, 370, 63, 64, 371, 65, 66, 67, 68, 372, 69, 70, 71, 72, 373, 374, 73, 375, 74, 75, 376, 76, 77, 78, 79, 80, 1, 377, 378, 379, 380, 381, 81, 82, 83, 2, 84, 85, 382, 86, 3, 87, 383, 88, 89, 90, 0, 384, 385, 91, 92, 4, 45, 386, 93, 94, 387, 6, 95, 388, 3, 389, 4, 46, 96, 390, 97, 5, 391, 6, 98, 392, 99, 393, 394, 100, 102, 7, 395, 104, 105, 396, 47, 48, 397, 106, 8, 107, 108, 398, 109, 399, 400, 1, 401, 402, 403, 404, 405, 406, 123, 110, 407, 111, 112, 408, 9, 113, 114, 49, 116, 117, 10, 118, 0, 119, 120, 11, 7, 12, 121, 409, 122, 123, 1, 124, 126, 127, 13, 129, 14, 0, 128, 410, 130, 131, 132, 133, 134, 411, 135, 136, 137, 412, 138, 139, 140, 413, 141, 414, 415, 416, 142, 15, 417, 418, 419, 420, 421, 422, 423, 143, 145, 424, 146, 425, 147, 17, 148, 181, 426, 427, 7, 428, 149, 150, 23, 151, 152, 429, 117, 430, 15, 153, 154, 155, 25, 156, 157, 27, 19, 158, 159, 431, 29, 432, 433, 434, 160, 435, 436, 437, 438, 161, 162, 50, 0, 163, 164, 165, 166, 167, 439, 168, 30, 440, 441, 442, 443, 169, 52, 143, 170, 171, 172, 173, 444, 445, 446, 174, 175, 176, 177, 39, 7, 178, 447, 448, 449, 450, 451, 452, 179, 453, 101, 454, 455, 456, 180, 53, 457, 458, 181, 459, 460, 461, 462, 463, 182, 464, 465, 251, 466, 467, 184, 183, 185, 468, 469, 470, 471, 472, 186, 473, 474, 187, 475, 476, 477, 478, 188, 479, 2, 480, 481, 56, 189, 482, 483, 484, 485, 486, 190, 487, 488, 489, 490, 191, 192, 491, 492, 493, 101, 194, 494, 495, 193, 496, 195, 497, 498, 499, 500, 15, 260, 52, 501, 196, 502, 261, 503, 267, 504, 269, 505, 270, 35, 197, 198, 199, 200, 53, 201, 506, 507, 508, 202, 509, 274, 510, 511, 512, 15, 275, 513, 7, 8, 57, 9, 10, 514, 11, 515, 516, 517, 16, 148, 518, 17, 203, 519, 278, 520, 54, 0, 3, 521, 522, 523, 524, 525, 204, 526, 527, 205, 528, 207, 27, 529, 530, 531, 532, 533, 534, 18, 535, 30, 23, 35, 208, 536, 537, 538, 39, 539, 540, 541, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 209, 560, 561, 562, 563, 210, 564, 565, 566, 211, 567, 568, 1, 569, 283, 3, 570, 288, 571, 572, 573, 574, 575, 576, 577, 63, 578, 168, 579, 580, 581, 64, 174, 582, 583, 584, 179, 585, 65, 586, 66, 73, 587, 212, 213, 588, 215, 589, 217, 590, 220, 591, 592, 74, 75, 76, 88, 55, 593, 101, 103, 594, 4, 595, 218, 596, 597, 206, 598, 599, 600, 601, 5, 602, 603, 6, 604, 12, 14, 605, 606, 607, 54, 608, 609, 610, 222, 611, 612, 223, 224, 613, 104, 614, 615, 616, 617, 618, 619, 225, 226, 620, 227, 621, 182, 622, 228, 15, 623, 624, 625, 626, 627, 105, 107, 628, 629, 630, 108, 631, 109, 114, 115, 116, 229, 632, 122, 633, 634, 2, 635, 123, 125, 130, 636, 637, 231, 638, 639, 139, 141, 143, 144, 145, 56, 640, 641, 642, 232, 55, 23, 643, 644, 645, 146, 7, 25, 30, 646, 647, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 158, 4, 661, 662, 663, 160, 161, 159, 664, 162, 233, 58, 169, 170, 172, 173, 665, 178, 179, 180, 666, 181, 182, 183, 667, 6, 184, 185, 186, 234, 235, 59, 236, 237, 668, 61, 62, 184, 67, 68, 69, 669, 670, 8, 9, 671, 672, 673, 674, 675, 676, 677, 678, 679, 680, 681, 682, 683, 56, 57, 58, 684, 685, 686, 687, 688, 689, 690, 691, 692, 693, 694, 695, 207, 696, 697, 698, 699, 700, 701, 702, 703, 704, 187, 705, 188, 706, 707, 708, 189, 709, 710, 711, 712, 713, 714, 715, 716, 717, 718, 719, 720, 721, 722, 723, 724, 725, 726, 727, 728, 729, 730, 32, 35, 39, 52, 731, 732, 733, 734, 735, 190, 736, 191, 737, 192, 210, 193, 738, 240, 739, 244, 740, 741, 194, 742, 56, 743, 744, 745, 746, 747, 225, 748, 195, 749, 750, 751, 752, 753, 754, 755, 756, 757, 196, 758, 759, 760, 761, 201, 762, 763, 764, 765, 766, 767, 10, 768, 769, 770, 771, 772, 773, 774, 70, 7, 202, 203, 775, 776, 777, 778, 779, 780, 781, 782, 783, 214, 59, 219, 221, 784, 71, 223, 224, 225, 1, 226, 227, 72, 229, 230, 233, 234, 235, 236, 237, 238, 239, 242, 247, 785, 289, 786, 245, 787, 0, 0, 57, 59, 788, 789, 790, 250, 251, 73, 254, 256, 74, 292, 791, 61, 792, 222, 243, 248, 249, 253, 257, 258, 793, 259, 238, 794, 241, 795, 796, 797, 798, 799, 60, 263, 75, 800, 801, 264, 266, 8, 802, 295, 267, 268, 803, 79, 804, 299, 805, 269, 270, 271, 272, 806, 807, 301, 808, 246, 809, 273, 274, 276, 810, 811, 247, 248, 812, 249, 813, 814, 815, 250, 816, 817, 818, 251, 819, 820, 62, 252, 253, 821, 822, 258, 254, 823, 824, 825, 826, 259, 827, 260, 828, 829, 830, 67, 261, 831, 262, 832, 833, 834, 835, 80, 277, 278, 836, 81, 1, 73, 82, 83, 77, 84, 78, 85, 79, 837, 279, 280, 281, 838, 839, 263, 840, 282, 264, 841, 842, 843, 266, 844, 88, 101, 61, 283, 284, 62, 308, 110, 268, 845, 63, 846, 269, 847, 64, 285, 286, 2, 65, 287, 86, 288, 289, 66, 290, 848, 291, 311, 849, 850, 1, 851, 310, 852, 853, 292, 80, 270, 854, 855, 293, 294, 295, 271, 856, 857, 272, 858, 859, 273, 860, 861, 275, 87, 296, 297, 298, 86, 299, 300, 0, 276, 301, 302, 862, 303, 304, 305, 282, 863, 864, 865, 306, 307, 866, 308, 67, 87, 309, 312, 314, 315, 316, 867, 317, 319, 320, 321, 310, 311, 868, 322, 324, 325, 327, 329, 330, 331, 334, 336, 337, 338, 339, 869, 340, 313, 318, 341, 323, 326, 89, 90, 91, 92, 93, 94, 95, 99, 100, 101, 102, 106, 110, 111, 342, 328, 333, 335, 343, 344, 345, 346, 347, 348, 349, 1, 870, 350, 352, 353, 354, 355, 356, 871, 357, 872, 873, 358, 874, 875, 359, 360, 876, 367, 361, 877, 277, 0, 878, 363, 368, 879, 880, 362, 881, 112, 882, 883, 884, 279, 280, 364, 365, 281, 370, 313, 371, 374, 885, 886, 372, 376, 377, 378, 887, 285, 375, 379, 381, 382, 384, 114, 386, 387, 68, 388, 888, 380, 383, 390, 392, 393, 889, 394, 890, 891, 892, 286, 395, 396, 398, 400, 893, 894, 895, 401, 896, 402, 403, 69, 399, 113, 404, 407, 406, 408, 409, 117, 118, 897, 410, 898, 287, 899, 411, 412, 900, 413, 405, 414, 416, 2, 901, 902, 424, 425, 426, 427, 89, 428, 903, 431, 430, 432, 433, 434, 435, 90, 436, 437, 439, 316, 440, 441, 318, 442, 904, 443, 417, 418, 905, 906, 419, 907, 908, 909, 910, 911, 444, 445, 11, 912, 913, 422, 446, 119, 120, 121, 447, 91, 914, 915, 916, 290, 92, 296, 917, 918, 453, 420, 919, 920, 3, 921, 922, 923, 924, 93, 925, 124, 926, 927, 928, 448, 929, 4, 930, 931, 450, 932, 933, 94, 6, 934, 935, 936, 126, 937, 938, 939, 940, 297, 941, 942, 95, 97, 943, 298, 944, 449, 455, 456, 451, 452, 454, 457, 459, 70, 0, 458, 1, 460, 2, 461, 463, 464, 71, 465, 99, 2, 72, 466, 468, 462, 467, 469, 470, 127, 471, 472, 473, 474, 475, 476, 477, 478, 479, 480, 481, 482, 483, 484, 485, 486, 487, 488, 3, 300, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 501, 503, 504, 304, 505, 302, 506, 507, 509, 945, 129, 510, 511, 514, 4, 303, 508, 512, 517, 513, 5, 518, 946, 519, 515, 305, 332, 516, 521, 522, 523, 524, 525, 526, 6, 343, 527, 528, 532, 530, 531, 533, 534, 535, 538, 544, 546, 554, 556, 520, 947, 948, 529, 536, 949, 950, 951, 344, 537, 539, 3, 139, 141, 559, 952, 557, 1, 953, 954, 4, 560, 567, 142, 100, 12, 540, 955, 541, 542, 123, 73, 956, 957, 563, 564, 569, 958, 345, 959, 960, 346, 570, 961, 348, 7, 962, 963, 349, 964, 965, 966, 143, 571, 543, 572, 967, 545, 547, 968, 350, 969, 573, 323, 970, 574, 971, 351, 352, 575, 548, 549, 972, 973, 974, 975, 576, 976, 977, 978, 353, 979, 980, 144, 981, 0, 982, 983, 984, 354, 985, 986, 987, 988, 989, 990, 145, 101, 102, 103, 147, 148, 991, 150, 151, 153, 154, 992, 993, 104, 994, 995, 74, 996, 997, 326, 998, 577, 550, 551, 552, 553, 555, 558, 328, 999, 155, 1000, 1001, 124, 75, 1002, 76, 1003, 5, 561, 578, 77, 579, 156, 580, 105, 562, 565, 125, 566, 1004, 1005, 332, 1006, 355, 1007, 1008, 581, 1009, 582, 583, 1010, 584, 1011, 1012, 357, 106, 1013, 107, 585, 586, 587, 588, 589, 590, 591, 1014, 593, 1015, 595, 157, 1016, 1017, 568, 596, 1018, 1019, 598, 1020, 601, 1021, 592, 594, 597, 599, 1022, 600, 602, 603, 605, 1023, 606, 1024, 607, 608, 1025, 1026, 1027, 609, 610, 6, 7, 611, 612, 613, 614, 1028, 356, 1029, 1030, 1031, 359, 615, 1032, 369, 1033, 361, 616, 617, 1034, 1035, 1036, 618, 108, 619, 620, 621, 622, 623, 2, 1037, 1038, 1039, 126, 78, 624, 79, 625, 1040, 363, 626, 1041, 1042, 1043, 1044, 368, 628, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 1052, 631, 634, 1053, 1054, 636, 637, 1055, 633, 370, 1056, 635, 638, 1057, 641, 1058, 1059, 163, 1060, 1, 1061, 1062, 639, 642, 644, 645, 643, 109, 9, 646, 647, 164, 371, 13, 1063, 648, 1064, 1065, 1066, 1067, 372, 1068, 373, 1069, 165, 166, 649, 80, 1070, 1071, 1072, 1073, 1074, 652, 1075, 650, 1076, 653, 376, 651, 374, 654, 1077, 655, 110, 1078, 1079, 10, 656, 657, 658, 1080, 659, 660, 1081, 662, 1082, 670, 661, 378, 663, 111, 1083, 1084, 11, 1085, 672, 665, 390, 1086, 394, 1087, 667, 167, 1088, 1089, 168, 1090, 171, 1091, 397, 1092, 404, 406, 1093, 1094, 81, 669, 1095, 1096, 1097, 1098, 0, 1099, 1100, 1101, 1102, 1103, 671, 1104, 1105, 112, 408, 1106, 1107, 1108, 673, 674, 675, 82, 676, 1109, 677, 678, 1110, 679, 1111, 1112, 680, 1113, 1114, 1115, 1116, 174, 681, 682, 1117, 1118, 683, 684, 1119, 0, 1120, 1121, 1122, 175, 8, 176, 685, 686, 1123, 687, 177, 688, 689, 1124, 690, 1125, 193, 197, 1126, 407, 333, 1127, 691, 1128, 692, 1129, 693, 1130, 1131, 695, 694, 696, 1132, 12, 409, 1133, 697, 198, 1134, 698, 1135, 699, 410, 700, 411, 412, 1136, 413, 701, 1137, 1138, 335, 702, 703, 1139, 1, 1140, 1141, 415, 1142, 1143, 113, 1144, 115, 1145, 424, 1146, 425, 1147, 83, 3, 4, 704, 705, 1148, 127, 84, 426, 1149, 427, 706, 1150, 9, 1151, 199, 707, 708, 1152, 1153, 709, 200, 345, 710, 711, 712, 713, 714, 715, 128, 716, 1154, 428, 717, 118, 1155, 119, 1156, 1157, 1158, 212, 1159, 1160, 718, 719, 13, 14, 720, 15, 1161, 721, 722, 723, 725, 1162, 726, 727, 17, 728, 18, 1163, 729, 730, 1164, 213, 731, 1165, 1166, 732, 733, 1167, 724, 429, 734, 735, 338, 736, 737, 1168, 1169, 1170, 738, 739, 740, 741, 2, 129, 85, 121, 742, 743, 744, 1171, 1172, 745, 1173, 430, 1174, 341, 124, 125, 0, 126, 127, 746, 747, 215, 86, 87, 748, 749, 88, 750, 216, 89, 751, 1175, 752, 753, 754, 755, 756, 757, 758, 759, 760, 762, 1176, 761, 763, 1177, 764, 1178, 1179, 768, 129, 1180, 217, 187, 1181, 1182, 431, 765, 432, 1183, 766, 767, 1184, 130, 1185, 1186, 769, 1187, 19, 433, 131, 1188, 1189, 770, 771, 772, 8, 1190, 1191, 1192, 20, 434, 132, 1193, 773, 774, 1194, 435, 2, 223, 224, 225, 436, 437, 1195, 775, 1196, 1197, 776, 777, 90, 778, 226, 779, 782, 133, 783, 784, 785, 1198, 786, 787, 788, 439, 1199, 1200, 134, 1201, 1202, 1203, 1204, 789, 790, 1205, 791, 440, 1206, 1207, 1208, 227, 792, 793, 794, 347, 795, 228, 1209, 350, 796, 797, 1210, 441, 798, 799, 800, 364, 801, 9, 229, 802, 10, 11, 1211, 803, 804, 1212, 1213, 1214, 442, 1215, 460, 1216, 461, 1217, 1218, 469, 1219, 1220, 135, 1221, 136, 1222, 1223, 1224, 1225, 1226, 351, 230, 805, 1227, 365, 130, 472, 91, 366, 1228, 806, 807, 808, 809, 810, 811, 812, 373, 1229, 813, 375, 814, 1230, 131, 92, 815, 1231, 1232, 1233, 233, 234, 816, 817, 818, 819, 1234, 1235, 820, 821, 822, 1236, 823, 824, 1237, 825, 826, 1238, 827, 473, 10, 828, 11, 12, 1239, 1240, 829, 830, 831, 21, 22, 235, 832, 1241, 236, 1242, 93, 833, 1243, 834, 1244, 1245, 1246, 835, 1247, 836, 837, 1248, 838, 839, 840, 841, 842, 843, 474, 844, 1249, 137, 1250, 845, 13, 1251, 23, 846, 138, 1252, 1253, 1254, 1255, 1256, 475, 847, 14, 1257, 139, 479, 1258, 1259, 1260, 1261, 1262, 480, 851, 1263, 476, 481, 1264, 482, 1265, 1266, 483, 1267, 1268, 1269, 1270, 6, 14, 1271, 1272, 1273, 1274, 237, 1275, 848, 849, 850, 852, 1276, 853, 854, 367, 12, 238, 239, 1277, 855, 856, 13, 858, 368, 1278, 484, 485, 15, 1279, 17, 1280, 242, 1281, 1282, 486, 1283, 1284, 1285, 142, 143, 7, 8, 859, 860, 861, 857, 487, 862, 369, 1286, 1287, 488, 372, 1288, 1289, 377, 863, 14, 866, 245, 867, 1290, 94, 246, 247, 489, 490, 869, 874, 870, 871, 875, 876, 877, 1291, 872, 873, 879, 1292, 1293, 1294, 1295, 15, 881, 1296, 1297, 868, 878, 880, 1298, 1299, 380, 250, 251, 254, 1300, 883, 1301, 188, 1302, 1303, 24, 493, 1304, 1305, 1306, 1307, 494, 496, 884, 491, 1308, 1309, 885, 1310, 1311, 1312, 1313, 497, 499, 886, 500, 1314, 1315, 1316, 255, 190, 1317, 95, 887, 889, 1318, 0, 256, 890, 891, 501, 261, 1319, 892, 893, 894, 1320, 895, 1321, 1322, 896, 897, 898, 900, 901, 381, 1323, 1324, 904, 1325, 909, 1326, 502, 1327, 1328, 1329, 1330, 385, 387, 389, 1331, 96, 503, 504, 390, 902, 903, 905, 906, 907, 910, 911, 1332, 515, 35, 1333, 144, 145, 1334, 912, 1335, 1336, 920, 1337, 913, 1338, 1339, 914, 16, 915, 916, 924, 930, 1340, 918, 516, 1341, 1342, 919, 934, 936, 521, 1343, 1344, 522, 523, 921, 524, 1345, 1346, 146, 1347, 937, 525, 922, 526, 1348, 1349, 147, 1350, 527, 1351, 1352, 1353, 148, 923, 1354, 528, 925, 1355, 927, 392, 928, 530, 938, 939, 929, 931, 932, 531, 1356, 391, 393, 1357, 397, 933, 400, 935, 1358, 150, 151, 153, 1359, 1360, 941, 945, 942, 946, 947, 943, 949, 1361, 1362, 1363, 1364, 944, 1365, 950, 1366, 534, 1367, 1368, 156, 1369, 1370, 25, 1371, 157, 1372, 1373, 26, 191, 951, 1374, 2, 1, 1375, 948, 955, 953, 404, 408, 415, 417, 532, 533, 954, 418, 1376, 1377, 1378, 262, 263, 1379, 956, 1380, 957, 960, 961, 1381, 959, 962, 963, 964, 265, 1382, 1383, 27, 554, 1384, 1385, 28, 569, 1386, 1387, 275, 160, 966, 967, 968, 419, 1388, 420, 969, 283, 284, 285, 535, 538, 286, 287, 971, 288, 1389, 972, 1390, 973, 974, 570, 1391, 1392, 572, 573, 1393, 1394, 577, 976, 16, 977, 540, 544, 546, 550, 1395, 1396, 978, 979, 980, 289, 291, 1397, 579, 1398, 1399, 580, 1400, 290, 421, 1401, 1402, 1403, 981, 982, 1404, 1405, 983 };
    protected static final int[] columnmap = { 0, 1, 2, 3, 4, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 6, 1, 21, 2, 22, 2, 23, 24, 25, 2, 2, 7, 26, 0, 27, 28, 29, 30, 31, 32, 8, 33, 34, 0, 35, 29, 36, 37, 38, 39, 40, 9, 6, 9, 41, 14, 42, 43, 44, 31, 45, 46, 47, 48, 49, 18, 50, 8, 51, 29, 1, 32, 52, 17, 53, 31, 54, 55, 34, 56, 41, 57, 58, 59, 60, 40, 61, 62, 0, 63, 64, 65, 2, 66, 3, 67, 68, 42, 47, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 38, 80, 81, 40, 38, 82, 47, 83, 84, 6, 85, 71, 86, 51, 87, 88, 89, 90, 42, 4, 91, 0, 92, 93, 3, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 46, 58, 107, 108, 109, 18, 69, 110, 111, 70, 112, 72, 4, 113, 2, 52, 114, 115, 24, 116, 117, 2, 118, 8, 46, 73, 119, 120, 121, 122, 123, 3, 124, 125, 126, 127, 128, 129, 130, 9, 131, 7, 74, 12, 132, 133, 75, 90, 134, 135, 136, 100, 137, 106, 1, 138, 139, 140, 141, 142, 143, 0, 144, 145, 146, 147, 148, 149, 150, 151, 91, 152, 2, 107, 56, 153, 154, 155, 1, 156, 3, 157, 158, 0, 159, 160, 161, 162, 163, 3, 4, 164, 165, 0, 166 };

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
            final int compressedBytes = 3282;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXb1vHccR31ufmBORwCvCkohUJ0FWGECF7MaJGh3lOJYDBB" +
                "EoEYgREAZSJCpYBa4EFUtaCKhULFSofFAhwJ3/BMJQIbhSESOG" +
                "/prsfbz37mN3f7M7d+/JZ4siNZzd2ZnZ+dy99/b9f+xf+vbG+Q" +
                "fnjm5894sXB2e3H39cPPnixwv6/g+3Tq6Jt3+5vn/p6cMvPjh3" +
                "dGjg+yV898mff7xwev9tDb+2f/npzl+flfBPX+y/+uTxXoV/ev" +
                "+nW3oE/PevA/r8+NL8p4RIRPlsmD/b926ar1qIQspMEOjjrn9i" +
                "/gD86smF50n6/6DaP0xN/9mvrv3h3NFO9oEQN77bPb54trGzJ3" +
                "T2mSGiEHoV/Ef6d83Mb+gr9e89M/8nj7ca/Xs9xzfwuxV9bTiZ" +
                "PwAO9f+fB5efHv7tvx9+c/ji85dfvbr9+O+f/vvgfyX+IwPf3B" +
                "QXtzKj6OeOCiHzQnz0tSqefHaa6L2TW6V8mftrDPmnRzvbz0r5" +
                "J8cXX23sbHXlP+3++vWmNPy5mxj+GPtg+PP7r28WT3ZL/vznVi" +
                "be+f1Noc+uv3uV/iL7ML19x/uLt3+BfjP1v9afrKM/uy39EZlI" +
                "q6/NY35Qsvm+EOv3P7It7KL11TySRN+09petn1C+6/b/64UPHt" +
                "3nL5c/7zh/J/d/PP/etR/pPD4r2vrvjT+c9nWvgv+whB/a/MPk" +
                "6588PkLxGTu+Tu3xc0KyT1smMlNLg2uSlQfKfC3zk8b+8vS7ke" +
                "9dl/+f2r/D/Iu9/0jxZZ1fJFV+0Ysvf9PEz0eHs0X8XK/vETU/" +
                "fVrTP6vpvzquf5k2fpSFTK3yUbtbpXzY8KH887b8Eb4lP+3nr6" +
                "kvf0VwlH8i+Pc1vM5fdqv8ZQGXI8BxfuyXP3f8EPxZjX8nhD5c" +
                "n0H+DcX3S/83q/FlO7/g5scIPwIuOvNPnD/P8d32cTXzu/Db8F" +
                "kNl224LX9v519IflPjv4vwgoefd+ojrvjydO81pf5Q+advDzv8" +
                "3a31Y+n/PHCjBSmqX9r3fzM/Sb5ZtH5EjJ+H1A+48O7+fFntzz" +
                "vN/nykr7HxueP34aIFF/b1iTHXZ02Ki2784I8vQf6dFQI/Gv6G" +
                "cvw7qu/AJ/ODufnruuE2/ejKZ/T5RRh9PPuI8n93/lr/7Y6f9k" +
                "LyT2d9oQ2Psc8W+kU3//bnPyj/bgaSyxpk2tlqCJ8L761P9rf6" +
                "BPOLEHxufaGNb5N/Kz4Wy/i58o/PbwlCfinkL335BYLD/Jk5/u" +
                "afnOur4n+uf0H4bfishnfi62BXFAwvgJMB/lHy/BekL4tYaGIx" +
                "pyqTbbSs+UZmfv018YEWaWl+akIyLbbLv3cqg6Sa+CFfkpr2iM" +
                "/mHMrmZKsx5VfRr0r6ZU3/85J+ZX7rrKGfznoZIz+wPjZ9pPjM" +
                "pz9FfyEqRL+QfVvuD1WtNo6+vv6o5hczTF+fv9t9/rr0UzfCQ+" +
                "vzG4COQ6x+uNdsrsKuT9z9jfC1Vb4pf/yMY3/T6exfAfY0tG+Q" +
                "pMQj+/j9rWn7E/J3bt+F1b4343vlnzrmTSn8j4KreP3GT5B9Y+" +
                "8/Ae2rrI8Q9P1DToNz/R93f2Vg/0L7XfD8G9M/Eex7QDXDwn9o" +
                "X6D/kj752eMzj33RgfsL6wfS35HikzbzWyW2zIVPs/8DuGbqn6" +
                "baFxFoP+L0K1i/++Oj/ifQX3w+IUk5/VNu/xHpxxX1kzQ2K72q" +
                "NmTyO/Oz1onIVZ6JWS52Q+JvRatoajtJzvpplHyW+5/bP6Qpeh" +
                "Zt33W/KBx8/nOU8xnboD6x7apPrIo+V3+y4i/Jpjv0k5lfcvuT" +
                "Q/v1wJFfOlQN0I/6g4g+Z31aDcfv1A+J40f0F4fnI334UL53vP" +
                "YL178Lf3qH5DNx/y4Uv9/fi8AXYfgFJT7DVajo+pHqkt3LH4Pp" +
                "D7Qf37frwz3/Pj+f5IPj+nGI/oWfTw7T7/D6OCUlGWqdIsP5/c" +
                "dA+Q+CXn/9Ato3t/FRNPzCbf8yvnxx/IPix8K7zwPPB1P6dyKw" +
                "Pyh8/UlL/9JWZprDpQV/rfAR+rde/kfIL6w/BS1GTP1FWeqXkf" +
                "Ej2f6290dGtr+M/ufz3vlIKlz0+sOoP+uNfy34CB7UP0b9Xwtc" +
                "tOH8/vZI/tFwUVnzGwH6l7i/V2rYFYMxqH9tYP1D50+a/OA98a" +
                "Er/2D2/4j1PXL9bBBfFCD+oOIT66+h/b/Q+qATLq3zL+pHZnJb" +
                "/eiKet2Fixou+vDMDhfibuUBSjtR6mpSzVvZ33wZKMmFTVzqN1" +
                "H/0fjl+lNf/cy/PkQfXD+gH9kPMv978hN0+VX8uWrWGEP/HF8O" +
                "8DVJPyD/G/m55MvVzyW+f/1x+jnC+tH8A/6I4f4i0b/pp9/FH7" +
                "D/kH0h9xdUXH8ror9+M6T/wJ3f2Hme/9mpEY1/16k2fkNLlZXs" +
                "TQz7L1n68zpwQQg/K1qctJWgzpj+k8kf8aaL39SSM3L/i+n/wU" +
                "TQ/58g/3cPiPNeWP98cL6tCNNvHco/IF94fgfpB1V/4uBIftz+" +
                "XjmGms8/P58l2vk5Lz7k0meXv4qHjxxf4/4KvH8Bzg/E1B9GtC" +
                "9c/jDHx/WfiemD58+KifkPlgPz93tNjUxKq3+aXP4wvy7/N/Rt" +
                "dvU/1Ukyqn8MfYj6geIja1FXgfjHftQwLj5dyJ8Wn9j9awr6J5" +
                "OtD58/Idm/NFo/YH1g2v2D7u9w/TP9fEtcfhJXf+La7xXGJ9bz" +
                "eSH+udffI+vfjFTfGaG+q+toRHbsc1bN76FP1uPrGicrkU6X/Y" +
                "lidf5/zf4B5i+B+X1w/sOlH4WdK4sfEmrEFeq/tUhmvz1+aFC+" +
                "1Fs6eTZTs+yPO3mqN8bIT9u1oJ9jfE6Rj+X3i3RF+eFZydY0r5" +
                "xLm548EC7j4Iv8zdH/gvMXomNfHfCA+oUK4w/i75su/iJ+pK4f" +
                "5q9e/wTjB7N/zQyzK8LsX3E+N6OdahMeFju++YPze3C/Y9Xwgf" +
                "zp+KT6SED8RDr/OPb+H8++xd9PiYkfckb9xpG/raw+Y58fx/eg" +
                "/nsijnNPfGueCxUoLUVk8pO8pGhXJVqdbNPor+xX9/xch38A7o" +
                "rZx6lvIflkneuVueWlN9T4RDrrL6z4l7s/+P4jBi7J9sdaf233" +
                "n3j2MyC/liup72omPDi/4u0P9vuV0fuT2PFpYP2EW18e2b8b/h" +
                "xzzpeM2J+QTPg49euBN2fGN7i/463/8O8PTx3/ASPA7J9NvT/R" +
                "/Tc2/yav300cvzPvtzlzl2aLQnwy/+fno/vT3WHK7/oBeP/bAe" +
                "v+2ZrlB+83Yf+B3g9JeH9SAH80Z//Z1qf88Re5fi7s9XM4ip9+" +
                "eP47s7x/pznfUT7o/hjFv/vG59o/RB/bPo7aP3oZ59/D9aMg0Z" +
                "cJvv1h2r+p4yPYH6K9X4dz/8o/PvP9TIT3l7DuZwnm/St4vwvI" +
                "j3A/7KO6/jNg1kn9YyEblvvqOz74GvordP7C83kTx5/c+2Hs9/" +
                "dJBPbTR79/5znfkrrhV9Tz6nxKLurzKfnifEreO38ed/+YXp/z" +
                "40e/X4s7P9o85PfnhZzPXj7ofAs8n58RFdFVXxWB+IHxZZz/o+" +
                "OjzycY3L8Y3i9h1Tdc91c85786+oXv//j3t//+iqLEn5pZPwuN" +
                "b5VPuxLn+lzn20Tn9F8C9rcL7sKn30/q879LP4a77jfZdh9V/u" +
                "8SPFsz/s8WjvzTKPsf3Y/z6C/Y3/D+60jri1v/WPcfY/f/eP5p" +
                "0+GfTpr4L63Gz6vxy8WX8R+uPwTcD4yqP9HjG3v8xe7vkP2X6/" +
                "5fIDwyPia/vzk0fmW+H33y+Joc/3P5g3ppzvN1Y+cfgfLn1QfY" +
                "55u49EH8N7z8E9hHdv67+N51/gnB/f09Qn2MWP9B+YM/vnaZd6" +
                "w/gfdvh/I7znz+vT++JX+8/PThlxV9nxv6bj/+Vy//lBz/gNev" +
                "iflzpH+Lys9t75eOy/9X/vnifUWauP4wQn+cd35slPMN8faTzb" +
                "9s6attny9DG9+Nv+77Vfjzndz9RylGuj+crq8+5z5/NVvEz8P7" +
                "s6q8P9tbgfTZJzd8MX4vPpzfz3XPT4PT5h/a71SMsz42HPfvuO" +
                "sXnvP9xPglBf1FwufPxMZvzPyr+YisvFjqf0lsXt0v1GJwvzk6" +
                "fs1A/CpAfCpo8WdofhI1f5uthYW/dP6j+A7ez3bGN8T4iKv/tP" +
                "wx8H67oPuHNfd34P1udv+C23+aNr5F53MI73dG9sP//md6fSLw" +
                "/rez/0PDl7k1P09A/JM48xvUX6r93/lqfuWsDyS2+kpKsIWD92" +
                "N3Brbcf5xR5eO4/zmjjY/zfzS+oz4WOn87v+/wH/EHwJXjfugs" +
                "qP8mCCc9IutHfvqgfhUi6NGh+Ch+LCwKryR9fgxPQjxWxPiE+t" +
                "R08S0z/h2h/zHt/RkE/z9GrIAd");
            
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
            final int compressedBytes = 2875;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb1vVEcQ37d5Ng8rEYsVwEq1RFZ0BQVJQ+KGZxISKCKQDV" +
                "IokCgpqKJUkYu15cKkcpGC8kSBRMef4EgpLCo3SMh/Td7Hfb27" +
                "3Z2Znd07y+EJYfvm9mt2dnbmtzvzRCG8z+mDbx5fffn8/pdLuy" +
                "/eXXj9+Oj2zneb+79+uHy4fbpxsC5EIfZELoUWbUWFEWv1z57o" +
                "V/8rIfj1Bz4mDh1qv7D+OvqLPb5zQy/sbKfPrzqf/ElEHz3Szs" +
                "LU5Rfdf5hu7PpLo/TXHOjNwPV4/QwGr8+FfJ4+WK/0Y6+o9OON" +
                "d59V+vHWzmq5f//DZbN1vGHWR+Pvzn/M9nn6Od7+JedLL8f8v/" +
                "ay99vfdf9/fP3431s7Ww3/D7c/1vyH6Bb9rgD9T6PD69+/PiLp" +
                "Pz1TXuP2J+gr4PiP6oZzV/vg/IiTQflle//R+nHFbt8B/IX7V/" +
                "cuxvqG7ItEdLR9ApWXWPmg9h+5vwXXj9yfAu2/xfMvkny41kfq" +
                "/accrTA7f5n0iRUctn799Z9eqsb39sbFR7V9cOH103p8rX2w/b" +
                "4eH35/mpP/aUI3aAlsIWH7VwT/E/LvvXRZ3BFKiUwoKZst6NXD" +
                "SjEoU+9rsioiC9nS6/HV9LUuHdG+X79Z9xdFn57w+fH3nwpLjO" +
                "o3CemQWDDqPxPj+x/R6fKnSPbD0JFw27e09q3LUfpE83LzSZ7V" +
                "NkdZd6sUmyoz6mAtAr6XBp88w+tn6uNVIZbUeBoqZf2oUur1t8" +
                "p2Vrj+bYEZy0R5035WxNLP7Pp55VH+kbf9EsRX/PUzy8P+BQtf" +
                "A9dfgCHm1TtmHviOCqDbFxBefqDBh/WP3L6J2//TSy78cPvYjS" +
                "+oBPPjtP94+LLIchy+4Hq4+gExfit/TRT+HH2x/tPSbjW/Qtx4" +
                "t7l35Wi5tyVMcVfVA0Por+T6EfJPAfrA/w3vP1B/zb98t7fW8C" +
                "9r+Lfa4V+QfYfnHxv/E2n5A87PTP05qf2vVuSV1eJeJpZ2K/+2" +
                "sk9/+OPm5v7mYWa2/tooYDokH9X+9PTq2xed/m+2/D219z9yeb" +
                "b8Nwyd1GJKkuATiny+aeTvzkD+/sToD6h/wPoB20fbvwMGON2W" +
                "sP1poB8Kp36YLo/xP7IA+AusH0l3EoLLs9oP03/x7A9ofm3yGe" +
                "4fvEngv/j1E0xP7P+M8IeZwRz4VgO8koyHPxD/EEeQ3ebl+Dgz" +
                "V1O7XTbd0sC6M51PVSD+D/QP9v9A+UnkP6bEVxntzwVfJfQPxP" +
                "+I8kvFJ510JP4ITBQXHwuTT8r5RWr7naufQugUfCaxf8j0n/j4" +
                "QIlY6xjYOxU+2j56Rj51NPylbDZZPVtkGcMfqH7jpa+siMp/q7" +
                "ixtFst+Mp/+/YPVe7frf23g4154Eud8enZ74Lns/7zV/B8lru+" +
                "Afm+rj7KygjJv64az76v/jammmqlC9HXYtPhklv3J+mo/7hbv2" +
                "jrF4P6F02H9ANYXtxr+FNvTXUVWcOXZn/Wo/np0EWHDteP0Dl5" +
                "OP6H558M5X/TP1d5IY69/OPyP0L/gfZLVvtR1idDv3Hrv65eNe" +
                "W1WJZ1eY0qz8AHPtHnTMf6DzJMPwH7P0wXZ7w8QNdDEEOb3NT7" +
                "vhRFvXzqjeLqJD4deD/zrI+/OOKND+JPry2YiV7LXyNVh79o+8" +
                "lef8D5alT7TYgTnv9QQKqB6P/M+l/I+0mB/lFw/BESH8E05Dlf" +
                "g+x77v1aIR7S6AbTf8r6htrH6geISLU/dGN/8Pmb2r8E1ycP/x" +
                "jYp+nwoyNgfBB9iH9nbWN6GuBE9k+F4vMh+pdw/yIq/kCnc++f" +
                "gPglfP7MxNcWgX8Sxp8Y3y3r88V/dFEt4Rs6M1eOlovV6uO7zT" +
                "ei4MPY84eE9/s595si0zOqf1QMRXzKPsynRyhd5zf1v3wmPjE3" +
                "WYZZH6PyKxPl1UR5oH2AXuP/114+f9LI7y+V/N7e+d1yfuKpP/" +
                "H6hf0n//4J+gc2BFRZLaiEdL/95isPx+808asjPW0WMr7F8Wd6" +
                "/HR6dzkO1q9OND5y/Vh8UbX2Mx1ftJ1/dfwzQ8pfY4C9ily/DR" +
                "+YZ36NEpjfMkA+KP0rAYZGsB88/u/IP1Mtvtz6Z2Lkn/HxRzX8" +
                "Yb+/B57/AvzB+F85wp119Q99ft3u7xeb9hUiPrOlo9Z3Pm3/dO" +
                "wT3v1LGB/i3S9x0eVE+Ul9OdCfRVO+M91e+REz7ePk357fYXb+" +
                "nPUHtY+fP8T8MPE14civ0Y8z/+D93Vjjs+fnCLufOX7et/E191" +
                "z5ed6P429e2OgwvmXVn1Lg9Q9v/eLsMzc99f2f5PjC2caHqIZ0" +
                "hsJK3fVD5bPo5VPzh1jekFnGbH/R8jkH/MhLN2dwfJ/oc6PrWg" +
                "Ky/sW9Z9UvT4wS2X5f9YufeyI3Ue4vhuKbn+Zn/vujDphfSD4S" +
                "2w8FdiNx6U9/fjs+3cPbuayvI3/+PXT9rv4D44fvZ3T7N8KvdE" +
                "L5o/DXLJjO7J9uONDPxLMaHamZaozSRdnDyl+E/p/r+22Lpofq" +
                "xw7+mSfT36nzHyfff7D+FeN+qWd9wPEPGP1tGU+ZD/nPyq8H86" +
                "e04LtT/M+p/hfZLc8F6VECnX+fKV9Fh2V68usGNdDU+c2ZdHL8" +
                "Dhrf6o/1Q5T7mYH3J/n3P2H9KHz4fMmt37I+Ju83cPOnCOb9Ok" +
                "D+Eq9P9vyj8d/A+5/W+hW+/iT7qyDod978IPKjIc+/oflbcfPX" +
                "s39D+aPY9weT5x/yr2+Ufih9/XPkTzscnN8A/QPzFzHzI4H5PZ" +
                "j5tRD5eZj5iYjnf246If8PoX9gfHwE+zph/+DzTT9/+fljMPah" +
                "Hv+aT23+sH2Yi3F+FjmdnyUsP/m5yn8ciT9h+WkR+Tug/ECQ/I" +
                "TlTwbvVwT6jyLUf6TgwynO31xeHc/+ybssKyeoBmNCcuN/pJCf" +
                "q2EfWv/uZtt2OYjfSps/KXX8BoxfRqifU55kv2tYlg3SPjNbqP" +
                "y6XP4tmg7Hz/nlE8wfAN5PHOVXkJP5EVz3E4PwBYR/Kjv6mZAf" +
                "AXp/E/Z+prt+7xPO/+H4S4vIUOLrePQo+ZFZdCw+4opPP4HwNc" +
                "A+OLH2jxS/7VkIUeJf82n7xW0/xrm/RcAvF47v+uOr2fyv7NPC" +
                "u36Z8dfU+mfxa8nDz7D7Z7B9x3t/AtV/MsTzlzNJF+j8jBH8Ny" +
                "D+GzLksPh6WHmufc+1f2H/gOs/YPtv508Tn7zbiU+uBqpG8cl2" +
                "+jh+Gf/+cwc+DZ+f7fHiS0rH+dG0BguLr4Xid/nxt346Pj9qqv" +
                "fjMPdvDH6T++27egvT5bh8/XXdxA8ZOH4QgW9564cUKdi/mfLK" +
                "b8G55Fc4/EMofuxhV77I9hkWf8dekIhh/xHtF058JhD/yX6/wd" +
                "T5Sj5xvlLG2N/Y7zcJ0T/E8w9Ufgm7fYDgf9L4RVR8GSN+c5p/" +
                "2ax+JL3/M0Ph1xR8GWefwPGFnvNtnH5y+T/M+32gAPvjJ0f0ZR" +
                "edGR+KbT8wvhPy/+D3tzjuV/fx8sWST/v+NTf5mAd/WPbdHOxD" +
                "VPw11/+XiP01F9ZNz3u/hL3/Es5H6HS8f0bBTyL63wR8orAQUP" +
                "ZHzuB/4z/o8Vc99xOKMP6z7t9h8pc5zseOcedjtvM5QWifh3/M" +
                "L74bYkSq99MC8UXw+YO3PNQ/+P2RafEvKL4jin/BOh+PJb/Q/m" +
                "w/P5LFnfb8QknZTPGr2fgK7TM6oPOPA7/+CYvvoJwPgf67HLBE" +
                "Bp7/Qe9XstaPvz/I0z/h7zcySPuB6X8z7ZM070en5Hf2nz8u/v" +
                "5L2vNRwv1MGWR/svOz+Jvn4tv8+1O88yHYP02Lz8WwH/38A96/" +
                "HeT/TfQPc/+/hOx398O9nw/hs+I/mUZlwg==");
            
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
            final int compressedBytes = 1886;
            final int uncompressedBytes = 25185;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXL9vHUUQnjvWZomE2FgJWFQXZCEXLhBNwE3OQIRSICwHS/" +
                "AfpKBCVMjF2kqRULlIQWmloPaf4CJFyjSRIv813N174Hf3bveb" +
                "ndnzs3iNZY/3987MN9/MPrI1xT+u/6sfiC1x25ey9vH5XXy49Z" +
                "053rZ3iHbOi5O7F+vbG+TtQ9c29FsZ2oc+PrpNaBvj63N8Odg/" +
                "Q1QMe3Kz2ft4S8/a//Hx3XJ7tD+h9paSPgWl3q/+9Rw5n3I+pV" +
                "J4Pp5MSdV8ItbTZvtzu/lL939IHhjfMue/QbS2oIEluZ/annw7" +
                "7zJ47I7i1+PmyE3/yOuF3fec87n88fPDj58/eXRn7fjX8/dfHl" +
                "48OPpy7+kPb2+fPr7cfYbtx/j+O7bZuPxoqxm/sT9rxzvn7zXj" +
                "3z/aqJ8+envbH7zbHbM/qfYTy0/G79/ZYB2lrH3wfsf6v/r7Pf" +
                "e6bA7ZfEbrVHzV/E6+oMo1ynFW0R5LgaPye+6vrv+K1su2/8rP" +
                "+rdU8fr/b323Jl5fqP27fns/aA/OXzs+a/+Nor3y/hC9icvV/s" +
                "H15X5gnwf68UE3f7eoHye69blRuV1anxXij/24/eHcD5NgH4NC" +
                "1L4M2vdPnj/55UVr379/efjqwdFvPfuu98/z9Qnxm7p9YD9qM4" +
                "BExayziuVgw2KfGb8q8R/23+h+bDX3Y/vn7n5829yP+0cHnf89" +
                "fTz3v6z4YfPFPH54heMHJ9l2LGcCbB9o5/rxmU3WX568YCN5Px" +
                "gfLUQ7vzz9i9bXrbAcvf/4frLmZ8BdwXKrlNOq5Pv9/fWp/r8O" +
                "nA/b/wL/EW//6a3y7oa1Ba0dE61XNX39+xd7T/dOC3/w565NmT" +
                "+B+Y/rf2vf1v7lR/Y6fuRg3L6VGDXVfYfQyZb5J8fDfzH/m4If" +
                "IL8Bxu/bxwLgnyLqf/7u9PubuX7/0e1v/HzG2vfsw2h84oLzl+" +
                "PjEEBG5wP8jxqfMfxXVO5aQRsfDvD9WdSXWC6+wPE5Xt8q9x/v" +
                "T1SuxV8cFV4+35H4383i31n8TyPxfykcvAb4VeffUXt+/xSwXx" +
                "L8jvhjxzcfueMLafziZOPL+EWXcf0S/r5/PwP85Osc/CTcH7F9" +
                "ZPNDo/aJ5oEy9n8X/fY0aI/3H/GnQn415v/QZ0L+3g+VU2kf2P" +
                "yZdPzM+u2n2v/x+83P/6D4Bqm1kB+F/QP5xPyc1r+y8I3R4ANk" +
                "X3T4BPJbtpu9XYBarhwP90L6G28vrC+AtHdC/BurD4D1A11H1V" +
                "WfZnCLtfm7WY/lfIhmDYbBnyfYH+X4uP6gTqIvRwxpfP2i+pIE" +
                "fnpAZ+THb+zxufjXpflPZf8Z61doNL8vIdIT+FW9fjL1L0Xuss" +
                "lD+WN7lT/W8y/R8eP5Z8b8op8M/avWF8o/8/3TG4Cf6iSrWQT5" +
                "kWH875ntnTa+EehP4v3X1B+w5ZP512j//PynjJ9J5Y9G7LvOfl" +
                "juhQvy1yc6/R1oiE/lx+t5e2B/3az+aMn+BPJDIf0Nx2/R/IcR" +
                "80fB/fPXk98Y6HfBjF/z8eswf6Di1xn4RKhfgD8qY/GzYP1i/I" +
                "TqE8D8kvZ35H6ufH/j+RNt/bs+/6mL73F+mc9f2xEB5pfraHzO" +
                "9q8uhD+fzetLzSy/RLP8EnX5JceML+Tzk+WXcsbPEn4X8/M2Gz" +
                "8P+l9GvC4ZXyN8GOHnS1uSc223jbyd4uZ+MzHn23WVtsefxPmH" +
                "oBzNP94e8svK9z1Ynsm/aOJ7E9UfnX9X8rN6/hbwq1CeQGlNYl" +
                "8y8WvS90taflKpPwz9TI2/0vqf2P5cX/yN+EUpfbB4fhXzfEs2" +
                "f8d632GAfkTk2vHl/NsZ9fPbJOPfEL8GxmfEZ+j9h5K/c8L5p/" +
                "K30vyDjv8X8FsuyT8n6m+RyG9CfgfsP66fzXa+svpBPb83cfxc" +
                "T1ofzOAPkC+K109OvH+Yn7vZ+8vBv1PWr8v4ccpmP1Pso1Xjk1" +
                "z4O2d+A8Xvyvgf4o86er9R/M6O7+X7r/v+AqC/CP+r8+sY3+Ro" +
                "b6L4xsjj7wzv8znsQ6z+RydXx0dMfsdJ11fr399ei/4I8XNS/F" +
                "+B/62WdFn9/RNAjvqH8akSX8r7PyNWfliLzyA+UsZ/nP4V8f80" +
                "7xsWpJ19rWwzhZ2q8A1+s43LoYddD+P2Nf37A1Tvw+pp8483HH" +
                "9Pn99H78v+7+sH/lOZv0zmVzzHf/frh+LvhxA+APg9s33N8z4k" +
                "xT4j+dT5D3S/Jsafqfgy8XzY+YmQf5sYH0F5jvpjlX+L6w/k75" +
                "X9M+pnVfgwU/2EieB3Dv9qwvhKhw9W7r+U/PNNnx/k53Pwd4Zn" +
                "Gm1g/3jvd4X1N9r6kKn5C8gvsPmJaepfVv2+hHn/y1786bK1V+" +
                "evRfWJKfnhDP7TxPhtZf3pqvN3E48v+363FPylzR9A/k+Lf3n2" +
                "LYm/z2g/1PV7hPPz0e9v4da3SO0f5GeV9gfgx5vOL13H+JH9g/" +
                "XV2u/nmFiO4ktu/Bn0z1DOuJ8K/Vx9/KLrn2Efo/ZZe774/HX8" +
                "T5b9jY3/D/Bv8Ow=");
            
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
            final int compressedBytes = 5096;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqlXAtwXcV5vmAM1GPHlDoFQnilwSlxGF4TQ6a0rO6VHMWZAR" +
                "IIpWAjAiTBdYwhdQ0YaHTOnnNlXckzJh0eyuRhDErfgZKETCeJ" +
                "28zQqZliG0iCLcnBAYwfFa8EzKO8+u9++5//3z1SptN6Z/ff//" +
                "X9366uztl7z5V7Jnsm7d/0TLqWreh8vvh0fgw0su/on9szufbG" +
                "/CS2UMx3q9n9PZOD/WZGz2Snt2ey0eiZLA9y9rxv6ACNQ9QPhp" +
                "2QNthlAfM7+YxGo30meyirdGP/hfkiZ2mEf1IRVtER0zXZ/3hs" +
                "i/2EeDznyuhk/qJEa+ziYT++22gMPNxo2AfDmhoxasqDVnSvvY" +
                "9xGg3Ta3rto24k+QT1t+x2aDTfkb1heovb7Y/ZQrZ9PMt+YHo7" +
                "1hxiettLYSkPIv97pnf4cpL7Te/a37Wv20d81oHiayH/7c7hdp" +
                "ddLojF+iDfsI/ZN2klP7e/sLuV/2Dqh4tOGFtoP/dlr0S2nWr+" +
                "LtZFszHqr/nZuH3Hox1i/1xFbqP+BuYDm7z/XeL9iWJmsZA8m2" +
                "lNjSr2WV3PW7YWtOJiRnGofdL05vNNL9XdaDZihLRvseb0fL3Z" +
                "WG51Fh0jbWiGOdhsHL4MdtrPjbSf3tOe8GivI94eGDwJdvu2+6" +
                "l2zxIM2s+ATHv5JqT7V/mpAu2nqup8g8dmr6bcudl3WfccXgN/" +
                "2k+HdoirHa34DczXnub9lDv4HO2nCWtq6KpxnUajOIg67SfvEP" +
                "XlZnmxy40k91P/efFf0Gj+vD3GLG+/Vhxgi1mODO99jPrP/o30" +
                "4slgmaT+a+rbzPLm1SQnildCha2d9SFmOzHY2T5LENtfDJ6nil" +
                "8Vu4tniheKXxRj4i9epP6q6KTtKJ4b7M4ORLZxNf9lsUfYggFV" +
                "3ed9LztmejXF43plyKDols6OVy4Wn/9S8RuOpJ6bHCOkj8lZz1" +
                "43+dq3YZcYaYi2mzhX/MW1Gq3R6NwOeznb2dpfE4yBIwSLc2IW" +
                "9aomHzw/ezvlXs8QDsKk+FK6Yo3AscUFaeV4r4RxeXg5S61+jp" +
                "mD0cmwkjlodoe9huSoWCQWM8nXmpPNq5KIUWSVs/396O8U0qhU" +
                "r3PQKGJpNAa/yXkpL0aSXBmdZGY17NFoD0bTyjErsYT95OhhM1" +
                "x+zI0kP0r9mPIkaDT/A9syw4MzXRQiRbLm9PJY2P38I/A0r/Ta" +
                "KaHCUUN/HTA/SD/RE7vOFoyBBYxVLgBK+QHgSsW0KrF6PP8TN7" +
                "fNgHuC+MsPcQZyA+px3ndy88p4NeXRemWcUVydVha/Xj3hfric" +
                "z5H0G/0X2YziETlTuH/ZqmxO9mb2e3QN/0w2b/Aasvw+nY+OJb" +
                "mabF+l+ck0vzH7iL3N/pXP+Cj1j1E/1V7o9dMHZjJe9vHsnEZj" +
                "6P6G+tc+znsWZb3ZJwcezD6VfbqqfZOcl7Il2dLyXLJd6T1XZ1" +
                "/Krs0Oyg52WvPxvCtEzc2OQEb2furH2H6K/Ut3XspOoH3Z7HNP" +
                "UbU/mJ0Z4j8Rap6bmazZiP4VmeyHj7mQeZX3hOzL9a5lV1GnnT" +
                "IrzUqMkP6nuZL1/AqzsvOGWCRWR9P1U1nQBwy8XGHoAY7x9/dt" +
                "gjiwXzz5ldXv28q4juiI7Hwhvyi2xf40V5jo2ho736BXVbTTyj" +
                "EHwWS+oS8zyzBCet8y1m1plnU2i0VidXScj96zHV6uIDF+P7cI" +
                "Ymeu9lT7uSyuIzoiO9/MPx/bYn+aK0x0bY1N+6lWVfworRxzEE" +
                "zmi949v3s+Rki/2vms24Hu+UMLYJcYaYhOu2sD/6TRBDX8zp0o" +
                "GJ1TtIelZlGv2j2/+Vh+Q8q9niEchEn7hHTFmOcbdEbx07RyvF" +
                "fCWHi7bkpTYoT0e12ybv/BlEOnwi4x0hCddtfWPqHRBDVUVxid" +
                "j2sPS82iXtWUzW35jSn3eoZwECbdtRVjTq9PlVH8R1o53ithLL" +
                "x97zN9GCG9r491+8+mb+hKsUisjo7z0QcvgpcrSIx/fT4giJ01" +
                "2lPx64vriI7IoW/lt8W22J/mChP7wNTYshd+P/8zrRxzEEzmG/" +
                "rd5m6MkN53N+s9Y+buzlOwS4w0RKfdtc6RGk1Qw11yTDB6dmkP" +
                "S82iXtXcPXRvPphyr2cIB2Hiascr1ggcW7yVVo73ShgLb99XmB" +
                "X2UTeSfMKsoJ/fdmik72jONSuGvmp/zBay7eOZy/VIK9pLtQXd" +
                "7ofXPgIPKgXbrq5zBLHzA/bYx7wXn4esiOuITthb6PX5k3w4su" +
                "3UGRxvxzjXjkPSSWuuytom2OxHfvEOeTfryvZZXc9btnK0fdKs" +
                "yOe7uTt/Yof7H6nOUnT+bB/jzp/NOdm8ob9150/qx5YXZUMkT6" +
                "R+so/z58/yc3aTnD+9fTg73ct1cv6kc+9n1Rnw4fJ8Pn92HtLn" +
                "z/6t6sxH58+sD+fP8hJ9/sw6Qz/Lv1Ve7KPC+ZNm76fuz5/9j1" +
                "L358+yT58/y8/4NdH5s7yMrHT+LC+Q82e5tLyCa5en+fFPA/L5" +
                "OH+WGC8tL+/fgvNnxZXPnyNmhF6fI14+YUb863Mk6Dvs98xIfi" +
                "+9PkcQSbZ97HXNvxJG6PXp/dx93H5o9Pr0cfmoZNhd7VMEI9/I" +
                "Hvf6pEz/+mScGDVgbzEjza35fez1tp2aFdvp9Rk4+ten97X/UG" +
                "Vtkzndj6pc4vsd8m7Wlen1ORIzsdXPnl6fI/T6dN4e04MR0lfv" +
                "Yd1+3/QM/b1YJFZHx/m6M5qghvt7IYhrv6g91fWoJ64jOkfmP4" +
                "ptsT/NFSa6dspeeJd/lFaOOQgm8w39HnMPRsiuW1lzes/Txr+7" +
                "imOkiU937eEK2g5cbp2faA9LzaJe1fHM/z3lXs8QDsKk69Y4K0" +
                "bgWKxc48Z7JYyFt98xegfcWlB+pbwuO7xFJ/fWgtYCfhKAubPR" +
                "u9Wj2ZadmH2InxIAC1HydCHEncW++J0wasjTg87LeN7h7FyVsi" +
                "/JLvVPQ1bqpxVo4JlvpyvWrGwZ8CnjSGJ5FMdmx7nnHagDVKmO" +
                "mGwhW+XpheaYLZac7HyOzS4KluWwZFfEO2Ca1Mb9CDleaWFu/0" +
                "VZJLbpcv1PprLBgu5s3jsOT6jENoXYeavKHq+8yl8hKg4uKh+L" +
                "bTqjih+vcschmVHN36z8wns8qjwe1aswOTrwXG1WY4RsNVlzeq" +
                "spo8RI81ir2c9dPBhFYgZEbkNXaw9kzKJe1dXMX9J8NKJkCAdh" +
                "kq4mXpkwSCujgmYS/b5z9B3mDoyQrUWsOb21yI10f49ipHmkOx" +
                "AlXTwYRWIGXG5D12gPZMyiXtXVtDM1H40oGcJBmDDbFDvO0GvS" +
                "FTSTaD85esAMYIT0vgHW7b+yRcdIg89u4lwdp9EEFdUHFMrQau" +
                "1hqVnUq5qB9iftoSn3eoZwECbtRemKNQLHls20crxXwlh4u+6u" +
                "xM3duCI36dTX3M2a093c2eQptXjlPsF+fT9yWP5z393xs2vYNO" +
                "LQLr4fIcd53Wz65+/gaX9HP3cXXszB3Y9Qh/nAl65GP4uXu5Lz" +
                "aC8jaCa8RsGkPZ1n5mGE9Hs9j3X7AbbomDSaXp/zOEZ69ZObp1" +
                "HD7/u3GcXMG75Ye+I8qSPxHGmPTbnH/pijWMpr0xVrBI4tl6WV" +
                "471KGVfRN1Cb8CPkRKWFuf2pskjsDS7XI1U2WNCdzXsn4AmV2K" +
                "YQhy+tsicqr/JXiIqDi7Ifjm06o4qfqHInIJlRzX9D5RfeE1Hl" +
                "iahehcnRgecSswQjpPcuYd2eHFskVkfT61NZdOdcwQjv340gDn" +
                "9Fe6qf95K4jugcaefHttif5gqT9nlTY8te+Nfnl9PKMQfBZL6h" +
                "H0Ztpx9Jkr6z0g7D3NsOU7Ywc7mSL5pHr2yIDzae7ay81IZXV3" +
                "hcTXPQdQ7TPO2M2KYzqvidwofZ6dp6LuwDx8grCAkPXhHz3DF9" +
                "g9fHVzZ72nRxU9vqvhhPquh6OkI8scWePh0HzPuPT1HrfDS25k" +
                "Gvz9Vp5elWwnzR8/vzB+S7Z/Tu9hZ9p86vkN/A/MtyF8y/q78J" +
                "ZzfJnOLcZ+CXqk+yHtLfmAvnpUPr7/Di55v6m3H5Uv00G/+6br" +
                "FGMNJvzOH+HqPwGrFC/12/62Jczakc1meWep2p2Pt9nU1t1I+z" +
                "/TdBRittdphri8TOdrmSL1pADTbEe9zZlXe08ipExASvqqjqzJ" +
                "6GZ8SrYlAxAw9ml9aOeajvh8TeCiHhwStinuvMOoyQ3ruO9Z6n" +
                "zDq7FHaJkYbotGsPV3BIbKfXyAuCkW/UHpaaRb2q6/aKlHs9Qz" +
                "jETOIVBx4bdIbtSyvHeyWMhbfv15nrmgfcCNk8wBrP6bRaWSTW" +
                "2V13I9tgQUcm5yKOZ7oGoiGbB9ib+jmGObgoe1Vs0xkcDxacw1" +
                "XiLKmvV4W90JV1nvCQ6MDzPnNf61w3QrbOZY3ndPr2I2wsWXM6" +
                "+7mLx38+X2k8A6JgsGydCzTXpI5G1TztF3R2iigrY46wxKuprw" +
                "y5iLKbdWVU0EwcpqwHmf4zxv8entue0e+/xdR1M1+XXbcv0b3n" +
                "Xr4Ol+856Z4f6at8u5Fet+UdpvqW9Wj5jvx2DBRyh8o38vvN9P" +
                "mRsw8fAZz2wfF3u7tutivTu4gwwPMjfc9Ud7KbdbzisUH//trr" +
                "o6dD5yO2fLt6zrUlviNV7zfXmDX2UTeSfMKsaRm7HRrpO1rGrK" +
                "Hd9yNsLFlzOvu5iwejSMyAKBgs/fOjNe75UctIHY0aeG3xPNfr" +
                "7BQxrGyMa/vnR2vS1dht6cqYtV6TrqCZRM+POPouanv9CLm30s" +
                "LcTrgxRN6lvHc5q7cHP3fEBW2viuOZrnGXeJDjvXsrnBhV8ez5" +
                "pc5OEWVlzBEWMNJZqn6VG3jvjSrvjeqFNYa7kexAy7SaT7sRsv" +
                "k0azynq21lkVhnd92NbIMFHZmcizie6RqIhmw+zd7UzzHMwUXZ" +
                "f4xtOoPjwYJzuEqcJfX1qrAXurLOEx4SjbldGJ+125/S1yF7Tn" +
                "x90GP93K3O838Wrr9nUz+zfp7venG683B6tp7uPE+4L+i/WknP" +
                "8+77n1Of59uL6tf4Kc7zG6Y+z6efCqa8zDfMN5ovuRGy+RJrPL" +
                "cPuRGREosGBPZzRxw0xHMlzHQNyXB2oLnG1hhV87Q/1NkpoqyM" +
                "OcICRjqL53T+rHIRpb1cQTNxmOHzT9mBftPffMaNkM1nWOM5Rf" +
                "tRYqR5rv3s5444aIhHHM90DclwdqC5xtYYVfOk/eyfmpdkgBlQ" +
                "YamvRupLLqK0lytoJg4z7KfswFXmKoyQrV7WnN7qdaNYJBYz+F" +
                "yUWHTnXMEI9/feOmL0tDbxawaIbPXaF7XN7oz9aa4wmQ5b9oIZ" +
                "xJV1nq7Dqwz9NnNb61Q3QrZOZY3nhO1HiZHmkW5jP3fEQUM84n" +
                "hmd2kU8SDHVcVM/KIJT/uyzk4RZWXMEZb6aqS+5CJKe7mCZtI6" +
                "tfr5yw6cR23Mj5BjlRbm9lfKIrHnuVzPtbLBgu5s3jsGT6jENo" +
                "UoHjNWeRM/xzAHF2V/Hdt0RhU/VuWOQTKjmv+8yi+8x6LKY1G9" +
                "CpOjA8+bzE2tM9wI2TqDNZ7T7vtRYqR5rJvYzx1x0BCPOJ6xlS" +
                "0skeOqYiZ+0YRncaHOThFlZcwRlvpqpL7kIkp7uYJm4jBlPch0" +
                "d36zBycAJ80e1nhun3EjnxXEK6cQ9utzhdkTrit76s83gRs/V3" +
                "TvN5HjvG42/fNN5zV77A/1803hpZ9vog7zgS9dzdTPN7EXujoQ" +
                "NBNeo2DS7E5zZ/NZN0I2n2WN53T38qPESPNYd7KfO+KgIR5xPN" +
                "M1JMPZgeYaW2NUzbOwOjtFlJUxR1jqq5H6koso7eUKmonDDPd3" +
                "2YHrzfUYIVuLWXN6a7HxnwuwRWIxg89FiUV3zhWM8PuxuI4Ynd" +
                "V3x37NAJGtxUUR22J/mitM0to6Vnhj5RrXPqvrCSavMvT1Zn1r" +
                "oRshWwtZ4zlh+xE2u4+9rnmk9ezn7uP2QwMa4njGVrawbC0Emm" +
                "tsjVE1z2KTzk4RZWXMEZZ4NXrOfmSghq7MK9E8qutntQP6GuQ9" +
                "3frK1eqWMf1/EOT6yf74OUv9naP6hmP3VNdPdf7s/m3XT0SUhe" +
                "bjEOvXz6neb6a1p75+6jVxhZTJlJ/X3WpubT7nRsjmc6zx3E66" +
                "EZESi+Z/9reynzvioCEecTzTNSTD2YHmGltjVM3TPq+zU0RZGX" +
                "OEBYx0Fs/p/WaViyjt5QqaicMM10/ZgVvMLRghffVbWLe/YYuO" +
                "kSY+3bWHK2h7o9H1vMbQHpaaRb2q6+XtKfd6hnAQJuUL6Yo1wk" +
                "CJ2PLFtHK8V8JYePs+y8zC6GTwzUJrva/1PljZIrGYSb7WtK5t" +
                "UgO4MSLnxRw0ilj8ih+MbbFf58roZFo75lH9BcqctHLMKmVcRc" +
                "80MzE6GXwzWbevwsoWicVM8rWmdW2TGl0bdVXx1DloFLH4FZ8R" +
                "22K/zpXRr+mVuHbMo9rP76WVY1Yp4yp6lVmFEdL7VqHZHd1fZ4" +
                "uOkSY+3bWHK2g7zc/SGJFnVbWjqzSruKrreVfKvZ4hHIQJ1lTH" +
                "jjPa76WV470SxsJbLPH9Su6L3e7vddZN93my/ePpvh9SfL/+SW" +
                "v0/ZA7/v+fJ7ff+d9+nqxPGFjTVJ8n5xuiPTj7//Z5snXvO0f9" +
                "M7zDIc0ox5jR7HV8E4O8R9MV6yJ6jzXq/r5DI5SfQ57HeA376e" +
                "POAkb1/xp8lus7RPf3cWFfjpfvZFTfHhl1f9/hEfuwn+Ul6rUw" +
                "CpTy4mxWtq/ieiRFH1UxOc7tJ8X0aWz393Fhdlm20PsvEFT993" +
                "E952SLyVL9fRzyygvd33e4v4/LlsOCv++QHWj8D5NtNnY=");
            
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
            final int compressedBytes = 3788;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtWmusVFcVvq0hBVo1IPRBS0OpLU3qD2jVaKK4mXsnCGhtjM" +
                "8YEQIEeosxwTaIRmDuHO48sPKsUqBwLTHxFRJpDX3YmsZoa0K1" +
                "ta0x1kRb/9Tc/mljNPERPXuvs+b71trnXmp1TtbeZ6/H931rc2" +
                "bumTOE9WH90FBYH6p5aEhXYf3wRcMXhfXFKDzI5Wxbz6ZoQ0Od" +
                "hZoTXxFXj86VHNGXZeS1Znausj4b97VQwtyMPTbBXXVu9cxWAz" +
                "BVb2Urw0oZZU6xlboeOWI9yOVsW8+mtcCo9uKRHNHt50rLg/UA" +
                "4ybrs3FfCyWxpzps7EVCf8IzWw3AVL2VHQvHZJQ5xY7JMTxzeG" +
                "Y4VswTP3JwSLY3jigD+8trZCZjcERn8DAqe4r5XnteAQ1QErlt" +
                "x3JeXp9UMX6/Z7Z7BcXQnawIhYwyp1ih68ZO9XAODsTYOKIM7C" +
                "//9Z9hDI7ozCpy1mid93jteQU0QIn0lGPbiu5Cz2z3CoqhO9mm" +
                "sElGmVNsk66LK60HuZzdfow9bFoLDPF1L8gR3ft9k+XBWjM777" +
                "U+G/e1UFIsqMfGXiSFizyz1QBM1VvZqXBKRplT7JSumy+HU82X" +
                "xY8cHJLtjSPKwP7yM+wbjMERnVlFzqra6nWhAhqgJHLbjhlBc7" +
                "s3ema7V1AM3cl2hV0yypxiu3RdLAq7isXiRw4OyfbGEWVgf/np" +
                "dAdjcERnVpGzJm3Xeu15BTRAyfgXfceMoLkrLvHMdq+gGLqTbQ" +
                "/bZZQ5xbbruvmCejgHB2JsHGn/Ais9K3v6CmNwRGdWkbOqtnpd" +
                "qEBHilhen7f5jhlBc7tLw/b2k4xr9wqKoTvZzrBTRplTbKeui+" +
                "vUwzk4EGPjiDKwv9zPrzIGR3RmFTlr0na9155XQAOUjIz6jhlB" +
                "c1e82TPbvYJi6E42M8yUMc5VbKauixvEqx7kypmsyr9HtJJ8Ns" +
                "ao/h7NYlZEcg2MAk/6e/Q+67NxrsWYelpiua0O5e/M88xWlVc8" +
                "yN4YNsooc4ptlGN4+fBy60EuZ9t6Nq0FRnX/uTxHdH/fN1oerD" +
                "Vz7IT12bivhRLPzbnQ3T3ima0GYKreylaH1TLKnGKr5Wj/duQh" +
                "60EuZ9t6Nq0FRvX3/aEc0e3nasuDtWZ2llufjftaKPHcnAvd3R" +
                "94ZqsBmKq3ssPhsIwyp9hhOYZnDM9QD+fgQIyNI8rA/vIamcEY" +
                "HNEZPIzKns4Pvfa8AhqgRHrKsW1F7wrPbPcKiqE72XgYl1HmFB" +
                "vX9cjD6uEcHIixcUQZ2C+4wOCIzqwiZ43WedRrzyugAUqkpxzb" +
                "VvQWema7V1AM3ckOhUMyypxih3Q98qB6OAcHYmwcUQb2Cy4wOK" +
                "Izq8hZo3V+4rXnFdAAJdJTjm0reos9s90rKIbuhL5kZImMMqdu" +
                "l+i6uFE9nIMDMTaOKAP7y0+nuYzBEZ1ZRc4arXPOa88roAFKxn" +
                "f6jhlBc3uf9Mx2r6AYuqOFE+GEjDKnvT6h62KpejgHB2JsHFEG" +
                "9g8NNX7KGBzRmVXkrNE6T3nteQU0QEnkth0zgub27vLMdq+gGL" +
                "qTdUNXRplTrKvrYpl6OAcHYmwcUQb2l9eIweCIzqwiZ43WvtRr" +
                "zyugAUrGO75jRtDc3j2e2e4VFEN3spEwIqPMKTai6+KmMDJ+Fz" +
                "zI5Wxbz6ZoQK2uz5/liO5+acTyYK2Z41+3Phv3tVDiuTV3vM9d" +
                "9Y55ZqsBmKq3sg1hg4wyp9gGXTdusB7kcratZ9NaYIhvxbdyRL" +
                "efGywP1ppZ3Gx9Nu5roST2VIeNvUj7edIzWw3AVL2VrQqrZJQ5" +
                "xVbpuvm89SCXs209m9YCo9rPb+aIbj9XWR6sNTNqq9Nl2TQCJV" +
                "zn1UN378+e2WoApuqtbDSMyihzio3quniX9SCXs209m9YCo/r7" +
                "flmO6PZz1PJgrZnNv1ifjftaKCneWY+Nvah+73DMVgMwVW9l/d" +
                "CXUeYU6+u6cX3op983TQ4OyfbGEWVgf7mf7wdG50qO6MwqctZo" +
                "nau89rwCGqAk9mQ7lvOxCa7oX+GZ7V5BMXQn2xF2yChziu3Qdf" +
                "OP6uEcHIixcUQZ2F++348wBkd0ZhU5a7SxYa89r4AGKJGecmxb" +
                "0R/xzHavoBi6k/VCT0aZU6yn6+Ye9XAODsTYOKIM7C/3827G4I" +
                "jOrCJnjVbuZ69eFyqgAUqkpxzbVvQ/7ZntXkExdCebFWbJGOcq" +
                "NkvXxQfEqx7kyhnqecVr9oFjxT3MikiugVHgSc/rhq3PxrkWY+" +
                "ppueW2OpS/v88zW1Ve8SC7FVoyypxiLV035qtHvWN/1ahkaIyN" +
                "I8rA/nI/jzIGR3RmFchhz9jfvPa8AhqgRHrKsW1F/27PbPcKiq" +
                "E72bqwTkaZU2ydrhtvsx7kcratZ9NaYFT7+Y0c0f19X2d5sNbM" +
                "8vpcV6fLsmkESmJPddjYi7SfT3tmqwGYqreytWGtjDKn2FpdFw" +
                "3rQS5n23q29HvcWsaongXflSO6/VxrebDWzO4y67NxX6u+sqcV" +
                "9djYi7Sfvwtr20/6fu2h3WMXyk/nyeakjHGu7uwmdd24tDkZv2" +
                "+qB7lyVmJc0JxsP6YrRcGafeDofoZZ47j7Vq2zGhgFnnSH+Hfr" +
                "s/ES8Wqtxag9Mbeej03wHqz4h2e2qrhT7hiv5hn0omfFB0v+cx" +
                "rd8+8YaS1CZuoLCJPtm8v34ecq9DP2itvzL5wL5nSv5hnUj322" +
                "ZLnQZ0QMzopnvNp9dT1y41z7QMUw6fHw6r/A+lu3VD38U71+Hr" +
                "yv5oa5MsqsPlk37lAP5/jserMMqE/nTykKI+fKwIN8zew+6rXb" +
                "uNeoHukpx7YV/d97ZrtXXrFmT/9+L1bz+7fu/e7f2/4Kr3+/h2" +
                "fq3+/mCj3P+737uNXu41O934tV9e93KI2vvZfkn1n1PPb9zt3W" +
                "7Oct5efKqf/Hfo592+zFZqgau++N7Wf7C29wPz/8ej4/966o/x" +
                "twvv0M+8I+GWVOV88+XTeuCfvS93eTg0OyvXFEGbp/gr/0nQNG" +
                "90UT2Td4B+1jVZY1Ib7ktecV0AAlsSfbsZyX39+pYu+HPLPdKy" +
                "iG7mTlS0aZUyzoulgfQvc1eJDL2baeTdGAWvl+CcTuqxwZ6AuW" +
                "JwT2pOvzrPXZuK+FkmJdPXa5n9TV3i2e2WoApuqtbGvYKqPMKb" +
                "ZV142bwtbem+BBLmfbejZFA2rlexqIvQs5MtjPrZYHa80s93Nr" +
                "nS7LphEoiT3VYZf7SV3tfdwzWw3AVL2VrQlrZJQ5xdbourHUep" +
                "DL2baeTWuBUfl+lSO6/VxjebDWzN5brM/GfS2UxJ7qsLEXaT9f" +
                "88xWAzBVb2XbwjYZZU6xbbpu7A/b0ufnNvj4TLJtvVisrJi2MW" +
                "p1r7c/R3T7uc3yYK2Zxe3WZ+O+Fko8t+aW1yd19bUZntlqAKbq" +
                "rezicLGMca5iF+u6cUi86kGunKGeV3GWSusDh0Y9e66BUeBJ1+" +
                "dbrc/GuRYjesqxoVQVWGaryiseZM8P8xvH4yhz47iu9FxGyUSu" +
                "eBLSfI2zNY5XO3dccrVOfEABYvRrNJ5ZHuQLtmLkuoQDnalG8Y" +
                "gSrtLz8vqkrqRzZrZ8qgNdVjoPhoMyypzYD+q62KEezsGBGFs8" +
                "ii8zGlCrXp9nDBM5OLg+DrIqyxqtt8hrzyugAUqKL/mOGQEKPL" +
                "PdKyh22ZvDZhllTrHNui52Ww9yOdvWs2ktMCrfszmi+/zcbHmw" +
                "1szeR63Pxn0tlBS76rGxF6rAMlsNwLTZdId/evDt//7B2QMl/y" +
                "ii1fOQ01M9xdhzWp6HFA8wXv4Kv8Z598V093PUfTs6jfr4PCR/" +
                "dV+yWfGMV1M9DxnfrxnFj9g/NuEV4LuPPg+B18+DzibChIwyq0" +
                "/WjXeEid64+JGDQ7K9cUQZ2F+eP8cYJjLByqDKskYr7z8n6nWh" +
                "AhqgJPZkO5bzMbMHvh9G5O6hlrXbb7qth9TfLv9deun/6bQ/wt" +
                "9fp3vSFq/P/Lu4e2bWxnnvwime2E1Of322z/rnBPYZwpTP69r1" +
                "mrLrc9oup4qGo+GojDKrT9bFWDiavm+aHByS7Y0jysD+8j33CG" +
                "NwJBxlZVBlWaOV1+fRel2ogAYoGX/Ydyzn5fVJFb4fRuTuobay" +
                "O8OdMsqcYnfquvkb9XAODsTY4lF8nNGAWu3nc4zBEZ1ZRc4arX" +
                "ef155XQAOUjD/rO2YEzS0+4ZntXkExdKcdm/553Z7//vmnf/Jf" +
                "+3vHlv/9+WfvzBt8Xrfr9Tz/nOo3m/M+r7s33CujzGmv79V10V" +
                "EP5+BAjC0ezcsZDagV++WMwRGdWUXOGq13v9eeV0ADlERu2zEj" +
                "aK5mWR2eh65Pzd4Stsgoc4pt0XXRtR7kcratF2vOk6gyICepnZ" +
                "cjus/1LZYHa83svWR9Nu5rocRzcy66Ev2+X3to99iF0g6EAzLK" +
                "nGIHdF30woHGEfEjB4dke4tHcwGjAbXazwXAaP6BIzqzipw1Wu" +
                "OI155XQAOURG7bsZwDTxVaZrtXUAzd6um+vXtt95r47u8uTnfx" +
                "dwzuLcpd6V+HT6DWd9P9/OT5fp1sLfV3FK3vV/N3EuNt5/19c9" +
                "q7stb39H6JP/Xt781T3i8teD33QzZO9/OT9fNgX+eEOTLGGT5Z" +
                "Fw+GOf2l8CBXzlDPK16zDxzdUSD1lyGSa2AUeKrnyXPqdFk2xq" +
                "16Ossd23PwW/0W1Xpcdju0ZZQ5xdq6bixUD+fgkFj7Ma3lPEYD" +
                "arWfuxmDI2Fwp89xzxqt/zGvPa+ABiiRnnJsW+H74Th3D7WVnQ" +
                "wnZZQ5xU7qunFVONnviR85OCTbG0eUgf1DQyteZQyO6MwqctZo" +
                "5fV5sl4XKqABSmJPtmNGgALPbPcKim12+qX08+kzYqb97Er3aj" +
                "/O7zFbi1qL7f81qfu9uDl7uk/C5uzugfPdf7Y+xXd93cP+vq94" +
                "tLu/NRv4rbnNydZl4JL7z5rfr2dX+e9OuHf7/32gWa3V/Pnp46" +
                "3bp/j9/RV/wFd+p32i+Up/L2dNX0HH3KliY0+WvrlY95dJXlJl" +
                "zoBgUcTTPjuVBjkf+znXAp25GXsQl9y5njnvRDBVb3X+H0Yvgg" +
                "c=");
            
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
            final int compressedBytes = 4115;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGuMXVUVvrwMIjLQMm0ppRQUqLUPsAi01LLnnt7iY6YtrZ" +
                "1CsSCghZYEDYWIJpbZ9zGduTM2YAqaqFgR+CdOpSP4p6jFpiTa" +
                "xF8iUUmoCTRD06b1EZGHe+911lmPve+9E/3hPdmvb72+vWaffe" +
                "4957Rmp9lZKpmdJm9LJRz5fv1FRLgOHSTjxR+VC7g38loKn8oF" +
                "3AeXYMtZxFEDt1+anWleZEEciImPLWfMPaAuakkeOg59UNuebk" +
                "9z9Rn2A/ZMlNkP2Q/bc+zUUqncY88PyDQ7w15oZ9nZrj/HXmov" +
                "s5e73hVBNteVea7Md2WBXRiwK11Z7MrVrnzSXhewT9nl9gaIUD" +
                "7VVhyy0n7a3ujaz9jP5ZE3Fxz67c32C3aTvd31v+jKHfYue7e9" +
                "x55iTwUPjRvtB+1Z9mzbZc+15zmNKbbbTrcX2JnB/qJGxV7s2k" +
                "vC6GOhXmSvCnP6hOtfa6+xS1x7vSvLrLFl12Z2BWXIftaVXlf6" +
                "7Gq7yq4J2E12nf28XW832I321oDclmvf6cqXXE43m81QQxtyvR" +
                "nH9f0SIV2uLe15QVvyAdjQWOyxJD5Szseo2fyJxKRc2xKT+q/T" +
                "vikXyEBGlhzIp9LeZDZBDW2QbcJx/YBESJdr1/ZxhBe0JR95Pl" +
                "+JPap8bpJxaIyazWclJuXalpjUf5P2TblABjKy5EA+lfZKdzwd" +
                "6tA6BEduXPlqQAkhXa+5MngqMECggC/05jL4R9QJGPOYcwiRC6" +
                "mSow5yIM8xr4IVzSi3AVmY08pIvhKZgj3NIRWBeNAsc54jZgRq" +
                "aIN0BMf13yHCdeggGS9cghE47rL7KvfBJdhyFnFUX5q/19xjC+" +
                "JATOq/1TPmHoiBjixzRYyVdo/pgRraIOvBceUNiZAu15b2UCoX" +
                "ghQjkE64el4Ye1Tne4+MQ+PCxxsSk3JtS0x0bK5LswL+er7ywN" +
                "lTFtxf66d+1Li/cZ9Z55B14Vr1Ns6q/o9ifuuwtnOg76Q/K7X4" +
                "1Peihes/F8tXPC20f+7KHpXPdWDf+IqTjSVj/JO0kBsfUV/Hrj" +
                "8LGvXxlBztuC+7SkeItSI/66G4jL2HWOVPXNp439cun+u53WCp" +
                "tq+VP/5pvEv9ob+XOnzMehXlVK3R/IPUIv4pe/rgnFrJAa+ewn" +
                "3l+VzfeAdR3eKnuqhUGnarZfgp0++k/QH7eCFdWPjsx9rlsz/F" +
                "o3pl0ZtHFunP4F865rNf2g8/E+XzFdCqzieGZKPtWew/8xmlIm" +
                "u56c/z2R9LW8+yMuGka0M+im+19be5FOR2DvQ7fdppDZ859Ggn" +
                "a2k/tCv6+z0x9AjX8j0+GpjdYi/6F+c39FiUz7Wav1mb53Mtob" +
                "oVXD869JGhSyrjlYmhSwPTMwpW1o1+HHrjaO3yOT6pfAqtqtgx" +
                "hy9jkifT1p2iVJ+SWr7HR63y6ecU83P+drfmD/nkqG7F7ML53j" +
                "PYc1+p1DMIJd/zil0LMK9h55C83aedVmcPjs1g+/O9mkktyRxm" +
                "0y52ew4yPuVTW2svdMZU9kjMl/o7lYnapoDsQcytz7wfn2v8LE" +
                "B/+swNLI75891LKhPVJ309sEadR3t8FBj5emgXaJO32m3ufOda" +
                "e5Al8B2YLfkV5/vDpA/ne85Dr889PJ86S0WkPTID2YnsBJzv/n" +
                "4InO8e87Vbn6dlJ5pn+371KGL+fgj0fQ0flCES9BZjn/SK7yxP" +
                "osT5fwtassT7IYhijUeez+ed1ll2CyJ2SnbCTkdde5HmV1yPXs" +
                "v1r6GY/tD59PdDKJ8Fr/z7kb0XkOpbMgNmtVmdX99XF/vvat83" +
                "q7Pp2XSzur7V97Fk0+0c7HMLKiDJpsPIe/Bt82/EzvtFPbO6eQ" +
                "Ja8iR5ADb8DGghN+fxpNSSDGJ+FF3HAq86n16T8sk9yKgqA2tM" +
                "fqZhi5hZky3KFpk1IZ9rsGSLUJcsuRztvZ4feQ++bb5PsWuvoR" +
                "d/NN+FVrOgCOQVNYPH96SWZBDzK7K0SMcCr4l8LuJcVD7XSKTI" +
                "wCqT77Wm2HMBM6tqr2RLAIVxwN6kMbegApLaERh5D6RXMF2Ceu" +
                "gpZkERyCtZlEojXVJLMoj5UWwdi8ePWcYeZFQZOzueHc931L2F" +
                "n4C5erE7jgNS2YuY18z7x8mCSu5hMfjzHkiv0F+McdATWSIbHw" +
                "WjkzZpjnRLLd8DZshX8qPYpE/zjTk6+WJmpbKE1jguMnA0Owo1" +
                "tEF2ND+63XHUn++k4xE6QFsX0MtH3aRX9Lq5l+YJkiADzkJ4ZU" +
                "jzpOYeW9CM0CMwkjOGfnU3twhoN/crc0WMibcvps/05Su2r1i7" +
                "AfNlxRbTF/bPPiqoS5ZajhJf117mesX1fQvqmT63f/alWEg/qE" +
                "3civ2zT7JqxY9i61jgNd4/awc5F3W+q1lh7Oxkll8nK8V9IMB8" +
                "WbE1Oxm+L52sPIeY14Q+WgJOcmjRH9cr5rQVJdnJkW3QkiXE8F" +
                "FghDUe9H2Ja/keMEO+kh/F5rMgz9H3T3FfTGcJrWmWUEyv6c0z" +
                "3FvkOmCmN5uWTTO9YX32YsmmoS5Zcjnaez0/8h58O/J1xm0aev" +
                "HHyNeg1SwoAnlFzeDxIaklGcT8KLqOBV4T1/dpnItan70SwdjZ" +
                "sewY1NAGP8dw3Dg3Oxb2T6FDB2jrwiUYYaRGuPs2WSEfI5ZLsO" +
                "Us4qjBY1Vzjy2IAzEpb9Ezhr7bP5mFng/3yGdPbDl3+ZvMPpDr" +
                "zMhmuJxOIal90SOt7hzZ4j6ovTKbAVpeP/q7z0ih7e8vxZ/G1P" +
                "Rv3k73lzB2ZcIuaXs/ZCLFstP9JTPVTIUaWsRg3Og2Uwe/BTjp" +
                "aO10kRHIPqzPleiFe46ZURzSR83BtzV3KdccESlv1TOGfnU3t+" +
                "Bz4RHiOEq7y3RBbboKWReOGwtNV9g/uwjjPbLnIz7mGMUYvok8" +
                "uf2zi9tJDtwLIeH750MSk3JuS7Vv6w/zGVPf5ZPlQPKXXiUite" +
                "22du+HVF615/t8Wrczt34/pLaP3g/B853tAon3Q4a30vshI5be" +
                "DxG7R9v3Q/z+6d8PcZLk+yGl0sDsFu+HbE6/H1LdPZn3Q1wR74" +
                "cU2vn7Ian9inai2v3xnkZ38xCl50cerd7eekdEq+GHO++PdK+u" +
                "uim1Z4+ex+41TpB/xGD/JC/IvHwP58L9Sk78HmQcp7hruE3tUt" +
                "eb66H2LWEwblwLKCKkCz2y5yM+5hjFGK7zqCSJOXAvhIR8TpGY" +
                "lHNbqn07+G8ZW/LgmZGRJSvNmGLl4+VmOfWhuHy6M9V/n+c6WH" +
                "PtuF95Pbq+LKcYldepP3pFi+diy7lHHgW/z5MP5MWZcXusfeuZ" +
                "ERfy6/bP5XouvMg4MR/FfplZRn0oLp9LRT6XgYTkqJ3qpyKgvO" +
                "cJ6o/Ob6XPPcaeXT6XcSn6lxay9m1jCedCfl0+l+m58CLjtJtp" +
                "2E/59eibrpwT3gpcKXRm2QG4Hrn68gKdyzQWtInArkc9P7AVu7" +
                "0Do5tDne/G9o5o/7wKrkdBWlyPXDuT75+J65Gbk33YX48Cxs7R" +
                "Vtcjen7Er0ctvt0ezA5CDS1iMC6vRYTr0EEyXrgEI3Ac/JIPLs" +
                "kOcmbESkZFbmleZEEciAnMKfZN/ogJ9ytzRYylduo6S9fJRg9d" +
                "BfV1rrqr1fM4ee1MPo/7Hr/i8+dxqd9I5Idfw0dviJ4oiu8drZ" +
                "7HNUz8G2ey3zbYr6nHJFI8j9uf7Yca2pDr/Tgu9yLCdeggGS9c" +
                "ghE47vK5m/vgEmw5iziqL6NGc48tiAMxgTnFvqWFng+X89kTWy" +
                "jsrzser8/yqmJtjLP1N66/u6XWZ/HEP/G3L6/qtD79mwqt12dY" +
                "ZyuU1jiyxPcZ0uuzmNO4XJ+NrN37GDpLRQ7G1fp8KXsJamhDrl" +
                "/Ccc+LiHAdOkjGC5dgBI47vz/kPrgEW84ijurLaI/mHlsQB2IC" +
                "c4p9Sws9Hy7nsye2UNj6HIvXZ+VwsTbG8pV02Gt2Xp/Ockys1s" +
                "Ni//w+W3GHk+tzzNu32z9zJqQ1hixh1Gp9FnMak+tTMuT5EOtz" +
                "rOA3Ica4Pg9kB6CGNuT6AI57foUIoo07UQoaKOMl6N3Nvbmzcz" +
                "NZuN9Hj5OPxl1cgi1nQV450viy5h5bEAdiAnOKfTfu4BZ6Ptwj" +
                "nz2xzcuh7JDeLwDzpXFvdig87zhUGUfMa0KLltAjOdiTFukVby" +
                "P9CCXZodGboI33T0TJD2r6Ep53MC3f86XWgyPJr7i+b5WzQM/t" +
                "3gcjD7R/oheZgfx7/wZ/0AiK+1u+jL+PAOM1afv7ITAmVPZ1jO" +
                "FfkHQ0+V4r6qKe9hZ+H23gWjhCREoI83OK/YffRxs0f15oDnwm" +
                "0SznmXlQm3kcg3FjG6CIkC70yJ6P+JhjFGNHF49KkpgD90JIWG" +
                "cPSEzKuS3Vvt1xjowtefDMyMiSlWZMscJooz9oBMWt5iMS4zWX" +
                "yJrjfEwxKkfMxg73EzZKj1p/xzTvg2vhCBEpIWxHd8o/x2QW+G" +
                "whBp9JNMu5Zi7UZi7HYNzYDigipAs9sucjPuYYxSiP86gkiTlw" +
                "L4R4D6PfkJiUc1uqfatjSx48MzKyZKUZU6wwusUfNILi8lmTGK" +
                "9JEvbPW1CLW4i/HItRfl5Lo/V5i/So9cvPc4/Ii5hpCWHeMvbP" +
                "sbD+z0dffLYQg88kPQ+zwB80glIqrbhKYrzmEllzPB2j/IJZ0C" +
                "GfC6RHrV9+gXtEXsRMSwjzlrF/jsks8NlCDD6T9DzMfH/QCIqL" +
                "fY3EeM0lsuZ4OkbzdDO/Qz7nS48pffKIvIiZlhAGc9L+OSazwG" +
                "cLMfhM0vMwt/qDRlDc+b5DYrzmEllzPB3DXK2lKUbcS0qfPCIv" +
                "YqYlhDUGU/45JrPAZwsxxEyS8zA3+4NGUNzf8jqJ8ZpLZM3xdI" +
                "xsr5amGHEvsX5jiDwiL2KmJYTBnLR/jsks8NlCDD6T9DzkHUD6" +
                "JV5eKrH0801dp+8Y8hjNGZ3vN0qPiXtUS+VTSvl8E98PSTzfXJ" +
                "ryP9nnm/ouV3oeZqE/aATFxV4mMV6TJFzfF6IWt2gVo7lUS1OM" +
                "uJeUPnlEXsRMSwiDOWn/HJNZ4LOFGHwm6Xmo9xmK50eN4t8k2x" +
                "nh+dGjrZ4f2W/r50d2cevnR83l/n2Gtk+P+js9P3KY//8uHkk/" +
                "P7IXtXp+1GiG/n/5/Ij+v4scuS39bEOe7zhuPKLPp2xA3jVrdV" +
                "5kA/lOOUBnFvayAY92et6h7s5FZ5rmruVwvy4+33Vs6QF1vRZq" +
                "yt0h5iGzkNp7inzuSu1Pk8lnaickDjsu/v/lc8fs9vlM75+Tza" +
                "eZaWZCDW3YE2biuPG4REgXtX2vto/LQJ8X7iM/32+QUbldvi/N" +
                "jOMQEvai70hMyrUtYs7uMT1jyR7968iSg2ZcaM8ys6CGNshm4b" +
                "jyV4mYWbU3SYqW3B71a0dgnD1IGPbc2fSgtkKdIp+z4jiEaB2p" +
                "waNJVr7VsTV70PVaXFNz0IzJMr6rX72x+I733egd0+2lSX1QL6" +
                "WfbZ+sl//l0/J92u2T5d9Os9bqX9P+B/a8E7A=");
            
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
            final int compressedBytes = 3368;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXGusXUUVPlpaC+htabG05SUQEfCSgkaOWtRz7r7XR1UoBb" +
                "QqWEFUfOCDijHapp19OOfU27QQaITEUlBj/EMlCOUhWJGIgol/" +
                "5E9TrdAmbRD+NET725lZe81as+ax92kvdd/Ma61vrfXNnD177z" +
                "N39lFr1axWS81Wc9RcXf6spQ91snqbGlMLW63ubepUK1mk0+nq" +
                "DLVdl+9Q56p3qvN17V1Wd4FOF+k0rtPFLXuoS1ruUO9T77flh9" +
                "SH1UdANr1GTWnJR9XH1cd0+Qn1yVZwqNXqOnW9WqNrX9LpBvVl" +
                "9TV1s3qTerNDnKhO0vk8NV+dossF6u3qNLVELQXtxrPU2Vp6jk" +
                "VeaPNl6lLbp/foeltdpj6gy+U6Xa46akKXhZpk8Vfo9CmdPq3T" +
                "FWqllV2l0zXqWvVZ9Xn1BY/tjTrd1Gp1xjpjkJsSDmiZdvdWkK" +
                "KEsFAje97ibS6jGNM3kaf+faQJOXAvJJEYH8Gjcb+8T6FvYore" +
                "/cgyos+YYplj6jXzRy1IGvGwL+M519TVZYzpr5KWYkg09yK9+R" +
                "6RFzGbem3jWT5vLCGe9M9l/ijw3kIM3hPJq3Np51LITUkyaPfv" +
                "BylKCAs1suct3uYyijF9C49KmpAD90ISifERPBr3W/Vphx/b58" +
                "FHxo8sI/qMHXpZZxnkpqx0y7Dd/wVIUdJZVvyQtGiJNWyZ0uCw" +
                "5D4gAmpl9JAD90wSifERPBr3CxIZ2+eBvg2KI+U4SMYUy15Nq/" +
                "tR1Vqvk70W9Hc62WKdzlA74H6k8/Od5gJ2Vb6Y1d9r8t4Lrs3u" +
                "R8Uqcz/KHeozarUt11TtGyIYfT9Sd9qaux/p0t6P1Jkw3839SN" +
                "3P70fFKlvX9yNbLmf+2P1oeK5a0Xue7kcV4ip1Nd6PKskXUz2Y" +
                "2hVe+foP+9qp14ofcWTSl8YByuDldbD3EkozHnblojhvDGVqJv" +
                "V+Ci0Yz/BwjLK9MPxjLNFKlhHkQ87XDjeeu3xtsaNYz5FJNhpX" +
                "Waz3fZqyWI/SzIg9lIuC3jjK1HiLeiFs18v+xlHaw/r0KMkyMn" +
                "vcfC+2u/H8nYiyvVinkefWjqfF2do636cpi3Uozc741Tn/FeYk" +
                "hl+g02khJrCtYsN8T0ZYp1bkWKpvJuy2FFsghxJl0O5dUWwZXm" +
                "AkvSsr6QbUAgJsfB+Aq6QbPOmWSrbBaztfyMBn4dujZHih5B5a" +
                "UI9YXzfIHkO9fMCz2AB99XnIOHwcOXcxxve583N3SqP788G4db" +
                "nGx/mWUE692BrhKK9PMwyP/h/ymNFip47h8sTY3VXcBTmUKIN2" +
                "/xmUcAz9kU7qUeNHQM3wYt4mOdr4LMKooWc/ArYJSfjhuOwx90" +
                "AMZOR4T4htlbYWWyGH0uq2Yrv/XLF1uAzkIJs6hFpAoI77ABxI" +
                "AU8+Qca9kCV6wxpn5Uc1aXiJ5B5aUI/QIzDyewx1Pd+ZhUFxJP" +
                "fIe09sOXdzTJ7udPe6ufNncTbf22w2hDiUQDm5P2VJLEbzH3pJ" +
                "YdKxRznqeU4ujTx//i3+3Ff//JmXTB6oZ3FMvV1q1pcSugMzFS" +
                "H6Sd9Z3Ak5lCiDdv/vKOEY+iOd1KPGj4Ca4Xd4m+Ro47MIo4ae" +
                "/QjYJiThh9+WPeYeiIGMHO8JseXc7YgviZyf+2b6/IRyOJ383J" +
                "ccnf/QS+r8HP5kRs7PJSlmyG1ysS8zqb+fr4fJUQlX+GKrWP4q" +
                "WBVrL19vM/nGlZKFXFOTK2Whf2JgvGw8K76mOLlXsox5rfsEJx" +
                "f7Eow9uXByIeRQWuxCbPcPoIRj6I90PHFN7wVqYU2fI1u4D67B" +
                "krMIo5LHGC+yICTDf132mHsgBpMLe89zv348Yky8TerM6cyB3J" +
                "TV2tMcbPdfASlKCAs1suct3uYyijH9XR6VNCEH7oUkEuMjeDTu" +
                "FyTDO/zYPg+2/ikiy4g+Y4oVuUc9gbXerSlN3i6P033a1nrDjx" +
                "QHun7WsWxy9NY2YOJWQfr/Tmnydnmc7tOO4zCeCQ7D+5qynDEm" +
                "j7vxfD2lydvlcfpKc8vx7EUqdh3LGWPyFNa650vN4C1N7KiGx+" +
                "Ak3pr+HtPMPTqWgxOb9sI/qE8+YjD7jRpP9pyxJ3g22dPILnz+" +
                "9Oym16U0I7CssUs9f5KdeOLZMyPn4oPFg5BDiTJoD96KEo6hP9" +
                "JJPWowApfr8fw598E1WHIWYVTuOeRFFoQk/OBk2WPugRjIyH48" +
                "Yuyjo2P8pJsbZ6Y0ebs8To/nr4/DVSvBgfpUx3LGmPzGXVPGUp" +
                "q8XR6n7wnfOp69SMWuYzljTJ5247kwpcnb5XH6meXR49kLEfuR" +
                "piyP5ugs6iyCHEqUQXsw7ksIi2hpj3ieuA+IOrFaWpEd8ZJxSC" +
                "IxPoJHkwzC2JI9+peR/XiSsUMv7iwubA5l0cIW1geXkYSwncXG" +
                "1reHlvVuZViiDGvgt+VZmRKkRYtz4HGIg0FJGbdosR4hR2THY4" +
                "c8IBqgCi9y4cUjn4iGullZmTgMKywTh/Wndxhbpm3qRkb7rUhL" +
                "6z6o5ys2xpc9Ew/z3VhQQ79yfQlsjNbU/Dh8Jxbw5PvTOC/kYN" +
                "aXIA7yAZ3sDdbLB/gqk9HwfmEEzgT7SD69+94Qc6jZ66f+Xji4" +
                "n7RgQfrckUPVe+gO6zBl4aN85t1h6vkTMXn/sfim99JaotRaNa" +
                "sYh/1Lxbg+c8d1q9pPa+unGpnbT/snXQ/202pZsJ/W+LIzYTzc" +
                "T2v9RvbTgo3Rmlp6Py3w9PfTao+R/bRaWu2ntXjYvzRO+2l1Pb" +
                "GfVmtW6MT20+rWc/5+WuyjRVf7aYt9xb6Je0wO5cQ92MK6Pptt" +
                "Thj6s772oR4T4KAFeMBhjccgCyMHb+YPpb5XzhNkMV5kAczAK+" +
                "Jlbyg+2QKKazECZ2J8VvPdjYC/11TLCn7lKgrK+dXOv36iPr8+" +
                "z69txoL7ofV51OeunxyBGt8jXj/9/yTwPoW+/f8k+H2CCJJJbH" +
                "3ezve2Ls1812XRLtpuvpu6me9tN9+f770Sme/tcL73qpU+rYvs" +
                "n9fSKfWXYL63ITd/2fneBqw339vR+d52873t5nubzfd2cr639X" +
                "xve/O9LffPA98Kjfvn53fmTxwxuSn1mXsEW6Zt6oOrSULYznxj" +
                "S/bUsk8rTgZ4kGEN/JIES5AaLY9InomDQUkZt0AdeAQeyI7HDn" +
                "m4GXzE15IHnwf2CHlGV2Cecd/NxJqXeqH+f22N7v2/fOO+F9F8" +
                "T3zffGpUj+rKEb4fvdp5FXIoUQbtgfIlhOVo354nikAYux7ybO" +
                "gx5OV75RKJ8RE8GmqIyWBj3DeNBXr3I/vxyKePlv9ptdfU77tn" +
                "vKvCzz48J3u7R/1/efflY/8fe3jf8++JyefPl5v5z+vz2s5/IE" +
                "Fefs7Njd/HcIAcaZVAWEzMGt0m5oFjsA8oS9ljn/L+y+vy+pS2" +
                "eyg8P0FG2rrzU2KlVfhpTpxwrOcneOBsG5+fh5r47x46pvPzv5" +
                "AgL290n+XuGA6QI52fwmL64Og2dRjsA8pS9tinvP/yK3l9Pbvu" +
                "JsyhZr+/363TNtJW3983NbqHb8pcPzfVW9dhbj/PR/nMu5uS5+" +
                "emJhxS8aW1RJX2HaTuGCTIy3c77c3Cm8V1x6KfqHsHtrwoYCEs" +
                "ug32DMajhJhynHMju5Q9xs77L7+R19do50GCvKx2hRTzCt0a3C" +
                "5xgBzpeVNYFPNGt6nDYB9QlrIv5jXxX96W16e1pXmnbHY5F96X" +
                "UXvLJeZ5qbTP+uUCm5/jnmz/odM/5fsyZSZyeZ61+xeT6G+w3Q" +
                "Z7hHPvy1g/J8ffl1EvVfozEyNRxZbvy5SniPgryrMz7IL3ZUq2" +
                "V6HzOiTIS7drafCYTlskrvN6/WiU3rtl0mLirw3uNTVRhi/6GO" +
                "wDylL2GFvqywe81o/z8VNaub5EK0GmPXgi9j60XF/q7ZbvNtet" +
                "L20+IbY+HzJLry+RZ8mrbn2p+2iT9SVEyTe+69aXioPFwc6vTA" +
                "6lHvmqZdqDJ00OcsLQn70eHUQ9JsBBC/B6BGeTBXgkH1iCjdFC" +
                "jfTUkjxjvMgCkOAVJHqknvatfA9gCyiu5XqSuLPVjYCyv3cB63" +
                "Xu2uB+72LwjPi9i/31v3ehDtT/3sXmOf/H37t4rMnvXWjUUf3e" +
                "RWyliObJ4I/htaHp7zPE5281nmP1386b/T6D/70t/H4Ume87/a" +
                "tQfAy6O8Prl7zeJZmvotyOtVsPGTwbwxAytx4iUe5Kr/R4nlM7" +
                "mqtS9s5Pz0eZGm+l7Lu/zfNDVFyPUlmmzwTN1L3nNfGDGKbZmm" +
                "cOtfm8eutm60uxWQWt1Pcj7FPef3lHXB++yyD0K6dWQg4lyqA9" +
                "2ONLCMvRvj1PFIEwdjwvDD2GvHyvXCIxPoJHQw0x6T4S901jYc" +
                "/PR2RkPx75RC6sfi3l9rNxb3qWdzvu18aQ2XMng9p8eb11kygc" +
                "ZWq8lbLvPl7Pz6DiepTK0sNcQ7mY79v0OXpEYgiZWU/blkNN1L" +
                "6PMHVNkygcZWq8lbLH2Hn/5b1xPUpl6Y7/AbgMUZM=");
            
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
            final int compressedBytes = 2852;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFXH2MHVUVfxZqSSstEUpb0DZZQxQtBAWMFopvdt6jH5tgG3" +
                "CLQcta1EqkiV8x8Q+VmbHOlN2a1H/8wKQm/mPSJoARLWqWpNo0" +
                "7VYxiootQow2lNRUQC2oifXeOe/MOffMvXPve/u2nZf7dc7vnP" +
                "O7583XnbezrVarNbq31eqe7p4e3auLrvVYy7UGar0hBvUoRT2X" +
                "ooxabQd6jGEisKe1wIh7JDzgwAdpyT/KHlwJUYkj6CEK9jkP8g" +
                "cjygTaQR8jSebk19yIV76CZsW5mnPNpu16d4TJTahBhg9uarXs" +
                "PCgazcSGMhnofPLvnPDRfsnS5jXaL6PU2cmRLp0FnQVQQ6t1MN" +
                "L9+CGUcAx9QJdNoy3HcW/ktZfPzdwH12DLWdSjcs91XmRBSMJH" +
                "P5Ez5h4QiyiTh4xDW4Ve3FksMw8yXfJ3ghbHiCUpjet67k3snx" +
                "9DDVjZMGSJNX7sKJOBGZfbRAckS5vX6ICMUmcnR7okn00uksjk" +
                "c8mlqr5c5fPG5IpScqUqV6vySg9xTVm/FY+T5O2qrFblup7+Bu" +
                "bt5uQ9tXxuLzW3J+uTdardkIy1LFvyoeTDZTtR1vcl25NPJK9L" +
                "5gnUkuSyXm+pKivo/JmsqjDXskw9nryrJ31vr701aSejIp+Piy" +
                "i181FyjzHapspHBWIH5hMl+U0mIludrmnNcktvUfn8RmvWW3pT" +
                "s15fj2xb9JUQ72Eo35Y8bOazc4PI51h6x1DyuXcI+RwbMJ+PBO" +
                "XzkdbQtvQDVW9c5PPTNnw23Zf3LSqf323N+Zbe5cjUE0H5tKCS" +
                "Q8H75XxVLil7x9VeeVsvT8/E4uhOTmSnkmeTEb/H7EVowUPynK" +
                "mNA84ayd3Nes0yWcjwb1RlmSrPk962Yezk3Z74G5tYJp/0fiMP" +
                "RTt0DaV3/nyetGW9g/p+f4PpKjYeTBqbKJM5zKYpdrN/V3xpHZ" +
                "KL9NtV72G5fw52vPP9M/2OOt73n4fj/VuOTD0WtDc8Ntv47bug" +
                "KCafqTh9n2vN2jObfU2o/Hd+NiFROIr4N9vnT/MZ2fOg+O83Pc" +
                "sIsrWt7pR2M7++xyOxOlPme2h11d6sJfW1l+UsNQKoeMSmi71n" +
                "YNsqtRklnyq4ru8Y2+UfcpD+oHsakSCBtr5WDdi3flTt9Qd9q9" +
                "jBtskn5/54d94vHfR9R2UOfjw8Jux+/p/Dyae0mzx44fKZHgg6" +
                "+z4xm9jtf0CBOv1Zlc9/23CA7Ne/kc+f92/jw+AcUOayj/aF+I" +
                "/2Nev7yUFaHY35f0SUDXIsJQ52AjX69NztlxjLxSwsdjrEIyj9" +
                "WpXP/7ZaX93NuK4X3NdLiWOOAjX6h9mzzH/bHMvFzBU7NdZs6S" +
                "8GZ5aVTydgfSTOn/+rZMtL5DfTX6v+SMj9Z3JjyestNd6/Ucf7" +
                "H73n8HHf+ij9pV4fJafk+qg3enP6K8f3cDRkfZTPJBvTVRZed5" +
                "rro2RrP9ejQmh2/lTns6993Z7P40M4ijz3sK58RvM8e9cGRNny" +
                "2cd5ZxfW0CvzOU9ou9T33SUSCu3sOpe1D1OuNxkKmQOjaJfzfm" +
                "lXCAdXfGntnwk/fxbiWXM0JsfRWNB3JVD5M3N4PRqzx6xiB527" +
                "0xNDvB5V683iYsF1oxxLSeXjz012o/Nny9HtAWO5mLXPBd3hnR" +
                "vw2zxprtV0DTLSmvflrmPcbWW5n3+5/zWAC4PR5G+szuP9ZIj/" +
                "6GSzvp+VTfqXqndyjtZHZy/gejNsP5tF5O5HqBb5fEGdb45LDC" +
                "H9Xh1H6+/91r4o+QkTpXt85LLH2FJv3n+6+KNUtgZmK9Xm/dLO" +
                "aRumuzUonw2o/Fm/dUgUjtI9XXY+2Wyf7/LzU9l90a5HqWwNzJ" +
                "1Ul75eqq5HC2wYQjbOtQGVP+e3DonCUbrHRy77/E9+fioHr9j1" +
                "KJWt5YwxiTX0ynxeIrSd9CzXN95/TrJ+x61zWfsw6asmCpn37j" +
                "8nnefPyRAOrvjS2u2l+2jFtHoKUiw0tdE6ExmtA0mzP7Rj16PX" +
                "vHveo9w+BKV7uiBHF7P0tTo/C+qcXY9S2RqY+6guV1zVqqhYZM" +
                "MQsnGuDaipj/utfVGyi0yU7vGRyz4L4KdQF9v1KJVt4Pp9ee17" +
                "G8r6fWr77O+HsgWDrd/zvwZzH3D93r2X6rJX/fpYXGXDENLv1b" +
                "517vZbh0ThKN3jI5c9xvb5t+tRKtuG730JrTezRUF7ynS/3+DU" +
                "py7c/Xy2sHVetm61z2RXVPvn1XWt2Q/xZznmTvmtfVGyK02U7v" +
                "GRyz5/IWQW2XK7HqWyDTt/Zm+am/Vm/rcLt3/mp1vneWPPl942" +
                "LJ/FtcY5LB7C9WjVYPkcRmzv3fProUCdVb9nFKttOED2c/6UFp" +
                "17wzj1g8E5oMxlj7F9/pv1Hu18KFBn1b5U3GbDRX0/vZQW+d/7" +
                "t/FhcA4oc9nnZ0L8Z+9o1ru00QPRA1BDizIYF5EpISxHm/a8UA" +
                "TClPvIRN1jnZfplUskxkTwaKghJjI2x5q8zchmPPJpouOX49qz" +
                "XZDpUnRAi2PEQp8sbXrTm9hHXkINemqyxBo/NrachZ0fXd9lLJ" +
                "vXek6aJVUGzsRnoIa21J3BcXE7SjiGPqTjhWswAperOZ3lPrgG" +
                "W86iHpV7rvMiC0ISPv+XnDH3QAxkZDMeMTbR5d3MBNXm8+Ts+u" +
                "qOZ8KGbLoeSZRxTZjnvcOaaLK3oXSPj1z2GLvZf/6qXY9S2RqY" +
                "bVSL50sbbBhCNs61ATX1Bb91SBSO0j0+ctlXz5e2eZ4vbWualW" +
                "xxa69pr4G6vYbLYFyMgRQlhIUe2fMRH3MZxei0eFTS1DlwLySR" +
                "GBPBo3G/IJGxTR48M2ZkGdFkTLF647XttdSHovL5eSP3a0FDek" +
                "Tb+nLTdqgvFvN4Ljz3aEOTD+TFmXF7rHWbr+RcTPZyLryYcXzs" +
                "1Z77Qcv58+a61uyH+LOslpb4rUOicJTu8ZHLPl8dNgu7HqWyra" +
                "5LR+OjUEOLMhgXaXy0yEBOGPoAOptGW47j3shr73i/lPvgGmw5" +
                "i3pU7rnOiywISXgd25wx90AMZGQzHjEW6Jl4BmpoS90Mjru745" +
                "nubpAThj6AloVrMAKXqzm9gfvgGmw5i3pU5GbnRRbEgZjo2OaM" +
                "uQdiICObuSLGAn0sPgY1tKXuGI67e+Jj3T0gJwx9AC0L12AELl" +
                "dzWsR9cA22nEU9Kvdc50UWhCS8jm3OmHsgBjKyGY8Ym2j4hbK4" +
                "w3y6hn+3Urw/5P8JSL35q6ftN9BiBWp8/0+g2DTs/yeQrwz5fw" +
                "K+J46u/ydA72t3x+n5J76vHd2P72t3x8v3tScBie9rVxbG+9rJ" +
                "lH5fG/3Z3tee+mKpaXhfuzuuonjf1y5Rvfe1VW+pKiuYxv6+9v" +
                "3wvrbSN7yvzfNRYjaZUmV9Dx/j+9rxkfgI1NCW++4RHBerUMIx" +
                "9CEdL1yDEbhc5fNL3AfXYMtZ1KNyz3VeZEFIwhcr5Yy5B2IgI5" +
                "vxiLFAH4oPQQ1tqTuE42I3SjiGPqTjhWswAperfH6Z++CauHqH" +
                "l+tlVO65zossCEn44jo5Y+4BscX1MrIZjxgT77Icjg9DDW2pO4" +
                "zj4nso4Rj6gE7dLx1GDOG4N/Lay2fKfXANtpxFPSr3XOdFFoQk" +
                "fHSVnDH3gFhEmTxkHJZPRD8VP1V7ElXKdCl+CFocI5ak8DzE1K" +
                "NPc2ScPzPUgJUNY/NDFnWUydCMy/HRMnMW9vjRMsmlzk6OTEl3" +
                "i2V9dGtdS/3slsbnS1sanod4/5tEd0uTvQ2le3zkso8u9/PTKL" +
                "sepbJtfLZC+XzfHP3+/vXZ/+a1c51H33VkamnQLzRLB6T1fyFV" +
                "oB4=");
            
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
            final int compressedBytes = 3482;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrFW2usXFUVvlcjQQl9UVpL6YMaSit6rTVqSblyZs4MtbQYGh" +
                "80PIRC7fURE4lo+8eke462cxHSYCkJGJNGjQZJRRsjfwz/TPSP" +
                "Mf7qD+kjUHKb2zRpQuSXiXvvtb+91tr7nLlzuYzMyX6t9a31ff" +
                "vM2XPOzDlTbC42j40Vmwvf0otGbtw/T1ZYGEs9GlWvyRHhZZE5" +
                "iOHp5yUre3INMgtbUoxGSDaZlyytj2purQO5gZLz1YxaMXP50e" +
                "1u4xEVm/VWbZM1e6rXMGar7qccTx9PvekLWODq8JwRulhZ6mEb" +
                "zSnNL216L8jZEoecSf08iv9Qobr3BuxVtw5HyPm85h8xXIzEYA" +
                "6wNcX37xwu/2B/k7c7252l2rVsC/YpskqM8AofF9g0Q86hMx6+" +
                "r04Z82CU6sx1wX94rdYY8VM6SmfQc5AIrVrqkOiwt9e6jUdULO" +
                "Jb2iZr6dG1tMtxyjHw/V+rM9bhOSN0sbLUwzaaU5pf2vRekLMl" +
                "DjmTVJf5vvmgrT9krjHXRtt15nqzyNxg10ZhlnvLCltWm5ure2" +
                "y73mwwt5qNtneb922y5eO2fMKWT4YMWwTDZ802306aL5i7otV+" +
                "kpi7zRfNDtvuNLvyvWX2mofMw+YR23vUln3mcTNlvmnGzQci4s" +
                "PmI7ZebJaYpbZdZm40K80qcxOOT7POWm/xSH+mMJ8yn7Zzusts" +
                "tf3Pm8+ZO2y73ZY7TWHati1Nx37G7QzZ3Vx323KvLV8yfv2YPb" +
                "Z8xXzV3G8eMA8qtY/Zsj++F+vdxiMqlvtNbZO19Oha2uWYOdov" +
                "pt7s2FuvM9bhOSN0sbLUw7b2i3X5pU3vBTlb4pAzyWa5ulhNNb" +
                "Ww0Xj6umL1kWfYwlig03hkkEVmDfvzhTSK41hXysMW9zryX23T" +
                "/lwj2pQb/d5JuQ+0/pQhVxzRa4o1VFPrfWswnl6qLYwFOo0HXh" +
                "aZg9g7P02jOC7oW5PzsCXFaIRkSxXk3Kl65E+ZNV+qWEamL3G9" +
                "dO/YSF6do2Mjfx1e+35wF1uKLVQXW6SNxtO3kRUWxlKP4+VIjq" +
                "WNOTpPSVb25BpkFrakGI2QbDIvWVJurUPuGc2cMmrFzMWv1vey" +
                "4/PL9YgFH5/90R+fTVpHy10eL49TTS1sbqvOTN8Oi8Twxr7UD4" +
                "9mgKczLcdsR4xUIbPnOut0cQQjGe+49YxlBlaQMtfPhNWGcqw8" +
                "RjW13ncM4+nPwCIxvLEv9cOjGeDpviTHbEeMVpGz5pk1A8aMZL" +
                "zj1jOWGVhBylw/E1YrtSfH7AvoTW9q8sxx1L/QZKG2P/R5bnrz" +
                "cPmHx/R3j3S9P1s+SzW1sNF4ehssEsMb+1I/PJoBnu7Lcsx2xG" +
                "gVOWueWTNgzEjGO249Y5mBFaTM9TNhtVJ7+ksEeu1FTZ7Brxw3" +
                "bOTor5fSOb3Hx+fz5fNUUwsbjft7YZEY3tiX+uHRDPDosbZLZa" +
                "xKs+aZ04zsTZVUf0hnLDOwgpS5aSYa7d+xq9y2r7avmicxdpYf" +
                "fxt++43/Je77a5K7W3dzvPh1YIvM6S0viyPkah7BWOet90tc+2" +
                "r/gTyWNHHL1rqe7vdO5hz1DINylifsNu5rasfjKPQthuoTCuu3" +
                "kEH4Ged6FCusJ4JtXI2jPWQbD73ol/HQSbY6XSJinDVGfDIbrZ" +
                "xig+5xxZzNxOUMe3GcI/17uYwK1eJ6/mvJNbLHEXJe19bL5v9J" +
                "NFdMdb/GYA6wNcX3Hx4u/2D/fGYk9ueDw+Dd/aP5vY4UC//k/8" +
                "mOOfzdhv359ZGej54rnyvOu5ra4jxG6Nvvpb5mDG+UQfoZ53oU" +
                "y1bqSw4ZSTHOSz32y3joJFudLo4gZZQV+HQ2WjnFEkp6wSCVuJ" +
                "zh2/t5jqw53h6NvX2jeRe7J96/66XRcrv7SsVp/516uWuL08Vp" +
                "3JeivrPxfSnCyrtc2o+shCNfeu3p8so7dLgf56xOhfTru2m0kU" +
                "59V86NWUFnOd2PA4p7UCut+d01yscxOgMx5Dld6azorCjOeoRv" +
                "i7PFWWejses7G42BoT5q7Q9sAUc+WCP7WWQBE7XOSqzsl2zYSG" +
                "ecmYiVynhmUg/USiv4tUbJoTMgJs3pirsfV1yw56BrrfWCK8UF" +
                "86S5vrfE3OD7y52N7sfZ8kOHsa1/JoDux3l/dj+OcN4X7scJrR" +
                "dCluR+HFmdN/gfMv5cbPznD9+PI51H3gpci80SijU32rIqnlXX" +
                "mHVQYTaj5xVtDdY7Ak+4H6c1Qk/A3idH3vKgyhnux3U2dDa033" +
                "Y1te23MULfXq36mjG8+XdqA/wohKMR4QmHnuTgCGenbG6DVWeV" +
                "OslWp4sjSBllBT6dDfNzLKGkFwxSicsZruff5sjwbl4rzkcH7H" +
                "iJ702N5lO7//jCc+D4bLzqa7ib039slOejzrLOMqqpLbsYuXHZ" +
                "5ZoxvFEG+FHYwwzSThk5h/RQq1XkrKwTnjQje1Ml6Wz0zFhByk" +
                "wMUonej1TSM1u5Sz53Uu7Kn6nJz++Emut5m5Rl0PM21ZuDzu+c" +
                "QefXz7zQ8za5HsxJqs01lrvS66DqjUG/mMU9dqo8VZxzNbXFOY" +
                "zQt5+2vmYMb575lPQzzvUoFjj0JIeMpBjnpR77ecQ6yVaniyNI" +
                "GWUFPp2NVk6xhJJeMEglLmc4H53jSPtd9BC3rUOtQ+UrGJMHfh" +
                "vxCvfFd9lD8Oa4enydFVjnrfdLXIrII5wattb16mfTxJFj63LO" +
                "8f39uyM6H33j/ft+1N8/0vvvK4uV5TZXU1tuwwh9+z5GS7Gymk" +
                "HPxep4GlHW6hKNyQcm6lFGtqAFm9QgeViXQ6U2GQEf8wCvuXMd" +
                "xEYo6dUMrIPRQeeqYlXroKupbR3ECH17NEcLY52dCtto5Gezii" +
                "LhIxt6yCujwENeycg8rMGhUpuMgI95gAdL6mf10Ki9moF1MBp9" +
                "ezV6Ubbti1jv7YvVD+yan4Xfxk1xn8atKY5Xv12rnK0p7ZMR/Q" +
                "M6xnnrMqa5q8t1fMylldX1dD/5ff5iOq9cU13O8qzdJn1N7WQc" +
                "hb7FUO1taDHy4+BHYY+d9d8FLvQoI+eI7WTINhl60a9ZSSfZoi" +
                "fJyDODxoif1FF6ZhRLqOpvinkym/9k3IuTHDnwfHQwOaPtTset" +
                "oe5mp6jOn0b4nM3ues7/B7dd85eLy1RTW7YwKi5XZ8qWs7oaG3" +
                "tdjzLATxZZmIE5/PtZk1Fd6yR+ZgVS6nSbjpAz0gqac/O+gALN" +
                "LOMkD2YZyhW7XfI1tZfiKPQthi2MdfYrPlO0kYUKRSI24K5Em8" +
                "jInuJS9CZ+YKCBsNomI6LvUowN+Hw2gl/O6pL2agahI6KDztli" +
                "lmpqyx0YuXG5w9XVX2BhLPV8rlmHYoss5AcOPcqbZlTHZ+JnVi" +
                "ClTrfpCDkjraA5N+8LKNDMMk7yYJbc7x7g2n9qPhWfHrojfj89" +
                "UIcc+Mv/gYV8DnUPDBMvUa4nR03xVX8YfdUz9X5Y0zb5FH2HCt" +
                "VV/AdFdawOR8j53N9MIzovDfHJ/s78MJgDbE3x4J4r/2B/k7d8" +
                "y24TvqZ2Io5C32Kofkth/RYyBD8K4cJoQuBCD1ZYYjsRsk2EXv" +
                "TziHWSrU6XiJhgjRGfzEbwx9igcEIxTyi+MEeeDyLFlcYT+nqp" +
                "OlMdr0cs+FfX34/++3uT1tFzF6/bz4H93f3F66642o2d3XnI7z" +
                "8rAgZ+WOGXVtikjyIpb3d/vQ7ykiKZUeKdF5pZA/LLCPCQRvIT" +
                "C/oyL+cjD+8JYKkPJp4b9/xoJtw3nnH9Yga/jbo+/PHe8oz+l6" +
                "O/kzqT/8YKm/ThV1twZPtzBl5SpH/z1ThoZg36X6X0+xJ4SGO4" +
                "szwj7jLPNP3y7Ty8J4DV/1/luXEv3q0L/zcsd9r+j2xZhJHA3F" +
                "w9Qf83tPXGaN3EKNzfrHvJ/xuWO0134FMrTsVeH/VIiN5Xg/D/" +
                "N/TepdW/6f+Gtn+T/L0u/78hqXX/N/TtdqHQ/98wKlT/N/SWv8" +
                "r/GzZccezp7im3upracitG6NtM0cJYZ3fF1bCRhQpFIhZM1NMZ" +
                "2VNuJZ/UwBmlBsJqm4yAj1QgBiw6ivnlrGhfSGYZxzoYjf7YWD" +
                "ushfaMK+14lLu+f/5TrPd2zXpv16x32No16x0c2e81M/C2B653" +
                "523P9PfJ9d6uXe/tuN7bcb23xXqXOnon5XqnfSHXOzTxeue5td" +
                "V67x6l9d49an7ePWrrsN5dH3VY77usN1vv3aP5eodN+OJ69xw1" +
                "652wzusVNa53qJLr3Y5q1ru1hvXuM/r1Tiy03q1/O2eV692O7r" +
                "FFrHefQa13nhv3Otd0rqGaWtho3P8OLBLDG/tkkR4wSLvk0BGI" +
                "0SpyVpk518URjGT8kcPpjGUGVpAyaz45F4Ve3FmcXaF5W2dxdW" +
                "Z6O3lpTAVjHjX5ZbaE4VV4KKoOw5GosdWjtALNq/S8mqps4tcs" +
                "gy3M3Vupnw/prap+gedDpv1TPb1b5ngSY4CW3scyy3LL/MrCr5" +
                "p7172750OauHtLM8u6eenZKL5LLKVCNf6PYI/Ph5LvHB7XWjrv" +
                "7ypJROeP849JX9MPawzmAFtTPLjnyj/YP8hb/Ta5Unwyas6ep+" +
                "39c55HkT8+q3hvpvpN7192Ti8v/PisfjkH8z8a9ufQ3MMdn9Wv" +
                "ql/Pkedn8fv770b01NSfF57j3T4//15wD3j9D19HqQA=");
            
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
            final int compressedBytes = 2212;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uMFFUUbVBGxEQHgpm4EBAS3WhcYAgjxpnurlKiAUcZPw" +
                "SMiSbG6EICuFATdboLqRnlp4n/D6IGooAoMcENGvGzGiQsjIkL" +
                "FoawgBhjooKoWNW3b937btWtT/erGePrvFfv3e+5Vbdevfp0/V" +
                "j9WCUo9fYW+zAefQC5SOVczqOKNG6NrALN2YNWuGWzmH5I3sQZ" +
                "x0UYTIxICX1zLdMC98+r3A8SMY++UnG/C380ghrsz4dMGm9NPd" +
                "6SBi/ch7NTcmUx7catxb3jiJCZuHHr7Eyyz2nmXiCb6INHkhVH" +
                "WJpTsTe6JsbbW7FQnA+6t7Hh5gy+W57vPKW6GVvotfbn44L7IP" +
                "VhDBTdHsrpPE07W8aUQuSIUUOGGun2Nf9SOy7V6Gu106PxZSPr" +
                "g7Y3yMUfRp9oUa6wedwas4Mc+dSCnYsy+Jcr+an4bsy0FJ+6P4" +
                "P8fCrP/mxcksJbkLg/d03i/tyVd3825hbCcyVsB7+nFnphP6xB" +
                "fo6YPBsltOZ8YsdOJ3zNt60oU/Nzd0n5ua973M1DHebnvjLz0x" +
                "13x6GFLdLa4y3BdgvQSYZ+IC0r56AHTg9i2s1tcA5uDRQxrxG2" +
                "RFykQRgISejbjJhbIATSs7mvCLEpDfk5+pGSn/tKys/9eY/66M" +
                "fW83N/yfPnAWqhF/Zh7Lxh8pSZ50Chee+ApXm4Q1QQU3m40uZP" +
                "94Ww2s9PK7g7vL77a8rJz+i4fEYt9MI+jJ3XTV6ahWL+LORnh6" +
                "ggpvJwpV7ff+x2/kxez/vrJjE/7y93PT/4LbXQC/swrj1t8tIs" +
                "5OWRdRu4i/MhprxIu17Vna1UwvxsX11PSb53YYb+OWP0B8yfzd" +
                "8iyl+No0FM3yjaZ+I0b2pQpyfI7lAx/NPKt8Pt0e/t7d8ta+dL" +
                "383TCT6n5Zs/vSlBPc/riR2JM1ChdbZGc83WJLlBEXXzYGYuMY" +
                "3Qpvtqjvw7k/GMaqspgzEgTdNH31n20/lZ2rxQfnoXi6cry+RY" +
                "UpSnNUyqEdzD+hu6P5O83nRfGjLfq0xocVzHpb7AulyOJUWJcb" +
                "n0keTX0nPH5ck+bfvJgeTRdgathW19Rn1GkAcP2/cU2u22+Gvz" +
                "RZPXd2O7xWc+p6FCy65HPwfXjnEpN3i6E/v8fK+NF9NJvE6Pmz" +
                "IYA9I0ffQt+RJTuv/sfVB9HlvotebuzVAj7h3UhzFQdHsop/M0" +
                "7SyZEBeXQuSIUUOGGun2Nf9SW0p5c9LW82NTynke4r44eet5zb" +
                "et5yEJM/YA9sZmVkop7psWrpsDnfFt+M48u3qhQkvz59ilSXLV" +
                "3k7sR8dxSRDTl8V0EvPhVlMGY0Capo++s+yn87P3QXUTttBr7c" +
                "8+kxvvZ9krzkOJLJnwfTGXMpFXN2nvi1Em3b7mX2pLKaff6a8d" +
                "D1vYBte59igcj813+psngU4y9AMLyMcKcjACefQEvdpxbsXfiB" +
                "zQCbnQQzpZ5Tibp5ArcZEGSIJVoAAiroX9xnbSNePiHjiS0GZ7" +
                "bXCca7bviqIrknd1tLc/T7x/ml/0fjN2b3NNjru0e7Lzc4StJU" +
                "dmBbWP8ZX89NtvzEcWZfi/JZX7iDJrL3WWQgtbpMF4bB5SuAz9" +
                "gNc8iLpcjlsjq+057Gtug3Nwy1HEvXLLcVykQZIkH/o2I+YWCI" +
                "H0bPojxKZ0exa4ACq0jZ+iPFosZo+WHEgWuh4JDfer4jpZMhgD" +
                "0jR99J1lP52vWp/nzoM23BINxmPXAhUpJAs9GDUP8hHI88pttM" +
                "+5Me6VOHEM3ApRpIwpwb1xu0DxR03fJg6+Z0zP0qOJmHylPV9y" +
                "Yu8QG0cKrrrj6/mjdtZ4Xn+G58PKDHcoN/a5dpCy8/2Gcta7/t" +
                "vlr6mfmaP4fqt834N/QoWW5ecXSXIgWdR+GTpcBmNAmqbvb8ln" +
                "P52vcWuzarOghS3Swl+9p95jUkiWS5v6vJIHkmk9M+uJW4zjMq" +
                "1yipQxJbg35BAS6ZvLmrhNz6Y/smlKJ85M0crNu8n+ueCF9zXn" +
                "yj/nPEe5MpfsW3++VB+oD5TzfKk+MHnPlzTf5T1fqr3CezQKju" +
                "0qcaxXSUrcRpIe55qS+Yumh77yIesehzgr7vNWeHd7txu0lRG2" +
                "6zu2e6fKWR3Uey0gH87gK/er+WLy7rI480RfUFSXCCwr5VhSlB" +
                "hW+i9XJqggIg2ZjKncYn7P4L9WdD2f73sGVXsCvmfwxbv/Mr5n" +
                "EDPJ6nivfWyH5FhSlJwYSrLeXdHsoC8NmS3/Ba5U0ZvY+nMC62" +
                "1yLClKjEKqNsUCyma6Lw2ZDd/FCvs+ZL3AukKOJUWJkUk1Riwd" +
                "9Wa6Lw2Zt27C83NadCyvqvxnS6f/j5v4mLwno+vRdnHsh+W4Op" +
                "wrP4cnDj360nx6103W/VGlMra4nPsj/53Juz/aOFrm/VH9RP0E" +
                "32Ifxu6zyEUq53JevHJrZJWo0mLsvtDwgyOJM44L+RIjUiCmuG" +
                "1zH8h45H6QiGXUruM61Ica5NF7XAdksOXSSX1ZQj2ulSZLtlAq" +
                "SZpsIC6OjOtjG269GzkWE72MhVfTTxb6YL7Zhi30Wuf7oMmN97" +
                "PsFeehRJZM6/07kzKRV7ep79+35cGg+ZfaUsoZcoaghS3SYFyb" +
                "jRQuQz/i8co56IHTuQ9TA3VMFHGv3HIcF2mQJMlDTHHbpoaMh/" +
                "N59ISWYxfn0cnoWripnGtg9dfyr/Pq+449E7DGWAQVWnb//n6S" +
                "XHVRJ/bL0OEyGAPSNH2vms9+Ol/npq2Xao+Vs16yss7rcL0EMW" +
                "Wtl7yXOvx/8RH3CLSwRRqMa/ORwmXoRzxeOQc9cDr3YWqgjoki" +
                "7pVbjuMiDZIkeYgpbtvUkPFwPo+e0KJmWn76H/7/8tNbVuZ6Hs" +
                "rgWajQsu8/a6YOyIFkkSI1qr8U18mSwRiQpun7e/PZT+fr3Eaf" +
                "/nyptrDrLFqQmCM7LTxlyPguwXtXmT8X5po/d3T4fci/KpNcqg" +
                "==");
            
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
            final int compressedBytes = 2201;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uMFFUUbd1IkAQNOqPgb/yMwdEEg7hxM13dlSFRE13AEF" +
                "eauDMk/sIGlNBTJT3dfli50LBxAQmCGGI0uvCDunAB6iAf/x9Q" +
                "GBUV0ZigiVb17Vv33PeqaqprunpaqvJ+955773mv3qt+VTVTKt" +
                "UGS8FRm1dqH7VLNz0a5BeE9UalJRkqpR61hSm6ayzJRaVS+Uxp" +
                "1kft/Bn0l8fLk2LXLuSat6stubIjPsNh7gw6g5RTyTJqNx7REs" +
                "EieuJNlGCSCBKj1ad/bI+andZjW5BapvWmrTAxYyNWeJf/NiPr" +
                "eOKTuUjdPLw3uNZYZVzbe8y2KUmYE6bd6axXvbG603nLsZKY1X" +
                "eXCjy8qSB93a5PB+kT78dwvbfaP1dvaZV/wqz+KLIMat6BVu1Q" +
                "W/JTkE4F6cN2+3PvNK13b39kdbg25X1R/iuGyVfeN94x7zvvpH" +
                "fQ+xTkvwTpD4U84n1fKk28oGSfQf1L74eA6X7D/4lW/psZ2/s4" +
                "cQVnWu/er97v0nJPuicpp5Jl1G48xhLEyCk6TK1zAL2J13bUAf" +
                "ShNCeRmbDSUdGzzUssBAn4AbPH6CHCDpiRdTxhLLy1RA6en8F4" +
                "Pm5ds49s/MSbMVdtVfzvkb+gFqyI+quzX1l6fsbMrv3x8icOZJ" +
                "p1q7POT3+ePz8dAeO5MW9v3UWJ/ZxK0xY/ntlid4NhzHhuyjI/" +
                "Y/ksSJufrJ2T8VyQlX+++Zm2/3RuLmb/2Y0j7/6T+pS2/+zs90" +
                "jvP2Gf8QznVGvNzycM7RqpU5sksfuWZ6C+JlmXZD0zRqOYOXNM" +
                "YsYW6f6T4pvWcSj/xnZ5Q5Au9Yd4vfvX1F/Lst79JVC/XmmW0v" +
                "z0o52uf1ltyr8qYeWMtMvFna93H+aSf3Xcevdb89W/zrvXkF8y" +
                "u/2Sf60/nI5wGnatfTXuNtumJN7SRNU/KG5HzbGSmG2+uFT4UV" +
                "5BifLaw9HYLwtG5kUTR8gZ7ibfmv7zcEq96i9qDPeBZUn23B9T" +
                "j/2cOX4nPZLxdDYHaQd4MZ7+yqvLCc+DoWWyXTcO5BUXK5HZjs" +
                "7kXWDq27U2R/N5flUoidvPa0vTznlj1hwTPXAsM2Znsesnujie" +
                "T9m1Nsdxs21K4i2TUIXcu8bTY/rLSz0+5P2Sc1NeH/6KUp8e+f" +
                "uUO+KTdi2/j8Ku+l05r/StRbJyh9whyt0hlFG78RRJWSJYqok9" +
                "tsLSuc+W6RgiEY3NAb2IxMRoBEZDvyRhZqZvYYoMsb86ombM6O" +
                "qK6grKqQx11ArrjadZghg5RYcpPJ0H0Zt4ZbboAzU2Ar3aPON4" +
                "iYUgBe88aPYYPQgDM7KOh31BdOyc3RU9bz6bd96PvtSv98/eM3" +
                "Pficbzudysj+l24/m+Gc9jvY44sTQahZ25WX/Xt/OzR8zKWzin" +
                "WmumbqGEMl2fyV/nOkbMhHG3aJRmnmzNmnT/SfFNaxPlr8T3n/" +
                "6dxvf3g+b7T/+ObO8/nbV9+/5zbZb3n/7ts33/Cev93Gi9Hw/W" +
                "xx61WvbE4GOeN+NwXb7LT+dc73t6fYeR9yGN6dx3qR2lPj16zw" +
                "y+H53K9PsVNz939+147p7D8fzHmrsZvxc7D8TfP+l7XFd2ITm/" +
                "xzGzDOu0S9+L/egrcfPcvL2tv92v87P+Vo93nwvl/tk8L/eq2t" +
                "W3671QZtWR6gjlVLKM2s151ZH6uyQnWX0vawnBOkzhOboNvYlX" +
                "jCtRRWMj0KvNE7nbFoIU/Og2s8foQRiYkXU87Aui0+env6l01h" +
                "0T83seMdpxNxcF95v31d3nvYyr6tO+Xe+FMquOVccop5Jl1G5e" +
                "zhLEyCk6TOE5ehS9iVeMqz2y3ESgV5tnHC+xEKTgR4+aPUYPws" +
                "CMrONhXxR6vDpOOZUt3Ti3m8MsQYycosMUnqPb0Zt4jeKDD9TY" +
                "CPSKkub1JnfbQjgIk9HtZo/RgzAwI+uxwr4gOn3/WXVy7+df7t" +
                "v1Xiiz6m3V25ytYU6ls5VbXKeckIKlkzywnhPhqEV4LskCY4jH" +
                "UE7ewpP9aK82zzheYkHMyCtJiAlaaQ9kSyjUol4k0TPCVrFM/f" +
                "u69db7peczvl9a37fvl9Zneb+U/flIv1+q3l+9n3IqWUZtZwNL" +
                "ECOn6DCFp7MBvYnXaF2AD9TYCPRq84zjJRaCFDz1yfatLcz+oB" +
                "57L2yRe9L9s3747Nt/ehuL9F45UzlDOZUso3b9c5YgRk7RYUIN" +
                "R0A5xtAWbKNZ2FHRs81LLAQpeG+j2WP0IAzMyDoe9gXRsWP8r1" +
                "3r9Pd9hqt499zNz2yx67n/gy/192iJ+XsU6+H/9ffeS7L8Hvnb" +
                "uve9Q+6f/nZrLmb8u7OJ9pv9yrrYObJuDufnuqyobrGsHY3Gs6" +
                "BvA5MXzd14bj7e64gwnjsLGs+BuRvPyUL/3ttd6a50psI8LIO7" +
                "yxS3wnZYR4lg3ZWhrdhLi7yyjPChX7biGIQWj4QhLUYUz8JB89" +
                "S8mIEwIx7MzoyteUR32SmtFQ+aB/eIec4wPwt61p0cPFvXuzvm" +
                "jlHujqGM2s4VJGWJYKkm9tjCNsp0DJGIxuaAXkRiYjQCo6Ff7J" +
                "PtW5giQ+yvjqgZM7qyr7KPcipbv237uD25iCWIkVN0mFDDEVCO" +
                "MbQF22gWdlT0bPMSC0EK3nvN7DF6EAZmZB0P+6LQhyqHrL1CSx" +
                "amyUtIy23GilTaotc+4yKIhqziMSwVr3FIkSBDHRdtvL26F8nx" +
                "dZR0iY6deP985ex7fq8X+veflSOVI5RTyTJqOw5LECOn6DChhi" +
                "OgHGNoC7bRLOyo6NnmJRaCFDz1yfatLcz+oB57L2zZMu15s7ms" +
                "X583y8fzPm9OLi7y/Wfc4Uf/Bdtc3q+rtjzde8suPB+9XtAu7Z" +
                "y5uxLFxnaH3WHK3WGUteUPkZQlgqWa2GML2yjTMUQiGpsDehGJ" +
                "idEIjIZ+sU+2b2GKDLG/OqJmHMX6D7ZODb8=");
            
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
            final int compressedBytes = 2214;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXEuIHFUUHfwGxV8wRI0axxkMSIRAZkYkgi3V5UI3Ll25EL" +
                "LRleLaZLpngp2auM1OJIpm5coPWTiC+EEUERWNXwx+knEymcQY" +
                "Y2KMVvXtW+fc96q7K9P9psAu6lXde8+957zX9erXk8Sb4k0jI/" +
                "GmuL2Vj1iZ3bpZvOoBVvbE0j212Gaf5YAHEV8DV4HHxVgEs3Fd" +
                "8cy8Z7mtjpERq5D7axmtYnBln9rGbIElazqejo9bjtiW/Wy7HL" +
                "0+ilVcER4VVReUuRH4WrcW1WefHQXurXBwT4r7URvNFliyptzj" +
                "1sctR2zL/l4cPcdz1FYswqOi6oIyNwJfa6yoPvvsKHBvhYN70q" +
                "8f2afxk+7NvjUS5FM/MlLZJyx3/AGWzGJfuneP9QiG0exTSyOM" +
                "AEZ5bRbyXF2MdWsU6bJsrgLtk18bfWGFtr92YcU5+n0smdWO5X" +
                "YyYT2CYTT71NIII4BpHyNn3CzkdfS97/PA42IsgtlcBT63q17r" +
                "u8yWz1XMmT3m+zuBZsWl1c331eem8Xw3UJ8uqnA8g3LXJmuT0m" +
                "Zb+MRO7hOveoCVPeSzxbb6khpz1P9hVkR8DVwFHhdjEYhpRfh8" +
                "bquDR8Yyu4xWcY6eqk1Jm207sSlZmgeTR8WrHmBlD/lssc0+cN" +
                "R/YVZEfA1cBR4XYxHMxnXF43JbHTSeDrPLaBUrOtob7ZVWtllM" +
                "rGw/eVI9jMGCmBvXiGXQSH2Jbfg1x6rwWf3KlkFtIIHPuG2PuQ" +
                "IUuMzFPYFa1m4/0Uu6lzzTLcKf5nz3CsWe+q/hz5PFWsNzN9a3" +
                "2zW5feP002l7bXs8d7c9fZ4BGtf0iI15nuvTPv0xBN1X9onf0m" +
                "U8u3A3rvM8Gy9Izx0lru8fBnpG+a3C56NV56bx/ChQn44PXmP2" +
                "4+q4e5xl9kf7pZWt+sROZtTDGCwSa877cc2yDBqpn2Abfs2xKn" +
                "xWv7JlUBtI4DNu22OuAAUuc3FPoJa12092/uxcj1plvhP/etT3" +
                "GPm3wvkemFuuR13GMxm4+lhhn84Nrnvm+T7xF7uM57ky16OZfR" +
                "d6PepUv61+W7EvW5M9ElVbsfDC9uM9GI5pRLKKMMjUVpduPWAF" +
                "ltfoOeaq7MZvWXp7LLczf/Mn3OS5wivBNwPPuaMlrjdfl8B8Qf" +
                "tfpuu35blnv+pT+/MV9Wx9fb3uWV+2Ji+LV22L5YyiuMtAvlMa" +
                "kawiFcjUVpdePWC9Vh+4Xa6iqv44dWN1RmCsPiatbNuxMbWTV9" +
                "TDGCyI8coRZWB/un+Wa5hIfr7luMvKlX1dyACS8GfdHnMFKHCZ" +
                "LR8UW3Sf+f5qmPkeX1bdfFfuQPN9vD4urWzVJ3bymnoYgwUxXj" +
                "miDMnr8Ke+Ba5hIuOsDKosK1f2dSEDSMIvuD3mClDgMls+KLbo" +
                "eDFe7HjW5d9g25etyQGJqq1YeGH7ca7mHCOXa0Sy4sWdD7sqkK" +
                "mtLk6tRWaV/azKzlutPnC7KouqeoqdeH2d9YC75/uQd8O8D4kv" +
                "KTt/kveG/T6kG/cw34ck5pek6QO5/4NAzyjLFT4fBeWO3o7ell" +
                "a26hM7OaIexmCRWPr87sU1SxnYn/bpJNfgiG5Zhc/KlX1dyAAS" +
                "+Izb9pgrQIHLbPmg2KL7vF/6OdAx8nuFx2dg7p7P7z8O+j6ky/" +
                "P76Qqf30+HfH6P38CSWexLx/MX6xEMo7O95jzHBM8r1+j0aRGV" +
                "OKJxl9HVWayd426u+nxuqwMaXGbL5yrmzB7z/XCYORFfXN18D8" +
                "sdv4kls9iXHp+/Wo9gGJ3tpccnxQTPK9foHJ8/oxJHNO4yujqL" +
                "tXPczVWfz211QIPLbPlcxYqODkWHpJVt+1p1SO1kST2MwSKx9P" +
                "p+SDHAcTVU7Yznea7BEd2yCp+VK/u6kAEk8Bm37TFXgAKX2fJB" +
                "sUX3me+Lga6xf1Z4fQ/KXVuqLUkrW/WJnRyzHmAZ3ZxnD69gAE" +
                "e7T4f9ir4uW5U9LsYimE0jUOJyM9bqtsyWDzUtuvD+Z3d+v3R8" +
                "5H/3mT1aBtXcM0TG/IksOR1ozv1d4XwvxT17Yojjmf8FRX3roP" +
                "fzAY+zUyvMO1kKtcKzbO147bi0slWf2HOXWQ+wjLb5vIIBmPbd" +
                "RUFFX5etyh4XYxHMphEoibvUxlhodcts+VDTQS/XlqWVbTu2rP" +
                "bc5dYDLKNtPq+am1Zao5j2cf+XX9EZz2XLAxtI67NxNxdKXG7G" +
                "Wt2W2fKhpkX3/r1j9kyYudq6s7rz57OtkNXv33r/Vmllqz6x44" +
                "b1AMtom88rGICB11b0ddmq7HExFsFsGoGSrE9FtTEWWt0yWz7U" +
                "tGh6q5S/oZ/ZnB9Hm4u+g+nbB/0Wd13THzP9SAnMFbS/Nl3Xl3" +
                "gv1Zlz01N9aj/YM/rEBbzBy5+Pdq0NdM/yXYX3S0G5o4VoQVrZ" +
                "qk/s1l3qYQwWxHjliDKwnzlshuZYFT4rV/Z1IQNI4Gd2uD3mCl" +
                "DgMls+7guj83c12+PtWSv7skZboi3mXVSKibYwUtH+vmaiQpan" +
                "8axK3zdf27liER71RJFVxvnaSq8416q3lqjkcQFPf/10duv8Zh" +
                "tNRBPe8TxR8rif6IUvWyXIjJwYPrL0+fOGQOewHyo8f646966b" +
                "8r0Ngfr0fYXjuercjac6x/tkNOnNgcmSc2WyF75slSDzfXL4yD" +
                "53Xvnvm3NXDfzd+H8f8ll633B32fy5q7u+hdnXh/mTLs9Hj5XW" +
                "vnHo4+n9zUrj02GMZ33H4CpXOp7luYc1nvl8H428v1yKRkvOld" +
                "Hu+KK6qzjfR4ePLHd8Ng9G27yjouT/FtHUu2KvQmNbkXcF3/pD" +
                "KxzPbcNHFtwbPxA/gH1Zow1Ren2feZwx0QZBWrS/H22wW8mWeF" +
                "aX+bop4oo+ujWFGqLIKuN8baVXrAV1Gy9YTdJXjIbl6afeHJ/n" +
                "c833Broj21LdfJ/ZsdqMOp7Ng3OlziAr+Pdc0xXef4bl/g+Jyv" +
                "8r");
            
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
            final int compressedBytes = 1568;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW82LHEUU70MOwTkYQRARFckaEMEcPASNSnc7S/xAyEUEPS" +
                "QaP0DFxJ3Vjbsq2ZnMxJbG9SN4DII3r+oKfozRVdEIuUkE/wT/" +
                "BsWuedvz6vV01VS9qppSe5ia9+r3+vd+9aZqqnubzTt5J0nyTr" +
                "77mSS1l3cGv5f7aQ/GytGDsdwjv+tzkQP6uq/PMibkoLjsYyTt" +
                "o3jzXFTSzC3HUt00M82HnDS67dj8Gz6rei4lBsdgnFge3ZeTaE" +
                "fY3Gkv7UELn3WfeFX1PEB7MFaOpufLb8yAMZMx9WYZZ3VRVrmn" +
                "GUMj5Gw1gkqaueVYqptmpvmQk0bDsXyreKEH7yQpb6N9couImJ" +
                "/gYy+1mzm6p5po86hj67i2eGSsdaEyer7c1z3Vxi/30SrIo4Uc" +
                "8kjmjWOy3len6/32QGvuTMT1Hjh3/7pJu3fqXy/q2d83qecdk5" +
                "5b5jBcrcH2z/RcW41p1YPuzhz8RkU9Fbn718je2Q+rnput9BxQ" +
                "IaN7pvPzzkBz5M3w83B0OE7u9GdswRI2+PkRiukYTDFk96HbHo" +
                "cxmSq116Nd74cCrfeNiOt9w2S9T3pY6z29gi1Ywhbvqp53U0zx" +
                "zVyxwURP9zUP85OpSpW7XamtnuXLy5ehhc+6D/zyWN0jx+ALsM" +
                "G4PleOk9kqpuPYX43pFZlDRupPWcVsVpl5VheegZEYL3LTEcsM" +
                "qKCZmeZDxTRat96rKjwZaL2fjLjeT4Zd79r9/Wig+803Iu7vgX" +
                "Nr5+eZQPNzzV33YIc5P9cC70fb2IIlbPCL+yim+CXetvrd3rY9" +
                "g5dVhQ/v8jEKTd4vsAVL2ODnj1BMx2CTz+4MXlYVDmNyHQXzfn" +
                "PTdb23RIv1fjrifnTadL0zv+cdbMESNvjF/RTTMZhiyO5Dtz0+" +
                "TG2UMnT9gC1Ywga/fIdiOgZTDNkd68lUlQ1N4kUUT2X6DbZgCR" +
                "v87CLFdAymGLI71pOpCsZkqpSh6ydswRI2+MWjFNMxmGLI7lhP" +
                "pqrh8zZKGbouYguWsMHPD1FMx2CKIbtjPZmqYEymSj3v71uBru" +
                "fXI+7v62Gu56ffwhhbsIQNfn4vxXQMphiyO85PpioYk6lSz/Pz" +
                "3UDzsx9xfvYDz8/vsQVL2OAXL1FMx2CKIbvj/GSqGo5slDJ0fY" +
                "UtWMIGP1ulmI7BFEN2x3oyVWWrNkoZur7FFixhg19sUEzHYIoh" +
                "u2M9maqGF2yUMnR9hy1Ywga/fI9iOgZTDNkd68lUlT1tEi+ieC" +
                "rTL7EFS9joU1vNYIo12dn1ZKrKTpjEiyieyvRrbMESNvrUVjOY" +
                "Yk12dj2ZqrKnTOJFFE9l+iO2YAkbfWqrGUyxJju7nkxVxQcm8c" +
                "V5PyrFIT0/6pnEM54fvZoEP5TPjwLnTn/BFixho09tNYMp1mR3" +
                "022PZ8dM4kWUnco6Wnd/tNvj//5oJeL90UqY+6P6eZzu+aZ7PV" +
                "uiq3oWD8WrZ/GgaT2Z6+ZTbMESNvrUVjPY5LM7g5dVhWfPmMSr" +
                "olz2I9PDfj8qHoi3HxVHkgUfWM9sxxfn8FklcoLHWBzl1dPfmB" +
                "Tz/FdswRI2+NkSxXQMemx0ldwjnzHa66LbHs+WTOPnZWiPTi9h" +
                "C5awwc9uopiC6ZINhuyO84CpCsZkqtReT9j9vf16KdsTb39X5f" +
                "5PXy89EfF66fHA10ufYQuWsMEvPqaYjsEmn90ZvKwqfNTxMQpN" +
                "3s+xBUvY6FNbzWCTz+4MXlYVXnziYxQ2x+b0iUD2W5jrz5iH2Z" +
                "gcfofI72e2voj9aPSYc1XW2fuR0fN3r//PNb3XKtfCzM/0r3jz" +
                "c/G5R89N63kuSfI//NdzPmc4DtV5PjTN+/0Mcf/e3/Sj8uw+5n" +
                "3qCyHnYneruwUtfNZ94JdF3SPH4Auwwbg+V46T2ZB1d0wvyhwy" +
                "Un/KKmazysyzuvAMjMT4ty40RywzoIJmZpoPFdPo1hk0/Ytr+X" +
                "agvy+txPv9PPfnIvf3xdxvFh7+X4a7v6vqGe7/uaTrzwDP4/qH" +
                "Pc2Dh5lXWr1Fr4jA+1FVz+6eePX0kZtbz7L89+7vxfvM+XncX5" +
                "T1/ebB/+F6Pxhxfp4PU8/shnj1LD4KWrx/AC26n1Q=");
            
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
            final int compressedBytes = 2071;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXEuIHFUULbIyRowDahBMjAwR/MC4C8SNqa5aZCYI4icKgu" +
                "ByEjczLkQzwamuVKbA7BIlGZGJBGOUceNnNiEb3UjEaHSpq5gJ" +
                "foIfdDEbsavu3L7nvnpd3VVd1YVjFfU+5557z32v36v+pDKOkz" +
                "3CV7n1+klngKN90Sl47L3baexYeHvUivMv9bYFjw0bPXjEcbyv" +
                "hs8ymMq3v7bDjlehXd18Dr8+0/n8uv757HVUoV3h+iyYTTCeQa" +
                "44Trw8fJbtd/oo91iHx64PnPs9zazPEvs9anB9RqNen8FMnfs9" +
                "fT96r7n3o9FrF53PEmM62+B8jly7/v2+9/3m9nsV2jl3k+PecS" +
                "qpZoz6giBHTrHhZfpiVFKNP8EYaOEas8iqYuRsXuKB+TMSf2yO" +
                "GCNIBqay1pOMNbuB9TnfWSPnh48T3lZyfZ7fgPv9XIP7/Vzd8+" +
                "dew9q9xvNJ/fYvbEeuzb93TNOGaPyp9kmsNi8zdvvXfjkgamvp" +
                "drCUP66sgj1msC0tb+r270rmM0h3kz+dIvf2WSlb+3+ep0gpcr" +
                "vjRAcqWJ9b+ti323HJxOCPqbvJG0U/zwf3pTM75861NiUl1a1N" +
                "3ON2OC2IcN25xJciMEYIXQmWWMnGSoxhRLG0NrHVtDOHc0hY8Y" +
                "rG0IP5lAX7sIr2Ym6whKOiuUBl9JM8hM3t3PV5qLL1eQjXZyX3" +
                "4bLr89Ag67P4901an37sx+5iUlLtLnKP2515T0vhyEkR2M4X8a" +
                "hHfFaiFmpIxASnaMnJfB0V8yTMlpd4UGYUlfnmaHQE8iUWWtEu" +
                "SPf+uSiend6a1O6au9Z9P1pLkHCa7chV9+UczG6zocxNrHY78t" +
                "y1+GK/HBC1tXRbvx+RRr6CLWb0Vu5+P2ju92ix5H4/qN6PTjW4" +
                "3w/Wut8v+Zfc1aSk2l3lHrfjzwQRLp0UgbGkTREZ67xyq8LjFm" +
                "qIR4JTtKydOZhne8XEzIiE8hip5oxMu38pWBI1YqHVHLnEZDa3" +
                "LZ/n/+m+li+atuhMudWUjdTUUXcmeft9Hang/R2Qzn733hw+7+" +
                "hsuf3eS7ua/e6d8c74C0lJtb/APW5TSUzh0kkR2M4X8ahHfFai" +
                "FmqIR4JTtORkVEfN5mnLSzwoM4pKCGWEXjoC+RILrWgXpLvmF8" +
                "Szkd/nTzT4+/yJOkfn7fH2UEk1Y9QXBDlyig0v0xejrquexBjK" +
                "sgczk6y0KkbO5iUemH8XOWmOGCNIBqay1pOMNdt6Z1p2NvARfT" +
                "AQ68OS63Oft49Kqhlb719gBDlyig0vtLAC4qihPdjHyCKjipGz" +
                "eYmHMIF/wRwxRpAMTGWth2NR7ClvikqqU9sUna2x1pg3FU4TLh" +
                "w5iW1eaGEFxB0niSsx0OJ174poN1WTK/7CzD3rITlIJom2HjG1" +
                "gyX0MMeDEXH0ku36NelNUkl1apuks7W5tdmbjL8kXDhyEtu80M" +
                "IKiHfGtBljoIVr0cGo2TxteYmHMIWfaOsRU7szn+Bhjgcj4ugl" +
                "W8zdeOd8tvZfsD9v7v5Zr7a7y91FJdWMUd9EhIvs9kVE8BIF0R" +
                "BUR8zmpaMi0umvmJhhN3xhrCv22DIX6b8aXDaVtZ6eHz3i0a/P" +
                "4Y9jd5b1jGt9fsnb7e2mkmrGqJ+03WXChSMnMcwLLayAeOeVXM" +
                "YYaOEas8iqJhfFsOUlHpKDZJL46RFTW+JJJhhXz5VkrNlFvx8N" +
                "+zzY0VvS58GuDL8SSj8PdsfAuQ/0/f3oTUdvhl9bxv1xKv1xxL" +
                "jPpWDYEn/sYR8x0XDnUdVUyypi38bRDFQzR5HV1nngzGhlU1Fn" +
                "LFqW1+WFuu9+8XfN3Xnjb2v9NbDtt6mkmjHqm4hwka398RIF4a" +
                "Tt09mI2bx0VERMjmagGltgrKftsWUuOLpW1np6fvSIh12fxZ9X" +
                "jB5tcH1+X+uvZxMW7MFSkR7uth4Y5fwEDxWczx9q/Tw/5o5RST" +
                "Vj1DcR4SJb++MlCsIRVEfM5qWjImJyNAPV2CKZ+IftsWUu0v17" +
                "2FTWenp+ZMT/v/Xpv1wdy/J5fr+3n0qqGaO+IMiRU2x4mb4YFX" +
                "V1RMZNBkbN5mnLSzwwf0biq+aIMQJz4x9NZa2HY8FZcGfdWSqp" +
                "TtfuLPdNRLjI1v54sa/E6O7VTERjv89qHenbYmgGqrFFMvHn7L" +
                "FlLtL1OWcqaz09PzDqGXeGSqpT2wz3TUS4yNb+eLGvxOjORSai" +
                "MZ8zWkf6thiagWpskUz8I/bYMhfpfB4xlbWenh894swdacbZwI" +
                "f/SnUsy/v7TncnlVQzRv34Z40IF9naHy9REI6gOmI2Lx0VEZOj" +
                "GajGFslk4ZQ9tswFR9fKWk9ianb++nTv33jrs+4xFXs+JLpsiV" +
                "D4+ZD4RgV5l3werNf/N9TPh0QflXs+JHw+fMK0hBX8nuz3/FUs" +
                "fC7POvgRPtnnO+03RTMrzrJk1ZnP8ED4eNXzGT6VN59VHP3mM3" +
                "ymx/fN3waK/nTJdbSdTo1VsD6351nqVci3D6ZdNkN/B50aq2C0" +
                "O/Is9Srk2wfTLpthbffPbbn3z2317/fimRVnje7+6W/Jnc8tzd" +
                "0/B9OuIsMqvx8Fud8vvHeb+/xZr7Y74U5QSTVj1DcR4SJb+9Pl" +
                "b3UnUEE46au/NRsxm5eOiojJ0QxUY4tkYmojV0ZF+Zvj1afMj4" +
                "y8ofXZ4N8T8P6Tf0/Av7XnTM/nWQu8Yu2qMyvOsnyL+Du6Fl2N" +
                "/sTvm9H1Yv8fwZrP+l+siv7KKP4k1mGOaLWP/ff8zHI8bxAr+q" +
                "NEWv8C5+1XPg==");
            
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
            final int compressedBytes = 1788;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW8+LHEUUHk8uq5gIBncPiYSwORjwEkTEg7PdsydR9yfGhI" +
                "CsoBBhD6JsQi7OzO6ONCjk4sXLsM5BEcFDSFhYluxiwB8HwR+H" +
                "xKMX/SfE7X79+n2vqnu3t6e7azM9dFXXe19973vV1dPTNTONhr" +
                "21P2pUvLW+ajjbqo7deTYqx5L2ZDienZOAOHsIwwnb1vw69p2z" +
                "0M+UpPuJQ/yn0+2szMI/ja21L/Ytzx1Jz/ksj3fVPipvK4uzKE" +
                "8VOR28HfV67+4c6Ty2S5qf3WL9Nl6tcuy8K94VKqlmG7VNi2A1" +
                "AvvTcfMb8SMHxtWMbE/zm1FNVRqBGXBLlJAym1vGQpSYXPql9c" +
                "qxdeY/HP489R47yBs85e5+1DtV6fxc3X/1o5LqftLiY7QIdjXs" +
                "GzP00UJ7aIu8/diTYKLjFMbQnnjBnzBCD4tjVfdI8P2kb59qUW" +
                "L4VxOloruvIut4CSej+dje1m4mo31rfy49Weg83Sruzfmufbfg" +
                "DMqI3elXdkU83hjhzXu/UvYL3gUqqWYbtU2LYBGt++MuEQQTvX" +
                "+etBltXZoVLSZGIzAae0RJ71Q6t4wFs+vIOp4eH52xNfM/TxRf" +
                "L3yerh/b+VmLshl/xpdj2tMxXIpVPn+m9cLe7PduY7wsPDJm6x" +
                "HFWhn25zKsvduoBXlRU6hQRsKOk62+mufNxJf6vOndGXqW3Sn6" +
                "vJkVWz9vRpZCz5v2ePbGqx7P4Ky75/esz5/ljKe36C1SSTXbqG" +
                "1aBIvo7g5acJcIEiM63rYZjVm0qONIO2bbNm2G3+gLuW6nc8tY" +
                "RHfM02ZkHU+Pj844+35UxfN7FP3e0Nf7vfp75loNnGvNUUk126" +
                "gtFsTIS3y4o6f7k7T4qNHwLyEHerhGFXZUYUzTJT1QP1vC2Dpj" +
                "ZGBscK411/0ReXU8USy6o32htUAl1ZFvgdtiQYy8xIe72RdZ4/" +
                "vkH8iBHq5RhR0VmW1d0gP1syWMrTNGBlFgRtbxRLGBnm/NU0l1" +
                "5JvntljY6t9gLyL0TjhqEV5Y4/l5AznQwzWqEIytE7XbPVA/W8" +
                "LYOmNkYGyIQiT6MXtRG+e75+9RSXXEtcdtsSTWNnsRoTkIF1vb" +
                "gkuO2siCHo6nVQCrpdOKq3qg/sTSNjNGhgTbply1DjOObAnXrr" +
                "9LJdWRb5fbYkGMvMjX3bH92BdZ4+iLyKE8u4nCXVSloyKzrUt6" +
                "oP7EsmhmjAyMDZ43I+t4olh0a0u+9fnuPznv+f/GcVLXsPxVd8" +
                "+b+WKHqLJU+t9ne/ybefoJLh1/EEtp45aRhcTOzpNQxVS6eN4M" +
                "XhjV56P6v4+Lzv6bDq/3imO7mJ/+krv5mRVb5ufad+Wuh9Swvr" +
                "Q19PPmVuH1pa0813tv4lEaz+Ald/Pz0+W61+tquN5nHV7vs7nm" +
                "5/njOZ4p6HB+vuxuPLvX8s7PcrbuCfk9Q+/FxiO7fXImI7/xup" +
                "XIeHp+YY5vj+s4F88pF/uEN0El1WyjdnQ8EEvYEi8jdP+4PYi9" +
                "A2CaSGwDYUSP1qVZ0SLMJosZjT2Q6yCDe6CyGnAOaREwDuuV4+" +
                "z5OcQcP3B+tv52Nz+rjd1aai1RSTXbqB0ee5tkJ5u3yV5BmDvh" +
                "qEV4YY3O5SayoIcVaBWCQQszaxbdQzKSXMN+OmPMjLFhC5HIiN" +
                "mLWtSe9bwZXGqM3Ba85e550xszPy+tf1nG589SdBd9Phqr8vN8" +
                "a7m1TCXVbKO2N84WxMhLfLijhyOgHWPoHtxHq7CjIrOtS3oIUv" +
                "CUk82te5j5oB+zF7Wofbj1pQLv2n85vB/VHruG8XzgcDwfuBpP" +
                "f8qfajTW1PNZaMm1yjiVjSfeoe8rlwuuf2bEtn9PW0Sl1/SaVF" +
                "LNtvDlT/qTXjO4KpbQxkeC1v1pD3GRpknyCCa0CQv2NnVpVrRE" +
                "z413tU37zb6SqxmbsZ0+ZiU5pEXAOIzm46Oth2z8Wsb9vbXi7v" +
                "6eFdtYX3q9vO87em8ccQXl6L9fuuzu/bPa2DMrMytUUs02apsW" +
                "wSJa98ddIggmyultm9HWpVnRYmI0AqOxR5SYsRGrdevIOp4eH5" +
                "2xNXOTX5x6sxXNkVmH87P22PJ5yavou0DP4febWbGDd8pg76X8" +
                "BrSX/HJs+mcrao5/m/Tmbdv6e1no9XfrHU87p1LfPy/OXKSSar" +
                "ZRe/oHbREsonV/3CWCYMSqGW1dmhUtJkYjMBp7REmYUxq3jAWz" +
                "68g6nnBq9MHrn9P3rfn5QbHzdozm5/2M6/1aZStOMp6/jd56Xf" +
                "05yXgGK6M3nuvX3I3nKG7N/8pDHZfxDD52N57V/v/98PHc+KyC" +
                "z4C/lDAuv5cbu7r/a1c/P70/HX6erz12tevJnVf2r/cS/uPbea" +
                "3gvD7j9nr3HlYwRx6648jqV1Ke/wPq9CaC");
            
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
            final int compressedBytes = 1318;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71rFEEUvxSCaGECBrFQ0YCVpBdB7ya7lb1i/gILK7GxMX" +
                "qRRLewkBAVQfATTZMmICoYjYraiElqsZBwpDGdZ0CCuzuZe+/N" +
                "x97uObtzt2aP3Zl57/c+5nezs7N7e5WKutXPV3LegosVZ9vkka" +
                "IjAp/saKV0W/F9yn98emfc8Vl87Kx8jr/O3KdRh3yOlnB8eg75" +
                "9MrHZ1B3eD1qlHB89jkcn33dzmcH8+c2h3xuc8dn7Wz51kvF9w" +
                "n4DG6Wj89r+8s3fzodn7vtoTKf78PKlflhZz4ndhg12wvmc9jh" +
                "/DlUwvE55JDPQ8r4fNLz4/OQYU386P+eP4OnHfJZtYfSsrcnPr" +
                "ZGSX3vlQvhsR8hDrbxsCtBp5xddUszfX1nG/0+A1PMgB+QUfUD" +
                "mfI5/J9e30/YQ22tl0KmjttDbfEZzrvPUqFmtvhMOT5H7KHSbd" +
                "H1iG++cq9b/5rxu1GvR0t2shx/0CbyF73cT33/nu165HJ8+jPu" +
                "xmfxsQt4njznjs/JwbwjVFegrK5UVwSfUR3r5XoamV6nkwpspN" +
                "XrMU5GJHk01UxW+hgqVu8zbD3GZfVxi8/NttBjrM7e7FPWYan/" +
                "ndpEWp2V7Jti9BYg1dXMdZ0kSZ+cbwHn+4syn+9sFUq2ylYFn1" +
                "Ed6+V6Gplep5MKbKTV6zFORiR5NNVMVvoYKlbn03/lv+JHXgoZ" +
                "b4MEY2Q03cEKRwD7uD6telSuwySOaGGknDvVyzm2JNNyj7EHHB" +
                "/vMg9yxrj3yvpzo8X7ZVk3cd/KOXfJ3fmu9sl6hCaUrMmarfO9" +
                "GUmCl0KPsTp7k09Vp5MKbKTV6zGONceft8sBS3U1kxXESI6g9x" +
                "m2GlCyBmvU50Wba1hDxersTT5VHdO+UyCimfQYJyOSPJpqJit9" +
                "DBWr8+mP+WP8yEsh421ZAliMpvZ4hwiAieu3VI/S7DlG40AbkF" +
                "RG9bIt6ustvW/gQninkWk8yg/tsbJems94J535fQb/tsP7zVxj" +
                "s1PsFD/yUsh4W5YAFqOpPd4hAmDi9ee86lHNi3rFEhlDETia0E" +
                "Amk4N638CF8E4j03iUH9pjZXze4OVI/0h/2Pc39r/FkX534zPf" +
                "2P5P/yc/8lLIeDuqs1kuBwx8OELesUZEwPLwm5zFPrBGlDgLNW" +
                "q0cx+6vMACcoBMIjvaY14Hf5AJ9ku5gowl9Jq/xo+8jHVrog0S" +
                "jIEP6PAu22Kvm3x+xj6wRpQ4CzUq9qzmBRY4fyGJYtMeYw+QgR" +
                "yZxoOMKTr5eXI8132y/DRwKezTR3fPk02xg495zQGUT+tPV0M+" +
                "/SmHz+enip6xs/KZbb0U83nXIZ93u4lPO78f+fcc8pk6dg/9fv" +
                "TO4Xr+nbvx6b3N53y3sXU6PtP1qdPNW/aW+ZGXQrbZXhASjIEP" +
                "6PCONSICluMY1ELYSFkoUbFnNS+wACTCL8g9xh4gAzkyjYf7gt" +
                "Ftxuf7fO7fXW7p+vQPs2XC+3Xeh1hi/f26idMW8u7w/TreJw2e" +
                "vF93dbqz9+u8RW+RH3kpZLwdLAkJxsAHdHjHGhEBy3EMaiFsaB" +
                "ZqVOxZzQssAAn4a3fkHmMPkIEcmcbDfcHo5Ot7rYT/Ly6+Tznf" +
                "Hx2z5OdkZ3bXf6RBBcu9xCdzyCerdPv4zLj+DPms/XHHp43Y3T" +
                "Y+a+sO+VwvIZ+/HPL5q4R8bjjkc6OEfP52yOfvfNnL5/9H/kDS" +
                "/ZE/4O7+yBSb3h9FKFv/P8p/fAbfHK4/53rxfGfnunU9b87Myv" +
                "YXgzcqzA==");
            
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
            final int compressedBytes = 1784;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWk2LHFUU7ejGJIiJJJEMSDAD4mQjCMnCzpip6m4hjHEruM" +
                "hPiChusrQ7mXwURAjMPhhoAgkScZGFMFmIIggqLhJmk1VAycrs" +
                "gnFhum7fPue+96q6pqequ6eqqVf17j33nPNuV3VXT9Jo+NtXXz" +
                "Yyt+4fjUJb/PkQv+gx/NkoZet9k5/v/pbvbPzWPVKOU9vPyz9O" +
                "wrHSz3TZLMdld3WyumxnVW3dL0w/f5rI9WZjTrfpO8u734tu8W" +
                "fzen1mO5vnfq7cmdv7/c6OvD7Pze31eW5HXp+35/b6vD27fsbX" +
                "J74+r+f1c3LeEq7P61X2Lj4dn5ZRjhobztfj01cfIQIso239cL" +
                "4uWVUAJj1f9xl9X5aVI+nz5z0bs3m3lta6HuZGL9SBVbYewGnR" +
                "wXf+Gs+u/lLBu/jd7J6XsrS7N0phvxBfkFGOGpP54HzQT40Ay2" +
                "hbzzsUgEnP7/qMvi/LyhEXYxGsphla690wd/cGr4rX4SuwjkUH" +
                "e7zmn235fcqtvPr37K7PSwenfkck/lnuL+mNPI6gwsa2PW5Mv3" +
                "Jixa/9sy33M7cyvr9tj/enX7n93+/x+Yldn2/M6TZ9Z/E1/2zL" +
                "12duZeuT2fWzWu3O0c5RGQdHxHSuI2J8JrPeBs+0yo9Bo3WGVV" +
                "01X5HnIYxFsJq7Cl/b+uDOWGVX0TpWdPtE+4SMchzkZKbnOgKD" +
                "l+R6G1rLOGYD67CfHzMHZ/TILnxVZvZ9oYL9a2SgbVfMDHDgKl" +
                "s9OLbo8b/f41vZs5zPjFvZ+PhWUZYiCmXV+eucRCHuxB0Z5agx" +
                "mafnfUQGM2QVYeuH8/4w2yemzijWByNnrC/LyhEwuyyummZorf" +
                "0M7r5ZVV/XEFJgHfWL8zHX583sWc77dDMbH98sylJEoaw6+3tz" +
                "gCrDZcr8T9XfscnuGf4++qvSZ6PVeFVGOWpM5m4EWEbbet6hAA" +
                "yiltH3ZVk50mhET9yYzbu1cBI9CXOjF+rAKls92x+74uzrM9nb" +
                "qN2W7Kn419Ab6fjKaH548PnZ3Zdqv5pG3gJ67fcAw2s57P6/vx" +
                "8oyfeY97r7Zjh+ZTEDv998KpzZ6r+/d98e38/Ok/r1U9Y0rp9r" +
                "30/aT92ixzrKGWL2zJ5nbxaVPQuzRY/HqYhT6zDsN+wrnz9L36" +
                "3O0dnUMdrMyvrnOY42J8spoogKo6zz7GrN5PNn6bvVPir3fl92" +
                "7/eLH+34+325yP1+cbWSz8+m288g087qZ7NIP9PIFL6PJr0+k3" +
                "076/uomuszeb1+3+/J/hk+L63U8H5fmeH9fqqGz5+npvP8SZl/" +
                "R9rv1+/3ZrVrinpRT0Y5akzmbgRYRtt63qEATPoZdthn9H1ZVo" +
                "64GItgNc3AyaWDYW70QtmtstWz/bErzro+W4utF3drssC51mKx" +
                "90lxIbzwbnfr3Zusrrh2GS7T77bh/+RvLbQWPI2Fgl4WsvEh3u" +
                "ltxbXLctl7qVHjLbpbHsrf2g/bD2WUo8Zkjghj8EKOd7eWWVnX" +
                "MmrcRTCr7zPkCxXsXyPRD+6KmUGxirI+XB3u43Bfai/JKMc0t6" +
                "RzRBiDF3K8u7XMOtInDs74CGb1fYZ8oYL9ayT61l0xMyhWUdaH" +
                "q0P9VPSx9jEZ5Zjmjuk8+lUjjMELOd45owocZw1boTXWha/KzL" +
                "4vVAAJvKzJ57YV7no4z6uHW63Me56Pfq7f7yNZ02x+H0UPatjP" +
                "B9X2M/D9fqDW3+8vl4fyt/h4fFxGOWpM5m4EWEbbet6hAAyilt" +
                "H3ZVk54mIsgtU0AyfRrjA3epH2c5erbPVsf2jVzbgpoxzTXFPn" +
                "bgRYRtt63rUWHBJb+c9ndPrZtDqYA2ljNu/WwomrzVjr2ypbPd" +
                "sfu+Lp3+/JO7O73698WiV7+2T7pIxy1JjMB+fxI4kDg5cg3J0z" +
                "qsDxF+/kI+bgjB7Zha862IUj5AsV8AAngzq7YjkHH5wwr+0VHD" +
                "vo5fayjHJMc8uj+XsaYQxekuttaC3jmA2sI33i4IyPYFbfZ8gX" +
                "KoAEPnnXXTEzwIGrbPV4LYzuPO08lVGO6V8In+o8/kAjjMELOd" +
                "45owocZw1boTXWha/KzL4vVAAJvKzJ57YV7no4z6uH2+F+pHNE" +
                "xs7oaUtmMseoMWQ5hx1VYAMroi6j93dfo6Mz16fvCx6sR41EZ2" +
                "2VZRg9L511la1r1zG0cv89rlW/5/m1vVN/nl+q9fP84fJQBf/+" +
                "OepntKf69SXtKfez0jW1nreeyyhHjck8+VAjjMELOd45owocZw" +
                "1boTXWha/KzL4vVAAJfNJxV8wMcOAqWz1eC6PH/D1kdw3/HrK7" +
                "ys/P1rPWM++aTWMSx4iYnqMylLdswXvjmSoBE6rUUV8ht+wi7G" +
                "/Uz0OuVog1OuSq+O7dWRr5H2Ot1Xs=");
            
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
            final int compressedBytes = 2638;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWkGIHMcVHRAEKVqwAjEhoCgWkoIcHIhBIF0C213bBxn5kp" +
                "CEKJgkCAzZQwghFzunzHgleecoI7Cxl71ohXyLhSProEUhh1gQ" +
                "yCEHIx+l20oXGwxGyJCp/vP7vf+ruj2anfHG20VX1f///f9e1U" +
                "73zPRseBwe93rhcRiPvZ5acV78SD2MQUOMT46Ijaq98cE1OJIi" +
                "uGqqM6cLGUACL2tKa9sMvx6O8+qhVs7qTHVG+jjKIVa0i8PiVQ" +
                "+wMkM+W2yzz3LAg0iqgavA4zEWwWxcl9eU1oZSVsjrtYxWcYM+" +
                "XZ2WPo7j2Gm1ix+LVz3Aygz5bLHNPssBDyKpBq4Cj8dYBLNxXV" +
                "5TWhtKWSGv1zJaxeBKj/59nV34orfrjtVrk6CGL05XvXqhekH6" +
                "OMKntvbw8Qz5bLHNPnAUV5jVs6WMbOcwFsFsfhUpt9XBO2OZPa" +
                "NVrOjwUfjI77H4xI8ePhtL47ZmjqHY0Ihk5TDIRNUcEh5WaHk5" +
                "p9iwq2jntyzdHnD3v1P3e5ur/bt//fOoP0DX/+Furv5THbEjie" +
                "fbo6vpp9u/bvv7vyT+vbz/wtMt+G8lnu8/kZ4f1Pv6cfi4/DT2" +
                "MpafqqXz1YXYCxJYaVJB43oKTizBK5PMmAMZ0S/VYlOvrco6Bz" +
                "c421fEylSjeEQRZ+m8v45cQXFUGVhJrCkH7cDdcFd6GWv2u2rD" +
                "wxg0xPj0uVx1fLf/BdfgiI6sImXlyqkuZLB+9Vx42q+YK0CBZ7" +
                "Z8UGzR1bPVs+Wj2MdxtNOP1Ip2nEefeoCNfjnFB6u+Ozc+wYtP" +
                "Z1oXdWRUNtbAlaEhoryPMzQmFUWHqmPuVEfzintko6hgdeiKGp" +
                "3Hq+PlVuzjOIpsqRXtOB9d740H2Op4zEU+rFp/4xO8+HQmdeHR" +
                "UbwxyoyoDA0RNbhhfZyheKkoOlQdc/O8v665otFGUcHq0BWpzt" +
                "r6fPFobOXncR6tXi+ecS5xsQUTe7HlkDyx4ZUqkqs4yQSHP5RN" +
                "4rYi40UnV1RdUCZzXYtqFJ+Ps0LWj53QPOFQJqwbq6yth4tHYi" +
                "sfxnm0RrgjMu/1XluOvfgiJvZij1Uc0Th7pUocgZNMcCT7+VCj" +
                "yoKKjBedw1+houqCMpnrWlSj+Hx8/P68rtVUP3ZC84RDmbBurL" +
                "L7+9HFb8znO8rw1zv3/ajt89JsjupDtGixL+wP+6sPVxcYhbmg" +
                "2aeWRhgBTP1uuN9nIc/rQhV48to5nmrU0XPrfHT/pD2w+j1Dqr" +
                "hB30GLVh0b22EhLFR3XltmFOaCZp9aGmEEMPV+Lvgs5I313Ul5" +
                "4Klf4y957RxPNerouXU+2k/aA6vfM6SKFb14avGU9HEc36tOwd" +
                "YePp6JNdhkS7NSn+WAx7OljGwLprzhfTbOubyKmGm5rY7mjvQb" +
                "z2xVecUN+uTiSenjOI6dhK09fDxDPltssw8cZZ9ZPVvKyHYOYx" +
                "HM5leRclsd9G7vmD2jVQyuzHf/K61PBa5MdldOcdZTvT3/9502" +
                "rfPlDu+Gd6WXUX1iw8MYNIkNNtM459qs6Hv9Jtvwa45VkbKmlS" +
                "2D2qxfPUv/8CvmClDgmfMrgVrW7vZ4o3X3Nyb8K21MmznDV0oL" +
                "Y/XKRJ97XpmWt+t53fD383leN4tj2ud1F9+c6/O6a+Ga9DKqT2" +
                "x4GIOGmI9zrs0av3rItv7m9XWNVVnWtLKviKhXMlz2K+YKUOCZ" +
                "21Zi0bkjvj7nexQ7+LvUfLmXDi8dzvvEjx4+G2uPtzMUn2lEsn" +
                "IYZGqvrW0FrMDyck7xmVfZxm9Zuj3NDhxbOia9jHXsmNrwMAYN" +
                "MT5jK5a5Gqo2/FSDIymCq7Jn+AevPc1g/eoplv2KuQIUeGa7V7" +
                "wWg963tC/Z+doXz/KyRNVWLLywbby8bKu5J0mXNSJZOQwytdeW" +
                "R1kFlpdzdE2sNs9vWbo9yh1uh9vlWuxlLNfU0rn0ggRWWn0nvs" +
                "1x4OJspH5NvJpXr2iNqyAiOTEqM1SE5XXmdCFDlElV8YgSzrIV" +
                "JFdQHOU4PM3faK1hvRVuSS9jzX5L7Tgf/lb8wKAJIhfXiDKwf8" +
                "R/nWtwREdWkbJy5VQXMoAEPnLbFXMFKPDMlg+KLbq6iRat+rNs" +
                "Y3uPYBgdZ4NNjmkWI1Cj+bx8Ex7PljJ6nR5jEczmFYzuu3+03F" +
                "YHNHhmy+cVN+gP0KJVxxo7zuPr02IYzT61NMIIYOrX53s+C3lj" +
                "fR+kPKzLYiyC2byClNur1/qe2fJ5xYoO98I96WWsX7v31C5fVQ" +
                "9j0BDjM7byVa6Gqs31QTU4kiK4aqozpwsZQAIva0pr2wy/Ho7z" +
                "6qF2fN4P96WXsY7dVxsexqAhxqfP5apj9ve5hok0v7Vw3LNy5V" +
                "QXMlh/43nfr5grKHb4J89s+aAYuq2HvuH+8Mm+Eww2n/RbRHlr" +
                "u99Dpq+wfe6uY/GTxU+kl1F9YnsPsIwebLKHTzCAo/5rnk0rpr" +
                "psVfZ4jEUwm0agxHMz1uq2zJbP7g/QxYnihPQy1t/ITqjtPcAy" +
                "erDJHj41FzXG+3kmrei+kZ6wPLCBtD4b97lQ4rkZa3VbZstn94" +
                "dX3PW8rtznn9ed/8/X/XlduW+S53UXXpzued2XPP98c/c9/1z9" +
                "5Tyff+7Q/3++9f/+/5/nr0+7n5n361VzfVxttzreRa+248urk1" +
                "aZhGFWeek6Z6GS9zMcDAdH18eC+Zx1cMJfbw6246XutlXemPJ3" +
                "pRbu/vqkyM6n1c8Xz0svo/piC0fDUeuJPp0BbfPl1EzBo4b4UI" +
                "WzvS5blT0eYxHMphGs1XMzFqvCGnIMzKNoOcOD8EB6Gev1Phi3" +
                "Q6P2IP6/DTDRgyZofwpubB0Crpkd4iocUQWswlQlz/Adrz3NwI" +
                "q0oiiyK5Z5f50zau8hrmv3Coqhuz63wpb0MtaxLbWLf6uHMWiI" +
                "8ckRZVh5GX7mCFsr5ziSIrhqqjOnCxlAAi9rSmvbDL8ejvPqoV" +
                "bO6mx1VnoZ62clZ9Uu/ms9wDLa5vOpuXE/FQOvtJVzNuLjzNBW" +
                "wyIQQy6UxDXlamMvtLpltnyoadHZOzP+/zP0dt1R/G52qMzvIg" +
                "eWDkgvo/rELv6lHsagIcYnR5Rh5WX4mWPpwMo5jqQIrprqzOlC" +
                "BpDAy5rS2jbDr4fjvHqo1cz08/zgneb70Xu77/vR8G879/2oeG" +
                "n37aesaX772fX/IcPru+/+2fb6nO1RjQ7M5cxjtGd0bp7L5qwu" +
                "LGopql0PFFtlnK99HF+/xFqseljDv2stX7Oq7EqS12nX9f6cv9" +
                "4v/ny66/38N9uu9/N7v+Lr/blJrveLP9vu9V68ob3MctF03vGJ" +
                "443pYoqYhIVRVnl7tka667fx++wOnkvayywXTecdii5NF1PEJC" +
                "yMssrbszXSXb+N32dPohLvR8Uzs7o/43pPInt7X+kxuzU9+f1z" +
                "Xs+Td/Tz0k8muX9G1Nfn+fxMntL+c7r9rP4yyX5G1LTvR2EPxr" +
                "An7NHrPc7j82SNM9Y8W+nw5WM5r2JjNB9nXNhjnyd3VWyb2bl9" +
                "niwc3QzZmv8DwfaGAw==");
            
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
            final int compressedBytes = 1902;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXMtv3GQQXyCghRZBH4CKSCKUsI3SGxJSJXpZe31CTVSFd1" +
                "E5IHHIEcGJS+O4ZRvvhVZCQjyUCCIutAeEVIlInMujcCFV+Afg" +
                "EAkOAQokKezn2dmZ72V7vfY6XVv+HjO/+c1jv6y9tttKRd8a7+" +
                "qjuG3x60oPWzrO4njCbyoD3hoXO/1HOMq1nhfzjbLnen478Hpe" +
                "0kc51vNS3lH2WM8fBlnLxe1KZeFNGLsH3YOVytJ+rvdnEuz/k2" +
                "Y3O/2fXcmO/7Rgtlj/o8uCO9tHVZf7z1hjuCXas2Od2V+dfjdi" +
                "G1F9L/7d4VtmPu9OV63gjvZxV3CPLHW2qHe2nC2spxhzvTpOI9" +
                "N1/gLymm2E1qznOGcreDApBi41jWxW5CPeg4mz0Wq0nG3RQu9s" +
                "4wzHS/tFC0jCwg4MqMcDcDADPHqCEfdBFkIObGJHqczK41y8yq" +
                "1VRsoMYwQJRMStcOwvky2guBY98EgEZ6ee22TZnm1S72w6m931" +
                "uQka1KvjNDKzziRFrNCa9RynIuIYbSObldmHjjVxNt9qz3Y70l" +
                "1xOLv+GzgXkmAe9YjReGNkZp1JilihNes5ztkNv0+KgUtNI3nM" +
                "vz/RR7wHM6f/SNR2v/H9I2J9+tG3U3gjkjxO6ObbhjPDA/bc/Q" +
                "lNcjifM6e/L0E/apY337fgD0hnmvfakvGe4qnZNN3z+yH3kFif" +
                "+V9FCN5+t/DHfH3L67OPzzlmfdZPqOvTyHBbrU/IqYz12flu+D" +
                "zftekv5MOZlcNml3ee9Q1sYWTS6uMkvt51iEjjhaPkyO3WqInn" +
                "t/lXrVWUt+QtOSuihd5ZwRmOoQUkYWEHBtTjATiYAR57sOA+iF" +
                "HIgU3syCOz6nGa4iILiAxYQQKRcCuZAWwBxbVcT5Lu2l4hy6S/" +
                "9yK28NdKads7DxXJ7oVeCC30KIO5GDurIAeZs4paQqgH4GAGeG" +
                "KNPs1VzsI1GIEcBWG4BJllFtmCMqJchZ2cMc8MsWLGkZyRZ0/R" +
                "8tgHvj5/H9b1mXA9v7V3r5fOf5rteunCRJrrpUiS0/XS+cuVId" +
                "6aH6RCfZjxruzxxnFooUcZzMM/UMIxtJOOH1yDHric+5At0EaO" +
                "QvfKmfW4yIKQhA8PqBlzBopA9Sz747lI6PXGOrTQR7p1nHt1lH" +
                "AM7aTjB9egBy7nPmQLtJGj0L1yZj0usiAk4SEnnVu2UPPhep49" +
                "RQuHN+lNQiv6zrlqEufeCZCihLAwIns+43Muk32QhDR6DJyFJC" +
                "pGRnBvnJfnpHNTpDxCnq/sUY6YfMXcD6m5NfV5h1tLedehZscD" +
                "b99PZq5mvB9SS3s/JEuUjZONk9BCjzKxu6PuKEpQKiS0k44fgI" +
                "MZ4Ik1inOUs3ANRsCj4Kx6nDx23YKQhIecdG7ZQvQcyfU8e4q2" +
                "c8w2ZqGFPtLNwu5OuVONWXG/jjBCQjug1QNwMAM8sUb1nOIsXI" +
                "MR8Cg4K5eE/6qx6xaUETJCRHLGMPaXuYXoOZIz8uwp2s4x05iB" +
                "FvpIN4Nz5xRKOIZ20vGDa9ADlwMvcXBNo/v8lOtVr5xZj4ssCE" +
                "l4yEnnli3UfLieZ0/RoqWnPRfxNoVMHPXroMU5YlHa/IzmnEfm" +
                "9CxPaYQf4jfrVbQsM6FwdHbMzFq/rmJNrCl+qWs5iiN4NZhTkc" +
                "FLXd8/5X89HbySE8+zGe9LFpATi6pdz+CF4JSlntq7FOFOSt7n" +
                "5Pm51231PPdaMfUMXrTUM9X7IcHzRdSzj/tW98etT7u2+Hqm85" +
                "01Qq/qVaGFHmUwr1+TJYTFGdhwe44jDyQnvzIjyk161asalYzg" +
                "GeiRiJxM3FQLikTlknc5XhzH3V9qflXM/aXwVon3l14e5P0l/j" +
                "5Y+7PUniGa3gfj79elex/M+ptnAO+DqTnh+2CSzz7eB4t9Xlzd" +
                "u/c/Mz8vrg76/qfyvPjn/K8q8uDMymGzyyvP2PV5bAjX57E06z" +
                "PP9xma33V9T6u61r0Zzx73WTXVjPVczHg9P13e86P6+BCuz/FB" +
                "r0/njD7K8bvzTLk8ReSUUGnpasHZKKCmG+Vx2OyKyNNUz2Hb6v" +
                "X8UCmvUrvvzLb2DV89be/T5re5I9S7I+4Ivu8txlyvjtPIzDqT" +
                "FLFCa9ZznIqIY7SNbFZmHzrWzGm4R0Jv198o5hNsPVz8OsTfm+" +
                "pW5vs2HUnu10utI+VdL9nqWdzvTfcLPnKuFHB+v1I8B2WRzi6v" +
                "PMtYn8618tanzXde67OUeq71vTrXMtdzLU09m78UUc/WY8P3e7" +
                "Pof39UyvlorLx6XviyyL9397R72tkRLfTODs5w3P4LiVrC0A4M" +
                "XE84MQJbksKY++CWYCO0MCI9t8c4QWaKiywgMmBFvJqNHDnYAo" +
                "pr0QOPxOk+60VkaevzieG9XiqlntN7vZ7FfH9683nVs/XkXjkf" +
                "efNJ9Qwu978++dbrv++I+/9D/ImiftcFHyfoP7H81n001fXSb7" +
                "3WM/l63j3qHlXXJ0iS1yfidLx/GHnLWZ823/r3Z7Yoy/j+dOdK" +
                "rOdcqvV5c2+ejwxocT566nY4v2fa/geBtXJC");
            
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
            final int compressedBytes = 1878;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWk1sG1UQXqmXUqhKpSCEEIopakh8qbjBpcS7NiBCegWkSh" +
                "z4kVD5kaoqN366btKa+sQBiQuqQELqESG14kCO/JRDyzkt6rGX" +
                "RFyDlAQ/z47nm/ferteO1+sY3mrfz8w3M9+M1vt2NwmC+PGg0+" +
                "LDQdLiJy5e6PSPmnntdlfydJDZ4mPBAC2eCUbS4of76J/yyykn" +
                "D/74iHhl1LP9/H7rGT8zafW8+kPeesazA/GZS9OYeg7SmuvBAW" +
                "rtJ4v1n3l9vvBfvT4vfTXs9Zl5/zw7ffWknIr7vWfWc2EK67lQ" +
                "7PVZxv2zXSnx/jlbrP/M++fpYq7PtdfLuz6vfF3k9RnFUUw9jS" +
                "yjtUgQIwfpmuuuHm3RK8WObqAPpYmZH+rtqOjZ5SUWyL8nuWFn" +
                "jB6EgR1ZxxPGFvpB9KC7Tkaeizy8bnqWhtdFywjfaaxMI7x4JZ" +
                "mRikQ0iqGKwytBsmftRfQ2R5bYsXVmHN2sGIlVcuNwtOS8H93v" +
                "rpOR5yIP75mepahlhD5Zht7Ea8L2HntBz1Y9VRzBax8uL+GgOb" +
                "LE2KEVz9mfxMcTc9VxNLp+qn6KehqNjlY8D++SXDByEMI+UcMR" +
                "UN7J6S76QA2PyMKNak7y4eMlFsJBmBg7nTHNxZ8wQb+6VsJYoz" +
                "279bH4PM9bDwVT15pHxh3x4k+FPy81Snxeqpf3vJRIRv681H6l" +
                "vOely4+V935UWD1f+7+eg9Wzdm5i3zfPjf173W7h98+XS7x/vp" +
                "QH1Xpu+AjhpozhZrjJ7+9mHgRfPMJ6xPrs03y6Op+UsUbr1yMu" +
                "3Gze7McBpb6ZnsfX3BjZEfw+PVfu+cKvz6USr89Xxx1R6hleGf" +
                "qaz7Rsny2vnmn7UXGt9WLyDnU0Omp+76OPYPyW1dJi69/7pD1/" +
                "hu9M6v7OzMa3vzd7X7Dbb07f++bV4+OJU/uLe5r5tO68n7/BdY" +
                "zIEwVRmnm6NWuy/afFt61dVObfj+7Yv/e128O9H62+l/Z7X317" +
                "zM/zd/L83i8vF/L3uPlR1XPtSFo91w6PuZ7zeeq59uPo7p/xP8" +
                "lOOBvN2vt7lDMG43x48rvfpp/nB9jfZ/Pu76NgWfus87z0xuTv" +
                "KobnuC2HivZJp54HYFc3PMdtOWCcL7mnmU/rzvv5G1zHiDxREK" +
                "WZp1uzJtt/Wnzb2kVl7Uf1X4Z9nrf3o8l5nqec+u1Hw/79vfVW" +
                "540h+T5nxnA33OX3dzPvfl+C73eh51telsyvC71fBElqtGHmF0" +
                "PiaX1fyvCYNtNz6/vSru3TjeD32VltyRhuhVu973VbpGG9Pc8j" +
                "8+t8UsYarV+POBuR5TFtlmblj+Fi/T7dxvWMZqKZgr6HzASltb" +
                "TYo/0e4qtn7ifB9UHun93vAh/un/elb/rov/PL2x/kun9+O+j9" +
                "M7213h9dPVNy+ri867P90dif7X6eDB9lZjcs//p8fZ56Gllmjm" +
                "ghWqjPm/unYIxEDkLbJ+FoRXjx2r2DLaAX1DADZIFeUdK8aXN3" +
                "LSQjydXE1hnTPL6GFmZEJHrE7IUtcrfuJH8nd+5KVHHu5pWcd/" +
                "1KOt7nd4z7UWX0yOwm+1Htz2DqWtE5ZX6ve3b63o8op377e1dy" +
                "YP7fpsx6tlaKrWfn6X5PxnAv3Ou9H+2RhvX2PI/Mr/NJGWu0fj" +
                "3ibESWx7RZmpU/hov1+ayt1Faop5FltK7/piWCRbS2x1MiCEak" +
                "2qP1q1zRcWTt86ERGI01wsTk5PMttWDvOrKOJz41us/3pVtT+H" +
                "3pVp7f++r3o/t7R7TrzgZt4buTur8XzayM/ah+qMTr81CR+1G0" +
                "HW0712dXZs7GadLymrE0F0ufXnvz/g62OZJgfJbc8+Fjiyz8/L" +
                "hxTsg2naPPg0/Sq8BOtEM9jV3dTnJUO0ci6UmrPe0O6vAkXLKq" +
                "Cq43q6IX1DADZKG8OjyRu2shSMBX7YzRgzCgXDUPOw7UM7FsLD" +
                "eWqTdjZ4/6nFe05l5kOBN7XOEaZTwThB0d9W4c5IU8NS8dzc7C" +
                "WOrYmgdcx1Zkzcpm3EOfaZyh3oydaJ/yitbciwxnYo8rXKOMZ4" +
                "Kwo6PejYO8kKfmpaPZWRhLHVvzgHpakTUrm3EPvdRYor7R+x9X" +
                "Wpl1/VeSskSwNBN7XOEaZTqGSETjckAvIrExGoHR0C/m5PoWps" +
                "gQ89URNWNGRxvRBvU0du8FG7yu/84SxMghOjxRwxFQjjG0Bdto" +
                "Fm5U9OzyEgtBCp5ycn1rCzsf1GP2wpbOxROLJ6g3IzVamXX9D5" +
                "IiRrSok5Nl6E28itT2aO+fOg6vbJ4uL+GgObKEcnJ9aws7L7sO" +
                "NmNGNqqNKvVmTK7dKq8bIUlZIliaiT2ucI0yHUMkonE5oBeR2B" +
                "iNwGjoF3NyfQtTZIj56oiascTqVvekOWRFp6r/SUJJjxrdoxzX" +
                "doysxljG+fDikXkJM1uDMp9/Wy9VwGwpBmbiz2NxzhyyolMjCC" +
                "U9anSP8qwYmfWc0x59ePHIvISZrUGZz7+tlypgthQDM3F4/Qtq" +
                "PvGI");
            
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
            final int compressedBytes = 1639;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWsGKHFUU7aVgwB5QxIWIOLhQQckuu0x31dIvyKyH+QAh6+" +
                "lpW1qH4EZdKCSCMIvgyl0IIWQpAy7VtbMM+Adium5unXPufdNJ" +
                "tz3SCa+K916/e88957w3XV3VnbS/tv05GCzaovd5jBiG0RzzmW" +
                "cYAYwdsYrVsiLPSxhFsFp0kLWje+ePyqoXHTv6+rXr16xfjHbY" +
                "zObeI8avUM8znnNMNRCJalmR5yWMIlgtriJrq4/BQB3G3Yg+vA" +
                "Ja+Zj85a/mx4N6rHSM747vWm+jx2yOCGNwIhfzXKtV0M0VXqMu" +
                "smpmjozIRifRrTLAQVS+aCWKLu7x6YW7f/qcf6XTdSs3+E453Z" +
                "b37Hzev/qyXsEb2M+v+lcndTdWvCoejB9Yb6PHbI4IY3AiF/Nc" +
                "y6ysmyu8Rl1kVWbOvlDB/tmJrpgZ4CAqqx6vhdHtPZyLWfcs1c" +
                "9jxDCM5pjPPMMIYJ4+r4UqVsuK0WfEKILVooOsHd07f1RWvejY" +
                "0ePz8bn1NnZ7fe5zRBiDEzlusZZZ+78ncXAmI5g1+yz5QgX7Zy" +
                "e6YmaAg6iserwWRk8+Ljx/frjOJ8fkk/7VB//nJ9bko62/H31d" +
                "7yubPD7/u+7BRt+f39Q92Oh+flf3YN1j7+reVett9JjNYwRYRm" +
                "s9NygAg6gyZl/KypGIUQSreSY7idzYC2dXZdXT/eEVT97s+lf6" +
                "++VbR5896Yd0B30Xr2e/Fe6wry25+76XIq9v6L7+6jPyb6/It8" +
                "OzLz59Enlnpfr3n44r7WeR6SXcz9kv6+3n+PH4sfU2eszmiDAG" +
                "J3LcYi2z9s+/xMGZjGDW7LPkCxXsn53oipkBDqKy6vFaGF06Zm" +
                "/U+8q6R3ujvWG9jR6zeYwAy2it5wYFYBBVxuxLWTkSMYpgNc9k" +
                "J5Ebe+Hsqqx6uj+E3m/3rbexy+37PEaAZbTWc/NacPR7kRjDfu" +
                "6rDuYlDkWwmmeyk8iNvXB2VVY93R+gm51mx3obFzmb+WvvgcGJ" +
                "HLdYy6y+F8zBmYxg1uyz5AsV7J+d6IqZAQ6isurxWgR91pxZb2" +
                "OXO/M5IozBiRy3WMusvT5xcCYjmDX7LPlCBftnJ7piZoCDqKx6" +
                "vBZBHzaH1tvY5Q59jghjcCLHLdYya69PHJzJCGbNPku+UMH+2Y" +
                "mumBngICqrHq9F0MfNsfU2drljnyPCGJzIcYu1zNrrEwdnMoJZ" +
                "s8+SL1Swf3aiK2YGOIjKqsdrYXTpOPqnPves/bx0u71t/WJEzO" +
                "foPYYs59BQxQpZIzKWnEHHZ9Fn9gUP6pGdcJUysD437bNjR46u" +
                "jK5Yb+MiZzN/zRFgGa313LwWHK6fGXU3Nc/zEociWM0z2Unkxl" +
                "44uyqrnu4Pr7h+fy99f59+u+7vIYXfP+/Vz8H/8HdO78/5/fr+" +
                "7CIvzO9184cv7/VePz8v+/NzsZ/12NznZ31/rv/52dxqbllvo8" +
                "dsjghjcCLHLdYya//9jDg4kxHMmn2WfKGC/bMTXTEzwEFUVj1e" +
                "C6OLz0v3L/V6ONoMz3RYr/d6P9re9+d2HqNHo0fW2+gxmyPCGJ" +
                "zIcYu1zMq6yujxiGDW7LPkCxXsn53oipkBDqKy6vFaGF2fly7r" +
                "2Pvde3tVyubXz+JbPeeI51FhlDq/uNozy/kv0o/VEdWetCfW2+" +
                "gxmyPCGJzIcYu1zMq6yujxiGDW7LPkCxXsn53oipkBDqKy6vFa" +
                "GF2v980esx9W/P9g39fvR8uel+Z/rraf8z/qfq78/Lnk3+NmP9" +
                "ZretnR3GnuWG+jx2yOCGNwIsct1jIr6yqjxyOCWbPPki9UsH92" +
                "oitmBjiIyqrHa2H0i/t9c/bTdl7vdT8v+/Nz9nP9HNzoM1Tdz7" +
                "WP9mZ703obPWbzGAGW0VrPDQrAIKqM2ZeyciRiFMFqnslOIjf2" +
                "wtlVWfV0f4Ae7Y52rbex+61k1+cxAiyjtZ6b14Kj/z0mMYbfvX" +
                "ZVB/MShyJYzTPZSeTGXji7Kque7g/QzUFzYL2N3b3/wOeIMAYn" +
                "ctxiLbP2zxfEwZmMYNbss+QLFeyfneiKmQEOorLq8VoY3U7bqf" +
                "U2du/dqc9jBFhGaz03rwVHf60mxnC9T1UH8xKHIljNM9lJ5MZe" +
                "OLsqq57uD9Cj4WhovY3de3fo8xgBltFaz81rwdFfq4kxXO9D1c" +
                "G8xKEIVvNMdhK5sRfOrsqqp/tDK/4XUFsPTQ==");
            
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
            final int compressedBytes = 1442;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW12LHEUUbRXEKJKIoiwiiwR9WJKJj4IL0e6ZqAQRHT8xsq" +
                "CC8xaQ/IEehk5myLMvPuwv8EH8O/smKkKEJCo+COLs3Llzz7lV" +
                "s2P39uwu2aqmqrruPffcU3eq52Nhi71iL8uKvWI+Z5mu9F5Hw9" +
                "hlPuw+FlmzeUMO9IQIZA11xnRZBOpHJbxjZDAFPjPnw70gOst6" +
                "dzLXenf2bWK30WzsQz+ul604j2cKdTCabTHUqrzdnvfHWFe3cI" +
                "9iKR/xyPJG+eR0fHp2/8xsfHban8+y8f054qX9cXx3EbE17ReM" +
                "YXyP+F6NKyqvlG+Vb87urkb918rPYfVl+XU5KB8qH3aos+U5WG" +
                "3A/ebBFRn/Mcdtl6+Xed16lp/R6otp/yrL8s18U0aZ932y0nu0" +
                "GBbRHI9dY41D84eMrJb9uI5xMAKzqSdU4rmtFsrOmTkf1wd3PH" +
                "xuNj6mKocb5Y3pCK/58EV4Tf8OX6nh2eWv4vB8YJme+Df+zA7d" +
                "hk+s8L8Qty/LPXwqsGzW0vPyfK5VzyhT7Xq20ZrWcyme6jn+52" +
                "jqOf73dNQznc9261n92LSeYcvPh3d1W/PIdbf1KhvtjPqjj0fv" +
                "ke3Tw/P2zi3NeO0gbw3lH6zwf1JXWX1UJO5M74yMMqtN1t5iWE" +
                "ZgPOIsg9ktLzOqPeb3Wb0qRuAOlitZphiVeC6+WK/0vJN3ZJR5" +
                "9kR0dO0thkU0x2PXWONYPHUBo3sqO5zH1jEORmA29YRKPLfVQt" +
                "k5M+fj+hi6u9vdlVHm2S+yXV2bBTF2mQ+7j0XWxa8+4EBPiEDW" +
                "UGdMl0WgflTCO0YGU+Azcz7cC6Lrfr7f+vB0fL7f6jf7fK/+qn" +
                "6ufqruYz2rX+t9XzqoVcGvkeq3dupZ/bLCf7cx8+/z+V5L3yh2" +
                "wrsWv6/sHC/POvYEr8UDez4njx7H+Uz1XP/zXm2v84mYPH40v4" +
                "Oq107KL7LhN1lqDVtxu7gto8xqk7VZEGOX+bwfYznK8oYRGsMq" +
                "wqwhs2c0r1fi1TKDKfCZl+2E0bG2//0ztRbP7Hf1Patwavm/DE" +
                "0V1tfa8rfBrXxLRpnVJmtvMSyiOR67ZTCMWZkx1MWsaPEYRmA2" +
                "9YRKPLfVQtk5M+fj+uCO09+TY783Z5YGvzfzft6XUWa1ydpbDI" +
                "tojsduGQxjVmZ057PPeWwd42AEZlNPqMRzWy2UnTNzPq6PoYtB" +
                "MZBR5tk7z0DXZkGMXebzfozlqPm7G6zZvnj/G6Aqzhoye0bzei" +
                "VeLTOYAp952U4YnZ73dp/3VM94PUffpnq2Vc/R983PZ1EVlYwy" +
                "q03WZkGMXebzfoxF1sX7DXCgJ0Qge6gzpssiUD8q4R0jgynwmT" +
                "kf7gXR6Xym989UT1/P4qPjq+ey3Ol8nszzmf6+1PJfj9P5TM97" +
                "et7T857OZzqfJ7KlejZv3YvdizLKrDZZmwUxdpkPu49FVszLjG" +
                "r3CGQNdcZ0WQTqRyW8Y2QwBT4z58O9ELrT7cgo88zX0bVZEGOX" +
                "+bD7WGRd5AcO9IQIZA11xnRZBOpHJbxjZDAFPjPnw70gOn0erf" +
                "/752Q71bPNz/fih/oe9h6MO5q2WsM6VOYb+YaMMqtN1t5iWERz" +
                "PHbLYBizMmOoi1nR4jGMwGzqCZV4bquFsnNmzsf14R2n70stns" +
                "/L+WUZZVabrL3FsIjmeOyWwTBmZcZQF7OixWMYgdnUEyrx3FYL" +
                "ZefMnI/rgzs+jv8vfnA/j3rXe9dllFltsvYWwyKa47FbBsOYlR" +
                "lZHftxHeNgBGZTT6jEc1stlJ0zcz6uD+84vX+21yZXQtvN/uE4" +
                "b75/euvZu9S7JKPMapO1txgW0RyP3TIYxqzMGOpiVrR4DCMwm3" +
                "pCJZ7baqHsnJnzcX14x8GZfTs9t40/JSM1nbyzuHu3BtMri6ir" +
                "R7qDCye9xun98xDtP7pCBP0=");
            
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
            final int compressedBytes = 1765;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWctuHEUUHbFCYgFISAjJBCEk+IH5ANzulixvskC2V8kCL7" +
                "1DfIAzAWO8ReKxQVjiA5DY+huIFHuRRaJEyg94kz1M39w+59xb" +
                "PeMZxlbidLWqquvec885VTPjtsfNuBmPRs24eTmPRr7ye44Ay2" +
                "it5+614BiNEFXGkTTN87rEoQhW80x2Erk9D9+qrHp6PrzjyYft" +
                "+La7nHx079v/xvewt8mno5lt8u6M3Gcp8sFoJe3wzzmuPl6Mb/" +
                "J+inyyUP3n07F+Wj+10WaP2RoRxuBCjnusZVZXZw7OZASzZp8l" +
                "X6hg/+xEd8wMcBCVVY/3wujmtDm10eb2vXvqa0QYE9HaUeX3zI" +
                "poZIyvt+r4SjlKvuBBPbIT3TEzsD73WBkdd8iL5sJGm9vcha8R" +
                "YQwu5LjHWmbt9ImDMxnBrNlnyRcq2D870R0zAxxEZdXjvQj6oD" +
                "mw0eY2d+DrGAGW0VrP3WvB0eknxvDuPFAdrEscimA1z2QnkRtn" +
                "4eyqrHp6PkBX29W2jTZPc7bye44Ay2it5+614PCzyIx6nprndY" +
                "lDEazmmewkcuMsnF2VVU/PB+j6vD630eb2Z+u5rxFhDC7kuMda" +
                "Zu1+fhMHZzKCWbPPki9UsH92ojtmBjiIyqrHe2H06/v70uSdq/" +
                "x96bufl/x96WH90EabPWZrRBiDCznusZZZu9eTODiTEcyafZZ8" +
                "oYL9sxPdMTPAQVRWPd4LozdebLyIJ20xi2NEzO9RWcorW6lNM8" +
                "40q9JHv0pu2UXZH1fEud9jmaHMOe31fr1vo83tWe/7GhHG4EKO" +
                "e6xl1u71JA7OZASzZp8lX6hg/+xEd8wMcBCVVY/3wuhqs9q00e" +
                "b2WbXp6xgBltFaz91rwdE9mxNjeL5vqg7WJQ5FsJpnspPIjbNw" +
                "dlVWPT0f3XH6yfpN5/jHZZ8Wy1e+3q2qq9pGmz1m6xgBltFazx" +
                "0KwCCqjNmXsnIkYhTBap7JTiI3zsLZVVn19HwIvVvt2mhzm9v1" +
                "dYwAy2it5+614OjOIjGG89xVHaxLHIpgNc9kJ5EbZ+Hsqqx6ej" +
                "6E3qq2bLS5zW35OkaAZbTWc/dacHRnkRjDeW6pDtYlDkWwmmey" +
                "k8iNs3B2VVY9PR/dsbX15z7aHWJ6p/f9TVH9qzLb+vN5KuZUHZ" +
                "b9ln3N5u/Tj9UzdB77aHelbL6f4ejxcjlHXEaFUeq8v9ozs/n7" +
                "9GN1RB1/Pfy9ucrv5+nkn/hod6Vsvp/xCj9ZLueIy6gwSp33V3" +
                "tmNn+ffqzOqMXen4cP3oz35+Hfy70/1yfrExtt9pitYwRYRms9" +
                "dygAg6gyhvfBRHWwLnEogtU8k51EbpyFs6uy6un56I5ju//WaG" +
                "grbD/8M5zBcJ6vbps+j66uTe7d6O9DnlXPbLTZY7ZGhDG4kOMe" +
                "a5mVdZXR4xHBrNlnyRcq2D870R0zAxxEZdXjvQj6rDqz0eY2d+" +
                "ZrRBiDCznusZZZO33i4ExGMGv2WfKFCvbPTnTHzAAHUVn1eC+M" +
                "vv7P+81u9aP6kY02e8zWiDAGF3LcYy2zsq4yejwimDX7LPlCBf" +
                "tnJ7pjZoCDqKx6vBdGD/8vXu3f79W4Gttos8dsHSPAMlrruUMB" +
                "GESVMTwnx6qDdYlDEazmmewkcuMsnF2VVU/PB+h6p96x0eb2vb" +
                "vja0QYgws57rGWWbvPB3FwJiOYNfss+UIF+2cnumNmgIOorHq8" +
                "F0YPn/fVft7ru/VdG232mK0RYQwu5LjHWmbtXk/i4ExGMGv2Wf" +
                "KFCvbPTnTHzAAHUVn1eC+C3qv3bLS5ze35GhHG4EKOe6xl1k6f" +
                "ODiTEcyafZZ8oYL9sxPdMTPAQVRWPd4Lo5vbzW0bp7M1W9naR8" +
                "T4DvW84jXHVAORqJYVeV3CKILV4i6ytvrACUXlqKiOoVX4SdD9" +
                "j+n4++E39GXal19ML6ysK8JQGDmjI8dnacxzxLgSHozuC85ihm" +
                "Ml/pjHKfBuTYN3Mm8f07bxx+KZeTiPXJZhWYeLe736Rp/3o+Gz" +
                "+6p//zmc59AW+H7pVn2rHLM4RsQ015/vV0DGqsoYj/roV98O2I" +
                "Hqak102adfqumLdCewVq8lZBuzOEbENNefZ7aixzVXKmM86qNf" +
                "ZZQ6UF2tiS779Es1fRHV1nb80/C5XWU7/vVG7+6XS6F+W53i/e" +
                "PhPbXSV/D34Qz+x2/vw/efK/z+czjP1Z5nc6e5Y6PNHrN1jADL" +
                "aK3nDgVgEFVGdad5Xpc4FMFqnslOIjfOwtlVWfX0fAh90pzYOJ" +
                "1f5k6wxugxZDmHjiqwgRXRyBhfb9XxVfSZfcGDemQnXKUMrM9d" +
                "x+yYdz/8vXnFz/e/hjO4ruf70fab8Tw6+up6nu/Dec45z38B9B" +
                "d1CQ==");
            
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
            final int compressedBytes = 1448;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm79uXUUQxm8VIVEAUqSIAiGERJUqknvb95wK6NwgWbL8Cs" +
                "gv4OskgOkpXdFS0SCFVIguEkmKvAASD+AU6RD2Gc/5fjO79xrf" +
                "+E9s76529+7MN998sz7n/jmSJ5PZvclRm703OWmzj3d3juYPJ2" +
                "ObfTZZ2GYfLPB9XljuTs6lzd4/xf/JGfk+4u7hT0eWT88U/8XJ" +
                "egXnuf/bzT3Psh2fZ2tL/53b/d6uzxtzfe7/8W5cn+tPlr0+15" +
                "/8n+vz+38u5/No/892v79b9/ts9ybf7f2z/pnNtrrN9tkibEQw" +
                "njhlkF15I6Pba/6cNauKCFYwX8k8xVSSuWKPev11eb//8Pt1+H" +
                "x//PNF3u+D5ZzeP9t5vs15VpT+0r71LP3+udPv2Gyr22yfLcIS" +
                "HeM5lEEYWSNjqSuy0pIxEcFs7imVZG6dhbPHzDFfPB+hu1fdK5" +
                "ttPfbZzl/7LIy6fBw5lqx+FuSgp0SQtdRZ06UI6qeSWDEZpCBn" +
                "jvlYC9HT59Pn+Zo1m9k1yxZ9pd85465sxx6LqmNKHkWUqKgw5o" +
                "3ssYr5+Wsx8yyeuzvoDmy2dTjrA9/LQoy6fBw5lqzj3xMc9JQI" +
                "spY6a7oUQf1UEismgxTkzDEfayG6/X5vz5cu4/fm231fWn1w3L" +
                "WzwWY2zvSc9rqeY1FzrONqeDG6LinLHtpq/NmvU2C1loOVnFbH" +
                "cNLftrt22TZ9M31Tt5lds2z+WpE1/6IM8jjTokifvc+rIKrO+h" +
                "iR1/ka6wx1zrl1/lq+uoZXyhVpb59HF/08pJ3n8uc53Zpu2Wyr" +
                "22wvCzHq8mU/Y2PUyd2IfbSP9+sWVcWsJXNmlDcryWojgxTkzP" +
                "MqSei96Z7Ntg6+Pd/LQoy6fNnPWLKO+cFBT4kge6mzpksR1E8l" +
                "sWIySEHOHPOxFqKv5n6ffnN19/u83Bf3/LP93myf7+3z/WaeZ/" +
                "+if2GzrW6zvSzEqMvHkWPJOj5bBwc9JYKspc6aLkVQP5XEiskg" +
                "BTlzzMdaiG7vn+39s71/3p7z7O5392221W22l4UYdfk4cixZPT" +
                "s56CkRZC111nQpgvqpJFZMBinImWM+1hLQG92GzbYOvg3fy0KM" +
                "unwcOZasY35w0FMiyFrqrOlSBPVTSayYDFKQM8d8rCWgN7tNm2" +
                "0dfJu+l4UYdfk4cixZx/zgoKdEkLXUWdOlCOqnklgxGaQgZ475" +
                "WEtAb3fbNts6+LZ9Lwsx6vJx5FiyjvnBQU+JIGups6ZLEdRPJb" +
                "FiMkhBzhzzsRai+5V+xWZbh+9SK77PFmGJjvEcHiuO8ftawRjf" +
                "3aOf+xpHRDCbe0olmVtn4ewxc8wXzydWnNuPdyetLdn6p/1Tm2" +
                "11m+1lISaj41AUMyheiMhYU6Y8voscNV3SEDVSSayYDMzPkSOz" +
                "YlZf/D76t11ny7b1O+t3bLbVbbbPFmGJjvEcyiCMrJGx1BVZac" +
                "mYiGA295RKMrfOwtlj5pgvnk+suF2f7fd7+/+Odn3Wrs/Hf92O" +
                "6/O7ry/n+dKjr27HeT76sj2vu47PP9v9vvg813bXdm221W22zx" +
                "ZhiY7xHMogjKyRMaqLfu5rHBHBbO4plWRunYWzx8wxXzwfVtzu" +
                "9/O83/vX/WubbXWb7WUhRl0+jhxL1vH3LjjoKRFkLXXWdCmC+q" +
                "kkVkwGKciZYz7WQnS32q3abOvwLG/V97IQoy4fR44l6/i8EBz0" +
                "lAiyljpruhRB/VQSKyaDFOTMMR9rCei1bs1mWwffmu9lIUZdPo" +
                "4cS9YxPzjoKRFkLXXWdCmC+qkkVkwGKciZYz7WQnR/2B/abOtw" +
                "7R76XhZi1OXjyLFkHe8PcNBTIsha6qzpUgT1U0msmAxSkDPHfK" +
                "yF6Oo769/jk+UHk9bO1KYvpy/rNrNrli36Sr9zxl09t0XVMSWP" +
                "ImoVRA85I3usYn7+Wsw8y5j7P0uEPtE=");
            
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
            final int compressedBytes = 1124;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1rFFEUTRmwUEEQixhEsDCCqVIINs5MoSKCtmlDmthJGh" +
                "tdQlayhWAhYqd9fouQX5DeNj9As9c759z73ttkJptlk9wZ5s2+" +
                "c88997yXt/OxkIWFdKt+LhS2cuQkniKnVeiq39/r+W/Vr+6Rk3" +
                "iKnFahr8PuXs9/+/R+IbaYz7nY6uV6OY8JjhaYjZXj5QqISFae" +
                "o6i2updGwA5sXZvjXZbq53JKSDsDS/VSwhxjgqMFZmPlOKtlPS" +
                "5ppTxHUW11z7OsA1vX5niXpfq5nBKitQeP09zBSp+VPlhtPz2c" +
                "5Tds8GjerwGjJ3EdjPmc4/l8GnMw1fl8FnNwhuv57XG72PbvHD" +
                "9/Dm4Q4x4+7/7OKFyfoH4/QW5Nyfe1E+JLHfVucm/46h+y3Cn/" +
                "QXF9vohV1vt5frPelFbOikkfCHOwI8aHz2VVrmsVFfcMVk195n" +
                "whg/2zEztiVoADX9nW47EwO943Z3A/eh1zMKv7UVbhEt6Pxkiv" +
                "+1HM53TnM66f8X2P9RnrM78+P3+4Gutz51usz3nddvdjDvpuzX" +
                "azLa2cFZO+R8Blts3nAxXAAWoVU19WlRHPsQyuppHUidfGXKi6" +
                "rWzr2fkBu96oN6SV8/hddEP7QJiDHTE+fC6rtu+7pMGRlMGqqc" +
                "+cL2Swf3ZiR8wKcOAr23o8FmbH81K8H12058/mz2W4M3y8exGe" +
                "P0fvYn3G8+fstmqr2pJWzopJHwhzsCPm45xrs1A3zdAc6yKtmi" +
                "p7RUS9E+/WKsCBr1waiWMPq6G0ch7HhtoHwhzsiPk457JqW580" +
                "OJIyWD31mfOFDPbPTuyIWQEOfGVbj8fC7Li/x/PSPM9nc9AcSC" +
                "tnxaQPhDnYEePD57Jq+0xGGhxJGaya+sz5Qgb7Zyd2xKwAB76y" +
                "rcdjYXasz/i+z/fvnzGffj539uN5fn62WJ/TvH5W69W6tHJWTP" +
                "pAmIMdMR/nXJv1//mX+hZvn5DX2ZWtmip7RUS9E+/WKsCBr1wa" +
                "iWXH+oz7e9zfr879vV6sF31EMMHRArOxcpzVcttxRLLyHEW11T" +
                "3Psg5sXZvjXZbq53JKCGrH+jzv35OrH90jJ/EUOa1CV/3+Xqf8" +
                "e/JRdZTHBEcLTD8jMxefVAERVZqUqa3upRFY194fZ/hz2WNeIa" +
                "95fIy+pLnDt2f7Gw3fXN23o2alWZFWzopJ3yPgMtvm84EK4AC1" +
                "iqkvq8qI51gGV9NI6sRrYy5U3Va29ez8gJ37f9i9V/pp9LXD9b" +
                "j9f9i9lzN9X56r/4dtVptVaeWsmPQ9Ai6zbT4fqAAOUKuY+rKq" +
                "jHiOZXA1jaROvDbmQtVtZVvPzo8dcbI+n8evRL2/LRO/752U4v" +
                "t+vE7XmjVp5ayY9D0CLrNtPh+oAA5Qq5j6sqqMeI5lcDWNpE68" +
                "NuZC1W1lW8/OD9j1YX0orZzH706H2gfCHOyI8eFzWbV9PyMNjq" +
                "QMVk195nwhg/2zEztiVoADX9nW47EwO7eNvsd1sPf2FxVWTiA=");
            
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
            final int compressedBytes = 1181;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj2LHFcQPLBBBge2wSAcGGMMzi91dju3v8AoEqv8YukPSA" +
                "jr0DlWaASKFUsgEOgPCMRJiRxc5p9wsdlt9VZV93t7u+M5vKB+" +
                "w/TM64+qes3MzjB3Bwd53L93UOM/j6O/3dpZK5rPr8LbPeYZ27" +
                "BwlirvV3tkM36PP1Zvo/LR27q6phx1v1c/92c8uLmyX63nPyz7" +
                "+eBbyvj5CoRvNsR+SZ7vJ9L99RXxH3fE+y55ftqp/telPV4cL8" +
                "za0X02h4dzsCHGe6xlVGdnDI7kDEbNOlu6UMH6WYmumBGgIDIr" +
                "H6+Fs4f3w/vYafOZHxY+jeW4Y+osj2XEqto5GQcVOUsVKq+i6y" +
                "r6/K2anmfdgfPhPGWufOaHhU9jOe6YOmtoPHemdk7GQUXOUoXK" +
                "q+i6ij5/q6bniaok9qyL+my735Kc555tEXbFH6/1+sfZi3pKT9" +
                "rPl9WDel/aj/el1nj8V11lU44/nlcP6n6f9n5/+GTs/V79nLKf" +
                "szezN2bt6D6bw8M52BDjPdYyqrMzBkdyBqNmnS1dqGD9rERXzA" +
                "hQEJmVj9fC2bten2fv9uP6nL0ae33OXm1zfZ7+M93zvb7X1fNo" +
                "f94/q5/X/T5f75/jx/zu/K5ZO7rP5tGDXM7Wet7BgBx4FTHrUl" +
                "T2xBzNYDaPZCURG71wdGVWPu0Pr3i3+/301udxv5/+Xr+f9Tyq" +
                "flY/q5/Vz+pnfV+qfvb6+fD5+OtzuDPcMWtH99kcHs7BhliMc6" +
                "1WGTvP1e/6OB5ZM3JERDQqiWoVAQoic28lml3fl+r7Uj2P6nlU" +
                "z6ORz6OT4cSsHd1nc3g4BxtiMc61WvXp95vm6l//wp+wKmXNyB" +
                "ER0agkqlUEKIjMvZWE7MWwMGvHVWzhc3g4BxtiMc61WvWJn+bq" +
                "XytcsCplzcgREdGoJKpVBCiIzL2VhOx3Q/oLm/nMDwufxnLcMX" +
                "WWxzJiVe2cjIOKnKUKlVfRdRV9/lZNz7PuwOVwmTJXPvPDwufn" +
                "qGzFFa2p8dKZkNOqdOtbSy2raOvjinjsa2wjtDGX+/Ht49tm7b" +
                "iM2czP3SIHG2K8x1pGdX7G4EjOYNSss6ULFayfleiKGQEKIrPy" +
                "8Vo4e344PzRrx9W3+0OfRw9yOVvrefdaYKz/VpEQw987DpUH8x" +
                "aGZjCbR7KSiI1eOLoyK5/2B9mzG7MbZu24jNnMz9mDXM7Wet69" +
                "Fhjei4yo/dQ4z1sYmsFsHslKIjZ64ejKrHzaH15xvX9O+z7v4+" +
                "jCrZ21ovm8PzZlXY1wdLENC2ep8n61Rzbj9/hj9Qaej27trBXN" +
                "5xsUfRwX84xtWDhLlferPbIZv8cfq2PW7GJ2YdaO7rM5PJyDDT" +
                "HeYy2jMq8iuj9mMGrW2dKFCtbPSnTFjAAFkVn5eC2cXd/rrn9U" +
                "P6cdR6/3A+P/XN1Y/cOH4UPbZ35Y+DSW446psza3VbVzMg4qWi" +
                "vQCGMquq6iz9+q6XmiKh5nv9U9OuWofk47/vyiejBpP7+sHkw5" +
                "6v+Tx4/50/lTs8sjfD6HdR+iHMOOKmbIHBGxpQw8Pos6sy5oUI" +
                "2shKsUgfl5V5sVr7n+BS79H4o=");
            
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
            final int compressedBytes = 1501;
            final int uncompressedBytes = 19489;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWU1PJFUU7aWJCzUxMS4cY0xcARq2urKmZmP8DwOGDQRIOr" +
                "JzMXYggQlrf8DwP+AHzZ9Q6vat83GrhjBpdUJeVeq9vvfce855" +
                "j26aKvqb/max6G/6YY4joogxZg4oY7jQBTawIuuMCztUJyP3WX" +
                "3Bg3pkJ9ylDKzPl47VMbRWXwzjR4msvvzz93/GT1G7+mbxzmP1" +
                "yTuwb0vm88VGjtXHD+BfPZLvs5L5+lH9363ntp9tP//1/Tz/q+" +
                "1ne38+vfdnf9afxRhz5iL2DGq5Wvv5ggJqkFVG+3Y/Ux3EUxxa" +
                "wWqJVCfOjb1IdlVWPd0fXnH/tvyl8vY+F3mMyCnGOMdzkeo4U/" +
                "Wh1ZqbqspXr57N6XrtFOvDR13j/DoXT+B49ez/0W2/Pzf5fdS9" +
                "7F7GGHPmIkaGa3ACc5x7tSvUOdZ8+mPcVSuzMwJ1J+5WGeDAle" +
                "dWYtVH3VGMMQ/YUcbIcA1OYI5zr3at9SnW/OjwiF2pamV2RqDu" +
                "xN0qAxy48txKrPqgO4gx5gE7yBgZrsEJzHHu1a61PsWaHx0esC" +
                "tVrczOCNSduFtlgANXnluJVV90FzHGPGAXGSPDNTiBOc69zDrq" +
                "EwcjtYLZq88pX+hg/+xEV8wMcODKqsdrkerD7jDGmAfsMGNkuA" +
                "YnMMe5V7vW+hRrfnR4yK5UtTI7I1B34m6VAQ5ceW4lVr3f7ccY" +
                "84DtZ4wM1+AE5jj3atdan2LNjw732ZWqVmZnBOpO3K0ywIErz6" +
                "3Eqs+78xhjHrDzjJHhGpzAHOdeZh31iYORWsHs1eeUL3Swf3ai" +
                "K2YGOHBl1eO1cPXq+/qX1Otf89X1D4/4+2usff3Lf/r389aHdB" +
                "fRb/VbMcacuYg9g1qu1n6+oIAaZJWx+lJWzniNVrBaItWJc2Mv" +
                "kl2VVU/3h6p3+90YYx6w3Yw9g1qu1n6+shcc414URtvPXdVBPM" +
                "WhFayWSHXi3NiLZFdl1dP9oerb/jbGmAfsNmNkuMar9UJXvmZW" +
                "ZJ2xfHJEJyPlmPIFD+qRneiKmYH1+fJOdwytdv++2efz9bj+cd" +
                "GODR7XP7U9eO/v9zf9mxjvZ+Qyxpg5oIzhQhcrVA1nnHIGnYzc" +
                "Z/UFD+qRnXCXMrA+XzpWx7z68v78ub3PNnncfx+1oz2fb/8vfn" +
                "r72S/7ZYwxZy5iz6CWq7WfLyigBllltG+jpeognuLQClZLpDpx" +
                "buxFsquy6un+oPr5i+cvYoz5HosoX+eIGpzA+PJeZs29YA5Gag" +
                "WzVp9TvtDB/tmJrpgZ4MCVVY/XwtXt877h/28ed8cxxpy5iJHh" +
                "GpzAHOde7Vo/L6RY8+MTxWN2paqV2RmBuhN3qwxw4MpzK7HqvW" +
                "4vxpgHbC9jZLgGJzDHuVe71voUa350uMeuVLUyOyNQd+JulQEO" +
                "XHluJVa97JYxxjxgy4yR4RqcwBznXu1a61Os+dHhkl2pamV2Rq" +
                "DuxN0qAxy48txKrPqqu4ox5gG7yhgZrsEJzHHuZdZRnzgYqRXM" +
                "Xn1O+UIH+2cnumJmgANXVj1eC1e376MNfx+ddqcxxpy5iJHhGp" +
                "zAHOde7Vr/PCnW/PgTP2VXqlqZnRGoO3G3ygAHrjy3Equ+7C5j" +
                "jHnALjNGhmtwAnOce5l11CcORmoFs1efU77Qwf7Zia6YGeDAlV" +
                "WP18LV7fO+4fvN7X47xpgzF7FnUMvV2s8XFFCDrDLa/ea26iCe" +
                "4tAKVkukOnFu7EWyq7Lq6f5Q9V1/F2PMA3aXMTJc49V6oStfMy" +
                "uyzlieJ4tORsox5Qse1CM70RUzA+vz5Z3umFdfnif/1p5itufJ" +
                "H/D/4w7aHrTn8+35fNvPtp8P3m+edCcxxpy5iJHhGpzAHOde7V" +
                "rfT1Cs+fGO44RdqWpldkag7sTdKgMcuPLcSrS6vT/b573tZ9vP" +
                "tp/v+Txkp9+JMebMRewZ1HK19vMFBdQgq4x2976jOoinOLSC1R" +
                "KpTpwbe5Hsqqx6uj+64nJ/9Ee7y2n3R+33Z9vPtp8P7uffEvyn" +
                "2w==");
            
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
            final int compressedBytes = 693;
            final int uncompressedBytes = 4705;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqtlV2KGlEQhe9jIA9JIBDyEEIIZCOXXomir4puIK5FR5eZ2H" +
                "Wrz0/REwZi03Wn6lSd+lpnnL7ru9b6ro+ztczy54zowQXNdZ7V" +
                "qdZ4h07kjFLUrdXZHaE6idOqAwh889qTaHdrly9zfJfK5evv89" +
                "/4Eb2XH+3V1+XDK9rPUvnc/svr8v4f+rc3+n0qle9vmv81v6+b" +
                "vokYZ9YiR4V7cEFznWd1anyelGt9+cQ3TKVbq7M7QnUSp1UHEP" +
                "jmtSfR7ulleon4POMVWeSIWYPKGm5MwQ2uqLqjf966JzPnrFxg" +
                "UEYm4Sl14P18a6zES+d1ukZ8nkO7IkfMGlTWcGMKbnBF1R3L+y" +
                "l7MnPOygUGZWQSnlIH3s+3xkqcnf3UTxHjnH93T5mjwj24oLnO" +
                "szo1/j4o1/ryF3RiKt1and0RqpM4rTqAwDevPYl17/s+Ypyzts" +
                "8cFe7BBc11ntWpsZ9yrS+Ee6bSrdXZHaE6idOqAwh889qTWPeh" +
                "HyLGOWuHzFHhHlzQXOdZnRr7Kdf6QnhgKt1and0RqpM4rTqAwD" +
                "evPYl1b/s2Ypyzts0cFe7BBc11ntWpsZ9yrS+EW6bSrdXZHaE6" +
                "idOqAwh889qTaPf0mB4Rn+f4bn0gR8waVNZwYwpucEXVHcv/I9" +
                "mTmXNWLjAoI5PwlDrwfr41VuKl8zbdIj7Pod2QI2YNKmu4MQU3" +
                "uKLqjuX9lD2ZOWflAoMyMglPqQPv51tjJV4679M94vMc2h05Yt" +
                "agsoYbU3CDK6ruWN5P2ZOZc1YuMCgjk/CUOvB+vjVW4uzs536O" +
                "GOf8XXDOHBXuwQXNdZ7VqfF9Q7nWl2+kM1Pp1ursjlCdxGnVAQ" +
                "S+ee1JrPvYjxHjnLVj5qhwDy5orvOsTo39lGt9ITwylW6tzu4I" +
                "1UmcVh1A4JvXnkS72x8eVr7/");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 10, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 13, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 0, 0, 18, 0, 0, 0, 19, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 21, 0, 22, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 24, 0, 0, 2, 25, 0, 0, 0, 3, 0, 26, 0, 27, 0, 28, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 4, 30, 31, 0, 0, 32, 5, 0, 33, 0, 0, 6, 34, 0, 0, 0, 0, 0, 0, 0, 35, 4, 0, 36, 0, 37, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 6, 0, 0, 0, 39, 40, 7, 0, 41, 8, 0, 0, 0, 42, 43, 0, 44, 0, 45, 0, 0, 9, 0, 46, 0, 10, 47, 11, 0, 48, 0, 0, 0, 49, 50, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 51, 0, 0, 0, 0, 0, 0, 52, 1, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 12, 0, 0, 0, 0, 1, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 2, 0, 14, 0, 15, 0, 0, 53, 0, 2, 0, 0, 16, 17, 0, 3, 0, 3, 3, 0, 0, 1, 18, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 54, 0, 0, 0, 20, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 56, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 57, 21, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 6, 58, 0, 59, 22, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 60, 0, 23, 0, 9, 0, 0, 10, 1, 0, 0, 0, 61, 0, 24, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 0, 11, 0, 2, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 63, 14, 0, 64, 0, 0, 0, 65, 0, 0, 0, 66, 67, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 14, 0, 0, 68, 15, 0, 0, 16, 0, 0, 69, 17, 0, 0, 0, 0, 0, 25, 26, 27, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 28, 29, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 33, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 1, 35, 0, 2, 0, 0, 0, 5, 4, 0, 0, 36, 0, 37, 0, 0, 0, 0, 0, 0, 0, 38, 3, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 39, 16, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 1, 6, 5, 0, 0, 43, 0, 7, 1, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 70, 45, 0, 46, 0, 47, 48, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 54, 0, 1, 0, 55, 0, 0, 8, 56, 0, 57, 0, 58, 0, 0, 0, 6, 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 59, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 3, 0, 8, 60, 61, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 62, 0, 0, 0, 71, 0, 0, 0, 0, 63, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65, 66, 17, 18, 0, 0, 0, 19, 0, 0, 0, 0, 0, 20, 0, 0, 67, 0, 21, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 0, 68, 25, 26, 0, 0, 0, 69, 70, 0, 0, 0, 4, 0, 0, 5, 0, 0, 72, 71, 1, 0, 0, 0, 27, 72, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 1, 0, 73, 0, 0, 0, 0, 0, 0, 74, 0, 0, 6, 0, 11, 0, 0, 0, 0, 0, 0, 0, 19, 30, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 1, 0, 0, 0, 11, 0, 73, 74, 12, 0, 75, 75, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0, 0, 0, 76, 77, 9, 0, 0, 2, 0, 78, 0, 0, 79, 1, 0, 80, 3, 0, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 82, 0, 83, 0, 0, 0, 0, 0, 0, 0, 2, 0, 84, 85, 0, 3, 0, 4, 0, 0, 86, 1, 87, 0, 0, 0, 88, 89, 5, 0, 0, 0, 0, 0, 90, 0, 13, 91, 92, 93, 94, 0, 95, 76, 96, 1, 97, 0, 77, 98, 99, 100, 78, 14, 2, 15, 0, 0, 101, 0, 0, 0, 0, 102, 0, 103, 0, 104, 105, 0, 0, 10, 0, 1, 0, 0, 0, 106, 4, 0, 1, 107, 108, 0, 0, 4, 109, 0, 6, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 111, 0, 0, 0, 0, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 20, 0, 0, 5, 16, 0, 17, 112, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 18, 0, 0, 19, 0, 0, 0, 113, 7, 0, 114, 115, 0, 11, 0, 0, 0, 12, 0, 116, 0, 0, 0, 0, 20, 0, 2, 0, 0, 6, 0, 0, 0, 4, 0, 117, 118, 0, 5, 0, 0, 0, 0, 0, 119, 0, 0, 0, 120, 121, 122, 0, 7, 0, 123, 0, 8, 13, 0, 0, 2, 0, 124, 0, 3, 2, 125, 0, 0, 14, 126, 0, 0, 0, 15, 9, 0, 0, 0, 0, 79, 0, 0, 0, 0, 1, 0, 21, 0, 0, 0, 22, 0, 127, 128, 0, 129, 130, 131, 0, 132, 0, 0, 1, 0, 0, 0, 133, 0, 0, 23, 24, 25, 26, 27, 28, 29, 134, 30, 80, 31, 32, 33, 34, 35, 36, 37, 38, 39, 0, 40, 0, 41, 42, 43, 0, 44, 45, 135, 46, 47, 48, 49, 136, 50, 51, 52, 53, 56, 57, 0, 5, 58, 1, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 137, 138, 139, 0, 140, 0, 59, 4, 81, 0, 141, 7, 0, 0, 142, 143, 0, 0, 10, 60, 144, 145, 146, 147, 82, 148, 0, 149, 150, 151, 152, 153, 154, 155, 61, 156, 0, 157, 158, 159, 160, 0, 0, 7, 0, 0, 0, 0, 0, 62, 0, 0, 0, 161, 0, 162, 0, 0, 0, 0, 1, 0, 2, 163, 164, 0, 0, 165, 166, 11, 0, 0, 0, 167, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 168, 1, 169, 0, 170, 0, 0, 12, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 16, 0, 0, 17, 0, 18, 0, 0, 0, 0, 0, 0, 0, 171, 172, 2, 0, 1, 0, 1, 0, 3, 0, 0, 0, 0, 83, 0, 0, 0, 0, 0, 84, 0, 12, 0, 0, 0, 173, 2, 0, 3, 0, 0, 0, 13, 0, 174, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 175, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 0, 2, 179, 180, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0, 181, 0, 182, 183, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 184, 0, 0, 0, 0, 185, 22, 16, 0, 0, 0, 0, 0, 0, 186, 0, 0, 1, 0, 0, 17, 187, 0, 3, 0, 7, 9, 0, 1, 0, 0, 0, 1, 0, 188, 23, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 10, 11, 0, 12, 0, 13, 0, 0, 0, 0, 0, 14, 0, 15, 0, 0, 0, 0, 0, 189, 0, 190, 0, 0, 0, 191, 25, 0, 63, 0, 0, 192, 0, 0, 193, 194, 0, 195, 19, 0, 0, 196, 0, 0, 20, 0, 0, 0, 85, 0, 26, 0, 197, 0, 0, 0, 0, 198, 0, 21, 0, 0, 0, 0, 18, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 199, 0, 0, 0, 0, 0, 0, 0, 0, 0, 86, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 0, 0, 0, 1, 200, 201, 0, 0, 0, 0, 0, 0, 202, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 203, 0, 0, 0, 204, 64, 0, 205, 0, 2, 3, 3, 0, 0, 0, 65, 87, 0, 0, 23, 0, 0, 0, 27, 206, 0, 207, 24, 28, 0, 208, 209, 0, 25, 210, 0, 0, 211, 212, 213, 214, 29, 215, 26, 216, 217, 218, 27, 219, 0, 220, 221, 6, 222, 223, 30, 0, 224, 225, 0, 0, 0, 0, 0, 66, 0, 2, 226, 0, 0, 227, 0, 228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 229, 31, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 29, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 67, 68, 0, 0, 0, 230, 4, 0, 0, 0, 0, 0, 0, 30, 0, 0, 1, 231, 232, 0, 1, 31, 0, 0, 0, 0, 4, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 233, 69, 0, 0, 234, 0, 0, 235, 236, 0, 0, 0, 0, 32, 33, 0, 0, 3, 0, 0, 237, 0, 238, 0, 88, 239, 0, 240, 0, 0, 34, 0, 0, 0, 241, 0, 242, 35, 0, 0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 243, 0, 244, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 7, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 37, 245, 0, 40, 0, 246, 0, 38, 247, 248, 39, 249, 0, 250, 0, 0, 0, 0, 0, 0, 0, 0, 0, 251, 40, 252, 41, 0, 253, 0, 254, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 256, 0, 0, 257, 0, 8, 0, 42, 0, 0, 258, 259, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 260, 0, 261, 262, 263, 264, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 265, 43, 9, 0, 0, 10, 0, 12, 5, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 70, 0, 0, 266, 0, 0, 0, 267, 0, 0, 0, 0, 44, 0, 0, 268, 269, 270, 0, 45, 271, 0, 272, 46, 47, 0, 0, 8, 273, 0, 2, 274, 275, 0, 0, 0, 48, 276, 8, 0, 277, 49, 278, 0, 0, 50, 0, 4, 279, 280, 0, 281, 0, 0, 0, 0, 0, 0, 0, 51, 282, 283, 0, 0, 52, 0, 0, 284, 0, 0, 0, 285, 0, 0, 0, 286, 1, 0, 0, 0, 5, 2, 0, 287, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 288, 44, 0, 0, 0, 0, 0, 71, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 289, 0, 0, 0, 0, 2, 0, 290, 3, 0, 0, 0, 0, 0, 11, 0, 0, 1, 0, 0, 2, 0, 291, 45, 0, 0, 0, 292, 0, 0, 0, 0, 0, 0, 293, 0, 0, 0, 0, 0, 54, 0, 0, 55, 0, 294, 0, 0, 0, 0, 0, 0, 56, 0, 0, 36, 0, 0, 0, 37, 5, 295, 6, 296, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 297, 298, 3, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 299, 0, 300, 0, 301, 302, 0, 0, 0, 0, 0, 0, 303, 0, 0, 0, 7, 304, 0, 0, 0, 57, 0, 305, 0, 0, 306, 0, 0, 307, 308, 0, 46, 309, 0, 0, 0, 58, 89, 0, 0, 0, 310, 311, 59, 0, 60, 0, 2, 26, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 90, 0, 0, 0, 2, 47, 61, 0, 0, 0, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 312, 0, 48, 313, 49, 0, 72, 0, 50, 0, 0, 0, 0, 314, 63, 0, 0, 315, 64, 65, 0, 51, 0, 316, 66, 317, 0, 67, 52, 318, 319, 68, 69, 0, 53, 0, 320, 321, 0, 70, 54, 322, 0, 55, 0, 0, 0, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 323, 0, 9, 324, 0, 0, 325, 326, 327, 72, 0, 0, 0, 3, 0, 0, 328, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 56, 0, 0, 57, 58, 329, 73, 0, 0, 0, 0, 74, 0, 0, 38, 0, 0, 0, 0, 0, 330, 59, 331, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 332, 0, 6, 0, 0, 28, 0, 0, 0, 333, 0, 0, 0, 0, 0, 0, 61, 334, 335, 0, 0, 62, 336, 0, 63, 337, 0, 64, 338, 65, 0, 0, 75, 0, 0, 339, 340, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 341, 342, 91, 0, 343, 0, 0, 344, 0, 0, 0, 77, 0, 0, 0, 0, 0, 66, 0, 78, 0, 345, 0, 79, 67, 346, 0, 347, 348, 349, 80, 81, 0, 350, 68, 82, 351, 0, 352, 353, 354, 83, 0, 0, 0, 0, 355, 0, 0, 0, 0, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 0, 0, 0, 0, 0, 69, 356, 0, 70, 0, 0, 0, 84, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 357, 0, 358, 85, 359, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 360, 1, 0, 4, 0, 5, 0, 0, 6, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 361, 88, 76, 77, 362, 0, 363, 364, 0, 0, 365, 366, 0, 0, 0, 7, 0, 92, 89, 0, 0, 367, 0, 368, 0, 369, 370, 0, 90, 371, 372, 373, 374, 91, 92, 0, 0, 0, 375, 0, 376, 377, 378, 0, 93, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 78, 0, 79, 379, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 380, 0, 381, 0, 0, 95, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 382, 383, 0, 384, 0, 385, 386, 0, 0, 0, 0, 97, 98, 0, 0, 0, 93, 94, 0, 99, 100, 101, 387, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 388, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 389, 105, 106, 0, 82, 107, 0, 83, 390, 391, 0, 0, 0, 392, 0, 0, 108, 0, 0, 84, 0, 393, 0, 0, 85, 0, 394, 0, 0, 0, 0, 0, 0, 0, 0, 86, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 395, 0, 0, 0, 396, 0, 397, 0, 87, 0, 398, 0, 88, 109, 110, 111, 0, 399, 0, 112, 400, 401, 0, 113, 402, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 115, 0, 116, 403, 0, 404, 0, 0, 117, 405, 0, 118, 119, 406, 0, 120, 0, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 2, 5, 6, 1, 2, 5, 6, 7, 8, 1, 5, 0, 2, 0, 3, 9, 1, 5, 0, 10, 0, 0, 11, 10, 12, 4, 11, 0, 13, 5, 1, 0, 6, 1, 0, 14, 15, 16, 17, 10, 11, 18, 6, 2, 16, 19, 5, 6, 11, 20, 3, 17, 10, 21, 22, 23, 24, 1, 0, 25, 26, 3, 27, 28, 1, 29, 30, 0, 4, 31, 5, 1, 32, 0, 11, 33, 34, 6, 1, 0, 8, 35, 36, 16, 2, 37, 38, 4, 1, 39, 1, 2, 7, 40, 41, 6, 42, 43, 13, 44, 45, 2, 46, 1, 47, 0, 1, 48, 49, 3, 5, 50, 11, 51, 52, 53, 54, 0, 1, 6, 1, 55, 56, 7, 10, 4, 0, 57, 16, 58, 59, 20, 3, 60, 61, 62, 63, 2, 18, 15, 64, 65, 66, 20, 67, 21, 68, 5, 69, 17, 70, 3, 71, 72, 73, 0, 74, 1, 21, 75, 2, 76, 77, 78, 20, 2, 79, 18, 80, 81, 82, 6, 83, 84, 9, 5, 10, 2, 85, 3, 86, 87, 1, 88, 1, 89, 1, 90, 91, 92, 22, 93, 94, 95, 96, 3, 97, 98, 1, 9, 99, 12, 5, 100, 101, 102, 103, 2, 104, 105, 106, 0, 107, 108, 5, 109, 0, 110, 25, 8, 6, 4, 27, 111, 112, 10, 7, 113, 4, 1, 1, 114, 8, 11, 115, 116, 0, 117, 4, 118, 119, 120, 121, 122, 123, 124, 10, 21, 0, 125, 4, 1, 1, 126, 127, 2, 29, 1, 4, 0, 128, 17, 2, 12, 129, 30, 130, 131, 132, 0, 11, 29, 6, 133, 18, 1, 134, 7, 14, 5, 0, 135, 20, 18, 3, 136, 137, 138, 21, 22, 21, 5, 17, 139, 1, 8, 140, 141, 22, 142, 6, 143, 144, 5, 145, 146, 147, 148, 149, 150, 31, 33, 151, 152, 9, 5, 153, 35, 16, 3, 154, 155, 7, 156, 8, 157, 158, 159, 160, 5, 161, 3, 162, 163, 164, 36, 18, 165, 166, 167, 40, 168, 2, 6, 3, 169, 170, 17, 39, 171, 172, 2, 173, 174, 175, 43, 26, 44, 176, 177, 2, 178, 56, 22, 12, 179, 180, 13, 25, 181, 182, 183, 184, 185, 186, 15, 0, 187, 188, 45, 3, 5, 20, 189, 190, 191, 9, 192, 193, 13, 1, 194, 195, 196, 22, 0, 2, 30, 197, 24, 22, 198, 2, 17, 20, 9, 4, 7, 23, 1, 199, 8, 200, 201, 0, 7, 9, 202, 5, 203, 1, 204, 205, 4, 206, 22, 207, 208, 2, 0, 209, 210, 211, 30, 8, 10, 10, 2, 2, 212, 8, 21, 3, 213, 214, 8, 215, 216, 52, 217, 10, 218, 219, 220, 1, 221, 222, 223, 8, 224, 47, 3, 9, 225, 22, 14, 226, 227, 9, 228, 229, 16, 230, 231, 232, 233, 234, 235, 20, 236, 237, 238, 239, 240, 3 };

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
            final int compressedBytes = 1593;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXc2O2zYQHnK5LnfTg5IagdMTszGKHHoq8gDcogh8zDG9Gd" +
                "jc+hLcAC02D9BDb7vHvsXumyS3PkapH8v6oSTaoihKngHseC3/" +
                "SPPzzccZjnNDbuHuR3Wu7xe/qlcf5XcgruWnn5/fwb9AXv/3G7" +
                "/nC/Zyxf/+69P6m/xFbqh8C2/Is9+fXv6zBZQQhNaeudQ3XnqG" +
                "FR4rfSP6JlF1KEaJav5jcrcZ+M8bHQxcX6nGP0bVKoIFCCJ/Ev" +
                "rQHcBWsYt7+gKWK64Wkgm4go2Qd/qfZ6sneIX4Bzdx/lhBfL+g" +
                "cf5YxPljvc7yxyPj90CZ1t+tzh8Pcf6ANH98fFpi/ugv8paAim" +
                "I05zosiZRpWLLnCi6AEKmjmMTYz4lai1sZxceFNox+0VIh/ufx" +
                "/6Dj/zKP/7VWkQ5zEEr7L6U8if/HNP5hM6f4l+QMHl/k/rORGv" +
                "olh+9/SPznViZZIPaf853/kNh/LuAa/ScQ/BUN+PtFh/nXGH+v" +
                "zPiL/L07f30t5a9vqL8B8eezRt4K/pAm/CEG/OEZb81E8HzlQ+" +
                "Kcl72Exekx+StNhij19aFReIRKQkFBQUGZFf9rr3/DIuZ/fzbX" +
                "vwuFsqjCRwrP0l2KvU75xxZc8I8/yAN08X/awv9vzkv898MB78" +
                "f6DcqQ8rZ78eDw20gSprwQtckjkUV0JKpBToOofxfXr2enGr80" +
                "u2Msw1hSgeWG9c0M1n95/yIq9i/Wsbfu65dJ/+J91r+AAPsXRe" +
                "NQunvqGkytzTwCBUIkSptLEVv/qb+Ig/f+uayeIPeR/8z9zx1+" +
                "bBP8wP4nij/8HyE9T37/jCF/dp9egn+SYP+44gzU8oVO+MfY/e" +
                "ci/l+a+ONmxx/z75dDfH/t+qF2/TLc/vvI+IVyshL3T9Xh+zfI" +
                "cPs39us3G/5t7r+/M9Yv3pnql839+y+7/WdX06x/8M5EI/vnf4" +
                "f+97nuf2QE//PrvxV7mer/5f0HgPsP5imkef9JLWpTb4hkO38Y" +
                "AW+8759prz8U65eLff1SYP3BkoKa0okfacouuItoOKJg13+WLv" +
                "vPE3H+2cpNe//fYv5CHA8YAfDPjv0LlvMn451/OPSlof9tAJZS" +
                "/5vXNZg9vixpiA9cIOk6f9Fw/g4lOjp+Jilu4rd9/4Kn+bE+9f" +
                "PX9fo5K9XPYVc/v9/Xz0ln/fzw9WePJe/uO5mm3w3+O9P9Exb9" +
                "DxCZ/R4M9rNfv0xl/8WszIvi26TCCFoTJui8K3Ki+J7ncCyAig" +
                "KspiB/FhdR+DLpHKeiuLUxSGfSNap3lZuB8vx0eFNKGViUyxrA" +
                "0V7MRrzog88rTkjZR1RILe9pQ+coMIP1t+w+ivmwk0H2jgTHa7" +
                "AUP9VkF+20xR3dXoqwVqdPczr3hRSr3J4/gaWRJvA6sJACTRiV" +
                "twwlZBZwFrh/y5D1zwf4DHWY5VRXWnJtX3YoB4psPnH6/Cty5A" +
                "S8d1zYBsgE8YuW7B+VDpCSXrgxBPq+f1ZVDHKUAUQTwLTxJzKG" +
                "pwzzfjaYneihjBVlpxjZzw/YodEou01P+6er8KjZ9PDTMv+KQv" +
                "6lbUZQ9T94VSE05EsdhD/xvRa4KyfmgBKs7xzsRQq1d1IyGAby" +
                "0K7rkPlpv/wB/Q+lIJ3zj6b957X9G7h/4iT8F+MnOP3bzP81zA" +
                "8OPf9XS8pRI0Dg/mOMX7z+sesd0DS/WYvaNPTczc8w9J+gz3++" +
                "C/Wj5i+d719G/jwp++P87Ynhr0Ld4/o1ePE8f+dm/cUbM6Df+c" +
                "kxWbfN9c+dIBw7fzue/6N0k1rmGi5OOhUHmL+M879gnv8VVvO/" +
                "fn9/ckjv9za/3Mt+9vO/J2W/6YBsZf5ym89fJrPsVvOXrCcqlf" +
                "Z/qcKB2v4vZoXjuN4ad+mApkEZezVwSk7HyosVUaR7siFKU/x2" +
                "On8/EL+Us7ZcUf+0Q/+Ritr0H2z8+RUViv4jNynTA4v3SADmir" +
                "/75YjBfwik+Akwjfj1XGwo68+Qv9L/kGxZz18hUNqwEA0FxXeg" +
                "t81vscr8lvTn0tYhzOrV/sIuQlMjgImgVtUzF2tnIWbqKQLHU9" +
                "bSbZLmMxfOvnnw319zS0kxFw7ifw0Mrqptnt8hfs3a+N0iUHUo" +
                "wS4pnfFXcPf7A6bz/x9x17+T");
            
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
                "eNrtXUmS3CAQLGE8gZcD4ejD+IaXhxA+zTPmKdinOc4T5iv+mS" +
                "X3JqlFC0nsyjyoN0lAVVFZBWog6sAJAID9oe35sjsKUqdvFLHT" +
                "W9YdjO6O7zg1JA5kLhcaQRriC6ORIfTlYDsD2AI2kKgc/NBQX/" +
                "ji/KE931Qtk8bv7YyLyTfzFVKN0/2iC4ufbGMIQeOe2556dbMR" +
                "sUVeYuazdywqwIG/DPgLAAAHSLsfYrdnnx2ISheRTfi/g2v8/i" +
                "ub/GGF/Hr8eaD3Yoro2PHWasS15lz+uP4G+U96QKgAkHNCkCb/" +
                "1mdGljmmgEBVoR+QXnoykkpkz/8Yh/G/8B40vmuz+l+ytr8auk" +
                "ur/wzmX274Vzq2v5Z5CGYfjJV37MlljHzr9ci/AGDnIZQpIYsD" +
                "LlgSJt3lf7aG/xmbIfs6BAdMi9CwEqvtnumF8nl3xaZmKsGtVZ" +
                "WzTcwv//QFlTjDVVb1gjiB6EkmYGV9bg2OEEZOBkZ4BHQUPz+l" +
                "Gz+D/QLp7F/eyR8FhXv+JgUH7p1DEUNA/hb+K+L/O7Bf2D8A/g" +
                "SW+G/jzX/Dfshboqaub7c8f1465ODFAQb194kfbYVEa0q/6YUz" +
                "8yjpgVSjf3YW99IaqeHijTE6PArzoLmi7/RE+qV9+fT4l74+j/" +
                "2P8hc/Kvh2V/+5Z/8B/qlWfnxIrIEcgrAy+MfZIgMkwHB7m7l4" +
                "pNm4+guOEPGD7pudQydv3BQA1Jq8rHRV4N9l/cn3vArkv9I9wg" +
                "Mc+VQFIqEl6yfdVIFtvD6D9t+LTF1CGW7t7mbqpnhOdgGrsX3F" +
                "6SoGEc2UzxKWX0oIpi5Xrp6/dV+/Zau/00n7UGD+HsmfpuTfnO" +
                "RPffnrPCxvif2srH84/S+rvzRr6l99/JeBFfb7D/X1d5T+/zhu" +
                "YH+E9ZPK0l9I/3fH/0YpHygxgw/DP2v4383/UTL/lzZ+S1/+Zv" +
                "2viJ/kolrkbT9Z50+v7utfmqn8SXYylr2hguNJXyZU2KzULzCr" +
                "f2EbttGTw11cRTe/yfHDkOufuo//ebheLMyeVPj+v3H8M9X4q/" +
                "DUfnEtX4zLjzETFEt+28f/U9uP5/kHE9F/5cz/zKYUzH/kxZ/J" +
                "9Zc0fkwL5SLMzPq7rKrnBf//XV55nvHT/ybkp5zlJ7Ia/1PDD3" +
                "qD/bCb9v+x2c/n8O2XC1pRPn8rwuO7xUCO7dOhMwTtL3r+V52n" +
                "JKtdfyxz0vQJFrL8xli4LhLlxCwfcHZ2qqjaeo2AAnRnta58a/" +
                "4hj07hw9slfmzO8WPn1F+7+HllHsZ95+JhxwJuuHfvy3JhWbK0" +
                "Pogn134ZwZssvP7O8ZX7/Fsz5gujb0Nz+jY8dYI6dHfZcwYeJf" +
                "X+lW5RTMXJTgbtz3//7CrT0VCjcNXNzukdWUbsTYm2zp8EW7+m" +
                "EPkB+85oUu+/XcL+2Tp7+efe/hLjX1lQ/wsQ3UWSVlHrDweTKP" +
                "7/FtJ/7Cn+9pmHqZ6tYf+jgtLXi3pYRXy8oOlx279p/5Y69p/h" +
                "S3OAa7jwDwrAtdo=");
            
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
            final int compressedBytes = 979;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUmO2zAQpBgi4AA58OBDAuTABHkIMS+Zp3TmBfMEPzXS2N" +
                "biiFq5swqwIS+t5tJLNUXLjHUQ90d4CGZU9yyZvr+jGdfMsPZd" +
                "3r28mvap+SJY08gLo+6tpn0QSWZYuhCsJoTpLfelctu5JOttlC" +
                "CfkrzlBHzmm7J/cqnf9v2ZQ9gf5J3bf+T4V3v8zbj/regc/xry" +
                "LX2yrI5/sQf/up1ixL/q9j8+OYGafNB8HpiZNECIX6X4r56+CF" +
                "CWnGh/47FZJrJ+/6cHfNePylLvLdJoh/LsiPzZsvZk/kiM/zlq" +
                "mvQ98Jvtxxxphpqcq7j1G91Lfjzxx4udP9KEP75mMjrGu/+cXL" +
                "/IXv6s/3vRf9p/Qoyfk/ot7vw7yH+x1//qtb+E40cUoP6IAb1A" +
                "wqwMtaCpEmnqN7DME9ZsktXPxzN/7vq9lb/c5V+uPX+ZyH908i" +
                "oJz7PzL77Ov9zwt5P1S96R79T4AwAA5Bv/3O+fk7v0+89fh/Rn" +
                "wB/m+8/5kgwP1AwTSvugykTSD8SH2e7ukUFblx6WhHJYOkmp/Q" +
                "QPqbF+28Af/PMPlWTqUaOmleVelMX4Z4Cl66cCw1PP+JuCB9ll" +
                "/N89KeSoB1H1l1McxLYfoHAUyEFg/3XnTwDxAwASzj/kP/8c3/" +
                "8Ool5hQjU59F9g/mGuAJCDg2l3JBkkOy6d4xu+AwAA4CLgpHf/" +
                "wJD7J9f2P47kb/3nQfc/ioMFsyvdMfUDQFpIkBr/H7/e0tg/vl" +
                "2/tFLaEEx3U1ijcvVH14bmW6EYEDx+3uKXtscv3ccvifE7HKh1" +
                "OJc2mKDs/C8Uf6mKLHrwn+n88ZX5UwHnL17ypFy7UO3Sf+xIgv" +
                "0TKCkAAEB2cZj/Y68f1K6/SK6C+Qc28Vc53H90WCj7rPqk60pD" +
                "LFw0NrMfCV38+GdaPzQbGkspjfxt/YMebVy6f/t0/eo1Z/t57j" +
                "+L2f9l/3+2HLPT/k2J/g/GkzoC/n9H7PiPLa0O4+90/f3dFn+/" +
                "BfMrg2laAbcco6rZyWg1hgEIn6cj7l8DqsavnqER4yQV+8p0Y/" +
                "7c7VK3JnhtD39+Z8S7NY/fj2T8ov6yH28YP9Q/QFrVili1ARHZ" +
                "BgVqOUSj+fqTH6s/Y18/ALytP+D+Z0AeRdx0JQHtT4YRZRb/9l" +
                "//ug3f04VPmtGmG/tnEdsfzrJDyONXPasg2zipXeOYCQEAT6Ez" +
                "/I9m48d7sN8fKxb9/x/J28x4uP+FSm3/iim4/vAwf7vy91ieDo" +
                "7fhYnZWM9vHdRPfJQe8gto9vnPuP8D/+WD9TT3hk7Sk0nB/v4B" +
                "KjjKcw==");
            
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
                "eNrtnV2OozAMxwOblVhpH/LQA2RvgvYkcxRrTzBHmKPuUFoEFG" +
                "i+scP/91BN1TFxY8exSUiVGtCqU1ZNPP5s76/98NIML91NzejG" +
                "Tzzk1a78MUf/p5Uyy/atauft0136h/5W4Vt/mgTJvf336BjJtf" +
                "43V/3/Hl7ZhLbv1X8b8iaB/s3yLam8RNnv091+5GE/EMssIj1i" +
                "USl/WjV+MCx5xC+VMn6t9aec+kvvPwAAAAAkxqALAABKXTnZR5" +
                "0DAMjEcK+WpncWHRIOoQuq6X/jUX8ErD9Y+BX7+lG6PPP6W9du" +
                "/9j8Hf6D8S+i/62nBvTMOwO/gbM8exKuf/pbG2mFCHoRWppA/+" +
                "+3/L8fLnf3/6/h2zeD/zdP/78XrPv7l8bx85D/9TWNn4X85yCf" +
                "Oj52V7b/6cOjf99nLfsv0wnQEbDOHxLtPz3K74hfL+ykN0aKFS" +
                "O4qZ/dlmHa8dJ2ZT562h+UmX+lzd9WnNo6x7AqXb+lTyUcEtM9" +
                "/amc/t2Z9kP9Fun/5lz/LzH+UH9h/AEQzWVLe+96sTlZHoAY4H" +
                "87nWFl1L4AgKAEu2ySE7t+mK3+NC/159h+m3H90RQwL6i9SHHd" +
                "V8MnqWlgNYD4lScBxn4E+J90iG37ZfZvnOG/bn2+v35DjM7/yW" +
                "c/v/PXnM8f87z/pE8bRzj/CZyP3tgiQVP029o9oa2k+E8n9N+b" +
                "2cMmbh/5G7hy/Eow/1qB8y+H/MFmi2v1x69w+/0+fv7HUR6A2c" +
                "i1KUNBOdqdvzep51Q9nA8IKs7f+jItB82fm8/fNm7yzSz/8mjf" +
                "7/lfl/YvFD/6YtUyuFjk2hx/dn/82an+6vx9dw09Plxc6s+RwH" +
                "jJQeyDQf+F6q8S6V+m/Z5TslwC7P8FOaPGy5DlmL9eb/6zCuuX" +
                "eXNXKfmPq/1T7h+ggPbPtt/Z9+8lPD8sLU4Qs/6Pz99y65/7/O" +
                "B6438+/ylzc4hH/NGX9R/ED+T/QG78vkL9xVzP6s9vyhG/qar6" +
                "v4yhW8a+1UYKto6phGVmspX/ty/+/28v/mL/FQDXgHzmzxTPP2" +
                "D9jQdV/H4H5imQq34Fy0qSjf1Cfj8ER6pLTkVQaOefInW2bgl9" +
                "/tTW7zRy3Qog/5BPD/3hv/B/+dR4FNp/QqnFLw==");
            
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
            final int compressedBytes = 799;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuOGyEQLZhWRKRZoGgWiZQFiXIQlJPkKJWbzFFjx2OPF9" +
                "1uGLqggPcWVkszZZqifq/A3UQZsMn/aSgLC2UjqB4fAFpgyXFS" +
                "QHot7gNLxYDhG0Ur30TJ/vzpblMMZMNduuJ4/nxaTinBvRBf5Z" +
                "gdxTx5c5U3d/Kt5w+MBptauzB0NVKqaDZybvy8/PUc/1y7+H0d" +
                "f0X+JVX+NwxPg+Vj/cBfRRFhv8PaT1H+WJf3+taP97Xg0qyf4f" +
                "+pcKuX4+ffN//hHft/3uHPafKt47ee8UuaB3bjGqiIQNgV0IQp" +
                "4vfDaR/2HVz0bXFyQ4zwRQAA6kG+f1QVLMb/j+HfTfThNOh/di" +
                "TYny+1v9j3/Kn1/Bvoz9TyHx36190/Ll9/ufwD/9GQfz5uv3Xq" +
                "j3HtH/UL+g8AAAAAUJzgQ/WxTcn4Ss8PTXP+bKG4Vr/G86r81/" +
                "/rWf9m8/z/w/rXjsH/ekNPu72l9tf7/IG50vOc9885+R/xXxmG" +
                "0R+OBoplcdn6l5WPrxvmQNphGssDe45gBZwVAMbkvxL8cyb+Xl" +
                "Bk8TgWh5A6GJLPL/Vw/kWz/urU76Vo27+QvH/wp30o9rN3++Ob" +
                "/X1g/2zs/BUJSPToADV0V3lK1x+ckb/8No2xIo5Z+vw78AfEb6" +
                "C0fu93/XF+ve38BfJXVv17L89C84+K7b9O/mGx+cN+9dvvPPV3" +
                "oEH6dyphJUKW3WQnq6WQQoLs6bJddZvExci+rDxSy2xQtYKjMT" +
                "ZPHg0G8IdD6o+qhsRTL2a1POUay1O346cu0aCNpmXo8TXzH6BG" +
                "/mCoEADE+ftB8bPVe5Bav3+p7/c/BdDTXvmjbv4TO59/5v7R4b" +
                "/fj4L338P85fNXv7QQ52+HzX+HPb8+dmi/gTp4/xz8t3n/BvvP" +
                "ffv/YVFknPyrzv465A8AAADo/wzQ/wDyGSPQR/2oY3zd9Z9gYK" +
                "0uDwDD8h8/9TIFwv7pjJVD1/3b3vfvwD/05584sG5+3Bgyk2Xn" +
                "6RMFE3+9sbFwsszX0+X3r8SWlkA/r8r47P/Stz8PM6jTotioWH" +
                "6G3y+r9p9/rKPuvQ==");
            
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
            final int compressedBytes = 212;
            final int uncompressedBytes = 11393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmEsKhDAQRNvoIksZPECOEuZkOfr8QCIMzIeo3Z33FooLIZ" +
                "3uWFWKqGTYPhaxTBZw2/+wuX0sJdFbV0wi8+Ma186mJaRqJsrz" +
                "9I/TfQ7iUg1yiXLdaU2RtgBwfuA4FVDsH0/wn0392/n+ufyi//" +
                "Kt/ucG7zv0T+7qN9inw/KX0/6zf2CZ/+YX42ybGf+Nf3KhP+iv" +
                "ovylOb9a6F/ecf29149/A/CNZv2x/v3r/f+ddf2k/hapOb+ic6" +
                "jV9vImTw8ewjagn270s4f9B1DLDT2XOXk=");
            
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
            final int compressedBytes = 4224;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXAmcFcWd/qq6u/r1ewMLDodnIucIBAhEYxwFxSSKR0RnPW" +
                "BAUMArQQFFVMKqwQOJqFEBTSIiKMEDYkBJiIiCAhrc3+IFHut9" +
                "4WI2McHsJgF8+6/qqu5+73W/6TfzBpLt369fVVd3V1fX19/3/9" +
                "e/asbqDQaOLHJ8p9UR+6ADDhQdcCi6ogf6op81AP2tt1m9uxn1" +
                "fDwGYbD1Dq/Nnojj+HB+GTbhFDuPBr5bPIpzMB7nW3X4AS7ma4" +
                "VwxltrvIG4AlPxb7hGLIUNF22dz/kt9m9ZHp2xL75q7cPn26NZ" +
                "t8yJ6GlPZ32sTejDdzjHYQC+kdmYvY/t5rfjWziCL8TR+A6Ox1" +
                "DrOf43/KvVma9EI0baXdhqjGYN3mprG/8x/4v1MSbyLzAFV1o3" +
                "Yzo7j7l8qPUSalDL14hO6IhO2B/78Sk4yJqFg9GFdUcdeqG31Z" +
                "7bdgO+bq3F4fgm74uj+AIxCMfi2/guH8cbswtwEk7m4MfYjTgN" +
                "Z3k9MQpjrFqMxThrNl7ABbgQl2AyNuMyPhI/xNXuGbDgQPCPkI" +
                "HHv8u/ZDvRBu2sbu6LaI8D+IDcERZDN/YEuvN1dvfMBByCr2Eg" +
                "DrNH8mP50XwX/xOfiiPZEByDIfYmnCCW40R8D8NwOs7Ame6ZGI" +
                "4RONt6BeeyETgPF+H7mOBuEIMxCZd69bjcOgTTcJVYkc87g3G9" +
                "fYXol8/zne7H/CJ+Ja71HhF983pzjvYxdkbxC3SJzWtzc+n64f" +
                "wqtiqf906i/O7M/HzB5t3rDSOMDzXHYqkuv53f5Rwq+ptyd5DY" +
                "4pyjrpjsjHDaUslKd508zjyXW53PWw+FdeJHdEwYq/uG6da8l8" +
                "+zBirfRnd0tD6mtnxBVxLGVE4Y5/N2Lh+zWbPkL+tuju2pqnSt" +
                "/OXnU26U+LZu+ZP52A1jHFWHNZv9JlouHpe/2eP9I/6JTr8057" +
                "OXquMf5I60btV98CMxN7MquP9lfqq4je+ia6ZTC4cEPfVh9CmE" +
                "8QyTZ9+PPH1wgNwgXEfHhDFu5A/xJZjJH+Y749/FepRqqc9OEU" +
                "+JNap23S98uG7zfyjs9Lu4f1a/M7KLshcWvPlSk+PPUn1BefYN" +
                "yWN1j836SIz5jsw38olbtqf7fPSYrdbPPTioX2Gszp2n0NsnGe" +
                "Ogxz7IbBZboxgTj0/W9Y0TK/U70BdrN0bqqFXfYn1hzXxkwXP6" +
                "FmPsvuhjXMOsZdRvNZRfF72DeDxPXfdrvqxMPzRE+mBaHMbE4x" +
                "uo/s+oph38XaPV/B3xNGn1TfxtzMKtpNW3ox9/y7kcd7L63GJ+" +
                "AW7BT6DrwHE6PYX2m93f0a9io6/V1IbF2QnqPGm1+ga2wdZv+6" +
                "bEWGo1PZ2+DLFFYoyemZdYH5dy3sW5rlKrVYsPU3UcQTtptap/" +
                "lc/jEsy26fZM9DHOHBlg3JuOa/XZTqoF7/kYowvts32tzrxoeE" +
                "xaLXm8UgyTWh2wVmm1yv0YZ0UxtmZT2W1SqyMcvzr3E5UK69AQ" +
                "Y7TxMUZ7hTG3lkG+eaAl+Brth4nX+Ov6ng9Ab4FjgvNKqylVWk" +
                "0paTX9nluIsdRqKpVafQeuUozeEtpju7Nvj+nK+4w99oY49ES2" +
                "1O4k7rf3lfY4BuMG7/Tsq9IeqxKFce7J3EKDcWCP50BqcYbqX+" +
                "RjrL9DhTGlxGP0oXt7GIz1MwKMMTQ7UNpjVUr2WKVzpVY7jsGY" +
                "9nm4MnsPpNKRVjtC2mN1TtnjkMehPQ6+FIWxdyuOsraLU4sxVn" +
                "34hrTHQamyx5Rqe0y5y2gne0y/lsK4XtpjgzHaqbvIHnu30dNK" +
                "MPbtcaadZvR+xh7TuRMMxr49VtcbjJU9ptyEKMak1XdpjN8ijO" +
                "+2J9mX4OfZkwnjn5LPNU5iLEahrz3Bvth5XYxnpEfuNXTnIKpl" +
                "NLH0VNpPCdpGfZyjb9/H2N9yT+UWibH6vMJY5draE90aiXGB3n" +
                "bTaR8xRmF8fJJCEcb0LRRirL9f36afq68jrZY+lyprRI0u7Uj7" +
                "z4pqrCtQg7W69Ci7VgxLaMNpkTxhHDkKMFZHlqpxsI+xvkKjhw" +
                "Oyp0mMS+oeSNq/PKNVx56sSzXG+uj0Ahs4Ump1FONIbffo9F5n" +
                "gM9j+tbewULi8QKvn9cf9xmtxnwsYmtxv7r6gXitJlzW+lpteF" +
                "yTzT0Q1WrJY22PyBPwvh7yOPNYlMfq+zghicf0ZQ0s7XXjcxke" +
                "G3vs8zheq9WR5PHsUh7nDqe7ZJ/F8DizItTqSAtLtFqnwvpOpL" +
                "SNTkmrc98ifv01TquJxx1CrZY8jtPqQh4XtEXzmPbFPo/F+2Di" +
                "93gYj4pPFY+X4EA85P0SXemavljqDER/8QGrxwrxGX5JWv0ren" +
                "at5HHudrE9wuPXCnlcMza3O3iq5jF+obSacqFfXchjOXaie7+p" +
                "WrajpCcfJB6vjuex0epSHkutjvD4kRDjsjy+PJ7FmZWGxxiTis" +
                "cnJ/NYfBTHY8K4k8G4HI+xLPS5DI/FH8QfI7Ut1+njxh47V+bm" +
                "GXvsvWnssa6lHuRbOlcYe5w9tdAeEy41hfaYSkaX2GMXbbXCJN" +
                "hjt5vCeFAZe/xuMcYYXehzFWNs9y60x3gsqFf6XDH2WGLstI/n" +
                "cRbF9lilyfa4IcQ4ao8lxu7L8fY48LD3832uJu2xwhi/xoQiHv" +
                "8m5LGPseYxYexMynUyGDuEl+QxvZ1sM2HsXCt5jONq+kseG4yl" +
                "X12IsW63xphyEYyjWk3fzmQfY3V9H/VLGIc8jmKcOyAe4+Ba6X" +
                "NNiWLsx0AiGO+XBmNRH4+xtMdpMcYqwviMZIzFR3EYR1SlXUqM" +
                "3zYxEMnjCMZ3RbT6ZtLq32ZdMZswfgIH5jqTPe6K1UqrZxitpn" +
                "uUVlttubZuUa1W6fgEL0VhbLTaxzheqw3GyVqt0hitLrqyiMeF" +
                "Wp3G5xKzEuuuTKsb47VavWGsVkcxrlyrsxGbBR294TWCYb2z0/" +
                "kSa8Tf8Cw24Dl+MP8KnsFaPE/toG+dd+e96FfzjrfhtVxQ6vGO" +
                "nNrKSdv5Idz3ZuqE+iq5y7MqbRtEBnrgafVeXUt7Tdhxfcm75l" +
                "NurCEO46jPlXhn96JR9tqgTbPyVdmsCWXiGBOLWv1UUb90kz5X" +
                "ivcP4lycRoYIIirYqOsJtDrriV2+VvOJYbxadJE85g3uRtTzS3" +
                "x7LHks7XGpVuN8UVdeqw3Gfrxaa/U5vlY7I+K0Gi8brZbx6lIe" +
                "J2u1tmq9S7UaB9F+cJxWy3i14XF5rY7yOEGrHdLqKfFa7fM4qt" +
                "UmXl2IcbJW+/FqqdV8UqjVWSei1dN8reYzBeM3Ojv5DXiTX4PX" +
                "8Dq/lqtIKLbS2asljym9KZZp18dw8qJEjdtSGY9bshkemzhX0s" +
                "Y/S+Kxu3+FT3yl4OitoMapFdTxaimPm7jj7OJ4NZW9EeT+U6dv" +
                "Gx7jHb5c85g8z5DHqi8e11cnxLlCn0tMS8/jYDxyYJzPFe9Xp+" +
                "GxsccJPH43jc/l1rXc51J+9R3JPKZcWZ+rPI9Dn8vy/er3Svzq" +
                "930eU85gTKMxM3YqwfiJ1BhPT8ZYxrniMA7HTnsE4w9TYdyvOh" +
                "jbh7c+xtE4VwHGH/kYZ3pG6pytSnoQpq8Uadqr/N/5Zv6CPtrE" +
                "f1epevKXks9l6lqmzMU+Fz4u73OJDXvO57KPbP69fGp0bjGNz1" +
                "XUD3ouEJ/h99iG7fhv/Mndik/xB/yRat+aBmOxsTKM8V8JbYyM" +
                "nZq7lca5gjOipC1TEr3gqmPsPNQyjFN949MSMP5cf2f9jFZn7s" +
                "AObY8/DbWaWHYnPW17nFaLNyuLgUitDsqj9njeHrXHf06l1W9U" +
                "R6udR5qv1Xx6Oq22+ydo9Rc6BtIOf8Xf8b/YCTUvK8gj9M60Mp" +
                "Fe25XEY35VdbTamby3/OpkHjftV+MvqXj8aAt4PD2lPeif0ML/" +
                "0elu727tc30pCGV0cLk+o+ad9LsfpPB/FvmkeSezRiCOxzpXME" +
                "KK8liOndCz4GzivFOTPT8xDuPCeacCHs+O47Gv1Qk8rmjeyXks" +
                "ft4pnsdy3imyOsVKXiMQ5TGfFD/vxICAhe7R3oP8YXqvXUrdiy" +
                "Iz6C/nJFQEckm+VTYvpc2S67kq4XG4DqRyHjdtj2UMJAWPV7WA" +
                "x8vKPn9Gky28TtvrS9whfAmbJDEuY9Xr8624VcPnqjbGlcZAEj" +
                "F+srUwbtrnYpFYqfG5VF7NT2OW0WrRxfuUXcrUKg42OWk9V6jV" +
                "7lnltTo2BjLvH0+rJcZV0ep1zddqMz5uSquL2hL6XHo9V3YOK/" +
                "KccG3BWLJLqNVpNnd4+fNxscy04+M9qdXW9ipp9bPN57HoVh2t" +
                "5jVg1r3ZR6yfs+kyzsV+SGMnOe+k1oEUzjt5S+R6Luue4nknNP" +
                "jzThjP69wRpfNOcuzEe6hc2ziMo/Y4nFuMm3eqbG4xHDvp0grW" +
                "cyVrdWXruZznk+cWY+oeGIdxufVcukyPnXhBq8P1XEars5sptx" +
                "AdmGKLWc9FPJ6PRdaCptZzBVrd+P9DqyWPq6LVm/aiVi82sUw2" +
                "w8fYWkzqdh065AhF68HQHlPp9WZ87GMsx8eJGI+Mx5i/VAbjOm" +
                "vhPxrGfEGVMP5w72HMbvAxDnmsLNhM6DWBIY/ZjfTGvdhN+Xzu" +
                "JDbLnz/mQmLMO8ZgPEpibOk5DN42DY+lVpdiHGr1P7XP9cmexF" +
                "itESjhsbTHdKRHTmSPb43aY3ab9LmkPcYKvq9e67O6rD0+O9ke" +
                "y7U+oonVHeXssVzrk9nRQnuccq1P5vOm7HG6tT7O9tayx1jWpD" +
                "1eHvpcWqs3sjk6Xq0wNnOL7C7jc1nPqL9bXO/z2McYpxiMZbya" +
                "MB6j/m6RMLY2GB77GCfPH6vjknh1HI8x1LNbGq9md6aJV3usOv" +
                "Fq0b715xZ9jNlcH+OIVs8LeWwwNvPHhRj7fjW7OyXG5wYYv1CC" +
                "8ZyWYxwzJzG3MoyLtDppTmL/KmHceU9hbHhcuvY2IcLylXB8rI" +
                "57sZ/xABNfq633pT321+zRr16z544t1WqV65E8Pk5oQ+or986c" +
                "RMp4wa7WGh/H9llBq9lPg/4otMfz5d87SR7n5hh7LMZLe2zW3o" +
                "rRPsa+z+XbY333eOsTd1yIsRgb2mOVNmmP/b93ao49jvt7J32m" +
                "sTn22LuhOvY447SuPRYjy9hjHUe1QXgHf2HGFmIDu1+Ms7Psvp" +
                "pbzNpbwpjGTrbrzy0SxmXWgbiRNdbmb9r02OnppnksmowdeQnr" +
                "+0oxDs40NofH3szq8DhzbMon/z1+7W2TXC/xO0vX3rIldhust2" +
                "vsf2EP0NFiW/5t7IxoLFMhlHpdpnte4vf/dGVandBnOyrV6kq2" +
                "EONkv7rCObUZ6a9tDsYxtQQYs19oHss1kespPQhrKOevoad24R" +
                "m2zPC4IozPb12MK7fHzcO46Xh1ui17/d7D2PCY15gYCPuVmXeS" +
                "fnU0zhX+nQRbjsH2V0v86pt9v1r5XBcYv7owBiL96qQYSNSvrn" +
                "4MRB3vtRhIdubei4GwFb5fbffiJ9K571GJ+m8fLJjvxNaIVg8q" +
                "YvDARHt8YWLvb/ln4nG11tDnBlRQRzPX0BeVlayhD9dXs3Xh+m" +
                "q7vmh9tYlXD4qNV0fW7LkXxa/Zk/HqysbHcfFqDPXWVzo+plwz" +
                "xsfeM9UZH9O+B8fHRXMSeg09/g+236Wu");
            
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
            final int compressedBytes = 2082;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW3lsFFUY/+bNm9l2t9QiUBEIEaSUU2MJRyOgoEGoHOFQoq" +
                "KigKIUuZECrRwSOQpEORSoAbQ0eMRE//BPvBJUWoQKXolRTgm3" +
                "JBrjkdRv3ryZfbMzs0dnO93ddpJ57817M2/3vd/+vmu+bWgwDu" +
                "mLBodD7aaVdFhDnEfg+ejjavcGHw5Y1rjn5E/NdXRqaPZDvd3b" +
                "89Lnek1yQAICQQhJh3Fvbob20IXcRrpCdyiA/nCHhjHpQXpj2R" +
                "HHh8FwOpa0IyqMItmkA+kM40kXmER6kXyYDjNJYWAOzIG5JECC" +
                "bPZceBGWQjm8RAqA4vO5BsZwC3S0oNKT131ZeRcMIOavAYrxvA" +
                "fux3I0u57MykfhMVbvsMwzz8AYVpp9OdCO1R0gHwTsANcGhdAb" +
                "+ogYwyA8h2bXYjkS7jPvfRDG8tZEmGr2Pg0zWP0szIYXYAG2Fu" +
                "G5AiqwlPFU8cyCbH53G8hjdVvozHsQRehhztYPimCgiDHcjb33" +
                "wggsx+BZAuNgAkyBh+Bhdv8j8DiWT+E5C57DspTg6mA+LMRyCZ" +
                "7noIwxdBZckY7AJalWOsqeuwbXsfcZK4/JSVJHjpFa/rs4Qr52" +
                "5XGpcz+px7kveudxsI0/PA6G/OUsndkEPK7je/+K9I10UjpB1k" +
                "nH6UKpXvqWrCJrTGQqOMbrHXF72QHjua2yOmVk9TFTVl+RvkMe" +
                "/wA3aIX0vcZjTVaLPA7LavaLK9dkNV4zWY11FzxRVrPZCgPzWW" +
                "3KavP3UJAoj4njncEnfOLxtHTEmHRy4jHdgu0f+ZyVrjbX1rht" +
                "rgWZwWN1Qwbw+KdIm4tuk0452Vx0h8Zj+rppc22PanMtygybK3" +
                "g4A2yuP3SbS8NYuiCdR9TPanY1nhxjbPWPlNWAPhR9I1JW6xjj" +
                "qIbxYrus1jFmrdxYPNYxjpTVZD8fdcDYjccixrzugKs8F3FnoR" +
                "OPYSjZ6zr3RKHNMeZXJsbsSua9JsbYzuN1Z5e5i5x4bGDMr6Y4" +
                "PCdgLPD4N15fxrF/pEvwr2Dj7RJlNfznZnM5yuolmSGrg6djfs" +
                "afKS+rL3I8q5DH16Ur2NpH9+g8pnvpO2Ee0wP0TfqudI3upweB" +
                "x0LobstqJ/Ea7f/AUotHUG3wmNYkymPHUd94HDybjjyOwPiqoY" +
                "/p+zwG8ju7uz32Mbsa9fGrkTEQ6Qbq4/ds+niTro+ZXb3Mro91" +
                "u1rTx2Eeu+ljfuWij+Ng8DwnHuv6GOt8S6+mjyud9DG2prnq44" +
                "1hfWyObtX0sXBdwWvVchf37aGt2ROhj/F00MfmONPHWNv0sWhX" +
                "h/WxBFwfDySDyGBSTAZge6ibXU0iYpmkyFVWlzWtrA5ejcuHGO" +
                "JZVl9Of7va3I2xpISMIeNYe0ISMF6eCvrYO8aZEAMxNeYFYWcO" +
                "mb2nGx3LXOEey0wnjIN/Rd21s+mEsRIQVphj9ppRYbmNgbES0j" +
                "GWc6NhHDF7diTG/hyZwWM5L0nzdBXat7nuGWKMiN0Um8epcXjH" +
                "OCSlAMbdksTj9javqlTkMfrHd1p5HENWr4zOYy+yGlb76B8fjv" +
                "kZTzavrIY1Mb/hWsN3wjbzndgai2P4ThtguJIf1XcqzwzfKdQn" +
                "A3yndbrvpHQ14tXsKRavxt7+Rrwa1z0qbHNp8WqdxzCKf+54Iw" +
                "aixatZjRgL6zLi1fVAYTuY76Fix6uFsWaIV4f6NX+8OrAq0Xg1" +
                "60WMlV4M451GvFopsWkzy7tFebT13aIyJuq7xUp7vFrncaKymn" +
                "iR6i30/bE1Xi0/wBFbrpRJbync45GnOPvHysp4PyWw03XfP/HP" +
                "d0pPjOFQJMYajxOe5TPTsqrhGJeL+liPZeo2V1gfh20ueYajrN" +
                "6ky2q2NzWirFayTX1cn6g+dpLVqCcn+6OPtThX+uljQ1ZzffwR" +
                "18dblM06xvLswAcGxuxehjH5md3FcwSkDzWM2agLxk76mLeo5Z" +
                "s0FuOp3jEmp9IDY3ImORijPt4tl7nrY3aN+jhrMelIGSrKrmj6" +
                "mD/hqo8T0C0tXh/TW73pYyMPRKlSNms5e1lHpePYWy9bNC8tpX" +
                "NFHifhe89rit3Iqku6f5wC7yToAo8xlBWGf6wc0DBW3tYwllfj" +
                "fp1w4rFgV1e38tifIyB7tKvNdwdwRV4Dl+S1Yn51+MhmWCkHhS" +
                "dzG73zF72vO7TYpzhXCvDYa7zazK/Owbl4nnT2MLs+1myuCB5/" +
                "nO48NmyuVOcxOZMcfazl7PH1bVe+tOfsKUW2nL3yaDl7ThiLOX" +
                "sx0ekbDWPvuT7yDivGbrk+7hj7l+tjYOw1Zw9HhDcsOsbKMSw5" +
                "xvzT1su7kOfTwfG/bWI+l+M4YsxbCWDsODo6tMc7xpY73fK5Lj" +
                "c/xoZd7TWfi1aZM+5TjjNN1I7uVYS3vXrOHtb7qamVrTl7USzD" +
                "arNVQzf5GDHy3eaiG1PB5or4TpWmXf2r8hrXtNvs+tgqq1nPL6" +
                "mvj6O/P24p+tjEusqIZSKPz2txruA5LffWiHNZeazHMjUexxfn" +
                "otVGnIvWJCfOFZPBSYplahg3d5wrICcnzsXkNvrHWHL/2PH3cZ" +
                "LUqQXx5Ai4/sKSlOsT+qrl+E6efS8ht06x5aepPe0Yx5ez1+QY" +
                "17ZiHLd/zH0ntS8pQQRY7q3axxWhuHNvU+PIjJy9pjhUHktWB9" +
                "v2bH16rSQzcuiTths5AsZL7HY1u0a7Wi027Gp1SLrb1ZnLYxLz" +
                "W6tlzhhjj+A7YcvEmF0hxuoIvzAO/d3K40QxFnkcGGTHOIByWx" +
                "0pYizntvLYJ7s4LzkYw/9VCIk0");
            
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
            final int compressedBytes = 932;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk1o1EAUx4dn9mW3u1WkVIuW4kfVVvCgXryo4AdoP7FotQ" +
                "dBb9KqlIq1IlSpWkVFkIoVxaO19ujFqze1rV9QxZN4VjzryXU2" +
                "m81Oskk2yba7nfE9SOYjM6HJb/9v5r0UUmnL9BPZEtZAQ1owaI" +
                "Rmfq7DfZkW7ocaQN5OQC2s5mU9P5pghTFykzlDhyqjXGrdY0M6" +
                "pME6t95UU6C5O9KRbMmrXK3qZ1pCg1XFRuh3rbENjrnNtpbBmJ" +
                "e1xrk+fwUPeDGO8Pe6M95CjEti/Dg6Y2wpk44PEuMojPUf2TI2" +
                "5scYW/0Yl8tXB5xLjAXDds74l52xcz3OMoY6O+PMepxjXMb1uI" +
                "sYl+Kr46MW+Y5o67E1YuHW425iHJYx8H01A34kOePJ4jpmu0Qd" +
                "4yHW4dQx62NnRMZsMKtjpol3ZCtZna290dbayrbnGccneM9uk/" +
                "HxIM8af25vsxq3UWytcW5mm90Zsz1srzW2lbVZ9aMu9zplaw2b" +
                "Jdp6q81yudWzPgxBxvc8rJMfR8x2j3E+aWfM+tkAP5/nb2GKXf" +
                "S+G3bNv47jc/Oi495Ab2OIdMzf+OfCEYlzJuHDBXNvSvas/6mv" +
                "9ngbKTyGhva1sappN1+N3dCIPdp4bs+l3a9oDuQSMQ6/50K+km" +
                "gpbRl/ri+8xtcKGAmmY7imro5VMki59HnkMs3fRG80HWu3yxcf" +
                "l74ehzXt1uLTsXbHJNYHfLcG7Zl68qvn3J2O9jbV12OVDE9DCw" +
                "NoF2MnP8aZ2ElkzDoKNMRjJ1t70CxDxk7C/lCInYJZ6bGTcUW6" +
                "2Mloe8ZOKHBZmPU4bOxUipXfVy9Gy8VOeDbvq318n2S+Oqop6q" +
                "v7ibHy++o2aMGBLGPopD2XkjoeJB2rztiD/AUH0zmYhQ8wY7am" +
                "4W1oZX0ixhWjOUQ6Vl/HeJkYK894JJevzpga+Wpi7GB8lXT8/+" +
                "258HowHRNjSeJj4fux1Wf/fjy6mL4fE2Pfd+b5/1wFff7fFu8R" +
                "Y7kYB/TeD8hXS+2rH+HDYjrGJ4KOx0nHsvlqfIqT+CyEr54gxr" +
                "L5apyi2EltX40v3Xpj1Q6mczAbS4bPV8cS1h0oX13J9fgjvsFp" +
                "5NTwPb7G2aK+eoZ8dUXU+C6Kr8YX/PhuXhXyHvlcJgz75UAol6" +
                "mUR/9NsZPMvjpX88xz/cn4aspzSRw73cj7avxr1K6Qr1ZVx0Hj" +
                "Yz1BOpYtB6InwzGOfSPGsuVAgpheTXsueY39A8DwAFc=");
            
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
            final int compressedBytes = 497;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmktLAlEUx+OkcygkKLHCRCqUsmX7Nr2zWrQIatOHaNM3KC" +
                "GiyECNoDe9VkEfoG0pReD3mW6TiM74aCSie+Z/wLmemXtVzu+e" +
                "/71nRgqYDqNBilb5MUqoY/+3x90UJEP5HRSiAdVG1GuUeq2eI6" +
                "URTJ1W21X+jLjp0mjY/HNrfza1Ngr/tCf3OMamTE+Y7ozrkA9w" +
                "0F0e+x6Qx3rlMbnX6j4wFs94CIzFM47LZaz9yht20Tdq8xNVns" +
                "VYtSHrGKnYrY3VY9zC7wXjX2LMk/695mP9+4if1vvqKXda7c9B" +
                "qzXL42nksadmwWOJ+4zjyhOiI8N4ti59MNZ7PV7iJC/wPC8abz" +
                "xnFJqtx0Ye67GE2smR4SuIn8b6vEFJxX+5yfyYsPnjiJxOWl3j" +
                "XOP7XJvQaglazVu2sUUq0DvlS94rvbj+9g8Q+Ofzo8jbFV4LjG" +
                "EaMN4BY+mMq5Q8BcYCdtu7WI+9l8dUUOQPoNVisvjQwTiFqOhc" +
                "HxtrxurXO1+6dn3MaYoZ675M+T97R6iP9aqPOYP7XOJ1OQvG4h" +
                "nnwFg842MwFs/4BIzFMz4FY/GMz8BYfn1c41zj58fnqI81q48v" +
                "kMfitfoSjD1K/srGFM+ddKZ5jTwWz/gGjMUzvgVj8YzvwFg843" +
                "swlm5tn/KFwyY=");
            
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
            final int compressedBytes = 555;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm89LVFEUx+P45t6TTEqEEaYSYmRbMZuNyxgdFSkiEQT9By" +
                "zcqWRSuBAqWhS4adWmhQvFjSkuZBTsh83fkFt37tw9r2908Dlv" +
                "nj5B8Jy+B2be3Mu9b4bz4Xvu997h2XnK+T71+bFBnafabT5CWd" +
                "gF5EAZ0cUyHe8iK3KD0nbZ5my37bI9pmCzZtv13aOm0JgWeuje" +
                "7xRb5g/dIuPa16mO7rprg3u10u1g5IOjGZaqg2tN6R73E/+yZt" +
                "BJnLP6Cox/mJ/mt/nl6BXMFhhf1TB/L8LYLAWMV8pGxjJOdYOx" +
                "NB1H9MUytqtgLK5Wr0HH6hmvw3OhVp+q1XkwFqfjDfhq7b46sY" +
                "7/gbH6Wr0DxuoZ74Gxesb7YKyesQ/Gchlz+jyMvSkwlsU4KvhG" +
                "2dxZ5E8w+TS34ZwL63GYMbeDsTTG/Nhx6wgUWuBH0PFVDc5chP" +
                "HxORc/SaLjqq9grL5WZ8FYFmPuSn08e27qE/In2lc/52f8Il7H" +
                "PHBCx0+hYwX740Hsj3XpOPF6PAQdS/VcPHwW46p84Ks3wVhBrR" +
                "5BrVZFvpdy/LL4TBv1Vxwl7Jk2ykjjwKOX/g2vvNriJ++my9C7" +
                "8+mYZqARqcFjqNXK9seTRwr+HO25eOLQc3lzJV89Ds8l33NFKP" +
                "s18qdhf1xRx1NhHXtfoGNZOuY3PB2QazwxEp7rf3Rhb5ED9Yzh" +
                "q4Wvx/zBfA/1xZ9Xv8d6LGw9/ob/j7XHtQNVY7GG");
            
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
            final int compressedBytes = 582;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm80rhEEcxzXNzjMjJCIhSYSrUi7KzdvKSXFxUDhKcRF3Nw" +
                "dKiZbyFjc3kav3/Qu8nCRvuUgk1uPBsHbtPs+W7O95vr/aZ56Z" +
                "Zp5tf5/5/n7zTLMsTa6LZbEU+masmBWF1UtZpXnNe6+JRZbNhF" +
                "lXLIcVmGWh+alguVbP8o8RBku1ygz9jLKQQ2MlIZhTn+VHbfWz" +
                "JrnJWqz71l/H1v6oVyX5b62hRkdu/KE30qK0xdSx3IKOaenY5i" +
                "zbgf8Ik9c65uPRdSz33nTMJz91zCegY2L5OD7jIBhTZyyPNc0j" +
                "O/mYj4AxOcanDtdcJ2BMNVbbZnwGxlQZy/NYjJGPPaXjCzB2Pe" +
                "NLMHbdu9MVYjX5dfW1Mx3zVTCmxZhPa73e6LZAWI8FPsNXzHJO" +
                "3uq2KXvfyef13SIIJJfJux/zozKsZunYLHOsa2GU+RSh4wTmJH" +
                "T8f2uue8RqWox/6VuUmI7lA3RMhbF8RKz2dKx+Qqx2g44jdP0M" +
                "/xEmH38P5AV7IJ6L1SEwpspYpcRiDB27PR8rAf9RzseiXbTFys" +
                "fKYKWiAzr2Uj5W6WBMl7HKtMMYZ/Y8oOMsMKa/5lLVEWNH4T83" +
                "7IHY1nEddEwtViu/0WQ0Gg1Gswga9eIg7n9T98GYHOMWsSP2xK" +
                "5JLyi2wThZTRwmwlis2dmvVp3Y56KtY9+Q6oo/1jcM/7ndVDd8" +
                "QNe+zmXyWdXzcRdQvd966HOZfFm34VwmLY322YjVY/CTp96P+7" +
                "HmIvfuNOiQ8QAY02Kc8go+uUP2");
            
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
            final int compressedBytes = 548;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7tLw1AUxvWSJjlBFFGLtKWKVFpRJ1H8D3xNOiiCq7Orgy" +
                "7i0EUcFAqim5ubk4uIgw98dFM3H5ODq4qKEtNQJZKkacSh5/pd" +
                "aEMu94bm/Pi+e+7hlmYjS2ZgiyybaGybqKE5V1+rSP64T4kO67" +
                "u5yHtTNAjVuifRJOLWNWF9MiJqj0wXZ2jCsK+1389oD/3L2kAn" +
                "dMxi3ow9+koypnkw5saY1rRhbUgb1EbUvDagngUxVk/BmB3jdd" +
                "PU+2x6eb0XjCu16f2/YaxuO71aWfH2atopMFZyX4yVVTCWfT3W" +
                "pyqPMe2CbzmMfXW8Bx3zZ0z7tj7v/XWsToIxX8a2Ug8CvfAQ8e" +
                "NcAwm9Pz6Cjvnp2PLpGaWu6MT11sgF19ys5xMXK+Xd6Bh8y9Gx" +
                "b851gpxLBh1bJM8dI9npGO0PvDCPGEjP+AIx4Lwe02VwXq1uOO" +
                "rVOazH/GogIfdOV2DMkTHdhGB8DcYsGd+FYHwLxjz3TgE51yPH" +
                "N66eBnWv9ZieA3X8BB1z82p6cxB+KcOrX8GYb15NH/SOsz6y75" +
                "3IhI7lYkwPBcaG0Mf08dJ5tT7hOOszCsYy5tVGI+LHOa82oq6+" +
                "0v+TQA2E9XpcDmMjBsZcGfudETDiOCPAnbGRCKdjZQuMpffqJB" +
                "hLz7gFjKVnnAJj6RmnwZh/DcTIuOZmET/ONZDQOu6EjqX36i4w" +
                "5u/VHu7djfj9K6/ugY556bjqE499JLA=");
            
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
            final int compressedBytes = 514;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm88uA1EUxuWmc809ERsh4l9ECPbYiIWFBxAJC0srL8DCRs" +
                "RChFiQeAPsRC1sGolEQmk9AiVBYsFKd1KjabV02rljo+f0O0k7" +
                "nclM05xfvu+cc2eayZQGDf0+otYyCOFBI8gB31ANPse6VdeP/V" +
                "416L235niPqialvX2jmlW7t+3wXgOqJXtmf+6KekXZbeP3d/SF" +
                "/mU9oBM6Z23+jGksHGPnFox5MaZxZzP4WmcL+ZPj1SYdpGPzDh" +
                "3z0rFlzzWB/HHWMU3pA71vX4/1HnTMrefKf4ps+zOm2S/Gkd08" +
                "48gOGHNlbD07zYExu9lpXl/qKx33XPhGX+hEoFdfg/F/hE7+hb" +
                "GOFjnyQtGZKyXX+q5lqlVkXk7QInIgZz62qMdL8GrxPdcyGPNf" +
                "A6F1u3qMYFx9N8BYcj3GerXsemw+3GHMx7VRj33c+xD54+3VdO" +
                "ROV+6r3ZmCjt1J6JijV1PUfnYCY771uOx9p2PcdxLP+ASMBa6B" +
                "xDAfy5yPy+r4FDoWqOMz6Fimjq376kfomGvP5T5XYlzwajoHY1" +
                "6MKZ6rsp1FZ+I5EIFebe7svZqqcL2aEiAZ5NXm3p6xSVUh4yT4" +
                "huurfXKYQv5qqa+mB/Rc3LyankLOTvhvKtvZyVrHL2AsnvErGP" +
                "PvuejNbj5GiO258MyefK9OgzEvxnWf2rXTfg==");
            
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
            final int compressedBytes = 436;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms9KAlEUxuVwnet4ok0UUmISRrVpV6+QFrQqiGhT24JWQf" +
                "+ENpWLdgVtfIGQniTxEXqLgtpMk1mgjuiFNufc78A4f5gROT+/" +
                "c77D3Cjqj+xH7xWqRQixQWMJ14pU6Dov0VL8mev8Az5pgoL4PK" +
                "RJmon3+XhbpKn2nQudJyxl2/vxv++Yd/5lc6DjnLPp/2HMKTCW" +
                "ytjcJzPm3Ddj8/jL2DyAsSzGScF59GNd/ZgLdt1WbNluBC27Fj" +
                "SH1ergBTqWVqt51q0fpytgrN5zFcFYnI5LqNXQcY+Ol8FYga9e" +
                "ga/WHrwKxspmpzL6sX+1OkHZm8ifZB07e64t6Fi9r94GYwW+eg" +
                "eey/NavQsdK9DxHnSsbHba/zka9P44c9b9/jhzCh2rnJ0OkD+v" +
                "+vEhdCx3duKjURibKhjLno/D92GMwzcwVtmPj5E/r/rxCXQsu1" +
                "aPwPgcjNUzvgBjqYwHrqG/xBp66Yy56qZj0wBjlb76Cvnzyldf" +
                "Q8fqPdcNGKvzXLfwXOI9Vw2eSz3jOzfG6Vcwlu+rud73LNaBqA" +
                "9+Qg7UM24gB17Nx8/ox7L6ceoLPQ+kIg==");
            
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
            final int compressedBytes = 378;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2EtKA0EUheFwabS1CicxGlREgqLO3YFjFRXMBhwojlyLgo" +
                "twB+5BENGRD/A18LEDB20bNShG6RrWqb8gaSokGdyPU/d2my9+" +
                "LZuyyR/7aZsv35sfO3dkdesr9wPWsPHyOlG+5myk883Zz1/022" +
                "DnOtT9j5kicFmrYIXWbKznp13jbK+3sTt+N84OvoyzfYzljE8w" +
                "VjGuelbnDxjHZVxtuVPqF7F8+Mx1Ro7Vz2p3jnFsxu4iX8s3/j" +
                "fO29/68SrG8jm+xFje+ApjeeNrjOWNbzCWN77FOFbjP59l3vEs" +
                "M/p7p/uwHGeHGMdlXG25R+oXsXx4P34ix3L9+Jl+HH0/fqEfJ5" +
                "/jV3KcxMxF+eSXr1GDlOZqX+esjrUfVzYexlht5vINZq7kcjyK" +
                "sVyOm+Q4uRy3MJbL8QI5TuEZiF+kfkndHy+RY/l+vIyxvPEKxv" +
                "LG6xjLG7cxlpyrN6lfUnP1FjmWP6u3MZY33sFY3ngX47iMa288" +
                "m7Qd");
            
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
                "eNpj4vmPAZgUmORQ+CpMmkBSAsLjKWESZmID8jmZRJmkgbQMEG" +
                "swiYFVqkN1sDNxgWk+uBmq/0kETEr/RwGpYSaFVZT0OC4djeNh" +
                "H8cVo3E87OO4dTSOh30ct43G8bCP447ROB72cdw9GsdDK44ZAM" +
                "UfD7Q=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 17, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 110;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3cEJACEMBMD0X7OgHfgQw+Yx8xfkXDYgB1YBAAAAAAAAAA" +
                "DMtK9W+/r0/un9/s5HfwCgv5Gvn/mSX9AfIH8AYL4CAAAAwHTu" +
                "xwD0IwCY3wCA+Q8AAAAAAAAAAAAAAO/S75/4vz7sAJROSpk=");
            
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
                "AAAIBfelUCAfgzAAAAAAAAAAAAAAAAAAAAAADAzQD3Dj29");
            
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
