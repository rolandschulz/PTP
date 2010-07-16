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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 46, 31, 1, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 17, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 0, 3, 13, 4, 106, 46, 155, 156, 157, 5, 158, 13, 6, 26, 159, 162, 178, 7, 8, 160, 161, 163, 0, 168, 179, 169, 180, 171, 9, 10, 97, 181, 182, 183, 11, 172, 184, 46, 12, 185, 13, 186, 187, 188, 189, 190, 191, 192, 46, 46, 14, 193, 194, 0, 15, 195, 16, 196, 197, 198, 199, 200, 17, 201, 18, 19, 202, 203, 0, 20, 21, 204, 1, 205, 206, 74, 2, 22, 207, 208, 209, 210, 211, 23, 24, 25, 26, 212, 213, 178, 180, 214, 215, 27, 216, 28, 74, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 74, 227, 29, 228, 229, 230, 231, 232, 233, 234, 235, 236, 86, 105, 237, 30, 238, 239, 31, 240, 3, 241, 242, 243, 244, 32, 0, 1, 2, 245, 246, 247, 46, 33, 248, 249, 86, 250, 184, 179, 185, 149, 13, 186, 187, 188, 189, 190, 251, 191, 192, 252, 181, 4, 5, 96, 6, 253, 34, 254, 35, 255, 105, 256, 193, 257, 258, 259, 194, 105, 260, 261, 106, 107, 108, 112, 262, 115, 120, 122, 263, 182, 196, 264, 265, 266, 197, 200, 267, 268, 106, 269, 270, 271, 272, 7, 273, 8, 274, 275, 9, 10, 276, 0, 11, 36, 37, 38, 12, 1, 0, 13, 14, 15, 16, 17, 2, 13, 3, 18, 277, 19, 20, 4, 278, 279, 21, 280, 1, 39, 23, 40, 24, 177, 41, 42, 28, 43, 29, 31, 32, 281, 282, 34, 30, 35, 283, 37, 284, 285, 39, 44, 40, 41, 286, 42, 45, 46, 47, 48, 49, 50, 287, 51, 288, 52, 289, 53, 290, 291, 54, 55, 56, 33, 57, 292, 0, 293, 294, 58, 59, 60, 61, 62, 295, 63, 64, 296, 297, 298, 65, 66, 67, 68, 299, 69, 70, 71, 5, 72, 73, 300, 74, 75, 76, 77, 78, 79, 7, 301, 302, 80, 303, 0, 81, 304, 82, 83, 84, 85, 87, 305, 88, 89, 90, 306, 91, 92, 93, 307, 94, 95, 96, 308, 98, 309, 99, 100, 101, 102, 8, 310, 311, 312, 313, 103, 9, 314, 315, 316, 317, 318, 319, 320, 321, 104, 105, 107, 10, 108, 109, 322, 110, 11, 111, 112, 113, 323, 324, 114, 115, 116, 0, 325, 117, 12, 118, 119, 120, 13, 45, 13, 326, 327, 121, 122, 328, 123, 14, 124, 125, 15, 126, 127, 329, 98, 330, 17, 128, 129, 130, 21, 131, 132, 16, 331, 31, 133, 134, 332, 17, 333, 334, 3, 335, 4, 46, 135, 136, 5, 336, 337, 137, 6, 338, 138, 139, 339, 340, 341, 140, 18, 342, 343, 344, 345, 141, 142, 47, 0, 143, 144, 145, 146, 147, 346, 148, 19, 48, 49, 347, 348, 349, 350, 149, 150, 20, 351, 151, 152, 352, 153, 50, 153, 154, 155, 353, 354, 355, 356, 357, 1, 358, 359, 360, 361, 362, 363, 155, 156, 158, 161, 163, 22, 13, 157, 364, 365, 366, 367, 368, 369, 51, 160, 370, 371, 164, 372, 373, 374, 165, 375, 376, 377, 378, 166, 379, 2, 380, 381, 106, 167, 382, 383, 384, 385, 386, 168, 387, 388, 389, 390, 391, 169, 170, 392, 393, 394, 156, 172, 395, 396, 397, 398, 17, 198, 24, 399, 173, 400, 199, 401, 201, 402, 202, 204, 403, 206, 46, 174, 175, 176, 177, 23, 404, 178, 405, 406, 179, 407, 207, 408, 409, 0, 181, 52, 53, 124, 410, 126, 411, 185, 186, 412, 413, 17, 208, 414, 188, 415, 7, 22, 80, 25, 28, 416, 30, 417, 418, 419, 36, 166, 420, 38, 212, 57, 0, 3, 421, 422, 1, 2, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 25, 28, 36, 436, 437, 438, 439, 440, 441, 442, 443, 444, 445, 446, 447, 209, 448, 449, 450, 451, 452, 453, 454, 455, 456, 65, 457, 66, 458, 459, 460, 67, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 474, 475, 476, 477, 478, 479, 7, 480, 216, 3, 481, 221, 195, 482, 483, 202, 484, 211, 68, 485, 486, 487, 488, 489, 490, 491, 81, 82, 86, 92, 213, 492, 493, 494, 94, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 516, 182, 517, 518, 519, 214, 520, 521, 522, 215, 523, 524, 525, 526, 527, 528, 529, 530, 531, 95, 532, 226, 533, 534, 535, 96, 227, 536, 537, 538, 234, 539, 97, 540, 98, 106, 541, 193, 194, 542, 196, 543, 197, 544, 203, 545, 546, 112, 113, 114, 138, 54, 547, 133, 141, 548, 4, 549, 189, 550, 551, 180, 552, 553, 554, 555, 556, 557, 558, 5, 559, 560, 561, 562, 563, 6, 564, 8, 9, 10, 11, 12, 13, 565, 566, 567, 568, 569, 142, 570, 149, 571, 151, 236, 134, 572, 204, 573, 206, 574, 575, 152, 576, 577, 578, 579, 14, 38, 580, 581, 582, 184, 583, 584, 198, 585, 586, 587, 588, 589, 237, 590, 153, 591, 592, 593, 594, 595, 596, 597, 598, 599, 154, 600, 601, 602, 603, 159, 604, 605, 162, 606, 607, 608, 609, 8, 610, 611, 612, 613, 614, 615, 616, 617, 618, 619, 199, 200, 620, 201, 621, 127, 622, 208, 16, 623, 624, 625, 626, 627, 628, 55, 629, 164, 165, 630, 631, 632, 166, 633, 167, 170, 171, 173, 209, 634, 178, 4, 168, 169, 635, 636, 9, 637, 638, 639, 640, 641, 642, 643, 644, 645, 646, 172, 17, 179, 181, 647, 58, 183, 186, 192, 1, 207, 210, 59, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 648, 239, 649, 212, 650, 0, 651, 74, 57, 652, 653, 654, 185, 188, 190, 227, 228, 60, 229, 230, 61, 242, 655, 18, 656, 231, 232, 233, 234, 235, 237, 657, 658, 238, 659, 660, 236, 240, 239, 241, 242, 63, 661, 662, 663, 243, 244, 664, 665, 666, 667, 245, 10, 246, 19, 20, 668, 669, 217, 670, 218, 671, 672, 673, 674, 675, 58, 247, 64, 676, 677, 678, 679, 248, 249, 5, 680, 681, 682, 683, 684, 685, 247, 250, 251, 686, 69, 687, 249, 688, 689, 690, 691, 252, 7, 254, 255, 256, 257, 692, 693, 694, 258, 259, 260, 695, 261, 219, 70, 262, 263, 264, 265, 696, 266, 267, 268, 697, 270, 271, 275, 698, 8, 269, 272, 276, 220, 221, 71, 222, 223, 699, 72, 73, 128, 74, 75, 76, 700, 701, 253, 702, 224, 703, 273, 277, 281, 704, 705, 225, 228, 706, 707, 708, 229, 709, 710, 21, 711, 23, 230, 712, 231, 713, 714, 715, 716, 77, 282, 283, 717, 78, 31, 24, 79, 81, 58, 59, 82, 61, 86, 718, 63, 285, 284, 286, 719, 720, 232, 721, 287, 722, 233, 723, 92, 96, 59, 288, 289, 60, 254, 105, 234, 724, 61, 725, 235, 726, 62, 290, 291, 2, 63, 292, 83, 293, 294, 64, 295, 727, 296, 256, 728, 729, 1, 730, 260, 731, 298, 64, 732, 240, 733, 734, 241, 261, 262, 735, 736, 737, 738, 299, 300, 302, 244, 739, 740, 245, 263, 270, 741, 246, 742, 743, 248, 744, 745, 250, 84, 304, 308, 305, 69, 306, 307, 0, 251, 309, 311, 746, 747, 748, 303, 312, 313, 314, 315, 316, 320, 65, 0, 317, 318, 1, 322, 323, 2, 324, 325, 70, 326, 327, 328, 66, 330, 85, 2, 67, 331, 332, 333, 334, 336, 338, 71, 339, 340, 341, 342, 343, 344, 346, 347, 348, 349, 350, 352, 353, 354, 355, 356, 357, 358, 1, 252, 359, 360, 361, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 362, 374, 253, 73, 92, 94, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 749, 375, 69, 110, 379, 380, 382, 383, 384, 750, 385, 386, 387, 388, 376, 377, 751, 389, 391, 392, 395, 397, 401, 402, 405, 407, 408, 410, 411, 752, 412, 378, 381, 416, 390, 393, 394, 396, 399, 403, 406, 409, 413, 414, 415, 417, 418, 2, 753, 419, 420, 421, 422, 423, 424, 754, 425, 755, 756, 404, 757, 758, 426, 427, 759, 428, 429, 760, 255, 0, 761, 430, 762, 431, 763, 432, 764, 111, 765, 766, 767, 257, 258, 433, 434, 259, 435, 259, 436, 437, 768, 769, 438, 439, 440, 441, 442, 443, 260, 770, 444, 445, 446, 447, 448, 449, 450, 106, 451, 452, 68, 453, 771, 772, 115, 454, 455, 456, 3, 261, 457, 458, 459, 460, 4, 461, 773, 462, 463, 262, 263, 464, 465, 466, 467, 468, 774, 469, 775, 470, 471, 472, 473, 474, 475, 476, 5, 264, 477, 478, 479, 480, 481, 482, 483, 484, 486, 487, 488, 489, 490, 485, 776, 265, 777, 778, 70, 491, 116, 494, 495, 117, 492, 493, 496, 497, 779, 780, 781, 498, 782, 499, 500, 501, 502, 783, 784, 503, 785, 786, 266, 512, 513, 787, 515, 788, 789, 267, 504, 505, 506, 507, 508, 3, 118, 119, 516, 790, 519, 1, 791, 792, 4, 523, 525, 120, 87, 11, 509, 510, 88, 511, 514, 793, 517, 518, 266, 520, 526, 521, 273, 522, 524, 527, 528, 794, 529, 530, 6, 795, 796, 107, 71, 797, 798, 531, 532, 533, 799, 268, 800, 801, 269, 534, 802, 270, 3, 803, 804, 535, 536, 537, 538, 539, 89, 540, 541, 805, 542, 543, 545, 806, 807, 547, 808, 809, 271, 544, 810, 546, 12, 811, 812, 813, 814, 548, 815, 121, 549, 550, 552, 816, 551, 553, 122, 123, 125, 817, 272, 818, 554, 555, 274, 819, 556, 90, 820, 821, 822, 823, 273, 275, 91, 276, 277, 824, 825, 558, 559, 826, 827, 4, 828, 829, 830, 831, 92, 832, 126, 833, 834, 835, 560, 836, 5, 837, 838, 561, 839, 840, 94, 7, 841, 842, 843, 128, 844, 845, 846, 847, 278, 848, 849, 95, 96, 850, 279, 851, 562, 557, 563, 852, 853, 854, 855, 565, 856, 857, 129, 858, 0, 859, 860, 861, 130, 97, 101, 102, 131, 132, 133, 862, 139, 140, 143, 144, 863, 864, 103, 865, 72, 866, 867, 276, 868, 566, 567, 568, 569, 570, 571, 572, 278, 869, 145, 870, 871, 108, 73, 872, 74, 873, 5, 573, 574, 75, 146, 576, 577, 104, 575, 578, 109, 579, 874, 280, 281, 293, 580, 581, 582, 282, 283, 583, 875, 294, 876, 284, 877, 295, 285, 296, 878, 584, 879, 585, 586, 880, 587, 881, 588, 589, 590, 591, 592, 598, 593, 882, 286, 883, 287, 290, 884, 885, 886, 76, 594, 887, 888, 889, 890, 0, 891, 892, 893, 894, 895, 595, 896, 897, 898, 596, 597, 599, 600, 604, 899, 601, 147, 900, 901, 602, 605, 902, 903, 607, 904, 610, 905, 906, 603, 606, 608, 612, 907, 613, 908, 614, 609, 909, 910, 911, 611, 615, 6, 7, 616, 617, 618, 619, 912, 295, 913, 914, 915, 291, 620, 916, 297, 917, 292, 918, 919, 621, 622, 920, 921, 922, 623, 105, 624, 625, 626, 627, 628, 2, 923, 924, 925, 110, 77, 630, 633, 635, 636, 78, 637, 926, 638, 639, 927, 640, 928, 929, 79, 641, 930, 298, 642, 931, 932, 645, 933, 934, 935, 936, 937, 938, 939, 940, 941, 301, 646, 942, 943, 944, 945, 148, 649, 651, 946, 652, 947, 653, 654, 647, 948, 949, 950, 648, 655, 951, 0, 952, 953, 954, 150, 8, 155, 656, 650, 955, 657, 156, 658, 956, 659, 1, 957, 660, 662, 663, 958, 664, 304, 959, 665, 666, 667, 960, 157, 158, 961, 305, 300, 962, 668, 963, 669, 964, 670, 965, 966, 671, 672, 673, 967, 968, 161, 969, 970, 674, 675, 676, 106, 9, 677, 678, 971, 13, 972, 679, 10, 973, 974, 975, 976, 306, 977, 680, 163, 978, 307, 979, 308, 681, 980, 682, 981, 309, 683, 310, 314, 982, 322, 174, 175, 684, 80, 685, 983, 984, 985, 986, 987, 988, 989, 686, 990, 687, 991, 688, 323, 689, 325, 690, 992, 691, 107, 993, 994, 11, 692, 698, 701, 702, 995, 703, 996, 705, 997, 706, 693, 329, 694, 108, 998, 999, 12, 1000, 708, 696, 333, 1001, 334, 1002, 700, 176, 1003, 1004, 177, 1005, 183, 279, 704, 709, 1, 1006, 336, 1007, 1008, 109, 1009, 110, 1010, 338, 1011, 339, 1012, 81, 3, 4, 707, 710, 1013, 111, 82, 340, 1014, 341, 711, 1015, 9, 1016, 186, 712, 713, 1017, 715, 1018, 187, 311, 716, 714, 717, 718, 719, 720, 112, 314, 1019, 343, 111, 1020, 112, 1021, 113, 346, 721, 1022, 315, 342, 1023, 191, 1024, 1025, 722, 1026, 1027, 723, 724, 192, 83, 725, 193, 726, 727, 84, 728, 194, 729, 730, 115, 731, 732, 733, 1028, 734, 736, 737, 344, 1029, 735, 739, 13, 14, 740, 15, 1030, 741, 742, 743, 745, 1031, 749, 744, 16, 752, 17, 1032, 746, 750, 1033, 196, 758, 1034, 1035, 751, 753, 1036, 738, 347, 754, 755, 297, 756, 757, 1037, 1038, 1039, 759, 760, 762, 761, 2, 114, 85, 116, 763, 764, 766, 1040, 1041, 1042, 1043, 1044, 1045, 767, 768, 1046, 769, 770, 1047, 348, 86, 87, 772, 773, 88, 1048, 301, 117, 118, 0, 119, 120, 349, 774, 1049, 1050, 1051, 197, 775, 776, 777, 303, 779, 200, 1052, 310, 780, 781, 778, 782, 783, 1053, 1054, 350, 784, 787, 785, 316, 788, 8, 201, 789, 9, 10, 791, 792, 793, 794, 797, 1055, 796, 798, 1056, 801, 1057, 800, 1058, 121, 802, 803, 1059, 205, 129, 1060, 1061, 1062, 351, 1063, 1064, 1065, 352, 354, 795, 355, 1066, 804, 805, 1067, 122, 1068, 1069, 807, 1070, 18, 125, 356, 1071, 1072, 810, 811, 812, 11, 1073, 1074, 1075, 19, 357, 126, 1076, 814, 815, 1077, 358, 2, 207, 208, 210, 359, 360, 1078, 361, 1079, 1080, 363, 1081, 1082, 128, 1083, 131, 1084, 1085, 1086, 1087, 1088, 317, 212, 816, 1089, 318, 115, 364, 89, 319, 1090, 806, 817, 808, 818, 820, 821, 822, 320, 1091, 327, 823, 825, 1092, 116, 90, 826, 827, 132, 1093, 136, 1094, 1095, 1096, 1097, 139, 1098, 828, 829, 366, 1099, 1100, 1101, 1102, 1103, 5, 15, 1104, 1105, 1106, 1107, 830, 837, 1108, 1109, 831, 841, 843, 1110, 844, 845, 1111, 832, 846, 1112, 848, 369, 10, 852, 11, 12, 1113, 1114, 833, 835, 854, 20, 21, 216, 836, 1115, 217, 1116, 91, 838, 839, 840, 849, 856, 1117, 857, 858, 1118, 850, 851, 842, 1119, 1120, 1121, 321, 12, 218, 221, 1122, 853, 860, 13, 861, 859, 328, 1123, 370, 372, 13, 1124, 14, 1125, 1126, 864, 865, 862, 863, 866, 867, 222, 1127, 373, 868, 1128, 1129, 140, 1130, 869, 15, 1131, 22, 870, 141, 1132, 1133, 1134, 1135, 1136, 381, 871, 16, 1137, 398, 142, 1138, 1139, 1140, 1141, 1142, 400, 874, 1143, 376, 390, 1144, 399, 1145, 1146, 417, 1147, 1148, 1149, 143, 144, 7, 8, 872, 873, 875, 876, 418, 877, 329, 1150, 1151, 419, 332, 1152, 1153, 333, 878, 14, 879, 219, 880, 1154, 92, 1155, 1156, 220, 223, 224, 1157, 1158, 225, 93, 881, 882, 1159, 0, 226, 883, 884, 887, 888, 889, 890, 891, 893, 894, 1160, 892, 896, 897, 1161, 1162, 1163, 1164, 15, 898, 1165, 1166, 885, 886, 895, 1167, 1168, 1169, 900, 904, 906, 1170, 334, 227, 228, 899, 1171, 1172, 901, 902, 903, 905, 1173, 907, 335, 1174, 1175, 909, 1176, 910, 911, 1177, 1178, 420, 131, 1179, 1180, 23, 421, 1181, 1182, 1183, 1184, 422, 423, 908, 424, 1185, 1186, 913, 1187, 1188, 1189, 1190, 425, 429, 914, 426, 1191, 1192, 1193, 229, 134, 1194, 1195, 1196, 1197, 336, 337, 339, 1198, 94, 430, 431, 340, 917, 915, 916, 918, 919, 920, 921, 1199, 230, 236, 433, 434, 435, 239, 1200, 1201, 1202, 147, 922, 923, 1203, 1204, 924, 1205, 925, 1206, 1207, 926, 16, 928, 927, 929, 930, 1208, 931, 932, 1209, 933, 436, 1210, 1211, 343, 936, 934, 437, 939, 940, 941, 943, 438, 1212, 1213, 439, 440, 937, 441, 1214, 1215, 148, 1216, 944, 457, 945, 458, 1217, 1218, 149, 1219, 460, 947, 948, 949, 463, 1220, 345, 346, 1221, 349, 950, 354, 953, 464, 46, 1222, 150, 151, 1223, 1224, 465, 955, 1225, 1, 1, 957, 958, 956, 959, 960, 961, 962, 1226, 1227, 1228, 963, 964, 1229, 965, 966, 357, 1230, 967, 1231, 466, 1232, 1233, 152, 1234, 1235, 24, 1236, 153, 1237, 1238, 25, 135, 351, 361, 365, 469, 470, 969, 367, 1239, 154, 156, 157, 1240, 1241, 1242, 1243, 240, 158, 1244, 970, 1245, 971, 972, 973, 1246, 974, 975, 976, 978, 980, 981, 977, 241, 1247, 1248, 28, 471, 1249, 1250, 29, 472, 1251, 368, 1252, 371, 979, 1253, 1254, 1255, 242, 247, 982, 17, 253, 260, 983, 292, 1256, 984, 1257, 985, 988, 986, 473, 1258, 1259, 474, 475, 1260, 1261, 476, 477, 288, 289, 290, 478, 479, 293, 987, 989, 990, 294, 295, 1262, 480, 1263, 1264, 481, 1265, 377, 482, 483, 484, 1266, 1267, 991, 993, 994, 1268, 1269, 1270, 1271, 1272 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 0, 20, 21, 22, 23, 24, 5, 6, 2, 25, 0, 26, 27, 28, 29, 7, 18, 16, 30, 31, 0, 32, 21, 0, 33, 23, 34, 0, 3, 12, 16, 35, 36, 37, 38, 39, 36, 40, 41, 0, 42, 43, 28, 44, 45, 40, 37, 1, 46, 47, 6, 48, 41, 49, 50, 45, 46, 35, 51, 52, 53, 54, 5, 55, 56, 1, 57, 58, 59, 2, 60, 3, 61, 62, 63, 12, 42, 64, 52, 63, 65, 66, 67, 68, 69, 70, 71, 64, 72, 65, 66, 73, 74, 67, 68, 75, 5, 76, 0, 77, 7, 78, 79, 80, 81, 73, 5, 82, 0, 83, 84, 6, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 16, 81, 87, 97, 98, 99, 9, 82, 100, 101, 91, 102, 97, 6, 21, 2, 103, 0, 104, 28, 105, 98, 3, 106, 16, 18, 101, 107, 103, 108, 109, 0, 15, 110, 111, 112, 113, 114, 105, 115, 12, 116, 7, 113, 11, 117, 118, 119, 120, 121, 122, 123, 124, 0, 125, 1, 126, 36, 127, 128, 123, 129, 0, 130, 131, 0, 132, 133, 119, 134, 135, 136, 120, 2, 137, 124, 138, 139, 140, 141, 6, 142, 3, 143, 125, 0, 45, 126, 144, 145, 130, 4, 9, 146, 21, 0, 147 };

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
            final int compressedBytes = 2968;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb2PHLcVf0vNCZOLA9MHK75yznCQDaAirpxG8OjiJE4aHQ" +
                "QLiRGkSZMUKlPKAO9iBKdOhQqVCxUC3PlPEAwVhis1QQz9NZmv" +
                "nZ2ZJfl75OPs3tlj66S7dyQfH9/H7z2Ss9+9/cGDnz9e5u8enN" +
                "/++sbzBy8/enRUfvnH/75j7n97pzyi7+5V9K+Wn1b0hxP69w29" +
                "av/eV8u/PK3pv33+4NVHj+4nbS/tf9/jC9sTLWjy6OE3c/P38m" +
                "cffHJwXukH0e2v717cenlzeZ9M/ruKiZJ2IV8GvRrfrb9vO/lL" +
                "oj9v7v3jb+89Xv716S/+/fD5H178+dXHj/5++p/P/1fR33zxyf" +
                "7to16/7Hx5/LRev8XFrVc3l0ej9bvq9nvd+ZuZTjllVH/tnuob" +
                "rbp/l7V/2LN/VENnVQ6+dp7tyst/3/Fl3/o1fcz4W7F8rrt8hf" +
                "yP7Tdbx/dyGN+98WuD35z0WfU7wfqh+c3pH6oVOOgBlar+05/p" +
                "6qupl+CA4584+ORxw9/tOebXsLwGiTerP8dnv26ttFQHGc2u/x" +
                "2+aPHhosGHI3zx5t4vO3x0/nDV46OmfY+P1vnHqp2fulL6mx6/" +
                "T55F5sP3iI7wOaJ/09JbfHi3wYc9fZGADvMHIF9p/yHtV2370x" +
                "D+0CP1b1L8vm7vss9dje9qP6SvWroa0hG+ldLH/L9o+D/t+P+C" +
                "g69Be2n/UzoN6GSfH6WcnxX0lfz6wzfD+DCxz0UzfkmcRzt+jv" +
                "ILK/9j+fkfgM/2Tbetrzs/TDI+xfPnxEc5B99a8QGIvwjfdWqi" +
                "qOgtYKRqqP3c9KHQO3WX8kch44vx66C9bf0OD+nWUV6pz8F5NX" +
                "5R0of/0uWXd58szP1nd9YsjfFtMcS3Ff0tH/5FdAa+FvV/+Cfn" +
                "/C7vdPjNh5+l+HtIX7X0cXwF+T2m8/y3s70iWfs2fmS+X8wcIU" +
                "R5/b8B8cFH125BGm788SzEYij3blo6V2O2DC+UNvIrNqyMJJnV" +
                "dENZ7R5bRnNDx/Xfy6ZNXv+vRhMxzlAdpV+qmpbW9YxVa1/Pav" +
                "vX1W+9bOyLEb/Zjwpq3wvaP3/QHs9PaF9s+80d+tHOr9iaX8Gk" +
                "g/6F9gHlN1J7FeFfAgVtb5+59dNmf2v/kYXP/9imPzb7XvePxo" +
                "f6VyKb0qNvzjrn1OMbmfxj/KdPBdH4Jkp/s1j+0fpWdBXuHwXx" +
                "yQS2Z/efu+h+/0LAvpLx5/pFYB/b9qNHKXzuGt/Qdh5rgQEQf5" +
                "Uc/BQRP0PjlzO+yOwrFJ8ahy/QZMdPYP6M+m8mqf9K66cn+ntV" +
                "WUf2vr6pFr8hOjFmQYUucloVVJg4fFHyPeqO9h+OQf5z7Mp/ds" +
                "Wfq/7ZyJ+l0w78IsSHc9cvQ9tP65sR7SmsfcnxL/FZBlwfPWZ7" +
                "4n+C+Q9cf1ifBfEFn0+Rtcf8MeODDsn+NJsur78Grt9WUPPHL+" +
                "n6GJA/4fgXXL+kwPoo+eq3lvrudArZgK4s7fdKT1Df9so/Yv0C" +
                "639R+s3Ht+L8JmX89O0PD+0nZ48vqC839XNUX7fQaVJ/R/Vvb3" +
                "1eWp9j1P9npYfz99mux39Lsj+C7B/tX1joNKSL92eE+x9D+9NW" +
                "/Eyovg3xi2//sq5f1BZ+UvW4Vb+4wfJvKty/Dlx8ftb5cKWGuU" +
                "TO9v+gfzh+rPc3vP5hfi+tD6Dx4woVObt/Ll0J24vntwiL+Mz1" +
                "XZIytFj96uKfVZPPzZFZPF3pVf77ZZGZG/PPj1F/9dff2fVBNV" +
                "hBrdj7U1z91pH1b4L4xHv+NHn9bvL09a1qcFt960R/29Lzjk4t" +
                "naZ0stOJPm29Zy39vJ6oWePLYpNoqd6iN/GDGV9Q//X8M9/8Af" +
                "+APyQfxD+uP/r526KbsPWp6I183q/mGMM/Qz/69RnTDVN/Su/6" +
                "suUD9dc//3nWn6l/nvG35UPb9pVx7OsQ8Kei7Bv5l8rPy+Lvsm" +
                "1Y4UOTmSpuGKXzuvtFNfxPGQ5zur9iAuk9/7HxUzh/ej1uP8Gn" +
                "GD+mwqf2gWB8v0T+/QzEt7Ow+BdKh/trqfBnLH4U6g/aP4b9c8" +
                "eX0veDT3F9CuC7fM2N2pzfoGn91cJvmbHwm5S/ep51fls0kx/y" +
                "UwTSVRy9P5/gyu/R+GX328pLd+rXtv7rMPkg+b4et+/9M3f+0v" +
                "rwzPoD8Xnp2Iv1+d+rY/+M8x3+8zXLpn61OqEq/6afFBX9ianw" +
                "Sbn0rW+A/4f14dK6abNf+pb98dsn1w9p/JX1L68fzsFfQv8Bz6" +
                "/GjB+4fjqh/9qxfiSvn3rTocJaaM0b+zsc+7fMtKnezvKL2P65" +
                "6+PIn3a2/vbxcX0U5CeXdFFY9Xu1Fu47zV9ZXTkoyloFSrqrF0" +
                "ZfHvP4b/DN+PzESH6ATo7+zW7sA9QP8tHx0cJyqZObXypefsl+" +
                "uPGf1ZknPtvqH5rfHvqffn/NZ38ZOF+Usf0Hq76jt+vjrvOfeP" +
                "wU+D6L9o+YPmv8Q/cfXWWjcHwx4k+lwjcB8VPNg19mzp9mj68h" +
                "+IOCzwcx3u81c/1k5vxz3/l96PpJ8b1xxR9H/9i/XUj237B9TM" +
                "6Psv3riju+6HzN+iBrsYmvuv3xys+fKjaNDeV1oyeb83dlhP9S" +
                "Qnoa/7OV7aTKb0Pja4e/YXupfSOx+PtH90Pw+U+pfEP2zy3n+3" +
                "H9Er0fBthf4PoZiXxs89N+/MH2D2T3D7AXP//4/TWW+63d/kjG" +
                "aI/ezyCNf/L38/jnJ/bvSfHBizj/Fq4/Zfr6oqM9fn8kqD90K1" +
                "c4Uj18P8Hfv/h+LDnwk2HxJ71/wmgvej8QWl/G/Y0P2/rWljAv" +
                "W60t1Qi/2etXPvqs9Sm8fyzV/3njt/T+Bf9+SR7Of4LzlenvPw" +
                "Te/0h9viUQf6JDtWj98fqeyvizMqotemCnn+hnTf2voLb+V/T1" +
                "v2JY/6PY+4/8+q+/ffz7ZUguP1Z+ngP7svfPPx+tAvoPoIP9aU" +
                "b887aHdDS+tH4xd30kevwVC//G4bt07ePrD0x6gHxzC0E8vx8M" +
                "PY+Lf2z/9kOXXyR9R/HvyvIv3X/b//rK8MM1p+87vpzoyw5/Zs" +
                "39iaK5f1ELv8aflzH5het+WVT9EPW/df9j+36LqH7uuj+Tu/vX" +
                "Y/mi+0f+/MB/fybn2LcR4q9Q/6GD/M9EORaO/MlLHzwuuqs9/3" +
                "7UVP7j+WG6636VzTi8/t8hX/v9qutDz4X0wx8hnW//6H6eR39D" +
                "8enE+Bj3c1nzi5u/9P6l1P7TxadDkL/a848E+TXT/6P8h0mPxs" +
                "eo/hNZP2LiV/x+7ZnqW2z8LJWP//Hcv0pd/wtcf9n+Sdz+bkL+" +
                "YPvX0vzR71+k9ef+367zt4juL2sx66P+EEUc/O3Br5k7vcH6cy" +
                "bcP6GL3Bcfp/1b8kcHf+v8Ue0zvxXnL71A1Sz1O3H+zj5fpQLk" +
                "b3u/XuT+xL7oJU8+fHyDPv8hko79B+f96OL6pft+Msv5hcQfx/" +
                "1l+/hYv1/77z/PHL+uxOf3Zhz7iNu/ke8/lTL/Jb2fkmz/ZSa6" +
                "FL9L8xtMnzt/k+Uf0vWdX36J9MPhf5LtD7vmX/YeJs4/+NtD+s" +
                "DDxdk3Gn+/+/stvdj8c/L5XLz+Pe1nuT8Ugj9Lkfysny80OF+Y" +
                "5P0EWSg+Tr3+c8Zfgvnh9v15Xd+fd+ygWPMDN73vf+LfB/fzHe" +
                "Pz6Lzxt/1rRmnmJ6Zj/CKaPzuBEp0PnO/8WS7z/wnsE8UHL13l" +
                "p+39Sa2U7fOT8Pt94PgR9SW0vkn1w88/8k/ErU+kpFPA+x8C6V" +
                "difj8ierj+BZ4/gPX3sPGn5tR9BGtRbtpnDeIr1mOD96PsO/+X" +
                "1v+59Gtin5Mf4/tFMfINyI/Y+ydz7d8I2zPya6v9qLX9nDnwE7" +
                "u+YMLtW7tl6ZQPBeo/l679bgG9XwedHwD7Q8z310TXX8X4lP1+" +
                "opD3/wT6R1Z9V13N+u3M9+vFn+8Stf9LlOrzh+fe3+Sen3Tq3/" +
                "8BCkSgFA==");
            
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
            final int compressedBytes = 2845;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTuPXbcR5qHOLqhNDNMLy1F5FKi4hQqnshsBx6/ASWNBsI" +
                "Ck0w9ImdIBuAsVcpfChcoLFQbc+SfcBC4EV2oM+Of4PO7rnEvy" +
                "m+GQ964MnUbaneWQHJLz4sxQqc2n1e6zyvNp3y9/fvf+kz/9sP" +
                "jHd2dX//rxs5dPfvrom8fts7/98p57/Ophe6l+/ur+kw9+WHz5" +
                "fg+/9fLJ6qNvLtfwX3u4Mir6QfxCuOez/pEg+vjh7P4db/5o/N" +
                "P23wP6HMLR+nTwWm3/qt703/Lp599fO7Rp9BfD8fydqrVq1n9o" +
                "nLrb/7vofjM0DsF1M0M+9l/5xlf74M47nOpw/Ov+L4Yfbw/922" +
                "58S2L/7br/0Gc3/+j9/WumiOtGnc/aLKnrs26vQ+3j+NH6YPzD" +
                "J+//Yq//Cf0BfewU7ka4YfavDubPOH+i84XGj/ZXq1if47YHcN" +
                "N6NrzV9P4xvAr8aZUJfxSO+Rf4qjoqP2H7ViT/SfvTKz8clf9H" +
                "6bN65/7nZ1cL875SD3785PrO6nzxWDnzhe0nNoyvLTt/wvjqq8" +
                "XdYXzVML5Lzviy6Fd1hL1M9Icdw6CzJ874R/3m0/X4/kNZHzQ+" +
                "QH/YPzwfdkoAFzpWafz5/+P+8O7fyne+HIEHV751AeIF4ifCFR" +
                "uus+J3OfjX8fTf1f76e/gDtA8Szh+Hf2P6ieXHe8Nhqg+YyXMg" +
                "nh1N0LbARENwkiDXA7sZN4udbZ1q3tNa+rnJb22Q/yV+I3po/6" +
                "P1g+1TFCErhFPIkoifwl8zjJ/c3qhrv3zb2C/M/esO9MM0+/nA" +
                "fg2wO7BQ3Q4625OwWtmve0vW9fM6S97/iP9y9ndp/awFUk3/0W" +
                "54SG/C3X304bg5Wn1WY7jHfgocJD99Lv6u7lya7o/Orro/aVr1" +
                "l3/b9tkn/63c4+cPx/aarx/awnDG+iL6nHp9CtsnBP1baJ+2vL" +
                "PK1q+tR/+zbP28Odi/TTb/ZjsoMc1hk1sc/0oIv0P2c1R+aNOx" +
                "XNvvT73Zn91AbYd0Ne5fAC9/vgv7l/Pof9CXFMVP8o/Q8FfJ9C" +
                "mNXwfwH2v/pNoHRLXNKubn3pDzkQLP6b92N3B+b+FHgzf9DqiW" +
                "t6+fdv/5p+tE0bOlXZq/LlTtssjPVP/12/W5GfJZZh9k8F++hZ" +
                "8SDuyPe/ZX3Z3f+s+d8lp93P3sXKeK28aoZaMat3e+kaDXAfyv" +
                "pvjViF+t8Z8ajuw32F59OdCnd830KKqBLoN+2mzP1wSuJnCMP1" +
                "U/ptmndPrpUuszjD+EX6lXgL5x+pce/+H68vrPcj4F8kOK/559" +
                "MbRv1Lnu2zcH7TPo57VE/5Xqx6duj/W/0W5sXO16v4NWpid/z2" +
                "j+sO8/M8Ct/obO36xk80P0WYwNK7UY6eu0ndAX7W+jovgT4id5" +
                "+h30P76W+QcNUk2Y/s1D/eWadr+SGB+J/KPwfoe5/r4vEn+C/I" +
                "sE/zb4HvHgjjJ+zvlG/fP8c4oZHx2WX80gv+T0/Z3bh5T7k7qk" +
                "/3IF5r8qTJ+Ny3YdVNTMHdRE/LaQf9aEjogDbIlm3/V8SHnjk5" +
                "tM8Jmis/8dxb+1CsQvN0z8ofGD+WP5HYhfbgT8gSO/bzb/ksbn" +
                "yuN73YnhwvE1A4WXlXra/XC731TOdeKxXVDPX4bx/67tv1PDpf" +
                "YD1A8AvA16GGn7KoW/lOZfKhv/Ks3/ysRn7fpv+/jd/zWmW+AH" +
                "TeXurM5N3+iL4S8I7YvDcf7cRrOdbrt6zqF0UL8yA3zbfuy8dq" +
                "OrcAu/2IPbHZwmf2sQHxGE9/FfgfXdj1+Nza9w/NOJ9wcz/qEi" +
                "8RcmPCL/cPwRhX955tPWNP8L2T8Tos/W/tH7+usEXnP9xwyx4T" +
                "vNEG65cGb/DPw4f3RsbwB+Qxu/WfOvhj5+PcHvZPRL6D/v+pHt" +
                "50z8pdmcm6B/P77+wH9tJkey2R+OIx0kgvw8pf3Ijs90VP/Wci" +
                "ffsvi/g/5nofy0svUx44iaHX3s+OvlMc9fSf6rAf+Lw6n3t3b0" +
                "L/Pvb33x34z7CYPmL8XfTgnhkvxLtdD+ZO4P1v6nwOuwfGgBwT" +
                "P4r2qK/ijVb7VkfWT9R+a3vb+xY/zCeH+jtvc3UD+U+zcS8kfS" +
                "48+VouSn2bn+h/lLqD4KtO/ahPGVtg8VfX7S+h6ofg3gnyT5EK" +
                "uvI83vx/qBLP8R1ReC+gWoL0CpHxOv7xBvj+rjJPXP8J/k0N/i" +
                "9nm8vpA4/xXVh8g1vwtv/2n5/Woifz/4dsiPfDDLj9z6Rzu48e" +
                "RP0vyn3vzG/RIebaH1b/yNHdN/Ujp/snD8AKH+H1H/5ezPmXyO" +
                "+PdQ/R6pfkhqH5simD+hvoywvg6Tv4ThjPo1Gf27ML+5cP/C8W" +
                "H+Gqd/WvzjHFGz+289E7zY/1SrXX0SPa9Pwq6v4GbyHfkPbz48" +
                "E33S7C9CfRVUHwftn+uk+h1Qf/O3h/cfPvlFuv9Agrr0/XdIf5" +
                "DJ53pKsnYP6igmMuIvsP6EsL5Faf/2dHyN5299cMb6GyWaf+n6" +
                "SqXX7yjxY4L46Zs+vtJwFL8N5y/cn6X9VwT75BrY59cs+++AkY" +
                "vtJzL9jAdAqq9WS+zjlqw/mrT1IdrfaecT9U+O37JAPheCw/xD" +
                "6D/d5mfq/fzKkP+0lP9ET/S7lPld8Pync/9xGH/0S6e/z7+cWP" +
                "84m/4T019C+YEg/w3q96+942Plz0U2Ypb8o3pufzDq80P7BcHz" +
                "1B8rF/8Rz28T07+TvybKP4T5b1z8h/5bXVI/yadfBs934v3Emn" +
                "8l1W89dX3U0nBFrk+awX8D8udk+unp66eVtd9g/heWX9H2aHz4" +
                "fYHC9oUwP038PpR0fVH+y9Z+C9wfpd6/T++v6lT6Z7M/WN8sv+" +
                "Rqkl/SLYTd5pf44bv8E+n8iP7V5PwMnB8Sby/O75D6p6T7h3z+" +
                "kX3g18+1+XTUD63WAwt8cZg/EfWaIv3yeVz/SMvf4OjfqH2r4f" +
                "1DXP9C9de9+On3q9L+FfH+xAD9xsQaC+KTpfIvxT5WGfUP5v2Q" +
                "h7+J7g+F94uIPvnq76fZ/8h+Lu7/l9bPke4fM5qoTbs73/1hao" +
                "b4wZYQf473Rxx/HI7jNx9N5SubPtT4FGoCXJH7pfT4elr8fnB+" +
                "4verZvFH9V78UZvnfqhAfDjHvi4xPpV3/9QF7+co8jFL/HdO+j" +
                "HhjPr4uf0T4vc1hPHj6PyT4n8F8fVz+lVc/2Swf+r+lOl/mw/H" +
                "f0fiV2nyK+R/znL/Fjkgsvd5Ke/viuL7hfH3yD7H+ht6v1fIP9" +
                "P0G4Z/X2ZAHIM+IvvrCPYbIT8m9X6FoH/j99mj+Y3gfd+k+RNS" +
                "NLfjR/wV6g9R/RPjF/JXfP5E88Pvt0rbE/UbyyNjtvshQD/8/m" +
                "XK/QlaP3vYnvU+T4r/JuCfFPt/xP5LpL/H/ZdC/gf9S95lf3Py" +
                "B1D8Nn7/j8N/GmwrzYZ9cRF8v/HFQ0p79P4jMrtgfLbMPyyNj0" +
                "6rv01vX9p/eez45orMCV0R/BWX/yXJl2PWP+Scnwh+m9Z/mv/p" +
                "JtTfXpLsc8b7M0kOKnp8ftA+i8fHZpsf730U+vstsv5J9K8F7Y" +
                "X7JxT/mk+/g/WxJvLFU39D9n7EvP9D/Q9MFJ1vwvsKUvtVah+r" +
                "uH8+Gl8i9h+08vx/cf2jSH1McfxiJvnGNnZp9ps0vydDfpCUP3" +
                "n9d/T6+rL6/2Xi32Ybhf0+nzR+Y4d/Nfpn7n7X+2eq6zs/nS8u" +
                "QfynTTkWGG7ZZJu0i9WHwl8sfk9WPzfJ/8/w3+SGO159O6n9KO" +
                "8/ZXz56lNmj591pdbfP3+6/4p4PxjUphL1w98Agj0Vrw==");
            
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
            final int rows = 608;
            final int cols = 8;
            final int compressedBytes = 1518;
            final int uncompressedBytes = 19457;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW71uHDcQ5jJ7B0ZAEEawEpWrQMUVblI5jYF1YgNJmhhCBO" +
                "QhUqZ0AEpwoTKFC5cHFwHc+REOhgvDVZoAfhxzd+9nubfkN+Qs" +
                "dUIIWJZuwOFwOL8feUJoMTbU9jdEb0fp/rk3JyP96Zouuz/NgK" +
                "4EGLU7f4//5neXrnb8jSilqNafKCNOm/8X9pNucTifKJ9n/+T1" +
                "1wKY4bL1Hnd3//WITDv66ovzx7OrhbonxP03j65PVvPFhTDqiW" +
                "4m1se9OXJ0d3Jvq7sFC/vvw5fnl9+8Xvz+Ynb1x5sfX12+e/Ds" +
                "on7+839fmYv3Dy1/Ln18/7sB56s6pDzC+dT4/EtBGDLi/GLk21" +
                "if3J7Jnn8G6K7+/mn198Naf3+1+g/b59h8V/8b+Y968muv/CP+" +
                "Qx3jBrzhpWP8K0L/kecjwPmMxLeGUFZi7sRPJZak+I/9A+6PqX" +
                "/kH+B8cPxE+gnSSfGHlAICo/Sr50y/lJZeVnouiu+FqIwp7NYq" +
                "JSr702D1UuJTML7w5jf5pbxanL5o8ktxffJuvjh28otqd696qt" +
                "ByPJ34/C88H9onyK/M+W+7/Y/m18LuH9E7RtWOZzkwHCQf9s+y" +
                "lb3a7KHUUe7r1YmZZP1yk5J6lrDmbpxPtYgchrZ/leLcmq6f3X" +
                "yZUr9NuD5tvtmrj67H4+9yXHnD+dz9KWhcxZCZHhgAsA+gP1S/" +
                "hdkeHYmTY2U3MbuyLKpafPenrp8/+rswFy8fUsTi+j+gH/3ile" +
                "+GIh/On1T9JdfH19T6RY0o5sOv55dfv178dK+R/7NXl6sHz47X" +
                "8n/c9Q/++AznA5M50zdd/rc/2vwvuvwv2vx/QzzfdPm453emP7" +
                "byfyu6+uWsrV+0rV+WXf0yRf0cXP+9u77o1hfr9QnyBccE/Fn7" +
                "E+JfJj4xmG8G8yn1bW8U3vpWDPpLQ5yvE/17GdmfpMYfQv9GK6" +
                "Aki5xefwX5k+OXNxFw6xdqfeL1D09/VxH1s3Lnb/lXJP75859A" +
                "9REvviqk/CLKYYsU+w70x1JJoXXD1tKbIzh9agXXpjm3Wen0B+" +
                "H62ktH8pPml65b7OjWAma9DtJu5reGk2n4zpL7jxg6jK955zPx" +
                "/W1+1XM5ml89+H3odoJyf0DG94H98vFlX3wwpPiTHd+F+DO0H2" +
                "J9FAggYfyUhy8m05dhuuzmc+8/yPvn+GdG/BbaZ2b9E/YXxI9X" +
                "fXyvaPE9F/9k47PM+AHwU3w/yMN/+fgwuh9F9PiSalL8air8Lg" +
                "r/1NPhn8z6hFD/sO6fIP/bqf9uoX/z9F/8+4Fp2ld//RMcsf1T" +
                "EY3/8/wzpj9WSfrjyYfwJ4Rf4fo4TOeun46f9e4fgvID/AzhY2" +
                "B9wv26g199vn3/EItfIfwsVv5Y/DX1fol3vwPxa+ifAB9i4j8k" +
                "+0TJv+TE3zrYvyL8hIyvpNc/koTP+O2DeP84vn9UHxD8l1g4ev" +
                "2PNR/iH7T6qQz6b0m0uto9sGLa+JL2fujA/TvuH+us7wMJ/TOy" +
                "v4j3UXexPz+sfin9b873t4T+OjN+C98X8ejc/oiKv+jU/dWs99" +
                "nZ8ye3fsveP/bXr4j9kYy4/0D9aS1Y7ydi708Nxf7o7xu4+S+d" +
                "P63+YucHGL+Z/RGFP6M/xvaB/Bf0J2z9hf2nbvNLpewW71eFsf" +
                "lLNUI/aTmM5xcNwnLk+0Ce/Pj8CfbBer/Pq1/x/d3dro/y3z+i" +
                "9///9/2n3C/pzPRbrB9j68PI+EO+XyDppwLCVXu2yH4/DOiIP3" +
                "4fzLM/Uv1U0o5WhSyM/b4v8f0rkz/h/Ser/qPXr2nv78jfr6K+" +
                "r4r1Txr+5OXPzb8Hzw9M/O2uywfxydz4BMQPyPiDTLHv3PkB0j" +
                "N//yQtPkXJT7I/6diUnmw++/426X1QzP0oN39y798I/WHg/oyN" +
                "Xx/6fiL3+vz6mhYfU/HFg79vCcsPv78F7BPR6e83pnx/rKebD/" +
                "07XH/defzkNtbPat8p9j+9fN78Cum8/MDOH9nrfx7/tPg8HR3i" +
                "C7B+zYtfcc+Huz+4/0/eh4yg");
            
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
            final int compressedBytes = 4959;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVW2t0XdVxvs2jSbEhxDGEkCaQVfqITVCzKA0BgrfvudcWrn" +
                "GTPpLQuDY4tolx/SJ+xWB8ztnn3itdSUapbSwX29QPwUocEigp" +
                "+dEXSRZxSxdNzFuS6arBlkwWCxHoI/xIVmf27Dkz+5wrVqu97m" +
                "hmvm++mb19dHR0Jdcn65O9q+pgkw/ZD/UcRw+XHUja9cnWWHZ1" +
                "fbJScfgcyE4wnlyPtvd9FKWXVirISjfaGZVK4nKVir0ofcqzj1" +
                "UqjSYr1Sez76GN5/bNqOQfrCwex5XK3Ml4SGOag3h8MeX4BZPM" +
                "K+vZWdzHnkav+V37Gy4aF6aexGEX2Avxs+k23fY6Z+eb7t7j6L" +
                "noc+nXTHc2Zv+IM8k8yE7k+J+j7RvI4z+xV5vuRq/9fXutXeYy" +
                "19s52YuE9m6xC9NnmAu6J+lz3077x/bz9s/sl+ziXOlTwnNxZO" +
                "fNfTXpcX7N5zS+wH7Wff5TeH0RXp/GKH06xz/DXvPbdq41Lnfa" +
                "Vm29tcN+3EXjSu1Guyjo/gV7k/MOm8P2G84+WKm0b0fPRcNpnz" +
                "nc+Kj9Vp45Cq8JjmhVKjn6CNr6mD3CWfsd+83Wd5lnH6idJ3Vw" +
                "TrmCfQj/De2jZU2JW99OejUWcuwxztm/gdf9GEk34bY2YSdX4a" +
                "6n3htsl4vGhcmMXPthS3tYbVYD4i15FKVPmtU9S3QmxMWSh371" +
                "Vvoaoox9o/cZQRst5prVjVHJ80eoLAuxniuTPo1pjvTTM1ZvLe" +
                "vp2ejDXjmVGi/7mn3dealJAXHWcVJacE6vmLQ5oTPMlGVv16hJ" +
                "syFhuXN6llG4G8ySuuZ9UpefU6AkC+OezyQDGgs53E/PmO0t60" +
                "mf/JxuLqrqSdwO8ZzQ6zf92TnOfjj7CHD6admB9Kemv+db2QzO" +
                "ICa45GhlM9FWV2bv5Ww2PTu3fYh5cE6R1LXeIwqVSnZRdrEode" +
                "rR83Iy7DTf77v9umZk5zEvuxBev0aT5Og03YvUoef52QfgnJ7X" +
                "HWmHYf/sguyDzltn1sH9Ce2DZh1w1tGC+9Or8HkY7k+cOapx8h" +
                "T/EfSry+0R1wkzcH8yw4Qmx+wDcz/FXNRlFXV/CpRluTvJVVTB" +
                "mOZA7THOwf1pHdyf1uEkHfSGebb8Gh4uq4X93f0JvVVmlU2dbc" +
                "KrHz0XDdgPm1U9f2vXcyZ5DrL7OQI1sI0TEmPGHnB73s6Z9n8z" +
                "anc1NjPXrGqdn+cbtm177RDpibIsm1Qq1WeTMxqzg4JTZ8jtJN" +
                "ze7SbblNdnwrR7PfdO8C28/t1FfWW1YlybVYOnCrL4gR5F1Y/W" +
                "ZrU/ojPMlBWitVnNWFjunP6HUfi6Wyh1rSukjj/KmhL3rknOai" +
                "zkcD89I05f5KaHuA/3zN5ZVNWTMBM/m4ZpwIk5686uQQvO+lLT" +
                "aM/XGWbKClHTaB4RltZzz4pXS13vTqnLvwZKmhL3nkp/RWMhh/" +
                "vpGe0lZT06J901u7SoqidhpvOWmqXgeUseRdWPmaXtG3QmxMWS" +
                "h37zv/yefYZxd04/Y65Z2vuXks/PKVCWhVj1mfQdGtMc6adnxO" +
                "mLXH9Oqm92+VRqpXjI4L3BWZcbogWzXWqG2rfqDDNlhagZ6qkK" +
                "S+u5r7sbpa73IanLz6mkKXH16fQijYUc7qdnxOmLXH9OqmvWVV" +
                "TVkzDTeWvMGvC8JY+iat2saT+iMyEuljz0e9b7PfsM4+56eoO5" +
                "Zk3v30s+P6dAWZa7zz2efkxjmiP99Iw4fZFbnA3O6bqp1Ipx/I" +
                "P4h/aT8JPk1+lnwOSS+ETSHz8H96fL4xfaPwFkNPlm/LLDPgFP" +
                "n+DFb7ifPf/T2beSSvIuyJ9Krkje2zsIrC54nZvA00n6kj6Hua" +
                "87/n/EL8Wn6i/Hp+OzEO3RjOSdybszA5+nwet9yYXJB+PH4u+D" +
                "f3n7ifQy4sRPxU9773l4vZiejnfjz8HxT2Hi3wXv5/4JcnZyjt" +
                "O8wHGPx/8a/1g6NRf5c5qTfINz8c/Ya/yqz+xN3uE0zkvON/vM" +
                "PjgxZ93Z7aMFna4w+9pP6QwzZcHzuEJJo5Oem22J1LXPkbr8eg" +
                "qUZGHc9670eo2FHO6nZ7SfKOtJn/x6+nxRVU8iO4JVN3V4fkLb" +
                "hFc/ei4aqL9o6u0F8PzkM/D8VIfnJx9BNVh4fspjzOTPTz5DLP" +
                "TtLtTj1Z6W5+X5KVCWhc9PfZek8zUGz0/1sDPkdhIOz08QSTd4" +
                "fsqZ7vkJc/75KbvJRX1ltVJ8yMDdjazLHaIF96cPmEPtcZ1hpi" +
                "y4nhRKGp303PU0KHWtf5O6/HoKlGRhXH0q7dZYyOF+ekacvsiV" +
                "Pvn1ZIuqehLZkTnk3ouCn/7ix20Tzrgf48ZafJ+u+n73Xtv6+m" +
                "SyvT6Z3ZY8B9n98avxa/zeV/YXjRP8Dld6CnP2QLIVryefg/tT" +
                "tsrfL3Y1j2Ur+D2w9gx6nw4q/PWU3BH/0r2j9x7kZGvVO2UJ1q" +
                "dLstXxk8k2/w7gYPws1J+sT6anoWYLvk9nd2ab3Qx3Q9ev4PTZ" +
                "hgT62CxbyVPavcnm7KvZJrqeko3ZYDyerbF9sP/XEc/8+x3Zum" +
                "xj/ItklbxvZ6oGv5d7Sx5F9iq0OhPiYskj3P+bVdkXtPkMc021" +
                "vVTyuibU1Jx0ucY0R/rpGe3vlfWKs8F5PDyVWinebOBfgazLba" +
                "YF57SUcpJhpiz4ulMo88t66EVHpK59m9Tl5xQoySLFdIXGQg73" +
                "0zPaJWU96ZOf0z8WVfUksiNYewx8bybrcntowdW2kXKSYaasEG" +
                "V+Wc9dTz+XuvajUpefU0lT9003aSzkcD89I05fnpH75Of0o6Kq" +
                "nkR2BKtlWuA563ItWnA93UI5yTBTFlxPCmV+WQ+91iKpa39P6v" +
                "JzCpRkkWK6WWMhh/vpGbMdZT3pk59TXFTVk8iOTEt+I1H8rUfa" +
                "z3exxyry24jwtxj2dvmdB/+2glW0nvup+2mpb79I93F9f3q737" +
                "fANAP/v9+3YLcitzgbPFG+u6ha/H1Lfh+faWbCiTnrzm4mLUBf" +
                "opxkmKlrJCZcVFyHlwRt/SFzzcy+KyWva0JNzUn3a0xzpJ+ekT" +
                "sXZ9R7hXP6UlFVT6LVzQazATxvyaPIrkCrMyEuljzC/Swb2Be0" +
                "dRNzzYa+w5LXNaGm5qQHNKY50k/P2Li2rFecDc7puqnUSvFisx" +
                "g8b8mjKD2IVmdCXCx5hPtZFrMvaO0Ec83ivvslr2tCTc1J79OY" +
                "5kg/PaN0C2fUs8H1fu1UaqX4hbdb6dEwhpr/c4x+ej/7ITM9wo" +
                "z8lKacgVjp8Nv1jS+W3NQz4fsqWhMyD5SnDuvzeADWMFn3vvqA" +
                "X8POH5YMoAMKH1CVjJJGBz3nDXeqc/lhX6eVQq6vzrFgDu4dzD" +
                "jcuZeaU+aTKQfcuQTaPl5r1oLnLXkU2a+g1RmNpw9JJXmE+6tj" +
                "LfuCNr7GXLO27x8kr2tCTc1JH9aY5kg/PWNja1mvOBt83V03lV" +
                "opPgprlKxd4z3KgQ+cPAP+UYUfVZWMQqxYo64Ho6O6tu/7QZ9R" +
                "X6eVwh6j6d8FWDAH9w5mHO0446iaczTf3ajoyMyc8/E2sw08Z1" +
                "1uGy24nr5qtqWP6wwzZcFzpkJJo5Oeu4/3Sh3cn7YJUuwc9iDF" +
                "9EcaCzncT88o3YTr3/dVXdPjRVU9iezIuJ+/o66sJ/462P6oi5" +
                "+3oi704acysK0v8vNW1BW/Kk9jhOqnsaiLWOzLB+rp5056zoz3" +
                "YF5jWW/4PEtqUVf6hH5KpD70ovczuR9/puk55q48E36mV+sL5N" +
                "H7BeFHvFc9Z+41+N6Vs+7s9tKC6nlsOcNMWSFKGuxH80TPzTZP" +
                "6nQfwnQm7IFxNC89oTGtJb31jMXJeE96rzyTTMk70No+jkwEnr" +
                "fk4YpmRDNMlB6VTIhLDceE+9P2GXguyFHU4wVfd3le14SampO+" +
                "oDHNkX56RukmXP91p/qmI1OpleL9Zj94zrrcflqwrwVo7XrJMF" +
                "MWcgQlDfajBaLnzmmB1Ok+hOlM2APjaEH6psa0lvTWMxYn4z3p" +
                "vfJMMiXvQGv7ODZx9SzZSoU8itDXGeDHEjFLo6Sh9ShLOrqW89" +
                "QHl2TEkx7Vs+kyjYVzcG89o96J8LgP7ZV3xwzEZGbO+XiZWQae" +
                "t+RRZC9DqzMhLpY8wv1VuYx9QRspc6Uu+LorYFoZpvnNsC7E9X" +
                "wUNZKyXnE2mOnAVGqleDusMbJ2k/coBz5w8gz42xW+XVUyCrFi" +
                "jbkejI7pWs77PmO+rohJjzH7WwEWzMG9gxnHOs44puYcy3c3Jj" +
                "oyM+d8PMfMAc9b8iiyv41WZ0JcLHmE+3+zOewLWuthrtQF11MB" +
                "08owze+EdSGu56NIuoUz6tkqlebNU6mV4q2wTpKFDHmUA19nwN" +
                "+q8K2qklHSUHo+SzqqlvO+z0lfV8Skx0n75QAL5uDewYwnO854" +
                "Us0p+z0pOjIz53x8j7mn+hZZuK85jyL0dQb490jELI2ShtajLO" +
                "noWs5TH1ySEU96VN+yyzUWzsG99Yx6J8LjPrRX3h0zEJOZOefj" +
                "9WZ9ZNiSRxH68J0zzwDfvSTSfKfn/wrDbieMcowKV3QwS6uoLA" +
                "uxyNiVGgu1ZBb0ZDdFLuoQF39/R7pTqZXiQVgTZG3sPcqBD5w8" +
                "A/6gwgdVJaMQK9aE68HohK7lvO8z4euKmPSYsLcGWDAH9w5mnO" +
                "g444SacyLf3YToyMyc8/Gd5s7qm2ThOnQeRejb2yQD/DslYpZG" +
                "SUPrURZR0gvZ3AeXZMSTHtU37WqNhXNwbz2j3gl76SHuQ3vl/T" +
                "IDMZmZcz6+w9xRHScLdc6jCH2dAf4dEjFLo6Sh9ShLOrqW89QH" +
                "l2TEkx7VcTgnhYVzcG89o96J8LgP7ZV3xwzEZGbO+Xij2WhTZ9" +
                "3fF6DnooGoZjbCV3CNM8B3L71CFK2wohpFnBeu1qG/L0BuGfOM" +
                "BLWyj2tMa3HW7iTP3h0ybCY87uPuopZnsn16h2F/jhvdyd7qmf" +
                "Sa+PHqmWSoeqY+2fic+4vIM9UzlUpfCy38vPjpxo34/kL1DP4d" +
                "RnKPf5pdRCjg/v9LUQxKEVrH+QNikF75A/OExb/EuK8H1C5pfF" +
                "YzUNeuBsaTkoufhfhk/g7IxdQP3wfhGdxfeM1In9d92SdNpzNO" +
                "2dj9FVvjBvWuyi+SVerp7hZzi1jyKIoWotWZEEeLHI4JF9VooV" +
                "S76ynnik7+8+jCorLmIp49qjE7GOJ6vvJkek/M5b7FjnrHQTzf" +
                "zI8+yZY8itAHpTwD/PlhFMaEu+5ej3KE2l3C5WrSp8UZwWQSZG" +
                "X/pLFQS2aRGfVOhMl9SFMmLauV4nthjZC1u71HOfCBk2fAv1fh" +
                "96pKRiFWrBHXg9ERXct532fE1xUx6TGSPRZgwRzcO5hxpOOMI2" +
                "rOkXx3I6IjM3POx5nJoqvIwvk6jyL0dQb4mUTM0ihpaD3KIqqV" +
                "mM19cElGPOkRXZU9obFwDu6tZ9Q7ER73ob3yTMxATGaWyZ13BN" +
                "Y4WbvHe5QDHzh5BvwjCj+iKhmFWLHouYDRcV3Led9n3NcVMekx" +
                "3jg/wII5uHcw43jHGcfVnOP57sZFR2bmnI9XmBXVCbbkUYQ+fC" +
                "/IM8BfEUZhTLj7buL1KEco6YXVnMfFGcFkEmTVT2ks1JJZZEa9" +
                "E2FyH9KUSctqpXiL2QKesy63hRZck4soJxlmykKOoMwnP1ok1e" +
                "7rbpHUaVX3LDMU9tEL42hRY6XGQg731jMWJyPP/b1v3pVn0qp6" +
                "x7IjWDvMjugaslDnPIrQ1xng77D7OWKWRgE/gJ9Fj7KIaiVmcx" +
                "9ckhFPekTXJGc1prU4S/NK904zch/aK8/EDMRkZpnceXeZu8Bz" +
                "1uXuogUa3Ww5w0xZIUoa7EfdoufOqVvqdB/CdCbsgXHU3dyjMa" +
                "0lvfWMxcl4T3qvPJNMyTvQ2j5ebpZXX2FLHkXo2/skA/zlYRTG" +
                "hLv7k9ejHKGkF1ZzHhdnBJNJkGX/WmOhlswiM+qdsAc/B7/CXN" +
                "SUSctqpXiBwd/VeEseRfawod8s5ZkQF0se4f6qXMC+oHPvZ67U" +
                "Be/7FjCtDD9RNMK6ENfzUWQPlfWKs1UqzeNTqZXi3WY3eM663G" +
                "5adqB2kHKcQU8iyWmP+GU9d07Dnevycyphum/zQY2FHO6nZ8Tp" +
                "O/fSXZs3F1X1JLIjWE3TBM9Zl2vSqlRqBygnGWbKClHml/XcOe" +
                "3rXJefUwnTfZvf0VjI4X56Rpy+cy/dtfXloqqeRHYEa5fZBZ6z" +
                "LreLVjQtmkY5zqAnkeS0R/yynrtnTutcl59TCdN9mz/WWMjhfn" +
                "pG6Vbspbu2VhZV9SSyI1gHzUHwnHW5g7TgetpPOckwU1aIMr+s" +
                "566nv+pcl59TCdN9mz/RWMjhfnpGnL5zL921taGoqieRHYHe7N" +
                "psOBNn3W/ZZtOKpkfTa7PtDySDnkSS0x4plPXc9TS9c13+/81L" +
                "mO5rf6ixkMP99IzSTbj+/5urrnOnF1X1JLKj2mzTY3rgxJx1Z9" +
                "dDC75npqaneZnOMFNWiJJGJz33b7ekc11+PZUw3bf5msZCDvfT" +
                "M+L0Ra7/exXVtbW5qKonkR3Bqhl8L9Rb8iiy/2Londs8E+JiyS" +
                "Pc77nGvqAty1ypC54LCphWrlTSZlgX4no+iuw/l/WKs8H1dO5U" +
                "aiX1/wXvHcD4");
            
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
            final int compressedBytes = 4519;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVW2uQXFURHkBRBF+kIpUFDVrqD19liUCVip6dm4kFyUJ8oa" +
                "VCRVESEoJlFCFFsO6dO7vziBICVmnFpBLE+Ed/WNmEDSUgihCM" +
                "CY8NKGgQlYdIopuYsEEQtE/37dvd58wdwpy653b314/v9py5j5" +
                "ldt8QtqdVcMZNEWmOfW9LYpy0Wl5kkwunFFsa9NOcJ9pW4mnqF" +
                "mM5MbHScxTU/0qSa5ai51Wrtn1Zli/SL3EUgFTNJpOX3uIvy+7" +
                "TF4jKTRHjB5SKWBZ3zJPtKnOlTgOnMwOZ+G2dxzY80qWY5am7Q" +
                "p59XZYv0s93ZIBUzSaQ1HvWztlhcZpIIL7iczbKgo7ewr8SZPg" +
                "WYzkxsdJzFNT/SRn8R5wu5QZ+2VmWL9J7rgYQz2no04B3cTTax" +
                "sKeMfLlG2T/Oh316UuJ01rJPEabr5g9ozPpwPc1x9ImqWrpq+8" +
                "Uwq2YiRwRjhVsBEs5oW0ED3sG/kE0s7CkD+qRQ9s+vDvN5aex8" +
                "idNZyz5FmK7beFZj1odra46eff9ayHtl0aeTwqyaiRwRjK7rgo" +
                "Qz2ro04B38veu21moLe8qwKOXQcvtURmu1+rES136LxJV9inKK" +
                "Xj+2PVtj1ofraY75g3G+5g1ch2t2hsKsmokcEYzMZSDhjLaMBr" +
                "yDo2QTC3vKsCj7x/mwZ9v6x5V9ijBdt326xqwP19McPfv+tXTV" +
                "zifCrJqJHBGMhW4hSMVMkh9JPal7XSwWlxjWCS+4LGRZUJ/PRo" +
                "t/WEeP4hPyYRtncc2PNKlmOWpu0KfdVdki/UJ3IUjFTBJp+cN+" +
                "1haLy0wS4QWXC1lW6C72lTjTpwDTmaFPH7FxFtf8SMsfivOF3K" +
                "BPj1Zli7PbV2OquF/57pxbtb21hLB0H+nNbnNb61JGm6fqWNDH" +
                "y7hLWJpza+urtZd8Za9qTDVnty4L7e2PtZZWR6VDWOtbZdXFnn" +
                "3r68SpdbE5km+0Li/79FfQl2GG/YgtKny+1vomHIW+Gp/oTuSZ" +
                "dT+S45LjyMYWL1nN6oTrrBKNn7vj2FfizHoKMJ0ZOK+zcRbX/E" +
                "iTapaj5gZ9eiLMqpno7I0p33GaaU3QgPf/NrIJzpKOYc2upzAf" +
                "rqfbOJbj0mG7kjWmfXE9Ddt6Fk+HhB9hUk1nttxqte7xYVbNhD" +
                "393q12q6FjOGPvVtOA47rFrcb7gtLCnjIsSjn65cM+3SJxcF+w" +
                "WpCwclwD+jRbY9aH62mOUk18i/sCVbU7EmbVTOSIYFzq4CzDM0" +
                "mk5Y84PP+IxeIyk0R4ccyXsizo8A/ZV+LM5y7AdGY4bz1t4yyu" +
                "+ZGW74nzhdygT5+oyhbp8AKpmEkirf5+P2uLxWUmifCCi2NZ0O" +
                "HvO2ejxT+so19ln0ycxTU/0jz70DfkBs8IC6uyRfo8GJvKmSTU" +
                "8kdRLy3gPy/QjE447jexpfCahzbJPW/0CmPf5OOCzDJMTrYJXl" +
                "bZZDiqI1ES1/F72LobKrOF+nK3HKRiJom0/Ck/a4vFZSaJ8KI7" +
                "YBleKdG4ntaxr8SZ9RRgOvPwSlhPJs7imh9Xj/NJnfJzt7sqW6" +
                "TPdDNBwhltM2lAn/5BNrG4mc1Pa83NzJeL7i3lMYNl+CqJxmNd" +
                "z74cLf5hHT0w9qrmZ2ycxdnGm68e55M6ZZ8eDrNqJjq7u85dBx" +
                "LOaLuORnJycjLZ2OIlb9PDoh4XL50P7p5O1rE6ruxThIkO17sr" +
                "NGZ9mL9smmlYi3lin14Is2omHIPSIgf3oDyT5EdyWnKaWzT2dr" +
                "F4ydtE0/6YD3C8ozyNLYzD6rxeYiXOrKcA05nhjLvXxllc8yct" +
                "ZOpHcV+wiHnWar1aVbZI3+g2goQz2jbSyL+bnEU2tngpX8ea2L" +
                "SUr/d7H2nzYffO6h9X9inCRIf1NK4x68P8ZWMO/WoRWvTpHWFW" +
                "zYRj/L65R+5j/ZyV98hZ3d5j6zvz8EXPd83bBz25Df9g8JOdej" +
                "qcHWKj/8ye7seFbfR8ZyL2hez7vXofqPawR+rWurU8s+5HcmZy" +
                "JtnY4iVv0wPO4wr1uHjpfLieVKzOqplYTHR4EhvWmPWRo+BNMw" +
                "1rMU987/4XZtVMOAalyx08PdOMtstpJLOSWWRji5e8TQ+Lely8" +
                "dD7o0iwdq+PKPkWY6NCnRGPWh/nLppmGtZgnrqfTwqyaCcf4/Y" +
                "Dn4GVH/hwsnvidzNV9n4OXhc+xL+c5uPOVl/kcvGzQczB/P97r" +
                "HPFz8Hrnz704Y+/W04Djusytb/5YW9hThkUph5abP2GU8vFo3i" +
                "hx5XqKcuq6zYc0Zn24nuYo1cS3uN6pqr3rw6yaiRwRjMVuMUjF" +
                "TJIfyVAy5Bbj9wWFxUveJpr2x3yA45loiC2d6xilfDw610pU2S" +
                "eTWUaxntZoTPtwLPMnLWTqR9GnxcwT+vSLqmyhnv46vUN/ZrLZ" +
                "6f2wjn8Pn57p9KEOZE7/CJ+Qx0ZHQPq7/z4TtH+D31vTQ6Pngv" +
                "6frJa9ooh9NefJXpvNGMbfdEbnlX24Hz8hf0n/lv6186P08fQp" +
                "8Hub/tRlx2SvxOjjRxdkr8/elJ2U3p7+Cs+3K/KlxWd1Mt1N/u" +
                "kfYHsEvE+F/VD6dPHN5rPFGXpF9hq8fuMddbo9/V16r++TvWau" +
                "ejXr6f70ALA9254HsqORz+uyN7g1bg0cA854NGtoJKckp7g1+b" +
                "Ni8ZK36WFRj4uXzgfr6RQdq+PKPkaY6PCu/Udj1of5y6aZim+x" +
                "ngqe/rXq9WFWzYT5oZS7HCSc0ZbTAGbPu7wzri3sKcOilIPl0S" +
                "nJh99I3ipxnc0SV/Ypyqnr5ks1Zn24tuY4ui/OV/RJVV11TphV" +
                "M5EjgrHBbQAJZ7RtoAHM/ks2sbCnDIuyf5wPz+Nr+8eVfYowXb" +
                "dzm8asD9fTHKVaWCuuqrNqJnJEbkN4fy3Xx/wFfV/Akv+9Rd8F" +
                "vNT34/IaO0H7x/cFEhVf96FPv4y/eZeKdF+gOYwdX8Wy4heb/T" +
                "He3Kui9/cf4PWKxv7W2tB6JDrcPxUy4004P4/NEL/OteJRHN/+" +
                "aiZ+66wZVLd5jNhoD/dPkS+ex1HKV2p+OlvIhXW63qW/Se8s+n" +
                "pPen96H17vXkwfQssfYXssfYKvd+m/0n+nB0E6lE7H17tCgutd" +
                "ke/udGe6A9/hmemjfL2DPV7vzDv6or/eZa/y17vsBHu9q9VaF6" +
                "a/TXeBF1zv0gdg/2D6h3RP+kj6Z0S/6K936T/pepcdmx0357Ls" +
                "NdkbsxP99S69i653RZ2H0z+pqk+m/4B5L13v0mfQdjh9Lv1v+k" +
                "J2lLreXeOugU8gzvhZvIZGrVZ/D9nEwp4yLMr+cT6UdvaPK88U" +
                "Eabrtmoasz61muZHmmffv1ZcVWfVTOSIYFzsLgapmEkirfGww9" +
                "9RxWJxmUkivKh+McsKvYd9Jc7cZwaYzkxsdJzFNT/SyD/mqLnp" +
                "yDBbpF/gLgCpmEkirXW0u6CzU1ssLjNJhBfVL2BZofexr8SZPg" +
                "WYzoz3BSbO4pofaa2j4nzFfYGpW5Ut0ue6uSAVM0mk1d/l5nbu" +
                "0RaLy0wS4UX1uSwr9F72hbybxa5jbE7tA31SmPaRepqjZx/6Fn" +
                "0ydauyRfoqtwoknNG2igasp9lulX9uEQt7yrAo5eiXD6UH+8eV" +
                "jCNM14U+Kcz6cD3NMd8T5yv6FFXVWTUTOSIYww7uYngmibTG4w" +
                "7vb8RicZlJIryoPsyyQnezr8SZ9RRgOjPcFzxl4yyu+ZHm2Ye+" +
                "ITcdGWaLs8MTxZL2JY3Njan2ovbi7OPlHU3513mNzXIPB8/Bmy" +
                "u/9/2V+MYvNznwW9/NEhl/71urdedaHy+VvDbH3/s2us3pfmy0" +
                "RedI9w9ipO4j7hzGvznKyr9RyK6Fc9Q6PDccUzyDX0b3T1V9av" +
                "0vuz67srJPD7QPlxwew7wnqL97uip9UfrUfi6Mrq9rT6e78qNL" +
                "vg/CtqeIvWI4+nupsTchgn+X036ec4DlW+KTH5V9L32yqk9w/3" +
                "RJ+KzQ6jS2wfwd/dzSendjqvsztGzTzy2k6e/6zJPEtqrnlvZt" +
                "8XOLetLZ5vM2plrd/s8t+L1K4VP4T5VMtsXPLa13EZuQJfHjmq" +
                "LHzy1UjaOT6WQa5ZsIJd1v9VnJdHeblxs3JdP++ydvhT7dRB5g" +
                "O5f2lIFwyiM2+f6p/UtGIO8E7dnLx/m8pEPmBYyzB/ZJ+XiJmY" +
                "iVOODamUVsOGN4nCyznu73uPn+6SYd7c5z53X8f7GcB+fKp/3e" +
                "S36rn+TO6+4i2W80p/vYwjYtsRfLZj3drv1pb70kQ2cv41w9X2" +
                "p9hJetx3vPXvS4VvwcHOPmGBe4BSgvKDDU/dZ6n1vQvZdk8cE+" +
                "LWA/2ovEXiybPt3hz0/k352gvfWSDO3nGOfq+dL2tPYRXrYe71" +
                "vvFTbt5+NaffoU4eYYz3Xn+uudg8+Qv955HRDY6kN+Jlkk7BNa" +
                "/Jwv93tGtT/Jpk+Psxd7hF5hBvGF690h6yO8bD3ea/ZSdWCfIl" +
                "wfWXIoOeQ/d42t9Lnzup/B8ucEuSWHGltZwvPTVvJgG0tF1Fa2" +
                "Rles7dqf9uLV2Orzcs7OXsbZo/uM9fESMxErcaBqxCZkSfy4pu" +
                "hwfgoYU7Uy+kByACSc0XaABqzc95NNLOwpw6LsH+fDK/Wp/eP4" +
                "FWO6bndaY9aH62mOY7OrasVVdVbNRI4oOeBG3AiurZFiraHut9" +
                "bpbgT/XmWEUT/j526E/WgvEnuxbNbTb/H8NMIVJLJc52UGOD+N" +
                "iIffxt5ZnJ9GhKnetJWqCRs4P2GmsbcN/NxFjPWRJYeTw3g/fj" +
                "Odn7wOHYStfmWC94XJ4cbNLOHn7mbyYBtLhPs8gpq/wzio/Wkv" +
                "Xo2bfV6pwzh79I6xPl7yW34s7TUH7NOVxCZkSfy4pujwuQsYUz" +
                "WOdvPdfLwvmF/cF8z3kt9aZ/qZZJHSfWxhm5bEn2TTp2e0P+2t" +
                "l2SA+4L54uG33jutj/Cy9XjfOkPYaJYD1lOE6yNLDibwPtOMvT" +
                "tIAyp9mGxiYU8ZFmX/OB8+yb65f1x5pogwXbd3usasD9fTHFsf" +
                "qqoVV9VZNRM5ouSgm+FmQMdwxt7NoAEr9xyyiYU9dYzohEsWnQ" +
                "/79EH2lTjz7gWYzgx9OsPGWVzzI82zD31DbjpSMtj6rCc7k53Q" +
                "MZyxdztpwHr6KNnEwp4yLMr+cT7s0+n948p3NsJ03eY6jVkfrq" +
                "c5ts6qqhVX1Vk1EzmiZGf5fDgR/n5Xf0djyv+9ClgmzG9hE/1/" +
                "GSsiJ/RTcvMnwqjjxL95Y/j7HcRNVP1+57fmpsBnQvGaiJ+DPX" +
                "vNFaveIMfJxyx69Bw8oaOT7Qnc/9GMvdtOA9bTp5PtzTu1hT1l" +
                "5Ms1Sjn65cM+XSFxzRslrnxnTSYZlLF5l8asD9fTHFufivPR95" +
                "lxVZ1VM5EjSraX78+WaD2d0ZjqrkLLFrOClJYvj9bTlqrvVTor" +
                "5Z3tfSFaT1t83urfg/H7Au2zRfHa0mc9nUFsovW0RdfUerSetu" +
                "jo5O7kbreJZv+/DF4irf5BP4sF+nq3aOylUcrBVuzN1YzqTOxN" +
                "Efx/GzEmNZgZY5YH19YcPfvQV+rw/21IJOdhm+QmvXx/xqP1dH" +
                "7ZzXGznsYHnp/Gq/4quH7+oL8vgLjxQecnH218xhWv8T7r6Xxh" +
                "w9koh66p9Wg9jevoZEeyAzqGM/ZuBw04P30u2eH/nk4s7CnDop" +
                "RDy70vMwpr69sS11soceWZIsqp6/a+pDHrw/U0x9Zn43zF+Smq" +
                "qrNqJnJEMCaTSX0/T7rf6guSSezTJNy/TxJWPDGUGpzHJzkD7Q" +
                "llW099b93ZxF7JZO9SiVNPCZOSmXBh01tmfbzETMTKHIm95uoH" +
                "np8qnlsk0jKSI+PfEQrvH5Wfk0+GnxzC+v2OEMb2e8X5wr/DGJ" +
                "S39QXw2KW81e8IUjlkn5n/QG59PubKvyP04WN+R6j+r4DhOyDz" +
                "2v4YPDOfNSi236ujvsHvLay9jJfP2/vSwB4P9WMf/Sp0w5FyLY" +
                "7xo7FtTvGbWVL+x0nj79H7ujG05csH41rv3BXb58w+kj55TjEb" +
                "zZxZD2JvbcnGl65r2P0f9IhDxw==");
            
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
            final int compressedBytes = 3254;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9WwuwVVUZvqIFcROT7IIMF2ZqpmnKphkrNXOaezhnp0nNlB" +
                "pBc2smgrQRLJk0U7C1zj3n4D13qoFBashBrjShlQZmEwEJmAii" +
                "CGQ+MhTxwXQhxUQtTLD12P/+//Xce/No79lrr/V//2utvV7/uu" +
                "dWF1QXdHRUVSovmdOlyiZNQ0p1QeMHUEIazTVulG/U15gHaEfH" +
                "TVtRrnE9ysFlajZtCE1zqT2TG+zRkvTe5m1cB3a0TlqiVFM3bZ" +
                "2OjtqEVG4QKI26eJZ0kAsx/xXHK7dhvj3LtJqvtz07hEodYJl4" +
                "z13O+mDxutjeVRdXF0MKZV2qbNM0pAAn3iYK/K4+1U7L/HLUE9" +
                "+tNbZvpnImN60FvKX3fluo06w/Uk3daXlhdaHIqVTRFupbfJEf" +
                "aRpSgBNvEwV+V59qp0G/XNZOC/231tj4MZUzucEeLbU+FLKFOm" +
                "mJUk3dtDaij3WncrcAJXnJ6f+35IyPKO7qQ6v5en3SqAMsowet" +
                "qcfia9i72vi0NgeAMmmRPT8hFmiJA1ImdPkwsJp/NX4erI3Qwc" +
                "b5vI/NT0WumHd8Ymbppw72qXBbSTofYcqYvK6+ohc/X8xPi8N2" +
                "sZ34p4ta4+eV86G6qLpo0jKdCu0qp0syr1N9S14sARdFtQ6fPp" +
                "mjsqYdeWt53039AC7TD1oLtO7aqg+CHa0T/EMvq4ugTVC3Lvu/" +
                "j+rpK4SeHeXGXQw1dZW5pN64tGfc7Yh7kFcXZwyOqY2BFMq61L" +
                "hD05ACnHibaG1M41fIRfWp9W6pX456YmLUbnsJxUwerAVab9we" +
                "suVapVqpJ8CpcuNq6lvU0i+iy/JprKyN6x/QeZuHpqABcdBi7T" +
                "N/gfztp/Tb5KK+AA4cfVfamsEv4DZ96Nvlehmfx12P0R8Xq96b" +
                "jbvfO/vxe3PmOgdvP0t6/XxCf6aMFiWxu5xlsEaRsutd1OK6rD" +
                "U/6bTTuqKyWe32Y751KaEPldGiJPaVs9y6xEWOazutzb7I+U47" +
                "rS0q643vlhfsT2uPoj85MtJ7Gzmu7bQ+G3d/dNppfVFZbzvdWr" +
                "A/rT+K/uTINFa7yPFsJ7KyXlBu3c/Db9pV1gOj3muK7AtIf7rg" +
                "6Nb/4JdYWV0JKZR1adJnNA0pwIm3iQK/q0+1016/HPXEd2uN0h" +
                "uUM7mxFmhd8/tsuVapVtsTVyKV+1P2RXrCGFx9c+I4vfq7C345" +
                "r5aB3nIy4H3cp2PoX3dnPV1YkPsnH5Yn622nb5FaX1tWC+yfis" +
                "o01rnI8ZyfMA5u3G+3E/9CfFbhI2IzTY2scQM3RDz4fJl2MuNg" +
                "1xp/3wla7zZk7bQpjOXJevvTrLIeGOOuv5xM365iPhdulz3VPZ" +
                "BCWZcamzUNKcCJt4kCf988W5/a+U3xy1FPTIzaHWhTzOTBWqCP" +
                "tdkhWzLXN9etP0jbunU5OSB7rk51P9a3aKctyQE57hCHHJWBkj" +
                "nObH2qnb4GssmBgVtlyirmCKKa8YZxZ9ozcTYO/dNY6xJXnx53" +
                "qBNLyEk9AU7l633sz8LO/SwdZewRtpPtYI+LdnqQPSnbiT0lnu" +
                "fZi2Ksf0nk/sleZq+ygyL3GntDpId4Bz8lnQuy+Ymfyt+b6tvC" +
                "HmZbVTt9h+3mX2TPsufYnoHl7AX2D2sPdISfzN/BhwvpTv5ufh" +
                "rv4mPYBrYR2ok9yLYJrr+wR9lfxfsx9gTbxZ5mz+h2YmIfyl4S" +
                "z3+E/Dv5u1pX8ZH8dD5azk/sAbaZPcS263Zif2N/J1b3MrHnZf" +
                "vZK+xf4v26ov2bvcneYof5SXyYqs0o/p6efT3CAqQ6p0uNbT1q" +
                "D4wUE8dU5zSuL6AArmbWq4EX5Wg72RjVLMbdr005E6f+6RJaM3" +
                "2kvlFJW5tdbryJ/VWmZL3bbq9Z5ghtHHbW4rez3BHPPH4VLTVP" +
                "ElxvBU4I/+vZj+/w+RJe7xqPNA5JtBlf3ZeE9+vm+FTt9Zp+ZM" +
                "qXZvHYNYCZfJQSaifvendNfEUJ61X96Tc+X9BvJ5pMvc9pp8Gw" +
                "Vbem/v7UHB7vT2Wv/rnHEh0O3FmuP/HbikR3fHmp/vS6fmTKV2" +
                "TUVYCZfJRSqp3mEU2rPN8vqndglc8X9NvRtipfp2in28McZk35" +
                "70QfHaUfmfLfZsjdmkL6suKjFMV3T8GT+O96/LzL1B6/JAdfSX" +
                "1Bv/3WiukMIT6s0tJ9Tb/VuDsd6EDT/RA5vNpb5RGNxnG+z+Sh" +
                "flVa7rjTmKuTUqS+mL82VjlNPzLl2UltZQ5gJh+llBp35Hy8Oc" +
                "zjV65e1xf02+GdU16njZgY38yn8y1sE/+DyF/BJ3KxAnMRFfH7" +
                "FPrNjO9xPkOkM9nLRNZ79sgfdSgipm79MO4xOxLtT2sFxzbC/Z" +
                "h4RAzHL1foNud8/EZFH23o2JjltuM+M+jPYf5ta8Z6VT8y5dnv" +
                "HZqnAGbyUUqp/nRzzr4gV6/rC/pt8zZPLq/TRigWju+aZ8BeIT" +
                "++A85GEonvltjxWZn4zo3EcuK7n7n63PW+8dmi8V11qCriG52q" +
                "2HhI36KdujQNKcCJt4lWh5LxyEX1KWvj/XJZ5O5g1C7o9PEght" +
                "bRmm2LWtVcVCv1BDjlOxwHN7vZk4qCcfCVoTiYz9JxcDI2HAcn" +
                "Y9luUVZxsHiXjIMFXioOTsa6cXCqh8TBwqeCcbA7TqD3Nd/v27" +
                "Ob/dM5T5ng29Wm3BPiu2Oq08dp+0g90fsCY26YgBKmNmMsT7Dn" +
                "EHPcebyYkvaEV7KZ8KMmlkyBd7S2QbQ1GJeL67V5ZA5KPtnWMr" +
                "83lJJM4QfDVv0eQdvxLIZvnm1/6fC3LhKlTeqNy+XHjeasTvuz" +
                "5+93vX5vzBiRHy4a3yWXJZdhqnO61Py4TCnFxDHVOZUfRbWitM" +
                "qNAl6UM/yyMKrZ9NTkoRjxcZSrz/YNfPJpc7WnEtPsv7fwt00s" +
                "mQbvyBfvDqNJ9O93oL0oj8xBySerrXno0wx9MX+9HiXp72PrWQ" +
                "9unmtiyVR4h/7OaaNWHPBCtA2mJlNz24nwyByUfLLamkunlGRq" +
                "vTts1aeV7iOz9a7nKNa7rsh61/V/Xe+6Cq13XfZKF1rv7PkTrV" +
                "c+SPfjOtf8sluj2N9bmuSLVFcjf3OKux83vbV9bX4lvIOB/Tj1" +
                "gXqfWb207AqE0rXOWmdHh07VuU2nvgW6S9OQUuuUNHqbKPBjHv" +
                "hVfLcU5VAPOTFydGIZNAFm8oA9fEwLaBXsmFapVuoJ1kjco2tG" +
                "TK3L8ml+VSNQhpSW+uZIfpOOWqw4eCMgWqPLAxTEwzzUL5uq+t" +
                "806rNPk3OyNzpEk7I6bhHfqxfWO7ZT9GMRt1SyuCXpZc8rbL7m" +
                "ZCqOZuqU3f77ndaDcQtpJxUZheKWpDfplXGLkhZf0Y1bNI+MW1" +
                "IvnhDP0xk9/VuTjFvUuOvmIyWif1+AcUvSS/XRsoxbHI+suEWV" +
                "zjT3Bc2v2zWt38n35KxIEwOnR8+FsWIXj/7KzN1nFrNWzic+XV" +
                "hSX1ue02Xt9A3znE7kZ/BD5jldYQuinSp7j+mc7o3YOZ3n3Hev" +
                "e07ntVrinE7pnV+Zrd9ZhHSOeM6VZaDZHN5d0vzySGp/fu75OO" +
                "Ex/Ep9d625OilF6ov5G8aM/jTDQWfUTy3Ue5xz37o8yX7xWMZd" +
                "fWQU7XRqGbWmz32bM8v7AedP2E7VCyHl6i9vfcP7Rsh2ouNO8x" +
                "geXAftRDHZTi4v1cNviI276oX10XLc8euhDONO2fy+207amj3u" +
                "hJ1rsZ3494SekuMuq1H2+7N6V37runFLQOsYsd490HHCrvoZTn" +
                "9aXUSuGJch0U7/ftfO5qcZQAda+ve7dr6ecohG47ianwgP9avS" +
                "9szjbb9NSpH6Yv7aWM9B/cgUz5+S/YCZfJRS5qrdkfOXj6he6Y" +
                "3rC/rtt5bna/0DYY5YTSuf4z/J5vHLXTTSHxRW/3CYv/+Y5vHm" +
                "FTG7rmeVDYVG0YbC4834rVblIixXnP+UqFwU1uDDTFr//jzr5f" +
                "0FG671YprL20/lkvpZ2Re8OsvNAfRoou1svnvoRMzg6ZzpeNba" +
                "Wmj+/1jpFlK/O65MrmS/P259RDxnyTLQKpOR05GfTPWYNBfx2Y" +
                "/jfTtNHvBL2hD0yYH69PvpaDNs1cTk73oIdjH+rqd+tiN5sXcl" +
                "uieEmbTaLz2yd5WOgFb6bLjWWw8X6k+fKNyPhsyTuMoQRYr8nq" +
                "4yVNDSCr/12Mh1xxhI5P2ezvXeX/syv6ejWP2crK3PKzIHFb8m" +
                "DT9xsp52WlPo260pPR/OtOOW2noTS2bCu4geT11HxOVierUs5Z" +
                "E5KPlktfcunVKSmfWesFW/R8n0tJ2y/VPrTBNLpsM7Wt8g2h4W" +
                "l4vrtXlkDko+2dZYvzeUkkyv18JWLa3/AzxviI4=");
            
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
            final int compressedBytes = 3213;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9XG+MVFcVH2tBW0qxJUJDmvil2eiHGj40NWYNmTdvXjexqa" +
                "n1DwrFprKBamitqyYGP/jeW5hHdilKVlr6hw+N4YOJCd0QWoFa" +
                "bauUFWHRVcClrYpKFShUZGmL1HrvPXPeOffvzuzOdm7enXvO73" +
                "d+59w77715e4e2UqlU4isrzT6+sl6rVMCS9vqXAcOXbvls3hNi" +
                "csnvw0iT6nLnlSj6zOycyzl42GqhV7IU3rMflLmf17FkKb77Vf" +
                "Lb/ejAwnD+kK7NkSO0XLFQve3nnmRpfoc/q7uiZHVzpp8pM72g" +
                "Y8lqfKeo/j6fjmOdPhVcg9X+SBdHjtByxUL1tp97ktX5nf6s7o" +
                "qiDaqvw7t8NVahH31RnZiW6jmuo/tsxJU/jJscrEvmEP66Zz4b" +
                "3H7S82d1Y9FG1ffAu1qnjehHX9STf54sI76H6+g+G3HlD+P5F3" +
                "QO1iVzCH+PZz4b3X7K6c/qxpKVzXq+VK7TQzqWrMT34LXhRQeW" +
                "hOPCuiZHjtByxeZfdFfDPcnKfIU/a6ii5Fx+b7lOm3zX1tReA8" +
                "l0ovOvhtB0kcW/pyXVle3UULuAfe1C9hH0kZczkzE7Uq3hmK6k" +
                "c/OvoZ6dG1q4utqF/P5QXtmjz6ybRvmTnIOHOcfWXrhO8fx4vj" +
                "inbjTWf25LGmPW5zZPKk7rfPpmEJ1jPXUFs2WjU6khmg2H7LPL" +
                "zW/8h+IliBGv/4loNveYr/5tqkbnnSheMnkVrTOwZqzbnc2nib" +
                "Xk3/VnDc8UnzPz7+ffq3T0NfC5yoy9HPentZ3PEs2CQ/b5uuaK" +
                "d8Vd4rpbBn7kxV3RLO6xPqcu6k3E5TWrCFxZDZ2BNWPd7kp8ml" +
                "hLvsGf1T3TpFe/PwmNTTqW9OJ78Hm813s+fSv4nd/rj3Rx5Agt" +
                "V2w07K6Ge5LeaNif1VatbhfeVbKvbsf7U3V7ba3sAVNxq4ADFk" +
                "XqShIl78C3CTG55JdNqrvrhTjIjBzIgxb4UR/ea2sB4dVAHuTI" +
                "cb5ZZ2jr5KwoGtTfxTW3DW3y6Qzn2TrYPgJoGM9O6RxeVzRo35" +
                "8AszW5R+qF6g1VlP+oHG3p3B0wf1icW9+Zuft4PmTN8umW7sxP" +
                "t5MlXox9vBi/7+LF5DWZk9nuSJNLfh9GmvHifGsorzyf0Gdm51" +
                "zOwcNWC94jVzQ/mZ+Ua73PxFyWT8dxH+8Px4V1TY4coeWKhept" +
                "P/ckK/Kf+rPqqtGaaE3tcexhBJYci/th6ZFc3dJtwNU9tKmH+q" +
                "BDXIxGv2zoIYwqkSzwIqZrUS1UI58JMVEHNU0uqZl2PCFatzr7" +
                "uuUhbfEufHIMHmkrxkT/E/CuGBPIB1Th8DzeTarsnIcsE2UGaN" +
                "3QEOP6yMBY4ijfBD/KWZSzQS3gUhzNtaypG7Na1/xEWfP5+Hzt" +
                "EvRifdUILDle/zZ5BP88WcjiKGhwPfBKFPR0NuaRjTw0ohyII6" +
                "bXgbl5jXwmxEMdmCvOFxkSo5rRh5mbV+Oy5v3p1vI7ZIeJuSzr" +
                "qvaiAz8Lx4V1TY4coeWKjfa6q+GeZFm0159VV40PxYewRxus+i" +
                "3gIw8yqeko8m09tU573HG8Eh3jeXmlJocwyh4948vFswKLq/JK" +
                "aEaijcajYqR65RuFJp4zL4GPPMikpqPIt/XUOu1zx5XrZGE8L2" +
                "q6OIRR9ugpXy6eFVhclVdCMxLtcHxYjFSvfIehVSrFjeAjDzKp" +
                "9fdxFPm2nhwNzqY4rlquk4XxvKjp4hBG2aPnfLl4VmBxVV4JzS" +
                "g+nJzTd3PBlke8CRC0ad+XrP4+yUeblExVtU5zEAHF5FwamfvK" +
                "PCM2N4fXJZ8z9drECjzLa3YpASu8q03R6Qvpi2q8HPcL0t+JvE" +
                "fE+XRLekzVMJ4sT/+msJuBmaq7f3pBrlP6dlbJ1O+p2cezD6JO" +
                "Njez9hMHXlJRf0lPpH8V739P/6lVtDxZnr0/UzsZmVjRbF62IF" +
                "uY/jJ9Xuekv0/HmlUcFccrpf9Uc8Xeaq7AuuxqiWQfVt6X0gPp" +
                "KJ8njKN1ZKf/NtZIVnSFqufa7EPxwfigOHdUr86xg9DEOt0KPv" +
                "Igk5q47hiKfFtPrdMRiuOq5XVnYTwvaro4hFH2aKcvF88KLK7K" +
                "K6EZiTYSj4iR6pVvBJpYp0+AjzzIpKajyLf11HV3jTuuXCcL43" +
                "lR08UhjLJHu325eFZgcVVeCc1ItAPxATFSvfIdgCbWKQEfeZBJ" +
                "TUeRb+updZrrjivXycJ4XtR0cQij7NHPfbl4VmBxVV4JzUi0sV" +
                "j7dQRseRSfBgRt7HVL8mlE/Nj6zaV2FedTnJmbFIgLsZzD6zK9" +
                "agV2uas07uO7eHbH3y1jujrbyaHn8Wcm2fPZ1d4e0eC0fpdq93" +
                "eEaEdL+087ppqRrdOeydn2/nhgnT76Xq5T42ArcY1DLe/RPRI/" +
                "gj3aYBW3g488yKSmo8i39dQ6fcwdxytxNVDklZp1uLDoCl8urg" +
                "ksHmlWUtpD8ZAYqV75hqCJdboDfORBJjUdRb6tp9YpcceV6zTk" +
                "bqCImmY8z82t6ru+XFwTWDzSrIQijPNrG46KO/2Y59wM4oN3Tf" +
                "WqmiwvMYgZtRQRtV7DlngL9miDVXw23tK4iXuQSU1HQcOlp77v" +
                "DrnjeCWuBoqNIzxOZ/NZ4DtlI27+JJ+xOX/y6tp8Nq6/cop7/J" +
                "hnxyaIN47N/H2cKmgc7WyG+LH4MezRBqu4F3zkQSY1HUW+rafO" +
                "p2PuOF6Jq4Eir9Ssw4VRNjMX17QjzUqIY/yeWP5uXjwgPpWbNO" +
                "yT4TWXfwfrr+Ib7Dnz+HQ+z8aRVs6nrDuUDa67KZ5Pj8aPYo82" +
                "WEUf+MiDTGo6inxbT830uDuOV+JqoMgrNetwYY1xXy6uaUealR" +
                "DH+5z5XGev8MFp/bue/BftPWfW7q/M6Iut04viHHilc8q18Wld" +
                "d6+2uU7jHb7utsZbsUcbrJroG69xDzKp6Wi8tcZYXE9V7onjlb" +
                "gaKPJKzTpcWM2bi2vakWYlxDHWfH85Gunwdffl6USv+0oQvds6" +
                "n77e2epx37h+g7k/XqxLzsnvu9D+uL7zPMn++FP2/jhmnXx/vH" +
                "Hatz8uNez98doDdpXh686uuH4DRde76l3Co3qFdUET67QefORB" +
                "JrX+Po4i39ZT67SX4rhqWZWF8byo6eIQRtlra3y57KxclVdCM4" +
                "L36kU4ZJ+XOw35KGL4Ah73tHXd7Q/jk+vatVDd1n3/TPuaJsKx" +
                "+oL6gtqPoRdnqxqBJcfQQ5NcspDFUdBw6ckRj9XzyAbxpKRzMR" +
                "oxvQ6aBc/uzkV1Un1UpTz0/KW9qK6+U+vNb1aw5VH8kBCTw3tU" +
                "IBxVjE/4Dc6Hd53Fa0Hc1NGz0WHW0LjLV6XvZeNUT31RPBwPi2" +
                "8+1avvwGFoYp2GwEceZFLT0Xg4OUksrqfuhifdceVzwbC7gSJq" +
                "mvG8fl4jZTNz2Vm5qlmJHdGM21n+bfawH/M8iwXxxpstPtHtnM" +
                "JToBXTuDh1tTafx490+PlppDJjL8f++Lsd3lc5EZ/AHm2wiq3g" +
                "Iw8yqeko8m09dZXf7Y7jlegYz8srNTmEUXbKZuays3JVXgnNiE" +
                "dY59Pxzn4ixez38nwqZnU2Q/VM9Qz1MAKreLx6BhkunHoYAc5V" +
                "KVpVPge5FGdWomvqHD1Ox3l9YBVX23pmbaYqV7Pss9WzYtTsYQ" +
                "RWsU323KPj1MMI8Gb2szgmtN6DXIrT1snAuDJp2hyOUY2UTa+R" +
                "12aqcjXLPl0VfzlhDyOwij1V9TcVeXScehgB3sx+GseEFvOQS3" +
                "HaOhkYVyZNm8MxqrG41tYzazNVuZqtXv6S86C6K9HvCM9avzI/" +
                "OK3fgw/M3P3Jrix/vaVv9/NTfi4o/zvqfKKd382j2xzVa776yh" +
                "lZodvc2TudLT4Vn8IebbCKcfCRB5nUdBT5tp46P693x/FKdIzn" +
                "5ZWaHMIoe3GdL5edlavySmhGPMJ6Lnirs59IvXc60e3u000vm3" +
                "eP6r7kPtnTIT5/8bda8Rox8CAG9PTuU27qzQ/xdE0XE3WoDrMS" +
                "XqN8fsIIXY10+MypR22d59slFP1mOHQ/7Xea+5v+/VLAa7vDvx" +
                "fr+4/GLu7uZLNZI68EnjN5jbXd+v6oa658/5Z6/781Fnl+lf66" +
                "ebVdFpb6V9PJFjiajH8I7B3xfiYVzxTpf5TvItzHs8D/Pyndn/" +
                "42/Y2a6970z8Fn6v8prQ+o/hpjnfYKfCRt/pum9A/i+GN6NH1Z" +
                "vL9K65S+Dv/eN5udXSUjsuuy65V/n5bnT2n5l0axMD2Z/kv4Tq" +
                "dviPn9N1XfXemb6aX0cvpO9j4W9n9tejCD");
            
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
            final int compressedBytes = 2857;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNWwusHUUZPsRSpaX0cb21UEljFV+QiGKQqtXuOWd5mEvjgx" +
                "YU8JGoxBo0Xo0JBnH3zKZ7bo69t7UN8UFNiEIFIT5joS8lAYvl" +
                "3gpRQCLio5HEIi1VaamW6s7M/vv/89jZ3XP23rgn8/q///HtnN" +
                "nZmbnnNhqNhrdIJp53TjbSK36u0QC5vKQelbDRRumrN+nGqV/9" +
                "is+0c0HehsWSIp9FUdU7hcvfmZY/BEn3NBXzd0LpipuPNne57V" +
                "x+pS3V4bWMs8UWLFz8qI+yjPwd6QiZlfXTGSrm7/B3tCLUtF2t" +
                "iKNcy0RsUhrf5dfU4TVo2WxlNFMuJYBKvlUY+Q+l5f1ZP81XMf" +
                "8hKJ33kovGy912br+6Dq9lnC228avsbKiE+ihi5K/x12Aua7LV" +
                "HeY5lfhr2C20peoLfKv4tm4ACeAopdagQzWpT1VHtVNxyl+2MJ" +
                "rKEdB01N2Q5830LmqH/cM8x5T00xLlLoS81UFNyLFM4nYwR88c" +
                "b3W4rXO0HNZzHafxwK+UBWehXGKcA1io3gCVTMGLGh0+Oofg/u" +
                "CBcDub02h0TiSth8NfJP10nqLx12Tumtu6Mfh7cChp/VPIjkos" +
                "zOa01o2Yp3YPBpPBPikN/ujqp0C8acOXivx0HV2/M/hVMJVq/j" +
                "ZJjwaPB08m5VNCXyDBs0l6IWnNDk/jHMKFoXhjBb9U4jwR/B4Y" +
                "JpyeDv6WyJ4JxNs9eF7kx4J/ByeCF8NT0Ko9pz0HcmjLVvd8KU" +
                "MJaOJHRUGffVn3JzittdtRJipG41Kmug5iyBGj6bF4jaX9FL9W" +
                "90qZ4B0ln6H2EO1t2eapu1Yi0OZ56yaQYI4eAG/dpHtNxuJmsI" +
                "UIpg5IEEed7pWqDuWlS4GDnaVE1ZJaqozQe7g3SQ8mabu4n/nh" +
                "w0mdP3dXqzbhY6af8Oe2Jyj8jSFJ3qL+440BrtC5agunjNnMEi" +
                "28L6v9ul8e/r7Uw7JsXXCtivn7/H2tEDWt66eQo1zLRGxSGt/l" +
                "19ThNWjZbGU0Uy4lgEq+ZRi1DrQOQA5t2ep+pnUgPpdLiBZDHH" +
                "Niy0TJdH9Cltnqdnpk1EOd+DzVTsVBhmx1pvzTuTWLw9KYTPdK" +
                "majeyRxypjKeArOPO/OKvnO2CJ87lq0sOsmaNT5/kOeOvdyFdu" +
                "Ya68w3OZ/T0s8dG2aLNVsxP3U+Jup8fhq3WJ3d/53GlwzST+sr" +
                "zk/xxY1pvZq30Bq2qlq7sap+iyzkOrNIv3pUYru0uRRzWZOt7g" +
                "TPqaS5tBXQlqovcfE8ByABnMvQFu10JqpPVUe1U3HKX7Z0piqj" +
                "VjqnAFPTm+ldOXO5jmW71e7mZPS+RUX9g9pzOIqYeEccxBxk6d" +
                "vjoG5b8am9IOeU6Do9EsSzzGK36mh5TuztSXqnqF1C1wXsfd0t" +
                "ony/fR5n16r9xK5gF4nyQvYOOY+zd7F34zzORtqzrfE/wNayD7" +
                "Gr2TWZ5G2aRotdDPM4a6eySwn+HvZePo+zZK/KrkrSCrE2zKKx" +
                "lUTXY6vkPM6azE96/1KDz+VstdK+kn1Q+V42iXxElmI87QQ5yL" +
                "wR1DS+1xHqR5WZiMV+kxvXdYAXj5HIR0xde0yF3yZXVDvmTXif" +
                "k2XWT/dBG2S6htX7RHUkjT9R2E9ER+GVcjejmT6phPtz8bVhfn" +
                "oq274MSqgB5u+C0rlm3oU+tL3SZW47t19dh9egZbOV0Uw5lVAf" +
                "xYzU/V24LNvfHalvtcH3d+1ZM7m/s0XD/V1fDPL66fkk/au+fX" +
                "B8RRk23aP19JM8961zH7zqMUw8h3r3WH3jifuL15TqpxfyPbj9" +
                "F+9b3D76Hk//qfdcpb1goP3dFys+dwvqHU/+I/4jkENbtronpA" +
                "wloIkfNkpR0Df9CeYL0Y56pUxUjMalTHUdxDA6RtNjmVGpV8oE" +
                "74iXeeNp7JSax9P8GR1P82ufn+7BxHOoj72knG3ZGPFHB5rh7q" +
                "mG2ubxsmyrzU/ep0rYVhhPvRk997WxH3A83YuJ51CPP1HOtmyM" +
                "3u8GGk/3VkPjj/fPttp4GpvV73iyrzN7T8zoeDqrzDrTppX7Xe" +
                "zFxHOo+zfLVGRbRor++x5Pe6uh8eddWlCLv1DpXXKHyO9WzlVu" +
                "GxOnIuyuTOu7Sdqed/7EfpKW34H5if2A3UnOVbb1pnKii18SsZ" +
                "8VcHScj7Pvw/k4+3GSbhcj5RXO8ZfOT24t4fFH7KfKOmp3+j7d" +
                "kr1Zt6iYvxtK53rMinJfvSfddm6/3APV4TVo2Wy9ITsbKvF3e0" +
                "P5Ue2M/HVpT2d/b2G7VMxfB6VtPOl+tJPE5O3T+4Ozn9bZLbNI" +
                "e1QdXoOWzdYbtrOhEn+dN5wf1c7IT38VGGZnLv5mmQDzJ6F09l" +
                "Purwt7Tzn7adJ3/i5RsCE6vAYtm623xM6GSvxJb0l+VDsjf0of" +
                "T945KuZPQem8n6m88VSwf5py+9V1eA1aNlvJ3pRTCfVRlpG3QS" +
                "0Tva/xxNsoUzWsp4AbqiMSdeOcDdWhvLwN5t+lJGb6pBLuz8WX" +
                "Ymwyd/10Vb37uwFP+qqeP3XqXo9r75VW9g1+uvF/dCGvcuh0sF" +
                "91TCae4/zkXw+Yqreqj/O78C/lWDg8HLVxQd5GP11f7NOtYb9T" +
                "b1wtk+fuI9D2xm2aOU/1eHVEom5c16G8vHHL/DRuj0kl3J+LL8" +
                "XaK9srIYe2bMUTUoYS0MSPioK+6U/1rdpRJipG41Kmug5iGD0e" +
                "z4tlRqVeVZ40sryCB9J5/U8gaS6z9zP/HWtfJ21/duPBSRcafT" +
                "jRILN18GiSshV+dI2uL9mHhf+3ETydi7wYktWMf8Q/Ajm0ZWvs" +
                "S1KGEtDEj4qCvulPMP+23Y4yUTEalzLVdRDD6BhNj2VGpV4pE7" +
                "wj3/gbHc7jY1+xrBfnlZq14Xdi/8B9cDJ6JwZ511T9nZg7WoXf" +
                "iR1iz1k9ZP3Uvqi+N6rop96M9lOvMa0XGU831zyexme0n8brHU" +
                "/tkfYI5NCWrbGvt0fWv4ZKQBM/Kip92PyJ+Wmr3S67MwOjcdcf" +
                "opiqg3eB0TEa6srfP5lRqVfKBO+Il7n7lm/Uu29pf3Um9y22aH" +
                "XsW7yNapn00zehjTJVw3auouqWQyTqxvm6gOpQXt5Gc10gMdMn" +
                "lXB/Lr465i2UieedC9Me2BCdCpiqRyWVdmgbCr6pQr8mF+Rtj1" +
                "bNp464rHEeby4fdB5X33eDXdHsavN4c3k987jCQfxXULQ0Opv2" +
                "U3ybkC4q109R+j6KXgb9FJ0ezSvXT9GSqPDvQ/J9F6XfcPRKxf" +
                "4M6KdocZLE/zWz7D6iubZ+ihZEQ6X6ZjhS/tbg7zHWBd9SMX8P" +
                "lM7ztFw0vt1t5/ar6/AatGy28al2NlRCfZRl5O9Px2u27sCaxP" +
                "z9UObfSXM8H42/5+yD/S6/pg6vQctmG2+zc6US6qMMo1XHZeI5" +
                "/h2h2QNM1aMSo58cq+D4joKToOOFZ0XHTc7A2z6eqvnUkXzM+y" +
                "T2U/RqE3W8NwTWeWNZ/bouGcOMFN81rVEvb45lY2PMRI3ZdVTF" +
                "VBtVvzlNjO3MykXz/ttn1NXN7LflTfM36qtdlqaNS7+2flptjx" +
                "SdM71x8f9b4jv7WGG8brp48fW4AzXW4+zcaftmPitGRLZWb26s" +
                "7qMfm5L99IZi7or+6+uN31zcXIy5rMnW2NbmYtCw4ZjLmsSpV7" +
                "SmdWqnM1F9oo73jBpP9aXyl634btOfzk29f9Wb3m6vaK9IdkMi" +
                "F/uiFfKT9NM2KUMJaOKHjVIU9E1/4l5Poh31mu3IDIzGBZ82Hc" +
                "QwOkbTY5lRqVfKBO+IWqTjNfuPu+iCGp+atw44Jzxb4P/NVS0q" +
                "X/8DzDLA5w==");
            
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
            final int compressedBytes = 2745;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNW1mMHFcVLUgGOzHGEWGRMVI+sLDZFPEB4gelurvaYFnI69" +
                "iOB4VA+AhCfGAJsMIHVdPVjKYlFCmOxOIvlACBOAp2ovCRjyws" +
                "RonAOLZCsMLMmDXEJI6XScZxMNSr26fufUtVdbmrW9RTvap7z3" +
                "33nFdT79XW43nREc+Lfpusv/CSJboh+kNSP+55sz/1tCV61rOW" +
                "6DHPsUTHLc+vPK9xyRtiiR4tRH9nelxs0RPZ3tGq/PHmuBV/Km" +
                "5k9kbsNTdU703czvFvStbPDHOc4qAQ/bTpGUx9vOFK9UxnZ1Hz" +
                "7d7/0RLfUoh+1jpOI1DvL9Gq6ngS3pkHgelx0lO3isEjoBm6zd" +
                "jOR6rnNBEX1rgrrbfRVi2zj8APX2MbR/bV7M3ab5N5dJ+NuPiL" +
                "cTMGuhRH4t+W05+73H7Ol8+qY/HnoiPxF13z+Mxhex6Pb7vyeX" +
                "y4peo8PnNokHk8/kIVDZ10Tuo8QMep7/vRTHrcOgezqHuT2Wtl" +
                "bo6H+tt7cJw6D3Z+ls16qzo/yW3587R+pETjOwqw+6dX9PeSv2" +
                "3nx+lxKsw3+PWuc6jzcHaGXUerqqc/D2/7k8D0OOmpcynPa2th" +
                "3Wasrb4qq93TxjJaVR3dCW/rTmB6nPRUWdoTJYpL89paWLebrV" +
                "pOEylqjXGXzOOP1Xu+BP8e3V1D+J5xsPmv06rq5q3ZHcitwPQ4" +
                "6Rl04azFKooz2FpYt5uxXGt+RFFPG1M87mafstGC83eqzBf8Z5" +
                "i/5fRXinht9uHYSkfrzTzu4ntttKhlkW/6S8Mqyz1ON7vZ43tG" +
                "+3wg5qdjDrUrB8rRv3/qnOP7gmRmvWYYXUX3BUn+FdY8fk1N9w" +
                "Uvd15x/HW2xPdnx+kZGy04n7aU+dpvGckI2OJmHw1bxro5fiA7" +
                "TidttKhlma/9ptEodrPXz+Z+/9T5zuzc8O+fZuf5uSX41zifW1" +
                "xsw7x/ap1unUYNm6zZi+RjHHuyDduEy6ycL1V+luOlX7bRc+ox" +
                "ejsdl/rIUmxmrKnNlVUq0bMbx7x/XzD9rdlkFp49W+N95ouDRM" +
                "2er+f902BsFdTfGNyIGjZZvWXkYw8iuego4u186d6Su51UomOS" +
                "Vyo1YxgTGpfyuGxWmVUq4R7JFul8uJ3Pp/gha87cXjCfbh/MV/" +
                "s8vt3NNH3HSFl3TMfZCHjNRotalvmClwcad0tVFbvZB2OrMO42" +
                "BZtQwyarfXewqX239CCSi45SDle+dO+Cu51UomOSV6lhTI/hXg" +
                "iNF/K4bFaZVSrhHiVlKkiejahOfVNUkvnpo+RjDyK56Cji7Xzp" +
                "3qK7XabYwiQvcrpiGBMaF/O4bFaZVSrhHgVT+c8tvU/Ue+a2rx" +
                "qmddXr3XBsjnF3U3ATathk9ZrkYw8iuego4u18qXLP3U4q0THJ" +
                "22tJTI/hXjB728vjslllVqmEe8QtGvvTeg9tU3QD/PA19nBk/y" +
                "mRv7fskXl0n4045uP9xbgZA12KI/HvsWPdnJq+/UWsOhbsCHag" +
                "hk1Wb5J87EEkFx1FvJ0v3TvnbieV6JjklUrNGMaExnN5XDarzC" +
                "qVcI8C63raPpON+RM1j/BXxvredwRsjY/Tqmoxj+8EpsdJD4+7" +
                "AY7TxXIVg0dAM3S72arlNBEdy/u9Sm+3/b4g/lO19wWZR70vuD" +
                "zW9wWX631fENwe3I4aNlm928jHHkRy6eyVKOLtfOmIvpbbyaxS" +
                "iY5JXqnUjGGM2ZnN5LJZZVaphHtEW/8SraoW4+7LwLIvEGmc9F" +
                "Qady+VfPko/X2UrYV1u9mq5TQRHcsdd18d/j2dHHftFeMcdy42" +
                "x+8LTg18l3yifQI1bLJ6XycfexDJRUcRb+dL95a720klOiZ5pV" +
                "IzhjGhcXkel80qs0ol3CO1zT2fvlHv+RScH+s8fr7eedy4Fu4W" +
                "89MdNmp9tdmbj5m+4LWRvAna7Wavn02dT67n4PbXrjjjceez6r" +
                "mhnoMLv0LE1tsml3rH/PTSsN9bkvPpmzWPu1fHOu5eLT9OcYV3" +
                "gq3LrcuoYZPV+zb52INILjqKeN7vzQBNlF9yt5NKdEzySqVmDG" +
                "PMzmwml80qs0ol3KOWcdfa2Bpk720C6w1OY2vBPLG1zBecGcn8" +
                "tNXNXj9b7rg7UPP907JxjjsXmz0/dSvd0zU+RquqeR7vvhWYHi" +
                "c9lZ6QXihXMXgENEO3m61aThMpwHZOZ8/I3VU2WsC3s8zXfvNI" +
                "xt1ON3vdbK1jrWOoYZPVO0Q+9iCSi44i3s6X/oVPu9tJJTomea" +
                "VSM4YxZmc2k8tmlVmlEu5RUk62tN+lkK3W3u8JgY1at1Q87+lZ" +
                "jJHwuozndiY3GFHcMVKX6SU2t8qCM+Zkns9u25gU4+566xyfLD" +
                "j/J8t8wX9HMu4m3ex1s7Webz2PGjZZvaPkYw8iuego4u186Yxx" +
                "tbudVKJjklcqNWMYY3ZmM7lsVplVKuEeqW3ufcFzNd+PvzHW+/" +
                "E36n0Obh1oHUANm6yZR8nHHkRy0VHE2/n03Ho7qcRVKKNUaupw" +
                "YZ1b8rhkTrulqYRjPK+5iLq52LqP2jcXlR3/gLB+zvukxS1NXN" +
                "YcabZlfx7GHM3FmSfzeRUz+bCVEbw3/UPsU0630sr342frHXdD" +
                "PjVU/X+pX4/0/dMucb1bbaMF7592ubJJq/uPkVzvdrnZZ37jjX" +
                "QRx2lNvZnb3x+mdfe942RzzOMHkzJBdWLRHvmSfelJYxk/KFoC" +
                "pRwiX99LeURb+Ps8E/127jKR5eV2mg6tFxm7U+OEiOX+TnAe1g" +
                "wfmD3PX0DtL+A52F9gr/hWs2B8u3Ha7pZmLPvzMM5Zxit9JruM" +
                "lTGct4i9/L1v7+/lbd3f79zvfWeeHud735mn6n3vG6wN1vqH1V" +
                "bV/mFlk6X2yaNsivUP0xY1xZPNOHl97f+LVT7wUUYqyi8xZAYO" +
                "XVIv2dCFaORgDWyDFXmQE7HcK/PYyK0/h9qfw/+9+nPsFb1NrO" +
                "6HdNvE9ZoRM5b9hHU/mDPu5pCru5Z93fcn9odtbp29u07XIWNo" +
                "7b7P7kX/yvEB7ZitCdb4p9RW1f4pZZOl9smjbIr1T9EWNcUHa5" +
                "CLcPL62rdWlQ98lJGK8ksMmYFDl9RLNnQhGjlYA9tgRR7kRCz3" +
                "yjw22IZPhr9spt8dVN1cim4Ij3le+GxzqbkUPqc84cnE/quKCP" +
                "9JkWH6hSm8kKIXIy+6uj/qlyNTtDK6vql9zVD50lYL4V/CRFf4" +
                "t/AF+IFFV0XpNTJakayrondF7w4fD58gXcgTPhMeJzv8Y7L+Of" +
                "O/2M+/lGm4Ns31zjT2SPh0eJTzICf3PIlx/O9FlL7ti94WXRes" +
                "C5Jzk+r02K2jklwNN6KGB5FcdJRyYL+1kfOlV9eN3E7yECY9Oo" +
                "eygQOTuZhbajSVoU+yr9DEKtEDmbtvrw/WJ3tpnfrWU0lybCYf" +
                "exDJRcUwinjab23m1qmmzdxOZk2vm9/TeWRRtsolMT2GMcnu4u" +
                "p8V/YVmmRW2WPuUbC+faZ9xp9P9ler2p9Xtvr1oT+v9smDXyNS" +
                "JFuIx28VgZPXn9fmp3m0BUP7TNhQrFSAITPFIQ/nohhlQ1ewWv" +
                "3ukLWxBrbBijzIiVjulTY/rUYO2jb2oW7sw/WusY+94mlqn369" +
                "01GZyW5pxrKfsLzrHenSr3eNffb1Dvklu7reSV4ZQ6u63rmV6d" +
                "c7738HvqSG");
            
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
            final int compressedBytes = 1939;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW91rHFUUv88VGxAffCj0b9DGhKYRd+eDPtS++VXwQRECKR" +
                "RKq/ii0MlkO5IlVPChKMYkz/Wj9avWPqj0I2lRq9RWhApSqGxo" +
                "0oba+qJV5+7ZM+fcOx/7dee2O8Nezjm/c3/nNyd37k42GyGCJS" +
                "GC5fh1XMRHsDn4MR6/EWLmllCO4LJIHcHXIuMILqYip4WIvhd9" +
                "HMHJQjTFHX2XkfVtYl3oRYM7jqM7HmzHGEXpCN/LmsnwOZWvKJ" +
                "fqYK18dZ1wYUyvznN5DvEWVU9mnnJPVV+DUQiwwJM2j8hc8jCL" +
                "o8DB+SAKPHwuxqGOPCmin3Ie4pil6qCr4NWzNCIPXCteHWZIjD" +
                "RjDCsL4azg6KxMjUD/nJXa4/H6mAcMD9XL8/UoxHg0+oHHVSw9" +
                "s7lSF4rq8piugaypRZ6Dr2y2jLt2SfE2J9dyoee95KIwfkwXXs" +
                "v0DT1y8JFO9qfptY73pmvutcoKjEKABZ60eUTmkodZHAUOzgdR" +
                "4OFzMQ515EkRsqgG4oipOugqePUsjcgD14pXhxkSI80Yw8rqgf" +
                "edELWtZleE96WweJiuVlmrrLlVHMECT9pxL5OIzIV89Hg+cLXe" +
                "9d4ADPkhTrnEI6Nw6sx0SgxxxFQu0iItuho9l3jC18HWc4lN91" +
                "Pr6SZaM432XQ73ifv0qI11kjV1q2f+J5I+3TbbJ+9Dq/ddR9Vq" +
                "T/bcJyfp0/r9tJ5qbpf51Y6yvI73p/XKOo1gyTOcdX3pyxFOFQ" +
                "eLo4ATq+ujBXHKJZ7kXdfXmXku4YipXKr+tDKyiAfr6hVJs+bf" +
                "rMQ7Eo5ggefukGO4lyIqLkeZgz7grT41MbQgTrnEk+jdoTPzXM" +
                "IRU7lU/Wll/JowF+vqFUmzzq7tbC+17qjZ2nbDO8YnVvenkqod" +
                "ONPqz+9JpYWczBs97jAvtFHwb1uN7FOBA5fi15WCPjXVBw+15f" +
                "wjF7kbTJJX3VLd4j6GI1jgSTtemUlE5qqe6gPeXM0tPuQHlHJx" +
                "NvDDiRHCSInMgihiKhdpIY38SigTeZBTzyU23c97Ho/vuxcN3w" +
                "knrd53hqu5a+5a5TcY4x2raYEnbR6RueRhFkeBg/NBFHj4XIxD" +
                "HXlShCyqgThiqg66Cl49SyPywLXi1WGGxEgzxrBynN3wJ/1JOc" +
                "qXEPJVaVQagEE/IQ6Z4IGPWOt9rqGPMh/5/MmM55IGnMSpMgIP" +
                "4BhFHVwJXAVhNIPXxShy0pXDXGTDs+BJnn4PftnwnfCp1fuu5G" +
                "r+seSO3NnBXbuzc6yb3HZ4+FZvyrrRlDF7BEd3xD+DMYrqme38" +
                "rJlpn+pgrXx1nXBhTK/Oc3kO8RZVVz4xXcXRWa3txpj0p98FTM" +
                "1s5/ORED2X4nkYcTqr0bmiujymVydrapHn4Cubre0nDfRcsMfw" +
                "jvG51f2p5GpsH99rWPnHVvtUcrXa/mQ9vWJY+VdW+1RCNb+Oo1" +
                "8PDmEMfMDUzHY+HwnRcymehxFnOx08plfnFs/BVzZbu4P+LjVz" +
                "p9e+p/8uNTUU/4SP9fOzDB8u3C0eSK2nwmrBhX7XFuvT3xl6Hu" +
                "yuTzP/sD4dsdqnIyX36RBaddM7xkdW96eSq+F6Cme9UXOfjwdX" +
                "Y+Un+tL1V5d9OlFyn2g9DXffp+DtAuUfWF1Phqs5m5xNNIIlz3" +
                "C2vlX6FFFxsMJ95APOWWl2U/kXmEs8uhKOcWZVqZrDMdJI1Tiz" +
                "qk1n5Wxp9tz1NGZ4PX1mdT0Zrubt8nbhiD549XGIUQQz6VRRzE" +
                "/zNa2j2fO4EhXjdblSPYcwpvFoXq10Vc7KldAV8Rn6c0G9am4f" +
                "b1Y7bnU9Ga+W9/3Muva3Uvn9zOlftVgX38/sU2W338/8OSOrj+" +
                "9nehPeBI7ot2LzEGORCfIoxi3IT/Op3Oo8rkTFeF2uVM8hjGmc" +
                "z6uVrspZVZ28cvZ95250N8rPn8ytW8nX+xFd7r/a1KL558z4vn" +
                "PM7k/RLzb3p+i8tf1pR3p/qr3T+/7knLe5P2VVS+9PtcMG1tNT" +
                "cbWf+vsZ1Nnn9P1xdTs7K78fBY7neDSC1fJCx8OMTNxTfcA5K8" +
                "3mNp+nK1E51Rx1nopzfS0sTPPp2nRWzpZm19bT+8laeNrs/uTP" +
                "dbQCnzH02eOc2d3JGXKGaAQLvPpzzlD9WR5RcRrBApyzogV2tI" +
                "K5NE9XonKqOeo8Fef6wIsaaT5dm87K2dLsuevpecOfLh8WFo+y" +
                "q7F9fLdh5YtW+2S4Wm1R/f+W2kJrPa7U95T4/y3XW9Xn7+3/t+" +
                "Szpfo+6o/SCBZ49b3+KGZk4XIM95EPOGel2U3rLOYSj66EY5xZ" +
                "VarmcIxpPJvm07XprJwtzZ77+VMH72Vdvd+ds3rflVyN9Wm/4T" +
                "4tW+3TsrU+vWr4N64bNvsUrdnqk39QDPBRtnrqU3RzkPsUXrLV" +
                "J++U2f3J7hH9aa1PS4O8nspWT8/j3mmDrFf7Zujy78HRbTvryd" +
                "3gbjDLbJrv3lZL+hQfhpW7Vvvk2ulT/FvPo4O8P5Wtnj0X3Bnk" +
                "PoVXbPWpOjbIfSpbPevTNgNPMXfvWZ+22erTm4O8nET0n6X3u2" +
                "E343tibpvvjgGeneUO2+xTcbUetPwPN1pcYQ==");
            
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
            final int compressedBytes = 1490;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXM2PFEUUH0M8qQE/IkaNcxoPGg6aKDsmZouebS6e1IP/x6" +
                "wgrKC72z1LhwnxaoyaeBFN1AsJJCzsFwsixEiWCFkxkV39Ewxe" +
                "nZpOzavuft1d71V1GWdTj9fv87e/rqqp6Z3QamVfC5+k//amel" +
                "OtwguzFf14VF2u21d1N3ssE566vS5Sv1uDrlseVZfrmKcu30vh" +
                "qdUaHquPjvrmlWfu++Sp6W4aT3OOke965WnXD0+9Tq+DzNdOzW" +
                "zvlEXh9Rpcdx2+lzifTjq+wzte59OOn/kUnelNI/vR5zX71Rfj" +
                "uzWN3sNpr/Npmu8lrbt2r91qxZ/l6rdr0LXLotJ6/FfyKJEnpN" +
                "viV6a/B8rMj6NxfTQujK/aC7dGcm207uZzcXeQ3FW04u2CZXP0" +
                "mz5mdf+WK70/5y2nbiBR6xPtF4f708DtSkgeN4kaLjnqts/buW" +
                "DotnLwsM/9qeluGk9fuj1nBnu88rTHF0/By63/8atp9AuTd4Lk" +
                "CeQdw2gvLu7ji3ttcUVPVXkXH8lb4ocqEbrcx8/asz78RttZn/" +
                "Q5n5be8bY/fev4hBx5PY9HfniKzgxXHD8vGHjlaeCNpw3HPM17" +
                "5WneG0+3HPN0xCtPR7zxtOWYpw+88tRwt8HNyT5+2zFP73vlyX" +
                "G3sB/2Qaaa/BnNp1/Dvooo+lMt6sN16terQvYYeRz2s9lhP49E" +
                "9+mVs0izMboPMEI3vXIWW76qXq1YPbfuzk7W3R3Hd/iY1/nkuJ" +
                "uYFbMgU03+jHj6TV6DJevXM1Mt9etVIXuMfFHFQl4eSbZmNiab" +
                "l/Xr+NIr6JbFqGPLV9WrFauXnjPvOd6fjnudTw13G2xP1t0fjn" +
                "n62CtPDXcb3JvwdN8xTye98tRwN3heMHT8F4uZWa88Oe+GPx8f" +
                "zafcXwptn4/PLFihJD4fx7oVn4/H7LkwmLAz/NPxHf6wwd1ip/" +
                "lu4joMKZWePF2Mw3JNrFDfBiXFG/1eFcXBUrbuCnG26+6E13V3" +
                "wmTdke7UXRhSpnqRp9Rfb8Os0jLzkdV8ukvzYt0gqroa+vluK9" +
                "xSUl3LH8lTalOW9FpdgU3X0vhivTHyJTxPR5L16X11pPkY8EF3" +
                "6JbvVeyqV9WRwG8k//W27ua8rrs51+sOf78r8mT9fnfU6/ud82" +
                "5l3y9wPZ8OPbBBGR2lzafkOef7+HkYUio9PG2WS+lhsY+fp3kx" +
                "9JYILsCQUunJC2a5lB52KCne+CU+Wrt1h+auGsbJdfe3z3381H" +
                "dV687BjtjY9z8P/eNzH0+ed/65ZROGlEoPnjHLNbFCfRuUFC+G" +
                "HqI4WMQVGFIqPWmb5ZpYoT6bpys0b3ygKoqDRVyGIaXSg+NmuS" +
                "ZWqM/m6TLNi6GHKA4WcQ2GlEoP3jLLNbFCfTZP12heDD1EcbCI" +
                "NRhSKj3pmOWaWKE+m6c1mjd+oyqKg0WswJBS6cGLZrkmVqjP5m" +
                "mF5sXQQxQHS9n5KTHgifK5xfLTFfH8FL/p+nOLy3Nm+Gw5T6nP" +
                "F09YtyJPFExiA4aUSg8OmuWaWKE+e91t0LwYeojiYBHLMKRUev" +
                "CaWa6JFeqzeVqmeTH0EMXBIlZhSKn05BWzXBMr1GfztErzxu9W" +
                "RXGwiHUYUio9eNss18QK9dk8rdO8GHqI4mARF2FIqfTkVbNcEy" +
                "vUZ/N0keaN36uK4mARl2BICbpZbub6B8yq12fzdInmTZGURXGw" +
                "iKswpATdLDdz/Rdm1euzebpK86ZIyqI4WMRPMKQEvRhXZxO7eG" +
                "RZTRpKilfsVkVxsLj8O0Iclp8zkwM+z5lLn5qcM+PD/9F5fH9J" +
                "3Ga5r6Hz+P4qnurwonP2HAwpQTfLzVx/X93DYt2do3kxJHYIsi" +
                "+75+PhPp7PGvUOrxsfE40nyvcOk4M+eUped91D3FQjCaVU11hc" +
                "nU18jUeW1aShpHhTJGVRHCzihhrJYSnVNRZXZxPbeGRZTRpKil" +
                "dsV0VxsPj6fkFi9X8JUd/vkinHz+n+BRq3oVk=");
            
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
            final int compressedBytes = 1787;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWkFoHUUYXtCDVMQePEgVe/HSUxG9SKB9u+4qCkkuVjQXpa" +
                "2ip2IMoimSbjYvmUsppRcPKnioQsVYiqIgRMVAvUjE0saIIOpB" +
                "PFRKAkLBgrv753///8/Mvjf7dt/mvbjDG+af//+/79vJ7OzuZD" +
                "0vvux58Xfp73MvPeL98Q9p/bXndMRfSbv1R0HcqlfxiL/s6v1e" +
                "77Epib+poqD1Kf2ymtpuucL+sDtHNZVlvDYlFRV8Rr+sprZbrr" +
                "AvdOeoprKM16akmgJtbu7Hln+5OtrCWwX9s2WR1OFy8XWo10bG" +
                "cX2KrzmsTxfz3iu29al9q9H16WK39Sle2/57/VbDfNroHZ1Mu6" +
                "HOv1L1r5mslJxPG/XOpvBceA7r1iVogZW1MQKjWpfQoj7eAgTE" +
                "o2ze1vO4Eukj2/Oi69wnY+gsiJ2U6lwmK0eVOsk25tOZSqvtJ9" +
                "6QHPUrqXN92u69svPPT37bZX2yRZVen1Zr/Fv8Xhnhn5Lr0+pg" +
                "5yuNk3pqlMdpIW5qnOo8svudf3uT69Og2QYzTtl8UkeanE/q6V" +
                "Edp9atJsepGpvDdf0Rt4Jf60OuhlU22xZf59l0n0/xzR7qXtyZ" +
                "dfzUPlclw3HdBTPDcr8rUtLMOM3f1UPdq/bnzPm7K7/f3dNV15" +
                "12JYVnubbD8+nE0MynEwOeT5Xe79SxYXm/U0cHiB31GifVY3Vs" +
                "fTA078EDVKIe73XdqZd6qNsYluuuVff+09nwbPA+1OlVnbfAyt" +
                "pQQ8liycIo7gUMG17W4rmSJyuQT0gyFrPRJ3XQWXB2OxfpJH2k" +
                "MvtJfrSbe79TL1e635Xcz1y6t6nngmCuj/vd3NDc7+aaGqecbW" +
                "j2J8sqscVXO5ui/czglBbnsJ8JOcPw/xZdfR5V4f8tQRzEVEML" +
                "rLy9zHukn2qKl6jYkm2epyuRmCJ3WeZJP9e37Vu24C1LbfL8JZ" +
                "qJXjSf1Ovl59N27xDsjye/uMwn9yM6HZ3GGm20sMYejKSSTHMv" +
                "Zel4EpvjSCXSx3m5Uj2GfMSu3ijiMlk5qtTJmQvvd2PeCB91q1" +
                "98pHAd983rbvHh/q+7xYcaXcf9mq+7tWiNamiBBTbv4X7q4y3M" +
                "wjb683eeN+15XIn0cV6uVI+R+sFSe4u4TFaOxpVw2xjz9zrvci" +
                "cts+/RUbnuFr4Y8HPmjc44zY/y+mS731U5oiRKqIYWWGDzHumn" +
                "WsYTKmXn455EicymeJ0nErG6UhnDfaRxaY+Jp2vTUTmaiV40n2" +
                "rftVFNzqelPbVfaQX3O39mmJ4zFw+Xu9/5M/Xe78Lz4fngGtTp" +
                "3TRvgZW1oYaSxZKFUdwLGDa8rMVzJU9WIJ+QZCxmo0/qoLPg7H" +
                "Yu0kn6SGX2k/xoV9t/cv2eLttXCVb6n03BSul9FSc29Xbf8zsa" +
                "xDhlx2NHmlyf3NjUBedxnwqmqIYWWGDznmAqeYdbZnzybq5xFn" +
                "vQT708G2N4JMeUMTJP+rl+sIhNakTv9mjOFqGZ6NoV8m9nJXyt" +
                "3vnU7OGivtQ6fig8hDXaYKlvoY96MJKK9GK8iSexZR5XIn2cly" +
                "vVY8hH7O3birhMVo4qdXJmbX160tsVx+ITTlHO3wxGm9Em1mij" +
                "hTX2YCSVZJp7KUvHy/8qd1Ce5NGZJQfXQXh6BOkDi9h0LpOVo3" +
                "IldEZp2Yq20lZe531bUKAtezCSivRSlo4nsWVeR7Hh47zqJ+6T" +
                "McjHNfovFHFxVojiqFIn2dWen1yP5r9X8Y87rfbH+9Wza8bJ6Y" +
                "sQ/9j/fpycvlfxjw5mnHp9J1b0Htz8OKmu32Hge7D6ue9xOuPt" +
                "isN/vr6oXX3dOX3H6jt/7Rquh+tYow2W/yD0UQ9GUpFejKd2+1" +
                "n0cqRwvf0M5XElEpPzcqV6DPmIPVNvaiQc8/wlE8+RGfp88g+O" +
                "9PvdwboRC/czH6h3P7PZ7zB09fb9TPfvMMKr4VWs0UYLa+zBSC" +
                "rSS1k6Xq78PnseVyJ9nJcr1WPIR+zqzyIuk5WjciV0RmkZD8fT" +
                "Vl7nfeNQoC17MJKK9FKWjpeP0/32vI5iw8d5EdMWQz5iV3uLuE" +
                "xWjsqV0BmlZTKcTFt5nfdNQkmfLf6CPurBSCrSi/EmnsSWeR3F" +
                "ho/zIqYthnzE3n6uiMtk5ahSJ7Mnwom0ldd53wQUaMsejKQivZ" +
                "Sl40lsmddRbPg4L2LaYshH7OpGEZfJylGlTs6sPYnsG9QdKPq4" +
                "yftd3WzBgeAA1dACC2zeI/1Uy3hCpez8HWELYylPVyIxZYzMk3" +
                "6uDyy1aeLp2nRUjmaiNzafGv0mtm62cCwcwxpttLDGHoykIr2U" +
                "pePl8+mmPY8rkT7Oy5XqMeQj9qXrRVwmK0flSuiMwrFB7hckm/" +
                "z9Lvyxyfe77mzu/zdP/k7gi4v/ADGoI7Q=");
            
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
            final int compressedBytes = 1764;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXM1rXFUUn41uNM3GgFKKNBQkoCBEVLpy7syzhtImSF24KB" +
                "RdFozJooVQxPlIZ14JBBT8aMGYXUFbKwouurCCYDeiUOmuCF1Y" +
                "2z+gK6O++87cnHPux5v3cd9Nk/fInfP5O793cu+bmzdDGg1+dN" +
                "YbBY7ecv7Y9h+NgEfd1TrP1oJ6t9EYblVCeFgsfvDYbu1T+0LI" +
                "PlWr5rtPeded7FPz65B9qlZtJ+dT83LQPl3etX26GrRPV3drn4" +
                "b/hezT8N9dO5+uBJ1PV7xfwy/Jz83k5wfoU+f3ZLxhibttsf1o" +
                "RbxlWH5OmH9VieX1TO+vRp8s1To/bUu/+ZtP4ojf+VQRoeB88s" +
                "k+8P7pXtD9071QfYreM73diVwYt3SM7mRVXr2nsrzdJ3SLjT1h" +
                "6HHdRe9X77rCCL/ufLA31vJ9NYr76u9gsPU24JVHjtN1K9ioNW" +
                "Z27rOz632ZVZfadA4odTdpjC2i1Hxa9DCfFndsPi0Guz8teejT" +
                "0o71aamGdbehRrGh+iQ20KpHjtPNzM5dhafnSTv32dglvB5m1a" +
                "U2vTqvRTFtTMPvC5or/vZPzZXC+/GVXbt/uhR0/3TJ85o7Jo61" +
                "HlcjSKBJuX8RLTKWa1wHv3xVeApfegGPZyu7PJUFfchERsX7qY" +
                "9jIRfkSK9ESd1NVQcwkamJpuvp65YaxVbnn1H/tqTevyjY01qh" +
                "Pbu163REj7A+95V2lw8xxVY8nVWX2vTqKCX7gi2KaWeac36tm1" +
                "KZbMtK+KwR8PBdTQyT88H2CBJoiRw/h5Y0lmtMB3/6OsLbxh+m" +
                "NsQeavYHMlb3IRMZ1TvNfAwLuRCO9EpGUjKfVJ0UE5la0DTd6N" +
                "sFUyrQ9Yyc9udB51PN1cTHplQm2+K7GbJPdVSL1tQYram/76I1" +
                "0MHHI8fpdESPHot2lw8xx/GgNr06lWiM+rGjjf09fGRKGU87ll" +
                "3ZnmfH9Xoy4tkCOzjHc9/2DS3udqOxqq36Is99K+5UCz731dmn" +
                "UcZz3/OnyvLpPg+vranWlNw/+ZsTEq/8Eb9QvVp3s8K9aRANcA" +
                "RJnq2J1kQ0iF9EC/fTTJDAT1GVBLLE49kYj5EUk8fwPO6n/EDD" +
                "ahgLfeJ1XWgmun0+PVrH4JnC8+8lz3+hfqfp37C74bc5EL7PeW" +
                "e1YHVyf2qrsjvXylfLczVOvCkxhSNIoIFOLdyPI49HVMxOf8Mv" +
                "q1jM05lwTB7D87if8gNt8LSJp3PTUSmarmfPp1wzMu98Wq0yn7" +
                "YzrpWvVml/f7h9WI1KV5oalUVF4sm9mKXjpfPpVXseZcJ9tC5l" +
                "qsegD6vHr7hqmVUpKmWCV5ScC+2FRErH1LYAJ8jcoiLx5F7M0v" +
                "HSd40v7HnbjA0fraswbTHow+pYTa9lVqWolAleUXtBnBVnk1k6" +
                "GkECDXRq4X459pbNeJWDEsirf6lYxGErRfNRZMQ0Y6gPOcavmX" +
                "g6Nx2Vohn6GXEmkUYjSKCBTi3cjyOPVzkocZnmsT5pPoqMmGYM" +
                "9SHHuGni6dx0VIpmood4Ph7+iD70F5X2ak7M4QgSaKBTC/fjyO" +
                "MRFbOpTPN0JhyTx/A87qf8QIvOmXg6t6RP51xoJvrenE9xru/1" +
                "xG+U3p+v75F11/EXlc6pQ+IQjiCBBjq1cD+OPB5RMZvKNE9nwj" +
                "F5DM/jfsoPtKhr4unckj51XWgm+h6dTx/4i5JHf77fYvpcJXaO" +
                "v+v7R92+nDzbWd7zn+RlUiZK9al/pN80+xQfL3E1kbtP1Y7sPv" +
                "WN+/Eg17ew+6/nngEH4US90nw66LbXg+zy5quWn1M0DSfqla5m" +
                "2m2vB9nlzVctPyf3/Sle8HdXrXvdWXZG837fF9z3p1K/9cmM+/" +
                "hkfX0y70/5qpXnhPsC8WTx7O7RxiNylGGfiTcrZnEEaaTtE7Mq" +
                "wuqf5Tr4KSpmU5nm6Uw4Jo/hedxP+Y18+0w8nZuOStFMdJ/7zK" +
                "z51P4z5Hyqu1r3eG3M7wTtk+dqYl7M4wiSPFszrRkxLz8PVhYp" +
                "SRtqNB6wWjPyFaIQX8qAx7PFvM6EY9KY+E2ex/2UP2g6U3nC55" +
                "zgNa+fo5no9vnUOtA6kLB7i3ulLesAvy0K8MofvdPF4m3V8HPz" +
                "Mlxc38MQ2if6u+t7GMLyfQTzexirn1bvkxFXsU/t5ZB9slXz8X" +
                "2V5t/8VUlyRBuPyMIxNZesLONxaQzlZctE9m5+Es9d1e5r3uGv" +
                "1M5tzcx3Erd3XF5z7DsUjaG8bJlgMz3UIvGy+HJfkXXXf7f8uo" +
                "vfDrnubP83xFx3/XfquT8Ztkf2Ph6fytMnH+93vueTOBn0/e5k" +
                "qPkkTvh9v9Pxau7TiZ3cF4xbd9F+d5/AF6pPtmpmnwpw+h8CNP" +
                "v/");
            
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
            final int compressedBytes = 2139;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2IVWUYPhAt2lYLqYVGbVqo21yE3nPOgHt1nKAyoYUiEk" +
                "yrmsTuvTN3uES4chEUFIiNEwhBYRnqVGoWlIUwFIMDQT/7QGgh" +
                "2Tnfe9/7vO/7fef+neNx9Hz4+f4+z3O+Oefc7965GkXN76KoeS" +
                "37cy7Kjubm5s/ZvBJFjWuROpqrUbTwnoldigJH84YXuRyVPJpf" +
                "Dcz+aCNWvav6um9dp787r4zK37nc+bJzrnmtc4nWqbPC6zTJMf" +
                "WYw/jGY/mWc5MenYHr1LkQVlJYf3GUqkFHa6bM2Uw9Olmu+mM0" +
                "ttE1NdqNNmayyCNfRnQeM+qntkhUdDtNW7gWfVaJxtQ1uk/npT" +
                "7ywKY1Sm2sKYTmo5e5ntqzo9fGL9d5Pd1tts4vfaY9FSvfU+s6" +
                "Bdi6h6rDbx3sX+c/BZheGw9t/lhB/M27vU4h9VUezc19pu8D6/" +
                "T6ZOvUOlx2nVpHx1ynkPrZEvdZ4b6gsToxZnBfUPJ5MHhf8Je3" +
                "TqvD9wWdP6q4nqp+jtd7LPxZ+Z1WsB/36gI/Ibsf37W0UfbjpG" +
                "TYfnz+94lfJw76VgWvPgfrRqhSffn7zvspnil4Ch8u/Tp8tBol" +
                "E/+cDsQHMJNFXveNfJYRmUdMWlQPm/PStn1Sic5JXqnU1mj95M" +
                "03i7h8Vq3MYpOfrqVrUURzfuQWezxzhCsxdBZdFs9Zy+E+Pvyc" +
                "5GXMUA1yQuNyEZfPKlGlEpyR7OjddydKvXr/U5xLl+p8vauaLZ" +
                "6L5zCTRR75MqLzmHU9UNHtlH/MteizSjSmrtF9Oi/1kQc2rVFq" +
                "s6gSzUc3O+VPogfi2PVfdVVurbbH2zGTRR75MqLzmHU9UNHt3j" +
                "U0uRZ9VonG1DW6T+elPvIWN/l4VptFlWg+unk+3VbPm2cqvMOv" +
                "l3ryrZZna31U4nm3Nd3KM/vs8cwRrsTQWXRZPKf8t3CfVKJzkl" +
                "cqtTXIgR1slstnlahSCc4oG9vSbZnlZhfbRoPseF1GuBJDZ7nL" +
                "x3PrtB7u6yv2cpKXuoHnq2Bm8sCG2vxspDZ4kslic02V7+960e" +
                "D7u+58ne/vFp8a5f3dvXofPHCdFmpdp+1Vr1N2TZ/iOT7VmucY" +
                "+ZTTlcN8iYeMrNVxy+JjDtMhY1KDzyUx/doBKnbEOzCT1fNOxj" +
                "u676iIzu/QvrNPSlS2etmTXIs+q0Rjypr2Ed2n81JfL3fSx6PX" +
                "O81bhOb5O+OdmdWbySKPfBnRecy6nntgkd19l2vRp9bJ5CQyMP" +
                "0amYPGbP/k4VltFlWi+ehmj1HqabtxjvjIRv48c+Bz/EStz/FN" +
                "d/f1bvEhXqf4rfHXiXo2wue+Vn3ZdUp3pbt4Zp+8+HmKIcKVGD" +
                "rL9T6extZ9UonOSV6p1NYgB/ZcfZjLZ5WoWif8eCaeydakN5NF" +
                "HvkyovOYdT33wCI7fYlr0aeuAJOTyMD0a2QOGsGmNUptFlWi+e" +
                "iD3gdX+XuptNbvF1TNlu5L9/HMPns8c4QrMXQWXRbPWbvDfVKJ" +
                "zkleqdTWICc07i7i8lklqlSCM8rG/nR/ZrnZxfbTIFtHuBKjPS" +
                "uz6LJ47ur9An2axzJrDqkDeLYC+sgDm+WSrFQlUaUSnFE2ptPp" +
                "zHKzi03TIFtHuBJDZ9Fl8dw+88NwX3+dvJzkZcxQDXJgX3y4iM" +
                "tnlahSCc4oG420kVludrEGDbJ1hCsxsutJZNFl8dzPbgV9mscy" +
                "a47cj1cYM1SDHNjBZrkka/eURZVKcEZpY+rW1K0oojk/cos9nj" +
                "nClRg6iy6L59bpfLiPDz8HPz7PmKEa5MAONsvls0pUqQRnNHUr" +
                "uZpcjSKa8yO32OOZI1yJ0Z6VWXRZPLdOl9GneTSzHXkmvsyYtl" +
                "9yS41gs1w+q0S1Svr+leRKZrnZxa7QIFtHuBJDZ9Fl8dw1vhTu" +
                "6yu+Eh5SB/B8FcxM3uKmIi6fVaJaJX5HFd8v6O2qgt+iS16sc/" +
                "80mK3de1fTXZ4Y/7PJcsPzU7+WVzB6D7NNglb++yrJsSF6jxVX" +
                "Deut+Ho6Nkp2HE21fa7y+YP0ucrdW6fkhTrXKcRm12n+3424Tv" +
                "GFEp9OXhj786cLo1xPi09uwPvuYq333Q/37X23t9b7bu9I19Oz" +
                "1a+TY18YvE6UTxb8dUoWklp/zxliwzpxdnJN/H261vHF5+6f36" +
                "68/YT3e6PK/2VIbc+nlTqvp9aNqp9PBn/g94nb7w95x/KBu5rn" +
                "gvfsXK37zLlRspNrao31vetxPh9PZmpdp5HYup+OjHcnucMz++" +
                "zxzJFsPs6eiEnrOKoknrOOh/ukEp2Dr5XaGuT4j1RquVgnNElU" +
                "qQTrkP9d1/MpfbjO51OIrczzKbmd3PZ9imGWPkfyuT2b/y3jYV" +
                "QX28MZ7g9zA9+vkMisC2qUxj36HEKKLHJRLO9NozRffTdHPYu9" +
                "NIqXZCT300iOyFhUT1USL3sfsSR7NY9l9jmiPmaoJhJ1UOLjxU" +
                "uR0BkFUSMPm9EL/5+HQ9XedyV3L+P+Pw+HKr7v1pI136dYshaf" +
                "Jlv68LgeFuXj0xY1j3EvM/g1QOI8aqhb6oUuG2UNViVjAA0RdG" +
                "pF/e71ZD2z3Oxi6zTI1hGuxMieTyKLLovnrt5H0Kd5LLPmkDqA" +
                "ZyugjzywWS6fVaJKJTijbNxMbmaWm13sJo1sb7FKMUS4EkNnud" +
                "7H09i6r6/Yy0lexgzVIAf2xVeLuHxWiap1SmZzpQ3YZyZD9qBJ" +
                "xf82cIynxxmroPH4KH2jVTnks8lZntknr7GNYogkZ7t/s4eYtK" +
                "ge9sIMZyVScnZhGn1SSWgQolQqeSW39HL1vkaJY89fM8kerbPX" +
                "1/9NTePp4lzBmi/fs+tpebj6++Xzgpr3BTtH2ReEqgqO/wEZUf" +
                "Rh");
            
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
            final int compressedBytes = 2111;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW12IVVUUvooWPZgvI0iUQSDmQ4EggSA654/o0ZfoSXoNew" +
                "gfeu52da5zC0cCf9LQsqboZ6gcsYegaWBKI8QgCAaCqEgT84ov" +
                "QY1pe58166yfvc/9mbvn4PVs7r57rfWtb31nu++++56mZCqZqt" +
                "WSvLeXHYEVPQY+8iRT41fRIh8fAZ7G+5/DKGdKpvY/S3l4SWZZ" +
                "A+OIkmhfzKrX2PE/OQ9V5ZlaCWHkVX+0dlde43/1h49Gw6HslW" +
                "5MN8I7t7lP2zqKDBQnVn89YOSZrhaMax5ZjV5aQxSXqRTzFHPm" +
                "8rkxbXO62YzyPvdthgZj6UEkNRmlLM0nuWVeocqJ8brI6cNQjK" +
                "pHO8pq8aqA4qxSJ7PXpGvEHOY2+Kjnto4SAzFpVskPjC4GPRQv" +
                "x3Bd2pt/Tq+XqRSf5jZn9qnF7GQumTM7Vd7ne9YcNBhLDyKpyS" +
                "hlaT7JLfOKfXzO37gO4nNVYGWwoqSsFq8KKM6qlRT2bDJrRnmf" +
                "+2ah1WrZi+AjDyKpySjiXT7JLfOKeZr1N2BETp3Pa3ONVr2/ll" +
                "uVs2olhX05uWxGeZ/7LkODsfQgkpqMUpbmy9f4LX9eodiJ8brI" +
                "6cNQjKo3V5fVcqtyVq6E7si0K8kVM8r73HcFGoylB5HUGnt5lL" +
                "I0nx3FLFfW0ZVlDa6D+DSC9IEV18pquVU5K1dCd8Qz4Gq+tFwn" +
                "oOi/Ks9boauN3hy9ST2MwAKbe2SceoknVsrOld9CLOVpJZJTYm" +
                "SejHN9YFE1qZFr06ycTdvR1miruYfFHkZggc09Mk69xGMOjeSY" +
                "54kVoGKCuS3rSS6pfzHWdvm0Np6p2Vz2+nnzumBeX8DvlvoPpv" +
                "/a7Lt31C+an8zvtTeVb8a3Rus/Op65Qdd9/cuO0YvOyei2B1V8" +
                "z9Uvwfv+o30oKJmn1gp3npzcu3aemud6mae+FJTMU9Ye5nnS6v" +
                "3zNPb8UvW8eqOoFNWG+Fpu9Wyedg71PAVWH22JtlAPI7Cytu25" +
                "R8aphxHEOavZ41ZRlLCUp5VITomReTLO9YFl1Wus1OaycjZtJz" +
                "cSs4Kgz8+eN6CZe7wPfORBJDUZRbzLJ7llXnEydmK8LnL6MBSj" +
                "6q3VZbXcqpxV6mR2OzE7HvS5rw3NrKcUfORBJDUZRbzLJ7llXq" +
                "HYifG6yOnDUIyqW/X+Wm5Vzip18spqf3qq+GZthv2Etx6ocn9q" +
                "rl9e/vrJYidMAit/rdJ9PFkW1plsxvb0Mv0OicAXQ8xQrBNzLz" +
                "jJ6UNKHuTVWUVsB2VINuLhdy55sPWynuJ9bnRs2wDr6fUq15NP" +
                "/UAraXe2e/QP7GEElh2b38qFx2KlJW2I57+vF/mQH3gIi9notw" +
                "09FCMlFgVejEku0kIa+Z0QEnmQU2OJTdv5fU1iPzqJ+/joZPMN" +
                "20OseAoxqZ7KeG3tBR/3Zr9zv67SndNfwY/nI47xIfo4j9P33e" +
                "HA33cPDvP3XTqSjsTXoTef6nwElh1zj8WShSgeBQ7OB17g4bno" +
                "hzq2kYdGVAPjGJM66C54dZ9G5IF7xbtDhI2RZvRhZfm8oLEWnx" +
                "c4O/yAzwuy45U+Lzhe1XOV4PN0otJ5OhF6nszqW8A+XsC/w4gX" +
                "rN1aCzGJ7GbzniIaS/6yGHHGC409nepyn67OsRyDLz/bYOupeX" +
                "rp66k1UunzzPW9rKfm230/Xzki37lf+jTCz9NPBKKd4xrDdfky" +
                "wedGuMfyddLr5T0s37lf+qKO54XyaLe8qOs5hGO4Ll8m+NwI91" +
                "i+Tnp1LL6GfXyt2J+ukVcju9n+TI0lf1mMOLvV5T5dnWM5hng7" +
                "Ve+2PyUjidlPWuvCfd8lle5PvmrDcS6ILw4yT42X+5snX7VBzw" +
                "WCq6+/z2zs7eMX91uVPn9a5mrpYT5qPRKONf4+jK4ez4Seaq2H" +
                "A87T4ndh+k56JCxrdiyErp7X07FlXk9H3dHgnzvLlZ0Ko6vHeT" +
                "oVfm4aH+b9FN+fGovPZBqfFKj3TPSfUo7pxfd3C8+njY+KXe+3" +
                "xgfZ6ZLMz/L+XJd98+8O+j9+5aHF0Rnzej+fp9OB5ubzxtnB9/" +
                "Gen2q9YJSfH0jvV32up/OBP2eH0kPYo41Weiie5h5EUpNRzHL5" +
                "8p112p/HlcgYrwvZxOeqwMpgUTXCxtN0x+79Y7bm1hl6PaXfhD" +
                "0XVHv1or6v56Pzap4mikrf1ob4Cq++7Dyefud5rvLL0s/jA6rs" +
                "83eLVu8/j+/7dfDzeHphqD93F6paT61NYX/fVbuemneqWk/Rtn" +
                "D/FvZcUO0VUr3g/Vm+c7/0aYSfp58IRDvHNYbr8mWCz41wj+Xr" +
                "pJfHsoPZQezRRgt79CCSmoxSluaT3DKPK5ExXpcr1RiKUfXxp8" +
                "tq8aqtxzWr1MkrV3EezzU9UeXnLvR/D84msgns0bYt2ZRsyiZa" +
                "T5LHjqyPNxm1cUJxvloN+Hx5XImMkW2+WffwmMTQXeCLK9W1UK" +
                "e+f8zW3Hx23PWUbEg2uLPq87lxf2a33LBX52pL0VLZc99Vg9z3" +
                "2M7+zgW+aiGf+449U7snrgMrekKtXPL56eSynWj+rfT81FO1A/" +
                "cPfs4MfcUrq5yn0NXS7el27NFGC3v0IJKajKbbs3WE4nz5t8Y6" +
                "fx5XImO8LleqMRSj6lRN1+JVAcVZuRKaB9Pm03kzyvvcNw8Nxt" +
                "KDSGoySlmaT3LLvGKenBivi5w+DMWoenS2rBavCijOKnXyylV9" +
                "7ir+fTcTDuXdnybukXk6Ew7VeZ7iJfy9d6ecVqX/P9+B24H38V" +
                "3pLuzRBiu6BD7yIJKajCLe5ZPcMo8rkTFelyvVGIpRdaveX8ut" +
                "ylmlTlb5f4W/lD8=");
            
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
            final int compressedBytes = 1592;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWk2IHEUUbvGkxIOXoAEJOeWygZy8ul0z4y8anBViEIzkaB" +
                "AkYY8LmZ3e3Ukn4iWs5CIGjevOrvFvjQeVdTeCIoiC4MGbexM2" +
                "u7dBIVnt6trq9+qnu6unqtvuVDO1Ve999d7Xb179dO+0p9pTnt" +
                "eOa1poi/X8n5gMJBwJl6jleNWeaFscx4uqw365TR0GdOCdstf7" +
                "Ur1iqyJP1O+2u1ErrmNZl11RnP5gMpBwJFyiluNVe6JtcVzCWN" +
                "Fhv9ymDgM68E7Z632pXrFVkSf23NmFUYMjnV3aZzKocV/WwvjO" +
                "7uQqtD1toXjRmqgTcViiYsDGhUMyjjMBlmmMsop6j0mkjnoWZX" +
                "LFq0lxzSQ4EbSE/jNlsA6es7bQLob373eH4nEKngp8N3EKOulx" +
                "mrxTXpyCp5V8MvIWPPn/xKnzcHqc0nTlxMnMmzmnzoHOAahZi/" +
                "VYH0uwHmS4xUfxNtfjtjwOMxF12C9mKmNE/qzn35fmC3tlKJGZ" +
                "bJuP6P0QfX6MPl9RWe9w79eo/k6NaO93jWxdF/veb4rke88L2z" +
                "b51Ps6U/uzsh+9pEFtJK1fbNfL2WXcI1vu1nI7W0VH6/Au7sbf" +
                "Ev9iuSjzt0zsFNEwrb+VzxEwmJduJJOpGiyh9rL4iroi827uav" +
                "a8I2+mzzu7UnTeMSZ5827unQIMXMZpujZxmjaJ0/xr5cQpbx0n" +
                "52sTp/Mlr+MvW6225+ry3OKeSVo+DZ53fC44acNy/oli+bTwiP" +
                "t86sdngf7HLE77sutkMf6bvAHofxBp/0m18UX8LS72308kn/SH" +
                "Ca+t/hKzpxn5aVzfzPk2Rxn8Vy4c2m99Hn0+ZEzcZFH/s/6aTj" +
                "5I3s2QaM0Ony2Y7VfH0xnwPeuOiYsymMK98DGHK8aXla5PGm+z" +
                "1xycM6+If7FclMkIvZ0iGqbN1ssYzEs3kslUDZZQe1l8RV3aOk" +
                "5mxjgXzNTmXDBjso4HfxrPs1OSrbdLm3drlc67tXLmXRKnw7zV" +
                "ftxgda3NeUkuYbdc+xAnQrwGF/fs6fqki5PRbn3O7JwZM9+0uO" +
                "vN4K8sfbBj4k2zPt22z6fx45Ryr7cs4nSr/BHjPreEodvnlmr3" +
                "u7lTbvc7Zf97vax8ar1i9Xz+RjG8nbci+eQ/WF4+hZfKzieZve" +
                "t8ImfUloPd50zVFlyy1+4cK6XNuxerPBeU7U08jzuNU7fSOJXs" +
                "bSDcOVmVsnk1J9tX01BkNW+siWU7PMh4a3xO/obVW4cNrybFjM" +
                "n4fOGXPoPpKN5D6RsZ5nyLwzQUGeaNNbGcVvj7zGw8yHjLnFN7" +
                "oj3Ba97nvfYEWcIS2uc9kOEWwzMUthcxWsJjRT+yZ9UHHY91Ig" +
                "bugn8wU8Cy9wWcZxwnxSpmAnHAPPfPcy9490TxX3WHsn++q3Gc" +
                "HnWHynm+e6vJz3fhZZPzuIvnFgXXqDgNFqqKk/9Ak+Mks7ePk3" +
                "SOvam26l9Urv5Bo/XpoIN8OtbofDpWbj7BfucfafR+55x9Ves4" +
                "Wbc4ja8X/v/dukk+DRYdzLvjjZ53x03iVOQ93b15Lrh42yRO4R" +
                "VT/63TrdO85n3e4zWXcCRcohZGyfbiJ+wb+nGYie7CPMCeykLU" +
                "gTfZF7apjpSZAKaqfPKXK513y009j/vDSuM0bOr6FF6vMk4mvz" +
                "sM/i6aT27fF+j/bx5azbui/zc3+33m4F33+aQda3gmivPpRn3y" +
                "qcxzpm2cyHyVcdJ5qyZOtutT62SVcdJ508y7j+qXT533qoyTzl" +
                "sz8qna83jH6PeZOlTGU+aI12TE9zsyov2Ll8hIReb1cQ0aMtL7" +
                "Zlc2OzISfz+uw3OZ7B1as9cwhn/01szzKfymyfm08G9V50zyUJ" +
                "PjJLMv77kl/LbR7wtOuM6nKPZ7vCZ7yfq0B1IZmdfXj5SxIE/T" +
                "gc08v1gme8dYjAG7Wd6r3+/m7laZT+Gm23xqzbfmyQ6ro8jGLd" +
                "aj7Wi/SyQUCz2OwlpmA9tjUqpl9kQ090MvkMgXHUd2+mfxOJEH" +
                "3AX2rnKM9rsd4An3yxFUB5y5bL+/3domd1gdjYtbrEfbTBIht1" +
                "kNeibDfaaP8zixx1DMDljCf6mcXlwCOmBCUcBB9Ms9MxlwxHcC" +
                "SG6H2xSxVAecRevefy0+VfU=");
            
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
            final int compressedBytes = 2404;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXE2IHMcVng0EQULQOmTXJkHxaklALMrFC8EQm2S6e/DVBy" +
                "fkktg+JtZJAh8Ettk/787MIaBLyF5s5SpjLbaxMYIkEglJiEJW" +
                "CklsglmCfNBNOggUB8Smq1+//r5XVb07Pz1jbRdT836/93VNd3" +
                "VN986mn6afJnfTom+1RBLNyevbrm+1nMX18ItN9VaxCYaiiKx+" +
                "wbPZandNLfCBiYvqvcU+iwUu4Mh7otLqRa0jmGCKyuBs0bPlbL" +
                "nVkt5tTnItPZ4ez5a7fVicBA02lgQhxCuqHY/n6Rb6uK5ixmLg" +
                "Q3VUQ+zqReBwVUZlJtgjzpBt5Rfyvvra1oetI7ytnp8s/soz5T" +
                "k0m862WuvbzSE7vNG33s741eR4Gnlk/pi//pS/Pii0x1d28/53" +
                "kbh/Rmy/jSL+PbD8Pt/T34zF8sqB3r/6ls3HIlFXK+lvD+84JT" +
                "PTHKdYtaMxTu3PpjlOsWrjjFP75fbL6EUSTXS2WL/r186G8UBF" +
                "NsvIRrxfh1txdFxhpjaG9wIckyshns8tnwveqUML0euOp/bu+M" +
                "fT+is4nnhbPz/x42l3kONp48ujHt/ZF0Kpfls7Ozhy98Q01wXZ" +
                "HxpfCUxpfsqOTXN+ilUbZ35KH6QPQj3vF/L2wK3HRS99C/KOPP" +
                "euCJV/wUctbAscL++x2sDniLWXfL7Ki9koU+EAXZCwfhIu/kjU" +
                "2VxuNpPlV1Dpi89hRlp6Kj0lNrU4ydm4Wa/zI4rx8lqnOJfzqi" +
                "Mg8EFXP/BCFhIHJnW1lCc0RmUmmlNk7KV7+V4UfTF2e2VbzNue" +
                "W4+rpZAWKz9sLC0iivHy90XO5bzqswt80PMr01X22RjljxczRa" +
                "wcT8oTVRnVjMEe9GBt/12Vtv48/CzS/vmk5uXe9c+XSXo5vay9" +
                "6qK1/yU2WDQSzXo1HvL6K+plpPTy+nnkMZNYE0Rm6vOI+Rz7kC" +
                "Pj+PtvK3GO5VnmXao+kb/U+2rGPPDr+imwN/xtXisfxL7Zbau6" +
                "Ym7l64Peu80hJx9Mc/006WpbNyrpZrPrzOluvX80fMReS69pr7" +
                "poybbYYNFINOtNryXbiGK84hPejucxk1gTRGbq84AP1VHNrxVW" +
                "ZVSfSaXfTm8nv5Y+35dCEs3J0kvL429D0yj2CkYMz0mca+u4Jv" +
                "lAsrGarT7LQ2szR67AtcAT/MDSvWx91Ve8GWjFzOyDzE8r7w04" +
                "Y7wfyX176O8vO6NXa3R+qr7FJa+OMHu++rDMT5NmsvH0pJDTqT" +
                "6XGKxa7+ORj6d/DxM9zPUuuTrG0XF18hlDjtMnkxqn9KdTPZ4a" +
                "rtZ+ov0EepFEE50t1u/6tbNhPFCRXTB/VmOB4zNhHyNbpjaGfe" +
                "CIanafmJuPymghet19ut5n4X26jV+Nfp9uzLuJQ96n6/13oPu+" +
                "vxz/fmb7J83ez5zuOPns4+P0+gsDn8d307vaq65aeje5xBanqw" +
                "YbSxIvUYyXz6uXONfW8SuHNVw++2wM9kJfzBSxySWtk5Tfl0NU" +
                "ZoJxYJ7lkVjdt+n9r3WEt9h5N87Web7zPHqRRBOdLdaP3sYDFd" +
                "ksc57PxGLaGJtn/cxPtPaLIZ7PLT87X6xDC/QXOvk5qr1IoonO" +
                "FutHb+M1B5KVOc+Mk+djZGCGMewDx62PQzyfW37UPahD8/VsLp" +
                "trtaQv7pnPScvH+rTYYNFINOvVeMgbP1YvI2VzGz9CXnVHP8Dk" +
                "uooZi4EP1R37kCNwuCqjWp6k72b59U36wrYrLR+nBbHBopFo1q" +
                "vxkPNx2g2xs918nHbZ49fxa6hffTYGPlR37EOOwOGqjGp5kn4m" +
                "O5NLRV/YzkgT2Vo0Es16keXjWWybVzEOfFxXMWMx8KF6++m6Wl" +
                "xVohjV8iR9M9vMpaIvbJvSWq3+rNhg0Ug069X4EM9i27xqnAIf" +
                "11XMWAx8qL75fl2tsCqjWp7QkxPJiXwdUfYiidZ/JCn+IgAW60" +
                "cvkvjLdckJleFFLPLMN1fPx8jADGPYB469L4Z4PjcfldFCdG/N" +
                "+nh1fekc5fVT0+yTNEnRiyRa5weuZ4v1oxdJ/IyKbJY5z2diMW" +
                "2MzbN+5ieaY+/H+tx8VEYL0Yf5e5XusaPy/a7z2iDf72JRw34P" +
                "7i9GxulLR2WcNvcHGachzrvZZDY9pr1Iojm524fFxVrN6uIvvj" +
                "eWeIrvvIJns9XumlrgAxMXtfYS+ywWuIAj74lKqxe1jmCCaYjm" +
                "68X7fe2T+zqPJ/dhpVG9741yVI9n+rGw1/mAeVhdtvnVOZZjgH" +
                "tQdd26X/HuF7w5qfu+0acWb0zqetefa/j6udxZRi+SaKKzxfpd" +
                "v3Y2jAcqsllGdmfZZ8I+RrZMbQz7wLE/H+L53HxURgvR8+NuX/" +
                "tkv1v+3iHZd3q3n5j5MPFmx7jOPTzJfvSs2pd2wHlX8NK/O4zX" +
                "ZZtfHdLqRY7RVxwtuAZ4T3y7X6uk+cHOu0GfB0fP+q9O6nlw/+" +
                "sTeHZ6R/vkjs5PyR1Y/cjD9HimHyvzk7PHfYx5WF22+dU5lmOA" +
                "e1D16vx8svNkOqO9SKI5eX0bFhdrNauLv1gXlHiK77yCZ7PV7p" +
                "pa4AMTF9V/jH0WC1zAkfdEpXxdMKOxDhNMQzRfP+B5y1MP0/OW" +
                "178/5POWp5pdZ2a3slvaq+5aOp/OZ7eK33OWFidBg40lQQjxiq" +
                "NsPp7HTKyP6zJTPwY+VEc1xJa/5wyqMiozwR5ltzrnOufyI6vs" +
                "RXItfTR9tHPO/V20WqwfOaqLvzybz6kMr8Oz2YhHJGNyTP8bNs" +
                "/6mZ9oqIZYGSdbtw4t0O917uVS0Re2e9JETnbYopFo1qtZIV4x" +
                "W+7E8yrGgY/rSjbwQhZaWTRUQ2yyo3VsVUZlJtgj9143PyU/bH" +
                "Z+8vEm+z04Vi2cn9b/M/KK5pvl09mldMmtM72nx0uHPF1eqosS" +
                "vNE3u84c4Dl3pBr9vmUELvZ46i5U17sbR/rvC240e707YF3wUb" +
                "Pj1P3WVMfpo6bPu9pxuhmOU39puHH6HH/PeXOy85Pef0pPpicj" +
                "Z/3JQ2aFk3VRcbzJbQdXG4VL7fF0/UjPT9cbXo9fyC5or7pr6e" +
                "n0dHbBrTPV4iRn42a9zo8oxss/wdOcy3nMxPqg52f8t9lnY7AX" +
                "+mKmiC3X4yVPf/8128fm0aF1wcIkzoLVn+V7+p2x1gVD/jeNre" +
                "ea3odp/Y66/72pPm8Z6P+GjD+Pl6vaT5o7nsbDGjY7Fj8Wg/8D" +
                "YwDiiw==");
            
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
            final int compressedBytes = 2572;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW02MHFcRHi2KWAkFiLDANx/2gGFvhgsSCE13j2KLyHCKEp" +
                "lIGIef2CiWrBwCUfDM7uys1wPKAgIEB4RJfMhhL7lEgCAOiZKI" +
                "H4XgaG2RRdoNFqCckpzsC+G9V11dX9V7PTuz2zt4u7U1VfV99V" +
                "X1m56e3l47eyl7qdXKgvWb9zhyrxuYYabsGuWqWC94G+k63mJM" +
                "9d1ATHO4n5pxI6G3wX10V1TFSWAd6PUy2+xy71BZf5liwipVFd" +
                "XFqCcIcnXedok1t5sDczhD3As1Y+72W/sN/Yp5nbOMtM4kCKGj" +
                "ccvBuVKVlIsRzHi9UfMi1lntrLLlmKPOavY0ZnzMkeTQIz6xUM" +
                "+9Z09jre5jO8c9fD1imiNHwT84qXD9dDJnOJMiVZxE1gHnpI0/" +
                "d81vw7w1xW35YLN6g0/3Xnar84r7eZbWqfdXZ6+4827WrOC6Y3" +
                "/K5J5LafauRpkXdztn77cj0b9En5rZBOv5ynt10v4rn6tdp/l4" +
                "nVY+u2/Wab7ZdYq0flF1+kDi7PuMjhfPtW7TLTX9brZOv9MXSx" +
                "5F7U96i5lOf3gcI82nelRttZbuE1S4nf7SvZjXKPKQgxhyEJNj" +
                "8NNbrp4tVkU1GxdPFU+1WmT95j2K2ocoJxlmyq5R5sd6WlvX8R" +
                "Zj2Jc1UxzBpLufPt0r7oqqek7sXHd9ij6R64nc1K5Pg89PeH1q" +
                "j3N9SrFqe9Ss0/BL+3mdlt9r9jqe3Z/dL5Y8ijoPe4sZjYslj3" +
                "BUdWv9gKDClTo7idbUHF2ncZyPIj+95erZYlVUi9VbrXyWbT47" +
                "OEb1+axkZcvNXUk6Tlfms6n3yefrMNHcri/mbHfkIkd0R3VXHS" +
                "qbt6p1gqxlbhenK/Oa3nkt1oK5RvfFnO2e13Dymklr7gse6zwm" +
                "ljyKKMaMxsVqvqhKdfgMnmSu1NlJtKbm6DqN43wULR+M9exsVh" +
                "XVbFxcL667b75gw3fgddrJ1xlmyq5RqbJ6YZ0eTNdV39ARhn1Z" +
                "M8URTLovH6zrFXdFVZxEjsi/Tuu+IPvzNH9vSXWLv+/6W+P2L9" +
                "aLdbYcc1SsZ89ghpmya5SrYr0w+TPpOpxEY9iXqkUvnoI7UyTd" +
                "hOuPBmfTx8/VVpvi/FZ+S10NQ0w5sRhzxtvFc/4V86iSvCrf4g" +
                "5YqasQtzq6m/zYGYZf08dQP5Eo1+V8bXY4O+zerdKSRxHFmNG4" +
                "t4vnYj7XiKd9qRa+7YO71ow5iMmMw6/HenY2q4pqNi7OFmfdmR" +
                "VsOMfO0u7ekW9QTjLMlF2jzI/1tLauqz4BEYZ9WTPFEUy6L79X" +
                "1yvuiqp6Tomze7J73IqVljy/5/P5vI8l4z2fkwj5xMzDUx9ile" +
                "9XiZKermYOMlFTc3SdxnF+iuykeqJ8PqWKajYuThen3YoFG9bu" +
                "NO3k6wwzZdeoVFm9cMV4KF1XvbMRhn1ZM8URTLq7+4KaXnFXVM" +
                "VJ5IiwovzGfKJa6e9P/u09qmb4rWk+p7vw36YVJ7l/Wnn/zu+f" +
                "2jen+tz3ZtPPfWufj1+9nZ6rTLxOV8dZp5UH9uZ82s06DR+Z5j" +
                "ql/i4Vr9PgKzudJ/t47O2k+v+9NT1J50LngljyKPJ+toUZjYsV" +
                "vlZlj/xsi7lSZyfRmsihaqnTOM5HkXQTbralZ9PHr9VsnB3IDr" +
                "gpSkseRcHfxIzGxQq/ejcPsA/oJnOlTp0BBkNlrpY6jeN8JbaZ" +
                "0NvUs2GlVbNxvpm7arLhHnGTdvJ1hpmyu9/vAJUqqxfuC85Kne" +
                "5jO+seOIfoWYbMR9Hw4bpecVdUxUnkiPxr52119r/tY8qJxdii" +
                "Uo9KWlXrazXbG3mYiTmoYXnFMT1rrDTWVQmOsfuC+k441H3N5c" +
                "J3Wzc8per+3f38M3yLniJO990QnfTPC7q3evI3+K+Czkei371/" +
                "E2o3u292w3Wi+5/o++h9vTsq/0O9j/Y+1r3SfV5zun/rwrdp9x" +
                "+V91aqm5vpwYC+3P1Tdyf3TDPBfrD34exodtR9AktLHkXBX8OM" +
                "j1WU5LvXNc4wHnJVrdSp65PBUFk0Yw7X8vwltpbQW6v6rKVUUS" +
                "2K57I555WWPIooxozGxWo+14hH/vDbzJU6tU4GQ2XRjDmIyYzL" +
                "B2M9O5tVRTUb97/Yz/t396u/i/aP7eou40A63/9CPTbe1i9Gok" +
                "fHnWQnrMC8s3OnWPIoGn7XW8wgLjn0iC8+4+jbOpxEY9gXJ7Uc" +
                "PT9FF2/W9Yq76smsNsXZieyEO7NKSx5Fw/PeYkbjYskjvDybT7" +
                "AvqHClTn3uDIbKohlzEJMZL96M9exsVhXVovhIdsR5pSWPovaX" +
                "vcWMxsWSR3jZ/Qj7ggpX6tQ6GQyVRTPmICYz+ukt185mVVEtio" +
                "9nx51XWvIoan/CW8xoXCx5hJfdj7MvqHClTq2TwVBZNGMOYjKj" +
                "n95y7WxWFdVi9fI5xL/1K3veSk4zUls9t87nzPa6yMG5UpUyff" +
                "18Xq++axprb+pXzOucZaR1JkEIHY1bDs6VqqRcjGDG642aF7GV" +
                "07X//qm/n5/TNf3vn6rV29KvmNe59tY4OpMghLa3tp9RODhXqp" +
                "JyMYIZrzdqXo3Vnk/fi8+npZ/tl/Pp4h1NP8+c5Lnv8q928dz3" +
                "R7ffc9/lX+50nqU3J2GP++/HVx5167S6d095l6JPz/CJZju0F9" +
                "oLYsmjiGLMaNzbxXMxX1SlOkz+Q+aKjp0EMVTWk2oOYjLj8Aex" +
                "np3NqqKajYsniyezd8i6O6rgUeT9laFkWi1myq5R0kA9ynqU9D" +
                "Sb+/hdMuJJj+ydxTOI6Tm4N86IR8LewiXuQ8fKx8sMj8nMnKPY" +
                "nl8LJ/ficxfOp59M8+8twx/vrf7gtT2b/KfTXKem/19Z3Trld+" +
                "V3uevvsDllrze9LdVt4VJz+nvz/xQXHnLn08/38/mU3chusOXY" +
                "7/lcPpfd8OcTZ7znc7hr1OPCQj33/s5hLdbhJBqT2F0JzyCmOX" +
                "IU/IOTCpfOJ57THj9XW+0yvpZdc16wIXeNdvdt+DrlJMNM2TXK" +
                "fPH7jzOKStm1/nekrpo40sS+rJniCCbd/fTxjKKDXVFVz4mdm/" +
                "zctc+0bpOt6UkGLw5+PXi298rgOfq9ZXCl7veWsT7F3wwaf4i6" +
                "vLDrOUf+3jL4V3qSWv7vg72xi+vVqdhr4Cp4atoKTU4/+nxqH9" +
                "6xZnU+Ld2XPp+W7m34fPpd9Lk73Oz51OznDtepfb65z137/MTr" +
                "dL7ZdSo2ig22HHPEljPMlF2jUuX99uNSrbV1HU6iMYm9FmKaIx" +
                "h2r+sVd0VVPafE4d51hm0+w//eN5/x8cqQsOoud8bc9SZjtIJY" +
                "ruTrMNHMZxbPjOqLOdtdvIVLyOGftFqkvpqvZu+SdVe/4FHkfc" +
                "x4rkTMQpQ0UI+ypIO1nKc+fpeM3X0d48zSc+BRSPfUjKzDmprr" +
                "MZmZc9y57v6p+GNrH2+NT/8/nZGpzw==");
            
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
            final int compressedBytes = 1457;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW01vHEUQHYkzd4tLVpGlnLxSfEDJKfLObP5CIp+cSJYVxS" +
                "d+API6EWHXd44ECH8BAuJgIyIlKA62FAEH4AB/gCtHpqe2tl5V" +
                "d69nP7OT9LSmt6pe1as37Z7Z9TrJMn3kj4eva/lalvWPQljsGI" +
                "87vumOi/qC6sfjuh1+OQmnPjrtTltmssgjHyMad/ODj/x8YZVq" +
                "Zx095VzhsUoQQ2atVOcgJhqPvvX5rDbLimw+e+95eb4oz6cu1r" +
                "vUOy/nkyzb+lWvaO83f5V7x6G17732Is+yGY/eD2PRVzZi1VdZ" +
                "P46ss0n7d651rslMFnnkY0Tjbi73k5cvrFJd7afvOVd4rBLEkF" +
                "kr1TmIicaj73w+q82yIpvPHt1PVxq9n67Mdz8V28U2z+y7kW/k" +
                "G8W2e45zxFkuhkOjDpcs5CufmxtYi3WoRGPiZ9mD+4jpHLkKPl" +
                "Gp5NJznHXa6+dqy01+51bnVrmzhjNZbuTr+brzJeIsFxMP8ykz" +
                "X6/eS9Y5wriLSa3UqfvOYMjMuNRpHPWTZ5VqRaTTsiKbzx6777" +
                "qdJt93Vv3M993d4m7+Hs3lT72yyHM2RlyueJyFKHEgH0WJB2s5" +
                "Tn3ckIhY0oNxxrQOuQrsHtLIPHStfHWc4TDRzLGhv1fslVY1V7" +
                "E9GqV1SjGI7IknMbQo3+fT3Lpu9BPzMOzLnKEcwUDjaayX3xVZ" +
                "tU7sHLvvil+afN9Z9bPed/F18vJqrFPn3qqsEym5aJ0e/v1m1g" +
                "n309bB/NZp62Diz08HF6/Tw/8m+B1zP9/nmf1qtMuxj5HKao9w" +
                "iaHVlizkK1/bWIt1qCQ0HKKVYj3q10pivRi11687YU1lDfJBaV" +
                "VzFRvQIFtHOFOGRqXK2VsfS7Xm1nWjdRqEh0McF9bpbMGwe6yX" +
                "3xVZrRLJiT7HXzb6Of5yWc/x7s1Gf868ucrvd+PWKd9e5jqFus" +
                "22Tobr0mjnvsoafMxfffT5dNbo59NZU++75a5TMCut0xLWKfZ8" +
                "SkfaT2k/LW13pXUKf864WlzlmX32eOYIZ8rQqFRZPs2t61CJxr" +
                "AvKrU5gmH3WC+/K7JqneBvFpulVc1VbJMG2TrCmTI0KlWWT3Pr" +
                "upFiD8O+zBnKEQy7x3r5XZFV68TO6Tk+zftd/6e0TrW+t/tmOq" +
                "wOPruCyWvmo8k/Dj9kq/9zepcb85P5ejqsDr4ITTqjvoJZtcJ+" +
                "Ok27Jn3OTOu0oM/jl4vL9Io+xqxvUWYQXFjD/YgRK30tjFse3U" +
                "3OkIawyvhKxNemHK2iVdmtIVb5GLO+RZlBcGYJqmlxB6zUVYhb" +
                "Ht1NzpCGsMroOrVCWmNX0/893WN1jv6fb8l1/FEr66+pPxf8m/" +
                "ZKrRX+J61Bnd+D0/cFaZ3m+v345+kOix/dJ90nbNHpZ/ApGVgz" +
                "jrlOnuYMK9AafSVaI76GecTDmbl13jD7pHviZjn9DD4lg/NC+c" +
                "hcJ09zhhUgD/PaKtGIr2EevHLNwwPy7nTvyEwWeeRjROMy63xh" +
                "lWq0sU7pNxgya6U6BzHRGOKz2iwrsvnskzzHH331dj3HH32xmP" +
                "e7d3editvFbZ7ZZ49njnCmDI1KleXT3LoOlWgM+6JSmyMYdo/1" +
                "8rsiq9YJ/k6xU1rVXMV2aJCtI5wpQ6NSZfk0t64bKfYw7MucoR" +
                "zBsHusl98VWbVO8HeL3dKq5iq2S4NsHeFMGRqVKsunuXXdSLGH" +
                "YV/mDOUIht1jvfyuyKp1it+93r1ePtGHM1nkkY8Rjcus87lGLG" +
                "1jnXq/MxgyC6efg5hoDPFZbZYV2Xz2d/n3llX4fxt2nQbvN3ud" +
                "0t8R0vcFaT+t9n4arL2Z/dQ5nvj/3x3X2U+ffragdfog3XeLvO" +
                "8O7zX+G7rz7rnMZJFHPkYQlxhaXMU242jbOlSiMeyLSm2O1q8z" +
                "/V5+V63McnOO+fd0rdV8v/vkxmr9HSGtU+T4Hyx6YVs=");
            
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
            final int compressedBytes = 1347;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vHFUU3ToVQvTRKj8gKSL6ZGYKfgQNLvMHQrnIxrIbRA" +
                "WR+JKlpQQkkFJQgCUkaBBIJC7S8SfsIg2Teblzzrn3vWXW610l" +
                "zntPc/fdr3OOn+fLK3k20/Hxe7M6MqN72D2ETavkJZ8jmofVeq" +
                "Cim9fc55UoptZon+ZZX1SmSMpbQovoOhZf1XMnN+7fuH8DNq2S" +
                "l3yOaB5W64GKbl5zn1eimFqjfZpnfVGZIilvCS2i1/Np2lj83h" +
                "9/9Mfjwbu5+Lu3v2bqzjKxX7KI/4TIbxur/Hll9s9JGKfj6q/0" +
                "uf/vpfXcrGdObrTP2mdmzTfPrEWsElOz6PJ4iq19rERzzMtKfQ" +
                "1yzF7iiqyMqjrhN0+aJ7xvyU8xWPZ91hDwiXUcVo8+zw0E1MYa" +
                "1uWjq1SWRy7PP9k696eDR9fr/nTw2Xbu4/t71+w+/kF93k3Zp2" +
                "29F7y51929xb0FbFolL/kc0Tys1gMV3bzmPq9EMbVG+zTP+qIy" +
                "RVLeEpr325P2pH/yDXZ4Bp6kmdYasUpMzaLL4ym29o1P6JBjXs" +
                "PM1SDH7CWuyMqoqpOZ63tmvY9f5X3cYX1Sz5x6Pl3+fGqeN8+j" +
                "n2Kw7FvEYkAAkkdVfOvPcwM/VjCy6WI1qoFtWZHW52IveruL7m" +
                "I2S3b4jvMizbTWiFViahZdHk+xtW/8BjbkmNcwczXIMXuJK7Iy" +
                "quqE3/ajf/INdngGvhxprRGrxNAsujyeYmvf+IQOOeY1zFwNcs" +
                "xe4oqsjKo6ye/arl8Ndoh1aaa1RqwSU7Po8niKrX2j4pBjXsPM" +
                "1SDH7CWuyMqoqhN+d96d92fWYIdz7DzNtNaIVWJqFl0eT7G1b7" +
                "wCQo55DTNXgxyzl7giK6OqTmZ2d67HcfXqj11ore8FdZ+u8P1p" +
                "r9kza755Zi1ilZiaRZfHU2ztYyW5yTqAF1VozitTFYYZO72S0T" +
                "9sDvvVYIfYYZpprRGrxNQsujyeYmvfuE+H+ck6gBdVGLNWRq7I" +
                "yqheCWp2dd017+/yusuxXenfwfV7lXofr8+7re9T97R7atZ888" +
                "xaxCoxNYsuj6fY2sdKNMe8rNTXIMfsJa7Iyqiqk5nr+VTv4/U+" +
                "vuvzqb3T3jFrvnlmLWKVmJpFl8dTbO1jJZpjXlbqa5Bj9hJXZG" +
                "VU1cnMOj5611bHH9Zrjd5bz5qz6KcYLPs+awj4jKiKbxW+xiOg" +
                "NqeXMxpdpXLVTpRiud5muQJp+T9Myy39LpdTK6Yr2FRr8+3lcl" +
                "Py29CkFdMVbKq1vhfUfdpktLfaW+mTfY5532cNAXmg5vkSIndG" +
                "LZb3OMqGI6chr7K8E+W96ee8nQ/r+cvc4HPM+z5rCMgbSlbN3B" +
                "i4U7s473GUDUdOQ15lcZ/mOa32ufjRXXffr32l/rTBVf7d2h0/" +
                "vBrX4fGi3ovqPl3hPu3XPZi0T0d1Dzb/XuVN/n8Edz59Ws+c8u" +
                "iW3dJW6YgVdqCCe1YhT6lTzLwC1RiVqEb+zOPAY2vYWlf/brnk" +
                "dfd53YNJ+/So7sGKe8Npd/rC4ogVdqDC6nL1jDylTjHzChjHcH" +
                "0XNPJnHod/csWxSX/rPWgfmDXfPLMWsUpMzaLL4ym29rESzTEv" +
                "K/U1yDF7iSuyMqrqZOZ13p+Ov7he709r3I++VP/gm11e7wdfvz" +
                "Z3ptvdbdi0Sl7yOaJ5WK0HKrp5zX1eiWJqjfZpnvVFZYqkvCU0" +
                "7/vvn47eGc+0iWfWJt8/Hb39unz/1N3t7sKmVfKSzxHNw2o9UN" +
                "HNa+7zShRTa7RP86wvKlMk5S2hRXT3G36rviVlx39WieYO");
            
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
            final int compressedBytes = 1134;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj1vE0EQvRoiCkSFhPIraNLhuyv4FUBJly4SFZYQENxT0q" +
                "ZFAomCIiCBoEGAkJJ0CERPmwqwvUzee7N7x9nkg4/ZVSY782be" +
                "PG3W57Vh/KiSsXmuWnCMH1dLj82zC3d7WJ3IaNfaNdi0Sl7yOa" +
                "I4rOaDFdW85jqvRDk1R+sUZ325MmXSvl1s3m++NF+qKtnZmK3M" +
                "M2sRy8RUFFWeT7m1zkaOcV/jLOUA4+5dvfKuzKo6ufP41fTn9f" +
                "Tnyfxcr47fTe2zwonfKcS2i6+ND1nkxe+e+/HTXvTNII7nB6u3" +
                "SyiIfVpGz2oVI85TnKc/6jzd+fhvnadbn+I8He24fTn2oHgfv9" +
                "HegE2r5CWfI4rDaj5YUc1rrvNKlFNztE5x1pcrUybt28WWs8fr" +
                "bshorjfXzZpvnlmLWCamoqjyfMqtdaxEMe7LSn0OMO7e1Svvyq" +
                "yqkzvH/SnumUd3z2y//o1Pj5vnj6vTpY/6m+Ma8xllnkWQhPbj" +
                "Pod1lSpTLEc4MuPr09un6Pb7eG+L+3jsU+xTfP/0N90LmmvNNb" +
                "Pmm2fWIpaJqSiqPJ9yax0rUYz7slKfA4y7d/XKuzKr6oRfr9fr" +
                "VZXsbMxW5pm1iGViKooqz6fcWmdDmTXXcPDlKhTzylSFceaVXs" +
                "mBP6kn09XczmOTNNNaI5aJqSiqPJ9ya93BPk3Kk3WAL1dhnTUz" +
                "75V3ZVavBDnxfIrneOxT7NNx7tMi/47w/+7Trf0FvkfZaXfMmm" +
                "+eWYtYJqaiqPJ8yq11rEQx7stKfQ4w7t7VK+/KrKqTO8fnlnjd" +
                "Hd5zvL5aXzVrvnlmLWKZmIqiyvMpt9axktJkHeDLVSjmlakK48" +
                "wrvRLkxHmK113cC477XtCsNCu5n2Kw7HsUDGDyrMqfGPMciwDv" +
                "zmFdPtqvsnsnumKpNp5Py4z6wXLYEPwoNGnGcAWLaq336/3cTz" +
                "FY9i1iMTCAybMqv9WXe4M/z2Bm08VqVAPbbkWaX4rNq/fqPUHm" +
                "forBsu9RY8BvrAud96xDnuMZkJvnsC4f7VPZs097XbF59W69K8" +
                "jcTzFY9j1qDPiNdaHzrnXIczwDcvMc1uWjfSp79mm3K1aqrbd6" +
                "mLZ+0WnriJ5PW0Mzhiv4Xa33vsUn3kH79D32IO7jh3d/2vwcJ2" +
                "fIiP8XPfB8x/eZg8bkVOzBoH06E3tQ/DbhSnPFrPnmmbWIZWIq" +
                "iirPp9xax0oU476s1OcA4+5dvfKuzKo64bcX24tVZTatkpd8ji" +
                "gOq/lWg5WuuY7/Yh5jZnDmOYxBY4nPa/OszJazx/e+cc88vH0a" +
                "vRy9NGu+eWYtYpmYiqLK8ym31rESxbgvK/U5wLh7V6+8K7OqTu" +
                "68yHmaXDiZ8zTaXvQ8jbaHnKe79+OeGZ+DT+ZzcOxTfA4+vNFu" +
                "tBuwaZW85HNEcVjNByuqec11Xolyao7WKc76cmXKpH272Lw/Oj" +
                "06PX13+GnTKnnJ54jisJpvNVjpmuvkHcphzAzOPIcxaCzxeW2e" +
                "ldky9h8HrlAu");
            
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
            final int compressedBytes = 1511;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW09rHXUUnc8grrrpV2g33bfTAe1XcNXSBLJoQjeB7vqk2D" +
                "wl30GkkK2g4MKFGgTdiIJEyEaQfIBupSvnzc2Zc8+9vxnm1dJa" +
                "+5vLu+/+O+ee/DrJe3mhTbP6qWlWP/ePb5r+Wl1d/db775t0rf" +
                "4o1L5rCtfq91T5sfmX1+rb2e4vizh+GKNf7fnJX9vquHmhz76u" +
                "tThR5tmmY935fpzxukpIq+WOr2z45vQWef/UZ1/XWpwo82zTse" +
                "58P854XSWk1XLHVzZ8c3q1t8333dMv/l/fd08/X7r/1sWtC3jk" +
                "yOBRwSRNu0RFPuVWnFeiPb/XK40z7PntU7vyVs+qOv3mcOZXm3" +
                "otuTfrOU2dzDv8vuDlr5unbw79Kq9lSrbRW++nek71nOo51XN6" +
                "u38PfnfP6cnfy/e399p78MiRwaOCSZp2iYp8yq04r6RkXgf5sg" +
                "rtRWWqApwZGZVwpr4fX3Q/PWgfwCNHBo8KJmnaJSryKbfivJKS" +
                "eR3kyyq0F5WpCnBmZFTCmfp6V3+O19e71/56d7e9C48cGTwqmK" +
                "Rpl6jIp9yK80pK5nWQL6vQXlSmKsCZkVHJmJ+1Z3JuQ241ep/H" +
                "Lhj4zLjw73KGDXkmMnA2z3hdsTqncuaOOZuqDegX7QvpDLnV6H" +
                "2OCmpkIFNkVX7gy7vJnyc8M3R5NarB+2lFOl+qDejz9lw6Q241" +
                "ep/HLhj4zLiw+Rwb8kxk4Gye8bpidU7lzDmdT9VK2E9X9T3lkq" +
                "ue07Lrsw/qGSw6pw/rGSy5PqnnVH6P+pXm6/fHO+ujhQxfv/z2" +
                "9Xtb6/3yzZxTd627Rm+RZZb7ivbpdZ6sRPvY46IS5dQZxWnf68" +
                "vKlEn3TrGl/EZ3o48uvUWWWe4r2qfXeWAYaexxck6h55nJmWd8" +
                "jxpLfFFbZPVsmb1+rlI/L6jnVD9/qvfT23w/dY+6R/QWWWa5r2" +
                "ifXufJSrSPPS4qUU6dUZz2vb6sTJl07xRbZq/308I76rlmm9xq" +
                "9D6PXeI9k7Iqv7LF3X7OV/IMOR5fmd6nW7d8F/4875z/+v7b1+" +
                "Mrr+E3vPp6t+Bq77f34ZEjg0cFkzTtEhX5lFtxXknJvA7yZRXa" +
                "i8pUBTgzMioZ84P2oI8GP9QOzCzWCiZp2iUq8im34sZzOiib10" +
                "G+rEJ7UZmqAGdGRiVjvtfu9dHgh9qemcVawSRNu0RFPuVW3HhO" +
                "e2XzOsiXVWgvKlMV4MzIqGTM1+26jwY/1NZmFmsFkzTtEhX5lF" +
                "tx4zmty+Z1kC+rwGadzLvyVs8alYz5frvfR4MfavtmFmsFkzTt" +
                "EhX5lFtxo+L9snkd5MsqtBeVqQpwZmRUMua77W4fDX6o7ZpZrB" +
                "VM0rRLVORTbsWN57RbNq+DfFmF9qIyVQHOjIxKxvyoPeqjwQ+1" +
                "IzOLtYJJmnaJinzKrbjxnI7K5nWQL6vAZp3Mu/JWzxqVZMTlp+" +
                "L173fL/i71cT2Dmd9jnnXPENkjT+DBCY+ZY14yp5xlBaoxK1GN" +
                "/rnMw8x7cOvc5fRJd4LIHnkCD054zORXf6Lcc5PRz2vMSlSjfy" +
                "7zMPMe3Dp3OX3anW48H3kCD05grpv5X0eb+SVzyllW4HnAG1HU" +
                "6J/LPP4rVx6Ym7veXae3yDLLfUX79DpPVqJ97HGiP/Q8syrVGd" +
                "+jxhJf1BZZPVtmr58X1M9VXt05dYfdIb1FllnuK9qn13myEu1j" +
                "j4tKlFNnFKd9ry8rUybdO8UW89t3bt9pGvObaxMhg0cFkzTtEh" +
                "X5lFtxuHLP7wVnaYY9v31qV97qWVWn31y/7xZ9nvmwfQiPHBk8" +
                "KpikaZeoyKfcivNKSuZ1kC+r0F5UpirAmZFRyZjvtDt9NPihtm" +
                "NmsVYwSdMuUZFPuRU3ntNO2bwO8mUV2ovKVAU4MzIqGfPDtv9Z" +
                "ZX6oHZpZrBVM0rRLVORTbsWN53RYNq+DfFmF9qIyVQHOjIxKxv" +
                "y4Pe6jwQ+1YzOLtYJJmnaJinzKrbjxnI7L5nWQL6vAZp3Mu/JW" +
                "zxqVjDP/ANyGkKI=");
            
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
            final int rows = 38;
            final int cols = 74;
            final int compressedBytes = 916;
            final int uncompressedBytes = 11249;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV8tq3EAQ1J/4H/wLQt/ixXuzd1lfdfZ+h/FjyTWHHJLgBO" +
                "cSEgjkeyLPuKa6umeFgnMwZKaZVj+qS6VBa6OuG5+6bvw27ffd" +
                "tMaT8efkP3Vhjb8rtY9dZY2/QuVL98o1fpjtfl/E8blEP/72/v" +
                "1lfwmPHBk8KkDStMspz6fcOmeV1MzqIF9UoT2vTFWAM056JSXf" +
                "9/spSj7V9tlyrBUgadrllOdTbp0r57Svm9VBvqgCd1ZkvFe8q2" +
                "X1Sohpv7vla7gZbhDlHRHYRNiZOeYlOOWsK1CNUYlqtNc6DzPr" +
                "wa04v67fdW0teTdP2hnMvPOPw+Oz544IbCKAq+Et8xKcctYVWB" +
                "7w+ilqtNc6j31y5YEZ3OlwSp+jnOXcVrRPr3iyctrGdk70u55l" +
                "VqWKsT1qrPF5bZ7VskX29v9uoYJ2Tu2c/t13y0V/AY8cGTwqQN" +
                "K0yynPp9w6Z5XUzOogX1ShPa9MVYAzTnolxLT3qf3u2jm1c3rL" +
                "38Hm++7VT/R/rOuv7Qzad/A/fJ+e2hm0v+Pt/107pzfyfXfen8" +
                "MjRwaPCpA07XLK8ym3zlklNbM6yBdVaM8rUxXgjJNeCTHtfVr0" +
                "Pp31Z/DIkcGjAiRNu5zyfMqtc1ZJzawO8kUV2vPKVAU446RXUv" +
                "Jdv5ui5FNtly3HWgGSpl1OeT7l1rlyTru6WR3kiyq055WpCnDG" +
                "Sa+k5Ot+PUXJp9o6W461AiRNu5zyfMqtc+Wc1nWzOsgXVWjPK1" +
                "MV4IyTXknJN/1mipJPtU22HGsFSJp2OeX5lFvnyjlt6mZ1kC+q" +
                "0J5XpirAGSe9kpKv+tUUJZ9qq2w51gqQNO1yyvMpt86Vc1rVze" +
                "ogX1ShPa9MVYAzTnolxDyv4X64R5S3X6hnZEbYmWPrGb8Ep5x1" +
                "BaoxKlGN9lrnYWY9uBX3gr4dbhHlHRHYRNiZo09/q9xzSO/nNU" +
                "YlqtFe6zzMrAe34vqr/goeOTJ4VICkaZdTnk+5dc4qqZnVQb6o" +
                "QntemaoAZ5z0Skq+7bdTlHyqbbPlWCtA0rTLKc+n3DpXzmlbN6" +
                "uDfFGF9rwyVQHOOOmVEJPetMNwQJR3eOsP2ETYmaO/poNyzyG9" +
                "j0xWS1SiGu21zsPMenAr7gV9N9whyjsisImwM0ef/k6555Dez2" +
                "uMSlSjvdZ5mFkPbsW9oB+GB0R5RwQ2EXbm6NM/KPcc0vt5jVGJ" +
                "arTXOg8z68GtuO4Pb5U9QA==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 0, 17, 0, 0, 0, 0, 0, 0, 18, 3, 0, 19, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 4, 0, 0, 21, 5, 0, 22, 23, 0, 24, 0, 0, 25, 0, 1, 0, 26, 0, 6, 27, 2, 0, 28, 0, 0, 0, 29, 30, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 9, 0, 0, 0, 0, 31, 0, 10, 32, 0, 0, 0, 0, 0, 0, 0, 33, 0, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 34, 0, 0, 0, 0, 17, 3, 3, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 36, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 37, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 38, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 39, 4, 0, 40, 0, 0, 0, 41, 0, 0, 0, 42, 43, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 8, 0, 0, 44, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 45, 10, 0, 0, 0, 0, 0, 20, 21, 0, 22, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 24, 25, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 29, 0, 0, 0, 4, 0, 0, 30, 1, 0, 31, 2, 0, 0, 0, 5, 4, 0, 0, 34, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 37, 0, 1, 0, 38, 0, 0, 6, 39, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 9, 41, 42, 0, 0, 43, 0, 5, 6, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 1, 0, 0, 0, 0, 0, 0, 0, 46, 2, 0, 0, 3, 0, 7, 47, 0, 0, 0, 1, 7, 0, 0, 8, 48, 8, 0, 49, 0, 0, 0, 0, 50, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 0, 0, 47, 52, 53, 0, 54, 0, 55, 0, 56, 57, 58, 59, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 60, 61, 10, 0, 0, 0, 0, 11, 0, 0, 62, 0, 0, 0, 63, 12, 13, 0, 0, 0, 64, 65, 0, 0, 0, 4, 0, 66, 0, 0, 0, 48, 5, 67, 1, 0, 0, 0, 14, 68, 0, 0, 0, 15, 0, 1, 0, 49, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 12, 0, 0, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 69, 70, 12, 0, 51, 71, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 72, 73, 0, 74, 75, 76, 77, 0, 1, 0, 2, 0, 0, 0, 78, 0, 0, 15, 16, 17, 18, 19, 20, 21, 79, 22, 52, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 36, 37, 0, 38, 39, 80, 40, 41, 42, 43, 81, 44, 45, 46, 47, 48, 49, 0, 0, 1, 0, 50, 5, 0, 0, 0, 0, 0, 0, 82, 83, 9, 0, 0, 2, 0, 84, 0, 0, 85, 1, 86, 0, 3, 0, 0, 0, 0, 0, 87, 0, 2, 0, 0, 0, 0, 0, 0, 88, 89, 0, 0, 0, 0, 0, 0, 0, 0, 90, 91, 0, 3, 4, 0, 0, 0, 92, 1, 93, 0, 0, 0, 94, 95, 96, 0, 51, 97, 98, 99, 100, 0, 101, 53, 102, 1, 103, 0, 54, 104, 105, 106, 55, 52, 2, 53, 0, 0, 107, 0, 0, 0, 0, 108, 0, 109, 0, 110, 111, 0, 0, 10, 0, 1, 0, 0, 0, 112, 4, 0, 1, 113, 114, 0, 0, 3, 1, 0, 2, 115, 0, 6, 116, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117, 118, 119, 0, 120, 0, 54, 3, 56, 0, 121, 7, 0, 0, 122, 123, 0, 0, 0, 0, 0, 5, 0, 1, 0, 2, 0, 0, 124, 0, 55, 125, 126, 127, 128, 57, 129, 0, 130, 131, 132, 133, 134, 135, 136, 56, 137, 0, 138, 139, 140, 141, 0, 0, 5, 0, 0, 0, 0, 0, 0, 57, 0, 142, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 6, 143, 0, 144, 58, 0, 59, 0, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 145, 146, 0, 0, 147, 148, 7, 0, 0, 0, 149, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 150, 1, 151, 0, 152, 0, 7, 4, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 11, 0, 0, 12, 0, 13, 153, 9, 0, 154, 155, 0, 14, 0, 0, 0, 15, 0, 156, 0, 0, 0, 0, 62, 0, 2, 0, 0, 0, 8, 0, 0, 6, 0, 0, 0, 0, 157, 158, 2, 0, 1, 0, 1, 0, 3, 159, 160, 0, 0, 0, 0, 0, 7, 0, 0, 0, 58, 0, 0, 0, 0, 0, 59, 0, 0, 161, 0, 0, 0, 9, 0, 0, 0, 162, 163, 164, 0, 10, 0, 165, 0, 11, 16, 0, 0, 2, 0, 166, 0, 4, 2, 167, 0, 17, 0, 168, 0, 0, 0, 18, 12, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 169, 2, 0, 3, 0, 0, 0, 13, 0, 170, 0, 0, 0, 0, 171, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 172, 0, 173, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 174, 0, 2, 175, 176, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 177, 0, 178, 179, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 181, 22, 18, 0, 0, 0, 0, 0, 0, 182, 0, 0, 1, 0, 0, 19, 183, 0, 3, 0, 0, 7, 10, 1, 0, 0, 0, 1, 0, 184, 23, 0, 0, 0, 0, 24, 0, 0, 20, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 16, 0, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 25, 0, 64, 0, 0, 188, 0, 189, 0, 190, 0, 191, 21, 0, 0, 192, 0, 0, 22, 0, 0, 0, 60, 0, 26, 0, 193, 0, 0, 0, 0, 0, 0, 0, 194, 23, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 195, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 24, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 196, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 0, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 197, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 1, 198, 199, 0, 1, 26, 0, 0, 0, 0, 0, 0, 0, 0, 200, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 3, 0, 0, 1, 201, 202, 13, 0, 0, 0, 0, 0, 0, 0, 0, 203, 67, 0, 0, 204, 0, 0, 205, 206, 0, 0, 0, 0, 0, 0, 0, 0, 207, 0, 0, 0, 208, 68, 0, 209, 0, 0, 0, 3, 0, 0, 69, 0, 0, 62, 0, 0, 27, 28, 0, 0, 3, 0, 0, 29, 0, 0, 210, 0, 211, 0, 0, 64, 212, 0, 28, 213, 0, 214, 215, 0, 0, 29, 30, 0, 216, 217, 0, 31, 218, 0, 0, 219, 220, 221, 222, 30, 223, 32, 224, 225, 226, 33, 227, 0, 228, 229, 6, 230, 231, 31, 0, 232, 233, 0, 0, 0, 0, 0, 70, 0, 2, 234, 0, 0, 235, 0, 236, 34, 0, 0, 0, 237, 0, 238, 35, 0, 0, 36, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 239, 0, 240, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 39, 0, 0, 0, 0, 36, 0, 0, 0, 241, 0, 0, 0, 242, 243, 0, 0, 0, 244, 1, 0, 0, 0, 5, 2, 0, 245, 0, 0, 0, 0, 0, 0, 0, 0, 37, 246, 0, 40, 0, 247, 0, 38, 248, 249, 39, 250, 0, 251, 0, 0, 0, 0, 0, 252, 40, 253, 41, 0, 254, 0, 255, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 257, 0, 0, 258, 0, 7, 0, 0, 0, 42, 0, 259, 260, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 261, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 262, 263, 264, 0, 0, 0, 0, 0, 0, 0, 265, 0, 0, 0, 0, 0, 8, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 0, 0, 2, 0, 267, 3, 0, 0, 268, 44, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 1, 0, 0, 2, 0, 269, 44, 0, 0, 0, 270, 0, 0, 0, 11, 0, 0, 12, 13, 0, 45, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 271, 0, 0, 0, 272, 0, 0, 0, 0, 273, 0, 0, 0, 45, 0, 0, 0, 46, 0, 274, 0, 0, 0, 47, 0, 0, 0, 0, 275, 276, 277, 0, 0, 48, 278, 0, 279, 49, 50, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 8, 51, 283, 0, 284, 52, 285, 0, 0, 53, 0, 4, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 0, 54, 289, 290, 0, 0, 55, 0, 0, 56, 0, 24, 0, 0, 25, 5, 291, 6, 292, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 293, 3, 294, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 295, 0, 296, 0, 0, 0, 0, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 297, 0, 0, 0, 0, 0, 0, 298, 0, 0, 0, 7, 299, 0, 0, 0, 58, 0, 300, 0, 0, 301, 0, 0, 302, 303, 0, 46, 304, 0, 0, 0, 59, 65, 0, 0, 0, 305, 306, 60, 0, 61, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 307, 0, 8, 308, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 66, 0, 0, 0, 2, 47, 0, 0, 309, 310, 311, 63, 0, 0, 0, 3, 0, 0, 312, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 9, 313, 0, 51, 314, 52, 73, 0, 315, 53, 64, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 316, 0, 66, 0, 0, 317, 67, 68, 0, 54, 0, 318, 69, 319, 0, 55, 70, 320, 321, 71, 72, 0, 56, 0, 322, 323, 0, 73, 57, 324, 0, 58, 0, 0, 0, 74, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 325, 59, 326, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 327, 6, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 328, 0, 0, 0, 0, 0, 0, 0, 0, 329, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 0, 61, 330, 331, 0, 0, 62, 332, 0, 63, 333, 0, 64, 334, 65, 0, 0, 75, 0, 0, 335, 336, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 337, 0, 67, 0, 0, 0, 0, 338, 339, 67, 0, 0, 0, 77, 0, 4, 5, 0, 6, 0, 0, 3, 0, 0, 0, 340, 0, 341, 342, 0, 0, 0, 78, 0, 0, 79, 343, 0, 0, 0, 0, 68, 0, 80, 0, 344, 0, 81, 69, 345, 346, 347, 348, 0, 82, 83, 0, 349, 84, 70, 350, 0, 351, 352, 353, 85, 0, 0, 0, 0, 354, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 355, 1, 0, 4, 0, 5, 0, 0, 6, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 357, 88, 76, 77, 358, 0, 359, 360, 0, 0, 361, 362, 0, 0, 0, 7, 0, 0, 78, 0, 79, 363, 68, 89, 0, 0, 0, 0, 0, 0, 7, 0, 364, 0, 0, 0, 365, 0, 366, 0, 0, 367, 0, 90, 0, 368, 369, 0, 91, 370, 371, 372, 373, 92, 93, 0, 0, 0, 374, 0, 0, 375, 376, 377, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 378, 379, 0, 380, 0, 381, 382, 0, 0, 0, 0, 97, 98, 0, 0, 0, 383, 0, 0, 69, 70, 7, 0, 0, 0, 0, 0, 99, 100, 101, 384, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 106, 0, 82, 107, 0, 83, 385, 386, 0, 0, 84, 0, 8, 0, 0, 387, 0, 0, 108, 0, 0, 85, 0, 388, 0, 0, 86, 0, 389, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 390, 0, 0, 0, 0, 391, 0, 392, 0, 87, 0, 393, 0, 88, 109, 110, 89, 0, 0, 111, 0, 394, 0, 112, 395, 396, 0, 113, 397, 0, 0, 0, 0, 0, 398, 0, 0, 0, 0, 34, 114, 115, 0, 116, 399, 0, 400, 0, 0, 0, 117, 401, 0, 118, 119, 402, 0, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 0, 5, 6, 1, 1, 5, 6, 7, 8, 1, 2, 0, 1, 0, 0, 9, 1, 5, 0, 5, 0, 1, 7, 2, 10, 0, 11, 0, 12, 1, 0, 0, 6, 13, 0, 14, 15, 11, 3, 11, 0, 13, 3, 1, 16, 17, 9, 2, 11, 18, 16, 19, 4, 0, 20, 21, 22, 1, 1, 23, 19, 2, 24, 25, 1, 26, 1, 7, 7, 27, 13, 0, 28, 2, 13, 29, 30, 1, 4, 0, 0, 31, 32, 7, 1, 33, 34, 3, 2, 0, 1, 2, 5, 35, 36, 16, 37, 38, 0, 39, 40, 7, 41, 1, 42, 0, 1, 43, 44, 8, 6, 45, 5, 46, 47, 48, 4, 9, 1, 6, 49, 50, 51, 20, 6, 9, 0, 52, 0, 53, 54, 10, 5, 55, 56, 0, 57, 1, 18, 0, 58, 59, 60, 9, 61, 26, 62, 2, 63, 3, 64, 9, 65, 66, 67, 1, 68, 0, 20, 69, 70, 71, 72, 73, 0, 3, 74, 18, 0, 0, 75, 0, 76, 77, 7, 10, 1, 2, 78, 4, 0, 79, 0, 80, 1, 81, 1, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 13, 0, 10, 90, 14, 2, 91, 92, 93, 94, 20, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 26, 6, 10, 2, 24, 27, 101, 0, 4, 102, 2, 1, 2, 103, 0, 9, 104, 105, 1, 106, 107, 108, 109, 110, 111, 10, 0, 112, 23, 13, 0, 0, 8, 1, 1, 113, 41, 2, 23, 13, 4, 7, 114, 6, 2, 11, 115, 27, 116, 117, 0, 0, 18, 29, 1, 118, 7, 1, 0, 3, 16, 0, 2, 119, 3, 14, 1, 0, 120, 121, 49, 20, 8, 4, 21, 122, 1, 4, 123, 124, 26, 125, 12, 126, 0, 6, 127, 128, 129, 130, 131, 132, 29, 31, 133, 134, 7, 9, 135, 32, 14, 10, 136, 137, 12, 0, 6, 12, 138, 139, 140, 14, 141, 2, 142, 143, 144, 35, 20, 145, 146, 147, 26, 148, 2, 7, 4, 149, 150, 0, 38, 151, 152, 0, 153, 0, 154, 39, 23, 40, 155, 156, 4, 157, 49, 41, 8, 158, 159, 9, 42, 160, 161, 162, 0, 163, 164, 14, 0, 165, 166, 47, 6, 1, 31, 167, 168, 169, 18, 170, 171, 12, 1, 172, 173, 174, 21, 5, 0, 41, 0, 0, 9, 175, 2, 27, 29, 10, 3, 1, 34, 1, 176, 15, 177, 178, 8, 8, 0, 179, 180, 181, 1, 182, 183, 24, 184, 27, 185, 42, 2, 0, 186, 187, 188, 31, 0, 19, 0, 3, 2, 189, 9, 23, 11, 190, 191, 2, 192, 193, 51, 194, 19, 195, 196, 197, 2, 0, 198, 199, 7, 200, 52, 3, 21, 201, 23, 14, 202, 203, 3, 204, 0, 15, 205, 53, 206, 207, 208, 209, 6, 210, 211, 212, 213, 214, 3 };

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
            final int compressedBytes = 1346;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE2S0zwQbQmREsNGUFmEnYAUxZoTiA01y2/5LVM1HESzoY" +
                "YjsJu5CdyEo6DYjke25b9ElttOvyoyCYljqfW69brdzh27h4cd" +
                "HB833L77z2xAfzX7/ZsH+Ans/V8hH4GL7U7e//i+fzJfzC2Yz/" +
                "CRvf7/z/bXAQgEQhPK/ZNtb/LTE7P8iX4EC9LN1MUPwe1OgYsf" +
                "zHzS7q0HgIMVrx75W3Dxw26M0PABbrV5cH9e7/7AO4ofcNcdf3" +
                "9T/CUM8b8n5383pf/tNTDnZqCt4w/nMvO/37n/wS35H4GADrLQ" +
                "DQX0ST8IYO7V6SPiqBqyV0YZMloJ0WteRUYiEAgEQn3j9fL25n" +
                "+rPGsvtpiv+f57gBj7791Lqr8REIBlNJce67NnuvAIpetOwlHU" +
                "r/z6yYul+g8vHoQoYgyrhaUWfbsC/V/WD5VfP9wf2fZcv8jqh9" +
                "+K+iFMUD/0jcuHlmc1BY01obLanfX7mP4Xrp+f+H/I+E/1c8Ic" +
                "xRNDhhnsv3PV3/34cRPaP29P+2d5fjPF+Rvzh8b8Dd7rDyj4L1" +
                "vyz2r9F6j+izSBaq/fN2iVr+bz+gkME7ie6w8YlbsYsjwIbERX" +
                "UbpWZkj90MSsHy6EvGjRU38d2H+DoH7QVr8LELNSv5PNGRTPby" +
                "ojlBMLpL7x65bxI4IaG7DnHX8U/vXUXxP1r53vf4a9B6uO7JPu" +
                "OGaOxxgB4o2FV8CYyVbRfb1kdv94n0sel9wx9+Vba7qHxMDr9W" +
                "wyJZ7/n04ihEu/Wvi30vqxcUvRWD+orp8u1u8psH7d9bdU9Weq" +
                "3xBy2PIBWqOH94GI/Bml4VRnVOVjBSI+/SiWTyUZK3dkHaJBZF" +
                "wQ7gy6/ATXHnvzTfLFsYggtx5xrfTiU9Dau5JGXJZfJ9udYnq/" +
                "jJUDm0SstImNZEaSqXtexw29mEItKZDtHGCzzHMF0eIi9SmCvs" +
                "FPy2ECYchGOx6JEO1/16T0s5nAZiGgbot8qiMwsrg+gCMShybM" +
                "bOSkfyorJTujiWwKcbmf6jE62wLh3CRF9NpP961eEh4P1hQmxD" +
                "bTNfA55QpC6jLYBlMk2bQk89KcifWLXKTPMTxDkdqzJe8ioW2+" +
                "kPUF5bBiiGWtfxr9K59ZIEcEMbkCPoyZrzifRSRjFpfpr5fz86" +
                "C3f7O8/rTpuP5E139Wmr/iOv+60d5/23CrXFnG698SxH/U41+v" +
                "UBne/7BB3P+wMGgyAe3/C4w/yON/4v7LOF4sWxVA2v7bmUsNvf" +
                "Nfe4Jzbv/2fPwnTItaUx4S0WCveEUQ7n/B/nUI96/rQf3rfOgs" +
                "l6hiDSLKXEEWQDcLXhL9k90/clH8GX7/BcUflDSr9W8fyv7t7F" +
                "6i8/u3E4bYSv+F9d5o9F+IaXTM8EBniXPr2uEJBMIVZ6HD+v8M" +
                "FBUZRfsAgUDoArsu0SiqxWpdxs+qoKuo7Dz/iHP/KfgnJEl6dv" +
                "7Ie+yvrOqyP1r/wytuUNtfxUmZU3gYVSovjd/6OYw2+ccgj78A" +
                "y/D/kZtBdf6B/Sv/Qfdtc/+ayfmuOF0bm7+J2v1bZviUVDr/a1" +
                "zt934FK9QIIDQFxQn4y8LSQSP3B9HRLWLCI9fRzhxPvycR7Zbc" +
                "gRDf/1oUSJ1tsnyg+E1ATN5+aCIrZrWvEi2J8vS3RfH7T3ZOF1" +
                "JBCd+c/2r0M771v7LgjVN/EyapdIyvf8Do3+9TA48XmLxgUdu6" +
                "LWoNJzvri45HBQoqBAKB9BfpL0IMLWPq26oYWUKweBJlAmFyyH" +
                "He1h6/+Tnxu/v+J74WwxHOzL9NzrqW2CttYob8A6OsIPg=");
            
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
            final int compressedBytes = 1226;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuS2yAQBaJMkc+CSnkx2ZHPQahZ5RhzFJLVLOcIc5W5WS" +
                "x/JRkwEjQNUr9FUomNgOZ187qFLMayQEV/0zIYiNCH+s4gOu9Q" +
                "1YIprgA87+Vs3bPt7s/5+KHmrvnw/gLyts3xv8z4q9pDSTT7KX" +
                "Hlvx3RXJynbUYTqn1JZWHqqP5POVhYoQchyZreUh+6vSnlbmA1" +
                "K8fMWCE0cjzQ3u3BMgKhKASZoN79ogvpAgKhUV57JVGtzFZ+He" +
                "cIoGcBpZdP56zf/vj1GwPWb6jxx6Ffd7HzfyqjnyPsv8BQA3G4" +
                "Yx+lSxiK46X1RMjac//T8Vsc/hAIy/1PBfxPAvofaWjKQbLF/4" +
                "b9r2T9SJD/ZRNamfRD61C3uj0MW279tqCvfu0NKvdU+steOmEf" +
                "FXtgmpvfPeNe9iS1nXwTgu0epX0wnWY/94mOedn/9eXxnX1/Tl" +
                "+/cfzS+eK3pu06Nv5uOf5UvH9148AERGjpjYCf73YJECDJbZP3" +
                "0snKll2/6gGhH8yQthFBgsctIAFKfDcaaqh+QfYjrA88d2hbKf" +
                "836r+rl2lxNJfXLdvCDWD++UWR2L6C+Ycyw5hUovO6q3VdVDJC" +
                "kRxHFEyCKpp62fknnb9Yx/mRbq5WUdeWxnX/yvTfOMz/rZ8l7+" +
                "fPz/Pvg6q9uX9cyZEkXULI3elfIPbfVAkgQX52xYfv3/+pPW77" +
                "VP0Do//i9RutP7XHzD9a73/r9k9on+X81rb9D7/+QO1rrN80oK" +
                "iTCxwnxpsj7cXw02+O+6mcOR2F0B7bdPr+8bSMbwtgZpQK8sPA" +
                "Xn5if+ayPz/Znzn3b1zmzeHPwvH7139e/8pi2M9QgAJn4dB/2H" +
                "D9j9Y/bFwj/rGS/oMbvwg5+BOKX4H4HdWesGHllmH/j4t/rNn4" +
                "t974Gbn+C/STmjWKuvlTdf70Gv/8vvXkT47fLxNX7rvuPnS6+P" +
                "Th6o/J518ytJcz1buG51/i+R+s80cy0/zltX857b/EScZS9kuv" +
                "P2PzJ3P91haMXzXvP8K3KHT+7378Lrl/oq8ftn5BhI4xZmX+rl" +
                "bleeC/H1JXnmHz+J/DfjrafrKq+pEe/wN2WBzw2mZ+/xP+i5v1" +
                "++fj/1f49VMzWLgS/cFZs9CMbgkQUvWfCvivLNCeLWmfhAz5cx" +
                "H5DZf/JtcvgPlnlgxDNaSTN5x/bQAQOk1N9ZnBGUfkxU19edjF" +
                "kvjvT8F+/qX59qn7F/bz03j2W8H9N/z7H+j+2y7/Ko4fVH8gzB" +
                "PjHpnmydB4Js91x+/T8/uf3i7xe/T8/msfv/Hf+uhN9XP2z23E" +
                "SuXU5Yj9E4pldLqp0eauP5SofxgQd4QJZxq5/2zpZnVcuKkdbP" +
                "21bvRaO9wYSvZPZa9tYqRq4fh1rt0rbSsyMWo79fe/CuUfkGz0" +
                "178OBYTL+J31r+2dX6rL/gQCgdBu/Mvx+5tjyFn9476/D1s/WJ" +
                "fNftxtcBjUs1tcBWuZAA8emXjNAPLYk3F1WbB/AgHEh6osvVh3" +
                "sjevUUVoYvzh/nn88798Om9rfPsPD+4/xrv/LK3JZNu/i75/vn" +
                "n+r2D+Pv5LF//1nHETAusJdYqFng5eKA5qAJ0fypR/Rua/8Pmz" +
                "qjJ1ujl/DievkLVe2P55+aOL11/Yf2fm6o8=");
            
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
            final int compressedBytes = 875;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtynDAQFQpJyVVeaDGLpCoLJZWDqHwSH6XjE+QIc9TwMQ" +
                "zYfAQS+r63GOOxG6n/3UgAY8AUFUQwQCwe9qghHsgf5mAGnT2H" +
                "J/SvRkrZforhi+Ynfz/k7Qd10vtSN5FZ3BiN9CQ6uU7p6QR96v" +
                "q3lV8a/FOCPiF9eV50+ieP0krB/q+33yn/j9jKH97XlbUN/7Py" +
                "Iv+85Eb+5cjJbf2qJrbGk4/nAcr+xPJH7dctP40aZvyy+4808m" +
                "/UnRPkh/4b9r9u/3S9/Z+/foFEU6BD6hL4r2E/MHegjHg8yWbc" +
                "8mQc8gwatLnB/wAAALgIOHqpf9NtV9dFontb/VRt/1YN/Vu7GE" +
                "EXXr8Qh+bvbv15Ri8/0ff8857/fy299KCecw2rq7FDjg8Ax0tT" +
                "ZUbk0prV/Be0iynbD/j3nWQ+5N9Xr+snDtZvxGpL5qNTMwpklO" +
                "/4wUfD9Fchz/ufWvc/NfqfYAAA7AAFKQAAAOC6fgMAAEUkWAcA" +
                "AACA0hD3/nvk76gtJ9P7p/JZbNbG+uM7+pMe9ZfuxQsKJYJi90" +
                "eEjiS4f60Qj0zLum1DaOgQXPr4WcZK6B8wyp+Cj5H7sVGoqzqF" +
                "60qn3rhpQS/+qVbZyz/R+qUymCzFJPm+/6Kxf76Z9s8vIfV/3f" +
                "WDMPwLs3xJwzcH7F/n6P+oeGLH1vOLM+u/cEt1wZXj9vXnt7X8" +
                "8WwcFzTEHDn4yjG6uoMV/ZmuppoFeAVBxpsnAX/ylwciDdYPAC" +
                "A6yMLpWdzj17nr37b/gP3A/+OXf9jnL2wXU3KjnqrOS9CYHgCA" +
                "C/Fr9EhinIRk35iq9J/3uKSaEHRvDn9+Z8TbNd/fQzJ+kn/Zj1" +
                "fILwTqAGfXluPrwPPP2BZc7T8/Xu0QfBG4HjqJWcqT/mv3/Nq9" +
                "578+3Uf/n9Ff8vxXUbL+gcTzX1AIo3Y8X5TOv4PwqPdjJs+I3U" +
                "X/5+f2v7jbfy3w/hHALn9dsP9XRd641avtpUxFixa4sa9iSTG8" +
                "P7X6oD4a9A8ALuyv+PjZi6/aj5Gqch0/47j/I95WIE9QrHzKQ3" +
                "rQaUhbB6b3DZXmtBOWf7j9Gxm/v93o/q01/ikq/slGf7Q4/zdv" +
                "74+RLOb3xwTWX+j4dcH7A2Vs9afOOP5doL9D/cOUnk7K78bqxQ" +
                "Btcf2iMva//zdpnXs=");
            
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
            final int compressedBytes = 857;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnUty4yAQhoHRgqnKgkUOQG5CzUnmKF1zghwhR01ke1SyjS" +
                "UkXg3838LllIN5dNP9IyEsxMwktLBi4fZWXV7d/CIvb9/FCn39" +
                "JID9/wurX3vqn4Qw9+WtUOvydCn/a/r5ip/ytBSndfvr9T9N+2" +
                "OZYko+tv89tP1/Nr/ZnK3/0Ph5ypsE7Zf3f5LIS5T9PsPtRwfs" +
                "B2JZRaRbLCrlTw+Vb0xLHvFLpIxfj+2nnO0vMX6u4zkymbr+B/" +
                "sN5H9q1Ex0ON/IyuUBqElX/isDZCFHzQwAOCrQyoqcSTiffnaz" +
                "qr605GtunJz1s/yvn+d4RLn1u3nS79f61bX+z7l+k3gwTAHzgs" +
                "5RMpeoB6AFtQ1At/IMYPyK07VU0C/vn079W/bs/Quiw/svsqzf" +
                "2IxiCWXP2DdVZEEVKO4sM5M9+L968v9//vkj6A15CUTn33z7r2" +
                "rrLzoz/1b5hxjtP8lnv2P714L3bx28fzUltWkJ/SK60h8M9I9+" +
                "ZWv3/NFcwvKYPzzWP77x24meNnH9WH+DkeNXgvxjG8w/HPKnzR" +
                "bX+sCxtP9b5PM3WP+CpNgE0aNS9FEv3nsxMDUoS/H11xj7vzGT" +
                "wSkPoWWOJL1/J8PKy5X+P1P/Uh7+3/X6Gfqe5xpyGrjvACSOfL" +
                "j/lArNyH635z9+fy32u9u/5H3+Q/fjv/a1/9rFfwP7W9/P/S2w" +
                "mHGgNdq+FMJVerq67Y+4/2/7d5p23apAOHAe6fGx6wpuLva34f" +
                "aLW/tjy2cC55eAPSfU/fRn3PiL9X/K8bMYP5br5377j/kL/6vp" +
                "Pymf/6BO/dcxnr8tnP/Z2vwlZuMfv/7LoJ8t8tc2BvGnWPzpe/" +
                "1XYZ7IsvY/H39w/QC0rP9GuP7gOrZ/UVo//z28/cX0K9P6AajB" +
                "NPCPTDC0xTqwFAwYplK0MlUGOer5ldj24/w6kFhk1T1/2/YmWg" +
                "FImj/ssn7XVeoXKesfAeRZAAAokj/ZnR8LYL+D4PpxI/qrD/9t" +
                "138y7F80/OxH+6MQ+PwdYf6Hor1vh7n+QJHnZ1Ky8zNdm/5zkJ" +
                "ibF4fOXwR5sBWjKlXMyzKmfqb6Cfob+X90htA/m91O9h3Etrex" +
                "v1++uf5Sp9Zf7J4/GtGDW/G/1vsPQP/wzX/iG7xC4oA=");
            
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
            final int rows = 608;
            final int cols = 16;
            final int compressedBytes = 737;
            final int uncompressedBytes = 38913;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnUtOwzAQhicmQmZnoS5AYmEQB7E4CUcxN+lRaVpauugjjm" +
                "N7xvN/SFVV4SYez+MfN2mJ5hApj9zx6hjS/n1kPp1468zd9GjJ" +
                "/73iyfw9NfuhYXp8GHcmsZuzN4qWQjP/27+fXeOdJMw/IB5Vw3" +
                "n9Z8SPy48f1fbLzj9Bnv2GWvqFh/0X6AfPQz/kzj/X/oiftvp/" +
                "Hfvz9v9Q0P519Cdf/4H+UoSBCYpl4aL5MzI/Pm+GFcvW0Hg8uB" +
                "cIpkCwAiAHmxQw4ZL+DZMq3kfSdlJfw6R/h6P+nXJYvKF/rWrr" +
                "zxZZEZ4qWL8BkLF/EBfsH5gSLmvuZa1hedtQB0eHcnWaxMHIz3" +
                "R48dpc3DqtsUkb73WExezPDyTsP3O2X53+P5dy69/6/OvYX7ZU" +
                "ZBxn//4XT/63met/X0r0YyAwM6I9zAA01W/0f8X1a0zQP+dNja" +
                "V7HY6SxF5tmrbxeBJ+/PJL3KlQG4UfH/uvPevvULB+gjX0p1z/" +
                "leA/+ceJLfVn6vik/ZPz8bHT9St/nqNa/2XYv7Hzf9TJuevvoT" +
                "9AUv6qMx4AUE0/uEaTcY2N6US7wrGAt/FCAAA40NX+WxA+/8T9" +
                "hwXX/5Tuv6Po+ZfXb3LrL65/7UH/9V0/GuUPe/Fpg/oHKtHg+p" +
                "Nr+dNf919/yp/d3Og4Kj9+a/3CW/9r1y+B8fw1fP7Yf/1G/wL/" +
                "BSC5Maw+HoBu859TvUyecP+iPOR/f4d05S66/5X++U/r80f91t" +
                "3/SPe/9+Pp7P5MtI4eyQ/hc3rJTrE40nb39O2FoqHR08dxMZ/c" +
                "D71+31RQdqljWIQc4n/meA377wEumLnNEdE/Y/0BEAH0DwBAsH" +
                "5ctf621z+xWP+B76/VXTg1XL8tsH/p6vcvYT98f4MKHEwgE9w/" +
                "LTX/Mc2/qD+M+q/AeP2k61/cP6F7/gD6o73qzv/9LWb1R038I/" +
                "/0n78D5g+A2v4hCD9/wvkDAErwC4YG8G0=");
            
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
            final int compressedBytes = 3941;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWwmUFMUZ/quqz1lAV0gEhUXkWlg51iMQhJUAIsfKCiLKJY" +
                "pR5FAOUW5YQE4PhCT6VhCQSwQ8QECOxQshGrxACYhRiaBGwiEJ" +
                "RkXj5K+je3pmeo7d2YN9L/1ed1VXV9d01dff/3/9Vw1dCwQoWG" +
                "DT3nQGnAfpUANq6oMhg82HBtCQbiDVjX1kJ20KzSGbLbDToSWt" +
                "AX+AHPYldKB9ja7QDXrQ1XAz9Can9Z7aBDbSsmAg3A53wxCjE2" +
                "hkPlTSc8hPrA5ZAtXgV3QaXah1g1PmIrhEWwc/sclQj3bX+kMm" +
                "eRQaW9fZR8lT0IQug8vhN/BbNoHeDNfSWWQVdIGu7H3SGa63a7" +
                "OF9D56IyuAvrQH3EG/hUGkGjnJZkEAzme6PpQsgAugKlwIv6Yn" +
                "4WKoA5dCXTpVa8dGw2VkPTSjS/UjcCW0oE3oxfYRaA1tyFLytd" +
                "YF2kEncyudDt3hBnoaFkBPuAn6QH94HG6lNeEuGKwvAwY6bQEG" +
                "mOQ47UeDkAaVjQ+hClQnmwL59DaoRTpBbfIvts/8AOpDI8jScs" +
                "k3pC35ivaheeRP0BSugKvY5dDKGABXwzXQFtpDR7hOXw6dIZfN" +
                "hjwIwo3QC27Rj0I/GGBVg9voGvg93GncHgzSTNpQ604bwD20tw" +
                "laDxiGZY2ss8Egm4+5+sEgqY77TnpFEDd2LLAcS2uQa4NBq1Uw" +
                "qI2yWNCzWUetHMQr4JwbnbB2NpYfoJdqDcgSWWq2NQq0MeK6rY" +
                "1gZzBdaaxXLXRKo94W2QTjLZ7q78hzPVuULsRW+rECbF3ghU9Y" +
                "DUu3BqM2ejKstdFY0hz3N/VvxO/Pdq7oC0O1tO3iztOkg/deYy" +
                "A/2lXEtedV6/1kak8WZ1cFptEzqnZro4nVVeWfIL/QD4yGok6W" +
                "p8VfvO3ry50cuUuVHHWf6AYYivURL20WbefwS5sL9yK/htP2MB" +
                "oyyP0wDhrCGFLdzmcZxhy4D7JN/E3OL3If5ATqwkjjbbsL8usB" +
                "sz3nl9HG/soeQUaRcZxfEi/QjLniOREfya9gMNBC66Y9hC1dYm" +
                "DvOF60uzkOMrGksXjORdAE88gvHInn6M3eXpHOAvG68kzyS5Rz" +
                "vF7DkvOdmlBV4oX8GsX5ZfzXWMj5JfBaqn8r+WXg3ZJfOBZdYA" +
                "TwN2y6+I18vHI/55fCpiaeDxYt63RzCC9Iw34ivzheaR1ZTahl" +
                "FXJ+iZr4vnN+0QyF0Cp6LeeX+4yCX5giv+wanF+YzyMTHbw4v7" +
                "CE82ss3GnjSJgdPPaws2MPjZaQoedxe6h3Ia0D72rzjIe4PaSt" +
                "9VxoaVxNr4YcbKeDhaMXsoc4usfs0bQLlil7aHcFjWZDJezLY/" +
                "i0rTheJsFcgd6D42UN53hBPWt8oE0IL0wVXmgPh3F7KMrQHop0" +
                "guRXCC9zKgwS7PmE20N1RdhDhZeyh5xfHC9oxgbr33G8VF3ES+" +
                "XaSbykPcRzZQ8xdyvuaA8Di4U93MrtoYOXYw8Rr+s4XlhWW7Un" +
                "7KGxiC5w+Z4r7SFea+Xg5bWHHC/axmsPFV5oDwWG9WGSxAvy2S" +
                "6rGaSzN2GyOQfwjYApgl/dzNl6JkyFiSBsEbRku/GYA9OQX7sD" +
                "jRAvfB8lXmksIKyXl1/qzeqI78ZcyS/B8DEcL0xHmPX5eaCtH1" +
                "74BvjYOBz3fzp44X6HfViVHo/HL2kP4TLrI0wX6/+JxstYLPml" +
                "RvZ0OL9ETYdfOzxlaeKI/LL2Y8sReEl+mb3cJ/9zLH7h0cHrs0" +
                "h+oT18UOHVFB6GmfAQPKIfsKvCPJirf0AbIb9m6wfhMe1S6b/0" +
                "TNXCPhvfWfMRYbPFyAfCxjNND7yt/9VjoTvBLJWbxPnl6eUplf" +
                "6kfyjaeTTou9nP+ZXqH4njftGCaw/194IJNu6/RPq6/l0whY2+" +
                "4fus1Tle3hKYIXxypuliTrOgabyW9ffxrqCL1xy3JTU+Wj14yt" +
                "JQbywVpYtFm40sHRaJnNIbsbc0M+wsL7AnzEd3AmW5jB/xuQ1X" +
                "lzQO4cWEhw48FgMvf3551IHHf32SDF4B1Abse7+rVqPoMngy7G" +
                "yZGvX9fvcHpuAYdwyrXyDGONfsmxxenF8hvDC3xM09rcZxk+O/" +
                "YDnyS/kvOwcytAXcf3G8jM1kJ97RnNtD+0Joqc3n9jCwBDoYu9" +
                "Lqcv/l2sM1aVmOPXT0PJ6h/zItY5v0XyF+cXso9TzeOVTaQ+P1" +
                "cP8VQPsW4b+uj7SHuA8SeB2P9F+wAnPKfzn2EEuaaTiO0fbQyp" +
                "L+C0vQf4k0yn/hkfuvQ9J/SXsIlbk9hOqcX8aWaP+FePUPt4d+" +
                "/itkDx09bxQ69hD3ldIewlpSAKu0xbAang2sgDXwHLyoLdWegm" +
                "f0PG0RrEO9sdD+liDbQGhYbg/DGNU94nwRAy+/8O1y3xFzrof3" +
                "PdxS4b/Spvu/cYFmyfFL6o1k7aG20pfLg5K2h9/HsoeGD/O0XE" +
                "/+6cStO/owjHkvqBE97PDL+HuIX4HGQs8jv2ADWcH5ZRzh/DK+" +
                "EPzawPV8Wh/OL6kPHX6JX1N6nvMLyxW/8Je2WUaIX+ZAyS/xLP" +
                "XE0Z9fXf345fbC5Ze/PoT1/vzSD/vxy9GHifmlaUXhVxjS+Yn5" +
                "RSYqfn3p4ddLkl+k0KgbyILt8DLZa9SHrbAZtsEW9F8byXvaRl" +
                "HjY/IR2UneFvmtBL886GSCOBH0VOQdclCU7xbHA2QL2Ybpjqg3" +
                "Zj95hfwl5L+8eiOi5r6i+n4Pv2Ylyy+jTjClTa8S64o9IQEz85" +
                "PokV/ZJodf7AuHX4hXIaSbw8yh+nih53dwPU9B+i88z7Zncj2v" +
                "T+L+C/eRil+vePnl1fOcX7rqgYFIevh1T4hfqqYPvxL2rG+kPY" +
                "zU84Jfo8L5JfHy5ZdHz4vzGHpev8BPz4uWt0Tr+TCkJybW8+yI" +
                "asvLr1clv4zDen9yEb7XNe3ZxscUe2v+QG19PL4JldQbkc7xop" +
                "bImxQ5ouebP6oWdyXxLiJetIrEK7SZP/vXlngVk1+flBm/Ympm" +
                "Y0uCOycmbt3Fq9Bjdy52+IU9Fvyy51g30KoiHmVjWQatRDIEvx" +
                "RepBZkc7ygJd41L4xfD8Tjl8C5SiS/ZDwqnF/67JLiF1mQgF9/" +
                "S5FfFxWXXzLem+B7uY2Dl4dfYyW/6DWh+IZxDMvTsc26HC/rMx" +
                "nfoL8TLXviG+Io4hsi54lv+NpDjyoM4SXjUeF48fh8ZHyjlPBK" +
                "1R5eGQsvzKWMl6ftEF4qvkFH0HslXqS+cYIOg3TrJB3E/RepR+" +
                "8SeA2nd9MhpAH3X9YJ4b9W0KGSX0nhNcEPL+uUv/8qGbxK3X+1" +
                "LC5eyfgvP7xIQ4kXnil+Efy6p6NQbwj/xe2h4le6wNUimY491F" +
                "fauZBDGjt4kaxEeNEqkXhx/1V+eJm1UsRrQFnj5fALkbsc92wQ" +
                "MUFyBWluo0LnegPriBgRnRflM1cVQQ3M0n0VLteHxd+c+EaE3j" +
                "ierN4wG6eoNwYm2fsZ0Xojfvww4v6o+CFpAQ+TK8lVni+IUw5e" +
                "qoZHC3F7iFefSR0v61SwhLaixg8FXtkp4jWsrPEirq1x4vMiPw" +
                "/mBupxvEgbh1+kddRvri4Bfv1csngVTc9ze5gSXsPLjV+F7HxW" +
                "GdOuZK9xjHTBvgwLzED/tYio2Yno+Ib+bPz4BvN8/cNCjpeMby" +
                "RjD1OKb5TC95d/fN6cmeTdBUXDC3IjzqPi85hT8UOSZ5xw19vI" +
                "+IaIz7N0Va85ZAcWCH24RsU3Oih92MNPbzjxQ30CVIqvD73xw8" +
                "j5ZXEWNb+cbPxQ6Y2o+GFMfZhk/NCc7xc/9NMbkfFDqTfixw+D" +
                "Kj4fpjfc+Dy7kPDxXg3PYn8uIjeSm0LxDVjH4xtSH/L4vIpvrC" +
                "uS7fDow7B3vWrFtYfmH4ttSScW7z7SU6W9SD/Sl9xCerM80oeJ" +
                "1SyR8SiJlyce9XxJ4HWu+i/WNfFvWm+VOV5q1RGsxXx/h1+YHw" +
                "AvCr1xq7i6jvsvvp7Nmf8S/usFEiNuyO71xwvvi/BfMZ+rTPwX" +
                "W5zaO2K/VW78KsT9NpHb62kV+RVHb7yYQG8Mj8QrWm+UL16p2k" +
                "N7T5njdbuDl6s39rLxXr1B3uN6gwxx8OJ6g+OFemM92QU5ZA90" +
                "4HhxvUF2i/W9Ai82ho119AYbJ/WGg1dIb4iR9tEbEq/S1hucX6" +
                "nojcDYMtcb7nwlEHIPGUmGS7zwisCL8wsaePkFzV1+bZD8knjh" +
                "HT28/OJ4iWfleE3h/ArhFYMh9WLxyw8vP36F8HJLL8C2lHWGOu" +
                "H8gmax+AXtVKrwUmcuXuJMrGgO4YX5yuLoO8cCjfz45eCl6rSP" +
                "ukvh5RmXESoV852wXdXbSqbANm4P+fyyYw+5/5L2kD0u8HrJ8V" +
                "9ErZIOt4fR/utcs4d0abCctmLbw8kqfRAIHqeTqaLvvvxSeqO5" +
                "qzc2Srw4v0Sq1s6wJ714ye8vya/4euP//EqCX9NUD5535ivF2R" +
                "Y+/yXj82wTmeGNz/P5FBWf3+SsB1Dx+ZlFj897/Ve8+Ly1vDTi" +
                "89bS1OLzYXVixufZ6hKMz6v1AGw728ZedXu8w9GHxF1XS1Ht0S" +
                "Fhv7mZDi3a91fxN2tjQkuxruj20Fpf0eyh29vPyKfkcyJ6Sg6H" +
                "9Dw54tY4Glb/EF59uajfy6W5FQevVPV8OeL1PTlDxFpy/QD5ga" +
                "eBZQKvs/rBaLz0fQKvuGtKvOvnk8FLrp+PyS9fe+hdPx/CK/n1" +
                "89aecxUvvn4+bg9cFaAfwDPsR5oooRP1g3RSpD3UhRqgk+PZw0" +
                "i8Evbgw6Lbw5Txere88Eq0/jARXqSQPqFye8PaLYgd38A0fnwj" +
                "ajSKr+fj641wvBJvLl7lpufpk6ndzw6RM+r/ehu5PYR06b9A/C" +
                "NQ/P/LtYd8fRQ5hPpwa4Q+zCr6eptk9WGCb68wfShKktKHMeMb" +
                "JaoPS2O9DTvB129QtTKR/TsUn2enY8bntyV4Bzxz/XJ+uWzi88" +
                "nzq6z0RmmsB/B+f0l+ye8vh18SL/ad5/trRzi/6J6Kxq9U10eV" +
                "Pb/oO4pfZzH/qfv+JcevV5LnV6rzX3ZmafDLblDh4lFufN6Zry" +
                "R7qef7K+58ymsJ9EY5xQ/PPXtYcnjRz90REnjRL3leM0P8ijO/" +
                "XKT/JJyL/stuVlHxgrXaefQrZ35Zy6BfJ7F+442KjldF4pdWQ7" +
                "Cqpwe7f0S16lkvqkWt/dJ3ll08yu5VKvzqWXH9FxD6o/xeNuon" +
                "OV+5K958JSIsZ0E88yllM1+Jadh8Cj2r8lHzKbHWH57785XwP6" +
                "N9anY=");
            
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
            final int compressedBytes = 1627;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWm1sFEUYfmc6s7d3LQpYAwSxBaqIqIAiCigaEQREQQ2CpX" +
                "6gsaA0oQimydHUL/SH4ge2EBU/sCH88POPMdo0Eq0WgdqkVSxi" +
                "pEZFSDFiLX7EP+u7c3vb3bu9r73du9tmJ5mZd2Znp3f79H3med" +
                "9b0gqErS4KKgrpUhQ4E+sYOIeHcdwJ5ytYyHfkG9JG9sM0tD8m" +
                "HyoKbydfoH0Qricd5DDecStpFyt7yEekRVHYPeoI1sKDas83Qw" +
                "k5RD4hB5QEBSoGbdJtujIruBXb+cJeDDda3n0f1jWaHdJnRyoK" +
                "/U+zx0dnizaJ8dTg0wk+yXVafwssM8zeifUurNViVCRaCQL69W" +
                "GiHW2542TzmNeL2cuxztbXzIu76za4HVYaZ8jjkZ49KdqD2ugp" +
                "taVBHmYdtCQyR0eIVhZtgEr4N/cpGRS+Ge87Q3GtkHfSXxvBS1" +
                "FCY5UcFLbF4mnUZ7lnE2tk2+N2DRtW7Ii7+mUG+3ereGVeaHO6" +
                "K1X/yhQvuivtz9/lLIZ28WJfa5+8Fr8xBRmCYlQD6E2smYfhXB" +
                "xVwySoo+vpWiqYDaaL9kq+n66DuWhtEOObKXonrIA7DM/wXlit" +
                "WcyIF5TC2YZRuem5X4D1Qs2+GOulMDMlVqti8YLhunWW1o+Fh2" +
                "ACTIzgBRdF/QtmwBXaiqvgas2qhYWm3R6G5VBpGD8gWm5aUyxa" +
                "nUFgHNYyzT4P6xQzXnAJXKavnQPXwLXYL4AbsF0ES7Bdati7Cu" +
                "7GFs8X2gH3C9z2kCqyiqykx8T3GYjyIdr9CfnwQIr/4lO55EM7" +
                "/hVaoOSp2PUv2quxfyt7n4vznMR4PunUek1vCFvoDeyF3sAq9A" +
                "b2Zr3xXtwZk1RvmFZ25wSvhXnD6yubp7TGV+wz9qnFrmH+hM6c" +
                "bbFXJZKp3nCz2NIbN3nNv3Q0DlnuGubv6iu+jcNrpOfxWupVvJ" +
                "APD2tWlxEvKZCYD6XSFHzYE4uXm3xoBy/6uWf964g1H0rDkvDh" +
                "8tz5V6jKFf+q9K5/ISJH2Y+qf7GfDHiNSOJfa1L4V2+h+1dgnJ" +
                "fxsuTDFUnwqkuB18+Fjleo2qt4aQz3mEnrT8aZGmFhvEfXIwK/" +
                "KAVbvOVfdEqW59dxdpL9GgiyPnaanWIn2G9sQGrE+Q59xV+m9c" +
                "ds/ZV+9kdB6cPavOE1P1s+lJrYP4ESIx/SeSpeWcTLf+cyXvaY" +
                "f2WNFw9xOT6/Eeizi5fUzIM+XgnxWpL9HkCx4jOWdg/OyRP0E1" +
                "JX9pF8r2bPjdlDz/fSxWKs53tNq9LM98rlar5XMNeeVJ9enmja" +
                "ZXjct1Nzu3q+d1DPW+V70Y7JVIEpdkk335suXjAHq8j3itEi0V" +
                "rme+WKSL43og+lfifzh9yAibwxO/8KvZXyv81O/jBv8Zc0YO8+" +
                "eZPOh6Pi9TwyxizSGZhtjRdtKKR8r8f48FGndwwIBGT9t1FVz/" +
                "My07du97qez59/Fe3L7n5Vz/NyeQzr4+Ojel6Nv0Jl0fhrKOp5" +
                "78ZflnuqeM0QzxlVBxmdT+8J7U0PL1LqDf/KFi88v6ZZnV/J9I" +
                "ZcmUJvTDV4Vrfb55cdvZG+fzn9/oZdPoy+vyH4cKZcY8xv4Oxr" +
                "xZOwfdWJ/AZ7Ixs+DH1voZXWeff8Ym86EX/JG8AQ49KgGn/xBm" +
                "2kv78B0yPvbySPv7SxKf6Kvr+RefyVhtK1EX+p/pWf+Mus5zOP" +
                "v8yFPxLFC9sSucWMV/R9G9qQQdyw0YiXG8VdPiwUPR+Nv/gWcl" +
                "pnln8H9YbmvareMOXmyRHFw8X7+pA/o/6+HNyWhHHblCFT8o+X" +
                "3fyGQR9uxfN3maPvs/U4l98oLnUDL+/lo3T/etbq/Y3C8a/iUU" +
                "MLLwfyAy+nuL4TUX3O58MCQauV7+CNfBt/gXTxJv4Sf55vz5YP" +
                "+YvO8aE7eHnZv/grqf3L1xsewvP1vJ5fFb5/mdB4Oz7+ivEvP/" +
                "4qqPNLt5z7fXmvg3q+zvevJN4m3iuP5qO0iDwuH2Ujqredjyqu" +
                "9/HK8Fmrvy//kLfzq8nHKxEf8l6H+PCor+dzgFzC8wurBV6i7+" +
                "DH4/Gy2Nv++bXTxytTvcFPZOZfjuK1y8fLXX3I+/hJB/Ha7eOV" +
                "JpImPtRnY/jQpC5/T+RfuNIuXh/4eDmPF+93iw99vWEPL/5nMr" +
                "xcPL9afLyMBf4Hcyf6Zg==");
            
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
            final int compressedBytes = 535;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmstLQkEUhz33MWmE5Sooat+mImgbFe0raBO1DUqJXv5Zra" +
                "OkxyIKehAYRY9FQbSPUFAsuulwufm4Rsq9dM/4O+DcUUGY+fjO" +
                "nJnRzFh2UNqqCLqynw/F1zGdyf4e7cjnif3dJd3L56ls72iXUp" +
                "ZL0C0d0rn1h6Bry8fQk5ayYWbLeTnzWcWrarbr8Cp+A16eBO03" +
                "65eZg1+B8isPvziGtlZkV1BjLCrkQ/PLeDI/zQ9KGy9u+dC06M" +
                "YtH9JFvXxoPCMf+hUipKf0I2c8B+5+afGqzxLwK7j1RqN+CQ1+" +
                "+eaXTllnrvKuM/ha8e4R69e/8jLAizE9E+tXwAkJ+MWKVxi8WN" +
                "WHOcqK9lpeVODCi7Zbyq8o/GJMr1NWExHZdti1RUy2Ydm2aaKZ" +
                "39Wi4OURoS74xYxYN3gxotWD80NmxHrhl0pR8kv0qTEWVe6/RD" +
                "/uv7jwcnre3acMgNc/rm+D6oxFBb/EiBj+zS8x2rBfQ/DLR15j" +
                "YlJMeJoPx8HLx3w3hXqeOcHpino+VtZv8vwQvHzlNQNejPfLZb" +
                "zELHgFyqw5SQj3KbyMitT6JeaRD5nQK50fLmC/HJT9lzw/XMT5" +
                "IZMVa6liPJmffKi/N7t+6W9Yv3zjtdxK/wdQoCJcAS9WvOLgxY" +
                "pXArxY8VoFL1a81sGLFa8N8OK1X3Z63t1XbgZ1v6yAX1vwixWv" +
                "JHhxitA3DxRTpw==");
            
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
            final int compressedBytes = 559;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm00rRFEYx53rnnMolMLC2sJGPgM7ZcUXYCOh2NCYwcLr0n" +
                "uGL+AlSZTQpDRRmNSIvCwoWbCykRR1XWeYXOSte8x9zP+p83bn" +
                "dGd6fvf/PM+ZZizrtYnmp95IV31G7JqRrfo01UtDWL8wI8uCuW" +
                "Kihd28zNndRzvYhWN1Ap8llJcPvEjxagUvUrz84EWKVwC8SPFq" +
                "Ay9KxkKiT3SJHtHNopbFg6KTjz6/svs8HrN9FmZbar7CltS4Yb" +
                "cdu0XYkVpvqv6QLbNV+y5D797lgK2x7W99nr0k0krvD/dXf2vX" +
                "IJ5qDzEe+vKsWwIveSoeTvJhPsgHWJSP8CDvRzz0OK8p5C86+Y" +
                "uF7HFaeSr6xnO/5mVWgZc+fcVnrvESM+Clk5eYhb4I5a951BtJ" +
                "Hg8XwEujvhZRH9KpD8WZ42zc4FjVqL7RbnVvztD1eNI9+xRcwg" +
                "feyl/mqbh6yl/muUv14RnioS4zmt7HQ3HtjIdGLeLhv64P76Ev" +
                "UrwewIsSL2mAlz5e0nSbFy8Gr8SZzIQPSFWQ9nlZ5sAP/zh/5S" +
                "Ie6uQl89zlZd6CFyl95YMXKV4F4EWKVyF4JYBknJfdwvGrcV5q" +
                "jDgq/6IYrw/vBl7e1Vcp9EWKVxl4keJVDl76eMkKt3mZc+CV4O" +
                "+jKuEHz+jLh98fJnn+8oOXRn212R5uj/HiQRmAvv7KZMfP9sf+" +
                "nyLH3NZXahZ4kYqH4+ClTZMT5vrnO8wwvOQdS3kEnz7yvw==");
            
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
            final int compressedBytes = 649;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtml1LVEEYx3s8M2cG+gB9C79AXaR4GXbbRVBeRUV4aXeSpq" +
                "AVlWnZq0SLV9FlVFgh9uIbwlbSC7mVKYYJZVlGN6fjuK7uwdg2" +
                "59mdwf8DZ2fOYXZhn9/+n/nPzFKfuq56VCqKKB3lBY0tt+oGva" +
                "ABGjLP7tJt0z6Or5H4GqVX5v6JeX1Jd+he/I5rUSJonB7QcPQP" +
                "Qc8ixIaioj4m0I88uBHUl+v9RV/0umh9PYK+SsFLPbXBK2gIjo" +
                "JXmevhIPLgiL4WaUFlf9P0a83z37neVN74N859g5uu5lalWT71" +
                "efBwpR/cX19fFYcSzw7jl+50PRxHHlzxGyojMmpiyW+ISUv+8C" +
                "38Rhmr8HvkwC0/H+vrg019iXfQF5t6JtXH1btgBH7De6LTyIFX" +
                "vOaQA7fmL/UtrLK6fziP+au86y+9FXlwI/Q2UeCsRAwgS27Vw2" +
                "zPWj3UlaiHfLz0dnlOnpVnKC07Zbc8Lbs2ykt2gBcjrx1hc9gS" +
                "Hl/Sl+wOm8CrVBG2Fjm+jqke1oAXo752hdV2/Xy4E7zYeC3Sgt" +
                "6d7eO80q73ruXxhyKj92C/148QnXpvgRHnkSWvVtT7kAOH9DUj" +
                "Puv9cTur68QX8UnMie+JET/y7v5rt17Mi6/IdYn0dQA5cMnPG7" +
                "9xEH7DK15HwMubetdYYO7B/rxj+sr27O1HHYO++HjpVuu8WsCL" +
                "kVebbrfLS9wCL0ZeJ63r6wR4sbmNU/g/wKb3G13QFycvfWGZF9" +
                "Zfm1RfF8HLK16XwIt5P+oy6qFH89cVy+uvn+DFFWJGXzXtbHyZ" +
                "8y/dkxiB8y8/VmapFX3FV24Ntqov044m1GH0ta5uoC/uSjlWLC" +
                "/dC17cseUPc4BkXw==");
            
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
            final int compressedBytes = 587;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmrsvBEEcxw07szuJUiUaEYVHCIkCjedfoJAgEoWGQjQaof" +
                "BqvUNFQSIiHpVXvA8JLpLzCBrEvyEx1h7CcZe7s7N27r6/ZG73" +
                "brf6ffYzv9/cDtkS70F84luQ8/fjHbkiHnJinW+QVet4ZI4zc3" +
                "jJrfX92Pq8IetkUwhjTgQEuSY75FSEEeRCIKIKY/6Dlzk8n/n8" +
                "5GUdvQHZtnj9ygG8bAkiw68F+PWPni0iB+7yS7s3lt780p7s8U" +
                "t7gF9KzYfL4CWTl7Hi5wW/YqJ+rSIHbvKLvpj1a40+B/OLCvgV" +
                "4/VrG7zk8TL27OalLYGXUn4dgJdEvw7t5kU5eCnl1xF4SfTrkY" +
                "7SYTpEfHSMTtJBOv5nv0bASyIvs4vXu/x+0Um9E7ycCr07svtZ" +
                "kxA8UdsPfZfmwVPt5vrF2V/94hR+OcOLc25gPlQ7eDJy4Ca/eC" +
                "qrYOV29vOsDH7JnA+1e56G9ynxu17WD+Xy4ulx71cG/FKHlzFh" +
                "PrOZeP+lVBeYFfRKNrIT2/WL58AveaFfJu1+nCdt/7ye2GaOlo" +
                "DfWv91LsiN+34jD/2GklWsQAW/ECEZFiIHSvEqRg7cVL94Sej+" +
                "kBVF/P9hPuqXUv18KXjJ5cUrbeVVAV6SeVXbyqsKvCSvv2qw/l" +
                "K1fmH/hsv7w4avvHidDfNhPXg55RdvBC/XroRrw7qrWYnnLgXr" +
                "r6j9aoVfEutXO+tl/azPvx+b9WD/oVPBBiK8v8ni1aFP67PB/d" +
                "JnIt5vMwVeDlW0np+/4X2KmyLhFbj0mvs=");
            
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
            final int compressedBytes = 483;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmksvQ0EYhk3NOGcmNiKR2AjWErZYWGAnfoM1Cz/A0g9waZ" +
                "taiIRiIyFBROuycEuoNCoEm0r8CAsWx3GISN1zZpyZ6fsl59pF" +
                "m/fJ+873Zep5PxUf9VAGFY9DA32KbL/dFUo+yb9eb8gF2SfHwX" +
                "2GbATXQ//I+ccpuQ6ej4LzFdkkWZ9w4sO3XJJdcvKr33MOJt/z" +
                "4inZvFgreBnlr2nwMorXLHip5EWLfO6ZF72Tw4vegpdKXjwt11" +
                "/0HryMysMF8DKK1yJ4GcVrCbyiq9iwT2AZOljsrxX4yyheq+Bl" +
                "FK818Iqu+Do0sNxfWfgr4v5wCzoYk4c70MDyPNxDHqrjxQ9k88" +
                "J+ZeTrVx46GMXrDDroun7xqtDrF0Me/g8vzp0Rlgy9fk2Cl0pe" +
                "/MGZ/7rfcNJ/5eXMgJdaf/FHmf0heKnlRYvCxf9tjOJVDV429f" +
                "OiBjpo5a9a+Msqf9VBB13n5fD+cpPwl0pebuqFl6w8FPXgpapE" +
                "w/unytzneRgbLHk3BOUinZe7ZeahaFTrL9FU9rx6pe5X9ijm1V" +
                "z2/XwL+nmr+vk26KCLv0Q7i7MJNk4KLMGm2Bj2U3TPQ9Ehd/6i" +
                "A+Bl0rwsOsFLYR52IQ+tmqj7oIHledgPf6mqiic+Munc");
            
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
            final int compressedBytes = 430;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmL1KA0EUhb2yY7KzTyD4Dj6LkMZCKwWt8gL6BBbxL+gbKI" +
                "jaqKgEEaKCPwirkRjQiFGw8acQbMd1SaOFCJnh7h3Pgf1rz8e5" +
                "98xSxbRFsfkmOm8/G1SjKh2n7zu0lT4Pk+s0uc7oOv0+Su912q" +
                "ZdY/SA+SG6oj06MX8QXRjoN38qumCbV34KvNzxcpCvQfASxWsI" +
                "vPjUXUwIDMMHj/M1gnyJ4jUKXqJ4jYEX8/4ahw8e56uIfDHnaw" +
                "I+iOI1CR+yMg91Sc2pGTVNsZpXi6qkyp3OQzWLecgnvQAPstw3" +
                "wp5O8xUq5EtUP1wCL1G8lsGLuR+uwAeP87WKfDHnax0+ZOb8tR" +
                "E0c7df+QpadvKVu0G+GM9fm/BA3Dzchw8e940q5qHD/XVgm1ew" +
                "Bl6M+yuGB6J4XcIDUbxq8MDzvlHH/hLFC+dlWbzuwUsUrxZ4ue" +
                "QVNPWDzf+HwR14ueSlHy2flz/Ai7HPP8EDz/fXM/IlitcLeDnu" +
                "G6/oG4L6xhv6hhxe+t32PFT94CVpf0UEXnyKQniQtb4R9aJviO" +
                "LVB17/d3/ly+DlSl2frsIrDQ==");
            
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
            final int compressedBytes = 362;
            final int uncompressedBytes = 18577;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt17tKA0EUxnEPLGwx7yDaWIk3iLHx8iRGtLH1bUwRJSTWgq" +
                "KoBBWCAS9EIhG1UfAhbNd1EQuDqJBx/Xb/B2Z3wxYJ58c3JxNF" +
                "35UbjKh/U9b4eOp8etN+vz9Y15p2njwf2n5yP4vXZbyu7D753E" +
                "qud3ZgR7HwUM+33NqxXfzo99xg8udew3j59Aoe3cibV/DcH6/g" +
                "CS9/Xm403AzrX+crrP3WK9zAK71yY/RAymucHmT8/8YE+6GU1y" +
                "ReUl5TeEl5FfCS8prGy/N5uch5WcfLzfQ3X8ELXimev2bpQcbn" +
                "1xz58jy/5plfQvNrgfmV6/1wES8prxJenufXEvMrx/laxstzvl" +
                "bIl5TXKl453g/X8JLyWsdLyquMl5RXBS8prypeUl5beKVXbpse" +
                "ZDxfO+RLymsXLymvPbykvBp4SXmd4CXldYqXlFcTrxTPXy16kP" +
                "F8tcmXlNc1XlJeHbykvLp4+aqBVxzmSJ0=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
                "eNrt2MENACAIA0D3H5q6gQ8fBuLdCFIBTU5qQWeRXwDMP/NPfQ" +
                "AA7EeoDwDmC+ovf+rP9/mQP+cj37hfyA8AAAAAvOV/CwAAAO9P" +
                "APR/AP0XAMD+NHd/sv8BAAAAAAAAAAAAAAAAwIUNI7zGPw==");
            
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
            final int compressedBytes = 85;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3LENADAIAzD+P5r2A6aCUGUfkGwZEwEAvHZK2Z7f3Q8AAA" +
                "AAAAAAAAAAAAAAAACwnf8FAPtv/wEAAAAAAAAAAAAAAAAAAACA" +
                "Sf5P4GMXlPy5QQ==");
            
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
            final int rows = 608;
            final int cols = 8;
            final int compressedBytes = 49;
            final int uncompressedBytes = 19457;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0DERAAAIBKDvH1pt4OjgQQQSPutVCQIAAAAAAAAAAAAAAA" +
                "AAAAAAALg2vdEe4Q==");
            
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
            final int compressedBytes = 152;
            final int uncompressedBytes = 14881;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2EsOgjAARdH6QQVhWbiurhsUXIIOSd/JmzK6J01Ja6k/Ni" +
                "+1WBsrf3zDm7fxNt7G247t/brrFOV90inK+6lT1P391inKe9cp" +
                "ynvTKer+HnSK8u50ivK+6OT+Nt7WhPdHpyjvVaeo/7Vepyjvq0" +
                "5R3qNOUd6TTlHeZ52ivHXK8r7p5H3Nmj3fD52cb+Ntx/cuX30H" +
                "gxU=");
            
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
