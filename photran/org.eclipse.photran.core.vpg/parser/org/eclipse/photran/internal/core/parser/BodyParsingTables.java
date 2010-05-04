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
final class BodyParsingTables extends ParsingTables
{
    private static BodyParsingTables instance = null;

    public static BodyParsingTables getInstance()
    {
        if (instance == null)
            instance = new BodyParsingTables();
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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 1, 47, 30, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 16, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 0, 3, 13, 4, 106, 47, 155, 156, 5, 157, 158, 0, 6, 26, 162, 159, 178, 7, 8, 160, 161, 0, 163, 168, 179, 169, 180, 171, 9, 10, 97, 181, 182, 183, 11, 172, 184, 47, 12, 185, 13, 186, 187, 188, 189, 190, 191, 192, 47, 47, 14, 193, 194, 15, 0, 195, 16, 196, 197, 198, 199, 17, 200, 18, 19, 201, 202, 0, 20, 21, 203, 1, 204, 205, 74, 2, 22, 206, 207, 208, 209, 210, 23, 24, 25, 27, 211, 212, 178, 180, 213, 214, 28, 215, 26, 74, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 74, 226, 29, 227, 228, 229, 230, 231, 232, 233, 234, 235, 86, 105, 236, 30, 237, 238, 31, 239, 240, 241, 3, 242, 32, 243, 244, 245, 0, 1, 2, 246, 247, 248, 47, 33, 249, 250, 86, 251, 184, 179, 185, 149, 13, 186, 187, 188, 189, 190, 252, 191, 192, 253, 181, 4, 5, 95, 6, 254, 255, 34, 35, 256, 105, 257, 193, 258, 259, 260, 194, 105, 261, 262, 106, 107, 108, 112, 263, 115, 120, 122, 264, 182, 196, 265, 266, 267, 197, 199, 268, 269, 106, 270, 271, 272, 273, 7, 274, 8, 275, 276, 9, 10, 277, 0, 11, 36, 37, 38, 12, 1, 0, 13, 14, 15, 16, 17, 2, 278, 18, 19, 3, 13, 4, 20, 279, 21, 280, 1, 39, 23, 24, 29, 281, 282, 31, 25, 32, 283, 34, 284, 285, 35, 40, 37, 41, 42, 43, 286, 44, 45, 46, 47, 48, 287, 288, 49, 50, 51, 289, 290, 291, 52, 53, 54, 41, 30, 42, 33, 177, 43, 44, 55, 56, 57, 292, 293, 0, 294, 58, 59, 60, 61, 62, 295, 63, 64, 296, 297, 298, 65, 66, 299, 67, 68, 69, 70, 71, 5, 72, 73, 300, 74, 75, 76, 77, 78, 79, 7, 301, 302, 80, 303, 0, 81, 304, 82, 83, 84, 85, 87, 305, 88, 89, 306, 90, 91, 92, 93, 307, 94, 95, 96, 308, 98, 309, 99, 100, 101, 102, 8, 310, 311, 312, 313, 103, 9, 314, 315, 316, 317, 318, 319, 320, 321, 104, 105, 107, 10, 108, 109, 322, 110, 11, 111, 112, 113, 323, 324, 114, 115, 116, 0, 325, 117, 12, 118, 119, 120, 13, 45, 326, 13, 327, 121, 122, 328, 123, 14, 124, 125, 15, 126, 127, 329, 153, 330, 16, 128, 129, 130, 21, 131, 16, 132, 331, 30, 133, 134, 332, 17, 333, 334, 335, 135, 3, 336, 4, 46, 136, 337, 137, 5, 338, 6, 138, 139, 339, 340, 341, 140, 18, 342, 343, 344, 345, 141, 142, 47, 0, 143, 144, 145, 146, 147, 346, 148, 19, 48, 49, 347, 348, 349, 350, 351, 149, 150, 20, 151, 152, 352, 153, 50, 99, 154, 155, 353, 354, 355, 1, 356, 357, 358, 359, 360, 361, 155, 362, 363, 156, 157, 161, 163, 22, 13, 158, 364, 365, 366, 367, 368, 369, 160, 51, 370, 371, 164, 372, 373, 374, 165, 375, 376, 377, 378, 166, 379, 2, 380, 381, 106, 167, 382, 383, 384, 385, 386, 168, 387, 388, 389, 390, 169, 170, 391, 392, 393, 156, 172, 394, 395, 396, 397, 16, 198, 27, 398, 173, 399, 200, 400, 202, 401, 203, 205, 402, 207, 174, 47, 175, 176, 177, 26, 178, 403, 404, 405, 179, 406, 208, 407, 408, 0, 181, 52, 53, 124, 409, 126, 410, 185, 186, 411, 412, 16, 211, 413, 188, 414, 7, 22, 55, 25, 33, 415, 36, 416, 417, 418, 38, 166, 419, 65, 215, 57, 0, 3, 420, 421, 1, 2, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 206, 444, 445, 446, 447, 448, 449, 450, 451, 452, 66, 453, 67, 454, 455, 456, 68, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 36, 38, 55, 474, 475, 476, 477, 478, 195, 479, 480, 202, 481, 210, 80, 482, 483, 484, 485, 486, 487, 81, 488, 82, 86, 92, 212, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 94, 508, 509, 510, 511, 512, 182, 513, 514, 515, 516, 213, 517, 518, 519, 214, 520, 521, 7, 522, 220, 3, 523, 225, 524, 525, 526, 527, 528, 529, 530, 95, 531, 226, 532, 533, 534, 96, 233, 535, 536, 537, 235, 538, 97, 539, 98, 106, 540, 193, 194, 541, 542, 196, 197, 543, 201, 544, 545, 112, 113, 114, 135, 56, 546, 133, 134, 547, 4, 548, 189, 549, 550, 180, 551, 552, 553, 554, 555, 556, 557, 5, 558, 559, 560, 561, 562, 6, 563, 8, 9, 10, 11, 12, 13, 564, 565, 566, 567, 568, 141, 569, 142, 570, 149, 236, 151, 571, 203, 572, 205, 573, 574, 152, 575, 576, 577, 578, 14, 56, 579, 580, 581, 184, 582, 583, 198, 584, 585, 586, 587, 588, 238, 589, 153, 590, 591, 592, 593, 594, 595, 596, 597, 598, 154, 599, 600, 601, 602, 159, 603, 604, 162, 605, 606, 607, 8, 608, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618, 199, 200, 619, 206, 620, 127, 621, 208, 16, 622, 623, 624, 625, 626, 627, 628, 164, 165, 629, 630, 631, 166, 632, 167, 170, 171, 173, 633, 211, 178, 58, 4, 168, 169, 634, 635, 9, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645, 172, 17, 179, 181, 646, 59, 183, 186, 192, 1, 207, 209, 60, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 647, 241, 227, 648, 649, 0, 0, 74, 57, 650, 651, 652, 185, 188, 190, 226, 228, 61, 229, 230, 63, 243, 653, 18, 654, 211, 231, 232, 233, 234, 236, 235, 655, 656, 657, 237, 658, 659, 238, 239, 240, 241, 242, 64, 660, 661, 662, 244, 663, 664, 665, 243, 10, 245, 19, 20, 666, 667, 216, 668, 217, 669, 670, 671, 672, 673, 58, 246, 69, 674, 675, 676, 677, 247, 248, 5, 678, 679, 680, 681, 682, 683, 248, 249, 250, 684, 70, 685, 250, 686, 687, 688, 689, 251, 7, 252, 253, 255, 690, 691, 692, 256, 257, 258, 693, 259, 71, 218, 260, 261, 262, 263, 694, 264, 265, 266, 695, 267, 268, 269, 696, 8, 270, 271, 272, 219, 220, 72, 221, 222, 697, 73, 128, 74, 75, 76, 77, 273, 698, 699, 254, 700, 223, 701, 274, 276, 277, 702, 703, 224, 228, 704, 705, 706, 229, 707, 708, 21, 709, 27, 230, 710, 231, 711, 712, 713, 714, 78, 278, 281, 715, 1, 79, 30, 81, 82, 58, 86, 59, 92, 61, 716, 283, 282, 284, 717, 718, 232, 719, 285, 720, 233, 721, 92, 95, 59, 286, 287, 60, 255, 105, 234, 722, 61, 723, 239, 724, 725, 288, 257, 726, 727, 1, 62, 289, 290, 63, 2, 291, 80, 292, 293, 64, 294, 728, 261, 729, 730, 295, 63, 240, 731, 732, 241, 262, 263, 733, 734, 735, 736, 296, 298, 299, 242, 737, 738, 245, 264, 271, 739, 246, 740, 741, 247, 742, 743, 249, 83, 300, 302, 304, 64, 305, 306, 0, 251, 307, 308, 744, 745, 746, 303, 309, 312, 311, 313, 314, 318, 65, 0, 315, 316, 1, 317, 320, 2, 322, 323, 69, 325, 326, 327, 324, 328, 330, 331, 70, 332, 333, 334, 335, 337, 339, 340, 341, 342, 343, 344, 346, 347, 348, 349, 351, 352, 353, 1, 252, 354, 355, 357, 356, 358, 359, 361, 362, 363, 364, 365, 366, 367, 368, 360, 369, 66, 370, 84, 2, 67, 372, 373, 253, 747, 374, 68, 71, 375, 379, 380, 382, 383, 748, 384, 385, 386, 387, 371, 377, 749, 388, 390, 391, 394, 396, 400, 401, 403, 406, 407, 409, 410, 750, 411, 376, 378, 415, 381, 392, 73, 78, 94, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 389, 393, 395, 398, 402, 405, 408, 412, 413, 414, 416, 2, 751, 417, 418, 419, 420, 421, 422, 752, 423, 753, 754, 404, 755, 756, 424, 425, 757, 426, 427, 758, 254, 0, 759, 428, 760, 430, 761, 429, 762, 110, 763, 764, 765, 256, 258, 431, 432, 259, 433, 260, 434, 435, 766, 767, 436, 437, 438, 439, 440, 441, 260, 768, 442, 443, 444, 445, 446, 447, 448, 449, 106, 450, 69, 451, 769, 770, 111, 452, 453, 454, 3, 261, 455, 456, 457, 458, 4, 460, 771, 461, 459, 262, 263, 462, 463, 464, 465, 466, 772, 467, 773, 468, 469, 470, 471, 472, 473, 474, 5, 264, 475, 476, 477, 478, 479, 480, 481, 483, 484, 485, 486, 487, 488, 482, 774, 265, 775, 776, 70, 491, 115, 492, 499, 116, 489, 490, 493, 494, 777, 778, 779, 495, 780, 496, 497, 498, 500, 781, 509, 782, 783, 266, 510, 512, 513, 784, 785, 786, 787, 267, 501, 502, 503, 504, 505, 3, 117, 118, 520, 788, 516, 1, 789, 790, 4, 522, 523, 119, 85, 11, 506, 791, 507, 508, 6, 792, 793, 107, 71, 794, 795, 517, 524, 525, 796, 268, 797, 798, 269, 526, 799, 270, 3, 800, 801, 271, 511, 514, 515, 518, 87, 519, 802, 530, 521, 527, 528, 529, 531, 532, 88, 533, 535, 267, 534, 536, 274, 537, 803, 538, 539, 541, 804, 805, 544, 806, 807, 540, 808, 542, 12, 809, 810, 811, 812, 543, 813, 120, 546, 545, 547, 814, 548, 549, 121, 122, 123, 815, 272, 816, 550, 551, 275, 817, 552, 89, 818, 819, 820, 821, 273, 274, 90, 276, 822, 823, 553, 554, 824, 4, 825, 826, 827, 828, 829, 91, 830, 125, 831, 832, 833, 555, 834, 5, 835, 836, 557, 837, 838, 94, 7, 839, 840, 841, 126, 842, 843, 844, 845, 277, 846, 95, 96, 847, 848, 278, 849, 279, 558, 556, 559, 850, 851, 852, 853, 560, 854, 855, 128, 856, 0, 857, 858, 859, 129, 97, 98, 101, 130, 131, 860, 132, 139, 140, 143, 861, 862, 102, 863, 72, 864, 865, 277, 866, 561, 562, 564, 565, 566, 567, 568, 279, 867, 144, 868, 869, 5, 569, 570, 73, 145, 571, 572, 103, 108, 74, 75, 870, 871, 573, 574, 109, 575, 872, 280, 281, 293, 576, 577, 578, 282, 283, 579, 873, 294, 874, 284, 875, 295, 285, 296, 876, 580, 877, 581, 582, 878, 583, 879, 584, 585, 586, 587, 588, 589, 598, 880, 289, 881, 290, 291, 882, 883, 884, 76, 590, 885, 886, 591, 887, 888, 889, 890, 891, 0, 892, 893, 894, 895, 896, 603, 897, 593, 146, 898, 899, 592, 604, 900, 901, 606, 902, 608, 903, 594, 595, 596, 597, 904, 599, 600, 601, 602, 905, 612, 906, 613, 605, 907, 908, 909, 607, 609, 6, 7, 610, 611, 614, 615, 910, 295, 911, 912, 913, 292, 616, 914, 297, 915, 300, 916, 917, 617, 618, 918, 919, 920, 619, 104, 620, 621, 622, 623, 624, 2, 921, 922, 923, 110, 77, 625, 626, 627, 629, 78, 632, 924, 634, 635, 925, 637, 926, 927, 79, 636, 928, 301, 638, 929, 930, 639, 931, 932, 933, 934, 935, 936, 937, 938, 939, 302, 640, 940, 941, 942, 943, 645, 648, 944, 650, 945, 651, 147, 652, 641, 946, 947, 948, 644, 653, 949, 0, 950, 951, 952, 148, 8, 150, 654, 646, 953, 655, 151, 647, 649, 954, 656, 304, 955, 657, 658, 661, 956, 155, 156, 957, 305, 300, 958, 659, 959, 662, 960, 663, 961, 962, 664, 665, 666, 963, 964, 667, 965, 966, 157, 967, 1, 968, 969, 668, 669, 672, 673, 670, 105, 9, 671, 674, 13, 970, 675, 10, 971, 972, 973, 974, 306, 975, 676, 158, 976, 307, 977, 308, 677, 978, 678, 979, 310, 679, 313, 314, 980, 322, 161, 163, 680, 80, 681, 981, 982, 983, 984, 985, 986, 682, 987, 683, 988, 684, 323, 685, 328, 686, 989, 687, 106, 990, 991, 11, 688, 689, 691, 992, 696, 699, 993, 700, 994, 701, 690, 329, 692, 107, 995, 996, 12, 997, 703, 694, 330, 998, 331, 999, 698, 1000, 1001, 174, 175, 1002, 176, 1003, 297, 702, 704, 1, 1004, 332, 1005, 1006, 108, 1007, 109, 1008, 333, 1009, 334, 1010, 81, 3, 4, 705, 706, 1011, 111, 82, 335, 1012, 337, 707, 708, 1013, 709, 1014, 177, 710, 1015, 1016, 9, 183, 311, 711, 712, 713, 714, 715, 716, 112, 314, 1017, 339, 110, 1018, 111, 1019, 112, 340, 717, 1020, 315, 341, 1021, 186, 1022, 1023, 718, 1024, 1025, 719, 720, 187, 83, 721, 191, 722, 342, 723, 84, 724, 192, 725, 726, 113, 727, 728, 729, 1026, 730, 731, 732, 1027, 733, 735, 13, 14, 737, 15, 1028, 738, 740, 739, 743, 1029, 747, 741, 16, 750, 17, 1030, 742, 744, 1031, 193, 756, 1032, 1033, 748, 751, 1034, 734, 343, 749, 752, 301, 753, 754, 1035, 1036, 1037, 736, 755, 757, 758, 2, 114, 85, 115, 759, 760, 762, 1038, 1039, 1040, 1041, 1042, 1043, 761, 764, 1044, 765, 766, 1045, 344, 86, 87, 767, 768, 88, 1046, 303, 116, 117, 0, 118, 119, 771, 346, 1047, 1048, 1049, 194, 772, 773, 774, 310, 770, 196, 1050, 316, 775, 777, 1051, 1052, 347, 776, 778, 779, 317, 780, 8, 197, 784, 9, 10, 781, 785, 786, 787, 789, 790, 791, 792, 793, 794, 1053, 795, 782, 1054, 798, 1055, 797, 1056, 120, 799, 800, 1057, 199, 129, 1058, 1059, 1060, 348, 1061, 1062, 1063, 349, 350, 801, 351, 1064, 803, 805, 1065, 121, 1066, 1067, 808, 1068, 18, 123, 352, 1069, 1070, 809, 810, 812, 11, 1071, 1072, 1073, 19, 354, 125, 1074, 813, 814, 1075, 355, 200, 204, 207, 2, 356, 357, 1076, 358, 1077, 1078, 359, 1079, 1080, 126, 1081, 128, 1082, 1083, 1084, 1085, 1086, 318, 208, 802, 1087, 319, 115, 361, 89, 320, 1088, 804, 327, 1089, 328, 806, 815, 816, 818, 820, 821, 819, 824, 1090, 116, 90, 825, 823, 131, 1091, 132, 1092, 1093, 1094, 1095, 137, 1096, 826, 827, 362, 1097, 1098, 1099, 1100, 1101, 1102, 5, 15, 1103, 1104, 1105, 828, 835, 1106, 1107, 829, 839, 841, 1108, 842, 843, 1109, 830, 844, 1110, 846, 363, 10, 850, 11, 12, 1111, 1112, 832, 833, 852, 20, 21, 209, 834, 1113, 215, 1114, 91, 836, 837, 838, 847, 854, 1115, 855, 856, 1116, 848, 849, 840, 1117, 1118, 1119, 321, 12, 216, 217, 1120, 851, 858, 13, 859, 332, 1121, 364, 366, 13, 1122, 14, 1123, 857, 1124, 862, 863, 860, 861, 864, 865, 220, 1125, 376, 866, 1126, 1127, 139, 1128, 867, 15, 1129, 22, 868, 140, 1130, 1131, 1132, 1133, 1134, 381, 869, 16, 1135, 141, 393, 1136, 1137, 1138, 1139, 1140, 397, 872, 1141, 398, 1142, 399, 416, 1143, 1144, 417, 1145, 1146, 1147, 142, 143, 7, 8, 870, 871, 873, 874, 418, 875, 329, 1148, 1149, 419, 876, 14, 877, 333, 1150, 1151, 334, 218, 878, 1152, 92, 1153, 1154, 219, 221, 222, 1155, 1156, 223, 93, 879, 880, 1157, 0, 224, 881, 882, 885, 887, 888, 889, 890, 891, 892, 1158, 893, 894, 895, 1159, 1160, 1161, 1162, 15, 896, 1163, 1164, 883, 884, 886, 1165, 1166, 1167, 898, 902, 904, 1168, 336, 225, 226, 897, 1169, 1170, 899, 900, 901, 903, 905, 337, 1171, 1172, 907, 1173, 908, 1174, 909, 1175, 1176, 420, 132, 1177, 1178, 23, 421, 1179, 1180, 1181, 1182, 422, 423, 906, 424, 1183, 1184, 911, 1185, 1186, 1187, 1188, 427, 428, 912, 430, 1189, 1190, 227, 134, 1191, 1192, 1193, 1194, 1195, 338, 339, 340, 1196, 94, 431, 432, 343, 913, 916, 914, 915, 917, 918, 919, 1197, 228, 229, 434, 433, 435, 230, 1198, 1199, 1200, 144, 920, 921, 1201, 1202, 922, 1203, 923, 1204, 1205, 924, 16, 926, 925, 927, 928, 1206, 929, 930, 1207, 931, 436, 1208, 1209, 345, 934, 437, 932, 937, 935, 938, 939, 438, 1210, 1211, 439, 455, 941, 456, 1212, 1213, 147, 1214, 942, 458, 943, 459, 1215, 1216, 148, 1217, 462, 945, 946, 947, 463, 1218, 346, 349, 1219, 948, 350, 354, 951, 464, 47, 1220, 149, 150, 1221, 1222, 467, 953, 1223, 1, 1, 954, 955, 956, 957, 958, 959, 960, 1224, 1225, 1226, 961, 962, 1227, 963, 357, 964, 1228, 965, 1229, 468, 1230, 1231, 151, 1232, 1233, 24, 1234, 152, 1235, 1236, 26, 135, 363, 365, 367, 469, 470, 967, 368, 1237, 154, 156, 157, 1238, 1239, 1240, 1241, 238, 158, 1242, 968, 1243, 969, 970, 971, 1244, 972, 973, 974, 976, 978, 979, 975, 239, 1245, 1246, 27, 471, 1247, 1248, 29, 472, 1249, 371, 1250, 377, 977, 1251, 1252, 1253, 240, 241, 980, 17, 242, 246, 981, 254, 1254, 982, 1255, 983, 986, 984, 473, 1256, 1257, 474, 475, 1258, 1259, 476, 477, 258, 286, 287, 478, 479, 288, 985, 987, 988, 292, 293, 1260, 481, 1261, 1262, 485, 1263, 378, 480, 483, 484, 1264, 1265, 990, 991, 992, 1266, 1267, 1268, 1269, 1270 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 0, 20, 7, 21, 22, 23, 5, 6, 2, 24, 0, 25, 26, 27, 28, 7, 18, 16, 29, 30, 0, 31, 22, 0, 32, 27, 33, 0, 18, 12, 16, 34, 35, 36, 37, 38, 35, 39, 40, 0, 41, 42, 36, 43, 35, 39, 40, 1, 44, 44, 15, 45, 46, 47, 48, 49, 50, 34, 51, 46, 52, 53, 5, 54, 55, 1, 56, 57, 58, 2, 59, 3, 60, 61, 62, 12, 41, 49, 63, 62, 50, 63, 64, 65, 66, 67, 68, 64, 69, 65, 70, 70, 71, 72, 73, 74, 5, 75, 0, 76, 72, 77, 78, 79, 73, 80, 5, 80, 0, 81, 82, 2, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 16, 85, 89, 95, 96, 97, 36, 95, 98, 99, 96, 100, 99, 6, 35, 2, 101, 0, 102, 46, 103, 101, 3, 104, 16, 103, 105, 106, 107, 108, 109, 0, 15, 110, 111, 112, 105, 113, 114, 115, 51, 116, 7, 117, 7, 118, 119, 120, 121, 122, 123, 124, 125, 0, 126, 1, 127, 36, 128, 129, 124, 130, 0, 131, 132, 0, 133, 134, 107, 135, 136, 137, 114, 2, 138, 117, 139, 140, 141, 6, 142, 3, 143, 120, 0, 40, 121, 144, 145, 125, 3, 9, 146, 39, 0, 147 };

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
            final int compressedBytes = 2956;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb1vXbcV52OuBUZIEUaoE41XgYu+Ah6aKYWXXqlfaRcLRo" +
                "w2KLp0aQePnQIPlBIU8ubBg8cHDway5U8QAg9BJi9FA/81vR/v" +
                "Pt0Pkr9DHvI9qb5AFD8dHfKQPDwfP/Lc98P7dx5++GSpfnrr7O" +
                "6377x4ePnp44Pq6z/+5wPz4Pt75o744X5N/2b5WU1/NKH/SKLX" +
                "7X/0zfIvzxr6b148fPXp4wej9rn0XcvHbR/wC7EQk0cPP3Dbv/" +
                "zJnd/eOqvXX4i73x6f377cWz4QRv2u7qQS12F+CPS6fbf+vu/s" +
                "P4n+vLn/j7999GT512c/++rRiz+8/POrXz/++8m/v/hvTX/z5U" +
                "X++WnWrzhbHj5r1m9xfvvV3vLgRq1f5v1zE/Q3p3xCiUI0P9dP" +
                "/UHL9b+rxn5kHp8cGqtq8LN+5Bb6v/H+Zcf02WPGH9nzc9Pnly" +
                "n/eH8WvX+vhv7d67+u4jcnPat+J1g/NL6c9vlAiFv6yiBJoT/X" +
                "9U/TLIGkxFeU+ORJK9/dHONrRe6DxL36v8PTX3a7tJJS8eVfxx" +
                "ddfLho48Og+OLN/Z+v46OzR6tNfNTSafHRID9ZdeOXW9Xf9PH7" +
                "5FkUvvge0VH8jujfdfQufjxu48cNXSagc/MLbvsh/KuO/yREPv" +
                "Rw7Rs3vu/5Y/dvqv5d/EP6qqPLIR3Fr1z6WP6Xrfwna/m/nI1v" +
                "Tue2P+UXA34RNz7B6X/Wvi3oq8b2h+MfhKoEfgz8C+34Pco/Rj" +
                "Nve0B8lptuW7/x/G1dPhEvv9e/w/g2xv+j+G7toKUoNztgpEqI" +
                "Pzd9OOlrleXKJ0L6Z8evA37b+u3vi9sHqlafW2d1/2UlPvmXrr" +
                "4+frowD57f60Uax7flML6t6e/54l9EJ8TXrPb3/+Qc38W9BPYT" +
                "8Q/pq44+9q+hpnZGp9hvD78UPP7OfxS+PywcLqLw2n/T27cwnz" +
                "STL6Z97V+IhcXzaSXHzRqaq1SVEUVj/jpBlBGHzf+XrUHU6/kt" +
                "r0QtJoNT/Qqqflia5condFkPS+tmxLLbX8+b/a/rv7ps9xf030" +
                "jVsP9H6+cfP+DH42PuL/L+VQ796MZXzsZXEumgfeb+gfM3UnsZ" +
                "YV+S7P/CrV+2/dXvfxU+/kOb/tj2b98+6h/qX4X2nB59OF0bp8" +
                "q+GknsZ8D6IBOpZvwx+lvEyl+vrwy3fzreP5lAftw+sh9iYj9t" +
                "T5HN/kH9Afo/3x96lMIrV//G7s5n/qoK237B/hOND+0/6D94+w" +
                "fS+/hHOOKfirX+BHy34OC7XHz0SP8o691RfKz35OJXQhwZsxCl" +
                "LpVYleI4Mn6o6PhK7vM1hF8O6bb8ZlvyufDNdv5JOu2IT5jx33" +
                "XHNyP4RRh/RbEv0VkAXh89Fntin4LlD1z/74b4wMS+9Oc3PjrG" +
                "Dyp4/sG5X4DGz8afaVnJROs0mc7HbwPXf+b0/P6Ri88bkF9h/x" +
                "mMb4pA/FT48F0L/muLPXu6tPDvlJ4A//bOf8T6BeKDUfpNj49R" +
                "fpQZX6Hbv+H+UWT7x8CfW3wd4e8Wupjg8wgf9+L3DM97pdL+84" +
                "Gs9HD5Pt92/+9xzk/Q/kfnGxa6GNLZ5zfM85Hh/tPW+FsAfFzA" +
                "+Md3vtngI80OP6pbnOEjeyT7JsPt6xDfOl3bcCmHuYgi23/QPu" +
                "w/1vobWvso/w893zKB8xvpSBS5fSpdMvkjx7cU0ojF6hfn/6x/" +
                "9YU5MItnK71Sv1+WhdnbQv+WU7MqoX5B/J2MH8rBCmlJPb/a4D" +
                "915zb850h/39HVmi46upjShZ0uxGeddWh6V80kmj5+Kq8SCbnR" +
                "2Cv7SLSfqP1m/jwIG5QfyIfmB8mP8Tm/fDO6CVufmt7Oz8f1GL" +
                "3tKz+/nPEb0vix/lTe9aXPT9z4evnj9DPF/pmOX8z3D0m+fat8" +
                "iB/NH9p/yL6Q/acm2m+Sf/XG/6P4kH++gLzFJc+/LjvGOv4zha" +
                "n9hpFaNdO7qKf/Q8v5igkcEOS/ZPpf5vjF6zH/JP7E8WGq+NPe" +
                "EfTvF8i/nYLlOg09n9Ws+NQknj92fMbUH3h+jNqn9s+l55k/pJ" +
                "8YfwL2VfXSyKv7G2KKr1rkrQqS/eTK14yzyV/LLlkeyFMG0mUc" +
                "fXO/wZW/o/6r9V8725/qt2P/O/Wb1L/7eT3m39hn6vi5+G9m/Y" +
                "H5SeU4q/XZ1+uz/wn3O/z3c5YtPrU6EnX+Lt4ta/pTU8cn1dK3" +
                "vgH2H+K/lfVQJh1dRPDr+P6T6wfX//La5+ODOeRLaD/g/dWY/g" +
                "PXTye0X1vWj+T4qCU+Vu3+2h/br8Is5vfqyuDzpfz4dqr1ceRP" +
                "W1t/e/8YHwX5zYU4L636verl+KAlFc1ql1WzxJU41gujLw5p8r" +
                "fxzfh+xGj+AF042jfbmX+AH6jR9dLSUtRpW7+A/JF7fsM8/+XH" +
                "n4Li3yf5pabzQ7O2OX+j4R/29SnA/aRs44P3TwWKn9jrn9X/of" +
                "pHF2wUHl+M5JOp4psA/FPmiV8y50/Z/WsY3Bp6/wfrhwD4TF78" +
                "J4//2mJ+H7p+3PjeuOyro31s38799m1yv5NsH9fxE+H+CtBPdD" +
                "/GjPCttX9Wbf8e+WTHbzoe1TA9vbofVwXIlyq+l0x6Gvszy2ZS" +
                "5beh/rXXH8S/2/2N6kfw/c8q6/6A9/8xfoneH0Oo7w5YP8OZH9" +
                "v4tD/+oMR/w8x+al9gK3754f1BZalvXZ+PNA+qr6DYL1/7XP1E" +
                "8nHvP6b1/y/j7Fe4flTp8UMHP34/JMAX1ppROlI1XF/gb59dHy" +
                "sc8ZEhycetHyHws97/g9YX119UchS/zfGlTzp8azbZF91HxL+L" +
                "8wP6/MPz5cz+mVs/Qa8fUeHyJ7i/lL5+IbB+g3s/hXY+KEJRjc" +
                "H4vOuP1/eEJ59VUG1B36QDf3ve4m+l6PC3coO/lZP7eXH1i3R8" +
                "188f//4YwZ8/Uv6twP6yt0+/fygD2g+gg/Nrgv/z8kM66p+bnw" +
                "P8I0H7kf0T8ZOo+C4d/9tDV3H+g2wf3vb55dn/bPy7lp97PrX7" +
                "9eX53xtOZ9vXaPyWSA8Yn7IQEnw/hfeZ1YfM629Y+LKrvka529" +
                "dj+VB9jj++9tffaNr5By9+CbUf2mfdFs7xuc6XxOh2wQLE1y66" +
                "i59Uv9fWN03nfyw/pjvrq5D/J9VfXX+6YtL3/z/p1uwq/f5H9X" +
                "0e/QX7G9bnMvRbs8cP6w/58+Olp/NP+w7/dLHGX4q2/bJtvxl8" +
                "g7/oGHzNVb8YdT4G2wf5U4L8mui/UP5GpEfH9wj/icSPiPE3fn" +
                "92JnwrzftZ4/Nn0feG6p+S4X+B6887P6Hvnzz94/Zfc/Nfr31k" +
                "48+bf7vutyO6P+0i4qOEEBvlD/742mXe8f2YwPphC76pfP592r" +
                "4lf3bI1+ePcqf5OTf/2kyozII/svFh8vvlZMD8297/HXk+sSt6" +
                "RZufCPuM/GsYHdsPv39OhL+6659p+EKA/5nw++ursX6/9tc3Z/" +
                "Zf1+L7eQvK/ojDF/n4aMWzX9z6nmTnR5no3Pidm99geu78jZd/" +
                "JDv/yzZ/ifTDYX+SnV+4xl9tLEycffDzDxH4uP0L2sf1/zs93+" +
                "/oA6hu8v1atPY9/Fnqf0Liz4o1f9bvDxrcD0zy/oEiND5Ovf55" +
                "25/Xx+umPn4yA9IX/7vpm/Yn9ruvv3f3T6PT+p/bz0KkGV+y+X" +
                "GNr6IYZ9S/O/8nJ1Cs+4H57p8pnn9IsH+Qf/DSpTrp6jO1lLbv" +
                "X8Lv74H9R+BLOlw94vXDLz9df2n6n4QuAt7vEEi/FuN7i+jh+k" +
                "eo/w3Ad0P7t25H6VNN8P6TXef/XPyfSr8h+3Pya1xfFDO/Ifkv" +
                "9fwk1/lNXn72+dv6K47L6mr/Nu60bOvTjZi9vyUUf0X11RAi0h" +
                "b9oryfKBVd+80Ker8Mun8Azpfw9x8y41Pa/gt8/06gfSThu/J6" +
                "4reZ6+fZ388Sdf4rCPafxpb7fJOUH3Ds+/8AjDLjEA==");
            
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
            final int compressedBytes = 2841;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTuPHDcSZtMtgVqcYXph6RS2DQUTKLAjG4paPvtgX3KCcA" +
                "LOmX+Aw4sODrgLBXLmwIHCgQIDzvwT5gAHgiMnBvRzrh+zM909" +
                "JL8qFjmzC6iTfVTzVSzWi1XVSu0erTzP7+89ePbXX1b//unWxX" +
                "e//u3Vs98+/f5p+/zrP993T18/cg/U7/988OzeL6uvPujh77x6" +
                "tvn0+/Mt/E0PV0ZFH9i/EO55rH8mGrzih7PHd7z1o/nP2/8M8H" +
                "MIR/vTwWu1e6u+Gr/l489PX/tu0/AP4abDeK1Vs12Ccep+/3PV" +
                "/Wd4D6/f3143i3mM41e++dU+uPMOVwXnfzb8eWcY33bzXxPHt1" +
                "c/9JT+9stut/MLPn27ulG3F32uqfuzba9D7eP9o/3D/Q+PfPyz" +
                "yfgz/CP8ALidw90IN4vx1cH6GedLcn7Q/CD9tIr1OG57ADeth3" +
                "9aTR//6kwREe6468sFL8T/VFVH5Sds34rkP4l+vfLDZcHP5t0H" +
                "X9y6WJkPlHr46+PLu5vbq6fKmS9tv7Bhfm3R9ffj1xer+8P41T" +
                "D++Wx8yB9l45P2p46wn5n+sGcodPbFmf+o33y+nd9/KfuD5gfw" +
                "C8eH58POEeBCxyqNf2/pxwTpJ6yE+Psn8R9Be37/+qjzS+FfBf" +
                "VbtL9Q/084Xxz+jPEjlg/vD4elPmAWL4D4djRB2wITDMFJglwP" +
                "7GQkFrsQbdVypK10c7P/2iB/S3zG7qF9j/YP+wcu/fx1HZFfNj" +
                "Ztqxj8Owxfx8fRrP31zF8Cx+RroMxxafrnUeZvY4Jw9pwrdWsi" +
                "QbWy/+otXdfTjSa0x/RNdSHl0F9S9K8WSDX9F3vFQ3oT7v6Tj0" +
                "fktlobDPfYTwFC86//7B/q7rnpXrp10b3StOqT/9j2+eMfK/f0" +
                "xaOxvebjzxaGc/iXTP8vvj9S+Sq0L+T2Z0vgRYdsha4/W4/+Z9" +
                "n6d3NAv00m/8uIgeqgvy09kP0rof4dso+j8lmbjuXanj71FX12" +
                "E7Vdp5uRfgG8/PmW4z8Oz6L/QV9TtH+S/4PWf5WMn9L960D/N5" +
                "1+UuA5/bvuGq7vLfxo8KangGp95/Lb7pdvXMeqn6/t2vx9pWqX" +
                "Rb6k+m/f7s/1kF9S+ca23+km11H8N1T7x/98aN/ojr7rjzrlp/" +
                "qs+9u5TpWzjVHrRj2e0j9ChA70/3revxr7V9v+Tw1H+j9sr74a" +
                "8NOb9n0X1YCXQb9pdvszg6sZHPefql/R7Bs6/nQq/of5hdor9T" +
                "qKPyn+M8wfjN+Kxs9yPgX8Vdr/h/bl0L5Rt3XfviG1F9x/vIUf" +
                "GZ7B/qgl+r1U/z91e6zfjnZj42rX+x20Mv3x6QXFvan/zAC39A" +
                "1dv9nI1ofwsxobVmo14tdpO8Mvom+jov0nxEfy9Dvof/xD5h80" +
                "iDUw/ZuH+ucl7f5KJ64f+Efh/Rlz/31PJL4E+RcJ/m3wPOHBHW" +
                "X+nPONxuf55xQz/jmsfzSD/iHH7023f6F9iO9P6pL+yw1Y/+YE" +
                "+LEel3E1vtwsHditp0lG/4MJHREH2BLNPu/5kPLGJzeZ4AtFZ/" +
                "ocxX+3CcQ3N8z+Q/MH68fyOxC/3Aj4A0d+X2/+JY2/lcfvuhPD" +
                "hfNrBgyvK/Vt98ednqic68Rju6Kevwzzv9b236n7l8Kl9oPMf7" +
                "y1v2rFfpyAv5TmXyob/yrN/8rEZ+3Hb/v43f81ptvgh03l7m5u" +
                "m/Pu318ObxDaF4ebXebRnKzqJQfSQf3JDPBd+7Hz2lXVDH42gd" +
                "s9nCZfaxD/kAzv478C+/uGmD9ZOP7pxPTBvN+rSvhXI/IPxx9R" +
                "+JdnPW1N87+Q/TMh/OzsHz3VX2fwmus/ZogN32mHDz1+V0xf2L" +
                "8bP3/Af2lmW9JMp+NIiMT84aT2Azs+z1H523rP37L4P4P+RyH/" +
                "tLL9MeOMmj1+7PjvNcc+rMGcSsOZ55+RX7rNf97h0Z1kfafE33" +
                "z9h+0RfE5uW/pqCs2f3T/1ft2O/mP+/bovvptx/2DQ/kj7b+eI" +
                "cF75lYw/2B7JZ6n8MaH9d1P7NDK5DP6nmqL/SfVTLdkf2fiR9e" +
                "3uX+wYPzLev6jd/QvU7+T+iYT8j/T4caUo+Xt2Kl9o/CNU/wTa" +
                "Z23C/DLbdzUBXbH6LqT9DdSXAfyPxN9j9W8gfVkZfWL5EK3vQ8" +
                "jvB/ofyO+n1HeJ11eI16dB/SeNn3X/Evd3rUj1f8T5v6g+Q671" +
                "nXnHT8u/VzP5eu+HIX/x4SJ/cee/7ODGk99I82968w+nJTSE/m" +
                "+afZJgn6L9j8k3qXxE/D2jfxvn3xP1W8XkPxP5HPHPofo6Uv2Q" +
                "1D62RLB+gnwQ1r9h8p8wnFFfhjE/mH+cwb9bcH6Yv8bxmxafuO" +
                "yo2f9aL5gj5o+12tcP0cv6IWn8VVrf4VrBM+Enzb7i1peA/n+V" +
                "6v9HgrL0/W9QfqP6PTJFAN/PyOJXAbyeb2k7gTqKCYz4C6wPIa" +
                "w/Ufr+YT6/xvOuD86gT6NE6y9d/6j0/h0lvksQ33zd53dqOIy/" +
                "FtJnmfgDhvwL8uedfXYJ7peAfChlH9H8j6Xrp0/hxgMg6R+1xH" +
                "/RkvVXk0AfCH8wvxL6L3f5p3qaPxryX+bzX83pR8/0P0b+KPAf" +
                "kf234f6jTzr+F/4RC/SrQvAs9Yez6T8x/SWUvwfy06B++4d3fq" +
                "z8tshByJIfVC/tI0b9fGhfIXie+mDl4nPi+Wdi/Hfy10TPrzA/" +
                "jdv/oX9Xl9RP8umXwfMt8k+n1690aWb1jfD/TLZDGP9AwC/IX5" +
                "Ppp8eOz6X5h3LKT5CfheVXtD2lvn68vn9h+0KYPya2L6T7i/JT" +
                "dvZb4H4o9X5+fj9Vp+Jfzv+p++ef35D/cTHL/+g2wu7yP/zwfX" +
                "6IdH17DSsxfwLmb8Tbw/wK2vxUan4ApH8p/ZDPP7IP/Pq5Np+P" +
                "+qHVemCBLw/zG6JeU6RfvojrH2n5FRz9G7VvNbwfietfqP68t3" +
                "/6/alMPkv591G+v8O5//HwD9H9m/B+DuFHWl9eaj8j+7O4/1xa" +
                "H0ZIP4T54fyqOu5f6U3Ipt23719vhvg/R4jvxvQX7R8RKpzfQX" +
                "sb96B4XwnCcXznk7l8Zu8/NT6FmsCWxJ/S4+dp8fnB+Yu/H7WI" +
                "L6on8UWtynI/VCD+m2Nfl5hfPvsK6Zel7T/x9xeL4I8JZ9Svz+" +
                "2fIMV3Hzm+PEZf/Pj5OX4qrv8xOD6V/mT66dWD47sj8ac0+RPy" +
                "L2e534scANn3cdH5kX8/VxZfj+xvrF+C789K+WOafsLw38sMnG" +
                "PgR2QfFrYvxfH7CucPxvV3/P1zUfw5+L4uRX9F/q3YFgm/H4X0" +
                "T9y/kP/i8ylaH/5+qrQ9Ub+xPDRmux8C+MPfp0y5P0H7Zw/bh9" +
                "cv9Q8i/TjuH0yKH6PQPNF/xoz/5/pfoX/Mu+03J38AxW/j7/Nx" +
                "+E+DaWEx7bOz4PcVXz6itEffZ0RmF4zPlvm3pfHRafWx6e1L+1" +
                "+PHd9ckTmFK9J/xbYPqf4njvw5Zv1CzvlK6d8nH6X+qaz1BZ2k" +
                "fj3j+z5JBgCKvyfYb/H43Gzr431/hv59HNn4JPzXgvZC+gnFv/" +
                "L1J5S/G4zPnckXT/0N2fcdluMf6qdgoeh8E75/ILVfpfaxivvn" +
                "QX1Pef0AaX6/uL5RpH6lOH6xtPyS2T/S/JoM+TlS/uT179Hr38" +
                "vq85eJf1sQCvv7h9L4k33/W//M/Z+2/pnfsH/GphwLDLdstM3a" +
                "2ej9eSR+Tmg/JedXc/2zR7L/Ha9+ndR+lI8vs6/Z59uV2h//+a" +
                "X7l4j3e0FtJ1F/g/2njF+gfqkRwkP9/x9gRzAl");
            
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
            final int rows = 605;
            final int cols = 8;
            final int compressedBytes = 1495;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW71uHDcQnqVXB0ZAEEawEpWrQMUVblI5cLVKYiBJY0OwAD" +
                "9EylSBC0pw4TKFC5cHFwHc+RFUuDBcpQngx8n+3OmWqyW/IWd5" +
                "EsJCsm80w+FwOD8feUTdKMkZhkZDQH+6pqv+v3ZE1wRG7fLfkL" +
                "/5t0vXW/mWSkXV+hNt6aj9vWw+6XkhP1M/z/rZ868VsONp6xvS" +
                "3fXXEzpt6Vdfnvy8d7HU94kevD+9PLxaLM/I6semZbQnAx41uT" +
                "p1Y6nbCVvap69Ozr99t3z+eu/i9/c/vT3/8PDFWf3y13+/tmcf" +
                "HzXypfTp9W8H5Nd1yHiM/anx/pfEGCpi/2L023hfL7+YOp8Bum" +
                "u/vzv7/bi235+d/cP+OcXv2n+j//5Af+PVf+L8cMe0A29kmZjz" +
                "FWH/yP0hsD8T8a0llBUtnPipaeWLxYH9mTofJPe/oP3R+QD7g+" +
                "Mnsk+Qzoo/aJT+5R+bN6qhl5VZUPEDUWVt0aheaaoqOmWYD4aI" +
                "mpOfAvsr42/zS3mxPHrd5pfi8vDDYnng5BfdWUcPTGXUdDrx+V" +
                "+YH8YHkF+F/Ov19/m16PKrs35E7wVVW5nl6BTj88nxTrWeollD" +
                "aaLCp9cmdpb5y01KGnjCWrp1PjUUOSxv/bz6jkD9O5x2LP9yOn" +
                "6tpuWM+eH8s9FVHvnAf4T+NfSfjTJm5ADAP5j6Ke7+O2N/nw4P" +
                "dLOIvYtGRFXT93+Y+uXpX4U9e/OIo5b0/AP6/m9e/V5x9MP5k2" +
                "u/5Pr4MlQfxNQ/esJwn56cnH/zbvnL/Zb/3tvzq4cvDtb8n7f9" +
                "hT9+Q37gUsfmVV8/ND+6+oH6+oG6+sHA/IH1TymvzUC/z51+31" +
                "Ff3xx39Y1p6ptVX9/MUT8H5//ozk/9/LSen6FfcMwgX7Q+on+E" +
                "+MSI3474OfXtYBTe+pdG/aVl8pvE873a+I/If1n0khX/VYQCu6" +
                "y/gvLZ8cmbCHLXJ/B8ePq7immfK5f/Wn7Fkp8//1HYPtL4qpHx" +
                "i6gDW6T4d6B/VlqRMa3Yht5uwdHTRnFj231T2ukPwvWrl470D/" +
                "MfNJXToENslH3W/qVt+VRyfxFDh/EzM/8oA8yM71/nV7NQk/nV" +
                "g9+Hbic49wdsfB/4rxxf9sUHy4o/2fFdiD9D/xLiixA/leGLyf" +
                "RVmK6Y/QnnfoGzfml8yISfQ//MbH8GPh3EjyG+J77fkeK7YfwU" +
                "3w/K8F85PozuRxE9vqTaLf6G8KcU/NPMh3/OVb/cXv0VW/86A8" +
                "rfWf/m6b/k9wPztK/++ic4YvunIhr/l53PmP5YJ9lPph/CnxB+" +
                "hevjMF06fzp+Nrg/CeoP8DOEj4H5Gfiyg199cf3+IRa/QvhZrP" +
                "6x+CvIT6n5D8UHcP7w+QT4kBD/YfknSv6lJP7Wwf4V4SdsfCW9" +
                "/lEsfCYR/0P5Wfz+BZ8vKX8Q34b4B6++KYPnt2R6Xe1umJo3vq" +
                "S9H7rl/h33j3XW94GM/hn5Z/j91E7xjyl86m7bN/f72hn6a1l/" +
                "IH9fJKNL+yN+f5q4vtz5rxa9/xbXb9n7x+H8FbM/UhH4A+pPa5" +
                "K8n4i+P7Uc/+O/b5Dmv3T54/rW+35blj9g/Bb2Rxz5gv4Y+wc6" +
                "v6A/EdsvfH7qLr9Uulnig6qwTX7TTclNjzsJ0/nFgLA8w/vGmP" +
                "2da/9T6ZnvR+54fZT//hG9//+/rz/lfshkpu+wfsz8fhvj/zH2" +
                "qYCu1Q1fFL8fBnQkH78Plvkfq35ind+AK5TC+B6IDxC/FspnvP" +
                "8U1X/8+jXt/R37+1Wp93vkw7eY9gH80vx76/lBiL/ddf0gPpkb" +
                "n4D4ARt/SPL/3PkB0jN//yQtPkXpz/I/5fSXZjZ+8f1t0vuhmP" +
                "tRaf6U3r8x6rfA/Zn4fdlt30/knl+Ov8rw0fz49Iz45FT9B76f" +
                "BfwT0fnvN+Z8f2zm44fnG9Rvdx0/2cX8Wf07xf/n18+bXyFdlh" +
                "/E+SN7/S+Tnxaf56NDfAHWr3nxK+n+SNcH1/8fqYKbXw==");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 4774;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWw2UXVV1fqJtEIGgDYWmKNYqwSKUdLXyo8CZ9+bBNOAfQi" +
                "st5ScENQwQqEnIH8O799z33sybRMIigURCYhKGFsEKUrpW2yVq" +
                "KqW6jAgugfxg+QlJ2oBKS1dX16rL7n322Xfvfe6dLnPW2e/bf9" +
                "+33507d+7cN2kfah+aWNAGmx3vjx/fgQiX72aT7UP9Z4szIPNN" +
                "imXnltn9aCfeQV7+3kYj+y68LvbvBPR6iD0N9ecgajT8cdmD3Q" +
                "IR1Rd/h7YzNDmzEf4xKyLtU2zoUGcDZ/Ur4UajM5s7QfErjPML" +
                "dBVb/0Hm9/sQ9b7u3xe8V6ebwM/yx+KrG3Ej/o+CdW5k4juIgj" +
                "cvX+5Git3+Qo64kWy4zO5HOzko/Y/5P3Ej3b7/fT/XX42RYg9E" +
                "/zDkzvBnThT5j4Sn2EWvk5O+7S/yHy9ZPg37En+pVIJ/+tDBbC" +
                "Kgj8bIOSp7nh8Kry3Yf+xG+hfD6ydgfyp/RlX9QbAfdiO9h/xZ" +
                "/uzg7fMf8ef2l/iTg/eqqj7fX6C8T/qLAxp1o/6vg33IjQ7+El" +
                "Hw7ssn3Wj33f5BjrjRRqPM7k/87f7rbrT9nN/m/4ai/Uc5D+fT" +
                "A43G8BHCA8cp8vn7YX9N84P/mFSi338gG2g1UUXMGjDDV5gB8C" +
                "Naz09xTX85d9D5NDHsTwveq1XO2PuwfzSgbW4bxIMNFdtowZn7" +
                "fbdt/NMS4bo6n1DzGnwlPPG08Po3Go1uV/q6/yt9VpMiVmX8tG" +
                "y1VrN51kh385r6GuZvxH/+spTbTuBf9z8LKHc5xIMNFTktOE4H" +
                "Xd7bJxGuk+VHbabYiK+EJ54RXjxOvZOlr3ev9FlNiliV8TOzL2" +
                "p1m2eNdBcb6muYvzxOf5Fy2wnCcUK02q0u3h7s7OIEqFhNy3fz" +
                "f3Orx+8v3skRt1qyqV/8Btrm/GIGRwebGRXvKI6E4zQkff23Ck" +
                "OYill+E/ZvpSrju7LtWq04RmeL3+ZccRS9Bnxsc76qOTzYI7Qe" +
                "HMmZxbvgOD2rualfT1DMKo4LaJFbBNcntA+5RVCxiBZcnw7B6x" +
                "RcnxbxkiwhVY3Xp0XNq+L1CWNTUuUfyB4cOkN4MBczdH1SrOH6" +
                "tEhrTpxO9awmqohlErg+RQbAjzSvkiov/VPSEY/Z1HScsRevT4" +
                "gWuoV+ebBjsD2i4HX9bLdw/Gt+oSuXX88ImMB2/1N8jPi7sp3w" +
                "PpdiZAA5vzp03eozuD4tFp7+kdzlc38HcTErxFY4pQlXmqezV0" +
                "UT+CZ0lqK+R3nfh70K9truF3SVYl8ZO5b4InzfvaC5Naf1hk8a" +
                "Pgl+GgWL/xCR15w9fNJgtkS4rs4n1BvDV8KDNy1vb5709T8ofV" +
                "aTIlZl4vPZQa1m86yRbpy+WpNvYX4+n4rDUm47AXuu67pwxIIN" +
                "x65LC5Te7bqDlkS4rs4n1NuKr8xmeYfOlL6JSemzmhSxKhMH8r" +
                "doNZu3arJx+moNHSdRhuN0YsptJyi9K92VgKIlRJ4/0V05aEvE" +
                "ZlMfce+N+L4NWzyfLhSeiduli+ulRyui3/xhfphWsxNZNdn+Pe" +
                "ncaONxUtrFKdNxJt4GtwFQsCG2gRbMd4LbMFggEa6r8wmNn4Ov" +
                "zGZ5h16XvomvSp/VpIhVaT6VH6/VbN6qycbpqzXxOJXKcJxOS7" +
                "ntBKV3g7sBULSEyGsOuRsGD0vEZlMf8fhofN+GLR6nnwrPxN9L" +
                "F9dLj1ZEf7Aj/x2tZieyarJx+uq0er54nD4yHaf1Ojs6/+T/HH" +
                "6bvIN+A8ze0/lhtqbzLJy5p3SeG3wfMrthvwKZD8G958uA/yP8" +
                "9vlmsP/T+WX2Noi/mJ2azZhYA1WnwT4ye1f+UsP8G3ot1L/Yea" +
                "nzcvtfO/s6B8G7i7PZYdlbi4/D6xHRPzo7Njuu863Ot7NTBk/m" +
                "76do55nOjyJ6HvYL+Suddfh7cOffYdo/A/Tfoud/L3t7YJoV6p" +
                "/sfA/sU5LvXRSP07kS67zBqPtrwV+fvSVwHJUd4zY6vIsONhy7" +
                "jbRA6VS3cfADiXCdLLgfNxnqZ2x5e5dL32CG9FlN5tBr8Mv8XK" +
                "1u81ZNtv9QfQ3zl+fTpSm3naD02q4N909ox2B7RMHrtne59uAC" +
                "uH9ql7H1jKAXLNw/lT5GyvunWAH3T9gV7p+QjdfgcO6K90+KNd" +
                "w/tWU1GpMn5OeLJvBN6CxF4f6JFPuwV8Feq/WoJrKvjB3x/qn4" +
                "jObWnNYrn+I8Ic+CutdjrDmzfWjwSniqM9Y+VCzEWOe1zk/5eU" +
                "4x6kf5qU3+Ij25yZYzR/5S8Xn9fdf7YrGAn+v0n6DnT/E7fVXn" +
                "F+Hp1q/z05/iBv3sqPmDfKS4rrMzW1nO+mPo3gsar0DXMnr+VC" +
                "zhbsCfo+mLRRnaa7Nb9LOlbElxc7G4PJ/yzv7i+sD688Dz2Ri/" +
                "sfhCdp08jXJb3VY4n9DCXSKcT1tp+W7zKAf3Q3A+bS1j6wXjgv" +
                "MpIjjmmIfzCexSjsD5hNFwPvUekL7BzJKRzifFEs4nowJfhStE" +
                "A3ombDbEelGxD3sV7LU4va2J7CtjB59Pt2tuqpGJlLfULQUUbI" +
                "gtpQWMn6EYR7iuzhcGwZa3t0P6BpdLn9VMFSmaL9BqNm/VZPs/" +
                "ra9h/vJ8ejjlTt9z9JquCShaQuS1b0IrEZtFC9fx0qfuOIdho1" +
                "hri/AMPiddWoE5pJL8/FqtbieyarJxejutTKi1i29Mx5l46916" +
                "QMGG2Hpa8BW5kmIc4bo6XxgEW97em9I3eFT6rGaqSNF8iVazea" +
                "sm219RX8P85XF6IuVO33P0+q4PKNgQ69MCpasoxhGukwXnk8lQ" +
                "NWPL258nfYO/lT6rmSpSNF+q1W3eqskuxuprmL88Trel3Ol7Jk" +
                "9/HmE/+8hX89X+W5VPOuJnFqPyqYf8tJFPNzTv8FPSP9hNP+90" +
                "/fSft8Aka37Vz1tko1718xY9X7yjfFvKbScof97NgrWbLBw5Qh" +
                "QD7D+rIrMEQ6XxQ2+wzEE+MxnmWZOnlyy7Y2fJaivJzzcpTa0a" +
                "7rU5Viru5unttMxedqTTljUykfIud5e3TmFLiDzEcP0tIzoLvZ" +
                "fbaoyg5Q7ymcnyTG5hFsppVltJfn6vaGpVjMkkrEjbVml27kin" +
                "TTkT72Z3M6BoCZHnr3c351+WiM2ihetT6VN3/P6HSH6f8Ibvu5" +
                "3Ck2+TLq6XCbQi+fl2rW4nsrPL1npSE58XKO18ajrOxHu+fkG2" +
                "tDb2q/v5wxRN45Pbubqaq2PNH/n/8p3ZEiPOao30SmW8Pl2Wct" +
                "qZSm+Ng9/yyYbYGlow3f0U4wjX1fnCINjy9i+Uvsl/kD6rmSrG" +
                "Sf5Kq9m8VZPdPau+hvnL43R2yp2+5+jdCGuqtITImyqtxCKC3h" +
                "ttdYhMxU8wLNtUyNbwhOhU5IqsSWWsUZpa9cagF2M0QblNVWlF" +
                "b0pPKzMoTuvd5+4DFGyI3UcLrnBNthITjEtyhKif2Syv5pl8XK" +
                "toTeawKvk/anXNlKrxbjWrczO7nornYu7qBOyF3/wni37njsq9" +
                "z0/gN/In7F1J5zW5w5D7DH1PUt6L/MQ+p+tfIvX5Nrp/kud0YY" +
                "pxe4ej7uT+Ob3vKedZx/dP9g6OprdzoqXruP6XP1k+pfu57qfn" +
                "dOr+aYVbAUcs2HDsVtCCI30+Wr+QI1wnCys0on5ms7xSq3kwrj" +
                "3msCr593SXZkrVeLfOr+oxu56K52Lu6gSld7e7u7mPbKNBiDzE" +
                "OsJ1db4wMBaETJYHc6IiHiJbSTX5Tt1l8xSlWemVdlWP2bmjGf" +
                "5iRaaVGplIeS2Hn9FFS4g8v8m1wv1TS2IWaZ+649cLInD/VFaF" +
                "nyu3CA/cP7UkY1m1zzX5c1rNTmRnl91dms6NNt4/Ke38+ek4E2" +
                "8TrD1kIUKIYoD9jSqyyWDrC0PEgpDJMFNuk8qpuK2kmvxN02Xy" +
                "MbpHKe7h6W0Ns5cde8Ix2KO5qUbNJ14H1l6yECFEMcAm0jHY+s" +
                "IQsSBksjwh11E5FU8UQ00+33SZfIzuVYp7eXpbw+xlx95wDPZq" +
                "bqpR84k3380HFC0h8vzvopWIzaY+dcfvI8MWf2/pVnl0vfRoRf" +
                "L9+22XzWo12VU9mVBrt8+YjjPxbnW3Nv+LLFzVAiIPsY5wXZ0v" +
                "DIwFIZPlwZyoiIfIVlKN/4DusnmK0qz0Sruqx+zcgYjVmJtqZC" +
                "LlnefOAxQtIfL8SWglYrOpT93x62XY4nW8U+XR9dKjFcn3c2yX" +
                "zWo12d3bUj2ZUGu3z5yOM/GWuWXNn5GFoxsQeYj9NRLhujpfGB" +
                "gLQibNTDlREQ+RraQav0B32TxFMcaKtKt6aPMtrEfKrMbcNIFM" +
                "pLy73F3Nl8lCV0DkIdYRrqvzhYGxIGSyPJgTFfEQ2UqqgeOkum" +
                "yeojQrvdKu6jE7dyBiNeamGplIeTe5m1ofZUuIPMRwv1pG0LNI" +
                "V2MkfrK/lCu4CrN1PBilTonbSvL9l3SXZZJJEPF7oOmtHrOT55" +
                "cgYjWZQTgTby2sA2QhQohigP0yFVlrsPWFIWJByGSYKbdW5VTc" +
                "VlKNv8d0mXyMHlCKB3h6W8PsZceBcAwOaG6qUfOJt8qt8suDpc" +
                "+DV9Hy3ZZzq+B4O46gJxiX5AgFzlgliD4PrufxecuJRz1+Rari" +
                "79VdmqnU6BHyfdihpqoX2VeyR58H8+R6Av0+S2+lW9l8iSx8tw" +
                "ZEHmId4bo6XxgYC0Imy4M5UREPka2kGr9Zd9k8RWlWeqVd1WN2" +
                "7miGvzySaaVGJlLeYrcYULAhtpgWHOkRinGE62RhhUZUzWyWV2" +
                "o1D8a1lyoSd3Gy7vITOmvVeLdGqnrMrqfiuZi7OkHpXe2ubp3K" +
                "lhB5iIGnjOgs9F5tqzGCljvQ96uZyfJQN7FTp8RtJfnw8051WS" +
                "aZhCbgXdVjdu5Ip005rde9KNvQmpuf3XmiNTfb2JrbPtS9NPTP" +
                "bc1tNCYLtPBb9VndT7UPtebi3/Vkd8c7x4spB9n3IsI8dWbhr4" +
                "a6n4xfscDEtfYf5Tq/QDzZBaYTMda9RD1vA9biMajZKT2dH4O/" +
                "t3yaNpvY8ZkaKxFvo5HNzJ8VJurWyoFtPz+nC1N/TLSz6wS7C2" +
                "DtKi0h8gD7NSqisqYvoBDZFRh3lRXMu4ujpvuCEN3FHsVtJfnF" +
                "47rLMskkpeIunt7qMXvZkU6bclrvHlj7yUKEEMUA+9tV5B6DrS" +
                "8MEQtCJsNMuXtUTsVtJdUU3zRdJh+j+5Xifp7e1jB72RHOIzVt" +
                "WaPmE69wRfMVsnD1D4g8xDrCdXW+MDAWhEyWB3OiIh4iW0k1xX" +
                "d1l81TlGalV9pVPWbnDkSsxtxUIxMp71p3LaBoCZHXmodWIjaL" +
                "FitoteZRd/xeNmzxejCvyhPu3u+wrFaR5ugeY7t01qrx9murej" +
                "KhTMVz1XEm3na3vfVhstAVEHmIdcRt9+sFS58gj78fRYwINzFZ" +
                "HopLTsdtJdW0X9BdNs8ahPCVdlWP2fVUrMbcVCMTKe8WdwugYE" +
                "PsFlrAMMxWYoJxSY4Q9TOb5a3nwbj2mMOqdBfoLs2UqvFuDVf1" +
                "mF1PxXMxd3WC0rvN3dZ8lSx8twZEHmK/QSJcV+cLA2NByKSZKS" +
                "cq4iGylVTjN+oum6coxliRdlUPbb6F9UiZ1ZibJpCJlDcGa4ps" +
                "+BR5LK4pvyVkJDJmsPGhkxliVFD5uXnZ112iGKZib+yxlTE6pT" +
                "SSKVhZKU6lc1NNzE+VHeFz8+53NDdNIBMpb57Da3C0hHC1jm4d" +
                "jb5EdFY62KfueF4btnh+H13l0fXSoxXJzw7aLpvVarKrejKh1u" +
                "5dMR1n4i1w+H+ioiVEnt+KViI2m/rUHecwbPH/t2yr8uh66dGK" +
                "5He97bJZrSbbfznVkwm1du+x6TgTb51bByjYEFtHy3eHv0Qxjn" +
                "BdnS8Mgi3v0Nb6Pq1APVWV3ibdZfNWTTZOX1fD/Op8SrjT9xy9" +
                "nusBCjbEerRAaSPFOMJ1db4wCLa8Q3fX91nNVJGivQd1l81bNd" +
                "k4fV0N85d/H3JFyp2+5+jd6e4EFGyI3UmrNaM1g2Ic4bo6XxgE" +
                "W15kq+vTCtRTVel9VXfZvFWTXdVjdj0VHKerU+70PUdvs9sMKN" +
                "gQ20wLviIbKMYRrqvzhUGw5e1fVt9nNVNFiva+obts3qrJxunr" +
                "api/PE7Xp9zpeyZveM7wHGANNnxuO4dW6/DW4cNz/LclwnV1vj" +
                "AItrzIVtenNamnquJ36C6bt2qyq3po4/83L5XhenBkym0nYM+N" +
                "u3E4YsGGYzdOC+7AxijGEa6r84VBsOXtj9X3Wc1UkaK9x3WXzV" +
                "s12c2x+hrmL8+nZSl3+p6jN+zwN4doCZHn/8UN994nEZtNfeqO" +
                "n0sZtvhz5ZIqT3lfoOLa55reIdtls1pNtn8y1UMb/64HPL8knk" +
                "9HTceZKPwfziybDg==");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 4438;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdW2uQHUUVXh4BeSiSsEnEEgEtSy2spEDRIFK9O7kRik2AiI" +
                "qWiZQJkGwilghCJD/m3rm7d+4N8khJCkliKCRWWWo22U3KwB/B" +
                "PAhIlvAIIAlvgZgNkmwIb8TTffrMOadnZgHvVPec5/ed6TuP7t" +
                "m7ptt0t7UZ36OEWmWX6a7sYov2hjpm40ejoW1yPY8j4zlHMqJu" +
                "K5FZ2ivZuOX5uELJ3fhDGWagzTKzQPI9SqglW82s5EG2aG+oY7" +
                "avQ6H5cXoujyPjOUcyop5s01naK9m45fm4Qsnd6CvDDLRzzDkg" +
                "+R4l1Co7bM8W7Q11zPZ1KDQ/Ts/ncWQ850hG1G0lMkt7JRu3PB" +
                "9XKLkba8swA22BWQCS651tAW7wLT6MNrJQHG/JPO3BaJI1bs9A" +
                "EU7IGTL6Sh6RWdqv2bj19BfHEH42TqND7PCYvdYyLZBc72wt3O" +
                "Bb3Ik2slBckc4ILGvczoOL8zRnyGi3zoNrvTJL+zUbN1t9UQzh" +
                "Z+P0VogdHrPXmqYJkuudrYkbfIvb0UYWiivSGYFljdv4U3Ge5g" +
                "wZ0Vp5TWZpv2bjljxaHEP4NE5pe4gdHrPXqqYKkuudrYobVAd9" +
                "fSlbKK5IZwSSG8dr3MZdnNcYz3makzA0S+NTkk37iSNslcKY2g" +
                "rCz8ZpSoitK8i0i83FIPkeJbtFZ0VnWZ0t0ssZpGO2P26FhjaL" +
                "FuLIeM6RjKg3Juos7ZVs3PJ8XKHkTgfLMANtppkJku9RQi15wv" +
                "Zs0d5Qx2xfh0Lztq15HBnPOZIR9cbpOkt7JRu35PGQjyuU3OmO" +
                "MsyQQX8qQ7hPeibfKe31udYT70Gt1qytr88jX+1EmQn6gMuYI/" +
                "Mn31mf3Tbip3pYZaj2WZf5U+1pTKp3l+fFx7ucq0Stl1k+2P/M" +
                "VlW/JMyo/7x+ZTZOz4I+3+Hsdb5Lfczl9V/UWpxjRpvR1JNut2" +
                "hUNAptZKE4mcG6lUNMiWvRQhwZzzmSEfXGGTpLeyUbtzwfVyi5" +
                "0+dC7PCYUasM2fMAezwrcINv5C60kZ/3nEGaPp/IJ3Etms6OO2" +
                "Q857BO2bVlmk174+OZjVuejyvkqtramoeF2LoC0swN5gYYMde7" +
                "sbsBN2BajzayUFyRzggsa1yLVpSnOUNGtDbOlFnar9m45fkIXV" +
                "YF43R2iB0es9fmG7g6qUcJtWSnme/mBfPZpiWpY7avQ6F520OM" +
                "A/OC+ezRqFKnGJgXCDZdkWbjluwI67a9nxcI7mZXGWagwQck36" +
                "OEWucptmeL9oY6ZvvjVmho67gljyPjOUcyol7drbO0V7Jxs9Xn" +
                "q5X14ad3ZhlmoJ1rzgXJ9yihljxte7Zor+1hfZfpmO3rUGh+ff" +
                "dYHkfGc45kRL3xK52lvZKNW8/2kI8rlNzNZWWYgXaFuQIk36OE" +
                "WvKy7dmivVrvWIjZvg6F5s+nm/M4Mp5zJKPVOxbC+aSypFezce" +
                "tYGPJxhZK7ua0MM9DaTTtIrne2dtxgnHahjSwUxxKcT17vuNZa" +
                "suP2ERK347d5HBnPOZLR6h3XwjipLOnVbNw6rg35uELJ3XwsxA" +
                "6P2WuLzWKQXO9si3GLxkXjzOLek9libSxzHkvROLvHKCm5+dO4" +
                "4jzJaSWpk7V3l8zSfqo9bHk+2/v7uK/VjdPbIbauINMuMzB7pR" +
                "4lu0UToglWZ4u1kcQZpFs5muBGZAL5kuvIR1aZTR6JEjKi3ujX" +
                "Wdora+eW5yN01LBWGKf/lmEG2m3mNpBc72y34Zb0RJPQRhaw3c" +
                "wy57GULLF7m4cWiYvWfJ5kwBzJiNb0TJml/cQRtjwfoaMWTUK5" +
                "dXKIrSsgrbaD57DuGdyZrbeicJZdu1DOZPmD67va3SOt3jpuLf" +
                "eJleFn8/g9T9W+K2P0vJvWd7msnRq57NOaUB4jPeZWcyv1pNst" +
                "Oi06DW1ksTaW7Qb3cS9hVHSa3WOUlSSuzGVMzYk5khGt6UUyS/" +
                "v5CHTL8xE61+q+v3dCbF1Bpl1lYKWNvbNdhVvUHrWjjSzWxjLn" +
                "sRTZ54WPkpIbp/biPMlgJc2I1vRHMkv7qfaw5fkInWt159PEEF" +
                "tXQNoI6+C5cKXd9mHWwbxedm9kfmkttTuCdfBcjq/d/hHXwY//" +
                "H+vguUXrYHzeWS3xY9bq+ZDr4OVmOYyY693YLccNmLrNcre+W8" +
                "42lrXOCCSnv9a4Fo22dBHnaU7C0CzpdZJN+4kjbJKPY/y8IGOG" +
                "cboxxNYVZNocMwck36Nkt2hsNNbMSZeyxdpI4gzSrRyNdVfYWP" +
                "KRxFbc0mWchZmMKnWKSWZLNumnaKqdm+TjGD9Oc6hWGKf1ZZha" +
                "i/8eb5BXTfWEeBucy49BdQfix915/SRcIy/0XBC/bN9ngjwMUS" +
                "fGr/VMB/2t+P3qoXA3XAC2wwmlenR1NDxxzlfPuyUO69n4ufh5" +
                "2P8r3gVxJ9FZXT24eojLPNI9qy6sfqLaXh0X3x3fY7Gru/0bq4" +
                "fjRzAjfgLaUxAPz8f4+Hi3f7P5huBbUD3CRlaPc/H3xveD/8H8" +
                "M3DRKNLjvfE+4J4qn3fVg1xVH69+0txkboIRc70bu5twi8ZH48" +
                "1NyRtssTaWOY+laLzdY5SU3Pk0vjhPclpJ6mRN3pRZ2k+1hy3P" +
                "Z3t/Pvla3TgdHWLrCjItMQlIrne2BDeo7m2TpH9hC8UV6T3PEw" +
                "KjadzGZs5L/8wImpMwNAtcd4JN+zUbt56dRTF+nDJmGKdvh9i6" +
                "AtLCmSPf9ZN35HtfbPbvCPLpNtJ7X/2ZvETG6+cdZ4RPXdTTv4" +
                "a4cmaMz7vwipq8JKzzg2ad8d7QX9stnncrDIwx9m7sVuAG4/Qu" +
                "2shCcUU6I7CscXuPLM7TnCEjWtP1Mkv7NRu33iOKYwhfzLQD7P" +
                "CYUavsKd5gNA+t7Kkv1bYwpkiH+ZPS3TczqvdYjksXkdd9W3s+" +
                "CDW9biR/7RC2IaZtMH8qOCp73aGUZH/HCjF1TaSZ6831MGKud2" +
                "N3PW5wPr1nrk/vZQvFFemMwLLG7R1TnKc5CUOzwP1JZGm/ZuMG" +
                "86eCGH9/yphZK6uANJwXxBvjTf4qHYy3xQ/ZeUHnF2leAO2F+E" +
                "WcF8T/iYfj/SC9Fr/O8wL39Dw8W0G7eUG8Bdo/4gd8NQ/Ez4Tz" +
                "guzO8J6dF1QPo3lB9Sg5L4Bxej++L94KcTAviB+F/fb4iXhn/F" +
                "T8NN6f7LwgfgXnBdVR0D5mq68eUT22ekz1uHgzeMS8APb/jHcI" +
                "9pfif0M/hPOC+ICzvRm/Hb9bbRPzgkvNpXAUvkcJtcp24/42Sh" +
                "btDXXM9mOi0LxtMI8j4zlHMqJuK5FZ2ivZuGFOWK2sL6w2xAy0" +
                "GWYGSL5HCbX6QWZGeh9btDfUMdszKzRv28Y4MC+YwR6NKnWKge" +
                "tOsOmKNBu3eq5u2/vrTnCHRxJWkGlTzBSQfI8Sap1fMFPcumUK" +
                "27Qkdcz2zArN2x7M48h4zpGMqMM4qSztlWzcbPX5av04Ce7wSM" +
                "IKMm2RWQSS651tEW7wjZyANrJQXJHOCCwHuNuL8zRnyIjW9GmZ" +
                "pf2ajVuyoziG8MU4BdjhMXutw8B8j3qUUKv0GDcTJIv2hjpme2" +
                "aF5m2P5HFkPOdIRtSbQZb2SjZutvp8tbK+sNoQM2SANcUljdmV" +
                "VZWhxk8as6pnZ/OKZ+HsvcVJq2hmC+vgVaXvfe+hyKKPeVjMvp" +
                "9RK4BVnIW/6wk/tgoZZSXSKquK3/tWnq0dyCpfpZGCyFU0H89h" +
                "FBxNvKnjcvcMvDF7usMasPlHd3842K3AL8d5Qdk41d+r/qa6oH" +
                "ScHm0Ma0tylOdZGL+nx6lxIIic3dgXb00OymrdDm2nz74G6w4/" +
                "vcc57zGA9nr1aoWmf3V1c/xS2ThVu/XKqr6o3qisDdd39S9Xhp" +
                "prnWUtr+9ILl7fIUrR+q5xL8c31+H6zmOstZhQRVq2vktmc5TP" +
                "yGqqrC1e39W/hPVQL95Vrg3OmrVl6zvMxmqi4ch9z5V+//7D6b" +
                "Z1tkfDzfutXOmPhnsusDYYp370g2U67qNhyiQUtOn3T40t5KF8" +
                "ZraYKGPfcyH5UXfjJKKsRHWwlbL9FdaO9VBPR8VHmo1GP42Tq1" +
                "q+f+qnqqJhc545L4Uzz5wHz5UX7d5KtnWOMec1H0CZmn1fgLLt" +
                "cc8S7llW43QfxzfX4Z6jZDbU8TL50Z7M1lG6Jm3NxmmMROSjGu" +
                "l9QehXR3m+cd+78d8+6rbVv8Ieam6czqeoZJ7dU5SMNufnxukZ" +
                "e3+ieNxzlK6icYD8aG/ua+yTUbombc2uu1MIsfG6PKoRxynwy2" +
                "Mz08w0+7wz06A+eN5ZHTzQOsfaHmVqbpycTD4pcTTK6r3vqzIe" +
                "9xwlszmOYprDOkrXpK3Z+TRWIvJRjThOgV8eW7Q32muvu8oavO" +
                "6sbnuwPBm5J0C0t7KGbHB/8jL5SPI5a8iae7ZvkPG4x6jKGotJ" +
                "eHjdkR/15n4dZSWqg62UTXxYD/V0BFSjuAutye5Pe0OPOMpXIl" +
                "hpY+9sr+AGZ+4EtJGF4op0RmBZ4/aeUJynOUNGtLYOklnar9m4" +
                "9X6mOIbweTRC7PCYUTNTjbvDG3+fR922+lfNVPe7nqnc3HU3la" +
                "JwzxLhkKzOp03u/jSV8DkzzHb3p6m8wTF/3t+fpnKVskkr8xGi" +
                "uz951t4TR7zugqrlsUX7o/1uPr4O709WhxGE1nml7a1cWUc2uO" +
                "68TD6S0G9R2KvuT/tkPO4xqrLOYhIeYcmI1ud0lJVsS0bhXlaQ" +
                "jdOVWA/1dARUo7i61mXX3f7Qw0dpukyXmxd0+XlBl5Vsq59ue5" +
                "SpufOpi6JwzxJHoyw/6XgZj3uOktluXtDFG4zTqTpK16St2fPu" +
                "axKRj2rE8ynwy2OLXo3gSYS9G7tXcQOmM9BGFoor0hmBZY2bTi" +
                "jO05whI1pbp8ks7dds3OqTimMIX9yfAuzwmFEzY8wYGDHXu7Eb" +
                "gxucuVPQRhaKkxmsWzn7HrxP4qYT8zgynnMkI+q1ZTpLeyUbN1" +
                "t9vlpZX1htcQWkRVsiWFFg78ZuC27wjXwr2mJ/r0IWiivSGYHk" +
                "2h0aN/0m59Vu5zzNSRiapfZ7yab9xBG2+plFMfiejplZK6uAtG" +
                "xtORCugztPqgzVNjnLgFhFDvDfD5N5+rcv5C1aB6dX8PpW/64H" +
                "cgbK/85pLbXNQdQA1WTl4nWwrT6rZ0D+roeONLtbDxT9fk4fK6" +
                "yDN0QwA8Tejd0G3OAb+U60oZmyheJ4S+ZpD+aTrHHTqzmvdRHn" +
                "aU7CkJtd30l27dds3OrTi2L8+ZQxs1ZWAWnub8IrYcxW296spJ" +
                "E3KztPtb2zrCabjeNvHqPF35ZXVlaTNb3GSu7qXonIRX83d/ir" +
                "LaZEDd+reF4f5TKGsjpW2/OJ0IkJq6dqKdMfz2quChFKz6fV4n" +
                "zaGG2EEXO9G7uNuMGZOzHaaH8nRhaKK9IZgeTWjzVuuoDzWjM4" +
                "T3MShmZpzZRs2k8cYbPV52P8+ZQxs1ZWAWnZN9mXuz9Nz0azT9" +
                "yf+op/h+Hz+sruT53Ty36HATl9I9+fbK6K6qOarFxyf5ou6ukj" +
                "VkQKzpq+0vOpT5xPmyO4S2Lvxm4zbnCFfz/a7M6nzWxj2W5wf1" +
                "IezEe5NU/jpis4rzWX8zQnYcgNzqduya79xBG2+veKYvz5lDGz" +
                "VlZBpg1Gg3L2jrptnV3RoBunQZi/e5ubyw9iHEWzRF6yqeddP8" +
                "fDdTfIMW5FMihRKY6iWjN1lJWoDrZytjufurgeiokG3fOuZN2i" +
                "8/Wxsod+X8DnXue0ol+dF/0dofisDa67Ed798N8RilHrP4SYrS" +
                "Je/B2Bf2ev+ZHP/h1BIf2ggP2l0v/sHuG/t6PfZSvXvwHu0iIP" +
                "rJi/7t5bzyvOLPqk94j/AZjb9qE/FrXV/UEReX5bfT6utqLtI3" +
                "wa38jbJn/aoy3Lxv+FgGdZaBnJH8amm/I+4hwZNY+lq6aKufIi" +
                "/g9CKkPPPv8D2r3kFw==");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 3225;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9Ww+MHUUZPyj2pGir7ZW2pzUxJkSNRjGAhRhz+/atgTaYQo" +
                "qeEjEWimLbSNRKlWJm3r33en8sadqYkvSgBIMUgyXEhv5NtPxp" +
                "QSlUKHDantDDK9BTgi0FitQ6s7Oz33yz38ybPap7mXnzzffn9+" +
                "23s7Pzze7Fq+JVbW1xWstDthQV7VJ9uideVV8O7TjXg1b9Fvmb" +
                "tVdgu9Hthp2fgR5GsBFVT/1mE82Wt3mqSO+LftZv0vbb8qOoby" +
                "Lk1Jp4jWilddq3Rv0J77jq0z1ajqLBArSx3egOWg8j2Iiqpx9p" +
                "2fI2T5U68+MZcSro43PG8tXOTOt23RM93oYO4NCHnx+tL/ZpzF" +
                "ZW+9e6uNKCxsX4tvet/ff7F98a36prTSsqeVX16R4tR9FgAdrY" +
                "rrRG6WEEGxE8AS1b3uap0jzPj0dFQOvic8by1VnZ+YzpnvovcH" +
                "SBQx9+fqWHuF6zwq5rfZXzigsLrJPCp/BKjyfCv+rMLLq35d6t" +
                "E2hPGHG/rcUd4uWblmzMVlYpXbCgcTF+UcdvyWXddfCP5UirRa" +
                "yMWYXPcY8Z2c/bpYZrdJm8Mge/WHgx6BvBejzxS/x4/IttbbUN" +
                "4x1b8ep4ta41rajKWtWne7QcRatWZa38BZumXWmN0sMINqLq6V" +
                "9natnyNk+VVnhUBLQuPmeQrwzqujKox0FlUNGVQbh6Feu6Ai1b" +
                "mm9aMy3RepXCWME90irusTXkeFL4gErZlfzaBt2vJSqDYeMpGc" +
                "N3iKJlqW9Mxvp6VVsXGO26Bv1krH4vbTN9Tt8J8v3Py5pFRS/A" +
                "lv6TVM91tl3TH9aJPctm13tsP2Xtv++KXoN2dVp1mpix0jqdu6" +
                "apP4F0f3WanJ90j5ajaLCg2/3D2G60AfT6D4AextQ2MEr/QRMN" +
                "8zWGXXoOUDIqToAMlMuDonx2t27Nr8hmPI8DxzHTIX7/y5jbvM" +
                "LgjYZYMOQPh+PmePMp7vjn8QLq9jzO51tx2h6qST5j/2qc94Fy" +
                "FvoPlsfVeJh7GuO0JZ8vL7LitCVUk8xbBgPG05ZxjSdSS3pf5J" +
                "7GOO3I77utLk4rTerofb4MNj4GvlFeq74lzOvguNwX36drTSuq" +
                "Mkf16R4tR9FgAdrYbu9BWg8j2Iiqp77N1LLlTTQo0ntKBs65GA" +
                "GQKfrjfjZWLmn11OxZHJ7f9QXkKC4LtifF9ZNbp1VWOu7xtS1H" +
                "+pI4u16K00qTjNNC4z76UTkLev1URkt6X+SexvlpU36H77TitC" +
                "lUk4zTd404LS9noUWcSK36Dop7+uIEeXD9YYvzVf8dw9uzs/oJ" +
                "Nd6bCwKwL3fM482QPNhaF9yQ2exwxH7ZexxPO/M4PeLitNIkx9" +
                "MNZbCtOK0sr9VzIMzr4LgMx8O61rSi6rvjYXnf6R4tR9FgAdrY" +
                "bt+NoDcwCHoYU9vAKD3XmWiYj9GgNL9Gyaj7DpBxBCgPNKUzRD" +
                "sDFXHaY+bBIGdq4Dy4mEsDt3k15LYDd+I82LSKs2CdB2M0zFV5" +
                "sF2a800pnAfjPN22XczDU28fYmIeYo+wR7On7JNsH/sze07E6T" +
                "GWrg2ZyADYS2yUXyl+/8FeY0fZMdF6g70p6hPsFD8rmwna83nm" +
                "A3yq4D0myp9YtoPYXMpe4FewF9khNiL6/85eMZ7sJ/mZfAKfKD" +
                "Qnpfrn8Ml8Op/B/sB2pTPAt9jjbK+Qe5o9w/aL32fZEDvIhtnf" +
                "Uu7V7Ijo+6cobwnd94nyfoG3hJ/NP8yn8A62W3D2sD+K+qkM7y" +
                "/MyJzYYfaqqMfY6+xf4vd42vc2e4e9y9v4Gak/H+Qf6jrSJVB0" +
                "rVqKqj8ha+jBXJtW2urA1rJ5dWnRjikPOiaiogc2Yi3MNdGgFP" +
                "HAQxPbPpPiOSuq/jbezTGed0/ad5M5YuvvFp7EJ/PWv4l5/MdI" +
                "9pQo7xTXl/UT1Lqw/hR+fuIdMfp5V99bfyvdE/tPi2fsevdK1O" +
                "Z0vaFKqpevMKJlmmdKAe2OE/m88zyHTZuU/YF7izLgM+2R9r5l" +
                "nO5yn5HNQePp7hzph77xVPboY9BunOG+bpT9gd+UH0+29844/b" +
                "rEeDquCr7vGhM1z5QCumSc1njG03G6ncdpU1EGfKY94ve4eUhu" +
                "o1vG5HDxTIsmq5LSz+Sc59LrMjm/QqkU0JnUUOC7sCWEj0+DZR" +
                "OFHB+in+83PdGytIbCc1mzLYdzoib+FeNpiqZVnxqFUdOL2RwP" +
                "x+RRdwE/gqXAJ/nruO+adk17gtH9PkdTVEl9yv3sekDzTCmgS6" +
                "77Nxvj+YEiPtX2yYDPtIbtvefaTgnj8F38WjEbPsrvF+2tMD/x" +
                "dGeZi1yd/z5tLRLlevaaoWnsqfL0+xAuVoOceDfNdzZv8ewgGU" +
                "9K/j1C+3dCZq8h/6woB0V/mjPy3eR7hBUpz4oBz3dZ+B5YZzrn" +
                "ru9bsT+qSsrL49CYoHmmFNAl5/H1nnn8KN32yYDPtEbjTJ81l2" +
                "U3x53fNabBm0J/fqfl6rEnv1tn2wnP74o5WEB+90sqvzOtZau+" +
                "alh+F4/Go2L2SOt0HhlVfyJO01Wf7tFyFK1aifx6JLeG7SYzaT" +
                "2MaSNqGVML8zEalCKetm56peVM2/Y5K8qdBzc+auXBS1x5MF+q" +
                "8uCkw50HJx3sBUGPKw8WEp48mHVSeXDSEZYHC7+C8mB7JMLYa3" +
                "wcr03t8Uvsp3QWV/m5VGerdS9e+dsS2D/TD7UuKNx7nfbdRllT" +
                "fuF5BM8EBd0rVc1fz2fCz9g8qJ1n7OQ2N7h1/DaLUrKlKZd28w" +
                "6Xz5jix9zoFEfHkef7AI3PU9fbnzG5uZVut05IzojGAJqTHe+l" +
                "ul0+Y4qfDMvvkvnJfKhVS1GN85P5WkL34ZZJp+1J2KZupX2Tin" +
                "ZMH0DHRMy1kRbmmmhGKeBp66ZXWo6yaSNk8lep2lhnnrJ5UDvi" +
                "P8vNTZzfrCZX+WxSUrKlKZe2wqN8tiifzwQnWaDqWj6KGxfaPK" +
                "izdxqLaStkDvCi05sFScA7K1NKtjTl0lZ4RZ9tqjbbjV7kmCvD" +
                "/Hn35dLPu6me593U//Pzbmrg826q/aSjn3fu7w6j88z1uCyNS/" +
                "FZ9Sx2v29pzLPyll2A37is3HeHjbnu1Yvru0PTezirxlfKPYWM" +
                "7w7bq+1tbapO923a1Z/gDqk+3VNtlz3mnymtLei20ga7fQ9Rdm" +
                "xMGxE8ATTM1xh2MX0FmWRI2zd2qizb2IOcmlxFu1GKlqXRrTia" +
                "1nLQ27NYSuN+sGGNp4c1R9nCMpoCW/qv6JvpU7E3n12/blqEs/" +
                "LuJU6me6S2yltE/Lv1vi/bJ8ayyFui2TpvSbrZS4LTq+RYmkWz" +
                "dI/dfn+nrKi8xYrTSCpP5C1Jd9It85ZUM1tZ2HmLkpJ5S+bDkC" +
                "jDeX/2lknmLfl9N5ufLbny+wIhYeQtibWSA1rmLZhj5y0pNdPa" +
                "H/8m1qr9lh9q8Uz6iHMnZ8TNa31w7/eZ9DozFK+MX2qfrvAe4d" +
                "t6ny7jLOIn8D5dCYSRaOQ97NO96dunc+z7jlD7dCR68D5dandl" +
                "tFT95hnSBaJcKGnVZ/NJ71aOh+O3me2PG1LgE/hN45k17YkP3e" +
                "eXMZ4WWpxFtclBY4fY961NiQ4FjjxiPNUm+TRq55Dn6MRrXIv3" +
                "fcsd6r7rmQhximNd85+nvHYZJ/O+i2O7xX/qihPIWu8XYr6Ced" +
                "8kx3FN7mbt5TdrWt93KeJyKk5xrPDUfcdvQldimRknYa3UfZef" +
                "0fS8dW7baTtqM/peafsfHTXyi7loc5h2qFwu34d/xdy0UNOqL3" +
                "t/1xdipRzH5Dnf3xlS4JP8dczjfXZNe4LR/T53HVMl9Snff0pe" +
                "1jxTCuhyR/VuzxuPY3Tb9MSWAZ9pjxRea29rn3DL+LR5/h98jU" +
                "VWdC/zjKOUV/ukWzr6VeC4JlAa1/tkab+iwO8yg+UOu+gC51K3" +
                "fpFn91QmtMamLYEm7ZHPr/LnHzwrfja/ij/IWzemFpPymbaxX7" +
                "0t0O8k3Fffdz3NrYHn+7mS83gv/hVInxLl05JWfdE8zEfa82zt" +
                "aB5tn0I2ebZeuiLZh6W0T1JW9M9zn49Z055gdLfP3Po/Jviup/" +
                "YFS2suOZ8N0Ty7p3oXofs04dtcz9y5n5KlNZrbA8fTBYHjaBTv" +
                "ekWjJqf193TRaBhO5Swau/BeY4zWTHcOR/Eunfd7ulHXKsOWC/" +
                "+eDsX3orw1J1yr9dFfas4plUnQcXowcJw8WA4t+Y6qIW+p7rB5" +
                "UPutkHHqcOv4bRalZEtTLm3lPeUzpmpdbnSKk1yjalg/NWfYPK" +
                "iz+XUxbYWM08VOb65xa9FSsqUpl3bzXNpnm6pV3eiI819VQWyk");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 3117;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9Wl+MXFUZH4ptpUghtdZEEx820aca/zy4piZm770zJUbRJu" +
                "pSBdMGGiIsRHwxRh+Yubuda2bEWvtHjA996ou0aYTULVVRKaXd" +
                "drVGEAgJRAqpwGLTBVNiNMFzzne/+53vO9+5M7vd7dycM9/f3+" +
                "87Z+49c+fcaTQajazRKPus0fx8o9SsvnMWfI0qoqHqWdD7eqbk" +
                "+iwxVNSxJt1vvZnnyyK4PDsLqh3u1foq9J2fVvy/lz7q9Vf+xb" +
                "i397k4cx2mFmUl1GLZUL1WM9fyW+Lsmqd1J/T5lyumx6WP+ihy" +
                "1NtL4jn1mGGUlVCLZUP1Ws1cy7fE2TVP0ufvjUZ3B+pgS5r513" +
                "w/Q5yT2WiR+Bqz70uaynn6dR6FNVmOpN/+kI4qe70Szj645tY2" +
                "6POt1Tz1pI/66CcQ9fa2xHPqMcMoK6EWy87HYzVzLb89zl5XV7" +
                "69micxm8nNMnZyIu6Tlt53hlsnQyRT0x11sVqGydk2HF9+Z2NR" +
                "r85Hyu+DG7MbzVyxczpfOxTCrFKNQ1vsK7+r1nu9+r02gK9zau" +
                "F1pJewTy/hPKWXyIorDsl+HkfQrOmlfELmchZ/pQtZ8vt0VnjZ" +
                "9QltMAYNt5zTAxQpq13g+fS/8pramW0KrrN9rXORK9nZJ/e7T3" +
                "ITWbzPd1NjyV/Aodc0iA/9+feH5xs7iP3YQbx/GjuY/9D24PPj" +
                "BunQ9+4n3WA9IGMxI7QPski/PZ/QBmNw1f+gnk9jHrh2roLm5n" +
                "eqnO+RbMSsT1vRbiOykWQV6cpnNEI9l7is8WtydbV0wxiqWa8I" +
                "+OLVYj35j+Ixmifp8XczRz9GnWxcjqMszFOPadaCN3iUX1PSi9" +
                "w/9WSvV1LHLj3JSmj+952Z6V3o86NIX9ir992a2V2py3UxVLOe" +
                "kRyuQ/Pj4jGap7UdelzHzTfB96SP+ujKGvX22vGceswwykqoxb" +
                "Kheq1mruU/i7PX1ZXvraR9S/fNlO/vHW0s0yvfo54Ljwy5Nj8y" +
                "PFO2EftsI37fZRvJyuM03Y8NrYCsM4f2kCV/qM5v1ye0IZOGy7" +
                "O18Q11L3IH9PmvqnX8l6aN+D7q61HU6+BMPKce01TxJI+yEmqx" +
                "bOCTNecHZI35oTi770nuTe5Nf449SKBZ2fBVFt/r54FkLbbHDN" +
                "ARieNANqBDJtl5JOgQgXEciSpBRmghH6JjhqxWYnItmzfHqDsD" +
                "R22zunk3NiuDxeq2TboVq/TPYzRIzg/34yVGdaaPAjLkVvhwjJ" +
                "K9wh71/POYSVHg91s1gnlkwuohosr00CrmUa/S+eAqrbKzi9nF" +
                "9DL0ZnadBJqVd75OFozTdEJAmSSL5CODj1hIsxKPJFTK4n6wWh" +
                "syQgv5EB0zrIRsiA0xVBFp1bX4jfJ6/Ux15R6RPikrV3TU25uO" +
                "59RjhlFWQi2WnUxTPTyCa8l0nN33ZGeyM9ijDlrzk2BDC8ZpOi" +
                "GQzHF7J/Q8zikZCZWyuJ+zUUse1WMIrZynRyW2HHOpnc3OGsn1" +
                "znYWDvMtcxlsaME4OiYnuAeiUea4vSc1HMkpGQmVsrifs1FLDu" +
                "kxhFbO0yGJLcdcarPZrJFc72yzcDQaxYfBhhaMo8PME/NANMoc" +
                "t3+thiM5JSOhUhb3czZqyTE9htDKeTomseWYQWvN8R1/0G3Leu" +
                "BBHePIOjlho1EnHInp5um96AGs1lw7kaw+Bx5hbX5NcJ/JKyvH" +
                "f9xHpFEF69jx8GmItLhqn2ifcPI4/g5u/9VwP2vOp0+0n3N1vN" +
                "Aab79iPBshrv2Ws/7bzlP7P+13O+9xeR/vrEaUzvs664J1/GmX" +
                "9Y/2y+3z5v3V9mtVLeOt8c6KzrUuc01ZxdrOBzofbP+x/Sc/qv" +
                "239tNlDc+b9mJlf6OcsXe88R/pXGe9nfUu/lTbrDTtc/5IMY70" +
                "trgvMNnXuGpu6NyUzWQz5txxvTvHZuAw8/QpsKEF4zSdEEjmuP" +
                "3r9DzOKRkJlbK4n7NRS6b0GEIr52lKYssxl9rJ7KSRXO9sJ+Ew" +
                "8/RpsKEF4zSdEEjmuP01eh7nlIyESlncz9moJUf1GEIr5+moxJ" +
                "ZjLrVT2Skjud7ZTsFh5ikBG1owTtMJgWSOm67U8zinZLRHupIQ" +
                "wyo4G7XkMT0G8at5ekxiyzGX2rmM7cODbluxGTyoYxxZIZokis" +
                "6Cvf3+TX485fmsPgcdYZSsh1dWjv/Xsk7upzgfP/jdco4zmDXr" +
                "1uB+fFr6WNxv/OecWqSYp49G73pvjWfpUVZCLZadPEz18AiuJQ" +
                "/H2evq8ubp+FLupvU/tlz7dPr+ePe3w2V3fzfkHt2ebA/2qIOW" +
                "PAE2tGCcphMCyRy3n+p5nEEy+ha0y3jpg2arr+PTZgBz+ZhLbV" +
                "e2y0iud7ZdcJj16WawoQXjNJ0QSOa4/a/oeZxBMvoWtMt46YOW" +
                "rKjnq667FWE+HzOPr2b3FygVX4h5Imdmrb+/ZXFX1SBWiuCRY+" +
                "8OhxTGRVh2Z7uxRx204ktgQwvGaTohkMxx++N6HmeQjL4F7TJe" +
                "+qAljXq+6nxS8vmYeXz4G6e4JeaJfDvU+rtPLfc6zvm7J5eWJd" +
                "uf7cceddCKb4INLRin6YRAMsdNn9HzOINk9C1ol/HSB20QnzYD" +
                "mMvHLOPl8+DidnzeUno+O+Dp9uo6b/rc4j9L+7xl8PnU2TSYzz" +
                "5vWeT5tC/bhz3qoBV3gw0tGKfphIBycQ/H7c7oeZxBMvoWtMt4" +
                "6YPWPV3Pp80A5vIxy/jgPvPxpby+u3++gie+f1jEfeaWxrK+vH" +
                "k6YdjOLhVu+uwVzPHswudJ57uC625vthd71EFLTd99lSwYp+kg" +
                "pe6dMH3cNJLnc6JFsvhVyipCH7RBfNoMYC4fs4wvZ/x0Jc0s6e" +
                "+7w1f3911699Ky4J5zc73cHy/ua83Z77vh9sf9+xd1f/xYuD+O" +
                "nIP2x7uvxfbHLYK+P57eo+2P1193surmespubmhucLYNpc/pth" +
                "UPkAcbRWKP+eRHDDFPJ/14eA8z/Cp4BMf1a9IqMPP0bVkn92sv" +
                "6aeqmhvwOQLdP1XPEdr4HMG0V0z7Z3meV88RTF89R/Dvn7TnCP" +
                "2nYs8RXEbtc4Spb5XM5XMEIz1v2osgT92mPUdI77LPEQySO2f9" +
                "5wg1V3Dw/wJ6jtAcaZprC3o3dyNwmHnqgw0tGEfH5AT3QDTKHL" +
                "d/WsORnJKRUCmL+zkbte7regyhVWeNwJZjpvixy9Dc6v2Xah0/" +
                "h77yl7WLIl25vs8vzudjxvBlDNWsZwBfXbWDGLnHrlGpW91snx" +
                "7AFS89YGXocd2EOFofIRok8oMV0EAnGeIhwj7nBB9fxwGL1nGM" +
                "wSjw8+eciIJMWLdcxwkLpfTAcM85m+uaZi2B3p1j6+Aw191PwI" +
                "YWjNN0QiCZ43bf1vM4p2QkVMrifs5GrfuWHkNo1XUnsOWYQcsO" +
                "Z+bOBnp3T3UYDjNPu8GGFoyjY3KCeyAaZY7bn9FwOCdhaBa0y3" +
                "ifjVr3v3oMoVX3mQJbjlnGl1nVv56KPTFP5M6+1t/cOsRvgyOL" +
                "+kVxpI5vcZgL+n3396XELVZc3fvx4pol3qd7KXsJe9RBK/aDDS" +
                "0Yp+mEQDLHLVbreZxTMhIqZXE/Z6NWrNJjCC2cAa2CMD44n15Y" +
                "ys+jmV3d82mp+cbeHHuTepBAKx6yPVm4V+qQzTFRcufT9SGOH0" +
                "85PiPG8Czu9dmoFWskH1Xoc8uRhGMutYtjF41U9iCBVhwec/9y" +
                "RQv32n5ygnTILpkZWvn5bgtx/HjK8Rkxhmdxr89GLeSjCn1uju" +
                "1jCm1uzNxLYQ8SaMX0mLvLQgv3Sh2yS2aGVp5Pa0McP55yfEaM" +
                "4Vnc67NRK26QfFShzy1HEo5Zxif3u1WpmulCfN+B33+F/8Oo2X" +
                "86s1zrU1iXG8e/hvx+n1/kfcH2Snp7AbVuHmRpbl+GGdqscy8H" +
                "X3Yhu4A96qAVL2cXivNkwThNJwSSOW56VM/jnIjhH+lRv0pZBW" +
                "ejFvIhul8VnwGtgjA+uC94Z9AcL+S6S6cX/QRiGvef9NfUbVfC" +
                "V6xb0O7vjtYO21Mz/YPQeITn34GeGCbGu/XpmVicxJOIxfrWg4" +
                "SDqLIOWX/xfl5tOFp9ZIiGHPouub9j0doNje+be/65+v8VULz7" +
                "fI/F/3/A8SRieiysz68D7jP9XXy3+3Is/q9xvn/PR0Y7KWKX//" +
                "9Zegvv");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 2789;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW3uMHVUZv2ip0EUu21JbS22UhEIgKq+CxQd37p1RlG1ti4" +
                "/EhshfNLp/+EhAgl06956hc7e7fQTDQ/5RoSoqsT5C2ZUaNdFu" +
                "F+gfxpaIENeYuKZZKiCxldKic+bcb75zznznzGNnNzqb8833/n" +
                "5z7jlnzsy9W6vxw/+t/7uYqXXeiKTfcy6cqUmH/7fO6Zbvv+T/" +
                "I+JfizUnhKW9AHxaPtLIfijyf8Z/Fmz+VM1w+GfiPAtBbvelPC" +
                "b9wz3uSNSO+n/0X4zOf441K2J6PGono9izo3aOQNHub9cj7cFU" +
                "tuf9FwBlhGvaPxbpZvxXoqs/5f8r9vi3f8o/3dbinMWixf10Br" +
                "TdBWCTvVAudoTvMtvknKb8ug9ipiPClbZspszZFu9JQb3Hk346" +
                "V7ex+4GnD/YAt7buoGy0tlfzyeyrkb04B5IpWtQD5OkrBTytO8" +
                "zVKYu3X1D21qSfFum21j3A00frHm7lXiDLNiOa/baclBfnQDJF" +
                "i3qAPH2lgEdgNtUkdBOCer9O+ukC3da6C3hDP93FrdyLshnRTN" +
                "hyUl6cA8kULeoB8vSVAh6B2VRT4jd6G5EKTkjdJZyiRrXqsoiO" +
                "Z9+darYenk3pPDIGjJErgo8apVrlatjS9RAhl1hv3oXvNuXUK8" +
                "TcjDfDKbaon1YrfRt5tO5GP6B4jrDdjZTr2ajwF1pvxvi5zah5" +
                "0p6YR9SXcYj7nY6fo5C90teCuCCnjAVqaHfKaF/Q3sfObY/hvq" +
                "B7hbovYItaQxn7giGk8b5gSNoXDJXfF2w/YtsXtA+S+4KhjH3B" +
                "UIJL2hdEZ8O+wF3oLgQKspC6NwsdaNyFra3IYxxyra38LLxkLs" +
                "azlY5Ta+oVe0gG5CjVjlegtnQ9yI5YEZecW0WQSHW3Lve1kHnr" +
                "bnDr4WrBi9baJjxkivFgb20DnZB7eLbJ/nIcVoWsYAdteKnqhZ" +
                "jSWqyn4+S08y0Vj4pR7Qk9uv2b3mjfF7Uxdn5yv9uUzINf0fOl" +
                "Le0v2pMxjWZH+1nC84D3h1rpo/1zq/UgueaR9drjCTdRDot3UN" +
                "D2qqSfPqvbkNqzkPvjK8wx9pxpL86BZIoOLzdh1v3N1WVLa6o1" +
                "BRRkIXW/3poK38s1oPWm0YoUI73pOPs02IBDrRzdmpIxgF6WwS" +
                "d8nxqlWjFSbul6nIp5J65FxiXnVhFgBem5Y3k0HnE8Dev92zk/" +
                "61Nn/dS8Y8s69fDD5ecdW2KzdvrI8XRWxlzOOe/YhWypFhmvT5" +
                "3bkn7akYpZqcmDBZ6DW+X7afuR4utT2KzN6dF8SOZQKhZrtxXL" +
                "muUv9pn5YopWliKXNZchFZyQurs4RY1q5ZQNoiyi1ZzAxffWt6" +
                "TzyP4YI1cEHzVKtcrVsKXrIUK5duiacuoVtLn0nmTePaG9jdli" +
                "eYezJUvj5vzcbFVoXzrCrXiesWuitibmGvFs763j7BPdp+LzzX" +
                "nWcbaOfSY+v59dhes4uzam17MPROv4BjLOYwNsfSLdErVPsU8r" +
                "HlfCOs4+1NNIdwR2I3P4Os6i1Y99vKf7ZNQ2htJayq6O6XUxXc" +
                "tuEOs4+yD7CInpo+xjkrSBbVI+m13qORpPvwRZ6JwB1a5ED+jR" +
                "oNHzE7G7ZJseR3kBJu4b6Qdof53SSNTq2Zi9MUFdLxm5nm5Dat" +
                "yZjclx0izwKC3E2HOmvTgHkila1KMw6/7m6qpFfW6R9k8vZT23" +
                "KFkmrdYD4cb5fW4R+3Hzc0sJFKZ+eiVqL1f1fOeel4Wj+2qV/U" +
                "TXm83zXeM5PDeeAynC/U/dI7XiDaazmGq4b8/sp9eyEBax0vXs" +
                "mUqOpxNVvi9w+2ZxT/5yifHUV+148g57h4GCLKTuSaEDDfhRMm" +
                "ZAXs0bbqbj1Jp6RcyKUapdrYYtXEn7YLZ0D1AI0MM0npwvVDme" +
                "Ro7O73hC9JWtT2N4bow1kntheJvuYYq0+YB1ZBYrQ1Zu8n73+e" +
                "KZyq1PziXFx1PM0ePp+fndFyD6ysbTOJ4b440kU/e07mGKtPnk" +
                "seZFWMTqrMjnS/uRsRN4bkw0kn7uvql7mCJVnvKW85bop4ni1v" +
                "BOuy9w4ddmvS/4T7l5R+/HZzOeSs27Zfn247Sf4X7yWEwfV94X" +
                "7PXui+6I97EfSX77TftM9ij7WXx+hP2YeO/7g0595E9k5e9FbZ" +
                "+me6Lce1/2Qynip44Uw75rfu/rLMnom58w5VPyxgV19yQ7tT26" +
                "Danpva9nWEncPSMvGHdw417G6sWRyF6cA8kU7SylMeuSs9Rcnb" +
                "J4twuK8449pduQGjMbrJ1BW4w9Z4TkgOrFOZBM0c5yE2ZVcpab" +
                "q1MW75CgbRxFu0VDG1Jj5kPFVx/vUFZUjETy4hxIpuhwyIRZ9z" +
                "dXpyzepKA4nrwtug2pMfNkifE0ac+Z9uIcSKZogZ7CrPubq1MW" +
                "Z9T5ijgnXrt447LQ6XZytI+WsdhzCiSyF2JC3HQ9mdJIbNVlC3" +
                "vGtC/wKn2+m+99gVf58512b7kxmeE7av8jB2LKbw2Hq8fROCma" +
                "vH+q1YZvAJvshXKhEfHX5sP2+hSfRJ9I+yBmGpGolwet2YeyOD" +
                "vVc9RPt4CMOpU3ZylmsedMe8mYnJ3098HCLlMaia26bHHXumuB" +
                "giyk4c+5a7dfjBrwo2TMgLyat/lNOk6tCTnUKttn5CjVrlbDlq" +
                "7Hqfi9ClZWe4BCoPvD/22w5BcszYuoPua/zyz1ru0vZpt/Jjve" +
                "l95A+kej9mIi0b8viNG36zkyTxtXiy9K94Xj3nGgIAtp+KtCBx" +
                "rwo2TMgLya1x2h49SaekXMilGqXa2GLV0PssuoqNz6Nev+qXU8" +
                "9cYh+/dP9Hs69mqn7oazeO9b4vdPWfVy//7pOHuZjE/6yb22uj" +
                "tq1E875rmf5nhXI42nb1Q6nrrz3E/daseTe5N7E1CQ+R+7d/h+" +
                "oQMN+FEyZkBeyztMx8kVREy6ioxSR6FWk9ow7YPZkh7VcuvXDB" +
                "6m55bmqv/n5xZEX+1zi7NbPUfz7gGQUafy5izFLPactVpwq+ol" +
                "Y3J2B5vN9WRKI7FV1y1Ov2jxbL8uQbcAbLIXysWO8DuW3u2neZ" +
                "sPYqYjxO9986A1+9iicd6F365qHef3u9nMu+Ds4us4W1XNOq7g" +
                "iP/DNVgRrFTudw/G2n5zP+H78aB3Rwrelu6noC84r1MPH82B4x" +
                "1Re2fW/S64QJEugn4KpF+oBEvDRyTpnJguUvspqAc5/ns4uDBQ" +
                "vpHxfiGo1E8P6TakxjdqRmu41xxjz5n24hxIpmgx7yjMur+5Om" +
                "Xxnha0mezPkAMbUvpo7rBZjWiezhMle3EOJFN0+H0TZt3fXN2G" +
                "C79HaGY+aejfS9kinNOzWJ9utVo3z6ae80aB93Svi6b2U3Ax2G" +
                "QvlIkV9bKS7wlfp/nelfyd8kHMNKLwMbPNXD2vRf6dfah9i+pY" +
                "vmUTtuCSfN7Wz/f2or50BLt8bp/vmjvTXA/VOgvidXqEzdt67e" +
                "uK+tIRweo57qd701wP1XoL4vV6hO7tnMp57esL9NN6c0TeeiU+" +
                "yS/F15p8Z9UcLdzHo3ODLLgsG3cq5tKKx8/i5mKkghPS8MPNxe" +
                "ABOpWTZRGt5gQOtWoe3QI50JPLzjG1mppJrYbNOabXQ4Rybf1K" +
                "0tcsJHeNu6ZWEzR+h7BG/EX9tFfoQAN++McGVYvwBl7N65yk8u" +
                "g19YqYFaNUu1oNW7oeZJdRUbn1a9b9e+P1qoS7usKZc4131lyt" +
                "F8GV5B6x6nr/BfHJqfo=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 2555;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW22oHFcZ3vgVUGiLklYUpJha1Ir/IpSC3b07YxCVJrlJbp" +
                "uoxNofQlAoCgb/tHdn1mXXP8mPUsEfgkaJtaTtLVULFYmKxdpo" +
                "QYyBltRINdabNkm9iTWp1jnz7jPv+XjPmbO7cxedyznzfj7PO2" +
                "fnnJmd2dtqLR9rldvyQ0X7yfJ7WuNtYQHS8s9a4rb8Y03+ddkf" +
                "L9rTQuQTrRm25UeD3l9JVq7eiH28kp6crIb8U3knT/NbK31rxX" +
                "TVpMeTd72ej+efnH6c8oWg92PiOEVWnyfT1dR7ANLggdb/yJZ/" +
                "JujdK1mzD61zTTshDb/fJG7yt/mO0yx8oa1z0NwX4/QodLJ1dp" +
                "j+4jPbX2XvsLNhsfElZt1n50lRqEnFFvYd/uPRe7kSk91fc/7Z" +
                "5WP556V1vPsNex3P75x+HU/+Nd91XOZz1/H8c/F1ZEfK/sEyfz" +
                "xO2eFheY3KfqitXd61MftutlLuv5Mddccp+0Hv6vTNYl4xt7OH" +
                "LNtjTtQ7givq28ZR2nqaPaLzZd8TRizyepc9nFWfUvtVaiXrvm" +
                "rePQmfHsX6ZFv6Br9Px/Th2zFcs5xBfDHV+mNsT+caauU48/r0" +
                "W/j0KNYnXMf/HVgbr5HlUAzXLGcQX0y1/hjb09lIzZh3/eHv4N" +
                "OjWJ9wnP4SqHSjLIdiuGY5g/hiqvXHhLJ5HR/+0VkJou7dhPXp" +
                "Qu/q5KXp1/G49ckapxq+6PXppeyceB/7aVeafZsNK5x977uaZ/" +
                "SsV5epWevTs/DpUayHt+Fz1uf71zC/JFdnzBfdGK5Zroj4Yqr1" +
                "x4Syed7l1tW0szcwx/eGLb390fe7e+Vx8sfKdeWH1/l7S3UXMi" +
                "zm5fBlrao7Akd3R50l+XvtGXi+jkXmkDPq+WYcpwerutesqrYF" +
                "Kt5WZ0nWasfpYh2LzCFn1PNN/K3A8/xpeKnJ50/J+Tl/bznf7P" +
                "On7pnuGfTQSRteIRv8vOcM1inOxNRxkwsujh7POaybVblVcDTY" +
                "uCk+t1odzR0BqQI33r7epQepVWf5bYEZcFvY0rsneT1yLgks6c" +
                "FQrFxXLF/0ffJNyU3ooZM2+jDZYEGcpDMCyxbua3KeyWkzMipn" +
                "mX6TTWuvyTGM5o6AVIEbb59P+Upzn0bvnnk/z+x9bX2vd70c0m" +
                "iLdZYvBmbLYp0leTly3i1OcL1b9GfE8kXPu63JVvTQSRt9lGyw" +
                "IE7SGYFlC/esnGdy2ozjSm7Vs0y/yaa1s3IMH7M7AlIFlbaULB" +
                "VS2Ze2JforpAWywYI4/sv2mx6KhmzhXpRwHE6LkVE5y/SbbFq7" +
                "KMcwWjVOFrZ9zHa8/b1lZD/h3hmYATvrLEnk98IQixwrZ8TyTb" +
                "p1Dpn7YpwWobPNlLP9PhQ/vuTx+3gd16P0mjqH5HWc/HovVxJi" +
                "1z3JLckt6KGTNtpFNlgQJ+mMwLKJm75FzjM5bUZG5SzTb7Jxc/" +
                "mArlclYdvHPNa2JcX3I+pL2zb6K8ZpH9lgQRz/FeuT4aFoyBbu" +
                "ixKOzWkzMipnmX6TTWsvyjGMVo2ThW0fsx0//txXq7P9903O63" +
                "Tjet3JyM/p1oOv8xFq1jr+Bfj0KNal9SkwTm8K80tyKIZrljOI" +
                "z4fmQw55fM8LRl+ynxfkJ2d4XnBuzs8LzjX7vCAwTl9u9LnKK3" +
                "Mep1fixil/foLn41eoWfPuK/DpUaxPNu+Sf4T5JTkUwzXLGcTn" +
                "Q/Mh+z3pM+kz6KGTNvoq2WBBnKQzAssmbvJPOc/ktBkZlbNMv8" +
                "nGzeUDul6VhG0fMyK88+5Ak/MufeN8553M18T6ZM+79O4pj+pp" +
                "8Z56hue++Zmg96w4TndHrk+rM6/j2Szn0yi31vHVOa/jqzHjlF" +
                "+KXl/vSu5CD5200TfJBgviJJ0RWLZwX5DzTE6bkVE5y/SbbFp7" +
                "QY5hNHcEpArceOd6d9S669oTuFfbU2dJ/hN5z7tngvvjPf6MWL" +
                "7o9y2Xu5fRQydt9BTZYEGcpDMCyyZuckbOMzltRkblLNNvsnFz" +
                "+YCuVyVh28dsx4+zXnel8ad3e+CTvb3OkkSuBCEWOVbOSC61Gt" +
                "686/hvGr0fvzLndfxK3PWu/9Ypa6rGqW/9rqizPfDJbq+zpLHn" +
                "yPYJzqft/oy01fzW2UKtfN9SfUfuXwWfHtXZMuX7ilfD/JIciu" +
                "Ga5Qzii6nWH6N7use7x9FDJ21wlGywIE7SGYFlE1fGsTltRkbl" +
                "LNNvsnHLluQYRnNHQKqg0k50TxjreKmrNniEPNARx1aKZsnEEK" +
                "+uJ4BvxjCKHifXptfkWrENHrbrNP1ydbJFOh5t3r3dOgt3B87d" +
                "3fWWyDVg96SxcsbgiWbXpu7J7kn00Ekb/IhssCBO0hmBZRNXxr" +
                "E5bUZG5SzTb7JxGzwmxzCaOwJSBRzhuy9Ib27yvmCmO5dpnhfc" +
                "3Ozzgu793fsXLlDfapFEmpLzb7EFcZLOCJBZUkg6MvmYhTXyuS" +
                "yDx/Us068fAe2pyXy9b4OPmMGGCLXHqMBCWrnfgL67oTv+/Wch" +
                "bWBfKR1mGTG6BL9pBZKda7LIqJH6YfDjGGRcM9s9vqnvx//Q6H" +
                "O6++Y87+5r+jmd93r3TuvqssuO1f7/zvHZlv6JyGvYrgmud7v8" +
                "GYOfttZ108bJen/YWQpUvFRnGfw88tgFlv67Q7FyXYNjDd8XHO" +
                "keaf+J+laLJNKUrFsQJ+mMAJklhWTiECv7dEwzklE5y/TrR0B7" +
                "ajKfQkeGksCGCLXHqMAC9iJ2BX17BetTe4Wt1Xsa65eIrOuxrp" +
                "WQxTc/K67dzyLrug1MEq4d6R7fdO8RRn+uy5Tf38nvEQa/nO97" +
                "hMEvmn2PkFyfXN8+VY7sKdWUrqztU0omi9LJRhnoKZok9gODrK" +
                "onZLARFvLYzqjwMzbXCj/q0I+A9tSobq4WGUADM6Mzom2pjuc0" +
                "+vZp/N9r+zRbq3O2kPsfMHVdsjOgE7I4C0pf//2mzY4p1/L3st" +
                "5/X6F/UMoBE6rv3+gy2sfX3xx1Pl2XXLewpvaqX1hTOmlKJovS" +
                "EUEy9RRNEvuBQVbVEzLYCIv+yEc6o7IfOtcKv964MjCheq4W0U" +
                "AD88KaPhbu2FRVb06K8aS+tG2mv2KVT9GzjWX1xz6SKB9oJq6M" +
                "o+y6BgyThRFNVr12s3VTlw/oelWoC9huBXZ8df+0r0L4hHT+6f" +
                "OOI+RY4/7DE5EdsuednMnzrpCdeeegHhxn3VhfV+S8uyG5of08" +
                "9cVsLSXSlKxbECfpjACZJYVk4hAr+3S7GcmonGX6+QjASM3lAz" +
                "oy2uUverhajuGKWEtX09XOgULepPrOAaWrXx92DiiZLPRrRIrD" +
                "LxPho3jlgR8YZFU9IVMW8NPVezuKk+2MijjGHn9n07ioJZvU7w" +
                "65MjCheq4WGUADM6Prv7kcn0Ob9Gzf9+DBU//Pz5++fm3D34P/" +
                "C/u2Wpc=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1882;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW91rHFUUn6c+NGAffOmT/4ESm6StlpjdmVnS+OZHngTRQt" +
                "FaVBTUR7OZLIvdl4JWLVHyaK3WqvWrIKTVtiIYRFEQVKTV1I2a" +
                "NvogVSk4d8+eOefce3dnnb17684yN+fz9ztz9t67s7MkCIIg2h" +
                "m0x2hndXcQgEZWPJJDgTjIB1LyvJ5BSLpVsthRe9O5DZlsuHqk" +
                "eX3djmg5Wi6twRgEIIGmZG7BOJtOCCiTpJAkDrCSj2PKSEKlLO" +
                "mnK0BGOE0+NSp0zFASsiE2xPCKUFN/w1Ucw9X57dC/cLU2ls6Q" +
                "F8GHNtlju05W1AGZ4upnKULHMFHTKg5395MNmWy46phfoki92r" +
                "yjelpoN6B0oJmXmey34n1ms8bvBIWPhZ+7en/rna96MpM+aWf/" +
                "2uO6O5++JmFMNZDAlsrCcr4dfZ7FCAnwkicxNkOf7IgDPmaXkV" +
                "ldPEv46Qpa0mR2GnyIDlryhJKQDbEhhl8nasbM3I5SbUex+WQ/" +
                "4lcDr4drvtJ6aZ1GkNQrqUVlpasRX+QFiXxRGbIlJkqt98SCA3" +
                "aJivlkwwi0SyTJhmdUNvmoQqoK6+KcsgI9PptPl7L9ad3lfPJ9" +
                "1Hr8vJ//vSD+rnYPagcuOV0Hb3hedz3y1SYL9qmUzaeN/83cKB" +
                "fImeoxLuxxf9oobZSfwhEk0JQcBGThXp4HkrKoETNARySJA9mA" +
                "Dplkl5GgQwTGSSSqBBnhNPkQHTP0anVMTbtcupxK7REk0KJpNS" +
                "b70CK9alQR8IqmIbvde4HW3i+nTRywS1TM53UQomSlaGTDM5o2" +
                "+ahCqgrr4pyyAj0+29fuw/2pVnG6X7zveX9yzFceK49FN+EIEm" +
                "hKTvudWbiX54GkLGrEDKVjlLJzHLADOmSSXUaCDhEYJ5GoEqgA" +
                "T5MP0XlVvFodU2rYr7n2d67kh+wdecnW17mCn4K1ezr75q7m58" +
                "+tMPnr9PwuZz61qq9u6QH5YsdvdA91uZ+g+/F7na6D457XnWO+" +
                "aC1aK30PY7pjtSTQlMwtGGfTCQFlkhSSxAFW8nG7jCRUypJ+ug" +
                "JkhNPkQ3TMUBKyITbEUEWktWKblb2VvWpUZxCos9QsNcEH/cQI" +
                "9KsRfBBNsZSBuMoCyMZdScuHeGiTkRCBNkTldWAOXQPiUpSORt" +
                "eHUTTCX+Lstu7Sz7v7na6DtzyvuwHzVY5lK3Imd83OFIntzd45" +
                "Inmmh91kJh+3V34Wuw3HaFvlNNrIKuNsOo81owDZzmzaO7PYdW" +
                "5DJhuuHmmvttsRNnEMm7UH0ab0hcWwqcfZdEKwWQHZzmzaTZb6" +
                "qW5+bkMmG277uW9TMofNYuuO3Rc4fWoSv+Z5fxowH9vHH3Fa93" +
                "ue+zRgvtpjmfS407qPee7TAPgqdRwr9epBtIEOPh5n0wnBZgUk" +
                "O7NpN1mkxYaENmSy4UoslOxx+Qf7XargE1Hb71LzW+JXir+Pyf" +
                "Vdd4oR63zK4cPfpYoerE9/GBVd10efjnru09EB9+lg9nz8Sj84" +
                "B/7S6n7X8/40YD42n646RL0QH+kj+88CfToy4D5l8ykey10P2h" +
                "1W9bkudb/teT455gu3hltpBAm0xs1qJIv0qjHZTzpkS0yUWnW/" +
                "aeLweMrhjBgjs6SXs9Fp8lGFnFtic0ydwT6f0j5tdzqfXvc8nx" +
                "zzxbPxLI6og9bYATa0YBy9kv3SA9GEKXBP2HB0Tp2RUClL+iUb" +
                "O0/YYwjN7ICtAjNe38cbtwRDfNQ/d75zt5+kVI+n5wfUp3gxi1" +
                "iGvwvfaJnst5/qp61xxX7/VP2wrwpPdPWes66TRWvsyaL3T/Ge" +
                "eA+OqKtXtDnaHO9ZWCQLxtl0QiBZ4io0Wx7nhByTpf4Fz5J+yU" +
                "anyafG+SW6ZrMDtgrMeGPd3fZf9/Gu6+BLz+vutK/7zMaUyz6F" +
                "5/z2yT1fp/0pfFrfn2ovDM/+RNV3359qh/qeT+WUbcXZ+7viN9" +
                "eeU7yKsBSWaAQJtMZuNZJFetWY3o9nOmRLTJRaz3kOmzg8nnI4" +
                "I8bILOnlbHSafFQh55bYHFNn0ObTUjafZvqZP43btX31J8/7+I" +
                "+O97uRcIRGkEBr3B2ONO4ii/TqOmRLTJRa8+lZE4fHUw5nxBiZ" +
                "Jb2cjU6Tjyrk3PqV6BWY8cZ8usPps+WXc2fgnX75Cswp9n8b+D" +
                "04XG3MDuz/Ni5SxLX+v41+nz+l7+8DTt/fj/zuT4PmY33a57Tu" +
                "s577dNZbnx52eT9eOeO5T2e89elRp9/v1jzfFzR99akyP8zPnw" +
                "ZdPfWp/ssw9yn5ytfzgnjZ3bqrXuirpgK/S9Uv+ZpP8cfDPJ8G" +
                "XT3r06lh7lP9sp8+RZuiTS5x3aJde76sT1PRlNO6pzz3acrXug" +
                "tvHOZ1N+jq2X3BxjD3KfnWV5/Ktw5znwZdPevTrr4+b65c4z7t" +
                "8rbu/hnq+4K/PX3ejUajxmfIaM5nzGjnqLxc5593o/35e+7TeD" +
                "RuYI/ncI93jsrLdd6n8f78PfdpIpowsCdyuCc6R+XlOu/TRH9+" +
                "4/gXCP0CPw==");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1646;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXEtvHEUQHiQkkHCCUJARsYQwlwgpHJC5ICNRu/aKExLiwJ" +
                "2zpWDimISEkHgfzkh74RIuOXDhcQuHTWKw18/Yjp2QQMzBSJAE" +
                "ofwABD8AZnfSU9291b1TPb0tZVYzXd31VdWX2uqenp0kUaQec1" +
                "8IqTkd9TlqU1HuY/KPKOgx6HhSno575X0/cJ7uh8nTxOjEqK7r" +
                "HaH0iJLx/Wx9H/m4eqmnWa/z7l7gevIeb279Uft9cv4w91KWp5" +
                "MZYtVguSDJu93r7eT8iUC246cLMGxZtdvU6IV1EvtjJt3wU0+1" +
                "xsR4T/1c7FNfX3Yrepys8/HA8268mD73+jQyMRJF9UuK75E+sU" +
                "dUlIzvZ2s74qcc8kTGq37lg4+2Pp32+f3GQ2HrKX4m2L7grE+/" +
                "5SfC5mnQ8aR1vNpTtQdzeSDW8eqzpf/62TVrxnXvkM2uSlZOv3" +
                "i+1vHkGzkSPcbHoNljnuIDj3Oe6lGoPDVjr+vqwbB5mn8vWJ4u" +
                "ed2Pnwu8Hz8XJk+1RvPrIn6a32i85wLnaS5Ynr71yvuzwHkacL" +
                "zGRpanJa/z7uPAefIcr3KscgyvqdT5JHla7fRxRNamUm0K+6m1" +
                "6lNIXd4zvX5kPNrIEQVGtVK1cjQ8e+MhQzm26lv2qUfQ5l22rj" +
                "Rve62n6cD1NOB4Yj+e1NPPXvN0PnCePMdr7Gj9G1mefvGapxNE" +
                "7O0B5umE5/VprDKG11TqfJI87XX6OCJrUylZn7J+aq36FFKX9+" +
                "e9fmQ82sgRBUa1UrVyNDx74yFDObbqW/ap9mAGZqJIXFMp7TV/" +
                "7VxxRNV2rrUp7KfW6aF6e5Sn2V4/Mh5t5IgCo1qpWjkanr3xkK" +
                "EcW/Ut+9QjGPdP+17nwZnA69OZYOv4b155fxo4T97jGd8jZG/A" +
                "fLxHmPyoAEOH9wh0vN73CPUHjve/vaye+r5RZd3vzg6qchp3w8" +
                "SDHWxhB7J9QvycjjBZopz6oJAmHxyGHG3tdzuWz4eed0k9PfA6" +
                "704Fnnen8s07xre1jy3sQ3aXa/6pI0yWKKc+KOTkyQL1tM/X0v" +
                "EQC8y7eeVO5Y64in7nk9TTvXRMjAgcfpJ9pqJJ0ehT9jt5mvKj" +
                "xkxt9CgqS52FGg3P3njCu8yK8q3/mQXCNO+kavUx76qB513V97" +
                "wz3e8872c+CXy/8x4vUD0V+NtUtWmHejrufR2/hi1cE71kVs7r" +
                "CJOlDSO0dn0+hhwtss/vqQ+LBWxhAbIaiV/UESZLG0Zo7fp8DD" +
                "na+qt8T27PLS7zzhijXfon7DoeD9vnXeFVcW8wq23p77Dr+IXv" +
                "vD+3XMcWroteFJWf1xEmS1Wm0LJfd4YcLbKnsXw+sIktbIpeUr" +
                "mHdYTJUpUptOzXIU+bfG39qB3L5wPL2MKy6CXfyKyOMFmqMoWW" +
                "/TrkaZmvLc/asXw+sI0tbEO2Kpbf0REmS1Wm0LJfhzxt87XIns" +
                "by+cAatrAmesm8e1lHmCxVmULLfh3ytMbX1t+0Y/l8YAVbWBG9" +
                "5Bt5RUeYLFWZQst+HfK0wtciexrL5+N7/0Q/t5T+Dbx/esH3c4" +
                "spT/GozzwVegJ1yFP9Le/PdxvYwoboJZU7piNMlqpMoWW/DvNu" +
                "g68tj9mxfD6whC0sQfa3ecqv6wiTpSpTaNmvQ56W+FpkT2P5fG" +
                "AdW1iH7N+FxEd1hMlSlSm07NchT+t8bf19O5bPB1axhVXI1qLy" +
                "uzrCZKnKFFr265CnVb4W2dNYPh9YxBYWRS+pp9d0hMlSlSm07N" +
                "chT4t8bf0DO5bPB9rYQhvaZoRtHGXaAxS449ltaW2pYccKqZT7" +
                "923Ywha2RC/xsKYjTJaqTKFlvw552uJrS2t2LJ8P7GILu5Dtg+" +
                "K3dYTJEuXUB4WE3QJ52uVr6+ftWD4ff/vxyrD5PUJlOOw+szKc" +
                "b5+Zn5e/PNUnjDHa8ZGweZq/aM9TVneTuau6hS20oGVG2MY7cv" +
                "lDcwxoFZh3Lb6W5lKEhXoU+X28cshNV5Dx3SLxXHkVytOQm24g" +
                "eRoqzlmrxVvYwi3RoxC28Y5cnk59UEiTDw5DjrY8bcfy+cBNbO" +
                "Gm6FEI23jX+nLqg0KafHAYcrRw2Y7l8/H5O53l/3l4I/DvdGOh" +
                "fqdzyZN5X1B6Mmye6HhF3gfDFWzhiuhRCNt41/qhOYbJB4chR0" +
                "tzKcTiKrZwVfQohG28a/2XOYbJB4chR0tzKcDifyoC21U=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1781;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW01oXFUUHkW6CbSL7qpk1a5LuwkECvNe3qhJVm4qKJrSpq" +
                "uhC2nIwiTWycvP6yK0CLoLUhB/EkpLXFRLUURQpCq20UUpIqgL" +
                "RdJUEFG04My7c+a7597zZt7fvLzUN9yTc8495/vOu+/e9zeTSo" +
                "VvjcFK7M2vx48NjlYK3YIjeSM2Pmn/vdJsH0jj1Pg4IvOapn9R" +
                "qbhnGl81tS+FyBuLWxkqfL9r72eS1z0jxn7Y0T5Xfxd+SFfT4j" +
                "+kOZ9mGf2F2S59LyecG8PJ+bNVH2OcHnSYvstz3RW99a4+4yoc" +
                "7AfqfD0YS5/t30hxfhrNdw+8C94FkmSTRRI+6NwGAnSOW30g53" +
                "FOkxGoyOL9nA3N5iN0vSoJ29xnM749ny6ys+HdvI5HFqQ0uXJO" +
                "liryu95V16Kvd5nOBSmud9W1eNe7nTg/Va9Eov6YqaY/86xl58" +
                "/jbr084+TWixyn6ka0JRzDjejrXZaaar+mmE8b5Z1PwfPlmU/B" +
                "c+Udp+rlEp2fLvd3nBbYdSJ4kVkv9Kjt7fLcj/e7lm7zqfF3j9" +
                "p+2on59OqBZLX0Y5yCE8ya6DFOd0q07u7s3Hya39ujtndCBOE+" +
                "c35flpr8/V2fHQeia+mynzt5n3m1RPPpav5zyF0l6a7S8527qm" +
                "zVp8dJNhA4mo4k55mYEgv32BnwEZOEy7FIk+NiHLuL/VnPwWSx" +
                "17vl/UWdn9yZxPNypkTPLTP5jou34q2QJJssb8Vdh4fiJBsI0D" +
                "mujGNyEgZncdf1LN7P2dBUjhnT2h9elT4CUgV2vDWfZhMfw9n+" +
                "PN+leZ+ZvPq06y54KUfUwtedf7eocQqPyloZnkHSVCHnZNmfqP" +
                "eZ7lAnIub7zKYsz/d3Q2Js6u/v3Dl3DlJpylI2PLy3Jf26Ha1j" +
                "kgYvxzF7bEaK4Vm8V2dDC86afKhQ5+bYOqbJkN/78W7zKZgqdj" +
                "4FA/HmU9ytdr52niTZynKPKR95KE6ygQCd48o4JqfJCFRk8X7O" +
                "htaqXooBmj0CUgV2vHW9m67s4m3hWr54S0ej1l0wZ667pSPp19" +
                "3S4WLXnXxfkGHd3azdhFSaspQNj95r2oQADF0LR/0VOU9nsBmB" +
                "iizez9nQlvfIMUCzR4DzmezGiL/ZZe4NZXgO9gt+Dt7Th/u42y" +
                "Td24375FO26tPjJBsIHE1HkvNMTImFe+wM+IhJwuVYpMlxwro7" +
                "VzsHqTRlKRse3tuSft2O1jFJC+u5buPo8cjRGVu2e12vUq8C0c" +
                "SGZvOhQp07eC0K02Qw1t39BM9QCZ5uR54pdt3F5Qveyvrc4kzl" +
                "eZ+Z6Zp8LPn1zpnK93rnXfIukSSbLJL08V+Hjjxo/hutvyPT5N" +
                "FxldfO45wmIypBFu9HH282H6Era2Rar0vH5hWAPeo+0zmb37or" +
                "/r1K7+oz3neOdFbuR7v5fnzx0ZzvCY67xyGVpixlw8N7W9Kv29" +
                "E6Jmnh3H3MxtHjkaMzUgzP4r06G5rNhwp1bo6tY5oMxhz/t3xz" +
                "I9hMse5O5BvnDXvDJMkmiyR80LkNBOgcV8YxOU1GoCKL93M2NG" +
                "dSjgFae5wmTWyzVh7fOT89VXkotqUnY8Y9HfN+/F7tHkmyySIJ" +
                "H3RuAwE6x5VxTE6TEajI4v2cDc05JccArT2fTpnYZq1ta7u23d" +
                "RCGfq21UfpuofiJBsI0DmujGNymoxARRbv52xozkk5BmjtcTpp" +
                "Ypu18nj5+5a8tuJ/rxJ8G/MakfL/Ox6WcXImYl7vJv7n43Q65j" +
                "idzjpOzkGror2xEMLn4MXx6HFaHC1gnA72qDLr78Q6v+txer7R" +
                "LvX/lR3uL36/1p3zeMHn8e/z3QPvlneLJNlkkYQPOreBAJ3jOk" +
                "/IeZzTZAQqsng/Z0MLBuQYoNkjIFVgx1vnp8HKLt7yrz7y+7uf" +
                "83yfWfTvMBafjfc+M+7vMLxNb5Mk2WSRhA86t4EAnePKOCanyQ" +
                "hUZPF+zoYW/CbHAM0eAamCjjXqNa/SSoa+UfVRuu6hOMkGAnSO" +
                "W3tPzuOcJiNQkcX7ORuazUfoelUStrnPbWvca97pKBn6xtVH6b" +
                "qH4iQbCNA5bnBPzuOcJiNQkcX7ORtasCXHAK0zTga2uc9ta8wb" +
                "a2qhDH1j6qN03UNxkg0E6By3ti7ncU6TEajI4v2cDc3mI3S9Kg" +
                "nb3Gczvn03cqBP7yT/KPZ6t/xLzu/HD7mHIJWmLGXDw3tNm6J1" +
                "TNLCY/K1jaPHI0dnpBiexXt1NjSbDxXq3Oae2PtsxiefT0meW7" +
                "xvip1PefN5Q94QSbLJIgkfdG4DATrHDf6S8zinyQhUZPF+zoa2" +
                "/IgcAzR7BKQK7Pjez3dJ3heweff7/D5vqdjnu158cd8X+Fv+dv" +
                "f3BfmuO+fdgp9b8ub7D2rK2ck=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1750;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWt1rHFUUX30yBgzBj9KHhCIELKJBhb4oyN7dUWJSyYMvIg" +
                "hKn0wr2L5IqeLuTj4GQQs2VM1DXoTSQvDBhyoURAgWob5EUUvf" +
                "art/grDVB3fm7Nlzzr13JvNx98Z1lrlzPn+/M2funUxmt1aTW2" +
                "u2NoKtdbv+VYXsv4rnVOHLVdOo+nTFc5+ujGefNu757dNGb0zn" +
                "02XP8+nymPbpkuc+XfLVJ9Vw2adK2SX65LJ6n/Op+affPlXhS6" +
                "nih8Hx6/7+ra1Pre9TMq8OZ/nZ1k+J5ef+fsMSea18fX3sbzLr" +
                "/9GeZY39bihdLzFHt3BUW9gntUVWGWfTeaxp7aPetuMgix0VdT" +
                "mfzAyywTnYcSWf7fzKrbvgnSqzVGb7vz9Vq75Qn07qvvZDuRBu" +
                "mNntqSo1dR7O8rYnrX06uU+V1531aaXSfFo54Pm04m0+narUp1" +
                "MH3KdT7nuj7uKo7rbOc1vnczhy2346WVEHZPY/xd8UoWOYqP0q" +
                "vsj2kw2ZbLjJGt2mSHvEgT8/XfT8/HTR17pz3KcLnvt0YUz7tO" +
                "m5T5u++tS85rJPvu/jLqtP7mcLaqFxP44ggRbL4RZZuJfngRRb" +
                "4rFWAyvoiIRWnh0fwcftMhL06DGeJZGoEmSE3eSLx/Y28gEzr1" +
                "bHlFpy7OGoeq1/Bv3rxXq4pdj7LqW9+yKdEGxWQLZepZ5pN1mi" +
                "mSw/tyGTDXfw964nmVXJ93nqY1Mqnmvx7da8bq75VNj/dIcjSK" +
                "D15ehxZmFekZdIiaWbIHaHEYjbRavIDhNrFzWwy0jQOyd4lkSi" +
                "SoaMXaxe8sVjfz51hxl6tTqm0Iy+rZlS6v9dK2m5Tq/k1VFmRU" +
                "8XeMbfwDHYwOfxYAN08PE4m04INisg2ZlNu8kiLTYktCGTDVdi" +
                "oWSPy3EdPjWl4rkut/VHymRF8yO+/33CJbVTLtfi26lQ046rnC" +
                "pV5H/vu6r9z9Ri94D93vtGz1SosMR733XrOyvzve/am+Vqaj8F" +
                "x8Z0Yzp+fnI1T2O0slv0hCu+9nbp90SdoEMjSPGnMdGYCDrRk2" +
                "ThXp4HEmRLTJSSuidMHB5POZwRY2SW9HI22k2+eIQ+cW79TPQK" +
                "zHg5n5zf91qenzMd87V+1/RfCub/kXPtPGfJ3SvI9WuBtfqs47" +
                "5Pq2kaQQINdLJIr65jNMdEKZm7X5o4PJ5yOCPGyCzp5Wy0m3xU" +
                "IefWz8Q8Z9CqzKf4eTzvfFr9zfN8et7tfGoeax7DEXXUcCQbyV" +
                "InBJIlrh1H59QZCZWypF+y0R69YI8hNLMDtgqG2lJzqS8lY2Jb" +
                "gg/I3IJxNp0QSJa4dhydU2ckVMqSfslGe3DOHkNog/vBOR1brx" +
                "U0dUad6a/AwQgSaKCTRXp1HaMxg9C4VeLoHpMRY2SW9HI22oP3" +
                "dT6qkHNDnA1T006r031pMIIEGuhkkV5dx2jMIDRulTi6x2TEGJ" +
                "klvZyN9kjpfFQh544aaZg6g4/vEfxvwQdu41SgAhpBAg10skiv" +
                "rmM0x0SJrBJH95iMGCOzpJez0R58qPNRhZwb4myYOsP/dD6ddR" +
                "tn9On8cOUujHOf1u+5xVNH1BEaQQItWoxHskivrkO2xESJrBJH" +
                "9yAGRWKMzJJezkZ79IrORxVybv1MzHPW4/X5pB4Y5/nkuvrweF" +
                "gX+ssV7gmPprIspPty1KiyvGufFaulXFzcpzAIXzT7pB4sfD6N" +
                "9D5VupaZfQpfss6nnNWHzZz9nIEP6RXm00wWyyhw0715+XLHzc" +
                "KH9ArnM5vFMgrcdG9evrxxLu9PGSwLzVujWncp70FuOT6D1PtT" +
                "ies+mXEfn/R7f8rLV7Yuei4ovrUXM67vTb/PBa751LyapxGk+N" +
                "OYa8yp+fh7KbTENpR4Hkix3JiLjxAFOvrQyrPVPK+BKuCMoEfH" +
                "ZZb08tppN/niEb5vgXMxOyAxdYbU58yjLueT9+fMo6PFb786uP" +
                "6HG4f7V3KZ+2JL1gZ+iuLx++Vmvoc/UTzHzkffcxatRy2qRRpB" +
                "Ag10skivrmM0x0QpuV+8a+LweMrhjBgjs6SXs9Fu8lGFnFs/E/" +
                "Oc9Xg5n1xv0Wue3xfc5xrRz+8wKlVY4ncY0RvWWON3GKub/60+" +
                "qdf99snOV/33KvU78ohSPJJNyukoZlw6Rv1ONibWILHNem18fC" +
                "zObvPUb8ojt3NbPfP5Ld2b5anneCbkUbymtFyw89FeSRa79KSt" +
                "O7Wsr7vw7QrrbtnzulvOt+7Ct9zfnwy71qfgUHqfgkN++2TnM/" +
                "uUv660PtV3Xc4n33/v6ru+5lN9b5yfC+p7B/dcsO+6m8pYd1Oe" +
                "191UznU3VbVP0XvjPJ/Cjxw/P/0LGWHpTA==");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 2104;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02MFEUUHg5evUA8GMIBvQCJF8OBALLT3XsgHPg1QQjRRB" +
                "ONG7zp8md0Z3ZnHONBDnozIrLIclCjBN2IgSX+RNGDgtmwMUD0" +
                "5MWEiyFho3b36zffe69qZmd7ejoL3ana9/t9r2qqa7pnZyoVfd" +
                "T2VnIfww/m8w3i6JWv17hqvVpHTxJppMOivVZP5OGVGpOltJ6V" +
                "Lo6sATmSkWN0lvZKNjSXj9FlVRznw7QM+ddTfaT32GBfuetp0H" +
                "yNi22m7YXWvb3kefLytZ4tCr/2TPtq+NFhebF3nPFjXXxHBj9P" +
                "bvX9HdFsNMs966RVvyUbW6LZ1kuQkacRWB4/pnElzvgR5GlOyw" +
                "hUsGk/fLol1bsxrYNAc2fAV4EbT8fYqkG8urWR6HQf2QdzvP6n" +
                "C96P/mxcbfzSuBXP0Cdx+7LxmxszdnFhnLEf2oh/eFhmo1N91H" +
                "itq/eGd54W4GvcTPvf89Y0mPUU1/1huft48XxjM9nfdD355qnT" +
                "ehr7wq6nsZ/j9pMn8kLrcB8Vft7V+53P2lzujZ1uS99n7yk3c7" +
                "6fHnClAt6lD5SdXWT15V13tZG+snPs40P/FhsX7A/2oyeJtERu" +
                "rIZFeq3OCMCQUmq/4s/jGpDjsjS+kVnar9lEu+KLqb2PMbszIE" +
                "duNWc9vd3H+9LfXV6P30q+Hy+YLxgNRtGTRFoqz8GivVanbI3J" +
                "Umqbc3FkPHIkY6bP6SzjFWyiOXw8HlmVngGNaRnMM8XZwby+rW" +
                "PlrqfmyoLX07pgHXqSSCMdFu21OkdLTJbSeXrVxZHxyJGMHKOz" +
                "tFeyoTXXWj5UKLntSNwx2/hsf5ofzOcqZR8TBe9P0ZpoDfess8" +
                "Y9bJC1TtLQGcphi8T141hOy4hKkKX9mg1t6Iw/BmjuDPgqaGtr" +
                "o7WxlPapbS2dJEsLx/l0koamKIctEtePYzktIypBlvZrNrShKX" +
                "8M0GRdEtvWyhElPbe8uTSfW5ba811fzwg55il4vuh5ijFPcB+c" +
                "qI2zjXTyyTifDgSNJpH8eRbTx6ItbgZszOTD1Vgs+eM8meuD9e" +
                "hJyrTjwfrWG7AYr9FT+bjGZCm1HXdxZA3IkYyk15/TWdor2URz" +
                "+JI+e24R3HYktoK2tjHYGEtZTxJppMOivVbnaM4AWnb/9JaLI+" +
                "ORIxk5Rmdpr2RDay63fKhQctuRuGO28dmz+Xg7/9Cir99DlSVz" +
                "FF9Lp3082HBP7+MbBvt+1/hvMO930VPlzpOfL/88RZuiTdyzzh" +
                "r3sEFOzvqI9nAOWxTuPh+O5bSMQEWW9ms20fb5Y4DmzoCvAtaC" +
                "PcGeeJVmPUmkkQ6L9lqdozkDaFk1kYsj45EjGTlGZ2mvZENz+V" +
                "Ch5LYjccds4wf7HBycK3kfP1dsXLQz2sk966xxDxtkrQMBssZt" +
                "vefP05yWEajI0n7NhtZc5o8BmjsDvgra2u5odyylfWrbTSfJ0s" +
                "JxOOP9SXk4hy0SN7jgw7GcljE5gwtAdKvQbGguH6PLquLX7wOL" +
                "bcecabuiXbGU9qltF50kSwvH+XQgQNa4wXl/nua0jMkZnAeiW4" +
                "VmQ3P5GF1WBa1TBW1tc7Q5ltI+tW2mk2Rp4Tic8XpSHs5hi8QN" +
                "LvlwLKdlTM7gEhDdKjQbmsvH6LIqaJ0qYG349vDtSoX65Egk1r" +
                "iHDbLWgQBZ47Ym/Xma0zICFVnar9nQmsv9MUDjw2LbMZMWXg4v" +
                "VyrUJ0ciscY9bJC1DoTwcv0Vtijcvf48zWAZpYXtNl6yibbXH8" +
                "No9Zez1+8ji23HnGkzYXw/Tn1qm6GTZGnhOJ8OBMgad/iaP08z" +
                "WEZpYbuNl2xoLh+PR1blw7ZjtvEL/988XOC7FOT3R4WHy71/6q" +
                "3W/HWFn3b2hGPdM8mPKBnfPXch7sVnMV8nzLZ/rFeekj4f/+xe" +
                "/3y89+uun+eWcE/J112PfK2Pl9Z6Cp8sdz35+ex6Gv9nqf2/JZ" +
                "jO/UQ7netzuunerrvmQ0tsf/qq5P1p5t78/124o+TrbkeP6+nR" +
                "4uep/k73eaq/m1Y46p2n0ZLnabT7PLE/b134Pl3z8eLefWqvDe" +
                "6d7fWHvYyF/zakpP3p63LXU+3Xwd4/1bp8ny48usA9y9HOUQvl" +
                "Fn7/dLQ3f966aov43uGi7jNL/l1Z0Xzh3fAu96yzFt4NTsGSaJ" +
                "CRB4miKUpK6f3MKX+e5mRezQJEnS1rt83l4/GgVtQlsXUFmKNy" +
                "9qdoWbn7k58v//4U3gnvuDrZwjvBSZKpJRrLHIF89gcn2UZ69r" +
                "qdlPH0F8zAg44IxpFcskkr+CQis6IilmSNeiZ0djgfzsdS2qe2" +
                "eTpJlhaOw1kf0R7OYYvEjR7w4VhOywhUZGm/ZkNz+RhdVuXDtm" +
                "POtNlQ/VaR9KRVXyAP6xwHK0VD0hje1TvL+DoGKDLOX5usybXy" +
                "IauXo+p6bc36LZTdaX9qXS1yf+rrDi/P893TBe9Pc+Ec96yTVn" +
                "2MbGwJ51o3ICNPI7A8sU3jSpyJrcjTnJYRqGDTfvh0S6r3xQDN" +
                "nQFfBW3teng9ltI+tV2nM2Z6hGxs4TifDgTIGtePYzktI1CRpf" +
                "2aDS2p3hcDNFmXxLa12vgsa7J9ha+2ntatritzUmoT2zpHTmwt" +
                "9A5w0sdvq8/2kpu5Wc6GZ7lnnTXuYYOsdSBA1rh+HOuxjNLCdh" +
                "tvfdSqK7rztedzhZuva9Xx7dF0/D17uMAv3cOCfwkvVsFfvfBq" +
                "/upQb9i9xt2v37OvBr293/njPK/GVDjFPeuscQ8bZK0DAbLG9e" +
                "NYj2WUFrbbeOujVt3Sna89n1vcfF2rjm/P86rKfXFUnyg2rvI/" +
                "i+BOGg==");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 2069;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW92rVFUUv2YEUoTWUz4IYr7UkyBlhXTPnHPoSX33oScfAl" +
                "/6Exyn7nRHg1LLQDSojL5QS6TiJmmfSPWWEb0IikgKQS+aodU5" +
                "Z501v7XWXjPOmTlz8roPe8/6/P3W3u7Zd9/DNVmdrJ4RLSl0sm" +
                "GEzfqS1doODK9xvMyTGcDiJ6xN1hRauc1ftnVqfz/uisQP6+1X" +
                "szZZm0nFWNjW0kOytHCcpwMBssb1cSynZQQqsrRfs6FHsR8DNG" +
                "oUJ7FtraW2LFmmVrDQ854+Rx7WOQ5WitZ2YLj7aRnj6xjWgMVP" +
                "WJusKbRyk9XLWQ1roR/Z8an41MwMjXnLJdZ4hA2y1oEAWePOX/" +
                "fzNINllBa223jJht5d4scAjZvFtnMutZPxyUwqxsJ2kh6SpYXj" +
                "8HS2aw/nsEXiRjc8HM0JDM/Cdhsv2dBDPp6PrMrDtnMutfPx+U" +
                "wqxsJ2nh6SpYXjPB0IkDVudN3P05yWEajI0n7Nhh7yMbqsysO2" +
                "cy61C/GFTCrGwnaBHpKlheM8HQiQNW70l5+nOS0jUJGl/ZoNPe" +
                "RjdFmVh23nbOOpdbfP3GYtutxc1uA2++fsnxhJIm3+Zj7Cor1W" +
                "p2yNyRKsGsd6GAORHKOztFeyoc/fsHyoUHLbmYRzJi1aH63PVr" +
                "8cSSJt/t98hEV7rU7Z5b+lQpNWjWM9jIFIjtFZ2ivZ0LsnLB8q" +
                "lNx2JuGcOaJ9muLbR7P+WXtV/wbS37ntL+nzxf16L7Y/FfKZYv" +
                "wp6z+Gu7b9xSR7vn18qPc7z5q637v2533p+3JOr41cxaB12mjX" +
                "Kci8nddp42jrVKGKQev01KJep6dGW6e5Z8eraecfLPWWzizi1r" +
                "truvhYp3Sin6y9u//fdUprvhdE66J1GEkiLZ3NR1i01+qUrTFZ" +
                "glXjWA9jIJJjdJb2Sjb0vPqwWokWroDG1Fp8Jb6S3TiLsbh7Xq" +
                "EnY3qabGzhOE8HAmSN6+NYTssIVGRpv2ZDz6v3YoAm65LYttZS" +
                "uxxnO5TGwnaZnozpSbKxheM8HQiQNa6PYzktI1CRpf2aDT2v3o" +
                "sBmqxLYttabXx5Pj3W/w1mZ63n6j3Nnk/dB6eL336TpdaOWuue" +
                "a3ad6q2+/A3nYrqQLuRj3rM9u5DbZi+Sr/z5UUawPx/JR9GIRQ" +
                "bj5hZCDplzH+OhGh1D7BpV1sE5mAPjIsqiYX4cJasgBq/icD8l" +
                "p61v7nGtdyq8W+h2m91PYfUT3jO2pltnD/NIEmm5b/YwLNIr80" +
                "in6OJf6DBHMG5ulzjpOc4in0Q1kVs1omZlXrIxI3UdhYqYjyTO" +
                "Rw3A1NqQc/zlWs/xe++sc1ys0yu17tvXG76P18yXLE+Wt36nMf" +
                "spUUik5bK0cJynA4FlSDmSxiFW+KRdRwIVWdqPGTAj9ZCP0Tkj" +
                "l5iNsSkGFUGz7ws69ztnew3vC9L9Db8v2N/Ue5V616m3otl18s" +
                "+nSdYp23vXeGxd43VqXcv13n3kk3GeDgTPSsg+c2gPWTrbhvml" +
                "jZk8XBtpqx1vP8XL4+w72XtA7qfuwfH3U7y82f3k84X7qXug4v" +
                "uVffpT2qXN+n2Uap7hmGGUrGlQLtnl6FcyjN3zRHv1p7RLm/X7" +
                "KNU8wzHDKFnToFyyy9GvZBi79bQu8di61D+fLsGq4zxdxoZWQn" +
                "ZPi0uhfTCLr0sbM3m4NjKc3+3w8651ptnzyee7/e8F6b7x16nz" +
                "/Bj3p3313wsU0qoK9Vd4X5C+0fDvLVPmS16VUm9lXait7+uoqc" +
                "Kd0OXrPVTbOu0pPw+yVMd+SvakByavqdJ+OjDl/bQ3lOpATQ/V" +
                "UVOFdTpU/9p03ivGj+T51Hmn/PxQnF3XByK83fmk+Hyrc8Txvt" +
                "8+n37t5r2b9aPGdiI4M68Oq35HeTZ0PhAYH0u+zuEJ1uZY5/jw" +
                "czz5qsa3WhP99VlnjL9OqLP6Am93sptH1llLdreOwsJxng4EyB" +
                "q3ddTP05yMoVkol+3ar9nQQz6ej6xKr4BXQRgf7Kdv6rwXNN1u" +
                "XX211v3ZrFP/bW/y7cwibvVXP+g+3nvY3se7v41/H5+ownHe0/" +
                "092n38hXOT3sejJxbzfqq/+obe+z7S8H56sKn9VGfb2fiJP98a" +
                "La63tuI+/VV/Sru0Wb+PUs0zHDOMkjUNyiW7HP1KhrFLT9pLez" +
                "yynj/xmnhN2us9Cktug4w8SPGa/JOipJQ3mSvzJGcuSZ2tnW0y" +
                "S/sxA91DPkZHrXYFvAoQ4X/v4pVx8K4gvsXbA/IjSsbHK5v93o" +
                "1Wa4XfF3elu3hknTUeYYOsdSBA1ritGT9Pc1pGoCJL+zUbesjH" +
                "6LIqD9vO2cZP8xzPvutXG74X1M7XzL0g+mf8Cuc2Vr8X+Hz1vf" +
                "ede2bmjmjdGyPG3Rxzd/X/ni76YVHfx0es/qUx/9/CJOdTumI8" +
                "3zTaqHyjxiUbkg08ss4aj7BB1joQIGtcH8dyWkagIkv7NRt6dM" +
                "yPAVq5745ZbFtrqZ1NzmZSMRa2s/SQLC0c5+lAgKxxfRzLaRmB" +
                "iizt12zo0YIfA7RynRYstq1Vx0/7XtD4+XSk3rhgnfrv6aLvFv" +
                "U6Tbl6rFPrpaq51TOmuE5L641LNiWbeGSdtOgXsrGF4zwdCJA1" +
                "ro9jOS0jUJGl/ZoNPa/eiwFauAJeBX1tS7Ilk4qxsG2hh2Rp4T" +
                "hPBwJkjevjWE7LCFRkab9mQ4+W+DFAK/fTEottay21zcnmTCrG" +
                "wraZHpKlheM8HQiQNe7sDT9Pc1pGoCJL+zUbesjH6LIqD9vOuY" +
                "z4D5kHcEM=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1584;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlGk1v3ER0j00rTpUqNUJIveVSpeIOrMe7QJFaDiAVqdKKQ8" +
                "UF2tJjbsnaaRwXpLJSBBKVKhoSpQmiaRGCktKICwip/IBeQsSF" +
                "KAmXHCrICc9O3r43nvHu2GtPbJiVx++9eV9+fm88nnWt1typ9V" +
                "rwfHOH44KGPdLiYyhN9VBYtLAO/FQe+VUcefVcAE+OqtaiKzkr" +
                "e0uvwLyhtH/Od+iI/1qtgOafDV8dQpqllwn+yfkKojj5Tf8VfZ" +
                "zC0VS63OQ4sftFxcnX3gG9vfZtRbqRR5xSZulzyXFKHismTqb2" +
                "jPmONI9gLyCBcZhtIIWOxnHQgDoo1L2/G3o58AFlVCtCFujyuG" +
                "wND9UeXA/1So4AvfI4ptT1WPb7zj6olabl78vUTwfne9Hx/dQL" +
                "Go71BMnvCPxr5Nu1qd8i6ImG89FQHn7Td/RnbZyuaXkf9qBfho" +
                "tae1nKrlYa2fpiefKpKF+cDflM6ZQWH9drSTfSX6fKRX1KkhV0" +
                "2us96WddHulXd8G7tO6mP+1fd/Wn5am7+lOzupuey39+GhQndr" +
                "VE89NVszhdb2Wcn94ZYk74qkTzU+6+JOVTwFI/764Uk0/XX8qQ" +
                "T1fyf955S92+ewcgTt6XB+cVYiXxncmb9x5E93DFu+N9rRm9O/" +
                "VHglz0VPLuxWjfKlf3rJ/3kwfvVh55Unv368RvbyH7PfJWPW3V" +
                "B733FhbNcGGqtxg2l21soK+XMqwz54qt6+D1nqVOat862cYKWY" +
                "8XZM/pyGdKpzSnY6Il3YjTMfEPuahPSbKCTnu9J/2syyNJ8zib" +
                "SD2PT5RoXTBhNo/7vxvW2fmYppvZ95/61sGq5bpbNdt/ynzvev" +
                "nUeHHg/Pp+rbQtfLNY/aTuXq5VuOXvPcxP8Thl1vdEe3/fzq7R" +
                "/7Pv6K6OOnPccH7aSRun4fefor4087jTMouT0xq27gzWfynmJ/" +
                "Y4cwU9tieVJZ+c05XOp9P5rguUdcJ7BeXTeuZ8Wm9fziJlLZ9O" +
                "VTqfThWbT6ylQjk8pVu2pfP0XvuUXS6m7twLdtdPRduT31tyjN" +
                "M5y3Eq2F5wuaA4nbccp4LtOT8ejuxhXUdWn/EboeDDaDZckubG" +
                "pQFz55LMRfkHyQ7Wm9QmR81lVN9M/WqMNcagBxywxhhbRArHEE" +
                "Y5hAS34KJQ159FvZxsE+zKVlCjLE19jx+qPbge9BX9orplDzBG" +
                "sR0aqa7ZfDKmuVvzMhflHyQ7WG8eMrj/BONZ/SLrzDNV3lfJ3/" +
                "uk9XgYVnk9Pn3BbD0+fJyqva8StG3FyTlaVJzCGxbe747mHafY" +
                "+uyBCpW76f10ThrOYyeHnp8+rnLdhR8Vm0+k7kYq/bzL3Xs78z" +
                "hby7x6Wsv0P+eaWT4FN8sVp+ay3brT2xtmn+4/+n/LCbM46fn+" +
                "P3Ga3TaLU/hJueLkLFjOp4Vq1p2zaDlOiyZx8p8Zr9IuuhehBx" +
                "ww6JGGsIyjBoRlveEdvZxsIW6RUoAe54+PiWPmeH97ugiArHzN" +
                "wGHp+4Ih8qnI7wuCzw6j7hJtPApX7NbdoDiVdZ3pvmU3Tnp7mn" +
                "z6olz5xNp248TaZc6n5Dg1b1lej9+qZt3ZXmc2Db9j1fMlvGnu" +
                "Qc/2IE5sj+OzgRijfDocNeioQrPeskpXrcjf2es0AQ0s6fTy1r" +
                "6NnHFvM+8/PaxyPs3s21qPs5Eqx4mN5L1Pl5hPP1T6PfiNvPMp" +
                "iv0+9Gy/Nz/tI1Xm0+GUV6UKzXrLKj3Zih6nNLCk0xvnVK+vDM" +
                "+76b/s5lO4nm8+uW23zbZFH0W3CwmMw9HzrkcBPh2OGgBGiGui" +
                "msUYWkFMjKlWvEtUSh7HKwCL4lDt8T563m2DBOvuUKG3cIaoAO" +
                "UA23K32N+ij6S6kMA4LCgR5xby8Z+gUG5O4T3oEDhokvXgWYxR" +
                "rdQH4BEcqhechp6ARXHIXFQ7SMS9RR70iGCb7ibbFX0k1YUExm" +
                "H/c95HnJvIx3+CAjiX4xTWfXMXVIGDJqoZ9XGqkES6zCnw8C6V" +
                "kjWhJ2BRHKo93kf5tAsScW/RA/QIscZ4Y7xWEz1vHOI/95h7rD" +
                "E+GyAF+HQ4akBY1su16eSoTSGjWkGNqheyNTxUe7wX36ugZZ1u" +
                "2YMex79gUX49");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 2306;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXEuIHFUUbQwulBjIDKLBmVFkiBGEgAQRTJCu6gaDbtz6Ay" +
                "GL4CzcZTEKMtMzpj8Q3BgXgiGrLFQiCRJFjShi/EzEz6Ajs8gm" +
                "O7MISJQYWqxXt26de+97PdOf6jJTxXt9v+fcenn9+lV1TyoVfS" +
                "y8wVLzbKWwY/G1SsnH4vx48ReeoNd4e7y9Ull6uyhchzbs0X6/" +
                "KL7FEyOMzBfZ6+mkfbQwE4j4vEfmOSF/m/YXk7YSiPy0/ckIFW" +
                "44sxe+DlmPTgZjP86lCzfnOFW75Y5TmG8LjNO1ksfpWrHjVD1S" +
                "PYKeJNJIh0V7Xd+Y86MlJkuwahzr8RmdHp2TVcoqEM1saNE5y4" +
                "cKJXf7dC9My9BrPlVXRplPS6/q+SS1pfkS5tNKf/Np+fYhP5f+" +
                "9aVeR2Ouf9zW3eXuC2pfFL4TKGV9qm0rd30K8w2/PsXX4+u+nv" +
                "RTyXm91XQyt3iKIpDlXjk/90/l3imBOyXj6RXMwIOOiMYhWytq" +
                "slbwSURmxf5J1oYqwpa0km6cfIJSn9q62TmbnF1lcbauPOHLpN" +
                "n0dTa3zArc2XCeZHCSYezCwnbth8+02XBMjj+bj4XB1hXk2nq8" +
                "nkhpn9rWs3MmOdfdfjy3ONu6POHLpJn0dSa3zAjcmXCe5HSSqi" +
                "Gzts/LLO3n2r02E4qh+cS1gl1i6woQYXb2j+T3d18Nuo5UXxrP" +
                "mtz+cfCcomuJ343f5Z510qq/kI0tHBfSgcDy0qsaV+YuzSNPey" +
                "yjtLDdxlsfNVd9qE6g+SPAufqabXyWdSr/F7nQy9NjxJVf75/0" +
                "sVTovTzzav7qhcpYj+Z3ufR9MuM/KAo3Olsp9Rg3XzPf/TQvFr" +
                "nPLPto/1zw+nQ+Ps8966RFx8nGFo4L6SRFx90rMCWuQwvlaQbL" +
                "KC1st/GSDc3nIzvQ/BFATKCey/Hl6B3qE/RUIs3J1MMGWetAgA" +
                "xJI+s8yqHTSToSlZDXr4Kszsbcft0UQ37mI4nZGJsqQEXQFn4z" +
                "O/tf1OzddH1aWOtzvTgTyB3wvbGwOsD6dGbM69NPOdPAn0rR/M" +
                "2zPo27luXHxoMbf1juOPXL1/51yPk0QN4gn3fRZ0PPjM/Kyxpg" +
                "nNbGM07xMyXPp4L5qg9XH0ZPEmmkw6K9rm/M+dESk6W07qd8HB" +
                "mPHMnIMTpLeyUbms+HCiW3xpaYlqHXc7r2X/Y53fJbwz+nG+lJ" +
                "4hDP6dp9fo+w/OaozzOrzxf5PLPscUL1G4/T6y/0+T6+El/hnn" +
                "XW4ivRKVicBhl5kCiaoqSUrqunwnmak3k1CxB1tqzdNp+Prwe1" +
                "oi6JrSvAGJl9weF85v5d2cJH+1qxePVn68+iJ4k00mHRXqtztM" +
                "RkCVaNYz0+I8foLO2VbGjVFy0fKpTcFBfCNNpz9ecSKetJIo10" +
                "WLTX6hzNGUCTVo1jPT4jx+gs7ZVsaM1Vy4cKJXf7n16YWqvtrO" +
                "2sVKhPv8/ZSWcy0g+RjS0cF9KBwPLykxpX5i4fRJ7mtIxABZv2" +
                "w6ebqz4UAzRZl8S215xpK7Xk84n61LZCZ8J0H9nYwnEhHQgsJ+" +
                "OkcGVuMk55nua0jEAFm/bDp5urPhQDNFmXxLbXnGmHa8nKTX1q" +
                "O0wnydLCcSEdCJA1bhjHclpGoCJL+zUbWvVAOAZo2fp0wGLbWj" +
                "OtUWskUtqntgadlUrnDrKxheNCOhAga9wwjuW0jEBFlvZrNrSj" +
                "H4ZjgCbrkti2VtKiXdGuZBeR9SSR1tnheli01+qUne1JFJq0ah" +
                "zrYQxEcozO0l7Jhta+xfKhQsltr8S/Zhuf7VPz/Xh9/1bePxVd" +
                "ffR49Dh6kkirV10Pi/ZanbI1JkuwahzrYQxEcozO0l7Jhuaq96" +
                "uVaP4IaEzL0P/vVVq3bJ37u/or/d3fheMGuQ/u3OuN061bZ5yO" +
                "/tPfOPX9vtsebY+3cU8SaU5uNWGRXplHkrPE6W+OyEo6I7FVZr" +
                "tX8km7jiS9cUhmaSRUwozUfD7XL55gPmKW1VpMrW3wXGX/zfJc" +
                "5fUDQzxX2V/sfKpdql2K/qQ+GblUIs3J0sJxIR0ILENySBqHWO" +
                "GTdh0JVGRpP66AGan5fIzOGU5iNsamGFQErf5y/eVkNct6kkgj" +
                "HRbtdX1jzo/mDKBJq8axHp+RY3SW9ko2tM6E5UOFkltjS0zL0G" +
                "v/VOz3CGUfncli8Vq3JXPvRjoDb7i2nL1voxtObzXJxzbzGXBD" +
                "S7KXOiEHviv8mny9UFnn32eG/dLGTCFcdyyeQKStdpN969763u" +
                "gP7kkizckJTm6RXplHkrO4njNIZySNQ9mETpmw60jSKYLjNBIq" +
                "YUZqPh+jc4at1mJqLY3tch91W9nfGUVdpy+9HYm/E4nM34xAB0" +
                "LISsjBXUnXt/ssnTs38ksbM4Vws/nU1cxRn393Y3+H0cqfYLUm" +
                "+lmfFtZGeM/vGHC/OcDvMDp3jfn3Bdn6FE/EE259Kuz72YnQ+j" +
                "TG74MnwvNp6PvqffV96ElyZzwZT9b3ud9Fs0V6ZR5JlK0xWUrr" +
                "nvRxZDxyJCPpnbt1lvZKNjSfz/U0TpLbXomtINeu1q8mUtqntq" +
                "t0khy9BwvHhXQgQNa40XvhPM3JGJqFctmu/ZoNzefj65FVQetV" +
                "ASJ63bdETxd53wK0cu6Dw3z+fcvSpSHX1uzJXbw73m3XJ2fZcE" +
                "3YraNk/Ga5G35eHBpifdq98fo0eD16PrXuye8kL27p3xdcLPY+" +
                "eIPnBatFjlPr3pLHabXo913PcfrBjlPngf7H6X//e84fxrs+8T" +
                "jF0/G0956f3mRNmNZRMn6z3ML3T9Oj+QeYT99s6fXpm4Kf0x2r" +
                "HeOedXfGe+I9tWNun8kWZ4OMPEjxHvdKUVJK//32hPMkp5Okzt" +
                "bO/TJL+3EFuvl8rqfPO67VjkCoAkSYfcE943gXLM51HhxhXzDE" +
                "bGweLPoayvk76s6jJX/f0uf/GzLqOp7tan8vaj6NgjRMbjhn+C" +
                "qiL6MvuWedteR1DRaOC+lAgGxw18J5mjPn1SxrMkv7NZtoa6GY" +
                "aA3X7I9AqAIZH53kPjrJ8yk6STr52GbG+KSWbM8SZD/PYoZYtM" +
                "XPgI2ZQrgai6VwXOD4D93N7x0=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 2550;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW02MHFcRHowPRMjIYo24ZQ9EcnCkSHCBC4L+GRIn5BgFoR" +
                "DzY3zI5MIlkSDIPbuzMx73AhEg5WChzYWDL+EQFEBCMhFSAgjk" +
                "ZNeQCO2SJXtBUZJDlD0ksjb06+qar6re696Z2dnB261XU7/fV+" +
                "+5e/rHs60WbdGr+rMl/NJn43725JFmTD9L9lRXS34pw500sctI" +
                "e7W9ypJtttqr8a/gcRZ01EGjbMqSmttkrWbRKNJmLxB1tezdDp" +
                "+P54Ne0ZfE1h1gjfTWvb11KFv+ldZct8HCbPH6n+++UK3Qr4vx" +
                "O6xTdNdo7a5VuZ8za/pbof+llH8vxt98lu4fDtJj97nG6IvBc+" +
                "auYO7vR9pLk/Uw/GLtOi3adRp+4Qit0+Js18lDWhsxfdw79sw6" +
                "LXdat+zmd3+wrZ21M0jSyIrOOAlPO8u/higkaVQNzJX7kQUv7S" +
                "tnUSUjjIFMzpFsMi5jerju/W4lmr8CGlNb6TPpM60WSbc5jazo" +
                "Y+RjD+eFbCBA17hhHMtpGYGKKh3XbBiu+1AO0GRfEtv2yhl130" +
                "/iXLxWc47O6fup/6Upvp+i8b6fwnnB2pp1yh84yus0+GC23+Px" +
                "g/GDkKSR1X7USXh01NpUDcz8G8iCV+PYCGMgk3N0lY7K3jFc93" +
                "63Es1fAY1pGVqt5DjL5Hj/HqpOjsPLm9S1LXN9LyGH/pWYJYw6" +
                "ni19zBTCtZn+/PY9pm6yjG/yOsU34dV5IVvm+l5CDjP7/nqWsC" +
                "19zBTCtZn+/BrvC55oPwFJGllkw6Oj1uZsicla+W33iI8j81Ej" +
                "GTlHV+moZMMYLFg+dCi57Uz8OZOVrqfrxZWvlOU1cJ120qWH80" +
                "I2EKBr3Pzb4TrNaRmBiiod12wYg4VwDtBG9wUG286ZM+ZzXxD/" +
                "eb7PLWE+/3rXe328HtIb6Q2WbLOV3oifhYfzQjYQoGvc+Nlwne" +
                "ZkDM1CtezXcc2G4fPxfGRXegVCHbCV7Ca76npQ2uSDhI91J5c7" +
                "7lP6JUbwarPL+LISFcCyGRpX9hTqoDjPv6PnIGdQt/lxVMd3xH" +
                "cUq19J0sgiGx4ddXK542dzBdCkV+PYiM/IObpKRyUbRv5dy4cO" +
                "JbfGlpjaSjtp8dRPsjzGOrQXTOfJxx7OC9lAgK5xwziW0zICFV" +
                "U6rtkwBh+Ec4Am+5LYtley4nvje4sVqyRpbk9OJ6edDY/zsYYK" +
                "tp2enC6P0tMcYw1eWc0RiWIZOUdX6ajsHcPnY3SyqFeLLTG1lV" +
                "5ILxQrVspy7S7QTrr0cF7IBgJ0jZvX1GlOywhUVOm4ZsMo7guC" +
                "OUAbHU8G287Z5lfXy6dG6zwsxtZEz4rDhtjW9PcF09SGaw7Sxf" +
                "j3T8Nj098/Re/N+b3ve7N+71v7fvz6rfJeZap1uj7eOg2/Pvvj" +
                "6SDrlD8633W69P5469R/ZMrvgs/42uS1//9t1r20e+0eJGlkOT" +
                "3ehEdHrU3VGpO1su9NH0fmo0Yykk21qNJRyYbh8/F8ZFd6BTSm" +
                "tuKT8ckCtZKkkUU2PDrqZHE/7mVzBdCq+4LHfByZjxrJyDm6Sk" +
                "clG0besXzoUHJrbImprWQzKdaYZHk/uEk76dLDediL5zsV4Rr2" +
                "KNznQziW0zICFVU6rtnEeD6cA7TRM5zBtnPmjPZb6jx8y9nkg4" +
                "TPxlAtcTSmRpf1mtXmhbwhjhBb2tbdyhlM8K0kqrM/qSvC7dnL" +
                "he+fTs/K37Vk/yrGTnEFra4M2bvl9fThUn8/+3D0P/DfFCiftI" +
                "z598v87ew/2Rul9l/Feqz7UWV/ovup7qezP2YvSG+2nm0Ia3TP" +
                "mL0Z+P+W6ncYw3NlxkvZXwt5feIr7UdKeaJ7Mk7jtDgDK0kaWa" +
                "V+FR5nIQqps4vPq6PYVY6xV1ZzRKKMkFKFfFVXmajoXQyPT3SY" +
                "smaxJaaxFuPFQqskaWSRDY+OWpuzuQJo1fH0uI8j81EjGTlHV+" +
                "moZMMYLFg+dCi57Uz8OZPVe6AXFZe9L3NV754D3GOcrIv0ztbH" +
                "9t96cWP0q5P1MmXebe3bIEkjK/+hk/DIqLUZARhSA5NfJyNU47" +
                "PILm0Xmg3j8m44B2j+Cmg+bcUPxQ8VR1YlSSMrf9JJeHTU2lRd" +
                "HckKTXo1jo0wBjI5R1fpqGTDuLxr+dCh5LYz8edcWXfHdxdaJU" +
                "kjK3rYSXh01NpUXTErNOnVODbCGMjkHF2lo5INw3XvdyvRbLcW" +
                "01j3xfcVWiVJIyv6rJPw6Ki1qbpiVmjSq3FshDGQyTm6SkclG4" +
                "br3u9WotluLaZlqN5D7OhP1pyET+uBtxk7dXn1GNFOMyb3oLH9" +
                "fkN8Uk7OHopEm/pT+qXPxsMok0WaMf0s2VNdLfmlDHfSxC4jw+" +
                "/V/v6pe5Tf083690+jtdvSn9IvfdHWOCiTRaKtcfpDluyprpb8" +
                "UoY7aWLXkdrjaWiPp5Wnj87xdPnYbN9nRkvREiRpZJENj446ud" +
                "zxsyUma+Wq/9jHkfmokYyco6t0VLJh5KuWDx1Kbo0tMbWVrqVr" +
                "rRbJ8n3EGu2kSw/nYV/u6AjXsEfi5j8N4VhOywhUVOm4ZsPIfx" +
                "LOAdroDYzBtnO2+bQtfetw3lfnT833/fis/76lvI96m2X8dv8a" +
                "+5w9vEQxmReygRDyEnKY2ff7LMvnm+LSx0wh3PIoWEOm7XayrX" +
                "9t/NxJ/m4j//l8j6f8Z4eLfzh/V7bUyX9xtM+7eDveZsm225MT" +
                "yYl4e3gJHs4L2UCArnEdWqhOclKNzyK7tF1oNgyfz8mlNaD5Kx" +
                "DqYGRtxBuFVsrSt0E76dLDeSEbCNA1bv50uE5zWkagokrHNRvG" +
                "YCGcA7TROhlsO2ebr8+7ZDFZdN/jMpYsNh+bFEeWzN+vtvF78P" +
                "zkNWG+pbVp+0lfS19jyTZZ0cvkYw/nhWwgsN57UuPK2t4PUKc5" +
                "LSNQwabjiOnhug/lAM1fgVAHyBj/dxiDXzY/t0SdW+j3Kp3xnl" +
                "sGV8a8D9jpb/Rf6W/zOvX/Ub9Ojd0W6xSX50f/jQDLqwdZp/6N" +
                "xui/g9enfc7Vfvm78f7m1Ne/c742g6vquXlXz7L75uMpunPS46" +
                "lCLI+nlfvrj6eVs7M8noZLwfPuztkeT7M576KLdp1md95FF6da" +
                "p4uHe96trE81lx81x4f9A6zTPtgrr0xTNdX5v8cy3uPf+8Z7zi" +
                "6eg/dsXsgGQshLyGFm3++zmOfgvboZ8BzCuNVz8J5mDucF7sdW" +
                "k9X4HZJFVamR5XTp4byQDQTWoTkkjUOsiElMnQlUVOm4nAF90g" +
                "jzOXSucBqzcYb75FVhD7PXPQenL7aO8HbY3SfVfVdyKjmln1uS" +
                "K+NV1kRPHayfMTq+0szn4ktrU383nYnPQJJGFtnw6KiTyx0/W2" +
                "KyVj4H/8bHkfmokYyco6t0VLJh5M9ZPnQouTW2xDQM/wNuKEod");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1400;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW79vHEUUvpoeURBZ7iKfFFcJHUi53RV/QCJXTiQrRLKUNr" +
                "WVc6JLnDT0CJn/ACHEL8VSRBNjYgokCqChoaakhZ199/l7782s" +
                "WZ/vDm8yu5p37+f3vR3NjNeXeDAYfz9orvHn9fh2vDKYXtd/hj" +
                "Z+MUhe42+UftTIn+pxnMg8GJzjGn95avRlysvuTe53J9rh2XoY" +
                "XR1dpRRNLLHpsdEgH96LszUmtHA9+zrG0fms0YzIsVU2qtk4nn" +
                "3l+dih5rbYGtMztK6ny71eT5fnu57KjXIDEna4i7VirdzYe0JP" +
                "8FFnHbViLXxKltbCpWt1neYMmrbhffiRrrJxPoEdMV+Qu/vAD7" +
                "36GUh1AGt0Y3SjXllTKVq4i9ViNdj0BB80VsAOerHazMgqYtDo" +
                "1dWIaBTPiBxbZaO6d46YD+hiSa8eW2N6hrZ9V33Q533H7ue072" +
                "6Xt0f/iKxnrtHECrr2IC9lEwE6tYBkcYSVMe23mURllY3zCcAo" +
                "I+YDOiqCBjZgSw47olVsF9v1Km1ksz+25a6jP4gPHuSlbCJQt7" +
                "hpHB/xjNoDv8/3MRmh+9P4uFLietsrMtr2Xfljn/cdu5/Pvmuf" +
                "J5XRcZ5Gdy/OPI3udpunR38sc56uP1jceqqxZ3l/etBlnh793b" +
                "WLYlJMIGE397C+J8YTfBN9MzbVhs3n8MQzVLjDdJ1j8IzKA7/P" +
                "h3RjmM45wR/GM8CcFPtc1tPOAtfTzkzraWdZ51N52Otz/PDinu" +
                "OnzVOxsdx5SvOdZ54cEt/HR4MeX/PvvnXfHfV63x0t7Xx61et5" +
                "etXP82nZ89SSu4DzKV95PeX1tLTVlecp/T3dlfIKJGxYkPRRtz" +
                "YRqFvcNI7n9IxEZZWNWzaOmA9+21WM7Wun1nq5XmuNbHzrcouu" +
                "PchL2USgbnHTOJ7TMxKVVTZu2ThiPvhtVzG2r0VGPsdn+Xm39y" +
                "LPU6fvo744e6Rb/Dzcs1edv6vp7rxT3oGEDQuSPurWJgJ1i5vG" +
                "8ZyekaissnHLxhHzwW+7irF9rc+Xa/c9aHsvB/lqX6efnT3SLT" +
                "7vfnzGcvtT6+kor5r8Pp7naUG/t6yUK7EtPkr6fIz1xPGYFl2w" +
                "bA4sYuFO92r7iTtI9enjqe7SnqaTS+UlE2ls8VHS52OsJ47HtO" +
                "iCZXNgEQt33JvuKfbqbNunj6e6S3tSz7P3S95jXa69316T5/i1" +
                "Y97v+fvMRf4enOep43vmX/ns6bRf/8xz0Gll7uc5aL+q/Wofmo" +
                "x0BiQy9WdcoXHb8zxeKtP3F/cBW/evs9Jo8ZMBDRyu8qA6CJKj" +
                "LYNxZFUtZzTzT8/zeKlM4gDV95HqX2el0fyTEQ0cJ1m3qluUoo" +
                "klNj026m1ka0xo9FocH4kZkWOrbFSzccR87FBz+yeJa5HR/b3g" +
                "8aev33vB40/m//70Js9TebO8CQkbFiR91K1NBOoWN43jOT0jUV" +
                "ll45aNI+aD33YVY/vaqbVZbtZaIxvfptyiaw/yUjYRqFvcNI7n" +
                "9IxEZZWNWzaOmA9+21WM7Wun1la5VWuNbHxbcouuPchL2USgbn" +
                "HTOJ7TMxKVVTZu2ThiPvhtVzG2rxWrulZdq0/0qRRNLLHpsVFv" +
                "IxsVRNNei+MjMSNybJWNajaOmI8dam7/JHEtMt7s34OX+3cb/z" +
                "1PT9/q+zzlf0fI3z/l9XSR19PTt/+P9TR6PtPf3z3vtp6efLyA" +
                "eXon77vF7rvde73/hu64OqYUTSyx6dFRbwOBGFojU1ynIzEjUV" +
                "ll45aNI+aD33ZlsTWfZ3f/7/Ddi/fzbvL+4vbdrOd4nqeOPX2Y" +
                "3wCS59P96j6laGKJTY+NehvZGhMavRbHR2JG5NgqG9VsHDEfO9" +
                "Tc/kni2mnGvw7OV3Y=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1376;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71uHGUU3SolEg+Q5AGSIpVrOzvzEFQRNHkDSqTFMVJcIJ" +
                "qAQMJoeQGKFIBEhGgSIYGUGJTQkWdwirSs5/r43HPv3WWdXa8S" +
                "+5vR9+099+ecO19mvh2vlNFIj8l3o3YUx+0rt69wNsuQYXo0Gj" +
                "GyPScsepUnRrIicrRKo16NI+uxQ68dryTXxvx2Py0+Jr+dfP4w" +
                "Gz9NrhUZv86p/NHZvw/zn7PxR5H5y0odPlwYfXwGpp9PrSf2ee" +
                "/fN+zpWrtzqqN73j3HDAyEmT7aislAW3lrnqgZFcnKKo2rGkfW" +
                "g1+7ytyx1tD46fipXzfD5uNMX4yhnp+084F81nlVr8EzZ8V+tD" +
                "Nma58xXnVXe6x6+f1p76uLtz/tPVj/Pn7vowu4j3/Yvu+WWafz" +
                "eC+4zM/dzmRnwtksQ4bp0WjEyPacsOhVnhjJisjRKo16NY6sxw" +
                "69drySXGuoO+gOZt98wzx8Bx7Yabb3IK/CZKCtvDVP1IyKZGWV" +
                "xlWNI+vBr11l7lgb89t7ZtvH17GPB6Yv2p3T7qc3v5/Gr8evMz" +
                "YfZ/pgI8Z68kROZUe9zyKfz6t7iz1VHWj3elWL1qL2HFf3r/pX" +
                "o5HNx8exBYSZPtqKyUBbeWueqBkVycoqjasaR9aDX7vK3LHWUL" +
                "fdbc+++YZ5+A7cttNs70FehclAW3lrnqgZFcnKKo2rGkfWg1+7" +
                "ytyx9gTtdDsza5gH346dZnsP8ipMBtrKW/NEzahIVlZpXNU4sh" +
                "782lXmjrWG+qP+aHZnDfNwjx3Zabb3IK/CZKCtvDVP1IyKZGWV" +
                "xlWNI+vBr11l7lgb8092rofZeruPTfXZ3gvaOq3x/enO+A5mYC" +
                "DM9NFWTAbaylvzxEhU9B74Y36MLerbX3Negbr2BO2Od2fWMA++" +
                "XTvN9h7kVZgMtJW35omRqOg98Md8r8aR9eDXrjJ3rEXGZp678Q" +
                "ebfe5qvTX+Hdx+V2n7ePu+O/d16p/1zzADA2Gmj7ZiMtBW3pon" +
                "akZFsrJK46rGkfXg164yd6yN+W1/as9d2582tU7dze4mZmAgzP" +
                "TRVkwG2spb80TNqEhWVmlc1TiyHvzaVeaOtTHfjk+3YO1/3PYk" +
                "9956OD7M2Hyc6Ysx1PMzcyo7MnyOr2bevKzYj3bGbO0zxqvuak" +
                "91PePpXJbp/6hMz+Xfcbpsxmb7G39/9shy8XX3EzM22197f2rr" +
                "tMrRXe+uZ2w+zvTFGOvJEzmV3bg0B4hcOOtetZ/cQdVnjFfd1Z" +
                "6hk6vdVYkM2Hyc6Ysx1pMnciq7cWkOELlw5t58T9nrs7XPGK+6" +
                "qz3H1ZMX4bn764zP6T8rPOOHZ8z/++15Dvc/aXtRW6c1rtOkrc" +
                "FS6/RZW4NVf1e53P8fIdxPn7c7Z/7RT/spLBt1BmZk+s9c4Xnn" +
                "50W+KjP2l/sA9v37rJotXxnYoNH+bllxH3/Q1mCpdfqyrcGC/e" +
                "lR/+h45piXwTiyqmz4LX9xXuSrMskD1thH1b/PqtnilZENGqd/" +
                "6d3t7mIGBsJMH23FZKCtvDVP1IyKZGWVxlWNI+vBr11l7liLjO" +
                "Xfn/a/vnjvT0v//vSye4kZGAgzfbQVk4G28tY8UTMqkpVVGlc1" +
                "jqwHv3aVuWOtof1vdN32nmxuV9x7/A7t4Df6G5zNMmSYHo1GjG" +
                "zPCYte5YmRrIgcrdKoV+PIeuzQa8crybWG4u90998/fUP49rx/" +
                "p7v/3rvzO11/q7/F2SxDhunRaMTI9pyw6FWeGMmKyNEqjXo1jq" +
                "zHDr12vJJca2j+/bTkv/FluZ+2+i3OZhkyTI9GI0a254RFr/LE" +
                "SFZEjlZp1KtxZD126LXjleTak4z/AJfxDb0=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1060;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrFOG0EQdQdS8gUU/AOfYJ+tfARQ0tEhUQcJ4RCa1Gn9AS" +
                "lSJBGREE2iSEkDVEnhJl1alCYpYry8vH2z48sZbBOSWbRzM29m" +
                "3syt1seeodXScfiwFcMde6fX1xej+Xpv1Yk4mZD5KtM/jOWn0f" +
                "zoRL69VYcva73vpmB681t7H+s0n3UyTKvxCYv9FPvpb9pP/c//" +
                "3n7aH8Z+muc4eBRr4I3ebm+XMmnJSjYR9Vob0TknNKLKYz1lRc" +
                "RolnrzapxlPXaY17Z3Uuba+Pjc1Y3uVncLEjYsSGLU1SYDdeX1" +
                "eWxNW5GszFK/VuMs6wHXrkpum4uIOD/FOXN+58zet/v25Hi8ss" +
                "hq7S96zfEcs36fZTpPPWcZlfc0KTfhufQ7qate19fBSfxui/N4" +
                "rFOsU3z/dJ/OBd3N7iYkbFiQxKirTQbqyuvz2Jq2IlmZpX6txl" +
                "nWA65dldw2N1nVdrXdaiV5Na40WJDEqKtNBurK6/NYj62YI8Bt" +
                "vPXV9c16XKcyX3OvrX7VH2ljOcb66SfpOYI4zyYDdeX1eazHVs" +
                "wR4DY+r8ZZ1gOuXZXcNhcR8XyK53isU6zTYtep+d8R/ud12v/e" +
                "+HuUs94ZJGxYkMSoq00G6srr89iatiJZmaV+rcZZ1gOuXZXcNt" +
                "fGx3tLfO5u/xyvNqoNSNiwIIlRV5sM1JXX57EeWzFHgNt466vr" +
                "O7/ncgX8XETEforPXZwLFn0u6C53l0s7YZTErI/55LGcyp64NA" +
                "YWufDj96r9lB14fVq/152PpOx4Pt1kVM+n9zTzz7ofGzHf/qrL" +
                "6rK0E0ZJDDp8zCeP5VR25OdR5Mvj/N5sT14H2r3eVd1a+Mi4k4" +
                "vqQjxjO2GUxKwP+bxSdzq5AL/G5NmMmxRl+9HOGK19Wr/XnY+M" +
                "+zivzsUzthNGScz6kM8rdaeTc/BrTJ7NuElRth/tjNHap/V73f" +
                "mIdz/VYCLL4A9VBnN5Pg2aRiy2v6c/4o230Tr9jDWI8/jszk+H" +
                "X2PnNBnxf9ENd2Z8n9loHC3FGjRapwexBu53CevddUjYsCCJUV" +
                "ebDNSV1+exNW1FsjJL/VqNs6wHXLsquW1usnprvbVWCzJpyUo2" +
                "EfVaG9HIIFuOKo/1lBURo1nqzatxlvXYYV7b3kmZi4j43jfOmb" +
                "Nbp85p5xQSNixIYtTVJgN15fV5bE1bkazMUr9W4yzrAdeuSm6b" +
                "i4jm++lo5S72U+f4Jvupc9xsPz15FufMeA++i/fgWKd4D57d6O" +
                "30diiTlqxkE1GvtRGdc0IjqjzWU1ZEjGapN6/GWdZjh3lteydl" +
                "brI6S53Rmy9k0pKVbCLqtTaikUG2HFUe6ykrIkaz1JtX4yzrsc" +
                "O8tr2TMhcRcc6cZrSHes3xHGsPm7BM52kPm/THqLynSbkJz6Xf" +
                "SV118fwCE/Wykg==");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1479;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW8FqXmUQ/Z9AcNuFT9Bu+gbt7Y2v4CalCWTRhFIIdKstJE" +
                "r0EUQo9AFcuFBBIm4UQRcBg9JNHsB91t7cuSdn5sz8tzc12Fa+" +
                "e7nzz5w5c2a+4c/f5i9drey68zK+rhzuMc1n9tUz85qZ5WdaV2" +
                "u4t/Ukc9195u7Z3TNYxIhgidGPMRXoR91aR3tqR6qyKuZjNz65" +
                "H/A4VdbWWuXb9eyDVbsWXG1Pazfz0/T69fB8V+3p2Y/2+tlXgn" +
                "/r/F9H+/vw/FYo/PCvJvxmNvvzFZS+v/R+mc705fXvKeH/gz29" +
                "7nXn+M3UXve1dJblM7f3U9tT21PbU9vT27ing7O2p1fv6eB8aW" +
                "V3v7sPixgRLDH6MaYC/ahb62hGO3oEuPI1Nze3P3PeQF2r/PZ7" +
                "S/u5a5/j7XP8Lfscf9g9hEWMCJYY/RhTgX7UrXU0ox09Alz5mp" +
                "ub2585b6CunaLNbnPwRjtim3ab7xHwqpgK9KNuraMZ7egR4MrX" +
                "3Nzc7Of2lOpj7RSdd+G9Z7FhtMTgI8d66qhmVEe9Z1HP8+rZdK" +
                "Zqgjh9PNXMz9Z5jYyTnHQnITPGhtES0xzq+Uq/mOQE+pHjq8lb" +
                "x9J54mRkxzk1X01XI+Mcp91pyIyxYbTENId6vtIvJjmFfuT4av" +
                "LWsXSeOBnZcU7NV9PVSHWezz9uf6NccrU9Lbu+6NsOFu1po+1g" +
                "yfXph20H5d/k/4zx0fuX76yPFtX/9fq9j9674qx/vLk99Tf7m7" +
                "TmWWQxkZjVGGyvCY9o1NFM7ghOrIpZ341P7scJfW89Sa6dotv9" +
                "7cGbrHkWWUwkZjUGGxVU82jU0UzuCE6silnfjU/uxwl9bz1Jrg" +
                "Wjff/Uvldpe2rf07X307v8fuqf9E9ozbPIYiIxqzHYXhMe0aij" +
                "mdwRnFgVs74bn9yPE/reepJcC0Z7Py18R/0do4vYMFpimmO114" +
                "maUd3Xx67Kq9Dc4+mNdd10zjneq6vntvYuXE9v/Ee/4bU/7xZc" +
                "3YPuASxiRLDE6MeYCvSjbq2jGe3oEeDK19zc3P7MeQN17RTtdX" +
                "uDN9oR27PbfI+AV8VUoB91ax3NaEePAFe+5ubmZj+3p1Qfa6do" +
                "p9sZvNGO2I7d5nsEvCqmAv2oW+toRjt6BLjyNTc3N/u5PaX6WD" +
                "tFh93h4I12xA7tNt8j4FUxFehH3VpHM9rRI8CV77vxyf2Ax6my" +
                "ttZO0W63O3ijHbFdu833CHhVTAX6UbfW0Yx29Ahw5Wtubm72c3" +
                "tK9bF2ira77cEb7Yht222+R8CrYirQj7q1jma0o0eAK19zc3Oz" +
                "n9tTqo+1U3TQHQzeaEfswG7zPQJeFVOBftStdTSjHT0CXPm+G5" +
                "/cD3icKmtrrfKnb8Xbv98t+3epT9oOZn6Ped4/h2dPzYAF07/m" +
                "Cq+7nqd6FVPny3Mg9vN7Vq2WTwY19JDKF/0LePbUDFgw/Wuu8L" +
                "rreapXMXW+PAdiP79n1Wr5ZFBDD6k87o8vLJ91DObB6tf8jyPy" +
                "53mqVzGpA1Wdo5rfs2o1PRnV0OOSdau/RWueRRYTiVmNwfaa8I" +
                "hGHc3kjuDEqpj13fjkfpzQ99aT5Fow2vcF7XuV69tTv9/v05pn" +
                "kcVEYlZjsL0mPKJRRzO5IzixKmZ9Nz65Hyf0vfUkudaiexv3Nl" +
                "YrsxfXhYcIlhj9GFOBftStdbSndqQqq2I+duOT+wGPU2VtrQWj" +
                "/dwt+j7zUfcIFjEiWGL0Y0wF+lG31tGMdvQIcOVrbm5uf+a8gb" +
                "p2ira6rcEb7Yht2W2+R8CrYirQj7q1jma0o0eAK19zc3Ozn9tT" +
                "qo+1U7TfDZ9VZkds327zPQJeFVOBftStdTSjHT0CXPmam5ub/d" +
                "yeUn2snaKj7mjwRjtiR3ab7xHwqpgK9KNuraMZ7egR4Mr33fjk" +
                "fsDjVFlba8Fon+OLPscfd49hESOCJUY/xlSgH3VrHc1oR48AV7" +
                "7m5ub2Z84bqGsnxj+ToXoh");
            
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
            final int rows = 36;
            final int cols = 74;
            final int compressedBytes = 860;
            final int uncompressedBytes = 10657;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV0tqG0EQnZP4CL5EMyexsHZGwr6AdobcwvgjyCKLLJJAQN" +
                "iEhEByp4ym9VSvXtUMSuKFFz1D11S9evWquhnZTLktt11XRru/" +
                "9h4iWMPM97EpmO91cx3NaEdGgCufu9mK/YD7qaK21oKxea78zY" +
                "dhfd6cdeHa7Lr02nwi/+dofw/rV8L82v3Htfk4m/3+F0pfjt6P" +
                "f5mkv+vv4NWVM2DB5GesYN1pnuplTJ0vzoGY52dWrhZ3BjX0mL" +
                "reve/adcqbedbOYOZ3t+t3e2trimF5sPrdlCb48zzVy5imA1Wd" +
                "I5ufWbma7szU0OPIOu/PzVavRjU2xGc1Bps14RnqdTQTO4Ljq3" +
                "yWu9mK/WxC7q07ibVgtP93J9a2c2rn9GrnVK7KFSxiRLCGme9j" +
                "UzDf6+Y6mtGOjABXvubm5uY9xxPIa8Fo71P73bVzauf0lr+D6f" +
                "vuuX2dnHROL+0M2nfwK75P39oZtL/j7f9dO6c38n13WS5hESOC" +
                "Ncx8H5uC+V4319GMdmQEuPI1Nzc37zmeQF4LRnufTnqfLsoFLG" +
                "JEsIaZ72NTMN/r5jqa0Y6MAFe+5ubm5j3HE8hrD9F1uR680Y7Y" +
                "db2rzwh4WWwK5nvdXEcz2pER4MrX3Nzc1o/OKdT72kO0LMvBG+" +
                "2ILetdfUbAy2JTMN/r5jqa0Y6MAFe+5ubmtn50TqHe1x6iVVkN" +
                "3mhHbFXv6jMCXhabgvleN9fRjHZkBLjyNTc3t/Wjcwr1vvYQLc" +
                "pi8EY7Yot6V58R8LLYFMz3urmOZrQjI8CVr7m5ua0fnVOo97XM" +
                "7x/7R3h16VUZsGDyM1aw7jRP9TKmzhfnQMzzMytXizuDGnpI5X" +
                "1/D6+unAELJj9jBetO81QvY+p8cQ7EPD+zcrW4M6ihx/Gtuyk3" +
                "sIgRwRpmvo9NwXyvm+toRjsyAlz5mpubm/ccTyCvPUTrsh680Y" +
                "7Yut7VZwS8LDYF871urqMZ7cgIcOVrbm5u60fnFOp9LfP7bb+F" +
                "V1d4V7c1Y3muSH9NW9ad5qlextT54hyIeX5m5WpxZ1BDD6l86B" +
                "/g1ZUzYMHkZ6xg3Wme6mVMnS/OgZjnZ1auFncGNfSQyqf+CV5d" +
                "OQMWTH7GCtad5qlextT54hyIeX5m5WpxZ1BDj+P1Bzcyvcc=");
            
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

    protected static int lookupValue(int row, int col)
    {
        if (row <= 64)
            return value[row][col];
        else if (row >= 65 && row <= 129)
            return value1[row-65][col];
        else if (row >= 130 && row <= 194)
            return value2[row-130][col];
        else if (row >= 195 && row <= 259)
            return value3[row-195][col];
        else if (row >= 260 && row <= 324)
            return value4[row-260][col];
        else if (row >= 325 && row <= 389)
            return value5[row-325][col];
        else if (row >= 390 && row <= 454)
            return value6[row-390][col];
        else if (row >= 455 && row <= 519)
            return value7[row-455][col];
        else if (row >= 520 && row <= 584)
            return value8[row-520][col];
        else if (row >= 585 && row <= 649)
            return value9[row-585][col];
        else if (row >= 650 && row <= 714)
            return value10[row-650][col];
        else if (row >= 715 && row <= 779)
            return value11[row-715][col];
        else if (row >= 780 && row <= 844)
            return value12[row-780][col];
        else if (row >= 845 && row <= 909)
            return value13[row-845][col];
        else if (row >= 910 && row <= 974)
            return value14[row-910][col];
        else if (row >= 975 && row <= 1039)
            return value15[row-975][col];
        else if (row >= 1040 && row <= 1104)
            return value16[row-1040][col];
        else if (row >= 1105 && row <= 1169)
            return value17[row-1105][col];
        else if (row >= 1170 && row <= 1234)
            return value18[row-1170][col];
        else if (row >= 1235)
            return value19[row-1235][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value19 lookup");
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 18, 0, 3, 0, 19, 0, 20, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 21, 0, 0, 4, 0, 0, 22, 5, 0, 23, 24, 0, 25, 0, 0, 26, 0, 1, 0, 27, 0, 6, 28, 2, 0, 29, 0, 0, 0, 30, 31, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 32, 10, 0, 33, 0, 0, 0, 0, 0, 0, 0, 34, 0, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 35, 0, 0, 0, 0, 3, 17, 3, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 37, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 38, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 39, 0, 20, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 41, 4, 0, 42, 0, 0, 0, 43, 0, 0, 0, 44, 45, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 8, 0, 0, 46, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 47, 10, 0, 0, 0, 0, 0, 21, 22, 0, 23, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 25, 26, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 30, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 1, 32, 2, 0, 0, 0, 5, 4, 0, 0, 35, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 38, 0, 1, 39, 0, 0, 0, 6, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 9, 42, 43, 0, 0, 44, 0, 5, 6, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 1, 0, 0, 0, 0, 0, 0, 0, 47, 2, 0, 0, 3, 0, 7, 48, 49, 0, 0, 0, 0, 1, 7, 8, 0, 0, 50, 0, 8, 0, 0, 0, 51, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 53, 54, 0, 55, 56, 0, 57, 58, 59, 0, 0, 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 62, 10, 0, 0, 0, 0, 11, 0, 0, 63, 0, 0, 0, 64, 12, 13, 0, 0, 0, 65, 66, 0, 0, 0, 4, 0, 0, 5, 0, 0, 50, 67, 1, 0, 0, 0, 14, 68, 0, 0, 0, 15, 0, 1, 0, 51, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 6, 0, 0, 3, 0, 0, 0, 0, 0, 0, 12, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 69, 70, 12, 0, 53, 71, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 72, 73, 0, 74, 75, 76, 77, 0, 1, 0, 2, 15, 16, 17, 18, 19, 20, 21, 78, 22, 54, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 34, 37, 0, 38, 39, 79, 40, 41, 42, 43, 80, 44, 45, 46, 47, 48, 49, 0, 0, 1, 0, 0, 0, 81, 0, 0, 0, 50, 0, 82, 83, 9, 0, 0, 2, 0, 84, 0, 0, 85, 1, 0, 86, 3, 0, 0, 0, 0, 0, 87, 2, 0, 0, 0, 0, 0, 0, 88, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 90, 91, 0, 3, 0, 4, 0, 0, 92, 1, 93, 0, 0, 0, 94, 95, 5, 0, 0, 0, 0, 0, 96, 0, 51, 97, 98, 99, 100, 0, 101, 55, 102, 1, 103, 0, 56, 104, 105, 106, 57, 52, 2, 53, 0, 0, 107, 0, 0, 0, 108, 0, 0, 109, 0, 110, 111, 0, 0, 10, 0, 1, 0, 0, 0, 112, 4, 0, 1, 113, 114, 0, 0, 3, 1, 0, 2, 115, 0, 6, 116, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117, 118, 119, 0, 120, 0, 54, 3, 58, 0, 121, 7, 0, 0, 122, 123, 0, 0, 0, 0, 0, 5, 0, 1, 0, 2, 0, 0, 124, 0, 55, 125, 126, 127, 128, 59, 129, 0, 130, 131, 132, 133, 134, 135, 136, 137, 56, 0, 138, 139, 140, 141, 0, 0, 5, 0, 0, 0, 0, 0, 57, 0, 0, 142, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 6, 143, 0, 144, 58, 0, 59, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 145, 146, 0, 0, 147, 148, 7, 0, 0, 0, 149, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 150, 1, 0, 151, 152, 0, 0, 4, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 11, 0, 0, 12, 0, 13, 0, 0, 153, 8, 0, 154, 155, 0, 14, 0, 0, 0, 15, 0, 156, 0, 0, 62, 0, 2, 0, 0, 0, 8, 0, 0, 6, 0, 0, 0, 0, 157, 158, 2, 0, 1, 0, 1, 0, 3, 159, 160, 0, 0, 0, 0, 0, 7, 0, 0, 0, 60, 0, 0, 0, 0, 0, 61, 0, 0, 161, 0, 0, 0, 9, 0, 0, 162, 163, 164, 0, 10, 0, 165, 0, 16, 11, 0, 0, 2, 0, 166, 0, 4, 2, 167, 0, 0, 17, 168, 0, 0, 0, 18, 12, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 0, 169, 2, 0, 3, 0, 0, 0, 13, 0, 170, 0, 0, 0, 0, 0, 171, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 172, 0, 173, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 174, 0, 2, 175, 176, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 177, 0, 178, 179, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 181, 22, 18, 0, 0, 0, 0, 0, 0, 182, 0, 0, 1, 0, 0, 19, 183, 0, 3, 0, 7, 10, 0, 1, 0, 0, 0, 1, 0, 184, 23, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 16, 0, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 25, 0, 64, 0, 0, 188, 0, 189, 0, 190, 0, 191, 21, 0, 0, 192, 0, 0, 22, 0, 0, 0, 62, 0, 26, 0, 193, 0, 0, 0, 0, 0, 0, 0, 194, 23, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 195, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 24, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 196, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 197, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 1, 198, 199, 0, 1, 26, 0, 0, 0, 0, 3, 0, 0, 1, 200, 201, 13, 0, 0, 0, 0, 0, 0, 0, 0, 202, 67, 0, 0, 203, 0, 0, 204, 205, 0, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 207, 0, 0, 0, 208, 68, 0, 209, 0, 0, 3, 0, 0, 69, 0, 0, 65, 0, 0, 27, 28, 0, 0, 3, 0, 0, 29, 0, 0, 210, 0, 211, 0, 0, 66, 212, 0, 28, 213, 0, 214, 215, 0, 30, 29, 0, 216, 217, 0, 31, 218, 0, 219, 220, 221, 0, 222, 30, 223, 32, 224, 225, 226, 33, 227, 0, 228, 229, 6, 230, 231, 31, 0, 232, 233, 0, 0, 0, 0, 0, 70, 0, 2, 0, 0, 234, 235, 0, 236, 0, 34, 0, 0, 0, 237, 0, 238, 35, 0, 0, 36, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 37, 0, 0, 0, 0, 0, 14, 0, 0, 239, 240, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 0, 241, 0, 0, 0, 242, 243, 0, 0, 0, 244, 0, 0, 0, 245, 1, 0, 0, 0, 5, 2, 0, 0, 37, 246, 0, 40, 0, 247, 0, 38, 248, 249, 39, 250, 0, 251, 0, 0, 0, 0, 0, 0, 0, 0, 0, 252, 40, 253, 41, 0, 254, 0, 255, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 257, 0, 0, 258, 0, 7, 0, 0, 0, 42, 0, 259, 260, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 261, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 262, 263, 264, 0, 0, 0, 0, 0, 0, 0, 265, 0, 0, 0, 0, 8, 0, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 0, 0, 2, 0, 267, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 1, 0, 0, 2, 0, 268, 44, 0, 0, 0, 269, 0, 0, 0, 0, 270, 44, 11, 0, 0, 12, 0, 13, 5, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 271, 0, 0, 0, 272, 0, 0, 0, 0, 273, 0, 0, 0, 45, 0, 0, 0, 46, 0, 274, 0, 0, 0, 47, 0, 0, 0, 0, 275, 276, 277, 0, 48, 278, 0, 279, 49, 50, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 51, 283, 8, 0, 284, 52, 285, 0, 0, 53, 0, 4, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 0, 289, 290, 54, 0, 0, 55, 0, 0, 0, 56, 0, 24, 0, 0, 25, 5, 291, 6, 292, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 2, 0, 293, 3, 294, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 295, 0, 296, 0, 0, 0, 0, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 297, 0, 0, 0, 0, 0, 0, 298, 0, 0, 0, 7, 299, 0, 0, 0, 58, 0, 300, 0, 0, 301, 0, 0, 302, 303, 0, 46, 304, 0, 0, 0, 59, 67, 0, 0, 0, 305, 306, 60, 0, 61, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 307, 0, 8, 308, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 68, 0, 0, 0, 2, 47, 0, 0, 309, 310, 311, 63, 0, 0, 0, 3, 0, 0, 312, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 313, 0, 51, 314, 52, 73, 0, 315, 53, 64, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 316, 0, 66, 0, 0, 317, 67, 68, 0, 54, 0, 318, 69, 319, 0, 55, 70, 320, 321, 71, 72, 0, 56, 0, 322, 323, 0, 73, 57, 324, 0, 58, 0, 0, 74, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 325, 59, 326, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 5, 327, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 328, 0, 0, 0, 0, 0, 0, 0, 0, 329, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 61, 330, 331, 0, 0, 62, 332, 0, 63, 333, 0, 64, 334, 65, 0, 0, 75, 0, 0, 335, 336, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 337, 0, 67, 0, 0, 0, 0, 338, 339, 69, 0, 0, 0, 77, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 340, 0, 341, 0, 342, 0, 0, 0, 78, 0, 0, 79, 343, 0, 0, 0, 0, 68, 0, 80, 0, 344, 0, 81, 69, 345, 346, 347, 348, 0, 82, 83, 0, 349, 70, 84, 350, 0, 351, 352, 353, 85, 0, 0, 0, 354, 0, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 355, 1, 0, 0, 0, 6, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 357, 88, 76, 77, 358, 0, 359, 360, 0, 0, 361, 362, 0, 0, 0, 7, 0, 0, 78, 0, 79, 363, 70, 89, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 364, 0, 365, 0, 366, 0, 367, 0, 90, 0, 368, 369, 0, 91, 370, 371, 372, 373, 92, 93, 0, 0, 0, 374, 0, 375, 376, 377, 0, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 96, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 378, 379, 0, 380, 0, 381, 382, 0, 0, 0, 0, 97, 98, 0, 0, 0, 383, 0, 0, 71, 72, 7, 0, 0, 0, 0, 0, 99, 100, 101, 384, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 106, 0, 82, 107, 0, 83, 385, 386, 0, 0, 84, 0, 8, 0, 0, 387, 0, 0, 108, 0, 0, 85, 0, 388, 0, 0, 86, 0, 389, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 390, 0, 0, 0, 0, 391, 0, 392, 0, 87, 0, 393, 0, 88, 109, 110, 89, 0, 0, 111, 0, 394, 0, 112, 395, 396, 0, 113, 397, 0, 0, 0, 0, 0, 398, 0, 0, 0, 0, 34, 114, 115, 0, 116, 399, 0, 400, 0, 0, 0, 117, 401, 0, 118, 119, 402, 0, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 1, 0, 0, 2, 3, 4, 0, 2, 5, 0, 0, 2, 5, 6, 7, 0, 1, 0, 0, 2, 0, 8, 0, 5, 0, 9, 0, 6, 9, 1, 10, 0, 11, 0, 12, 1, 0, 0, 9, 9, 0, 13, 14, 11, 3, 11, 0, 15, 3, 2, 16, 17, 8, 2, 11, 18, 15, 16, 3, 0, 19, 20, 21, 0, 6, 22, 23, 1, 24, 25, 0, 26, 0, 8, 5, 27, 9, 1, 28, 1, 15, 29, 30, 3, 4, 0, 0, 31, 32, 2, 1, 33, 34, 4, 1, 0, 0, 6, 10, 35, 36, 16, 37, 38, 0, 39, 40, 6, 41, 0, 42, 0, 0, 43, 44, 7, 5, 45, 5, 46, 47, 48, 7, 9, 0, 5, 49, 50, 51, 19, 6, 8, 0, 52, 2, 53, 54, 15, 8, 55, 56, 0, 57, 0, 18, 0, 58, 59, 60, 8, 61, 23, 62, 1, 63, 3, 64, 2, 65, 66, 67, 2, 68, 2, 19, 69, 70, 71, 72, 73, 0, 3, 74, 10, 0, 0, 75, 0, 76, 77, 8, 10, 1, 1, 78, 4, 0, 79, 0, 80, 0, 81, 0, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 9, 0, 11, 90, 13, 3, 91, 92, 93, 94, 12, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 22, 22, 9, 1, 24, 15, 101, 0, 4, 102, 1, 1, 3, 103, 0, 11, 104, 105, 0, 106, 107, 108, 109, 110, 111, 10, 0, 112, 24, 16, 0, 0, 13, 1, 0, 113, 27, 1, 26, 0, 4, 14, 114, 6, 1, 13, 115, 27, 116, 117, 0, 0, 18, 18, 5, 118, 7, 0, 0, 4, 20, 0, 6, 119, 1, 34, 1, 0, 120, 121, 49, 18, 8, 4, 15, 122, 1, 2, 123, 124, 22, 125, 12, 126, 0, 5, 127, 128, 129, 130, 131, 132, 29, 31, 133, 134, 6, 19, 135, 32, 13, 16, 136, 137, 20, 0, 5, 12, 138, 139, 140, 13, 141, 2, 142, 143, 144, 35, 19, 145, 146, 147, 38, 148, 2, 6, 4, 149, 150, 0, 39, 151, 152, 2, 153, 0, 154, 40, 26, 41, 155, 156, 4, 157, 49, 22, 7, 158, 159, 8, 42, 160, 161, 162, 0, 163, 164, 22, 0, 165, 166, 47, 5, 0, 31, 167, 168, 169, 16, 170, 171, 12, 0, 172, 173, 174, 41, 6, 0, 38, 0, 0, 9, 175, 1, 19, 23, 15, 3, 2, 45, 1, 176, 21, 177, 178, 7, 7, 0, 179, 2, 180, 2, 181, 182, 26, 183, 27, 184, 23, 1, 2, 185, 186, 187, 29, 0, 25, 0, 3, 1, 188, 8, 26, 9, 189, 190, 2, 191, 192, 51, 193, 18, 194, 195, 196, 0, 0, 197, 198, 6, 199, 52, 3, 28, 200, 8, 13, 201, 202, 3, 203, 0, 14, 204, 205, 206, 207, 208, 209, 5, 210, 211, 212, 213, 214, 3 };

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
            final int compressedBytes = 1359;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXEuO2zgQLTJMg+lsmMALZ8dMjCDrnKCyCXo5y1ka6ByEvQ" +
                "l6jjC77pskN5mjRLY+TUukvpRE0fWAqO3IkkjWq69KumcP8LiH" +
                "0/aGmw9/4w3ob3g4vHuEf4F9/F/IJ+Bit5cPP38cnvEr3gF+gU" +
                "/s7T+/d/8dgUAgNKGyf9K3k5cfcP2BfgIDMhtppv+Cm72CTP8Z" +
                "ftbZrkeAoxFvnvh7yPTf3KDQ8BfcaXzM/rzd/4YPpP+Tcd9uf3" +
                "+R/U0bpf49Z/p3W+nfQQPL1Ay0yeTPuTzr369c/+CO9I9AINQg" +
                "i7ijgC7jDwEs+1b+RJyijvM3VJjQ7EXn8iiiCIFAIBDqjtPK25" +
                "v/rfKsvXAx33L/eYQQ/vP+NdXfCBGAnWkuLdafP+lCI5SuKwmP" +
                "on5l109ebVV/eLERorAxrGaWPPFtBOtf1Q+VXT88nNjyUr841w" +
                "+/F/VDSLB+aAuH9y3vajI6MeFCWq31+5D6566/l/pzPOsP1d8J" +
                "axRPkBamt/6uVb+37cety//elf63uj7Ocf3G/KExf4z3/kUQ/k" +
                "tP/nhZf4VE66/bT4D89fMGLXJphpOfiGEBVrl/0B7/2PnDzUv+" +
                "oCn+GU4hOex8vuyE7qLMoniO1fXVHzFk/TEh+zWufvQ6RP9NBP" +
                "m/r37nINZF/U42Z1B8vr0YoZw5Qegav/aMPyKooQZ33fEH4V9H" +
                "/XWh/rXx+ofsIxh1Yp/MjmN4OgYFiHcG3gBjeJZidnrJzOHpIQ" +
                "95suSOZSffGWwfEgOr17PJlHD6X15EiCx88vAv0vrxVGAmiob8" +
                "4FJ+upDfs0N+/ePPmOvXVL9JBabagNd6WD8IyJ9BMZxqtap8/Q" +
                "BRzPjr7aUVsm3m6rSVlTvSwLXFvtzJvToVAeTOIp6Rve0L6ww6" +
                "nOu/r2jEZTUc6VeK+fUyVA6My7A67KQDzYv1ntfJoRdDqCUF0s" +
                "8BtkjQcRX2Y0D0KZy6wUtxoMMMmWDHRxKIdu/FZa61KtgqBNQ+" +
                "y6VaDBsLqwNBLWmoGkQuExM46Z9rlRa7IgZeCjFdT/WQONsAYW" +
                "ySIjrXT3dJj0/ghJ8l2E0dvn64ER/1JscPDHbOFEk2JcGsNKl/" +
                "+rUhsHiGIrW1lryNhKb5RdYJwYGwQP4SE39eWCAHGDEZx9AXM7" +
                "piPAsoDNlcpp6Sr4oBnf2Xrv6lxv0jun+TaP4Z1/XThr//tqFW" +
                "eWSZWP9m+vwbPf50A5VR/bPB+xfIfy0qP+p/pvhji/Yvcv+zcP" +
                "+mDlkqcFjgZft314z6+sw/dQc1tv97Pf4T5kWtqS+MuaGaX1r+" +
                "z9n/Du7+d92r/533naXeoAQxIsro9BWGHjacYv0Xe/5kkv3p//" +
                "wG2Z8oaVbrHz9W/ePnZ5F69Y+vWj+t948Ya0ejf0TMEwf1N3QU" +
                "fyXm4QkEQkD7DU77jWVFQ5EdJS8SO9h1iUtcFhu1nfuiJ0rK48" +
                "egzy/OVJ/BpCVnrz/vWH9lVNv6R6t/V2qcVffOOOSP8071CrJ4" +
                "aZVDHPJjkNtPgG3oL2yr2ely/R3+L38h+K7p/7bBaAo3CVvNH0" +
                "Xt+TFcjtK9VVg079Zbb8Fy3cgXmpziDPaLuUNHHbk9FC3dHuge" +
                "uQ525dnfPxM2pCRfRphB/zwRbJ1tstqQ/SZETN5uaCLrCqHikD" +
                "MuIhJlxd8mivc/mSkqoJwheHP8FD9HK/8rM95xxt+EWSodw+sf" +
                "MPj9farn8SImLdiUWzdFraFcZz3p+H55L4FAIBAo/iIsGItg3S" +
                "3PFK2owdkb5WCEBCGHaavffvMx9rv9+SWeysIRRubfmLPOY3ul" +
                "WZghfwCF/TK0");
            
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
            final int compressedBytes = 1215;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuS3CAMBeJMkc+CSvVisiOfg1CzyjHmKCSrWc4R5ipzs9" +
                "j9c9sNNjYghK23SabbtIQkpCeMuxlLAhV8pWV5IKbe1DNKNF5V" +
                "1ewUlejH28Fl7Rv8+B9zfkEC2GEb4Gk/bsbYzbzM05uaY3Qe7y" +
                "Yg71+Xg+A7X6o9SyIrQO0lIYU1xxXftEJ7wwp9k5Ks6Sb/oWlN" +
                "Lw83hrBy6JkNQheOB+0tD5R8CcAQZAKkkD7+53uZQKgjrr2UCG" +
                "tkKz+PcyTQC4HS66dz4W9//PyN5eRvDv54CJX/FOPORPw/gP/O" +
                "2u/APkoXMRMnAXqkq72MH8u3BfxH9YdQMxo1sX5l6PoBClTi0J" +
                "uzX4r6UfX6g9w/ErT+khXaG04Sw19qh7rn7dOwpH9K/GoVkm0o" +
                "/WUvjbCPij0wzc3vLuJe2iC1jXwTgh0epX0wjWY/20bHvLT/fH" +
                "l8Z9+fx/lHp8u/mhEC8+ee8wfi+tMMC0OmgJbeCvR5VmQGAkTL" +
                "NrqWjDwL6z/0sHAOCIa5DfuAJMMp1usm7ytTHe0/UP9I2B546t" +
                "Sy0fjf6frdPE0LC3PZl0ybT4Hl52dF5HgE85/qDENaica7XK3r" +
                "QyUjgPQ4okAThGDqsPOPun9e+vwHS3L/v1nKVVQ/0rjuP5nuiu" +
                "P837pZ8m7+/DL/Lqnau/u/SA4raAgiNyNfLJQfW39ofNnxsfU3" +
                "D/8I5w/kfxpfkv/WLn/v9o8Yn+T8z77XX/n+l8Zj3D8ASAlbAc" +
                "xsxWYMbMvbX8fXjyew+ZoFrWp6mLwfP7I/c9mfn+3PMuyfxEbe" +
                "kvhZqX8+/y/TX9k1+htGyB2Ft+uH3frvZP0jjxvEH8MRfwQo/0" +
                "/ln4n8GzSeQEhVf9bU/7D8x1bnvzwMSu9GfrT/V/AntUgL3PGD" +
                "sH/t+6fX8OfPrat/Uux0u/Da4J4u+uZwIV/pX8Ks/6Vvs8E47/" +
                "40Gjz88u3/Rp9/STBeLuyedP71H3n+p9T5I5lo/rKXL8fyIU4y" +
                "Qtkvfv+/dPwk3j+3gPkLc/0XPqds7vxf4fpXvf+K8sey0CHGRL" +
                "be1aZWbvbv/8DV59k0689hPx1sP4lq/08P/8irVs5nN81y+aP4" +
                "F3f+++eL/6/5/acWROFG+EfFj/ZqRrd0CLH8T02sXwkwnq0ZH4" +
                "UE/TMI/c7X/0bvX2SOP7NGDVURT95x/7UD5OBpaszPTBk9Aj/c" +
                "4OvDrpYs//s9pZ8/qn58bP0q/fx0Oftt4P4bgvsfKJ//qyL+EO" +
                "cP2n/YDfQkzfJ0WChc5c3f5+f3P71d8/fg+f3XLn/j+dZbkVM+" +
                "twGeTsnLC8ongHV0uiptU+8/QOx/mCzLMU8603Dym7p8edf77/" +
                "1nbehnfeJyGNmv9ui3VWiqVuqvU1WvuFJkQth27Pd/AfUf8P1T" +
                "Z6bjBsJVf+f+1/7OL+GyP4FAINSb/1J8/+YQElT/uPpXM39w21" +
                "9M7mUKIDUMlPRelCkkn0DIs5TdzV9ZWHezt2wQIlShP+79Ch7+" +
                "/DEf290aB2f4MbzUYQrTDXtOySLS8Y9D+u9/qz1+t71+ffEvXf" +
                "Gvl+iNFTYnzadfB1lJDnDUAgKK/pWl2v9VKFunu/Pnm6FXdpH9" +
                "08aPZgV+fyYO/wFB7ujC");
            
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
            final int compressedBytes = 890;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuS2yAQBaKkmKosWHiRVGVBUjkIlZPMUTpzghzBR41l+S" +
                "PZwkI/aOC9hUv2DKb//RCSLERZkAK4QI8edmhgHtgf4RAGx1i2" +
                "k+dM+6qFvXxihbocqvaFztJ/ak6VUR8E3QaS7um1wP/2eX5aPH" +
                "++/tvG/nuDio1/P0xG+Zfe26bo+N/bon3977VR3bPnTEtP+g/o" +
                "QZ55hfqTC/+0vVhTG/qjGtqeAbasv03ctH6aNc38da8/6ujfde" +
                "Qv4h9gGL+0f/wuP3+BRlNhQrsc5G/gP/QPoI562OtGauWXKdgz" +
                "KR1SAf8D8HGYG+OvrmW1Z08e2+4hW/4qr/y1PZlLO66/9LyAW7" +
                "V+9I43T+M7/VWn/792vEH8AEBYrbcp5k45PwDMp6Y2bNCW0WyH" +
                "b7BczDl+oH/sJvPAH9+j7l9ssH+ivUuyGCu1oEJG5c6ffDaI74" +
                "VZnn/Wn3/2ln9aAAAwARBSAAA49n8AAEBCoDoAAAAwH7yv30X9" +
                "Zx05hd4/Uc5mlwv2n5rwn4nov3SLX8pVhWr3Z1NXEtz/whuwHw" +
                "AAkbvL2v6f+vx/7fMXyVXgfyCIv2p1Y073C4XOqz699UqjeXHT" +
                "jRv9U2OLt3+m6wcZICxxsnx3/oOuMtpD6PmrPyn9v7X+IrH+Oq" +
                "xf0vWTGfHvSsx/MB7uePX84cggxC8Qq38M9w8+fP3ja3BdcDBz" +
                "4VCe46pWhVYsu61SDgq8RTTx7ZNAPPubGZUC+3cAwA6m8vGC9/" +
                "xN6f5fu/6oPX5qlz8P+6V9/sJrMmVe8Cm53ILB4wEA2BE/bxlJ" +
                "QpE24ouw0v2+1CV7KkHH0+GPb4JUu+f769qM38xf8f0d9uPH25" +
                "pJDrVkfpdYfsBrra3u/5jPdgj2B/aHy0JKszB/1z1/eer5xW/H" +
                "W/4Pxu/y/GJds/+Tp4ebtpkqrho8xL9atv+93fWXGr8/AKzjbz" +
                "tc/2eZE7fGSy9NLl5cgYP4rMcco7qvtg/uo6v/AazfAIAD9Kt2" +
                "A/l37R/V85/OfHKa41i5Nf/hcf9GDqt4TiCueppZfsiEALjE42" +
                "PD5il2xvZPd/1Ewfw96P4pn/7ESn9a4z8alf8j2u+3GMH591ui" +
                "1A9im78j4w03/ucY168d7D+Lf/fHU6L6fRDNaIFdcf5Pzsufvv" +
                "3uBEbdo0deBB20B8cm/sLk1175V+j/H8KOozQ=");
            
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
            final int compressedBytes = 840;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUty5CAMxQwLT1UWXuQAzE2oOckcRTUnyBFy1JSTjivd7Q" +
                "9/JHhv0eVOlwxIQnoymCh1DXfym1FqWT9nZW9/sUrfLvX6QZ/S" +
                "v4ya1PyqaBOk+fS+YTApko/9f/Xt/9/TOy+x7Qfpb0d+ydD/6f" +
                "4rqbJIst+bv/0owH5AKuyPa13Vnx4aD5+W1VBg/j/KU8n4yyP+" +
                "x+cv6TBLW/3XsL90+3Xjf3rUTBacr6bG8gCQgq78b/KghRw5Mw" +
                "AAFVGoNnTQbEWCLYukGuX26ie3+uLnSN7XwU1r/TR9109rPqOr" +
                "+n95qr++5PWX/Nsqv3DUiJ5KkXIAkMC2AaDb9AxAf+LAmmrMh+" +
                "unpn/LxK4/kP/62T1/fuDfnelPP43//77+FL10FJfqTBTNeG7q" +
                "REHtSW6tLJMVzx/l9h+15h/ka8aD+Ets9g+UtF/Y/i3v/UuB6z" +
                "cmq01r5G/VSf4FmOT/+cjX3fNPq4TlET941C97+rvIHjZz+6i/" +
                "gZHjV4b8awXmXw78wRaLa33AsbT/S+jzn46ffwAMYDNEj0bRRx" +
                "9c72KBqYG6qF5/jbF/GzMZiPIQ2uZI1vW7yU9++sH/Y9rf5OH/" +
                "XdfP4PdlakADdbCpn4HhIx/W33JhZmS/2/7/3++b/e72L+3u/5" +
                "9hQmDYRwFyiaocimKKqSV2/dn27zRy3Uouf7LH/Mlu/GlOn1zk" +
                "dlL3n0tXdqvYPw7hOLL/KlP/67TvTkNQh8D5Jf2TqI6qhXHzH+" +
                "r/nPqz0F9CcYDxY/7C/2TF/5zvvxBT/3WM55+E8zelzT9ipv/0" +
                "+qsA/7XIP7zjR53NKTzijxnWf1rHjzz2j48/qP8BPvy3TP0vPU" +
                "+i/mcC6ee3+/e/Gv9l2j4ANEqnGlqIxdLEYEnvX6T2v+Pz18x9" +
                "YLcpfoBoD9QiWW3P37a9kVYAyJp/7fb8YG7SvsrZPup/AAAAIF" +
                "P+Ynd+LgD7BQLPj4Xwrz78V67/FNi/uPCzH11rwfP9R8L898W8" +
                "eznM8wdKPD+Tsp2f6WT6TyBSFn+Czl8EysA2jKrUMC9PKe0z5U" +
                "/g38j/o2MI/nM67Gz3ILajLfb/p9f4raPqL+yfZeDBUvxP+vgB" +
                "IITej9l/ggtwxQfij93H");
            
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
            final int rows = 605;
            final int cols = 16;
            final int compressedBytes = 736;
            final int uncompressedBytes = 38721;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXW1u2zAMpV1jUP8JQ3+0wH6oQw9C9CQ7inaTHnVx06QZ4K" +
                "ZyZJmk+F6BwDAqS/x+lO2EqEsM6/59Ui5OvrbyOH8GSh9nEo0f" +
                "h+P7UJ4/76aDSsLDxYVyIN5g/gp5whZXsiA/E+AZmu1fED+xPn" +
                "5c6686/7A9/Q1t64c2/d/AH5IO/lArf63+ET8S/Gs7/92Hf0nb" +
                "T5p/a/cfv/XfFUaoQFv+3Sb+pefXjWFD2jYIjwe+C4SxQbACax" +
                "BWGYyX+BfPrOzdkm8zexhm/jWc+NccQ/kK/wowAaCfZGXoyjB/" +
                "A4CK/YO86/7BQmaatr3oWMh4kz4TRjrSjbMQRyP9pOPJr2SJhf" +
                "LTLvIra+2L7x9Y2H/WrL99+v9atLO/9Pr30b9tqqg4zj79L5/9" +
                "76HU/16d8EcmoDCiE9QAeKrf6P+a89e8gv9cdizh2w7BSWLfTc" +
                "wgPJ7Mzs/l4dZpFsH8QKf8mxvWT2AL/mnXf/H8ouz+6cL4Vfsn" +
                "l+Nzp/Zrv87W9TM3k186fj34f8918n/9J/AHQF3+w/4ZABQg+p" +
                "u/Cf+JLr3nRABkWAgAAMARXe2/sXH5V+6f3fD8T+v+O5uWvz3/" +
                "sVt/8fxrD/yv7/ohlD/C4qFA/QN2Al7cNNp/TpIzL9a/9HX+Se" +
                "f6F9rNr6n+soD+e+IvrFh+D/cf+6/f+vJnT/5ncP8EAEoaw93H" +
                "A0AzKL1v6SR/J8L7i/Zg//s3rDN30/2v9fs/0uv3APQ/euOnFs" +
                "+n5Rz+xhwi/aA08Mt8KsyxONHb4fDXI+WRpkS/T8a8j3/p6c9V" +
                "BhdudQzcBkD8l473sP/OcMHKbY7sOv5g/wyNAG4A/gQA4N+C/G" +
                "HT+ivPf3Kz/gPfX+u78Hl4fttg/9LV719Cf/j+BxeIUIFN4P1p" +
                "q/lPaf5F/VHUf7Fi+1nnv3h/wrf8APiHPOuu+P0snfXHTfwj//" +
                "SfvxnyA4Db/oGNr5+wfgAAWuAff+3vbQ==");
            
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
        else if (row >= 2436)
            return sigmap4[row-2436][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in sigmap4 lookup");
    }

    protected static int[][] value = null;

    protected static void valueInit()
    {
        try
        {
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 3967;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWweYFEUWfq+qw/QsCIiCCJzAAkuSoIIgUYIkBQEFSSoYCE" +
                "pGJAeRaACRTw4QBBMSRNAVBEwnoIj3KXrqJ3jIeR6oyB2IKAfe" +
                "Mfcq9HTPTM/s7C67yN319013VXV1dXf9/b/316saQOBsLYTAYT" +
                "2hGBSHS9gMKGcOgMugKmTxuexlLGW9zWpDXajHH8JtTlFoyMrA" +
                "AmjGv4HWrLfVHjpBV/YC9ICe+KPZ1hjHB4c49IP+MBAGW23AwM" +
                "ehiNkEf+UVcAVcBBezB9hSoyMctRdBBWMt/Mrvh8qsi9EHquE8" +
                "qBHq5xzC5XA5WwlXQAO4ho9mPaANm4XPQgfoyD/EDnCDcylfyE" +
                "axbnwB9GZd4Q52FAZgSTzKp0AYSnDDHIQLoCRcCKWhFDsCZaEi" +
                "VIJMNs1owYdALfwMarPl5p+gPlzNarGyzkFoAk1xJX5rtIOW0M" +
                "5ex6ZDF7iRHYPH4Sa4GXpBX1gEt7JycDcMMt8HBia7Giyw8Qjr" +
                "y85ABhS1/gAXQBncG57E+kF5bA+/w+P8Y/sDqALVoabRAb/HQ6" +
                "wX64xPQB24Eq7ifaCR1RcaQ3NoAa2gLVxn7ob2cD2fCp3hDHSD" +
                "7nCL9Yr5KfSB20LF4Ha2Gu6CO63bIhFWg2UZXVhVGMJ6GjfCvZ" +
                "GI9S9WPfRzhDZWhc+NRLAUpa4Uef49bguvpFwZvC4SCdWPRIyR" +
                "9pmIbwvtCzUivEw3b7Wh2vWo/BNWyaiMK1Sp3dh6whgjz9vGcP" +
                "4THZ+xXtIt9M9g/hb5aGuTOJrbVd6sI0sXUis9+AJqXeJFT1mS" +
                "Sl+LJGzsSExrQ6ikLv12mHvl/We6Z8zFXi1jq7zyGLb1X2vdLv" +
                "ZOWJ5br1vvq46OfBtWPzyFHde1r7Fqhlro9EI8Y1WRNWr62jvt" +
                "b93c7aZwQLTs0+gTdYZ76AqBV0vgxkzFL2MO8WsojmGtYBjxaz" +
                "Rk4WheHks5Y61ZcD/Usyvh+EhE8AtHQbNwBRhhbXbaEL/G2c0F" +
                "v6zGzn7nHhhFdQS/xgi8wLBmyye9n55E8isSCV9ldDToS4AK1j" +
                "HqQ8KLdbGHQTUqqSGfcwlcTmniF/XEGtbD/17YQSL+O5VT/JLl" +
                "Aq+3qKSEWxMuVHgRv0YKflmnrN8Lfkm8lpsHFL+sflRT8ot6pR" +
                "0Mh3Z0drq8xwQ6c5/gl8amHOUHyZZN9qqHF2TQexK/BF4ZrXhZ" +
                "KB/aJPglaxJKgl9MPq/1LGsj+BV9QskvOhK/nIsFvyjdGSd5eA" +
                "l+UZng11i40+lKabKHdgu/PTS7QTmrgbKHZifjUWwcft+aI+wh" +
                "a281Yk2godmZNYZmdG3rkNhH7SHr4HztDJNPou2hcx0YrB4UoX" +
                "d5jJ63ocDL+jelnjC7C7xCgwVeUDl0X/gaDy86arzIHt4l7KEs" +
                "I3sojxMUvzy87Ikgv0m+T9hDfUbaQ42XtoeCXwIvqM37mwcFXr" +
                "ou4aVTLRVeyh5SXttDSt1KP7KH4aXSHm4R9tDFy7WHhFdrgReV" +
                "ud+TtIfWYvaoxvd6ZQ/pTCMXL789FHixpsoexuFF9lBiWBl4qI" +
                "bAi++ASVCcb7cfhKkwmfCaAlkw0czCzvZ0mCb4xXfCA4Jf8kkE" +
                "UiOs7HBV6CTzhBcx50x4ZxSv6Ypfmv2t6YudofglGT5G4EXH4X" +
                "ZFkQ83DcKL+LUpErDxwy5e9LvD+UaX/pCKX8oeQq3QR3RcZB5K" +
                "xMtaovil+/ZYLL9kTZdfb/jKMuSe+BXaQy3H4aX4ZXfTT/huMn" +
                "7R3sVrfyK/yB4+qPGqCY/AwzADHnKKwTyYYx43T7HqxK/ZMN+o" +
                "aJ4mvEo5xfX1J+zZTsnYngvH9GcGhN81/+mz0W1glk5NEPzyve" +
                "VRffzVlL4yPCsSuDlrgkrNX+T+pGwhag/NY5EcNuG/5PFt82Ak" +
                "Hxt7J/BZLxJ4+UtA+sdQJftmfV1NqJOqXfNHuuaMD6+50ZY0Q4" +
                "1MOwLLSW+QFoBlutXqIYAnhd6QPVEq1R0yjJhcx/CuGB/dBpbq" +
                "FKESwqguqezhJfwX4TUnCV7B/Fro6xPPf+1LB6+wsKe/BJ0NZS" +
                "aWweKY3NO6hz4Nuj48kfq4dUz9JbKPO9jd08FL8MuPF6WfiqZW" +
                "6iP5L3hG2EPil/RfxmNQzmmk/JfCK0zYCf9lzKf+K+mzh/RsGR" +
                "WE/3LtYcaqjGp+/6XsofBftuH6L49fwh4qPU9XDgz2X+GilIr1" +
                "XzfE20P6DZB4/RDvv+BZSmn/5dpDKqltUD8m2sNQFeW/qIT8lz" +
                "wm+C/aC/+1V/kvZQ9BPCP5L8Eva3ui/yK8bvHbwyD/5dlDV8/L" +
                "67U9pN9zyh5aH8Fa63NcYqwIr4Dn4QVYbXaz9hjLrX3wIqwhvb" +
                "ESn3T+DhtgFawzltH4qyQus/ayCKy3vpCM6hTHt8Wx/KKvK/qN" +
                "2DN8zO8eLZX+K2OqvuLruG+2Vnr8UnojXXtoPB/I5X5p28Nfkt" +
                "lD65PEcqNDNPV0zm27+tD60trve8uXPH7R3scvHAPlwlmKXzga" +
                "XsbVGT00vzbieMUvoecVv5Q+dPkVbV3zi1KaX/L7RY9fdl/FL1" +
                "m7stwH86tdEL+i94nyK1gfwsZgfpl7gvjl6sOc+WXwZPxyRiXy" +
                "y4fz1Jz5hZMC+fWK5lfFcDXMhm2w2cqELbAJtuIueI38V7bxCu" +
                "FCag8/xg/wbXm3ybgNaaSIG5H0EdIIFnfgHvlNvKm/jY9wA76M" +
                "ryZ8Mx/iJr//8uuNuJq7c+v7ffyaki6/rMsi+drMonnUKVPTeB" +
                "/tvzBG04DuU7KH3Ppc8MvK5F8Tv163B1h7zLHErzcgy76b7gGy" +
                "Pul5amOb0PPOFOG/rC9ghOKXq+fNCTH8khiCYY7z3dXj10CPX/" +
                "qc5Jeyh56ez+Hdesfbw3g9L/k1MpZfCq9Afvn0vMwn0fNmiSA9" +
                "L1v+JFHP+3Aen7Oe53/17KGPX2+5/gvLODRSwrLWe2Yf6sefWH" +
                "GBl/4eHFaElWAh+4TGfBuzqFRaAuW/Yr66yYHfIuHFEr5H+2Qw" +
                "AvH+K1f82ldo/Cqd7EyQ//LjlXPbHl7ONJ/dudTjlzND8IvsVR" +
                "ugkRYrae0R8SjqiSwXL49fzIKGzLYe8fMLy/v9F5bz4lHKf3l4" +
                "+cbLcxP5Zc48W/zCBTnwa3c++VUmb/xS8d4cxstNPbx8/Bqr+M" +
                "Wau3rD+lbENwi1AzAVK7nxDapxLZ2fpvCKj2/E2sM4vTFd28PZ" +
                "QfZQxaNi8RLx+fj4RgHhlV97eEUyvCiVT7xi7u/hpeMbmAk89L" +
                "3Ei8Y0UDz0HRuAlYX/8vMLqyi8CD3Jr2C8zKWBeI0L4lfocLD/" +
                "Ojt4Fbj/apA3vNLxX8F4YVXNrxHA2XAZ7x0m+EX+axBWk3qD+I" +
                "VZbDAbye512gq82ECsrvjFhgbi9aQfL6yRUm+cPJd42aXziVff" +
                "wsbL5RchVw8epn1d+l2BdWgE8Rm1S3oD5mvNMT9tL7wsoEdn+f" +
                "Hy6Y2B+fH3bnwjTm/8kK7esKvkU2/cnqYWmhmvN1LHDxOuT4gf" +
                "YgN4xDmCV+JV2k59J9sd67O5pdJ+i+Xp4xU6HDlLW27jhxKvWv" +
                "nEa3Bh44X1o2U6Pk+peTAnXFHhhU1cfmHjtN/iqVzw6+TZxSt3" +
                "et4unU+8hpwrflGKY3OlD4XeoHcZIPBSeiP8gMsvV2+k0ofmih" +
                "j/1SIv4+W8+i+XX4WjN+xpha43rlX+ixfjRTCb8tdbh7FjJBKe" +
                "h7skXlI7xMajxPgrVTzKXKniUdw3OoalAi8Vj0rHHuYrHlUA4+" +
                "Xg+RT74TSvXpIbfqn5lGhPvJNsPgVvVONlFe+lYzmpD/V8Cpez" +
                "lSLem8CvuHiv+UxQvNcc58Z7/fySuTTivTKXsB4g3Xiv5ldCvD" +
                "epPkwz3mvPC4r3BvErNt6r+JU63ku/FPMpvBSsjSLaTcyn8DJK" +
                "b+BNXjwKxeh2nRp/qXgUrA+w6s+lH4+iO194/vqv0PY8+r3xeb" +
                "0j6hlq7It9sAf2xF7Ym8tvi7f09KEXj4pGmKPxw4CneT43eJ1L" +
                "vcEXpajTOud7OjsLHa9bou/r8etWwS9Xz6MeY+DHVGcD3ib4pf" +
                "wXHd8I5NcqetsBQXjRFZvSfK7d5wO/nF0Fj1fsfIrLL5nO1kc5" +
                "v6r0hi7Jnd54QeuNgfF4JeqNc4tXKn6ls4XvK3S8+vv0RrZczz" +
                "ZKzy/v8vQG7sR7FF5yPfYwgRc0FHhBM9wOrQVeQm/gm1JvrNZ4" +
                "jXD1Bh+p9IaLV856Q+H1W9cbtC9wvSH1YcL8MiAOxWwcicMFXp" +
                "TXeFGqqp9fUJfedlgsvxReVLOr5tcaFy/ZssBrtuCXh1cShlRO" +
                "xq8gvIL45eEVLaXxGA7T6Yqx/BLr55M8SUt91HjpXBQvmZPrxT" +
                "28KC29M5QJbLF6Ir9cvHSNVgnXRPHy9csIfZQjP9gm91twKmxV" +
                "/guyPXtIvxh7yB9FvV4Sd+ijwmutwivRf/3W7GF+/Vee4yJ5t4" +
                "dTXHuID+IMyouZLckvNf7y80uNvzy8hN6QZa0VXmL8JZ9mHX/c" +
                "w0uNvxS/UuuNVPwKLSsIfoWWnHf8mq7f4EV3vlLWpLEx36ziUT" +
                "hTzH/xbP/8cg7zXy/GxKNmpZr/8vuvVPGo0PqCmE8Jrc1fPCqm" +
                "TtJ4FF91FudT9PoNvpVv4dKacd+acHMsRvtezH/F9xMbGsj29c" +
                "nGXwW54br/TnuY5G3341d4AL+k1F+kUn3KPCXxkv8hME9T+d+i" +
                "dztBuRR9Yso1jf718+ngpdbPJ40kLAu8xrd+3sMr/fXzoXd/m3" +
                "iJ9fM54HUSf0a5+gllP2e8p/E6nQe8NhQAXusLBK9d5yte/P1o" +
                "KsbTs4lsUrw9ZJNT28OC2IL5FWwPc96ieC05N3ils140hxYWef" +
                "EN/f7y22OLcx/fcNf38j8m9GkB6vm84JXf+EaeezsX3wkG/meJ" +
                "7wWOJ4Q+FPYQ5NwJy3bnK+V10h66+hD3pZiv3BijD1Out0lXH+" +
                "Yw9orRh7KkUNbbpKcPC2J9FD+CSqe/HkXwuD8+z4/lIj6vecr/" +
                "4bvjrMKLz6fPr8LShwWyfiNu/KXjG5ep9YfU1yX4iTTHX6/6+c" +
                "U+OBv8cjILgl9OxfOOX+56gFPA2Z6Y8fJxP14uv9LCa7PLr/j1" +
                "G3kfL//fHsbi5Z9PYX/O53zKa1pvnDk38cP07aFT47yLH+r5FD" +
                "G/TPue2IsdUCWGlef55S3J4hv/q/7r7MU32FceXsYF2JNFR8VG" +
                "+TzjtfXs4+V0KRB+dT5/8DIu8ePlrQewMtlB/3oAdkjXJy/jrb" +
                "eJXhW0HuD1cxE/zAte+f3/V+Hzy10PAP8BtXXoGw==");
            
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
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 1689;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmmmMFEUUx1/VVvX0zIKwiIoBFlZ3vRAEXbzQVTkUVhSFrL" +
                "gKHqCCGw7ZeIDxRmPwQDkkxAgKmPhFiZgJfkBwWXcNcSOHBxBU" +
                "AroomgiKmOCntrq6pqevme3p7tmZnnQlVVPVR810//q99683rS" +
                "iKgpIKL/g3tUU7FIUu1Lagdlb3oA7UIsZb0EbWbkJbWfsFq21o" +
                "N9++TW3pVrQLfYw+IVcqhkIXsf070WbFVUFfmcfxJV2dATMV16" +
                "Vsrpj1ZaUghT7p/ljUahrNEFeL8CmUJPfxfi9W+6EdMEDlBecZ" +
                "ecFwOy8Yq/GCyYLXNsHrXj7bbGjSeEGP7LygOhMvuJq343hbDz" +
                "dn4gWzRD+hbz2dPX//iX6VmRcMS/TL8EtGi8/b4TbD1ums3sPq" +
                "Q3yEeStBTN/fk7dnO854oZ0X1LJ6lX7EGNs5U+AOuNPCa0GKV5" +
                "lMXhR9zou8lOaF47gHrsAyGw0XNiixKn4pjBWfk8Wv+dz0rQZe" +
                "uGdWC6m2b8Pr8skLr+0OXmSxH14Wa1ss5lxBlpOVxOKF6ELSoX" +
                "/rW66tvcVx66L8+hj0Ye7+MJN9FZM/dC5kle2aTqTjF46X/aXZ" +
                "l/7cG+zL9mu2ixmOGebfo/LKbl/+ipf4lRjt+v7sLg5e5Btx/x" +
                "dAGbtmGeJ4PvvszfZs4HdhEKvns/1NuBnPYf0nYASezfcwNYHn" +
                "QR3rcZ8Kt/J2KjQa7uH9MANzCwViurd94QzDaLBp3wWsXiT6Q1" +
                "m9FC7vktXdVl5Qoff6iM/+0AznwLkaL7iY82KeHEbCFeKIa+Ba" +
                "0XsExptmewwa4C7D+GHeUtMx5bw9TR8PZLVS9GtYHWLg1QGXwG" +
                "X6kaPgOriefd4EN7J2Akxk7STT3NOAaQFg6gLdAA9wbzIdTUNJ" +
                "1Ig7yUbhX7zrQ0XojY+C04d5sq/6sOlDfFDY2XaiRx06Jh2/DJ" +
                "bY6vYbpN6FiF+eeE0MW/xK6XnyneO8C+nzOq/vXfPqExa9gdtC" +
                "qzf2pdfLRn9I38/dH0pThD/cG5w/TDTmRR9ODZs/NBD7ka2XmX" +
                "ckh4y8JOqB14OC10/dGb+88IqdFV5ezv5QKtcjXTM57NIfPh6a" +
                "9dfM4ueV5YqT0rNGf4jZmlzqxUjV5GZfSkof/mz7hsi+tGd/iF" +
                "/7IkfI76STHGW9E+QY+ZX8Qf6WljFeDfoR/5iO/8VTlDxO/iwq" +
                "+5pbIF7j/M4gLSf/WvUGZrpeaso9fun2dTKyL/+8nO2Lgrr+il" +
                "GbRXR4WH+9V4h7ECpevtd9NE5jKBmTzXo+xctsX9L67PaF6zX7" +
                "onJw9pXYUEp6Phde2fShZIsusSM60R5KAUvig1LiJR0PRh9a18" +
                "uMV21spNW+8DPu4hftG8WvDPb1nH/7gjKVF8TpmfxKvtV4yQOt" +
                "/hBGlLWneUGdmZeWn0/neyH1/zUx8uo6P6/xkvur+Xl31yVXmm" +
                "apsF2fOpeen0/bl1N+nvXHW85uMI1c5uczPi9fms4cxSrPz/PR" +
                "BN5OMvJK5+flQVp+XlyzySKw+E8U12jrZVrp/YmS5+X7mfWS7w" +
                "3D+styH+en1190kLr+ooNT6y+Vl8xW0KSq8OuvxGclpTeG+J2B" +
                "DnOOX4kBdn0oT3IZv4ZG8cuNP/Sa36C15Kg8O53fYFvXJkbY8x" +
                "tkjRf7Iu/6sq/9Dr5hVljti6wPZh55Tgb7jdOn3L+/4fgL9/Cz" +
                "iuz9Dff2Fez7G971fOr9DQcP+bTallfLSeP7bJqez52XODOPvE" +
                "pVz2eg8wLT8ydVPY9OGfWhquc1vYE6xXPMPSQ6IEZ1lqfc9L6N" +
                "Vc/rW12+b5NvPa/yKoSeN+uN7Hqej2x6ni5R84fxpVkssdXP8x" +
                "Dp+WD8YUrP01dUfZi4xaoPRd9bfn5vcPqwvCJQfRaifFSG/Pyr" +
                "pKWLSNeqFLCU9y0lXoHF7Lez7HvHQvi18F5nof2h/0JX0mUoSd" +
                "+gS+lyuoq+Tlf494f0ze5cL5eqfWX7P4Wudm9fYS4lYF9sxY34" +
                "//dpPW/h1WkaHejm+FUVxS8bEYf8oS9/uLnY/WEY7Mvt+4d0iz" +
                "EfZcxveMlH+c1vlD8a2VeOHrO9oHp+UcQrR9topl8XkNeyiJdz" +
                "/KK7AotfOyM9333xy86LVQdefNxG99l5OczpPR+1OuKVqz6k+3" +
                "Ozr0B5rYl4edXz9IBLf/hDgLzWRbxyOEf4Q33MebFP3R9atORB" +
                "Z/tiR3rltSniFTwveihf/jDSG1540cPZeeUxfn0a8TIW+B+/P4" +
                "Ie");
            
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
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 557;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt0rBFEYxr1ndo7d2lV7oYh7N0i5FXKPciNulY+w2L9LQl" +
                "LSSj5KlI9bUcoWoSUpcuE4u7Zttpm1Ztth3vE8NTt7zjSzzf56" +
                "3vO+5xyllDKvlUV0YPm+r48zfWzn25u0pD9XKJVv79Fp7ryVb5" +
                "/QMq0qm+iY1tWPRIfKQxmzio1ox/09ZvqLV+EZOV76XOBl+xVH" +
                "XrofvKrGi9Yq8Zd5A3/5zl+38BcnXlmJpHmnAiJOvEo4KBO6oD" +
                "XzwbwPXTnHQ/ORjpziIe2WioehS8RDr/xlfhgbRu7fNlKl/CVm" +
                "bH0J+OsPSZbJN9z6S9bAX175S5K+8pK7/lbivnRR6xzj119KCv" +
                "BiSs7A+MWAUgj+YsVLgher3PBVhp140TsXXrT4r/wVhb+YE4xZ" +
                "8oqIiIq4CFt6dPwUtW6fKWLgVSU6dfAXO2b14MWGVQPmD9kxa4" +
                "S/giORlE1BeZegrH/JZqx/+a6urHj/huv1lBbw8pJXmdGtNTix" +
                "PQD5YYds/95fstO1v9rgL6/8Jbtkrx6/eqoaD7vBy8t4KPuQzw" +
                "dDsl/n8/Gi7L6i+UPw+iVeA+DFrl628ZKD4OUjTw1Z6GA9hZe3" +
                "Ill/yWHEQ0bMknIE9bJflJ8/HMX8IZN6ecz2Ts/WeGg8uR+/jA" +
                "zGL8+yi/H/tB8gANngBHix4jUJXqx4TYEXK17T4MWKVwK8WPGa" +
                "Ay9uqvr+jXm/1susqDjXywvwF6t4mAQvTqr5BEFaS2g=");
            
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
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 566;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnDlLA0EUxzPrzowBg6awtrCx8SsIFoKVfgRbEYkaFY/Ks9" +
                "TEqPkGHgiGKGIjSogKohC1FQULtbIMmMp1chBzeJIdd8b8H8zu" +
                "zF6E95v/e2+WJZZVamz4vW+4jTrDa9QWHGGiceuXZngsmC3GRi" +
                "yLJNM9kvr4CvJQNLqBzxzlNQpeWvEaAy+teI2Dl1a8JsBLK16T" +
                "4KUVr3k2TfbYLJuxLBpmU3SZnBXQORXtmlyQWG58QCJiu0MOxf" +
                "ZYtBNylTl+lDt/SaJklwbLmCfI/s9+DzmvGs/PfeOJeNkdPT96" +
                "bgCzWiHKwW/Xuu3wkkK81ugS2aMBukhDNEwXEA9VsfJ4mOG1jv" +
                "ylW/4SvDYKrquYl9kDXrL0leVVNKqYF9sEL7m82Bb0pUn+iqDe" +
                "qPp4GAUvafraQX2oU33IEvl18cAn62W/0Z/r9eaP+TD/lZ0Hj/" +
                "CBSmbeinj4JPb3ttWHd4iHsvKXMVQaD9lz0fk+xEPlSNpdH75A" +
                "X1rV8ynw0orXK3jJ4sVd9vOireAlU19fG3fDd/+73uD10Jdz+j" +
                "L8vAHeU8e41259mUnoS6f6kDeCl1a8msBLK17N4OVMvZHlVTDO" +
                "8BL7w9z4pCQXtqR5ffgk8FJXX23Ql1a8OsBLK16d4CWfF++ybf" +
                "21DV6Ovt/ohveUebvhw/eHVZ+/BsFLFi/uT/Pimf9IoWE+BH39" +
                "WWQb+S2v9PeHPGS/vmo84KVVPFwBL2nxcNWMfX2fGYfv1DHXG5" +
                "WG34A=");
            
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
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 662;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnE1PU0EUhj2987XmX/gHdKcS3bogQRNXLIwLE6OBpXEBis" +
                "JGEQs1GoP+ADViGmNCQIKYGFSEFR+CIVEUjWiMJBg34/S2krZo" +
                "WuAeOwPvSXo797a9Tc9z3zPvzLTVt/RNyuo+uxb0vKj9zFp9m1" +
                "7QcGF/gO67bT8Nuu1Tdxulifj4UOHx1/SAHuobtixonB7ZqoLG" +
                "LCKfiZHNvS7Voh8je16RzJbslemLJjesrwHoi1NfeV56qBKv6E" +
                "x1vKJm8KppPXyC7HlDcVW/opW49bPk+K+11vuS47PefYK7vuZW" +
                "v2Q563gUV7No8F/6Sp1ad+w0rnRfw9XDCWTBG83OiDk3/pq2Vi" +
                "wk5g+n4Ddq5zf0G+TOp4j1NZekvsQ89MWlLz2v3+Zb0Rj8xrbo" +
                "4RaQg6B4fUQOvOKxTFm1v6hubt0ffkH/VTt/mGoxCtnzJUydGK" +
                "7gH0eQJc+Ul/B6itmNeshVD80e2U1Z2SWvyGsyIy/L9NZ5yavg" +
                "xcZrr2pzfuOCOm+tzKhW8Ppfodo3yks1OV716kDS/lDtAy8ufd" +
                "GqOYT1LyYvd5DjrLn5Q3MY84dh6EukTUMFnr3IXVAjtEbkwJ8Q" +
                "i+KTOSKWHJej4qv4ID6L72XP+FGy925T7/JNLCPX3o6Xj6H/4u" +
                "q/qqiHx5E7rypizh+egD8MRV8xr5PgFQovc7aC/jA/v939xjno" +
                "i0tfpo2BVyt4sfG6+IeXaU+s/7oHXmy8Ohj0dQm82Hh14vsAO9" +
                "5vdEFfjH6+O88L468weDHoKw1eQfHqAS/m+ahe1MNg+q9M0voS" +
                "K+DFxUssmutuuxS34/VKU/bvNFivDCdMX05fRcxjfbn7wm/TaX" +
                "TdVeH09derBfry12/cQT3k4rXrN+TMRao=");
            
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
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 604;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms9LG0EUx51kZ3be2YOXUjzYiqAn295q8hd4ELR4ak89iV" +
                "DrSS+29WpbFfIHKOIPUBJZBGvVoBVEweq9BbHHgv4FJWbXHDbN" +
                "j7p0Zp0x3wdhmZnMhn2ffOe9NzuFQj1jB8XPWai9xdaC63ap/a" +
                "38+3KOZdl61TttFG5l7KgAu/HEXvQ5cj4ar2IPeGnnxbyy1uHf" +
                "+mLHLB/mxXI+L7bv82KnQf9Oafy7z0suVPzCCXjFoa9AY4vwnU" +
                "nm/GCeXCpez1Xpy/kJfVm1Hi6Dlz5ecuWGF/R1T+JXFr4zx/hl" +
                "EL9y/HctffEr6Ouex69N8NLPS35RFr9WwcsqfX0FL1285K56Xl" +
                "yCl1X6yoOXNn1d8Gnm8U/8I5/hGT7FZxXo6zN4aeP1y9eX+zbI" +
                "7TPuG/CKy9zRqLzEyyKvP06+/jxnD/91w5QXil/E/l9f1AR9xZ" +
                "FvkENJrIdm8yJXpJknUirzQ9EDXjr1Vd9Iwncmmb/fSy14n9K4" +
                "9bK7pZcXPWhkXoG+HkJftvCS076+qBXvv2zKN6it5sgj+M7cel" +
                "nFekiPoS99+nJPkoG3k9vVxxMjiaGKvuG7fBJqb/h8vgP5hm31" +
                "MnXaoi9YXY5d8IFVvJ7ABwbRePqv/FB0R97v7UL80hm/lOfzz8" +
                "BLLy/qUcrrOXhp5pVWyisFXvp4BfVXL+ovO+MXzm+YzYtehHlR" +
                "v5L1cAC84tEXDYKXubyo71YV2isrnq65gUiqrr9eQ1/a4teQeM" +
                "c88UG8989jiwmcP4zLxGRUXv557BC5serzsD9vl9E4fGCONV0D" +
                "BsKuRQ==");
            
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
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 502;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms1Kw0AUhXtrhvTOQgTBjahrF92qC1G34kP4AiLoA/gG2l" +
                "ZF0EUruhEUbSWoKJbiD0iLf3sFH0LQXYxtFa0IKjMmMz0XmiYp" +
                "3ZyPc+7cSXjGXSHPzfrvRRcfzs99381RhUr16yPaDo4FOg6Op8" +
                "HnjG6q94v1368pT7vust9QdEV7/o+Kyj6qpsTJ3/7HaWgXMZLe" +
                "p6sGf9Htb/3FGfhLl794UT0vkQQvnXmo3F/oX2bxyoKXPl7OHX" +
                "mcC74fVPFy7sFLHy9eVe0v5xG8jMrDNfAyitc6eBnFawO8wtvf" +
                "iE/zJtSzen9jC/4yKg93wMsoXnnwCq9/cQHaWd6/9uGvUNeHB1" +
                "DPnOJDaGB5HhaRh7rykEt4Xmld/6pAPXMq4HUJFaLav5gU9K8Y" +
                "8vA/9jfYcafEgoL+lQYvfbz4Ce+LmuYvfla5PgQvnbxe37eRAu" +
                "/bGMWLwcue+Uu2Qr3oVNVfbfCXRf5qh3pRnZdV+CuRgr/0+SuR" +
                "qfFSl4eyA7x08ZKdb2ct5e/yMD7x5d4k9AwzD3lIZR7KLr3+kt" +
                "1Nv78xopIXD2vm1dP083Iv1vMWreeTUC8qJftEsD4UKTEn5sWS" +
                "mMXzFBP6l+xX2b+ccfDSy0vtvCwHwEvb/DWIPLRrvSFHoV3U5m" +
                "XFeTgGf+nyV+wFDLK1UQ==");
            
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
            final int rows = 45;
            final int cols = 108;
            final int compressedBytes = 454;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmDtLA0EUhXNlxySzveCP8K8INhZaKWhlZac/QGw0vvAXKI" +
                "iomLDYSCS+QBSNgUAUfESw8QE29teJpggiImSG2bueC7vsozsf" +
                "58yZYWamiFuGTlqej81VoTMqNd93acvc81Q090NzHdHl5/e95v" +
                "8ybVNB9/K3oQva4T8NnTLmS4mDn77qPvu8MlPg5YoXu/BXP3iJ" +
                "4jUAXi55/T4d43oQ6sWKpG1/DcFfovJwGLy85uEI1Et0Ho7CX6" +
                "LycAy8vObhBNSTM4bXJFSIy+hpNU+RyqlZtaCW1YxabD8P1Rzy" +
                "0F8e6iVoF99+mKX2/ZVNwV+i+uEKeInitQpeXvv8GtRL9PnGOv" +
                "zl1V8bUC82+698cENR+oo5qNvyV7oGf3ncfxWgnaQxeViEConu" +
                "GyXkofv9l963xSvYBC+P69c5tBPVIMvQQBSvCjRIeN+oYv1y3z" +
                "cs8roGL1G87sBLFK978HLHq3F+qOs2zw+DW/Byx0s/2PZX8A5e" +
                "HvfLj9Au4X3+Cf4S1Teewctx33hB3xDTN17RN+Tw0m/281D1gJ" +
                "ek9Ssk8PLX58NOaBenafSNsAt9Q1I/DLvB6/+uX5kceLnilfoA" +
                "vgcVlg==");
            
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
            final int rows = 43;
            final int cols = 108;
            final int compressedBytes = 375;
            final int uncompressedBytes = 18577;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MtKw1AQxnEHAlmcRxDduPMKXjZenkRFN259Gwt2UcG1aC" +
                "1BEK0UqyBWqt0r+BDu40GyUESrkGPzmf9AkqZZpMyPmTmnadov" +
                "3HBKFCos+XB38+7ztT961rFWdn9mh/58bE1/bvvjyh7evr/Int" +
                "9b3Rpu5NMbunbyw99yi0eWics/8xrFK5xX9GiJG/PX57y8oie8" +
                "QtZXn/k1Tu6Uwk2QgwJpTMZVS+La1/Mr3vttP4x36YdS640pvK" +
                "S8pvGS8prBS8prFi8przm8Au+X59kvq3i5hbzrK3rBa4D/byyS" +
                "u4JJ5j2/lqivwPNrmfklM79WmF8lX8+v4iXltYZX4Pm1zvwqcX" +
                "1t4BW4vjapLymvLbxK3A+38ZLy2sFLyquCl5RXFS8prxpeUl77" +
                "eIX0+j7cAbkrmGTe9XVEfUn1wzpeUl4NvKS8TvGS8jrHS8qriZ" +
                "eUVwuvAe6/2uTun++/OtSXVD+8w0vKq4uXlFcPr1BeQ6/gfC8y");
            
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

    protected static int lookupValue(int row, int col)
    {
        if (row <= 44)
            return value[row][col];
        else if (row >= 45 && row <= 89)
            return value1[row-45][col];
        else if (row >= 90 && row <= 134)
            return value2[row-90][col];
        else if (row >= 135 && row <= 179)
            return value3[row-135][col];
        else if (row >= 180 && row <= 224)
            return value4[row-180][col];
        else if (row >= 225 && row <= 269)
            return value5[row-225][col];
        else if (row >= 270 && row <= 314)
            return value6[row-270][col];
        else if (row >= 315 && row <= 359)
            return value7[row-315][col];
        else if (row >= 360)
            return value8[row-360][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value8 lookup");
    }

    static
    {
        sigmapInit();
        sigmap1Init();
        sigmap2Init();
        sigmap3Init();
        sigmap4Init();
        valueInit();
        value1Init();
        value2Init();
        value3Init();
        value4Init();
        value5Init();
        value6Init();
        value7Init();
        value8Init();
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 17, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 97;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2sENAEAEBED91yyhA/Hw8Jgpwe6Jx0UAAAAAAAAA3KhRGp" +
                "D89Uv+yF9/vD/zlZ/8Qf8AAAAAAAAAAFb8nwCw/+1/APsZAMD9" +
                "434DAAAAAAAAAAAAAAAAgDcaioDt8A==");
            
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
            final int compressedBytes = 83;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3LEJAEAIA0D3n1nwNxDEB5u7PilTJgIAmKpWrvPbfgAAAA" +
                "AAAAAAAAAAAAAAAIBr/hMA7DcAAAAAAAAAAAAAAAAAAAAAwE/+" +
                "UYAzDyrfuTU=");
            
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
            final int rows = 605;
            final int cols = 8;
            final int compressedBytes = 50;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0DENAAAIA7D510wCDjjhaSU04VOvShAAAAAAAAAAAAAAAA" +
                "AAAAAAAMC1ASlrHt8=");
            
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
            final int compressedBytes = 128;
            final int uncompressedBytes = 11317;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmMkRgCAAxPDsTOqiaS88S+CrJJOvr2RWGFJIBWLxC6mFKe" +
                "uAQux1gNn1ogNM600HmNarDjDndacDzK5vHWBanzrwvBZby29b" +
                "zzrA3MNbHWB2fekAs+tBB5jWow4w//BDB5jWuw4wrR8d+JYi1d" +
                "3NGh24a7G1fJPwAtzLGAY=");
            
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
