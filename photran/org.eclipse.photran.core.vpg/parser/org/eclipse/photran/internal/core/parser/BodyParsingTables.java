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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 93, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 1, 30, 47, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 16, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 3, 0, 13, 4, 106, 47, 155, 156, 157, 158, 5, 13, 6, 26, 162, 159, 178, 7, 179, 8, 160, 0, 161, 163, 168, 180, 181, 169, 9, 10, 97, 182, 183, 184, 11, 171, 185, 47, 12, 172, 13, 186, 187, 188, 189, 190, 191, 192, 47, 47, 14, 193, 194, 0, 15, 195, 16, 196, 197, 198, 199, 200, 17, 201, 18, 19, 202, 203, 0, 20, 21, 204, 1, 205, 206, 74, 2, 22, 207, 208, 209, 210, 211, 23, 24, 25, 26, 212, 213, 178, 179, 214, 215, 216, 217, 27, 74, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 74, 228, 28, 229, 230, 231, 232, 233, 234, 235, 236, 237, 86, 105, 238, 29, 239, 240, 30, 241, 3, 242, 31, 243, 244, 245, 0, 1, 2, 246, 247, 248, 47, 32, 249, 250, 86, 251, 181, 180, 186, 149, 13, 185, 187, 188, 189, 190, 252, 191, 192, 253, 182, 4, 5, 96, 6, 254, 255, 33, 34, 256, 105, 257, 193, 258, 259, 260, 194, 105, 261, 262, 106, 107, 108, 112, 263, 115, 120, 122, 264, 183, 196, 265, 266, 267, 197, 200, 268, 269, 106, 270, 271, 272, 273, 7, 274, 8, 275, 9, 276, 10, 277, 11, 0, 35, 36, 37, 1, 12, 0, 13, 14, 15, 16, 17, 2, 18, 19, 3, 13, 4, 20, 278, 279, 280, 21, 281, 1, 282, 283, 284, 285, 286, 287, 288, 23, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 38, 24, 39, 27, 177, 40, 41, 28, 29, 30, 317, 318, 0, 319, 31, 32, 33, 34, 42, 320, 321, 322, 323, 36, 38, 324, 43, 44, 325, 45, 46, 39, 40, 41, 5, 47, 48, 7, 49, 50, 326, 51, 52, 53, 54, 327, 328, 55, 329, 0, 56, 330, 57, 58, 59, 60, 61, 331, 62, 63, 332, 64, 65, 66, 67, 333, 68, 69, 70, 334, 71, 335, 72, 73, 74, 75, 8, 336, 337, 338, 339, 76, 9, 340, 341, 342, 343, 344, 345, 346, 347, 77, 78, 10, 79, 80, 81, 348, 82, 11, 83, 84, 85, 349, 350, 87, 88, 89, 0, 351, 90, 91, 12, 92, 93, 94, 47, 13, 13, 352, 353, 95, 96, 354, 14, 98, 99, 100, 15, 101, 102, 355, 356, 357, 358, 103, 104, 105, 21, 107, 16, 108, 359, 16, 109, 110, 360, 17, 361, 362, 3, 363, 4, 48, 111, 5, 364, 112, 365, 6, 366, 367, 113, 114, 368, 369, 370, 115, 18, 371, 372, 373, 374, 116, 117, 49, 0, 118, 119, 120, 121, 122, 375, 123, 19, 50, 51, 376, 377, 378, 124, 20, 379, 380, 125, 126, 127, 381, 128, 52, 129, 99, 130, 382, 383, 384, 385, 386, 1, 387, 388, 389, 390, 391, 392, 155, 131, 132, 133, 134, 22, 13, 135, 393, 394, 395, 396, 397, 398, 136, 53, 399, 400, 137, 401, 402, 403, 138, 404, 405, 406, 407, 139, 408, 2, 409, 410, 106, 140, 411, 412, 413, 414, 415, 416, 417, 141, 418, 419, 420, 142, 143, 421, 422, 423, 156, 144, 424, 425, 426, 427, 16, 198, 23, 428, 145, 429, 199, 430, 201, 431, 202, 204, 432, 206, 30, 146, 147, 148, 149, 24, 150, 433, 434, 435, 151, 436, 207, 437, 438, 0, 152, 54, 55, 124, 439, 126, 440, 153, 154, 441, 442, 16, 209, 443, 155, 444, 7, 22, 56, 25, 27, 445, 28, 446, 447, 448, 35, 153, 449, 37, 212, 57, 0, 3, 450, 451, 1, 2, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 25, 27, 28, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479, 166, 480, 481, 482, 483, 484, 485, 486, 487, 488, 43, 489, 44, 490, 491, 492, 45, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 46, 515, 516, 517, 518, 519, 520, 521, 55, 57, 66, 68, 522, 523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 69, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 7, 559, 217, 3, 560, 222, 561, 562, 563, 564, 565, 566, 567, 70, 568, 208, 569, 570, 571, 71, 216, 572, 573, 574, 227, 575, 84, 576, 85, 86, 577, 578, 156, 157, 579, 160, 580, 161, 581, 163, 582, 583, 87, 90, 97, 106, 584, 585, 4, 58, 586, 109, 110, 587, 158, 588, 589, 164, 590, 591, 592, 593, 594, 595, 596, 597, 598, 599, 5, 600, 601, 6, 602, 8, 9, 10, 11, 12, 13, 603, 604, 605, 606, 607, 113, 608, 116, 609, 117, 228, 125, 610, 165, 611, 166, 612, 613, 126, 614, 615, 616, 617, 14, 30, 618, 619, 620, 167, 621, 622, 168, 623, 624, 625, 626, 627, 235, 628, 127, 629, 630, 631, 632, 633, 634, 635, 636, 637, 128, 638, 639, 640, 641, 129, 642, 643, 135, 644, 645, 646, 647, 8, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 170, 171, 658, 172, 659, 127, 660, 173, 16, 661, 662, 663, 664, 665, 666, 667, 59, 668, 136, 137, 669, 670, 671, 138, 672, 139, 140, 141, 142, 174, 673, 143, 4, 144, 145, 674, 675, 9, 676, 677, 678, 679, 680, 681, 682, 683, 684, 685, 686, 150, 17, 151, 152, 687, 153, 154, 175, 1, 159, 60, 162, 164, 167, 169, 170, 61, 174, 176, 177, 178, 180, 182, 183, 184, 688, 689, 690, 186, 691, 0, 692, 47, 31, 693, 694, 695, 155, 168, 171, 191, 195, 62, 202, 203, 63, 237, 696, 18, 697, 172, 188, 190, 193, 194, 196, 698, 699, 197, 700, 701, 204, 207, 206, 209, 210, 64, 702, 703, 704, 705, 211, 706, 707, 708, 709, 212, 10, 213, 19, 20, 710, 711, 176, 712, 177, 713, 714, 715, 716, 717, 33, 214, 65, 718, 719, 720, 721, 215, 217, 5, 722, 723, 724, 725, 726, 727, 238, 728, 218, 219, 66, 729, 240, 730, 731, 732, 733, 220, 7, 221, 222, 223, 224, 734, 735, 736, 225, 226, 227, 737, 228, 68, 178, 229, 230, 231, 232, 738, 233, 234, 235, 739, 236, 238, 237, 740, 8, 239, 240, 241, 179, 180, 69, 181, 182, 741, 70, 128, 71, 74, 75, 76, 742, 743, 245, 744, 183, 745, 242, 243, 244, 746, 747, 185, 188, 748, 749, 750, 189, 751, 752, 21, 753, 23, 193, 754, 194, 755, 756, 757, 758, 77, 245, 246, 759, 78, 30, 24, 79, 80, 30, 31, 81, 32, 82, 34, 760, 247, 248, 249, 761, 762, 195, 763, 250, 764, 196, 765, 74, 93, 34, 251, 252, 35, 248, 96, 198, 766, 36, 767, 199, 768, 37, 253, 255, 2, 38, 256, 83, 257, 258, 39, 259, 769, 260, 770, 771, 772, 1, 773, 250, 774, 775, 261, 36, 200, 776, 777, 201, 254, 255, 778, 779, 780, 781, 262, 263, 264, 202, 782, 783, 203, 257, 261, 784, 204, 785, 786, 206, 787, 788, 207, 84, 265, 266, 267, 38, 268, 269, 0, 208, 270, 271, 789, 790, 791, 272, 273, 274, 276, 277, 278, 286, 40, 0, 287, 290, 1, 292, 293, 2, 300, 306, 39, 307, 310, 312, 313, 317, 41, 320, 85, 2, 42, 322, 323, 324, 325, 326, 328, 40, 330, 331, 332, 333, 334, 335, 337, 338, 339, 340, 341, 342, 343, 344, 346, 348, 349, 350, 1, 211, 351, 353, 354, 356, 357, 358, 359, 360, 361, 362, 364, 367, 368, 369, 212, 792, 793, 43, 41, 794, 795, 796, 797, 798, 799, 800, 801, 802, 803, 329, 371, 804, 805, 806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 818, 370, 372, 819, 373, 375, 47, 48, 49, 55, 66, 68, 69, 72, 73, 74, 75, 76, 77, 83, 352, 376, 377, 378, 380, 381, 382, 383, 384, 385, 386, 2, 820, 387, 388, 389, 390, 392, 393, 821, 394, 822, 823, 391, 395, 824, 825, 396, 398, 826, 399, 397, 827, 213, 0, 828, 400, 829, 401, 830, 86, 831, 402, 832, 833, 834, 214, 215, 403, 404, 218, 405, 260, 406, 408, 835, 836, 407, 409, 410, 411, 412, 413, 414, 415, 837, 219, 416, 417, 418, 419, 420, 105, 421, 423, 44, 424, 838, 839, 88, 426, 428, 430, 3, 220, 422, 425, 431, 432, 4, 433, 840, 436, 435, 221, 222, 437, 438, 439, 434, 440, 841, 441, 842, 442, 443, 444, 445, 446, 447, 448, 843, 223, 449, 450, 451, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 844, 224, 845, 846, 45, 463, 89, 464, 465, 91, 466, 467, 468, 469, 847, 848, 849, 470, 850, 471, 472, 473, 474, 851, 852, 475, 853, 225, 854, 476, 477, 855, 478, 856, 857, 226, 479, 480, 481, 482, 483, 3, 92, 93, 484, 858, 485, 859, 860, 861, 1, 4, 486, 487, 94, 86, 488, 489, 490, 87, 491, 862, 492, 493, 494, 495, 496, 497, 498, 88, 499, 501, 262, 500, 502, 263, 503, 504, 863, 505, 506, 5, 864, 865, 106, 46, 866, 867, 507, 508, 510, 868, 229, 869, 870, 230, 513, 871, 231, 3, 872, 873, 511, 515, 874, 875, 516, 518, 876, 877, 520, 878, 879, 232, 517, 880, 519, 11, 881, 882, 883, 884, 521, 885, 95, 541, 523, 525, 886, 524, 526, 96, 98, 100, 887, 233, 888, 527, 532, 264, 889, 533, 89, 890, 891, 892, 893, 234, 235, 236, 90, 239, 894, 895, 896, 546, 897, 4, 898, 899, 900, 901, 902, 91, 903, 101, 904, 905, 906, 548, 907, 5, 908, 909, 544, 910, 911, 92, 7, 912, 913, 914, 103, 915, 916, 917, 918, 241, 919, 93, 94, 920, 242, 921, 243, 922, 557, 528, 529, 923, 924, 925, 926, 559, 927, 928, 104, 929, 0, 930, 931, 932, 105, 95, 96, 100, 107, 108, 114, 933, 115, 118, 119, 120, 934, 935, 101, 936, 47, 937, 938, 271, 939, 560, 530, 531, 534, 535, 536, 537, 274, 940, 121, 941, 942, 107, 48, 943, 49, 944, 5, 538, 561, 50, 543, 122, 551, 103, 539, 540, 108, 542, 945, 275, 244, 277, 553, 562, 563, 247, 254, 564, 946, 281, 947, 246, 948, 318, 249, 319, 949, 565, 950, 566, 567, 951, 568, 952, 569, 570, 571, 572, 573, 574, 575, 953, 253, 954, 256, 258, 955, 956, 957, 51, 545, 958, 959, 960, 961, 962, 0, 963, 964, 965, 966, 967, 576, 968, 969, 970, 971, 972, 123, 973, 974, 549, 975, 976, 977, 978, 979, 980, 981, 550, 554, 555, 558, 982, 577, 578, 579, 983, 580, 984, 582, 985, 581, 585, 986, 987, 988, 586, 583, 6, 7, 587, 588, 989, 590, 591, 259, 990, 991, 992, 261, 592, 993, 265, 994, 266, 995, 996, 593, 589, 997, 998, 999, 594, 107, 595, 596, 597, 598, 599, 2, 1000, 1001, 1002, 109, 52, 600, 601, 603, 604, 53, 605, 1003, 606, 609, 1004, 611, 1005, 1006, 54, 607, 1007, 267, 608, 1008, 1009, 610, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 268, 612, 1019, 1020, 1021, 1022, 124, 614, 615, 1023, 1024, 616, 617, 618, 619, 1025, 1026, 1027, 620, 621, 1028, 0, 1029, 1030, 1031, 8, 125, 130, 613, 622, 1032, 1033, 623, 131, 1034, 624, 1035, 625, 132, 1036, 1, 1037, 1038, 626, 627, 628, 1039, 629, 269, 1040, 1041, 630, 631, 632, 1042, 133, 134, 1043, 270, 320, 1044, 633, 1045, 637, 1046, 634, 1047, 1048, 642, 635, 636, 1049, 1050, 643, 638, 108, 9, 639, 640, 1051, 12, 1052, 641, 10, 1053, 1054, 1055, 1056, 271, 1057, 645, 146, 1058, 276, 1059, 277, 644, 1060, 646, 1061, 278, 647, 279, 280, 1062, 287, 147, 148, 648, 55, 649, 1063, 1064, 1065, 1066, 1067, 1068, 1069, 651, 1070, 650, 1071, 652, 293, 653, 290, 654, 1072, 655, 112, 1073, 1074, 11, 656, 657, 658, 659, 660, 1075, 1076, 661, 1077, 662, 663, 321, 664, 113, 1078, 1079, 12, 1080, 665, 666, 292, 1081, 300, 1082, 667, 1083, 1084, 149, 1085, 153, 1086, 154, 279, 669, 672, 1, 1087, 306, 1088, 1089, 114, 1090, 115, 1091, 324, 1092, 325, 1093, 56, 3, 4, 674, 675, 1094, 110, 57, 326, 1095, 327, 676, 1096, 9, 1097, 156, 677, 678, 1098, 1099, 679, 157, 324, 693, 686, 689, 690, 692, 694, 111, 326, 1100, 329, 116, 1101, 117, 1102, 118, 331, 680, 1103, 337, 328, 1104, 158, 1105, 1106, 681, 1107, 1108, 695, 684, 159, 58, 687, 160, 688, 691, 59, 696, 161, 697, 698, 119, 699, 700, 701, 1109, 703, 704, 706, 330, 1110, 707, 1111, 13, 14, 708, 15, 1112, 709, 1113, 710, 1114, 1115, 1116, 711, 16, 712, 17, 1117, 713, 714, 1118, 162, 716, 1119, 1120, 715, 717, 1121, 718, 334, 719, 722, 267, 720, 721, 1122, 1123, 1124, 723, 724, 725, 726, 2, 112, 60, 122, 727, 728, 729, 1125, 1126, 1127, 1128, 1129, 1130, 730, 731, 1131, 732, 733, 1132, 332, 61, 62, 734, 735, 63, 1133, 280, 123, 124, 0, 125, 126, 333, 736, 1134, 1135, 1136, 164, 738, 740, 742, 1137, 743, 167, 744, 1138, 1139, 745, 1140, 746, 747, 748, 749, 750, 751, 752, 753, 754, 755, 1141, 1142, 335, 336, 756, 757, 758, 340, 759, 8, 169, 760, 9, 10, 761, 1143, 762, 763, 1144, 764, 1145, 765, 1146, 127, 766, 767, 1147, 170, 129, 1148, 1149, 1150, 338, 1151, 1152, 1153, 339, 340, 768, 346, 1154, 770, 773, 1155, 128, 1156, 1157, 774, 1158, 18, 129, 348, 1159, 1160, 775, 776, 777, 11, 1161, 1162, 1163, 19, 349, 131, 1164, 778, 780, 1165, 351, 173, 174, 175, 2, 353, 354, 1166, 355, 1167, 1168, 356, 1169, 1170, 134, 1171, 135, 1172, 1173, 1174, 1175, 1176, 282, 176, 771, 1177, 283, 114, 357, 64, 284, 1178, 769, 779, 772, 781, 782, 783, 1179, 785, 1180, 1181, 786, 787, 1182, 115, 65, 784, 788, 132, 1183, 133, 1184, 1185, 1186, 1187, 136, 1188, 789, 792, 358, 1189, 1190, 1191, 1192, 1193, 5, 13, 1194, 1195, 1196, 1197, 799, 1198, 1199, 1200, 804, 1201, 1202, 1203, 1204, 1205, 1206, 817, 823, 1207, 825, 359, 10, 824, 11, 12, 1208, 1209, 826, 827, 829, 20, 21, 177, 832, 1210, 178, 1211, 66, 828, 830, 833, 834, 835, 1212, 836, 839, 1213, 837, 840, 841, 1214, 1215, 1216, 285, 12, 180, 182, 1217, 842, 843, 844, 13, 845, 847, 848, 1218, 341, 1219, 360, 361, 13, 1220, 14, 1221, 1222, 849, 1223, 846, 850, 851, 852, 183, 1224, 362, 855, 1225, 1226, 137, 1227, 853, 15, 1228, 22, 857, 138, 1229, 1230, 1231, 1232, 1233, 364, 859, 16, 1234, 139, 367, 1235, 1236, 1237, 1238, 1239, 368, 863, 1240, 369, 1241, 370, 371, 1242, 1243, 372, 1244, 1245, 1246, 140, 141, 7, 8, 856, 860, 861, 862, 373, 865, 288, 1247, 1248, 375, 342, 1249, 1250, 343, 866, 14, 867, 184, 870, 1251, 67, 1252, 1253, 186, 187, 191, 1254, 1255, 192, 68, 869, 871, 1256, 0, 195, 872, 873, 1257, 1258, 874, 877, 1259, 1260, 1261, 1262, 880, 881, 882, 1263, 1264, 1265, 1266, 15, 884, 1267, 1268, 885, 876, 878, 1269, 1270, 1271, 886, 887, 893, 1272, 289, 200, 201, 888, 1273, 1274, 890, 892, 895, 897, 1275, 898, 1276, 891, 344, 1277, 1278, 901, 1279, 908, 1280, 1281, 1282, 376, 132, 1283, 1284, 23, 378, 1285, 1286, 1287, 1288, 379, 380, 899, 377, 1289, 1290, 912, 1291, 1292, 1293, 1294, 381, 383, 900, 384, 1295, 1296, 202, 134, 1297, 1298, 1299, 1300, 1301, 291, 294, 295, 1302, 69, 385, 386, 296, 902, 903, 904, 905, 907, 910, 909, 1303, 203, 204, 387, 388, 389, 205, 1304, 1305, 1306, 142, 911, 1307, 1308, 1309, 1310, 1311, 914, 1312, 1313, 915, 16, 916, 917, 919, 923, 1314, 913, 927, 1315, 920, 390, 1316, 1317, 1318, 921, 922, 924, 392, 925, 928, 929, 930, 393, 1319, 1320, 401, 404, 931, 403, 1321, 1322, 143, 1323, 932, 409, 933, 405, 1324, 1325, 144, 1326, 408, 935, 936, 937, 410, 1327, 297, 298, 1328, 1329, 938, 345, 939, 411, 30, 1330, 145, 148, 1331, 1332, 427, 940, 1333, 1, 1, 934, 941, 942, 1334, 945, 943, 946, 1335, 1336, 1337, 944, 947, 1338, 948, 949, 950, 346, 1339, 951, 1340, 429, 1341, 1342, 149, 1343, 1344, 24, 1345, 150, 1346, 1347, 25, 135, 299, 301, 302, 437, 439, 952, 347, 1348, 151, 152, 155, 1349, 1350, 1351, 1352, 206, 156, 1353, 953, 1354, 954, 1355, 1356, 1357, 955, 958, 959, 960, 961, 962, 956, 207, 1358, 1359, 27, 441, 1360, 1361, 28, 445, 1362, 303, 1363, 304, 957, 1364, 1365, 1366, 209, 210, 963, 15, 214, 227, 1367, 251, 1368, 965, 1369, 966, 968, 964, 446, 1370, 1371, 447, 448, 1372, 1373, 449, 450, 252, 253, 254, 451, 452, 255, 967, 969, 971, 256, 257, 1374, 453, 1375, 1376, 454, 1377, 305, 455, 456, 457, 1378, 1379, 973, 974, 976, 1380, 1381, 1382, 1383, 1384 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 1, 20, 2, 21, 5, 22, 23, 24, 5, 5, 2, 25, 0, 26, 27, 28, 29, 7, 18, 6, 30, 31, 0, 32, 16, 0, 33, 23, 34, 0, 3, 12, 19, 35, 28, 36, 37, 38, 39, 40, 41, 0, 42, 43, 36, 44, 45, 39, 40, 1, 46, 47, 15, 48, 41, 49, 50, 45, 46, 35, 51, 51, 52, 53, 5, 54, 55, 0, 56, 57, 58, 3, 59, 3, 60, 61, 62, 6, 42, 62, 63, 64, 63, 65, 66, 67, 68, 69, 70, 64, 71, 65, 66, 45, 72, 67, 73, 74, 0, 75, 0, 76, 73, 77, 78, 79, 80, 80, 5, 81, 0, 82, 83, 2, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 16, 81, 86, 96, 97, 98, 6, 90, 99, 100, 96, 101, 97, 5, 19, 2, 82, 0, 102, 40, 103, 100, 1, 104, 7, 6, 103, 105, 106, 107, 108, 0, 5, 109, 110, 111, 106, 112, 113, 114, 12, 115, 6, 116, 9, 117, 118, 119, 120, 121, 122, 123, 124, 0, 125, 1, 126, 31, 127, 128, 129, 130, 0, 129, 131, 0, 132, 133, 113, 134, 135, 136, 116, 2, 137, 119, 138, 139, 140, 141, 2, 142, 3, 143, 120, 0, 40, 123, 144, 145, 124, 4, 3, 146, 28, 0, 147 };

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
            final int compressedBytes = 2962;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXTuPXLcV5lB3BGbjJPRCiqe8KyvIBFARV06j5GrjvNxoIV" +
                "hIjCBNmrhQmVIGuBsjWHUqVKgcqDDgzj9hYagIVG0TJFCVn5L7" +
                "mtn7IPkd8vDO7OgaXmn1DcnDw8NzDj+Sd9786O7jHz9bqlvz03" +
                "vf3nj1+OLjp4fFV3/49/vm0b/uF4fizcMS/2b5+xJ/MsD/W+Nl" +
                "+Q++Wf7pRYX/+tXj1x8/fdQrj3Bu/aj8nssvxEwMHt39hVv/xQ" +
                "/ufjI/LcdfiHvfPji7fXFz+UgY9ZuykUJcB/0QcEf9XTzevgH+" +
                "9uHf/vLBs+WfX/zkH09e/e7rP77+1dO/Hv/z8/+U+NsvP5leP9" +
                "X4ZafLxYtq/GZnt1/fXB7u1fhNPf/3wH6nlE8okYnqZ/uUv2jZ" +
                "/r2o/MfE/ZNdZ1V0fraebd/1e+3lZ+Kjx/R/Zetn3/XLlL8/P7" +
                "N1fC+68R3GtyZ/c+KT2neC8UP9m9I/lyMw3yRUsvxPf6bLn6Ya" +
                "gjklv6LkJ89q+e7ton91l9ZJ5M3y/8XJz5tZXMh5xu9fm380+e" +
                "Oszh97+cfbhz9t86PTJ6tNflSXp+VHnfXJqum/3Kr9ps/fB88s" +
                "8+X3CEf5O8K/a/Amf3xQ548bfJYA564vuPWHlF815Y9D5EMP17" +
                "9x8/t1edf83Fb7rvJdfNXgsouj/JWL9+X/upb/uJX/y1H/xji3" +
                "/mF50Skv4vonOO2P6rclfQWdf/iuGx8G83NWt18I/BgngtYXDq" +
                "fa1a//AfnZrnHb+Pb8U/r2Rbx83vgO89uY+I/yu9ZMpMg3M6Bn" +
                "Kqg8F+8qtTXnqdsXIeXZ+WunvG38Dg7E7UNVms/8tGw/L8RHf9" +
                "fFVw+ez8yjl/fXIvXz17ybv5b4e778FuHIfrj1H3zq7N/5/TZ/" +
                "8+XP3Py7i68avB9f6a42Egf+XYqwZ1h/Ez8y3wczRwiQXv9vQH" +
                "zg4NqtSEPQwayr17ZbWsl+s86K9EB/RmSV+2sEVUYsqj+XtUNU" +
                "rX7zq670NJ1V/yh7HTXuUGvrn1RSal31SDbz52U1v3X5qYt6/u" +
                "Dxp5uOjIv/3PH36gf2n5QfpZifymEfjfz5SP6ciIP6o8fXBNqP" +
                "pjmbwPaJ5TO3fdnm19o/ZJT+i17/Fzb7sc3fdf3AvpB8wMH3Ep" +
                "r6l5PWOW3yH57+VcxE0PHjb6LsN4uVH40v9h/M+KNsOCN+OetX" +
                "LtzvXwSYX8nkc32QMn+Gyu8s8ZWrfSNs69hRGoDaH+Kp/aui9t" +
                "8ZX3jzKzT/NA5foIU9fwL6IfC/GYf/5fKnHy7eSn1LzO/om3L2" +
                "SyGOXl4eiVyvlPjfnfldE5dfFHSPOvX+G+I3u7ht/bMt+Vz8Z6" +
                "1/kk078hdmfnjd+c+I8iKsfEHxL9GrBDw+ui/2wP8Eyx84/pCf" +
                "BfGFfX5gK/wxXHUMrEaTcT7/Gjh+o6Dmj19wfED7BqyfcPwL5j" +
                "dFIH8qfPythd8ddiHr4NJSfqd4An7bq/+I8Qvj90SUfdPzW7S+" +
                "mZg/ofu/7vxR5PjA4Jdr/hzx6xZcDPh3xH97+XlG5LwyaT//Py" +
                "keLt9n227/Pc7+CJr/aP/Cgosuzt6fYe5/dOeftubPAvDf0H5l" +
                "NYOPyhIjfuKGgPubBP8mw/1rx8Wrk9aHS9ldSyhieRwfUPtCsO" +
                "SHjrhg+e/g8iT+B8uvyP2n4pJZPpH+k9c/psGKlOOzFNKI2epn" +
                "Z1+URT43h2b2YqVX6rfLPDM3SPyrn38n84OyM4JaUvevNvxO2b" +
                "iN3/lw8aaPLxpcDHFlx6X6VJT4rJbuh+UgLC7X/bvTxifTSDb0" +
                "nzT/Cuuv9Odh0FD/kHyo/0h+zL8R9T8YP0Efv1p/d8o++nA5wi" +
                "9I44/6j+ST6tg7vnT9yCj7XcsfZ5/YPkj252l/rJ+T8fwiyX/g" +
                "l9+hPzT/kH8hx1cduf8m4PrIe749+f7B6Lngxd9lU7DMH01mSr" +
                "0bqZXOlZiVw/B9y/6JGdZvwvZfRuUvmPGb2X9x2S8/zE/R+gfu" +
                "f4P8/hzg3PwGduQE2N8Jc3+6CLNvkv2/S/kl077R/jWcH9T5E4" +
                "ej+YH5K+B/1bo1eXW+Qwz5Wct4FBnJv3Llq/pZrX/zuvNdefJA" +
                "XMbhm/MLdn4At1+0n3bWP7Q/x/x32h+pffdz2S+/8d/U/nP544" +
                "ntRwXGBwP8k9mu/6SdD8P8k/P8zbLmv1ZHolw/i+/lJf7clPlL" +
                "sfSNb4B/hvxbYd3U2S0+ig/08lu2j6njO5s/nET+hP4Dnm+NaT" +
                "9wfHVC/zU1Pzt1/hjM33af3ErUqnp+HvT9X2ZmdRVsfpvLn6fS" +
                "v2P9tbXxt7dPWN/51z/n4iy32v9qPbjv11BWUQh5UZlAIR7omd" +
                "HnC5r8df7TP3/R0x/AhaN+sx39A/5B9Y6f5pZLobbxC1hfcu2b" +
                "ub/cPp74jfgTUB66nc3+nPDYR0b2D3b9Z+71K69/+Hxpkvw/i/" +
                "aPGGfyP/6BRvcnHfLJ8PyjJ59Mlf8ExFc5TX4z8fpq8v3pwPzE" +
                "hOWf2D6m7d/U/NLO1/+C5F9DHgI/a/PPjvLYv51x9i+x/QzOn5" +
                "L96ypN+/j8junxZ238UXX7nvIyF5vCRqiq0POr83tFhP+STDyN" +
                "/xmtdlKtf0PHZ51/o/K7nd/ofgk+X8rVb8j5A8v9AMxvovfPEO" +
                "6HB4yf4ejH1j/tzz8o/qO78h/6B1iLX358f8FyP7bdP8kI5dH7" +
                "HSj+ldP+1PYdl7/w7CvYv4XbT5Gef3SUx++fBPxDaxm5YymI7z" +
                "f462ffrxWO/MmQ5OPeXyGUZ71fCI0v4f7HRw2/NVLmeWO1hezl" +
                "b3b+yofvYH+Crn+4Pz1x/Obe36DfX1Hh8ic4H5X+/kTg/RHu+R" +
                "ba/qIIZT06/fOOPx7fY558VkG1xQ7s+JF+KUu82t+Xs1+UHsOY" +
                "mcgrgjjPy19o9ftwMv/rLx//fhrB1x9pfa7A/HKc3yGfb5QB9Q" +
                "fgYP+aEP+85SGO2ufzB9PyI9Htr0j5b1x+l6785PVH8xtEPKB/" +
                "ygJMrb/9wVVcfCX7z3ddf5H4luLrtZWfzw/venx5+ck1x7n+cX" +
                "T/ZHy/hsU/u+7vKHf9ui8fuP8D8mv//R1F29/g5S+h80MHza/B" +
                "7J451h9evPO4cFd5+v2sof77/cN43P2s/cEVEz94V3ESuSVdH0" +
                "k0/5H9eezXKz/h/i/RP6D+xfUf3W9MoB8vni4+HVjj05E+b/mX" +
                "TNT8i6j4l6rzFf9yHsOvue5HRu2fwfrB+mAL+//E9QkRj85fEf" +
                "8TyR8R80v8fu6J+C1yfsvVj//B96eS8X9h4w/7z9tfiTsfodPJ" +
                "B8tfctd/Xv/J5qc3f3edz/Xj9fm4W4z7ydb1A532IvKzhBRGRt" +
                "pX4P1lCz+qfPnBsH7L+tMh33r9KXfKb05fP299uBkwGcX/0d+P" +
                "JwP0Y3v/eOT+xq7wgqYfen7kiO9cHOc3lPezs/lJ9/1r2vorID" +
                "4Nyvvvd2P7vvTfr544vl2L7w8OiV+B+zP8/aXCXz+ef7z7Lcn2" +
                "VybCufk/d32E8anXf7z1S7L9scn0l8g+HP4n2f6v9/5a/PkE7v" +
                "3/joeLm9+ofd75gDT+M7+qavD9X+zyE98P4taPvx/QfX56LhK9" +
                "/8ATP/H3S+z5+gWsb+3383V1P9+xw2RdP7jxTf0D/9+5/+9on4" +
                "bT2h/730yk6R8bx/kNq//kBRbr/OF059sULz4kmJ8ofnjx+v2U" +
                "unRlWkrb9z8R3s/Aqj+OH0Pjn9R+/PoVVP5CBMyvkPdLB7ZvKE" +
                "44RP4t4/suf2o8NH80oec7IP8fMT9E76syqiVYXlyVr6Z7Xt/P" +
                "rdsG72/ZNb/A3X+g4nsyfwb/jO8/xeg3ZH2N9ne448OtH5QnrN" +
                "/98+fEkX+R+SkTF/8cunTqRwTaPxXXfreA3o+D9j/A/hN8fw6T" +
                "32Xnt+T3J3nnR8byjyT+WF5Pfnj6+9Gs881x+8MdQ/8/B2bTUw" +
                "==");
            
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
            final int compressedBytes = 2865;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTuvHTUQ9po9V87lZaIEbrkJKU6RAipogpbwENAQRUSCjp" +
                "KCkhIk36sUoUtBkfKIAildfsIBUaBUaZAQFT+FfZzX7tr+xh77" +
                "nAPKNsm9c8ePsT0vz4yFsH1aOD8z/PHp5zfuvf54/smV2fm3T1" +
                "74+d7yne8v1/c//fM1c/evW/VlIZTwf6ppsZSiWv2hMuKs/Xfe" +
                "/KZDRvDNJ8H4EXzwB8nm9/TVG/feeDz/8qcW/4Of7/3+zvd3V/" +
                "h/tPi54Whx6fhs+tr3T9Lx/wLoM4UT9me53Wjluv861f7aNsul" +
                "byQcn7+48yvt57Owja+0wY21u8I5/tPux0td/7oZ/4LYv17/I3" +
                "f35xazXo3P+bV4ZSVORm0uqOuzwpcufH/7mD/y2l99TvwR/dWU" +
                "/qB9PYSbHq4C5y8m8w84f6zzhcaP9k8tgj4Tig/gqrbwVy3p/a" +
                "/PFPH3JnR+TDiXv4mi9CpHEL9m6Qek/WmVHyYJ/1++fOPD2flc" +
                "XRHi5pP3L64uT+Z3hVEf6XZi3fjqrPNv+y/P52dd/0XX/+VB/5" +
                "D/8fonrU/pYS8D/WHLMOjsKWT8vX5zezW+Hyjrg8YH6Av7h+dD" +
                "DwlgXMcqjj//1u8f6/4t+vWTA3xD4MGFbV0kECzO9on9A/EV33" +
                "9W/Ljzk06/Xe6uv4V/QPsg4vyF8G9MH7b8eK07TOWEmTwA4tvQ" +
                "BG0NTDQEJwly2bGbfrPokegrxj2tpJ8Z/FY7+V/kZ2j+AbR+ED" +
                "9GEdJMOIUske1T+GuC8ZPxlbiwy7eFR3/QbkKZiX4Y6d9a+FdB" +
                "0haq2UGzHQkrhf6itXRNO68ZAT9uf2qyfMivn9VAasqX9JqHtC" +
                "bc2Z23+s1Ry1mJ4Rb7yXGQ7PM//UxcvayaP5qdN39S1eLt73R9" +
                "//2Hhbn74FaPL8PppzPDQ/yjPPsg+/pw5SvT/uDbpzWBl07ZIl" +
                "2/1mKq/+lg/bya7N8qmX+z7pSYaoryQoh/xdW+QfazV35I1bBc" +
                "3e5Pud6fzUB10+iy378Anv98Z/Yvp9H/oK/J2z7JP0Jrv4imT+" +
                "72paP9Q8iHlOsfiG8CVJK92jfyiOAp/d/mCOf3HL43eNXugGJx" +
                "6eLr5j9fmUaU3V/ohfp4LkqTRP7G+r+fr8/h5DvdkfDm2d9SXx" +
                "Gz643yU7wnxLVHz66JqtlA4p/rsxtmZ30Ro5eO9p8O2z/r2xer" +
                "9g8NR/o/wpfqM9HAi9a0F6808v/s2Vp/vN7rl7dH8DsDOBxftH" +
                "5Fs2/o9JMueDc/F1yqp176cNcP0Z8wfmb/t1n9JzmfDP7Bbf+a" +
                "fiSb/dfev8vi3UYcGdOYgrpSYlE1P0TqP8/hxwNPoF+XHP2Vq9" +
                "8eGh/rb73dWJnStHxDCtUen9aQfHHXf6YG/p//zfzVkjc/RJ95" +
                "j1iIeU9fI/WAvlz7UglL/wH+Syjfqf27vmc8/6FCrCPQ/znVTy" +
                "9o9y8ycv7Af0q5//HEl7D9h8o/EYL/G3x3wuA2lZNl36H+w/x3" +
                "bmCoflJ1+gmfvsdo/+0Rzr1/scZnphz/EtAPwdcu3VXQUTV2YB" +
                "PHpzP5X5XriBjAH2n2e8vHhDV+uUoEHylCu99e/FdLR3xzFdi+" +
                "a/xg/lh+O+KbKwZ/CJHfx82/uPG7/Phfc2A4c3yNftxQeHFNfN" +
                "P8cKlq/vyhacRjPa+I5y/B+I/aPjx0+1w4137g+ZdX9lkpgj/D" +
                "4C+5+ZdIxr9y87888Vvb/us2vvfXSjULfLMqzNXliWqRPur+go" +
                "C/B/0U7l9ls9/LMYeSTv1KdfANft952dgZhnZ+evzTHXy9gw/6" +
                "B/A2Psyxvrvxg775ZY6POvD+CIxvKHL4Xz3yD/sXgH6D86f9/h" +
                "eSf8ZCr7ok0m8lHwb5nzrAPqTS3+PfmPSgU8Pj+yfml26WwmQZ" +
                "/6HhPvoN5z/FR/AhO17x7yrT+CPbj7DvE/G/an1unfcTlv2pt/" +
                "sT+N/VgCVUu8MxpINMkO+HtG+D/cNmMn4D+HMi/7zTP86U75q3" +
                "PqofUbWlj+5/vUhzPqn37/pExt2/2+K/A+4fFOJ/3PbrISFMlP" +
                "8omr4Q37p+QfuHAi/d/LUGBE/gnyop+iFXf5Wc9eH175nf5n5G" +
                "n4jt/YzY3M9A/Y7vv4i4v2DU3xGU/DQdUF8J1E+B9lsdMb7c9p" +
                "8A+r+gz5+8/o76NYB/kuSDr74O3H+at3+xfPHWDyLUFwDyGdQX" +
                "oNSP8dd38OMT4Z76Of78Ff76Ra7vQpDqC7HzX1F9iFTzO7X2H5" +
                "ffLwby9/UfO//HzZH/Y+P/bODK4h+h+Uet+Y27JTyY/nOa/yDC" +
                "fvDJtz3jZ7z/J/i/iPpxyP4dyW+Pfw/V9+HqjyR83xTB/AnygV" +
                "l/J5D/uOEB9W0S+ndh/nPm/pnjw/zXT/+4+MdxQ9X2v+VIMcD8" +
                "sxTb+iVyXL8kjv9y62McFTwRfeLsM0J9C1Q/B+2fi6j6HlC/s+" +
                "M783/1LoFi7jeQoM99P+7SP3jyuRySrN6BGooJjfgLrE/BrH+R" +
                "2388HF9l+VsbPGB9lWDNP3f9pdzrt5f4MUb89LGP79BwGN/N3J" +
                "+5/Vvx9/Mb+/2CVp/dxch59lOC+vMOUiuqfuD3D5Li02j6pYpb" +
                "P1b9fG78FszvhP7NTf6r3M1fdfk30/m3hvSRA/0qIH8V+JfI/l" +
                "13+94vnv4j/4oG+lUm+P7eJ0hZn5Oenwfz46D+/8w6/qD8Os9B" +
                "SZKfVI7tk4D6/tC+QXBe/TJu/BuG+/Pf2PRv5LPynm9mflxo+1" +
                "P/sMypv9DlN/n+NIw/JHvfhuhfMZS1PHb/kCDXN03g3wH5dTz9" +
                "9fD10/LadzA/DMsvLz7lfQD/+wR57Q9u/hrUr8n1dyPXF+XHbO" +
                "w7x/1S7P3+8H6rzGX/cesDI/7f5Z+cD/JPmoXQm/wTO3ybn5JE" +
                "fmH/anT+BsoPYed3UOAc/xV3/5DPP/Kf2vXzrn6TFjOhpexY4K" +
                "Opfun1qiL99IFfv6Drt0j/jq7vI+H9hN++QvkjnYpZ1Vt4O62q" +
                "iy+q1/2X4/7J97OU+53Sbx+C8RHlv9v/4W8fKFIIf2o/ar8FaP" +
                "2TMlb/4crH/PW/U/knhJM/s+5HmfeniH7c9we4/gtk/2e/3+DW" +
                "B+LuHxBfj+NH7wzlc/D4qPEv1AS6LPdX8fH9tPwB5/zY72eN4p" +
                "vKnfimOoX9kiU+PcQ+zzE+kXb/lPH6J7/+Sc17XzIL/QLhAfX5" +
                "U/s3wuCO+PM9x7/79l94fP+Qfu73dT34pQcfv5/L0s/WX/j7tF" +
                "T9TCP/dZL7Rc8B4b7fS8R3wnn5B4H7x63fOXUB9H4wk3/G6Tfk" +
                "9afHf8o4/SQBfTj2Ue73b7Psv+23fn/2p9X7kb+P35/F9hN+P9" +
                "6bfwneH6bYB8h/5lsi5vtZSD/F7TP5Lz6frPnh92W5+ET9R4eR" +
                "Mdn9E6Affp8z5n4GrZ+e4ge9/xPjX3H4R9n+GbZ/lMkf7f1T/a" +
                "PQ/2Nd9v9O/gKKH8fvE4bwnwrbUqNhn54635d8dIuCj96nRGYZ" +
                "jA/n+Xe58dlx9b/p+Ln9i/uOry7InNBkab8I5X9R8mWf9RdDzo" +
                "+nfR3Xf5x/an/vy9Lzn1zvy5DfN4pyYKH4f4L95o8PTja/sPd3" +
                "6O8D8fon0b9k4DP3jyu+Np1+B+tfDeSLpT4I7/2Kcf9i0j+YKD" +
                "rfhPcduPYr1z4Wfv+9N76Fr1/z6w+w6zN562cy4yMTybdgY5dm" +
                "v3HzhxLkH3H5k9W/R6/vz3t/IE983WijBL//yI2v2LZP9/8RGz" +
                "dCxMF1MNkGeLH1qTYr4PT/8+r3Mv2z2e3/af1DHca/SOqlr37h" +
                "RWB9xrDxcePP/gUdIWRc");
            
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
            final int rows = 632;
            final int cols = 8;
            final int compressedBytes = 1586;
            final int uncompressedBytes = 20225;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW71uHDcQnmX2DoyAIIwgJVeuHBVXuEnlNDbWSQwkaWIIEZ" +
                "CHSJnSASjBhcoULlQeXARw50c4BC4CVWoCGK7yKOHu3t/e7fIb" +
                "cpY6ISzO8I04HJLz+w2PaDkUrYehrZG3/xtB10I67Yd+89Pp+e" +
                "dvpt8fjS5+ffvR6/P5oxeH5csf/vnMnr1/XB7Szaen51+8mf7y" +
                "qqJ/+/r83aMXZwv63xWdqCT/2KJbYgwD/rCL3n2/TsKRWf+JIv" +
                "OzcZ+2kmvEmN9xlvXQO98oj6wbdNt8p9n8Ef354n7b/D2Cdt1P" +
                "jvffpus1f0u5omLxjbY0qf6dum+axeF8pnw9+s1efyGA3V623O" +
                "He3n/puVOi+Sen340upvqI6OHbp5fH8/H0jKx+ZqqJtX349UPt" +
                "mspqwayyT2B/Unr3/tcDztel7/AY98PwHznHZ6iA+wuRb6l9an" +
                "UnOzbpobfP78/6/L5ZnN/v9fn79bNrfvv8l/IfbMhveuXvsB/u" +
                "UF7/ZkLsK+D8A++HwP10+LeKkBc03vLPM5b/xfZBcv3znj+yD3" +
                "A/2H+i8/HS5flD1/2tx4m5Vo6eF2ZM2ddEhbWZE73QVLhPy4jP" +
                "SITA/GbnfmXzq/iSX0wnr6r4kl0evxtPD1vxRdenozeOyqjucN" +
                "Knf/750D+A+Cqc/1ez/874mrn9I3rDqFjzzLcUC9snRzvVYgm3" +
                "h9wEuU+U3wrXz5chaUMTFtxt61tDgcPy9s/L7wjUL578v+w4YB" +
                "PAX7i+3jkWE7Q/TZfd/ne2jH8y/nh9pFzZNjPDquSGqd8Q24MD" +
                "Oj7UbhOjC8eiKOmr30z58ukfmT27fswRS2r/gH7wY698Vxz5cP" +
                "zknl90fnwJ8rOeo9AsfKGJj/3+Gc4HKnNirpr8wH3U+QE1+QHV" +
                "+cEV837j5ZPe35eTD8oc0eiBy6+yJ24/17cnVJiZpn8fjE7tMP" +
                "mzd/2b9vqTZn1arM+QzzsG4C/aH9GtEJ+4BfgOI7/dGFlv/ktb" +
                "9aVlzjeR9t0Tf4LrbwY9Z/l/FSDAXeZfXv5s/9UbCKT5Cze/6b" +
                "WPnvquYJ7PvD1/xb9g8U8f/wjlRzL/qtHhZ0EGm8Xot6d+VlqR" +
                "MRVbR6+uYPLcCW5sdW+jvFUf+PNrE5t/o/0t8N9e/oH9D+vD53" +
                "b1C/YPouqbEDr032nnC/sHq/htxqozfvf0B3y3y+lPsPsHwD7k" +
                "+HWf/7Es/UuOH0N8G+qPEL+E+KwMv4ymz/x01cyX9lfY+5fafy" +
                "J8Hupn4vNn4N9efHq+iR9mNX7YxlfF/SMpfuzHZ3H/UYYvy/Fn" +
                "1H9F9PCUbVB8bCh8MghfNcPhq8L8hJH/hOa/YfxhfsnKHxnzU9" +
                "eHPfWdvP8wTHncn/94R2h9lgX3F2T2GVJ/66jzk8mH8C2Ej+H8" +
                "2E+Xrh+Pz82o3b+nOHwO4W9gfUb/voWPfbx6XxGKjyF8LlT+UH" +
                "wXxKdE/SOIj0P7BPiTEF9i6ScK/rnE/5be+hXhM2z8Jj7/USJ8" +
                "p1ZRd77l+nwrsYo6fy9J3B8F+B7KL8Tvd7D9iuZD/ISXf+Ve+8" +
                "+ZWlu2Lzwb1j/FvW/ac/2P688y6ftFRv2N9M//vutO8ZMufOt+" +
                "n2/q978D1OeJ8V/4/klGl9ZXXPzFxO6vFL0fTx5/pflf8vpzc/" +
                "2CWV+pgP4Jqm9LEr3vCO3vWo7+8d9fSONfPH9e/iWOH9B/C+sr" +
                "Dn9BfY31A9kvqG/E5+e3n7KOL4V2W3xYZNbFN10J/azm0B1f/P" +
                "47+P0i4f6dYH/832d59Ef0+4PE/Zd7nj+l72+i3y/83/cf038y" +
                "iel3mF+G5reB/ondv2CdTwGELXZ0Ufz+GdARf/y+WaZ/rPyKZb" +
                "8eVRD5b79/gPi4kD/j/aooP+Tnt3HvB9m/D5PVP3m0/fLwq/7f" +
                "1wvj897jhxC/u+/yQXwzNb4B8Qc2fqFi9Dt1/ID0xL+vifNfQf" +
                "Kz9E+1dMoMNl/cP456vxTSn5XGV2n/jxEfPP078fu2ffc3Uq8v" +
                "x295/jEWn9z7+xq//PD3aUA/EZ3/fmTI989muPnQvhE+dM/xlb" +
                "tYP6l+x+j/8PL1xldIl8UHcfxInv/L+Mf55+HoEH+A+WtafEt6" +
                "P9L9wf3/B3YE0rg=");
            
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
            final int compressedBytes = 4667;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVWw2wXVV1vqNl6LQBJjSaENpSVNS2gJYUKgpmv3fvhWfUsb" +
                "UCWgIhKeDE/JExJC95yeOcs+//ezFhkpCXggn5eQmU/oog7QwM" +
                "tIXYlh/lRxPCj0pIJQFUqmhmqO3aa5111tp7n8vY7Llrr59vf+" +
                "s7+57zznn3vtSP1493b6yDtac3Luo85TyM2ulY/Xj7cDq7frxS" +
                "SU/Ls8e4Xqk42z2douxMF2cr7bT044B+nRB2ZvY01dO7mut5Tf" +
                "144wFnk4GxD1aKf8wrKI4qlYGfJRO64qMrlWQm59hmQyGfs/Zs" +
                "7mRfcV7rfnsW+Ec10tcC1el2hpvNkBmyA2g/ZYa6B5yH0eXZiB" +
                "lqvJRWMboizx4r6l9wdmyyiBeYoeZG+zF7sTX2WsxUba1xmKrd" +
                "XvYdRgLrD2ge22v/zH7eXmnn2WukaueLD9FFdmjgjbSD/ify3C" +
                "dV/dP2MzhfhvYv0F6VPaMQH0c7aIZa99pLbB2jV+yldk67bf8Q" +
                "/KNev8/aP/fiufZq9DKT2bvQ/r3d1+s6zw14T9abrHmW/Tus7a" +
                "Us7FOmh71HR/WX7aT9R7eWGOzftO9nttq7BQf7lGftncDxNfce" +
                "Ks77NKertO9Nu+zrWUf2b5X/dd3N3lHwrONOdD51L7N/BP5Rze" +
                "Rrgerd9l70dpldUEGLmF00YJ+eMLs6S7jGWa6XxYNLiIHX2De7" +
                "LzOu+RXBtU7gLKF135DTRZ2L0l5/FVoh28ElZXXpxFe7/WPNxh" +
                "jNbt+wP0VvkVkEldySR1F2zCxqvS41PbNnl+q4sYP7Uwb26QhX" +
                "Wx9mJLDu46z8k6r2CdO5JF1froLq/jE429ge8tHRCTrfp4+U9d" +
                "Mx7pPzxs04XHfOwnUHmHEatp29asY734DrzkV7KSv1HHWPjgYX" +
                "43WXo9x117uH17U+Lbj2yZyF6248v+6E8z7N6SqdH6V36O6+Co" +
                "rguhP/64OLFV+xNt+ncfag/yHNxhjvCN1157xlZhlUckseRdnr" +
                "ME9KTc96jcSDXyrODszAPEnV9K6BP2GkY+U16nxa5jNLBD9HBm" +
                "hFrCLvEuiB6+5LIR/aSa2N9cX9SuOFZqFdizaBV9N5GLXt75iF" +
                "nQftYhcBlrITXE8fd7b5FscOYbfl79JqylQqY1PydRua6xhpFr" +
                "an59mbbAPQ1m5mft0rx4zAUR9KX/ZUjGs0ZW0HbRftaHOtYtjE" +
                "a1HbOopsC6MXoD6s+zFfFDdNEzy0mGvSAHXvM83euVzjLNfL4l" +
                "aHGIRv7CTGtT4nuPb5nCW07htyuqg7nB7rr0IrZOu0x/VsN3fi" +
                "86lxomZjNT47xbXzaufBXRut++c8igY/UDuvdyXXOMv1srh1Fz" +
                "HEfPCs+BHBdXdwltC6b8jpou4b2Tv6q9AK2TrtcZ32SbTBPr1P" +
                "s7Ean51iM8/Mgx3LLXkU2feYeb2rpKZnvUbi1lvFdY8Z9pzfuo" +
                "yRZl53N2fVz6d5PrNEcNTPZu8sV0H1UA9oPzPkczY/n1Tnxrll" +
                "/UrjCTMBHlrMTdAAde83E70RrnGW62VxZ4QYYj44n34uuO5DnC" +
                "W07htyumjwYHZafxVaIVunPa7n+1Rog32apdlYjc+ex0uMe5bM" +
                "LXkUDX7SLOntl5qe9RqJuzuLswMz7Dl/4BeMBNxjnFXn0xKfWa" +
                "JKpXcwe0+5CqqHekh7rDTUBvs0UNavLE7+NfkXu9ZekNxsk/Sv" +
                "bTM9I/lWOp58B+535ybf7R2xi5ND7jfD5CX4/fYcO5G9DL93vg" +
                "H+48lP4Sn7reR4Wkl/DeJz01+32zKH+pBdnZ6U/hY8WRwunlM2" +
                "DLyJv7F+L/l+8oP6a8nh5IeQhftdspXud7DunekJjXr6m+ifkr" +
                "47nZ48mDxkR9Kzey9kZxFP8mTyFKyDp5zkALyez44km93vwclR" +
                "27HnJ7+wXew2as9JfwN53gXRpmR/8iignqD7Hf4GfDnd7xq19E" +
                "47nHP/mNU2T8ozW9J3IMvJ6almm4G7OVncu200gPHDZlvvh1zj" +
                "LNdpwPO4ipgh5gNl1wuud7rgw75hDxeNzcxm91ehFbK1HyqrS6" +
                "fifLpSszEmPCb06qYOz0/OJvBqOg+jdv2/TL13NTw/QQRYyk5w" +
                "HZ6fwMLzUx47hHp+wgz2oHUbHBuP3m/nWf38VNdMMtzz09j52S" +
                "c8FeMaTVl4fnK2i3ZUd4Pnp3xt/vyEUX4+XQ31Yd1PNPsxfh60" +
                "qbEheVg+B2re6D6HGTytfrz3lsuk6+iTGfdKXk1el8907FL5FA" +
                "g/93kpHWYWfd3B+TQhn+y0n6TPn3gVnNkjyS9RycbwEyC83x3I" +
                "5kDnx9I1xSdOT8P65+rHsyOgbhV9/sRcuOI0/NRsqrClK6UKlR" +
                "X5+dRLjuTX2E90HTP/ky6U9WangZ++ZHHvdtKAXtMpJwiZBeVH" +
                "hI/5YJ/uFlzvvYIP+8ac7pVd01+FVsjWaY/r0qm47rZqNsaEx4" +
                "TeSrMSPLSYW0kDzpVZlBOEzILyI8LHfLBP3xdc78uCD/vGnO6V" +
                "Xd9fhVbI1p5XVpdOxT7dp9kYEx4TeoNmELzckkdRfdRZqemZPf" +
                "g5rmJ1lx/kiKvVOxlpBnujnPVXhD0Ek32xXEWokG19NOQTfbpz" +
                "4+GyfqXxFrMFPLSY20ID3pOPUk4QMgvKjwgf88H59L+C6/2H4M" +
                "O+Mad7Zav6q9AK2doLy+rSqdinRzUbY8JjQq9t2uChxVybBvT6" +
                "S8oJQmYecD6piBlivkqlfbngev8p+LBv2IMQ2XB/FVoh20arrC" +
                "6din3y2BgTHhN608w08NBibhoN0PYVyglCZvZgn1SsrqKAD36X" +
                "fJ6RZlrvvznrrwh7CCbbUK4iVMhWd5NKqA2eKKeEx+RrkVh/c6" +
                "PvyHA+LeO74oOV+Buh/Pud4LlAf8uk+eB8ukJWj82R54Jf5Xsp" +
                "2Kft/7/vpZqDZd9LhdoAVw2PKfxeqnguWG6Ww47lljyK6q86Kz" +
                "U96zUSq7NjOUWOg/z2fEaa5WP/zFl/RdhDMNmOchWhQrbcNzw6" +
                "rQ33aUFZv9J4rpkLXm7Joyi73Vmp6VmvkVgd9VyOuFo7xEgzd+" +
                "wBzvorwh6CyXaWqwgVstXd9NH52mA/62X9SuMDbzcy9/m4iv0o" +
                "jMMqrL+jrJLtpRxqOEC236Batu/t+iQzOadtjM52S5Tv/50xuk" +
                "+83qwHDy3m1tOAn087KCcImQXlR9D5a2V8cIYnght7RvBh35jT" +
                "vbK7+6vQCtk2byqrS6fifLpEszEmPCb0lpql4OWWPIrsLmelpm" +
                "e9RmJ1FS3liKu1mxlplo4d5Ky/IuwhmOz+chWhQra6mz46Xxvs" +
                "596yfqXxHrMHPLSY20OjOqM6w+zJ9nONslIvi5kh5oPn8RmCg+" +
                "uuwId9Y073yr7ZX4VWyFZ3k0r+eWahDXj/XbOxmvCY3IyfD3ym" +
                "+fnk5rEd7Wv0PdTu0fd7QH2OPi9Qmcv8OzXfd3XU/CzNA39VCZ" +
                "4u6Lkg2aqfDMZux7v6Ff5dHY7n0fh5I//8YjM/F+gnE7vb166f" +
                "C/S/1sL2PP15QfNPvU8MtqjngjVmDewYWty7NTRsu7aPcoKQmY" +
                "cfM0PMB/u0N17FaN035nSv7Fv9VWiFbJ32uC6din16WrMxJjwm" +
                "9LYaeEfJYm4rDei112zF+12BkFlQfkQMwgf3u7wycKvg4Lor8G" +
                "HfmNO9smf7q9AK2db2ltXz665QCryHNBurCY8Jvaqpgpdb8tyo" +
                "Tq1OdbHU9KzXSKx+Klc54qpj89dqvPBoTsFkPy9XESpkG3cTfb" +
                "pza2FZv9L4NnMbeGgxdxsNeE8mKScImQXlR4SP+eB5/Pp4FaN1" +
                "35jTvbJr+6vQCtk67XFdOvE+tRdpNsaEx4ReYhLw0GIuoQE/C9" +
                "9POUHILCg/InzMBz8hu/EqRuu+MSdq+UB/FVoh22anrC6din1a" +
                "qtkYEx4TegvMAvBySx5Fgx1npaZnvUZidRUt4Iir7TFGajZ/Rd" +
                "hDMPaD5SpChWyd9lhpqA1UrS7rVxqvM+vAQ4u5dTRA2+9TThAy" +
                "C8qPCB/zwZWwMV7FaN035kQtf9BfhVbINu7md+J9Gpii2RgTHh" +
                "N6s81s8HJLHkX2CWelpme9RuLi6WU1ZXi181tXM1KzqfMpqmqM" +
                "/Wq5ilAhW/t4yCf6dOf2qJlth8N+pfGwGQYPLeaGacDT1VHKCU" +
                "JmQfkR4WM+uBKmxKsYrfvGnGZ4cIrd3l+FVsjWaY/r0qk4n07S" +
                "bIwJjwm9W8wt4KHF3C004D15lnKCkFlQfkT4mA/euYfiVYzWfW" +
                "NO1HJ7fxVaIVt7sKwunYrz6W7NxpjwmNC7wdwAXm7Jo6i+wVmp" +
                "6VmvkVhdRTdwxNX2fkZqNn9F2EMwdme5ilAhW6c9VhpqA1X/VN" +
                "avNN5oNoKHFnMbaVRPrZ5qNtqHuUZZqZfFzBDzwZPfqfEqRuu+" +
                "Mad72Uf6q9AK2cbdnM2fxwttsE8PaDZWEx4TemvNWvDQYm4tDX" +
                "hP3jRr629yjbNcp2GX6ogZYj64A/1I4wQf9g17EMJp6adCK2Qb" +
                "d/M7Ffv0u5qNMeExoTdi3N+CocXcCA14D580I/ZprnGW62UxM8" +
                "R8oPzH8SpG674xJ2p5pr8KrZBt3M3vxPvUOVGzMSY8JvRWmBXg" +
                "ocXcChrwHr5COUHILCg/InzMB0/ID8erGK37xpyspZ8KrZBt89" +
                "/K6tKp2Kf3ajbGhMeE3nwzH7zckkeR/a6zUtMze3DdqVj9VJ7P" +
                "EVebLzJSs/krwh6CsQfKVYQK2TZfCPlEn+7cuaasX1mcbs0+lk" +
                "5UKvJ3PcXT4vP6OLKPNob573rS/L7aKO4b9P/K1N/NXFJ848qf" +
                "tj7WWFl5m3/JL3HdidnvwZrV4Td7jXMay5LHFBp+HiTPFdFM7J" +
                "Q/ZTdWgXaoNUbSqdnBxiKfC/JfbtxY7NOL7u96Gmvc53S4dnHO" +
                "sbyxwv1dj3ofLzWXiiWPosELnNUIvx7HISt7WP02IzWbvyLsIR" +
                "j7xXIVoUK2TnusNNQG+3SsrF9pfKu5FTy0mLuVBmj7HuUEIbOg" +
                "/IjwMR/cWZ6KVzFa94053atxf38VWiFb+2JZXToV+/SaZmNMeE" +
                "zoNUwDPLSYa9CoDlWHKCcImXn4MTPEfPBEMxSvYrTuG3O6V+OR" +
                "/iq0QrZxN79TsU8/0WyMCY8JvevMdeDlljw3bLu238VS07NeI7" +
                "G6iq7jiKuOzV+r8cKjOQXT2F+uIlTINu4m+nTn7vSyfqXxbgNP" +
                "qWQxt5tG9ZTqKZQThMw8/JgZYj54h0+JVzFa94053avx7f4qtE" +
                "K2cTe/U7FPX9BsjAmPCb2bzE3gocXcTTTgPfkm5QQhs6D8iPAx" +
                "H7GFqxit+8ac7tWc0V+FVsg27uZ3KvbpKs3GmPCY0Ftl4D5KFn" +
                "OraECvRygnCJkF5UeEj/mILVzFaN035nSv+rH+KrRCtnE3v5P6" +
                "HmGVPmY5Yn1M6I2aUfDQYm6UBtwzXqKcIGQWlB8RnrIDa4UP5k" +
                "fjVYzWfWNOx9Rc0l+FVsh2YG1ZXToV59M/aDbGhMeE3hwYk4Ul" +
                "DyP7KsaYAewchZSMH08W/0sSM+jzOmGe0xyVLKInC5zulWM0Y6" +
                "iCugTHEHRTaycVE/J2D/tsii+MrzXXgpdb8iiyrzkrNT2zB7+3" +
                "qFjdvSAzMMKeqwzsY6Rm81eEPRgzMNIcL1cRKmQ7MBLyiT7duX" +
                "u0rF9pvNm4/zWBFnObaVTPrJ5JOUG4LNdpCJ6qhCeU5iO2cBXV" +
                "/b4hJyHSY/66WINv425+J96n3sn6mBjjs+dxy7TAQ4u5Fo3qhd" +
                "ULKScIl+U6DcFTlfCE0nx2g17JWar7fUNOQrS+6q+LNfg27uZ3" +
                "KvZpqj4mxvjsebzJbAIPLeY20bDtapVygoDsBNdpCB6r9L+LNr" +
                "mVPh8or8arqO73DTkJ0fqGvy7W4Nu4m9+p2KcLWK3G+OwU12bV" +
                "ZsF9FC3+X9hZNKoXVy+mnCBclus0BE9VwhNK84Hyi+NVVPf7hp" +
                "yEaN3nr4s1+Dbu5ncq9qmqj4kxPjvFZrvZDjuGFvduO43qGdUz" +
                "KCcIl+U6Dfg5rqLqGYQnlOYjNh6cpbrfV6oSwT49568LEaGNu/" +
                "mdiv9XdYI+Jsb47HncMR3w0GKuQwN28UbKCUJmQfkR4e3qkI/Y" +
                "wlVU9/vGnO7Ver6/Cq2QbdzN71ScTzVQO+z3io8JvZqpgZdb8i" +
                "iqrTS11tlS07NeI7G6y9c40mz+Wo0XHs0pmNbPylVQPdRT1s3Z" +
                "/PsW1bl3W1m/srjyf19OEQk=");
            
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
            final int compressedBytes = 4334;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdW3uMXWURv9AHiIrSWk0akVIFfEWRmBgR8Lv39JZgHzREAZ" +
                "XWGmK3ElK0sYVqIufce3f37lapLZJow7aF2tI/fOAfxsR0+9jl" +
                "UcQ+LKWuWBBFoIoQC0i7bek6882ZM/M9zgXckzPfzDe/mfl93z" +
                "2v79y75ipzVaVickkabsm0ZBra4qNe9rv9pCXTKvaPUKTrbG4s" +
                "IzTOzSmYRtuNc/0+H2bgM5VKXLX/Pj0mnS+wbzQ3gpZL0shqHk" +
                "cpPt3qGLHVqG9kq/DuYaTO5kb4NRjT/dzM0+MsfIYsu5/18wk/" +
                "Xbl/b6xe1L7J3ARaLknDLZmeTDc3NU+Kj3rZ7/aTlkzPj6fp1G" +
                "NrqGxuLCM0zs0pmObrbpzr9/kwA59pY6Og6W/lB/SYdL7A7jf9" +
                "oFlp+/ppA25jpr91D/u4l/0xmzNQb3VFpdJ7YYHbJ7jeDwnerx" +
                "vmxEy9F5Sz0AxZVlfE/Pk8FUxhni7Q2ZiNPyarrTAwHpK2bwVt" +
                "lUqrQn2CkFZQrkV46u1+QfJBuzeMYrSuG+bEvfeychaaIUs47y" +
                "J+qVTM09d1tooasR6T1fpMH2hW2r4+2mCeTqM+QUgrKNcifJiv" +
                "UpmxMYxitK4b5sS9t1bOQjNkGVZzK6nzvU+PWUasx2S1zGSgWW" +
                "n7Mtpgnk6nPkFIKyjXInyYr1LpmRxGMVrXDXPi3lsvZ6EZsuyZ" +
                "FPNLJTVPmR6zjFiPyWoLzULQckkaWbVLUIpPtzpGbFV9IVuF93" +
                "FG6mxuhF9DMK3xcRY+Q5bIPWTqcyMrrBe1F5gFoOWSNLLqz6AU" +
                "n251jNiq+gK2Cu9jjNTZ3Ai/hmCQS4yFz5Al4X2mPjeywnoxu+" +
                "L91UdZa010Pd1fRF/67/zJb2Xjd91fYl/jfB0J9m/Y7r4m57O/" +
                "8qb+GvCs2n2d39t7ZaeYdKrf05rgj+aN/tIjlu08h8sP1HxOMp" +
                "NYsk1W7WLqE4S0EqNtP6vkg/YAI3U2N8KvIZjGujgLnyFL5B4y" +
                "9bmRFY7Br49tfRTnnCR9ArTBZ/JB6mMpSB3DlvvZ+fngiDjAkR" +
                "yVVt3j2PWKZY+nq9xqrj+d6vKBZ79Dfj7hJ1mZtUa6XMQ2q8wq" +
                "mDEr7dytog28L5lV9jmzQEgrKNeiDGE+GOnjgoPnzALv1w1z2u" +
                "gLyllohiyRe+jPnzMLbpqt1ArHZDX4Ay2XpJFVvxOl+PAve178" +
                "up+ji+qGLfZW7zbGjdV4yaNzCiY77Ma5fp8PcQ+Z+tzICutF7Z" +
                "vNzaDlkjSyWhejFB9uME+FX/dzdFH9ZrbYW/0JIyXKmafAqzEw" +
                "T06c6/f5APdP+vmEn1s5Vi9qzzKzQMslaWTVzkMpPtxgngq/7u" +
                "foovostthbHWCkRDnzFHg1BubJiXP9Ph/iHjL1uZEV1ovaS81S" +
                "0HJJGlm1c1GKD7fGteLX/RxdVF/KFnur6xgpUc48BV6NaVznxr" +
                "l+nw9xD5n63MgK60XtKWYKaFbavim0wbF7CfUJwkyB46nw637S" +
                "VHUvH8zTTxkpUc48BV6NgePJiXP9Ph/g/ik/n/BzK8fG4Ne32h" +
                "qzBjQrbd8a2uAzmUZ9gpBWUK5F+DAf3LEOhlGM1nXDnDb69nIW" +
                "miFL5B76pZKapzV6zDJiPSardZku0HJJGln1w6ar5+Pi062OEZ" +
                "trN1dQj62Re3umMlJnU3wDr8b0vBxnQX6fT6UyY5mfD2X+XOBU" +
                "Nl3N2/x6UXuD2QCalbZvA21w7H6a+gQhraBci/BhPhjp+8MoRu" +
                "u6YU7ce7eWs9AMWc5YHvNLJTVPG/SYZcR6TNg2nuJnUnryzIqn" +
                "5KwWW/u5T7PNJbK+awx1Wj/1vtB5fcVZG9Nia7L2LP95X7MO13" +
                "c9577Z1V05xh2pWWvWsmSbrNZnqU8Q0grKtQgf5oMrxu4witG6" +
                "bpgT9/bschbuGMqquZX0+PWYZcR6TFZbZpaBZqXtW0Yb1Po+9Q" +
                "lCWkG5FuHDfHC/Gw2jGK3rhjlxb88rZ6EZskTuoV8qqXlapscs" +
                "I9ZjwrbDOvhz9dHGpje3DtbrTH3MNrawt3pC0I3Nb3Ud3Hjira" +
                "2DW5fG1sF0Hf8/18EDZgBmzEo7dwO0Qa0rzIBdBxcIaQXlWpRB" +
                "8rXXsafnw4Jr3y14v26YE/f2QDkLzZBlz0Uxf36/K5hqtlIrHJ" +
                "PVFpvFoOWSNLJaxixu/1x8utUxYqvqi9lib/tCRkLWX3CvG+HX" +
                "EEyzK86C/D4f4P55Px/KfJ4Wu0zDelF7tVkNmpW2bzVtcI7PM6" +
                "vb29jHveyP2ZwhzAfzdLng2oOC9+uGOXGHeSploRmyRO6hP5+n" +
                "gptmK7XCMWGbDqdD+v6YnZfugzP+IHwmSfone/b/xb7vfQb25/" +
                "G9L1xZXgbc+emr1juaVbLxNvJMyZO9M5vs3dmvsOin07+lf4f2" +
                "H+nhvN50vgJk47IJYL/d9r4re2/2vnRHujOP3pW/29ufPkb4dA" +
                "T2JwEJzxHp1PRfNvux4r1vLTvLPuXYdUf6cPoHQO2NPQ2wlh5J" +
                "/xM+F2SnWzZnZ5NM0zRhxqy0c9ekDT6Tj1GfIKQVlGsRPswHI/" +
                "1CGMVoXTfMaaMfKWehGbJE7qFfKqnjqanHLCPWY7LaerMeNCtt" +
                "33ra4DP5sllvr+MFQlremku0xRnCfDDShuDgOl7g/bp+DULAdb" +
                "yUhWbIsnV9zJ+fdwU3zVZq+dnJxqOrd2nvt+GJ+Zbeb+l3ybXL" +
                "6qPtEblX0o7ft0hPc8kbvR8vzrte/+nCfS5w32L7zwXNrvC9uz" +
                "pnpvoMkLv7bl+eC8q+bwmfzBsvKjbH4xugJtSP4zrY7fVRMUt6" +
                "WWtMrO31cfl4j5Ms54Gy5xOdWDTGS06SYTWb5aOao9R3c8dtcw" +
                "dsm0jCEUaatWqXWo/tAc8dCimba2+yR+kdHANHUbvAKaTKtolk" +
                "0aO9RQXmFWeR927S0nJ3OBIvlWlTft6pbGjlfSo72XS/Sx9IH8" +
                "yPvz3pvvSPeL+r3cD3OyufSZ+l+136Uvpy+gpor6avhfe7/C5W" +
                "3O/SXenv00dttr/G7nfFcX8K73fZGXi/y97h3+8g+pF0N6Dgfp" +
                "c+Du2BdCQ9lD6ZPkXnHd7v0hfxfpdNzN6G+Oys7N3ZOdmU9CG6" +
                "34HcW7vBov+cPqHqPgf7P9MX6H6X/tf2HU2PpyfT17PT1P1ukV" +
                "kEM5ZL0shqzTeL2k+IT7c6Rmx1dVzEFnvbfYyErIPc60b4NQQD" +
                "z09RFuT3+QD3G/x8KPPr+CKXaVgvas8380HLJWlk1a418+3z+H" +
                "zp136UcL9Ttqo+ny32tu9npM7mRvg1BAPzFGVBfncMzN3NhzKf" +
                "p/ku07Be1J5pZoKWS9LIql2PUny61TFiq+oz2dLZ3FiNlzw6p2" +
                "BaC+MsfIYsw2ooW19zuZEV1ovaK81K0Ky0fStpgxX+A9QnCGkF" +
                "5VqED/PB8TQcRjFa1w1z2uhXy1lohiyRe+iXSmqeVuoxy4j1mK" +
                "xWNfAcw5I0supHjH3CEYSpYh/7UcJ5p+z6kaJ6lWIZDyPdz0jJ" +
                "4hxPVTezWJKHPS5aM2QpPLWH+LmVY/VidvEcsTW/UxW/oKk+JD" +
                "5+3mqlsL7bGvk1Dr73HW51S578fGkVz5kH29KbBe9Yt3Ic/q6n" +
                "PS74nU6zd0wwVIWt+tbwvW/1ocYxQrTHS0Trdn+0oqVHyjkVLO" +
                "5sreLngqz4jUKG6+6D9io6oWrfg6PE54L4PHVPzO6EqNviz7uU" +
                "Sf81z1bPEd9LT1kmP2pMK4tOdzfHF3fzA7AfymNvrS6JV8vOUR" +
                "WWaw7N/JPI7sLngvg8wXPBN8MVRn3IXz20+oqn9iGU/Dux+hCv" +
                "PPB3Yv66pT7kri/4d2LVjfF1C0dhVq7cfZ2/bum7RmOsPlrwGA" +
                "rXLa229Q2pdceQs64a8jVat+jfiVE9jk9OJafaR0Hf0X7N/kYe" +
                "bJQwsj3Jqdq9qNd3UA/uME87SE9OMZ41m3OHZHHOhHsEXX8xjM" +
                "KsXKN9jPySBXkIBnVkRXjplZrInVBcFXXMks/BDl9Lj/iMqR7H" +
                "m6vN1fZadXV+XbM27vWj2sO9uA4mnfs5QjJoq6h6tO2h3bq6cn" +
                "sc+TWX3jHBaD4ul+J7qUu4B69PkqfTOjj0O2OcZ+bh+wKQ9n0B" +
                "2ijh2P2xsUehSNztPM1jHLXadr3qivFDH62zs0+q8ZZzuUtjNB" +
                "+Xi1RzuUvmDvMU+J0xzjVz8bwDac87tFFCrbVmrn3/NJf6abfz" +
                "NJdx1LJdWytI8qp5WuujJW/OqqjcPkZ+6WndqzGaj8tFqukezt" +
                "P5vYrP2B1jcjI5ac+L7fn/8Fgb99pAcrJvEHX0cS9cn7aTTnHY" +
                "slYb4Dzcp+ZpoK3QOoqqY1au0R5HfsnS7ILnggKDeGRFeOmVms" +
                "idUHjeoZ/QxVVgu6/B9cljTPWKMY4mcE0naftGaYPP8JfJaN8u" +
                "9nEv+2M2ZwjzwRXjq4Lre0Twft0wJ+7NrnIWmiHLnq/E/FJJZk" +
                "NnY4w/JmzNHDPHXp/m5NcnsFHCPN1v5vQ9RTpJ3O15N4dx1LLd" +
                "+rUgyavud/f6aMmbH+VFZfZLD7/PlLzscbkUzwW/0j06c4fzLv" +
                "DrMSZjyZh9LhjOnwvARgm1fpuM9T2Nen2YenCH826Y9GSM8azZ" +
                "o3VYsjjztF7QcDwFUZiVa8BzwRhv8t5XMKgjq2SseSa1OpfFH0" +
                "KdmFJV1vNzatjX4LzzGFM9jjezzWw7Z7PzObQ27q2t2sO9onM/" +
                "R8S88jfjsI926+rK7Jee/gkao/m4XKSay12zL/sL/XqMyYnkBM" +
                "yYlXbuTtAGtS6nPkFIKyjXInxzhZ8PrhgLwijyu3XDnLj3Tyxn" +
                "oRmynLEs5pdK6vp0onmbWyscE7ZmspkMM2alnbvJtME9w1CfIK" +
                "SVGG2rT8LLB+fdJkbqbG6EX0Mw/WfEWfgMWSL3kKnPjaxwDH59" +
                "bJP9yX6YMSvt3O2nDc67HdQnCGkF5VqED/PBPP0sjGK0rhvmxL" +
                "3/PeUsNEOWre0xv1RSx9N+PWYZsR4TtsXKcqe/Dq7VilXgTuc3" +
                "dzvL/u9FvOH3d9UNnb6/g6idZd/f2TX5To2hKgWPnZHv7yx3YS" +
                "porhfRPMZUr1gH7052w4xZaeduN21Qawb1CUJaQbkW4cN8cA87" +
                "HEYxWtcNczKXMhaaIUvC+36ppI6n3XrMMmI9JmyLT2hbcDzNLG" +
                "Zzm3M8bet4PG0r+T74lY7H0zbMWv59cP9nNIY4FDy2RY4ny12Y" +
                "CprrRTT/eNqm45M9yR6YMSvt3O2hDY71B6hPENLy1lyiLc4Q5q" +
                "tU+j6icYL36/o1CNE/s5yFZsiyNRzzSyV1PO3RY5YR6zFhW3xC" +
                "g8F7ukeL2Rx0jqfBjsfTYPz61Le04/E0iHHl16f+KzWGqhQ8Bs" +
                "PjCf//TjMVNNeLaP7xNKjjk33JPpgxK+3c7aMN5mk39QlCWkG5" +
                "FuHDfDBPK8IoRuu6YU7cG+vKWWiGLHsWxvxSSR1P+/SYZcR6TF" +
                "YbSUb08zvZuLf2JCP4O9ZkxK5b8l67GhghJOPJp71sNbYUb86+" +
                "IejGZo5y1ggjUoP80tO4T2OoCvOQXskI6+ARzZTQ8l4lXLfoaM" +
                "1J5iRf3zwY/B/1XuD3cPib9Nj3CG/8q/aeW9W3Dpsj69BTnX7v" +
                "3tiF3yMotPoeIfo7++X+9wi27saQI3+PEGHkfI/g/yXFEdDCs+" +
                "XuuK9S6U3KI2N/M9T3MP23VN7CX7Il2dLs6ozoVE3PU2eOwf9Q" +
                "zIiM46K8ZvFZt/C59h6Hz+aObCPe/uVF/u+q3u+EdTvlTTb3Ly" +
                "ud/4tilXU1Z542v5V5crj9Dw+aHxM=");
            
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
            final int compressedBytes = 3342;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W32wVkUZf/EzQLnaHeTDDCQkmGj6w5xCi+655z2jeHP6mm" +
                "ayKTIosBFhprTMppnd9973XO55Z2oqC2TuNIGh1h+JaSQffhAg" +
                "kYSmwEVADVS+JPnWriPY7tmz+zz7dd5zuNh75+zZZ5/f8zy/3X" +
                "d3z+6e94YLwgWVSpim/MNzQuraJspChYA7oHRJ4AENuZ67bSuJ" +
                "xnFtnzzt6vOzwAxl2rXVpQduFfXB3iQbs05prjfsZbk0Tct6xV" +
                "+lEtwZ9nYtljpZKvUuWXoAdFKTuZ5OwCUE8GZc22dqQf0sMEOZ" +
                "cu62vnafXledrcjpXECW+OqEzO4BWRJ8v2J8QOf65Gt7YsjXB0" +
                "Nexs3zGz6Q/MGn5fZ2ZJt7EY4u38pyYbhQplIWUtvDogwQcAeU" +
                "Lgk8oCHX8wvAcc+g1+PaPnma/NHPQq8DcLf1wA3XH9cZc9broN" +
                "ruKnGP+tW3PpSN88W4fUHn+uRre34J+eQO9J1d1fx7jfqTO73f" +
                "ObMno83S+hAXtnZfpdTHxa06PmvfJSrWMLOdQOfs07nauBu1E7" +
                "Hj5vkNlyTUW5fxrshx7G6nfI6+NnF96BjVTiMs3ZS8XkM/0HWj" +
                "rsVSvLhyhh96Lb0u+VNeH7b7U/w7j69P63JXR1EW4aJwkUylLK" +
                "T65eGinl6MgDugsBRNEB4ADbloAuCSI1ivxzV9Cqlztp+FXgc7" +
                "GmhYf9LqKnC4zpizXgf/LFO/ouz8FI0zy5K3lBbpkhNl+hPz25" +
                "+czEPY/clmIsadWQM3zjkGx1THyFTKQqqPr47h7QQIuAMKS9FE" +
                "4QH8NZRdNBFwyWlZKtA4rulTIJL3/Cz0OtjRQCPmcairwOE6S0" +
                "7YOpPHVdM2rWYtK2R+1SdVx6XtpPSAwSl4iEaq1h9XNb4poRPo" +
                "5AS20m2wV/Ai+xOUSKy4TF/RSJMjT+3nHTDG1mYt7NqwsbhOjb" +
                "trrOfdutyZbl001o/GuuR02bk8eS8/slX/sS6No53GnunTJfyb" +
                "aqcpfp3bMvqoH63ryn4aPyjG2YyWz3ggrMI1qp2uZavDXrfObR" +
                "lN8qOxrvGzsqw6ZxfjbEbTNY7+NOmM22m9aqepVjutL2bpkmO0" +
                "Q2v86iy3k8UrftClKbseL7b7qF9fbmdiPXM1uX3GgMbd4nLrAh" +
                "ktn3Gp/rM8XC5TKQspmCDKABEur39D6iVKlwRelrK2/pbM9dwP" +
                "uPo3AW/GtX2mfqZjjY0wU87d1te/DvWB+uM6Q41xnXSLzG6t6g" +
                "97bZ1d5rIEGfDhE6in7S3dy3MtzMiA1zXl4+bEfEyNu2/7dfmW" +
                "LrnnoQGNuyVlIjPutxRhPJAP7IODK8x5nN5kza9z8T5Ynw/o57" +
                "HUsxXV+q9nax737YM595TD8PdrHg+fVt/JTOt593ReO5laXQ4O" +
                "oHZadZafdxYvGU3XDKSdwgPhAZlKWUj1W0UZIOAOKF0SeNsfY/" +
                "6GbSXROK7tk1+NtX4Weh180fRIuP64zlBjXCd+57txsSeH3bmQ" +
                "6reJMpkCEttISR9zpj/G/KC0lFYkME8FzBjgpbFOj6bryWidjy" +
                "sa8ONSfQ4euRipcwGZrCVsbU/WkWxFRjaT58i/yDamPUn60pKd" +
                "afoqeZ1+id0PkTfJMXKc5U6Qt1jaTyv0PDk/qbnpYtqa+fs7+Q" +
                "d5ho3gbeQV+kXyb7Kb7GGlr5H9xhroND2Xnk8vpEOZ9UW0hV5G" +
                "R5CnSLaeZlw2kn8y1PPkBcJmOrKFbCe7yEvkZdFO5CBL/0P+y2" +
                "wvoINZO62gQ+gl9FI6nLCRRzaQTSx9Nov0ItmhxucKwp6B5AB5" +
                "gxwlR1gu3W+Tt8k75F1yig6i56S1GUY/2HawjcWQqcgJqT6Xp6" +
                "DDd5nrnItlqLUokTme7+mTSOxNtzBjAKY+z83CZCjTYI3pD/jh" +
                "yBxnx3PJ9UGy74kehs5971K5H+t9NCs933gWX4Dy51lnFI9oWN" +
                "b36ue6Vu/1c1xr6MZ6c1xj1vbzrvpIPUXVL2zydL/Xv2KHmqrv" +
                "8oS4eEp/q2JtNC0Frs1zGonbyfG+5a18xuDV5b+xUS+XjCVrq5" +
                "02ivKm7bS47YSfkanT+pN6dsZXulYrdisX+/S8W3Rf6fNfpj/F" +
                "Y4vt7ujvS/Wnk+LiKYy7+Gp2fdLGtZ08k3YKHmzSn07aObR+2q" +
                "qXS8aStVX/pT5PBu5+P0avKe0zLF9QuW3Fzgvo9iLt1H6xw/L5" +
                "IucO2JZuKXZeEBRc8QeldgbBMHHxlB5SY/wZN04gy37ahzXnYO" +
                "Z0W1wuGQfD3BaCexGmfoxd06BFXDylh1WsTZZlihPI0ifcrU34" +
                "ttg5HwIYS9ZWO23K8+Tzamp0HV1DZ9CnyHq6jG6kM+kYmu2x6U" +
                "prnHyHXd8lb6b5zWnq7LfU6ol0dTIxny9R7xaoYy9H2aqCrzMV" +
                "mo0/souV35pq7f3dw2n5pYaXFTaO7KUbPIxO0e8ZM9YxcfGUHs" +
                "32PaPCUWweH2/jBLL0PnJUk3n8mJ3zIYCxZO2O1pwpPeHH6DX1" +
                "7+/iqVF//Ln8/V3nXHNv5NvfJTeZ+7My+ztzN9l0f/dk/v5O9a" +
                "cni+7vwqMh60EiTb+Jo+KvszusijJAsNKFUi/+AJ9q783wVdMf" +
                "u1dtK6HX45o+JUK3szkYadWlh0io5x0FLI4FZUL27YPDyeFk0h" +
                "fP0PbBt7Ey5z6YzuH74HByFnuyuQ9Ovb3CpDPcBzN9qX0wY1Bo" +
                "H8ykvYxZgX2wPU5k74vnuNanev9s/lZDjbsvFHlbY5/QuEYyHn" +
                "+wftLG3aPmaHOvs4NHzdFszgGW9S1ReqZM1T4sngs6fJeSp74e" +
                "bbw93yrfq4nhOWBl28Z9JhcdIyX6tj+um5NsPdi31KwVsv/bbv" +
                "6OL7k536r5Xkyf1TErx3p8pWtHaEvByqL7u2h6NB1SkRNSfA9P" +
                "MULX27LpVeZ4PpktkdibbmHGwBg3C5OhTIMnTH/AD3vlODueS1" +
                "Y8ZkYz0150ueq7vwYdvkvJ8y14tNVl+Vb5Xk0MzwEr2zZ+0eSi" +
                "Y6RU+7A/rptTNCOaYcxPv2HXZKGTGCx5vHu07VvyrfK9xq/qGJ" +
                "4DVratiIbLeR7et0hNbbw/rssvPBtg5LeT5s878V6q0PMu/n8+" +
                "7zj3Is873Zu9ztSt2sRpzVJ+Se9tS+OlPOUl4E8goUTgpSy04I" +
                "W1TrcskZZ4PS5Ksc5mCtwAwyXcBnw9LsokOlhlcpRa4JjOT6uw" +
                "b/+3F/VXW6tsRSjSdB5pFX+sr/9FlAEC7oDSJYG3/bEWm29bST" +
                "SOa/tsxgLrZNo136WHSGjebMV1hli4TmludFV7qgo5vT4jNJBK" +
                "pF4uPWA/plfWTgswGqxckaXeh7H5mAyCP5scdX6Aw74dTx9lL/" +
                "YtrG/Nkr8vIM+xnrytUukeJN/fcR1hMyntFkiS7qNJespuvr8T" +
                "fmDfotopPRHy7VuiWdEsvm9hlkNTe2vfIjB835Jx2M6ul1R5+v" +
                "6I71uy+j9Gh3CN+H0B37cwzLPYl8TJHEMdMcYc52TsW9BJg1pn" +
                "dqPfLtL0nKL2EN3t/31B7vnFnmTzQH71QHflnl3Z68zVBU9yV5" +
                "dlEnTDle1m7wFdcHt6v53uwwhXO2FtkXIVIVdP9+sYzFSys6Nh" +
                "n7p/ZNtdnBPlpwLpeQE/z1T96Tr7PJOekueZJfvDnmTHgM4z38" +
                "k7z3T0p8dd55mOtniceH87Zp9nphbz4cra6bNYl73d24cRzsjz" +
                "y5VLLehdz+e0PyEMZhrMd7TTfDOmHh9s/bzydNr8NNXuT7WWIv" +
                "OTfT5euyTZWbjvOfpTbWieRe0iq471grMNw/nOxz29bH3nYKOd" +
                "1K/n6E+zVhnC2wmPO7ud6F2edjqU2zY/Ibn/o1Br5eOO3m2Ou9" +
                "T2R452Wm6OO/pDLd4dElfmPYLG6DKVG1E5a5/ayMaHKu/bpzbc" +
                "aqdlBfvTsrKxggZcWX/6Mtal92np/NTI8TINa4Npuv+86KDHVt" +
                "r8hDGS7bS0fJqrNnpMPb6qUcPPy9a1HRcXT+nPFc7qjwInkM7v" +
                "9WP+lmhc2eS91HE750MAY8naquOGPE8+r6YG6wJjFQqyqWElNz" +
                "jb/YgPgaXGuHzLZlE8bG9wW7g9F2VQaKx/XI27r1heo+Znsz4p" +
                "fr3wDBAVZysi2BbxawVr+4nS81MCV9ZOX8W69N6BJaeXDqwNOn" +
                "T/edFBH3QUwEi2HWl5h6s2ekw9vqpR4uel6/y/66ldbVne6Hym" +
                "b/chsNT+ssPS+bsed5TMYosLa1u4ojn70zVl+hI+H6t9SvmY4s" +
                "ad2e/pGh8pOmLPyu/pdjQ7Z81wO4u+bwkOG9/QYZ/GF9fGOc/H" +
                "W4paOn4n1pJv4di3HC442xwu+31H86J5xnr8a+o9wjyJwZLPj6" +
                "eddheJnvseAWF4DljZtiIaLud59B4h09Ta/XHdnKI5UfqeHNZP" +
                "3TeDDt+jObk19mjj/flW+V5NDM8BK9s23mdy0TFSql3vj2v4/R" +
                "8kkc2x");
            
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
            final int compressedBytes = 3105;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W2+MFVcVf/afRPlTIRTKB4p8ahPlg5o2aKL7dt6UxMS28Y" +
                "OapppgipUWNkCN2lrT92bJzttdSGOMCm5gSQjfjImfapMmLS1Y" +
                "IxqksFhiSIqtsYEGKK6ruxH13nvmzDnnnnvnzf7zTebce+75nX" +
                "N+576ZeTP3vddoyFfri9jLn2gs4Gv0vvl4E6t61uYt9eLWxdEr" +
                "3ZputW3nhXKetjcaQ5fBhhiu6Vf2pbi1/+062WOvoSsSY3vESv" +
                "tCNj5u+9lRrjnGD8Xzhjmlu9PdzvORMtdhw+8q2BDDtcBcHI5b" +
                "ra1innbHo4YwtkestC9k4+MSg1r25XjeMKfmMO3F8fRdbnNti2" +
                "vB2NPcmk7n3/NjRI7/YbJDlh6Ygmk67cZboWpkTpm/rGg4zits" +
                "a+6nvZinZ7nNtVuyr3BEIMoWbm1ukfEr/PaTnXuVZ/RXPQyy3e" +
                "LGt4SqkTll/rKi/XFeYVu6M93pGD1aztM+svEWNfsaHNBxItfx" +
                "z1eeUztjfmGM7REr7Zt9zeciMahl34jnreaUfbOcp/36rJrH59" +
                "0Xan+iBLJkj1d5tNcp/NZ6ubJtc62nc085TweCVVysrPFiWMue" +
                "Gn1sPvcF2UBVPs2p77/14tbFVc7TWIDviloxTim/O0e3z2uenq" +
                "60LlXXlppxLa7zRl0WzTtgt7IzVc7T4TAOkLM+757szcHvxRDE" +
                "GFmr+4KnqyKxGX42jqmuFO8zs072wwW9H9/VWLRX4Pr03GLkad" +
                "4Ou5XZUHk8HQnjADnrefptbw5+j9XdlePIGFmr42lPLJIXdySO" +
                "CVeaDqTuWsnOO3OXP/QJsCGGa5H7gsgVd/Rs5fV/IOZXPLfckB" +
                "jbI1bat/87PhfbZ88thSX7UTxvmFNzH+3FPB3jtuKs/BvXgu/C" +
                "vtmNo7Xa3nlPYjjT5j593oGNx5TxyTeeN2xLd6W75Odd9mOy8T" +
                "atvNLErKPVz8G70p7XL46xPWKlffsVU4mRkebCKftp2fvZwl0B" +
                "swOjlxbvOp79RJ2nf28s8outq7y+kHGH/jmvmRib3efd0KOLNT" +
                "9psSqX/aKcpxO+LaQJy/W41dqqsqdP9GZIGNtDLeQL2fh4uIbs" +
                "lxXViLjNHc0dJKEHWv6OlRwh7Vr3o2LP9of+g0geTXr4OTgmzM" +
                "JniHLoph+P+MnMoXwhPbmZ3OR8Qbd7fi25mV+HPkhEQt/KwQHb" +
                "EkJGEdfxd3AcvX0vyox2HYVGEEtceKz8Dj7CI8dfITtjM5PMmJ" +
                "6TbmwGNnPcjSUz6RjacBTtIR0j6HiNRutx7YVonlfHRC4xFpwh" +
                "Sp1NZmIzMcNrRk6yJmz7jtkdt+JJ+hiOgh1HuSb7ZIVRjqW43I" +
                "tySLuOSZL7oWav45STkL7k3KiNZw5Z0mKFJ3ug/Iz5lW8LabE4" +
                "/itfVu1VHdXH2B5qId98qc8lVkM8r4ybnEvOoUQdtHQ8OZeOcw" +
                "S1hJIaRNDxDPOV2gvRPK+OibgYC1lDke1jITuvmOrnNVMuXpPr" +
                "TSQTpuekG5uAzeT6N4wRglpCSQ3wOp65YjysvRDN8+qYvVhwG0" +
                "qdTWZi8zTBa6ZcvCbXO5+cNz0n3dh52BqN7koYIwS1hJIa4HU8" +
                "M+t3aS9E87w6Zi8W3IYyXx2yUyY2T+d5zZSL12TbdFquR4Nu9+" +
                "4asJBEJI0MDlg82HgcP6p5h5/CcUS3m/66OOUAnI5CI5xPex1n" +
                "Ctkkd46ezbo8+bdfb7/m+ttw3bf9R5PZHk9r239yLP5sbW1z/9" +
                "P5JCDbN9z4pJPTnUbnNmPb1FlCcTrLOqu8K+vdDv12+1L7L6Z9" +
                "t/2e4LMt3da5tXO78fyo81/Ruauzpv1q+7jEtN9sny04vGX2i+" +
                "W4+966/a8y29rOR6yls9qNv9H+vcGc5rF0r+09MThO7nv1zvLO" +
                "yuRsYjKDdMfYWdjMPK2HMUJQSyipAV7HM+/wDu2FaJ5Xx+zFgt" +
                "tQ6mwyEzvvzvKaKRevyfVOJ2aeQbqx07CZefo4jBGCWkJJDfA6" +
                "nnmH12kvRPO8OmYvFtyGMl8aslMmNk+nec2Ui9fkemeSM6bnpB" +
                "s7A5uZp8/AGCGoJZTUAK/jNRr9r2gvRPO8OmZypv+VKhbchlJn" +
                "k5nYPJ3hNVPFvCbXu5BcEE80Trd7dzNYSCKSRszz3QX04FY/qm" +
                "F+HKOj3UdQZrTjSP9xidF8OFPIJrlztLhqruf5A893F2RWttJD" +
                "9+Mv9VgT+nVofTz6PcK7c1/zyTfM9vuW/J7FXqdj8/TyQsbtf2" +
                "3xfPU8zSdbZDXhUHIIJeqgdbfBGCHMNoJ2RAl9pMCPoJX5jWgv" +
                "sMu8fkzQut+SfhrhyZGQHXfMWeBGOFJyYfrB5KDpOenGDsJm5u" +
                "lJGCMEtYSSGuAJTb38Pu2FaJ5Xx/Rlb4TJdm/ITtzYcXKQ18w5" +
                "yxrU8VV+x9XdoW2DByqOzKODWPPzAevzcz7ijxKnGKJuNh9pcf" +
                "WYJWPJGErUQes+A2OEMFsX7YgSerfAd9HK/LraC+wyrx9TMqyH" +
                "MLIbsuOelN9KOFyXIyUXPgOxp5zuqLYlWdXzEVpDqCrPnitP07" +
                "O9jtfNZnH1sMmR5AhK1EHrHoQxQphtL9oRJfS9BX4vWtE++AL3" +
                "xFGwy7x+TMmwHsLIvSE77kn5ywmH28uRkgufAe/7u/LOo/tzZd" +
                "tc+c3fkvJXB88E3o95/E6s87lZH081s+WbDNvv1zqexpNxlKiD" +
                "1h2DMUKYLUc7bOZ+nOt5gc8xBvNjnjgKdpmX5Rrn3KRfNcLIPG" +
                "THHXMWuJwjJRc+A41G3xTsVrL7zFfVyvpUMtQ31TcVnfMh2XKL" +
                "HvNj6x67+z0ux5Exsg4ziTMlnK0pxqjKn83TSXNcfsqLW/krcL" +
                "RqVDJc7dnz/Pj0LM+7YLbsaAhXj1lyODmMEnXQuuMwRghqCSU1" +
                "wBOaeukl7YVonlfH9GVvRCgb8uK1SrZo1zVJj2LOf1f2Ti3k81" +
                "H+wOI9kwaeg+9f8N9fFCvOrQ1qffw36XS+Wa6Pp5NyfZxWvMlq" +
                "epNq1X0SPf318daGOuvj+Wdj6+PWX6+PWwZ6fVyfd5ZVyVrdpb" +
                "U2kH9rU8t8NoJ0tk2wmXk6BWOEoJZQUgO8jmcqfVh7IZrn1TF7" +
                "seA2lPlDITtlYnOxiddMuXhNrre+td70nHRj62Ez8/QHGCMEtb" +
                "gNDnANI+h4pl3OcYT38/o5EBFnwW2lXB6yU6by/HyER+O5eE2u" +
                "t7G10fU3FlGcbvfuTW7BUTkCeBmBoohjeClHh7woM9oDUTZyDr" +
                "SrWEt9jpJf6KXtFFfbsjfLHvtWormL5Byv448t3nVc88rvXfB1" +
                "uheTF1GiDtrwh2GMENQSSmqAJzT18q9rL0TzvDpmLxayhiLb/S" +
                "E7r5jq5zVzzrIG8oDbraRcFR/+kJpTtWLO18d9q9Rbd9d514Zv" +
                "mdN7rXhhtuSl/8P6+FtzOAMejGmttYtyxj2osy5OtuRychkl6q" +
                "ANL4ExQlBLKKkBXsczzNdoL0TzvDpmLxayhlg2mYnXz2umXLwm" +
                "bJsrYbeSHU/qX22AA+Ssr+Pf7nFkrNS9GIIYI+vwdbwO0zhGVt" +
                "r3ft/7JKEHWnO7lRwh7VYODnDdj4o92x/9KyJ5NOnh5+CYMAuf" +
                "IUrLXcYjfjJzKF9Qv9p31fQKCT3Q8p197l/5hJB2rbPsV1FD6+" +
                "g1RPJo0sPPwTFhFj5DlPkOPx7xk5lD+YL6lb4rpldI6IE2vMxK" +
                "svGW+5DOsl9BDa2j1xHJo0kPPwfHhFn4DFE21/nxiB+PanE6X0" +
                "gvnwhPpifdVemD8jP6VrLxFrXIc3XEOvpBtRf4Dd/WG4NZiJXO" +
                "mV3zuUgMatlkvJpQXDN6InX/06D/m2dTZONteqKy4oh1dLLaqz" +
                "qqj7E9YqV98x/4XCQGtfy5eF4ZN7mR3ECJOmjDK2CMENTiNjjA" +
                "NYyg45l5+gfHEd7P6+dARJyFrAFkc03Izisuzrs1PBrPxWviHu" +
                "lEOuHdZ86QjbfpRPh+XGLV8VS5Vo3Z62Jsj1hp3+Yqn4vEoNZc" +
                "Fc8bipvuSfdYSbs5nu4MYwgBktpgtj3gUY2SEUNIjIIMNQ/Or7" +
                "laMg1HsjgZjSIR79CqJv8dbXoIdn+9ka869v6+lq939v7ON4ZM" +
                "D/kMOQ9Y9+X8mmvjv/Lla63NtTJaxa+N/wclKhZU");
            
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
            final int compressedBytes = 2842;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW32oHFcV30KNCJE0RWwxf4SABf9QBEH8o9bu16T6+vrxkr" +
                "RNWzUq0iKRSCSi4kdn39WsWyQNr2miGEEatVZrP2y1LzHQltbE" +
                "WhWFmNimKAhW5LWVEmqN2NY598zZc869587Ovo/wspd7555zfu" +
                "f8fjNv9s7s7L5GA175E/mv/KQx/Vph/RFmrYsa6pX/ffr1Ynw+" +
                "f7EYT3nPvzHSO7eRfOW/zn+TP+Vnf21UvPLXfKU3+nGlEX8y/1" +
                "05+1PRj+V/zp8ttn/xnrf58YX8P0Xuit6bUHvvvN7qwnskqvR0" +
                "/oywniv6P/O5/KVi31/NX/a+V/L/5v/LX+2dE+vIjmXHYOvY8z" +
                "3sECOMtOxXdTSdNTpPYmDGquLc/q2hFo3RlcbT1LvfRw/1Zkvc" +
                "5xrL5rXz8apoLzpnllI7Hie3YnicPn8WH6cl1579fnju7mucJS" +
                "9cn9T7bu+iH5ct2RYecVZa+4vtfokI4pEdVqUZzNvfJ6SspjNC" +
                "DlFlv60C46Eei431aWaLz7JL9OnsNIzcG43B5cX5/k5RscQwAk" +
                "femn+H05jRPlCF0hVj5M6XsQpFqSr68HyS+toHtNLyWn6AcZKX" +
                "qjE/tejM9fcFbqW8Lxh8MrwvcG+e/31Bd9+ZvC/o7pvPfUExS9" +
                "4XdM/vnk8j2WgNtqKPEbxllLYQH9crtrfHWYSWvHHNUSr0PqTY" +
                "NJPcf7nPzCX3yc/WdNfIo4029MEujPBISO2nCrJOWLXw7JVozr" +
                "KYKZ7CxHpCBd29oUatz3pZcZnfe6zoj8J9Qe/J4r23mu4LBnsq" +
                "r8X+uth72Iw9FXkOd/cs6J7lwfHuC2y23kHTe3Reitbi1vW7zT" +
                "DWunlUtkZIq3tHXQWjWUJsnFGfbfyXWyOPU3HNuCjGTK8aUeMt" +
                "8nxyw/ua6fMWqO2tVdHpaN23tCfPjMrzyV3gLoRtZ64zRyPZaL" +
                "XfgT4aGSlzyCIcV5H1ZGWdJTNCDonReToe6kHtsdJQG6mWSK1F" +
                "1g/+Np8Y3tP+NDrLJ93aEe+DybRV+700OS42znDrluzzypHMr4" +
                "btO4fn7p1kZUcII634BehUlOvquWavrswYmLGqOFfrpjnz6krV" +
                "R6SsuK69jkecoTW4q71OI3Q8tsOqNIN5/15Cymo6I+SQGFtFqJ" +
                "DG/sqwHuvTzBafZQ+P3tHMr2fu7eQZ3M0xuc0q171UtH9/dVY2" +
                "8uosMTBjVXFu/75Qi8boSnU0uUuL3vKzSXVfcO3gxwJ1XXi9c9" +
                "cHK4Jf19zF7hLXxOud67guX+/6D5hXkym32d3otriPCd/HFeL9" +
                "7oN0vXMfKn2Xi/gV7iq43rlrvHWDHz/SF1dA9wE/tv243mWl9z" +
                "I3YV3v3Aa3Udkfdh9VK+IM9/J8+gnHWtv9djshh1W2RSvrTGLF" +
                "nalcj2eq4yFGKiV1MZusqeuL3JnxNLV2cy+P0z0yppEVe7N7PD" +
                "9Fq+MhRipt7Y6fP2FM1rT3oYrXjmU7sh3+TnaqvKOdohnE5Jas" +
                "xLt6B9dQd8hT1VnVVUMMzFhVnIts0q8xulJdTW14YnOaGvlg5m" +
                "7CWHmElUWZ0pIIsDjSNp4+oVfzAkuszd0sa1AeYpGHOCVbOMLz" +
                "pxBhK7M1d94LnRr5cIaxcg+ukBZFpSURYHFEI6VX8wJLrE3XoD" +
                "zEIg+r1dr1GCNsZbbm5jPQqZEPZu5TGCv34EppUaa0JAIsjmik" +
                "9GpeYIm16RqUh1jkIYxExmOMsJWlNXcmubOHfK3PaKQ1Dz061q" +
                "n4NNKZhFYVJxTjuaJWF2rXY6imijeM6edPvbX0/Kn/s0X8zuvw" +
                "AvPHfP7k3lX/+VNtDYnjNLh3MZ/Tndnj1H9oqZ7Tda7mzp7iL/" +
                "Np9rU2sF9ixD3HBpmPeBupuaFxDUsbohiPXsBqHp0RjqEayWup" +
                "Cq6AL0KnRj6cYUwjdWZYh7wap5HSq3ljDNfT2tiC+0zJKbWHak" +
                "JEFbOONI9T59G/7w7VeM64rd7Z2jzeXrGQ9x1pqhu12QzcG8ZS" +
                "cRI6NfLBzG3HWPmOuFFalCktiQCLIxopvZoXWGJtugblIRZ5CC" +
                "OR8RgjbGWW5tQ63rljuazjrZfcZ8dcx3++2Ot4djI7SSPZaA1m" +
                "0ccI3lJz26RFFeJ6xRl+rsQxPuQNOQiRVqH3IcWmmeT+y31mLr" +
                "lPtG0+C51aedaVM4yVf9frpUVRaUkEWBzRSOnVvMCiMVxPa4MO" +
                "WORhtVq7HmOErczW3H4eOjXy4Qxj5R5cLS2KSksiwOKIRkqv5g" +
                "WWWJv/HBxogw5Y5GG1WrseY4StzNbcfh06NfLhDGPlHlwlLYpK" +
                "SyLA4ohGSq/mBZZYm65BeYhFHlartesxRtjKbM2d9dCpkQ9nGC" +
                "v3YCN5ZKZacTeCx30RY62NaSRzaF5i0dqkIs5DLPKwWq1djzGi" +
                "s959IXGfuT7U7L5ceCdcXvSvd8qnxq6PMxjJ19pEnmGtieA4bW" +
                "JPZwLxNtJz3OK+VoyuMwGNayjMlyiXMTiDDljkcd/w6IEfv8ra" +
                "9VjEvoKW26lrGscpijX/AZ0a+WDmbsVYuQc3SIsypSURYHFEI6" +
                "VX8wJLrE3XoDzEIg9hJDIeY4StzNKc/Bz8h2Vz/3Rq3Pun1qml" +
                "+Bzchm/k56iRD2cYK9k3S4ui0pIIsDjSnrN4JfNwHzfH2nQNyk" +
                "Ms8rBarV2PMcJWltbc2cCdPXrWmtJ+PUeEzGpNpZGaGxrXsLQh" +
                "qlxjvklKAat5dEY4hmokr6Uq8LwPOjXy4Qxj5R5cKy2KSksiwB" +
                "quxrdppOTQvMASa5OKOA+xyMNqtXY9xgjJbKlTnouhUyMfzjBW" +
                "7sE10qKotCQCrBRScmheYIm1SUWch1jkYbVaux5jhGS21KmV/W" +
                "no1MgHM7cbY+UeXCctypSWRIDFEY2UXs0LLLE2XYPyEIs8hJHI" +
                "eIwRtjJLc3OWOo/+eneyxhOZ2ZpPbmbb5yzo+dPseFGbra7a8Z" +
                "6Pt2v8Xmu5Ph+3tS/0vqD5HHRq5IOZm8GYRurMsA55NU4jpVfz" +
                "2tp0DcqTGvRo+TSat2nmMNJ+ATo18uEMYxqpM8M65NU4jZRezR" +
                "tjuJ7WxlY8Wj6N5m2aWUeaB6nz6HEX1jgTa34j1jy4sPdddX4c" +
                "tbUvVIW/Cm7izp5wFlo6Ujcr+jy8p7OpKo4xiZFKrUzK8NVvT6" +
                "up4g1jye+DH57vOm7gzvT3we+uv47XPqePUufRPxW+pV5uHS9X" +
                "nb/GcaK29rpqk2dYEzo18uEMY8O/07ekpedF9NsSr2Pakhya19" +
                "YmFXGerKxHy6fRvE0zm5qv5M6e0Mf+eB560llxDrSqOKEYzxWt" +
                "TNaux1BNFW8ca/8LOjXywcx9B2MaqTPDOuTVOI2UXs0bY6C7/b" +
                "IG5UkNerR85e/pAkQVcxhx/pe97j53l/w/oL5fz53/1Yr7YYkM" +
                "VkIXfJ/ofuAeEP8HNPwlbOr/gNzdRQ9+P+SMTxej/w/Iif8ycQ" +
                "+59wjrRyOuEdX/B/Sg+4Va+Y5n/tv37nfJw7PsuNxmld/121Gu" +
                "lcqqrgr5EgMzVhXnIp/0a4yuNI6m7ER2wh/d4fnkHuGY3JKVqG" +
                "5Gp7eOOE4nqqu6xzQGZqwqznWHQy0aoyvV1dR8BTuMvduG77tf" +
                "RldQj0PkYr+4aqq+9JNiUh1i+4eqKqWqhhEr1trFvXyuclTGNH" +
                "L4d9tm1TG/C9hV+U3Brup4iJFKrUz0yYi9D1W8YSx5P/5o5eq3" +
                "vH93+MhiP1dxv00dp+yys/c42doX+vyptRo7jLyOD/5m4xA53m" +
                "t6a7ZqtIZwlkKwYlIdHadVVZVSVcOIiv0fuihmuw==");
            
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
            final int compressedBytes = 2366;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWmmIHEUUXjW6ZnO4Wa8YFw8QRNSfuv5zZtJtjFFRs977R1" +
                "dFjU4i3hfuTJcOzoBBDAmo+CcYj3jn2njgGU88EQwoKpqIoLhq" +
                "PDa7Jtrd1a/fq7O7d3oanKHreO+r9733pqq6p2a6usSXswha93" +
                "7flePL+aut0Yuyadtjs73cd913g7p2eJynn1BHa+iZ7Oikte/c" +
                "7jTspldtQsQELfRKHcvZqFzEiJay+FRejleUpzGqE5Hml6i999" +
                "e0o+x6GUM91Y3kMqrRx2DjFXWO67hQQp/3mj1chgisESX2OF61" +
                "5386+6ujAE15VZtJXogxmNhEJho/jRm5aEx0xMiW5p5B7W0DSX" +
                "MvfZ5HfpnKuva22/Uju0POaRbEh6T9uX99ZcY23gtX65xkv0Z+" +
                "MGp21a4iq3DSnYQS+rzXnMtliMAaUWKP41V7ft2rjgI05VVtJn" +
                "khxmBiE5lo/DRm5KIxiSOiHTPex5v7qjmu75dm7tQ+iOZQfNep" +
                "97oz0oxsTjfMxoNso+ozlT14RvqZXnvHug5+9/7Qjory5DWalf" +
                "zupvVeZ0c747PmqT22VNnF+bQwz/nkTBaap8m855Mz6AxCCX3e" +
                "cx9xBt1HKAJrRIk9bkG159d/qKMATXlVm+CLyQsxBhObyEQyOk" +
                "hjBp/EmHhde92/XvOv52rvBfOpNhrNp3Otn8JHYbnJNp+I5GV3" +
                "z7Zm+Tqr9m1lf9Ky1TZnn0/KU9X9eEV5Oo/qRKTdThY5aO365v" +
                "kihnqqG8llVKOPwcar17lb3a3hWh8ACZuOOlpDz/C0b9A64/ZR" +
                "dqsyJmihV+pYzkblIka0NBWfcB93zkzex71qunXn7+O721l3rC" +
                "fjPr47r3089sDPC5sdto6U7nfkaZQdJOeJzdVam4l5Yr1sDsnT" +
                "vxYfDmWHpbnfsQM1Y4/geWJ9RDaPsrFZYSl9wuwAdnCaPLFDWD" +
                "+0Szv5FZQkT1fLYziOI23zSTuLZ9n1aJXa1yPQY/Baz2ayZLIq" +
                "a2RdaZxfQVlZAVJsibiScadRRxDPexL8HVdbJgR6DF7r2UrjKf" +
                "I0btbIunIvv4KyFp8lVJTdnuM4Upsnyz3L+SfhfturtkwI9Bi8" +
                "1rOZPdVblTWyrtzNr6DEPLGjlZEhrmw8casfa8nTRIK/3WrLhE" +
                "CPwWs9W7k7RZ66zRpZV5rgV1BWcD4pJ1gcVzJGXLGctbl7Jcz/" +
                "CbVlQqDH4LWerTSRYt1NmDWyrnwiv4KysiqOepWS4RDHkdo8rZ" +
                "r6nR+tmuxTOXgMXit3qWNslkxWZY2sK03yKyi9o+L73TVKhkNc" +
                "yfgdk1nWnbtvwuc6qbZMCPQYvNazlVJ8GzZj1EjLJ/ArKCsr49" +
                "mxUslwiONI7Xxa2cZ8OkFtkU/geFEOHoPXCv44kyUzr6qhukp/" +
                "pR9L3uK95s2VfhEh6oPSq9K+bBVaQds9AJDUmjhC5qAYvReyh1" +
                "CqbOifyKzj0/WdslP27w9hGd4pyvzt5+kuLkME1vD2qrQHFlR7" +
                "fv03xSFe5pU5AGH2guri8m+dHpnInbFMY0YuGpM4Ipqx8d7FBv" +
                "I7/WMnuQd27myRKWunE2zG86e7cz1/6iv0/Kkv7/MnNshOZaez" +
                "U+J+fErQ+CLz53qaUXM2O6etGbPQqj1DltxzREq7i6bqUf2p+L" +
                "mgmeesdXZ1FfjqPBu7KM5TKxmd4bxgjyLz1Dm28gN4RXm6j+pE" +
                "pN1OFjlo7XoZQz3VjeQyqtHHYOO1+0TOn5bn+gm3N8uHrdpL8m" +
                "XTclxee51drb3fPahBX5Xtftd8KL7f7V3o/W7vdPc7dmXW31vi" +
                "fRz3p2clm7cE5cjPRLIsxecQ7WDOTnZTinyEJxnsVo2dpYlMd0" +
                "b1zQGbX98e9pYouOvZjZLkNj+u38JW9J2WXcdu0OzGa8PyWe8x" +
                "Yd1tCaXPhOWaKIvy7wgbpP6j3vPk9+D43lnvdbX/HfGe8K8XJN" +
                "moBpf4e7D3NEGvp2ze4wmfjf334HXeRi3npXGetuV6p/690OeC" +
                "jrPheWZze677+MxCnws6zkbW3S+5fsJ/FjqfOs5WuUJt5Wm1mP" +
                "F5+p647lKcBmZ4Hp/Wjlf1asZ1N624dcfW5me1vqTN8RnzxJ7s" +
                "yFpbE1zwBhlIuZ4ixZGyHZBSLNqlo5BD1Ot8ozZEr5ANS53Mz/" +
                "ZqFWFjNmnE+dTSfJfM9j8M4f+Z09v5JDP/PzMD29T+n8nip77W" +
                "PrnuT7MLfS7oOBtbH+epO1fP9yk0Tx1gM537tqxzN/O57+pCvw" +
                "evTvc9OHndwWv+jvk7oIQ+77V6uAxKRGLLq9K+bJWOu/sbHI21" +
                "OELmoBhxnKinMfCy8b5sD/0TmeWYRF+off3zU73WmpHnrG18XO" +
                "S6a3yU+xP+gDMAJfR5rzWHyxCBNaLEHsejvVYfaBqfqqMATXlV" +
                "m0leiDFEbJ/o9DRijJ/GjFw0JnGEPJ/Yi13/21f9jo48Z45BWR" +
                "mr3wOylv/M4j1UGVORpj70ZFQgobLGZ1Qq6vS+eQ/bvBBjkC1i" +
                "O3zOHFNjTrXuFjuLoYR+JDuZyxCBNby9Ku2BBdWeX79BcYiXeW" +
                "UOQJi9EGMwsYlMNH4h5sWiL6Q/7Az7rbAMZcP87e8ti7kMEVjD" +
                "288T6YEF1Z5fb6I4xMu8MgcgzF5QXVxu0umRieRpmMaMXDQmcY" +
                "T4vcVrtFL8Vpr+edx5udDzp46xlVfgFT1nnkV1ItKcp/IKs30b" +
                "u10vY6inupFcRjX6GGy8os5Z4CyAEvrB259PZ3MZIrCOUVXaAw" +
                "uqPb/eQnGIl3llDkCYvRBjMLGJTDR+tEa5aExha8gZ8lthGcqG" +
                "+NufTxdwGSKwhrefJ9IDC6o9v36F4hAv88ocgDB7QXVx+YpOj0" +
                "wkT0M0ZuSiMYkjom9GO2F/Yl/mumO82rndaGRekWzK+dOFue7j" +
                "mwvdxzvApj8v8PenizWnLt9O9bzAeavI8wI9WzvnBc4yZxmU0O" +
                "e91qVchgisESX2OF6159cvqaMATXlVm0leiDGY2EQmGj+NGblo" +
                "TOIIZd0Ntz9XW5fF/mwodN1tKGrd+TEuyfOczhlNlddrclp3o+" +
                "nWHfsx7bpzv3a/5mXrWugHb39/qgYaisAa3mIfLMIYtOd7/qY6" +
                "CtCUV7WZ5AXVQamyiUw0frRGuWhM4ghl3S3NdSVsLHTd5c/2H7" +
                "V/YKw=");
            
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
            final int compressedBytes = 2204;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW91rHFUUX9SW1qqtta1YhX6EvNhqqnkoRGmTnVm1Cv4Bvi" +
                "mC6ENAKgi24kw22107LYoiVPyA0ActQpEKflNTRUupVq3S+FGx" +
                "xqClNTbWxDZCqzNz5uw55947k0327kBm2DP3nPO7v3Pu3XvvzN" +
                "xNCgX/QKHgD4afN/1DoVzhv1sIj3I18AsZh38klu8YfYc1y4fu" +
                "YKGJw38r0/uZajFH898zWg82mMMBoa2ox9oz41Yd1m39k830U/" +
                "9YpndC66c9jfVT//j0+kkfT4VCULE6nvbnOp72N9JP/f81Pp7c" +
                "ue5clKiDFtTARgi64lnu5Roy6Hzh9X2OI7waV42BiPQsZBvSos" +
                "lIvP28zRSLt0nWSOot0Es2DvdAIcejFdHS5l31R5vzrskcpznv" +
                "tq1sbH2qLG58fVK46ut45ZrCrD2qP7U6Qt/6ej8ts5r5iVz76Y" +
                "htRmfIGUKJOmju62AjBF0JJTXA63ycmbMB2uwlLTsL2QbKXffz" +
                "FlP7eZvVTJk+7AyLfov1UC4Jz+H+gagMdixxC+CTGsMqi+Bdoq" +
                "I1xDDFAD9hqj+rvDIfnhFEU3OMZN/uzBEznGbTcxXzbrnNcVv8" +
                "Ks95Zz+aM+KMoEQdtGC7M1Ic4gi64lnu5Roy6Hxh5kMcR3g1rh" +
                "oDEFDbnIVsQ1q0SBaHqMXUft5mzEm2Ca5pzwXFp2fvc4E592be" +
                "75wBZwAl6qAFO8FGCLriGY4npiEDoalUeo3juF/GVWPIDBtDmK" +
                "JhXrytMlv0q+xqjaTeXiwFQboPj3Jvllfcqc82sTbsna6/+ld+" +
                "z+PBczbnXWkgz3lnjtbcvkrq/W6lzW+j9Eae97vWR2P9tNpq5k" +
                "3dqStteUYzzu19zr7iGZDhfSIugRa84OwrvwQWwCGSTqlHDBES" +
                "rcgfWTiyOs6tkUQcj8UjlF9Oz0K2QY9GLejbTRrkyrMFDOUs25" +
                "D+Hhw8O3Uv83V8im/4aK7zrgXR0vZ9g102931L3+S572uO1sy+" +
                "r7vWXQtXrkef4BXuQau0AF4ycFZ2pz7H0aZakSV4lbPqLGTBfJ" +
                "BNclX/UXOU+Zl7Ir1vdF/fA/XvJGXeVW5txUyp3DIlYg0r3xR+" +
                "OjPmXZx75eYG4t6Y6ukQfdbmtsGV69Gnep570CotgJcMnFX7ht" +
                "qQX69FkdGvs5AFscgmucrfqznK/My5mbLFq/eJ97Fcn7zwnuod" +
                "C+8I6734vdGL93+9X8PP74Dx4mdrbzyWk37BvyyuOY+xXOkbd0" +
                "O9E94v3nB4HfFOaqvHpf6cUMa78v5Cf5l/rTfoKfvc3lEvWXm8" +
                "78JPfdfSOxXL8/X3u/X+5THP0th+0Ps8lF9m95NnWAP9S2J5lb" +
                "/Y7XDD8QUy7rsOOMPxNAk2QtCVUFIDvM7HmTkboM1e0rKz4D6U" +
                "5eMmP0ViY6aDt1nNlMdOW5/cL6Zenxp/Lsh3fapesL0+lSZL8S" +
                "+17iquRx/3a/CQLE0ihktkKE3qrNr6Oon8pUmvh8flkRGns5Cl" +
                "nmVY31uuZqDmzrNPXfs1v7uK6rvr3HWhJZaxbx2cYekI2AhBV4" +
                "YSGuB1Ps7M2QBt9pKWnQX3oaxeNPkpEuuLdaLNSqY8tnm/wFnk" +
                "LLK5X5DNZnu/wBzN5n6Bf2cSaZOzyep7pFW2/KM5h5xDKFEHra" +
                "cLbISgK6GkBnhC6yXJBmizl7TsLGQbKHfdz1tM7edtVjPlsVP3" +
                "VW63+X24p/IcT62IlvYe3HNb8+/Btbl2cpzue7A592beg51RZx" +
                "Ql6qDV5oONEHQllNQAr/NxZs4GaLOXtOwsZBuS3OeZ/LzF1H7e" +
                "ZjVTHjt13nVbnQl/5DrvrEfrHu0eJQml6HS6nK7uUYmIrOiXdi" +
                "g5Xcl31AUWrB2VqSbVUvOQXomR9aRfzccUDVpHaOLlWOLT+ZXx" +
                "VP9FJ9hdmLVHpafBXwPGZxzBTcbEBmeDYTXbkLnWbUhHZde0/r" +
                "TTYLTKHTPNrFJ/Qgvetrk/3uT4uHua+LsaxN3T8Po01j1GEkrR" +
                "6Wx0NnaPSURkRb+0Q8nZmHybG8GCtaMy1aRaah7SKzGynvSr+Z" +
                "iiQesITbwcS3yafqb7TFhKJJSi01njrOmOf7shRGRFv7RDyUl2" +
                "PgCV5MHYZF1EcJzk5BhZT/rVfDADNVPORLwcS3w6f9r+U+Veq3" +
                "fq07k+F1iP1tPZ00kSStFZrjqlnk6JCK270C/tUCq/mIynEliw" +
                "dlQGG6/b06nmIb0SI+tJv5qPKRq0jtDEy7HEp/Mnu8OfJitz/W" +
                "+oiik7jN6fM1qJH5xid/riVAwe21/1vg0/x9OxkLt/9dR5eb+l" +
                "ei74D2c8UdDz+EM2x21tWZ7zrgV/7zvhTKBEPT7bw3Mi/jvWOi" +
                "K2TvCT4WNvwtCu8bXzmmgFvxJX4QREbamsp+egyHaTH/6OlXLj" +
                "2VIslZ3XKG0ubY4kfcLnp4+U/eMEQwiQdNWPYDDCR/7a8nQUcU" +
                "hePTp6kVXWovxq18lMzUw6GzGhrYF594jNcVucn+e8a3200gf1" +
                "sWD1f2mKC3Ltp5ZHK9V/Na1dP3vfg/vntPxN6tH6eDpm8/2udk" +
                "Oe/bTtvlZHYOvTY1afkINcn8eDvPqpXK08bjXznbn2U8ujVZ6o" +
                "99MWq5nXcu2nlkfzn8F+Cn6wuT65T+baTy2Phr9Lhf10cqb9pP" +
                "9u3rfIfaqZrMqZbz19V2j9NI1ozf7fa2D4rbBv4Yz7aWuu/bS1" +
                "5f1E8+601Xm3I9d5tyPHeTdqr5/8YddrKqt/p9lPXsv7icbTtP" +
                "eZ/OczMt+e63iyHq24uriaJJRAC84WV0uE9Eey3Mt1lRVLUdn1" +
                "EcnZZA01BseYs1AzRKlHo/xkZFM8k54xnv62Op6quY4n69Hc+9" +
                "37UaIenWE/nQMbIehaR/VyDRl0vvBa4TjCq3HVGIhIz0K2IS2a" +
                "jMTbT2w8Fm+TrKE9F5y3er/bkut4sh/tfwpwx9A=");
            
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
            final int compressedBytes = 1334;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW82LFEcU70NOQUguevDgImyIB1G87EGR7e6dgbiC4gfowV" +
                "tyCyyEZVkQSeid7tkOwnrxvAiKX6Auq6KCh3gw2RDITWR1A2Gj" +
                "kP8gZJJAprd8U/WqXvd0T1fVVnfTr+vV+9V7v36+qu7pbT0veu" +
                "F50Q/9YyX6uS/Hoqdef+ssXv7bU7b4d2hFv27JJx6xRb8oPc9b" +
                "33s1tuhhofVHuYeOFj0je38qw6A105oBCXq29/PUY30cwc+wYx" +
                "08qP7651gdBWgxrupzGAt8DXnRcCTx+rk3MZZ4TXjEh+yOQevy" +
                "P57GrdXxLG7mo0GeOouamXet5slAtLz1icIm10Zen5asrk9L5d" +
                "anZLns+mStnhas1pP2aOF0OM0la2V7lqdMFxGiHffDaOwVWlm7" +
                "9S0gRW94hBxDxNAsZIYg1WicH45MxaN0pZ6WoZWOeY3dOhva62" +
                "lnuJNL1mIa00UEtqu67BVaWTv4F5CiNzxCjiFiaBYyQ5DpuOyP" +
                "88ORqXiUnl9PeregZ7OeFh+ZjpDcMJSn/2zmKf1Mt8f2pfYlLl" +
                "mLaennmRQR2K7qsldocT/Yi8xDjiFiaBYyQ5DxQdkf54cjU/Eo" +
                "XZl31wcerjZ3HTfPnf9uCfc0N0/6uSd3Jf3OYI7vM74W3jblOT" +
                "6kvUKDdsAlazEt/C6TIgLbVV32Ci3uB3uRecgxRAzNQmYIMuOu" +
                "MpW5MU2NR+nKvLsyqN1TDZ53xrnzPKUHmpuneNJensL9Da6n/R" +
                "br6WCD6ym0l6cR7gnjo9kMPD+N68XpzVM8XfBLYsJmnrrXSjI+" +
                "vi31tHc0m4F62qsXV/Q8PgK73bleN/NtpVj1dDEZDVd4vzva4P" +
                "udce5Cno40OE9H7OUpPdzg54LzFuvpXIPr6ZzFemrwvIsvuPWc" +
                "2Zkpi0x9m3lKJ93KUwXmodU8BfbyFKxVZndMqf+YnBVRZc9fVM" +
                "NX514jT7/pnHd2tzLct2/ehRedud9ddDlP/n1X8mSeSa08vXMm" +
                "T++cztOmM3natJsnf1WKv1rIbtUUq/ZfFfO06nI9hfPOrOPzTs" +
                "+7u87MOwNM6O8zh44ivs/0H3ywEd9n1uRY8ftMYCLhanw/rnXe" +
                "zTkz7+ZcnnfpSVfylJ5wen2658z6dM9untLT0r9T4d/t/ZvO5M" +
                "k4k+K/tyx8Usjuj7x1fOHTeqw6uwpZ7chjUuqKNazj6Vmpns4U" +
                "5mndmXpatzvvKrK75UyebjmdpxVn8rTicp7c2YKuXlxRnoKXDc" +
                "7TS3v1lJ5vbp7iVPtMnvVnuWQtpjFdRGC7qsteoZW1g48AKXrD" +
                "I+QYIoZmITMEqUbj/HBkKh6l21qf0q+s/m750oTXqY+zA3boy1" +
                "rJK2bDSDxS9gO9GIeRYi+OS3PDPmCcyAFLqg+j+Tk/cp5l+PN4" +
                "jflt9f8BmYim7/1T+LUr75+AybD3T/GfVX63hL3sgB36WIvZMB" +
                "KPlP1AL8aFPSquGLmIG/YB40QOWFJ9/d+B11VEUeSw4Cu+5C3C" +
                "vtf25uy9udHJm3L4ulczNZEdsEMfazEbRuKRsh/oxbipCSquGL" +
                "mIG/YB40QOWFJ9GM3P+ZGxxV+Dg8sSK+Ual4O78TeiDaP9Wl9G" +
                "FI9WrfT7TNpLWWb61vGBjVjH01mb6/jiWLl1vMr7J/81HFxuze" +
                "Zk2CiQg/mfiDYVXaOeXlez0txpL+W4tTfaGyBBB629ET4WEfzM" +
                "UVhjo1R/omfRG0PTVq71r/txPgt8DUwyvGzPrkbkJrLlsdRrYu" +
                "e8eRd2Kz8/dZ15fupqn3f/A4EjYGU=");
            
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
            final int compressedBytes = 1515;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWs2PFEUUn4OSiCRiSEx0jXjw7tnEQ/f0bDzxFxgCET1t4k" +
                "E8yHJhZnZ2ew5qAheFveByIUEhwLoCA7sLsoYl0btuSDRqvBFJ" +
                "5GD86o99vqquV9WvurprZ3ryXk+/r9+v31TXVE9PqyVvgx/Eo/" +
                "bV1lhsZh6D73nxbmfTXU9kLZFL3XuJ3t/9KrfHXWPWt5leIX33" +
                "FcvIrU/994xcNpT4LTLuGmn9hv1pnU0FXmDL3+U+OVLOLNYBqx" +
                "wnR4pWGZfmJteAPJGDrCmbHI17PbLsCb4EQZ3FheUdhtjyOG5k" +
                "FRzVS3N35LACgjrDeoOXy8PgRlbBUb00dzcOuvmJlctE7o7iOS" +
                "eOV+zmp/h5/vzE/qy+BkGdYc3zcjlWrFqdo413btWFrQblDgjq" +
                "rE8f8nI5VqxasU937Lz09x2XrQblJgjqdAvf5+VyrFi1Yp9u2n" +
                "lp7ly2GpQNENTZePqOl8uxYtWKfdqw8w52u7DVoKyBoGbzX+NZ" +
                "7apycfTe8G1+HJdZcAsENZv/LZ7VrioXR+8Nj/DjuMzqXxdQ9y" +
                "3xRz7XBQvP1n/f4qlPpya9T8FtENTs6+E2z2pXlYuj94Zv8eO4" +
                "zIIbIKjZ/G/wrHZVuTh6b/wLM+7XstpC9joIajb/dZ7VrioXR+" +
                "8ND/HjuMyCVRDUbP6rPKtdVS6O3hu+w4/jMguug6DOarzCyzVZ" +
                "B4fAIkYODlr36bqdl+ZexraEwwgEdYb1Ei+XY8WqFcfTyM5Lc+" +
                "ey1aDcBUGdzXF/8HI5VqxasU937byDwy5sOZv8HKFs67/bGoNN" +
                "fY4Q9+tHCe6BoOZlgaZ9soVX1YzG98Z/8atwmVVbj9s+bwmnfK" +
                "7H40c7e9/S/8R039L/NN9HJ9Q+qbYm+0SjqX1K47jMgisgqNOt" +
                "fYCXy8PgRlbBUb00dzcOfubxtsPv42W5xPNgJlq8ZMslOoCClq" +
                "IN7er7okWfpeakLzO3PArjsSKVidxlXWRjwjVzamo8RW/6XBdw" +
                "0eJli2v/PghqXhZo2hfOihZeVXoLZ83ZqlfENsXprUTcJgjqdJ" +
                "v+oCwLNO0Ta2DVivP4pp2X5l7GdizWTy96XT9ZPA/m/18lClOB" +
                "F9jyd7lPXD9FoZwpr58wXvZFIYUrIpu4iYwwT6wsa8omR+Nejy" +
                "x7/Py/YHrF53ii0Rz/X3AVBHWGNcvLNW1QA6tW52jjpbk7clgG" +
                "QZ1hHePlGvt0DOKCZVeONl6auxuH5ErMBF5gS98NHkXbfjFSzm" +
                "wp3kjxRYVI0Srj0tzkGpHEIyJ0pPGoESbkyHSt7/9/DXJ0Un5/" +
                "IlZcR+uvqZvHp4+7rwugRnc0eOx1Hj/Om8fnfrNZF4jb/BPN3L" +
                "d09vocT82jze8ax+so/tnyujtcb5xxfnq1vvHUm3HrU9/yKSmH" +
                "u+UIXewsgoZjOAINEbjHKPkIsor1khHxr5qFGGKdYs0yFvI5bK" +
                "P9Q/nFM8bzF88ZseQ+iBnb4+njZq6c6Ws+r9Mm0PzcBw+f9Pl9" +
                "t/C4iftgen6q9bP4qbPllP+n5Wyy1fSItesTdx5P+vTAa58ejF" +
                "efuFtvZviUz/lpYc9k9ikZTye9jqeTk3rdhZ/77JMbGmebu9zQ" +
                "nel5r/fB53d2PHX/rnrdhRebG08nXlD6dHG8rjt+n4ZP+7zuhr" +
                "t3tk+9Z1g1lHVmb2/4hdP93XNGVsq3mw3aeK0zwwte5/ELTZxF" +
                "+yHo9kO4v8tt/TP5Xo7UHcNRMSq1iLbhHtEq+2hu/UUTC/kcih" +
                "XxfW+J4mhCN3xyDd0Hd373+X3XPFpj68xzXteZ5yazT72Zzlmn" +
                "edzydzo3NLLi6c5p0HAMR6AhAvcYJR9BVrFesv9MzUIMsU6xZh" +
                "kL+Rx0aDKSeP7iOSOW3AcxozieOpt1jie3fOvxtOnruov2RfuS" +
                "b6bX65qf0mr+5icarbfUQJ+moqnkbq+26zxy+l/08LU60Nz65O" +
                "d3X5fnnAsv2/7uOwzIuBqec4Y/olA++kj9XYXKN9nBa/YXY0Sm" +
                "esaihz4HE27B9x/0BtQL");
            
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
            final int compressedBytes = 1708;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW01oJEUU7qOHBUXEiwl7WAIShBwEQRTc1HQHD4o/MQlGkR" +
                "Vd9yIBwYt40PnJJO2aXS8a18iieMme1l2FKIiYi7IurCeRvRhW" +
                "A+seFdmDbMTufnnzXlW96emeqa5MYjf7pl69977v1ZvqmpqabB" +
                "DUN4Og/l3y7/P6pUQern8VFLjqVzK5IdouWz3fqCvBAFf9i1zr" +
                "92aPzFb/Wuz9oUgGakktkYQWaKBzD92eyuYC101UbBGOjmLmYX" +
                "J0Ije7ZWFm2JGbJh7lx5njR0Q+QY9Wo9UgAJleaQu1aFVtoA17" +
                "0S7piGDjJVwbdhR6c14bk6LlLHiGKG02HA3PjWdLXPaY9IjdWX" +
                "g4qOSKVeDxWnI+ivbDZdan9kP9rk/tB32uT/Hjrten6Gp0lSS0" +
                "QAOde3C7pCMixmB02lYn7Sji4DgmZq8szDHIbDoTH789Bkm3qn" +
                "s2d/b1/fTEoc/nLq5VzVD/sxrcaN1nndyzRSvRCklogQY699Dt" +
                "qWwucN1ExVbaXtxCT46mR5gc3EfOwswQZfyYiUf56cwSn6T7mk" +
                "9+r+gdt37CCjR1IOrUduuXfDIcU8dIQgs00LmHbrd1ExVbhKOj" +
                "mHmYHNxHzsLMEGU8beJRfhw1fkbik3Trubt5IObTSbd+WWW8fA" +
                "8ecDf8aLl9ZvRusX1m6ldsnxmeD8+jRB01lOhBr+Slaxhl4nFk" +
                "joYckpW0/Cz0MYCMliU7H/FunZY5GufS60AR9ve7eN7hruy3Ae" +
                "P/Kee/fJvrJzmcCqdQog5a/Dz0kQe9kpeugb+Nx5E5GnjLVtLy" +
                "s9DHsJv7c5Kdj5jGz8dsZsq5jZXgqYOwjrefKOj3dOFPhlvRLZ" +
                "Sog6buhD7yoFfy0jXwt/E4MkcDb9lKWn4W+hgod9vOR0zj52M2" +
                "M2X6TrSTtDKZ9e3AnXDdBX3kQa/kpWvgb+NxZI4G3rKVtPwsuA" +
                "1lmrttJyZWpx0+ZjNTzu3jnK5xR3hjkPjm3bnoh6xV90bVT3Zl" +
                "dbrutU7X/dVJ3e+yToPFl62Ty9z7mU+N2wthXBbm0xte51MJtm" +
                "L7cSvqNLzWJmoTQdD6VLemfd0vtNpegDbA+eQL5fxltsZnxX33" +
                "an2Kj/ucT0uHnO/Ht8ItlKind+1I7Ui4Fb/IPdJetMNN/mAFBP" +
                "DieIBmRoFd5zUxwaN5Qo+zc9ClzZZKmE+UG8+WuEx0M6LK+eT7" +
                "il+tYOXucq4y+ZPA/2a5c5VWC89VFtm5VqteOseSv99JuUvnKq" +
                "0/Cp+rXAuvoUQdNZToQa/kxbVoDKNMvGRPO2ZHEQfH0TF7Z6GP" +
                "oRubztTZjY9xNM6l1yG5Z8KZpJXJrG8GbmhjD/aTXdKjUYwy8Z" +
                "KMRu0o4uA4OmbvLLgNpc2mM3XqNMrROJdeh+SeC+eSViazvjm4" +
                "oY092E92SY9GMMrESzIasaOIg+PomL2z4DaUNpvO1KnTCEfjXH" +
                "odkns2nE1amcz6ZuGGNvZgP9klPRrHKBMvyWjcjiIOjqNj9s6C" +
                "21DabDpTp07jHI1z6XWgiM5e5J5S+5qFwr/rvOzz8849m5pQEy" +
                "ShBRro3EO327qJiq3M+hJ6cjQ9wuTgPnIWZoYdabFRfjqzxCfp" +
                "3eeTmnf6fsx7nU/O2cJaWEOJOmjqWegjD3olL10DfxsP0Mwo9O" +
                "a8NmavLPQxdGNLZfw6IdH4+ZiJi49JjzD345OXBj0vaN7cq3MV" +
                "Kff+zguafzX/zjsvSLh+FPbjb+d+W2hY+92W5Fd+P172knJP8n" +
                "trr84zi37e+f79bvFXf+d0k784RPVcJ5e5uz9/Oro+LHU6uj7U" +
                "dTo3NHU6569O8cr+fe5ay8M8n/LqFH7ps06Dse1pnS56rdPFCs" +
                "bg5e/EwgsD5VjyPFNmG+T/I/iaT57X8X/d+vmrU/yBzzotVf5r" +
                "SFW/B6stn9/vyrAN13MXn/E6n+7dr5938Ude6/RA5XU6zbX2fa" +
                "5w1c+DRLd/d8Em/33BkM2ns/+ndXy/7AvUa/t2ffrkoM4n1Si9" +
                "BjWGZj41XNdFTatpktACLWtvcw/dbusmKrYy6zZ6cjQ9wuRgKN" +
                "tyFp08pw1pseFoeG6g2XySbr1znbN/tRYE8celar7Wn63APvNE" +
                "yXd/rerPO/W+3SofK3yDPx54vNyzqVPqFElo7Wq1VHIPw27pJi" +
                "q2CEdHMfMwObiPnIWZYUfWTDzKT2eW+CTdqtt7divneViQY4V3" +
                "+BWv86lyNnXGbpWPFTJ/0mudKmfTv7eUm0+5NfzWZ52KsvWflf" +
                "rQbpWPta/4gs86uf+76OA/xxXEzA==");
            
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
            final int compressedBytes = 1946;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHFUQHiJ4MviDPyQ5LCToIYm57UIuSaZ7RhJjEgUDq6" +
                "CiEpKg7FEUkkNnfpaZPUhuegk5GCGwLKKIBgXXKCsmmGBU9BA8" +
                "BIJeklPwD/zpntra+nnVva+nu8fddDM1Va+qvq/em/d6XvfO1m" +
                "rR57VaNB+/3ou+juVY9HEt9eicRi26NJAfWVHRRafl02C+NvQR" +
                "zEcfZPmjBTfDjDunW7pH49avhqnpxKN5oltT3n1dKDBOC1Vl9D" +
                "4ctqbKxulCgXG6UH3GckfzZPMkSdDAAptHSL9ra1TUEr13DiM5" +
                "mszQHDzGrkJXiHJ6TONRfZLZ4rPs6Ee1hr/LO9LRT84se8ONCl" +
                "8yMq/k5vreL85iM9fdJ3G1r3vN0A3BBpKggQU2j5B+19aoqCV6" +
                "8xpGcjSZoTl4jF2FrhCly0b1SWaLz7LzzaewlTWf0GtFmZnVza" +
                "eWf5xfbCNoBCjRRgslRjSCcAb9GMWtcAbiIYrjhTM8E1uJg+NI" +
                "TKsKN0JLl00yLY3TDO8T55LjEJ+TjclYG8hB2yScoGMLtocd9G" +
                "MUt8IOxEMUx4sr6rhZxMFxJKZVhRuhpcsmmZbGqcP7xLnkODQm" +
                "g+PB8XgFLkrQwAKbfMnZeov8vB201tuL7BEioB/beC5G8DiJyW" +
                "NknvTreiw27A1aVCmPJTzHPhYci7VFCRpYYJOPv6PWmuI26/Ux" +
                "tNAbPo+RHE1maA4eY1ehK0TpslF9HLV33uKz7PBqeFVc2QY2tG" +
                "mJkdTSmkriwce9GjVueQbR0e9ECGY8iZVi3Hp4pcAma9c45jX9" +
                "alqbW2tyfyc+5Tm1i5jL3GPMpUUFc9mZy+5e5sqIt+ryxQ4OBA" +
                "dIggYW2DxC+l1bo6KW6L0FjORoMkNz8Bi7Cl0hyukxjUf1SWaL" +
                "z7Kz51N5R/hCbYRH9WzRm+JTnlWzbzZzbs6mRQWz2ZnLzvrZMu" +
                "Ktunyxg83BZpKggQU2j5B+19aoqCV67zJGcjSZoTl4jF2FrhDl" +
                "9CWNR/VJZovPsrPnU3lH79tRrrsTP5SN2H66vVvY+ws8o9mUyv" +
                "JUus+ryj1Z3u4p/0qGi4Nxaj/RfqyMcWo/nj5OjbXVjVN7n3PX" +
                "6snW3us9B7bASXaB+bQli6XQ08Qt+by+bP5VNbfCSXaB3mzNYi" +
                "k0TlvzeX3Z/Ksqdn3yfT4er7t7q1t3xtOiQmxVX5+a6zKu4+tG" +
                "eX3yZRu+KtoX1I/k/jbeV1shR/7al91nbg+2kwQNrN4vieQR0u" +
                "/aGhU1wpEoug7NwWPsKnSFKKdf1XhUn2S2+Cy7zH3mCppP95cb" +
                "Z/T1ySWMbcau+vd8aB3zfrTzXOXjtK30dXcwOEgSNLDqmxLJI6" +
                "TftTUqaoQjUXQdmoPH2FXoClEmtbuV6trAcvksO2M+bTTm01+r" +
                "ZD5tNO8x/yz0JCXl9yrN14wenl7Kyvl7lYJPe3L+XsWq3fq9Su" +
                "eU/+9VRvO7nv7aUY7T9F1+45T/dz31G/SiFq25loVhxfJ2F6F+" +
                "Ix2VkCUaWlYmtKVzUm5Wb0zka/SyfGlWdqx/VrZfx/BK0yvmHr" +
                "sPWbzal2fdtY8Ou+6CO0a57mw2d921j+RedzfpRS1acy0Lw4rl" +
                "7S5C/WY6KiFLNLSsTGhL56TcrN5IX575xLJW9PddPfCbT0lcFd" +
                "93BdbdmpGuuzXlr7vR7AsK7Rpv5Z5Pt8rfPymsiv7OWfAp2cuZ" +
                "3hedcdrp+QnszDEyt+P1aYfn9WlH8XXXv2f13rf07y57P9690j" +
                "3fnY/mu5dhnLpf+s2nTEznb5rdhYKIX2R6v3HueP/xQr3Y+zeW" +
                "l4Z6/vTsSrw+9f7OeeXfVW5cHNmqt0iCBhbYPEL6XVujokY4Ek" +
                "XXoTl4jF2FrnBJhhqP6hOoocVn2cXmU47/27hzlPOverbuz0vX" +
                "wvtqq/aY/qxqhhNL//HQ/GP1jlP1tdM+s7m3xNF/pWD+VM5x2l" +
                "v6OkvdFzT3DI35v+8L/GrvDvZ5w+0L2HwqYe72H7h91l3qfvyh" +
                "1Xvf0n/Q83nBr8PeBweHXa2E7+nDo80vs/Zl193+8lBHfh3fX/" +
                "a4BIeCQyRBA6u5L5E8gvstGxExB7MJR2ZhtO0lK7sK3Qes3fXz" +
                "HlP/3T5Y9uD9bPLCczH/LLaCH1u5JXXyQiuPJVyeRRzS72KS5H" +
                "kcTUqrTdZG7+nM3NO43riOEm2wmruhjSLonaKkBfEuHkfmaBBt" +
                "e8nKrkL2gWp3/bzH1H/eZ10p51bXpwK/6+lm/P2+v36k9y2lP5" +
                "UNoiDadQYlaGAlvl1noAXiuJ9nop3kJAfmID7hwNm8Qa1JNHBJ" +
                "ZF4HIeoqgEX2QbNRLrCBhbVKNMLTtrOjeL+aT7g/tsrn03gwTh" +
                "I0sMDmEdLv2hoVtURvvoORHE1maA4eY1ehK0TpslF9ktnis2zn" +
                "+vRbRXcSZ0Z631I6W2O8MY4SbbRQYgS949ma4hYiuHhx5e/yOM" +
                "3BcSXH8lXIPqSxSSbef95n4pLjEJ8TjYlYG8hB2wScoGMLtpPf" +
                "shHBxYuvTw+7WcTBcTTmclVwH8rpMctPTGycJnifiUuOQ/Ju3w" +
                "eH68P4m7z/SFn3wWGhfUHe+2Cbzb0PHrT63Qf/B/hQhFA=");
            
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
            final int compressedBytes = 2224;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW01oVFcUflLBkH0W7SZQ3KY0ILRuNPPeDOimlFaoFBIrQq" +
                "UUshTFLhpHTZyNi27FlBYKpYLBKFbbhUWoP5HWn5UbN6K4MrhJ" +
                "NoX0vnvmzPnOvWdeZjLzhqR9F8+755zvfue7xzdvZt5oksz8kS" +
                "Qzt9yfhZl7zo7O/Jp0cMz85e11M7cURX5PnyQ9HDOLhdk/w4hd" +
                "beaGGb2zEUWnzqp6N5I+Hb0xdbvaxveiId2b7hVLM/LyeeM9RO" +
                "h87IesPPPZR4xENr0irIEYWwXr1Hqsan43Y1obeXE906+lNTdr" +
                "Wpo1vUZuJYdnXCM+VK+xx1lBIpteEdZAjK0iVNiyjZBP9OnKVj" +
                "3LL37d9e+o/ZAM8Cij2mDu47VLg7yP29V6u4/rPs0Ol9SnRwPt" +
                "06N+9ymdSqfE0ow88hGh87mtT6MfsvIsn9d+ZiSy6RVhDcTYKk" +
                "KFbONqok9XtupZftTdFZ5V/16/y/XpZFMenWjvknFfdR9b9pux" +
                "hxQThJwBpTzCx3zIjGyEtrPiFavQexDtcR53LPtXew6Ugj9ZnX" +
                "Qzb31skoabPaaYIOTMoz6NHjPEfMiMbIS2s+IVq8Bcyz628lIJ" +
                "+jSp9hwoBf9Q9ZCbeetjh2i42ROKCULOgFIe4WM+ZEY2QttZ8Y" +
                "pVYK5ln1h5qQR9OqT2HCgFf6rq7lFkfWyKRpI0PqSYIOQsKO0R" +
                "PuZDZmQjtJ0Vr1gF5tjODVl5qQR9msI9h0rB31/d72be+th+Gk" +
                "lS2U0xQchZUNojfMyHzMhGaDsrXrEKzLHNtcd5qQR92o97DpWK" +
                "X1urrbn3UW/9J481GjTnCMclb/nMEPMhM7JxDSsrXrEKzLE996" +
                "mVl0rcpcZuZMNaug+1texB9iBJyOZHPmOPLSPkLCjt8SrkoFlj" +
                "T7xKaiBPyLmeCsyxnR218lJJridkw1q6D24sZe7TM1kfW2qOMT" +
                "eWGns514ou4QC8zzYZxjgL68biVZQP6gachKgf1etiDYEds/JS" +
                "CfoEe2KMZg9XND9njjbX78x2xp+2rFicjVE2W3lHN9U2piy7ub" +
                "FcnNV+OlxiVyJdXK1Y8WZ8XpD2prHb575J2c99+XXX7yMdGuTr" +
                "roxq/bueaqMFz1VGB3k92dXC6+n0Wo7b2POn0l53t3u4Om53/b" +
                "q73dnrbvbdzdanZKD3p8qtzvqU4zZXn7LPB9knu5pxPb1fdp+a" +
                "a693iBv09bTY4fW0uGFF55u/u8zM7k22yPHtO9GvRt9s1d9bBn" +
                "w9Xevwerq24d/NW7/oVB4mW/YoX/upHn5na3y8Wfo091Hff0fY" +
                "Ud3Bln3yKksUE4ScBaU9wsd8yIxshLaz4hWr0HsQ7XEedyz7xz" +
                "2HSrF2u/tT5dkWvj896/f3u+r26vbYp1hoGUlzjsusPat7TR5A" +
                "tLVKV+bRTi1jtRbGnRvW2lF9+060i3klQ1X3nZGsjw3RoDlHOC" +
                "55y0+P8aqQD5mRjWtYWfGKVWCObXrMyksl6MQQ7jlUKn72PHuu" +
                "Psl6n2KhZaSO84rseXpSsxhPigAdInRlHhYm1oOK/Pe7k6FGzL" +
                "Z5ivW8XYzWF31+mvukm/vTxE+b5f7ESvp3f8peZi/Zss9e9nLu" +
                "ACLkLCj0Jp7SqpgPmZGN0HZWPK3TQoR24qmVxx3L/nHPoVLwX2" +
                "Qv3MxbH3tBg+Yc4bjkLT89watCPmRGNq5hZcUrVoE5tukJKy+V" +
                "oE8vcM+hUqwdXF+XC669y4VX5uWJS0kpR3FdK99/JdnV7Cpb9t" +
                "ljywg5Cwq99DivQg4902xcw8qiV6QiRrjr6biVF224f9xzqBQ7" +
                "EPRtoaCnC4UdX5j4paTraaHbfBlK/pPPC97q8HnBWx2/3y1mi2" +
                "zZZ48tI+QsKO3xKuTQM83GNawsekUqYoTb/zYrL9pafdqGbLhj" +
                "3Ycs+nsq63epiX8G+byg/9WqY9UxOqOPMbGCQRsyICt8vzuIaG" +
                "uVrszDVit6mE1zzR0JNWp9difa98aN8eq4m3nrY+M0aJ6+4hxH" +
                "OW/5zBDzuXegV/EqRmPdmFNW2ypQIdu4Gu8GtaFaqRXvyc9Gqi" +
                "Oqh97P/6Tz1ZHGFM3JMlIihOdZOq9Z1C9q84iWVVZlzgumfjTk" +
                "1XpQEVULNWK2zfU00i6Wr8/uZ/fdncpbf8+6T8PVukAxQchZUO" +
                "ilFwgvaJnlbOEqRmPdkJMR7VVgjm1cTVeC97H7uGfUHOzhTube" +
                "F8n62B0aNM//v5Qg5Cwo7dEqQcss/S1exWisG3MKj60Cc2zjar" +
                "k99aMwtfp0B/eMmoM9vM5eu5m3PvaaBs19n1oIOQtKe7Qq5nPK" +
                "b8arGI11Y07GtVOBObZxtdw2+9TShmrDWroPbixny27mrY8t03" +
                "C16hQThJwFhV5aJ3zMh8zIRmg7K16xCsyxzbXHeakEfVrGPYdK" +
                "sXbwS/Lx1j1sz/qfKjbr/9tofNn3T2RvJt6IpRl56b7cIkLnYz" +
                "9k5ZnwaJZQR1gDMbaKUCHbXHusNNRGXlzP8iu7Krvc5/empRl5" +
                "5EsOz7hGfPjetIs9zja+ZiSy6RVhDcTYKkKFbGdHQz7Rpytb9S" +
                "y//ffgxsX46jvz/db4Hnzmi86+B5+5OJh/d9j5/Sm9u/EupXdP" +
                "HynKnz7cz2r/q+cqUx0+V5nq/d8/VYbL6lNjvvQ+DXfWp7Nfbf" +
                "jfq7wp6XV3r4fX3b3yV2yWPmWHB/n5qf/VKuOVcbE0I498ROh8" +
                "buvT6IesPMvn2WeMRDa9IqyBGFtFqJBtXE306cpWPcvPVrNVx+" +
                "ut/5tYpUFzjnBc8jTq0+gxQ8znzgcRF9ZAXl1jfRWYa9mDVl4q" +
                "wZW3inuWWroPbqxkK27mrY+t0KB5eoVzFE2vcJ5R6OVoWhXypV" +
                "dwJUcpr+uGnIRgxnaI0MbVeDe4V8IJltVodr2idX/6oPVN77zj" +
                "CX7xSQt/IeJsjEoX0oVe7g7drrbxlq7uuZvvnRfLubPOfjfI+3" +
                "jl7f7i3PEvS4OIuQ==");
            
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
            final int compressedBytes = 2139;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXEuvHNURnh0BQjAkODwsHDbIWcRiBwsWd6bvCCQWCEWs+A" +
                "EggViwCewynpbHvkJiESf4IvOwEK/LK7xBChJGyBGKQkKkRIqi" +
                "JARvYMsyCEOfqamp+qrqHPe9M9PypY/Oo77vq8dp98z0PK57vX" +
                "QMPx5+nEbpdAxe7s2PpBm8LErR8cxq65X8kmevcOiIXsl5maWo" +
                "1osZqkAr6Dj0tOgkLsW2kQSLj9ETvNo46dnDg94Oj8mxXofHxl" +
                "PLjji8Z3iPjLQiq39hGrUCeW/bqLySOBjF1mFzaE1cha2Qx1S7" +
                "r9TWRpbPF9n2vB26cX4FPNbbtUd/73J1/lDn6fHde56Onm35+H" +
                "y+bcT1fev7eGSbrP4vCBOFzKJCi/Q+no6so5E6ZsUqV4F7kNo9" +
                "r3cs+9d7tpXq3KNTTf+g6b8fNc/v48tG786uyetK53f0yXR8J+" +
                "T+5JA/LHZ9jN4osqfd4ymsffReiP6xZQ1wnkb75+fphl18nm5Y" +
                "/nkyXvtX84wxfLfL56dVZMtdT+Fd0NZOr6f+VqfX01a762ny/H" +
                "avp/6j0iMuZ5W17b3KvNXoSvMVaybeQylvzPV/Jz3iclZZ296r" +
                "zFuNrjRfsWbiPZTylmta1fNT/8VO7zNXkm3wdercGEur+iRxqE" +
                "RPG4dR1KFSo5g3rm1yWsdgP10DjhFG74OtopTZMtXFqXNjjFbE" +
                "oRI9bRxGUYdKjWJer5F4WJtYfowwVMucz2yZtc9S58YYrYhDJX" +
                "raOIyiDpUaxbxeI/GwNrH8GGGoljmf2TJrr6fOjTFaEYdK9LRx" +
                "GEUdKjWKeb1G4mFtYvkxwlAtcz6zZdb+kzo3xmhFHCrR08ZhFH" +
                "Wo1Cjm9RqJh7WJ5ccIQ7XM+cyWWftf6twYoxVxqERPG4dR1KFS" +
                "o5jXayQe1iaWHyMM1TLnMyOznfvxnb+/23ivy/vxyf5VvL8bnE" +
                "2dG2O0Ig6V6GnjMIq6wdkor85cqg1jsJ+uAccIQ7XM+cwRU90u" +
                "XRCLCe7XFsl7eZ/USjyrRC8RI0+pHUdbTSlvzFV3ShfErqyFTF" +
                "sve4yPVXeWeOK0RlcaebLHNPpv8tWU8npu7b+pc2OMVsShEj1t" +
                "HEZRh0qNYl6vkXhYm1h+jDBUy5zPjExHz+PvL/I8Pn7gfHge7z" +
                "+YOjfGaEUcKtHTxmEUdajUKOaNa8MY7KdrwDHCUC1zPrNlqkHq" +
                "3BijFXHzf9fj2sJ1w25qPXJV8A0p5cC8XiPxsDax/BhhqJY5n9" +
                "ky1S2pc2OMVsRZdbQWZPyQ57ySc2DeKB5XgrWJ5ccIQ7XM4wdz" +
                "mcOab5M+vTYmtNKYKKO1RfJe3ie1Es8q0UvEyFNqx9FWU8rrub" +
                "UvU+fGGK2IQyV62jiMog6VGsW8XiPxsDax/BhhqJY5n9ky/V+l" +
                "zo0xWhGHSvS0cRhFHSo1inm9RuJhbWL5McJQLXM+c8RUd0gXpH" +
                "n83acxwf3aInkv75NaiWeV6CVi5MkedrTVlPJ6rropdW6M0Yo4" +
                "VKKnjcOo51wlN+nMmWpVPKxNLD9GGKplzme2zNq/U+fGGK2IQy" +
                "V62jiMog6VGsW8XiPxsDax/BhhqJY5n9kya1+kzo0xWhGHSvS0" +
                "cRhFHSo1inm9RuJhbWL5McJQLXM+c8RUv5QuiF1ZC5m2Xt4ntR" +
                "LPqtk9y8O60siTPexoqynl9Vx1c+rcGKMVcahETxuHUc+5Sm7W" +
                "mTPVqnhYm1h+jDBUy5zPnGNW+T3nxoddfs85WckuBl+lzo2xtK" +
                "pPEodK9LRxGEUdKjWKeePaJn/WMdhP14BjhM2+5zSKUmbLDH+b" +
                "OjfGGCVeK9HTxmEUdai0OWJe16ZjYFU6G48RhrXJnM+cY9Kx/o" +
                "RfLX4sGmu7/susPZPhydm8xatlRu3Kf5m1ZzI85VfLjNqN/zJr" +
                "V5+vTX8FM351/Jy83lVXVFf0ekdPjF+Zcs/OXg2/MZ5vGfuZ8W" +
                "vKemn+Kvp5ihZkfqHp5tvpcfC58+j/pfp/fXXjpf5WZPymzjae" +
                "//aZ/m5jm+fmjfHbi98XjO9vpzt070Kvx6fGp7br0fJu5XTrK3" +
                "RzfZNHtlOr9lZ71zfrk1ohMze0OYKP11yfe70Xq3VeHzP1jY/y" +
                "VeAectnSSNeT1KarlVx+T+ix2vvMwcNd3mcuP9vkc3OeHllN5c" +
                "OtLs/TKrLlvr/b+GtwVufvoM/v39kf+UGoc9/f1V908zv7ts/j" +
                "zSvnz7u8ntb/0t31NPhx0eu8vp7i2pdxPfU/kx5xOausbe9V5q" +
                "1GV5qvWDPxHkp5yzWt5vVusfunHTyPT5ara5THh8d5ZJusjU8J" +
                "E4XMokKL9D6ejqyjkTpmxSpXgXug8ehdEa93LPvXe7aV6twdfU" +
                "73t939Od1wc7jJI9upVddX1w83j57QioQyT030xFIEUul4FM16" +
                "EY95bUxSjO9GP18Djj5bGmd/lz+vTVcruWx064HXU3WwOhh8Zn" +
                "ywdM6Z9ao42uqO7WRrq+3md2KDCxbZ9+Fbt3lfcEG7+4IpuqP7" +
                "zMN39L4Hx5GLWuou3vEd3ewT0+pAdSB9j2Cu0QPFK/hATkXRFn" +
                "gV+Ps2H3dhNv/505E959pTt/dPzV3bt11eT8vPtt4cPLLNFo+s" +
                "kFlUaLGXjacj62icI2LFKleBe6Bx4x8Rr3cs+9d7tpUq+8z6mW" +
                "Y1HafYGWrNv8mnhIlCZlGhRXqJV9fCiK4eaRTz+pjnqkJzPKba" +
                "PS+Z1Hk6o/dsKxW7bh5n9Y+me7oOH3f9f6n3i7P/h+TQpXPkyu" +
                "jqrH8or3f1nvqyup757QHVyHhdU197js8lpvnr4DP2+mdN9CZr" +
                "fbnCrobaL5mOlxq/n9Q/bfN6V19V7ys/P/X/uXtf71ZQ+3c3/j" +
                "kF");
            
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
            final int compressedBytes = 1616;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdG0toHVX0LQOCCkIWtSFdGUi6iKELwQrOzHtSo9YPIvTFIH" +
                "5CYxfZuRTy8ibtJG9RKLgTSRGtxW/amipUqoUYpNZFu+kiC00R" +
                "BRe60EUF0Zl3c945995zJ3fe3BkzvY+5757/uSdnzpyZN6nV5N" +
                "E6CSvv+1rGsfyjiglDji9s1Qoe2X3POjBO/ql45xtZZBMJI+2r" +
                "WonDvbX6kfoRmAEGCGbgwG/kotDyTyCl6qOaqTawwVERSvdC3o" +
                "OYo8Mcne4Y90/3rHpK4Kn6VLzqzl3clPiINWAAj3QO9o6BlKqP" +
                "aqbawAZHRSjO6y2zF9RDmL1jHB0tkThN0T2rnhK4WW/Gq+7cxT" +
                "XFJ87dFYFDDvxGLgr5K4Jf1ye0qVLATe2qOoHD7AWlwaxbky2R" +
                "ODXpntEW4kCicRulotHG7QQWOHUGTsS05wQ/YmHIEGKBX6ejJu" +
                "CjGJ0HdczvUfmWf5M9pd5nGVReHtG4ZG/DWWW9VGodd24tfD48" +
                "JMGHe7bCzN4ZJcJnc3r5uCtP8sQpfDJ8TI+TzWjPSbomzXHyc2" +
                "Smv5Eep/ApXcJy90+UEyflbL7fHCczLX8+6XGytWbvVWOwMYiz" +
                "WAlIwJSD0jkYNIIMSCfr4LIuhTaoHlXnTl6oe+CtyZZ69fd3bg" +
                "8cHHfg38TH1/HxWeu7eB5ufSF0dO5J7dt/6M4XWdpVDZOzrrbO" +
                "p1K/VTGdu1m+L1lsXxVh4aP+61PaCF4r83pXnDXvFzw4mglK57" +
                "WXSqerPNRTs8eUwu8hza5M80IvxFmsBCRgyiHTk7k9R2FVK6yS" +
                "dfACcFJtsoRqg/LwXqgewqxbQ/9ky5w9DjbXJ29aj/HiSjXqE+" +
                "c7V58W37GvT8Y6fm9142Rbx4+/nj9OO0jt7nx62S5OCV+f17um" +
                "1NeuKn3uamrfvGri8lfTJXfsyFdd8HN+2es25VM0tXvy6fihbP" +
                "kUvVhE/9T+sDt/2j6TxEmhfdKd39/W+Y9C/VyB32uTv06714u1" +
                "tgyWz8bHOQXHnPetv9P8n98TS31MNFyIfibQB2bJzn07xuZ8e4" +
                "3DR70z29ufvytbfInFThf+O8L+oi1Er/Rs7atwnPYVpvktPDia" +
                "CUrntZdKp6s81FOzx5TC7yHNrkoz9k8PVLcvOHHZro6Hv9rW8W" +
                "hW0XWydgcM7xG3fEzMe9e7zkh147Q8WLQFjFPj6erGqQjfk/rE" +
                "xmmyb41XXfsY/pFK/UuL06Rlffqz3/sWtc909Zyu3NEZK0Krfy" +
                "Y54AM4wAo65ZQlVT2Apbyol0qhDZnO+UZ1yF6hNZw5nOwbfpst" +
                "yxRjX/BQdfuCaMB1X6BZeGM3nncLGe10CrhvMT7PPFjh508Hi8" +
                "4nf1ZfOaiAs+XKu/TdcI09d0dc78YL7zO371uCvcHeWm3pbVd6" +
                "E23lDd7awrsOn6u8uW1pKBiKs+u0M8+HcuXHgy6suYyTd+X/kX" +
                "V8H3ylaI/xzbFoPq6Ha84q+Vpx0slzXxv+PD7UD9QPwAwwQDAD" +
                "B34jlwyBlKqPaqbawAZHRSjdC3kPYm4scXS64+2sWKLaqC05Di" +
                "jRO4ef2T7Dx4NxvY4HqdcRoOpcQlv/o300Y30at69P/XkG98HB" +
                "SMA8fwpSn0kBVefitRV4vRtxz2vqx32nv9+la3Pdj/PW8v1+dy" +
                "f+bh6dKi9O3vUK399ddx8n5Wy9pK+c3EnUC6xGmqfRqyU+L9is" +
                "cD5tFp1PvevdRDCh37ckuJS/64SJS2jLkY0PZ8ywiQx9wUS+fH" +
                "Jbx/31HN34eubr3bpdPkWnHdTxG8zf9dFscYL/55TPu+z/z5n5" +
                "vLthF6dsz+n8zeSAD+AAK+iUU5ZU9QCW8qJeKoU2ZDrnG9Uhe4" +
                "XWcOZwsm/4bbYsU4z5dK3Cdfxaif3TzQrH6WZV+/HOc2XG6cSw" +
                "+/pUUpyauy9O4b/2+RTMBDMwAwxQMOPfohz4jVwyJKSQG1f+LV" +
                "0KuKldXWcyC2neC3kPJmtiN/JeZW/FSvYFYfN7GN5dFs+F5uzy" +
                "Ke/I+h4G7zvTP511UMcHLGQvWtoou44P2Nfx3Pd3YxWO01iJcR" +
                "pl+vGZbHGC933lOGV/3zdznEbLi5Pb610wXWY+BZb/LxVdcFCf" +
                "hivcZw7b55OL9+n8LVdXrHyaskrz/Pl8ML4ndrS6+VTAe2L/AW" +
                "Z14o8=");
            
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
            final int compressedBytes = 2233;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXNtrHGUU35eCLaKgkCKYRuxDbfAlTcEH6cNcFi2I+AcoPp" +
                "akEBFCKWjabDbbJE4h+KIvIoqKN7TWUKzgQwXRilC1phfi5cGH" +
                "RqGFPATpU3C+OXvmXL6zt+zstrtDzpzr7/y+rzOzM98sLZUq35" +
                "ZKlQvp3xeVH1M5UjlfauNTuZTJr8zYT57nm1JXn8pK0+j32hME" +
                "Zt7XVl7lhzY5NJin8vHu5ymZvjPzZHP352n+n/bnSWGNcCs8I6" +
                "PatqN+VnimeWWrT6fVdr7Fq33sRsdTcqLI8672Xz+Pp8V7ij6e" +
                "otPRaZRoo4USM2hPWdLCKo4BWnLcr6IeHEdjtmIhxwByYcSK8x" +
                "HT+PmYOWc1hs1oM9Uymfk2YQOdS8rkNdxm3RVeOk+vUDbtZYXu" +
                "wXNknYxrPtk8bfpMNTccnR6D7p9pN6ObqZbJzHcTNtDD806Sn8" +
                "e5DRmsu8JLrwLnsVJW8Qrdg3Kg2mdB3KT0u+FoODdkzTMlF7Lj" +
                "J+MnSyWQ7uM0tFBiBu0pS1pYpfFS5pf9KurBcTRmKxY8htLvJj" +
                "vRPHE03kvOA6+oX+uWYT9XWfyuNLCfuVd73aHyVCfZ1am7c56S" +
                "HsxTf+7Hk9l+3hcsjLR3X5B5u7wfD1YH+LlltX/z1PnxlFTvln" +
                "lafLboeQqOBcdIggYW2DxDxp2sTnFbo6JGOBJF89A9eI7NQjNE" +
                "mcxoPOInO1v9LLtv16f5fh5PS7vaO55qD2z3OTjexbVwvahvi+" +
                "6QOq2287vj0J/jKb6/n8eT3a2b61O8I97h2/GOaCwagwhJ5yUd" +
                "/aRBNL3PH9OoDo1n8yrdmfI0CmeLuZIL9ZPcCdlYCRgjRH92cj" +
                "Y7452plsnMtxM20MMbGEMvxi0bEXy89Ji/4VdhNu/rY1K1zYIz" +
                "ROl3w9Fwbpwt9fLH5PbRepSetSCz2V2HDXT0oJ/isFWnuIUIPl" +
                "76DbTA83QPjit7tGbBYyiTU1acOrGjaZ2PmXrJeYi8K9vcE/k9" +
                "yJUin1uiC/18bim+W3QuOocSbbRQYgbtKUtaWMUxQEtO+1XUg+" +
                "NoTC1bZ2TrT0acuPHx8zFzznIM3rydFd+kKzIWNvneoaifFa6E" +
                "K138W55tXi052wwa8WqU2+qz+Huu/VHoE/zr/Tzv7OfgIj+Lf+" +
                "baX0Ven5KP+rqu8mHh16eL0UWUaIOVvAE+yqA9ZUkL8inb1yQa" +
                "ZNtRspqzkGOoc7/PivMR0/j5mDVTZm9EG6mWycy3AVva603wUQ" +
                "btKUtakO/jcWSOBtl2lKzmLHgMZTpPRpw6sXna4GPWTMmuXFN3" +
                "8b/lT9LPt/lMcX3bTyOXO65YbS+vXe5dXJ/+znvtH9z18d5zr8" +
                "X5tfCtwZ0n+31woR3yO/Tk/cGdp9eGej5P/3aS3cF9wSd9vS/4" +
                "uPAz+UBwgCRoYIHNM2TctzUqak5PPsVMjiYrdA+eY7PQDFEujG" +
                "g84ic7W/0su7N1uto721737ep3Pb16L1V7+068b2k6T1/ejfN0" +
                "aqLt3/Xcjm6jRBstlJhBe9yqU9xCBB8vPe8+53m6B8eVPVqzkG" +
                "Ood/vMivMR0/j5mKmXnAdeUT8SX67X7452l0pLhd0bOLQ+rj+Z" +
                "3ebe2z5ieaI8QRI0t0X7o/3lCTdPlOG8GJd+0KL63R1kET6iyV" +
                "rM4HkSk3KqR2SdjGs+yEAzhXmSnfmYOJ5nT5YnU60uQQMruOIk" +
                "xfie15DNuk+CNT+PUcosT85XyMsrdA+eY7PQDFE67j5TyQ0tv5" +
                "9lx8PxcKkEMlszH4Yt7bUGPsqgPWVJC/J9PI7M0SDbjpLVnAWP" +
                "oXTc/Th1Yu8RhvmYNVNmr8VrqZbJzLcGG+joQT/FLTs4ilUajy" +
                "NzNOxhRclqzoLHUAZHrTh1YvO0xsesmTJ7Op5OtUxmvmnYQEcP" +
                "+ilu2eEUVmk8jszRsIcVJas5Cx5DGU5ZcerE5mmaj1kzZfZyvJ" +
                "xqmcx8y7Cl/yaPgY8yaE9Z0oJ8wqu9SBHKq73AvbKvj9mKBY+h" +
                "dNz9OHVi87TMx6yZkh3uDfem81+XoIEFNsX4nteQzdbnU09wEj" +
                "XCkShiRd+LUo5DsllohiiDkxqP+MnOVj/L9u5ZC1uBD2buHFJx" +
                "vfP5PBweJgkaWPGqkzxDxn1bo6JGOBJF89A9eI7NQjNE6bj7TD" +
                "U3sPx+lt3kd4dX/VldenC7z3fd/Wt2/LvDq+0932Xebn+fuc+Y" +
                "p6EBmad9Rc9TOBQOkQTNbdGeaE845J5bKIPHpR+rJSpqTndosj" +
                "Yc0jx0D55js4C45mN1cxKeW2Rnq59ll2fK6TUPJWhggU0xvket" +
                "OsVt9jQwgxZGk58xk6PJCt2D59gsNEOUySWNR/xkZ6ufZXf3fd" +
                "fBuu+vfV33/aVoxKWH1LpKfWU5Go/G/XUV52uymjHeKAvQvDWc" +
                "tt+lV490uK4y3v66SvMx5cfdofIhkqC5LRqNRp3NM5wX49IPWj" +
                "Ra7zwKHqxGNFmLGTxPYvIcWSfjmg8y0Ew5EuHyXMLz8dXx9XCO" +
                "8Mzgvpcqnrt+b770SK492iZC/t48fKnDc35Px2zbfG/eKZNtvA" +
                "/O33zFV3veq2e/9SmeezkshyRBAyu+7iTPkHHf1qioEY5E0Tx0" +
                "D55js9AMUTruPlPNDSy/n2X37r1UMFvU/Xgwe+rpDu/HZ4u+H4" +
                "9vxbdQou226GB0EHyU4bwYh43yIQr5kMXxAE1XQVz21ZiYIet8" +
                "DlL63WQnPn7K5b3IB3Z5q7yVHlmZzI6xLdhARw/6KW7ZiADe4A" +
                "ThcWSOhj2sKFkOqTELHkMZnLDi1Imdd1t8zJopn4F09u91f7jV" +
                "74Eybf5diOX3ZMKSOkXBK/NkJvfKvn6O+1vaxzGwjnOQ0vLBfa" +
                "bOaNZZRhpdn+IrRb437+96gc292/9fJfzA/eGGPtAgJjNlpcZB" +
                "r8yzqsAr+9rcJAbWcTQpLZ+F1bxzo0h27zean7nPDfD9eA+4y/" +
                "Nu6fH8vLs2wOfdtcLXM/8H6/a6vg==");
            
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
            final int compressedBytes = 1914;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW09oHHUUnli8Kliw0IM5FzQIXgp6yXRWWtBAS+6lJyniH3" +
                "Iw19002bogFSxIaEEUL+IhCB5U8JBWaRFrqxUULWKsKEV6EG/2" +
                "IM7O25fve2/e/NxNmrYmO8O8+X3vz/e9329nd2cnbZZl2YFd/U" +
                "P3rNpktPiOxLJMvYzsGFHx2jybyV6rW8/pH70p5tA67sHayJdl" +
                "C+/WM1LKNtI5Vx6r5fFB54vSTnY+Fn/xQ5bYOpcr+1EY+7Lm+b" +
                "T3eLaJrfNhMnrBe+LeO594z+KN0ntxyB4a1uk/qkZcpyy7k+vU" +
                "kLepdcqy/M/+obv6+qPeExKzmbbS86jX5tlM9lrduLcTzzGH1n" +
                "EP1kY+ed/5jJRyU6Ra88lsvDVsrTf7h+7qU6/EOdNWeh712jyb" +
                "6TXiOPfGHLYrVlMb+WxvODcr28hO/nyqvEN9PhVni7NqFStSqx" +
                "k4I8sirfJ8zMxsqhFFgdJd2Dk0qVklnj/P2dcSXi6Wy1FlK9+y" +
                "7DJWj/oRj7Ay1PmYmdlUI4oCpbvgGJSjOJRonZZ5zr6Wte3We3" +
                "QrPgEXnt9c/Ylzd/9TfHz/tKGOJu/F6+nub/ml/JJaxYrUagbO" +
                "yLJIqzwfMzObakRRoHQXdg5NalaJ589z9rWsPb7PHGWb/hlHFG" +
                "tC6dzhq9Jxn8OdNnfMkXgOKV0ba51pnVGrWJFazcAZWRZpledj" +
                "ZmZTjSgKlO7CzqFJzSrx/HnOvpa1x++7YbbuU6PcF3Sf3Km/W3" +
                "qHRlmn3sGduk61qreSV1++Y5+pnGqdgpWRIMGcYeN17Fl1BB7L" +
                "4vvwGpwTd+E7hLLnQ39WOdKLcH40P1reSQ2sjAQJRozPXANMd2" +
                "9HFTGbreX8KGpz4i58h1D2fOjPKkd6ES5WipXyF3Flq9/GK7LL" +
                "WD3qRzzCylDnY2ZmU40oCpTugmNQjuJQoucFKzxnX8va/4ffwd" +
                "2Dd/9zfLxOG+ro8PiecrxOm7gvWGotwcpIkGDOsPE69qw6Ao9l" +
                "8X14Dc6Ju/AdQtnzoT+rHOlFuFgr1spP9MpWn+1rsstYPepHPM" +
                "LKUOdjZmZTjSgKlO6CY1CO4lCi77s1nrOvZe3xc99haovrxXW1" +
                "ihWp1QyckWWRVnk+ZmY21YiiQOku7Bya1KwSz5/n7GsJTxQTZt" +
                "0qLD5vNVPG6sfIswSvyoTy16ussu71eu7B98KZbLn7xitmosnX" +
                "r8+n8qnyznxgZSRIMGJ85hpg+jUwpYjZbC3nR1GbE3fhO4Sy50" +
                "N/VjnSi3AxX8yXK1bZau3mZZexetSPeISVoc7HzMymGlEUKN0F" +
                "x6AcxaFEV8w8z9nXAudH8iPlig2sjAQJRozPXANMr9IRRcxmaz" +
                "k/itqcuAvfIZQ9H/qzypFehIu5Yq5cscpWazcnu4zVo37EI6wM" +
                "dT5mZjbViKJA6S44BuUoDiW6nuZ4zr6Wtd135+vrK3165L8Fns" +
                "628TbSc9/dG71/mv7nTt4/xWrb/3nBVt1n9o6P12mYdeoe3+hz" +
                "lfyx+mj02ru93f5OWm+03oCVkSDBnGHjdexZdQQey+L78BqcE3" +
                "fhO4Sy50N/VjnSi3C+N99brv/AykiQYMT4zDXA9HruVcRstpbz" +
                "o6jNibvwHULZ86E/qxzpRfjAjQPlb2ax/a0/UqRWM3BGlkVa5f" +
                "mYmdlUI4oCpbvgGJSjOJSwTszGWnYd+ufWLXP93+pj8XmrmdZv" +
                "veAJ3+W3NL8eB5Pmsaeewxw+rzhsO+XuR/pUovr2efOdMNn+uv" +
                "R91x+3v6/stcr+Wn6LviQ57b+q79QXqvHfHf2OfdHw7I5022vt" +
                "X9rVs4r2jdq30a7O/evjBzsPd/a0V9vu32W2r7a/JfTT+uiPhv" +
                "u9l6voxfal0l7ZwPfsfZV9oPNQPpPPlO/AgZWRIMGI8ZlrgOld" +
                "P6OI2Wwt50dRmxN34TuEsudDf1Y50gvxvnxfORpYGQkSjBifuQ" +
                "aY1PcpYjZby/lR1ObEXfgOoez50J9VjvQivDi7eHDxmcWn15+E" +
                "zmziHuORpsji4ebYMNvioWT02eE72VhembmntQdWRoIEcwbHI6" +
                "yMWqPV4LFV0IiiQOku/BxiNavE86/PIcL5sfxYeWUNrIwECUaM" +
                "z1wDTFfzMUXMZms5P4ranLgL3yGUPR/6s8qRXoj35/vL0cDKSJ" +
                "BgxPjMNcCkvl8Rs9lazo+iNifuwncIZc+H/qxypBfi2Xy2HA2s" +
                "jAQJRozPXANM6rOKmM3Wcn4UtTlxF75DKHs+9GeVI70Irz+xuY" +
                "kDHj+qIx9pymV/nWH6ZjMrmC2boqhSfM2aqE3NJmT+DUcUa0Lp" +
                "3OGr0nGfw502d8yReA4pXRvrvTL+f4qjdDL9O44o1oTSucNXpe" +
                "M+hztt7pgj8RxSuj42yvW09PZOfZ7Zvdo9313trHavyDp1Px/u" +
                "fZfk/KbmubBJxs+S0a82yFq9nt3Lt//5+Mn3t9f1dPK98fV0e6" +
                "+n7bBOvVfH63SvrFPtG+3aVvydqPfaVv4VaunHrf9L1/TC9AKs" +
                "jAQJ5gwbr2PPqiPwWBbfh9fgnLgL3yGUPR/6s8qRXoSzfwEuxD" +
                "ba");
            
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
            final int compressedBytes = 1287;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrFqHFcU3T9xK1wbglW4mBlBfkIQ1NkqBPqDFSZGVVy5TB" +
                "cIAoEhRRKUIm5i7ECadIFNaYxJAiEf4MzO08k59777ZkerlWwp" +
                "b4a98867955z3tPsaLV2d9qdzmbdEJfHcgSEiApeWWURujyfMi" +
                "sbNKIs0bgLzVE5ylNp9t+hbKpl92F5nb/oXz/2r+fzV328M/9u" +
                "NuGY/zLEb8Pcz9nMD7NLHZ9/Ourlp2ks8+/D2ZfrODram9Vjyk" +
                "/u97oHdZ82d8zvXAXr0f5N35fmXfMOERgIERW8ssoidHk+ZVY2" +
                "aERZonEXdg0lNauk69c1+17Bi2bRj4Y4zC3SmcaYwTzzEQZDzq" +
                "fMygaNKEs07kJzVI7yVJJ9Wuiafa9qX8f77vYdzcN8tEnW6+nf" +
                "pHdhPVm+cGIOsymvlbbT82BWa8mrXdSw+cibclhXVGOM5qw3Xs" +
                "vKmunedG8QgYEQUcErqyxCl+dTZmWDRpQlGndh11BSs0q6fl2z" +
                "71Vt93z6oj57oqN91j5DBAZCRAWvrLIIXcphR5YNGlFW0ZiLvC" +
                "Ligy9dq3WLfL4m21F/313o/vpqvdyq7GUcrWKerry+x+Zec48x" +
                "jRJKWCtsPseeFSPyWBbvw2toTezCO6Sy56M/qxzpRfhmfP80/+" +
                "ZDf//UPGgeMKZRQglrhc3n2LNiRB7L4n14Da2JXXiHVPZ89GeV" +
                "I70I1/tp2v3U7XV7iMBAiKjglVUWocvzKbOyQSPKEo27sGsoqV" +
                "klXb+u2fcSN7vNbn9nncc0Sihh5vSqPcRyN+8CKZvt1fooa2ti" +
                "F94hlT0f/VnlSC/C9X038X33qHuECAyEiApeWWURujyfMisbNK" +
                "Is0bgLu4aSmlXS9euafa/gg+6gHw1xmDtIZxpjBvPMRxgMOZ8y" +
                "Kxs0oizRuAvNUTnKU0n26UDX7HuJ28P2sP+cOsThE+thOtMYM5" +
                "hnPsJgYHU+smzQiLKKxlzkFREffOlarVvk8zWla30+Tez+H+/T" +
                "47d1n1bv0+P30++n9mn7FBEYCBEVvLLKInQphx1ZNmhEWaJxF3" +
                "YNJTWrpOvXNfte1a7Pp/q+u3n71H52nfsUq23y//XU733r+64+" +
                "n+o+fTz7VJ9P9X6q99MHv7vqPsXfZ97v7iMCAyGigldWWYQuz6" +
                "fMygaNKEs07sKuoaRmlXT9umbfK3i72+5HQxzmttOZxpjBPPMR" +
                "BkPOp8zKBo0oSzTuQnNUjvJUkn3a1jX7XtWuz/F1ft8d/1r3Kf" +
                "z+adEucpzmfESlnUeHZj2rsiPvK6wyTqpat9aPOoo92mzsrTQX" +
                "raY9G2E6G9U5u0j1Jo+y0tV5OPoEo+Pf6qeBkZ/NyXq5VdnLOF" +
                "rFPF15cx7lflrUu6Z+Hq/7dEV/t9zt7qarYp1jZI1Gz6CskVpi" +
                "yrusMs7YLf2ALXdgPdps7K28N/251W0N463z3IB1jpE1Gj0DWQ" +
                "IvW+DPu6wyzryfHviKHFiPNht7i9yWVnP8tr7HphzHf96KVfwx" +
                "se6vtT8X/F3vlUk7/E/dgynfF9TvVeo+bfTfEb6s77DysXO6c4" +
                "pRepVqEFGr11KXrS7V+TjuMPeh/hSXmXI2MtG36X2983oZ+Qqc" +
                "ntewAnVxPbtsdanOx0gdWbDaLvqzayoxeTZl4tx53f7OPmMaJZ" +
                "SwVth8jj0rRuSxLMZ9lrU1sQvvkMqej/6scqQX4Ys9x5+c3K7n" +
                "+JOvJz/H/wUtgN4I");
            
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
            final int compressedBytes = 1493;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWctuHEUULSs/kC8gbBHsyZK4e0Ys7AV/EJQFYyF/RBtnAE" +
                "sj/iCCHcKzQGIBSCCZRxIhRBAsvUg2QWKL+AGmuzi55966Xe4Z" +
                "ZwZnqG7N7Tr3cc6tSnW7ZxJC810Izdni83nz08LeaL4Kvcf7n2" +
                "HUPOrsl15W83Pi+SZc6mi+yEYfDGT5OpnPpwvvwyG19e36Niww" +
                "ECwy5CpZGqHK8jEzs0HDiwrKd6Hn0KemlXj+PGdbS3hSTxajzn" +
                "a+STzjGB74Je5hMKR8zMxs0PCigvJdcEyUvbgo0TpNeM62lvBh" +
                "fbgYdbbzHcYzjuGBX+IeBkPKx8zMBg0vKijfBcdE2YuLEq3TIc" +
                "/Z1goeVaMqBNg4iihiifGVawSLOhikTjKZTVdYDc7xu7AdirLl" +
                "k/60sqfn4bDUc5yeiVvxHD/+c+hzvD3e+KP94IQvjmJMZ+pKyw" +
                "OvztOZ7NW6fm+aA3Xcg7aeT2fLtV9ZRzazn05eevH30+7f7Qcn" +
                "fHEUYzpTV1oeeHWezmSv1vV70xyo4x609Xw6W679yjZS7bQfnP" +
                "C1o5OXY0xn6krLA6/O05ns1bppTvt57x3mQB33oK3n09ly7Vfu" +
                "i3R780YoR99991f7wQlfHMWYztSVlgdenacz2at1/d40B+q4B2" +
                "09n86Wa7+yjpT3gpU6Kvdd38ossZ9OXvkv9tPu/WX30+79Yfvp" +
                "g0+G76el1unVct+t8747evdFv+tG56NzsXEUUcScwXEPgxE1qB" +
                "YeXSUaXlRQvgs7B19NK/H80zl42N53H752Ff/eTd9cz33XeVd6" +
                "PpV1GtjRW+UNwH0+HY2OxMZRRBFzho6n2LJiJDyaxfZhNTjH78" +
                "J2KMqWT/rTyp6eh5NdeK/sHfcd7vrudbFxFFHEnKHjKbasGAmP" +
                "ZrF9WA3O8buwHYqy5ZP+tLKn5+Gyn9bxvaV8Dy7fg/NH/bR+Cg" +
                "sMBIsMuUqWRqiyfMzMbNDwooLyXeg59KlpJZ4/z9nWCq7Oq3P1" +
                "a2eHo89aZGo/KjhqWZkdcZuhlXGKquSk/XBHfo866vfW54v1yz" +
                "yf7n68Xc+nu/fW87vK8cGWPccn61mnbft7t/r3u6v59+74Tjb6" +
                "9lV7f/o/P5/Kfhr0O8rv0++nZ83Z9Ne4n6Y/Dns+ZTl/SzwPLs" +
                "n4Qzb6y4qs3b6fPhqSe6u51YiNo4gi5gwdT7FlxUh4NIvtw2pw" +
                "jt+F7VCULZ/0p5U9PQ/X83q+eOPsbPfuOY9nHMMDv8Q9DIaUj5" +
                "mZDRpeVFC+C46JshcXJXofn/OcbS1rl+935f1pje9PH5WdU/bT" +
                "6vupvlZfS3H0WYvMOIZfRv2s7Ee1rdLKOPu6Ra7uhTPZcvf9K9" +
                "Hna+vHO+OdEKJtj3YEBIsMuUqWRqiyfMzMbNDwooLyXXBMlL24" +
                "KMlKMBtr6XUY79R79d5ixTrbrd1ePOMYHvgl7mEwpHzMzGzQ8K" +
                "KC8l1wTJS9uCjRjtnjOdtawvv1/mLU2c63H884hgd+iXsYDCkf" +
                "MzMbNLyooHwXHBNlLy5KtE77PGdbK3gcxu3O6mz4dwQ0fuYJz7" +
                "xjdQaDUGX5gsoMiUbIcF7URXBscOOBmILp1mqNTefJL53fpqOr" +
                "fmym0/JeUNbp+a1TdVAdwAIDwSJDrpKlEaqYQ480GzS8KKNcF2" +
                "mGx4e+eK66W8TTOXWjWTVbjDrb+WbxjGN44Je4h8Eg2elIs0HD" +
                "iwrKd8ExmYkXFyVapxnP2day9mbuu+rOJu87X237/x+hfA8uf+" +
                "+2a51GT0ZPYIGBYJEhV8nSCFWWj5mZDRpeVFC+Cz2HPjWtxPPn" +
                "Odta1i7Pp3LflefTxn/PvFnfhAUGgkWGXCVLI1RZPmZmNmh4UU" +
                "H5LvQc+tS0Es+f52xrWVsfR69jdDIrzyR6c31cPU5x9FmLTO1H" +
                "BUctK7MjbjO0Mk5R1d3qfrgjv0cd9Xvr83mzqU4zTKdZndM1/U" +
                "ueXsQ8XPn59VjNV4tdFL1MRxcxD1e+RI//APqciRo=");
            
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
            final int compressedBytes = 1319;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWUGLHkUQHYK/QkS9LvkFuQTcnfkB/gTxIDmGQM6ufons3v" +
                "YgeFBRQiASJZEEVPAQL0pYyMEE9CQ5eMhpT0EQBP1mel/qverq" +
                "znyb7JrN9gxT01X16r3qzjdj79h1emy+0bVjxtHWKT760/3pdG" +
                "efY2YNw9YzMGuklpjyKlXGGXdr/YAt70B71GzcW3ltludavzaN" +
                "1/Zzk88xs4Zh6xmMJehlDfx5lSrjzOutB7uiDrRHzca9Rd3ivv" +
                "mbe+7ur/yk/n7gZ/zXlSsevCjP4fbH7V3U1uk5rtMnbQ1mrdNn" +
                "bQ2O7/5p8W41+86RrMxPy+vO8rq5eXdcp83vy9hLXzypujfZ70" +
                "LG3Szy4zP2eKua/Xkmyw/ZfD5fRn9pv6dDfD9dbe+e8jHcGG5g" +
                "lK4SBhZYvpeqFF3CeVvvMO+D+2O/zJSzGZP13f4Ofqbn7uu2Br" +
                "PW6Zu2BpW3w+6wO1q7yhhDABfjrUrRJZy3kTqyYNUq60/nVGLy" +
                "bMxksf1vBxf6C7Dw4cECYXdDqYcqz8fMzAaNKGtevQudQ0lNlX" +
                "j+PGdfy9qr7DO3b75c+8wpOmufuf2t26F+dXRP+6Vrx+jNdGY4" +
                "YzaNkpd8Rmg+9z0rRsajLL4Pr8GYuAvfoSl7PutPlSO9yPffM7" +
                "fefPJLuz3z13zg75lbr6/8/P1v3zOHs8NZs2mUvOQzQvO571kx" +
                "Mh5l8X14DcbEXfgOTdnzWX+qHOlFfvZv/FrbJYW/5OJzN5vhZD" +
                "x3/dCbTaPkJZ8Rms99z4qR8SiL78NrMCbuwndoyp7P+lPlSC/y" +
                "+71+b7mTmuy0p9pLZxojgrjlIx8MOR8zMxs0oqx59S44Z8pR3p" +
                "Ron7nHc/a1rN116/+MF85Un0Zbn6YcWNXTsWVTVHGK5Kjq5pjx" +
                "+vA95kAd96A2inXdB1dyRE1ZM6vsx6nqhO3H2zrN/z6+8cp44U" +
                "QsjVJOkVrpeRBVnCI5qrpxb8qBOu5BbRRTtN3Lyj6z/ni8cCI2" +
                "jpbvp8cpz0it9DyIKk6RHFXdHDNe0/vJ9WZebqPY/vvJIWrKpU" +
                "z77vuU5+7UeOFELI1STpFa6XkQVZwiOaq6cW/KgTruQW0UU7Td" +
                "y8o+s/7XeOFEbBwtvkw5RWql50FUcYrkqOrmmPHavsMcqOMe1E" +
                "ax/efOIWrKmmn/vTtQR+399Bz2Tx89erl+T4tH7fd0uMflt9sa" +
                "hN9VFsPCbBolL/mM0Hzue1aMjEdZfB9egzFxF75DU/Z81p8qR3" +
                "qR3567eUd/vj8PCx8eLBB2N5R6qPJ8zMxs0Iiy5tW70DmU1FSJ" +
                "589z9rWs3fZP7bvK4e0zh7+P37vj/VePTuutP+2KciWvjp1fVc" +
                "97DHda7pgz8Rxquprrr/fXYeHDgwXC7oZSD1Wej5mZDRpR1rx6" +
                "FzqHkpoq8fx5zr6Wtd0+84+2B2h/t7R1auv04hzr18YLJ2KIpj" +
                "wjtdLzIMpY4+Uq09B81BtzaFemZjaKaW92Lytrpu0zZ/19d64/" +
                "BwsfHiwQdjeUeqjyfMzMbNCIsubVu9A5lNRUiefPc/a15m9c3L" +
                "jYdcmOxziCBwuE3Q2lHqqYQ0fKBo0oy16tixwR8aEvnqt2i3w+" +
                "p2m0s7GzHE12iu2kM40RQdzykQ8GQ+cjZYNGlDWv3gXnbCZR3p" +
                "RonXZ4zr6Wtdv7qX0vaOvU1uko12mV/y91ctdp8e/839PwcHgI" +
                "Cx8eLBB2N5R6qPJ8zMxs0Iiy5tW70DmU1FSJ589z9rWs3f6+m3" +
                "X8B5aYM5s=");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1386;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz2PHEUQHVmyiAhOOscmICbhB1g3M/cD+BNe5/4FC6c15m" +
                "IyEgsHCHQSEgEgERwYG6ETRpA6sC0SO7YIEexM33O9V13TN7e3" +
                "fPeMpqar6tWr162+udn1uWmWXzfN8nh9fbr8fm2vLr9oZhzLh6" +
                "P9PMydZJGvmgsdy8+K2QczWb4Mo9/Nqe2ud9dh4cODBcLuhlIP" +
                "VcyhI2VDjyjLXklFjoj4oIvnqmqRz+eU7nU/zaz+H6/TwbO6Tm" +
                "ev08Hv8/dTv9vv5n6KeQukxsHAPJ6V44kpR2hnnFNqVU+kQDVq" +
                "NtY2FUv19fm0iaLu7ma5s7KbH93ds5jnd95cY78+c78/HavtX2" +
                "KsrqfRNCvH+4kq7dwTJuLt5YoUNMLWTCjyFVFsqO+edE9kxUc/" +
                "xbwFUuOo4KxnZXbkPUI747Suhsn1sKJYo2ZjbVOxUcnj7rFkRj" +
                "/FvAVS46jgrGdlduQ9QjvjtK6GyfWwolijZmNtU7FoNt1Rgemo" +
                "2OfoT3o+HZ3FPL/z9jQe7jb1mLNOV+oa1Pfx7b0/3X5Rd86cY/" +
                "VWXYNZe/NqXYNZz/HX6hrMWqfX6xqEn2oW/QIWPjxYIOxuKPVQ" +
                "5fmYmdnQI8qaV1ahc5jqpp14/jxnX2v+/rX9a00Dm0bJS77l+M" +
                "415lt3MFidIZlNK3wPxsQqvELr7PlMn3aO+kX++Db/6nDhPH3D" +
                "H0e3P0i5l+/94unYsimqOEVyVPvmmOF6Z8EcqGMNaqNY07z9YY" +
                "4odfaZ9rfhwolYGqWcIrXS8yCqOEVyVPvmGONTbeblNoop2u7T" +
                "nX2muzxcOBEbRuv9dDnlGamVngdRxSmSo9o3xwzXuJ+cNvNyG8" +
                "VO95NDlDr7TPvrcOFELI1STpFa6XkQVZwiOap9c4zxqTbzchvF" +
                "FG336c4+070yXDgRG0YHd1JOkVrpeRBVnCI5qn1zzHAdvsEcqG" +
                "MNaqPY6X5yiFJnzdR/v6vfF2xvndqT9gQWPjxYIOxuKPVQ5fmY" +
                "mdnQI8qaV1ahc5jqpp14/jxnX8u9z7OfDt/8O/ZTe/+8+6m9P2" +
                "8/vXtn0++f6vcF9flU/17lr//et36fGR/7y/2l2TRKXvIZofnc" +
                "96wYGY+yeB2+B2NiFV6hdfZ8pk87R/0iv91pd9a/H05tGiUv+Z" +
                "bjO9eYT79vduAxm9YyPsoqJlbhFVpnz2f6tHPUL/Lre+b5nk97" +
                "z+yKclNeGTu/qpz3GFY6rZgz8RxKfePc3i92Rbkpr4ydX1XOew" +
                "wrnVbMmXgOpb6aW/28+mZ1vDxe/Zh+7lbfzvu5K/7O/CmLPLgg" +
                "471i9ocNWcfnw+rh9t+fbn3833o+3fpo9ufg5+1zWPjwYIGwu6" +
                "HUQ5XnY2ZmQ48oa15Zhc5hqpt24vnznH0t966f7+rn4O0e7SfD" +
                "hRMxRFOekVrpeRBlrPFylfXQfKSNOVSVdTMbxVSb3ac7a6Z+Dt" +
                "5E0d69zWdzkdrtHnOVnEdx3U91neo61XWq6/Tv/l6l/j/FObXd" +
                "je4GLHx4sEDY3VDqoYo5dKRs6BFl2SupyBERH3TxXFUt8vmctK" +
                "J+bqk/d/U5Xp/j/7Dn+M3uJix8eLBA2N1Q6qGKOXSkbOgRZdkr" +
                "qcgRER908VxVLfL5nMbRohv+9nG0Y2yRzjRGBHHLRz4YDJ2PlA" +
                "09oix7JRU5IuKDLp6rqkU+n9Nw7y/1l3h/JT/FvAUyjRG3kWfJ" +
                "D6CjKu2MM69nDV4LI9my+qkjylt996h7JD+Ho59i3gKpcVRw1r" +
                "MyO/IeoZ1xWlfD5HpYUaxRs7G2qdio5Gn3VDKjn2LeAqlxVHDW" +
                "szI78h6hnXFaV8PkelhRrFGzsbapWDSb996vb5Th8QfMm4+W");
            
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
            final int rows = 65;
            final int cols = 74;
            final int compressedBytes = 1590;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWT1vHVUQfRWi4gekIPyOJIWVzf4A8gcAUSDFliwhW0mkFD" +
                "aJkEwkW7JFAUpHH9miACQKaBIhEiokKKDnHwAdu3d8OHPuzC6b" +
                "D2Ej7q52ds7MuWfOXb334ry3Wunx0cerdiw47r/VnsGi5/R2ew" +
                "ZLjg/fbM8gO3Z/Urz3xt+vrPcXKvz8vLP3Xn9mtz+e1XPqL/WX" +
                "GC0zZNgztB9xrYqMOqpS+6hneE7uonbIybUe/enkbF6K1/q1IT" +
                "uNlhkyzJ6/+zXEbvoakFfTtZ6fdZWTu6gdcnKtR386OZuX4fHe" +
                "vTZeOG09MutBVZHm7HIlO8r0VZ0bOdRTb0QxZjVl8z49WTu73w" +
                "7XN8N1vPvdEC/ufrnok+KHEr9Ie9+Hytcv+Cn6+Wz30UKVr9Lq" +
                "44Wr23NaeHSvjBdO1CyznjJ1Za2DqvKU6as6N/emGljnPWjMas" +
                "rmfXqydv7Pr6d7v7X3Xft8OovXU3+3v8tomSHDnqH9iGtVZNRR" +
                "ldpHPcNzche1Q06u9ehPJ2fzMtxeT0vfd/2fikZstTqCqXWt5q" +
                "pe3a+Okz3PVyKHGjsX8mnqcYrzz88m+ng+tbM+di6cwfcHF8/j" +
                "k7j37mz3nX/lybS/C5b8LX6ju4EIDIQIBu9kKcIqr6GZqmFG1v" +
                "VozkVkZHrw5feqbtGPeyrZdrc9ZCWW2radlqOCOvsZhgLZMVM1" +
                "zMi6Hs25iIxMD778XtUt+nFPJdvsNoesxFLbtNNyVFBnP8NQID" +
                "tmqoYZWdejOReRkenBl9+rukU/7qlkB93BkJVYagd2Wo4K6uxn" +
                "GApkx0zVMCPrEs278D3uJOtzkntOB37P9VqHt7qtISux1LbstB" +
                "wV1NnPMBTIjpmqYUbW9WjORWRkevDl96pu0Y97KtlGtzFkJZba" +
                "hp2Wo4I6+xmGAtkxUzXMyLoezbmIjEwPvvxe1S36cU8l2+/2h6" +
                "zEUtu303JUUGc/w1AgO2aqhhlZl2jehe9xJ1mfk9xz2vd7rtf6" +
                "2dXvnJ+03+qWHPc/bc+g/b/lhX/tfNg/RGbXFAcRXH+fWqXsKV" +
                "4d5x1GH96fx9NKUY1K9C1rT/oTZHYlTk+sR4ZfM7n/E2jPsVQx" +
                "Y9YOow/vz+NppahGJfqWtU/6J2PklTg95ZABXs7nKmVP8eqYTU" +
                "cXqrqK/nRPU0q1mldi7ZR3ub/MaJkhw56h/YhrVWTUURVxH7rK" +
                "yV3UDjm51qM/nZzNy/B4v/r7eOG09ZbtPbAeVBVpzq5VladMX9" +
                "W5kTNed9/zGljnPWjMaqvVB59Fxtxk7bTvn9r3dC/vOfU7/Q6j" +
                "ZYYMe4b2I65VkVFHVWof9QzPyV3UDjm51qM/nZzNy/C169eur1" +
                "YWx2PMgBDB4J0sRVhV63llr4YZWZdo3oXvcXLW5yQ+J6/mZ+lz" +
                "GO/tfbfo+/Gb3U1EYCBEMHgnSxFWeQ3NVA0zsq5Hcy4iI9ODL7" +
                "9XdYt+3FPJ1rv1ISux1NbttBwV1NnPMBTIjpmqYUbW9WjORWRk" +
                "evDl96pu0Y97Ktmd7s6QlVhqd+y0HBXU2c8wFMiOmaphRtb1aM" +
                "5FZGR68OX3qm7Rj3sq2VF3NGQlltqRnZajgjr7GYYC2TFTNczI" +
                "ukTzLnyPO8n6nOSe05Hfc73Wz26f44s+x293txGBgRDB4J0sRV" +
                "jlNTRTNczIuh7NuYiMTA++/F7VLfpxTyU77A6HrMRSO7TTclRQ" +
                "Zz/DUCA7ZqqGGVmXaN6F73EnWZ+T3HM69Huu1/rZ7X33DN9nHv" +
                "fHyOya4iCC6+9Tq5Q9xavjvMPow/vzeFopqlGJvid+R3i0asd/" +
                "9neE83L0T/unY+Q1zSEDvJzPVcqe4tUxm44uVHUV/emeppRqNa" +
                "/E2invSn+F0TJDhj1D+xHXqsiooyriPnSVk7uoHXJyrUd/Ojmb" +
                "l+H27137PvNl/13QvTpeOFEbs70H1lOmrqx1UFWeMn1V5+bext" +
                "8Ram9EMWY1+x2hZsxNrjtX/xgvnKhZZj1l6spaB1XlKdNXdW7k" +
                "UE+9EcWY1ZTN+/Rk7bT33aL33K3uFiIwECIYvJOlCKu8hmaqhh" +
                "lZ16M5F5GR6cGX36u6RT/uye7t9dT+vWvPqT2n8/y9ivu+4Jf2" +
                "v91Fz+nX9gzS4y9WuYoO");
            
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
            final int rows = 20;
            final int cols = 74;
            final int compressedBytes = 663;
            final int uncompressedBytes = 5921;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV0Fu20AM9LWv6GMWek6M+GrDAXTuZwordlsgh+SSoiiQL/" +
                "QrVZYZcEbkLgS0R0nYFckZzg4V+ZDdTq/x8267Vlxf/mzvoHWV" +
                "T+8LN2oWGaZM7VzqoKo8ZXJVz829qQb62IPuWU3Z/myfrMj4Mq" +
                "/neX0df73/7sbva97t+Fb3byn2O1Se/u0vOU5d9HWlyo+0+nNl" +
                "9/aetvf0395TuS/32JEjww6GP52lGbpYQyNVwxkZylnPRWRkev" +
                "DFs6pb4HEme27f06rv6a7cYUeODDsY/nSWZuhiDY1UDWdkKGc9" +
                "F5GR6cEXz6pugceZanQu5zmqe62d7bYYFdQdz3IoODtGqoYzMp" +
                "SznovIyPTgi2dVt8DjTDU6lMMc1b3WDnZbjArqjmc5FJwdI1XD" +
                "GRnKWc9FZGR68MWzqlvgcaYaHctxjupea0e7LUYFdcezHArOjp" +
                "Gq4YwM5aznIjIyPfjiWdUt8DhTjfZlP0d1r7W93RajgrrjWQ4F" +
                "Z8dI1XBGhnLWcxEZmR588azqFnicyTuGx+ERka14GQc7uPxsdS" +
                "m7xVvu2enuMPpgf5y3laKaK7lv6b0MF0S2EqcXw5zBPc35L9Du" +
                "sVQxYy4dRh/sj/O2UlRzJff98d09lAfsyJFhB8OfztIMXayhka" +
                "rhjAzlrOciMjI9+OJZ1S3wOFONTuU0R3WvtZPdFqOCuuNZDgVn" +
                "x0jVcEaGctZzERmZHnzxrOoWeJzJO4bbcENkK/nyb4Y5g3uav6" +
                "cbtHssVcyYS4fRB/vjvK0U1VzJfUvvNEyIbCVOJ8OcwT3N+Sdo" +
                "D93/PZiTMZcOow/2x3lbKaq5kvuW3utwRWQrcXo1zBnc05z/Cu" +
                "0eSxUz5tJh9MH+OG8rRTVXct8f11/FmmjG");
            
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
        else if (row >= 1235 && row <= 1299)
            return value19[row-1235][col];
        else if (row >= 1300 && row <= 1364)
            return value20[row-1300][col];
        else if (row >= 1365)
            return value21[row-1365][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value21 lookup");
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 18, 0, 3, 0, 19, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 4, 0, 0, 21, 5, 0, 22, 23, 0, 24, 0, 25, 0, 0, 1, 0, 26, 0, 6, 27, 2, 0, 28, 0, 0, 0, 29, 30, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 0, 31, 10, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 33, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 34, 0, 0, 0, 0, 3, 3, 17, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 36, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 37, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 38, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 39, 4, 0, 40, 0, 0, 0, 41, 0, 0, 0, 42, 43, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 8, 0, 0, 44, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 45, 10, 0, 0, 0, 0, 0, 20, 21, 22, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 3, 0, 0, 0, 24, 25, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 1, 31, 2, 0, 0, 0, 5, 4, 0, 34, 35, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 38, 0, 1, 39, 0, 0, 0, 6, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 9, 42, 43, 0, 0, 44, 0, 5, 6, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 47, 1, 0, 0, 0, 0, 0, 0, 0, 48, 2, 0, 0, 3, 0, 7, 49, 0, 0, 0, 1, 7, 0, 8, 0, 50, 0, 8, 51, 0, 0, 0, 0, 52, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 0, 47, 54, 55, 0, 56, 0, 57, 0, 58, 59, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 63, 10, 0, 0, 0, 0, 11, 0, 0, 64, 0, 0, 0, 65, 12, 13, 0, 0, 0, 66, 67, 0, 0, 0, 4, 0, 68, 0, 5, 48, 0, 0, 69, 1, 0, 0, 0, 14, 70, 0, 0, 0, 15, 0, 1, 0, 49, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 0, 0, 12, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 71, 72, 12, 0, 51, 73, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 74, 75, 0, 76, 77, 78, 79, 0, 1, 0, 2, 0, 0, 1, 0, 0, 0, 80, 0, 0, 15, 16, 17, 18, 19, 20, 21, 81, 22, 52, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 36, 37, 0, 38, 39, 82, 40, 41, 42, 43, 83, 44, 45, 46, 47, 48, 49, 0, 50, 0, 84, 85, 9, 0, 0, 2, 0, 86, 0, 0, 87, 1, 88, 0, 3, 0, 0, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 90, 91, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 92, 93, 0, 3, 0, 4, 0, 0, 94, 1, 95, 0, 0, 0, 96, 97, 5, 0, 0, 0, 0, 0, 98, 0, 51, 99, 100, 101, 102, 0, 103, 53, 104, 1, 105, 0, 54, 106, 107, 108, 55, 52, 2, 53, 0, 0, 109, 110, 0, 0, 0, 0, 111, 0, 112, 0, 113, 114, 0, 0, 10, 0, 4, 115, 5, 1, 0, 0, 0, 0, 1, 116, 117, 0, 0, 3, 1, 0, 2, 0, 0, 4, 118, 0, 6, 119, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 120, 121, 122, 0, 123, 0, 54, 3, 56, 0, 124, 7, 0, 0, 125, 126, 0, 0, 0, 0, 0, 6, 0, 1, 0, 2, 0, 0, 127, 0, 55, 128, 129, 130, 131, 57, 132, 0, 133, 134, 135, 136, 137, 138, 139, 140, 56, 0, 141, 142, 143, 144, 0, 0, 5, 0, 0, 0, 0, 0, 0, 57, 0, 145, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 7, 0, 146, 0, 147, 58, 0, 59, 0, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 148, 149, 0, 0, 150, 0, 151, 8, 0, 0, 0, 152, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 153, 154, 155, 0, 156, 0, 7, 4, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 11, 0, 0, 12, 0, 13, 157, 9, 0, 158, 159, 0, 14, 0, 0, 0, 15, 0, 160, 0, 0, 0, 0, 62, 0, 2, 0, 0, 0, 9, 0, 0, 6, 0, 0, 0, 0, 161, 162, 2, 0, 1, 0, 1, 0, 3, 163, 164, 0, 0, 0, 0, 7, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 59, 0, 0, 165, 0, 0, 0, 10, 0, 0, 0, 166, 167, 168, 0, 11, 0, 169, 0, 16, 12, 0, 0, 2, 0, 170, 0, 2, 4, 171, 0, 17, 0, 172, 0, 0, 0, 18, 13, 0, 0, 0, 0, 63, 0, 0, 1, 0, 1, 0, 173, 2, 0, 3, 0, 0, 0, 14, 0, 174, 0, 0, 0, 0, 0, 175, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 0, 179, 180, 181, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 182, 0, 183, 184, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 185, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 186, 22, 19, 0, 0, 0, 0, 0, 0, 187, 0, 0, 1, 0, 0, 20, 188, 0, 3, 7, 10, 0, 0, 1, 0, 0, 0, 1, 0, 189, 23, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 16, 0, 0, 0, 0, 0, 190, 0, 0, 191, 0, 0, 0, 192, 25, 0, 64, 0, 0, 193, 0, 194, 0, 195, 22, 0, 0, 196, 0, 197, 0, 0, 23, 0, 0, 0, 60, 0, 26, 0, 198, 0, 0, 0, 0, 0, 0, 0, 0, 199, 0, 24, 0, 0, 0, 0, 12, 0, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 200, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 201, 0, 23, 24, 25, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 202, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 203, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 204, 205, 1, 0, 1, 27, 0, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 4, 0, 0, 1, 207, 208, 13, 0, 0, 0, 0, 0, 0, 0, 0, 209, 67, 0, 0, 210, 0, 0, 211, 212, 0, 0, 213, 0, 0, 0, 214, 68, 0, 215, 0, 0, 0, 3, 0, 0, 69, 0, 0, 62, 0, 0, 28, 29, 0, 0, 3, 0, 0, 30, 0, 0, 216, 0, 217, 0, 0, 64, 218, 0, 28, 219, 0, 220, 221, 0, 0, 31, 29, 0, 222, 223, 0, 32, 224, 0, 225, 226, 227, 0, 228, 30, 229, 33, 230, 231, 232, 34, 233, 0, 234, 235, 6, 236, 237, 31, 0, 238, 239, 0, 0, 0, 0, 0, 70, 0, 2, 0, 0, 240, 0, 241, 0, 242, 35, 0, 0, 0, 243, 0, 244, 36, 0, 0, 37, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 245, 0, 246, 0, 1, 38, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 36, 0, 0, 247, 0, 0, 0, 248, 249, 0, 0, 0, 0, 250, 1, 0, 0, 0, 5, 2, 0, 251, 0, 0, 0, 37, 252, 0, 41, 0, 253, 0, 38, 254, 255, 39, 256, 0, 257, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 258, 40, 259, 41, 0, 260, 0, 261, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 262, 263, 0, 0, 264, 0, 7, 0, 0, 0, 43, 0, 0, 265, 266, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 267, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 268, 269, 270, 0, 0, 0, 0, 0, 0, 0, 271, 0, 0, 0, 0, 0, 8, 0, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 272, 0, 0, 0, 0, 2, 0, 273, 11, 3, 0, 274, 45, 12, 0, 0, 13, 0, 14, 5, 0, 0, 0, 0, 0, 0, 0, 275, 0, 0, 0, 10, 0, 0, 1, 0, 0, 2, 0, 276, 44, 0, 0, 0, 277, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 278, 0, 0, 0, 279, 0, 0, 0, 0, 280, 0, 0, 0, 46, 0, 0, 0, 47, 0, 281, 0, 0, 0, 48, 0, 0, 0, 0, 0, 282, 283, 284, 0, 49, 285, 0, 286, 50, 51, 0, 0, 8, 287, 0, 2, 288, 289, 0, 0, 0, 0, 8, 52, 290, 291, 53, 292, 0, 0, 54, 0, 4, 293, 294, 0, 295, 0, 0, 0, 0, 0, 0, 0, 296, 297, 55, 0, 0, 0, 56, 0, 0, 57, 0, 24, 0, 0, 25, 5, 298, 6, 299, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 300, 301, 3, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 302, 0, 303, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 304, 0, 0, 0, 0, 0, 0, 305, 0, 0, 0, 7, 306, 0, 0, 0, 59, 0, 307, 0, 0, 308, 0, 0, 309, 310, 0, 46, 311, 0, 0, 0, 60, 65, 0, 0, 0, 312, 313, 61, 0, 62, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 314, 0, 8, 315, 0, 0, 0, 0, 0, 63, 0, 0, 0, 0, 66, 0, 0, 0, 2, 47, 0, 0, 316, 317, 318, 64, 0, 0, 0, 319, 0, 0, 0, 320, 321, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 9, 322, 0, 51, 323, 52, 73, 0, 324, 53, 65, 0, 0, 0, 0, 0, 0, 0, 66, 0, 0, 325, 0, 67, 0, 0, 326, 68, 69, 0, 54, 0, 327, 70, 328, 0, 55, 71, 329, 330, 72, 73, 0, 56, 0, 331, 332, 0, 74, 57, 333, 0, 58, 0, 0, 75, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 334, 59, 335, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 336, 0, 337, 338, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 339, 0, 0, 0, 0, 0, 0, 0, 0, 340, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 0, 61, 341, 342, 0, 0, 62, 343, 0, 63, 344, 0, 64, 345, 65, 0, 0, 76, 0, 0, 346, 347, 0, 0, 77, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 348, 0, 67, 0, 0, 0, 0, 349, 350, 67, 0, 0, 0, 78, 0, 4, 5, 0, 0, 6, 0, 0, 0, 0, 3, 0, 0, 0, 351, 0, 352, 353, 0, 0, 0, 79, 0, 0, 80, 354, 0, 0, 0, 0, 68, 0, 81, 0, 355, 0, 82, 69, 356, 357, 358, 359, 0, 83, 84, 0, 360, 70, 85, 361, 0, 362, 363, 364, 86, 0, 0, 0, 365, 0, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 366, 1, 0, 4, 0, 5, 0, 0, 6, 0, 367, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 87, 88, 74, 0, 75, 368, 89, 76, 77, 369, 0, 370, 371, 0, 0, 372, 373, 0, 0, 0, 7, 0, 0, 78, 0, 79, 374, 68, 90, 0, 0, 0, 0, 0, 0, 7, 0, 16, 0, 375, 0, 0, 0, 376, 0, 377, 0, 0, 378, 0, 91, 0, 379, 380, 0, 92, 381, 382, 383, 384, 93, 94, 0, 0, 0, 385, 0, 386, 387, 388, 0, 95, 96, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 97, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 389, 390, 0, 391, 0, 392, 393, 0, 0, 0, 0, 98, 99, 0, 0, 0, 394, 0, 0, 69, 70, 395, 0, 0, 0, 0, 0, 0, 100, 101, 102, 396, 0, 103, 104, 0, 0, 0, 0, 80, 0, 0, 105, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 397, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 106, 107, 0, 82, 108, 0, 83, 398, 399, 0, 0, 84, 0, 8, 0, 0, 0, 400, 0, 0, 109, 0, 0, 85, 0, 401, 0, 0, 86, 0, 402, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 403, 0, 0, 0, 0, 404, 0, 405, 0, 87, 0, 406, 0, 88, 110, 111, 89, 0, 0, 112, 0, 407, 0, 113, 408, 409, 0, 114, 410, 0, 0, 0, 0, 0, 411, 0, 0, 0, 0, 36, 115, 116, 0, 117, 412, 0, 413, 0, 0, 0, 118, 414, 0, 119, 120, 415, 0, 121, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 122, 123, 0, 124, 0, 0, 125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 0, 5, 6, 1, 1, 7, 6, 8, 9, 1, 2, 0, 1, 0, 0, 10, 1, 5, 0, 5, 0, 1, 7, 2, 11, 0, 12, 0, 13, 1, 0, 1, 6, 7, 0, 14, 15, 12, 3, 7, 0, 16, 1, 7, 12, 17, 8, 2, 7, 18, 16, 19, 3, 0, 19, 20, 21, 1, 1, 22, 23, 2, 24, 25, 1, 26, 1, 7, 8, 27, 16, 0, 28, 2, 16, 29, 30, 1, 4, 0, 0, 31, 32, 8, 1, 33, 34, 2, 1, 0, 1, 3, 10, 35, 36, 18, 37, 38, 0, 39, 40, 8, 41, 1, 42, 0, 1, 43, 44, 9, 6, 45, 4, 46, 47, 48, 4, 8, 1, 6, 49, 50, 51, 38, 6, 10, 0, 52, 0, 53, 54, 11, 5, 55, 56, 0, 57, 1, 19, 0, 58, 59, 60, 7, 61, 23, 62, 2, 63, 3, 64, 11, 65, 66, 67, 0, 0, 0, 22, 68, 69, 70, 71, 72, 0, 3, 73, 19, 0, 0, 74, 0, 75, 76, 7, 11, 2, 2, 77, 3, 0, 78, 0, 79, 1, 80, 1, 81, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 12, 0, 11, 90, 14, 2, 91, 92, 93, 94, 22, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 26, 6, 14, 2, 24, 16, 101, 8, 4, 102, 2, 1, 1, 103, 0, 8, 104, 105, 1, 106, 107, 108, 109, 110, 111, 10, 0, 112, 22, 16, 0, 0, 8, 5, 1, 113, 27, 2, 27, 16, 4, 9, 114, 5, 2, 11, 115, 29, 116, 117, 0, 0, 18, 29, 2, 118, 6, 1, 0, 3, 20, 0, 4, 119, 2, 14, 1, 0, 120, 121, 49, 18, 7, 3, 26, 122, 1, 4, 123, 124, 35, 125, 9, 126, 0, 6, 127, 128, 129, 130, 131, 132, 31, 32, 133, 134, 7, 10, 135, 38, 12, 11, 136, 137, 13, 0, 5, 13, 138, 139, 140, 10, 141, 6, 142, 143, 144, 39, 24, 145, 146, 147, 35, 148, 2, 7, 4, 149, 150, 0, 40, 151, 152, 0, 153, 0, 154, 41, 27, 42, 155, 156, 4, 157, 49, 7, 9, 158, 159, 11, 47, 160, 161, 162, 0, 163, 164, 26, 0, 165, 166, 26, 4, 1, 12, 167, 168, 169, 18, 170, 171, 13, 1, 172, 173, 174, 31, 6, 0, 26, 0, 0, 8, 175, 2, 29, 34, 11, 3, 4, 45, 1, 176, 14, 177, 178, 9, 8, 0, 179, 180, 181, 2, 182, 183, 24, 184, 29, 185, 41, 2, 0, 186, 187, 188, 38, 0, 15, 0, 2, 1, 189, 9, 31, 16, 190, 191, 2, 192, 193, 51, 194, 27, 195, 196, 197, 1, 0, 198, 199, 9, 200, 52, 3, 19, 201, 32, 14, 202, 203, 3, 204, 0, 15, 205, 53, 206, 207, 208, 1, 6, 209, 210, 211, 212, 213, 3 };

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
            final int compressedBytes = 1375;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtu5DYQLbLLA6bhBWcwi5kdg2lkbeQEDOBlFn0EA74IEy" +
                "BAEGSRIzj7LHIEA8kBcoQcIUeI1PqYalESJVFsSqoHTNuNltz8" +
                "vFc/lTTP7Af4+RPkr++4+XzW70B9p08n+Tc8GvjtP45nYPx4j/" +
                "qvbz+cXz6rE6iP8N789MfTL9//CQQCoQ2Z/cOuD1n1i7r9QL+A" +
                "ASEAMv0jN58kZPpn+pt8ZI8AD5rjP+wryPV/UFzCezhJ9Zj9uP" +
                "v1ydyT/m+O5377/S/Z77RR6e/3TH/HWn+nTH8vmf5kpr8zY3jR" +
                "31OhPziR/ggEwhVEGXdUIUgVf3DIbEzxAebvVHmYEmpDs8fBAw" +
                "RRhEAgEAjXjtPK29seRRRZOy+z9sKbPkAI//l8R/U3QgJgF/IL" +
                "SwwX/svyvZDXemFJ1K/s+sdhr/rh5QtiabRYOyLmjvMS2L+6/i" +
                "jt+mNe/4C3+sel/vilrD9CgvVHe6k591xeSUYnJejrvRnMKEPo" +
                "x11/r/j/cOE/1d8JtyieKFoYb/3eqn5v24+jy3+eKv9Zf79a4v" +
                "vb81fX81fpXr8Iwn/RkT8266+w0frr+hOgnvr5NS2Kw8LtH6aw" +
                "APu5fpBi5I2JcUT2sITQtSRe9UMIWD9cCXmTxUD91bN/JoH8v6" +
                "t+55Bto34n2jMof7/rYPMyCcLQ+CPUH+VYUq9bv0H4N1B/jdR/" +
                "Nl1/mn0NRuZkEtl5TOdVII2A8hXQgHm5/Om8tAqvH8765WKysx" +
                "ONUXDUfV+Qj8JYvZ5tjxFO/9WMEbP0q4O/fHP8hWL/WHv/oLl/" +
                "stq/1/b+9dff1lh/pvrNmmHql9LHuVm/AH9GxXCi1yfyTgs12q" +
                "RtPf6MYxSw7yCZv4raWijgymJf4eTYjxkr8WgRL3OSahSp+4IO" +
                "5/jua/YzrIeD3aKIhwmGVQfJoXnceeoljjdDaUAo/4esHhI2j8" +
                "SZHFJkf5qkRKfAWbXbyrFMOtj5iQSiw4wJFo8lHNixmxBQdRkZ" +
                "0bdZJqgGglrSsEn9a6Ckn8HawHoEJMNFlDhfp2pMnK2BMDVJ4X" +
                "7Gum/3oujAO9bTLlLovoHzG26KTtFIfHSSBR00MLJlSBeKX9YZ" +
                "UybkIlBaa8n6SGjab8S1FWGwC0SZ5jryJ3wbF44wYlu4QC5GzB" +
                "en7yKFMRsxDtQUMhWD/Zv19adDz/Unuv6zi+CG0RYs7PQ6+nev" +
                "ZbXJ/s/t82/y+LcbqPj3PxwS7n9YGejOP/L/a7Q/bFWyWrp/M4" +
                "yKRedg4/bf3rjUMDj/rSc4U/u3b8d/wrLgzdwjUNCgaWG35P+c" +
                "/evg7l+XXv3r3Pfq4BqjWJUQZXaQBVBdeJb1N5HuH5llf/zvvy" +
                "D7kx7a/d9Pdf/34bLBlzcD/d84k+mN/glj205jW220AqK04hh/" +
                "LlL8tYtSCYFAIKzASvn1L6rKzQuys9HTxq0vNjYzRWUvgupIJ7" +
                "GiZLj7Fxfqi1ab3jl7/fnA+gst+tY/Wf3FhQn1h8TkD1e2//Pt" +
                "476LCbb9BNf+5ZzM7SfAOvQb2QQ018/lv3JNN9YPll6/lGqvFC" +
                "wSksVA/sOv7t9S8SjtLWFsH2v9J6Ou6iyXIb+f0vW3VWce0UU4" +
                "8szNf6I8/yVGFiL8si8dK+BT5AsJBG/9dkTg2nWk7+VG8l/r3f" +
                "zRKTu1A8xB+NUTkfQnrfjdJPH8KDNHAtIZws9Tyqbj7/T2f2fG" +
                "ewP5A8E3u3WVEkI//096ns9TUkGa8Hr+He9WYqzn58VxFQQCgU" +
                "DxF8VfhFixkLJDuDLgG1WC0OTrCTvSjBh3Zrf9Z172XzfVVNx/" +
                "pTsSwKQfAUH1/ikYmb+r4pSKIP8DxRsWLw==");
            
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
            final int compressedBytes = 1218;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTtu3DAQJRU6YAwXDOAiJY34ADkCA7hw6SPkKHKQIkWKHC" +
                "VHyBFypEjr3ZW0S0n8DYeU5jXGGuLOcP4zorSMTSHbhlUI5Xyl" +
                "AeKg8WHvkgkxy6ocNMN2BJ726wwst8KB5/bNDloAfnjPgJxjS0" +
                "/ZUN4eAyx/1Qz230744iexaYubmGJtN6ujioPEREf0JCTNGj1y" +
                "IqN7SfHvnSjF7UhqRlxYxvagsOKBhT5HoE8gQGVUQsp8IQJqSg" +
                "Jh0xAL/RGUZ6j5pswSQM8ll4iu357n6zcGXL+hxh9L/Xrvuv8H" +
                "CFPztBSH+ntVf/fsRo67xInFNd33K0vxaMQV/RbHfgiEcM9TC/" +
                "4jXO1X5GGWaujNyS9F/K68yMo3P+Lkf6nrj9j6oXao67p9GSaf" +
                "/nQF+//cmYzsrOeV/RRN+0mx90xz89iz/tR9rWnEC+fs9k6Yd7" +
                "pR7CN7ZPqp+3Pz61t796e0+KXTxW/FCI7xd8/xp+D81Uw7eCCD" +
                "lrMR8GaVJECApJ42Ope4FyV7FLYB+U49dds1tJSzdl38B2qb5h" +
                "fUfxJ2ZJcN2T/57/bLNOF6lQIq4oQ1KzueX2wi1xew/zWhiCAF" +
                "cjZzUlowAiTatYBptr31vPuPOn+xjfMjwtep5bDS2O5fmf6Kw/" +
                "5fDv9/7ff/47T/Tr/m6v5xIUFF5yjkVug3iPQTYV9HJ/PYLocS" +
                "cGz9Qutx18fWbzD1q3v9Sfqn9Zj9U+309y7/iPVJzp/t2//w5y" +
                "e0Htd/vZGaPs7zAw/Z9quXtoDUEifDhfyZTf7sKH8GMD+JtTwf" +
                "+wnkf17/fvSlwZBfCXra+vxh7D9srP/jhz4PT+yP0fNHlelvIX" +
                "4sxE+n9YRtA7d+8MufIfnfLf4xtPiHLf/q9R9QP0kvLsq2nwxa" +
                "CO+ffrs/P25s/ZPq+yN1nCScZc0+2FTYhumXsKr/S3HywRls1t" +
                "Go7OYHN7+LPv+SYL307J4UvP9Hnv/BOn8kE+1fDPTFJX2exymz" +
                "yC9+fottP4nnn23G+FVy/udzV7rEr5z5g/Ivsv6rrj+RW3OXdI" +
                "UbL7RX/5b+/GPyPotahwiYNP5rsR/tbD+iqPmlnn4AdtY2r2pz" +
                "0k8AHmF/b7Xwhf19nYtf7+HtT3rsAjV/J3MjzQiEmPpRLdi/yL" +
                "CehayPQoL+PUv5Dtd/R89PgO1Ph7AhK6pTd9y/7QAQSVle1jcG" +
                "hw9H+hqcfjAK+P0W7OdXql8fm7+wn9/Gk98G7v8VcP+lyOfHqr" +
                "C/guNHFfMXQtEVnlwqlto0nmuP38f3B4h/Q/wevz/guY/fchxD" +
                "I6q72OoQGn+t6QGmmkSmT8jm76oqbkubX+SgH79xmS2caWT6Hm" +
                "jqsoWr2cPef5aOfpYPNwaT/GOt11TBqQzkX+dK26H0+Tjaxr2/" +
                "LFH/gplt5udnhwHEwD/E++uqPD9WELD1RyAQCPXGz1TvL11tet" +
                "HyXxD9TPVLaywyezhRmR0d6J6rL/b6dLE9Abg/YdwnHCB3R4yN" +
                "ZEb6BAKID800n2VxqlNuD69ZLpv/Zfrc4/npc1JRUPknMAuny9" +
                "/36d9fV7v9bNt/5uxf2OxfTe2fEByPoG4U0dOJIS5WCKjCTtR/" +
                "Ova/8P2zLFKx5/Pz/wE9Rch7");
            
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
            final int compressedBytes = 886;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXV1u3CAQxohGqIoqKuUARMpBJm95zBFyFFr1ocfpUXqk2t" +
                "711o6xF9uY3+97SHZXmcD8MgNjljEPMCwhfJ6MvPxq7H8tGFPd" +
                "T8n09RPNuB6RUP+m+dn+Y/GV0Y2QRE8xpdeb6YEpGojAF4T15Q" +
                "Uc4oH8V+YHjKDLNtJD699u+1Hz8c3u8fPVvx/5nw2q0H9kRv53" +
                "HOrEDLx2+3eRghxzM+b/+qaTacv/hKae+qHG+BM/NVAjW2uC+k" +
                "POGa0Mzae3+CnCuuVs1Djj111/5LH+ov6qWf6ws6ztl8633/37" +
                "D1hoKnRIXQP/HPYDcwdqAI1N9Kjf42gwbjrFoSAAAAIFHLLVb9" +
                "RtY/b123v/+Y+ufvs11G+mXXJOrN/kNkqP/Vsjejmn7/lvLvy/" +
                "dfTnb/XynQWrr7Fjjg8AU7g0B2o3Ip/WrKdvCIrK2H7Af+iC59" +
                "P6+xH0/MPD+Ytc3H8OUak5CYHKHd8Ck7eXZj79bYn8kv/pZf9T" +
                "N/9D2zIA+AnRAAAAAJBR/ggAAJJYsA4AtSLt/l/4b9KWU+jzF+" +
                "Ucdmln/fE7+pMB9be/eDG11k/VdjZS5PHx/AzsAwAA7B94zB9i" +
                "5x+1j5+WNUH/QMj8VTS3zGl8x4ieX2eTwvzlsr/ZuOOqeP1lWn" +
                "80DpOllCR/2T8xQ1zXT677X88x9e+bf3Ye/zoH/5Nus6fhExXZ" +
                "/5ExZRTUY+uXwrEKAMfWn+n5xevS+vNQxP3HZYAvvEZVuNHM9p" +
                "ja9YlsGOtxGIigGPnLDZEG538AkBxk5fRh5Mcn8U9ZEgttCZMU" +
                "oF0lEflr2A/mX6784t7fsO5hci2fMrsl6EwPAMCJeL5VfoZxIx" +
                "V7YLqhl2u2odoQ9N6+/PbIqOnOfL8PoUL8JvP4B/KLgXOXBXF3" +
                "hYjRJu0+fl1nIR6fH9l+okrwRSBidlqE/x67//be/bHi73//H9" +
                "PP749NoKFLwn6AaOsfAKQR+jTmHxBky2btqW0JzzTrtfjJ9/X/" +
                "+Ot/l/h6EeDY+n9C/3i6/fNrex7Ecmn2OrZn88S+DGyaWbTmrf" +
                "KURXkkiuG/ev8VtsWZrH5sPPtvmPiTbypYJihVp1WbgkomebqO" +
                "TB+J32K2J9JnJF7/RcH7V07PXy3xb5Lin47oj6zzfw32/TGSpf" +
                "z9MVnEH0rW/y30KrX8U58Z//4B3peMrQ==");
            
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
            final int compressedBytes = 886;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXctt4zAQJQnBEHLiIQUwHWwJs7c9piQl2AK2tJSUSLG9dq" +
                "wP/xyS7x0MS8B4SM6fGtFChGEQQs+fozDnO0ao81c5f9ById+F" +
                "mIYnQVdCGhaKFfpnW/qXH/STB/8YeBbDuHZ/4a++xqfvbtOF/w" +
                "6kWCHZk8J4O5vb+Z8vlrV5uqexnb+x0IIw/nb0wwp9qP6Fzj8H" +
                "fysrjGe/Tva3jbGU/9ARxu9of0Xl989efuQgPyAUelOhKC9zd7" +
                "PMhtL+04I/FeafNH7VjkGz15/u5deM/sleIxkFrlR2egBg5Ckq" +
                "19/pOK1ErAGAFsK7ylz/0Vr+THOdujjR1+X+25w//73kz1/+iF" +
                "Ln7+Nj/r7wl9/8/8z8Y9fSiZwrfGJPUNLH6gEAdTkAoKbB+gEJ" +
                "CwxuGDefv6r2JeP7/ILIvf8iRf3WjBRyp0nMdFsFEu7QT0lKbJ" +
                "XE/tSD/fxetz9Bp47sh1z8V6T+pdL5B/noz43/pYz9Z+Xk59b/" +
                "Zd3/5Nx/WSoP4tF/CACBWjxu2YpZtS6lefgfHvXPsNPXt2HlOj" +
                "J/1O8A6reg+G0qjN8c8g8DZ1Gt//TXn1Pg+z8nqArw00xMnaam" +
                "Nr6vYoSogbzIXn/10T8OSwa8NOTbHiYR+fmftKSfxN77u/b00P" +
                "+m62fk5zxrSNXx3AGAx/4Hnt9xyIeP3h8ZPv7L77b/6fL+yEQr" +
                "Q3+5ZjlbqZuZf/UX4/mbbf3VV/213Dbf0/Od9RPO61cAvuMXkc" +
                "afh/+6BLUAGotf6F87AlU9elX5qqpkVuDbf2HaVxrf+h8oBpy/" +
                "Ahz5o4aMtl//g/2HmOtnsH6O9TvmD/uF/vUbP1o4/z1E/yo4/z" +
                "aYv+nMfqf6/bd01iJXaMSvtIj3MCil/+Lhf9qu/wrYiXTwHBHk" +
                "7+9/sH8A8Ml/0+wfoP7G+EtkhewgU800evwpzR8ACoVThVXgAS" +
                "XuhJHRYehC3koXUXg+5+8hJgDhbqPs+eOmtaQVAKLGD+f++2T7" +
                "H9g262L/AgAAoJL4ye78YADycwT2jyvJv9rQ33r1J0H/iuYnPz" +
                "peBcv31wj272EU7Ra6/v8/cHR+J0U7v9PUqT8ZHarT+ZFAOjUp" +
                "papTQWORIfyZ5k/IvxH/UZp3kP88YkzwG8RYyGH/375bf0lRyf" +
                "sDDZmq6OtY4VD9Lb1+Uc+P88EnwNLkZg==");
            
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
            final int compressedBytes = 741;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnW1O5DAMhtMqWnURQkHaAxiJA3AEH4EjhdX+2ONwxJ0OSx" +
                "mk+ehXYjt+nx8IRpNp4jj26zR0QgAAAM2w8/4vbt8te3s0a79D" +
                "z9P4cwj0/xUKPZ0YgY9/dL9DyPHu5IM4Ti3q+x/vZnQL46cAPK" +
                "N5/mesn2H7+nFtv83xh+zZr6ulX3TYf0UqS3b0FxW0P9aPBv2/" +
                "3n/r6C/p+ZPW377rR+hnJXQwgbb4u8/6l76+cvKOsi0Ltwc3Fk" +
                "J/6z09zAQ054+h6pX5nP7lsRNHufB6fP1t1L9/PvXvIYbxN/0r" +
                "1399zA4vDL83rN8A2LB/wCv2D7oSKqa/+XF5ddlQhzSG0nQyiI" +
                "8O/zyXiPKlLNWXs19Qbr8KOw28+/qpuf+s2X516v+tlJt/6f7X" +
                "sb9tqah4nX35X57879dc/3tyoh8pgJlmgqmAq/yN+q+4fuUF+i" +
                "dddqrObWCvNsxBuL00A+JB4QDb6L2raPz62D9uuX6ggvkf7KGf" +
                "7fovzl/K7v+eab9o/+e0fW50/sr3s3T+5GLjl16/Hvy/5Tz53f" +
                "4E/QDUxT/s/wFgSP9LHTKRPtxi+3ANyUkd7F8BACaa2n8j4+Nf" +
                "uH+24vxS6fqbTY+/vH6zm39xfrcF/dd2/hCKH3Evfd2Yn7eLwP" +
                "mXzGcu/eGY+cpSoNGnXgrGf7q8ftIUv5spO6Pz60vrJ931h3f9" +
                "RIrH7+H+Z/v6IZqNXzh/Av8FNQWzcHsAWo1/yf00IVVZw/7zT6" +
                "wrd9P1r/X7T9L9R/72Xf9Y97+nKUnk0OchhR+BOn6ehtaH18Ov" +
                "D/eBu9Cn8Pg5zPiX8/37VQUV5zsGnr+I+CHT3sP+PUT9YpTd/8" +
                "f8Y/4BuJLFoJ8AAG3ox13zr7z+4WL1B54fvCMOE6eH8+sG65+m" +
                "vr8U9sPzL1yAm6tN1M/ATvxTGn+RfxTVb6R4/qzrX/z/hu/xA+" +
                "gPedW9/fvTlOUfN+sf8af9+E0YPwBl+Af/Pt0l");
            
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
            final int rows = 23;
            final int cols = 16;
            final int compressedBytes = 29;
            final int uncompressedBytes = 1473;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjYKA9UBhg/QxD3P6R7v5RMApGAQ4AAGRfAIE=");
            
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
            final int rows = 45;
            final int cols = 107;
            final int compressedBytes = 3957;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXAmQFEUW/ZlZZ4+c4i4ghzjAcAsIOgoKIgiKwshwiAioyK" +
                "kIDIfHsiCXyKoIKLAgri4gunKIgCCODIoiiOBquOqyCwvihgoR" +
                "KojEiqP2/jzq6q6e7plmZiDCiuiurKyjq/LVe//lz4yma4EAAx" +
                "ssOoDO1sdCJagMNaAW3QR1oSFkscdJdeMQbQktoRWbS/Lt2pBN" +
                "a8KT0IF9DZ3pQCMXekAv+hL0g/7ke32ANp2NtqrAnXAHjIBRRg" +
                "5QsgDO07uQUyyTPAvV4AI6kz6j5cJ35kq4SNsIhexBqE9zoRGZ" +
                "C02sPvYp8gw0pyuhNVwOV7L7aH+4ls6hHclyuAG6s4/J9XCT3Z" +
                "QtphNpH7YQBtDeMIR+D8NJNXKcTYMIVGGmnkfmwflQFX4Hv6fH" +
                "4UKoBxdDJp2hdWVjoRn5BFrQ5foBaAuX0Ra0lv0DtIeryHPkmN" +
                "YTOkE3cwedBTlwMz0JC6A39IFbYSAsgsG0NgyDkfpq0ECn2WCA" +
                "Sb6hgxmBDKhgHIaKUJ3sjzxFh0Bt0g3qkJPsU/Of0AAaQ1Mth3" +
                "xJOpKj9DZ6M3kKLoFLoQ27HK4whkM7uBo6Qhe4Drrqa+B6uJFN" +
                "h54QhVzoC7foB+E2GAS309UwFO4yRtDGNIs2gtFaXzrANG1D6w" +
                "f30CbRqDY5igttyLGKRo15fIt9S/IjW7C2JukSjVr40UZb50Xd" +
                "hbaxjltdEavznRojB2tb47Ff0EytCXlW1prdjOXaFLG/spbHfs" +
                "T1WrnH6ptRPepbOFbijOv052WNni3qF2PdXWwhXl1gFY2Salib" +
                "H41b6PHA9cZiTSv8vKsfEXtbOHv05d5R2g6x7yTp7D/XGMW/7Z" +
                "pi33p19cFybT8pti6LLKKn1NGdjbZWf1V+lkTpR0YrcUwz74qm" +
                "7r++vsYpkWGq5qC6nz5wN15lhDaXdnZ4pc2He2EMVKZdYAKZiL" +
                "x6ALLIeFLdXsgugvuglSl+h/OK5EGHSBsYB+PtvtADf7U755XR" +
                "xf7RnkwewGM4ryZxrIAaC8Q9Yq3kVTQa6ablalgLFxmF2H6IFe" +
                "cVbjcRd7gCmmMZeYVtUGB8Emx7cr14yiZyS/JK1HOs3saaKs6R" +
                "UFVihbzK47wyNWMF55XAarl+jPPKmG9gK0heYZv0hLHQDffOEr" +
                "/xCO6ZyHmlcKmN2yPFlXW6xcMKMvA5kVccq4yBrA7UtnZxXokj" +
                "G+AHeUXrKtTW0es4r9x7FLzCNfLKbsB5heWeZLKDlcur++Eue4" +
                "h5o08Du+t9HQ00OkoN1G8m7SNfaIu4BtKrjGv1XMSKX7EDbQ+d" +
                "jU+x5Gqg8aRdaE8T96A00O4HlLYG5B5FZhrXcKxMC0vL9f4cK+" +
                "tBjhXUt2Z5WOFaYYUaOIJroKpFDRTrP0heeViZc2C4YM0RroFq" +
                "j9BAhZXSQM4rjhW0YEP1bzlW6ljESpU6SaykBuK20kAscVRQAy" +
                "MbhAbmcw10sHI0ELEaxLHCujrqekIDjZV0gcvznlIDcd8VDlZ+" +
                "DeRY0avDNBCxawZ/dLBiu9kuKxuxegimGB8jVlMhy5xPepjz9J" +
                "acV+LXp0E2TOZY4Wec1S9yJWI1nb3HsYpGM2pEDrhYzZC8Um9U" +
                "V3xTF0heCV5P4VjhOs9sLs4IwQp5tYf2j4Ys7DsHK/wMsU+o2u" +
                "NF8UpqIDSzDuN6qf5dPFbGKskr1aong7wSRzq82h7kFX4jr6zP" +
                "8coxWElemYPcO9+TiFf47WB1OMgr1MCZAqs28DjMgkfhMYPade" +
                "EJPHYObaIXIq8egXlaQ4PxeKW3FOf+Yi60M53fsevx78g+fxtm" +
                "XBg5aBCfxufAbFWaxXnle2qntQv1X6MJF7sgvF4KvQHiCq4G6q" +
                "ejSRYer8T6Hf3baBoLfSf0butzrPw18LCIwq3MO90zm8ElRV1Z" +
                "/wnPirpY/UldZ654vxvRLKsCPAfPiKvi2wF/oeL9hmXcW2ArVC" +
                "/q2hm1A1vDIocC8TgHnlZxFG/AquTUW5d6WPF4lRCrPQnafLGv" +
                "Pbx4dSQVrCKoR6wwbK/VOr4Olga2lAOhn4WdH0GHod8UOH6JaO" +
                "Mcc0hqWHFeeVhhSbkx+KtozQInXhk7kVcrnHhl3yDjlbZE+MDt" +
                "PF4Z76FnzwThxNBbbMR4tTvjCh6vtD+LeLUrY0fGNf54JTWQxy" +
                "uzkrFHxiuPV1wDpWd3NND4MBivIsjduHh1U6wG4me4wOp4bLyC" +
                "lVhS8crRQKxpoaGTiNdAq62MV1iD8Uqs4+IVfvN4dUDGK3EMxi" +
                "uugVCd88p4Kz5eIVZDgxoYFq88DXQ8u7FXaiDWPc81EFaTpdoq" +
                "WAUvwt8ir6G3WAMvwXrtBW0lvABryTL0Fk9HCKxz33R8Zm0Foy" +
                "6TRsTwbFOQV3j0865HXeDjuhuHZLwKXyIdU+WV9BapaqC2JpTF" +
                "41PWwNOJNNAIuQctx1d+MfnVHR/oe8aX43kVaefwSnj2hsKzvw" +
                "AbFK++kbzStnLPnjGO80r6QPwWvBK/9IDHKywpXuEv7bEqebwy" +
                "R0leiTtJzKvB4bxyn8HlVbgPhFfCeaXvD+OV4wOT80qzisOrAM" +
                "rTk/OKTI7j1UbOK7LJaBppD/mwhew2mlsVYStshtfhNS0fBEPI" +
                "TvIRfu8V5e0knyDD6EME24Bsw8/bxBdlyYekgPydvEw2uDWvqv" +
                "U+spns8OKV31sE3qg9xY/zPl5NS5VXRuNoWot+QcIIOy8JI6en" +
                "8ETxNa9KXrEvHV4ZzeENcwLyaps5Xp8G2H+DLLw6EfEKPbs9he" +
                "TzvrCMV9yzS17hvc/0eOXGK+HhgOrq7gzEwceriR6vPM8e5FUK" +
                "TzUgVgNjPbvgVV6QVxKrUF75PLvYTuDZ9Rq+Oteziyu/Fe/ZAy" +
                "jPSO7Z2VfqWh6vtnNeGQX6UFKT1MLSTuNLWsUSjptGdPWO0gq0" +
                "KrUN1Z8g+VQwn2J/1kJ1s2epOxDO3MMqcHeIFa0osfJ5Lj3sWI" +
                "lViXl1pMx4VTfRHuOtJGfOSH51DytXby6UvMKnFbyy51iDaTWe" +
                "Y+JYYW1dUgeyHKxkX5hjxXlFLehgLIZxpLbklT9eubyaJHklsK" +
                "0YyyuZY/LzSn/izPGKzEvCq6Np8qpeSXklc7dJ+sJXKz2d7fLq" +
                "fskrJ29hP2qcsI7Ra3jegmRyrHjeAtv5WkcDOVbBvIX4vR4wPR" +
                "SrGUoD54dpoMwx+bHiefYywypdDWxXmli5sc/DaqbEio5z49UP" +
                "dCwdDZVJA+snHq/oSPSB9WkeHSWxiuSit2gI2XQEvQc60DE8Xl" +
                "mnRbxaWwRWofHKKjyn41WH0oxXrp668YpkBXll7EQOTbAIYtWY" +
                "xyvJK78GZnSSGkgacQ20b3G8BWmir4vHijT1sIrXQB6vyg8rMz" +
                "NNrIaVLVaSV4hZa/y0gsdE+VL7KJG5P/QWIPwnfSqFWPtywtac" +
                "rYe6VO4DS744eYsYb3E8VW9htkrTW4xM0fc8HO8tis4HxpwfyA" +
                "eSy+Fx0oa0jckXgO72VYrOB1qiB6+vLy5WVmH0DC3FzQcKrLLT" +
                "xGp82WJFHAVQeXZRfiLSFuZIXpGrJK9I+xTu/ZViY6WfWayK59" +
                "nNzDSxmlQuvNrEqrJKpDuWdhsn8CkmkBuQV0swXi1LlLfAUkze" +
                "Qt8QnrdgleFpjpXMWyTXwDTzFqXQvwrPs5tzUzx7SfGwknn2aI" +
                "I8O65VPhCf4AfS08kHCm/RkHt2JmZP8HygOt7LW3RGVFfxfKC+" +
                "UY41Bnygygfq08GdkxHuA718YOy4sNgKGRdONR+ovEVcPjChD0" +
                "wxH2guCcsHqu0i84HSWxSdD4yqPDuuY/LsrAa+z714nh2fpRbJ" +
                "xa0+Mm8Ba528hZdn9/IWgTt4tQi9UD4w7h2/4NzVQHNZidVzRk" +
                "nOIr3Fd19yGxlAbiH9ya2sN+uhEInJMflyj+FYbSk+VmdrvGI5" +
                "yX/T+kcZY9VPPOlqUR4oeYUlzvX1wlsMAjETjMcrh1dOvBLlbb" +
                "47eM33rKPiscKjY+JVgnsqo3jFlqb3ftj7y4VXcuSD9413B66p" +
                "eJWit9iawFvcLbGK9xbli1W6GmgfKGOs7pBYOd6C7GaT2PhYb8" +
                "GxIqPIXu4tJFaQzSaSV6AD2QadOVbCW7wu5t26WDnegk2Q3sLB" +
                "yvMWopXjvIXEqvS9BedVOt4i8lgZews11giEjCbjyBgHK9wjsM" +
                "J1Q49X0NLjFXtU8kpihcf10vPjeSWx4rzysErAjPrhvArHKoxX" +
                "HlZuLbpXcq8q1wvyClok4hV0UmuFldpysRJbmvh2scJyBfEdmj" +
                "OAxmG8crBSx3SJO0th5baLuG/ykNjnzi6GrWQqvM410D8urM5w" +
                "NZDNd+KV0sA31BExGujEq7NNA+nyaDktJdRAMaOczERe4flExa" +
                "dwXvH+Vby34P0rxSvXZ7BFfl5JrHj/6jdepcUr4c/YemesUWwh" +
                "o6AymSXz7HwMn22Rnt2Zd+uNNXrjV3gHBfF9YfJwMM8e7Av745" +
                "WXZw/2hdEbryudPLu1Or08e+CYQJ7dH6/YS+nl2X3xSozhswK2" +
                "jfneePYmWev3gdjOefFtRO/BzxjfHWwvun9V0sXanoI+rCm+Bl" +
                "pvnFsaqJ70IPkPOUT+TQ67NV8EsSL/DRz/r9A7eLN0sErpCUqA" +
                "VbqevZywKiQ/ktO4/tlQ8zPJr9FoZLODlcE8rPRfEmLlmxHin8" +
                "+eClZFzWdPpIH++eweVqnPZ7c+O1ux4vPZE2LlzpN1+sJUeA46" +
                "NXFfOPX5gex9taeEPrB4Gph8cbHaX15Y0TRUhmyiS2KwCuRfUs" +
                "tbJMRqX5pYrSslrFaXG1bLSn4u+9DxgVID2UdQmW7OOOL4QINJ" +
                "b8F9INdAOSbizI1xfeAOzwdyDYydb+H6mhAfKDUw3AcmVAqlgU" +
                "EfyDUwNR+YMG9xRn1gSeZb6D+F+UA5N4Z9TlqT07SA/BzTGsXz" +
                "Fm8ndNWzS9tbeFilzquy8hZndgyfnQj0r/7H+1dyTET1r06l1L" +
                "9619+/Yic5r+i+9PtXSZ9lQCxWZTOPqfR4Fd6/oh/IeUwCgUMx" +
                "qhrx9a+qMp+/SjB+tat0PLvdpnR4Zbc+x3JMIs/OfnXGGukRya" +
                "sgVg6vkmD1XqBFTsZiVTZjjWefBp4prOjn0gdKrOhXaY1f7Qn3" +
                "gRory9xtMXjV7lzESo4L06/luLCWQY+WaL7F+0Vr4G+8SlMDxb" +
                "iwdr6GsZAeUzy4OFYDU5xvsbd0sLLvLCVe3X4uxis+54yqObBG" +
                "8/gxEcSvgTiqZUgrqf/AgV76ByF7A2MiRbZ2/QT1xRwTwXVgTI" +
                "Sqfkj8mEii+YFn91gj/B8HWod6");
            
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
            final int cols = 107;
            final int compressedBytes = 1621;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWgtsFEUY/mdv5m57lVKgJhAQFbRApQo+QMEKPkDQRnwERS" +
                "higSAQldqoJMYorY9EkZAovvqI8RUfQQ1aGuMDExVzNFQebRSI" +
                "oo1o4zu1URCr679ze3u7t7u9vd273m6zk8zsP7Ozczvz7ffP//" +
                "83pAVIqFDCRGJyCUMxj4IxrA6vpdi6k+zDcjdMwfIj8j55k/fd" +
                "hvlDuIx8Qj7FfteyPbx1L9lB9pC3yNt8pNWwllZIEquHE0g7aS" +
                "UfSxYJTktIpE3XPqOgGctLldoVcKXp0ysw36LIUbV1hCQJfYp8" +
                "aqI1VMPrZxY0WrzJJcr1arhG07oU8zLMq3iN8jIMEfX+EF6ONB" +
                "1xkr7OHuCt52K+QO0zx/DUdXA9LErWSb1c0uWYO7T9aCcfs06t" +
                "r5TSJra3n3v1Uk4T2Wq/bxwrSYpOlvKU4lg5nGkLVb7JOK/UMR" +
                "WsErzicgqvMHNeYe99Rl5xnFfRgzJW/fNK8zYKr4SX7M9A5lWm" +
                "WAkv2H2CHvACVvQQLx/D2YagAERe24S1YpxLlNXByShPEIYIw4" +
                "UClNbDVD7LCJyPpQizsO0OvlZX4RvshxvgRs36LYdqoZ1LgoyV" +
                "UKS0l8CJml6naOSJmMsUuRzz2TDdBk5LUrGCYao0XLmOhloYB+" +
                "PjWMHkBK/gPJim9LgQKhSpBubpRrsLFsJiTX0NL5muD99BoEit" +
                "n4R5LF/NjXA6ymfosYKz4By170y4CGbjdS5cjuV8qMRygWbsKr" +
                "gJbsb1+xxQu9FGUkWWkEVCN22gTUZeYY9mG19LR/50oBNeRRf7" +
                "SwcKXcoV2cEq1dbbNMx7jbfUCmtTnlwt3IrlOskTyRFWVXnDqt" +
                "PRTlXN8XhDN5ejONqjcR2otPwZ14EqTtz6EUT9WGFdPdSbyiuh" +
                "yGO2RbU/bQvEq1VrW7DWzG2L8CgL22J7HKtMbYucY7XCj1jRXT" +
                "SWwpGilP3KxvqFR/vLZhc+8yVWXxnWvSQFq69tYLU0N1hFa3LE" +
                "q9t96l99o/evwiMd6MB1FjrwsDd1YGS8L3nVZdCByxzowA0+i1" +
                "vc409ehR+h39NumVf0R77udwplqtVXao9XylhGXv2g3Al4lVhR" +
                "l9GtSLGOaZ1hXWRTqDVh40+SZ5IjXj2YN6zmuuJVU2SEdr+inY" +
                "Im7uuSVz8HvMoeVvQ32kt/pT30H/oX/Z3+QY9HelN69Olqv6CW" +
                "fD3jXzlGj3qJV3nEaoErO/A/CEkSiCQW5rMWdbHGJK9gaoJXQi" +
                "XZBrP0vJJjt1peQTwq8i8IWl6li93GeSVOkmO3XFe9l34Gom4H" +
                "SMZu1Rb0/ZKx26TNbha7RXleytMLdTWbsVu7WMFMzDx2y2vzeW" +
                "kSuxXL5dgtaWHD2NDwsdT/RLKjA1mxeJ8bHRjdkf4ZR/HAvPlX" +
                "4eNOnhLvt7A0ZvPyYnW9SwzfxgbJQ8lnOrDejQ5UJeRVpAsx3J" +
                "w5r0K7LHg1xpu2Rf54FWpzg5U4jo3V2oHRKVn0r7YHdmB2/auU" +
                "PWJOEitaau5fsdKBmVm0Y7Dxyg1Wss3OJtAeNjFhsxt6GGx2k7" +
                "1vjeX4B3NtszuxLezzKtvnLZzpwPh5C77S67Xthdx+perpFGI4" +
                "S0VfzOh3XnbBqx7TL+NuI69IiT94RV91s1+x6RAS75X9K2Ul0v" +
                "pX8n5lz79i09z5V7bsWQf+lcyr/PhXeps9M/+K68AKbdxCWYGd" +
                "1jowE5s97l/5Vwd6w2a38q9wvDIhmrAt2EN2zlukfcMiyRNpMN" +
                "iBBcrZRvawMqJ61jhxPjDAKp9xCwWdjbib/B1dSfosd7Qjutqh" +
                "gZxZYVlusPJbjEnB6nEaS2O5tOWPB4Xlgw0rNwntwCeSMSaT++" +
                "7iFodzH7fwlw50k9gWL/MqV1j5k1f4LTf0g+STeL9x8GHlT16h" +
                "DmxmDewZ9jSJsUb2HHuKNWVPB7Jn3enAwhkBr3TceT7N+gW88g" +
                "+Wr+TRDtwc8EqDRIvsX2H2pn+1JcBKu1+pUi7OW8Rc7ldbA6ws" +
                "WdYevw72GNNgwMp0nc3+F/5igHTgOwFW5jqQHci6DvzSpQ58N8" +
                "DKdJ362a8U2YAVlhqs2LepWKlPOsXqgwCrTG0L1uWMV66x2h1g" +
                "ZYUV+y7LOvCIO6wC2yIjLDU6EGsKVlw26ECOTrcZr7CfM17tD7" +
                "BKJvgfxwNJRw==");
            
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
            final int cols = 107;
            final int compressedBytes = 542;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2u8rA3EcB3Cfu913Nr+ywjOKeOoZUv4AP/IU5Yl4SGZS/E" +
                "8ikica8qOWCE89RlYUzRqRc11z3XE22+7cfbb3p3bd1rq6e+39" +
                "+X7ve1PV3EXHdGl6t0Nrxn5c3x6Zv63c0Tmt0+aPo5zRNh2oeY" +
                "tOVFdLjqplXGYrJZnPinZh5V4p98Z1SuSzor0vK9qws6ILWHlq" +
                "+VBID9Q/sbHSPoVV6T1uq5RcKY/IlY9y9YRc8SkpZiOY4nku3H" +
                "ugkgncKGnlmRKBpF0PVF607al9D6TD3D0wcIse6FyJoByXTVdR" +
                "3v9LrqRZ7TWPXPlxblFsrkQIuXIwV2HtCr1qr/dfr9+15d0V57" +
                "NlblUDK7Z2tRivfGtTh1yxsWqAFZt54JtotFrRBy8rWq2YXDUh" +
                "V0zlmrNzh7Axi6iTIlLINKsI6tvqwo4r1cPKAZ0W5IqVVyusWD" +
                "i1YT2QjVU7rFh5daAHlkfZ5Up0wsqDO0n9WaPowrNGDlbGnhvP" +
                "r7ph9a+jWU/5nAv3XIl+0ZcrV2KghFz1IleOWg2KETHsUg8cgp" +
                "XDXW4Uc3bGemOmOXvEMoMvau0WVi5ajcOK5b2wjZWYgJXneZrM" +
                "iuD5FZ8shc25ElPogUzc7NYDp2Hlxf2Vvh44UxnrgcxHqDnL7y" +
                "7zvQfK6cLHKznl1/GKuVW0kv5vwdxqAVZsrGKwYmO1CCs2Vkuw" +
                "YmO1DCs2Viuw4lJVnwrZkoM=");
            
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
            final int cols = 107;
            final int compressedBytes = 543;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmjsvBFEUx51r7h2PUIgOhVqhEV9Ap5OoaEVEiNjS+1V5dl" +
                "gk+w0ssiQeS+KRiBC0aiQSCZVEQsYYg2WtJbmzO4f/Se7dO7Nn" +
                "tzi/+Z9z58xQxHKN9q0vjPbo1J4PnfUWrdO8s1q0R9QeO7Qb43" +
                "tCm3RMYVp6PaM63G+OaIW2raRGBxYsoalOO0L39nhIGL/zD0dn" +
                "iFnaWHWBFVNy3S+fIuf1jMgTBSL73UOYzpz1u/8V+YitBjo90B" +
                "UbVr1gxYZVP1ixYTUAVmxYDYIVG1ZDYMXFKKIm1IgaU6PPfQsZ" +
                "UsNyTl/fQgbRt/hGJ+O/8m76gU8QV7RPWc/E3cdWISq+yYFhOS" +
                "On5RTty1kZkpPIgT5mtYB6xaNe2awW3Th58EzEaAQrnbp6W3nx" +
                "/GoJrLTmwAh0xYbVKvYWTOrbWjIPowFR+hf1ah260poDN7Bn59" +
                "JjUtcfuhStcX2LQNyZJtFiz2240n1H/wYx8E+9Mi7U7XO9Mq60" +
                "79kvkQN1mmhPlAPV3UsOFM3IgX9/H2gK6IoNq0ywYsMqG6x0sj" +
                "JzvWMlK8EqlWYWIgb/IgcWQVcp3dMHvtBaMeKSlnpV4p2ujEfo" +
                "ik0OLAUrNqzKwIoNq3KwSiFLh9XbkcvKWUedeffTrqMillXML8" +
                "HK77qqhq7YsKoBKzasasFKLyuzzrP7q2WwSnvfoh5xSUvfog/v" +
                "cvIwsz+ZB97l9ItlPAEBQX4k");
            
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
            final int cols = 107;
            final int compressedBytes = 647;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtml1LFFEYxzs7580gkeg2CL+EH0CvNL9AkLcJRYZ50bWlF7" +
                "6hkO/FmoQSSVJYN9ILlbD4QnmZRFetYvhCipWuuE2z07aWq249" +
                "Z+eM/R+Y2TO7D7vs/zf/8zxnZthY0g8WS+4RbILNuvtpb/ycjb" +
                "MH3uihuz11t1fsdUbuW/aMvWGj7NHPd1S9/8kMe8JeJg8MNplE" +
                "ZFdnTDW4mt5IsRJRdV3comMlesEqe6jGXLJltcsqas5XThFYUf" +
                "rK6Bx4B6wIfTjIY/tncOhnT726p4bV/ey+UiP/4Ksh+CqfEand" +
                "w40T0OXI1asYfGWGlZpyrhL3gXVgFfgcOA1dAvBVQs2xTfd1O/" +
                "3Ozm8ZH3cdzVn3D0as7bHfkX/jeydjdnJeHMZXkUvuVoMzPRRz" +
                "4AfoEsj6aoHH1fyP3oIvkveBcfQWeZ11F6GBPT2766tPZnzF5+" +
                "ErQtcsqeXMY2cWvUWIaa5Ag9Cw2oAG9tQrlZAVhq4HbqFeBb2+" +
                "0qegSxChT/8a82Kfz/E0qRORk5GCDHLK2+sceRdCZwJSZ3BfOE" +
                "z1yh8ZqFe6BPWKkpUuFX2iR3SzmOgXUdGFZ84sZlUmm2WrbEk9" +
                "HyibwCpfIdtyyq42PAdWghWpr87Js6bWV7IcrAhZJXQV7gsb6r" +
                "HP0/eBPK4v4Dq7/cH702fBxSwZt6FSaFbLl6GBJb5a4eu6hn/W" +
                "V/gXvsrX+NYfGdu7jpb+6le+8a/QOg++qoUGtvTsXm9Rh94iNK" +
                "yugVUo5rimA2oN9LPIV/7IxDWmFviKkpXuMMiqHaxIWXXqm6ZY" +
                "8cdgRcqq26CvusCKsLPowT189BbemTAAX9Gy0oMpVlhf/de+ug" +
                "tWdHHsO0holRE=");
            
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
            final int cols = 107;
            final int compressedBytes = 588;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk1LG0EYxztmZnfmIm2p0E8gvUjxoJaCvWmhSl8+QW82h4" +
                "LQr6D4EbS1hYDgCx4qvoW2IbZNEwghbfXeq4IgiiKag7JOXlpW" +
                "YyLRZ+wO+//DDJPdCYHnN/95ntkNW/IqYlnvHLEMW9N9vjT+yh" +
                "LsY2k0r1tStx8s7Zu7ylbYbzbHFv5ekZOVOz9ZnKW8C8VyHlQ7" +
                "OpoVX5dTRVZ8k5oV3wArSlZy2pyv+DFY0Ylvyxnd7+p2wHf4np" +
                "ytmnF06tPWpX6lwA8Ra9OSc0Vf/Vv3FV+VxslSn67yhs9Xvqvw" +
                "lXlW82BlTT7LNMZKLoCVyTrQYM2+iNrCGlZxsLrWjPUJMQjUWf" +
                "gzzsKh3wO/gBUtK5kos4KvrM9X3xGDoPhKHOp8lRL7tXwlCvBV" +
                "KPJVDqyI81XeFCu+DFbW+OoXWFGykqvmWImbYGWNr9bAitRXW2" +
                "JcvBVjLCvei5gYFR8IffUOrEhZbXueO1z2lYi5Q2B1XXJHGpnt" +
                "RFUzz9afw7HWA5uv1G06X6lb8JUpVqpF3cEeaK/UXcQgKL5SrU" +
                "6/02emZneewFe0eyBfV/fwTiTsZ2E3a4KVagu1r+7DV3awkhN6" +
                "rbbj/ZU11V5HzTudiE4Y8pXqgq8o5f6J+KIY+Xb2ftOb6u80vd" +
                "Zt8L/tAQ9CXVs8RG1hYd7qDr6voJr0HiEG1rDqQQyCkq9Ub/06" +
                "0Om+wvNA1IH21OyPwYqalXpmiNVTsCJn9cIQq+dgRX6+eonzlY" +
                "35Cv+3CHAdGPWzUgOke+ArsKLTjROHh+IY");
            
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
            final int cols = 107;
            final int compressedBytes = 522;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm00vA0Ecxg27s53hQjg4uIhE4iJI3HwBJPgMDkQkwsXJiZ" +
                "PXIxIJEW+Jt6imDl4q2iZCvVTcxaESH0LWtirRVqOYaXd2nyeZ" +
                "zXZ3Ln1+ef7z3+6U+MykyGX8yAbMFJEwiVrHSOI8QI7JXuLswB" +
                "qn1giS0Je59+SM3JF94v28wvqSd26In1yYP4pcmVAWsd4c5gyr" +
                "8E1IpfNpkbRcZdz/X65GkSuRrNgYnaIzdDrOSl+mk/qSOFb6Il" +
                "hlF5391ex+i9WEsWlsZ8+VsfN3VsYGWAnN1RztpB1yaiBtByuJ" +
                "3cZ8+pXikcxZxYPWGIJbtqO3AA+UYbUGD1zRs69jvRLaW2zJY6" +
                "W3gpUyudoFK2VYecFKLCstxg7jrLRX0ay0F7ASul755OVKewMr" +
                "ZWqgH6yUYXUEVsqwOgGrfOq73wNZAL44LlfnyJUyrIJgpQyrEF" +
                "jlUywMD1yRq2vkquB9YAS+2LIG3sIDV9TAB9RAkazYozxWeNdo" +
                "g/XqCb4ow+oZvthhvWIVAterctRAWaxYlTGO/ex2ZsVLsZ9dnV" +
                "zxMjl9IFiJZqXFeDX2xijDqgasnNqz81r4UrBc1SFXjs1VPXyx" +
                "w7OwyFx5VpArsaw8qx+sxNdA3gBW4sQbUz+XRHOpgfgPauFqIO" +
                "uSUwN5k4xc8WZXs+qR9K6xWwqrFlf37G3o2e2voncFth5a");
            
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
            final int cols = 107;
            final int compressedBytes = 444;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmV8rREEYxr2aae3c8Y3cIsUnkJAbF/aK4soX4Gb9LYU7RY" +
                "SbxcZSZ0UoxRc4q1Vu3PhTOo5tiWyOMmfmvLPPW3OaZubi9Px6" +
                "3nlnJgiiojHzc0y1BgjjQVuqTc7JGTlNnpyXizIrF77NH9Nl+D" +
                "2t9POUo7VKbyNse2Er0NGXtRe0T+e0TpsfI3K2OnNGO3T4h785" +
                "AZFfWbVXe17N+X+xEgNgpZPVZy8GVqoDrPSF6oxaIfqgUmJyYB" +
                "f2Kya+6oavHKLZAw3qorboRQ5kw6ofrLTWFoPxsWrKghUbXw2B" +
                "FRtWw2BlMmre3Wagi3O+GoGv2LAaBSvrOXAMujjnq3H4ig2rCb" +
                "CyngMnoQsbVlPQxXyopagVeBNJzn6llvHWyMRXK/CVQzRXoUFS" +
                "a/Z0i74cmG5GDmRzvsqBFRtWu2Bl/XyVhy7O+eoAvrLuqwJ0sX" +
                "IW9oSfKr37SpR1+yrlw1dGz1dFaMA6B15BF+dqi2vkQL2s1E1c" +
                "rMQ2WBndr26hARtWZWjAhtUdNKiL2uIe+xUbVg9gxYbVI1ixYf" +
                "UEVnpZCV89x3MfKEpgpZOVeonPV+IVrPRFwxttgLkC");
            
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
            final int rows = 45;
            final int cols = 107;
            final int compressedBytes = 405;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2s8uA1EUx3GH20xjHsDSI0hEdGMn/iX+bNAX8AoqbCR2ns" +
                "HGigVBBYlISfwJaQgegsRDNGNMEAtSkTNXT+73JtNMOl0059Pf" +
                "nHMnTZJmq7ORsFpiycHn2c2316/kMX29zc7P5ER2srNqetTS40" +
                "Iuv3z2QU7lXnZl/+OduP39yp0cyfkvvk0dkX+z6sBK18o9xe7N" +
                "yr1oW7lnrDSt4kJ+uXINrFStivlZFfqxMtOvurDyueJuatBSs0" +
                "UPs4UZq16sQu9XxXWsvParPmoQxBzIzG7HqoSV+mwxwGxh4rnF" +
                "YLQZbf2cq2j771bRBlZeZ4shamDGapgaBDFbjHAPNGM1ipUZqz" +
                "GszFiNY2XGagIr9b3wJHthE3vhqfxyxf8tPO+vpqlBEP1qhlyp" +
                "96tZ+pWJflWmX3EPTO+B81iZsapgpd6vFuhXwedqESv1XC2RKz" +
                "NWy1gZeCqx0uwTbo4qBdGvVsmVYq7WyBW5yn4Je+TKjFUVKzNW" +
                "h1iZsTrGyoxVDSuvk+I1NQgiV3VypbfaXgErNN4U");
            
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
            final int rows = 11;
            final int cols = 107;
            final int compressedBytes = 146;
            final int uncompressedBytes = 4709;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpj3PIfChhP/McCGI8yXgSSZ8Ds/Yy7GdeBWRuBeC8QH2Y8gq" +
                "T2AuM+xvOM6xk3wUS4T0NlzjJuYzz0nyBgPPV/FOAOHdrG1dnR" +
                "uBoycXVlNK6GTFxdH42rIRNXN0bjasjE1e3RuKIn4L43GgYjIl" +
                "89G81XQyauno/G1ZCJq5ejcTVk4urNaFxRDzAAAPMWQuc=");
            
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
        else if (row >= 360 && row <= 404)
            return value8[row-360][col];
        else if (row >= 405)
            return value9[row-405][col];
        else
            throw new IllegalArgumentException("Unexpected location requested in value9 lookup");
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 108;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MERABAMBED9F+104IdgtwLkQkYy0xvcLPINgPcN9QEAMB" +
                "+hPgB4X/im/q/nr/r+9D8n8yF/zke+0V/IDwAAAADs5X8LAAAA" +
                "AKjK/yWA+xkAwHxk/QAAAAAAAAAAAAAAAACwxAAvu8Y/");
            
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
            final int compressedBytes = 86;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3LENACAIRUH2H1rcwAaImtz1UP7yRQAA9MqjVb6v/gcAAA" +
                "AAAAAAAAAAAAAAAAD4nf4CgH0HAAAAAAAAAAAAAAAAAAAAAHjJ" +
                "7X6KfgujNj9J2CE=");
            
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
            final int rows = 632;
            final int cols = 8;
            final int compressedBytes = 50;
            final int uncompressedBytes = 20225;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0DEBAAAIA6D1D6028PITIpAAX/WqBAEAAAAAAAAAAAAAAA" +
                "AAAAAAAHBrAMZmHuE=");
            
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
            final int rows = 31;
            final int cols = 124;
            final int compressedBytes = 162;
            final int uncompressedBytes = 15377;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2TkOgzAARFEX2UNCdm7kc/noOUJCB56naRHFfwJL0Er7sb" +
                "ppxfpY+eMa3ryNt/E23rZw77tOUd7jvHvWs66r9n7rFOW91SnK" +
                "e6dTlPdRpyjvg05R3i+doryvOkV5Dzo5v423deG91ynK+6lTlP" +
                "dFpyjvj05R3pNOSd5z5//3yp/vk05R3jedfG+xbr0fOkV5jzp5" +
                "nxtvW753+QJpcO93");
            
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
