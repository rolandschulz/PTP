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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 1, 46, 31, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 17, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 0, 3, 13, 4, 106, 46, 155, 156, 5, 157, 158, 0, 6, 162, 159, 178, 26, 7, 8, 160, 161, 163, 0, 168, 169, 179, 180, 171, 9, 10, 97, 181, 182, 183, 11, 172, 184, 46, 12, 185, 13, 186, 187, 188, 189, 190, 191, 192, 46, 46, 14, 193, 194, 0, 15, 195, 16, 196, 197, 198, 199, 17, 200, 18, 19, 201, 202, 0, 20, 21, 203, 1, 204, 205, 74, 2, 22, 206, 207, 208, 209, 210, 23, 24, 25, 27, 211, 212, 178, 180, 213, 214, 28, 215, 26, 74, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 74, 226, 29, 227, 228, 229, 230, 231, 232, 233, 234, 235, 86, 105, 236, 30, 237, 238, 31, 239, 240, 241, 3, 242, 32, 243, 244, 245, 0, 1, 2, 246, 247, 248, 46, 33, 249, 250, 86, 251, 184, 179, 185, 149, 13, 186, 187, 188, 189, 190, 252, 191, 192, 253, 181, 4, 5, 96, 6, 254, 34, 255, 35, 256, 105, 257, 193, 258, 259, 260, 194, 105, 261, 262, 106, 107, 108, 112, 263, 115, 120, 122, 264, 182, 196, 265, 266, 267, 197, 199, 268, 269, 106, 270, 271, 272, 273, 7, 274, 8, 275, 9, 10, 276, 277, 0, 11, 36, 37, 38, 1, 12, 13, 0, 14, 15, 16, 17, 2, 278, 18, 19, 3, 13, 4, 20, 279, 21, 39, 23, 24, 29, 280, 281, 31, 25, 32, 282, 34, 283, 284, 35, 40, 37, 41, 285, 42, 43, 44, 45, 46, 47, 48, 286, 49, 287, 50, 51, 288, 289, 290, 52, 53, 54, 41, 30, 42, 33, 177, 43, 44, 55, 56, 57, 291, 292, 0, 293, 58, 294, 59, 60, 61, 62, 1, 295, 63, 64, 296, 297, 298, 65, 66, 67, 68, 299, 69, 70, 71, 5, 72, 73, 7, 74, 75, 300, 76, 77, 78, 79, 301, 302, 80, 303, 0, 81, 304, 82, 83, 84, 85, 87, 305, 88, 89, 90, 306, 91, 92, 93, 307, 94, 95, 96, 308, 98, 309, 99, 100, 101, 102, 8, 310, 311, 312, 313, 103, 9, 314, 315, 316, 317, 318, 319, 320, 321, 104, 105, 10, 107, 108, 109, 322, 110, 11, 111, 112, 113, 323, 324, 114, 115, 116, 0, 325, 117, 12, 118, 119, 120, 13, 45, 13, 326, 327, 121, 122, 328, 123, 14, 124, 125, 15, 126, 127, 329, 153, 330, 17, 128, 129, 130, 21, 131, 132, 16, 331, 31, 133, 134, 332, 17, 333, 334, 3, 335, 4, 46, 135, 5, 136, 336, 337, 137, 6, 338, 138, 139, 339, 340, 341, 140, 18, 342, 343, 344, 345, 141, 142, 47, 0, 143, 144, 145, 146, 147, 346, 148, 19, 48, 49, 347, 348, 349, 149, 20, 350, 150, 351, 151, 152, 352, 153, 50, 154, 99, 155, 353, 354, 355, 1, 356, 357, 358, 359, 360, 361, 155, 362, 363, 156, 157, 161, 163, 22, 13, 158, 364, 365, 366, 367, 368, 369, 51, 160, 370, 371, 164, 372, 373, 374, 165, 375, 376, 377, 378, 166, 379, 2, 380, 381, 106, 167, 382, 383, 384, 385, 386, 168, 387, 388, 389, 390, 169, 170, 391, 392, 393, 156, 172, 394, 395, 396, 397, 17, 198, 27, 398, 173, 399, 200, 400, 201, 401, 203, 205, 402, 206, 46, 174, 175, 176, 177, 26, 403, 178, 404, 405, 179, 406, 207, 407, 408, 0, 181, 52, 53, 124, 409, 126, 410, 185, 186, 411, 412, 17, 211, 413, 188, 414, 7, 22, 55, 25, 33, 415, 36, 416, 417, 418, 38, 166, 419, 65, 215, 57, 0, 3, 420, 421, 1, 2, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 208, 444, 445, 446, 447, 448, 449, 450, 451, 452, 66, 453, 67, 454, 455, 456, 68, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 36, 38, 55, 474, 475, 476, 477, 195, 478, 479, 201, 480, 210, 80, 481, 482, 483, 484, 485, 486, 487, 81, 82, 86, 92, 212, 488, 489, 490, 94, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 512, 182, 513, 514, 515, 213, 516, 517, 518, 214, 519, 520, 7, 521, 220, 3, 522, 225, 523, 524, 525, 526, 527, 528, 529, 95, 530, 226, 531, 532, 533, 96, 233, 534, 535, 235, 536, 537, 97, 538, 98, 106, 539, 193, 194, 540, 196, 541, 197, 542, 202, 543, 544, 545, 112, 113, 114, 138, 56, 546, 133, 141, 547, 4, 548, 189, 549, 550, 180, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 5, 561, 562, 6, 563, 8, 9, 10, 11, 12, 13, 564, 565, 566, 567, 568, 142, 569, 150, 570, 151, 236, 134, 571, 203, 572, 205, 573, 574, 152, 575, 576, 577, 578, 14, 56, 579, 580, 581, 184, 582, 583, 198, 584, 585, 586, 587, 588, 238, 589, 153, 590, 591, 592, 593, 594, 595, 596, 597, 598, 154, 599, 600, 601, 602, 159, 603, 604, 162, 605, 606, 607, 8, 608, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618, 199, 200, 619, 206, 620, 127, 621, 211, 16, 622, 623, 624, 625, 626, 627, 58, 628, 164, 165, 629, 630, 631, 166, 632, 167, 170, 171, 173, 208, 633, 178, 4, 168, 169, 634, 635, 9, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645, 172, 17, 179, 181, 646, 59, 183, 186, 192, 1, 207, 209, 60, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 647, 241, 227, 648, 649, 0, 0, 74, 57, 650, 651, 652, 185, 188, 190, 226, 228, 229, 61, 230, 63, 243, 653, 18, 654, 206, 231, 232, 233, 234, 236, 655, 656, 237, 657, 658, 235, 238, 239, 240, 241, 64, 659, 660, 661, 242, 243, 662, 663, 664, 665, 244, 10, 245, 19, 20, 666, 667, 216, 668, 217, 669, 670, 671, 672, 673, 58, 246, 69, 674, 675, 676, 677, 247, 248, 5, 678, 679, 680, 681, 682, 683, 248, 684, 249, 250, 70, 685, 250, 686, 687, 688, 689, 251, 7, 252, 253, 255, 690, 691, 692, 256, 257, 258, 693, 259, 218, 71, 260, 261, 262, 263, 694, 264, 265, 266, 695, 267, 268, 269, 696, 8, 270, 271, 272, 219, 220, 72, 221, 222, 697, 73, 128, 74, 75, 76, 77, 273, 698, 699, 254, 700, 223, 701, 274, 276, 277, 702, 703, 224, 228, 704, 705, 706, 229, 707, 708, 21, 709, 27, 230, 710, 231, 711, 712, 713, 714, 78, 278, 280, 715, 1, 81, 30, 82, 86, 58, 92, 59, 94, 61, 716, 282, 281, 283, 717, 718, 232, 719, 284, 720, 233, 721, 92, 96, 59, 285, 286, 60, 255, 105, 234, 722, 61, 723, 239, 724, 62, 287, 288, 2, 63, 289, 79, 290, 291, 64, 292, 725, 295, 257, 726, 727, 1, 728, 261, 729, 293, 63, 730, 240, 731, 732, 241, 262, 263, 733, 734, 735, 736, 296, 298, 299, 244, 737, 738, 245, 264, 271, 739, 246, 740, 741, 247, 742, 743, 249, 80, 300, 302, 304, 64, 305, 306, 0, 251, 307, 308, 744, 745, 746, 303, 309, 312, 311, 313, 314, 318, 322, 65, 0, 315, 316, 1, 317, 319, 2, 323, 324, 69, 325, 326, 327, 328, 330, 331, 70, 332, 333, 334, 336, 338, 339, 340, 341, 342, 343, 344, 346, 347, 348, 349, 350, 352, 353, 1, 252, 354, 355, 358, 356, 357, 359, 361, 362, 363, 364, 365, 366, 367, 368, 360, 369, 66, 370, 83, 2, 67, 372, 373, 747, 374, 68, 71, 375, 379, 380, 382, 383, 748, 384, 385, 386, 387, 371, 377, 749, 388, 390, 391, 394, 396, 400, 401, 404, 406, 407, 409, 410, 750, 411, 376, 378, 415, 381, 392, 72, 73, 74, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 389, 393, 395, 398, 402, 405, 408, 412, 413, 414, 416, 2, 751, 417, 418, 419, 420, 421, 422, 752, 423, 753, 754, 403, 755, 756, 424, 425, 757, 426, 427, 253, 758, 254, 0, 759, 428, 760, 429, 761, 430, 762, 110, 763, 764, 765, 256, 258, 432, 431, 259, 433, 260, 434, 435, 766, 767, 436, 437, 438, 439, 440, 441, 442, 443, 260, 768, 444, 445, 446, 447, 448, 106, 449, 450, 69, 451, 769, 770, 111, 452, 453, 454, 3, 261, 455, 456, 457, 458, 4, 460, 771, 461, 459, 262, 263, 462, 463, 464, 465, 466, 772, 467, 773, 468, 469, 470, 471, 472, 473, 474, 5, 264, 475, 476, 477, 478, 479, 480, 482, 483, 484, 485, 486, 487, 490, 481, 774, 265, 775, 776, 70, 491, 115, 497, 499, 116, 488, 489, 492, 493, 777, 778, 779, 494, 780, 495, 496, 498, 500, 781, 782, 508, 783, 784, 266, 509, 511, 785, 512, 786, 787, 267, 501, 502, 503, 504, 505, 3, 117, 118, 519, 788, 515, 1, 789, 790, 4, 521, 522, 119, 84, 11, 506, 791, 507, 510, 6, 792, 793, 107, 71, 794, 795, 516, 523, 524, 796, 269, 797, 798, 268, 525, 799, 270, 3, 800, 801, 513, 514, 517, 518, 85, 520, 802, 529, 526, 527, 528, 530, 531, 532, 87, 534, 536, 267, 533, 535, 274, 537, 803, 538, 539, 540, 804, 805, 543, 806, 807, 271, 808, 541, 542, 12, 809, 810, 811, 812, 544, 813, 120, 545, 546, 547, 814, 548, 549, 121, 122, 123, 815, 272, 816, 550, 551, 275, 817, 552, 88, 818, 819, 820, 821, 273, 274, 89, 276, 822, 823, 553, 554, 824, 825, 4, 826, 827, 828, 829, 90, 830, 125, 831, 832, 833, 555, 834, 5, 835, 836, 556, 837, 838, 91, 7, 839, 840, 841, 126, 842, 843, 844, 845, 277, 846, 95, 96, 847, 848, 278, 849, 279, 557, 558, 559, 850, 851, 852, 853, 560, 854, 855, 128, 856, 0, 857, 858, 859, 129, 97, 98, 101, 130, 131, 860, 132, 133, 139, 140, 861, 862, 102, 863, 72, 864, 865, 277, 866, 562, 561, 564, 565, 566, 567, 568, 279, 867, 143, 868, 869, 108, 73, 870, 74, 871, 5, 569, 570, 75, 571, 144, 572, 103, 573, 574, 109, 575, 872, 292, 280, 293, 576, 577, 578, 281, 282, 579, 873, 294, 874, 283, 875, 295, 284, 296, 876, 580, 877, 581, 582, 878, 583, 879, 584, 585, 586, 587, 588, 598, 589, 880, 287, 881, 288, 289, 882, 883, 884, 76, 590, 885, 886, 591, 887, 888, 889, 890, 891, 0, 892, 893, 894, 895, 603, 896, 593, 145, 897, 898, 592, 604, 899, 900, 606, 901, 608, 902, 594, 595, 596, 597, 903, 599, 600, 601, 602, 904, 612, 905, 613, 906, 605, 907, 908, 909, 607, 609, 6, 7, 610, 611, 614, 615, 910, 297, 911, 912, 913, 290, 616, 914, 301, 915, 291, 916, 917, 918, 617, 618, 919, 920, 104, 619, 620, 621, 622, 623, 624, 2, 921, 922, 923, 110, 77, 625, 626, 627, 629, 78, 632, 924, 634, 635, 925, 637, 926, 927, 79, 636, 928, 300, 638, 929, 930, 639, 931, 932, 933, 934, 935, 936, 937, 938, 939, 302, 640, 940, 941, 942, 943, 146, 645, 648, 944, 650, 945, 651, 652, 641, 946, 947, 948, 644, 653, 949, 0, 950, 951, 952, 147, 8, 148, 654, 646, 953, 655, 149, 647, 649, 954, 656, 304, 955, 657, 658, 660, 956, 155, 156, 957, 305, 300, 958, 661, 959, 662, 960, 663, 961, 962, 664, 665, 666, 963, 667, 964, 965, 157, 966, 1, 967, 968, 668, 669, 672, 673, 670, 105, 9, 671, 674, 969, 13, 970, 675, 10, 971, 972, 973, 974, 306, 975, 676, 158, 976, 307, 977, 308, 677, 978, 678, 979, 310, 679, 313, 314, 980, 323, 161, 163, 680, 80, 681, 981, 982, 983, 984, 985, 986, 682, 987, 683, 988, 684, 327, 685, 328, 686, 989, 687, 106, 990, 991, 11, 688, 689, 691, 696, 699, 992, 993, 700, 994, 701, 690, 329, 692, 107, 995, 996, 12, 997, 703, 694, 330, 998, 331, 999, 698, 1000, 1001, 174, 175, 1002, 176, 1003, 297, 702, 704, 1, 1004, 332, 1005, 1006, 108, 1007, 109, 1008, 333, 1009, 334, 1010, 81, 3, 4, 705, 706, 1011, 111, 82, 336, 1012, 338, 707, 1013, 9, 1014, 177, 708, 709, 1015, 1016, 710, 183, 311, 711, 712, 713, 714, 715, 716, 112, 314, 1017, 339, 110, 1018, 111, 1019, 112, 340, 717, 1020, 315, 341, 1021, 186, 1022, 1023, 718, 1024, 1025, 719, 720, 187, 83, 721, 191, 722, 342, 723, 84, 724, 192, 725, 726, 113, 727, 728, 729, 1026, 730, 731, 732, 1027, 733, 735, 13, 14, 737, 15, 1028, 738, 740, 739, 743, 1029, 747, 741, 16, 750, 17, 1030, 742, 744, 1031, 193, 756, 1032, 1033, 748, 751, 1034, 734, 343, 749, 752, 301, 753, 754, 1035, 1036, 1037, 736, 755, 757, 758, 2, 114, 85, 115, 759, 760, 762, 1038, 1039, 1040, 1041, 1042, 1043, 761, 764, 1044, 765, 766, 1045, 344, 86, 87, 767, 768, 88, 1046, 303, 116, 117, 0, 118, 119, 346, 771, 1047, 1048, 1049, 194, 772, 773, 774, 310, 770, 196, 1050, 316, 775, 777, 1051, 1052, 347, 776, 778, 779, 317, 780, 8, 197, 781, 9, 10, 782, 785, 786, 787, 789, 790, 791, 792, 793, 794, 1053, 795, 783, 1054, 798, 1055, 797, 1056, 120, 799, 800, 1057, 199, 129, 1058, 1059, 1060, 348, 1061, 1062, 1063, 349, 350, 801, 351, 1064, 803, 805, 1065, 121, 1066, 1067, 808, 1068, 18, 123, 352, 1069, 1070, 809, 810, 812, 11, 1071, 1072, 1073, 19, 354, 125, 1074, 813, 814, 1075, 355, 200, 204, 207, 2, 356, 357, 1076, 358, 1077, 1078, 359, 1079, 1080, 126, 1081, 128, 1082, 1083, 1084, 1085, 1086, 318, 209, 802, 1087, 319, 115, 361, 89, 320, 1088, 804, 815, 806, 816, 818, 819, 820, 327, 1089, 328, 821, 823, 1090, 116, 90, 824, 825, 131, 1091, 132, 1092, 1093, 1094, 1095, 136, 1096, 826, 827, 362, 1097, 1098, 1099, 1100, 1101, 1102, 5, 15, 1103, 1104, 1105, 828, 835, 1106, 1107, 829, 839, 841, 1108, 842, 843, 1109, 830, 844, 1110, 846, 363, 10, 850, 11, 12, 1111, 1112, 831, 832, 852, 20, 21, 211, 834, 1113, 215, 1114, 91, 836, 837, 838, 847, 854, 1115, 855, 856, 1116, 848, 849, 840, 1117, 1118, 1119, 321, 12, 216, 217, 1120, 851, 858, 13, 859, 332, 1121, 364, 366, 13, 1122, 14, 1123, 857, 1124, 862, 863, 860, 861, 864, 865, 220, 1125, 376, 866, 1126, 1127, 139, 1128, 867, 15, 1129, 22, 868, 140, 1130, 1131, 1132, 1133, 1134, 381, 869, 16, 1135, 393, 141, 1136, 1137, 1138, 1139, 1140, 397, 872, 1141, 398, 1142, 399, 416, 1143, 1144, 417, 1145, 1146, 1147, 142, 143, 7, 8, 870, 871, 873, 874, 418, 875, 329, 1148, 1149, 419, 333, 1150, 1151, 334, 876, 14, 877, 218, 878, 1152, 92, 1153, 1154, 219, 221, 222, 1155, 1156, 223, 93, 879, 880, 1157, 0, 224, 881, 882, 885, 887, 888, 889, 890, 891, 892, 1158, 893, 894, 895, 1159, 1160, 1161, 1162, 15, 897, 1163, 1164, 883, 884, 886, 1165, 1166, 1167, 901, 903, 906, 1168, 335, 225, 226, 896, 1169, 1170, 898, 899, 900, 902, 904, 336, 1171, 1172, 907, 1173, 908, 1174, 909, 1175, 1176, 420, 132, 1177, 1178, 23, 421, 1179, 1180, 1181, 1182, 422, 423, 905, 424, 1183, 1184, 911, 1185, 1186, 1187, 1188, 427, 428, 912, 429, 1189, 1190, 227, 134, 1191, 1192, 1193, 1194, 1195, 337, 339, 340, 1196, 94, 431, 432, 343, 915, 913, 914, 916, 917, 918, 919, 1197, 228, 229, 434, 433, 435, 230, 1198, 1199, 1200, 144, 920, 921, 1201, 1202, 922, 1203, 923, 1204, 1205, 924, 16, 926, 925, 927, 928, 1206, 929, 930, 1207, 931, 436, 1208, 1209, 345, 934, 437, 932, 937, 935, 938, 939, 438, 1210, 1211, 439, 455, 941, 456, 1212, 1213, 147, 1214, 942, 458, 943, 459, 1215, 1216, 148, 1217, 462, 945, 946, 947, 463, 1218, 346, 349, 1219, 948, 351, 354, 951, 464, 46, 1220, 149, 150, 1221, 1222, 467, 953, 1223, 1, 1, 954, 955, 956, 957, 958, 959, 960, 1224, 1225, 1226, 961, 962, 1227, 963, 358, 964, 1228, 966, 1229, 468, 1230, 1231, 151, 1232, 1233, 24, 1234, 152, 1235, 1236, 26, 135, 363, 365, 367, 469, 470, 967, 368, 1237, 154, 156, 157, 1238, 1239, 1240, 1241, 235, 158, 1242, 968, 1243, 969, 970, 971, 1244, 972, 973, 974, 976, 978, 979, 975, 238, 1245, 1246, 27, 471, 1247, 1248, 29, 472, 1249, 371, 1250, 377, 977, 1251, 1252, 1253, 239, 240, 980, 17, 241, 246, 981, 254, 1254, 982, 1255, 983, 986, 984, 473, 1256, 1257, 474, 475, 1258, 1259, 476, 477, 258, 285, 286, 478, 479, 287, 985, 987, 988, 291, 292, 1260, 480, 1261, 1262, 484, 1263, 378, 482, 483, 485, 1264, 1265, 990, 991, 992, 1266, 1267, 1268, 1269, 1270 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 0, 20, 7, 21, 22, 23, 5, 6, 2, 24, 0, 25, 26, 27, 28, 7, 18, 16, 29, 30, 0, 31, 22, 0, 32, 33, 34, 0, 18, 12, 16, 35, 36, 37, 38, 39, 33, 36, 40, 0, 41, 42, 27, 43, 33, 44, 37, 1, 44, 45, 15, 46, 40, 47, 48, 49, 50, 35, 51, 49, 52, 53, 5, 54, 55, 1, 56, 57, 58, 2, 59, 3, 60, 61, 62, 12, 41, 50, 63, 62, 63, 64, 65, 66, 67, 68, 69, 64, 70, 65, 66, 71, 72, 71, 73, 74, 5, 75, 0, 76, 45, 77, 78, 79, 73, 80, 5, 80, 0, 81, 82, 2, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 16, 85, 89, 95, 96, 97, 27, 95, 98, 99, 96, 100, 99, 6, 36, 2, 101, 0, 102, 49, 103, 101, 3, 104, 16, 103, 105, 106, 107, 108, 109, 0, 15, 110, 111, 112, 105, 113, 114, 115, 51, 116, 7, 117, 7, 118, 119, 120, 121, 122, 123, 124, 125, 0, 126, 1, 127, 49, 128, 129, 124, 130, 0, 131, 132, 0, 133, 134, 107, 135, 136, 137, 114, 2, 138, 117, 139, 140, 141, 6, 142, 3, 143, 120, 0, 64, 121, 144, 145, 125, 3, 9, 146, 37, 0, 147 };

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
            final int compressedBytes = 2961;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXb2PHLcV59KjA31wIPoQ2VfOGQqyAVTElQM1mVO+nDQ6CB" +
                "YSI0iTJilUpjJU8M5GcOpUqFC5UCHAnf+Eg6HCcKUmiKG/JvOx" +
                "szsfJH+PfOTuKR7AZ+29JfnI9/0e39z3N28//ODJUv30xvmdb9" +
                "558fDqk8dH1Vd//M/75sF3d81t8f39Gv718tMa/mgC/6GF1+M/" +
                "/Hr5l2cN/DcvHr765PGD0XgE586PxufGPzN+QizE5NHDD7nP9+" +
                "ont39747zmDyHufHN6cevqYPlAGPW7GolKXIfzvdmu7+bfm078" +
                "ktDnzf1//O3DJ8u/PvvZl49e/OHln1/9+vHf7/378//W8DdfXO" +
                "6A/wn0K86Xx88a+i0ubr06WB6N6Ldn/LLL/77xywwXShSi+bl+" +
                "6g9arv9dNfphz/pRDpVVNfhZP3IH+mvf+nvf/MHmr+ljxh/Z5/" +
                "O222/m+LH8Fr19r4b23Wu/tv6bE56VvxPQD+0vJ32OhLihtwpJ" +
                "Cv2Zrn+ahgQk/UTxT560+N3Jsb8W5d5JPKj/Oz77ZSellZRKZJ" +
                "ePtX/R+YeL1j8c+Rdv7v987R+dP1pt/KN2PM0/GsQnq27/cqf8" +
                "y5Xvuf8+eRaFz79HcOSfI/i3HbzzD09b/3ADlwngMH4A58udP2" +
                "T8qht/LwQ/9HD1G9d/78e75HNX67vGD+GrDi6HcOTfcuFj/F+2" +
                "+N9b4//FbH9zOHf+UPzFAC7s+xNJ17c5fRU9/4Dsg1CVwI+B39" +
                "CO36P4Y3Sytgf4Z/uG2+jrjg+TrC/i8fPad+jfxth/5N+tDbQU" +
                "5UYCRqyExueGDw99zbJc/ETI+mz/dTDeRr/DQ3HrSNXsc+O8Xr" +
                "+sxMf/0tVXp08X5sHzuz1KY/+2HPq3Nfw9n/+L4AT/mjX/4Z+c" +
                "+7u8u/bffPqRq1+H8FUHH9vXUFU7g1P0t2e8FLzxnf0ofF8sHC" +
                "ai8Op/0+u3MJs0wy9mfu0nxMJi+bSS42n7f1RGFI166xZSRhw3" +
                "/1+2Ck+vz6/colJMkFc9hVSPto40xVH8Jettad3sWHby9byRf1" +
                "1/66qVL4L9Jj8yaLyhwr3nh/fHlC+y/Co7/db4lzP8SyIczM+U" +
                "H3h+I7aUEfolifwXbv60yV8v/yp8/8c2/rHJdz8/Wh/yX4VkSo" +
                "8+nK2VU2Wnhkksf6EqCq1vovi3iMW/pq8M13863j6ZwPHk+ZUL" +
                "7tcfAshPMvxcXwT8P5cPPQrRlWt9YzfnM3tUhYlfsPz456fbJ6" +
                "f94MkPhPf+j7D7P8zzIeR3C05+l5sfPdE/yFo6io/0gVz8SogT" +
                "Yxai1KUSq1KcRvoPFV1j7qi+cAzim2NXfLMr/Fz5zfb8STzt8E" +
                "+Y/t91y29O85cR40XY+IqiX+KjCEgfPUZ7op+C8Q+k/7fD/MBE" +
                "v/T1Gx8c5w8qWP/g3C9A+2fnn2lRyYTrNBnOz88G0n9m9Pz2Ed" +
                "8vqojsZtdf2H4G5zdFYP5U+PK7lvyvLU3Uw6Vl/F7hCfLf3vOP" +
                "oF9gfjCKv+n+MTs+Sml/ffpvKD+KvD4j/9zm11H+3QIXk/w8yo" +
                "978/fc/B2hPpAVHo7fZ7te/z1O/QTJP6pvWOBiCGfXb1LZ//qU" +
                "tdX/FiB/LqB/4qtvNvmPRsJP6hln+Y8Dkn6T4fp1oOLV2VqHSz" +
                "mMRRR1PIAT7j9GPiYOP259y2TGj+aMRtBHRq4fh99m/0shjVis" +
                "fnHxz/pXn5sjs3i20iv1+2VZmIMdrG+pmlUJ6Qfz72C1Tf6mHm" +
                "zL35zo78Zw0cHFFK7scCE+7aS7wU81h2B6/6fcBgJyw1Fb/UbU" +
                "f2j+Zv+Fb//+/SH84P4B/ji/Rjz/Cf0EnX7t+XxU7zEGf7T/fn" +
                "45m9+Q8Ovp56Ivlz+34/37z0P/mP2LufwUWH6wfB0C/GSUfCP9" +
                "QrZ/OrJ+JmD84r2/zq8PIG1/xbOPy25g7b+ZwtR630itmuNd1M" +
                "f/gaU+YgI3BMdfAf+uGpy0LQWL7CvzfMTr8fiZf+lNHhD8Q6b/" +
                "oPwLQft+iezjGSD3WRj/hsJh/Y3rf3H9s33PD/gb1a+hfFDlJw" +
                "6O+BPnn1D9t19Nbu9viGl+1UKPqiDpXy5+zT6b+LXsguUBPmUg" +
                "XMbBN/cXXPE7Wr9af1t64QH8qcPOB53v6/H4jf6m7p+b/83MPz" +
                "C+qRy1Wp/+3Z3+pN3/wvkj5/2bZZufWp2IOn4X75Y1/Kmp/Ztq" +
                "6aNvgH6G+d/KWpRJBxcZ5tdu+I75I7d9x/WhfeCfUH/A+6sx6w" +
                "fSVyfUX9eMfwICOUK4VFrlW7XydzjWb4VZLHYaX8TOT6WPK77a" +
                "Ff3t6+P8KIhPLsVFaeXvVY/H+y2oaKhZVg0LVOJUL4y+PKbh3/" +
                "o34/sRo/MDcOGYf0fxDcg/qNH10tLS1EmNLyUtviQ/VPtPmgzZ" +
                "30n8punjof7Z1M98+Y0C0L/A/r0rvvfvD94PheeXxL8vovUjhm" +
                "e1f6j/0ZU2CvcvRvjJVP5NgP2UefyXzPFTdvsalq4Nvf9DeL9X" +
                "1v3x80O8+mf+/BCiX+B45P8bl/1xjMf67cKv3yb3O8n6ce0/Jb" +
                "jfAvjXjPJba/uh2vU9+MlufdONUc2gp9v7cVXA+qn8e8mEp9E/" +
                "s2gnVXwbal97/kHjufLNk1/UP4Lvd1Z8/iffP7Dc/8f5S/R+GE" +
                "L/dwD9DOd8bPvTfv+D4v8NI/+pfoGzkPjH8/4aS3/ruj7SPKi/" +
                "gqK/fPNz+RPhx7UPae3/yzj9Fc4fVfr8oWN81P3I2f11Tv+Cf3" +
                "52f6xw+EeGhB+3f4QwnvX+H0RfQv/Fx13+anaYl93HSo78O3t+" +
                "ygfPmn/C9WEu/+e1z9z+CXp/iArHP8H9p/T9C4H9G6nvrwT6l+" +
                "h2PKI/pu89Hn5WRLUle2iHn+jnbX6vFF1+r9zk98rJ/b64/kV6" +
                "ftc/Pv79MYJ/fqT4WwH5cty/Id9flAHzB8BB/Zpg/7zjIRytz4" +
                "3PQf4jwfwsOMF/uwD5lwz+Ybr9vT1wFWc/yPrhx36+PP2fbfy+" +
                "8efWp/ZPX579fcvh+fsTmfCA/SkLgPv++1n/yLy/hpU/dvXfKP" +
                "f8I/2M+3f8/rO//4ZUHzFM/yRUP+gg/TLRbgtHfOGFDx4X3DWe" +
                "1N/X9kdNz3+8Pwx39FdZvWevfnecr70/6/rA1Z7H/z/C6fKP+/" +
                "+c/Bvqf06Ej9DfS9pf3P65/Ztc+U9nnw4d9ulynV8p2vnLdv5m" +
                "801+Rcfkz1z9jVH1Lzg/iI8SxM9E+4XiMyI82n9H+Z3I/BDRv8" +
                "bvz86Uv6K9XzXf/kW/mrN/KnV+L5D+vPpI3P2khPjB8a+58a1X" +
                "P6ajnwuuLfnTYX0Lwf1hFzE/6jfBghJfePzzwq3+MX+dMesn4k" +
                "L57P90fkv87MCvj5/lXuPz/PPz4r8NwWRUfpN+f0oGnI/t/XiR" +
                "9Yl9wSva+dD9H4f95sKx/iDab17+1d1/TFJ+IfbJ0Z9sXx/z92" +
                "t/fzPXvoHzvRZ/n7egyEdcfpGfH63882P54/WfJKsfZYJz/W9u" +
                "/IPhueM7Yv2Be/7c+s+++cOhf5LVL1z7rzYaJk4/+McPM/Rx8g" +
                "vmx/3/rPp8Gv1Zbqea/P0t9nh2/wxTfzPv/1n/ftDgfmGS9w8U" +
                "of7xWxSfoLKjtT9eN/3xjgqRNT5wwzfzT/T7oP/esT4NTlt/rl" +
                "8LkWZ/bDj2X1j7JwdQrPuB+e6fKZ59SCCfyD544VLd6/ojtZS2" +
                "v5+E398D14/ILyH6JuUPP/5IPwlqfiIlXAS83yEQfi329yOCh/" +
                "Nf2PsRcXwUtr5VHKWPNcH7T/Yd/3Pz/1T4jviLK7+TX+P+opjz" +
                "DYl/qfWTXPUb5nhCfN2kGMpqK3+NOSzb/nIjZu9vCc2fov5omO" +
                "LRFv6gvF8oFVz71QJ6fw6qT4D6EPH9NNH5V7Z/Sn7/kFc+CpZ+" +
                "JOV35fXM32bun2e/vyKq/itEqr8vnLv+SL0fGq3f/wdL0+MQ");
            
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
            final int compressedBytes = 2810;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXb2PXDUQ9zMvkXMCYU5JSPlAV2yRAipQqpdAUKAhijgJOv" +
                "4ASipE4TulCB0FRcpVCqR0+RMOKUWUigaJP4f3sbe7763t34zH" +
                "3rugvCYfs7bHY3u+PDNWav1p5flef3t0fPP54sH1Kyc/vXjn2f" +
                "HZZ78eto+//ucD9+jfO+5IKaOi3+v3j44/fL74/o++/RfPjl9+" +
                "9uujVftXffvScM9nOfj5yWM93SL46nM8+vDw/xPQZxeO1q+D12" +
                "r9q/p8/DZh/jo4Rp2DvgG46Shea9WspmCcutX/uej+Z/gdnr+/" +
                "vW5meIzjVz78ah/ceYergvgfDP+8NoxvO/yXxPHb1fihz57/ob" +
                "f3p5l2XDfq6qzNkro+q/Y61D7eP1o/af+rL9h+Rn+zS3/Qv53C" +
                "3Qg3zPmrnfkzzpeIfyH80f5qFetz3PYAblrPhreaPv75mSIS3H" +
                "HnlwteiP+pqo7JH9y+FekHpP3rlR8uC33O3jv68srJwlxX6vaL" +
                "u6c3zq4uHiln7tt+YgN+bdn5E/CrTxa3BvyqAb9DDn5Z9Ks6wn" +
                "4m+sOGodDZFwf/Ub+5t8LvF8r6IPwA/eH48HzYKQFc6Fil8e/V" +
                "/jDh/aGIDE8z+I+gPb9/vVf8UvhXQf0WrS/U/xPOF4c/Y/qI5c" +
                "MHw2Gpd5jFEyC+HU3QtsAEQ3CSINcDOxk3i52Jtmo+0kq6ucn/" +
                "2iB/S/wczf5H6wfbpyhCVginkCWxf0ehrRx/cnujTv3yaxk25e" +
                "P+C0uyjzf6pYuPD9gtWKhDpa5sSVCt7He9Jev6eenk/W/p/F96" +
                "PsT6Vwukmn7XnvOQ3oS79fCTcXO0WhsM99hPgYPkp8/BN+rGoe" +
                "l+dOWk+0nTqk9/tu3ju79X7tGTO2N7zdf/bGE4x/8p0/+Lr09h" +
                "+4OgXwvtz5Z3Vtn6s/Xof5atfzc7+7fJ5H8ZKVDt9LfaD2T/Sq" +
                "h/h+zjqPzQpmO5tt+f+nx/dojartOzcf8CePnznct/rwvjR5Pv" +
                "VYr8IvlHaP1XyfQr3b8O9H/Z908KPKd/113C+b2F7w3e9DugWl" +
                "47/bH7yw+uY9WPl3Zpvlqo2mWRL6n+27fr8/+QX2z7km6S7cN/" +
                "A/TTj+y/utu/9cedclN93v3buU5Vs41Ry0bd3d7faKI60P+raf" +
                "9q7F+t+r9oONLvYXv1YKBPb7r3XVQDXQb9pFmvzwSuJnDcf6p+" +
                "RLNf6PTTpdZnwD/Uv1KvAH3j9C+N/+768sbPcj4F/FPa/0f26d" +
                "C+UVd1374htRfcb7yF7xmewb6oJfq7VL+/6PZYfx3tvsbVrvcr" +
                "aGX649MLipvb/jED3OYXZR8J+zdnsvkh+izGhpVajPR12k7oS9" +
                "af/P0nxD9m1d+U+lvo/yP4x3VG/+Zu/6e0+xWdSB/gH4X3O8z9" +
                "4fsi8SXIv0jwb4PvIQ/uKPhz+AMan+d/CwO5+kkz6Cdy+r7p9m" +
                "8JuCLbH/74y5z4nQH6Ifi5S7YaB2vmDmgifraQ/8GEjogDbIlm" +
                "v/d8SHnjk5tM8JkitP3txX93Fohfbpj9h/AH88fyPRC/3Aj4A0" +
                "d+X27+JY2/lcfvuguGC/FrBgovK/Vj949r/aZyrhOP7YJ6/jLg" +
                "f6ntw4vuXwqX2hcE+yAKb4MeZNq+SuEvpfmXysa/SvO/MvFZm/" +
                "HbPn73r8Z0C3y7qdyNs6vmsPvv+8MvCO2Lw3F+3LlmOt129ZxD" +
                "6aB+ZQb4uv04eO2qagI/2ILbDZwmf2sQ/xCE9/FfgfXdjl+Nza" +
                "9w/NNltF9S4j8E/teI/MPxRxT+5ZlPW9P8L2T/TIg+a/tHb+uv" +
                "E3jN9S8zxIbvNMOPYj9l2l/Y/xs/f8C/aSZL0myj40iEJPDPi7" +
                "Qf2PF5juqfWG74Wxb/Z9D/KOSfVrY+ZsSo2dDHjv+9jJwfC85X" +
                "djjzfDPyR1f5y2s6uQuZ30XSbzr/3fYIPt1Oq/3TFMKf3T/1ft" +
                "2O/mH+/bovfptxv2DQ+kj7b6eEcGz/LYU/1UD+Mdefxb8o8Dos" +
                "31tA8Az+p5qi/0n109T7sQz6cWR+6/sXO8aXjPcvan3/AvU7uX" +
                "8iIf9DUB9HUfLL7Lb8ofGXUH0TaJ+1Cfhltu9E9Vuo6xuoLwP4" +
                "I4n/x+rfSPPrsf4my09E9X+g/gfy+yn1XeL1FeLtifBIfZt4/g" +
                "jkDxn067j9HK//I85PRfUZcs3vwDt+Wv69msjXm78N+Yu3Z/mL" +
                "a/9lBzee/Eaaf9Obf7hdQkOqH5HslwT7FK1/TL5J5SPi7xn927" +
                "h+H1H/5ezfmXyO+OdQfR2pfkhqH5simD+h/ouw/g2T/4ThjPoy" +
                "DPxg/nEG/25B/DB/jdM3LX5x3lGz+Ws9Y46YP9ZqUz9Ez+uHpP" +
                "FXaX2KSwXPRJ80+4pQ/wTVr0H75zSpvgbU3xLvJ1Tq/QQS5KXv" +
                "r0P6hUz+1lOStVtQRzGBEX+B9SGE9SdK3z9M8Ws8v/XBGetrlG" +
                "j+pesflV6/vcR3CeKbLzt+peEovhrOX7g/y8QfMORfUH6t7bNT" +
                "cP8EGHkp+4jmf4TxQ8L66ttw4wGQ6q/VEv9FS9ZfTcL+gPmV0D" +
                "+5zj/V2/mjIf9kPv/UdH/oif7EyB8F/iGyfzbcf/RLp//M/2GB" +
                "/lQIvr/6/tL40VD+HshPg/r73178TC76gECMLPlB9dz+YNTPh/" +
                "YLguepD1YuPieefyamfyd/TfR8C/PTuP3v+nd1Sf0EwrPpn0H+" +
                "IPRfM/3nb55/J77dhPEPBP0J5LfJ9NN9x+dy/S9y+Qrys7B8i7" +
                "ZH+OH6/oXtC2H+mNi+kK4vyk9Z22+B+6HU+/fp/VSdSn85f6eu" +
                "nx+/If/jZJL/0S2EXed/+OGb/BDp/Ij+0+T8CZy/EW8vzr+Q+q" +
                "ek+4d8/pH94Nfftbk36odW64EFPt3Nb4h6TZF++SSuX6TlV3D0" +
                "b9S+1fD+Ia5fofro3v7p96cy+Szl33t5f4dzv+LhD6L7N+H9HK" +
                "JPvvryafYzsj+L+8+l9WGk+4dyv1nH/Su9idi0m/b9z5shfs8R" +
                "4rvx/or2jzYixG+nvY17SLw/CcJx/ObDqXxlry81/oSaoFbkfi" +
                "k9fp4Wnx+cn/j9qFl8Ub0VX9TmuR8qEP/Nsa9L4Kfy7p+64P0c" +
                "Rb5L3l8sQj8mnFGfPrd/gnD+isaPk+J7BfHzc/pUXP9icHzq/p" +
                "Ppp+cf/31W//wj8inkX85y/xc5ANL3a4ntg3BZfD9z/4T136Cs" +
                "R+/nCvljmv7C8M/LDJx90EdkHxa2L4vsP5b9iN8/F8Wfg/d1Kf" +
                "YT8m/Flkj4fhTSP3H/Qv6Lz6dofvj9VGl7on5jeWTMdj8E6Iff" +
                "p0y5P0HrZ3fbs96HSPE/BfyTYv+V2H8pzW8S+Tehf8y77G/O/T" +
                "KK38bv83H4T4NtpRnaBwfB9xWf3qG0R+8zIrMLxmfL/NvS+Oi0" +
                "+tj09qX9r/uOb67InNAV6b9i24dU/xNH/uyzfiHnfEX6t2njp/" +
                "mnstYXdJL69Yz3f5IMAHp8ftB+i8fvZpsf730a+vs5svFJ9K8F" +
                "7YX7JxQfm0+/g/XLJvLFU39D9r7DfHy1Mz6YKDrfhPcPpPar1D" +
                "5Wcf98NL4kR/0AaX6/uL5RpH6lOH4xk/xiG7s0+02aX5MhP0fK" +
                "n7z+PXr9e1l9/jLxb7ONwn4fURp/sul/5Z+59cfKP/MS+2dsyr" +
                "HAcMsm26Rdav2n9QoE/f+y+rbZ/LN7sv8dr36d1H6Ujy/T79nn" +
                "25VaH//5pfuXiPd7QW0nUX+D/aeMX6C+qRHCQ/3/By3EMCU=");
            
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
            final int compressedBytes = 1508;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWz1vHDcQnWVWB1pAEEawYpWrQMUVblI5cLVKbCBJE0OwAP" +
                "+IlKkCF5TgwmUKFy4PKgK4809Q4cJwlSaAf4734053u7fkG3JI" +
                "nZAt/KHxDIfDIefNI03UfSUNPkOjTyB/tpSr/q92JNcEvnqov2" +
                "V/9eehXK/tWyoVVcufaEtH7e/z5ie9LtRn+ueYP3v8pQN2PGy9" +
                "ZX04/3rCp7X8+uuTJ3sXc32f6OH708vD69n8jKx+alpFe7Khoy" +
                "Znp7amuh6wlX365uT8wbv5izd7F3+8//nq/MOjl2f1q1//+9ae" +
                "fXzc2JfKp+e//qC+rn3BY6xPjde/JManAtYvxL9V9vX2i6n96Z" +
                "EP4/dPF7+flvH7q4u/Pz+n9IfxX/m/v+G/cfo/sX+433QCr2yZ" +
                "kP0VEP/A9SGwPhPnWysoK5oNzk9NC9dZ7FmfK8/6OOcnjD/aH2" +
                "B98PmJ4uOVs84f9JXu6R+bt6qRl5WZUfEjUWVt0bheaaoqOmWE" +
                "Dx4RNac+efJbpt/Wl/JifvSmrS/F5eGH2fxgUF90Fx29ESqjps" +
                "uJa//59WF+gvoq1F/Ov6+vRVdfB/NH8t5QtbZZjnYx8k+zslMt" +
                "h2jmUJqg7euMiU0yfrkqSRuZsLRuBz81FPhZ3vx5+I4A/nXHZw" +
                "iXgvFbwvF5+nYLH11On7+LaTtjfen8NEyuYmzMjBIA5AeIH8Jv" +
                "frP7+3R4oJtJ7F00JqqafvjT1K9O/y7s2dvHHLek+x/I939z+v" +
                "ea4x+un9z4RePjSx8+YOAfR6j6v336/eT8u3fzX+63+l9dnV8/" +
                "enmw1P+87i/c5zfUByl1bF73+KH5pcMP1OMH6vCDYa5/Av8c63" +
                "NsPnf+fU89vjnu8I1p8M2ixzcp8LN3/I/D8akfn5bjM/zzfgns" +
                "i+ZH9K+Qnxjp25E+B99ufIUT/9Kov7RMfRO5vxeB/Uns+cPo33" +
                "gASonE8fjLa5+//12FQIpfuPjEuT8c/V3FjM/1UP/GfsWyn7/+" +
                "EcJHsvNVo+AXQRu2iMlvT/+stCJjWrONvF2Co2eN48a266b0oD" +
                "/w42unHPnv1z9okNNGh9g4+7z9l7bVU9H9RYgcnp+Z9UcVIDG/" +
                "f1NfzUxN1lcHf++7neDcH7D5fZC/cn7ZdT5Y1vmTnd+F/DPMLy" +
                "Y+8hwgfv5Uxi9Gyxd+uWL2J5z7Bc78o/d3Xv4W5mfm+DPm5+WP" +
                "Ib8nvt+R8rt+/hTfD8r4Xzk/jO5HkTwcUiXlr1Lxd0H8p0nHfw" +
                "rxCwMfie6foP3M+O/2+jdH/yW/H0jTvrrxj/cL7Z+KYP5ftj9D" +
                "+mMdFT+Zf4h/QvwVxsd+uXT8eP5s4/7B6z/gzxA/BsZn8MsD/u" +
                "rezfuHUP4K8Weh/ofyr7H3S7L7HbT/8P4E/JCQ/2HlJyr+peT8" +
                "rb39K+JP2PxKPP5RLH4mkv9D9T/k/scPDJ37S6rv5bch/8HDT6" +
                "V3/5bMrKuHC6bSni9x74d23L/j/rHO+j6Q0T+j/Ax4H3UX+/Pd" +
                "xjf3+9oE/bWsP5C/L5LJpf0Rlz8xsfOrRe+zb69+RuK37P3j5v" +
                "gVsz9SAfcjqD+tSfI+Ivj+1HLyj/++QVr/4u2P8a3z/basPsDz" +
                "W9gfcewL+mOcH2j/gv5EHD///qm7+lLpZooPq8I29U03kJuedh" +
                "am64sBx3KC94Eh65tq/WPlMvyK7+/uNj7Kf/+I3v//3+cfcz9k" +
                "MstvET+mfr89Op/Y9wus+FTA12orF8Xvh4Ec2cfvg2X5x8JPrP" +
                "3rSYVSeL57zgfIXwvtM95/ivCf9P2v/H2c/34N7z8Xv8WMD9CX" +
                "1t+d1wch/3bX/YP8ZG5+AvIHbP4hKv9z1wcoz/z/T+LOpyD/Wf" +
                "mnBv2lSaYvvr+Nej8Ucj8qrZ/S+zcGfvPcn4n5613fT+QeX86/" +
                "8s7HWH5x5+9b/P7D/58F8hPJ+e83Ur4/Nun04f4G+O2u8ye3MX" +
                "7W/I7J//T+OesrlMvqg7h+ZMf/Mvtx53M6OeQXIH7Ny19J10c6" +
                "Pzj/L1TPm18=");
            
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
            final int compressedBytes = 4842;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWwmQHNV5HrATyRiQcUQgCjaJY3MEL4FUwmWOp5kdaS2wnc" +
                "Q4MYnCIaSYS0jYOkASq+l+PTO7s5IlFxKSjJCsY+1ghRh8pCpJ" +
                "mWAFTOICE3AZ0GECCAnFi00gpCqplCv5j/f3/7/Xs1T21fv7P7" +
                "/v76ee3rfdo+ZEc2J0fhNkdqo/dWQvajh8OxtrTnSfKy6EyCPs" +
                "yy6XaPZ9lKPvpcwjzYn8N2rwA8cltVr2BnlPyp8JuZf5U7I97a" +
                "I5gRk4ir9B2Zo5Nq0WfgQXNbXYM3OitVli9sh6rdaaoXXZ10XP" +
                "Z9kskf4cYfOvotb5pv8QWYctv2Xw0/3JeHRDbsj/PknnhkYfQ4" +
                "2sOfldbqjY768UjxvKBsvoH6Mc65F+hOQn/PluqN31v+f3kv07" +
                "xYGQe6G/aLTIf6Q4xT4+jo35pr/Kf8r/UYl7NczPaCZY5808mo" +
                "2SdmnwXGaiV/iZdGzA/Lgb6gKS/yTMP8yfNVm/S/ICN9T5K3+J" +
                "v5isV/3H/OXdFf4ssg6b7Fl+trH+wH+atJ1up/8ayT21Wu/zqJ" +
                "G1Kx9zO9sf8F8Xj9tZq6muNqwTSvI0ny+9O7rfCkgP+b+s1QaP" +
                "0zpYJ2EZ9w/6v1Zc1Py3Y5buA1nPssddsEV1D/CR9Ictn98tOd" +
                "3FeLWQj66n0UF/LlmHFU0yQu03/DdJy10OfpKUkfOAK/dJl498" +
                "Wj2Sl9pW1m9Ue/QZyfJv1mrttta1/0cRUtYqy8i52RrLHseVPZ" +
                "7YSb8cYRNmf02Kbftxuf+Zf4O0W92t4A+SNbbyo+7WziH1xFGU" +
                "/hZbjcdiEx/RHn1WcnGdOmcpTud+iZTrZHAtI1ojF2VftOxxR3" +
                "HvOot7077ZrxVhnf58MkwetE6orXFriveQnFGcBhlrePh2/m9u" +
                "zchXi5PE49Zo1Nosi19BWZ9X2lN62zireG9xPKxTQ+u671IE/C" +
                "l+Vezi16osIy9muyx78T4bLX5dYsUJfCT9ZOykzJlK8jjh44pi" +
                "WvF+WKfnLDbX2w6K6cUppC1yi+D+hHKPWwQZi3jA/WkCjuNwf1" +
                "okQ6OsWenxs72ofj2xoL3DjQckuD9le2ZeqDgSgS7l/mRw4f60" +
                "yHKOnsf5kqXZqGsPcH+iI+kP16/XLLg/Sd24VoQ1G58MM9Ti/Q" +
                "m1m93N/i6Sq2F61Mhq+xnu5pFv+JvEA74NogESyPZbqsN+4EnI" +
                "2AjnvpTt3n+EqjF/N9yflihO93hB8bnP/HpGECy/QjPRU38mO6" +
                "w8EB+xUfb6TqjtwlwFc137CzZLJDCuDBVLfEGfu30W22LG1uAZ" +
                "g2fAbyOS+IMaW/UZg2f0ZqhH8lLbys6w2r23JYt2KnO0rnu2Iq" +
                "SsVZbRm7Kjlj2OK3s8sftqTr5d2IS5ODbFtv2o5dquDStGktau" +
                "zQOYPuDavYZ6JC+1rezssGiShT8zL9K60TFFSFmrLKMv5sdY9j" +
                "iu7PHE7qs5vE6WuTg9xbb9GGuz2wwaSfJt5gFX5Oluc6+pHslL" +
                "bSs7b1o0yaLr6UqtG12nCClrlaX+L/mxlj2OK3s8/Qf75YR1Ms" +
                "zF2Sm27cdY17nrQAuSNbbqp7nrevPVE0erEo8jl/FRbNHgevqZ" +
                "4ow+aCOamfYQ+ng6P9Wyxx3FvevE7qvdhnUy3MW5k2Em1kK3EL" +
                "QgWWOrPtMt7D2knjhalXgc4d1HaYsG6/RzxRn9WxvRzLQH9vT2" +
                "5r9p2eOO4t51YvfVbpWtXKePTYYZW629rX/0fwZ/TX6J/wLMPt" +
                "h6Olvbeg6u3N9uvdB7EiIHYMJ+M/so7D1fAf0t+uvzbZL/3frf" +
                "/KXsXVQ5kE0ZXZudC9rx2fvzl2vRz8zXKf+l1sutV5r/2nq1dR" +
                "SsezWeHZu9u7gCjseRdWJ2cnZK69HW97Jzek/kH+ac1jOtHwXt" +
                "eZg/yQ+1NuDfwa2fQrd/Ctp/KZ4/O3sPIU2n/CdaPwD5rMY7V4" +
                "V1ulx9rTdFa/8S2RuzYwjjhOx9bovbAitGktZuCw9gGnBbej9U" +
                "j+TpgP24qRapaFJFfc3Vut4URUxZqyxjx+SXW/Y4HvPq9B/tny" +
                "Ns5fX0mRTb9mOspmvC/gnlapgeNbLazX2u2ZsN+6dm6dsgGtSC" +
                "hP1TqdP+qRn2T2SzH3y0f0I0Gb2pglLunwwu7J+aOmq1sdPy2c" +
                "oD8REbZS/sn7i2C3MVzHWWTzokxpWhIuyfis9a7LjzyNrhdoBG" +
                "knw7eMAnfJrb0TukHsnTAdeTqRapaFJF19MXta77mCKmrFWW+g" +
                "/zIcsex2Nendh9vxxhK6+nPMW2/ahFz6Lugs/i4341rLZHu30b" +
                "Pqern4B3LH9TcyIbbk4UN4NvQ+v11s/luVdxa/stPOYvoSe7K3" +
                "sSMuh6Ii/cn4qb6E5B11PngWK+PP/qTePndJBP11O2qvULeqL3" +
                "y/QsbUWxMH4Sl19b3NJ6KlvJth9p/RiqDwLHIai5E5/T+U6xlG" +
                "q7EF9VfM6vw+6LxdlJIBdky/UZnF+ZLS0+Xywpr6d1rSPFbXQ3" +
                "+neMF38R1u/24gvZLfrUztVdHVYsSNbY8p9FqZ44WpVcLxVaGa" +
                "6nvYrTm2sjliPuQXLy+ZY97ijuXaf/k7Tvan+0Hg9NhplYy9wy" +
                "0EiSbxkPWMXF7BOP5OmAz52pFqloUoU/je1a1/ucIqasVRZYpw" +
                "WWPY7HvDqx+345wlau03dT7PScg7XRwWeFJfk28oB/kevYJx7J" +
                "S+1UKppk0fX0ttb1vqUIKWs/lnypZY/jMa9Of23/HGEr1+mxFD" +
                "s952B1HXymWZKvywOYrmefeCRPB1xPplqkokkV/nTnaF3v24qY" +
                "slZZYJ2WWfY4HvPqLIb75whbuU6rU+z0nNmy7yPiNx/5GrmLPV" +
                "p50xHeWdxi31HYo31rEZ4IPK31vf18H9f70zu9b4FO1v5/37fo" +
                "RL7q+5a4P9pRvjvFjt+3lPfx6W564xyWcBchjS3UrUfycMAamz" +
                "r2oE8x2BakGGfsPEHBGEYtrs3knHyrcsZxYeFe+cgzzhKJaFLR" +
                "oHdU0q1maufRedwBY38pWWMLdL/AeEwUauO6OwgPdJDWDkji5T" +
                "G2vUTZH6IW1zJSTn6/4YziJct+U7tfuo+7RYloZcV+qt4fYced" +
                "W2uumwtakKyx5W91c/Md6omjKOH+ZKq5no+o5bsllz53TylO/h" +
                "UbkYpqD5KT77LscUdx7zotn+aE5wWGOx+fDDOxXug/IFrK2De5" +
                "3c/jXsgfSr1juyQ3rNIL78ySP/xO8dYM9TFm/z44rpnh/nRNih" +
                "l3VFpr3VrQSJJvLQ/o7qvsE4/kpXYqFU2y6PfdlVo39neKkLL2" +
                "Y8m/ZtnjeMyrs31x/xxhK9fpkhQ7Pedg3Q5jvJSssTVeSvUFDW" +
                "rjutsJ73Z8nyF2mT2u3hiHvBy1uDZTcpQzigdW8pW1lb45J8TH" +
                "y4rx8L7FYlvM2NrtdoNGkny7ecBvgrpI9amOg2MsBcOiSRX9Xj" +
                "E4Y48oIkYsaz+W/O8tu0VKeWU26tW+OSZ9yfVku9ccWxusFW4F" +
                "aCTJt4IH7BpedCvyx9UjeamdStGwXnG7V2sd3MdX2Jhl7ceSf9" +
                "+yx/GYV6ewxznhPm6Y8ydSbNuPWvSX/1jRbX3J7sFwNmbJ8wLd" +
                "vbVe150YZzQnOM9W2n1c+e82y+48eZ/JzzM1UoxW93mIn//A9o" +
                "b5ZT8bZJ9p97mYo13FvfXvS58X2J/WRrPP3OQ21V9lWauxxhbq" +
                "1iN5qc0SVj3oYrOHkWIciXAMozZSZcmfslVxXHmllmeVDyX2IR" +
                "V1+saK7V5ytB9jNRy+owuSNbb8fa5B+6eG+mItllzPR9Rg/9SQ" +
                "CPxeWa448LkzEamo9iA5+fOWPe4o7l1ne1naN8rwuTPc+QuTYS" +
                "bWVhgHWIKHNfaB7hcaz9ZIN3UooVb0YAcPIUXIW8sIsxxQm7QK" +
                "S/5WVBXFlbesPSDdxzkosY+y4gD1aLsPOaY/tVowDrIED2vsAz" +
                "3ytCLd1KGETNGDHTyEFOOUEWY5qDZpFZZ8XlQVxZW3rD0o3cc5" +
                "KLGPsuIg9Wi7DzmmP7XmuXmgBckaW/63UKonjlYl10uFVoa/W9" +
                "pVHPO5MxHLyDn+w3FVHLV1Oqt81f7oHn3hZJiJdbe7u/6fLOGu" +
                "RhpbqFuP5KU2S0ALutjsYaQYRyIcw6iNVFn8R2xVHFdeqeVZ5U" +
                "OJfUgFatKXYHOO9mOsK9wVoAXJGlv+DJTqiaNVyfVSoZXhPt6q" +
                "4pjryUQsI+f4M+OqOGrrdLZXp3zV/uh6umgyzMS6091Zf4MlrC" +
                "5pbKHu56lH8lKbJaAFXWz2MJJFllyJYdRGqiz+RlsVx5VXanlW" +
                "+VDm24WNeaUvwcajdh6dx73u3vorLKGKNLZQtx7JS22WgBZ0sd" +
                "nDSDGORDiGURupssA6mao4rrxSy7PKhxL7kArUpC/B5hztx1iL" +
                "3eLGpSJZYwt12K+WHrRiTescf9+YpF+qNmuNS/vjoBdHjGszOc" +
                "dvsVUxknYitTyrfNyfsPklqElf2oHtPLLWw3iNJXhYYx/o/k7j" +
                "WR/ppg4l1Ioe7OAhpAh5fRlhltfUJq3C4r8cVUVx5S1rX5Pu4x" +
                "yU2EdZ8Rr1aLsPOaY/tVa5Vf4ukvQ+GDWy2g3nVsF6O/GgpToO" +
                "jrHkGErRJBtQ754MB98HN5zaqPkVKYvfaqssknh9J9R2YVJOlY" +
                "97o+8XcFf0Pth2Lzn2PEtrpVtZf5klfFpJYwt165G81GYJaEEX" +
                "mz2MFONIhGMYtZEqi7/fVsVx5ZVanlU+lNiHVNTpm0e2e8nRfo" +
                "y1xC0BjST5lvCAlR5in3gkTwdmiBQMiyZV9O821A+HI9bux1Kc" +
                "Zav8iI3GvDIbQ1U+jklf5fOCIYstObaWrfasbHNjIL+k9XhjIN" +
                "vSGGhOtD9F9QONgVptrEAJfy1e3L6yOdEYwO9hZJvCjugqjqGU" +
                "/y+FOuA0Q8bH+XsY7K31+UEWjLR+gdZYG5BObwy0P2nf7DUG/I" +
                "2Q8ZTWtH4M9sHy6ccMRqdnLgMymS87KX9OkaRbPT9COyLPVajn" +
                "2ea7a7eYHd4NMPaVkjW2QPdrjMdEK3U3BKx9IK0dkMQbVd9AXo" +
                "7aiGWknOI7tipG0k7K2n3SfcyHEtHKin1UvS/Cjju31mw3u3G+" +
                "SNbYQh3Wu/TYKNRGdY7+FVCXCrZZU6+t5ggOsTliMzmneMRWxU" +
                "jaidTyrPKh5F7k/Oh6Ot9ix51H1n0wjrAED2vsA92vNZ77It3U" +
                "oYRa0YMdPIQUId9XRpjliNqkVViKf4iqorjylrVHpPs4ByX2UV" +
                "bQ5y3qPuSY/tQqXFE/xBLu/qSxhbr1SF5qswS0oIvNHkaKcSTC" +
                "MYzaSJWl+GdbFceVV2p5VvlQYh9SgZr0Jdico/0Ya5fbBRpJ8u" +
                "3iAVfkHPaJR/J0YIZIwbBoUkXX95x+OHSfX2/tfiztabYqjse8" +
                "Mv26Kh/HpK/yt8KcFDs952AtcAsaF4hkjS3UAaf0uAV+g2hQG9" +
                "WhB334vUNrs9a4IMaxERxic8Rmck7zJ7YqRtJOpJZnlQ8l9yLn" +
                "R+t0gcWOO4+s5W45aCTJt5wHIAyKVJ/qODjGUjAsmlRRP31xOG" +
                "Ltfizt+bbKIqW8MhuDVT6OSV/l9TRosSXH1gZrtVtdP8wSPq2k" +
                "sYW636QeyUttloAWdLHZw0gWWXIlhlEbqbL4zbYqjiuv1PKs8q" +
                "HMtwsb80pfgo1H7Tw6j2EY4yzpLfJwGON+G0XUMxzppQ1VpSRP" +
                "8AbPcPnevKxrLzUIHA02aQmL1Bq8OBp8Ze142rfpUNnCe/P2Yx" +
                "Ybj6Zzex7zHf5fnyBZw9E4sXEi2uqxUa1QyfVSoZXh+j6ximN2" +
                "uyZiGTknOxpXxVFbp7PKV+2Pvjd67WSYiTXHzQEtSNbY8l9BqZ" +
                "44WpVcLxVaGf5/y84qjlknE7GMnNP2cVUctXU6/faUr9ofrdN3" +
                "JsNMrA1uA2gkybeBh28Pfpl94pG81E6lokkWrdOOfjjlOplIla" +
                "Vzj62K4zGvTuy+X46wmespwU7POVgd1wGNJPk6PIBpC/vEI3mp" +
                "nUpFkyxap039cMp1MpEqS2ePrYrjMa9O7L5fjrAJc/faFDs952" +
                "Dd4+4BjST57uHRmNKYwj7xSF5qp1LRJIvuT1P64ZTrZCJVls6D" +
                "tiqOx7w6q3zsF7ZynW5IsdNzZmvwzMEzYfVJ0vvIM3mAtpl94p" +
                "G81E6lokkW9XNNP5zy/1GbSJWl811bFcdjXjM3988RtnKdbkux" +
                "03Nmy21z22DFSNLabePRmNqY6rb5R9UjeamdSkWTLLqepvbDKa" +
                "8nE6my+O/Zqjge8+qs8qEM38MwzDOPT7FtP8YacSOgkSTfCA/Y" +
                "gQ2zTzySl9qpVDTJon+34X445TqZSJWl84itiuMxr876cP8cYS" +
                "uvpztS7PScgzXo8O+GIFljy/+TG+x8SD1xtCq5nt5LlbZocK5X" +
                "V3HMvsBELCPndH4aV8VRW6fTP5HyoQzXE1g+PPmdecJkmAnD/w" +
                "FKDZmt");
            
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
            final int compressedBytes = 4464;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqdW2mQXVURHpYgCCokTBKhZNOyLMVKChQNInVmbl6AykxCxQ" +
                "WXSqQMSzIgZUVSkIIf9773Zt4SBYNAEUxMLMEqS802kzL4RzAL" +
                "AckkQEKQhDVCkAmSTAh7xD6nT9/uPue+AXy3Tr/evu6+/c6975" +
                "z7Zswl5pK2NuMpciiVXjaXlF5mjbbGFPH4TjJxbW2Tq3Gctvwl" +
                "LTIj+thKJEpbJY5HnC+uz75qv28VM5B6TA9wniKHUmWL6alsZY" +
                "22xhTx+E4ycVD383Ec0SdhkRnRp7JNo7RV4njE+eL6XJ9WtooZ" +
                "SLPNbOA8RQ6l0i5LWaOtMUU8IRjp+/RCHEf0SVhkRvSxlUiUtk" +
                "ocjzhfXJ/r00CrmIG0wCwAzlGnW4AHfIqPoY405MdHpUeiiXI0" +
                "QtlXb39RnLxPwhJmgUoelyht13l59K4p9qFseZ9Gh7HDc/ZS0z" +
                "SBc9TpmnjAp7gbdaQhv1AOKUcjL/vqPLIoTt4nYQmzdB5Z7pMo" +
                "bdd5edjqi3woW96nt8PY4Tl7qWEawDnqdA084FPcgTrSkF8oh5" +
                "SjkZer549FcfI+CUucpfS6RGm7zsujsr3Yh7JR5np7GDs8Zy9d" +
                "bi4HzlPkUCpl5vLqYtZoa0wRj++Wq51Kvq5P93Gc2qelhRBxDe" +
                "RTO0Vm1xXp2nnY6uNqy8sY4fs0pVXMQMpMBpyjTpfhAZ/Ik6gj" +
                "DfmFckg5Gnk5fktRnLxPwhJnqU2UKG3XeXlUdhb7ULa8T4Nh7P" +
                "CcvTTLzALOU+RQ6jzbUtZoa0wRTwhGet2jcRwxn4RFZkSf2nka" +
                "pa0Sx8NWH1er63N9eqpVzDCDfpWGiKvskvrqXGtJ96FUbpTXVa" +
                "9ha/kMiS33e8wc9qhtrF7R9gGv7JjSUPl0wP1E62uTqj2tUekp" +
                "Ltd8UevVUP1T8P5TW1P1yhBRnVe9Pu/TbpCvdXH2O9tV3ue66s" +
                "/KTfFJjjajiZJsj+TC5ELUkYb8JEKjZQxGos5GC+PoKsgiM6JP" +
                "7XyN0laJ4xHni+tzfXoujB2eM0qlITsLkOKcwKPSO/k+1JGd3x" +
                "lBVM9Fika+bp15H2EJl3bIWcwWlghdXiKzSzvOJ8bxiPPF9dlX" +
                "45gwtqyHJXOruRU65qjr3a14JKOSUagjDfmFckg5Gnm5+TSqKE" +
                "4+n4QlzlK7QKK0XeflEedDPWXL+3RxGDs8Zy/BCzhPkUNp8l+N" +
                "cesCwzrNaYp4fCeZOIxGL1gXCAt7hjWQD6wLRHZdka6dh8zHPn" +
                "5dIHI3ulrFDKRrDdzFiCKH0uR1xt3fSKOtMUU8IRjp+7QujiP6" +
                "JCwyI/rU5mmUtkocjzhfXJ999c1qFTOQppqpwHmKHEqVZ8zUvr" +
                "NYo62Wwv5OoBGP7yQTB/utHXEc0SdhkRnRp2+vRmmrxPHo3R7m" +
                "s9TPJ5G7saRVzECaZ+YB5ylyKFX2WsoabWXacTPyiCcEI1HXcV" +
                "ccR/RJWGRGK3XcnL2iUdKqa+eBVYXV6vpcn7a1ihlI7aYdOEed" +
                "rh0P6NPLqCMN+TEH8wlox02EljEY6ft0RxxH9ElYZEYrddwEfV" +
                "IoaaVs4cCqJCKuz/XpiTB2eM5eus3cBpyjTncbHsm4ZBzqSGN1" +
                "zEscU+uBXoxMxrnvu3EhjnNKWVpJV1sjUdrO2fWI86GesmFV0K" +
                "d3wtj6nHPpagOrV6LI2SOZkEywMmusjjhGMLXvyQSocALL9qgs" +
                "dH2awHHIIuaTsMiM6FO/QKO0VeJ4xPlQTwhbqevTf1vFDKTlZj" +
                "lwjjrdcjwqvckk1JEGdLczL3FMK3eY5RYnkX79NCnEsU3K0kq6" +
                "+mUSpe2cXY84H+opWzIJ+eZZYWx9ziSVd/Ea1u2xOvPdVqL3e3" +
                "rdrl+4vyvfX7xTdPenxSPt7NjX7u/0q3d39oqOJtfdtL+LULvi" +
                "GopezQmtfaTF3G3uJkqyPZJzk3NRRxqrY94ecB8XaPJAL0b6+S" +
                "SwHFNWQRaZEXX1H0qUtnN2PeJ8qKdsybn+83s3jK3POZfmG9hp" +
                "I3W6+Xgk7Um7mV/+LWusjnmJY2o90Atmxj2Ecn0S2PJyjqCzWk" +
                "5m9JF2cobQztn1kPnYx6+f8qpgPk0MY8t6WGq1D4YV7dzSkN23" +
                "fJh9MO+YYT3hY9Z/ofbBcxlfX/jR9sH1n/8f++C5Rftg7JPb5/" +
                "snK83eD7kPXmqWQsccdb1bigdk6jFL679mDfmFckg5Gnm5PvUw" +
                "rr6EI4RZi7JUZsvs2q7z8pD52MfPJ5G5+cswtqxHSHPMHOA8Rc" +
                "4eydhkrJlT/zNrrI44RjC178lYmPFjWUaUu+7Gcpz6nyhK3icR" +
                "V2ZEH+jTHNZLO2EJx0PmYx/fp7wq6NO6VjG1lP49XS/v7dlp6V" +
                "aYy09AdYfSJ9283gXXyJ7eqene8rfTfcAPg9cZ6eu9XWB7O32/" +
                "Y0F2FOKzj/kYJ2Tu+Vav+GW8w90R0+fS59MX4P1f6cvgdyZfdd" +
                "mR2dEO+3HATcs+mbVn49L70wcAuaD8Hf/E6tH0cfRPd8J4GrxP" +
                "h/dT0lf8k823RL4F2XHWMzvZ+T+YPgz2x+LvwIWjSE73pwcg90" +
                "XybpAd4Wr6RHaiWWQWQcccdb1bhEcyPhlvFlXeZI3VMS9xTK0H" +
                "enGsZLybT+NDHOeUsrSSrvKWRGk7Z9cjzmepn095VdCnE8LYsh" +
                "4hVUwFOEedroIHVPcO6khDfqHc+4yMIaORl/t87yyKk/dJWOIs" +
                "sL8TKG3XeXn07ir2oWz5fLoojB2es5eWGegxUqdbhgf06V3UkY" +
                "b8QjmkHI283H38zqI4eZ+EJc5S/4tEabvOyyPOh3rKJu6NQezw" +
                "nFEKV9j87Vh5Tz4fx2F/b5FrgJGfj8tX33HyiXm4LgjXFsG6YF" +
                "0YV2bFdUF45+k7tnWdLX632R/ay6+IavYVH+B1dGlfdbHWhT4x" +
                "qrQP1k+BDmKN6juRNbB+8r7+/PaNnAXWTyPYy0exDmPaAeungr" +
                "Oy9yfkKvkvU2FMXRFJ5hZzC8wsR90cuwUPmE+HzS31B1lDfqEc" +
                "Uo5GXu7zHV0UJ5/7whJngXWBQGm7zssD1k8FPv4+LjLL6gmrzx" +
                "klXBekG9KNfvYNplvTbXZd0PkFWhfA2JO+mO611136n3Q4PQjc" +
                "6+kbuC6A++xRfj0QrAvSzek/0kd8NY+kz4brAjHnD9t1QXYMrg" +
                "uy4+W6APr0fvpQugW8YF2Qbof3HenOdHf6dPoMXnd2XZC+iuuC" +
                "bBSMY2312XHZp7KTspPTTWAR6wJ4/2f6lMj9UvpvoEO4LkgPOd" +
                "2b6Tvpe1mbWBdcZa6Cs/AUOZRKO4z7bZQ02hpTxBOCkV43GMcR" +
                "n6mwyIzoYyuRKG2VOB6ICavV9UkpjhlIM81M4DxFDqXqEWZm/S" +
                "HWaGtMEY/vJBMH/DaOA+txYWHPsAbygetOZNcV6dp5VKO6LfXX" +
                "XZS7KGYgTTFTgPMUOZQ6P2+muP3dFNZpTlPE4zvJxAG/NY4jah" +
                "UWmRF9oE8Kpa0Sx8NWH1fr+xTlLooZSAvNQuAcdbqFeMAnchrq" +
                "SEN+oRxSjkZejt9RFCevVVjiLPU9EqXtOi+Pys5iH8om+hTEDs" +
                "/ZSx0G1jFEkUOp1GvcCoc02hpTxBOCkV73eBxH1CosMiP6NAKU" +
                "tkocD1t9XK2uT0pxzDBDW1vtytoVpRWlodqPa7Ozi/N1xXMwe+" +
                "9y3ApascE+eEXxc1+rLz/QehVnHhNrymeD56srOGr83BerkD6W" +
                "I6m0ovi5b+m58qG88hU6Upid1plRjIJzTTd2XOe+A3+ZPx//FX" +
                "ySf3D3hyPdDu06XBcU9ylbAFfq4ez2ln3aXjugNZXj80w3p4dl" +
                "n2qHAs/ZteF0S+WIvNYdMHZ77I1Yd7T+H+OsJ0G0N7IbVDT9N1" +
                "d3pC+16lPWo/cL1YXVWmkg3LdUv1Qaaqx1mgHetxAf7gcsfqR9" +
                "S20T70caA7hvyf0HSk5TbRTvWyqz2cf75zWVBor3LdUvYlVExb" +
                "PKgWDWDLTatyAaq0mGk2HHr/HP+51sR2d7Mtx42PKlNclw71Sr" +
                "gz6tQTtouqS3xWMk0urnT7UHyUJ48rJYGxUlS3unkR11rk/Cx3" +
                "JUB2s5v7vC2rEqolQnn2nejTXUJ5dbPn9aQzUlw2a6mV6HmWem" +
                "w/fKi/bdcnZ0jjHTG48gT8Pug5G3VHpLnqxqPm0mH4g6gO/Siy" +
                "NCHXvJjtrKbO2ja9LavE9jOJ6us/U+OLTLszSXmksdf6m3OdmO" +
                "6pfZQsP16VLyqvSwt0RSTNWnZ+39yVrIQ3vJOmqHyI7axoHasP" +
                "TRNWltft2dTfFqb8jaRuxTYOea4JhmptnvOzMN6oPvOyuDBUbn" +
                "WEuRp+H65HiykUXyZFW/c75GPuShvTgi+5FPY1j76Jq0Np9PY8" +
                "N42l7Yp8AuzzLZn+y3111pNV53VrYUNE8l7hsg2V9aTTq4P3me" +
                "bLn3anxnbfDdvp4shGev0moblSLa647sqGsc1D6WozpYy/kxH1" +
                "ZFlOrEStX9aXV+f9ofWvgsk1cT2GkjdbpX8YCZOwF1pCG/UA4p" +
                "RyMv9z19WlEcqkha4izNIyRK23VeHn2fKfahbNyNMHZ4ziiZbt" +
                "Pt5la3n2tOtqP6FdPt/p6um4e77rrJS3pLnqxqPm1096du8iIu" +
                "n+OiDrg/dfMB5/w5f3/q5irlkFrOR/Hc/cln7TtjxOsuqFqeZX" +
                "IwOejW42vx/mRl6CCMzusttXxpLenguvM82chi8ahjq7o/HSAL" +
                "4dmrtNZGpYjsR7rmZ7WP5eyojMJ3ziv6dD1WRZTqxErV1bU2v+" +
                "4OhhauxnSZLrcu6PLrgi7L2VE9z1Lkabj51EVe0lvyZJWv+njy" +
                "IQ/txRHduqCLD+jTOdpH16S1+ffdVzmernOE+RTY5VkmryXwTY" +
                "TU9e41PCDT+agjDfmFckg5Gnm5Pk0oipPfI4QlztI8V6K0Xefl" +
                "UZ1U7EPZxP0piB2eM0pmjIGdEFLXuzF4wMydgjrSkJ9EaLSMwU" +
                "jfp4lxHPHJCYvMiD7lJRqlrRLHw1YfV6vrk5LMX5Qh2ZzAShmp" +
                "691mPOAT+Way2f5dD2nIL5RDSlz5HvJyffoG48rLOUKYtShL+X" +
                "cyu7brvDyqFxT54HM6mVlWT1h9zijle8v+cB/ceWZpqLzRafrF" +
                "LrKffxer9Ih9cL/+u5zo/jSP97fl5fr3O8D2t/r9zsrlTYFPP9" +
                "Vk+eJ9sK2equXa3O9S/cHdur/4LwDlucI+eH0CK0Ckrnfr8YBP" +
                "5FvJ+kadNeTHR6VHoolyNEK5Pt3AuOb3OGKYNc5i93cyu7brvD" +
                "yqM4p8/HwSmWX1hNXnjJL7G6h7oWerLDX3UufNvZ3nWOo0q0hn" +
                "/fhzZ2/Uowdp6zf66/9e1Bb9Hmxt4L8K0ejhsqjnKj6v93H+Q3" +
                "kdq+x8ouiUCaunagnpz2cVV4URWs6nVWI+bUg2QMccdb3bgAfM" +
                "3InJBvv3dKQhv1AOKXHNH5GXm08LGNecyRHCrEVZmrNkdm3XeX" +
                "nY6mMfP59EZlk9YfU5o5R/jiuj+9OMvJsrxf1pZfHv9lY/0v2p" +
                "c0brvy8A7MqR7k8Wq3xWUk2Wb3F/mkFVydowUjBrVracTyvFfN" +
                "qUwF0SqevdJjzgCr8s2eTm0ybWMW8PuD8JNFHimtcQys2nZYxr" +
                "zuWIYdY4C0Tqkdm1XeflUf1ukY+fTyKzrJ6w+py9NJgMytU7yn" +
                "Z0diWDrk+DsH73OreWH0Q/7W31xFNM9X23hvyTQbjuBqWP25MM" +
                "hnHpsNed9rEc1cFawvr51EVVydrc912LfYvG63NlC/19Ac+9zm" +
                "lFf3Ve/DtC8bxV193Iz34OjxSz+gPw2CK8xe8I/Hf2Ojvms78j" +
                "qEjfL8j9Usv/6x7hv7eT3+Q7179B3MVFFtgxf809t+5phY1fdf" +
                "GbVXNu24d+2ZjNng/yiLPb6mO/8rK2j/CqfT3WTT7VR1uS939P" +
                "kGdJqIkqUR7au74xtlDOkWPGleiqqWKuvKj6D4rUKnr++h/Ptu" +
                "OY");
            
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
            final int compressedBytes = 3211;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9Gw2MHUX5QbEnRVq4XqGc1sSYEDVqxACWxph7t28ThGgKFj" +
                "2DYqAWpWmjURqqFDPz3r7Xe1dLmpJo5T8YoBjEkFRafjQC8leB" +
                "lgotCidt8VroCUHKX7EV52e/ne+bnZndPa7uZWbn+//m25nZ+e" +
                "btRauj1bVapGp5yZaG6vdrHGCi1cly046QnKmTKwycrAAuedWv" +
                "Q3p+ajRQq0YThpPLMd7FjWm6SO/zGpPLwFqthm1TeWwhg9ZGa0" +
                "VL1Qq3Vv8J77jGAQb4bNiujTbgUnG63qUn83VtyMoIwbu4MU2X" +
                "hPntYcsuedpnyt/oT6WuA0z9sRq5DMV3hTjq1+RxYLNI58hVPq" +
                "rUAFapddv7Mv6H/IuujK6EGmANJT/XOMAAnw3btdEGXMrvG116" +
                "sBd+K8lqjHdxY5ounZP99rBllzztM+VvnJRKrQNM/LL1PNYVPv" +
                "sAh60N2yzS6ZI1GsAqtd75ehXv/NpzuNlpf8YBM7hGPMlfoZ6O" +
                "F+mNx6WM+3JRwGbRlVzt7YnQwPpd/rk9ad5QKU4B//hHM0vWqs" +
                "DnumNlcLzHyNicg1fVJnTxM8T69Evfc5E1xInPC9vjX6i9jyta" +
                "E60ZvFbXQrtqaUi2dW1wpo3lZG00AQzcUgfWjPVImqTmtWIYW7" +
                "DpuAfGPvXb6G/eANa0XfALOOQdogIYDflHR3KL0PJ4tXnnp2FN" +
                "VS6pMyzrmXePV9cUnIMzGzOhBlhDyfrGzO6wwQCfDeM6uQ1rAy" +
                "71vrvByI3sMBpsqy4rrYXYOqUb67Qkt7p49PqELWPvQZb2WUPx" +
                "OH0OGpYl+V08LtdxgIFP36HOUwA7Mkr2M9cCJR4feU7WrJ63i/" +
                "XqPwmNPE95qD+s37Yvr9YOl5/hdTw/H7C0tV7dk8X5FPq+MxTv" +
                "Wkc4Rl4i86CFKHvKacj495a3atuj1Grvu6DVTdn6tMGK06bysv" +
                "mrcw7q93PVNMB4qiLTme+iTmKcNmZP5HQrThvLy+av4R2lxtPG" +
                "CYwnp4z0Pk+dxDjdm42nTT5KsawjTrurWcfXqm9Wl0k2lvV64u" +
                "/h9InMK1rlWovL7wu6s6tZJ2NjXpl9gVumeDdTcvzcHt0ONcAa" +
                "GpyrcYABPhu2a6MNuFScLnTpwV74rSR3Y7yLG+yaIr138Zg+5y" +
                "NgeLCszZ9K3Z09kS+K3g27KN6YBzi630Pz6MfVNLQWVrcqvc9T" +
                "J3F9uiOb4fdZcbqjvKwjTpegOC2vpqEgTk6Z5F4XdfLiZPLg5E" +
                "GL8tWiNYX3+Fea7g9LWf+Kcx3vlMmDrdim71fed3jW8ei+LE4P" +
                "+SjFso786O/VrJM4rawuA/aKvS4Zl9FoFGqANZQ8HI3KeQcY4L" +
                "Nhu24tA23ApfZ9C4zcqmuNBtuqy0prIbZO6cYuLY1LXDx63kmo" +
                "dWk+AiBL+6whyHxwbqah5JF4XMYJ6OZuJEL5nZFScTrf5Gyrbr" +
                "TzO6wXZ3daurUQW7ezP53f2aUzH3PR/I7ma7bufH6pvH2AiXWI" +
                "/Zmls4w9ybawrWy7iNOj7FmFEVkGe5GN8XPF/V/sVfY62y9ab7" +
                "C3RH2AvSfWgCl0feIf4r1K8lH2F5aeZHSWshf4OWwn28XEjpP9" +
                "k5Hsjx3iR/Kj+FQhO02UY/h0PoufyP7E7lcrwLfZY+wJwfUU+y" +
                "t7WtyfYTvY82yU/UNRz2f7BO4VUd4Rsh8Q5YPC3hJ+NJ/Bj+d9" +
                "7GFBeYRtFvW21NrfGFoF2B72sqjH2Wvs3+L+psK9zd5lB3mNH6" +
                "H6cyw/bmDfgLACtW5pKNksa4Oh1Hyt5UHCSKbrxdK8HuMrpmCL" +
                "mmfVeipFqVjOlLy9vH8YyuukUPIOPaVA77sn6XuLzs/koONdfC" +
                "i9/yf/xuteSjjFKEzedZ4OHnDo3UK10ZMe9/sueSJ5W3Ik/y14" +
                "w17t37HblIE3dFFy2Q6jvgxomMvA/jgZfhSnZSFvQ1rFeLqNco" +
                "C/GueWBe8L43ST37ZNIePp5szSj0LjqWqu1mWm3T6iyomxiNNv" +
                "qo8n23tvnG6pMJ7e1IXOu/ZUoGEuAxddlLO7tjxvLk53UA7wV+" +
                "PcsvzWYr2Kb72fB1P4MyL203VR8FMZ5Wn1XKZnT0hxGTjl2u6z" +
                "QjkbSxw+bnXz+rTxbdgTkHLLanvFekM8Lkq9Q+9iPB0HsMbpUV" +
                "j35BB5PB21PjlNC1FFfPZRHuOTvHvmXceu3Z6EbNuU+gxdlE9Z" +
                "7wbuBBrmMnDhcyKc0QY0nu8M8xZpA381zi1re1/WTz+F/4GLfJ" +
                "w9xMUqwO8y6xNX55n8u6L8UbUWiXIxe1W1H1b174medEfJN6d3" +
                "dELA7+lcETxrOxQcT6LPcp+ZcYu1gj0v8N9X1Aed5+MrFO14S1" +
                "N2CswfMvtMr13rHHLgdV0U7ZVs3k0BGuYycOE6Tji715TnLeIA" +
                "fzXOLds+spzeEA+m+PO79kzzC1hRfgecScOb362z9ZTP7/I5WI" +
                "n87heu/I7mn2rPF5fL76KxaEysHqpW68iY/hNxmqVxgAE+G8Z1" +
                "PBtrAy5la7ZLT7Z6IUreCrZg0w2Nlrw9jTfasF9Yt91nDfnz4P" +
                "ZHrDx4iT8P5kt1Hhz3+fLguI+9IDATyoMFPZAHs35XHhz3lcuD" +
                "hV+l8mB7JJqx1/4Yfcvb45fu1NNWvy2T6e0P7Y2pRtfv19Q/7I" +
                "feF+TmXr8921zatF+uFQT7TmTP1TV/LVsJP23TTO2T91+dwMl0" +
                "fG6RNOWRLYB8sp3rfT5TiO/323ZRII4826u3P+d62r5RUZRJDQ" +
                "6FRlOZb4bsTBPant/vhnw+U4i/VS6/i+fH802tWxpqnxLPBw7A" +
                "0Rat1X2a0QncqZ1peT3Ui7wPSJpIUSqWQyVnT+Oxf8Yvl07bQs" +
                "p/nq7RPvM9m2ZqR+w11ftlahz4ZjU+z6fVzSNbAPlktT2XzxZ0" +
                "kt+2ixIv0HUzG8Xt02yaqdPffhbb8oHsYGcgBguKpCmPbAHkk9" +
                "X28j7bUHOO33aegvd42fvuSxN43/V633e9/+f3XW/J912v/aZz" +
                "v+/839PVT8b7cVnaX6N9ai0O/d7SJl9wd+839tsLqn1P1z7Pv3" +
                "vxfU+HvTe9ap9T/mSRSjd6Gj21mq7VuU2P/hPUZzUOMI0eicF/" +
                "IEdraAF3GqcHXHqy0yKkEVvEmgBP6dSuKdhXwxM/C9bQSZWlm/" +
                "Y5g6Y36HmagmVpD2kKwMBnsK3FhttQQIf1u/mDQNESbrtGO/y5" +
                "eGx/bPtqDH7D1kfpzrO96W6MlNZ5i4j/EJz7si1iLIu8pT4H8p" +
                "Z4iL0oKMOaj6ksmqkzdvP7nZS3f78jcVLfibnzlngoHpJ5i5JV" +
                "Ows7b9E8Mm9JfdghymiGT39lknlLNu/m8KMlVX5fIDhQ3hJbOz" +
                "kDy7yFUuy8RUGzrfPxb1Gp5m/5zsK30oc9Jzm7fJQyFw9+m+De" +
                "Z5a1V8UvfU6X+x3hO3BOl1IW8YNwTldKKz6n21Xf/T7O6Q6Ezu" +
                "k85767Xed0Ttulz+mU3pX1pfqeZUininKahDXOptvyBfunlSFa" +
                "WFqdjyMe45Px220P125PQrZDXqHxdJFFWdQ8ttQ4cpz7NmfUd0" +
                "183jWnBanHOPvotddeSM99q1163rWmmjhFEdT8Z4rWI+OE553h" +
                "EDw/seNE+jJDc7muKOIrQvMuipq9ct7xywGGeaesLXfFKYpSr9" +
                "S845eR57gMx0loqzTvsh7Nylon1Cbtap7Yfal2mK6m84u5+oaS" +
                "vwptqGat3qV3sTZdBLDGpb/fdcPy3t/vuiHbIWq6PiEe45O8e9" +
                "bxrl27PQnZtikD+3VRPmXnT/FeoGEuAxf+jkE4GzeX581FfC/l" +
                "AH81zi2r7RV72/y4nyckzbP/yGsvsqL7ZU/UEb75CYOl/PVfT3" +
                "xmtS8OWXb7VS/5XWZpvj0+OEc50y3vwZ9J8YNTim17TkKnhD3y" +
                "+TWx/pdeFT+TPcUfZC31TXP9bI+ls91Yiu/cXZv0S1tw2+9sKt" +
                "nfz1a0OUzvwtInRfmUhDWuHlM6Xa9tfDyu+W39btshqtiRbKU8" +
                "4JM6/Rmmdqg9XLs9CdnGFPldD1mfsu96mp+3pM5yrmfb3fj6WR" +
                "TfuMkhu7VinrfN5ZHbfueekuPp1JLjaIyeetXHMKX4ezrDH94X" +
                "DB7ltu07IbMl1dgZo6d0we/pxor0Al/57+lIfE/PWnPLSxVbGT" +
                "lc20xfnO4qOU7uqtirC3Vt8pbGvTbN1D55/zXSF7JdJE15ZAsg" +
                "n6z23uUzhZoDftsuSnyBrs3+qXOCTTN1ur4utuUDcTojEIMLiq" +
                "Qpj2wB5JPtzHL7bEPNht82ofwP905seg==");
            
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
            final int compressedBytes = 3091;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9XF2MVVcVvqUClkrbINLYBx8m0SeMPw+OwcTMuedeiNEqiU" +
                "rB2oCFGJm0sb4Yow899wzcY2aslPBTjQ888WKZkLaZDKVatXQK" +
                "A0RiW6nR2FhqkBRCOrWixoe6915nnfWz9z73zswd7s1es9da3/" +
                "q+dfY958y55ww0Go1G2miUNm20Pt8oPevvPQe5RoVoeL5G+KhQ" +
                "lHJhVu5jT+G8zaZKP43ocWQaZOv1an8VbOfxSv/XOkc2Vp9/Mc" +
                "Y//rk67RhrGGNn6MVqoftQz9LL741rhzLtnWDzL1dKz+sc2Vh9" +
                "/DWe1KzBzl7VEmNn6MVqoftQz9LLN8e1Q5lkQv5sNLq70IdY0s" +
                "q/xvMV21VZh9GkFeIPvZKJuqzZlq9LDPZklU28FebUNtxJnXYo" +
                "094ONt9ardO4zpGN1dfsT5tr9pXtvaolxs7Qi9XmW2I9Sy9/IK" +
                "5d11X+rWqd1GommzR2bDQch6iMj3+3seBX/mDk894U18+398m9" +
                "s/8+mm+jbb7d+QjGKIpHEs11HY+gD8cjMOW7dS3lwqzczx+uy2" +
                "f3UAz1fdZyVY4Q0u9+Pi9cp/TO9E6zT31YqKzui+FsoD/HtuD9" +
                "6Xu12duDv/976HVmGot6df5VHlN70w3ecXawfSFwFLPY2CHT4Q" +
                "YddX1vaAz8BRqhnnrrYT7/Qf96I0fRjhzF66eRo/mPrIUcx8V9" +
                "5CjPSI9gzHA96mMx1x9rPG+PO4yhvlH8YUxPz/p/JStguPUdK9" +
                "d7KB0yx91WjFtEOpSsID/4KQ3BQFaMUTTeQc2R1ZUI7Bdi4VrQ" +
                "i/NiP/mP4xidSZbD4MedWaOfmDGEcUSR33P1BbJ5tn+sfnVflA" +
                "jsF2LhWtDTufyI9xnsi2uHMu0dYPE8bhge1zmysfqa66eac3F7" +
                "R69qibEz9GK1yWSsZ+klk3HtUCYZlz/Np/gL9Ckm56H66GczXp" +
                "err+68JTG8p2Tcnp9ietyGO6nTrusqP1jNDg3uN1N+eDxrLNEr" +
                "PxDcxqf7PDs83b9Suh5tuh5/36XrKSpxoToe8VGhKOXCrNzPf1" +
                "6Xt/sTxlDJZ9XVoe3r61rkQbD5L6sz4fd1jmysvub8NFWn3ata" +
                "YuwMvVgtdB/qWXr5sbg2zyQPJQ81n0ALM/Ds3OhVEZ7VdTZiY8" +
                "0nsAK5gUnyYMZGIcszHAkYyBMfz1InWAvD17OW2ECXuvU5pZfO" +
                "mfew2wOH7bC++Wlidg4R69sx5s5dZX4O0YCAjLseLznYvj4MA+" +
                "qwvqwdpkz5c5jl57CSMJDno9qCOVTC7gFRVTK2SneY9TnnHaVV" +
                "dXo9vd68AdasrpuBZ+d7r1AEcdoHa9jKOfoQASbOjFjM2SzP+C" +
                "qQJz6eJV2sheHrWWv7wAo7w76QGzDUD3nVsbitPF4/Ux25x3VO" +
                "z8WxvK3H+Wm65tyzrVe1xNgZerHaZJq6kgjpJdNxbZ5Jz6Zn0a" +
                "IPXuuTEMMI4rSvLbEhyq3TqRAP70LWczRX0HmpSyN5JowhtnKd" +
                "ntHceptL71x6zsycdbFz8DbXmTcghhHE0XtslFejJTascuv0Yo" +
                "inWieW0SqYJz6Z5XU0kmNhDLGV63RMc+ttLr3z6Xkzc9bFzsO7" +
                "0SjuhhhGEEdvs06sGi2xYZV9Tdwa4qnWiWW0CuaJT2Z5HY3kZB" +
                "hDbOU6ndTcepvBa1/F+454F9L6dqTjkEEfcRQdGyU0VsoZvSbe" +
                "jxmoaF/NEl+XcyHWx8h+sntkZ+X2n9B8Mk84/UTEf0biun0hO+" +
                "XmW/B+ZnbBaF80+9Mnsj+5Pv7S3pK9aTLrAZe946Lv2nXK/pu9" +
                "ZzK32njn452VJccHOmu88/grrupv2RvZJfPz79kV1s0WU72s8z" +
                "5Xu8rZOzof6tyd/Tb7Hcdkf8heKXt4zYy/VvG3yhX7D9v+PZ3b" +
                "XFdrHf50Zs402ct8SxFHfqauC0z1La6b1Z270tl01uw7zrp9bB" +
                "beZp0+BTGMIE772hIbotz+dFuIpzruWMZX4Qo6L3VpJMfDGGIr" +
                "1+m45tbbXHoz6YyZOetiM/A26/RpiGEEcdrXltgQ5dZpVYinWi" +
                "eW8VW4gs5LXRrJVBhDbOU6TWluvc2ldzo9bWbOuthpeJt1SiCG" +
                "EcRpX1tiQ5S7nlse4qnWiWW0SnM5V9B5qUsjeTaMQbVqnZ7V3H" +
                "qbS+9CKu7Dg29HsREy6COOohzN55rT7U93IQYRIV3OhW8fo/uR" +
                "nZXb/1SsT3Uef4rze99bLkgFc866z7sen9Y5gZvC55w+JvSa+G" +
                "jNtfZ9vaolxs7Qi9UmT1JXEiG95Mm4dl1XbJ1ODvJu2sTHluo+" +
                "Xfh+Zve5/qq7v+rzHt2B9ABa9MFLXoAYRhCnfW2JDVFunZohHt" +
                "5FXIUr6Lyfg2G7j+lx5VC93ObS25fuMzNnXWwfvM35aRPEMII4" +
                "7WtLbIhy6/SVEE/V6746Fa6g834ORrIsrseVASfr5TZLfNVxdY" +
                "+1+EIsE903axATmxd2VPWvKpEj7/XH5OMiKvvT/WjRB6/4EsQw" +
                "gjjta0tsiHLrtCXEw7uIq3AFnfdzMJJGXI8rJ4F6uc0S73/HKe" +
                "6NZaK/H2oQ3ZeW+jwu1bszg1VJD6eH0aIPXvENiGEEcdrXltgQ" +
                "5a4zXw3x8C7iKlxB5/0cjDo9rhyql9us8fg9uNqfvmmfm7PMZ3" +
                "v+rcvKeK752sI/S/vcvPf+1NnQW89/bt73/nQoPYQWffCK3RDD" +
                "COK0ry3OilFEuW2dDfHwLuIqXEHn/RyM7pm4HlcO1ctt1njvOv" +
                "P5gV5nTi7iie9v5n+d2dzdWNIXW6dT5lM5Nyje5sVFHHfnF7BO" +
                "Fwd83B1MD6JFH7ymsd03KYI47XPbFGyIcn0fDPHwLuIqXEHn/R" +
                "yMOj2uHKqX26zx5YqfqWaz8jzuv/j34J7H3YlF7E//WMD+NDrY" +
                "/QnvObfW6vvjxcO97o/r+86198dn/PvjqLnw++OWIXx/vPnt/u" +
                "6P113/tdZSdWtda52LrStzzrejeJQyOAiJ1s9gVK3TS5iBCpxV" +
                "Pa3TvBrBeXlPMlqt03fifcZeOk89tdbhcwS6fqqeI2T4HMEMc5" +
                "7Kyr0//ByBXz+FniNMnIk/R3A1Nc8R9jxQKpfPEczMXB3BcwST" +
                "vT/0HKF7xT5HMExun+XPEWqOYO/vC+g5QmuoNdQ8AtZ8Cm4Gnp" +
                "2DpRjNeZ21ZtXLOfqIthycmfPYnM2ib2e+ClfQedLFWr9vwEAe" +
                "1WCGfSG3/en2IFaL1eY78w0Y7kz3++qcdwFz5TdrhyK/10siu9" +
                "f6x/ZCYL8QC9d2N/fHW4fhmdaa1hq06INXTEAMI4jTPrftS5wN" +
                "Ue5ceCnEw7vAjK/CFXSecnL4ehAnNn8FCOOrx58HFz8d5O+77j" +
                "9v7vPg7juL/33Hq9PJ1HyjAOuuqSbhbdZpP8Qwgjh6j43yarTE" +
                "hlXuPD4b4qmu9SbjKpgnPh+NujS6/wtjiI1rc269zRpfVlV/9V" +
                "QciGWi1/Y1iNbWvr4dHJ//FWC4BvUWwjjP73d/HCRvsezmPm8p" +
                "bhnwfbrX09fRog9ecRhiGEGc9rUlNkS5vleGeHgXsp6juYLOS1" +
                "0axYowhtj8FSBMSD26P/15kJ9HK725+9Og9UaujVwjCzPwip+N" +
                "XEMExuRMWqgnTkSX+9PtPo/swu+BMLJKZnkdjWKV1vP787ll58" +
                "K7PnLdzEoLM/CKyRH3V64YkVlrx0Z5NdRjBVWWn+92n4f1yjJc" +
                "UTLqLkgF62j4en5/PrfsXHhXR8w1AlqYgVe8POKuHjAis76Feq" +
                "ygynJ/usPnYb2yDFeUjLoLUsE6GsVqref353PLzqVC9STL/fvL" +
                "vFrpQv2+Sx5Z1P2ns0t1fvL7cttxrc/f73MLvC6o/l1+/m6ffW" +
                "7sJ9rasQQrtDGuP2i99HJ6GS364BVvpJeLSxRBnPa1JTZEue/l" +
                "UyEe3oWsp3dziivovNSl4etBnLbZXwHC8FqN964L/t1rjedz3D" +
                "WnF/wEYhrvP4Vfe+5fjF6xZl53f3e1d1lLw9jHYEgEy++iXJwV" +
                "cxOvxlDEgAiJLNa2HyMe7EL3ofsvPii79bc2vGXIxnuv/ybe3g" +
                "9D3kfgfy8e/55NNdX/inGi7tu6ZJTI5gm/P94HXGfyuxp2UFXo" +
                "LgCPyS2T9ytY0f8B+nkL9g==");
            
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
            final int compressedBytes = 2788;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW2uMHEcRXiA5El/gcr4YG8dYwlLiQATk5YDDIzu7MxDI3e" +
                "FzgB9YEfAnlvMjCIRFZHLx7PbGs+fzS0Ag5A/kpYSXIQgb30EQ" +
                "UkjOdmIJydgoIcLwg0PGNiFExMSxgenpranqnuqZ2bnZE+ypa7" +
                "qqvqr6trenp2d2r1KRL//X/lNRp9J8LdR+I3vByQp5+X9unq37" +
                "/kn/b2H/5cjyCvgab4Be3VetE7Pff8Z/Vulh7LGK9eWfi/L0df" +
                "L1J/wH/EOd3m/DdsT/nf9CePxDZFkayVNh+1cYe37YLlAsGgON" +
                "wdD6dCLbc/7zwDLkNesfD20n/L+H7/6M/88Icdo/459tGHHOQt" +
                "WicToH1vZ54KMo1LNeOjJ4W35sFgL4KhsfGyzLlzcNw3m8nynp" +
                "/SAepwtNn/g69G3x4t7wE9rI+XkrxNqy8hjZA80Wq+oB8yRT4F" +
                "PfaK/Neby9Sor4HGovMH31u6Fvi5cI2TTGd6O0xdqy8hjZA80W" +
                "q+oBc56pOtprcx5vRknvV/E4XWz66ndC3xYvEbIxvO9MGYMZW1" +
                "YeI3ug2WJVPWDOM1VHe23q8ca8MZSqp7T2kJRo8cbqm9CblPJY" +
                "3xRW3oS6/BOTEZ9NmAc8OoskB8ToUbqXxmFL1lN2iJBMkReX06" +
                "wQ9U54J6TEFo7TSm1sOwj0K6n3ojVqI40BX32tjjI+OS1jEol5" +
                "gAXyUNc7k399rc7WzCa+1FnvV9B3hkwod3KlDPcFjd3iwsZe3B" +
                "e0r9T3BWJBfTxzXzCumrYvGFee4vuCLYfT9gWNJ9l9wXjGvmAc" +
                "GNN9QXi07AvcPrcPJOhKa69x+4LL0eL21Tdjn8ahlAiFwlz1zR" +
                "GfzWYc1qQ69YItWEmjdD9W11uynpTNb0M1xQpwNDflQ7QBd4CO" +
                "tdJla9+sPKC7A/W7QEcJXqVJRP0uPaeKgliI4OvSvIhpD+sY5J" +
                "S0YlU7T+SDRzOeWlR044nObN8dtr3izfH1bm18Xv3SjG9Ec7mx" +
                "R7M92zkeTJ5XjWnvcKXwq/F4qvdJdsVj6zXiHVHjqWJcvKeVbC" +
                "yPx2mD6UNpi7e/givTamdF6xjZA80WG7zTxtnE22tTT/1Y/RhI" +
                "0JXW/kr9WPAuaQGrN4teXUKsNxtmn4VsEBXVjGMxn84CcyFSYY" +
                "J361G6l8ZhS9aTUq1PyArZ0vp8BXJFXxLOR5xPE+b4Nt+U6yyJ" +
                "zjsxSPIubg4EHyx+3omhNG+zn51Pr8tgmfO8E5eIRUZktD41Px" +
                "eP09ZEzDJDvz3/ew3qxcdpy+Hu16egVunpq3Yf7aHWbXSap7ys" +
                "uM/MF9NtZRK5uLYYpeoprb1DSrToXinF7TRaxWNOQHeusK9P5t" +
                "FZJDkgRo/SvTQOW7Jekl8071xbTrOCcS69PT7v9hhPY9ZbntKs" +
                "56263e3B3FcV+Ppl1xPXhm1V1KtGZ3tnHRcfa/88Ot7MrePiU4" +
                "k8I+Lq6HhdZx1/T+x5r3hfuI6vYat7Ylh8XMQ7NfGJsH1SQ1wF" +
                "67j4QMdCrgjiRuHIdVyEq5/4aMc2GraxgKyl4ppIXh/JG8RqtY" +
                "6L94sPsZw+LD5CtDXiFu2z2aEfw/n0BOjK5gzr/jiStTvDzjCX" +
                "n50XO9K8SQxwkhVC+zCPNyXPJK227tH342RfcDJrP97Fjno6GJ" +
                "vf/bjaZ9r34wVY2MYpvH9uv1jWfYt7UTaT9kvljRNfby73LdWj" +
                "eKweBS1k/Q8TYcbk3z9Vj7o59qjtl9MZduPl66VnKjifXinzPt" +
                "jtn8O15o4C86m/3PnkHfIOgQRdae3TygYWwJm6KTEboKL1Yh2X" +
                "h7LQ4ymaVjD9el1swTIeg9mSI4AYrrptPjkbypxPk0fmdz4h+9" +
                "LWp314rO4DLfxEPmsizJik3V5jcg4rQ3od3ht8pvtMBdens93N" +
                "J/15QWI+PTe/+wJnab75xOPYMZ7CY3UKtHCc/m0izJikPatGwf" +
                "k01b03uCMfNvj8nOfTf4rNJ36fOZdxKjSfFufbZ/I4doxn8Fid" +
                "qeK3rl9VDRFmTHUmLZ9uq87MYT7NdO91htKx0HOGuriePBrJ72" +
                "v3wQ9N3BAdv0dwe+zP6cSDxvPMB2LPj8VjzYHJ59nKj4gfit2G" +
                "7afFnmeK75KIxx3yFFI8bH+e6SzKGJsfiZ9o+6h9Srq74p3aLt" +
                "OHMjlOXuqVxN01+fuUPdy+rGgdI3ug2WKdJTxnU3OW2GtzHm9K" +
                "SVyfxLTpQ2mLt37mG1L3ulPp0eIXOkb2QLPFBuM2zibeXpvzeL" +
                "cp6Xrxp+iZPpS2eD2OzAiPs2KsLSuPkT3QbLGqHsfZxNtrcx5v" +
                "v5KN+GxzLjN9KG3xxV7e/uxoipE90Gyxij3H2cTba3Me74CSeN" +
                "55600fSlt8wfPuQHq0iZE90Gyxij3H2cTba3MeZ5vzBXWMUTtl" +
                "k7qymX4zPuN55LY0X3q0ZEIxyAl58/Wo5Jmk1aYe8Yxtn+mVeh" +
                "883/tMr/T7YGP9uzG+Ymyt/I+8kFN+bzBRPo/qadXoPrNSmbgF" +
                "fBSFep6s8af3p9r9ebHMLHhVRwBfZeNjVb08bO0YzuNs14/hzN" +
                "0hm9TRpve5eOu5vj3Nlx4tmVAM5eRs578PVn4qeSZptanHXe2u" +
                "Bgm60iY+7a7esgItgDN1U2I2QEWf77e4PJSFHk/RW/5Ko3S/Xh" +
                "dbsp6Und/TkcqUPcTq71nHw/9tiPgXLLVLuTGWv88s9Ezyj2le" +
                "/1xWvE+e1PpHwvZCrPG/L4jYNwazmfmz1vOd3ped8k6BBF1pE1" +
                "9UNrAAztRNidkAFX0mk1weykKPp2hawfTrdbEl6yk7ZkuOAGK4" +
                "6sYY4jr+5cRusYvfP2lz6aXmgBsUv84U+f1TVr3cv386JV5k4+" +
                "Nxcq8r74oajtPWeR6nHu9qyHz6WqnzqT3P49Qudz65N7k3gQRd" +
                "/ol7Ju5VNrAAztRNidkAFfUnuDzxuyKeZBVawfTrdUmb4DGYjd" +
                "amuc33DAjbfUtt+f/zfQuyL/e+xdmpH8Pz7hugo03vc/FZ+Xlf" +
                "enTrVh1DOTk7W+vs9ajkmaTVNj3OoGrR2X59zO488FGUM5h75D" +
                "Vk8EB+bBYC+CobH6t+75uHrR2TFo3nXfCdstZxeb2by3nXOr/7" +
                "dVwsL2cd13hE/+HaWtpapl3vvhlZB+3jRL9HaA3p49R6Y+zpb1" +
                "3UHAgeSqn/lrj31uzrXetiTbsUxqlF2LUWBQ8S7YJILtDHqTXQ" +
                "yvHfw61LWto3fN60kmSc7jN9KJl71YyVOng45T532stc5ylG9k" +
                "CzxarzjuNs4u21OY93UMlavD/DHvhQ2uJrhXZ33kFbVh4je6DZ" +
                "YoNHbZxNvL12Giv8HqGWeaeR/D2dPcY5O4f16dZU77q51HNe6+" +
                "I53auq6ePUWgE+ikKdXVOv0LN2w8D6Tv5iIoCvsvGxwWN5Odgx" +
                "adH4O/vgEYOv5Xsuam9dhlbntkqPX6oCX0e8o7e1a/ckex1WIx" +
                "a2I1y0M6LjnTM9GKcRO69e1NPGaXuy16k8amE7ykU7ozZ8ieM0" +
                "aufVurxnVaNfTNXi76xq2wqM8rZeMGtdkc07EbOy5PmzsLYQpe" +
                "p1tFptISBim9bTpYrHnICmlfQ8OoskB8ToUbqXxpFWM+sl+SVz" +
                "68yp5q5yV1UqSkbPEFapv3Cfeb+ygQVwpm5KzAYoPTPNY/owHv" +
                "+c47SC6dfrYnOO8xioRp6rGLlNria+M1+vjnvXlHjmXOuc7tV6" +
                "0bqKPRvLrvdfmoSnIw==");
            
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
            final int compressedBytes = 2591;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW12MHEcR3uMvEigJIrKCQEIRDhGQiDcjRZFg92YGI0DkHP" +
                "t8/gHETx4gEkgRSJx4SW53ltUuD9gPUeAJkRgwfyY+SIIgQUAE" +
                "CEMgEoqxBHIwCphwTmwH2wl2IExP7TdV3V09M7c7exJ76t6ur7" +
                "6qr6Znpmd2dq/VWnm4lb9WDmXtgZU3tMav+SswWvlJy3mt/DLv" +
                "77ew347fj7S818qPWlO8Vg6Xeh/RUK7e4j5YjH6xvhrS96fz6b" +
                "vSdmG/G6PBt9a/RWkcwN+Tvm/yeUqjUu9WDe3dUDN3MllN3WJ2" +
                "hgequL3b6udN5qaYpw+Wevc2rVerph3FPH29ybzxPzZ2nqbRK3" +
                "t19tnv2Tx9HzZhnVtsP44nHyeUcDe/rl3m9TmoyShk+C3h7ZG9" +
                "XkmZtvSkH1p5OP2Yto5HX3DX8fSjk6/j8b83dh3X9fx1PP1I/T" +
                "p638j7b+fx43nq3Tt8KH8Xq3n38pIc9xQV5PPU+2rhua93sHtl" +
                "8nI16mu97/YOOdgPPNZVpSvqq8asb4qIw1Kvp6y0da93ve/1Vj" +
                "FuP08tVy3md/gr+CSL7aqXzUxeUp9bxUC9hOmxpFen2jDH9XRe" +
                "TS2fZ16ffgefZLFduaJYzPg/9blVDNRLmB5LenWqDXNcT+cyat" +
                "Z51x/+Hj7JYrtS32LGf6vPrWKgXsL0WNKrU22YUxY9/wF/NP1r" +
                "ulzl0Xe+rnnFwHl4kZpz3v0RPsliu05WsX+frs/1VupP2gzUS5" +
                "geS3p1qg1zyqL5viC91zkK9waOzr06KvHuJ6bZj2aewsq6fnrP" +
                "bO/HeZ6Gf/LqvbxWhvH90/DPxbX1bPfK+O+T11TvvsA5nir0at" +
                "8XPN07re4H/nyX+YfPiL23O7BXd+uojcfnqmsanlnnZ4jdYf06" +
                "elN9vvtOUbWj1FkIVLugozYe/7PGPJ1f5zwthPXr6K3zTAs8fx" +
                "peaPL5U3xmigon+dxyptnnT9HJ6CR62GQNLxEGP79zhB0tc3Cu" +
                "cd1n/Tx2FfCwZVflV8EqiONm9Pxq7fr03LIeqeDMeHFfkOyjVh" +
                "zlNweO/pt1VOLdO+IXJz+ekn1lyrr+NHpqvuvj69HDJmv0NsKA" +
                "gOfabs/ZwMrHL2h5ZBV2vGRLBddv64r2gs7hbP4MMEdTDx1P6W" +
                "pze6N7x1TXlgme03U/N9vrXbeH0WiLc5RvDxz923XUxuNnZvBs" +
                "cXtYv2m9eGu8FT1sskbvIAwIeK7t9pwNrHx8Sssjq7DjJXv0Th" +
                "ll+21d0U7pHN5mfwaYI2PH1lK8lI3yPseW6C8bzRMGBDz+690m" +
                "o9FzNkTl4/NanqJW4XFV4Od8tlfGiXZe53A2qS1zu9vs8t3PLS" +
                "PnuXNnR+Do36GjNh5fnMF5tyOs37RefFN8E3rYZI22EwYEPP7L" +
                "jicRjZ6zISq/tr9CyyOrsOMlWyq4fluXm69HOGfzZ4A5mvp43+" +
                "y337N5WoTNmD229u3+in2/v8xXHm2ud5Ija+rs16935Je9XkmZ" +
                "tvTEC/ECethkjT5MGBDw+C87nkQ0es6GqHz8lJZHVmHHS7ZUcP" +
                "22rmhP6RzO5s8AczR15953rdiLf2jy/E4ua83opT/PnIVe5+3U" +
                "nHX84/BJFttV35tLZlb3y+pzqxiolzA9lvSq85ZxbE/oecHoU+" +
                "7zgvTYFM8LTm/w84LTzT4vKJmnTzf6XOXZDZ6nZ+vNU/rE1PP0" +
                "mUbn6bkNnqfnmj6eWq32JWrO+vRZ+CSL7ar1STKzuv9Vn1vFQL" +
                "2E6bGkV523jCM9yWPJY+hhkzVaJgwIeK7t9pwNrLzuNS2PrMKO" +
                "l2yp4PptXW6+HuGczZ8B5mjqOO/c4ym5fcKz5Ih6rzjFc9/0ZK" +
                "n3lHq9u73m+rQ29frUm259GqVyfUpeurHrk67nzlN6ofbnu1vj" +
                "W9HDJmv0JcKAgOfabs/ZwMrHT2p5ZBV2vGRLBddv64r2pM7hbP" +
                "4MMEdTDz4vcH6X1NkTuBvbo6M2Hv93BvfHe8L6TetFF6OL6GGT" +
                "NTpCGBDwXNvtORtYed0XtDyyCjtesqWC67d1ufl6hHM2fwaYo6" +
                "k78/aiPxrvvV2BvbpLR208PjmD42lXWL95veA6/ptG7zMvbew6" +
                "ruv517v+KyesqZinvvP7hs62wF7dpqM2nrRmcDxtC+vPRG8Ltf" +
                "z7luIzcv8K+CSrs2U9WcX+fb4+t4qBegnTY0mvTrVhjvREj0aP" +
                "oodN1uAQYUDAc22352xg2ZllHtfH8ZItFVy/rcutt6RzOJs/A8" +
                "zx1aOj0VFrHc9t0waHyQMbPEYlG2N75F0ljiJC15W5why3Hrsy" +
                "eg3uC9VZcu0/qiPa9ojz7jXOUbgzcHTu1NEQv8H1YmdYf/Djhu" +
                "+fjkXH0MMma/AAYUDAc22352xg2ZllHtfH8ZItFVy/rcttcL/O" +
                "4Wz+DDBHUw/dFyQ3NnlfMNWdyySf725s9vlTdHd09/xZ6lstGp" +
                "FlxumXGQHPtannTLAJoUwyM7jwGa+fVdqDH8oo2y+3gPWpel+v" +
                "+xWokS7qAsO8c+VyO/L3OfTRXDT+X5BsNMe+fHQgcv6nTWf4LA" +
                "1ln551HfaBce14n+PqNcXQ9k18P/54k+ddctcGn3d3Nf/cN3C9" +
                "e61zdVl0ueP/v1tUr0WLNt5/fAbXu8Ww/uCh2V5rxTw53x92lg" +
                "LVLumojQ9+NnlN/deXKev6g582fF9wMDrY/gv1rRaNyDJjiYDn" +
                "2tRzJtiEUCY7DzzkM14/q7TJz/mkV24B61P1vp6pAxFmhLrAMO" +
                "9cudyOnLuKvr2K9am9ymjx/YPzS0Sd4bM0lH161vq2xKDkZ/WZ" +
                "fvX11nHveeZfqyL176X07xEGj2zs9wiDnzf7PUJ8TXxN+3g+s8" +
                "dNM7ZB28fNmBBjE0YR6IlNDMJkjmIPHicUapSL3o2PPZyD/ODI" +
                "WslGTfYWQJ/ycp2IlNmgy9k5o4ugtvYJ9O0T+L/X9glGiy3Oxv" +
                "232LbPkDZnclH25Vnf7GZ17f4b2e6/KbPf6nNYH9X3r/MV3e3r" +
                "b651PF0dXz1/zrybfv6csckyY0KMDQaNqSc2MQiTOfAymUwPNc" +
                "pFf8bHHs4BP6nIWsnmmngLWJ80uU5EymzQnT8n58Kfm6LqzXE2" +
                "n9Tn2Gb6y1b5BD1jPDZ/5KMeOWQ2ROXXDDUPeaStqXBu1rNzIQ" +
                "4tSnw98qGu4nqfyNzgyFibX9w/Ff9vHr1XvZcR5x0xdJ53/xFk" +
                "9fbZ550eyeddNvbOOy/nF8dR11XXVfO8uza+tv0E9dnZmo/IMm" +
                "OJgOfa1HMm2IRQJjsPPOQzXunxVcjP+aSXdRFLzdczvakDEe38" +
                "Fz2yenC4HraStWSts5yNN5m+s2xs8+vDzrIZE0K/RiQefpkIn0" +
                "HxW0ViIEdx17xMKDGQP1m7s2M0yUsI5yAeYeRnDvmpxZvM7w65" +
                "Miiheq4WEcgGXc4uf3M5PoY2yejQ5+DBr/+fnz99flPDn4P/B4" +
                "9kWs0=");
            
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
            final int compressedBytes = 1867;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW82PFEUU7xMHNsYYL5z8DyQr+wG6wZ3pngngzY+9eDBhEy" +
                "Ji1Gg0HnG2tzNm50IiKgQNRxEFP1A2MdFF+TgYMZpo9KAxG12c" +
                "FWFXT6AhcWrevH6v+lVPNz3VhdOTflvv6/d+87aquqdn1/M8L9" +
                "jm9WSwrbHT80AjKx7hQU87DBGv6jrFSStWSXpuVuc2rCRRZaRk" +
                "3+8IloKl6osgPQ9GoKkxt2BcUgdJSKiDBZB0HPSAT3klKtfBT3" +
                "jcS3XVCOsDez1GScUDM9QIeSE2xHA+qKmf/gpKf2VuEvrnr0Rj" +
                "nfnxOvjQpvc4qSNG0pa0Ns9xXzZqeKi/n2xYSaLCMXeUIuld5z" +
                "san2raXThayEQI9xnxvjRZax95hY/53/t6/8xfr7EYj873si/n" +
                "XHfLwXJlFaTnwQg0NeYWjEvqIAkJdbAAko6DHvApL/fIKuAnPO" +
                "6lupgLp6ynpOKBGWqEvBAbYogPaWJmTuIo2lpsPpmP2tue08N2" +
                "vcqVypVgO0oYgabGnV7GFqXpI8pTFmXrdu8F0mEUbDfjKKt66b" +
                "g8ErN5lo5ETDAXTlkP+CFa+LwaIS9iwJlzTcynq/H+dMXmfHJ9" +
                "RDmv93N/FcSf6vUgWrhqdR2cdLzuctaLthfsUyWeT+v/m7lRLZ" +
                "AznTPOz7k/rVfWScJIvcIoqCpdSXyRF0bgAwlYHBOje9dVAw56" +
                "dFzSwIJ+tOtIOnc8g6qsJ/kRL16TmGvaWmWtM+pJGIEW7FAy3I" +
                "sW3aukikDZxVvr8Yh1HAFaEgc9Oi5pyIP4UT1C4Hl4BjtkPcmP" +
                "ePGaxFyvkNjXduP+FNWt7heLjvenkuq91Ls/DX+JK71hjCu4u0" +
                "eP9q1+I5PfRTb+vnP+lNGnLvvGHTne+aXUTyrsel4dq44Fm1HC" +
                "CDQ17szL2MK9yTxlUbZgM2YgNmDoONyjXqiDh0diNs/SkYgJ5s" +
                "Ip6ylJaFCX2EpMXetzP/6Y1XXwnuN1Z7lesBqsVn4G2dmxuiPQ" +
                "1JhbMC6pgyQk1MECSDoOesCnvNwjq4Cf8LiX6mIunLKekooHZq" +
                "gR8kJsiCE+pHVj2/U99T1KqtPz1FlpV9rgg35iBPqVxAjQMRoz" +
                "IEdFgZWi2F1JG7yImMRDTKiLVQGXZyFvfA+ISlFJNHp/GEUSmZ" +
                "sYJ9dd53q32+o6+MDxuiu5Xv1EvCJ3Za7ZXfni+kdl5Sf94ct5" +
                "quVjlZd9L3YLymBL/XO0kVWPM+VxSzIqzYpVzKj5dW7DShJVRk" +
                "r2WYffRum3o71oU/r8Yb+djDPlcYuMMlnJZ0blenOpn5/bsJJE" +
                "jZ/7tjmrNF45njPQfcGTVveLdxzvTyXXY/v401Z5n3bcp5LrRc" +
                "/Go+es8j7huE8l1Ks3UdabjQNoAx18PM6Uxy0yymQlnxk1mZ/u" +
                "5zasJFGTWDgyx2Uf7Hupgk9ETd9Lzd1ee6v47zG8s+9OMWKcTx" +
                "n18Hupogfr09+C0W0D9Om44z4dL7lPB+Ln49cGQ1q4rvH+2PH+" +
                "VHI9Np9uWERdrh0bIPt6gT4dK7lPB+JKY5nrQXwv1TiYyvt9x/" +
                "PJcj1/k7+JJIzUK4xa9yidLNwLo3Afz4Z8wsToHu8PJY7OQnKg" +
                "GD1L9/I8OmU9yU9i68z1CmnzqTVpdT6963g+Wa5Xm6nNoEQdtN" +
                "ZWsKEF4+gV7uPZKAkNs7rjUyYczkLP59G8QtKv12XnKXMMockO" +
                "UIypeto+3rrXG+Kj+bX1nbv390+Nk53zNPWpdiSO+Ax+zv8YWy" +
                "50pXbtbXyVfv/U+GQghn2fuTXOGtfJEWPsYtH7p9psbRYl6uoV" +
                "bAw21mbnD5MF45J6UhIaRnWf82w04XAWej6Pbn7Ds3S/XpdOWU" +
                "/JuaP0nmUHMFd/z8l4se7uv/l9vM86+Nbxujvj6j6zNW2zT/4F" +
                "t30qux7rU7VT7aI13hfd5ppzirPwK36FJIx62n4lyZLwCgn5hI" +
                "nRvJKOo7OQHChGz9K9PI+d+5P1JD+JrTPXK6Rd71o7k9e76FDx" +
                "613zN7fXu+av+a530WsFOR2N+7RrsP2p9YD2hOyw2/3Jdj1/xB" +
                "8hCSPQWo/4I62HyaJ7pYR8wsToHu9XJI7OQnKgGD1L9/I8OmU9" +
                "yU9i68z1Cqnz6UGrv983s2NaD7mtd9Nziv3fBn4O9ldaM6X938" +
                "Yl7ru1/7cx+POn1uNW59MXjvenkuuxPj1hlfc5x30656xPT9m8" +
                "H6+fd9yn88769IzVz3erjj/ftV31qT43zM+fymZPfWr+Mcx9Cr" +
                "9z9TnYv9si6vJA2QW+l7LJPmM+XR7q+fSDqz7Vlmzu466P5pqz" +
                "Pp0d5vlUNnvWpzPD3Kfmups+BRuCDTZx7aLd+npxn6aDaau8px" +
                "33adrVuqveN8zrrmz2rE9TA+4Q125pn6Zc9an571Dv4/842p9G" +
                "g1Gx5kczd4XRtKjsXMv70+hg/tx9Gg/GBfZ4JrvxtKjsXMt9Gh" +
                "/Mn7tPE8GEwJ7IZDeRFpWda7lPE4P5xfEfe+4B0w==");
            
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
            final int compressedBytes = 1651;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWktvHEUQHiQkkHBAKCgILEWYCwiFQzAXZCRq115xQkIcuH" +
                "P1wZg4TsBgEntnnZH2wgGQEAcuPG6RUGJb2vVj/cRxIDhwMIfE" +
                "4cAPQPADYDbjnuqu7p6dmultKbPaqemqr6q+1Fb3dtobBOo195" +
                "l4ak4GPa76eJD7GrsTeL36nU+q0zmnvI881+nIT51Gh0aHqE3X" +
                "mBAUlYx7+7q9euUry0fqp2mn8+6u535ynm9u9Vhejd9Lc6fTOl" +
                "1MEWuaz+79+6Ki+/lY7htytKJHSzD8MdO6ZdJe6Rixy+nTjpt+" +
                "qjdGR7T++bxnh30Rd/SIsc9HPM+7kXL23OvT4OhgEIRfKbEHe7" +
                "Ib1FHJuLev/YoeKVAnY775b/L/W3KvTzMuP99owG8/RY952xfM" +
                "uoxbfchvnfqdT1rH57WuPZErgmEdn3+i8l9vz2bdsuqdzPKaN3" +
                "ZOr3yu1vH4E3kheICvfrPHOkUnHuQ6hYGvOjUjp+vq437rtPC2" +
                "tzp97XQ/fsnzfvySnzrVG81vy0VqfqfwnvNcpzlvdfreKe9PPN" +
                "epz/kam2md2k7n3Qee6+Q4X22iNoH35Kn7iuu03h2jRrYmT/Vx" +
                "2Tvxx5gCfcx7So+jstA5IEb1Uq2yH771fDo/PbbKXM1A5l26qj" +
                "R/cdpPk577qc/5xH487qdfndbpsuc6Oc7X2CPjn9I6HTit03lD" +
                "7t0+1um84/VpuDaM9+Sp+4rr9Ft3jBrZmjzF65PknfhjTIE+5v" +
                "2pHkdloXNAjOqlWmU/fOv5dH56bJW5PIIpmAoCcU+eklHz9+4d" +
                "Naq1e6+Py96Jv/BAz+M6TetxkKtskTOqESkLzCL88K3n0/npsV" +
                "Xmagbr+nTodB587Hl9cp7PfD4e1+mPFOHgfHzsoxIMC5yPm/Pp" +
                "5+PhvYLr+u10X3DH6Tr+ft92xgd+8sEeStiD9PsvepIiqI+ql+" +
                "PoeJOWy5BjrR9mY/l8rPPuyOm8m/U872bzzTvGp3WIEg7FKK7T" +
                "PYqgPqpejqPjxz4s0U+HfKs5H2KB+S1Vu1W7Je5inIyadxOd0A" +
                "gcvuL9k+Qt7hhNeN3nfdEUR2ah+stoOQO1q3nxredL9BhNrwBi" +
                "TNmt8+5Pp/NuxvO8m3E978zfd/WG4/3MvOfvO+f5bP0kIVz004" +
                "XiDOsTBfrpgvN1fAklLIlRvC94hiKoj66358iPtTPkWMOX+JF6" +
                "sFhGCcuQVry2QBHUR9fbc+TH2hlyrMg+fyQ/8y4zR2usxK/zCq" +
                "3j57LnXelV8XZ/VtvKP37X8eiU8/+3bKGELUg/nepTFEF9YCsr" +
                "nqozY3kMOVZkb8by+cA2StgWo/gTeZYiqI+ut2nluAXqtM23hm" +
                "eysXw+sIoSVsUo/kSmKYL66HqbVo5boE6rfGt1OhvL5wO7KGEX" +
                "0jPr6psUQX1gNyueqoMSZ+HZvmYrsjdj+XyggxI6YhTPu+cogv" +
                "roeptWjlugTh2+NXwtG8vnY/u9b9F9gXk/Xvnb777gyg/O9+Nr" +
                "KGEN0ppUn6cI6qPrbVo5boF+WuNbkb0Zy+fjqZ/+9dtP0dPO+6" +
                "mNEtqQ/voiGqII6gPtrHiqDtol+qnNt4avZ2P5fGATJWyKUdy5" +
                "wxRBfXS9TSvHLVCnTb61OpyN5fOBDZSwIUZxprMUQX10vU0rxy" +
                "1Qpw2+FdmbsXw+sI4S1sUonndnKIL66HqbVo5boE7rfGv4TjaW" +
                "zwdaKKElRvEn8hZFUB9db9PKcQvUqcW3Inszls8HVlDCihjF/f" +
                "QyRVAfXW/TynEL1GmFbw3fzcby+cAOStiBHTuCamAnK56qgxLn" +
                "9tm+ZmulkY0VT5XcfweAGyjhhhjFEToUQX1UvRxHx5u0XIYca6" +
                "WTjeXzcbXP7P4doXbKts+slTg3K7LPNOfT95n5ebncj4djlhyt" +
                "6EW/dVr4MrtOKeNa7q6+hhKuiVG8jr9BEdRH19tz5MfaGXKs4W" +
                "V+JM5V7ny8dpJvKc34oEy+orxK1mmAb+lTnQbKMtZ68SZKuClG" +
                "JgTVqPruqPqeCW2Ly2XIsXaZZGH5fGAfJezDvh1BNaq+O6pOmt" +
                "C2uFyGHGt1MhvL5+NyX2A/f4pe9Xz+NOz6/Mn1OZ15X1B52G+d" +
                "zPnK/D0YrqOE62JkQlCNQX/VlsMUl8uQYzUzKcViESUswqIdQT" +
                "UG/V+2HLBYniHHamZShgXpzNP5sZzfRUevBF6v6KzjgP8DEhTZ" +
                "rQ==");
            
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
            final int compressedBytes = 1799;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW09oHFUYH6X0EkoPvVXpqT0XvUSCYmY6iyY5CRJR1JTGHo" +
                "x4kBYPNrFuNptMD6FF9BikIFUbtBrQQtBeih6siG0UKR4k6kHR" +
                "JhUUVCo4M2+//f68NzuzM28mkzrL+/Z9/37f9759b+bN7K7jND" +
                "9x4qP5Xtg+au5ztKN5SZN8FtMPmewLxxn+IXz/3ICw2v7NyX00" +
                "P+ipvWySRpkYbC92e5+q97nv8+XU/gd67mWn0DE3nah5qT+kYK" +
                "j/6EWzT63TrW6kb9JsW1NObY/07IsdpnVX/Jh9NhjN7936uH+f" +
                "YMTuCPzT/mmgwAMHFGXYp36cIhoiheeLf004NAvuT61pBKnncb" +
                "Hp8WA8dLQmbDlmad+ZT2co531n6/MogpTH1+xTJAub1zvv+aTr" +
                "XaFzQY7rXZRJluvd1pyfhs8noK4Xyulve5nU4zw+fKE2dbpQZZ" +
                "2GV5I5Y3YrSde7Ijk1fslRp5U6zyfvubrMp6RM6lGn4Im61Cl4" +
                "vNw6zRU6/w2/W5f9eNmZ9JpPzVup2f1Y/Xx6ZW8/mZRTp+Apxj" +
                "2ZWqdztbnendu6+TS7KzW7t8z7zNndRXJq7el57ziQlEnPcVrd" +
                "ZwYTjHs6tU7XazOfrpc8n84UOnu+X5vzeAmZeEtAvSWok7ekeK" +
                "WjdiY/KgEe36mUe0Q6MyrluUTqeSxoJisqh57ZbquePzXXg8lq" +
                "193CHsvPnxb9RaDAA+cvessoATvJS4poiESRKY7UoT+19papF9" +
                "fzuNiUj7SJxkNHyysAvjxXaS/nk3cixwo+Ucb9XZ7nmXmyz7fu" +
                "vOkc2U3X5v5uuqo6xdFKfoqTccznbfkUGU/S88zgha5F5ueZ8X" +
                "stvr9rfWu0tfb9XT/7pzp/LxUcszynZ7wZpKrX4QYjihKh1ajy" +
                "R0ywppE4Ds9CzwFtuBfXUj/SBmU8PT8dm2fOI9j8HiF53QXHq1" +
                "13wUC2dZf1aJxqnAIKvOK8B5QMJGAneUkRDaw4MsWROvSn1jSC" +
                "1PO42KLsTTaIplcAbUzRk653wYvONj7mLtrFm7838Xo3I9fd/D" +
                "351938wXpe7zKvuyuNK0hVT3GKRwnVUt6MgZ6dqr9swqFZ6DnQ" +
                "TFDO9TwutoWdZhtE0yvA48noouJv9Jh79xW4TreqXXcLO0vY71" +
                "4D6l1r3gSZ4pWO2pn8qAR4fKdS7hHpzKiU5xKp57GgmayoHHpm" +
                "O8O6O9k4iVT1FKd4lHBtRFtT1BvsKQ+9MJ9VHYdnoeegJN4qzZ" +
                "Lr6Qh40+Pp+cXz/NUkTBlBrLub5ezHDz1S7brLGi94M+f17/4e" +
                "dXkttXKvhxka9xOHKt5lpMUDfda8vHFvHKnqKc49FlGUcG1EW1" +
                "PUW/kjJljTSByHZ6HngDbci2upH7Yoex5Pz0/H5pnzCGLd/Qk9" +
                "97izjQ/72SfuMy/ZvL8rtBd+sP99ZvtOu/tM/6x/FijwwAFFGf" +
                "ajV2uKegNFNEQK+ztMODQL7k+taQSp53FJ22G2QTS9Amhjit7r" +
                "eaa1OVv5c1/3Gbt2/pA/BBR44ICiDPvUj1NEQ6QkHKlDf2odrF" +
                "EvrudxsbmHzTY45k6dDktsmSu3754LHnJui2O+kdHu4Yz78Y3G" +
                "BlDggQOKMuxTP04RDZGScKQO/ak1jSD1PC42d9Jsg2id+TQpsW" +
                "WuHW6zsRn2YhrLNtVL9akE7CQvKaIhUhKO1KE/taYRpJ7HxeYe" +
                "MdsgWqdORyS2zJXbl3ser/53PcHXGe9bcv4P5napkzuR8Xo38T" +
                "+v09GMdTpatE7ufi2jXZkQOvvx9nhSndqPVlCn/SlZFv09Xfd7" +
                "Tjf1iXat/393sFz8stade1fF5/F1uyPwr/pXgQIPHFCUYZ/6cY" +
                "poiBR+vnebcGgW3J9a0whSz+NiCwbMNoimVwBtTNETz0/7nG18" +
                "2M8+8bnKTzafq1T9e5X2Y9meq2T9vYq/5q8BBR44oCjDPvXjFN" +
                "EQKQlH6tCfWtMIUs/jYgt+Ndsgml4BtNGj+yP+SNiLaSwbUS/V" +
                "pxKwk7ykiIZI4d7/HRNON1ei0aPQCFLP42LT48F46GhN2HLMHW" +
                "7MHwt7MY1lY+ql+lQCdpKXFNEQKfx8N0w43VyJRo9CI0g9j4st" +
                "uGG2QTQam2LLMXe4UX807MU0lo2ql+pTCdhJXlJEQ6Tw81024X" +
                "RzJRo9Co0g9TwuNj0ejIeO1oQtxyztO7uRveVcf4I/qr3eLfxs" +
                "F8874B1AqnqKUzxKuFanYE956IWfyZc6Ds9CzwFtuBfXUj9sej" +
                "w9Px2bZ84j5J9P/dy3+F9VO59sx/MH/UGgwAMHFGXYp36cIhoi" +
                "hevuLxMOzYL7U2saQep5XGwLd5htEE2vANqYome/v+vveQGZd7" +
                "/P7vbnq72/S4uX9XlB60Zrs/fzArvrzn274vsW2/H+A37O2VQ=");
            
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
            final int compressedBytes = 1739;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWs2LHEUUH3NyXWRZ/CKHXYKwYBBdVMhFD3b1tLofhj14EU" +
                "HQ3LJZweQSlhicmd6PRtAcNq5xD3sRQgIreNCAqBc3HoIHXYiw" +
                "5BRM9j/wsMnF7qmpee/Vq+rt6a6pdeyhX7/v95vXVTU1PVOr0a" +
                "MxXuvD0bgbfFMher/3mCr1CmHqV5+ue+7T9cHs0+oDv31a3R/Q" +
                "8XTN83i6NqB9uuq5T1d99UmELvtUKbpEn1yi9zme6n/77VOVeh" +
                "YUP3eu36bnD6Y+NX5hmt/a9Pv2CF/s6H7vXG8ZMvxYHl+w2Pgu" +
                "F/+v5iij740ud7PEGN1QVGyoPokN0FI/UxzWcK80611znsxmzo" +
                "plOp50O9ap+jwrfVfm91du3kWnq41THO9/faqKvoc+ndFtzccL" +
                "ZbjF45sjVTC1nsizNoeNfTpzAMqbzvo0X3E8zR/qeJr3Np4WKv" +
                "Zp4VD7tOC+N+K+ouJ+4xLWtdblFevsssqh63Tt6kNsOzhr66t8" +
                "O+hUJZ61M0c3wdPscej7p3XP+6d1X/POcZ/WPPdpbUD7dNlzny" +
                "776lP9p0H+fucSfXs9mxJT4RFFJSeljI+vgAZb9bhMk+nCI7Ua" +
                "llUmpcXR2TWzZVZswZ7SJ3kaR9FMgETFypPXy2hzU1WTdQEtz0" +
                "ml9nVfUbHf+KfTv/1Mjq8IdC+Fdl8hDmu4l0kLNnNWLCdjeXas" +
                "U5V41u7n3T5GZcNVYHx9xrky0cyyXfN6uK4n4vS116WSk1LKJ8" +
                "8iDbKyuLiTay+lWO5kUloSHbe10ootuGLbp3UKR9FMgKQbu6fQ" +
                "03oZTcfTXjdirx29R3JT5EhifVvmnPV712l7tMM7eaOfUcmLPe" +
                "zxVxWNVtV+PFqVsrRhP1Mc1nAvkxZs5qx6vN2OdaoSz6rnUpzZ" +
                "r8B9+IJzZaLdHStPlolKJvu8/n2OObFVNppZtipg2nIVUwVF8e" +
                "e+S93vo/i5b1eX+9w3eakCwhLPfVeMz6z4c9/l98thar4gr+Fo" +
                "OJrtn1yN0yxb2SN5zlW95qar99Pt01A4lCJ83lmfhvzun8z1yv" +
                "cpakUtoJKTkpRBQ62cKn8sKy5dGRo8D0XBMYAPjaJWHAcnr8fx" +
                "8dwUOZYat7UZ/GePa8dfBefOK4bYP3qstdPDXH3Z8efbqBgFKj" +
                "kpSRk01Mqp8sey4tJ78jXPQ1FwDOBDo6gVx8HJ63F8PDdFjqUq" +
                "4ynbjxcdT0u3PY+nV92Op/qJ+glFlawkRUEHPI6jFLJBJlse3Q" +
                "bx2BtX0O20LpzJa2YfyMY7AD68en22PptybdrWzcqX5LFG+emy" +
                "TiEbZLLl0W0Qj71xBd1O68IZXTD7QLbOenBBz61jlZI4J86lM7" +
                "BDJSclKYOGWjlV/lhWnDkPWiOQBXvSjDoKqKLi4IzO6/U4vnaf" +
                "zttyatJZcTblOlRyUpIyaKiVU+WPZcWZ86A+IQv2pBl1FFBFxc" +
                "GZCL0ex9dex0JbTr2Cj98R/B/RJ279RCQioJKTkpRBQ62cKn8s" +
                "K86ch6LgGMCHRlErjoMzuqjX4/jafbpoy6lX+J+Op0W3fqxPl7" +
                "ozd2qQ+7TywG0+cUwcAyo5KSUzGQUNtXIq4yGn8saVaB6KgmMA" +
                "HxpFrTgOzmRar8fx8dwUOa1gG0/i0UEeT67RxydjQeS3Kq0KT1" +
                "mqTNsshTDm/nd3ea0XJGX9sj7Fb8Sv8z6Jx0q8o7qtT5XuZW6f" +
                "4jeN46kg+jgq2M8x+QK50ngas1dxnzXPWrReYb9x+QK5Up/G7V" +
                "XcZ82zFq1X1M/t+mStMl2/0695Z3kOcsfxO7CuT6Xu/LB1HR/2" +
                "uz4VrVcWF+wLSv0KMWO9v7t+9wWu64lJMQlUctkrnAgnxGT2u5" +
                "TSZDrF6XFiUuYKJ2o16aVyS53S4mgxSVFwDMoneZtGUSuOg5PX" +
                "y6j8vQVQ0Q7QnHoF6z7zuMvx5H2feby/+Zsn5TU8Gh5N7+Qctm" +
                "Wa/CPz0L2kfHBsznP4U73HmOvB73e94hEzYgao5KQkZdBQK6fK" +
                "H8uKS9eLj3geioJjAB8aRa04Dk5ej+PjuSlyWsE8nlwfyTuenx" +
                "c84jqjn/9hVEJY4n8YyXtGX/Y/jKUv/1t9Eu/67ZO5XvX/qwT3" +
                "6FVxGQUd5U3xNk1ejuCeLSv4Yx+MKR8PpjZc9tomS7BLr1iPdY" +
                "Fl/xYcsK/Lswe7wYG7QuyDMeXjwdSMJK82tdjmnZjT5138YYV5" +
                "N+d53s0Vm3fxB+7XJ6Sx9Cl6xtanzOKzT+Z6vE/Fcdn6FGy7HE" +
                "++P++CbV/jKdgZ5H1BsHN4+4IC827EOu9GPM+7kYLzbqRqn5KP" +
                "B3k8xZ863j/9CzzU6FE=");
            
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
            final int compressedBytes = 2124;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWz2MFGUYXgpbG4iFIRRow5HYGAoC6O3MXM3fafgJMYZCvQ" +
                "QrzYFg9HZh1zUWkmhpRASUK4gaxaCgHIqFWhhAL3cGiUZNLCwo" +
                "NCQQ9Zt5553375vb3dnZ8WAm33vv7/O837ffzM7u3dVq8mhsqw" +
                "1wjN3df2Q4R698vebVm/UmSdDAAps8Mmpl/HNsOWFidtrPcosj" +
                "u7A9UI6sklFeR8Py4XyoP+rLh6kZiu+n5kTvucGOavfTsPlan2" +
                "dMG0vte2PF6+Tl6+wuC7/xeHYtfG1YJvpBOnggN/Lc8NfJdl/u" +
                "MbUiY7po1unpouvUeGqQdWrsKbBOF737aU/h6+yX1qXWd61rbo" +
                "VOuXG6dcWzdp8Zz1cLIP7s8f0QnRjgXnB5wehVn7cbX+unRP44" +
                "+H4q94iOVXt/Kp9v6lz6M9lPvnXK209THwnft+lPz51h6pPo7Q" +
                "E6fH/B6BfedfLyTX2cael1efBawfeJXVYr4d1nV9XVZXZf3XUn" +
                "7+N9Vxe4+47+U25esDPYSRI0sMAmD49y249Blek7zT4fDu/C9s" +
                "A7Ib+MS14a7aX+HEKzKyD5pBXNRrPuak5kcl3Pwgl6ayV5ME/b" +
                "WhIaITmub3w42d2ERSxL60teJeOSl4bli2XjTWRjdzKFzfshy1" +
                "x3rw70NP9n7r79vuLn8ZL5gslgkiRoYCX6PHlk1EqoJ0zMTn3z" +
                "Fkd2YXvIcOZllYqyOjYMH86H+tMrIDE1g3qKnh7O69s5UO1+ai" +
                "8veT+tDlaTBA0ssMkjo1ZiPrdRc+v0vMWRXdgeKEdWySivo9Ee" +
                "0Xy2P4stO5cM6v7013C+V6n6OHSl5Of7VdEqlGijhZJ8pPM6kq" +
                "PvcjRCysPRMeDULJxBxykmR9yJL4fQ7ApQjmWPRqIRpyUy8Y3A" +
                "CTr3YJ62uRw9ydEIKQ9Hx4BTs3AGHaeYHHEnvhxC49wcW/eKGd" +
                "V8vuu8XO3nu/bS3j7fLbbPwQN9liqwTsETZa+TwzyCMjjSaKIP" +
                "bIjxPF8d96BNP7lXVsQxPyq3pUfHJRcOXxb3o+bP81SuCdaQBC" +
                "21DgdrOi+RR0WNTH4eJkzMTnkOWxzZhe0Bc5q7ZZWM8jo2DF8s" +
                "4XOL5fZhKmtdsM5pqQQNLLDJI6NWYj63UXPPT69YHNYri3BGia" +
                "i7IBaso+E+B6+z3cr+LLbsXDKo7zCy31QFewtcwXtri+Qov5O8" +
                "+3iw9ra+j68d7vtd698hfe+7veLvfbeXu07BeDBOEjSwwCaPjM" +
                "ayOcGrMZ/bqLm+d1gc2YXtgXJklYzyOhqWz/ZnsWXnkqH457u+" +
                "Ph9FFf++pWS+aH20HiXaaKEkH+nx2Zzg1SgJjZDca/KhD4d3Ie" +
                "t5NmfQcclLw/LhfPhsMY9j6zmn1uZos9MSmfg2wwk692CetrUk" +
                "NEJyzwVv+HCydWIRy8IZdFzy0mgv8ecQGufm2HrOqbU12uq0RC" +
                "a+rXCCzj2YR6fbT6waJaERknvdzvpwsl5ZRLMEZzmDjkteGpYP" +
                "58Nn616/tzS2nnNqbYm2OC2RiW8LnKBzD+ZpW0tCIyTX92kfTr" +
                "ZOLKJZgtOcQcclLw3Lh/PhsyWL88s5p9aGaIPTEpn4NsAJOvdg" +
                "Hp1uP7FqlIRGSK7v8z6crFcW0SzBec6g45KXhuXD+fDZksX55Z" +
                "zBGrs+dr1WAxkfsYYWSvKRzuukJDRCcvv7uA8He+URy8IZdFzy" +
                "0mgv9ecQGufm2HrOYIUXwgu1Gsj4iDW0UJKPdF4nZfNZqme423" +
                "w42KtF5TZn0HGKqbHNn4NozWfS1+8dja3nnFoz4YzTEpn4ZuAE" +
                "nXswT9taEhohudfkig8nW6eZhVg4g45LXhqWD+fDZ+vD1nPW+d" +
                "3/viDc1+1pLM7wZ3WvLffoxofxon2F7+VHwqlutXGGzgK7W203" +
                "9n5rkC8PMYtP9cpT0ffjH9zu34/3cd3t7/ra7s/L6l5b8nW3v7" +
                "d4731Vs5/CR6rdT34+vZ8O/r3Yft8SnCn8zeSZQt/Tnentumvf" +
                "s8juT59WfH86f3v+/i7cVPF1t6nH/XR/+evUfG2hdUoyXncdTn" +
                "rXabLidZpceJ0wXrQv+nu69oPlvfs0XhjeO9uL93oZS//fkIru" +
                "T+eq3U+NS8N9fmr08fd0/fz9Uzhe8fNTj3ydU4tsnSr+v7Ky+c" +
                "Kb4U2UaKMV3gyOkSe2SOd1JOMMyCKsIPlPE14rWSQrj6IPEXW1" +
                "ZJfD8uF8sNP0Octg837Iquz/W5ZUe3/y8xW/P4U3whvWBl94Iz" +
                "gKOozYQh0zKBszgqMSE6qwFitQ030QLmVALe+VetJeYtV4NAOO" +
                "SR5Zzz1JJ7fCW05LZOK7BSfo3IN5dDYneDVKQiMk9/re5cPJ+m" +
                "ERzYJxwpNRXkfD8uF8+Gx92HrOmJF33dWfLPO6G+jJpcB1R92X" +
                "dN3NhrPWjkfnMkTQxjzy8myOpDElOlT4eTlWfo7uR3aWPiU/lt" +
                "fnQmvh9ySdzIfzTktk4puHE3TuwTxta0lohJSHo2NUz7M5g45L" +
                "Xhr1Zf4cQkv33TKNrXtNrblwzmmJTHxzcDqEB8CHnnCu8yvpvE" +
                "5K1A49ilkSOZw7NE4ImhXrOQtn0HHJSyPu3pdDaJybY8tebX5a" +
                "dTxb6fvyIrn79njtfziQVbLr7gdmmQ6nUaINVn0l+NATTnd+J5" +
                "3XSYma20/ThMtx3H6azothPbc5g47bGIy4e4vY+Y3Q7ApgLWew" +
                "+WlV7v+zh13/sz48MZwd0/mjF1bJXh/tDbvXvDv17+zrQW/PBf" +
                "48z6txMjyJEm20UJKPdF4nJaERUh6OjflYOIOO2xiM+sP5fJwZ" +
                "8mS97FXmZ+u8onZHHPWHys2r/Qeg2kyS");
            
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
            final int compressedBytes = 2095;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW0uPFUUUvogxIRozyEoWJARnoysSYlBCuH27O66APQs3bB" +
                "QTfwOX1rnMBROFCZAQMD6Ir8jDiCaOYxAF42MphrghQghxJjFh" +
                "I2IGtatPnz5fnar77tvOUJ06t87r+07V1K2p6UC0PlpfgxZlOt" +
                "lEik37XA9bfc14KCPy8iJu5xhdj+Y3bXq+c53YphcQ3623qGYy" +
                "mkxHmcxsk/TQGC0cp3UtBU2QOuFon+RjNDJov80rPQj9MYJGje" +
                "IQW9eaa6uiVdYKZrrp8QvkYZ3jxIrRYmOrdz+t4gw/L+J2jtH1" +
                "aH7TsHpdZ6fm+iU7vBBeqNVImmZGrLEUm4wxz5aCJkjp/r7rw+" +
                "F6XFTUkUH7bV7prRX+GEFDbsTWc861uXAuHWUys83RQ2O0cJw8" +
                "yR7MZilogpTu70UfTlHrXGcW9gueG8280l0+ng/O1oet55xr18" +
                "Pr6SiTme06PTRGC8dpXUtBE6S07rs+nKJW8LgsyKD9Nq90l4/n" +
                "g7P1Yes559qN8EY6ymRmu0EPjdHCcVrXUtAEKa37Lx9OUSt4XB" +
                "Zk0H6bV7rLx/PB2fqw9Zx1PLXWS7Ul1oL56rI6t/rt+m2RNCJt" +
                "+p6RYrG9rqR8weRoZLJx7CrcGiTGzrK9mCd9elHzufW52HblqA" +
                "Wbgk3p6ueSRqRN/2ukWGyvKymfMySz+Ak7OPDTBw9G2oi6CmHh" +
                "POmt85rPrc/Ftiu3GZr52d88nfbPmuuKG0ixc5tf0eerxwrL5U" +
                "yex53Z/Cn//MHdtc0vRtnzzXNdvd/4rLH3e9f8vBhdyud0pO8q" +
                "Oq3TVr1OkLP012lrf+s0QBWd1mnLsl6nLf2t09Tzw9W074/uO7" +
                "f/1n7g//wtGc+PF1/Wqf1gbRm39sqSbycbg40iaURaXDdSLLbX" +
                "lZQvmByNTDaOXYVbg8TYWbYX86Sb6t1q7fpcbLty1MKFcCG9cW" +
                "Yyu3su0JMybSMbWzhO61oKGkfZyIijfZKP0cig/TavdFO9L0bQ" +
                "kBuxda25Nh+m32SSmW2enpTpWbKxheO0rqWgcZSNjDjaJ/kYjQ" +
                "zab/NKN9X7YgQNuRFb16rj8/Pp6eIvmH2lnhcPVXs+tdaMF7/5" +
                "Jo8ae0ute6radSq3+vwvnJvxbDxrpOnpnp01tvpN8uW/Z/MI9h" +
                "vJEaRzNGdQjokiq0QhM3kZUeMxJvEyK+FiFtfNc2BUidJoMj+O" +
                "whqIwVexu5+ir7VvarOtJ3sG2E+taveTW/2I97Fd8a76KZY0Is" +
                "346qfEgl7MozFhmSy2kp0w2Jo/1xjF+IhJcK3IDAd5bD/OgHOp" +
                "21GCzWw04nypACtHrcs5/lqp5/jD99c5Duv0eqn79ki161Q2Xz" +
                "QRTTR+J5n+lshGpJkxWjhO6yQFiXWyEJKNwx7yGS96XBbyCx56" +
                "hZdzqbt8Rpo6OMOMuC7GphipRzT9viB51HO2l/C+ID5a8fuCo1" +
                "W9Vyl3ndqrq10n//k0yjqle+8Oy8YdXqfGHaO3HyEfxvny0OJG" +
                "+azi86Oinuzu5kcbM7mobqRb/TD7KZwI0+9k+zHcT60Tw++ncK" +
                "La/eTnc/dT6/iA71dm7E+0o037e9n78QczvbLtGKypez0o/ZV0" +
                "4/Z5gsP2J9rRpv297P34g8O9su0YrKl7PSj9lXTj1p7GLZaNW8" +
                "X5dEusdpwvDy1ulM8qPj9q/zramMlFdSPd6pfC77vG99WeT36+" +
                "pX8viGeGX6fk5SHuTzPl3wsspHUD1D/A+4L4WMV/t4yZL3oDR+" +
                "21ZaE2viujpgHuhF6+9uOlrdOh/PMEj8rYT9Gh+PjoNQ20n46P" +
                "eT8ddkdloMYny6hpgHU6Wf7aJO9l8iM8n5J38s8P4exa7ILxtt" +
                "LfKkZnk/ebv8Xe0zY5lXycnFa2T50z82636vfmZ0PyAWCcQ77k" +
                "3RHW5kzySfdzPLpY4lutF0f6OX45xB68WPL37GB0kCXrrEUHG2" +
                "fEwnFa11LQBCk9V8/4cLAKOx+jKVfw/DXY3eXj+eBs7RXgXHvO" +
                "Ot7ZT9+WeS+ouvWufrDW+lmtU/G2N7pUW8at/Oo73cfbT+j7eO" +
                "vX4e/jI1U4zHu6v/u7j79ybdT7ePDMct5P5Vdf0XvfJyveT2uq" +
                "2k9lttHuBcO06UZ/ce3JAffpVfsT7WjT/l72fvzB1V7ZdgzW1L" +
                "0elP5KunGjJ27HbZasmyfcEG6I2+2nxGJsMsY8kSaCogQr3GDG" +
                "mCuYWAV7kJFsyW7Msv3CbneXj+zMRlXZKyAxmIur437vwrWh86" +
                "4g7Pn2wEToKNLDtdV+73rxDVpPfCA+wJJ11liKTcaYZ0tBE6T0" +
                "fuzFwSrsfIxGBu23eaW7fDwfnK0PW89Zx4/zHE+/639WfC8ona" +
                "+ae0Hwz/AVTm0b/F7g5yvvve/Uc7X7orUW+4y7N+TuKv49XfDj" +
                "sr6P91n9/iH/18Ro51O8enDPeFq/fP3GRZujzSxZZ42l2GSMeb" +
                "YUNEHqhKN9ko/RyKD9Nq/04Kw/RtDyfXdWY+tac+1KdCUdZTKz" +
                "XaGHxmjhOK1rKWiC1AlH+yQfo5FB+21e6cGsP0bQ8nWa1di6Vj" +
                "t+3PeCys+n0+XGOetUvKcLLi/rdRpz9bJOjf2DZw+TM6Z1Wllu" +
                "XLQ92s6SddKCX8jGFo7TupaCxlE2MuJon+RjNDJov80r3VTvix" +
                "E0dwUkxmWPdkY701EmM9tOemiMFo7TupaCJkidcLRP8jEaGbTf" +
                "5pUerPDHCFq+n1ZobF1rru2IdqSjTGa2HfTQGC0cp3UtBU2Qar" +
                "X6og+nWCfwuCzIoP02r3SXj+eDs/Vh6znnEf8BWj9vCg==");
            
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
            final int compressedBytes = 1603;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdGj1vHEV0yyQWFVIkLIQElZvIET8AvHN3CUTCKUAKUoQpIh" +
                "pEElK68304Xm8VTjIKUqSIGBvHhuBEfAhbsQsERcQfoDGmwrJd" +
                "uUBgUbCzc+/em53Zvbn9GO8yp52d9/323Zu3M3PnOI0Dp9+8Fx" +
                "sHHBY47BEXpVFpHFOsaP4E8FN5Kitrgk8Sj7jPjKrWgid5U/aW" +
                "PoF50z1lT/8rFGI7Tk4ti6Y0snqZ9F50JjtMgt9wCmidS/6FDN" +
                "K14WW8f3J+giBOnQudCX2c/NEhtdXj4sQeFxWnzkVt5mjtte4r" +
                "0o084jT0fH4uLk5xlKLiZGrPmO9U4xT2YiQgASOGUilMe/YRws" +
                "BNLUX1RGmyD9QTxMt02Xe8uCc6HtSmRkC2F7XefCr4m4+C6/vm" +
                "S2osm1sK5pew/07C/Rp4dyu4P9No2MiSpc3EOdv8STvvbml5f+" +
                "iPfs5Wr1qrUjV8bzjpiWWnJK0oT9wd+U7xFOfuJMsP0q+nuTuD" +
                "/UMe6lOyP7TXe5JkW6YkzTvvfTrvZu8OmncTv5Vl3nFPTObd7K" +
                "fGXhjXp8FxYjdLU59umsXp9lTK+vRupqrwVWnqU+6exOWTx1K8" +
                "724UkU+3X0+RTzfyf9+1vwz7tVC+F6f2Yu9O3n/NfxN0PAi/xb" +
                "U+/Hl/tN5eaf4RI7XU/rr9KIL7Vnm6xD3ITG/P0H5IdDxGTwLo" +
                "i/TfUfub9hPtvqi/umULwb7l4pC7z4XhKQa+XkuxD14odl57/X" +
                "0L66bwrjs8pZhWlD23K98pnuLcbrL8IP16mtsd7B/yUJ+S/aG9" +
                "3pMk2zIlro6z6RR1fLo064Jpszre+d1wnk1GNN3Jcq6SMA/WLc" +
                "+7dbNzldTfXT+f6q8OrK8fOqVt/uVi9ZN595pT4Za/91CfonFK" +
                "re+Z9vt9J8M53Z+J1EMddu55w/p0MGyc8jh/iouT/TruTpnFyZ" +
                "3KOu8M1n9D1Ce2lXoGbdmTSpNP/lyV82n2Sr7rAmWd8EFB+bSd" +
                "Op+2W9fTSNnKJ/dMUfnkexbq05li84lNqaMc3tJTtqXz9F77ll" +
                "0tZt7VrthdPxVtT9635BintyzHqWB73vWC4jRpOU4F23Ofnpz0" +
                "STxHWo/xnz7ex0E1XJFq48rA6rmicgl4sGyy1vg2M2oug1gYmf" +
                "pVH6uPQQ8wQPUxtowYDuGYymHPOQQX6mLhL7NUVrYiW6VUwIHG" +
                "qLRsXb5Ue/A84GkvTopu6g9CygmNNK/ZYjyk/b4WVS4BD5ZN1p" +
                "qPDJ4/AT2tX9nPC0pSn17Ily9hf+dXeX/nz5utx7PvW6p9ruK1" +
                "bMXJPV3p86fTeccpsj57oo7K3fR+umcN69PZzPl0rtL5dK7YfC" +
                "JxernS77vcvbdTx9lm6tXTZqrfOTfN8sm7k3nena/0vDtvFifz" +
                "c7r/57pgft8sTv4nhm+Lq7Wr0AMMEPSIwzGVk3vUhpqC/fWqTg" +
                "/1It4KtRClqzRxJdmjlnXy8jMDh518cpcsz7ulaq7H3WXLcVqu" +
                "Zn3yH9iNk9nv5p2/hs+nvM4LYv5fkCGfivx/gXfXdj4l2tjw18" +
                "qVTye3zkyOE2vZjRNrVTNOjXt246S3V4Y4Jdfx2tt246S3p6lP" +
                "i+WKk+31eMPwf6x6vpid5hH07AjixI44PO8JGuXTyVGMyqXDIk" +
                "2vlcLy/+x1mgAHllStorXuI6fqfbp88n+scj7NHdtaZ7JKn/uy" +
                "3M99Y/Npo9LnBZfyzqcg9sfQs+N+fTpGrMynk6MYlUuHRZpeqz" +
                "lMcWBJ1apyqt6X4X03e2g3n/ztfPOp1qq12L7og+iGIwHxcfC+" +
                "62OALwqLHjUBLDBCE9UMvEDjVFUrhdvXqJRMR7t8BPaF9zIP74" +
                "P33T5IsPAkj3ov7ui59Bx7tT32t+gDqXAkID4WmIBzD/n4R2BA" +
                "TmA4DnUIGDTJevDOaZxK9aIV4BF01Qu0Atr5XVwyF/SoTdhFb5" +
                "ETPZeeY7e2yw5FH0iFIwHxcecz3gecu8jHPwIDchwO9REdAgZN" +
                "VDPq41hBpRTKKXj8h1RK1oSegKy4VHu8D/LpECRYWAnAW+REz6" +
                "mF+nh93HFEzxsf8U9tpDZSH5/3EAN8UTjaozbgCm2N6PTA7KcU" +
                "1Qq1EKXLdvFS7fFe/F+FWlZ1U3+I9f8A9j97Ug==");
            
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
            final int compressedBytes = 2315;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW01oXFUUHgwulFhMgtjSJhUJMYIglCKCLWXumwEFN4K4sF" +
                "Uo3dRsuuuiFiSZSTs/UFwVu7Fk1UXrb6kFtQGr1qJp8aeWSBbd" +
                "dNcuAlKlDSO++8477/zc+6bz8+a1eY975vx+57yTO3fuezMpFO" +
                "Qx+yFytbOFzI65Dwo5H3OHBos/+yq8BsPBcKFQPZEVrkXr9Wh8" +
                "klW+uZN9dOZC/PppOL6anfB4LDqaSxE9J3RL8evPHoSvG9/0Ue" +
                "EXba3f+7RHx7y+5xPux4ezT8VWvn3y51sHfbqTc5/uZNun4sHi" +
                "QaLAgQQyaaTV0soMj0Z/LiPnx5FVuDWAxpznVUo7vwI5zHmdz6" +
                "0vWvs+S8PUGdLmU3Gpv/lUPcznE/esHsphPi11Np/mH+/xc+k/" +
                "l0s7KjOd49Y35rsvKH2X+U4gl/WpNJTv+uTP1/v6FNwN7rpySL" +
                "eE5916zfI4gi3gQVHoncSGHjaOY8ZRW9Af45VXgkh+qKvs07VS" +
                "TVpLWTWepbR/gnp0L/yaqJJWEH6CAo10rficDM+W0Fhdi58YRz" +
                "TymEQ0jIr4SR1HObnMreTDo6SdbGpM+n2SbJM8N8eW15xIK8FK" +
                "yEU00q3E50R4rtj9eKKxuhV+YhzRyGMC0TAq4id0HOXkMreirr" +
                "HIo6Sdsqsx4fOB+URVocSxRRdWyEPt7F9K7u9+6H4lKc4MYk1u" +
                "/PrgKwlOB6eRogxS8Q/QoQb9tKwpctXD6CWRg9PVQ4Sgbb4sPI" +
                "O2uzYYtnoXkV+z2wGMldes/eOoU8lf5Kc0S2rPhQffP8mjmum9" +
                "PGaV2XX1WR+15FO99ks44z/PCtecLeR6DDpfLdnH1q5kuc/M+2" +
                "j8nvH6tBgsIkUZJHMcdKhBPy1zCjGIhl7R3/e4D4dXkZ6FZ9B2" +
                "ssnh5gM9obkdIB83e3AzuGk+BhqiRxxIlgdKOuJ5nKUhWsyjjN" +
                "4WgyNzHGuzVpQt52bhGbSd8mKsWzf4gB2zAYd1IbZ9jTrDYkGa" +
                "/VPt7H8Ts/e+69Ps9Q7Xiy89sV1+3s928V7y5ct0fUr6ZHr4VD" +
                "KHHpb1adCVzL8yGNzgXL596jRf43qP86mLuG4+78yFnmfGhfyi" +
                "uujT8mD6FLyd83zKOF9xW3EbUeBAApk00mppZYZHoz+XkQvrft" +
                "3FkVW4NZCPjJJWHkfDzefW52LLymWGtOd0jX/0c7r5j3p/TtfX" +
                "k8QentM1OvweYf54v88zi+9k+Twz7z5R9e37dOTdDt/Ht4PbSF" +
                "FGKbhtTpHGSsTzOKLWA7wIy0T3pzxWZpFZuRV1iKijZXY53Hx4" +
                "PVhpvN472Lwekpx9wf5k5v5bWMdH4062eOXd5d1EgQMJZNJIq0" +
                "vRn8vI+XFkFW4N5COjpJXH0Sju1fnc+qL35940TCXtKe8JuZgC" +
                "BxLIpJFWl6I/l5Hz47A+MQv3lIi6CsqCcTRq13Q+t75o3t1Lw5" +
                "RSaaQ0UigAjb7PGYEz7PQLoEMN+mlZU+Tm30IviVwamX+TEHRW" +
                "XxaeQdtlXhq2ep8PofHcHFtfcywtlcLPKaCRbgnOMNMzoEMN+m" +
                "lZU+TCPi0RLo8N+7Tkt1E8z8IzaLvMS8NW7/MhNJ6bY+trjqX9" +
                "pXDlBhrp9sMJPNegn5Y1JTRCSsPRNorn3jyDtsu8NIo7/T6EFq" +
                "9POzW2rjWWKqVKyEU00lXgLBSaT4AONeinZU0JDb0kMsfRNorn" +
                "3jyDtsu8NI6e8/sQGs/NsXWtIJlNZlO4i4gpcCA1N1hKGml1Kc" +
                "RjBEUmd6YODrtrZRbuKRF1FZQF42g0hnQ+tz4XW1YuM6gda7If" +
                "LxfX8/4p6+rNLrOLKHAglXdYShppdSnEEyZ680wSR1bh1kA+Mk" +
                "paeRwNW71brazPxZaVywyd/16l/sj6ub8rv9/Z/Z3fr5v74OZW" +
                "p0+Prp8+Hb3XWZ86ft8Nm+FgCClwIFm+XiMNt+o4MwxYwVB418" +
                "hkREItj7av1mat3MI9waeyj0dJJKoEY2G4+SydO4nZIC9V62JK" +
                "qc1zlR0Py3OVI7t6eK6yI9v5VLpRumH+Bhp2LuJAsjzXoJ+WgR" +
                "ISyqABJImDFrBZK7e4WcBOeNxKeTEWhpvPUlsHRlgO60Js8KF6" +
                "SCofKB8IV7OYAgcSyKSRVksrMzwa/bmMnB+HrbrMwj0loq6Csm" +
                "AcjeaozufW52LLymWGtP1Ttt8j5H00x7LFqz8Wzr21aAau2TEf" +
                "/x7GrFm5XgMb6tRnwJrWAob28mmjZ4KXrM2PymX8fabfznWYyU" +
                "WFY+4kebrVt923vlh+0dxCChxIlg9xEg236jirsTpzCyMQG5Ak" +
                "DlqsFqzcwj3BB+yEx61UCcbCcPNZSmiQl6p1MaUU+baQmlY9/j" +
                "8j07Jy9YRh/ydi1P+MUBzXuF4+Ldn8qFxuPtXOznWYyUVN5lOL" +
                "V5VWl+eTUv0Oo/5kwo12sj7NXu/jPb+hy/1mF7/DaD494N8XxO" +
                "tTMBqM2vUps+9nR33r0wC/Dx71z6ee76u3l7cTBc6ewVgwVt5u" +
                "fxeNGm7VcZZCPGGid1z3mIsjq3BrQJ/mRhklrTyOhpvPUuiTm9" +
                "uHqTOk7cfNG1nuxwktn/s7fz53P1690eOaIZ5ImTOZPb85k2+s" +
                "P6afKuR8qm9O7pCurOvvza9ke3/X5j74WpZ9qm/NuU/Xsn7fpf" +
                "bpqu5T87lu+vSA/0/x6mDXJ+xTMBVM6X2B1dzn03jK9QL5/rFt" +
                "9mn7etgXTLXfF3RfT+p8uryu16fLGT9/OlY6hhRlewbjwTjoUG" +
                "N1xPM4otYDvCgyGI/+fuM6jnJymVvJh0dJO9nkcPOBHtGgKh+2" +
                "vGbyUPuCzYPYHc+913y+j/fdt93H1F7L+hry+f/g5ss5f48wlu" +
                "06Xl4tryJF2Z7BdDBdXo3uW1ZJRzyPI2o9wIuwgunofTet4ygn" +
                "l7kVdc1neZS0U3Y53HyWxvctSVWyAxjLM5BHu+eZ5q+s3nf9IP" +
                "US64/pvQpz0VxEijJK4esyadBPy5oSGiGF/LIPh1ch44X3Mo+S" +
                "dpmXjWVvnct0zW4HMFZeM7ueBaRmAeeTWQAZbKhTPV7QWoiR3o" +
                "jkw7E2PyqXpUbbZS4cPi+uR87v5zn+B8vd7XQ=");
            
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
            final int compressedBytes = 2565;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW02IZFcVLmMWCTIy2CPu0gsDrRMI6MbszPspMwzqKj8iMU" +
                "OcOGC1GzcGNMGq7uqqqalWgwoKg3Q2LmYzIOoIgn+IUUTIpHtI" +
                "BtJtJpmNSOJCMouEofXed96p85177nv109WV9HvcU+f3O9+7fd" +
                "9Pve5uNGhLXtGfDfCjL4yP808ST14ZV61zkFM9H5RxJnW9MdLc" +
                "bG6yZJut5mb6c/F4S3SsE+kzKEuwPEajgbW6i+6KUfYxYlitu+" +
                "th+/HxMFPqbLGRj1jhHHbuaRzKNnywsdCtvzRfvN6nO78rZ+iy" +
                "G1dknpK7RnP3+zL3UyPPC4X8tZrhf5Sff7ddOr89CMfOL2qjf4" +
                "6eM3dFc38z0v4yHYfBA5XzdF84T4PPHKF5um++82SQtkadPmTW" +
                "3gPaXm813rebZX+wrdlutkWSRlZy0kvxNNvDz0vUSqoXzI1HOV" +
                "e8tG88rCPYFTlIjvTRcYzp4dlbtohmZ0Bjait9JH3EXfVLSRpZ" +
                "ybKX4tFRK6meK6SS+Vgc4YoRzNSIIQvpwnUyPHvLVvOz2Jo5Wv" +
                "nz+fONBkm/eY0tluITHeu0FDRBqsIJY1KP2dghjOu+MpIkniNo" +
                "5fmZhNghV86vuo4PvxBex+EqttDreO+z01/H++/O+zpeNU/N1s" +
                "Hmafjl93Kemq353++yO1lmd/YeYp94dV6sDj02K+aVWBx1cht9" +
                "3Mmi2kzLftyW3maZ3uZ5Sm+LV+fF6tBjs2JeicVRJ7fRx50sqs" +
                "207GufC55uPi2SNLLIFo+OWsn5aLPmzsInLI5mYTlIjq7SUayT" +
                "0V8K+1l+FlszRyvfzrfdFb2QxbV9m3bS0cN5oR1KQRMkN09Pxn" +
                "BG9zuI2C7YIYzrvjL6S/EcQcPeiB0eM2dUXcfh6jeH+136t8V+" +
                "b4n3s9fx7muTcciv5ddYss1Wfi29LB7OC+1QCpogOd6XYzjIQt" +
                "djNtUKXpyDHrYfHw8erZ4BrtXHTFZ2K7ul7geFTT6R4mPdy/UW" +
                "ZuOnxtToXK+z2GJpMxAXOWnv6OnvqyGejsfZxT2+Or03vdfNfi" +
                "lJI4ts8eiol+strOZ8tFmL48BZAhHM1IghC+nCdTKGZ8N+lp/F" +
                "1szRylfzVbeyClmssVXaXaenyMcezgvtUAoaZ2lkxAljUo/Z2C" +
                "GM674y+u/GcwQNeyN2yJWs9FR6ys1YKUnze7aSrXhbPN7HmlSI" +
                "9J/ZilulK2JTVbF2VwSHI/AzhQh25BxdpaNYJ8P2Iz9XEKsQWz" +
                "NHKz+Xn3MzVshi7s7RTjp6OC+0QyloguRW57kYzuhnChHbBTuE" +
                "cd1XhnsuiOYIGvZG7PCYw/zyfvncaJ4HbuxNdwf3NRWRvdmfC2" +
                "apjdcchMXkz0+DO2Z/fkreXvB737cX9b4gefH98l5lpnl6cbJ5" +
                "Gnxp/uvpIPM0bC12ns6/M9k89Z6Y8VrwcavNUv1eb/Nm0uw2uy" +
                "JJI8vr6a54dNRKqhdMzi5571oczcJy4ByqlSodxToZth8fj/AL" +
                "Z0Bjais9nh53qKUkjSyyxaOjXrrncajmfLRZc88F37A48NOHCH" +
                "bUiCEL6cJ1MoarYT/Lz2Jr5mhlu5mbY5LF8+Au7aSjh/Nkd9/v" +
                "oJqloAmS06/EcEbfoyASduG44Oko1sG4Es8RNOyN2OExc0bzTX" +
                "Uevult8okUXxjDatE1pkbH+rCvztO+6brlTc0Wj2CKqxJUt9X9" +
                "onNP291R2y97vX29kK+6cdPdQcs7Q/u/xf308UJ/p/0/+B38V0" +
                "YoHwk7Dr9Z5N9ov95+o9D+FdyJ7ujAm+rOhzsf7Xys/cf2nzCn" +
                "/VJ7B6zRM2P735Hft5R/hzE4U2T8te3uwu3tqe+0Hyjksc7xNE" +
                "9zdwaWkjSyCv2SeLwlUStH+ZfAzsnHXqymnPIaARHVMZdaqQqi" +
                "UAfD9GN+Zf2lsvOlKszAWk6XnVZK0sgiWzw6aiXno82aW0/fsj" +
                "gwTxDBjhoxZCFduE5GfynsZ/lZbM0cre4Xu2n3c90Huap76kBP" +
                "Gcfj/u7pqsgkWzerjT40DZOZ8+5u3i2SNLKG3/FSPBhFO44hld" +
                "gpxAljmoPkYJWO674yLtyK5wianQHdT1vpY+ljbmWVkjSyhs94" +
                "KR4dtZLquUIqR+vb4MDahwhmasSQhXThOhkXboX9LD+LrZkr6/" +
                "70fqeVkjSykse9FI+OWkn1XCGVIz4GB7hCBDM1YshCunCdDM/e" +
                "stX8LLZmrqzT6WmnlZI0spJPeikeHbWS6rlCKkd8DA5whQhmas" +
                "SQhXThOhmevWWr+VlszVx3KN9D3NSfrHkpPq2r9xg36z11GMnN" +
                "KlTJxxzkVM8HZRWv6t6xSLKrP9GPvjA+zj9JPNkdV61zkFM9H5" +
                "RxJnW9MTL4WuXfP3WO8nu6+f/9Uzl3e/oT/ehL9urrx+HHY8ne" +
                "eH6Sg5zq+aCMM6nrrSOV62kQrqeNnx6d9XThg/N9n5msJWsiSS" +
                "OLbPHoqJfrLazmfLRZc7P+PYujWVgOkqOrdBTrZAw3w36Wn8XW" +
                "zNHKt/KtRoNk8T5ii3bS0cN5sq+3sJqloAmS4/2DGM7oLQhEwi" +
                "4cFzwdxToZw+/HcwQNeyN2eMxhPm1rTx7O++rhc4t9Pz7v/28p" +
                "nqPeYpm+1fsD+7w9OE8xzIvVocdmxbwSi6OivX62Lo4+7mRRy1" +
                "WwJZmW/TQbz9Mk2zT/tzH80WLX0/CHh4t/OP9Xtvb14Y+P9nmX" +
                "3khvsGTb79mx7Fh6Y3BePJwX2qEUNM4q3sUfi+EgC12P2dghjO" +
                "u+Mmw/L9e2BM3OANfqYy6tnXTHaYUsfDu0k44ezgvtUAqaILnz" +
                "4CcxnBFXiNgu2CGM674y+kvxHEHD3ogdHnOYr8+7bDlb9tdxjG" +
                "XL41anzwizyB5fW3MdPDt9Tbzf2tbkxzLp3xckV8Pn8f7Ppnke" +
                "7z5T9Tze/fbhP48L+/rn8f7FCe9vb/S2e1d7r/E89a5F0Cu+B0" +
                "fYrTrE1yNdXj7INbS3Uxvdq2JSW/XPQr4683X9jNXmcLc4s+jq" +
                "ebKf/3pKn1r8ehqsVTGZ53qqnqfkE9PPU4FYztPGo1XztPHw4c" +
                "+TsD+c827jpanfXH13fM6gN/tqGoe/cXVWVtNs+fX8Oku22WIp" +
                "PtHFTp5FDEQTpCqcMCb1sifPIsswrvvKIFY2R47ZzoDkYK3kp/" +
                "ss033+e99039vue/A+nPP7wTVgP/QSRpgV80osjop28D14P4aj" +
                "+1vU0ffgfWRVxSvyPLaZbab/IemqCo0sr6OH80KbpCCxTR5C0j" +
                "gcoZiPWlS0KS54GMUjkP7E3vbzPLjCa8yLM/ynMMfjqP4enL/Q" +
                "OMLbYbPPyufT7ER2Qn9vyS5OWhuNnTgYnwkYX6zv5+NrWzM/w5" +
                "1MT4okjSyyxaOjXq63sJrz0WbNfQ/+lcXRLCwHydFVOop1Moa/" +
                "DPtZfhZbM1cd/g+gaEj0");
            
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
            final int compressedBytes = 1401;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vHFUU3ZoeUWBZ20VeKa4SSpB3ZsUPSOTKiWRZSA51as" +
                "vrRJts0tAjZH4BUooEiQQoEEnsuEGiAJr8A0pamDd3r8/9eM87" +
                "tneXneTN6F3fz3PuXL2ZXe/anc7wx059DL+r1rPhamdybPzG2v" +
                "CnjjmGL2v5VPlOJj+PO+4Y/tC5xDF8cmb0l5gX3avc70+1X8/X" +
                "Q/9a/xokaWSRDY+OBnnvS1nN+dJmrdN5/Mzj6C58D8jRVToq67" +
                "AeP7V8vj+PrTvXDMn9dKXV++nKbPdTuVlusmQ7nMVasVZujh/C" +
                "E3zQZR1kyKAsYBVrQZe1wJRdcEQyku/ejqzScbDr5fmCPDhkNu" +
                "pKT4BrJQNb/Rv9G9XOmkjSwll0i26w4Qk+1lABGX4W3arDLmyq" +
                "qufUBQ5HxN4XEcnIObpKR2UdlucjP1dQVxZbd64ZUvfd4NM233" +
                "fofkb33e3ydv9fktXkao2soEsP51mbJJDYJg8haRyOUCxEZcSz" +
                "UBx4MgperqXl+YIMfXBF0LgvxqYc9AOr2C12q11ay/r+2KWzir" +
                "4mH3s4z9pWAo2zNLLE8bEYi2SwcR+jFbpP8UnmWL3ulTNS9115" +
                "3Ob7rjye7X2XnpPIaDyn/hfLMqfQSZM53X+7qDlt7M93P23sX+" +
                "j9036TOd3/p2kXxagYsWS7PnvVOVKe4BvJU1efZvQYjatqvWfr" +
                "wKk4RtoCEvtj2UGa1YvnnLL1/ASQE2O/9H7am/N+2rvQftpb1P" +
                "OpfNXq5/irZX6Op+dUbC52TnG+y8zJIOH9eL/T4mP23Sfvu6NW" +
                "33dHC3s+vWn1nN608/m06DklcufwfMpH3k95Py1sd+U5xT+nu1" +
                "peZck2Wyzhgy7rtAQakFI4NoZ6mS0ZbFzzYnk+9surjWHb2om1" +
                "Xq5XWi1r3zqdpEsP51nbSqABKYVjY6iX2ZLBxjUvludjv7zaGL" +
                "at5Yz8HL/I69345zynRp9HPTl/pHnGPBDOrrl8T5O7c6fcYck2" +
                "Wyzhgy7rtAQakFI4NoZ6mS0ZbFzzYnk+9surjWHbWptPx8EnrI" +
                "1f5ncDZ+zTb88faZ4xy35sxrTM2XYn9tNR3jX5/Xie05x+b1kt" +
                "V71NPkj4bMxHPKZGp4o4r8RN59h+LP+0PtOziHvqTlbKFRWpbf" +
                "JBwmdjPsLeaCcrXBHnlbjpHNuP5Z/WZ3JOK3FP7HrGv+d7rMkx" +
                "/vMduY4/Gub9dcH3BX/nvdJovm/zDJp8XpA/V8lzmun3CIf5Dk" +
                "sfg8PBIWu04hksORM1KdTpWYjJHs7uz/fBtuxfZsXR/JUxmu79" +
                "NPfF4EWQWKkMxJEVy+ea6VmIcYbPBA53YfuI9S+z4mj2yoCme6" +
                "88twa3IEkji2x4dNRLzpc2a3Ec3YXvATm6SkdlHZbn8/15bN25" +
                "Zmj+HH/wzbv3HH/w9exf797nOZU3y5ss2WaLJXzQZZ2WQANSCs" +
                "fGUC+zJYONa14sz8d+ebUxbFs7sbbKrUqrZe3bopN06eE8a1sJ" +
                "NCClcGwM9TJbMti45sXyfOyXVxvDtrUTa7vcrrRa1r5tOkmXHs" +
                "6ztpVAA1IKx8ZQL7Mlg41rXizPx355tTFsW0vW4PrgevVEn0jS" +
                "yCIbHh31kvOlzVocR7zmiIjM1Ii2C7BwHZbn8/15bN25Zni/f2" +
                "9Z3P9tNJvTow/aPqf8PUL+vCDvp2XeT48+/D/2U//5hf7/7nmz" +
                "/fTwqznM6aN83833vju40/pP6E4GJ5CkkUU2PDIq7TgGKiWTxb" +
                "Ex3YPsBH4d17xYno/96M9OQPNZdvN3hx8v3+vd6LPl+x4hz6lh" +
                "T5/ndwDR59PdwV1I0sgiGx4d9ZLzpc1aHEd34XtAjq7SUVmH5f" +
                "l8fx5bd64Y/gPKhVS3");
            
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
            final int compressedBytes = 1333;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71qHFcU3spVCHkB2Q9gF65Saz0zD5HKxI3fIGVgIytgFc" +
                "aNTRKIwvoBAiEkgYDShDgQUkiCqI2fQS7cenfOfv7O390dSavF" +
                "lu4M9+z5+c53zr2auTO7oNHIHpMfRvVIjns37t2gFE0ssemx0S" +
                "iB1za0nMd2EXsgxmbZqM7jiPVif5Hbdm4r1Otp2DE5WHz+OBu/" +
                "Tm4miD+C52UvfzG+fxef/yQMv1+ow5+WRv88A9Nv77S/5PPR/+" +
                "fs6Wa9crKjPWlPIGHDgqSPus6zkmxkKvH4GPM1WlfwcVuXI9aD" +
                "X8824/a5YjWHzaFeN7HFR0mfj2k0kZbTsgOR1dVcOCPG92M7Iz" +
                "rvs3zEuM4evj/tfnP19qfd5+vfxx89uIL7+Of1eTdknS7jveA6" +
                "33fjyXhCKZpYYtNjo1ECr21oOY/tIvZAjM2yUZ3HEevF/iK37V" +
                "xb7X67P3vy9bJ/Bu7LKbr2AOdtL8lGphKPjzFfo3UFH7d1OWI9" +
                "+PVsM26f6/H1PbPu4+vYxx3T03rl1Ovp/NdT86Z5E23xUdIHHT" +
                "EfyTgtO/ItiozE5b35nqxXoy2fj2fd5Z55dve6ez0aiZwfcw0W" +
                "JH3UdZ6VZCNTicfHmK/RuoKP27ocsR78erYZt88Vq91ut2dPvl" +
                "72z8BtOUXXHuC87SXZyFTi8THma7Su4OO2LkesB7+ebcbtcxfW" +
                "uB3PtF72vrGcomsPcN72kmxkKvH4GPM1WlfwcVuXI9aDX8824/" +
                "a5YnWn3ensyuplf42dyim69gDnbS/JRqYSj48xX6N1BR+3dTli" +
                "Pfj1bDNun+vxi53r56i938em+qzvBXWd1vj+dL+5DwkbFiR91H" +
                "WelWQjU4knxrIquoKPx9iyvvWc4wrkuQtrp9mZab3sfTtyiq49" +
                "wHnbS7KRqcQTY1kVXcHHbV2OWA9+PduM2+cCsZn7rvlss/ddXm" +
                "+N34Pr7yp1H6/Pu0tfp+6oO4KEDQuSPuo6z0qykanE42PM12hd" +
                "wcdtXY5YD34924zb5wJRr6e6j9d9fNPXU3unvQMJGxYkfdR1np" +
                "VkI1OJx8eYr9G6go/buhyxHvx6thm3z/V4Ob76FNreF/VeU++t" +
                "x81xtMVHSZ+PaTSRltOyA5HV1Vw4s15tP7YzovM+l61F7snm00" +
                "yLLNOVaz69hL/j4KqrkOvtrnlx9shwxDr78YhVyPV2V98L6jpd" +
                "5GhvtbeiLT5K+nwsRiKnZZeMvK7mLWN8P77+qj7La5F7+k622i" +
                "0T6W3xUdLnYzECb9rJFjLyupq3jPH9+Pqr+iyu01bumWdP/nP3" +
                "3dEZ79OTC9zjh2fEH78/9+Hel3Uvquu0xnWa1DUYtE5f1zW46O" +
                "8q1/v/Edz19KReOeWjm3ZTaDJyBCSQzCmxrkYxpntY3l/sA7bu" +
                "X6NytjgzsNne6/eWc+/jz+oaDFqn53UNluwPB93BXHKUEIwTle" +
                "GRsxrFGBARSR504fvI+teonM3PjGy299l3vIftQ0jYsCDpo67z" +
                "rCQbmUo8PsZ8jdYVfNzW5Yj14Nezzbh9LhDD35/2vr1670+Df3" +
                "961b6ChA0Lkj7qOs9KspGpxONjzNdoXcHHbV2OWA9+PduM2+eK" +
                "tfedXbfdvze3K+6+/IB28NvdbUrRxBKbHhuNEnhtQ8t5bBexB2" +
                "Jslo3qPI5YL/YXuW3n2vK/0z3+5N0bwveX/Tvd448/nN/purvd" +
                "XUrRxBKbHhuNEnhtQ8t5bBexB2Jslo3qPI5YL/YXuW3ntoL7G3" +
                "9U3ybTa7l43w3Mvyb33egt7rThQw==");
            
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
            final int compressedBytes = 1088;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWr1OFFEUng4S34CCd6Ch352Z+BBASUdHQu0mhFVprG23N7" +
                "FQE4kaYrQwVkAlxT6ALbHRQpjL8Tt/M3MXFhA9d3PPnt/vfHNz" +
                "d7gzWq/Wq0VRX8qkJSvZ8MiolZTPbdJ8nKLQMckBObJKRnkdpu" +
                "1n+VlsyVx2GL1L+aMX5/P1aLkwY/TeeD438pXwfb38/uIgvC2u" +
                "MUYvO6MfZ0B680f7NDOLWKercFouYsR+iv30F+2n8bd/bz/tTm" +
                "M/3eTYexhr4I16p96BTFqykg2PjFpJ+dwmzceRLCwH5MgqGeV1" +
                "mLaf5WexJXPZIX53OaParDZJkk0WSfig8zopgQakNhwdQz3P5h" +
                "10XPbFtP3Iz6/Ww9a1lBHnpzhn3tw5s/5+3+4cj5Zus9vgVH5z" +
                "P/fpeJ8/Jz447auWOZxTNx8ufSZdvbtY7X2Iv21xHo91inWK90" +
                "/36VxQbVQbJMkmiyR80HmdlEADUhuOjqGeZ/MOOi77Ytp+5OdX" +
                "62Hr2mSVW+VWUSR5MS40skjCB53XSQk0ILXh2JjXhXfQcRvr4o" +
                "1uWCdbL2svrXE5Ptca2fjG6ZN07qE8bWsJNCC14diY14V30HHZ" +
                "F9P2Iz+/Wg9b11JG3J/iPh7rFOt0u+uU/+8I//M67f7Ifo9yVB" +
                "+RJJsskvBB53VSAg1IbTg6hnqezTvouOyLafuRn1+th61rdX48" +
                "t8Tv7vr38XK9XCdJNlkk4YPO66QEGpDacGzM68I76LiNdfHm12" +
                "xXwK+ljNhP8buLc8FtnwuqxWrR2skHCZ+O2YjFlOipwu/Lcdtz" +
                "NB/dv49n+1r4nlQd96erjPL57JH8jHny0Rl9mddjV56VZ9ZOPk" +
                "j4SKeYjniYEp3qZRYQkedz05ykl2dLPB332PmehslJeSIijZ18" +
                "kPDpGM9GpsSU6JTh9eVY9LE5mo9khmyfZ8c6nfiehsdxeSwijZ" +
                "18kPDpGM9GpsSU6JTh9eVY9LE5mo9khmyfZ8c6Hfse73rKSSvK" +
                "pPc3PLmB+1N2177M+bJ7+jOeeLPW6VesQZzH53d+ejKNnZMz4v" +
                "9FZ+7MeJ+ZNfYXYg2y1ulBrIH7LmGtWiNJNlkk4YPO66QEGpDa" +
                "cHQM9Tybd9Bx2RfT9iM/v1oPW9cmq16pV4qCZNKSlWx4ZNRKyu" +
                "c2aT4OuPIIz5SImgW6UB2m7Wf5WWzJXHaI975xzpzfOg0Ph4ck" +
                "ySaLJHzQeZ2UQANSG46OoZ5n8w46Lvti2n7k51frYetaysjfT/" +
                "tLd7GfhgdX2U/Dg7z99PhZnDPjOfgunoNjneI5eH6j3q63IZOW" +
                "rGTDI6NWUj63SfNxJAvLATmySkZ5HabtZ/lZbMmcW8OF4fmTL8" +
                "mkJSvZ8MiolZTPbdJ8HPbXiUV4pkTULNCF6jBtP8vPYkvmskOc" +
                "M2cZg6n85n7uG0y76/vw/dhg2s8POZxTNx8ufSZdvUXkN64v2q" +
                "4=");
            
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
            final int compressedBytes = 1462;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71uHVUQvk+AqFPwBEmTN3A2a16BJlFsyUVsRZEspYVEsk" +
                "GGR0BIkdwjUQASEm4QFIjGSKZI4wegd83eMzv+vvk5526ClRB0" +
                "drXjmflmvm/O5ObavlFWK7m2XtmvK8pzzuOb8kvwrVebum0Nz9" +
                "Seh20+SUubkXuX9y7VaqyRWuTgc5+1YANTjcdj6OdqVvC41cUT" +
                "9TTPp824fa+vl+vFR6t+Lbj6nqqb+Xn++u30/JDt6cWZfP3im+" +
                "vMb8V+b6r+mL/+njD89K8m/K6J/vIaTD9ee7/OZ/r65vdEmf/N" +
                "nt702jp7d903eS2dZPnE/fXU99T31PfU9/Rf3NPRZd/T5j0dXS" +
                "3tHB4OD9VqrJFa5OBzn7VgA1ONJ2KZCit4PGKtufnMcQN5r6/v" +
                "v7c0X0+Ph8dqNdZILXLwuc9asIGpxhOxTIUVPB6x1tx85riBvF" +
                "cr+ve7/j7ev9+99e93D4YHajXWSC1y8LnPWrCBqcYTsUyFFTwe" +
                "sdbcfOa4gbx3jq4Gs1OJJQeLnPqKeQTZ9E/lSjvUs7rQjBXMyz" +
                "PZLFdbPo9n0+WZMsn5cG6QEksOFjmPcTUqLadl14pMl7n0jjV+" +
                "HjsZqvM5G3s6zzNljovhwiAllhwsch7jalRaTsuuFZkuc+kda/" +
                "w8djJU53M29nSRZ7LzfPlp/5lyydX3tOz6auw7WLSn7b6DJdfn" +
                "H/cdpD+hunfykw+vX1mfLOr/6821Tz54zVn/fHd7Gm+Pt2HFk0" +
                "hiZCwardZzrF7OY6eIM6DGdlmU+/BEvThf5LaTm+jueHfyZiue" +
                "RBIjY9FotZ5j9XIempUQrrSMfgqoaB+eqBfni9x2cqvQP1fpnx" +
                "f0PfXPn/rr6X1+PY3Pxmew4kkkMTIWjVbrOVYv57FTxBlQY7ss" +
                "yn14ol6cL3Lbya1Cfz0tfEX9baN1LDlY5DzG3fAtp2Xnfq9r62" +
                "yupvb8Vk3Nz9mq29zd2tr7cD2/9ZZ+w+vf7xZcw6PhkVqNNVKL" +
                "HHzusxZsYKrxRCxTYQWPR6w1N585biDvnaOD4WDyii25A7nF54" +
                "zW+dhbsIGpxhOxTIUVPB6x1txQoz2Ffts7R3vD3uQVW3J7covP" +
                "Ga3zsbdgA1ONJ2KZCit4PGKtuaFGewr9tneOjofjySu25I7lFp" +
                "8zWudjb8EGphpPxDIVVvC41cUT9TTPp824fe8c7Q/7k1dsye3L" +
                "LT5ntM7H3oINTDWeiGUqrODxiLXmhhrtKfTb3jnaHXYnr9iS25" +
                "VbfM5onY+9BRuYajwRy1RYweMRa80NNdpT6Le9c3Q0HE1esSV3" +
                "JLf4nNE6H3sLNjDVeCKWqbCCx60unqineT5txu17ff38qXj/97" +
                "tl/y71Wd9B4/eYl+NL9eTJK9RqJXpqrJurgPEM7fniHBrz/FyV" +
                "s8WTKZud/br2dDxVT568Qq1Woic9/Sl461XAeIb2fHEOjXl+rs" +
                "rZ4smUzc5+XXs2nq0tnloFcFSNlf9ztK7eXAVMK2IleHQKP0c2" +
                "P1flbP5kYLOzT5k74x1Y8SSSGBmLRqv1HKuX89gp4gyosV0W5T" +
                "48US/OF7nt5Fahf17QP1e5uT2Nh+MhrHgSSYyMRaPVeo7Vy3ns" +
                "FHEG1Ngui3IfnqgX54vcdnKO7m/f316txK6vtaeRWuTgc5+1YA" +
                "NTjcdj6OdqVvC41cUT9TTPp824fa9W9L93iz7PfDI8UauxRmqR" +
                "g8991oINTDWeiGUqrODxiLXm5jPHDeS9c7Qz7ExesSW3I7f4nN" +
                "E6H3sLNjDVeCKWqbCCxyPWmhtqtKfQb3vn6HCY3qvEltyh3OJz" +
                "Rut87C3YwFTjiVimwgoej1hrbqjRnkK/7Z2jk+Fk8ootuRO5xe" +
                "eM1vnYW7CBqcYTsUyFFTxudfFEPc3zaTNu36sV/X180fv40+Gp" +
                "Wo01UoscfO6zFmxgqvFELFNhBY9HrDU3nzluIO+dK/4B8Zt3yA" +
                "==");
            
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
            final int compressedBytes = 855;
            final int uncompressedBytes = 10657;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV1tqG0EQ1El8BF9i2JPY2H9GQr6A/gy5hbEsQSCQjyQQSE" +
                "yeHyGHympHpaqe7hkpiT/8Mbtsu3uqurpmWNtsukt3s1ma4u7a" +
                "ZagQucY8SZ+NVKNSTcdj0RSdUOJ2Lh8/D+u620i77AVj9SnzV2" +
                "/G5/3qbOau1We38nOK78za7/3PX4HCx9l/XKu3TfTbXyh9OGQ/" +
                "/sXJcD/cI8tPzEAEkz011eMsYuqh7c/7QK3+lRWr+Z1BzXr316" +
                "vXs36d8mae9TNovPdPw9Mu8qkxiJMV8dFznEUMDM+kDlyUPiL/" +
                "yorVyp1RzXofV86Hc8ac5SrXXLGoj+BrjSzWsS68B3Jsl0W1j4" +
                "+f5/15bevcTuj/707s7efUz+nZzindpBtE1KgQucZc+2ykGpVq" +
                "Oh6LpuiEEvdYy7fu2Z9A3AtGf5/6710/p35OL/k7WL7vvvSvk5" +
                "PO6Ws/g/4d/Izv0/d+Bv3veP9/18/phXzfXaUrRNSoELnGXPts" +
                "pBqVajoei6bohBL3WMu37tmfQNwLRn+fTnqfLtIFImpUiFxjrn" +
                "02Uo1KNR2PRVN0Qol7rOVb9+xPIO7dV8u0HLMpTmvLfOdcV8Ar" +
                "6zJSjUo1HY9FU3RCiXus5ZvT5Jxcv+3dV9fpesymOK1d5zvnug" +
                "JeWZeRalSq6XgsmqITStxjLd+cJufk+m3vvpqn+ZhNcVqb5zvn" +
                "ugJeWZeRalSq6XgsmqITStxjLd+cJufk+m3vvrpMl2M2xWntMt" +
                "851xXwyrqMVKNSTcdj0RSdUOIea/nmNDkn1297lT88Do/I8lNe" +
                "mYEIJnuia8c+ziKmHqLp9Od9oFb/yorV/M6gZr0fuA/DA7L8xA" +
                "xEMNkT7v6BunUWMfXQ9ud9oFb/yorV/M6gZr2P79RtukVEjQqR" +
                "a8y1z0aqUamm47Foik4ocY+1fOue/QnEvftqkRZjNsVpbZHvnO" +
                "sKeGVdRqpRqabjsWiKTihxj7V8c5qck+u3vcoftsMWWX7cu7rN" +
                "CHGyIj56jrOIqYdoOv15H6jVv7JiNb8zqFnvB+56WCPLT8xABJ" +
                "M94e7X1K2ziKmHtj/vA7X6V1as5ncGNev9wN0MG2T5iRmIYLIn" +
                "3P2GunUWMfXQ9ud9oFb/yorV/M6gZr2P1x/JnbyD");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 0, 18, 3, 0, 19, 0, 0, 20, 0, 0, 0, 1, 0, 0, 0, 0, 0, 21, 0, 0, 0, 4, 0, 0, 22, 5, 0, 23, 24, 0, 25, 0, 0, 26, 0, 1, 0, 27, 0, 6, 28, 2, 0, 29, 0, 0, 0, 30, 31, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 10, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 34, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 35, 0, 0, 0, 0, 3, 17, 3, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 37, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 38, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 39, 0, 20, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 41, 4, 0, 42, 0, 0, 0, 0, 43, 0, 0, 44, 45, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 8, 0, 0, 46, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 47, 10, 0, 0, 0, 0, 0, 21, 22, 0, 23, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 25, 26, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 30, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 1, 33, 2, 0, 0, 0, 0, 5, 0, 4, 0, 0, 35, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 38, 0, 1, 0, 39, 0, 0, 6, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 9, 42, 43, 0, 0, 44, 0, 5, 6, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 1, 0, 0, 0, 0, 0, 0, 0, 47, 2, 0, 0, 3, 0, 7, 48, 0, 0, 0, 1, 7, 0, 0, 8, 49, 8, 0, 50, 0, 0, 0, 0, 51, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 49, 53, 54, 0, 55, 56, 57, 0, 58, 59, 0, 0, 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 62, 10, 0, 0, 0, 0, 11, 0, 0, 63, 0, 0, 0, 64, 12, 13, 0, 0, 0, 65, 66, 0, 0, 0, 4, 0, 0, 5, 0, 0, 50, 67, 1, 0, 0, 0, 14, 68, 0, 0, 0, 15, 0, 1, 0, 51, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 12, 0, 0, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 69, 70, 12, 0, 53, 71, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 72, 73, 0, 74, 75, 76, 0, 77, 1, 0, 2, 15, 16, 17, 18, 19, 20, 21, 78, 22, 54, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 34, 37, 0, 38, 39, 79, 40, 41, 42, 43, 80, 44, 45, 46, 47, 48, 49, 0, 0, 1, 0, 0, 0, 81, 0, 0, 0, 0, 82, 83, 9, 0, 0, 2, 0, 84, 0, 0, 85, 1, 86, 0, 3, 0, 0, 0, 0, 0, 87, 0, 2, 0, 0, 0, 0, 0, 88, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 90, 91, 0, 3, 4, 0, 0, 0, 92, 1, 93, 0, 0, 0, 94, 95, 5, 0, 0, 0, 0, 0, 96, 0, 50, 97, 98, 99, 100, 0, 101, 55, 102, 1, 103, 0, 56, 104, 105, 57, 106, 51, 2, 52, 0, 0, 107, 0, 0, 0, 0, 108, 0, 109, 0, 110, 111, 53, 0, 0, 10, 0, 1, 0, 0, 0, 112, 4, 0, 1, 113, 114, 0, 0, 3, 1, 0, 2, 0, 0, 4, 115, 0, 6, 116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117, 118, 119, 0, 120, 0, 54, 3, 58, 0, 121, 7, 0, 0, 122, 123, 0, 0, 0, 0, 0, 5, 0, 1, 0, 2, 0, 0, 124, 0, 55, 125, 126, 127, 128, 59, 129, 0, 130, 131, 132, 133, 134, 135, 136, 137, 56, 0, 138, 139, 140, 141, 0, 0, 5, 0, 0, 0, 0, 0, 57, 0, 0, 142, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 6, 143, 0, 144, 58, 0, 59, 0, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 145, 146, 0, 0, 147, 148, 7, 0, 0, 0, 149, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 150, 1, 0, 151, 152, 0, 0, 4, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 11, 0, 0, 12, 0, 13, 153, 8, 0, 154, 155, 0, 14, 0, 0, 0, 15, 0, 156, 0, 0, 0, 0, 62, 0, 2, 0, 0, 0, 8, 0, 0, 6, 0, 0, 0, 0, 157, 158, 2, 0, 1, 0, 1, 0, 3, 159, 160, 0, 0, 0, 0, 7, 0, 0, 0, 0, 60, 0, 0, 0, 0, 0, 61, 0, 0, 161, 0, 0, 0, 9, 0, 0, 162, 163, 164, 0, 10, 0, 165, 0, 11, 16, 0, 0, 2, 0, 166, 0, 2, 4, 167, 0, 17, 0, 168, 0, 0, 0, 18, 12, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 0, 169, 2, 0, 3, 0, 0, 0, 13, 0, 170, 0, 0, 0, 0, 171, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 172, 0, 173, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 174, 0, 2, 175, 176, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 177, 0, 178, 179, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 181, 22, 18, 0, 0, 0, 0, 0, 0, 182, 0, 0, 1, 0, 0, 19, 183, 0, 3, 0, 0, 7, 10, 1, 0, 0, 0, 1, 0, 184, 23, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 0, 16, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 25, 0, 0, 64, 0, 0, 188, 0, 189, 0, 190, 0, 191, 21, 0, 0, 192, 0, 0, 22, 0, 0, 0, 62, 0, 26, 0, 193, 0, 0, 0, 0, 0, 0, 0, 0, 0, 194, 23, 0, 0, 0, 0, 12, 0, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 195, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 24, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 196, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 5, 0, 6, 7, 0, 3, 0, 0, 0, 197, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 1, 198, 199, 0, 1, 26, 0, 0, 0, 0, 3, 0, 0, 1, 200, 201, 13, 0, 0, 0, 0, 0, 0, 0, 0, 202, 67, 0, 0, 203, 0, 0, 204, 205, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 207, 0, 0, 0, 208, 68, 0, 209, 0, 0, 3, 0, 0, 0, 69, 0, 0, 65, 0, 0, 27, 28, 0, 0, 3, 0, 0, 29, 0, 0, 210, 0, 211, 0, 0, 66, 212, 0, 28, 213, 0, 214, 215, 0, 30, 29, 0, 216, 217, 0, 31, 218, 0, 0, 219, 220, 221, 222, 30, 223, 32, 224, 225, 226, 33, 227, 0, 228, 229, 6, 230, 231, 31, 0, 232, 233, 0, 0, 0, 0, 0, 70, 0, 2, 0, 0, 234, 235, 0, 236, 0, 34, 0, 0, 0, 237, 0, 238, 35, 0, 0, 36, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 239, 0, 240, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 39, 0, 0, 0, 0, 36, 0, 0, 0, 241, 0, 0, 0, 242, 243, 0, 0, 0, 244, 0, 0, 0, 245, 1, 0, 0, 0, 5, 2, 0, 37, 246, 0, 40, 0, 247, 0, 38, 248, 249, 39, 250, 0, 251, 0, 0, 0, 0, 0, 0, 0, 0, 0, 252, 40, 253, 41, 0, 0, 254, 0, 255, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 257, 0, 0, 258, 0, 7, 0, 0, 0, 0, 42, 0, 259, 260, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 261, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 262, 263, 264, 0, 0, 0, 0, 0, 0, 0, 265, 0, 0, 0, 0, 0, 8, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 0, 0, 2, 0, 267, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 1, 0, 0, 2, 0, 268, 44, 0, 0, 0, 269, 0, 0, 0, 270, 44, 11, 0, 0, 12, 0, 13, 5, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 271, 0, 0, 0, 272, 0, 0, 0, 0, 273, 0, 0, 0, 45, 0, 0, 0, 46, 0, 274, 0, 0, 0, 47, 0, 0, 0, 0, 275, 276, 277, 0, 48, 278, 0, 279, 49, 50, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 0, 8, 51, 283, 284, 52, 285, 0, 0, 53, 0, 4, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 0, 289, 290, 54, 0, 0, 55, 0, 0, 0, 56, 0, 24, 0, 0, 25, 5, 291, 6, 292, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 293, 294, 3, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 295, 0, 296, 0, 0, 0, 0, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 297, 0, 0, 0, 0, 0, 0, 298, 0, 0, 0, 7, 299, 0, 0, 0, 58, 0, 300, 0, 0, 301, 0, 0, 302, 303, 0, 46, 304, 0, 0, 0, 59, 67, 0, 0, 0, 305, 306, 60, 0, 61, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 307, 0, 8, 308, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 68, 0, 0, 0, 2, 47, 0, 0, 309, 310, 311, 63, 0, 0, 0, 3, 0, 0, 312, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 313, 0, 51, 314, 52, 73, 0, 315, 53, 64, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 316, 0, 66, 0, 0, 317, 67, 68, 0, 54, 0, 318, 69, 319, 0, 55, 70, 320, 321, 71, 72, 0, 56, 0, 322, 323, 0, 73, 57, 324, 0, 58, 0, 0, 74, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 325, 59, 326, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 327, 6, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 328, 0, 0, 0, 0, 0, 0, 0, 0, 329, 0, 3, 0, 7, 0, 0, 33, 1, 8, 0, 0, 61, 330, 331, 0, 0, 62, 332, 0, 63, 333, 0, 64, 334, 65, 0, 0, 75, 0, 0, 335, 336, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 337, 0, 67, 0, 0, 0, 0, 338, 339, 69, 0, 0, 0, 77, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 340, 0, 341, 0, 342, 0, 0, 0, 78, 0, 0, 79, 343, 0, 0, 0, 0, 68, 0, 80, 0, 344, 0, 81, 69, 345, 346, 347, 348, 0, 82, 83, 0, 349, 84, 70, 350, 0, 351, 352, 353, 85, 0, 0, 0, 354, 0, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 355, 1, 0, 4, 0, 5, 0, 0, 6, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 357, 88, 76, 77, 358, 0, 359, 360, 0, 0, 361, 362, 0, 0, 0, 7, 0, 0, 78, 0, 79, 363, 70, 89, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 364, 0, 365, 0, 366, 0, 367, 0, 90, 0, 368, 369, 0, 91, 370, 371, 372, 373, 92, 93, 0, 0, 0, 374, 0, 0, 375, 376, 377, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 378, 379, 0, 380, 0, 381, 382, 0, 0, 0, 0, 97, 98, 0, 0, 0, 383, 0, 0, 71, 72, 7, 0, 0, 0, 0, 0, 99, 100, 101, 384, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 106, 0, 82, 107, 0, 83, 385, 386, 0, 0, 84, 0, 8, 0, 0, 387, 0, 0, 108, 0, 0, 85, 0, 388, 0, 0, 86, 0, 389, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 390, 0, 0, 0, 0, 391, 0, 392, 0, 87, 0, 393, 0, 88, 109, 110, 89, 0, 0, 111, 0, 394, 0, 112, 395, 396, 0, 113, 397, 0, 0, 0, 0, 0, 398, 0, 0, 0, 0, 34, 114, 115, 0, 116, 399, 0, 400, 0, 0, 0, 117, 401, 0, 118, 119, 402, 0, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 1, 0, 0, 2, 3, 4, 0, 2, 5, 0, 0, 2, 5, 6, 7, 0, 1, 0, 0, 2, 0, 8, 0, 5, 0, 9, 0, 6, 9, 1, 10, 0, 11, 0, 12, 1, 0, 0, 9, 9, 0, 13, 14, 11, 3, 11, 0, 15, 2, 2, 16, 17, 8, 2, 11, 18, 15, 16, 3, 0, 19, 20, 21, 0, 2, 22, 23, 1, 24, 25, 0, 26, 0, 8, 6, 27, 9, 1, 28, 1, 15, 29, 30, 3, 4, 0, 0, 31, 32, 6, 1, 33, 34, 4, 1, 0, 0, 6, 10, 35, 36, 16, 37, 38, 0, 39, 40, 6, 41, 0, 42, 0, 0, 43, 44, 7, 5, 45, 5, 46, 47, 48, 7, 9, 0, 5, 49, 50, 51, 38, 6, 8, 0, 52, 2, 53, 54, 15, 8, 55, 56, 0, 57, 0, 18, 0, 58, 59, 60, 8, 61, 19, 62, 1, 63, 3, 64, 8, 65, 66, 67, 2, 68, 2, 19, 69, 70, 71, 72, 73, 0, 3, 74, 10, 0, 0, 75, 0, 76, 77, 8, 10, 1, 1, 78, 3, 0, 79, 0, 80, 0, 81, 0, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 9, 0, 11, 90, 13, 2, 91, 92, 93, 94, 12, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 22, 22, 9, 1, 24, 15, 101, 0, 4, 102, 1, 1, 3, 103, 0, 11, 104, 105, 0, 106, 107, 108, 109, 110, 111, 10, 0, 112, 23, 16, 0, 0, 13, 1, 0, 113, 19, 1, 26, 13, 4, 14, 114, 6, 1, 13, 115, 27, 116, 117, 0, 0, 18, 18, 5, 118, 7, 0, 0, 3, 20, 0, 4, 119, 1, 27, 1, 0, 120, 121, 49, 18, 8, 3, 19, 122, 1, 4, 123, 124, 22, 125, 12, 126, 0, 5, 127, 128, 129, 130, 131, 132, 29, 31, 133, 134, 6, 20, 135, 32, 13, 16, 136, 137, 21, 0, 5, 12, 138, 139, 140, 13, 141, 2, 142, 143, 144, 35, 15, 145, 146, 147, 38, 148, 2, 6, 4, 149, 150, 0, 39, 151, 152, 2, 153, 0, 154, 40, 26, 41, 155, 156, 4, 157, 49, 22, 7, 158, 159, 8, 42, 160, 161, 162, 0, 163, 164, 22, 0, 165, 166, 47, 5, 0, 31, 167, 168, 169, 16, 170, 171, 12, 0, 172, 173, 174, 34, 6, 0, 51, 0, 0, 9, 175, 1, 22, 23, 15, 3, 4, 41, 1, 176, 25, 177, 178, 7, 7, 0, 179, 2, 180, 2, 181, 182, 24, 183, 29, 184, 23, 1, 2, 185, 186, 187, 31, 0, 26, 0, 3, 1, 188, 8, 27, 9, 189, 190, 2, 191, 192, 52, 193, 18, 194, 195, 196, 0, 0, 197, 198, 6, 199, 53, 3, 28, 200, 34, 13, 201, 202, 3, 203, 0, 14, 204, 205, 206, 207, 208, 209, 5, 210, 211, 212, 213, 214, 3 };

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
            final int compressedBytes = 1355;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXEty2zgQbSAYF+JskCktlB0yUU3Nek6AbKa8nOUsVeUcBN" +
                "6knCPMzr5JcpMcJZD4EUQCJCiBZJPqV2VZMiUT6H79ZVOP7Ame" +
                "t3B4vOP2w7/mDvRns9u9f4ZvwD7+FPIFuNhs5dPXL7tX87d5AP" +
                "MXfGLv/vux+X8PBAKhDeV+ZOwgr56Y5W/0E1iQbqfOfwhutwqc" +
                "/2DmT+0OPQPsrXj7wn8H5z/snREa/oAHbZ7dr3fbH/CB/Ac8dv" +
                "vf7+R/CSn29+rs7762v50G5swMtHX84Vwe7e97YX/wQPZHIKCD" +
                "LPOGErrKHwQw96p6izhkDcdXRhkSWg3RK15FQiIQCARCM/B6dX" +
                "v7z6qo2ssQ87mIv3vIEX8ff6P+GwEB2JHm0mP98ZkuLULpppFw" +
                "FP0rv3/yZqn2w8sHIUofwxpuKZLfriD/r/uHyu8f7g5sO/Uvjv" +
                "3Df8r+ISDsH/rK4antXU1OBxPOtNXZv89pf+H+ecX//ZH/1D8n" +
                "zNE8MSSYZPudq//u+4/7UPx8qOJnfX4zxvlb+4fW/g3e6w9Z+C" +
                "8j9eN5/xaof7vSAirev2/RqmDDSf8CwwYWef2hO3/y64e7U/2g" +
                "V5g/iRT1DkCsOqGrKF2STekfmpz9w4WQDy16+q+J8zcI6v9Y/y" +
                "5AzLP+nWzvoHx+f7ZCOXKB0Ld+HVk/IqihDnfe9WfhX0//daL5" +
                "tcvtz7CPYNWBfdJ9jpnDZ4wA8d7CW2DMHLXo/r1kdvfyVKQ8rr" +
                "hj7p9vrOleEgNv1rPNlHz2X51ECJf+RPi30v6xcapo6Q/O9adL" +
                "/b0G9JeePy6l/0z9myXD1g8Q9R7eGzLyZ1AOpzq9Kp8/QRQjvh" +
                "tZ5q0Oj7IOJxq49thTBKk3hyJebjziWJnsH1hv0hCU37amAZf1" +
                "cmSc1B1llcRhl7lqYDMNq7O7iRz7YlcdHiKXQ0JQiqBRVMhkDi" +
                "buc7n+I0/2KYK2wSt1moAZ22yfR5KI9h81U9rZTGCzEFDHPJ/q" +
                "cIwsrw3g8MShDTObuegfS0qTndFkFoW43k71kDzbAuHSIkX0yk" +
                "/3aY9fwYk4S0w/dfj86QZC6jHYBEsE2ZYk88qkkfMPuUibYXiW" +
                "IrUnS95FQtt+IZsK5XATyFg/zVsnZuDPiQVygBPDYLdD1psvdx" +
                "nMAkpDFleprylWYUDv/GVo/qh1/Yiu36y0/sR1/nUjPn/bMqsi" +
                "s8w3fyWI/6jXv95E5aL51+zzCxS/FqV/TfeP3Zb/tTe+/zRMPL" +
                "+pc7YKAh542vndJWeNKfLDHuAunf+ej/+EcdEY6tM4VmVvWCMI" +
                "419w/h3C8+86af6dp+5SL1CDBhFl9PoNhm42vMb7T3b/yVX+J/" +
                "3+DfI/KGnWmD/f1/Pnx3uRkubPZ+2fNuc/rHegNf8hxslj0h2d" +
                "Jc6tK8ITCISM/huC/ttUHQ1FfpSiSB/YbYlLnDf7tF97mkiWUu" +
                "RvWe8/HKk/YlatOV/+vEf+yqou+aO1P7zOEbX8VZ6SYwoLo07P" +
                "tf771M4I8I9B4X8BJuMfn23/gfhVfKH2ph2/ZjI+SvcIhKT6TT" +
                "TuvzLTUTrZhEX7arn3LVShC+lCU1Bcof9j4dRVI/fHomPaw4RX" +
                "rrOdefTvr8mb0lIsJRBaVhypAJrWIusHin+rVn4/NCkbc7WpJl" +
                "KJ8vJ/i+L7n+ycJqSCJUR7/6vJv/Hp/8acN878m5AfPPbXvN/f" +
                "pxI/LzBZwaLCui17DZWc9VWfT6t7CQQCgUD5FwE1owIR3fRnO2" +
                "pw9UY1GGGFkMOsLe6/+SX+u/v+Jb4WwREurL9NwbqI75V2Yob8" +
                "AlZfMrQ=");
            
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
            final int compressedBytes = 1207;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUty2zAMhVg1w34WnI4X6Y79HISTVY+Ro7BdZZkj5Cq5Wa" +
                "04tiWZlCkSJEgZb5FMG1MEARB4gCgZAAUq+JMW8kAs/VFfEaL3" +
                "iqoilrgBdLiXs3Wvtr++5sMfdedaTzdcQF6OOfyXmX5Ue1ySTH" +
                "9KnP3fTtxcHJdtJguq3aSysOuo4accGVboUUiyZtDUh36vSrkb" +
                "ac3KqWdsEJo4HmhverDAYBSFYBXUmy/6JV7AYNzovvBSqlw7Q/" +
                "l5nCOAHgmUjhfnyN/++Pkb5ORvDv64C53/IYx/BsgfoT+NZ/Qd" +
                "fJQuYiYOounZXPYo/1x/lsB+nH8YLaNXC/FDZowfzKG5BkHLXw" +
                "3vv5L9I8H7Dy3Rahz+0jrUJW9fhmX5MfFrL5Dcu9JfeOqFvVdw" +
                "B7ozvwePe9o7qe3lixCwu5f2zvQafu4LHfO0//Xl/hW+P87jj8" +
                "aLvxoYgfHzluNHxfmnnyaGTA4tvRno89UpMxAg3rbJuWRm2bL2" +
                "qx458qcZu21AkOjCDMjIRZ4bDTXcf2D9MbaHDju0bdT/b3T/bp" +
                "6mhbm5PKdsm0+A9ecPReL4Cta/VBmGlBK9d7ta10UlMIrUOKJg" +
                "EVTR0suuP+n+OfX5D0C5/9+v5SoqcTxi5Deu+19mkPBN/y+Dlr" +
                "tB/91R/0NQtz796xJE7kpVK1bOn5p/eDzt+NT8m4d/hPMHtj+P" +
                "p+S/rc9/6/pPGI9y/ue29x99/cvja+wfFAgJW0GZ1YrNKjj+/F" +
                "xK/H9Y35CJhIHwUhMfJu/lZ/oHl/67d/1Dhv5Hquet8Z9I+f32" +
                "Xze/shT6M8DI7YXj/QNj+x+0/8bDJv4HJfcPbfxi+w/2zxk/F+" +
                "J3kfkZDTM3hPwfFv+g2fi33fgZaP8I/qRWSVG3/1RdPz2HPz9u" +
                "XfWTgsPttlOBevjQN4cJu0j7Mq7aX/qaBcZ596bXxd0vX/82+f" +
                "wKwni5snrS+fd/4vkdqvNDEmn98jy/nM9f4iRiKf2l9++p7Y/c" +
                "/7YF41fN+V/4jMrn9+rKn+T2I+WPEaho/2nZlrwbo94l3v9RV5" +
                "1ocfavQ386WH+yqv6fnv4jr1hdxmsb4vkJ1DPbv+LC//759u/X" +
                "/P6nVqyiff6lgW/pMFL5o1rwf1lgPMSMT0Ku+pusfkUSTeZWfL" +
                "D/mBgxVEM8ubX6jYFNC6OojVo3iaFbpKmvDjtpkv77T6ifP2p+" +
                "fGr+on5+mk5/G7h/h8DfiP23pvcvcPxoqv/CwGqd+GmWp8Kqwl" +
                "Te+P3+/Pynl1P8njw//zzEb+UvtZFLSku6p2yApTF5OeH8jGIV" +
                "nW5KWuz+Q4n+h8myHfOEI008P1q5WZ0vXPQObv1rcfhrgWhjKO" +
                "s/1XttE5KqSPk1VvZKS0X++cV4NWnv30KqP+qrnwY1vTUQTvI7" +
                "+1+3d36pLv0zGAxGu/EP+f2XULa9nZr/WuYPbv2LxV6qKCSGKT" +
                "X7eSpDND+DkWcr19h6se5ib92gitCE/Mvzd+HPD3fzdVvjyNk/" +
                "ph91iGKGYY+oPRm0/L/Df39b6/6z7f3j83/p8n+9Rm7Ggj1z0X" +
                "x+ujiSHNQAPj9USf0KWP1fVWXpdHH+PB+9IuZ6y/rH9R8NBN8/" +
                "k4b/6nLowg==");
            
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
            final int compressedBytes = 897;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUuS2jAUlBQnpanKQgsWSVUWSioHUeUkc5SXOUGOwFGDMQ" +
                "YDtpGtv9S9oAwzQnr/fpZtGMsLnAGeIGcPB3RQD/QPd7CDyXht" +
                "J8up/lUyfflEM3E5FP0LnVf/qTtlVnlgdB1IciLXDvvr5/lp9/" +
                "zl2s+P/nOXnwqMWlVQ/OUlLVXm/+H9dyr/LTeKW/Sdae1J/jt6" +
                "YFgrcNN/O3ryyz/1xNdE8fk8AW0vAD7zbxc3rJ9mTTN/2/1HGf" +
                "W7jfgDUP8r9H8K7//7z1+g0DQY0KaE9XewH+oH0EY+nFQj4fhl" +
                "AvpMSoeExf8AAAD4SDhmrv8yfVd2zkTHnv3wvv/iY//Vb0ZQwP" +
                "MPctP63c6fLI5XT+MH+cUg/79+vIpgnn0Np6+5U84PANupqbYb" +
                "5NOb9f0btIsl+w/kj11kHurve9T9Dw/7L3KxJYvRqVklMqp3/u" +
                "SzYfmLUPvjTy/Hn77Gn2QAAAQGCC0AACHqPwAAIBEQHQAAoEXk" +
                "ff0t8nfWnlPp/RP1bFYZa/uJF/ZTEe1XbvNKqVTQ7P5q6kyC+1" +
                "caiUgAAFBd4lCY1BSo9fmr5CoN6R/7Xy78VYorc7pd6HPu+qTv" +
                "TqNbuenAzP6p09Xrv9H+gVsISzlZbjh/QuMa9cH2/NeflP7jW3" +
                "6WWH5pV29p/GRD/Jga8wcYU+5Ye/5wZBD8F6iV+a7vX30s1a+v" +
                "1nnJQM0vIBaO0dVt7Aj2dEX8LsFrKDLfOgnE07/akGnQvwNAdl" +
                "CNj2d5z9/Vbn9X/t+6/7S+/jL0l/b5C+tkSq3wKb5fg9bjAQAI" +
                "iJ/XiCQmSCr2hWlufl/ykj6loOPp8Mc3RqLfM/41FuM39Zd9f4" +
                "f+UqBL8O3GcX6TeP0V+4Kv+0e2sx1CLALhYYpYpdoZv27Pr331" +
                "/Ne34zX+78YHef6rbNn+ycPDvNaZqC4bPPi/2Lf/7O/6TYnfHw" +
                "Dc+FuA6/905sStW6SXqhQrOuDAPss5w4jhq/WD+Wi0P1BV/wUA" +
                "ySDXygXWHzT/N89fBvXx1xxFc9/8JY/7L0rowmsC5aontcmOhR" +
                "AIk3h8bOgyl12w/tNdP1Ex/7e6f2pJfspKfnKxH82u/yPa77co" +
                "lvPvt0TJH5Rt/M6MV7nxR1Nx/gpgv038fzqedurvwLrZBOtw/o" +
                "9vi5+p/DcCI27ewy8LvSsPxlv8u85vN17OjHf2//9VCKM0");
            
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
                "eNrtnUty5CAMhoHxglRlwSIHIDdx5SQ5impOkCPkqFNOelz9cN" +
                "s8BAj8f4uuTlIYjIT0yw+i1DHzzt8mpdzyaZW//MYrc/lqlg/6" +
                "af1nUlrZN0VrQ7K7x41jyml5P/630PF/7B7ZpfYfNX8b7R3D+P" +
                "Xtj6TKkmW/r3D7UYT9QC7+6rup6k93nccvy2oUWP/37alk/JUR" +
                "/9PzV+9Mru3817B/7/Ybxv/MWTNZdL7SjdsDkMNQ/qcDZKFEzQ" +
                "wAiBVYdUXKpOYt/TsvqvhnJN/L4PSif/V//bvEIyqtv92D/v7t" +
                "3/z2/7X0z10LuwrmBYNjdClRDkAPahuAYeUZwPxVZ2ipYJ/eP5" +
                "3Gt2zq/QcKv392Wz8x12/C5s88nP/f7flT9DpQXKqzUIzgtWky" +
                "G5pAcev7Mtlh/ij3/FBr/UAp8eMqfpKY+/8l7Rf3/FXw80eR91" +
                "8mVpvWyL9qkPwJhORv+8zX58c/LS28jPgxRv2yNf8H2ccz94/6" +
                "G5w5/jHkb99h/pagP3yxuAZKx990/3mNvX408PUTwBRGPMMxGm" +
                "CefN/EwdSgLtXrt3M8v42VDJI8hNY1wnr/Toe111f6P6X/tT38" +
                "f+j6GfpcZg04nfjcAZBx/QP37x6wgux3ef/j5Xu1383zS5vvf1" +
                "iYEJz2UkC/QrUfiTIVm5bU+89+fKfp163a5U//XP/4Vf/Y/MVB" +
                "80bqfT90xXlp9ikhnCaOXzGNv07/824IGRDsPwKOFtFA1QLyH+" +
                "p/jvnzmL+M4gDnj/UL/+sr/nO+P0NC/XcWvP562H+zt/VHwuY/" +
                "v34roH898k9Z+B4uKRm/ZMSfseu3ButER0QOBvunxx/U/0CO/i" +
                "1T//eeJ1H/C6H3/dvDx19N/wrtH4BG6dRgFsTY4jqwVAwYrlG0" +
                "ck0mWc7+c3gfBeSLrLb7b/vRRCsArPnDr9cPbJP+FWf/qP8BAA" +
                "Aw5S9x++8C2C8SXD/uRH+N4b/9+k+B51ecPPvR8SwEvv9IWP+h" +
                "2M2vp7n+QJn7XxLb/pdzn/4TSc7F+6j9F0EZfMOoSg3zss7pX6" +
                "h+gv5G/j87p9A/u6fNdgwSe7a5/798t/4yqpP3B+DBvfpf7+cP" +
                "QIy8P+f4CS4glX8CcN3H");
            
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
            final int compressedBytes = 732;
            final int uncompressedBytes = 38721;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXdtOwzAMdUuFwluE9gASDwHxIRZfwqeEP9mnsjI2hjRGuz" +
                "S1HZ8zqaqqpYkdX47TG1GT6Ob9fVAuTr408jhuA6XvI4n6793+" +
                "qymP25thp5KwOTlRDsQL9F8gT1jiTBbkZwI8Q/P8T/CfWO4/rv" +
                "VXHH/Ynv66uvlDm/6v4A9JB38olb9U//AfCf61tP/otn+uqP91" +
                "+Kde+/Ge/12hhwqqReGq8TMr7183ugXTVifcHvjPEfoKzgoAdh" +
                "BmOQyf4788suIvT9qO7K0b+W934L9jDMsX+G9wrf3JJCvDUg3z" +
                "NwAoWD/Iq64fnIlMw7In7Scy3qRvCiPt091RiP0k3dP+4F+yxG" +
                "VK635e+2TDLSZfP7Cw/qxZf+vU/6WoN//S419H/7apomI/+7G/" +
                "fLS/zVT7e3PCH5mAiR6doAbAU/5G/Vedv+YZ/Oe0KAn0X4XiJL" +
                "CvJmYQbk9m++fp7tZoFEH/QKP8myvmT2AJ/mnXfi3YT3k/WZJ/" +
                "zm0/a/3ktH1udP7qj3Nwa78K6zd19t9ynvyt/wT+AFRAFm4PAM" +
                "Bq/CEKCROFlRlNm8KBAMhYIQAAwB5Nrb+xcflnrj9ccf9P7fo7" +
                "m5a/Pn+zm39x/2sL/K/t/CEUP8LZXYH8B6yEABXYrD8HyZ7P5r" +
                "/0d/xJx/wX6vWvKf+ygP5b4i+sWH4P1x/bz9+oX2C/ADC7MFy9" +
                "PQA0G/+i62lKhOcX7cH++zesM3fT9a/16z/S40f+9l3/WLe/58" +
                "Nwdr8+h0i3lDp+HQ+F0RcH2u52nx4o9zQkejlM5l38oMf3iwwq" +
                "XGsYuAwA/5/a3sP6O8MEC5c5MupnzD8AuAD4EwCAfwvyh0Xzrz" +
                "z/ydXqD7y/1nfi83D/tsH6panvX0J/eP+DC0SowCbw/LTV+Kc0" +
                "/iL/KKq/WPH8Wee/eH7Ct/wA+Ic86y7/fpay/OPG/xF/2o/fDP" +
                "kBwG39wMbHTxg/AAA18AkMou9t");
            
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
            final int compressedBytes = 3974;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXAeYFEUWfq86z4KIKIrCCRIWFhRQQSUIIiBBJKlIUgxHVq" +
                "JIFJCMAgLfiWRQDAQRgUMXMKISvAPD+QF3YA6HCRROQE/mXoWe" +
                "7pnp2e3d2V3k7vr7erq6urq6uv7+3/vrVe0CAmOrwQaHdYYSUB" +
                "LKsElQ1ugFf4AqkKlNY+uxtPkaqwm1oLY2HbOd4nA1KwNzoZH2" +
                "OTRjXc2WcBN0YM9CJ+iMPxod9BFaH1uDu+BO6A19zeag4RwoZj" +
                "TAY1p5XAbnwnnsIbZQbwOHrXlQXl8Nv2rDoBJrr3eDqjgLsuwe" +
                "zqe4BC5lT8DlUBeu0YawTnA9m4IroCW01nZjK2jjXKjNZUNYR2" +
                "02dGUd4G52GHphKfxBexAy4GxNN/rgbDgHSsH5UJp9DxdBBbgE" +
                "KrLx+nVaf6iBH0BNtsR4H66Eq1gNdpHzCTSAhrgcv9ZbQhNoYa" +
                "1hE6EdtGdHYA7cDLdAF+gO8+B2VhZ6Qh9jJ+hgsKvABAu/Y93Y" +
                "KYhAcfN1OAsuwL2RMawHlMOWcDH+pL1n7YLKUA2q663xEH7Fur" +
                "C2+BhcBldAHa0b1DO7Q324FhpDU7gBmhu7oBXcqI2DtnAKOsKt" +
                "cJu5wfgAusEddgnowVbCH+Ee845olFVjmawK3Ku3Y5319tA/Gj" +
                "X/zbLsf0VpY5W1adEolqa8yfxcO4TZkWWUXwZviEbtOtGoPsg6" +
                "FY1t7Ar7Y/sawstwc8zmlFubyr7PLtEr4TKZa9U35+nDxHVTH6" +
                "AdpeOT5vPymn1nJOrftCHmJn40tslzo6bInUu1dNJmU+0CL2pl" +
                "Kcp9MZq0se/jaqP3Y7Vof9PYJ67WcK8Y871S+mZx7Qg2999r9u" +
                "C/jmgeW6tq7yaPjngbVicylh1VpeuZ1e3GKv0nPGVWFiWq++r7" +
                "xV+7sctNYa9Y3gexFrWFfnQHx6sJMH2y5Jc+lfh1Hw5j18MA4t" +
                "dQyMShWjks7QzHIfAA1LbK48hoVPBrBDSKlIdB5otOM+LXEKsR" +
                "55dZ3/nc6QfUds4viRdo5lTR0geoJYJf0WjkSr2NPp1KlTePUB" +
                "/SG7L21lCoSjlZop0L4FJKE7+oJ1axTv73wlYC8T/IM8kvkc/x" +
                "eoVyznZLQimJF/FrMOeXedKcz/kl8FpifMz5ZU4xqZ2SX9QrLW" +
                "EgtKCrE8UzRtGV+zm/FDZl6byPqNlgmzy8gBDk/OJ4ZVyvXQTl" +
                "7E2cX6IkocT5xUR7zRWsGedXrIWCX3QkfjnncX5Rui2O8fDi/K" +
                "I8zq/hcI/TgdJkD63GfntotIGyZl1pD40W+kysH9lpXsPtIWvJ" +
                "Ghit4GrxrEasPjSzr6VUzB6a05yvnQHiqrKHDuHFakMx1orNpP" +
                "ZexfEyf6PUPKMdx8vuy/GCSvb9kXoeXnRUeJE9vIfbQ5FH9lAc" +
                "R0l+eXhZo0F8k9pBbg/VFWEPFV7KHnJ+cbygpnan8QXHS5UlvF" +
                "SqicRL2kM6V/aQUrfTTvYwslDYw2xuD128XHtIeDXleFHexao+" +
                "YQ/NBWyOwvdGaQ/pSj0XL7895HixhtIeJuBF9lBgWAkYjOF4aW" +
                "/aWVBS22ZNhHHwIOE1FjJhvJGJba0JMJrzS3sLHuL8knjRPsjc" +
                "GKkCN4lzwisazYDIWzAhnl+K/dQj1iTJL8FwzsLydBxgVeDnkY" +
                "ZBeBG/NkUDNu0bFy/a73YOqtxvc+KXtIdQw95Dx8eML5PxMhdJ" +
                "fqm+PRLPL1HS5dfLvjxhITm/7Hep5gS8JL+sjqqFb6fiF/26eB" +
                "1M5hfZw4kKr+rwCEyCh2GGUwJmwTTjR+MEyyJ+TYVH9QrGScKr" +
                "tFPSEjbNIMvllIrvuUhcf2awyNvGcZ+Nbg5TVGoU55fvLQ+r46" +
                "/GMVHPw9HAzVkVlGsI/2r8LGqI2UPjcDSXjfsvcXzV+CKaxsbe" +
                "CGzruRwvfw4Iv29XtG5R91WHy3Kq1+DfyCkfXtNjNc1U33lFK0" +
                "p6YxksprwlqtYsG2AR1xuiJ0rn9IQMPe6sdWRHnI9uDgtVinrY" +
                "RjffruzhpQkPHZmRAq9gfs319Ynnvw6GwSvC7enPQVftSsl5sC" +
                "Du7AnVQ38Luj8ymvq4aVx5oWKIX7eGwYvzy48XpZfGUsvVkfwX" +
                "PMntIfFL+C99NpR1rpH+S+IVIey4/9If5fzy2cNmhFB57r9i9v" +
                "CZjKp+/yXtIRQjW6i7/svjF7eHUs/Tnb2D/VekOKXi/VebRHtI" +
                "ey+B17eJ/gtWUEr5L9ceUk5N/XH6TbKHdhXpvyiH/Jc4Jvkv+u" +
                "X+a7/0X9IeAm8j+S/OL3Nbsv8ivG7z28Mg/+XZQ1fPi/uVPaT9" +
                "KWkPzd2w2vwQF+qLI8vhaXgWVhptzD36InM/PAerSG8sxcXO9y" +
                "wK6+AZfSGNv0rBGlxk7gOhaM29GTcl8G1+PL/o61rinlmTfMxv" +
                "F8sV/itjnLrjk4RvtkY4fkm9EdYe6isCuXxXaHv4cyp7aL6XnK" +
                "+3jqWW5V63qw/Nv5sHfG/5vMcv+vXxC4dB2Uim5BcOhfW4MqMT" +
                "6XnOrxdwZCK/pD50+RWrXfGLUopf4vtFj19Wd8kvUVrYoRT8ah" +
                "HEr9hzYvwK1ofwQjC/jD1B/HL1Ye780rVU/HKGJPPLh/O43PmF" +
                "YwL5tUHxq0KkKq6HLfCiWRGyYRNsxu3wEvmvjfoGwvpN2vfgTn" +
                "xVPG0MZuMaynket9Dv67S/gbvFN7FVfRt/xedwHW4I+GrewY2e" +
                "//LrjYRyO/Lq+338ejAsv8yLo2ltRvF86pRxId5H+S98LS73z6" +
                "rlu4GZH3J+mRW1T4lfW61e5h5jOPHrZci0etIzQJQnPU91ZEs9" +
                "74xVen4v55er541RIvWKp+c5v4wRvqd6/Ort8UtdE/yS9tDT87" +
                "m8W9dEe5io5wW/BsfzS+IVyC+fnhfnKfS8UTJIz4ua30vW8z6c" +
                "R+au57XPPHvo49errv/CMs4EJBVqbjfo7a2j7GyOl/oeHFaMlW" +
                "S26uNjmM1MyrVi/mlv3FcX+H1LvNhZ8bnW8WAEEv1Xnvh1sMj4" +
                "dX6qK0H+y49X7nV7eDnjfXbnQo9fziTOL/IvzYC+G1bK3MPjUd" +
                "QTmR5eLr+YCVczC8tBI3OGyy8s6/df/niU9F8eXr7x8vRkfhmT" +
                "C4pfODsXfu1Kk19l8scvGe/NZbzc0MPLx6/hkl+skRvfML+iXM" +
                "LL3g/j8BI3vkElrqP80RKvxPiG1BtefEOkJiTYw6lB9lDGo+Lx" +
                "4vH5xPhGIeGVrj28PBVelEoTr7jne3ip+AYbCIzdJ/A6xPpBSf" +
                "sQ64WVuP/CiqQPK7PebADrK/nFeoo6CC/W34+X/U/lvxYG4hXo" +
                "v+xvgv1XweBV6P6rbv7wCuO/gvHCKhIvOlP8EumS5L8GY1WhN8" +
                "Z69hAzOV7ODVhN2sNgfhmLxDeUFYRXoj3k/uv04WVdkCZe3Ysa" +
                "L5df1L/cL9WCGfR7OdYk7fch1Ut6Ax5Vnmd2aC+8OKBHp/j55d" +
                "MbvdPx9258I0FvfBtWb1iV09QbPUJqocmJeiPn+GHS/UnxQ6wL" +
                "j+AVeGVsjP6dqHe4z+aWzq1Wbg/FXUvC42V/Ey2gLa/xQ4FXjT" +
                "Tx6lvUeGGdWJ6Kz1NqFkyLVJB4YQOXX1g/9FsszQO/jhcsXnnT" +
                "89wepoXXvaeLX9x/YSO/PrR6cbykno885PJL6kNXb6TwXyI6ho" +
                "3TGS/n13+5/CoavWGNL3K9cZ30X1oJrRiuRyppHkIRn8PtAi+p" +
                "HeLiUXz8FRSPisyS8ShjuYxHab5oDSyUePF4VBh7mFY8qhDGy8" +
                "HzKdYjIe+enxd+yfmUWE+8lmo+BdtJfSjjvXQsK/Shmk/RxBfL" +
                "471J/EqI9xpPBsV7jRFuvNfPL3EWIt4rzpLWA4SN9yp+JcV7U+" +
                "rDkPFea1ZQvDeIX/HxXsmvnOO9tOcwn6KVhtUxRDvy+RStjNQb" +
                "eLM/HoXujJsJa4SeXxtg1Z8KH4+iJ59z5vove1s+/d7I/D4R1Q" +
                "w1dseu2Ak7YxfspolvS2vi6cP4+KHEyx8/TGjN03nB63TqDe2x" +
                "HMo0zf2ZzltFjtdtsff1+HU755er51GNMXCPKLUO75D+C7gH2x" +
                "LIr2fobXsG40X3bAzVrh1nAr+cHYWPV/x8issvkV6vjnd6ekPl" +
                "hNIb7vyX8azSG72S8UrUG6cXr5z4FWaL3F/keN3l0xvrxXq2wW" +
                "p+ebunN/BN7CfxEuuxBwh+Xc3xgkb4OjTjeHG9gVuF3lip8Bro" +
                "6g1tkKs3JF656w2J1+9db9BvoesNoQ+T5pcB8T7S84NxIMeLzh" +
                "VelKri5xfUorcdEM8viReV7KD4tcrFS9TM8XqA88vDKwVDKqXi" +
                "VxBeQfzy8IrlkprBASpdIZ5ffP18ipY0UUeFlzqL4SXOxIowDy" +
                "9Ki/ELBNpYqJbMLxcvVSLJX3p4+fplkDqKkR9sEb/ZOA42S/8F" +
                "Gz17SHucPdRm4hZ1rtbhKbxWS7yC/Nfvyx6m67/yHRfJvz0c69" +
                "pDnIiT6JzPbAl+yfGXn19y/OXhxfWGyGsm8eLjL9GaNdocDy85" +
                "/nL5lZPeyIlf9uLC4Je94Izj1wT1Bmvc9VFyPoVyNsl4FE7m8y" +
                "naBv/8shh/pZ5PeU7UPCXMfIrff+UUj7LXFsZ8ir06vXhUXJmU" +
                "8Sjt6QKcT1HrN/AfeAAP4n5KfeSrdzh+FkP28zic9+XA9rWpx1" +
                "+Ft+HH/532MMUbZGsvaWr1vkZWLrLUOCHwEuvLjZPEDDG/LM6O" +
                "xkbN/QNbI9Y0GsfzhpdcP58ykrA48B7f+nkPr/Dr5+23f5948f" +
                "XzuXydv+BxPCFSYiVgxnaF128uXh6/OF458mtdIeC1tlDw2nGm" +
                "4qXt9KV3+eJOo5lanebxi43JmV+FsQXzK9gehrAmLl4LTg9eYd" +
                "aL5lLDPC++od5/u8h/PO/xDXd9r/aXgF4tND2fH7zSjW/ku7fz" +
                "8J3E68PYGxwAhie4PuT2EMS6VbbRna/09IarD3FfDvOVL4gSWW" +
                "HmK8Pqw1zGXnH6UOQUyXqbcPqwMNZHad/x9TZsqw/Bn/zxee1I" +
                "HuLzikHaD74nTim6+Hx4fhWVPiyU9RsJ4y8V31Dre6mnS2rHQo" +
                "6/xJp89k5B8supWBj8ciqccfxy1wOcBMbejRsv/+THy+VXKLzE" +
                "/1PAxtoPBTde/r89jMfLP5/CDqQ5n/KS0hunTk/8MLw9dLLOuP" +
                "ihmk/h88v02xm7MKWzdDPf88vZqeMb/5v+q+DiG+wjDy/9LOzM" +
                "Yn8vr5fLN16bCx4vp32h8KvtmYOXfoEfL289gFmRfelfD8C+Uu" +
                "WFl3HX21B5EaEPXA+w9XTED/ODV7p//1X0/HLXA8B/ACj35zI=");
            
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
                "eNrtmntsFEUcx3+zN7O3d61AtRoMpaVafCFItSiKioJVrCgKqV" +
                "gRH6AFa7VofFAj8YHE4ANtgWiCEKka/7E+GqIxoLQNiKYFxUdB" +
                "IwoKiiaCoiY1JK6zs3N7u7e71729vd7tZTeZ2Zm52bnd+9zv9/" +
                "vOLyvLsow6ZHYIvyg12ibLZLE6grbQ8hn6FG3m/Q/Qm7R+G22k" +
                "dRct3WgHG9+k1ORDtB21o3fwRFl3kGY2owdtkB0c6BNjP7J8oC" +
                "tgvuz4CDXyVZ+Ss3KQh53PRZ2G3jz+tEjoRx34VtYeSsuJaBuU" +
                "KLzgFD0vGG/mBdUqL5jJeX3Eed3CVlsIDSovKEzOCyrseMH5rJ" +
                "7K6ivhKjtesIC3C7TRY+n/71/eLjfygrOiw23uZAo/XwvX6Ubn" +
                "0nIzLfWsh1ktQlj7/BhWW64Jp5t5wQRaLtBmXGa6ZhZcDzck8L" +
                "o3xisk4Sd5m/HCy+K8hIhQKBQJEu2N5zYo0sLvFKr5eSa/m82G" +
                "bzXwEoYksZAK85jQlklewrrB4IWXpsMrwdqW8jVX4la8Cvck2O" +
                "1i3Kt962rH1t5pOdqcWR+DfkjdH9rZVy75Q+tDWKRr38X4rI/H" +
                "Lzq2UGgSGni7XhtttLybLvMY3plpXm7iV3SK0yvw57nBC3/Bz6" +
                "+DQJ9Zggh79iL6TEcUXlBKe6cKkdAf3B8+BJWqP4SJij+EybQw" +
                "nwrXsHo21JH/2Iye0CGYB7fxXzOk8lL9IRTD8bpfepThdz+Nlj" +
                "N4eywtZ8O5A7Kak8gLhmmt4/h5BNwHJ8HJKi84k/GiPgiq4Dw+" +
                "40K4iLcWwTTDag9ALdyo69/JamKYE2W15u9hJC1lvD2aljF6Xj" +
                "AOztFmToKL4RJ6vgIup3UNTKf1DMPaNwHVAkDVBboUbmfeZC6a" +
                "gzpQnbAfv8X9i2t9KA7leqPdO32YIfuq8Zs+FL7n9tWFtXEyNR" +
                "6/dJbY7fQbxKJsxC9XvKb7LX7F9Dz+ymblxzVeX8s5fbjRG8KW" +
                "7NyrWJLuCnhXfL+s94fkNRf+cBb3h33e+cNoXUb04Wy/+UMdse" +
                "/ofpl6R7xXz0vELnjdwXntGcz45YZXeLh/eVnblxh1wetBzmtf" +
                "rvOKzvczL/HRhD0ZVdUi3bMLo1mvCf+Yb/ErW/YljEk7fh3Av+" +
                "H9+CBtHcG/45/xr/hPsYXyqtVm/GWY/5PL7zmMD+VQfuPuLPGq" +
                "TluxtOJ/Ev2hQHW92JC6P6Rn1R/+bfGrBvErRV7W/pCAsv8KE5" +
                "M99Ka+/0ryr3gl8IeMV9r7PhIhYdQRlox6I8bLaF9imzP7IpLp" +
                "Pmtc6/lX80nPp8LLXm9QEqbYEj6gES3MppqIvpFPvMTDnngUk5" +
                "6nvKrCExLtS1ji0L6Kg/hlY1+PpW9fICi8IEJOYE/ypcpLGpno" +
                "D6EytDXOCyYbeSn5eU1vtNPZWn5ez2vg/LzKSxqh5OedPZdUal" +
                "hlmOn5lLW0/Hzcvqzy87Q9LeHqWkPPYX7e9v/yseHKSbSw/Dzr" +
                "sRx0PD+v8Irn56UyNT/PnrjYvP9iZ77/IqXu/1HSPZn+z7rJ9/" +
                "pv/yU1xfdfpEzZf5FRsf2Xwkuie2hcnv39V3RTXumNtPfLZJx1" +
                "/IqWmPWhNMNh/BobxC8n/tBN/GL2VYUPSgvi+Q06+nK00pzfwG" +
                "vc2Bdel5Z97bbwDfV+tS/s0T5UarSx3wh5RH0fQBvRvR/l6A53" +
                "8uuG+DN+efv+hns9H3t/w8JDLlHqggppg/59NlXPp85L45wxXv" +
                "mq523oPEH1fL+i59FRvT5U9LyqNxD3gOr7Noj7J+V9G8O/nOl5" +
                "XT9Bz/Oew/dtMq3nFV7Z0PNGvZFcz7OeSc+T5Ur+MLIiiSWmlT" +
                "8M9Lw3/jCm58nTij6MXp2oD3nbXX6+zzt9WFDkqT7zUT7KJj//" +
                "DO4cINJ1y1k8CorziZdnMXtNks/Wmhg/69fnzLY/9Oogz6XCy7" +
                "+H/+2LrCatqIO8QJ4nK8mLZAVZlX78Ii2Dmd/IV/uyiV9t9JN+" +
                "9vlRm+sMGQ20e5DjV3lgXyYiFvnDtOzrvcC+MmVfFha3UZ+P0u" +
                "c33OSj0s1vFNwf2FeKEW5rVvV8c8ArRctoIr1Z5NUS8LKOX2SH" +
                "Z/Fre67HLz/wchq/zLxoseDF+t2kz8zLclW3+aiXAl6p6kOyKz" +
                "X78pTX2oCXWz1PvnHoD7/1kNf6gFcK13B/qPUZL3rW/GGCltxj" +
                "Z190rjte7wa8vOdF9mbKHwZ6ww0vsi85rwzGr/cDXvoD/ge+O4" +
                "FH");
            
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
            final int compressedBytes = 554;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmt0rBFEYxr0ze87abEop4g9wg5R7IbcoN+JW+dws/i4iua" +
                "AlHykk3LogskXIJlei7Ti7pm22nbVm22He2eeps7NnTjPT6dfz" +
                "nvd8KKWUuFM20ZHt/6EuF7rsWvUtWta/q5Sw6gd0nr1uW/UzWq" +
                "E15SA6pQ31C9Gx8lBmTLER7bl/RiS/eeXekeWlrzleBV8pwku3" +
                "gFeFeNF6Of4S9/CX7/z1AH9x4pWRERePKiDixKuIg15CV7Quns" +
                "VT6NY5HooUnTjFQ9ovFg9D14iHXvlLpM1Nc8fqTcLZX8Zswb0Y" +
                "/PWPJEvkG279JZBveOYvWaNb3rPtn0WeS+bVLjF+/ackgRdbdg" +
                "bGL98zMuEvVrwEeLHKDT9k2IkXpbnwopuq8lcd/MWcYNSWV0SM" +
                "qNFg1NruSF3C7t9q1INXRejUw1/smDWCFxtWTVg/ZMesGf4Kjo" +
                "y4bAlKX4Ky/yVbsf/lu3ll2ec33PKSbeDlJa8So1t7cGJ7APLD" +
                "Ltn5s79kt2t/dcBfXvlL9sh+PX71VTQe9oKXl/FQDiCfD4bkoM" +
                "7nG/Ky+zLXD8HrT3gNgRe7+XIBLzkMXj7y1IiNDvZTeHkrkvGX" +
                "HEU8ZMQsLscwX/aLrPXDcawfMpkvTxT06c0eD81X9+OXmcL45V" +
                "l2MVlN5wECkA1OgRcrXtPgxYrXDHix4jUHXqx4zYMXK15x8OKm" +
                "ip/fWPDrfJkVFef58iL8xSoeLoEXJ9V8AbXlStQ=");
            
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
            final int compressedBytes = 567;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtm00vA0EYx83amSHxenB2cHHxFSQOEic+Qm8iSCqpKk5er1" +
                "RV+gm8RLxU4yKVoJEggqsDiQQnVwmJxJptG7ZVWrGjM/p/ktmd" +
                "2bc0z2/+z/PMZmtZ2cZ8H32j0qgy6o0KxxEmGrd+bEaNBXPB2K" +
                "BlkWe7R15yX0FuM0aX8FlRefnBSyteAfDSitcweGnFawS8tOI1" +
                "Cl5a8Zpm4yTGJtmEZdEIG6NhcuSgcyjaBTkhe+nxDlkT200SF9" +
                "sD0RLkPHl8N33+jKyTKJ3LQf2UbBfye8hxyXh+Ko8n9j/d4Sno" +
                "ubOY1QpRDuZd6bbBSwrxWqQhEqNBOkvnaYTOIB6qYp/jYZLXEv" +
                "KXbvlL8Fp2XPdrXqYHvGTpK8UrY/RrXmwFvOTyYqvQlyb5awP1" +
                "RsnHwyh4SdPXFupDnepDduZYGfflXC97jd50r/v9WD/mv7Lz4A" +
                "4+UMnMKxEP78X+xrX68BrxUFb+Mgay4yF7yDjfg3ioHEm368Mn" +
                "6Eurev4ZvLTi9QpesnjxMvd50Rbwkqmv741Xwnf/u97gtdBX8f" +
                "RleHkdvKeO8Xq39WU+Ql861Ye8Aby04tUIXlrxagKv4tQbKV6O" +
                "cZKX2MfT40RWLmy2eX3xLPBSVV+t0JdWvNrBSyteHeAlnxfvdG" +
                "39tQ5eRX2/0QXvKfN2w4fvD0s+f/nBSxYvbv/fPMYDdp9G+BD0" +
                "9WeRbeSnvOzvD3nIfX2VV4OXVvEwDF7S4uGCmaduNBPwnTpW9g" +
                "bNR9si");
            
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
            final int compressedBytes = 661;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnEtPU0EUxz2981r7LfwCsuXh1oWJGnYmhIUhJHVtWIDiYy" +
                "ViwUZjkA8AwTQkRlBUYuIDkaUaQUkIgiZgjCwMxlynt4X0AbTA" +
                "PXYG/ifp7dzb9jY9v/s/85+ZtvqevksZfT/cDHpZ0H4RhnqAXt" +
                "PT/P4jGrLbERq32+f2NknvouOP849P0zA90HfCsqApGg2rCHoV" +
                "InKZeLa31yWS+iGy5xTJTNFeib5oZtf6GoO+OPWV46WfVOIVJK" +
                "vjFVwAr5rWwwlkzxmK6/ot/Y5af4qO/91sLRQd/+DcJ/jiam71" +
                "FMtZp4N8JQzGt9ZXoq3sWDuudFfD1sMZZMEZzX4Us3b8ZaucmI" +
                "/NH76H36id39CfkDuXItLXbJz6EnPQF5e+9Jz+vNEO3sBvHIAe" +
                "bh458IrXEnLgFI9VyqiGgrq5f3+4gv6rdv4wkTQK2XMlzFFRga" +
                "OYRJYcU17M6ynmGOohVz00x+UtysibskemZFrekH375yV7wYuN" +
                "V53qsn7jsroUhjKtOsHrf4Xq3i0vdc7yalCNcftDVQ9eXPqidX" +
                "MC619MXq6J46zZ+UNzEvOHfuhLpMypCjz7kTuvRminkQN3QiyK" +
                "7+aMWLZczooV8VV8Ez9LnvGraG9hj+/zQ6wi246Ol5vRf3H1X1" +
                "XUwxbkzqmKmPWHrfCHvugr4nUevHzhZS5W0B/m5w+63+iAvrj0" +
                "ZboYeHWCFxuvKxu8THds/dcweLHxusagr6vgxcbrOr4PcOj9Rg" +
                "/0xejne3O8MP7ygxeDvlLg5RWvPvBino/qRz30pv+6Hbe+xBp4" +
                "cfESiyZtt8tRO1qvNCX/ToP1Sn/CDGT1VcA80pe9z/8yncrGZl" +
                "l9bXO9QF8x+Y0deA2Cl1u8jvwD5h9Pgg==");
            
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
            final int compressedBytes = 598;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk1LG0EYxx2zMzvPuQcvRXrQFsGcWnur2n4AD4KKJ3vqQQ" +
                "K9WaoX+3KuRgXPCsUXDMall7yUxCColNgP0AZ6Fuw3kDS78ZDU" +
                "JCY4E2fM/w9hszubhH1++c/zPLtTLDYSOyq9flbsJ9hesE1d7e" +
                "eqz5cbLMbidb7rW7EJsZMiVI5Etu6IV7V3/D8vdsoylbzYvs+L" +
                "Hfq82FlwPH01nvd5yc0av/EDvIzl9RW8dPJqLLmF2Jkk5zfz5H" +
                "Zp+0eVv5wC/GXVfLgDXvp4yd0yL/jrnuSvOGJnjvhFkL8O+Hk9" +
                "f/G/8Nc9z18J8NLPSyaV5a8YeFnlrzR46eIlM+p5cQleVvkrC1" +
                "7a/PWLrzCPL/MlvsrX+Re+psBfUfDSxqvg+8t9F9T26+4ceLVL" +
                "7vtWeYmZEq9L54Y+2snhv26Y8yryF7Hb+4u64K921BvkUAjzod" +
                "m8yBWvmCdeqqwPxSh46fRXY5FE7EySf7+XevA8pXP7ZTeplxc9" +
                "7GRegb964S9beMmo7y96hOdfNtUb1Fd3pB+xM7dfVjEf0mP4S5" +
                "+/3Hzoe/ldKFVrvPtt9+y1Y5G7vBJ60vH1/ADqDfv6ZRq0w19Q" +
                "Q4phxMAqXs8QA4NoDN1UH4qnLd/vDSN/6cxfyuv55+CllxcNK+" +
                "X1Arw08xpVymsEvPTxCvqvMfRfduYvrN8wmxdNVfKiCSXz4SR4" +
                "tcdfNA1e5vKi8aY6tNdWXN2DDiKpuv96A39py18R8YF54pP46K" +
                "/HFotYf9guic+t8vLXY1exm6/1Odyft0u0gBiYo65/u+ObmA==");
            
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
                "eNrtmr1KA0EUhXPjDps7hQiCjai1RVq1ELUVH8JeBDtLH8EkGh" +
                "AtomgjGElcBH8iGkXwJ0RfQMGHELRb1yT4FwuVGXdnci4k7G5I" +
                "cz7OuXNnh+fcFfLcVf+t6OLD9bnvuzm6opPG/QHlg+8ClYLv0+" +
                "BzRje150eN36u0TUV32W8qqtCu/4OiSx9VV6L8t/9xGtpFjKT3" +
                "6e6Lv+j2t/7iDPyly1+cVc9LJMFLZx4q9xf6l1m8cuClj5dzRx" +
                "4H60PnQRUv5x689PHiNdX+ch7By6g8XAcvo3htgJdRvDbBK7z9" +
                "jfgMb0E9q/c38vCXUXlYAC+jeBXBK7z+xTvQzvL+tQd/hbo+3I" +
                "d65hQfQgPL8/AYeagrD7mM95XW9a8K1DOnAl5VqBDV/sWkoH/F" +
                "kIf/sb/Bjjsrsgr6F85HaeTFTzgvapq/+Fnl+hC8dPJ6PW8jBc" +
                "7bGMWLwcue+Uu2Q73oVM1fHfCXRf7qhHpRnZdV+CuRgr/0+SuR" +
                "qfNSl4eyC7x08ZLd79dt19/nYXyq6dk09AwzD3lEZR7KHr3+kr" +
                "0tv78xppIXj2rm1dfy83I/1vMWreeTUC8qJQfEAnkiLVJiUSyJ" +
                "ebxPMaF/yUGV/cuZBC+9vNTOy3IIvLTNX8PIQ7vWG3Ic2kVtXl" +
                "achxPwly5/xV4AyFGwBw==");
            
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
                "eNrtmL1KA1EQhTOy1yR3e8GH8FWENBZaKWglWOsDWIkxGvAFFE" +
                "QS1MVGVExEUJEYBEEFfyLY+AM21o43miKgiJB7uTvrGbjL/nTn" +
                "48ycWWZmirit6LDt/sCcOh3TXut5i0rmukbb5lo1Z59OP9/vtL" +
                "7XqEzrup+/FZ3QJv+h6IhRX0pUfnqrc/Z5ZabByxUvduGvAfAS" +
                "xWsQvFzy+r26JvQQ1IsVSdv+Goa/RPXDEfASxWsUvLzOrzGol+" +
                "j5NQ5/efXXJNSTU4bXFFSIS+kZNU+RmlN5taAW1awqdt4PVQH9" +
                "0F8/1EVoF9+8kaXO/ZVNwV+i9q8l8BLFaxm8vOb5FaiX6H15Ff" +
                "7y6q8S1IvN/rURXFOUvmQOGrb8lb6AvzzuXxG0k1SmH+5ChUTn" +
                "jQr6ofv9S1dt8QrK4OVxftWgnagEWYcGonidQYOE541zzC/3ec" +
                "MiryvwEsXrFrxE8boDL3e8mv8PdcPm/8PgBrzc8dL3tv0VvIGX" +
                "x335AdolPM8/wl+i8sYTeDnOG8/IG2Lyxgvyhhxe+tV+P1R94C" +
                "Vqfr2Dl788H3ZDuzhVM2+EPcgbkvJh2Ate/3d+ZfLg5YpX6gM6" +
                "zxGS");
            
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
            final int compressedBytes = 376;
            final int uncompressedBytes = 18577;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MtKw1AQxnEHAlmcRxDduPNW8LLx8iQqunHr21SwiwquKy" +
                "3BTbFivYBKteJewYdwHw+ShYJahRybz/wHkjTNImV+zMw5TdNB" +
                "4UZTolBhyYe7q3efL/1xbzd2mt23reHPTTv25zN/nFv/7ftO9v" +
                "zODq3lxj55R8+OfvRbrvHIMtH9M69xvMJ5RY+WuAl/fc7LK3rC" +
                "K2R9DZhfk+ROKdwUOSiQxnRcsyTe/3p+xfXf9sN4j34otd6YwU" +
                "vKaxYvKa8KXlJec3hJec3jFXi/vMB+WcXLLeZdX9ELXkP8f2OJ" +
                "3BVMMu/5tUx9BZ5fK8wvmfm1yvwq+Xp+DS8pr3W8As+vDeZXie" +
                "trE6/A9bVFfUl5beNV4n64g5eUVxUvKa9dvKS8anhJedXxkvI6" +
                "wCuk1/fhGuSuYJJ511eT+pLqhy28pLwSvKS82nhJeXXwkvI6wU" +
                "vKq4vXEPdfF+Tun++/etSXVD+8xUvKq4+XlNcDXqG8Rl4BX/0p" +
                "Rw==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 24, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 108;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MENACAIBDD3n5nk2IC3hHYCI4eoyaQe/CzyC4D5Z/6pDw" +
                "CA+xHqA4D5gvrLn/5mfT7kS39e3l/1018gfwAAAABs5P8KAAAA" +
                "708AnP8Azl8AAPcj6wcAAAAAAAAAAAAAAACAUxruxMYR");
            
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
