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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 1, 47, 30, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 16, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 3, 0, 13, 4, 106, 47, 155, 156, 5, 157, 158, 0, 6, 159, 162, 178, 26, 7, 179, 8, 160, 161, 0, 163, 180, 168, 181, 9, 169, 10, 97, 182, 183, 184, 11, 171, 185, 47, 12, 172, 13, 186, 187, 188, 189, 190, 191, 192, 47, 47, 14, 193, 194, 0, 15, 16, 195, 196, 197, 198, 199, 17, 200, 18, 19, 201, 202, 0, 20, 21, 203, 1, 204, 205, 74, 22, 2, 206, 207, 208, 209, 210, 23, 24, 25, 27, 211, 212, 178, 179, 213, 214, 28, 215, 26, 74, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 74, 226, 29, 227, 228, 229, 230, 231, 232, 233, 234, 235, 86, 105, 236, 30, 237, 238, 31, 239, 240, 241, 3, 242, 32, 243, 244, 245, 0, 1, 2, 246, 247, 248, 47, 33, 249, 250, 86, 251, 181, 180, 186, 149, 13, 185, 187, 188, 189, 190, 252, 191, 192, 253, 182, 4, 5, 96, 6, 254, 255, 34, 35, 256, 105, 257, 193, 258, 259, 260, 194, 105, 261, 262, 106, 107, 108, 112, 263, 115, 120, 122, 264, 183, 196, 265, 266, 267, 197, 199, 268, 269, 106, 270, 271, 272, 273, 7, 274, 8, 275, 276, 9, 10, 277, 0, 11, 36, 37, 38, 12, 1, 13, 0, 14, 15, 16, 17, 2, 278, 13, 3, 18, 19, 20, 4, 279, 21, 39, 23, 40, 24, 177, 41, 42, 25, 43, 29, 31, 32, 280, 281, 34, 30, 35, 282, 37, 283, 284, 39, 44, 40, 41, 285, 42, 45, 46, 47, 48, 49, 50, 286, 287, 51, 52, 288, 53, 289, 290, 54, 55, 56, 33, 57, 291, 292, 0, 293, 294, 58, 59, 60, 61, 62, 1, 295, 296, 297, 63, 64, 298, 65, 66, 67, 68, 299, 69, 70, 71, 5, 72, 73, 300, 74, 75, 76, 77, 78, 79, 7, 301, 302, 303, 80, 0, 81, 304, 82, 83, 84, 85, 87, 305, 88, 89, 90, 306, 91, 92, 93, 307, 94, 95, 96, 308, 98, 309, 99, 100, 101, 102, 8, 310, 311, 312, 313, 103, 9, 314, 315, 316, 317, 318, 319, 320, 321, 104, 105, 107, 10, 108, 109, 322, 110, 11, 111, 112, 113, 323, 324, 114, 115, 116, 0, 325, 117, 12, 118, 119, 120, 13, 45, 13, 326, 327, 121, 122, 328, 14, 123, 124, 125, 15, 126, 127, 329, 98, 330, 16, 128, 129, 130, 21, 131, 132, 16, 331, 30, 133, 134, 332, 17, 333, 334, 335, 135, 3, 336, 4, 46, 136, 337, 137, 5, 338, 6, 138, 139, 339, 340, 341, 140, 18, 342, 343, 344, 345, 141, 142, 47, 0, 143, 144, 145, 146, 147, 346, 148, 19, 48, 49, 347, 348, 349, 350, 351, 149, 150, 20, 151, 152, 352, 153, 50, 153, 154, 155, 353, 354, 355, 1, 356, 357, 358, 359, 360, 361, 155, 362, 363, 156, 158, 160, 161, 22, 13, 157, 364, 365, 366, 367, 368, 369, 163, 51, 370, 371, 164, 372, 373, 374, 165, 375, 376, 377, 378, 166, 379, 2, 380, 381, 106, 167, 382, 383, 384, 385, 386, 168, 387, 388, 389, 390, 170, 171, 391, 392, 393, 156, 172, 394, 395, 396, 397, 16, 198, 24, 398, 173, 399, 200, 400, 202, 401, 203, 205, 402, 207, 47, 174, 175, 176, 177, 23, 178, 403, 404, 405, 179, 406, 208, 407, 408, 0, 180, 52, 53, 124, 409, 126, 410, 186, 188, 411, 412, 16, 211, 413, 182, 414, 7, 22, 80, 25, 27, 415, 30, 416, 417, 418, 36, 166, 419, 38, 215, 57, 0, 3, 420, 421, 1, 2, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 206, 444, 445, 446, 447, 448, 449, 450, 451, 452, 65, 453, 66, 454, 455, 456, 67, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 26, 27, 36, 471, 472, 473, 474, 475, 476, 477, 7, 478, 220, 3, 479, 225, 195, 480, 481, 202, 482, 210, 68, 483, 484, 485, 486, 487, 488, 81, 489, 82, 86, 92, 212, 490, 491, 492, 94, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 183, 514, 515, 516, 517, 213, 518, 519, 520, 214, 521, 522, 523, 524, 525, 526, 527, 528, 529, 95, 530, 226, 531, 532, 533, 96, 233, 534, 535, 536, 235, 537, 97, 538, 98, 106, 539, 193, 194, 540, 196, 541, 197, 542, 201, 543, 544, 545, 112, 113, 114, 135, 546, 4, 54, 547, 133, 141, 548, 189, 549, 550, 181, 551, 552, 553, 554, 555, 556, 557, 5, 558, 559, 560, 561, 6, 562, 8, 9, 563, 10, 11, 12, 13, 564, 565, 566, 567, 568, 142, 569, 149, 570, 151, 236, 134, 571, 203, 572, 205, 573, 574, 152, 575, 576, 577, 578, 14, 38, 579, 580, 581, 185, 582, 583, 198, 584, 585, 586, 587, 588, 238, 589, 153, 590, 591, 592, 593, 594, 595, 596, 597, 598, 154, 599, 600, 601, 602, 159, 603, 604, 162, 605, 606, 607, 608, 8, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618, 199, 200, 619, 206, 620, 127, 621, 208, 16, 622, 623, 624, 625, 626, 627, 55, 628, 164, 165, 629, 630, 631, 166, 632, 167, 169, 170, 173, 211, 633, 178, 4, 163, 168, 634, 635, 9, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645, 171, 17, 172, 180, 646, 58, 182, 184, 186, 1, 191, 207, 59, 209, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 647, 241, 648, 227, 649, 0, 0, 74, 57, 650, 651, 652, 188, 190, 211, 225, 226, 60, 228, 229, 61, 243, 653, 18, 654, 230, 231, 232, 233, 234, 236, 235, 655, 656, 657, 237, 658, 659, 238, 239, 240, 241, 242, 63, 660, 661, 662, 244, 663, 664, 665, 243, 10, 245, 19, 20, 666, 667, 216, 668, 217, 669, 670, 671, 672, 673, 58, 246, 64, 674, 675, 676, 677, 247, 248, 5, 678, 679, 680, 681, 682, 683, 248, 249, 250, 684, 69, 685, 250, 686, 687, 688, 689, 251, 7, 252, 253, 255, 690, 691, 692, 256, 257, 258, 693, 259, 218, 70, 260, 261, 262, 263, 694, 264, 265, 266, 695, 267, 268, 269, 696, 8, 270, 271, 273, 219, 220, 71, 221, 222, 697, 72, 73, 128, 74, 75, 76, 272, 698, 699, 254, 700, 223, 701, 274, 276, 277, 702, 703, 224, 228, 704, 705, 706, 229, 707, 708, 21, 709, 23, 230, 710, 231, 711, 712, 713, 714, 77, 278, 280, 715, 78, 1, 24, 79, 81, 58, 82, 59, 86, 61, 716, 282, 281, 283, 717, 718, 232, 719, 284, 720, 233, 721, 92, 96, 59, 285, 286, 60, 255, 105, 234, 722, 61, 723, 239, 724, 62, 287, 288, 2, 63, 289, 83, 290, 291, 64, 292, 725, 295, 257, 726, 727, 1, 728, 261, 729, 730, 293, 63, 240, 731, 732, 241, 262, 263, 733, 734, 735, 736, 297, 298, 299, 242, 737, 738, 245, 264, 271, 739, 246, 740, 741, 247, 742, 743, 249, 84, 300, 303, 304, 64, 305, 306, 0, 251, 307, 308, 744, 745, 746, 302, 309, 312, 311, 313, 314, 320, 322, 65, 0, 315, 316, 1, 317, 318, 2, 323, 324, 69, 325, 326, 327, 328, 330, 331, 70, 332, 333, 334, 335, 337, 339, 340, 341, 342, 343, 344, 346, 347, 348, 349, 351, 352, 353, 1, 252, 354, 355, 357, 356, 358, 359, 361, 362, 363, 364, 365, 366, 367, 368, 66, 360, 85, 2, 67, 369, 370, 372, 373, 71, 72, 92, 94, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 747, 374, 69, 109, 375, 379, 380, 382, 383, 748, 384, 385, 386, 387, 371, 377, 749, 388, 390, 391, 394, 396, 400, 401, 403, 406, 407, 409, 410, 750, 411, 376, 378, 415, 381, 392, 389, 393, 395, 398, 402, 405, 408, 412, 413, 414, 416, 2, 751, 417, 418, 419, 420, 421, 422, 752, 423, 753, 754, 404, 755, 756, 424, 425, 757, 426, 427, 253, 758, 254, 0, 759, 428, 429, 760, 761, 110, 762, 430, 763, 764, 765, 256, 258, 432, 431, 259, 433, 260, 434, 435, 766, 767, 436, 437, 438, 439, 440, 441, 768, 260, 442, 443, 444, 445, 446, 447, 448, 106, 449, 450, 68, 451, 769, 770, 111, 452, 453, 454, 3, 261, 455, 456, 457, 458, 4, 460, 771, 461, 459, 262, 263, 462, 463, 464, 465, 466, 772, 467, 773, 468, 469, 470, 471, 472, 473, 474, 5, 264, 475, 476, 477, 478, 479, 480, 481, 482, 484, 485, 486, 487, 488, 483, 774, 265, 775, 776, 70, 489, 115, 492, 498, 116, 490, 491, 493, 494, 777, 778, 779, 495, 780, 496, 497, 499, 500, 781, 782, 508, 783, 266, 784, 510, 511, 785, 513, 786, 787, 267, 501, 502, 503, 504, 505, 3, 117, 118, 514, 788, 517, 1, 789, 790, 4, 521, 523, 119, 87, 11, 506, 791, 507, 509, 6, 792, 793, 107, 71, 794, 795, 518, 524, 525, 796, 268, 797, 798, 269, 529, 799, 270, 3, 800, 801, 271, 512, 515, 516, 519, 88, 520, 802, 532, 522, 526, 527, 528, 530, 89, 531, 534, 536, 267, 533, 535, 274, 537, 803, 538, 539, 540, 804, 805, 543, 806, 807, 808, 541, 542, 12, 809, 810, 811, 812, 544, 813, 120, 545, 546, 547, 814, 548, 549, 121, 122, 123, 815, 272, 816, 550, 551, 275, 817, 552, 90, 818, 819, 820, 821, 273, 274, 91, 276, 822, 823, 553, 554, 824, 825, 4, 826, 827, 828, 829, 92, 830, 125, 831, 832, 833, 555, 834, 5, 835, 836, 557, 837, 838, 94, 7, 839, 840, 841, 126, 842, 843, 844, 845, 277, 846, 847, 95, 96, 848, 278, 849, 279, 558, 556, 559, 850, 851, 852, 853, 560, 854, 855, 128, 856, 0, 857, 858, 859, 129, 97, 101, 102, 130, 131, 860, 132, 133, 139, 140, 861, 862, 103, 863, 72, 864, 865, 277, 866, 561, 563, 564, 565, 566, 567, 568, 279, 867, 143, 868, 869, 108, 73, 870, 74, 871, 5, 569, 570, 75, 571, 144, 572, 104, 573, 574, 109, 575, 872, 292, 280, 293, 576, 577, 578, 281, 282, 579, 873, 294, 874, 283, 875, 295, 284, 298, 876, 580, 877, 581, 582, 878, 583, 879, 584, 585, 586, 587, 588, 589, 597, 880, 287, 881, 288, 289, 882, 883, 884, 76, 590, 885, 886, 591, 887, 888, 889, 890, 0, 891, 892, 893, 894, 895, 592, 593, 594, 595, 603, 896, 596, 145, 897, 898, 598, 604, 899, 900, 605, 901, 609, 902, 903, 599, 600, 601, 602, 904, 612, 905, 613, 906, 606, 907, 908, 909, 607, 608, 6, 7, 610, 611, 910, 614, 615, 296, 911, 912, 913, 290, 616, 914, 301, 915, 291, 916, 617, 618, 917, 918, 919, 920, 619, 105, 620, 621, 622, 623, 624, 2, 921, 922, 923, 110, 77, 625, 626, 627, 629, 78, 632, 924, 634, 635, 925, 637, 926, 927, 79, 636, 928, 300, 638, 929, 930, 639, 931, 932, 933, 934, 935, 936, 937, 938, 939, 303, 640, 940, 941, 942, 943, 146, 645, 648, 944, 945, 650, 651, 652, 641, 946, 947, 948, 644, 653, 949, 0, 950, 951, 952, 147, 8, 148, 654, 646, 953, 655, 150, 647, 649, 954, 656, 304, 955, 657, 658, 661, 956, 155, 156, 957, 305, 300, 958, 659, 959, 662, 960, 663, 961, 962, 664, 665, 666, 963, 964, 667, 965, 966, 157, 967, 1, 968, 969, 668, 669, 672, 673, 670, 106, 9, 671, 674, 13, 970, 675, 10, 971, 972, 973, 974, 306, 975, 676, 158, 976, 307, 977, 308, 677, 978, 678, 979, 310, 679, 313, 314, 980, 323, 160, 161, 680, 80, 681, 981, 982, 983, 984, 985, 986, 682, 987, 683, 988, 684, 327, 685, 328, 686, 989, 687, 107, 990, 991, 11, 688, 689, 691, 992, 696, 699, 993, 700, 994, 701, 690, 329, 692, 108, 995, 996, 12, 997, 703, 694, 330, 998, 331, 999, 698, 174, 1000, 1001, 175, 1002, 176, 1003, 296, 702, 704, 1, 1004, 332, 1005, 1006, 109, 1007, 110, 1008, 333, 1009, 334, 1010, 81, 3, 4, 705, 706, 1011, 111, 82, 335, 1012, 337, 707, 1013, 9, 1014, 177, 708, 709, 1015, 1016, 710, 182, 311, 711, 712, 713, 714, 715, 716, 112, 314, 1017, 339, 111, 1018, 112, 1019, 113, 340, 717, 1020, 315, 341, 1021, 184, 1022, 1023, 718, 1024, 1025, 719, 720, 186, 83, 721, 187, 722, 342, 723, 84, 724, 191, 725, 726, 115, 727, 728, 729, 1026, 730, 731, 732, 1027, 733, 735, 13, 14, 737, 15, 1028, 738, 740, 739, 743, 1029, 747, 741, 16, 750, 17, 1030, 742, 744, 1031, 192, 756, 1032, 1033, 748, 751, 1034, 734, 343, 749, 752, 301, 753, 754, 1035, 1036, 1037, 736, 755, 757, 758, 2, 114, 85, 116, 759, 760, 763, 1038, 1039, 1040, 1041, 1042, 1043, 761, 764, 1044, 765, 766, 1045, 344, 86, 87, 767, 768, 88, 1046, 302, 117, 118, 0, 119, 120, 346, 771, 1047, 1048, 1049, 193, 772, 773, 774, 310, 770, 194, 1050, 316, 775, 777, 1051, 1052, 347, 776, 778, 779, 317, 780, 8, 196, 781, 9, 10, 782, 785, 786, 787, 789, 790, 791, 792, 793, 794, 1053, 795, 783, 1054, 798, 1055, 797, 1056, 121, 799, 800, 1057, 197, 129, 1058, 1059, 1060, 348, 1061, 1062, 1063, 349, 350, 801, 351, 1064, 803, 805, 1065, 123, 1066, 1067, 808, 1068, 18, 352, 125, 1069, 1070, 809, 810, 812, 11, 1071, 1072, 1073, 19, 354, 126, 1074, 813, 814, 1075, 355, 2, 199, 204, 200, 356, 357, 1076, 358, 1077, 1078, 359, 1079, 1080, 128, 1081, 131, 1082, 1083, 1084, 1085, 1086, 318, 207, 802, 1087, 319, 115, 361, 89, 320, 1088, 804, 815, 806, 816, 818, 819, 820, 327, 1089, 821, 328, 823, 1090, 116, 90, 824, 825, 132, 1091, 137, 1092, 1093, 1094, 1095, 139, 1096, 826, 827, 362, 1097, 1098, 1099, 1100, 1101, 1102, 5, 15, 1103, 1104, 1105, 828, 835, 1106, 1107, 829, 839, 841, 1108, 842, 843, 1109, 830, 844, 1110, 846, 363, 10, 850, 11, 12, 1111, 1112, 832, 833, 852, 20, 21, 208, 834, 1113, 209, 1114, 91, 836, 837, 838, 847, 854, 1115, 855, 856, 1116, 848, 849, 840, 1117, 1118, 1119, 321, 12, 215, 216, 1120, 851, 858, 13, 859, 332, 1121, 364, 366, 13, 1122, 14, 1123, 857, 1124, 862, 863, 860, 861, 864, 865, 217, 1125, 376, 866, 1126, 1127, 140, 1128, 867, 15, 1129, 22, 868, 141, 1130, 1131, 1132, 1133, 1134, 381, 869, 16, 1135, 142, 393, 1136, 1137, 1138, 1139, 1140, 397, 872, 1141, 398, 399, 1142, 416, 1143, 1144, 417, 1145, 1146, 1147, 143, 144, 7, 8, 870, 871, 873, 874, 418, 875, 329, 1148, 1149, 419, 333, 1150, 1151, 334, 876, 14, 877, 218, 878, 1152, 92, 1153, 1154, 219, 220, 221, 1155, 1156, 222, 93, 879, 880, 1157, 0, 223, 881, 882, 885, 887, 888, 889, 890, 891, 893, 1158, 892, 894, 895, 1159, 1160, 1161, 1162, 15, 897, 1163, 1164, 883, 884, 886, 1165, 1166, 1167, 901, 903, 906, 1168, 336, 224, 225, 896, 1169, 1170, 898, 899, 900, 902, 904, 337, 1171, 1172, 907, 1173, 908, 1174, 909, 1175, 1176, 420, 131, 1177, 1178, 23, 421, 1179, 1180, 1181, 1182, 422, 423, 905, 424, 1183, 1184, 911, 1185, 1186, 1187, 1188, 427, 428, 912, 429, 1189, 1190, 1191, 226, 134, 1192, 1193, 1194, 1195, 338, 339, 340, 1196, 94, 431, 432, 343, 915, 913, 914, 916, 917, 918, 919, 1197, 227, 228, 434, 433, 435, 229, 1198, 1199, 1200, 147, 920, 921, 1201, 1202, 922, 1203, 923, 1204, 1205, 924, 16, 926, 925, 927, 928, 1206, 929, 930, 1207, 931, 436, 1208, 1209, 345, 934, 437, 932, 937, 935, 938, 939, 438, 1210, 1211, 439, 455, 941, 456, 1212, 1213, 148, 1214, 942, 458, 943, 459, 1215, 1216, 149, 1217, 462, 945, 946, 947, 463, 1218, 346, 348, 1219, 350, 948, 354, 951, 464, 47, 1220, 150, 151, 1221, 1222, 467, 953, 1223, 1, 1, 954, 955, 956, 957, 958, 959, 960, 1224, 1225, 1226, 961, 962, 1227, 963, 359, 964, 1228, 965, 1229, 468, 1230, 1231, 152, 1232, 1233, 24, 1234, 153, 1235, 1236, 26, 135, 363, 365, 367, 469, 470, 967, 368, 1237, 154, 156, 157, 1238, 1239, 1240, 1241, 238, 158, 1242, 968, 1243, 969, 970, 971, 1244, 972, 973, 974, 976, 978, 979, 975, 239, 1245, 1246, 27, 471, 1247, 1248, 29, 472, 1249, 371, 1250, 377, 977, 1251, 1252, 1253, 240, 241, 980, 17, 242, 246, 981, 254, 1254, 982, 1255, 983, 986, 984, 473, 1256, 1257, 474, 475, 1258, 1259, 476, 477, 258, 285, 286, 478, 479, 287, 985, 987, 988, 291, 292, 1260, 480, 1261, 1262, 482, 1263, 378, 481, 484, 485, 1264, 1265, 990, 991, 992, 1266, 1267, 1268, 1269, 1270 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 0, 20, 7, 21, 22, 23, 5, 6, 2, 24, 0, 25, 26, 27, 28, 7, 18, 16, 29, 30, 0, 31, 22, 0, 32, 27, 33, 0, 18, 12, 16, 34, 35, 36, 37, 38, 35, 39, 40, 0, 41, 42, 36, 43, 35, 39, 40, 1, 44, 44, 6, 45, 46, 47, 48, 49, 50, 34, 51, 46, 52, 53, 5, 54, 55, 1, 56, 57, 58, 2, 59, 3, 60, 61, 62, 12, 41, 49, 63, 62, 50, 63, 64, 65, 66, 67, 68, 64, 69, 65, 70, 70, 71, 72, 73, 74, 5, 75, 0, 76, 72, 77, 78, 79, 73, 80, 5, 80, 0, 81, 82, 2, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 16, 85, 89, 95, 96, 97, 36, 95, 98, 99, 96, 100, 99, 6, 35, 2, 101, 0, 102, 46, 103, 101, 2, 104, 16, 103, 105, 106, 107, 108, 109, 0, 7, 110, 111, 112, 105, 113, 114, 115, 51, 116, 7, 117, 9, 118, 119, 120, 121, 122, 123, 124, 125, 0, 126, 1, 127, 36, 128, 129, 124, 130, 0, 131, 132, 0, 133, 134, 107, 135, 136, 137, 114, 2, 138, 117, 139, 140, 141, 3, 142, 3, 143, 120, 0, 40, 121, 144, 145, 125, 3, 5, 146, 39, 0, 147 };

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
            final int compressedBytes = 2942;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb2PHLcVf8uMDszBQehDZF85ZyjIBlARVw7UZO7yYTuNDo" +
                "KFxAjSpEkKlakMFbyLEZw6FSpULlQIcOc/4WCoMFypCWzor/F8" +
                "7O7NzJL8PfJxdu+iAXT23VuSj3xfv/dIzn738zsP33sy17+4dX" +
                "b365+8eHj50eOD6stP//euffDtPXuHvrtf07+af1LTH43oP7Do" +
                "df/vfzX/67OG/vsXD1999PjBoH8p/f+cP6IZjR7T/0U6/uXP7v" +
                "zh1lktf6K7Xx+f377cmz8gq/9YD1LRNuafYf09/ffpk+n3m/v/" +
                "/Pv7T+Z/e/bL/zx68fHLv7z63eN/nPz38+9r+psvLqZfn0Z+xd" +
                "n88Fkjv9n57Vd784MbJT9p/9edvx37F9JUUPNz+dS/GLX8/6rx" +
                "HxPPT/WdVdX7WT8qg/966+PLxPSNxw5/Fa/PTY/fWe2zWMX3qh" +
                "/fYXzr8JuXPql+Z5Afmt+U8jkgumWuHJIi85mpf9pGBCz/xMEn" +
                "T1r+7k4xv5blFUjcq/8dnv6ms9JKKU2T28cSf3T4cdbixwH+eH" +
                "P/V0t8dPZoscZHbfs1PlrlH4tufupa6a/Uvjfx++iZFSF8j+gI" +
                "vyP6Nx29w4/HLX5c01UGujS/kPYf037RtT+J4Q89Uv8mxfer9j" +
                "773Nb4vvZ9+qKjqz4d4Vcpfcj/y5b/kyX/X3DwM2gv7X9Mpx6d" +
                "3POjnPNzgr5q6H9C/h/aj64IPxZ+wnj+jvKPwcq6HoDPdk13yX" +
                "e4vtnHp3T+vPhIc/CtEx+A+Ivw3TJAKyrXFjBQJdReSu8v6lIl" +
                "px6fYtqL8WuvvUt++/t0+0DX6nPrrB6/rOjDf5vqy+OnM/vg+b" +
                "0VS0N8W/bxbU1/J4R/EZ2Br0X97//ZO7+Lexn8J2rfpy86+jC+" +
                "xrraaDrw74rinnH/XfwoQh8sPCGiCPp/u/JvcQuxwZ8vWiX2T+" +
                "OUYjkto9WQbdtbn/JqqGLEnK4sFY376xjRlg6b/87bNqb5iBow" +
                "ar2hFoVi57RUzbYxzYxUZz/PG/s29acuW/tJW98IOlJFrB/h9Q" +
                "Ht5fOvZPaztk/tlt9yfuXG/EomHfSfLN/Y9TM8ZxM5PrN94ddP" +
                "l32u/IOOn//haP5e+1/1D/QH8Qcc/ADQtL+cLp1T5ZaGjba/hE" +
                "Bl8sU3vUE3PPlbrn6roH6j+BLPf1x73D/yHzTyn66nSPdvUvvm" +
                "2Md48XspuvaNb93hfCMeVSJ8Bf0jtH9kfzB+hO1DjC9X+Ifc+A" +
                "etH5gfo75bSOq70vrokflB1dZRfGD21Oy3REfWzqg0paZFSceJ" +
                "+KDie8wt7S8cgvzm0JffbIs/X32zXX+WTnvwiRDfTV2fjG0/rl" +
                "8mtKe49hXHv6RnEVA+Zsj2yD9F8x8p/2/69YGRf1nt34TouH5Q" +
                "wf0PyfkCNH9pfYSZtY60zrDp8vpspPw3gl44Pkrr8xbkVzh+Rt" +
                "c/KbK+SqH6rqP+68KeK7pytN8pPUP9O7j+CfKLq/9Rkn778THL" +
                "cxsHfp0mv+D7v779aLb/E9Sf2/o6qr876DSqz6P6eLB+L63fMf" +
                "YHJqXH8/fZtsd/R7J/guwf7W846NSni/dvcsX/epWNE38Tqo8j" +
                "/VWNBR/VLTbqH3sE9z8Z/k3F+9eei9enSx+uVD8X0Wz/D/qH46" +
                "d6f8vrH9ZPIvevbOT6ivlHbpE7f5UmH+n85qQszRa/Pv9X/afP" +
                "7YGdPVuYhf7TvCzsnntXq5pS/pnnB+vvsfjGp18mtX4N8UPw/K" +
                "i8Phcef12/qgd31a+OzLdDOnV0GtO1m070SefdGvnoZqJ2hf/K" +
                "q0RIrS3qyr8z/T/qv5l/EZp/eH6IPzh/wD+uLzLXfyQ/4suvXZ" +
                "8P6jmm8L9qrzbaW5Z+wPVfys8nX6l+XrUPzz9NPzPMH42/sT60" +
                "aV8s/vfD/PvWB9gf8i+1n5bFx3nXsMZ3trC137fK6Kb7WT38e4" +
                "79ERvpUFF7XfU8uavEeimMf8L1odfD9iN8SeHiAc5vUPy9QP47" +
                "F371MXoKxH0aFz9j6XD/bbf4Sax/cP8Y6S9Xv6X03eDP5p9Z8b" +
                "c6f0H8+008/OrgtypY+E8+/mUzjaKkvRE/ZSRdpdHX5xvc+T0e" +
                "v1p+WgXpEfpv4tYHre/rYfu1/+bOX1r/nVh/dGR8sJz6wjXKP/" +
                "H5jvD5nHlbv1ocUZ2/00/Lmv7U1vimmofkG+H/Yf2scm7K7Ja+" +
                "YX/89tn1Y+r4G6bL64NT8JfRf8DzqynjR8rPZPRfW9YP+fgx6V" +
                "LpzB90a3/7Q/9W2NmMVX++KfVtX361Lfm7x8f1UZCfXNB56dTv" +
                "xYqPd1tS0UizrBoVqOjYzKy5OOTx3+Kb4fmIwfoBug8P2+3YB6" +
                "g/6MHx0tJxqZObXypefhmr39L7G2L8SSC+u+ovht8e+q/1/pvb" +
                "fjWrvl6A80mTzQ+ePyUWfpLIf9L4h+4/evhT8fhiwJ/KhW8i4q" +
                "eaBr9MnD+J43dO/EHx538A//D9WiC+yusrU+CHLeb3sfKT4nvr" +
                "86+e/rF/Ow/7t9H5TrZ/7PATQz+l51/soL61jM+6HT/An+r6t1" +
                "0b3TR6enU+rsplXxH+SwnpefzPRraTK7+Nja9L/M26P7g7+0b3" +
                "R/D5T+n6xuy/O87/4/olej8MsN9I+VnJ+rjmZ8L4g+1fyO1fYC" +
                "9h/uH5Qe2437rcX2kedL+C479C/Uv1E/En9v9Z4//LNP8Vrx9V" +
                "/vqhpz1+PySoLyw1o/Skavh+Qbh/8f1Y8uAjy+JPen+E0V70/h" +
                "8kX8b9iw+7+tXGYl50v1ZqgO/c9akQfQf7B/z1R/vTU8dn6f0J" +
                "/v0RHc9/hvOX+e8vRN7fyH1+JRJfotPxSP5Yvicy/pyMGkf1TX" +
                "nqb8/b+ltJXf2tXNffymX9jdF/iM6u74bbp78/huTrx8q/NbAv" +
                "d//889Mqov8IOth/ZsS/YHtIR+NL83NQ/8jQv4jOwG/n4fxZ3P" +
                "+k87s5dJ0WP9j+4W1fX5n/n6z9rvmX7k/tXr6y+Htj9FMIpH30" +
                "CP60gyD171O3PzIXS3xZtPcnyvb+RaMcDb40KfmD735ZUv0P9b" +
                "9x/2Pzfouo/u27P6P9/Q/iC75/FMb/4fszhuN/rBBfxfo3E/K+" +
                "M+/8fPtfNDj9MAP430f3teffjxqv/5B/TPfdr3IZB1f+14mud9" +
                "z+xtIRPs1i/+h+XkB/gX3D+7eZ5pc2/1z3L1PtP1982gf5pTs/" +
                "ypA/M/0/ys+Y9GT8juo7ifUhJr7G78+eqH6V5/2r6fkxrUbz3p" +
                "/KXd+LlL9sfyTt/EJG/mD719L8lulfpPLz0Y2jftrf3wJ0Jn5k" +
                "fxGBTaqvMiCwStSvU+H+CZ3rUPwc9+/IHz38rfJPNXF9VRSf8P" +
                "paTv09OT6I58fbH3Don82y/8B/v13i/kYqveLxx8c/6PsdEukY" +
                "v4Tjd6b6q//+MS9/j4hPnvvJ7vGxfr0O32+Wxjehf97K9/PGxK" +
                "/I+qXcP1Xh/rH9ye73ZNs/moguxffS/AfTp87vZPmJVL7Tr18m" +
                "/fD4n0nwQ0z/1doDuddfSO95uDT7RuNfh/35Xils9P1cvP4D7S" +
                "e5/xOD/yrR+jm/P6j//poc7x8oYvFzbvlvIz/w54eb9+NNcz9+" +
                "tEIqlD/46ev+R/69d//eMz6Pzht/078WlGd+YjrGL6L5sxMo0f" +
                "nA6c6faZn/z2CfKD4E6UqfdPczjVKu71/C7++B4yfUt5B8s+pH" +
                "mH/kn4hbv8hJp4j3O0TSr8X83iJ6vP6ZqPwH1+fjxneaowqpJn" +
                "j/ya7zf2n9n0u/IfY5+jO+XzRF/Zci9lek8pH2L2ufUN914K/a" +
                "fqsr+23CadneT7e08f6W2Porul8NS0TGoV+c9xPlopuwW0Hvl0" +
                "HnE8D+Ev7+QyE+5dlf5Pt3Iv0jq76rrmf9duL789Lz1Wn7t0S5" +
                "vl946v1NVn4g8e8/AuyC4xA=");
            
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
            final int compressedBytes = 2815;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTuvFDcU9jgDGq4SxVwBoRwiii0okoqIaiAhImmCUJCSjh" +
                "+QMlVE4XtFQboUFJQrikh0/ISNRIGoaCLxczKPvXtnZm1/5/jY" +
                "u5eIaXic9evYPo/Px8dKbT6tHN/bz68//OLl4pdn545+e/Xti4" +
                "evbz5+0Dz54d+L9sGbW/a6evvT9YdXXi7uXeron7x4uLr5+HBN" +
                "f9/RVaWCH6xfSHd8xt0TDX7iprPbt7zxo/5Py/8N+LNNR/PT0k" +
                "u1+VV50n7D5597fZ1WG8d/SK9ajpda1eshVFZd7f5ctP/T/w6P" +
                "311e17N+DO0Xrv6VLrp1Nld4+3/Q//NC375p+78ktt+s2/d95u" +
                "QPPV6f1bTislbnZ2WW1PlZl9e+8uH60fzh+vtP3v7BqP0J/xF/" +
                "AN1M6XagV7P21db4GftLsn9Q/+D6ahTrs9zygF41jgVvNL19TC" +
                "8IHJfUT6Nnkn+qKIP6E5ZvRPqftH6d+sMm4c/qs+vfnTtaVJeU" +
                "uvHq9vHl1fnFA2Wru6YbWN+/Juv4u/bLo8XVvv2ib/9w0j6Uj7" +
                "L2SfNTBsTPxH44FSh08cXp/2Df3Fn37w/K/KD+Af7C9uH+MFMG" +
                "WN+2ipPf6/VTedfPvLwlyOiCZ97R6tcM+eYiRJcXtR+3P9LZt2" +
                "h+of0fsb848hnzR6wfLvabpdwSFk+BerY0RdsAFwzRSYpc9+Jk" +
                "WCxmptqKeUtr7WYn/2u88i3yG6qH/j2aP1g+xhAyQjqFLZH1U+" +
                "Rngv6Ty1fq2K2/lgH7wISaNST/+NS+tOH2gbgFE3Wo1LmRBtXK" +
                "/Nx5srYblyaUj1ufhqwf8ttfDdBq+lNzIkM6F+7q/a+GxdFoXW" +
                "G6w3/ybCT3+A9+VJcPq/ZH547an9SN+vp30zy5/VdhHzy9NZTX" +
                "fP6ZzHTG+kD82ff8SPWr0L+Q+58NQZZui0W6/Wwc9p9h29/11v" +
                "qtE+EvAweKrfrW64GMr/jqt8g/DuoPXbUi13TrU5+sz7ajpq10" +
                "NaxfQM+/v+X8D9N3Yh9CrClYPwkfodVfRPMvd/3aU/9ZXz8x9J" +
                "T4rj2D4/tI3xm97lZAsbxw/Kj9y6+2FdVPlmZZfb9QpU2iX2Lx" +
                "24/zs1t6LP5BdEuNYn6p8Jcc8l99pH8odOBfXDPvdSt/yi9b47" +
                "T4pv23ta2pbepKLWt1eyyf0ELXnvrfTOtXQ/1qXf++6cg/g+XV" +
                "vZ4/HfTSVVH0fOnty3qzvyZ0NaHj+mPtW5r/SeefjuV/3z9fea" +
                "XeBPkn5X+C/oP2G1H7SfanQP9J679mnvfla3Ved+XrrfIJ/ItS" +
                "Yr9L7ft9l8f26+D31ba0Ha6gVdWxvxM0V8b4WAVg8w90/NVKNj" +
                "7En8VQsFCLgb9Wmwl/0fquVLD+iPhHnv0G8cV3QvyPgI/rhPjm" +
                "dv3HtPOVSPwzNj5y3H4gPkSMD4KOEvBt8N3n0V37T+TfovbZ/h" +
                "0r/tmv3+pev8n5u2//t5HKF9n5iDN+MqX/ugLjX+XlzwaSLYYf" +
                "13MAmli/icXPY/TD5PycCHT4hPzKE59cJ6LPDKHxtxP8buWJb6" +
                "6Z9fv6D8aP9bsnfrkWyAeO/j7b8ksafyuP37V7pgv7V/ccXhbq" +
                "UfuPC92isrZVj82Cuv8S9P9/7R/umy71Lwj+QZDeeBFI2rqKkS" +
                "+55ZdKJr9yy7888Vmn7Tdd/O4/ddVO8I26sJdX56vD9r/v9r8g" +
                "lM9Ox/7fieU7XXblXEJpr31V9fRN+aHx0hYFcf8M5Q9G5c2oPG" +
                "gf0Lv4L8/8juMDQ+PLHP+05/XBPB8sSPKFSQ/oP4wvUOSXYzxN" +
                "ScNfyPiMjz8b/0eP7Ve6/0jlrwrhh0z9Q/GfEq0vjP+G9x/AN6" +
                "vJlNTj7lgSIwnyc5/+Axt/s0z8Lxn+GYlPyvFVjH+Ovde1/prc" +
                "j8VfCfqUm87c/xz8a7j/vOGj3cv49sef+fj59OlyW6+vOtP42P" +
                "VTz2/NgB/zz29d8d2T/Y/lT3D+pPU3U0bYKP+wBPqPOb8s+UWh" +
                "l3793gCGJsCfSor9J7VPdUL/ntl+YHyb8xczxCcM5y9qc/4C7T" +
                "s5PhFxviGKPyTcLzNj/UKTH778JtA/ayL6l9i/E+Vvoc6vJ78M" +
                "kH8k+R7KfyO9X4/tN9n9RJT/B9p/4H4/Jb9LOL9COD8Nqj+qfQ" +
                "a+kcK+DvvP4fw/4vupKD9DqvEdONuPu3+vJvr1yp/9/cUbs/uL" +
                "G/yypVeO+400fNN5/3CcQqPJNP+1m/eWiW/kvt+45/N/Qn4+on" +
                "2rmPJnND8BfA7l15Hah6TyoSGC8RPyvwjz3zDlj5/OyC/D6B+8" +
                "f5wA383YPyxfw/yNi1+cV1Sf/rXcPtwgYEcn+UP0PH8IO/+Hne" +
                "l3hO+efXoi/sT5V4T8Eih/DVo/x1H5NaD9Fnk+oWLPJ5Cizn1+" +
                "7bMfZPq3nLKsGVEtxQVG8gXmhxDmn8h9/jDtX+34rYvOmN9Kic" +
                "afO/9R7vnbSXyXIL75rPdv33QYfy1cn3niDxj6z6u/Nv75Mcv/" +
                "2xLkufwjGv4ojo+vfFNVUe2LMH5Iij+j2adVxPyT168B+jcTHd" +
                "4PhPjo5v6kHt9/9OGjufARPbHfYsZ3wMNH5/iwv/7gF89/F34c" +
                "mV84mX0Tsk989/fA/TRov79z9q9KpH/z37+ayhx2/nzovyC6LD" +
                "9YmvizED18/yyB/jmugvJFeD+NW/82fquz2idk+zHOf4s/n1jL" +
                "t+j8lZbo/36I+I8i5w9NgN+A+20y+3TX8blc/EWuX8H9LKzfgu" +
                "Up+fXD+f0z+xfC+2Pi95nE/ksT9jA2/pvnfCj2/H16PlXG8h/S" +
                "hfl3kX7o738cTe5/tBNhNvc/3PTT+yFy/4qEn0bfn0D3M8T3Ky" +
                "h0CT4lXT/k/Y/8B7f9rqs7g31ptO5F4PNt+zKImiL79GnY/oiz" +
                "bzn2NyrfaHj+EPa/UH50Z/3081OZfpbK7528v8M5X3HIB9H5m/" +
                "B8DvFHml9e6j8j/zM7fk5+v0aWP1vQv2Na/hk//tK5kHVzWr77" +
                "ed3H91kc301Yf8H60UKF/dsqb8IIivMnXjqO77w/1b/s+afGp1" +
                "AvsEXJp/j4eVp8vrf/4vejZvFF5Si+qFFJzocyxH9z/Osc/Uvn" +
                "XyH7Mrf/J35/MQv/mHRGfvrU+ARh/2WNHyfF9wri5+f88b8vGy" +
                "hfBsrD9SezT08+HN8diD+l6R8fviy83wE3gOx9XLR/5O/nyuLr" +
                "kf+N7Uvw/qxUPsbZJztbH7vgj8g/3IF/Sbr/End+QrDf8fvnov" +
                "hz8L5uFH8IVzA340PyF9oPQfsT1w/lL7W8jisf7h9+P1Vanmjf" +
                "GB4bk50PAf7h9yljzk/Q/Jnt8qz3JWLwJw8+KcavxPilUP7J8E" +
                "2Ijzmn/cO5P4Dit/H7fBz5U2NfadbtgwPv+4rPb1HKo/cZkdsF" +
                "47Nl+LY0PjouPza9fG78ddfxzQVZEtos9Rds/5CKP3H0zy7zF3" +
                "L2V6B+E9d+HD61i/zZS5L/zng/JgqgQvH3BP8tHD+bbHy8903o" +
                "76/I2ifxvxSUF64fX3xsOvsO5i+b6BdH/o207zuorfbBQNH+Jr" +
                "x/IPVfpf6xCuPzwfgTuX0tv98vzm8UyF8pjl9MpL/Yzi7NfyPc" +
                "78laPoF8cuJ79Pz3svz8eeLfZguF/b5euvct1/jM1WdrfOY1xm" +
                "dMzLbAdMNm26RcbP6nzQx4zw9k+W2jzgcY+E1quuXlr5P6j/L2" +
                "ZfY9e3/bXPPj3r90fIl4vue1diLtN1h/TPsZ8ptWQrqv/v8AUj" +
                "AwJQ==");
            
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
            final int compressedBytes = 1503;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWztvHDcQnqVXB0ZAEEawEpWrQMUVblI5cLVKYiBJY0OwAP" +
                "+IlKkCF5TgwmUKFy4PLgK4809Q4cJwlSaAf072cac77i35DTnL" +
                "OyFb+KHxDIfD4Tw+jom6ryTnMzT4BPSnS7rq/2oHdE3gq13+Lf" +
                "mrP7t0vZZvqVRULX+iLZ20v8+bn/S8kJ+pn2f/7PWXCtjhsvWW" +
                "dHf/9YhOa/rNl2c/H1zN9X2iB+/Pr49vZvMLsvqxaRnt2QaPGt" +
                "2d2trqesGW9umrs8tv382fvz64+v39T28vPzx8cVG//PXfr+3F" +
                "x0eNfCl9fP/rD/LrOmQ8xvnU+PxLYnwq4vxi9Ft5Xy+/GLufAb" +
                "prv787+/24tN+fnf3D/jnG79p/pf/hhv7Gq//I/eF+Ku18zOq3" +
                "8fuH7B95PgTOZyS+tYSyopkTPzUtfLE4cD5j94Pk/rdP+2P7BO" +
                "ms+MNKAYGv9Jvn1LxRDb2szIyKH4gqa4tma5WmqqJzhnk58Sl4" +
                "vjL+Nr+UV/OT121+Ka6PP8zmR05+0d3u9YYpjBpPJz7/C/PD+A" +
                "Dyq5B/uf8+vxZdfnX2j+i9oGotsxzcYnw/ieF9arlEs4fSRIVP" +
                "r03sJOuXq5S04QlL6db5qaHIz/L2r1Mut+Hbxy2Xouu3Cdfn8d" +
                "ut+uh6PP4uAvVRhHzEr6FzFUNhZuAAwD+A/VD9FhZ7eEjHR7rZ" +
                "xMFVI6Kq6fs/TP3y/K/CXrx5xFFLev8B/fA3r36vOPrh/Mm1X3" +
                "J9fB2qD2LqHz1iuE9Pzi6/eTf/5X7Lf+/t5c3DF0dL/s/r/sIf" +
                "vyE/cKlT86qvD5pfuvqA+vqAuvrAMM9/Av0853NqPnf6fUd9/X" +
                "La1S+mqV8Wff0yRf0cXP+juz7169NyfYZ+wW8C+aL9Ef0jxCcG" +
                "/HbAz6lvN77CW9/SoL+0TH6TeL8XK/9JaA9NHL1kxX8VocAu66" +
                "+gfP799yUCaf3CrR+898PT31VM+9y4/LfyK5b8/PmPUH0ki68a" +
                "Gb+IurBFin8H+mOlFRnTim3o7RGcPG0UN7Y9N6Wd/iBcX3vpSP" +
                "8w/1FTOW10iI2yz9p/aVs+ldxfxNBh/MzMP8gAE+P7t/nVzNRo" +
                "fvXg96HXCc77ARvfB/4rx5d98cGy4k92fBfiz9C/hPgixE9F+C" +
                "Kjf0iMv4swXW34R8m5feh9TBgfTMz7FP98oX/u3b5h/Bjie2J8" +
                "Vhg/AH6K3wdl+K8cH0bvo4geX1LF4X87wu+i8E8zHf4prF8Y9V" +
                "FsfRonP3P9t7v+zdN/yd8Hpmlf/fVP8Ivtn4po/F92P2P6Y51k" +
                "P5l+CH9C+BWuj8N06frp+NmC3Pd1SsPPED4G1mfUhw5+9cXt/E" +
                "MsfoXws1j9Y/HX1Pcl2fsOun/4fgJ8SIj/sPwTJf9SEn/rYP+K" +
                "8BM2vpJe/ygWPpOI/6H8L55/wfdLyh/EtyH+waufyuD9LZleV7" +
                "sHpqaNL2nzQ3L8M3P/WGedD2T0z8g/w/NTme2H8am7bV9O/5tz" +
                "/pbRX8v6A/l8kYwu7Y+4+ItJ3V8tms/eXf5MrN+y94+b61fM/k" +
                "hFvI+g/rQmyXxE9Pup5fgff75Bmv/S5Q/rW+/8tix/wPgt7I84" +
                "8gX9MfYPdH9BfyK2X/j+1F1+qXSzxQdVYZv8pZuSmx53Esbziw" +
                "FhOW7+j/3+6Tvfqc4/lZ75feSO10f53x/R/P//ff8p70smM32H" +
                "9WNsfRgZf9jvCyz7VEC5assXxfPDgI7k4/lgmf+x6qeSd7Q65G" +
                "Hi+b7E+VehfMb8p6j+k87/yufjwu9r+P758C2mfQC/NP/uPT8I" +
                "8be7rh/EJ3PjExA/YOMPSf6fOz9Aeub/f5IWn6L0Z/mfcvpLMx" +
                "m/+P02aT4o5n1Umj+l728MfCvwfiaeL9v3+0Tu9eX1NS8+puKL" +
                "e59vCesP/38W8E9E589vTDl/bKbjh/cb1G93HT/ZxfpZ/TvF/6" +
                "fXz5tfIV2WH8T5I3v9L5OfFp+no0N8AdavefEr6flI9wf3/x8s" +
                "/ptf");
            
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
            final int compressedBytes = 4750;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWw2wXVV1vqJtEIGgDYWmKLYq0SIUO638KLDfve+FZ6DaFm" +
                "y1TQkJiRIC+RlCwARezjn73Hvfu+8lQkkgCRBMwlMxpUaqnWk7" +
                "UkxLqYyAIAPkxxQISShBKS2d6XTGsWvttddZa+17nmP27HW/9f" +
                "d965537r3nnfsydHTo6NjCIbD5qf7U0d2IcPlePjF0tPtceS5k" +
                "HqNYflGVPYx27F3kFe9vNPKn4HGlfzegN0Lsaai/EFGj4U/Jd7" +
                "ZLRFRf/h3abGB8eiP8Y1ZE2qfYwNFsE2f1I+FGI5vJnaD4DcbF" +
                "bF3F1p/J/P4gos5D/reDd2iqCfwMfzI+umE37P8gWOeGx/4FUf" +
                "DmFF9yw+VefylH3HA+WGUPox3vVf4f+j91w+2u/13/Mb8bI+U+" +
                "iP5+yJ3rzxsrix8JT7mHHsfH/ZC/zH+mYvkT2Ff4z0ol+OcMvJ" +
                "qPBfTJGLlQZS/2A+GxBftTbrh7OTx+GvYfF8+oqt8L9uNuuPPX" +
                "/gJ/fvAO+k/4i7qr/YeDd0hVz/aXKO+P/OUBLXFL/NeC3emW9J" +
                "YhCt6OYp1b0n6v/wZH3JJGo8oeTvzt/ltuydDzfpt/kKLdhzgP" +
                "59PXG43B44QHjlPk85Ow/0bzg/9tqUS/+0De02qiipg1YIYHmA" +
                "HwLq3n7+ea7nLuoPNpbNCfHbxD/Zyx95v+oYC2u+0QDzZUbKcF" +
                "Z+4P3PbRyyXCdXU+oebV+Eh47Gnh9W82Gu229LWflz6rSRGrMn" +
                "p2Pq7VbJ410t28ur6G+Rvxn//zlNtO4H/i3wiocAXEgw0VBS04" +
                "Tq+6ovOKRLhOll9uM+VmfCQ89ozw4nHqnCF9nXulz2pSxKqMnp" +
                "ev1+o2zxrpLjfV1zB/dZzmpdx2gnCcEE24ifKdwc4sT4OKCVq+" +
                "V/yHmxj9avlujrgJyaZ++WtomwvKaRztbWVUvqs8Ho7TgPR13y" +
                "4MYSpmOQX2b6QqowfyHVqtPElny9/kXHkCPQZ8cnOBqjk22OO0" +
                "HhzJ6eV74Dg9p7mpX09Qzih/PaBlbhm8P6Hd6ZZBxTJa8P50FB" +
                "4n4f1pGS/JElLV+P60rHlVfH/C2KRU+a/nOwfOFR7MxQy9PynW" +
                "8P60TGuOnUP1rCaqiGUSeH+KDIB3Na+SKnh/4r5J6YjHbHIqzt" +
                "iL70+IFrvFfiTYEvYYouD1/Ey3ePSbfilHILaFETCBbT8pPkb8" +
                "3fmz8DzXYKT33+DfHroK34H3p5XC0z2eu3zX30lczAqxtVKJfv" +
                "Pp/JBoAt96naWoH6e8n4Cdw97YvkFXKfYsdqz2o+F192PNrTmt" +
                "N3jG4BnwaRQs/kNEXnPm4Bm9mRLhujqfUGcEHwn33rK8nTnS1/" +
                "2I9FlNiliVsWvy17SazbNGunH6/priPubn86k8JuW2E7Dn2q4N" +
                "RyzYcOzatOBIn+7avZZEuK7OJ9TZho/MZnkHzpO+sXHps5oUsS" +
                "pjB/KfazWbt2qy/fvqaug4iTIcp9NTbjtB5W1ymwAFG2KbaMFP" +
                "5L1uU29IIlxX5xPqvImPzGZ5B34ifWNflj6rSRGr0vxhcYxWs3" +
                "mrJhun76+Jx6lShuN0ZsptJ6i8eW4eoGgJkdc8zc3rLZSIzaY+" +
                "4tEL4/M2bPF1d6nwjD0oXVwvPVoxzPFUcapWsxNZNdk4ff+08T" +
                "gp7fLsqTgT73p3PaBoCZHXHHDX93ZJxGZTH/Hokvi8DVs8n34q" +
                "PGN/L11cLz1aEf3e7uK3tJqdyKrJxun7p9XzxeP0iak4rZf9c7" +
                "bbfw5+m7ydfgPM35f9MF+XPQev8N/JXuj9ADJ7YcNVfv5RuPZ8" +
                "GfB/hd8+3wr2/7Kf5++A+Iv5Wfm0sXVQdTbs4/P3FC81zL+B10" +
                "P9i9lL2ctD/569kr0K3p2czY/J315eDI/HRf+E/OT8lOyR7Hv5" +
                "mb3Hig9SNHs6+1FEz8M+UBzMNuDvwdlrMO2fAfpf0fMfyd8ZmG" +
                "aE+seyx8E+I/nOZfE4XSSx7D8ZtX8l+BvztwWOE/OT3GaHV9HB" +
                "hmO3mRYoneU2956UCNfJgutxk6F+xpa3M1f6etOkz2oyh17jby" +
                "su0uo2b9Vk+4/W1zB/dT59NuW2E1TekBuC6ye0JewxRMHrDe1x" +
                "Q71L4PppqIptYQS9YOH6qfIxUl0/xQq4fsKucP2EbLx6x3JXvH" +
                "5SrOH6aUhWozF+WnGJaALfep2lKFw/keIE7Bz2Rq1HNZE9ix3x" +
                "+qn8nObWnNar7uI8KveC2tdhrDl96GjvYLirMzJ0tFyMsez17K" +
                "d8P6dc4pfzXZviRbpzk3+JOYqXymv0666zvlzI93W6/0b3n+Ir" +
                "/ZbsZ+Hu1q/y3Z/yOn3vqPlkMVxemz2Rr6lmfRa694PGQei6me" +
                "4/lTdyN+Av0vTl8hztovwmfW8pv7G8oVxZnU9FdrgM70LZm4Hn" +
                "CzG+tFyRXyt3o9w2tw3OJ7QlHO0xRMHrNU9wcD0E59O2KrZFMC" +
                "44nyKCY455OJ/AruEInE8YDedT5wHp602vGOl8UizhfDIq8FO4" +
                "UjSgZ73Nhth4VJyAncPeiNPbmsiexQ4+n76sualGJlLeKrcKUL" +
                "AhtooWMM6lGEe4rs4XBsGWt7Nb+npzpc9qpooULRZqNZu3arL9" +
                "X9TXMH91Pu1KudPnHL2mawKKlhB5/kq0ErFZtPA+XvnUHecwbB" +
                "Rr3Sc8vS9Kl1ZgDqkkv1ik1e1EVk22/8t0bplQa5ffnYoz8Ta6" +
                "jYCCDbGNtOBVuZxiHOG6Ol8YBFvezlvS1/tb6bOaqSJFixu1ms" +
                "1bNdk4fV0N81fH6dGUO33O0eu6LqBgQ6xLC34iV1GMI1wnC84n" +
                "k6Fqxpa3O0f6et+WPquZKlK0WKXVbd6qyS5H6muYvzpOa1Pu9D" +
                "lHb4aDKzGyITaDFkw3TjGOcJ0gOE6Vj5HqdRQrNO/gU8LT2ytd" +
                "VjNVJL+Y0Op2IqsmW+vpGumIV5TvSLnT50ye/t4m+Y7oAH8qPt" +
                "L3jZB806M7WTtED1je7qVSP34OXReY+im/l4LjdM8v+72U2gfq" +
                "vpcSveo4fT7lthNU1wUr3Ao4YtESIs9fjVYiNpv61B1/XoYtHq" +
                "crhGf8PunSCswhleQX92o1O5FVk90+P51bJtTa7Qum4ky8uW4u" +
                "oGgJkee/4OYW2yVis6lP3XEOiBT3S1V43T0hPMU26eJ6mUArkl" +
                "/s0Gp2Iju7bK0nNfF+gdIuJqfiTLwX6hdkK2tjv7xf7KJoGh/f" +
                "wdX9uTrW4lu/KJ/NlBhx9tdIr1TG1+C5KaedqfLWOfgtn2yIra" +
                "MF032VYhzhujpfGARb3vZN0jf+D9JnNVPFOMnXtJrNWzXZ7VX1" +
                "NcxfHafzUu70OUdvKazJyhIib7KyEosIepfa6hCZjN9gWLbJkK" +
                "3hCdHJyBVZk8pYozS16tKgF2M0QbVNVWVFb1JPKzMoTuvd7+5v" +
                "nUkWrpoDIg+xjnBdnS8MjAUhk+UZf1irYCUtRLaSaop/5Gz/FB" +
                "SlWemRtq6iGmbnjlb4mwyZVmpkIvHCb/7jZTe7XX9mhr8yWQG/" +
                "kT9qP22z1+WTEyqW289OjuvPXf432JbP22IbXRfIfbowxaj95J" +
                "ZP5uJf08/zap4NfF1gr0xQT+aRZ0Xv4/pf8Vh1l+7NdOpso7ou" +
                "WA1rL1m/JCKKAYZjKZHVBls/MkB9xIKQyfKE3GqVU/FEMdQUj5" +
                "suk4/RvUpxL09va5i96tgbzpW9mptq1Hzi3eXuAhRsiN1FC87I" +
                "JluJCcYlOULUz2yWt54H49pjDqtSPKG7NFOqxrvV7Ndjdj0Vz8" +
                "Xc/RNUXsvhd3TREsLVOrF1omuF66cWL8HSwT51xzkgAtdPVVWY" +
                "50ThgeunlmQsq/a5pnheq9mJ7OyytZ7UxOsnpV28MBVn4t3j7g" +
                "EUbIjdQwuUZqP1SznCdbKwQiPqZzbLK7WaB+PaYw6rUryluzRT" +
                "qsa7Nbtfj9n1VDwXc/dPUHmZy5qvkG00CJGHWEe4rs4XBsaCkM" +
                "nyYE5UxENkK6mmWKC7bJ6iNCs90u7XY3buQMRqzE01MpHyFrgF" +
                "gKIlRJ7/AFqJ2GzqU3f8eRm2eJ2Z9fPoeunRiuT7D9oum9Vqst" +
                "trUz2ZUGu3d0/FmXi3wtpH1t8QEcUAQ41EbjXY+pEB6iMWhEyW" +
                "J+RuVTkVTxRprg+ZLpOP0X1KcR9Pb2uYverYF47BPs1NNWo+8S" +
                "52FwOKlhB5/gy0ErHZ1Kfu+PMybPF6puzn0fXSoxXJ97Nsl81q" +
                "Ndn9ejKh1u7Mm4oz8W6GtZ8sRAhRDLCJ3Gyw9YUhYkHIZHlC7m" +
                "aVU/FEMdT4+abL5GN0v1Lcz9PbGmavOvaHY7Bfc1ONmk+8O92d" +
                "zf8hC+9qAZGHWEe4rs4XBsaCkMnyYE5UxENkK6nGL9BdNk9Rmp" +
                "UeaffrMTt3IGI15qYamUh5y93y1ifZEiIPMXxuVhH0LNLVGInf" +
                "7K/hCq7CbB0PRqlT4raSfL9Qd1kmmQQRPwea3uoxO3l+NSJWkx" +
                "mEM/Fuc7f5kWDpe87baPley7nbgMdxBD3BuCRHKHDGKkH0PWc9" +
                "j++2nHjU49emKn6R7tJMlcY4IT8BO8eafr3InrFH33Py5HoC/T" +
                "wr7xZ3S/MNsnAWBkQeYn+NRLiuzhcGxoKQSTNTTlTEQ2QrqcYv" +
                "1l02T1GMsSLtfj20xX2sR8qsxtw0gUykvDVuTfNlstAVEHmIdY" +
                "Tr6nxhYCwImSwP5kRFPES2kmrgOKkum6cozUqPtPv1mJ07ELEa" +
                "c1ONTKS8lbCOkPXrIqIYYKiRyEqDrR8ZoD5iQchkeUJupcqpeK" +
                "IYasoPmy6Tj9EjSvEIT29rmL3qOBKOwRHNTTVqPvHmu/nNl9gS" +
                "Ig8xHO8qorPQO99WY6QZ/jaMouQzk+WhbmKnTonbSvLhfFJdlk" +
                "kmYUXa/XrMzh3ptCmn9dqz803FBfnm7NH2ZxqN8VLuVbWG1d2s" +
                "89uX8t/15PGeRPsyrijej0g68/BXQ+1P6Xtemi2NZz9DNN4Gpt" +
                "ND56clP3S0NVx+B2qeqP5Sbn32LPj7q7tpM/vv0rWGSS+fXjwn" +
                "TPX62WG+Txe01Zz5tYLdJe6S1llsCZGHGHiqiM7qPkIYQcsd6P" +
                "vbmcnyUDexU6fEbSX55cO6yzLJJDQB7349ZueOdNqUM/HuhrWH" +
                "rP+riCgGGGokcrfB1o8MUB+xIGSyPCF3t8qpeKIYasp/Ml0mH6" +
                "N7lOIent7WMHvVsSccgz2am2rUfOKVrmx9jCwc3YDIQ6wjXFfn" +
                "CwNjRLiJyfJQXHI6biuppvy+7rJ51iCEj7T79ZhdT8VqzE01Mp" +
                "HydsA6TNZviIhigKFGIjsMtn5kgPqIBSGT5Qm5HSqn4oliqGmf" +
                "ZLpMPkYPK8XDPL2tYfaqI7wvqWmrGjWfeIvcouZBtoTIQwyfB1" +
                "VEZ6F3ka3GSDP8H0mKks9Mloe6iZ06JW4ryR/6se6yTDIJK9Lu" +
                "12N27kinTTkT7yZ3E6BgQ+wmWnBGzqEYR7hOFlZoRNXMZnmlVv" +
                "PAp9ed2ksVibu9UHfZvFXj7Tf26zG7norn0tzpc47eWre29XGy" +
                "0BUQeYh1xK31WwRLnyCP74MRI8JNTJaH4pLTcVtJNflrusvmWY" +
                "MQPtLu12N2PRWrMTfVyETKG3EjgIINsRFawDDIVmKCcUmOEPUz" +
                "m+Wt58G49pjDqnTu0F2aKVXj3Rrs12N2PRXPxdz9E1TeQreweY" +
                "gtIfIQ+3slorPQu9BWYwQtvOIPcQXzYlzzUDc+Uk7HbSX5fqvu" +
                "skwyCSvS7tdDC78HH+KOdNqUM/HmuDmAoiVEnv8KWonYbOpTd/" +
                "x5Gbb4/1u29/PoeunRiuS3ve2yWa0m29+X6smEWrvznak4E2+D" +
                "2wAo2BDbQMv3BrdQjCNcV+cLg2DLO7Ctvk8rUE+/Smen7rJ5qy" +
                "Ybp6+rYX51fzzhTp9z9DquAyjYEOvQAqXNFOMI19X5wiDY8g7c" +
                "Vd9nNVNFinYe1F02b9Vk4/R1NczPx6l7ZcqdPufo3eHuABRsiN" +
                "1BqzWtNY1iHOG6Ol8YBFteZKvr0wrU06/S+a7usnmrJrtfj9n1" +
                "VHCc5qfc6XOO3la3FVCwIbaVFvxENlGMI1xX5wuDYMvb/Xx9n9" +
                "VMFSnaeVh32bxVk43T19Uwf3Wcrku50+dM3uCswVnAGmz4nm0W" +
                "rdaxrWMHZ/lHJMJ1db4wCLa8yFbXpzWpp1/Ff0932bxVk92vhz" +
                "b+f/NKGd4Pjk+57QTsuVE3Ckcs2HDsRmnBJ+aIG+18QCJcV+cL" +
                "g2DL2x2p77OazGFVOkd1l81bNdnNkbqa+PcqlTLMtSLlthNU3q" +
                "DDK71oCZHnH0crEZtNfeqO30sZtvi5ckU/T3VdoOJWkfyiY7ts" +
                "VqvJ9t9P9WRC9PzqeD6dMBVnovD/enmEFw==");
            
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
            final int compressedBytes = 4453;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdW2mQFdUVHlRcookGHCBYcYmpVJIyhaWJCcZYd17ziJYzSJ" +
                "nNLC4VXBg0FiFSSvBH93tveAtGxIqWUQiYOKlKJWGAgQpYlYqG" +
                "xSUy4m4AN4iCDkQYxLgRc+499/Q553b3aPJu3dtn/b7T9/Xrvv" +
                "fNG3O+Ob+tzfgRJdTKu8z55V1s0d5Qx2x8aTS0TXo5iyPjOUcy" +
                "om4rkVnaK9m4Z/m4Qsld/10RZqBNM9NA8iNKqFU3mWnVx9iiva" +
                "GO2b4OhebnaXsWR8ZzjmREvbpZZ2mvZOOe5eMKJXd9WRFmoM0w" +
                "M0DyI0qolbfakS3aG+qY7etQaGjr6c/iyHjOkYyo20pklvZKNu" +
                "49K0M+rlBy1/uLMANtjpkDkhudbQ42eBefQBtZKI5bdab2YDTJ" +
                "GrdnRx5OyBky+kqelFnar9m492zPjyH8dJ7eCbHDc/Zay7RAcq" +
                "OztbDBu7gNbWShOG4wT8qD0SRr3NIheTghZ8hoW+mQ8psyS/s1" +
                "G3dbfV4M4afzNCrEDs/Za03TBMmNztbEBu/i06ZZu5stFJenMw" +
                "LJ9RM0bv0PnFf/FOdpTsLQLPXxkk37iSPs1afyYipLCJ/mqdEe" +
                "YusKUu1yczlIfkQJtXJiR7Zob6hjtj9vheZtm7I4Mp5zJCPq9d" +
                "N1lvZKNu62+my1sj4/T5OLMAMtMQlIbnS2BBu8I8+hjSwUl6cz" +
                "AssB7uP5eZozZERr/SyZpf2ajXv12fwYwk/naSDEDs/Za5eaS0" +
                "HyI0q2RedG51qdLdLLGaRjtq9DoaHNooU4Mp5zJCPq9a/qLO2V" +
                "bNyzfFyh5G5sKcIMGfSrPIjHamvSWmmvdVtPvBu1SrOypnYN+S" +
                "ony0zQ+13GdJk/aW3tirZhX8nh5cHKSS7zWu2pn12bUZwXj3c5" +
                "s0WtV1s+OM60VdWuDDNqP6tdn87TNtB/4nD2Od9VPua62k8rLc" +
                "4xo8woGkm3LRoZjUQbWShOZrBu5RBT4lq0EEfGc45kRL2ySGdp" +
                "r2TjnuXjCiV346UQOzxn1MqD9jrAEa8KbPCO3Ic28vORM0jT1x" +
                "P5JK5F09lxh4znHNYpu36OZtPeeDyzcc/ycYVcVVtb8/AQW1dA" +
                "mllgFsCMudHN3QJswLTGLHDrggVsY1nrjMCyxrVo1GBdkOZpTs" +
                "LQLLAuEGzar9m4Sz6O8euClBnm6bwQW1eQatcauBvQiBJq1W3G" +
                "3SfIor2hjtn+vBUa2jruzOLIeM6RjKgnO3WW9ko27tWtIR9XKL" +
                "mbnUWYgQYvkPyIEmql0+zIFu0Ndcz2dSg0P0+3Z3FkPOdIRtRh" +
                "nlSW9ko27rb6bLWyPnzNu6wIM9AugNabjig5rbrd6WwRXsgNdG" +
                "fpdcfeNIJwe8mKrWd2ioI+gaojPXKv4JQ1Eq+zpYy9Yd0Y4/29" +
                "aYartrlIYUtMrc0ys0DyI0qoVXfakS3aq/WOmzDbv18KzV9Pv8" +
                "riyHjOkYxW77gJrieVJb2ajXvHTSEfVyi5m5uLMAOt3bSD5EZn" +
                "a8cG87QLbWQx7ZVvs9eOsA/2esdca0nP20dI3I67GIcwZTznSE" +
                "ard8ytfEdnSa9m494xN+TjCiV385kQW1eQareZ20Byo7Pdhi0a" +
                "G41FG1msjWXOYykaa48YJSW3fhqbnycZrKQZ0VqfJbO0n2oPe5" +
                "aP0LlWN0/vhti6glS72sDqlUaUbIsmRBPM1fNOZYu1kcQZpFs5" +
                "muBmZAL5qgvJR1aZTR6JQkgyFu61u3SW9srauWf57OjXBVdTrT" +
                "BP/ynCDLSlZilIbnS2pdiqrWgi2sgCtrtY5jyWqna1tdTmoUXi" +
                "ojWbJxkwRzKitb5CZmk/cYQ9y0foqEUTUW59JsTWFZBW2cprWP" +
                "cMLqX7rShcZeuVLL9wf1e5f7jdW8cdxT6xMzwpi9/zerJTxuh1" +
                "N+3vMlmvaeSiV2tCcYz0mLvMXTSSblt0ZnQm2shibSzbBvdxL2" +
                "FUdKY9YpSVJK7MZUzNiTmSEa2Nc2SW9vMZ6J7lI3Su1b1/74XY" +
                "uoJUm21gp42js83GFrVH7Wgji7WxzHksRfZ54aOk5OapPT9PMl" +
                "hJM6K1cbHM0n6qPexZPkLnWt31dHqIrSsgbZh9cPdH3QdznPtG" +
                "Zm7uPrg7xPno++DGj/6PfXD38Pvg6s/9PPV8xH3wYrMYZsyNbu" +
                "4WYwOmGWZx5Tdsobg8nRFIrtyrcS0atco9nKc5CUOzVJ6VbNpP" +
                "HGGXfBzjn3cpM8zTrSG2riDVppvpIPkRJduiMdEYM919XzCdbS" +
                "RxBulWjsa4T5iLavyC4v3nbgzjNOZzFmYyqtQppnGzZJN+iqba" +
                "uUs+jvHzNJ1qhXlaU4SptXhd/Df5qUlOjDfDtfwMfH4OxM81Fo" +
                "G8BT4jO3ouiHfa7zNBHoKok+M3ezpBfzf+IDkM7oZzwHYEoSTH" +
                "JKPgiXO++k7yz+4z8lL8cry9cXf8z3gXxJ1CV3VySHKoy/yYe1" +
                "ZNST6etCdj4/vjByx2tdt/Uh+Pn8SM+FnoL0A8PB/j8fHr/pvN" +
                "t8XzdU5ylI1MjnfxD8aPgP8JO0/6+TZ/JOnxvngvcJ8nn3fJCF" +
                "fVJ5LjzEKzEGbMjW7uFmKLxkXjzMLqv9libSxzHkvROHvEKCm5" +
                "62lcfp7ktJLUyVp9W2ZpP9Ue9iyfHf315Gt183RMiK0rSLWqqY" +
                "LkRmerYoPq3jXVxp/YQnF5es9uQmC0APdRzmv8kRE0J2Folmq3" +
                "ZNN+zca957W8GD9PKTPM0zdDbF1Bqi0xkIujsy3BBtW9hzayUF" +
                "yezggsa9xJd+Tnac6QEa2NNTJL+zUb9ywfocuqWCuqgLRwhc1P" +
                "x+r7cl2A3f69Ra4Chvt+XL/mHSXj9bqAM8LViV8XrA1x5VMe1w" +
                "Xh+nnekWGdH7Y6j/eF/srr4ix35zeIOqy8u3a3toUxeTqsn5Tu" +
                "GEfOO47jGvPJ66rY/WGojZuH81cOZRti2g7rp5yzcvdxJ9H6iW" +
                "qU+HkaPu/i9fEGP6sD8eb4Mfe8Oxg/5yxboO+IX8HnXfyveCje" +
                "D9Kb8Vv8vHNPhSPSnaF73sUPQX80/rt/f0fFL9LzDo7ueZe+kw" +
                "ft8y45nJ53ydHyedfWVrskfjjeBHHwvIufhuNT8bPxtviF+Hnn" +
                "/aF93sV78HmXjIR+pF0/JUcln0yOTY6PN4LHP+883z/irYL91R" +
                "j2gvEgPu/iA8727/id+P2kTTzvbjG3wCfQje6zeAu2trbS59FG" +
                "ForL0xmB5QB3ID9Pc4aMaK1+ILO0X7Nxt9XnxRC+uD8F2OE5e+" +
                "0qcxVIfkQJtfLTxv1tlCzaG+qY7ZkVmrdtzuLIeM6RjKjbSmSW" +
                "9ko27pgTVivrC6sNMQPtEnMJSH5ECbXaCHNJ40G2aG+oY7ZnVm" +
                "je9lgWR8ZzjmREHdYFKkt7JRv3WqZuO/p1geAOzySsINUmm8kg" +
                "+REl1EqfM5MbD7NFe0Mdsz2zQvO2pxkH1k+T2aNRpU4xME+CTV" +
                "ek2bjb6rPV+nkS3OGZhBWk2nwzHyQ3Ott8bPCOnGjm230LWSgu" +
                "T2cElgPcJ/PzNCdhaBaYJ5Gl/ZqNe3VrXoyfp5SZtaIKUq3DwD" +
                "qGRpRQK79k3AqHLNob6pjtmRWatz2RxZHxnCMZUW/s0FnaK9m4" +
                "2+qz1cr6wmpDzJAB9qhX1q8oLysP1n9cn5ak+8ByTyotoxUb7I" +
                "OXFX7v+wBF5r3MU4Xfri7jLPxdT/hqdugoK5FWXpb/vW+5p3Ig" +
                "rXyZ5gv5aZ2ZrSxnNbqh4zq3Vrg1XQXBHrB0p7s/HOJ24Nfh+q" +
                "lonmoHk18mc4pmo76mPpTyv+hQj/Y8N8UH9TzVD+jc0p31ffGm" +
                "6oi0VpjzeJvPvhHrzqz/RzvvsYD2VnIDIwGv/tXV7fGrRfOUzN" +
                "A7htr8Wr28Kty31L5YHmz+3llW8b6F5Px9C6Lk7Vvqa7P7Fo+x" +
                "ymJCFY2ifYv7XsVH+Yy0pvKq/H1L7QtYD43iu8pVwVWzqmjfgt" +
                "lYTTQUufe5vNJ//+F020vt0VBzlZXLK6OhngusDeZpJfrB0onH" +
                "aIgyCQVtwfdP95EHUFfjkZgtJso49kwhP+punkSUlagOtlK2v3" +
                "LasR4a6az4TNPZWEnz5KqW3z+tpKqiIXOhubABV565EO6Xr9ij" +
                "lWwvjTYXNh9BmbrdB6NsRzyyhEeW1Tz9RcbjkaNkNtSxk/xot/" +
                "Mko3RN2prO02iJyGc13D449KuznGqmOnmq9znd9tqXzNTmoyhT" +
                "d/M0laKqM+2R8hmHZDVPz9v7E8Y3V+ORo3QV9QPk9zzd9X0ySt" +
                "ekrenn7jRCrL8lz2rYeQr88tzMFDPFPu/MFKgPnndWBw/00hg7" +
                "okzdzZOTyScljkZZ/Z3zDRmPR46S2RxHMc19OkrXpK3p9TRGIv" +
                "JZDTtPgV+eW7Q32ms/d+UV+Lmzuh3BssWOVi6vIBvcn7xMPpJ8" +
                "zgqyhlWU1sl4PGJUeYXFJDz83JEf9eaQjrIS1cFWyiY+rIdGOg" +
                "OqUdyFVqT3p72hR5zlnmgPSG50tj3Y4MqdgDayUFyezggsa9x5" +
                "J+bnac6QEa3N/TJL+zUb93mfzo8hfJ6NEDs8Z9RMl+ly11aXv9" +
                "acbnvty6bL/V6li7v73HVRFB5ZIhyS1fW0wd2fugifM8Nsd3/q" +
                "4gbn/Fl/f+riKmWXVuYjRHd/8qzzTh72cxdULc8t2h/td+vx1X" +
                "h/sjrMIPTS9Xa0cnk12eBz52XykYR+i8JedX/aJ+PxiFHl1RaT" +
                "8AhLRrRG6Cgr2V4diUdZQTpP12M9NNIZUI3i07U6/dztDz18lq" +
                "bTdLp1QadfF3RayfbaWXZEmbq7njopCo8scTTK8tUYJ+PxyFEy" +
                "260LOrnBPJ2qo3RN2po+774iEfmshr2eAr88t+iNCJ5EOLq5ew" +
                "MbMJ2NNrJQXJ7OCCxr3MaE/DzNGTKitXWGzNJ+zca9NjE/hvDF" +
                "/SnADs8ZNTPawE4IRzd3o7HBlTsZbWShOJnBupXT98H7JG7j9C" +
                "yOjOccyYh660ydpb2SjbutPlutrC+sNr8C0qKHoodgxtzo5u4h" +
                "bPCOfANtZKG4PJ0RWNa4ja/n52nOkBGtlUUyS/s1G/faOfkxhC" +
                "+upwA7PGfU0r1lf7gPLp1SHrS/VwFLv9hF9vPfxaoz9W9fyGvl" +
                "yr3B/WkW728r98i/30FOf/Hf76yl8tsgqp9qsnL+PthWn9bTT2" +
                "fl/i7VH9yt+/N+P6fPFfbB6yJYAeLo5m4dNnhHvhWtq2xgC8Vx" +
                "q87UHswnWeM2buC8yj2cpzkJQzaYp42SXfs1G/faRXkx+H0mM7" +
                "NWVAFp7jdQvTBny/G/BmjmTW/pDDs6y3Ky2Th+5zFa/Jaqt7yc" +
                "rI0breT/m6CX5PDvwQ5/ucWUqOH3Kp7XR7mMwbSO5fZ6InRiwu" +
                "qpWsr057Ocq0KEwutpubie1kfrYcbc6OZuPTa4ck+P1jcbbKG4" +
                "PJ0RWNa4jTmc17qY8zQnYWiWardk037Nxt1Wn43x11PKzFpRBa" +
                "Sl72Rf5v50UTqbfeL+1Jf/+wKf11f0q+DSRUW/L4CcvuHvTzZX" +
                "RfVRTVYuuD9dJOrpI1ZECq6avsLrqU9cTxujjTBjbnRztxEbfM" +
                "K/F220v6cjC8Vxg/uT8mA+yq3LNW5jCee1LuU8zUkYssG64DLJ" +
                "rv3EEfbad/Ni/PWUMrNWVEGqDUQDcvWOuu2lzmjAzdMArN+9za" +
                "3lBzCOolkir5Vb1wTPu5Uc3+rmPL8jGZCoFEdRrRk6ykpUB1s5" +
                "211PnVwPxUQD7nlXsG/R+fpc2UO/w+BrrzQl71fneX9HyL9qg8" +
                "/dMN/98N8R8lFrP4CYTSJe/B2Bf2ev+ZHP/h1BIX0/h/3Vwv/s" +
                "Hua/t6NfpzvXvwLu3Xke2DF/zX1vPTM/M+/VeED8D8ClbR/5ZV" +
                "Fbl31YRJbfVp+Nqyxp+x9e9YlZ26QTPNqidP53BDyLQstw/jC2" +
                "sSHrI87hUbNYumqqmCvP4/8wpCL09PVfCbj47Q==");
            
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
            final int compressedBytes = 3205;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9Gw2sllX5U8ybmFBwUaBoa22tXFvZ1NBau+/3s0UsBwZ1q6" +
                "2mhnUbrLuKiYHtnO8P7r3BmGzhxlVc5YIGFI4JipmCiCZXsCxI" +
                "QAG7oN50JP5hkp33Pd/znuc873POd94r9t6d853n/3mf9/w953" +
                "1vaXlpeaFQSur4ilsaih7SOMCUltcXmXYplTOt+i3xb6u9xNYb" +
                "3YH03GzkbAvUosbUF2NrlJ/SdIm9z/pZvwn0F9IrK48tUP7y1J" +
                "bU7YCpS1XWIH23F7yXnx4han+PbbOd1v4fuqixBrBr268LTlN1" +
                "bSHHhf0r3Vq6FWqANRQ9pnGAAT4ONhpM29YbreHlbAvUosb0W1" +
                "KUn9J0ib332eMiALL2PbegFaUVqpXUCW6F/lNP5BcaBxjg42Cj" +
                "wbRtvdGdvJxtgVpsebIcS1F+StOl+Um/PRSnjLx9zzZ/eUpL6j" +
                "bAVF4g/fa2NiPES6fasM12WjlZowHs2vabX8/nn0t7Bje5dT8j" +
                "6QhfXSgU96A7HfFr9dOxJmqz3cXJGg1iKmefl3Frcml3XfKjqc" +
                "6V9jwup7tjEeNlRyzhihqm5bnkVcqLQd+TgTjJq/325OfzzuNW" +
                "b1xZWgk1wBoqrtI4wAAfB+tWcVX8a3RivbE2Ts62QC1qTP8qLE" +
                "X5KU2Xdva4CICsfc+GvzgIdXEQ+kFxUMPFQfP0iuS5GjhuAR1r" +
                "w5p4uWKmr9iYWKuNoRJxf9L2jVVOb0yvrgU8cBQHw/pTZcQeIR" +
                "qOS32dpgAMfAaruQ2+vp7Xmax3azF/XIso64XRBX/Jfms11Yv9" +
                "EVNtz1qz62+pn6OZYY10eWJ5opqxkjqZuybqP2Xp9+WJfcsMBv" +
                "g42GgwbVtvNGjk+vcbOdsm6LCt1HqwNZtuWzOldpDj0fOTsWwg" +
                "lwdZ/tZo3ZY+kS1kP76tzUxn0fsPk9FUQ7SDIRoQ/6Fwu9SeTR" +
                "39PJ6xel8a58tInO4LlUzu7YRNbc5GtOEQDYj/eLjd1N4sjnoW" +
                "47Q1fSJXkjhtDZXkrmX7A/rT1lH1J1Yq9j5LPYtx2p6Ou20kTt" +
                "tDJdk4HQvoT9tH1Z9YqfpWjnr24mTm/OL0dqtBrTd8P943OY9t" +
                "cs/3+qQEe+oA3rdb44L7z4bSBqgB1lDxao0DDPBxsNFg2rbevu" +
                "t4OdsCtWg8MVKUH1szRctkecw9ZyNgeLL+ZOKWPrviF10UR8S9" +
                "9L7vBzwzh4aBb+aXAu/beT3q/rUp7e33q7tbxlHaSbJx6kH3/Z" +
                "N8Gmo9+e3Wt3PUszc/mTy4vtOOk7zGP7PIDt9cU34axWmRw/ZX" +
                "88XJzoN5e7LzPVrv7k/j9LCL0k6S7U+9eWyTcdfML1U7GOZ1cF" +
                "wOlw5DDbCG6o9oHGCAj4ONhtLh2mLAYL3NObycbZNa1NiBpVjK" +
                "pps7sEu5h+cB/bWfZSPAeQCQyUvtDFTFaXdlJB53QDe/RsLOg7" +
                "O5tKE2v21y24FBOw/GWu0sWMO1HtuaTdV5MC3NWZgL6urabJ5O" +
                "dWfz8MTbnWKHsvWw2NXajTwh9om94u8qTo+KA3GchBrp4jkxLK" +
                "9Vv/8SL4tXxCnVelW8ruq3xDvyvNZMkM5P8gNygqI9qsoe8Xgr" +
                "TgvEs3K2OCKOimMDd4p/iufRDuiMPFeOkecrybGJ/IXyIjlJXi" +
                "IeFA/pOInHxJDie1L8VfxN/T4l9otD4hlxWMdJvKjql1R5U8m+" +
                "T5X3K3vz5QXyQ3K87BSPKMpu8WdV/0XHSfxDoIxAHBcvqHpE/F" +
                "ucVL+vJbg3xGnxtizIcxJ/xskPdr3YpaxArVsaqu+Ja4OxqRTW" +
                "0vqytbXm1QVZPZjfyGCLGh5YZ0vZVGzNlKw94yG2Te8ke88aqr" +
                "9pn+ag9e4JOppwj63/J7MSn0lbbzPz+E8t3ndUeSu7NtZPc/vn" +
                "+l57/bRPxPj1rj5UfyM5E/tvm/V9jXvHTildr+qSyKUrZ7QQaJ" +
                "jLwO44sevdQjcN6+T0D6zP8hifeY/A+7Zx+pX7jiiF70+N8339" +
                "Ke/VJ8LyOk7/wO/y9yf5m7DsTt6Voz+9pksity59Ij8GGuYycM" +
                "44oTewjXOy9rl2GqdNWR7jM+8R9d4Zp/VuHkyRm4ncxrT1h5Bs" +
                "Xt4d+C5sPiO7Id9bQLkp/LyAs/dur2icLokvqZ9dm4GGuQyccz" +
                "+7BT2nzVn7XNvHY3zmJaj3/rsPo0TjdUni9FI6OsYADXMZOOe4" +
                "W+PxdDzf9vEYn3mJxrk+bS7NPorcJW9QvXeXvEfP4/KBFv7BpP" +
                "6eKruT1jxVbhQvI8k/ovbepH5KlSeZEbOjeYvnpA2tlPIHjPS9" +
                "imcI8Ssr4pDCJ/mxfJw9H1+S0EgM5J/S1pDZZzrHOfmeqOsVXR" +
                "LayfSJjAca5jJwvqvpydSxTpd+ymN85iWad/i0oViccvNgiju/" +
                "a0zE7zl9+R3w1cue/G411ROe32VzsID87pdcfoe1tXZ9lbD8rj" +
                "RcGlazbFIn8+2w/lNxmqRxgAE+DtatyuT4F7TZeiuTeTnbJrUI" +
                "PFjKptvWTMnaA+3YK+DDuuk9a8idBzc+Ig4kGMiD57vyYLlA58" +
                "GVTnceXOkUzyo4yYPVb648WHHkzoMrnVwe3NKG8mDlV1AeTHui" +
                "6XuNj9n7Gtp/mfOUqdldfso1td2+1975Uw7bP+yH3j9lxt5U91" +
                "cFeNzFftnziD0TZGSv1bVMc9jGpynN1M47dlKL3W4Zv84sV9wC" +
                "yCWt7XE+25A847bOUSCOJr+rss/bt1+ujPX0mbHu3hSSM1p9wJ" +
                "qT+f24tsf5TKCxYfldZVZllql1S0ONz1ZmAQfg7BaGk/YEWye0" +
                "EtyErB7sg5HBFlNpS8qmYmuoZOyBduwV8HE6qYUW/1xdV9On07" +
                "iM0kzNvQ+mVLK3PeJ8anPdUjxX3ALIJa3tZX2mUHWa2zpHqczR" +
                "NZqfrqA0Uzs1O6nRMbeMX2eWK24B5JLW9jifbaj6cbf1LAXvDN" +
                "P17ku517spnvVuyv95vZsSuN5NoSsdv965vzuMPkG/O2x8zb6r" +
                "Wq/7fUuDfL/d94Cx35iT77vDxlz37sX13SH23txVY7Z/rXA9wc" +
                "pIuaPcUSjoOjm36dB/inpA4wBT7ogx+A9zgwZoa2mjt28Hp4fa" +
                "pBaNJ8aaTQcbtGBfDU/lAOhHJ1VEt+1BCo0rW6csGo5Lo1tTAA" +
                "Y+g631xtw23ugg/WknULQumwcgowv+sr5hn7LYdHb9BtZo7sp7" +
                "ljiOx8TSOm9R8e+G83GxT/VllbdE0yBvqXSL5xRlmeYTSRYtkj" +
                "N2+v5Oa9F5C4lTMq9yeUulu9Id5y2JZGuXRfMWzRXnLS0f9qvy" +
                "TIpvvWWK85Z03E2TF8TU+PsCxYHylgrZ8RpYnKQUmrck0GTyHu" +
                "FbtlR1ozzSZk36sPP04qib1v6ST/uojn1moL28fkXNaIH+TeP0" +
                "XYA1jtJ5LaOh+HWqOJ2wuYxPxm/eHq55T3zWKUWfZ2beS10P55" +
                "ktyjx52j7PzNEjjkZH38V55uu+80y+P2l7kjnTbdxArAefZyZ6" +
                "l9q/hULzclWuiGGN02ukobPeLR0NBdO4XVbSnxCX8Sn+dcRpKa" +
                "15T2zroT5b/elHaatX96fqRUF9hzkfr47vez6w5zH9qTrWJ1G9" +
                "kL3HLW3sDIXxZfp+Mu5q55s4lUpQy58ntI44TnjcaQ7ckje74m" +
                "R4yfuqklwivG/cS6VqfOo3JBcDDOMusbiIi1OppO3pcSdvsiK0" +
                "EMdJacs17tI7mpS2Lj57bwirl0S/LrxHV7WT7U+B39FFOb+3i/" +
                "rsXzU3XQ+wxkUzbLolPYNKA4bq5yxjGpVL5yfEBT7FvAo/w30/" +
                "uOY9sa37fe46pUviU/offJUTQMNcBmae7KVuWnGM573UKb4Nkr" +
                "EnlMf4zHsU3eOm2XxuHkyJyOg0cIbyZdbOcZ5GMcXzXLLtrBhJ" +
                "3iOfXwHj6PgoR3t6Kt6YRzRW8mfaKFO6K9BvxkrjRp9Vx3cYgf" +
                "aqn8k5Py2zf9X89ClVLo1hjYtm2nRLeiaVBgzVz1nGNCqXrLT7" +
                "bC7wKeZV+Jnu+8E174lt3e2z+7ue6ueI1FfYtfNunkYxTeb/J7" +
                "jvengrLf5NHC8v0dwW2J8uD+9L+NSremWqYXr2lHH039P1B56P" +
                "+f7zn/rrG3fRRp82zBf2viUi/wtnYErh9UXDgXHqDJP13Rlvyx" +
                "GnQL9C+VL/rtO12Y+Xt1OaqVvzRi+vhY3TVW7LbimeK24B5JLW" +
                "3md9plC1y22do1S+o2uzf2peTGmmdmp2Uvu/4Jbx68xyxS2AXN" +
                "LNSS6fbahadlu3KP8DMHduUg==");
            
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
            final int compressedBytes = 3086;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W12sVFcVnlIBS6VtEGn0wYfb6BPGnwev4cHMOWeGxliVRL" +
                "2gtaG2pLG1Ta4+GKMPnTmXzjEzVkR+rO0DiQkvFkJqg1DUqgUK" +
                "F6jYH1vtA7G0odVLCLc1aNQE99nrrLP2WnvtMz/MZU72nvX7fW" +
                "vvOb/73Fur1WpJrVb0Sa3x6Vqh5fpDfwVfrYyoqXri9a6eKLku" +
                "SwgVdaxJ9+fexPElAVyenXjVDvZpfhH69o9L/t9KH/X6J/1s2N" +
                "uNwsxVmFpULqEWyobqtZq5lt4WZtc8zbuhTz9fMj0tfdQHkYPe" +
                "7rpwTjWmH5VLqIWyoXqtZq6l68Lsmifq8e9arbMJdbBFjfRLrp" +
                "8hzslstEh8jdn1RQ1lP/0yj8Kaco6o1/qAjip7vRLO3r/m5kbo" +
                "0w3lPHWlj3r4zEzrKOr+NB381TaGs/SoXEItlJ1O6TVLLb0jzF" +
                "5VV/r1cp7EbEa3hrN8n7R0vz3YeVJjSe+qitXrSjcOxpfeXRvp" +
                "0/5gcT24MbnRzNX7GebygRCeV6qxaKN+0nsqvder17U+fO3nhq" +
                "8jvoh9fBHnKb5IVjzjkOzmcQTNGl9MvylzOYt7pvNZ0gd0Vvjk" +
                "5ye0wRg03GJOd1GkrHbI/el/xbmnm6yRvplHm6cDR7K1zzxmf8" +
                "k1ZHF+3zW1sX+AQ6+pHx/60+8OzlffjX19N94/1Xen38978Llx" +
                "/XTou98i3WA9KGMxw7f3s0h/vj+hDcZgq/9eNZ/G3PfcuQSand" +
                "/NxXxPJBPm/LQB7XlEMhEtIV35jSao5xKXNX5NLo+Wjh9DNesV" +
                "AV+4Wqwn/UE4RvNEXf5t5uiHqJONy2GU4TzVmOZc8CaPcmuKuo" +
                "H7p67s9Uqq2KUnWgzNvd6Zmd6CPjeK9OE+3VbF7C7W5aoYqlnP" +
                "iH5ZhebGhWM0T/NO6PE8bq4E35E+6oNn1qC3uz+cU43pR+USaq" +
                "FsqF6rmWvpT8LsVXWl20tpx/iuTOnO7oHaAn3Sbeq+8OSA5+Yn" +
                "B2dKVmOfrMbrXbKarDxO091Y3wrIOrNv91nSR6r8+fkJbcik4f" +
                "JsbXwD3YvcBX36i/I8/pj0UV+Noj6//zycU43pR+USaqHszqFQ" +
                "zVxL94TZXU90f3R//FPsQQItl81xXlpcr5sHUm7Je8wAHZE4Dm" +
                "QDOmSSnUeCDhEYx5GoEmSE5vMhOmbIaiUm15J5s03aPXAyb7lu" +
                "vo0tl8GS63mbedTGgX8eo0GyfrgfLzDKPX0SkCG3xIdtkuwl9q" +
                "Tjn8dMigK/28oRzCMTVg8RZaaDVjJPOpXOe0dpmZ1cSC7El6A3" +
                "s2sl0HL5oUtkwThNJwSUScqRXGTwEQtpucQjCZWyuB+suQ0Zof" +
                "l8iI4ZuYRsiA0xVBFp5bH4leJ4/WR55O6TPikrR3TQ2z0czqnG" +
                "9KNyCbVQdnSA6uERXIsOhNldT3IiOYE96qA1PgY2tGAcbTPT3A" +
                "PRhOnido9oOJJTMhIqZXE/Z6MW7dFjCK2Ypz0SW4650E4mJ41k" +
                "e2s7CZu5YlwCG1owjjYzT8wD0Shz3O6LGo7klIyESlncz9moRY" +
                "f0GEIr5umQxJZjLrRTySkj2d7aTsFWq2U3gw0tGEebmSfmgWiU" +
                "OW7vWg1HckpGQqUs7uds1KKDegyhFfN0UGLLMYPWnOMr/qDnLe" +
                "mCB3WMI+vMdB6NOuFITDtP70YPYDXnWpFkdTlw82tza4L7TF5Z" +
                "Mf79LiKNyjuP7fffhkiLrfZw6xkrT+FzcOtPhvtlsz99tPUXW8" +
                "erzanW68azGuJab1vrP23/n9bl9rts3kfaSxGl/Z72Cm+errPx" +
                "f2u91jprvt9ovVXWMtWcai9qX2szlxVVLG+/r31z6/etP7hRre" +
                "dbLxY1vGLamdL+j2LG/u2Mf3P7utzbXmnjj7XMmab1gjtSjCO9" +
                "JddWp9rX2GpuaN+UzCazZt+xvd3HZmEz8/RxsKEF4zSdEEjmuL" +
                "1leh7nlIyESlncz9moRfv0GEIr5mmfxJZjLrSjyVEj2d7ajsJm" +
                "5ukTYEMLxmk6IZDMcePFeh7nlIz5Fi8mRL8KzkYtekqPQfxynp" +
                "6S2HLMhXYsOWYk21vbMdjMPEVgQwvGaTohkMxxezfpeZxTMhIq" +
                "ZXE/Z6MWPaHHEFoxT09IbDnmQjudsHV40POWrQUP6hhHVnO9O4" +
                "359E0yOz99CKMwwo1xsykuFCXr4ZUV43/cRaRReefxx11877nl" +
                "NGcw56z13v34Aeljcb9S72DXh+5tex8O3vWuD2fpUbmEWii782" +
                "uqh0dwrfObMHtVXc48HRrnalovXqh1usD6+KIB1+kGjEu2Jduw" +
                "Rx206BmwoQXjNJ0QSOa4vS/oeZxBMroWtMt46YOWV1/Fp80A5v" +
                "IxF9qWZIuRbG9tW2Az56dbwYYWjNN0QiCZ4/bW6XmcQTK6FrTL" +
                "eOmDVr9czVe+0bvs5/Mx8/hydn+GUvaZkCewZ1b6e1OjHVX9WC" +
                "mCR0YDIkWD1rE12Yo96qBltyVbO7eQBeM0nRBI5rjxCT3P5SQM" +
                "bukccbNkvPRB0/nSXTRmfwYwl49ZxstnnOxzIU/g6lDp7zy70O" +
                "dxzt85Ol6WZGeyE3vUQcu+Cja0YJymEwLJHDd+Sc/jDJLRtaBd" +
                "xksftH582gxgLh+zjJfvg7OvmV/lFsfzqT5vt5dWeeNXRv8tO0" +
                "cG2Z/aa/rzwXE30v60I9mBPeqgZfeCDS0Yp+mEgHJ2H8ftzOp5" +
                "nEEyuha0y3jpg9Y5Xs2nzQDm8jHLeO8+8+lxHt+d50bPTX83/H" +
                "1mZ11tQT/OPB02bCfHhRu/fAVzfGr4edL5ruC4255sxx510GLT" +
                "d94gC8ZpOkix/SZMFzcO5LmcaJEsbpWyCt8HrR+fNgOYy8cs44" +
                "sZP15Ks2N9vtt7dZ/v4nvHy4Jrzo2Vcn08e6A5l1/vBlsfd+9f" +
                "1PXxg/76OHL2Wx/vvBVaH88R9PXx+D5tfbz6uJNVN1ZSdmNVY5" +
                "W1rSp8Vs9b9iB5sFEk9phPfsQQ83TUjYdvP8OtgkdwXLcmrQIz" +
                "T/fIOrlf/VuRVVKnbHyPQPdP5XuEFr5HMO11094s9nP1PYJ7/6" +
                "S+R3g29B7BZlS+R9h8R8FcvEcwkrk7gvcIxnu79h4h/kbbvrlo" +
                "233WfY9QcQR7fyNN7xEaE40JM2O2t3M3AZuZpx7Y0IJxtM1Mcw" +
                "9Eo8xxe8c1HMkpGQmVsrifs1Hr/F2PIbRyrxHYcswUX78EzZ69" +
                "/1iex0+jr1iBsFGkK8f32dF8LmYIX8ZQzXoG8FVV24+RexorGi" +
                "viXdCbvdVKoOUy9GQjmeuEQDJJHJnnQQ5sucQjqRLw+lXQCJDb" +
                "rxtiwI98ICEbYkMFVBFp4ffB2Y/6vQ+m61f/613nnav7Prjz9m" +
                "Dvg6tXPCg72ZuYOxvo7T3VXtjMPG0FG1owjraZae6BaJQ5bm9W" +
                "w+GchKFZ0C7jXTZqnf/qMYRW3mcKbDlmGV9klX/1lG0LeQJ39p" +
                "X+xoYBng32jfREsa+KbzTMoZ7v/jxO3GxRbYE++v14ds2Y1+nO" +
                "JGewRx20bCfY0IJxmk4IJHPcbKmexzklI6FSFvdzNmrZEj2G0P" +
                "wZ0Crw47396dVx/h6N5OruT+Pmq5+vn6ceJNCyR/KeLNwrdcjm" +
                "mCjZ/el6H8eNpxyXEWN4Fve6bNSyZZKPKnS55Uj8MRfahfoFIx" +
                "U9SKBle+v2r1zRwr15PzNNOmQXzAyt+H03+jhuPOW4jBjDs7jX" +
                "ZaPm81GFLjfHdjGFNlc39wjYgwRa9kLd3j2ghXulDtkFM0Mr9q" +
                "cbfBw3nnJcRozhWdzrslHLlks+qtDlliPxxyzjI/uf82k505m4" +
                "3kXef9bPTA+x/nRioc5PkVpFen7A6/v8iPcF5f/lp+8MUevafp" +
                "bGnQswQ2t17oXgS84l57BHHbTsteRcdpYsGKfphEAyx43363mc" +
                "EzHcLd7vVimr4GzUfD5Ed6viM6BV4Md79wX/6jfHwxx38cj/fx" +
                "cfwPUn/bP59ivhy1YMtfq7qbkp76mZ/mFoPMLxb0JPCBPj7fnp" +
                "pVCcxJOI2crmw4SDqLIOWX/2Xl6tP1p9ZIiGHPoqufsk3twKja" +
                "8jOP650FO2i4Te+GD4aZ3jScT4oF+fWwfcZ7qrGnmjLG0VwLXx" +
                "kbnrICzz/zGUDA8=");
            
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
            final int compressedBytes = 2801;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW2uMHEcRXsAxiS/hcnbMGcdYRIADiYC8HHB45GZ3BgI5Gz" +
                "vhIWFFwB9bAQkMkkXk+OLZ7cGzd34CAoL5AXkp4f0QcexAAoog" +
                "Z8dYEcIJCgFO/OCQdTmCibBJYgPT01tT1T3VPTN7eyfYU9dUdX" +
                "1V9U1vz3TP7l6tJl/hL8NfpUqt9WJiPS61eLpGXuFfWqcbYfhM" +
                "+LdEfy7tOak8zXmAaYQoE/+hBP/r8Aj4woma5RWeSfPMB7vZl0" +
                "McDo92tCeSdiz8XfiH5PjHtGdpKhO24b+S2LOSdrZi0Rxo9ie9" +
                "j+ayPRU+DSwTXpPh8aRvKjyRnP0L4T9TxKnw+fB004jzFqqWjt" +
                "MZ6G3PAx9FoV3tFb/a7qM5bflNDHLmI+Jlrmy2zMWe4AElg+9m" +
                "43SO6RP7QOdf4uvS29jM+fjeTs0His+GoqQGli1a1QPm+TMFPo" +
                "3N9uqcJ9ivpHhZNk4LTF/jNtD5V+M26ZUosKnPyma/KyeHkhpY" +
                "tmhVD5jnzxT4KM62mkzfuJLBL7JxOt/0NW4B3TJOt0ivRHE+K5" +
                "txV04OJTWwbNGqHjDPnynwUZxtNYm+LliHUmnKai+SEnt0r2mr" +
                "6PTqu1XP1uFzQz4P5YAxtCJg9CjdS6thy9dDhtISWzr3sdfYcp" +
                "oVUm0qmJISWzJOK7SxTRCNLYgDiceE2xaUsl98QeFVL+Jy79uU" +
                "niePxDyqPuWh1juTv2RBUflzQV6Qk3KBGsZKmewLmvvFOXJf0H" +
                "xY7Qval+r7ArGgMVKwLxhBme4LRsi+YKT7fcH2Y659QfMIuy8Y" +
                "KdgXjGS8yL4gOVr2Bf58fz5IsJXVvl71QY8/v7EVdYxDrbFVHh" +
                "WKaimfrXycXtOs2GEyTKN0P56B3vL1IDtyRV40t84gs/r9fjrW" +
                "ypatvdbvj1coXbXGNoWgEuPB39gGfcru8NlG8TQOq0JW8ENvfL" +
                "GOQk75Xqxn8pSy9Q2dj85RHwkzutnZiTfTVVC8ovlwZ727IbsO" +
                "LCtC8yGip9dq81jSfsMgHwl+W+v61Tzo9B5h73lsvebPM+1od1" +
                "yCR5VsLs/2BR82fSjdWdj98aX2GHfOPEpqYNmi40tsnE28vbrN" +
                "I5Yk44zjdKvpb51XdDZigJtPYrDVH7+z+/kkFrm8rT52nF5SME" +
                "dLzidxgVicXp8TjQmQYCurPdqYiN8ke6A3mEQvSowMJtN3YRJ8" +
                "oGEvjW5k6x/tpzZg4jfrUboXI2nL15NS3Z/UuVBeNLfOACtkI5" +
                "zen1ofz+5PY7mxXWbYmyo8Bze6n0/bj1W/P8X12qy+6rdTDa1q" +
                "sW5ftaxFeLXPLBdTtTKJHKwPolSastq7pcQe3Sul2IS2itZzgp" +
                "aurS/N56F4jKEVAaNH6V5aDVu+HjKktWPfltOsYFxLF2X38fuN" +
                "T2M2Oj7D2VjU45d831xVeCwf4ff4OhNXJm1lqg2lV3tnvRPva/" +
                "80PV5fZr0Tq8WH0uNbxOW43omrUvlW8bZkvVvLxgViWLw/s5Id" +
                "m/iA+KCGuAzWO/GOTg9ZOcW1wpPrnUjufuK9nb41SVsXk3upuC" +
                "KVV6fyGrFKrXfi7eJdLKd3i/cQa624UXtvduvHZD49BLbq84Z1" +
                "vxY9bEZDj5mfid1NfWYchwJOEpv0D/N4U/JM9Oouzvp+vLk8W+" +
                "+eKdqPa1ked3ofidfN7X5c7TPt+/EuWJBxaj9LxunvvXxu8c8t" +
                "4tE+0ctx4uvN5Lll6El6HHoS7PY/TETuSt6Uz2Kr4Rfu5dvPFT" +
                "Gs4uXruTOVn0/adXeyp/OpbwZrzeYu5lNfb+dTcDQ4ChJsZbVP" +
                "qT7oARxnYwbU9bzxej5Or2lWxKwYpfv1atjiZTwGs+VHgGOACN" +
                "t88m7u5Xza8cTczidk37P70wF6HDoAdvwxE2GLdGHAu2MGd4ai" +
                "3Ox699Hqmbq8P52uPp9SjZ9PT83xvuBT5eZT/OnS79ZBehw6CH" +
                "b73ybCFunClPGWZVjF6y0th+VxlebTf7qbT/w+cybj1M188gbL" +
                "7TN5HDvG4/Q4NA528EXVEGGL1HUOjVm7mk/j1b3eIjcWNG9Rhf" +
                "Xk3lR+R3sOvmv0mvT4bYK737bPFHeKH6XHO8T3mM9972v17/g9" +
                "W/mepH3f6PtJd5/7im+RiB96i4l1t/1zX4pjGf5A/FjbRx1Q0t" +
                "+b7dT2mj6Uts8zA8t64u/d8bR1B3cgKFiFJBOKkhpYtmhvCc/Z" +
                "tLwl9uqcJzioJH6PIB40fSitmS3e1iddMUHBXV78TEdJDSxbdD" +
                "xi42zi7dU5T7BBST/I3sXA9KG0Zt5A48iMCLheiHHnzKOkBpYt" +
                "WtXjOJt4e3XOExxSspldbd7rTR9Ka+ZD1e/SwaEyURQlNbBs0Y" +
                "o9x9nE26tzHm+n9xl1zFB7ZJO26jP9LLud3XjcORUTikJOyJuv" +
                "RyXPxFWdesS4bf8UbOzl891c75+Cjb1+vuvkPawk3seDm00fSn" +
                "eWivfxw+6ceZTUwLJFK/YcZxNvr+7i5V+brRhjtf+RF3Iq741H" +
                "e89j6JRqdJ9Zq43eCD6KQrvSlfPn+j53fU7Pok/mMciZZ6TqlW" +
                "Frx3Aeb5d+TGbdbtmkjX26bs9SzePOqZhQFOXk7eK/D1Z+Knkm" +
                "ruqcB/4fQfwpe0cu5GLl7w67+qxtwu4LzxTHh+S+GyarhfzdYc" +
                "fivzdP2Tf7S2SetF4FnyBX9yp/FUiwlTX6EX/V9tdiD+A4GzOg" +
                "ruetf42P02tCDr3K9ikapfv1atjy9aRUv1fByvoIcAzACqaD6W" +
                "RmpzKd49PqLxmnz6o+6AEcZ2MG1PW8/g4+Tq9pVsSsGKX79WrY" +
                "8vUgO2XF5TbP2cTn7uOfy63u55W6Z+c/LzjR6vfj7teZbn4nVl" +
                "Sv9O/EpsWzbHw2Tv5VvVtRk3Eam+NxmuVdDZlPX+rpfGrP8Ti1" +
                "ezuf/Ov860CCLf/E2OiXVR/0AI6zMQPqRt5RPo5WUDH5KpSlyU" +
                "KvRtooj8Fs2Ygauc1zBoTt+a6+/P/5+Q7Z9/b5ztujH5Pr7itg" +
                "Y5+u27NU87hz1mrRTTqKcvL2ROvt9ajkmbiqmx5vQLX0ar86Yz" +
                "cPfBSFdrVXfIdjdAd43YVBznyE+l10GbZ2jCsa7+PxN3t1H5fr" +
                "3Uyuu+is6vdxsbw393GNR/ofrtHSaJm23n017R2wjxN+jxB1Vq" +
                "To5flxivqic1v98V0leAwm7VVF6110vmZdCOMUEXbR4vhOYp2d" +
                "ygX6OEX9UYn/Ho4uiF6pPWs+qCQZp9tNH0rrE6vVG99tj3HnzK" +
                "OkBpYtWl13HGcTb6/OeYLHlKxn+zPUwIeSf9XHXF4rm8fKRFGU" +
                "1MCyRcf32jibeHt1Fy/8HiG6qHAPZnx/17rEcbc8PYP7001O7/" +
                "qZ1PNerPA53fOq6eNUj8FHUWgz86nLZziaM5/f+yuHQc48o/g+" +
                "u89evawnOdfP57UOX8e3bMpHI0y090LJ93dD+fFVWD6ibL1uX/" +
                "Vdea1TebWD8WozwoV2nvvqqlg+Inrd7I4T/t9GfI/Bao2DceqL" +
                "VpRDO899TVUsHyHeOFsj5KW/LKtn3+3Vd1aeiztnh1n0hmLeuZ" +
                "iLe3ydLawvRKm0jlWvLwRE1qdp1FbRek7QsFfPY3ogByIBo0fp" +
                "XlqNtLpZDxnS2uaZ5M9ZWf5Kf2WtpmT6GcJK9ZfsM/epPugBHG" +
                "djBtT1vHwes6ZZUf55xzFjnoVeDZt3nMdAfvK5ipHb5GriO/P1" +
                "8ky7oodXzpXeqdm6X0SXsVdjr+v9F3ikq2I=");
            
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
            final int compressedBytes = 2563;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW22sHFUZ3itqEw0f0TQYSAixSEQI/0pCSHDnzow1gUjp7R" +
                "etJQH7g8RoroHEhj9wd2fZ3PVP+VEx4QdBi1YFwSuQmKAQYmmE" +
                "FGiE2kRTrAIVb6EtlIK3KO6Zd595z8c7Z87uzt3EuTln3s/nee" +
                "fsnDOzs3Mbjbm9jXybezLvL5r7PenT5zQG29y+hrjN/U6TX8r7" +
                "V/rtgBD5bGOMbe63Xu8LkpWrN2KfLqT9w9WQfSObzr6WNQv965" +
                "C6vxj2eLKk1HNddv3o45TFXu8aydq+IhA7Ha2mVjE687urYtuz" +
                "4bjp1BjjtM3r3Vo3X1BN64tx+mmduMk/JztO4/D5tminue+P02" +
                "+gky1aZ/r184l8uhcWG19i1n12nhSFmlRs376u/Hj0Xq7EZC+v" +
                "Obt5bm/2LWkdj39gr+PZraOv48m/J7uOy3zuOp7dEl5H+2d5/0" +
                "sap4HtJ/NP5XttNW+dXYrw4/av8/2D7UfccWrvaZ2bfkrMe6jf" +
                "fmXZHneiPu9dUT87iPq5lvGYztcWVtrQ61370fYC5OaH1HLWYn" +
                "zn98GnR7E+3JZ+otynY5bh2zFcs5xBfCHVlsfYnug8avk48/r0" +
                "Inx6FOtDruP/8ayN58myL4ZrljOIL6Ta8hjbE62gZsy73vxL8O" +
                "lRrA85Tm94Kl0hy74YrlnOIL6QastjfNkYp/759GdnJTg7aNV1" +
                "16eTrXOTt0dfx8PWJ2ucKviC16e328fF+/1vutL423hY/uy7L6" +
                "ifsWS9WqJmrU9/gU+PYt2/zf/V+nzf9PNLcnHGfMeN4Zrliogv" +
                "pNryGF82z7vsIWu2bvXM8a1+S+vbwfe7W+VxKo+V68p2N5Zxm3" +
                "8n4+931ryMbvIc3U1VluRUJfeJKhaZQ86o5hvz+93DRd0WU7TW" +
                "U/HaKkvyr8pxer+KReaQM6r5hv5WUPL8af50nc+fkhMT/t5yot" +
                "7nT/HR+Ch66KTNnyEb/LznDNYpzsTUcZOTLo4ezzmsm1W5VXA0" +
                "2LgpPrdaHc0dAakCN96+3qU7qRVn+Q2eGXCD39K6K/k4cC4JLO" +
                "lOX6xcVyhf8H3y5cnl6KGT1ruSbLAgTtIZgWUL9yM5z+S0GRmV" +
                "s0y/yaa1j+QYRnNHQKrAjbfPp2yhvk+jdddY15YRntO17lze61" +
                "0rg9RbbZ3lM57ZMlNlSd4JnHczQ1zvZsozQvmC592mZBN66KT1" +
                "riUbLIiTdEZg2cI9JueZnDbjoJKv6lmm32TT2jE5ho/ZHQGpAj" +
                "fevh9Ppq1Pb73zHXG23GdbkvcDz5H1Q5xP68szQvmCz6c1yRr0" +
                "0EnrrSMbLIiTdEZg2cJdkvNMTpuRUTnL9JtsWluSYxjNHQGpAj" +
                "s+utfc98dpBjrbTNn9XUr3hnvKfbyO61F6TdG98jpOfr2XK/Gx" +
                "657kmuQa9NBJ620gGyyIk3RGYNnETT8t55mcNiOjcpbpN9m4uX" +
                "xA16uSsO1jHmhrk/73I+pz21r664/TNrLBgjj+a8+aHoqGbOG+" +
                "JeHYnDYjo3KW6TfZtPaWHMNoxThZ2PYx2/GDz32xONv/VOc6mK" +
                "5YrjsZ+TndcvBFV1Ezr3e92+DTo1gf8nfzT/r5JdkXwzXLGcRX" +
                "hlaG7POUPS/ofdd+XpAdGuN5wfEJPy84Xu/zAs843V7rc5V3Jz" +
                "xO74aNU/Za4Hx4OX0ZPXTSeneQDRbE8V971vRQNGPquMl7Eo7N" +
                "aTMyKmeZfpONm8sHdL0qCds+ZkSUnk/fr/V8+mDC59MHdc+7Rq" +
                "N5hpq1ju+AT49ifcj7/kU/vyT7YrhmOYP4QqotjzE9OJ/sceo+" +
                "NuKnf0C8px7juW921Os9Jlm7jwauT4tjr+PtceZdLzPnXXrWZO" +
                "edzGePU3Y6eD5sT7ajh05a70dkgwVxks4ILFu4r8t5JqfNyKic" +
                "ZfpNNq29LscwmjsCUgVuvLM+We8lRVs892pbqizJfwPvebcMcX" +
                "+8pTwjlC/495aleAk9dNJ6z5MNFsRJOiOwbOImp+U8k9NmZFTO" +
                "Mv0mGzeXD+h6VRK2fcx2/CDrY1cafHqbPZ/s5ipLcjTwHNk8xP" +
                "m0uTwjlK+G+/EXar1/OjPZdVzmc693nc+MWFOxPnWs94qiGz2f" +
                "7I1VltDX2X0scqyckTbq36LV1PLfW4rvyJ1z4NOjotUj3md+6O" +
                "eXZF8M1yxnEF9IteUxuifeH+9HD5207hNkgwVxks4ILJu4Mo7N" +
                "aTMyKmeZfpONW/dxOYbR3BGQKii0g/FBYx3PddXSq8kDHXFspW" +
                "iWTAzx6noQ+GYMo+hxcm16Ta61mHdX23Wafrk62SIdjzbvPmed" +
                "hRs95+7GakvgGrBx2Fg5o/t0zfdPh+JD6KGTln6PbLAgTtIZgW" +
                "UTV8axOW1GRuUs02+ycVPVSzGM5o6AVAFHlN0XdJ+s875grDuX" +
                "Ee4L2tvqfa4S3xffN32S+kaDJNKUnN3PFsRJOiNAZkkh6cjkYx" +
                "bWyOeydJ/Ss0y/fgS0pybztR4AHzGDDRFqj1GBhbR8P4U+nooH" +
                "73/2pSn25dLu2PqfNs03xX7TCqRY/H84sMiogfpu8OMYZFwz2z" +
                "2+ke/HX61z3qW7JvxcZVf9zzNLrndfsK4uG5w1YLbcZ1s6/wi8" +
                "hm0Y4nq3oTyj+0xjWTdtnKzfD6NNnoo3VVm6ewOPXWDpXOiLle" +
                "vq/qHm+4I98Z7m36hvNEgiTcm6BXGSzgiQWVJIJg6xsk/HNCMZ" +
                "lbNMv34EtKcm8yl0ZCgJbIhQe4wKLGDvxy6gby7ge3Bzga3Fc3" +
                "XrTUTW9VjXSsjik/oF117OIuu6DUwSrh3pHl/YOu48z/x7Vab8" +
                "u7n8O0J334R/R3iu3t8RkouTi5uH85E9rJrSlbV5WMlkUTrZKA" +
                "M9RZPEfmCQVfWEDDbCQh7bGRV+xuZa4Ucd+hHQnhrVzdUiA2hg" +
                "ZnRGtC3F8RxB3zyC/3ttHmFrcc725c5lpq5LdgZ0QhZnQe7rfN" +
                "m02TH5Wr6K9c6X+vpXpBwwofrOpS6jfXydLwadT+cn50+fUnvV" +
                "T59SOmlKJovSEUEy9RRNEvuBQVbVEzLYCIv+yEc6o7IfOtcKv9" +
                "64MjCheq4W0UAD8/QpfSzcsSmqXpX0Pynqc9sq+uuv8il6trGs" +
                "/thHEuUDzcSVcZRd14BhsjCiyarXbrY4dfmArleFuoDtVmDHF/" +
                "dPxf+bx9dJ558+7zhCjjXuP0oi2j+0552c2dEqdeedg7prkHVp" +
                "dV2B8+6S5JLma9T3Z2sukaZk3YI4SWcEyCwpJBOHWNmn281IRu" +
                "Us089HAEZqLh/QkdHM3+jhajmGK2ItXUwXox19eaXqox1KV28f" +
                "RjuUTBZ6G5Hi8GYifBSvPPADg6yqJ2TKAn66eHekONnOqIhj7M" +
                "F3No2LWrJSvXfIlYEJ1XO1yAAamBldf+dycA6t1LNLnz/98f/5" +
                "+dM9K2v+Hvw/cbdgLg==");
            
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
            final int compressedBytes = 1887;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW0tvG1UUnlUXjVAXbLriH4BCk7SFKsSesZWGHY+ukJBaqe" +
                "IlSmGDBIvGcSxLsYQqgRAQSpeUQnkVilhAoA9AQgEqdYHEQ4po" +
                "ig30AQtUQJWY6+Mz55x7rz3D+M4tHmtuzvP7zhzfe2dsK0EQBN" +
                "H2oDdG22s7gwA0suJRfykQB/lAqi/rGYSkWyWLHTWbzm3IZMPV" +
                "I83rG3REK9FK+UkYgwAk0JTMLRhn0wkBZZIUksQBVvJxTBlJqJ" +
                "Ql/XQFyAinyadGhY4ZSkI2xIYYXhFq6m+4jmO4vrAV+heuNybi" +
                "GXIIfGiTPbbrZEUdkCmu+QVF6BgmalzFK4P9ZEMmG646Fg5TpF" +
                "5t2lE7I7SbUFpqp2XW91vxztqslfeC3MfizwO9v2Xnq60k0mov" +
                "+9eM624tfk3DGGsggS2WhWWtF73GYoQEePWnMTZBn+6LAz5ml5" +
                "FJXTxL+OkKutJ0chp8iA5a/SklIRtiQwy/TtSMmbkVpca2fPPJ" +
                "flReC7wervlKF0sXaQRJvepLUVnpasQXeUEiX1SGbImJUvc9se" +
                "CAXaJiPtkwAu0SSbLhGZVNPqqQqsK6OKesQI9P5tOlZH+66HI+" +
                "+T4aGe/3C7/nxN/R68HS0iWn6+BNz+suI19jOmefSsl8uvy/mR" +
                "vlHDkzGePCjPvTlfjVSUaQQIvl2E8W5hV5Xalr6XQRO0kE4nYk" +
                "Ti8b0DuogV1j7OoQkcQJJKokYexg9ZIP0ZMMvVodU2qXS/G8wR" +
                "Ek0KJZNdb3oUV61agi4BXNQnav9wKtt1/Omjhgl6iYz+sgRMlK" +
                "0ciGZzRr8lGFVBXWxTllBXp8sq/txv2pUXW6X3zgeX9yzFeeKE" +
                "9Et+AIEmhKjvudWLiX54GkLGrEDKVjlLJzHLADOmSSXUaCDhEY" +
                "J5GoEqgAT5MP0XlVvFodU2rYr/nep5f6j8k78rKtr/M574KN+/" +
                "r75q+l58+vMvlcfH6fMp+61dc2ZUC+0PcT3cMDnifoefx+p+vg" +
                "Lc/rzjFf1Ik6pR9gjHesrgSakrkF42w6IaBMkkKSOMBKPm6XkY" +
                "RKWdJPV4CMcJp8iI4ZSkI2xIYYqoi0bmy7ure6V43qDAJ1ltql" +
                "NvignxiBfjWCD6IpljIQV1kA2Xgq6foQD20yEiLQhqi8Dsyha0" +
                "BcitLR6Powikb4S5yD1l18v9vtdB2843ndFcxXPZasyLnUNTuX" +
                "JzabvX9E/ZkMu8lcOm5Wfha7BcdoS/VTtJFVxtl0HmtGAbKd2b" +
                "T3Z7Hr3IZMNlw90l7toCNs4xi2Gw+iTemLy2Fbj7PphGCzArKd" +
                "2bSbLM1Tg/zchkw23N73vm3JHLbzrTv2XPCI0/3idc/7U8F8bB" +
                "/f57TuE577VDBf4/FEesJp3cc896kAvmoTx2qzdhBtoIOPx9l0" +
                "QrBZAcnObNpNFmmxIaENmWy4Egsle1z6wX6XyvmNqO13qYVNlV" +
                "fzv4/1GwfuFGPW+ZTCV1sdbm6xPv1hVHTDEH066rlPRwvu08Hk" +
                "+/Grw+As/aXV/b7n/algPjafrjlEXascGSL7zxx9OlJwn5L5VJ" +
                "lIXQ/a71K15wbU/a7n+eSYL9wcbqYRJNBat6qRLNKrxvp+0iFb" +
                "YqLUrfttE4fHUw5nxBiZJb2cjU6Tjyrk3BKbY+oM9vkU92mr0/" +
                "n0huf55JivsquyC0fUQWttAxtaMI5e9f3SA9GEKXCP23B0Tp2R" +
                "UClL+iUbO4/bYwjN7ICtAjNe38dbtwUjfDS/cr5z935BqJ2APt" +
                "U+7vV5OYn4HP4ufqtlfsTkr7vjOfvzU+3kUBV+OND7pXWdLFtj" +
                "V/I+P1X2VPbgiLp6RRujjZU9i8tkwTibTggkS1yFZsvjnJBjsj" +
                "S/4VnSL9noNPnUuHCYrtnsgK0CM95Yd3f813184Do463ndnfb1" +
                "nNmacdmn8DO/fSqaj/WpHLOtOqt71W+uPSd/FWEpLNEIUk87oE" +
                "ayaF5Nh2yJiRJZJY7uQQyKxBiZJb2cjZ0HdD6qkHPrV2JeM0b0" +
                "u9+1dur3u8YL+e93zfN+73fNn7Ld7xrP56zpcLLu5obZn1p3at" +
                "+Qveh3f3LNF46FYzS27lESaK17wzGMQJuUuK5kHROlbt3Pmjg8" +
                "nnI4I8bILOnlbHSafFQh59avxLxmPd6YT3c5fX8PpUW07vbLl2" +
                "NOsf/bwM/B4XprV2H/t3GBIq73/20M+/1T/P4+4PT9Pel5fyqY" +
                "j/XpIad1n/HcpzPe+vSoy+fx6mnPfTrtrU+POf181/H8+a7tq0" +
                "/VhVH+/qno6qlPzV9GuU/1c74+B1dW3K272tpQNeX4Xap5ydd8" +
                "qpwa5flUdPWsT5+Mcp+al/30KdoQbXCJ6xbt+vMlfZqJZpzWPe" +
                "O5TzO+1l148yivu6KrZ88FV0b6ueA7X30q3z7KfSq6etanHUPd" +
                "b65e5z7t8Lbu/hnp54K/Pd3vxqNx4x4ynnKPGe8flZbr/H43Pp" +
                "w/c58mo0kDezKFe7J/VFqu8z5NDufP3KepaMrAnkrhnuoflZbr" +
                "vE9Tw/mN41/ISgOf");
            
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
            final int compressedBytes = 1597;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW0tvHEUQXiQkkHBAKCgILEWYCyCFQzAXZCRqx15xQkIcuP" +
                "MHwhLjJDyT9Y6dkffCiQM+cOF1yiWRI+z4tbFNbCsJL+FIJDYH" +
                "fgCCH5Ds7Ka3qnuqe7dmehtlrJ2qmvqq6nNtde/YY5dK+lH7Qm" +
                "mNaqnHUe+JwGPidinoMeh6pE8nvfLeD9yn/TB9Gh8ZHzF92Suc" +
                "H1EU3yvW99EfVy/zNOV13d0JPE/e69U278uF9vlobeV+n053ET" +
                "9ZIpeJfrN9/r31+oVBNpNHCzD80end5a6e32Cxq13tup95qs+N" +
                "j2Xm56se8zXfnugxds7HAq+7sWL+vven4fHhUime13IP96g9rK" +
                "Movles60geydEntt701z74GPvTxz7f32Qo7DwljwW7L/jMZ97y" +
                "3bB9GnS92tFun6YzU3uorwzMPj79RPRQr7hG3brvHXbFTbOT06" +
                "uer328VemF0gN8DJo99ik59CD3KS6F6lMj8bqvPh62T7NvB+vT" +
                "vNf78bOB78fPhulTfa7xTZE8jW8N3rXAfaoF69N3Xnl/GrhPA6" +
                "6n7gtafVryuu4+CNwnz/Vmtgx7s9unVa99mmRqbwywT5N+81VG" +
                "K6N47mgdq3E9PeMV3Zue61W0O9F6TqW1eVezeSgeY2hFhdGjdC" +
                "+thq9sPWRIa+u5aU6zgvEeb3fn6abXeToXeN15rlc5UTmB546W" +
                "frX69HNq4xXq7WiteeranWg9p9LavD/M5qF4jKEVFUaP0r20Gr" +
                "6y9ZAhra3npjnNCsY+/kN3nn71Ok+fB54nz/VgEibx3NE6VuM3" +
                "mFQIdU3X6lW0O9F6TqW1eU9l81A8xtCKCqNH6V5aDV/ZesiQ1t" +
                "Zz05xmBWN/+qM7T3te399PAs/TgOvN3Or26ZZX3h8F7tOA65Hf" +
                "P932uj+9H7hP3uvxz1ta89R9UujjectEgd+O5nnewtfLPm+JD3" +
                "Kuu25c447XeTo9sJ1iP0w9uEYlXFN28qSJsEXSaB6NWYswlHjr" +
                "f7qxcj7WdXfgdd2dCbzuzvS37gTv1h6VsNeRrT79ZSJskTSaR8" +
                "PeRIGnXLAn9/L1EAvCu57KjcoNdVZ2+lWfS3V6ReE4GzOgrued" +
                "mObjaIVsRcyKUbpfr4avbD2VnbLicpvfs0LY1h2ZVh/r7lTgdX" +
                "fK97rjP+9on7zcz5wsDeiwfN55r2f7exW/81T+Nz/D+pR8npIj" +
                "3vfxBSphQdmVWRNhi3RhlNft74+hxIvs+8/Ug8VlKuGyspNnTI" +
                "Qt0oVRXre/P4YSb/ySPFO4dWet0Sz/E3YfP/+9e93leLeuUglX" +
                "lR09ZSJskbrOoTFrEYYSL7LnsXI+sEElbCg7edZE2CJ1nUNj1l" +
                "x92pB742NurJwPLFMJy8qOpkyELVLXOTRmzdWnZbk3mnJj5Xxg" +
                "i0rYUnb0pomwReo6h8asufq0Jfciex4r5wNrVMKaspPnTIQtUt" +
                "c5NGbN1ac1uTd+zY2V8/H9eWe5z/wv7Odd8rT3+8wVKmFF2dHz" +
                "JsIWqescGrPmmqcVuRfZ81g5H9s8JSM+56nQT1Y55il+3fs8Na" +
                "mEprKjURNhi9R1Do1Zc81TU+6NRt1YOR9YohKWlB0dNxG2SF3n" +
                "0Jg1V5+W5F5kz2PlfGCVSlhVdnLMRNgidZ1DY9ZcfVqVe+N33F" +
                "g5H1inEtaVHb1lImyRus6hMWuuPq3Lvciex8r5wCKVsKjs5GUT" +
                "YYvUdQ6NWXP1aVHujd91Y+V84AqVcEXZWYTrOuocmssqZyjxlm" +
                "fcWKXxODZ2k0rYVHZ5zUTYInWdQ2PWXH3alHvLa25sET7pMXOQ" +
                "P7ZyJJ+vIOP9IvX65wXbVMK2spM3TIQtkkbzaMyaa5625d74nB" +
                "sr5+Pv57t4wvF/ry+GvR+f/bK/+/G4Er5PFev/f9WalcNh+8TX" +
                "y/7et39ecJFKuKjsLMJ1PdWj9+w1bDkkDCVenksRFh738aF8vo" +
                "Hs40PFOfvqk+Tvn5JXwvYpOe67DuxSCbuwa0O4rqd6VE3PHJrL" +
                "Kmco8UZVN1bOB3aohB1lZxGu66kOF9rnHQ5pyyFhKPHCBTdWzi" +
                "fM3xckr4b9vEtG+7sv+H/6ZL8vKD8ctk98vULPgy9RCZeUnUW4" +
                "rqc6/G2vYcshYSjx8lwKsLgHx3XWMg==");
            
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
            final int compressedBytes = 1811;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW01oHVUUHkW6KaWL7qp01a6LbgJB4c1kHppkJUgFRVP6sw" +
                "qFSEpBmliT17xMFqHiUilSkKopRRGqWLSCIi6CP/1bFBcSdaFI" +
                "kwoiKgq+N/edfPece+a9eW9+Mq3zuPedc+4533fmvntn7txJPK" +
                "92yYsP8127RDoO1+La23LtB89L8kzCSHN0j9Vb9VyyZMGPuT0k" +
                "+Z9nwTk906Xtxf6wouH++bNlr/bMF53vD0w/zV1xPL5MiPzEkr" +
                "/xvODY3I2WdFXx/Gzh7wwZftS1dVWzBsdU3083pa86v9n3mcfT" +
                "zV6+jam0qPOTWX7JxscDjKeb+Y6m8Ex4hmrSSaMaNshcBwJkjh" +
                "uN6XGcUzICFVG8nbOhRKO6D9DcHtAycP074+kVr5Cj9q9X6pE/" +
                "X37Xp9pK8vUpU4YDXJ9qK+muT9nvdwP8hu8moq5lyumPPHPJe1" +
                "3Q/xFMVqefgsky+yn4Lh/U+cksSIPE6jF5nU+28RQ9W53xFD1T" +
                "3XlXu1ih69PFAu+l77fWpyvd23vFF3HUfxnsXLZqPM391SO3H7" +
                "diPL28u79csh/R8937KXquRz+dr9C8O79142l+R4/c3ooRlHXm" +
                "/M5Mz3e7ut5Ltyfn0uU8M6wzo4M9xtNEj366VaHxdKvg8ZTh+a" +
                "72nleZo4hcgrNUB2epn4KzRjdttp+mA4Gj2Uh6nMTUWLjFjYCN" +
                "mDRcjkWS7lfs+qkL6lp0pNx5t7gr5/2n5XCZatJJC5eDC7CQn6" +
                "YDATLH1XEkJ2FwluCCHcXbORuKiZE+7fPhWdk9oGXg+svxFJzs" +
                "e/6eTH6+y3S/G2A/s//sB513wUzfuc1UaL9gpqx+itlWqnDvGi" +
                "QLPSbL+STtZ0YvbHqk3M9s1ZV539JQd1ByfN/Sx/op/fuW8o9o" +
                "OveZljCegqE8x1OmDAd5fzeUbjylnsezwSxqIxnN6LDwVqmTt4" +
                "1JUvz7HndxbH/E2Izkw6N4q82GEm2XfMjQ5pZn4p6z0epL9SXP" +
                "M3W817NkPq3Wx4yNLOSn6UCAzHF1HMkpGYGKKN7O2VDa2Ws+QL" +
                "PzsrFlrtJf3u+iE95dfJz+MF+85iOJ97tZeX1qPjz49am5v5r3" +
                "u7RHfbW+itpIRjM6LHar1AkBGLYU9/pLepzN4DICFVG8nbOhLG" +
                "7TfYDm9gDnk+yix9/oMvaGMtynG+XOu8VtBax3r1EdXJu7Qzaj" +
                "mzbbT9OBwNFsJD1OYmos3OJGwEZMGi7HIkn3U+bdqfop1EYymt" +
                "Fh4a3tujHletuYJMX5XHZxbH/E2IxtPbhsZ2lnAW9iQ3H5kKHN" +
                "Hb2ahCkZxLy7U8x6fOTJcuddWr7ozazrcf94VdbjzUf7v98h+3" +
                "zud+G58BzVpJNGNX0ar0FGHKTG6+3vkRNksXGN1Y3jnJIRmSCK" +
                "t6ONF5eP0I02csLOy8bmGYA9aZ3pT+c378rff/Kni53XzZHNmX" +
                "vlbl6PL9yf85rgQHAAtZGMZnRYeGu7bky53jYmSfHYfcDFsf0R" +
                "YzOSD4/irTYbisuHDG1ujm1jSgYxxv+p4F7S9QHm3cF8/Zx597" +
                "h3TxzNekq/J1Le74bDYapJJ41q2CBzHQiQOa6OIzklI1ARxds5" +
                "G4p/RPcBWmc8HZHYMlej1dfr660VZ1zHa8918zGybSE/TQcCZI" +
                "6r40hOyQhURPF2zobiH9Z9gNbpp8MSW+ba0TbqGy0prmPbhvkY" +
                "2baQn6YDATLH1XEkp2QEKqJ4O2dD8Q/pPkDr9NMhiS1z5f76e6" +
                "m8jvL/rie6kfIeMeD/d9wr/eRPpLzfTfzP++loyn46mrWf/L1O" +
                "RjtSIcTPwQsHkvtp4akS+mlvjyyz/t/G5ntOv+eOdpXfc/r7i8" +
                "Uvat75D5Z8HV/L9wzCq+FVqkknjWrYIHMdCJA5rv+QHsc5JSNQ" +
                "EcXbORtKtF33AZrbA1oGrr9zfdrj3cVH/tknvr/7Kc/9zLL/Xm" +
                "Xh6XT7mWn/XiW8Hl6nmnTSqIYNMteBAJnj6jiSUzICFVG8nbOh" +
                "RL/qPkBze0DLYFMbDUdbUlzHtlHzMbJtIT9NBwJkjlt/R4/jnJ" +
                "IRqIji7ZwNxeUjdDsrDVuec0cbD8dbUlzHtnHzMbJtIT9NBwJk" +
                "jhut63GcUzICFVG8nbOhRLd1H6Bt9pPAlufc0cbCsZYU17FtzH" +
                "yMbFvIT9OBAJnj1i/ocZxTMgIVUbyds6G4fIRuZ6Vhy3OW/p3V" +
                "yO6C9iR/L/d+t/hzzvvj+4J9qI1kNKPDwlulTt42Jknxb/K1i2" +
                "P7I8ZmJB8exVttNhSXDxna3PJM3HOW/v2Pp36eW8Jvyx1PefOF" +
                "Q+EQ1aSTRjVskLkOBMgcN/pTj+OckhGoiOLtnA1l8T7dB2huD2" +
                "gZuP69n+/62S9g4+63+Z1hs9znu158afcLGrcbG933C/Kdd/7b" +
                "JT+35M33H+Ax5dw=");
            
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
            final int compressedBytes = 1753;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWs2LHEUUHz25LrIsfoQcdgnCgkFxUSEXPWRqptV117AHL+" +
                "JB9BbXiCYXWRJxZno/GkFz2Bhx0b0IIcEV9KAIfkBY8SAiES8h" +
                "p5Bk/gRhogen+82b915VdW9/1JSOPXTN+/z9Xr+p6q3tmVpNHq" +
                "3Z2giO1vX6ZxWy/yyeU4UvV02j6tMlz326NJ592rztt0+bvTGd" +
                "Txc9z6eLY9qnC577dMFXn1TDZZ8qZZfok8vqfc6n5g2/farCl1" +
                "LFT4P3r6FPrR9AD14bRvyckvk9k39Lxj/65xWZndgvV6rw20zv" +
                "Lzar5B/G/jiUfnU1n4LXq+DIbP/rrlr1hfq0UqlPK/9yn1a89e" +
                "mE7mvfkwvhipndnqpSU+feLG970tqnE/tUWWLdqW0c1Tb2SW2T" +
                "VcbZdB5rWvuo1+04yGJHRV3OJzODbHANdlzJZ7u+fft0C0d1q3" +
                "WW2zqfwDu37aeTFXVAZnvlvyhCxzBR+1V8mu0nGzLZcJO5t0OR" +
                "9ojR7gvqq6PbP9VXS+0zV8d0/3Te8/7p/Jj2actzn7bGtE/nPP" +
                "fpnK8+Nb8b5//vXFaf3PcX1ELjThxBAi2Ww22ycC/PAym2xGOt" +
                "BlbQEQmtPDt+Bx+3y0jQowd4lkSiSpARTpMvHts7yAfMvFodU2" +
                "rJew9H1Wv9PehfL9bDbcWedynt2RfphGCzArL1U+qZdpMlmsny" +
                "cxsy2XAH+4KeZFY5n+epsP/qDkeQQOvL0YPMwrwiL5ESSzdB7A" +
                "4jELeLVpEdJtYuamCXkaB3jvMsiUSVDBm7WL3ki8d+n7rDDL1a" +
                "HVNoRt/WTSn3Gs7IUHs1r8eo+dR7ppT6f9ebablOK/pmlFnRoy" +
                "Wr+sCUiue6PDbuK5MVzbuvJNjEMdjE/++CTdDBx+NsOiHYrIBk" +
                "ZzbtJou02JDQhkw2XImFkj0ux5x4n0tqt1yuxbdbYeXtusqpUk" +
                "Xac18WMXjuu/aRZs947qtFXo4eq1Bhiee+G9ZnVuZz3/WXy9XU" +
                "fgTeG9ON6Xj/5Gptx2hlj+ghV3ztndL3pk7QoRGk+NWYaEwEne" +
                "hhsnAvzwMJsiUmSkndEyYOj6cczogxMkt6ORudJl88Qp84t34l" +
                "egVmvJxPzv+6tzzvnxzztb7U9IJ3utZXOdfOE5bczwtyfVFgrT" +
                "7uuO/TappGkEADnSzSq+sYzTFRSubuxyYOj6cczogxMkt6ORud" +
                "Jh9VyLn1KzGvGbQq8ynej+edT2s3PM+nJ93Op+aR5hEcUUcNR7" +
                "KRLHVCIFni2nF0Tp2RUClL+iUbndFT9hhCMztgq2CoLTWX+lIy" +
                "JrYleIHMLRhn0wmBZIlrx9E5dUZCpSzpl2x0BqftMYQ2uB+c1r" +
                "H1WkFTp9Sp/gocjCCBBjpZpFfXMRozCI1bJY7uMRkxRmZJL2ej" +
                "M3hb56MKOTfE2TA17aQ62ZcGI0iggU4W6dV1jMYMQuNWiaN7TE" +
                "aMkVnSy9nojJTORxVy7qiRhqkz+Pgewf8RnHEbpwIV0AgSaKCT" +
                "RXp1HaM5JkpklTi6x2TEGJklvZyNzuAdnY8q5NwQZ8PUGf6n82" +
                "nVbZzRp7PDlbswzn3auO0WTx1Sh2gECbRoMR7JIr26DtkSEyWy" +
                "ShzdgxgUiTEyS3o5G53RczofVci59Ssxr1mP1+eTumuc55Pr6s" +
                "NjoRL6sxXuCfensiym+3LUmPnb3fWtYrWUi4v7FD4dHjX7pO4u" +
                "fD3N9D5V+iwz+xQ+Y51POasPg5z9nIEX6RXm00wWyyhw0715+X" +
                "LHzcKL9ArXM5vFMgrcdG9evrxxLu9PGSyLzWujWncpz0GuOb6C" +
                "1PtTic99MuM+Pun3/pSXr2xdtC8ofrQz7tbNq373Ba751Lyapx" +
                "Gk+NWYa8yp+fh7KbTENpR4Hkix3JiL3yEKdPShlWereV4DVcAZ" +
                "QY+el1nSy2un0+SLR/i+Ba7F7IDE1BlS95mHXc4n7/vMw6PFbx" +
                "8bfP4HGwf7n+Qy98WWrAP8FMXj98vNfA5/vHiOnY++5yxaj1pU" +
                "izSCBBroZJFeXcdojolScr94w8Th8ZTDGTFGZkkvZ6PT5KMKOb" +
                "d+JeY16/FyPrk+ohc8Py+4wzWin99hVKqwxO8wopesscbvMNY+" +
                "/G/1Sb3ot092vuq/V6nflO8oxSPZpJyOYsalY9RvZmNiDRLbrN" +
                "fGx8fi7DZP/ap853Zuq2fu39K9WZ56jj0hj+I1peWCnY/2SrLY" +
                "pSdt3allfd2Fr1ZYd8ue191yvnUXvuL+/mTYtT4FB9L7FBzw2y" +
                "c7n9mn/HWl9am+53I++f57V9/zNZ/qv4/zvoCq978v2HfdTWWs" +
                "uynP624q57qbqtqn6K1xnk/hu473T/8AAF3rKw==");
            
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
                "eNrdW0uMFFUUbRZu3UBcGMJC3ACJG8OCADJdVbPmNxqEEGNc+C" +
                "EYdgMMRrt76LaNC9lqjIiAzCyMMWSSiaBAICYSYxDIZMZEjJ8Y" +
                "Fy7YGBImalXdun3uve/1Z7qry4GqvDf3e859r16/qprpKZX0Ud" +
                "1T6vsYfbQ/3zCOXvl6jSvXyjX0JJFGOizaa/VEHl2tMVlK61nt" +
                "4sgakCMZOUZnaa9kQ3P5GF1WxXE+TMvQ/3qqHeo9Nthb7HoaNl" +
                "/96xbT9lzr3l7wPHn5mi/lhV99sfVp+NZhea13nMljHXxHhj9P" +
                "bvWDHdFcNMc966SVr5GNLdFc83XIyNMILE8e07gSZ/II8jSnZQ" +
                "Qq2LQfPt2S6t2Y5kGguTPgq8CNp6OyZhhXt3ogOjtA9sE+rv/Z" +
                "nPejX+s36zfqP8czNJPqtytfOXP3TXecyvctxF88LHPR6QFqvN" +
                "XR+5N3nrrw1e+k/Y/91jSc9RTX/Umx+3j+fJVr2c8Zmqfe11Pl" +
                "ol1PlfjKV254Iq80B9i7K7Mdvdd91sZKb2zrbl75Lrun3Onzfr" +
                "rflXK4S+8vOjvP6ov73FUPDJTdxz4+8k++ccG+YB96kkhL5Ppa" +
                "WKTX6owADCml9uv+PK4BOS5L/arM0n7NJtp1X0z1I4zZnQE5cq" +
                "s56+m9Ae5Lf3W4HrcLfh7PmS8YD8bRk0RaKi/Aor1Wp2yNyVJq" +
                "W3BxZDxyJGOmL+gs4xVsojl8PB5ZlZ4BjWkZzDvF9HCub/NYse" +
                "upsTrn9bQh2ICeJNJIh0V7rc7REpOldJ7ecHFkPHIkI8foLO2V" +
                "bGiN9ZYPFUpuOxJ3zDY+258Wh/N7laKP47/k/Ny6LlrHPeuscQ" +
                "8bZK2TNHKOctgicf04ltMyohJkab9mQxs5548BmjsDvgpa2vpo" +
                "fSylfWpbTyfJ0sJxPp2kkSnKYYvE9eNYTsuISpCl/ZoNbWTKHw" +
                "M0WZfEtrVyREHvLe8sz/eW5fZ+N9A7Qh/zFLyc9zzFmCe5D05W" +
                "J9lGOvlknE8HgkaTSP48i+lj0RY3AzZm8uFqLJb8cZ7MjcFG9C" +
                "Rl2olgY/NtWIzX6Kl8QmOylNpOuDiyBuRIRtJrr+os7ZVsojl8" +
                "SZ+9twhuOxJbQUvbHGyOpawniTTSYdFeq3M0ZwAte35618WR8c" +
                "iRjByjs7RXsqE1Vlo+VCi57UjcMdv47N18spV/eMmf38OlZXPk" +
                "X0u7fTzY9EDv45uGe7+r/zuc+130fLHz5Ofrf56iLdEW7llnjX" +
                "vYICdn7ZD2cA5bFO5eH47ltIxARZb2azbR9vpjgObOgK8C1oKx" +
                "YCxepVlPEmmkw6K9VudozgBaVk3k4sh45EhGjtFZ2ivZ0Fw+VC" +
                "i57UjcMdv44b4HB+cL3sfP5xsX7Yx2cs86a9zDBlnrQICscZsf" +
                "+vM0p2UEKrK0X7OhNVb4Y4DmzoCvgpa2O9odS2mf2nbTSbK0cB" +
                "zOeH9SHs5hi8QNLvhwLKdlTM7gAhDdKjQbmsvH6LKq+Pp9bLHt" +
                "mDNtV7QrltI+te2ik2Rp4TifDgTIGjeY8edpTsuYnMEMEN0qNB" +
                "uay8fosipo7SpoaVujrbGU9qltK50kSwvH4YzXk/JwDlskbnDJ" +
                "h2M5LWNyBpeA6Fah2dBcPkaXVUFrVwFro3dH75ZK1CdHIrHGPW" +
                "yQtQ4EyBq3ecafpzktI1CRpf2aDa2x0h8DND4sth0zaeGVMH5W" +
                "pj45Eok17mGDrHUghFdqE2xRuHv8eZrBMkoL2228ZBNtjz+G0W" +
                "pHs+v3qcW2Y860y+HlWEr71HaZTpKlheN8OhAga9zRW/48zWAZ" +
                "pYXtNl6yobl8PB5ZlQ/bjtnGd/+7edjluxTk90eFR4p9fuqt1v" +
                "7rCj9v7wkrnTPJjygZ3zm3G/fSs5ivHWbLX+mVp6Dfj3/xoP9+" +
                "vPfP3SDvLeFYwZ+7Hvmany2v9RQ+W+x68vPZ9TT593L7e0sw2/" +
                "cb7Wxfv6eb7e1z13hsme1PXxa8P119MP9+F+4o+HO3o8f19GT+" +
                "81R7v/M81T5IKxz3ztN4wfM03nme2N9vXfg+XePp/O4+1TeHd2" +
                "d763Ev49Gi/t6S8/50sdj1VP1huM9P1Q7fpwsnujyzTLSP6pab" +
                "+/PTRG/+fuuqLuF7h0t6ziz4/8ry5gvvh/e5Z5218H5wGpZEg4" +
                "w8SBRNUVJKn2dO+/M0J/NqFiDqbFm7bS4fjwe1oi6JrSvAHBWz" +
                "P0Urit2f/Hz970/hvfCeq5MtvBecIplaorHMEchnf3CKbaRn1+" +
                "2UjKefYAYedEQwjuSSTVrBJxGZFRWxJGvUM6Gzw8VwMZbSPrUt" +
                "0kmytHAcztoh7eEctkjc6BEfjuW0jEBFlvZrNjSXj9FlVT5sO+" +
                "ZMmwvV/yqSnrTyK+RhneNgpWhIGsO7eucYX8cARcb5a5M1uVY+" +
                "ZPVyVB0/W3N+C2W325+aN/PcnwZ6wuvn/e6FnPenhXCBe9ZZ4x" +
                "42yFoHAmSN68exnJYRqMjSfs2GVl7ljwFatu5WWWxba6bNh/Ox" +
                "lPapbZ7OGOEpsrElnG/+Bhl5GoHl489pXIlzfAx5mtMyAhVs2g" +
                "+fbkn1vhigyboktq7Ajc+yzrRmem07T5uVeab0vxzMq/lt9QOz" +
                "TIfT3LNOWvkJsrElnG7+ARl5GoHleD0pXIkTr6dWnvZYRmlhu4" +
                "23PmpJ9W6dzd+B5s4A50oGNz7Lavv/7GGX/3QPzw5rxTT/7IVX" +
                "85dHesPuNe5h/Z59OejtfueP81yNqXCKe9ZZ4x42yFoHAmSN68" +
                "exHssoLWy38dZHrbytM19rPre5+bpWHd+a5zWlh+IoP5NvXOk/" +
                "mDFTSA==");
            
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
            final int compressedBytes = 2078;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uIHFUUnRgRgiKjWZlFIMRsdBUQ/+JUVxWukuyzcWtw48" +
                "Z9Oq3TThtBk6CBkIif4A+TKEGQMX6iMUgQEYyIm2CCBGdAyEaN" +
                "xE9V3bp97r3vdqeru7rM+Ir3+n7Pue/Nm9dviiTZkGyYES0pdL" +
                "JhhM36kg3aDgyvcbzMkxnA4iesTdYUWrktLNk6tb8ftyzxw3r7" +
                "1WxKNmVSMRa2TfSQLC0c5+lAgKxxfRzLaRmBiizt12zoUezHAI" +
                "0axUlsW2uprUnWqBUs9Lynj5GHdY6DlaK1HRjuflrD+DqGNWDx" +
                "E9Ymawqt3GT1clbDWuhHdvxp/OnMDI15yyXWeIQNstaBAFnjLl" +
                "z28zSDZZQWttt4yYbeXeXHAI2bxbZzLrUT8YlMKsbCdoIekqWF" +
                "4/B0ntAezmGLxI2ueDiaExiehe02XrKhh3w8H1mVh23nXGrn4/" +
                "OZVIyF7Tw9JEsLx3k6ECBr3Oiyn6c5LSNQkaX9mg095GN0WZWH" +
                "bedcahfiC5lUjIXtAj0kSwvHeToQIGvc6A8/T3NaRqAiS/s1G3" +
                "rIx+iyKg/bztnGU+s+PnONtWipuazBbe7S3CWMJJG28Fc+wqK9" +
                "VqdsjckSrBrHehgDkRyjs7RXsqEvXLF8qFBy25mEcyYtuiu6K1" +
                "v9ciSJtIV/8hEW7bU6ZZc/S4UmrRrHehgDkRyjs7RXsqF3j1s+" +
                "VCi57UzCOXNE+xTFtz8oxvXtj8sbSH/ntk/T59P79V5si2+N9j" +
                "fF+F3Wvw13bfvkJHu+/eFQ7xnPmrq/d+1P+tLX5ZxeHLmKQev0" +
                "kF2nIPNaXqeHRlunClUMWqcHVvQ6PTDaOs0/Ol5Nu34dvnNHbb" +
                "3r/ttvyXRpuvhYp971Myu49VbXfDvZHG3GSBJp6Vw+wqK9Vqds" +
                "jckSrBrHehgDkRyjs7RXsqHn1YfVSrRwBTSm1uLleDm7cRZjcf" +
                "dcpidjephsbOE4TwcCZI3r41hOywhUZGm/ZkPPq/digCbrkti2" +
                "1lJbirPfZBoL2xI9GdP9ZGMLx3k6ECBrXB/HclpGoCJL+zUbel" +
                "69FwM0WZfEtrXa+PJ8urv/F8yuWs+LG5o9n7prp4vffpml1s5a" +
                "655vdp3qrb78C+fndDFdzMe8Z3t2MbfN/Uy+8nu2jGB/PpKPoh" +
                "GLDMbNLYQcMuc+xkM1OobYNaqsg3MwB8ZFlEXD/DhKVkEMXsXh" +
                "fko+s775e7TeeaLCfuo2u5/C6ie8j21Pt88d5pEk0nLf3GFYpF" +
                "fmkU7RxU/oMEcwbm6XOOk5ziKfRDWR2zWiZmVesjEjdR2FipiP" +
                "JM5HDcDU2pBz/Llaz/Eb/1/nuFin52vdty82u0518yWzyWzrFx" +
                "qzb4lCIi2XpYXjPB0ILEPKkTQOscIn7ToSqMjSfsyAGamHfIzO" +
                "GbnEbIxNMagImn1f0LmZ3xeIs72G9wXpSw2/L3ipqfcq9a5T75" +
                "Zm18k/nyZZp2zv/c5j6/f2erbleu8m8sk4TweCZyVknzm0hyyd" +
                "HcP80sZMHq6NtNWOt5/i2Tj7nezdKvdT9+D4+ymebXY/+Xzhfu" +
                "oeqPh+ZZ/+lHZps34fpZpnOGYYJWsalEt2OfqVDGP3PNFe/Snt" +
                "0mb9Pko1z3DMMErWNCiX7HL0KxnGbj2tizy2LvbPp4uw6jhPl7" +
                "GhlZDd0+JiaB/M4uvSxkwero0M53ctfN+1vmr2fPL5rv17Qbpv" +
                "/HXqPDnG/Wlf/fcChbS+Qv0V3hek+xv+u2XKfMkLUuqtqwu1db" +
                "qOmircCV2+3m21rdOe8vMgS3Xsp2RPemDymirtpwNT3k97Q6kO" +
                "1PRQHTVVWKdD9a9N541ifEeeT53Xys+3xdl1eSDCq533is9XOu" +
                "863jfbP6Wfu3mHs37E2I4HZ+Zvw6rfWZ4NnbcExjHJ13l9grU5" +
                "2nl/+DmenKzvJ7Fron991vlojD14subfs93Jbh5ZZy3Z3ToKC8" +
                "d5OhAga9zWUT9PczKGZqFctmu/ZkMP+Xg+siq9Al4FYXywn76o" +
                "817QdLt69dVa9zuzTv23vcmpmRXc6q9+0H28d7u9j3d/HP8+Pl" +
                "GF47yn+3O0+/hT5ya9j0f3reT9VH/1Db33vaPh/bR2Ovsp+kF/" +
                "Sru0Wb+PUs0zHDOMkjUNyiW7HP1KhrEPq6vK38FN3Z/GaQut0e" +
                "J6m0a83/fSHo+s50+8Md6Y9np3wpLbICMPUrwx/6QoKeVN5so8" +
                "yZlLUmdrZ4fM0n7MQPeQj9FRq10BrwJE+PspXhcH7wriq7w9ID" +
                "+iZHy8rtn9NFqtFf5efDZ9lkfWWeMRNshaBwJkjdua8fM0p2UE" +
                "KrK0X7Ohh3yMLqvysO2cbfw0z6fsTPyt4XtB7XzN3Auiv8evcP" +
                "7B6vcCn6++977zj8z8L1r3yohxf425u/r/ni46s6Lv4yNW/8yY" +
                "/29ikvMpvWU83zTaqHyjxiX3JvfyyDprPMIGWetAgKxxfRzLaR" +
                "mBiizt12zo0TE/BmjlvjtmsW2tpXY2OZtJxVjYztJDsrRwnKcD" +
                "AbLG9XEsp2UEKrK0X7OhR4t+DNDKdVq02LZWHT/te0Hj59OReu" +
                "OCdeq/p4u+XNHrNOXqsU6tZ6rmVs+Y4jqtrjcu2ZJs4ZF10qLv" +
                "ycYWjvN0IEDWuD6O5bSMQEWW9ms29Lx6LwZo4Qp4FfS1bcm2TC" +
                "rGwraNHpKlheM8HQiQNa6PYzktI1CRpf2aDT1a5ccArdxPqyy2" +
                "rbXUtiZbM6kYC9tWekiWFo7zdCBA1rhzV/w8zWkZgYos7dds6C" +
                "Efo8uqPGw75zLiXwfwc6g=");
            
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
            final int compressedBytes = 1593;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdGjtoHEd0S39IFTBEBENSqTEyqUO4nbuzE4coRQIOGJRUAY" +
                "M/calO95G12so5UDDYYGxFF1nKRzEh4AhLYAgpQgiGNG4UpYqQ" +
                "VKkwyVXZuad3b2Zn9m529qPdzLFz7715v3n35nvrOPU9Z1C8V+" +
                "t7HAca1UQLt5G0qEeEofgV5BfliV/FiVfPhfDMmGot6Mk7srdi" +
                "D8yL2suB/tdFjG05KZUkmmxk9TL2XrQn20zC33YyKO13/XMJpK" +
                "vxZbx/U+5BEKf2uXZFHyd/LJauWnSc2PdZxal9Xps5WnvNe4p0" +
                "PY04xRzNL0XHKbotmziZ2jPmO1Y/RjVAgAFOFLE1jAPErpAOES" +
                "JLqpzYolokT0hKbpet0cOu6HlImxoB2V7YeuNn4G/82K9PNzbC" +
                "sWz8oo9x44kA/x74dqPxRwA903A+TTI3NB4Pbf1VO+5uaHk3B9" +
                "Bv9v54U8EIXrGXr3SdwpSsfHG35G+RLtLcLRMt8VrcLRP/iEv0" +
                "KUoW6GKt92SYdbmFxp33yfBxN3t7+LirPC/OuKs8Nxt3s18Ye2" +
                "E8P42KE7teoPnpulmcbn5s51PzowRzwtcFmp9S9yUqnzwWe727" +
                "lk0+3XzTIp+upb/etb7q16sQp0Pa4uG3sP41Is8CrQetYP9bWW" +
                "3db32jaV1u/BUhtxQ834ZoPyi9ezHM+5nDM0ProaBjrbIqYF/a" +
                "/0at71qPtHuDwe6WLQTnlvOxzp4Ldm0jfb1scQ5eyHZce4NzC+" +
                "vE9q1j15ZFycqe25G/RbpIczsmWuK1uB0T/4hL9ClKFuhirfdk" +
                "mHW5JWoeZ9Ox5/HpAu0Lps3m8fafhuNsMqTplv29ytBxsJbzuF" +
                "szu1ex/u1OI1R7Y+T8+plT2OK/n61+ihN7yylxSd97nJ/CcbLW" +
                "90z7+36Y4J7u76Gt+zrq3MuG89Ne3Dglv38K6sLM4+6UWZz0fH" +
                "HGncH+L8b8xDasR9BGflI2+eTPlTmfZi+muy9Q9gmfZpRPm9b5" +
                "tNm8aiOVVz65J7LKJ9/LYX46kW0+sSkVSmGVnspbOk3vtavsSj" +
                "bjrnox3/1T1vbkc0uKcXov5zhlbM+7mlGcJnOOU8b23CdHI3tU" +
                "/bD1md708YJsYcvS3Lg8Yu5clrlE/lGyo/VGlZkxcxnVN1O/au" +
                "O1cawRR6w2zrpE4RjBJEcQcAOXCPX96erlZJtoV7ZCGmVp0ffw" +
                "o9rD/pCv5JeoW/aAYhS6oZHGNVuMxjS/1qLMJfKPkh2tNw0Zun" +
                "/Cdlu/kt8XFGR+eiVdviHnO7/M5zt/3mw/nvzcUu57Fa+ZV5zc" +
                "46W+fzqedpxC+7NHKlTsovfTPWU4P51KnE9nSp1PZ7LNJ1rv3N" +
                "dKvd6l7n0+8zhbt949rVv9z7lulk/ercTj7mypx91ZsziZ39P9" +
                "P/cF87tmcfI/N1wtLlUvYY04YlgTjWAZJw0Ey3rrK3o52ULYok" +
                "hBepg/3AbPKHu6CKCs3GfkyCef3KWcx91SOffjbjfnOHXLOT/5" +
                "D/KNk9n/5u0X8fMprfuCiPcLEuRTlu8XeLePIp8ibTz1V4uVT0" +
                "XdZ1Y/yDdOenuafLpfrHxizXzjxJpFzqfoONXv5hsnvb3ij7u8" +
                "9+N1w/dY9XwRJ80DrNkBrnfsgOPzHrSJfDqcNOiooFlvWaWrVu" +
                "T37HWakIaWdHp5ad4jzrC3tvnkPy5zPs318tpnslLf+7LU730j" +
                "8+mnUt8XXEg7n4LY97BmvcH81COqzKfDRV6VCpr1llV6tBU9Lt" +
                "LQkk5vmFPtXxHWu9levvnkb6abT9Vmtcl2oQ6i24cA43Cw3g0o" +
                "yKfDSQPCBHFNomZoIyuEQZtqpXVZlJLbqQdoER7VHq+D9W4XJV" +
                "j/Jo+8xW+MClIOsZ3qDvsH6kCqDwHGYaAEnDvExz9AEbk5hdeo" +
                "A3DUJOuhb2gTtYo+IA9wqF5wGnmCFuGRuUTtKBH2lnjIIwHbrm" +
                "6zfagDqT4EGIfbd3gdcG4TH/8ABXEuxymsf3IHKuCoSdRM+jgV" +
                "JIkucwLuPxSlZE3kCVqER7XH6yCf9lEi7C15QB4RVpuoTTgO1L" +
                "xwiH+qJ6snaxPzHlGQT4eTBoJlvVybTk60CTKqFdKoeiFbo0e1" +
                "x2t4X4Us63TLHgw4/gMb0YaY");
            
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
            final int compressedBytes = 2294;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW0uIHFUUbQwulChmBlFxMhEZYgRBkCCCEemqblBwI4gLEx" +
                "F3iS6SlZAoyEzPmP6AuAqCYMgqi8T/J4gmYohEdOI3yMgssnE3" +
                "WQQkSpQW69WtW+fe+153+lNdZqp4r+73nFtvql6/qu6pVPQ2/w" +
                "ZLzY8qhW0Lr1ZK3hYOTBZ//jE6xhvjjZXK4ltF4Tq0Ubf2O0Xx" +
                "LRweY2S+zo6fpv3s/Ckv4myPzJNC/iHtzyftp0Dk6fbnY1T4WV" +
                "/vdyHrwelg7Je5dO7aHKdqt9xxCvOtg3G6XPI4XS52nKovVV9C" +
                "TxJppMOiva5v7POjJSZLsGoc6/EZnR6dkFXKKhDNbGjRCcuHCi" +
                "V3+71emJah1/VUXR7nelp8RV9PUlvcX8L1tDzY9bR044ifS//6" +
                "Uq+tsW9w3Nbt5a4Lal8VvhIoZX6qbSh3fgrzjT4/xVfiK76e9D" +
                "PJfqXVdDK3eIYikOWOnJ/7Z3LvjMCdkfF0BDPwoCOiscfWipqs" +
                "FXwSkVmxfpK1oYqwJa2kGyefoNSntm62zyV7V1mcrSt3+DJpLj" +
                "3O5ZY5gTsXzpMMTjKMXVjYrv3wmTYXjsnx5/KxMNi6glxbjVcT" +
                "Ke1T22q2zyb7qluP5xZnW5U7fJk0mx5nc8uswJ0N50lOJ6kaMm" +
                "v7lMzSfq7da7OhGLqeuFawS2xdASLMyv7B/PnuzLDzSPWFyczJ" +
                "7R+Hzym6lvhYfIx71kmr/kI2tnBcSAcCy4uvaFyZu7gfedpjGa" +
                "WF7Tbe+qi56kN1As0fAc7V52zjs6yj+V/kbC9PjxFXfr1+0tuw" +
                "66fBeDV/9exk1x3Nb3Mp+bxtv18UbvRRpdRt0nzNfB3bvOoKY5" +
                "h1Ztlb++eC56dT8SnuWSctOkQ2tnBcSCcpOuSOwJS4Di2Upxks" +
                "o7Sw3cZLNjSfj+xA80cAMYF6fo9/j96mPkFPJdKcTD1skLUOBM" +
                "iQNLLOoxzanaQjUQl5/SrI6mzM7ddNMeRnPpKYjbGpAlQEbf4D" +
                "s7JX7w6vPj/NfzjgfBGImz8+5PPLu0PMTx9OeH7Kn86iod8wRw" +
                "eunflp0rUsPTwZ3PiTcsdpUL72ryNeT0PkDfN5F50c+co4WV7W" +
                "EOO0Mplxip8p+XoqmK/6QPUB9CSRRjos2uv6xj4/WmKylNb9hI" +
                "8j45EjGTlGZ2mvZEPz+VCh5NbYEtMy9HpP1/7TvqdbenP093Rj" +
                "vUkc4T1de8DvEZYOjfs+s/pske8zyx4nVN9/nF57bsD7+GJ8kX" +
                "vWWYsvRkdhcRpk5EGiaIqSUjqvHg3naU7m1SxA1Nmydtt8Pj4f" +
                "1Iq6JLauAGNk1gW78yv3r8o63tqXi8Wr76zvRE8SaaTDor1W52" +
                "iJyRKsGsd6fEaO0VnaK9nQqs9bPlQouSkuhGm0XfVdiZT1JJFG" +
                "Oizaa3WO5gygSavGsR6fkWN0lvZKNrTmecuHCiV3++9emFqrba" +
                "ptqlSoT7/P2UR7MtL3kY0tHBfSgcDy0tMaV+YuPYU8zWkZgQo2" +
                "7YdPN1d9KAZosi6Jbc8505Zry4mU9qltmfaE6S6ysYXjQjoQWE" +
                "7GSeHK3GSc8jzNaRmBCjbth083V30oBmiyLoltzznTdteSmZv6" +
                "1LabdpKlheNCOhAga9wwjuW0jEBFlvZrNrTqI+EYoGXz0yMW29" +
                "aaaY1aI5HSPrU1aK9UOjeRjS0cF9KBAFnjhnEsp2UEKrK0X7Oh" +
                "Hfw4HAM0WZfEtrWSFt0R3ZGsIrKeJNI6N7seFu21OmVnaxKFJq" +
                "0ax3oYA5Eco7O0V7KhtTdYPlQoue2Z+Ods47N1av79X726ntdP" +
                "RVcfPRo9ip4k0uo7XA+L9lqdsjUmS7BqHOthDERyjM7SXsmG5q" +
                "r3q5Vo/ghoTMsw+O9VWtetn+e7+suDPd+F44Z5Du5s8cbp+vUz" +
                "Tgf/HmycBr7vNkYb4w3ck0Sak1tNWKRX5pHkLHH6myOyks5IbJ" +
                "XZ7kg+adeRpDf2yCyNhEqYkZrP5/qFw8xHzLJai6m1Pu9Vdlwr" +
                "71Ve2zHCe5UdxV5PtQu1C9Ef1Ccjl0qkOVlaOC6kA4FlSA5J4x" +
                "ArfNKuI4GKLO3HGTAjNZ+P0TnDSczG2BSDiqD1XhcU+3687K0z" +
                "VSxe6wbz/unr8sZp6cwEx2m64PXY/fX7o3+4J4k0J7easEivzC" +
                "PJWVyfXMn/cATjOrvEoWx3JJ+060jSG3tklkZCJcxIzedz/cJh" +
                "5iNmWa3F1Foau8Z9tNbK/n8mWoM1/2xcM5+Va1qyGawTcvDTds" +
                "2392YJ69LGTCFcG+mfX99PC/P7gtYtuTQ1yH03P8b3+K2bh1xH" +
                "DfH7gs5tE/guvst91OX5Keo6ffGtqGvjQjoQQlZCDs1P5OuFyn" +
                "rn1n5+aWOmEK7bkvuuK88vjBacn7bXt6Mnye3xVDxV3+5+P84W" +
                "6ZV5JFG2xmQpXc9N+TgyHjmSkWN0lvZKNjSfz/X0e1/Jbc/EVp" +
                "Bre+t7EynrSXJ7PB1P1/e630WzRXqRwTplZ8wKLRunaR9HxiNH" +
                "MpLeuV1naa9kQ/P5XJ+Nk+C2Z2IrQESv9Xj0ZJHrcaCV83wX5v" +
                "PX44sXRpxb1Rup6Hhhc+DxcnPDOeNUoa+n1p358925df29+bli" +
                "n+/6PAefL3KcWltKHqfzRd93PcfpeztOnXsGH6f//f8Uv5/s/M" +
                "TPwfHWeGtyFzalz1n6beRHlIy/Wm7f9eye4XPCfOL/yrYWdt99" +
                "s67np28Kfv/0eu117ll3e7w53kw2tjgbZORBije7I0VJKf37bQ" +
                "7nSQYnaUaOkVnaD59uPh+jo1Y7AqEKEGHWBXdO4i3Hwoude8e4" +
                "774YPqf5eNHnUM7/B3ceKvl7hOli5/H6pfol7ll3e7wt3la/lD" +
                "63XIINMvIgxdvckaKklN5328J5ktNJUmdr526Zpf04A918Ptdn" +
                "zy1ZrXYEQhUgot973+i3ou67cZBGyQ3njF5FdDo6zT3rrCXHFV" +
                "g4LqQDAbLBXQnnac6cV7OsyCzt12yirYRiohWcsz8CoQpkfHSE" +
                "++gIX0/REdLJxzYzxke0ZHuWIPt5FjPEoi1+BmzMFMLVWCyF4w" +
                "Lbf4ou8yk=");
            
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
            final int compressedBytes = 2553;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW01sXFcVHtosWqGgCKfqrl5QKSWVKsEGFl30/QyNWsGqPw" +
                "iVRoUQqXUXZkNVtSgz9ngmk2egAiSQIuRuWGTTDSgSCwggQRAI" +
                "pYkNicBO3XpTVS0LVC+IIsO777wz3znn3vc8Mx4P8Xu6Z87v95" +
                "13c9+8n4wbDdqi6/qzIfzSZ+N+9uiRekw/S/ZUVUt+KcOd1LHL" +
                "SHO5ucySbbaay/HP4XEWdNRBo2zKkprbZK1m0SjSZi8QdbXs3Q" +
                "6fj48HvaIvia07wBzprf1AY1+27LHGVLfezGTxup9v/6GcoYs0" +
                "T+3flKvunsHcXS5zP2fm9NdCv1LItXxc9Vnav99Lj+1f1Ub/Ej" +
                "xn7gnmXhpofx2th/4XK+fpYTtP/S8coHl6eLLz5CGtDJg+6a09" +
                "M0+L8407dvO739vWbDVbkKSRFR13Ep5mK3sSUUjSqBqYS88gC1" +
                "7al55ClYwwBjI5R7LJuIzp4br3u5Vo/gxoTG2lb6ZvNhok3eY0" +
                "sqJZ8rGH80I2EKBr3DCO5bSMQEWVjms2DNd9KAdosi+JbXvljK" +
                "rvJ3EuXq44R6f0/dR9dIzvp2i476dwXrC2Yp6yLx/keerdmuz3" +
                "ePx0/DQkaWQ1X3ISHh21NlUDM/sasuDVODbCGMjkHF2lo7J3DN" +
                "e9361E82dAY1qGRiM5xDI51H2cqpND8PImdW3LXN9LyKF/JWYJ" +
                "ow5nSx8zhXBtpn98u66p2yzj2zxP8W14dV7Ilrm+l5DDzL6/mi" +
                "VsSx8zhXBtpn98tfcFrzRfgSSNLLLh0VFrc7bEZK34tnvex5H5" +
                "qJGMnKOrdFSyYfRmLB86lNz2SPxjJiu9ll7Lr3yFLK6B12gnXX" +
                "o4L2QDAbrGzV4I12lOywhUVOm4ZsPozYRzgDa4LzDY9pg5Yzr3" +
                "BfGfpvvcEubzr3edd4brIV1L11iyzVa6Fr8FD+eFbCBA17jxW+" +
                "E6zckYmoVq2a/jmg3D5+PjkV3pGQh1wFaynWyr60Fhkw8SPtad" +
                "XJx3n9IvMYJXm23Gl5WoAJbN0Liyp1AH+Xn+DX0M8giqNj+O6v" +
                "jB+MF89ktJGllkw6OjTi7O+9lcATTp1Tg24jNyjq7SUcmGkX3T" +
                "8qFDya2xJaa20rl0Ll9ZhSzW2BztOdMp8rGH80I2EKBr3DCO5b" +
                "SMQEWVjms2jN6tcA7QZF8S2/ZKVnwiPpHPWClJc3tyLDnmbHic" +
                "jzVUsO305FixSo9xjDV4ZTVHJIpl5BxdpaOydwyfj9HJol4tts" +
                "TUVno6PZ3PWCGLuTtNO+nSw3khGwjQNW5WUac5LSNQUaXjmg0j" +
                "vy8I5gBtsJ4Mtj1mm19eL98YzHM/HxsjPSv2a2Ib498XjFMbrt" +
                "lLF8PfP/XvGv/+Kfp4yu99P570e9/K9+NX7pT3KmPN05Xh5qn/" +
                "1cmvp73MU/bSdOfp7H+Gm6fuyTG/Cz7ja6PX/v+3SffS7DQ7kK" +
                "SR5fR4HR4dtTZVa0zWir7XfRyZjxrJSDbVokpHJRuGz8fHI7vS" +
                "M6AxtRUfiY/kqKUkjSyy4dFRJ/P7cS+bK4BW3he87OPIfNRIRs" +
                "7RVToq2TCyOcuHDiW3xpaY2krWk3yOSRb3g+u0ky49nIc9f75T" +
                "Ea5hj8K9GMKxnJYRqKjScc0mxsVwDtAGz3AG2x4zZzQ/VOfhh8" +
                "4mHyR8NoZqiaMxNbqs16w2L+QNcYTY0qbuVh7BCN9Korqlrtnt" +
                "B1pv576/O711o5D/yMdWfgV9njJa/y6up88V+q3Wfwf/A/91gf" +
                "Jpy5h9u8jfbL3beq/Q3lesd7XvVvbh9n3t+1u/bf1OeltXW6vC" +
                "ujnQPgj8f0v5O4x+cT1rXW79OZfXRr7SfqKQn2ofidM4zc/AUp" +
                "JGVqFfgMdZiELq7PzzwiB2gWPsldUckSgDpFQhX9BVJip6F8Pj" +
                "Ex2mrFlsiWms2Xg210pJGllkw6Oj1uZsrgBauZ6+4+PIfNRIRs" +
                "7RVToq2TB6M5YPHUpueyT+MZPV+Uon7nyp8xhXdU7s4R7jSFWk" +
                "82R1bPetk9RGHx+tlzHz7m3eC0kaWdlrTsIjo9ZmBGBIDUx+nY" +
                "xQjc8iu7RdaDaMc9vhHKD5M6D5tBU/Gz+br6xSkkZW9rqT8Oio" +
                "tam6XMkKTXo1jo0wBjI5R1fpqGTDOLdt+dCh5LZH4h9zaT0SP5" +
                "JrpSSNrOg5J+HRUWtTdcms0KRX49gIYyCTc3SVjko2DNe9361E" +
                "s91aTGM9ET+Ra6Ukjazos07Co6PWpuqSWaFJr8axEcZAJufoKh" +
                "2VbBiue79biWa7tZiWoXwPsaU/WXMSPq0H3mZsVeVVY0Rb9Zjc" +
                "g8b2+w3xSTk6eygSretP6Zc+Gw+jjBapx/SzZE9VteSXMtxJHb" +
                "uM9L9V+fun9kF+Tzfp3z8N5m5Df0q/9EUbw6CMFok2hukPWbKn" +
                "qlrySxnupI5dRyrXU9+up6WfHpz1dO7uyb7PjBaiBUjSyCIbHh" +
                "11cnHez5aYrBWz/j0fR+ajRjJyjq7SUcmGkS1bPnQouTW2xNRW" +
                "upKuNBoki/cRK7STLj2ch31xXke4hj0SN/tBCMdyWkagokrHNR" +
                "tG9v1wDtAGb2AMtj1mm0/bwgv78746e2O678cn/fctxX3URyzj" +
                "j7qX2Ofs/lmKybyQDYSQl5DDzL7fZ1l8sS4ufcwUwi1WwQoybb" +
                "ejbd1Lw+eO8ncb2Y+mu56yH+4v/v78XdnCXPbjg33exZvxJku2" +
                "3Z4cTg7Hm/2z8HBeyAYCdI3r0EJ1kpNqfBbZpe1Cs2H4fE4urA" +
                "DNn4FQBwNrNV7NtUIWvlXaSZcezgvZQICucbOfhOs0p2UEKqp0" +
                "XLNh9GbCOUAbzJPBtsds8/V5l8wms+57XMaS2fq1SXFkyfzdam" +
                "u/B18cvSbMt7Aybj/pjfQGS7bJit4mH3s4L2QDgfXO6xpX1nZe" +
                "RZ3mtIxABZuOI6aH6z6UAzR/BkIdIGP432H0flb/3BLN3UG/V5" +
                "kb7rmld37I+4Ct7mr3aneT56n7t+F/r6Jy8nmKTxUI7wVYru9l" +
                "nrprtdGbwevTqV0wi9+Nd/859vXvpK9N4Kp6ctrVk+y+fj1FD4" +
                "26nkrEYj0tPVO9npaemuR66i8Ez7uHJrueJnPeRWfsPE3uvIvO" +
                "jDVPZ/b3vFt6Z6xj+W59vN/dwzztgr10c5yqsc7/HZbxDv/eN9" +
                "5xdv4cvGPzQjYQQl5CDjP7fp/FPAfvVB0BH0MYt3wO3tHM4bzA" +
                "/dhyshz/i2ReVWhkOV16OC9kA4F1aA5J4xArYhJTZwIVVTouj4" +
                "A+aYT5HDpXOI3ZOMN98qywh9mrnoPTPzYO8Lbf3SflfVdyNDmq" +
                "n1uS88NVVkSP7q2fITo+X8/n4gsrY383HY+PQ5JGFtnw6KiTi/" +
                "N+tsRkrXgO/qWPI/NRIxk5R1fpqGTDyH5h+dCh5NbYEtMw/A8P" +
                "1k5J");
            
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
            final int compressedBytes = 1404;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1rHFcU3Tp9SBEj1BktWJWdKgQ0H+QH2KiSDcIYZNKkcC" +
                "28sll77SZ9CMpfSBFDsPNBIJEdOQSBiySN/0HKtMnM3D177r3v" +
                "rTJa7W527DfDu3s/z7lzeTO7Wkm93uCnXnMMnjRybfCd2Fsnvf" +
                "ExOOpFj8G3Sv+tka+qdRLJ/LF3jmPwzanR45h36ySa+/1E+/Vs" +
                "PWSXs8uUooklNj02Wst7n4bZGhNafTx+EuLofNZoRuTYKhvVbF" +
                "yPv/Z87FBzW2yN6Rmm7qeLnd5PF+e7n4rtYhsSdn3mG/lGsT16" +
                "SE/to846avlG/SpZWqsPXavrNGetaRvee7d1lY3zCuwK+Wp5cA" +
                "j8ulc/gVgHsLKr2dVqZ42laPWZr+frtU1P7YPGCti1nq83E1lH" +
                "DBq9uhoRjeIZkWOrbFT3zhXyAV0s6dVja0zPMO2+Kz/q8n3H7u" +
                "d0390obmT/iKwm12hi1br2IC9mEwE6tRrJ4ggrY9pvM4nKKhvn" +
                "FYBRVsgHdFTUGtiALTnsiFa+l+9Vu7SRzf2xJ2cVfS4+eJAXs4" +
                "lA3eLGcXzEM2oP/D7fx2TV3Z/Gx50S1ttekTHtvit+6fJ9x+7n" +
                "c99Nn5PKaDmn7NbqzCm71W5O918vc05bdxe3nyrsWT4/3W0zp/" +
                "t/t+0iH+ZDSNjN2a/OofHUvqE+GRtr/ea1P/H0FW4/XucYPKPy" +
                "wO/zId3qx3Mm+P1wAsyJsc9lP+0vcD/tz7Sf9pf1fCqOOv0cP1" +
                "rd5/hpc8q3lzunON8i5lRmnf48ns17Tg5pbbJzX/Q6fMy/+6nP" +
                "p+NOP5+Ou/l8WvacpuQu4L5LR9pPaT8tbXelOcXfPy8VlyBhw4" +
                "Kkj7q1iUDd4sZxPKdnJCqrbNyycYV88NuuQmxfO7Y2i81Ka2Tj" +
                "25RTdO1BXswmAnWLG8fxnJ6RqKyyccvGFfLBb7sKsX0tMtJzfJ" +
                "b3u9EPaU6tvo/66uyRdvHzcM9edf6uxnfnzeImJGxYkPRRtzYR" +
                "qFvcOI7n9IxEZZWNWzaukA9+21WI7Wt9vhwHH0Ab/Zw+DZyyT7" +
                "88e6RdfN79+Izl9qf204u0a9Ln8TSnBf3cslashbb4KOnzMdYT" +
                "x2NadMGyObCIhTPeq+0n7CDWp4/Huot7mk4uFBdMpLHFR0mfj7" +
                "GeOB7ToguWzYFFLJxhb7qn0KuzbZ8+Husu7oldz+hVusfaHKM/" +
                "3pDr+L1l3p8zfi74K+2VVvN9nWbQ5vuC9L1KmtNcf49wmO6w6U" +
                "d5WB5CkxXPgESmfg0rNO70PI8Xy/T9hX3A1v3rrDhaeGVAA4er" +
                "fFY+qyXXtAzGkRXLhl/yT8/zeLFM4gDV9xHrX2fF0fyVEQ0ck6" +
                "zr5XVK0cQSmx4b9TayNSY0ei2Oj4SMyLFVNqrZuEI+dqi5/ZWE" +
                "tcho/xx/8MWb9xx/8Pn83+/e5jkV14prkLBhQdJH3dpEoG5x4z" +
                "ie0zMSlVU2btm4Qj74bVchtq8dWzvFTqU1svHtyCm69iAvZhOB" +
                "usWN43hOz0hUVtm4ZeMK+eC3XYXYvnZs7Ra7ldbIxrcrp+jag7" +
                "yYTQTqFjeO4zk9I1FZZeOWjSvkg992FWL7WrHKK+WV6ok+lqKJ" +
                "JTY9NuptZKOCaNprcXwkZESOrbJRzcYV8rFDze2vJKxFxtv9c8" +
                "ty/2/jv+f06J2uzyn9HiF9X5D20yrvp0fv/h/7KXs60//fPW23" +
                "nx5+toA5vZfuu8XedwefdP4bupflS0rRxBKbHh31NhCIoTUyhX" +
                "U6EjISlVU2btm4Qj74bVcWW/N5dvd3h++v3vvd8MPV+z1CmlPL" +
                "nj5OnwCiz6c75R1K0cQSmx4b9TayNSY0ei2Oj4SMyLFVNqrZuE" +
                "I+dqi5/ZWEteOMfwGsf19a");
            
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
            final int compressedBytes = 1346;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vHFUU3SoVivgDSX5AUqSi9mZmfkSqCJr8A0qkxTFSXC" +
                "CaRBApRksNSICERGGQEBEFQsQRSkt+g1Okze7cvT7vnHt3GWfX" +
                "q8R+M3p37uc5d55n3syO5NGIt8nXo7ol261Lty5BmmaW2fBwVG" +
                "3PLjFdg5dxNBIZPYerOFqyYUQ+dFhy65nEWs2v19PqbfJkcfy5" +
                "l1cnv4aMP5dUHhb6P738dzaOkszf1+rwl5XRv06B9NuJ9rcd7/" +
                "33hj1drVdOtrXP2+cu3XbLJXzQ2QYCdMbNcZRTGYGKKo4zG0bk" +
                "cz93FbG11qzmafO0nDezzQcJn8a8HkfocfN81JWsJQf2mKX9cG" +
                "fI5j41nnWXe6x6+Pq09+X5W5/2Hm5+Hb/30Tlcxz+sz7sh83QW" +
                "7wUX+b4bT8YTSNPMMhsejqrt2SWma/AyjkYio+dwFUdLNozIhw" +
                "5Lbj2TWGtWe9AezJ58veyfgQe2m156PC+zgQCdcXMc5VRGoKKK" +
                "48yGEfncz11FbK3V/PqeWdfxTazjgvRFvXLq9fTm11PzqnkVbf" +
                "NBwue6x1APHMVkdK8vs4BX5uW9aU9ZB9w9n9Wqucg98+ruZfdy" +
                "NDI53+aaWy7hg842EKAzbo6jnMoIVFRxnNkwIp/7uauIrbVmtT" +
                "vtzuzJ18v+Gbhju+mlx/MyGwjQGTfHUU5lBCqqOM5sGJHP/dxV" +
                "xNbahTVuxzOtl71vbLvppcfzMhsI0Bk3x1FOZQQqqjjObBiRz/" +
                "3cVcTWWrO64+54dmX1sr/Gjm03vfR4XmYDATrj5jjKqYxARRXH" +
                "mQ0j8rmfu4rYWqv5i5Xrp6i93du2+qzvBXWeNvj+dKe549Jtt1" +
                "zCB51tIEBn3BxHI8pYetyv+Rpb1Xd5znEG8tqFtdvszrRe9r5d" +
                "200vPZ6X2UCAzrg5jkaUsfS4X/NLNozI537uKmJrrWds575rbm" +
                "/3vsv5Nvg7uH5Xqet4fd6d+Tx1R92RS7fdcgkfdLaBAJ1xcxzl" +
                "VEagoorjzIYR+dzPXUVsrdX8uj7V+66uT9uap/ZGe8Ol2265hA" +
                "8620CAzrg5jnIqI1BRxXFmw4h87ueuIrbWar5tn37g2v7HdU0q" +
                "3lufNc+ibT5I+DTm9ThGTEb3jDKnrEbesizthztDNvep8ay73J" +
                "OdTzNdijL9H5bpmfwdp0Mztttf883pI8Pim+5HM7bbX31/qvO0" +
                "ztZea69F23yQ8GkM9cBRTEY3LM5xC1i+571yP7GDrE+NZ93lnr" +
                "6TK+0VivS2+SDh0xjqgaOYjG5YnOMWsHyPvZU9RW+ZzX1qPOsu" +
                "98yrJz/IfffdKe/TH9e4x789Zf73b899uP9JXYvqPG1wniZ1Dg" +
                "bN02d1Dtb9rnKx/x9BrqfP65WzfOum3dQ1G3mGS88sj7GixF2e" +
                "p3hZpvYX+3C77L/MytHimTmac9TfLWuu4w/qHAyap4d1DlasT4" +
                "fd4VxiLMtA3LOybPdb/uo8xcsygeOo2kfWf5mVo+mZAc05Tn7p" +
                "3W3vunTbLZfwQWcbCNAZN8dRTmUEKqo4zmwYkc/93FXE1lrPGP" +
                "7+tP/V+Xt/Gvz96UX7wqXbbrmEDzrbQIDOuDmOciojUFHFcWbD" +
                "iHzu564ittaatf+I523vyfZWxb0/3qEV/Hp3HdI0s8yGh6Nqe3" +
                "aJ6Rq8jKORyOg5XMXRkg0j8qHDklvPJNaapd/p7r9/8obw+Ky/" +
                "092//O58p+tudjchTTPLbHg4qrZnl5iuwcs4GomMnsNVHC3ZMC" +
                "IfOiy59UxireYv/sbv1bfJ9Fpeet8NrL8g993oNRDo5kM=");
            
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
            final int compressedBytes = 1094;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrtuE1EQ3S5I/EEK/oGGPl6v+AhCmS4dEjWWooRHQ02bf0" +
                "BCEUQgIQqEEEkqKPwBtIgGCoJvDmfOzPVmHZyEx9xoxzNnZs6Z" +
                "vVrbu4buRnejaboTW7wSlZiIZn2ManSQzaLK4zNRETXapVmrxi" +
                "PqcUKr7c8k9qJi8rrUT57O7LXJi8atyZumuibPjf9+Zo+Ojw+V" +
                "ylfNb6zJs97s2wWY9n957xaeIvfpLDNda3Ll9ZTX0x90Pe18/P" +
                "eup61pXk/nubZv5h7UVne3u0tbvBKVmIhmfYxqywmPqPL4TFRE" +
                "jXZp1qrxiHqc0Gr7M4m9vj7fd31rvDHegEWMCJYYfY3JQF956z" +
                "xe0yuSlV2aVzUeUQ+4ThW5fS8q8v4p7zPP7z6z+/y3fXLcW71I" +
                "tbVP+mpxi/l8nWWxTD9nrLIzzestuLX1SfrU++ba3s/vtrwfz3" +
                "3Kfcrfn/6m+4Lx7fFtWMSIYInR15gM9JW3zuM1vSJZ2aV5VeMR" +
                "9YDrVJHb95ao3Ww3m6bYn+unhwiWGH2NyUBfees8PuMVLQLc1/" +
                "tc39zU4z7Ffu09iXbanWNvZmfYTvkrvkVQV4vJQF956zw+4xUt" +
                "AtzXWzUeUQ+4ThW5fS8q8vMpP8eXt0/dQXcAixgRLDH6GpOBvv" +
                "LWebymVyQruzSvajyiHnCdKnL7XlTk9ZTvu9P3afi/t/zP+7T1" +
                "NZ9b8rnlMj6f2vV2HRYxIlhi9DUmA33lrfP4jFe0CHBf73N9c9" +
                "tzjjtQ70VFXk/5vsvvu4v+vhtfGV+JccFoifkc+8njOZW9cGkN" +
                "InLhrz6rzhMnqM3p87Xp6kjpzs+ns6z2yeKZYfllz+Mrzne+9k" +
                "v7JcYFoyUGHzn2k8dzKjv6bRX5bF19Nj9TbQKdXs+qby/qyGyS" +
                "o/ZIMrO4YLTEfA79fKVfmeQI/Fpju1k3r8rPo5OxWuf0+dp0dW" +
                "Q2x2F7KJlZXDBaYj6Hfr7Sr0xyCH6tsd2sm1fl59HJWK1z+nxt" +
                "ujpSO592dy7L7ikqu+fy+bQ7tOJi53v4LZ94B+3T99yDvB9f3v" +
                "3Tg2leOUNW/r/o/D1zmevRSu7BoH26mntQ/S3h1vgWLGJEsMTo" +
                "a0wG+spb5/GaXpGs7NK8qvGIesB1qsjte0vUXe+uNw1s8UpUYi" +
                "Ka9TGq0UE2iyqPz0RF1GiXZq0aj6jHCa22P5PYi4r83TfvM5e3" +
                "T6OXo5ewiBHBEqOvMRnoK2+dx2t6RbKyS/OqxiPqAdepIrfvRc" +
                "Xw6+nR6mVcT6O9s1xPo71h19P9x3mfmc/Bl/EcnPuUz8HLW92d" +
                "7g5t8UpUYiKa9TGqLSc8osrjM1ERNdqlWavGI+pxQqvtzyT2lm" +
                "i0Mjp+8oUtXolKTESzPkY1OshmUeXxmaiIGu3SrFXjEfU4odX2" +
                "ZxJ7UZH3mYustam+Wtxia9MhLItl1qZD5mOVnWleb8GtrU/Spy" +
                "6ZHzgs5IY=");
            
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
            final int compressedBytes = 1478;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7FqHVcQfV8QUrvIF9iN/8Ber/ILaWQsgQpLGIPAbWKDlC" +
                "D7E0LA4D6QFIEUUSAQUoQQBFGTRh/gXnVWO3t0Zs7MW68cEdvh" +
                "7rLzZs6cOTN3eHq2nvFqZdedv+PryuEe03xmXz0zr5lZfqZ1tY" +
                "Z7W08y191n7p7dPYNFjAiWGP0YU4F+1K11tKd2pCqrYj5245P7" +
                "AY9TZW2tVb5dzz5ZtWvB1fa0djO/Tq8/2J6eHSfGb/b61TeC/+" +
                "T8P0f71/CcFD1++VcT/jib/f0KSj9fen9MZ/r6+veU8P/Bnt72" +
                "unP8bmqv+1o6y/KZ2/up7antqe2p7el93NPBWdvTm/d0cL60sr" +
                "vf3YdFjAiWGP0YU4F+1K11NKMdPQJc+Zqbm9ufOW+grlV++72l" +
                "/dy1z/H2Of6efY4/7B7CIkYES4x+jKlAP+rWOprRjh4BrnzNzc" +
                "3tz5w3UNdO0Wa3OXijHbFNu833CHhVTAX6UbfW0Yx29Ahw5Wtu" +
                "bm72c3tK9bF2ik668HNisWG0xDSHer7SL969J9CPHF9N3jqWzh" +
                "MnIzvOqflquhoZ5zjvws+oxYbREoOPHOupo5pRHfWeRT3Pq2fT" +
                "maoJ4vTxVDN7Oq+RcZLT7jRkxtgwWmKaQz1f6ReTnEI/cnw1ee" +
                "tYOk+cjOw4p+ar6WqkOs/zz9vfKJdcbU/Lrhd928GiPW20HSy5" +
                "vvy07aD8m/x3MT76+PKd9dmi+u/fvvfRR1ec9dt3t6f+Zn+T1j" +
                "yLLCYSsxqD7TXhEY06mskdwYlVMeu78cn9OKHvrSfJtVN0u789" +
                "eJM1zyKLicSsxmCjgmoejTqayR3BiVUx67vxyf04oe+tJ8m1YL" +
                "Tvn9r3Km1P7Xu69n76kN9P/ZP+Ca15FllMJGY1BttrwiMadTST" +
                "O4ITq2LWd+OT+3FC31tPkmvBaO+nhe+o1zG6iA2jJaY5VnudqB" +
                "nVfX3sqrwKzT2e3ljXTeec4725em5rH8L19MZ/9Bte+/NuwdU9" +
                "6B7AIkYES4x+jKlAP+rWOprRjh4BrnzNzc3tz5w3UNdO0V63N3" +
                "ijHbE9u833CHhVTAX6UbfW0Yx29Ahw5Wtubm72c3tK9bF2ina6" +
                "ncEb7Yjt2G2+R8CrYirQj7q1jma0o0eAK19zc3Ozn9tTqo+1U3" +
                "TYHQ7eaEfs0G7zPQJeFVOBftStdTSjHT0CXPm+G5/cD3icKmtr" +
                "7RTtdruDN9oR27XbfI+AV8VUoB91ax3NaEePAFe+5ubmZj+3p1" +
                "Qfa6dou9sevNGO2Lbd5nsEvCqmAv2oW+toRjt6BLjyNTc3N/u5" +
                "PaX6WDtFB93B4I12xA7sNt8j4FUxFehH3VpHM9rRI8CV77vxyf" +
                "2Ax6myttYqf/pWvP373bJ/l/qi7WDm95iX/Ut49tQMWDD9a67w" +
                "uut5qlcxdb48B2I/v2fVavlkUEMPqXzVv4JnT82ABdO/5gqvu5" +
                "6nehVT58tzIPbze1atlk8GNfSQyuP++MLyWcdgHqx+zf84In+e" +
                "p3oVkzpQ1Tmq+T2rVtOTUQ09Llm3+lu05llkMZGY1RhsrwmPaN" +
                "TRTO4ITqyKWd+NT+7HCX1vPUmuBaN9X9C+V7m+PfX7/T6teRZZ" +
                "TCRmNQbba8IjGnU0kzuCE6ti1nfjk/txQt9bT5JrLbq3cW9jtT" +
                "J7cV14iGCJ0Y8xFehH3VpHe2pHqrIq5mM3Prkf8DhV1tZaMNrP" +
                "3aLvMx91j2ARI4IlRj/GVKAfdWsdzWhHjwBXvubm5vZnzhuoa6" +
                "doq9savNGO2Jbd5nsEvCqmAv2oW+toRjt6BLjyNTc3N/u5PaX6" +
                "WDtF+93wWWV2xPbtNt8j4FUxFehH3VpHM9rRI8CVr7m5udnP7S" +
                "nVx9opOuqOBm+0I3Zkt/keAa+KqUA/6tY6mtGOHgGufN+NT+4H" +
                "PE6VtbUWjPY5vuhz/HH3GBYxIlhi9GNMBfpRt9bRjHb0CHDla2" +
                "5ubn/mvIG6dmL8A4gOgGI=");
            
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
            final int compressedBytes = 859;
            final int uncompressedBytes = 10657;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV0tqG0EQ1Ul8BF+imZPYWDsjIV9AO0NuYfwRZB/IIhExDl" +
                "mEEAI5UKRuPdWrVzWDknjhRc/QNVWvXr2qbkY2U27L7WxWqt1f" +
                "ew8RrGHm+9gUzPe6uY5mtCMjwJXP3WzFfsD9VFFba8FYf2389Y" +
                "dqz9afZ3Ktv83Sa/2J/J/V/t6tXwnzefYf1/rjZPb7Xyhtj96P" +
                "f5lkuBvu4LWVM2DB5GesYN1xnuplTJ0vzoGY52dWrhZ3BjX0GL" +
                "vevZ/165Q386yfwcTvbjts99bWGMPyYGVs4I0/zVO9jGk6UNU5" +
                "svmZlavpzkwNPY6s8+HcbPNa1GJDfFZjsFkTnqFeRzOxIzi+ym" +
                "e5m63Yzybk3rqTWAtG/393Ym0/p35Or3ZO5bpcwyJGBGuY+T42" +
                "BfO9bq6jGe3ICHDla25qbt5zPIG8Foz+PvXfXT+nfk5v+TuYvu" +
                "++9K+Tk87puZ9B/w5+xffppZ9B/zve/9/1c3oj33dX5QoWMSJY" +
                "w8z3sSmY73VzHc1oR0aAK19zU3PznuMJ5LVg9PfppPfpolzAIk" +
                "YEa5j5PjYF871urqMZ7cgIcOVrbmpu3nM8gbz2EK3KaudVW7FV" +
                "u5vPCHhZbArme91cRzPakRHgytfc1NzWj84p1PvaQzQv851Xbc" +
                "Xm7W4+I+BlsSmY73VzHc1oR0aAK19zU3NbPzqnUO9rD9GiLHZe" +
                "tRVbtLv5jICXxaZgvtfNdTSjHRkBrnzNTc1t/eicQr2vPUSX5X" +
                "LnVVuxy3Y3nxHwstgUzPe6uY5mtCMjwJWvuam5rR+dU6j3tcwf" +
                "HodHeG3p1RiwYPIzVrDuOE/1MqbOF+dAzPMzK1eLO4Maekjl/X" +
                "APr62cAQsmP2MF647zVC9j6nxxDsQ8P7NytbgzqKHH8a27KTew" +
                "iBHBGma+j03BfK+b62hGOzICXPmam5qb9xxPIK89RMuy3HnVVm" +
                "zZ7uYzAl4Wm4L5XjfX0Yx2ZAS48jU3Nbf1o3MK9b6W+cNm2MBr" +
                "K7yrm5axPFekv6YN647zVC9j6nxxDsQ8P7NytbgzqKGHVD4MD/" +
                "DayhmwYPIzVrDuOE/1MqbOF+dAzPMzK1eLO4Maekjl0/AEr62c" +
                "AQsmP2MF647zVC9j6nxxDsQ8P7NytbgzqKHH8foDIFPBbw==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 0, 18, 3, 0, 19, 0, 20, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 21, 0, 0, 0, 4, 0, 0, 22, 5, 0, 23, 24, 0, 25, 0, 26, 0, 0, 1, 0, 27, 0, 6, 28, 2, 0, 29, 0, 0, 0, 30, 31, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 0, 0, 10, 32, 33, 0, 0, 0, 0, 0, 0, 0, 34, 0, 1, 0, 11, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 35, 0, 0, 0, 0, 3, 17, 3, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 37, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 38, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 39, 0, 20, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 41, 4, 0, 42, 0, 0, 0, 43, 0, 0, 0, 44, 45, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 8, 0, 0, 46, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 47, 10, 0, 0, 0, 0, 0, 21, 22, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 25, 26, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 30, 0, 0, 0, 4, 0, 0, 31, 0, 1, 33, 0, 2, 0, 0, 0, 5, 0, 4, 0, 35, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0, 38, 0, 1, 0, 39, 0, 0, 6, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 9, 42, 43, 0, 0, 44, 0, 5, 6, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 1, 0, 0, 0, 0, 0, 0, 0, 47, 2, 0, 0, 3, 0, 7, 48, 49, 0, 0, 0, 0, 1, 7, 8, 0, 0, 50, 0, 8, 0, 0, 0, 51, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 53, 54, 0, 55, 0, 56, 57, 58, 59, 0, 0, 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 62, 10, 0, 0, 0, 0, 11, 0, 0, 63, 0, 0, 0, 64, 12, 13, 0, 0, 0, 65, 66, 0, 0, 0, 4, 0, 0, 5, 0, 0, 50, 67, 1, 0, 0, 0, 14, 68, 0, 0, 0, 15, 0, 1, 0, 51, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 0, 0, 12, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 69, 70, 12, 0, 53, 71, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 72, 73, 0, 74, 75, 76, 0, 77, 1, 0, 2, 15, 16, 17, 18, 19, 20, 21, 78, 22, 54, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 34, 37, 0, 38, 39, 79, 40, 41, 42, 43, 80, 44, 45, 46, 47, 48, 49, 0, 0, 0, 81, 0, 0, 0, 0, 1, 0, 5, 0, 0, 0, 0, 0, 0, 82, 83, 9, 0, 0, 2, 0, 84, 0, 0, 85, 1, 0, 86, 3, 0, 0, 0, 0, 0, 87, 0, 0, 0, 0, 0, 0, 88, 0, 89, 0, 0, 0, 0, 0, 0, 0, 2, 0, 90, 91, 0, 3, 0, 4, 0, 0, 92, 1, 93, 0, 0, 0, 94, 95, 96, 0, 50, 97, 98, 99, 100, 0, 101, 55, 102, 1, 103, 0, 56, 104, 105, 106, 57, 51, 2, 52, 0, 0, 107, 0, 0, 0, 0, 108, 0, 109, 0, 110, 111, 53, 0, 0, 10, 0, 112, 4, 1, 0, 0, 0, 0, 1, 113, 114, 0, 0, 3, 1, 0, 2, 115, 0, 6, 116, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117, 118, 119, 0, 120, 0, 54, 3, 58, 0, 121, 7, 0, 0, 122, 123, 0, 0, 0, 0, 0, 5, 0, 1, 0, 2, 0, 0, 124, 0, 55, 125, 126, 127, 128, 59, 129, 0, 130, 131, 132, 133, 134, 135, 136, 56, 137, 0, 138, 139, 140, 141, 0, 0, 5, 0, 0, 0, 0, 0, 0, 57, 0, 142, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 6, 143, 0, 144, 58, 0, 59, 0, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 145, 146, 0, 0, 147, 148, 7, 0, 0, 0, 149, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 150, 1, 151, 0, 152, 0, 0, 4, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 11, 0, 0, 12, 0, 13, 0, 0, 153, 8, 0, 154, 155, 0, 14, 0, 0, 0, 15, 0, 156, 0, 0, 62, 0, 2, 0, 0, 0, 8, 0, 0, 6, 0, 0, 0, 0, 157, 158, 2, 0, 1, 0, 1, 0, 3, 159, 160, 0, 0, 0, 0, 0, 7, 0, 0, 0, 60, 0, 0, 0, 0, 0, 61, 0, 0, 161, 0, 0, 0, 9, 0, 0, 162, 163, 164, 0, 10, 0, 165, 0, 11, 16, 0, 0, 2, 0, 166, 0, 4, 2, 167, 0, 0, 17, 168, 0, 0, 0, 18, 12, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 0, 169, 2, 0, 3, 0, 0, 0, 13, 0, 170, 0, 0, 0, 0, 0, 171, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 172, 0, 173, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 174, 0, 2, 175, 176, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 177, 0, 178, 179, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 181, 22, 18, 0, 0, 0, 0, 0, 0, 182, 0, 0, 1, 0, 0, 19, 183, 0, 3, 0, 7, 10, 0, 1, 0, 0, 0, 1, 0, 184, 23, 0, 0, 0, 0, 24, 0, 0, 20, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 16, 0, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 25, 0, 0, 64, 0, 0, 188, 0, 0, 189, 190, 21, 0, 0, 191, 0, 192, 0, 0, 22, 0, 0, 0, 62, 0, 26, 0, 193, 0, 0, 0, 0, 0, 0, 194, 0, 23, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 195, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 24, 25, 26, 0, 27, 0, 28, 29, 30, 31, 32, 0, 196, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 197, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 1, 198, 199, 0, 1, 26, 0, 0, 0, 0, 3, 0, 0, 1, 200, 201, 13, 0, 0, 0, 0, 0, 0, 0, 0, 202, 67, 0, 0, 203, 0, 0, 204, 205, 0, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 207, 0, 0, 0, 208, 68, 0, 209, 0, 3, 0, 0, 0, 69, 0, 0, 65, 0, 0, 27, 28, 0, 0, 3, 0, 0, 29, 0, 0, 210, 0, 211, 0, 0, 66, 212, 0, 28, 213, 0, 214, 215, 0, 30, 29, 0, 216, 217, 0, 31, 218, 0, 0, 219, 220, 221, 222, 30, 223, 32, 224, 225, 226, 33, 227, 0, 228, 229, 6, 230, 231, 31, 0, 232, 233, 0, 0, 0, 0, 0, 70, 0, 2, 234, 0, 0, 235, 0, 236, 0, 34, 0, 0, 0, 237, 0, 238, 35, 0, 0, 36, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 239, 0, 240, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 0, 241, 0, 0, 0, 242, 243, 0, 0, 0, 244, 0, 0, 245, 1, 0, 0, 0, 5, 2, 0, 0, 0, 0, 0, 0, 37, 246, 0, 40, 0, 247, 0, 38, 248, 249, 39, 250, 0, 251, 0, 0, 0, 0, 0, 252, 40, 253, 41, 0, 0, 254, 0, 255, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 257, 0, 0, 258, 0, 7, 0, 0, 42, 0, 0, 259, 260, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 261, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 262, 263, 264, 0, 0, 0, 0, 0, 0, 0, 265, 0, 0, 0, 0, 0, 8, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 0, 0, 2, 0, 267, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 1, 0, 0, 2, 0, 268, 44, 0, 0, 0, 269, 0, 0, 0, 0, 270, 44, 11, 0, 0, 12, 0, 13, 5, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 271, 0, 0, 0, 272, 0, 0, 0, 0, 273, 0, 0, 0, 45, 0, 0, 0, 46, 0, 274, 0, 0, 0, 47, 0, 0, 0, 0, 275, 276, 277, 0, 48, 278, 0, 279, 49, 50, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 51, 283, 8, 0, 284, 52, 285, 0, 0, 53, 0, 4, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 0, 54, 289, 290, 0, 0, 55, 0, 0, 0, 56, 0, 24, 0, 0, 25, 5, 291, 6, 292, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 293, 294, 3, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 295, 0, 296, 0, 0, 0, 0, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 297, 0, 0, 0, 0, 0, 0, 298, 0, 0, 0, 7, 299, 0, 0, 0, 58, 0, 300, 0, 0, 301, 0, 0, 302, 303, 0, 46, 304, 0, 0, 0, 59, 67, 0, 0, 0, 305, 306, 60, 0, 61, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 307, 0, 8, 308, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 68, 0, 0, 0, 2, 47, 0, 0, 309, 310, 311, 63, 0, 0, 0, 3, 0, 0, 312, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 313, 0, 51, 314, 52, 73, 0, 315, 53, 64, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 316, 0, 66, 0, 0, 317, 67, 68, 0, 54, 0, 318, 69, 319, 0, 70, 55, 320, 321, 71, 72, 0, 56, 0, 322, 323, 0, 73, 57, 324, 0, 58, 0, 0, 0, 74, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 325, 59, 326, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 327, 0, 6, 0, 0, 21, 0, 0, 0, 0, 0, 0, 328, 0, 0, 0, 0, 0, 0, 0, 0, 329, 0, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 61, 330, 331, 0, 0, 62, 332, 0, 63, 333, 0, 64, 334, 65, 0, 0, 75, 0, 0, 335, 336, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 337, 0, 67, 0, 0, 0, 0, 338, 339, 69, 0, 0, 0, 77, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 340, 0, 341, 0, 342, 0, 0, 0, 78, 0, 0, 79, 343, 0, 0, 0, 0, 68, 0, 80, 0, 344, 0, 81, 69, 345, 0, 346, 347, 348, 82, 83, 0, 349, 70, 84, 350, 0, 351, 352, 353, 85, 0, 0, 0, 0, 354, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 355, 1, 0, 4, 0, 5, 0, 0, 6, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 357, 88, 76, 77, 358, 0, 359, 360, 0, 0, 361, 362, 0, 0, 0, 7, 0, 0, 78, 0, 79, 363, 70, 89, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 364, 0, 365, 0, 366, 0, 367, 0, 90, 0, 368, 369, 0, 91, 370, 371, 372, 373, 92, 93, 0, 0, 0, 374, 0, 375, 376, 377, 0, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 378, 379, 0, 380, 0, 381, 382, 0, 0, 0, 0, 97, 98, 0, 0, 0, 383, 0, 0, 71, 72, 7, 0, 0, 0, 0, 0, 99, 100, 101, 384, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 106, 0, 82, 107, 0, 83, 385, 386, 0, 0, 84, 0, 8, 0, 0, 387, 0, 0, 108, 0, 0, 85, 0, 388, 0, 0, 86, 0, 389, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 390, 0, 0, 0, 0, 391, 0, 392, 0, 87, 0, 393, 0, 88, 109, 110, 89, 0, 0, 111, 0, 394, 0, 112, 395, 396, 0, 113, 397, 0, 0, 0, 0, 0, 398, 0, 0, 0, 0, 34, 114, 115, 0, 116, 399, 0, 400, 0, 0, 0, 117, 401, 0, 118, 119, 402, 0, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 1, 0, 0, 2, 3, 4, 0, 2, 5, 0, 0, 2, 5, 6, 7, 0, 1, 0, 0, 2, 0, 8, 0, 5, 0, 9, 0, 6, 9, 1, 10, 0, 11, 0, 12, 1, 1, 0, 9, 9, 0, 13, 14, 11, 3, 11, 0, 15, 2, 2, 16, 17, 8, 2, 11, 18, 15, 16, 3, 0, 19, 20, 21, 0, 6, 22, 23, 1, 24, 25, 0, 26, 0, 8, 5, 27, 9, 1, 28, 1, 15, 29, 30, 3, 4, 0, 0, 31, 32, 8, 3, 33, 34, 4, 1, 0, 0, 6, 10, 35, 36, 16, 37, 38, 0, 39, 40, 6, 41, 0, 42, 0, 0, 43, 44, 7, 5, 45, 5, 46, 47, 48, 7, 9, 0, 2, 49, 50, 51, 19, 6, 8, 0, 52, 2, 53, 54, 15, 8, 55, 56, 0, 57, 0, 18, 0, 58, 59, 60, 5, 61, 23, 62, 1, 63, 3, 64, 2, 65, 66, 67, 2, 68, 2, 19, 69, 70, 71, 72, 73, 0, 3, 74, 10, 0, 0, 75, 0, 76, 77, 8, 10, 0, 1, 78, 3, 0, 79, 0, 80, 0, 81, 0, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 9, 0, 11, 90, 13, 4, 91, 92, 93, 94, 12, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 22, 22, 9, 1, 24, 15, 101, 0, 4, 102, 1, 1, 3, 103, 0, 11, 104, 105, 0, 106, 107, 108, 109, 110, 111, 10, 0, 112, 24, 16, 0, 0, 13, 1, 0, 113, 27, 1, 26, 0, 4, 14, 114, 6, 1, 13, 115, 27, 116, 117, 0, 0, 18, 18, 6, 118, 7, 0, 0, 6, 20, 0, 4, 119, 1, 34, 1, 0, 120, 121, 49, 18, 8, 3, 15, 122, 1, 7, 123, 124, 22, 125, 12, 126, 0, 5, 127, 128, 129, 130, 131, 132, 29, 31, 133, 134, 12, 19, 135, 32, 13, 16, 136, 137, 20, 0, 5, 12, 138, 139, 140, 13, 141, 2, 142, 143, 144, 35, 19, 145, 146, 147, 38, 148, 2, 6, 4, 149, 150, 0, 39, 151, 152, 2, 153, 0, 154, 40, 26, 41, 155, 156, 4, 157, 49, 22, 13, 158, 159, 8, 42, 160, 161, 162, 0, 163, 164, 22, 1, 165, 166, 47, 5, 0, 31, 167, 168, 169, 16, 170, 171, 14, 0, 172, 173, 174, 41, 6, 0, 38, 0, 0, 9, 175, 1, 19, 23, 15, 3, 0, 45, 0, 176, 21, 177, 178, 6, 7, 0, 179, 2, 180, 2, 181, 182, 26, 183, 27, 184, 23, 1, 2, 185, 186, 187, 29, 0, 25, 0, 3, 1, 188, 8, 26, 9, 189, 190, 2, 191, 192, 51, 193, 18, 194, 195, 196, 0, 0, 197, 198, 4, 199, 52, 3, 28, 200, 8, 19, 201, 202, 3, 203, 0, 20, 204, 205, 206, 207, 208, 209, 5, 210, 211, 212, 213, 214, 3 };

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
                "eNrtXEty2zgQbSCIC3E2SEoLeYdMVFOzzgmQTcrLLLNUlXMQeD" +
                "PlOcLs7JskN5mjDERSNEgCJEiBJAj1q4oiWaKIz+uH101QD+QR" +
                "nvZweryh+u67ugH5VR0OH57gHyCf/mP8GSjb7fnj3z8PL+qLug" +
                "f1F3wm73/83v17BAQC0YUw/7jvTXp+otZv6GfQwE1LTfwzqvcC" +
                "TPwT9ac0bz0BHDV790w/gol/faOYhD/gXqon89/7/W+4w/hfHQ" +
                "/9+v0L9TttnOPvxcTfbR1/BwnEhBlIbeaPUl7E368y/uAe4w+B" +
                "QLTAK99RQZ79BwNiXp0/wk6uo3ilhMqo92xweARSBIFAIBDthd" +
                "PK27t/FmXWXi0xX8v18wgx1s+Ht1h/QyQAUtCcW6wvnskqIoRs" +
                "BwlNon5l1z/ebDV+aPXAWKUxpCVLHn+bgX+v64/Crj8eTmx7rX" +
                "8U9cdvVf0RZqg/2oNLQ8uzEkUjJzRmu7d+HzP+3PX3M/+PBf+x" +
                "/o5Yo3iicGCC43et+r2tH7eu9fP+vH7W51dznL/Tf+j0X6V7/S" +
                "IK/7knf2zWXyHT+ismUP76e4dWJRte55+l0IFVrh/0+x/b/9+8" +
                "+n+J/idQwhxyFAhfdoNXUfpGNqR+qGLWDzdCvmQxUH8N3D+TQP" +
                "3AV79zELNRv+PdHlTPbxst5DMnCEPtl572R4QYK5jbjt8o/Buo" +
                "vy60/2x6/CnyCbQ4sY+b44g6HaMYsA8a3gEhqmCB+XpO9OH5sb" +
                "Q8Jrkj5st3WvU3iYC117PLtHjxfz4JY8Y+efibaf1YmanozB80" +
                "509W8/fimL9w/zln/RnrN4gSun4Ar3pYH4jIn1EeTvSqKl3aIL" +
                "INfOOKlYmeRZ8Vc8mMTMr6E1Ra7CsXuTenIgDfWcTT3NIX52jt" +
                "axpQXn8d95O6J63iaQ2omhjXF+fQ7IKTJjIuJLhfpwW96kIrKe" +
                "B+DpFE5z9jtaGN9ovGG6QxXtzJzUuP34ImqKi0SdjYkVUIKH3K" +
                "J3qEkcSNgTSU2NVhoiMl/RS2BtoTQPHyb59+j4lTOcZna0BMTV" +
                "LY4PjJodmjF3DCzxI1TB26vl3QKcb4Zf6BwM6ZYvDuTBArTQpP" +
                "XzaaK64MLq2xpH0k1N0XvE0ICgivuLAs+fPKAj5CxHgaTV9MdN" +
                "l0FqANycINb3WtSgGD+y9d+5c614/w+s26/L+S8+cN//7bTliV" +
                "zjLe/iuG/E+6/fkalUn7Z2e9fw7Xr+TnH/dPX5n+6ivvfxgW3v" +
                "8pY5YKHAq87P7dNV1fSP9zX6Cm7v9ej/+IedHa1BdHbrDml9f6" +
                "59z/Du797zJo/zsN7aXc4AyqhCgj8w8YvNnwEvVf7P6Ti/Qn/P" +
                "4N1J8kadbaP36s948X9yJN3z8+QiIb+z+09UZn/wdL08eECx36" +
                "r8xWeAQCEVH/wan/6lzREDF0FFUYgcgX5LrCnTWLvdKuPSiPSy" +
                "39e5z7R4eLHWjpQvIvOjD+Qou+8U82/paFTmX8RZyUcQtJ03VX" +
                "+mz9BBd/CJT6CbCN+I1j6yeOn2P9Kn9Qfdddv+ZDSozWiU90X/" +
                "7GWvdvqeW6JML517labv0KletCOpMptT93LMZ/4rYucrb2RPXf" +
                "a7pYHuYttG9IF7M0GsMJEZ//HgfUZhuvH1D/ET3kGYZMqxtI1r" +
                "RGTyw0JcLy/zqJ33/Sa4awcKYQ3f5n47/Tm/8rWzwyyB8QQaC+" +
                "v8b9/T4ReDxLKQpWnRY5clnXVa1hqi0IPn55oKggEAj0X+i/ED" +
                "G8jGovq2xkCUGnkygjELODj4s2v37TKfrdf/8SzWXgEBPzb1Wy" +
                "zqO9XC/MkP8BR1UytA==");
            
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
            final int compressedBytes = 1216;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUmW2yAQBaL0I8OCl+dFZ0eGg/CyyjH6KCSrXvYR+iq5WS" +
                "QPbUkGGURBgVR/04ONqyhq+IWQzBgIVPA7LcsDsfSivqNE51VV" +
                "3Z2iEtfxdvK2/gV+/MWc/yEL2GEb4LAfd8fY3X2Zpxc1r3Hx+D" +
                "ABeft/OXG+81u1JySyoqi9ZElh3THiu17o1bBCj1KSNcPk33W9" +
                "6eVhZAgrpyuzQWhkf9De8kDJl1AYgkyQL993S3WdQNhpXHgpUa" +
                "2Rofw8zpFALwRKr5/Ohb/99vM3lpO/OfjjIVT+rzL8NWD+K+w/" +
                "ImcH9l66iJk4fbSeEUl7kT/X3yKsH9UvQsvo1EL8y4zxTxyaeh" +
                "Cw+tNw/JXcPxIUf2CFFoi/tA51y9uXYUl/SPzoFZK9K/1hz52w" +
                "j4o9MM3Nz8HjnnsntZ18FYIdHqV9MJ1m3/tGxzz3Pz49/mNfn+" +
                "b5R8PlX03lNjR/7jl/VFx/umlhyOTQ0luBPt4VmYEAUdgm15LZ" +
                "ypZdv+qRo36asdsGJAketoCErZL3lamO9h+ofyRsDxw6tWzU/3" +
                "cav5unaWFuLq8l0+ZTIP78rEgcX8H8lzrDkFai84ardX2oZIQi" +
                "PY4o2ARVNPWy80+6fo59/oOBXP/vYrmKShwPmPmN6/qXGTQ82v" +
                "91sDIf7M8v9h+SuvXZX5cgcne6ShEpP7X+0Hjc8an1Nw//COcP" +
                "tP40HpP/ti5/7/ZPGA9y/mff8Yff/9L4GvcPqttASoXAYfQbtu" +
                "glY5hT2hBj7/ziuB7K/S0koS1v0en1N/z+k9R8Z1h4qw8Pkz3+" +
                "xvZnLvvzs/2Zk//gel6M/6zUP9/6x+mv7Br9DSWo7F44jh82Xr" +
                "+T9Y+Fa+J/TNK6NLV+OfPfQv4tIp9QN0rWnzX1Pyz/MbT8h8vf" +
                "8OUnr/8K/qSitKjbf6run17C79+3nv7J8fwucfVd19WbTheffr" +
                "792+TzKwDjZSR71/n9L/H8Dtb5IQk0f3mVL+fyS5xELGW/9P17" +
                "bP8B3v+2BfNXzfVH+BaFzu/dz98l6yf6+mHzl1hUFH9aoupb4v" +
                "kX4DydLv0kwMLEr8N/dLD/yKr2r/T0j7xq8YyfbeLlz+Jf3Kzf" +
                "X1/8f86/firCCzfCXzhuGGhGIGD2H2ohfmWB8WzN+CQA9N9F6H" +
                "u+/jl5/yOz/5k1aqiGeGpr/RsBmhauomYqTojBm6TJLn+9JfG/" +
                "vwf7/qPmx6fWL+z7p/Hst4Hrd/jXT9Djt13/qzh/7G3/gZC0de" +
                "OnaZ4OjQNFrjt/n++f//D6lr8n98+/DPm7nqfeipzyuQ1YKUhe" +
                "jiifUKyj001pC73/UGL/w2QJxzzpTCPLB2s3q/OFm72DvX8tDn" +
                "0tEG4OJfuneq9tQlO1Un8NVb3SSpEJYdupz98q1H/k9Eb//tdx" +
                "A+FNf+f+V5Pnt+rKBkn2JxAIhHbzH/DzL1nc9jb29++1zB/c8x" +
                "eLe5mikBqmlPSrKIMkn0DIE8o1br1Yd7MXN6giNKF/3fsVPPz+" +
                "ZT63uzUOzvBt+laHKcww7AmyC4TjDwf455e17r/bjl+f/0uX/+" +
                "sYvWuFjaX5MaC7i1eSgzpqAQGk/wzsf/P3z6rK1unm/Plm6JWN" +
                "sj+s/2iG8P0zafgPJzfowg==");
            
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
            final int compressedBytes = 893;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnUuS2yAQhhEhKaYqCxZeJFVZkFQOQuUkc5TOnCBH8FFjWb" +
                "Ys25KMJAQN/N/C5Xlg6CfdelmIsmgEuKBH33YoqAf6hzv44Riv" +
                "7WQ5075qYS+/sUJe3sr2hc6r/6ROmVEfBPUDSQ/kWmF/+zw/rZ" +
                "4/X/uF0T93+SnDqDUZxV9YKKK2cvD//TU6lP+WG+Ut+s5l6Un+" +
                "u/LAiVrYlj/q0VPY+tMOfE1mn88TlO0ZEDL/qrhh/TRrmvnr7j" +
                "/q2L/riF/4P2Dov7S//64/foGNpsKAdDnIr2B/uCuoI58OdiO5" +
                "8cMk9Jm0HJIe/wMAACESjhvrv1zblZ0z0bGtfpq2/2qu/Vd7Mo" +
                "J2PH6gF61/2/GPyfHmaXwnv+zk/9eONxHMs67hDDV3yvkBWF6a" +
                "Wr9BIb3Z3v+AdjFn/4H8sTeZh/33Per5iwDnT/RkSxajU/NKZF" +
                "Tu/Mlnw/InMevjz07Hn+3jTwsAwM6goAUA7LH/AwBQREB0AAAA" +
                "AICY8L7+HfUTa88p9P6jck4WO2/7yRf2MxHtl+7gEeUqQrXXN6" +
                "TOJLh/jDdUp3dvzT+pj9/XPn+RuRL2B177p5Z95r5d6HOuOnXo" +
                "SkfN3HTgRv+kbPH6r7R+aTyEJU6W6/o36vvvg2///Sdn/5s+/h" +
                "BcfpdD/Gq/1dP1N5aH/VAxsWXu+cOF9W+4pTrjymX++PPHVP7/" +
                "WsTzg0EI5MR7dIULO4I1VUVzl+AtFMl3nwTx9G8WZBr07wCww1" +
                "Q+XvCeX5Vu/639B/wH8c9f/2mfvzBfTJmZeqpZr0Hv8QCAHfnZ" +
                "RyQJSdqIL8I27vclL9lTCjqe3v74Jki254x/XTfjN/NXfH+H/l" +
                "KgEny6S7EtrZpf1eYLoa5fX17tEGIRxI19vpiV8bvt+bWvnv/6" +
                "duzj/278Ls9/1TXbP3l4uNc6k8Vlgwf/l+vOn4e7flPj+wfAtv" +
                "pth+sfLfPCTU2WlyYXK27gID7rMcPI7qPtg/noan+A/g0ADui5" +
                "7Qbr33X/qL7+6dTXvK5xbBO6/olTv3HowkuCuOrJLLJjJgWASz" +
                "w+NjbPZWes/3TXTxRcv3vdPzUlP7GSn7bYj0bX/xHt+1uM4Pz9" +
                "LVHyB7GN35Hxhtv9y67g/LWD/RbV78PxtFJ/B6FGE+yG43/Nsv" +
                "gZyn8rYOTNe5rLQu+2B8fG//zWryfXv0H+/8N3ozQ=");
            
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
            final int compressedBytes = 839;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUty5CAMBYYFU5UFixyAuQk1J5mjqOYEOUKOOuWkx9Xp2G" +
                "5jfhK8t+hy0oUBSUhP/Fqp54gH31ml/PLpVLj9JyhzezTLB32U" +
                "/mGVVu5V0VqQ3OF702BzSj62//Vs+38fvtlfrT9JfhvlfYH266" +
                "9/kqqLLP29ndcfJegPyEW4ezZN7emh8oNhycN/qZL+67H9VLP9" +
                "EuQXBx5j1veVfwv9S9ffMPZnZo1kyfFKdy4PADkYyv70CVrIkT" +
                "MDAJBKsNqSFKviFv+NCyv+aMn70ji98F/9n/8u/ohq82//jX9/" +
                "1m8+639b6veFheEbqBcYHEbXIuUAIIFtA8Cw9AyA/MSBNdVwu+" +
                "undnzNXF1/oP31873yVfI3ZvIz3/r/d1t+il4Y9b+NoRvGY8tk" +
                "FjQnyWmQpTKgcvyrt3+qN3+is2a8Ez+I0f6RevpL2392ev9V4v" +
                "qTLarTFvxDDcIf2MR/t6fr+P2rpUTgMX4GyD925P/E+4bC9SP/" +
                "Bmb2fwXiVxAYvzjE31DNr/FA5G35F/X/kjr/w3j+AxgAoYD36O" +
                "R9zM7zJjxUDbRF8/xtjv3bGMnAJQuhdYwUXb/T58rrO/5/pf61" +
                "POx/6PwZ/J5nDmon7jsAFPZ8WL8qBcdIf7fzHz/fV/192b+0ef" +
                "7DQYXAtFMBcomqHIpiq4nl6vpzGN9o5JqVXP4U9vlTWPmTyx9c" +
                "FDdC96+nphyXYn84uOOL7VeF2t+m/njoggYE7i8Zn0QNlC3MG/" +
                "+Q/5eUX4D8MpID9B/jF/Yny/+XPD9CTO03Mh5/Eu7flDb+iJn8" +
                "8/Ov2u2/wJ8HTn55+I82m1N4+B87rf3AfyD/B+T67xnmD5D/M4" +
                "H0+9vPtz/xRr3i8ad3/QDQA3biH4lgqIt7x9LQYfhO3sp3ETKf" +
                "++dwHgXIJ1l9798Oo5FWACgaP8Ka/7su9auS9SP/BwAAAArFL3" +
                "b3zwLQXyIwfyyEf41hv3Ltp8L+Rc9Pf/RcCifPPxLG/1m4zcdp" +
                "5h8o8/5MKnZ/ZpRpP4nImbxPun8RqIPQ0atSx7isc+pnyp/Avx" +
                "H/Z8cU/Oew28XeQWx7m/v75Yf5l7mUf7E7vzSjBUuxP+n9B4AU" +
                "ej9n+wkmwBX/AIex3cc=");
            
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
            final int compressedBytes = 724;
            final int uncompressedBytes = 38721;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtOwzAQnYQImZ2FugCJhUEcZMRJOIq5SY9K05DSRdo6jT" +
                "8z9ntIVRTh2DOez5tJ0hJViW7dvw/CxfHXVm7HT0Pu74yj/u+w" +
                "Pw7l8fNhOKjE7M4u5A1xhPk3yGNiXEmD/ExAy5C8/wH+Y7f7T9" +
                "P62xx/WJ/+urT5Q5r+7+APTlP8Scc/4D8l+Fc8+83Dv+q1/zrs" +
                "B/yrCfRQgbT4G8f/S88vG11E2tYVHg/ccoQ+gbMCgB6YVQ7DS/" +
                "yXR1Z89KT9yL66kf92M/8dY5hPyH+Nau0HkywPS1XM3wBgQ//A" +
                "Z+0fLESmIe5F+0DG6+RtoaUp3Z2EmDbpmaaTl2SxcUrrft14p8" +
                "Mtgu8faOg/S9Zfnvp/K9Ltf+n159G/bqoo2M/+7c+f7G8Xan9f" +
                "jfBHJiDQox3UALSUv1H/JeevfgX/OS9KDN2qUBoJ7NnENIXHk9" +
                "r5OdzdKo0imB+olH9zwvwJxOCfeu0Xzy+WlT8B/13Vfzkf7xPJ" +
                "z4LtP0/+9Mnkh/3Kt9926ncH/gFEj3/onwGAIv5vCwljCyvTqj" +
                "aFOYGXsUIAAIAJVfXfWLn8K/tndzz/k7r+9qrlT8/f9OZfPP9a" +
                "A/+rO38Uih9m8bBA/gMywUAFOuvPoeTMi/nPXY4/7pT/TLr5Je" +
                "VfLqD/mvgLC5Yf988Rv4X5rzj7U9g/AYCQwjD7eACoNv7ZprfJ" +
                "Ed5f1Af937+hnbmrrn+13/8pvX7k77brH+329z4v5/DXe2PpkV" +
                "zHn+MpM/riQPvD4dsL+Z4GRx/zZj7ZH3r9vsqgzL2GgdsA8P/Q" +
                "8S303xkmuLHN4VE/Y/8BQAXAfwAAUMwfo+bf8vzHJ6s/8P21bS" +
                "fOFp7fVli/VPX7l9Afvr+hCVioQCfw/rTW+Cc0/iL/CKq/WPD+" +
                "aee/eH+ibfkB8I/yrHv772cJyz/N+D/iT/3xmyE/ADRbP7Dy9R" +
                "PWDwBACvwCpYTvbQ==");
            
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
            final int compressedBytes = 3960;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmwe4FNUVx8+5d+o+EBBFUVD6o4lYUARsSBEUKRYEQQWjFB" +
                "UEEakCIkUFQfkSpIMFFBALCIKoISgiGkSsEFGJxtglWIiasDm3" +
                "zM7M7uy+fbu8ByTZ73tT7ty5U37zP+fcc+8DBM6WgQMu6wbloD" +
                "xUZhOgitkHToQ6UMinsWewkvVH1ggawyl8Om5wy0JTVhkegHP4" +
                "Z9CKXWW1g0ugC3sMukI3/IfZxRjBBzocekMv6Av9rTZg4ANQxm" +
                "yBv/FquBCOgqPZnWyO0QG+t2dCNWMZ/MZHQS3W2egBdXEa1Hd6" +
                "ubtxPpzEFsKpcAacxYezrnABm4RLoB1cxLdie+jgHsdnsVvZpX" +
                "wmXMW6wHXse+iDFfE7Ph4KoAI3zH44AyrCkVAJjmHfwvFQHWpA" +
                "TTbOOJ8Phob4JDRi881P4HQ4k53Ejnc/gRZwNi7CL4120BIutJ" +
                "ez8dAZOrE9MAMug8uhO/SEmXA1qwI3QD/zVWBgsjPBAhu/YT3Y" +
                "fohBWWsDHAHH4srYaHYtVMV2cALu5W/ZW6A21IMGRnv8HL9g3V" +
                "lH/AOcDKdBE94Nmlk9oTmcC+dBa2gLbczN0B4u5hOgI+yHS+EK" +
                "uNJaae6GHnCNUw6uZY/D7+B66+p4nNVhdVkh3Gx0Il43xeNGZ+" +
                "tfrJ7zU1z++LR4HCtRrdNYbdr7EjfEFtJeZWwbjztNqPZge388" +
                "8HM+ds4iXqa3b7Wh2qdQ+XZWw6iNC1Wp3dx60Bgqj1vGQP4DrR" +
                "+2ntQt9I4FG4zz4dZqsTa3qH3zZFk6i1rpymdS65IX3WVFKl0T" +
                "T/mxb0OtDaaSxvT3svm5vP5E74g5y69lrJNn7sE2wXOta8XSlb" +
                "fHVujWe6i1K5+GNYmNYXt17WZWA+c8vf173G8VyhoNAu39Gmzd" +
                "3OxtYZ9E2e7EHXWEG+kMwaslcGOi0pdxN+lrAA5lF8BA0tcQKM" +
                "QhvCpWcodZk+B2OMWugSPicaEvvBXOiVWDQdYatzXpa7h9rtCX" +
                "1dz91L0RbqU6Ql9DBS8wrMnyTm+nO5H6isdjpxsdjHuoVjVrD7" +
                "1D4sU620OgLpXUl/c5G06ibdIXvYmlrGvwubC9JH6C2lP6kuWC" +
                "14tUUsGrCUcqXqSvwUJf1i/WLKEvyWu++b3Sl9WLakp90VtpB7" +
                "fAhXR0vLzGSDpym9CXZlOF9vvJlk32rM8LiKDQl+BVcAE/Hqo6" +
                "q4W+ZE36yoW+mLxf6xHWWugrcYdSX7QmfblHC33Rdkcc7fMS+q" +
                "Iyoa9hcL3bhbbJHtrnBe2h2QGqWGcoe2i2Ne7D5rHXrLuFPWTt" +
                "rLNYC2hqtmfN4Rw6t5Ujlgl7yNq7n7sD5Z1oe+gSL3YKlKFnuZ" +
                "/ut6ngZf2bth40OwleTn/BC2o5t8Wa+bxorXmRPewv7KEsI3so" +
                "1yOVvnxe9iiQ3yTfKeyhPiLtoeal7aHQl+AFjXgfc6/gpesSL7" +
                "3VUvFS9pD2tT2kLfq2hT2MzZH2cK2whx4vzx4Sr1aCF5V535O0" +
                "h9Zsdp/m20HZQzrSzOMVtIeCFztb2cMkXmQPJcNawPnLghffCK" +
                "OhvFPfvgvGwh3EawwUwiizEDva42Gc0Bd/Be4U+pJ3IkgNslbF" +
                "6sAlcp94xeMFEHslwWu80pdWfyv6YicofUmFDxW8aD3Qri72Y2" +
                "dH8SJ9rY5H/PhXHi/6u87dpUu/zqQvZQ+hofMmreeaP6TysuYq" +
                "fel3uyesL1nT09cLgTJpIYW+nG3UchIvpS+7i77DTen0RUuP16" +
                "5UfZE9vEvzamDUIP81BSa45eBemGr+2XyH1SN9TYN7YLL5LvGq" +
                "5FaA6cJ/mW/ak92K4TcXC73PAhbbZL4dsNFtYJLeGin0FXjK7/" +
                "X6N3ObbOfeeOTPXRpVar4ll9tlCwl7aL4eL+In/Jdcv2Tujefx" +
                "Y3+KvNejBK9gCUj/6NS0L9fnNYCTM7VrvkHn7A/wujvRklYozL" +
                "PFciHMD9xNPQdgbsJjVJJ6qBl9hQIjtHdxbHPIR7eBOXqLIhgH" +
                "E3FJbZ+X8F/Ea2oaXtH6CkQHAf+1MxteMWFPf4466tRKLYNZob" +
                "2H9Bt6J+r82Ch6x61C9WfLd9feviIbXkJfQV60vSCxtUivyX/B" +
                "w8Iekr6k/zLuhyruWcp/KV4xYif8lzGd3l/FgD2keyuoJvxXwh" +
                "4uKagb9F/KHgr/ZRue//L1JeyhiufpzL7R/itWlrbC/qtDsj2k" +
                "vz6S19fJ/gseoS3tvzx7SCWNDHqPqfbQKVT+i0rIf8l1iv+ipf" +
                "BfO5T/UvYQxD2S/xL6sjam+i/idWXQHkb5L98eevG8PF/bQ/p7" +
                "VNlD601YZr2Hs40FsUWwGB6Dx80O1jZjvrUTlsNSijcW4Tz3W3" +
                "gKlsATxjzqf1XEudYOFocV1gdSUZck6W1WWF/0dS309uwJAeV3" +
                "SpRK/1UwVp+xO+mbbZidvlS8ka09NBZHarl31vbw53T20NqeWm" +
                "60T2w9VHTbXnxofWjtCjzlk76+aBnQFw6FKrFCpS8cAs/g4oKu" +
                "Wl9P4wilLxHPK32p+NDTV6J1rS/a0vqS3y/6+rJ7Kn3J2tIOpd" +
                "HXhVH6Slwnoa/o+BCejtaXuStKX158WLS+DJ5OX+6tqfoKcB5b" +
                "tL5wdKS+Vmp9VY/VxXXwPKyxasBaWA3rcCs8R/5rlbGSuJD/w/" +
                "dxO26SV7sDN+AqKlmDG2n5Gv1twffkN/Gy/jbexWfxOXw+5Zt5" +
                "G9cH/Vcw3kiqua24vj+gr/HZ6ss6MZ7XzyybY5wyNovn0f4LQ5" +
                "EA6B4f2UNuvSf0ZdXgn5C+1tt9rG3mMNLXC1Bo30DXkG9ZxPPU" +
                "xgYRz7tjhP+yPoBBSl9ePG+ODOnrRbllmMMDV/X11dfXlz4m9a" +
                "XsoR/PF/FsVyXbw+R4XuprcFhfilekvgLxvNxPE8+bFaLiedny" +
                "9tR4PsB5RNHxPN/t28OAvl7y/BdWdscjRaHWqyb1/uwfWHnBS3" +
                "8PLivDKjDH/lEz38AsKpWWQPmv0Fd3R+S3SLzYEcml9r5oAsn+" +
                "q1j62llq+jom3ZEo/xXkVXTbPi93XMDuHOfry50g9EX2qjWUp3" +
                "db0dom8lH0Jgo9Xr6+mAVNmW1NDeoLqwb9F1bx81HKf/m8Av3l" +
                "e1L1ZU48UPrCGUXoa0ue+qqcm75UvreI/vLZPq+AvoYpfbFzvX" +
                "jD+rvIbxC1HTAWa3j5DapxPh0fp3gl5zfC9jAp3hiv7eHkKHuo" +
                "8lFhXiI/n5zfKCFe+drDU9Pxoq08eYWu7/PS+Q2sCdz5UvKiPg" +
                "2Ud75gfbCW8F9BfWFtxYvoSX1F8zLnRPIaHqUv56to/3VgeJW4" +
                "/zojN17Z+K9oXlhH62sQcHaLzPcOFPoi/9UP68p4g/SFhaw/G8" +
                "xuctsKXqwv1lP6YgMiec0N8sL6GeONfQeTl31snrx6ljYvT19a" +
                "p1I72BimUg/iXWp3GJ6KOnPCplPt6Vl54XkRb3RSkFcg3uibj7" +
                "/38htJ8cbX2cYbdu08441rs4yFJibHG5nzhynnp+QPUeTAv4Ep" +
                "SNTxdLJTX8h2hwVYVsr6KeZnz8v5Kn6AfsXNH0peDfPk1b+0eW" +
                "GTUPkUmEDLe2FqrLriJfLz2ELWbJ61vhYUQ1/7Diyv4sXzwh7m" +
                "xevmg6Uv2uJ4rooPRbxBz9JH8pLxRuxOT19evJEpPjQXhvzXeb" +
                "n0l3P1X56+SifesMeVerxxvvJfvBwvg+vwYrzIkhYqNg23Sl4y" +
                "dgjno0T/K1M+ylyk8lE8kK2BOYKXykdlYw/zykeVQH85ejzFnp" +
                "Ll2bOLoy81npJ4E5vTjadgJ9VfVvleWleR8aEeT+HlZb3GEfpK" +
                "yveaD0fle83hXr43qC+5l0W+V+6lzAfINt+r9ZWS700bH2aZ77" +
                "WnReV7o/QVzvcqfWXO99JfhvEUXgmWJYheKsZTeGUVb+Blfj4K" +
                "r6AznlD9L5WPghURVv3R7PNRdOUjD1//5WzM0e+NyPWKqEeosS" +
                "f2wK7YDbvjVVx+W7ylHx/6+ahEhjmRP4y4m8XF4XUw4w0+N0Od" +
                "VkVf032l1HldmXheX19XC3158TzqPga+T3WewmuEvpT/ovXGSH" +
                "0toae9KYoXnbE+y/vadjjoy91c8rzC4ymevuT2Or3uJZdbfX0V" +
                "M954TMcbNyfzSo03Di6vTPrK5he7rdR59Q7EG+vkfLZhenx5qx" +
                "9v4Bt4o+Il52MPEbygqeAF5+Br0ErwEvEGvizjjcc1r6FevMFv" +
                "V/GGx6voeEPxOtTjDVqWeLwh48OU8WVAHEDx/GC8RfCifc2Ltu" +
                "oE9QWN6WmHhPWleFHNLlpfSz1esmXBa7TQl88rjUJqpdNXFK8o" +
                "ffm8EqXUH8OBert6WF9i/nyaO2mp15qX3kvwkntMLhO8aFv2Xy" +
                "DSxkK9VH15vHSN1innJHgF3ssgvZY9P5Aj+LAWx8I6nd9Y5dtD" +
                "+gvZQz4DdXyEela75rVM8Ur1X4eaPczXf+WcF8ndHo7x7CHehR" +
                "NoX4xsSX2p/ldQX6r/5fMS8YYsa6V4gZ69ai7nD/q8VP9L6Stz" +
                "vJFJX868ktCXM/uw05eem8KXe+OVsib1jflqlY/CiWL8i68Mji" +
                "8XMf71RCgfNSnT+FfQf2XKRzkrSmI8xVmWXz4qVCdtPoovPoDj" +
                "KXr+Bv4FP8RduIO2Pg60Owz/miD7aYjzBxnUviJd/6skf/jRf6" +
                "c9TPMEa/lzXM7e59KHxRaY70hen8n23yVtDGa6T2W+meg1D4i8" +
                "GzmnMTh/PhteZkaP5cyLPCcwf97nlf38eWfToclLzJ8v4uvchz" +
                "+i/O88/KdYFryqef3q8fL1JXhl1NdTJcBrRYnw2ny48gr2l/Xe" +
                "VqmgUWx0rvNFecrbyD0+jNZXtD3Mwpp4vGYfHF7ZzBeNjg8TLc" +
                "zk+qvkIbYsMY7g28PA0QHxQ+aXC6988xs588r7O+E7gONPIj4U" +
                "9hDk2Alb5Y1X+vGGFx/iBxnGK58OxYcZ59tkGx8W0fcKxYeypF" +
                "Tm22QXH5bE/Cj+jaSgYnVps/jeYH6e7ylGfn6V/ga+C1xxUunl" +
                "57PXV2nFhyUxfyO5/6XzGyeq+Yf0rivwH7Psfz0b1Bd7/UDoy6" +
                "1ZEvpyqx92+vLmA/wCnG0L9Zf3Bnl5+sqK1xpPX8nzN3LvL//f" +
                "HoZ5BeND9mGe4ynP6fhw/8HJH2ZvD936h13+UI+niPFlWnbD7k" +
                "znNwwr5/HltenyG/+r/uvA5TfYRz4v4wjsxj7zjhhVc+a17sDz" +
                "cjuXiL46Hj68jGODvPz5AFYN9rfgfAD2ua5PXsafb5M4K2o+wP" +
                "qDkT/MhVe+//9V+vry5gPAfwDGUeuB");
            
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
            final int compressedBytes = 1687;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmnuM1EQcx38zN9Pt7qF4ehoMbwFfCIKCLxSVhyKCKAQRBR" +
                "UIggRBLhLBqKhoDKgoB6gxgSCY+BcmJijmROUl0cMQHnIQRaMC" +
                "cmo4FCXBf6y/zs51+9zrtt3r7qWTzHSm2862/fT3+33nl2qapp" +
                "E6TRR6XG/Jbk3jC7J7yDdYD5J9ZKccbyUbsd1EtmP7NdZ60iD2" +
                "79Bb/hk5QD4in7DrNFPhC/H3/WSz5quQPdZxeklLZ8A0zXepqJ" +
                "GzvqzFUvhT/o8lX1lGU+XdEnqG1LGHRb891gvJbuik84KLzbyg" +
                "n5MXDMvygrGS1+eS10NitpkwK8sL2uXnBT29eMH1oh0q2jvgTi" +
                "9eMEP2K4295+H796/sd7fygr6ZDh5XMkRu74G7TXsnY30Q6yNi" +
                "REWrQMr4/SzRus4Jlzl5wUCsNxhHDHecMw7uhftsvOY186pQ2Y" +
                "uyL3ixl3K8aJq2o1VUxVE/aYMKVnmlMExux8qr+cLyryZe9Oy8" +
                "FtLTuY+uLSYvuqY1eLHFYXjZrG2xnHMFq2UrWb3NbhewXca/rv" +
                "Jt7Vtc9y4sro8hPxbuD73sq5T8oXthbzru6VQuftF0xZ9Z+zLe" +
                "e5N9Oa5mq5yhyTT/Xp1XfvsKV4LEr8wQ389nT2nwYvvk858HFX" +
                "jPKqTpXNyeg7+s13lBVxxdgr/PojV0NvafhP50png+qCboHBiM" +
                "PeFT4S7RToCJ/D/jGU6FKVRYKDCzfUE1nG8adbM890uxXi77fb" +
                "BeBde0yOoBOy+oMnrnym1HqIGLoEeWF1wheKEnhwFwrTziRrhJ" +
                "9h6HEZbZ5sN4uN80flS03HJMRrTGGwmdsXaR/V5Ye5t5wZVwtX" +
                "HkILgZbsHt7XAbtiNhFLZjLHNPAtQCgOqC3ArThTeZTCaROjKR" +
                "HkVSH4TTh0p7qTc2RKcPi2RfI8tNH1Lp9dlWZkQdPjQXv0yWuM" +
                "3vPyhVccSvQLxGlVv8atbz7FuPmZ83eB3QSroE0Rt0RzzXqnQK" +
                "rTcO5tbL8v6FP+TvBfCH46Q/bIjOH2YmFkUfTig3f2gidhjXy3" +
                "jP7CczL4UF4DVd8vqhNeNXEF6pDuXLy92+lEwAXvMlr59LnVdm" +
                "WjnzUhbZcgu4JlcMfUpr2C/Y9mpL8Ssu+6K9Q8evY+w3dpQ1Yu" +
                "8Ua2K/st/ZX8py5DXeOOJvy/FHAv3LSXaipPIbj8XEa3hoxVLL" +
                "Ttv9IUVdr8wq3B/iNusP/3E80yR+FczL3R9y0NdfKe6wiF0B1l" +
                "9rE3/YAq/RoSNgmqdIXUq16o1mXlb7Utblty86MmtfXI3OvjLr" +
                "25KeL4SXt95AEk32PaljBtF2caqJzPttiZdyMhKP4tDzyGtAaq" +
                "Ddvuiz/uIXr07il4d9PRfevqBC5wXp1H5+QY6X2tnuD6F/xZc5" +
                "XjDYykvPzxt6YwMePUXOzsy8Ws7PZ3mpHfX8vL/7UrtYZnHkME" +
                "Gfy8jP5+zLLT+P/RG2s8dbRj7z857vy07LmYOwivy8GIkcdC4/" +
                "r/PK5efVrtn8vLjjauf6yzSq4V2Cr7/UOcV+Z4Pke8tv/aXOza" +
                "2/eFd9/cW7Na+/dF7qIuv6i/WIZ/2V2VwMfxib3gi9XuZ93eNX" +
                "ppNTH6pjfMavPkn88uMPg8QvYV8DWKM6M5ffwL1rMv2d+Q22Oo" +
                "h9sbWh7OuQi2+YUa72xdZFM48628N+0/xp/99vuF7hXnFWiX2/" +
                "4d++ov1+I7ieb/5+w8VDPqO3lT3Vjebv2bJ6vnBe8swi8mqret" +
                "6Dzguo50/rep6cMetD1VCl5EhWb4DwkET6J/17G8tbLvS8aWzR" +
                "88Zen9/bFFvP67zi0PNWvZFfz4uRQ8/zJXr+ML0sjyVuC/M+JH" +
                "o+Gn/YrOf5Ul0fZkbb9aHsB8vPN0SnDyurItVnZZSP8sjPv8K2" +
                "tBDptmkxlsrqtsQrspj9Tp7fVtsIv1q+9xm3P4yq8Nf88yrnUv" +
                "72xVfxWlLH3+Cv8xX8Lb6Mrwwfv/jy1sxvtFX78ohfuOLW9Ty2" +
                "ZzzOs2Q0yKFWjl/dE/tyEHHJH4ayr48T+yqWfblY3KfmfJQ5vx" +
                "EkHxU2v1H5RGJfBUa47bHq+YUJrwJto4Z/FyOv5Qkv9/jFD0cW" +
                "v74v9fhVDrz8xi8nL6wuvMS4nh918nKZM3g+6u2EV6H6kB8rzL" +
                "4i5bU64RVUz/PjPv1hY4S83k14FXCO9IfGWPDCreEPbVryD3f7" +
                "wiOD8vow4RU9L36iWP4w0RtBePGm/LyKGL82JbzMBf4HntuPng" +
                "==");
            
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
            final int compressedBytes = 550;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt0rBFEYxr0zew6bpbYUce0OKdc+co9yI26Vj2gxfxvZFh" +
                "ciIUnk4wIp90JkfXWcHZtGM2vNtsO8s89TZ2fPmWam06/nPe/5" +
                "UEopcascon3H/z1dTnXZytfXaVH/LtNGvr5LJ/Z1M18/piVKK5" +
                "foiFbVr0QHKkCZlmIj2vb/jLj75PX1DpuXvn7xcn3Fk5duB6+y" +
                "8aJMKf4SD/BX6Pz1CH9x4pWTYYknFRFx4lXAQa+xc8qIZ5GNXX" +
                "nHQ/FGh17xkHYKxcPYBeJhUP4S72baXLP7slLIX8asqy0Ff/0j" +
                "ySL5hl9/CeQbgflLVuk7j/b9bIHnrr/VzjB+/ackgRdTcgbGLw" +
                "aUTPiLFS8BXqxywydZ7cWLXrjwosuK8lct/MWcYMKRV8SNhJE0" +
                "ahwtUpdqv+806sGrTHTq4C92zBrAiw2rRqwfsmPWBH9FR4Ylm6" +
                "PSl6jsf8kW7H+Fbl5Z8vkNv7xkK3gFyavI6NYWndgegfywU3b8" +
                "7C/Z5dtf7fBXUP6S3bJPj1+9ZY2HPeAVZDyU/cjnoyE5oPP55L" +
                "fsvqT1Q/D6I16D4MVuvuziJYfAK0SeGnbQwX4KL2/Fc/6SI4iH" +
                "jJhZchTz5bAov344hvVDJvPlcVef7p3x0Lz1P36ZNxi/AssuJi" +
                "rpPEAEssFJ8GLFawq8WPGaBi9WvGbAixWvFHix4jUHXtxU9vMb" +
                "82GdL7Oi4j1fXoC/WMVDC7w4qeoDXH5S9g==");
            
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
            final int compressedBytes = 568;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm0svQ0EUx824M6OJVxfWFjY2voLEQmLFN2ArIkRLqZXn1q" +
                "NIP4FHJISEkMYCFUFEhHgsSCTCypKwcs1tG/rwTO9tZ/R/krl3" +
                "5r7SnN/8zzlzc2uayca9H33qooXUTQvijnDZhPlHo8UmzBbjna" +
                "ZJnqweefn8CnKbMLqEz7LKywdeWvHqBi+tePWAl1a8/OClFa9e" +
                "8NKK1zDvJyE+yAdMkwV5H5skR3F0DmW7ICdkNzbeIityu0bCcr" +
                "sv2wE5jxzfiZ0/I6tknY2nMD8lG7/7PeQ4Zzw/9IMn9lLuaPzV" +
                "c0cxqxWiPPbjWrcGXlKI1zQLkBAbY6NsggXZCOKhKpYaDyO8Zp" +
                "C/dMtfktds3HVp8zKawMspfUV5JYzS5sXnwMtZXnwe+tIkfy2i" +
                "3sj5eLgEXo7paxn1oU71IX+nQ9u/WC97aGus1/x+rA3zX9l5cA" +
                "cfqGTGlYyH93J/Y1t9eI146FT+oh3J8ZA/JJxvQTxUjqTd9eEz" +
                "9KVVPf8CXlrxegUvp3iJPPt5sSrwclJf35twwXf/u94QJdBX9v" +
                "RFPaIU3lPHhNtufRmP0JdO9aEoAy+teJWDl1a8KsArO/VGlFfc" +
                "OMJL7sOx8UFSLqy0eH36JPBSV1/V0JdWvGrBSytedeCVCV6i3q" +
                "b11wJ4ZfX9RgO8p8zbDS++P8z5/NUFXk7xEtb/zUOi2+qzoPBB" +
                "XxmLbP6/8rK+PxQB+/WVXwReWsXDSfByLB5OGZvf32dsw3fqWN" +
                "4bX9ftvg==");
            
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
            final int compressedBytes = 660;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnE1PU0EUhj3tfK39F/wB3alEtyxMxMQNG+PCxLggxoVxAQ" +
                "rqCsQCjcaAf8CdRlPUgBgDWD6iUSCgkhC+FWPUYNhcprcNtkXT" +
                "AvfQGXxP0tuZ26/0PPc9885MU31f36OU7go2g0by2ukg0N30ll" +
                "7n+n30yB6fUL89DtrbEH0Iz7/KPf6eHtNTfTcoCnpHz4KygsYC" +
                "RDYTAzt7Xaxe9yB7TpFMFfSK9EXj29bXC+iLU19ZXrqvFK/4pf" +
                "J4xS+DV0Xr4UtkzxmKazpNv8LW74Lz65ut2YLzE859g0+u5la/" +
                "YXnX4fjzsNr1/EtfsQtbzl3Ele5q2Ho4iiw4o9lJMW3nX7bKiZ" +
                "nI/OE4/Ebl/IaeQu5cilBf01HqS3yEvrj0pT/rnL+Kp+E39sUI" +
                "N4MceMVrATlwiscXSqmjeXVz9/5wBeNX5fxhrN4oZM+VMAdFbw" +
                "n/iNVD15QX8X6KqUI95KqH5pC8Qyl5W7bKhEzKFtm+e16yDbzY" +
                "eB1WjdZvXFfXgkAmVQN47VWopu3yUnWWV7U6FrU/VEfAi0tftG" +
                "ZOYP+Lycsd53jXzPqhqcH6oR/6EglzsgTPDuTOqxnaKeTAnRBz" +
                "YsnUikXL5bRYFfNiWXwvesaPgt7sjj7lm/iKXDs7Xz6D8Ytr/C" +
                "qjHp5F7pyqiBl/eA7+0Bd9hbzOg5cvvMyVEvrD+vx+9xtXoS8u" +
                "fZlGBl4N4MXGq/kPL9MU0fj1ELzYeN1k0NcN8GLjdQu/B/jv/U" +
                "Yr9MXo59uyvDD/8oMXg74S4OUVr3bwYl6P6kA99Gb86oxaX+In" +
                "eHHxEnMmaY+LYTvcrzRF/06D/Up/wnRl9JXHPNSXve/P9Ye2XB" +
                "VWX3+9WqAvd/3GA9RDLl4HNgBlN1Ko");
            
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
            final int compressedBytes = 605;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmr9LHEEUxx1vZ3ZebWETgoU/ELzKH50//gILQcVGUlmJhY" +
                "USbUxM7Y8kYG8I/oBABEXZmMSohCQYEUUtopA/Q9Bw3qxXrPG8" +
                "uDizmfG+D45lZm722Pe577z3ZieTKWRsJ/s5jrS/sOXwupVr/7" +
                "j+fTnLVtha3jutZ+5kbC8Du/LEt/hz5Jt4vLI94GWcFwuutXb/" +
                "1hfbZ1+jvNiq4sW+K17sKOzfzo0fKl7y7Y1fOACvJPQVamwOvr" +
                "PJvBMWyPns9bcufXmn0JdT6+ECeJnjJReveEFfDyR+vYfv7DF+" +
                "HsavJX52m774BfT1wOPXKnglwUsGmuLXO/BySl/r4GWKl/ysnx" +
                "eX4OWUvjbAy5i+fvGXLOBTfJK/4jN8gr/WoK9p8DLG61Tpyx8O" +
                "c/sZfwi8kjL/aVxeojfL64+3UXiet4n/umXKi8QvYvfXF5VAX0" +
                "nkG+RRCuuh3bzIF20sEK0680PRAl4m9VXYSMJ3Npna76VyvE8p" +
                "3nrZ/2SWFz0qZl6hvh5DX67wktNKX1SB918u5RtUeetIFXxnb7" +
                "2sYz2kaujLnL78n6mP6pr6kH+8dLC0/0bfwP98Eqop+ny+FvmG" +
                "a/Uy1bmiL1hBjmn4wCleDfCBRTQa/5UfivrY+71pxC+T8Ut7Pt" +
                "8EXmZ5UYtWXs3gZZhXm1ZereBljldYf7Wj/nIzfuH8ht28qDvK" +
                "izq1rIdd4JWMvqgHvOzlRR13qtCeOPF0ZUVEUnf91Qd9GYtf/e" +
                "IZC8S4eK7OY4sxnD9MysSLuLzUeewIuZH887A/75bRKHxgj5Vc" +
                "AouBvjI=");
            
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
            final int compressedBytes = 501;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk8rRFEYxucd93TnPQspZSOsLWaLhbCVD+ELyMLC0kcwM0" +
                "yJxYzYKApFM92UJpI/QyJsKB9CsbuuuZMYKXSOe8+Z5637d5rN" +
                "8+t53nPOPTzjLpHnFvz3oosP91Xfd4t0RUf15wrtBOcSHQTnk+" +
                "A4pdva+8P67ze0S2V30W8ouqY9/0dFlz4qVOL4b//jLLSLGUnv" +
                "01ODv+jut/7iHPyly1+cV89LpMFLZx4q9xf6l1m8CuClj5dzTx" +
                "4Xg+ujKl7OA3jp48XLqv3lPIGXUXm4Al5G8VoFL6N4rYGXUbzW" +
                "wSu69ajkFG9APavXozbhL6PycAu8ostD3oZ2ludhCf6KdLxRhn" +
                "rmFHvQwPI83Ece6spDruB7pXX96wzqmVMBr3OoENf+xaSgfyWQ" +
                "h/+xvsGOOy3yCvoX9kdp5MXP2C9qmr/4ReX4ELx08nrbbyMF9t" +
                "sYxYvBy575l2yFevGpmr/a4C+L/NUO9eI6X1bhr1QG/tLnr1Qu" +
                "5KUuD2UHeOnjJTvDa0v1uzxMTnx5Nwk9o8xDHlKZh7JLr79kd9" +
                "Ovb4yo5MXDmnn1NP18uRfjeYvG82moF5eSfWKOPJEVGTEvFsQs" +
                "vqeY0L9kv8r+5YyDl15eaufLcgC8dPGSg8hDu8YbchTaxW2+rD" +
                "gPx+AvXf5KvAIJRMWe");
            
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
            final int compressedBytes = 455;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmDtLA0EUhXNlxySzveCP8K8INhZaKWhlZac/wEqMj4B/QE" +
                "FEC0VRgoJBERU1vqPgI4KNj8LC2utEUwQRETKT2bueC7vsozsf" +
                "58yZYWamHFcNHVY975urSCe0XXnP05K5r9Cmue+aa48uPr9vVf" +
                "6f0zKt6lb+NnRKa/ynoSPGfCmx89NX3WafV2oIvFzxYhf+agcv" +
                "Ubw6wMslr9+noV93Qr1IkbTtry74S1QedoOX1zzsgXqxzsNe+E" +
                "tUHvaBl9c8HIB6csbwGoQKURk9rMYpp0ZVRk2oSTWisrXnoRpD" +
                "HvrLQ52FdtHth2mq3V/pBPwlqh9OgZcoXtPg5bXPz0C9WJ9vzM" +
                "JfXv01B/Uis/9aCK4pl7xkDkq2/JUswl8e91+L0E7SmDxchwqx" +
                "7hsbyMN67L903g6vYB68PK5fB9BOVIMsQANRvI6hQcz7xhnWr3" +
                "r0DWu8rsBLFK9b8BLF6w683PEqnx/qks3zw+AGvNzx0ve2/RW8" +
                "gZfH/fIDtIt5n3+Ev0T1jSfwctw3ntE3xPSNF/QNObz0q/08VC" +
                "3gJWr9egcvf30+bIR2UZpy3wib0Dck9cOwGbz+7/qVyoCXK16J" +
                "D273JeE=");
            
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
            final int compressedBytes = 378;
            final int uncompressedBytes = 18577;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MtKw1AQxnEHAlmcRxDduPMKXjZenkRFN259Gwt2UcG1oI" +
                "iihgpKRbyU4gV1o+BDuI8HyUIRrUKOzWf+A0maZpEyP2bmnKZp" +
                "u3DdKVGosOTDXevd56Y/HuzGTrP7Y9vx5z1r+PO5Py7s/u37k+" +
                "z5ne3avuv59IZbq//wt1zhkWXi7M+8evEK5xU9WuL6/PU5L6/o" +
                "Ca+Q9dVmfvWTO6VwA+SgQBqDcdWSuPb1/IrXftsP41X6odR6Yw" +
                "gvKa9hvKS8RvCS8hrFS8prDK/A++Vx9ssqXm4i7/qKXvDq4P8b" +
                "k+SuYJJ5z68p6ivw/JpmfsnMrxnmV8nX87N4SXnN4RV4fs0zv0" +
                "pcXwt4Ba6vRepLymsJrxL3w2W8pLxW8JLyquAl5VXFS8qrhpeU" +
                "1zpeIb2+D7dB7gommXd9bVJfUv1wCy8pr228pLwO8JLyquMl5X" +
                "WIl5TXEV4d3H81yN0/339dUl9S/bCJl5RXCy8pr2u8Qnl1vQIs" +
                "N0Fi");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 17, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
                "D89U/+yF9/vD/zlZ/8Qf8AAAAAAAAAAFb8nwCw/+1/APsZAMD9" +
                "434DAAAAAAAAAAAAAAAAgDcaroTt8A==");
            
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
            final int compressedBytes = 84;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3KENADAMA7D+f/OklQ8ULSqxD0hYYKoAgNcdnXh+uh8AAA" +
                "AAAAAAAAAAAAAAAAAgzX8CgH0HAAAAAAAAAAAAAAAAAAAAAPjJ" +
                "/wmwpgEq37k1");
            
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
