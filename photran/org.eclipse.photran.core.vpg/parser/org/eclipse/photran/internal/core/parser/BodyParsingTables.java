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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 47, 1, 30, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 16, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 3, 0, 13, 4, 106, 47, 155, 156, 157, 5, 158, 13, 6, 162, 178, 159, 26, 7, 8, 160, 161, 0, 163, 168, 169, 179, 180, 9, 171, 10, 97, 181, 182, 183, 11, 172, 184, 47, 12, 185, 13, 186, 187, 188, 189, 190, 191, 192, 47, 47, 14, 193, 194, 0, 15, 16, 195, 196, 197, 198, 199, 200, 17, 201, 18, 19, 202, 203, 0, 20, 21, 204, 1, 205, 206, 74, 22, 2, 207, 208, 209, 210, 211, 23, 24, 25, 26, 212, 213, 178, 180, 214, 215, 216, 217, 27, 74, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 74, 228, 28, 229, 230, 231, 232, 233, 234, 235, 236, 237, 86, 105, 29, 238, 239, 240, 30, 241, 3, 242, 243, 31, 244, 245, 0, 1, 2, 246, 247, 248, 47, 32, 249, 250, 86, 251, 184, 179, 185, 149, 13, 186, 187, 188, 189, 190, 252, 191, 192, 253, 181, 4, 5, 95, 6, 254, 33, 34, 255, 256, 105, 257, 193, 258, 259, 260, 194, 105, 261, 262, 106, 107, 108, 112, 263, 115, 120, 122, 264, 182, 196, 265, 266, 267, 197, 200, 268, 269, 106, 270, 271, 272, 273, 7, 274, 8, 275, 9, 10, 276, 277, 11, 0, 35, 36, 37, 12, 1, 0, 13, 14, 15, 16, 17, 2, 18, 19, 278, 3, 279, 13, 4, 20, 280, 281, 21, 282, 283, 284, 285, 286, 287, 288, 23, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 24, 27, 317, 318, 0, 319, 28, 320, 29, 30, 31, 38, 39, 33, 40, 32, 177, 41, 42, 34, 1, 321, 36, 39, 322, 323, 324, 325, 43, 44, 326, 45, 46, 40, 41, 42, 5, 47, 48, 7, 49, 50, 327, 51, 52, 53, 54, 328, 329, 330, 55, 0, 56, 331, 57, 58, 59, 60, 61, 332, 62, 63, 64, 333, 65, 66, 67, 334, 68, 69, 70, 335, 71, 336, 72, 73, 74, 75, 8, 337, 338, 339, 340, 76, 9, 341, 342, 343, 344, 345, 346, 347, 348, 77, 78, 79, 10, 80, 81, 349, 82, 11, 83, 84, 85, 350, 351, 87, 88, 89, 0, 352, 90, 91, 12, 92, 93, 94, 13, 47, 13, 353, 354, 95, 96, 355, 98, 14, 99, 100, 15, 101, 102, 356, 357, 358, 359, 103, 104, 105, 21, 107, 16, 108, 360, 16, 109, 110, 361, 17, 362, 363, 3, 364, 4, 48, 111, 112, 5, 365, 366, 6, 367, 368, 113, 114, 369, 370, 371, 115, 18, 372, 373, 374, 375, 116, 117, 0, 49, 118, 119, 120, 121, 122, 376, 123, 19, 50, 51, 377, 378, 379, 380, 381, 124, 125, 20, 126, 127, 382, 128, 52, 98, 129, 130, 383, 384, 385, 386, 387, 1, 388, 389, 390, 391, 392, 393, 155, 131, 132, 133, 134, 22, 13, 135, 394, 395, 396, 397, 398, 399, 136, 53, 400, 401, 137, 402, 403, 404, 138, 405, 406, 407, 408, 2, 409, 410, 106, 139, 411, 140, 412, 413, 414, 415, 416, 141, 417, 418, 419, 420, 421, 142, 143, 422, 423, 424, 156, 144, 425, 426, 427, 428, 16, 198, 23, 429, 145, 430, 199, 431, 201, 432, 202, 204, 433, 206, 146, 30, 147, 148, 149, 24, 434, 150, 435, 436, 151, 437, 208, 438, 439, 0, 152, 54, 55, 124, 440, 126, 441, 153, 154, 442, 443, 16, 209, 444, 155, 445, 7, 22, 56, 25, 32, 446, 34, 447, 448, 449, 35, 153, 450, 37, 212, 57, 0, 3, 451, 452, 1, 2, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 166, 478, 479, 480, 481, 482, 483, 484, 485, 486, 43, 487, 44, 488, 489, 490, 45, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 25, 27, 28, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 46, 515, 516, 517, 518, 519, 520, 55, 521, 57, 66, 68, 522, 523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 69, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 70, 566, 207, 567, 568, 569, 71, 216, 570, 571, 217, 572, 573, 84, 574, 85, 86, 575, 576, 156, 161, 577, 163, 578, 164, 579, 165, 580, 581, 7, 582, 222, 3, 583, 227, 584, 87, 90, 97, 106, 58, 585, 109, 113, 586, 587, 4, 588, 158, 589, 590, 160, 591, 592, 593, 594, 595, 596, 597, 598, 599, 600, 5, 601, 6, 602, 8, 9, 603, 10, 11, 12, 13, 604, 605, 606, 607, 608, 116, 609, 117, 610, 124, 228, 110, 611, 166, 612, 167, 613, 614, 126, 615, 616, 617, 618, 14, 30, 619, 620, 621, 168, 622, 623, 169, 624, 625, 626, 627, 628, 235, 629, 127, 630, 631, 632, 633, 634, 635, 636, 637, 638, 128, 639, 640, 641, 642, 129, 643, 644, 135, 645, 646, 647, 8, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 658, 170, 172, 659, 173, 660, 127, 661, 174, 16, 662, 663, 664, 665, 666, 667, 668, 59, 669, 136, 137, 670, 671, 672, 138, 673, 139, 140, 141, 142, 175, 674, 143, 4, 144, 145, 675, 676, 9, 677, 678, 679, 680, 681, 682, 683, 684, 685, 686, 687, 150, 17, 151, 152, 688, 153, 154, 176, 1, 159, 60, 162, 170, 171, 173, 175, 61, 177, 178, 179, 181, 182, 183, 186, 192, 689, 690, 185, 691, 692, 0, 693, 47, 31, 694, 695, 696, 155, 157, 168, 195, 202, 203, 62, 208, 63, 237, 697, 18, 698, 169, 172, 188, 190, 193, 194, 699, 700, 197, 701, 702, 196, 204, 206, 209, 210, 64, 703, 704, 705, 706, 211, 707, 708, 709, 710, 212, 10, 213, 19, 20, 711, 712, 177, 713, 178, 714, 715, 716, 717, 718, 33, 214, 65, 719, 720, 721, 722, 215, 217, 5, 723, 724, 725, 726, 727, 728, 238, 218, 219, 729, 66, 730, 240, 731, 732, 733, 734, 220, 7, 221, 222, 223, 224, 735, 736, 737, 225, 226, 227, 738, 228, 179, 68, 229, 230, 231, 232, 739, 233, 234, 235, 740, 236, 237, 238, 741, 8, 239, 240, 241, 180, 181, 69, 182, 184, 742, 70, 71, 128, 74, 75, 76, 743, 744, 243, 745, 186, 746, 242, 243, 244, 747, 748, 188, 189, 749, 750, 751, 193, 752, 753, 21, 754, 23, 194, 755, 195, 756, 757, 758, 77, 245, 246, 759, 760, 30, 78, 27, 79, 80, 28, 29, 81, 31, 82, 761, 33, 247, 248, 249, 762, 763, 196, 764, 250, 765, 198, 766, 74, 92, 34, 251, 252, 35, 248, 95, 199, 767, 36, 768, 200, 769, 770, 253, 771, 772, 773, 1, 37, 255, 256, 2, 38, 257, 83, 258, 259, 39, 260, 774, 250, 775, 261, 36, 776, 201, 777, 778, 202, 254, 255, 779, 780, 781, 782, 262, 263, 264, 203, 783, 784, 204, 257, 261, 785, 206, 786, 787, 207, 788, 789, 209, 84, 265, 266, 267, 39, 268, 269, 0, 211, 270, 271, 790, 791, 792, 272, 273, 274, 276, 277, 278, 279, 40, 0, 286, 290, 1, 287, 292, 2, 293, 300, 40, 306, 308, 311, 312, 313, 317, 321, 322, 41, 324, 325, 326, 327, 330, 331, 332, 333, 334, 335, 336, 338, 339, 340, 341, 342, 343, 344, 345, 1, 212, 347, 349, 350, 351, 352, 354, 355, 357, 358, 359, 360, 361, 362, 363, 41, 329, 85, 2, 42, 353, 365, 793, 794, 43, 42, 795, 796, 797, 798, 799, 800, 801, 802, 803, 804, 368, 369, 805, 806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 818, 819, 370, 371, 820, 372, 373, 374, 376, 377, 378, 379, 381, 382, 383, 384, 385, 386, 2, 821, 387, 388, 389, 390, 391, 393, 822, 394, 823, 824, 392, 396, 825, 826, 395, 399, 827, 400, 397, 47, 48, 49, 55, 66, 68, 69, 72, 73, 74, 75, 76, 77, 83, 213, 828, 214, 0, 829, 398, 830, 401, 831, 402, 832, 86, 833, 834, 835, 215, 218, 404, 403, 219, 405, 260, 406, 407, 836, 837, 408, 409, 410, 411, 412, 413, 414, 415, 220, 838, 416, 417, 418, 419, 420, 105, 421, 422, 44, 424, 839, 840, 88, 425, 427, 429, 3, 221, 423, 426, 431, 432, 4, 435, 841, 437, 433, 222, 223, 436, 438, 439, 434, 440, 842, 441, 843, 442, 443, 444, 445, 446, 447, 448, 844, 224, 449, 450, 451, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 845, 225, 846, 847, 45, 463, 89, 464, 465, 91, 466, 467, 468, 469, 848, 849, 850, 470, 851, 471, 472, 473, 474, 852, 853, 475, 854, 226, 855, 476, 477, 856, 478, 857, 858, 229, 479, 480, 481, 482, 483, 3, 92, 93, 484, 859, 485, 860, 861, 862, 1, 4, 486, 487, 94, 86, 488, 489, 490, 87, 491, 863, 492, 493, 494, 495, 496, 497, 498, 88, 499, 500, 262, 501, 502, 263, 503, 504, 864, 505, 506, 5, 865, 866, 106, 46, 867, 868, 507, 508, 510, 869, 230, 870, 871, 231, 513, 872, 232, 3, 873, 874, 511, 515, 875, 876, 516, 518, 877, 878, 520, 879, 880, 233, 517, 881, 519, 11, 882, 883, 884, 885, 521, 886, 526, 95, 523, 525, 887, 524, 527, 96, 98, 100, 888, 234, 889, 528, 533, 264, 890, 534, 89, 891, 892, 893, 894, 235, 236, 239, 90, 241, 895, 896, 897, 546, 898, 899, 4, 900, 901, 902, 903, 91, 904, 101, 905, 906, 907, 547, 908, 5, 909, 910, 544, 911, 912, 92, 7, 913, 914, 915, 103, 916, 917, 918, 919, 242, 920, 921, 244, 93, 94, 922, 245, 923, 557, 529, 530, 924, 925, 926, 927, 559, 928, 929, 104, 930, 0, 931, 932, 933, 105, 95, 96, 100, 107, 108, 109, 934, 114, 115, 118, 119, 935, 936, 101, 937, 47, 938, 939, 271, 940, 560, 531, 532, 535, 536, 537, 538, 274, 941, 120, 942, 943, 5, 539, 561, 48, 543, 121, 551, 103, 107, 49, 944, 50, 945, 540, 541, 108, 542, 946, 275, 246, 277, 553, 562, 563, 247, 254, 564, 947, 318, 948, 249, 949, 319, 256, 320, 950, 565, 951, 566, 567, 952, 568, 953, 569, 570, 571, 572, 573, 574, 575, 954, 258, 955, 259, 260, 956, 957, 958, 51, 545, 959, 960, 961, 576, 962, 963, 964, 965, 0, 966, 967, 968, 969, 970, 971, 972, 122, 973, 974, 549, 975, 976, 977, 978, 979, 980, 981, 982, 577, 578, 579, 983, 550, 984, 580, 985, 582, 554, 555, 558, 581, 986, 583, 987, 988, 989, 584, 585, 6, 7, 588, 587, 589, 591, 990, 261, 991, 992, 993, 265, 592, 994, 267, 995, 266, 996, 997, 998, 593, 590, 999, 1000, 594, 107, 595, 596, 597, 598, 599, 2, 1001, 1002, 1003, 109, 52, 600, 601, 603, 604, 53, 605, 1004, 606, 610, 1005, 612, 1006, 1007, 54, 607, 1008, 268, 608, 1009, 1010, 609, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 1019, 269, 611, 1020, 1021, 1022, 1023, 123, 613, 615, 1024, 1025, 616, 617, 618, 619, 1026, 1027, 1028, 620, 621, 1029, 0, 1030, 1031, 1032, 8, 125, 130, 614, 622, 1033, 1034, 624, 131, 1035, 623, 1036, 625, 132, 1037, 1, 1038, 1039, 626, 627, 628, 1040, 629, 270, 1041, 1042, 630, 631, 632, 1043, 133, 134, 1044, 271, 321, 1045, 633, 1046, 637, 1047, 634, 1048, 1049, 643, 635, 636, 1050, 1051, 644, 638, 108, 9, 639, 640, 1052, 12, 1053, 641, 10, 1054, 1055, 1056, 1057, 1058, 276, 642, 146, 1059, 277, 1060, 278, 645, 1061, 646, 1062, 280, 647, 281, 286, 1063, 287, 147, 148, 149, 648, 55, 649, 1064, 1065, 1066, 1067, 1068, 1069, 1070, 650, 1071, 651, 1072, 652, 293, 653, 290, 654, 1073, 655, 112, 1074, 1075, 11, 656, 657, 658, 659, 660, 1076, 1077, 661, 1078, 662, 663, 317, 664, 113, 1079, 1080, 12, 1081, 665, 666, 292, 1082, 300, 1083, 667, 153, 1084, 1085, 1086, 154, 1087, 156, 280, 668, 670, 1, 1088, 321, 1089, 1090, 114, 1091, 115, 1092, 322, 1093, 323, 1094, 56, 3, 4, 673, 675, 1095, 110, 57, 324, 1096, 325, 676, 677, 1097, 1098, 678, 158, 679, 1099, 9, 1100, 159, 322, 680, 687, 690, 691, 693, 694, 111, 327, 1101, 326, 116, 1102, 117, 1103, 118, 327, 681, 1104, 338, 328, 1105, 161, 1106, 1107, 682, 1108, 1109, 695, 685, 162, 58, 688, 163, 689, 330, 692, 59, 696, 164, 697, 698, 119, 699, 700, 701, 1110, 702, 704, 705, 1111, 707, 1112, 13, 14, 709, 15, 1113, 708, 1114, 710, 1115, 1116, 1117, 711, 16, 712, 17, 1118, 713, 714, 1119, 170, 717, 1120, 1121, 715, 718, 1122, 716, 331, 719, 720, 267, 721, 722, 1123, 1124, 1125, 723, 724, 725, 726, 2, 112, 60, 122, 727, 728, 729, 1126, 1127, 1128, 1129, 1130, 1131, 730, 731, 1132, 732, 733, 1133, 332, 61, 62, 734, 735, 63, 1134, 281, 123, 124, 0, 125, 126, 333, 736, 1135, 1136, 1137, 171, 737, 739, 741, 1138, 743, 173, 744, 1139, 1140, 745, 1141, 746, 747, 748, 749, 750, 751, 752, 753, 754, 755, 1142, 1143, 334, 335, 756, 757, 758, 341, 759, 8, 174, 760, 9, 10, 761, 1144, 762, 763, 1145, 764, 1146, 765, 1147, 127, 766, 767, 1148, 175, 129, 1149, 1150, 1151, 337, 1152, 1153, 1154, 1155, 336, 339, 768, 340, 1156, 769, 771, 1157, 128, 1158, 1159, 774, 1160, 18, 341, 129, 1161, 1162, 775, 776, 777, 11, 1163, 1164, 1165, 19, 131, 345, 1166, 778, 779, 1167, 349, 2, 176, 177, 178, 350, 352, 1168, 354, 1169, 1170, 355, 1171, 1172, 134, 1173, 135, 1174, 1175, 1176, 1177, 1178, 282, 179, 772, 1179, 283, 114, 356, 64, 284, 1180, 770, 1181, 1182, 773, 1183, 780, 781, 782, 783, 786, 787, 784, 1184, 115, 65, 785, 788, 132, 1185, 133, 1186, 1187, 1188, 1189, 136, 1190, 789, 790, 357, 1191, 1192, 1193, 1194, 1195, 1196, 5, 13, 1197, 1198, 1199, 793, 1200, 1201, 1202, 800, 1203, 1204, 1205, 1206, 1207, 1208, 805, 818, 1209, 826, 358, 10, 824, 11, 12, 1210, 1211, 825, 827, 828, 20, 21, 181, 830, 1212, 182, 1213, 66, 829, 831, 832, 834, 835, 1214, 836, 837, 1215, 838, 840, 841, 1216, 1217, 1218, 285, 12, 183, 185, 1219, 842, 843, 844, 13, 845, 846, 848, 1220, 342, 1221, 359, 360, 13, 1222, 14, 1223, 1224, 849, 1225, 847, 850, 851, 852, 186, 1226, 361, 67, 853, 1227, 1228, 137, 1229, 854, 15, 1230, 22, 856, 138, 1231, 1232, 1233, 1234, 1235, 362, 858, 16, 1236, 139, 363, 1237, 1238, 1239, 1240, 1241, 368, 860, 1242, 369, 370, 1243, 371, 1244, 1245, 372, 1246, 1247, 1248, 140, 141, 7, 8, 857, 861, 862, 863, 373, 864, 288, 1249, 1250, 376, 866, 14, 867, 343, 1251, 1252, 344, 187, 868, 1253, 68, 1254, 1255, 191, 192, 195, 1256, 1257, 196, 69, 870, 871, 1258, 0, 200, 872, 873, 1259, 1260, 874, 875, 1261, 1262, 1263, 1264, 878, 881, 882, 1265, 1266, 1267, 1268, 15, 883, 1269, 1270, 885, 877, 879, 1271, 1272, 1273, 886, 887, 888, 1274, 289, 201, 202, 889, 1275, 1276, 891, 893, 894, 896, 1277, 898, 1278, 892, 345, 1279, 1280, 899, 1281, 909, 1282, 1283, 1284, 378, 131, 1285, 1286, 1287, 23, 379, 1288, 1289, 1290, 1291, 380, 381, 900, 377, 1292, 1293, 913, 1294, 1295, 1296, 1297, 382, 384, 901, 385, 1298, 1299, 1300, 203, 134, 1301, 1302, 1303, 1304, 291, 294, 295, 1305, 70, 386, 387, 296, 902, 903, 904, 915, 905, 906, 908, 1306, 204, 205, 388, 389, 390, 206, 1307, 1308, 1309, 142, 910, 1310, 1311, 1312, 1313, 1314, 911, 1315, 1316, 912, 16, 916, 917, 918, 920, 1317, 914, 924, 1318, 921, 391, 1319, 1320, 1321, 922, 923, 925, 393, 926, 928, 929, 930, 931, 394, 1322, 1323, 405, 409, 932, 403, 1324, 1325, 143, 1326, 933, 410, 934, 404, 1327, 1328, 144, 1329, 406, 936, 937, 938, 411, 1330, 297, 298, 1331, 1332, 939, 346, 940, 428, 30, 1333, 145, 148, 1334, 1335, 430, 941, 1336, 1, 1, 935, 942, 943, 1337, 946, 944, 947, 1338, 1339, 1340, 945, 948, 1341, 949, 950, 951, 347, 1342, 952, 1343, 1344, 432, 1345, 1346, 150, 1347, 1348, 24, 1349, 151, 1350, 1351, 25, 135, 299, 301, 302, 438, 441, 953, 348, 1352, 152, 155, 156, 1353, 1354, 1355, 1356, 208, 157, 1357, 954, 1358, 955, 1359, 1360, 1361, 956, 959, 960, 962, 963, 964, 957, 209, 965, 1362, 1363, 27, 442, 1364, 1365, 28, 446, 1366, 303, 1367, 304, 958, 1368, 1369, 1370, 210, 214, 961, 15, 227, 251, 1371, 252, 1372, 966, 1373, 968, 969, 967, 447, 1374, 1375, 448, 449, 1376, 1377, 450, 451, 253, 254, 255, 452, 453, 256, 971, 973, 974, 257, 258, 1378, 454, 1379, 1380, 455, 1381, 305, 456, 457, 458, 1382, 1383, 976, 977, 979, 1384, 1385, 1386, 1387, 1388 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 1, 20, 2, 21, 5, 22, 23, 24, 5, 5, 2, 25, 0, 26, 27, 7, 28, 7, 18, 6, 29, 30, 0, 31, 16, 0, 32, 23, 33, 0, 3, 12, 19, 34, 35, 35, 36, 37, 38, 39, 40, 0, 41, 42, 38, 43, 44, 39, 40, 1, 45, 46, 10, 47, 44, 48, 49, 45, 50, 34, 51, 50, 52, 53, 5, 54, 55, 0, 56, 57, 58, 3, 59, 3, 60, 61, 62, 16, 41, 51, 63, 62, 63, 64, 65, 66, 67, 68, 69, 64, 70, 65, 66, 44, 71, 72, 73, 74, 0, 75, 0, 76, 72, 77, 78, 79, 73, 80, 5, 80, 0, 81, 82, 2, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 19, 85, 89, 95, 96, 97, 6, 95, 98, 99, 96, 100, 99, 5, 38, 2, 12, 0, 101, 38, 102, 102, 1, 103, 16, 6, 104, 105, 106, 107, 108, 0, 7, 109, 110, 111, 104, 112, 113, 114, 61, 115, 6, 116, 9, 117, 118, 119, 120, 121, 122, 123, 124, 0, 125, 1, 126, 44, 127, 128, 129, 130, 0, 129, 131, 0, 132, 133, 106, 134, 135, 136, 113, 2, 137, 63, 138, 139, 140, 141, 2, 142, 3, 143, 116, 0, 45, 119, 144, 145, 120, 4, 3, 146, 39, 0, 147 };

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
            final int compressedBytes = 2985;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb2PHLcV51BzAnNxIvogxVvOyQqyAVTYldMoGV2cLzc6CB" +
                "YSI0iTJilUupQB3sUITp0KFSoXKgS4859wCFQEqq4xHKjKn5KZ" +
                "2dnd+SD5e+Tj7N4pa/mkvd+SfHzk+yTf7Jsbdx795Olc3dw7uf" +
                "vttZePzj95clB+/Yfv3jcP/32vPBBvHlT4N/PfV/jjAf4fEl71" +
                "/8E38z89r/Ffv3z0+pMnD3v9c/F3nD4hMjF46e4b7vjnP7rz6d" +
                "5Jtf5C3P32/umt8+vzh8Ko31SDlCLF/Kbm741mfPf+veGkL8n6" +
                "vH3wt7988HT+5+c//cfjl7979cfXv3ry16N/fvF9hb/96tPp+V" +
                "evX34ynz2v1y87vfX6+vwgaP12vb7c/i87fTvWL0KJXNQ/21f1" +
                "Rsv232WtPyaen+wqq7Lzs9Vsl57/l92+TIyPXqb/ls2fq26/k8" +
                "pnvrLvZde+e+3Xxn9z4pPu7wTrh+Y35fpUK7C3dqhk9Z/+XFc/" +
                "Tb0EexT9RPFPnjb03Z1ifg3JKyfxevX/7PijpZSWci/n09/6F0" +
                "v/MGv8wyD/4u2Dn7X+0cnjxdo/avC1f7SKPxbL+clLtX+58j32" +
                "3wevLPf59whH/jvC/7XEl/7j/cZ/XONZApwbX3D7D2m/WLY/Cq" +
                "EPvbj6jevfr9rHym+q8V3tu/hiicsujvxXLt6n/1VD/1FL/1cU" +
                "/xm0D+1fdPoXcfMTKedndfo2QCNfuUP+Mor8qFJQXtqLGieC4o" +
                "8e520v4J9NjdvWr8+/rdMn4ul3+keK4t9a/QNgf5F/124DKYq1" +
                "BPS2Gmo/Nd5lerudufSJkPHZ/munvW399vfFrQNVbZ+9k2r8oh" +
                "Qff6nLr+8/y8zDF/dWJPX926Lr31b4ez7/F+EE/5rV//5nzvmd" +
                "3Wv9N45/jdp38cUS79tXuiqNxIF+lyLsNex/aT9y3wdzhwmRXv" +
                "1vVvoN4GUYo0b0u6ydh9FZl6/ttLSSfbLX9BmR1+ptOZAyYlb/" +
                "PW8Unqr/yB4hZsCnZn7F5hM9TucpTbUVl0pKresZy6V8vajlX1" +
                "efOm/kC9pvtNWw/WfuDz9/4fxo+z9eftb7SNnXr6W/GNFfEHHQ" +
                "f5R8bD5I55+mKZvA8Yntc/f+ssnXSv5zyvxFb/6zoXy45HfVPx" +
                "of7q8SyZzuvTluldPav+HxX8UoGp3OvqkRrmnrb6j727u+cfpR" +
                "x9snE9ie3L9y4X79IoB8pV7f0QeBfIzlR/dCfOUa3/TNvcsNGI" +
                "4f6H/h/YPmR/WPnPbFLz+p/U/j0AVaOPwn/0CE/G/Oyf9y86cf" +
                "zt5KfVPs3dbXZfZLIQ5fXByKQi+U+O/tvTsmzn8o6Rp16vM5lN" +
                "/s4rb4Z1v0ufKfDf9Je9rhvzD9v9T5S25+dZj/jGgvwtqXFP0S" +
                "HSUMvB9bfAbPD4LoD1x/lL9F9iNVfjeWPvL5jA6JDjUZ5+dnA/" +
                "kzMmp++8VdHwPiJ2z/gvOXIjA/Knz5W0t+dziFvINLS/ud4gny" +
                "217+R6xfWH5PRO1vt39L0rwp4xOm/rPKjyL3z8gvN/lzlF+34G" +
                "KQf0f5b29+nmM5BS3/PykeTt/n2x7/Pc75CJJ/dH5hwUUXZ5/P" +
                "MM8/uvKnrf6zQPlxtH9lLcGHVYtRfuKagOebBP0mw/VrR8Wr41" +
                "aHS9mNJRS1PcAJ9xsFi36oqEuefo/Kn4TQh9Qej/8bXDLbJ+J/" +
                "6v2Dxp8LaUS2+Pnp36tffWEOTPZ8oRfqt/MiN9cs7gTZI6HNj5" +
                "Cf9efnATXr/E3V2Ja/+XD2po/PlrgY4sqOS/WZqPCstkPixxWT" +
                "Zhcr+m639scs99ZQP9L0J+y/nn/um79/fog+NH9EP86vEfk/WD" +
                "9BX7+Gf7erOcas76q9HLU/J80f0SfVkXd96fyJm9+K/mnWX0TM" +
                "/3gsPzmWHyxf+4A+GSXfSL9Un+PZl/myYeUfmdxU4xqplS6UyC" +
                "oyfmg5HzDD/g3xfMXV/hz4R2XHk7elIJF9Y/JHXPTbj/wzov+i" +
                "Y883YfzprS9Ifj5DzNCRM7Nc/0f5B8Ln7yC+OAO4OAb8O+adT4" +
                "f6vyYxf3fsf/L7B/KPzreh/qDqlzgc+6+rT8vN/Q1Br4+iybdl" +
                "PcqcpB/445/Xs8qLZvJdeopAXMbh6/sN9vwBHr9sPy29eMD+1G" +
                "H8Qfy96Ldf2zfq/Ln55Yn3D9LvVv2pgX7dnv6k3R/D+Snn/Zx5" +
                "kx9bHIoqPhc/KCr8man8v3LuW98A/Qzzc6X10CcdLiLa6/jxk+" +
                "+Py57/2QV9CfUHvP8aM37g+umE+mvq/O3W929I/FJY5V818rnf" +
                "13+5yZq+thafTL0+rvh0W+tvH58Qf/njnzNxWlj3/2K1uO83UF" +
                "6HwEVZb4FS3NeZ0WczGv2N/9O/n9HjH8Bd/rLZDv9B/kb1rqcW" +
                "lqJRavwoafEr+UX1D0idIfvtyT+B9lD/rM/vfPKXg/tLOVl/mN" +
                "D8mr9/fP80if+fR+tHjEfYR7r8ofpKV9op3P/o0SdT+T8oPReX" +
                "/2PY/9Tx1eT2lZc/xesvWP4rsq/8/MsU9muL8X/o+nH9f+OyP4" +
                "7+sX475Zx/YvkY3E8l69e1/2Umpa+9KFts7Kte/nrhp08Wm8ZG" +
                "qLrRs839vjJCf0kmnkb/jKKdVPFvqH1drT9qv1v5RvUn+H43l7" +
                "8h8mGpH8D5TfR8GnB/LnD9jHMvy2j/rRu5D+Ub9hKyPuH8hfUX" +
                "ylI/256/5IT26PkPXPmg6F8O/ez7mUn9g1dx+i18/5Xp84+O9l" +
                "H3N0f36zn1Ff7+2fW3wuE/GRJ93PoWQnvW84fQ+uL6kFL2/LNx" +
                "/unjZf5rxOyz5a5G7afN36LzaSg/cP9Pa7+59R30+jEVTn+C+z" +
                "3p6ysC60u491to54vRWRG0/nh9j3j0WQnVln1gxw/1C1nh9fm+" +
                "zH5RaQRjMlHUCeKiqN7Q+vfh5Pyvv33882sEn3+k+FwB+XLc3y" +
                "Hfz5MB/Qfg4PybeL8fPb8j+vw9QX7gdOL8Q+T4C5L/y63PYdf3" +
                "7Lj/dwdXcfaJrH/+3/nLsy+Ttd81/dzzsd2vL8++X5n9yXTUXX" +
                "gAfcoCcJ+/P6pPGdfvsPLLrvoe5e6/pz9hfRrwn/31PYoiX9zz" +
                "kVD51T7tkjnn5zo/F73bCxnwn124qz2pfrCpvxryv08/xp31W8" +
                "j+euu7rg6umPj+u4lbo6f08o/rC/dj96cXJ9UPk+YXN39U/5iA" +
                "P148nX3aB/Gd3f9Pcv5N0v8o/iDi0f6porktofkVov/oro+ZOP" +
                "9De/7pdPMXq9Gc9Uup82Nh6w/3B+/8Ie58VKejD7a/4MZ3Xv1z" +
                "qM/a/G4umvyuqPO79eLX+d2zmPy9q3446nwf9p9sf7pwbcmvds" +
                "+/EO4Py+Kej0Knv7nfd5NRv2yNbwLsU2j9siU/qnz+w7B/S/7R" +
                "Qd8q/ygvc34zQf+8+HG9YDIuf0k7X7Dsb5Pk/IL+/L7I85FYvK" +
                "TRR/f/XN/vwcSx/0b0X3j5VXf9My1/EWB/HfXR9vHx/rrw11dz" +
                "4wfA30vx/cI5RT7i8pPTPJ8uKP7i1bckOx+aCOfGH9z4D+NTx7" +
                "fE84Xo+HPX/Eu0Pxz6hy2f3P7LtQay8x/gHQ0WJ7/M8Xd8P2CJ" +
                "F5t/Dr4fjNa/p/0k9UUh/l/J4p/1+4+6z89J8fwDj/3E3z9xxe" +
                "MXED/b6/N1XZ8/4KD0xRdufN3/QP936v8d49Nw2vhj/ZuLNPNj" +
                "49i/Yc2fHGCx7h9Od79N8exHAvlE9sOLN8+/1JUq01Lavj+K8H" +
                "wGVv9x+Sm0/kn3j5+/gprfEAHyhUNc+vMlgH5l079l/KrTnxoP" +
                "9R8NpX454P5HlHyI3ldp1CFYUW7a1+JeNPW5DW3g+S27zi9wzx" +
                "+o+BWRr8Gvcf1TDH8D4jPy+c1U50fM9oT43S8/xw7/i5y/MHH2" +
                "z8FLJ39E4P6n4tqvFtDzcdD9DXD+BJ+fw8zvsv1b8vOTQp5PFK" +
                "gfSfljeTnzwxPX/3PvZ0P+/A9pI941");
            
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
                "eNrtXTuPHEUQ7mnPnsbHq32yzYVz5oINHEBkEqOxwQiccDpxEm" +
                "SEBISERuo7OTCZAwcXrhwgOfNPWJAD5OgSJIuIn8LM7HNmuvur" +
                "7uq+XZAn8aO2X9XV9eqqaiFsnxb2Twlqs9ffHJ7cfDH+6vro9K" +
                "eXV56fTO882qsef/3XNX385m61J0Qh3F9R95hLUc5/WGix3/w5" +
                "rv+nbYzgy0+C+SN45wer9X1wePLhi/F3z5r1ff785NWdR8fz9f" +
                "3ZrI+7ftg/E07f3DD8eY+v/daP5t9t/xvAzxBOoM98RWj5Yvwq" +
                "Fn2tuuXSbyAcn7+w8yvN5zMzzS83wc38KbPOf7f959V2fFXPf0" +
                "Icv5qPD3ihmnegF8N2Os5LsdNrM6Huz7y9tLV394/5I6//+Wdt" +
                "38N/McQ/6h/AVReuZ/CiN74YrN/j/LHOF5gfpK9KeH3atz2AF5" +
                "WB4JUMUhss8Mzy0yxS/044l7+JLHfKT9i+Ysl/En0a5YeOwv+n" +
                "7x1+MTodF9eFuP3y3tmN6c74WOjigWoW1s6vSrr+Zvz8dLzfjp" +
                "+14+91xof8jzc+aX9yB3vp6A8rhkFnTz7zn+k39+fz+4WyP2h+" +
                "7POjugvUtmMTQ/8crv+PGf0Y6Tcz65dzxlT1BpaA/0gAsLbn9s" +
                "8dn9U+jH9dnv47Xd9/A/+A9kHA+fPh3xh/bPlxrT1s+YCZPAHi" +
                "WdMEbQVMNAQnCXLZspsZsage6WT9kebST3f+V1n5X+Cnaf4PtH" +
                "/Yf3Bm5r8Th3xT9vXpgf4S6H+ZuJEnvfbXMH8OnLKtgf1rCm3w" +
                "509uDxZaU9BoTQJLob5tLFnd0M0omP4VecPZ54Otn1VAasp31Y" +
                "KHNCbc/tHHM+RWcpRjuMF+shCiGT+7D8WNvaL+0ei0/klZiU9+" +
                "VtXje08zffzk7qy99NcPVWK4D//i2QfJ94crX5n2B1+/rvzOqr" +
                "f+rcRQ/1Pe+ns5oN8ymn+zapWYctjkio9/xda/RvazUz7Loma5" +
                "qqFPuaDPeqKq7nQ6o18AT3++E/uX4+h/0Jfk7J/kH6H1nwXjJ3" +
                "X/0tL/JuTDNtFfiP0cc3y9gfW9hW8NvGwoIJtcPfuh/sv3umb1" +
                "jydqUnw5FrmOIp9C/cNv9+dy4aH+E6JZqITnF8l/A/TTj/b/lu" +
                "q6GN2qlZvsMyEOzi8ORFkfAPHPrdGhXqNPtFBp6f91t//9Wf9i" +
                "3v+m4Ui/R+1l8VDU8Kwx3cX7tXzfv1joh7dm+uP9HvyoA4fzC9" +
                "afaPYLHX8yFP/t+m3tZfHaiT8u/iPMH4x/nzV+lPPJ4H/c/g/U" +
                "uazpr7lfl9mntTjVujb1VFmISVn/I45+mXP0N65+t+n2WH+Z2R" +
                "WlznVDd1IUDfobQ+Oddf9K0fEPbI9+zOy/mPLWh/AznjXMxHiG" +
                "Xy1VB7/R5HNhFKsB8XdR9QMhLqL4lxj3o2/hG4UT/Ocyov9z2P" +
                "8Z7X5LBtI38J/C+zXP8236HPEpyP9I8H+D78gPrinz9+H/aHxv" +
                "+80rftquv5St/sLH76bt2230LwqyfWKOz4w5vynAH4IvXLrzoK" +
                "Sy78Amzk+F+t9D5Dvlfppm3zd8SBjjl8tI8J6iu/5din9uaol/" +
                "Lj37t80frB/rZ5b45pLBH3zk93bzL278Lj/+V28Yzpxfbf/UGJ" +
                "4ciB/rf1wt658/1bV4rMYl8fxFmP//2v7fNJxrHxLsAye8snqY" +
                "aXSVwv6MeX+5Wf0rff6ie/yqie/9vSzqDb5dZvrGdKdoGj1of0" +
                "FonxyO8+cWmmmX7PI+h5JW/apo4cv2s8Hz2s7QtPMza7+71l6t" +
                "tQfjA3gTH2bZ3zdr8s21vsTxUdtov4TEjzD8Mw75h+OTgH7DjW" +
                "8m+WcM+KpyIv7m8qGT34nyQ0P4v8O/MRhBxYaHj0/ML11uhU4y" +
                "/03DXfjrrn/YHsG77HjOv8tE8w/sP8C+j8T/ysW5td4/GehTre" +
                "gT3K8UHZZQrk9Hkw4yQb5v0r71ji/VVP/ZZCV/o/jnrf5xpnxX" +
                "vP0pZjMqV/hRs/+exDmf1Pt5tSPD7udN8d8e9w8F4n/c/qsuIn" +
                "SQ/ygYv7C9cf+86IcCz+38tQIIj+CfQvpNzrE/AX2mlh9g/sv7" +
                "GbUjVvczYnk/A/U7vv8iIH+EFX9IyP9THvWVQH0UaL9VAfNLbf" +
                "8JoP8L+vrJ+2+pXwP4J0k+uOrrAPzi+gKKR99Y/rjrC0H5DOoP" +
                "UOrHuOs7uNsT4Y76Oe78Fcg/Iug/bvvbXV+InV+M6kfEWt+ucf" +
                "yw/H7Rkb83f239H7d7/o+l/7OGFwb/CM0/asxvXC/hUSXaf5d8" +
                "8vZ/BNgfxPE3fP9P8H8R9WMf+u3Jb4d/D9X34da/IbV3LRGsH8" +
                "ondv0dT/5jh3vUv4no3y2o53ND8X8B+9ulHzf+w+Jb+x2Vq7/m" +
                "PcUA+3dysapfIvv1S7zrL+ie/Ef+ue2HR8JPmH2G61OQ7g9W+r" +
                "8IvT9AgjD1/bNVPqP6Qej8nAXVj4H67YRoH7vheXdLqzWoppjQ" +
                "iL/A+hTM+hep/cfd+ZWG35rgHvRXCNb6U9dfSr1/lxI/xoif3v" +
                "b5pYaj+G24fiZ9pvZvhd/PL+33M1p9dpug4dlP4fYVzb/Jjs8v" +
                "bFtZ0PWPnGO/V2T9tQigD5jfCf2Xy/xXuZ6/avNfxvdfdHe/X3" +
                "+Fvr5dP/9l339r79/5hePf5N8NrD8sUsoXoN+B/EaYXwj1ywvj" +
                "/Iuh/u/WnxLB2fsHLtqj5CflffvJo74/tL8QPE79snTxF+78tw" +
                "jy56xw8j9mfpxv/0P/rkypv0B4NP3Uyp8ivW9D9K9oyl5uu39o" +
                "Dd3M+AqCfgXy63j662XHB/v6j/jyH+SHYfnqbE95H8D9PkFa+4" +
                "Obv8Z+X4ptv1RuC2Np31nul0Lv77v3W3kq+49bHxjx9zb/5LST" +
                "f1JvhFrmn5jhq/wUtn1L8/8G52+g/BB2fgcFzvFfcemHfP6R/9" +
                "RsP7T1nZQYCSVlywLPh/kVTq8q0i+fuPULen4H0r+D639IeH/i" +
                "1q9Q/kirYpbVCt4sq2zjh6rF+Hl/fPL9LOV+I3fbr2B+RPlv94" +
                "+4+weKFGo/tB+V2wI0/iQP1X+48jF9/e9Y9qWw8mfW/Sjz/hTh" +
                "L977A2H+C2T/J7/f4NYH4tIPiK/H8aNHXfnsPT9q/As1gS7J/V" +
                "V4fD8tf8C6PkL8ErJ/O/FN+Vp8UyVEhPuRkPuly3sfJF58Ypr7" +
                "y9T2I/t9yST4o8PN96u9/AuP+v2x/R9+cOP5TRr/TopPZsT/9/" +
                "GX+fo3reNT6Zenvy0+//drqfqbQv5tZv4KPGDc932J7a1wXn6C" +
                "J/3Y9T+rrgDe5+Xy1zD9hzx/+v2ADNNfIuCHYz9t5H3c1bd4X/" +
                "bZ/H3IV77vywbZfzb9zfK+vDP/ErxPHIQfQorpwL4I1j+c+ivu" +
                "H/Jfavuw8wnWh9+f5bYn6j/KD43R7qcA/vD7nSH3N2j/1LC9ff" +
                "1c/ySTf5jH988f4fqPLP5fpv8J+oeM2/7fyW9A8eX4/UIf/lNi" +
                "WuhNe3fX+v7k+V1Ke/R+JTLLYPw4z//Ljd8Oqw9Ob5/a/3jZ8d" +
                "cZmZPoJP1n3vYh1X/lI38us36jz/ly9K/Cxg/zb0Wtr6g59fs9" +
                "3kcKcnDR8wes9ps7vjja+vze76G/L8Qbn4T/nNGeST+2+OBY+h" +
                "GhPlZHvhjqh/Det+iPLwbjg4Wi8014/4Frv3LtY+H27zvjX/j6" +
                "P78+Aau9BR+r+prM+MlI8svb2KXZP4T7s6TtI/Ano3+PXv+f9z" +
                "5Bmvi7HqF4vx8Z7/1O6D/0JU4tRBhceaOt0y60ftVyB6z+f159" +
                "36D7AQ//Bxc+rI+o/PgXSb101Tc886zf6De/fwG4JGZp");
            
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
            final int rows = 640;
            final int cols = 8;
            final int compressedBytes = 1607;
            final int uncompressedBytes = 20481;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWz9vGzcUf8ecBMZAUdawU43n1IOGLJ3SJcUlTYC2SwOjBv" +
                "ohOnZMAdrI4LFDBo9ChgLZ8hGEIkPhyUuBoFM/SnnSWbqT7vh7" +
                "5Dvaag+IFPiJj4/k+/t7PNJEmhQVpfuPe7SlSe6+C1W4z7L+t/" +
                "mYjr8pHz0H4zFdC+l0N/SrH45PH7ybfnswOvv5/b23p/PHr/bL" +
                "19/99Zk9+fik3KerT49PP383/elNRf/m7emHx69OavqfFb17/5" +
                "vPBt0S4zHgh1307vN1Eo7M+ieKzI/GfdpKrhFLP0ynBNqrX117" +
                "rRrrN43xiD+iv6zPt83fI2jX+eR4/W26XvO3lDv7pLV9Vt9TKu" +
                "rJ4XimfD36zZ6/FsBuTltucW+v3+9f5p8cPx+dTfUB0aP3T88P" +
                "5+PpCVn9wlQDF/bh1w+1bSqrCbPKPoH9Send618/cLwu/c4XnQ" +
                "88P4Z/yTk+RQWcb4D8K+1UqzPbslkPvb2/vy/291m9v78uzsev" +
                "v13j2+dzI/9eQ37TK3+HfXEfFXc+5uar2z7F+gPWB+mmIuQFjT" +
                "f894zln7H9kFz/vPsP9hedD/avaH+8dHl+wfhJ3r89R+ZSOXpe" +
                "mDFlX7m80trMLa3QVLhPy4jvpUw4LRtfxZ/8bDp5U8Wf7Pzww3" +
                "i634o/erF63dgKo7rDTd/5+8dD/wDir3D8H8v1d8bfzK0f0ZeM" +
                "ijXPfENxsH0SQ/tUPYVbQ26C3CfKf4Xz5zchqaEJNXfb+quhwM" +
                "fy1q9jjNsTn+xW/nHe7b9mnvwigD+sP7n1aV99U3YoiLm9+TVU" +
                "rmxzMsOq9Iap7xDbvT063NduEaMzx6Io6ctfTPn66W+ZPbl8wh" +
                "FLav+Avvd9r3wXHPlw/OTuX3R+fO7LD0LyH92xcQifWMbPfv8N" +
                "xwOVOjIXy/zAfSzyA1rmB7TIDy6Y5z+AfD3n88Xkb2UOaPTQ5V" +
                "fZ107ey+sjKsxM0z8PR8d2mPzZO/9Ve/7Jcn6q52fI530G4C9a" +
                "H9G1EL+4BvgPI79tPFlvfksb9aVljjeR9j270Z+I8jAQn81Z/l" +
                "8FCHCb+ZeXP9/++wKBNP5L8fPrnvquYO7PvD1+xb9g8U8f/wjl" +
                "hzL/qtHmZ0EGm8Xot6c+VlqRMRVbR6+OYPLSCW5sdW6jvFUf+P" +
                "NTE5u/ovXV+HAv/8D+iPXhc9v6BfsLUfVNCB3677Tjhf2FVfw2" +
                "Y9UZv3v6B77T5fQv2P0FYB9y/JpbH6nO+BOG/yfAlyH+DfVLiG" +
                "9C/FaELzPql0j/P/PTVUN/UH+Ds36pf0iFHyfeP6z/sD/hxa/n" +
                "TXwxW+CLbfxVjA8L/QvAb3H/UoY/y/Fp1L9F9PCUblh8byD8Lw" +
                "h/NcPhr0PlL/78DOV3veMZ+Vdo/t164u6PpKgfe/IDeX9imPK5" +
                "Pz/yPqH1WxacP8nsM6Q+11H7J5MP4V8IP8P5s58unT8ev5tRu7" +
                "9PcfgdwufA/Iz8sIWf3V/dvwjFzxB+Fyp/KP4L4lNs/EP+Ad1P" +
                "hPYJ8Ckh/sTSTxT8c4n/Lb31LcJv2PhOfP6jRPiP9P4v7P/68T" +
                "8U/8X3e7D9isZDfIWXH+Ve+8+ZWlu2DzxLWh9y8YPdrl9xfVom" +
                "vf8I62esn/77YTtf/9/t/nLq65T3jxn1e2L8GN6fktGl9Re//o" +
                "1cX+L4Krz/Ls4Pk9enzfkLZv2lAvANVP+WJLn/Edwfthz949/f" +
                "kMa/eP68/EwcP6D/FtZfHP6C+hvrB7JfUP+I989vP+UivhTaLf" +
                "FRkVkXv3Ql9IsFh+744vffwfcz+/LbYeI7//0vj/6w7r+oPvwX" +
                "jJflv//1/Cp9fxS9H/F/X39M/8skpt9i/hma/wb6L9yfCNmfAg" +
                "hbbOmi+H41oCP++P60TP9Y+RfLfj2qIHo/ze8fIL4u5M+4HyvK" +
                "H6X3o+X3B1f5s+T+Wh5tvzx8K/r9aGn8vvP4IsT/dl0+iJ+mxk" +
                "cgfsHGP6L696njC6Qnfr8nzr8Fyc/SP9XSKTPYeHF/Our+U0j/" +
                "Vxp/pf1FRvxA/UGPfAz8frf7J6nnl+PDMvw2PX4+IH66LT98Py" +
                "5Kf4mPTyL+Uv8h9j/I/hH+tOP4zG3Mn1S/Y/R/ePl64y+kp4gf" +
                "Iflx6vpAxj/OPw9Hh/gFzG/T4mPS85GuD67/X2FH2sI=");
            
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
            final int compressedBytes = 4681;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVWw2wXVV1vladMi0Yg1ADpWRQQdsKlhpQ1Jj97n2XvMbq2F" +
                "qJlpSYGCaFhAAdmt+X5Jyz7/97yE9CeCkayCOPJBprpzJj6Uz5" +
                "iUqwGqQwIBICgvwFAigWxDY6XXuvs/Za6+xzufbu2Wuv//Wtfc" +
                "+559xz36sfqR/prqoDtSc1ZncedJyXOulY/Uj76XRO/Uilkp6Q" +
                "aw+T3Y3uybhm76pUgK5KP26PB9+Xna4CryzPZv8w3dP8ktOh3L" +
                "jd0WRo7PRK/uKcWnLy0KvJBHKk0d7JiaRzFGf2F9KHqD2dsttD" +
                "jmvdbk8D/gXpWaxvZ1jfuxkxI/Zjns41I90DjvPSX2WjZqTxVD" +
                "rspb/OtYfJ7sbYnuD9N2akeY39O3u2/bDd5zUftR9vPJ1b53TH" +
                "sx9xXOOnefzX7V/az9jz7OfZZs+3C2QN+6GhV9KO52q5ZlhYz7" +
                "W4zvP00zA/C/Nz2UPC5yOezjYjrW9ZY4e8dMhWbb1t7fuBf0HV" +
                "+6T9lJLn27/13DKzzE55utss6/Uc5wa8J1ebZc1T7XZv24Va2K" +
                "dlPCoVydWfszfZG9374T3/2X6tfVcedUulMjyD42Cf8ih7M8w9" +
                "nMfr/lXXaH8r7cpq2hulvOZO5Ow3ZDU7SR7tVkDnj6fufPvnwL" +
                "+g68n8YP26/RfPZSYDi6e44oB9us9knRVkIy17FKXqJZiBs3Wf" +
                "Q4v9ZaXSvIo9W6dyBnzJnDorYJid9gZhYIqzekmZB9eiuvYsmY" +
                "98ZH77C/uq5ybNJFg8xRUH7NNhM9l6mWykZQ8zaVdIqXETZuBs" +
                "3UO5H+xT64Ps2drFVXLsk1Ijs5rJztz0yv4YJD6qXqk0bizz4F" +
                "phnxbJfOSjenT75LhxMw7nnaO7wWOchu1kL5rxzm1w3jlpF2ql" +
                "R1GqLnfnHWndede7Lc8F513rU+zZnhaq0HkncsJ5Ny5H53B6i6" +
                "xWhiGvuRM5+43qctHJJHnk+zROHNR6VOYjH5nfn3eOu9RcCpac" +
                "IodS9jKsU2yTa1FyXPXicHR4DaxTZE33DH1IRAa9jGCdrtGtoX" +
                "8ZBq4mO4Dz7uIiSk+nJDpCWKzXR77IXGTXe5rCbDvOSx17srmo" +
                "s9cudxL4onaC7G60TiLO2e1W2I/98B6tQU2lMjY9j7rKJs0NHN" +
                "eeQVG2aRt2M2XPdaOyBvR8IH1GYRiXVtTarqc9mBthbmqulz7e" +
                "tgG9bYvivPQ4WFbrehKNkJumCZynuOIAfO8xzd6ZZCMtexSlVh" +
                "czcLaxY8kP7oI+zJ7tszlD/r42pUZmNc3u2vTQIAxMcTrssUe2" +
                "g7JT3cZRMh/GFvOjPDxreBZctT3FFQfUeu/wrN4FZCMtexSl1h" +
                "7MwNk4Cj6fPsue3e2cQfrFKLz3/2ZvGoSBKU6HPfbAfZJ1G6fK" +
                "fBhbzI+ymTATsGOe4ooDjsl3m4neF8hGWvYoSq1fYwbOxlFwPP" +
                "2SPbtTnEH6xSjcqD6SvWUQBqY47bvKPPLjSdRtnCnzYWwxfy4v" +
                "NAuByylyKFVPMwt7G9km16LkuM5o6NpriEO+dR7Hdb/DeulHOl" +
                "2j+uPsxH4YuJrsALFrlI7m+yQqN2bFPfWRLzFwf0gUOZSqnzCX" +
                "9L7PNrkWJcd1bw5dew1xyA+9znHd+1gv/Uina/QOZu/uh4GryQ" +
                "4Qu0aJeo0O9qka91QuJ99O9tr1dmFyjU3Tr9l2OjP5YTqePATX" +
                "uw8kP+o9b5cnB9w3w+SncDU73U5kz8A3z1dg/jccIycl/5NW0r" +
                "eA5Yz0KLs1eyrdn37ArkmPSd8B9xVPh6sKXO+GXvPfWX+SPJE8" +
                "Wf9Z8lTyHEjX0fUOMrw5fWvj3PT33fUOpLelf5C+M7kzuSt9f+" +
                "/J7DTMk9yXPAB2uMdJHob5WPZsstl9D06et117QfK67YEVzgC7" +
                "yZ6R/p7PeryP25d8325I7sfrnT+65+f7VE+/alfn2X9OeJtvyz" +
                "Vb0t/xWaal081WA9dzpLjigIxnmq29w2QjLXuYrXA/vlVGYAbO" +
                "xlGAbCl79k7WMbqurmG2jv1RZvpjkPioOmD/szIPrhWOpwUyH/" +
                "kUu/Jc3dTh/snRFGbbcV7q1J839d4iuH8CCXxRO0F2N+D+Keec" +
                "Xdw/eY2vgFFwPLlsNHozKSrcP9U5E9w/1aU89pFsnsIwLq2ohf" +
                "snR3swN8LcJKvlHhvQG+6fAjrYp4VgWa3rSTQs++dBmxtXJ9/l" +
                "Jy/Nle45TPWE+pGxNzldup6f7CSHk5eItyvkMyM4055KV1MWed" +
                "7542kr52/fj8+fMBJ2d13yG4/jWtLIp0fVh7NPQN396drwxOlB" +
                "iH60fiR7FrCtwudP9OwIXw475H27pyv52VJ6RUW9GmPJM/k59j" +
                "MZ7zW/Tpfy8yiz3WyHHfMUVxxQ652sI4v0iCX05mwcBft0K3v2" +
                "TtMxuq7O6jTZokEYmOJ02GMPrhX2aULmI59iV56rmipwOUUOJT" +
                "vLUbbJtShhbOi6ShJZYZ+e5LjeKtZLP9LpGrBPS/th0PioA8D+" +
                "wSJKRigrN26Le+ojrzQrgfMUVxxwtKWsI4v0MCvhc3yljEBvzs" +
                "ZRlUptN3v2Mh2j6+oaTpP9fX8MEh9VR+yxB9cK+3S3zEc+xa48" +
                "d525DjhPccUB78kXWUcW6RFL6M3ZOKpSaQvf3r06RtfVWZ0mWz" +
                "0IA1OcdnGZB9cK+3SvzEc+xa481zZt4DzFFQfUWsI6skgP04bj" +
                "qS0j0JuzcRTs03z27P1Qx+i6uobTZGv6Y5D4qDr0X+rBtcI+dW" +
                "Q+8il25Vb5i4S80gC6L9Gn/Z2Vst866keK1zv564nM5r9LPs7R" +
                "vdfxeqcj+v3eAkiu+v/+3uKqxb+3FNHBHeUxMl/Z7y3henecOQ" +
                "52zFO/d8fhgOPpUtSxB6844Hg6jvXiU7mQzR9Pn+O4sU+zXlfl" +
                "ilLObuyHQeNDFG42a9JHI5SVm8MyH/kU63vucnM5cDlFzo3ajN" +
                "oMJ7NNrkUJY0PXl5NEVvgcn8FxY3tZL/1Ip2vAPt3UD4PGRx3o" +
                "atKm0cE+fTHuqY+8wCwALqfIoZRtd5Rtci1KGBu6XkASWeFMOM" +
                "hxY3ezXvqRTtcAJJP9MGh81IGuJm0aHZxRc+Oe+sgPv9HIdmoZ" +
                "Yn5LyUfvJou2ZVMyqhgXy9muN7InJ5LOUZrF4XTZDvbEV/bVuI" +
                "s+8pXmSuA8xRUHfD5tYx1ZpEcsZd90Gs7GUfD5tJg9xw5yjPSL" +
                "UeR5bx2EgSlO+5UyD64VjqcRmY98dP5cXmHcb5k5Rc4N2xne7W" +
                "S2ybUoYWzoegVJZK1Uhv6J48aeYL30I52uAft0ez8MGh91AOfd" +
                "7iJKRigrN++Ne+oj7zA7gPMUVxxQa5fZkd1NNtKyRyxhBs7GUb" +
                "BPt7AnnHcqRtfVWZ0m2zcIA1OcDnvskYXs4by7R+bD2Lgrt/rn" +
                "A+c1FybXwLnxhbFb6D6iNr02Xd4Zgdf5+LyA7lKaC/QdDd2dSK" +
                "n5eUJUm14p3IUlQ8l1+g5qbCfGNS/QdzDZ/vi+LH96sZnun/Qd" +
                "XBG7vH9STzEubi+Uzwua89UTgy3i/mmtWQs75imuOOA92ck6sk" +
                "iPWEJvzsZRcDzdUBYXjieVRWZ1muyBQRiY4nTYYw+uFfbpQY0m" +
                "rh/k6831wHmKKw74LPy2uT7bSTbSskcsYQbOBte7YGmm7AnnnY" +
                "rRdXVWp8kODMLAFGczKfPIzztRN3tU5sPYuCvP1UwNuJwih1L9" +
                "ZUfZJteihLGh6xpKLgPx7aVxnI5gna4B3VzYD4PGRx1w5aJNos" +
                "PzLu6pj/xl82XgPMUVBxxPd7OOLNIjltCbs3EUnAmbyuLCPqks" +
                "MqvTZK8NwsAUZ1wN9dwPvtrLNZq4fpATkwDnKa44YJ/eyzqySI" +
                "9YQm/OxlFwJvTK4sI+qSwyq0fyvkEYmOJsdss8uFbYp0s1mrh+" +
                "kBebxcDlFDmUqh1H2SbXooSxoevFJJEVEI3HcTqCdboG7NMf98" +
                "Og8VEHiF1XY4Sycntt3FMfeb1ZD5ynuOIAdH/COrJIj1hCb87G" +
                "UXAmXFsWF/ZJZZFZPZI/HYSBKc64Guq5H3wNHa3RxPWDPMfMAS" +
                "6nyLlRO7Z2rJlj72CbXIsSxoau55BEVrijOTaO0xGs0zVgn+7s" +
                "h4GryQ7KqjmaX+9E5XY77qmPvNqsBs5TXHEAuv2sI4v0iCX0tm" +
                "ukRHxrYVlc2CeVRWb1SO4ahIEpTvuDMg+uFY6nYwCv8izWD/IW" +
                "swU4T3HFAdfWF1lHFukRS+jN2TgKPjGOLosL+6SyyKxmS/Vou3" +
                "cQBqY4HfbYg2uF4+lWjSauH+TLzGXA5RQ5lOwBR9km16KEsaHr" +
                "y0giKyD69zhOR7BO1wAk3+mHQeOjDiDikWI1Rigrt/8j7qmPfL" +
                "W5GjhPccUB78m1rCOL9Igl9OZsHAWI9pXFhX1SWWRWp7HfHYSB" +
                "KU6HPfbgWmGf9mo0cf0gj5pR4DzFFQfU+pUZrf+KbKRlDzNqV0" +
                "gpRyGycRRcgX4uPXWMrqtrEJJ+GCQ+ql5WDfWUPezTTI0mrh/k" +
                "dWYdcJ7iigPexQfMOvsg2UjLHrGEGTgbRwHyV8riwj6pLDKrR/" +
                "LQIAxMccbVUE/ZqW7nKI0mrh/kK8wVwHmKKw54Fw+zjizSI5bQ" +
                "m7NxFNwh31EWF/ZJZZFZCckbY2CKs3l7mQfXCvv0Ho0mrh/kRW" +
                "YRcDlFDiX7sKNskysOOO8WsV58Ki8iiayA/HGOk3rpRzpZw3+O" +
                "/7gfBo2POoBqjxWrMUJZuRP1VFbfren12cfSiUoF/65H/hpoH5" +
                "PPrLKPNtbQ3/Wk/mrZCL/u4P+VsW8avo83+C/a9zdWxc/JKCr5" +
                "jY/73ewUH7VWezTOaFyW7BfPz+DTIHk0SCf6mA2hprt/OgjraP" +
                "r27JHG8mLNxhWN8PcFnSfc3/U01rnndN6W/0Vi4x8a/+j+rke8" +
                "k3PNXKbIoVQ921HpIe1awlidk7g8y3/FcTpC+M7Vsl3aD0N5B4" +
                "hdV2OEsnLnpbinPvIN5gbgPMUVB6B7gnVkkR6xhN6cjaPgynKw" +
                "LC7sk8oiszpN445BGJjitD8p8+BaYZ9+odHE9YPcMA3gPMUVR2" +
                "2kNsI6skiPWEJvzsZR8I1rpCwu7JPKIrM6TWPfIAxMccbVUM/9" +
                "5Pv0qkYT1w/yzeZm4DzFFYftDH+PdWSRHrGE3pyNo+BK/b2yuL" +
                "BPKovM6jSNewZhYIozroZ67gdf3RM0mrh+kC80FwKXU+TcqE2r" +
                "TXMy2+RalDA2dH0hSWSFd3haHKcjWKdrwD7d3w+DxkcdlFVjhL" +
                "Jy9/y4pz7yKgPXIaS44oD35D9ZRxbpEUvozdk4CrPFcWGfVBaZ" +
                "1WmaJwzCwBRnXA313E++Tws1mrh+kDca978ZnuKKA2rdwzqySI" +
                "9YQm/OxlGYLY4L+6SyyKxOU39pEAamOONqqOd+wu8ICk1cP8gb" +
                "DNx5IMUVB1wznmMdWaRHLKE3aodGOZ/nflAWF/ZJZZFZXabmik" +
                "EYmOIcGi3z4FrhePqmRhPXD/ISGFOBIucl+6KXvQZ8lwjPJUXJ" +
                "26fC/0h6jefJb0pGNjeGKG8JfqRbImXOGGMINXQHU9qHbRod7N" +
                "OzxZ4Eai3PM/OAyylyKNmXHGWbXHHA95Z5rBefyqAZWkccWoZ2" +
                "cpzUcwTrZA2IXNe8sh8GjY86cDHFaoxQVu6+WOwprp/Lm437vw" +
                "lPccVRO6V2CuvI4nQ8pL+zoTf65JiCRUZyXNinzVIjszpNekhG" +
                "abvE5yjOuBrquR989abLnshH58/llmkB5ymuOGrn1M5hHVmcjo" +
                "f0dzb0Rp8ck7fYqwD5OWVxYZ9aUiOzOk1rm4zSdonPUZxxNdRz" +
                "P/k+HS97Ih+dP5c3mU3AeYorDtup1VhHFjvBHqzNbfi/RZtcHE" +
                "aS3d8/1criwj5tkhqZ1Wla/yajtF3icxRnXA313E++T+cQXumj" +
                "8+fyNrMNOE9xxVGbXZvNOrI4HQ/p72zojT45pm3Ey0iOC/u0TW" +
                "pkVqdp3SajtF3icxRnXA313E++T+fKnshH50d5+Kzhs+B+w1Nc" +
                "cdRm1mayjixOx8OukFJtJnqjT/7/0cEiI7lO+D/qs6RGZnWa1k" +
                "EZpe0Sn6M442qo537y/6t6q+yJfHR+lE3HdGDHPMUVB1hXsY4s" +
                "0iOW0NuukRLxLlscF44nlUVmdZrWY4MwMMUZV0M995MfT3MB72" +
                "rpWawf5GEzDFxOkUNpeLUZbp3ONrkWJYwNXQ+TRFbMVozTEazT" +
                "NWCfXuuHgavJDsqqOZr/Hiwq926KeyqXK/8HacUUqg==");
            
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
            final int compressedBytes = 4302;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdW3uMnVURv6uUlwoIqRoSY6lGQUJAjf/gg7P3UYV2S02MgN" +
                "CKMekSQggPESrGfPfer7t3FxVBIdpYipVFiIkofxgTY7qlu+ur" +
                "pVgKYnmLogKSIqhAH3HmzDdnZs45915gv3xzzsz8Zn5zzne+17" +
                "133Zfcl2o1V0nq4dZY0liCuvjIyn5tpV5jSc3/EYb6HMVWG1cL" +
                "f9qnNdI7UzYqRdsR5NjILnj6m/5JOqaU3/fOdGdCr5LUI627D6" +
                "X4dBtrFBtGfSZr7IXevWmcjVBYw7H+6WWL+tVg6+MRQMzfYjap" +
                "UDNP35+OqY9+sbsYepWkHm6NpY2l7uLuAfGRlf3aSr3G0mo9LS" +
                "WLZ6ii2Grj1Dwpn9ZI7x60USnajiDHhrJzm+CreXoxHVPK73vT" +
                "bhp6XlJLW61WglZuZh9bBZFqlIGso+tqtckTxePuE+Tk+22M5b" +
                "VZMdPkB4bVIJL20XU5RDVPive69+p8FJuOyvfWORgRSWppg3ka" +
                "ERt7NCLVCE3W9c9KPt/bmYsL82Sy6KxomfzEsBpE0g7nXQYhXG" +
                "GePmirSfmDPuWmoOcltbTBPL1JbOzRiFQjtGSTqFqtOZOLC/Nk" +
                "suisaJlsDKtBJO0pG9llPNU8jdtqUv6gX+guhF4lqUda+WaU4t" +
                "NtrFFsGPWFrLG3VptYlMbZCLFZDpinZr8abH08AmA7JGaTCmPm" +
                "lC+rt10bel5SS1utVv+w2NijEalGaMkmUdB7IBcXqjVZdFa0lI" +
                "uG1SCSdqw9RQiXmidTTcof9DVuDfQqST3SWk+jFJ9uY41iA/ca" +
                "1tgLvfvTOBuhsGusjpXka7D18Qg4QuOlwpg55cvpNfPX+UZrP/" +
                "fLw6xv/fnoK56rkL9af0GIgvu+xIH+C9bXnxfq2VV7DX+dE3xU" +
                "VNXkpwfFFMentvJQanVVg/+KvZ75HFPNN9V8HuuOZck6afXTyC" +
                "YIaWMNe3FOyeZ7u9M4G6Gwx1q9s6lfDfEIeMfaLZtUGDOno4j5" +
                "sW3txzknSUeANjgm7yMbS0EKhlt75OJsfk08FscVozZC57L65F" +
                "mWzXqL42UEvHcfjdmkQqmO69bIHD+27np3PcyYl9TSBt4X3PX+" +
                "OTMgpM1rlEGySRSM9XFBwnOmibG8Nita4DlzSA0iacfaU0T1nK" +
                "l4pV6JT0fle/AHvUpSj7TWzSjFh3/tZ53602jCVtyONfbCE/IP" +
                "0jgbITbLUau1n7NRKdqOgGq3bFJhzJzyZfVL3CXQqyT1SCtPQy" +
                "k+3GCeLpFNowlbcV/CGnthnr6XxtkIsVkOP08mKkXbEUDtp8Zs" +
                "UmHMnPJl9eVuOfQqST3S6u9BKT7cYJ6Wy6bRhK24l7PGXpinjW" +
                "mcjRCb5fDzZKJStB0B1W7ZpMKYOeXL6le4K6BXSeqRVn83SvHh" +
                "1jmH/drKsYH7CtbYC/N0SxpnI8RmOeB+d66NStF2BFS7ZZMKY+" +
                "aUL6svdouh56W3LaYN1u5HyCYItxjW02LZNJrjJUKy+Xn6fhpn" +
                "I8RmOfx6MlEpWiTt5YdjNqkwZk5HEfP73o3uRuh5SS1tcEyWiI" +
                "09GpFqhJZsEgX3rCdycaFak0VnRctkb1gNImnH2lOEcKl5MtWk" +
                "/EEfd+PQqyT1SGs968YnThGfbmONYumvu44snmGc+xNvTeNUtc" +
                "oXc0Dsi/1qYLQdQa3WvDpmQ1k9F0TM3Wtivqx+q7sVel5SSxus" +
                "3Y+KjT0akWqElmwSBWM9KhcXqjVZdFa0TM4Oq0Ek7c1rcgjhUv" +
                "Nkqkn5We88Lk+x/loQnpLb9fQ52z7Ndi+V97vOtsHvT5PPD/Jy" +
                "zs4JuTey3opcHWzLvd9NHP1a3+76Y+xzu9vgNrCkljZYT6eLjT" +
                "0akWqElmwSBVeMHbk4XYVYdFa09MaG1SCS9pSN7DIeYdbVpPxB" +
                "v8pdBT0vqaUNuL4uNvZoRKoRWrJJFNzvXsnFhWpNFp0VLb3PDK" +
                "tBJO1Ye4oQLjVPppqUn/UB78Efb+3v/Pi1vAfr81av2M6d8i45" +
                "uk/wnZnX9x7cefj1vgeXH8u9B9N1/A2+B290G2HGvKSWNuA6w2" +
                "3078EBIW1eowySrXereCZOEmTvFhtjeW1WtPQ2DatBJO0TJ+YQ" +
                "1f1O8Uq9Ep+OyvcuchdBr5LUI60cdRf17hKfbmONYgP3RayxF8" +
                "b6AYnr/VTsGsc2ywH3i/F+NQibHgHU7uIqUVbzFDGnfDm92Fbc" +
                "I9f99gnt9xQ7YS0/COf4quJPvS3Qf9h/jvkU7H/HzzPhjPk3tC" +
                "/5Nf9qu9Y+xEcerrK8rX1ccs/6pMc/UTxZ/KU3W/y1+AfglspZ" +
                "135zexFY3lLda49qv6P9zmK22ErzVH1idV9xP+GLh2B/DHBLoD" +
                "2+eMbnflnY6qvaRyKuvdijf1P8Afy7Oreldze2FHuLF9L7XftN" +
                "vpqj2293N7gbYMa8pJY2OCZNsbFHI1KN0JJNomCezsjFhaNqsu" +
                "isaOltH1aDSNrLRg4hXGo9mWpS/qB3XRd6XlJLGxyTk8XGHo1I" +
                "NUJLNomCsS7PxYVqTRadFS29HcNqEEk71p4ihEvNk6km5WcdV9" +
                "fklZPwXjx52eTl+r5Rfr61H6/j8rkuf48QPl29dNjnvuq8K+VO" +
                "0rslvt/lPpuV3L1N6efJ6ow5Pq2gPC/+zFrud/2+R0hr7jyv7n" +
                "ebHFRBklra4Jh83G3q7WEfWwXhNnUv1Vp1fFQ2iYKx9jTSxlhe" +
                "y4EWuI73rUHXx+xUe4qoruOKV+qV+Dg/6a0D/bbOoa0DE6dYG8" +
                "ztUE2sYukcVt8ZI1naLHFW1CdOHeTvLNIZaddsEjVxsmatVmYy" +
                "iryO9zs3U8wV824GZm6muLfYWfyxeNDN1E8v/uRmwPKwX5dPFX" +
                "8r/g7ac8Xzxb+LF4uXiv+Cpu53GO9nf4bvd72p4rfF74vtmAWw" +
                "j/P9Dlp/v0MsbqAfxPtd+zC830H8W+39DmJ/V+wAFNzvit2gPV" +
                "A8VDxSPFY8Sudd8QzY/lW8DJGHwn4E1t4+sn1s+5j24mIBEHC/" +
                "A8SuaoR/Lvao8+1psPyzeJbud8V/vO1/xSvF/uJAe0Td777lvg" +
                "XRXlJLGxyTC8TGHo1INUJLNomibGlcWP0mi87KlQyuQSTtKRvK" +
                "+gUyHmHW1aT8QV/r1kKvktQjrVzj1vYeEZ9uY41iA/da1tgLa2" +
                "ta4nqzYtc4tlkOf33qU4Ow6RFA7avjKlFW16eIOeXL6qvdauhV" +
                "knqk1T/nVvvn8dViFz9ucB1fLXbFvZo19sI83S1x2q5xbNMc1T" +
                "z1qUHY9AiodsuGspqniDnly+rL3DLoVZJ6pNXPRSk+3cYaxQbu" +
                "Zayxl7LFcTZCbJYDVscX+9Vg6+MR5NhQlhfa6khL+bL6de466H" +
                "lJLW3whj8nNvZoRKoRWrJJFKyn+VxcqNZk0VnR0nthWA0iacfa" +
                "U4RwqXky1aT8QR918LzHknqktV5y/klQEG4UbbLBeRfQ6Avcox" +
                "TJaD9PuyVO2yVCbJpD8rA9h7YjEGbrowpj5pQvp4fniC3VG+in" +
                "2DK6ID5+Li3b8B68JXluxc9958oJyVI9Fa9Xz+MP9UaCvRu9cW" +
                "7hOPpdT++Q6Fc6ZU9hiIW11pbc576jC51XCNNbJHFlkdbOFvpd" +
                "T76qEP/d8tvFfDVP4TcKbfx+50F/HT101H8SjhKfn3LztP6w9n" +
                "cg5pp+7wWUSf91jw5M1xYHfR030jzlYosd3UXhmecB2B+pYq8e" +
                "vbQfW/sYL78i1m50BNo3FU/3myd4fhpP38Va8/ZTS6h6OrzdzK" +
                "Pk34m15gmBvxOL3+8kS/XbsvA7sdEfxp+KFqPhbW0ec8o72/o1" +
                "9v1u6rMa4/v7uYrWfPb9bsp751mGzzrnk1Uzr9/v9O/EiJGrbt" +
                "aatd6r0N/We7lWa/qt5vfRe5u1+mbst7bVghXmaRv3Bc96axtb" +
                "kzNhs+Bbe5sqEqMwp2TtvcJ+0rEKwWC/WeMqxKo5sfZahZHc9c" +
                "2t5FshthR7m4mH4qlqd7Y721+rKkk67uVN1kM7fl7AfY0nnX2s" +
                "qcp/hNcnjbco4cXrE/vJWt7cUxhdjWbT2SY+xLbeIpu7/+cFqV" +
                "8yw7bKrcLPVUBeNnk5am4VeGBvvYqS+iRx9/O0SqyMJ519rKmj" +
                "82qMtyjhFRxjKDZlsWw628QXbe06d995SvySGbaVbiWedyBfRo" +
                "kbtrDaN7iV/nuElR5bWf08rRQr493K+gbBkddcWTfEeIsSXjzv" +
                "2E/W8kcao6vRbDob1i42zj3486e0ZsnsVjYONvy9puU/JUcNdd" +
                "zrGxsHp2axjz62wvXpHu4LHnv1jTpLMk8b8byT/BKJUZhTsvYO" +
                "YT/p3fGewiAeayK8WDUn1k44eC5Quak+s87v4XmKaybGMMp9jX" +
                "3Q85Ja2uAo3tXYN/UH9rFVEKlGGSSbREHl3xTk1HYbY3ltVrR0" +
                "x4fVIJJ2zaZ9nF1mQ+djTDwqbN2YG/PXpzF/fRrDDVYa7OXP3d" +
                "jUk9Qnibs/78bEyng3Vt4tOPKa6/imGG9Rwis4xnTHNUZXo9l0" +
                "tvJn2mZz9z3vEr9kdmPNkeaIfy5Y8M8FI7hhC1y/bI5MPYX91g" +
                "JZcIfzboH7gme9tcDWZJ5mBD+1nVpGtRYwp2SF54IRjfDfSwUM" +
                "9rGm5kj3CGp1BdUz7KOoUaU6d2shOe8WwnPBSOyheKrarXAr/J" +
                "xVknTcy19bD/d0X/DWx5q6A10Q4y1KeAXHmOnDNUZXoNl0tonz" +
                "41FZf+4v9Utmt6JxoHEAzkAvqaUNZvEMsbFHI1KN0JJNoiDbM7" +
                "m4cJUwWXRWtEwfMawGkbSnbGSX8QizriblZ90d546DGfPSz91x" +
                "tMG10JFNENLGGvbUcYiy+fPutjTORojNcsA8HdmvBlsfVcG1Wz" +
                "apMGZORxHzY9vY3dgNM+YltbTBebdVbOzRiFQjtGSTKJinW3Nx" +
                "4aiaLDorWqbfNawGkbSXszmEcKn1ZKpJ+VkP75Zz8feN9WZ4C5" +
                "zT3yy25uJfwKi36bn4N3fhc5Vn0vfgcMWcw7j895xUicYQCzFh" +
                "P/ceTLVX1ajcPEp1tZ7r98s6YuRqGjsbO2HGvKSWNuCqi409Gp" +
                "FqhJZsEgXztDcXF46qyaKzoqW8Z1gNImnH2lOEcKn1ZKpJ+VkP" +
                "x2hrsp6WhdncatbT1vR7c5slt56mTh6wnrZizv7fm0+frjFUAV" +
                "WB/ex68rVXlarcPEq1arb2XU9bAweup/sa98GMeUktbXAU58XG" +
                "Ho1INUJLNomCefpyLi4cVZNFZ0XL9JnDahBJezmXQwiXWk+mmp" +
                "Sf9XCMZuP1VG4Pszlr1tPsgOsT+Lrrcutp4gsD1tMsxvW/Pk2f" +
                "pTHEQlVgP7eemlf7bLMsw3qaTdbTbPeaPutpNnDgetrV2AUz5i" +
                "W1tME87RAbezQi1Qgt2SQK1tO1ubhwVE0WnRUtnU3DahBJe/fR" +
                "HEK41Hoy1aT8Qd/T2KOf30nHvdzZ2IO/Y23sQR9bESl9RJNmfa" +
                "R17lTraa3gOzMcJ28Iwis4RnXu0BhiISbqcy36eRw1wkjuzm39" +
                "31tsvFQlc1K938zb1dfaX8K52VlIf6NXPP/6fq9eVa6+YejMJL" +
                "/6Ozjod/Cd3+D3COqdVX2P0Od39lfL9wiKN/P5E3+PkPGY7xGi" +
                "/xpY1rijmt07yj/C2XKL9rIv9zfI568YX5X+9JWvNYoQ3fFhiE" +
                "Fsg+dp4P9QfCqT+aSK9XZuSzhq5WZT0e0Dqs34pq9V+b+m7OuY" +
                "c1BGyTv91b6zf1KeWbO98XniOfF//wcZyslC");
            
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
            final int compressedBytes = 3362;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9XHuwVVUZv4KogYCPEGEohuZ2E7RmmtGmzNG77zl7ArXGzJ" +
                "pppj8MIbEUs+kl1cxa595zuPecmv5Ig9BxRuImNlMho/gq1JRH" +
                "4fuBgAISyghXvSSSoEatx177+9Za31pn7wu1z6zX9/zt7673OV" +
                "BZUlnS0VFRuS7Np+9FoBmOLeG2tDTI4trAT2k9o4Xb2Kqk9G1p" +
                "hwFynfo2UxLgqyN/sD0j474V1qielendYcrkB8Lf8g70GB71UL" +
                "xmHeoDqN7sNT5jFsFusy/ElagpGxK7/9RWdJR6TEwUjlsrt5pc" +
                "l+bTfRfQDMeWcFtaGmRxbaDftuxq4Ta2KinNP7TDALlOtgfgwf" +
                "vA+4M9I+O+laotrSwVNZXr0nyS7wHNcGwJt6WlQRbXBn4FkvWx" +
                "vhZuY6uS0vxjOwyQ6ySx+xLgC8UJ2TMy7lthjeondJl+YMr6ye" +
                "64MzzqifFUnG5C4+5G22e7p7koODKEBTbVp9fHHZNxR+CrdmXx" +
                "XW7K+kRvfloemUeWx302+on5qaudlrYbmZ+6aM+NxcckTl1hHp" +
                "+elRfUz/R4nwv3G35S3yU2z241yIjwz7bDys8XcVod68NUf2rc" +
                "XiwSfZcWjVllWWWZyXVpPvVplWUDt2EJKKlWepa2ALK4lp4Fks" +
                "2D2Ibt10UhKb1Xt8MAuU7YG/B0fwKvWg7saV3Xvq3h9gIxP320" +
                "3PyUdnrzyvuIj7jNw0VnNS3RfC8mQfUnHws97mg5cgx2Vjt1id" +
                "sy1WdWO2WcgA8yQAX9dJZrszUGIZoF8q3Ruqx2ujgwCpBoHW/b" +
                "BQQ62W+gvWGasU3EaRZGEI6N+MyozhA1letSf0ScZlVnqDjlEl" +
                "BSrXSqtgDWQEsgmgqSzcNgAcv5KDSl+V47DJDrhL0BT8cJ+5Vy" +
                "YE/ruvZtjXy+Wp+vred56936yDy3Pp0Zk8Xc1uhy647pTyHPxL" +
                "ibSfGI/jSzY4RP5bE8TueHeZRe+rGYrM0tGacbiyH2vcUQHx2q" +
                "yqN5nD4vdoe30TxKLz07Jou5rZvLYeq9uhhi35vNI/rT2SOO04" +
                "Y8Thd5cdpQRI9uN36P4rTsmMaJQNW4k+KV3WdaXtZU1phcl/oj" +
                "4jQbaIaDJfyWlgZZXOuZS+lhFK4WUFor22GAXCffm6bD+4BnjM" +
                "b372u4O5qky+fVLy96vpPt+lfR+W4lOn19pdzfsn5F2f2TwW6j" +
                "ql/Wccyeyrr8Tff6PJ/m60EbS1fWohjuLYcpLu96xho2r6zfqN" +
                "f78ujPDfNienR74K6jWO/uLIYY9Zxvtkdc/ukZNnnPsDkH9wwn" +
                "HxEz6C2al51Jv4hbRs+cg3uGdS/XVH4p9PmeYflBMVsd4rhWs3" +
                "n8VpoP52D8Bga7QjEJa8l53LfuUwr1p4353+Rb3nq30cG/MMxz" +
                "2wnq862Hjul6t5GYn/ZSvKNa74YqQybXpf6IOF1TGVL3BbkElH" +
                "RLWwBroCWQI8nWo7aO7de2Kim9V7fDALlOCSmR3Rcgv4AX9P23" +
                "kqU453+gTvv5PYH+iDhdq2kmB0mQMaW9srjWVJz2uXossTWwLb" +
                "vd2mR7s7lsKryBSb63bAW+DqMzuLEk5V+hfZT9Vfh6jGUrA3uS" +
                "PcWeYZsF9xB7UVFeUvlu9hr/sijfYG+xt9kB9g77V+9C9h7v4M" +
                "fr+QnNVeP56Zm1jezv7HE1J73AdvLL2CtsF/uHoL/KXrfW9iN8" +
                "NB/DT+TqvpafzCfwM/hk9jB7ROE8xP7GnhBST7Pn2POifIFtYS" +
                "+zHWy7jhPbJ/I32SGheYJIHxJxup+P5afxU/gkJk54bAPbJPJn" +
                "M19b2bZ8fN7P9gjKXjbE9rN/itpBJfEuO8w+YP/mx/FRCs9Efm" +
                "r3vm7hxeS6plv162UOPFy6La1rHk0xNV0f2Orr2RpAs30IJN8N" +
                "YbDxmTcQ7/+I6w0QYs9SzvdHteujoLfie1+B7sZ8v7jI7qOKO8" +
                "ZZiU9E9ROIe657LGnxN68f7+9S66Opm7vW4+64xpipfWb1nvpx" +
                "al98Uptb5WXhm0J7fKp4vaOT0sxXuOomw7PlgILXOxwn8vuWbT" +
                "Eu2MT+8jg9QeEwNErDYG8bp9spbePF5Vn96be5jRX+/ExFudjT" +
                "2Nrudjd8E9x6pmx/amwpdqvMB0v1p4M62eOuca5I5xk6yGFK8W" +
                "fgcLQ/HfRraP+02aYbvJpGafA7QhxHbmVYxn5TLlafZIJOqr0j" +
                "5+xSM90EtHtTcpgipHYXjNN/CJTbsWW35uwcBZ3vxDiMLKWR3B" +
                "uzheXCMu6bZtTFkLL+dJFI3YaneyIfwhK0lbIcyQMuNQ74G7YM" +
                "xpksJu8LFrs5jQJbjaHKKBN1UpjezON0uuHZcphS/ElWRrkT/V" +
                "pIAvBqGqXROC1mK2TX5dg8/jCfy9eydfxPfIOcn/i9Gf1+lV+F" +
                "JOeJNJ+9lbe92ysuVia+iegRD/aMj94hHcklr/HOqeO5ODfLfW" +
                "Yu/YJILwvZBUqDuFdJ1O0EP8VBcZ8vx/bwwLcjYp/pnCu739ZJ" +
                "2dqfr61PGJ4thykl7iSicwXY9K1rTR+HoVF4fOztPfsczAuf76" +
                "pPFj3f4X1F6HzXnOye0Iqf7zDKYuc7iZ063/nrfdHzXeVA5YA4" +
                "Eatcl9lnivgcaJxheJpqSXgtbQGsgZaoTaH08lO7ZQVbBUoMA+" +
                "RZmkJJgC90X2Ch8f2bdugc3NtfqXrn4O/0LsXnYEFV52B+rTwH" +
                "9/4m81ylzsHCmljVR3YOFvyS52CBodA5WLT2CGQFzsH+SNGpck" +
                "7lHLFuXGWPjco5dv/GK7mUxyX0YEmRmu32465FeiTj8Qf7cRuX" +
                "9OjPB/S40++EZx931Fvac1N1H87zuatxHfBwmc6NvG+QV10d04" +
                "rZ9GVkDTBRuo1tNF5Xlh8Me6Ytm+jxd3Nf14v0Sf/vPLJfH/Y8" +
                "H9OKn8Qar9oydm+m9pnam4u3tsL1ww8VPd+lV6ZXQq5rutW4We" +
                "ZYAvPtlta1bZqarjfP9vVsDaDZPmIY6DcQ+6KHXG+AEHuWcr4/" +
                "qp0jnZ/Ot8/BtVHAw6Vp2fcqtqT/NCPfMRrf0XGHZGQNMFG6yd" +
                "0UXh9fcnfYM205nZfOU9GZlvf2XwMPl6ZF2g7yGntiWjGbvoys" +
                "ASZKt/EajdeVrU0Pe6Ys26uFHpeNJbRMaL0LjfFsn3n5/3O9Sx" +
                "4ott4lD1DfhlDrnTuHgvUeZuva72Hv0kP7cTTuvuHu6vF+nNoz" +
                "+yuzv3uB/biNwMVOR6z9GgT61UnVSWLVVrku9Uf0p98BzXCwhN" +
                "/S0mANtEScvk3p5bsGywq2WgwD5DolaykJ8JX3p7U2Gt9/3p5W" +
                "nWbtc1RbpsYazYHcSJp670IpjaWwDac/tQxHa4f9ghwt46PxEf" +
                "QN2NixfHCHNy1Ek/ry3NI9KPrWApl3D/Lp7CnRlzd3D1YvZC92" +
                "D3YPspfSBfLcItbCxVqSib0ok/f4g/j7O81TNrJzS/Pn2cl7UH" +
                "7UGHHOLZqTLkgXyHOLsDJO0kRpnVu0ZeFXnFtkS9S2iKRuXhV9" +
                "n5Rgh/QbyJQ8yMdKnvx9geCLc4vA+qzU1Djy/vQgUOS5xRpzEp" +
                "VzbkE7r3xfsHgUok5Rq8MqvnVk387zbc1fjPy7ff5c9O6Kus9c" +
                "XfD2cHUpHGJ/bs7BKE6d/j0dP4Lv6fz9UyROt4z0nk7Q3o/d05" +
                "Fxuo+6p6PkWHC/4t/TKY1+SNl59ibgJeqsl1yn7sf7w3FK+oOI" +
                "+iNo+2Pc7H4cyWCcBhvlDec0ipjnOCrUny7wePNqBW6cqXvf2q" +
                "nNpwv2PKI/1U6OadSIG+XkzwXHnZDj68uMPbaud6wTpwtz7D/L" +
                "+s84GScYd8Rb/jgYp2cjWj+BcUdG4sNy3PFF7rhTuj8i4/QXGH" +
                "f8h8jT9125MvfjFqbJee3MjmP21KY0d3T8j57aGUSc6gX7U72s" +
                "t6QFKetPF2Ee+v6uFbdSliN5wA1+f4dkMM6kRc7jLTenUWCrMV" +
                "TZ/uaATvb9U7LB8Gw5oBRf78Reamf0W48Dfi0kAXg1jdLwsQf6" +
                "4sfDMvhNib/dL/P+9CXP+xy7bcepNsvm263m/oL9ek6JMTAnpJ" +
                "GsKWhhTWFf+0PtxHuzZHZc3+bbrdaM9r5DPoJYZxdBVebtS8yK" +
                "n8r7k/evBpJL2ni9JNzqKTiPt/Phy1IaRb3VPl16Hm9CyuJ0Be" +
                "apMsUt6hYC8+wZOawlecDVPtrIZDjVTVOT0tCyOKdRYKsxj/p3" +
                "Pdb8lP8lauea7xFyzYuJ+Wx3iG+3enYRutsJdBfT3yMo+Z2ULK" +
                "VBeaN+P177TOF+NGzfeiXDmNP+93RJwV/zt7po327fC92mqb4z" +
                "bN/ShX9P52MPvX2Z39NZEc7/R4Ha+UVu/Io+rYL/90V5H2ScVh" +
                "XsJavKektvSG9wzi1fAx4uTYu2EvweYW9730XwGS+AidJtvE7j" +
                "dWVrlbBn2nK6MF3o7J++LvwNaZ6RwS3SdpDX80pMK43uWBtv2D" +
                "KyBpgoXe3NxVtb4crWvhD27Fj+Lw7N+fo=");
            
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
            final int compressedBytes = 3045;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W2uMHEcRXiVxCEG2cSzH+PzDxgoSRkqIBCgh8GPnZif3B0" +
                "EsfvAIDymBMwH7fLItCETA7txxs3drEwmJh+X4nJf5hYTEn8gI" +
                "FGwkbGGkPKzYh4wiB/wDuMPgxPgMOWJ6pqa6qrure2cvu9lWP6" +
                "rqq6qve+fZe1ermZ/Gx3CUfbbWx0/nvcv3JU5VrVPnqkWe+lOv" +
                "XJIHkwfzvvWoXqevkI33KNmf9ON+W602vLp77pAnx+Qj4iT5Du" +
                "+R+drY9D5/ZjlysicpYqfbdK5Ztd7/BBtiuORwm/XbwOpltMfv" +
                "J2HyEXGSfCGby9fGpp/0Z5YjR9NUy+Ppq9xW9A0uCZGXuC1Zsu" +
                "P7PtE0WSFHF0zJM8+g9A15NmYrs+BRQxmZdj/Vcp2+wW1FP5J+" +
                "iiOcGCPcFo1kD5vxvX77yRqNCGf0py0Mch0p9CPybMxWZsGjhl" +
                "ixb39XsqvgdL9ep0fIxnuUxCPKa+vcEfIKxXQx+Yg4Sb7pZ2S+" +
                "Njb9oj9zmFX6Jb1O+93zypQnxnq438UV7ydLAqcvhzyaQ4LHA9" +
                "WypaPLvQe3Nul1+r44j/OBOZ73SemOTmP5zwXpeCifxCi6rlrk" +
                "qrjgOh0QGK+uEOGU4Lem88CbWKe9QetKV1e/Vi1yjmv9riqP6E" +
                "aoxSwX9To9pp4Lbkc94bimh/NuVzcG9og9Db5m6pEv6CSP4b2S" +
                "JX3aWeNH/LMJz5SeM9Pv9PV53Dl30on+RBavT9+uDeATrYBaZJ" +
                "jSx9NhtJk4rulhnb7ejYE9YrOelnigTvIY3u2zWJE7fow802gf" +
                "1XKdnuC28mib55IcpVdLbgtZVdYFE8N5Rvuk4wmsvJVZhDLLtm" +
                "Q8GTev4+kPyMb7ZDxwx/PaOs+HvEIxXUw+Ik6Sb+Tha2OjAGM5" +
                "crI72W1dx58mG+9Rkp6fyOasU+ANHnMH14lh8hFxknyHxyW+Lr" +
                "/0h/7MYVbpj/XoJ/27AqYHOn+tDeiT/ki4Q16uDfjD9lV+2s+4" +
                "U1ffxEoc7PV+N3X/oNYneajk9DO9Tr+ybZLE9K/5bWD15/b7SZ" +
                "h8hJLsC9nAwu02Nv15gLEROdoR7aAWRiBlr+QtR3C7KYGvGRNH" +
                "MJ665vqZHqQzc4Q4yDNQ2d6wsxFDO7ObT5LjazF7xs+lXM5rdg" +
                "Es1CKSxognG2qd6/jfzPjkyX0og4vgcRGLsWwG2dtM7jy27yPZ" +
                "2SyXYvWGDi30UFSuf8VL2SW0oZYQrgQRKBp51WqNUclP8zGi8K" +
                "ikCXGgFqqbDfQUizJzNm5+7lE/klcs5RviEdSCHbW2xN4pj3A7" +
                "R/K4pp+d1Y1qMuEySvl13MyJWLu1M0kaO6/33nK3Hv2in/eJbN" +
                "Wg7kDS/S5b2e8s8Zn4DLbQQ1HX+9n4TDLLEdTLEkSgaOSlmK+V" +
                "/DgL0vCoyCTMgVqo2S0Sgs+YMnM2bn4tn43PqlHRQg9FsXsyPp" +
                "s8iTbUEsKVIAJFIy91xdgm+Wm2RhQelTQhDtRCdbOBnmJRZs7G" +
                "za/luXhOjYoWeijqO3mddGjhCFcCNEUjLxVtveSn2RpReNRqHK" +
                "iFmt0qISgXWyeDjZsf5WTJ3JEGOa/ttWChFpE4nhjL0SDxKHbM" +
                "4njaiRbENyM5L+Fkbjab5hDnidlM7hzfy848+Td/2zxejEdx37" +
                "f5nMqtzsv2u5rFUdc8l9uaf1G22wHZfFXJxRtU87+tWusGZbmj" +
                "dRNFaa1srXWurMXVtnm++Urzz6q/0GTve8loMtq6vrVCeb6jZL" +
                "GqdWtrffM3zWMc03y+ebpkMKfqy1r/9yI2ezPKNrRuzm2tdQX6" +
                "RPOUsr9Is+S59f3gkm1R/sX+eWt1a038UvySOrKKFnooap02kA" +
                "4tHOFKgKZo5KW+4THJTx/9RhQetRoHaqG62UBPsSgzZ+Pm1/IL" +
                "8QtqVLTQQ1HrtIl0aOEIVwI0RSMv9Q1vlPw0WyMKj1qNA7VQs5" +
                "USgnKxdTLYuPm1fDpWxzK00ENR67SFdGjhCFcCNEUjr1pt+FnJ" +
                "T7M1ovCo8enhZ7tzoBaqmw30NB/KzNm4+bV8LjZ20EDOa/tDYK" +
                "EWkTieGMvRIJm22NmVGz6G0RHhy0s4KMPHTIzLhvPEbCZ3jjeu" +
                "mps4A+Hp8pyZV121tjvP47+0bVxKj4r7mdu9+5nzgTvNdr+fms" +
                "u7bUw+Qkn2zTYTG253sf7MYVZsnX7dz+f+4eOD8pTeW5afzfve" +
                "cig+hC30WNr3kE5bZjjCxMczJXoGreSlRjOyH7FwvCwN6k27OQ" +
                "NdZyQEn3E5nuFzQoyUPT4QH1CjooUeS3uUdGgxEbYEaMLyUfY+" +
                "2Q+9uMyj5pr29m4cqIWabZUQlIsdJyweYuxZmR6l31PYt7/m2i" +
                "a8vy0oG/L4rmiXtU91Pd6XhZCzybhq2PhgfBBb6LG0d5JOW9oc" +
                "YeLjdoluo5W81Kgt+xELx8vSoN60mzPQtS0h+IzLcZvPCTFydv" +
                "ktJ1lqf8u1xan/7QhtMsbvOYj9p6rZclw1bPx4/Di20GNp7yOd" +
                "tkxyhImPJ0v0JFrRPvGoGk3KfsTC9rI1qDft5gx0nZQQfMbleJ" +
                "LPCTFyduv3u/LJq/XRtnOstT4c+N3vJv0r8TfF7+Nzb+nxVDFb" +
                "9n7F9+FKx9Ph+DC20GNpP0Y6bck4wsTHWYnO0EpeapTJfsTC8b" +
                "I0qDft5gx0zSQEn3E5zvicECNnr9XqV6Baz5nH0KZ31q/EU/Ur" +
                "XGOs+JTZmxZXa+zZX3FH7Nn3uM2Dc5Y8IJuPKcflc/JxCvmzdf" +
                "q9Oi4/YMUN/BU42lxMrgl5dj07PtjzeSdmc/+erjqzeDaexRZ6" +
                "LO1DpEOLiVDvwUzCeITlo+QCR7peXOZRu3EwZ4DVzQZ6ikXzp3" +
                "iIkbNba67/CjX9Q19/l7r7Lf1d6q6+//1Fuefc2OLsjz+RLGX3" +
                "mPvjyaK5O0172GhTo0VnX3sRqr0/jjm77Y9nH/Htj+cRpP3xnI" +
                "O7P+6ed8RM2h9vbCH/xp2NO5WmaKGHotbpJOnQwhGuBGiKRl5q" +
                "rvdJfpqREYVHrcaBWqjZJyQE5WJrYbBx87se5dmm90TTF9lfdY" +
                "xTS59e/h+h8c5BnXfR+PKzZdsq87+tcRv0XM5r+xS3oNYc52jT" +
                "n8c08qzieOhdH87CRvC4iMVYDoNVJnce278S/rVRZXNjsxoVLf" +
                "RQ1Do9Rzq0cIQrAZqikZf65j4v+WlGRhQetRoHaqFmWyUE5WJr" +
                "YbBx86McPxM/o+58RQs9FLVO10iHFo5wJUATlo+yL0h++u5sRO" +
                "FRq3GgFmp2l4SgXOy5wGDj5nc98s/0dfFRPb7eedY66r8+2TZb" +
                "bmzsfg2YvmFZO7JHhXNoo9/Wnw97Hv9jz1fUe/1SY2gAV/B73T" +
                "yDyhYvxAvYQg9FfbtvJx1aOMKVAE3RyEsx3yD5cRak4VGrcaAW" +
                "qpsN9BSLMnM2bn7uEd0C1TqeXkab/v4KHNf08Dwe/IteiumL7v" +
                "JAneSRbQ3F8sW1LdxWX6gvUAsjkKKH6gsmgtvzMjFG6PqCHRNH" +
                "MO4wP67nONTVF0zZz0GeAXA3sxFDO7ObT5Qv1i+qUdnCCKRsLG" +
                "/Jxnsoap0ukp7lvogSWtU6/YP8uJ7jUMdz8IguB9OGM1Dcd9rZ" +
                "iKGd2c0nyvP1eTUqWxiBFL2nXvyGSwhuNyXw1bnnUUKrWqdF18" +
                "/0IJ2ZI8TBtOEMgLuZjRjamd18kqzfCE8mJ4ur0qv6Lr2CbLxH" +
                "SXyr9to6V0Ne4Dd9Y3cMZiFOUs70kszXxqb/9jOWIycnkhOFp/" +
                "5/8/Qq2XiPkhjba+v8J+QViuli8hFxknyjIZmvjY2G/JnNyPHl" +
                "+DK20ENR3+7NpEMLR7gSoCkaeal1WpL8OAvS8KjVOFALNZuUEH" +
                "zG5X3xeyYbN7/tkcwlc9Zzwetk430y598vSOa8x9P/AsfKnN9P" +
                "wuQj4iT5Ruslvi6/aL0/sxQ52ZvszVuq6nhaLWMIBeuUBP5rPk" +
                "eDvfNGGMcjuUiMgpmJBbWcfbSWa3yxcpwZj2IRc2lXk+9VTq+R" +
                "MLTvyD1Cu6XVcOZv0d348d1P2Pc1eUXr/H/ly3dbo3VmPL5La+" +
                "3K/h9I3yET");
            
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
            final int compressedBytes = 2921;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW3uIXFcZn0BtKRg1IcH4KLWC4D+KILVqfcydmVtl2cS8+k" +
                "r1j4K0CDEShGpR48wczXZV2jUaTVBI3Y0v2ia1pbFLqPhHHybq" +
                "H0Vt3bWufzUibaISbFqIree73/3me5xz7rx2g72H79zvdb7f79" +
                "w5986Zmd1aDY72o+3HCqXWfcVbT4I2vbamjvazDvrn22d8f9bL" +
                "C+jvXFRLHu3ftE+2f1fqf6vIe7modElZ8bVB/ET796X2Ry9/av" +
                "+5/Yw//7XwvLnoT7df9CMv9nJprZZt8Oe1nTd4/+NBrYX2IunZ" +
                "hvYp7/lH+7n2v/zcX27/p8g4136pfb79386qkGn+dP40nF0vlt" +
                "+NAjHKkVbsqIpVjaoeVzAROaAxp9jYqW/G+Ya5aeQUq87RIvpI" +
                "55dl3hdq/yfH3kerop3HInNcQe54ndwlvet0+6v4Oq049/zJ3t" +
                "o9UHtVHPh8Mvfd95f9utyc38w9aqU1589zMkPGtYVjdU3SUG8c" +
                "DsfpEezTGJql5hCfQQyNGVrkEC9ml9nn8/PQs/j3u41+xb9LVC" +
                "xzdBafo6/Cecz3zOeq82Qlm7n3BapCyMyC1pPm1ZiTHjq6hzlT" +
                "IjNL6qkFa7fYF7jVcl+QH0IR+4LXjb4vaB0cfV+QHxp2X9A6ON" +
                "i+wFtiX+C15L6gta61jno8Y/Pr6Rb2UURmhBZmczUe5bX9sXGS" +
                "hchdZ+v248B9KftjGXLGjCzZhPg9+7LWZfJqow0y/VmMcE+ZrE" +
                "O2zJI19NE6IPN5XIjLefGckE2EwQHNXeanjlhcju/82suvYF/Q" +
                "ecLfe2tpXzA903cncSzw+Luj89tI5vHW98bYsTww7L4gjtZ5OO" +
                "p9fCROl+PZ3TEdYGW3Vo/VcW21Bnyn7ocR5sZGtJZ9X8CHe6u8" +
                "Tv494x1hTvf1fWqsj60n95bumrGYvbEq2l0d+mLckyujcj25De" +
                "5NcG6ebp6mnmy0Gu9EH/WcyTl0piyuIavpynpcGLMYmqXmYGdA" +
                "Atw1GjO0yHLOcXw9onx1Pt3b0x4J1vmke1vlfTCZtga+lyaHzY" +
                "2NcG9fsc8rJ/ITxYqd7a3dWbIwQmeygrU+m47JulqX2FVjZQ5o" +
                "zCk2VvLmeGM2zE0j68qNKxpXcI8aWq0G9DJDxrWFY3VN0lCfOh" +
                "qO0yPYpzGqOMRn4NFWWzRmaJFDvJjdu3on85PFiu09Aad/zjF5" +
                "Jiv6GiRjU7+oGlVVM8wBjTnFxk7dH+cb5qaRdeV8T76He9TQmr" +
                "4Hepkh49rCsbomaahPPRCO0yPYpzGqOMRnAOvJojFDixzixWz3" +
                "Ab+GPlSspI+p/dOWqQfFc3Fr9b7AbS/6T7n3uffjvsBd7T7Si3" +
                "40tS9wk26bu87dKDw3uU+qjKtoX+Capaclote4j8O+wE0U1mYv" +
                "13q5wb1b5Hyw6D9c9HWXld6Gy2P7ArfRbVL29W6Heu/Yx1Led/" +
                "dyLNtdnHdTZvIdaN/wkaL+vr7vbSJH8iRuMTTZx1lUIcdj2QxL" +
                "eZ3ukzGdWV7rXbEqCdSKT0HZTNb3M5LMkTyzmdj3dBiVfZxFFX" +
                "IYa7wEQo18oLlbMFaOVJbU0JJxsDjCdS2yRiUMneNuJT7MDAQQ" +
                "EIcweS7Mj/vuYcsjzUwi9vakV4JQIx9qGCvnsFFaUkNLxsHiCN" +
                "e1yBqVMCw75kMaCCAgjuYqudveck4xC3NrtfoiCDW/ij6DNsfK" +
                "OWySltTQknGwOELV7CFRJYZlx3xIAwEExNFcJXfbW84pZmFuef" +
                "UmWdhDvuxzOtNq/WPN5OeR5iS09JOJeFAOamxJbjJDziDNqpqX" +
                "+rysvn/qXN77/unIMv7mdXys0UN//zT10ODfPw3MInWd7l/O7+" +
                "lG55f9e4TrdGylvqdrbmZhj39W7WRftpX9WqO4jGVbbf0UMjSu" +
                "EWOGWZyPXsi1ODJX9nEWEjlVRbwDngGhRj7UMKYzpWWrkFfGZF" +
                "2LrFHDqlxPM2ML9k+aq+Rue8s5xSzMrT9Fwn1x3z3S93vGXYOu" +
                "1vpTjYtHv/OI0eDROFqY2XjNUDz+AkLNz3832hwr74mbpCU1tG" +
                "QcLI5QtRCZUSWGZcd8SAMBBMTRXCV321vOKWZhbuo53ty/nM/x" +
                "xkVjfD/++WGf43G0cZ7j+VK+RD2esfn77jj7KCIz8iW3S1pUj6" +
                "vxKFtbj9ExjZEvZWerOOgZkGRnYxlyxows2YT4ckT9GRBq5aor" +
                "NYyV98SN0pIaWjIOFke4rlnbBpUwdI7kQxoIICCO5iq5295yTj" +
                "ELc/0qfR6EGvlQw1g5h83SkhpaMg4WR7iuRdaohKFzis/BhhkI" +
                "ICCO5iq5295yTjELc73nFRBq5EMNY+UcPiEtqaEl42BxhOtaZI" +
                "1KGJYd8yENBBAQR3OV3G1vOaeYhbn+iX0NCDXyoYaxcg7byMPj" +
                "1D5zG3jc7RjDbF0t2MsZVKph2ckKqIEAAuJorpK77XV1sN0XE/" +
                "vMgHP97yDU/Ey/hDbHyjlsl5bU0JJxsDhC1YJngECVGJYd8yEN" +
                "BBAQR3OV3G1vOaeYhbn+unzFX70J1/Ey1ZwofXegBj35sh3kKa" +
                "/4hFlPO2QsE98ru7bNLf1fd19rTkDjGu7L5nUteVAOaiCAgDhu" +
                "uqj2DS9f5VzZuz2ou726dpxXLJb8HPyHZd0/rbqg+6dVK/E5uP" +
                "EcCDXyoYax8rW+QVpSQ0vGweII17XIGpUwLDvmQxoIICCO5iq5" +
                "295yTjELc8tVtpWFPVrLtsT9vTlukTHMTuXKCDSuEWOGWeXq+h" +
                "bxhFyLI3NlH2chkVNVhOcqEGrkQw1j5Ryuk5bU0JJxsHr3zV1c" +
                "1yJrVMKw7JgPaSCAgDiaq+Rue8s5xSzM9Z6rQaiRDzWMlXO4Vl" +
                "pSQ0vGwZIxnSv9EpUwLDtZATUQQEAczVVyt73lnGIW41xfAKHm" +
                "X/8ZtDlWzuF6aUkNLRkHiyNULXjvFagSw7JjPqSBAALiaK6Su+" +
                "0t5xSzSO7DJNwXz7G+f4lUH/j75vpY30xXj45F49zHY+HHnwKh" +
                "5tfTt9HmmM6Ulq1CXhmjujFkRo1X5XrkR40t3dNcYjFZpR+zMD" +
                "e1f2psGH7/VHiX+XeEUX5viXMfe/8Ef9F4mhr5UMOYzpSWrUJe" +
                "GZN1LbJGDatyPc2MLd3TXGIxWaUfszC3fLpvZ2GP1ar81bEwt7" +
                "dr+G5zezpKI2WO5Bkb6b5DY2xGyCqNbGP1eRLui9/A+j71KHOA" +
                "J+D8WM/P+WGj7j3LzyL1fMq7oz2fopkX+PkU5z7e78H1J0i4L9" +
                "bT/CAjB/Ny1VEZDhd17x2Pb+JOrINQIx9qGOvhH5AWa0XsoMzW" +
                "Ma5rkTVqOJLraWZs6Z7mEouFPCx2f87NTSzssT72a61/LMzlCL" +
                "SKV3ETZXE+14uNpFzZp1hV8zLvgP8EoUY+0NwPMKYzpWWrkFfG" +
                "ZF2LrFHDqmC7HxIfZsaW7mkusVj3sOWRZiYRe3fMj4u++Htx/j" +
                "+gafx78h8V/c/KzAfVOPO7ubvbHaJ9pjvi7un5f9Jdk0f/4tfN" +
                "ebnX+IKn9qD/B+R+Wp6PSjQ32+ddovr/gO5z6u928tvy2+Dc6v" +
                "2nK2sYoTNZsSMVax2q4kLYqQNGyxzQmFNsLOKFfMPcNHK8cr6Q" +
                "L+j15OY5Js/5QsWME7HuzsrrtFBV0zM5rnNAY06xse5YnG+Ym0" +
                "aOV84X8+KvEVu9X5NYyxflOV+smPGiHKde4W1pD2FXXkuRAxpz" +
                "io3F2iHfMDeNHFaun0Mp1tNdvefTEsV0nvQMfrRerNwDnQu1VA" +
                "bzRV9sBKINwjSdE840W4NinuPPUkznZSP8b2Z3Z35pVZxrpqqH" +
                "PMgXG4FogzBN58Rnmt3JUl6nUzKmM5O17xw+ArGqaJgjecZHol" +
                "f28ewqZBtL/n53ZvjPd+nf7/L1F/Tz3fpl//7pf/vOoRA=");
            
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
            final int compressedBytes = 2348;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNW2mMFEUUHg9Yw7m7KCisWTUxJl7/UPi3M9sdsioqMaBogt" +
                "EIiuKKKyqe2ZkuGdhJJMgGfmhUFFEDitEoVwSMHEGCisY/QmJM" +
                "1EhixIAIsrJa26+r36uuo3tmejp2p6qr3vW9el1Hd02Pd7C4M5" +
                "crbudpY3Evz9uLm3L+sexYLuYofqJQDvC0X9SWHQ/p29zWXM1H" +
                "8UMrd7dK06MVN2upe6r3qG+YMz0sD8+leLijE6A36enoU1JuEr" +
                "Qa27Hf9ftBsR3q3tK+kcijV3e/zYrhnn/vnIjH7htl0B5AGYGC" +
                "PukwAU31V5U1t0ZvOb8cU3B3R1OeLGk6zDy7lo2rylA/9ZpApb" +
                "le2oas5/UGo9z7SVDKO/T6vb/X0l+9n23c3sE4/d4DpPwdT4dt" +
                "0uXtfj9sjver9xcj50zxftJDpznTRA5XOHl/akWa4FAJtQbSaA" +
                "21eOmMTo96QWSnRe3G+YB5kM7oJGiLEZl6o+KLujvo8nsJOVzh" +
                "5OU1/LpG8ELqoCQj1cACWkMtjvWXTi+cCyQr1GroidUHzCGpaE" +
                "AX1sksJHmj4qsawZzZHs5PrtoXS2MTrOLK7OedLLU4p2tfY7wJ" +
                "Nm5Js7ZVg2Z/LvCOe9oViMTppvRW01KLe26WcaoHLWF0MU53pN" +
                "mf3Fymccql3Z+cWc4skcMVTh6n2UgTHCqh1kAaraEW9/wsnV44" +
                "TiQr1KrvyZ1xPmAOSUUDOrYHkak3Kj5qmN5b+h6u972F0Le5Y+" +
                "vo49W/t4xt1HtLfgWmINK3Up4sGfTJbp0Vs30zz8ZVZaifek2g" +
                "0lwvbUPW89xD7iF/tE8RFDYCefQqatpnfSPPHWXTstlUZYZK6J" +
                "NOF9BUf1VZi8dWr8g83h03j3vdyebxofXOGax93LGRVT8XDKY1" +
                "j4c+XMKT33p2WSROC4nUBPt6xy4KrqMxTqwl5LbyOP1j8eFiUm" +
                "6PW+/YeKl2KcaJjQuukygaG+PnkXcYdgG7MEmc2ETWJsodf0OK" +
                "xGmx4MlySNH3J0MvHmbjok2Kp5dAf4Gm0wA0ky2T3Sgnyus4BW" +
                "noKPQLKpQEHeUohR6oqY3TCKu3p9SSSQL9BZpOA9BMtkx2o5wo" +
                "L98Mye9P4V4Cu0LwZLm84S28dKU1TuOtq1mzWjJJoL9A02kAWj" +
                "7BfoFZRm1pvgmSHKfCCsGT5fKGXceCdW13rGMAbZqsq34Imk4D" +
                "0Ey2THajnCiv4zQkf865PJyflgieLIcUeX5iV1n7k/XdA212nI" +
                "6TQH+BptMAtI4Eb8NmGdrSIHLXQfJ7xeqwf6wWPFmOUqT+tLr2" +
                "lR9tmqyrfgiaToNdbbNlshvlRHkdA5D81uK4Wy54shylSHGy7t" +
                "2651vv6oBaMkmgv0DTaQCayZbJbpQT5eUnQ/Jbuyps9yrBk+Uo" +
                "RYrTqjr602S1RHrHtTo/BE2rcY2JY0ZWOZRXaCu0YQ4lqPUtK7" +
                "TJEpQv10BXtilKUHb+VfVkDaTJGDYf9C3QoaGHUWQVT1d3Op1O" +
                "btfP4Qonj9OLSBMcKuF0et20Fqw0xBpq8ZFwDpWUdWRcGSPOB8" +
                "oT6Do0oKMtRKbeqPiqRtBjrw9LU9Lb/WNT3bMbtbPINDNxI9CM" +
                "+0+xv6lVtf/UVIeH1e8/NaW9/8Rmsi42nYW/1LBbRKm8q8r7eq" +
                "OFN4PdVkePucHKvVmllT9PaLnm3wBKG8LnzJfS7LXOQC7Do/Fo" +
                "7K4wTivTtOuOyzJOjUPLr8QUxOlVypMlbVbw6HstqVZ+Zbx/KE" +
                "P91GsCleZ6aRuy3Suy//RRqiPhzzr6+H1W7r3pohlQ5hV3soe0" +
                "690+jfSDNa93YzJd78YkW+/Y/Nq+E+PzOM5Pv0ZsPjWU9/4W1h" +
                "9NcBceCT0fzp6MjUewmrOnFTsLY5GeD0uLh9D49Vm/tkCRXMSe" +
                "iFCe4e36wy8Fv8WxHva4iuG95efvRsbdEZ+6xs/fCaJo/RXOe9" +
                "17TfQn731vfUhfV2pxTmo13uRpQ4Sm9J6kvwd7bwfXjRTNeyPm" +
                "7th/D37P+0CLGs4FfcfSHN/uyEzXu4aj4X5mJVa2qt8RmjONU8" +
                "PRcNxVhqcap/MyjVPD0Qrz1FKaVrPRTtP3uHHnduX+J0dpYdX9" +
                "qSu7ccc2pNjSBdnGia1vyFhbN5TEKWiCCnwqSWtRK4JKJaldWS" +
                "+KqlqVPaF1Ki1jCtloXlorI6meqbgJ5nHN2lrbdxhD39PVcx+r" +
                "/56uvK+KEVTT95ksfKurjEpzvXvhhyxntPIXDd9X+TiMU6r/EX" +
                "HXZfpc0AA0075vJfZLwWreg8tfZfkeXP4y2Xtw/LgTR+eJzhMi" +
                "F3WoVZqBJnKURBlaitqkWtzzgyhP6TIqxaIyspYqjTmk8tdRL9" +
                "HDKLLaiii+uBaOirxwVDw/FY5WxvMZ6GXghWtAghpSKxMEZegk" +
                "d/sbf/71VI5q1Z8HX7Hz5RaAVZ1Eaa3OZ509zY7WVGeqyOEKJ2" +
                "/Nt0gTHCqh1kAaraFW1LasI/Nkq8l8wBzSkladBG0xIlNvVHxV" +
                "I/o8zrY1+Cnba6Dt5xr+jrAkHDWT0rTr7MpleKSP5sx15oocrn" +
                "DyOM1GmuBQCWeu101rwh5aQy1e2kIlZR0ZV8aI80FuQZi26CRo" +
                "ixGZeqPiqxry87i31Mmn+ZzpbM+0P6WO5sx0ZoocrnB6SyszkC" +
                "Y4VILLdNOasIfWUIuX9lJJWYdalWuUovdBbkGY9uokaIsRmXqj" +
                "4kc18v2YgvlpFuXhgTXN/zaM3/zmLV8D5/vz/XH3k8pQP/WaQK" +
                "W5XtqGLPOcLqdL5HCFk8fpdqQJDpVwunh/6qIaII3WUIuXdlBJ" +
                "WUfGlTHifJBbEKYdOgnaYkSm3qj4YX2OM4eX/ByucPJxR2iCQy" +
                "W4TDetBbjEGmrx0mdUUtahVuWaE+sD5Ql0HRrQ0RYiU29UfFUj" +
                "eIMM//HADqc6Dzbsaax3YpZo6npXuTvV9W5PputdA9CM+wX3aH" +
                "Zdfqx1v8D5NMv9Aj1aPfsFTo/TI3K4wsnj9ADSBIdKqDWQRmuo" +
                "xUubdHrUCyLbE7Ub5wPmQdqkk6AtRmTqjYqvaij7vvPr7akV8t" +
                "WGszXTcbc1q3HH56fY7yyqGne7E8S1J7VxtzvZuGNHkn+vQuNU" +
                "eYzMT4tSjdPmTOenzWnPT7n/ABnDGwQ=");
            
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
            final int compressedBytes = 2241;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW+uLHEUQX3wmiho1GvCBaLxPkdMYQTkxuduZQXyAf4AfIy" +
                "gI90EICL5mbrPu6qrBB6goiBK/RFDIBxWUEA0aJWiCQWN8YfSI" +
                "IsaNj3ga46Nnamqqqrtndna3ZzDbTE931a/qV10z3TPbtxfsC/" +
                "Y1GkFSwxlKo9FbRzLUcITZAzR5I6tGw99qs2s0OM6MgkuKYqAa" +
                "DpMN5OSLmHk0Jr9pAZ/oAji3ur1Ow+HHf7tR46cKtmirLU+K66" +
                "UR/e2wSdcfHj3C9T8Xan+35Mkae/S6YXtISd8tn6doizpeibbH" +
                "eYpeA3mvN9DyVUPygT1P0Rv+m2Ncyc2F2ncseXqzTJ7aIC2VJ3" +
                "+RvwhrOENRedpAMtRwhL+oNct76I+8kZVqbeNIaSN5JcegGOQI" +
                "smObDcFHTMw8GpPftEjtTjFbR9+ns7ea9ck27zp7XM67sSIcet" +
                "7df0a59al9Zvn1yf68Uz6WOr3CX9R6P31YNcPcVVmelrn029xV" +
                "Z57cs3l7vb1YwxmKWp82kQw1HGH2AE3eyEr3LW2kTnotFwPVcM" +
                "Sxmwg+YmLm0Zj8WX/emxd5S/qqXqrK/PoX4jbIscXbMTrFz+s+" +
                "tOuxVMfbealHmM7nul8dizJi4zL0Pbex8I6Zz5PZxsPm3bnq/t" +
                "3jbCbsqdPWbjFODN5+bz/WcIaivD5AMtRwhNkDNHkjK923tJE6" +
                "6bVcDFTDEcduIviIiZlHY/KTRe77+BMu3ws6v9b5XtD5pdx7Qf" +
                "n3ce9573ms4Yyl9zTJUCMRrVneQ3+E5a1gE0eaVrzPvQ6KQY4A" +
                "D5MN5OSLxk/+EGNn1/L2Mp57j+bp8NOazdfpn2BjVe8ANuYq2H" +
                "Ln3bMu513wcp3zzs42zrxLngN9rJt9fN41++0L1R3zDOgksrgn" +
                "pSCJC1s9DuVp7PatZ4v1cgTg1YaI3wvsmrHex5e7vGuD3aPbti" +
                "+uk23Y73e9xwZh+fo0MPKP6/zeUgVb3n5m78nR8mTfzww+qXM/" +
                "0842zn6mt9nbjDWcsfSeIxlqJELvAZqwvNX5w25HUehWuqQoBq" +
                "rh6CzYEHzENH7yhxgbuz/pTya7cpPp7lzSj4/gca5BqWzHaGlP" +
                "Pqy7p5Ponyx1XsLpfqiPWPSlR6DHzn3n7uxO2qLNG8/cLdlT6c" +
                "+cVXWV+7nfvnwgYgVrq6jbVxSuCJ8luEtLMF+Sq7lM5GzCn4Az" +
                "78dH80quQalsx2hpz31artkE+idLnZdwuh/qIxZ96RHosXPfuf" +
                "fThC1aPIfbwrflOh7ubDRCtQ52DofJ9+sw2dUIv1XHd4AJ1ben" +
                "8LekdThqRMcldmynPTolOtMeS/h1uC/8Rp3nw++1NfbY6HhVn5" +
                "z2To3OjpaFW8O3hPWuMH3eh5+q46tM/kNS/8Hupy+jkxI/ZyWa" +
                "7aF6toQfFecptDwtomOS+rTodH+lv1JlLKnhDEW1d5IMNRzhr2" +
                "zN8l56DZg3sjJ8CxupkxyDYuA6ZFfX+B8bgrjYPSOiMfmz/uW+" +
                "WhughjMU1d5NMtRwhNkDNHkjK8O3sJE66bVcDFSnx24bgrhYnk" +
                "Q0Jr9poa/j/q6jdx3v/Ot6HQ+OBEeSrCzn/eCIt8RbAhqqocXb" +
                "MZqjpE/xnraE4+M6nPGX6zbEgMUWK0UQewjPMSPQY+e+c9/hDb" +
                "2/nNvb9wu867zrXO4XDPbmcr/AzjbufoGwuhZbM1ONo/bjPnZv" +
                "h7cDazhDUVxXkww1HGH2AE3Y7olSY9rxKEjCvZaLgWo44thNBB" +
                "8xMfNoTH7TwthXucbl9fB/rPN+qoItb7+ge9KI/na4jnD4/YLu" +
                "Yuf7BX2vjzWc0zKlSl8iEikh+gKvdCl6Cr2hPmlN2ex4FAzbly" +
                "wyBqmXI8iOKRuCj5gxT3Gkzm9aGPNuxulM+KnWeeecbfrA9AGq" +
                "oRUXb7W3Ou5zRCxFPZdCy1udXqHVIEFbaHNLLuc4lEkOPQYbWo" +
                "7AxgZywhOzPiaTX1qk91P2F8LexkbNn96Lzt7vmyX/GnBoZIYg" +
                "vSvWeGssq9magpVuTRGmyNL9pyxb+9pRI2tfn13dTf+Xt8b2DU" +
                "NblHz7b99Yen06OH2QamjFxVvhrYj7HBFLUc+l0PLSb6qAIe+g" +
                "4ZYol1GQTHLoMdjQcgQ2NpATnpj1MZn8Sas/rZ55WEMLer0t08" +
                "nTkBBcH5fWLKGn2ZMTJNiCtn+A7Lic41DGObhHMwapwxHY2ChC" +
                "ndnks/Xz91XaN7mcO91ldc7U6n6fGabfulvZL8eaK3OQI72btG" +
                "8pZP9nYHwfsPbH6ij8XSzEHi0pMe79uZq/o1vZN+tVM6uohlZc" +
                "Wl0vmFklEUr6FOq5FFqtp9P1KQAJ2kIbpNJORkEyyaHHYEPLEd" +
                "jYQE54YuZYO7+0MN7Hb3M6786tc951z3H+prHgLWAN57RMqLKQ" +
                "/I41QyRSQiwIvNKlHibQG1mp1oTNjkdBEuFVSbpncyuplyPIjg" +
                "kbAn7HynkpXrLX/XOLYF2wLq7pUM+77dq+aIqRKDpb3q/fi9Gg" +
                "by7Ox6GPfI/oBZkpCqp5XM3FXJLny/RHvijynDd5mne3u7xvmy" +
                "fXOe+qZwu2ZHP8vKN333f9CZXnKfurae99pyvr+XXm6f6bK/8u" +
                "dUeWp68GYYf5nZj/cK37T5Wz4frU6rbXOY18Q6152lBjnu50Gv" +
                "mDteapcrb2XVme7nYa+X215qlytmgD5qn3jdP1Kao1T5Wzsd+x" +
                "HhwtT7a/S82d7t87ekytwr2GOcv/MQ/DNt7/var7yfK/NHOnjZ" +
                "ynR2rN0yOV54nm3a9O591crfNurq55p/J0yF2eos/8h8aI6a+h" +
                "8/RQ5XnK3jx6vw+Xp+jxwshbtd5PztmaFzUvohpacVH30+G4zx" +
                "FcL3tgK31iC9r+A6adtCCZ5CiKwT4CGxtFqDObfLZ+wfo05N0+" +
                "4H7q1Ho/OWfz1/prsYYzFDXv/iUZajjCX9ua5T30R97ISrXu4U" +
                "hpI3klx6AY5Aiy4x4bgo+YmHk0Jn9m8R+02ivC");
            
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
            final int compressedBytes = 1331;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW81rFVcUf61dFYq4CN1IG5CGCoLFhS5sYd68vEW1UD+RLt" +
                "zpSiihaShkE18y8zLFQN25U6StgmASAlGhULrxA0RQoUgiEhqb" +
                "Rf+FVMGZnNycc+69M2/mzZ2bO/OYO/fM+frd3zv3zrzJpNHgW+" +
                "dTOE5FM41e29T3jdzb8KWGxa2KbJ2/4v3PeJ/rPEx46tyF8zPv" +
                "qbbBP8zzjhLrSbw/1uT4YzgsgXAhU3tfw5M2W+ee9uyDXMyPDI" +
                "+IFo7wievpfTwnNNRClcAao6FX3Ovq/CgKYjsix+2FAdvNvauz" +
                "oCPGzBSNml/1UObdDqMzIbI67yrPhjwZRn7ZKk+Xba1Pep7C63" +
                "2vT4HV9SnItz6F1/KuT9bq6aLVejKezT/iH8EWeiBFg0lLLaie" +
                "S+DLY4oe7/No1APP8RxZGPQjiL/lV3I2RChnVvPpZKWerlbzDT" +
                "ff2Kyn6DPj9TTgD2ALPZBAphZUzyVhS2OKHvSb66of96B5uZyO" +
                "QT+CRmN6Uc6GCOXMaj6dbK2e3lqtp6GqM4S/i177SqO2m3ns7Y" +
                "n2BLbQAyn6PGmpBdVzCXx5TNHjfR6NeuA5niMLg34E8e+r/XI2" +
                "RChnVvPpZGXe/bo1cz+pbz1Vj73zy9Yc31tfnoID9njyJ2pcTx" +
                "MWeTpeY56OW5x3+2s87zyL9bSvxvW0z2I9fVHjemrZ46nwndhQ" +
                "/1rjd4VDZu3Sn6v08R0ezYi6HB0q8d39X9Sjez0n5m+2oZ729K" +
                "81Xk97zNoZ5Wl3/1rjPO02a5d5vfuqxte7yrETng7XmKfD9niK" +
                "asxT8J3FejpT43o6Y7GevqxxPZ116T6zyHsYUdMmT5HnEk+FkL" +
                "es8uTb46n5qCA25W9cQco7KsFUwchfFx1FUeyleHplct7Z3Xpj" +
                "37555487dL0bd5cn7193eKoeSwmeZh3iadZhnlYd4mnVBk9e6v" +
                "tGXsabSN5CZb/+1wvzVAGWtPdYc3iy98T8n7LeEyuFsPB7YgkW" +
                "jWWJ91gNzrtbDs27WzbmXZ/Y5hziac5dnvwxh+6fxtzlKTrmDk" +
                "/Rt5XzlPn3lsmdGbV+ezOCZh2f3FUG09THmZg+SseSa8R9r+PR" +
                "ydTv6UQGTzccWp9u2Jh30elUnk5lYFtyiKclGzz1ie21Qzy9dp" +
                "inmw7xdNNhnuYd4mneXZ5c2ppds3ZZ9wXN+zXmyTj28LkkP9u6" +
                "tlX+t8LwaVWRg5+NP7NotVvYQg8kkKkF1XNJ2NKYogf95geqH/" +
                "egebmcjkE/Al02RChnVvPpZG/UG43Xvc0WeiCBjDp6lCVhu7mK" +
                "jgoJY0XnVT/uQfNyOR0D14kRxNnOydkQoZxZzaeTk2Prw2QXH/" +
                "CHXvg36ERUVcJN6OAs1dG4fJOzqlExHkeGEm/FWHQ6GqUXMtW2" +
                "yO+7Eldqq/8HVEU2Y88zLzj0PPOC1lJ5nhn8V+R3sL+e7OIjzk" +
                "EPdNySSnIUcZbqaFw5M8+qRsV4HBlKvBVj0ekmf5NxpCOjGbXX" +
                "6pfMds3Yk7O1qnzD5bweZUfTOpjs4iPOQQ903JJKchRxlupoXD" +
                "kzz6pGxXgcGUq8FWPR6WiUXshUW++R2LHNuVoyy+gHjKVaeiXe" +
                "jMj21Wn1zzP1cfIiM7WOb5xJXcejH22u49OD+dbxIs8zvRdix3" +
                "ZjNod5PMnsDzFWtmXhenpRVKvHro+TD1t7pb0iWjjCByR/kVrg" +
                "US+BD0ZDLzk291Hz8rj+Yi8M2MIOHrJFMhqKjuJFf3VUcEybd3" +
                "632LxL7J25f+oan3fvALFQYsI=");
            
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
            final int compressedBytes = 1499;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW71vHEUUPyqKEBBxgYQRaRBt/gAktHt7FpUrREVB+OoQTa" +
                "icIOXufPFeQxoXIKxIEBMJKRCJBAfZSXAINiIGJFojEBUf1UlE" +
                "IgVSxO2u597MztvZ3+zszp139d7dvM/fvp2Zm/GuWy31GPwit9" +
                "rXWjNwmFEM9lEPt6vpbo/p1piudL8b8+Pd65k87pV6bmiSH8e0" +
                "x1huuSDsnzKi2GE8fmUtv2Klu/D9+ighcQpZ9i3TqZZyKx9FSG" +
                "WdHDefWc2qR6V4KjJqqVxcC6eTo5Qh022DDUHEU7uwrL7BBnon" +
                "KGqVw+zLaXnsLhjG3tcFEU9zvYB4ojlwW9s8nJbH7oKheH4CPO" +
                "H7092KBw4Iv7Cdn+In8fmp2qH+3pXMr2/Pwq8h93sX9+vPE3wr" +
                "iHiaawXxxKQUtSpCO+3ythte1u6OIOJpnd5DPDEpRa1Upzu2Wn" +
                "5dgONl7W4KIp4c4TuIJyalqJXqdNNWy2PH8bJ2u4KIp/3pZ8QT" +
                "k1LUSnXatdUOjrjhZe22BRHHPTGpTVQ0j0kbvoFZJnYosuCWIO" +
                "K4Jya1iYrmMWnD1zHLxA5FVvf6id/fxed9rp9WjtW/v/NUp9XD" +
                "XqfgG0HEcU9MahMVzWPShq9hlokdiizYEkQc98SkNlHRPCZt/C" +
                "dmGf9VFl3y/loQcdwTk9pERfOYtOErmGVihyILbgsijntiUpuo" +
                "aB6TNnwTs0zsUGTBpiDiaYxnEE+TdPASyWTbwYuWddq01fLYy/" +
                "CWoLghiHia62nEE5NS1Er96YatlseO42XtdgQRT+e4+4gnJqWo" +
                "leq0Y6sdvOqGl7X7XhBx3JOPpcvxqEieMm38AI+DIqtznVn8vC" +
                "Wc97nOjEfTXY/33y+uU/+D7DM6y9WJkzZXJz6bXqfEDkUWXBVE" +
                "PDnai4gnmgO3tc3DaXnsLhi0GbChv4+3K//dt8yTfR4MZos/mb" +
                "U6RS+3Gjq4OqHZYusnVdEiEUnyMpKr38p10WJx5uQ0I8usyJ7i" +
                "cZ7CVuZFqMy4lFG7J4g4OG/scZJMGi6pcjyqtmZcMvtyWjW7yb" +
                "JIytjdFUQ8OTo/IJ58LF3OSeH7cddWy2Pn46DIPK2fnvK6frJ4" +
                "Hoy/rxKFCYlTyLJvmU5eP1ErCvPrJ7JWdRQ3n1nNqntSPBUZtV" +
                "QuroXT6TjyuU2Y/bxfsLDpsz/x2dzeLwiuCSKe5lpCPE0HRaCo" +
                "VRHaaXnsgdP7dMGXgoinuc4gnsY6nSHLMtvqeTgtj90FQzoSUx" +
                "KnkCXfBv9EB3rZUm61NF2k6eS4+cyRpo0YdC0NWZTTqggiVtfS" +
                "cBQjaxk06Rg+PlmDnKpzPe73KMdeYZYsmMcXTruuCyhCd2vwr9" +
                "d5/DQ2jy//bbMu4PtTnfu73ludY9Xr1Ld+puWSrSDihc4FwbPP" +
                "7MxaqgV98i3hI7fy31U/GYWcV40b/1GGgXhG4UnOQr7ig/F5Uk" +
                "Wj5897TPrT5D2A8MQhnp9O+Juf6t23DB/yOT8NW03sW6rOTxZR" +
                "9xccnkt1/7P1cMk23ToNH/ZZp5X7h7NO49+733zOT81na2Zd0N" +
                "3v/O6zP7lkm/K4O+J13B1tuk7LVxsaCatex93qdPtT90HVcRd+" +
                "1lR/Osv87dIl23THXfipz3Hnkq2OOvUeAyIw68ze4+EVh/3dE0" +
                "ZMzFxkk2221pnDoz770/CRxvvT+Wbihp973d81kq09Erw9EnXK" +
                "ZP0P2yPd0txqj/T4ySnd7UeLNLx/f82sV68gi8pZ9NZ5dDM1j1" +
                "/2Oo9fbnzcNbVvuecwj9v/ne5e3VfQWeusCZ59ZmfWUi3ok28J" +
                "H7k1iXWJ85NRyHnzccswED+gS5yFfMWUWUaj59c9Gu5PF732p4" +
                "uHc9yN98HrXvfB677qFM1H8+Pd3sd1xY0c3mMdPldPtp5T9Qqf" +
                "S71b6ln2XGoSwftzKRZ7nc+lzh38x0M0F82N7+TztfWnOZ/rTD" +
                "5br8bReO7ZZpC3f/JZp+az2exb/DyXat9uzmcYwiH/B+zkvv4=");
            
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
            final int compressedBytes = 1729;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW02IHEUU7quHIIrgRbJKDkGIzMGDIArZmu5J8KDiotkNKg" +
                "sR1tOCFy/Bw/zsjE1wsxdBEwkSPBg8SMySFYSQixEN4kkECS5L" +
                "9rJ4UzQHwdi9b96896re9HTPdNfOLnZT1fXqve99r95U91bXzJ" +
                "qe6QWB6dfQAglk0vGrLaEtHIgnFLWlN47gvFzuPTE8BhkfjiAI" +
                "YmOzUYQ2s82ny2g/u0XFPmQfSe3lbMt8mlSXpXVteJw6Enp5rV" +
                "tnMdu65s2k3EjKl83vknqmuRHkPJrXnZ4fk3JbsfwmDoOxj+ZX" +
                "mdpv3b7e46rl12rvrTwxRBeiC1jDFU6QzAa3oKsuAYa8ESqZux" +
                "sajkfBeaVfwGbFQDUUlw1Hw6Pj8RLeHZVE9LM7E1RymHOBx6N8" +
                "tu7zRe677nPj3nfdZ33ed/FLpd93d6I7vMYTdNLCtbERgOEStu" +
                "OGhuNRcF43kuwYqIYSR5oFHzExa+PW2K3sXsqcffVx50T0hc/7" +
                "rnq25h/5bd11wfBjZdNnnuIXS8/8WrRGNbRAAplbcL2U0Jb7xJ" +
                "ZsS28cwXmlPDwGfQRJ+cBmowg5c2rn8mnyJPNpeo8oLtdOeQKd" +
                "OAh5il/Nafda7pXGolmkGloggcwtuF5KaMt9Yku2pTeO4LxSHh" +
                "6DPoJknqzabBQhZ07tXD5Ndu67ewfivjtfrp2/97uJ1sIvFF1n" +
                "RufyrTNTu3zrzPBqeBVruMKZ3LuvUx9quIUrgTV5I5TtW2KkTn" +
                "rNFwPVUN5/QLPgIyZmHo3L7yLs97v4zRJXZb9OhP6n8HP8jarv" +
                "7O4rB+H51H05p91cXo/hyfAk1nCFM3nKP0x9qOEWrgTW5I1Qtm" +
                "+JkTrpNV8MVENJY3ct+IiJmUfj8qMc3Y/uJ8+z3RqucCZcj1Af" +
                "ariFK4E1eSOU7VtipE56zRcD1VDS2F0L4mJPchGNy49yI2gkLa" +
                "jh2mBSMNA1xFWXEMMlbIe/a7hA2LlRNHLGQDUUly0QMQWMORCW" +
                "gTIqiahyn671ULgzPrr9aKbvQ8rTZKfqJyDlyTxdZp4mQRfPU5" +
                "mxj8pTyfPprNf5dHZv89R6MIeH21qe4iWfeeodKjDiW2Plqf/G" +
                "U6/Va0HQuSy1ad+wA3WuTdqThRy5alwsitDZWp/lt92r+87v8y" +
                "leLnsE4Va4hTVc4awfqR8Jt+Iz3CLtJQvqRR14ABvyDhqOJByP" +
                "gnq417SnvcRRUi9HgMVlS2uYT5yX4iW87d9GTDqfoqPja0vfVz" +
                "larl3WvsrsT8p8fq/IvkpnhfZVVv4mq0674LOg8Pd3Wuzavkpn" +
                "J/e+yt3wLtZwhRMkaUFXTYpmEIN4QiWf3IyG41FwXskyOgaqob" +
                "hsOBoeHdjxaFz+gXwqPJW0dmu4wgkS9qGGW9hSdBgxiCdUEtFh" +
                "DTfIk8UrWUbHQDUUlw1Hw6MDOx6Nyz+QF8KFpLVbwxVOkLAPNd" +
                "zClqJjiEE8oZKIjmm4QZ4sXskyOgaqobhsOBoeHdjxaFz+gTwf" +
                "ziet3RqucIKEfajhFuF8e5lL/SiYN0IlbxJvcUuJcXltv8Nj4D" +
                "pk19hwNDw6Hi+30dmt1chj1fwFMmd8/r0rn83UTI1qaPWl06Ym" +
                "LbheSoCVPrHV93LaxUkEs61JeXgM+gg0NorQZnb5NHn4fDILpX" +
                "4eC17nk8oWvzvBejwKI6zhCmeyBvme+lDDLVwJrMkboWzfEiN1" +
                "0mu+GKiGksbuWvAREzOPxuV3EcX3C/Tfq7j7Be17vt/vVn4ra7" +
                "+g/Wf7r6z9guQz+UGZua2Mt/qOs9pd0S2LrseLH1rsSYTNKvaf" +
                "Zn8p0avn7+/KjL3sfZXjn09PnrJi2fM8XZmiPF3xl6f4/P697z" +
                "rx9M6n7DyF133maRK2Pc7Tutc8re/bPF3zmqdrFYzh5rT/Tqz4" +
                "fubxf/PtZ6Z2Y34vVdF8ij/yOZ/0/5faD/ed2fSZJ7O5t3ma4P" +
                "vgiz7f73pPlvV+5/2++8TrffdM5fNJrC67T5W2I/Tz+Njudjls" +
                "+vfm0/V8ij/9/zk+fe8t5p39mqf48kGdT6ZV8DNsTdF8annMU3" +
                "0f33f1svNi5swc1dACabe9zS24XkqAlT6x1fe17eIkQvBKeXtY" +
                "DPoINDYcDY8OJIVvTmOwPrvB/0uZZGUYXyqQ8Yvja0esMwv/Zl" +
                "Fnm2RdYFbNKtXQAglkbsH1UkJb7hNb0A6XXJxEcF4pD49BH4HG" +
                "RhHazC6fJjt5W3NbQz/nZR2p7nS8HXg8qmczH7qtokg1cq//X1" +
                "Q9W7PArniR/8s3N3zmKS/b+FGZj91WUaR2xOs+81Tk9+M5j/8A" +
                "dkOEsA==");
            
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
            final int compressedBytes = 1934;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2IHEUUHiN4E4UEwSySJSJoIHsLSS4xU92DmJisIezBCA" +
                "piUIMGjwq7h9n5WaY9eMkhnoKHoAtLBAUVlLAQYzBGIyrejBAJ" +
                "QSHiIf6gF7v3zZv3XtWr3v6bNpNuuvp97+97VVNd0z3bbKMhN3" +
                "PSlXxb57geqW3m80bBrUhk1pjBx9lztlfj42x8vNe+ELdb2h/5" +
                "fXtvi8gPnVxfxceXCscnZrXwOK2230+t/7wWo3o6o9I/FmsLfY" +
                "KLM9l95Xxap7cXC4/TxXpi0rfWidYJakECBJh7cLtE6MtzogTy" +
                "4FM3TkZwXon9Neg9aDSWpm02qtBmdvk0XGY+eWbZ65o2eK5R45" +
                "aVbRCvN53XMq1OVy18JV9F7Z8zzttrSuyPObl+ynyVXKt65M2U" +
                "maIWJECAuYeZCjpo51qQgs7ws+xgPNpJK+NkFZxXYlmD5i17oL" +
                "FhbxBRrXafXP7knG8+BW/45xPaXJ9E42rHOZ80Np9fNt8w3rCF" +
                "M2yApEcYBr0w5D4cBT3wBh/KDjKPpDheBeeVLLIGaZc9wMNlw9" +
                "5QX7Eq8uU9duo5Eh6JpbUWzrADQh1aOifJg7RD21vgHbQxHu1r" +
                "FbW1uNE4WbySRdYg7dwGFSeHy4a9ob5iVeTLe2yzmwWzEF+Bwx" +
                "YkQIDJxs+wd46TN/gOV4AFRJQreIbiuJ77Ea/E/hqkDXugsVGF" +
                "nHlw3u6Txr8mzZv5WBq2IAECTDZ+hj0ep3nSs17PI6JcwVMUx/" +
                "Xcj3gl9tcgbdgDjY0qtJldPg0HVwKxcgMGXXDFnCEZMcroDYhs" +
                "5oydM7Gglfz9vGhHL4hFH1kBaXm+pAbSYW7Mo1Um46kqycu+U7" +
                "aM585v8EWd95lL05XfP82aWWpBAgSYe3C7ROjLc6IEcvCsGycj" +
                "OK/E/hr0HmhsVKHN7PJpOH0+mRVrVFdSRnzF55No0iLX/SxXqo" +
                "mosrL2m2O67r6t9br7uvLrbpvZRi1IgABzD26XCH15TpRAHnzv" +
                "xskIziuxvwa9B/HT/A82G1VoM7t8Gq5rPoX31DmfqmfrznUfF3" +
                "i28C9ZD6WwHEqzrlvjvjRr/1S+Wor4wTh1D3QfKzJO8vfM7v60" +
                "cQo3jmucugeV+ZSRrftE5lmwHXbChefT9nSWEr+5bs9rzcqWva" +
                "rWDOyEC/dmJp2lxDjN5LVmZctelX99ar5Y4Sp4qFT0vrwRVdZe" +
                "fn2yPpup1HV8qs71KStb8arovmDwa77IxYONW2ZbeqXy+8zdZj" +
                "e1IAFqziQt9zC7B3+gnWsxVuZsNHpzaEUt7L3DXC9tEvFY1Gve" +
                "sgdQu6ySKrSZXT4N++dT88HJnU95a8+/LY5W3OZW5Snt7zy5en" +
                "Me/eGxj9NW9RnzzxLX3ZyZoxYkQIC5B7dLhL48J0pSltl4BOeV" +
                "2F+D3oN4nDbZbFQhZ078XD4N++fTrbNF+efT3mr91lakVf19lZ" +
                "by9+Ti76uUGaf876u01L+Fu++r9E5lf1+lnvd6onvrHKelu7ON" +
                "U/73epo36CCNLbnIzaB7+mTAvpw8s6wHkR4JWt7mZ9Ztzat0aD" +
                "YfSvPMHpVmdX14nXokaHmre6cx27Y81133WNHrztxZ53Wns7nX" +
                "Xfel3Nfdb3SQxpZc5GbQPX0yYF9OnlnWg0iPBC1v8zPbtjzzyf" +
                "p8btnvu+aj2eZT4jeO77sS192GWq+7DdVfd/XcF5R4BrlZYD7d" +
                "rP7+yco1pr+bl/qN7PlUq/IuZtNk/AxMjpG5HdenPRnXpz3lr7" +
                "to0+Q+t0Qbq74f73/XP9dfbZ/tfwPj1D+ffT55Mip/Ie9fKJXx" +
                "s1Sr8jfy6I5MeS9F8Xrfv1zo96enG7fB1gyq9Ys9O80OtSABAs" +
                "w9uD3ZO8fJu9mxc6IEsrmL4rie+xGvxP4a9B5obFShzezyadg/" +
                "n6L7Jnc+LZ0bN0N/9I5269/JHafx1744uldpHZjgcRp77XSf2d" +
                "pf4ei/XCr61dzjtL/y68x7X1B07tJ9QXT//3VfkK32/qW19nK5" +
                "+RRNTe51F20ew5XmuR9vPTm5zy167crvBb8UfQ42L7hS+a1crv" +
                "zRVda+7jo+W13W2tfx2arHxRw1R3mLe8y1L2mlzfaRCLwxAmOl" +
                "LON4FaThWbPVQC0cSe2uB+8xMbu90Nnj83Jy4D6MX0Yt2FFrI8" +
                "a4zO3ck+eVcTarm1VWwjH3lpzo69okk6axeXELr4fXsYUz7ICk" +
                "B511hDEcoRw9oMXxKjivnXe9GqiFY2la8+A9JmZejcvvRgzXp8" +
                "Lvj/f/Sf2m3lrr89105etT27T3nsYWJECJbe9p0IAft0sEvpAR" +
                "IzA7ZOGRrd8xCizohzrOwTO6Neg9kGzkkTDx6iDa5dOwc0fxwZ" +
                "ieuN6p9fmucjazw+ygFiRAgLkHt0uEvjwnSiC33nXjZATnldhf" +
                "g94DjY0qtJldPg0769Nf2cc4z/9XaS3XOp8qZwt3hjuxhTPsgK" +
                "QHnXWEMRyhHD2sxfEqOK+dd70aqIUj/r5TPHiPiZlX4/KP8K5w" +
                "VyyttXCGPdgcbA53RY+gDbTcw0WQgbJRVKORZHPjRtWKLDwrad" +
                "JqoBYOlw30lIuYeTUu/yjiP3dsePw=");
            
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
            final int compressedBytes = 2188;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW8+LFEcUbiOImtyFXMySvS6bRQ8JCThT3StBDzmIGBJYNQ" +
                "iSHDY3iUggu+OPdS7JHyBIIPGii8uq+aEQBEniortosh5yyU3c" +
                "o5tLFHJIdb9+/b1XVTPTjj3tbtJFvar33vfe96rs6enuWaNo6m" +
                "YUTf1k++WpX63cPvV9VPKY+s6zLNp+J4C8YX6P+j6m5rt6f/Zt" +
                "YbapH4LWX0pW0WGfTLvKfYqiWvepXf0+6WP6tOL7MaroeJ5Mzx" +
                "4bjniuGnaZXZA0Iy2dt8ckQvq1RrE6J8/yXPf9OB0hebXeuYbw" +
                "CkJs2Wre0NWR5vMF9XEzbme5pBlppMMnR1djbM49zhpyjX/rx+" +
                "kIyav1zjVoH68gxIYKXWafL6R3/9x1P1qT5bHjl6Iaj0Gw1fN9" +
                "N365zut4mK3K77szLw9on5Zr3aflqvcp2ZPsYUkjNTu/Dxt7JC" +
                "LZ05qUGudDNkR5uVWM9mmOXjXoFRT9fgghVwxmWY3Pz7qZMBP2" +
                "SpVLmpGW/JZK+OToahRbXBsnWGMv5jqbjIBNc3SrQft4BVS7Zk" +
                "OFLrPPF9K9s/DvYqeXq7yO13sky5VnPJgcZEkjNTt/ABt7JMLX" +
                "CI1siPJyqxjt01nL1QCZ9wchhFwxmGU1Pn+hH04O21kmaaQWRe" +
                "23YWOPRPgaoZENUW5uHaN9Omu5GiCpz2wJIcAl9klV4/MX+qHk" +
                "kJ1lkkZqUdR8Czb2SISvERrZEOXm1jHap7OWqwGSelq7jwCX2C" +
                "dVjc9f6HuTvXaWSRqpkcY29kiEr3GM1Ny5jiuqdXjdvL1qgKR+" +
                "dn8IAS7mbb+jq/H5Wd/90u6XoogkjdRIYxt7JMLXOEZqPG83Q3" +
                "Fcr8vr5u1VAyT1M6+FEODC+aSr8flZj5fipSgiSWPeRmxbahv2" +
                "FVYglhTe+vIMI+xFlJ2NhOK4WulzslpL66iM0n5ZXyrzPhJCgA" +
                "v7JNfEGJ0/1xdjew9Nksa8DdtW2ArPsEAsKrz15ehh9iLKzoZD" +
                "cUW1i9Kisno1aL/0pTLvwyEEuMQ+iTUxJszu3D9tH8wdjdla5/" +
                "3T4Nni69X4XN3UWrEpUfHafF9gNtf63nfzoN/79v+5Gx/q31v5" +
                "+6ehanH1nU/1/o7QvFnmfDqV4Z7395aKP3e3+r4m3+rjc3er3O" +
                "fuzOtrbZ9qPp+ulNunFLe29in+oM59CrMFzqexF7NPHZF1n0/X" +
                "Sp5P1/qu6ctirxuD/U6ablWT54tXA7k/r+/3lua9dXx9ujfo+6" +
                "fpvn/5au9bO+99Z94bNAP2qXknWrdH9bUnW5ItLGmkZrn+hI09" +
                "EuFrhEY2RLm5dYz26azlaoCkntbuI+SKwSyr8fkRUc99QftAnd" +
                "ens69UfX1KNiWbtJbqZHMlIyWC45NN5lg4p8tG0YiUMWDwETIv" +
                "YzkX6szvx4/p2mXuzjvRyZZVsjXZameZpJEaaWxjj0S4mjnBMR" +
                "yPKDe3ZAnxapbeNUBSNydCCHCJnVDV+Pysxw/jh+pO1uoz+8jm" +
                "SkZinqJJix82LsgcHd4VCbzPKxm4+Ri/GlknHY0LunaJ7/ge62" +
                "EnG8X716eZ/f1cnxp/rJ37p7SWaq9P8Uq8wpJGaqRpBMaQZo5z" +
                "DMcjys0tWUK8mqV3DZDUzfEQQq4YzLIan7/QH8WP7CyTNFIjjW" +
                "3skQhXa1ziGI5HlJtbsoR4NUvvGiCpNy6FEOAS+6Sq8fn9iDzu" +
                "sh5DvuB5edl89mLuKUNVVV9LfDW+ypJGbqmmERhDWuMioYGVM4" +
                "nVc9ens5arAZJ642IIIVeM9SOfXLHP7uzbnB5DvuB+z72oZ5QQ" +
                "c3Njyeebjc/wbfJffJ+5oeT7pw31/97S/Wj8U+c5Vj1bfCW+wp" +
                "JGbqmmERjDGqGBlbP2h+E4VOFGuZZuNUBSnzkSQsgVY/3IJ1fs" +
                "siejyWh2bz6a38dnOtvMCuYSA6sbjxzON9CKxNPox8gqgDArbl" +
                "7Gci63ArMibZzbrHR9vhv1LWKVO5IddpZJGqlZrvPJjvZh9rEV" +
                "CFcz5ykDsiGKsvlxRUU7pEVmTS2to71qgKTus5Gds4u9UNX4/I" +
                "W+Ldmm9jDT027OkQeSkZinaJ6ZczqHcz6dk3jE+bzAhTF+NbJO" +
                "ZtO1S3zH82lbJ1saH9+N79pPYCZppEZa+v+AgMAY1igGWDkzN0" +
                "JxxVXC4XXz9qoBkrrPlsrpb5ALzLIan7/QF+IFO8skjdRIy/ap" +
                "QGAMaxQDrJyZ66G4olqH183bqwZI6j5bKvN9EryyXo71V5XNHs" +
                "eP7SyTNFKzXC3Y2CMRrmZahEY2RLm5MfN9Omu5GiCpp7X7CHCJ" +
                "fVLV+PyFvhqv2lkmaaRmP5UN2NgjEfFqa1JqOa/Ihig3t47RPs" +
                "3RqwbpY3Z7F/JxCAEusU+qGp/fj8h/3TxeXAvfXb+/t1Rfe2O1" +
                "sQpJM9JIlwjp1xpjZU6e0bz9qR+nIySv1jvXEF5B+ve+LhsqdJ" +
                "l9vpDe3NncaZ9zckkz0kiHT47UWpNAEzZ/atrJGnKZ24iTdokD" +
                "r9TN7c416Pp4BSE2VOgyu3xhvcvv5iP+2Xfq63XyHDxS7jn41P" +
                "k6noOf5f9LmYW+rzULJ49085/8qEq2/9l7lYmS71Umnv/vn5pD" +
                "63ifhsrt0+lP+v57lb8G87mLD9f6jnPgbAPbpwO17lPlbM2x5h" +
                "gkzUgjXSKkP232vmAMdjcnz2gev484aZc48Gq9cw3hFYTYUKHL" +
                "7POF9Php/NTmzSSN1Egz8+wjq5kHAlb2cQzHI8p+A82H4op/f4" +
                "dXs3DGEKusL5XUfTZeDdaafS/OyzVRrJs/15/ET+wskzRSI83M" +
                "sY+sZg4IWNnHMRyPKFvRXCiu2CeHV7NwxhCrrC+V1H02Xg3Wmu" +
                "3TnFwTxbr5dURxfXqzeNL7yuaZde5LZrvc58x2wqSWbpE976Bm" +
                "q4mg9yp9VvYv+DRO+g==");
            
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
            final int compressedBytes = 2182;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2MFEUUHk9EEVEMi6ICeiCbmBCPHDzs/GEiB4OEowdPJo" +
                "Rw8aDenO2GWSYkHkTFLEFckR8BMRzUhGjkIBqFA4maiFExhkSu" +
                "XknUrnnz5v12dc/ObGexK1Vd772vvvdVTU1Pz89urSaPzlHs1Z" +
                "+qTfDoHqpVeExWOx3tK+0roaVaq/U+9DESRec83nI4jrBIyYKs" +
                "fJTUte9F7snj0nyciyv3DtpP3rG/uej99Fal++nhyeKytdvd3k" +
                "0t9MCq3xNajuBxacFYyYk92Q+ld4L7ZUxafKynwZ8BaJcqSaHO" +
                "bPN5tl632a3DHTBfu2OP+tRkcfZg63T0zl2n3l0lcWfLMrY2tD" +
                "ZgC2coGccp8mGEI6wFaGKjUZpbjpExyVpOA7VQeyc9BJ8xZeZq" +
                "bH4a0fkqq19m9Xznm1otWdP5bLAn7y5a486nxnM1q987yIvj7I" +
                "7OhWj0a+f55GrvfO56L5dUIdapsxHXqcTIEdapfbHKdfKzjbdO" +
                "atTGJbqyflTpdXwJso2yn7pnFruf6mcqfd6dKbefuqdH3U/1d6" +
                "h6sTwrhiw/Kha1GK7THwle3vroWGY/Vn+bqhfLs2LI8qNiUYvh" +
                "Ov2R4OWtj45ljqtaqutT74sqr0/dTUvB2rgdKhb0hV66ADGJ5J" +
                "ZmQS+PcV6dWWa1rMHuXkY9pIws2eJcvNjsca0jXxnPiEdzZahY" +
                "0Ac9iEkktzQLenmM8+rMMqtlJT6pjCzZ4ly8GGcpUmaxtdrMjV" +
                "CxoA96EJNIbmkW9PIY59WZZVbLSnxSGVmyxbl4Mc5SpMxiM8+F" +
                "ULGgD3oQk0huaRb08hjn1ZllVstKfFIZWbLFuXgxzlKkzGIzz2" +
                "+hYkEf9CAmkdzSLOjlMc6rM8uslpX4pDKyZItz8WKcpUiZxWae" +
                "P0LFgj7oQUwiuaVZ0MtjnFdnllktK/FJZWTJFufixThLkTKLre" +
                "b9Xe9Slffj/n3BuO/vGv+EigV90IOYRHJLs6CXxzivziyzWlbi" +
                "k8rIki3OxYtxliJlFjt4FXyOKnm0j/yyVxyzWIqEkv8oog7EQI" +
                "8sn1HOIF9VXJfj3UWVPLoX88djFotHcqi5Kz+KIzmG6/RGJm/i" +
                "GI2wqvIz29jM76FiQR/0ICaR3NIs6OUxzqszy6yWlfikMrJki3" +
                "PxYpylSJnFVnQdv7z463jy8vK4jtdfDRUL+qAHMYnklmZBL49x" +
                "Xp1ZZrWsxCeVkSVbnIsX4yxFyiw2eyY2QsWCPuhBbPjIHuYW9f" +
                "qxdzlaxohXZ5ZZ7Ujik8rIki3OxYtZHTp3XPPMrVCxoA96EJNI" +
                "bmkW9PIY59WZZVbLSnxSGVmyxbl4Mc5SpMxis5V7JlQs6IMexD" +
                "Ra97gnec3GiFfjZVaPFXVIZWTJFufixayOYCc5zztfc3M71f6z" +
                "aA563EdI3SuOWSxFQoncF2xHFOGJzxuJWN7mqYrrUlesV0LFgj" +
                "7oQUwiuaVZ0MtjnFdnllktK/FJZWTJFufixThLkTKLHazeDqrk" +
                "yfbVHu4jv+wVxyyWIqFE9tMORBGe+LyRiOVtnqq4LuXZGioW9E" +
                "EPYhLJLc2CXh2TNvfzrHYk8UllZMkW5+LFrA6dO6555tdQsaAP" +
                "ehCTSG5pFvTyGOfVmWVWy0p8UhlZssW5eDHOUqTMYjPPX6FiQR" +
                "/0ICaR3NIs6OUxzqszy6yWlfikMrJki3PxYpylSJnFDnbZTqrk" +
                "0b2YPx6zWIqEErk+7UTU4JX4INfpjUQsb/NUxXUpz9OhYkEf9C" +
                "AmkdzSLOjVMWlzP89qRxKfVEaWbHEuXszq0LnLaB68Kxx8z9lc" +
                "21xbqx2Y2G/FAlt1h59t9vh4rI2/Q8WCvtBLFyAmkdzSLOjlMc" +
                "6rM8usljXY3auoh5SRJVucixebPa515CvjGfFoHw4VC/rQC3GO" +
                "5JZmQS+PcV6dWWa1rFIJtzmaWo61Ma0jXxnP6B2tY7Y3/jEe1+" +
                "ijJ6k9J8P7g/NZ7E2StarRk9Sek2HB9ibJWs3oSWpnn7D1/0Ij" +
                "Oc1f74ax/iOTnBq8GopvapK9CnsseW/Y/zgZ/qYtOdG5npP5g6" +
                "yq3yYn6tu6xqXO7Zj619cPR54cnM832LeFSWTNet8Vrs255JPY" +
                "fcFkj9k9jYNjPI4j/wZ2nGw5O3S+NY8tnKE0p5pTrfl0gSPo7F" +
                "vAQGw0KrujmfLGcRXk4azB0/u2SAO1UG220ML9E89Lemm8nZUc" +
                "sZT7KXuVPVflfebks3X/VOv0xgjPh73lsQeerHKdWtcmz5n3/V" +
                "3vB2dVxSfry/fvEeZK/j1Cemu5/c6+2qPdmywutp8aDxaOXLb7" +
                "yde+NPup9+Mk7wuq3U8HXlgq5voNql4sz4ohy4+KRS2G6/RHgp" +
                "e3PjqWWcba8+15bOEMBSyJoLNv4RhuYb/3kzeOq+B5NW+RBmqh" +
                "djd5CD5jyszV2Px2hPqcbnNzs/2cLvhyPxvbnIcJntjIwvuPl0" +
                "b+nG5z+c/pyilrH2kfwRbOUJpbmlvIh5Hgo8LxIQZowBA7RPhI" +
                "GsdVkIezWg0yLmeA1WYDP3FRZsIixs+es5+mm9Ph80y19tORx3" +
                "A6DxM8sZFFR+/nkffT9Aj7qaSyan4n1lix+HXa/+zI9wUryt0X" +
                "9L2Lui/Y//z/4T5zbmVJ3L2Lvqdbor/Gr/9b5TqVzTb3wKLX6X" +
                "/xvqV3feLvrLe1tmELZyjZY3KNfBjhCGsBmtjSfTKCJU3kGJlX" +
                "spbTQC3UoN0i+IwpM/Ehxsveutm6mfX6LZyhZLl+IR9GOMJagC" +
                "a2bJ1EBEu2TmKMzCtZy2mgFmrQbhGUi60T40OMlz3dlM1ndX9W" +
                "T8jnXZ29GqfrBq+sq/1dmQ7+7026il7v0uFzP10zG70OpI+xvv" +
                "O8T9YJtPifKOnjs6uG/cG73/QRof2+fnu/yrk2fajM6126Pn20" +
                "3/kPx3c4+g==");
            
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
            final int compressedBytes = 1614;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW02IHEUUnmNyVmFZsmxuAyaHzYJ7iuB0zyCJUQkhIuwO+J" +
                "NgXHHx5FF2tnuSdiYgKF7NmqhRosYorILxh8i6h7iX5BJkT0YN" +
                "OYmgXlzErnlTXa+qXvdUd1d3prebqq569b2felP1qvpnajX56E" +
                "zzUuOnWqqj96tK8bs00vdqBR9pbU9/dN7Iytn7rTY2R/BkeX5y" +
                "3gz7ft2ck+ETWr8p00/2tTXnm/M8hyucUJMR4krVGouch/MLLl" +
                "U21kLplbX0fh9lg8ghNRYpBO6x0Iyt0fVH9XazHZYGOVzhDH+T" +
                "VUHjLRih1pxVQAtpgguk6XyRtW1MwVLNbBA5JF0b0IUsoRlbo+" +
                "uP6gvNhbA0yOEKJ9Q4jbdgRHPBW8K1oV4kTXCpsmUeXa8qN94G" +
                "3Ma1hyPwDwohdCE/Sdbo+gVHaxvFv32tbVYHWmubxSdeFkiMwD" +
                "RxyLXBeLrK8UKezIEpgJNpOgauy5Nx2mQrddToQ+0l8tSBSJef" +
                "Mnb647Pe2bfFP+4fkuqFrKj+Ufe7HNyH03KYauv9lcZP/uP+o1" +
                "n85C1Jkh5L8pOzkXlsbCT7yX+C4jHs/ZEy/KTM5akkPyW15htP" +
                "lJ9MtZlb1ZpoTeCcn9AmI1SMt6RyAA+u8bJ7AiNlHixVrmEKbY" +
                "PaA0i6Nt4bbB22N06/4Oh8H6Zvw3S5s8Hu7zpfgoT+/SP37msa" +
                "ZTNMxB6+83Wue4TPE1vXdVr/PhL5FUn9MYtNKx9njU8jIutTZa" +
                "53xWlr3BGJaourJSHNuZJadQy2k+YEKs5pdJJmua3hN3yRQwlq" +
                "rOxcwQhW5+2YCiWGHaw1Vzi/4OJUmU+2AuuV61yiqlWg5R5Q2o" +
                "SFWDPDqX3S9cM1Lj412rqPu+9WIz5RtlPxqXvOPD7FxvEHqusn" +
                "0zh+ejG/nww4x3c8PWvmJ4bLuN7N13bAEdwxw/UnUvxaMeMpWB" +
                "iX8XT6cNrxFBjGp3T7J+/9Qf7RgEu8R9g/oA4ikvfhUOZ/iXJW" +
                "vXNR+VPvUvf4sPxB52cpyh2LUBfCpOzYPG2Wdf5N0ro8GXFeHF" +
                "4vg+3D2vk8v5H3ifcZOWKfi/y0N+/o537S6MeKnnf5bR85s5/f" +
                "CfGp8bBdHOJ4WySqLa6WhDTnSmrVMdhOmhOoOKfRSZrVttj907" +
                "7q7gvO/GAWx/27pnE8eFGRFb2/6++v7rzrTRatQax3raPV9VPx" +
                "tiM/HamwnwqwncUnyk9Znz91rtu20P8zsfUfYp89Yxif/s5635" +
                "LfT+Nw9A8UIdW5yBI/OY1ToR0jcU2VwqkYieXKfKpWXapsCa5j" +
                "tKyTY/U2WRNFUfUa7AtG7sHGd18Q7La9L9A0vDp+827lldTz7q" +
                "Fi4jj5PPNghZ8/HSx6PDmn9JKFCHiqXG6btsessl/A1d3j7qnV" +
                "Xn/HllwmrbyD1rbynsVZOLxvcafcqdBr561ZnuO9eX/Ojjabfg" +
                "pek8bvmrWZt1YmL81hrzdhBLxWPmcBz5+uFW2z+HIs6FTjXm6Z" +
                "eDbQOmvY27OmWppzzTmewxVOd8adac6xOC4QjCoQgsrbQAJghH" +
                "RowZyCD1shKFgqo3gvYC65Xe4BT7o2lkN8wnqFvYJfla9yDGPd" +
                "8ImEW3frRHysJ8TOehyGUZI4C1jv6sVg9ftgd9ad1dc7RovVNx" +
                "uHYZQkzpHr3SOp/TRrvt6ZWha3H3dGvvFMsx93crw/Tb8fp7Xl" +
                "e3+3E9+bB2+V56fGjQrf392w7ydltl7VS1aenB0qLGYTdgYnSn" +
                "xesFXh8bRV9HhC3xfcJEZFK9U99b37n+LN8saT3TjurGe+u1vP" +
                "sN6tm42n4IKFOL5Z4Xm3aeandM/pnC2W+MlpnArtGIlrqhROxU" +
                "gsV+ZTtepSZUtwHaNlnRyrt8maKIqq12A83arweLplO467J92T" +
                "PIcrP1nNuY0R4krXQILA4pJzm+YTVqhcggK8STaIHJKujfdG9F" +
                "W1l/PqvYJrOXG8/3SZ4+nM3qret/SfGUc/pYnjsfGpwt8/0bar" +
                "fuqmHk/kPvNBYp/5koG8yE+2vs9M/x0GZTu5f7pkYTxNpx9Psc" +
                "iyx9O0eXzKfX+3q8J+2lWen7LE8bHxU8PMTzTuXq53brtMP7mG" +
                "/0cI1iz8X+rlCj/P3G0+72x8T+f8YuupRB5J6Xlpjly9+R92Ud" +
                "KM");
            
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
            final int compressedBytes = 2197;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2IHEUU7luQ3YsEExHJRoLEXUTY7B68hDBdPUg86EG8eQ" +
                "+4YQQPexCMOzs7yU56VYIXL0bUeBKMiSZGApIIRogIKrqJITGi" +
                "khCEBHLIISer+vXr915V9UzPTE87axf96v1/r2r6r3qbDYLmuS" +
                "Bofq33483vNJ1qfhkkW/21oMfWPO1oftD79yjFWYbm2WCIrXmy" +
                "q/VbV+evvXnG1qzc0toLBavImacCkT3mienPxgeqnKdD24vNU6" +
                "Idr3lqjuM89XM8WbmmuBQel1Zb9tlcH6PpFtlr6z/WHzFcZXnH" +
                "U7xS5vHUvlfl8bT6QNnHk1pTa0ihhxYEtQnSoYV7qLVWg0uYj3" +
                "w5J3PLGGmTGL1qkCPA3dTuevAREzKvxsXP5HtK/9JAE909aMCH" +
                "ZwwlPdmlhF6Ug2dLjvAz5M/1EpVjyVg3mnsThR0iuD+OhlcHkj" +
                "sKGz/hbqvbmktoorsNDXhOyZN8bF/KwbMlY/3ZHydRORb3kVGu" +
                "N1HYXTSq0EbmY/bjmz7aHe0OAqDQQwMJdWjhHtHuVoNLgMuzUZ" +
                "SdW8a4uHbe/Bq4DdH1tXXZ50FYNE+yGhffjUivdW9n18ILwUi3" +
                "5dYIc78ejHhrPlPct9UIxnSLR/AbVPScefD/+jxe+6XMeap2fe" +
                "evfTzWLXFnfOZp9fmy56m2WFskCpxpalbNGpl7GC3auRY4NZve" +
                "aWdBg7Fg4ZGol1WQTmLYNfi85Qh8aKAnf0K2x+TiQ1/R9elwlc" +
                "dTZ7LY8dTePOg6OJrkXHizrLvFMJn6j/VHDDeaao6n6MEqjyc/" +
                "2jDXp2hTtElKRgZdtCm8QTxynEd/srk509/yhsxPkTyGELgHxP" +
                "K86Iu57ArCG1yHuTFP3kzk6ZJKJqIJzSUUemggoQ4t3COa0OuW" +
                "CRkBMVxCPn6Te8oYF9fOm18DtyG6RlvzeRAWmwlRjYuPsrqlbu" +
                "n7Q0KhhwYS6tDCPVwJY7iU5Trvi8tWoxaunbdXDUTT/bzPg7Cy" +
                "Z5m3ZDUuvh2RrYyezp5BLpW5blHnqly3lI+mTqlTSKHHZiTpQb" +
                "1fAm/y5Vz8jj+OqrCjbE23GojCfmi7z4OPmMZP+fiIXXRr3k7w" +
                "Pvxc2qTst7k+RpMfWeAu3zUWK+4dUWZlq9cy7vdSV/DvVnne+d" +
                "fBZW6r1zPujzKvT/HxKucp/rT069NFdREp9NA01nukQwv3cCXw" +
                "Jl/OydwyRtpk1mI1EIU93uzz4CMmZF6Ni5/Jd9VdzSUUemga6y" +
                "jp0MI9XAm8KRtF2blljLTJrMVqIAq7niePB2GxeRLVuPgoN/+0" +
                "nuKzq1LtpUJrir+GWI9c69P/elHPYrUPdX36O8OaDjbsNvra2/" +
                "XsWvjRxp0n/9+DS0WgZ/pPNu48HX5k5PP0T3Hfvp4LTlT6XPBZ" +
                "6WfyrtouosCBBDL34HYpoS/PiRzw8Uk3TkZwXCnn1+AfgXnOtN" +
                "GoQhvZxfPJ/b2na38w8Hvf01W+pyv6d6n2+2P3ndhX4zhPB18u" +
                "/F3PfXUfKfTQQJIe1ENrNbiE+SgbRenz7hT3lDEurp03vwY5At" +
                "zjL3wefMSEzKtx8d2I9Eh8NY3fqrYGQedoaeujrZW+f/KiLR8b" +
                "PGN9ob5AFDjT1LSari+YeSIPo0U71wKn0mc78KHsYOGRqJdVkE" +
                "5i6PvqPhnlessR+NAMhXmyke0xufgJt7++X3MpBQ6k2q+Gko33" +
                "tgSxGfZ+kFbaaEUttJUW10ublHisrwZpwxFA7bJKqtBGdvF8cj" +
                "QVTQUBUOihaawrpEML93Al8KZsFGXnljHSJrMWq4Eo7KZ214Ow" +
                "2N8RRDUufiZfja5qLqHQQwMJdWjhHrZUW8AYjKcoOzdH8eFKlN" +
                "41EIW9tuDzICw2T6IaFz+TF6NFzSUUemggoQ4t3MOWwgbGYDxF" +
                "2bk5ig9XovSugSjsYcPnQVhsnkQ1Ln4mH4mOaC6h0EPTv8kTpE" +
                "ML93Al8KZs7RelBVv7BRkjcWXWYjUQhd3U7noQFpsnlg99fOjh" +
                "jnCHnv+UAgcSyGTjvS2hb/p2XmtqbyCHFjdORnBcLptMeTXI+n" +
                "AEJsZGowptZBvPLzvPrFMlrRtL+0JykEy10r/PDPeGe4kCB1K0" +
                "bij34HYpQazMiZzkZTYeQTqJ0a0G/wigdolGFdrILp5P7vLd4b" +
                "o7q52HBl3fDfNbDvDd4Xqx9V2iHfb7zJ2eeXp4g8zTzrLnKdwS" +
                "biEKnGlqm9oWbjHrFvLgdilBrMyJHPAmmx0nI0gnMWSVsgb/CH" +
                "xohsK6xUZ28Xxyfam+pJ/MUwocSCCTjffQWg3yBt90LbCEEuWK" +
                "L1Ec13M/wpVyfg3ShiPQaOs2GlVoI7t4Pjn/vKs/V+b7p2HOu4" +
                "PP9nve+Wsf6nu6O9EdpNBDA0l6UA+t1eAS5qNsFKV/4d+4p4xx" +
                "ce28+TXIEeAeX/Z58BETMq/GxXcj5POTmlNz7vsno8t95zOX52" +
                "M03SJ7/r1iX9/vn+aKv38qVlnnUes93U9p9Iya8eSc6VLbTJ6P" +
                "0fgi2z+O7D3dTNm+9T31PUSBAwlk7sHtpunr+B7S2zmRA159Q3" +
                "Fcz/0IV8r5NfhH4EOjCm1kF88nO8cXvRO6vHH/zll+7fZ3GJ1s" +
                "xdd5vFB89h1G+Eq/2J3H+qy18HcY/dfS74bXJ/2bXBk51siuT+" +
                "XXXo/qEVHgTFPzat7I3MNo0c61wKn59Lo4DxqMBQuPRL2sgnQS" +
                "w67B5y1H4EMDPfkTsj0mFx97NWl2bOloE27lQ7Bl9wZHYveNSW" +
                "7nNp7XutdYqG5WI3emsR6qjCRJcSw+2/Ixu478yjjiKP8eXFsq" +
                "63m8tjTAOnip2PN4f/83JPzY7NhQBxzYpCeX7Cyo5TaeV9psVD" +
                "cr5ZOVkSQpjsVvk0g+jbR1uQc9WdJbyANlXUEHyVQeet5513kK" +
                "z7vo0jDn3X/7/slf+1Dv6f4F7Chnng==");
            
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
            final int compressedBytes = 1927;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE2IHFUQ7rh4VURUkku87sFFBCEHPWxvT8SIK67xvOQkqO" +
                "APOZhbTzKZZDwoIeRgYAUlF/Fg8KCBQFh/Yg5GEwwoKvgTo2jw" +
                "IB6FiD1TXV1f1at+PTPZjZPd6Wbq1ffq5/teT890b+8mSZIkCz" +
                "P9F+/JYCOv8zbFkoRnLZKNYzSLMeyrN8sadu3j3v2sR5QJ0pbX" +
                "4sX2n7A66pUhI23t1eJ1pni91z5X2O3tD2m+tZQ0bO0Pgpkvit" +
                "fnTubp3gPJ2Fv7/Wj0bDjna2+fsjOdP4rZz4ZUUXOcsm/X8jgl" +
                "yY08Tr726ztOSZL+1X/xznN9r/cgxXQmItuFZzGGfS2zZg279v" +
                "GBZ1iPKBOkLa/Fi+0/YXXUK0NG95hvr96T75Obdlsf7a03+i/e" +
                "eY5nKY6ZiGwXnsUY9rXMmjXsqpUgxmyxmBvGrI56ZcgY/34a4n" +
                "tjYr+fajJPubNDfT9lK9kKWxppJ6QzZPQR1yCyvq5DFchr+zZp" +
                "EIuqbQauWJhRTchf4ePZ8cIbWBppJ8RzHMGMEHENIuvrukqt4b" +
                "V9mzSIRdU2Q7jgOCk1IX9YQVtvbj2+Afc/fz3VB05PwrVhM38/" +
                "DX//tHPLzi1saaSdkM6Q0Udcg8j6ug5VIK/t26RBLKq2GbhiYU" +
                "Y1IX9YYe+fJudzNwlbej49z5ZG2gnpDBl9xDWIrK/rUAXy2r5N" +
                "GsSiapuBKxZmVBPyhxXreT5tnG3+R3l5sToUyxy+KhYNc1CnX0" +
                "mzaP3sGLOOtVZaK2xppJ2QzpDRR1yDyPq6DlUgr+3bpEEsqrYZ" +
                "uGJhRjUhf1gx/dzFtu7Do9w/dR/arD/f9R4b5Tj1dm3W4xRUvR" +
                "k9+xY26+eudaR1RCx5hAhjBsY14lzsyZ72dTesQF6N6zX4K/DY" +
                "RKFlDvk8nC6ny8WdVGnJI0RYYjhaxLnlvdsyI+mFlXrexixHTI" +
                "OO8Qo8NlFomUM+D2cns5NJQpZG2gnxHEcwI0Rcg8j6uq56umF4" +
                "bd8mDWJRtc0QLniuotSE/FIx+c8Lursm4Xt8epzG0rSUTLfpcR" +
                "r/vuBw67BY8ggRxgyMa8S52JM97etuWIG8Gtdr8FfgsYlCyxzy" +
                "eTi7nF0uvtEHlkbaCfEcRzAjRFyDyPq6rrrqGF7bt0mDWFRtM4" +
                "QLrndKTcgvFdPn48PUZleyK2xppJ2QzpDRR1yDyPq6DlUgr+3b" +
                "pEEsqrYZuGJhRjUhf4VnMvgrnz7qY5qzljMxg+ttl5p3peovlV" +
                "gjDGEG9uVc7mUVWO3Yu/aMmamb69enc+lccWdeWvIIEZYYjhZx" +
                "bvmzwBwj6YWVet7GLEdMg47xCjw2UWiZQz4PZ/uyfcURG1gaaS" +
                "fEcxzBjBBxDSLr67rqfTO8tm+TBrGo2mYIF5wxSk3IzzhdSos7" +
                "JrbkESIsMRwt4tzyPVpiJL2wUs/bmOWIadAxXoHHJgotc8jn4W" +
                "xvtrc4YgNLI+2EeI4jmBEirkFkfV1XvauG1/Zt0iAWVdsM4YLz" +
                "SakJ+cOK8tr5enWkj474m8CjG/uOfKTnvneNe/80/++NvH/y2T" +
                "b684L1u8/sPTc9TsMcp+6z4z5XSe8LvVEr//9t7bW0jrWOiSWP" +
                "EGHMwLhGnIs92dO+7oYVyKtxvQZ/BR6bKLTMIZ+H023ptuL4l5" +
                "Y8QoQlhqNFnFu+m9sYSS+s1PM2ZjliGnSMV+CxiULLHPJ5eOHq" +
                "wtUkIUsj7YR4jiOYESKuQWR9XcdqLa/t26RBLKq2GcIlx0mrCf" +
                "mlonVNfQ6v9THNWcuZmIFz2KXmU36NK2yGdJI8PRfm1LNlT2EE" +
                "1zTitxLU5x+ra8L2/EIx93Xfz78Z2MHf9ue/FFfRlygn/7vwXx" +
                "h4/7T5Cvui6nKnz5v/lP+cD5595b+ba9FM+1ZAt7Xvbt+Tr+Yf" +
                "qeqL+SVAP1Te1dr7vZcH8XN5cQ3OvxrjSnvLwN7eviNdTBeLT2" +
                "BpySNEWGI4WsS55Wd+kZH0wko9b2OWI6ZBx3gFHpsotMwhn4tn" +
                "09nCKy15hAhLDEeLOLfknmUkvbBSz9uY5Yhp0DFegccmCi1zyO" +
                "fhztOdRzuPdx6pnoQ+MfYdxr31sc6TsWjT1on+/q6zOJqWcfKK" +
                "zK2trWh5p5jOCHNsBdUgsr6uQxXIGyqJaxCLqm0GrliYvXWHON" +
                "2T7inOrNKSR4iwxHC0iHPLc3kPI+mFlXrexixHTIOO8Qo8NlFo" +
                "mUM+F+9IdxReackjRFhiOFrEuSX3DkbSCyv1vI1ZjpgGHeMVeG" +
                "yi0DKHfC7ene4uvNKSR4iwxHC0iHNL7t2MpBdW6nkbsxwxDTrG" +
                "K/DYRKFlDvk8XD2x+VNeMmO9EOn5+sw6n3BdT+ys9TDyK2kW7e" +
                "jMfmz+V3l5sToUyxy+KhYNc1CnX0mzaP3sGLOO9V6Z/nvOUbTM" +
                "/yYvL1aHYpnDV8WiYQ7q9CtpFq2fHWO2sVHOp4Nvbdbnmd1L3U" +
                "+6q+0z3Yt0nLpnh//c1XR0ft7snruujp9Go1+O3Xfwr6C6F9b+" +
                "+fihdzfW+XTonen5tLbn081/nHqvTo/T5Byn4Iq2Lv8zSe+19f" +
                "sd1MHvbsivuv4DukobOQ==");
            
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
            final int compressedBytes = 1289;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj1vE0sU9T+htygpkEBI2d007z9EokiDFAr4B7YAKdWjoq" +
                "SjoQmiAKRIiAYoUGqqKJR8FHzUr3gbX1+fcz/Wuxg7KDA78p05" +
                "M+fec2ay6zhWtqZb09Foax5lJEgw1rj3SLlyaT6yMLbVOIN1Le" +
                "72YNd0B5kaHHrlqJfh5qA5GI0kSi9NkM7pCjMi0hxGfmzz1K3X" +
                "9XX7PCCya8+AFs7Juon6yJi8al8v29fB5G0bL0yejwZek2dh5q" +
                "h9vUuYh6NfuO79s9TF68F+X6Szb1bxNN0dlWvIz+6knEE5p/Vd" +
                "kwubqDrdO+/nUn2uPmuUXpogy0CfI81h5Mc2j12wrq/b5wGRXX" +
                "sG7xjK7CbqL/BxddyOZlF6aYJ0TleYEZHmMPJjm7dw63R93T4P" +
                "iOzaM6BF52TcRP2Yscnn7s+7qhtxtM6qZ5O9Tu9U9fHpS5vO6a" +
                "ysM5ORr6KzzOS6Ns+rxqrWCWNmW03lxjWrlM143cXn9o/NR43S" +
                "SxNkGehzpDmM/NjmsQvW9XX7PCCya8/gHUOZ3UT9mDF/f/q3vP" +
                "dkV/2gfqBRem2nyDLQ50jY4PKIuXbs12zVYR4Q2bVn8I6xf3Bj" +
                "LquX33cr3V+PbJ+tLctbTW2zjLX8rrtUXUKUkSDBzOB1i5TLNX" +
                "Vkx7YaZ7Cuxd0e8h1kanDolaNehs/D90+Tp7//+6fqWnUNUUaC" +
                "BDOD1y1SLtfUkR3bapzBuhZ3e8h3kKnBoVeOehku99Ow+6nZbX" +
                "Y1Si9NkGWgz5HmMPJjm8cuWNfX7fOAyK49g3cMZXYT9RVXO9VO" +
                "e2fNo4wECcYa9x4pd34v7yhCLc60837NayzzYNd0B5kaHHrlqJ" +
                "fh8twNfO72mj2N0ksTZBnoc6Q5jPzY5rEL1vV1+zwgsmvP4B1D" +
                "md1E/QW+1dxqR7MovTRBOqcrzIhIcxj5sc1buHW6vm6fB0R27R" +
                "nQonMybqK+4vp2fbv9TDuL0ms7RTqnK5HBSNjg8oi5duzXbNVh" +
                "HhDZtWdAiz7NU72Yy+rl/Wlg9l98Tnc+lXPqP6e7P3E/1ffr+x" +
                "qllybIMtDnSHO4Aq/EPHbhs/zMMg+I7NozeMdQZjdRHxnl/ak8" +
                "d+fvnOrrZ3lOuVq5n87ifirfj5f7qTx35bk7J09hOafy3JXnbu" +
                "NXc6W5olF6aYIsA32ONIeRH9s8dsG6vm6fB0R27Rm8Yyizm6i/" +
                "wFebq+1oFqWXJkjndIUZEWkOIz+2eQu3TtfX7fOAyK49A1p0Ts" +
                "ZN1Fdcn9TmP6EFy5yPymSG8u1a3fHf1czv1tV1z/IqdtbX48j8" +
                "zm9OTrrmJL+8j6/y+27/fTmnQd/bHa5nrT48q99Eqzr+tWt6WU" +
                "f7x+XTwJKfwGPbZ2vL8lZT2yxjw/fTh3LXlM/j5Zw29HfLxeai" +
                "9Ix5DhEczPp8rpmrSTYyoy7WbR1g5Wot78B759rdJ9F9Nm0bN+" +
                "PZeDxfm2GeQwQHsz4fNVIvY62PzKiLdVsHWLlayzvw3rl25zmN" +
                "M7dd+9n/Up6xIdf+tz9iF18H8r6X7+k2+/ddOaeBnzN/lPeeQU" +
                "/sf+UMBt2bD8sZdF/bT7af6EheXRyNYOVs5AzjMSMyvT+4QIy+" +
                "8r2gVqyHWuyc2EfbR6cRr2Qvc45n5WzkDOMxIzJtFa3KWZmvfC" +
                "+o5etxLXY+m7u5fRNRRoIEM4PXLVIu19SRHdtqnMG6Fnd7yHeQ" +
                "qcGhV456GR79Dw+FFoE=");
            
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
            final int compressedBytes = 1439;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWU2LFFcUraDu/QcibsV1cBWnq2fhQiE/YRIIMwT8DaUzrW" +
                "mQkD+QELIKAQNZJIEQEZKJi3wStwoiKLgU/0Cq6ubUPffeV101" +
                "bbqZ6bx69H33vPtxTpWvqmvaoqgeFEV1v/58XT2s7bnqu6L3uP" +
                "0Vo+pbH69+rz+/xrrqh+INjuqbhdHD0X2+D+fzZb36y8jqfJ1G" +
                "HOVOuQMrswxBNkPnNEINI+/bOlbBvL7vkAa1rNpn8BkrM6uJ/B" +
                "3eK/dqr7UyyxCENUQ4IyLUMPK+revUOl7fd0iDWlbtM5SLrpNR" +
                "E/k7fKO8UXutlVmGIKwhwhkRoYaR921dp9bx+r5DGtSyap+hXH" +
                "SdjJrIDzytj6KAFU+QYI3x7BFy5UC9Vqlvu3EF81rcr8HGcAYp" +
                "NlXomSNfCh/tOe6eiyf+Ob7/cvz3XVG887z5YGBNPInZTEa+C1" +
                "Y5xn09s2WNXbWfVabIWpxLKsZdhpTF3PXsp/n5k7+ftl43Hwys" +
                "iScxm8nId8Eqx7ivZ7assav2s8oUWYtzScW4y5CymFsUk7eaDw" +
                "bWGm9+QWI2k5HvglWOcV/PbFlj1wbf+gB6VJkia3EuqRh3GVIW" +
                "c93ePFfko+++e9V8MLAmnsRsJiPfBasc476e2bLGrtrPKlNkLc" +
                "4lFeMuQ8pibn4vWEpTvu/+g98L5hfXv5+2Do++n7YOx+2nO1+s" +
                "5neV+aV8363yvrv54Um/66ZPpk/YYkjMZsQcXyE1jLxv61gF80" +
                "YlizWoZdU+g89YmVPnnWK3991Hl47f993s6qruu3Z1qedTvk4j" +
                "Nb2b3wCSz6eD6YFa8QQJ5gyOW4Rc7gnP+rYbVzCvxf0a0meQYl" +
                "OFnjnypXDYhZ/mvZN8izu7dVateIIEcwbHLUIu94RnfduNK5jX" +
                "4n4N6TNIsalCzxz5Ujjvp1X83ZL/Ds5/By8+yhflC1iZZQiyGT" +
                "qnEWoYed/WsQrm9X2HNKhl1T6Dz1iZWU3kB548njw2v3a2WNa8" +
                "RSZnIN/GbE/bHRn9vIj7LM9iV30/tpzfd6TiXH+U59PB55v1fD" +
                "r4bDW/q+zvbdhzfHc112nTvu+W//vuOH7f7b+/MLpz/N6f/s/P" +
                "p7yfRv2O8mj20+xBdX/2l+yn2eH451NPx78Taw/fqOPPC6N/LN" +
                "33t9b+OSb3SnWlUiueIMGcwXGLkMs94VnfduMK5rW4X0P6DFJs" +
                "qtAzR74ULu+V9+o3ztbKLEMQ1hDhjIhQw8j7tq57K3a8vu+QBr" +
                "Ws2mcoF72PGzWRP1bkv+/y+9MK3p8+zjsn76fl91N5pjxjUYNl" +
                "zVtkcgbqfZd+NqnWSq5RhpjBfZGLXl6B1869+69E31pTv316+3" +
                "RRiJVZhiCsIcIZEaGGkfdtHfR4Xt93SINaVu0zlEuvhFUT+YHL" +
                "a+W1+oq1VmYZgrCGCGdEhBpG3rd13b+b4/V9hzSoZdU+Q7loxx" +
                "g1kb/D18vrtddamWUIwhoinBERahh539Z1ah2v7zukQS2r9hnK" +
                "RdfJqIn8wNuntk/VO6u1MssQhDVEOCMi1DDyvq3rdr/j9X2HNK" +
                "hl1T5Duei+M2oif6z495fOH6N3vI916czvBfk65eu07us02Z3s" +
                "wsqM0SCboXMaSbbmsse51vcx23WcBrWs2mfwGev5a26sJXx3cr" +
                "f2WiuzDEFYQ4QzIkINd+BIrOvUOl7fd0iDWlbtM5SLrpNRE/m1" +
                "Yj333eS9dd53abZN//+W/HtB/r7btOs0fTZ9BiuzDEE2Q+c0Qg" +
                "0j79s6VsG8vu+QBrWs2mfwGSszq4n8sSI/n/J9l59Pa/vd93J5" +
                "GVZmGYJshs5phBpG3rd1rIJ5fd8hDWpZtc/gM1ZmVhP5Y4UcN9" +
                "+GN/8kP5PozfXp5GnEsuYtMjkD+TZme9ruyOjnRdxneRa76vux" +
                "5fz+K9G31tb/AzbHrHo=");
            
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
            final int compressedBytes = 1334;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWcuOFVUUvTF+BRNjHN7wBUyAW8UH8AmExAFDAmOvNna6Zw" +
                "xJCAbjQKNE7fggMTEOfAyMRD9ASWgmzBh0GDGw6m42a+1Hvbh9" +
                "GxpOndQ+Z539WOscqqrrFrOZPRZf2j7zZUefb52sw4jYxLH4yv" +
                "aZry/vxdg2G7GJY/nOrBxln174qE5WJ6VnzHOwiMGsz+eaOZtk" +
                "IzPywm/rAGus1vIKvHau3b0T3XvTtHk1X43nz3wrzHOwiMGsz0" +
                "eNVMtc6yMz8sJv6wBrrNbyCrx2rt25T/NMrfbLB+6++2/ifbq/" +
                "xj3+78T4+6/Ofbh7ozyLyj4d4j7dLHswap9ulz04ru9PWxd7vR" +
                "eOaGd+ac6fm/Pr5R/tPi1/7I69Zq625Q+h1l/N+WfC8dNaCvd6" +
                "vb+NrnM3rOeTZvb3cj1t8Pn0RXn2dB/1Xr2nIzm7YtQiqt7rrz" +
                "sujiNipNcHFbBRV74W1Ir1UIuVl9/Ba9x335Q9GLVP35Y96Hk+" +
                "3KvvtRZnd4yPyqORMy6OI2KkraJVOSvTla8FtXw9rsXKV98Mrl" +
                "RX1EovTZCNQJ8jzWHkxzaPVTCvrzukAZZV+wheMZhZTeRHxpT3" +
                "zN3vXq/3zNXsqPfM6nH1WK300gTZCPQ50hxGfmzzWAXz+rpDGm" +
                "BZtY/gFYOZ1UR+xbvfuzf5f47uqXjt72P0BD9Vn4KVkSDBHMF+" +
                "izSWa+rIjm01zmBei7s15CvI2KDQM0e+DPvvvjvvPX8W3R11z6" +
                "/x3Xfn3YnPqZf43bc+XZ+GlZEgwRzBfos0lmvqyI5tNc5gXou7" +
                "NeQryNig0DNHvgyHf+PyuyW/ljvvu5H5b8p9d64+BysjQYI5gv" +
                "0WaSzX1JEd22qcwbwWd2vIV5CxQaFnjnwZbvuzT9tTm+TLaOeW" +
                "+LRqRDjUJ7Ps47r28Kyxaos/el/1QBmQtbqWzPfhZ15HtzJmnP" +
                "7d190Hb9D7eNmn8d/HF2+3pzadk5H4bCQjX0Vn2cd1PbNljVVR" +
                "zyoDslbXkvm4ypCyGNvciQftqU3n2lHzfDoQP0cy8lV0ln1c1z" +
                "Nb1li1xavnk1MGZK2uJfM1z6cDr7lLGTOW777TjsVb7alN52Qk" +
                "PhvJyFfRWfZxXc9sWWNV1LPKgKzVtWQ+rjKkLMY2V9iT9tSmc+" +
                "1o61Px2UhGvorOso/rembLGqu2ePdX1QNlQNbqWjJfc9898Zq7" +
                "lDFj+Xs37e9deT4d9vvT9qPX63raelSup80eH58ve5B+L9iut2" +
                "FlJEgwR7DfIo3lmjqyY1uNM5jX4m4N+QoyNij0zJEvw+W+G3dU" +
                "l6vLaqWXJshGoM+R5jDyY5vHKpjX1x3SAMuqfQSvGMysJvIjo7" +
                "w/le8qm3vPrJ8etyfHByeOku3MQ5yZrwv1RY7P6vPGGNaZZ8os" +
                "2zy6j9n6qjvVHbXSSxNkI9DnSHMY+bHNYxXM6+sOaYBl1T6CVw" +
                "xmVhP5Y8az98z7s3KU3y1ln8o+vTLH2c/bU5vO6az4OZKRr6Kz" +
                "HMl1bZ5njVWtEsYcbTk1NvosUzbject75rT3zOpSdUmt9NIE2Q" +
                "j0OdIcRn5s81gF8/q6QxpgWbWP4BWDmdVEfsWLq4urs5lY6bW1" +
                "SOfUEyMYSTRiecSxdux9tuo4DbCs2keAC/vE9WIu4euL681oZa" +
                "WXJkjn1MMREWkOV2BPzHuu1vH6ukMaYFm1jwAX7ZNRE/mRUZ5P" +
                "5XvB4e1TvV/vq5VemiAbgT5HmsPIj20eq2BeX3dIAyyr9hG8Yj" +
                "CzmsiPjHI9lftuaJ8m/P/d/0vFN+k=");
            
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
            final int compressedBytes = 1313;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz2PHEUQHVk6OXJwiSWcOCIgJiHdmb0/wI/wR+xfsHCsse" +
                "4/ENhOLOuERABISJYlczg4vgTxBXdOuB9ABhLbU/dcr6qrZ2f3" +
                "xgbhntFU96t6VfW6PTt3s7abZvG8aRbPVteXi5cre3PxTTPyWH" +
                "ydeX5aXccB87vmEsfiq8Ho0eg631r8mXh/2ErTzaYe8c68s/dT" +
                "s9H9VPep7tO6fdo/r/s07XO8u9XdgpURZ0KWoWOMhK1cnjHXzn" +
                "3MVh2nQS2r9gxesa5fuXmu4vn1+XXeN8Hi8xZMZiCfq/iaNiLZ" +
                "Q30Rjzm5mlyB18780hHFOb8+x7fR1D22YxQbytuu25tlTHHMr8" +
                "yvWJSw+LwFkxnI91XK3SRbMzlHO+QMrgsuankFXjvXLu9EyZfy" +
                "u7PuzPzp9Fh83oLJDPBtzNa01cEo90Xcs3wX6/X12DK/eF+elX" +
                "y9jtPu1ER6LD5vwWQG+DZma9rqYJT7Iu5Zvov1+npsmV/cp9OS" +
                "L1pPd2jHKBZ2Odzq+XT4Nhhv4jh4r77xjtqnG3UP6nvLdL8/Pf" +
                "ir3jljjuXHdQ/q95kTPsffr3swap8+qHsQvtXcnd+FlVFOQZah" +
                "Y4yQw8jPbR6r4L6+7joNalm1Z/CKtTOryfsD7832Zk0DKzNBgj" +
                "XGo0fgyoF8zdK5rcYZ3NfisgYbwwqibqrQd877Rbj/zf9aunBe" +
                "vA30swdfSOz1O0KG6P3hGsc5xnXdO4frmldN+NPb0KPKFFmLtU" +
                "SxTx57HWVl3BFH+3e6cMInM4lZJiNfBV6OcV3f2XbNq2o9q0yR" +
                "tVhLFOMq65Tl3NXO7aQLJ3xptrqfdiTOTEa+Crwc47q+s+2aV0" +
                "24v5+cMkXWYi1RbHU/7XjNJWXc8fXO/ZkunPDJTGKWychXgZdj" +
                "XNd3tl3zqlrPKlNkLdYSxbjKOmU5d7VzV9OFE740238oMctk5K" +
                "vAyzGu6zvbrnnVhA8+hB5VpsharCWKre6nq15zSRl3rO/B9e85" +
                "p96n9rg9hpVRTkGWoWOMkMPIz20eq+C+vu46DWpZtWfwirUzq8" +
                "n7a8Ym99PBR2//fmqPNr+f2qNx99Pnj+q/f6rfZ/4732fWfarf" +
                "Z0537O3v7auVmSDBzOC4ReByTczs3FbjDO5rcVlDvIKomyr0nf" +
                "N+EW53293Vz4cLKzNBgjXGo0fgXvy02QXSWpxp/T7mewxpsDGs" +
                "IOqmCn3nvF+E6++Zmz2fZn/oFcVKaIg5PmsomnNYZ5wpXrYxe6" +
                "hzHJu90iuKldAQc3zWUDTnsM44U7xsY/ZQZxtb/r58sXy+eLb8" +
                "VT53y6Pxn7vCT8zfAt/LS1X8fjD689Z1f+ztL9P//nT/6f/r+X" +
                "T/yej34PP2HFZGOQVZho4xQg4jP7d5rIL7+rrrNKhl1Z7BK9bO" +
                "rCbvn2fU97v6HjzN0T5NF0744JU4Mxn5KvAyk+vaPN81r2qVMG" +
                "a27QluHrOdIo/vW9+DL/cePHux7Wq2z5z+GKtlE831fqr7VPep" +
                "7lPdp7pP78L3T/X/vY7J7e50d2BlxJmQZegYI2Erl2fMtXMfs1" +
                "XHaVDLqj2DV6zrV26ey93r+1393NXneH2O/0ef4/e6e7Ay4kzI" +
                "MnSMkbCVyzPm2rmP2arjNKhl1Z7BK9b1KzfPJXy7S/9KtLcy4k" +
                "wIPkRyBiNhK5dnzLVzH7NVx2lQy6o9Q3vRPlG9PJfwSXdi7q8e" +
                "i89bMJkBvo3ZmrY6GOW+iHuW72K9vh5b5hc/WSclX5//DxX5aw" +
                "o=");
            
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
            final int compressedBytes = 1644;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWU2LHFUUncUgrvIDsgni30iymOr6A5JfIH5AEsgmIRPIIp" +
                "MJA2MggWTlJihkLzGDCoK4MS5Ek38gOCt/gS5cWFW3T59z7301" +
                "/WIGZ8RXRd13z7vnnXveo7vTmV5sLjY3VtdiczFhm4sRTGUsNp" +
                "VFlY3iRX2u1DXskBmqCy60ooPoXbXnrlKd67vD7lArhm0uRjCV" +
                "Ab6veU2vDsZ8X9QjK3bxs1FPo/LnrlJd1/vrk0832lVxtXOqu+" +
                "5/2M6g6pw+amdQc+29186gdO385vH+u6tX1nbV+sN/3nv/ndf0" +
                "+uvJnVN/vj/PaJkhw8rQukfgqiYyn3s1XaF9PZ73UN5BqRsdxs" +
                "65XxFv9VtDtoyWGTLMmo4RgbvsvQVELV3p52Mt9jjKg69hB6Vu" +
                "dBg7534lPH2POjM+uJffsJaZ1VbfuxKS72RntK411Q3f40LXrE" +
                "o974zIR+ylVFOVdc4yd+f74flueL7Y+XGI53a+rv68+CrN/Dw8" +
                "PxWY377RZ+iXR1Z/qNb5pjj7onJ1O6fKq3trfHBjzjKreaaiqI" +
                "JZralu7Oy7ZlXqeWdEPmIvpZqqrHOWuf/n19O939v7rn0+ncTr" +
                "qd/r9xgtM2RYGVr3CFzVROZzr6YrtK/H8x7KOyh1o8PYOfcr4f" +
                "Z6qn3f9X95NGKbixFMZejcnKZXV9XY1/P8XObYeOfsXDfvcp61" +
                "7myij6N2eHqvO2dP5O8H507fSdz74Mjq+//SybTvBTXfxS93lx" +
                "FtxD0iz+BYRsYmVzPl+jzWvGqdB0Z1HRm6Y+6f3LxW8I3uxpBN" +
                "0UbcI8IcKpmhyNjkaqZcn8eaV63zwKiuI4O95JxEL68VfK27Nm" +
                "RTtBH3iDCHSmYoMja5minX57HmVes8MKrryGAvOSfRy2sFP+we" +
                "DtkUbbTbEOZQUUZGWKMKWsnrVm5D36i7zgOjuo4M9pJzcm5y/x" +
                "W+3l0fsinaiHtEmEMlMxQZm1zNlOvzWPOqdR4Y1XVksJeck+jl" +
                "tYKvdleHbIo24h4R5lDJDEXGJlcz5fo81rxqnQdGdR0Z7CXnJH" +
                "p5reAH3YMhm6KNdhvCHCrKyAhrVEEred3Kbegbddd5YFTXkcFe" +
                "ck7OTe6fVyx/jfqs/VZXc93/vJ1B+3/LG//a+ax/hsyeOQ4iWW" +
                "U219TxlJGZ0R9dMGZf5b1QK+tRS50L+6A/QGZPYS8HVgNDV8zu" +
                "/8Arz/OUkZnRH10wZl/lvVAr61FLnQv7Zf9yjHwKe1lyIqvM5p" +
                "o6njIy06tAVVeVfJX3Qq2op1rqfJq70F9gtMyQYWVo3SNwVROZ" +
                "z72artC+Hs97KO+g1I0OY+fcr4THceuP8cFt6y3bf2I1qGbECz" +
                "Wb1Zrq+it2zaoj3v0YfuiMyEfspVS7+zT6mHemHdvfn9rvUsd9" +
                "Tv1uv8tomSHDytC6R+CqJjKfezVdoX09nvdQ3kGpGx3GzrlfCS" +
                "8uLS5tbFi00W5DmENFGRlhjaKY+3VwG/tG3XUeGNV1ZLAXz8m7" +
                "yf25or3v2ufTMf6OcLO7iWgj7hF5BscyMja5minX57HmVes8MK" +
                "rryNAdc//k5rWCr3RXhmyKNuIeEeZQyQxFxiZXM+X6PNa8ap0H" +
                "RnUdGewl5yR6ea3g293tIZuijbhHhDlUMkORscnVTLk+jzWvWu" +
                "eBUV1HBnvJOYleXiv4cfd4yKZoo92GMIeKMjLCGlXQSl63chv6" +
                "Rt11HhjVdWSwl5yTc5P7c0X7HK/6HL/V3UK0EfeIPINjGRmbXM" +
                "2U6/NY86p1HhjVdWTojrl/cvNawY+6R0M2RRvtNoQ5VJSREdao" +
                "glbyupXb0DfqrvPAqK4jg73knJyb3J8r2vvuNf7u+7x/jsyeOQ" +
                "4iWWU219TxlJGZ0R9dMGZf5b1QK+tRS50Xf2/5pf2W8l/9veX0" +
                "XP2r/tUY+cxzIqvM5po6njIy06tAVVeVfJX3Qq2op1rqfJq72F" +
                "9ktMyQYWVo3SNwVROZz72artC+Hs97KO+g1I0OY+fcr4Tbv3ft" +
                "7yrH/b2ge3t8cGNuzPafWM0zFUUVzGpNdWNn3zWrjnj8vSU6I/" +
                "IReynV7j6NPuadacfVLzB/jg9uzFlmNc9UFFUwqzXVjZ1916xK" +
                "Pe+MyEfspVRTlXXOMre976rec9vdNqKNuEfkGRzLyNjkaqZcn8" +
                "eaV63zwKiuI0N3zP2Tm9dq9/Z6av/eHeM5/Q0IU8c4");
            
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
            final int rows = 24;
            final int cols = 74;
            final int compressedBytes = 678;
            final int uncompressedBytes = 7105;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtlkFq3UAQRLXNKXyYQcexsbc2NmidwxgCX1+fQMjGySLkFg" +
                "m5hjVqiqruHjlDAl5JQj3d6jc1NYM+9jBMX4dh+rI+z9P3NV5N" +
                "56Hzmub05uf6/GiQn4f/uKZPb3ZfunWW5ttvnbOPc/qH6+Ov4b" +
                "h6zun3cQZd3+bVcQZd39Of4wz2rvKhPrjxzjLreVKrqIK32lPd" +
                "uLJfNatSzztj5SP20uqpyt+cZfb4e3f8X3Cc03ufU7ktt4g24q" +
                "6VJzi2K6PJaqasz2PPq/Z5YFTXkdAdc/9k81xd/fieur6n63KN" +
                "aCPuWnmCY7symqxmyvo89rxqnwdGdR0J3TH3TzbPlfqxPK7ZFm" +
                "3EXSu8QycTWhlNVjNlfR57XrXPA6O6jgTXknMSvTxX6rtyt2Zb" +
                "tBF3rfAOnUxoZTRZzZT1eex51T4PjOo6ElxLzkn08lyp78v9mm" +
                "3RRty1wjt0MqGV0WQ1U9bnsedV+zwwqutIcC05J9HLc6W+KTdr" +
                "tkUbcdcK79DJhFZGk9VMWZ/Hnlft88CoriPBteScRC/P1dXrNZ" +
                "7HMzJ78mUMIqnxjb+Ple7jlMhk9EcXjNlXey/UynrUUudCn8YT" +
                "MnsaezlZD4TO2N3/ySvvc0pkMvqjC8bsq70XamU9aqnz7Zt6Kk" +
                "+INuKulSc4tiujyWqmrM9jz6v2eWBU15HQHXP/ZPNcqR/Kw5pt" +
                "0UbctcI7dDKhldFkNVPW57HnVfs8MKrrSHAtOSfRy3N19e1Lu4" +
                "wXZPY0fhsX64HQGbu/p4tX3ueUyGT0RxeM2Vd7L9TKetRS50LP" +
                "44zMnsZeZuuB0Bm7+5+98j6nRCajP7pgzL7ae6FW1qOWOhd6GR" +
                "dk9jT2slgPhM7Y3f/ilfc5JTIZ/dEFY/bV3gu1sh611Pl2vQIW" +
                "4Y/T");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 18, 0, 3, 0, 19, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 20, 0, 0, 4, 0, 0, 21, 5, 0, 22, 23, 0, 24, 0, 0, 25, 0, 1, 0, 26, 0, 6, 27, 2, 0, 28, 0, 0, 0, 29, 30, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 10, 31, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 33, 1, 0, 11, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 34, 0, 0, 0, 0, 3, 17, 3, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 36, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 37, 0, 19, 0, 4, 0, 0, 1, 5, 0, 0, 0, 38, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 39, 4, 0, 40, 0, 0, 0, 0, 41, 0, 0, 42, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 8, 0, 0, 44, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 45, 10, 0, 0, 0, 0, 0, 20, 21, 22, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 24, 25, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 29, 0, 0, 0, 4, 0, 0, 30, 0, 1, 32, 2, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 34, 0, 35, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 38, 0, 1, 0, 39, 0, 0, 6, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 9, 42, 43, 0, 0, 44, 0, 5, 6, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 47, 1, 0, 0, 0, 0, 0, 0, 0, 48, 2, 0, 0, 3, 0, 7, 49, 0, 0, 0, 1, 7, 0, 0, 8, 50, 0, 8, 51, 0, 0, 0, 0, 52, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 0, 0, 47, 54, 55, 0, 56, 0, 57, 58, 0, 59, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 63, 10, 0, 0, 0, 0, 11, 0, 0, 64, 0, 0, 0, 65, 12, 13, 0, 66, 67, 0, 0, 0, 0, 0, 4, 0, 68, 0, 0, 0, 5, 48, 69, 1, 0, 0, 0, 14, 70, 0, 0, 0, 15, 0, 1, 0, 49, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 6, 0, 0, 3, 0, 0, 0, 0, 12, 0, 0, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 71, 72, 12, 0, 51, 73, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 74, 75, 0, 76, 77, 78, 79, 0, 1, 0, 2, 0, 0, 1, 15, 16, 17, 18, 19, 20, 21, 80, 22, 52, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 36, 37, 0, 38, 39, 81, 40, 41, 42, 43, 82, 44, 45, 46, 47, 48, 49, 0, 0, 0, 83, 0, 0, 0, 0, 84, 85, 9, 0, 0, 2, 0, 86, 0, 0, 87, 1, 0, 88, 3, 0, 0, 0, 0, 0, 89, 2, 0, 0, 0, 0, 0, 0, 90, 91, 0, 0, 0, 0, 0, 0, 0, 0, 0, 92, 93, 0, 3, 4, 0, 0, 0, 94, 1, 95, 0, 0, 0, 96, 97, 98, 0, 50, 99, 100, 101, 102, 0, 103, 53, 104, 1, 105, 0, 54, 106, 107, 55, 108, 51, 2, 52, 0, 0, 109, 110, 0, 0, 0, 0, 111, 0, 112, 0, 113, 114, 5, 0, 0, 0, 0, 0, 53, 0, 0, 10, 0, 1, 0, 0, 0, 4, 115, 5, 0, 1, 116, 117, 0, 0, 3, 1, 0, 2, 0, 0, 4, 118, 0, 6, 119, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 120, 121, 122, 0, 123, 0, 54, 3, 56, 0, 124, 7, 0, 0, 125, 126, 0, 0, 0, 0, 0, 6, 0, 1, 0, 2, 0, 0, 127, 0, 55, 128, 129, 130, 131, 57, 132, 0, 133, 134, 135, 136, 137, 138, 139, 56, 140, 0, 141, 142, 143, 144, 0, 0, 5, 0, 0, 0, 0, 0, 57, 0, 0, 145, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 7, 0, 146, 0, 147, 58, 0, 59, 0, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 148, 149, 0, 0, 150, 0, 151, 8, 0, 0, 0, 152, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 153, 154, 0, 155, 156, 0, 7, 4, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 11, 0, 0, 12, 0, 13, 157, 9, 0, 158, 159, 0, 14, 0, 0, 0, 15, 0, 160, 0, 0, 0, 0, 62, 0, 2, 0, 0, 0, 9, 0, 0, 6, 0, 0, 0, 0, 161, 162, 2, 0, 1, 0, 1, 0, 3, 163, 164, 0, 0, 0, 0, 0, 7, 0, 0, 0, 58, 0, 0, 0, 0, 0, 59, 0, 0, 165, 0, 0, 0, 10, 0, 0, 0, 166, 167, 168, 0, 11, 0, 169, 0, 12, 16, 0, 0, 2, 0, 170, 0, 2, 4, 171, 0, 0, 17, 172, 0, 0, 0, 18, 13, 0, 0, 0, 0, 63, 0, 1, 0, 0, 1, 0, 173, 2, 0, 3, 0, 0, 0, 14, 0, 174, 0, 0, 0, 0, 0, 175, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 176, 177, 19, 0, 0, 0, 0, 0, 4, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 178, 0, 179, 180, 181, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 182, 0, 183, 184, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 185, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 186, 22, 19, 0, 0, 0, 0, 0, 0, 187, 0, 0, 1, 0, 0, 20, 188, 0, 3, 0, 7, 10, 0, 1, 0, 0, 0, 1, 0, 189, 23, 0, 0, 0, 0, 24, 0, 0, 21, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 0, 16, 0, 0, 0, 0, 190, 0, 0, 191, 0, 0, 0, 192, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 193, 0, 194, 0, 195, 0, 196, 22, 0, 0, 197, 0, 0, 23, 0, 0, 0, 60, 0, 26, 0, 198, 0, 0, 0, 0, 0, 0, 0, 0, 0, 199, 24, 0, 0, 0, 0, 12, 0, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 200, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 201, 0, 23, 24, 25, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 202, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 203, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 204, 205, 1, 0, 1, 27, 0, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 4, 0, 0, 1, 207, 208, 13, 0, 0, 0, 0, 0, 0, 0, 0, 209, 67, 0, 0, 210, 0, 0, 211, 212, 0, 0, 213, 0, 0, 0, 214, 68, 0, 215, 0, 0, 0, 3, 0, 0, 69, 0, 0, 62, 0, 0, 28, 29, 0, 0, 3, 0, 0, 30, 0, 0, 216, 0, 217, 0, 0, 64, 218, 0, 28, 219, 0, 220, 221, 0, 0, 31, 29, 0, 222, 223, 0, 32, 224, 0, 0, 225, 226, 227, 228, 30, 229, 33, 230, 231, 232, 34, 233, 0, 234, 235, 6, 236, 237, 31, 0, 238, 239, 0, 0, 0, 0, 0, 70, 0, 2, 240, 0, 0, 0, 241, 0, 242, 35, 0, 0, 0, 243, 0, 244, 36, 0, 0, 37, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 38, 0, 0, 0, 0, 0, 14, 0, 245, 0, 246, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 40, 0, 0, 0, 0, 36, 0, 0, 0, 247, 0, 0, 0, 248, 249, 0, 0, 0, 0, 250, 0, 0, 251, 1, 0, 0, 0, 5, 2, 0, 37, 252, 0, 41, 0, 253, 0, 38, 254, 255, 39, 256, 0, 257, 0, 0, 0, 0, 0, 0, 258, 40, 259, 41, 0, 0, 0, 0, 0, 0, 260, 0, 261, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 262, 263, 0, 0, 264, 0, 7, 0, 0, 0, 0, 43, 0, 265, 266, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 267, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 268, 269, 270, 0, 0, 0, 0, 0, 0, 0, 271, 0, 0, 0, 0, 0, 8, 0, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 272, 0, 0, 0, 0, 2, 0, 273, 11, 3, 0, 274, 45, 12, 0, 0, 13, 0, 14, 5, 0, 0, 0, 0, 0, 0, 0, 275, 0, 0, 0, 10, 0, 0, 1, 0, 0, 2, 0, 276, 44, 0, 0, 0, 277, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 278, 0, 0, 279, 0, 0, 0, 0, 0, 280, 0, 0, 0, 46, 0, 0, 0, 47, 0, 281, 0, 0, 0, 46, 48, 0, 0, 0, 0, 0, 282, 283, 284, 0, 49, 285, 0, 286, 50, 51, 0, 0, 8, 287, 0, 2, 288, 289, 0, 0, 0, 0, 8, 52, 290, 291, 53, 292, 0, 0, 54, 0, 4, 293, 294, 0, 295, 0, 0, 0, 0, 0, 0, 0, 55, 0, 296, 297, 0, 0, 56, 0, 0, 57, 0, 24, 0, 0, 25, 5, 298, 6, 299, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 2, 0, 300, 301, 3, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 302, 0, 303, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 304, 0, 0, 0, 0, 0, 0, 305, 0, 0, 0, 7, 306, 0, 0, 0, 59, 0, 307, 0, 0, 308, 0, 0, 309, 310, 0, 47, 311, 0, 0, 0, 60, 65, 0, 0, 0, 312, 313, 61, 0, 62, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 314, 0, 8, 315, 0, 0, 0, 0, 0, 63, 0, 0, 0, 0, 66, 0, 0, 0, 2, 48, 0, 0, 316, 317, 318, 64, 0, 0, 0, 319, 0, 0, 0, 320, 321, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 0, 50, 51, 9, 322, 0, 52, 323, 53, 73, 0, 324, 54, 65, 0, 0, 0, 0, 0, 0, 0, 66, 0, 0, 325, 326, 0, 67, 0, 0, 327, 68, 69, 0, 55, 0, 328, 70, 329, 0, 71, 56, 330, 331, 72, 73, 0, 57, 0, 332, 333, 0, 58, 74, 334, 0, 59, 0, 0, 0, 75, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 335, 60, 336, 61, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 337, 338, 0, 339, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 340, 0, 0, 0, 0, 0, 0, 0, 0, 341, 0, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 62, 342, 343, 0, 0, 63, 344, 0, 64, 345, 0, 65, 346, 66, 0, 0, 76, 0, 0, 347, 348, 0, 0, 77, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 67, 349, 0, 68, 0, 0, 0, 0, 350, 351, 67, 0, 0, 0, 78, 0, 4, 5, 0, 0, 6, 0, 0, 0, 0, 3, 0, 0, 0, 352, 0, 353, 354, 0, 0, 0, 79, 0, 0, 80, 355, 0, 0, 0, 0, 0, 69, 0, 81, 0, 356, 0, 82, 70, 357, 0, 358, 359, 360, 83, 84, 0, 361, 71, 85, 362, 363, 364, 365, 0, 86, 0, 0, 0, 0, 366, 0, 0, 0, 0, 0, 0, 0, 72, 73, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 367, 1, 0, 0, 0, 6, 0, 368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 74, 0, 87, 88, 75, 0, 76, 369, 89, 77, 78, 370, 0, 371, 372, 0, 0, 373, 374, 0, 0, 0, 7, 0, 0, 79, 0, 80, 375, 68, 90, 0, 0, 0, 0, 0, 0, 7, 0, 16, 0, 376, 0, 0, 0, 377, 0, 378, 0, 0, 379, 0, 91, 0, 380, 381, 382, 0, 92, 383, 384, 385, 386, 93, 94, 0, 0, 0, 387, 0, 388, 389, 390, 0, 95, 96, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 97, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 391, 392, 0, 393, 0, 394, 395, 0, 0, 0, 0, 98, 99, 0, 0, 0, 396, 0, 0, 69, 70, 397, 0, 0, 0, 0, 0, 0, 100, 0, 101, 102, 398, 0, 103, 104, 0, 0, 0, 0, 81, 0, 0, 105, 0, 0, 0, 0, 82, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 399, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 106, 107, 0, 83, 108, 0, 84, 400, 401, 0, 0, 85, 0, 8, 0, 0, 0, 402, 0, 403, 0, 109, 0, 0, 86, 0, 404, 0, 0, 87, 0, 405, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 406, 0, 0, 0, 0, 407, 0, 408, 0, 88, 0, 409, 0, 89, 110, 111, 90, 0, 0, 112, 113, 0, 410, 0, 114, 411, 412, 0, 115, 413, 0, 0, 0, 0, 0, 414, 0, 0, 0, 0, 34, 116, 117, 0, 118, 415, 0, 416, 0, 0, 0, 119, 417, 0, 120, 121, 418, 0, 122, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 123, 124, 0, 125, 0, 0, 126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 0, 5, 6, 1, 1, 7, 6, 8, 9, 1, 2, 0, 1, 0, 0, 10, 1, 5, 0, 5, 0, 1, 7, 2, 11, 0, 12, 0, 13, 1, 1, 1, 6, 7, 0, 14, 15, 12, 3, 7, 0, 16, 2, 7, 12, 17, 8, 2, 7, 18, 16, 19, 3, 0, 20, 21, 22, 1, 1, 23, 19, 2, 24, 25, 1, 26, 0, 7, 8, 27, 16, 0, 28, 2, 16, 29, 30, 1, 4, 0, 0, 31, 32, 8, 3, 33, 34, 2, 1, 0, 1, 3, 10, 35, 36, 18, 37, 38, 0, 39, 40, 8, 41, 1, 42, 0, 1, 43, 44, 9, 6, 45, 4, 46, 47, 48, 4, 8, 1, 6, 49, 50, 51, 20, 1, 10, 0, 52, 6, 53, 54, 11, 5, 55, 56, 0, 57, 1, 20, 0, 58, 59, 60, 7, 61, 26, 62, 3, 63, 4, 64, 11, 65, 66, 67, 0, 0, 0, 23, 68, 69, 70, 71, 72, 0, 3, 73, 20, 0, 0, 74, 0, 75, 76, 7, 11, 0, 2, 77, 3, 0, 78, 0, 79, 1, 80, 1, 81, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 12, 0, 11, 90, 14, 4, 91, 92, 93, 94, 23, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 26, 6, 14, 2, 24, 16, 101, 8, 4, 102, 2, 1, 1, 103, 0, 8, 104, 105, 1, 106, 107, 108, 109, 110, 111, 10, 0, 112, 23, 16, 0, 0, 8, 5, 1, 113, 27, 2, 27, 16, 4, 9, 114, 5, 2, 11, 115, 29, 116, 117, 0, 0, 18, 29, 1, 118, 6, 1, 0, 7, 21, 0, 4, 119, 2, 14, 1, 0, 120, 121, 49, 18, 7, 3, 26, 122, 1, 9, 123, 124, 35, 125, 9, 126, 0, 6, 127, 128, 129, 130, 131, 132, 31, 32, 133, 134, 11, 10, 135, 38, 12, 11, 136, 137, 13, 0, 5, 13, 138, 139, 140, 10, 141, 6, 142, 143, 144, 39, 24, 145, 146, 147, 35, 148, 2, 7, 4, 149, 150, 0, 40, 151, 152, 0, 153, 0, 154, 41, 27, 42, 155, 156, 2, 157, 49, 7, 13, 158, 159, 14, 47, 160, 161, 162, 0, 163, 164, 26, 0, 165, 166, 26, 4, 1, 12, 167, 168, 169, 18, 170, 171, 15, 1, 172, 173, 174, 31, 6, 0, 51, 0, 0, 8, 175, 2, 29, 34, 11, 3, 0, 45, 1, 176, 14, 177, 178, 9, 8, 0, 179, 180, 181, 1, 182, 183, 24, 184, 29, 185, 41, 2, 0, 186, 187, 188, 38, 0, 15, 0, 1, 2, 189, 9, 31, 16, 190, 191, 2, 192, 193, 52, 194, 27, 195, 196, 197, 2, 0, 198, 199, 4, 200, 53, 3, 20, 201, 32, 18, 202, 203, 3, 204, 0, 21, 205, 57, 206, 207, 208, 1, 6, 209, 210, 211, 212, 213, 3 };

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
            final int compressedBytes = 1363;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtu5DYUfKSfB0zDC85gFjM7BtPI2sgJGMDLLPoIBnwRJk" +
                "CAIMgiR3D2WeQIBpID5Ag5Qo4QSS3JlETqS0mk/AqYthstuUWy" +
                "6n1K7J4n9gP8/Anyx3fcfL7od6C+0+ez/BseDPz2H8cLMH66Q/" +
                "3Xtx8uz5/VGdRHeG9++uPxl+//BAKB0IXM/qHvRVb9ova/0C9g" +
                "QAiATP/IzScJmf6Z/ia/sgeAe83xH/YV5Pq/UVzCezhL9ZD9uP" +
                "310dyR/hfjqT/+/kvx99io9Pd7pr9Trb9zpr/nTH8y09+FMSz0" +
                "93jVH5xJfwQCoQVR1h1VCVLVHxyyGHN9AfNnqjxMCXWg0ePgAY" +
                "IoQiAQCIR24rT69m5GEdeunZdd+zWb3kOI/Pl0S/4bIQKwgvzC" +
                "EkPBf1k+F7KtFxaFf2X7Jzep6oeXD4hl0GHdipY7zotg/mv/UN" +
                "r+Ye5fwKt/UfiHX0r/ECL0D+2p5nzk9EoKGkeCbq/tYEcZQn9u" +
                "/73Sz32hH/LfCXuYJ4omZrR+9/Lv7fhxcuXfc5V/6/dXa7x/d/" +
                "yqPX4V7/2LIPwXnv6x6b/CQf3X9BugHv+8TYvrYeHWD2OYgHTu" +
                "H1DlPYNCGGaO6S7KKsJzzLrXf4SA/uOB4tcsDPivI/ffROAf+P" +
                "w7h2wb/p3ojqD8/dbDxnUahKHrj9R/7FnQTfyDvbPogP+60f61" +
                "+frT7GswMieTyM5jOneBNALKF0AD5rn407k1Cy8fLvq5CLnZic" +
                "YoOOm+N8ivwlh7PbsRP5z+qxEjZu2Xh3+R+sdLobPutrN+0Fw/" +
                "Wa3fS3f9+v23FP1r8m9ShqkfyhznZv0K/JlUw4nenMa9EWpySK" +
                "P6dfDKZf4oarUr4MpizzVJsR8zVuHJIk6W5FT/+O9q9jGs/xz6" +
                "STlA6slFB8a0PjpkDzw7MPMdBx3qeDPUBoTSL7L6krB5JM58m9" +
                "USa7rxp6qOXQJn1WorxzB1sPMjKUSHGROMNhEXdmwXAipfkBF9" +
                "i2WCaiBoJA3b1L8EbvrTIQfrEZAMF5FxuU7VlDpbA2Fuk8LHBe" +
                "u+1duEx6NrPe0ihe67cL7jokRIXQYfnWRBBw2M7ATSleqXNGtC" +
                "Fs+loLTmkvWR0HSfiHYUYUDw5io8JH9ex4UTgtgRbpCLCePF+S" +
                "ygMuYgwZ02hczF4P7N+v7TTc/9J7r/8yaKG6pB1k56nv27bVkd" +
                "cv/ngfmnSX8j8g+fnX8Ik0D7lyn/p9gosaRktfb+yzAqFt6L3X" +
                "b/beJWxeD8xd4gzd2/vR//CeuCN3uPgxQNOuFrjzD/Ofevg3v/" +
                "uhy1f52PvTuYIiFVRJR5A10A+cKLor/Z6PMji+LP+M9fUPyJD9" +
                "3944/1/vGbYoGLJ3P2j09gemP/hLFjp7GjNloFUVx1xHguauIc" +
                "1ZcEAoHgzH/gzH+qSrMiRJyjKLlp23f06cZmp6fsSVCedhArSg" +
                "f4/KLVMx3BNNit/ucD8y+06Jv/aPW3LUwssyCGX4xj/ZfHRzID" +
                "lsbv19V0rH/O6Tz+Amymf7bb+F35K9d0Y/yw9vhj8k6jrl8G6m" +
                "fe+vyN2m5IYjz/Osda/0mky13jMjEKJQ1qlwhb1a1sRHUTGx+x" +
                "J9Yo95XLYO+8qH8M2n9u0nRSLCIQOlHA00Fo15Fjb1dS/TR/8p" +
                "Nr2clBiGv2xEb6k1b/aKL4/imzp4SlswVdpvSo+7f41v+NJY84" +
                "62/CKt2tA8G/P1COPJ/HpIIdwf1KWvr9edPPp7hKIBAIVH8R3i" +
                "Yjm1B2CVcWfJMsCD2nLSQQEtWMmHamP/6zUfFfN9V0/fyW9jSA" +
                "UX+FBPn90/E/I0QVEg==");
            
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
            final int compressedBytes = 1225;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtu3DAMpVylUIMsVCCLLhU0B+gRVCCLLHOEHkUpuuiiix" +
                "6lR+gReqTayWTsmZFsWRL1sfmANpnAskiKfCTlzwBAB8DhCDn+" +
                "yvp/ZvhFHf7A3z70QzRYIUwHDUJ6H6mRJOjWiHcuBHeKKsaVgR" +
                "2BpT2drltb7qGzefUjY9GHDScQrtOq09PI1RGDbH854S/jwV/V" +
                "L6nI7Dpy+F8cjaSgU5Mg0mqwFPvem5JfT6ym+ZlnbA+yMB9IJ6" +
                "FpIBBazqiEKd/zgJqQQNg0+Ex/U2tkSHdTZiHQY8nFo+u3R3f9" +
                "Bsj1W1H+stSvt7763/nVv9j2c/bvvp52C1di2uWdeFzXHyktxa" +
                "PmF/YzxfQnEAIjR87EL/f1X55HWKqhN2e/OvJH1frrZPqzWv2n" +
                "XP4mJCnYC1//sa+lIvsv2v9zHzKij55n+Mk780nCe1BM3w+me+" +
                "hPqzv+xBhc33D9TnUSPsI9qIf+x9Wvb+bmzzl/qXT8LSm2fPmX" +
                "+K/K+qc77eCRHFo4GfBqcUoEgqSeNprL/YuiPRpb51uAFTKp07" +
                "BfgiFnjyr+G60QaP+C7EfYkV925P8Uv9sv07jvURKpiFtz//WF" +
                "CF3k+Ar0XzIKD1pABo47pWnrFBdmiTD1tlXPq3/U/RfbuH+Erw" +
                "1qMY7UtutXejjiRf+nl78/D/r/eNO/X199cf24ElJROQq5hfm7" +
                "gvMnwiqBY/MnjS87PrZ+wKmf/OsfWn8aX7J+b33+vds/YnyS+5" +
                "/2HX/l+3caX+P+R0bk6V1Y0x1BYvvJeP68854s1l/VnAqFWtpk" +
                "OLM/2OwPB/sDwv5HrOet8Z9A+fHWf538Imj/SQEB2wun8QPT9T" +
                "t8GOqYE/+DOvyPkG39Z/hnhn+9xhMIqfJPSP734z8I5j+cCkpm" +
                "G9/8+gfUT2KVFHX7T9X902//57+1o3+yvP+KjYthk66T2dXH23" +
                "+Lvn8iwXixsnqX+P4Xef9IqftXRCL9+Tg/P5+f5SUFVPvF77+W" +
                "Xv/E+5cmI3/VnH+Y60gf/sqZP7aQ/4vaL3b9m65/5BCP8sCEY/" +
                "x/sGUSE5VmCIlQEX8q0Za8O3Rdtar/TX//p6o+duLfvzbYT3nb" +
                "j1e1f6lOPyAHq8m7tDnnTwAW4X+vGfzM/7664vc9vv+JFVoUrb" +
                "+OYaCAQCjZf8oZ/+cZxkPI+Chg7b8U278wyaXiZf1PhYghGqqz" +
                "S19/IJTqf6JKG7GOj1CLC708dZ3FTQXf31L6+aHmx8fmr9LPb5" +
                "ez3wau3yao30pf/9qv/1XMH03svxBQKzQxV+yYCiR38vfh/QH8" +
                "38jf0/cHPA787d8faWQ1OtT5/1rTA041WXh+Qja+kE1JW9v+RY" +
                "754xUX2ehQFZ4/GVlX5wsXew97/1o7+lq/shxM9o/1Xt2EpCJQ" +
                "fpUrbYfOz6ZsG/f+skz9S/7+axD6ZQNilB/j/XX7u39qW+tHIB" +
                "AI7fJnqveXLja9+fnfI3+Wrl+Mttjs7m0W59aBGqT6Yq9PZ9sT" +
                "hOsT2n+HA+XqiLZNmXF+AgElhhzNZ12SqpTqlWuW65Z/fn624v" +
                "n3Y1KRWPknMIuny/+36d9/17r/bDt+XP7Pbf4vT/2fEMxHWBeK" +
                "6MHgkBCrBFRhV9K/Qqr9Y4G7sP8BCyDJiA==");
            
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
            final int compressedBytes = 898;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXV1u3CAQxhaNUBVVVMoBiJSDTN76mCP0KLTqQ4/To/RItT" +
                "e7jl1jL7YxDPB9DyvvKhM8vwzjAQuxD2r4CAArGMG6OBWicf+1" +
                "FEL3n0qY6y9GtGZEQpcvzc/uH8vPggZCkheKKb3ZTA+ERQMRjE" +
                "zbcfmOFuKB/FfuDxjBlG2kh+a/3faj5+Pb3ePnq/8w8ufOP2Xo" +
                "GSoj/zsOfWIGnrv9i9Ptf8z/h8M1oy+9TDv+JzT1rB+OxQ+ss/" +
                "aFJj2ytSZyPM83I1VxlRQwfsq4bjkbNc34da8/6ph/S19/wf4B" +
                "tvZLZ9YPMFHAoaOPn0VAaqF/zD9ADaCxiR71ezwaTJtOtVBQVg" +
                "ojV/5LfRnokv++XX7/0ee/v275r+1cNuj6bZo/q22UAftfRvRq" +
                "Tn/hv3nn/1tPr2A/AOAX63WKsVOODwBT+DT3GT+ikNZspl8Iis" +
                "rYfsB/7AXPf/nj96jPPwI8f1GL9esYKzUvIVC54ztg8/bSzG9/" +
                "20J0yf/Msv/pwf/QtgwAYUI0AABASUB9GQCAypMg5H8AAPAF7/" +
                "5fxE/WllPo/olyHlYZb/21d/SnIuov3eLR5spCtZ2JlHh87J+B" +
                "fTBcPx6NP6nrd7WPz8uaIH/YT8z5UzZD5B6fUWDmx2FwuH+17G" +
                "8u7lpdvP6Q/wAAEBmNR7DhE0+G+ou95WXmybd+9pwyfofmXyTm" +
                "X/nly3T7ZcP8xT/jLruR04gqIfnol2C/QKlOtv7863Vp/nrwjk" +
                "sGYr6DduHaCVRlls1sj6ldTxSAsR6HhQiKkb/aEGlQPwMAdlCV" +
                "08eRXzuJf9qRWBhHmKQI7S5M5G9gP/D/cvWf9vyGdQ9Ta/mU3S" +
                "1Bb3oAAE7E87Dys6K1SosHYRp6uWYbugtBb93ll0dBTd+z8fUW" +
                "KuRvso9/IL8UOHdakHdniBRt1v7j1/UsJOD+k+0dWQRfBBJmp0" +
                "X477Hzm++dfyz/fvj/mH5+/jGDhkwF+wGSzX8AwCP0Gdx/RJAr" +
                "m3WntiXsiTZr8bPd178Tbv+KwutFgGPz/wn903z759dqHiRyaf" +
                "Y6VrN5Ep9ubNpZtG475WmH8kgWw3/1/itdkzM5/dgG9l8e+zf4" +
                "poJlgrg6rd4UVDLJ001i+kT8FlOe4M9Iuv6LgutXXvuvlvi3rP" +
                "inI/oj5/2/Rnt/jBKc3x+TRfwhtv7voNfc8s9V+f8DhXaKJQ==");
            
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
            final int compressedBytes = 884;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuS2yAQbSjKpZoVizkAc4McgeyynCMpqRwgR8uRMpbHjj" +
                "3WBxDQDXpvMTVyVQvof6MWMkSWyNBAji5wpD//Vec/frpQv4hG" +
                "80KervDmRsEJs3P+M/SvofRvX+jHRP69khnm1jbR64/724ef/Z" +
                "V+BYpmSNa4ONxL837+nxfT2l4eaULl7wKkuG/8MHozQ28q6L8r" +
                "qL/ZrCif/UXZzzIGLvu3GeYfaX+s8vsTLj8fIT9gL+yiQvm6g6" +
                "+YpWk8f6iQf/iS6+eOX83Lz/LqL+R3IP1TR41kfienqtMDgCBP" +
                "0bj+jttpJWINAKDghU23nt7pyvW3n6uf/HmfYgqi79PvP8/10+" +
                "9r/fQRj3zp+m14rt+m8dVl/B/n8QeJwtQqReoAgLocAFDTgH/A" +
                "gVKFYfH5q+5fsqnPL7yP778okb93xn/9xL/v8/wnfxLEP1NlFC" +
                "XYNvVOwhX68f7Csk84b/wo13/EnT/4FPu/859eUP9FOfnF9W8F" +
                "9y9F909y5UFH77/pJooPS7riZrVLWxn2h/y/V/3biF428/io34" +
                "Ej21+G+O8ajP8S8hcHZ9Gs/03Xn9PO939OUBXgq5o7waYSuJmz" +
                "ubEzQNRAXVSvX4/RPw5LBpI05GIPI2V+/qcC6Udae383nB7633" +
                "X9jPxcZg2oD7x2AJCx/4HnfxLy4a33B8zf//K773+6vj8w+pmp" +
                "v92ynKXUzZ3v+k3w+t2y/tqb/gZum6/p+Qr/KJp/DEidP2Waf5" +
                "3x5yVoCegsfjXav1gRvunZ68a5qotZQWr/hetfaVLrf4ANOH8F" +
                "2PJHHRmtaXnm2D8Qw38H/kfW71g/7F960QU/KVd/JZz/zuk/uO" +
                "2/hfNPW7PfsX3/r+rOPyF/x+bzBvI9TCrpv2T4n773rxj8Z+SJ" +
                "Tun23/r3OwDUj3Ly7z73Pxz0h8Wri4MqtdLs+Sv3+ADAAXO87u" +
                "SMsCwCk3P+XF8+TdODMVhOPUK0AELVlvf8ctdb0goAWeNvdP9+" +
                "sf0HbJsdYv8CAACgkfgp7vxiAPKLBPaPG8m/+tDfdvWnQP+KlS" +
                "c/v82FwPffPOw/wSj6LXTTv3+wdf6nz3b+p2tTfyo61KjzJ4Fy" +
                "asKlqiOjsag94wvNn5B/I/6jND9A/vOMocA9vGAh7/v+92r9pa" +
                "iR9wc6MlU61rHEeb5fz8i/f3uO5SU=");
            
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
            final int compressedBytes = 760;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnV1O7DAMhdOqQuUKXQWJBXgkFnCX4CWwpFzEA8thiXQKlD" +
                "50ZvoTJ3ZyvocRjCbT2nXs46SUwG6kdzNO42twS3x9nmh4+ecA" +
                "AKB0uPLz3zy+2fbxzqz/hjP359fe0fc75FqaOYHHX5rXoZ52f2" +
                "ZfxN00In38cTSnW7CfHKgZzdd/xfzpr82fo+Phf/n8Rfb816TS" +
                "Pzr8v6MUekv5S06/YP5o6B/2x28a/ZbXf5Rdv9bef5arH0zRwA" +
                "Xa8m+c+Z/7+MoJEWVbyDwe3JgI7a3PtHCTbP7rN43kJf3F5y8Z" +
                "y9XL+P7/s/56+9FfwxziqPpr//kDsDO9MHxlWL8BcGD9gJOuHy" +
                "y0f7FUUHvz68LutiMN/pyK/cyIrxO+XxIC4ZJKaOX855T7b8VK" +
                "A0efPynXnzX7L03/fxS565/7/NP437ZUVDzPfuMvTPH3tDb+Tp" +
                "XoR3JgpZvgKlBV/Ub/J65feYP+8ZeDqqk2sSczs888PjdYOJdO" +
                "sIXuXXXGj4/145L7BxKs/0CD/rYb/xbi7/hxWK39Avp/0/rTfH" +
                "wQsp8Ux3+a+sti9ufe/7AQf7Xrj3TrFwT9AqLnP6wfArACX9/x" +
                "RfS7rzJ6KJ/UwfoXAGCiqPU3Mm7/xvWzHfc/SfffbNp+ef1jt/" +
                "7i/t8S9F/Z9SNT/uhi6evC4rxcMtw/E3jh0Amf/3spf9Pl+eOn" +
                "/F1M29lVfvzc+kl3/3HQ/qPPDzSvv0ix/sT+P/K3sv4J+//Qz0" +
                "BMcGceD4AUSvddK8nfhFJlEPvPT7Gu3E33v9b3r3Kffy2FAfVT" +
                "5/w5ymkqEsG1offuzlHDz5NprXsZfvz74LhxrXePP2Z27xwePq" +
                "4quG59YOD5mcgfNvNXDfsHaAo2o+z+A1x/XH8ArlQx6C8AQBn6" +
                "MWr9za9/WKz/wPOPI1Jh4bT+/BT0T3nnD/IP/Acigc3dIvpvAP" +
                "2B+pOmf1La/2L/IJF+xt+f1G0/gH7Jr9qP//84q/nX+vxH/ik/" +
                "fxPsl+MTy8fnNg==");
            
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
            final int rows = 31;
            final int cols = 16;
            final int compressedBytes = 32;
            final int uncompressedBytes = 1985;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjYBgFo4C2QGGA9TMMcftHuvtHwSgYBTgAAGZfAIE=");
            
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
            final int compressedBytes = 3967;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWwmUFMUZ/ququ6Z7FnUFNAhy39fKJYLKoiAIcsmKgAiIAi" +
                "IgKLdXRJZLUeRSEQQ1cimKgoAgh7ioPMB4hng89QUlBkMSLlEj" +
                "aiZ/HX3N9OzO7rC78J793nR3VVdXV9fX3/9/9VcNfRkIGGCDRf" +
                "vTB80xcA5kwgVwId0IVaEO1GWzSQW+n2yhTaAJNGWP2tWgFa0E" +
                "8yGbHYL2dCDvBd0hh74IfeAG8r3Z08hlo6yycDPcArfBCN4TGJ" +
                "kHZcyO5EdWizwD5eE8Op0+bfSCI5EVUM3YAL+we6AW7QX1yKPQ" +
                "wOpt/48shcZ0JTSDltCaTaL94Co6i15JnoPO0IXtI52hm92YLa" +
                "QTaW/2ONxIr4fB9HsYRsqTYywXMuBcZpnjyFwoC+XgD3A+PQqV" +
                "oDrUgJp0mtGJ3QmNyH7IosvML6EFXEIvopXt3+ByaEOeJd8Z10" +
                "I76BTZSmfCtdCTnoAF0Auuh34wABbCQFoFboXh5l+AgklbA4cI" +
                "OUwHMQpROIt/DWdDBfL36JN0MFQmnaAKOcE+jeyD2lAfGho55B" +
                "C5ghykA2gOeQwuguZwMRsEl/Lb4DJoC1dAB+gIV5v74BroyqZC" +
                "D4jBddAb+ppfQX+4CQbRNTAEhvLhtB5tROvDKKMP7R+xbMvoC7" +
                "fThrGY8ceY3ARWsRhidTGti6kj0a2xGK1EOsRiVkcsNdo6K+bb" +
                "rP9YnRCr8k6a98TSzTH/W1rLaECeUbmRznyZMVlep8Y49jMeX9" +
                "b398mo5q9PYCXv6GCuVDlma5m/EPOGssexdokVthCfyTbGEjZ6" +
                "NFDfnZjTDH/vmN/I589zrpjLvFLGW/LOE6Sj/14+UuztyvLaOl" +
                "37IHW0H5epS6KL6U+6dAfe0uqnz5+lQPfxJrJMY6/GCPfXb+5z" +
                "zsitOucr3Z7egM9GrDoY8xxeGXNgNNwBmbQjjCcTkFd3Q10yjl" +
                "SwF7LqfD5MgqYRfJLgFRkL2dFLYAyMs/tCd3xqV8Er3tE+Yd9P" +
                "7sYyglcTBVbA+ALZxnuwDZJXsVi0i9HLwFyoxn/F/kOsBK8w3U" +
                "C2cDmIpyCvsA/y+CfBvied5Vs2VCnFK5kvsNqOOec6JaGcwgp5" +
                "NVbwKmLy5YJXEqtl5neKV/x2LCl5hX1yLdwJnfDqTPmMh/DKBM" +
                "ErjUsVTA+XNZt0s4cVRPE9kVcCq4zBrCpUtvYIXsmStfGHvKL6" +
                "C+Sv0E6CV24bJa/wiLyy6wle4XkPcp+Dlcuru2CoPTTSzWcDu5" +
                "oDHBvIr1Q20OxLLoseNBbyx4QNpNlmP2jFr8Ias2kbaM8/xTPX" +
                "BmLf/mRPlW3QNtC+ARhtDmVoN/oYtrSdwCpi49kyU7xlNetegR" +
                "XUsmZ4WOFRY4U28DZhA3Uu2kB5vFfxysMqMguGSdYcEDZQX5E2" +
                "UGOlbaDglcAKsthQ87DASpdFrPRZO4WVsoGY1jYQzwbiD21g9D" +
                "VpA7cJG+hg5dhAxGqIwArzquj6pA3kK6jLX9pD2UC8dqmDld8G" +
                "Cqxo2zAbiNg1hj86WLE9bLfVGrF6AO7n+xCryVA3Mp90j8wzm2" +
                "IdyCvcT4FWcJ/ACn9jrL7RNohVLtursMq4MLrfxWqq4pX+orAP" +
                "IgsUrySvJwus8DgukiXvCMEKsX+P9ouFbOyogxX+Btsnde6x/H" +
                "ilbCA0stD6scXmkUSs+CrFK92rJ4K8kiUdXr0Z5BXukVfWAaw5" +
                "DivFq8jNbsvfTcYr3DtYfR3kFdrAaRKrlkY99FezYTqvYdeAh7" +
                "HsI7QhzEFezeKV4EFek1SAuQIr4a94ZbsO9vkT0kbXEvvoR/4+" +
                "zKgS/ZpX99n4njBTn80QvPK99RF9/IVXiSXd7LzwfF5V7qVl8W" +
                "wgvyBWwCb8lTy+YR6OpbHRPaGtFR69qj8HZkgv3Cwy2L2zMVyU" +
                "X828It4Vc7F6SNfzqNw/jXWdDc/CUlnrObI+6Q1gifYQFTQT6o" +
                "fVnVEjkLo9eiDw5J7wlPajBGvPdDVICw8r4a+SYvVekj5f6OsP" +
                "z18dSAWrKH5p7Newq1bzxDxYHEg9p3v8s7D7o9gqs3ug/CLZcz" +
                "mRIalhJXjlYYVnWo3Bn2Rv7nD8Fd+FvFrm+Cu7q/JXxmKlA3me" +
                "8Fd8r10HWsm7s6Ob0F/tycgW/spYJLXF7oy9GR39/krZQCiDWG" +
                "U6/oq/6/BK2ECl2R0byD8K+quouBLvr7rF20D8DZNYHYv3V7Ac" +
                "z7S/cmwg5mQZ+O6JNtBqqfwV5qC/kscEf4V74a++Uv5KlkF/JW" +
                "wgVBC84jsT/RVidWvQBob5K88GOpqdv6dsIOatEDYQXiSLjFWw" +
                "Ep6HF6LbUFusgdWw1njeWAGr4CWyBLXFU1EDy2lVLWygsZwxl0" +
                "mj43i2NcgrLL3S1agLfAp1kJublfw7i7ZPlVdKW6RqA401oSye" +
                "kLINPJnMBvKQNhg5vvMXCq7d0YG+d3wlkVfRbIdXUrPXkZr9BX" +
                "jV5dVhwStji9DsGZMEr5QOxL3klXzS3R6v8EzzSn63mR6vIiMV" +
                "r2RLkvPqlnBeue/g8ipcB8K6cF6Zn4fxytGBBfPKsAvDqwDKuQ" +
                "XzityXwKv1gldkPW8UbQtbYRPZzbOsc+B1eA22wGZjG2yQPf82" +
                "+VDYQLJXpnYQ/CLpFLIWz7fhbyd5y/c9fEC2k/fJy2Sdm7PBPf" +
                "sz2aj8FckLaovAF7Wn8H7ex6vcVHnFG8TS2szzk3rY+QUwMjeF" +
                "N0rM2ah4xQ46vOJZsC0yAXm1PTLezAVUNIBfCmXaX6Fmtx8QY2" +
                "Hlr4RmV7zCtk/3eOX6qzfkGTOn+p7p8WqixytPswd5lcJb3Rhv" +
                "A+M1u+TV2CCvFFahvPJpdplOotnNir48V7PLmncmavYAytMK1u" +
                "zsH7ouj1c7BK/4DnMoqUguxLNd/CAta1GJfoap8adn03I0KrDS" +
                "aUvucTxrIYa2VuTmg7Lu3aHfoMSKuhpQYWXxUMX6UVq8OlBivK" +
                "qWVHPvLODOaQXX7mHl2ptKilfWIMUrG8dW9DwRYxJYYR9UJVWg" +
                "roeVGgtTS/CK2pDNn4QxpLLild9fubyaqHilMc4M8krFmPy8Mu" +
                "eeOl6RuQXw6lCavKpRVF6p2G0BY+G22p4+5PLqLsUrJ25hz+bH" +
                "reO0nYhbkJoCKxG3wF6+yrOB8XEL+bzukBuK1VRtA+eH2UAVY/" +
                "JjJeLsJYZVujbw8uLEyvV9HlbTFFZ0rOuvfqBjKPKK1LZ+Ff6K" +
                "jkAdWIuOoyMdrKK9CepAOpyOgmw6Wvgr6xfpr1blg1Wov7J+O6" +
                "P91RXF6a9ce+r6K1I3yCu+Czk0waKIVX3hrxSvgjYw42phA0k9" +
                "YQPtfo62IA3M5xOxIg39WMXbQOGvSg+rSO00sRpWslgpXmlmNt" +
                "PHpva/4RE8NjdzSRPtaRbA3AJ9bdIRHsz088o3dzMxHd/OjoZq" +
                "i2OpaotIszS1xYgUdc+MRG2Rfzww7v5APJDIbwtmkxYydbGOFz" +
                "DTHQc48cDwzZLjI3N1YbESNvDUbIWNB0qsWqeJ1fiSxYr4bIyI" +
                "s8vjw9FWglcwB23gLCyDtoFclgKvXiw0VvzUYlU4zS5sYFpYTS" +
                "oVXq1n5Vgm6UKuIbv5cXwLGROLLkWsliSLW+AxLm5hvhQet2Dn" +
                "wlMKKxG30N9IXnIbmGbcohjGV+Fx9sicFO9eVDisVJw9liTOjk" +
                "cdD8Q3+IH0cOKBUlvUEZqd6dUTIh4oj17coj2iulrEA801aq4x" +
                "oAN1PNCc6sQDlbbwbGBiPDB+XlimQuaFU40Ham2REA9MqgNTjA" +
                "dGFofFA3U633ig0hb5xwNjOs6Ox7g4O6uI33OOiLPju1Qm12Hq" +
                "ehW3gJecuIUXZ/fiFoEWvJKPvYiLW7jf+Plnrg2MLC2y9ZxWlL" +
                "tIL7nvTfqTfqQvuYHcyHqwzhqRuBiTL/YYjtW6wmN1uvor1qXg" +
                "Z1qflDBWfeSbSlVABihe4dlNmIfeCDX7QFBeSPorh1fEnfkR/s" +
                "ptwau+dx0RhhWWD/irJG0qIX/FFqf3fdhflgqv1su9GBsHIq8O" +
                "r1LUFuuTaIuRDlbx2qJ0sUrXBtp/K2GsblZYOdqC7GYT2dh4bS" +
                "GwIiMEVkJbCKygFZtA1kI22QbtBVZSW2yQ625drBxtwcY72kJh" +
                "JbSFh1WitlBYFb+2ELxKR1tE55awttBzjUDIKDKG3OFghVckVn" +
                "is4+cVNHF4xR5WvFJYYbkcc2MirxRWglceVuG8glrhvArHKoxX" +
                "HlZublmsTa8vgOpBXkFWMl6BXmnlYKVTLlYyJWeNPKzwXK49ht" +
                "CYAdQP45WDlS7TIeEujZXbL7Ld5AF5zV0lAa+TybBF2MDgvLC+" +
                "R9tANt/xV9oGvqavx9lAz1+dXjaQLouV0lZEG3i/3E9DXmFvki" +
                "n6zZPwSoyv4rWFGF9pXm1ye+MJP68UVmp89Tuv0uCVVA9sNV7R" +
                "Y2FMIZcgk0xXcXYxh8/WOZo9fq7Rm7/CFmxOHAuTGeFxdl+phD" +
                "h7cCyM2nht8cTZrTXpxdkDZQJxdr+/YsvTi7P7/JWcwyf7yF/J" +
                "J+Rj8qmL4Rd+HYjpgD4lobPs5usFja+Ktll5KdiHzwpvA603zi" +
                "wbqFu/mW1ivpES20K+DWJFxyXeRUfhz7cy0NxSPFilZMuLgFW6" +
                "mr10sCK/kpPkZzz+xvUaAuKOr6KIAK/p5xWvnIRXvhWc/vXsqW" +
                "CV33r2ZDbQv57dwyr19ezW56crVrxiPq1/3y0lsWIf0MmSOagP" +
                "Mw4JrPy84uqfYlPieRV4WvXCtT5frPKKCasvSgurgtYH5ocVWU" +
                "91vMWJW9AlgespxS102cS4xYfutSJp9oK0RRCrFCy+g9WaUsNq" +
                "aRo2cL2jAx2sIJNukuMrqQN9Y+FJTtxCYCXjFmNU3AJLb0uMW4" +
                "j1FuxjRwfmF7fwdGAwbpHC2CqgA2VOSjowadzilOrAdNdbeDpQ" +
                "rY1h35CTYr0FkXPqdEd8PDBFHbg9aW/OLG5tEcTq9NIWp3YOnx" +
                "0PjK/+K3il5kT0+OrHlMZXb/rHV+yE4BV9P/3xVXHxKt11TCXN" +
                "K/qBt46J7o+zqhk+zV6O+f7BkmT+Kq94NLvdsnh4Zbc4w2JMMs" +
                "7OYs5cIz2geBXEyuNVvli9FeiRE4lYlcRc4+lnA08VVvQbpS0U" +
                "VvS7tOav3g7XgYZRkrHbQvCqzZmIlZoXpv9U88JGGXqoSOst3i" +
                "nIBv7Oq7RsoJwXNsob6AfpvzQPasbbwBTXW+wqHqzsIcXEq1vO" +
                "RH8l1pxR/a90npU4J4L41dHlmiT0kv43L+SYIf+8ip8Tyae3ay" +
                "XJL+ScCB4DcyJUr+1NnBNJtj7w9J5rhP8DX2B+XQ==");
            
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
            final int compressedBytes = 1624;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW3tsFEUY/2Y7s7d3J9hCSTSIqAhFSqkUFRQFRcWKIj6CIv" +
                "JGKWDUWiT4j69i1CgaExWFtmh8kKihwTRiQnxWDH0g1dgI4gOs" +
                "RojvYIM8rOt3s3t7+7r2dvfOvSU7yczONzs7nZ3ffr/5zdyUNA" +
                "EpOEHGQFoSKZyI8WQ4hdXidQSWbiOfYbqVtMHZeP2AbOJ1N2N8" +
                "Fy4lzeRjrHc9a+WlHeQ9spM0krd4S0thOZ0ky2w1YPtkB3lb5o" +
                "F8JJsCnJnMkVZD+fnRFzG9TLWuhKtkmwCLMVap+bhWOkCWhR41" +
                "f3qytKCa22OiDbJtgCnq9Vq4Tlc6F+M8jEu4JfBUhIh2vx9PT7" +
                "Jt8SyjzR7mpedivECrc7nlqRvgRpiVskltIqWL6RfGerSTt1mr" +
                "2bfJfQbW1su91XJOA9mdeV0FK7yul30KClYu37SJLlVzLYY2Va" +
                "xSfsUtg19h5H6FtdutfsVxrqJ7FKx68ytdb1qdv0HCr5xiFSvP" +
                "9An6VT5gRb/m6ZP4thSiIHHrKbQKkT3irBaGYr5E6C8MFGJ8TF" +
                "bBWLwjwQRMo4DsBnfz8muwBzvgJrhZN34LYZGwk+cKFKyEQm4V" +
                "wyBdrdN0+ZEYR6n5MmEjVMB5GeB0ixkrKNJyA9XrYKiBM2CYgh" +
                "WM5r15BcvPgfFqjQvhIjVXDZWG1lbCTJits5fxlBnqKKPTX7OH" +
                "YDyVj+YaGI75UiNWUA7jtLoTYTJcjNepcAWm0+BqTGfo2p4D82" +
                "EB9rcDkN1oPZlDZpNZwgFaRxusfoU1NmTwtXzqHwfm1q/ygwOF" +
                "LvVagy1M10rvSmFF3+AlK4TbTU8uE+7A9E5dD44GDKu5wcKKLO" +
                "Re02h4l8PY2hMKB6olh5IcqOLEuVKIGtsS44ZWuq1YKRyYL9oi" +
                "Nj+g2uIdo7Zgm51rC7FfGm2xJYlV7rSFK6xuDSJWtI2axkcEU4" +
                "12Oa+DK6yW+NVbscQDVt9aWhNN2uK7DHowL1jrK+GTgHLgPiMH" +
                "ilEXHFidhgP3euPA2D058quaQHJgl4UDF5j8KgMOFB8Mll9Fhg" +
                "fTr3CkH6c/0QOkhf7Mx32loFu9pfxKKEnvV2pdq1/t1+7ll7a4" +
                "3y+shDIvWEWKjBxIO8V6O6x648C0WP2Sn1j551dCpQdd0hApNr" +
                "FipzDV0PoKG+b8NeA68LEgYkX/oN30d3qQ/kMP0z/pX/RYpNtU" +
                "o8dg/ebqi3iTHqF/h/MVYjXDkw6UgcoySKRF5PwmjTbc13EgjE" +
                "1xIEwycmBi71bPgbCII/svFPAeTlc4MLF3m+JA696twoHSKMxX" +
                "ZPoGkmEGSO3daiWDMWp7tyms7PZuMW/67mGmwcpw7zZTrGAiRr" +
                "53y61pPLXZu5XGJPZuSRMbwArFI+bfRLIzX7Ei6QEv81Xs/b6x" +
                "crUf6JtmF4+5eUpKo7Mjl/B0ijbegyzfxkP/15vFmo+39ZVQ60" +
                "2zp3RgpAsxfNq5XxW0pfGrIaEONPWg3QtWbKg0TK/ZY+OyuL7a" +
                "kqfrK//8qiyrvFMplNppdqo75cFGBFuz+6gDPWCV0OyshB5kI5" +
                "Oa3VIjA80uLU/b/h716kqzx3b5rS2yfd7CHQcq5y34SN+rL49z" +
                "VUFf075by1kq+qqjv7Mx29+mtMrqV6Q4GH5FX/c0X00AKt2XWF" +
                "+pI5HF9RUbr6yvkvOV0/VV7FBu1lcJv/JnfWXU7M7WV5wDJ+n3" +
                "LdQR2J6eA51o9uT6Knf7Fm440Mf5ypVml9L+jiGUCtr5CfZI8r" +
                "yFoi3sz1tk0MdCOQ9CsLWFEqLqDMQeVVvUsEqdD1TtECsf9i1U" +
                "dNaQo8jfVaQn7Yz2jcH6XA5wCDhWz9A+1p9+no2Jl+cGK//Wwp" +
                "5Wkk3s2dQek819b7817vW2bxGvCLHS+dVz+exXxx8HevSsdb0g" +
                "uRbv1x1/WAWWAzewOraOvUBaWD17iT3PGrLHgWy9Nw4M/crkOy" +
                "/3gaaPfhWfHPqVDqlGwv/HI9TsQeBALZeL3/CbPerAtaFfpfWy" +
                "7fm1bxGvC7FyhF+Hj/NVU4iVI4+wOcvJvgw1u7/zFdud9flql8" +
                "f5amuIle049aotVMuEFaY6rNg+M1a6dtxh9WGIlXMdyL5341ee" +
                "sdoWYpUOK/ZDljnwR49YdYZYOcDSwIGY26TlLBzI0dlv71dYM4" +
                "/2mIKJFfwHMptNuA==");
            
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
            final int compressedBytes = 554;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt0rBFEYxr0zew52SyvE3+EGKfdCblFuxCWxpPifiCS1re" +
                "TjYkk+7iTXRBRZ39YYu2Oa0THs7qyddz1v7ew50zTbOb+e533P" +
                "OWsY3kFbdGBe43Z/zm4lMtdN59PinPZonhYV79mlZau17vFr2+" +
                "5+5NjwNfTx7Hc4ZpRhuFmJC29WtOovK7/jkxXPEJf2PCV/ZkVr" +
                "WVa0oGJF+2BVUpZXuXhg5o6SlXkfrAr1uKVCdCWuoasA6eoGuu" +
                "IT2qSC4C3PsXD3QPEgUqFTcUfJ0IXKA8Xjh65oR+WBtOHtgaEz" +
                "eKCPHvekr+gJx2jiv9GVNmp+xqCrYNYW+elKvEBXPurqlZ4zs5" +
                "T+dv5OXL1DzqNlzioNVmzZvSFfBZaNAV1xCUlgxaYONGsLqTtZ" +
                "ETNWdPRvdFUJXTElV2XVDhG7iqjR6rSwo6rIPKFV5/pmLQpWBd" +
                "MJQ1eMaEXBigmpWuwHsmFVD1aMaDXAA8slVLqSjWBVgpWkddYo" +
                "m3DWGHxWdqsI51eyGaz+NJ+1lM9YuOtKtss2L13JjgJ01Qpd+c" +
                "qqU/bI7iJ5YBdY+epxvajZWfPrc9Tsda4KPs+9W7AqGqt+sGK5" +
                "FlawkgNgVXI9DVpEcH7FR0sRp67kEDyQCTfVfuAwWJVifWXtB4" +
                "5gPzDwGcp1CqU/fvVA/T73fKWngpqvmFcTE//p/xbMWcXAig2r" +
                "SbBiw2oKrNiwmgYrNqxmwIoNq1mw4hIV7+XQkds=");
            
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
            final int compressedBytes = 544;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmjlLA0EUx33jzkSjiCB2amFtYSN+ATs7wUpbkSAqaKlRvC" +
                "rPziMK+QKRxKCFJohHEUU8emsFQdBKEDSum8OEJEZxN9ln/g92" +
                "dmZ22OL99v9m5s1SMBo3ikSzGJ3StV7u07nROiSfcffrV0i/ju" +
                "kkZewVhemStimQ6FFjyWcXtBuvHUVzGp1FYTlNjdOr4aW3nP67" +
                "TWvdwGdFY+UGK6bkJmJ3UZXoETWiTji/RogKo6z87ZtFLbz7Zz" +
                "qT0BUbVlNgxYbVNFixYTUDVmxYzYIVG1ZzYMXFKKhW1IJaUouf" +
                "eQvpVfNyy7y8hdxA3uIbnSz/arTrB2M28EXblLUnYxfbAa/YJg" +
                "b6pUeuyzWKyE3plauIgTZmFcB8xWO+0lntxP1kwZmI1g9WZuoq" +
                "WbPi/CoIVqbGwF3oig2rfawtmMxvB/lGaH3wUknMVyHoytQYGM" +
                "aanUuOST2mZSmGM/IWoxk9LjGol0P40m1H/wk+sNN8pd2pZ4po" +
                "D6av2e8RA800MZIrBqqXWAwUA4iB/38d6CiHrtiw0sCKDSsnWJ" +
                "nJylFtHSvZDlaFNEc9fFASMbABuiromn40i9Ya4ZeizFdN1ulK" +
                "e4eu2MTAZrBiw6oFrNiwagWrArKMs0q2fcmacY6Yysqg05bKKu" +
                "1NYGVvXXVCV2xYdYEVG1bdYGXq/qrHwv3VHlgVPW/RC78URVfj" +
                "+JeThznc+UbgX067WNkH1xx8Iw==");
            
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
            final int compressedBytes = 658;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnF1LFFEYx3t2zlsFUki3QvQl+gB11csXCPI2oRfNuugmtV" +
                "uzKCjfZcmwCBVD6kKM0BK2lKyLQsLrNSxRNHqxXbbTuK5rOum2" +
                "5+ycqf8DM3PO7mGX/f/mf57nzAxLg5lsUCKzSdBzeq33Q/TS7z" +
                "2lPv84oLdhvY3Ss7yxk/SEXlE/PVx9RV7OvTdBj7KtkUxg0IsM" +
                "IlidQdmgNa1fYcXjso53mmPF28AqOOSVQkaLKs0qbs9X3l6wMu" +
                "krq3PgHbAy6MNutoU+bBwqOZOvHsj7si/YV7K3CF/dg69KGbHa" +
                "Tdw4Bl3+uXyVgK/ssJLj3nnDdeAFsAp9DpyALiH4KkXL8p0+pn" +
                "Ov/D5iel3vjXO/YMrZGvut8U+c8obXet7QdnwVO6O3szjTIzEH" +
                "vocuoayvPuj1blLOUILNGq8Dk6gtSjrrzkIDl2p27auPNnzFZu" +
                "Arg675JOfy+94kaosI05yHBpFh9QUauJOvZFoctXQ9MIV8Ffb6" +
                "Su2DLmGEqlhrswNZPrtzpMpi5bFdeeSUv99ZMPE9ULpoUvtxXz" +
                "ha6yu/ZSFfqYPIVyZZqUO8nbfyFkrwDh7nzXjmzGFWh8VVcU00" +
                "rTwfKBrBqlQhrhc0usryHHgcrIz66oQ4Zmt9JY6AlUFWKVpWlb" +
                "gvbKXGPmmjDmRJdQrX2V0P1pE7C04HjOiCSpFZLZ+DBo74ap59" +
                "VtVsUdWwb2yBLbEfG0ak1/Xm/vJ7vrOvUNu6r2qhgSs1e7a2uI" +
                "jaIjKsLoFVBOa4xi0yDa6zO+Urv2XjGlMTfGWSlbppkdUNsDLK" +
                "6pa6/SdWqrmIfPUYrIyyarXoqxawMlhZtOEePmoL/0zAfyYYZ6" +
                "Xu/mKF9dV/7asesDIXO34CCVmTfA==");
            
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
            final int compressedBytes = 592;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk9LG0EYxp1kZnfm1mJR0G/QSykeaovY3qpUQe0n6K0W2o" +
                "sfQOmX6D8FD4K2aGlIIxI3iE0OW1vSUC/9BAqFoqjUeLDbdWNj" +
                "wropaWfiDPs8MJud3QmB97fPvO9klqS9UxHXO0ekQEr+MUs2gt" +
                "4aWQo+3/nN8dsHkq8Z+4XkSJG8Jak/V/h89d5nkjk9W/ciRT56" +
                "UHR0AlZ0iy8Ql36XzYpug5VMVvy1Ol/RX2AlT3SHv/GPe34r01" +
                "26zxdDI47rej/+8XeO6CGirVY8VfFV9clfqp45wTEf8kaNr+qu" +
                "w1eqWaXByph8VmiOFX8PVmrrQGU1ewa1hTGsVsCqpRkrixhoth" +
                "ZexVo45nOgA1ayWfHcCSv4yvh8lUcMdPEVO2QHfr4qRPmKleGr" +
                "WOSrDbCSyYoX1bGiy2BljK9KYCXVV1/VsWKXwcoYX22ClVRfHb" +
                "BX7AV7Tlw2zWbZMzYj0VcvwUoqq5+eZ09VfMVm7UmwapXsp82M" +
                "tsbFJfqX+NBPeKJ1zVfiijxfiXb4ShUr0Sk6MAfqy0p0WcPWkJ" +
                "o60LoHVq2U6EYMdJoD6Za4ij2ReK+FbVcFK3Et5r66Dl+ZwEr0" +
                "8LnofWHsX2lVPdxoeLcXEYpDvhI34SuZsr8lnbNeMvQ2YGIi/J" +
                "3EY789ubB54FbMa4s+1BbG5a7b+vsKiqR3BzEwhtVdxECXfCUG" +
                "GteBVv9//Hfbi3xlTM0+CFayWYlRRaxGwEo6q/uKWI2BlYL11Q" +
                "Osr8zLV3jfQuM68FEtK/FQ6hw4Dlby1PYbDH/gBw==");
            
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
            final int compressedBytes = 519;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmztLA0EUhb26j8wUWhiwsbARQbARH629Wqi/wSKgNlZW6W" +
                "x8YWlAEBEVMT6QKAYlmIhGkUQbsRaJ4I9w3YQoJmYh6kyyszkH" +
                "ZpPdnSbn49y5yU7oyMqLktkjm7AKRJd0bx+jdJs7i1E493pgjz" +
                "N7xCnxbW6azilFe3T4eYUFvu7dUST/7sJyFN1YkIPYeBlzplX4" +
                "JOT3Pi0qytWP+//L1QxyJZIVCxrzxqKxkGWlrxlz+qo4VnoIrJ" +
                "xlLP1qdsBmNWtum2HnXJm7f2dlboGV0FwtG8PGkJwaaAyClcRu" +
                "Y6X4Sn2J3qJ+0h5TcMt19ELwQBlWm/CgJnp29BZie4sdeaz0fr" +
                "BSJlf7YKUMqwhYiWalZdgxJbU30ay0V7ASul6dyMuV9g5WytTA" +
                "U7BShlUUrJRhFQMrZVjFwaqSKvXbLUvAF8/l6gq5UobVNVhVUi" +
                "wJD5Rer1LwxXM1MI0aWNEa+AAPaiJXj8iVSFbsSR4rPGt0QW/x" +
                "DF+UYfUCX9ywXjG/wPWqGTVQFivWYgaxn93NrHgj9rOrkyveJK" +
                "cPBCvxrLQMb8XeGGVYtYGVN3t23g5fqpirDuTKo7nqhC9u+C4s" +
                "Mle+deRKNCvfRpaV+BrIu8BKnHh34XlDupwaiP+gVi9XbERODe" +
                "Q9MnLFe2ua1ZikZ42jUlj11XjPPoCe3e2q+wBWRByS");
            
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
            final int compressedBytes = 443;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmbtKA0EUhj0yQ8y0+ki2KoI+gQQFbUS0UtDKF1CMNxC19Y" +
                "ZoIUrUGFwlXkpB6w0oClY26jqEGBRDtnB2ds/kP7DJZGeK8H/8" +
                "57IbBGHRPPL3nmoPENaDdlWHXJTzMkueXJIrck4u/9o/o1v9eU" +
                "CX5V852ih/b+vrUF+nlP9x9oaO6Jo2aef7jlyo7hVpr7I6qfNv" +
                "LkCkLqvOysqruf8vVqIfrEyyqq4iYKW6wMpcqO6wEyIDlRKTA3" +
                "tQr5j4qhe+cohmHzRoiN4igxzIhtUAWBntLQajY9WSBSujrIbA" +
                "CjlQ58BhsGLDahSsbEbN5+xj0MU5X43DV2xYTYBV7DlwEro456" +
                "sp+IoNq2mwij0HzkAXNqxmoYv9UKthJ/D+Kjn1Sq3hvTATX63D" +
                "Vw7R3IIGSe3Z023mcmC6FTmQzXyVAys2rI7BKvb5Kg9dnPNVAb" +
                "6K3Vfn0CWWWbioZyg/VSJPPJr2VcqHr6zOV1fQgHUOvIMuzvUW" +
                "98iBRuvVQ3SsxD5YWa1XT9CADatnaMCG1Qs0aIje4hX1ig2rN7" +
                "Biw+odrNiw+gAr06yErz6jeB4oSmBlLpq+ACzctsM=");
            
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
            final int compressedBytes = 408;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrtKA0EUhj2yMjELvoKNjyAEiY2deAUvjdHSp1DRRrC0tE" +
                "6jhREliCIJEk0RRdSnUPAhHJf1goVB0JllD/Md2PtW59t//n+G" +
                "lXrRvpd07A8lbXlM9hdym15dSi09HidbI9mu5Prbuw/SlHs5kp" +
                "PPO9Hr17M7Of04a9muJTeW+kfFvfQgHyV161FXsUFXalgVYOWa" +
                "VfQU90snenHuV8+wcskqLpIt1LAa8MeqrwQrNX41CKtMM/sQPc" +
                "hZthgmW6hhVYJV2H5VqMIqU78aoQdB5MAyulLDahRWHrLFGNlC" +
                "wbrFuDkwte66Mod/Z2X2YZVptpigB2pYTdKDILLFFGOgU7+a9j" +
                "gX3oOVGl3NwEoNq1lYqWE1Bys1rOZh5WHdYoF1CwU5cNGfrvg3" +
                "JuO58BI9CMKvKujKg18t41cK/GoFv2IMTMbANVipYbUOKw9+tY" +
                "FfBa6rTVh50NUWulLDahtWuV+V2PntjWiVLgXhV7voyqGuqugK" +
                "XaVfwhm6UsPqHFZqWDVgpYZVE1ZqWLVg5a563gDy0b6v");
            
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
            final int rows = 14;
            final int cols = 107;
            final int compressedBytes = 164;
            final int uncompressedBytes = 5993;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt100LgkAYBODm7P//GYYRXkKRSqEiMhRvQkHQMQm8b4todK" +
                "iTKQ3NwH7vaR/ew2Ju2mBj3gQxjrZfYNesIrjN6NkW2LbC+uVu" +
                "ihAHTDHrdpzkebaH386W5mOwNUqPOKne4DeCYesqU13RWOWyor" +
                "EqZEVjVcqKxuokKxqrs6xorC6yGvV/ddUb/EVd3VRXNFaVrGis" +
                "7rKisapl9b1MHnwJCjE=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 109;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MERABAMBED9F+3SgYePm7FbAXLEJDnZC5pFfgHQ//Q/9Q" +
                "EA8D9CfQDQX1D/T/LXvj/3n5f5kD/nI9+4P8gXAAAAAHQx/wIA" +
                "AAAAWplfAnifAQD8j6wfAAAAAAAAAAAAAAAAAK4MmwfGPw==");
            
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
                "eNrt3LENADAIBDH2HzpkAyoQiWQPAOV3FwEA0CtLZ/z+9H8AAA" +
                "AAAAAAAAAAAAAAAACA1+kvAGAfAAAAAAAAAAAAAAAAAAAAAICf" +
                "bPdR9FkoXaUA2CE=");
            
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
            final int rows = 640;
            final int cols = 8;
            final int compressedBytes = 50;
            final int uncompressedBytes = 20481;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0KEBAAAIA6D9f7RaTQYznEAC8NOnEgQAAAAAAAAAAAAAAA" +
                "AAAAAAAADbAPC/HuE=");
            
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
                "eNrt2TkOgzAARFF32UNCdg7kc/noOUJCB56naRHFfwJL0Er7sb" +
                "ppxfpY+eMa3ryNt/E23rZw77tOUd7jvHvWs66r9n7rFOW91SnK" +
                "e6dTlPdRpyjvg05R3i+doryvOkV5Dzo5v423deG91ynK+6lTlP" +
                "dFpyjvj05R3pNOSd5z5//3yp/vk05R3jedfG+xbr0fOkV5jzp5" +
                "nxtvW753+QIqxO/X");
            
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
