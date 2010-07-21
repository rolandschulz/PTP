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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 1, 47, 30, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 17, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 3, 0, 13, 4, 106, 47, 155, 156, 5, 157, 158, 13, 6, 26, 162, 178, 159, 7, 179, 8, 160, 161, 0, 163, 180, 168, 181, 169, 9, 10, 97, 182, 183, 184, 11, 171, 185, 47, 12, 172, 13, 186, 187, 188, 189, 190, 191, 192, 47, 47, 14, 193, 194, 15, 0, 16, 195, 196, 197, 198, 199, 200, 17, 201, 18, 19, 202, 203, 0, 20, 21, 204, 1, 205, 206, 74, 22, 2, 207, 208, 209, 210, 211, 23, 24, 25, 26, 212, 213, 178, 179, 214, 215, 216, 217, 27, 74, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 74, 228, 28, 229, 230, 231, 232, 233, 234, 235, 236, 237, 86, 105, 238, 29, 239, 240, 30, 241, 3, 242, 31, 243, 244, 245, 0, 1, 2, 246, 247, 248, 47, 32, 249, 250, 86, 251, 181, 180, 186, 149, 13, 185, 187, 188, 189, 190, 252, 191, 192, 253, 182, 4, 5, 96, 6, 254, 33, 255, 34, 256, 105, 257, 193, 258, 259, 260, 194, 105, 261, 262, 106, 107, 108, 112, 263, 115, 120, 122, 264, 183, 196, 265, 266, 267, 197, 200, 268, 269, 106, 270, 271, 272, 273, 7, 274, 8, 275, 276, 9, 10, 277, 0, 11, 35, 36, 37, 12, 1, 13, 0, 14, 15, 16, 17, 2, 278, 18, 19, 3, 13, 4, 20, 279, 280, 21, 281, 1, 282, 283, 284, 285, 286, 287, 288, 23, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 24, 27, 317, 318, 0, 319, 28, 29, 30, 31, 38, 39, 33, 40, 32, 177, 41, 42, 34, 320, 321, 322, 323, 36, 39, 324, 43, 44, 45, 46, 325, 40, 41, 42, 5, 47, 48, 326, 49, 50, 51, 52, 53, 54, 7, 327, 328, 55, 329, 0, 56, 330, 57, 58, 59, 60, 61, 331, 62, 63, 64, 332, 65, 66, 67, 333, 68, 69, 70, 334, 71, 335, 72, 73, 74, 75, 8, 336, 337, 338, 339, 76, 9, 340, 341, 342, 343, 344, 345, 346, 347, 77, 78, 10, 79, 80, 81, 348, 82, 11, 83, 84, 85, 349, 350, 87, 88, 89, 0, 351, 90, 12, 91, 92, 93, 13, 47, 352, 13, 353, 94, 95, 354, 96, 14, 98, 99, 15, 100, 101, 355, 356, 357, 358, 102, 103, 104, 21, 105, 107, 16, 359, 17, 108, 109, 360, 17, 361, 362, 3, 363, 4, 48, 110, 5, 364, 111, 365, 6, 366, 367, 112, 113, 368, 369, 370, 114, 18, 371, 372, 373, 374, 115, 116, 0, 49, 117, 118, 119, 120, 121, 375, 122, 19, 50, 51, 376, 377, 378, 123, 20, 379, 124, 380, 125, 126, 381, 127, 52, 128, 98, 129, 382, 383, 384, 385, 386, 1, 387, 388, 389, 390, 391, 392, 155, 130, 131, 132, 133, 22, 13, 134, 393, 394, 395, 396, 397, 398, 135, 53, 399, 400, 136, 401, 402, 403, 137, 404, 405, 406, 407, 138, 408, 2, 409, 410, 106, 139, 411, 412, 413, 414, 415, 416, 140, 417, 418, 419, 420, 141, 142, 421, 422, 423, 156, 143, 424, 425, 426, 427, 17, 198, 23, 428, 144, 429, 199, 430, 201, 431, 202, 204, 432, 206, 30, 145, 146, 147, 148, 24, 149, 433, 434, 435, 150, 436, 207, 437, 438, 0, 151, 54, 55, 124, 439, 126, 440, 152, 153, 441, 442, 17, 208, 443, 154, 444, 7, 22, 56, 25, 32, 445, 34, 446, 447, 448, 35, 153, 449, 37, 212, 57, 0, 3, 450, 451, 1, 2, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 166, 474, 475, 476, 477, 478, 479, 480, 481, 482, 43, 483, 44, 484, 485, 486, 45, 487, 488, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 25, 27, 28, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 46, 515, 516, 517, 518, 519, 520, 521, 55, 57, 66, 68, 522, 523, 524, 525, 69, 526, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 70, 566, 209, 567, 568, 569, 71, 216, 570, 571, 572, 217, 573, 84, 574, 85, 86, 575, 576, 155, 156, 577, 157, 578, 160, 579, 161, 580, 581, 7, 582, 222, 3, 583, 227, 87, 97, 106, 112, 584, 585, 4, 58, 586, 108, 115, 587, 158, 588, 589, 163, 590, 591, 592, 593, 594, 595, 596, 5, 597, 598, 599, 600, 601, 6, 602, 8, 9, 10, 11, 12, 13, 603, 604, 605, 606, 607, 116, 608, 124, 609, 125, 228, 109, 610, 164, 611, 165, 612, 613, 126, 614, 615, 616, 617, 14, 30, 618, 619, 620, 166, 621, 622, 167, 623, 624, 625, 626, 627, 235, 628, 127, 629, 630, 631, 632, 633, 634, 635, 636, 637, 128, 638, 639, 640, 641, 134, 642, 643, 135, 644, 645, 646, 647, 8, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 168, 170, 658, 171, 659, 127, 660, 172, 16, 661, 662, 663, 664, 665, 666, 667, 136, 137, 668, 669, 670, 138, 671, 139, 140, 141, 142, 672, 173, 143, 59, 4, 144, 149, 673, 674, 9, 675, 676, 677, 678, 679, 680, 681, 682, 683, 684, 685, 150, 17, 151, 152, 686, 153, 154, 174, 1, 159, 60, 162, 166, 167, 169, 170, 61, 173, 175, 176, 177, 178, 180, 182, 183, 687, 688, 689, 186, 690, 0, 691, 47, 31, 692, 693, 694, 163, 168, 171, 184, 192, 62, 195, 202, 63, 237, 695, 18, 696, 188, 190, 193, 194, 196, 197, 697, 698, 203, 699, 700, 204, 207, 206, 208, 210, 64, 701, 702, 703, 704, 211, 705, 706, 707, 708, 212, 10, 213, 19, 20, 709, 710, 175, 711, 176, 712, 713, 714, 715, 716, 33, 214, 65, 717, 718, 719, 720, 215, 217, 5, 721, 722, 723, 724, 725, 726, 238, 727, 218, 219, 66, 728, 240, 729, 730, 731, 732, 220, 7, 221, 222, 223, 224, 733, 734, 735, 225, 226, 227, 736, 228, 177, 68, 229, 230, 231, 232, 737, 233, 234, 235, 738, 236, 237, 238, 739, 8, 239, 240, 241, 178, 179, 69, 180, 181, 740, 70, 128, 71, 74, 75, 76, 741, 742, 243, 743, 182, 744, 242, 243, 244, 745, 746, 183, 185, 747, 748, 749, 188, 750, 751, 21, 752, 23, 189, 753, 193, 754, 755, 756, 757, 77, 245, 246, 758, 78, 30, 27, 79, 80, 28, 29, 81, 31, 82, 33, 759, 247, 248, 249, 760, 761, 194, 762, 250, 763, 195, 764, 74, 92, 34, 251, 252, 35, 248, 96, 196, 765, 36, 766, 197, 767, 37, 253, 255, 2, 38, 256, 83, 257, 258, 39, 259, 768, 260, 769, 770, 771, 1, 772, 250, 773, 774, 261, 36, 198, 775, 776, 199, 254, 255, 777, 778, 779, 780, 262, 263, 264, 200, 781, 782, 201, 257, 261, 783, 202, 784, 785, 204, 786, 787, 206, 84, 265, 266, 267, 39, 268, 269, 0, 207, 270, 271, 788, 789, 790, 272, 273, 274, 276, 277, 278, 286, 287, 40, 0, 290, 293, 1, 292, 298, 2, 306, 307, 40, 311, 312, 317, 320, 322, 323, 41, 324, 325, 326, 328, 330, 331, 332, 333, 334, 335, 337, 338, 339, 340, 341, 342, 343, 344, 1, 209, 346, 348, 349, 350, 351, 353, 354, 356, 357, 358, 359, 360, 361, 362, 313, 329, 41, 352, 85, 2, 42, 364, 367, 211, 791, 792, 43, 42, 793, 794, 795, 796, 797, 798, 799, 800, 801, 802, 368, 369, 803, 804, 805, 806, 807, 808, 809, 810, 811, 812, 813, 814, 815, 816, 817, 370, 371, 818, 372, 373, 375, 376, 377, 378, 379, 381, 382, 383, 384, 385, 386, 2, 819, 387, 388, 389, 390, 392, 393, 820, 394, 821, 822, 391, 395, 823, 824, 396, 398, 825, 399, 397, 47, 53, 54, 55, 66, 68, 69, 72, 73, 74, 75, 76, 77, 83, 826, 212, 0, 827, 400, 401, 828, 829, 86, 830, 402, 831, 832, 833, 213, 214, 403, 404, 215, 405, 260, 406, 408, 834, 835, 407, 409, 410, 411, 412, 413, 218, 836, 414, 415, 416, 417, 418, 419, 420, 105, 421, 423, 44, 424, 837, 838, 88, 426, 428, 430, 3, 219, 422, 425, 431, 432, 4, 433, 839, 436, 435, 220, 221, 437, 438, 439, 434, 440, 840, 441, 841, 442, 443, 444, 445, 446, 447, 448, 842, 222, 449, 450, 451, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 843, 223, 844, 845, 45, 463, 89, 464, 465, 90, 466, 467, 468, 469, 846, 847, 848, 470, 849, 471, 472, 473, 474, 850, 475, 851, 852, 224, 476, 477, 478, 853, 854, 855, 856, 225, 479, 480, 481, 482, 483, 3, 91, 92, 484, 857, 485, 858, 859, 860, 1, 4, 486, 487, 93, 86, 11, 488, 489, 87, 490, 491, 861, 492, 493, 262, 494, 495, 496, 263, 497, 498, 499, 500, 862, 501, 502, 5, 863, 864, 106, 46, 865, 866, 503, 504, 505, 867, 226, 868, 869, 229, 506, 870, 230, 3, 871, 872, 507, 508, 510, 511, 513, 88, 515, 516, 873, 874, 517, 518, 875, 876, 520, 877, 878, 231, 879, 519, 521, 12, 880, 881, 882, 883, 523, 884, 526, 94, 524, 525, 885, 527, 528, 95, 96, 99, 886, 232, 887, 529, 533, 264, 888, 534, 89, 889, 890, 891, 892, 233, 234, 235, 90, 236, 893, 894, 895, 546, 896, 897, 4, 898, 899, 900, 901, 91, 902, 100, 903, 904, 905, 548, 906, 5, 907, 908, 544, 909, 910, 92, 7, 911, 912, 913, 102, 914, 915, 916, 917, 239, 918, 93, 94, 919, 920, 241, 921, 557, 530, 531, 922, 923, 924, 925, 559, 926, 927, 103, 928, 0, 929, 930, 931, 104, 95, 97, 99, 105, 107, 108, 932, 113, 114, 117, 118, 933, 934, 100, 935, 47, 936, 937, 271, 938, 560, 532, 535, 536, 537, 538, 539, 274, 939, 119, 940, 941, 107, 48, 942, 49, 943, 5, 540, 561, 50, 543, 120, 551, 102, 541, 542, 108, 545, 944, 275, 242, 277, 553, 562, 563, 245, 247, 564, 945, 281, 946, 244, 947, 318, 246, 319, 948, 565, 949, 566, 567, 950, 568, 951, 569, 570, 571, 572, 573, 574, 577, 952, 249, 953, 253, 254, 954, 955, 956, 51, 549, 957, 958, 575, 959, 960, 961, 962, 963, 0, 964, 965, 966, 967, 968, 969, 970, 971, 121, 972, 973, 550, 974, 975, 976, 977, 978, 979, 980, 981, 576, 578, 579, 982, 554, 983, 580, 984, 582, 555, 558, 581, 583, 585, 985, 986, 987, 586, 587, 6, 7, 588, 590, 988, 591, 592, 256, 989, 990, 991, 258, 593, 992, 259, 993, 261, 994, 995, 594, 589, 996, 997, 998, 105, 595, 596, 597, 598, 599, 600, 2, 999, 1000, 1001, 109, 52, 601, 603, 604, 605, 53, 606, 1002, 607, 609, 1003, 611, 1004, 1005, 54, 608, 1006, 265, 610, 1007, 1008, 612, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 266, 613, 1018, 1019, 1020, 1021, 614, 615, 1022, 616, 1023, 617, 122, 618, 619, 1024, 1025, 1026, 620, 621, 1027, 0, 1028, 1029, 1030, 8, 123, 129, 623, 622, 1031, 624, 130, 625, 1032, 626, 1, 1033, 627, 628, 629, 1034, 630, 267, 1035, 1036, 631, 632, 633, 1037, 131, 132, 1038, 268, 320, 1039, 634, 1040, 636, 1041, 635, 1042, 1043, 642, 637, 638, 1044, 1045, 133, 1046, 1047, 1048, 643, 639, 107, 9, 640, 641, 1049, 13, 1050, 645, 10, 1051, 1052, 1053, 1054, 1055, 269, 648, 145, 1056, 270, 1057, 271, 644, 1058, 646, 1059, 276, 647, 277, 278, 1060, 279, 146, 147, 649, 55, 650, 1061, 1062, 1063, 1064, 1065, 1066, 1067, 651, 1068, 652, 1069, 653, 280, 654, 290, 655, 1070, 656, 111, 1071, 1072, 11, 657, 658, 659, 660, 661, 1073, 1074, 662, 1075, 663, 664, 317, 665, 113, 1076, 1077, 12, 1078, 666, 668, 292, 1079, 293, 1080, 671, 1081, 1082, 148, 153, 1083, 154, 279, 673, 674, 1, 1084, 298, 1085, 1086, 114, 1087, 115, 1088, 306, 1089, 307, 1090, 56, 3, 4, 675, 676, 1091, 110, 57, 320, 1092, 321, 677, 1093, 9, 1094, 155, 678, 679, 1095, 1096, 688, 156, 324, 692, 685, 689, 691, 693, 694, 111, 326, 1097, 322, 116, 1098, 117, 1099, 118, 323, 680, 1100, 337, 324, 1101, 157, 1102, 1103, 683, 1104, 1105, 695, 686, 158, 58, 687, 159, 690, 325, 696, 59, 697, 160, 698, 699, 121, 700, 702, 703, 1106, 705, 706, 707, 1107, 708, 1108, 13, 14, 709, 15, 1109, 710, 1110, 711, 1111, 1112, 1113, 712, 16, 715, 17, 1114, 713, 714, 1115, 162, 716, 1116, 1117, 717, 718, 1118, 719, 327, 720, 721, 267, 722, 723, 1119, 1120, 1121, 724, 725, 726, 727, 2, 112, 60, 122, 728, 729, 730, 1122, 1123, 1124, 1125, 1126, 1127, 731, 732, 1128, 734, 733, 1129, 326, 61, 62, 735, 737, 63, 1130, 280, 123, 124, 0, 125, 126, 739, 328, 1131, 1132, 1133, 166, 741, 742, 743, 1134, 744, 167, 1135, 1136, 745, 746, 747, 748, 749, 1137, 1138, 330, 334, 750, 751, 752, 340, 753, 8, 169, 754, 9, 10, 755, 756, 757, 758, 759, 1139, 760, 761, 1140, 762, 1141, 763, 1142, 127, 764, 765, 1143, 170, 129, 1144, 1145, 1146, 331, 1147, 1148, 1149, 332, 333, 766, 335, 1150, 767, 769, 1151, 128, 1152, 1153, 772, 1154, 18, 336, 130, 1155, 1156, 773, 774, 775, 11, 1157, 1158, 1159, 19, 133, 338, 1160, 776, 777, 1161, 339, 172, 173, 174, 2, 340, 346, 1162, 348, 1163, 1164, 349, 1165, 1166, 134, 1167, 135, 1168, 1169, 1170, 1171, 1172, 282, 175, 770, 1173, 283, 114, 351, 64, 284, 1174, 768, 778, 771, 779, 780, 781, 1175, 784, 1176, 1177, 785, 786, 1178, 115, 65, 782, 783, 131, 1179, 132, 1180, 1181, 1182, 1183, 136, 1184, 787, 788, 353, 1185, 1186, 1187, 1188, 1189, 1190, 5, 15, 1191, 1192, 1193, 791, 1194, 1195, 1196, 798, 1197, 1198, 1199, 1200, 1201, 1202, 803, 816, 1203, 824, 354, 10, 822, 11, 12, 1204, 1205, 823, 825, 826, 20, 21, 176, 828, 1206, 177, 1207, 66, 827, 829, 831, 832, 833, 1208, 834, 835, 1209, 836, 838, 839, 1210, 1211, 1212, 285, 12, 178, 180, 1213, 840, 841, 13, 842, 843, 1214, 341, 1215, 355, 356, 13, 1216, 14, 1217, 1218, 844, 1219, 845, 846, 847, 848, 182, 1220, 357, 849, 1221, 1222, 137, 1223, 850, 15, 1224, 22, 851, 138, 1225, 1226, 1227, 1228, 1229, 358, 853, 16, 1230, 359, 139, 1231, 1232, 1233, 1234, 1235, 360, 854, 1236, 361, 1237, 362, 368, 1238, 1239, 369, 1240, 1241, 1242, 140, 141, 7, 8, 855, 856, 858, 861, 370, 862, 288, 1243, 1244, 371, 342, 1245, 1246, 343, 859, 14, 860, 183, 864, 1247, 67, 1248, 1249, 184, 186, 187, 1250, 1251, 191, 68, 865, 866, 1252, 0, 192, 868, 869, 1253, 1254, 870, 871, 1255, 1256, 1257, 1258, 872, 873, 879, 1259, 1260, 1261, 1262, 15, 880, 1263, 1264, 876, 875, 877, 1265, 1266, 1267, 881, 884, 885, 1268, 289, 195, 200, 886, 1269, 1270, 887, 889, 891, 892, 1271, 883, 344, 1272, 1273, 890, 1274, 897, 1275, 1276, 1277, 372, 131, 1278, 1279, 23, 376, 1280, 1281, 1282, 1283, 378, 379, 894, 373, 1284, 1285, 907, 1286, 1287, 1288, 1289, 380, 381, 896, 377, 1290, 1291, 201, 134, 1292, 1293, 1294, 1295, 1296, 291, 294, 295, 1297, 69, 383, 384, 296, 901, 898, 899, 900, 902, 903, 904, 1298, 202, 204, 386, 385, 387, 205, 1299, 1300, 1301, 142, 906, 1302, 1303, 1304, 1305, 1306, 908, 1307, 1308, 909, 16, 911, 910, 913, 914, 1309, 912, 915, 1310, 916, 388, 1311, 1312, 1313, 918, 919, 389, 922, 924, 926, 927, 390, 1314, 1315, 393, 401, 920, 392, 1316, 1317, 143, 1318, 928, 404, 921, 403, 1319, 1320, 144, 1321, 405, 923, 930, 931, 408, 1322, 297, 299, 1323, 934, 1324, 345, 935, 409, 30, 1325, 147, 148, 1326, 1327, 410, 936, 1328, 1, 1, 929, 932, 933, 1329, 937, 938, 939, 1330, 1331, 1332, 940, 941, 1333, 942, 943, 346, 1334, 944, 1335, 411, 1336, 1337, 149, 1338, 1339, 24, 1340, 150, 1341, 1342, 25, 135, 300, 301, 302, 427, 429, 945, 347, 1343, 151, 154, 155, 1344, 1345, 1346, 1347, 206, 156, 1348, 946, 1349, 947, 1350, 1351, 1352, 948, 949, 950, 951, 952, 953, 954, 207, 1353, 1354, 27, 437, 1355, 1356, 28, 439, 1357, 303, 1358, 304, 955, 1359, 1360, 1361, 208, 210, 956, 17, 214, 227, 1362, 251, 1363, 957, 1364, 958, 959, 960, 441, 1365, 1366, 445, 446, 1367, 1368, 447, 448, 252, 253, 254, 449, 450, 255, 961, 962, 963, 256, 257, 1369, 452, 1370, 1371, 453, 1372, 305, 451, 454, 455, 1373, 1374, 964, 966, 967, 1375, 1376, 1377, 1378, 1379 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 3, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 2, 20, 7, 21, 22, 23, 5, 6, 2, 24, 0, 25, 26, 27, 28, 7, 29, 27, 30, 31, 0, 32, 16, 0, 33, 18, 34, 0, 3, 12, 27, 35, 36, 36, 37, 38, 29, 39, 40, 0, 41, 42, 39, 43, 44, 44, 40, 1, 45, 46, 0, 47, 45, 48, 49, 50, 51, 22, 52, 50, 53, 54, 5, 55, 56, 0, 57, 58, 59, 2, 60, 6, 61, 62, 41, 6, 63, 51, 64, 65, 52, 63, 64, 66, 67, 68, 69, 65, 70, 66, 71, 44, 72, 73, 74, 75, 0, 76, 0, 77, 71, 78, 79, 80, 73, 81, 5, 81, 0, 82, 83, 6, 84, 85, 74, 86, 87, 88, 89, 90, 91, 92, 93, 94, 16, 89, 95, 95, 96, 97, 6, 96, 98, 99, 99, 100, 101, 10, 39, 2, 82, 0, 102, 39, 101, 103, 1, 104, 16, 3, 105, 106, 107, 108, 109, 0, 7, 110, 111, 112, 103, 113, 114, 115, 12, 116, 18, 117, 9, 118, 119, 120, 121, 122, 123, 124, 125, 0, 126, 1, 127, 45, 128, 129, 124, 130, 0, 131, 132, 0, 133, 134, 105, 135, 136, 137, 107, 2, 138, 114, 139, 140, 141, 142, 2, 143, 3, 144, 117, 0, 50, 120, 145, 146, 121, 4, 5, 147, 36, 0, 148 };

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
            final int compressedBytes = 2994;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb2PHLcV51KzAnNxIvog2VvOyQqyAVTYld3IGV2cLzc6CB" +
                "YSI0iTJilUupQB3sUITp0KFSoXKgS4859wMFQYqq4JEqjKn5KZ" +
                "2Zm9+SD5e+Tj7N7BY/us01uS7z2+9/jej+Tsmxt3Hr33dKluzo" +
                "/vfnft5aOzj5/sF9/88d/vmoc/3Cv2xZsHJf3b5R9K+uMB/b81" +
                "vWz//rfLPz+v6L95+ej1x08eJm3P7X/q8SfmT4iZGDy6+8vU+j" +
                "/72Z3P5selfQhx97v7J7fOri8fCqN+WzJRiMug3xv1+G77veHk" +
                "L8n8vH3w97++/3T5l+e/+Ofjl79/9afXv37yt8N/ffmfkv7268" +
                "/2tzI/2fFy8byan9nJrdfXl/u9+Znafnfd/2Xnb8fxRSiRiepn" +
                "85S/aNn8uajix8TyyW6wKjo/m8h21fV76fln0keP6f/K1s9V1y" +
                "93fe/5Z9au70V3ffeuXxf5m5M+qX0nmD8k35TxuZyB+SahkuU/" +
                "+gtd/jTVFMwp+RUlP3la83d3Cvlqltsk8Xr53+Low7WXFnKeic" +
                "ntv8k/1vnhrM4Pe/nH2we/bPKj48erTX5Ut9/kR239sVrLJy+V" +
                "/XLX33H+PnhmmS+/R3SUnyP692v6On+8X+ePG/osAR3WD0C/3P" +
                "5D2q/W7Q9D+EMPN75x8/u2vcs/tzW+q32XvlrTZZeO8lcuvc//" +
                "q5r/w4b/ryn5M2gf2r/o9C/i5BMp5bMmfQUdf/i+uz4M/HNWj1" +
                "8I/Bj4Ce34e1R/9DRve0B+tmu6bX578Sn9+CKeP2d+pCj5rTU/" +
                "AOsvyu8aM5Ai33hAz5RQey69q9TGXKceX4S0Z+evnfa2+dvbE7" +
                "f2VWk+8+Ny/LwQH32li2/uP5uZhy/utSz189u8m9+W9Hd8+S+i" +
                "E/JrVv97nzvlO73X5G++/Jmbf3fpqzW9v76GhtpgOojvUoQ9w/" +
                "7X60fm+2DmWCKkN/6bNr6FKWLEX0z/2q/oWVevjVhayX63hrZU" +
                "qsKIrAp/a0aUEYvq/8s6IKrqX9lj1Izalz/yi0/0ZiKD+pFKSq" +
                "0rieTaf15U/q3LT53V/hOvXyIdmSKeP6Aff3soPyk/SuGfymEf" +
                "a/nykXw5kQ76Z/oP3X40LdiE+i+tfea2T5v/tP6fUeQXPfkXNv" +
                "ux+WfbPxof2l+BfEr3fjlqgtMm/5kgfgbMT1A1QeDP7v9ZLP9o" +
                "fnH8CFxfsHxh7cn9KxfdH18E8K9k/Lk+CPxj7D+6V+Ir1/imv9" +
                "y70oDh+KH5F/Qvf3v6+uVcX/z+kzr/NA5f18KePwH9EPDfjIP/" +
                "cvHTDxZvpb4p5rf1dTn7VIiDF+cHItcrJf53e37HxOUXBT1ibm" +
                "n/YQHqn4Wr/tkWfy78s9Y/yaYd+QszP0yNX3Lx1SH+GdFehLUv" +
                "KPElvsqA86P7bA/iTzD/gfMP8VmwfnDPDyD+2fgyreoYWI0m0/" +
                "n4a+D8jRY1//oF5weMb0D9hNe/YHxTBOKnwoffWvDdoQhZhy4t" +
                "7XdKT4Bve/UfMX9h+aWIsm93fkuKvCH1Tcr10xf/uv6jyOMz8O" +
                "UaP0f4uoUuBvg7wr+9+DwXnyPg/5PSw/n7Ytvjv8PZH0H+j/Yv" +
                "LHTRpbP3Z5j7H13/09b8WSD8G9mvrDz4oGwxwieuCbi/SYhvMj" +
                "y+dkK8OmpiuJTdWkKR4z/oH44fG/0NrX9Y33PxATQ+l38UFqny" +
                "y7j54cq3FNKI2epXJ/8o/+pLs29mz1d6pX63zDNzbQvjizFMVi" +
                "S0LwL+6sffyfig7MygltT9qQ2+Uw5uw3c+WLzp0xdruhjSlZ0u" +
                "1eeipM9q7n5eKnlx3sp3u1mfzJqzYfykxVfYf6U/D4KG5EP8If" +
                "kR/xh/e2OdHxE6P256rb/bpYwx/CP52/7lqP8zEn9SHXrnly6/" +
                "jLLfVv44+xQU/QTKfzT2HwJ/mP89P/8O/SH/Q/Gl/Bxv/VmuG5" +
                "b5k8lMOa6RWulciVnJxk8t+wdm2L8hxldX+zPm+sSUX5z32w/y" +
                "M3L+oiP3LwWsP733C5Lvz4TWh3D/G+T3p4CeLP91CXIE9HMUpr" +
                "9QOtx/4+ZvU+fPwP/Q/jL0X6p/c+nT6A/5B8av0P5vy428ON8h" +
                "hvishd8iI/k/l79Kzqr+zWvhu/zkgXQZR9+cX7DjA3j8ovm09N" +
                "ID7F+H6Qfp97zffrN+UeXn4scT248KXB8MBZ+4PP4P8W90/mZZ" +
                "41+rA1HW/+IneUl/Zsr8rVj65jcg/kP8rbBu6uyWPvI/evst28" +
                "fU6zsfX5yC/4TxA55vjRk/cH51wvh1yewnoJBw5req9q+9fvzK" +
                "zGzcRR5cf0yPj6eaH1d9ua35t49PqN/89cmpOMmt9r1qJ//dmp" +
                "RVJWxeVFNciPt6ZvTpgsZ/nd/0z1f09AfowtG/2Y5/AHxF9Y6X" +
                "5pZLn9T6UtLqS/JDXf9JnXnWZ4QPgfYwLG3233z4TkaOD3b9Z+" +
                "78nycfPj+aJL/PouMjpk+6/qH7kQ7+ZHh+0eNPpspvbDzx8TvG" +
                "+p+6fpp8feXhn3j+xQT1bZh98/Ah5v7o5PgQE5+GygX2a70fEh" +
                "TfTjj7k9g/BudLyfF1k3+xztcQ+Dc9fKxZf1Q9voc/mYtNYyNU" +
                "1ejZxfm8IiJ+SSY9TfwZVTOp6tvQ9bWdf9R+t/6N7o/g86EF33" +
                "7J/ms5/4/xS/R+GeCfgfNnOPqxyaf9+Qkl/+tW9sP4AHvx84/f" +
                "b2O5/9rsj2SE9uj9DZT47hufa78E+XnnK5PmB6/i4lu4/RTp8U" +
                "VHe/x+SYA/NJaRO0pBfH/B3z/7/qxw5E+GxB/3fgqhPev9QWh+" +
                "Cfc7PlrjWyNlnq6ttpC9/M2OX/noO9h/oOsf7j9PvH5z72fQ75" +
                "+ocP4TnM9Jfz8i8H5I6vMtgfknQkXQ/OP5PeTxZ2VUW+zATj/Q" +
                "L2RJr/bv5eyTMmIYMxN5BRDnefkLrX8fnYz/+tvHv39G8PVHqs" +
                "8V8C97//TzdTKg/wA62J8mrH/e9pCOxufiF1PjI0w6IX87AfjK" +
                "BPlhOvmuDl3FrR/k+PBj1y8v/k/Wftf8c/evdj+/vPX3itPZ8T" +
                "Ua3yXSA+RTFkKC77fwPqP7JeP7Oyz82XU/R7n7133+wP0ykF/7" +
                "7+eQ9kcMM38JjR86KP4Mot/MUX946Z3HRXe1J93/q+9PDfXflw" +
                "/THfevrNm1N/479Gu/v3V16IpJ3/sR0un+j+8HOu3Xa3+E+70M" +
                "+1Vs+dH9xQT68dLTrU97oH6z1x8J6lNi/Ef1D5EenR8rWloTir" +
                "8Q81f3/ZiJ8SFy/szVj//x3F9KjZ+FzT+Un7c/wT4/xeUPtj/n" +
                "1pfe+HOgTxv8NxM1/isq/Lea/Ar/PY3B9133g6P274Px0VB8ev" +
                "Nn1/lcP70+33aTcf/YWj/Qyz4iPusNET79YP84Yu6/iBPlyw+G" +
                "/Vvqbwd/bf0pd1rfc+u3jULlJPglVz76++9kgP5t7++L3N/YFb" +
                "2g6Yee36Hvl4ik4/yM8v51Nn7rvt9MCl4h66vj/rN9fGzf5/77" +
                "09z6gBmft/L9wCHrVyA+ycdXC1784t5vSbb/NBGdW79w6ztMn7" +
                "p+5dVf3PmdXn+J7MMRf5Ltf3jvr8WfT+De7+9EuDj/RuPz9vfT" +
                "xM/8oqvB93ex27PP7zPjN/d8oXCfn56LRO83yELz46T1yeT9j+" +
                "/f6+r+vWOHyJr/u+mb/gfxu73f7x6fRqeNP46fmUgjXzL9uOQr" +
                "KMEZje+u/8kFFOt84XTn1xQv/ifwH7Q+eOn1+yer+5daStv3Mx" +
                "Hev8DqPw7/0uHmE28/fv0KKj7hohPif7BZMb5/LZj/LdOvOv+p" +
                "6aH5oQk9vwH3JyL8Q/S+6qIqsfLion1WZ4x5OzZ4P8uu8QPu/g" +
                "KVfkX8Z/DX+H5TjH4D6iu4f8OdH27/oD2hPrf6j2z958iRn5Hx" +
                "CRO3/jl06dSPCLR/Kl37wwJ6/w06fwH2l+D7cZj4LTu/Jb8fye" +
                "sfGSs+kvBheTnx34nv97PfjxG1/ysI8Z/Y7P/Xs80k");
            
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
            final int compressedBytes = 2854;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTuPHTUU9jizK2d5OaskbDkbUtwiBVShCZoEgiAN0YpI0F" +
                "FSUFKC5F2lCF2KFCmvUiCly0+4oBRoqzRIiIqfwszc58zY/o59" +
                "7LubKNPkca5fx/Z5fD4+Pv3m+oOrzydfXd45/unFhWcPZjd/2a" +
                "8ffv33JXP0z616Xwgl/J8SRpRSVIsfKiMO2j8nzf90hRF99cmN" +
                "v2tLQ4je+8HqO2WO7/SD6w8+fD757klb/vNnD17e/OVoUf6vtn" +
                "xuuuXTIf1LyN/5Z8L4E9b/3wF/xnTC+izXC61ctl+nWl/rarn8" +
                "jaTj/Re3f6V9fxa2/pU2urE2Vzj7v9f982LXvm76PyW2Xy/ad3" +
                "16+YfcXL+qX3FZid1BmSl1fhblpau8v34sH3n1Lz5n+QH/1Zj/" +
                "qH5A1326mdPVoH0xGn/A/mPtL9A/uL5qEfSZ0PKArmrLgteS3v" +
                "5yTxH/34SOj0nnyjdRlD79g8vXLPuAtD6t+sMkkf+z965/sXM8" +
                "UZeFuPHi9smV2e7kSBh1V7cD6/pXZx1/2355PDno2i+69vd77U" +
                "P5x2ufND+lR7z07Ie1wKCLp5D+z+2bO4v+/UqZH9Q/wF/YPtwf" +
                "us8A49pWcfL5z/n6sa7fYj5/slfeEGRwYZsXCRSLs35i+0B9xb" +
                "eftXzc/kln3842598iP6B/ELH/QuQ35g9bf1zqNlM5EiaPgPo2" +
                "NEVbAxcN0UmKXHbiZr5Y9ED1FcOWFtrP9P5XO+Vf5Gdo+ACaP1" +
                "g+xhDSTDqFLZH1U+Rrgv6TyytxYtdvU4/9oN2MMiP7MBLfmvpn" +
                "QdImqllBOxsaVgr9bevJmnZcO4TycetTk/VDfvusBlpTvquXMq" +
                "R14Q7ufzxfHLXcKTHd4j85NpJ9/Hv3xJV91fxo57j5SVWLT37W" +
                "9cPbjwtz9OjWvLwM55/OTA/BR3n+Qfb54epXpv/B909rgiwdi0" +
                "W6fa3F2P7TwfZ5NVq/VTJ8s+6MmGpc5EIIvuKq3yD/2as/pGpE" +
                "rm7Xp1yuz6ajuql0Nl+/gJ5/f2fGl7djH0Isyls/CT+h1V9E8y" +
                "93/dJR/3lfPzH0lPiuOYfje0vfGr1qV0AxvXjyQ/OX700jqh9O" +
                "9VR9ORGlSaJfYvHdt/PzZuivwPImwOXaCn5D9X/s30cH/0p9We" +
                "xca4yf4jMhDp++OhRVs8HEf9d2rpuN9Y8YIR31n/brP5jXLxb1" +
                "nzUd2f+ovFT3REMvWtdevN/o94NXS/vx2ty+vDOg3+/RYf+i7S" +
                "eaf0Pnn8w1Px1/XPVLdQr46+d/7v6P5zes/ST7kyFfufUf6qey" +
                "WX/t+bssPm3UtTGNK6grJaZV84809mvJsQ+59uNZl8f20dyvqE" +
                "xp2nUnhWrZ3zoa72ziL6qHH7wx41cz3vgQfybzgoWYzPlrpO7x" +
                "l6u/lbC0H4B/Qf0QiJ+PvldJ8CfG+elb+lnSFSoaiH+O6z+hnb" +
                "9E4qPR8c8b7XviS9j4IegoAf8G3/0wus3kZPm/qP0wfM5NDLVP" +
                "qs4+4fP3dfeP68j9UYnV+UqZEx+fgfHPMvNnCdkugo6qIUBNrF" +
                "9nwieUa4sYIP9o/nsrx4Q1frlKRB8YspvfVvC9mSP+uQqs39V/" +
                "MH5sfznimyuGfAjR3+dbfnHjd/nxv+aM6cz+Nf5Nw+Hpofix+c" +
                "fFqvn5Y9Oox3pSEfdfgv6/0f79WdO5/h8PX17416UI/kxG/zLl" +
                "+WYO+yud/MsTv7Vuv27je/+oVDPBN6rCXJntqrbQ3e4XhPLZ6a" +
                "sQngG+Ug4lkHTaT6qjr8rPKy8bP8Js0vc26HpNp+nXEsQ/RNPb" +
                "+DDH/Ka6H8y0L854fQSe/xU58BeP/sP4ArBv8P1pP/5Cwmcs/K" +
                "pLIv9W/pG047fIv6Ty34NvYP1UgvZL6vqhtR9QP+1+ftnfp5b6" +
                "Fa3/aiH/Knr/Za9+w+NfRPtp54/sXyeSP9Vy3zjPdyzzr9fzD8" +
                "4vVG9LVpvdMaSNhPXHmfqXwfisCcSHk+Hjkfg1H3/H+n9zvy32" +
                "n+rfT8+9/3LKXwnkn59OPb/XuzLu/N4WP95bH3h9esfPrb/uM8" +
                "IE48uU9V0C+Rm4PoLWP4VeuvVDDRieAN9C9lHJ8V/B+syt/0D/" +
                "V+c7elesz3fE6nwH2od8/CPi/gkjf4+g3G/TAfmZQH4V6P/VEf" +
                "3L7T8K4D8I+vjJ8+/IfwPkJ0k/+PLzcPMDYPuAd38S5SeC9gXI" +
                "T0DJP+PPD+EvT6R78u/477dA+ZHAfvP77/78ROz7syi/RKrx7V" +
                "nbj8sPIHr69+pvHX5yY4CfrPDThq4s+AoNX7Xej9xMAcK1n0j4" +
                "Q4T/g+bfp/8C9WfG+AICPka0f0PW50A/e/A/lP+Hmx+HVN43RD" +
                "B+Qn4aZn6eQPnipgfkv0mI/8L70ZnbZ/YPy1c//+PiW4cVVeu/" +
                "lgPFj+VjKdb5TeQwv0mcfOXmzzhX9ET8ifO/CPkvUH4dtH5Oov" +
                "J/QPvNXh6ef9j0F+n8Ayny3OfnLvuCp5/LPsvqDaqhuMhIvsD8" +
                "Fcz8GLnx7X7/KstvbfSA+VWCNf7c+Zlyz99W4ssY8dXnvX9nTY" +
                "fx38z1mRu/ij+/X/lnJ7T87S5Bnst/ouGTMH6Jn//eMZWKan/4" +
                "8UVSfBzNflUR6wPxD97/hPjl6n6s3Lzf6sIv0+FX/fUje/ZVwP" +
                "1WgB+R8Vt3/d4vnv82fDfn+wEp82fS7z/C+4fQ/n5l7b8a299+" +
                "+yobnWp/5bof1pc5wfn/oX+D6Lz8Ztz4OEz3349j87/Rz8or35" +
                "j350LrH+O/Mqf9ks7+dMoXFn4dnH/TUObqdceHBDn/aQJ8B9y/" +
                "49mv244fDsVn+Pod3B/D+tNbnvJ+gP/9gsz+B/N+G9v/4M4vuj" +
                "+z8u8c50ux5/P9860ylv98+U+dP3v/uvspx737Kc1E6NX9FDt9" +
                "fX+FO761hRV5vwPeL/GXh/c/aP0TsfcX4Prnrh/y/kf4qd0/6P" +
                "I7tfallrITgU/H9qUXVUX26SO//UG3b5H9HXs+WEt4PuG3v1B+" +
                "d2v99PNXbvuCeL6igH2jfIUZ8ctc/RdyfhDlX27rfUinfGSdPz" +
                "LPJxH/uPn/ufgA8q+znx+Q3x9KmV9qeP+zWZ/1en22m63q4g/r" +
                "yPxoIqB+Px3Hf97v6+dg/lDjW6gX6LKcT8XH59Pi/53jY7+fNY" +
                "hfKjfil+ok5yMx50f09Zvg/IvZ/7znk7n9Q/b7kln4F0gPyL+f" +
                "Gr8Io1v3Z9b4dFL8MCM+f8i/IhS/dLZPXZ88+3D5hb9PS7UPNc" +
                "KnmfdL4Abhvt9LLO+k8+4PBK4ft33ptAXA+7tc+Rln35D7T96f" +
                "jvWxDf5w/LPc799mWX9B/hl+H95bP3hfmOJ/IPzLNwXM97GQ/Y" +
                "nrh/KVWj5u/4Hx4fdjueWJ9o0OY2Oy8yPAP/z+Zsz5Cpo/PS4f" +
                "9P5FDH7jwDfZ+A8b3+Tej2LhnxBfsk7763P+jOK/8fuDIfKnwr" +
                "7SoNt7e873I5/eopRH708itwvGd/PwY258dVx+b3r53PjltuOj" +
                "C7IkNFnqL4L9Pyr+FKJ/tpl/MWR/xdRv049cfCppfkTDyb8f8H" +
                "5RlIGP4vcJ/pk/vjfZ+MLe16G//8Nrn8T/klGeuX5c8bnp7DuY" +
                "X6unXyz5O9K+TyFG7YOBov1NeL+B679y/WPhx+dBflJ+/gFu/g" +
                "BWeQc/1vkxmfGNufUXz3/j3s9JcL+HK5+s+B09fz/vfYE88XGD" +
                "hRL8viM3fmNd/2yOzxw8afGZ4uTKy93JPogP1THbAtN1MNt65b" +
                "T3fN0TX8f0nzA9Ef6ayf8f5yfUYfKL5z/y24/pX7r8luz42v8B" +
                "gs5xIg==");
            
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
            final int rows = 619;
            final int cols = 8;
            final int compressedBytes = 1556;
            final int uncompressedBytes = 19809;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW71uG0cQnlsfibWAIBtCSlieHBUs3KRyGgfnJAZiNzGECM" +
                "hDpEzpACvChcoULlQSLgK48yMQgYtAlZoAQao8SvZ4FMkjb/eb" +
                "3bkjjeQAUzJHOzs7Ozs/3+wRlRR+tuiWGI8Bf9hGV1v0+hkRDc" +
                "z6TxSZH4z7tJVcA8b45u/rR+98owKybtBt/Z1m82fNn7fPuQ/6" +
                "iyW9ub6AotrsI8f6b9L1mr+lXFGx/EZbGlc/J+6benI4nimfZ/" +
                "3s+ZcC2O1pyx3uzfWXAZsimn909u3gcqKPiR6+ezI9mQ8n52T1" +
                "U1MNLEfQPtXuUV1NmLl/Nx+fXXz2dvLj68HlT+++eXPx/tHL8/" +
                "LVsz8/sed/PHb8pfT29a8fOF6XIeUx9ofhv3KOz1IR+xcj3531" +
                "qdWe7JzPAL2pv98W+vt6qb9fFvoP22fb+Kb+7+Q/2pDfeOVvOT" +
                "/cR6Xtj7n70X7+kP4j94fA/rT4t4qQFzTcig8zlv/H5wOuT6h/" +
                "oF+0P9h/Iv0E6Sz/g57cv/xTc60cPS/MkLIviQprMyd6oalwn5" +
                "aRHyARIvOrHf3KxlfxJb+cjF9X8SWbnrwfTkaN+KIX2tEbqjKq" +
                "PZz49j88HtoniK/C8b/X62+Nr5lbP6LXjIo1z3zLsJB8mmWdaj" +
                "mFW0Nuoo4vyq+F8+d3IWnDEpbcbeNbQ5GP5a2fl98RyH8D9UfZ" +
                "omATwV84v95Ri4lan6Zpu/+dtceHWP54fmRc2TYzw6oku6kfEd" +
                "ujIzoZabeIwaVjUZT0xc+mfPXk18yeXz/miCU9/4B+9Nwr3xVH" +
                "Phw/ufpLzo+nofyAkf94VFX/7+b7s4tP306+O67G33tzMX/0cr" +
                "Qc/9e6vvD7bzgemNSpuarzB/exyB+ozh9okT9cwfiB5U9J79bC" +
                "fj7+W5ljGjxw+VX2lZP3+vaUCjPT9M+DwZntJn8Ozn/TnH9cz0" +
                "/L+RnyBZ8O+IvWR3QrxCduAb5UUsyTefNf2qovLXO8STzfs8j6" +
                "JNX/MOo3XgKlROT0/CvIn+2fvIFAmr9w8xvv+fDUdwVTP/Pm+B" +
                "X/gsW///hHKD+S+VeNlJ9FHdgsxb4D9bPSioyp2Dp6tQXjF05w" +
                "Y6t9G+SN+iCcX3vpSH7W+Lx5LAw7P4L9h6T6JIYO/W+/44X4/y" +
                "r+mqFqjb8efD/UveD0F9j4P7BvOf7s8x+WZX+9478Qn4b2w8yf" +
                "Ag4mjK+K8N8O8FUL8pt2utqwD9R/4Kxfcj4Pia8L9YftH64viC" +
                "/PN/G/bIH/NfFRcf9Hiv+G8VXcP5Thw3L8GPVPET0+5eoU3+oK" +
                "X4zCR013+KgwP2HkP6L+FL7fsZf8cA/1nac+k/cPuilv/flP8I" +
                "mtr7Lo/oDsfMbUzzpJfzL5ED6F8C2cH4fp0vnT8bUZNfvvlIav" +
                "IfwMzM/IDxv41v3V/YhYfAvha7Hyx+KzID711P9B5w+fT4AfCf" +
                "Ehln2i4J9L/G8ZrF8RvsLGX9LzH8XCb/z2MeXpN+3+Z0z/KJw4" +
                "es+faDzEP3j5Ux48vznT6srmhmXd+pe0+10Hri9x/Vj2en8Q1r" +
                "fY/iLuT7Xy/1/rt+/7tx3U1z3jt/D+kYwurY+4+IlJXV8pur/d" +
                "e/yU5m+914+b8xfM+khF9D9QfVqS5H5FdH/VcuyPf/9BGv/S+f" +
                "PyL3F8gP5bWB9x+AvqY2wf6PyC+kSsv/D5KRfxpdBuiQ+LzLr4" +
                "piuhny44tMeXsP+Ovj8ok5///lXAPkT3+2X5q7x/cdj8qP/+I3" +
                "o/4L++/pT+kOmZvsf8UXq/G/gndn+BpZ8CyFrs2KL4fjGgI/74" +
                "/rDM/lj5E+v8BkxB5L/D/gHi10L+jPuhovyPn7+m3c9jv3/FvX" +
                "8Vie8x8Scvf2n8PXh8EOJvH7p8EJ/sG5+A+AEbf1Ap9t13fID0" +
                "nt9PSfNPUfKz7E81bMp0Nl7cv026PxTTH5XGT2n/jVEfBvpnYv" +
                "z60P2JvueX4688/5iKLx78fktYfvj+FrBPROff3+jy/rHpbjw8" +
                "3+H864PHT/Yxf6/2nWL/3cvnja+QLosP4vjRe/4v45/mn7ujQ3" +
                "wB5q/94lfS/ZGuD67/X+MSuIE=");
            
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
            final int cols = 75;
            final int compressedBytes = 4635;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVWw2wVdV1vjVt0iQGJinoyFAbY9K0iWCbtCbFRPa7990RbZ" +
                "Om+NPGMkGN6CAFwSAgPOCcs+/fu+/BqMhfRCIIGGNsx0xNpzFj" +
                "BKLJJBFlVCSKjeI/WBsTbGc6bdq19zrrrG/ffU46fWv2Omt9a6" +
                "1vrbvvOfeddy40TzRPjF7fJJ2eZqf0HneWE9tLx5onui+mM8k+" +
                "xlh2hkRb5zZP1GpUeRr7tZrzshvS82q1sd/19b9Tq2VPSNSelt" +
                "7THuMaX/+A08lQ+vNajWslU7hUarWhXyRb0Ee7VkumSB2ubNZg" +
                "hdN2mrDbV53VecB+2HuvV/e3p9hTOWZmmVn2U17XzazRg87y3l" +
                "9kK82s1gtpg+xjjHG2j36Oj2NbCuQvzaz2evs39pNjW73/x/ac" +
                "1tE89md2xmiSPSW5xPt8HvmyPd9+3v5VwXIxrUvtX2sm+Z8Y+r" +
                "e0B/55YM+0Q/7opmzS+nNaX6B1UfYkZP2J1582szr/aM+1n/He" +
                "q/az1nQ79mPeex2yZ9kLgv6z7SX5q89MZnd5/XV7Rz9xlhN6Z9" +
                "abrH2mvYsixwSTqGah13zefk0wu93+ffd7jNtv2N212vD7NZP2" +
                "Kq+3Oyl+D/IRdl/YpXt/OlrVkz2uI6Y9+fFe7GZ3SE53xJ01Hv" +
                "Pn1ehs+0feex37hx3sN+0/5LEFZoFqttjLDpgFvWsQxWgZUr+W" +
                "ryj2aZJ/kZh9u1Zrj0uuWdA5UyJaoTOoz1jv3LSPPtrh7Lrq1w" +
                "5WCLfO537s31Zxiti37C/znJ1mp2iftZOF9uqY2dl50yGCalTE" +
                "LkSvdbtm+736mdS5vepM18zObokgP1s4BUuvma5TL4zqKwhXa1" +
                "t5jrAXezUXOcv6+73i2LgZp2vQaboG2fdeLztuxnv30zU4LqhG" +
                "RUKkPp+uwRxz12D/mzmXvwY7F2hmd4LU0zU4Ttcg8PlrMOjSey" +
                "PdWdWTPa4jpj358d76fM2ha7DI8TsxLhb1egY5JQc7+GuQY9eZ" +
                "61SzxV72r3TcjShGy5D6Nfks3qfjbonVauk9Q5+SXMerEanQGd" +
                "RnbPQ8yQ97aq5Mqat+zWCF17txPp2xjLOkx3wz3672OqXVdZb3" +
                "enaqmd970C4w8zmLsM0SFaQzRZD0AMW3+HdqBWfQ3cNv5HXrbW" +
                "Jb7VWSa+Z3T80jbbvBzwK8dkR9xuqH05fVt2MYy1/BKK0+rbW0" +
                "bnF4eyVm+Zw1nG873luen1cv4OtBzkGfdNu0RXu0zUITnmHa/Y" +
                "86RFCNahZ6nY5m+706SbL8vcxszez+qUaQhb3BPqM3pMereuor" +
                "CJebPs7Jdgq7nFetdyJnWX+tGJ42PE20+3EWe/aM4Wn92Q4RVK" +
                "OahV7nbs1GNraHPq2Zo1s0gizsDfYZPZ6dVNVTX0G47AfLcniv" +
                "dCbaqzORs6y/VpgtZotov4NbWOid+bDZ0r/YIYJqVLPQ6/yXZi" +
                "NbvlcnNHN0m0aQhb3BPvWns3dU9dRXEC43fZyTn1fFTLRX05Cz" +
                "rL9WmLlmrmq22Kufaeb2r0cUo2VI78J8h7wvltidiyTXzB39Dk" +
                "ZCFvTzSQ5lp6GPdji7Ljd9WOF0vlfQufWJKs6SHgvNQtVssVef" +
                "ZRb2H0QUo2VI78b8lXtfLLGH3pZcs3D0EYyELOgz1n88+xD6aI" +
                "ez63LThxXCjfPRXs2s4ox7JPuTffZL6TeSm/mvxvT3ksfT8eQQ" +
                "XfFnJYf7z1LGM7RepAhdsdlLZP/C/8V6Ijvqj/+Z1tLfpOj09L" +
                "dHv0rHs2m9L3V/Ob9YC36Gfunzn09eSI42X0teSl5jPNnk+74j" +
                "/a0WfTqm72U0nZCekp6aPJTsTc/qP5V9xGceTJ7Ia56m9Vz2cn" +
                "Kr+9s5OUbTziHrP7Sb/Xj6Hs8z2ef/IPkR6cc03rk436t6erdg" +
                "yVtitfMpko3pSZ5lYvp+2rGtZqtov4NbWajb2WZr/zmHCKpREb" +
                "pvB4/rY7Z8uqs0sz8prpAOOAXL2MRsJnYJe2odLju9PEfYi/Pq" +
                "i8hZ1l8rTNM06f7K6ZRW11ne6zWPmmb/Erq/anIWYZslKgjdX+" +
                "UI3V81i/srn+F7cJ2/v3J8Iv3JeUTur4DX3181Veje42PZ+erT" +
                "/RXE8lcwSqtPay2tWxyO3fKcNZxP91fOy++vWnPw9SDnoE96h9" +
                "kh2qM7WOiKP8Xs6L/lEEE1KkLnFXhcH7Pl59UmzewejCukA07B" +
                "Un8quwC7hD21DpebvixH2IvzqoecZf21wj/NWt1alzxs09bNtu" +
                "v89lfcs776JP+UbkHzRLo6f1K2OXkjeVOenbnPq84UeUaWHqD4" +
                "lnSFO68Y0c8rPq869+lTtP5UftbnziviX5X8yj8BXO+7jKQrB5" +
                "/nZfRbKHk0f244ljxJtUdogpepdrl71mdHCe/TcS0db6FplvH0" +
                "6Qf02Z1d45GltuNnkvNqY/JK/tn0c87Vn+S/02u0nnasbuqq2W" +
                "LPXuY0ohgtQ4rfanWxJebPq2cl19T712IkZtFM9rN56IcxnF2X" +
                "/eJghU6InVvfruIs6bHMLBPt0WUstJc3MiZxParQNQgeZiMb24" +
                "09mtm/Ia6QDjiFoNnV6IUxrcPlpi/LEfZir/YjZ1l/rTAbzUbR" +
                "Ht3IQu/MFYxJXI8qIYLZyJafV/+jmf29cYXw4RSCZsuqe2odLn" +
                "t5eY6wF3v1I+Qs668Vpmu6oj3aZaFuVzImcT2q0HkFHmYjG9vd" +
                "SzSzvy+ukA44haDZcvTCmNbharXKc4S92Ks2cpb11wr8niP8Ri" +
                "Ubl881QcPvWx5yn8ML8Xsc6T/Ilv8dfVgZ+q/xZztWVH+PQ7Os" +
                "+/9+j+O6xd/jDM5Hd53vQc6q/vln+yQzSbTfwUksdF4tYkziel" +
                "SLzitAik/qAbb8vLpUcs2ksfMwEvKiLznZ7eiHMa3D1TaDFToh" +
                "dm4PIWdV/zy2xCxRzZaTxuTGZOcjqtFypHjlS8SWmP9snyy5Zs" +
                "nYvRiJWTST/Ww7+mEMZ9eF3TAnnI/26vIqzpIec8wc1Wyxl33N" +
                "aUQxWoYUr3yO2BLz1+AhyTVzxu7DSMyimexnd6AfxnB2XdgNc8" +
                "L56OoyVZwlPQ7/Osl2oRdnhwh7imV3Ce77Ie+dkvl/ceb5e6rj" +
                "yRRlKmfUumynTsk/2dcHOQdnFZ/0OrNOtEfXsdDn1e2MSVyPKi" +
                "FCne8rY8s/ry7XzLEfagRZ2BvsQ7zfqu6pdbjstvIcYS+uwR8j" +
                "Z1l/rTCLzCLVbDmxveGdzkdUo+VIcUUtElti/pnMVyXXLBr7CU" +
                "ZiFs1kP/su+mEMZ9flpg8rdELs3Byq4izpscvsEu3RXSzUbYfZ" +
                "lT3iEEE1qlmhp9nIlu/VHs2kazCqED6cQtDsB9U9tQ6Xmz7OyZ" +
                "+LFjMR8w+Rs6y/Vvi/lT/XnZvc3L60Vhu7Re4yGhMaE/D+qj3b" +
                "HZM39A6Ezt+LwjsvvBvLz/Av6F+ijQl4x6X3V/ysz/ljG4S9fU" +
                "l4h5P9ePC+jRc/60NUu+FdX3h/hT+dq7tfwr+d258P/nreGNxf" +
                "rTQrRfsdXMlC78wdjElcjyohgtnIlp9Xt8V1IT9bOIWg2WPVPb" +
                "UOl5u+LEfYi706EM4T99cKs9lsFu3RzSz06bjPbM7cWblZUI1q" +
                "Vuhptj+/71Kc3rE1mknXYFQhfDiFoNlPq3tqHa726rKc/BosZi" +
                "LmZ5CzrL9WmIZpqGaLvearTiOK0TKk+KRusO0YxKbfg/MkF+u1" +
                "Alk0k/3sy3GtxHB2XdJ78PXhfHwNVnGW9Nhmton26DYWOq8eZk" +
                "zielQJEcxGtvxedDyuC/nZwikEzf69uqfW4Yq7CTfOR+/gteE8" +
                "cX+tMIlJRHs0YaG9+ghjEtejSohgNrLl12A3rgv52cIpill+v7" +
                "qn1uFqd8pzhL3Yq4XhPHF/rTBXmitVs8Veve00ohgtQ4or6kqx" +
                "Jean6ksu1mOesmgm+/ajca3EcHZdbvqwQifEzt0VVZwlPVab1a" +
                "I9upqF5vsDxiSuR5UQwWxky6/Bsbgu5GcLpyhm+cPqnlqHK+4m" +
                "3Dgf/XY+OZwn7q8VZqaZqZotJ42JjYlmpn0QUY2WI8VZMlNsif" +
                "k7nomSi/WYpyyayb79XlwrMZxdV9zN6fz3IHTurq7iLOmxwqwQ" +
                "7dEVLDTfAcYkrkeVEOFsu2KQLf+NMyeuC/nZwimKWR6q7ql1uO" +
                "yj5TnCLt/jDL0vnCfuLz7pTWaTaI9uYqHfui8zJnE9qoQIZiMb" +
                "2/V3x3UhP1s4BUv93XZvdU+tw+WmL8sR9uK8+lY4T9xfK8xis1" +
                "g1W+zZnzqNKEbLkOKKWiy2xPxU/yS5WI95yqKZ7Nv9ca3EcHZd" +
                "9vBghU6InbsPVHGW9LjJ3CTaozex0DvTZ0zielQJEcxGtnyqR+" +
                "K6kJ8tnEJQ+/3qnlqHy01fliPsxV49FM4T99cKM2JGRHt0hIW6" +
                "vWVGmu47+hFBNSpiF6LH9TFb/nvwGGbGFYLjFDjLYC16wqIr7i" +
                "bcOB/t1enhPHF/rTCrzCrRHl3FQu/lQbPKPuEQQTWqWaGn2ciW" +
                "79XxuA4rBMcpilmerO6pdbjibsKN89VqvXeG88T9tcIsNUtFe3" +
                "QpC72XLzEmcT2qhAhmI1t+3/6duC7kZwunwFmqemodrvY/l+cI" +
                "e7FXHwrniftrhbnCXKGaLfbsIacRxSgf6RoEpPikvkJsifm9ek" +
                "5ykRHzlEUz2bdPx7USw9l1tY8MVuiE2Ll3WRVn3CPdnJ2bbqnV" +
                "kocHn5fZI/rMK5vRWub+T5z7dzJp8fu2tQifi2llOlxk/J3Gza" +
                "OtpbWKn+RXvu5d1OmDvm4F8jZPtM5qLUweLbKfpFVMl0zxFau0" +
                "onUDTf8sHW9MP5Adbs0ffMbXWtL6SrFXR5JXWsv5WZ+P5f+nqH" +
                "Vd63r372SwzpxvzlfNlpNGs9F0PqIaLUdCTrHEdnxxPeYpi2ay" +
                "b6+OayWGs+uKu+mE2Ln3WhVnSY/bzG2iPXobi+0N72NM4npUCR" +
                "HMRrb8s31fXBfys4VTCNr6bnVPrcMVdxNunI/26lg4T9xfK0zL" +
                "tER7tMXSOLlxMmMS16NKiGA2suXn1clxXcjPFk4haOv71T21Dl" +
                "fcTbhxPtqrN8N54v5aYeaZearZYm94v9OIYrQMKa6oeWJLzJ9X" +
                "+yUX6zFPWTST/dbDca3EcHZdcTedEDuPTqriLOlxp7lTtEfvZK" +
                "FuexmTuB5VQgSzkS3fq71xXcjPFk4haOux6p5ahyvuJtw4H+3V" +
                "peE8cX+tMGvNWtEeXctCn6fPMyZxPaqECGYjW24fjOtCfrZwCk" +
                "Hbp1T31Dpc9mflOcJe7NVl4Txxf60wy81y0R5dzlKr1T/JmMT1" +
                "qBIimI1s+V8TR+K6kJ8tnELQ5ivVPbUOl5u+LEfY4buJ5eErHO" +
                "yvFWaNWSPao2tY6J05ypjE9agSIpg9NKJsOfNP4rqQny2cgmVo" +
                "pL2guqfW4RoaKc8R9uK8ujucJ+6vFeYqkt2FZst79rj3CcmzIF" +
                "qB7M7/p6f3vS1ZLMJ9VXtEI5BXsGgmM2Et2kXt7nCFFZhTRHbn" +
                "e/Vc8HqQc8AnfaG5UDVb7Nk3jP9/W4pilI/0Nw4gxW818odWiS" +
                "X40F2Si4xagSya6fyhVe1+XCv1OLuuoVWDFTohdh59sYqzpMet" +
                "5lbRHr2VpXF643TGJO4wiYpoPsedz1nI5nC64zk9rgv52cIpBE" +
                "2PV/XUVxCuuJtws8cz1Wr9d4XzxP3FJ90xHdEe7bA0zmmcw5jE" +
                "HSZREc3nuPM5C9nser9X58R1IT9bOIWgnV1VPfUVhCvuJtzsuV" +
                "n9Xr03nCfuLz7pDWaDaI9uYLG9hmFM4oRtlqiI5vu4+7+bG1xd" +
                "yJbft5u4LuRnC6cQtHN/VU99BeGKuwk3ew2T79XZ4Txxf/Hpnm" +
                "368HTR/o5xOktjRmMGYxJ3mERFNJ/jzucsZMv3akZcF/KzhVMI" +
                "2vl2VU99BeGKuwk3e25Wv1efCeeJ+4tPO7bdbBftd3A7S2NqYy" +
                "pjEneYREXosx28xlSXyVnI5nCaHqolEvKzhVMI2jkU16InLLri" +
                "bsLNHs9E92EnhfPE/cUn3TM90R7tsdBeLmZM4npUCRHOtisG2f" +
                "J3c3FcF/KzhVMI2nm6uqfW4Yq7CTd78v1g/7PhPHF/8UkPm2HV" +
                "bLE3vMQMdz6OKEbLkOIOYFhsifm9WiK5WI95yqKZ7Hfejmslhr" +
                "Prirs5nX/vDJ37G6o44x61/wUulwA6");
            
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
            final int cols = 75;
            final int compressedBytes = 4368;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdW2uMVdUVvoLW1qptIbQJqUFJtCZ9aNMfaqN133u4VoIDpA" +
                "YrbSEmJAMxgmiLSkztOfce5t4ZUijYxpaKQ8GpSR/2T1tmYKCF" +
                "KlaQR4Wpj6q0xWdLjeIAQhW6915nnfXts/cZobOz99nrW996nH" +
                "X3ed17Rt2qbq1UVDbSzLTogugCIyNqEGqMCN/MogsMn1iVCtmS" +
                "neBFe+RxDiIzp9HybVmHuUv3o7FvkiinSqXnoTKfgRhz1BwZaU" +
                "ZS85gZEUVtCMn3fA7PWWfnu5iL9sgTL8I08pKX6+/7tmyPuUtf" +
                "cqBoIRli5J5tZT4DMSaryTLSzLRoQjRBTW7+F1GDUGNE+GYWTb" +
                "DrZwLJNkZmJ3jRHnmcg8jMab7n27IOc5fuRzNjYy1bUE6VytKx" +
                "ZT4DMXpUD48W7aGm8zuhetJegzAqWmG5krCriyuV1mcE1+geYb" +
                "Yu9i3YH2ZBrbq4dUl5TLHDXl0c4mS1ynPStRqPPkPxxUItVot5" +
                "tOhiarpWJwljvWyluQiyl7wq3jLPu3071z/NMAtGW1eXxxQ77P" +
                "oYDHLYe16rGW4+fnyxUN2qm0eLdlOrVNIKYayXrTQXQTZ6o/mk" +
                "h3w71z/NMAtGW7XymGKH3Y/GvjE/kcrjAydRCY8WTajpWp1BGO" +
                "tlK81FkI3eaN412rdz/dMMs2C0VS+PKXbYu0aFOewdapW4e1iM" +
                "D5xb1C0y0oyk2mVmRBS1ISSPfgvPWWfnQ8xFe+SJF2GSnI72bV" +
                "mHuUs32bsWkiFGxgq4PgMxZqvZMtKMpPpLZkQUtSEkjz6b56yz" +
                "873MRXvkiRdhkmxyKdqyDnOX7ltIhhgZK+D69GMU/+rDPEvPQn" +
                "zJ1+rDjYvigyQ1ehoDGrsxbNn4LUtLpkNOT1dO4a9xobWb4aKt" +
                "68ot4vEhND2zmNUH/cVv2chTnWyWoqTGqDE8skxS7fOEsV62ZY" +
                "jrU7xl833MRftiVI6JXJ3zat+WdWKH3WTvWkiGGBkrUB6ftvVh" +
                "U3ka6XOgpj+ZiYSxXrY0czX4CRa9ZWvjRbZmu7iKFuJXZLZuXV" +
                "+0FV083s2HenN/0UIylJxEGjk+bdVytZxHW8Hl1LR+mVpu70WX" +
                "MypaYbmSsNFbVquXhKnvRT0L9odZMKrvRUtjih12k73Pye5F85" +
                "xEKo8PHP0nI81Iqr9hRkST10Trbtk6i654zjrzV31QKd8eeeJF" +
                "KeRWKsnrvi3rMHfpJnvXQjLEyFgB12cgxm3qNhlpRlJ6mRkR1b" +
                "XKte6WrbPot/GcdbZWP2Yu2iNPvAiTZF0rz5Z1mLv09AtFC8kQ" +
                "I2MFXJ+BGFPUFBlpRlLt02ZEVNcq17pbts6iT+E562ytVjMX7Z" +
                "EnXoRJsq6VZ8s6zF26yd61kAwxMlbA9RmIcae6U0aakVQbb0ZE" +
                "GzNE627ZOot+J89ZZ2v1EHPRHnniRZgkN27ybVmHuUs32bsWki" +
                "FGxgq4PgMxxqlxPFp0HDW9ir9IGOvVOL2ucq27pVkeveAtq9VP" +
                "mIv2xagcE7l2XXm2rBM77OnlRQvJECNjBcrjZ7qVaiWPFl1JTX" +
                "8yFxDGetlKcxFko7fsOrjft3P90wyzYLR1X3lMscNusg9x2DvU" +
                "aqW7h8X4wJmr5spIM5LqB9Tcrs8iitoQQrGb95BsY8yVedc5zE" +
                "V75IkXYZLcdci3ZR3mLn3SwqKFGbN7Bi0178bYIZ+BGGvUGh4t" +
                "uoaaXsVfIoz1spXmIshGb1mtzvXtXP80wywYbW0ujyl22CfdEe" +
                "awd1hXa9w9LMYXTuNFude1Z4b8XjqpFe/GGxe5d7x6Dc2HZ6ct" +
                "Iz9vtf5VrpM7anoeLP61J7uZYM7h58Gu8071abCcU9xbtUqt4p" +
                "FlktIrCWO9bKW5CLLRG81r23071z/NMAtG21PKY4oddj8a+8b8" +
                "3AqE4wNnkVrEo0UXUdPR7iWM9bKV5iLIRm/ZdfCIb+f6pxlmwW" +
                "h7fnlMscNusg9x2DvUapG7h8X4whnh2fnL+oh4+NSenfEoFm+N" +
                "R/DZufqu8BvrTu/ZufHc6T47p1eFnp3p3P5/PzuvVqt5tBVcTU" +
                "1H+4pabZ+dVzMqWmG5krDtWeZBwbV0sTDbq3wL9odZMNr+aXlM" +
                "scOeXhPiZNfBPCeRyuMDZ56aJyPNSKp1qHntXyKK2hCSR5/Hc9" +
                "bZWl3NXO33F6jxvQiT5GYnyq4Oc5dusnctzJjVCiJjBVyffoz4" +
                "T/FWvBokE+I9Gv2r/mSq8bPtjXr+vD5aDujta7of1PNDmnVRnP" +
                "Hj40klsd9BJh9mL8l5ydjAteway/97/I/4n+3B+OX49SziRLPC" +
                "k9GJ/R42+Sh5Sc5PPpl8Kv5jvIVqZdD4L/FeyjV+Rnd9BU8u1N" +
                "vxcXaFjY/KcZWq5ByzTex9d/xEvF2Puxtr/aseI/Fb8dv+dTAZ" +
                "ZbP6WPIJXbEVagWPtoIrqOlP5lLCWC9baS6CbPSW1ep63871Tz" +
                "PMgtH2tvKYYofdZB/isHdYVyvcPSzGB05TNXm0aJOaXldfJ4z1" +
                "spXWnI8SstFbVqsEmb4F45gFo+0nfFuU2Iv09KYwh71DrZruHh" +
                "bjA6dX9fJo0V5q+pO5SvXac3svo6LlpmsFEtn73rJaLRGmPrd7" +
                "FhwBs2BUn9t7kVOU2It0k73Pyc5XeU4ilccXjjkmVV/rDi33tR" +
                "aqPiO3FhisdoUZDdK6na6mqs/8joPfrhOfEdUnmK1OV5ZNHzX8" +
                "Bp7uGRiV6zh7ca/ZgtEc71HMPYPBTFza2jvfKzBP2kvUc1z8Hc" +
                "c/lzUO4j1D/XC4ad5Z9cPtIRfzWb4kGM9s1A91XeIzbR4j+qTW" +
                "7CzXN84kDHv9cNfFIb/63O7E5vjos5iTcOg6GD8WP55Vd1e8J3" +
                "7aXAdrM+NnLfK87gfiV+g6GL8ZH4rf0TNd5/hI8TqYXdny62D8" +
                "53hH/FT2jDMz3s/XQb3Nr4OWd8JcB5Oz6Tqox3Pd66C2fTLeaa" +
                "6D8ZBm74ufiV+IX4xfonVlroPxf+g6mHxI948Yi+Sc5OPJmGRc" +
                "vE2ug7WZ1uK5+G8Q+9X4DT3+m66Dsa1N/G58LH4vfj85w7kOLl" +
                "PLeLRrcxk1fXb8plrWfsYgjIpWWK4kbPSWna9awmwP+hbsD7Ng" +
                "VN9flcYUO+zpN0Kc7HyV5yRSeXzgdKpOGWlGUu1G1WnvRXMUtb" +
                "TV53ZA8uidPGedrdWjzEWPyBMvwiS52enbsg5zl26ydy3MmNUK" +
                "ImMFXJ+BGLPULBlpRlJthhkRRW0IyaPP4jnr7HE0g7lojzzxIk" +
                "yS09m+Leswd+l+NDOms9z80L7oMxDjOnWdjDQjqfqYsm8RCIra" +
                "EJJHv47nrLPragtz0R554kWYJLff9m1Zh7lLN9m7FpIhRsYKuD" +
                "4DMZaqpTxadCk1fe4/SBjr1VKDuE0fgyDVDwrbXj0Osi+7v7uR" +
                "KRrxTzPMAnNBTlFiL9J9CzPWD7J3qBX4DMUHTlVVZaQZSdVtyn" +
                "5nImh6n2jdLc1Svi+ukmXaYJ2t1V7maub3UON6QTn3lKDs6jB3" +
                "6SZ718Jm+F3MD7P1ffox8ruM9dlz71fzt3JaouPvkPWz8/rSt4" +
                "G2MjP8V13XOlHyXe16sTPfIdfXt0e5jO5prZPCMTOW6utD3yHX" +
                "16ddjaPEao92NT4X3ykqywveVvp+/Hh1QaqfD5P8rYjkB5WK+R" +
                "1HX4Xs/VN1galV/GZZLZacmdyf3FNeq9pTRaR5Xhbp3thWMV1G" +
                "tWqO9r49/5zel535HdE+3V/IbO+uLvBjJXdRtGRMwc+lzcKnkP" +
                "wwfrWsVvr+am7o+/f6JvdbQl2bXfXhGj0FbaoPm3fV7Hcym+R7" +
                "RvOumvtOEXmR7x3xXbXqz+DZ5XX5XtT63+R6WTLDfcaxeQhnE+" +
                "VEtvS9aOF3lE0me2LiXtV6eT+hApvwGQffVaO8xD46Fh3T63S4" +
                "vpH0Rm6/Y7B0pRkNUt9oZqbrWmVz0tGMedrnRsYCx+Ba5KNdfa" +
                "Pxil7ah0nPaHq/QVkyM85DULLN9nJj1+Xkib3yHvB+QkU2cq2K" +
                "WVNeYq+mqWn27DUtO9NZ2fT6IdFwN8/ONEc+Iq7WiXzInK+Qj5" +
                "FdqT2K9MzStifdqNgRzY+22ezPnK9kr0Z6B9LXS1bWfrrSx0nr" +
                "DjsuJLm1wEi1B9R0+53M9NbtRjbd1mo6W1kv02luxtoDRa1zvn" +
                "qgyCcWjyixnlnpGuRwFD8XjOZ7V9Ppvr20Vl7WkpW1n6qmmmNQ" +
                "ZcepkdvvGKm2Sk3t/r1BpNtaTWUr5jNSW1XUOrVaVeQTi0eU2o" +
                "dJz6xmJ3LcnFxUovneQ1k5tfL0kpXZRkcj+y11fUN2zFvZ9PRX" +
                "0dHaXjOvb2BMn6+yeXRU+DQ3I3kRrVOrveYYZD7a1TcYr+ilPY" +
                "r0jGrbk8IxM85DULLNjvcNtb3kKTpqjkGOWdvL+wnnhg35+epo" +
                "UUNRcvvhaJhHiw5T07V6NBru3moQRkUrLJTS3wgbvWXn9l5hdm" +
                "8RDXohqRjHrKuymLIHbk9/Heawd6kI+gzFF47qUB32fNWRna+0" +
                "rM9XWkp/pzq6zXcyHfp81WG22THYwVZ2dXbQnBFX61wH+4p8sX" +
                "Dthces7BiEqNgRze/d9vveQ1k5x6Cnl6zMNjoeHbf3DJuzKmtZ" +
                "3zNoLN0QHe9+ziD1zUY2XR+D2dyMzKe5GcmLaJ1jsEf4el2BXX" +
                "2z8Ype9D3DcW75b145x8xMb55NW8klP3o213rIE3vlPeD9hCNt" +
                "c34MHi9qXHt1g7rBVu6GrJJWNn3SlaLhLjLyEXG1zj3zzCIfI4" +
                "fy4KafcU4gx83JRfNoN/veQ1k5q+gGH0H76Eh0hEdbwSPU9Dq4" +
                "hjDWy1aaiyAbvWXH4MO+neufZpgFo90ny2OKHXaTfYjD3uF8dc" +
                "Tdw2J84aixaiyPtoJjqeljcDNhrJdtGZJ/GgVvWa3WMBfti1E5" +
                "JnIrlZ6Kb8s6scOebipaSIYYGStQHp+20e5oN4+2grup6U/mWs" +
                "JYL1tpLoJs9JZ9J/OKb+f6pxlmwWj6h/KYYofdZB/isHdYV7vd" +
                "PSzGF07+PDpYfHauVfPnxkF422bQ/e+bwls7g+4bSs77DG/5vw" +
                "9mPgaNHTw7Dxbf6qlVybfkkOcxGHx2HqTsJdv8faNB79w+WPaG" +
                "H+UFz87bo+082gpup6bziwhjvWylNeejhGz0ln0HdQkyfQvGMQ" +
                "tGe873bVFiL9JN9iEOe4d1td3dw2J84eSf1UBxXaVb85oOwLoa" +
                "GHFdDYTes8xqtbB0XQ0Yr7CuBoq/O/dcTpElhzyPgeC6Gki3kC" +
                "dvXQ1462qgdF0NuPbRjmgHj7aCO6jpWj1JGOtlK81FkI3eslrd" +
                "5du5/mmGWTDac215TLHD3twf5rB3WFc73D0sxhdO/ln1e+tqR1" +
                "7TflhX/SOuq/7y89WkA6Xrqt/YwbrqL56vehT5lhzyPPqD66p/" +
                "0gHJx1lX/d666i9dV/2ufbQz2smjreBOarpWTxHGetlKcxFiN+" +
                "8pesvuDr/l27n+aYZZMNpYXR5T7LBPWhjmsHf5XwA3Hz8+cIai" +
                "IbzTJ9n0dGc0ZN6tjYb0nX6G2aeGIeIJn7RmZCZpG4849+1zhN" +
                "9Yx3b5s8SQ+DW5kJ5ZjZ+Tb8mB8xBUcjJo182Sj+xVY235Mw7a" +
                "Y15inz85Zu9+RPm7NukuneM2OK6tpvy3iahv5CeILnhPurGu8N" +
                "x6otwu6ov6Gk+U/TZRFrfrO/LbhDBC3/XxbxMBTeG3iRH+j36P" +
                "Ph//KPz/Ba3odP7/IDtffVvmPZ2VU/4z54pm5+n+Hz1Gg//wX1" +
                "s5rb/WpKDvidmnlH/i6b7C57tuZL8fpO++u6SGEz/Ib7QuHSq3" +
                "DcdN9556ZiN8vm5u/wMDSgXs");
            
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
            final int cols = 75;
            final int compressedBytes = 3326;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq1G2uMHVX5toD0xZZSW2DJgi2NZmOiUWIKAuZO516lFBMwJo" +
                "JItYhFatcfRlqiAc+5u/dud26iQbupmGgFWSCakEKRFgq0pS9R" +
                "HgWKyKOtpaUvtrWFLSix1TNz5jvfeXxn7sxumck5c773N9+cc+" +
                "Z8584Nl4ZLS6UwqeMjbkkouDVc2rM8xgAWqchlQiZ39EPEl0p9" +
                "dyBntJCSkC3dC8Xf5beJcnqJvXd9rd0D2kvq0HVS9l2JyrRU8l" +
                "7ABD8qGQdS6KMVva+B7frprmW/3vDe6Pc0LZal7eret/LMf5i+" +
                "hXeFd0ENsITKD0kc0PGKp4mxufWWiFUvcsa6XQnZ0r2AM7rbbx" +
                "Pl9AIWTE79nt0I0PY1nv6wH+oE2y9P8ezHhv3JGOwHLFKRy4RM" +
                "7ug2xItY3Ymc0SJKQrZ0LxT/Yr9NlNNLfQzlazoGlU8I+e27Ep" +
                "VPyGt1SI2TCaVSHCs4kEIfreiVW7EdLXQtZ2mOujw6hSxrb2UN" +
                "DxmrAmOQ9K1yQRplFZ36FDNW4fIW80oLeqOuxWqRa9mvN1weLf" +
                "bcywU+u40eyrPCscr0jStq/VyLconZd+x+xMf0fCWrnzWWD29+" +
                "5V/kl0YD/p5M96vGbzUNF/u191yR35NwWbgMaoAlVD8vXNYXj+" +
                "BlgEUqculQdZrJrbfEnU1DzuhtnUe3qsvh2T3fZxPvwCy6NeRM" +
                "5yvlE/Dpd2jbtyWo3lCfXnS+qnaYmOgdg65RowP5n6bQOxQd9N" +
                "PpfqVbQ8/dMWj7nDki2yvtUAMsofqnKu1xrICOVzxNTPVC5E4i" +
                "8j5wJV5diJzREFJ0LRKy7QhNx3w28Q7MoltDHhkr9An49Du07W" +
                "tR6qgkka2k8ZVwXOoXVTqSWHVgQU6dHzDVyboO5wlORv7ogC6n" +
                "y5h6ASv7FerVfTKxaM3VXukg+tVkrd84XqNX1B2F69UYnGm9B9" +
                "e3mPnWV6dncevUaKjY/B4dy7JLjsHpFJWI1fTSsI9wrYrVxT6K" +
                "T7J6Xha3Ti16NG/O4zFlrZXXI/EqfErF6jKx1u6nKD7J6owsbp" +
                "3a5MW86p6fx2PKmkkl+tWMEcTqaRWrqhWrp/NK0tyN+7VYNU5i" +
                "rEi/GvdR1KJr0byZSvBJm1L/Zv4cJ4bqNxj7DNrcV7++mFf1bx" +
                "VdM4D3plf1a0cSm3BluBJqgCVU3SVxQA9XxhjzRH7QgDipQdO8" +
                "Bjl1iq5FQqZWXRNlE+X04krEdXUX3rMbAdq+K6Ek16ln8G0fxR" +
                "P1ddncfQ+MYG5fmteu8n5uPq9H1M/+rHpxh4+izSNdPrrL3ff8" +
                "CGLVn8djYwx2ZFFPzqHlzvPMuZ1/NXsPho8xoKts+iytnzb/eD" +
                "Lm9qzcGazxKR/d3B5uULGab70HN7ToVxtoPepJ79Vi9eBJfA9u" +
                "IPvVXoo6sliFe8I9UAMsofotEgd0vOJpYnRuXVvq/X5XztQvW7" +
                "oXgG0+6reJcnpxrUl8fYHunxkB2j7yxFk81JDVp7gjEgd0vMqW" +
                "SdHHpK0tjdU+kAY5FugSqBdhpfuILYs01m76I4trDT1EnxBqYT" +
                "+5sg1MrNbYRrYpXa08z7ayl9jfxRNYyP6RYF4XZTd7m18jroPs" +
                "MHuXvSdasez7onzIS/xUc77iZ/A0I2V/YX9jz6Zz+wtsJ7+a/Z" +
                "PtYm8Jyh62X1sjneCn8NP46UJ2fKJhAm/jU/nZbB1L8rl6F3uG" +
                "PcdeZC+zVwT3NvYqe5NtZztkrJjIrNkhUT4Qkh8TZayI1WN8HD" +
                "+Tn8WnsM2CsoX9VdQvpNZeY2+osfoY28sOCNw77Ag7Kq5J5sn+" +
                "zf7D/suO81F8dOLPRD6pVCofLB/EWrYkVF9UTrL78sH6YsAhVV" +
                "67u3QM2JcwtKDdtxV4UV6X0LUgp4Sbq1xZoOm+YwnW2xLooW5Z" +
                "8lE6XRs9/8N+a74HKxvtkaX33vopzormVNUaTf4++C+DWzz/+i" +
                "j7nSpyA3rNsNr1JPs9WNnYcyLRd1qL9/5v/HuY5lhN4zwkSyL7" +
                "O5VRTQKazlX2asZYeX5LzdhXAa0+7c0nXE8AQ8s0zpS0lrG623" +
                "9H1N0a/Ur9RlvZnNWvih/BA61zySztBfvV5jz73uJ+Bwr2q2Oy" +
                "mGOw8TlRPl8+ZnKVjw03VrPGZfSrY+bVluzeptPAX4mhZfh9fp" +
                "rBd7+fx75b/pYlu0O1dmWv0xXfbgve7onV+CK7HJQk31lgn2FV" +
                "zt6+quDoaJMl8WdQ9eItQNO5EC56RGdke2Ba8/OgvxJDy9je59" +
                "HrUlxaMFGWJFaH09Xs1HCqGIMfBzxwIVw4Q5ia4e9E85rFg/5K" +
                "DC0jreXx1s/j3i1fy2/k69gmvoI/E89XfHWKfzypv6v4bhLle+" +
                "ywIat6MFe7CPxZcqSuiTK+HWInUq7ve8b5SsHznOIWsxd7U2Bv" +
                "SWibyft8KKGdZel5TLW2AB/b6/XqOHf2+cvvypLoOKrm9stEuR" +
                "zwwIVw0aOSkS+D1izttieAoWWktdbe8iE/j323vnywe0kY2Plg" +
                "9zJfPihb3b9Oen9A54NS3/DyQTMHzJMPSi/8+WAYFM8Hw8FwEO" +
                "rkjgbTs1Ocg43vxBiF7VTU9NT4E3oCdzraOpN2pysneVCLhCyt" +
                "Fsak4h1YpZPmUbo61cxm+OPaRx5/7txYYOXOP3Bz5+4umTvzha" +
                "1z5+jLTLzzh5c7C46iufPanLnz2vy5s7uLAv2wsdA3Vu1VdvbK" +
                "F6jRFVl87sqcXkO740Wur5w9mUdsb+n1ePCIeWf2GCS9vV7W/L" +
                "ia239p07D23nMGtfGSX8qUo7UgNm4BZMsCtvEircnG8BN+n2nd" +
                "+GQxx6mRTz07w8qiRlfnzbs8T3OIyl/FG6Kd4g0e9/VPa23xeP" +
                "58sHpd9TqsZUtCjV/FtY7VqRTG1AktaEdzgVeX1/lQC3Kamkyb" +
                "yAteYgmesiXQQ92y5KN0ujaUt3NlXVPfQjSW2jSsvU8ig1r5k1" +
                "/KlKO1IDZuAWTLArbxMq3JxtTO9/tM6xb4G2SN89Ws220a1tTv" +
                "OC7VGoO3e326wZSjtSA2bgFkywJWeu/SbExtht9nSnd5IB6ZcV" +
                "0egLmgPND4Q1yXB3DsSj59rpBUgCVUVt/BRnfId0msJT6J3aAB" +
                "XYOsY4zNCZalTfc9CL7jPQRr7PdgrFcW6VM6BtfYb0D/e5CG49" +
                "JYqb+l7RWCuzKnfscx+tXPdH5z3W77SntLrWxg3W7P2z0N+96H" +
                "94W+Ll9pq7RBncwqbfIUrZkSB3S84mlidG5dWxqrn7typn7Z0r" +
                "3QuXw2UU4vwcM0D+hSc/vDpj+ufZSoTKkYv2JLOC69oyQFYOBD" +
                "LPCbGFdnGqsNOj/KmTKmXprH9sf0LI3BE7o+vCtnP+IJXT/xRt" +
                "LkZY4jetk82ENmWwVW5Di9F0KOU53HdgtKr+RjSe7NhuTcbv4+" +
                "KLVgjmPEKsmiqBynOq86L85xEtnxUoud4ySWRY6Ttl4VZTvIsv" +
                "QXrDjHAd5gNR8XX+X3DJjjwH2qWK1GDDtqjb/YLyfHSeBzzf32" +
                "8E7rjfEgfy3re4YWex+vRy8O/0sCvi1j74veQ34y557wk8W9CX" +
                "qDLnkFTO+lSEvqLr5PtXt9WrIsZNjuzebk+3Vs3AII/KY1upps" +
                "jG27FY3fKJ7OJvt3nN7L7X1R/qG9L1qoX700gn3RD/z7op5+Va" +
                "f2RSm+YvuiicwS8ypi9SWTJnLFfdBGLlpLMVqwBGjyDe1yJv1q" +
                "ia4J/aViBRpdTTYGbWf55XmC2K+usig31dryzFf0fnvtzGhXjt" +
                "7n6Ve1jN/LahPI+3y0haUt+fjI/paMwe4xWqyuUXqTfKF7bBwr" +
                "cww6Htzmj1VzklfqpzAGU/gnjvRkagwmvIupWPFFwQp6DPIfW7" +
                "FaUXwMKq/UL1O1s0sn8aid05xa+kiO2hRyt2xbPunGK8N4DzbN" +
                "q+hXXzNpwexkvmoCJpjt1yL5/TSboqzPpjmT+aqpawIPhOxsv0" +
                "ZXk41B21l+aZnpe7IkXv1CcW4Bms6FMPGEP531NJoZ/RS0Zmm3" +
                "PQEMLWN7n0evSzFpwSHLxiEv5Qoa9mmw+Zvn5Ojdmf+opT2iZW" +
                "zevDoLjf7PqDH4dTGatZ4SVPPvF1P0WTnW7bYNNats91ugZWhr" +
                "7nfItc8OY76KzKuI1TdMWjBHa0cA01qQn6bZFGV9jp8TsYn11A" +
                "MhO8ev0dVkY9B2ll/p/OT9pqh2kSV5JQ37vimy+Rs7czyxKzNX" +
                "JDspblqmsSPnOPpCgf40aO6cBYM6Jc93fShBw2pUTGg9Xj3fX0" +
                "1If5sZNHf6sr7rs73333+x7/qMOM9UrUuKzU+tjub5rXmGY8MT" +
                "q9E5+8ro4harC2StrduvtWlYZ2vx9Ks3/FKmHK0FsXELIFsWsN" +
                "KaS7MxtcDvM61b4G+WNa6veufDexBoWHu1Z1Bn7fBLmXKulvg9" +
                "iNi4BZAtC1hpzabV7nEwVb/Pju7/A8JKsNY=");
            
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
            final int cols = 75;
            final int compressedBytes = 3088;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9XGuMHEcRXiAPCHbAdvyMTPLTFiI/MEoI/Lm52Y0lBAn84B" +
                "El5Cywkpj4xEPiEYLEztx5Z0/r8JDg7jjbwrLIL4TEDysEJYoN" +
                "iYiwLHP2ITtCiUQcWzE4PkNiOyaSE3q6pqYe3T27e9nLtrqn6q" +
                "uqr6r7Zmdneteu1eSr3kApe7DW9TU2Wuv51dlUW/CLqurVNvR2" +
                "b8y9+vFX414Yk5+Wa/VdbaPR/0o/K63Z97jWOhnOLeP8OQjNJd" +
                "R0LKKtl/xMGknvDM/Iz23w7TCmd5UzfdjM7xS30Rhkr7AOvxiO" +
                "knEuS+s0R3MJNR2LKGTTtnSfg3whXLOfu1aLJuTRrFVH2qI6k8" +
                "3YuBBmsZkuhG3aUmavhz0JzSWswMTWw4wuk0Yod1VdCn9UHs1a" +
                "7ZS2aHP6xVJ+NNfDLOAftmlLmX2z3zP9Ekdt9qICE7s5zOgyaY" +
                "RyV9WlzrcHYUzvLtdqSttoDJ61FdbOJ8JRMs7PQmguoaZjEU2/" +
                "7GfSSHpfuGY/N4v9WrlWMyryQu0dvTqf7+ETJpAj/Xo4prnOG7" +
                "Glt6rSrQufUXJTuVZ7PHN53i975y3s6Tc6X114VeloOIO/jqhH" +
                "5qg2iLUa3mU+f+ZFxddXx+YRhuGwd7YfBusC1+o7FbYl4Voq5v" +
                "ncwiqJroFuOa6U59Wv0ca9SO/7PXhfdQUyW9iH6gXEHzP8rW58" +
                "xUo/HPbpNlu8F02T9Ee1gb46W2uL8gpcrx5ZnGzR1dBtjh3leb" +
                "UPbdyL9L7X6snqCmQ2NfPMrQQRf8zwaBUfY54I+4Rm29gGI3sP" +
                "/sZcrz7KbTSGngelVa3VX4KfAttknMvSeo2juYSajkV0+Ns+Jn" +
                "PfrpGfhWv2cxv8IRjp2p7+XNtoDLJXWDtz4SgZ52chNJdQ07GI" +
                "Dn/Tz9QLEqqrPN92yqM5r/6obckrKJOXn6U/W7RT2lzP5AxHc4" +
                "nq9V2vkNFl0ojO3avNnk2/LKXJQV4T06nO3xfnapv+woe2Xq8t" +
                "+ovtyTwzWObWG+9gPWb6/RxsfWXx1qhRPNukvy3X6qS2adnhOF" +
                "ttDeeWcX4WQnMJNR1bomf9TBpJfxeuWXNH26PtNIIEWjYfbc/O" +
                "c5RbfYjkRAnl1lvoy+O5H7GQp2SSOckXq6TeuqIjqEKema+A5H" +
                "RzxJfjy3ztQM97YzK+3JgEGTt4UNTYaC6BTDyas/gcPI42ZOBx" +
                "mgXsiDYmJS+vSaLlu+Ial91XlZx7CCviL8YXcbToRWimvpn4Ym" +
                "MmRxAlK3lJjbw5G8j1ETeORyDOq+BeoZwUx7ubDbl5faSF88uI" +
                "ocfyTiNgHOV+KPGjjJG+gBOzjJdZCQvxulp+bUeEuqyTz1LPQG" +
                "erqkRd624rpd8P9tMjW/puPg9mSwafKT4aH8URddCyy4ChnY7U" +
                "JMK9OVtR/XI3TvKDxKvgXqGcFMd7tszvQ3N2V8Cfn/kci4/haN" +
                "Fj0Gq19jLA0E5HahLh3pytuF59xo2T/CDxKrhXKCfF8e5mQ25e" +
                "H2nh/MxnLp7D0aJz0MxarQIM7XSkJhHuzdmK82qVGyf5QeJVcK" +
                "9QTorjPVvp90EutlZzcoY6P/k0Lsg9btDz3l4NFtTRj9D8niH3" +
                "ByvxaM7ivLofbejfjHRengWb6yPraa6TlWE2zkez6ne3n8c3n2" +
                "n+2WojuIfcnDWouRdqr2/anezmPxojzZeN5WPg13zNoljpm0kt" +
                "ucpYb0nejyzJ0mSF52q71vr/s/lS0zwRNE81z5T1jDRGkvcldq" +
                "8o+SCwJNcnq5LVzYPNP5FX82hzrpBOmP4ixjb/XdTyBvlma5Lr" +
                "8mOy0lqeax4y499onmw1SqT5X20x8e+1VX0oWWbOrtl4Fkd7ts" +
                "1CM2t1M2BopyM1iXBvzlb8pR9w4yQ/SLwK7hXKSXG8u9mQm9dH" +
                "Wjg/8zkcH8bRooehmbXaBBja6UhNItybsxXn1To3TvKDxKvgXq" +
                "GcFMd7tsTvg1xsrQ7LGer8zOdIfARHix6BZtbqk4ChnY7UJMK9" +
                "OVuxq/uUGyf5QeJVQBt+iiM6J8Xx7mZDbl4faeH8zOd4fFzcb1" +
                "k97+0RsKCOfoTa58HjGMG9JWexVk8jP/pwLx5PftCGn25v4T66" +
                "HlkZZuN8NCvnKvoRXoPn/vO4zGGvY1uc+/YntE34/cG7317xbW" +
                "/nRPDzZ4uM0yzZzRLNJdR0LKLZTf56ekFCdVU84zw52CeE4QOL" +
                "E+l/xll4topnnJl4BkfUQWtvBQztpmVoJS+hZ1bP0EZxVs7cOM" +
                "U/A5piVXmklWagurdWNseskDNZj5ufrdJkPImjRSehmbW6HzC0" +
                "05GaRLQ3l8w7aaMbJyNA4lX48uicFMd7tsHHQXNkZ8uknKHO70" +
                "aUkXtRan9fW8amKs/QvWPT9hj4vjpe4PfY8V6qyW/vls3ngfZ+" +
                "qoqn42kcUQetPQEY2k3bgVbyEvoOq+9AG8VZeYcbp/inQVOsKo" +
                "+00gxU99bK5lh8vw5+VI+bn1Yp/EzUntKW+MfVT1NgD3lVR/f7" +
                "nNbt2t4tG9r7qSreE+/BEXXQ2tOAod20BK3kJfTE6gna0Dr2Ey" +
                "snbpzi3wOaYlV5pJVmoLq3VjbHpJATWY+bn1bJ+X6w/I6+/Stl" +
                "ud315vdX+bOzxX4Q+KvcveDvLD+9gPOqS7ax4mqc3dLHebU73o" +
                "0j6qC19wCGdtPG0UpeQh+3+jjaKM7K426c4t8NmmJVeaSVZqC6" +
                "t1Y2x/FCHpf1uPlplewe/CXo6l70ANrQKx4bukS6Z93HaOwFZ9" +
                "8CXJJH5w75oKyE1+yPgWzharGafE7hqqpmK9bqWXOGflzwt7qc" +
                "oy2/Vwjv43uNTX2/B73Z0n3a3k9V8a54F46og9Z+FjC005GaRL" +
                "Q3l8yn2QtunIwAiVfhy6NzUhzvPJvk5rOVK+DP70aUK//XUjo0" +
                "4O+8bntXv/O6dRF+81HsYddvdPbbDzUuZLfz/fbGeb3fTvvnzH" +
                "7e2cM/z0Znvx0yV++3Z5/y77fnsf799rwKd7+d3oNQDR3993H1" +
                "G3l8fWN9I47WuhGaWas3AUM7HalJhHtztuIvfacbJ/lB4lVwr1" +
                "BOiuM9+5zfB7nYemyUM9T53YhaLbK/ck7Lnej0qFxjsIfvr7q9" +
                "6h9YnPegr67es2V39TGDtfW1OKIO2sRVgKGdjtQkwr05WyFf68" +
                "ZJfpB4FdwrlJPiRL/W70NzdlfAn5/5rK+vt5od21dAz3v7LbJg" +
                "J0/SJANinr/gPdxfx+Va+23NK5lI4zVJtMy2QbNLu/e8We8iPD" +
                "7eH+/H0X467odmzqurAUM7HalJRHtzyVR/rxsnI0DiVWgmX06K" +
                "4z271e+DXOyeYb+coc7vRpSRj6M0EbT4r1fS7nrXb+h2PZh4z4" +
                "L2tx73niM3VFkH82L37Se6XFPv6M9eXzHwq/od4ToGn82s++n4" +
                "NI6ogzZxHWBopyM1iXBvzlZUv9yNk/wg8Sq4VygnxfHuZkNuXp" +
                "9cAX9+GREth67OqxfQxr1I7/u+/YGKM2S5PFb5UL2A+GOyDd34" +
                "XF7XIm1Drw69SiNIoEXb8pGj3ArHsVGOSE6UUO48j76ckfsRC3" +
                "lKJlkF+WKV1PPqZQRVyDPzFZCcnhzzQ/M0ggRatn3I/ktLQrnV" +
                "h5TZ51FGm12rk+jL47kfsZCnZJI5yRerpJ49pCOoQp6Zr4Dk9O" +
                "Q4O3SWRpBAm1gyZH9PTyi3+pAy+1mU0WbX6mX05fHcj1jIUzLJ" +
                "nOSLVVKP1ukIqpBnBj8fp5ujfG48CGP6n/ITfKm20Rh8Eq+wdk" +
                "6Fo2Scn4XQXEJNxyKazvuZNJK+Hq7Zz23wAzDSv6NPL2objUH2" +
                "CmvnlXCUjPOzEJpLqOlYRLMf+pk0kj0Srllzx+ficziiDlpjOj" +
                "7XyL8xO4coWbGNjXIN4l22Yq3OcE83AnFeBa9Fx3INWahHq/0+" +
                "NOfiPbha1uPm1xGmmlkY2T3D/7SNxtA+g7Sq8+pfwb/frIzzsx" +
                "CaS6jpWESjFX4mjUQrwjUHuEcbo/lI3Yy7oEsPsrtHPy9Zq/yk" +
                "3fVEHqyC1clw6tFKXa3LZddqpZwZsmEL747yX/g2pqDL/VL3F8" +
                "lV3wvLXxJX+Um79mxM8X1T2lulPWT+a2n7f/KsCf/6mNcUrZEz" +
                "o31Q/Sto8/o/6QH2qw==");
            
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
            final int cols = 75;
            final int compressedBytes = 2914;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW22MXGUV3hY/ohRSNayRH8Vo+EViIqKiiO6dO5PSdLfbQr" +
                "8ovzQRCYYU/KPxc2Ze7TA1KWWNSjSxpYoYBQQKusgPsVGwATUS" +
                "rKlt9I8WViqpsbQa2+p93zPnno/3vHdmdqYN3Oace97nPOc559" +
                "6Zufd2dndiwm/NXzZ/FYKJ9uli9ayPsksn2Nb8W/tM4Y82Xyr8" +
                "v3rYCdi3XjOR2Jq/bj7dfKZc/SXJOxN0Xo/r1rKIsb/5m7D/Q2" +
                "HPNf/YPFzs/xyQi4P/R2Eni8rXFfYGmL61vPXmAn0y0jrYPMRW" +
                "R5oLhX+xeaw4+lPNlwP27+Z/mqeap1tLrGkbvwPf/h8i3Wt0jn" +
                "xqq86mq2SdrUKoj3ClaxHtdG2lQZDUXLS1Hgz5R1vzPeatE6+Y" +
                "bdu+dK71pHmcZ3V6OFfuteW5+tSr+lydg+kb+zHqfGPiVbHB9U" +
                "pvna+fhXOzpbGFPESw6n7Ue47yrIVITYwwrt2NXF7PeaRCTKkk" +
                "exIXpySLu9GEvDM/A1Iz7tHjH28c956sOFc3Fe/9yzSD8vHeeC" +
                "WOQ0U4V7ureFJHM7e9jDo4Bc0B7ys9f223njbc5ffQsVhHRpPg" +
                "P+N9HJ4Z3Pn8maHr5DODWzbaM0N97lw+M9TnFvPMUOwrnhnqF9" +
                "QvQI9rWHW7gGGe9vRPIpzN1XrxjrhO6kPEp+CsVE+qE7bD5tAx" +
                "x2fA7s84k/VJce7COthVkCnXk8ggD3yJxJo99E7OpzpZI3Vtjp" +
                "5HTobduB4dVdVmTj3Je7R+XtgT/pmhVdwD3XJ8ZujuHuA546dl" +
                "9NsyesZkPl6/Y4Tnmb3DPjPY3VqPldFTY3jKugT27vbuHpnJPl" +
                "G91pvO13f2795P02LbNYN0G2VzF9O5Ku4k79D59oUDne3wvnJv" +
                "Ecpvay8fabKL0rn2MguNp1dTDvy+cpPurRDlR/Ij6HENq9qlgG" +
                "Ge9hDJDKy4JqlxXCrpDKoQU8/FpyAunwfMTy8raELemZ+BdH9Z" +
                "Ub5KHyuffO9T7/hpt0Ku+3xCpod//wxXA2y7xr39LP7f5hfga7" +
                "vKd/EuMMqRt7faruqsHXtNWRereD6hPsKVrkWUT149YXpmrV1b" +
                "UVtBHiJYdb9bWwEMRHnWQqQmRhh37kcur+c8UiGmVJI9iYtTkn" +
                "WW6QqakHfmZ0Bqxj3Kc7gPvHtn+f3V93SOfPlOv0W9EhXfB3R+" +
                "nHz99sk6W4VQH+FK1yLaecBWGgRJzeXeW9j7Q1QTzwzT3XvS90" +
                "E3E10fZoPf7N7Tuw++272vzH3AfbC9vPOgeV1Z6da4deVqfWEb" +
                "3SbBuFzeB92HWfwRl/n7oMsLaxS2urC1hV3XYXdHd0XwVwZ/lf" +
                "sQ3Afd1W7KnOgat0qsr3Ub1HVyLrsN9uX76l7KBc/yxNIqFVfi" +
                "uXRG5mwmoT7CFc5tK8ZKGtG9B8llO+W+OFcP6RzFiOjPYFbxBJ" +
                "jOZTtlzmYS6iOa1/r+ChVjJY3o3oPkGlvB13t34Pp0fVrnyCc/" +
                "4RXZ+nQKa2yVdbYKoT7Cla5FFJX7T5ie2dKunfBGHjAfuY8DGs" +
                "7yjRR7n92IK9LhDJ8nnJStCsyBpmbWTjjWi7oAG+eQRnPyo2zv" +
                "0Rl7rnQuv9wbecAgAjQcxwzF3mczuCIdzshmOE7KVgXmQFMzqa" +
                "fsAmycQ5qs4SqWmr3pnLtpYmLqT97I+w0jQMNxrKHY+2wNrnBD" +
                "DYh9nnBStiowB5qaST1lF2DjHNJkDVex1OwtlctXZVthTwj6fB" +
                "XncVa+SqvEURohfZmzVaknnwnnthX7T6F7V+fk91etS/D7q85D" +
                "Y/2J2uMjVQ/9/ZW7rPr7q0XOkThXtVXj/a7v3J4re/rxfNeXz8" +
                "g9Ru6TGGXXEuq9X+cztoqPgG/lNJrPYI730JU0IdTgBFYfVIx7" +
                "xtr2XKlc7UVv5AGDCFDOw4jveY1kIE7KVoVmx/PxnnIm/yyKCJ" +
                "k1hT4uu1vVJFMH0MiH5/a947xeoer4q+1cZ+/4p3C3FgqHvJEP" +
                "qr0I0PD5uIFi77MbcFVOcogzfJ5wUrYqMAeamkk9ZRdg4xzSZA" +
                "1XsdQSr0KUS13b87lXyrU9e8ndNuy1vfPI+K/tjQONA+hxDavu" +
                "fsAwT3v6527hK87mar1P/3mcGVcgzqfgLF3LV6hCFndDbT5fwV" +
                "sq54n7y4ragjfygEEEaHh1r6fY++x6XJV9FzjD5wknZasCc6Cp" +
                "mbWF8P/BBd0F2DiHNJqTVPRx8bV5bTdyU4e9kQcMIkDDcayl2P" +
                "tsLa5IhzN8nnBStiowB5qaST1lF2DjHNJkDVex1BLXKyNXO+WN" +
                "PGAQARqOY5Zi77NZXJEOZ2SzHCdlqwJzoKmZ1FN2ATbOIU3WcB" +
                "VLLfG+MnJ57o08YBABGo7jOlyD92tkcyX3GYiAT2qaS10oB5qW" +
                "KvXCGmTjHNJkDVeBvfu0PE7zWTTKpe6D3efGex+sLRnhyWbo+6" +
                "DdbbT7oPtc8bl83hv58FntRYCG13w9xd5n63FVfr6f5wyfJ5yU" +
                "rQrMgaZmUk/ZBdg4hzRZw1UstcT1ysi5LxTvt5WuVUSdfGUPux" +
                "0i7yHKtuAavF8ju3zXrqQoY79n6Jruy5ob8K+APuZ8jfu8rYoo" +
                "sGmCUNMtbHthX8KJZU3o9UVA3Dapbc2VztVe8EYeMIgADcexmW" +
                "Lvs824Ih3O8HnCSdmqwBxoaib1lF2AjXNIkzVcxVJLXNsTuXxW" +
                "7uMoW0ex935NnLgmW5fKaTSfxRzvoSsRdV+FGpzA6oOKcc9Y25" +
                "4rlcuv8EYeMIgADcexkWLvs424Ih3O8Pne0e3wGcmVFZgDTc2k" +
                "nrILsHEOabKGq1hqiXNl5PIrvZEHDCJAw3FsoNj7bAOuSIczsg" +
                "0yI7myAnOgqZnUU3YBNs4hTdZwFUstca5Uzt1RXO8PeiMf7gG9" +
                "CNBwHJso9j7bhKvyvnGQM7JNHCdlqwJzoKmZ1FN2ATbOIU3WcB" +
                "VLLXEfjCeZRyMfnq8ODfC90vzA30DNTyx6y45VVdu57Ni4p6h8" +
                "Fj08zLMowxLPotmZc/l9u91txGfRO4uzfcQb+fAK9CJAEcOYI+" +
                "xVOxIzECdlq0KzNUv21DPRZGTWFPq47G5VkxTPEX/3Rh4wiADl" +
                "PIz4ntdIBuKkbFVodjwf76lnosnIrCn0cdndqibpXfPXyb0dYR" +
                "yzq2pSSHhff81nZM5WldqpCQrFOVLsP4XuXZ2begyNfDirFw1w" +
                "xR74Z5NTI/0Us6raztnTT52ln6V2frK4a3uCea5/7vyu6mv7Il" +
                "+zp9DIh++YPztY7SAY113sjMPl7OmJu9hp8qu9kQcMIkB7r9U3" +
                "MSaWeC3vIrbMeDWJ8C4yFzN1Tz6TnJIsngIRSy11XuxcvlruMf" +
                "KeMI5Ktl1t5zSar5Y5W5V68pnsPqjYfwrdu1+udtQbecB85L4F" +
                "KOdhxPe8RjIQJ2WrQrM1y32b99Qz0WRk1hTh9/qO6iOw5krnXP" +
                "hNdvcDt5v/7VL3iYDey3gPR5Xqd2vdrvJvl77jHijRH7p72ssb" +
                "bzSvwXsK+5HCot9mGvRvl9z3e/v7eTd3t3GVH/xvl+5z6jfzG7" +
                "8HX78LEYowRz75k9lkltSsKlkXq/hqQn2EK12LKPSLc4MgqblK" +
                "/Fnw9L5y8zpHPqmezLZvrqqSdbGK+xlHfYQrXYuoe9RWGgRJzR" +
                "XunyfBwnuz/HvO7l8xx1m0Hnar/7Pivn9S7qs4NC8gdg10G2Ta" +
                "NCd1tNkOuS/O1Qs6RzFHLJWewkI6JzMyZzMJ9VE8r6UYZzWie1" +
                "fn+HN797/sO5lTwzy39//5YGNpP7Xu6fE9t9vdRvxO5unU/3G2" +
                "Lxnzuer7F8Dbl47xXF04/nMV3mtvAgsa5bV9+3mY4yxaD7e1b2" +
                "6cXz2B7Jbm0LyA2DXQbZBp05zoaP8P7bBUew==");
            
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
            final int cols = 75;
            final int compressedBytes = 2271;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVG1mMFEV0jOJCuNZbMIqJiTHiFxFIdBN3hh4R5dAfgR/ORB" +
                "RE44Fi8GNnumDIDIHg7o+aoOKF4sFhYOVW40EieP5A1B8MX7ji" +
                "wX3ZPdWv36t61TU9Mz0d6U5Vvfu9ev2qa6ZmN5NRL+degMr9M4" +
                "le+csb18Wo4vKa8VZzJl/IvjBC4u7y8t06D3u7FdNVOOj0RWup" +
                "etxK4QSl+hBgui5QpTfOi0OJiguu7Cp19CRflo3SAKYUk5X6eN" +
                "lVKo9L+nEg1Yd4vCaLnKtTdN82ntPpdEIPuMTKOUkDPo54qxQq" +
                "Ta0F8HGup9qXEI2CSkX5RD2lHTfL4Jx5Bsz+uUbXl8HaOwSU0j" +
                "ZTrrv6Gl3l7u/RvK7ztfW79oXQz177pYa36dW1e2UMu4cjOecK" +
                "85TqPp4/Dj3gEitPlDTg44i3SqHS1Frw/P7keqp9CdEoqFSUT9" +
                "SjjXsD2zQ+NQNm/1wjfIuOCGtzgs4rDmFPb6HBwreGp3ys2O78" +
                "0/jO414TzSsOMu6ONbwVvo7t+y83whbmqjwryX3Wy9W5VHN1Lt" +
                "Pyi+RqdsJ1dSrVXJ1Kvq6cKc4U6AGXWHmSpAEfR7xVCpWm1gL4" +
                "NNdT7UuIRhHGMjnaJ+op7bRZBuccZnWKOkPdP9Uo7PbaHq9tKO" +
                "z166rQG9TV3BhVuDWE9tvqyqNuzzfxPaCw2cL7yvg50uit8Gn9" +
                "dWX43LVaHb1cLdB5CANFX4NUMsqDiaPyzJJI9SEer8ki5+oU3X" +
                "ccXv4n2RfHAkX013nYR34rsHDz/aK1VD2zFaT6EGC6bkjtZ7YU" +
                "hxIVl+Xd/kJy7/bqPnii8aoXA+p+t59I6t1OorjJa4MD+GYlVy" +
                "9G50qwfUlcp+ZKDCS8oaLdy9WxGNHc4LUb4++D4mqZKxFELa4I" +
                "xmHUm6hmUwxRcyWuEtfGytD1Yjhinadk0+qqADwq1dnwzu9Yvs" +
                "eAVZt1PRKgmHWktzjRRsuYZtt5Ujb/yoVvMwkBHaQQ51dutfVd" +
                "dqkl3pPqaJPBeCXFrCO92exxu5zDedl22ap1FZ5C5FYCj0ohbs" +
                "jVSmuuhlr2yHZ1tMlgvJJi1pHebPa4Xc7hvGybbGquxK3Ao1KI" +
                "G96yI61r8G9LvG3qaJPBeCXFrCO92exxu5zDeZ2nZavWxoqwSl" +
                "YAj0ohbqirFda6sjxjsGqzrkcCFLOO9Gazx+1yDudlx8hWnW9P" +
                "OPMe4FEpxA256mn4k/AYdbTJYLySYtYRt9Wyx+1yDud1npGtuj" +
                "vfEu6DReBRKcQN8d1uravBlmd7Rh1tMhivpJh1pDebPW6Xczgv" +
                "O1q2am10h1XSDTwqhbihrrobrqvR6siewh08EqCYdcRImz2Tbx" +
                "NH5eWG5YZhLyGJlZf6PaVSromi2gQIYOckyFJ9KodWUFK1pPpE" +
                "WYgSG/eGEVLPNAOqTe7D6XA6oK/OqEPeXq6WSRrwccTbXUgxKk" +
                "2tBfBZKsk1gE6joFK6LsXACmlnzTJgi+yYHeoMdf9cI6zdsOrE" +
                "mEyClxjrnMm05BJ3Gj8ztMRb5PnV6mbPr8ovKedXzcRY//lVJv" +
                "nzK/GQyIsJYlyIPwBQaWfdtsZbeJPEg03UjuU3enG/iVraEdPy" +
                "fY1HVVwfVsU7ydav828mxSsNb2JamKsdyVrOD0ozV630lu1WRy" +
                "9XB3Qewtluu5X6eNlulWeWRKoP8XhNFjlXp+i+4/K086uDCa+K" +
                "P5qo9tkW3sykvUV6mlvYLR417oOHmOy8+vZBRXJ7vq3xKBvYB9" +
                "vi7YPikSbe7dPDujqq5Ok5v+86otCeMJ23KxKPkyd9QTxbIyPh" +
                "/MRiZqmWpyUEXuR788bqLwbiMSb7lHhGwZ+vzq06XzE/oD0pnj" +
                "b5cd+q9uvc15U1WP211X2X5LHmbxPua1BX7hr3o5D6vvt2sd05" +
                "avS91mvrNdpGJhXzd2c32LvdD6k39w3Dc4n/u/MH7scRnueEuY" +
                "rxybdWXSmr4rJU98EUvOG5aPlswtEPSDVXKXgja/B8wtFfkmqu" +
                "UvCWm8uhZO2mo51s9LXWYOnzzP/mKi6sV6P0WZprULyX6Gznp5" +
                "srsa5F6+5Nv2EvaZRK5QCio6qjyko6Wlb1Va9Ii7Jrigm9YVPj" +
                "BLi4Vudwb7ZIouoqn2NPd0isyoz4u75mnmb9f9fHo2/481Xk34" +
                "uKDeE+eCFzEV+lb1I4k9kEUCVOzut4jyz9NdVc7W3J2zziDLkS" +
                "479/7N+dK23Kd+c1qX53XhPvu3M917i+cX3QAy6xykBJAz6OEq" +
                "IcwKhNtBY86f3cEtWgVgDncVGfKEvjka20T9fACKlnmoFo/6qG" +
                "/vmqWHDuSnYNlr5PdQ1+l7xNZ5QzKndE9t4uWYUkVhnvjHJf8S" +
                "lSCuTorVJ8C0BD6z5F3ihZ+gE5aF9C0gq9vWfyapRPnAFtdCZU" +
                "prgWOLngdAmiRUmMG2gSt9WV6L2Y98HikhR8LA2r+MeLOVfL2l" +
                "uwBmc4M6AHXGKVeyQN+Dji7S6kGJWm1gJ4J5XkGkCnUVApXZdi" +
                "YIW0nWYZnDPPgNk/1+Cf2ytTkn23O5vSrKtWeHMmO5OhB9y/3e" +
                "UVR9KAjyPeXl0RjEpTawG8lUpyDaDTKKiUrksxsELaVrMMzpln" +
                "wOyfyOScHPRVak7eXq7ykgZ8HPH2ckUwKk2tBfAeKsk1gE6joF" +
                "K6LsXACml7zDJgi+Qqp85Q9881sj3q6K3BiToPYaCw/8ex/BVk" +
                "NC/bo/LMkkj1IR6vySLn6hTdt43nTHWmQg+4xCqTJA34OOLt1R" +
                "XBqDS1FsC9VJJrAJ1GQaV0XYqBFdJ6zTI4Z54Bs3+uEX6PCv5m" +
                "wl0uDiT8ZtzWmrd41/A0vUXsgw8nnKtPUt0HW+LNfM7gvdtZ1Y" +
                "nf6jxnmEbPGZzdaZ4zmL01d87gLHAWQO/NbrYPSawyR9KAjyPe" +
                "KoVKo/UQ3sL1VPsSQj3VbpRP1FPaFrMMzplnwOyfaxjW4MyEV8" +
                "WGWhKVWWl6S/SsL+H/d3Y2p7oGN8dbg+JwIu+rRQnnalequdqV" +
                "/PvKsgYXJ7wGN6a6D7bC239EBPSm");
            
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
            final int cols = 75;
            final int compressedBytes = 2244;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXN2PFEUQXxSUqCCnoIjkTu8FBD8DySVA9G579tTEP8AHnk" +
                "iMvhwmJgqKmpmbW3aTeVBjokYNJmhMDA8YEZVgImoEQUCNF+TD" +
                "80BUCPKxCkqiYpye2pqqnu7Zz9nW2073dFX/6lc1Nd2zPbOXKx" +
                "woHMjlClErP7Ini18O1oAOx+lIxR/iEkdzNug7WzlSt0A9j4Kj" +
                "krZcQhaqujfk5vGRlO6fW3gf5djH68Ge80aupY+326wfOZ9r+T" +
                "NyusbYWZPWHL23Je7tqFr/1tS5hbnytoX1bW+nzJX3AegDtwHb" +
                "9+Pe3tq58rY6W1rPlbepxth2Y662NJKrkQvNROFMciZhizJIwV" +
                "Ogw3E6UvGHuMTRnK3a38yRugXqeRQclbTlErKwutmMoXPWM2D2" +
                "r1vElpfovWw+zoc5i5/OeEtbg6V92a7BtmJseg2undHY/ao4o4" +
                "2o4nt7sSs3gT+l/Z33MdwX52pmxtEfspqrPdlzilExii3KIDmv" +
                "gw7H6UhF1XA0Z+O+VDuVH3oqklBpPsmOVxm9CUPnrGfA7J9hxs" +
                "SYkrtIDtuusIyNvCb7VAFBLeCrFmMqh3ZVupJ4jkJJ5UVt6aDK" +
                "q8ajRobeOB/6HF5fc96MpenMZ8TW4OxsZ3B+j8012AlvYlyMY4" +
                "sySMHTYjwvZ944amkUiz/EJbDX2arRj3KkboF6HgVqTbZcQhaq" +
                "uoVs86N0znoGzP65RdqeIb92Iu8ZzNHre4am5tU6sQ5blEEKiq" +
                "DDcTpSCecVk5Jo3gufP9dzpMkCejyKJF6Pgs5Ardybys3PVs2A" +
                "2b9uEVtuwF7gp43Qxx+qPa58i//a8n1iQ/OjpYrNfXtQznYNFl" +
                "61uQbN3tpbgzW+B7uzvSaFN21+D9rwxnJ1Q8bRf9G6bfFGm95S" +
                "V/tGsTF/Etrw2yPqgRQEYqP/stQACnG8qBrJQDpklxoohCydpR" +
                "Hihx6wqMV/Jc0nnQGv/Ey4ndyL0tlSBtCjPFLc6nnUenYOns14" +
                "Xu2thwies+mt1Xu7MVcvZPsOudDGTrr5d8hmb+29Q3bmOfPgyG" +
                "VZg7doBCshofWHJF5lIM7Et/gfOAZsqp0pDsIkeXlMqjb29rvO" +
                "bopKzUV6dky2wyviKxMY77K3d2JuF29rAHNT3FsY1jvqrMEo+u" +
                "LNDfDOTx25JZG5bqcbjlyWtXSeRrASkuNVDXEar103elDtTHEQ" +
                "JsnLY1K18S55v87u1NkF6eMUlTy6n7qfqPcr96tQuy8c/9yN3i" +
                "26B8N6NKzHAOFGK9w9B2vQ/dPLeZMjy6kxxzTv6rR43MPuEfeH" +
                "8Pije1zxe7E3JTpeXpWne9d417rb3I+Z7dfuN9Xet2GN35K4J6" +
                "pH9qta6S/vsohnVjSyw90Vtl/WzpVreLLwLoraK72uMCMLnAXY" +
                "RhlcACXs7QYdjtORiqrhaM4WXyXNTuWHnookVJpPslPqbjMGud" +
                "jcWaCeYdK/bqHfr5ydE/l+Vfq7A/erhc5CbFGWRUwT00CH43Sk" +
                "omo4mrNV93zTdDuVH3o8Co5K80l2vOrekJvHp2bA7J8whXOF6M" +
                "7jzK1+f0Ry4ZwYFIMwAjJUlLmEDKghzsQTwiDHy9YdIM9JFsCp" +
                "TCRhPNLWnaNGht44H/qs892pjTtzuX2Nd32LJ/S7vsWdf8/g3R" +
                "2v+Au5Cfzxxzvw7LxdbMcWZZAGloAOx+lIRdUk0bzHuZMY7pXb" +
                "qbxpPsmOVxm9CUPnrGfA7F+3MLyTWZLtVXF+sjmvOuMt7dl5YG" +
                "m7z87lKVnF2Pyzszn69p6dxXFxHFuUQSpPBR2O05GKquFozsZ9" +
                "qXYqP/RUJKHSfJIdr+VLzRg6Zz0DZv+6hWEN3pnxqjhmdQ12wF" +
                "v/qf5T1EJPFtEn+qTMtVIDBTWElz0R5RlQxI59sub2HIcxkJxk" +
                "Un0SFqOkqntDbpBEn54BlVP3oc2r+Lei4J12r0Ow6b/bMxTvav" +
                "D3hbNt+MhXZ8FSod0dRZ27PYynocRSm7lqLNbwfJ02chXvRYN3" +
                "/z87y+I9TVsMNoi7t4n7VaW/Qi30ZBHLxLL+CiBQKzVQUEN42R" +
                "PLoiu2DGRkxz5Zc3uOwxhITjKpPgmLUVLVvSE3SBCrmgGV0+Dj" +
                "TP8ZaqEni5gv5kuZa6UGCmoIL3siersBqGocVTvSJ+05DmMgOc" +
                "mk+iQsRklV94bcIIn53LeJU/eR/v6qeF/G3+I/W90zdMDbwKKB" +
                "RdRCTxa/LPJS5lr/RRxFDeFlz38pmj95kJEd+6BP2nMcxkBykk" +
                "n1SViMkqruDblBEnk9Ayqn7iN+1/xZ9fn8aPxW41bjO+nTLd+f" +
                "V9R40/1PfXs3/sXPHQ3rd7XREL13VQO8qTPQveA9WHO/Qfv2B7" +
                "KdweVZNtdgR/4OuSIq2KIcld6wVKK/ra3E2t54tFoYPhqP5F6N" +
                "rTfq9+p2gCEWkBKsoaY8M80nnUGi9pow8Le1FJOaAbN/wlTfnq" +
                "4srJQt1XB/pfw2gQgal0f4WzWUDW9lV4JFNK+uq4VTeXQk8mAU" +
                "FAfXUy3PTkarc5nODNmwNLgGH8p2Buen2lyDNrwV3ov37Q2seP" +
                "43kHWjv8Rqrix4K+yK78VzJvL79pHJFp68Ho7n1aFs51X5epu5" +
                "Wnt/532w+9UjGe+k19rMlQ1vmCu/XHw04+jLVnNlwVtxdZyrxz" +
                "OO3reaKwvevGcwV8FYtvcrZ7XVXFnwhr95hbn6vrVcmX+jH57h" +
                "PNF6VH6N56PhK4y5quMtk9/o6W9rD2tRTW8rV6us5mqVhVzRGj" +
                "yS8aooWV2DJatrMNNfub2Dzpo2rJv+nyHteGthXjX9ZtF7vmb0" +
                "RavzqgPe8j35HmqhB1JwMt8DCNTyUTj6Q1yjcmIP+86TiOWMHE" +
                "cshFSZ1CgIi1FS1b1RhNwzz4DKqfuoMa9ONPs9WGdeDVudVx3w" +
                "5ix3lmOLsixhrn4BHY7TkYo/xCWO5mzVvsuRugXqeRQclbTlEr" +
                "Kw6poxdM56Bsz+dQvDnuFUxlflMavzqhPe/gUkjQXi");
            
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
            final int cols = 75;
            final int compressedBytes = 1324;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWr2LFEkU70C4REQEI8UJPE4MFOEM9FBvpp1mTdxA5IIDjZ" +
                "XTE/VgEfygZ3rWEgP/AhVEFAR1WYNTDDRRVzi94ILRDRRWMbzA" +
                "RO7g4Ka39s17r6p6tnu6uqyuoWpevXofv/5NVXf1dAdB/CQI4q" +
                "eDOhO/HLSN+GEwKF1x+e9AKck7VRP/PpReD6U/AkOJH7eTYOwS" +
                "Pxgx9tykNWeLHw2lF8VRtI+2j0ILfehhCzoYRSveQ2sabUk+r/" +
                "vx+FKiKKhVVk70Y/W82QaPWWfAnF/3GPLdkN9dEVgu7QuBw+Ii" +
                "W4VcxU65imvN1SWnXFWQLZwIJ7CVUvpJuUr7VIujZg2PCRLI7X" +
                "NgS/2pHUZBSx6J50RbQIlVz4YIaWbKAI+p5wgyr4M6r73rpa6D" +
                "Z5xeB8/kuw72rpZAdQ0ksSGocem+r2ANrgnXYCsl2ZN9qqWjJg" +
                "2PCRLIrX/AlvpTO4yCljwSz4m2gBKr2Kh6IEKamTLAY+o5sueV" +
                "7dL64nJeXXzg4Dp4pTKu/nXJlfjWJVfiuzqfr5KtLrmKrtSZKx" +
                "fokatwXZ25coGerMFNtV6D25zOq7O1nldnnXI1WWuuHKCHe+fB" +
                "GtxiNe58Ke/Ce7Nkt9N5tbnW82qzS67E1jpzlTT93rdHjfFHre" +
                "+vGnbtbHOVTIy869jukqvpnP+1JPu+0rxaP/6o9Xm13q6dda7W" +
                "jj9qnau1du2WuQ7+UOvroAP0hKsdteZqh0uuxM5a7xl+cjqvDt" +
                "Z6Xh10Oq9qfb5KfvZtL9o9nt9W/OiSK7HH7337MuhbTrlyeo/T" +
                "miu9DjqZIwXfzBATRXOXR1+Iq3m7a9Btac37vQab9/zhqhos5v" +
                "cZcvqy9xmaHxeljPcZSmEs/D6DxKLZlnqvz+q8WvBoXi14vgbv" +
                "esRVBVjEfotc3fKIqwqwiEmLXL31iCsHWOIR/6l2Vi2D70P2ub" +
                "2zugyq7oh/mTors7GMOM4xzu3N2fzzSrUtOj5+iT6XPS7vzu23" +
                "PVqDtz3n6o5HXN3xnKsZj7ia8Zyr+x5xdd9vrry6d562a2cqvZ" +
                "fDKM9qzVUF6KOpaApbKcmeOJC2VEtHTRoeEySuV/35CERBSx6J" +
                "50RbQIk1mVY9ECHNTBngMfUc2hq8WdkvvcLpvHKQLa7sDQ1x2C" +
                "VX4pD9mL0/lf7r0hFfZVyZ/nN6HawgW7Qr2oWtlGQvlcMFqqWj" +
                "Jg2PCRLI4QLYUn9qh1HQUvZNvogSsWPVPeB4KD7OAI+p52iebp" +
                "7GVkqyJ/tUS0dNmvDI0m+65I9WtMf9+QjmpLZmX0SJ2LGGR1QP" +
                "iE3xUX81pp7D5v4qnPJnz1AVlr3fpBVbqUul3l9SS+1Aot/Uh1" +
                "uAHiObPFRrHR/NqWJCZFhNKGh0Hi2Ll+yxstfB8DeP5lUlWOw9" +
                "xwlPZf/X5/o5jsSi2WrPcZJPBX+DL2nFVuqkJLXUDiT6TX24Be" +
                "gxsslDtdbx0ZwqJkSG1YQiCDo31BEzruXHFndHJf6nFse+xmrr" +
                "vTFi+aWic/v3acVW6qQktdQOJPpNfbgF6DGyyUO11vHRnComRI" +
                "bVhEI9LnO2bCRif3NusH9YrNhmzJtftd3xHJXkcyaz/6i4OXbh" +
                "c0XHzM+8ON7C62Yy/7ldnBh9bpfPmTx5Rv8237m94G/Wh4ptEd" +
                "9siVuKkyXmVb/o2MVGXrwF7gf7UR9a6EMv6oezaQtaHEUr3kNr" +
                "Go3m4n7UA/TcUmrD2eyc6Eer7gHHQ/FxBsz5qUfWGgw7hfdXHY" +
                "/2V50K1uD/F5ZShQ==");
            
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
            final int cols = 75;
            final int compressedBytes = 1513;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW82LHEUUHzxoRJH1IAwqLoj33AQvS/fOzGX/AkHU4MdF8C" +
                "AxHgJiembH9KAheFhWNDHoTTAxmmiMmzXxI5Fo8KrJIaKCpwVF" +
                "FPELnO7eN+9Vz6ua97qma2d6eK9r3sfv/aamqrp6erfRMI/+Vf" +
                "pu+b3GTBwuHv3v5Bm+nyc5P5QLQzmZXB7qxeSjbdyeIPfMqPUN" +
                "ZCRX2MgNH469ZxwcLrG9wrJPzo5aX1b81t7IBHVhK1qFlcZBi5" +
                "5pjhkBdkTmMsrR4/xozTInZIbCsSh/Lr6ai0n0IQjq7EhfmNzL" +
                "ECuJlMfq6vC+3vd+fC21zoCgznt1SZYrrSKP1dXhfTx7Hw6u9U" +
                "qUK66dbKRdD46ntOtV2nSvV9M4zOvgxDX36Z2/RvLXwfRAHbWi" +
                "L0BQ56O4LcuV2ChuVY46H88eY6uxiT4HQZ1/M6uyXImN4lblqP" +
                "Otbrpjq7GJNkFQ5331kixXYqO4VTnqfJbr4KYE0VHrEgjq7Iif" +
                "leVKbBS3Kkedj2ePsdXYRBdAUOfj6oosV2KjuFU56nz9Xe7Yam" +
                "yiT0BQa3IlNi2uDNPli59wx0KLjwu1v7LdD6Yvh9xfHby9jvvB" +
                "YH31yvz3VbQBglqTK7FpcWWYLl/8uDsWWnyctdZnIKg1uRKbFl" +
                "eG6fLFj7ljocXHWWudB0GtyZXYtLgyTJcv/dEdC630JxWPT0FQ" +
                "a3IlNi2uDNPlix91x0KLj7PW+hgEtSZXYtPiyjBdvvhJdyy0+D" +
                "hrrXMgqHOU+2S5dlv/YWqlsf2HlH11Tuvj2WOsC9FR6yII6rza" +
                "PbJciY3iVuWo8/HsMbYam+gyCOp81ftNlmtvmZG8Xc5R5+s/Iu" +
                "Ubfi+aPcdx70Xju0LuRdNfdnbf3lt379t7r2a69TzfV7y9rr6y" +
                "sDhb9utYRadAUGtypZEaXE0d3pf+5cdXctT3e/ty5f2VO9Py3F" +
                "lYLX1Tz6e1Yp6hlWm0UasZzWfzvrK1tWL6eFSsSTnxdQBxMoty" +
                "bamv3nHVerBRy8GPK2m1VDUro69BUOejeEWWa2+ZkbxdzlHn49" +
                "lPYjmRx1cgqDW5fCveX47U4Jb2lftdubyvXN/Odyb3V3cH3V81" +
                "69hfDef2UiaoC1vRKqy4v0IrPcP+CqNNT4bWWuIqm1VpZZMfrU" +
                "k5mSxRxlmAhUOz9UvZF+rvGTrvhxxXfDXfv2eIToOgzo72RVmu" +
                "tIo8VleH9/HsfTjk+R+AoM6/mb2yXPtBERC3Kkedj2fvw2H7+v" +
                "pfJqgLW9bq/1pYaRy06JnmmBFgR2Quoxw9zo/WLHNCZigcC4pu" +
                "otn6xe7LZ/Ti6KorGFez8DdFln3G3jpQbWt7Z5/vnoEiJBv934" +
                "Ou7ftke4bVnz1YLdZzj9N9qn1LdVY99W/1PtWsmOvtddDwHt6h" +
                "Bht4Mcp8h9EUjdYy80z8omVGFtb0B3tNzKMS7+Fj8DNvz9U9Jp" +
                "/x+uUMMq4Oj1B2z/V6tTvkejXte5z036D3OP/Uc49Tfb1S4F7r" +
                "eOwDkz+1GZ3T9Y/d+vpqcEPIvjr4x/z21fA6+G3I9SpEtbr2DM" +
                "m19tWQ48qn2gzMwRuDzsFb6++r1ZO1zYpDQefgoZ0dV8nfPnMw" +
                "Pl7PuDpwJ7u/Oj7PczB+O+Qc9Kk2jb7q3iZCYPei3YX4XY/7wT" +
                "scrNiVaVK12d6LDnaFHFeDmwKMq8O13aGdCHo/eGKu++qdoH0V" +
                "oFp9c7C9FXQvujW/fTW8xznmsbbrf+s7Nv3P0F5rry1vFbrRKF" +
                "rwrr3Wey2zFFEQR1+mpcgpbIieWYoXRg5uRg/iF60Chb6GffW6" +
                "rSZ+Air0k9CY7lvgWd6iPQAVszPyBhvEBBxXR4KOqyNzvV4dDb" +
                "peHQ3XV61mqzm8O5zirG81q+cO7p9OtWwOevWO7ZmX4H94Jzzz" +
                "IgjBn3mx7Kf7zOvFe7e/l4XWwvD7fGCK42oh5P6Kr+Y7rkp9Nf" +
                "rfjM5zvlj+CLNZO75unjkftsFS/k2Gy57si6+bPj4SrVnLxRcR" +
                "x71lS7m20/c/Jfv8oA==");
            
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
            final int cols = 75;
            final int compressedBytes = 1711;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0FoHGUUnpuXou2htOAheykI5tCTomib/TNzqCJucmgUqV" +
                "QPBQ8RhB4EhbDZzW6GYAOBarwo6MWWioKCSqB6slSLghSEkB5a" +
                "xHtRSS6KO/v2zXtv3j/bmZ2ZP7uZ4f/3vfd/733vf/vP7L+zie" +
                "c1v/e85g+99mXzZq+fan7rZT6a38TSL7F0y4rcMj97Ix/Nr4aM" +
                "/Wiz2tma38XSjfxZmLZpUw8SaKBzKx+F19Yit8iYKEm79E+OEK" +
                "fAXrf5UpaUO2vXkx6UIWcOn0mLqTmCjWAD++iIJNSoRxuOEkpq" +
                "hObRMC/tJ+ODJJHR2a3ZfLmGUaiFz9oxGJ1Wi8xH82uPeG1OiV" +
                "X3tVfaUSRWfl+7R7H5dJ7Ofr/qPFXkftV50uX9Knyu/PtVcDu4" +
                "TT1IoIHOrTRKKKkRmqKjHJ7WfjI+5pDkkZYkJ8+dWnfKjqE56w" +
                "rY2Ei3VP6jIWvwdKFrcNVzeLhga96vKnJYd1mrcKb8mMFqsEo9" +
                "SKCBzq181GaRMVGK5U8Ry/05jqIQUkaSnITFLFlTbJQhZ+YVkD" +
                "E1R7F11VrMjl2543RdnankqnO0by/0aR3k/RwMVrJ9Dtpx9sO/" +
                "6l/FHnXUqEcbjhJKaoTm0TiX9JPxQZJIQqVxkh9vQdOOoTkPat" +
                "WU+Wj+pEfaXrTE1btdyHs39zXYyIibK7Dan/cOxBF0ysX19yHn" +
                "zDnqQQINdG7lozaLjImStCf95Qhxcqzdl7Kk3KkF3aQHZciZAW" +
                "eLqTnUav/3gKyrVrm4/l2r7texRx20cAFsOE6vdEoLR/NonEv6" +
                "yfggSSSh0jjJj7fVh+wYmrOugJ1fe8T3qxcPxrrqvJAR18ixVn" +
                "eDXexRBy18GWw4Tq90SgtH82icS/rJ+CBJJKHSOMmPt/AlO4bm" +
                "rCtg52eYvWAP+751D87e/ewRsOE4vdIpLRzNo8VZKT8ZHySJJF" +
                "QaJ/nxFmVvw2AsVqs9OcMkv/bQ+ytzpMxrYvlwEe/W0SGRD1k/" +
                "349Uf51XtRddPuz/4bJWRdjGoFb3nNbqnstamZMqq4czRbg1Ft" +
                "fgyQdkeaOEWq3D6+z07LTntT/mY5Fl2AHjGpVmz/Ht7pW8Hna2" +
                "5U+yzmWfr8GLTq/Bi+XPwd/2t7FHPTpna7M1fzt8NbKgNbLIk/" +
                "AwHumA4tEie++drGk/wFAU0GTUyNK6kMZJM5BNs0U9rCvKSVbA" +
                "zk8YV+uqdx297nLf3j00yc9FV/4ukGP+3wffyPZctP1njmtwx9" +
                "/BHnXUqEcbjhKKa0GN0DzaYH9c034yPkg8C46yc9IMZNNsGJvn" +
                "hzg+wyQ/q1LDb2DftzbgBBl7tOEoobgWHCc0jzbI6rj2k/FB4l" +
                "lwlJ2TZiCbZsPYPD/E8Rkm+cnDn/fnse9b5+EEGXu04SihuBYc" +
                "IzSPNsjqmPaT8UHiWXCUnZNmIJtmw9g8P8TxGSb5ycOf8+ew71" +
                "vn4AQZe7ThKKG4FpwgNI82yOqE9pPxQeJZcJSdk2Ygm2bD2Dw/" +
                "xPEZJvmTHmy38miufU+O3wfNay4/B6tgM9NmmnqQQAOdW/mozS" +
                "JjohTL5xHL/TmOohBSRpKchMUsWVNslCFn5hWQMTVH+royCyW/" +
                "KwtO11UFbP4p/xT2qINmzoINx+mVTmnhaB5tkP1Z7Sfjg8Sz4K" +
                "g0TvLjTbNFffgWzVlXwM6vPfS+vX6zvOcMrX9cP2fQ2Y/6nKF1" +
                "v/XX8OcMPbZf1V747XwzbC+njixVfQ3q7Kt8flX/SdXq3dFrJX" +
                "+jz1ur/L/R6+z7M3inolr9Xmpcx3/PUG72ZTy/yr6/cl2rlZ3x" +
                "ft4+89n41GpYLmNRqytjVKsrLmsVLk/y/ardGe91NbxW/hcua1" +
                "WEbf+fi/qfu3wuamcr9n8TDtfVNafr6tokX4OO7+3/lYvLW6ti" +
                "vzuHay6/D3anyvo+uB/ryuy4XFfGxV50nWudx0t8ovTb6L6dO+" +
                "Ww0W/0432/Ci+5XFfdxyb53h6uO63VExNdqw+c1mpqkmvlds9g" +
                "3pzodfXhpK8r0zAN6kECrS/f5VY+arPImCjF8l3Ecn+OoyiEHO" +
                "gWX5ZlnDtrygPnw/OTFZAxNYd6B+P/MTGbnhdezlX3zdFHH7AX" +
                "vZB7DWxWv2cwG1rK72v9hnbec3hUwWbWzBr1IA20JbMGCLSKUZ" +
                "tlicdEyaxxrqS/HAE2jpSRJCdhMUvWlpIeGJvnJysgY2oOVbv3" +
                "tDTk2li0+1rfaad/z+CCzbyvpfy+1uyd/henCzb6PmhmvAk+XG" +
                "RvLmsp8zU41MNsOa3VVrm4/vE/r0WivQ==");
            
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
            final int cols = 75;
            final int compressedBytes = 1897;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW02IHFUQHhBEJaBiIHtyIUL2JkkOucWku6djogliAoKoBJ" +
                "egouzBkyaHJbMzmc0sAY8KIvESIQYkqKASNPEva+LPyYOXmKAg" +
                "wSAYA5qLxO2prfmq+lX39r87PbzX9fPVV9VvXr/p6elptTpnW6" +
                "3OuaV2unNhqZ/sfNxK2I4cj1s6H42kH0bSd1Zs58zg3VbhrfNB" +
                "iu+8ZZ1fY2I/GUmLtO8/V7yquQfzoLsz2bH+l0VrKhKZNWbwXl" +
                "Njlav6T5uMLJ4teQuPhcfQk0Qa6dIqvbTvzkiL5mSJZX+RsZJR" +
                "4sACZKT7i9KiZV07mpsNFcrMcgQ0p5uj80vsjP4591rya0y/lD" +
                "Db3y/7vnYuZ8fOT9ZwZkz4E+hJIo10aZVey7K8hr3K8UBFW7Cf" +
                "sTJe4sACpGbSOYHlKtHcbKgw0rqvLL+DHyZxujmam1fhpSbnVf" +
                "ls7tbe2t7KPeusoSdbcJi9QEktOBzphJJsy/PqsBun+UmSVUiU" +
                "nRNHoJubjblJi2pFVajHzS9GaV97H/dD6z56kcw92YKj7AVKas" +
                "HRSCeUZIvsS1UddeM0P0myComyc+IIdHOzMTdpVBPjUI+bn/Wl" +
                "M/GQfwg9SaSRLq3BHLx6T1IwN8w+x/FAwR6PlziuAXqcSecElq" +
                "tEc7MxN2lUK6p1OY0cB/2D6EkijXRp7b4Or96T1H1jmH2W44GC" +
                "PR4vcVwD9DiTzgksV4nmZmNu0oJZWZXF6eZw1s9cn7V5rtuDp1" +
                "oNblmzDc7kuGbY7e9GTxJppEur9NK+OyMtmpMlloMnGCsZJQ4s" +
                "QGomXQWwXCWamw0VysxyBDSnmyN9XvmnkjVj3E/ZqCR7jnf0VD" +
                "URsLJUpqrOa3WdFYOzTZ6DtVy3T/lT6EkijXRplV7LojlZYjl4" +
                "mrEyXuLAAqRm0jmB5SrR3GyoUGaWI6A53Rzp88o/mawZ437SRi" +
                "XZc7yjJ6uJgJWlPMy9x3uh0h8tcc/i/pQ8e9K8K1a5I9nXfzNv" +
                "LUVwPFa9Xb2girHqPZw2VoNv6hmr3iPmevV9RuadOebCBnpBLz" +
                "GvNqTnqYs5b0QR3BA7RS/oJY5oKj1PXcx5I4rgql6vUvPsGVys" +
                "5xxMuBf+Yw3HUOF6Fa5NXdvXNrleZc1Wpqoy16Jzu1PvlN3R5L" +
                "VoHdn8Lf4W9CSRRrq0Si/tl74PCovmZInl9hrGSkaJAwuQmklX" +
                "ASxXieZmQ4UysxwBzenmSJ5X3gvVzqtmt/zVFziPHht9f7vSGu" +
                "Nt/qUazsG9/l70JJFGurRKr2XRnCxpezxee5BTYu1YVIna0by1" +
                "8QhUKDMTzuJ0cyTPK8/5BXrwZ7734cgziZ7a7/t5tfx+nvTsh/" +
                "eAc4Slnv0oVWPuZz/c6odY59mPI29VMVbhy+M8Vm719lgVeU7G" +
                "+13vbYllabFYVtLinrQ4tuh63HotRpspLXcWn3dZ7y0fZC/hF2" +
                "Av5ZfhZJ93WftsJKyRlFYvGF1v3BLPne7L/qxa7/ky5+DC7U2e" +
                "g1mfVesVOQev6b0tsSwtFstKWtyTFscWXY9br8VoM6XlTvdln1" +
                "fG+7R6Pwf9bPPKxuX9HKz6HPQbHSs7W9lzsKlna71bjc6rW3Vc" +
                "X8XYRr+reeud6/Yb/9d1e+/ZFN9+c6zWW9bBX/Vct4/5erUt43" +
                "q1bTWegyW+210vMFbXq79u73/bP9f/rHOuf57Gqr+YZ16ZjBdM" +
                "68VSnF+k+L4yx+qhFRi/zoZLvX/15Gq8GzX4O/dc3F4tbojtel" +
                "30JJFGurRKr2XRnCxpezxee5BTYu1YVIna0Qb/xCNQocw8uJnE" +
                "6eYYj3lVYI0LqsWZ5/FPedC5/o9zW5Nj1US2uWmWFu4a53m1cG" +
                "f9OXAturCm0vfgxVLRM3kj5j+vfmySrxnCGwUZm75m+MOyrlQ9" +
                "XTP0r1Uzr8Id43wO1lN94v329jh/x7GrN+4z/Fbi8+OAK1XyuX" +
                "Sg2ehqq19xbb9nnNf2hbtruA6Z9qfRk0RaeMOfJgRb4QVKa0AP" +
                "670Xds6l4zQ/1xDPY8dKjVnQouotDI7ZHQErG/Rl9DtRQ082aZ" +
                "U4luRex2gs2cGs43VW2JJ4rZqQDU3XKY8yfgTxbMmVtK+0r3DP" +
                "OmnhLrKxH3u8tEWiJZvMpeM0P0kaCVRSTsTJFlVvYXDM7gjY+d" +
                "2I0Xo1eqYo3Jn3fO7fXEXXDDtrWK9m/Vn0JJEWBv4sIdgqvZZF" +
                "c7Kk7fF47WEWIDWTzgksV4kWVa8jUKHMLEdAc7o54lvvdG3f0O" +
                "5rcl7V8n+czf7m7Se4J4m0yLf9RGQhlPTaliim1SIb2ImFmPgV" +
                "XoUHOLAASUwyVsryCGTTERLDHsrL8TKjrEfqxnr1b23zamLc51" +
                "V7Y3sj96yzhp5t7AVKa0BLtuXV9rgbp/lJklVIVFJOxMnmZmNu" +
                "WZ8eATu/wGxqb+J+aN1EL5K5Zxt7gdIa0JJteazeduM0P0myCo" +
                "lKyok42dxszC3rg5acX0Y09fvgQon/eRV4TmYy23fnKu4zBOuC" +
                "dUtrzGR1YxXxNTdWdraSY/UfILtAHg==");
            
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
            final int cols = 75;
            final int compressedBytes = 2278;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uIHNcVHXswAq20jTdiskiM0CIKgsRg8HRVz8qLZBETSc" +
                "YO8UK7WRl/IBvNjJS2e2FDIB+BkpAQCEgjJGNi2XiRWYw1kmUL" +
                "7w1CXnghrTJaiginqm6fOue+96qnp6erMnIV79X9nHvufXeqqq" +
                "urmbk5v62em9vFtrY8OXbpb3Mdbm1ky57LnuNskmmV/KFa1Zuy" +
                "eE5ItfwhsBqvOLIQOdITsVJlXbuMKALr0fp8BzxnIkexcTbJNN" +
                "PVqt6Upc6eQSZXIX+ZZXG84siSZYr1Fi/72mVE2VihZtYOeM44" +
                "x9zYa3D4wxmewbe7vAbT2YY/2Bvryr+LsVGMqys3i/nwykejbI" +
                "MJYq/VUl3byudJ5Cd7qvGDMb7ryV4lq1/5uJa29t6rwUH0aqLY" +
                "XfRq6c9d9iqdbW+9yl7KXuJskmmmq1W9dlxbVovnhAR56R/AKq" +
                "PiyEKkZ/JVEIsqOeJsrFAzawc8Z5wj6vyj1j7F/9npM0ML2fp5" +
                "P8cMHRpn2OAlymtEK9uo+s/jOM9vklahqKacjNMRZwO31uc7kM" +
                "4vmBP9E5gr6wnbC+kzs8HPI/e1ZdUUrWx1VYKMI2D3SKLCWNXA" +
                "IuOzNAZc0qsTfoVhfsGc6p/CXFlP2V5It8wGP4/cvUXRylZXFc" +
                "V5fpM8kqimnIxz41YaAy7p1Sm/wjC/YE72T2KurCdtL6QvzAY/" +
                "j9yL80o0RStbXZUg4wjYPZKoMFY1sMj4Io0Bl/TqpF9hmF8wxY" +
                "a5so62QrptNvh55OYtila2uqoozvOb5JFENeVknBu30xhwSa/6" +
                "foVhfmKWHi49xFzdgR/aXjzjHjMb/Dxy9xZFK1v92RTFeX6TPJ" +
                "KoppyM0/H2gTQGXPKJ+dCvMMxPTH4zv4m53ErJtN6zZoOfR+7e" +
                "EqJVUu4Qo1k1zvM25WScjrL6FAZc7JWvJ84vmBv5DcyV9YbtJm" +
                "OGDV6ivObRKil3iNGsGud5m3IyTsc7P09jwFV/Q/yxryfOH0bI" +
                "s+jhtp4Ohz/p8ll0cLj9HPnoG15+JD9SrO+n6ikt4yLNP5qviU" +
                "fs021rp3euOLAeGeedtpqm9wyz/u6cHej0PcOBNt4zpK/BfCFf" +
                "iP4WCzuckwtpVJO91etjYW/+/+951XvU5XmVzhaeV2cf7dNrcG" +
                "Pqd5wbU1yDG5Ndg4Pv78tePdXp/eqp9t+Ltter/Jdd9iqdLXFe" +
                "/aj7XlXfBJ4eg/xknLeFd8hPj+/VJDXvWNV7o99zVgbPzz0W25" +
                "nkald/00aurq7Bbn/H6U14b+9tTF/V6qW578TW+2C2uO90r/41" +
                "W1z1FuvJ/pOYoZvW+9Js8PPI3VsUrWyay8d5fpM8kqimnIzTUV" +
                "afwnDNcQfS+TWi6X7Vu/VY369uzf75Kv82/zbWy9H7yjzQgTOZ" +
                "GmTyhJwhPxg0LmQxv2fSrDrUWvfqq5i9uao43tssvj/fn8dcnW" +
                "3ztpuMGTZ4ifIa0co2en/1Qhzn+U3SKhTVlJNxOt45kMaAS67B" +
                "eb/CMD8x+Z38jutjpZuNM23eZ3jz5ncW15Wj4W8neEVB87xpTF" +
                "iPr8y2xXW/Bo1orq3Jhhyze75avLR/7leLl1q4X32df40ZOjTO" +
                "sMFLlNeIVjbN5eM8v0keSVRTTsbp6M2nMVzz6L427+uJ80uX7u" +
                "Z3MVfWu7abjBk2eInyGtHKVvcqivP8JnkkUU05Gaej90QaA666" +
                "V0/4euL8YYScZ+uNZ+D6DmfoDv7F/079jnN9997ps43J9H7+Pm" +
                "bo0DjDBi9RXvNolYrPwZ/FcT7CJK0ilSfMyTgdb7+c4tA1xx1I" +
                "548j6sjL7o3ZN02enSITb9++mfrveXlcbDpvOmL6Gmb9niF7bf" +
                "98DlotM/4cvJpfxQwdGmfY4CVKtewtj1ZJuUOMZtW4dB7v5Qr8" +
                "yN5Kceia4w6k88cRdb/38Htt9sb+ec/QRi39Z/rP2FF1tUEnIs" +
                "TDkr3uOZvyGZvPnKqDmJBXa/LWulevx+zNVcXxYXeq+KP9o5gr" +
                "61Hbi2wX+keHL5YWWOklSrXsAtHKNqr+QhxnGM0Km8+ydropJ1" +
                "fgR5wN3Fofteb8gjnUP+Q6WenlyM6bBzpwtAIPS3ZeOaKr4rzi" +
                "GedjPG8aE9bjK0M25eOqxp5Xh5psFp9v5VuYq7vYlu0ml/9jAj" +
                "+P3L3FYohWqaj+ozjOR5ikVYRMqZyM0xFnK+fVv4NL7u1bfoVh" +
                "fsFs5puYK+um7SZXvdqElV6ivObRKhXVX4vjfIRJWkXIlMrJOB" +
                "1xtnIe9aquiVpzfsHcy+9hrqz3bC+ynTEb/Dxy95bsDNHKVlcV" +
                "xXl+kzySqHROrsCPsvoUBlzSq3t+hWF+wdzP72OurPdtL67RZ8" +
                "0GP4/c15ZVU7Sy1VUJMo6A3SOJCmNVAwvH8OU0BlzSq/t+hWH+" +
                "OALb4M367rj0OP+O00b1i9uL25xNMs10tao3ZfGckCAPXwVW4x" +
                "VHFiI9k89JLKrkGBwOI1ihZtYOeM44R+947zhnk0wzXa3qtePa" +
                "slrqd0LHIZOr+Et/CqwyKo4sRJZ69qlavOxr54izsULNrB3wnH" +
                "GO5u/Ow9+H5+C5vz4+v3mde2Wy787n9vJfjbv6Prib/6PPrk99" +
                "77l+9tfN3rO/mm22bt7J7KvfUic8r9K43faqd7C9Xg3/0HqvDk" +
                "7Wq9+env4vuPqftq7B/JW5DrcusrXYq1902qsWsvWO9Y5xNsk0" +
                "09WqXjsWzwxi8ZyQIOcvAquMiiMLkZ7JV0EsquSIs7FCzawd8J" +
                "xxjvxB/gBztaIHtpucXSlnWLMr8BKlmqENpWylvfhkuhLHGUaz" +
                "wuazpGJVAwtHHIH1mGYScKwnzg+9mLfzbcyVddt2k7PL5Qxrdh" +
                "leolQztKGULat+adFoeAyjWWHzWVKxqoGFI47AekzLRr/+oNrm" +
                "/NAT96v6v7EG7xZMF90zy8UdnoMuplFN9l08YV2cTYS9k1H/Xq" +
                "pa+Utbd9vB77q8t/e+N1sct6XNpc1y5iiegf6UQtAfH9O89I7D" +
                "eX+MBA+qYB1q5xj+Maw25opXRjbsLvB/esa29w==");
            
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
            final int cols = 75;
            final int compressedBytes = 2177;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXEuMFGUQngOJLwRkBRSjq9msF2LCxRhPS0/PJB4IEk/eST" +
                "xoOHiCG852h9kdDxp3BfHBKhEf4AMfiSIRvShi0MSTxmRXvfg4" +
                "cdSoB/vf6tp6/NX/9OxMdxZiT+r/q7/6qurrf3p6emYHGg25dY" +
                "6jF12nQo3DU40htu5co8bNVz/81t7X3kcjeLAH+xzlUQuRNdGT" +
                "uM6XEerJuXYuqSTtZNFWnUEKeWfgWTX9Hnqbvm/lPDjWuIK32T" +
                "/L8XoLq+/B1urFEZ/BH9S5VlV0a21rbcMR92EvugcwjNNMD4lw" +
                "Nq/Ge8k8WR88ySRWUU/K4+bUWxw6Zn8F7P48o/NZZp9ndqZzsd" +
                "FINnU+zl/Jd/Vf6c5HK963K94lk3lumGe0EzhLOl+a13ZTfefs" +
                "indhVTrEWnXGV9Zq5xW9VjurWCtVbbyyd/FTtd4zVNKt6Lwy7p" +
                "DeHOa8ik7Xel6dLndedV9fxfNwVM5WjHyLFcLDseiojNlMQp0X" +
                "0ksV/ahGdO8yseiInK0Y+RYrhIdj0REZs5mEOi+klyr6UY3o3m" +
                "Vj1V6vemfqvF51KzqO5l/OaATMeekCoJyHHp95jmQgTpWtDM3W" +
                "rO4XvKfWRMrILBXZnfYJHbF1Fcfia53RCBh4gHIeenzmOZKBOF" +
                "W2MjTb18d7ak2kjMxSoY/L7hZS0mjs+tkZjYCBByjnocdnniMZ" +
                "iFNlK0OzfX28p9ZEysgsFfq47G4hJRn2vjMaAQMPUM5Dj888Rz" +
                "IQp8pWhmb7+nhPrYmUkVkq9HHZ3UJKMmzJGY2AgQco56HHZ54j" +
                "GYhTZStDs319vKfWRMrILBX6uOxuISUZ9oszGgEDD1DOQ4/PPE" +
                "cyEKfKVoZm+/p4T62JlJFZKvRx2d1CSsrftw/3ebD3YZ337fY9" +
                "w/CfB5v/OKMRMPAA5Tz0+MxzJANxqmxlaLavj/fUmkgZmaVCH5" +
                "fdLaQkf3/cLWf03EgYRyXbzrZjGo13y5hdlXpyTXYfrNhfhe5d" +
                "JhY/JGfbQ99nh3KKELclcy4iY3ZVWbtIQVbxGarYX4Xu3S+26y" +
                "dnNAIGHqCchx6feY5kIE6VrQzN9vXxnloTKSOzVOjjsruFlNR2" +
                "bT+7+mt78vhaubZHB53RCBh4gHIeenzmOZKBOFW2MjTb18d7ak" +
                "2kjMxSoY/L7hZSkr0up5zRCBh4gObP7lH0iSWe/eeILSOuWmz8" +
                "JVZ35Z2lPt6Ta5IqyXwViFjVCq5XRiw64IxGwMADlPPQ4zPPkQ" +
                "zEqbKVodm+Pt5TayJlZJYKfVx2t5CS7Br2hzMaAQMPUM5Dj888" +
                "RzIQp8pWhmb7+nhPrYmUkVkq9HHZ3UJKsnOt5YxGwMADVHIJ4R" +
                "HcTw76EVdNc6mLjPlM3ZNrkirJZA5nwpwcKOoWUpLjD8g5mQHP" +
                "jYhRXLN1FT9iIVRfxuyq1JNrsvtgxf4qdO8ysXiPnNFLHuMY+j" +
                "7bzrZjGo33yJhdlXpCTqgPVuyvQvfuF4vvdUYjYOABynno8Znn" +
                "SAZFJCIzeMxn6p5aEykj81UgYlUrWCsjtmvRGY2AgQco56HHZ5" +
                "4jGYhTZStDs319vKfWRMrILBX6uOxuISUZ9rszGgEDD1DOQ4/P" +
                "PEcyEKfKVoZm+/p4T62JlJFZKvRx2d1CSvLzba+cbQ99nx3KKU" +
                "IQjffKmF0V0eRJyAn1wYr9Veje/WLx/c5oBAw8QDkPPT7zHMmg" +
                "iERkBo/5TN1TayJlZL4KRKxqBWsViC1/psw/c8ab482Nxuyzo/" +
                "srpKtX32Z3mz4xbN3mZWc0Aua8dAFQzkOPzzxHMhCnylaGZmtW" +
                "9xLvqTWRMjJLxfLfUi/rI7B0FcfaTzujETCOch56fJY5OuJwqm" +
                "xlaLbNsve4MjJLBSqRR2Dp6h9zW+uY741iG67a4NmtGn4Z3Ho+" +
                "n0+iN9q6dWWPVn1Bjxd8b7R168kerXr2Hd2ry+Mbycv+b4oS9r" +
                "u3zt9e5n61v/Jr8eR48s6Kfyo52fmxoHf2zpSo3+Al76lr7PlO" +
                "4NfqT2xnma/l89vN8wx9pTi792nf1XkreTd8zzDqbfrR5uwQz+" +
                "YnA7+3z1bwmptvzeOI++4Rj8Vjrfl0wSGIUhQfEoF8v1p+xzPm" +
                "5/EMxLkKRHvnintSHje/mxvh/oo0yRWw+/sZVZ9X2TvwyTrvRa" +
                "vo1tW/bnhqoNfG/vLc2bvrXKvW11VULfr7YM/7C1r3Ny93zf5b" +
                "gJlrTK7398H01yFUjTeuiq09PVpe+LxqbiqRu2bPK1v9aM6raF" +
                "HOVoz8aDFcZbBYtChjNpNQ54X0UkU/qhHdu2xMvgZ7X432/qre" +
                "1+DswxW8rufaczjiPu7RiBhGiSX3iM2r5Wt/0c+T9cHjKjirqC" +
                "flceuO2xw6Zn8F7P5+hvdd30Q8ob/rc0jw+7UJm1WED3Bv8sjA" +
                "3/VNhL/rW42a9nx7Hkfcd494R7wDMIw7DKP4ID7E3T6weDWHZ+" +
                "p2+HmyPnhcBWfZPekIpPndsDbsgSa5AnZ/4hSeV5PxpPteVDxX" +
                "k32ey0mbVYSX33qXBj6vJvucV6tSU9dv1ZrrVr9Wh9sD3zOsK3" +
                "fPMMx2+MGr4150puT/ZTFz/RB3fcerUh/9W+dale02s/H/zzi9" +
                "byr4PD7VmsIR92Ev+g4wjNNMD4lw9vLnh2nCee3WVHrIz8B6kk" +
                "msop6Ux82ptzh0zP4K2P0ZZ6m1hOMyugSPrNsPgGGcZnpIhLPz" +
                "tVqiypybrZWXgfUkk1hFPSmPm1NvcbAWW6sleYS6P3HSOzK7Mf" +
                "8ceSd/DUbe+9n0BvTSLTqWbpPvg+kNbq3y2MZ007T4LJsess/y" +
                "9LbMbjfuRbcU8G/OVK3P5lx1elM+38rVp+uXxw3iWnMhHUu3ln" +
                "nlpbek20d/vYoq+8zX+37taLG+F22+NGhuOGPwesNslXT7DzFi" +
                "7i8=");
            
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
            final int cols = 75;
            final int compressedBytes = 1610;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtGk1oHFV4oYoBD9KL0FVIhCjYGtZLwUN7mJmdqhUVsQna2k" +
                "MDFg8GDz0KNZudtGv2UC85arRUKzbGag4aFauhKIJ6aE/x0iDB" +
                "owdbEATBnbx9+33fe9+bmdd5M9kRZ3hvvvf9v2+++d6b2a3V6N" +
                "E6JyG/By18U7M4/HOJ1C9rJR5FWGseaR6RvRzLEfQSJ6nybM/g" +
                "EebG2rAtqonqFxDlBC5VFo+kFmgLmzwPzFmPAG8f8Uw1p2S/jZ" +
                "0Sp4DjvJJ0uMJJMUJG19a/02u6HJaQeOwF5jLZBDncdGtSN/YP" +
                "Rmb7iGeyOSn7beykOHvWWgIn6XCFk2L8FnBjbQOvNDmqX0CUE7" +
                "h4mzAD2mLvOR6pC8Vqks5QtY8lwlsg13k4vBWPBQ56wFFafKV4" +
                "HZZH8LXkpxpUPTAGXo5HwrN1szXwFs8AHwtbyTUKy0fPRSGmRU" +
                "/ZVLv2TFbO6Gn/6m3X6avRIXsZ97U9jlX0ZBS4iFX0eFKsghM5" +
                "vEyIVXSYw2a1Fj1RVqyUfL03KVZJVPexymrNxqtwd7gbegGJkR" +
                "hjLFDl2Z7BI8wN2iUcPI85dQnwQbVDMZRKfYemW5O6sX80Apw1" +
                "GOux6+wnT/2KecTUiBWey4S3qD4rbiQAKyE7za0rvfZtr11u/d" +
                "jrR1tfCHz3jgyynw+gXwbQTyznV3mqams1gfY9h+3uYnnXBtAP" +
                "+Wv93CUJecdrFT6K9N7boleOBrC3lazFjuZtURrPCdgYSvIXNO" +
                "pUFaPaTvQr8iLoBSRG3TvjHmMxlcNQnRKieFWeUqQW4KSaqE3g" +
                "lV5C6+5SJcBDbBlHgOrUbZjrlfeIGuf5Jbt6NX/cVK/mjxVdr3" +
                "Tv+Xo1/7aL2u6NVTpWY9lideaki1hlkh3addA7kS1WXo63ibkX" +
                "a/+Jo/NbNr7uiIu86kwOT16dCW3zqjNVzP6qfWG7/7D9bhwrhX" +
                "YRWfo7VdOgorXfaQ92xO2P2u+3fjVInO+1SwruU22Of5ltztaR" +
                "5Af968feQYR9L2EnczB1TsvtTwy5O6i63fuq/Aye/a6E5/ylQa" +
                "zur3KsFu4pcN++SK8cDWCOKwmfTPMWKY3nBGwMJfkLGnWqilFt" +
                "J9NMtT0Mq7xn4L3Xa3v0u8UzN61oG/zGFx6u8jNYhvfqOpiyRs" +
                "wMa6y6Y//HKnOsHigkOlfcxoqvV/mO6I8E2k02Vg9mrFd/5s+r" +
                "YE+wp1Z7c9HdfGN95R28tbnzefX6F+IGvcBhLOaTEL5SGcor8K" +
                "CZylOrgDPp5XwCa9Con3iW6gxUa0meGL+3T1R5z9C5y/2eQbNx" +
                "ahgr9Zz1GhJGbvmS88o7UOnvVweKzyv/ZR1yceTTZi/t1nvDCn" +
                "y5v47Ug3pvtORwZarn2C095MZa/nWQf8fZvjurDjNrtUxZXsLl" +
                "fHq1/fX+fZkIJtT9VZCyMgq6zmXCW+x5T1rn1URyXuXxZlAT13" +
                "dG1vk3pvXifYb/vnVOO35De6yYqMyylalTwG/0zUazIXs5js9g" +
                "PBgXOEmPcZIqT+AX9HgsuLC2GN/L+nFdjuoXEPYCc/E2YQa06d" +
                "akbjESPtEI8PaBR3uin+0/z42goa6DQSOlRjR4LhPeIicfta5X" +
                "jZR61cifZ/CdwX/BbQa71rcT1oz79muV3rdfy7ZvdxOrTLLD++" +
                "78Vpmx8jYqnVcbRcRKqYlrGPI3HVaQzWJkweN0ibzzMebVdW01" +
                "2m+XV9GcKa+iNwrPq+vF5xWsg97PtQofxXhfVm33b/ttwl+3zy" +
                "vemp5XnaVhjFX3UI77aR2rs6PZYmX7rc/fiBv0AoexmE9C+Epl" +
                "KK/Ag2YqT60CzqSX8wmsQaN+4lmqM/A3zHFRaaXl1TPDmFdWe4" +
                "TpYFr2cixHwbR/I+4lFqjARUeUG0O9u3RDl6MSAsJeyJOTxSOp" +
                "BRqWAM54Pni2NAK8fSxh3DOMuM2rkveiI6Xu2/dVOlb7iqjtxl" +
                "jt1faiR+1itaP/b9+bJVbRP/Z5xe5F706XHd7/fvDeM/uri07y" +
                "atTmGUzlLPsZTFkHd3LfPnSx8rLFiuezjVX3WJVjlfbfj+F+Hw" +
                "yOlhkr3hpTrz5zEavwVN5YdV/ZubzivS/qm8z2XnvZPGLen5Z5" +
                "LhPe4o112Y0EYCVkp7m098FXq/+dobRYvVb1d+fav7wX4l8=");
            
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
            final int cols = 75;
            final int compressedBytes = 2225;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE2PFFUUrQQM0Y0mkEjEBBaGkZhMgIW6YdFV1brwJ6g7YA" +
                "Nk1BhEjUAzg0xPL1gZE78lcUsIxqgxRkXRmCgoOsaPKAkQhoQF" +
                "C92ws1/funXOfe/VTDfdXTDRenm37j333I96U1VU1YwmSeuzJG" +
                "l93p0nWt925frWR0nfW+vDUjtTat9FmZ+kZ5Mb3lrvL+L7OobG" +
                "q7U+LrVvZD9zeYiu1puKHyQj24bJNXhsPGLY46k6rzrPjPa8Ov" +
                "xPnefV7KrRn1dZO2urVFstSMXUq2N6D1s+mzXO7XO4KsfZvH4s" +
                "W5oFs/NUnINjDlcgXp8417JrKnvoNRmiq1QMXpndtTIIcnA24H" +
                "4mjuAsaod9cRfgam7MztN+BDrkyrwC1fUL30K2oLKHLhRjsjsW" +
                "BCnRSfUqqp4icrK3nwyyEY54sFBVclIXYV8LVscReDOoprmL+E" +
                "mujX7i9WWfP5g/qNJtTlMLUjH1gmUtsDlbcf97Noyz+UXjLphV" +
                "VRNxPI+sj3M0F84r209YP4wo73lHZX+oNXsqWcbboRfHX6P1qG" +
                "qNn5fzWo2n+7qeRTvP1fnM0O7zmeEWXasDda7VkfXLeq1mlvta" +
                "NfY29kKKJpbT00uMsjeG2JyqqZ5eUi7HMw9ZwBQ7Fosu0TtmGK" +
                "HHw/3ZFbA5wxqLvOO0R3teDXVPHfi86qzo77w6fNeNd5Xfxlp6" +
                "cYTvzhfrjI1HDHs8dd2v8jvqPK/i1Ya7X+XdEdp5oasEprpY03" +
                "vyUs8rcxZXxQvqy2mPKjZLXo5Yr7mZjJbVng+z50uuRRVWxK/M" +
                "V6pMClstSMXUq6O7VmQxm7MV712fMjOMUJy7YJYfy5ZmwQyraW" +
                "7uLzErEK8PTnYhu6Cyd0QXZIiennBS0fSEesFiS9jC4mwO794r" +
                "ToRxwuGqitkqsVi2NAtmGKHHI5ZoykM/YX21I+9RD5Vfy84tfQ" +
                "VP7+n/au+8Xuc7Tue10efMTmYnVaotVmdOMPVjj2ERn80a5/Y5" +
                "XJXj4nX8mojj2VkRy8HHHK5AvH4YUUYeL5/RnqjyVKz68XGdI4" +
                "tnjnv97sexzf5aar+N+Bp8s9Zr8I0a1ur3UvtjxN2/Vedaxd8H" +
                "hzy/T2enVaotVmOTYOrHHsMiPps1zu1zuCrH2bxVNRHH03Uf4+" +
                "CYwxWI1yfO1eyqyh56VUb3PDgqmPqxx7AIszlb2VUQZ/OLZplg" +
                "VdVEHM/ZVXGO5qK1umqP0K8PTst7bmj9VV4zr/b5/uG9Y7X+HN" +
                "s32/P9c+furOF+dX4Q9kD39ndrvbe/M/4ah9OxdX9sud/bg/Pq" +
                "UnEVr8nWJEn7lRH+K7KmzrWKVzs00p/X7OWxnVfvLffzqrG1sR" +
                "VSNDeyjdnGxlZ3XgF1iAxFwHdatrH3E90otma3uB/PPO0BtnKm" +
                "d4ax6uPeMcNqTsp5hZ7sCticYY3qb32N+eBe9vZg3/pmDlV9Q5" +
                "4Z8Hc6g3/rC7uv+IY80NvE6L6LNnbdOt/bpZel1+rlnQPcA//O" +
                "/laptlqQiqkXLLbSXWBzNq5l42x+0SwTrHhNHIGd6a44B8ccrk" +
                "C8fhhRXmdT5U/m/qGfP56s9Dw+7nv78N2HW3N7czukaGKJzSh7" +
                "YwjnbBxQzeJ+vPWgJnNdrjAWXaJ3zMYBPwIdcmVeAZszUmNHcw" +
                "ekaGKJzSh7Y0hZvWs3XlLN4n689aAmc12uMBZdonfMxkt+BDrk" +
                "yrwCNmdYI1+br1XZ+wq/VkZXOyuY+rHHsAizOVv5W4AgzuYXzT" +
                "LBqqqJODPPxjmai343sdYeoV+fOPP5vMoeOi8jW5ety+fd85X6" +
                "scewiMSH2Yp76LowjiMU5y6YVVUTcTzDak7K8xV6glVdnzhT+Z" +
                "TKHjolQ3SViqlXx/QetpjN2Yrn9lPMDCMU5y6Y5ceypVkwO1/E" +
                "OZqL1mrKHqFfnzhz+ZzKHjonQ3SViqlXR3etyGI2ZyvW6itmhh" +
                "GKcxfM8mPZ0iyYnS/jHM1FazVnj9CvD066Id0AKZob2eZsc7rB" +
                "XYNAHSJDEfCdlm3uXWubxe799q2IA+7HM097gK2c6Z1hrPq4d8" +
                "ywmpNyDaInjvdzhjWCJ9vinTObyCaC59aJJZ5rJ+KsKnys3xkm" +
                "hvPHtvSR9BFI0cRq/OIko+yNITanahb3461Hs4BpM9ma4GqXmK" +
                "57G4EOuTKvgM0Z1ljk3TlY+fZdg70739T3wYn+3gcH7KNirZqP" +
                "Bmu1evmsVdj98GuVrk5XQ4omVv5juloYirI3hticqlncj7cezQ" +
                "KmzWRrgqtdYrrubQQ65Mq8AjZnWKP6vMp/GvSbzPjOq5ebg55X" +
                "8e6H/Fu1K/kVlWq7kW3JtgimfoepVwf44ne2sDibw7v/8mwJ42" +
                "x+0bgLZsVr4gjsDKtpbrGkJ7sC8frgNPc190GKJpbYjLI3hqS7" +
                "i3tFEQ8WWzbeelCTufFYdIneMdPdfoTm5v443s8Z1qh6vhr6+9" +
                "HBEX6LOnhz65f/tt3tfYU7M/QXvu97ve4f4VrtrydmyW99Dzcf" +
                "hhRNrPwHJxllbwyxOVWzuB9vPZoFTJvJ1gRXu8R03dsIdMiVeQ" +
                "VszrBGcJ7dU+Z4LFnG2zi69//2o13er9ob+szg/e1H+95xHX/r" +
                "/K3188D9Kj83zP3qZm832v2i5+q25jZI0cTKf3aSUfbGEJtTNY" +
                "v78dajWcC0mWxNcLVLTNe9jUCHXJlXwOaM1LjevK6yh16XIbpK" +
                "xdQLlrXA5mxlV0GczS+aZYJVVRNxPMMIxbk/WNX1bUR2u5uQgj" +
                "lt5m1Bmaca7znGMhRH5liEz/ZZ7fu4pt8TOsOMdeH+Tsb3xPuq" +
                "8tX13y7V+52hgjv0/08mPeYmpGCiCco81XjPMZahODLbeFuVK/" +
                "tsP8b32RnrQjsJs1WtS7ro32u1J5L/tz6/9bU3/beuwYG/4/cm" +
                "ZFJq3Xt74Qcv8RDOEzIUzzyujcgCzLLaDyReZ7aDLJixLnr39u" +
                "AIskXWxfP9C9WAelY=");
            
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
            final int cols = 75;
            final int compressedBytes = 1873;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7+LXGUUfSi2/ojgYrVdQINgk0IbmbdvArrKsn1IYZFCTR" +
                "P/gTebyYYpgpGQlAo2ZjvBwl+F0cIQEX8gSDQJaBbECDZCGk3h" +
                "zLved855330vMxs3brLzDd+d++Oce+5+Ozvz5pFk2eDTLBucH+" +
                "/3BhfHdnHwYTb1GnxQe1/X3lch8pPRU9mW1+D9jtoXM/T5qPYu" +
                "2PPw15nmuENnlWV3/1llWf7HZMNabuKNnrYs49zjZ+YowvPoHD" +
                "Ga6Cbq2GHWbM6EybCjKbJs7Z1mJZ7r1rXqvBez+epY/TcnG9Zy" +
                "nGWce/ysnGZlkkfniNFEx6g44smwoyl8Ev0Jornaarv7/WqWVZ" +
                "wtzrr12CNYz3kVKI2A5m6spTztb54igWrTBI93yvA8z6cnEOsT" +
                "5nRx2m2VPW0P8916zqtAaQQ0d6unSnja3zxFAtWmCR7vlOF5ng" +
                "9Ru37K8DV6YnveE9deuR32sY93xjv7/Ppqy1Mt7sTX1c5Y+cX8" +
                "oluPPYL1nFeB0gho7sZaytP+5ikSqDZN8HinDM/zfHoCsX7KmF" +
                "+LTr96V/Q5qsGPUF357lrvitZiJLITr2tedEyrzUxTu6vWP9M/" +
                "49Zjj2A951WgNAKau7GW8rS/eYoEqk0TPN4pw/M8n55ArJ8y5n" +
                "+Dt17rz05/zbD+zO7+jjM6MP1Zjfq7+6ySbm91vAaf2+X3Y0b9" +
                "Eax5FlnMWa5GGe3pnuabfK1Ak7ExF1NiduyUgQlZmU9Ae6YaxU" +
                "ax4bb6lrhhD/Pdes6rQGkENHerv6UmPO1vniKBatMEj3fK8DzP" +
                "h6hdnxl3w/fB9f7OeL+an9XU3wcP5gdhzbPIYs5yNcpoT/c03+" +
                "RrBZqMjbmYErNjpwxMyMp8Atoz1Uh+gyvza86pX+3zs2q/Zij7" +
                "Jax5FlnMWa5GGe3pnuabfK1Ak7ExF1NiduyUgQlZmU9Ae6Yaxe" +
                "Xistvq0/GyPcx36zmvAqUR0Nyt/nROeNrfPEUC1aYJHu+U4Xme" +
                "D1G7PjPm95CnXcXV4qpbjz2C9ZxXgdIIaO7GWsrT/uYpEqg2Tf" +
                "B4pwzP83x6ArE+MEs3l27y2VlsOVjk3EfkPvo0ezb7ewdV0S5W" +
                "106sypuzjG52b58q4muXyXO+L98Ha55FFnOWq1GmvmLZ5z56IV" +
                "K+VqDJ2JiLKTE7dsrAhKzMJ6A9U43iaHHUbfVqO2oP8916zqtA" +
                "aQQ0d6tf7QlP+5unSKDaNMHjnTI8z/MhatcHJl/JV2DNs8hizn" +
                "I1ytS/qRX30QuR8rUCTcbGXEyJ2bFTBiZkZT4B7ZlqFEeKI26r" +
                "EzxiD/Pdes6rQGkENHerf4MJT/ubp0ig2jTB450yPM/zIWrXTx" +
                "n1Z+kb9Xmfmvm75ansHl8z3EN++Haur3p/38nrq1htN9yT2b5r" +
                "0dHh+VlNe1brh7c+Vf5k6s3O/f/XdszSP9k/CWueRRZzlqtRRn" +
                "u6p/kmXyvQZGzMxZSYHTtlYEJW5hPQnqlGvpAvwJpnkcWc5WqU" +
                "qX+nC+6jFyLlawWajI25mBKzY6cMTMjKfALaM9VY2lzadFt989" +
                "m0h/luPedVoDQCmrvV360SnvY3T5FAtWmCxztleJ7nQ9Suz4z+" +
                "DfmbvDGJLQeLnNYc3+yjPZv9tUOzD+M024y61IoXdVr+CWZ6j7" +
                "rByuXn8kmxWH47zv1QVS5V9qfxHp/q6DVDlH9Wn7X//ju98q+B" +
                "X329Sl0ebdMufy5/Ka9V3m+ie//gAYkfHDw2WCjPl58R97vye4" +
                "rq+yrl7+0/66i63i4vlF+O7Tdb+AS+r7IPDR4Z/yUu58uw5llk" +
                "MWe5GmXqd4Bl99ELkfK1Ak3GxlxMidmxUwYmZGU+Ae0ZaOzN98" +
                "KaZ5HFnOVqlKnV97qPXoiUrxVoMjbmYkrMjp0yMCEr8wloz1Rj" +
                "uDrsD58fLjlzuHwb1x+Pt9eGL3VVb7WGBzpqL8w6y1ZwFXZPfw" +
                "+seRZZzFlUgdIIaHR3n7FcUVXPpX3bNHl27JTheZ5PTyBSQzx+" +
                "dR3KD8GaZ5HFnOVqlKlf1YfcRy9EytcKNBkbczElZsdOGZiQlf" +
                "kEtGegsT/fD2ueRRZzlqtRplbf7z56IVK+VqDJ2JiLKTE7dsrA" +
                "hKzMJ6A9A43VfBXWPIss5ixXo0ytvuo+eiFSvlagydiYiykxO3" +
                "bKwISszCegPVON+n7PdX2OPfc5I3eNrk8XNStdPM/oPOm8Uce4" +
                "U5f2NLXeNX2OavAjVFe+u9a7prUYiezE65oXHdNqM9PU7qqNXp" +
                "//X8tZV29Tn6Ma/AjVle+u9Ta1FiORnXhd86JjWm1mmtrdtelf" +
                "V8ff3u33Rac/qxMb995ZnXh361Md/3F77umOBtt1t/j4pTt1X7" +
                "q31luDNc8iiznL1SijPd3TfJOvFWgyNuZiSsyOnTIwISvzCWjP" +
                "VKM4V5xzW90jO2cP8916zqtAaQQ0d6vvvyU87W+eIoFq0wSPd8" +
                "rwPM+HqF2fGfN/s73VtfZyNl/T/gYvzc9gflb/wfoHs2FMGg==");
            
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
            final int cols = 75;
            final int compressedBytes = 1322;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWL1qHFcY1Zu4EH4Ag0UKd7ujl5BRJaxCjZ5hjJFRYVK4T5" +
                "HGThdIEYVAIkJk48SkCaRQpyJWkQTyAoHI8+XsOWfuvauZXe2C" +
                "lDvL/fb7Oec731zNjMe7seFHe29jJcfTg41bf0wuJ5ewiBHRIo" +
                "cqUR4Rrd1Uy3nePzxHElXSJE9XykBe5/MdyOsL5nxyDttlz+MT" +
                "PixyqBLlEdHabTZVwvP+4TmSqJImebpSBvI6H6OyfspY9T14F4" +
                "6jn49Oj75rT4/etF+2767it+3Jkh3fZbM/LdXzhzm1vxbqeNbZ" +
                "P5Z6fu2n3o08F/fXy77Z6e/MdfXjuq6rulcjr9dXHxdt5DSrOH" +
                "j67RzHRp6dne+qzJX65maiGpfPqWfZP4O+WnmS5qK5gEWMiBY5" +
                "VInyiGjtplrO8/7hOZKokiZ5ulIG8jqf70BeP2XM3hk+re8GpW" +
                "P6cvoSFjEiWuRQJcojR6unvfsYVVVeXqevSZ6ufA8953QH8tyU" +
                "Ud9FF7jOPh9fGVZffKL5na/XvanJJg8mD2jDiyhizWo1l/Ge8D" +
                "zf53uFmorNczklZ+dKGZxQlXUHvGeqcXXXfX+1Tq9W937V3hvz" +
                "ftV+PfN+mXnvs8hvl/mLtl/Nqb0Z0eebmfd2gevq0eQRbXgRRa" +
                "xZreYy3hOe5/t8r1BTsXkup+TsXCmDE6qy7oD3TDXqdTX8aHab" +
                "XVjEiGiRQ5Uoj4jWbqrlPO8fniOJKmmSpytlIK/z+Q7k9YmZ7E" +
                "x2aMOLKGLNajWXmV3VO/DZi5HzvUJNxea5nJKzc6UMTqjKugPe" +
                "M9Wo9+CIe/BJ8wQWMSJa5FAlyiOitZtqOc/7h+dIokqa5OlKGc" +
                "jrfL4DeX3BHDQHsF32ID7hwyKHKlEeEa3dZlMlPO8fniOJKmmS" +
                "pytlIK/zMSrrEzM9nB7Cdu+4h/EJHxY5VInyyNHqae8+RlWVl9" +
                "fpa5KnK9+Dc8hb/aGfYZ+rjPq8GsH/X+/Vsw91r4bt1bN/Rv0v" +
                "/sX0BSxiRLTIoUqUR45WT3v3MaqqPO9b0iRPV8pAXs/WdyCvr4" +
                "z6vKr34G3eq+nuOvcqr1avq/VdV/X39npd1Xuw3oO39I6se1Xv" +
                "wXoPrvVoHjYPYREjokUOVaI8Ilq7qZbzvH94jiSqpEmerpSBvM" +
                "7nO5DXF8xWswXbZbfiEz4scqgS5RHR2m02VcLz/uE5kqiSJnm6" +
                "UgbyOh+jsr4y6vNq0Wf78fu6V4N/zzoZX8nVpyfreuLOV1rlHE" +
                "8/gXf8a/2X75q/0qvxlWH1xSea3/l63VVNJtfVb/XKqe+ida/W" +
                "8N5+v7kf3xprDjERfbxn2LOkF92cl5uDmH5fncmziu53L0+V4/" +
                "tUHX+z2eyizf+qXaw5xET08Z5hz+w8m1BwXm4OYvp9dSbPKrrf" +
                "vTxVju9T5bjHF/VeG3ocf7gj5/H7QNzlEu8Mf9frZfDf48+6B0" +
                "P/71x/Z6h7tYrfGdrP6p02/9h+vf0aXqw8ApZY/c6zWJ2H83qK" +
                "RB9MwTk07/P7tLmzTc8M3fDJzHq2ffbRcpUQWu9/51mszsN5PU" +
                "WiD6bgHJr3+X3atFd6ZuyGj/H2t/dpw4soYs1qNZfxnvA83+d7" +
                "hZqKzXM5JWfnShmcUJV1B7xnqjHm2f78i7v3bH8+6retuleDf2" +
                "d43DyGRYyIFjlUifKIaO2mWs7z/uE5kqiSJnm6UgbyOp/vQF5f" +
                "MHvNHmyX3YtP+LDIoUqUR0Rrt9lUCc/7h+dIokqa5OlKGcjrfI" +
                "zK+sL4F3sM47Q=");
            
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
            final int cols = 75;
            final int compressedBytes = 1430;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7tuXFUUnR9IjagiISsSdQq6iPjeK7vjE4DGRPxDikkcBN" +
                "PwBZDSpkOi4FWAUoCQEF0UJU0qKBG9IwvPbNastc7jeiZmJsac" +
                "Ozr77r3OWnvvOTn3MZHc3+nvTCb9ws6PuYeIFhhmyfKIbM02mX" +
                "huzaj5w3MmWbWa1OnIFcC1P0b1+uQMt4ZbtOFFFLGiOltCUB16" +
                "sjRyvc+wpnLLWnbJ3jlyBTvUyroCnjOvMZlMfzgbP56NL6e/nN" +
                "nr028mKx/Tr5feb0vv1yLz+8kFjulXI3M/rZHn26X3c5wP/1iv" +
                "k7d/nw/awMILVHnw9KwaZwBn5pIiZef9ac20J3bGUeoi/V7lam" +
                "OdbGtfzV67Cvtq96/5oA0svECVB0/PqnEGcGYuKVJ23p/WTHti" +
                "ZxylLtLvVa421skZdjoftIHNvdnrgSoPnp5V4wzgzFxSpOyUdf" +
                "9Aa6Y9sTOOUhea3bNV1mpkbrE3r0/aMXYN/jkftIGFF6jy4OlZ" +
                "Nc4AzswlRcrO+9OaaU/sjKPURfq9ytXGOmnvDC/dVbsGx1Zn5X" +
                "01e+PV7KvdR+vvq91Hq+2rjx9uaK122jW4jWvw3of//StweDw8" +
                "pg0voogV5SxZHpHN7PCVqzNeFViet1ZTe+fIFcC1P1+BUjXG+T" +
                "X4yZuX8Tn40bCpa/Ai9/a2Vmt09U57M6jer+4Od2nDiyhiRXW2" +
                "hHhOeI6nep9hTeWWteySvXPkCnaolXUFPGdeI9uln7f9U33Pu7" +
                "Z7jTa8iCJWVGdLiOeE53iq9xnWVG5Zyy7ZO0euYIdaWVfAc+Y1" +
                "2r7azHt7++3cfjuvevTP++ewiBHRAsMsWR6Rrdm0lus8f3jOJK" +
                "tWkzoduQK49ucrUK5PTveke6JrF3FgtMR8Lvgxq2zPmeYHR1mq" +
                "J89Z9L0f74xstaqo91bDUGP1+9WDh1fvfvXgs83c2w8/uIL39o" +
                "P2HNzG78HL+Rw8fH9k7t3L+H7V7ldtX6163J7entKGF1HEiups" +
                "CfGc8BxP9T7Dmsota9kle+fIFexQK+sKeM68Rn/UH8Eu3rqO4h" +
                "M+LDDMkuUR2Zpt+daX6Tx/eM4kq1aTOh25Arj2x6heP1e03zjt" +
                "nWGj7wyftt3T9tXF91V32p3mcWC0xOAzgs88ac40PzJ4Fc8S85" +
                "5Jq+pQVNlp9u70vLWoYaEfXgwvYOfH3ENECwyzZHlEtmZD3Vzn" +
                "+cNzJlm1mtTpyBXAtT9G9frk9Hv9Huzi6bgXn/BhgWGWLI/I1m" +
                "zLp3Om8/zhOZOsWk3qdOQK4Nofo3p94ez3+7ALdD8+4cMCwyxZ" +
                "HpGt2ZZdZTrPH54zyarVpE5HrgCu/TGq1ydnOBlOYBe77SQ+4c" +
                "MCwyxZHpGt2Za7PdN5/vCcSVatJnU6cgVw7Y9RvX6uWN7Lvsu9" +
                "y31ss8/2ztDWagPvVwfdASxiRLTAMEuWR85WT3OnHK2qunKdtC" +
                "Z1Oso59DvnK1DWCmfWzWAX6Cw+4cMCwyxZHjlbPc2dcrSq6jxv" +
                "rSZ1OnIFcP22jOr1VbGta7B7b5vXYLna/+H/29tv5/YcvLprNT" +
                "wbnsEiRkQLDLNkeUS2ZtNarvP84TmTrFpN6nTkCuDan69Aub4q" +
                "2r5q9/Z2b3+V+6q/2d+ERYyIFhhmyfKIbM2mtVzn+cNzJlm1mt" +
                "TpyBXAtT9fgXL9XIHj3lvwZoftmkveb592T/M4MFpiPhf8mFW2" +
                "50zzg6Ms1ZPnLPrej3dGtlpV1HurYeVv1B1XMx2fU+l4Q/+ax+" +
                "OZz6+7sc6+WH9mtfmX72g88/l1N9VZe2doa/VvHP2N/kacNVYM" +
                "MRkp3xHmrNWLbK4r9UFOmld7clTZafZ6VyW9d7XQ7/SLv2Lu//" +
                "lb5ogVQ0xGyneEOYv97KCC60p9kJPm1Z4cVXaavd5VSe9dLc5/" +
                "A8RzEVw=");
            
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
            final int cols = 75;
            final int compressedBytes = 1259;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWU2LHFUUbf+GCP6JbLIKyXSt/QkiBFchm/yA2DpRppFxoz" +
                "sl4EcS4kZwEcWFrkYEzToQVKKgZDnEjQvRrrmePue8d6tSY2bG" +
                "+XjVvFv345x7b910v6mqLH6Z2bH4abbPY/FrYf84O6Rj8fPsWB" +
                "3Lt2ftaLM6+Fm902YweVbvthlM3j9fPI5dbb4yEnv5CKfz9Wp9" +
                "s1qfLb7rZ7X4Ygh542bFvbfW7q+179MqXz1Tj5+PxHb2kefLtf" +
                "btv9f0QfteHcl+9X7bh8aP7m53F1qsHAFJrJ5zFqNjOI/XSORB" +
                "F+xD/d6/d5tdbX1lyIbPyfsNHtN7ho/aDCbP6uM2g6fsVzvdTi" +
                "+5hhAaL885i9ExnMdrJPKgC/ahfu/fu61z1VfGbPgob351fhUS" +
                "NixK+BAlyi2iNZvWcp7nD82RRA3VJE9XzYBf+/MJ5PWVMf1edH" +
                "n79N2L7ueYP54/hoQNixI+RIlyi2jNprWc5/lDcyRRQzXJ01Uz" +
                "4Nf+fAJ5fWKWd4rnmPvPuvfd+OHU7urnunOUoYUVtno1mnk8Jz" +
                "T3l3yPsKZicy67ZO9cNYMdamWdgOesayyKd8hb63vR5acT94Di" +
                "HfLWC4f2lP8/v0PuznfnKUMLK2z1ajTzeE5o7i/5HmFNxeZcds" +
                "neuWoGO9TKOgHPWdcY/l5N/tc+O9+rC90FytDCClu9Gs08nhOa" +
                "+0u+R1hTsTmXXbJ3rprBDrWyTsBz1jXKY+v59hQzdlz6s1+U4e" +
                "u1rffCqzhoelaOI+Bn5oxRokvUG69qzbIndsaVdTGbvf5hGcn7" +
                "GopNv29PdpAzdd/eZrW/Y+O5flGGL7TwKg6anpXjCPiZOWOU6L" +
                "o/rVn2xM64si7K68qrjXWy+l3u9osyfL222q92I04cND0rxxHw" +
                "M3PGKNElam+/2q2raDVfWRd7+9VueQVZX0+PtXfIE/4O/t0vyv" +
                "CFFl7FQdOzchwBPzNnjBJd96c1y57YGVfWRXldebWxTla+J/2i" +
                "DF+vbd4Mr+Kg6Vk5joCfmTNGiS5Ry3tas+yJnXFlXez9Bp+UV5" +
                "D1NRRrfwf/c1dtvzqQ+6u3fj9936vN39r36iiON19qMxh8J3O9" +
                "u04ZWlhhq1ejmcdzQnN/yfcIayo257JL9s5VM9ihVtYJeM66Rv" +
                "sNTj/mV+ZXIGHDooQPUaLcIlqzaS3nef7QHEnUUE3ydNUM+LU/" +
                "n0BeXxnt/qq9kzn8e9Huj5O2i7x25G+9Lz7ycxajnqHG/OOxi4" +
                "88liPp7bWxfpmxjpaesvZYbH5rfgsSNixK+BAlyi2iNZvWcp7n" +
                "D82RRA3VJE9XzYBf+/MJ5PVrxvpe9EG7N2jPOG1WbVbH97j0Sb" +
                "8ow6dexUHTs3McG35mdr5XpW8ob9YTq3F5n3qV5RWU1cY6afei" +
                "k58HL88vQ8KGRQkfokS5RbRm01rO8/yhOZKooZrk6aoZ8Gt/Po" +
                "G8PjEb1zauQfZHr8GihA9RotxytGqau8RoVeXldcqa5OnKc7AP" +
                "zsr7qbmC2d7YhtzzbscndEj4ECXKLUerprlLjFZVnucdqkmerp" +
                "oBv14treH6ymj7VXvPcPCz6h52DyFhw6KED1Gi3CJas2kt53n+" +
                "0BxJ1FBN8nTVDPi1P59AXl8Z7XvVfoPTZrW//x88y7Pa/Ks9Dx" +
                "7a03Lbr9qs2qza3n4y9vZ/AIe1YtI=");
            
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
            final int cols = 75;
            final int compressedBytes = 1392;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj2PHFUQHJ2c4OAcOUBCckDCb3B2O3cxAfwCf8X+AU4WTp" +
                "hjSYktSICLkAgAEYCMBEZCxJZtCcvIFxERYlliZ9q9VdWvZ272" +
                "fKeV7Pek19NdXd1d8zS+3R25vdpebZq2t93qPI9gHfMsWBopmz" +
                "3uHTk8levyOXEm6njnPaCjaVgV32GsBWf3wu6FhpbFhsEC05zx" +
                "FSl7xv7WTVmxi+VzTtSjysBmyxXD2oYwnzH/abl/Xu5v5r8v7a" +
                "X5983kNf9u5f258v5ImT82L7Hm347kfl2jzw8r77fmpVf7+fqZ" +
                "afmTKxrvfPzc01LWPmuflbFhsMDcR+Q++sSesb930CnaxfLaia" +
                "fyZpTZsfuwqqxeu/T1D9uHkutjw2CBac74lmW29oz9ncMsrgdP" +
                "WfBVjyoDmy1XDGsbwl7UP2gfSK6PDYMFpjnjW5bZ2jP2dw6zuB" +
                "48ZcFXPaoMbLZcMaxtCMvvqD0c7HR4zKTDM/p7dTje+fi5Z6Vs" +
                "8UZT19SzOl/PYOQ7R/1+dcJ18E99eqauj96tZzD5Kb1Uz2Dy3/" +
                "aL9Qwmn9Wb9QwG30Nc2b3i1mOPYB3zLFgagc3deJbWaX/zlAnW" +
                "0EzU8S4rHGd9egL5fHD2Lu9dhjXPIosZ5WyG+HSvB4sjrdcMZj" +
                "I3r4VKaMcuK6CQJ/MJaM9yRv8b4Hy3YQ3rvIPPDGWee3zlGmU4" +
                "js5ZRWRH1oc3eGbUBGXYmYqm+eCLmMl1Dedm/3Ub1jDzDGWee3" +
                "zlGmU4js5ZRWSX+nhm1ARl2JmKeF/5tDEly/Pb6jasYZ23fK62" +
                "LA+ee3zlGmU4js5ZRWRHVv9cbZVTeJruTEX/XG3FO8h0Dedm/3" +
                "Yb1jDzDGWee3zlGmU4js5ZRWSX+nhm1ARl2JmKeF/5tDEly/M7" +
                "121Ywzpv/46hzHOPr1yjDMfROauI7MhavM0zoyYow85U9M/VuX" +
                "gHma6h3Ov923n/qL5nOIuzmt2b3XPrsUewjnkWLI3A5m48S+u0" +
                "v3nKBGtoJup4lxWOsz49gXw+V0x/rhbvbOa5mt1d/7ma3Z32XH" +
                "18p75nqO9FN/1etJ5VfS96+mvv1t4tWPMssphRzmaI9nRP8Viv" +
                "Gcxkbl4LldCOXVZAIU/mE9Ce5YzZ9mwb1jyLLGaUsxmy+gzadh" +
                "+9EGm9ZjCTuXktVEI7dlkBhTyZT0B7ljPqd9H11ew81WuWg5+x" +
                "xvDx3M5TzeVMoJ03phcdy2xE4uwpuZ3Hes1y8DPWGD6e23msuZ" +
                "wJtPPG9KJjmY1InD2em/5v8PbXr96/wdtfrvH74Wh25NZjj2Ad" +
                "8yxYGoHN3XiW1ml/85QJ1tBM1PEuKxxnfXoC+fyyov7Gqb8HT3" +
                "vNvuo2rGGMMs89vmqNcg1HZ63XqcCG+maaMA1bdfJdxjuI08aU" +
                "1N+DJ107v2ym9rTXVC3raa7PVT2relb1rOpZvXrvGV7ns9p/vo" +
                "6K9np73a3HHsE65lmwNFI2e9w7cngq1+Vz4kzU8c578D2XJ5DX" +
                "lhX1N86E5+pme9Otxx7BOuZZsDRSNnvcO3J4Ktflc+JM1PHOe/" +
                "A9lyeQ13JF/Rysf9vr5+BGPwevtdfceuwRrGOeBUsjZbPHvSOH" +
                "p3JdPifORB3vvAffc3kCeS1xnrdythYbBgvMfUTuo0/sGft7B5" +
                "2iXSyvnXgqb0aZHbu3xzxFWV7q77f3JdfHhsEC05zxLcts7Rn7" +
                "O4dZXA+esuCrHlUGNluuGNY2hL2of9Q+klwfGwYLTHPGtyyztW" +
                "fs7xxmcT14yoKvelQZ2Gy5YljbEJbf0Sef1u+cU1c9q+lr8V49" +
                "g8ln9X49g6mr/l+1kW+yTzQ+WL1nWNyY2OHv0OGtM9P614YP63" +
                "8RT4rt");
            
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
            final int cols = 75;
            final int compressedBytes = 1593;
            final int uncompressedBytes = 19501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWc1uVWUUvYnRiW/AhKcABsw4nCHPYJxIm3TSBkgaEkKVCW" +
                "lNmfACzggMTBwocaAjwcSo4SF8ApzLuZvVtdb+9jneKkiJ3zn5" +
                "9tk/a+29vi/3tre344Xxwmo1vrbhRRSxZrVaZVavL/CJ0sj5Xu" +
                "FMxdZcqqR2rpZBhTqZtdyzmHF5vEwbXkQRa1arVeZk+mX47MXI" +
                "+V7hTMXWXKqkdq6WQYU6WU/Ae7Yzpmv4eFq0kQsvsoqDp0/lOA" +
                "J5dq4YGZ1RPjNrojKuSkXeVz1tScnBD6/Wj6/W1wc/v7LnD75b" +
                "bXwdfHvi/Xri/VIiv1/9i+vgm4XaT6fo8/TEe/aPdPSzOsU1fD" +
                "At2siFF1nFwdOnchyBPDtXjIxu9enMrInKuCoVeV/1tCUl/+/X" +
                "1b0/+nuw/7x6t6+r8c54hza8iCLWrFarjPeE5/nM9wpnKrbmUi" +
                "W1c7UMKtTJegLes53RX1en0TG+9GiKI0fLnNeAz328Z+7vHXIf" +
                "xXk2R/G8e25+GtXqDk5/Olnd3+3yrF53z72ryQfnz+J53Pt0of" +
                "bJf3g6/TPDpp/Zrw/XYREjokUOVaI8crR62jtjdKry6jl5Jnm6" +
                "6h665/YEaq5g9oY92HV2L+7wYZFDlSiPHK2e9s4Ynaq8ek6eSZ" +
                "6uugd1yFnt+Q4zVzA7ww7sOrsTd/iwyKFKlEeOVk97Z4xOVV49" +
                "J88kT1fdgzrkrHZ8h5krmKPhCHadPYo7fFjkUCXKI0erp70zRq" +
                "cqz/vOzSRPV8tAXnfLaH6+YHaHXdh1djfu8GGRQ5UojxytnvbO" +
                "GJ2qvHpOnkmerroHdchZ7foOM1cw28M27Dq7HXf4sMihSpRHjl" +
                "ZPe2eMTlVePSfPJE9X3YM65Ky2fYeZK5jD4RB2nT2MO3xY5FAl" +
                "yiNHq6e9M0anKs/7zs0kT1fLQF53y2h+fsvAdXi06teG1+GX/Q" +
                "z63zhv6hofjY/gxaoRsMTqs2axuoTzeotEH6igDs27fldb7bbd" +
                "GbrhLrQ+GZ/Ai1UjYInVZ81idQnn9RaJPlBBHZp3/a622m27M3" +
                "TDXWh9Nj6bLNccQuv5WbNYXcJ5vUWiD1RQh+Zdv6tte7U7Yzfc" +
                "xrs4XqQNL6KINavVKuM94Xk+873CmYqtuVRJ7Vwtgwp1sp6A92" +
                "xnTNeVl9Oijdzk3X8YWcXB06dyHIE8O1eMjM6oLz7TmVkTlXFV" +
                "Klarz7/KlVrXXK1/f9W/63vzZzXeHm/ThhdRxJrVapXxnvA8n/" +
                "le4UzF1lyqpHaulkGFOllPwHu2M65eu3oNdromDxEtcqgS5RHR" +
                "2g2qWp73D8+RRM3NJE9Xy0Be9TGan6+M/h7c+Pv2G8MNWMSIaJ" +
                "FDlSiPHK2e9s4Ynaq8ek6eSZ6uuofuuT2BmiuYrWELdp3dijt8" +
                "WORQJcojR6unvTNGpyqvnpNnkqer7kEdclZbvsPMFcz+sA+7zu" +
                "7HHT4scqgS5ZGj1dPeGaNTlVfPyTPJ01X3oA45q33fYeYK5sHw" +
                "AHadfRB3+LDIoUqUR45WT3tnjE5Vnvedm0merpaBvO6W0fx8Zf" +
                "Sf7Rv/bL813IJFjIgWOVSJ8sjR6mnvjNGpyqvn5Jnk6ap76J7b" +
                "E6i5gjkejmHX2eO4w4dFDlWiPHK0eto7Y3Sq8rzv3EzydLUM5H" +
                "W3jObnK6O/B0/5l87j8TG8WDUCllh91ixWl3Beb5HoAxXUoXnX" +
                "72qr3bY7Qzfci/+beNr/5/B+/2/iLF3j8/H5ZLnmEFrPz5rF6h" +
                "LO6y0SfaCCOjTv+l1t26vdGbvhNt6l8RJteBFFrFmtVhnvCc/z" +
                "me8VzlRszaVKaudqGVSok/UEvGc7o/8e7N+Lvq3PDMOH06KN3O" +
                "TdfxhZxcHTp3IcgTw7V4yMzqjpfxOuzBUgw1WpmP43kSu1rvna" +
                "lT+nRRu58CKrOHj6VI4jkGfnipHRrT6dmTVRGVelIu+rnrakpL" +
                "8HN37/3RxuwiJGRIscqkR55Gj1tHfG6FTl1XPyTPJ01T10z+0J" +
                "1Fxl9NdV/z3Yz6qf1fv0nYx8z/Bb/+t447P6vZ9B/07mLbyuXv" +
                "QzWPw8+tG0aCMXXmQVB0+fynEE8uxcMTK61aczsyYq46pU5H3V" +
                "05aU9N+D/TPDWzirvwDSBoxS");
            
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
            final int rows = 15;
            final int cols = 75;
            final int compressedBytes = 598;
            final int uncompressedBytes = 4501;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtVstOG0EQ3D/Jx4z2Z7Dw1ZY57QcBtnIIKAc4JYoU5ZuCdl" +
                "Suqp7SCsQVRtPbj6rq7pEPTNPyMk3L69s9L3/e7LflaXr33/Lj" +
                "6v27en8j8uf0ib/l+0bt1wd0nq/e749P0W7bLSxiRLTIoUqUR4" +
                "5WT7UrRrsqL/epPcnTmzV05/EFMlcZX7+rd/+ubtoNLGJEtMih" +
                "SpRHjlZPtStGuyov96k9ydObNXTn8QUyVzCndoJds6d+ug+LHK" +
                "pEeeRo9VS7YrSr8nKf2pM8vVmDc8hbnXzDyhXMvu1h1+y+n+7D" +
                "IocqUR45Wj3Vrhjtqrzcp/YkT2/W4BzyVnvfsHIFc2gH2DV76K" +
                "f7sMihSpRHjlZPtStGuyov96k9ydObNTiHvNXBN6xcwezaDnbN" +
                "7vrpPixyqBLlkaPVU+2K0a7Ky31qT/L0Zg3OIW+18w0rtzLmx/" +
                "kRXr/1ryNgidVvZrG6hfP6iIQOpuAcmvf5fdpRK20GNZww6/18" +
                "D6/fjIAlVr+ZxeoWzusjEjqYgnNo3uf3adO242ZQw7H/Ge7aHS" +
                "xiRLTIoUqUR45WT7UrRrsqL/epPcnTmzV05/EFMlcwx3aEXbPH" +
                "froPixyqRHnkaPVUu2K0q/Jyn9qTPL1Zg3PIWx19w8qtjPkyX+" +
                "D1O/xuL72i9frNLFa3cF4fkdDBFJxD8z6/Tztqpc2ghhNmfZgf" +
                "4PWbEbDE6jezWN3CeX1EQgdTcA7N+/w+bdp23AxqOGHW83yG12" +
                "9GwBKr38xidQvn9REJHUzBOTTv8/u0adtxM6jhGPE/L6spAg==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 0, 17, 0, 0, 0, 0, 0, 18, 0, 3, 0, 19, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 4, 0, 0, 21, 5, 0, 22, 23, 0, 24, 0, 25, 0, 0, 1, 0, 26, 0, 6, 27, 2, 0, 28, 0, 0, 0, 29, 30, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 31, 10, 32, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 34, 0, 0, 0, 0, 3, 17, 3, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 36, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 37, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 38, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 39, 4, 0, 40, 0, 0, 0, 41, 0, 0, 0, 42, 43, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 8, 0, 0, 44, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 45, 10, 0, 0, 0, 0, 0, 20, 21, 22, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 24, 25, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 0, 0, 4, 0, 0, 30, 0, 1, 31, 2, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 34, 35, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 38, 0, 1, 0, 39, 0, 0, 6, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 9, 42, 43, 0, 0, 44, 0, 5, 6, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 47, 1, 0, 0, 0, 0, 0, 0, 0, 48, 2, 0, 0, 3, 0, 7, 49, 0, 0, 0, 1, 7, 0, 8, 0, 50, 0, 8, 51, 0, 0, 0, 0, 52, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 0, 47, 54, 55, 0, 56, 0, 57, 58, 59, 0, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 63, 10, 0, 0, 0, 0, 11, 0, 0, 64, 0, 0, 0, 65, 12, 13, 0, 0, 0, 66, 67, 0, 0, 0, 4, 0, 68, 0, 5, 0, 0, 48, 69, 1, 0, 0, 0, 14, 70, 0, 0, 0, 15, 0, 1, 0, 49, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 0, 0, 12, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 71, 72, 12, 0, 51, 73, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 74, 75, 0, 76, 77, 78, 0, 79, 1, 0, 2, 15, 16, 17, 18, 19, 20, 21, 80, 22, 52, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 36, 37, 0, 38, 39, 81, 40, 41, 42, 43, 82, 44, 45, 46, 47, 48, 49, 0, 0, 1, 0, 0, 0, 83, 0, 0, 0, 50, 0, 84, 85, 9, 0, 0, 2, 0, 86, 0, 0, 87, 1, 88, 0, 3, 0, 0, 0, 0, 0, 89, 0, 2, 0, 0, 0, 0, 0, 0, 90, 91, 0, 0, 0, 0, 0, 0, 0, 0, 92, 93, 0, 3, 0, 4, 0, 0, 94, 1, 95, 0, 0, 0, 96, 97, 98, 0, 51, 99, 100, 101, 102, 0, 103, 53, 104, 1, 105, 0, 54, 106, 107, 108, 55, 52, 2, 53, 0, 0, 109, 110, 0, 0, 0, 0, 111, 0, 112, 0, 113, 114, 5, 0, 0, 0, 0, 0, 0, 0, 10, 0, 4, 115, 5, 1, 0, 0, 0, 0, 1, 116, 117, 0, 0, 3, 1, 0, 2, 118, 0, 6, 119, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 120, 121, 122, 0, 123, 0, 54, 3, 56, 0, 124, 7, 0, 0, 125, 126, 0, 0, 0, 0, 0, 6, 0, 1, 0, 2, 0, 0, 127, 0, 55, 128, 129, 130, 131, 57, 132, 0, 133, 134, 135, 136, 137, 138, 139, 56, 140, 0, 141, 142, 143, 144, 0, 0, 5, 0, 0, 0, 0, 0, 0, 57, 0, 145, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 7, 146, 0, 147, 58, 0, 59, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 148, 149, 0, 0, 150, 0, 151, 8, 0, 0, 0, 152, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 153, 154, 155, 0, 156, 0, 7, 4, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 11, 0, 0, 12, 0, 13, 157, 9, 0, 158, 159, 0, 14, 0, 0, 0, 15, 0, 160, 0, 0, 0, 0, 62, 0, 2, 0, 0, 0, 9, 0, 0, 6, 0, 0, 0, 0, 161, 162, 2, 0, 1, 0, 1, 0, 3, 163, 164, 0, 0, 0, 0, 7, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 59, 0, 0, 165, 0, 0, 0, 10, 0, 0, 0, 166, 167, 168, 0, 11, 0, 169, 0, 12, 16, 0, 0, 2, 0, 170, 0, 2, 4, 171, 0, 0, 17, 172, 0, 0, 0, 18, 13, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 173, 2, 0, 3, 0, 0, 0, 14, 0, 174, 0, 0, 0, 0, 0, 175, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 176, 0, 177, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 0, 179, 180, 181, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 182, 0, 183, 184, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 185, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 186, 22, 19, 0, 0, 0, 0, 0, 0, 187, 0, 0, 1, 0, 0, 20, 188, 0, 3, 0, 0, 7, 10, 1, 0, 0, 0, 1, 0, 189, 23, 0, 0, 0, 0, 24, 0, 0, 21, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 16, 0, 0, 0, 0, 0, 190, 0, 0, 191, 0, 0, 0, 192, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 193, 0, 0, 194, 195, 22, 0, 0, 196, 0, 197, 0, 0, 23, 0, 0, 0, 60, 0, 26, 0, 198, 0, 0, 0, 0, 0, 0, 0, 199, 24, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 200, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 201, 0, 23, 24, 25, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 202, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 203, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 204, 205, 1, 0, 1, 27, 0, 0, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 4, 0, 0, 1, 207, 208, 13, 0, 0, 0, 0, 0, 0, 0, 0, 209, 67, 0, 0, 210, 0, 0, 211, 212, 0, 0, 0, 0, 0, 0, 0, 0, 213, 0, 0, 0, 214, 68, 0, 215, 0, 0, 3, 0, 0, 0, 69, 0, 0, 62, 0, 0, 28, 29, 0, 0, 3, 0, 0, 30, 0, 0, 216, 0, 217, 0, 0, 64, 218, 0, 28, 219, 0, 220, 221, 0, 0, 31, 29, 0, 222, 223, 0, 32, 224, 0, 0, 225, 226, 227, 228, 30, 229, 33, 230, 231, 232, 34, 233, 0, 234, 235, 6, 236, 237, 31, 0, 238, 239, 0, 0, 0, 0, 0, 70, 0, 2, 0, 0, 240, 241, 0, 242, 35, 0, 0, 0, 243, 0, 244, 36, 0, 0, 37, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 245, 0, 246, 0, 1, 38, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 36, 0, 0, 247, 0, 0, 0, 248, 249, 0, 0, 0, 250, 0, 0, 0, 251, 1, 0, 0, 0, 5, 2, 0, 0, 37, 252, 0, 41, 0, 253, 0, 38, 254, 255, 39, 256, 0, 257, 0, 0, 0, 0, 0, 0, 258, 40, 259, 41, 0, 0, 0, 0, 0, 260, 0, 261, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 262, 263, 0, 0, 264, 0, 7, 0, 0, 0, 43, 0, 265, 266, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 267, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 268, 269, 270, 0, 0, 0, 0, 0, 0, 0, 271, 0, 0, 0, 0, 8, 0, 0, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 272, 0, 0, 0, 0, 2, 0, 273, 3, 0, 0, 274, 45, 0, 5, 0, 0, 0, 0, 0, 0, 0, 275, 0, 0, 0, 10, 0, 0, 1, 0, 0, 2, 0, 276, 44, 0, 0, 0, 277, 0, 0, 0, 11, 0, 0, 12, 13, 0, 45, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 278, 0, 0, 279, 0, 0, 0, 0, 0, 280, 0, 0, 0, 46, 0, 0, 0, 47, 0, 281, 0, 0, 0, 48, 0, 0, 0, 0, 0, 282, 283, 284, 0, 49, 285, 0, 286, 50, 51, 0, 0, 8, 287, 0, 2, 288, 289, 0, 0, 0, 0, 8, 52, 290, 291, 53, 292, 0, 0, 54, 0, 4, 293, 294, 0, 295, 0, 0, 0, 0, 0, 0, 0, 296, 297, 55, 0, 0, 56, 0, 0, 57, 0, 24, 0, 0, 25, 5, 298, 6, 299, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 300, 301, 3, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 302, 0, 303, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 304, 0, 0, 0, 0, 0, 0, 305, 0, 0, 0, 7, 306, 0, 0, 0, 59, 0, 307, 0, 0, 308, 0, 0, 309, 310, 0, 46, 311, 0, 0, 0, 60, 65, 0, 0, 0, 312, 313, 61, 0, 62, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 314, 0, 8, 315, 0, 0, 0, 0, 0, 63, 0, 0, 0, 0, 66, 0, 0, 0, 2, 47, 0, 0, 316, 317, 318, 64, 0, 0, 0, 319, 0, 0, 320, 321, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 9, 322, 0, 51, 323, 52, 73, 0, 324, 53, 65, 0, 0, 0, 0, 0, 0, 0, 66, 0, 0, 325, 0, 67, 0, 0, 326, 68, 69, 0, 54, 0, 327, 70, 328, 0, 71, 55, 329, 330, 72, 73, 0, 56, 0, 331, 332, 0, 57, 74, 333, 0, 58, 0, 0, 75, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 334, 59, 335, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 336, 0, 337, 338, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 339, 0, 0, 0, 0, 0, 0, 0, 0, 340, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 61, 341, 342, 0, 0, 62, 343, 0, 63, 344, 0, 64, 345, 65, 0, 0, 76, 0, 0, 346, 347, 0, 0, 77, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 348, 0, 67, 0, 0, 0, 0, 349, 350, 67, 0, 0, 0, 78, 0, 4, 5, 0, 6, 0, 0, 0, 3, 0, 0, 0, 351, 0, 352, 353, 0, 0, 0, 79, 0, 0, 80, 354, 0, 0, 0, 0, 68, 0, 81, 0, 355, 0, 82, 69, 356, 0, 357, 358, 359, 83, 84, 0, 360, 85, 70, 361, 362, 363, 364, 0, 86, 0, 0, 0, 365, 0, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 366, 1, 0, 4, 0, 5, 0, 0, 6, 0, 367, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 87, 88, 74, 0, 75, 368, 89, 76, 77, 369, 0, 370, 371, 0, 0, 372, 373, 0, 0, 0, 7, 0, 0, 78, 0, 79, 374, 68, 90, 0, 0, 0, 0, 0, 0, 7, 0, 375, 0, 0, 0, 376, 0, 377, 0, 0, 378, 0, 91, 0, 379, 380, 0, 92, 381, 382, 383, 384, 93, 94, 0, 0, 0, 385, 0, 0, 386, 387, 388, 95, 96, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 97, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 389, 390, 0, 391, 0, 392, 393, 0, 0, 0, 0, 98, 99, 0, 0, 0, 394, 0, 0, 69, 70, 395, 0, 0, 0, 0, 0, 100, 101, 102, 396, 0, 103, 104, 0, 0, 0, 0, 80, 0, 0, 105, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 397, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 106, 107, 0, 82, 108, 0, 83, 398, 399, 0, 0, 84, 0, 8, 0, 0, 400, 0, 0, 109, 0, 0, 85, 0, 401, 0, 0, 86, 0, 402, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 403, 0, 0, 0, 0, 404, 0, 405, 0, 87, 0, 406, 0, 88, 110, 111, 89, 0, 0, 112, 0, 407, 0, 113, 408, 409, 0, 114, 410, 0, 0, 0, 0, 0, 411, 0, 0, 0, 0, 36, 115, 116, 0, 117, 412, 0, 413, 0, 0, 0, 118, 414, 0, 119, 120, 415, 0, 121, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 122, 123, 0, 124, 0, 0, 125, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 0, 5, 6, 1, 1, 5, 6, 7, 8, 1, 2, 0, 1, 0, 0, 9, 1, 5, 0, 5, 0, 1, 7, 2, 10, 0, 11, 0, 12, 1, 0, 0, 6, 13, 0, 14, 15, 11, 3, 11, 0, 13, 1, 1, 16, 17, 9, 2, 11, 18, 16, 19, 3, 0, 19, 20, 21, 1, 1, 22, 23, 2, 24, 25, 1, 26, 0, 7, 7, 27, 13, 0, 28, 2, 13, 29, 30, 1, 4, 0, 0, 31, 32, 7, 1, 33, 34, 2, 1, 0, 1, 3, 10, 35, 36, 16, 37, 38, 0, 39, 40, 7, 41, 1, 42, 0, 1, 43, 44, 8, 6, 45, 4, 46, 47, 48, 4, 9, 1, 6, 49, 50, 51, 38, 1, 9, 0, 52, 6, 53, 54, 13, 5, 55, 56, 0, 57, 1, 18, 0, 58, 59, 60, 9, 61, 23, 62, 2, 63, 3, 64, 5, 65, 66, 67, 0, 0, 0, 19, 68, 69, 70, 71, 72, 0, 3, 73, 18, 0, 0, 74, 0, 75, 76, 7, 10, 2, 2, 77, 3, 0, 78, 0, 79, 1, 80, 1, 81, 82, 83, 0, 84, 85, 86, 87, 3, 88, 13, 0, 11, 89, 14, 2, 90, 91, 92, 93, 19, 94, 95, 0, 0, 96, 97, 3, 98, 0, 99, 22, 6, 9, 2, 24, 27, 100, 0, 4, 101, 2, 1, 1, 102, 0, 9, 103, 104, 1, 105, 106, 107, 108, 109, 110, 10, 0, 111, 22, 16, 0, 0, 8, 5, 1, 112, 31, 2, 26, 13, 4, 7, 113, 5, 2, 11, 114, 27, 115, 116, 0, 0, 18, 26, 2, 117, 6, 1, 0, 3, 20, 0, 4, 118, 2, 14, 1, 0, 119, 120, 49, 19, 7, 3, 22, 121, 1, 4, 122, 123, 16, 124, 8, 125, 0, 6, 126, 127, 128, 129, 130, 131, 29, 31, 132, 133, 7, 9, 134, 32, 12, 10, 135, 136, 12, 0, 5, 14, 137, 138, 139, 10, 140, 6, 141, 142, 143, 35, 23, 144, 145, 146, 38, 147, 2, 7, 4, 148, 149, 0, 39, 150, 151, 0, 152, 0, 153, 40, 26, 41, 154, 155, 4, 156, 49, 22, 8, 157, 158, 9, 42, 159, 160, 161, 0, 162, 163, 14, 0, 164, 165, 47, 4, 1, 34, 166, 167, 168, 16, 169, 170, 12, 1, 171, 172, 173, 34, 6, 0, 51, 0, 0, 9, 174, 2, 27, 29, 14, 3, 4, 45, 1, 175, 15, 176, 177, 8, 8, 0, 178, 179, 180, 2, 181, 182, 24, 183, 27, 184, 41, 2, 0, 185, 186, 187, 29, 0, 19, 0, 1, 1, 188, 9, 32, 13, 189, 190, 2, 191, 192, 52, 193, 18, 194, 195, 196, 1, 0, 197, 198, 7, 199, 53, 3, 20, 200, 19, 14, 201, 202, 3, 203, 0, 15, 204, 57, 205, 206, 207, 1, 6, 208, 209, 210, 211, 212, 3 };

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
            final int compressedBytes = 1357;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXEuO2zgQLTIcg+lsmMALZ8ckxmDWcwJmE/RylrM00DkIex" +
                "N0jpBd902Sm8xRhrY+oSVSomRKoqh6QBS7bVki+erVRyU9kEd4" +
                "OsB5u6P6/T9qB/KzOh7fPsF3IB/+Y/wZKNsf+OO3r8cX9be6B/" +
                "UXfCJv/v21/3ECBALRhjD/uO9DWr1Q6x/oJ9DAzUiNfjCqDwKM" +
                "fhD1pzQfPQGcNHv9TN+B0Q+9U0zCR7iX6sn89+bwC96jfsBDt/" +
                "7+RP3NG5X9vBj7uavt5yiBGDMBqc36U8ov9vOzsB+4R/tBIBAN" +
                "8DLuKCGr+IMBMe+qr7Bz1HF5p4TKaPSsd3oEUgSBQCAQTcdp5e" +
                "3tP4siay9dzOfCf54ghv98+APrb4gEQC405xbrL69kaRFCNo2E" +
                "JlG/susnr9ZqP7TcMFZqDGnIkie+zSB+r+uHwq4fHs9s+13/uN" +
                "QPv5T1Q5igfmhPLg0tz0oUjZxwtdqd9fuY9ueun1f8P134j/Vz" +
                "xBLFE4UTE2y/S9Xvbf24c/nP+8p/1sdXUxy/NX5ojV+le/0iCf" +
                "5zT/55Xb+FTOu3yydA/vp5ixbFasSbf5bCBGzn+kGOkTsLWd4I" +
                "c4RXUbpmNqR+qGLWD1dCvmTRU38N7L9JoH7gq985iHlVv+PtEZ" +
                "Sv767OkE8cIPWdv/Scf0SIoYK5bvuNwr+e+utM/Wvj7U+RD6DF" +
                "mX3c7EfUeR/FgL3V8BoIURcWmJ/nRB+fH4uQxyR3xPz4XqvuUy" +
                "Jg9Xq2mRbP/quDMGbSLw9/M60fK7MUrfWD6/WT5fq9ONavu/42" +
                "V/0Z6zeIArregFc9rC9E5M+gGE50qipdPkBkE357fWkJ7xq5OG" +
                "957Y4kUGmxr3Byr85FAL63iKe5pS/O+TvUNKC8/jnuJ3VXaJhW" +
                "6q5G2vXNOTS74aCJzAsJHtfZoZdDaCQF3M8hssg4s9SPAdEnc3" +
                "KbVsuhHDKko+2fSCDa/6ma084WAlmEgNKnfKJDGElcG0hDiV0D" +
                "JjoR/0lTOaKKPBXsdjuVQ+JsDYixSQrrnT/Zt3qz8Dg4plAutq" +
                "muE18yXEmQugT2zhSJt2eSWGnSxPELX6XNkXROhUtrLmkXCXX7" +
                "DW8uKIVNYNz6zSwo88S//DcL+AAR4xmwYMh42XgWYBizukw/X8" +
                "4vg97+zfr6067j+hNe/8k0f03r+HnD33/bMqsissys/zN//o0+" +
                "/3wDlfD+h13C/Q8bg8Qp2Jb+6Y2PPwwz92/GsULujQDm7b9duN" +
                "TQO/7cE5yx/dvL8R8xLRpNeYk4fb3hFUnQ/zn718Hdvy6D+tdp" +
                "6CjXGIWqhCizgSgebxa8Rf1nu3/kJv0Jv/8C9SdJmjX6v091//" +
                "flXqLx/d8DJPKqf0JbH7T6J1iacUi40GnkXF4eHoFARNR/cOq/" +
                "qioaAnUUvUjuINtabnZdrJR27qw8UVYRf8a5f7E/WceQJCR/oD" +
                "3zL7Tomv/J7CdfcU90/gOyIpGUhW27UmXrJ7j4Q6DQT4A5+XNr" +
                "WLnQ/Dn8V/FA733bf6VQkth4uNqTf7DG/TtqviGJcP61rvZaT0" +
                "FyXQhmEkUR0y1E/LjTFXrKyfh0a/4TNX9aMgvhYbGh9i3JbEkf" +
                "agliAv57IuAm23i9Qf+PSJi8/ZBI1pSzRTHTkggrf9NJPD9KL2" +
                "lCwpkCtsefTf6V3vpvTLwzyB8QQaC+v8Z9/p8I3J+lZAWrcuu6" +
                "rFVU8yxv2j8poKggEAiMvzD+2iojHB5RTR+tiMHZG+ZgiAxtjg" +
                "/b06/fNEi/1XXkW9w/pTypWtKPcMDHyI3Myofk36pgnUd7uR7I" +
                "kP8B7P8kbg==");
            
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
            final int compressedBytes = 1228;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUty4yAQBUaTYj4LasqLzI75HITKao6RozCzyjJHyFVys7" +
                "HsOLZkwAhoGqR+m1QS426a/rxGSGKsCajoT1ogDUTon/qGEoNX" +
                "VZUwxRWAl/06C6vtcFvn4z81h9CHjwrI678f/2SmH9UelwRFaL" +
                "5KnP3fTtxcnMxmJhOqsqQZkDWFDQeLDXuh54UV+iIlWTNa6sOw" +
                "N6XcXVjNyqlnrBAaKx845AsE+QRCHD8h5OX7IcQLCARCFo+StS" +
                "JL+XmcI4GeCJROV+fE3/74+RsD5m+o+cvBX3ex83+I478R9kuY" +
                "aMH+acc+ShcxFEfV9EyWPek/t5/F8R8CIT3+VSB+JWD8EoemHq" +
                "RY/eg4/mruHwmKv2JES5fhD71DXfP2MGy99TObmX86fu0VkntX" +
                "/MueBmHvFbtjmpvfo8c+7Z3cDvJFCLa7l/bODJr93DdK5mn/48" +
                "v9K/v+OM9fulz+1owQmX+3nH8arl/DNDEBObT0ZsDPN0UCJEgK" +
                "2+xaMlvZuuvXPCDqp7l024gkweMWkLBW8p+Y6mj/gvpPwvrAS6" +
                "eWlfr/RuN39TQtzs3luWRaOAWWn18UmeMbmH+oM4xpJQZvuFrX" +
                "l0pGqNLjiIpNUENTrzv/rPMX6zg/MizlKipzfMHMb1zXz8yo4c" +
                "H+L6OV+Wh/frL/mNStz/66BpG70VWKhfJz6w+Nxx2fW39h+Ec8" +
                "f6D1p/GY/Ld3+Vu3f8b4IueHth1/+P0vjW9x/6BCSlgL6sxWrM" +
                "bAFt/+Or9+PFSbr2HxrWp5GNivn9mfuezP3+zPAPZPcj1vif8k" +
                "6u9f/2XylcWwn2EEaC+8jB92uf5H6x943MT/WM34wc1fhBL+E8" +
                "pfgfwdNZ6wXc8rUf/j8h/bbP5rN/9Grn8Cf1KLtGjbfxrsX8/9" +
                "03P8/ePW0z85np8lzr7ruvow6OrTh9t/zD5/UWC8XMjeNbz/ZZ" +
                "4/wTr/IgvNX57ly7n8Gifpatkvf/8Z238K79/aivmr5fojfIsS" +
                "k79q1o811H9U++Wuf9f8R7HjcaX3CR8/9M1BYXkivyU0Cx3jzL" +
                "j52izq38qffyzeZ1HoZMCWyZ8O/9HR/iOb2j/U019g1eJIge6R" +
                "P4t/cbV+/3zx/xV+/dQCL0TlH324R4UwIlZOyOPvKhC/ssJ4lj" +
                "I+CwX2T6q0T3D7H155sIaP9h+ToobqiKdiXz8gYPV/WdRMLRNi" +
                "8CZpwOWnWxL//S3Y9z91Pz63fmHfv41nvxVcf8W//oUev/36X8" +
                "P5g/YfCMvIuIemeTo0Xihy3fn77f79Ty/v+Xty//7zmL+Vv9Uu" +
                "3FJa1JiyEStVkpcjyidU6+h0V9qW3n+osf9hQMIRJh3pevKHvt" +
                "byqvff+mvl6LV6eTmM7Ne799suNFWJ+utS1SuvFPnli8vZ5D3/" +
                "q1D/gemN/v2vwwbCu/4gz5/r8vxXW9kEdf0IBAKh3/xZ4vmfU0" +
                "YuF8nHfX8hNn+xLpv9uDngoNSjm9wF93IBbnwz8ZwD5LY74xJZ" +
                "UT6BABJDnuYTF9bdbC4b1BC60D8sn8fff87n87bGV394sP4Yb/" +
                "1JrMLl6veu/PPnevefdcePz/+ly//1Er0JgfWEOkVDb0dJJAct" +
                "gM4vFeo/I/tf+P5ZNdk6XZ1/h6NXYfwH1s7lOw==");
            
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
            final int compressedBytes = 878;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuSmzAQlRSS0lTNQgsvkqoslFQOopqTzFE6c4IcwUeNMW" +
                "MMGLAA/fXeYsaesRD9e92NBGbMNWjyXnW/xPynm+sHGiaZ/vyL" +
                "ZkIPhpBpf35pGGfyNDg4SWYexuvN44ExOFTgCnL2Ze/2APQPd7" +
                "BByTx9NP/t9h/9OD/tnj9f+7vRf+ryU4aRoTKKv7SkpcL8379G" +
                "hvLfuVHco+9aFl/kH5UX9fQPx/gDfda++lUPfE1kz+cRyv7M8k" +
                "cTNiwfZo0zf939Rx35t/T+C/4PJOu/5N9/919/QKKpMCBNDfI3" +
                "8B+4O1AHHw+ymTh4MAF9RiVtYfEZAAAAF4Rj5vo303Z1VyY6t9" +
                "UPb/s3fuvf2sUI8ti/yW0jHe7fGoxXD+M7+UUn/792vApgnn0N" +
                "q6u5Y84PANtLU203yKU36/EbtIs5+w/kD51kJvn3Pej6h4P1F7" +
                "nYkoXo1KyIjMqdP/psOP1FqP3xp5fjT/fxJxkAAJ6BghYAAB/5" +
                "HwAAFBEQHQAAANiOtPf/gv+T9pxC778oZ7HLWNtPPLGfCmi/fJ" +
                "tfiqWCatdnYzMJ7p+pJCLz8u6jFBqbgmufv0iuhP0Bq/wpRc/c" +
                "940K16pTplbpNCubrs3svxpdvP1Q/wAAsBHcgizS4YP++gndzl" +
                "GfbK9/veXM31P5mT/5069Y1/P/1HPN5vznz37oWJLF2vOPA4P8" +
                "xw9Qa+e3vn70sZQ/Xq15wUDNiUMsvJ4Frsosd9R7qgo+IngNRa" +
                "abJ4Fw+lcbmAbXvwAgOajKx4fRnxjxnxr9g48KcDmmtKZ0+x/t" +
                "P7B+j/hPX/9xn9+wXkyplXqK79eg9XgAADziVx+RxARJxb4xzc" +
                "2fT17SFwo6X17+/M5ItHsuft+S8Yv6y368Q3970EQ4ujk4v4l8" +
                "/sCitlzdP7K92iHoH/APU3T8Hnv+7bPnx76c+/gfjX98fmwCy0" +
                "IS/gNkmz/TCR1dnx1rl98BPZrnnCkKEnc2/sW+/TPu7r+Q+P4T" +
                "4Fj+8rB/Wife+DWL7anKxYoHcGJf5ZxhRHdoPTEf3ewPAC78r3" +
                "r+7NTHn3Ok5q75Mwz/l3AVoCRQqnpSm+xo8tC2iTw+NHSep52x" +
                "/uPt/yj4+9+t7v9akp+Skp+O2I9mz/8j2PfXKJby99dkwT+UbP" +
                "zPjFep1Z/GJ//9B2dcmd0=");
            
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
            final int compressedBytes = 881;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuO3CAQBeIFkXrhRR+A3ATlJDkKygnmCHPUkac9TqcHd4" +
                "P5VBV+b2FZI9VQUL+HXaaVosWk1LxcrXLrX5wy661ZLsEv1x+T" +
                "0speVdgEg1U+Ln9Nlf/9IB8OjL/gqiYbm5u5TdBtqq2SX/JPoF" +
                "VE5Nkq2vtB7vW/aalXRe/wT/9X8AlWLBs/Td5G5Ev9p3T+PcZP" +
                "iqJ68ZcVP/uYqeJ/rqB/ZvyR2u8t3X4hw35AKdxjLejmTw+D54" +
                "el5Pr/KB9a5l/q+iMd00xbP2G/E/mfOWslyq43mlgeACgxlP/q" +
                "BFrIkTMDAJBL0PqSnEn5GH/2C6v+1OR9UU4v/Fl/8eclH4XW/H" +
                "3+xt9v45vb+G/L+LX3wnMH8wKDw+hWpB4AJLBtABiWngFYP3Fg" +
                "TTXs7vvTaXzLHH1/EUJ2/0ST/Ruz9TPf5v83vn4qXAbKS30CxT" +
                "COTVMoaBLJrZNlsub1o13/ETX/CKlm3Mm/oWP/GJ398vq3kvuX" +
                "svsnqXgQj/5BoNiKds9XvIp1+k6OR/wNwP8H9Z8X1cdVHh/7d+" +
                "DM8VehfjuB9ZsD/3DN8toY8Cztfyn8/uaCugDUhKuQPYiyj9m5" +
                "j2KGqYG+6L7/PEf/OCIZOOQhYYuRqu//dJq8vuP/R8bf5OH/Q+" +
                "+fwe/b7AEnLAeb/TNw+syH93e1YBnZb/1+5Of7Zr//+p+i34/Y" +
                "cfzX7fuv2/w3cb70fh7XwCHiRsu/J+3fy0CQbm7RFHNqtixH+w" +
                "fc+E4j1606pAMfoS6/XrqCX8T+CNZfrfqXyjcCzk8BXjmhHWc+" +
                "k2TNsf9ns/4O689y/z3u/BH/8D/J9YfD+emU/kcdvxLOH5UWv4" +
                "HZ+pfv/1rrf4B/O+Te56jX3NIyf/HIP2M/fyPIn5knKh2Pf+m/" +
                "fwGA/7Z+fiB9/wP9K0H6+fPp+nerP0zHBwCicmiwCmxscZ9YOi" +
                "aMmShbzSSLzOf8O3zPApSTLNrzv91opBUAqtYPt+3/Lcn4qub4" +
                "Z4DHEgAAAPSon+zO7wVgv0zg+bEQ/jWG/8r1nwb9JzM/+4XXq5" +
                "D4/V1A/KfCRm9P8/whFJ6/Gaqdv+ll+k8mSh7eZ53fCLSBI8yq" +
                "gbAu65LxmfIn8G/U/7PjFPzn6bSr/Y/Adralv5/+dP9llJD+/w" +
                "FYvJIZraX+J33+QCE+ANMC71U=");
            
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
            final int compressedBytes = 737;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtOwzAQnYQImZ2FugCJhUEcxOIkHGW4SY9K0tLSRT9OE9" +
                "sznvcqVVFUx57xfN44TkoEtA82Pn6GCcxDN+/ng1r7GUfup29H" +
                "4e9MoP7vsN81jdP3wzCqxG1OLsSOYjX7213PrXElDfJH+KNpSJ" +
                "7/BP/xy/3HtP4Wx5+oT39dKf4iQ/938IegKf7k4x/wn7r8fx39" +
                "y7b/nPytDP+0XT+DPwtBDxVki8JZ4ycL7182uhXTVle5PXDLEf" +
                "oMzgoA5fKHK9pzPMd/48SKd560ndhbN/Hf7sB/pxjGV+oHZ3ou" +
                "k0kWw+4V8zcAWLB+wEXXD85EpmHdi/aJjDfIm0JP+3R3FGI/Sc" +
                "+0P3lJFp8oPxWRX1hpn3z/QMP6s2T9lan/lyLf/Ncefxn966aK" +
                "gv3s3/74aH+bVPv7MsIfIwGJHh2gBsBS/kb9l52/8gz+c1qxuJ" +
                "sVgpHAXkxMV7k9Ke9fvok0SvQG5f1j/bZl/h4z5l9gDf6q1341" +
                "2M/yflis/Bn486z1m9P2nEn+KNj+S+RPyfZrwf6s84f0+BPAH4" +
                "AM4MrtAQAoxn99JWF8ZWV61aZwIAB1rBAAAGCPptbfonL5Z64/" +
                "3LF/KHf9zarlz8/f9OZf7J9tgf+1nT8qxQ939rBC/gMKocL+lU" +
                "vxM1y233CMn83stxmM91+bv8jm/9b5SxQsP+6fI/42Vr/AfgEg" +
                "rTAs3h4Amo1/3vQ0BcLzj/qg//0d2pm76vpX+/2f2uNH/rZd/2" +
                "i3v/fDcMZPz87TI4Uufk6n3OSLA23Hw7cX4p6GQB+HyXzyP/T6" +
                "fZVBuXTDwPsH4f/3tbew/h5hgguXORj1M+YfANploeBPAAD+LY" +
                "M/rJp/6/MfzlZ/4P23K0Jh4rOwf1th/dLU/2dCf3j/gwl4qKCF" +
                "+hXQE/+Exl/kH0H1VxQ8f9r5L56fsC0/AP5Rn3Uv+P8tmfnHjP" +
                "8j/rQfvyPkBwCz9UNUPn7C+NvFL5ou8P0=");
            
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
            final int rows = 10;
            final int cols = 16;
            final int compressedBytes = 15;
            final int uncompressedBytes = 641;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjYBgFo2AAAQACgQAB");
            
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
            final int compressedBytes = 3968;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWweUFFUWfe9X1e+qHgmD7IrgMKLoyChRxNEVQUBU0EGCAR" +
                "ATIGIgG3BRUFRYBEGMLBgQEwhGgmEQE7usi0cwoKsryyZRj8jK" +
                "shjYPbXvh0rd1dM90xPgHOuc7vr1q+r3r7p177v1/m+2HBBMcM" +
                "BmQ9jt0AgK4WA4xBoDxcZsOBJK2IvYjP+JdYAO0NGYgxVOCyhj" +
                "zeFu6GZ8Cb3YBbw/lMMAthTOg8G4yxpsTjOushvDpXAJXA5X8H" +
                "JgeBccYPXE/xqH4cPQFH7BprNFZj/YmVgCh5rPw15jMrRmA+Ao" +
                "vBNK7X7Ot/ggtGWPQyc4Hk4wrmXnw6lsJi6BPtDXeB/PgLOco4" +
                "z72CQ2yLgHhrKBMJx9B6OwKe40boECaGxwaxzOhyZwIBwEv2Q7" +
                "oQW0gsPgcHaL2csYC8fgNmjHFlufwnHQhbVlhzg74CToio/g1+" +
                "aZ0ANOT7zEboWzoT/bBfNhEJwDQ2AY3A8XsiK4DEZbm8AAi5UB" +
                "hwTuYMMMgCQ04J9CQ2iG/0jewS6BIjwdWuK/jY8SH8AR0AaONs" +
                "vxC+yOX7Gh7Gy8F9rDsdDZGAYn8pHwKzgZukNP6A2nWZvhDDjT" +
                "mA79wIWBcC6cb30GF8BFcDFbBiNgJB/luqwNK2FHwdXmIDYkkT" +
                "DPgauortQxXNeYTaUjXRebuS6/06XF2IEVyWeptjn2dl27l+ua" +
                "V9oF9H2DKxfW2d5h9yasmrh64eVU24mO3cYON9vgw6o2cRp/yJ" +
                "wi94M5zvie1k+rPfbZBQ3c0CKwEmvrHbVtdZG191Ebw417qG2J" +
                "FfWxKdWudNMWtjPS2liq6Uif9dY2ubett8d6JDjKfF3u24Wnhs" +
                "/ll4tvp5nc96xufZhaOzPlVpfkHLZbH92DH2sP1OVF6LL3eXt5" +
                "zDFBiwkr3L612SvhZbrmM92fgXAltSKw6mnO9XhlzoYxxKuxrB" +
                "dMgmK8BiZDicDKmWUUw3XQMSF/SfAKJ0K3ZFsYjxOcgcSriYk+" +
                "gle8p/OdMxkn0zGCV9cKrIDxebKX11NLkleumzzF7GdSLRzK99" +
                "IdJKwEr2i7VPZxMdA9FLyiu7CGvxe+IjxDXmWp2lK8kvUCqwqq" +
                "aewdCQcqrIhXEwSvEiZ/RPBKYrXY2i54xefy0XSk5BXdkzNhHJ" +
                "xOe2+VvzGD9lwjeKVxKaLt0bJli60OsIIkXSXxSmBV0N8ogiJ7" +
                "veCVPPII+hCvWLFGbTnrLXjl91HyitbEK+dwwSsq98MpHlY+r6" +
                "6Hkc7F1Ke+IQ3s42kgPxmKrUFCA61+eFLyE/MeoYGsKz/F6g9l" +
                "1K+ToBu124tvom9fA/ldzm7nRtkLrYHOIGCsExzA+rK51NfuAq" +
                "uETaWHrPMEVvb1AitobU8PsKK1xoo0cJTQQFlHGijXv1a8CrBK" +
                "zIRRkjVbhQbqPVIDNVZaAwWvBFbQzhhhfSOw0scSVrrUQ2GlNJ" +
                "C2tQZS6UL6kAYml0oNfEVooIeVp4GE1QCBFdW11O1JDeSL2d0+" +
                "z89SGkj7TvSwCmugwIqdHKeBEr1SuNHDyvi9fTwUGr+DaXATFC" +
                "fmwVQoMTZgeWKu1U7wCqbAzYJXsh/d4BYYb/dPHgvl/F3aJqxc" +
                "t6Bh8gMfq+mKV/qZon4k7lK8ksyeIrCi9biEVKI4rIhXbykNjC" +
                "7Gtx5W9BnufK1rd1bGK6WBcIz9Oa0XWDvSseKPKl7pu7oryit5" +
                "pMer16K8om/ilb2VWk7BSvEqMczv+R8y8Yq+Paz+EuUVaeCtGq" +
                "tOMAduhztgNj/UKYK5MIsfzEqJVzN5K5hnHqHildVOXk2LxD1O" +
                "K7qDLYP+J98O38WCxskPeXFI5cthhi4J5LqHrltHEtjLD3EzLs" +
                "6auFpeJL9lLwIN5Ae5WRYRr+R6rfWNm8fC3ort62ECq3AN3CZj" +
                "cPvEJf6Zx0D7ylrmdK/B9bH6jW7nTv18l7AS+wB4BB6UtQ/JFk" +
                "vtBrBIlrS3yLwUNIlsDU5uifx2OSzUkZS6YDf06u2OAVYiXmXE" +
                "KvaueBoYxcrYmgtWSbp+46e4vXaH9Dr4bWTrUX3Ht8Sdn5xF97" +
                "dv5PgF8g6XJy7NDSvBqwArKmkvBov13azw4hUsIV7peOWcBsXm" +
                "/SJemfeRZ19Lx1O8ontS4bSCsuQyoYF8I5ATLGgn4pWvgasKuo" +
                "TjldJAOICwauTFK/6GxyuhgcqzexrIN0TjVbI5laLx6qxUDaTP" +
                "KInVztR4BY9RSccrTwOppp1JT2S6BtqdVLyiGopXcp0Wr+hbxK" +
                "vPVLySx1C8EhoIzQSv+Lr0eEVYjYhqYFy8CjTQ8+z8LaWBVPe4" +
                "0kB4GheYS+AJeAqWJp+BZbAcnjMfNx+FJ61B5mJYQd5ikfMTLo" +
                "Rn9JPeKvKkYsEFKTx7Msoreq4e813qXSGPep5f2zbzk5Y8ITde" +
                "KW+Rqwaay2I5PDZnDfwhkwbyP6fXm+Wh8hPZW/d8YOga9TsC3+" +
                "7xin8Z8CrZWXp24hW8gE/xtfxrwStzjeJVwUjh2QWvcAL/Kswr" +
                "+VuTA15RSfNKPrkNA14lRiteyb5k5tW5cbzyr8HnVbwPhOfjeW" +
                "V9HMcrzwdm55WZqAqvIihPy84rnKJ59Y3PqxcVr3Alb5M8Dl6F" +
                "NbiBl8LLsBpegZfsBubLuB7k2yVuxj+ivL9sKlYg8QufR1JFpF" +
                "iCb+MmeYx2RvgePosv4KqYp+VdXK3iFb4Z9RaRo96papwP8eqW" +
                "XHnFS9y8FuvAjPH1tiyMnJbDFaXXrPJ4ZfzT4xVhVQGFiYmw1p" +
                "oK5OYSE8S7MEMZr65T8Srw7MSr8dS7eSA5Lnhl+XdL8kriB8wK" +
                "9S/Eq0kBrwLPHuVV1qsamqqBqZ5d8mpClFcKq1hehTy73M7g2a" +
                "2Q4ww8u2x5Xbpnj6B8c3bPbnyh2wp4tU7xim+3hmNzJN/szOfb" +
                "WCEpFQFrTSWMZC9YA9aEr2WOfu4rmHgHtG2kMzemPW+xT5XCij" +
                "XyY5jEyjZjPeuGPHi1tc54VZTRc6/LcubN2Vv3sfI9MLbweGUP" +
                "Vrxy6O2aNRU5JnkPilkSW0KJh5XHK5aAMmbzez1eibwFFoXjlc" +
                "+raxWvNPsbRXmlckxhXllzaopXOD8Lr/6eJ6+Kq8srlbvN8i58" +
                "soeVz6vrFa9Ydy9vQXu/o3piFkzDw6HY3i7yFnRED9lubN5CYe" +
                "VpYApW07UGzo1qYOADo1iJPHtq3qKWsMpXA0/IhBWV8sYq1LaH" +
                "lc5bsHFsrMLK/p7vZldBIbscjxDxCluzUcSr0Ww8u9Leo3l1pM" +
                "KKjZH5wBSsrEdjsZoWh5X9Q1y8qhmsaj1eda0uVrnEq3SssERh" +
                "RTU+rwiDiVBoA7YRWLGk4JXQQKqXGoh0P5UGOudANyxNw2pJGC" +
                "s8OoxVoIEaK7P+sEq0zhOrEXWLlccrQq0TfTrCbFk+Fjs4f1Pe" +
                "go5Roxnzc465j8XczxlWrE8VPrC6i5e3SPEWO3P1FomOeXqLUT" +
                "n6ntvSvUXl+cCU81PygXg8zMHOeBz5wB9DbU4NaWwzYsGenK7h" +
                "8dyxsn9wa2Spaj5QYlWWJ1bj6xYr7OLX6Dy7LM+FWcl2Aivs6v" +
                "EKT8r5Gp6oAlZmTWJVNc8uNDAvrCbVE69WGoVGQ+xLpQ38O+xD" +
                "VzJRYAWLcH1yXjRvITx7ZXkL60mVtzAahX5vocJK5C10G29m1s" +
                "C88ha18H4Vn2dPzMnx7AVVw0rl2d2MeXYq6Xwg9uO7/bkxMm8h" +
                "8oF0heQtkg+qPLsaw1eeXYwLK2/h5QOtpyI+UOcDrWlePjCTDw" +
                "zyganjwnIrbVw413yg9hZp+cCMPjDHfGDigbh8YJy3SM0HKm9R" +
                "eT7Q1Xn2kLfw8+wG/QoOEHl2upoWOBDPCfIWsML3gTrPrvIWGb" +
                "RhWS55C/8pb7q/amBiYbXV8+bqnIWD9PpcvACH4vk4GIcYfYxe" +
                "gbcIckwCKz/7WBlWy6uC1b4Zr4ze2X/T3lzHWOnxI5CzvXCY4h" +
                "WVLoLnpLe4UO5dIeKVLD0TxCtar43tCR1tjI7His6JxKsMvaqT" +
                "eGUsyO/5cN6vJ16pcQ/xdrwh6tlxvT4id2/xjPYWV6Rjleot6h" +
                "OrfDXQ+aiOsbrEw8r3FhuMa8LeAtcLb4FXKKzkvNsJAisoE1hB" +
                "N3wLegmshLfA16S3eFZjNdHzFsYkz1sorIS3CLBK9xYKq9r2Fo" +
                "JX+XiL5PQ69hb+WCMgXo3jcazCivZIrASv4MiAV9CBrnVClFcK" +
                "KzpjgObVcx5WsqcCqxsErwKs4nkFreN5FYdVHK8CrPxairE4Rp" +
                "dbRXkF7TLxCnrotcZKb/lYyS1DfvtYUVnOFIbYGUTQJo5XHlb6" +
                "mJ5pZ2ms/LsyTq+lQsGr+qiXcSq84mmgNy5Mn4gGGvO8eIV6xp" +
                "nG6nmFVVy82pc0kC1262mppgbepNe3Eq+m01ohHssruadDurcQ" +
                "vJLrAbonLxj3Blip9yuPV5V5i595lYVXerzdIPcX5NmNF8X4Fd" +
                "6m8uy0p8R4LsizK88u8uz6XTg1z/5iJM9+e2V59nC8ypxnt5fX" +
                "Rp7dXppfnj1yTMY8u7GkxvLsegzfWGOsNl71r/dlXJGauxXjV6" +
                "l3iY2JZfjKzO9X1VvstVn1YXnVNdB+Zf/SQP9aP8It+DF+gJ/o" +
                "7c/S8uyROW9YybuFtaqmscqh/9XAKl/PXm9Y/YD/wT20/pFLRc" +
                "K9yRUKK94qFSveIgtWMiKF57PnglVl89njNTA8nz3AKvf57PaH" +
                "+ypWvNL56OjrlnoXZjdqlbsp/V041/mBRoybq54PrIoGZl98rL" +
                "bUF1YsL43BleyBCFZ+/qXqeQsfq401htXyWsFqab1htTCfs433" +
                "PB8oNNDYBIUFH7FVwgcKDZTzmKS3ED6Qy3lqlcyNWeNpYPp8C+" +
                "1sYnyg0sCqzLfwNDDqA4UG5uYDM+YtatQHVme+BW9W2dwY43Ps" +
                "hHvYWvwx0mb1vMVLMb56Ru16iwCr3HlVV96ipsfwjX9F3q/2iP" +
                "crOS7svV/trsL7lfb+xi7BK7Yx//erLNcyNBWrupnHVHu8in+/" +
                "Yu9685jknUzxu8H4Fa0JK8OfyVLp+FVFTXt2p2Nt8Mpp79bTkl" +
                "+e3fifN9bI/ip4lYqVx6ucsHrN41U6VrU/1rjvaWBNYcW2eT5Q" +
                "YcW+qIHxq3XKB5qsPnK3VeBV2f6JlRoXZtvVuLCZZF/mMd/i9c" +
                "wa+DOv8tBAPS5sNjHJM7Ov9FarVA2s0nyLN2oaK+eiWuHVsP0z" +
                "Xok5Z0z/L52XhsdExJwziZ98CiHuX+p6Jo0/JhL513zqmEgl9z" +
                "vTGEWVxkRoHRkTYfofk+ljIpnmB+7rY43wf5z0psQ=");
            
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
            final int compressedBytes = 1634;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWnuM1EQY/2Z2Zre363EImKCgCHKn4CmK4OsQUBF5+EKCnh" +
                "woDxHxyRGJiUruRBMEY+IDkTuIRqP4OCNBTv8SjXAHK0KUi4JR" +
                "FHyA76AELxwY69dpt9vudrluu0vbSyeZZ6ezbX/7+77ffC1pBh" +
                "KJy5hIUpahDPPJ0JfXYb8VKsT4DvIp+RjOE+0PyDtYriMbsNwE" +
                "Y0gL+RzPuJF8qBzlLeQzspa8y0YoPbgT7hKj9XACzt5O3pdFIh" +
                "vljAQDUy2y1TR+SckKLK8S7QkwUbZIMBvzXK2d0Ed7yDI9qrX7" +
                "p0Yj94v+uSXLZcsEV2j1DTDJMDod862Y7xC9iCijENOPl4qyt+" +
                "WKg8x9/qgYHYb5Un3OlVlnTYaboDrdJ4vVms1gO4zzWJtYs84w" +
                "Mku2mXir5Wi9XMRE3rY/V8VKluPlskdJxcrxvTazOVoraVhT8E" +
                "obF7zS2iZeYRa8wlrl1WaNV7cbkN6lYnUsXhmuRuMVfc3u9Su8" +
                "yhcr+rLdM9hOP2DFvtLqZXi/DEpAEr0n4UQVK+hH0TLCmbSUKv" +
                "akBB6E88V9xuBiinNhFOZa8bSuE+XNcAvfoj/BWTCTbhMtqmJF" +
                "y0SvF5xkeM6nG9pnYR6stc/BPBQu6hSnmkysoLve6qnVfWABDI" +
                "AzVKygMsUrGA4XajNGwGVaaz6MM622EKbAVEN/nii5aY7wH9BN" +
                "75+K+TTxLJeC8jtnm7GCIXCBPrcKRsJorMfC1ViOh2uwvN6w9j" +
                "S4DWbgs9sOgk+sgUwjNaSa7mcrWaORVzqqq2z/a5LH3wY64VV8" +
                "UrBsIN2r1fPFGhO03j2Izev6HPTZdB6tpXdnnX2f7JPkCKvJnm" +
                "HV5shPzdRY02S6m3b+RIpXVLA7cki1gTpKqH+olONKDmurHMzm" +
                "lWoDfaItqoOqLRCv5rS24E3OtUW0RNMW67Oxyk9bFBmrqcHEim" +
                "1hm02r/Zflr5J214omgqHZaWtAsfo643mzLKy+sY1VTaGxis8t" +
                "Cq/mBHZ/9a1xfxWVXNjAezUbuNvfNjA2MKC82mO2gdHpLmzgoo" +
                "DELWqDyqvoEvYj26fwiv2Mz3sB2nOxJ1V4RSvy4RXWKq9+svid" +
                "kFeKp6x0h1Wsu9EGKvHAaEMKq/xsoI7VL/7GKv6wZ1iNdXN2tD" +
                "HW02QTESs6xrR+LftV9m0KGK9cYcX+YH+z39kB1sH+YX+yv2IH" +
                "2eGMGUdMvd8c/SPeYO3sUIgVvdalDvwXUKeDRJLRt2RZGqwfyb" +
                "CBSuw2bQNhlNkGKrFb3QYexdlaXASouMaJqg1UYrdpG5gdu1Vt" +
                "oKS0hwprta6z65dMsdF07FYf6YNZj92mNbtV7Bbb4zLOnmLq2Y" +
                "zd2sUKqjCL2K3ojRelRexWqlRjt6SZl/HSaIf5nYg7f8W7GZ7l" +
                "I278Vfy9zrByFA/0bH8VPeLkLGmR9XhstFZfruvMHvq/oi70Vy" +
                "5tYL07G6i3BK+kp2J7jbyKJB3w6hSf60DPeBX5xB1WUn/eN41V" +
                "fFAB9lfrw/1VMfZXWRG4kSmshOorVzQ79+ze4lu7Fq/cYaVodl" +
                "7ODvAKVbPjiEmzS7PdaXa2S6uLpNmdaAv7vCr09xbObGDqewuB" +
                "x8J0OyG0MntFlOIbEtLb3fWxV13war+FKnogm1ekVzB4xda481" +
                "d8ODDpIWV/JZ5EAfZXfFjm/irlr/LdX9nQsw72VwqvvNlfmTV7" +
                "vvsrYQOr0nGLlA2UNmXHLRTNnq8NTO2vgmkD/aHZc+2vxIqCW8" +
                "r3FqycP2b/e4tjXmWZ7HnqGjqwRPuujD9uWFewO/V9YIiVd3EL" +
                "PSqxFH1Je7yGdOT0aKa3vKTteN5bol8xsApajEnH6mnze2EL7Z" +
                "L0jgmJAV0LK3cJdeAzxhiT4Yjzd427j1/cIlg20F3iz/qZV8XB" +
                "Kqi8wv9yQyfHVyOiz3UlrILKK8RheedYeeavhoS8Mvmr1byBv8" +
                "BXkCRv5C/y5/kqt/6Krwz9VVFY9aai2TH7U7MvDnll5JXeKtw7" +
                "/A2F4lViSYhVTpZt9FfcIvFSiFVe+G0LNXtQEq3lX3imA9eEWF" +
                "n7K76zQP7qy4L5q6YQK8vnlFNbYLbASvRb+HfZWFmu7gyrtSFW" +
                "+elAvic/XhUQq49CrJxpdv69TRv4g7/3wl1PWxhtoD4msMJat4" +
                "EZmnFfLl7hXCe8ag2xSif4H6kJRwM=");
            
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
            final int compressedBytes = 537;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmktLw0AUhb1JZ6oFLVT0X7hTEdyKinsV3IhuhdYg6J8SRZ" +
                "GC+ABfIIr4WLtT8AWKVau1Sgw1pKltimkbmpueC8lkpiVl5uOc" +
                "O4/qulPQgVme29o2aSlXbpn1/d9S3Jr1U1qm1ZJvO6Gk+bSrO/" +
                "/mke5hqAk9oGFnJe7Ks6JtsPI+xL01TofOrGjHzopW8qzoDKx8" +
                "QPHh/x5o+4YDK+MTsKrW6dYq1ZV4hK58o6sn6IpbKJp4DkZP+H" +
                "ugeAtdiReRosPQTSkPFGk6LuWBtOfkgaFreKAnTveuJtUNqzfr" +
                "pXSlzBS1xaErf84t3OpKZKArT3T1aYxQ2rgyjuN3WVC7QL6qG6" +
                "ssWDEl94V85WM639AVG1Y6WLGZB35IsrOiLC9WtNg4rKSArpiS" +
                "k7b5QyR3b1Vixr3Fag0bV7P7NytRsKqaThi6YsWrDaxYcIpiP5" +
                "ANqxhYseLVDg8MRiia7AhGT4Jx1ig7cdbIgZX1VLPzK9kFVnXK" +
                "Zt1B6Ql/Xck+2VtOV7Lfta56oCuPWA3IYTlUUw8cBCvPXG4Ec3" +
                "bmBEfNOXusYAZf4d4tWHnKagys2K2Fi1jJcbDygZYmbFRwfsVH" +
                "T5G8ruQkPJAFM01OYS3sj/VVbj9wuhH2A9lnqIKzKTX91wPVV/" +
                "f5Sk35M1+xZxVvnP9bsGeVACs2rGbBig0rDazYsJoDKzas5sGK" +
                "DasFsOISTT8CD5Yw");
            
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
            final int compressedBytes = 540;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmjlLA0EUx511Z9YoIoidWlhb2OQT2NkJVlqKIKKI6bww0c" +
                "qz1ESFfAMTjcELI3iBKOJR2ylaahUQi3USlmgSlhiya+aF/4M5" +
                "d9ji/fYd+3ZZ3LSEXZlZwi6t8YHdsFNrfswist9hCdmfy3bB7t" +
                "P7J9b1OxZlMTFu5gm7ZXvW7My0FXZtQmxFTEgNJWX7tNXfU9bq" +
                "ETorG6tJsCJKbupnrtWm+3qtUfaezK4hW03xd9YaoN2S6UzDrs" +
                "iwmgErMqz8YEWGVQCsyLCaBSsyrObAioqwuFgRC2JJLKbqFjws" +
                "5vlmqXULHkLd4g9WslzU6cE/nQriiVaEbqjgW2wntKSMD9zi6z" +
                "zI19gV3+BhvgofqDCrCOIVjXglWUUtPTn2TUQfACt37Cozc+77" +
                "1TZYueQDY7ArMqz2kVsQiXAHhU7o/dBSBcerQ9iVSz7wCDk7jZ" +
                "xdvGbVKEayVukalObThvNqGaN4ypXk/w4dqBOv9GfxkYpX+ptD" +
                "OfsLfKAboo3l+kCR/O0DtSH4wErOAw0NdkWGVTVYkWHlASt3WB" +
                "l1TrPiXrAqjxhN0EEF+8Bm2FWZsnqf0QItKBKvWp22K/0LdkXG" +
                "B7aBFRlW7WBFhlUHWP0zxQyrX3tpVnJMWOuLnPzem2Jlcz+wUt" +
                "GuumBXZFh1gxUZVj1g5RYro9fh96tdsCpb3aIPWlCkbuHHv5w0" +
                "xAgUOoF/OVWRqm8rJH0L");
            
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
            final int compressedBytes = 652;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt9LVEEUx5u986sghHoVwn/CP6CgevBR0NcewlQQM+jdfo" +
                "CZKGRaKiyroIiJYooglVBttSiRRSFBb1paKRaUrPpwve3etHVb" +
                "bXVOO1e/B+7cmdlhdzmf+51zZuayEdc3FnNTjD3z76/ZJHvs1x" +
                "+yAa8cYo+88ql3RdlUon/c//wVG2T3Va2bZuwlG/VrT9yMxiZc" +
                "WGbvjKhrrquuJlmJsLoiOvbKStwFq51NXc9mtCxLsAqb1pWTB1" +
                "Y0uiKYAyNgRaLETv58+xE8Bi9ZE696VY/qy6wrdS9rXXVDV7mx" +
                "0EUVhRf2bbx6AV1Rs1IT27Nyav4xD7wEVjmbAyfhBSt0FVfv2L" +
                "J3X/Hba2kjPqS03lj2//utzbDfEnzntPPgd90Z+5uuQpVpfVV4" +
                "yi2dA9/DC5asrz7xGfXxV27B5wzlgbPILXI0787DB/bk7J6uPp" +
                "vUFYeuaFTzVS1stpwp5BaBprkIHwSG1Q/4wJ54pVblGaP7gSuI" +
                "V7laX+nj8IIdpvM367zgD0ZHEuXR0DGvPLzRq7xL74J4Hjy9Z1" +
                "IncC4cpHjl14zFK12IeEXDSp8UbeKOaGUx0S7CogXvnFnM6pS8" +
                "IW/K+uT7gbIOrP6PyYasRpcRzYFFYEWkq2J51uz6Sp4GKxJWcV" +
                "2Kc2GSDLuEIg/kM/oc9tntN9628RyczzCiA14KzGr5Anxgia4W" +
                "+Hddzpd0Bf/JF/k3rye+ZcRqSuvLLn9nmWO/nl5X1fCBLTl7Ir" +
                "eoQW4RGFaXwSoQc1zdDpEG++wW6cqvmdtjqoeuaFjpJuOsGsGK" +
                "iFWzvmWWFR8GKyJWLcZ1dRusSDKLVpzhH+jcIgxdUbHSkSQrrK" +
                "8OoK66wIrCDq0DLfmT9g==");
            
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
            final int compressedBytes = 595;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk9rE0EYxp1kZncGoaKg38BLEQ/VIJhbLSq0+gl6q8WDUO" +
                "hXqPgR/FsIhar0ojE2xAaNWk1q2pD+OfkBKohWsRSTQz2M02QL" +
                "iWEDMTPJTvK8sLuzOwOB9zfPPO/shiSlFyQv64LkvOsWKZBlr/" +
                "2GPFfnBMmo80d1ZMlm5flbr3+DxMlLPi8bghRJymt9kL5B1iTC" +
                "PzuKFd3mjw9Y0a96WNEvYGWGFX+iW1f0D1iZCPqDP1XnX+oo0Z" +
                "90V0q+8M+I/bq77//5O2X6G9k2FfzZoa5q5n1FV+qa8e6zDcpQ" +
                "uvLRDHRljlUcrKxxslzrrPgLsDJbB2qv2ROoLaxhlQSrLvlWCj" +
                "kI1F74FfbCfboGLoGVKVY8XWUFXVnvV++Qg6DoipWUX71ne366" +
                "YmXoqof9Kg9WxvxqVS8rughW1uiqAFZmWPGiblZsAKys0dU6WB" +
                "nS1Tf2kN1n90iePWIxdpfNtq2rB2BliNWOlO7tqq5YzJ0Bq86E" +
                "e6eV0c5NKcVRutJ8FM1jRgfVr8SxdnUlUFt0gJU4IY5jDQwuK3" +
                "HSuepc0VkHOpfBqjshTiEHwVkD6bY4jW8i/bkXdj+ZZCUG+1xX" +
                "Z6ArO1jxOTVbz+L7lTUVxJBvzzlkp3f9SpyHrsyE+zn8+rAdTj" +
                "f2h6ZDtxqeTXVxBYj0eW1xAbWFda51Mfi6QjThF0UOrGE1jBwE" +
                "xa/EpeZ1oBNt+d1tBH5lTc0+AlbmWIkxraxGwcogq+taWV0DK4" +
                "P7q3Hsr2z0K/zfIsB14GQtKzGhYQ28AVYm4shfZ6HhKg==");
            
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
            final int compressedBytes = 514;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7tKA0EYhZ24O5uZQiGIjQiCjail+AhqoT6DhZBCCASsbA" +
                "Sx8NoLYtDCFMY7KgpJQE1QDMHYiW0EH8JiTeIiwSRozIw7szkH" +
                "Mnttcj7OP/+ys+TMdkTuiyMLfh2nne0TyZBrZz9ODgvjCUkUxt" +
                "vCL0VypfNJ5/ojOSKnbNquEMmSC2fvxq4p8mBDVcWmfnVXSP1/" +
                "QjqagRf5lquyK3/P1SxyJYcVm6PLdJWuFFmZEbpkbjbKytwAq5" +
                "9F1+q6O1hiNW9Frb3aubJi9bKydsFKUq4W6RgdFVkD6QhY/UO3" +
                "USWXvrBvpuJcCF4pyW8dHmjDagseeLhnj2C+ktRb7IhmZQ6BlT" +
                "a5ioKVNqxiYCWLlZFn+0VWxpsYVsYrWEmarw5E58p4ByttauAR" +
                "WGnD6histGF1DlbasLoEK3fkC7MruODZXMWRK21YJcDKHbEkPN" +
                "BovkrBBc/WwDRqoEs18A4eeDhXGeRKDiuWFc0K7xpd7C2e4YI2" +
                "rF7ggprzFWtveL5qQw2Uz4oFrAWsZ1eZFTewnl2fXHFTZB8IVv" +
                "JYGXkewNoYbVh1gpU3enbeBRcUylU3cuWRXPXABTWfhRvPlR/f" +
                "iUhj5d/+ZCWqBvJesJIh3ld+1JqrVgPxDapKNZCNi6yBvF9mrv" +
                "hAk7OaFPqucUIqq8Em79mH0bOrr5YPnuoc/w==");
            
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
            final int compressedBytes = 439;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmUlKA0EUhn2hipha6H3cqoheQARBEBe60qArL6AHcMgALj" +
                "yAYpQg4jxAMGh0LwrGkI2JB3DRtlK4EEmDVlH1iv9Bd4quhg7/" +
                "x/+G7ihKilRW9UUID4L2VL/My3W5RhVZkBtyVRb1zpX+vacqne" +
                "n1IW3F5x06is8X8XFJd1/Xj/V+jbapJHO/POeGynp13uHfXINI" +
                "R1YDelX5sfNnVmIKrOyw+l4ZY6UGwcpGqKGkO8QkVPImBw6jXj" +
                "Hx1Qh8FRDNUWgQcG8xhhzIhtU4WFnqLSZMs+peASs2vsIszIfV" +
                "NFi5iVRWzUCFYH01C1+xYTUHVmxYzYOVs3q1ABWC9dUifOXMV0" +
                "tQgQ2rZajgQ6h80h34JuJPvVIFfGtk4qsifBUQzU1o4GvPnun9" +
                "bw7M9CAHspmvSmDFhtUuWDmbr8pQIVhf7cNXznx1ABU8mYVPRD" +
                "3d+PSVaJrxVfoFvnI0X51CA0Y5sAoVgu0tbpEDbbFSNbOsBOYr" +
                "V/XqARqwYfUIDdiweoIGAfcWz6hXbFg1wYoNq1ewYsOqBVa2WI" +
                "m6apt8HyjwPtASK/Vm2lfiHaxsRNcHdyi3ZA==");
            
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
            final int compressedBytes = 409;
            final int uncompressedBytes = 19261;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2tsrBGEYx3FvvRrNuPQnKImbLSWncrhx+C/cIW6UK3+DP8" +
                "GNQ0JOyYWsYimthL+Bcu/GKY2hyYWwqXm8+7zzfWtnp52t3Z5P" +
                "v3med9s4rrTC+5hVFcvsfp6dfblymj5fm3NzlJ4fmI3kuG2Kyb" +
                "GUPE7M1cfrh+n1S7NpdsKHbz7nwuylZ8e/fJsyIv9s9YiVlJW9" +
                "CZ/erexdNlb2FisZq/A561zZF6yErF6ztqptw0pLv4rqsXKzog" +
                "ZqUE2zRdTIbKHGqgmrfParunmsHPWrZmrg8RzYQq7UWLViJThb" +
                "FJgtNFhF7cFysPpzroK1v1oFS1g5mi06qIEaq05q4PFs0cU9UI" +
                "1VN1ZqrHqwUmPVi5Uaqz6sBPfC/eyFVeyFB7LOFf+3cLa/GqQG" +
                "HverIXIl2K+G6Vcq+tUI/SrH98AxrNRYjWMl2K8m6Fc5zdUkVo" +
                "K5miJXaqymsVLwq8RMpXfYUarkcb+aJVciuZojV7nO1QK5UmO1" +
                "iJUaqxWs1FitY6XGagsrR5PiPjXwOFdFciWxat4AmxXhBA==");
            
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
            final int compressedBytes = 140;
            final int uncompressedBytes = 4709;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt00EKg0AMBVD/2vtfQ6mIK5VaFUVR67JF6FHiINmIuJQS+R" +
                "8mE2Z2eQSxaNDKLqj1ntGj0D5F4OoDmaulOxXe23uu/xNCRH4u" +
                "h2BAot1LToNOmPPpXGH1pJUZq4ZWZqw6Wpmx6mllxmqk1X/iz5" +
                "zBjffqw70yY/WllRmrhVZmrH60uiLeCvavQmw=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 107;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MERACAIAzD3H5q6gOebg2QEKYImP3Wgs8gvAOaf+ac+AA" +
                "D2I9QHAPOFNfWXL/2PfMif85Fv9dNfyA8AAAAA9OB/CwAAAO9T" +
                "ANz/ALifAQDm7kf2OwAAAAAAAAAAAAAAAAB4uNaQxj8=");
            
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
            final int compressedBytes = 82;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3CESADAIAzD+/2iYnsGgOBLfyspGAAD8qpXj/LQfAAAAAA" +
                "AAAAAAAAAAAAAA4Dr/DQD2HQAAAAAAAAAAAAAAAAAAAABgE/8q" +
                "rPYA5F+5QQ==");
            
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
            final int rows = 619;
            final int cols = 8;
            final int compressedBytes = 49;
            final int uncompressedBytes = 19809;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0DEBAAAIA6D1D60m0NsDIpAAmz6VIAAAAAAAAAAAAAAAAA" +
                "AAAAAAgG8Gm20e4Q==");
            
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
            final int rows = 30;
            final int cols = 124;
            final int compressedBytes = 148;
            final int uncompressedBytes = 14881;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MkNwjAARFFnZwlL6CnU5c6hBDhGnqe5+vSfLEuupf7Y/q" +
                "nF2lj54wxv3sbbeBtvO7b3e9UpynvWKcr7qVOUd6dTlPeoU5T3" +
                "oFOU90OnKO+LTlHeJ52838bbmvDudYry1inL+65TlPdZpyjvTa" +
                "co75dOUd6LTlHek05R3led/K9Zs943ndxv423H9y5fwlh5tQ==");
            
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
