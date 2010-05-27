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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 30, 1, 46, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 16, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 0, 3, 13, 4, 106, 46, 155, 156, 157, 5, 158, 0, 6, 26, 162, 178, 159, 7, 179, 8, 160, 0, 161, 163, 180, 168, 181, 169, 9, 10, 97, 182, 183, 184, 11, 171, 185, 46, 12, 172, 13, 186, 187, 188, 189, 190, 191, 192, 46, 46, 14, 193, 194, 0, 15, 195, 16, 196, 197, 198, 199, 17, 200, 18, 19, 201, 202, 0, 20, 21, 203, 1, 204, 205, 74, 2, 22, 206, 207, 208, 209, 210, 23, 24, 25, 26, 211, 212, 178, 179, 213, 214, 27, 215, 28, 74, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 74, 226, 29, 227, 228, 229, 230, 231, 232, 233, 234, 235, 86, 105, 236, 30, 237, 238, 31, 239, 3, 240, 241, 242, 32, 243, 0, 1, 2, 244, 245, 246, 46, 33, 247, 248, 86, 249, 181, 180, 186, 149, 13, 185, 187, 188, 189, 190, 250, 191, 192, 251, 182, 4, 5, 96, 6, 252, 34, 253, 35, 254, 105, 255, 193, 256, 257, 258, 194, 105, 259, 260, 106, 107, 108, 112, 261, 115, 120, 122, 262, 183, 196, 263, 264, 265, 197, 199, 266, 267, 106, 268, 269, 270, 271, 7, 272, 8, 273, 9, 274, 10, 275, 0, 11, 36, 37, 38, 1, 12, 0, 13, 14, 15, 16, 17, 2, 18, 19, 3, 276, 13, 4, 20, 277, 21, 278, 1, 39, 23, 24, 28, 279, 280, 29, 30, 31, 281, 32, 282, 283, 40, 34, 35, 37, 41, 284, 42, 43, 44, 45, 46, 47, 285, 286, 48, 49, 287, 50, 288, 289, 51, 52, 53, 33, 54, 290, 291, 0, 292, 55, 56, 57, 58, 59, 41, 60, 42, 61, 177, 43, 44, 62, 293, 294, 295, 63, 64, 296, 65, 66, 297, 67, 68, 69, 70, 71, 5, 72, 73, 298, 74, 75, 76, 77, 78, 79, 7, 299, 300, 80, 301, 0, 81, 302, 82, 83, 84, 85, 87, 303, 88, 89, 304, 90, 91, 92, 93, 305, 94, 95, 96, 306, 98, 307, 99, 100, 101, 102, 8, 308, 309, 310, 311, 103, 9, 312, 313, 314, 315, 316, 317, 318, 319, 104, 105, 107, 10, 108, 109, 320, 110, 11, 111, 112, 113, 321, 322, 114, 115, 116, 0, 323, 117, 12, 118, 119, 120, 13, 45, 13, 324, 325, 121, 122, 326, 14, 123, 124, 125, 15, 126, 127, 327, 98, 328, 16, 128, 129, 130, 21, 131, 132, 16, 329, 30, 133, 134, 330, 17, 331, 332, 333, 135, 3, 334, 4, 46, 136, 335, 137, 5, 336, 6, 138, 139, 337, 338, 339, 140, 18, 340, 341, 342, 343, 141, 142, 47, 0, 143, 144, 145, 146, 147, 344, 148, 19, 48, 49, 345, 346, 347, 348, 149, 349, 150, 20, 151, 152, 350, 153, 50, 153, 154, 155, 351, 352, 353, 354, 355, 1, 356, 357, 358, 359, 360, 361, 155, 156, 157, 160, 161, 22, 13, 158, 362, 363, 364, 365, 366, 367, 163, 51, 368, 369, 164, 370, 371, 372, 165, 373, 374, 375, 376, 166, 377, 2, 378, 379, 106, 167, 380, 381, 382, 383, 384, 385, 386, 168, 387, 388, 389, 170, 171, 390, 391, 392, 156, 172, 393, 394, 395, 396, 16, 198, 25, 397, 173, 398, 200, 399, 202, 400, 203, 205, 401, 206, 46, 174, 175, 176, 177, 36, 178, 402, 403, 404, 179, 405, 208, 406, 407, 0, 180, 52, 54, 124, 408, 126, 409, 186, 188, 410, 411, 16, 211, 412, 182, 413, 7, 22, 61, 30, 36, 414, 38, 415, 416, 417, 62, 166, 418, 65, 215, 55, 0, 3, 419, 420, 1, 2, 421, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 444, 445, 207, 446, 447, 448, 449, 450, 451, 452, 453, 454, 66, 455, 67, 456, 457, 458, 68, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 38, 54, 55, 473, 474, 475, 476, 477, 195, 478, 479, 202, 480, 210, 80, 481, 482, 483, 484, 485, 486, 81, 487, 82, 86, 92, 212, 488, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 94, 506, 507, 508, 509, 510, 511, 183, 512, 513, 514, 515, 213, 516, 517, 518, 214, 519, 520, 521, 522, 523, 524, 525, 526, 527, 95, 528, 220, 529, 530, 531, 96, 225, 532, 533, 226, 534, 535, 97, 536, 98, 106, 537, 193, 194, 538, 196, 539, 197, 540, 201, 541, 542, 7, 543, 233, 3, 544, 235, 112, 113, 114, 135, 545, 4, 56, 546, 133, 141, 547, 189, 548, 549, 181, 550, 551, 552, 553, 554, 555, 556, 5, 557, 558, 559, 560, 561, 6, 562, 8, 9, 10, 11, 12, 13, 563, 564, 565, 566, 567, 142, 568, 149, 569, 151, 236, 134, 570, 203, 571, 205, 572, 573, 152, 574, 575, 576, 577, 14, 56, 578, 579, 580, 185, 581, 582, 198, 583, 584, 585, 586, 587, 238, 588, 153, 589, 590, 591, 592, 593, 594, 595, 596, 597, 154, 598, 599, 600, 601, 159, 602, 603, 162, 604, 605, 606, 8, 607, 608, 609, 610, 611, 612, 613, 614, 615, 616, 617, 199, 200, 618, 207, 619, 127, 620, 208, 16, 621, 622, 623, 624, 625, 626, 57, 627, 164, 165, 628, 629, 630, 166, 631, 167, 169, 170, 173, 211, 632, 178, 4, 163, 168, 633, 634, 9, 635, 636, 637, 638, 639, 640, 641, 642, 643, 644, 171, 17, 172, 180, 645, 58, 182, 184, 186, 1, 192, 206, 60, 209, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 646, 243, 227, 647, 648, 0, 0, 74, 57, 649, 650, 651, 188, 190, 211, 225, 226, 62, 228, 229, 63, 246, 652, 18, 653, 230, 231, 232, 233, 234, 236, 235, 654, 655, 656, 237, 657, 658, 238, 239, 240, 241, 242, 64, 659, 660, 661, 244, 662, 663, 664, 243, 10, 245, 19, 20, 665, 666, 216, 667, 217, 668, 669, 670, 671, 672, 58, 246, 69, 673, 674, 675, 676, 247, 248, 5, 677, 678, 679, 680, 681, 682, 248, 249, 250, 683, 70, 684, 252, 685, 686, 687, 688, 251, 7, 253, 254, 255, 256, 689, 690, 691, 257, 258, 259, 692, 260, 218, 71, 261, 262, 263, 264, 693, 265, 266, 267, 694, 269, 270, 274, 695, 8, 268, 271, 275, 219, 220, 72, 221, 222, 696, 73, 74, 128, 75, 76, 77, 697, 698, 253, 699, 223, 700, 272, 276, 279, 701, 702, 224, 228, 703, 704, 705, 229, 706, 707, 21, 708, 25, 230, 709, 231, 710, 711, 712, 713, 78, 280, 281, 714, 1, 79, 54, 81, 82, 56, 86, 58, 92, 60, 715, 283, 282, 284, 716, 717, 232, 718, 285, 719, 233, 720, 92, 96, 59, 286, 287, 60, 255, 105, 234, 721, 61, 722, 239, 723, 62, 288, 289, 2, 63, 290, 80, 291, 292, 64, 293, 724, 295, 258, 725, 726, 1, 727, 259, 728, 729, 296, 63, 240, 730, 731, 241, 260, 261, 732, 733, 734, 735, 297, 298, 300, 242, 736, 737, 245, 262, 269, 738, 247, 739, 740, 249, 741, 742, 250, 83, 302, 306, 303, 64, 304, 305, 0, 251, 307, 309, 743, 744, 745, 301, 310, 311, 312, 313, 314, 317, 65, 0, 315, 316, 1, 320, 321, 2, 322, 323, 69, 324, 325, 326, 328, 329, 330, 331, 332, 70, 333, 335, 337, 338, 339, 340, 341, 342, 344, 345, 346, 347, 348, 350, 351, 352, 353, 354, 355, 1, 252, 356, 357, 358, 359, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 66, 360, 84, 2, 67, 371, 372, 254, 746, 373, 68, 71, 377, 378, 380, 381, 382, 747, 383, 384, 385, 386, 374, 375, 748, 387, 389, 390, 393, 395, 399, 400, 402, 405, 406, 408, 409, 749, 410, 376, 379, 414, 388, 391, 392, 394, 397, 401, 404, 407, 411, 412, 413, 415, 416, 2, 750, 417, 418, 419, 420, 421, 422, 751, 423, 752, 753, 403, 754, 755, 424, 425, 756, 426, 427, 72, 78, 94, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 757, 256, 0, 758, 428, 429, 759, 760, 110, 761, 430, 762, 763, 764, 257, 259, 431, 432, 258, 433, 265, 434, 435, 765, 766, 436, 437, 438, 439, 440, 441, 260, 767, 442, 443, 444, 445, 446, 447, 448, 106, 449, 450, 69, 451, 768, 769, 111, 452, 453, 454, 3, 261, 455, 456, 457, 458, 4, 459, 770, 460, 461, 262, 263, 462, 463, 464, 465, 466, 771, 467, 772, 468, 469, 470, 471, 472, 473, 474, 5, 264, 475, 476, 477, 478, 479, 480, 482, 483, 484, 485, 486, 487, 490, 481, 773, 265, 774, 775, 70, 496, 115, 498, 506, 116, 488, 489, 491, 492, 776, 777, 778, 493, 779, 494, 495, 497, 499, 780, 781, 508, 782, 266, 783, 509, 511, 784, 512, 785, 786, 267, 500, 501, 502, 503, 504, 3, 117, 118, 519, 787, 515, 1, 788, 789, 4, 522, 521, 119, 85, 11, 505, 790, 507, 510, 6, 791, 792, 107, 71, 793, 794, 516, 523, 524, 795, 269, 796, 797, 268, 527, 798, 270, 3, 799, 800, 271, 513, 514, 517, 518, 87, 520, 801, 530, 525, 526, 528, 529, 531, 532, 88, 534, 535, 272, 533, 536, 277, 537, 802, 538, 539, 541, 803, 804, 543, 805, 806, 540, 807, 542, 12, 808, 809, 810, 811, 544, 812, 120, 546, 545, 547, 813, 548, 549, 121, 122, 123, 814, 272, 815, 550, 551, 273, 816, 552, 89, 817, 818, 819, 820, 274, 275, 90, 277, 276, 821, 822, 553, 554, 823, 824, 4, 825, 826, 827, 828, 91, 829, 125, 830, 831, 832, 556, 833, 5, 834, 835, 557, 836, 837, 94, 7, 838, 839, 840, 126, 841, 842, 843, 844, 279, 845, 846, 95, 96, 847, 280, 848, 558, 555, 559, 849, 850, 851, 852, 560, 853, 854, 128, 855, 0, 856, 857, 858, 129, 97, 101, 102, 130, 131, 859, 132, 133, 139, 140, 860, 861, 103, 862, 72, 863, 864, 275, 865, 561, 563, 564, 565, 566, 567, 568, 278, 866, 143, 867, 868, 108, 73, 869, 74, 870, 5, 569, 570, 75, 144, 571, 572, 104, 573, 574, 109, 575, 871, 291, 281, 292, 576, 577, 578, 282, 284, 579, 872, 293, 873, 283, 874, 296, 285, 298, 875, 580, 876, 581, 582, 877, 583, 878, 584, 585, 586, 587, 588, 589, 597, 879, 288, 880, 289, 290, 881, 882, 883, 76, 590, 884, 885, 886, 591, 887, 888, 889, 890, 0, 891, 892, 893, 894, 895, 602, 896, 592, 145, 897, 898, 593, 603, 899, 900, 605, 901, 607, 902, 903, 594, 595, 596, 598, 904, 611, 905, 599, 600, 601, 610, 612, 604, 906, 907, 908, 606, 608, 6, 7, 609, 613, 909, 614, 615, 293, 910, 911, 912, 294, 616, 913, 296, 914, 299, 915, 916, 617, 618, 917, 918, 919, 619, 105, 620, 621, 622, 623, 624, 2, 920, 921, 922, 110, 77, 625, 626, 628, 631, 78, 633, 923, 634, 636, 924, 637, 925, 926, 79, 635, 927, 302, 638, 928, 929, 639, 930, 931, 932, 933, 934, 935, 936, 937, 938, 303, 640, 939, 940, 941, 942, 146, 644, 647, 943, 944, 649, 650, 651, 643, 945, 946, 947, 645, 652, 948, 0, 949, 950, 951, 147, 8, 148, 653, 646, 952, 654, 150, 648, 655, 953, 656, 304, 954, 657, 658, 660, 955, 155, 156, 956, 305, 309, 957, 661, 958, 662, 959, 663, 960, 961, 664, 665, 666, 962, 963, 667, 964, 965, 157, 966, 1, 967, 968, 668, 669, 671, 672, 670, 106, 9, 673, 674, 13, 969, 675, 10, 970, 971, 972, 973, 306, 974, 676, 158, 975, 307, 976, 308, 677, 977, 678, 978, 312, 679, 320, 321, 979, 323, 160, 161, 680, 80, 681, 980, 981, 982, 983, 984, 985, 986, 682, 987, 683, 988, 684, 327, 685, 330, 686, 989, 687, 107, 990, 991, 11, 688, 689, 695, 698, 992, 699, 993, 700, 994, 702, 690, 331, 691, 108, 995, 996, 12, 997, 703, 693, 332, 998, 333, 999, 174, 697, 1000, 1001, 175, 1002, 176, 294, 701, 704, 1, 1003, 335, 1004, 1005, 109, 1006, 110, 1007, 337, 1008, 338, 1009, 81, 3, 4, 705, 706, 1010, 111, 82, 339, 1011, 340, 707, 1012, 9, 1013, 177, 708, 709, 1014, 710, 1015, 182, 312, 712, 711, 713, 714, 715, 716, 112, 313, 1016, 341, 111, 1017, 112, 1018, 113, 344, 717, 1019, 314, 342, 1020, 184, 1021, 1022, 718, 1023, 1024, 719, 720, 186, 83, 721, 187, 722, 345, 723, 84, 724, 191, 725, 726, 115, 727, 728, 729, 1025, 730, 731, 733, 1026, 732, 734, 13, 14, 736, 15, 1027, 737, 739, 738, 742, 1028, 746, 740, 16, 749, 17, 1029, 741, 743, 1030, 192, 755, 1031, 1032, 747, 750, 1033, 735, 346, 748, 751, 299, 752, 753, 1034, 1035, 1036, 754, 756, 757, 758, 2, 114, 85, 116, 759, 760, 762, 1037, 1038, 1039, 1040, 1041, 1042, 763, 764, 1043, 765, 766, 1044, 347, 86, 87, 767, 769, 88, 1045, 301, 117, 118, 0, 119, 120, 348, 770, 1046, 1047, 1048, 193, 771, 772, 773, 308, 774, 194, 1049, 315, 776, 777, 1050, 1051, 349, 775, 778, 779, 316, 780, 8, 196, 784, 9, 10, 781, 785, 786, 788, 789, 790, 791, 792, 793, 794, 1052, 796, 782, 1053, 797, 1054, 798, 1055, 121, 799, 800, 1056, 197, 129, 1057, 1058, 1059, 350, 1060, 1061, 1062, 352, 354, 801, 353, 1063, 802, 804, 1064, 123, 1065, 1066, 807, 1067, 18, 125, 355, 1068, 1069, 808, 809, 811, 11, 1070, 1071, 1072, 19, 359, 126, 1073, 812, 813, 1074, 2, 356, 199, 204, 200, 357, 358, 1075, 361, 1076, 1077, 362, 1078, 1079, 128, 1080, 131, 1081, 1082, 1083, 1084, 1085, 317, 206, 814, 1086, 318, 115, 364, 89, 319, 1087, 803, 815, 805, 817, 819, 818, 820, 325, 1088, 822, 326, 823, 1089, 116, 90, 824, 825, 132, 1090, 137, 1091, 1092, 1093, 1094, 139, 1095, 826, 827, 367, 1096, 1097, 1098, 1099, 1100, 1101, 5, 15, 1102, 1103, 1104, 828, 834, 1105, 1106, 829, 838, 840, 1107, 841, 842, 1108, 830, 843, 1109, 846, 368, 10, 849, 11, 12, 1110, 1111, 832, 833, 851, 20, 21, 208, 836, 1112, 209, 1113, 91, 835, 837, 839, 845, 853, 1114, 854, 855, 1115, 847, 848, 850, 1116, 1117, 1118, 327, 12, 215, 216, 1119, 857, 858, 13, 861, 330, 1120, 370, 374, 13, 1121, 14, 1122, 856, 1123, 862, 863, 859, 860, 864, 865, 217, 1124, 379, 866, 1125, 1126, 140, 1127, 867, 15, 1128, 22, 868, 141, 1129, 1130, 1131, 1132, 1133, 396, 871, 16, 1134, 398, 142, 1135, 1136, 1137, 1138, 1139, 415, 872, 388, 1140, 397, 1141, 416, 1142, 1143, 417, 1144, 1145, 1146, 143, 144, 7, 8, 869, 870, 873, 874, 418, 875, 331, 1147, 1148, 419, 332, 1149, 1150, 334, 876, 14, 877, 218, 878, 1151, 92, 1152, 1153, 219, 220, 221, 1154, 1155, 222, 93, 879, 880, 1156, 0, 223, 881, 882, 884, 885, 887, 888, 889, 890, 891, 1157, 892, 893, 894, 1158, 1159, 1160, 1161, 15, 895, 1162, 1163, 883, 886, 896, 1164, 1165, 1166, 897, 901, 903, 1167, 335, 224, 225, 898, 1168, 1169, 899, 900, 902, 904, 905, 336, 1170, 1171, 906, 1172, 907, 1173, 908, 1174, 1175, 420, 132, 1176, 1177, 23, 421, 1178, 1179, 1180, 1181, 422, 423, 910, 424, 1182, 1183, 911, 1184, 1185, 1186, 1187, 427, 429, 912, 428, 1188, 1189, 1190, 226, 134, 1191, 1192, 1193, 1194, 337, 338, 341, 1195, 94, 431, 432, 343, 914, 913, 915, 916, 917, 918, 919, 1196, 227, 228, 433, 434, 435, 229, 1197, 1198, 1199, 147, 920, 921, 1200, 1201, 922, 1202, 923, 1203, 1204, 924, 16, 925, 926, 927, 928, 1205, 929, 930, 1206, 931, 436, 1207, 1208, 344, 933, 437, 936, 937, 934, 938, 940, 438, 1209, 1210, 439, 455, 941, 456, 1211, 1212, 148, 1213, 942, 458, 944, 461, 1214, 1215, 149, 1216, 462, 945, 946, 947, 463, 1217, 347, 349, 1218, 950, 352, 355, 952, 464, 46, 1219, 150, 151, 1220, 1221, 467, 953, 1222, 1, 1, 954, 955, 956, 957, 958, 959, 960, 1223, 1224, 1225, 961, 962, 1226, 963, 359, 964, 1227, 966, 1228, 468, 1229, 1230, 152, 1231, 1232, 24, 1233, 153, 1234, 1235, 25, 135, 363, 365, 366, 469, 470, 967, 369, 1236, 154, 156, 157, 1237, 1238, 1239, 1240, 238, 158, 1241, 968, 1242, 969, 970, 971, 1243, 972, 973, 975, 977, 978, 980, 974, 239, 1244, 1245, 28, 471, 1246, 1247, 29, 472, 1248, 375, 1249, 376, 976, 1250, 1251, 1252, 240, 241, 979, 17, 242, 246, 981, 252, 1253, 982, 1254, 983, 985, 984, 473, 1255, 1256, 474, 475, 1257, 1258, 476, 477, 259, 286, 287, 478, 479, 288, 986, 987, 988, 290, 291, 1259, 480, 1260, 1261, 484, 1262, 378, 482, 483, 485, 1263, 1264, 990, 991, 992, 1265, 1266, 1267, 1268, 1269 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 0, 20, 7, 21, 22, 23, 5, 6, 2, 24, 0, 25, 26, 27, 28, 7, 18, 16, 29, 30, 0, 31, 22, 0, 32, 33, 34, 0, 18, 12, 16, 35, 36, 37, 38, 39, 33, 36, 40, 0, 41, 42, 27, 43, 44, 44, 37, 1, 45, 46, 15, 47, 40, 48, 49, 45, 50, 35, 51, 50, 52, 53, 5, 54, 55, 1, 56, 57, 58, 2, 59, 3, 60, 61, 62, 12, 41, 63, 63, 62, 64, 65, 66, 67, 68, 69, 70, 64, 71, 65, 66, 72, 73, 67, 72, 74, 5, 75, 0, 76, 77, 78, 79, 80, 81, 77, 3, 82, 0, 83, 84, 2, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 16, 81, 87, 97, 98, 99, 27, 82, 100, 101, 91, 102, 97, 6, 36, 2, 103, 0, 104, 36, 105, 98, 3, 106, 16, 101, 103, 107, 105, 108, 109, 0, 6, 110, 111, 112, 113, 114, 113, 115, 44, 116, 7, 117, 5, 118, 119, 120, 121, 122, 123, 124, 125, 0, 126, 1, 127, 45, 128, 129, 124, 130, 0, 131, 132, 0, 133, 134, 117, 135, 136, 137, 120, 2, 138, 121, 139, 140, 141, 9, 142, 3, 143, 125, 0, 64, 126, 144, 145, 127, 4, 10, 146, 33, 0, 147 };

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
                "eNrlXTuPXLcVPkNfLeiFg9BCZG9511CQCaAirhyoyd3Ny0mjhW" +
                "AhMYI0aZJCZSpDBbU2glWnQoXKgQoD7vwTFoEKwZWaIIZ+Te5j" +
                "ZvY+SH6HPLwzq/gCWml09pCHPDyvj+Sd7358+8EHT5b6Jzce3/" +
                "n2nRcPLj95dLP66g//ed/ef3XX3qbv7tX0b5af1vSHI/r3Lb3m" +
                "//Cb5Z+fNfRfv3jw8pNH9wf8iC5tH/G/5fITLWj0mP4HafuXP7" +
                "r9mxuPa/0T3fn25PzW5cHyPln927qTinYx/gx0T/99evr6BvQ3" +
                "9/7+1w+fLP/y7KdfPnzx+6//9PJXj/52+q/P/1vT33xxMf/8Nf" +
                "orHi+PnjX6W5zfenmwvBmlv33rd277v+7jF9JJU0HNz/VTfzBq" +
                "/e+q8Q979o+q76yq3s/6URn81+zzv+/4su/1NX7s8KN4fq77/M" +
                "4d3wf2W2zie9WP7zC+dfmblz7r+s6gPzS+OfVzk+iGuXJIisxn" +
                "pv5pGxWw/BMnP3nSyndnjvG1Im+SxIP6z9HZLzorrZTSNLt9rP" +
                "OPLn9ctPnjIP94c+9n6/zo8cPVNj9q+bf50ab+WHXjU9dq/ebP" +
                "30fPogjl94iO8ndE/3dH7/LHkzZ/3NJVBrq0vpC2H8O/6vhPY+" +
                "RDj9S/SfP7Db/PPnfVv4+/T191dNWno/xWSh/K/3Ur/+la/i8m" +
                "45vSpe2P+anHT2njI0n/k/ZdSd8VobWvwmN/ilW/VEH7QfWDUz" +
                "7HY3wEDRhBfrZvuku//vowS/+ULp83P9Kc/NaZH4D4i/K7dYBW" +
                "VG4tYLBUEL+U3p/U9ZKeu3+K4Rfnrz1+l/4OD+nWTV0vnxuP6/" +
                "7Lij7+p6m+Onm6sPef392INMxvy35+W9PfC+W/iM7Ir0XtH/7R" +
                "O76Lu+v8LZQ/S/PvPn3V0YfxFdT3mF5R1GNDDj6Bv4sfRegXC0" +
                "8IKIL+34L4wKRD+X3RKqCIhSOyGa2GYlleKNSVpaJxf50g2tJR" +
                "8/eydYimaUgNBLUT/vpHefUbBRjcSCxVi21MMyLV2c/zxr5N/V" +
                "uXrf2kzR8jl+DHf6R/MD9hfjh+LbQvtv1qz/roxldOxlcy6aD9" +
                "ZP3ayPVjeM4msn8mf+FfXy772di/jh//kWv9uOxz0z7qH66/Ct" +
                "mcGXw4Wzunyq0NG21/CYZg0vVvk9ZvkSp/rV8V9g+R8QOPL44f" +
                "t4/8B438p+spZvN/cP2A9T+1DzMo4bWvf+sO55N4VcWZn4Xyxc" +
                "VfaH8wfsjsJzb/tB5bN+TOj8D4GPhuIcF3pfjosfle1dZRfGQO" +
                "1OKXRMfWLqg0paZVSSeJ+UPF95g72l84AvXNka++2ZV8PnyznX" +
                "/WmvbkJ8L877rjmwn8FMdfcfxLchWA9WOGYo/8T7T8kfpH+CyK" +
                "L5z9GRH+y6vv8WNiqj/Dpsvx1Uj9TYJaOH5J8XUL6iMc/6LxS4" +
                "rERymEzzrwW1fuuKErB/9e6Rnw6+D8J+gvEt9LWt/8/FZc3+SM" +
                "nyH/1bcfzc+/0/HjFh9H+LmDTiN8HeHbQfxdEDmvlnQY35+VHi" +
                "/fZ7vu/z3J/geyf7Q/4aBTny7efxHub/TtzzjzZ0L4Nsx/QvuT" +
                "Db7RWPhx3eIE3zhg+TcV7197Ll6frX24Uv1aQjP5cXxA/ad6f8" +
                "sbH6zvpfiAVD7k9iLH56UrIX+c/Mn6iW1/ScrSYvXz83/U//W5" +
                "vWkXz1ZmpX+3LAt74N71qjLqD+LnbPxP9TRkFHf/aYvf1J278J" +
                "tj82pIp45OY7p204k+7bxD07tuJtFu8qfyqpBQ2xV75R+Z/hO1" +
                "38xfACFD40PywfED+TG+9sqpH4rVj5/ezs9H9RhDdDWhW9b40P" +
                "zA+V/rz6df/vhV2vpdjy9tfZJ8/Kj/yfzQ1L5Y8h+G5ffMH7I/" +
                "5F9qPyeLL8uOsc5/bGFrv2mV0U3zi7r7Dxz7AzYS8If8l8L4JB" +
                "w/vR7yT/IvZn5iEvcnCdaXwfsB2fdfYus/FH8vUPwR5icaCXoG" +
                "xn8m3D+u4uaXNf/XKT8E9gX3f5F9cu1XSp9n/tD6x/gT2t/dSK" +
                "Ouzl/QGF91yFsVLPuWyteMs6lfSzoYzV8ZSVdp9O35BF/9jvqv" +
                "1r/tbX+8vj32713frP79z+sh/zY+cccvxX9nXj86Mj5YDr5wfe" +
                "wf4tfofM2yxadWx1TX1/RuWdOf2jo/q5Yh/Ub4f4j/Vs5Nmd3R" +
                "KYHf+OnZ18d+8Rkx/jeLfBn9Bzx/mtJ/pP5MRv+1c/xO2n9MfV" +
                "E67Ve39nc49G+FXSxY+HOu+mBufNtXP+5K/+7+MT4K6psLOi+d" +
                "63u1keP9llQ02iyrZglUdGIW1lwc8eRv85vh+YjB/AE6edrfHf" +
                "4dwk/04Hho6biUya0PFa8+jV3fwv1fef5JnPg9qi8Nnx/6r+3+" +
                "m9t+NQs/KcD5pNnGB8+PEsqPxPqfNf6h+4se+VR8fjGQT+XKby" +
                "Lip5onf5m5fhLH75z5B0Wf/2G8n2vW+jdt/1KaP+ywvo/VnzS/" +
                "tz7/6mkf+7dzyf4jto/R+VC2f13nX8L9UZZ/7+Nj6/iu2/4D8q" +
                "muf9vx6Ibp6dX5uirBfykhPY//mVQ7uerb2Pi60T/i3699o/sf" +
                "+HyndH5j7MNxfh/jl+j9L+D8WqT+rGR+XOMz4fyD7R/I7R9gK2" +
                "H54f0H7bifut4f4fCj9ytI708g+eZe32n5mWx9Rfu3+PVT5ccX" +
                "Pfz4/Y8Af1hrvvSUcvj+Qbh98f1X8uRPliWf9H4Jg1/0fh+kX8" +
                "b9jI87fGsymRfdx0oN8jc3fhWi72F/gT//cP955vgtvV/Bvz+i" +
                "4+XPcP4m//2GyPsd0vMrvP1DikU9euML6h/r91Qmn1NQ40DnlA" +
                "efe97icyV1+Fy5xefKNT7HaD9EZ+O/Yf7098OQfP5Y9bkG9uVu" +
                "n39+TkW0H0EH+9eM+Bfkh3TUvxwfOJ8Zf0jsf8XKf9Pyu3z8+2" +
                "7//4eu0+IT2//80OdXFl9m49+3/NL9sf3rVxbf35r1KUzUffQI" +
                "+bSDIH2/Pep/cn9kej9GhD/77t9of/tmKB+6vxTOr8P3bwzH/q" +
                "T7J7H2bULeZ+Edn29/nQanDxYgv/bRffz8+1Xj+R/Kj+lp96uu" +
                "D13vmf8tpTurp2D8NW7/JLN/tP4C6xfYN7y/m2l8aeOH9xPl8x" +
                "Ok54tPh6D+c9cHWfbHWf4f1SdMenL+qnlpTSz+wswv/fdnZsaH" +
                "UH479/hp0xu6X5QNP4vUv2z/Ie38QEb5IP9raX0X9C/H5mKN7x" +
                "at/ypb/9Uot8F3TQp+77vfm7Q/H41/xuLP23/7zr8DOjO/ZX/R" +
                "gE3CVxkpukpc/5H3ix34pg7F93H7DvzQI9+mflR7xidl9dd2Qt" +
                "Us+KAYH2a/f05FzL/r/XmJ+xP7ole8+eHnZ+j7GxLpOL9i5hcy" +
                "fNR/P5qHL0TEzxF/+P41Xt+vw/efpfm90D/v5Pt3Y+JXJL4o9r" +
                "/y82ey+z/Z9ndmokvrA2l9hulz15+y73/Itj832/xlWh8e/yO2" +
                "TzT+auth0vxDmL+PwKfZL2gf3//f6/5+R+9BdaPvx+K1H+AXn7" +
                "8X+m/p+UDyn29WlOn9BEVsfpxb/3PGX4L14fT+vGnuz49mSIXq" +
                "Az992/7Iv/fu53v659F5/U/9a0F5xiem4/xFNH52ASU6Hzjf+T" +
                "Mtiw8Z7BPFhyBd6dPu/qVRyvX9Sfj9PrD/BHwL6Tfr+gjLj/wT" +
                "cfGJnHSKeP9DJP1ajO8HRI9ff4z7wRHnJ2L7d5qjCi1N8H6Ufd" +
                "f/UvyfS39L7HP03/h+Ucr8xtS/aH9Fqh9p+zL+BHzXkX/V9ltd" +
                "2W8TTsv2/rmlyftdYvFXdP8aQkTGsb447y/KRTdht4LeP4POT4" +
                "D9Jfz9hsL8lGd/ke/nifSPLHxXXU/8dv77x7Lvb0navyXK9f3B" +
                "c+9vsuqDPPHXvQD/B+v63ec=");
            
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
            final int compressedBytes = 2827;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTuvFDcU9jhzr4arRDFXXEI5RBRbUCQVEdVAQkTSBKEgJR" +
                "0/IGWqiMJ3RUG6FBSUK4pIdPyETUSBbkUTiZ+TeezuHe/Y/o59" +
                "7N2LxDQ8zvp1bJ/H5+Pjs89vPPri1eyX5wenv73+9uWjN7eePG" +
                "ye/vDfZf3w7W19Q5z9dOPR1Vez+1c6+icvHy1vPTle0d93dFEJ" +
                "73eG6mfSLZ+y90SCn9jpwe3rsPGj/pvl/wb8mdLR/LT0Umx+Va" +
                "7bb8L5Z/zA/Mp4/rPpePxalFLUqx9WWlzr/py1/9PX46LLeqsf" +
                "Q/uFrX+lja6t3Smm/V+1f9T/81Lfvmr7tyC236zad31q/Yccr9" +
                "/KrLisxeFWmQV1flblpau8v340P7j+/uO3fzRq3+A/4g+gK5Ou" +
                "B3q11b6YjD9g/7H2F+gfXF+NCPp0aHlArxrLgleS3j6mFwSOc+" +
                "r30rH8Al9RevUnLN+w9D9pfVr1h6bKfy9/lp/d+O7gdFZdEeLm" +
                "6zvzk+Xh7KHQ1T3VDazvX5N1/F375ensWt9+0bd/bLQP5R+vfd" +
                "L8lB7xYtgP5wKDLp5C+j/YN3dX/fuDMj+of4C/sH24P5TJAO3a" +
                "VnHy+d9h/VjXr7Tblw6BJYH8AeqFWj68frnT/sXIr4z27XI8vx" +
                "b5AO3/iP0VIp8xf9j64XK/WcqJsHgG1K+mKdIGuGCITlLUshcn" +
                "w2JRW6qt2G5ppd208b/KKd8iv6F66N+j+YPlYwwdxaRT2BJZv6" +
                "bwlt9/cvlKzO36a+GxD5SvWZXEP574pw5xBybqWIiDkQaVQv3c" +
                "eaq6G5cklI9bn4qsH/LbXw3QavJTtZYhnYt27cFXw+JopKww3e" +
                "IfOTaSffxHP4qT46r90cFp+5O6EV//rpqnd/4q9MNnt4fyMpx/" +
                "KjM9YH0g/ux7frj6lelf8P3PhiBLp2KRbj8ri/2ngu3verJ+62" +
                "T4ZdMbMfW0yGEIfuKqXyP/2Ks/ZNWKXNWtT7len21HVVvpcli/" +
                "gJ5/f2fGj9PYfxAr8tZPwj9o9RfR/Mldv3TUvw/9cJHWX6B/m7" +
                "x9vYfxfaRfGHrdrYBicWn+uP3Lr7oV9U8XalF9PxOlTqKfYvHf" +
                "j/OzW3osfkJ0a5UI/FLhN1T/x/5dV+9luz7LL1vjp/im/bfWrS" +
                "mn6kosanFnvH4RI6Sj/rdm/WKoX6zq3zcd2f+wvLjf86dz7bsq" +
                "ip4vvX1Tb+bHoAuDjuuPta9o/g2df9JF79t30YV46+UPe/4A/w" +
                "n9Z7bfsNpPsj8Z8pFb/3X1oi9fi0PZla8n5RPYnyXHvuPaf/su" +
                "j+2bwe+odak7v1WKqmN/J2iujvGXCsCy+7KfmfVXS974EH9mQ8" +
                "FCzAb+aqkM/qL1XQlv/RHxdWH2AcSv3iXBl8L8u4/0C0OvUNFA" +
                "fHNa/5x2vhKJf0bHR47a98SHsPFB0FECvg2+B2F0m3xk+aeo/W" +
                "D/LCj+2W1/1L39wefvvv3X7P4hPj8pc+KPSzD+ZWb+rCHbYvhx" +
                "vQ1QE+tXsfh6jP42zs+JQIVLyC8d8cd1IvqWoTr+doK/LR3xy3" +
                "Vg/a7+g/Fj+8sRn1wz5EOI/r7Y8osbf8uP39V7pjP7V/ccXhTi" +
                "cfuPS92i0rpVj82Muv8S9P9C++/7rp9L5/p/PPx45T+XIvjTGf" +
                "3LlOeTOeyvdPIvT3zWeftNF7/7T121E3yzLvTJ8rA6bv/7Xv8L" +
                "QvnsdOz/rS1bc9mV2xJKOu2rqqdvyg+Nl7ooiPtnKH80Kq9G5U" +
                "H7gN7Ffznmdxwf6Btf5vinPa+PwPO9Igf+4tF/GF+gyC/LeJqS" +
                "hr+Q8RkXfzb+jxzbrwa9pPAf8Vf48N1A/UPxnxKtr3rNNyc+79" +
                "9/AH+ujCmpx93RJEYS5Oc+/Ydg/E0H4n/J8M9IfJKPr2J8Z+y9" +
                "rvSXcf8VfyXoU2564P6nn3+vJq7c8FHvZXz75J85/ml5RDeX22" +
                "p91Zn6H1w/9XxdDfhx+Pm6Lb7b2P9Y/njnh1t/YzJCW/VXNP9g" +
                "eaSfufqncs2/Hvunns4lwJ9Kiv3HtU8lZ3547XvGtzl/UUP8yH" +
                "D+IjbnL9C+4+MTEfc/WPGDhPtlaqxfaPLDlb8E+mdNRP9y+3eC" +
                "Pj5u/g2UXwbIR5L89+W/4d6/x/Yd7/4iyv8D7UNw/5+S38Wff8" +
                "FfHuWviWo/AP9IYX/7/Wt//h/2/VWUvyHV+I6s7cfdzxeG/r36" +
                "Z3+/8ebW/cYNvtnSK8v9Rxr+ab2fOE6x0XD3Jw8WR/Tc9x8zn/" +
                "8T8vMR7duQ9bmlnz34HMqvw7UPSeV9QwTjJ+R/Yea/CZQvbnpA" +
                "fpmA/sH7xwnw3Yz9w/LTz9+4+NLtiurzv5bTww0CdrTOHyK384" +
                "fEyUdufooLRU/Enzj/ipBfAuWvQetnHpVfA9pnkecTIvZ8Aini" +
                "3OfTLvuAp39Lk2XNiKopLjCSLzA/BDP/RO7zB7N/teW3NnrA/F" +
                "eCNf7c+Y9yz99O4rsY8c0XvX/7psP4a+b6zI1PEfyPOfDv5uB8" +
                "CQhynn8U7z/R8El2/HzlmsqKan/48UFSfBrNfq0i1gfiD7xfCf" +
                "HJzf1TOb4/6sIn0+FT5vqQhn0VcH8U4ENkfNZdv/eL578Nv43M" +
                "/5vM/vDZD677j+B+H7Sv31n7V03ta3//s9Gp9lWu+1emTAnOjw" +
                "/9F0Tn5QdLE3/mo/vvnyXQL/PKK7+Y99NC65/iszKnfZLOvnTK" +
                "l8jzh5X8jM5fqePw8Q8C/xHk/KEJ8Btwv41nn+46PpeGv6S8/w" +
                "PuZ2H96S1Pya/vz++f17/g3h87477PxPZPGr8HsfHfHOdDsefr" +
                "5vlUmcu/4+bfRfK/v/9xatz/aCdCbe5/2Onn90PY/isNX42+P4" +
                "HuZ7DvV1DoHHyKu37I+x/5J3b/QFZ3B/tSSdmLwBdT+9KLmiL7" +
                "9Jnf/oizb0Psb1S+kfD8wW9/ofzo1vrp56c8/cyV3zt5fyfkfM" +
                "UiH1jnb8zzOcQfbn55rv+M/M/s+Dn5/ZqU+Y2C+jen5Z9x4zud" +
                "C1k35+W7n9d9/J4mxHfj9eetHy1U2L9JeeVHUKw/cdJx/OYDU/" +
                "8Gzz81PoV6gS1KPsXHz9Pi8539Z78ftRVfVI7iixqR5HwoQ/x3" +
                "iH+do3/p/CtkX+b2/9LFd6fkXyA9IH99anyC/f4FMz4c7X9SfC" +
                "8jfn6bf0UoPulsn7o+efbr+sPx3Z74VJp+cuHPSc7/PBuE9z4u" +
                "5f1bVvw+M74e+efY/gTvz3LlZ5z9EoDv8xygXfCH5T8m8D/LUP" +
                "2S7nyFYN/j99G9/Qfv61LsV4Rv+aaA+X4Usj9x/Uz5ivcfa3z4" +
                "/VRueaJ9o8LYmOx8CPAPv08Zc36C5k9Nywe9DxGDPznwSTZ+xc" +
                "Yvkf3uxy+Z8g/iY9Zp/3DuD6D4bfw+X4j8qbGvtNXtoyPn+4ov" +
                "blPKo/cZkdsF47N5+DY3PjouPza9fG78ddfxzQVZEuos9RfB/h" +
                "8VfwrRP7vMXxiyv2Lqt+lHLj6VNL+g5uSvD3jfJwqgQvH3BP/M" +
                "H5+bbHxh78/Q38fhtU/if8koz1w/rvjbdPYdzF9m6BdLfo207z" +
                "tM7T8wULS/Ce8fcP1Xrn8s/Pi8N/6EjR80/Pv97PxGnvyV7PjF" +
                "3PqL578R7u9kLZ9APlnxO3r+e15+/jzxb1sLJfj9w3TvUy4HfO" +
                "ba8w6fKeYnbw5nxyD+U8VsC0xXwWwzyvnyP+HPF7/Hy28bhf8H" +
                "4Dep6Tosfx3Xf+S3z/Ovg/e3zjU/9v1Lx5eI53dOayfSfoP1x7" +
                "SfIX9pxaTHtv8/oCUxww==");
            
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
            final int rows = 604;
            final int cols = 8;
            final int compressedBytes = 1522;
            final int uncompressedBytes = 19329;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWz1vHDcQnWVWB1pAEEawYpWrQMUVblI5cLVKYiBJE0OwAP" +
                "+IlKkCF5TgwmUKFy4PKgK4808QDBeGqzQB/HOyH3e6496Sb8hZ" +
                "6oRs4Q+NhxwOhzNvHmmi7ivJ+QwNvoD86VKu+r/agVwT+GpXf2" +
                "v81Z9duV6Pb6lUVC1/oi0dtb/Pm5/0ulCfaZ9n/ez5lwbY4bT1" +
                "1uju+usRm9by6y9Pftq7mOv7RA/fnV4eXs/mZ2T1E9Mq2pMNHT" +
                "W6OrW11PWErezTVyfnD97On7/eu/j93Y9X5x8evTirX/7y79f2" +
                "7OPjZnypfHz96w/q6zrkPMb+1Hj/S2J8KmL/YuxbRV8/fjF2Pg" +
                "Ny139/d/77Yem/Pzv/h+NzTN/1/8r+/Q37jdf+kfPD/cYDeDWW" +
                "iTlfEf6P3B8C+zOS31pBWdHMyZ+aFr5cHNifq8D+eNcn9D86H2" +
                "B/cP5E/gnKcf5BR7TGnin97jk2b1QjLyszo+J7osraollapamq" +
                "6JThXk5+CuYXmX5bX8qL+dHrtr4Ul4cfZvMDp77obvV6wxVGjZ" +
                "cT3/kL68P4BPVVqP++X/9ofVXN+pG8H6haj1kOTjGyT7OiTy2n" +
                "aNZQmqjj6/WJnWT+clWSNiJhObp1fmoo8rO89fPwHQH86/ePC5" +
                "ei8duE8/P07RY+uhzPv4sAPooYH+lrGFzFcDAzCAAQH8B/CL+F" +
                "h93fp8MD3Sxi76IZoqrpuz9M/fL0r8KevXnMMUt6/oF8/1evfa" +
                "849rHqJ8t/yfj4MoQPGPjH46r+b59+Ozn/5u385/ut/hdX59eP" +
                "Xhws9T+v+wt//ob6IKSOzaseHzS/dPiAenxAHT4wsH5g+1Pgnd" +
                "mw73Nn37fU45fjDr+YBr8sevwyBX4Ozv/RnZ/6+Wk5P8O+4DfB" +
                "+KL1Ef0j5CcG+nagz8GvG1/hxbc06C8tU98knu9FZH+Smn8Y/R" +
                "sPQCmROB1/Bcdn5ydvIZDiFy5+8J4PT39XMf1z7erfjF+xxs9f" +
                "/wjhI1l+1cj5RdSBLVLiO9AfK63ImHbYRt5uwdHTxnBj231T2u" +
                "kPwvjaK0f2h/UPGuS00SE2xj5r/6Vt9VRyfxEjh/kzs/6gAkzM" +
                "79/UVzNTo/XVw9+Hbic49wdsfh/Er5xf9uUHy8o/2fldyD/D+G" +
                "Lio0ACCfOnMn4xWb4IyxWzP+HcL3DWn3y+8/K3MD4z+5+xPsAv" +
                "s/jRnv8rOv7P5UfF9z8y/hTfD8r4Xzk/jO5HkTweUsXxf7fE30" +
                "Xxn2Y6/lOIXxj4SHT/BMfPjP9ur3/z9F/y+4Fp2lc//gl+sf1T" +
                "Ec3/y85nTH+sk/wnsw/xT4i/wvg4LJfOn86fLci9X6c0/gzxY2" +
                "B+Br/s8Ff3bt4/xPJXiD+LtT+Wf029X5Ld76Dzh88n4IeE/A8r" +
                "PlHxLyX5tw72r4g/YfMr6fhHsfiZRP4P1f+Y+58wMPSeL6l+kN" +
                "+G/AcPP5XB81syo652N0xNm1/S3g/tuH/H/WOd9X0go39G8Rnx" +
                "Puou9ue79S+n/835/pbRX8v6A/n7Iplc2h9x+XWTur5a9D779u" +
                "pnIn7L3j9uzl8x+yMVcT+C+tOaJO8nou9PLSf++O8bpPUvffwh" +
                "vvW+35bVB5i/hf0RZ3xBf4zjA51f0J+I/Rc+P3VXXyrdLPFhVd" +
                "imfukGctOTboTx+mJAWo57/8e+//Tt71T7nyqX4Vd8f3e38VH+" +
                "+0fG/cz/ev1MfJMPn+wWP079fnuQn9j3Cyz/VMDWaisWxe+HgR" +
                "yNj98Hy+KPhZ9Y5zcQCqUwvwfyA+SvheMz3n+K8B8fv6a9v2O9" +
                "bwj0N/j8+fgtpn+AvrT+7rw+CPm3u24f5Cdz8xOQP2DzD0nxn7" +
                "s+QHnm/3+Slp+i7GfFn3L6SzOZvvj+Nul9UMz9qLR+Su/fGPgt" +
                "cH8m5q93fT+Re345/8rLj6n84s7ft4Tth/8/C8QnkvPfb0z5/t" +
                "hMpw/PN8Bvd50/uY35s8Z3SvxPb5+3vkK5rD6I60d2/C8bPy0/" +
                "TyeH/ALEr3n5K+n+SNcH1/8fmM+cUw==");
            
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
            final int compressedBytes = 4831;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNp9W32wVdV1v0k6YoyiSYiNROnENtCEEEs7FZKibu4HPJ/Np2" +
                "ljGxIU1CgUwciHosA9Z59733v3gZLKpyLKx2s1cXSapjNNq4lU" +
                "beykVG1jlA9rBBTGZ62N7Uz7RyZda6+99lp7n3Pz9py1f+u31/" +
                "qtdTbn3nveuY/WeGt8ZHELbPZh++vDBxDhsDYbaY0PvVBcDCs/" +
                "JC67lFezH6EdOcNFvtYaz6dkl9RqMH+zVsso/5xaLX/Oa52bPd" +
                "SxrXGMwFH8Ddr23NGJtfDDymWEq3PH2ztijqNovT0ZenqQeFrL" +
                "m2Ud6OS3uZY9jqj7V/ajzjshMbobWPmgnYSzGTADdpazdTMw8i" +
                "Qi5/1hvsYMFIfsZ5kxA1kzrF6FdnTE4dec/bz9PTPQ6dqL7dXO" +
                "n2k/XRzx0Z8Zsfm/ik5xmObRnp1vP2e/aL9s/zgofyWg3/XzpX" +
                "bu3FPZkMOXea4harZlr8B56IuAvwDHlejlz4f13/fzbDPQ/Y6d" +
                "Y//AecftJdYM3WanOe+E0huwlyvvS/aPHMpNbvc4O1ar9ZYhct" +
                "59+YjJO+fb/cyYvFYTLD7sE9pdaFsvBvZe++2h73qtvbVa8wzJ" +
                "g33iKvfbv7QP4r9gYB5Jq+E89GA2nHIy7D7m8LDfQU/q2d1KZz" +
                "nXoutppGFnOO+ExOhuYOUh+7BDew2cBVkXsZcGvGp+bPYOf0kY" +
                "jkt9beuLtdrIcxRn367VOoXkdSeLAv+UazDCeXhG1ku5qj74oE" +
                "7KOlKL69rfqYoRbfumfcuhpWYp8N4SIi8/aZZ2jwsTr6K1S3Q2" +
                "zsUOmskfeZ6icZ+6HxOd7i5WUfvUpwZFDc/KNsac7irun7xie1" +
                "mHK+radkE/Pd877hOijWZjcbqz5xUfgYiNNKzNT5mNw39RnMOM" +
                "2Sir2idbfABtfVHwTyvO6N1HccX7YJ+M5A29WxTwp/hQ8eFyDU" +
                "Y4D7+c7XdKZ3vFybqP4kyOw6OYxJ341Ql+fi/Xo+hiYvF+2KcX" +
                "0loc4bM+WJzr0HKzHN6f0I4ZePUict59+Rswj8H703IeskpIW7" +
                "sL5/rVrgr68P5kxrzW3uyhubNEh3noUt6fuO4jaTV3ZV5EOcLp" +
                "ruD9yXN4wPvTcuwkKO5WOWNcK1zHY2U97bn3J0Q3mhvtrc6ugy" +
                "NH5DxrzzM3Dj9qb2AGuC2MQAls9/1os38mBjm7FbpexX7v5z5v" +
                "FN6fbhGdofexir3dtu0Gu5n0RFkju6ZWqz+fHY+4YVGjysAVtG" +
                "476HW+Gbpe6+e7KNZHr7S3Qf3MHo2ril7idUwHkLOO69CA3iab" +
                "Tu88YTgu9bXtrtNqvXc4Du6AZkve0MdFIfy7lmowwnnkG9mplK" +
                "vqgw/Twe7LOvn9XIvrFu9KY3Q34jWnNqfCp7az+IOIvPoFzam9" +
                "ujAcl/radh+oUnN3dJdL3khPFPinXEOyIePl7JcpV9UHH82p2H" +
                "1Zh/ZJVy6mpDG6G/HMQrMQdsxbQuTZKWZhrylMvFq2OHffppl8" +
                "jnf7NCg6I3cKH66nPjUoqv5cXos53VXcP3n2grKOWeivJ1W7+E" +
                "Q/vcTbYeCznKzjdtCA3s43O3qLheG41Nd2eE6Vmnvd/YfkjTws" +
                "CmGfSjUk2+yoP5ufm3JVffABGedX6fh9UpWLGWmM7kZ5y8wyQN" +
                "4SIq8+1yzrPSpMvFq2OA8voZl8jnf79JbojPyt8GGf+tSgqN6B" +
                "/DdiTncV908edp/q6K7CPn2mn17stf+h/aT9U/hN8lv0G2A2pf" +
                "1cNtr+KbzuPtF+sfdjWDkMB9xvZp+Ee89XAf/c/eb5387+X/uX" +
                "+SvZr7nMGdmEkY3ZpwCdmX0AYn+m92Humy7+Z+1X28dar7RPtE" +
                "+Bt11HZO/O3lNcCvMZcEzMPgT23PYT7QMwT+/9Y34hRbWfb/+b" +
                "Ry/B8XJ+rL0Ffw9uvwEd/wmg//X3jx/P3utUJ7nYH7XxTJ6Vat" +
                "0r/D5dIlz7vxh13Bm1t2XvchpnZWebnWYn7Jizbu920oBKnzQ7" +
                "eweF4TgZcD+ustmW1VxfX5W83mmiGK6nUg3JNjtHa/mclEv74t" +
                "rk2elVOlIrXE9frorR2t5rmRbcP6FdB0eOyHm2dci0evPg/qkV" +
                "uC2MIBcs3D+BhfsnxyDn75+8T1HAwv0TqvHoTWAVdf8UKWuE90" +
                "+jH8ljbljUuA7cP7l1uH8CT+rB/RPNd+mu+P6p+EpcVfRiLzzB" +
                "eVqeA3WWIlc/uzXeO4ZMdkdrvLgBufab7bf4iU6xxC5xT3peQS" +
                "Zbw89uaJVed8U3wnW+qVjMz3WGnqLnTxwP2Wvbv3DPtU7DmOLP" +
                "0udP9X/J5xU3tg9mt4V+fwIKR6H6Mchajc+foNpK7qG4Hrsvbs" +
                "rOcfy18mwpWwX+iuIWVxVskbVfL9zvdO23Xex1/jpbVtycXS9P" +
                "o8wesweuJ7TrYH9zRM6z9bPMHmBuYAa4LYJxwPUEFq6nPRjpIr" +
                "bCsYp89y9BeXA9dR+UvN7EoCjXk2fKyK5BnC+IuGHdh69dEILr" +
                "CTzs3seu9fNduqtwPd2pdXGWvvk8HKobuJdkS4g8exFaYeLVsq" +
                "V8zhBEuHtAdHpfFT68P/WpwVH5opjTXcX9k2c/VdYp9wfXzaP9" +
                "9BJvtVkNyFnHraYBV9sK4pjhOBnwPq6y2ZbVEDXul7ze9aIY9q" +
                "lUQ7LxyBenXNoX1yYPu6/S4Vphnx6ritHa3ttq4L2XrOO20oDr" +
                "6evEMcNxqZ/aspq7nt6RvN53RSHsU6mGZOOR35JyVX1Iffu1ah" +
                "2uFfbpqaoYre29ITMEyFnHDdGAfVpIHDMcJwOuJ5XNtqyGaOhy" +
                "yev9tSiGfSrVkGw88pUpl/bFtckr1lXrcK2wT+urYrS29yYZuB" +
                "Mj67hJNKCzHnHMcJwg2KcoW2toNfe75LOi0zskfNinPjU4Kh+N" +
                "Od0VV5TDTJJ6OiftD+4o31MVU1bHz7zGdLLwLjKdv49pTEdMzB" +
                "M15uLvesjX9wOigagW8ohlf/Qiui9Alsav+l6KsvN7Yq78vRRX" +
                "4PpyJjLLfQtVpbOMv7uKv5cK9wULYBwOlhB5gO11ilGrsMfBJ+" +
                "z2HRiwXs0zCyLWjdHdQeUwjRC5oAK57PzemBO1UOewXz+cRByW" +
                "2V9P/vx8/aiq0ou9m83NgLwlRF6+C60w8WrZUj5nCCI8NCg6o3" +
                "uFD6+7PjU4Kr8v5nRXcf/kdWaXdcr9wevu0/30Eu+l6gGrYPO9" +
                "Za6/nzL5fmYT/gGODbv0Uv9Bcfm+X1W5PVm4fl3inN8vrNcdS3" +
                "PifoK3CcYYWfdUfZMfYw6PKWZThIMPWcH6nFQtVt6UVBlz656p" +
                "QKqPiNuURI55NFaqoXTcefvKrqtIF+cQ4Rnv3WRuAuQtIfIadb" +
                "bCxYjWCOePUL7TdWuMiNU6o98XntZSZY0oIn9Uc3FXcf+6tzRH" +
                "+pPacVXRS7z9Zj8gZx23nwYozEMLv9/tF04wDoyQbLaEGvNEzf" +
                "UzT/JGHxdFWtN1U4RzY17+fc1ptbQ210916Hy4q7BP89JaehfE" +
                "c7/594pu+1v6d3087FL4bfxJ/gsQOtpvyicnRCxJP0vl70VqyU" +
                "/zoHze5g/QfUH8nM51MqQV9Gd1/pTujztwPW2h+wJhqZ70KXr0" +
                "3Ff/5E+Hp3Rvp123t6n7grVmbf0E2VqNEHmINcNxqQ/7HSwyok" +
                "ZcqsyxXAWHMGVEmvkzKZfqUb9SP+2a1rkrqkpnKbo4S9/EeG87" +
                "jCNkgSFEHGC7TDHbIxx8yArWMUHNc6ny9sBTlSNuvbQm2XjkB0" +
                "tcqnfEoyNJxBGZ/T75yq6rI1oXZ9X3duU1TAOQt4RwNCY2JppG" +
                "vlcYvSoZYimfZkJwX9BgjGo84L4g8OHOoE8Njsp/GnO6q7h/8q" +
                "SezvHft6ja+Yv99BJvF4yjZIEhRBzgiNkV4eBDTLCOCWqeS5V3" +
                "BZ6qHHXrpTXJxiO/usSlekc9OppEHJXZ/5v4yq6ro1oXZ9X3Lu" +
                "W1Tbv+P2Th9eoQeYg1w3GpDzrBIiNqxKXKHMtVcAhTRqSZv5Ny" +
                "qR71K/XTrmmdu6KqdJaii7P0TYz3FplFgLwlRJ69EK0w8WrZUj" +
                "5nCCLcWV3WiV53fWpwlP3NmNNqcf/kdVaVdcr9QV9X9dNLvHVm" +
                "XWMOWXgXcYg8xJpBT7DkESvWrmI1jnfvT5U6WAWHMGWEmY059r" +
                "dSLtWjfuVs4i55nbuyK+l89TnQLH1z7w5dZi4D5C0h8uzH0AoT" +
                "r5Yt5XOGIMLNTlknup761OAoOzXmtFrcP3lST+ek/cG90ax+eo" +
                "l3q7m1/p9k4fXqEHmI7SJhOC71QSdYZESNuFSZY7kKDmHKiDTt" +
                "4pRL9ZBDxPXTrnH2n3e+Mnalz4Fm6ZsY720z2+rHyEKeQ+Qh1g" +
                "zHpT7oBIuMqBGXKnMsV8EhTBmRJuxTwqV61K/UT7umde6KqtJZ" +
                "ii7O0jcx3lthVthbnV0HR47IebZhzAp4BRtm0IsRrRF2eit8H2" +
                "6NEbFVOvx9MMb2qwGdrMFs+5jmYjVibUHrthNH2LWSI/1Bbff9" +
                "nT6HWC/xNsM4SRYYQsQBtkOK2Rzh4ENWsI4Jap5LlTcHnqqcdO" +
                "ulNcnGwz5e4lK9kx6dTCJOyuz3yVd2XZ3Uujirvjcr7w5zR/1V" +
                "snAdOkQeYs1wXOqDTrDIiBpxqTLHchUcwpQRadofplyqR/1K/b" +
                "RrWueuqCqdpejiLH0T473bze2AnHXc7TTgWh8gjhmOk4ERks2W" +
                "UGNAct3rbqBKxz/bGNBMinBuDNgnNGeHdR9xba6f6vD56LPlvn" +
                "St9Jy9t9KsbMwgC1kOkYdYMxyX+qATLDKiRpxZiX9foHOZpyo4" +
                "hCkj0iympVyqR/1K/bRrWueuqCqdpejiLH0TQ16nlW1rzMxnt5" +
                "9uzBy1jZmt8Wx753OgMLMx0+23s/mszmBrvDET/14l89+ldq6g" +
                "Nbc+xX8vNRMzshbndQbCv9vMWsUPVqFK7V+gP1qA1gWdz8YxqG" +
                "sXQ8xB4do/Af9oeFI0mSq4b9JU39k5+QtcmWd6pkRV0bZf5+dP" +
                "ruP56i/Xrlf3d9fAOBQsIfIA242KUas6j7DXOgTWqwV1zersax" +
                "zrRoi8pgK57OJ7MafVpBvqIYk4JHN0zod8/aiq0ou9+TBeD5YQ" +
                "eYDtnYpRq5AbfMJODxiwXs0z8yNWZ893rBshcn4FctnF4zGn1a" +
                "Qb6iGJeF1mv0/+/Hz9qKrSi717zb3142Th/d8h8hBrhuNSH3SC" +
                "RUbUiEuVOZar4BCmjEiz+EHKpXrUr9RPu6Z17oqq0lmKLs7SNz" +
                "HeKwy8J5B1XEEDXruDxDHDcTIwQrLZEmoMSq57Pxis0vG/u23W" +
                "TIpwbgwWz6Rcqse1uX6qY+/S5xje6QZT3fScvbfP7GtcTBayHC" +
                "IPsWbMPrtFsOSBTrB2q1YjDtdiHeapCg5hyggzGxd3zk65VI/6" +
                "lfpxl7zOXVFVOkvRxVn65t4dus5cB8hbQuQ1mmyFixGtcTbl04" +
                "xrjIit0gn/ps1UOY1qNFsvay5Wi/vXvaU50p/UjquKXuKtMWvq" +
                "r5GF16tD5CG224XhuNQHnWCRETXiUmWO5So4hCkj0rQ7Ui7VQw" +
                "4R10+7xtk/L/CVsSt9DjRL38R4b4PZAMhZx22gAe8a9xDHDMel" +
                "fmrLau7vMK6s0lH3JxVrMsN9zeKUq+pD6tud1TpcKzx/ml0Vo7" +
                "W9t96sB+Ss49bTsLZ5D3HMcFzqp7as5v4/wt4qHbVPFWsywz7l" +
                "KVfVh9TH7qt0uFZ47vtkVYzW9t615lpA3hIir7kTrTDxatlSPm" +
                "cIIjx3T1knep7ZpwZHZadiTqvF/ZOH3ac65f5qte7X++kl3qCB" +
                "T3C2hHA0JjQmGPpsH+QhWDLEUj5nCCKMaqlOtE99anBU989jTq" +
                "vF/ZMn9XRO2h/ofq+fXuJtMVsAOeu4LTTgetpBHDMcl/qpLau5" +
                "62l7lY7ap4o1meF8vp1yVX1Ifey+SodrqeupIkZre69ruoCcdV" +
                "yXRuP0xumma/9OGI5L/dSW1dz1dHqVjtqnijWZ4VPl71Ouqg+p" +
                "L/V0jv+8U5WHvlauFZ+z9+42dwNy1nF304BPgn8njhmOS/3Uih" +
                "oqMB66qkpH7VPFmszw7/5wylX1IfW5dqrDtcI+XV0Vo7W9txvG" +
                "GFn392W7/RizP3ArwuyOcPAhK1jHeNZV2F2hvLuzUinw39N5pg" +
                "KNOTyWcjKktkNjScSYmsdCV/7v6YaWal2cVd+7xWtOa06D9yJn" +
                "3fds02jAlX6AOGY4LvVTW1Zzn78bqnTU30dVrMkM19NjKVfVh9" +
                "TvrK/W4Vrh/0+eWRWjtckzw2YYdsxZt3fDNOBedT1xzHBc6qe2" +
                "rOau73VVOup1V7EmM+zT4ylX1YfUx+6rdLhWeN2tqIrR2t5rGv" +
                "ztyltC5Nl/Ms3uhcLEq2VL+e7/KXqf493nypVlnei+oE8Njuq+" +
                "EXNaLe6fPPtMWcc0/fs4rvtnwHPP6qeXqP8/xLaUqA==");
            
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
            final int compressedBytes = 4418;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdW32QHFdxX4RFAiSBWEhyTAyYVFGkQlkE5wMBRb270YqAdb" +
                "JQKiEflsvJEVm6KFRMbIgCf8zs7mn39ig7VgLlCCtSJVFVqgL6" +
                "8MmJzR+BRLIignXyh2RAkmNjY9lEcnyWZcA2Dul+/Xq6+703J8" +
                "hNTU/373X/uqf37cyb3T037sZbLRckaWS1n3Lj7acEsaOppHg6" +
                "ao30Fb2Up6X+mnKwF1ajMc1m6ydL8umYuL5Wq7+riS+yJtwEaE" +
                "GSRlb3XjfRnRXEjqaS4umoNdJXfCvlMX1qyMFe3aMW02y2frIk" +
                "n46J64M+7W7ii6wPug+CFiRpZLVPohTEjqaS4jlCNNJXPJbymD" +
                "415GAvrEZjms3WT5bk0zFxfdCnmSa+yNrsNoPmpcc20wav4P2E" +
                "McJ+snUndDTLlA21yf05HtWnzJgcoZoHYizm49xkTc7keThX3a" +
                "eLcz6aO1hDNwTNS48NaYNX8BRhjLBfbMcyZUNtdEGOR/UpMybH" +
                "0QWdXozl6pD8WH3KI7nqPn0/56O5gzXlpkDz0mNTtMEreIwwRt" +
                "gvtmOZsvl6/inHo/qUGZMjvGrnYyxXh+TvPpjn4Vycd/CGnI/m" +
                "DtZ17jrQgiSNrHblruttE8SOppLi6Uha/43sDfrdwtP/OcHrPj" +
                "XkYK/+pRbTVdn6ycLqYx53XWeHrs/3qd3EF1mVq0Dz0mMVbTCf" +
                "vk4YI+wX27FM2bx2JMej+pQZkyP06Z0xlqtD8ncfyvNwrrpPR3" +
                "I+mjtY17prQQuSNNyK9xfvR1sQPSoRIimeI0QjHdliHjOfGnKw" +
                "V//XLKbZbP1kST4dE9cHffpmE1/Mbv/aZ8J6pbvibo33NuBIeZ" +
                "asTr/zzz21/um8Scd29tVR17O24u7eeOuCf9Wr2mc6l/U2pSP9" +
                "X+9tbI4rL/XZbqzzrsfqex+jmnoftd69P+39Wd2nk2D/seeY82" +
                "N/FHz+pHdDZ6Bex4vdxSzZxq1YWCwkjBH20xE2WnNoNj+fFqY8" +
                "Zj415GCv/nssptmkDt6x/pQnrQ/69EjOJ2Vvn8GOk6Q5QRvMgC" +
                "8RxuNylAiWdi7GbH4+fYljOa4csfM4n6Oeo9ssptlwPnEdPCb5" +
                "dExcX6s1tTDnY9nx6G5xt0DHvPS9u4U2OLO7CGOE/WI7limb79" +
                "NdOR41nzJjcoT59L4Yy9Uh+SWf5eFcdZ8+kPPR3MHa5OBqwJI0" +
                "sron3Sa/LtgkmNWspHg6ai2M3S88sC7YJD7aM83BXrAuMJiuyt" +
                "ZPVvdEyuM2hXWByj11VRNfZMEfaEGSRtboO1AKYkdTSfEcIRrp" +
                "I7elPKZPDTnYq3rCYprN1k8WVh/zpPW1WluubeKLrKvcVaAFSR" +
                "pZ3YdRCmJHUcLznYqmeI4QjfTJ4ymP6VNDDvbq32AxzWbrJ2vy" +
                "WMqT1gfz6fNNfJH1cfdx0IIkjazuaZSC2NFUUjxHjHxaYv18+m" +
                "zKY/rUkCNEfxrmk8E0m62f86c8ur66T0eb+CJrsVsMmpceW0wb" +
                "9OlJwhhhP9FgPplozTHyKYn1Z/o3KY/pU0OOEP0p6JPBNBtXID" +
                "vmT3l0fXWfjud8Una31W0FzUuPbaWtWFosdVu3vFUQxETXcSLR" +
                "g7w0W7EU1k9L4zjx0nlTDY9wHXkyxnJ18K5r1THhOh5q8n16Ic" +
                "2l2WvregdrZpak4VYsK5Y5v5pmBDHWJEIkHotl0JFlbHNsd5pR" +
                "He2uN/MpGou9+vssptls/WTFtfIYe2Olvk8vN/FF1k63EzQvPb" +
                "aTtm63WE4YI4D9teg6TmT3s24nxlk2vx5fHseJl86SaniEdfN7" +
                "YyxXB+9cRcrDuYrlpA8vz/lobrI6J2Qd6++/9R2zGtFrbNQ6a/" +
                "VKVv/R813nX5ufwEa2zf9kJ7ydy2IE7panOr9pEb3+5uc7/Td5" +
                "MubI/w2vaPbRI26b28aSbdyKK4srCWMEMdFxg+u4imYP8tJsfj" +
                "6pWOG0dcRjcoT59JEYi/nIT1eS4+FcxZXh9Xsp56O5g3WTuwk0" +
                "Lz12E23F4mIxYYwgJrqOE4ke5KXZCrhr6FjhVH3KjMkR+vT7MZ" +
                "arg3ddq+XhXEW44w2X5Xw0N1nzPAdvhHfa3/1oz8HyxAzriZvY" +
                "7vyDeg7eKPGdnf+P5+CHfszn4I2552C636HVDZ/BDHs/4nPwdr" +
                "cdOual79122uDMJtx2/3y3XTDRdZyVwjb4DPsRG2+DoTDU8ynJ" +
                "wRpxDqZjLFeH5Jd8OiasC1Tm4S1pLnvOwdrgNoAWJGm4FUuKJW" +
                "7D4POCIMaaRIjEY7EE3mFL2OZYQWkb3M4sqk+GWWvhc8Nxiwkb" +
                "R3P9ZMW10jH0KdTk+/QvTXzWKv+9PKCv7dWbyvtgHj8ElZ0vv+" +
                "7n9Al4jzw++aHySfw8E/Rz4PWW8vwkPPuUL5Q/BOsiiq9+InD8" +
                "VHXxCH1r9xv1/e5znuvR8lvlY3D8dvkU+F2u33XVguqVPvo1k2" +
                "PVz1Rw/aiWlF8p/81Hb66eCJ9a3V8+SBHlN2B/GLzeDMdLy/8O" +
                "n2x+P+TbXL0avao3ePQ/yq+BPJreM6cvYqScK5+Fij+gx6tX+I" +
                "p+unqdu9XdCh3z0vfuVtqKS4pL3K3d7wqCmOg6TiR6kJdmKy6B" +
                "+XRJHCdeOm+q4RFete/FWK4O3nWtOibMp1AT/k2/Ns2l2Wur67" +
                "qgeemxLm1Q2QuuO/iiIOwX27EkbfJRYfPr6VmJG3xBGOo+JTkk" +
                "2lczHmO5OlT+kzme0CeVeXplmsuec7B2OIgl6bEdtEFlLxLGCP" +
                "vFdixTNn8d/1yOR/UpMyZHuI7fFWO5OiS/5LM8nMtmjn00N1nx" +
                "Clvujt2X9OfjtOP3LXoNcKHPx9X6+CX9eXm6LrCri3RdMLg7/e" +
                "xdMtK6QNcw+WJznQ3f2czF453vqFXC2fwGXgvaZ3vbLBb7pFHt" +
                "s7B+ChqPA9crtywQr8FQPMLZnW3eyG8wPV/mzit0NpSwfop88d" +
                "jZwSiunyh/zGfrYYvud+XB8p7Q1dnyvvIBf7/7Ad/vYH+8fILu" +
                "d+X/lOfK50A7X35X3+/8fUHd7wLb4fI/y3v956sXlY/E9zvzar" +
                "6M97vqVXi/q14b3+9ard415VfLI+AH97vyOByPld8oT5UPl//l" +
                "R38P73fl03S/qxZWP7lionp19bPV6/F+Vx6S+53P9c3ypMp8uv" +
                "wOyDN0vyuf99j3yhfLH1Qtdb+72d0M70Av/XvxZtpardG3E8YI" +
                "+8V2LFM2r92b41FXicyYHOFV+98Yy9Uh+bH6HA/nspljH80drP" +
                "VuPWhBkkZW+zhKQexoKimeI0QLY7Mpj1lnNuRgL6xGY5rN1k8W" +
                "+ccxcX06NuaLrHVuHWhBkkZWD/TBIUHsaCopno5aC2P3pTymTw" +
                "052AvWBQbTbLZ+sro/THncurAuMLmb+CJrpVsJWpCkkTX6Nrdy" +
                "cFgQO5pKiqej1sLYUeGB9dNK8dGeaQ72gj4ZTFdl6ycLq4953M" +
                "rQJ5O7iS+ypt00aF56bJo2mE+XuWn/fDctmOg6zsqUzWvHczyq" +
                "2syYHH2fIixXh+TvnsjxhD4lmW0ue87BGnGwjmFJGlntR51f4T" +
                "BiR1NJ8RwhWhh7MOUx86khB3sNHrOYZrP1k4XVxzxpfTo25ovZ" +
                "4Znio/3x9u72mf4f9P+wqmdke7LWdvOKDZ6Dd+c/90W88+XmVZ" +
                "x7YN5PfXcLL3/ua/+mnPVCja327vRz3/Zk57x46Eyxzgj9rqfJ" +
                "t15F3DPyMb9WqO+Y1V/CNeo2f23wqwgcx/VTvk/VJ+Gd+nK1tb" +
                "FPx/rP1vkf8ayvUb97+ovyZelT/3waP3pbf648Ig+E5THYT4Xo" +
                "T1Dt+m/LQj/yej8PnheWSn2G2sG131+Vp5v6VK23Twu9YW9Le3" +
                "/83NL7xfaZqX/0yH55bmE9fh7A+PmeW/pH0+cW9R3FfuSFSvpN" +
                "zy3dcfEKEXVd7f3pc0vv7VRVrk7JKjL33KKji3PFOa/fQWNk4z" +
                "66uDg3tR/19h3FuckPIQZ9uoPGAblKe2M8MTFqP3/q38cjwDpD" +
                "R/bCaOQlG5jHmFd8fJ+UF2pci6BSA1ZPXvas+DzlnBkp53DcfP" +
                "6kot3V7urBE/C+uBquld/GI2q4jy5yV099lXTe8TmYdJTaW+s8" +
                "aubT/ezDHrGXcA5Oay/i7I5bL1uXRX2fFjFbXGfzc3A8rs/SrX" +
                "FrvL4mjHkb99473Jqpr5HOu+/TGvbqToi3jmRO06dTeH3CEWCd" +
                "oaP1kkr655lLOLvj/TntZeuyqH/f/ZKcV/95Xec8fYrGpSLYVr" +
                "vVeL9zq4EP7ndowwjso0tQks6775PXeYxHtM6j5nvOZ9iHPWIv" +
                "4RQu4Zyas162Lov6+aSqt3XO06doXJ9lMVfM4fuuvY/ed2ijBO" +
                "RE4Wsr5tr7GIPrU9B5rPbeR0dBo/vVAR7heO3V3oe8zDk4rb3C" +
                "uuBZ64Ua1yKo1DB6gKrK1SlZRfrrU1S1ji6eLp4GzUuPPU0bzN" +
                "wrCGOE/WI7limbv09fluORmnJjcoQ+nYuxXB2Sf8vP53k4l80c" +
                "+2hustyYG/NzayzMNW/j3rvSjfnf9YzJ7t93Y+ylvbXOo2Y+3e" +
                "OvT2PsxZqa5TUnXJ+UF3Fu+YVwfRqTSvWuUcon5wXXp8Cz5c3z" +
                "vu+iqvVZFs8Vz/n1+J10fUIbOgj76I0oUW/fyRi874LOYzyC8Y" +
                "TJqLk+PcsjHK+92nciL3MKr/gMIy/UcO9eREfJHfp0I1WVq1Oy" +
                "ivTvu6hqHe1WuVV+XbAqrAtWoYZ771dRks67n0+r2Et7a51H9d" +
                "9gKfuwR+wlnLAuUF7EOXyr9bJ1WdTf735Fqrd1zjOfonF9lsUz" +
                "BdyJSPrePUMbZFpOGCPsF9uxTNl8n67I8airRGZMjtCnX46xXB" +
                "2Sv/fuPA/nspljH81NllvkFkHHvPS9W0QbzNyVhDHCfjrCRmsO" +
                "zeb7tCzlMa9dQw72Gr7LYppN6uDdLcLqY560Ph2rfVL24nBxGD" +
                "rmpe/dYdpgPr2PMEbYL7ZjmbL5Pr0nx6Ne1cyYHPH/NmIsV4fk" +
                "7703z8O5bObYR3OTVT9XzsTPwaOX8+962jPqFy8z8r1Yd0I9X8" +
                "4ovf5dj7o+3SDPwenveiB6pvn7O9w7fx95zXBdqKfPwVi9rpZ/" +
                "18PnKecsSPIcrKKLAwWsAEn63h2gDebT2uJA54AghdFx607oaJ" +
                "Ypm+/TJySus1MY61c1ySHRuHcOxlhcF+cmq/fhHA99nplmtrns" +
                "OZPlfwO1C3q2F6XbxZ13u0bfhdIjexlDP3nVxZtw8mAUevPJcA" +
                "XYRXv6fTDy+xx7KT43nygy5A5ePuJMXctenE+cgY5YPVXlIz2P" +
                "3/dyTagTa+N82qvm08HiIHTMS9+7g7RBpncWB6f6grBfbMcyZf" +
                "M9+3OJG35EGOpXNckh0bh3x2MsV4fkx+pTnjCfksw2lz1nsupX" +
                "cU9yfVpbd3OPuj7tyX9vj3h8fTLr8bXz/b4AovfMf33CeOO1h+" +
                "tCPXN9WstV6ToB3aOz6jPPzCcVXRwqDkHHvPS9O0QbXJ9+uziE" +
                "vztkhP1kg+uTimYpbMNrOQ7m0w6JG14jjPWrmuRgjTiH62Isro" +
                "tzk9X7rRxPmE9JZpvLnnOwZotZvXonG/fRVcWs79MsrN8D5tfy" +
                "s+RnvRFnnTmH6j+IB3ewfzE73EBH/YTA8ZqBvXAfbrReqHEtgn" +
                "JdVL2ulnjgftf43CKx6rlFRfP3CPHcG12d+9V5/nuE/Lw177vV" +
                "8//eXb5HyPP2fhd8jrSy3yPI7+zj6ul7BPUf1L+TVsnfI2T+q3" +
                "t9c03F39ZPrl8G3m25EXhifrf/zHqiKTb9G3xF/Q/ANa0f6w95" +
                "h+su5JFWn/leaMf8VUafwS5PsRVvDJlur1/Fx6Nabo+RpNrIQ1" +
                "uDgynOOS/Yp9vTamzlXHVz9YKKz4X+TH3/B68G41A=");
            
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
            final int compressedBytes = 3245;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W3uMXUUZv1DKthXaul1qH8HoP+IjRlSCtVHTu/eeBJSkTW" +
                "l19Y/GUlQoIEmhNDwSZu49925315KyxKbpgxIJDyNtQsQWKFFb" +
                "WSpCARHpWlhpwW2Fxca+LK+KM/Pd78zrm/PYFs/mzJnv/TvfnZ" +
                "kzM+dsZVVlValUUaU8ZA2o8g7gIaeyKl6h6xXDTpfxbaa3+FbU" +
                "E942Gn5u1h7wsD1rj3AVvm5xeTQOvEr0vp/4Jk2Zke1YpveE6q" +
                "/0i5oqFa8f/gQyVumP12kO6rm0W/reVJ7u1na9V2kPCdp+909b" +
                "y7P3apdH48BrfDvlp7bJxKQj27Hsezbvp1SqzmhZbURO+emSdW" +
                "hJ6EjTKK/3eRgzj9/eO0NS6QUjh9EXuQ8KX+WOyh1YIg1U3Ac8" +
                "5KCeS7ul703hvofyY+OgvKPP+Gcuj8aB1+YFtB8Tk5sBVwf1TP" +
                "3qdLhGI8iJ3rSzqyWhI03D9WbGzHNQ9toLm+EiaC4snfJB4atO" +
                "a2V3LXLiNaVS57NG3tdm9o8UDdOTGzOPX8pee8HIGgGtL7nZ95" +
                "EHH/9k4nO1yNU6QzKLbjGax9ukDd26TEnRg39dIFmf1oaxPfHZ" +
                "6fHkOM6/NhoMldWV1VgiDVTnXcBDDuq5tFmCjetN4b6L8mPjoL" +
                "zDVYzj/S6PxmEiofyYmNwMuDqol9zDBiw7N2Ar6NwANMjgl9N1" +
                "1HF/W7DR3rSeqavr0r8Zg+wrG6RfW8e1kO1J8iR6Hd/WlVfZnu" +
                "AO/fvL6INTq1PhatLyjB/QEjy1Jpa2JH7Q9GnNCzahBCywRiMx" +
                "tUDau8bWsnHZXDW63h/GGc5FKDfib0p1iqipUvGmwJ+ItKU6pa" +
                "dbc1DPpd3S96bytEHb9b6sPSSYvBjaWp71JS6PwqHj1/dSfmqb" +
                "MJYd2Y5l37Or3+qtjyfPu1/b47iWBMc6R6P3VaOn1A3+3vw+Ep" +
                "tXikTW8WwJ5Ol0HJVHkzx/2cnTo/ltW/d2UNeb8wz+cH4fic2B" +
                "IpFFvLmU5DTmaVvyi1zs5Glbflv/WPlyzva0bVTtybOS6H3Jac" +
                "zT9qTfbXPytD2/LZGnfTnb0/ZRtSfPKt5KSU5fnvTssHNW1qqk" +
                "vjT/uqXnE0XjO/ed2uuZt6JG9NmrrZzt56HKQ1giDVTnbOAhB/" +
                "Vc2i19bypPP6T82Dgo7+hTorF5FA4dH/R9P/qe/QxoHdO3q9+y" +
                "eiz5Rb4RkgRznqLR8+Ocv1vAR19XMStEn416lO1rS9LSRc+W8y" +
                "dfkm1L5Okq456XFfdRX1Iscvw4JTl945NeB8c77Tzxy7JGFN4W" +
                "HmmqxjOub0VK/O8Uy5O9Dvbj8Y6P6Hn3RJKnP4Qk2bZEe7q+aH" +
                "yn3zWKWdX35kWdE9dQZQhLpIGKB4CHHNRzabesL3e9qXnf5ZQf" +
                "G4cr01eRp6bLo3DgWRmqXk37wVj1G/0MaB3TN1DRiGy5UEI7hj" +
                "+Rp6eiEdnvUK6v2gJLu6e53lSefoC20Ujfelmyst1/6Bjoqb7E" +
                "5mlv0O8QB8qac30/0Qj0O+1VU1rHRKMptpOJ/sWeZAOtmchz7A" +
                "X2IhOz53gX2yPzxEQLZm+wYS5WHuxtdogdYUdF7Rj7jyjfZR+K" +
                "MeAse3zi5/D2lrc/sj8xtbPYvJa9xueyfWw/e71vE/sH+6czAz" +
                "rJz+Rj+NnCegL/GJ/IzxO1qez3bAfmiT3Ndgu9P7O/sL+K60ts" +
                "kL3KhtjfIU/sLVH+S5zvCLuxfFzzGj6ef5xPluMTe4rtYs+I6/" +
                "OQJ/Y3Zszu2QH2pihH2L/ZYXE9rngn2HvsA17iZ6j7OZdPmvPW" +
                "HBEBS6gBFT8jS82xpX4J9miha1CvXuf7MfMUioFafQ/YPNObjR" +
                "8oHc+0cfGZtq4/m4rf0e3Ved7tdt4rWP0zfo+YM59sXT8gxvEb" +
                "LE3RCuP36adj/C71vIyfszlmP6Oed/Gz8Qkpjf+b8XxfG56xu5" +
                "I5x+BUdvoN2HKUmVqaDueJfN7dmI7X92tz+h60OYgZeL41os/M" +
                "0z3hO3IlVnv6RaL1cFp7Knr03G7EfzjPus7m9P2yWHty0QfzdG" +
                "+B9nQcTrvfNcaizNTSdME83ZnRno6nc/o22xzEDDzfmt9H8z29" +
                "+8M6poTvc+ySPUj+Wp6VPN+fcx/+WsL2leI7B3wo334BFe9Uj/" +
                "JEOBWO5L1reRnKTC1NF5zPPqLrjQCGIhzEDDxCdxnNzxM5JClP" +
                "glPl6e3kbs5Emaml6YL9bl0G2knFOIgZeL5u4wyanycyLeG/5Y" +
                "v5FjbAt9rjE1c7Y/wKcf5O1cRKlF/JDqn6LlX+RpXOG2pOfCnC" +
                "H2velo6Wec9K/hOLEuMy223ovyROMUZwtQ/Bn/T2x29V/MmOz2" +
                "QXmA/oeWZw7HL2guYcgVPJDiW/yESUmVqaLnY0784Yx48U4yBm" +
                "4Pm6zY0038vF4bCOKQmv7xrtuLrJXt+hZlwJru/WuH6Kre/8dV" +
                "jG+u7n1PrOXX+KOV813/quMlwZFqOsKtV4Owx/Ik8dwEMO6rm0" +
                "WUbTKG8q1jTKjzHOEzJ9RQ2TR+HAE5DQfkx8iIuKpRHKa3gd3J" +
                "jJ9igOroOXhtfB/BpYB0cdoXVw1MHEPAPWweJaeB0sNAqtg6MO" +
                "ah3c8mSsgwWuXOtgfycE217jU26/sPdR7Jl6qzbDnQMlbXdG+t" +
                "zY9ElruhhNLDB/MvudRKL7TcgT4KJGELvP+zh5sr5tfCH/PWSv" +
                "EDq70rOUZz1kj1bmeEO8l+rKXg3JOn8//7pF8eZDacwLTroyXQ" +
                "btJwRjTkjNwPyQ35CWrCFFWUM8gj/frkcTwpEt3XnRPF1CDajG" +
                "l6J5qIE8u2aX6jpd+9S2qjbd92NhCsTQWjbP9Gbjb8mm+35MVI" +
                "nX6SF/rveW/kIoa8k3iY0LXZku/ffBUcZ3o+V9qS1lYZTju1NT" +
                "S9aQoqwhns83ObJemxmOTEmiBVAa49NFrkyXIfuUPL2emoEFWf" +
                "aulqwhRVlDPJ9vcmS99ulwZMJ6xJ6nqTx9cxTPu/bg8679//y8" +
                "a8/5vGt3n3T088590ujo5c+Y83F5Nubb91Jfmv6+pWG0254ndP" +
                "zG5f583Ebqz8cbC8IzGJyPmxhM9NpfY16xnRxtXW2rtpVKUKp9" +
                "mzb4E9JB4CGn2iY55h/a2aX2hvoqTzsoP8Z+keXTrIFPsDF5FA" +
                "4d34yhr9EgxrIj27FM7wk1sWrtsgAtz8b3QII06mlufanW1hL0" +
                "4eyr7EQJWPg6yEF5upaLycXQ+K72Zt5V6l7iRJojrWHdIn6vLt" +
                "z3ZS+IdizWLeXzcd0SdbE3hKQJekytopnaY9fv76S9//7OyJPa" +
                "9wytW6KuqEuuW5S1mPlQ6xbQkuuWFo5BcQ4l/NabJrluUf3ufD" +
                "5eSuD7Ar1uibpMf+adC/lhp9d1uesWRU1z9se/b1vVfsX3ZD6R" +
                "ZgZ2LwZDkrwHfzF192pGXiSj1Uueo83ydXBN8rQIaeC5ctc+y3" +
                "9G9Ax7PmxraVwaux/P92pywEc4sivhi8UvMuC9b1mM+5ktyRJ+" +
                "AvczC7eGwfL+U9zPPJa2n+m3J4jn7mc6e8NXFNvPVH677Wup1P" +
                "yqOC+SNPDgGVnuDuS+O6M9dadLfbn9lFbtydDSuOSVyFM3HdXk" +
                "gI8wsjTMRnv6aVK7HtpT7ZxcbYfYH6+d23OwcBu02lNtXJpubb" +
                "x3j49keB/Ip+e1e2VXH6vzVKlgydU7gPrZMk9mvwONVtwVdp5M" +
                "mcyTTTvvrCr8FnYy9a1WpTZZ9jt+M9LY71TEm/w8QTyz37U4y3" +
                "We+A3KU6F+l9xR8p8ctY7T94awdl753tJHeNS8eUg553d05YLf" +
                "25V77KsYmxYjDbzypbY8sUzlu/5D0bPs1fhkaCEuqSX4l9L3Q3" +
                "jtsetU5BDmOUfhVHiS/xiODqLM1NI0+bteENzPHJPxXupoGqdz" +
                "jERjchAz8Hzr8laa7+uFdUxJ2emdmvYkl5BxDtB8V7/zLMo29Q" +
                "4uoaxpVD6GdN/F9bwW8fnkKXel4zHwxXI+fvW+okhs+8aP0rR8" +
                "DHnj1b5YENVK+yrGp8+K83OSBl45suX2PCfEd/2HovtyiJfsnT" +
                "5vayEutQO00tbV8QivK+06FZnSDX/XU7vQsfo2+ezcT/Nd/Sbx" +
                "fzzp3/UE4g1RWr5uc1vO9vSVnO1o2N7xKg+bkuzv6crD+eL0fE" +
                "jHztojczlok/U9nYs+fP/F3ksl+U12xWsX57fKPnqnFLUoEo/I" +
                "0+ac7WRzQVSLodTz8ep2V6bL1pix1LVPydOs9OhZ9q6WrCFFWQ" +
                "N6n29yZL32rXBkShItglLPn5rjXJkuQ/YpeZqdmoFFWfaulqwh" +
                "RVk322hUJkfWa53hyJbkf0qdSxs=");
            
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
            final int compressedBytes = 3163;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9XGusVFcVnqLQ0kLRK2LSH/640X8YHzHB8MPMmTODr6YkVk" +
                "FsDSjEFCRR/zT6Q3vmXDon3mullFf0D4kJMbEQUlt6Aa2xPHp5" +
                "KdpiHz6wD4vopYSLFi3W1L33OmvW2muvfWbu5cKcnHXW+tZa31" +
                "pn3zP7nNkzUKvVammtVsq01vx4rbSsff8J8NW6EbUKGzmkL1Vj" +
                "sUoa8fnZ2Jde2XpTz5cGsWlQudazdvhq3Qmy/YNu7Sekj2QsP/" +
                "9UjH+4Xl09xhuLshpaWjZ0H+IcsXr+6XhlzdNaDTK/vVvpl9JH" +
                "MpYffw0vrRyB1b3yZZTV0NKyofsQ54jV8zvilTVPMuIfa7XOar" +
                "QBS5r5Z7m/yzbu5/m45NdfyUjoT5rcyu/0o7AvW8XgTf18FNYR" +
                "X9cqV/XcWgkyX9Ydp+9JH0l4Da2T+RXX09crr5SVvfJllNXQ0r" +
                "Lzz+ldccTq+V3xylU95V1fZ1iM7icio94XPvzNSU6YIj9fVRUV" +
                "9pB/qb8q+ZdrU3q131veD+al88xY3eJxzumL4ZjSzVzLdjWvfE" +
                "2ld3Zwl+xRr31k8j00LqJsXMRxalwkFOcb0nmeRGSe1fO1YSxW" +
                "wUqx+Q04869VVc5uI0zW53i+g1ems5zC9XSlnHuG0sXSN7SldU" +
                "o5H4YNbTV/ycUhTuh0v6BK2FeveujP7+2/Vn0nyvpOfH6q78y/" +
                "bSX4eFzcRg5X/ztmTvqGRMN42PrrLl7ZXk+IlfW/JWP9LqBqde" +
                "VgLpwFu+PPy/EeTAfN/LQccRuRDiazyFb/SoOw94OGPVQh+QYf" +
                "wZ4BC7OhXrxb7CfvxGOkJ5kJO5/HDcP30cejyJ7ca/i+HuM0c3" +
                "II9gyYEvszHQ/j4jGaJxn2j+ZaGkE7GQ7jYvnRmsPV3l757Vf9" +
                "KN5XMmzfd1q9kFWeS1VlzdNaBRLncXMvuFf6SMbyK66nxypn41" +
                "W98mWU1dDSsqH7EOeI1fMH45Wresof6mqbp++ulG8Zfrx2DV/5" +
                "puBaGO1zbh7tv0q6EGW6EO936UJC/bi4jRxaXhiLVbBSVXfpwn" +
                "xbVWX7vkNM1vdxXpnOchLPIV8Bmf+kOz/9SPpIxvLjr+aPq6v3" +
                "ypdRVkNLy+7s17viiNXzn8Yrc0+yPlnf2I4SNLCsbt7nXYR7eR" +
                "7owNXYjhlWAw9HeTaww4YI+UiDbKwiO8HKgEEPfgRq4Kdo4OXn" +
                "4PP5VjphtkXuSlxkd2ubo8GsDoi17T60xcWBf4JHl9fyhHseX0" +
                "Sc7DqHGhNdftgWwYY+wT+BTMSGlTGO4rss5dkgG8S6WhO8n/J8" +
                "WZ/czxGXfSG90LgM0oyv08Cy+v3nCME4aRuerrQIsQEmmTEWq9" +
                "iNkFADTl5FdkK1rYb1Zdfgx66gKpwl8doj9Q0IVi7fiyvKO8dH" +
                "u/eQ3dInde+9vKLHc8HByplnRa98GWU1tLTs5FG9K3kuyaPxyt" +
                "yTHk+Po0QbrOaHAEME42gbWsezUYZsbpwOaTx+H9JHR79T2Ylf" +
                "G6xkV4yH92fGaVesFnXotBPpCaM56bATsJk7xuuAIYJxtJlxYt" +
                "koQzY3Tk9rPGycFB8dMYJjkg9rg5Xsi/Hw/sw47YvVog6ddjI9" +
                "aTQnHXYStlqteA9giGAcbWacWDbKkM1qIzM0HjZOio+OGMExyY" +
                "e1wUoOxHh4f2acDsRqUYf22Br31/vBtns6DB60MY7QoXUUjZm+" +
                "Rq+RG9EDGa3xLJHrcrweSsnEq1BP2W1+d+b89xAbP6tgHtsT++" +
                "bDz84OZof8dd/st6bus+Z6+mD2nOvhD2Z/xXjKJ9fskpP/cvKN" +
                "7C3jebvL/ED7xpJjTnsgGKebXPyL2UvZy+b41+xc8El3Rvtt7n" +
                "iz2W9tv9vIBdmvsifFk/fvsmdK7Xmz/xnHKftHqf2nPP+9bbcW" +
                "3J7v0LHMvIMyZZUx2cu4g+eC9g1Ozm3PS4+lx8y146S7xo7BZs" +
                "bpw4AhgnHSljJkc+M0W+Nh7zvFR0eM4JjWB9VPNsR4eH9mnDbE" +
                "alGHTjuSHjGakw47ApsZp48AhgjGSVvKkM19fp+p8bBxUnx0bM" +
                "zkVWQnfm2wkv0aD9XqjtN+LYZzl9ZYOmY0Jx02BpsZpzpgiGCc" +
                "tKUM2dz1NE/jYeOk+OiIERzT+qD6ySMxHt6fGadHYrWoQ6edSv" +
                "31fmfbvWiBB22MI9Tc706Rh3TJ6cbpfRiDETIGEWKg6DBK9uR3" +
                "Z87/YWLjvQXz08OcO/jccspnZys59Dy+t3LF57HJrUeMvP9arj" +
                "+F65mdA/1ldn4+xRUvGqd903kmI8n1HadkRp/rdH3GpZvTzSjR" +
                "Bis5CBgiGCdtKUM2N053aDx+Hxo7cvIqshPNZ7uP88jKegzGOW" +
                "1jutFoTjpsI2xmfloCGCIYJ20pQzY3Tks1HtbtRrlRNvXBMb0P" +
                "PNbfquKhyhCnx2Cc32nZ7w9RKz4Z80SvzYqIkc9P/V3Vf2WKTP" +
                "pk63c6SDelm1CiDVbxmXRTZ5AQjJO2lCGbe346rvH4fWjsyNk5" +
                "JDG9DzxSPY7b3xdolf1a/jnz89E+4xS3xzzR9ZqKiM6R6zGPUw" +
                "edw9NbId2WbkOJNljFCsAQwThpSxmyuevptMbj96GxIyevIjvR" +
                "fFRP45GV9RiM4/GtZfJ78+Iu81cZJF/7YzwuuJYMjp+DtVfjuc" +
                "orcVmMt3t9HPKjrIYW4e3Ffj3Jmu/gCOjxytyTbk23okQbrOIe" +
                "wBDBOGlLSWzFWowz53lU4/H70NiRk1eRnWi+zlgVj6ysx2Acj2" +
                "8tD54zfyF9UvfGfnmP6+Fk5fW0vFd+/oQfZTW0tOzOUr2r8Fzi" +
                "lat6YuP0pKl2fLrmwcazV5ffOTG553G9Hv2ebtLz+JZ0C0q0wW" +
                "oY2XmFEIyTNpcNlc31vUXj8fvQ2OHodyo70XyNSh5ZWY/BOD++" +
                "HPGnutrYtH6+23V9P9811k5vBVxvbs6X6+PF+ta4vd/1uz7Oj8" +
                "r6+Gi4Po41+1kf7/wttj5uWcL18cY6bX28+n0nu27Op+zmYNOM" +
                "BUjnG4TNjNN3AUME46QtZcjmxumwxsN6Unx0xAiOaX1Q/cY9MR" +
                "7en8Yrz7m0FjQXOH1BmeVsuxf3kQd3ikQZehAV19MR9EAGaqzf" +
                "BbweRYm/8QJejXbZQ+Or8T6jv6mJ1HJ9DDQHjOakwwZgM+M0DB" +
                "giGEfb0DqejTJkc+M0pvGwnhQfHTGCY5IPa4PVORfj4f1pvPKc" +
                "ZXw5e/+mq52awmz38uQ9/b023F3p/eJ01wueC3anuxs7QJqr1W" +
                "lgWR0kYaSTDRzEFLL5zJzH8tsNsv0NMd4Hx2SkxayG9WU14KFz" +
                "hq58XnvECETw/MS4db8fLR6IeaJjXhHRudTn323PlP7aQVZn4m" +
                "r49Ff9Muziefw0+ngU2f6/K+v5/HS0dw+TQbBnwMLYzhUd76ey" +
                "5knPpGdQog1W8SBgiGCctKUM2dxc+AWNx+9D+ujodyo78WuDRf" +
                "UkD+9P45XnLOOD6+mF6ZwHixuu7/N4Mc0V6ufr50mCBlbxkJWE" +
                "+N5QQj5xUq7re1bII/vQalCUj3E2v3+wipkhT9if5OV8wrpQv2" +
                "C0UoIGVrG17n6ViIjvDSXkYwZpoDfTkMcbp0gNivIxzub3DxbV" +
                "4zmyP8nL+YQ1XjefYVCCBlaxve4+3SDie0MJ+ZhBGujFzSGPN0" +
                "6RGhTlY5zN7x+sYnbIE/YneTmfZBfz02vdd/juXu/ZSd3vjl3f" +
                "+Skf7y8zn/S/50zcvyqkf0edX9L8Ux2n5sprOU5hb9NdLz2bnk" +
                "WJNljF44AhgnHSljJkc1fnXI3H70P66Oh3Kjvxa4NVzInx8P40" +
                "XnnOMj54Lrjc599yST94c9U1upKW6D1cm3qtNa01VtJuroDTWg" +
                "T5W2vIV8Vbst3aWlPdgZR6j+hHZpmHvoJlSD5iCs8MM6hzbS2R" +
                "r1wWL5r9JT/C/31v7++BaUWysbf6+2R/9TFYxd0b9sh7gXmcVl" +
                "5b4zwj/pvh8MxoBVNblc0OZ+X32rn5XJQ97WI3wV5GvJr/18jz" +
                "mXmiyP7pkMs4j8PvfdX70NHseOa+kWqMZn+pvGP9zzG5f7ncvi" +
                "UYp9ENd2fHsl+Xsb83++ns+exP5niG1lWy1+D3vu2Z7Zsao+b4" +
                "zvY7HP6UqPVC9sfuXX0gO5v93WDjmf3/Dd7IXncR/86uZG+25X" +
                "t8AHY3Tm92x/kBuycDflQyMMXvEZ7pMc8MVCHFfNsNR7BnwMLs" +
                "4l063k9l1fN/mQPeOA==");
            
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
            final int compressedBytes = 2778;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNW3uMHVUZvwFcqe3a7payFZSIsTRAUMFWrS927p2RYrstW8" +
                "UHMfJfmzZRQ+Ia0ix07j1D5y7b3TYEVHxVHooCwZgA3cVE/aN0" +
                "24b+I8UUJfQva2Ap1gesFmqcOWe+c77znJl7ZzfO5nxzvvdvvn" +
                "vOmTNz79Zq7AgOMBo8nglq8as1RSeoftjkcNSnXNok74F8b2yV" +
                "9oAzeYOHC6f7isya4GlGyXkgaZ+v6hoh9G3+qUXa5MMslb1tcW" +
                "1WaQ84kzfLp8tBAtfSCO2ZTZpghtHg97xOF6o6QW3+9iN+n7MC" +
                "M3n+qlXaA87kHb/XjApL3Fek2A4Hw4KyHuPai1IqJMEwuVdodZ" +
                "qeyX3JJzQCPPjSz29ExMFyjAPrdCtZhqPJ+Bkn8mEfEakxko2r" +
                "EVs8NTrtzQazKRUtqdNS6ToSi8adwg6o3GvcyRr2Sv1SqbAyfn" +
                "azKtUtQM4wYCzhJUIOWIWHGg/0DDHEkRFAfPkID4bP1mokmWmt" +
                "swn3B1qnfsniL2RR4/bwtfD1pP9PKnkTdM0LeJ1uZ417HQ6Phs" +
                "8xTXjSVafwHI3UQ+lio8WR8FjWeyFpx8MT4UvJ+WVRp/B00v6d" +
                "+L+jeWGKotnXXEblh5RIL4Z/BpQJrlPhK4lsNjxDdW9QOheeDd" +
                "9uIh+/x+8BCjzj2quYDCRgp/IqJSNqNIpniymOjEPVibOMVEUi" +
                "dND8HpFPjcN48u1sHbvclksgpL2lvjS/GJ+29lVMA7y/tLETeE" +
                "FBy7jUorFTjUnGUynYMw/VBmJBrjwrgUuX0s9lpx0n08tn7KtK" +
                "mHfzt0l7Imn0Lkh6+f1uM59Xv1P9m/RO0HyK0ucU3RF9zjSng+" +
                "drXR3NXzu1B7XVzJivyXdEzWc7wxEcYrR5Ga/T51Vd4w7o2/xT" +
                "i7QpexmjVPa2xeVoNshWaQ84kzfLp8tBAtfSuMOeGWsaJxsngQ" +
                "LPuPYwk4E++RsVWply39GEjmrRuBR7N6R1XdXpVrIMR8M4uG5U" +
                "jyOhGs36oyYbU3S0jgwk41GMpy+p9W0tKTRD6Lwjy1Dci1u98d" +
                "XdzTvS79K2Fmn7zKtyUBacd2Q5uQh58fWpdSuv007N59LOrzOu" +
                "d1en3c+XW59irzavR/37uCe4st4uTdmo+T5s/5Rv30nmzHOgPi" +
                "Ao6zGu3a4PxFcISX2gsUtodZqeG7uS2bwLePAVUuwNNthSRoCt" +
                "4tWyDEeT8TNOxcrOrf1gnWKia88uWzw1ujK33s/n3ViC7hqh8b" +
                "YFp0yV9rahO8Qp1mQ5lhY/cNwEyYdcVrIt5DSsY/tVfVFcZE3S" +
                "Pk57dAWBdZxsbE/S85BpHSdfoXQHirOZfJSeP5at49eSdVz3yV" +
                "avf54x+w1kExkmXyT8nkG+zHvXZefPEg/WcXJ9JmugGAHZAOs4" +
                "uSlpX6B7Q56PrM3On6D00+RTbB0nnyGDSf0bGqb15EbEbSE343" +
                "Xcm2TruDfJx9ME8EIm93GdsNz4uU+6tXn+6TqOrTCuBPtBcz49" +
                "qnotrswuTH6g9zKvjZZoG00R/ECWq9EKzLuNxa102/L5cvc70n" +
                "ML2j89lffcUiLHtN8typLPLeZ8zQPzUKffVPl8F29e2Dqx95lV" +
                "Pt8N/lE0oLROs9hC9ymXI76puzq5M+pa8368LG7zeGq/Lo2nv1" +
                "U5nvxCzzztM9b78rdKzrsl1Y6n4FhwDCjwjGu/wWQgATvxR3Zg" +
                "b6B6NIq71xRHxqHqxFlGqiKRczNO5FPjYHymuOo1g4V1ffp7pe" +
                "NpcaHx9I/KxtPiytenKdGAUsxvYgvdxyS354hv6XJ9miqnNa/j" +
                "xREXv99524uNJ/N7FXU8jR9f2PudQF/ZeJoWDSj9RG7FFrqPSW" +
                "7PMf5Cl+Npupw2/nr5KB3un97qbDyZ95njJxZ4//SNYvvM+JuF" +
                "P6kZ0YDSOp3DFrqPSW6TDs4MznQ5nmbKab1L3HbQM9tZ7iUPUP" +
                "oz6X3Bj9v/peeHkd2vFD/8vuBHynvfH5JHue7BVu/4i8bM+8kj" +
                "5BeS5Iny733JQ/i9L3mMXj9/Y0R+4nrv6w3k1OaX5HFpH8V+Bz" +
                "Pl7+N31n2qTlC9ToHzTuLvG/+Tcxc3FeTciVI02CrtAWfy9pab" +
                "UWFJ2veW2zObNME0o2J9Ik+qOkFt/rajtT1ntzsd5Kyw5GnZKu" +
                "0BZ/L2VphRYUna91bYM5s0wVZGm/ydS7CXNaET1Obf6RFszfOn" +
                "aJBV2gPO5O2tNKPCkrTvrbRnNmm8Pd5t7Myt7klbyjOZqlf9c9" +
                "4T7XFr8/xVK4FLYNfz6VGxhMWwZ8Yacsi6z1xV5XNL12/Jyu4z" +
                "V1W9z8xGz2FGRZ2CSdaETlCbf8fr02G3f4YGWaU94Eze8agZFZ" +
                "a4r8h9Tf713Gpb7f/oELiKaecLfXCEUTSetqs6QW3+1nlxIi+7" +
                "2z+J8C/ZKu0BZ/Jm6A3yI/o1u2uizOcJ+Vyrja0DXsjkvsnfui" +
                "ZOuLV5/qoVxuVN6N8HM50eVb0WV2as8df564ACz7h4jMlAAnYq" +
                "r1I9mhwZx5FxqDpxlpGqSOTcjIvbtjgYnymuilW2D7P1n7wMkr" +
                "rxVxfp7zM7OchJtz48lx8jRG8hw+NJe4lz+u8LKPrmsgJRrd8I" +
                "N/Fe63RwGijwjBvbwmQgATuVV6kejeL+gSmOjEPVibOMVEUi52" +
                "acyKfGwfhMcdVrVu2zGvJ1fOyr2t29xO+fpLF0ptXrj3d3nyn7" +
                "+6e8fIV///QaMc4gVKfbqrubJnWKF7hOcW1eD1Sn71Q6nu5e4D" +
                "rdXe148tf764ECn8nW+Ot3f0BIwE7lVapHo+vT/aY46LoMOnGu" +
                "1Xa/qspMOER+kQ/7sN/16JnlXPI1g4Xt+W7sniqf7/z2wj7fmf" +
                "N1/3zn7ZXPSZ3uBV7I5L7JPy++TZvnH31NtsK4vL3RLeZ8elT1" +
                "WlyZVc3gHGt0pq/J5iWJzgcdthJ8yeezsZz333PlJICZyXRblq" +
                "8IWruNy1vMu/plVa3j6f2u2/tLdEG5dVxH39k6LmGg/5EYvSe6" +
                "FNcppmtetKxYnaJ+uU5RT/QurlvsrlO0IlpZ7H4XZf+BEkn772" +
                "gJrlNEf+9N+P9FRu/MzovkOkXvjvoK1GZ5dDGfh32sKev4faDD" +
                "Vl5fZ+Mh/mnO+tVXTgKYmUy3jS0ximR2aYJnGK3zfYfogU5Qm3" +
                "/dumuJH3S+V3nGFtdmlfaAM3nHD5jRYon7isya4Cij4nuEeqzq" +
                "BLX516274PghZwWO2uLarNIecCbvuGZGiyXuK3JpknnH6xRd3s" +
                "Hzyer5ek5I9wUOrbYviH9ePYbB/7BGR8RdfDzdBTpsJXj5e07V" +
                "W5vrb+djKCMBzEym27J8ulyze8tu4/IW/48QP6xEtHzPheXRB/" +
                "PtC+98txa30m3JlfP7HFyf0HsZoiEL0iGTt92+cJ2GilvpttGq" +
                "ea7THr2XIdpkQbrJ5G23L1wnyT9a7bLSc0VXVFyX/nq/oKzHuL" +
                "Hv1vvBAmRyj+zA3sxfxBS+uI/jqDhMOZiV91eMFFsJjICfcfEj" +
                "ehwdn1wBOZ7M+Wv9tcnTEKX0uWgt+0vq9D0mAwnYqbxK9Wj0Ss" +
                "+a4qDnMYNOnMECy0w4RH6RT42D8Zniqtes2mfj9SO8d22Fd/Xr" +
                "upyDr+TE/3BZjw7e7NjeP91f5fsnb65LlGV/XzBX7funaCjyoi" +
                "Di3ztHN/B528F/9EYNi/zGaEOXI9KJJvqctu4WRB8V/Qel/wGz" +
                "a4LF");
            
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
            final int compressedBytes = 2704;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW2uMHEcRvgsBy1EcSIQ54R9RwI4gsuBXgoIi0D5myA//sG" +
                "MbX+IEBVAkhJFAgAT5gzSzu6fVLhKcJSJLvCKwo1wSCGARHgoK" +
                "eRAiAwbzukRB+M5guMTnR45cEucMmOmuqanq6p7ZHu/ciuxour" +
                "u+qv7qm97p3tm+vbEx89W6F1uNy8b+j16d2wq9uyWy2uo7O7DV" +
                "ve+1PE7tzaujoz5d/xTUiPT2o02Y2W7vceNu/gHZp700TnM+tF" +
                "C7nc9mlddSlJl7Oh+KH+l8NH5wbCz+kbLjK9ETfhpb8c/T2I9k" +
                "yFO6fEiXvzHZ40N2xvinw76P8Q8Kvb+QCKk34n6ctZ5Mr+nD/h" +
                "ra39LlPXyc2t/o3q/rA2zturSA4+vmOLW/1r4/8327tS6n193t" +
                "e9szBvJgTuQVBbn3t9Yy6wFdvjuzv+kcsSc9x+a+9newXTsLpx" +
                "6L27N5dw/6eBTZ5V7heLHf5i1GUDNgdizk81GbH1PUO/5SNk4H" +
                "xWzdbo31Hjfuig+eK71ibvePsmPL5/PI9SY4zfWp+UX08Siyy7" +
                "2CVwdrKIOgZsDsWMjnozY/pqg3jVPvYekrWp+K1vH2C6114euH" +
                "ey+L1qdE2Vpr3g3I570+nWyfzsZtDZz6+Sx7ToEW4hhFdplX47" +
                "aB7+qaYkQxcAQ1A2b3lurLZB7sMdanX1Y5t8OLhuvf+kSRN9pQ" +
                "dT7nur4CpznvOgfQx6PILnWVH/fRUISoceIIagbM7t3Z78Z9Mg" +
                "/2JPzZt5XeYXEX3ppzd3rhwX9Kf7rc6h9lx5bPV/J71APp+tXu" +
                "/VYouiVHqRce/KP0ON3iH2XHls838PPpkeR0fG/pzcrvLaxP6e" +
                "8twanRfm9x57O/t/i+mgvNBSzRBqv3LGDop5p6mL05hx7pv2B0" +
                "ovufNo/U4cpBUSaGUZgZMFKr8kkeqc/Ny9Vwdnr1Thmfd6fFHb" +
                "4t58534q0vmHiw7POu9c4MzufKbsf65SvxnLw52Iwl2mD1XgIM" +
                "EYyTtixtNt064eIxdUgf1aZSqcTMnfpO5PFwfS5eec0yXj4/dc" +
                "SKUN+a845uzbmftg75bWqrf5Qd27pzdT/vWq1sDrxY6X37gte8" +
                "W65qP9MvXwn9NwY3Yok2WL0VwBDBOGnL0mbTrSUXj6lD+qg2lU" +
                "olZu7Ut5THw/W5eOU1p9ZkMJm0dKmxSTiSJ//pYDKcJgTjpC1L" +
                "m023zrt4mFqHj2pQY2IuHZSf8pk8mMvMLGM4t4yX31v67xIrwY" +
                "6cFcILD/5den3a4R9lx5bPN2De3RDcgCXaYPWvBQwRjJO2LG02" +
                "3Trt4jF1SB/VplKpxMyd+k7n8XB9Ll55zWZ8fa9ZJ+P0PrQBq+" +
                "80/VnPQlzy59wVewf177/fjEJdKirBd9qMOar2mm1XZldssC3Y" +
                "hiXaKdYADBGMo6O9h/fG0mbTrZMuHlOH9FFtKpVKzNyp72QeD9" +
                "fn4pXXLOPTfZvF7LP2D5XO75dW82nG3n9a3XzGOn5TpeO0Mlz/" +
                "0s9PK5WPTM5+QX+n3C/oPH3h+wXhG0a7X+DOd+H7BcEdwR1Yog" +
                "1WfztgiGAcHcn6xHpjabPp1vMuHlOH9FFtKpVKzNyp7/k8Hq7P" +
                "xSuvmeLr74FT3E+3oy9d/XUU2ebvCwbujw/Y0ee8PghqBsyOhX" +
                "w27pPZ7cmdd5+scp8uvHjE8+5iv3nXmRt6ffpMpfuZZ0a8n3mm" +
                "2vUpPBIewRJtsPqfBQwRjKOjvYf3xtJm07pfdPGYOqSPalOpVG" +
                "LmBovySR6uz8Urr5nia+fgFPfTx9CX/pVGR5Fdbn0K/jXgr0Pn" +
                "yiGoGTA7FvLZuE9mt0fNO9fzU/f7FzhDDjmfcIbcD+osFHpPSq" +
                "T7Pc/1aXHo9elzla5Pr4x4fXrFZ5w6L3v/vWWluYIl2mD1Pw8Y" +
                "IhgnbVnabFr3oovH1CF9VJtKpRIzN1iUT/JwfS5eec0yPu113m" +
                "6lTxO7c54yvPDwdaX3n3b7R9mx5fNd8LxrDTvv+m02746PeN4d" +
                "91ufptYO+z146hLxzt2c84564cF/S99PN/tH2bHl85X8e8u12T" +
                "iJ357Wc/YP/PDg5dLjdJN/lB1bPt+Adfxw8zCWaIPV3wcYIhgn" +
                "bVnabFr3govH1CF9VJtKpRIzN1iUT/JwfS5eec2pNducNcZN2+" +
                "rsfxc8aGMcoTwa22aLvb/n0AM97BhE0F8cJTWZ6lS+PJ0F98ys" +
                "G0H2+nVwinl3OfrSO1xHkV3y+9Gg+XNdOQQ1A2bHhjkcPpldnu" +
                "YzzWewRBus/iHAEME4acvSZtPv71kXj6lD+qg2lUolZm6wKJ/k" +
                "4fpcvPKaMSLvuaD7UJXP40M/vZR8Luj+sNr9gua+5j4s0QYrfC" +
                "9giGCctGVps5nMnMfU4WJHTp5FKnH5lPp8HpnZHYNxWDeWsGws" +
                "NdP/Z2ksKbvzVfDp6APUxhgx5geAQ/oIlS+VBTLlv6B39yfFmQ" +
                "HDmkdgS9Wtu3lmLId8Hv/Va3rePVztvCt4zpwQq/8uGZv+f8uu" +
                "nE8LA5/6c+nnzF3+UXZs92cjex5/q1D0wRylXnh4V+lxMvpPbS" +
                "iKsjWUzzdgHZ9JjnEoEwtagCVtA5kx2pkNHIwpY0sxyTyT4ZBl" +
                "XPtn5EG9UwaJychxwzduqdbXwxhA1TjnVTWLmSFL72/OY1mbx/" +
                "WpNk9o+s5NUhtjADcR7Ec4odbO6jwczjtlMr83V4UlYlK3ifPM" +
                "dJW+67hrv6D/pyr3fbuPjXjf99Fq932Dq4KragdVrcraQWWDpd" +
                "qAKBsjoA0lRSMXcdTYfzkCitmgBxwqizrQh8w8inpTFCjmJ6mD" +
                "GnSjDX7UCX7g5Tq531QEde0olrWj+H+vtaOEZtxJe+qdpi3mw1" +
                "HOZqN2PBwJ7zty/+qR9Z56G2FTmxL7GhlnXk0ScbXEeWYop97u" +
                "dT9NBBO1Y6pWZe2YssFSbUCUjRHQhpKikYs4IDbVdAxOyAY94F" +
                "BZ1IE+ZOZR1JuiQDE/SR3UoBtt8AcTXBPwcp3BhD02WEePR080" +
                "9K+RVdlYjq+MjoyNRbON5cZy9LRComcT++/JuQBxkf7LWKSjo1" +
                "ej88lcT/9GHa9JOS6Nr2iwXzgDqnvNR8eivyX18eg58MChe18U" +
                "6z3t+JLkvCxen5RviR6NHqPemuH30R/BjpLvptFfM/xEmuEsXk" +
                "2sd3TjN+vYp6JfJ2p/xzUBL9cZWc/msf7UjtfFbww2BhuTEdOl" +
                "HruNcCSfhiGWhFFbHeDD3lhCqxkSm/5sdfKkzyYhR2RL1RiBGG" +
                "eTuTG/5IHrQVXZc1Eoc/FRICtchF/QBevT5zNtq7O5BTxo4y/t" +
                "CFURtgc4mlvEc9oW9ECPcDGqY872NGXGXFgCRmyIkCbFEm2QGr" +
                "h66NX+cuj4bOM6pT9YT731XJ3DsjaXreNzhGZzek6s49ZvPYDD" +
                "1a/m/F2IygKZCtbxrDdbx+cc6/gcr9MeV0ucZ4bScx3fFGyq3w" +
                "ll8rymW2CpNkcwTtrAQUzEBphkxljMog5C7BZw8ixSCeVWLcwv" +
                "VYOfrhlU8WuAmnQDgplz9wtOVblf0H1ixPsFj1e/X9C8Hsvm9f" +
                "EHECM02x/4iqufsYNwF2crjsUsmKlInQ8bYjK/ifPMdJUer/8B" +
                "UMqAQA==");
            
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
            final int compressedBytes = 1925;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW92LHEUQn6eIiYL4lCf/A0NI9qIkbm7nY4l58ysvgiiBxB" +
                "D0UFDy6O3NrSO3kAcVv2IQfIkfSfxMgi+eH3eKkjwpGESFqGd2" +
                "MR8nCFFRcHprqqu6Z3Zmdrenk51ha6qrqn+/6tru3tm5W3/RX/" +
                "Trfl86TqKBLdYVi2gtKmedrCTDJxEN4/taNk4dzhQyj6onuek2" +
                "PbKeaHUtok5Xx8GswieS8dY5rrhS3jL35OqtoPRW5mqA5a20Nz" +
                "lO+CL40OYoh95GjCJrtEQeOJ2cA7zhS/nMZKPRpO1zr3FmlOWO" +
                "1sdK6xY5mmVnpKP1lVPBMX8+13tBtzx9XWZup6SWjG7+93L8/j" +
                "n/HEpsizMMfbfvcdGCcex0eW+USZRLaH0tEyfhdLlF1/pXl7Mg" +
                "sxqJ3MifQnT5GOX4XZ2LV4Faet1w3TlOe4vJ+RB86Fg9TPM1Lj" +
                "YuugdQggYtoTsOWbiX9wMdsNwD2ENo4OFW3hvQ4UQL+UiD3sii" +
                "Z4LMYIMc1AjUwE/RgMvHoOKprdR8kpaFc0U1Dvc71+zRvq1c3N" +
                "zqiPhbZZ0umaxT8KbldVeSr33HiHWalnX69VqaT+3GkPHbS8a5" +
                "Jfen1fjsSQkatGI99pOFeXk/0BOsnuzRk+jcynsDev+UkasZWr" +
                "83suiZSObVJF8+GjW211hVxtxL+BVWhqe2Ljcux1oiQYOWv0PI" +
                "cB9aVK+QIoJ6Q3+4Ch9qYIVYFUd+Nu/QkfUojECbiqbmz3PT+1" +
                "B+xK2yEp6Oru1rDyZrKmwHRveL45b3J8N87mZ3s78BJWjQEnpc" +
                "b2nhXt4PdMDyN2APoYEHfByH7IJFnGghH2mAiSx6JsgMNshBjU" +
                "AN/BQNuHwMKp7awnrNJvfx4U/yHXk1q66zl0bch+/P98/+V4wx" +
                "e4bp38avH3LmUz/71k0lUH8b+A1sb879RE2uuweMroNTltedYT" +
                "6/5/caP4KMd6y+Bi2hcwvG6W3AICRCA5uOjLHIIk6ypDXA5Cx6" +
                "JsQtNOTXswY/jRmy4mOAK+UNFmSOo7vNPc09QoqX44hXo9vogg" +
                "+wMQL9QpJP3mV0sQdIEQ9xgJy6K+nCSZgqHscEbmQGZN4Dc8fR" +
                "YA9kxmxxLIBLYyKJeWdlnPE9+CGj6+Ady+uuYr7mMbkidxau2Z" +
                "3lfcPEFkeEzwyfWzZLMTeL3YTS39T8FG1kVeMGtxGjXCyyIFNe" +
                "dmXQ0Kbzq3bOTKMsd3hdlF63/TDaRHv+Fa+rxw1uI4buI2s6Hs" +
                "6i7LxutJjPTDadn9vZc98u4OZzl7ovMPotN3jP8v5UMR/bxx81" +
                "mvfblutUMV/7MTmfHjea90nLdaqArxmhbEatg2iDNvh43OA2Yu" +
                "g+sqbj4SzKTo/J6oE2nZ/buTULt/xBf5dauDDac7qsv0vN3Rgc" +
                "G++9DG/O3S2uT82nAr7W8nj5sDqlnhzP3VAKIbtORyzX6UjFdT" +
                "oo6/SH0f3iLcv7U8V8OJ/CcOHKuFgLf0nUs8GJMfP6c8g6nai4" +
                "TjSf/h2h97MD837D8nwyzOet99aTBA1awWYhyaJ6hQz3897Qnz" +
                "Cpbz/vd9M4eh5ZHBSl2jiamj+0iI/30fPTcTmejp49n8Kws3H4" +
                "z7uc+fS+5flkmC/YFexCiW1odabAhhaMozPcz3ujTKP1taNZOG" +
                "oeuo+uaqZ6Jip34js6CIfnl4Wrj1mP1+8LOluGn08578cHlueT" +
                "cT7x/0+t4/HrpFan22VE8l19/jtp+bIv+58prdPF90+tj8bOMv" +
                "d7bWtJt0RfZ8adGvX+Kdgd7EaJ7cR2CGxowTi9rcs0morMcdQ8" +
                "dB9d1Uz1TFTuxHdoEA7PLwtXz1WPV9edv9ZfK54/mZq1Am2cIz" +
                "ptgo+eP5m6z4zXXd3k/hSdsbs/RZ9Y258a+v7UfmH0/cn7wu7+" +
                "lM2X3p/az489n7bHbGdMzSdvzPk0bP/s+NGz8BpegyRoSespr4" +
                "ER0qZoqoT+hEl9uc5x9DyyOChKtXE0Nf/E91QaJ52fjsvxdHRt" +
                "Ph2W82mHyf2p+bLd/ck0n7fOW0cSNGh17hSSLKo3LaE/YcYIOz" +
                "E63ld/SePoeWRxUJRq42hq/tCKfk7j6PmlcTmejj5wPt1l9P19" +
                "rkxU5267fKMdnXvi/V/eMXXuNVqnw5bXnWG+5kxzxltBCRq0Ov" +
                "c1Z8TvW9DCvbwf6IAFvxnBNni4Fc5oBVHo9y0qMtfw9y2qjdD4" +
                "KCAHNQI1cYX7TGjhr25UVsJTW6l197qcT3uNvh+fWZ5PFfPR88" +
                "zOPqN5L1uu07K1Oj1i9L5gyXKdlqzVacbo97uu3TpF523VqTnn" +
                "TPBRdfZUp6g3yXUKv7FVp2DR5LqzfUQXrdXp80meT1VnT89VAo" +
                "NPulpnx0YY8u/B0SU788lf468xiWsW7erzyTpN+9NG8562XKdp" +
                "O3WKv/vcOsn7U9XZs/uCyxN9X/C9rTq5Wye5TlVnz+q0bex7mC" +
                "tXsU7brK27fyZ5PkV/W/q82+in/q/HL/xPHxGRHVXc1/Dn3cbx" +
                "/KXrVPNrKexaYXa1QVHFfQ3XqTaev3SdpvypFPZUYXZTg6KK+x" +
                "qu09R4/tTxP7IQM2U=");
            
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
            final int compressedBytes = 1553;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWktvHEUQHiQEgjgS4hERWQqES5DCgbAXpJVQ70ucEOLAD+" +
                "Dqi+0kNhCcON4Z2yOtxIlccuHA4xgJgRN5k6w3ie0QEhnCAUsQ" +
                "Oxd+AIIfALs96q2eqerZKXdPS7TV5Zmqr6q+remaac86CNJj6X" +
                "N11JkOxoxwKig8mo8Cr6PsfFqdZp3y3vdcp30/dWocbxzP2rCG" +
                "QtB+433djnH5bPlo6+ms077b87ye9vyspzBsVFFdvhhbuUuDK1" +
                "Ulr1/V83qq2tkL991kYzIIosup2JNj2U1SKFrLG/FTzDqR+dpf" +
                "Fv8sqDK9wbwymFfl2bFR382PEBvI566Ua1I+yNjuETnW46ctr9" +
                "93udbNrGa1T+KujY62nN2fPnXZB/Ehv30XP+ttX7DgMm7tX791" +
                "KjufVqcll3HrTxRBddp+87moU/1E8D8eZbOH+3g8gZ4WE4UiEP" +
                "fx9mFbXuHzedb2Mwg/pu8c3sdXnd5XD/tdTyvve7s/XXa6H1/0" +
                "vB9f9FOnMOx8ZRur87XGe8lznZa81ekbp7wXPNdpwVuduk77bt" +
                "ZznWa91anntE5nPNep5Hza33cPnNZpxnOdHOdbvps53x6tpx2n" +
                "dbpI5N4qsU4X3cZrVVoVkMnR8GdQp5+H56DRrclROKV7J/4QE3" +
                "wl77M4TpYHlQNQaZ0eLc0/OYN8uk+WXzauHi99Js6IQScrmRwN" +
                "fwZ1eihkjyuNbk2OwindO/FXHnCUHDcv4Dh6nUw5AJXW6dHS/J" +
                "MzyKf7ZPll4+rxstGN+8xfnfbdnOf705yf+/hgPf3mlPdnnuvk" +
                "PB/9PnNQp90RwsH7zKblWz/u+0w6H36fGT0+4PPv4ajv/nDad9" +
                "Nlrp7lX8rPJ36EqaT8C/85HYF9sIcJPdRRWi5LjjX8PR/H52Ps" +
                "uz2nfXfec9+dL9Z3jCu1C1NJWad9HYF9sh5m9FDX/MRyPe3yrH" +
                "Q+wOXHI/aZO60dJdV5ctZ5lOiURuHgZ7DP1LyVxNEk74+pOGke" +
                "WRv8TjPNMknnTs4gXzaOzo+Km/3MCmHsu8dO++6c574757rv6O" +
                "ddGDrez7SDEgfxvHOez7SeNISL9TRvxzKcZq6neef38aswlZRd" +
                "uaIjsA+lN+cojjWz5FiBffEoYxhcg6mk3D+9rCOwD6U35yiONb" +
                "PkWKPX+VHK7btCOdabpz3fx0/n990BrtQmTCWHo/6ijsA+lN6k" +
                "FZs0lseSYwX2NI7PR9yBqaTsu6M6AvtQepNW3KGxPJYca3QyH8" +
                "fnI27CVFJekTkdgX0ovUkrbtJYHkuOtT6Xj+PzEdswlZSZ3tUR" +
                "2IfSm7Rim8byWHKswJ7G8fmIPkwlZd+9oiOwD6U3aUWfxvJYcq" +
                "zR2/k4Ph/T/x0e9HlH7zNrf/t93sUvOd9n9mAqKVfuazoC+1B6" +
                "k1b0RM9yPfV4VmBP4/h8PK2nv/yup9Vvna+n6zCVlCv3VR2BfS" +
                "i9SSuu01geS441qubj+HzEbZhKypVb0RHYh9KbtOI2jeWx5Fjr" +
                "lXwcn4/YgKmkzHRKR2AfSm/Sig0ay2PJsQJ7GsfnI27BVFL23U" +
                "kdgX0ovUkrbtFYHkuONfogH8fnI7owlZRX5D0dgX0ovUkruqJr" +
                "WacuzwrsaRyfj7gBU0m5nt7QEdiH0pu04gaN5bHkWKMP83F8Pm" +
                "ILppIYQWnElike1gnL99H5/thaW87HqSMaN37A98FuR+2foMSB" +
                "34/HR1znEPdgKik/WV9HYB/sYUIPdZSWy5JjrfXzcXw+tvtx/X" +
                "uE1hHTfrxleX25+3E6H96PF+fl8u+WqGHIsR6f8FunlUv5dRox" +
                "bhZe0d/DVFJ2+Ds6AvtQenOO4lgzS441WuRH8Xcfb73At5RzHy" +
                "+a76C8LOs0wbeUVKcJW8ZoLd6HqSRGUBrsUf/IFIHSsvruPs86" +
                "ZJKH4/MRP8FUEiMoDfaoz5giUFouS461PpOP4/NxuS8wv6eLK3" +
                "6fd/Fbrt/TuX6fSe8Lak/6rROdz+r74B9gKokRlIbQ/2nKQcXl" +
                "suRYaSY2LMQaTCUxgtIQ+iumHGLNsk5rPCvNxJaFtjKPFcdy/i" +
                "86PhV4HfGbjgP+B0gMnBo=");
            
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
            final int compressedBytes = 1773;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWk2I3VQUjqALi93oRq3Mqt1a3ThYFObFPHXalYgDitrSqY" +
                "tOEcSqINNS37yZN+miFkSXXRSk2pHaWnDEgrrQIlKR/ijUuhDt" +
                "QpRxKigUhIJJzjvv/NybSfJyk0lrwpx7zrnnfufLfffeJHfieZ" +
                "3PPa/zYfS36EVHZ8Qzjs4XhufrRH6cyG/JP/ZrZH9jQfh0bskr" +
                "dXQ+WrH2K+2JmVjiPhlop6Gc/XlIPoN+av2QFdudyos6s9MreX" +
                "QXi8Vnsy92BAeDgyjRBqv1JfjQg3Ha1pLQZqcxTuLMvk4InIfM" +
                "gRpghg9qn40H5Y/Z23Doms0eoBiOreP74+lNr5IjHPdqPcLHXS" +
                "O6XJ8S27o+jV2reX26lm99GprPSJnW/ospqBdL/5r/uGHibHSV" +
                "6qexhab0UxqTavrJ/8kN6szOskhF29vjXV1P6fF0vDHj6XiT55" +
                "2/qzHr065q+2n2qPhVTqZb1l/xZFW82r8XHE8nV288da5mPrU8" +
                "szrj6Y27DSZPN/p+d6wx69Ox1eunmdsy2V22P2fOrC39fnf7iv" +
                "fTW21MVrxOp8+Z4XPCejazn440ZjwdqXg8ife7cKuwns9k96PX" +
                "kKNqJuXeg8fea0w/Vcyk5Dp+ojHz7oTbfgkOBAf8QyCjp7NEAy" +
                "vWQZKPdLIBg5BMNInMcWL8+ITWhMM1zoP76KTcsYb5dTbAoWsG" +
                "VhI3LjECPXh9LsdT+vtdOFnyfldwP3P+jrrmnT89xHvLdGPeW6" +
                "brXJ/8D5qyLhdlYo8vcz1p+5n+nkFE7v3MuE0z/t9C7EWcs/+3" +
                "9O4S2RYaM54WXMS7u57eusFT5UvedXx0LzlfkdLm3WjxeZfY1n" +
                "lXmmXReTeab97lHp97/b0kQQMLbPLI2lh2p3hrjCcb47nOcTQP" +
                "Ww6Kkj6OJvmDFb5s4pj8NC7Hk1Z7f3u/54FM9sP2wwk692Cctr" +
                "U00ZJZvNuGw/bhLHVUYgT32XhQ/nBNGg7nZ8PV16zjjeenh6/n" +
                "9ck1+979aetT+Kpen3r3Db8+9e6td32aXXS7PrXPtM+QBA2scE" +
                "8sycNruW3HIHSpSxzJQ9dRKZlqJjI3WN1LaTicn8bFGtMyevzQ" +
                "CmPvgeHHQ7i33nk3f0vF7y3L1eCGM9d3P7X3tff551GCBhbY5O" +
                "G1vB3ogOWfj1bQPhpqUuc4sRdOwCA0rkFrnoszwczgAw4yglgA" +
                "FkbbcDmetMqMp/zfiUWMTpW6e52qqkX4Vtnn8dYrTXoe7z1U7H" +
                "5H7N3c74LDwWGUaKOFknykx2d3irdGaaLF2iNP2HAkD11HpWSq" +
                "mcjcYFE+jcP5RePp3bRcvB840+z9zO7bmTPxnahHXrPt09m8Ve" +
                "7TZeXD+mF59fzByN3tcn2q+8hmX3C9m/AnSIIGVvhZLMkja00J" +
                "7QmT2nKd42gethwUJX0cTfIHa+4mE8fkp3E5nkZX4/vfau53wc" +
                "31jifX+YJNwSaUaKOFknyk83ZSmmgSmeNIHrqOymh0n9c+Gw/K" +
                "39pmx6Fr7s/PbbYYji3jB+vTo94NcfSCnHGP5XweX24vo0QbLZ" +
                "TkI523k9JEk8gcR/LQdVRKppqJzA1Wa0caDucXjacdabl4P0Tn" +
                "lfaVSEtk4rsCJ+jcg3Ha1tJEk8gch/WTpY5KjOA+Gw/K35pMw+" +
                "H8on6aTMvF+4EzzX5+Gv6o//un1vaczw/bh+Nzo/RTeCHn+933" +
                "/+9+am3NOZ62uu+n7O8O096DV6GfXshgeTpfXGr7wXdirfVlr2" +
                "3uqRT/kzW8t6yvFp/tq2x09zy+CuNpo9t+Cc4F51CijRZK8pHO" +
                "20lpoiW819lwJA9dR6VkqpnI3GCFv6ThcH42XH3NOr7KdTzqp3" +
                "vqfR4P1zifaWn7mSMu9zPr/q6nNZJvPzPvdz3BheACSrTBCi+D" +
                "Dz0Yp20tTTSJzHEkD11HpWSqmcjcYM1NpOFwfjZczbVvjQfjkZ" +
                "bIxDcOJ+jcg3Ha1tJEk8gch7G11FGJEdxn40H5wz/ScDg/G67m" +
                "2re2BFsiLZGJbwucoHMPxmlbSxMteUc6asNhbC11VGIE99l4UH" +
                "7Kp3E4Pxuuvua+tTnYHGmJTHyb4QSdezBO21qaaMm6+qcNh7G1" +
                "1FGJEdxn40H5w6U0HM7PhquvWcf3n0TurOb+0675i1jX+fwN/g" +
                "aSoIEFNnlkrSkxnmyMT8bT3yaO5mHLQVHSx9Ekf7DmfzNxTH4a" +
                "l+Np9HrGU/BdzfvjjvMFo8EoSrTRQkk+0uOzO8VbozTREu2sDU" +
                "fy0HVUSqaaiczdrzubhsP52XD1Nev46vYLun/NrA2vlvs9i77f" +
                "zWexzPl/8+5Sd3nl/QLH47ZX87yrOF+R97v8+wWdi633S/Iq+H" +
                "/zsvmM4z91PtkF");
            
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
            final int compressedBytes = 1754;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW89rHFUcDz0Zq4aoRQIm1EMRBa166EEPsm921JiIAUHQgy" +
                "i9xdRDeymhFXZ3muwgxIOtleTgRSgtRPCgJSB6iYjgqSA0VEGx" +
                "zV/Qg+vJmf3O2+/3/Zp9M+/ti9sZ+p3vr/f5fua7b97MTrYTE+" +
                "LWmpsYwda62fjKEeFutXzXekP5jKpP1wL36dp49qnbC9un7j9j" +
                "Op+uBp5PV8e0T1cC9+lKqD6xyGefnBEq9skn+4LBD9m/r7N/35" +
                "nmU+tHxfNzX37bl79KsV80CDvNvx1ZflMa3ZU9+nqt6wPtp+oc" +
                "2BaXbIv3iW2hV8wz2xxDHde6qcfJq/BKZewyhLvlldEn16d+sT" +
                "Ke5cGtT41Vn9ddY7Xy+rR6cOt4+wErBM11136wedmNV+fhsmh7" +
                "UrnuhtSrc92FuN81L4Zdx13rHVifLgXu0yX/Z8HucMnutDaor3" +
                "MZjtRntjnGMG/3X4zAPpxd54vyyujDs1H97S9pZT1O1fnU/H6c" +
                "n598si/rUzQZZWtj+rSvPkWTYfukr4fzyd/6FC+7YiFC+Pnkzt" +
                "66TyvOfVo5wD6t+O0Lm2fz0SEuQQMr15NN9NAoHQc6YEWHshlf" +
                "oHF06qWj82NeJd+5B2Oowej0iOijaMgGOIgZXMuPcN3x84P6Yl" +
                "XEE63+sccl67WKexHr5Xayych7Iya9Q2LKOyXAkGPoVfNhL/0k" +
                "+6PTx8sro0+uT/3kftcDXFbzvRj7RNXqjFYiuxNBN9/1WJLt+w" +
                "MJGliZnj5BPCRKx4FeYO1nskAboFMvHZ30vf19kJlotP7ozknR" +
                "R9GQDXCQMvbxWFx3xfkV9YWqBE+wlL6tqZrxO9eyebTnT/P6qE" +
                "akz1S4N3S5jLv8eTzugg0xmme2OYYcQ6+aD/swdnKObgT3yfWp" +
                "n3p1uBU+h09Vrc5ov9v6o1VHpM+OeP3boBrbrjtaiWw78tr2ke" +
                "/CwvTeN/5wkFG8973w+cBj9d6XIOw4P2dWfO+LtYU85b3v2nv1" +
                "+LSLb3TRdDSdPz/5mqc5msuWPumjXv3vd3En7qAErbBO5RI9Ul" +
                "SRMB4xAYHrOhyZh64GZok+iibyL2KnVByZn4pL8WR0/XzyvaXP" +
                "hX3OXH/E8/fePyX794rj/7JcV1uasbdq8P2jfj2n+8g0m0YJGl" +
                "hgo0eMqpLno83z+/PpBRVH5qGrgVmij6KJ/MFKn1dxVH4yLsUT" +
                "rVDzKd4MO5/iTb/zqXmieYJLbnOLS/Shnu+dZTqaSxWt/0zxmw" +
                "5H5CHH8CgylZmItcFKXzThUH46XPmcC2uxuZhpfdn3LcIOOvXw" +
                "PNmWpYomIlMcwlYTwyPPoD4dD6yfvmTCofx0uDJXsNgZdia7Ag" +
                "sJGlhgo0eMqpLno83zqU5xhPXJUAOzRB9FE/mDFZ9TcVR+2fV5" +
                "zoQnWafZ6UwrJGhggY0eMapKno82z6c6xRH6ZKiBWaKPoon8wY" +
                "rPqjgqv6xPZ014Mrrp/fh4b2nDMo9ZPhfELEYJGlhgo0eMqpLn" +
                "o83zqU5xZB66Gpgl+iiayB+s+LyKo/LL5tN5E56Mfm/Op/hjv3" +
                "lKnzbukT6t+s1jR9lRlKCBlb6WS/SIUVXCeMTEsVSnODIPXQ3M" +
                "En0UTeQP1npPxVH5ybgUT0Y3zaf09bFex+f94iVvJMKdIXnVab" +
                "YfMVSZN0WseZbel9Y+s2VSNy/vUxInL6t9YvfVOJvI1Cfnz7O0" +
                "T8krynpiyT5pWvZzFna0nebTrLmK47o8Wy1qW886bw52tJ3OZs" +
                "5cxbFPc9WitvVs88zrE7vf4yo44utOcx/3yL58far1qR82ruOH" +
                "w65PtvXq8nJ7zmwb503z1kTQzXc9dpwdRwkaWGCjR4yqkuejzf" +
                "P7vPdUHJmHrgZmiT6KJvIHC+vRMTI/GZfiyeim+cSe8jmfQm91" +
                "2Fc60+JNX3QsOib/nTP3lG95hpql91Z8vl6slq+vh3/nrMqHLb" +
                "AFlKDlezQTzbCF9E305D6uyeNymR+jmYzBDLf5WPTS0TyHZlIG" +
                "YlbnpOijaCJ/sGSucCx+/1Rwkjsg4sno+vnkfV39KPA67r2e/f" +
                "+/q/o7DOLbSd9yZFnxdxjr+jzldxgXLv6/+uT8aVbsU/quXZ+q" +
                "/l6lcVs8ci2X6BN13XiTrxyjcduES8fQLMrLXFtXyYxpdU574p" +
                "H6qU+OD/PbxRt7w8bLWZSXbiz41Ih8LmWVxYj9dZd8UP+6Y++E" +
                "ve709dTrLnnf//pEPIY+xY+Z+pRHQvZJX0/tkz0vU5/Yktf5tB" +
                "R4Pi2Fmk+N3XG+3zV2D+65wOK6mzJed1OBr7spy+tuynk+3Rjr" +
                "+XRjNM9Pg+8tbzu9p3uoemQ0m209a17/AS/f5Fk=");
            
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
            final int compressedBytes = 2137;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW8uLHGUQHw/+BRs8SBASvSQBL7KHkIc73Z3zJtkoeRAFPW" +
                "hWBQ/KJqyiM5PsMAjCgt4EQ9ysbsBgQFciUbPRID5AjIlLVjT4" +
                "QLx4yEWCCWL3V11Tj6++nZmdnnbTzVTXr6q+X/3mm37O7lQb1U" +
                "alUs0teIAAU0RmfZttd6zFEZUKjc28HWt9ngpbQj2oSsY4m9QP" +
                "iPrxMZwJFqiz+DS7XOoPV7peGuPd10b7K6Uug+439Um702ihuk" +
                "dLniezX+uxovjrj7b386+8Lod64zo6GYgfGfw8+er7W5LFZBEt" +
                "YkDVixDDSLLYeoZ8Pk5aYjs6iXWS5+gRYuA6iId7wMm78Cq/N6" +
                "BMvc/TepqY/BmQvUihrIeldk9773m2uE+jfqhvhqd63JdfKvh8" +
                "9OvUpalva6enrtXmHb7s19Q+9SJfLMP4ixH7IZntU+f3y2Z/8o" +
                "6TDv2mfnb2x5Xqof2p4ON7ptzzePH9aukVrnY6fc2H5im0P9U+" +
                "cPYblfvSYDibvNWnyjPLZj/35snsV/uw7V3Mj89rK7yeHvS9Aq" +
                "7SB8tmKFJ9ecdd+efxkX+LrYsORAfIggcIMEV4lmObg9jBbx22" +
                "eKQOnaOtVKqVyN6AmkMhHq5P89I718jbn14VV4X1PV6V/gp+Hl" +
                "/3eb37rMfjzuxXf3PFx/1ENEEWPECAKSKzvsV6wljvvCs+j9Zh" +
                "9aAqGeNsUn+eu+Lz+Po0L+fT7Or+bE6oXyrsTL5U7ni7fuUqok" +
                "3RJrLgAQJMEZn1LdYTxnp3fpr0ebQOqwdVyRhnk/oBNdf6PL4+" +
                "zcv5NLs6P/0zmOto64Vy7zObGwu+b92QbECLGBFaipGfrY1xPh" +
                "qtzyaZOY/UoXO0lUq1Etkb0LHLIR6uz+LVWnO0MUlnHqyLbYQV" +
                "fB7BOo25HXnHYpPMnIepNXK0xQoes3TgC5TYPFyfxau1YkWRzy" +
                "0jc6Hnlr7vf3t8bhmZ6+65ZbU937Va5c5Tc6joeUrP58fRRsfr" +
                "dYwBhhyvC2PkkJ705biMX/aw1ekaawTvJntYKqz3t6yK4WiYLH" +
                "g5mo6GW02KqKxn3XaaONHLc9M+j9Zh9cCqxuMyxtmk/jw37fNE" +
                "w3A/LnuH+BTaEm1JvdyCBwgwRWTWt1hPGOu5z3nEPAV6UJWMcT" +
                "apP8894fP4+jQv59Ps6gmoPqD7p1dKvn8aKut7uuhw7+fxbMzq" +
                "uN6R+sFc76ba38dEm4u83pU+T5uLnadka7IVLWJEaClGPh8nrc" +
                "/mvH0Wj9Shc7SVSrUS2TvP7QvxcH0Wr37PgKI90Z509nMLHiDA" +
                "FJHZzDbG+WisJ4z1rtd+n0d8/oEeVCVjnE3qB0T9+BitT/NyPs" +
                "1eznNwkpT895aC+yW7kl1oESNCSzHyszV9Dmaj0fps7jN53+KR" +
                "OnSOtlKpViJ7A6J+mofrQ11WLz4P6TqWjKWesy42Biv4PIJ1Gm" +
                "vrs7n7gjcsHjZPRo62WMFjlg7q3wzycH0Wr37POdqd7E49Z11s" +
                "N6zg8wjW0ZruT2w0Wp/NfW7nLB6m1sjRNjrHu2glsjcg6sfHUK" +
                "/2fd1xq4Zz52hbsi31nHWxbbCCzyNYp7G2Ppubp3mLh82TkaNt" +
                "NM+7aCWyNyDqx8dQL9lZ13BuQDuu77heqYDNlsxDhJZi5GdrY5" +
                "yPRuuzuXk6b/GQWitH2+g876KVyN6AqB8fQ71kZ13DuQHFF+IL" +
                "lQrYbMk8RGgpRj4fJ63P5vbvGYuH1EpmXsV18Jilg/o3h0I8XJ" +
                "/Fq99zjhbihdRz1sUWYAWfR7BOY20bz2s25+21eJjaBb3SaNLB" +
                "Y5YOfKXr3hAP4MZz+flpNtSLzwNXOsi/m6f77uVy758G3S9+L5" +
                "yJj3Qam1XYVfGRfvv3Ngr7hfja+RX+D+Ry+1Nc66i3ZlXZ0QF/" +
                "3rXu8t3rKun78TO3+/fj5cxT/FC582T30/N09O/VNk9RX99ARW" +
                "d7/v7pbHf7U/OuVXbcfVTycXf+Nj3udpZ83O3scn+6r/h5arxm" +
                "zRNfGq+nCifMeZooeZ4mlp8nzK9UF/0/XfOBft5X/cUy7wJevt" +
                "vrf7isv7cUfH76uNz9qf5d0ecnxf9297W9/K4s3lPyfWaX/Vrv" +
                "Fj9P8WRHdZOhqs5jC56nye7y3eqKb8Y30SJGhJZi5GdrY5yPRu" +
                "uzOW/U4pE6dI62UqlWInvnudEQD9dn8er3jBUl/W7jjnLPT3a/" +
                "lZ+f4hvxDR9DLL4RzYAPr2gGojSKV2NFNKM5IYr1ON7uzPl5TT" +
                "Sj9ZIuHYV6YiO+aIZr4ls+VkecjlvxrdRz1sVuwQp+dIIiGSKf" +
                "jyObVUAVZ8tY+FjZRffVXrbF8Txm6cAX18rHZEown8/TCb8XZ2" +
                "+jxVj8VhEwxMhSjEfT85OoporY+/1jcidmYIRfgxHML1+lNUl1" +
                "WT/5Hnh18NhatCMwOnR+qj5Z5Pmp77u8Hs9PpL6g89NSvIQWMa" +
                "DWJYhhBOs01tZnk8ycR+rQOdpKpVqJ7A2o+UiIh+uzeLXWHF2N" +
                "r6aesy52FVbweQTrNNbWZ5PMnIepNXK0xQoes3RQ/+qaEA/Xl+" +
                "53a0K9+DxwpTnHyTbD/TrT+q3DvnlS4mOB7+aOjRV+V3myk/q+" +
                "O5yKT6FFDKh6L8QwgnUaa+uzSWbOI3VY7MjJu2glVi5TH+bRne" +
                "0arJP1+aj277Sr63Wm9UeHOZ/93/an2U7q86e531f79wWlX++i" +
                "7q53dp3xSczFc2gRI0JLMfL5OGl9NsnMeaQOix05W3/qmK0Dt9" +
                "URm0dqSudpJFxD8xB7vwIZ1N+Dy16qDxZbl6xL1sGWYx5DTBXc" +
                "+hnitLvBCPRsJbzK1it1yah7/9vDOsU8befcoblJt/8B54lgdQ" +
                "==");
            
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
            final int compressedBytes = 2101;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW82PFEUUHyQe0Jgs4aJrwgU56I2EGESJ09Pd3oA7By9clI" +
                "v/AkPLzu6w0QgbMPJh/CB+BjBRjNmsgoKE6M1g8EQCIRtm44HE" +
                "sBA2xK5+8+Z9VPVM905NZ5fu7Ot6v/fq9379qK2tmWi0Odpcq0" +
                "WZNZcZoYeWMBpHbJ60Nptk5jy13uWK0RMzOObSQfWn7uTxcH21" +
                "2lQnrxbvQ3qvi9bV2AU+YGQJ0zE7gqjrMhGYYecggvH+WVqT1h" +
                "CE+Tr5FYSc29aLs8ML4YVaDay5zAi8+C3AEME87Wtrs0lmzkOK" +
                "JDPPAk5eRSuRtcEz6t08XJ+LV2vtenPhXDrKbIbNwQ1jjmCe9r" +
                "W12bL1fd/Fw9TO6Ztmkw6OuXRQ/VYtj4frc/Hqd+56N8Ob6Siz" +
                "GXYTbhhzBPPoTvbx2Whttmx9L7l4mFpHjJ6YwTHNh7XBo3qah+" +
                "tz8ep37nq3wlvpKLMZdgtuGHME87Svrc2W9emBi4epdcToiRkc" +
                "c+mg+lRP83B9Ll79zjofrta+2kiu4H6t0st3vfrd+l2yMAIPfE" +
                "Jk1LaYTz7m8zHn0TpcNSAr6PBaPIs0on7wgo7NY+uTHZB80gu2" +
                "BltTHV0LI/CmlowlREZtC/NxBo3kmPOIf/+cGpQlMc4m9YM39d" +
                "DmsfVpXs6n2Zs/pz9n0p/zBmtu7J3AHuGo+Qs8Dx7tIVcy+0Nm" +
                "/5QrtHnVXrXNn4Zd983v+kYvWfvH9868H3ujy913mimsIKdPcU" +
                "f3ic1Z8X0i9f37VEJBXp92rOo+7SjWp4k3l6fnwL+9Sq/WVvE1" +
                "avXUp/YTq7lP7TWezxlbgi1kYQRe3DGWEBm1LcwnzlTrWsx282" +
                "gdrhqUJTHOJvWDZ9RrHq3P5uV80gsXwoX0xJnZ7Oy5AHfapzpg" +
                "iGCe9rW12SQz52GnYkeMnpjBMZcOqm/Uu3m4Phev1tr1OmH6lw" +
                "FshnXgTvv0OmCIYJ72tbXZJDPnYWodMXpiBsdcOqi+Ue/m4fpc" +
                "vFqrzu/uT70TWKvpdb94str9qbVhtPzNk72/GNu96j5Y8d+77f" +
                "4567fj2XjWWPOT1pg1WP02xLp1uxkYN5ZixIQzwJp8yANmuzLc" +
                "xCn5OCfUxsrAzGegdnwbnIGVUS2+C/DSO0kFyD94PTX269jEy0" +
                "Osp4lq15Otfsj1uSfeUz+NFkbgmVj9NCE8yufBGLjMLESRnaPd" +
                "+wayGH5zYyay8ZGJEqtWgpUBAw0yA0fwfpQNqlC9zSe9Pvv4tN" +
                "d9/KnHax9nfXrf67o9WvE+7rleNBaNNe6ATX+rsxF4ZswRzNM+" +
                "cBATsQGmmTEXq5ibEHsEnLyKVkK1zQjra9UQp3cGVfwd4Em6Ac" +
                "HK8vuC5BnH3u7h+4L4WMXfFxyr6nsVv31qj1X8Pd0G331KV98i" +
                "2sYi9qmxaPz20xDjefk+cugYoXY+3IPUNRaTvf0rE6brS5xXRj" +
                "vMegrHwnQFtNfz9dQ6sfz1FFa8ntz17PXU+qjk9ysz8slxjgUz" +
                "/ecP4s+LBjNFNFIW1+WaC5gd0e/Sr7KT94h8cpxjOj4ILxYPjg" +
                "yar7O4LtdcwOyIfpd+lXWkMY+2Md/bn+YJlXn5PnK45tm5WAUr" +
                "5e5P80UqE6brS5xXprdcOX/vGler3Z/c9Vb+uSCeGa5PyTslz0" +
                "8z/s8FgmljCe0l/luE+MOKP7eMuF70AR+1n/PF2rjiS1fBM6Gz" +
                "XvtZb3063H2exJGP9RQdjo/70VV4PR0f8Xo6Yo98sManfOkq2K" +
                "dT/nuTfJLZ03x/Srrfayafs72rzxk/OaH848nXvfGnzevxb85Z" +
                "HydfJF8K5EzOvvlfn9qf7R9n3jdZn3r1kqE6lnyVfNt/H48u+v" +
                "u9O/D20P+W50uuv4uef8+mo2m06KMXTTfOEoJ52tfWZsv21bMu" +
                "HqlDx+iJ8znm0kH1qR6fY96H65MdwPnynXW+tZ5+ra3iy7f61l" +
                "+qT+/1Kl3yeX6qvE+XfDPmncejy/o83vpn+efxoVWW/NwSXS52" +
                "Hn/3xrDn8fam1fx713pQ1XoKXvH5+a7q9UTq/a6n4Lp8cpxjOj" +
                "4ILxYPrg+ar7O4LtdcwOyIfpd+lftpKvM5uPg1/Pmp7DUVFMtr" +
                "v1DwfN+O22jRRw8tYTTm86S12TI9L7p4pA4do6dUqpXI2uC1Nu" +
                "TxcH0uXv3OOl+up3BTmO7j7Zd4LBy4s5sMO8uNljyP7y2XP6he" +
                "WT3xofgQWvTNHY6H44AhYjAa83lkTQZkcbYw/fTF5xKn1KFj9J" +
                "RKeZZUgD9cq+bBuN0BWYsUSqWj3J/Szx0V70/+61XzvW9wbziV" +
                "E6+VPBfcK3YuWLaeN2qPxdV6WDBvaZmr6+RodAePqu1T0XqTy/" +
                "zv8Yfbn+L15SOjuYrWK5oXbYu2oUUfPbSE0ZjPk9Zmk8ycR+rQ" +
                "MXpKpVqJrA1ecC6Ph+tL1925vFq8D+l9LbqWjjKbYdfghjFHME" +
                "/72tpskpnzsD45YvTEDI65dFD9YDaPh+tL+zSbV4v3gSsd7bmg" +
                "6is44zfP6lPve7rgj1XdpxGrpz41JpdxuptcMX1a6zcv2hntRI" +
                "s+eMHvgCGCedrX1maTzJxH6tAxekqlWomsDZ5R7+bh+ly8WmvX" +
                "2x3tTkeZzbDdcKd9+hswRDBP+9rabJKZ8zC1jhg9MYNjLh1U36" +
                "h383B9Ll6ttevtinalo8xm2C64YcwRzNO+tjabZOY8TK0jRk/M" +
                "4JhLB9UP1uTxcH3paliTV4v3wTxj9v/sTT4fLxgfMLKE6RifTW" +
                "OOwlVfwnw+X5z4Fvh8qCj58yvuH9d59SWplr9BidMqzf4fDKmH" +
                "0A==");
            
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
            final int compressedBytes = 1577;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlGk1v3ETUx4aIE1KlcEDKLReUijNq4/FuoRLppVI5VIRDxQ" +
                "UiKm7kgMR+JPH6gKoVIFQCVRuSpmlVFRAfUURyAaFK/AB6CbkR" +
                "IKcVgpATnp2dfW/GY+94PTPxqrZ25r0378vPb57Hs/Y88QgnPS" +
                "tHdM5zeoQvm9XXnG36Av6SDa+bF6JqQQ0kZ5yOzMepWW2eU8cp" +
                "msipLUiLE/nSZpya52WK2l79ZkKyYiJOeY/q02lxShuxFSdde9" +
                "p8p6qnoGUQwyhM9oCCRzGu1gHaGUz2VHpEP+Qx6Lk8pqn8APtg" +
                "D8vQ68H+iRHAVy5jnlfbiX8P4t+3lFZ7LhnL2m6C8nO3/abb/o" +
                "KyfT7GHyk0bBWtD7XMeVv7MTHv5pV83/Whn4r5U79TRJq845Xk" +
                "sOWJvyf2mI5p8vggut64vzdIXubCfqlkGS05Il9LlmVxJGveha" +
                "/hebf48aB5N7NelnlHPdGZd4sfanuQFafXc8bpcWni9FgvTktz" +
                "Q9any4WqwrXS1CfjnqTlU+jnf97N3LeVT0sv5syn++afd41b3X" +
                "YNx6nxWa//Aln5N0PHSvcuvt3HP21s9uHbtV9TpG427jQ2BMqD" +
                "lEj8nWF79f1nEXZP8uTzIvencbehjHh4vojWmXtlmXe2PQn77y" +
                "3ko/j9LmfUqEzeEc37etWUJwXXT22xx3RMk8cH0fXG/fYgeZkL" +
                "+6WSZbTkiHwtWZbFkbQ6TtpDvLe00+o4aRe7j7nfW9p6dbz5m+" +
                "Y8m5U0fVBkXyVjHjx0vC54qLevMvR9g3xaGMK7Ba8kh21PIE6V" +
                "FwbW1je90h7RRRvrTGU+nR1S3yMb1938PXP0MJFPZzXr018m3u" +
                "/y1vG0ONW2oktu6/jyM3b3n1RxMjHvyE6harNjX2LYfIqWTOaT" +
                "6/2Cxctm1wWJdcIblvJpt1A+7dbfyivhKp/8p2zlU7RsP5/Aez" +
                "v5ROaSkIHVzJxrDSa9Vz5h79qZd8GrbtdPtu2J7y0G4zTrOE6W" +
                "7YXzluJ00XGcLNvzfzg56ZO4jmE9rvbX8eG1uBoKe7IipqyeGy" +
                "ouNTVnXc6Ux/u+WfxA5ZCuX5WpyhRvOc6xyhRZBwrFAMZy0FIO" +
                "xoW1US1YVrQi25Uh2nN5TFP5wX/YVyxD1rkt0vt/L6kXewNYYo" +
                "fmFSH+q+mY8n6tqrjU1Jz5tGqCH/af+PiwfuV5vyvz4c+Z5XO1" +
                "X+D6/c6f0FuP+xNPdpzCmul9ldT9gtYoxykK7e4/BV8lofIfSV" +
                "/905r16XTRfPLHRro+jdnNJxSn50f6eWfcezd1nGwXWj1t5/6f" +
                "c1svn8Lrhefd5EjPu0m9OOnv06XG6cxIx+mM2foUXAmu8JbjHO" +
                "Mt0ADGcmKb1CZqxnpEP1TauU5sRfZENdb6I0sPWI6up/NAHGjv" +
                "pj5VN93mk9qejXWm2Tj5a47n3Zqr+mQ4TuuO46T1XXTzn3z5ZH" +
                "K/QP19QXSrmNa83xfo/m8efuIqn7RsbEWO592gOLlfZ2rGafNJ" +
                "j5NefQocf6+itqeYd7fLlU+k7jZOanvlz6fqiuP104r5/QLS4S" +
                "3p8DiRDsVby2wM86XjXIc8BtQkPzsHeUc64vfjKglOk+1jOvof" +
                "ocP0ZtvW2s/8fpTf75b/G811pus4VRf04lRdKBonMtL7mWTMdD" +
                "7FOo95S4779ekYqCJfOs51qOSSvNwKt5Tl3WDLQJPti3RsGa6y" +
                "UH3aMplPi4du86l1wWw+BfWgTv5kbRzfLsQwCsfPuz6F88k40w" +
                "GaQBujyZo5L7dCT6AkuZjOxlWZJnNSGoW4fdlr2rPnHbdMvcLX" +
                "wHrwm1F62EFwQI5YG8t1IYZRmFFizgPgoyejMJzBXX1IBzliFK" +
                "yZ49BTK/TkFBjDVikftyJ7wi0zGvNB5OAQGwduphdfA+vBb6w9" +
                "2A/2ySFrY7kuxDAKN2/QNubcBz56MgrDKdyzj3SQQ0ZJagZ9lM" +
                "pOToExbJXyRRsiDWsDb5gPIgeHaN/Lp971MfuiR6BP1F6Zrkx7" +
                "HmvpQSF6BuPBeGW6tQwUzifjcpvU1rU1rtID8181Bj3nwDSVH2" +
                "Af7GEZFqekZZUt8FDk79W3/veZ4ddF6m79Pe9Ej/q7hhX+DxcZ" +
                "Ub0=");
            
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
            final int compressedBytes = 2376;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW02IFFcQHjGHhGwgu4YYgy4JLMseAoLsKazIdM+EHLyEkE" +
                "s0AfGQzV68CeppnF13fsBT8ODBsCcPiiE/Kgm4ISbG/KwmQYOG" +
                "TTABb3oQooFlmZD3urqmft7r2Z6Zno7bj6mu+l7VV9VvXr/u6Z" +
                "kpFCqLhULlnHldKJitMlpwtsqXDnI1kucjuaT6vvcwfN74otDX" +
                "Vvm4Y+83Gpnf5PW72Nau9FnPa7APh8KhQmH2ZCGjzbL1szXOZp" +
                "Hv6Id9jEwu86nYync++fP1M59yGqdHOY/To2zHqXiweJAkaGCB" +
                "TYjstbI6w6PRn2z05zrn0XX4coBXcJFXyr2oRqwfrOCiy+PWZ8" +
                "7nc0l8mj2f+dTv+tbtfGospptPc0/1uN7+62pJW3UmPW99cyHX" +
                "rfRV1oz5zKfSxnznkz9f7+tTuBKuuLaRW01bqc9bHV/hVvCgKP" +
                "RuxxoPG6c4t8IL/DHen5nzc5/qfl0v1aVRyElsxEf3BVCPHgs/" +
                "EtXRCs0VFGSEteI2ZlpLIBZr8YZxJCOPMYdtzGhjOo68eBZXs3" +
                "v04JivDnzxWjUP9vPMvlxUYaQth8tGi2SELcdt1LRle5/ZRiy2" +
                "zBvGkYw8Rh02cy7zWOJk4+Tpo71Zly9pzFcHvnitPAbmE9ZEmW" +
                "Uuzk4e6o51ErXa192vIsWZQa3LjZ/+30rCM+EZlGiDVVwCDBH0" +
                "07aWxDZ7BP0kz+whYuB1+NiRk2fRlfj6bPXJPDqz3wf9pH8cdb" +
                "r9jtxI6kkcc+Uxe8TvN3so69mGmZOrz3qrta/qtR/MbP8oK97g" +
                "03zvnwadr/ZjW1vK8j4z763xS8YzdjFcRIk2WMWrgCGCftrWkt" +
                "jM+rToMoeLZn1a5D2yV2cDTp5FVyJzg2Wr9/Pwo/Xx6mOOrbvh" +
                "XaNFMsLuQjMz9wRgiKCftrmEGM0WnQcnfDysWk8f7dGDY7468A" +
                "WV+Hl4fT5efcxgVf40x3AqOpJT9lX5PT6/T4ENfYipNcDYlb80" +
                "wtk0Knkqy5Zf5vCsNKKOyh++SjhG+V2co+7xdbU+/SzO8uzW8U" +
                "9yXscHnG/u1Xamwz1Ud/hxWccHXUnt18HwhufzHae0+Ro9Hm/t" +
                "Vnrfbu4Lgkt9zY5Lg49Y43PQjuIOkqCBBTYhstfK6gyPRn+y0T" +
                "96f992eXQdvhzkJTHOJusHi/LxGF2f5uV8mj3pOV3jkX5ON3di" +
                "HT33fZjyue8H/T7PLL6znp+PU/Wdx+nYuynXu/vhfZRoo4WSMN" +
                "Jtq87waJQuW6Tt9vHIOnQf7WWluhKZO+7bncTD6/Px6mPW/vE8" +
                "fE+shqeTLe/qedrn5Ue7XJdPZ+FPKGppect7yntIggZW4x8rCZ" +
                "G9roR44qRYrnMeXYcvB3lJjLPJ+sFqPHR53Po0L+dT1t7yXqPF" +
                "EjSwwCZE9roS/clGf65zHjFOCTnIS2KcTdYPVnGfy+PWZ9axfU" +
                "l80ioNl4YLBZDR9znD0EDnCPppW0uXTTJzHvY9kqeP9ujBMV8d" +
                "lL92I4mH12fOmpWkXHwcTFsqmesVyAhbgmZG+hXAEEE/bWtJbH" +
                "NvoZ/kmXuTGNrj5ORADTh5Fl2JzA2Wrd7Pw4/Wx6uPObamS9NG" +
                "i2SETUMz4/QSYIign7a1JDYzTtMuc2najNM075F5tQacPIuuRO" +
                "YGy1bv5+FH6+PVxxxb1VLVaJGMsCo00DmCftrW0mWTzJyHVevp" +
                "oz16cMxXB+Uv7kzi4fWZ2bAzKRcfh1I12BJsMVfHWIIGVnPISk" +
                "JkryshHiNIkzrnEVfshBzkJTHOJusHa/4zl8etT/NyPs2u7ljb" +
                "9+PNZwrreGtsyPg5za5gF0nQwCpPWUmI7HUlxBMnxXKd8+g6fD" +
                "nIS2KcTdYPlq1e87j1aV7Op9mTPt+Vi/rzXX3D+vl8R9V3/nzX" +
                "RQWpf9dTf2IdjdORdONUPpLyvBsKhsKNKEEDy+r1eUJ4L48DHb" +
                "jCjeZTY8yG7Bzl0XZvs9iGCPWRBtHV/RLjbFQN1CA9ULN7+B0G" +
                "Hh/kl1mJT1rlA+UDwd8oQQPL6sanjfBe8z60bdCj98YgGGE16O" +
                "EojwZ2aIhQH2kQjVl0JZgZMKhBeqAG/eQNvPwYJJ+0ks+75ujj" +
                "9Pzp2FR35938SrbrU+lO6Q5KtNFCSRjptlVneDRKl00ycx5Zh+" +
                "6jvaxUVyJzg9UcTuLh9fl4da3aX98/Zfs9Qt5bcyRbvvqT5ixd" +
                "jVarVfua+zZe31etXZ+HPsTUNWDVuSqscjYXVc8Er9gskKnDlS" +
                "aKxt9nJmUmTOfnOP0+E7KulZtdP7eXtwf3UIIGltUNUxvhvTwO" +
                "dOAK7mGE1aCHozwa2KEhQn2kQTRm0ZVgZsCgBumBGvSTN/DyY5" +
                "B80orGtoUyaNWfjse7Ze3ZkwH7rWmg/jMSOP8hAQ7dR6jrD63j" +
                "fIqim891zkyYzs9xNp9awBuk/N+N/R2GOA+fbWvDadYn+TuMLs" +
                "/5Hj5Bwu8wUqxPzw/49wXx+hSOhCN2fcrse+wR3/o0wO/Nvet4" +
                "7/8rK0+WJ0mCZlu4KdxUnrS/i0aE9+q48iRwcU7UQLdsmkfX4c" +
                "uBXs3NEuNssn6wKB+PgXGSuZP4lPWg/MBokYywB9BAD84Sgn7a" +
                "1tJli9aDsz4eVq2nj/YYzzFfHZSf8vGY4CzmkpllLnnM6JH4ff" +
                "DUuv4+eCrd/fjsnR7X1hfi83s8HNfrk0XWWBXGfV5+tLtN3j+l" +
                "WJ/GO69P3dcj51P9xfaV840s5xOx5TOf/PkG8fypeC3LcaqP5n" +
                "zeXcv6vEscp5vren26Odj1Cccp3BZuc875bWuuCtt8Xn50sNta" +
                "+bqvJ3E+XdfzqTne3Xyi/7fI+dTL/1u6nk/XM37+dLx0HCXato" +
                "UT4UTpuL3PRMRipPM4ktYDvDhbOGHevwkdR148r6vZvXmPXtaY" +
                "rw588Vp5DFzvsCY9AhjP2clD3Re0r3fF77I7C46+3y9D9UJ3/l" +
                "lW3+33Lf2s482JfNfx2uv5rOPxXchvWc2nfpm6jff7915FcDm4" +
                "jBJttMz+NiHop20tXbZIu+3jkXXoPtpjPMd8dVB+yidibtMxuy" +
                "OA8fKYyT9YQBks4HwKFsCGPsTUGC84o74g2TSq4yy/zOF5J506" +
                "fJllNpnDV4Xv+FKte7fknuMcK97qHL8Wf1Jv8VaaGsmL1+WLBc" +
                "zt0cfSKbPo+Q8yXwA/");
            
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
            final int compressedBytes = 2504;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWk2MHNURHhIfgiIHi4UcvYcgGRYJJRe4RELzuocAISdkIQ" +
                "TECSaW2L3kglBAUXp2Z3cYeiEKkTjkR/aFgy+WOCSICwTlB6JE" +
                "8s9aYMEuGHyJEN5DZEuxhJK896qr6+e9N9szOzve7dZWV31V9V" +
                "W95+5+3T3urHZWW62Ol25zGlooCSO9w/KkDNmcVt4T42nVW8xH" +
                "R4zgWKwPqt+fSfHw/mK8esw6HrbuQW6Z19JWbHMRYVQcHW0bNT" +
                "8eTyhqTXlXvtN9y87OKfv3x3Ceqrl7u4r9do286+UfvPyniv57" +
                "hOHN8t7tzVP39aHev2ikPxONe6PW/jpa/cE9qXlqf03P0+Du8e" +
                "ep1ZruPFH3k5mngOn3daWvB+fe3dJemm/t2i3sfntbp+gUJEED" +
                "q32nk4RIbyghnzgpl+ucR/cRq0FREuNssn+wXPeaJ+xP83I+ae" +
                "Un8hOtFki3OQ2s9hxgiOQnyu+TzvOkJLblwxgneZYfJgbcJCfX" +
                "gJNX4VFhbbBc93EePtoYr+yVIpL3p1l9f2JX59TvTyvfHfH+ND" +
                "vp+1Pz9W5PzVO72TzF4yJPFIfNYZKggVU+5CQh0htKyCdOyuU6" +
                "59F9xGpQlMQ4m+wfrP61kCfsT/NyPs2u/t3uq+9v217Nykev33" +
                "rX2YG1ONuHMtuH85TtI1TGpW3kiOWFsVgFKw3rbuvKhOn6EueV" +
                "aZQN5v3ZzrPmS5SggeV0e87VCPfyPNCBy3yJGU4DD0d5NrDDjg" +
                "j5SINsrKI7wcqAQQ8yAjXwUzTw8jFIPmnl5/JzduXz0q+B52AH" +
                "nSMYp20tQzZ/DT4R42Grc8RHR4zgWKwPqt+fSfHw/mK8eswYMZ" +
                "31rvzR7ny/633SrH5+Pj+PEm208vPmFCEYp20tQza/ZpyK8cg+" +
                "tI+OmM+xWB9Un+rxHDce3p+cAcyXYwYru5pdFfc3bwNGkjDU0a" +
                "c9hKrnj/fQg/nxypw/jOHcvC+JQj05Bt5ncsW4GkdctrnN3GZZ" +
                "KwkaWGATIr1OLs3zbIwnG+O5znnEPCZqUJTEOJvsH6zyxyFP2J" +
                "/m5XzSyhfyBXtmeenPsQXYQecIxtG+NM+zUYZskpnzsLM/4qMj" +
                "RnBM82FtsMonUzy8vxiv7hUsc7+5385YJUEDqzzqJCHSG0rIxw" +
                "zSpM55xPmUqEFREuNssn+w7PN4wBP2p3k5n7TyY/kxO2Ne+rk7" +
                "Bnt2KDsEGCIOI53nkXQREMXZskP2Gj+k8yiKVwk1d8QIjsX6wD" +
                "/eq+ZBP68cq0Udyk6r9fLlep4HY3zFTuaUP5nue0v8uWBbTyaN" +
                "n58GN4z//NS+MuXvvlem9V2lfXo3fVcZeZ5ON5unwSO76/tT+f" +
                "R05+mF/zSbp5Ufjvk72bdCbZzs671NupNOr9MjCRpYTjcbhEhv" +
                "KCGfOFED3WyEPLqPWA2MgnzCOJvsHyyqx3PceHh/cgYkn7TMAX" +
                "PAdlFJ0MDy+joh0htKyK/ymFb51kMe8e+fqFEzrUuMs8n+K996" +
                "yAPj4f3xXM0nrWw9s7kg/bvMOuygcwTjaF+a59koQzb/XLAQ42" +
                "FvUhEfHTGCY5oPa4NVzqd4eH8xXj1mjOh8Ic7/L5wNGEnCtI9n" +
                "ky45JTvPDyvLLiT/KBXzjuyWj2CEuxLLLv4sVoSDxRmLve/04g" +
                "MvP7R/l+wK+gREFP/26+lj7vfg4lrxP/Yb/OM1y83Bm7dfTYuL" +
                "xafFZ177V7AWfaX71Vr/RvdWK79Z/Kl4R0YVZ4s1Zm3U2uexer" +
                "Ynv54V7xb/sPL0yKusf2Ls7u/eZHKT2yuwkqCBBTYh0htKjCcb" +
                "4/1199OQR9yfEjUoSmKcTfYPVn8m5An707ycT1mzZtZqlQQNLK" +
                "+fJMRZ5A1lHX8S7Tq3Rnm2Eb9Eap+OQlYdVVeu+698J0Me6AT9" +
                "MV7OJ63eD3ptu+zV/+um971tPWUciOO9B1KeplvPDPXe17STse" +
                "Nu7NxIEjSwwCaEe7kd5yB20MtnYjyyD+2jo+xUdyJrg9WfSfHw" +
                "/jQvjVxb5hFj33BQggZW+Zzx7z6ISG8oIR8zSJM65xHXXaIGRU" +
                "mMs8n+wXrxSsgT9qd5OZ+y7jJ3Wa2SoIFVPu8kIdIbSsjHDNKk" +
                "znnEPCVqUJTEOJvsHyw7TwFP2J/m5XzKetA8aLVKggZW+3EnCZ" +
                "HeUEI+ZpAmdc4j5ilRg6Ikxtlk/2C57jVP2J/m5XyavfoOcUke" +
                "UXOSMKmL7xiXhmPDOdqXUrw8h0fxvtK1Y5XSnI3GtC6PHOeY9m" +
                "+FN/O317fK11G8r1guYKFHj2VYZe4ZPJX8TnfHnv5Od8ekv2dW" +
                "vBvyyHGOaf9WeDN/e2OrfB3F+4rlAhZ69FiGVZae1PlUFvp8Wn" +
                "5175xP/WuT/Z7ZXmwvkgQNrPIFJwmR3lBCPnFSLtc5j+4jVoOi" +
                "JMbZZP9gvXhDyBP2p3k5n7Ty4/nxVguk/x5xHHbQOYJxtC/N82" +
                "yUIZs/O1djPOw7SMRHR4zgmObD2mCVZYqH9xfj1WPW8bAtHml+" +
                "LYzy/+zLl6f7fbx8aWf5V97aob5/udd/57TPm5dRmss4T+aysw" +
                "d98PG4tI0c2kdoGA/7Vt2Zy0tHh1cmTNfn+OJxXhnlWOvKwZ24" +
                "7hafLl+Z8nX3qwmfSxfNRZRoo4WSMNJ5npQhm+/71zEe2Yf20V" +
                "F2qjuRtcHqz6R4eH8xXj3myloza1bz0mNrsGf7s/1mbdAnBOO0" +
                "rWXI5r9X74/xsG4jPjpiBMdifVB9qsdz4LoLK8dqUYcyfvTrbq" +
                "Tr4NW9fR/PL+QXUKLt9mw2m80vuPMJEYeRzvNIugiI4mzZrD2f" +
                "ZnUeRfG6oeaO9l54VGOxPvCP98pz4HzCnvQMYD5np4jke/AZ/d" +
                "7S/91o7y295+PvLb2fTeE9+Eyz95b+bxo+L322cm7ldPfUyicw" +
                "TyvnI+yJ7wWRt6AFy/BppMr7236uWxvq3Yh1MjTjYy8/Gnv9Ox" +
                "JqE1hVj0ybYZLdT/58Mk9dn/Np0I11MsnzKT1P7dtHnyfPUM/T" +
                "8uH4PC0/vPPzRN3vzHW3fHYy52n7F2Icy5Nki3R9ZtSMbX8Pm9" +
                "B7ffvnk+xqdLbJ1rfPNavZqvkvSHtVew0sp9v34BrBOG0DBzER" +
                "G2CaGWOxitsJCaOAc+moxnQkYlhfd+2O7vmJKruu+BjgSH0Dgu" +
                "Oz0ZsozSY+P5lNQut746a6V24Gd89NzhaiYTzsQ1euzSaVCdP1" +
                "Jc4r0yjHOL9+Wz+n/i3l2Tp3wud848rp7rfbpZkzcyRBc3t2S3" +
                "aLmfPvwXO4k67znIR84kQNdMemeXQfsRoUJTHOJvsHi+rxnOo9" +
                "WNRO8Wn25HvL2a2eC3b173dnm723NN7+D1N0QpA=");
            
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
                "eNrtW01vHEUQnSt3xAFZlm/IluJTckNIng/+gCOfnEiWhbTknL" +
                "PF2tGajS+cEcj8AqQAAaFE5ELkOPGNA3CBX8CVK0xPTbleVXfb" +
                "s96PeJKe0VZXVb9+9bbSM/sV5zfzm1mWt5Y8iiiWjJ519uBeDq" +
                "sZLzHjnXf0vc+TwRGrISidQzatn6Kj73weX5/lRT7LPvylfnxb" +
                "P350ueEyr9/4gL3hs8wcw5PGPm7smZk7zbxj+HM25TF8dOHsrz" +
                "Yj6hXup3Pv+WT1y61yiy3HHLGVnPjuPLiHq9n6bM1+ehzi0Trs" +
                "nIxaqVWia1N09EOMB/WFeO1zpijfzDfrndVa8txZrBar+eb4UD" +
                "Iux56sEOvGYjXLCNXu6XYdZ3E1YxCJCjTqYFfnkE3rp8hqpXH/" +
                "mNFOk1QO8Vn22HVXfdTn607Uz+i6u1veZcuxO4uVYoVynHE58X" +
                "GdWIcgFLIVK/V+WrHrBIVVfM+NWimitAJ+oFbLw/N+B3QtUdh4" +
                "u+Vu/h/Zeoc1HkXOxwzjbFzznFuXETbKWWbGchV3Ssb3iBOrWC" +
                "VS23lc36qmeVZFVelZCq8bRTdluHLsuitf9Pm6E/Wzue4u6NPL" +
                "XvfpZbc+Pfh72j4BonOf8k+uS5+cksv79ODfrvWLQTFgy3Fzrt" +
                "XnQGVcboCnXn2OWPPY1mpvza4TlKliUG7USpHBU4BKojykyXbA" +
                "wwwgGhWj2mtskxvRST5mGGdja8nb+EzWambkgT6N7Cmric3mQj" +
                "qwfohHaunKFoPcjJjldSf3p429WV53G3sTf27ZW9h9/KTX9/GT" +
                "RfVpuv1k+1RsLbZP4XrT9MkwyeeWPOvxMXv10evutNfX3enC7k" +
                "+vet2nV/28Py26TxHcHO5P6Uj7Ke2nhe2u1Kfw95k3yhtsOeaI" +
                "reTEx3Xa+myaGXm0Djsno1ZqlejaGunzoL4Qr9XaRuvleu01ts" +
                "mt00k+ZhhnY2t9Ns2MPKA2MCcjIzAX0oH1YzyoL8RrtTIi3cev" +
                "8no3fpb61Ol7u0eTz3RHTFN/Xqqvduyf//I+fp6lI/4v883kM9" +
                "0Rs9ZkEV0UzEYl7KcXadek95mpT3N6P75cLtOIMeY4FgRaf0Y4" +
                "w9VoBXthJYgK69W6dPYynfFexHpTn0vlUuMvtXNNjDmOBYHWn+" +
                "FsUMkSr2BPz0k9QfkMUk0eVsNFOqN9itQKPZ/xb+ka63KM/3hD" +
                "nsfvHXF/XvF9wT9pr3Tq71+pB10+B6fvC1KfZvr9+NfpCosf1X" +
                "F1zB49wgi2jJQ1cV5kvkiBtZdp9LVwzEgZY0z+M5OVGtVin1ZP" +
                "nZVHDCHzggrhZRXPxlEya5l9BTzPzHadqMQxxmSfmTCh8iZzp7" +
                "ojljyKKJaMnvUt4yVmPPrIo7RHaghK55BN69dIvcbqs7zIZ9m7" +
                "38cPv3rz7uOHX87+9e5t7lN5u7zNlmOO2EpOfFynrc+mmZFH67" +
                "BzMmqlVomurZE+D+oL8VqtbbRdbtdeY5vcNp3kY4ZxNrbWZ9PM" +
                "yANqA3MyMgJzIR1YP8aD+kK8Vmsb7ZQ7tdfYJrdDJ/mYYZyNrf" +
                "XZNDPygNrAnIyMwFxIB9aP8aC+EK/VSlF1q7pV39FbSx5FFEtG" +
                "z/qW8RIzHn3kUa93kRqC0jlk0/o1Uq+x+iwv8ln2t/tzy+v5u4" +
                "14nx6+0/c+pd8R0vcFaT9d5/308N3XtZ/yJxP/XdmTbvvp8y/m" +
                "0Kf30nU33+tu/9Pef0N3Vp2JJY8iiiWDsxiHOYRd+5pH67BzMm" +
                "qlVomurZE+D+qzvPLMbeT9f7r3r+fr3ejD6/U7QupTRz0fp3cA" +
                "wfvT/eq+WPIoolgyeta3jJeY8egjj9URqiEonUM2rV8j9Rqrz/" +
                "Iin2U3OzP9LhU+/gex2Uls");
            
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
            final int compressedBytes = 1381;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71uHFUY3SoNQuIFnDxAUqRAqb3emYegClR5AkokjLNFLE" +
                "EVEEhgyTwAEkKAKAINASFRJCnSAMK8glOkZXc+fznnfPe764m9" +
                "thL7zmi/+/2ce+7x3Tt3Zkfy1pWtK5PJ1pE1zyKLkdFqaR2P2P" +
                "HsM8+EjtoYQGmO2VS/IrVP1Bd5mS+y67H91aQd6bH98+LzzeLz" +
                "wxBdTRC/FJnfB/v9YP8MtT8Shp9OrfLbldVfR7L8+MJ7aO3df0" +
                "+o52pbOdnRPe2euvXYI7fIwed+aks2ZWYe1RFraFVpVKJjK7Lk" +
                "YX0Zb9Rq0ezR7BHPm8WWg0Uu1hgNpHIquyMixjNgALpERU2qbp" +
                "XO+lHWuff4/Wn+2cXbn+b317+P333vAu7j77b73Zh5Oovngst8" +
                "3U23p9uw5llkMTJaLa3jETuefeaJOrIxgNIcs6l+RWqfqC/yMp" +
                "9G3V63t7jzDXa4B+7ZaT5nHBfjaEs2ZWYeujsnNbSO4Fymg8ev" +
                "8bC+jDdqjfj2nNn28XXs44Hpk7Zy2no6+XqaPZ89L2PLwSLnvt" +
                "diJeNUdu+fj8z8JYa5WZdm2ct01ucizyx798/6Z5OJ2eWx9Dxy" +
                "ixx87qe2ZFNm5oGirIbWEZzLdPD4NR7Wl/FGrRZ1m93m4s432O" +
                "EeuGmn+ZxxXIyjLdmUmXno7pzU0DqCc5kOHr/Gw/oy3qj1KJp2" +
                "04U32CE3tdN8zjguxtGWbMrMPKQ2qaF1BOcyHTx+jYf1ZbxRq0" +
                "X9YX+4WFmDHdbYoZ3mc8ZxMY62ZFNm5qHVn9TQOoJzmQ4ev8bD" +
                "+jLeqDXij3au70rv1T/OQ2t7LmjztMbnp9uz22499sgtcvC5n9" +
                "qSTZmZR3Vk7M7Jo0QlWY3HKHniyDkG87A4d2Y7C2+wQ27HTvM5" +
                "47gYR1uyKTPzkNqdeKI3dHAu08Hj13hYX8YbtTrifK672Tvne9" +
                "3l463xd3B7r9L28Xa/O/N56h/3j9167JFb5OBzP7UlmzIzj+qI" +
                "NbSqNCrRsRVZ8rC+jDdqdURbT20fb/v4ea+n7kZ3w63HHrlFDj" +
                "73U1uyKTPzqI5YQ6tKoxIdW5ElD+vLeKPWiLfjo7fd232/XWv0" +
                "3Ppk9qSMLQeLXKwxGkjlVHZHRIxnwAB0plc1qbpVOlfNRZ7J/p" +
                "7ZfpVl/9g53z+j73L0yGMUrEfl7OuXr4xHrFtTRIxRsB6V7bmg" +
                "zdNpju5ad81ajjnnMRBsywo489Gsh3u5EkblelWXZo/TWZ+L2t" +
                "wszo1uY/A3jmpDzDmPgWBbVjybKtnwHu5pDeMBVTJgNHyihlU6" +
                "q/NUGWvZbh+E6+7vl7xO/zvFNf7XCfr882pch7sftL2ozdMa5+" +
                "nDNgej5mne5uC071Uu9/8jhPX0cVs59aPf7/fds0+OcOtI9Knz" +
                "MvMqBdEep7HU4rEj0daYyr8MPRXVfrec8Lq73+Zg1Dx92uZgxe" +
                "7woH+wtPjUEKgDleHRy6t1FKqRuVTgdWeO/aCS2xpT/MvAxMqH" +
                "33h3ujtuPfbILXLwuZ/akk2ZmUd1xBpaVRqV6NiKLHlYX8YbtT" +
                "pi/PPT7ucX7/lp9Pung+7ArcceuUUOPvdTW7IpM/OojlhDq0qj" +
                "Eh1bkSUP68t4o1aLdr/QeZv/dn674vzha7SDX++vw5pnkcXIaL" +
                "W0jkfsePaZJ+rIxgBKc8ym+hWpfaK+yMt8GsX3dPfeevGE8OVZ" +
                "v6e79+br856uv9nfhDXPIouR0WppHY/Y8ewzT9SRjQGU5phN9S" +
                "tS+0R9kZf5NKqvp5Hf72VZT7f6W7DmWWQxMlotreMRO5595ok6" +
                "sjGA0hyzqX5Fap+oL/IyX2QP3/Eb7ddJevwPl2IO9Q==");
            
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
            final int compressedBytes = 1089;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWj1vE0EQvYpE/AMo8h/yE+yz+RNJynTpIqXGUhTz0VDTpk" +
                "eiAIQiBUUCISSqJA2JhH8AbUQBDc4tw7w3sz6vEychMGsxt/Pm" +
                "7dvnZX3eM1TVYL+qBi/Gf15X4zZYqlwbvHPIxya+auJnU/uUUX" +
                "hbXbINXrZW3xeqvPnT+zCzg1ini/hZqqLFfor99Bftp+GXf28/" +
                "bY9iP11l23kQa5Br/a3+lsbUS1nKFeGqj8LXXPjYRx3rIzeHsh" +
                "hDNfbPTB5j/Vld1LPq8bkrab313rpEySWTqJj2cRxHr8bKqMM+" +
                "bE2v7NQ64bmZ6XXQX07XehVGnJ/inHl158z+t9t493h4/7pm6p" +
                "zyFXHEbH0aXlbvnE4bb1noKzc2Yb5i30vbzG2edvbjuy3O47FO" +
                "sU7x+9NtOhf01nprEiWXTKJi2sdxHL0aK6MO+7A1vbJT64TnZq" +
                "bXQX85Xes1ZfVGvVFVKZ63855kEhXTPo7j6NVYGXXULSsjC30g" +
                "lvdhs7yOnTnP0XUYv4b1cNxrYoMN0yv1ERGezW30aqyMOuB2aF" +
                "86Wn0glvOB80/SQX85XetVGHF/ivt4rFOs0/WuU/m/I/zP67T9" +
                "vfh3lMP+oUTJJZOomPZxHEevxsqowz5sTa/s1DrhuZnpddBfTt" +
                "d6tfx4bonP3eXv4/VqvSpRcskkKqZ9HMfRq7Ey6rCPnLpo4izW" +
                "Sa6Gc3gdO3Oeo+twfo39FJ+7OBdc97mgt9hb9HnCNCpma77iNV" +
                "k9jfAcQaTezrKerIc2n5PXIo+k0XF/ukirn89eKWfM25NllDi4" +
                "mMv6rD7zecI0KiZ9qdlKTpPVZXx+ZtT3HNRGX4xiL+dz8lrkkc" +
                "bHcX1MlSZPmEbFbA3ZymRNVheG5QiiCsr2LOuJ3bX5bFmn4zzS" +
                "+Diqj6jS5AnTqJitIVuZrMnqwrAcQVRB2Z5lPbG7Np8t63SUR3" +
                "Lvp96dqLI79TO8e0X3p+KZSxzMx+WTH/HEW7ROP2MN4jw+v/PT" +
                "46+xc0pa/L/owp0Zv2cWtad3Yg2K1ulurEH2t4SV3opEySWTqJ" +
                "j2cRxHr8bKqMM+bE2v7NQ64bmZ6XXQX07Xek1Zf7m/XFUSUy9l" +
                "KVeEqz4KX3PhYx918O9r0hzKYgzV2D8zeYz1Z3VRz6rH775xzp" +
                "zfOnUPugcSJZdMomLax3EcvRorow77sDW9slPrhOdmptdBfzld" +
                "61UY5fvp6b2b2k/dvVn3U3evbD89ehbnzHgOvonn4FineA6eX+" +
                "tv9jc1pl7KUq4IV30UvubCxz7qWB+5OZTFGKqxf2byGOvP6qIe" +
                "Z92F7sL42+F3TL2UpVwRrvoofM2Fj33Uoe+nCXMoizFUY//M5D" +
                "HWn9VFPase58xZWmfEV8QR64zax0/Tn1TtjEo8Kgt95cYmzFfs" +
                "e2mbOat7wlfEEbP1aXhZvXMybbxloa/c2IT5in0vbTNT5Rcu2c" +
                "bM");
            
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
            final int compressedBytes = 1509;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vHVUQfX8AiToFvyBp8g/wev0XqBLFllzEVhTJUkpQIj" +
                "2DH/wDRBHJPRIFIBrTgBASjQuXyFDTu2bfnTc+Z86dXS/GSgK6" +
                "e/XmzZx77pmzo40/npWty63LxWKrxPW1zrzyCAz5Fp2LsVaLyq" +
                "yzuL6yPbw7g7HMB/cf02F/ma56Vb5drz5YtGvG1eY0Opmz4fX1" +
                "8PpubE6vfrT3z766Rn4p8dsSfxP2r4nCD//a5TeTuz/NVPn+Ov" +
                "t5c09f3v2cCPnfzOm214dnb+/0XV5zncx33J6nNqc2pzanNqd3" +
                "cU7Lyzanm+e0vJrbv3vcPfbotVcegSHnczHWalGZdaKPTN01uY" +
                "s6yfa4R62jnXMO5sD89nvL5PP0tHvq0WuvPAJDzudirNWiMutE" +
                "H5m6a3IXdZLtcY9aRzvnHMxh/d6+37Wv4+373Rv/fveoe+TRa6" +
                "88AkPO52Ks1aIy60Qfmbprchd1ku1xj1pHO+cczGFY5915mFup" +
                "DUMEpnvMBjNqRnVnKMcRKIBds9RTdDflc+KZOc+R4uOqC8+e1Y" +
                "YhAvPc93QHaOrkyk94pp1Zv+awNvuKKGeZz9E5XeVI8XHRXYSd" +
                "UhuGCEz3mA1m1IzqzlCOI1AAu2app+huyufEnC5yJLufzz9uP1" +
                "POudqc5l1fbLcZzJpT32Yw5/p0p80g/Qn1j1iv3r9+sj6adf7P" +
                "2/devXcLv7+/nTn19/v7iJZZZTWQuFtH56N2Puesoz6yHmBFjN" +
                "Wi/8iMZ9Sf6rKeVA/7h0O2iZZZZTWQuFtH56N2PuesE+Y00gOs" +
                "iLFa9B+Z8Yz6U13WU/X2uUr7vKDNqX3+1J6n//Lz1L/oXyBaZp" +
                "XVQOJuHZ2P2vmcs476yHqAFTFWi/4jM55Rf6rLeqrenqeZT9Rf" +
                "sVrXhiEC0z0+jTxqRnU+X3eOLqL+eMeX98Y76l39o5/Ck7ucvs" +
                "N3+3p57w38hte+3824uifdE49ee+URGHI+F2OtFpVZJ/rI1F2T" +
                "u6iTbI971DraOedgDsM67A6HrMSCHdqynBHnaa2xVovKrENuD3" +
                "XhNHwwlvvQKtfRzjkHcxjWfrc/ZCUWbN+W5Yw4T2uNtVpUZh1y" +
                "u68Lp+GDsdyHVrmOds45mMOwjrvjISuxYMe2LGfEeVprrNWiMu" +
                "uQ22NdOA0fjGU+uP+YDvvLdNXrpjroDoasxIId2LKcEedprbFW" +
                "i8qsQ24PdOE0fDCW+9Aq19HOOQdzGNZetzdkJRZsz5bljDhPa4" +
                "21WlRmHXK7pwun4YOx3IdWuY52zjmYw7CW3XLISizY0pbljDhP" +
                "a421WlRmHXK71IXT8MFY5oP7j+mwv0xXvSp/86l4+/vdvL9Lfd" +
                "JmMPF7zOv+tWf2yhkenYkz47qsPOVA400eay9eOxPvY0r1neFk" +
                "ZG24p/2pZ/bKGR6diTMjd38alSfmdKrxJo+1F6+difcxpfrOcD" +
                "KyNtyz/mwd8RpjYB+sfvT/HK35vttP/s8k1syZUcmV9Rxc8vuY" +
                "kt4ZlNh5QR70DxAts8pqIHG3js5H7XzOWSd4H+kBVsRYLfqPzH" +
                "hG/aku66l6+7ygfa5yd3Pqj/ojRMusshpI3K2j81E7n3PWUR9Z" +
                "D7AixmrRf2TGM+pPdVkvVts72zuLhcX1tc688ggMOZ+LsVaLyq" +
                "wDt9ke3p3BWOaD+4/psL9MV706o/27m/V55rPumUevvfIIDDmf" +
                "i7FWi8qsE31k6q7JXdRJtsc9ah3tnHMwh2HtdrtDVmLBdm1Zzo" +
                "jztNZYq0Vl1iG3u7pwGj4Yy31oleto55yDOQzrqBu+Vlks2JEt" +
                "yxlxntYaa7WozDrk9kgXTsMHY7kPrXId7ZxzMIdhrbrVkJVYsJ" +
                "UtyxlxntYaa7WozDrkdqULp+GDscwH9x/TYX+Zrnp1Rvs6Puvr" +
                "+PPuuUevvfIIDDmfi7FWi8qsE31k6q7JXdRJtsc9ah3tnHMwh2" +
                "GddCdDVmLBTmxZzojztNZYq0Vl1iG3J7pwGj4Yy3xw/zEd9pfp" +
                "qtcN42+nsawo");
            
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
            final int rows = 35;
            final int cols = 74;
            final int compressedBytes = 832;
            final int uncompressedBytes = 10361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV0Fu20AM1EvyhHxC0EtixLfAhvMBn/uLwI6NHgr00Ba9NC" +
                "3aoijQR1XWZjrkkCsbbQ49rBaiSO5wOEvICdR1209dt30z3u+7" +
                "8dpedeHaPoXMj8m+m+wv2fuZMHzs/vHavp3d/XYhy4c/3ve/UT" +
                "E8DA/wyp0jYIFkTZ3XMs8pUHtOY9SCGEg+a0zxZKz0KL1eve7a" +
                "dcmbedVmMPPWPw1PJ8u7huA+URmeVdito7irzFEB9sGsdVRpnz" +
                "UmPRmZrPIpcz1c0xavRCVmxu9GCzxj4K1veZz2Sg+ifM6yef0e" +
                "6WtUn/JaPmVv/+8urG1zanN6sTn1d/0dLGJEsMzRt3XeRjbPbH" +
                "m8jowdnLaLKsn2bI/Io51zDOdwerb3qf3u2pzanP7n72Dzffe5" +
                "fZ1cNKcvbQbtO/gF36evbQbt73j7f9fm9J983932t7CIEcEyR9" +
                "/WeRvZPLPl8ToydnDaLqok27M9Io92zjGcw+nZ3qeL3qeb/gYW" +
                "MSJY5ujbOm8jm2e2PF5Hxg5O20WVZHu2R+TRzjmGcxjXpt+M3m" +
                "Sn3Kas4tsMcBqrjWye2fIYtRtdrKYOm8t1aJTzaOccwzmMa9kv" +
                "R2+yU25ZVvFtBjiN1UY2z2x5jNqlLlZTh83lOjTKebRzjuEcxr" +
                "XqV6M32Sm3Kqv4NgOcxmojm2e2PEbtSherqcPmch0a5TzaOcdw" +
                "DuNa9IvRm+yUW5RVfJsBTmO1kc0zWx6jdqGL1dRhc7kOjXIe7Z" +
                "xjOAfgh8fhEV659SoIWCBZk18nvGWuX5YzR6rGqAUxkHzWmOLJ" +
                "WOlRz9jdsINX7hwBCyRrKqffeeaZOe3UntMYtSAGks8aUzwZKz" +
                "2qv+/vYREjgmWOvq3zNrJ5ZsvjdWTs4LRdVEm2Z3tEHu2cYziH" +
                "ca379ehNdsqtyyq+zQCnsdrI5pktj1G71sVq6rC5XIdGOY92zj" +
                "GcA/DDcTjCK3d4649lh/tEZXhWWeaZ391Rba6AGqMWxEDyWWOK" +
                "J2OlRz1j98MeXrlzBCyQrKmcfu+ZZ+a0V3tOY9SCGEg+a0zxZK" +
                "z0qGfsYTjAK3eOgAWSNZXTHzzzzJwOas9pjFoQA8lnjSmejJUe" +
                "1f0Gy1pxQg==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 18, 0, 3, 0, 19, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 4, 0, 0, 21, 5, 0, 22, 23, 0, 24, 0, 25, 0, 0, 1, 0, 26, 0, 6, 27, 2, 28, 0, 0, 0, 0, 29, 30, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 31, 10, 32, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 34, 0, 0, 0, 0, 17, 3, 3, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 36, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 37, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 38, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 39, 4, 0, 40, 0, 0, 0, 0, 41, 0, 0, 42, 43, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 8, 0, 0, 44, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 45, 10, 0, 0, 0, 0, 0, 20, 21, 0, 22, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 24, 25, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 0, 0, 4, 0, 0, 30, 0, 1, 31, 2, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 34, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 37, 0, 1, 38, 0, 0, 0, 6, 39, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 9, 41, 42, 0, 0, 43, 0, 5, 6, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 1, 0, 0, 0, 0, 0, 0, 0, 46, 2, 0, 0, 3, 0, 7, 47, 48, 0, 0, 0, 0, 1, 7, 8, 0, 0, 49, 0, 8, 0, 0, 0, 50, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 0, 0, 47, 52, 53, 0, 54, 0, 55, 56, 0, 57, 58, 59, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 60, 61, 10, 0, 0, 0, 0, 11, 0, 0, 62, 0, 0, 0, 63, 12, 13, 0, 0, 0, 64, 65, 0, 0, 0, 4, 0, 66, 0, 48, 5, 0, 0, 67, 1, 0, 0, 0, 14, 68, 0, 0, 0, 15, 0, 1, 0, 49, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 0, 0, 12, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 69, 70, 12, 0, 51, 71, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 72, 73, 0, 74, 75, 76, 77, 0, 1, 0, 2, 0, 0, 1, 15, 16, 17, 18, 19, 20, 21, 78, 22, 52, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 36, 37, 0, 38, 39, 79, 40, 41, 42, 43, 80, 44, 45, 46, 47, 48, 49, 0, 0, 0, 81, 0, 0, 0, 50, 0, 82, 83, 9, 0, 0, 2, 0, 84, 0, 0, 85, 1, 0, 86, 3, 0, 0, 0, 0, 0, 87, 0, 0, 0, 0, 0, 88, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 90, 91, 0, 3, 0, 4, 0, 0, 92, 1, 93, 0, 0, 0, 94, 95, 96, 0, 51, 97, 98, 99, 100, 0, 101, 53, 102, 1, 103, 0, 54, 104, 105, 55, 106, 52, 2, 53, 0, 0, 107, 0, 0, 0, 0, 108, 0, 109, 0, 110, 111, 5, 0, 0, 0, 0, 0, 0, 0, 10, 0, 112, 4, 1, 0, 0, 0, 0, 1, 113, 114, 0, 0, 3, 1, 0, 2, 115, 0, 6, 116, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117, 118, 119, 0, 120, 0, 54, 3, 56, 0, 121, 7, 0, 0, 122, 123, 0, 0, 0, 0, 0, 5, 0, 1, 0, 2, 0, 0, 124, 0, 55, 125, 126, 127, 128, 57, 129, 0, 130, 131, 132, 133, 134, 135, 136, 137, 56, 0, 138, 139, 140, 141, 0, 0, 5, 0, 0, 0, 0, 0, 57, 0, 0, 142, 1, 0, 2, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 6, 143, 0, 144, 58, 0, 59, 0, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 145, 146, 0, 0, 147, 148, 7, 0, 0, 0, 149, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 150, 1, 0, 151, 152, 0, 0, 4, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 11, 0, 0, 12, 0, 13, 0, 0, 153, 8, 0, 154, 155, 0, 14, 0, 0, 0, 15, 0, 156, 0, 0, 62, 0, 2, 0, 0, 0, 8, 0, 0, 6, 0, 0, 0, 0, 157, 158, 2, 0, 1, 0, 1, 0, 3, 159, 160, 0, 0, 0, 0, 0, 7, 0, 0, 0, 58, 0, 0, 0, 0, 0, 59, 0, 0, 161, 0, 0, 0, 9, 0, 0, 0, 162, 163, 164, 0, 10, 0, 165, 0, 11, 16, 0, 0, 2, 0, 166, 0, 4, 2, 167, 0, 0, 17, 168, 0, 0, 0, 18, 12, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 169, 2, 0, 3, 0, 0, 0, 13, 0, 170, 0, 0, 0, 0, 0, 171, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 172, 0, 173, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 174, 0, 2, 175, 176, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 177, 0, 178, 179, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 181, 22, 18, 0, 0, 0, 0, 0, 0, 182, 0, 0, 1, 0, 0, 19, 183, 0, 3, 7, 10, 0, 0, 1, 0, 0, 0, 1, 0, 184, 23, 0, 0, 0, 0, 24, 0, 0, 20, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 0, 16, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 188, 0, 0, 189, 190, 21, 0, 0, 191, 0, 192, 0, 0, 22, 0, 0, 0, 60, 0, 26, 0, 193, 0, 0, 0, 0, 0, 0, 0, 194, 23, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 195, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 24, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 196, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 197, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 1, 198, 199, 0, 1, 26, 0, 0, 0, 0, 3, 0, 0, 1, 200, 201, 13, 0, 0, 0, 0, 0, 0, 0, 0, 202, 67, 0, 0, 203, 0, 0, 204, 205, 0, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 207, 0, 0, 0, 208, 68, 0, 209, 0, 0, 3, 0, 0, 69, 0, 0, 62, 0, 0, 27, 28, 0, 0, 3, 0, 0, 29, 0, 0, 210, 0, 211, 0, 0, 64, 212, 0, 28, 213, 0, 214, 215, 0, 0, 29, 30, 0, 216, 217, 0, 31, 218, 0, 0, 219, 220, 221, 222, 30, 223, 32, 224, 225, 226, 33, 227, 0, 228, 229, 6, 230, 231, 31, 0, 232, 233, 0, 0, 0, 0, 0, 70, 0, 234, 2, 0, 0, 235, 0, 236, 34, 0, 0, 0, 237, 0, 238, 35, 0, 0, 36, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 239, 0, 240, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 0, 241, 0, 0, 0, 242, 243, 0, 0, 0, 0, 244, 0, 0, 245, 1, 0, 0, 0, 5, 2, 0, 0, 37, 246, 0, 40, 0, 247, 0, 38, 248, 249, 39, 250, 0, 251, 0, 0, 0, 0, 0, 252, 40, 253, 41, 0, 0, 0, 0, 0, 254, 0, 255, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 257, 0, 0, 258, 0, 7, 0, 0, 0, 42, 0, 259, 260, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 261, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 262, 263, 264, 0, 0, 0, 0, 0, 0, 0, 265, 0, 0, 0, 0, 0, 8, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 0, 0, 2, 0, 267, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 1, 0, 0, 2, 0, 268, 44, 0, 0, 0, 269, 0, 0, 0, 0, 270, 44, 11, 0, 0, 12, 0, 13, 5, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 271, 0, 0, 0, 272, 0, 0, 0, 0, 273, 0, 0, 0, 45, 0, 0, 0, 46, 0, 274, 0, 0, 0, 47, 0, 0, 0, 0, 275, 276, 277, 0, 0, 48, 278, 0, 279, 49, 50, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 8, 51, 283, 0, 284, 52, 285, 0, 0, 53, 0, 4, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 54, 0, 289, 290, 0, 0, 55, 0, 0, 56, 0, 24, 0, 0, 25, 5, 291, 6, 292, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 293, 3, 294, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 295, 0, 296, 0, 0, 0, 0, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 297, 0, 0, 0, 0, 0, 0, 298, 0, 0, 0, 7, 299, 0, 0, 0, 58, 0, 300, 0, 0, 301, 0, 0, 302, 303, 0, 46, 304, 0, 0, 0, 59, 65, 0, 0, 0, 305, 306, 60, 0, 61, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 307, 0, 8, 308, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 66, 0, 0, 0, 2, 47, 0, 0, 309, 310, 311, 63, 0, 0, 0, 3, 0, 0, 312, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 313, 0, 51, 314, 52, 73, 0, 315, 53, 64, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 316, 0, 66, 0, 0, 317, 67, 68, 0, 54, 0, 318, 69, 319, 0, 55, 70, 320, 321, 71, 72, 0, 56, 0, 322, 323, 0, 73, 57, 324, 0, 58, 0, 0, 0, 74, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 325, 59, 326, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 327, 0, 6, 0, 0, 21, 0, 0, 0, 0, 0, 0, 328, 0, 0, 0, 0, 0, 0, 0, 0, 329, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 61, 330, 331, 0, 0, 62, 332, 0, 63, 333, 0, 64, 334, 65, 0, 0, 75, 0, 0, 335, 336, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 337, 0, 67, 0, 0, 0, 0, 338, 339, 67, 0, 0, 0, 77, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 340, 0, 341, 0, 342, 0, 0, 0, 78, 0, 0, 79, 343, 0, 0, 0, 0, 68, 0, 80, 0, 344, 0, 81, 69, 345, 346, 347, 348, 0, 82, 83, 0, 349, 84, 70, 350, 0, 351, 352, 353, 85, 0, 0, 0, 0, 354, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 355, 1, 0, 4, 0, 5, 0, 0, 6, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 357, 88, 76, 77, 358, 0, 359, 360, 0, 0, 361, 362, 0, 0, 0, 7, 0, 0, 78, 0, 79, 363, 68, 89, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 364, 0, 365, 0, 366, 0, 367, 0, 90, 0, 368, 369, 0, 91, 370, 371, 372, 373, 92, 93, 0, 0, 0, 374, 0, 0, 375, 376, 377, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 378, 379, 0, 380, 0, 381, 382, 0, 0, 0, 0, 97, 98, 0, 0, 0, 383, 0, 0, 69, 70, 7, 0, 0, 0, 0, 0, 99, 100, 101, 384, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 106, 0, 82, 107, 0, 83, 385, 386, 0, 0, 84, 0, 8, 0, 0, 387, 0, 0, 108, 0, 0, 85, 0, 388, 0, 0, 86, 0, 389, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 390, 0, 0, 0, 0, 391, 0, 392, 0, 87, 0, 393, 0, 88, 109, 110, 89, 0, 0, 111, 0, 394, 0, 112, 395, 396, 0, 113, 397, 0, 0, 0, 0, 0, 398, 0, 0, 0, 0, 35, 114, 115, 0, 116, 399, 0, 400, 0, 0, 0, 117, 401, 0, 118, 119, 402, 0, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 1, 0, 0, 2, 3, 4, 0, 2, 5, 0, 0, 2, 5, 6, 7, 0, 1, 0, 0, 2, 0, 8, 0, 5, 0, 9, 0, 6, 8, 1, 10, 0, 11, 0, 12, 1, 0, 2, 9, 9, 0, 13, 14, 11, 3, 11, 0, 15, 2, 0, 16, 17, 9, 3, 11, 18, 15, 16, 4, 0, 19, 20, 21, 0, 6, 22, 23, 1, 24, 25, 0, 26, 0, 8, 5, 27, 9, 1, 28, 1, 15, 29, 30, 3, 4, 0, 0, 31, 32, 8, 1, 33, 34, 6, 1, 0, 0, 4, 10, 35, 36, 16, 37, 38, 0, 39, 40, 6, 41, 0, 42, 0, 0, 43, 44, 7, 5, 45, 5, 46, 47, 48, 7, 10, 0, 2, 49, 50, 51, 19, 6, 8, 0, 52, 2, 53, 54, 13, 10, 55, 56, 0, 57, 0, 18, 0, 58, 59, 60, 5, 61, 23, 62, 1, 63, 3, 64, 2, 65, 66, 67, 0, 68, 2, 19, 69, 70, 71, 72, 73, 0, 3, 74, 16, 0, 0, 75, 0, 76, 77, 8, 10, 1, 1, 78, 3, 0, 79, 0, 80, 0, 81, 0, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 9, 0, 11, 90, 13, 3, 91, 92, 93, 94, 8, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 22, 22, 8, 1, 24, 15, 101, 0, 4, 102, 1, 1, 1, 103, 0, 10, 104, 105, 0, 106, 107, 108, 109, 110, 111, 13, 0, 112, 24, 15, 0, 0, 12, 1, 0, 113, 27, 1, 26, 0, 4, 13, 114, 7, 1, 11, 115, 27, 116, 117, 0, 0, 18, 18, 0, 118, 8, 0, 0, 4, 20, 0, 4, 119, 3, 34, 1, 0, 120, 121, 49, 18, 9, 3, 15, 122, 1, 2, 123, 124, 22, 125, 10, 126, 0, 5, 127, 128, 129, 130, 131, 132, 29, 31, 133, 134, 6, 14, 135, 32, 12, 19, 136, 137, 20, 0, 5, 13, 138, 139, 140, 9, 141, 2, 142, 143, 144, 35, 19, 145, 146, 147, 38, 148, 2, 6, 4, 149, 150, 0, 39, 151, 152, 1, 153, 0, 154, 40, 26, 41, 155, 156, 4, 157, 49, 22, 7, 158, 159, 8, 42, 160, 161, 162, 0, 163, 164, 22, 1, 165, 166, 47, 5, 0, 31, 167, 168, 169, 16, 170, 171, 10, 0, 172, 173, 174, 41, 6, 0, 38, 0, 0, 12, 175, 1, 19, 23, 13, 3, 4, 45, 0, 176, 21, 177, 178, 6, 7, 0, 179, 180, 181, 2, 182, 183, 26, 184, 27, 185, 23, 1, 0, 186, 187, 188, 29, 0, 25, 0, 6, 1, 189, 8, 26, 9, 190, 191, 2, 192, 193, 51, 194, 18, 195, 196, 197, 1, 0, 198, 199, 6, 200, 52, 3, 28, 201, 9, 12, 202, 203, 3, 204, 0, 14, 205, 206, 207, 208, 209, 210, 5, 211, 212, 213, 214, 215, 3 };

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
            final int compressedBytes = 1349;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE1y2zoMBlnWw6YbtuOFs2NfPW/euidgN50su+zSM+lBmE" +
                "0nPcLbJTdpb9KjVLZkhfqhREuUBNH4ZqLYlmWRwAcQgCDdswd4" +
                "3MFxu+H29qvZgP5s9vt3j/AT2Ic/Qj4BF9udfPjxff9sPpk7MP" +
                "/BR/b22+/t/wcgEAhNqOxP+nby8wuz/EA/ggWZjTSzf8HtTkFm" +
                "/8z8q7NdjwAHK9488feQ2b/dGKHhH7jT5jH793b3G27J/hfHfb" +
                "f//kX+GzfO9vec2d9NaX97DSwzM9A20x/n8mR/v3L7gzuyPwIh" +
                "Ocgibiigz/GDAJa9O39FHKOG0zujDAmthOgVryIhEQgEAqG+8D" +
                "p5e/NjlWftxRLzOV9/DxBj/b1/TfU3AgKwE82lw/rTK11YhNJ1" +
                "I+Eo6ldu/ePVWu2HFxshCh/Dam7JE98mEP+X9Ufl1h/3R7a91D" +
                "9O9ccvRf0RJqg/usLloeVZTU4jJVS03Vm/j2l/7fX3M/8PJ/5T" +
                "/Z2wRPHEkGCC7Xep+r3rP27a1s+78/pZnt9Mcf7G/KExf4P3+k" +
                "UU/ktP/lit3wLVb5EmQP76e4MWuTbj6U9gEMB6rj9gjLxFiHhX" +
                "BJ+M6SpKl2ZD6ocmZv1wJeRHi576a2D/DIL6ga9+10LMSv1ONm" +
                "dQvL6pjFBOnCD0jV97xh8R6lKHvW77jcK/nvrrTP1nw+3PsA9g" +
                "1ZF9MjuOmeMxRoB4Z+ENMGZOLMh+XjK7f3rIQ54suWPZj2+t6R" +
                "4SA6fXs8m0F/mfvyRElj55+Jdo/XcsTKaKhv6gqj9d6O+5RX/d" +
                "9be56s9UvyHksOUGvN7D+UJE/lwUw6lOr8rHB4gCgf2kk5bIrp" +
                "mr41aWy5EGrh325Yvcq2MRQG4d4lnp+JdW+e1KGnBZ/pz0k7or" +
                "NMSV+puBdj06h56Zp3YCubDgeR0X9GIItaRA+jnEFtF/kv4jFL" +
                "wyf1XZwSrykq3cGns8kkC0f6+Z51yLgi1CQO3zXKrDsbG4NhDV" +
                "k8aqYeQ6sUjWT47ljCayKMR4O9WXxNkWCEOTFNErP92nPT6CE3" +
                "6WmH7q8OXDDXzUGx0/MNi2pkiyqQnmpEnh6deKwPAMRWpHlryL" +
                "hLb5RtYJwSFhzOwQzDr488ICeYETkwnw4ZL5iuEsojBkdZl6up" +
                "xfBr39l+X1o03H9SO6fpNo/onr/GnD33/bMKs8skysfzN9/g0e" +
                "f7qBSnj/wgZx/8KF0EAgrMj/2Suffxhm7r+M40WkNwKYt3925a" +
                "WKXvlhT5CG9l8vx3/CtKg11SEJWuwVawTh+tfafw7t/ec6qP+c" +
                "h85yjVG0QUSZK8hC6Ga/Md4/+P6PRf1P+P0T5H9Q0qzWv30o+7" +
                "dP9xIN79+e0cVW+jess6PRvyGmiWPCHZ0lzqW1whMIhOvOCgP6" +
                "Bw0UFRkVYx2gVQQ32HWpS1SLjdrNfY0nSsrjxzj3D/Yn2xRShM" +
                "T/vEf+yqou+U9mP+tCNGtX/TuR6m92C7vuSpPrP6FN/wxy/wmA" +
                "Uv8ThWUD5deyfuUP1N4216/pgInRFrmiu+JvUbt/x8w3JRXOv8" +
                "bVWucpRG0XcoUmp0jpEiF+3NkWeurJ+BQ1/1kyi5BhsZ31iXS2" +
                "kJJ8AWEC/nsi0DrbZLmh9ZuAmLz90ERWzNmemkklysm/LIrnL9" +
                "kxJqBaU7Dm+Cl/Qqv/K3PeCeQPhCBw36dxn5+nAo8XmKxgVcu6" +
                "LWoNZznrUcfPkzcTCAQCgeIvwoyxjKkvy+LCEoKNlygTCOghL7" +
                "M2v//mQ/x39/1LPBXBEQbm3yZnncf3SrvKhywNv//pL3TmIYg=");
            
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
            final int compressedBytes = 1220;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXU122yAQBqrm0Z8Fr8+LdEd/DsLLqsfIUWhXWeYIuUpuVs" +
                "l/smSQETAwyPNtEttCDDPD8M0YZMYyQSx9qGev7ex152w1XKXO" +
                "r5Tn3kqM7e3ksv4Dvv/HHN+QCyIQLsHz3u6GsrvbfR4+1Byj8f" +
                "gwAHn9vpw43/FS7ZkSoCiqL1mys24/47u+01GxQl+EJGuGwX/o" +
                "etXL3YUirJxaZoPQlf1Be5cHCr6EwhCkAqSQPv7ne5tAaMOvvZ" +
                "QIq2crP49zBNATgdLxwznxtz9+/saA+VvV+OPgr7vQ8T+F8V9o" +
                "/cUp6oIc7thH6SKG4nBrPSOy9iT/XH+22vgJhMiZoxbmrwz130" +
                "LLCXHozekPx/qBevw22/gFzT9s/KF1qGvevgxbzn4l4sOvfkCy" +
                "N+Vf9tIJ+6jYA9Pc/B4s/tI7ie3kmxBs9yjtg+k0+9knGual//" +
                "Pl8Z19f8aiv1zxS+eL35qW69D4e8/xB/H61U0DE5BDS28E/Hyz" +
                "S4AASdM2eS2dWbas/dADYv0zl24bECR4mAEJWyX/kaGO6heUfx" +
                "K2B547tGzU/+90/m6epoW5uRyXTAsnwPr9tyKxPYLxL2WGIalE" +
                "552u1nVTyQhFchxRMAlCNPSy40/af7GN/SPdWq6ixpbG9f2VGa" +
                "7Yj/9tGCUfxs9P4x+Cqr36/hjJliRdgsjd6F9U7L9GUp26flL7" +
                "uu1T+QMMfwrnP2R/al+Tv7fe/73rP6F9lv1P9z3/6ufv1B5j/Q" +
                "Mr242GaDojgKxfxO/fS4m/T8XGa1akqvlhYG8/0z9z6Z8f9c8A" +
                "6iepnrfGfyLlh7P/OvmVjZHfMAK0F17OH3Zpv4P29zxo4n+Mzh" +
                "+V1T9k/FqIn0X6J2wbJdefmPU/LP6xavGvLn+r33+y/SP4k1ol" +
                "BW7/QZi/jvnTa/j5cevJnxzPrxKj77qq950uPny4+l3y/osM7e" +
                "VK9q7h/S9x/0mt/S8y0/jl2L+c919iJ10p/aXXb2vbP3P90xaM" +
                "X5jXH+EzKu0/ux2/S66f1e1Xlb8odtjudB7w4aJvDgrKI/kpAS" +
                "10iDNbyJkP/vyM7HkOuX4CbJ745/AfHew/ElX9UE9fwIrFAe9t" +
                "1vc/m//iyn7/fPP/K7z91Aov3Aj/4+0GFs3o+D4hlX+rhfkrC7" +
                "RnMe2TAFW/qJb/ZxJNQis+2H9MjBiqIZ5au35PgAQET1Nzfmbq" +
                "yBF4cwPef7wm6//+T+3zO823T12/ap+frqe/DXz/mYG/VfZfTM" +
                "9foPhB9Ye7g16kWZ4MC4WpvPH7eH7/09s5fk/O778O8RvPU2sF" +
                "ZP/cBlg6Jy+v2D+hWEanm5I2d/2hRP3DgExHmHCmK/efLd1E5w" +
                "tXtYN7/1k3+lm7ujGU9J/qvbYJSVWk/DrX6pW2FJkQtp36/K9C" +
                "+Uf5/GlQ076AcJbfWf9qcv8WrmiQpH8CgUBoN/7leP7mFLKo/G" +
                "nrX8v8wa1/sVjLFIXEMKV6H7sylfonEGCmMsbSi3Une+saIUIT" +
                "8uOuV/Dw8998rndrHJzhx/RShyrM0Ow5J4vIxz92+Z//1rr/bn" +
                "v++vxfuvxfr5EbKywkzafT2ZHkAMdaQECRv7Jc9V+FMnW62n++" +
                "GXplV+k/r/9oVuH3Z+riP22M6HY=");
            
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
            final int compressedBytes = 903;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUlu2zAUJVm1YIAsuPCiBbpgix6EyElylN+coEfwUWt5kC" +
                "VHkiXOw3uLRBlokn98n6QkxvICZ8AVcvbygg7igfxhDttgqp+h" +
                "hf710FL1X+XtF6fv4nop+i90lt6X7hSZ5YHR0J7kWa7j9mTRvn" +
                "T9u8qvjPlTgT6hYnledvqniNIqwf7DS3Q8/3tsFXfvO9Pa0/wn" +
                "9KL+vOQnfrQjJ7/8VY9sTRQfzxPQ/sLyRxfXLT/1mqb/tuuPNv" +
                "Jv1pUX5I/8XbP9Unj7tV+/QKJp0KFNCePvoD/kD6CNeDjKRsLx" +
                "wwTkmZQOiQ3/AwAA4CPgmLn6y/RV2TkSHXv2w/v6i9/qr34zgQ" +
                "KuH8hd4/e3fzxprz61v8xfXOb/r2+vIqjHruD01XfK/gFgPzXV" +
                "2xr5tGY9/QHlYsn2g/nHTjIP+fc96v6Fh/0TuViSxajUNgUyqr" +
                "f/5L1h+ItQ9v6nl/1PD/4nGQAATwBCCgBAc1AQAQAAbZMg8D8A" +
                "AOpF3ud3EX+ztpxK75+oZ7PLbNafeKI/FVF/9sUnpR8CTLYpho" +
                "77X/IGtWndrvErdfxrvf8qY2VD8sf6uUv+lGKI3PeDCmfWKX0z" +
                "nW7l0LSZ/VOnq5c/+AsAADvBNwSLfOLBsP5BtzHqw9b1q7eS4/" +
                "fj/Fm4+efPONfz/6Plmt35L5z+UHFki7XnD0cGhfcfoNXKbX3/" +
                "52Mpf7xujgsGYs4cYuF6FlgVWa6obVgFnwR4DUHmmyeBePJXOy" +
                "IN1r8AIDuoxtuzvPvvate/a/3Ruv20Pv4y5Jf2+QvrZEqt8Clu" +
                "L8HN7QEACIhfg0cSEyQV+8Y0N3+ucUmfQtDxdPnzOyPRn7n4fU" +
                "vGL+ov+/EO+aVAl+DTTYq0ZNV/15ot+Lr/Yz/bIfgiENf384Wy" +
                "9F+359c+e/7ry3Hw/0n7IM9/lS3rP7l7mOcyE9VFgwf7F3b73/" +
                "7OP0u8fwBw428Bzj/qzIlbt0gvVSladMCBfZVzihGXj9YP6qOb" +
                "/gHUbwCQA+RausH4g+aP5vnPRXz8OcfR3Df/icPf0rlCnaBc56" +
                "l26aEQAmASt48NXeawC5Z/uvMTFfP3TfdPLc2fspo/ueiPZsf/" +
                "Ee39LYrl/P6WxPpLHb8CvL9P5cYfTcXxL4D+dvH/cXuylN+Bdb" +
                "MB2mH9kO/zv/H87wRI3K2HXwc6SS/Gm/9u61/O9O/Hfh3m/x8g" +
                "laM0");
            
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
            final int compressedBytes = 831;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnU1y3CAQhYGwIFVesPAByE2onCRH6fIJfAQfNSV7ohpnJI" +
                "0Qf03zvsWU7CkEopvuh5AYpZ4TD76zSvnl06lw+09Q5nZolg/6" +
                "LP3DKq3cq6K1ILnD86Zhc0r+3/7Xs+3/fXhmf7X+pP7bKO8LtF" +
                "9//5NUXbLs937efpRgP5BLuDs2lfypWvwJWcO6GSeun2rG3xbx" +
                "PwoeI9b3zZ+w30T+Z2bNRMn5RncuD0AOovxPn5BlHDUvACBVYL" +
                "UVKVbFLf0bF1X82ZKPpXF60b/6n/5d4hHV1t/+QX9/1W++6n9f" +
                "6i89F/UNzAuEY3QtUQ7ACGobALHyDKD/hoO11HC7659WvmWurj" +
                "/Q/vr3Xvkq8zdh/W8e+u9tu/8VvTDqvzYDxTAemyazoDkpbsNY" +
                "JqueP+o9P9Rbf9CV+HEXf6nh81/97Jf2/NXp548S129sUZu2yN" +
                "8K+besFdyerePjV0uJwGP8CNDvQv3nSfYIhevH/BvMPP4K5N8w" +
                "YP7loB9Ctbgmg8jS/i+p938Y378AAggFoken6GN2jjfxMDVoS/" +
                "P55xzPb2Mkg0seQusYKbp+p8+V13f6/0r9a3n4v+j5M/Q9zzmk" +
                "nfjaASgc+bD+VgrHyH639z9+fqz2+/b80ub7Hw4mBNPeChhXqI" +
                "4jUWy1brm6/hzkO824bjWufgr7+ims+snlDy6KG6n711NXjkux" +
                "PxzC8cX2q0Ltb1N/PAxBAsH+JfJFlKDZwrz5D/P/kv0X0H8Zkw" +
                "NcP8Yv/G+s+F/y/RcS6r+R8fgdYf/O0cYvMev//PlbBf0ckL+O" +
                "8Yg/zeKP7Plfh3Gi29r/evzB/QMw8vxthvsPEf5ThtH3bz/f/m" +
                "b6lWn9APTATvwjEQxtcR9YGgYM3yla+S6dnPX+Sm77sX8dKCyy" +
                "+u6/HaSJVgCK5o+wzt9dl/pVyfpnAHkWAACa5E92++cC2C8R3D" +
                "8eRH/J8N9x/afC84uen/3oeS+cfP+RMP7P4jYPp7n/QJn7Z1Kx" +
                "/TPjmP6TSM7iRdL+i6AOoWNUpY55WefUz1Q/QX8j/8/OFPrn8L" +
                "KLnYPYXm3u75cfzr/MpfkXu/ePZvTgUfxv9OsHIEXez9l+ggtI" +
                "5S9fyN3H");
            
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
            final int rows = 604;
            final int cols = 16;
            final int compressedBytes = 727;
            final int uncompressedBytes = 38657;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtOwzAQnYQImZ2FWIDEwiAOMuIkHMXchKPSUFq6SItTx/" +
                "aM5z2kKopw7Pm/cZKWSCSGdf8+kWzESyv386ej8Hsm0Ph7OP4M" +
                "5fnzZtqpxD2cXCg64g3mz5DHbXElDfIzAZYh2f4J8ePz48e0/r" +
                "LzD+vT31C2fkjT/xX8IcjgD7ny5+of8dOCf20dP7L9nwvqvw7/" +
                "lOs/4F+GMEIFxbJw0fwZhc8vG8OGZWtoPB74LxDGAsEKAHrgVg" +
                "UML/FfnlnxTyR9zexrmPnvcOC/cw6LBfmvU639ZJIV4amK+RsA" +
                "ZOwfxKr7BwuZadr2omMi4w3yTOhpX+6OQuyNdE/7k+dk8YnyUx" +
                "X5hbX2yfcPNOw/S9Zfnf4/F+Xs33r9dfSvmyoKjrM//4tH/3tI" +
                "9b93I/yRCUiM6AA1AJbqN/q/4vw1ruA/px2L+7dDMJLYq4npGo" +
                "8ntfNzerh1mkUwP9Ap/+aC9RPYgn/q9V8N/pM/T2zJP9eOX7V/" +
                "cjo+dmq/8uuczPqvwP5NnP+jTqbaP4B/AKvyV53xAABU4w++kT" +
                "C+sTK9alc4FPA2XggAALBHV/tvrFz+lfsPVzz/U7r/jqrlL8/f" +
                "9NZfPP/aA//ru340yh9u8bBB/QMqwUEFOvvPqeXMi/UvnM8/4V" +
                "j/XLn5JdVfbqD/nvgLC5bfwv3H/us3+hf4LwCsbgyrjweAbvOf" +
                "N22mQHh/UR/0f/+Gduauuv/Vfv+n9fpRv233P9r97+WwnN3fGJ" +
                "2nWwoDv82n3ByLE33tDp8fKY40BXo9GPPOf9LTx0UG5a51DNwG" +
                "QPynjrew/85wwcxtjoj+GfYHABMAfwIA8O+G/GHT+tue/8Ri/Q" +
                "e+v9Z24bPw/LbC/qWr37+E/vD9DSbgoQKdwPvTWvOf0PyL+iOo" +
                "/2LB9tPOf/H+hG35AfCP9qw74/ezZNYfM/GP/NN//mbIDwBm+w" +
                "dWvn7C+gEAKIFvTlPvbQ==");
            
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
                "eNrtWwmU1EQa/qsqqXR6uByUARyd4ZoBRBER5VBwuUVuBZRDYR" +
                "VGXORWhkPuQxRExYNzAA/kFgcVGXRV1FF0FVh0Bdd10VVU1J1V" +
                "VxefK71/VSWdpDs93TM9B7y3eS9JpVKpTtWX7/+/+qsaCDC6GU" +
                "wI0EFQHWpAbToP0vVRkAFZkM0W050kjb9Mm8HF0ILdQwrMKtCa" +
                "psGD0J59Bp3oDbw79Ib+dCMMghtIkd5Py2WjAxR+DyPgVriNdw" +
                "FKHoAqejvyC8sgeXA2nENn05VaTygyHoFMbQv8yu6EhrSPNgQa" +
                "k6XQNDDcPEbWwIV0A1wCl0EbNokOgC50AdkAV8M17F3SHXqZdd" +
                "hyOoH2Yw/AENoXbqb/hBxSk3zHZkAQzmJMv5Usg1SoCbUgjX4L" +
                "50I9qA8N6CztKjYGmpFPoDnN0/8Ml8Ll9AJax/w7XAFXknXkS6" +
                "07dITuxjY6B/pBX1oEy+E6GACDYRg8DDfSujAKRutvgQY6bQUc" +
                "DHKCDqa/QQpU5a9ANahDPgvOoDfCeaQbnE/+xQ4ahdAImsAFWg" +
                "/yOelAjtPraS/yEFwELaEVGwJt+VBoBx3gKugM3aCrvh96QE82" +
                "E/rAKbgWBsL1PF8/DEPhpkA1GE43wS0wkg+jWbQRbQi3a30Qrz" +
                "GhkNaX/xoKBf4dCtHGtAFbHAqRNEy3COHGviIFwfV4lUY6YplL" +
                "sfRE47eQawt8EmiNeGn2Ne+CpZtj/iGaqTUgeSrXaMcf1e4QKf" +
                "2kNo79gOUe4zusGkYET7lrZJP4c7LkanWtXyRzl2MtA9kDWLvE" +
                "C9+yJuY+E4ra6Lee2rCFFGug+/Qj8v0W2Hf0FU4pbY98soh0cj" +
                "/LbxJH05T3tlm1D1ZnU7aGtgzOpN9bpdvwpoEOVvoh8l/6J54l" +
                "yzRx1fiLu359v50iI8N5h8Pv1Bv+gE8Mo1cB0xYofml3I7/Gks" +
                "kwDiZDBv0dZJOJLJ2kmVP4QpgCLYz6JDcUEvwiE6B9MAMm8OfN" +
                "zsivqUZ7wS/e1vzMvA0mYRnBrzsFXkD5Ivme2CLFr1Ao2FLrqe" +
                "GXAJm8CPsQ8aJ9jEnQGHOayrdcCRdiGvmFPbGZDnC3inSXiJ+v" +
                "rhS/ZL7A6wXMOcsuCTUVXsiviYJf/CRfIfgl8crTP1X84sOxpO" +
                "QX9kl3GA9YP50jf2Ma3rlD8MvCpi5ej5Y163SXgxekYDuRXwKv" +
                "lI6sLpwX2C34JUs2wh35RdMthB6nnQS/wu8o+YVn5Jd5tuAXpv" +
                "uQ6Q5egl+YJ/iVCyPNfoD20Ojgtod6T0jnrZQ91LtqS0m74Fv8" +
                "bmEPaTfemraF1vrVtA20x1o6Ba7EY9ge0u7mF+ZY+R6WPTQRL9" +
                "ocqmBbluHbXi7w4v/F1KN6H4FXYLTACxoGJgfbOHjh2cIL7eEt" +
                "wh7KPLSH8jxd8cvBy5gOOZI9R4U9tO5Ie2jhZdlDwS+BFzRnI/" +
                "TPBV5WWcTLSnVUeCl7iNeWPcTUjbijPQyukvZwt7CHNl62PUS8" +
                "Ogm8MM/+nqQ95CvpfWG+X6PsId5ra+PltocCL9pO2cMIvNAeYk" +
                "4DYOw1gRfbB3dBjUATYx7MhpmI1yzIhhl6FullzIU5gl/sdZgr" +
                "+CXfROA1ge8KNoLe8hrxQuaEgq+H8Zqn+GV9WWhbjPmKX5Lfdw" +
                "i88DzOyBTXwSv88EJ+PRfy2djXNl6432x+bOWeKI5fyh5Cs8B7" +
                "eH5E/yIaL75a8cvq2SIvv2RJm197XXkp8oj8ChzAmiPwUvwy+o" +
                "ff/I1Y/MKjjdfH0fxCezhf4tUUlsISWAD3mtVgGSzmNXga3I38" +
                "up821jJ5bcQrzaxu9XmqschM9fZc0NOfKST4Bq/lstBdYJGVmi" +
                "b45WplkXX+lct+Dd4T8t3MzX65/Gx5lNg79pBXC8XZhP+S5z36" +
                "56EkNvqK77sKm1zXnQMLpU+ub1wXfrIJXFRczRz7Gk658Ar3C0" +
                "iOavWNU6g31sNazMmz7xkhWCP0huyJtOLqT2Geqx7BNz2/3gUs" +
                "ncBRwQQgrEsaOngJ/4V4LYmBlz+/lrv6xPFfRxPBK4hfDvvJ72" +
                "6gQXQerPJcPWb1+mG/54NoqXWPRoGVso97GAMSw0vwy40XpteF" +
                "UxvkEf0XPC7sIfJL+i/tfkg3Wyv/pfAKYp8L/6WhDzJTXfYQ3y" +
                "0lQ/gv2x6mbEzJdvsvZQ+F/zKY7b8cfgl7qPQ8Ppnj77+C+GyE" +
                "/+oVaQ9xz5F4nYj0X/AEpiz/ZdtDzGmuYT9G28NAlvJfmIP+S5" +
                "6j/Bcehf86ovyXsodQVdhDqCP4xfdF+y/Ea5DXHvr5L8ce2npe" +
                "1mDZQ9yfFPaQvwdb+QdkJWwMrtPWwSZ+FPXGZm0tbIMtsFPvyg" +
                "+SVea32np4CrZra3D8lUpW8yP0FOzgH0pG9Yrg2wovv/Drclg7" +
                "36Ve+4Rzpf9KmWU9cSzim70gMX4pvZGoPdSe9OXyiITt4U+x7C" +
                "E/FJ2v9XClN8Sv3daH/CP+saudT9v8wqOLX6jn04NZil9kIuST" +
                "p1IGWvzaSXIVv4SeV/xS+tDmV7hui1+Ysvglv19w+GUMVfySpR" +
                "vKoz+/uvnxK/w7YX7560N4xp9f+gE/ftn6MD6/NBqLX+bEaH55" +
                "kJ4Zn19kui+/dkl+ZQazST7shed5PdgDu6GAFMILRkjLh2cRl9" +
                "dwP0DeJi/J35pBCgiOFMnTBPUReRX3feQ9+UW8aH0Z75LtZCfZ" +
                "FfXFvEOedfsvt96IKPlWSX2/i18zEuUXPz+U1KZXKbVSmZlAiy" +
                "z/RTyqBqQnR3vI+AeCX7we+xT59SK8ZIzSp0CGMVL4L7SH8mmh" +
                "57GGAqHnzZnCf/EPYYLil63n9Wkefv1Rpqie6/pNh185Dr+se5" +
                "Jfyh46ej5Oy4ZE2sNIPS/5NdHLL4WXL79cel5ex9Dzeg0/PS9r" +
                "PhSt5z1IT42v59kxxx66+PWy8l+kjjmXnIupQh1bT6sZqNf0KZ" +
                "gS48EgP0irU278aCFeQDXM1WVtH0Z9dXf5fouIF02JzDV+jqFn" +
                "jyXBr6MVxq9aMfX4oThPTo1fu4OXOdtleera/DLnC36hveoMNS" +
                "h+nzAO9wxqQLaNl8MvqkFrqvMlbn6R89z+i6Q78Sjlvxy8XOPl" +
                "xdH80heUFb/Isjj82p8kv2qXll8q3htnvNzOwcvFr1zBL3qlrT" +
                "f4cRHfQNSOwGxS345vYIkOeH+OwisyvuG1hxF6Y55lDxf52UMV" +
                "j/LiJeLzkfGNcsIrWXvYIhZemEoaL88bOHjJ+AZpACzwlcQLxz" +
                "TIr5GBL0lD4b8UvxAv5BdppPDCK8kvf7z0Nb545frxK/C1v/8q" +
                "G7zK3X+1Ki1eifgvf7xIluTXWGB0nIz33i74RXOMH0hjgZfgF8" +
                "mmt9Lx9Dazq8CLjiJNFL/oGF+81rrxIk2L1Rs/VyZeRu0k8Rpa" +
                "8XgpfiFuLWAJHi/G/RLSXI773ke87g8r0KUJe+E8nx5d5MbLpT" +
                "dykvH3dnwjQm+cSFRvGI2S1Bs3JaiFFkbrjeLjh1E1RMQPyWWw" +
                "1PyGtCSXhqNoXyp9GLa4aQm3Yl3ieAW+DpXRVtL4ocSrWZJ4ja" +
                "54vIhlg+34PKaWAaq2YCa5ws0v0i7hVqwvAb9+Llu8SqbnjdpJ" +
                "4jWmsvjFqrEUkk96oA8+Tq6W+BTK8fKa4JzI+IbQ88XFN/QNKr" +
                "7BXKN/WC3wUvGNROxhUvGNchh/+cfnjdkJPr2yZHip+Hy4L17x" +
                "j8+T3kofqvhh8D5Il3rDis8zOfsl4ofWc574vDt+qD/mFz/Uc6" +
                "FKtN5w68Pi4ofyKmp+OdH4oaU3ouKHMfVhgvFD416/+KGf3oiM" +
                "Hyq9UXz8EPeY8Xl2DmyVWPaTdzcxoZw261NIf2qQ62w9L+9tV3" +
                "pexTdgh4+VeCLx+Ab+8llnrj007iu1JZ1a2ifJtfI4hNxABpBB" +
                "5HoymKFVZJ1lreF4lIOXpRbD8Sifd9lYErxOV//Fusb/zcC+Ss" +
                "BroNVexa+hil/yKPgl+L9T3jkgj8MEv5T/wvNeX349ha0d5YcX" +
                "PvFsgm9VIf6LPZLcN2K+XhF4eePzil8ylY97eERBCh09X0K9sc" +
                "nSGzmReEXrjcrFK1l7aL5ZCXgND+uNfLk+aqI1X1no6A3yGhmt" +
                "8JLre8cKvKC1wAvak1ehk8BL6A3yotQbmy28xtt6g01QesPGy9" +
                "Eb8spHbyi8yltvCH4lozeCkytCb0h9GDFfCYSMQT0/nowVeOG1" +
                "hRemstz8gouxtWO9/FJ4Ycn+Fr+22HjJdxV4LRT8cvCKwZCGsf" +
                "jlh5cfvxy8wrmpWNftVrqel1/QPBa/oKN1tvCyrsJ4ySu5etnB" +
                "C9NV5bGOb41N/Phl42WV6Rz1VBgvV8+Mk8dZ8r5cTwd7yEwosP" +
                "WGM7+Mu8cesqXEWn9HLL9r4bVV4RXtv043e0jzQpW0JWEP71L2" +
                "kMzD9FyCI0DFL9wj+KXGXw5eQm/IvE4KL7BWQ+rb2IMOXmr8pf" +
                "hVvN74P78S4pdcJc422vNfslwNtpPMF/xS8ylsu63nE5pP2e6J" +
                "zy8obj7F7b+Ki88H1pZHfD6wKrn4vKdMzPg8W1+m8Xm5HoDtYv" +
                "lst6w9vDKTbHXHe8V8SmQ/Ud/4mb4j1vir9FtgR1yrsaXk9jCw" +
                "9Uywh76tPUzeJx+QQ5j6SzjvqCc+/5Gn/MFi3uXpsscrgRaUAq" +
                "9k9Xwl4vUT+YHI1TRERsyDeTyNnLTx4rXdePHUOHhJz8VrlQwv" +
                "XrNYfvnaQ/f6eQevxNfPB944XfHi1RPALN9zVUinocWbHj1eTn" +
                "T9IYsaTZZeH5bEHsbfwni9WVl4JbL+0F8fWs8/zN622vKOOKYU" +
                "8jT6aBjt2m7/xVOL919WqVola0Gl8GtVpeG1Ijl+sSPAyI9CHw" +
                "p7CHIdJM0X+hCvsh3/ZetDcjD2+ij9GY8+LHb9RqL6MM7Yy6MP" +
                "ZU5C+jBmfKNM9WF5rLdh3xCl0wss/L53x+dZUYni85ZdZd+5fm" +
                "9RxcXnE7eHFaU3ymU9gGf8ZdVqjb8kQtXZjwmOv55z84vuP335" +
                "lez6qMrgF31b8uukxZu/lgG/no/mV7LzX2b98uCXmXnm6XkVn5" +
                "fzlflivpJ+YuvDUs+nvGDpw98qJ354+tnDsowf0r/ZeOFxEP2H" +
                "ytV0N79KOL+8J9Z4+XTzX2aTM49fCi+tqppfpvL/17BJS1fzy/" +
                "SLUqzfKDhT8Dqz4huaXAHqzC9LHI57anXFo7SMhN9lb9nHo8y+" +
                "5cKv3meq/wJC/6PiG7xevPnKSP8VNV/5kvJfmtRd7vmUipmvxL" +
                "NnPoVaWip6PiXW+sPTf74S/ge3BFNp");
            
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
            final int compressedBytes = 1664;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW3tsFEUY/2ZuZu96pwJWAlpeig9QUBDwhS/AB1CCikKpqD" +
                "xUQGy1akhLJETUGHygFCREgygY/cNAJA3RRKq2VRuoAvUFSgQf" +
                "oEEjYH0Q/zBZZ2fn9nZu9667e3fd22YnmdmZudnhZn79ffP7vh" +
                "sAkdmoIRJVWYIeLPdFrdCP1rLauaqKPmJ5D2pD78MIbQR6D21m" +
                "5dtoOyubYQJqQbvZyGmoUfuUfoB2oS1oK7mLz7YQFvHeOjgJfY" +
                "q2qRkTnJ2qox3SJ5eXrGDl9bw+Ccpt376b5QWiHjd6T1VV/K+o" +
                "n5nsjVTx9kUlT2X4JuPF8xa42dR7J8tsTTCftwgvFYgan5/My9" +
                "NtZxwqt+kS3juG5SuMMddZ3roVZsBM1GTuQ8u1kjyR6iE7WX6S" +
                "5TYNL8y+D2brxz2wkhqD2bfF1H619EPb3jr2RkItWEJvOR+r46" +
                "Wq8T5qFyTyuM1uLMlxztWknqyxzFprGvGi07lok8387Rpe7hPe" +
                "6HSkxi+3eOENjvdnT34x9I4X+ZyXayHC1lwCMb72npEOHS8YiK" +
                "NwXuR4kl9QCyN1fsFlGr/gapYf4u9M5WUFVNJmsStHYR7MxTt5" +
                "PzbzC06D3qadHiTt+xCWzxf14SxfDJd0itWsdLygl1ErFc8yeB" +
                "jOgsE6XjAsyS8YDZeKEVfCVaJWAxOl2RbDdLjd1L6Pl5J9Ab42" +
                "OMVo92d5gKifw/IFMl5wIYwyxo6Fa+Ba9rwRbmDlZJjCypuk2e" +
                "+A2aycw/awDe5l1mQWqsQPogp8CFdz/DbJ/MILcQ1eJOrzjd4q" +
                "27+dlkz2sJDJC7/i41Sfknd+4QMcny0MswY2z3jjPGg1nQ2G3h" +
                "BtSW+wzPUGezaKz3W9sdlyxmTVG9LIHV2C1wTf8GpzcTLLemMO" +
                "x6uJWFQCfUw6v5odf5c//OCXJ70xKXj8Emh8aTPnGxJeXzmdS0" +
                "GBwas8sHjt1e2haf2t9EQSL3f2UIkIe/h1Ol6FtIde8MItQcAL" +
                "2entb2zs4X/e7KEyLf/8is8sCL9mBJVfDI/vUAM5yJ4/GPxSPf" +
                "LrHsGvA8XOr2jfoPKL96fZQ+U2j3gtFnj9WOx4xecFGS9lWVqb" +
                "e4V4COY+Jq4hP6lFnILFLzw0R2v4M/mVlYfJEfJnlJBj5BfyG2" +
                "E+AulQVokRf0njD3n6V46To0WlD6t8wytHz0+pJ/8k7WE0qttD" +
                "wn06PM6zv/x3V/rLAeOXC7zs7CGNUSX9/Ioe9hbfUF7V8aLREK" +
                "+MeJXnhpeqavFeVsZSPbF+EqLJWO1IxRQz1+K90iwVUIlNsVKY" +
                "K55YGuUw3hs7Q4v3csu1qbNVxfpLs/SyrK+MZSPem9LzdvFeVp" +
                "+Y9vZ0qeUw3usULxjLMo/38tZkXmaI98YGaPFeXR8qv5v5Zap7" +
                "soe01LSb1bnxK/5mZ294ih/65n8px7zyK/YAZ09vq56PjmYWY4" +
                "wVL7y0GOO9AbOHy3LX82INXxhISnPiGioYHvm4u+h5//gV+SR3" +
                "PR8rpQPIETqQtbiej5dpej7pf3VHPR9c/8s2ZjdC95cJ1wOoj5" +
                "/siW93hhcqDQa/csWLDreeX5n0RmyqQ70xzMSs9kKfX170hnN+" +
                "5fv+hht7KJ9f4v4Gj2/QUVp8I7ZAt4eJweQV0mGMM+whWe/FHp" +
                "INudjD+D4bBT8/uOcX2ZjrDJr/Fbvf6n9p99noo6n7bMn7Nvp9" +
                "Njv/S2pL/pfb+zYp/6vz5MX/0vjlj/8l63lv/pdkH5eKXdhmvX" +
                "+Il2a/f2i7m9VmvAqRCmsP/dTzaTup+1/LmUXhFg+dSJs5qTf2" +
                "S9anXQ1wCr4+pCu035dLVmaxuM1qt0n+4+UmvmGL19OaPoxPyW" +
                "c8Sr6/kZs+TPQsBF5BjEcJvJ6x3t8oJn4lSrsXXnmKEbyU5bOX" +
                "0xB+NrSH/iW6hq5CDfR5upLW07X0Obo6D/H5F/JnDwuDVzD4lT" +
                "neS9c551eoN3zl1+uZ9LyBl696PjEoPL8siDiMHzq2h+8Wuz0M" +
                "Br+y//4l+NZoeHau/39lFi/Rc3wj8UjIL5cW09dfKRN1IV4uuV" +
                "FDP/MRr1UhXvbnF92dt/NrV6jnu+r8ssOLZRu8eLuF7rXiZTOn" +
                "93jUuhAvt/qQ7nPHr7zitT7Ey6uep986tIf784jXayFeLt4Q9t" +
                "Boc7zY07CHaVrygD2/2EiveG0N8co/XvRgoexhqDe84EW/z45X" +
                "Ac+vd0K8zAn+B92a6Bc=");
            
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
                "eNrtmklLw0AYhjtJZ4pV6Z/wpiJ4FRXvKngRvQoulKr1b4miFE" +
                "VwARcQUVzOoiAKikottdbloONYY+iSWlMamy99X5imk5KU8PB+" +
                "y2SklJJfyiyx3azvO2ocq7FhzFfZnPpcYGvGfJsdZY7rxvyQzb" +
                "NFWSB2wJbkn8T2pIPSI5KQ2KbdK/jVNy/zDhle6mjyKvgPS17q" +
                "PHhVjBeLleMvfg1/uc5fN/AXJV5f0qL8VnpEtHhZOijuP2Mxfs" +
                "/v/BfW8ZA/sH2reMi2isVD/znioVP+4gk9pq9knmW5mL+0cMG5" +
                "CPxVRY4l6g27/uJJ+Msxfz2q86nMr89FrjrNmZ0gf1U1f6XAiy" +
                "i5J+QvApTS8BcpXi/gRaoGSfO3fF7slRIvNltT/nqHv0jz+zBr" +
                "ioAaQTVCmsiqNPxqcLt31erBqzISPviLHDMBXmRY1WH9kByzIP" +
                "zlHWlR0eCVZ/HK+y/RiPdfLuyNy9y/YZeXaAIvJ3mVyG7N3ont" +
                "HqgP20Tr7/4S7bb91QJ/OeUv0SG6Vf7qqmg87AQvJ+Oh6EE974" +
                "GurNeo50M51X1Z64fg9Q+8+sCLZL9cwEv0g5drXDVgksH7FFq+" +
                "Cvz4SwwiHhLiFhVD6JfdImP9cBjrh0T65ZG8J0pmx0M9UU7+0u" +
                "PIX47VF6O1tB/AA/XgGHiR4jUOXqR4TYAXKV5h8CLFaxK8SPGa" +
                "Ai9qqvj+jWm39svEuFj1y1H4i1Q8nAEvSvJ9AqsMTxo=");
            
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
            final int compressedBytes = 564;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnEkvBEEUx1XrqhIkODg7uDj4CuIiceIbcBQRYmIdJ+vRzn" +
                "wDSyQjJhMnS2yJCBmchcSBkyPhJK26tWWMNbpMPfN/SXXX0t2Z" +
                "vF+9f73qmYzjJJtoe6pZUpVcVQos8TJu2apw54dm5TmwQEy0Ow" +
                "67dmvs9v0r2ElS6xg+SyuvDvAixasLvEjx6gYvUrzC4EWKVw94" +
                "keI1JPpYXAyIfsfhEdHLJ9nuKzo7qhyxfbbut1dYVB2X2Ko6bq" +
                "myzQ69/jV/PMEWWYyPpTA/YMvf+zxsL4N8P/iFLzZT7qj7xlNH" +
                "MasNYjz25V63El4yiNcMH2dxPspH+ASP8GHooTmWqoeK1yzWL1" +
                "rrl5hj8aSrfs3LrgcvXfHl9QfMS8yDlz5eYgHxRWj9WkS+kfF6" +
                "uARe2uIrhvyQ2vsNkfD2xS0f7JdDVpNfa3jua8bsN3YWXMAHJp" +
                "l9qvTwUp3PA8sPz6CHOvMNq/VFD8XVm7FG6KFxHIPOD28RX6Ty" +
                "+TvwIsXrHry07b808OLl4KUzvj43mQPPUTIrJAvghf+cH8pC6K" +
                "E+PZRFgX+fcgNelPJDWQxepHiVgBcpXqXglZ58/pHXq7bHS51X" +
                "/fb2m7WwzOX17pPAy9z4qkB8keJVBV6keFWDly5esiZ4XnYUvN" +
                "KTb/jvo2rhO1NMtuH3hxm/fnWAl7b1y/1/gLjsdus8IjsRX3+o" +
                "beGf8hJ1ciL4+MrOBy9SejgFXtr0cNre+PwuewueM8eyHgAOTN" +
                "qy");
            
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
            final int compressedBytes = 665;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmstrU0EUxj25M3dm63/hP6A7H9htF0IV3LgRFyIi2YqLVl" +
                "t1Ux+tjVrE4lpalKAIrVXbQh8GHyuV+gRpqW+kVtyNk2m8JK2a" +
                "pL3HzNTvQO7NTObekPO738w3M1GXVC/l1WWTBI2XvR8zRvXRfR" +
                "oulQep3x6v0ZA9jtjXKD109bdLnz+gAbquLpolQQW6YWoKmjSI" +
                "X7m4t5KrMll1C7nzimO+orREX/Sobn0NQl+c+lrkpYar8YoO1c" +
                "YryoJXQ/vDO8idNwwX1CTNu3ffk7ofFS2mK0qPvfsFV33NrZpg" +
                "uetU5PxEdPNP+socWFZ3EE+6r2H7wwKy4I1mn4kXdv711BjxJj" +
                "V/+AR+o3F+Q00jcz6F09fzNPUlXkJfXPpSr1Qpu1EBfmNNjHCv" +
                "kYOgeM0iB17x+Ej5eEtZr7l6f/gB41fj/GEmqyVy50vo9eJuFf" +
                "84gix5pruU91P0BvSHXP2h3ijPUl6elqdkl8zJTtm9el7yDHix" +
                "8doUt1m/cSw+aozMxa3g9e8ibq+XV7xHb4u3pu0P483gxaUvWt" +
                "BN2P9i8nLbOe5aXD/UzVg/DENfolvvqMKzB5kLaobWghz4E2JG" +
                "vLNMdoo5vcuWPotZ8d7Vf01azFe0f7uib/kiPiHX3s6Xd2P84h" +
                "q/augP9yJzXvWIRX+4D/4wFH05XvvBKxRe+nAV/WF9fq37jSPQ" +
                "F5e+dBsDr1bwYuPVrjvS5iX6wYuN1wkGfR0HLzZeJ/F/gP/eb2" +
                "C/ktPPdy3ywvwrDF4M+joHXkHx6gEv5vWoHPrDYMav86nPv76B" +
                "FxcvMaMvuPOcOyb7lbo3aYH9ymBC9xX1VUbc6cueh0rl0WXPhN" +
                "XXb58V6Cs1v/GXK8bq46WvgBcvr3U/ASttTPo=");
            
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
            final int compressedBytes = 608;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk2L00AYxzttZpLnLOxlEQ/uimBPut627SfwIKjspezJi3" +
                "rwIr5cXPXqqhX6AUTEFyp2w+4KbbW2C4svqHep4FlYP4EwJtkK" +
                "wb7YwEw70/7/kKZJmIY8v/7neZ4kUkrJfBkT24193wmWr+wje9" +
                "PdrrFK8PmK1YPPVrC02Zdof6N7/DN7yareI/mP2Ce2KUcSey+h" +
                "v7F4l3SE9zjkFfuFiFewrne32z3nCHj1PTd4KeOl3F9P4K/J+C" +
                "vy2FNEziQ5HeZ7z4L1D1X+cr7DX1bNh8/BSx8v78U+L/hrSvJX" +
                "FZEzR3wvyl8b/Ocgf/Ff8NeU569t8NLFy6up5+VUwMsqfzXAS5" +
                "u/3qrnxV3wsspfTfDS5q9v/AHz+T2+zku8zO/yhwr8dR+8tPHq" +
                "hP5yr0a1fdm9Al7jk3stKS9R9H47zeGjnBb+6Yb5Lpa/KKUgf0" +
                "n4axz1BmUojfnQbF4kRIH5Iq+yPhQ58NLpr+EiF5EzSeH9XprD" +
                "85TZ7Zfdml5eND/LvCJ/HYS/bOHllUJ/0SE8/7Kp3qDDA48sIH" +
                "Lm9ssq5kNahL/0+cv9kHkdrjNb/UelL6cv9Oy7NMnroCMzX88f" +
                "Rb1hW79Mx2zxFzSUYxYxsIrXCcTAIBpL/6sPxfHE93uzyF8685" +
                "fyev4keOnlRTmlvJbBSzOvglJeefDSxyvqv06h/7Izf+H9DbN5" +
                "0bk4LzqjZD48C17j8RetgJfJvOj0CB3aqhXXdmCGOKruv87DX9" +
                "ry10WxxnxxW9wK38cWN/H+4fgk7iTlJYoxctf7j8L9ebtENxAD" +
                "c5T6A9CVnhw=");
            
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
            final int compressedBytes = 504;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7tKA0EYhTPJDrv/gAiCjai1RVq1ELUVH8IHEAsbS99ATa" +
                "IIWiSijeAtYRHFK1FBJPHyAgo+hGg5rkkUjQheZtydyfkhu9ld" +
                "0pyPc+af2QlNuovMd7Pyrdj5u+9nUro5VmJHtet9thEc8+wgOJ" +
                "4En1N2Xbl/WHt+xTZZwV2QdcXKbFt+q9iFRL1qUfzNrygF5SLG" +
                "0f9wVecvdvNTf1Ea/tLlL5pTz4snwUtnHir3F8Yvs3hlwUsfL+" +
                "eW+ZQLzveqeDl34KWPFy2p9pfzAF5G5eEyeBnFawW8jOK1Cl7h" +
                "rW/Ex2kN2lm9vrEOfxmVh1vgZRSvPHiFN35RAcpZPn7twF+h9o" +
                "e70M6coj1oYHkeHiMPdeUhFfG+0rrxqwTtzKmA1yVUiOr4RbG/" +
                "56EnkYf/sb5BCXeCzyoYv1LgpY8XPWK/qGn+oieV/SF46eT1st" +
                "9GONhvYxQvD7zsmX+JJmgXnar4qxn+sshfLdAuqvNlFf7yMP/S" +
                "6C8vU+WlLg9FK3jp4yXaqudE+as8jI9+ujcGNcPMQ+pXmYeiXa" +
                "+/REfDr28MquRFA5p5dTb8fLkL/bxF/XwS2kWlRDdPM5/P8Gme" +
                "4fN8Cu9TIt8f9ij/v94IeOntN9TyEr3gpYuX6EMe2tVviCEoF7" +
                "X5suI8HIa/dPkr9gxwS7Bu");
            
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
            final int compressedBytes = 453;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm7tKA0EUhnNkh8TZWvAhfBXBJoVWClrZWOoDWHmLik+gIG" +
                "LEuFhpxKgg4iUKglp4Cdh4KWxs5TiRCEFBBGeYOet/YJfsbvd/" +
                "/Of8ZyDMzJRwU9F+0+89c53SIW01njeoaO6rtGnuO+baperH+3" +
                "Lj+wmtUEl38peiI1rnXxUdMOpTi8r3d7rLPq/cKHi54sUu/JUH" +
                "L1G8usHLJa+fq2VI90C7oDja9lcv/CWqH/aBlyhe/eDldX4NQL" +
                "tUz69B+Murv4ahnZwyvEagQiilx1SBEjWpJtS0mlPjaubv/VBN" +
                "oR/664d6FsqFmzdaMxbOexn+ErV/zYOXKF4L4OU1zy9Cu1Tvy0" +
                "vwl1d/LUO7YPavUnRNSfaSOarZ8lf2Av7yuH+tQTlJZfphGSqk" +
                "Om9sox+66oe6Yp9XVAQvj/PrGMqJSpBVaCCK1xk0SHneOMf8cj" +
                "m/rPO6Ai9RvG7ASxSvW/Byx6t+fqjvbJ4fRvCXQ166Zn1ffgUv" +
                "j/vyPZRLeZ5/gL9E5Y1H8HKcN56QN8TkjWfkDTm89Iv9fqg6wE" +
                "vU/HoDL395PlZQLqSq5424DXlDUj6M28Hr/86vHP7/5YxX5h3x" +
                "UBIZ");
            
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
            final int compressedBytes = 377;
            final int uncompressedBytes = 18577;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MtKw1AQxnEHAlmcNxC7cecVvGy8PImKbtz6Ngp20YJrL9" +
                "XgQmxFrSDSUvUBFHwI9/EgRRTRKuTYfOY/kKRpFinzY2bOaZr2" +
                "CjeYErkKSz7cXb/7fOWPO2vZWff+1Pb8uWZ1f770R9NuX79vdJ" +
                "93bN8O3dCnN7Tt+Ie/5QaPt1xc/JFXCa9wXtGDJW7YX5+y8ooe" +
                "8QpZXz3m1wiZUwo3Sg5ypDEWly2JK1/Pr7j6234Yb9MPpdYb43" +
                "hJeU3gJeU1iZeU1xReUl7TeAXeL8+wX1bxcrNZ11f0jFcf/9+Y" +
                "I3M5c8x6fs1TX4Hn1wLzS2Z+LTK/Cr6eX8JLymsZr8Dza4X5Ve" +
                "D6WsUrcH2tUV9SXut4FbgfbuAl5bWJl5TXFl5SXmW8pLwqeEl5" +
                "7eAV0uv7cLtkLmeOWdfXAfUl1Q9reEl5HeEl5XWCl5RXHS8prw" +
                "ZeUl7nePVx/9Ukc/98/9WivqT6YRsvKa8OXlJe93iF8hp4AZhb" +
                "KM4=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
                "eNrt2MENACAIBDD3n5nk3MCXiQHbEeRANDmpBZ1FvgFwv6E+AA" +
                "D2I9QHAPcL39Rf/vQ/c+svX85HvtFfyA8AAAAA3OX/CgAAAO9P" +
                "AMx/APPZfAYA7Ed99yP7HQAAAAAAAAAAAAAAAAA8sAHOlcYR");
            
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
                "eNrt3MEJADAIA0D3n7mgG9iPYKF3CySvPBMBAP/J1lnP3+4HAA" +
                "AAAAAAAAAAAAAAAAAAcOMfAcD+238AAAAAAAAAAAAAAAAAAAAA" +
                "YJJ/E+BZBb7EuTU=");
            
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
            final int rows = 604;
            final int cols = 8;
            final int compressedBytes = 50;
            final int uncompressedBytes = 19329;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0DENAAAIA7D510wCDvjgaiU04VKvShAAAAAAAAAAAAAAAA" +
                "AAAAAAAMC3ASlLHt8=");
            
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
            final int cols = 123;
            final int compressedBytes = 148;
            final int uncompressedBytes = 14761;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2NkNgzAABFGTQI7CoC5XDeSEEvgF9mk6mNHalmupG/RjLc" +
                "hAa62hNbSG1tgPw5WDmF3/OIjZ9Z2DmF3PHMS0/nAQ0/rNQcx9" +
                "feMgpnXDQcwZvnDgvobWOGzrFwcxrScOYt7hHQcxrTnIaf3gIK" +
                "b1k4OYt9mfg5jWXw5izvALB/7NcLpdtxzYNbTGPikrJyx3mQ==");
            
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
