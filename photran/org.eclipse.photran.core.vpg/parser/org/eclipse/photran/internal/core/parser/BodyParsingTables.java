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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 31, 47, 1, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 16, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 0, 3, 13, 4, 106, 47, 155, 156, 157, 5, 158, 13, 6, 159, 26, 162, 178, 7, 8, 160, 161, 0, 163, 168, 169, 179, 180, 171, 9, 10, 97, 181, 182, 183, 11, 172, 184, 47, 12, 185, 13, 186, 187, 188, 189, 190, 191, 192, 47, 47, 14, 193, 194, 15, 0, 16, 195, 196, 197, 198, 199, 200, 17, 201, 18, 19, 202, 203, 0, 20, 21, 204, 1, 205, 206, 74, 22, 2, 207, 208, 209, 210, 211, 23, 24, 25, 26, 212, 213, 178, 180, 214, 215, 27, 216, 28, 74, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 74, 227, 29, 228, 229, 230, 231, 232, 233, 234, 235, 236, 86, 105, 237, 30, 238, 239, 31, 240, 3, 241, 242, 243, 244, 32, 0, 1, 2, 245, 246, 247, 47, 33, 248, 249, 86, 250, 184, 179, 185, 149, 13, 186, 187, 188, 189, 190, 251, 191, 192, 252, 181, 4, 5, 95, 6, 253, 254, 34, 35, 255, 105, 256, 193, 257, 258, 259, 194, 105, 260, 261, 106, 107, 108, 112, 262, 115, 120, 122, 263, 182, 196, 264, 265, 266, 197, 200, 267, 268, 106, 269, 270, 271, 272, 7, 273, 8, 274, 9, 275, 10, 276, 11, 0, 36, 37, 38, 1, 12, 0, 13, 14, 15, 16, 17, 2, 18, 19, 3, 277, 13, 4, 20, 278, 279, 21, 39, 23, 40, 24, 177, 41, 42, 28, 280, 1, 43, 29, 31, 32, 281, 282, 34, 30, 35, 37, 283, 284, 285, 39, 44, 40, 41, 286, 42, 45, 46, 47, 48, 49, 50, 287, 288, 51, 52, 289, 53, 290, 291, 54, 55, 56, 33, 57, 292, 293, 0, 294, 58, 59, 60, 61, 62, 295, 63, 64, 296, 297, 298, 65, 66, 299, 67, 68, 69, 70, 71, 5, 72, 73, 7, 74, 75, 300, 76, 77, 78, 79, 301, 302, 80, 303, 0, 81, 304, 82, 83, 84, 85, 87, 305, 88, 89, 306, 90, 91, 92, 93, 307, 94, 95, 96, 308, 98, 309, 99, 100, 101, 102, 8, 310, 311, 312, 313, 103, 9, 314, 315, 316, 317, 318, 319, 320, 321, 104, 105, 10, 107, 108, 109, 322, 110, 11, 111, 112, 113, 323, 324, 114, 115, 116, 0, 325, 117, 12, 118, 119, 120, 13, 45, 326, 13, 327, 121, 122, 328, 14, 123, 124, 125, 15, 126, 127, 329, 98, 330, 331, 128, 129, 130, 21, 131, 132, 16, 332, 16, 133, 134, 333, 17, 334, 335, 3, 336, 4, 46, 135, 136, 337, 5, 338, 6, 137, 339, 138, 139, 340, 341, 342, 140, 18, 343, 344, 345, 346, 141, 142, 0, 47, 143, 144, 145, 146, 147, 347, 148, 19, 48, 49, 348, 349, 350, 351, 149, 352, 150, 20, 151, 152, 353, 153, 50, 154, 153, 155, 354, 355, 356, 357, 358, 1, 359, 360, 361, 362, 363, 364, 155, 156, 157, 161, 163, 22, 13, 158, 365, 366, 367, 368, 369, 370, 160, 51, 371, 372, 164, 373, 374, 375, 165, 376, 377, 378, 379, 166, 380, 2, 381, 382, 106, 167, 383, 384, 385, 386, 387, 388, 168, 389, 390, 391, 392, 169, 170, 393, 394, 395, 156, 172, 396, 397, 398, 399, 16, 198, 24, 400, 173, 401, 199, 402, 201, 403, 202, 204, 404, 206, 31, 174, 175, 176, 177, 23, 178, 405, 406, 407, 179, 408, 207, 409, 410, 0, 181, 52, 53, 124, 411, 126, 412, 185, 186, 413, 414, 16, 209, 415, 188, 416, 7, 22, 80, 25, 28, 417, 30, 418, 419, 420, 36, 166, 421, 38, 212, 57, 0, 3, 422, 423, 1, 2, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 444, 445, 446, 447, 448, 208, 449, 450, 451, 452, 453, 454, 455, 456, 457, 65, 458, 66, 459, 460, 461, 67, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 474, 475, 25, 28, 36, 476, 477, 478, 479, 7, 480, 216, 3, 481, 221, 482, 195, 483, 484, 202, 485, 211, 68, 486, 487, 488, 489, 490, 491, 492, 81, 82, 86, 92, 213, 493, 494, 495, 94, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 513, 514, 515, 516, 182, 517, 518, 519, 520, 214, 521, 522, 523, 215, 524, 525, 526, 527, 528, 529, 530, 531, 532, 95, 533, 226, 534, 535, 536, 96, 227, 537, 538, 539, 234, 540, 97, 541, 98, 106, 542, 543, 193, 194, 544, 196, 545, 197, 546, 203, 547, 548, 112, 113, 114, 138, 54, 549, 133, 134, 550, 4, 551, 189, 552, 553, 180, 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 5, 564, 565, 6, 566, 8, 9, 10, 11, 12, 13, 567, 568, 569, 570, 571, 141, 572, 142, 573, 149, 236, 151, 574, 204, 575, 206, 576, 577, 152, 578, 579, 580, 581, 14, 38, 582, 583, 584, 184, 585, 586, 198, 587, 588, 589, 590, 591, 237, 592, 153, 593, 594, 595, 596, 597, 598, 599, 600, 601, 154, 602, 603, 604, 605, 159, 606, 607, 162, 608, 609, 610, 8, 611, 612, 613, 614, 615, 616, 617, 618, 619, 620, 621, 199, 200, 622, 201, 623, 127, 624, 209, 16, 625, 626, 627, 628, 629, 630, 631, 164, 165, 632, 633, 634, 166, 635, 167, 170, 171, 173, 208, 636, 178, 55, 4, 168, 169, 637, 638, 9, 639, 640, 641, 642, 643, 644, 645, 646, 647, 648, 172, 17, 179, 181, 649, 183, 186, 212, 1, 191, 58, 207, 210, 216, 217, 218, 59, 219, 220, 221, 222, 223, 224, 225, 226, 650, 239, 228, 651, 652, 0, 653, 47, 57, 654, 655, 656, 185, 188, 190, 227, 229, 60, 230, 231, 61, 244, 657, 18, 658, 232, 233, 234, 235, 236, 237, 659, 660, 238, 661, 662, 239, 240, 241, 242, 243, 63, 663, 664, 665, 245, 244, 666, 667, 668, 669, 246, 10, 247, 19, 20, 670, 671, 217, 672, 218, 673, 674, 675, 676, 677, 58, 248, 64, 678, 679, 680, 681, 249, 250, 5, 682, 683, 684, 685, 686, 687, 247, 688, 251, 252, 69, 689, 249, 690, 691, 692, 693, 254, 7, 255, 256, 257, 258, 694, 695, 696, 259, 260, 261, 697, 262, 219, 70, 263, 264, 265, 266, 698, 267, 268, 270, 699, 271, 275, 276, 700, 8, 269, 272, 285, 220, 221, 71, 222, 223, 701, 128, 72, 73, 74, 75, 76, 702, 703, 253, 704, 224, 705, 273, 277, 281, 706, 707, 225, 229, 708, 709, 710, 230, 711, 712, 21, 713, 23, 231, 714, 232, 715, 716, 717, 718, 77, 282, 283, 719, 81, 31, 24, 82, 86, 58, 59, 92, 94, 61, 720, 63, 292, 284, 286, 721, 722, 233, 723, 287, 724, 234, 725, 74, 92, 59, 288, 289, 60, 254, 95, 235, 726, 61, 727, 240, 728, 62, 290, 291, 2, 63, 295, 78, 296, 298, 64, 299, 729, 256, 300, 730, 731, 1, 732, 260, 733, 734, 302, 64, 241, 735, 736, 242, 261, 262, 737, 738, 739, 740, 303, 304, 305, 243, 741, 742, 246, 263, 270, 743, 248, 744, 745, 250, 746, 747, 251, 79, 306, 308, 307, 69, 309, 311, 0, 252, 312, 313, 748, 749, 750, 314, 319, 322, 315, 316, 317, 323, 65, 0, 318, 324, 1, 325, 327, 2, 326, 328, 70, 330, 331, 332, 333, 334, 335, 337, 339, 340, 71, 341, 342, 343, 344, 345, 347, 348, 349, 350, 351, 353, 354, 355, 356, 357, 358, 359, 360, 1, 253, 361, 362, 365, 364, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 66, 363, 83, 2, 67, 376, 377, 72, 73, 74, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 255, 751, 380, 69, 110, 381, 383, 384, 385, 386, 752, 387, 388, 389, 390, 378, 379, 753, 392, 393, 396, 398, 402, 403, 405, 408, 409, 411, 412, 413, 754, 417, 382, 391, 420, 394, 397, 395, 400, 404, 407, 410, 414, 415, 416, 418, 419, 421, 2, 755, 422, 423, 424, 425, 426, 427, 756, 428, 757, 758, 406, 429, 759, 760, 430, 431, 761, 432, 433, 762, 257, 0, 763, 434, 435, 764, 765, 436, 766, 111, 767, 768, 769, 258, 259, 437, 438, 260, 439, 259, 440, 441, 770, 771, 442, 443, 444, 445, 446, 447, 448, 449, 261, 772, 450, 451, 452, 453, 454, 455, 105, 456, 68, 457, 773, 774, 115, 458, 459, 460, 3, 262, 461, 462, 463, 464, 4, 465, 775, 466, 467, 263, 264, 468, 469, 470, 471, 472, 776, 473, 777, 474, 475, 476, 477, 478, 479, 480, 5, 265, 481, 482, 483, 484, 485, 487, 488, 489, 490, 491, 492, 495, 501, 486, 778, 266, 779, 780, 70, 503, 116, 511, 513, 117, 493, 494, 496, 497, 781, 782, 783, 498, 784, 499, 500, 502, 504, 785, 514, 786, 787, 267, 516, 517, 788, 520, 789, 790, 791, 268, 505, 506, 507, 508, 509, 3, 118, 119, 524, 792, 521, 793, 794, 795, 1, 4, 527, 526, 120, 84, 11, 510, 512, 85, 515, 796, 518, 519, 522, 266, 523, 528, 525, 273, 529, 530, 531, 532, 797, 533, 534, 6, 798, 799, 106, 71, 800, 801, 535, 536, 537, 802, 270, 803, 804, 269, 540, 805, 271, 3, 806, 807, 538, 539, 541, 542, 543, 87, 544, 545, 808, 547, 546, 549, 809, 810, 551, 811, 812, 272, 813, 548, 550, 12, 814, 815, 816, 817, 552, 818, 554, 121, 553, 555, 819, 556, 557, 122, 123, 125, 820, 273, 821, 558, 559, 274, 822, 560, 88, 823, 824, 825, 826, 275, 276, 89, 278, 277, 827, 828, 561, 563, 829, 830, 4, 831, 832, 833, 834, 90, 835, 127, 836, 837, 838, 564, 839, 5, 840, 841, 565, 842, 843, 91, 7, 844, 845, 846, 128, 847, 848, 849, 850, 279, 95, 96, 851, 852, 853, 281, 854, 567, 562, 568, 855, 856, 857, 858, 569, 859, 860, 129, 861, 0, 862, 863, 864, 130, 97, 101, 102, 131, 132, 139, 865, 140, 143, 144, 145, 866, 867, 103, 868, 72, 869, 870, 276, 871, 573, 570, 571, 572, 574, 575, 576, 278, 872, 146, 873, 874, 107, 73, 875, 74, 876, 5, 577, 578, 75, 147, 579, 580, 104, 581, 582, 108, 583, 877, 280, 282, 293, 584, 585, 586, 283, 284, 587, 878, 294, 879, 285, 880, 295, 286, 296, 881, 588, 882, 589, 590, 883, 591, 884, 592, 593, 594, 595, 596, 601, 597, 885, 287, 886, 290, 291, 887, 888, 889, 76, 598, 890, 891, 892, 599, 893, 894, 895, 896, 0, 897, 898, 899, 900, 600, 602, 603, 604, 901, 606, 902, 605, 148, 903, 904, 607, 609, 905, 906, 611, 907, 615, 908, 909, 608, 610, 612, 910, 614, 911, 616, 912, 617, 613, 913, 914, 915, 618, 619, 6, 7, 620, 621, 622, 623, 916, 295, 917, 918, 919, 292, 624, 920, 296, 921, 297, 922, 923, 924, 625, 626, 925, 926, 105, 627, 628, 629, 630, 635, 637, 2, 927, 928, 929, 109, 77, 632, 638, 639, 640, 78, 641, 930, 642, 654, 931, 655, 932, 933, 79, 643, 934, 298, 644, 935, 936, 647, 937, 938, 939, 940, 941, 942, 943, 944, 945, 299, 648, 946, 947, 948, 949, 651, 653, 950, 656, 951, 657, 150, 658, 649, 952, 953, 954, 650, 659, 955, 0, 956, 957, 958, 8, 151, 155, 660, 652, 959, 661, 156, 662, 960, 664, 1, 961, 665, 666, 667, 962, 668, 301, 963, 964, 669, 670, 671, 965, 157, 158, 966, 302, 300, 967, 672, 968, 673, 969, 674, 970, 971, 676, 675, 677, 972, 973, 161, 974, 975, 678, 679, 680, 106, 9, 681, 682, 976, 13, 977, 683, 10, 978, 979, 980, 981, 982, 306, 684, 163, 983, 307, 984, 308, 685, 985, 686, 986, 309, 687, 310, 312, 987, 313, 174, 175, 688, 80, 689, 988, 989, 990, 991, 992, 993, 994, 690, 995, 691, 996, 692, 326, 693, 325, 694, 997, 695, 107, 998, 999, 11, 700, 703, 704, 705, 707, 1000, 1001, 708, 1002, 709, 696, 327, 698, 108, 1003, 1004, 12, 1005, 711, 702, 328, 1006, 329, 1007, 1008, 1009, 706, 176, 177, 1010, 183, 279, 710, 712, 1, 1011, 335, 1012, 1013, 109, 1014, 110, 1015, 337, 1016, 339, 1017, 81, 3, 4, 713, 714, 1018, 110, 82, 340, 1019, 341, 715, 1020, 9, 1021, 186, 716, 717, 1022, 718, 1023, 187, 311, 719, 720, 721, 722, 723, 724, 111, 314, 1024, 342, 111, 1025, 112, 1026, 113, 344, 725, 1027, 315, 343, 1028, 191, 1029, 1030, 726, 1031, 1032, 727, 728, 192, 83, 729, 193, 730, 345, 731, 84, 732, 194, 733, 734, 115, 735, 736, 738, 1033, 739, 740, 741, 1034, 737, 744, 13, 14, 742, 15, 1035, 743, 747, 745, 751, 1036, 754, 746, 16, 758, 17, 1037, 748, 752, 1038, 196, 760, 1039, 1040, 753, 759, 1041, 761, 347, 762, 764, 297, 763, 765, 1042, 1043, 1044, 766, 768, 769, 770, 2, 112, 85, 116, 771, 772, 775, 1045, 1046, 1047, 1048, 1049, 1050, 774, 776, 1051, 777, 778, 1052, 348, 86, 87, 779, 780, 88, 1053, 301, 117, 118, 0, 119, 120, 781, 349, 1054, 1055, 1056, 197, 782, 783, 784, 303, 785, 200, 1057, 310, 786, 788, 789, 790, 791, 1058, 1059, 350, 351, 793, 796, 794, 316, 795, 8, 201, 797, 9, 10, 799, 800, 801, 803, 804, 1060, 805, 806, 1061, 808, 1062, 807, 1063, 121, 810, 809, 1064, 205, 129, 1065, 1066, 1067, 352, 1068, 1069, 1070, 353, 355, 811, 356, 1071, 813, 814, 1072, 122, 1073, 1074, 815, 1075, 18, 357, 125, 1076, 1077, 817, 818, 819, 11, 1078, 1079, 1080, 19, 358, 127, 1081, 820, 824, 1082, 207, 209, 210, 359, 2, 360, 361, 1083, 362, 1084, 1085, 364, 1086, 1087, 128, 1088, 131, 1089, 1090, 1091, 1092, 1093, 317, 212, 821, 1094, 318, 114, 365, 89, 319, 1095, 823, 825, 826, 828, 829, 830, 320, 831, 1096, 327, 832, 833, 1097, 115, 90, 835, 834, 132, 1098, 136, 1099, 1100, 1101, 1102, 139, 1103, 836, 837, 367, 1104, 1105, 1106, 1107, 1108, 1109, 5, 15, 1110, 1111, 1112, 839, 840, 1113, 1114, 841, 844, 846, 1115, 847, 848, 1116, 842, 849, 1117, 851, 370, 10, 855, 11, 12, 1118, 1119, 843, 852, 857, 20, 21, 216, 853, 1120, 217, 1121, 91, 845, 854, 856, 859, 860, 1122, 861, 863, 1123, 862, 864, 865, 1124, 1125, 1126, 321, 12, 218, 221, 1127, 867, 868, 13, 869, 866, 1128, 328, 1129, 371, 373, 13, 1130, 14, 1131, 1132, 870, 871, 872, 873, 874, 875, 222, 1133, 374, 877, 1134, 1135, 140, 1136, 878, 15, 1137, 22, 879, 141, 1138, 1139, 1140, 1141, 1142, 375, 880, 16, 1143, 382, 142, 1144, 1145, 1146, 1147, 1148, 399, 881, 391, 1149, 400, 1150, 401, 1151, 1152, 418, 1153, 1154, 1155, 143, 144, 7, 8, 876, 882, 883, 884, 419, 885, 329, 1156, 1157, 421, 333, 1158, 1159, 334, 886, 14, 887, 219, 888, 1160, 92, 1161, 1162, 220, 223, 224, 1163, 1164, 225, 93, 889, 890, 1165, 0, 226, 891, 892, 893, 894, 895, 896, 897, 899, 900, 1166, 898, 901, 903, 1167, 1168, 1169, 1170, 15, 907, 1171, 1172, 904, 902, 905, 1173, 1174, 1175, 909, 910, 913, 1176, 331, 227, 228, 906, 1177, 1178, 908, 911, 912, 914, 1179, 915, 335, 1180, 1181, 917, 1182, 918, 922, 1183, 1184, 422, 131, 1185, 1186, 23, 423, 1187, 1188, 1189, 1190, 424, 425, 919, 426, 1191, 1192, 923, 1193, 1194, 1195, 1196, 428, 430, 920, 427, 1197, 1198, 229, 134, 1199, 1200, 1201, 1202, 1203, 336, 337, 338, 1204, 94, 433, 434, 340, 921, 924, 925, 926, 927, 928, 929, 1205, 230, 231, 437, 435, 438, 239, 1206, 1207, 1208, 147, 930, 932, 1209, 1210, 933, 1211, 931, 1212, 1213, 934, 16, 935, 936, 937, 938, 1214, 940, 943, 1215, 941, 439, 1216, 1217, 341, 944, 945, 440, 947, 948, 949, 951, 442, 1218, 1219, 443, 444, 952, 441, 1220, 1221, 148, 1222, 953, 445, 954, 461, 1223, 1224, 149, 1225, 462, 957, 959, 960, 464, 1226, 344, 346, 1227, 961, 349, 347, 962, 467, 31, 1228, 150, 151, 1229, 1230, 468, 963, 1231, 1, 1, 965, 966, 964, 967, 968, 969, 970, 1232, 1233, 1234, 971, 976, 1235, 972, 974, 355, 1236, 975, 1237, 469, 1238, 1239, 152, 1240, 1241, 24, 1242, 153, 1243, 1244, 25, 135, 352, 358, 361, 470, 473, 977, 366, 1245, 154, 156, 157, 1246, 1247, 1248, 1249, 240, 158, 1250, 978, 1251, 979, 980, 981, 1252, 982, 983, 985, 986, 988, 989, 984, 241, 1253, 1254, 28, 474, 1255, 1256, 29, 475, 1257, 368, 1258, 369, 987, 1259, 1260, 1261, 242, 243, 990, 17, 248, 253, 993, 261, 1262, 991, 1263, 992, 994, 995, 476, 1264, 1265, 477, 478, 1266, 1267, 479, 480, 288, 289, 290, 481, 482, 293, 996, 998, 999, 294, 295, 1268, 485, 1269, 1270, 489, 1271, 372, 483, 484, 487, 1272, 1273, 1000, 1001, 1002, 1274, 1275, 1276, 1277, 1278 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 3, 20, 21, 22, 23, 24, 5, 6, 2, 25, 0, 26, 27, 28, 29, 7, 18, 16, 30, 31, 0, 32, 21, 0, 33, 23, 34, 0, 3, 12, 16, 35, 36, 37, 38, 39, 36, 40, 41, 0, 42, 43, 28, 44, 45, 40, 37, 1, 46, 47, 6, 48, 41, 49, 50, 45, 46, 35, 51, 52, 53, 54, 5, 55, 56, 0, 57, 58, 59, 2, 60, 3, 61, 62, 63, 12, 42, 64, 52, 63, 65, 66, 67, 68, 69, 70, 71, 64, 72, 65, 66, 73, 74, 67, 68, 75, 5, 76, 0, 77, 7, 78, 79, 80, 81, 73, 5, 82, 0, 83, 84, 2, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 16, 81, 87, 97, 98, 99, 9, 82, 100, 101, 91, 102, 97, 5, 21, 2, 103, 0, 104, 28, 105, 98, 1, 106, 16, 6, 101, 107, 103, 108, 109, 0, 21, 110, 111, 112, 113, 114, 105, 115, 12, 116, 6, 113, 11, 117, 118, 119, 120, 121, 122, 123, 124, 0, 125, 1, 126, 36, 127, 128, 123, 129, 0, 130, 131, 0, 132, 133, 119, 134, 135, 136, 120, 2, 137, 124, 138, 139, 140, 141, 2, 142, 3, 143, 125, 0, 45, 126, 144, 145, 130, 4, 10, 146, 36, 0, 147 };

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
            final int compressedBytes = 2965;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb2PHLcVf0vNCZOLA9OHKL5yznCQDaAirpxGyOjiJE4aHQ" +
                "QLiRGkSZMUKlPKAO9iBKdOhQqVCxUC3PlPOBgqDFdqghj6azJf" +
                "uzszS/L3yMfZj2Qsn3T39pGP5Pv8kZz77t0PH/3k6Tz/8dHl3a" +
                "9vvXx08/GTk/LL3//7PfPw23vlCX33oKJ/Nf+0oj8e0b9v6BX/" +
                "+1/N//S8pv/65aPXHz95OOBHdGn7iP/A5Sea0ejR/W+k7d/86M" +
                "NPji6r9Se6+/X9qzs3t+cPyeS/qTopaR/mh0Gv2nfr77vO/pPo" +
                "z9sHf/vL+0/nf37+038+fvm7V398/asnfz3/1+f/qehvv/hk9/" +
                "NTr292OT99Xq/v7OrO69vzk6D1PXT7PwD9nVI+yimj+mv3VN9o" +
                "1f27rP3HxONTfWdV9r52nm3v53/X8WXH9I3HDL8Vz8+hz69Q/q" +
                "F9Zsv4Xvbjuzd+rfM3J31S/U6wfmh8U/qHagWOVgmVqv7Tn+nq" +
                "q6mX4Ijjnzj5ydNGvrtTjK8ReZkk3q7+P734RWulpTrKKFX+0O" +
                "aHsyY/DMof3j74WZcfXT5erPKjhr7Kj5b1x6Idn9or/U2fv4+e" +
                "WebL7xEd5e+I/k1Lb/PD+01+uKLPEtCl9YW0/RD+Rct/HiIfeq" +
                "T+TZq/L/lj7TdV/y7+Pn3R0lWfjvJXKX0o/6tG/vNO/i84+TPg" +
                "D22feu1T3Pgo5fisSV/Jxx++6cePkX3Omv5Lr/2g+sHhNP1J63" +
                "B+/Q/Iz6am29Zv4H+2Lx/Fy+/Mj3JOfmvND0D8Rfldp0aKipUF" +
                "DFQJ8U9N7096Zw5S+Sikf3H+2uO3rd/xMd05ySv1Obqs+i9K+u" +
                "gfuvzy/rOZefji3lKkYX5b9PPbiv6OL/9FdEZ+LWr/+A/O8V3f" +
                "6/I3SX6N+Pv0RUsfxtcAV2mnlxT0GJ+Dj+Bv40fm+2DmCBHK6/" +
                "8NiA9MOpTf9iHtX4hZf967YelcDcUyvfkp1l0NZipzdd6fX0NZ" +
                "7R5bQXNDp/Xf86bNvP6jBgMx8aHYRlfVsLSuR6xa+3pR27+uPn" +
                "XT2BeM30jVcPyX6od3fvD4hPbFtt/csf6t/MWG/AWTDtqPso/1" +
                "B+H8DdRaRfiXJPadufXLZp9L+8/Cx39q0x+b/S/bB/qF5AMBYJ" +
                "CwNN9cdM5pld/I5l+6PoRdYJB8ZsN+/PFJuL4VXYX7Rx0fn0wg" +
                "P7v93EX3+xcC9pVMPtcHOfYznvxeiZ+7+jdkq3M30oBx/6H5Ga" +
                "q/o+Ov4fl/ZD+p81PjsHVNjvzJ3xED/80k+K8UPz3T36vKOrIP" +
                "9G01+yXRmTEzKnSR06KgwsTlFyXfY069/4bwzT7dVv9sSz4X/t" +
                "nMP0unHfmLMD9MjV9K8dUx/hnBT2H8Jce/RFcJeH30UOyR/wmW" +
                "P3D9IT4L4ocU/4X7K6nqHx1SHWo2XY7PBo5vI6j545d0fg2on3" +
                "D8C8YvKRAfJR9+a8F3x0PIenRl4d8pPQG+7Z3/iPULxP+i9Nud" +
                "37I8r7bkn9PUB/z94b795Pz8Ox5fbvBzhK9b6DTC3xH+7cXnBZ" +
                "FzrdJ+/H9Serh8n227/3ck+yPI/tH+hYVOfbp4f0a4/9G3P23N" +
                "nwnh2zD/8e1f1vhFbeFnVYsb+MUtln9T4f615+Lzi86HK9WvJX" +
                "ImP44PqH8ikfzQEZci/x3Mz8J/sPw5e/xcuhLyJ5r/1O3PSRma" +
                "LX5+9ffqR5+bEzN7vtCL/LfzIjO3LOkCO+Pgyc/AX/34u3R/dM" +
                "mvI/FpgvmF9/xocvzNWR+rngZrhSq05bPCtyrhbfjWmf62pecd" +
                "nVo6jelkpxN92nrPuve8niizzC+LdaGlVha9jh/M+ILar+fPgy" +
                "Ai+dH4pfJj/JEp32j9uOtT0Zv5+aAag3N8HvmX/GqD3yTSn9K7" +
                "vvz5UfHrm8WuL8nHj/rfmB/atC+W/Md++V36AewP+ZfKz8vi77" +
                "xlrPJDk5kqbhil87r5WdX9Dy37J2bcvmH6Vxf/jTB+C8dPb4b8" +
                "4/xUnD9y89Oct/81ju/XyL9fgPh4IdtfDt3/M4nnb9/zR6n+ov" +
                "1pqP9c+4ijI/3E+BTa/132ptbnN2iMv1rWo8xY+Z9UvnqcdX1b" +
                "NIPvy1ME0lUcfXU+wVXfo/7L7tPO9sf657B/p/6x+nc/b4b8K/" +
                "/MHb8UH55Yf2B+Xjr2Yn3+dXv+k3f+C+NLzvM18wa/WpxRVX/T" +
                "D4qK/sxU+Uk5961vgH+G+HBp3bTZHp0i+LWbvmX9mDq+i/HBSe" +
                "RP6D/g+dWY/gPXVyf0X3umP8EbcZb8OG/s63jovzIz22yiCN5/" +
                "SldfhD6h8++on5Ktf1z7GB8F9c01XRVW/V4sF/+9hpTVyEFR1k" +
                "tc0n09M/r6lDd/TX4zPD8xGB+gk6P97eHjPvwgHxwfLSyXOm3r" +
                "G1A/SvVbuD/cPSj+evAPwA/d0mp/zWcfGa4vvW5vsvHh86FJ8v" +
                "ss2j9i+qTxD91/dMFG4fnFQD6VKr8B2yeR+zeC+J+6fpp8f5n3" +
                "Yxcdrz+J8lM5vhOzfynNH7dY30fpNwzufP213v8I8m9Xkv03bB" +
                "+j86Ns/7rKv8TnZ4D8ZoCPdfEnb/r3yKcKWjEbymumZ+vzd2WE" +
                "/1JCehr/s1HNpKpvQ+Prcv0R/27tG90PwedDS7n+su3Xcr4f45" +
                "fo/THAPgPXz0jmxzY+7c9POPlfv7If+wfYil9+/P4ay/3Wbn8k" +
                "Y/Cj9zOI368j11/v+KT+P21+8CrOv4XrT5keX3Tw4/dHAvyhW7" +
                "nCUQri+wn+9sX3Y8mRPxmWfNL7Jwx+0fuB0Poy7m981OJbG5N5" +
                "3WptqQb5mx2/8tF3sP/An3+4/zxx/Jbev+DfP8nD5U9wPjP9/Y" +
                "fA+x/S8yu8/cNoVAStP17fc5l8VkG1RQ+UA9970eB7BbX4XrHC" +
                "94ru/BqjfR+djf/6+ePfL0Py+WPV5zmwL8f5HPb5ahXQfgAd7F" +
                "8z4p+XH9JR/1L8Ymp8JLr/BQ//iMrv0vHvuv3/HXoeF5/Y/uf/" +
                "fX5l8WUy/l3LL90f2/36yuL7weinMFF30QPkyy0E6fvxz/R1l1" +
                "9mzf2IorlfUS9enV9ex9QPrvtnUfggbB/wb9z/2LzfIsLPXfdn" +
                "cnf7eigfun/krw/892dY+ztGmH+F+icd5N9GyjNz1E9eeu9x0V" +
                "38/PtR4/kfjg/TXferbMbDXf99ouc75j9YOtN+ZPaP7ud59BeA" +
                "i/D+baLxxY0f3i+Uz4+Xni4+HYP61V7fJKivmf4f1VdMenT+jf" +
                "CfSPyImR/j92tPhG+x83Pp/PgffD8qGf4Xtv5w/LL9lbjzETqd" +
                "fJD/jbR+9fsfKT69+rfrfC6i+8syJn7qD3HEyc89+W3mLo+w/g" +
                "TeP7bgn7kvfo7bt+CLDvmW+KLaZ/wyQfuy+mm1YCoKn+S/v04F" +
                "zI/t/eCR+xe7ope8+eHnP474LaVj/8F5f7oYP3Xfn2Y5v5D4M+" +
                "L338/G+v3Gfz964vi1F7/fN+PYRxz+KPaP8vNrsvsryfZ/JqJL" +
                "83tp/YPpU9d3svok2f7dZPOXSD8c/kdsn2j85crDxPkHPz/1PF" +
                "ic/YL28fsBRPv/afxnDwob/X4vMb/4fL7Qf0vPD5L7/PIRJXp/" +
                "QRaaHx9UfWK5X6/r+/WOHRRr/u+mr9of+e/l/X13/zw6r/9N/5" +
                "lRmvGJ6WXs+vDGD/ANfgElOj843fm0XBYfEtgPig9eusrP2/uZ" +
                "Winb71/C7/+B/UfgSzpcPeL1wy8/cfEHCrCvkPc3B/aP8/dA+b" +
                "dMP3T5U9ND9dNw3h8UgP9G2QcNfhVFXUIV5Zo/azLCYikbeL/K" +
                "rvEB6f4Al34g9jP6Mb6fFDO/IfUxd39lqv0dIT+j/rbaj1raz4" +
                "Uj/2LjSyYu/jnm0jk/FKj/XLr2uwX0/hq0fwH2j+D7bYT4rDh/" +
                "Zb/fyGsfmcg/svBftZ/47sT388Xvt4jaHyaG/+exTb0/Sf8FrQ" +
                "6fZA==");
            
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
                "eNrtXTuPJLcRZlPcBW9tw9RCJ1/YMi6Y4AI7kpIDWrJlyE58OP" +
                "gAO9MPcOhQBriLC86ZAwUXDhQYUKafMDYUHBQpMaCf437Mzkz3" +
                "kPyqWOTMyrhO7nZr+ejqYj0+FovKKq+MVq2yanisV4+Gf1f9b9" +
                "T2d9OjD/7v1PGD6LM/2D3f/fzxi198vfrzlxc3f/3mN1+9+PbD" +
                "L553L3//33f98zdPu2v13R8fv3j/69Vn7w30d756sfnwi+st/Y" +
                "eBPptiTv9CeuBxnPEL8nd6PO/9efP/F+DPMR19n55u9oJm7sbv" +
                "SsnXvlspfzPp+P3T6y9G1+H12YTmZ0J0H5xOczz/7fhX448Pxv" +
                "FdP781cfxuO37scXf/6EP5tfOOTasuF23W1O+zba9j7dP9Y/0o" +
                "63/7RNsv+G+P+Y/6B3Q3p/uJbhfjq6P3Z6w/0foC84Py1SnW47" +
                "ntAd12AYF3+njNxdYimt+Z6Vh/gacxKfuD23ci+0+Sz6D98FT9" +
                "n+TP5mePf3txs7LvKfXkm49vH24uV8+Vt5+64cXG+XVV338Y39" +
                "ysHo3jN+P417Pxof6TjU/6PiahXmb+w15h0NUTZ/6Tf/PJdn5/" +
                "p3wfND/AXzg+XB9uzgAfW1Z5+vk/k/wE5bcJrS9P0MFN6LsA8w" +
                "L7J9IVm65rjp+nv07n/24Ov39Af8D4IGP9cfQ35p/Yfrw7LiZz" +
                "pExeAfPuaYa2AyEaopMMuR7VzSQsbiE6zXKkrfXzs9+6qP7LfK" +
                "buYfyPvh9sn+MIOSGdwpbM/in6tcD8ye2tug3bt3WYC34pSUC+" +
                "c+Pn2PhLdQI+VC9BFwcWViv3pyGS9cO8Lwjt8+QT6edy/qFUf/" +
                "Ys+am70yFDCPfo2a8m4ej0hcH0QPwUWUjh97/6g3p4bfs/urjp" +
                "/6Tt1K//5rqXH/+z8c9fPZ3aaz7/XGU6Qz4Qf879faT2VRh/yO" +
                "PTjqBLj9Ui3b92Af/Qsf3z9kh+22L4Zjc6Me1xk3c4+Eqsf4/i" +
                "56T90LZXuW6QT30nn/1EXd/pZpJfQK+/vivjy2X8P4hFJfsn4S" +
                "O0/pts/tTuX0f6P4d9uE/yl0MviQ/7e/h+b+kno7eDBDTrB7ef" +
                "9//5i+9V/cu1W9vfrZTxRexTLj789vuclp6LnxDDWqeYT2n8Jh" +
                "uffEs/K50av4afD9wPutcv5pe989p81P/sfe+Ku9aqdataf6B/" +
                "kCDrSP9v5v2rqX+17f/cdBS/wfbqs5E/AzQzdNGMfBn903b3fW" +
                "Z0NaPj/nP9Y1p8SuefzuX/OL9Ye6XeJPkn5X+B+YPxO9H4Rdan" +
                "wL5J+//AvR7bt+pSD+3bo/YF4gcj8c+l/vu522P/dIobW2/8gD" +
                "toZQf2D4rmJ4f4mZ3hP/837283svdD/FlNDRu1mvjrtZvxV+p/" +
                "WRUYn4FfQvtAHT/2fC/DDy1ybZj457F/Q9z/ycRHs/Mr1wn8OP" +
                "H9Q08iPwXhjwT8GzzPeHRPmT9n/aPx2fGb47SP27d2tG9y/p47" +
                "vr2P+CJlfbX79WVqzm8D+LcR8vcO8t0mJbVLgLsLNCmIT9jYEv" +
                "FALdHiv0EPqWD+cluIvnCEDp+T4HObSP5zy+w/Nn/w/th+R/Kb" +
                "W4F+4Njv+62/pPm78vxff2a6cH7tyOF1oz7vf3gwCJX3vXnsVt" +
                "T1V2D+9zo+PHf/Uro0fpDhk9v4zCj24wX6pbb+UsX0V239Vyd/" +
                "az9+N+T3/ru1/Qd+0jb+4ebSDo0+Hf+C0L46fZfCs4jPzVID6a" +
                "j/ZEf6rv3UufETVLijXx3Q3Z5Os68G5EdE6UP+V+T7HeYHJt9P" +
                "5L+L86PuY/ySkz8i2B9K2D+cnwT8G3w+Oo2/kPCZAL86Q+TfLj" +
                "7SYfwPxYdU/ifwDWyfDBjfUOWHNj4jf5k6Pyui76ez1X8t/f31" +
                "rH8v41/G+GW/H1d/SvVPe7duovsDAflwe/kA+LedLcn2cDqetJ" +
                "Cw/ThrfMnO7/RMfLgYPp6JX8vxd2z/D9fbdv3Z+fn02uuvpv7V" +
                "QP+l6dT9Xzfhz/z931D++Ew+sHwm31/afzdnhGfjwxT5NkB/Mu" +
                "WDJf8Uuonbhw4wvAC+hfwjI4lfgXzWtn9g/rv9HTflP0z7O2q3" +
                "vwP9Qzn+kXH+RJS/6PD4jlGfCdRXgfFflzG/2vGjor+ftD4Iqn" +
                "8D9CfJPqTq80jrA2D/QHZ+EtUngv4FqE9AqT+Trg+Rbk+kJ+rv" +
                "pM+/QP1RwH9Lx+/p+kTi87OovkSp97sKjp9XH0DN7O/7/xjxky" +
                "cL/GSHn/Z0G8BXaPhq8HzkYQkQEn6I8IVUfFNp/afsH9N+VswP" +
                "IOBjRP+XI58L+5zA/1D9H2l9HFL71CuC9yfUpxHW52HqlzidUf" +
                "+mIP5rqev7TOcTMr7vXH7S/M/Lj1x21O7/axaGH+NPRu3rm+hl" +
                "fRN2fRK/sO8IP7z/9EL8yYu/CPUvUH0dJD+3WfU/oP8Wbg/3P0" +
                "L2i7T/gQx97f3xmP8hs89mzrLugOopITLSL7B+hbA+Rm18ez6/" +
                "NvC3ITrj+1slev/a9Zlqf7+T5JcJ8qvv+/xq01F+N3x/oXzWxq" +
                "/y9+938dktrX57TJHL4qf8+IqGX4rz923sU1qq/5HGD0n5cTT/" +
                "1Wb5R11gSbmQ/1GFjr4PPL8I8dPd+U59eD4zhp+Ww8/m8qln/h" +
                "3jfCbAr8j4cbz/5JPP/xC+XPP+gpL1O+nn9+D5Oej/fx+cP+v8" +
                "XUJQi5xfMsv4hFH/H8Y3iF6mvlm9/JD0+bgC9ufWJvWL8Pwct/" +
                "9jfFjX9F8gvZh/Gl3/Qnybib//+PCftLgJ8ycI/hU4XyfzX0+d" +
                "P0zDZ0qeTwLnx7D9Sran3B+Qvr+gbvwhPd8mvn9KHL906QhjF9" +
                "9F9pdy9+fn+1umVvwnrR+M9Pt4PuVmdj6l/xBudz4lTN+fXxHH" +
                "tzsLlXe+A58vSbcXnw+h4McS/EoqP+T1j/DTsH+u7SeTf+i0Hl" +
                "Xg6+PzF0lUFfmXr9L+Bf38B/K/s+uTabg/kfavwP5KuH/6/qt0" +
                "fEXcX7HAv7GpxoL8Zan94+wfZMWXp7ofMqofRfuPwv1JxD9p/X" +
                "8pPoDi6+r7B+T7hyrF/3YKYdtuL5/DYmvH/MMus76WYvSfpuP8" +
                "z2dz+8zmDzW/hXqArsr+VH5+Pi3/P/p+4vuzFvlL5iB/qVOqwP" +
                "5Hzv4RXX4L7H8J5193f7J2fFguf7wk/5h0Rn3+0viF2L8Q5p+j" +
                "9U/KHxbk5y/513Dxyej4VPmU+Yd3D/9+Wqp/6BA+LTxfAheI9P" +
                "5eYvsoXXZ+gCk/cf8y6guA+3el+jPPvzmZfJyCP5L4rHZ8VyL+" +
                "M6L4DN8Pn+wf3C+cNX/CEc/d/HP8O7r/ifuH+pXaXue1T88P3x" +
                "8rbU/0bxyPjcX2jwD/8P2bOfsr6Pu54/as+y9y8JsIvinGf8T4" +
                "JvLf0/hmlv5k4EvBz/7j2X9G+d/4/kGO/mlxrLSY9tVV9P7I10" +
                "8p7dH9kyjsgvndMvxYml+dV9+b3r42fnnq/OiGrAl9lf4bdvxH" +
                "xZ849ueU9Rc56yun/5B9lOJTResjekn9fcb9N1kOPj2/Pxqfpf" +
                "Nri70f734W+v0xsvFJ/DeC9kL5ieXHlvPvYH2tmX0J1O8oez/F" +
                "sf8HXhStb8L9DdL4VRofqzQ+n8xPKVF/QFo/wEjtS7I+pjC/sb" +
                "b9ksVvhP2vqu0L6Kcgfkev3y+7X6BOftxCUNj3A5a7f3Mz4TOP" +
                "vhzwmeb24beXq2uQH+pylgWmOzbbZu1ccn89kV8njJ8wPQP/Z+" +
                "A3pemeVx9PGj/Kx8+ZX7n6lvXzazve+lp+3/8BuXouCA==");
            
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
            final int rows = 616;
            final int cols = 8;
            final int compressedBytes = 1531;
            final int uncompressedBytes = 19713;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWz1vHDcQnWX2DhsBQZiDlahcBSqucJPKaQzQiQ0kaWIIEZ" +
                "AfkTKlA1CCC5UpXLgUXARw558gBC4MV24C+Od4P+60t3tLviFn" +
                "eXfIAjrZOyI5HA5nHt/wiMYeXf3YwTs1kLfPgmimuz9RpH/T1W" +
                "fV2NCM0b7/7+4ptt4oIqeuG3LbvivY/bPGz8fH3IX86Uren5/H" +
                "UIPH9Ns77d+XF13/lnJF5epNYemk/r2s3rSDw/ZM/RzzZ4+/Us" +
                "AOhzVbvffnbzw+RXT7xdnj2eWyuEd0/82jq+Pb+fKcbPFE1w3N" +
                "Avqn2ppqN2BW/bz/8uzim9fL31/MLv948+Ori7cPnp2b5z//95" +
                "U9f/ew6l8qH59/98D2hfEZj7E+Bq9/ToxHBaxfiH5r71N3a7K1" +
                "Pz3yvv3+aez3w8p+fzX29/vnWPu+/df6H23or536j+wf7qPi1k" +
                "evf43vP2T/wPUhsD4j8a0W5CXNB/nhhhX/8f6A8zts+2P7eOU4" +
                "/oytT/ec6peqkuelnlP2PVFpbVapVhZUVp+Wkf8NtqyJFjLii6" +
                "x9nV/yy+XJizq/ZFfHb+fLRS+/FI31ig1X12o8nbjin7899E+Q" +
                "X4Xt/23nP5pfs2r+SN52VHZ95oPAgPTD+zNvdC/Xc8h10PZF+F" +
                "o4fr5OSRuesOrd9t5qCnwsb/5FzObTfPt07VUMfpOPv2UWHTS/" +
                "gq7G4+/NuPFC+8fjI+fKhp3pgQMA/xCdH1G3R0d0vCiqScwuqy" +
                "5KQ9/9qc3zR39n9vzlQ45a0v0P5Ee/OPW75ujHyp8s+0Xj4ysf" +
                "PmDgH4ep2v+9//Xs4uvXy5/u1e0/e3Vx++DZYtX+Y3e+cMdv2B" +
                "641Km+bvFF9dHgC2rxBTX44hrmD6x/DLzTG/p9bPT7llr8c9rg" +
                "H13hn5sW/0yBn73jv+uPT+34tBqfoZ/3maB/0fyIPgj5iQ+AXz" +
                "IU8mSuDN+Tu/NPFprLUP4R+i9LnrPivwpQYJf4y9s/Oz45E4EU" +
                "v3DxjXN/OM53JdM+t/32d/2XrP7T5z9C+EgWXwtk/Cxow2Yx/u" +
                "05X6tCkdZ1t5W8XoKTp5Xi2tbrNst75wM/vnbKkf6s9nl/W2g2" +
                "PoL1h6jzSYgcxt+07YX8/13+1XM1mn8d/L6vesGpL7D5f+Dfcv" +
                "7ZFT8sy/+S87+Qn4b+w8RPngDj51dF/OME/KoF+GZcrjb8A9Uf" +
                "OPOX7M+E/C70z73bF/HP0voOi39t+cWs4Rf7/KuQX8X1Qxk/LO" +
                "ePUf0UycMh16T81lT8YhA/qqfjR4X4hIF/RPUpfL9jJ/hwB+c7" +
                "x/lMXj+Y5njrxj/eJ/R8lQXXB2T7M+T8XETZT6Yf4qcQv4XxsV" +
                "8uHT+eX9uoT3j1B/wa4s/A+Ax82OO3Pr+7HxHKbyF+LVT/UH4W" +
                "5KdE9R+0//D+BPyRkB9i+SdK/rkk/hrv+RXxK2z+JR7/KBZ/4/" +
                "YPZn0y7v5nSP3IDxyd+0/UHvIfPPyUe/dvzvQ601+wbNr4Ene/" +
                "SM6PJj4/mqT3BxnnZ+R/AfenDvF8vl/7pr5/O8H5OjF/C+8fye" +
                "TS8xGXP9ex8zOi+9vJ86cUvyU/P26OXzLPRyqg/oHOp4Yk9yuC" +
                "66uW43/8+w/S/BffPw9/ifMDjN/C8xGnf8H5GPsH2r/gfCK2n3" +
                "//mCa/lEU1xftlZqv8VtRKP2l6GM8vGoTlwPuDMv3537/y+Ifo" +
                "fr8Mv+L63WHjo/T1R0Z95n89fya+SYdP9osfp77fPYhP7PoCyz" +
                "4l0LXc8kXx/WIgR/3j+8My/2PhJ9b+9biCKH774wPkr4X9M+6H" +
                "ivAfH7/G3c9jff8T19ec92/E9UXQvzT/7j0/CPm3Q9cP8pOp+Q" +
                "nIH7D5BxXj36nzA5Qn/n5KXHwK0p/lf6rnU3qy9uL6bdT9opD6" +
                "qDR/SutvDPzmqZ+J+et91ydSjy/nX3nxMZZf3Pv9Fr/+8PtbwD" +
                "+RnH9/Y8r7x3q69nB/+/HXwfMnuxg/qX/H+P/0+jnzK5TL8oM4" +
                "fyTH/7L+4+LzdHLIL0D8mpa/kq6PdH5w/p8AxFybZQ==");
            
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
            final int compressedBytes = 4949;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVWwuUXdVZPta6WktShAQKVAUU7SOhjagIjSR77oO8FoZHbb" +
                "VNGxOSJiHm2ZCkCQPnnH3uvTP3zqSpeZcAzWNgAfYBuLQuXEqL" +
                "2Jdx2fBo5tEKlcwEpLWkuFyyFP3//e///P8+5w6rZq+75///79" +
                "vf/+0959575s6kfrZ+tn9DHebkYntx3wmMcNiBpF0/23o+u7Z+" +
                "NoocPheqZxhP5uDcfyFl6WVRhKx0iz0/ipIfYS2K7EXpSc9+OI" +
                "oafaxUP5t9Fee4p3NJlP9jZYk4j6Ken8QHNaY5iMeXUI0f4GRe" +
                "Wc++n/vY0xg1v2p/3WUTwtROHHaBdXs08818e62ba2Z+/z9h5L" +
                "I/SD9j5mfft4u5klwP1TM5/jGcO/vy/Cb7u2Z+o2N/215jV7vK" +
                "bPv72b8Q2t9rF6TPMBd0f0BfO/vtjfZm+1H7R/aPc6XfEZ7Lja" +
                "32/Cjpc3GPr9UVPs/e4L7eAo8/hMfVmKVP5/iHOGo+Yq+zc1zt" +
                "tJ1rKy1r3+eyCaW20C4Kun/YfsRFR81R+6CbvxhF7V6MXDaUDp" +
                "qjjcvsn+eV4/A4wxmNKMrRx3Cu/8Ae46r9sn2o9TfMsw/UzpV1" +
                "cE65gv0Kfg/tX5Y1JW89mvRrLOTYh7lmH4XH/ZhJN+G2PoOd3A" +
                "p3PfUvth902YQwmZFrP2L/wkWpSQFxs+OkNKIoPWHSvhW6wkwZ" +
                "IWrSymphwd5/2j/KKDzv+mVdY1TW8b+ypuR91yQdjYUc7qc9Vl" +
                "Z388h9uKe9qqiqneCw/25fddFasxYQP1NEWfqSWdv8N10JcZzt" +
                "Zsmxkn3ee3EVOKcxQZtXMhd0j0o9P6dAWQZifZVkUGOaI/20x+" +
                "xQWU978+f0ycnUeLhzwmjADGTnuPmXs18FzgANO5C+bAb6Hs2m" +
                "cQUxwaVGI7sA58qq7Be5mk3N3tl+kHlwTvNlXescUYii7KLs3a" +
                "LUrUffS8mQ0zzfd/sVzcjOZV72Lni8g5zk6BTdi9Sh53nZdDin" +
                "U7oj7TDsn12YXeyiDWYDvD7h/EWzATgbaMDr0yvwdQhen7hyXO" +
                "MUKf5jGFdW2mOuE1bg9ckMEZo8bB/o+T3moi6rqNenQFmGeyWZ" +
                "TSsY0xxY+zDX4PVpA7w+bUAnXfSG2Ft+DQ+V1cL+7vUJozVmjY" +
                "3d3IRHByOXDdh3mzV9j9uNXIH1a+xhnZk1jZOcJ885/B63515i" +
                "wPvCG8y2exrbmWvWtKbn9cz22z57kPREWYZNoqjyXHJaY3a34G" +
                "5nWBv0Hnc5Z9vy9VaY9oDn3gExvoKOuqxdVivlDdOAyM2u1qAB" +
                "3i41jfYVusJMGSFqGk0rLHdO/8soPO8Wy7rWVbIu/96WNCXv35" +
                "yc0VjI4X7aI7ovctMj3Id7Zm8tqmonzMSvtRm1GVFEM/7DiLLK" +
                "r9VmtBfrCjNlhGhtRvN+YWk9d694jazr3yfr+F9ZU/L+M+nPaS" +
                "zkcD/tEd0XuXROumt2eVFVO2EmfjVLzVI4MT9TRJm9zCxt36Qr" +
                "IS4zRRg3/8tfG77CuDuns8w1S/sPST2/ngJlGYhVnk3fojHNkX" +
                "7ao720rOevJ9U3mzGZWik/aPC1wc2udpAGeLvcHGxv0BVmyghR" +
                "c7BvnrC0nnve3Sjr+v9K1uXnVNKUvPJMepHGQg730x7RfZHrz0" +
                "l1zWYVVbUTZrponVkHkZ8poqxyvVnXflxXQlxmijDu2+r37CuM" +
                "u+vpp8w16/qflHp+ToGyDPc6dyK9XGOaI/20R3Rf5Ba9wTldN5" +
                "laMY+/Hj9pl8BPkp+Dn1Yfgp9hL42/mwzE8O5lZ8bfa58CZBQe" +
                "/4r85Eq4+4Qopp89X3Pz60mUvBXqLyQfSN7eD+8nyQfhMTWZBr" +
                "Uf6nPoedXxn49fiH9YH49fjM9Atl8zkp9PfiG7Eb6eA493Jhcm" +
                "74qfiL8G8cz2yfQK4sQn46d9hN6+n74Y78Wfg+OXwfHHIfpPfw" +
                "c5I3mH07zAcb8Rfyf+Z+nUvMmfk0ke5Fr8E44ab/eVA8lbnMa5" +
                "yS+ZQ+YQnJib3dkdogGdPmAOtUd0hZky4H5coaTRTc95Wybr2u" +
                "fJuvx6CpRkYN6Zks7RWMjhftqjvbKsJ33y6+mjRVXtRHYEo27q" +
                "cP+EcxMeHYxcNlB/3tTbN8P9k68Avw73Tyozdbh/8rm7f6rn90" +
                "+O4Xp4tt2Dejza5+d1uX8KlGXg/VPnPen1GoP7p7rwqQr3T+Rx" +
                "F2bSDe6fcqa7f8Kav3/KPuaydlmtmLtPg3Zl7fgp+RSosRE/h6" +
                "lMr59t/xgrSS9/LgPX4ivxj/kzHei1mT+5SV8gPNnOKsXnXXOP" +
                "fLbTOkmfP5Guu7Z3xm84Lx35DEk+X6o8nc6H3ieSHfknTs/C+r" +
                "H62fRF8LeNPn9ij27FdOfmPP2JkuuzVT3Pt2SNeNw/x14VL77y" +
                "P8lqWW2OmCNwPeHchH13MHLZQOV8A+8PcD35CpzrEbiejugB15" +
                "OPkufcqnscq5f47nvh19o9zS/JuvbFeR+5nlQfPWyCtfSTGoPr" +
                "STGoCtcTedyFGbr3XCs8dz1hja+nP3NZW3TEM9d8vtXACdPsal" +
                "tpgM5vUU0qzJQRoswv67nr6ZSsa6+SdfnrU0lT901XaCzkcD/t" +
                "0c7q5pH75K9PjxVVtRPZEYyKqUDkZ4ooq2/DWVdCHGd4Hc9zwr" +
                "2XCseCVo8z11Tam6Wu14iyDELTlRrTHOmnPaL7IrfoDc7picnU" +
                "Svk+sw8iN7vaPhpwPf0J1aTCTBkhyvyynrue/lvWtf9W1uXnVN" +
                "LUfdPbNRZyuJ/2aJd288h98nP6ZlFVO5EdwWiZFkRudrUWDTin" +
                "ZVSTCjNlwPWkUOaX9TBq3Szr2n8n6/JzCpRkkGK6VWMhh/tpj1" +
                "lS1pM++TmlRVXtRHZkWvIbieJvPdIOv9o/EclvI8LfYsj7nX6v" +
                "Keu5nyafk/Xt0/R+p593b/b7FnAz8P/7fQt2K3KL3uCO8m1F1e" +
                "LvW/L3u+lmOpyYm93ZTacB6GmqSYWZeo3khIuK63Ba0NYtzDXT" +
                "O7OlrteEmpqTHtaY5kg/7ZE7Fz3qvcI5faKoqp1odbPEwE8tPF" +
                "NEmV2Bs66EuMwUEe69LOFY0NYnmGuWdB6Sul4TampOeo/GNEf6" +
                "aY+N68p6RW9wTnMmUyvlm8wmiPxMEWXpvTjrSojLTBHh3ssmjg" +
                "WtPcNcs6nzJanrNaGm5qT3aUxzpJ/2KN1Cj9obXO9zJlMr5afe" +
                "bKTHwhzW/Mw5xun9HIfM9Cgz8lOa1AOx0qE36xtfIrXJPeHnKl" +
                "oTKg+UXYfr83wQxhDN7nP1QT+GXDwkFUAHFT6oVjJKGl30XDTU" +
                "bZ2rD/l1Wink+tU5Fvjg3oHHoe69lE/xJy4H3bkE2j5fb9ZD5G" +
                "eKKLOrcNYVjadfkZUUEe6vjvUcC9rYyVyzvvOU1PWaUFNz0kc0" +
                "pjnST3ts7CjrFb3B827uZGql/DiMEZrtOh9RDWLg5BWIjyv8uF" +
                "rJKOSKNeJ6MDqi13a+FfQZ8eu0UthjJH08wAIf3DvwONLV44jy" +
                "OZLvbkR0xDPXfL7D7IDIza62gwZcT582O9KndIWZMuA+U6Gk0U" +
                "3PvY63ZR28Pu0QpNg57EGK6T9oLORwP+1RugnXf+6ruqbfKKpq" +
                "J7Ij4z6nqM5qLIw/V53VWlKd5T5XuQVrGMNPZbPo/quxuPNZrM" +
                "avyN1Y40biy91YdRZ+pSrG8Gy7gRyhnr7vpPvMeD/WNdbZ7ZRv" +
                "ljs9VKvOSr+t7xKpj/+kZy/dZ7IGfSXfnHNXwugrPVofp4g+V2" +
                "ksCj5ZOaDuMw8Y/KzBze7sDtCA1fN45gozZYQoaXBcnSd6zts8" +
                "Waf7EKYrYQ/Mq/PS72pMa0lv7bHojPek98qexCXvQGv7vGqqEP" +
                "mZIhzVadVpppoek0qIyxrOCfen7StwX5CjqMcDnnd5Xa8JNTUn" +
                "PaUxzZF+2qN0E65/3qm+6fBkaqX8sDkMkZtd7TAN2NcinO1GqT" +
                "BTBnIEJQ2Oq4tEz53TIlmn+xCmK2EPzKuL0uUa01rSW3ssOuM9" +
                "6b2yJ3HJO9DaPo9NXHmJ5iiiiDKMdQX4sWTM0ihpaD2qko5ey3" +
                "Xqg0MqEkmPykvpf2gs9MG9tUe9E+FxH9or744ZiIlnrvl8uVkO" +
                "kZ8posxegbOuhLjMFBHur8rlHAvayJgr64LnXQHTyuDmN8J1Ia" +
                "79UdawZb2iN/D0zcnUSnkvjFGa7e0+ohrEwMkrEPcqvFetZBRy" +
                "xRp1PRgd1Wu57vuM+nVFTHqM2t8MsMAH9w48jnb1OKp8jua7Gx" +
                "Ud8cw1n881cyHyM0WU2ffgrCshLjNFhPvv2VyOBa31M1fWBddT" +
                "AdPK4Oa94boQ1/4ok26hR+0tipq3TqZWyrfDGKMZKhRRDWJdgX" +
                "i7wrerlYyShtLzVdJRa7nu+4z5dUVMeozZ5QEW+ODegcexrh7H" +
                "lE/Z75joiGeu+Xy/2V95nWZ4XXMRZRjrCvD3S8YsjZKG1qMq6e" +
                "i1XKc+OKQikfSovG5v1Vjog3trj3onwuM+tFfeHTMQE89c8/lG" +
                "s7HawzNFlGEM75x5BfjuIZnmO72N/jnSSxjVGBWu6GCVRlFZBm" +
                "LVHrtSY6GWeMFIdlPkog5x8fd3pDuZWinfbXbb2M30e87dNOxA" +
                "tW7gZwicaQDfPfQIUZyFVaXfOe/munC1Dv2eE7llzDMS1LKf0p" +
                "jW4qodpMjuChnWCo/7uO9myp5sW+8w7J/nd5g7Kq/RDNehiyjD" +
                "2N4mFeDfIRmzNEoaWo+qiJJeyOY+OKQikfSovGbXaiz0wb21R7" +
                "0TjtIj3If2yvtlBmLimWs+32l2ViZohnUuogxjXQH+TsmYpVHS" +
                "0HpUJR29luvUB4dUJJIelQk4J4WFPri39qh3IjzuQ3vl3TEDMf" +
                "HMNZ9vgTFBs/2sj6gGMXDyCsRbFL5FrWQUcsWic2J0Qq/luu8z" +
                "4dcVMekxkb0/wAIf3DvwONHV44TyOZHvbkJ0xDPXfL7MLKuM80" +
                "wRZRjDiecV4C8LszAn3P2tiNejGqGkF67mOg6uCCZOkAXXk8JC" +
                "LfEiHvVOhMl9SFOcltWKeXIg/VByMIrk73rynxFvkHvA9Nrs0/" +
                "J3PYn/K7hsLXP4/5XlfzFT5/XZGtHLNkWT/ENu/IZb+TZQuzTb" +
                "Ev4GFNb+dXZbfCL//xa742dhxVj+SZH7/2nZdnaQbUbFbGtyXv" +
                "o9+Pop3Sdbn21kr9Ub4vHsdvq7HrdulV//p9kG/LsedRc8z8yr" +
                "XsUzRZRhDEp5BblhFuaEu+5ej/URtXuEy6tJnwZXBBMnyMq+pr" +
                "FQS7yIR70TYXIf0hSnZbVSfre5u3o1zbDORZRhrCvAv1syZmmU" +
                "NLQeVRHVSszmPjikIpH0qF6dfV1joQ/urT3qnQiP+9Be2RMzEB" +
                "PP4txFGYxhmu1eH1ENYuDkFYgzhWdqJaOQK9aw68HosF7Ldd9n" +
                "2K8rYtJjOPvHAAt8cO/A43BXj8PK53C+u2HREc9c8/kxGOM02/" +
                "0+ohrEwMkrEB9T+DG1klHIFYtexxkd12u57vuM+3VFTHqMN6YF" +
                "WOCDewcex7t6HFc+x/PdjYuOeOaaz1ealZUzPFNEGcbwjpBXgL" +
                "8yzMKccPd+5/WoRijphau5joMrgokTZNVf1FioJV7Eo96JMLkP" +
                "aYrTslopv8vcBZGbXe0uGvDcXUw1qTBTBnIEZT7F1cWy2r0+LZ" +
                "Z1WtW9fx0M++iBeXVxY7XGQg731h6Lzihyf5+Zd2VPWlXvWHYE" +
                "Y5vZVp1NM6xzEWUY6wrwt9nDnDFLo4Dj37EqPaoiqpWYzX1wSE" +
                "Ui6VGdnZzRmNbiKvmV7t08ch/aK3tiBmLiWZy76E5zJ0RudrU7" +
                "aYDGAp65wkwZIUoaHFcXiJ47pwWyTvchTFfCHphXFzSPaExrSW" +
                "/tseiM96T3yp7EJe9Aa/t8oVlYeZlniijD2N4nFeAvDLMwJ9y9" +
                "Pnk9qhFKeuFqruPgimDiBFn2CxoLtcSLeNQ74Qh+Dn6ZuagpTs" +
                "tqpXyFwf9T7meKKLNHcdaVEJeZIsL9VbmCY0F7HmCurAs+9y1g" +
                "WjmKGq1wXYhrf5TZI2W9orcoan5nMrVSvtfshcjNrraXhh2ofY" +
                "FqXMFIMqnpiPhlPXdOn+++Lj+nEqb7Nh/VWMjhftojuu/eS3dt" +
                "3lpU1U5kRzCapgmRm12tSSOKavdRTSrMlBGizC/ruXO6v/u6/J" +
                "xKmO7bfExjIYf7aY/ovnsv3bW1qqiqnciOYOwxeyBys6vtoVGd" +
                "Up1CNa5gJJnUdET8sp57zZzSfV1+TiVM920+rbGQw/20R+lW7K" +
                "W7tm4rqmonsiMY95p7IXKzq91LA64nX5MKM2WEKPPLeu56urv7" +
                "uvycSpju23xGYyGH+2mPtUl76a6t24uq2olRJ1CbWZsJZ+Jm91" +
                "u2mTSqU6tTazPtk1LBSDKp6YgUynrueprafV3+/6hLmO5r/15j" +
                "IYf7aY/STbj+/1Grrj1TiqraieyoNtP0mT44MTe7s+ujAe+Z1v" +
                "Q136srzJQRoqTRTc9975Z3X5dfTyVM922e1VjI4X7aI7ovcv3f" +
                "q6iurR1FVe1EdgSjZmoQ+Zkiyuy3cdaVEJeZIsL9nmscC9pqMV" +
                "fWBfcFBUwrR1HaDNeFuPZHmf1WWa/oDa6nqZOpldT/D5ZWyfY=");
            
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
            final int compressedBytes = 4461;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVW32MXNV1XzANqfPRAKJENsgU2ipRQ6tItECakrv7dtwi29" +
                "iFRE7UmiJZIo5NjWTVbVeQ6r15s7sza4QLUdo0XRdMZFqpf/SP" +
                "yPb6I4SPkBiMQ8CGAIWWhIYPk0K9GEyCgZ57zz3v/M69M2uzV3" +
                "PfOed3Pn7vzJ15787MurVu7dCQizNLrLVec2tbr6HF4jqzxDj/" +
                "iUVwL42+JL4aNwR/KYaZmQ3GWRz5sabVLEfkNjQ0+R+DsmX6Fe" +
                "4KkuLMEmv1I+6K+lG0WFxnlhiPXK4QWdHRl8VX40yfEgwzE5vH" +
                "bJzFkR9rWs1yRG7Up5lB2TJ9tVtNUpxZYq31Uz+jxeI6s8R45L" +
                "JaZEXHd4mvxpk+JRhmZjYYZ3Hkx9r4TJ4v5UZ9+s6gbJk+5aZI" +
                "CnOwTfGgZ/AQ29QinjrqDYiKf54v9OkljcOsTZ8yDOvWjyNmfa" +
                "Qechx/cVAtrNqdl2ZFJnpGNMbcGElhDrYxHvQMPs82tYinDuoT" +
                "oOJffzXN56WJL2kcZm36lGFYt/U2YtZHaiNHz75/rcD7prieFq" +
                "ZZkYmeEY2e65EU5mDr8aBn8Meu15lGi3jqsCjnQHnyNwUdGho5" +
                "XeMmL9C4pk9ZTtVHTp+8EDHrI/WQY/1Enq+9Veo062lRmhWZ6B" +
                "nRqFxFUpiDreJBz2CXbWoRTx0WFf88X+jZ9v5xTZ8yDOtOXoaY" +
                "9ZF6yNGz718Lq3ZXplmRiZ4RjWvdtSTFmSXW6qf9jBaL68wS45" +
                "HLtSIDekB8Nc68jycYZqY+Dds4iyM/1uqn8nwpN+rTU4OyZfo1" +
                "7hqS4swSayOf9jNaLK4zS4xHLteIDOij4qtxpk8JhpmpTyM2zu" +
                "LIjzXPPvVNuVGffjYoW57d/rVmRaqfRfv4csbKn7Pe7rVnxlcI" +
                "2j7fxra/3cQta67B9wyd1F9rtr1o/KrUOlnMFVMuSC31M+n59P" +
                "vrvthkOBLYLkG0jVfjM92ZMovuR0F/bBOLl6xmdcYxq0Z7yeez" +
                "0eqf1sEROU/bOIsjP9a0muWI3KhPr6RZkQlmb836jvPM/edR3z" +
                "x6D9sUFwljRLPPXZov3I/fI7ESVw7bdYQY+ob11LL1LF4uUH6M" +
                "aTXMbLkNDfXOSLMiE/H0R7fZbaaOhTn0bjOPYn4x320O9wXR4i" +
                "XV1IYSZ8jzhfU0X+PovmCzIrZyvxrUpwsRsz5SDzlqNfWN9wVQ" +
                "tXd1mhWZ6BnRuN5dT1KcWWJt9F4/o8XiOrPEeDzn60VW1Oez0e" +
                "qf1sER19ONNs7iyI81rWY5Ijfq08pB2TKd/kiKM0usjX7XuYlP" +
                "oMXiOrPEeOTiRFbU57PR6q+emBN9Jl6zcRZHfqxpNfWN6wnqTq" +
                "welC3Tl9DY1swsBa1+LuiNhfyXJJrRGQ/HbWKJXkuCTXMvGR8z" +
                "9m0+Lsmsw+QUm+JNlW2GI5wJSFLHH+nRu2tgtlTf4DaQFGeWWK" +
                "tf9jNaLK4zS4zH7pBl+CaN9tLwtPhqnFlPCYaZh2+qDts4iyM/" +
                "qZ7n0zrN6+7pQdky/Wx3NklhDrazeVCfDrNNLeKpMfUG1RnXLM" +
                "M3anQ412+Ir+YxfUowzDx8I/XJxFkc+Un1PJ/Wafr0XJoVmWB2" +
                "d5u7jaQwB9ttPIpzi3PZJhYveRsOi3pcvTAfXe3OxViMa/qUYa" +
                "rT+/gexKyP8NcHMk1rCU//N3VamhWZSEyQvuy+TFKcWfKjuLi4" +
                "2Otq8ZK3qYb+7FlcHO4ALhaL4LQ6v6axGmfWU4JhZrojXGzjLI" +
                "78WUuZWkbMk/r0wUHZMv0OdwdJYQ62O3jUNxeXs00sXqqnRVMb" +
                "SvUWf/SRNl/o3uX945o+ZZjq1Kc/Qcz6CH99CId+tRiNffpUmh" +
                "WZSIw/tp/R+1g/V809cjWS7o/snbvZCYX9XXvOHdzwlhPt65ps" +
                "i1Js/LXqcD8uYsv3d+Ovnnh3R326bLCHPVP3TfdNmUX3o7i0uJ" +
                "RtYvGSt+Gg93FAPa5emC+sJ4jFrMjEYqrTelqLmPXRs5AHMk1r" +
                "Cc/w2eFQmhWZSEyQNrqNJIU52DbyKBYUC9zG9rfU4iVvw2FRj6" +
                "tXWBl3Ccr5ZLTv1LimT1lO1SnTk4hZH+GvD2SqvvE+M/IM6+kz" +
                "aVZkIvz8cdA+mO6f17dm/f7uZPbBumMOn2l8VSzdb8A+eL36d/" +
                "/h/e6Du//4PvfB6/N83Kewy4+fj0/9/Unvg7c4/94b5tC7LTzo" +
                "vG5wW7r/hhbx1GFRztEvX+jTDRrX/VeNa9ZTlhPr1usQsz5SDz" +
                "lqNfWN6wmqTv1zmhWZ6BnRWOPWkBRnlvwoFhYL3ZruLrV4ydtU" +
                "Q/+Qj/DwTrRQLIJ7m8ZS3hmNavpkMuuIa3QdYugjscKftZSpH7" +
                "FPa4Qn9en+QdlSvbyvvB9fM9Wi8lFax08Qs2Plj8Oa/k96hTwf" +
                "pBfbny9/Thr5Vr9RHg22X1ZD1Wkx9oOSp/pIddbwWHK9+5fg/1" +
                "z5k/KndPyf8iXyuwBfddW86ldC9Ifo8dHq16tzynvKe0PsWPsL" +
                "8TOrx8qD7F8+SY9nyfN8Oi4oD8fPJY/FamPV/HD9DnfU5Q/K/e" +
                "WP8uv3po+JXh4p/y+/3lWnBj6/Vn3M3epupY6FOfTuVh7FecV5" +
                "7tb6F2rxkrfhsKjH1Qvz0Xo6D2MxrllPGaY6PWu/RMz6CH99IF" +
                "P1jesp8vR/m85JsyIT4Rek2tUkhTnYah7E7Djb1CKeOiwq/iyP" +
                "H9Ho8Az/U/+4pk8ZhnVpfweY9ZHayHH81UG1sOqmq9KsyETPyN" +
                "V+dU2un/xL2kGtm7wer2z1O/i5r0j+ewS8up3oc1/9G51G//x6" +
                "p1H59Yyud/flnyhrRb7eIYfR6UEsB3wTcSTH26/A9e52dzt1LM" +
                "yhd7fzoD69yza1iKcOi4p/ni98JvbR/nHNesowrNu9HzHrI/WQ" +
                "48RHBtXKq2JWZKJn5G5vvd5/UDdPa73emU6tJ6PT/VOUBW/T+/" +
                "PE2epH90+NR1wHrw9m4h90/zRH3fY8tfGR7p8yX//+xFJ9E/LD" +
                "bCkX0fl6V36vfCCuvx+Wj5aPhOvde3K9o8fz5c/89c6/7spXy9" +
                "mS4suj5Zv59S5KdL2L+faVD5UPh/V0Tvnf6fXOrPx3/fWuOt1f" +
                "76oP2+vd0FDnL8oHywPkRde78hAdHy+fLJ8pny3/K6Cr/PWu/F" +
                "++3lUfqH519IZqfnVmdYa/3pXf1+sdaU+VT0PVF8qXaX6Fr3fl" +
                "G8H2VvmL8nj5TnUKXO9ucbfQygpzWGO38KC9z0VsU4t46rCo+O" +
                "f5gvRw/7jmFZBhWLdzCmLWR+ohR8++f628KmZFJnpGNK5z15EU" +
                "Z5ZYaz3jZ7RYXGeWGI/VrxMZ0B+Kr8aZ+8wEw8zMBuMsjvxYY/" +
                "+cI3LDyDRbpq9yq0iKM0usdea5Vd3H0GJxnVliPFZfJTKgPxJf" +
                "jTN9SjDMHO7HTZzFkR9rnVPzfPH+ydQdlC3TF7vFJMWZJdZGfs" +
                "ct7h5Ci8V1ZonxWH2xyIA+Ir6Ud0btGGNzog/1CTD00XrI0bNP" +
                "fWOfTN1B2TJ9k9tEUpiDbRMPWk/nu01hH9xYxFOHRTlHv3xBer" +
                "x/XMM4w7Au9Qkw6yP1kGP9VJ4v9imrilmRiZ4RjWFHd3sys8Ra" +
                "60UX7gPVYnGdWWI8Vh8WGdCD4qtxZj0lGGam+4JXbZzFkR9rnn" +
                "3qm3LDyDRbnj2+T26PO9g/biw3K6b3urQP3j7wc9971Tf/c49N" +
                "vj3Hp77bNbK9aPK9FO8tnTyOPl5qeG3PP/dt3dx+Uzy6po6VRe" +
                "ff9QxiFO9NbulMlQ8Mrw99an6jUNEecCR8/lLPi3vw9Xz/NKhP" +
                "46dUX6v+dmCfDgGHF0LeD6ulurF8N3DZ1O/zcWZSHqhPbe57Hq" +
                "dH/I1T9TfMHf8mPh6QM9Ic1V/Db6ROqb5evjCoT3T/tKbPnmoP" +
                "6v7Ruag12/t2sOxpzervxLzGfv53Yun+jvOoDX4ndm++v4Md4R" +
                "6fV/ZT41el+7t6XeKzh3kxv3x/1/kUs0n3d3KeIovO+zv8nRhX" +
                "k+jiWHGsS71s7eqGz1+87mfq/oLiWO+7Xm7tKo4xFvq0iz3EJh" +
                "LjrV2aJflF3H3q37ubj+rV2uXzSs7urODiEfoEPl4SJmplDmHt" +
                "LGA2KUvmJzVVL4+kjLmaRLvlbnl4VSyPr46g+8fIx93y3iGW1c" +
                "d/XsAWsaEkXiKbPt3v35/En4/WSzNMvie4VK/XTR5HH+Vl68nR" +
                "sxe9O5TXyj8vyHFzjivcCv+5Cs3hcxWv+5lW7u+5Fb0nWHbhl5" +
                "h+Dn1aIX58VEm8RDZ9+oH69+7mo/VKM4iHf/j1hD7Ky9aTY+d3" +
                "NReynKNPGW7O8Up3pX/d0Rxed173Mz0jC/3MskqhT1eKX73BHw" +
                "VFf5ZNn14WL/FIvTRDd1Zwqd572/ooL1tPjsheq87ZpwzHMyve" +
                "KMIeuTUTX99B94/WTxgp3mjNiBTen2bYQ2wixagZzZJcsfb515" +
                "3481G9WjM+r+ScfE9w8egdp/sC8PGSMFErc+BqzMbPXWAp5ymy" +
                "6PT+lDDmas05zhb0ns5zsM3yoJX7abapRTx1WFT883zhSn1h/z" +
                "j5yzGs23sHMesj9ZDjxAWDauVVMSsy0TMqZt0ytyy8Py2L70+k" +
                "+5n69AduWfjdYdR5Dq+7ZeLHR5XES2Sznh5Cfz5arzSD+lKXP2" +
                "l9lJetJ8eRhzSXZJv4rTlfdxljPLPireKtcF+wN94XkO5nqjTm" +
                "Zy+39ooUXnd72UNsIjHe2qtZku+ljqI/H9WrtdfnlZx0X/CW9Z" +
                "j6kPXxkn/UH+Ajcgh9GmM2KUvmJzVVp9ddwpirSbRb6paGni2N" +
                "PQy6f3QuVST1ET8+qpSipk9voj8frVeaQX2pTxdZH+Vl68mxcw" +
                "lyzmv12S8szS0aXRwt6HnmOfTuKA+q9Fm2qUU8dVhU/PN8YZd1" +
                "Qf+45p0iw7Du1GcRsz5SDzl2/nBQrbwqZkUmekbFUXeWO4s6Fu" +
                "bQu7N40Mpdwja1iCfGqM64ZsF8oU+Xia/GmWcvwTAz9emPbJzF" +
                "kR9rnn3qm3LDSM1g64teHCgOUMfCHHp3gAetp8+xTS3iqcOi4p" +
                "/nC336TP+45pnNMKzbnkbM+kg95Ni5fFCtvCpmRSZ6RsWBZn+4" +
                "O90Hj/x2a9b//oksu813hrv7f4MYI3fj74Tad8H/JrXUv31n+j" +
                "0nxe0e9D2nf7S3JT67gdfufB/s2SNX+V2PnKecs+rp95xcrdkH" +
                "7yv2UcfCHHq3jwetpy8U+9oPoEU8ddQbEOUc/fKFPv2dxrXv1L" +
                "jmmTWZdHDG9vcRsz5SDzl2Pp/n488z86qYFZnoGRX7mudnZ7ae" +
                "LmnN9m4Llp1mBYFWb8jW085B35t32/rMTq3O1tNOn3fw9+bh8w" +
                "L02Qm8dvZZT5cwm2w97cSaqGfraSdGFw8WD7ptPPv/ZfASayO/" +
                "72e1UF8fVE28EOUcYg29qQXFTOLNEfJ/GzmmNYSZYJaH1EaOnn" +
                "3qq3Xk/zY0UvKITXOz3jw/O7L1tLLp5g6znnbM+f60Y9CvgkdW" +
                "zvU7DIrbMdf7k482PjuA144+62mlspFsnANrop6tpx0YXewv9l" +
                "PHwhx6t58HvT99qdjvf5+pFvHUYVHOgfLUOkFpbXU0buorGte8" +
                "U2Q5se7UWsSsj9RDjp0v5vni+1NWFbMiEz0jGgeLg3g/z7p/jF" +
                "zNSHGQ7t+jFHcMoHl/lQQVm1lPV6O/xsEu4aBmZlzZdP7c+nhJ" +
                "mKhVWAl75epH588G71s00jLSM4v7mwfS1Tfyp8RuOv8lPH6PUG" +
                "/o/0v55nei8Kl999/B/ld9dqLvzvUL/KmN/nsE8IbvEfR39pZ9" +
                "+j0CryfrKd8j9OFjvkdI/4pvNfux76V9UmxoaPJzc8X2/d/uu6" +
                "FPXxl6H38+79TaE3nk7LNvhbaeLNd4ji63jZ4f45tcrcMZm62p" +
                "za6nHEe9+3Bul6on6NPWfmyQubCei721FVtPXNew+39oXjpc");
            
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
            final int compressedBytes = 3264;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W3uwVVUZP755pQGFwA2BbJppanKmMUdlGtn3nN2Q1Uz9gU" +
                "ZgNAqBkc2IWI2krXXuOYf7mOlxGUZroAIxM9EJrGFIFCSFkHhc" +
                "zAeFEdcEuxCDF7UswNbjfPv71l6Pvfe91/aevfZa3/O3v7vW2u" +
                "tb+9xyd7m7VCqrUh6yplvRdk1DSrm7thRaSKO1GpN3tFf7HnBL" +
                "pfZ9qFe7G/XgMC2bPoSle6g/Uxr80ZZEn5at3QV+tE3aolTTNo" +
                "1OqVSZ3NRbA5RaTVwrS+RAnvsI86P7sd65xPSabbfzTh9X2gDP" +
                "BH2bLVldnf9Z0ujK95bvhRLauhXt0TSkgCSeJhfkbXsqTqvdeh" +
                "SJ69QWO1dRPVOaPgXcJXq3L7RpPj9STdvN9vLyclFTpaIt12ep" +
                "FJ/UNKSAJJ4mF+Rte7Im7bn0kjgtd5/aotZGezYKk9f4qM8X2q" +
                "QtSjVt06cRfewyfY/7k577g3Q/RJ77CPNb2x09+7JSzqP2Q+/o" +
                "EDbYxDQCl7eihwtdZVIzvqsSTyus+WlVxjyySup447TC7zXLrk" +
                "Di9S1tADITfXB+WpUjTgF0PJm7Wu+zeNf4+4yk82Gmjilr28t7" +
                "8GvF/PTTUB+G/sSn5fXGry6GobyivKJ1tS6FdVXTLVnXpT6lLL" +
                "ZAinK1DZc9WaO6ph95an3XSXGAlImDPgV6t31VEz/aJuBDlOUV" +
                "EBO0rdv+Oab2kLDTM4TzU89A+5O0G9Z2zE89YQRZz2KNwfGV8V" +
                "BCW7dqD2saUkAST5NbGV9bh1LUnloXrHHrUSQmj/rtXEN5pgw+" +
                "BXqv/crny/ZKrVIkIKlqLZUWVW9p6qm2vGobKi0d3bqelqElWE" +
                "A+WEmtM3+G8p29+m5KUSzAB4m2RWnLgAukTQxtB2yUdB539JgW" +
                "m4La1ny1JRl3G6333ZaMuc7id76G9cYNhH6kiBWlcbSY58ZMmx" +
                "OOU8F5/YkkmldZcXoir27ydKfIX+cwob9exIrS6C/mGbxRzpDG" +
                "aXMy602z4rQ5r64zv3sgZ3/aPID+ZOm0TrM5Qxqnrcm4e9yK09" +
                "a8us44rc3Zn7YOoD9ZOrXf2ZzBxKm8vrweSmjrVuunNA0pIImn" +
                "yQV5256K08/dehSJ69QWa5upnimNT4HeJXq3L9srtZpGYmtYK5" +
                "Drhja/a+/Nv1pyrH2uy7N+suWLrpNy968nE0+Rn5el64zT8aII" +
                "6NE1r5gOoM/CPOA4PZaMcPFG7eh28+BoWxzm06PjcvLU9+RBYH" +
                "halA91gv5JmzOU8zjmwbXt6Tjxz4dHCx/WfKa7XX2+MYvEqS2A" +
                "4HNF4mTmwWRd8M2mtfe74tT23UH3p6eSOO3w87J0nf3ptqIIjH" +
                "H3o2I6bQfyYc4dl95yL5TQ1q3aTk1DCkjiaXJB3ran4nSHW48i" +
                "MXnUb1c35Zky+BTovTHb58v2Sq1SJPhE5d64X/ZcXep+rE8Rp2" +
                "fjfjnukA81qgMtc5yl7alx91XQjfu7HpQli8wRRC3jCePO9Gfy" +
                "2UTEp3mNmbY9Pe7QJrZQkiIBSYV1G/u98PM0e6b5ht3Dethe9o" +
                "KI0y72oowT+4u4XmGvirH+RVE7zk6wfibyEfYGe0uU/+Elfn5z" +
                "LhiWzDHv4WOb9v7AnmV/VHFazA7xL7C/scOst+th9nf2Wurdfp" +
                "afxy/gFwntkXwUv5iP45eyrewpiBPbyXYLqf3sOfYncX+evcQO" +
                "spfZX3WcWJ8o/ymufwn9C/nwxu18BB/DR8v5iW1nO9gutk/HiR" +
                "1gfyZej7B/iPIYe52dFPc3Fe3f7G12mp3h5/Bz1dNcwt87vW+6" +
                "8AClrulWbbcsKcXkY6lrmq8PoABfzayLQRb1aJzSPGpZjLvHTD" +
                "2TT/HpFnozMVJsVDNtLd2uncb+KkvyvutJv7PMEVq35rr6OYnu" +
                "Wcc8vsSQPU9IvePZITzj+I6w34XF/76r7av9V3Lr5wbf7j/2r0" +
                "PN8ani9Ya+ZMmTfejoTuCZcpTii5Pzfbc0/Ebx21X96TcuLIjb" +
                "yiab6DPitNrv1X5Soz8lX22jxeH+VPToIGum+vlFs56u3xbrTz" +
                "Z6Z5zWFupPb+pLljju6sOBZ8pRSqE41TL6U9Bu1yYXFsRtReAX" +
                "2TaF1IN+CfNJuciEo4v1JUv+SML5taaQv5GSoxQltyHnTvztDp" +
                "zrTOvhQ0rwRykWxO32ls+mj+PiRct0X9N31Z/GAB1ouh+ihNP6" +
                "suIczQ3zeZ8pQ3FFyxzjbpnbJ6VIeyG8aV50ib5kyY8l/W498E" +
                "w5Sik07r5P+vR6B65MuzYWxG2NmvXFbaY5Jo/v4DfznewZvlHU" +
                "F/DJXKwH+BZxbVPcW4jkPHHNZycIJdkt5C8Q6nNWb3harDOrYc" +
                "TsbLA/PS4kdhPp58V1UNBvVdw91ncEruijDRtJrsf34jrTi+cM" +
                "X5iKfb++ZMmTKNQvBJ4pN32g77uVGfN4pl0bC+K21igXFLeZ5k" +
                "x35jfpLKs+DtYK2fkdSNbiQH63Mp2fFcnv7EwsI7/7iW3Pft/X" +
                "Pp03vyv3lcUMqUuVG/fpU8RpvKYhBSTxNLnlvngySlF7yttkt1" +
                "6SuVs86hdsumSQh97RW9oX9aqlqFWKBCTl3Z8H1yezFxUF8+BF" +
                "vjyYf0PnwXGLPw+OW9gh0VZ5sLgXzIMFv1AeHLfYeXDTDsmDBa" +
                "acebA9TqD31T/kWrOb/dPaT5nqWtU2paeGV8fUpksyjZEi0esC" +
                "Y26YihqmNWMsT03PIea4c6Bo7sryk8lMeIXJi2fBPfi0Xm5jbV" +
                "gvbDctI2vQcuk27nejoZR4Fj/l9+pGBLHjyVq9fmX6L+3/W+fJ" +
                "0lpvCutl543mrE77s+O71E1uNGaOyN/Km9/FN8Y3YqlrulX/pC" +
                "wpxeRjqWuqPppaRW1VGw2yqGfgSvGoZROpKUN5BONo214aG2By" +
                "WbOtNzXmpL+38HdMXjwH7oG/+AQ/N54Q7CtzQnZtGVmDlktXe3" +
                "PQ5xj2QnidiOLZzW83SQ+uX2Py4tlw932/S3NTecCrwRjM9mu6" +
                "ZGQNWi5d7c2mU0o8uzrJ79Vlla4jk/dd6wDed1MC77sp/9f33Z" +
                "Rc77sp6Ted732Xnj/Re/Rhuh7XtfoN9hOFvrfUv0zWkZtQvv4l" +
                "ez1uok1jrc/yr2BgPU4xUPSJ15lF30CoXRlVGVUq6VLt24zSp+" +
                "Ae0jSkVEZJGj1NLshjHeRVfvcA6qEdsmNk2cQ2WAKeKQP+8DI9" +
                "oFfwY3qlVikSfCJxjq2MNfa3VFte9TmaA20oaattsZQ36WgllQ" +
                "fvAo62aMsABfl+GYorTVX9bzbF7LJk7eyN9dGkrs5bxN9rLrzv" +
                "WI/oxyJviSZB3hLPZa8ont7Lm8v0nKB22dPf77QdzFtInFT24s" +
                "tb4rnxXJm3KO2R4rLyFi0j85YmipfE9XJCb+ZsMm9R424SHyE5" +
                "+vcFmLfAc0Id2+xkasxJRKm8RbUmmOuC+lfST1p9hB/OeCN90L" +
                "N71Ovn5Tv4weDe1cS8SAYilWC4WXhSebDcp0vidIu9T8dPm/t0" +
                "uT2IOEVHB7VP93Zon86x73vU3qdzei2wT6fstke36XuSIYn1U+" +
                "Na2QZaWsK5Smovzmn6b8/cHycyBq4mdtubbZNSpL0QXj/P6E9f" +
                "s7jzqhfn6j3Wvm9V7mQfGcy4q44IckdaTxn0pvd96wuK44D9J4" +
                "xTeQaUXP3uq+2itmEyTnTcaRkDwXcgTpQn42TLUjt8aWjclWdU" +
                "x8hxx++CNow75fPbdpy0t/S4E36+hXHiS4SdguMueaLk92fVcd" +
                "nRtfMWj9VLxftud+ldO6rvs/rTpjx6+aQMja7m97uuZH5aAHSg" +
                "Nb/fdWXbKcbR3DBfzU9EhuKKuhzzeJfbJ6VIeyG8ad70U/qSJe" +
                "4/xSeAZ8pRSpGjsi7jy0fQrkRjY0Hcbm9ZWKuX+yVCTxp9hif/" +
                "FVy/1eYG+oPiVT/il+84PpiRVf96yK+NLMr1u8wo9683o2NGaw" +
                "a2TY7m+i24eCatoz/Le3G84MP2ns9ycf9Nvc9WP5b8Be9IakuA" +
                "G9LMojX2vhszuPZhe2/syTX/f7ywv05VxvquPAkbjStkG2hRjJ" +
                "K+XQnKNXcq3HrIDfPb9psygEvtAHVGsed5Ot109On3avK48YuI" +
                "6Hr8XU/1E5bm9c430QYfz6RVHnLoriucAT3q8mF7b+zL1Z+uLO" +
                "Kb7sRVr0psXG1/nxjM7+miX2ajyC+R+Xu6jXlsRhvzfm+J+kwe" +
                "tk2O36st5z5ahw2FrlvDEadclvP6JzFYmM5bKttMXrwQ7nnsOJ" +
                "51eFgvZFfrUhlZg5ZLV6O36ZQSL6xO93t1I4rnN+OUrJ8aHzB5" +
                "8Xy4B5/Xy+0cEdYL203LyBq0XLqNFjcaSonnVyt+rymr/wMRWp" +
                "RR");
            
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
            final int compressedBytes = 3210;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9XH2MFVcVf7aCQhW0QEltYv1v/adG/tCoJWHnzZui0VZNij" +
                "E0YLUC4cOvNTFa/rkzC2/KQsWPFQtFG5LyjzGRkg0lqKUWtC0i" +
                "7BbEtiC1RY3tUlr5WGtpq/fe886ccz/37e5b3s3cuef8fud3zt" +
                "ydmTfvvt2t1Wq1dEqt1adTGmmtBpay158GDF+mFbJ5T4jNJX8I" +
                "I02qy59Xoeizs3Mu5+DmqsVe2WLY55ur3AdNLFuM+7BK8ekw2v" +
                "f+eP6YrstRI7R8sVC96+eebHHxmXBWf0XZqtaR3lZl+r2JZatw" +
                "T1G9PSEdzzyl0TlYFY70cdQILV8sVO/6uSdbVXwunNVfUbJB9w" +
                "3Yq1dzJfrRlzSI6aie5zqmz0V8+eO4zcG6VA7pbwSOZ4PfT3rh" +
                "rH4suVf3C2Gv52kz+tGXLCxuJ8uKX8h1TJ+L+PLH8WKRycG6VA" +
                "7pXxg4nnv9fsoZzurHsuWter5YzdMPTCxbjvvotRFE+7J4XFzX" +
                "5qgRWr7Y4gv+argnW14sCWeNVZSdL+6s5umHoWtrfK++WycSXX" +
                "wlhor3OfwvtaV611hqqF/Cvn4pvxF95OXM7IQbqefwhKlkcouV" +
                "qOfmhhavrn6pWBPLq3r02XXTqNjBObjZx9jeC+cpnZ3OlufUjd" +
                "b8z2hL45jzc5upFCd0Pn0ril7jPHVFs+VHx1NDMhU21eetue3d" +
                "lC5AjHi925Op3GO/en+ma1zgfV5cMHoV7TOwZqzbny2kibUU3w" +
                "1njR8pPmcWolhb6+ir747apL0896e7O58lmQKb6ot1rRnvSrvk" +
                "dbckmcJ5aVcyJYl9xuii3kZ8XruKyJXVNBlYM9btrySkibUU94" +
                "Sz+o80W2Hen6TG900sW4H76PP4iuD59L3oe/6KcKSPo0Zo+WKT" +
                "h/zVcE+2InkonNVfUbLJ3Mtz6QG0yWcyvD+FTWNHAI3j+Usmh9" +
                "eVbHKvO8BcTe5RerF6bax7p5y9larv3on38e6d9bWqB0zP70rg" +
                "gEWRppJCydt3NyE2l/yqKXV/vRAHmZEDedACP+rDvr4WEF4N5E" +
                "GOGhc/MhnG+RSsSF9t/dXoJ527AxZb5Jytnbz7ePFj52zY39ad" +
                "ef9YsqTzsE/n4ftdOo+8NnM02x9pc8kfwkgznVdsjeVV1x367O" +
                "ycyzm4uWrRe2Tr80rxi+r+tMvGfFZIx3Mf3xiPi+vaHDVCyxe7" +
                "rtdfDfdkdxa/DGc1VZM1yZr6duxhBJYay+u88iiuaZk24Pre0N" +
                "JDfdAhLkajXzX0EEaVKBZ4ETO1qBaqkR8JMVEHNW0uqdl2OiLb" +
                "fH32zVebsuVe+tQYPMrWjJHe7bDXjBHkA6pxeB6fT6rsnIcsI1" +
                "UGaPOhIcb1kYGxxNG+Eb5VR1EdDWoBl+LoWKua5mNW55ofqWq+" +
                "kF6oX4Zezq8egaXG6y+TR/IvkIUsjoIG1wOvQuuXeSz6IY9q5K" +
                "ER5UAcMbMOzM1r5EdCPNSBY8XjRYbCqGb0YebW1bikdX/6aPUe" +
                "8isb81nOVR1E+x6Nx8V1bY4aoeWLTQb81XBPtiQZCGc1VdOj6V" +
                "Hs0Qar8RHwkQeZ1EwU+a6enqfH/HG8EhPjeXmlNocwyp7sC+Xi" +
                "WYHFVXkldESyDaaDcqR77RuEJt/v3gIfeZBJzUSR7+rpeTrij6" +
                "vmycF4XtT0cQij7MkjoVw8K7C4Kq+Ejki2oXRIjnSvfUPQarXy" +
                "A+AjDzKp9fZwFPmunhptnEFxXLWaJwfjeVHTxyGMsie/DuXiWY" +
                "HFVXkldETpUHbeXM0FW23pZkDQpnVfsnp7FB9tUrJV9TzNQgQU" +
                "s/MisdeVeUZsfg6vSz1nmrXJGdjNa/YpASu+qk3R4jFxQI+X4n" +
                "qBkHMoTsjz6WPiL7qGk9lScUZjNwFT6GhxUc2TeD2v5W/X6Ify" +
                "d6JO/u58lnMf19e6+Jt4Xrwg938X/zIqWpotza/O9UpGfo3cZu" +
                "TX5XPFo+J3Jkc8JY61qnhabn+t/C+1Zuw/rRnYm09XSD5Hex8X" +
                "fxSD/DhhnOwlW7xqzZGq6Cpdz8z8PemR9Ig8d3Svz7Ej0OQ8fQ" +
                "J85EEmNXndMRT5rp6ep9MUx1Wr687BeF7U9HEIo+zJb0K5eFZg" +
                "cVVeCR2RbIfSQ3Kke+07BE3O083gIw8yqZko8l09fd3N9sdV8+" +
                "RgPC9q+jiEUfbkt6FcPCuwuCqvhI5ItsPpYTnSvfYdhibn6VPg" +
                "Iw8yqZko8l09PU9z/HHVPDkYz4uaPg5hlD1ZH8rFswKLq/JK6I" +
                "hkO54eN57Uta228jZA0MbetBSfRsQ3VfWnvumcT3F2blIgLsRy" +
                "Dq/L9uoZ2OOv0rqP7+HZPZ9bjpvqbCWHnscfHmXNZ8/Y1og23n" +
                "Alv0dIdrW1/rRrvBnZPO0bne2uj0fm6cNXcp6aQ+3ENZ9qe43u" +
                "vvQ+7NEGK3kcfORBJjUTRb6rp+dpnj+OV+JroMgrtevwYap6fy" +
                "6u6UbalVR2f9ovR7rXvn5o8v70WfCRB5nUTBT5rp6ep1v9cdU8" +
                "9fsbKKKmHc9zcyu5KpSLawKLR9qVUIR1fv0cR+Xnw1jg3IziGx" +
                "eP96oaLS8xiNn9v3YiRmMx/pZ0C/Zog1XeDj7yIJOaiSLf1dPz" +
                "dIc/jlfia6DIK7Xr8GFJLZSLayZOpF0JRfg+0ejzaZG8z33Qjw" +
                "VWbKJ4/ehE7tTNZ9u5j1MFvmzFjvHnT+9P78cebbDKu8BHHmRS" +
                "M1Hku3r6SE/543glvgaKvFK7Dh/WPBnKxTXdSLsS4ljfu1bfm5" +
                "fLHOzj8TlXn4Mj59Mzk/9ckN88WdnSbek27NEGq+xJt6nrjjzI" +
                "pGaioMHH5bcRlZWf8sfxSnwNFJvP8jiTzY8C95SNuHDdkaZ5/O" +
                "Q1tfnseJ8zH+nsT6T53IS+8d0/xufM07VJfbF5OiCzPd855frJ" +
                "Cc3yC2ObJ1+2Cd3Ht6ZbsUcbrLrsm8Pcg0xqJppurTMW19OVB+" +
                "J4Jb4GirxSuw4fVg/m4ppupF0Jcaw5f6IaPdnZM3XjlycSvS76" +
                "+7vrnG+X6l/vbPW4bty43l4fL7+TnVf38dj6uLnyPMr6+F53fR" +
                "yzjr4+3nwltD6uNNz18fo33Crj151bceN6im50NbqkR/ca64Im" +
                "5+ke8JEHmdR6eziKfFdPz9MBiuOqVVUOxvOipo9DGGWvrw7lcr" +
                "NyVV4JHZFsNzT0ulCjtToEttrKDYTYHN6jAuGoYp1Pg5wPe5PF" +
                "a0Hc1jGz0WbXUP9aqMrQy8WpHtnmNubKke61by40OU/94CMPMq" +
                "mZKPJdPT1PQ/64qioH43lR08chjLI3Xw3lcrNyVV4JHRHsu0dg" +
                "U31RfS4qBhGrPoNrXvfION/ZL8bx0XXdWqhuJ9uisWvaCMfS3e" +
                "nu+oPQy7NVj8BSY+ihKS5ZyOIoaPj01IjHmnlUg3hf43Ugy6yD" +
                "joJn9+eiOqk+qjLdjXNC2mA7z1MD1ee7n4axwLPYQPZi5P3kxT" +
                "af6AbG8RQ4EMo2HrUxPo+f6PDnlstXdH389Q5/Dj6TnsEebbDK" +
                "beAjDzKp9fZwFPmunr6PH6M4rsorMTGel1dqcwij7OXVoVxuVq" +
                "7KK6Ej4hHO+XSysz+RxpIreT51Olv32e6z1MMIrHK76rnHxKmH" +
                "EeBclaL1/W46cinOrsTUNDlmnInz+sAqp7l6dm22Kldz7HPd5+" +
                "So1cMIrPIB1XOPiVMPI8Bb2c/hmNByJnIpzpgnC+PKpOlyOEY1" +
                "ljNcPbs2W5WrOfZw97ActXoYgVXuUD33mDj1MAK8lX0Yx4Q2Po" +
                "lcijPmycK4Mmm6HI5RjZTNrJHXZqtyNVe9+g75m/qu9Er1XOB8" +
                "bw6McX8ffHzy7k9uZcXLbb27j/svnunvqIuLY6r0ltF85axJma" +
                "Fb/NnLazv8XDCcDmOPNljlQfCRB5nU5HMBQ5Hv6unr7qsUx1V5" +
                "JSbG8/JKbQ5hlJ2y2bncrFyVV0JHlA5Hngte6+xPpLzuSq7TlX" +
                "Mm4+zNVmerVU+bzPScycCNGNDTPqQMeGNZjGdq+pioQ3XYlfAa" +
                "G8sowlQjHX7k1KO2yQutEsp5UtfeWb7ah+uM/vXN8HppS29u/P" +
                "tic/0xvPLK1zuRCc+ZvMZymrk+6tPh67fUh3/XWOY5KFr/nad4" +
                "Q1p6bSXrh63F+IfE3pT7s0I+Uwj99x6ite4Av+8beFJ+QhwS+r" +
                "f36vtE9Jsp8ZbWeofu32Wtdu/L+sWT4k8tpnznFH8WT4tTcn+a" +
                "5km8DL/vm0/Np9X3yf21+Xu1/w9GnmcE+w5e/FPIT8xiWPxbHt" +
                "9lof+aVbwm/iveEG/mb2Nh/wcNyjzK");
            
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
            final int compressedBytes = 2846;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNXHusFUcZPxgtobdCL9BeSkGMirb2Dxsr1KIie85uGq9Q35" +
                "Ya6yM+av9AqzFqjKS7Z7Z37+2lXKBCxIi1WjV9aX1ErAqUpBVr" +
                "hAsRGm0aq4Y2MYqmFMWrQJzHfjvfNzM7Z/ecc2/cm3l9v+/x2z" +
                "mzszNzDjQajUYwXyWRt8818iv6ikhKri6lhyXsM43KV/MXfhz7" +
                "ddvaXDTvzhauK7u0XIPeadEre/PyR4WPf1Es2gulr58wSq/xYz" +
                "7G4L3kfpaaOqJWcHbYZkvcbLAE+6jKKNqT3/kLQTI6l2LRHii9" +
                "91uKNjvY+fw295g6ogYtl22zhCuWYB9VGUWH8vJA0U8LKBYdig" +
                "61btOarqt1m0CFlo24pDi+z6+tI2oFZ4etimbLlQRQxbcKo2h9" +
                "tF7nqqZaowtFjiUU17mqKRx71dbySXgl6Go7wsvAsGfKlOpgTH" +
                "PMltv+TG6mV+zN9i5rJ6OTIteJ99NichdSzr6mNSHXJX9qd8lP" +
                "64vYTlkIqdZzfn4nzdzEcTzwq2TxYi1XmOAAFtQboMAJLHB0+D" +
                "M5xI/GjyW72fmNRvsMb00m+3g/LSUaz/BeGGix+G/x33nreSnL" +
                "Z/qkmNNaTOe53a/iX8e/ETJu+7Svn2L5pk1my/wCEx3ZGz8eH8" +
                "w1j/J0LP5d/BQv/yD15RMSn+DpNG+dl8wRHJL5yaCU/5LE+X38" +
                "JDDknJ6N/8Jlf42fk9g/Zf7veCo+E59NZmmrcCAcgBzaqjV6pZ" +
                "JpSTjQ2ggtLcO11kathf1JThvddpgJxXSbMjV1NAYJMzVjAU/N" +
                "CXvFTMBG1haGC3Fvq7ZIoysUAm2Rt24Fic61B8Bbt5pe+Vi8E2" +
                "whgq0DEo2X62BephQ4uFkqlJbYkkbT3hP+hkse52m3vJ95yRFe" +
                "F8/dhzuvHZNHitoTSPpbS+9R/tQ/2ejhSn7uRe33miNasr+oTX" +
                "bLI8qf/WRZsS54P8Wig1B28sM2OtcM1/vt/H5Hb6Q6ogYtl62K" +
                "ZsuVhH1J1bMryqNSr63jreOQQ1u1Rj+iZBrneVvjOke2bVm2TX" +
                "9SVtiadmZkrYd1qB3FQabZmkwNRu283ja9YibUO5pDLiHj6bN2" +
                "H7fndhqXbL5+7tiiwm4eXz+9rpfnji30oe0Ba99ylfc5rfzcsY" +
                "vYxYatnJ/aH5J1MT+NOKyWdn+n2Vt76aeRvfXmp2y4Ma1Xcxeu" +
                "6VZdaz9W128ni3hxFf36UZHtkuYSnauaao1+ubkENABvJbhF9R" +
                "Uun+cEJIALmbbVdiYT6pPqUDuKY/6qZTKljBRPzdT2ZnsnZy43" +
                "sZcV89N2PnpfQ9FWWnpac5OMm+ocZDmjtNy20lN7pS8ujgTxHL" +
                "PY3SZanRO7mqdrZC3E6wJ23ehXZfk29zzO3kfPn9g72ApZXsVe" +
                "r+Zx9gb2Rj2Ps7eEs53x387eya5n69kNhcSY79ka1oJ5nAW5LE" +
                "L4tWydmMfZu3h6D08r5dqwiMZWId03sdVqHmdvZk3e+2stPsOM" +
                "zKPs3ey95HPZJvO1qpTjaSfIQRas1ZrmOV2wFvuhMhtxjIttft" +
                "zUAV4iBpevtXXdMQm/bb6obizYEnxalUU/7Yc2yEwNp/ct9ZE8" +
                "/paO/YR0CK+cux3N9oklwp+PrwuL9uU7mmEow2GKRfug9K6r92" +
                "kfxl5p2G/n92vqiBq0XLYqmi3HEuyjMyO6v0uWFfu7A/1bbYj9" +
                "XfiimdzfuaLp/V1XDMr66XR/98HZ+pnsp2x5v/fBa57QSeRQH/" +
                "2vsRP9T/d3KfxlN1TRHD1T7sHvv/O+xe+j6/F0tr/jKRys1E/n" +
                "StYvn6v53A32dzxFR6OjkENbtcZmRUezlVjC8xPQQjJcO6G1sD" +
                "9ensC22A4zoZhu8/FxNcaojr4LSJip1lXrTOAp6ydMr5gJ8FNl" +
                "2Xgae0Gfx9OFPZ0X1B1PF/Z9fvqpTiKH+tjsarZVY2Qf7aWf/J" +
                "Fs1DWPV2Vbb34Kbq4znojUOZ7Gn57J952LfY/j6WGdRA717OZq" +
                "tlVjjP+xp/H0cD00+0T3bOuNp7E53Y4n9zpz/E8zOp4WV1lnur" +
                "RKP4sDOokc6mPnV7OtItX+ux5PB+qh2ed9WlDLvlDrXXKvzB8k" +
                "5yrfjnbyN+JO9kChdQ9Puw1Lfa6S/x6IfQvmJ/Z9dh86V/luWP" +
                "JLI/aQzH/SgaPnfJzdD+fj7Ic8fUe+77y/fKpxPv4D9mOyjsqf" +
                "oHBH8WbdQbHoESi96zEnKnyNH/fb+f0KD1hH1KDlslXsbTmWYB" +
                "9VGUUb8p4uvm9heygWbYDSNZ5MP8ZJIn8njD/j7acNbssi0j6q" +
                "I2rQctmyn7nZYAn2UZVRlI/EZKKQ7FAJsGgSSm8/lY7o8We9/T" +
                "QZeZ8FyQbpiBq0XLbBkJsNlkSTwVB5VDej6LA5nqLtKgEWHYbS" +
                "ez+Hy8ZTh/3T4Q5+t1MdUYOWyzZY4GaDJdHhYEF5VDejYDMt+f" +
                "sugraWUQ3nKeDm+ohC/bipg3kFm+3vpRRm+8QS4c/HF2PsYOn6" +
                "6QP93d/1eNJXd/10UZX1uEur2hWGxXj6WOP/6NK8qqHBoipeq2" +
                "kVq67TKolcz0/BcsCo3prTXYyGP1dj4fEw5eKieVs9sLyzT7+G" +
                "+06DCVry8RRDW8uohvMTmqiPKNSPmzqYVzDhmJ8m3DGxRPjz8c" +
                "VYuDpcDTm0VWtsRMm0BDT1H0VB3/ZHfVM7zIRiOC5maupoTEfP" +
                "Rspi2VGxV8oTR1ZX/Fg+rxe71eiT7n4Wv2Pt6qStw7MXn/Oh6Q" +
                "e5BvrdVnyMp6cK9EbrrS7ZJx3PmuPSVV18NkFnDtGp6BTk0M5l" +
                "tygZkpzSLS3DNaVv+6O+qR1mQjEcFzM1dTSGON5SFsuOir1Snj" +
                "iyMV8W83h2p2O9OLfSrA2/E3te74N7fd/V/p3Ytj7tg//BnnN6" +
                "KPopvKZ/b3XRT827ZrKfeotWYSwU/TS2q7/jKdwyk/3kj1Z/PI" +
                "XrwnWQQ1u1xu4K141chiWgqf8oqny4/MlP+Otuu+LOLAzHHTmJ" +
                "Maqj70JH19G0rvpeyo6KvWIm+o5EWbpv+UZ/9y3hppnct7ii9e" +
                "P348FWWvJ+uhvaWkY1XOcqVLcaolA/LtYFWAfzCrba6wKF2T6x" +
                "RPjz8TWxYFAlkbdXFszOA4zqBYPdfRJhh/V2Z782F83bHa2eTx" +
                "PxWaN5/Ju9zuP0fRfe0ctzl86uOY/f0Z95nHCQUdIl6UvQ9wib" +
                "xu6R0gXV+inNTyjSOdBP6YvTuaSfSk960kXppdXed2n+7UBKfs" +
                "eezoN+Sod4kt8T6WjpBa5+SgfThZX65uL0ErLO32+Op+ZLKRbt" +
                "h9J77tjlb7HAe1UdUYOWy1axt+VYgn1UZRQdyf0Xs4iuKSw6Am" +
                "X5nTQnytHsPm8fHPH5tXVEDVou22yOmyuWYB9VGK2ZUknk+nuE" +
                "5ibAqB6WWP3kefdnD3Q4CZrqeFY0ZXMG3la0++v7NJFyLPi47q" +
                "f0FTbqeW9IrH15uX724HTsH1QMm1k2pzGNV7CueXsxNm63UZ+l" +
                "bePT7x9jd6TsoWmNep3+dxvNl9uoz1KOwVdV1e8fY3ckm31/r+" +
                "Y2u9aNdb8vsR73oNZ6PL1s2j6ZT8k7Ldbqza1d9NPWaeunV3fm" +
                "TvQv7/P4GWoO6VzVVCu7tzkEGi5c56qmcOxVW+M6tjOZUJ9Uh9" +
                "pRHPNTLbbM9mdyM71ib2Y7XBWu4qtXmct17Cr1x/vpe0qmJaCp" +
                "/ygK+rY/6pvaFTsNC8NxwadLR2M6OruiLJYdFXulPHFkY7wWv0" +
                "9PK/x73qr//1O6gn8qs6ZvVk1faz0nlaI1q4f4H6zsxq4=");
            
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
            final int compressedBytes = 2713;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW2uM3NQVdqHZUAILFVCk8PoVlCBA+VPEPzxrD4EQlCVvAr" +
                "RBPEoKou3SIlRVYM94tFoLARKRwo/8QbwU2pQSXgLEq0VEFImW" +
                "REqoIqJdaCkqG/KAhjyA4HvPHJ9zH7bHa88ifOVrn/Ode77v3v" +
                "G99nh2HSfc4jjh28n+gpNs4Tnhe0n9muOM/cUp3MLX07PtzLvN" +
                "iHvTqbQ1JsOXc3W8q3tGn7ZEvZGe/aOsgmhx5EULokZqX45nY3" +
                "8ubt0e0bI1M1gWJfuVFcbps8jP7cVlZoueen/pVBW1nkzH6ZXy" +
                "45Tb16NVrqjo57nodfWy2Tf3IOyijlag13sAMTWOe0pdE18Vq+" +
                "g9AjWjbjtbuZw6YsMaD8p6CRzl9fQW+tHXWEKRRvslPI/qMxEb" +
                "fz6ux6AuwZH4l2T050G7n/Jls6pYtCbcEt1kW8eHFliu7xu/m3" +
                "U8yVByHbepN9fx6IYpr0+rUqZTlBG6A47BJPPd2sO6spad/6aH" +
                "8ZgpI39ryfTL7FbBbBlxVxo7ItRHd3atm5U8t0e/VuzfyQz75P" +
                "kvur7bol9ZVuONst4E11PX99joM/L4pzTq0WQUBzNX9Ge6x0fw" +
                "emo/1U7vCa2T2k9ktpTPH+3nC+4Yp+Zgf2zN6p5tTvbH5dkFuZ" +
                "9Gz88F7afbz6Yz8WTYRd1ag97mXYipcdxT51ac19RCuvVYU31Z" +
                "VrOnjZmwizq8P31SexYxNY57ah2nmWUiUDPqNq6D+eVz6oiOuY" +
                "dhFzXOu+R+9x5iahz3lNmaxxXcoQvzmlpIt52tXE4dycYa1wxd" +
                "n67j15tozudyTZ7PzFV2y8oAHCZ7dcbcq/Bqmndj75toXssin7" +
                "+nirLWbXm8Jns1tsJxGqZ5Fz1uonkt83yttVWVZY7TsJ09eszp" +
                "68bWp39Z1A72lKP7nNn+nJ4LkhWjkq6854Ik/yxjfXJqei7Y09" +
                "5n+XQWR5vScZow0ZzraXGRr3lCX2bAYjt7f9jY9fpUOk7/qZ5t" +
                "7GOm/EfT+b6gGpv1Gsx6T3dI6/PBau/pmj/saWQP1/P9zsZW5T" +
                "2dN+lNYo22KO17Ywd8hOMZb0O243iTalbKJ+9ALJ77eRs1pxrj" +
                "WdoTM+kDy5808+nabFm5EjW7MtuX4nNBKxj7xlgLluasE0uLfP" +
                "4XPV1PJd+vAYfJ3htb75s/35+PNdpgxTPARx6MpKKiGG/mk2e7" +
                "7e24EhXjvFypHkMY07g7i8tk5Vm5EuoRbyE/l2V0PUXPGZ/dsp" +
                "zPdVlvvtrvd8vsTK3f94HrIthF3YrSlXC92MHP47in1JW7r1hF" +
                "zrfD9WoEakbddrZirdkRak/9a/1rsUYbrPhi8JEHI6moKMab+e" +
                "TZEXs7rkTFOC9XqscQxjQeyeIyWXlWroR6xFvoz+PxJTWvhF9O" +
                "5/NTNTaL+kX+IqzRBiu+HHzkwUgqKorxZj55dtDejitRMc7Lle" +
                "oxhDGNB7O4TFaelSuhHiXF9d3kTNbS50JJxmkh+MiDkVRUFOPN" +
                "fHKFGbC3SxUbGOeNr+CYGoN8XCOx6VwmK8/KlVCPqEVjnaxXw1" +
                "GiC9CPvsZqiux+S0x/5wRMRdFnIpZVc10+rsegLsGR+FebsXZO" +
                "Rd+6PFYV85f7y7FGG6z4OvCRByOpqCjGm/nk9XSsvR1XomKcly" +
                "vVYwgjdmLTuUxWnpUroR7xFt377/50bdxe80r4//49RcHvUv1m" +
                "c4/ALmp2v/sZYmoc95T5+wL/82IVvUegZtRtZyuXU0dULOt9Qb" +
                "zGcv/dOfX3Bf5Xld5qlHxfYGOr8r7AX+uvxRptsOJbwEcejKTS" +
                "HuEoxpv55Iz+AbXjWbkSFeO8XKkeQxixE5vOZbLyrFwJ9cjX3l" +
                "s3VrF5d4dxB1hlvBUdycZ0X/PEvny/W2Vnr58tc979oYe2Zebd" +
                "3mmdd3t7mXfRRzWM0931jlNzcDrHycZWZX3SruKr2Ly7x0TzWh" +
                "b5mrP6Mu+usrPXzdbc0dyBNdpgxS3wkQcjqagoxpv55Ew4YG/H" +
                "lagY5+VK9RjCiJ3YdC6TlWflSqhH4ijmne19wehLU54j26zf6S" +
                "u9iY0+zUWN1Wj0xZ7Wpz01rE/tmtfxQ9O6jh8qHqeoxF+UeEe9" +
                "o1ijDVZ8H/jIg5FU2iMcxXg6j+9HNFF+mNrxrFyJinFerlSPIY" +
                "zYiU3nMll5Vq6EegTHxk9hF7Wf/u4FZ+DvrpkyjntKPdF+U7Ai" +
                "F+Y1tZBuO1u5nDqiYpnz7pGanwuOn9bnguN7WZ86U3wabaygdb" +
                "wzaKJ5LYt8/v6+PBessLP3hy1lXd5Kf2Po/NhE81oW+fxP+6PY" +
                "zl43m7fV24o12mDFL4KPPBhJRUUx3swnZ8IMezuuRMU4L1eqxx" +
                "BG7MSmc5msPCtXQj1Kyk5PeVcCttjj7YCgjbVqiXg6U7Non/Bn" +
                "PJ7a6dzIiMUew3XpXmCzq8y5YnZm+cy2jZVs3p1mXOMrc67/lU" +
                "U+/+u+zLuVdva62bwPvA+wRhuseAf4yIORVFQU4818ct4dY2/H" +
                "lagY5+VK9RjCiJ3YdC6TlWflSqhH4pj5XDBe83NBpb87L/1cML" +
                "Pe9wXeBm8D1miDFe8GH3kwkkryPM5QjDfzyZlwlNrxrFyJrUBG" +
                "rlTXYcOITefiOc2WuhKK0cZtY/pN8rVsLGPMNzrf0YbMTP2r0/" +
                "U+s+nWO+8qqiw779x+vqdzHHa/O8P5Hm+jb/c3Pxuns4qjy/x+" +
                "1/mkiq7O2SXH6e81z+xN3qahA1A7DpyBJc6jDeQRsWRhFEchB8" +
                "8HXoFCPjUaeUQhj15Eu6EDo2/xdqoO3gtiNzW2HkYeyIn9xQiB" +
                "kWb0gS2PA1h7A/g92BsgLxvVAW2Urba9pR5L/iyMchbxcp/Ozm" +
                "N5DOXNY1fXcdt733jvVOed/b1vc8N0vve1sVV57+vP8ee44+Io" +
                "andc2GCJc/AIG2LdcThiDfFgEw5eV3lOFfmQDzJCEX6OYWbEUR" +
                "fXCzbqwmjMQRrIRlbMgzkxlnqljw0/upuxdjfj/726m8nLeptY" +
                "nQtVW8fVmhA9lvyAdc63f5KgS67l55KvMzexLzC5VfbOPFUHj4" +
                "G9M8fsRffOcZ4yZmf6Z7q7xFHU7i5hgyXOwSNsiBUxZGE82ISD" +
                "F85ThbuwLTJAEX6OYWbEURfXCzbqwmjMQRrIRlbMgzkxlnqljw" +
                "0eg78Gf3Plf0aJ2p0IzwmS58xguzvhTgQ7hCfYmdjy9+XgvxAZ" +
                "yHeFwRcSPRw6YfedengcZgpPDE9xlf+3Evlkq/FgIvgwOf47+A" +
                "T9iIXHhjPkcVayD4Y/CU8PXg/eAF2YJ9gabAM7eD/Z02+0wf+6" +
                "+b9MNcg3v+FpMnZL8E7wT8qDOannSYztl/ZjZH1SeLI/1587dA" +
                "jq5D4pz8AS59yTjOtcsjCKo5CD5wMv5OFt0Q88opCHzogDccRU" +
                "HcjNNfKeUBzmgb5i7zBCYKQZfWA398Nf0Pndv0gDW+zeQqohBv" +
                "/WjiyBYgb+t3jC5y3U7tsLEYGMzf1BA1kJwzwQhx7KBR5hoy6R" +
                "I5itawDdpJXamZp09em8m02t/Xl+stZBLbF5UJIcw+AjD0ZSET" +
                "GEYjyce8PUWmoapnY8q3y+eEjl4UXYIhfH1BjCOLuNq72e9xU1" +
                "8ay8x9Sj5PgtZzuIWw==");
            
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
            final int compressedBytes = 2026;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW02IHEUUbtCDEDARvEnISVFRD1kwglmc6Z5eiSy5qJegJg" +
                "gSDwElsBrUwM72toOZJRE8qdFk1bO/0ZjEnwirMZofs4uRkCzB" +
                "HEKyMrtLVDBqxK5+/fq9qu6e7pmuqWS6mZr33vfV917XVFU6Pb" +
                "OWZVmVs1bUVs6ObbAs8ChKh/D8e2RfxeWWEJVLccD8uywrnYFa" +
                "/m0U828P/LuTueXs/h1yHZwDL//W5FXA4d8p+/VDwetw8Nobei" +
                "vqJ4L2a8vadtnKPeoHY+ski84keFOW1ThulTjqB9qix9RI41gK" +
                "65vY6qqW6hZsq1vqD2KMoiozz0/vqXIpnoWRZl5eHlOzcy7nkG" +
                "677Hg4U8E5CG3ggQWxwOYRwfV2xvgU6xmhAf5W+B7rRfpToUXa" +
                "UxiP8gyGuacyzsGoNt5vkDPoKnj21Boxz2BkS1yBUc2s8uidH2" +
                "P3xutzlaX1aJywDB6Nn3Qriv2JeSviTDNdK87ov+7x39qii2rk" +
                "5VuK7E/jC0XzO+ed89XnoQ1WamiBJ2weEVzykMVR0OB6EAUd3h" +
                "fjkEecFCGLciCOmFwHXQXPnlYj6sC14tUhQ2BUM8Yws2XZc9ja" +
                "c7ju7Dn/fsvydgGGh+xl+WoUYjzamOZxGUv2FK23u11eHlNrIG" +
                "tsknPwla6WuDtpVVqOjS1Y4Ak7GMs4IrjAR4/zQQs0va2AoT7E" +
                "iUs6IgqnqkynwBBHTNaiWoRFV6NyScd7CWyVS2qqn9jH4xW77a" +
                "/8UfY2W9fo4a8uwhq71LX+A/E4/aO38toBk+NULJtf7XqcavE4" +
                "/XtNzQ+3Q75TiDVUVK+yWFmkFixxehPOkPBFC6eMg8VRwEnVGU" +
                "IL4sQlnfhf3SFVmXMJR0zWkutPVkYW6WBeNSPVrPgLwXkxbsEC" +
                "L7ADThwJ7AXFk3zAw/dIL4otRDqkvaDELwquilElggXRGJO0qB" +
                "ZWI78SUsY8qHkxU03xEzvbhnhOrtG7j9c+NLo/ac5WHagOUAsW" +
                "eM6waL1nKSLjohUc9AEnVWcYLYgTl3Ti+T+sKnMu4YjJWnL9yc" +
                "r4NSEX86oZqWZVHY7Rb6MZ8mv8ibyTPrKj813uxOvb46P/5SmM" +
                "HmX2z8HrTJv5FFZfvylX83wmcqX+dJs7iuh+3Jvwn9S8Ej41uu" +
                "40Z3Pmg3MA2sADC2KBzSMhl/B51hNR0GB6UTScgbwvxqM8A1E/" +
                "FaMcER5jUh10FTx7ao0DrE663gHSoZpZ5dF7Zdbd5G4SrXhZln" +
                "hVZiuzgMF4QhyY4IGPWHQ/MKu2go96xGP3JbNwkqasCDqAYxTr" +
                "4JXAVRBGPXhejKImXTn0RTU8C627pzSvhK+MrrseZKtcwLZywd" +
                "2DMYqqzDw/vafKpXgWRpp5eXlMzc65nEO67bJnHe738c61tsDu" +
                "trY41gk3D/de7a6yTmpK6b0KW2eVvwljFFWZeX5az6RPeTBXdn" +
                "VFtDCmZudcziFdp4sn3Oz5+DOad4zPjO5PPc7G9nHNz5Zqnxsd" +
                "px5n80di6znNlX9sdJx6kM1uYWu36jswJvzxnXYryczzeUuI3U" +
                "rPDWf76uxW42i7vDymZidrbJJz8JWulnfg91LexLYr3Y578nup" +
                "saXBJ/xlmc/Su7ntbrEkMZ/aZqsfLzu36Pu75nUp9dzY2Tg1r2" +
                "fjtMfoOO3RP07udmzd7bju3O3gAyYz83zeEqJyKZ6FkWZeHTym" +
                "ZucW5+ArXa2D+XSDvh2wfi74hN8vpXC5M365bAXq2RFnuq/z55" +
                "n119pU/pHRf+80Z7OX28upBUuc3kRztfApIuNgeZvJB5yrUu+w" +
                "8i+QSzpqJRzjynKlModjVCNl48pybaoqV0uqZ82nZlXzfPrA6H" +
                "zSnK22rrYOW/TFGcwnG2IYERZ5EWszR5Gf1AutfdSPq/JKZIzn" +
                "5ZWqHMJYjfuyciWzclVeCV0R75HYx2uaP5G9RueT9mxZv89sDi" +
                "e546eVvgdjK/f3mbVPSlXZ4e8z07KV+X1mbWNtI7bog9dcAzGK" +
                "IJPOYN0xFPlJvdDaT/24Kq9ExnheXqnKIYzVuD8rVzIrV+WV0B" +
                "XxHuq6a/xi9fGR9ns6rauQ9qeHOv/37to5GqdM7U+1yZSnLm92" +
                "vz+VrLLT/WmyyP7kv152PjlLnaXiuYq+z0PolZgfp8tnG5sscT" +
                "/u2i61YIHXfNh2kZGGUwsW4FyVeodXega51E+tRNaUOXI/Gef1" +
                "gdc4ltRTa1NVuVpSXZlPb8f70yPl51DzUVbRjyb3J93Z7GX2Mm" +
                "rBAq+5PnifpkjzCRmnFizoz1XRitBp5FI/tRJZk3OgN/WTcV5/" +
                "hE2n6E3LtcnXL6sl1bPmkz3ez/cFva7e3xWvmcf03he4uwut1M" +
                "f1XEexbB3ojbgj1IIFXnODO4KMNJxasADnqtQ73MfnkUv91Epk" +
                "TZkj95NxXh94jVZST61NVeVqSXVl3b0bf7Ka7yHdN0yuu15nY8" +
                "+fRjRX/p7RcepBNv73LfH3d3PNrT38+5ZFHr96f9+SrdbBfCrw" +
                "V44d7eOHjc6nw8bW3Quax+mI0XE6YmycXtQ8Tkbvx3udjY3TqN" +
                "5xavxu9LnKJVPj5L7Sz/fjva6ePc/8Q6PqudIKHX4f7J00NZ/s" +
                "lX39/7uVpsap8Wc/j5N3ytQ41b7Tu4+bPRqXjY3TD/08n3pdPR" +
                "unQ/08To2/zYyTs8RZoldZt97VzRaPU83R/PsC3XqGs/0PoFKs" +
                "1g==");
            
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
            final int compressedBytes = 1494;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWctv3EQYt6rABQkE4o1Qg0CLBBUShxZ2I6GJneWAOCGQ+D" +
                "u2JYU+gMTexNKmcOCG4MAlN6CoUis1bZOUNNAEFSFUHuVxaLfi" +
                "dUZwxLNm9hvbY/ubmc8TxdF8+fw9f/l5ZvyI52WPuXeFNt3xdv" +
                "HRNHqJpyn7avGeHeNpyhVP8S27eT7FE254CvYH+4tela3oV0fV" +
                "5dIe1d3ssYx5agdtRf12Dbp2eVRdLjFPbXOvFk+dQLETBjW7Y+" +
                "pXRwVO7wvV3eyxwP40mKdFPnPDJU9Nd5N46hMjv+mUp5tueApa" +
                "QUsxX1s1s71VFqWu1+C6a5l7S5j5IhmXk3FmdLZ37ptEribzKU" +
                "bkro217yTrt4W4jeQKD62u30ql90phPim6za2Pta+p5lO4FLCi" +
                "N/ygOjv8cHS1mPIaMqfziZl7tdbdZDDpeVGOF26r7D9ZFpXWs3" +
                "huvFOTJ0W3+Y+wf4fWPn6C+An5LkzU4B2abotXnN3v3iPm6V6n" +
                "7y33NMzT3jFPy4qZe3tuP+opaxT28fk7PM+/1QZXeHeVd/62vK" +
                "W6G90+nvD0qT3rg5MS8gmX88mfcLXu/H27+XtB0+il7yr37Wae" +
                "oj2ueBp8Rryz3u+Sp4VXnPG0idhdexpvXJHT97vIDU/h0mCLmK" +
                "cFpzw13E08FyQ8/UDM05xTnoi79b/KnW+PebpGzNOsovtWYzzN" +
                "0tbrdrodkKnGfxKefup2RETRn2phD85Tv1wVskfIXxexUCePRP" +
                "bJlbNIszGyDzBCN7lyFlu+qlwtf84OsoOeJ2SqpWeDX7iULVk/" +
                "l2EPzlN/egiLiB/x9JqIhToyT3mfXBlqFmNkH2CEbnLlLLZ8Vb" +
                "lasXpuJVwbr7tfiVdC3+n+1HC3/s9jnq4TI3/DKU8Nd5OeC4i/" +
                "xM+ETnlquJv0veAP4vvdEac8kXdTfx9P5tOfiNy1sVb/ffxtK5" +
                "S638cV3YrfxyPjNdMfjtfdb8Tz6c0Gd9UbzXdjX8LgUujxQ7jc" +
                "vJbVsz3sUOp4wx+xaO3WXbI//UW87g45XXeHMOtO60p9D4PLVA" +
                "+XsLl5LauDZcbq//DFmtVeVTc1WuR7y9XuVSHFOf9J9qffU5uw" +
                "cA3O/o/qyV4RX6w3Qn4c8uSqMpKsT+4rI83HgA+6Q7d8r2JXua" +
                "qMBP4i/rts3aFWg866O+Z03R2jXndl9zvyJ5q3nN7vyLuVPT+R" +
                "z6dFG5ThrOZ8WiTfx0/D4FLo8aO4XJ0eFvv4aT1vtM8cbUmPMz" +
                "C4FHr3BC5Xp4cdSh2vCr0dApp1VxPH191Rp/v40ap1Z3SlPofB" +
                "pdD9B3C5GCvUt0Gp41WhhygTLGwDBpdCjx/D5WKsUN+Ypw09b/" +
                "RUVZQJFnYeBpdC94/gcjFWqG/M03k9rwo9RJlgYZswuBS6/yIu" +
                "F2OF+sY8bep5VeghygQLW4PBpdDjJ3C5GCvUN+ZpTc8bTVVFmW" +
                "Chv9+VPGcednq/O0z+nHkBBpdC9x/H5WKsUN94Pl3Q86rQQ5QJ" +
                "Fpr34Pr5NP2Py/kUT5LPp4swuBR6/CQuF2OF+sbz6aKeN3quKs" +
                "oEC1uBwaXQ/WdxuRgr1DfmaUXPq0IPUSZY2CoMLoXuH8DlYqxQ" +
                "35inVT2vCj1EmWBh6zC4FHp8AJeLsUJ9Y57W9bzRy1VRJljYWR" +
                "hcCt1/CZeLsUJ9Y57O6nlV6CHKBAs7B4NLocfP4HIxVqhvzNM5" +
                "PW/0alWUCRZ2CQaXoONyM+efqKxyfWOeLul5UyRlUXZY+NEfeg" +
                "0d0397jR3F7+OLJ6l7sMswuAQdlytrbJi15nvYodTxsmE92p14" +
                "Hq//P8L0v06fxx+hfh6nfG/pPlwSt1Hua4YnVbfid18dTOwUDC" +
                "5Bx+Vmzq9X97BYd6f0vCokdggo9/GoW+6Ln3a5jy+8j8L7/M7w" +
                "1H3QzEfPE66bDia2DYNL0HG5ssY+zlrzPSzW3baeN0VSjVYTwZ" +
                "YY8QtcinNsrqyx5aw138OCpy09L1uuR6t1/AffTn9m");
            
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
            final int compressedBytes = 1805;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1sHEUUviIUCMkUkAYi0lCloUFQELB3s9tgsF0hFBcgAo" +
                "gIhCIDlYmSvTufh4ZYitxBChchioR8skBIUDhIIEhDRJBw4gIi" +
                "UiNoYgmJKOzuu3fvvZnZvd3bH68tdnTj9/u9b+dmZ39u3WoFP7" +
                "RawdXw81Ur3ILDwc9hv9nKtAVXhtKvUe8/FMu/GHHfoW/cLfgm" +
                "1fuTbrFVC74dSteSoxJrlDJOI+LCcVLP1TlO6tm0cRpnm/oCP2" +
                "o66lHPmiv0m+k1irHM47UxKcjgS/pEPcnZcoV+O71GMZZ5vDYm" +
                "xRhoc/NwnujOQvZYdbRV46aeKRuxzHV86rPkdXzpXp3rEzAZtY" +
                "53bxefT852efOpfbLot9nZzBefhX2ezVv1VrFHHTTnR7CRBSOp" +
                "SS/Gk9w9g16O5K12T1MeZyIxeV3lcZ+Mob2g6hF7kyPtsbn/mK" +
                "1j6xnhXN0I59O51PVyY8Rqu1HVeuP/nXPd32j0+nQ5eX0qyDLv" +
                "+nQ5y/pU1/nOYNdPQP2jMK9/ymGyO9cFBrv1xozTepPHqbrzXd" +
                "7N6ZUXNeK64PsSR7/2+VQm+/RxUrN7eZy6nWrHqft5ZUfCgVqP" +
                "uwN1zSer99/x55Oar24+nX3EuL87vhfX8Wicpu7Wer67u5vj1J" +
                "5Iz3bftF9nth8sfH/3cCqvB+xMEvdyl68z3Q+aso4nMSltPon7" +
                "O/dWecjFsPJm2+LL3Jv0++CR7BZaDdmqZlLwuDvVmOPuVJPHSZ" +
                "2s6r4l73M69Va54+Kd985jr54HCbRIxgiMUm+jRjYuTV2ELERA" +
                "P5f1PM5E+kiXTPUY8lH1qYtJtcyqHFXy5JXZdzA9aj6pd0Zctd" +
                "xsynxKYtL860z1bp3r0/KhCs4Na9i7azhO7hro4JORo3SORx4e" +
                "K+16FRNzFA9u4xzMWhzTjE39phKe+7pnM+ReGUrxc1/IacLvUj" +
                "b2RX6Xcttum3qQQIvlPrdIP/UUL1FRkjLP05lITJHbl3nSz/kN" +
                "fH0LXl9yk/sv0Uz0xPkUjDGfguT5VHCFyzufgizzKfvmr/gr2K" +
                "OOmr/irnMLRlKTXswy8SS2zONMpI/Xdde5T8bQXlB1iJex7jrt" +
                "sbn/mK1j6xnG88wPW3t465R8XdB7OvG4s7w30Xsq/bgbyNbjrv" +
                "dkrcfd0ZKPu+v+depBAg10buF+aJ0F7qUslDGeyxRNHr0Ob5wH" +
                "4ekRxA80dTqplllVMtOx9YzBSF9InX0F3s1RZ+o87tTBiq/Hh7" +
                "/ju+5eXp/KZu/3/B71IIGmAr+HETY/9SCBn6NSNpd5ns5EYsoY" +
                "mSf9nB9o3a9NPJ2bjsrRTPSk+aTU/+e7lBXIqWzF+KjOcVqeKP" +
                "k4nnfnqQcJNNC5Rfqpl/GEStnxOH2MsZSnM5GYMkbmST/nB9ry" +
                "hImnc9NROZqJHv7dwt7dCu4M8rdAB98QdUsbZavO8cjDY6Vdr2" +
                "JijuLBbZyDWYtjmrHjPC9w3s9/f5d2nVlwPfDyXWfa2Be5zvQu" +
                "eZewRx017NGCkdQ6C9xLWTpe/K1tUp6so1eWNSLd3eRM9RjyUX" +
                "WqptfiVdUFHZUzoT3iPMd5npn1fd/oeeaxl+p8npmtmsr81p03" +
                "6U1ijzpq2KPFm+x8ghrZuNT5NPp7bFHHi5kv2vM4E+kjXTLVY8" +
                "iHH+RgqwXewWgu6qicCY0D5zlYB15o7YutN50p6sXM1+N3/DvY" +
                "ow6a8x7YyIKR1ML7YObFeBNPYnMcyUT6eF3OVI8hH1WP2NtrmV" +
                "U5quTJ9B1/J5TiPrbtQAuP3atgIwtGUpNejDfxJLbMGzI2fLwu" +
                "YtpiyEfVl+5LqmVW5aiSJ69cxzoevdfj3V/oPiTnez3FqmU4r1" +
                "Tzvm/t7z85r2dBzRa1r8fpRKZxOlHNOLUz3V024X0657VUhtey" +
                "RKUinGs1clO/5Yt3Xi0vaj8fd+r3TKN/K/P1+A3vBvaoo4Y9Wj" +
                "CSmvRSlo4nsWUeZyJ9vC5nqseQj6o7ryTV4lUhiqNKnrxyHfOp" +
                "/s15o7yoEc9VHi/+XGXpOD1X4e9hLL2cez3I+fudjX2R9zC8bW" +
                "8be9RBc54AG1kwklpngXsx3sST2BxHMpE+Xpcz1WPIR9Uj9vZa" +
                "ZlWOKnkyfcabCaW4j20z0ECWFoykJr2UpePF3/Cj9rwhY8PH6y" +
                "KmLYZ8VF39mVTLrMpRORPao7DNeXOhFPexbQ4ayNKCkdSkl7J0" +
                "vHicDtnzhowNH6+LmLYY8lF1dTCpllmVo3ImtEdhm/VmQynuY9" +
                "sstHC/HgMbWTCSmvRivIknsWXekLHh43UR0xZDPqoesbfXMqty" +
                "VMmTV9auRIb/eaT+2svnu6X5cvHcI+4R6kECDXRukX7qZTyhUj" +
                "aXeZ7ORGLKGJkn/ZwfaGrHxNO56agczURPmk9lb36/zvlUerX/" +
                "ADOeC7s=");
            
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
            final int compressedBytes = 1752;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2IHEUUbjAqIu5CkD1FRMiCB/ViEDSrMDXTRqOuWVCP8R" +
                "Dx4sndQIIXmZ/96d3DCHrwZ8XdYyBuMBcPCioKkosiormJ7Eln" +
                "D6IiuJdo17ytee9VVfdUd9fU7KSa1Lzf73399nVvz+yQxlxjLo" +
                "oa/V0uKSlN7cqiIvHgXszS8aS0tm/PU8v00boK0xaDPqy+9m9W" +
                "LbMqRaVM8IxoBqzmvVHOak1FDqv5I7y2/x7kTUdRfDWqsNp35/" +
                "K6U7fkV2t+71z3j/afVoRuNKK19l8UcK3+NVr8/HkqjbqbTu9P" +
                "lRD2i8VXq+a7T+3FAn26HrRP1yd1npJbQvZp9Y6Jve42gs7Txq" +
                "Red7WPQ/apWrVxzlPtUtA+XZrYPl0J2qcr3s/h2/TftfTfp9Cn" +
                "5g/p/oVj7pcD6WfzOZNYvknv47dWYvlZrvc73ZIcsUR9Vfw5cw" +
                "zztBN0nnYm9rq7HLRPl0P1STxV9f0dyZuuyqvo+zsb+zLv78bw" +
                "/NQL+vzUCzVP8evV0RSG7FNFXvvlKvtcoqd20VOfF4Ct/ZHomZ" +
                "HDdNGz1aDW5DZq5z47u/ZWXl1q0zmg1NqmMbaIUvO05GGelsY2" +
                "T0vBrrtFD31aHFufFoP16byHPp0fW5/Oj+D+tKV2saX6JLbQqk" +
                "cO083M5q7C0/Oknfts7FJe+3l1qU2vzmtRTBvTTBbzYr5+u9pB" +
                "Ak3KnU20yFiucR388lXhKXzpBTyerezyUBb0IRMZldxHfRwLuS" +
                "BHeiZKam2rOoCJTE00XTfm+x9fU1p7w9/EF8fyWd06X2+ZUpls" +
                "y5Pfh1HA5buaSNLjxmAHCbRU7myipR/LNaaDv/96gDfAT/o2xE" +
                "40+w0Zq/uQiYxK7mc+hoVcCEd6JgdS+vyk6vQxkakFTdONvm2Y" +
                "UoGu5+Q03g86TyOoJvbULvYGz+N7Uk8eBB+PHKbTHT16LNqzfI" +
                "gp9tqv5dWlNr06Suk87VFMO1PHnr1jSmWyLT/hD4LO04iribdN" +
                "qUy2xXctZJ/8V8v63LfxtRm7rP2UinzuW5Flwc99bezNz31Xzh" +
                "V6xu+qPe62HlA20OOuGTlMpzt64q69Nhz57IbxoDa9OpVojPpn" +
                "Rxu2VJ/qM/WZKOps+ptbiVd+JQ9Xr9barvBOdT1exx0kedSn6l" +
                "PxevIIWrifZoIEfoqqJJAlHs/GeIykmDyG53E/5QcaVsNY6BOv" +
                "m4Wm681PtGu40N9R5d+DmxW+19R0/LRffD7I2CmakTuljzr/Xp" +
                "gRM7iDBBro1ML9uPN4RMVsKtM8nQnHxJjVe3g9jsX5g5Y8ZuLp" +
                "3Pj5czRdz58n4TArrvNkw2o6//VIZTvP01U3m/Pz2MnGSbUrXW" +
                "lqVxYViQf3YpaO15/xOXseZcJ9tC5lqsegD6uvHsuqZValqJQJ" +
                "nlF6LDQWUqm/920LcIDMLSoSD+7FLB2v/9NcsecNGBs+Wldh2m" +
                "LQh9Wxml7LrEpRKRM8oxTvoriYnsPBDhJooFML9+PO41UOSiAn" +
                "T6hYzGNXheajyIhpxlAfckweN/F0bjoqRTP0C+JCKh3sIIEGOr" +
                "VwP+48XuWgBHK8rWIxj/VJ81FkxDRjqA85YjXOkXLTUSmaiZ71" +
                "dwTX5wLXtfxbyPd3yZOe3y+eFqdxBwk00KmF+3Hn8YiK2VSmeT" +
                "oTjsljeB73U36gJadMPJ2bjkrRTPQq83R4V9z2F2V9GureJH16" +
                "019Uf6ZmxSzuIIEGOrVwP+48HlExm8o0T2fCMXkMz+N+yg+05D" +
                "kTT+eW3sXms9BM9Jt0njr+ouTqPN+pM/3pSuyOZVR5NtvnyLOR" +
                "511515VJmSjVp86pTs3sU/JCibOJs/tUbeX3qWN8e27tiBOq89" +
                "NDfBwO1CvN0/Fs+2iQs7xu1dw5xbNwoF7pbGaz7aNBzvK6VXPn" +
                "5Pf+lFllxNedpQPL/qLy70+lfupHc+7jR0Pen9yqledU7bmg9c" +
                "yheS5o+ovqP0OdECdwBwm05CW5Uwv34w4S+CkqZlOZ5ulMOCaP" +
                "4XncT/mBlrxo4uncdFSKZqJnzZO4a5LnqQz7Qmc6+OaYmI4meP" +
                "lmL86IM7iDBBro1ML9uPN4RMXs/qenuyoW83QmHJPH8Dzup/xA" +
                "w2qcI+Wmo1I0Ez1rnnyvxq8h58l/tazvYYiHLJ9JTsz3MGzsze" +
                "9hLL9XvU+2VaVPjaWQfbJVq/p9Fblqv/NXJckdbTwiD8fUsmRl" +
                "GY5LYygvWyayz+Yn8bKr2n21X/grtXObHmHHKeIBb75fj6G8bJ" +
                "lgMz3UIvHy+HJfkeuu80r56y55OeR1Z/t/HszrrnNuNPcnI/fQ" +
                "3seTV1365OP3ne95EmeD/r4763me/gcgRQ5o");
            
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
            final int compressedBytes = 2158;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW09oHFUYn14UvetBSnMoghRsC96KyO7M5NB7bRPFttRLpc" +
                "VDQFBJ2mxmO+uiIIgUxHrwEGpaSvEgKtFotE1tD61StFBrcvCg" +
                "eBeKUHHmffvN9+e92Z3dmR2TeeTl+/v7/ebN7OzMbuJ5rWue17" +
                "qe/HzuJVtrovVTMn/jef7znrWd+VD6rW8z6xcWva37WlfceMW3" +
                "1nLf7E0dcbG1VjPrVm9/PhhCQc46FepV6zS5I3+dIFfXOrnY7H" +
                "UaRlPeOjWvV3s+lduGXSeXenud4peL8sc/xF/Hy63r8fewTvGV" +
                "Yc4n6yg+bjDWLJZrmBt1i1f6ZlfdSnLrvytS1W9bmILfwa5gl+" +
                "e1z8lsGuu3Qd5VBXijb92p4epdbAsfF90Pe2tGzYhmsNIR7Ah2" +
                "NKPuixRJrTRGHq8HrMC83qGK8FMb8GQ31vBKjslrohOyT+a5fv" +
                "C00nTAOkHW3n+JZqO7z6cRX3ePjZarfivGNrqm+OdS6naOlhvD" +
                "Ou2srsp5fTo6THU0U7zWP1LnOo2brTWRMR2oDnXhlbJ4CyeHXC" +
                "cHW/fVEu+3ufcFzVsOptcKYWb3Be3TdF/At/ZcxfcFf1nvT7cG" +
                "3xfEf1ZxPjVvONbpjeHQcJ2s+Ny4X3dO9a+XeqXl3Y/fGf65ZR" +
                "Pdj98pcj/e/n3k698x26rgqnqsboQq1fd/3VX5fpdex0u+Ewx5" +
                "HT/zR8Xvn0f9ozSDBR74PMLzFONW4xOqInxp6z6uROY4L1eqa6" +
                "R+VJLHZbNKZRob/HA9XPc8mNMttdDDGSNYSUNmw/XGElVxPIkt" +
                "+3Czc5wXMV01lCP2xlIel83KUaVOzqxed+9m7w6nR7jL+NvbJF" +
                "s7qvh1N+vP0gwWeODziMzTLOsJlbrNUbmEtdSnlUhMWSP7ZJ7r" +
                "A4/YpEauTaNyNBtdHYdL4zrC4cU6z6eq2fy9/l6awQIPfB6ReZ" +
                "plPaFSt1F+AWupTyuRmLJG9sk81wcesUmNXJtG5Wg2uro+bZor" +
                "TLmt8W91VeYo7w5344w+ejhjBCtpyCx1aTzzvvCWu48rkTnOy5" +
                "XqGsoRe2d7HpfNylG5EtqjZOwJ9ySWmU1sDwyw46d4BCtpyCx2" +
                "2Xjm7P3R3ZcptnKcN77Lc7IG+bhGYqNa+DzTZuWoXAntUfq7yu" +
                "+l+j3f+XfrfL5zsdnPd1U8B1e9Tt2361ynzpNVr5N6jmqLo7JR" +
                "4TvrRp3drvoyCvx9/j6awQIPfB6ReZplPaFSt7mOv4O11KeVSE" +
                "xZI/tknusDr/OMjae1aVSOZvmNZCxmM1jgLWazGUl9Q3nCh7z5" +
                "3cPLrIaxCLuh4oumViKzWurOcgyrx2xiTOOi5oK9wdoUk5Q60J" +
                "Sf/7rzzybnwHtDnp1nR8sV+ATnRHkl9D1nlZ9nVn4df7/W6/j2" +
                "8b7fdR4e1zrV+7mvf7LadQqbYRNn9NHDGSNYSUNmqUvjmev4WX" +
                "cfVyJznJcr1TWUI/bkfjyHy2blqFwJ7VHY9Kf96WT1ezNYPW8u" +
                "nUVE5qelb+ze9ygYwTy3eZ84A1SOIxOmXcNzTOOcjae1aVSOZq" +
                "PnPQf7z23l5+Cq1YcHw4M4o48ezhjBShoyS10az1iH3X1cicxx" +
                "Xq5U11COaTycx2WzclSuhPYoGVPhVGKZ2cSmYIAtI1hJI5rhWe" +
                "rSeMY6Qn2SRzNLDq6D8HQF6evljuRx2awclSuhPUrGofBQYpnZ" +
                "xA7BAFtGsJKGzFKXxjPWfndfptjKcV7EdNVQjmncn8dls3JUro" +
                "T2KBl+6CeWmU3MhwG2jGAljeR8Ylnq0njmivEl9UkezSw5uA7C" +
                "0xWkDzxi01ycFao4KldCexT6k/cn73sezOmWWujhjBGspCGz1K" +
                "XxzH3BeXcfbnaO8yKmq4ZyxN55JI/LZuWoXAnt0eT94FqQ3GfC" +
                "nG6phR7OGMFKGtEMz1KXxjPHbpX6JI9k1iPN+KuIqfs5N9dIbJ" +
                "qLs3YvalStJPPXgrXEMrOJrcEAW0awkobMUpfGM+u07O7L1mnN" +
                "PdKMv4yYup9zc43EprlsVo6qldgd4/z7ArNOV0vcDV0df8dwW/" +
                "DZaLnB+e7l8gqK9+Bz8ChohZ43J8pjRM6/AgteqvN+vD9b1Pt+" +
                "t/vp5vtcZfLXOj8vcLFtke8Rvtpsnz+1/9mM6+SvlLiOrwz9+d" +
                "NKkfOpM7H51il4oc7zycW2NV53Qa3/pxgU+j/FztPjWafgVN46" +
                "8byu6q3TqVrX6VS/dcLs6Jrw7+kW5jvPbp1P5eafsL5bebPyO6" +
                "a63u+u1nk+dW5WfX1Sx+FC37u3eMDdXZxXFcSDeiu+z4yLZEfX" +
                "NGCd5geom8+vGtRb8TrNF8kW1xQmA2f00QtVJPSic6HHh6es6K" +
                "P0dzCr8YymWXefZzHbHFKprvFYHXrEprkg6zFNnoNJrkNY5/Xp" +
                "Rp3Xp4Xb1V6fggfBA9uHGM3cx0g6RzPpbx53o5rYNGaw381N+H" +
                "YFR0ZdpEZonJb74FKUHLsvOLJrdbA73BZuS84sM5tzbBsMsGUE" +
                "K2lEMzxLXRrPsB2gPsmjmSUH10F4uoL0gUdsmstm5ahcCe1Rgn" +
                "cvuCdW0PgQC+75S2BznzysJwvy/pJGTWPYiwx2DSFhnmqgm2q4" +
                "Lh1FDVolYhAaRahTKiL0uq5P4UN1Xp9cbKWuTxvBBs7ooxds+O" +
                "d5JPXRoxi3oB6qOF5y5M7zXsmjmW2OtJ/nZA3tBf5wpVSbqiOd" +
                "5nyyULkSWodkrAfriWVmE1uHAbaMYCWN5DrOstSl8cwRfpT6JI" +
                "9mlhxcB+HpCtIHHrFpLpuVo3IltEeB9f9SQXaf2Tyen8s5Ny/8" +
                "X893yNxPfUmGy8FlnNEHr/sbxCiClTRkFuttPIkt+7gS1wBErl" +
                "TrcOU6x/O4OKbdqZVkNf8BenW7FA==");
            
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
            final int compressedBytes = 2090;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWkuIHFUULVETAjG6cFxooMFICFlEAlEQjOn6JRvdB3fBlW" +
                "QhBHHtpJ10plxoZpE4BEdIHEWj8TfoQjDaOGg+RERwFV34myxi" +
                "ZoK6ECfBqrp9637eq+6q7uoybT369bv33HvuqdevX1fVjOPIwz" +
                "/l5B69sCL46A6sTArcu4vkFYuCY/Kr+HU2fn2SWo3Jb+P+TMyx" +
                "zYyd/l3lfp6Nvmfe73A09WTX86XMm9pbdiYmP+2JXjRmwKJ+8o" +
                "ts9E3pT+K0fxp7tMFyN4GPPBhJTaIYb/JJbpnHldgaMHKlWocN" +
                "S9Tba3FOM1MroRg1543sE7nfsp5Wyn0KuJ4M/95RfxOt6pcH5w" +
                "s2B5vhndvcp22NIgPhxGqvB4w809SCOEZM/6GZURdGSw1uM0+l" +
                "mM0mr54/N3HbGmyNR2mf+rZCg7H0YCQ1iVKW5pPcMi9TZWC8Ln" +
                "LaYgij6q6XV4tXhSjOKnUye0OwQcxhaoOPem5rlBiISbNKfmA0" +
                "Y9BDeH4M16W96QzsylMp1tMuzmxTi9n+or8Y71Rpn+5Zi9BgLD" +
                "0YSU2ilKX5JLfMy/bxRXvjOojPVIGVwXIfy6vFq0IUZ9VKMrvj" +
                "d+JR2qe+DjQYSw9GUpMoZWk+yS3zsnnq2BvXQXymCqwM1vSfeb" +
                "V41em/NKtWktlL/lI8SvvUtwQNxtKDkdQkSlmaT3LLvGyeDIzX" +
                "RU5bDGFU3fXzavGqEMVZpU5mX/Yvx6O0T32XoTlO+Az4yIOR1C" +
                "SK8Saf5JZ52TwZGK+LnLYYwqh6ot5ey6zKWaVOXlke7WdHdUUT" +
                "3VrnnUx7XbV8zWvNa9TDCCywuUfiSd86YMYTK2UnI+8WjCUerY" +
                "RjnFkqlTEcI41UTZ4T16ZZOZu23R3ujvh72u1hBBbY3CNx6mU8" +
                "5tCoi97AWMoTv9EK48zEacZwjGm8YfJpbZqVs5nseffB1vuN4+" +
                "XugzNPfB/sXh9m3Ze+D75e5D54anb45wWFckvM01B3a8ul52m5" +
                "2ucF+fMUrb155qn8eorWFJmnQ08Nqudg9jlE65wxPtqfjZaf5i" +
                "m8Ns7zVLV6d7u7nXoYgRUGSc89EqceRoBzVsrmY56nlUhOGSPz" +
                "JM71gZWo17Fam2blbNr2V/yV+Ioz7dNrzxVo8Tx54CMPRlKTKM" +
                "abfJJb5mVXxgbG6yKnLYYwqp6ot9cyq3JWqZPZy378TYM+9S1D" +
                "i/enO8BHHoykJlGMN/kkt8zLFBsYr4ucthjCqHq0Pq+WWZWzSp" +
                "28stqfHs52whcrvm+5q9Z9fONo+SfnqtwJI/bsq/3SOO/jXdZO" +
                "2El6esX9HhmBLxbRIawXc5E4yWmLlDzIq7MybA9lSDbi4Wcueb" +
                "AVWk+7TfTQo0N8E16udT3trphvX7iPehh1LTfphUfi+6QNOGel" +
                "bD7meVqJ5JQxMk/iXF8Xc00+rU2zcjaT3XGa89g353Efb863jy" +
                "Y9YNlTiHn1VMZqay/4uDf8jft1lf6c9gr2eD7iMbaIEtfj9Hs3" +
                "W+3KjSbG+fcumAgmmr9CH89sOgIrGXNPEksWRnEUODgfeIGH56" +
                "If6iSNPDSiGogjJnXQWfDqNo3IA+eKZ4cRCUaa0YeV5fOC1p2j" +
                "eq4Svlrn8wJbtdE8V6l8nuZqnae5qufJcbyr2HtX8f8wvKvk1Z" +
                "H9bHumjiV/Hkac/epyn67OY3kM8faqPth6ar8x+HqK7qtzPdn2" +
                "cXM9tV8v/XzlmHznfunTEXaeMgigvXEdw3XZMsFnItyT8PXSa+" +
                "U9Kt+5X/p0hJ2nDAJob1zHcF22TPCZCPckfL30asxbxd5bzfan" +
                "1cSO7gFMRvazeU+IjiV/Hkac3mprf6+63Ker81gegy87W9H9yZ" +
                "/w42uGaGN1v3f+RJ37k63aeFwXeBeHmafWc+XmyVZt6OuCK9h7" +
                "V7Lv3RXy6sh+tj1Tx5I/DyPOfnW5T1fnsTyGeHtVz72POWaOKr" +
                "g7OlY3Q5XqrfyvdN9P4qhK1voYqlRv5Z81R1Wy1sUQzFY/N623" +
                "0v5d2Me7vu4zmdY7WVR87TqpfkFbB7LRR933k5nnvdbb2b75U+" +
                "vN8LWc6u+n/cd9fjH+7qH/1PP3dkcfxq/0niGvWum5+aC1YNXT" +
                "EM+MHqim2sGn493ywlB6z5SLt1WLNg2xnmeCGezRRiuY8Ra4By" +
                "OpSRSzTL5U+YI9jyuRGK8L2cRnqsDKYFE1ivUW6IzN88dsza0z" +
                "9HryppwxPqpW3/5RzdPI/soWHq9znqqvVuq5yi9DPKebH0pl2e" +
                "d080Wux1/4eWA9jVK764ESn/CJWtfTif9yPQ1zfxeeq3U9natr" +
                "PQVfV7eekuuCeo8i6gc53B/kO/dLn46w85RBAO2N6xiuy5YJPh" +
                "PhnoSvl16OhUfCI9ijDVZwFnzkwUhqEsV4k09yyzyuRGK8Lleq" +
                "Ywij6ol6ey2zKmeVOnnl3O/d+Wr38Zq/d+er5Qtnwhns0QYruA" +
                "A+8mAkNYlivMknuWUeVyIxXpcr1TGEUfVEvb2WWZWzSp28ct56" +
                "iraN8/X44dvqui5wH6n2umC441BQ7rrApn7Y575Cz+PO/+I4vK" +
                "ZQ1NqBV9fcqJRHO+qcp2L/13N4fT33LTfvMf1Eoc/uwcK/nzuD" +
                "ndijnTR/i78l2Bk9RJ5klPh4k2iCUxTncxzgs+VxJRIjO74C2c" +
                "8xGUNngS+uVNdCnfr8MVtzd+1LwaV4lPap7xI0v+E3wIeeZJT4" +
                "eJNoglMU54vnqcFzeV6m2MDIRpz4TBUQR0ryaqFOsjgrV4I5Uu" +
                "eov3fe7bU+pxtxtdE9p3P/qXOeKq/2L9c4dYw=");
            
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
            final int compressedBytes = 1553;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlWk2IHEUUHlkv8ZKLSBKEEBBvC8GL1+2aHo0QFGb2sEYxgT" +
                "3qQaOIIITsTm/SOxAC+WEx5hKMuu44xDXu5uDB1QREBH8QvHgz" +
                "Rw9KEBYlsPZ0TfV7r7r6p6aqa3tiN1NT9d733vvmTfWrmp5uNO" +
                "ixcF702LmG9pFnw6YaDg/b0fy23xatGIuRaIVEIOGkWr/d2gco" +
                "7G/Ya+1T22EmVIfjYqYyBnQQHaLJsXBUjsJeMRPIQ3TO+rNRL2" +
                "5j2Sw/eZ9KBBJOqgUr2R/1Te2SPKV0OK7wqcKADqJ7W1mxcFSO" +
                "wl4pTzTu+J2oF7exrMNP3qcSgYSTasFK9kd9U7skTykdjit8qj" +
                "Cgg+jezaxYOCpHYa+UJ47cugdcwydb94ZjLoMWj2Ut2GNPuI+P" +
                "IZ56ozqKw5I0BnycPiDjvA3KNe0Jo7IOsA5eCJpYEzyX+PjJXh" +
                "UMjhp78PXwNtnH8aM8Bc8GniJP34/hrbU7eQqOpPJUin3wjHme" +
                "xjlaj2bnKUtXTZ7KRSvPqbW3tRda3uMj77dhiyVYDzLc43joCz" +
                "3uy3aYCdXhuJipjKH8+WjIXh0rHZUyk33LFqNK/lRVO7/eEZf7" +
                "zOWHbXtc+DZ6fRe9bsWjgws/R+1XJW23kt6vw3bm07j/Swp3x5" +
                "jll7naH2QJZyKhvk56P5ryWVwzsZ7pN2pyVMXEu0vfsZzKZITa" +
                "j46Ga/P1MgbzUllyWVqDJUN/eXwJNvACaHmPj/gYS6geWooHr2" +
                "CN+9iO8JJ02DNlSjFYhzhOpf3J3KL+VJa3tHed+rT0fn59GvWV" +
                "9WnmvtP6dL9MfVp6r5o6bpKn3lGXeQqPlcnTmfndWe/y8uR2vf" +
                "MeKpMnFarkejdH7nbdbVg7zHzpWqvwZgyy5lPYrtN8OuNrXncd" +
                "+/un7idxO+B5Gsk+HL0n+7Xu9UibWYu7N+Nv7I3uB4nkRjfZiy" +
                "383v040/KzuN0s+Eb+yeHfP31g1Ps8en3Emdi5Crrr3S9U8nDO" +
                "6Op4uy77zKqZhC8asXurNnmqiIl3mb5jOZXJCLUfHQ3X5utlDO" +
                "alsuSytAZLhv7y+FKdzX0Be7Mu+wLOpKiOB6VXwPC45Av+v7sS" +
                "7Q3bmrP9yni6EvX0VXtMrNxlOZhEWhmjKqyMp6ugPq24ylN8b+" +
                "0Ji8w3neZJEW3xmuk+UzmfTo3B7pS6PpkfwR+52r/UTArr0581" +
                "nE8bTufTRhXzSbXeseYk/w5WsTdZ71Lr3+ui5z9dYhU66Wp+LL" +
                "6mh+8ds165Hd1X6R13OZ/OPl7tfGLz6Z6FajHv2oNN9sqVY6C1" +
                "+9O47thtgyzdrt5Cc36fryxPBrVc35bdqTZP4TtV5an5sst9Qd" +
                "XRvG92z9r95xifLzxNFb5reT61q8uKuJ9ZXTR/2p8WrRiLkWiF" +
                "RCDh7J7EWrCS/cXMO2BH48iRaQzMA/zJCODHRxBNjpWOir1iJv" +
                "CJsMVoP/c8qYbS6scKVkOuV6HYgA1Mvk9dazUHuTc+J/r7jklP" +
                "M7CCpxu4XoVifWb0ZISutZqD3NPx+oD+z/lKmf24CvX/ylN4rk" +
                "yebPy+612Y5DwtvWQ7T9J6egv32Kp0ha8WVIXVLBRbLbIt47mY" +
                "dR4e7j8JrQ6nB7Q+7S9Vn/abr3feI9XtC3sXK9+PW2fvaj6xLY" +
                "Orbkv7fuZWmfkUXrVQxy9P8nXXu1QmTzr36bLy5O2Z6Pq0x+56" +
                "1zzRPCFaMRYj0QqJQMJJtWAl+6O+qR1mojoxD/CXZkF13mNZsb" +
                "BPjsKWMhOwyJxP0xM9n6Zd7TO9QxOdp0PO6tPhic7T4eI8Bf/q" +
                "zqes+wVjfibl/+atdROfuv+bq6Ip9gXXXe+fCnDO59Py33l5Sn" +
                "YPV+uXJ2/N6XW3ViZP7vfjJfLUd5qnfj3zVFzHm3Mu86SKpqhP" +
                "N+qXp97AZZ7KPV+g+StzW7RsW6x3bHs4Xr7AdRRZNMYtaGQsyL" +
                "N04JNt0+czVXghk6NDb/EaxoiX2tsuzqf1+s0nG/tM63nanOzr" +
                "ztn9p7Mu86SKZvr8E9sRLdtJ6tMOSGVk0VhtKWNBnqUDn0VxsU" +
                "yOjrEYA37zoifHfy+czYY=");
            
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
            final int compressedBytes = 2469;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXEuIHNcVLY8d0CIOGgiSDLZsZUJAVlZGaOME01XVJFsvHE" +
                "wghNi7KNpoMYuAkednTc8sAkIbI22EtpMghYCNbOLEys+JiXGU" +
                "OF4kZIyRYRbC0koiECfvvVu3zrnvVfV0T3+sTj/0+n7OPffUm6" +
                "rqqpoeZdnS77Ns6R3377XMvZYeX3rfzW9l2cZvsl1fS7+qrQ8o" +
                "eiPBOa61/2YjvJbe6Jv9cxxZf6gB9evaek/eVz8etH9xtjibfy" +
                "pzloklnrd75xDxWHiK4qxwMJ9EfVb4LFr7+IFIPHxd/unKSa6z" +
                "OrAV3D3VuHxJ+8i26vYqwuegWWOVf6u4lf9HZlcXLPG8LRGHvC" +
                "Uz8hJjX/L+HXyCEh4w8buP+6ER5KDEo6DB9tXOEoNG3hIglUc5" +
                "LdbnoNmyFzeLm/kdmV1dsMTz9upFPzvkTZmRl5j61b4ZOJRFbM" +
                "0Ln63WuB8aQQ5KPGrjF5yzXNACjbwlarn96Y5iPSeUojM0W/by" +
                "eHk8y2T2L2/5Uewv9pfHe+cQ8RY8xNgShpQvdNvfXKevNMd9lb" +
                "MJgxy6oxuwy5fAw12ZlZVgi7iiOsP9RN6XX15/K5vh1/KPJ8u/" +
                "9K3qGJov5t3nwcXxMXu+vb82Xh+9m+xPe16ZluuCgWqHuC7oXp" +
                "7mdUFTt/S64H5cpxH382HX6eVB1qkJNew65V+a5XVqUj/K/tRZ" +
                "7CxiFks88Tli835eOZ3iwYpqtlENfNyHR9jqN1mpxfBWQGP+Zs" +
                "oXa3NnvWttbCl7633Lb2d5f+o9N8j+tPbwXvWUD6ZW+2vl9ODM" +
                "vYVpXheUfxj7lcCUzuPlvmnuT03dRjk/FZ8Vn6W+xDCzrxGNgQ" +
                "FMMWs4F/yO8fLe1Bv8KYKZVRergYazj9ptaFIUM7fFfG05V865" +
                "1Q9z+DnMySiOFEfKuXDfUkW85WM8bNbngWI+1+sI13JdvQckOf" +
                "juCD/JOYtR/fjHSoGt7lsqnejKrKxE9YWK7WLbbUWYw9ptV+Oo" +
                "G9scCdbROo8YW0eBYj73fpRrua7+2SU5+JonvkSF4KCkrZfqhM" +
                "esZg22uXN0bX+iftL13njPg/ncNM/j4+5WXCmu6Kx+GAtuXPH3" +
                "dxoJ1kKdR4ytBaCYz70vcC3XsZKm4TPu7PYO11k0bwWUpL3kuN" +
                "NsvP3aKebm1SG1W33WdGuXNe+b7/x7cnuPdoaCSXYLR9vfauuD" +
                "WX6usvHHCa/T32vrw+GrOz+c2Hb/9fNVUlwvruusvnid9yWGiC" +
                "IxbFbxsFfPaJaZiuurL6GOlTQNYWSlsQ7k0N2rTzWCJ91+24lr" +
                "grVT7DgrzCG2I8Ot04cSQ0SRGDareNhunXZS7mLHrdMOZ+I+cQ" +
                "/Na85ikEN3rz7VCB7uyqxWJ/ylq9G1/U/rPffdAe8oft6eWz3T" +
                "En+pqt0a+v7lZwMed+9O+Pz0j/oK5MIerlou7C03geunCXTLL+" +
                "ucX157RmPi55dT5G4+8yHDWBuPu6Scu+ngGGtIezFnih1if/rI" +
                "fMq8McafxutT3Z8m3G3942HQwzx/mvL10z/Hy9d5qvMUZrHE87" +
                "bfnxCxeczAW1a1xM5fUyzqYiWW02Jsnc2zPvHQzWpkbTErs6Xs" +
                "rb9HaPisWrvQ/zmd1NwXv0c4M9Bz31dHf565+cAsPx/fzAZZp1" +
                "deHPh6/E5xR2f11dNZI4rEsFlUxXzButZcx0psjvuy0hiDHGm8" +
                "1taLu258FLOyEmwR66z2xB9N6jyevz3Cp9fbk6/o/+q+0H0Bs1" +
                "jiic8Rm/fzyukUD1ZUh5/K9xULnlgJ55jZKrUYzkEjutltYm0x" +
                "K7Ml/otdd4zqLJZ44nPE5v3s1inBaw0ssYtnFQses05RjpnBmW" +
                "I4B43oZreJtcWszBb75YHyQJbJHJ6ZH5Ahdr7FEe+rhxhbghcU" +
                "87njYItrbZ+4c9rD13POYlQ//rFSYPMt7ZNX95UpKyvBOrhxo3" +
                "SfTjKH2A0Z7qrhexJDRJEYNqv4lM9y27p6nZIc91XOJgxy6O7V" +
                "N/dKuzKr1Un+qfKUs8IcYqdkuE/WOYkhokgMm1V8yme5bV2tOM" +
                "lxX+VswiCH7ptZW6+0K7NaneSvl+vOCnOIrcsQ20YUiWGzqIr5" +
                "LLetqxUnOe6rnE0Y5NC984O2XtxVUMxqdcLPD+eH3XFazWKJJz" +
                "5HbB6zxWsNLGtznfkkj3LMDM4UwzloXP9Xyhdrc8fMF9rYUvbo" +
                "mvXxeq2/PvpVx9p3W+LPT/o+eBzqzU+xzEvMYonXecLPHLF5zG" +
                "JJnln9OiELbF6uPc9xm2UcYzjHGM5Bo1cfY622lJXZUvZhvq/S" +
                "e3hW7u863xzk/q4JNfR98KGGddo/K+t09peDrNMQx918Po9ZLP" +
                "E2H8nnFdGUxyyW5JkV1WxzXazEclqMrbN51ifexhdTvlhbzMps" +
                "sd9d7C66K/NqFqvyim74diJFbH7R+pKv7gYW1UYWWNSZ+5Yox8" +
                "zgTDGcI41Fyhdri1mZLWVvO+66376fnj+9Ug533DWpH+W4Kz8p" +
                "Pyn2yezuVYMlnrd75xDxWHiK4qxwMJ9EfVb4LFr7+IEILPQo9q" +
                "2c5JzVga3g7qnG5UvaR7ZVt1cRPgfNGhM/HH/3dM7v6fVTfg9R" +
                "OprvRWe3Rr+5MsYi3pYD5259ORZ3ZyxjwNuve/0J9uXoCvBP0/" +
                "x9y9rEvlWy+diYn9Od6J7ALJZ44nPE5v28cjrFgxXVbKMa+LgP" +
                "j1SpxXAOGjcPp3yxtpiV2VJ2d5RmOhdZ72D1FDn47vxkn6bHT9" +
                "cb/aIhVyRYxNtyGelaOdmvL8fi7rDc+clwNitt+aSIvofRe7S2" +
                "HhvsuOv3PYxdj/pHhr7eHPB7GJtfmcDvmG/rnN/W81N+G9EYuZ" +
                "vfXBlj5fzk48055tytL8fi7oxlDHj7da+Pz6e7TxdzOoslnrdX" +
                "LyLisdazvuTDflzxKb/PCp+t1rgfGkEOSjxq8wnOWS5ogUbeEr" +
                "XccTenWM8JpSlb4t/t3nVWmEPsroziYHGwe9d/z14j3oKHGFvC" +
                "kPKF1TvYXFf/xJIc91XOJgxy6I5uwMr3fdOuzMpKsEX+vfU++M" +
                "lZ/n3wuP+fh+TM+tXqE+FQcWjMfx98aKTroYXRu43z74N7X/s/" +
                "+bvX5UH2pybUsM+fOt8Y7zr1npzmOjWpH+24a/1ez3fGu06D8I" +
                "1vnZq6jfP8hN8jhG5Xx3iVdnWa1U340RS0Hnd/meXzU5P6kZ4/" +
                "nS/P66y+H8Wx4lh5PvydYhXxlo/xsFmfB4r53OfPMa7lOlZic/" +
                "DD3ylSzmKwFfqPlQJb/Z1ipTPefq2OuSvM/wAuFrmn");
            
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
            final int compressedBytes = 2501;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdXE2MHFcRngUjgfizjEwullaKIoQT50IEXLhMz4zAkMhHIs" +
                "sHpARBWCCOheACZmZ3dsZDI1BiJH4CQUHIlvawlxw4WEBMIpYI" +
                "OfyEgC1DxCxEsnLcnNgT773q6vrq1Zvx9PzhpZ/mTVV9VV99/d" +
                "zb3dOz3uaTzSdrtWaY/eYtPxp3N+6mGEe85WM4NOpxyUK+Wo34" +
                "UnW8WUx8xoXPqqA8UTKqF+sUD1lRCddonbQNPsBW/UZtbtv6Y7" +
                "MybPy6Wv481dPW2XGvl9zrF8Fb7fzJzU5V/RWbm38oqn2+tP4K" +
                "0bKy+80i8qKu636jssorY9GXzTol1HeultYfCh3/mnrVVum9cX" +
                "/jfsfzY4362LiNcJu1/hjxTb/lFatT3dafnXQ/7JbtZDs8s09e" +
                "/RrFJMKZMjTK+ZZPc+s6VKIx7ItK4xzBpLtXn+5luyKr1omd08" +
                "fT/Lf8w7UlbhdOL4a3/nf9jnEdizPSPFUQQsfjcQ7qSlVSzCIY" +
                "8Xzj9CLWuti6yDP77LUuZq9hhDNlaJSrLF84el9L16ESjWFfqh" +
                "Y+q4I7kyfdJNfvDWrT+8/VMXdckfq5y/4xvyN2Nq6q1an8WRT0" +
                "PjrqviCZ/ZFq9wVlxN0X5I2Z7l4q3hf0j01yXzD5NmhUWadBNs" +
                "M6feogr5PheqY8j70zcTx9LLpLPle7Q7eU+lm2Vr/Vzy7xTBZ5" +
                "5EvE52pP+4SH80DBx5ZHkUnqKO4H8Qkz5ko1Y8iFeyEacU9wnz" +
                "jXc4pSy2bYT2fuToNnssgL9hZGvK+8ZL573+II4yFW1kqdOstG" +
                "GDILp83hWtZfYFsJvq2yz1aKFdliv3m5edl9Ig5z+Gx8mYY7ct" +
                "9OMYlwpgyNcr7l09y6rvzkbjDsy5ypHMGku1ef7mW7IqvWiZ1H" +
                "fg6+b4KzWYXz+Gxbr1nxc/B98z6Pj1ynexOfPT5dbZ02T6fXaf" +
                "Phha/TvYu93vUeLDut1g7wtmj1sk4He6vX55cVrnnnW+dlJou8" +
                "/GE/Y0TjMpNFOLJKNdpYFyvRnDpH12kc9ZF34ZDli7XFrMgW+8" +
                "0bzRvujB7mcG6/QcOhT1BMIpwpQ6OcL3b+WUaRSdeVVx6DYV/m" +
                "TOUIJt29+nQv2xVZtU7sXOVzyyzXu3ymZ+SL+dwy+XPf5s3mzc" +
                "Y7aK7VyCLP2xjxueJxFqLEgXwUJR6s5Tj18UMiYkkPxhnTOmQv" +
                "sHtKI/PQvvLecYbHRDPHiop9N1bCM+MV//K+e3cxb1PE+yGDMy" +
                "ljn/MJBXxFWMsn0itcW3agsUKDMeTnDNJW9tgvtO7jS7SJBvHL" +
                "riusRl7lvJ94ys4c+9nx7Li7My9mssgjHyMal1nnc41YZOdf5F" +
                "ypU59bIgyZhdPmICYa+8csX6wtZkW22G+ebZ51R1aYwzF2lgbZ" +
                "2XMY4UwZGuUqyxe6PZeuK88ABsO+VC18VgV3Jk+6Sa7fG9QmHn" +
                "aKuQu+h7KHnIpiJos88jGicZl1PteIVaDXOFfq1PEUYcgsnDYH" +
                "MdB4zfLF2mJWZIv95lpzza1YmMPardEgW0c4U8bGOUSlKubT3M" +
                "gD/7IGw77MmcoRTLrnXxrVy3ZFVq0TO0fXzu+WK/2dCb7Njp7T" +
                "TVKznC3/8rK+N08+93339PdP9f8s8/4p1W0xzwvyx++k5ypV1+" +
                "nCoUnWafDIYo6nme7Hn7jz7sd7j0yrJ/ugtaap/l9v81bSylu5" +
                "zGT50TjROOF9iXjLx8TDfMpsnAj3ryc4wriPSa3UxUo0p87RdR" +
                "pH/eTFSrUi0hmzIlvsZ0ezo271i5ks8up/8TNGNC4zWYQX/5pH" +
                "2RZUcqVOHQERhszCaXMQE41efZwba4tZkS32G8PG0P1rhzkcC0" +
                "MaZOsIZ8rQqFTFfOFK/ZV0XflZymDYlzlTOYJJ98GhUb1sV2RF" +
                "JbJH/r31pjr63/Q+xWRGP0alHpk0q+bXbHFvzMOIzUGOOK/5Sa" +
                "3VMk10VoJ9bL+grgmrbXe9a4erV/tv2a6bb7pXeE4zKJ4gtfeC" +
                "97lg73fk7urzwPM+c2bdDfn/bA/bZN0y16O3dt5W2u/pvL9zV/" +
                "v59lWsdlV/bsPVtF3+ZkX7jVQ3p+kLAd1p/779R45VuMq+Jczv" +
                "7RzOTmYnHWsxk0VesIcY0bjMkl+qPMk2oEPOlTq1ZxGGzFwtdR" +
                "pHfQU2TPANtTasjNmMf092j7OKmSzyyMeIxv28cc7mc41YZOdf" +
                "41zhUesUYcgsnDYHMdGYf9XyxdpiVmSL/e6pbqP78W75vUP35E" +
                "x3GXel490HR2OTbd2x3991PzGpkmmyQubh1mGZySKPfIwgTmPj" +
                "HKJSxTbnh6vGFanTfTDT9kAdwhdniD7ypFvcy3ZFNlSCfnYmO+" +
                "OOrGImizzyMaJxmXU+14hFdv51zpU69XMXYcgsnDYHMdHYP2b5" +
                "Ym0xK7IZ/4HsAWcVM1nkBXsbI95XXjLfvW9zhPEQK2ulTq1ThC" +
                "GzcNocrmX9Bbad4Nsu+2ynWJHN+KeyU84qZrLIIx8jGpdZ53ON" +
                "WGTnHc6VOrVOEYbMwmlzEBON7ngyfLG2mBXZLHvxvOaWfmfLz3" +
                "V1p1O/Nfab6JG5o2yO3J4Xc1BXqlLUj9bn+UZ3TWP1oX7HuI7F" +
                "GWmeKgih4/E4B3WlKilmEYx4vnF6ERucHfmcrneQn9PltcX8Xk" +
                "99V79jXMfqu5PwVEEIre/eXqPkoK5UJcUsghHPN06vxkYeT31b" +
                "u/n0/9fx1JvDc9/6GZvbv3RQ1iml3q5T/+fT6tn8d9np+Pyemg" +
                "7OL/Yp76b5vZN5qg986/V1mckiL/+2nzGicZnJIhxZpRptrIuV" +
                "aE6do+s0jvrIu3DI8sXaYlZki/3mpab7WaI5PN+6RMOt0/coJh" +
                "HOlKFRzrd8mlvXlU/WDIZ9mTOVI5h0/9a7RvWyXZFV68TOS/te" +
                "6ulZjvuqv++76P/fsv6ZKtlV/n9L/oNlfi+Vf3+x/L1XF7ZOP1" +
                "rqOv1wAd8J7vGc7fE6ZXveHzxFmM68nY+zIHGuxEdhwpntbayN" +
                "64uxuLtY689iDr/SbLc946wu4njyf+chf2apx9NP5nwsvZ69zj" +
                "P77PHMEc6UoVGpivmC8p+m61CJxrAvKo1zBJPu/WOjetmuyIpK" +
                "ZI/cuJ5dd1aYQ+w6jcaRxpHs+uApiXhLPImhRQyWLzz3PZKuKx" +
                "UbDPsyZypHMOku3SSX/s6D7YqsqET2CCum+bmr9JPws2X+3KXu" +
                "C2a6vv2u98velc5LvRfo/qn3Yvn57tWpOX9bfg9S/H2VXvT3JK" +
                "r/fZXer8aib5jPGWPV934T5lsznK8etdYczoKPLpthnurHH09T" +
                "fVpc08dT2WVnZp3jj6eraSVzPJ7+C/UWOwo=");
            
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
            final int compressedBytes = 1644;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW79vFFcQXgn/CWmokAUWqXwSliJZornbvYL/AOLKMrIcEA" +
                "09xmeTI8cVEdRRZKTEfwIUKGBiRKAI2CBRxlJIkVQp3KXKvp2d" +
                "nW/eD7N3tz58sPu072bmm/nm2+e3d3uH6L7o/tJ91HnZ3ek8jN" +
                "Kj+6yzF0WdJ9FQR+ubjOO5He/+Fo14dB8fij71Kwnm/5rNf5fv" +
                "n+wn+zyzb0Z8Jj6T7PfuScRYJoZDowaXLOSLIuLz1aESjYkfRR" +
                "tXEdM5chV8olLJXb/PfYxO+/q52ubG1ZGj8z1bzS+jkY9vvw7E" +
                "L0ZHfFShHo/4bnyXZ/bZ45kjnClDo1Jl7OZNqdbcug6V+IZBDB" +
                "fW6Wwf1rwZ6iXXoq9fopobVwf206nK/qI3KtwdNz5m9+IveYLn" +
                "+ES8yTHj9+4Rlsc20ZNKG8dZMu1aiYewIiPl3bga7ms65/o33e" +
                "5ird9nmzj9SgOfUI10HBQzWeSldooXkSxXe8onPHvN+Qp+4hHu" +
                "hhU/MLk2JkpMFkULTHGJFtCIVyLM3Ic5D4Jslp/eaelndudlej" +
                "6k+46fC5LfP7zKne3CegfRt07es1H3fOfRoegr53Pco75TPD10" +
                "Xg/8xDPfmpeZLDPik/HJ1rx5LuCIxrGSLMKRlS2yDZ+ulnzJRE" +
                "6do+s0jvrIk26SS88Fum+IzWUP7afmu0neTz71o+ynZCFZ4Jl9" +
                "9njmCGfK2LiOqFTZfMbqP5E63cfurHugDuGzM0Qfef3HoV5uV2" +
                "RFJXJFyULrYit95uOZLPLIx4jGzbxx3c3nGrHI7j/lXOFR952F" +
                "IbNwujmIicb+tstna7NZkc1lD953Zyf6vjtb8X23lCzxzL4Z8W" +
                "w8myxl3+/yiLFMDIdGDS5ZyJe+i89iLdahEo2Jn32/A0znyFXw" +
                "iUolN/9+l+u0r5+rbe7cX0lWUiubs9gKjXgmnqEYR4xlYjg0an" +
                "DJQr50nWawFusKxQ4mPuPC56qgPFES6sU6xUNWVMI19Bq679rt" +
                "Sb7vfOpHue8Oec58Pcnr5FPvrtOt9xWs095Er9Peh9fp1n8DfA" +
                "e+lo4pmlOPLIqlNkayXMGvQSWjxAF8eZR4oJbjeZ+pvM4/pgpe" +
                "qVM61FUU3b0apyBXrndKeEQzx3K/H/dTK5uzWD8fjXT0MZJZjQ" +
                "KXGFoNyUK+9LWBtVhX/MX6/mEQxoHPUUF5oiTUi3WKh6y2EskJ" +
                "3Xel7gbrvmtdOS73HSkZx/v4MOuE70/NterWqbk28HPm2qSsk7" +
                "2f4oVx7idft2O6n1Yr3E+rA++n1arXKfT7ePIqmuCjevXB5/EL" +
                "E/08fmFsz+O7E/2cuTu2++7NRN93b8Z131X9eTfe/eTNOpL9VB" +
                "/1Og1xH59LzvHMPns8c4QzZWhUqmw+za3rUInGsC8qtXMEw+6h" +
                "Xm5XZNU6wZ9L5lIrm7PYHA2ydYQzZWhUqmw+za3rCsUOhn2Z05" +
                "cjGHYP9XK7IqvWiZ3r9/FhPu96L+p1KvW73YPhsDL46AoGr6lG" +
                "k3usf8VWbzeqj/Bf5ufhsDL4UWjSGeUVjKoV9tPbetfUz5n1Oh" +
                "3R8/jp5DS9oo8x27dRZhBcWP39iBErXS2M2zy6m5w+DX6V4ZUI" +
                "r006ppPpzJ7OsczHmO3bKDMIzixeNdPcASt1FeI2j+4mp0+DX2" +
                "VwnaZ9WkNX0/ujvsfKHL33n8h1/Fkq66+hnwv+rfdKqRX+p16D" +
                "Mt+D698L6nWq9PfxH+s7LHy0t9pbbNHpZvApGVhzGHOZPM3pV6" +
                "A1ukq0Rnz184iHM3PrvDx7p71jZjndDD4lg/N8+chcJk9z+hUg" +
                "D/PaVaIRX/08eOWahwfkXW5flpks8sjHiMZl1vnCKtVoY53Sb2" +
                "HIrJXqHMREo4/P1mazIpvLPsj7+O2tT+t9/PZPR/N59/muU3Ip" +
                "ucQz++zxzBHOlKFRqbL5NLeuQyUaw76o1M4RDLuHerldkVXrBH" +
                "8xWUytbM5iizTI1hHOlKFRqbL5NLeuKxQ7GPZlTl+OYNg91Mvt" +
                "iqxaJ/jLyXJqZXMWW6ZBto5wpgyNSpXNp7l1XaHYwbAvc/pyBM" +
                "PuoV5uV2TVOsVvn2+fT9/R85ks8sjHiMZl1vlcI5a2sU593lkY" +
                "Mgunm4OYaPTx2dpsVmRz2T/n7y1V/L+NqtfpzheTvU71vyPUvx" +
                "fU++l476c7pz7OfmptD/z/W7bL7Kfvfigt4X8QLt0n");
            
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
            final int compressedBytes = 1323;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXE2LHFUUrX1+RLAJ7rNxn1TVrPwN2TgbIf8gqwqJTAYhuA" +
                "wBUXDGLFTQjYILFQbEjSJDTDZZ+StmyMJOPW+fc+59r+yP6TaZ" +
                "vPfoW+9+nXPyUl/dkDTN8GvTDL/NP9838zFcHf6c25+a7Dh8R/" +
                "3h58XqL4qe+r7hpNlwDD9OZn9fCuOXxeqPdLz399p6rjZbGHc/" +
                "bN7w0Z/2p7Bplbzkc4TziPHKumxteV77PlaiOeZlpb5G9Wtl5I" +
                "qsqsxjW41edw/enbruwtm3s+vuo24b191KV1rdp3X0vN/Ukbs/" +
                "3envwKZV8pLPEc3Daj1Q0c1r7vNKFFNrtE/zrC8qUyTlLaFFdH" +
                "duflrPndy4eeXmFdi0Sl7yOaJ5WK0HKrp5zX1eiWJqjfZpnvVF" +
                "ZYqkvCW0iF7Pp3Wed9Pv4//f8+7teB9/80f3onth1nzzzFrEKj" +
                "E1iy6Pp9jax0o0x7ys1Ncgx+wlrsjKqKoTfvu0fcr7lvwUg2Xf" +
                "Zw0BR6zjsHr0eW4goDbWsC4fnVJZHrk8/8lWuT/df3y57k/3H2" +
                "3nPn5v/5Ldxz+oz7vtfg+u111+3BhuDLBplbzkc0TzsFoPVHTz" +
                "mvu8EsXUGu3TPOuLyhRJeUto3u+OuqP5k2+04zPwKM201ohVYm" +
                "oWXR5PsbVv8YQOOeY1zFwNcsxe4oqsjKo6mbm+Z9b7+PZ+pxse" +
                "1jOnnk/rn0/ty/Zl9FMMln2LWAwIQPKoim/9eW7gxwpGNl2sRj" +
                "WwLSvS+lzsVW9/3p83TbLjb5znaaa1RqwSU7Po8niKrX2LX2BD" +
                "jnkNM1eDHLOXuCIro6pO+F3f9fMn32jHZ2CfZlprxCoxNYsuj6" +
                "fY2rd4Qocc8xpmrgY5Zi9xRVZGVZ3k73V789Vox9hemmmtEavE" +
                "1Cy6PJ5ia99Cccgxr2HmapBj9hJXZGVU1Qm/P+vP5mfWaMdz7C" +
                "zNtNaIVWJqFl0eT7G1b3EFhBzzGmauBjlmL3FFVkZVnczs7lw/" +
                "xNXrP3ahtb4X1H26wPen/XbfrPnmmbWIVWJqFl0eT7G1j5XkJu" +
                "sAXlShOa9MVRhm7PRKFv5BezBfjXaMHaSZ1hqxSkzNosvjKbb2" +
                "LfbpID9ZB/CiCmPWysgVWRnVK0HNrq679tYur7sc24V+D66/q9" +
                "T7eH3ebX2f+uf9c7Pmm2fWIlaJqVl0eTzF1j5WojnmZaW+Bjlm" +
                "L3FFVkZVncxcz6d6H6/38V2fT9317rpZ880zaxGrxNQsujyeYm" +
                "sfK9Ec87JSX4Mcs5e4Iiujqk5m1nH3PVsdDvVao/fWZ+2z6KcY" +
                "LPs+awg4RlTFtwpf4xFQm9PLGY1OqZzaiVIs19seTyAd/wfT8Z" +
                "b+Lo+XrVhewaZa2y/Xyy2T34YmrVhewaZa63tB3adNRnetu5aO" +
                "7HPM+z5rCMgDNc+XELkzarG8x1E2fHIa8irLO1Hem/mcdbNxPf" +
                "s3N/oc877PGgLyhpJVMzMG7tQuznscZcMnpyGvsrhPs5xWOw7f" +
                "uuvu65Wv1O82uMq/Wrnjm9fjOjx8UO9FdZ8ucJ8+rnuw1D59Uv" +
                "dg899V3uZ/j+DOp0f1zCmP/kn/xFbpEyvsgwrumUJepk4x8wpU" +
                "Y1SiGvmYx4HH1rC1rn5vWfO6+6zuwVL79Hndg4l7w0l/8sriEy" +
                "vsgwqr60+mkZepU8y8AsYxXN8FjXzM4/CfXHFs0ne9291ts+ab" +
                "Z9YiVompWXR5PMXWPlaiOeZlpb4GOWYvcUVWRlWdzLzS///0xe" +
                "V6f1ph/AOFrjSF");
            
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
            final int compressedBytes = 1211;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW7FuHFUUnQqqNBQgEFIqfiBN0qTAM9PxEUnKdPmASFhKAC" +
                "f+BCQ3QXKFBA0FBUZCQYAEQojI2yGBFNHShQrvPh2fc+6bGc9i" +
                "r43JfU++fveee889evt2dmYTd8+7503TrexyLFfwYBFBJqejrI" +
                "p8zu11GDWmfcE5lENMu4/1qrsqq+uk/+iTxsbDH5pzHA+/by7J" +
                "6K/112jLqnjF14jjtJ5PVlbrWuuiEuf0HK9zXPXVypzJ+46xRX" +
                "/7M9+3nbexerQ/b6e3P//3r9LOm+tWbH96Qefpen+dtqyKV3yN" +
                "OE7r+WRlta61LipxTs/xOsdVX63MmbzvGFv0x8/T7Ff45ThPN/" +
                "ubtGVVvOJrxHFazycrq3WtdVGJc3qO1zmu+mplzuR9x9hq9vAK" +
                "v9HkGD7J3x79fHf088XKu7r985H9ambtwfHqmUR/qfK+ObXKLy" +
                "fRH2dxfH28+in3aVP7FLiu5jssz1Oep//Wefrwj//XeXrwe56n" +
                "zY4P3ss9GLwfv9/fpy2r4hVfI47Tej5ZWa1rrYtKnNNzvM5x1V" +
                "crcybvO8ZWs+f7bs7o7nZ3YeHDg0UEmZyOsiryObfXqRLHtK8q" +
                "jTnEtPtYr7qrsrpO7Zz3T3mfubn7zP6vy3j1eP+t8+r07m/+W+" +
                "MeixnDPOsgBZ3GY47qGqossRrRyJJvSq9i3ZPuCSx8eLCIIJPT" +
                "UVZFPuf2OlXimPZVpTGHmHYf61V3VVbXqZ3DfeaveQ+Qzy25T7" +
                "lP+T3dZbp/6u50d2Dhw4NFBJmcjrIq8jm316kSx7SvKo05xLT7" +
                "WK+6q7K6TvrtvfZe0xS7HMsVPFhEkMnpKKsin3N7HYYzey5w8t" +
                "UqHIvKXAU468qo5NjfbXePViu7iu2WWdYeQSano6yKfM7tdcf7" +
                "tDs8VQf5ahXo7Jl1r7qrskYlzMnrU17Hz26f+kW/gIUPDxYRZH" +
                "I6yqrI59xep0oc076qNOYQ0+5jvequyuo6tXOep3zfnbRP6/y7" +
                "1Mu7Tw/+zueWfG65mOtTe7u9DQsfHiwiyOR0lFWRz7m9TpUMTd" +
                "VBvlqFY1GZqwBnXRmVMCfPU77v8vPu/D/v8n036/unK92V2i8x" +
                "WvUjSgYyRVbnL4x1DiLEx3NUV4xOqxzfibHYUG27N/HZuHfCZ+" +
                "feZu5cTuZFxnwF62ptX7Qvar/EaNVHBDEykCmyOj/qh3uTv85Q" +
                "ZuhSNa5B7bgizx+KraoX7cKQlV9itOpHFAz8zfVA5wU61DmRgb" +
                "l1juqK0SmVE/u0GIutqg/bQ0NWfonRqh9RMPA31wOdD9GhzokM" +
                "zK1zVFeMTqmc2KfDsdhQbTvx12TtCX9p1u5v6Pq0PzdjvoLTan" +
                "38Sj7xztqnV3MP8j7zNM93Pnb+zJMzZ+T/H8/vM8/0Ov5a7sGs" +
                "fXo992DwKflWdwsWPjxYRJDJ6SirIp9ze50qcUz7qtKYQ0y7j/" +
                "Wquyqr66Tf3+hvNA1sWRWv+BpxnNbzUcOVr7VOX7GIKTM56xzF" +
                "qHGIL2qLrMpWs+f3vnmfeXb7tPV06yksfHiwiCCT01FWRT7n9j" +
                "pV4pj2VaUxh5h2H+tVd1VW16md1zlPj9+5mPO0dbDuedo6mHOe" +
                "Pvp4toR/AChPjIk=");
            
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
            final int compressedBytes = 1378;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWrtuHVUUvR+QHyBC4iOSJmU8mo4CfiFWZMlF7DZRiliKiO" +
                "7lN1AiuYgEDQUFIIgQDQgh3cINBf6ClKRhfLb3rLX2PjOaG0xC" +
                "yJmju+9+rLXO8tHY95GsVnqdfLRqV/U6+Wl4/Dw8vrZzOvltiN" +
                "8u5H43Zlvq/p5wP/5jl9/MTn9ZpPH9mP3azunfOie9Pvu4/YbV" +
                "rv5B/wDRMqus5o7OERUPVbA5Z150opqKUZ7O2V92pkq675RarP" +
                "eu7V1brTxaZpXV3NE5ouKdg0xz5vE5xRkrQzNjeAaPNb3oLaqy" +
                "WlZ/n/8+Pf5zVx+3z/WZ+9qLiLrOLhObzs8jhn3VmNbLE+5c6M" +
                "35rer+oc/c115E1HV2mdh0fh4x7KvGtF6ecOdCb86vznb5vXvy" +
                "9P/1e/fki6X7753vnXv02iuP3nEklk7BinqqrTx2ojPel51GDG" +
                "a8+9ReeVdWVZ+8c/vc8lr3dzun9rnlCj+33P7h7bGv8lrmZBe/" +
                "7X5q59TOqZ1TO6d3+3Pw+3tOj/9avn+33+179Norj95xJJZOwY" +
                "p6qq08dlJb7AN62YXOojN14ZqZGZ0A096PL7qf7nX3PHrtlUfv" +
                "OBJLp2BFPdVWHjupLfYBvexCZ9GZunDNzIxOgGmvd+3veHu9e+" +
                "Ovd3e6Ox699sqjdxyJpVOwop5qK4+d1Bb7gF52obPoTF24ZmZG" +
                "J2P9qnsl51Zq6yFy7R3vQQFKUVX1nV/fG/oZwcrui92oB47Tjh" +
                "Rf6xX2ttvKpNTWQ+Q6Tl0Bz8grO299h4yJCsBmDPuK3TmXM+e0" +
                "neoV9ll3JpNSWw+R6zh1BTwjr+x85jtkTFQANmPYV+zOuZw5p7" +
                "OpXo27Wbf3lEuudk7Lrs8/aWew6Jw+bWew5Gr//2niPeqXWq8/" +
                "HO+s/YUKX73+7usPdvb7/O2cU3+jv4FomVVWc0fniIqHKticMy" +
                "86UU3FKE/n7C87UyXdd0ot1bf6W0N2GS2zymru6BxR8c5Bpjnz" +
                "5JzCjJWhmTE8g8eaXvQWVVktq7fvVdr3Be2c2vdP7X56l++n/m" +
                "H/ENEyq6zmjs4RFQ9VsDlnXnSimopRns7ZX3amSrrvlFpWb/fT" +
                "wjvqpVYXtfUQuY5T8FlJVVVf1eLejONOxkDj0fXp/XTXHd+Fv8" +
                "x7zv98/+3r0fU38Amvvd4tuLq73V2PXnvl0TuOxNIpWFFPtZXH" +
                "TmqLfUAvu9BZdKYuXDMzo5OxPu6Oh6zE0ju2Zbl2HImlU7Cinm" +
                "orbzyn4/piH9DLLnQWnakL18zM6GSsD7vDISux9A5tWa4dR2Lp" +
                "FKyop9rKG8/psL7YB/SyC51FZ+rCNTMzOhnrTbcZshJLb2PLcu" +
                "04EkunYEU91VbeeE6b+mIf0MsufGdF5r3yrqwanYz1UXc0ZCWW" +
                "3pEty7XjSCydghX1VFt5o+Oj+mIf0MsudBadqQvXzMzoZKwPuo" +
                "MhK7H0DmxZrh1HYukUrKin2sobz+mgvtgH9LILnUVn6sI1MzM6" +
                "Get1tx6yEktvbcty7TgSS6dgRT3VVt54Tuv6Yh/Qyy58Z0Xmvf" +
                "KurBqdZMblt+Lt3++W/bvUpp3BzOeYZ/0zz+yREf4Agjlzyktw" +
                "qll3oB6zE/XIz3UdVBxdW3GX6NP+1DN7ZIQ/gGDO5E9/qtpzyB" +
                "jnPWYn6pGf6zqoOLq24i7RL/oXFxGPjPAHEI6r4Vl5CU416w5Y" +
                "x3UjCx75ua7DP7nq+CLczf4momVWWc0dnSMqHqpgc8488R9mrK" +
                "xOFcMzeKzpRW9RldWyevu+oH2vcnXn1N/v7yNaZpXV3NE5ouKh" +
                "CjbnzItOVFMxytM5+8vOVEn3nVJL6n8DOGrBJw==");
            
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
            final int rows = 44;
            final int cols = 74;
            final int compressedBytes = 1129;
            final int uncompressedBytes = 13025;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV8tOHDEQ3D/hK/IFZk78BwLBiccCxz1H8B+I1y055JBEio" +
                "giJUFRpHxPZm3K1dXtHU0UDhw8Lfe4u6tqaq1Z0G7vbO8sFts5" +
                "r6/1DhUyOkAydEqW11Nt5eGKM/tcaLYwnNmnb3pWfKpVVZ/2ya" +
                "uv4/o2rvfr3mpr9WvMHxezrtWnuvtjur8D7sviP6/Vh8npz1ka" +
                "n+vu6V+fn47SETJqVMjoAMnQKVleT7WVZ520wvqgXnShM+9MXU" +
                "AzMr2TWu+m3XGXc+7tlih77QDJ0ClZXk+1lVfPabcd1gf1ogud" +
                "eWfqApqR6Z3U+iydjbucc++sRNlrB0iGTsnyeqqtvHpOZ+2wPq" +
                "gXXejMO1MX0IxM76TWV+lq3OWce1clyl47QDJ0SpbXU23l1XO6" +
                "aof1Qb3oAk9WZHxWfKpV9U6I6X/HZ/0dP0knyKhRIaMDJEOnZH" +
                "k91VaeddIK64N60YXOvDN1Ac3I9E5qfZkux13OuXdZouy1AyRD" +
                "p2R5PdVWXj2ny3ZYH9SLLvBkRcZnxadaVe+EmP69m38N18M1dm" +
                "VFBBYRljOlPAenmm0H6jE6UY/23tZhZTO0Feevt+8W/Zrzbm71" +
                "M5h45x+Hx3XmiggsIoBr4a3yHJxqth1YHeh6Fj3ae1vHfnLVQR" +
                "jcm+ENc9mVqtS2o3NmxVOVbLu3PPHvZlZZnSrGzuixpee9eVWr" +
                "FtX7/7uZDvo59XN6ud8tx+kYGTUqZHSAZOiULK+n2sqzTlphfV" +
                "AvutCZd6YuoBmZ3gkx/X3q37t+Tv2cXvPvYPP77nv/dTLrnH70" +
                "M+i/g1/wfXrqZ9D/jvf/d/2cXsnvu4N0gIwaFTI6QDJ0SpbXU2" +
                "3lWSetsD6oF13ozDtTF9CMTO+EmP4+zXqf9tIeMmpUyOgAydAp" +
                "WV5PtZVnnbTC+qBedKEz70xdQDMyvZNan6fzcZdz7p2XKHvtAM" +
                "nQKVleT7WVV8/pvB3WB/WiC515Z+oCmpHpndT6MB2Ou5xz77BE" +
                "2WsHSIZOyfJ6qq28ek6H7bA+qBdd6Mw7UxfQjEzvpNan6XTc5Z" +
                "x7pyXKXjtAMnRKltdTbeXVczpth/VBvehCZ96ZuoBmZHontd5P" +
                "++Mu59zbL1H22gGSoVOyvJ5qK6+e0347rA/qRRc6887UBTQj0z" +
                "shZn0Nd8MddmX5C/2CLAjL2XSt8XNwqtl2oB6jE/Vo720dVjZD" +
                "W3HP6JvhBruyIgKLCMvZ+OlvVHsK6fO0x+hEPdp7W4eVzdBWXL" +
                "pIF8ioUSGjAyRDp2R5PdVWnnXSCuuDetGFzrwzdQHNyPROar1M" +
                "y3GXc+4tS5S9doBk6JQsr6fayqvntGyH9UG96EJn3pm6gGZkei" +
                "fE5DftYXjArqzw1j9gEWE5G79ND6o9hfQ5Klkv0Yl6tPe2Diub" +
                "oa24Z/TtcItdWRGBRYTlbPz0t6o9hfR52mN0oh7tva3DymZoK+" +
                "4ZfT/cY1dWRGARYTkbP/29ak8hfZ72GJ2oR3tv67CyGdqKW/wF" +
                "f+mojA==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 18, 0, 3, 0, 19, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 4, 0, 0, 21, 5, 0, 22, 23, 0, 24, 0, 0, 25, 0, 1, 0, 26, 0, 6, 27, 2, 0, 28, 0, 0, 0, 29, 30, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 0, 31, 10, 32, 0, 0, 0, 0, 0, 0, 0, 0, 33, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 34, 0, 0, 0, 0, 17, 3, 3, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 36, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 37, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 38, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 39, 4, 0, 40, 0, 0, 0, 0, 41, 0, 0, 42, 43, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 8, 0, 0, 44, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 45, 10, 0, 0, 0, 0, 0, 20, 21, 0, 22, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 24, 25, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 29, 0, 0, 0, 4, 0, 0, 30, 0, 1, 31, 2, 0, 0, 0, 5, 4, 0, 0, 34, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 3, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 37, 0, 1, 38, 0, 0, 0, 6, 39, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 9, 41, 42, 0, 0, 43, 0, 5, 6, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 1, 0, 0, 0, 0, 0, 0, 0, 46, 2, 0, 0, 3, 0, 7, 47, 0, 0, 0, 1, 7, 0, 8, 0, 48, 0, 8, 49, 0, 0, 0, 0, 50, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 51, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 0, 47, 52, 53, 0, 54, 0, 55, 56, 57, 0, 58, 59, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 60, 61, 10, 0, 0, 0, 0, 11, 0, 0, 62, 0, 0, 0, 63, 12, 13, 0, 0, 0, 64, 65, 0, 0, 0, 4, 0, 66, 0, 48, 0, 0, 5, 67, 1, 0, 0, 0, 14, 68, 0, 0, 0, 15, 0, 1, 0, 49, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 0, 0, 12, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 69, 70, 12, 0, 51, 71, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 72, 73, 0, 74, 75, 76, 77, 0, 1, 0, 2, 0, 0, 1, 15, 16, 17, 18, 19, 20, 21, 78, 22, 52, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 36, 37, 0, 38, 39, 79, 40, 41, 42, 43, 80, 44, 45, 46, 47, 48, 49, 0, 0, 0, 81, 0, 0, 0, 5, 0, 0, 0, 0, 0, 50, 0, 82, 83, 9, 0, 0, 2, 0, 84, 0, 0, 85, 1, 86, 0, 3, 0, 0, 0, 0, 0, 87, 0, 0, 0, 0, 0, 0, 88, 0, 89, 0, 0, 0, 0, 0, 0, 0, 2, 0, 90, 91, 0, 3, 0, 4, 0, 0, 92, 1, 93, 0, 0, 0, 94, 95, 96, 0, 51, 97, 98, 99, 100, 0, 101, 53, 102, 1, 103, 0, 54, 104, 105, 106, 55, 52, 2, 53, 0, 0, 107, 108, 0, 0, 0, 0, 109, 0, 110, 0, 111, 112, 0, 0, 10, 0, 1, 0, 0, 0, 113, 4, 0, 1, 114, 115, 0, 0, 3, 1, 0, 2, 0, 0, 4, 116, 0, 6, 117, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 118, 119, 120, 0, 121, 0, 54, 3, 56, 0, 122, 7, 0, 0, 123, 124, 0, 0, 0, 0, 0, 5, 0, 1, 0, 2, 0, 0, 125, 0, 55, 126, 127, 128, 129, 57, 130, 0, 131, 132, 133, 134, 135, 136, 137, 138, 56, 0, 139, 140, 141, 142, 0, 0, 5, 0, 0, 0, 0, 0, 57, 0, 0, 143, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 6, 144, 0, 145, 58, 0, 59, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 146, 147, 0, 0, 148, 149, 7, 0, 0, 0, 150, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 151, 1, 0, 152, 153, 0, 7, 4, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 11, 0, 0, 12, 0, 13, 154, 9, 0, 155, 156, 0, 14, 0, 0, 0, 15, 0, 157, 0, 0, 0, 0, 62, 0, 2, 0, 0, 0, 8, 0, 0, 6, 0, 0, 0, 0, 158, 159, 2, 0, 1, 0, 1, 0, 3, 160, 161, 0, 0, 0, 0, 0, 7, 0, 0, 0, 58, 0, 0, 0, 0, 0, 59, 0, 0, 162, 0, 0, 0, 9, 0, 0, 0, 163, 164, 165, 0, 10, 0, 166, 0, 11, 16, 0, 0, 2, 0, 167, 0, 2, 4, 168, 0, 0, 17, 169, 0, 0, 0, 18, 12, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 170, 2, 0, 3, 0, 0, 0, 13, 0, 171, 0, 0, 0, 0, 0, 172, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 173, 0, 174, 19, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 1, 7, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 175, 2, 0, 176, 177, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 178, 0, 179, 180, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 181, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 182, 22, 18, 0, 0, 0, 0, 0, 0, 183, 0, 0, 1, 0, 0, 19, 184, 0, 3, 0, 7, 10, 0, 1, 0, 0, 0, 1, 0, 185, 23, 0, 0, 0, 0, 24, 0, 0, 20, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 16, 0, 0, 0, 0, 0, 186, 0, 0, 187, 0, 0, 0, 188, 25, 0, 64, 0, 0, 189, 0, 0, 190, 191, 0, 192, 21, 0, 0, 193, 0, 0, 22, 0, 0, 0, 60, 0, 26, 0, 194, 0, 0, 0, 0, 0, 0, 0, 0, 0, 195, 23, 0, 0, 0, 0, 0, 12, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 196, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 24, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 197, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 0, 198, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 0, 199, 200, 1, 0, 1, 26, 0, 0, 0, 0, 0, 0, 0, 201, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 4, 0, 0, 1, 202, 203, 13, 0, 0, 0, 0, 0, 0, 0, 0, 204, 67, 0, 0, 205, 0, 0, 206, 207, 0, 0, 0, 0, 0, 0, 0, 0, 208, 0, 0, 0, 209, 68, 0, 210, 0, 0, 3, 0, 0, 0, 69, 0, 0, 62, 0, 0, 27, 28, 0, 0, 3, 0, 0, 29, 0, 0, 211, 0, 212, 0, 0, 64, 213, 0, 28, 214, 0, 215, 216, 0, 0, 29, 30, 0, 217, 218, 0, 31, 219, 0, 0, 220, 221, 222, 223, 30, 224, 32, 225, 226, 227, 33, 228, 0, 229, 230, 6, 231, 232, 31, 0, 233, 234, 0, 0, 0, 0, 0, 70, 0, 0, 0, 2, 235, 236, 0, 237, 34, 0, 0, 0, 238, 0, 239, 35, 0, 0, 36, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 240, 0, 241, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 39, 0, 0, 0, 0, 36, 0, 0, 0, 242, 0, 0, 0, 243, 244, 0, 0, 0, 0, 245, 0, 0, 246, 1, 0, 0, 0, 5, 2, 0, 0, 0, 0, 0, 0, 37, 247, 0, 40, 0, 248, 0, 38, 249, 250, 39, 251, 0, 252, 0, 0, 0, 0, 0, 0, 253, 40, 254, 41, 0, 255, 0, 256, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 257, 258, 0, 0, 259, 0, 7, 0, 0, 0, 0, 42, 0, 260, 261, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 262, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 263, 264, 265, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 0, 0, 8, 0, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 267, 0, 0, 0, 0, 2, 0, 268, 3, 0, 0, 269, 44, 0, 5, 0, 0, 0, 0, 0, 0, 0, 270, 0, 0, 0, 10, 0, 0, 1, 0, 0, 2, 0, 271, 44, 0, 0, 0, 272, 0, 0, 0, 11, 0, 0, 12, 13, 0, 45, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 273, 0, 0, 274, 0, 0, 0, 0, 0, 275, 0, 0, 0, 45, 0, 0, 0, 46, 0, 276, 0, 0, 0, 47, 0, 0, 0, 0, 277, 278, 279, 0, 0, 48, 280, 0, 281, 49, 50, 0, 0, 8, 282, 0, 2, 283, 284, 0, 0, 0, 0, 8, 51, 285, 286, 52, 287, 0, 0, 53, 0, 4, 288, 289, 0, 290, 0, 0, 0, 0, 0, 0, 291, 292, 0, 54, 0, 0, 55, 0, 0, 56, 0, 24, 0, 0, 25, 5, 293, 6, 294, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 295, 3, 296, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 297, 0, 298, 0, 0, 0, 0, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 299, 0, 0, 0, 0, 0, 0, 300, 0, 0, 0, 7, 301, 0, 0, 0, 58, 0, 302, 0, 0, 303, 0, 0, 304, 305, 0, 46, 306, 0, 0, 0, 59, 65, 0, 0, 0, 307, 308, 60, 0, 61, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 309, 0, 8, 310, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 66, 0, 0, 0, 2, 47, 0, 0, 311, 312, 313, 63, 0, 0, 0, 3, 0, 0, 314, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 9, 315, 0, 51, 316, 52, 73, 0, 317, 53, 64, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 318, 0, 66, 0, 0, 319, 67, 68, 0, 54, 0, 320, 69, 321, 0, 70, 55, 322, 323, 71, 72, 0, 56, 0, 324, 325, 0, 73, 57, 326, 0, 58, 0, 74, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 327, 59, 328, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 329, 6, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 330, 0, 0, 0, 0, 0, 0, 0, 0, 331, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 61, 332, 333, 0, 0, 62, 334, 0, 63, 335, 0, 64, 336, 65, 0, 0, 75, 0, 0, 337, 338, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 339, 0, 67, 0, 0, 0, 0, 340, 341, 67, 0, 0, 0, 77, 0, 4, 5, 0, 6, 0, 0, 0, 3, 0, 0, 0, 342, 0, 343, 344, 0, 0, 0, 78, 0, 0, 79, 345, 0, 0, 0, 0, 68, 0, 80, 0, 346, 0, 81, 69, 347, 0, 348, 349, 350, 82, 83, 0, 351, 84, 70, 352, 0, 353, 354, 355, 85, 0, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 357, 1, 0, 4, 0, 5, 0, 0, 6, 0, 358, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 359, 88, 76, 77, 360, 0, 361, 362, 0, 0, 363, 364, 0, 0, 0, 7, 0, 0, 78, 0, 79, 365, 68, 89, 0, 0, 0, 0, 0, 0, 7, 0, 366, 0, 0, 0, 367, 0, 368, 0, 0, 369, 0, 90, 0, 370, 371, 0, 91, 372, 373, 374, 375, 92, 93, 0, 0, 0, 376, 0, 0, 377, 378, 379, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 380, 381, 0, 382, 0, 383, 384, 0, 0, 0, 0, 97, 98, 0, 0, 0, 385, 0, 0, 69, 70, 7, 0, 0, 0, 0, 0, 99, 100, 101, 386, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 106, 0, 82, 107, 0, 83, 387, 388, 0, 0, 84, 0, 8, 0, 0, 389, 0, 0, 108, 0, 0, 85, 0, 390, 0, 0, 86, 0, 391, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 392, 0, 0, 0, 0, 393, 0, 394, 0, 87, 0, 395, 0, 88, 109, 110, 89, 0, 0, 111, 0, 396, 0, 112, 397, 398, 0, 113, 399, 0, 0, 0, 0, 0, 400, 0, 0, 0, 0, 34, 114, 115, 0, 116, 401, 0, 402, 0, 0, 0, 117, 403, 0, 118, 119, 404, 0, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 1, 2, 0, 0, 0, 3, 4, 0, 5, 6, 1, 1, 5, 6, 7, 8, 1, 2, 0, 1, 0, 0, 9, 1, 5, 0, 5, 0, 1, 7, 2, 10, 0, 11, 0, 12, 1, 0, 0, 6, 13, 0, 14, 15, 11, 3, 11, 0, 13, 3, 1, 16, 17, 9, 2, 11, 18, 16, 19, 4, 0, 19, 20, 21, 1, 1, 22, 23, 2, 24, 25, 1, 26, 1, 7, 7, 27, 13, 0, 28, 2, 13, 29, 30, 1, 4, 0, 0, 31, 32, 7, 1, 33, 34, 3, 2, 0, 1, 2, 10, 35, 36, 16, 37, 38, 0, 39, 40, 7, 41, 1, 42, 0, 1, 43, 44, 8, 6, 45, 5, 46, 47, 48, 4, 9, 1, 6, 49, 50, 51, 38, 6, 9, 0, 52, 0, 53, 54, 13, 5, 55, 56, 0, 57, 1, 18, 0, 58, 59, 60, 9, 61, 23, 62, 2, 63, 3, 64, 5, 65, 66, 67, 1, 68, 0, 19, 69, 70, 71, 72, 73, 0, 3, 74, 18, 0, 0, 75, 0, 76, 77, 7, 10, 1, 2, 78, 4, 0, 79, 0, 80, 1, 81, 1, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 13, 0, 11, 90, 14, 2, 91, 92, 93, 94, 19, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 26, 6, 9, 2, 24, 27, 101, 0, 4, 102, 2, 1, 2, 103, 0, 9, 104, 105, 1, 106, 107, 108, 109, 110, 111, 10, 0, 112, 22, 16, 0, 0, 8, 1, 1, 113, 31, 2, 22, 13, 4, 7, 114, 6, 2, 11, 115, 27, 116, 117, 0, 0, 18, 29, 1, 118, 7, 1, 0, 3, 20, 0, 2, 119, 3, 14, 1, 0, 120, 121, 49, 19, 8, 4, 22, 122, 1, 4, 123, 124, 16, 125, 12, 126, 0, 6, 127, 128, 129, 130, 131, 132, 29, 31, 133, 134, 7, 9, 135, 32, 14, 10, 136, 137, 12, 0, 6, 12, 138, 139, 140, 10, 141, 2, 142, 143, 144, 35, 23, 145, 146, 147, 26, 148, 2, 7, 4, 149, 150, 0, 38, 151, 152, 0, 153, 0, 154, 39, 27, 40, 155, 156, 4, 157, 49, 41, 8, 158, 159, 9, 42, 160, 161, 162, 0, 163, 164, 14, 0, 165, 166, 47, 6, 1, 34, 167, 168, 169, 16, 170, 171, 12, 1, 172, 173, 174, 34, 5, 0, 26, 0, 0, 9, 175, 2, 29, 35, 14, 3, 1, 41, 1, 176, 15, 177, 178, 8, 8, 0, 179, 180, 181, 1, 182, 183, 24, 184, 29, 185, 42, 2, 0, 186, 187, 188, 22, 0, 19, 0, 3, 2, 189, 9, 32, 13, 190, 191, 2, 192, 193, 51, 194, 18, 195, 196, 197, 2, 0, 198, 199, 7, 200, 52, 3, 20, 201, 19, 14, 202, 203, 3, 204, 0, 15, 205, 53, 206, 207, 208, 209, 6, 210, 211, 212, 213, 214, 3 };

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
            final int compressedBytes = 1360;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXEuO2zgQLTIcg+lsmMALZ8ckxmDWc4LKJujlLGdpoHMQ9i" +
                "boHCG77pskN5mjDG3Jij6kvpRMUfWAuO3ItshiveKrUskP7BGe" +
                "DnB+3HHz/h/cgf6Mx+PbJ/gO7MN/Qj4DF/uDfPz29fiCf+M94F" +
                "/wib3599f+xwkIBEITyv6TvoP8+gRvP9BPYEDakVr+C24OCiz/" +
                "Gf6p7aEngJMRr5/5O7D8NzsUGj7CvcYn++fN4Re8J/5PxkN7/P" +
                "1J8ZfQh78vlr93BX+PGpilKWhj/YdzeeHvz4y/cE/8JRCSg8x1" +
                "Rw591R8CmH11fYs4q47LK1SY0OxFp3kUuQiBQCAQ6htnKW9v/r" +
                "fKsvZ8i/mc7Z8nCLF/PvxB9TdCBGAXN5clr7880zkjlK6ThEdR" +
                "vyrXT16tlT88fxAijzGsFpY8+jYC+xf1Q1WuHx7P3vK7/nCpH3" +
                "7J64cQYf2wbFzetzyrKWikhMpqt9bvQ/LPXX+/8ud04Q/V3wm3" +
                "KJ4gGaY3f29Vfy/HjzvX/nt/3X+L8+Mc52/MHxrzx3ivPwTxf+" +
                "nJH6v1V0i0/rr+BMhfP2+4Rbaa4dZPxGCA7Vw/0IuFEHeVac02" +
                "oqsosxDPYV1f/RFD1h8Til+j0FF/7dl/E0H9wFe/czhWpX4nmz" +
                "PIn99VRihnThC6xq89448IamjAv+34g/hfR/11of618fxD9gGM" +
                "OnuftJ9jeP4MChBvDbwGxvCyivbrJTPH58dM8tjkjtkv3xtsHx" +
                "KDUq9n01PC8f96EiFs+uXxv0jrx1OBdika6wfV9dP5+r041q+9" +
                "/rbG+jXVb9YMUzyAN3qU3hDQfwZpONUaVfl0gSgi4E8ylY1xm7" +
                "7sI2XExReEfYcuzsB1yXuzTfLVuYgg9yXHNbIUn5z2PxRuxGXx" +
                "ddJPivl5GSoHxpX4qZlhXmzS4SF2OQuCfAq1pEJO9CHcVvzoVp" +
                "/CyQ1+XU50hBET7PORCNHuo7jMuaLZa5ZzQO2LXKolsLGwHAga" +
                "SUPVILI1MYGSfg5rA28hULj82xf/h/BUD9HZBghjkxTRaT/dtX" +
                "qL8KC3pkCXt2HbwG8pVyJ0XQZ7Z4ojm5ZkpTRnZv0iV8k5Fs9Q" +
                "pC7Zkrc5oWm+kPUF5ZAwFg4IuA7/+e0FckAQkwn4w5D5ivFeRD" +
                "ImCTWdhs/fBp39m8X1p13L9Se6/nNb/9/I+dOGv3+3QatMWSbW" +
                "/5m+/40ef7pCpX//wy7i/oeB0BTsCGuKf2bj8++Hhfsvw0QR6V" +
                "UAy/bfrrxU0Wm/2BOksf3bt/N/wryoNeUFEi2GDJvS/ufsXwd3" +
                "/7ru1b/O+85yjSoaI3KZDWQhdLPglOi/2P0jk+JP//svKP5E6W" +
                "a1/u1T0b99uZdofP/2giG20n9hSgca/RdiHh3UP9CR/kpshycQ" +
                "CNvOCnv0/yHkFRkVYh+gXYRASBdsW3QX1WK1LuJnVdBVVHaWf4" +
                "S5/xTKJyRJOjp/5B32V0a12T9a/i0LE4v9VZiUdw1J37YrleX4" +
                "CS7/YZDFT4B18DeMrB9pP8f+lf2g+765f82HmDzaRL7QbfmbqN" +
                "2/hctNSfX3v8bV/tKvYLkaAYSmoDiD/zK3dNCR80G0dIuge+Q6" +
                "2JnD6fdFJIUhOhAIdRZ7FFSdLbJ4oP0n6cXvhqbFjlntq4WWRJ" +
                "X0t4ni95/MFAoopwRvjp/0c7Trv7HgHaf+JsxS6Rhe/4DBv9+n" +
                "en5exMSCVW3rJq81XO2sJ31+mbyZQCAQCKS/CAO0CNa31ZnUih" +
                "qcvVEORkgQchhb/fGb94rfWFXO2f1P6EnVeCqGI4zMvzHzOk/s" +
                "lWZhD/kf0Kwhug==");
            
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
                "eNrtXU122yAQBkrz6M+C1+dFuqM/B+Fl1WPkKLSrLHuEXKU3q2" +
                "THlmSDjATDgDTfJi+J8QzzxzcIbMayQEe/0jEYiLl/mjtKyKCq" +
                "esUUNwCe9+0crLbyvs6nfxoOoQ/vFVC3fz/9yU5fagIhCYq5+W" +
                "oxxL+bhLk4m81OJlTEpQlQJYXJo8VkJ3RwrDCjkuRsb6l3sjOl" +
                "Ooys5tQ0MjYIg1UPPPIFgnwCIY6fENLqvZzjBQQCIYlHqVKZpc" +
                "M8zlNAzwTKrFfnzN9+hfkbA+ZvqPXLw18PsfN/iuO/EfZbMdGM" +
                "/dOBvVc+YihOqpkrWe6s/7X9HE78EAjr81/P5K8CzF/i0NSDZF" +
                "s/Gs6/kvtHgvIvG9EyefhD69C3vH0erpz/StSHH92EVOfK3+xF" +
                "Cveo2QMz3P7sPf7SBYmT6lUIdnhU7sFKw753jYZ96X58evzHvj" +
                "63779p/TL56rdhhMj6u+f6U/H6JaeFCSigVbACfrwrEqBAUtom" +
                "1+Irz5b1X/WA4A92HLYRRYLHOZCwVfK/stTR/gX1n4TtgecuLR" +
                "uN/53m7+ZpWlyYq2HJdHAKLD+/KBLHVzD/uc4wppWQwXR1vjdV" +
                "jFCkxxEFm6CKpl52/knnL7ZxfkQu5Sp6GGl9z69s/4rj/F/7Wf" +
                "J+/vw8/76oupvnx5Uc1jMliNwd+QJRPkZTnbp+0njc8an8AYY/" +
                "xfMf8j+Nx+Tvrcvfu/0Txmc5/7Tv/MPv32l8jfsftbLd1RB1dg" +
                "Sandq9i4In737xPA/lzJuoBIxdFtz4N+nr31Ox+doFWwX5YcHz" +
                "d2x/5rM/f7M/8/IP3Mq7JH5W6h/2/zL52mHYzzICdBSO84eN/X" +
                "+y/nHhm8Qfo/tfbfkPsv7N1N8i8gl1A45/yCzrf1z9Y2j1D5e/" +
                "4ctP9v8K/qQXaVF3/FS4fzD0T3/j7++7QP/k+fwyMcSu7+mJNM" +
                "WnD7d/mnz+JcN4tZC9G/j4Szz/g3X+SGWavxrkq2v5JU4ylrJf" +
                "+v45dvxk3n92BetXzeuPCDklpn6VXD+2sP6j2i/V/63zH0SYGG" +
                "NWVi/0pjIP/PNH6upTXJ7889jPRNtPVbV/Zaa/wKrFAd/bIstH" +
                "MM9V/oqb+PsTyt/P8PGnF8wCdf3NlkbE6ghp/E/PxL8qMJ6tGZ" +
                "+EDP13EfoN1z8n738Ax59do4ZuiCfvuP/aASB4jr7mNxZHj8g3" +
                "t/X1YRdL4n//Cvb9n+bHp65f2Pev8ey3ged3FTw/qfL+XRPxV3" +
                "H9aGL/hZBr6yRMswIdVhWuCtbvt/v/H14v9Xty//9vX791uNXO" +
                "3FI61JxyEZ7OycsR5ROKdXSmKW1z7z+U2P+wIOkIU45MOfmyLV" +
                "/e9P57/1o4+lo83BpI9seOfteEpnql/ibX6pW2FIXli/Fs0j4/" +
                "LFP/UV//1JvpuIFw0d+7/7W/80t12Z9AIBD2y6ZyfP7nFKqo/m" +
                "nrJzb/cD6bfbs74KjUs5+cze7FAlx8svGcA+TalfWJLCifQADJ" +
                "oUDziAvnbxaXDaoITeg/L5/H3z/m1/N2NrT+8Nn1xwbXn5WreL" +
                "71/5D/88daj59t508o/pUv/s0SvQkz/oSi+fTtJCvJQQ2g80eV" +
                "9K8s1/6xrrJ1ujm/DkevkLnevP2T/f8fA/boOQ==");
            
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
            final int compressedBytes = 866;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnVlu2zAQhkcsWzBAHvjghxboA1v0IEROkqNMc4IewUetZc" +
                "XypoWSKK7/ByRxkNAkZ4aziJQsiTSRJEWG6PNLtD9OiPYb2/b7" +
                "F0kNqQMxXWBFloBvGojgghp82SEhHsgf5uBG+X56hf5N3/Im/t" +
                "Hi+Hfbnle0z13/W+WXx/w5wzWhQ628JPTvc7ZcmP3vL5Hb+V99" +
                "q7iuvnNae5r/XXpRT/2wzX+gzlqXv5obWxPZ+/MIaX9m8UOGXZ" +
                "ZPvcbpv+76o474m3TlBfkjfpdsv7y//a6/foFAU+GCtDXMX0L/" +
                "mC6ow0BvopnY+GYC8ozqtIXD/wAAgA+HY4fqN9tWdWdPdGyzn6" +
                "at35pL/dZuRvBE/aaCjt/f/vNde/3Uvpu/6Ob/r22vQ0xvVcHq" +
                "q++Y/QOwPDU1bo18WrO5/wXlYs72g/mHDjIP8fc96P6Hh/0XNV" +
                "qShajUnBwZl9t/9N4w/FH0+vVnxtef6defIgDADEhIAQDVoSEC" +
                "AEDdSRDyPwAAAAD4J+3z98h/kracQu9/Kmez2TrrT8zoTwfUX7" +
                "yLP5zrFKo9HxHbk+D+tbThOq17q/+Jff299v6L9JXQP3CKn0r0" +
                "nvt6UOicdarUMh05cdOEHfyTNMXrL9P8p3EYLKck+a5+477+Pr" +
                "jW328528/49YcI859e/4+WYxfavy1x/SNjSp2p5x8XVr/hlmwQ" +
                "OX6PXT//GItfr85+yULMOyNGXldVVRpad1tmc+fgDawp3TgJws" +
                "lfL/AU2H8AIDl05e0p7f5l6frfmv/Xbj+1jz8P+cV9fsN0MqUn" +
                "8qlmvQSd2wMAduRXvyKZBCtN38g09s+nXzInF3Q8vfz5nVi0e7" +
                "6/L8H4Rf+lH++QX3p5m5zNodb0byOPH4xKy9f59eXZDkP+YH9s" +
                "0et32/Nv554f+3Ls1/9d++fnxyawraNgPyBa/Msa5VTOl0vt8/" +
                "fgHu28zxQFTXfQf4h151f8nf9W+PwTsC3+7XB+2CRe+MnR8lTn" +
                "osUNHOirGlKM6N7aPKiPL/oHwIf9Ve8/O/E18z7SNL79Zxr3j+" +
                "RwFaAkOFU56UV6zKQAt5Hbh8bkOeyM5R/v/EfB18+c7v8amz8n" +
                "NX/eoj8eHP9HsM+v0ZTy59dE1l9s/7XD5xfq1PJPW7D/2zz+/3" +
                "u7mGY=");
            
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
            final int compressedBytes = 887;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnUty4yAQhhuGBVPlBQsfgLmJak4yR6HmBDlCjprIlmXHRh" +
                "aIN/zfwqVK0ubRTXcjtYggUkSCJGm6os98ueTzh5nmz1+CGMkz" +
                "GbphJP2lWfIuf/2lJlf5abk8k5Bk4SIvvr9Q//ixucm/gZFFZJ" +
                "vH8V+GcG//2ku2dPSBe//32P+70Pbd5KVFXrzq31t/IePP0T45" +
                "zH+AZND62UYdbd9r/izyKkL/PddfUf19uOvPeOgPhKKfY0E2e6" +
                "ps8G/cQoL1/yxvUvrf0vGndYQqGz+hv4Hsj48aibzjDSssD0BJ" +
                "urJf5pCW1Z42AgBcErS8SY6gyZY/T3NWfenJ59w5NufP7JY/z/" +
                "7IpM7f1Uv+fm2fX9v/mNtXkSdDZVAv6BzOUiX1ALSQbQPQbXoG" +
                "MH/Z6TpVkJvPT0X/mj36/MIY7/qJJPu3zuafv8zff/v8kzlVNH" +
                "95FgqveG3yQEHumNzqtlQGEsffdPVXpfMv42rGG/HHZKyfK6c/" +
                "v/o15/ot7/rRUnlkHfWTIFiLcstWJrJVOgtdx/qrY/9km78d76" +
                "sjt4/9OxjZf0WIX7rB+FVD/NXJ/BpI7X+P288p8P2dE+IKePYc" +
                "OsJ3FIBvXFtRUDXIS/b91xj141jJ4JCFmHWNRH3+x9zk2UP+f6" +
                "T9VR723/X+Gfl5mj0cHv3Vs/8Fw3s+PL+KhaxIf8v7I78/V/39" +
                "qH+yvj8i+7FfvW2/erVfx/GWt3N7DzRWXG/+t9H6u4yY1tXddI" +
                "opkk3L0foB3b/RtGtWGdzBZEld/uyawjSL/Wu4/7T0P1Q+ETg/" +
                "BewZoexnPKLlnmP/X838a8x/lfvvfseP9Q/7K2k/ooLzz1u2v9" +
                "Lrt4XzR1tbv6ay+Q/f/6Xu/4H8W8N3vydecUtK/1WH/+n7/lsB" +
                "/zmY/0BFDPLfevf/re9/0P9ItH7+vHv/mbcXiBs/SrcPQKFwyj" +
                "EL1eji0bFkdBiqkLdSRSY56P2X0P7j/DwQOckqe/637i1pBSBq" +
                "/NDr/QNZpH2K2f4IIM4CAECW+Fnd+bUA+vME948byb/6sN927S" +
                "dB/YmqT39mfxYc378zWP+uSOvlMPcfTOD5mSba+ZlTm/bjScjD" +
                "C6/zG0EadEGvagrGZRbSfqX5E/JvxP/RGSL/eTvsaN9hqh1t6P" +
                "9Pf7v/4tRI/T8suFX7a338YHC+ANzA7J0=");
            
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
            final int compressedBytes = 729;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtu4zAMpV1joO6EQRctMAvNoAcRepI5CucmPerESZNmkY" +
                "8cWyIpvhcgMIzIEil+HmXZIQLug52Pn2ECyzAs+/lk1n52I4/z" +
                "d6D0dSbR+HU47pvm+ftp2qkkvJxdiANlMfvbXy9scSUL8mf4o2" +
                "tonv8C/4nr/ce1/lbHn2xPf0Mr/qJD/w/wh2Qp/tTjH/AfWf6/" +
                "jf5h/3X5p+36Gfy3E4xQQbUoXDV+svL+dWPYMG0Nwu2Be44wVn" +
                "BWAGiH0NRh8iX+m2dWvPekz5n9DTP/HY78d45hfIP/BtezV0yy" +
                "GJZumL8BwIr1A266fnAhMk3bXnQsZLxJ3xRGOqS7kxCHSfpJh5" +
                "PXZImF8lMT+ZWV9sX3DyysP2vWX5v6fy3qzb/0+Nvo3zZVVOxn" +
                "3/bHJ/t7KbW/Dyf8MRNQ6NEJagA85W/Uf9X5Ky/gP+cVS7hbIT" +
                "gJ7M3EDMLtyXj/+k2kU6I3Ge8f67c98/dcMf8CW/BXu/ZrwX7W" +
                "98Nq5a/Anxet35y350ryZ8X23yJ/wn5922+7+jsh/wMKwcLtAQ" +
                "Boxh+ikDBRWJnRtCkcCYSMFQIAABzQ1fpbNi7/wvWHB/YP1a6/" +
                "2bT89fmb3fyL/bM98L++84dQ/AgXDwXyH9AIAvtXrsXPdN1+0y" +
                "l+drPfZnLevzR/0c3/vfOXrFh+3D9H/O2sfoH9AkBZYdi8PQB0" +
                "G/+i62lKhOcf7cH++zusM3fT9a/1+z/S40f+9l3/WLe/38fh7D" +
                "4jh0g/KA35fT4VZl+c6HN3+OuVeKQp0Z/jZD7Hf/T29yaDCo8a" +
                "RoDLwf8L23tYf88wwZXLHIz6GfMPAC4A/gQA4N+C/GHT/CvPf7" +
                "ha/YH33/pOfB72bxusX7r6/0zoD++PcIEIFdgEnp+2Gv+Uxl/k" +
                "H0X1V1Y8f9b5L56f8C0/AP4hz7pX/P+Wzvzjxv8Rf/qP3xnyA4" +
                "Db+iEbHz9h/IBW/Ae+MPDt");
            
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
            final int rows = 7;
            final int cols = 16;
            final int compressedBytes = 14;
            final int uncompressedBytes = 449;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjYBgFQxoAAAHBAAE=");
            
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
            final int cols = 108;
            final int compressedBytes = 3974;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWweUFUUWfa86/xFFxVUQR1FghFEEETOoBIUBBgmiIAbMgk" +
                "gwoiRBRJGs7gqjgIgEQUwDCg5iXldFR1ERPIhnRVgTipHVo72v" +
                "Qvfv/r9/mPkT4Jztc/p3dVV1dVfdvu/dflWfLQMEBg7YrB+bCP" +
                "tBXTgEGhqDIV+bCU2hgD2Dh5gfs+OgJbTSZuGrziFwMqsP90E7" +
                "bTt0YheaxVAMvdgSOB/64S7jfH2UNtyuAwPhMrgGBplFoOEs2M" +
                "c4E3/WGuE8qAcHsfGsRC+G7635cIS+Av7QxkBj1ku/GI7GadDc" +
                "7uZ8gw/DsWwhHA8nwana7ex86Mgm4QIogq5aOXaB7k4TrYTdwH" +
                "prs+FC1hMuZz/A1VgPv9cmQgz21wxjCM6AA+EAOBj+xnbCodAI" +
                "joSj2B16e+1GOAafhBZsvvElnAAnsmPZoc7XcDq0xfm4Qy+C9t" +
                "DZWsUmQE84l+2CWdAHzoP+cBE8ABezhnAVXGusBx0MdhKYYOG3" +
                "bABzIQ/qmBthX6iPz8buZgPhMOwMh+OP2gbrQ2gCzaBQ74pf4Z" +
                "m4nfVnPfB+OA5aQxvtIjjFvAxOgzPgTOgAZ8M5xrvQBbppk6AH" +
                "uNAb+sIFxnYYAJfY9eFSthSugCvNK1yXHc0K9F6sKQxh/Sxd7w" +
                "3XUV5zB1xXm0mpJq6Lh7iuea9Lm/YNvhpbTrn1sYPr2me5rn6N" +
                "bdPvLa7YWGv7a7sD4bWfqzaziHJbUd0t7Ei9Kc6TuVZHs0QfKc" +
                "rz9CHaL3RcYq6UZXb3vJgb2Dhe/GjMl+dGa5FbQq1cqs2m1gVe" +
                "9JT1KHe1m7SxnaHWbqSclrS/ZnwjSo/1SoyH47X0taJsF3YMXm" +
                "teyX+dg0TZCtX6AHl0JoqzNrHJ7CdV+wyzpX2uSs/Gv9i7ZnNR" +
                "pzDeosWC7Rvveim8SuVs95+oJwymdggv/R7W3uOXPgWuJ34NZR" +
                "3gRsjHm2AkFHC8nElaPtwCraxm/FrOL7wB2sWaw3Ac4ZxL/LrB" +
                "OofzyzzT2encDDfjSM4viRdo5lTxnISp5JfrxtrpxTrlwhHmbh" +
                "pDwov1skbD0ZQj+mTMBRpHzi8aiVLznWCvsIvoaYE8k/wS+Ryv" +
                "lylnf68mHCDxIn6N4Pyy0HyY80vgNd/4mfPLnGLS2Eh+0VgUwT" +
                "DoTKUTxD3upJKbOL8UNg3p/FrRssFWxvGCPOon8YvjlVesNYTD" +
                "7Nc4v0RNet85v1i+Qm4p68T55T+j4BcdiV/OEZxflO6Bozy8OL" +
                "8oh/PrVrjSuYieqnPAHnbx7KF5GuQbPbk9NLrh6bGP9JncHrLT" +
                "zXZGMeFFrIF27DToZK6nlG8PzWnOLuc28RzKHjo9QWOtYB/qC7" +
                "HVbMvxsgxKlRh9OF72TRwvaGyPi3WM40VHhRfZw+u4PRR5ZA/F" +
                "8XbJrzhe1kS4WrBnK7eHqkTYQ4WXsoecXxwvaKFda/zG8VJ1CS" +
                "+Vai/xkvaQzpU9pNTFtJM9jC0S9nA1t4ceXp49JLx6cLwo73DV" +
                "nrCH5lw2w+d7d2kPqewUD6+gPeR4sbZBe6jwInsoMCyA0R5e2h" +
                "v2CVBXex3GwRjIt6bCWCjQ/onF1hSjkPMLRsEdnF/iSdrRPtwu" +
                "jrWEYvMtGM/xct28vFg5TJB4efxSb1Z7ejemSX4Jho/keNFxiC" +
                "Uwip0ThRfx6yVpD8Ob9q2HF+2XOztU7nfp+CXtIRxjb6bjXGN3" +
                "Ml7mfMkvNbK7wvwSNT1+rQ3zi36JX/an1HICXpJflt+G9mYqft" +
                "Gvh9dnifwie3inwqslTIWJcC9MMT5xGsB0mGy8z5oTv+42NsEM" +
                "/SjpvwxhdY0N1kwn37uXc6gY55eD45hXJ/a+sTFg84tgkkqN4/" +
                "wK9Px7dfzD+FC083c3cnNKo3KNj8Tvx6IF3x4a77kZNu6/xPEV" +
                "4zc3h429Evmsh3O8gjlwl/DJx1gX+1cWwnHpWjbK6SrXx+sev6" +
                "Vp6j1vAnNth/SG8OcgvC5rbsfgIZFSeiP1lrdf6KxvbEPIRxeB" +
                "slzmn/TceV6+3SKOF/dfhNeDKfB6KXLcSwJjEvdfW7PBK0ZjqO" +
                "2OKrWPTc6DOaGzBWrUP4q6PkbvptE5VH+2GOOu1iXZ4cX5FceL" +
                "UvP81CNqHFd7/gseJX4p/+V0hHz9fu6/9PtIz6+h+uS/zH+Rns" +
                "/37GFsMXQihAq5/6JzaQ+fyWsd9F/SHnL/Ze1jrpP+K84vbg+l" +
                "nqcrb5D20Hw97L9i5IMS/Ff3RHtI+9UCr+8S/RcspJTyX549pJ" +
                "wWOuGdbA/t46T/ohzyX+KY5L/ol/uvzdJ/iTrkv7g9hPqcX2ZZ" +
                "sv8ivAaG7WGU/4rbQ0/Pmy959pD2x6Q9hGU4Bxbp82EJLI0tg8" +
                "fhCXhKX6DPg8VGT30uLCe9UeL8hg+BUrBxeyjuDnn9Evi2MMwv" +
                "erse8fXrtADv+/i5wmfl3Rv9xsVOzI5fUm9kaw/1xZFcvi5re/" +
                "hbKntobkrO17sG0o9mbt3ThyHmPalGdJvHL/PLOL9irYSeJ37B" +
                "M7jEXGPuCPJLL+V6Pu8y6IQjzO1Bfqn7jfT4RfmKX3SndXZenF" +
                "/WVZJf4lkai99ofvWO4pffC59f0foQno7ml/HvKH55+jAzv3Sj" +
                "IvwKIT02M79wlOLXVwF+PSv5hWVm09jx8AI8h+VmAayGVbAGni" +
                "f/VYrr9VWixib8EN8UqdX4KlIeG4M0qvg27e/gJ6LkDfG7EZ9H" +
                "sp24NumN+QhfxLfi/iuoNxJqflBR3x/g18Rs+WU2dnPajLqpSp" +
                "zxGZg5NoseReWt9PilfeHxi/Aqg7rWMFhrjAKye9ZQ/r3MQPiv" +
                "WyS/uJ43xnp6np5vKhSL9nx+wYtxPc/5ZYxWd1pHZ3F+DY/zS9" +
                "WM4FfGnl2YaA8T9bzg14gwvyRekfwK6HlxnkLPG/UCeb6eFy2X" +
                "Jev5ENJjMut5bZtqK8ivdZJf5jbjUmxA73VDZ7q5hdF7Y5GSM8" +
                "iCMke8D/uw/c01zBZ1CS9mUul4RrbA+ivLd5HwYnUkXgEthtG1" +
                "JV6V5NfWGuNXg1QlZlmGK8dkbt3HK6CN8VCPX9RjwS9nht2XHc" +
                "jjUWIU8pmD+VDg4eXxi5lcHzIL2pmzOL94fAMPC/Hr5jC/BOp1" +
                "Evkl41FhfhmTq4pfOCMDvz7PkV8NK8svGe/N8L3c1sMrwK9bJb" +
                "/YGV58g8p3Uj4xDMbhUZBvb+PxDapxlmg5Mr4h8aJ9fACvCQn2" +
                "cEqgbz5eMh4VxovH5xPjG9WEV6728MRUeFEqZ7wCbcfxUvENNp" +
                "RdL/GyfzZ3sSFQl12NTbj/wsbsKuLXNWwYG2T/xPGiuzUV/usR" +
                "NjiMV4hfE6L9Vxgv+5do/1U1eFW7/zq1snhl47+i8MICiRed+f" +
                "wi9EaQ3vgTm3G8mMP5xe0h5Qt7iDSi3B4aC7g9dHrF8cLmcbyw" +
                "MBkvbg8T8MLaxMtqlCNeA2saL49fNL7H094KhNXC1tjS2Sr1Bt" +
                "URUWU2PclnPloBNTApyK/A3M/wXPy9F99I0BvfZas3rBY56o0r" +
                "suz9Xcl6I338MOH6pPghngRT8QSk7zfn10CrgS9sHj+0fwrdc2" +
                "HueNm/uFW0VTR+KPBqkyNe19c0Xuh7TC8+L9LTYXKskOOFbT1+" +
                "4elJ93ysCvDCqsWrYnqe28Oc8Bpea/wq0+pq5F2wK5abO7GI+j" +
                "KM4wUP4frY1Oj4hrEofXxD2zdwxxKOl4xvZGMPc4pvVMP3V3R8" +
                "3pqc5dWzK4aXjM+7aeLzlFLxQ+xh7vLX24j4Bo8fUh/Jg8fm8P" +
                "ihqn+ysVjp+U5SbyTGD+Pxeak3ZPwwtT4Mxg8T55fFWdL8crbx" +
                "Q6U3kuKHKfVhlvFD64Go+GGU3kiMH0q9kT5+6Kr4fEhv+PF57W" +
                "Dk470EllJ/GmBvPC8e34Dlvj70Vpjw+MZSZlXAdgT0YehdP3Dv" +
                "tYfWg5W2pGMqdx2q+QzsiwOwP16A/bRueKF2dlxvxONRHC8/Xs" +
                "nxWlYVeO2p/kvrnPme9rs1jpdaFQHLKH2Rxy9KXwJPCb0hZrBh" +
                "OfdfIiX4pfzXckwR5dOuj8aLrkzwXymfq0b8lzY3t3fEea/W+F" +
                "VG+6UiVR7W87he1UjWG09k0BtDE/FK1hu1i1eu9tD5oMbxGujh" +
                "5euNcu22oN7A9Vxv4CCJF9cbEi/SGyvwdWiHb0MnjhfXG/iGWN" +
                "8r8NJu0W719IY2UuoND6+43hAjHaE3JF7VrTc4v3LRG7FxNa43" +
                "/PlKQByCw3GoxItKBF6cX9A0zi9oGeDXk5JfEi+6oleQXxwv8a" +
                "wcr7GcX3G8UjCkcSp+ReEVxa84Xn4uqRlU1hkahfkFLVLxC9qr" +
                "o8JLnfl4iTNd/Pp4UVp4Zw+vhBabRfHLw0vV6ZB0lcIrMC7D1H" +
                "GcKH9B1VuNY2GN+P4qjdtD2oU91B5QeD3l+S9Ua2/D9jDZf+1p" +
                "9pDNd2tpq7Q9VNfhnYD0OwHvEH2P5JcoaRnQG09LvDi/xLGXGo" +
                "05Qbzk95fkV3q98X9+ZcEvtc5AW+HNV4qz5/n8F06U8XkqKdBW" +
                "xuPzUs+T/3qGx+dTzafgXdnE54P+K1183l5aHfF5e1Fu8flQnZ" +
                "TxeW1JFcbn1XoArUx7QfPnnbUXPX2IK/0vrmFsUMI78iwbXLHv" +
                "r8pv9pqMlqK04vbQfm5vs4d+b7fgZ7gVPxXpz+N6Hr/wa2wL1d" +
                "9MpaUVesLR1TsClcErVz1fi3j9jrtRrH40PkGxtiz2uMDrT2NT" +
                "Il7GBoXXyrRPtLFieMn18yn5FWkPg+vn43hlv37efn9Pxcsoz9" +
                "CDt/2apMw10g95wuOzUcYmNjpsDw21Np6NSWcPE/HK2IMPK24P" +
                "c8ZrQ23hlWn9YSa8sIz9Q6VCNdmDqeMblEof30iKrlVez6fXG2" +
                "G8Mm8+XotqDa85uV2vfYa7PX3I7SHXh6xUzqdwfejZQ08f4mbS" +
                "h6sS10eF9GFhNuttstWHGb69QvpQ5GSlD1PGN6pUH1bHehttJ1" +
                "+/wdQqR+3nxPi89mNEfP659PF57YfAHSfVXHw+e37VlN6ojvUA" +
                "we8vyS8R31DfXzTWhJf2a+j7a0267y/29p7Pr1zXR9U8v9g7il" +
                "9kA9kW//3Ljl8vZM+vXOe/nBbVwS/nmL0uHuXH5735Sixnge+v" +
                "tPMpa9PrDR1qJ36459nDqsOL+d+WEi/2JU/rdiK/IueXX9wz55" +
                "crwK82eytesEyvy7Z788v64WxHFus31u3teO1N/NLFf2GwTwC7" +
                "/yS1Glgvqh+ZVPpSzcWjnAHVwq/+e6//AmT/ld/LZkGW85WvpJ" +
                "uvJITlLEhgPqVm5ivpGJpPYb+rdNJ8Sqr1h3v+fCX8D/GYZVc=");
            
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
            final int compressedBytes = 1634;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWntsFEUY/2Zv5m7vrhUqVSEgSFEKgiL1hVaqEZGHiqhBak" +
                "WlVRExSiEafLRFDYIx8YFI2yDxETA2xhg1xqhpjNqWorTSYAmU" +
                "aAAFFVIUi4KPP9Zv5/a2u7d7j93b692SnWRmvpmdne7ur99vfv" +
                "PdkGYgtMoXlCTSJUkwCPNQGMFqsN0JYyVMpId0k61wAVqfkhby" +
                "sSSxFtKGrW0wnXSQ3XjHzWQLH7mLfEI+kyRaKbdgMSyRa1YLeW" +
                "Qn+Zx8I8VJcHa/TXborlweXIflNdyeDdeZ3n035vsUO6T2DpEk" +
                "4V/FHh3t9T3M2+cH18Z5kquV+iaYq+m9A/OdmBfxFuWlHwLq9X" +
                "xeDjOdcby+zep478WYL1PHTDPcdQvcCuXaHvJ0pKZrePmt0npW" +
                "mbWGbheCsiXkCQVYitH7BD9ebRUCUsqJ1eJd+VLGEvko9bERvC" +
                "QpVCQNQKKrTb5GXZpz1tP1tMEwa41mRKPhapuF+btlvKwnYXOq" +
                "I2X/soqX8EbKz/+dsxjaxYvuVJ58Kb6xAEHgHiQ8CIPpZhkvGI" +
                "mtRVAsLBaqBeQ1WAGTle8zhW0RHoAytJbx9hxezofblBm3YasS" +
                "qpTRPi1eUAinaVpn6b77OMznKvZ5mEvg0qRY3R6LFxSo1qlKPR" +
                "yWQxGMieAFE6P+BRfBJcqIK2CqYlXDTN1sj8A8qNC07+cl040J" +
                "8/IUtX0m5lGKfQ7mCXq8YBJcqI4thSvhKqyvhRlYzoLrsbxRM/" +
                "cCuAvLhfhVO+BejlsTWUAqSLlwkL/Pn/3+FeFDX58JH7Yn5kPf" +
                "0YHkQzv+FZojZSnZ9S9hr8L+zfQDNoNbXTHrQqdSc73BLUVvoM" +
                "X1BmauN7DW6433DWtMQr2hG7ljQPCamzW8frC5SlcqvNhKW0xm" +
                "rWHPqMxpWK38PktPWJvZL2BLb8xzm3+paOwynbWGvaOO2G3Aq9" +
                "D1eM13K17Ih3sUq0uLlz8Ynw/9pyfhw55YvDLJh3bwElpd61/f" +
                "m/Ohf1ACPiwfOP8K3ZMR/6pyr38hIvvoj7J/0QMavIYk8K8lSf" +
                "xrf677V2C0m/Ey5cOKBHg9kQSvg7mOV+ght+KlMNwqndbHXat/" +
                "KbdwvydU05+lHE7u8i9hQprr1yH6G/01kE976XHaRw/TI/Qvfz" +
                "32b1dHnNCN/8XWXzlG/8gpffho1vCani4f+hvoP4HBWj4Upsl4" +
                "pbFf/nsg98su86+08WJ5LGSMbwR+T7B+vZUYLxb28IqL1w3pzw" +
                "ECZhFxeLu/TyxWV0hF2Ufjvfg3Z2OrLGYONd6rtNV4r643xXiv" +
                "KMdJSzhzvZfs6cVxulkKDG83HLMa7+3X82bxXrRnxtyti4SkGu" +
                "9NFS8oxczjvbw1i5em8V5xfCTeG9GH/uNOxg/ZGZqv+Vh6/hX6" +
                "MOl/m534Ydb2X/4T9u4TH1f5cJhRzyNjTCWdgTJzvIS6XIr3uo" +
                "wPVzo9Y4BHkMXnlDdsF6pZ1t4uY/owa/7l25q+nmdF4kjay8ZE" +
                "9by8/wqNje6/TkY97979l+mcMl6l/DvjKk2GZtN7Qu2p4UUK3e" +
                "Ff6eKF61eJ2foV1RviQht6Y7LGs7ozvX7Z0Rup+5fT5zfs8mH0" +
                "/Abnwynicm18A3tfD0/C8jUn4hv0zXT4MPSTiVZa5t71i25yYv" +
                "8lrgAx5psUR85vsCej5zci+y/5/AaWAWv7r+j5Dev7rxSUro39" +
                "l+xf2dl/6fW89f2XPrGnNEwbFL8wP38o1KV+/jCy/8q18zbu0/" +
                "PR/RdbTdRoH/mvX28o3ivrjQM69tkjuTi5Xx+y5+Xfl4ONCRi3" +
                "TTppUvbxshvf0OjDF3D9LXf0PFuPc/GN8IhM4OW+eJTqXy+and" +
                "/IHf8Kjzq58HIgPrAhyfVXEdWXPD7MEbSaWSNbz9axl0kXq2cb" +
                "2FrWkC4fslec48PM4OVm/2Ibk/uXpzdyBq0mo56PwSurej480f" +
                "MvPR+qlnO/VzbnOh+6179ivO1LbXyDlybxDTkeZXGXaDu+EV7p" +
                "+ZdFDL/Oqp5f5eFl0Teq2d4s4rXRwyve+sX2O7R+7fP0/AAgF1" +
                "dvYDbBi9cd7JARL5O57cejNnl4WdWH7LA1/3IUryYPr8zqedbL" +
                "jjiI17seXikiqeFDtc/AhzoleTSef+FIu3g1e3g5jxfryxQfen" +
                "rDDl7sWGK8Mrh+feXhpU3wPzLS/rc=");
            
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
                "eNrtmktLw0AUhXvzmLZSwbpTUHTvRkVwLypuVXAjuhWrErD6y0" +
                "R8UQRRxAcUUXysFPwBIiq1WmMNJW3aVGxJbO70XOhkmEBg5uOc" +
                "e2em+qtZCEqbjqDzwvM2/zu2ett0QJtW77Dw7oxurOeR1V7TFu" +
                "2YLkFXlKIT8w9BF6aPoSZNaUN/K/KyV7OCV9lqV+GVfwNengTt" +
                "1asv/R36CpS+stAXx1AM/VOWucjghyKk3etfeo7S2qObHwqiSz" +
                "c/pNNqfqg9wA/9CqGou+q+PZ+Um76UxYqxBPQV3HqjVn0JDfry" +
                "TV86Zey1+nBdQadL3iF/NZSXAC/G9MLIXwEnFIG+WPFqAS9W9W" +
                "GWMiJWyYtyXHjRRlPpqw36YkwvXlJTRK02puTHlIg9Kqw2XNt3" +
                "lVbw8ohQO/TFjFgHeDGi1YnzQ1a8usCLGbFu+KFMoRiiR5a5yH" +
                "JfKXpxX8mFl93z7v6rD7wamN/65ZmLFH44JAZ/05cYrllfA9CX" +
                "j7xGxLgY89QPR8HLR7+bQD0vRd6aLNTzcUd1X9d5L3j9A68p8G" +
                "K5X3bhJabBKxCamikhg/svXrqKFvUlZuGHbLgZYg775QDtv37O" +
                "D+dxfsgkYy045vNS7ofqc+35S30Kav6SoMJINNP/NyTgtQRerH" +
                "gtgxcrXivgxYqXAV6seK2CFyteSfDitV+2e97dV64Fdb8sgb7W" +
                "oS9OEfoGjJpKVg==");
            
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
            final int compressedBytes = 545;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmzlLA0EUx511Z0ZBBTu1sLawyUewsROstBZERBHTeaCJCp" +
                "6dN0T8Ah5oIfEg2IgiGiGI4lEpptTKJmCxrpMQkxjQyI7ZZ/4P" +
                "do7NMAnvt//33oRE9LGYlTD2ZmUxFk2b3Vsw15jo/xwbpaotMy" +
                "rttiR5V6hW5ravUQ7fOkRoAPoixWsQvEjxGgIvUryGwYsULx94" +
                "keLlBy9KxkJiRkyIKTHJIpbFA2KcLydeuUj0d+yKnarRPjtiQT" +
                "U6tq9z+wqzWzU/Ue0N22MH9i4LX97lmh2ysx99nssC0sp0juvb" +
                "f7RqAU+1ixgvfnvWbYCXXBUP1/gin+dzLMKXeIDPIh66nNc68h" +
                "ed/MVCdr+hPBXJ8NyveZlt4KVPX8mRY7zEJnjp5CW2oC9C+WsH" +
                "9UaBx8MgeGnU1y7qQzr1oXhKOxv3pM06VOs1ur6cobvxpLv2KX" +
                "iGD9yVv8wH8fKRv8yoQ/XhI+KhLjN6M+OheE2Nh0Yn4uG/rw8t" +
                "6IsSL1kEXqR4CfDSx0uWOM2Le8ArfyYr4QNSFaRXVsEL/zp/VS" +
                "Me6uQla5zlZcbAi5S+asGLFK868CLFqx688kAyySvlXgov1YfT" +
                "Kn9PnFfW3cDLvfpqhL5I8WoCL1K8msFLHy/Z4jQvcxu88vp9VC" +
                "u84CJ9DeH3hwWev3zgpVFfI7aHR+O8eED6oa+/MjmW2/r4/1Pk" +
                "itP6Kq4AL1LxcBW8dFnRO2zP8N0=");
            
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
            final int compressedBytes = 640;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm1tPE0EUxxl2bo9+AL8FH0Bf0PhoAq8+GNQEQyDxQb8AVo" +
                "MJN0FSgxcIgfDkgyFoTGPSaBqpCTFWoU9VkVuABA1xU2PKsm2h" +
                "bIuldKadCf+TbOfsdtptz2//Z87MtuIJjWb+a/RdBmaMkTdiQo" +
                "yLKc+bDzzzMduKSfKZxPwjr0iUzPiex5DMeVucLPj77/3Hr2SW" +
                "vPZeMVZ0lgSJkA/H+jyfwKQ6a+wSUJhB+tr3jtAXWaxYXzHoqx" +
                "a8xJwKXs5t5w541TUfxhEFY/SVJq74kvP/Fhz/t+8tHeqfNO4b" +
                "vDQ1tiKh5V0XnLd534mU0ldje9Gxm7jSDc6HSUTBoPnXD5oS3/" +
                "fqDbqkqD78hnqjjln4J2JgVj3v6WtZpb4o9KVPPSti9WDPiaPe" +
                "sJ7oOmJgFa9txMCs8Uu4/ILS9cM/GL/qOf+SZxAFU0yexf1K2/" +
                "JhzlOWD2UT8qE+XvIcG2KDbIDMs2EWZv3sUbW82EPw0sjrPL/L" +
                "7/HQnr5YmHeDV62M36+wf5umfHgJvDTq6zK/qLae583gpY1Xmr" +
                "iyNefjfqXa2rtFT31IU/IK1nvtMDosr5bpMYIoWTWjvoYYGKSv" +
                "Nbolr3vthrxBf9F1ukl3Aj0OrQbSlROd5TfFqnGt9NWBGJhUz/" +
                "v1RifqDat43QIva/Jdd5mxB+vzhukr56lbjwpBX/p4yQfKefWA" +
                "l0ZevbJPLS/6Arw08hpQrq9+8NJWbQzi9wCnvt54DH3p5CVHs7" +
                "ww/zql+noKXlbxegZemtejniMfWjR+jSmef7ngpcvomhz32w1v" +
                "8+9/yYlAD9z/smNmNp3XV8HVX6Avvw38Xyyrr5K6gb6UWMMujz" +
                "pxCg==");
            
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
            final int compressedBytes = 578;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmssrRUEcxw1n5sxEFP4CG8nCI8UOC++/wEKhu2LDykaJ8l" +
                "p6l7JhwQKRRwgpXcmjJMKOkvwTOjjHs+vcHM7cO3Pv97u4Z845" +
                "c++t3+d85/ebOWNZbuLzzpHcfF4j+2TdPgbfzk++f4dskK2ff4" +
                "3skiPrFyLnFvQnkVOvvPgCeEmnsv3ROnPjRS7Iod3a/MqLHL/y" +
                "Itf2+YH9eeXw4osh/3IJXgrzWgav6ImvIAZq+cu45auv/jLu/f" +
                "GXcQd/aTUeroGXTF7cYQB/xUb+2kYMVPIXS3jJXzv0yc1fjMBf" +
                "MZ6/guAljxc/8JuXsQReWvnrELwk+uvIb140Gby08tcxeEn01w" +
                "MdoyN0mJzRcTpJh+jEv/01Cl4SeT1altnt+ItOml3gFSmZPd76" +
                "s4BlCW7sh+9lBPFUq5y/RMp//SVQb0SIl0gTqRgP1eYl0lkVq/" +
                "SzPmQV4BU9iQzEQK3x0LgVWXifEr/zZVPyepTIjnt/5cBf+vDi" +
                "Uy/PbC7ef2lVVeS53slHdGI7f4kC+EuezOukvfd20m7o/cS2xJ" +
                "aQa61RHQsK477eKEK9oWUWK9bBX1BYhiWIgVa8ShEDlfKXKAtf" +
                "H7ISz+u9hchfWtXz5eAll5eo8ZVXNXhJ5lXnK69a8JI8/6rH/E" +
                "vX/IX9G4rXh81feYlGH8bDJvCKlL9EALyUnQk3/KqXFmtPJBPz" +
                "rz/7qx3+kpi/OlgfG2D9zn5s1ov9h5ESG/TYP2Dz6jRnzDl3f5" +
                "mznvfbTIOXLCU8A0BPkP4=");
            
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
            final int compressedBytes = 496;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmztLA0EUhb1xJ5sZhRQ2Igg2opZiYauCEfVX2GqTfyCW2v" +
                "mGiBZaGEUbwWBiEHy/IgQxRLsI/gi7Ma4iahQimXF3NufCJrNb" +
                "no9z7h1ml9J8IjgQjEhJWfml6Ob9/4Hu6MJZJemYEs7qtHhdF6" +
                "8M3Tv3Z85vnvYoJWWwX34rytEBXckyim4lquzik6XPAtHAaMmz" +
                "MWjlWYZT0MAoXjFo4J2i9MdKWf/iS+hf+njxFdW8WCd4GeWvNf" +
                "AyilccvHTysgp845WX9aSGl/UIXjp58U21/rKewcuoPNwCL6N4" +
                "bYOXUbx2wMsoXrvg5V4FojwBFXztryT8ZRSvFHi5V3wfGvjcX4" +
                "fwl6vzxhFUMCgPT6CBz/PwHHmojxe/VM0L55Uu968cVDCKVx4q" +
                "eLd/8fqK+1cd8vB/ePGwPc4WK+5fc+Clk5cgO/77vGGv/5WXvQ" +
                "peev0lAirnQ/DSy8sqiDDetzGKVwN4+WeeF41QwWP+aoK/fOSv" +
                "Zqjg3f1y5f4K4fsUrbxCy2+8VOWhaAEvXSVaP9/VZn7KQ3wP67" +
                "U85IMq81C06fWXaK96XsNKzyuHNPPqqPp5vgvzvI/m+W6o4B1/" +
                "iR42z2bZDGXZAouxaZyneD0PRa/a/Zc1Al4m7ZdFH3hpzMMI8t" +
                "CcqnkB1ybf2g==");
            
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
            final int compressedBytes = 429;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmMtKw0AUhh3JUDNx4yP4CL6LCIIbF7pSqCtfQB+g3gpufA" +
                "QF8Up1I0qxFYooWipYpRsv4MYbbmIM4kIXCp04OeE7kGRCFgn/" +
                "x3/OPwnD38r0h1RqSpW+VrVvT44+r3V1osrxakvtqfV4tR8dle" +
                "ioqvP4/iA+n6lNtR0RHvjxllO1qw7/9D3HMPl3XoPwSo6XGbLN" +
                "q6sAL1H+GoaXKF4j8HJXnXkzigqZ9tcY/hLFaxxeonjl4eV0fk" +
                "2gQqb9NYm/nPprChVE8ZpGhfT0Q1PU83pWz6iaXtCLuqCL7fZD" +
                "PUc/dFdmCQ3SnDf87nb95Qf4S1Q+XIaXKF4r8HKaD1dRIdP+Ws" +
                "NfTv21gQop2n+VvGbu+sNfXsuOv3JX+Mvh/msHDYT1wzIqZDpv" +
                "VOiHCc6vqm1eHvsvl/OrjgaieDXQQBSvCzTIeN64ZH6J4tWCly" +
                "heN/ASxesWXkny8prmzub/Q4//h4nyMveW98uv8HKY5x/QIOPz" +
                "6xF/ieL1BK+E88YzeUNQ3nghb8jhZd5s90PdBy9J8yvw4eWugh" +
                "40SFveCHrJGzKq4x0E8kjo");
            
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
            final int cols = 108;
            final int compressedBytes = 380;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt17tKA0EUxnEPLKwwryFBGyvxBl4aL0+ioo2tz2KjEVELWy" +
                "8x2ngLRiWIGgMWCj6E3bgsIhJQVByWb/d/YJMJgS3Oj2/OjFW9" +
                "j55cl/fWiF78p7Kr9++W3VotXe3bse2kq9PkqSfPpT2kv8/Sz6" +
                "btWSV537NvK7uzI7vwPyi78dR3/al+rBpt//zZq3MZr+zKlehB" +
                "vvPlusmXlFcPXiG9kvNGL+cNFS/XF6/Fm1/nK974rVdcxivD80" +
                "Y/PZDyGqAHOT9vDLIfSnkN4SXlNYyXlNcIXlJeo3gFvi+PcV/W" +
                "8XLj/5uv6BWvDO9fE/Qg5/NrknwFnl9TzC+h+TXN/Cr0fjiDl5" +
                "TXLF6B59cc86vA+ZrHK3C+FsiXlNciXgXeD5fwkvJawUvKaxUv" +
                "Ka91vKS8tvCS8trGK7tyu/Qg5/mqkC8prwO8pLwO8ZLyOsFLyu" +
                "scLymvGl5SXnW8Mrx/XdODnOfrnnxJeTXxkvJq4SXl9YhXqOp4" +
                "A51QbXo=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 111;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2LERACAIBDD3H5p3AwsrOJIR5AU0eakDnUV+ATD/zD/1AQ" +
                "CwH6E+AJgvrKm/fLn/yIf8OR/5Vj/3C/kBAAAAgB78bwEAAOD9" +
                "CYD+D6A/688AgP1o7n5kvwMAAAAAAAAAAAAAAACADxcjvMY/");
            
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
            final int compressedBytes = 80;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt3KERAAAIAkD3H1rdwGLy/O8QiUQAANfUKNf5bT8AAAAAAA" +
                "AAAAAAAAAAAADAd/4bAOw3AAAAAAAAAAAAAAAAAAAAAMAl/ldg" +
                "0Dy1uUE=");
            
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
            final int rows = 616;
            final int cols = 8;
            final int compressedBytes = 49;
            final int uncompressedBytes = 19713;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt0EENAAAIBKDrH1pt4Fs3iEACf/WqBAEAAAAAAAAAAAAAAA" +
                "AAAAAAAFwzmw0e4Q==");
            
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
                "eNrt2MsNwjAAREFDCOETSJoKdbnxACXA0fKO9urTG1mWXEv9sW" +
                "2vxfpY+eMMb97G23gbb2vb+3XTKcr7pFOU91OnqPf7o1PU/T7q" +
                "FOV90CnK+6FTlPdFpyjvs07eb+NtXXjrlPXf8tYp6n7POkV5Tz" +
                "pFeS86RXmvOkV5jzpFeQ86RXlfdfK/Zt1633Vyv423te9dvjAZ" +
                "e3c=");
            
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
