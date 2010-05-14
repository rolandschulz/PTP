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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 13, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 0, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 92, 117, 0, 118, 119, 120, 121, 122, 123, 124, 125, 126, 13, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 0, 139, 140, 86, 31, 1, 47, 105, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 136, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 17, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 1, 2, 3, 0, 13, 4, 106, 47, 155, 156, 5, 157, 158, 0, 6, 26, 162, 178, 159, 7, 8, 160, 161, 163, 0, 168, 179, 169, 180, 171, 9, 10, 97, 181, 182, 183, 11, 172, 184, 47, 12, 185, 13, 186, 187, 188, 189, 190, 191, 192, 47, 47, 14, 193, 194, 0, 15, 16, 195, 196, 197, 198, 199, 17, 200, 18, 19, 201, 202, 0, 20, 21, 203, 1, 204, 205, 74, 2, 22, 206, 207, 208, 209, 210, 23, 24, 25, 26, 211, 212, 178, 180, 213, 214, 27, 215, 28, 74, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 74, 226, 29, 227, 228, 229, 230, 231, 232, 233, 234, 235, 86, 105, 236, 30, 237, 238, 31, 239, 240, 241, 3, 242, 243, 244, 32, 245, 0, 1, 2, 246, 247, 248, 47, 33, 249, 250, 86, 251, 184, 179, 185, 149, 13, 186, 187, 188, 189, 190, 252, 191, 192, 253, 181, 4, 5, 96, 6, 254, 34, 35, 255, 256, 105, 257, 193, 258, 259, 260, 194, 105, 261, 262, 106, 107, 108, 112, 263, 115, 120, 122, 264, 182, 196, 265, 266, 267, 197, 199, 268, 269, 106, 270, 271, 272, 273, 7, 274, 8, 275, 9, 10, 276, 277, 0, 11, 36, 37, 38, 12, 1, 0, 13, 14, 15, 16, 17, 2, 278, 18, 19, 3, 13, 4, 20, 279, 21, 280, 1, 39, 23, 24, 28, 281, 282, 29, 30, 31, 32, 283, 284, 285, 40, 34, 35, 37, 41, 42, 286, 43, 44, 45, 46, 47, 287, 48, 288, 49, 289, 50, 290, 291, 51, 52, 53, 33, 54, 292, 293, 0, 294, 55, 56, 57, 58, 59, 41, 60, 42, 61, 177, 43, 44, 62, 295, 63, 64, 296, 297, 298, 65, 66, 67, 68, 299, 69, 70, 71, 5, 72, 73, 300, 74, 75, 76, 77, 78, 79, 7, 301, 302, 80, 303, 0, 81, 304, 82, 83, 84, 85, 87, 305, 88, 89, 306, 90, 91, 92, 93, 307, 94, 95, 96, 308, 98, 309, 99, 100, 101, 102, 8, 310, 311, 312, 313, 103, 9, 314, 315, 316, 317, 318, 319, 320, 321, 104, 105, 10, 107, 108, 109, 322, 110, 11, 111, 112, 113, 323, 324, 114, 115, 116, 0, 325, 117, 12, 118, 119, 120, 45, 13, 13, 326, 327, 121, 122, 328, 123, 14, 124, 125, 15, 126, 127, 329, 153, 330, 17, 128, 129, 130, 21, 131, 16, 132, 331, 31, 133, 134, 332, 17, 333, 334, 3, 335, 4, 46, 135, 136, 5, 336, 337, 6, 137, 338, 138, 139, 339, 340, 341, 140, 18, 342, 343, 344, 345, 141, 142, 0, 47, 143, 144, 145, 146, 147, 346, 148, 19, 48, 49, 347, 348, 349, 350, 149, 351, 150, 20, 151, 152, 352, 153, 50, 99, 154, 155, 353, 354, 355, 356, 357, 1, 358, 359, 360, 361, 362, 363, 155, 156, 157, 161, 163, 22, 13, 158, 364, 365, 366, 367, 368, 369, 160, 51, 370, 371, 164, 372, 373, 374, 165, 375, 376, 377, 378, 166, 379, 2, 380, 381, 106, 167, 382, 383, 384, 385, 386, 387, 168, 388, 389, 390, 169, 170, 391, 392, 393, 156, 172, 394, 395, 396, 397, 17, 198, 25, 398, 173, 399, 200, 400, 201, 401, 203, 205, 402, 206, 47, 174, 175, 176, 177, 36, 403, 178, 404, 405, 179, 406, 208, 407, 408, 0, 181, 52, 54, 124, 409, 126, 410, 185, 186, 411, 412, 17, 211, 413, 188, 414, 7, 22, 61, 30, 36, 415, 38, 416, 417, 418, 62, 166, 419, 65, 215, 55, 0, 3, 420, 421, 1, 2, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 441, 442, 443, 207, 444, 445, 446, 447, 448, 449, 450, 451, 452, 66, 453, 67, 454, 455, 456, 68, 457, 458, 459, 460, 461, 462, 463, 464, 465, 466, 467, 468, 469, 470, 471, 472, 473, 38, 54, 55, 474, 475, 476, 477, 478, 195, 479, 480, 201, 481, 210, 80, 482, 483, 484, 485, 486, 487, 81, 488, 82, 86, 92, 212, 489, 490, 491, 492, 493, 494, 495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 94, 508, 509, 510, 511, 512, 513, 182, 514, 515, 516, 213, 517, 518, 519, 214, 520, 521, 522, 523, 524, 525, 526, 527, 528, 95, 529, 220, 530, 531, 532, 96, 225, 533, 534, 535, 226, 536, 97, 537, 98, 106, 538, 193, 194, 539, 196, 540, 197, 541, 202, 542, 543, 7, 544, 233, 3, 545, 235, 112, 113, 114, 138, 56, 546, 133, 134, 547, 4, 548, 189, 549, 550, 180, 551, 552, 553, 554, 555, 556, 557, 5, 558, 559, 560, 561, 562, 6, 563, 8, 9, 10, 11, 12, 13, 564, 565, 566, 567, 568, 141, 569, 142, 570, 149, 236, 151, 571, 203, 572, 205, 573, 574, 152, 575, 576, 577, 578, 14, 56, 579, 580, 581, 184, 582, 583, 198, 584, 585, 586, 587, 588, 238, 589, 153, 590, 591, 592, 593, 594, 595, 596, 597, 598, 154, 599, 600, 601, 602, 159, 603, 604, 162, 605, 606, 607, 608, 8, 609, 610, 611, 612, 613, 614, 615, 616, 617, 618, 199, 200, 619, 207, 620, 127, 621, 208, 16, 622, 623, 624, 625, 626, 627, 57, 628, 164, 165, 629, 630, 631, 166, 632, 167, 170, 171, 173, 211, 633, 178, 4, 168, 169, 634, 635, 9, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645, 172, 17, 179, 181, 646, 58, 183, 186, 192, 1, 206, 209, 60, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 647, 241, 648, 227, 649, 0, 0, 74, 57, 650, 651, 652, 185, 188, 190, 226, 228, 229, 62, 230, 63, 245, 653, 18, 654, 211, 231, 232, 233, 234, 236, 655, 656, 237, 657, 658, 235, 238, 239, 240, 241, 64, 659, 660, 661, 242, 243, 662, 663, 664, 665, 244, 10, 245, 19, 20, 666, 667, 216, 668, 217, 669, 670, 671, 672, 673, 58, 246, 69, 674, 675, 676, 677, 247, 248, 5, 678, 679, 680, 681, 682, 683, 248, 249, 250, 684, 70, 685, 250, 686, 687, 688, 689, 251, 7, 252, 253, 255, 256, 690, 691, 692, 257, 258, 259, 693, 260, 218, 71, 261, 262, 263, 264, 694, 265, 266, 267, 695, 268, 269, 271, 696, 8, 270, 272, 273, 219, 220, 72, 221, 222, 697, 128, 73, 74, 75, 76, 77, 698, 699, 254, 700, 223, 701, 274, 276, 277, 702, 703, 224, 228, 704, 705, 706, 229, 707, 708, 21, 709, 25, 230, 710, 231, 711, 712, 713, 714, 78, 278, 281, 715, 1, 79, 54, 81, 82, 56, 86, 58, 92, 60, 716, 283, 282, 284, 717, 718, 232, 719, 285, 720, 233, 721, 92, 96, 59, 286, 287, 60, 255, 105, 234, 722, 61, 723, 239, 724, 62, 288, 289, 2, 63, 290, 80, 291, 292, 64, 293, 725, 257, 295, 726, 727, 1, 728, 261, 729, 294, 63, 730, 240, 731, 732, 241, 262, 263, 733, 734, 735, 736, 296, 298, 299, 243, 737, 738, 244, 264, 271, 739, 246, 740, 741, 247, 742, 743, 249, 83, 300, 302, 304, 64, 305, 306, 0, 251, 307, 308, 744, 745, 746, 303, 309, 312, 311, 313, 314, 319, 65, 0, 315, 316, 1, 317, 320, 2, 322, 323, 69, 325, 326, 327, 324, 328, 330, 331, 70, 332, 333, 334, 336, 338, 339, 340, 341, 342, 343, 344, 346, 347, 348, 349, 350, 352, 353, 1, 252, 354, 355, 356, 357, 358, 359, 360, 361, 363, 364, 365, 366, 367, 368, 362, 369, 66, 370, 84, 2, 67, 372, 373, 253, 747, 374, 68, 71, 375, 379, 380, 382, 383, 748, 384, 385, 386, 387, 371, 377, 749, 388, 390, 391, 394, 396, 400, 401, 404, 406, 407, 409, 410, 750, 411, 376, 378, 415, 381, 392, 389, 393, 395, 398, 402, 405, 408, 412, 413, 414, 416, 2, 751, 417, 418, 419, 420, 421, 422, 752, 423, 753, 754, 403, 755, 756, 424, 425, 757, 426, 427, 72, 78, 94, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 758, 254, 0, 759, 428, 430, 760, 761, 429, 762, 110, 763, 764, 765, 256, 258, 431, 432, 259, 433, 260, 434, 435, 766, 767, 436, 437, 438, 439, 440, 441, 260, 768, 442, 443, 444, 445, 446, 447, 448, 449, 106, 450, 69, 451, 769, 770, 111, 452, 453, 454, 3, 261, 455, 456, 457, 458, 4, 460, 771, 461, 459, 262, 263, 462, 463, 464, 465, 466, 772, 467, 773, 468, 469, 470, 471, 472, 473, 474, 5, 264, 475, 476, 477, 478, 479, 480, 481, 483, 484, 485, 486, 487, 488, 482, 774, 265, 775, 776, 70, 491, 115, 492, 499, 116, 489, 490, 493, 494, 777, 778, 779, 495, 780, 496, 497, 498, 500, 781, 782, 509, 783, 266, 784, 510, 512, 785, 513, 786, 787, 267, 501, 502, 503, 504, 505, 3, 117, 118, 520, 788, 516, 1, 789, 790, 4, 523, 522, 119, 85, 11, 506, 791, 507, 508, 6, 792, 793, 107, 71, 794, 795, 517, 524, 525, 796, 268, 797, 798, 269, 528, 799, 270, 3, 800, 801, 511, 514, 515, 518, 87, 519, 802, 531, 521, 526, 527, 529, 530, 88, 532, 533, 535, 267, 534, 536, 274, 537, 803, 539, 538, 542, 804, 805, 544, 806, 807, 271, 808, 540, 541, 12, 809, 810, 811, 812, 543, 813, 545, 120, 546, 547, 814, 548, 549, 121, 122, 123, 815, 272, 816, 550, 551, 275, 817, 552, 89, 818, 819, 820, 821, 273, 274, 90, 277, 276, 822, 823, 553, 554, 824, 825, 4, 826, 827, 828, 829, 91, 830, 125, 831, 832, 833, 555, 834, 5, 835, 836, 557, 837, 838, 94, 7, 839, 840, 841, 126, 842, 843, 844, 845, 278, 95, 96, 846, 847, 848, 279, 849, 558, 556, 559, 850, 851, 852, 853, 560, 854, 855, 128, 856, 0, 857, 858, 859, 129, 97, 98, 101, 130, 131, 860, 132, 139, 140, 143, 861, 862, 102, 863, 72, 864, 865, 277, 866, 561, 562, 564, 565, 566, 567, 568, 279, 867, 144, 868, 869, 108, 73, 870, 74, 871, 5, 569, 570, 75, 145, 571, 572, 103, 573, 574, 109, 575, 872, 280, 281, 293, 576, 577, 578, 282, 283, 579, 873, 294, 874, 284, 875, 295, 285, 296, 876, 580, 877, 581, 582, 878, 583, 879, 584, 585, 586, 587, 588, 589, 598, 880, 288, 881, 289, 290, 882, 883, 884, 76, 590, 885, 886, 591, 887, 888, 889, 890, 891, 0, 892, 893, 894, 895, 896, 603, 897, 593, 146, 898, 899, 592, 604, 900, 901, 605, 902, 609, 903, 904, 594, 595, 596, 597, 905, 612, 906, 599, 600, 601, 602, 611, 606, 907, 908, 909, 607, 608, 6, 7, 610, 613, 614, 615, 910, 297, 911, 912, 913, 291, 616, 914, 301, 915, 292, 916, 917, 617, 618, 918, 919, 920, 619, 104, 620, 621, 622, 623, 624, 2, 921, 922, 923, 110, 77, 625, 626, 627, 629, 78, 632, 924, 634, 635, 925, 637, 926, 927, 79, 636, 928, 300, 638, 929, 930, 639, 931, 932, 933, 934, 935, 936, 937, 938, 939, 302, 640, 940, 941, 942, 943, 147, 645, 648, 944, 945, 650, 651, 652, 641, 946, 947, 948, 644, 653, 949, 0, 950, 951, 952, 148, 8, 150, 654, 646, 953, 655, 151, 647, 649, 954, 656, 304, 955, 657, 658, 660, 956, 155, 156, 957, 305, 300, 958, 661, 959, 662, 960, 663, 961, 962, 664, 665, 666, 963, 667, 964, 965, 157, 966, 1, 967, 968, 668, 669, 672, 673, 670, 105, 9, 671, 674, 969, 13, 970, 675, 10, 971, 972, 973, 974, 975, 306, 676, 158, 976, 307, 977, 308, 677, 978, 678, 979, 310, 679, 313, 314, 980, 322, 161, 163, 680, 80, 681, 981, 982, 983, 984, 985, 986, 987, 682, 988, 683, 989, 684, 323, 685, 328, 686, 990, 687, 106, 991, 992, 11, 688, 689, 690, 699, 993, 696, 994, 700, 995, 701, 691, 329, 692, 107, 996, 997, 12, 998, 703, 694, 330, 999, 331, 1000, 1001, 1002, 698, 174, 175, 1003, 176, 297, 702, 704, 1, 1004, 332, 1005, 1006, 108, 1007, 109, 1008, 333, 1009, 334, 1010, 81, 3, 4, 705, 706, 1011, 111, 82, 336, 1012, 338, 707, 1013, 9, 1014, 177, 708, 709, 1015, 710, 1016, 183, 311, 711, 712, 713, 714, 715, 716, 112, 314, 1017, 339, 110, 1018, 111, 1019, 112, 340, 717, 1020, 315, 341, 1021, 186, 1022, 1023, 718, 1024, 1025, 719, 720, 187, 83, 721, 191, 722, 342, 723, 84, 724, 192, 725, 726, 113, 727, 728, 729, 1026, 730, 731, 732, 1027, 733, 735, 13, 14, 737, 15, 1028, 738, 740, 739, 743, 1029, 747, 741, 16, 750, 17, 1030, 742, 744, 1031, 193, 756, 1032, 1033, 748, 751, 1034, 734, 343, 749, 752, 301, 753, 754, 1035, 1036, 1037, 736, 755, 757, 758, 2, 114, 85, 115, 759, 760, 762, 1038, 1039, 1040, 1041, 1042, 1043, 761, 764, 1044, 765, 766, 1045, 344, 86, 87, 767, 768, 88, 1046, 303, 116, 117, 0, 118, 119, 346, 771, 1047, 1048, 1049, 194, 772, 773, 774, 310, 770, 196, 1050, 316, 775, 777, 1051, 1052, 347, 776, 778, 779, 317, 780, 8, 197, 781, 9, 10, 782, 785, 786, 787, 789, 790, 791, 792, 793, 794, 1053, 795, 783, 1054, 798, 1055, 797, 1056, 120, 799, 800, 1057, 199, 129, 1058, 1059, 1060, 348, 1061, 1062, 1063, 349, 350, 801, 351, 1064, 803, 805, 1065, 121, 1066, 1067, 808, 1068, 18, 352, 123, 1069, 1070, 809, 810, 812, 11, 1071, 1072, 1073, 19, 354, 125, 1074, 813, 814, 1075, 200, 204, 206, 355, 2, 356, 357, 1076, 358, 1077, 1078, 359, 1079, 1080, 126, 1081, 128, 1082, 1083, 1084, 1085, 1086, 318, 208, 802, 1087, 319, 115, 360, 89, 320, 1088, 804, 815, 806, 816, 818, 819, 820, 327, 1089, 821, 328, 823, 1090, 116, 90, 824, 825, 131, 1091, 132, 1092, 1093, 1094, 1095, 136, 1096, 826, 827, 361, 1097, 1098, 1099, 1100, 1101, 1102, 5, 15, 1103, 1104, 1105, 828, 835, 1106, 1107, 829, 839, 841, 1108, 842, 843, 1109, 830, 844, 1110, 846, 363, 10, 850, 11, 12, 1111, 1112, 831, 833, 852, 20, 21, 209, 834, 1113, 215, 1114, 91, 836, 837, 838, 847, 854, 1115, 855, 856, 1116, 848, 849, 840, 1117, 1118, 1119, 321, 12, 216, 217, 1120, 851, 858, 13, 859, 332, 1121, 364, 366, 13, 1122, 14, 1123, 857, 1124, 862, 863, 860, 861, 864, 865, 220, 1125, 376, 866, 1126, 1127, 139, 1128, 867, 15, 1129, 22, 868, 140, 1130, 1131, 1132, 1133, 1134, 381, 869, 16, 1135, 141, 393, 1136, 1137, 1138, 1139, 1140, 397, 872, 398, 1141, 399, 1142, 416, 1143, 1144, 417, 1145, 1146, 1147, 142, 143, 7, 8, 870, 871, 873, 874, 418, 875, 329, 1148, 1149, 419, 333, 1150, 1151, 334, 876, 14, 877, 218, 878, 1152, 92, 1153, 1154, 219, 221, 222, 1155, 1156, 223, 93, 879, 880, 1157, 0, 224, 881, 882, 885, 887, 888, 889, 890, 891, 892, 1158, 893, 894, 895, 1159, 1160, 1161, 1162, 15, 896, 1163, 1164, 883, 884, 886, 1165, 1166, 1167, 898, 902, 904, 1168, 335, 225, 226, 897, 1169, 1170, 899, 900, 901, 903, 905, 336, 1171, 1172, 907, 1173, 908, 1174, 909, 1175, 1176, 420, 131, 1177, 1178, 23, 421, 1179, 1180, 1181, 1182, 422, 423, 906, 424, 1183, 1184, 911, 1185, 1186, 1187, 1188, 427, 428, 912, 430, 1189, 1190, 227, 134, 1191, 1192, 1193, 1194, 1195, 337, 339, 340, 1196, 94, 431, 432, 343, 915, 913, 914, 916, 917, 918, 919, 1197, 228, 229, 434, 433, 435, 230, 1198, 1199, 1200, 144, 920, 921, 1201, 1202, 922, 1203, 923, 1204, 1205, 924, 16, 926, 925, 927, 928, 1206, 929, 930, 1207, 931, 436, 1208, 1209, 345, 934, 437, 932, 937, 935, 938, 939, 438, 1210, 1211, 439, 455, 941, 456, 1212, 1213, 147, 1214, 942, 458, 943, 459, 1215, 1216, 148, 1217, 462, 945, 946, 947, 463, 1218, 346, 349, 1219, 948, 351, 354, 951, 464, 47, 1220, 149, 150, 1221, 1222, 467, 953, 1223, 1, 1, 954, 955, 956, 957, 958, 959, 960, 1224, 1225, 1226, 961, 962, 1227, 963, 357, 964, 1228, 966, 1229, 468, 1230, 1231, 151, 1232, 1233, 24, 1234, 152, 1235, 1236, 25, 135, 360, 365, 367, 469, 470, 967, 368, 1237, 154, 156, 157, 1238, 1239, 1240, 1241, 235, 158, 1242, 968, 1243, 969, 970, 971, 1244, 972, 973, 974, 976, 978, 979, 975, 238, 1245, 1246, 28, 471, 1247, 1248, 29, 472, 1249, 371, 1250, 377, 977, 1251, 1252, 1253, 239, 240, 980, 17, 241, 246, 981, 254, 1254, 982, 1255, 983, 986, 984, 473, 1256, 1257, 474, 475, 1258, 1259, 476, 477, 259, 286, 287, 478, 479, 288, 985, 987, 988, 292, 293, 1260, 481, 1261, 1262, 485, 1263, 378, 480, 483, 484, 1264, 1265, 989, 991, 992, 1266, 1267, 1268, 1269, 1270 };
    protected static final int[] columnmap = { 0, 1, 2, 2, 3, 2, 4, 5, 0, 6, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 5, 1, 19, 0, 20, 7, 21, 22, 23, 5, 6, 2, 24, 0, 25, 26, 27, 28, 7, 18, 16, 29, 30, 0, 31, 22, 0, 32, 27, 33, 0, 18, 12, 16, 34, 35, 36, 37, 38, 35, 39, 40, 0, 41, 42, 36, 43, 35, 39, 40, 1, 44, 45, 15, 46, 44, 47, 48, 49, 50, 34, 51, 49, 52, 53, 5, 54, 55, 1, 56, 57, 58, 2, 59, 3, 60, 61, 62, 12, 41, 50, 63, 62, 63, 64, 65, 66, 67, 68, 69, 64, 70, 65, 66, 71, 72, 71, 73, 74, 5, 75, 0, 76, 73, 77, 78, 79, 80, 80, 5, 81, 0, 82, 83, 6, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 16, 81, 86, 96, 97, 98, 36, 90, 99, 100, 97, 101, 96, 6, 35, 2, 102, 0, 103, 40, 104, 100, 3, 105, 16, 102, 104, 106, 107, 108, 109, 0, 15, 110, 111, 112, 107, 113, 114, 115, 51, 116, 7, 117, 7, 118, 119, 120, 121, 122, 123, 124, 125, 0, 126, 1, 127, 36, 128, 129, 124, 130, 0, 131, 132, 0, 133, 134, 114, 135, 136, 137, 117, 2, 138, 120, 139, 140, 141, 6, 142, 3, 143, 121, 0, 40, 125, 144, 145, 126, 3, 9, 146, 40, 0, 147 };

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
            final int compressedBytes = 2938;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlXTuPXLcV5tBXC3phw/Qisre8ayjwBFARVw7UhLuJH0mjhW" +
                "DBMQw3bpJCZSpDBXdtBKtOhQqVAxUG3PknLAwVgis1hg39Gt/H" +
                "PO6D5HfIwzuzii6glVZnDnlInudH8s7Pb924+86DufrDtbObP7" +
                "725O7lh/cPzHf/+OVte+fZLXtD/Hy7ov8w/7Si3xvQfyPRq/bf" +
                "/WH+r0c1/W9P7j798P6dXvtc+q7l47YP+IWYicGju79MLd/lmz" +
                "f+fu2s0g8hbv54fH79cm9+R1j1USWEEVdh/d/y9t+lT6bfL27/" +
                "+6t3H8y/fPTHb+89+eT7z5/+9f7XJ//74teK/uKbiy3oD2H9ir" +
                "P54aN6/Wbn15/uzQ9667dj+aa2n53Lt2P/J5QoRP1z+VS/aLn8" +
                "t6n9x8Tjk11nZTo/q0duwX+99PFlx/TRY/u/sufnZZ9fpvx9+y" +
                "xW8d104zuMb23+5qVPqt8Z1g+Nb0r/cCDENb1xSFLoz3T109ZL" +
                "QPJPlPzkQSPfzSnG14i8ShL3qj+Hp39urdRIqcTk+r/ML9r8cN" +
                "bkh7384sXt95f50dm9xTo/avhp+VGnPlm045db1d/8+fvgmRWh" +
                "/B7RUX6O6D+19DY/PG7ywzVdZqDD+gHML7f9GP5Fy38SIx96uP" +
                "6Nm7+v+H32ua3+ffxd+qKlyy4d5a9cel/+7xv5T5byf0PJnwE/" +
                "t/0hXXTowj0+kXN8zqTP9P1PyP9D+1EmaD+ofnDK53i0j6AAI8" +
                "jPdk13rW9/frP3L9LlC8Z3mN+mxH+U3y0DtBTl2gJ6qoL4p6Z3" +
                "J32p8lz5REz/7Py1w+9av/19cf1AVepz7azqvzTig/9q893xw5" +
                "m98/jWSqR+flt289uK/kYo/0V0Qn7Nan//n97xXdzK4D8Rf5e+" +
                "aOn9+Arqez497N/7Dp7wDNtv40cR+mDhCQFF0P9bEB8sUb6U9n" +
                "V4omeOyKaV7DdraaFQGSuK2v21gigrDuu/541D1HVDsieoHfFX" +
                "P8rNJwow+Ej9kdWwtK5HLFv7elzbv64+ddnYFyF+kx8Zxb+eaD" +
                "A/YX48PsOzD7J9Ko9+tOMrR+MriXTQPtN+4Pz11F4m+Jcs9l/4" +
                "9dNlPyv7V/HjP3Tpj8s+V+2j/qH+GWRTuvfL6dI5GfdqZPGfyB" +
                "B0PvtRI7qmrb+l6reM9386PT7ZSH5y+8pHD/sPAewn9/qNPgj0" +
                "f2wfuleiK1//1h3OR/HIxJmfhfJliq+W5t+RfeTOL63H1rVw50" +
                "dgfAR8t+Dgu1x89Ej/JivrKN7Te3L2FyGOrJ2JUpdKLEpxnJg/" +
                "GLrH3NL+wiGobw599c225PPhm838k3Tak58w87+p8clY/iF+mc" +
                "Av4vgNxb+kVxFwfXRf7IH/iZY/cv1/6uIDA/+y2r8J0TF+YOD+" +
                "Bwe/4J5PIM+fjqkONZnOx2cj138UFMPxj4vPW1Bf4fgZjW+KSP" +
                "xUhPBdB/7rgolWdOng3yk9A/4dnP+E9YvD/0SSftPzY259xPQv" +
                "dP/VtR9Fz9/T8ecGX0f4u4MuBvg8wseD+D0XvyPsD0xKj5fvs2" +
                "33/wZn/wTZP9rfcNBFl87ev2HmF1370878WyB8HOmvrC34qOIY" +
                "4Rt7Au5/EvybjPevHRevTpc+XMpuLaKo/IBOOP+Y+Ng0+bLjC6" +
                "j/NKAj1/xv6JLJnzi+uZBWzBZ/Ov9P9V9f2AM7e7TQC/XxvCzs" +
                "3hb6d+yamYz6BfF3Mn4oOyukJXV/ao3/VJ278J8j/aylqyVdtH" +
                "QxpAs3XYhPW+9Q967qSbSr/KncFBJyrbEb/0j0n6j9ev4CCBuS" +
                "H42fKz/G554514c6/wR6Mz/vVWNIGf+KX4747WZ+AuPH+mOC68" +
                "tdvw2/9K9v4V9fkn54x0+wH9D/eH7E2L4Kin7uA/lkkn0j/1L5" +
                "OV58mbeMVX5kC1v5TSu1qpufVd2/49hfsJEFG+S/ZMYf5vjF8z" +
                "7/KP8i5ic6dX8T1pfB+wXZ929ikVlu/qDCHcH4foHi2ykY/2nc" +
                "/MTS4f5brvwyNT9j2hfaX4b2SbVfLn2a+UP6ifEntD+8kkZuzm" +
                "+IIb7qkNcUJPvmylePs65fy2bwXXnKSLpMo6/PL7jre9y/WX7a" +
                "2/5Qvz3269VvUv/+53mffx2fqOPn4r8T6w+sT4xnrzbkf6+O/c" +
                "P9I3T+Zt7gV4sjUdXv4vWyoj+0VX5m5qH1jfD/ED8zzk2Zaemj" +
                "+JKv/S3rx9TxHe8P7UL+jP4Dnl9N6T9yfXVG/3XF9Ce6fgh+sH" +
                "TWD6qxv/2+fyvsbLbV+iK1fer6+OrHba2/u3+Mj4L65EKcl079" +
                "XqzkeLshFfVqlqZWASOO9czqi0Oa/E1+0z8f0Zs/QBee9u125h" +
                "/gJ6p3vLR0XOp0rR+jvhSR9pvn/kYgfrvwH03nh25nvX8Wsr8C" +
                "nB8qyP7DUvAtevv4/GiW/L5I9o+YPmn8Q/cffbBRfH7Rk0/mym" +
                "9cMvHxOUb8z10/TR5fefgm+/1LYHx8fCdl/5Ibv7ZY38fnl7z8" +
                "3vrij6d97N/OOfuP2D4G50PJ/nWZfzHPxxDktz18bBl/VNN/QD" +
                "7Z9m9bHlUzPdycrzMJ/ksy6Xn8z6jayVXfxsbX1foj/t3aN7o/" +
                "gs9XG77+ku3Xcf4f45fo/TDAPiPXz3LmxzU+Hc5PKPlft/If+g" +
                "fYSlh+eH5QOe63LvdH6gfdr6C8/yrEj/qfWn/T8i+e/kT7r3j9" +
                "MPnxQw9/0vnI0fl1zv2FcPvs+7HCkx9Zknzc+yMEftb7f9D6Eu" +
                "5ffNDiV6PJvGh/NbKXn7nxqRB90v0FvD/M1f9p4zP3/gT9fpaK" +
                "lz/D+Zr89xci72/kPr8SmV8i1AOtP17fE558TkG1A72THvzucY" +
                "PflaLF78o1flcuz+cR2g/RyfhumD/9/TGCP3+k+lsB+3K3Tz8f" +
                "JyPaj6CD/WlC/AvyQzrqn4tPTI1/JPe/oOEbzPsv7Pszrwxdpc" +
                "UPsn941eeX5/8n49+1/Nz9qd2vLy/+vuT06e8nMukR41MOQobv" +
                "pwg+o/sh4/svLHzZd79G+dvXffnQ/aRwfh2+X0Pa/7DM/CXWf+" +
                "iQd5t5x+fbPxe90wszkF/76D5++v2p4fz35cd07/0pFP+D96te" +
                "Hrpi0vf/P+nO6iq//aP7ewH9jc1PB8ZHuL9LGl/a+Ln3M7n2ny" +
                "8+7YP6z11/ZKhPif4f1T9EenJ+rGhpTSz+Qsxf/fdjJsaHyPkz" +
                "d37CD74/lA0/i1x/3v5D2vmAjPJB/ufc+jHoX470xRLfLRr/VT" +
                "b+q17cGt/VKfi97/5u0v57NP4Ziz+v/+07X4vo4bKLiI8SUmxU" +
                "P4Tza9/0Y/0/Ze6fiHMViu/D9h31s0e+Vf0od1qfc+uv9YTKSf" +
                "BHNj5Mfr+cjJh/1/vxEvcndkU3tPmh52fo+x0S6dh/UN6PzsZf" +
                "/fefafhCRPwc8IfvV2P9fh6+38zN78H8Xonv5y0o9pGGL/LxUc" +
                "PzX9z7J9n2jyaic+sPbn2G6VPXn7z6Kdv+32Tzl0k/PP4n2/5F" +
                "avtm7YHc9sWkdzxcmn2j/nn7+3n8Z7lpavD9WWx+9vl6pv9mnv" +
                "9zfn9Q53xhlvcPFLH5cdb6ZOL4K2B9OL4fr+v78YMZkqH6wE9f" +
                "tz/w7537957+aXRa/2P/Wog842PTcf7CGj+5gGKdD5zu/Jni+f" +
                "8M9oniQ5Au1Ul7v1JL6fp+Jfz+Hth/Ar6E1jerfoTlR/5JUPGJ" +
                "nHQR8X6HSPqVGN8rRI/XPx1V/+D9g7j+neYoQ6oJ3n+y6/qfi/" +
                "9T6VvSL679Dv4b3y9Kmd+Y+pe6fzLV/g2Tn1Bf1xBDaTb2V4fD" +
                "srkfbsXo/S2x+Cm6Hw0hHu3QD8r7hXLRddgtoPfLoPMPYH8Ivn" +
                "+Gib+y81Py+4di3u8T6R9J+K68mvjt9PePee+fSNr/FSLX9wtP" +
                "vb9Jqg84/v13tOXjEA==");
            
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
            final int compressedBytes = 2819;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXTuvXLcR5mGOLqiLBKEvLEXlcaBiCxVJ5UDVkWMHTpoIQg" +
                "QknX9AylSBC94LFUqXwoXKhQsD7vwTNoALwZUbA/45OY+9u+dB" +
                "8pvhkLtXgE6jxyxfQ3I483FmqNTh08rz/fDrxy9/8+3m71/du/" +
                "7nd3/8+uX3H3/5on31558+cC/ePnWP1Q9/ffzy4bebzz/s6b/4" +
                "+uXu4y+v9vSfe7oyKvrB+oV0z2f9PdHgJ346u33HGz/q/7z8N4" +
                "A/azqan45eq8Ov6tv2Wz7//OvrWG0a/8V0PH6naq2a/Q+NU4/6" +
                "Pzfd/wz1hOi6WfRjbL/y9a/20Z23O9W6//v2L4d/3h/at13/ts" +
                "T22337oc/e/qGn69fMK64bdbEos6XOz768DpWP14/mB9c/fPL2" +
                "Lyftz/iP+APodk53I90s2ler8TP2n2h/gf7B9dUq1ue45QHdtJ" +
                "4Fb/V6zxEZ6rj9L0zH8gt8VR09P2H5VnT+k9an9/xwVPkf5c/u" +
                "V48/vXe9MR8q9eS7ZzcPdhebF8qZz2w/sKF/bdHx9+3X15tHQ/" +
                "vV0P7VrH0o/2Ttk+anjoiXmf5wFBh08cTp/6jffLLv378p84P6" +
                "B/gL24f7w84Z4ELbKk0+79ePCa6fsJLhr98pjjTkl+fXr0/avx" +
                "T5VVC/RfML9f+E/cWRz5g/4vPhg2Gz1Cth8Roc3452kLbABEN0" +
                "0kGtB3EyLha7ONqqZUv7083N/tcG5VviN1YP7Xs0f7B8iqJjhX" +
                "QKWxLrdxTeyvtPLm/Ujf/82voXjVv+J1jfqfZxqP2lOAETdaXU" +
                "vckJqpX9W2+pur7fOnn9W7r8l+4Psf7VglNN/9LeypDeRHv0/H" +
                "fj4mi1NpjusY8CG8nPn8u/qAdXpvvRvevuJ02rfv8v27569t/K" +
                "vXj9dCyv+fqfLUxnzC/iz7nnR3q+Cu0Luf3Z8vYqW3+2Hv3Psv" +
                "XvZrV+m2z4ZTsoMc26yAUHPwnV75B9HD0/tOlEru3Xp75dn11H" +
                "bVfpbly/gF5+fxfGj/PofxBritZPwj9o9VfJ/Cldvw7U/66vnx" +
                "R6TvzW3cHxvaefjN70K6Da3r/5ovvLP1wnql9t7db8aaNql+V8" +
                "ScVv38/P3Ti/pOcbs7xjmGQnwW+o9o//+8j+rLv1Xf+2U36qP3" +
                "T/dq5T5Wxj1LZRz6brHzFCB+p/O69fjfWrff3npiP9H5ZXnw/8" +
                "6U37vopq4Mug3zSH+ZnR1YyO60/Vr2j2DZ1/OpX/Q/9C5ZV6G+" +
                "WflP8Z+g/ab0XtZ9mfAvkqrf8j+2Yo36gL3ZdvVuUz6K+1RD+U" +
                "6o/nLo/1o9HuaFztertVK9Ozvxc0D6f4iwGw7Ds6frOTjQ/xZz" +
                "MWrNRm5K/TdsZfjD9H60/wr+PpBxC/+jELvsSz/97T7wzdoKJM" +
                "fHNdP/F+JxH/TPaP3Ebw4cj+9X0R/xKELxLwbfA959Edpf8c+Y" +
                "za5+Fziun/HNY/mkH/kPP3Xbd/28T90Rz3R10Sv9yB8e/OwB/r" +
                "gYyr8cfNEsBuPUUy4g8mtEUcEEs0+7yXQ8rrf9xkoi8U1el3Ev" +
                "xuF/Bfbpj1h/oPxo/1r4B/ciOQD5zz+27LL6n/rdx/152ZLuxf" +
                "M3B4W6kvun/c7xeVc93x2G6o+y9D/++0/X7u+qV0qf0nw4/39n" +
                "Ot2J8raF/mvL8srV/I5F8Z/6xj+23vv/u/xnQT/KSp3IPdhbnq" +
                "/vuz4ReE8sXp2P671Wzny65eSigd1K/MQD+UHxuvXVUR989Y/n" +
                "JS3k7Kg/YBvff/Cszv1H81Nr7C/k9nXh/M+72qBP4SOf+w/xFF" +
                "fnnG09Y0/IXpfxv0b5nFX6L4zRT5rmL4LvP8odhPmdZXc8u3ID" +
                "4f338AfzazKWmm3XEkRhLk5zntB7Z/nmPif9nwz0R8Uo6vYnxn" +
                "ar3uzy8zjy+uQZul6cz9zfDv38cvH/jkzjK+8/FnOX4+fb6c9u" +
                "unKTQ+dv3U+3M74sP8+3Of//Zsf2P5Ep0/af3tnBGOjf9S5FMN" +
                "zj/m/LPkF4Veh8/3FjA8A/6E9JtaYl+C9VlafwH9P9y/2NF/ZL" +
                "x/UYf7F6jfyfGJhPiPdP9xtc4v4GnfTs8fmnwJ5S+B9lmb0L/S" +
                "9p2ij0+afwPllwHyk3Q+xPLfSOPvsX4ni19E+X+gfgji/yn5Xe" +
                "L5F+LlUf6apPYZ+EcO/TtuX8fz/4jjV1H+hlzju/S2nxafr2bn" +
                "78P/DPGNTxbxjQd8s6MbT/wjDf/0xidOU2wg+UbSz2P2aaH9Hz" +
                "v/mOdnwft/Qn4+ov7LWZ+L8zmCz6H8OtL8M6TysSGC8RPyvwjz" +
                "3zDlS5jOyC/D6J+h7t9z+d/x52++PuL8TfMvXVbUHP9ary83wF" +
                "erY/4Qvcwfws5/4BbnN8J37z49E3/S7CtC/hOUvwatn5uk/BpQ" +
                "P0u8n1Cp9xPoIC99Px3SL2Tnbz1nWTuhOooJjOQLzA8hzD9R+v" +
                "5h3r/G81sfnTH/RonGXzr/Uen5O4l/l8C/+a7379x06H8tXJ+l" +
                "8SmC/XED7K8bcP8EBHkp+4iGP5bOnz6lGw+BlH+tluATLVl/NQ" +
                "nrA/EPxldCfPIQf6qn8aMhfDIfPjVfP3qmXzHiRwE+RMZnw/VH" +
                "v3T++/DbxPy/2fSPmP4Qin8E8X1Qv/7R2z+z1q/j/S9Gp+pXJg" +
                "1/odhf9dL+YOTHh/YLoufJD1bOPycefybmf3f+mqj8Esancetf" +
                "47u6pH6ST78MyhcR/pyWX/Xc+UtL0xU5f2gG/AbEr8n001P759" +
                "Lwl5zxPyA+C5+f0fKU/Prx/P6F7Qth/JjYvpDOL4pPOdhvgfuh" +
                "1Pv1+f1Uncp/ufynzp+/f0P8x/Us/qObCHuI//DTj/Eh0vER8d" +
                "Xk+AkUnyGOr6DQJfiUdP2Q9z+yT/z2gTafjPqh1XoQgW/W8Q1R" +
                "1BTpl6/j+kdafAVH/0blWw3vH+L6F7g/8ddPvz+Vnc9S+X2S93" +
                "c49yse+SC6fxPezyH+5Msvn2Y/I/uzOH5Ofr+mkH1Mud+s4/hN" +
                "byI27bF8//Nm8M9zBP9uvL6i9aOFCPu3Km/jCIn3J0E69s98Pj" +
                "9f2fNL9T+hBqgVuV9K95+n+ecHxyd+P2rhX1RP/IvaLPcbKfc/" +
                "p3s/Q97/sveLpe2/fP7dOfnHpDPy1+fGJ8TvXwj9w9H+J/n3Cv" +
                "znl/yruPhksH3q+pTpr7cf9u+O+KfSzq8Q/iyM/4AbRPY+LuX9" +
                "W5H/vtC/HtnnWP8E789K5WeafnOy9XEK/ojsR2F5sX8+wf6oRf" +
                "Ylfh89Wj94Xzep/4QQzEP/U/Q7uv6J64fylVpep5WP9w+/nyot" +
                "T9RvLI+N2e6HAP/w+5Qp9ydo/uy6POt9iBT8KYBPivErMX6J9P" +
                "c4fpkkPxn4mHfa3534AeS/jd/n48ifBttKi25fXgbfV3zzlFIe" +
                "vc+IzC7ony3Dt6X+0Wn5senlS+Ovp/ZvrsiS0BWpv2Lbf1T8iX" +
                "P+nDJ/IWd/pdTvOx+l+FTW/IJOkr+e8b5PkoKP/O8J9lncPzfb" +
                "+Hjvz9Dfx5G1T+J/LSgvXD8h/9t8+h3MXzY7Xzz5NfK+77DW/8" +
                "BA0f4mvH8gtV+l9rGK4/NR/5Mc+QGk8f219HyJ5K8U+y+WPr9k" +
                "9ps0viZDfI5UPnnxO3r+e1l+/jL+b4uFwn7/UOp/cqx/j888+m" +
                "qPz3yP8Rmbsi0w3bLZNisXy/+Ev5j/niy/bRL+z8BvctMdL3+d" +
                "1H6Uty+zr9n725WaH//+peNLxPu7oLaTqL/B+lPaL5D/1Ajpof" +
                "r/DxWIMCU=");
            
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
            final int compressedBytes = 1505;
            final int uncompressedBytes = 19361;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW71uHDcQnqVXB0ZAEEawEpWrQMUVblI5cLVKYiBJY0OwAD" +
                "9EylSBC0pw4TKFC5cHFwHc+RFUuDBcpQngx8n+3OmOe0t+Q87y" +
                "TggLS77RDIfD4fx85BF1oyRnGBoMAf3pkq76/9oBXRMYtcu/JX" +
                "/1u0vXa/mWSkXV8hNt6aT9OW8+6XkhP1M/z/rZ8y8VsMNp6y3p" +
                "7vrrEZ3W9Jsvz34+uJrr+0QP3p9fH9/M5hdk9WPTMtqzDR41uj" +
                "q1tdT1hC3t01dnl9++mz9/fXD1+/uf3l5+ePjion75679f24uP" +
                "jxr5Uvr4+tcD8us6ZDzG/tR4/0tiDBWxfzH6rbyvl1+Mnc8A3b" +
                "Xf3539flza78/O/mH/HON37b/S/3BDf+PVf+T8cIdK2x+z+jF+" +
                "/pD9I/eHwP6MxLeWUFY0c+KnpoUvFgf2Z+x8kNz/9ml/bJ8gHc" +
                "efsf1Zj1PzRjX0sjIzKn4gqqwtGtUqTVVF5wzzwPiC/qSWMI/F" +
                "xyj+Nr+UV/OT121+Ka6PP8zmR05+0Z319IarGzWeTnz+F+aH8Q" +
                "HkVyH/cv19fi26/OqsH9F7QdVaZjk4xfh8olF2ulerNZQmKnx6" +
                "bWInmb9cpaQNT1hKt86nhiKH5a1fpxw+w7ePWy5F128Tzs/jt1" +
                "v10fV4/F0E6qMI+YhfQ+cqhsLMwAGAfwD7ofotLPbwkI6PdLOI" +
                "g6tGRFXT93+Y+uX5X4W9ePOIo5b0/AP64W9e/V5x9GPlT5b9ku" +
                "vj61B9EFP/6BHDfXpydvnNu/kv91v+e28vbx6+OFryf173F/74" +
                "DfmBS52aV3190fzT1RfU1xfU1RcG5g+sf0p5bTb0+9zp9x319c" +
                "9pV/+Ypv5Z9PXPFPVzcP6P7vzUz0/L+Rn6BccE8kXrI/pHiE8M" +
                "+O2An1N/bozCl+EduvHG1yI2l6H8I/RfFr1kxX8VocAu66+gfH" +
                "Z88iYCaf3CrR+858PT31VM+9y4/LfyK5b8/PmPUH0ki68aGb+I" +
                "OrBFin8H+mulFRnTim3o7RacPG0UN7bdN6Wd/iBcX3vpSP8w/1" +
                "FTOW10iI2yz9q/tC2fSu4vYugwfmbmH2SAifH92/xqZmo0v3rw" +
                "+9DtBOf+gI3vA/+V48u++GBZ8Sc7vgvxZ+hfQnwR4qcifJHRPy" +
                "TG30WYrjb8o+ScPnQ/JowPJuZ+ir+/0D/3bl+EL0vvb1j4qh8/" +
                "FOKn+H5Qhv/K8WF0P4ro8SVVHP63I/wuCv800+GfwvqFUR/F1q" +
                "dx8jPXf7vr3zz9l/x+YJr21V//BEds/1RE4/+y8xnTH+sk+8n0" +
                "Q/gTwq9wfRymS+dPx88W5N6vUxp+hvAxMD+jPnTwqy9u3z/E4l" +
                "cIP4vVPxZ/Tb1fkt3voPOHzyfAh4T4D8s/UfIvJfG3DvavCD9h" +
                "4yvp9Y9i4TOJ+B/K/+L3L/h8SfmD+DbEP3j1Uxk8vyXT62p3w9" +
                "S08SXt/ZAc/8zcP9ZZ3wcy+mfkn+H+NrP9MD51t+2b+33tBP21" +
                "rD+Qvy+S0aX9ERdfN6nrq0Xvs3eXPxPrt+z94+b8FbM/UhH3I6" +
                "g/rUnyfiL6/tRy/I//vkGa/9LlD+tb7/ttWf6A8VvYH3HkC/pj" +
                "7B/o/IL+RGy/8Pmpu/xS6WaJD6rCNvlNNyU3Pe4kjOcXA8Jy3P" +
                "s/9v2nb3+n2v9Ueub7kTteH+W/f2Tcz/yv18+sb/LVJ/utH6d+" +
                "vz2IT+z7BZZ9KqBrteWL4vfDgI7k4/fBMv9j1U+s8xtwhVIY3w" +
                "PxAeLXQvmM95+i+o9fv6a9v2N9vzPQ3+Dz58O3mPYB/NL8u/f8" +
                "IMTf7rp+EJ/MjU9A/ICNPyT5f+78AOmZv3+SFp+i9Gf5n3L6Sz" +
                "MZv/j+Nul9Ucz9qDR/Su/fGPVb4P5M/L5s3/cTueeX46+8+JiK" +
                "L+79fUtYf/j9LOCfiM5/vzHl+2MzHT8836B+u+v4yS7mz+rfKf" +
                "4/vX7e/Arpsvwgzh/Z63+Z/LT4PB0d4guwfs2LX0n3R7o+uP7/" +
                "ALKAm18=");
            
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
            final int compressedBytes = 4877;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqNWw2UXVV1fqBtEIEgDUojiq0WUISSrpY/gZx5b14yDlTbij" +
                "+0KSEhUdIBktgkkB8m995z33vz3sxgWJA/gdBkMliMLLHWrtVa" +
                "fkxNsS4B0SWQHyiQ3xoUsbhWV9dytfvsffbd+5x7p6s5a/bb3/" +
                "759nfv3HfnzH2T5vHm8e6iJtj0LHvWyG7nuWVb6VjzeOf5/FLI" +
                "PEGx9Ooie8TZ7jsJZR+o1dLvwesK+670qvQNF6vVsucoW6vZ96" +
                "S7WrnzKJJ/y9mkb3R6zf9jXucJokit1nc82cJZ/cr5ZCbF0q9y" +
                "DqbP0TVs7Yd5mj3kvPbf2d9FdFjP1/x2hj3TvZoBM2D/CK0xA9" +
                "3vOg/RYLbaDOT77DUcMQPpnCJ7xNnRXoH/2H7GDLQ69vftLLsA" +
                "I3+YH/C5S+1l3Tz7sfDk++l1dNQ27bX2k/bPCp7r4OvTUgnoYn" +
                "tl37G0i/5VPjZb5Ru2D18/bgY6wGM/gehPsx+pmj9Ae4kZaH/N" +
                "XmEvR3TIfsxe3Vljz0d0WFXPsXMV+hP7KfSGzJDdgfYhM9T7ov" +
                "MQbctGzVDrfXaSI2aoViuyRyL8gP2aGWq+YO+3f0vRWq3zTe6y" +
                "E7Va/8nCA+eJM38DXw8Lj/Ps16XSRYDp4bSn50k95VHBTop7tE" +
                "vPsw9yV2dZUY/XU7ffXoTocJnR937VPoLeDrMD4mixYgctuHJ/" +
                "YHaMfEoiXFeFyavf5F6Zrfsc5+ybtVqrJX3t90pfPDWe4tDIRe" +
                "monleukE5G9ZuqK3gaT7bXx8xaj9lhf2bfQC8zGcTRYkVGC87T" +
                "MZO1D0qE62TZoTCTb3avzNb9EefceWqfL33tB6QvnhpPcWjksv" +
                "QuPb9cIZ2M8k3VFTytOE9/GTNrPXCM7jw5b8yM5e9AOzM/GyrG" +
                "aNlW9h9mbOQr+bs4YsYkG+P8t5ytL8yncbRW623zuXfmp8B56p" +
                "O+ztuEwf3L313w/HY8xaGRl9MJzJ5epSN/L9pTKU65/Mz6QlVx" +
                "EtqTeR5x5NPzM+A8Pa8ZiUHz5zPy96C31CyF+5OzD5mlULGUFt" +
                "yfjsPrJNyflvKSLHmq2t2fltZv9Pcnyk1yrZ1Id/VdKjwqw/cn" +
                "xQv3p6V6JlyZF1MH10k95VHBTop7tKt+o9TA/Ym7Jrm+uJInp2" +
                "L0ve7+5LwlZoldg3YYvqzzELXsTLNk5FG7xBTLbmQPmMC2zxDs" +
                "InYT7A+esaso0vtP3zVm74T70wrh6ZzCXTa1mb2b2JhXEHavhn" +
                "vNc+lhmQqxruRpMsTalLcdROtaf604Ngg3zFyLsZXg5fD1kmbW" +
                "jBFqmRZ4aDHWogXqZppWb6ZEuK4Kk9cedq/M1nuLc7hTGZS+zk" +
                "ekL54aT3Goe3N6TM8rV0gnI6e+XJFt52k8OT8xZtZ6BPWf238u" +
                "/NRG6/45j1D9ff3n9hoS4boqTF57u3sVNs7hXvEy6euOSl88NZ" +
                "7iUPfl7AQ9r1whnYyc+nIFnSc9OT8nZtZ6BJn5Zj6cMW/JI2TP" +
                "MfN7TYmE2Rg7v/2mvzqKCHtwPV0jPN0v6YxUxhq4pv7D7EQ9L9" +
                "QUqmdk3x/rdtZfT2p2/uGpGCO0xWwBDy3GttACdWebLb1FEuG6" +
                "KkzeyFXuVdg4h9fTz6Sv+4j0xVPjKQ7Vn83O0vPKFdLJyKkvV/" +
                "jzpCbnF8XMWo9Ct5pbwfOWPEL1PnNr71GJhNkYO39kyB91EWEP" +
                "ztPPhaf7jzojlbEGruntzn5Hzws1heoZOfVltaE+PE8fm4oxRM" +
                "m/JLvtX8DvknfTb4Dp+5MfpuPJ83DlXpC82PsBZPbBF+w304/C" +
                "3vM18H+Jv3u+hfa/k/9J3wbxV9IL02ndcai6CL5OSc/IXq0F//" +
                "pex/pXkleT15r/nhxKjgHaLPn0xPTt+Wx4PRnRaemZ6buTJ5Pv" +
                "gH9B76nsQ1SVPJf82HsvwNdL2cHkXvd7cPJT0Pvn4P1XsX/8SP" +
                "oOZJqB1U8l3wf7rMxrX+vP09USS95kr/UbiDelJyDHqel0s9Vs" +
                "hTOGFs/dVlow6UKztfeMRLhOFuzHgwz1a1942/OkrzdN+uKp8R" +
                "SHRk/IrtbzyxXSych+tLqCpxXX06djZq1HoaZpwv7J2WH4ss5D" +
                "1GruNc3eXNg/NYvYRvagFyzsnwrsIsX+qYhgF+6fHBuv3kncVe" +
                "yfFK8g7Ib90+jZ2VyZCrGu5HkO7J8wD/snh9bpebB/Krhx/+Ri" +
                "fv+Uf04zh8oDtN3AvY0sxrbTgnf4dLO9d1AiXCcLrqcgQ/3aF9" +
                "72XdLX2SN98dR4ikP1Z7IBPb9cIZ2MnPqqCp5WXE9ZzKz1CMJn" +
                "UWvgvbjHwi7RWodbt7jndPVT8RnbkubxdLh5PIdXuzF5Pfk5P/" +
                "fKh9pn8NOt7BV8CrYpXe2uJ3x2B/en/Gb8/uH11H44X8TPv3rT" +
                "6TkddOD1lK5Lfo3PAX+TnpPlt6jnZKsdR3ZD/lfJ0+la/9yvm/" +
                "wE+g/AlIPQdYd7Tmfb+UrU0IH+L9h1Tn2+ND0d7GK7Adlvpzuw" +
                "XZsvz1fYlelKvJ6+lBzJ8W6d/AJnf96fv9vyL6Y3y1M7Uzd1OG" +
                "PekkfIfs5ZiYTZGFO3/37Vdae/nnYLT2+ezugZoQapyRbpeaGm" +
                "UD0j+9lYtyjUs/NHp2KM0CqzCjy0GFtFC87iMopxhOtkwfsuyF" +
                "C19oW38aD09b4gffHUeApFssV6frlCOhk59VUVPK04T4/FzPEx" +
                "e7TRbAQPLcY20oLvyHyKcYTrqrAwhL7wtt+Svt43pS+eGk+hSL" +
                "ZSzytXSCcje0N1BU8rztOemDk+Zo86Bt7RZDHWoQWTbqQYR7hO" +
                "FlxPQYaqtS+8nUHp6/299MVT4ykUyVbp+eUK6WSUD1dX8LTiPK" +
                "2PmeNj9miGgZ0YWYzNoAXaxijGEa4TD85TgV2kOOoZutP/pvus" +
                "8PT26Uw4VU+Rmmxczw818TzdHc7TFVof7ijfHjPHx0wI7+X7yE" +
                "JkH/+cMfucbz/v7JM1joWf9DB2fS7iLHPIpzuOiaIcGb2Yft65" +
                "KGWn/lyKerP79edRokM+l2J+PhZdw4qoWh+tqJ3qc6ni591ys7" +
                "xxAVvyCDkf7r9FRGeBf3lY7SLOcgcqKphCntEHmcXlqFd4BTFz" +
                "44LsAZmq58ocUivHUtZN3KyEeEVtmTFC88w88Lwlj1C2zVmJhN" +
                "kYU7e/rufpTn9/ukZ4Rid0Rs8INUhN9qCeF2oK1TNqXR7rFoV6" +
                "duuKqRgj9GL1gizYbKIc+//i7CGOxnXZDq72ul+celFVNvl/zU" +
                "1mSoxewwpB2XZdgd+Br8T1oaICjcOaJItP1cf9mkR/UkXGAz/E" +
                "wuB9xTUZMY9HU6hr3HeNCypqJwvOCh0+Oum9SaU+mEfZQgl6jJ" +
                "iZ5msFBbrN3Aaet+QRatTZSiz0JNeoUzd8fx5lNq72+0zFM/pP" +
                "YSbkFcQ1jXr2DT0/1BSqZ6RryGeFok906ZmiPEA7zU7w0GJsJy" +
                "1gmOOsXcIRrpPlKrRH/cLGXahnjvSNPi6MLqOnxlMcaszJvq3n" +
                "ay6Zp7vDeaxOH2NxnuZoZq7R7B6tMfBbMFmMraEFZ+gWsybbIx" +
                "GukwX7pyBD/doX3v6npQ/uT2t0Tk+Np1Ak+1c9v1whnYz0PKnw" +
                "z33V5OypmFnrEeT2BvVDeSe5G+xo/RDvIuqHnF+rkeVY8rrsMC" +
                "hH9eTVD1EV7zvon2NyVvYl9Jr0JZtdTjJ5t7x/cWz1Q9n35e9/" +
                "iItV0fNMmk3TeWask7m5Qr7808xfaNX0PFPtnzbD2k8Wzhx5FA" +
                "Pf3qYimwM/xMLgffw+FEwB8+YiQ1P2C3aeyjLz/uyZoC/Q4aP7" +
                "vbdf1IfqiLtQgpoYMXNRo9g9apgGeN6S51bjtMZpppFNSERnpY" +
                "Mxdft3EUZgX1Dk4D5wmvDA+05luKOsQWqyF/S8UFOonpGeJxX+" +
                "fadmZy9OxRih+2EdIAsR8igGfhC5P/BDLAzeR/6CKeQpMjTlgG" +
                "DnqSwzH8gWBn2BDh894L0Doj5UR9yFEtTEiJmLGsXuUWKS+q/I" +
                "wrsVPULO1xGuq8LCwD7yF0whD2coR12SEcTM9V9lv9R9oQ6Kkl" +
                "pRUtZN3KyENDFiZq7R7B4tNAvB85Y8QvaDzkokzMaYuv27aKHu" +
                "9L8f3F7mUe87ldETucZ+KOwL87qTUWtVPE8U6tmt66dijNCd5s" +
                "7GlWThXY0eIefriEPiS594yHmnXSU+eSGz5nFTXFZnwimur3Gl" +
                "/T3dp7k4SmrlWHQNqyNVhNznLaJLtGrlwXHMNrPB85Y8QvZcZy" +
                "USZmNM3f77NVt3+v1Tq8yjrieV0RO5xp4X9oV53cmoPE8U6tnN" +
                "S6dijNAd5o76G2Th3YoeIefbmyTCdVVYGNhH/oJJM7ucnkJdkh" +
                "HEzPU37CLdF+qgqIvJdFIfqnNZ9/NOHy0jZqb5WkGBNplN9dfI" +
                "Qhd6hJyvI1xXhYWBfeQvmEIezlCOuiQjiJnrr8F5Un2hDoqSWl" +
                "FS1k3crIQ0MWJmrtHsHi2DdbSw5BEC396hIioLvcvCaowcRdaj" +
                "VOFrkImjQfcyjB71vUVGUMF81D6m+0Iu0UIaRH2om7gLJcgrai" +
                "sYQ7TBbLBr0OLnnM5D1GoYswHuc4YjDonvluTIQ05fRT557nPO" +
                "ah73OWfDCJb+omK1Y7eP6z7NxVHbJs92EK3TNayOVNm1rM99zi" +
                "k5XaPZPVpn1tVfJQtXIXqEnK8jXFeFhYF95C+YQh7OUI66JCOI" +
                "meuv2id1X6iDoqRWlJR1EzcrIU2MmJlrNLtHa81a8NBibC0tON" +
                "MDFOMI18lyFdqjamHjLvxpPFDFQxmN4ykONQbsd3Sf7cYV0slI" +
                "z2N1+hiL5yoDmplrNLtHK8yKxoVkoQs9Qs7XEa6rwsLAPvJjzo" +
                "6FzC6np1CXZAQxc+PC/HzdF+qgKKkVJWXdxM1KSBMjZuYaze7R" +
                "Alh7C0seIfDtuIqoLPQuCKsxshdZ91KFr0EmjgbdCzC61/cWGU" +
                "EF8174eaf6Qi7RQhpEfaibuAslyCtqKxgD1JqTbmnMyq5I9jRm" +
                "pVsbs5rHW5/Esz6rMatWG82dhd+qL29d0zzemOX+XiX1fwXXup" +
                "ZykP2A81yeOtO5vuLj/tqeRV/lf26KyyS/dmi0BVznQN8ndIXj" +
                "zf8Bap6WWPITwAeK50Qzid89LWIN9Jqenj0fzhaV/JUc4edPqH" +
                "lA/Y3fzWonPBfWkcKSRwh8e5eKqGzQhx5GcCJFC25k4mjQPRej" +
                "R3xvkRFUMB/JH9d9IZdoIQ2iPtRN3IUS5BW1FYwhus/cVz9IFu" +
                "7+6BFyvo5wXRUWBvaRv2AKeThDOeqSjCBmrh/Mn9B9oQ6KklpR" +
                "UtZN3KyENDFiZq7R7B7lBnYRZDGW04IrcpBiHOE6Wa5Ce1QtbN" +
                "yF1/lgFQ/uYu7WOJ7iUGMw/zfdV66QTkZ6nt0gFayreA8Pxszx" +
                "MXs0YSYal5CFLvQIOV9HzITdKL70ief+PpN95J8gL2R2ET3FZX" +
                "VGEDM3Lmmdrvs0F0dJrRyLrmFFpEofLSPRqpUHx7HYLAbPW/II" +
                "NfrZSiz0JNfop27//VpMPteiugoezoS8grim0d98SfeFXKF6Rr" +
                "qGfFEos7lOzxTlAbrd3F4/TBberegRcr7dIhGuq8LCwD7yF0ya" +
                "2eX0FOqSjCBmrh+2W3VfqIOiLibTSX2ozmXxeYE6WkbMTPO1gg" +
                "KtN+vBQ4ux9bTgvnEfxTjCdVVYGEJfeDvXVffFU+MpFGkt0n3l" +
                "CulkZL9cXcHTin3CZTFzfMweDRv3v8HQYmyYlm31f5liHOG6Ki" +
                "wMoS+8fTuq+8Kp0h9GWlb3lSukk5FTX1XB04rnvt+NmeNj9mjQ" +
                "DILnLXmE+rc6K5EwG2Pq9joGdac/T9vLPGoXpzJ6Itekx8K+MK" +
                "87GTn1ZbWhPvy70flTMUZokXH/J8pb8txqTGtMc1giOisdjKnb" +
                "61ikO/39clqZR50nldETuaZ9T9gX5nUno/I8Uahnt781FWOE7j" +
                "X3gocWY/fSgu/IFopxhOuqsDCEvvD2ba7ui6fGUyjS3qX7yhXS" +
                "ycipr6rgaep6ipjjY/aobdrgocVYm1bjpMZJpm2/LRGuq8LCEP" +
                "rC69iq+sKp0h9G7D/rvnKFdDIqz3PWfx6sJnduiJm1HoXuMfeA" +
                "hxZj99CCnwQvU4wjXFeFhUF81y+8neur++Kp8RSKtB/RfeUK6Y" +
                "ynxxU8rThPC2Lm+JgJ9Z/Xf56ZJOv+usx5hOwTzkqE66owXOee" +
                "gaP4WZSvCpn7z2utFAbKah5BPIO7hVFX8Gzy+FjKuinLXPz3dJ" +
                "1bNDPN1woYmW1mG3SgxXO3jRZc67spxhGuq8LCEPrC20qq++Kp" +
                "8RSKtB/TfeUK6WTUWl9dwdOK/z95SswcH7NHI2YEPLQYG6EFO9" +
                "VhinGE66qwMIS+8HaGq/viqfEUirQf133lCulkVB+uruBpxftu" +
                "ecwcH7NH/cb9fuUteYTs90x/+4MSCbMxpm78/X+VRNiDI72uzK" +
                "P2BSqjJxbdPw37wrzuZGSfiuc56+/jgNzn5ng9nToVY8T/vzuh" +
                "lkw=");
            
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
            final int compressedBytes = 4479;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNqVW2mQFdUVHtTRGE1ixAFKE6MxlUolpjCSDbe6M80jWsygRR" +
                "azlEhFRBZjKkSjlP7o9/q9ecsYXBIttSCiKaxKpcJOBf0TDYto" +
                "ZMAFNYKKEoU4GgHFHc0599zT59x7+w06XX37LN93zunbt7vvfe" +
                "+NucRc0tFhXEsSaaXd5pLSbrH43lAnNv2JhaWOjgnVOE5Hh2bE" +
                "NQgGa9E836+ZrMX5pEKdu35Pu4iBNtvMBsm1JJGWbTKzs81i8b" +
                "2hTmxXR25hCep+IY6j+kl5dEbGZFt8nu/XTNbifFKhzl1f2i5i" +
                "oJ1nzgPJtSSRVtqGrVh8b6gT29Vxnma6fnoxjqP6SXl0RsZgLZ" +
                "rn+zWTtTifVKhz11e1ixho88w8kGxrbfNog2v4GNnYwjjZsjm+" +
                "h9Balri1lUVx8n5SHj8LWbLHNS9GCJO12opiBGfL+2lkGDk8Z6" +
                "e1TAsk21pbiza4htvJxhbGFekSwZclbs8hxbwwa5gFtZ5DKv2a" +
                "FyOEyRpWX4TgbHk/vRtGDs/ZaU3TBMm21takDa7hVrKxhXFFuk" +
                "TwZYlb/2sxL8waZiFL6U3NixHCZC17ohjB2ThzoyuMHJ6z08qm" +
                "DJJtra1MG9QGbfV2sTCuSJcIIte/wD7bT/cJr3688MKsYRay1E" +
                "/Q+WKEMFkrFSIqd3G2vJ8mhpF1PUqbZqaB5FqSSMuexlYsvjfU" +
                "ie3OeppmOtumOI56jiuPzsiY+rd8nu/XTNayp8J8UqHO3RhsFz" +
                "HQppqpILmWJNySc5JzUBeL9gqDdWK7OqZqJtkwWhhH9ZPy6IyM" +
                "qY/3eb5fM1mL80mFOnfjmXYRw/j+X2nIzVhqE+7V9uos9KSvkl" +
                "ZpVtZU57CvcpJmgr7ScWYKf8K91ekdB/krH14aqnwJeJeHnvoZ" +
                "1dnteenxNttVea2XYT44/hqrql4a4qu/qV6Z99N20H9lo+yxvh" +
                "kOc0V1bqWlruSx5lhuWcct6Uw6ycYWxmmG6Cj7MdnrxlNnHMev" +
                "gj06I2PqZ/o836+ZrMX5pEKdu7EjjByeM2mlIRwH1NKooA2uyH" +
                "1kY78chcGaP57Ypr0YzWen3XoUi0c0YVcW+Pl8f3q8MDl7nE8q" +
                "lLgdHc3Dw8i6HtHMDeYG6DHb2r67gTbItIZsbGFckS4RfFniYr" +
                "QiXpg1zEKW+tmaFyOEyVqcj7ycLe+nc8PI4Tk7Df5Aci1JpGXb" +
                "jbHzAiM2X9I6sV0duYUlkB+VODAvUB5BhjUIBuYFKp9fk189a9" +
                "m2sG5s3bxA5W72tosYaJcbeGpySxJpPaca+zxli+8NdWK7s75c" +
                "M8nWfVscR/WT8uiMjCm/4vN8v2ayhtXH1fr14V//xe0iBtokMw" +
                "kk15JEWvYctmLxvdjC+i7Xie3qmKSZbn33ZBxH9ZPy6IyMqc/1" +
                "eb5fM1mrbQ3zSYU6d3NBu4iBNtfMBcm1JJGW7cJWLL7X17uvI7" +
                "arY65muvF0SxxH9ZPy6IyE6b4OxpPH8/2ayVr3dWE+qVDnbm5p" +
                "FzHQukwXSLa1ti7aoJ92k40tjBMJxpPTu69FS37WXZrp+un2OI" +
                "7qJ+XRGQnTfS30k8fz/ZrJWve1YT6pUOduPhlGDs/ZaTebm0Gy" +
                "rbXdTFsyOhltbu4/RSxoE1l4IiWj8UgoiYVWmM+MLub5WVHSGd" +
                "nSv0vzYoQwWYvzYeue43lV0E/vhZF1PUq7zMDslVuScEvGJmNR" +
                "FwvaWBIG6ygnY22PjBULbtn1YtVswrh+Uh6dkTH1FT7P92sma3" +
                "E+8jKeaoV++rBdxEBbZBaBZFtrW0RbVkvGk40tYLtFZOGJlN2K" +
                "R+RpppuPjy/m+VlR0hnZ0jhL82KEMFmL85GXsyXjSW59OYzsnz" +
                "NrlW0yk7Vv4J58tZWEs+zKj/RMVv5ofVe5f7i1W/cdw3nV2vBL" +
                "cYbas5Ufa4yee8v6LuBs9+O2+2uNbY/RHnOHuYNb1nFLxiXjyM" +
                "YWtImMGzzHnUSoZBweCSVMN54UV2LqKtijM7KlcaHmxQhhshbn" +
                "Iy9nw1rt9Xs/jOyfc65dZWCdTa21XUVb0pV0kY0taBNZeCIl+L" +
                "5wKGEm9p2huZrnZ0VJZ2RL4xeaFyOEyVqcj7ycLXFvvNZpYWT/" +
                "nFkbZh08C+60P3+cdbCsl+0nMr8jS+Uebx08S/CVuz/xOvipT7" +
                "wOnlW0Dqb3HWqZ67NW7WOugxeahdBjtrV9t5A2yDTbLLTru4Vi" +
                "E9nXJYLIjd+zz/bTbOE1BoQXZg2zkKVxvc4XI4TJms4nCDcvUJ" +
                "lbN4aRdT1Km2lmguRaknBLRiWjzMzGQrGgjSVhsI5yMsreYaPE" +
                "Qiyx0tZYICzXTyquzsiYbLrOpxHMZiZrOp8gXD/lVUE/rWkX0d" +
                "fStek/9V1TPjHdAiP5Sahtf/q0HdXPwD2yszYp3YWfZ4K8D1An" +
                "pW/WekF/L/2ofCg8DeeB7QiOUj66bD/fqqlvMrtvtbF2pC+kL8" +
                "LxP+luwJ0sd135kPJhlvtp4E0uf7bcVR6V3p8+YLnzyq+4z6we" +
                "TR8nRgp3Yvos4OH9mB6fvuI+2XwnzzevfCTiysdZ+4Ppw4DbHL" +
                "/NBjr5zNM96V7Ifa5GlEfYmj5T/py5ydwEPWZb23c30ZaMScaY" +
                "m7K3xYI2kYUnUjIGj4SSWGiF6zummOdnRUlnZEv2jubFCGGyFu" +
                "fD1o2nvCrop6PDyLoepWUmA8m21pbRBrW9Z7LGErEwrkivvcgR" +
                "dDRG2Rn1ZuE1/iYRwqxhFrLAfafyxQhhslbbXoRw/aQyD/wgjK" +
                "zrUdqd5k6QbGttd9IGtb1PNrYwrkiXCL4scSfcWswLs4ZZyNL4" +
                "u+bFCGGyFucjL2dTz8YgcnjOpIUzbHk7Zh/oz8dpx+9b9Cxg+M" +
                "/HvfnxBxofzgv0Z+rxexn6aU0YWa8haF7g11B7P4x3sNl5uif0" +
                "V15R1bxavAHqsNKr1dt9W4gp0mH+5Hltxs7+QwXXGGC/O7tX22" +
                "+Ealw/XN7KoTobtTB/KsBX7sqrvEpn1xH9ilgz8818GFm2tWNs" +
                "Pm0wng6Y+Y0HxcK4Il0i+LLE7e8s5oVZwyxkgeeT4sUIYbIG86" +
                "cChHs+qczikyj+OZNG84J0Xbrejb7BdEv6GM4Ler7G8wLYd6Yv" +
                "0bwg/V+6L30DpDfTt2ReYN+eR+QraDcvSDem/0ofcdU8kj4fzg" +
                "vUmD+A84Ly4TQvKB/lzwugnz5KH0o3AQ7mBekTcNyaPpVuT59N" +
                "n6P7DucF6Ws0Lyh3lj+F1ZePLH++fEz5uHQDzQugdfOC9N/pNp" +
                "X75fS/0A7RvCDdb21vp++mH5Q71LxghpkBZ+FakkgrbTX2u1G2" +
                "+N5QJ7brkxma6WyDcRx1TZVHZ2QM1qJ5vl8zWSNGWK1fn9biiI" +
                "F2kbkIJNeSRFp1hLmo8ZBYfG+oE9tlzi0sgbxF4sC8QHkEGdYg" +
                "GLjvVD6/Jr961qpR3di6+y7KXRQx0CaaiSC5liTSer5qJtp1y0" +
                "Sx+ZLWie0y5xaWQN4cx1G1Ko/OyBjoJ4/n+zWTNaw+rtb1U5S7" +
                "KGKgDZgBkGxrbQO0wRU5kWxsYVyRLhF8WcXdWswLs4ZZyNLYqX" +
                "kxQpisZduKEZxN9VMQOTxnp3UbmMdwSxJppZqxMxy2+N5QJ7bL" +
                "3K2ZzvZ4HEfVqjw6I2OaAc/3ayZrWH1crV+f1uKIYXxYU1xan1" +
                "5aUhqq/7J+STlfB5Z2wOi9zUpLeMYG6+AlbT/3fYCRRX/mMTWn" +
                "fD74fHWJ8PB3PfEf1qFRKLFWWlL0uW9pR2W/Xw/KdD6i6yP9rs" +
                "evq2A2ur77CvsOvDF/u/8BruNf7NPhELsCv4LmBe36qfph+Y/l" +
                "eW376Yn6Pt+SHZVnui49oPupvj9kZ9Pre9NN2Yi82q2wb3fsa6" +
                "hy/6//cOs7BqK95XBXBzHJekv6crt+Ks8M1wvVemkVtAN6hl/9" +
                "RmmoucpaVsm6heXidQtGKV631LcIvrma1i05cxVGLQ1Vm+3WLd" +
                "l0QTlGXlVpVdG6pfp1qkfFWxWs0Fb5x3jdotnJvsRe59IK93m/" +
                "1XHv6Ur2NR9GubQi2VebhDbopxXkB0svHZN9zOQobNOfP9UfZQ" +
                "/zhVlagVFJw7Y2mf2MsP2kUChxJWIltrtTu6gejsp41Qsr/GO6" +
                "x+bWnz8ptjnfnN+AkWfOh7fKS3hECfeekeb85iMk847rYJKxpa" +
                "NIdBTZG0+PCb65mo4apfmNXeznvNl0H+VX5VtdP42UePqshlsH" +
                "h37vLC8wF1j5AuezOu7Vb4qHd9tPFzAqm4NHRmk0R1P99Dw+nx" +
                "hPR43S/Pp+9nPe5t76Xo3yq/Kt7r47lePh80kiDdtPgV+fm5ls" +
                "JuP7zkyG+uB9hzp4YO8ZhS3JvNt+sjL7tCRokr3vOV/XeDpqlO" +
                "aLn/M29/kovyrf6sbTqDCe9rbpp8Cvzy3Zk+zB+660nO471LEF" +
                "yzOJfQMke0rL2QbPJyezjyXHWc7W6M2+VuPpyKjScozKEfG+Yz" +
                "8jmm/4KJS4ErESm/NRPVIny/nTZ7l/TKOqNTt5LYF1NrXW9hpt" +
                "MHLHko0tjCvSJYIvS9z+E4t5YdYwC1laIzQvRgiTtf4vFiM4m/" +
                "RGGDk8Z9JMn+mzY6vPjTWr4179tumzv+vpk93ed32MoqNIHIdl" +
                "bzytt8+nPo4vTJ2X+PB86hME7v1fcc+nPqlT79rK+TiefT65SP" +
                "0nDXvfBVXrc0veSN6w8/HV9HxCHXoQ9p4rsUW5tJptcN85mX0s" +
                "kR+jiNd7Pu3VeDoyqrQao3JEwQmidYqPQgn3rJOOugbXT1dSPR" +
                "KP5fyuWu0f4b4LqtZs02t67byg180LelHCvfpdbEnm3Y6nXkbR" +
                "USRBk6z/GmM0no4apfkwL+gVBO6t032UX5Vvde+770g8fVbDjq" +
                "fAr88teT2BNxG1tu9epw0ynUE2tjCuSJcIvixxG2OLeWHWMAtZ" +
                "WuM0L0YIk7Xq+GIEZ1PPpyByeM6kmZFmJPSYbW3fjaQNRu5Esr" +
                "GFcZohOsr5dRipma6fTovjqCunPDojYyoLfJ7v10zWsPq4Wr8+" +
                "ren8RfGTjclG6DHb2r7bSBtckbOTjfh7FbYwrkiXCCJX7mGf7a" +
                "czhVe5W3hh1jALWSqLdb4YIUzWqmcVIehzOp1ZfBLFP2fS8pXl" +
                "yvD7u56TS0OV9dayUv3iZaV8L5bN8X/7wt6idXDjt7K+DX/XA6" +
                "yV7b+/s78R2hCgVnJVKBetg7F6XS3hqZ84q3+Mv9/T7GRtAjNA" +
                "am3fraUNrsgPk7XNhlgYJ1s2x/cQX8sSt3G18Fo/FV6YNcxClm" +
                "y6zh8jhMladUoRwo0nlVl8EsU/Z9Lsb6AWQ58tw9Ys5mtoFvec" +
                "jq21LGMb4uS6E1r9lmpxaZnEaFzj7v/FFLno+2D0IQujFo8n4r" +
                "nMDmUZQ3kly3A8cXzGY/VcratzGXkZQWeMmLbjaZkaT+uSddBj" +
                "trV9t442GLmnJevwd2JsYVyRLhFEbk1jnx1P84TXmiq8MGuYhS" +
                "yti3W+GCFM1rD6GOHGk8osPoninzNp+XVcGj2fpuS9uVQ9n5YW" +
                "/77A8Za2ez71TGn/+wJgLR3++YRsD7WUq0K58Pk0Repx8UCmKj" +
                "irfywYT4qdbEjgGUmt7bsNtMEdfmGywY6nDWITGTd4Pnke4rPc" +
                "msM+O54WCa81S3hh1jALWVqzdf4YIUzWqj8pQrjxpDKLT6L45+" +
                "y0wWRQz95Jx72nNxm0/TQI83dns3P5QcIxWiT2ss17360UPNx3" +
                "gxpjVySDcVyppXWxj0KJKxErs+146pV6KB7K3vsuWLdodr5uUe" +
                "fG3yNEI3dy0a/Oi75HKB61wX03/Gc/B4b/DX7154DZpPDqewT5" +
                "nb2ugPLh9wjef1D/LK6Sv0co+L/ume2rSv6Ur1z/AXFvL/LAiv" +
                "l79lPrOcXMor/GA+p/AGZ1fII/jNuafTBEWAFWX/DN0F3DVxl8" +
                "Bvv92DbhBJdrQX4ddwbVLAgtw/t9rbE+9nDOg/TTgrgWv26uWW" +
                "pvxyjt1JiD/Xn1/R9GAuXy");
            
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
            final int compressedBytes = 3233;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9Ww2MVcUVfoplBVuwsFh2W5uYJiZt2qY2apE0DY/3btJK2g" +
                "ABu42tjT/YWiA1bakYsZl5+95jdymEYFJtQVEbFRuLMSUCYtIi" +
                "Ilj5a6sFgRWBLipbjYJYsVI6P/fcMzP3zLy5C+3d3Llz5jt/M3" +
                "f+zty3lcWVxaVSRaXykjlNlTfqMiipLK7Px3wlk8Nc/U75BG31" +
                "BYDJq3yvoed2lLOt2lqBErruMBGK38a093mO+m12bcG2KW37gx" +
                "z6qnamUvdCSZ2XrAsR+grj5fvyZWCztd6+ZT5U6gDLpgd1NhQv" +
                "/f5VllWWQQq0psrP6zIoAT6KRg12HvWWl9NyttW8Fa2x7y4Tof" +
                "htTHuf57D9c1sAcLvOKbWkskTkVKrKlui/Uil5Q5dBCfBRNGqw" +
                "86hXaqPkbKt5K1qjlkaNeX4bK5Wal1Ictn9o25R262zzVztSqX" +
                "uynvtLp8fe02J8BPHJ3US/7ogad0JvfbF3bHSgZdMDyl5MLVr5" +
                "Vx2fvvfBzNJS4d+vkQMR+koGpYS3nZb6bba+6r/x1kXoYJ15D2" +
                "lfag+UClwh//inM0t3OcgEf1vJct5mSricrrb4i18l5qe7/Xax" +
                "nfjE1vb4V4bmRWVpZenkFToV2lVOUzKvUyzDvE2jBsijLqnD1G" +
                "zLadTUalOmF4DYfph1QE/y9iRf7QG7tuAXaJZPaBUo0RT9hlRf" +
                "f1ho2VZk3AXnp21D7U9Sb1iaHHfbaC9a1cLvhS2paXnXVyWDvT" +
                "06Dzd6AynKJ4P1R2mdal9wP/L37ZYpK1N+mHpRT/dNrmbTI9Zp" +
                "+6be8iOuPpmG56e81yhdHVsdK2Yslaq5a6z+E5Yer46V8ziUAB" +
                "9FowbM9/UDptppJcr17UM516prRZf07Tft5TlQEqjufRSHbifT" +
                "MmKoxa6zy5/OV09lb2SNvd4h4pnpHLzvNZNqTjOQgVgdmcSRIp" +
                "aVvakUVmy9C9pcl7XzZU47rYuVJNfYvUat9xXV0be/qGWwZ2Nn" +
                "sZ3WZnPelU47rY2VJOO7FVH9ae2Q+hMhJb3PY2exnTZk426dD2" +
                "klSV09u4tZt69F3ykqVV8b5/WZrMLpG5lYZN1vhfccLGbd6R0T" +
                "Y/YFlMRQ9wG5d/FY5TFIgdbU5Am6DEqAD/+6Z9uI5jbzqLd3PK" +
                "XH9IK2ojXW15sIxQ+WgZLeUxxY53wLII8p6/KnUuuzN/JVUbse" +
                "CvG0eBDvvd4YRT8tqgP2T/FS0vs8dhbnp9XZCH/aaafVsZJkO/" +
                "3AaKf5RXW0aCdCqr6Bws5eO2EcXN/kIN8Kzyu8La3Tz6l5oTkj" +
                "yvo3PfN4MyYOtvYFt6Qa2z0tP+8M+9PTWTs960NaSZL96ZZi1p" +
                "12WlhUqntfnNfR7dJf6YcUaE3Vn6v0y3EHJcBH0ajBzqPe3ltR" +
                "btEKlHOtulZ0SfdNpr08B0oC1byG4tDjzrRstgBosOusKYgQMZ" +
                "KE+LO+xYyDkc+UsOPgfEyLaPNajG0X3e/GwaZeMwrGONi2Z+M6" +
                "DjalZdxi8thxsB2nu5rzcbjydhN7Rlh6lm1O9yI72C72V/Z30U" +
                "5b2R5VIiIAdpgN8Oni+U/2FjvGjovcu+w9kX7ATvNh6UzQls0y" +
                "H+VjlORW9gJLzzeac9kBPo29yg6yQwL5B3vd2gGd4ufy8/hwIT" +
                "tS3BfwUXwcv4j9iW1M54DvsefZdsH3F/Y39qJ4vsR2s/2sn72i" +
                "0GvZUVH2prjfF9If4ecLe3P4CP5xfiFvZ8+J8i3szyLdmVp7mR" +
                "mREzvC3hDpIHubvSOeJ1TZv9hJ9iEv8XNUfT7GR086OknYgFTn" +
                "NFXfJlMssVGX1tL6whLIiXl1bl4P+moipkXgWbTKlrNxUxKovD" +
                "30MG+b0mhT9ZP2WY6x3u1wR5PZY+uncivxf7Lcv4l5/Gcm1ZBc" +
                "H5Kngx9QO+j6TnsFtc/EqPWuvr3+vjoTO91ihV3u37G7yKR39a" +
                "3kVmaR6zzATC6k/e1ErnfBddjUSllY9GieB72mJMD7lu30oL9G" +
                "LmL1p4cyridC/ano1csM+0+E3hxlYdHvivYn13tvOz1coD+d0L" +
                "c97hrDATO5kC7YTsuC/ekEnc/aaXWeB72mJPgjPsThW+XnMREu" +
                "Vp/yKH0r+pUMURE+lAMX0inX4chvYXMIH/tN3VTeGkminB8wfQ" +
                "FeSkLb8+ly9cYj5ab9FP1pNNC6TPfCcjNos1kckZiJUuOAH7W5" +
                "0Cv5pMadRk29rgfAEe9zebS+lUeZl+WfAGZyIV1w37/GWO3OyX" +
                "k0ms6HeNBrSsL1PvCWRschfCO/UcyFm/njIr8W5yeuTuC5iNT5" +
                "H1VulrhvZm8ZksaJJ9+aPnfwF4j+sKF5Z/Ckzdhl8B8R8n8QPN" +
                "sN/pfEvV+Uq6iRbya+IyxQyIWOnuyUhW/BfaZ37vqhM18d07fC" +
                "3sze+jDATC6kC87jy4Pz+DE6H+JBrymJxrkhXT69fsQf3zXG4p" +
                "fCcHwHfPVqIL6729VTJL7LR2Et47tfUfGd7Z/a9SVx8V1loDIg" +
                "Zg+VqnlkQP+Jdhqny6AE+Cha55Lx8onaAFO2xtNyrlXXii4BnZ" +
                "QfiJnW8/ZAk1lb4DM1u3XWlD8ObnzKiYPn+OJgPlfHwUm7Pw5O" +
                "2plY04caBwuOQBzMOvNxcNIeFwcLv6LiYLcnYt9rXGLvTd3+S5" +
                "yndLr7WUNvZ2hv7NPo2s/PA7gft8ed9MUebZQu7Zc9j9jzQE52" +
                "uk7529lM+HkXw9RbXy/aXBmSCmvNc8kcULR08z7XH5dL0/y43z" +
                "aFQDvyLIpvfIl62+GIyY9O7gpJxUSN9qxu9j7y+11Xq7hR0/xU" +
                "XHyXTE2mYqpzmmpclkwFDiizcyat8iNNnYCmZSPzemwv8j6YPL" +
                "acjZuSGZWzp1HTP/SL0ujqT/ln6tTYZ552MUw97T/GjyZjAnIz" +
                "Q1opLpkDipbW9kzE5UrrE/KZ0jtDp7WsDzeucDFM01Pr2bQWMg" +
                "Z4NdACM5KIr1Yml8wBRUtreybicmm6drHfdh4x93jZeve1wutd" +
                "R2C96/g/r3cdketdh7vS0eud/3eH5UvN/bi8G9PtOnXPDn1vaV" +
                "xjxS0b0X5jRtHfHTZm+vcv9O8OTe+xVo1pxVYh43eHbdW2Ukmn" +
                "6tymTf8JdI8ug5Jqmywx/0xu0IB54E7b6RlKT3ZaZOgxLUKJlg" +
                "Akz4GSrnWbI9kD1oyTKkezXeeMGlW1TqM0Le9Gl0aABj4s7Z4t" +
                "ue1y1OHEwZsA0bpou6gd/pzTN8sa5vM+NL7t6jNRz1niKLpESu" +
                "u4RbR/F5z7sl2iJ4u4pXwxxC1JFzsskB7Nx1QUzdQZu/v9TmuB" +
                "uMVqp0OKn4xbkq6kS8YtSlbtLfJxi+aScUvqxW5x92fl6XcmGb" +
                "ek4+5iPkJi+vcFMm4RXDutMdVlPwXXO86o63LjFkWNd87Hv2tL" +
                "1X7PX26xIn3Se5Kz14/FXPzF4OlVZxFfhsanvFDndLnvCN+Hc7" +
                "oUmcVP2ud0BSzsLR86o3O690LndOS57yHqnI60HX1Op/QuLM/V" +
                "zyxCulzcV0hal7k4uUtaWBxRthe2aOejNhd6hZ5T9ky9rg3gGJ" +
                "rPRn+6wUFm1UZF9R3i3Lc2unwwuu8R/ak2MiRRu4CoYwt7fEvj" +
                "xqGMDD3uuodjO1UqkPJfKKxNtpM57jSHmeO3+9oJeXNfGCp8AT" +
                "sV/AZRqY2R447fATSMO2VzPtVO2p4ed2n+Nrud+LxUV6Fxl9Vo" +
                "XJa7qHTWrtonel8v/c+uGvGbufKaONlYvoy/136KuekGoHVZ+v" +
                "2uN0ZLEURiJur9fmdwoVfySc7jva5V1wPgiPd50nF9K4+y86fk" +
                "NcBMLqSLXdWHgt88jtN50xeXB72mJLS91t7WPuPnCUnz7D/yGr" +
                "Oc1v1GoEcorPZZP3f5t9E9m7DTuDnES0mUI3+XGc13xEfnkK/7" +
                "5fOYWzJ5WGvbPjsoS/tEeVY+MrT6R8+JX8je4Y+z3K1KY1I80j" +
                "bOq9dH96ck3tt0ziQkmusi6/vFgvN4j/0UlsQoan5O0rqsPMXG" +
                "LekprjSUuPpp2ybqSqo9yS6bC7ySvKJ8iq8+lt4emsPvmYlwJ5" +
                "7A3/XUvuxIXU3OZ4dpzC2pPkjI9pPeXR2YPQ9QvJRE86nI/nR5" +
                "ZD8asM+8ygMm0vr3dOWBODuTz6NtE182BmlZdXY4YJ/S+X9P53" +
                "rvr3/87+ms9r0yy02Il2p99RWccwrFElQ7PRnZT54sZiu5XqcY" +
                "t1Q3uBimYS1kO7WHpMJa81wyBxQtrb03EZdL07VJftsUklynU9" +
                "w/NUe4GKbp7Dqb1kK201WBFrjOL0dzyRxQtHTzfNcfl0vTtcRv" +
                "20L+C1JLbCs=");
            
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
            final int compressedBytes = 3058;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNq9W2+sFFcVX6gUS1swSInpBz88o58waTQRg4nZ2dmlMVolUR" +
                "FtDaQlRkqr9Ysx+sHZebBjdm0R+WONH4wf+GAKIbbBR6tWLaXw" +
                "gISa1tY0sbHU0NpHSKkNGjWp994zZ8459547u/vePnYzd8+f3/" +
                "md39ydmZ13BxqNRiNtNMoxbbQ/2Sg96+8+B7lGhWiofhqMHJUq" +
                "UcrpGc5MqnQdNpt6SuK6+d42hnQPX53Pw9j9UdX9d36ORv2Vfz" +
                "qe7X+irncdq4ayFnp6NajnGR8Ffn5HvLeW6dwDY/7ZqtOTfo7G" +
                "KHM020/qqupZQ5S10NOrQT3P+Cjw803x3lomGcjPRqO3HX2IJe" +
                "38CzwvGOf8aoz4/NorGfBs0laO1C9KFKqyXUy8HdsfwTvQEXFl" +
                "WqazFcZ8SzVPfT9HY/QbiGb7m+qq6llDlLXQ06vzzb4eHwV+fl" +
                "e8d52qfFs1T/783+5jp3fGc36k/41Rr5Qhl1F1dx1Wrdg6Wrf8" +
                "nsbIr9abOLbe7L4fYxTFM4lsXicZeJSY8h1+lHJ+Rp6xoCq/X+" +
                "8Lr+xWiqEGjiE7/4Xc25Br1BfOU7oqXWWOqZvE3K8cieGs8q05" +
                "tvm/8m/VZm9U7ieG9Oueaizo1f1feU7tTjcE59mBzvnImezi0w" +
                "edwg0UYbo3NBbhBV00VcP6YT7/zujdmodwbB7C+6fmofx7doQc" +
                "xw3zMdr/JkYNV+ZjKadndE69rz3vMIb4/LsaHvZRWqO/kuthc/" +
                "M7Xc73VDplzrstGLeIdCq5nnzlO5qicVg0VKDZ1ZnVCzGkWquA" +
                "fnG1qCf/QRzjZ5JlsPHrk2F4CHMcRf54r/4DtfO0TLfrMKRaq0" +
                "iO1HFxXByj8vblpzmWfog+xaQdZxknY3P1rObbe0OiuKqkb887" +
                "vV+dckSMp7mzDUa8jptfzG/7ORqj19Votp/VVdWzhihroadXg3" +
                "qe8VHg5z+O965Tle+vrAOT+13KD/aPNRbtle9TjoVHR7w2Pzp6" +
                "n3Qdjuk6/L1L11FU4jSfY0MUMOudsU+dtnRd/rDel37vMIYa4r" +
                "r53sZ01RzVd8OY/7K6Pv3MbB/gORrrWdQ77zN1VfWsRsfTEmUt" +
                "9PRq6Mcz1rb341Jrfjjem2eS+5L7Wj/BESzwrG36VRGe5XVg2Y" +
                "gdsQK5gUnyYMZGIcsz5CEzIoiRc5EW0EDqpW7gRiXISzmfUXrp" +
                "FfNe747E9Xazvvk0MWtDxPp2m3ZXrDJ/BdFguTzcj5cc7EhfDx" +
                "vUYT10tm/MIDvLX6FaQkE3vlX7UO0L6C7z1V6hGtJE3JgXyknN" +
                "5fRy6yqMZnadBZ61d/+DIojTfGJA2/FXTJzZ5ngXqKIMeciMCG" +
                "LkCIjaGHUH9VIdcKMS0IQeMiOGs2Pv8lz8cvnb8bHqV+Son/Nt" +
                "5YyOZvszdVX1rCHKWujp1cljw5SDnzwW780z6Zn0DI7og9e+DW" +
                "IYQZzmE4O0ibd/Qq/zu/pdIMJ1agiqRC+Z0RHEVc7njM/s73Pp" +
                "nU3PGsuNLnYW3uY35irEMII4ek/vlBlAc5t4+09rPNU8sYzsAh" +
                "Hk1HRQjndPDusI4irn6bDP7O9z6Z1LzxnLjS52Dt6NRvE+iGEE" +
                "cfQ28yQygOY28Q6u03iqeWIZ2QUiyKnpoBzvnhzXEcRVztNxn9" +
                "nfZ/A6c3L9EHy7pX3IoI84ik7vtGj0icfndPP0bswAV2cuS8K+" +
                "xI5vf62Tq0A7u1Vqc/v/hM8XPtsAXGwlVVZnJ7Kn5Hpm9qzp/I" +
                "I5nm7L/uJUvGS2V02mvHPN3nLj23aesv9k73Svc3Uf7i6vOG7q" +
                "rg6u48+5qr9lr2QXzOffs9e9v3SXdt/lPle4cWX3lu7a7A/ZH7" +
                "077z9lz5XWi2b7K85T9kZp/bva/6PdGxzTGhc/lZkrTaas5yVH" +
                "GXtwX9Bd4sabu6vS2XTWHDtudMfYLLzNPH0EYhhBnOYTg7SJd3" +
                "CDXud39btABDk1HZTj3ZNjOoK4ynk65jP7+1x6J9OTxnKji52E" +
                "t5mnj0IMI4jTfGKQNvEOVuh1fle/C0SQU9NBOd492aUjiKucp1" +
                "0+s7/PpXcqPWUsN7rYKXibeUoghhHEaT4xSJt4W8v0Or+r38V6" +
                "rWXIqemgHO+ePK4jsFs1T4/7zP4+l975VJy14Nut2AgZ9BFHUU" +
                "CTReg0uBIM3sPxVCf7Sl7S4qN8RVKb2/9f+XySiXCcPfi75bzk" +
                "Z2s5dD8+U7vm82v+nHP4a/DBxVt/UtczHxlx/emR+fVk8/TEJP" +
                "dl8KFrO0+934xW2/vtiGt0+9J9OKIPXvIUxDCCOM0nBmkT76Cl" +
                "18muYRdg5Do1hJ8D9SFC6vNnQHYjhc7ak+4xlhtdbA+8zfXpdo" +
                "hhBHGaTwzSJt7B5/Q62TXsAozIGUP4OTNPSzWE1OfOu6V+tb/P" +
                "El8p/ilaxadimciRWZsfbJrvWTWsLyE4svnOaGwxXFC3N92LI/" +
                "rgFZ+BGEYQp/nEIG3iHWzW62TXsAswcp0aws+Z46ShIaQ+dzwF" +
                "1f4+S3z4N05xR92/I1FWa2rzvWcW/zrOFfROTrZHejA9iCP64B" +
                "VfgRhGEKf5xCBt4m09r9fJrmEXYOQ6NYSf0/qhNr63GrO/zxLf" +
                "2ew/Ny/uqp63uFz34xynPLNdXpdtvVhzJG6O17HnLQxlLfQo3t" +
                "3g9+O81hbPWzbLT00Xm80D6QEc0Qev2AExjCBO84mB7OJezLk9" +
                "ndXrZNewCzBynRrCz5l+pzUE1xfOgOxGCjm+86XgPvNJP+fbyj" +
                "cQzQ6O1FXVsxotv5coa6GnV7d2DFNO9ePvjZinE+ZbOTup62Dr" +
                "hYVU986Nez+u9+Pn3ZjX8f3pfhzRB69lxt6rFEGc5oPVcp/Ehj" +
                "mnO1Inu4ZdgJHr1BB+TuuH2vjeasxSDyG8GT9dWbN4HY+9xvo7" +
                "+PiCjqfXxj6e7p3s8YQrzu01/vp4cf+w9XFa75Z3L+r6+MlwfR" +
                "x7LmR93HJo6+Otr422Pl53B9heQ9Xtte21Lra2zDnfbsX3KYMb" +
                "IXHEesojhzdPz3A8fHKU1IF5n0d2oy3U0Pq6z8ez+ivWzX7WPE" +
                "fI5HOE7LW65whw/xR/jjA4vZDnCLu+WvccYded4XOE3uujPUcQ" +
                "52/Nc4T2VHuq9XMYzbfgLPCsDSPFyJY+MaBNXJaDM8s6yKJvLf" +
                "IQiyowI3VA1Maou6YbsqgELPSQGfpzBejFn98Vg0len3qXru3z" +
                "u96mhV+feHV7dducIzC6uVsNbzNPD0EMI4jTfLA6F+wnsWHO9b" +
                "qg1/ld/S4QQU5NB+V497AfMvG91Zj9fSZ88yps7pezOofzZzFX" +
                "rtQ4FPlj/rL/sy7LWWMdfAyp1ip6b9VxxXjjmfRIegRH9MEr9k" +
                "IMI4ij9/ROmQE0t4l3MKvxcBV6F2DkOjUE4sjr/VdHEFc4A7Ib" +
                "KZT4sqp6jlzsi2Uid/a1+faWkf46ODqvvymOxvvNj3Gs5y1/ni" +
                "RvsfTaPm8plkx4ne7l9GUc0QevOAgxjCBO84lB2sRbLNfr/K5+" +
                "F4hwnRqCKtErrtcRxBXOgOxGCiU+OJ5emuT30U6v7fE06X7NS8" +
                "1LNIIFXvFw8xIiMCYt7kM158RseTzdGPJIFaEGjpF1Ms8r0StW" +
                "+P1IYdhbY/S8y83LxipHsMArjjTdv3LFiMzacXon+VBddr7MK8" +
                "vvd2vIw7SyDO/oc/o6qA9Wohf2I4Vhb43R8+aa5p4TR7DAK2aa" +
                "7m4UIzLr+1Bddp7jleXxtDLkYVpZhnf0OX0d1Acr0Stu9vuRwr" +
                "C3xujzV0+y3P+Qy6uZLrzfu+SBBa0/nVm861Oi/N++/NKIv+9X" +
                "5nlfsK2y3h5D6cZhkfa2RZmhjXr3yfdLL6YXcUQfvOKV9GJxgS" +
                "KI03xikDbxFqv0Or+r3wUiXKeGoEr0iiU6grjCGZDdSKHEB/cF" +
                "/5rk99E6tpBaXH/SX7vunGy/mhWF7Z3tdqTNjA/CJhEsvx2uT+" +
                "DFWDHbmonjiJF7bJ9nOg8SE+rgSiiOGlszUm24t+Vx/l65Z8jF" +
                "tdev63T2wibXpVh+bti/K+ArSIPn61Z/6hmLW0KFXAncZ9I6mV" +
                "s9WxNfdZLrWnLP5PoXK/o/0hIKKw==");
            
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
            final int compressedBytes = 2817;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW3uMHVUZvyhU6GK33VJbCzTBB6AYeRYtPti5M4Mo7doW9Q" +
                "8bIn+BTRNfSbVBCnPvGTp3u9tuowGRKPIoiq/UxEi3YqImlt1S" +
                "6x/W1vCI9R9r6lIBia1Aq86Zc7/5vnPmO+fO3L270dnMN9/j9z" +
                "3mu2fmnJl7t1aTW/SbaG/G1Jqvp9LvJZdM1cgW/aV5KqUvRH9P" +
                "6SuZ5oSyNN5Ys2zRZLQ/+q3i6+PRkZp1i05nkea0I/bp1vp4it" +
                "gXHWhj/5Duh6I/Rs+nxz9lmqUZPZ7u/0q9z2qcrXwaCxrzU91T" +
                "hWzPRM8R6Wh0LKVT0Uvp2b8W/TPTnYxejU41DD9vQO1Zn06Dtn" +
                "Um2ADlR94AysXNj5B20hYr4HgXBqvmPFQ+e7VQjzynMlXBFu5W" +
                "NPxR3qdzTBtS22a3JstcXu6oRZTkQOK9kwvNekwU+lc5m/AJRU" +
                "V+FbXmFmz3As9v4j5p9Teyn99GRweecEXlUJIDifdW+ahFR/kb" +
                "lQxHPiOjm1A0/FXep/mmzb8LeMtYvktaJUrXIrXUM+GKyqEkBx" +
                "LvrfJRi45SteKRz0j4NeEapIpTUmuhpKgJ1/ib0IpUcZL3N2WZ" +
                "N6EGONBSb7DQHHoNFKP76XbqCVIxn7ICXtWK1RYjmvEzbiqckh" +
                "T3tE8Xa71tI9AeToHFuPq+TH3A6q81cXp0V0TMD1aIrHRqvsPq" +
                "JfXX6tWasUT7PpC8jZ4ZVkJrJ3Njui5o7BLnNHbjuqB1mb4uEH" +
                "P9O9zrAv8OpLguEKNK2/26oFbbctC1LmjsLa4LZBWudQFUmdZF" +
                "1gXp0bIuCOYEc4CCrKTWTUoHmmCOvxl59EPO3yyPCoWeUpvWs5" +
                "n3M7NSK2paK6lfEYGeIBXzKStkU1UBjkbWzzmX+oN+2msly721" +
                "WllADvr9O0FGiv5g9+/UY4KW4qkfzavHNTGIwqqKWshqxqNWqA" +
                "ePureuUd6NX7dH+6503y3m5fPdWuAav+SvlsY44Sfbx9819jPI" +
                "J8ODtWlsjZ86rXuZ+xmbr7En5ya6qyR8StFGvh5srU/vcpdQm3" +
                "838JZ1wd3SKlG6Fqkttytqdre9VEdJDiTeW+WjFsk3H9ZrxaO9" +
                "J8w6cUnaZ+zTV017c16nbosFaYTCeBKLm/3Je6YznsRCl7XZx3" +
                "T2sg4jtOR4EueJRVk/j/hHgIKspNawfyR5r9SANjyKVqToGR7N" +
                "PoWjoAMv1FJvn8x/1IISYpLLdT/dTj1BKuaTVI0nrAqrpfn5+H" +
                "mHs/tT89Z8PG0t9PaC7kdE4k9nPG05WPX+lNRrM7rV76ccStV8" +
                "3bZqUTt7qHVmGY/qmXPPxfXFSBWnpNaYpKjRrZKKDSgrbxoT0O" +
                "0Z9g3FOHoVxRooRvfT7dQTpGI+rJDGTQJbRDO+cW1dlF932423" +
                "MbcXsBvsNlMTlP7kirE6YTmPoMfXmbg63Zdn3GB2rbfnO/GxVv" +
                "ZWQdxUZr4Tq8SnsuPl4ko134lrctv7xPvT+W416xeKleLjIl+p" +
                "iU+k+yc1xBXigzDfiQ+1ddcTuy88Od+Jj7bloYyuUe+f2rqrMn" +
                "ptRq8TK9R8Jz4gPszWdIP4CJFWi5u1T2a7fkzH0y9AVjpvpW7X" +
                "vFea3qAx47Pe26nV9ORQUJXEpvqVHNrMalYACHtlnCUcVzQI85" +
                "Ebmjak1pXZOPUjV0HIadHLHbWIkhxIvLfKRy0mCv1dZ0NmVe25" +
                "hawzX+j03KJFmXRan0zWzO5zC7/OxOeWLmqw9emldH+xV893wb" +
                "mdK2m93Ls+8fmm83w3eBiPg4dBSqv+h4mwz3d2DFiDN5fo0yud" +
                "aixv5fO543Q5nk708n1B0DetWfkLlcdTX2/HU3ggPAAUZCW1Ti" +
                "odaADHyRhB5zFuso73M7OaWZSG1skh0BOk5EIegbGKHdCzYYXq" +
                "aBtP3vpejqeRQ7M7nrD6nt2fxvE4OD6Yn3tyq4mwebowYB05PJ" +
                "0+dYrOzHefqR6ny/vTqW7GE/f+KRtPz8zyuuBz5cZT8vnSn9Ue" +
                "PA7uGcwjtf5tImyeLkwZa/kay1u9peWQPK7SePpPd+OJX2dOr0" +
                "/Vx5O3uNw6k8exPZ7A4+DEIH43/TW1I8LmqfMcmsbtajxNVLV6" +
                "C91I4LyFFWaTRzL6Xe19wYPD12XHxwjuJ7Z1pvi2yH7nIr4lHm" +
                "fejz/a7B95ls38ULp/39Dt6ub9uNip4X/oLSLSd+zvxymOrfAH" +
                "4sfaOmqPosGOfKW2w7QhLa7HKZJZ9e0Yec6xhtsTdrh/yVooSn" +
                "Ig8d7eErMeE6Vkb4k9N2cJb1MU70/iZ6YNqTWyxdpc7/ZyR01r" +
                "2a2jJAcS751sNusxUehf5WzCSUUb+Sjy3mnakFojT3Zz9wkny/" +
                "hRlORA4r1V9dRiotC/ytmE+xTF8RTebtqQWiPv62o87XNHLaIk" +
                "BxLvraqnFhOF/lXOxhv1vqiOOWpM7lJWOtPOvgEcrW7Jco926N" +
                "GYjsKqsHIuH41r5gBEuZrFftv6Kezp891sr5/Cnj/fGTNL/o4+" +
                "2Vr7n9mC66tak+HeVzF4Uu10/VSrDd8MNopCudJ4eLb+QKcKOD" +
                "73P1HEYNWch8pXplo7hrN42/RjOnK3y13KqNN5e5QqFmlzR1W1" +
                "UBStytvGfR+srK7KAVGu5mBFsAIoyEoa/nSwYsvbUQM4TsYIOo" +
                "9x69/k/cysZhal2fI36ldEoCdIxXySqt9h0My0AxBBP2cdD/+3" +
                "IfJfZtTP53osf5/Z1Zu2P7us0enOESLyDjI6lO7P5xL3+4Ks+s" +
                "b8EnGPWu8WnyUj+nh4HCjIShr+ktKBBnCcjBF0HuMGI7yfmdXM" +
                "ojS0Tg6BniAV80EkerZcZPOcTXzhPv6VwmpxXql7dvE5+OVmf5" +
                "BM671v5d+JdcpX+ndix8WLrH/ep+Ca3s2oaZ+2znKfZnhVQ8bT" +
                "13s6nlqz3KdWb8dTcGNwI1CQ5Z+4Z/hepQMN4DgZI+g8iTvM++" +
                "lZ0V/X0Do5BHrm0jCPwFg0N41snjMgbM8t9WX/z88t9WUz89zi" +
                "jenH9Lq7D2TU6bw9ShWLtLmj1mrxLTqKVuWNxets+VyVA6J8zd" +
                "4CtWfX+rV5bWeCjaJQrrYlDzv7u4DnXRismvNIznDFssUta6H3" +
                "8eShXt3H5Xw3vesuPqvqfVws6819XKsi+w/XeGl8gTbffSPTLr" +
                "D3Cd+Px+35KH5TsU9xX3xusz951JH/LTn3Vtd8F7Or6/h82adY" +
                "+31KvCh5hEhnZ3Su3qe4Px4o0ZvzYu2bq/DnipI+3W/akFqfV6" +
                "3WZKfLyx21iJIcSLy3uu6oxUShf5WzCZ9WtJ6vz5ADG1J+q291" +
                "WR0deLqMH0VJDiTeO/meWa2JQn97Rsd1m3+PEF/UcQ1mfC/VfJ" +
                "fjbnlqWvenW5zWdd3n816v8J7uVbXrfaonYKMolJnx1PUzHI1a" +
                "zOD9lcNg1VxNyeM2iyt3WUt6rtuKXLtax3dsykY9vNu67VkVT4" +
                "XlPOJ3zOzzHf4/QvKYUdMqR72ZLb64HLrDua+qiuU8xLtntk/1" +
                "e4pcu6YhR71DpoeJ9l4rfe5DFfo0ZPMon6/y55j9Yqqef2dVH6" +
                "3c49GZqi2+tHPlhsclPR4/A/UBpIpT0vAD9QFAgE7nqKy8aUyw" +
                "Uq0eR6+iWANgvGN6Pj2WXj1I3jEzH1ZYzM1F1KVgebC8VlM0e4" +
                "ewXP2lfdqpdKABHP6JDbpFoSmPcb2TXJz83Qax6FmUBmJydaCN" +
                "Zi/mg0j0bLnI5jmb+PZ4vTLnrurhdXN1eMbM3VPjK5g1Yq/z/R" +
                "es7qrU");
            
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
            final int compressedBytes = 2598;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW2uMJFUV7vG1RhMgGoLBxBAXiYrh35oYEu3uqnZN1siys7" +
                "uz7IpR4YeG+INoIvHXTHe1nW7/zP4gaOITVrMggg7xQcCY9QVx" +
                "A5IQ1000O6xRV5yF3ZHl4Q6CVXX6q3Puvadu39qu6Wh16tZ5fO" +
                "c7596ue+vRM43G0pFGvi3dn+4/XXpHY7y125CWftFQt6WfCfnR" +
                "8fHxpaMK8qHGVNvSA17vb1wbV28gHyykR6pVkHwsaScfTpqF/p" +
                "Ei00VVe5PEpZ4dyUenGack8nq3K+MUWH3SubCKundDGtzT+J/Z" +
                "khu93gOurfe+Ta5oN6Th9+rkjf8x23GaLl/51lo2j+k4PQCdbK" +
                "1dpj/9zm4ponfZ0bDY/Hpu6bUjNRSqyrCpfVdZfwzeZR1RXpn0" +
                "JJ9YOpLcpK3j0VfsdTz59IWv4/G/Z7uO6/ncdTz5VHgVvTvzNp" +
                "9nGKfeN4cP58fvirWrdG3sfaN3b378eu+wO069u7oXd16vxn07" +
                "3e+2bPcruLd619Q3p4hDBv77Ml/vW8qIBV7vevf0fgC5+RLtec" +
                "5PFvPuEfgkivVqW+c1Pq9kLctgY7hqLYLyhVRbjrE9rUtoz8eZ" +
                "16fH4ZMo1iuu4//xro6X6LIPw1VrEZQvpNpyjO1pbaHdmHf94e" +
                "/hkyjWK47T37y1btFlH4ar1iIoX0i15RhfdPvjrjT9Ni2XP37x" +
                "8vozqvPwPO3WvPsjfBLFesXz6ZlJFWhysVJ/zsVw1VoE5Qupth" +
                "zji+b7guQu6yw84Dl3D/gt3c9WuJM7oI9TOVaLSO7c3PtxHqfh" +
                "n5xqg56ZcF8w/HNxbV3vXhz/fZqqJt8XOOfThHzB9wXP9M6ozw" +
                "DFU90w9Q+fFd/dDZ5z4IZJlvjc5JqGZyfl0bNoESH5pnqSureo" +
                "2srU2umpd+ckS/zPgHF6flIePYsWEZKv4kwref80fKHO90/x2R" +
                "k/t5yt9/1TdCo6hRY6acMNssHPR45gnXCSk6PyutddHrMKeFiT" +
                "GDPO9MtIaFk+t1qzPp1Z1iP5rREv7gs6y7QX5/h1nvP/Or+lux" +
                "i/GjyXlDydZR9WiwjPF3hfc3V8NVropI2uIRsswGk6M5iy4H1Z" +
                "j7Oz2lnIIuvUEBxZaC/rCOZyR8DMxhWaePt8Slbq+za6i7N+n9" +
                "n90uZe77o9SKNt1jk+75kr85Ms8bPB826+wvVuviwiPF/gvNse" +
                "b0cLnbTRB8kGC3CazgymLHhP63F2VjsLWUYfknEugiML7bSO4D" +
                "67I8AYGTvWFuKFVMrb3LZAn1Rqkw0W4PjTu8X0EFrKgvd5jaeo" +
                "VXjMLGQBp1YH+2R2Nx+YZG81ZrvPNt5+bhnZb7h3e87/3ZMscf" +
                "Dzsy+PjtUi4vM1z7tr42vRQidtNE82WIDjT3o+GR5CS5l5O2/Q" +
                "eGQV8JhZyCLr1BAcCc3NBybZW43Z7rOJbx00j+k47YHONlNWvt" +
                "+D1T2Zz89K1zuJklW1DmrXO/L6KgcirOZ4Z7wTLXTSRjeSDRbg" +
                "+JOeT4aH0FIWvE9rPLIKeMwsZJF1agiOLLSndQRzuSNgZuMKTf" +
                "x4fqwV3+GTdc7vzpbGpm3a+8zNyNd6P+3WOv4Z+CSKdfk7Z8A4" +
                "vW5SBZrsw3DVWgTlK+Mq4/V5yt4XjG613xckx6d4X3Bmxu8Lzt" +
                "T7viC+Ob4ZLXTSRp8nGyzA8SddnwwPoaUseJ/TeGQV8JhZyCLr" +
                "1BAcWWjP6QjmckfAzMYV4tjcoN2ad1+Ab/xWPUexXvH7+Jf3Xf" +
                "6GLvswXLUWQflCqi3HmJ7SeffFWt/TvTjjefdi2LxLnpr2febo" +
                "tlrHaW3G47RW7/rkeW7pWav/fs81Y79rGSXG9ee1wVff/RWu1P" +
                "vLIsLzBV6vn+g8gRY6aaOvkg0W4DSdGUyZeeO/6nF2VjsLWWSd" +
                "GoIjobn5wCR7qzHbfQYC884+nzq3XuAZeVR98lif6j2d96/jEu" +
                "W3Zr16ZX06PfX6dF+t69MrM16fXgkZpyT46hKdj86jhU7a6Hdk" +
                "gwU4TWcGU2be+AU9zs5qZyGLrFNDcCQ0Nx+YZG81ZrvPNn4c9a" +
                "orjdfMfZ71dN8kS3wqeG3eV2Ed31cWEZ5v6nl3tNZ5tzHjebcR" +
                "tj713zTtfUHf+uuG1vWe7/X6SZbwP2f35dGxWkSnUf/W2kZ7/n" +
                "tL8Yzcvwg+iWptu8DnlpcmVaDJPgxXrUVQvpBqyzHSEz0WPYYW" +
                "OmmD+8gGC3CazgymzLw6j+3jeNMi69QQHAmtt6AjmMsdATMbV5" +
                "hLx6Jjxjqe69k++BF5oAPHVkKzZHKoV9dj4NfzmrwuD1vMisza" +
                "sm3wQ5tPesuq0y1af8S8e4t1Fu71nLt7J1uCV4G9VbFaxODhet" +
                "em6Hh0HC100gY/IRsswGk6M5gy8+o8to/jTYusU0NwJLTBj3UE" +
                "c7kjYGbjCulYdl/Q+UCd9wVT3rtUvi/g6ut5XxDdEd3RXqe20S" +
                "CJtExOvsYW4DSdGSCDm5gkc+aTWShK84K5vT54UHrMOmQfuBK3" +
                "7gzX/Y7ZW9QF5uzIlct+5Mc5tNFcNP4/kVSaY18uHWIZGCnB76" +
                "KIWf2W5pCn9Huc0xgt/RAq4L5IjImXvS2rq9L9+B/qnHed22c8" +
                "727f3Pd04nr3NuvassfGiv+/c3y2pX8s+Bq2p8L1bk9ZxODnjU" +
                "3dxDhZvx62Fjz1LkyyDH4Z3HclT//tPqwWMThS833B4ehw8ylq" +
                "Gw2SSMtkaQFO05kBMriJyeSBh3wUpXnBDAQzunjYUIlbd4bj+q" +
                "gm1AVmwsgKoOW/v6ygba5gfWqusLX4ncb6S0TWJdZFEbP6y88K" +
                "8pT+NrTiy+vaUEN53bK3ZXX51nHnd4S/TIrUfzfX3/sOfj3b97" +
                "6DX9X73je+Ir6ieSIf4RPZnumZtXkik8mS6WSjCLSEJon94Ci+" +
                "vxPEjGzEhTjykkWy48OxjKJswMo+cA1Ut6wTeNlbyc18tgW1NU" +
                "+ibZ7E/702T7K16HEq999j6lJyI5jJtrIvZ3136bwrYvvvZFv/" +
                "Xan+XhsnaxjHXOVWJHubc20NOp8uiy9rn8uOWds+l+mkZTJZMh" +
                "0IkqklNEnsBwe2jIkQlI246JP52CPZGYFYRlE2uXNtqIHqlnWC" +
                "Gwjemd0dm6KarXE6ntTmtq30SVf5Dlq2sZx92EcSxTMbovJrhs" +
                "pDHqnbWTINCHgkF+eT0WY+VCf7WFzvO5IZGMlOWmeN/jIsvnR8" +
                "H5vr2R7tIA90/AUZWzME4aMd8i/MwGncf+yAh7g6a4stytk7KG" +
                "PQ4sOxJjMqyjgWLzdroxjm6y1zr/S67OjxeF7K1eRzdRVtc7VY" +
                "n1bZWszsVWt9WjUlN4KZbCv7ctby9amIFevTqrI+rZqVjGOuci" +
                "uSvc25wtanK+MrW7dRm97Z5hJpmSwtwGk6M0AGNzGZPPCQj6LY" +
                "wxqYgWBGieA+cHaq3qyOuFEJ1QQNzMBIduQuew4e/Pb/+f3Tl9" +
                "9Y83PwfwFQEVoI");
            
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
            final int compressedBytes = 1894;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW81v3EQU96mHRgIkLj3xH4BCk7SFaMmu7VUabnzkwgFBpI" +
                "pWESCQgCNsnNWK7KUSFIgKQpwohVIgQKk4pIEGkKr0BBIqIFRB" +
                "qg0iH3BABVQJT56f3xvP2OusxxPWq528z9/7+e3M2OtVHMdxvE" +
                "NONHqHGocdBzSy4hGccKSDfCAFryQzyKtasYrek5Wry0AbsuYx" +
                "cjw/2zReGvxFb7G6BqPjgASakLkF43Q6IaCM2IAk46AHfJCl8y" +
                "IyRhCiGg9siYnKW3gFK362qCEyxnB0rO047iqO7urMAeifu9oc" +
                "CmfIa+BDm9xjva5aAZksrWXuS8brMIP5rLrchhz0vGfeks9Wj6" +
                "U/GkuSdhtKc51umcG0Fu+Szup/7BQ4ZjO5zK7nrdc4H0tfR7m/" +
                "51x3V8NXBcZQAwlsoSxZrkbRV1mMJAFe8CzJUVYlFUdUqZAupE" +
                "SVSsSM51V4BJ3DtlQh9kl2EattLXiG82JcGXN+Hsm+4bpznObB" +
                "3uaT/vDfcaweputV16vrNIIkXkHTqwldjPgiL0jk82qQzTExOp" +
                "q3Ghz0yLikYQxGoEfGktmjxmNAJoZUG+N4TWIu4yfm00a8P62b" +
                "nE+2j2bO6/3MHz3ij0Y9aM5tGF0H71tedznrNSs99qkaz6et/9" +
                "HsqO04YyxnnJtzf9qqbtWewxEk0ITsOGThXp4HkrCIETMQG5Bk" +
                "HPQIK3i5hzRExghC5FjEBTgQe5k3YCMTxCVfEjGhbVY3QykaQQ" +
                "LNGxdjcAwtsleMIgJe3jhkR73fBBljt/fLcRUHPTIuaRiDEeiR" +
                "sWT2qPEYkIkh1cY4XpOYy/iJfe0R3J+adaP7xTnL+1NJ9V6Ivk" +
                "sEP8eVXtfG9bi7Nx/KrH4jB8MVJn8Xvn/M7NM2+8YtOXCvpX5T" +
                "OUpybag25N2BI0igCTmcl7GFe3keSMIiRsxAbMCQcbhHvFAHD2" +
                "mIjBHokbGIC3Ag9jJvwEYmiEu+JKKsZdyPP2x0HZy1vO4M1/PW" +
                "vLXqTzCGO9a2BJqQuQXjdDohoIzYgCTjoAd8kEUe0hAZIwiRR9" +
                "A5UHVgL7MDbGQCnFBDZIzh6Fg7jO3Uj9SPiFG8HUe8q51qB3zQ" +
                "T4xAvxjBB9EUW42/24toESUsgKzclXTAi4iYxyPgDZWxLiDzPG" +
                "SO50I5yJMQIQKxMYpGZK5jnFx34fXuUaPr4EPL667kevUz8Yqc" +
                "6LpmJ3qJze9JiwhezLGfTOSr0L06i92Po7e/voQ2sspxOp3HJq" +
                "MQWV8Z62RxS6+r2pBDOm9+tmm8Up6tdnB0O82jaBP67LzbScbp" +
                "dELQRQGyvjJ4s7m5ndYFfV3Vhhz0vLef+7KzTeOV4zkD3RcYfW" +
                "riv2t5fyq5HtvHnzDK+1PLfSq5XvOpWHraKO8zlvtUQr16C8d6" +
                "q3EcbaCDj8fpdELQRQGSvrJcQc8tGaPLQBty0PMmJihlV08/2O" +
                "9SPT4R1f0uNXOz/3aRTzK4NXOvGNDMpy718HepXg/Wpz8VPjcV" +
                "6NNpy306XXKfjsfPx68XQ5r7W+L9ieX9qeR6bD7dMIh6xT9VKP" +
                "+vHffpVMl9iueTP9R1NSTusBovZ/D+yPJ8MlzP3efuoxEk0Np3" +
                "ipEssleMwTTpkM0xMTri/YGKI7NQOfAYOU/280zU1HrEUK2tQ0" +
                "zi6+dT2KcDRufTe5bnk+F6/qQ/iSPqoLUPgg0tGEevYFr2QDSX" +
                "Ge6CDoezQI9cBSycpy6CMmNtQR9BWGoH5GrEUI5P7uPtu5w+Pl" +
                "qXjO/c0ZOUxtnwfY765J+MIxbh7+z3iczPmPxN9Pey7v6p8XlB" +
                "jguZ3mXNOjmpjTzf6/2TP+VP4Yi6eHl7vb3+1Ow8WTBOpxOCLB" +
                "OuQNPlyVUpX7a0VnieGkGZqKn1xCieq/CzlTuACPI5J+OVdXfP" +
                "TvfxzHVw2fK6W7J1n9keM9kn9yu7fTJfL21/cp9P7k/NV/tnfy" +
                "L22ftT80Th+VQLq60Y+3xX7GbrM3pn4VbdKo0ggdY+LEayyF4x" +
                "hvfjsQ7ZHBOjoyc/8yqOzELlwGPkPNnPM1FT6xFDtbYOMYmfmE" +
                "9vxvNpotgMat8r7au/Wt7HfzG83w24AzSCBFr7QXeg/QBZZG9S" +
                "h2yOid5oPr2k4sgsVA48Rs6T/TwTNbUeMVRr6xCT+Knz6T6jz5" +
                "bfyDED77dbb8dziv0nA34Pdlfbk6X938Y17tvd/9so+vwp/HQf" +
                "MzqfvrC7P5Vdj/XpmFHeFy336aK1Pj1u8n68vmy5T8vW+vSk0e" +
                "93a5bvCzq2+lSf6efnT2Wzpz61fuvnPgXf2npe4C+aW3eNKwVZ" +
                "7fh3qdaGrfnkf9nP86ls9qxPF/q5T61NO33y9nh7TOKaRdv9en" +
                "Gfxrwxo7zHLPdpzNa6c2/v53VXNnt2X7DVz30KfrDVp9rd/dyn" +
                "stmzPo0WvOJc39U+jVpbd//29X3BP5aud4PeoHINGexyjRlMj+" +
                "qWa/x6N1jMn7tPw96wgj3cpfZwelS3XON9Gi7mz92nEW9EwR7p" +
                "UnskPapbrvE+jRTzK8d/B9wCHw==");
            
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
            final int compressedBytes = 1630;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXM1vW0UQf0hIIBGKUFEQREKECwiVAzIXFCTGTixOSIgDd8" +
                "6RSmgaWlpK66/Uki9cqIQ4IfFxg4PbBuLmu0nTRgQaDkGClgrx" +
                "ByD4A+A5L/tmd9/seue99Up9Vr0zO7+Z+Xk8u177tY0i9ap9Iq" +
                "TOTDTgakxHztfU71HQa9j5pDqd8Mr7buA63Q1Tp8nxyXHdlp2h" +
                "7DqKnh325cbVSz/NeV13dwL3k/d8tbXD8bv4z0LtmbROp1LEis" +
                "Hze0nePhx3azsEstd+uBDHrtW6mZ27uEYif0ilG376qdGanMj0" +
                "z6cD+uvSQUdPkH0+EXjdTRSzO+9PY5NjUdT8TIk9NiD3GIWiZ3" +
                "lX+yF2nch89S9cXwtjfzrj8/1tj4Ttp/Yjwc4F53zGLf8Xtk7D" +
                "zift4/VM1x5xikDs4/XHKg8M9uw0jDvfUZtfneidQfl87eNxpu" +
                "ej+/gaNnusU/vR+7lOzShUnTptr/vqkbB1mn8rWJ0+93oePx/4" +
                "PH4+TJ0arc6XxSJ1vlJ41wLXqRasTl975f1R4DoNOV9rPa1Tz+" +
                "u6ez9wnTznqx6vHsfnROo/4jqt9HWcka2J1JhGPfGWYwr0Ie/Z" +
                "bByVRZaDjFH9VLvsKbRsPmSYzU1F1ONr6y7dVTo/eu2nmcD9NO" +
                "R84jwe99NPXut0IXCdPOdrbWv6jbROP3ut00ki99YQ63TS8/5U" +
                "qpbwOZH6j7hOe30dZ2RrIsX7U6on3nJMgT7k/XE2jsoiy0HGqH" +
                "6qXfYUWjYfMszmpiKqGszCbBSJ50RKtM4v/WecUa3958Y06ol3" +
                "cuGMkGLec9k4yFW2yBn1mDoPzCM8hZbNhwyzuamIenzj+Wnf6z" +
                "o4G3h/OhtsH//VK+8PA9fJez7jfYT0DpiP+whT7xXiyL6PQOfL" +
                "3kdo3sv5+beX9tPAO6qsz7tzw+ud1u0Q+WAbR9iG9JzQflxHmD" +
                "zVKBSanuVzdLc2frMj+XzodRf30x9e193pwOvutNu6Y7xX+zjC" +
                "PqSfcp17OsLkqUah0LA/dapQP+1zrXQ+RALz07y6W90Vz0LvP+" +
                "J+upPMiRmBw0d8zlQsCVqWMe7UGSqOzEJY1CzJjMyTQqCn0LL5" +
                "RCT51VKR9dcsEKZ1J3Wrj3VXD7zu6r7XnenzzvN55oPAn3fe8w" +
                "Xqp0J/m6oxw+6nE9738as4wlWhxeeCp3SEydOGEVa73ZWju7X5" +
                "Ij/OAA4LOMKC0OLda15HmDxtGGG12105uluRvXucfN9b8q07A7" +
                "JX/ifsPt4eta+7wnvi3nD22vLfYffxi994/95yHUe4LrQoqjyh" +
                "I0yeqkyh5bhFOLpbkT2N5POBDRxhQ2hx5z6tI0yeqkyh5bi56r" +
                "TBtTaP2ZF8PrCEIywJLX5H5nSEyVOVKbQcN1edlrjWypwdyecD" +
                "WzjCFqS/7Vfe0BEmT1Wm0HLcXHXa4lqRPY3k84FVHGFVaPG6e1" +
                "ZHmDxVmULLcXPVaZVrbb5qR/L5wDKOsCy0+B15TkeYPFWZQstx" +
                "c9VpmWtF9jSSz8f3+Yn+3lL+N/D56Unf31tMdWqP+6xTwe+g7D" +
                "o1X/P+/W4dR1gXWty5JR1h8lRlCi3HzbXu1rnWSsmO5POBHo7Q" +
                "g/Sdr7ysI0yeqkyh5bi56tTjWpE9jeTzgTUcYQ3SfxfSPqYjTJ" +
                "6qTKHluLnqtMa1Nt+2I/l8YAVHWIF0L6q8qSNMnqpMoeW4ueq0" +
                "wrUiexrJ5wOLOMKi0OJ+eklHmDxVmULLcXPVaZFrbb5jR/L5wD" +
                "Uc4ZrQKIRtHmU6gikGj6O7tdyyI4VUdv59GzZxhE1IP2PLqzrC" +
                "5KnKFFqOm6tOm1xredWO5POBmzjCTaHF6+51HWHyVKNQaHqWz9" +
                "Hd2rxgR/L5+DuPV0fN9xGqo2HPmdVRt3OmOy9/dWpOGXP02i+E" +
                "rdP8JXud0r6rOvd0F0foQteMsM335cq75hzQLbTuulwrzaUYC/" +
                "kq8vt49Wg+W2HOt/Pny8urUJ1G8tmGUqeR4py1XtzBEXZgx4yw" +
                "zfflyow5gikGj6O7tTJjR/L5wC0c4ZbQKIRt/sD7W3MEUwweR3" +
                "drn4sNyefj93c64//z8Erg3+lKoX6ny1cn07mg/GDYOtH5itwP" +
                "hss4wmWhUQjb/IH3X+Ycphg8ju5WmksRFnAFR7giNAphmz/w/t" +
                "OcwxSDx9HdSnMpwOJ/5RzbNA==");
            
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
            final int compressedBytes = 1762;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrlW09oHFUYX0V6CbQHPdWSU3su7SUSKGQms2qSkyARRY007a" +
                "EsHtQQkCbWzSab6aFEEL2J9GDVFInEQ7SCIiIKgWIbPbReRD0o" +
                "kqYFEQUFd/fNt7/3vffN7szO28mkztC33+973/f7fvPy5s2f3Z" +
                "ZKfKsOlhJvtUry2PB4KdctPOaasfpF9LnW+LchjVP185jMjzX7" +
                "m1LJf77xebW6KUR+urSdSeNHHXu/sn1NLULkJ23ra/W5+GOPit" +
                "rj5H3vbj4tnMn2t6xtpM3orj7dFqwEK9QSVsj7UvnIQ3ESBgPs" +
                "xTnq48zByuLLyDOrmlWUJxzW69kRyCTUVC9F4JjtEUCMnmvGR/" +
                "PptT6tF+M5r09jxV2fWp8x69PIv/muT3I9e33qWdFg77kjq7Gs" +
                "NzOq+tOdFkezK8s4rRVonNbyHCf/BzesC2eyMaXPljNcHU+2+e" +
                "RXijOf/Epxz7vwqeKMU/hkf8dpka1/I+vxSFgT1vulqvxb6vVp" +
                "fffmU/XvLto+2J359OrBNFr6MU7hMww93WWcLhXoendp9+bTwv" +
                "4u2n5uMQj3mQsHMj7f3d/xajoQp6XDcTq9zwynGHq2yzjdKNB8" +
                "utHn+ZTh+W7k3VJhtn5ryTROHxZonPqsJcv9UwfWm+F0vufd8g" +
                "NujyC4EFzw31Jt4y62ZSnUtFULH2yOwUA2uJocOjPPU72EmxYQ" +
                "xZIK6uE6cAyoLulWvaREWYSIWdXXFRCKn0/+2dTPCmfjn+8yXu" +
                "823Glxfd75c6m1zRXo+W4uz/XJv1yMVTm9Djkjy/HEvc8MX2hH" +
                "OHifmff3LTXxDYq771vq7FnJXy3IfFp1k+HueOqHUqyulVJht/" +
                "BFx3+peX8erbIiNNRs4TF6DayydU7q1b2ch6uwNZjZyOP9emYb" +
                "DZn1oNCuLTGa/Pl83xLO5Ls+hQPJ1qfEb8DOl89TS1gh/4TykY" +
                "fiJAwGboNX5jH7kM89uk4pApmEmuqlCHDZI8CrQSGPN+8LwtnS" +
                "Ht4WN9zy1Y/H3hfMm+dd/Vjv5139aDHvCxKfd5vlTbTKUkhheP" +
                "ReExMDOJAZjforch5U2Brg0XVKEcgktLxPjgCXPQL6kZvIGvG3" +
                "O8y9hzJcp2v5nnfL+/pwF3edWv969Tb5FFZ9epyEwQAvPhWTnM" +
                "cryNrMGCmDV43TDSVkda6uzbpz5XNolaWQwvDw3mZbq9jR4KTo" +
                "SOEVm4ersDVQjH9F18kj9GNAtlQPCvXa4etxjCa/cd7d7s/9+O" +
                "hj+Z53SeuF72R9DvZmXN5nZrwqn0h7vfNm3F7vgovBRWoJE6KW" +
                "9tobsJEHq/Zm83N0FvngVV47z6yq98Kj65QikEnIrkdMCo3O6r" +
                "p0Zn7MqB13n+m95O68y/89XXf1GWf4aPvM/Wwv348v3ev4nmDS" +
                "n0SrLIUUhof3NttaxY4GJ0VHc/0+m4ersDXoMTyP9+uZhOx6UG" +
                "jXlhhNfmOG/1PIt0lbqc+759zGBcPBMLWECVELH2yOwcBt8Mo8" +
                "Zh/yuUfXKUUgk5B3So4AVzROp0xmUyuPb69Pj5Tuiq3+cMK4Rx" +
                "Pej98q36KWMCFq4YPNMRi4DV6Zx+xDPvfoOqUIZBLypuUIcEXz" +
                "adpkNrVGaKe807Babcu3o3Zl6x6KkzAYuA1emcfsQz73EKekA3" +
                "16de+kHAGuaJxOmsymVh5v3z+53PL//VP4XcIrRI//D+ZuGSdv" +
                "KuH1bup/Pk6nE47T6azj5B229OxPxBA9By9Nxo3T0uM5jNPhLi" +
                "qz/u6w/Tsxr+sb7SJ/z+kd7S9/v84778Gc1/Gf3B5BcC24Ri1h" +
                "QtTCB5tjMHAbvN4hOc+salbRVYDRVkGZhMIBOQJc9gjwavo46P" +
                "HW+jRY2sObe/Wx39/94vJ9Zt6/61l6Itn7zKS/6wm2gi1qCROi" +
                "Fj7YHIOB2+CVecw+5HOPrlOKQCah8Hc5Alz2CPBq+jg09rFgrG" +
                "G12pZvTO3K1j0UJ2EwcBu85fflPLOqWUVXAUZbBWUSsusRk360" +
                "ErN5zBGaCCYaVqtt+SbUrmzdQ3ESBgO3wRvekvPMqmYVXQUYbR" +
                "WUSSjcliPApdfWmc1jjtB4MN6wWm3LN652ZeseipMwGLgN3vJl" +
                "Oc+salbRVYDRVkGZhOx6xKQfrcRsHrMZH92LHOzTG8k/8r3eLf" +
                "/q+P34Ef8IWmUppDA8vNfEFA1O6o3+aldtHq7C1qDH8Dzer2cS" +
                "sutBoV1bYjT5e59PaZ5bgm/znU+u6wVDwRC1hAlRCx9sjsHAbf" +
                "CGf8l5ZlWziq4CjLYKyiS0fI8cAS57BHg1fRz0+O7Pd+neF2jz" +
                "7s7CgaCe7/Ndt3pJ3xfUtms7nd8XuD3vvPdyfm5xXe8/Oh3Xqg" +
                "==");
            
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
            final int compressedBytes = 1767;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW8+LHEUUXnMyLrgsGiWHXYIQMIguKuSiB6dmWo0bwx68iA" +
                "dRcnKjYHKREMXZ6STbCJpDQow5CUJIIHjwEEVBhIiC4MGDkAQV" +
                "NNk/IbDRg13z+s17r+p1b/d0ba1jD6l+P7/39Zuq2p6eydSUPP" +
                "rzU5tw9K93PmuVf7tpRrt6NRhtVp8uRe7Tpcns0+qduH1aXZ/Q" +
                "+XQx8ny6OKF9uhC5Txdi9cl0Q/apZX7jPoVkH3M+9f6K26d29V" +
                "QO3xXnz/N/V7Q+9b8tyfxyNMuP9n8sbD/3f1Iiv27DMEf/ovIK" +
                "vtdy1MivRtIPzXmY8zia89gnc56sMk7TeawflaNe13Gsz433a+" +
                "T5t/W6vg05lNWTV6tjbem6Oxt53Z3duv1p5d5aCMq6W5npnW7D" +
                "anBflXdlWunTBvXGWXdR5tOZyPPpTKz51Ptmku8LQrIf7W63cD" +
                "S3+qe4bfAxnLltI923AjL7TPE397nxGubgXFVdbkMOOu+VT+XV" +
                "tp9P3e3d7VNT2SOh5pNFizmf9Hq2T6HXXfJGOySeH3/dtWXfoE" +
                "+HWvbp0Jb26VC0Pi237NPylvZpOWxfzD6zr7sNR5BAs3J6jizc" +
                "y/NAshY75jvDNoiAGEBCK8+2Z+uDXPKQhsjdbdkDPE9iERfgQO" +
                "wlb+u1+xNGWInY+ohSG57XcTTr/X+K/q1bPT1n2PMu4zz7Ip0Q" +
                "tChAVt+ldfBWvI/D3GxOr+vbkIPOe/j3jl1tGS8FP81fa6MRJN" +
                "ByOXuIWZhX5A2loWVtiLgGEUXMEAmtIjsdWteK3JGHtBHy2uAg" +
                "z5NYxAU4EHvJ23qHfVojXGKrIArN69sJX6q9hisyzNWpqMdm1z" +
                "Mf+FLpp67lstzAnK5sXk722JicPvKl5rlhj5M7mudkC+F5JKs4" +
                "Jqv4uSVZBR18PE7TCUGLAiS9sqygc3NjtAy0IQedNzFBqbp6xZ" +
                "z4kEvm8ni5iu9yq5V3OUxGGxZlz32TN0cRxXPf486zL3rum8ve" +
                "c1/Kb/vcN89v/NyXV2eR3nPfE6+Ox2jl0eJz5Gx31t4/hVrbFm" +
                "38I3s4TL1wn4OxT6GP7PG49wUn7w+8hw+SAY0ggQY6WaTX1TGa" +
                "MNFb7Ax9H0ey8DnwGJkn/TwTNb8eMfRra4hS6//prODfG+4cNb" +
                "8ny55Ucn9rvE/9UXv+PhH4Hm7WzNIIEmigk0V6XR2jCRO9xbv2" +
                "iY8jWfgceIzMk36eiZpfjxj6tTVEqbWZT/Z+vO58Ov5r5Pn0VN" +
                "j51Nvb24sj6qjhSDaSpU4IUiZcHcf1Ub60cJ5aBGWilj2tRxCW" +
                "3wFZjfchf+3v7c+l4Ti07YcXyNyCcZpOCFImXB3H9VG+tCCmxo" +
                "N8vHpyTI8grGI/OOYiu1xBM0fMkXwFFiNIoIFOFul1dYzGDMrk" +
                "VonD9gjm4ZEupsuD6mAmask7bj1iyHEhTkN0tMPmcC4VI0iggU" +
                "4W6XV1jMYMyuRWicP6xDw80sV0eVAdzEQtM249Yshxs24Zootf" +
                "9nx8so/k3bBxJjEJjSCBBjpZpNfVMZow0cutEkey8Dm42ZQn/T" +
                "wTteQ9tx4x5LgQpyG6+P/T+XQ0bJzXp1Ojlbtvkvt08k5YPLPL" +
                "7KIRJNCyRTuSRXpdHbI5Jnq5VeJIFj4HN5vypJ9nopa94NYjhn" +
                "5tDdHFL5tP5u5Jnk+h2acHUiP051vsCaXPsdPFZEcrlpW/3j1x" +
                "ugmX8eJsn9Jn02f8Ppl7Gl9Nr7xPLd/Nyj6lzynzqSb7NKnZzz" +
                "l4kd5iPs1VVWn112uuqbduvdpx8/AivcXVzFdVadWn+abeuvXq" +
                "xoXcnyqqLPZubN66U5+D3Ah8BaX70xjv+nTFPj4dd3+qW29cXn" +
                "RfMMZ3EBW7de9a3PuC0PXMglmgEST76u7u7jYL9nsptFgbSjwP" +
                "JCt3d9szRCG2tPJssyBZ+BwoJntR5kk/z0TNr2dH+F6KWMkOSE" +
                "QXv/Q+c0/I+RT9PnPP5uKvHIBzd2d3Z/4+LnGftVQd4HejdGuz" +
                "Y3CwaYZej77nbMrHLJpFGkECDXSySK+rYzRhorfYL97ycSQLnw" +
                "OPkXnSzzNR8+sRQ7+2huji6/Mp9JG9FPl5wV2hEev//7tmv8MQ" +
                "kdF/h5G9okZ6v8M4fua/1Sfzctw+6fXa/16lc1OeUbIj2aRcju" +
                "JHVmF0blajIgvJzWfs16uuulFtzdO5Js/czm2dyvu3cm9VXuda" +
                "p8ZdIY/irPRcsFYxx4i6nMvWnVly1136eot1txR53S3VW3fpa+" +
                "H3J8/u9Cl5sLxP1hezT3o9v0/1eZX1qXM15HyK/feuczXWfOr8" +
                "Msn3BcQ+/n3BhutupmLdzURedzM1191M2z5lb0/yfErfD3z/9C" +
                "+IYulR");
            
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
            final int compressedBytes = 2110;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW0tsVGUUHhZuXTUxMchC4gJIXMmCANLee7vmqeERookmPg" +
                "jutCCY0A5MHeNCFrozAQSkXahRghJIoESikcQYQJs28RmNceGC" +
                "lSQ06p177pnvnPOfmU5n7lwL8d707/nO4/vO/89/7zzaqVT0Mb" +
                "a90vUxfH93sX4cnep1mjdUHapiJIsQYXh01OKGPbxUcnI072dp" +
                "yKO7CHuQObpOx2Ulo1CP54P+0JfHaPm730/VPZ3nRjvL3U/91q" +
                "tdaiptLLTvjSWvk6tXf6Yo/rGnmzv9q0DlxYUwHT7YMrK//+sU" +
                "dl/sMbqsqXQ1WKeXul2nsRd6W6exvQtep6vuftrb9XX2S+167Z" +
                "vaj+kKfZj+fFq76azdpQ7W98sm48+OynfJ6Z7uBjfaRr8PffPp" +
                "1X7Ixtne91OxR3Ky3PtT8XqjU/nvbD9569RqP41+Fu6n0a9Hrz" +
                "mZF5L3eurxk7ZR5xrz9UbPN60v8nvAT10+T+wOrQKefXaXXV9k" +
                "9+Vdd/o+3kX9gu+/g38XmxftinZhJIsQYXhk1GJmAAcq82ea/X" +
                "4dugh7gEf26WWgktH4gJ8BrnAF5MwtSqaT6fRqzsbsup6mk+za" +
                "cng4z8Ng0DZ4o2t+nVW1KuSpfS7rwgxUMgr1GuPYcVYTdzLDLP" +
                "sBCq67t3p49v6zzb79tuTX4wXrRSPRCEayCGX2LDw6ajFVS06O" +
                "5r7ZkEd3EfZgq1Gn47KyiQI9ng/6syugGS2/eRU92Z/Ht36w3P" +
                "00/lDB+2lVtAojWYQIw6OjFnM2ODmar9NrIY/uIuxB5ug6HZeV" +
                "jMZXWT10GGp7jJbf3J/m+vO5StnHkYLvT8mKZAWPjBnxCB9sjc" +
                "kaPEM1qAevz2NjqNce2aeXgUpGg2f8DHCFK6DV5Dqk58pkZWpl" +
                "Y+ZbSSfZ0sN5HiZrcIJqUA9en8fGUK89zOn1gZhUH5zwM8AltS" +
                "Wz7ZUzynl/V3+j3Pd34wOdvb9bbO+De3w3teB1ip4rep1SzmM8" +
                "RsfGquwjTDGZ52EwwIvfxOTXaQW/N5vjVWjVVn2jE7baqwuO1d" +
                "FqjGTl6Gi0uv46PCZqcGYflZwczX1HQx7dRdgDcqrP6jodl5VN" +
                "FOg1RnrfEmp7jAatjdamVj6SRYgwPDpqMWdzBSrz109vhjyiVx" +
                "GRipbT9gEdrmSUvg9eG3ar+wuZdeea33yC0fxLVbRvwdfvvsqi" +
                "OYrvpdV9PFpzV9/H1/T3+a72T58+991R8ue+O4pdp2Rdso5Hxo" +
                "x4hA9246zu0RGugS14d3o8sguOaBXZBRhtBiqbaKefAa5wBbSa" +
                "XIdkXbQt2pbu0nwkixBheHTUYs7mClTm3SQhj7hKREQqWk7bB3" +
                "S4klGohw5DbY/R8pfzPjg6W/J9/GyxecnmZDOPjBnxCB9sjcGg" +
                "bfDW3/XrrKpVkV2AMeyCKxmNL/EzwBWugFaT65CeW5OtqZWNmW" +
                "8rnWRLD+fhTO9PKsI1sMEbXfR4mr2KiFZpoOgic3p9ICbVQz2e" +
                "j5xt+vgdt8x2zjnakmxJrWzMfFvoJFt6OM/DYNA2eKNzfp1VtS" +
                "oNFJ1jTq8PxKR6qMfzkbMFkvp6zjlan6xPrWzMfOvpJFt6OA9n" +
                "up9UhGtggze67PE0exURrdJA0WXm9PpATKqHejwfOVsgqa/nTG" +
                "j41vCtSoXGxtGwGPEIH2yNwaBt8NZP+XVW1arILsAYdsGVjMYH" +
                "/AxwSW3JbOdMKL4SX6lUaGwcDYsRj/DB1hgM8ZXqK7AF73a/Tq" +
                "uGKrILMIb5lCfQdj+Duaov54/f+5bZzjlHU3H6epzGzDdFJ9nS" +
                "w3keBoO2wTt806/TqqGK7AKMYT4rMwr1mEnO1mO2c7b58/9/QT" +
                "zP/3dR3M+K95f7+qmzXrvvK/6odSQebV9JcZvlexeqvtAq1mvF" +
                "2IyPdrdO7fZT9e15Xp+/kymPuH2NlLyfRjqLd95XSX9H+Pj/vy" +
                "N0sk7xE+Wuk69n1+nwX4ttnaLzPbynPd/F53TnO9tP4w8ssuvu" +
                "QsnX3dRdet1tKvm629Thfnqk+HWKD7RfJ4rbrHydDpS8Tgfarx" +
                "PHu+0L/083/lhxz9Jjh/r5GuDQg47iq2X9vaXg+9PlcvfT2PWi" +
                "70+Gf6I/n/vG20p+ndmhXv2DRbZOJX+vrGi9+E58h0fGjOI70U" +
                "l4Ggg26mBRNmWBK8q+aSJrtYpWlVF4mNPWy+5ltafH80Gv6Esy" +
                "y36ASvt+y5Jy70++Xvf3p/h2fDvE5ItvRyfIpp8GYpszUM/x6I" +
                "TmZK/Mp98yC4zIQwZVy27RlfVyheVDh5IRHlmtPVknc/FcamVj" +
                "5pujk2zp4Tyc1T06wjWwwZvc5/E0+xERrSK7AKPNQCWjUI+Z5G" +
                "w9ZjvnHE3H6jsJhBs/Q89ThDHnwUvZsDSHu3unmd/X1bwhDzy6" +
                "I91b45Ddy1m1vbamfQ9Vt7o/1W8UeX/q8TXewt/fPVXw/Wk2nu" +
                "WRMSMe4YOtMRi0DV6fx8ZQrz2yTy8DlYyGBvwMcOX7bsAy215z" +
                "NBPPpFY2Zr4ZOlOGR8nHnnim/its1GkG2Eee5JhmjmeObEOdVZ" +
                "Ws8DCn1ZXaspq69zLAJbUls+41zM+rTjVXenmrSIudearyHx2s" +
                "LDuw3fesMRlP8siY0NDD5GNPPFn/HTbqNAPsdD9NglfypPtpsl" +
                "VMsjLSfXoZNkbdhxn13/RsPWbdDzLMurX8Pns8zzfd49P92zH1" +
                "PzpRlh0MDXbG3Gnevfp/9kNRZ893fp7zWEzEEzwyZsQjfLA1Bo" +
                "O2wevzhDGrIrsAY5ivY+n8N3gZur9snTbYaturzm+u87LKPXEM" +
                "PV5sXuVfni9OKA==");
            
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
            final int compressedBytes = 2098;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdW9+LVFUcHzMCKWINX/JBENuXehIkLJHmzr2XntR3X3rxof" +
                "ClP8FxascZDUotA1Oj2uj3ChEWw5JaZiH0lBG+FIos7ULgi5ux" +
                "Vvfc7/3O98f5znjvzp2r67nMme/Pz/dzzpw5c/bARhujjTXWol" +
                "QHG/Vk075oo7QThtUwnufJHI7r48hqJPscOvMaj3updRY4us+3" +
                "z2YymkyktE9tk/CAzC0YZ+mEIGXCtXG0j/KlBTEtHuTj1YPQji" +
                "AsaBDHkTXXTFsTrREzmOruFb8IHtQxjqwQLe2EYa6nNYhv15W4" +
                "Po6sRrLPgbPnoxrWfD9lh2fDs7Ua9K45CTXsyUay1AlByoTbuW" +
                "Xnyap+Fc6CEP14rIxae5UdQVi8NkfWY8602XA2kdI+tc3CAzK3" +
                "YBw9rb3SgzkkE26wZOH0uc4OqsJZEKIfj5VR8+shEh+thazHnG" +
                "lXw6uJlPap7So8IHMLxlk6IUiZcINbdp6uqqtwFoTos8BM1Px6" +
                "iMRHayHrMWfatfBaIqV9arsGD8jcgnGWTghSJtzgbztPV9VVOA" +
                "tC9FlgJmp+PUTio7WQ9Zh1PLT23to914L5anKGtfqN+g3qQQKt" +
                "c9v1ZJFerUM2x0Qvt0ocycLnoLMpT/p5JmqdJV2PGPq1LUSpBV" +
                "uCLcnsZz1IoHX+cz1ZpFfrkJ19llt4JrdKHPbpMw+P1JiaB9XB" +
                "TNTaX+l6xNCvbSFq/OY5iG/OJK8zzQ39E0h/5Ta/hfdXj8m12P" +
                "yayT9m7z83L/mrttkbbdU3vxzqveDbYvN71/ymL13MxvRmbg6D" +
                "5mm7nicv816ep+355qkAh0HztG1Fz9O2fPM09cLyGO3/C6Xu6t" +
                "oKbt0HxotP8xSP+MvaffBuzlNc8rkg2Bxsph4k0OK668kivVqH" +
                "bI6JXm6VOJKFz0FnU57080zUHHufreTnI0vmXAsXwoXkxJn26d" +
                "lzAZ6k0nNgQwvGWTohSJlwbRzto3xpQUyLB/l4dcfeiiAsXpsj" +
                "a66ZNh8mKxT61DYPT1LpWbChBeMsnRCkTLg2jvZRvrQgpsWDfL" +
                "y6Y29FEBavzZE1Vx2f7U9P9/+C2V/qvvpQtftTe9148ZunUGrs" +
                "K5X3VLXzVC777C+c63Ev7rnevZI123O2+nXwZb8fWQT6XQ8+iK" +
                "ZYzIAcF+UsgOxXBi8iYh6PgBdUxrqAzPOQOY6FcpCnHi1iYxTn" +
                "APhxL896is5p39RWqbcK3C2029WuJ5/9iOeM3fHu+jT2IIHmfP" +
                "VpsnAvzwMdotPPa5osEOXsHCf+HbOcDyoRLmnIgzA1D6rjbMAB" +
                "a2reyBAjnITZvCYx59qQffy1Uvfxh++vfZzN0+ulrtu3Kj6Pl1" +
                "wvmogmGn9Cn/xKpBJoTuYWjLN0QkAZsQFJ4qAHfJBFHtIQGSMI" +
                "kUfQGKg6sJfsABuZACfUEBljODrWlvcFrUeNvb2E+4L4WMX3Bc" +
                "equlcpd566a6udJ3t/GmWekrW3iH1jEeepsej07iPg43GWTghW" +
                "FCDblcE7nFtjsbXHruvbkMNg3ny0g3jlX0/hRJh8J7uP8fXUPr" +
                "H89RROVLue7Hr+emofL3i/clS+czu3ab+NUsTjfMNR/SjOys4F" +
                "6zDmGFGMc3BEvnM7t2m/jVLE43zDUf0ozsrOBesw5hiRn3NjDv" +
                "vGXH9/miOrjLN0HutHAbK5+8xhnYH709ywur4NOQzmzUc7iNfd" +
                "+71r/FTt/mTXu/fPBfHRUeap9XLh89PR8s8FAmlDAfYF7gvity" +
                "v+u2XM9aI3uNRdXxZq42I5rHKfCc163cdLm6fD2fsJlMpYT9Hh" +
                "+HgZrAqsp+NjXk9HfKkM1PhkOaxyz9PJ8uem9V7af8j3p1ZWpz" +
                "XN9q5bAxFOtD5L399pfWR4329eib8z895NXh8r24yxa94cxn9f" +
                "sju0PhAYn/J6rVMjzM0nrc+H7+PR+RJvtV4a8ZM8U3gFni/5e3" +
                "YoOoQ96qhFhxozZME4SycEKRNuY8bO01V1FbBANiH6LDATNb8e" +
                "joePVs4AIsgx63hvPX1f5rmg6nZn9sVa+xc1T/3b3uhCbQW38t" +
                "kPOo93n9Dn8faV5Z/HR+RY/J7un3zn8Vf+GPU8HjyzktdT+ewr" +
                "uvd9suL1tK6q9VRmG/VcULx1GvniupMF1+lv8p3buU37bZQiHu" +
                "cbjupHcVZ2LliHMceIfJzjbtzFHnX3hJvCTXG3+xRZnI1kyiMp" +
                "3OTeIYqwnLVW47k8T1Z1Eq+IltYenudHUCZqfj3wYjVgJWeAYn" +
                "gunx3/exeuD727gvAOtwfg11G2ddwtH9cCfy8ejA9ijzpq2JON" +
                "ZKkTgpQJt1Gz83RVXYWzIESfBWai5tdDJD5aC1mPWcePcx9Pvu" +
                "s3Kz4XlF6vmnNB8O8oHKe2Fz0X2PXKu/eder52X7T2Us6428tc" +
                "Xf27muDSij6P52R/YJn/tzDK/hSvXZ5vHC1vvbxx0dZoK/aoo4" +
                "Y92UiWOiFImXBtHO2jfGnhPK0IykQtOG1HEFa27k5rZM010y5H" +
                "lxMp7VPbZXhA5haMs3RCkDLh2jjaR/nSgpgWD/Lx6kHPjiCsbJ" +
                "56GllzlfHjPhdUvj99UW6cN0/9e7rghxU9T2NmT/PUOFA0t3jG" +
                "GOdpdblx0Y5oB/aogxb8Cja0YJylE4KUCdfG0T7KlxbO04qgTN" +
                "QceyuCsPwZkNWIYSrtinYlUtqntl3wgMwtGGfphCBlwrVxtI/y" +
                "pQUxLR7k49WDVXYEYWXraZVG1lwzbWe0M5HSPrXthAdkbsE4Sy" +
                "cEKRNufcnO01V1Fc6CEH0WmImaXw+R+GgtZD3mLOJ/e8pvsw==");
            
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
            final int compressedBytes = 1596;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrdWktsHEUQnWM+4oYiYSEkOPkSOeIO2unZTfgIcwApSBHOJZ" +
                "zIhxx9834cj+cEK1koUvjF2PIHlAAHCBaxOCCQInGHg+Wj5SQX" +
                "H/j4xPR0aqp6p2bc890ZZuWerur6vK6trulpr2V1Hlrh5T7beS" +
                "hpxcMWeaNjqE3t0L66vBbIU32qoevDJ1lK3ecmov7cV3W0dAbm" +
                "V3SWof0XKCV2rIKufJbSa/Ma2VEMpgdCo1+xSrgGr3tnc+k7aT" +
                "XcfwuegR+nwdlBi4+TN5HKVjs+TuKb8uI0OMdkDuuv90VEt1NE" +
                "nFKu5qfi4xQ/Vk6cTP0Zyx3rHMNW9RSlaOTQ0VFa9cRltIGa1F" +
                "NUTx/TMSCH4uQkUBMocZmXQFvRCNCZj1KW1f1ZyXfv+H/fd5+L" +
                "xrK7zce4+wPp/+Zju+7ff+8+YCS38lWH7neJo78w6+46K3kv7P" +
                "2aD1FvXauG76bRba1atbnKwmLv6HfKpzx7x8RKmhE5Zu+YIEQp" +
                "iorXVdwk5CBhijlp3bkX6bqb/zh53bX+qM+6k1hM1t38kjEG4/" +
                "p0VJzEtRrVp2tmcbpxMWN9eidHTfiqRvWpcCxx+eSK1M+7q2Xl" +
                "042XUufT1eKfd/3bQRs8ISBO/U+f3FeIl9h3gf4nff/7a232b/" +
                "XXmNHl7p8xep/7f+sjvDvM/P5Kwj/nvzX0v9RsbLY2CfVZ9m+o" +
                "v9H/mn0vCve2wq9w3rlU755L2cYM0F5K/R68VO66dsP3FjFMjW" +
                "2YbayMqyx/9lC/Uz7l2UMTK2lG5Jg9NEGIUhQVr6u4SchBwhRz" +
                "XB0Xs6nr+GyN9gWzZnV8sGu4zqZHLH2Y/VwlcR3crXjd3TU7V8" +
                "n8zYX51H7xyOr6vlXby3uzXPtk3b1sNfgqHj3Up9E4Zbb3gP1+" +
                "3851TreXOPo4ylt42rA+PSr+/e6oOh7ca1LH7RmzOPFyadadwe" +
                "4vRX0S93OsofvV6GTLJ2+hyfk0f77YfUFkn/BeSfm0nSOftntX" +
                "0utUlU/2ibLyyXMrqE8nys0nMRPtFfCUnqlav0j07DN2o5x155" +
                "yvdv9Utj/9vaXAOL1RcZxK9udeKSlO0xXHqWR/9k/j0R3XPLJi" +
                "xl/6uB/41VA7xRVrR9TONU6K56asyonacxOmGsiFnimu9mR7El" +
                "qggWpPilXkSAr7qIc9Ja2k0JYIzt2pru5F90pHkQM2R/UpeqrN" +
                "+YP5IFbERS1TPEhFTmi0dS2W4ynm21rmpHhuynxaLkYDz59gPC" +
                "uu/OcFNalPzxQrl/B+5zX5/c5bNNuP539vafa5iturKk728Uaf" +
                "Px0vOk4j+7Nvo726XxxS+5RhfTqVO59ONzqfTpebTyROzzf6eV" +
                "c4+mrquMiRUWIrw/85t8zyyf0o97o70+h1d8YsTubndP/PfcHi" +
                "vlmcPMN8ci44F6AFGihokYd9nUYLeh/tdjZ4Pd1r1AtFgRaj8v" +
                "oY5w+w0dlylkfnDBLV5JO9UvG6W2nmftxerThOq82sT97tauNk" +
                "9n/zwd/p86mo84KY3xfkyqfyfl/g3hxPPsVIbnmb9cqnce8z4+" +
                "IketXGSfTqHae4+uS8VW2ceH/MuluuVz51blUbJ95f/fOp6v14" +
                "x/B3rLxczHvmAbTiAOIkDiS96KoxKsfRaIGTUpZ5z2o0GZs40H" +
                "9nz2kADzDwuOX/Eehs43ClySfvXpPzaeGwqn2maPS5ryj83Dc2" +
                "n35s9HnBa0Xnkx/7Q2jFYVifDpGry3E0lY1KKcu8Z/CThC3eb5" +
                "QHGOJx09nG4Rrf827+cbX55G0Xm09Oz+mJfdX60Q16ipJ9/3kX" +
                "ckCOo9EC9MG2skQtyzHqRWlxo2BZ7Pcv0REdB84BvSv0Ojo5Gj" +
                "zvyGyBAsvyjsi1eew5e+If1fpaQU9Rsq84vuQeysmP4lBpyZEt" +
                "2Ajsh5Z0O3iXY0oXR5BCVIoXxYF+wD7MhcoAIiVNZ4toqX+KIK" +
                "R2nV3xSLW+VtBTlOwPbsrWl9xFOflRHKClnuSI4Lf9ihvYDy1R" +
                "y2hPctUoHUEKUXnrVE+3hVgUBkSPNkA7yCcyW0RL/VMEQLWn2l" +
                "OWpVp5yZ78OCedk+2pRRc5IMfRaEHvo11pjdPTvaK+zgGbHA4c" +
                "o96j/mSrfq9CPUctUzzE939dlX2j");
            
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
            final int compressedBytes = 2339;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVXF+IVFUYH5Qeik3Y3SJFd41Ytg0CQSSCFJlzZ6CglyB6SC" +
                "MCH6x98C3QfGh3dnX+gPSS4UuwTz5oGJpYiIZSKbVKfyQ2ljLK" +
                "t3wQwkKXie653/3u933nfLPeO3Pn6t7Lnvv9/X2/e+bMmXPv3N" +
                "lSSW5TH6BU/6yU2zb9fqngbXpff/GnXoRjMBAMlEozR/LCtWjd" +
                "b81P8qk3PddDz1yIjyfCvzNTo0rElx0yP2fy5fh4deo7JfJs82" +
                "xPr96pZb1f+7aDj6mRXyTSpQezn8rtYvtJr7cC+ul2wf10O99+" +
                "Kr9bfpdakEADnSzSa9vapB9NmBjNrRJHsvA5YIw5w3nKCH4OlG" +
                "1z3HrEkNdunuiE6OJ3Gk/l+d7G08x+Pp543szeAsbTfLrxNPtI" +
                "l59L//lSp602mR63sbbYdUHlQu4rgULmp8rqYucnvV7381NwJ7" +
                "jj62G7IdzvNOpWxr9gA0RQlj1ifuLfIDHRyuPhKKISRIqjiNou" +
                "ly2xcq1Y1cWzLa2fgI/bF7olYtIOwk9QaCNbO97Hwr0tLNbW5j" +
                "v5YmksOo4hGmZF8pieJ6taSVSMLcjQzefsebZWD5GIK2kcWZ5z" +
                "oi0Gi6EUtZFtMd5Hw33RrscTi7Ut8p18sTQaHUcRDbMieVTPk1" +
                "WtJCrGluZ5nudHUGaijWoRMJ6IFWocWfTCIkU4K/vnkuu7r7LO" +
                "IuV3+jUrN7+/31yCY8ExbFEHrfwT2NCCcZpOCCTP7EefRA6Oze" +
                "ylPNfnVgFEzlOLcH3A3o/g/PwekNWIoYyPs44mr8ilTp4OPe74" +
                "+frJ8ezN+bU+6jMoXyr1dat/m0jh53vz07xwzali10/9rldP1r" +
                "H1K3muM4vemj/mPGbPB+exRR00cxhsaME4TQfJHLZHQkNf9Poe" +
                "1vNkVb8KIHKeWgTGkebXQyR+thqye86xdiO4YT6GNkSPJNCsDC" +
                "3ZSJY6IaBMWBaDI8s88KJuJdIwFlmgR/IAq7VRdY03eJEJSKgh" +
                "MtTnDFCb+sNZ2f8mRu8956epP1POFyeV3F8zX79cTz0/nezz/P" +
                "RDUinzHWaz78GZn/rNZfaF/uAGp4vtp7T1mj93OZ4y5GX5vDPn" +
                "ehgb54rJydRPC/3pp+D1gsdTzvXKm8ubqQUJNNDJIr22rU360Y" +
                "SJ0THvl30cycLnwGNknvTzTNT8esTQr60huvid7tM1/3Hv081+" +
                "1P19uh7vJWa+T9dM+T3C7Ie93s8sv5Hn/cyi+4nYL99PB95M+T" +
                "6+GdzEFnXUgpvmKFmsRjLlkQTREEVYJro65bmyiqzKvWRBTDef" +
                "s+fZWj08H+JKvDgy50Oaty7YnYzcf0sreGvezhevuqO6g1qQQA" +
                "OdLNLr6hhNmOjlVokjWfgc3GzKk36eiVr5LbceMeS4EKchOtrO" +
                "6s5QiluQQAOdLNLr6hiNGZTJrRKH9RPz8EgX0+VBdTATtfo1tx" +
                "4x5LjNu50QpVYZrAyWStBG3+cMwh729LNgQwvGaTohkDz7Gvok" +
                "cmVw9lXKc6u6VcCCmBoP8vHqlr0WQVi8Nkd2zznW5ivzoRS1kW" +
                "0e9rDSk2BDC8ZpOiGQHPbTPOHy3LCf5nUf5UsLYmo8yMerW/Za" +
                "BGHx2hzZPedY210JZ25oI9tu2EHmFozTdEKQMuHqOK6P8qUFMT" +
                "Ue5OPVy9v0CMKK56dtLrLLNdZqlVooRW1kq8FeKrUeBRtaME7T" +
                "CUHKhKvjuD7KlxbE1HiQj1c/eFqPICxemyO7XEEz68y6cBURty" +
                "CB1lpjW7JIr6tDdrwmWcczuVXisKtW5uGRLqbLg+pgJmrNVW49" +
                "YujX1hBdfGfFmqzHq1tX8vopb/Zmu9lOLUigVcu2JYv0ujpkc0" +
                "z0cqvEkSx8Dm425Uk/z0TNsvfZSn4+smQu8dM/r9JYtXKu76rv" +
                "pbu+0+OyXAe3Nnr99NDK6aeDd9P1U+r33YAZCFZjCxJoVm7Uyc" +
                "K9PA8kawmiZ47AitiAhFaebY/WB7nkIQ2Rg9W1XTxPYhEX4EDs" +
                "JW/rtc9hYISViK2PKLVl7qtsfXDuqxzYlvm+ytZ8x1PleuW6+R" +
                "vasOciCTQrcwvGaTohoIzYgCRx0AM+yCIPaYiMEYTII+gcqDqw" +
                "l+wAG5kAJ9QQGWM4OmjVPdU94WwWtyCBBjpZpNe2tUk/GjMok1" +
                "slDpt1mYdHupguD6qDmai1htx6xNCvrSG6+J3WT/l+j1D01hrO" +
                "F6/xcDj2lqLxuGT/ZuP3rVmyeqMOPrQ5nwFLUuItjwJk5bvCb6" +
                "xP88gaZgmfz9R5cBty0HnbeZyfrY6lrls3VTeZv7AFCTQrhziJ" +
                "hXt5HkjWYlvMQGxAkjjosVbwcg9piIwRhMixiAtwIPaSN2AjE8" +
                "Qln4sotSizja1pN+LfGZm21WeOGPY7EeP8ZoR0QtCiAFkdLW3w" +
                "LjOeotzW43pd34YcdN7ReGJn24mX8knpPIfRSO5gNYbSzE9pn8" +
                "NQ3/NrMq83Uz+H0Xqiz88XxPNTMBQM2fkpt+9nh7T5qY/fB6uv" +
                "cve/v6tuqW6hFiS7B8PBcHWLfS4aLdzL80CCbI6J3pj3sI8jWf" +
                "gcKKa1VuZJP89Eza9nW+gnv7aG6Gi3qrdCKWoj2y3YQTbHyYJx" +
                "mk4IUiZcc1zPc6u6VcAC2YTos8BM1Px6eD78bEnj1eQ5Y0Sn6x" +
                "bzSp7XLYRWzHWwXs+/bpn5vcu5Nb5zF4wH4+78ZC3LzgnjWpRu" +
                "zbbJ9VOq+Wl8+fkpOx85nhrrkyvJKyv6+YIr+V4HL3O/4Fqe/d" +
                "TYWHA/Xcv7fdexn666/dR6Oks/3effc17t7/yE/RSMBCPee37k" +
                "HnPCiBalW/u9peOay/vu8oqeny7nfJ/uUOUQtqjbPZgIJiqH7D" +
                "oTLdZGMuWRFEzYI0QRlrWGr9+EnierWolXREvrKZ7nR1Aman49" +
                "28LnHbGSPYAIHJ8inHXB+n68C6bfbj3T07rgTNaM+kt5n0Mxv6" +
                "NuPV/w9y0p/29Ir/N4vKr9Ja/x1BtS9mw9o3sW5qK5iC3qqIXH" +
                "BbJgnKYTgpQZ7oKe51Z1q8SWBZ7nR1Bmoi1oEWaBztnvAUSQ58" +
                "zOZw5bM4fjycyBbtidCOPclSCdEMhKR0DS82QF5XX0WPg8/Kqd" +
                "eBMTlEzaOy3/A15u73c=");
            
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
            final int compressedBytes = 2563;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVW09sHGcVX6IcWqGUCBmO9YFKgVSKBBd6Qep8M0uj8OfUNg" +
                "iVhj8hEl0uXKgQrZi117vZjIEKkHqIUNoDh17KoaiAOIQiQSuQ" +
                "SGpHbVTZrdtcEGo4oPpAFJnON2/evj/fm/GMd72tZ5W37/f+/N" +
                "5vPs/OzE7sTge26HX53mFxHtP5sLptxufqWcMqrsruhWidcqxo" +
                "prm72l1FixhRd9X9hiIekU995EE1VBGXj3Y6vFdOkVN5liLIqf" +
                "u5et5tzcP9Ia2kizNzPYT0Gvbv7uzLlt3fmes2Wpgt3/Bz/ZfK" +
                "Ffpt/u/3tE7RHZO1u1zWflat6R+Y/0r5/s/+P8Ip/T9Np7L/Qm" +
                "32r8Zn5g6z8o8T7+V2Csb3Va7TvXqdxp8/QOt072zXKWC6NJn0" +
                "0eDYu0/i5V7nQ7uF6qfbumk3JQseoOi4txTpptmXKUsWPOjmnC" +
                "sPU07yrDwoM5yHs+puzPAKnqNuUB+q5VwWM2eUKHkmeabTAes3" +
                "7wGKFiGGEayzMDFIn3htHp2jfhlBTksH5fh0r96qIC4+mzNrrV" +
                "hRdX5in8XLFZ/RuZ2fhl9ofX6Kmp2f7Dqzt2Kdsq8c5HUa3Zrt" +
                "edw95B4iCx6g7mPeUkRmNYZuzpl9nXI2j1QRatDd1CfzvBORVx" +
                "+q5VwWM2fU/J1OfBhtfHj4AHTHhymKG/cl5rVhFTBbPyWfszOc" +
                "uXpuGEMN1br53lbpqjimbqN1t3Gd3G2KyjoL89qwCpjtyTinTl" +
                "v13DCGGqp1872t0mXcFzzefZwseIAAU0RmNcZq4sRsebZ7NOSR" +
                "KkINvEb2yTzvRDRa0PNIYTjbYpQoWUvW8itfYYtr4Bq8wOcRrL" +
                "MwMUifeLNv2X16qp7CVRBjqAI7EY0W7Ari4rM5s95nrJjPfYF7" +
                "Zb7fW+x54fVusNVMQXItuYYWMaLkmnueIlhnYWKQPvG65+0+PV" +
                "VPgQh0E2OoAjsRhfNwf/jeyhVABrnPgOLteFtcDQoMMbIUQ9/b" +
                "5Z5/53HOYV69tpGfd/IetGGFZOaqLA3ZtzUfz1apsyO+293j7s" +
                "lXv7TgAQJMEZn1drkXVmMHdfKo5GGfEpbhlZpT66A52Iko+46e" +
                "RwrD2RajREkvyb/1gy2OsR688klnIYYRrLMwMUifeG0enaN+GU" +
                "FOSwfl+PTRLbuCuPhszqy1AnIn3cl8xUoLnn/Fx+JjHlPEx9Cj" +
                "DsTej48VR+kxikAXRXk31JQ/U5bhE6lG9sk870QUzoMs1oMqzS" +
                "yVc5ScS87lK1bYYu3OwQt8HsE6CxOD9Ik3q+jTU/UUroIYQxXY" +
                "iSi/LzAriIvP5sx6n3V9eb18arLO4/zfZqvviuOa3OY09wXtu+" +
                "2OaVQ0v38aH9r7/VP03pyf+7436+e+lc/Hr3x4nqvsYZ2uNFun" +
                "8ddmfzxNs07ZY/Ndp/P/a7ZOwzN7PBN8KvTa937w26y1dAfdAV" +
                "nwAHnfbVBEZjWGbs6J2VL3RsgjVYQadDf1yTzvRBTOw/0hfXoF" +
                "JKNE7qg7mrOWFjxAgCkis97m9+NBNXZQZ3lf8P2Qh/30WYZP1J" +
                "xaB83BTkRZT88jheFsi1GieCPO1xhscT+4AS/weQTr6JV/vxMZ" +
                "7CGf8b5o8Uy+R7GMnMJVEKOuoM4JetGuIC4+mzPrfcaK7rvic/" +
                "iuxxAjSzGdo27OIzklO+/Xc2WdjFlV1fOSrlTL96DFWYl1p38R" +
                "V4S706t57DXvp9cL+0b+70Z+BX0UKtL/FtfTRwr/Vvr/yf/Af4" +
                "OxfFxPzH5Q1G+lb6fvFN6/1JXoUJ89qe7f1f9E/5Ppn9OXZFX6" +
                "arrO0OSuMf138P8t5e9hjIvrWfpy+vfcXml9nf1IYY/0P+YSl+" +
                "SfwNKCB6jwn6OIR5QlK6vz9+dYJJFR3g015TmCZcTEsgY5ZT+f" +
                "g50TFMxjCieqNLNSztGiW8y90oIHCDBFZFZjrMYO6iyPpx+GPG" +
                "ydWIZP1JxaB83BTkSjBT2PFIazLUaJBl8duMEXB/dj1+DkFPcY" +
                "R6sygy9V55psg7g2+0AbLXusu7N7J1nwAGU/9pYiPKsxMhAHdf" +
                "JJYZ/MSQ0U4TqtCupEdGHbriCucAVIU4jcaXc6P7JKCx6g7Alv" +
                "KSKzGkN3eSSf5p08KnnYsc8yvFJzah00BzsRXdjW80hhONtiVO" +
                "iEO5F7pQUPUPSItxSRWY2hu5x8gnfyqORhWlmGV2pOrYPmYCci" +
                "rz5UK/WFzFK5QKfcqdwrLXiAos94SxGZ1Ri6y8mneCePSh6mlW" +
                "V4pebUOmgOdiLy6kO1Ul/ILJVL/vI5xA35jp63FJO+8TSjsrKO" +
                "I7pRz4oqpLZQcTivfupus61MtCHfeZzHdN5maZPxuXrWsIqrsn" +
                "shWqccK5ppHn+38vef+gf5Od2sf/9psnab8p3HeSzabMLSJuNz" +
                "0WYThVTFVdm9EK1TjhVNNVceT2N9PK08fXCOpwuHZvs8M1qKls" +
                "iCBwgwRWTW2+VeWE2cWF2u+k9DHqki1MBrZJ/M805E2aqeRwrD" +
                "2RajRMml5FKnA7Z4HnEJXuDzCNbRa7knM9hDPvFmP7d4Jk9BWE" +
                "ZO4SqIUVdQJ6LsZ3YFcfHZnFnvs66Hbemb+/O8Ontqvs/HZ/33" +
                "LcV91E207ubwMsY8Hp+HHK+zMDFYVcBsT4ZsvTZ3c/msPTeMoQ" +
                "Zb99Kzcm+rdO2+DS83r23zdxvZL+d7PGW/2F/+/fm7sqXvZb86" +
                "2J87t+W20CL2r/hIfMRtjc9TBOssTAzSJ17PZvXJqdQvI1ynVU" +
                "GdiMJ53vrPHd9bi5nrYWjdredeYYvYOrzA5xGsszAxSJ94s6ft" +
                "Pj1VT+EqiDFUgZ2IRgt2BXHx2ZxZ77Oul5+7eDFe9OdxnosX64" +
                "9NyOsqO9puk+fxJps9D46nvemp/D2Mq/p+fPTrNvfjgyeq7scH" +
                "P9r/+3FSX38/PrrY8Pr2znBteHX4Fq7T8JrB3uAa6NcpKq5/w7" +
                "eNKa9NdxYdrtdmjW9o0S7X4uGbhX1jz+f1M6E3g6vFmXn3z1L9" +
                "rI8nd/aDOZ7GS8Y6nZ3t8VS9TtGn265TyViu08rDVeu08uD+rx" +
                "Op35/P3cranp5e/aQ+Px5OczTtxr7yavuetltyPbmOFjEitBQj" +
                "X+LoSd5DPvHaPDpH/TwSPcl1WhXUiQgUhRW0z+EKUA3vpXq3g9" +
                "bt4O/7uh2P8+/BO+wzv6POATvS45ZXAbN5tt2BbM35uOhV34N3" +
                "7DquwdZdfA9me1uly7gfW41X3X/A5l2FB8j7PIJ1FiYG9JEbmC" +
                "QPZiAHXVYWmbGCGMN6jKGSULevI32gCXUhM9RwBYiqvwcnf+sc" +
                "4G2/1cfl/Wm8EC/I7y3xxWadFdmFaRU10Hxxt3lLz8YX96bCHX" +
                "fHyYIHCDBFZNbb5V5YTZxYXX4P/l3II1WEGniN7JN53okoe0HP" +
                "I4XhbItR8b8PO1FKHA==");
            
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
            final int compressedBytes = 1412;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWz1vFVcQ3Tp9lCLIcodsCVeQDiTvh/IDQK4MkuUgOaKlRj" +
                "yDDA+q1FHk5BdEUZQvGSVKAcYBRYqUIkmT/IK0KZPdOzucmbmz" +
                "Zm32vbyFuyvfNx9nzpwd3d33/AxZNvkxC8fk8/rnm8lS1h7rv7" +
                "A1+SFzj8m3wn7avv48eeYgD7JXOiZfHpt9HMegXiG/e2EdnkxB" +
                "fj4/j5Us8shHRGeb9c6NGA1ORlPs4dcxj1YRa5AYXafzspK9h1" +
                "/ZflAY9/YYLX/nfjo76v10dtj9VG6UG7yy35zFSrFSbkzvI9LE" +
                "YKMOVrHSvBIKXE00y2StrNNdG0t25MidD2RdjEAle3G/Zt39jL" +
                "uRKj0BZpD87OWX88v1zmpXspqzWC6WGx+RJsYWKthv7GI5TGQZ" +
                "EapCVFYTpt37IiM7AqPrdF5Wshf3oyzjSZVl1so1f9d9V10a83" +
                "0H9QPdd9fKa/m/tNaTCxZ5jS0jjPN8MLDN3MSkeThDOapCBh4z" +
                "MwKMEoFrQHdSr9URNyshTewxM2Mke+ttl9u1FdYQ26aztp5SjC" +
                "OM83wwaBu8Po/NoV5HmNPTgZzs3qj3EOCSvSWz1cqIrvuu/GnM" +
                "9x3UD3Pfdc9JIHrOKb++OHNqtPSZ092/5jmn9duz3E81+8k/P9" +
                "3uM6e7//TVUOwUO7yyH87V+txRkSa2I0/kWms1vK4yG1cFe9Wv" +
                "M11Vlj2t00PYnNePmKRWOwHdDQqDtVfs1VZYQ2yPTrJlhHGev3" +
                "5L1sAGr88T52yXxlu/xZxdCFpld1IUI7ib2CmG2WplROdz/HDU" +
                "z/HDeT3Hq3zUnzPzxX2/O25OxcZ85+T3m8WcyqNR33dHQ8/JMG" +
                "FOz7IRH8Orn899N+/91IFc+OfT+OfUdd+lI+2ntJ/mtrvSnPz3" +
                "z3PlOV7ZZ49XxGBrHwzaBq/PY3Oo1xGp00Og0na3CHDFE9Dd5B" +
                "zqc61cq62whtganWTLCOM8HwzaBq/PY3Oo1xHm9HQg53W3CHDJ" +
                "3pLZ1jIiPcdP8343/T7Nqdf3dl+cPNMv/2rdT1s1hCrv2H2Pre" +
                "mT9C53zPw/PXmmX354RRbRR8FQKsV+Okq7Jn3OTHOa0efxpXIp" +
                "9imGFTGbQz14LKdmJy6/r+aNeXQ32LGGWKfO+ur8SFBypjyjMs" +
                "GnGFbEbA714LGcmp24/L6aN+bR3WDHGmKdOuur8yPe9Ux/TfdY" +
                "n2P6+2tyHb/1xP1xys8Ff6e90mu+f6YZ9Pk9OH1fkOY06Pfj++" +
                "kO6z6q/WqfLfrxEbwyUr76NchW+8f1fxmjVRgrYV8i/auRquyV" +
                "MZfW/gL7qHrUrPjpQiDPKA+NGmS7cX0YwcQ6rBKoB9K/GqlKXx" +
                "m4tPY6crW6ipUs8shHRGetz2hwclZGNY9WEWuw1ajTeVlpu9vr" +
                "k/piZq+WEf2f4/c+ef2e4/c+Hv797k2eU3mlvMIr++zxihhs7Y" +
                "NB2+D1eWwO9ToidXoIVNruFgGueAK6m5xDfW6Wm7UV1hDbpJNs" +
                "GWGc54NB2+D1eWwO9TrCnJ4O5LzuFgEu2Vsy29rW2yq3aiusIb" +
                "ZFJ9kywjjPB4O2wevz2BzqdYQ5PR3Ied0tAlyyt2S2teRVF6oL" +
                "9RO9Xckij3xEdNb6jOYKVMqo5hHvOSIjkZbT6kAfrrTd7fVJfT" +
                "GzV8uIN/v3lvn+v42Xz+nBW2OfU/o7Qvq+IO2nRd5PD97+f/ZT" +
                "fnCK/3930G8/3f9oBnN6J913s73vdj8c/Td0z6vnWMkij3xEZN" +
                "b6zAAOVMpOcZ3OaQ2ISJ0eApW2u0WAK56AvHLrRf+e7t1FfL/b" +
                "u7hof0dIc+qp6P30CcB9Pt2sbmIlizzyEdFZ6zManJyVUc2jVc" +
                "QabDXqdF5W2u72+qS+mNmrbRH/AQSpVyk=");
            
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
            final int compressedBytes = 1386;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW71uFGcU3YoyUh4AeAAoqFx7mZmHSIVCwxukjLQxjoSLiA" +
                "ZQIsVo8wIpKAKKUJQiIVKkCCjcJbHgDUxBy+7cvZx7znfXjO31" +
                "CuxvVnv3/p1zz3zMfDteidGIj8nDUT2S4/qF6xdgzbPIYmS4qr" +
                "F3g9OrMcs8rKLUoGjguB6ROl3PL+ormTOs9tfr6fBj8vvi8+fZ" +
                "+5fJpaTjtyXIx8H/a/H5z+TvpPPXE2p8dGj1j8E8T957z+zz9v" +
                "4xFV2qV052tHvtnluPPXKLHHyOwcA+eHMerQHPmagz6wBSp2sH" +
                "uMoV4GlxHdq95nnzPK6bxZaDRU5rjscn/PLwfuB4LvNGLdylil" +
                "hbrpOrubo8Y+jh+9P2g7O3P23fW/0+fvvmGdzHv6zfd0PW6TSe" +
                "C87zfTeejCew5llkMTJc1di7wenVmGUeVlFqUDRwXI9Ina7nF/" +
                "WVzBnWona33Z198/W2/w7ctZf5MeN9WQwG9sGb82gNeM44Z6YD" +
                "tWy6doArzo7MitX++pxZ9/FV7OPCdLdeOfV6Ov711Lxt3pax5W" +
                "CRc99rwINHOZnd8dwFRvSVPDwN71KDnkM8g+VrkWfm6O5N92Y0" +
                "Mjs/5p5HbpGDzzEY2AdvzqM14DnjnJkO1LLp2gGuODsyK9aidr" +
                "PdnH3z9bb/Dty0l/kx431ZDAb2wZvzaA14zjhnpgO1bLp2gCvO" +
                "jsyKXUTjdjzzetvnxvYyP2a8L4vBwD54cx6tAc8Z58x0oJZN1w" +
                "5wxdmRWbEWdQfdwezK6m1/jR3Yy/yY8b4sBgP74M15tAY8Z5wz" +
                "04FaNl07wBVnR2bFav9i53pUeh/7sR6l9bmgrtMKn59uNDfceu" +
                "yRW+TgcwwG9sGb85Q1nRJVgLHs51o2z7XFs82YFbuItpqtmdfb" +
                "PrdlL/NjxvuyGAzsgzfnKWs6JaoAY9nvk3W6doArzo7MivWO9d" +
                "x3zRfrve/yeSv8O7j+rlL38fp9d+rr1L3oXrj12CO3yMHnGAzs" +
                "gzfn0RrwnIk6sw4gdbp2gKtcAZ4W1yH21/2p3nd1f1rXOrVX26" +
                "tuPfbILXLwOQYD++DNebQGPGeizqwDSJ2uHeAqV4CnxXWI/XZ8" +
                "s+Hezld1TwrPrS+bl2VsOVjktOZ4fJaczO4d2VzmjVpULStibb" +
                "lOrubq8kx2Ps10Kcv0A1Omp/QvOR3aMUTBqlQ2Px29Mqy+ekXa" +
                "MUTBqlTW56e6Tic52svt5TK2HCxyWgMePMrJ7MaVz2XekoenwS" +
                "81lDq5mqvLM72Si+1FqvSx5WCR0xrw4FFOZjeufC7zljw8DX6p" +
                "odTJ1VxdnpmjJ6/kvvvviPfp6xPc4/8eGfH/x3If7nxd96K6Ti" +
                "tcp0ldg0Hr9G1dg5P+rnK+/z+CXE/f1Stn+dFNu6l79s473Hpn" +
                "/MwxqHbTw+Z/iFEVlko8jp352URVembOxdrr3y3H3sfv1TUYtE" +
                "736xocsj887Z7OLd7LOlD3rqwbGFSX9w1hBJPrUCVQj878bKIq" +
                "PjNwsfbZ33i32ltuPfbILXLwOQYD++DNebQGPGeizqwDSJ2uHe" +
                "AqV4CnxXWYfw5/ftr5/uw9Pw3+/Wm/3XfrsUdukYPPMRjYB2/O" +
                "ozXgORN1Zh1A6nTtAFe5AjwtrkO7v/MDr9v2s/Xtitt/fkI7+J" +
                "XuCqx5FlmMDFc19m5wejVmmYdVlBoUDRzXI1Kn6/lFfSVzhrVI" +
                "f6e78/n7J4QfT/t3ujuffTq/03XXumuw5llkMTJc1di7wenVmG" +
                "UeVlFqUDRwXI9Ina7nF/WVzBnWouXX08B/4fNyPW10G7DmWWQx" +
                "MlzV2LvB6dWYZR5WUWpQNHBcj0idrucX9ZXMGXbR8Q53EQ4H");
            
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
            final int compressedBytes = 1078;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWr1OFFEU3g4SfQIK3oFH2J2Z+BBASUdHQi0JYUUqW9p9AA" +
                "sLNRSEwmhiYgFUWqy9LYmFFjJ7OX7nO/fs7l3cBdFzCWfO73e+" +
                "ObmzM7PQ6fA6fNyJ5a69s5vjy+vf13urTsbpmMo3Sv9wc/y099" +
                "HJPPlDjq8mRt8V47z9rb2POS1mTgZpNa6w2E+xn/6m/dT//O/t" +
                "p/2vsZ8WuQ6exAy81ew2u5BJS1ay4eGotSUbmBLVXsZhFjkHW4" +
                "06jutK292en+aXI3u1Nj+uu0mr3qq3RIotlkj4oLMNBNaB6+PY" +
                "GOrZo3l6Gai03W0GsPIJcDc9h/YYz0/xnLm458zm28P77Hi6cn" +
                "e9ul/4qP3aZ+M+yiyRNjYZNc/SrPza5J3EXDJux/ngNO5t8Twe" +
                "c4o5xfdPD+m5oN6sN0WKLZZI+KCzDQTWgevj2Bjq2aN5ehmotN" +
                "1tBrDyCXA3PYd6s9qutjudJNvVamKJhA8620BgHbg+Th6zXTQL" +
                "IOb5HPP6CTd9th6yrb2x+lX/WhvJka+ffpKuPZLn2UBgHbg+Th" +
                "6zXTQLIOb50tl2txnA0r01sq2VjPh8is/x+c2pOW/ORYotlkj4" +
                "oLMNBNaB6+PYGOrZo3l6Gai03W0GsPIJcDc9h/YY+ymuu+lzKv" +
                "97y/88p/3v8d4S7y338flUbVQbIsUWSyR80NkGAuvA9XHymO2i" +
                "WQAxz+eY10+46bP1kG2tZMR+iusu7nd3f7+L667o+6flejm3kw" +
                "8SPhtDPXAsJqMnLL8v4+Y43A16ziHnyVGfne/xzqc6HntnPJ5y" +
                "5zxezHPLdFzJKGFwW5bVVXWV28kHCZ/oEkM9cCwmo0s9ZwEReT" +
                "kOd8NvzsGegz6D8bPwPSMml9UlRUZ28kHCZ2NSjyN0h8ml4Pt9" +
                "GVdz4SzLiLn5PDnqs/M9IyYX1QVFRnbyQcJnY1KPI3SHyYXg+3" +
                "0ZV3PhLMuIufk8Oeqz8z3e+VSDsSiDKV0GC/p8GpRmlDCYF8vn" +
                "P+KNt2hOP2MG8Zw5v/9/OhzGzilZ8X/R8X3mPNfRUsygaE6PYg" +
                "buO/J6vS5SbLFEwgedbSCwDlwfx8ZQzx7N08tApe1uM4CVT4C7" +
                "6TnU681as9bpiExaspIND0etLdlSgUrtZRxw1RGdaTEtD/SRSt" +
                "vdnp/mlyN7tZIR3/vGc+b85tQ7652JFFsskfBBZxsIrAPXx7Ex" +
                "1LNH8/QyUGm72wxg5RPgbnoO7bF8Px2t3M9+6p3Mvp96J2X76d" +
                "mLeM6M9+D7eA+OOcV78PxWs9PsQCYtWcmGh6PWlmxgSlR7GYdZ" +
                "5BxsNeo4rittd3t+ml+O7NUmq7fUu37zFZm0ZCUbHo5aW7KlAp" +
                "Xayzjq7qQiOtNiWh7oI5W2uz0/zS9H9molI54zZ1ndIR+1X/u6" +
                "wxKUWSJtrDssYYgszcqvTd5JzCWjkPMv0x6yTg==");
            
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
            final int compressedBytes = 1501;
            final int uncompressedBytes = 19241;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW81qXlUUzRMITjvwCdpJ36C9vfEVnKQ0gQyaUAqBTrWBRI" +
                "k+ggiFPoADBypCxIEoCEIGmTiJ6NB5nHpz9l1Z+2d96W0NtpVz" +
                "L9+5e+299trrHL58bb7StTW77vwWn2su73O5XtkvW7moXa1aWd" +
                "6V7rXsVc7BWOb57tndM6zAQFiZYxwxFWJMXa2Ta+yPGe9TMdiZ" +
                "p2cGteoJxGn+HDzfrv331vq14OrntPJkfpifX06vr9U57X9vz0" +
                "++SPlvXPzz/Px1/xeh8N2/9PjVldUfF+t8exn9NO/p8+s/p5L/" +
                "H5zTq153jl9P73VfS70s99zfT/2c+jn1c+rn9Cae08Hv/ZxefE" +
                "4Hfy/tHO4P97ECA2FljnHEVIgxdbVOreUp3gUVKz/W1Dx487tV" +
                "yrk38/vvLf3nrn+O98/xN+xz/OHwECswEFbmGEdMhRhTV+vUWp" +
                "7iXVCx8mNNzYM3v1ulnHtntDFsTFFbW27Dbot9BjyFqRBj6mqd" +
                "WstTvAsqVn6sqXnw5nerlHPvjM6H8/D+athyXJlDjBr7qZM1oz" +
                "r6I4uK5FWdOI2v6iHvwe9g5c/Wuc40JyfDSag0bDmuzOUa+vlk" +
                "LJycQF/PjbreS2RlR9Gb9hmr2p3ONCenw2moNGw5rszlGvr5ZC" +
                "ycnEJfz4263ktkZUfRm/YZq9qdzqj9fPph/xvlkquf07Lrs7Gf" +
                "waJzWu9nsOT6+P1+BvJv8n9EfPTu5Tvrg0X9f7767KN3Xtrt2e" +
                "s6p/HmeJOrRYYMMxOrGYNNTVR9NupEF9VD7mZfrPvOPD3vz/ur" +
                "yqp3RrfH21M0rxYZMsxMrGYMNjrY6bNRx3l1Fc/MmtkH56AzT8" +
                "/78/6qsuoFo3//1L9X6efUv6fr76e3+f00PhmfcLXIkGFmYjVj" +
                "sKmJqs9Gneiiesjd7It135mn5/15f1VZ9YLR308L31F/RXSBLc" +
                "eVuVxjt9eJmlHd9+e5kRdzimXPpzdWzYs+V7OWdF91am/H9fTG" +
                "f/IbXv/zbsE1PBgeYAUGwsoc44ipEGPqap1ay1O8CypWfqypef" +
                "Dmd6uUc++MdofdKWpry+3abbHPgKcwFWJMXa1Ta3mKd0HFyo81" +
                "NQ/e/G6Vcu6d0fawPUVtbbltuy32GfAUpkKMqat1ai1P8S6oWP" +
                "mxpubBm9+tUs69MzocDqeorS13aLfFPgOewlSIMXW1Tq3lKd4F" +
                "FSsfk/P0zKCWn+2Vc++MdoadKWpry+3YbbHPgKcwFWJMXa1Ta3" +
                "mKd0HFyo81NQ/e/G6Vcu6d0dawNUVtbbktuy32GfAUpkKMqat1" +
                "ai1P8S6oWPmxpubBm9+tUs69MzoYDqaorS13YLfFPgOewlSIMX" +
                "W1Tq3lKd4FFSsfk/P0zKCWn+2Vc2/mz9+K93+/W/bvUh/1M7ji" +
                "95hn4zNE9tIMrGD6p+5hdTVviWJ2WJ0Ae6bejXeVdwat6P2S+3" +
                "x8jshemoEVTP/UPayu5i1RzA6rE2DP1LvxrvLOoBW9X3KPx+OL" +
                "la9VDNbBGlf+j6MLNqvjFf8z6cWKVIKP7ITuydS78a7izqgVvU" +
                "+ZW+MtrhYZMsxMrGYMNjVR9dmoE11UD7mbfbHuO/P0vD/vryqr" +
                "XjD69wX9e5XrO6dxb9zjapEhw8zEasZgUxNVn4060UX1kLvZF+" +
                "u+M0/P+/P+qrLqNXRv/d762pqtF9dFBISVOcYRUyHG1NU6ucb+" +
                "mIGm8sGamp4Z1PKzvXLuBaP/3C36PvPR8AgrMBBW5hhHTIUYU1" +
                "fr1Fqe4l1QsfJjTc2DN79bpZx7Z7Q5bE5RW1tu026LfQY8hakQ" +
                "Y+pqnVrLU7wLKlZ+rKl58OZ3q5Rz74z2humzytaW27PbYp8BT2" +
                "EqxJi6WqfW8hTvgoqVH2tqHrz53Srl3Dujo+Foitrackd2W+wz" +
                "4ClMhRhTV+vUWp7iXVCx8jE5T88MavnZXjn3gtE/xxd9jj8eHm" +
                "MFBsLKHOOIqRBj6mqdWstTvAsqVn6sqXnw5nerlHPvzPgHo4B6" +
                "Bw==");
            
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
            final int compressedBytes = 863;
            final int uncompressedBytes = 10657;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtV0tqG0EQ1Ul8BF+imZNYWDsjIV9gdobcwvgjyCKLLJKQhX" +
                "BISCCQM2U0radXnzdiSLzwomeYmqquV69eNS2bKXflbrEooz1c" +
                "Bw8RLNfo+5gM3iev5sm52MWqIGPGo3PsHhHksr0tc6wFon+p+P" +
                "7D8HzqLxbp6vcLefWfjf/r+P7T/xbIr4v/uvqPZ7M/ZvN8OXk/" +
                "/0VHd9/dw6uPRsACad+6htlp3BzGqDArQWyRehqrKk4GLq89X+" +
                "/eL9o152RetD04c+733f5g+UwhmAeq259jZXYaN4eRTNARlVA9" +
                "kXoaq8pPRi6vfVi57C5pq1ejGnPFZ2MMNDmRtauex6vIGmI163" +
                "zeVsbucT6rLzOrWiDa/7uZtW2f2j692j6Vm3IDixgRLNfo+5gM" +
                "3iev5sm52MWqIGPG+5zqB212WsUca4Fo56n97to+tX16y9/B5v" +
                "vupX2dzNqnb20P2nfwK56n720P2t/x9v+u7dMb+b67LtewiBHB" +
                "co2+j8ngffJqnpyLXawKMma8z6l+0GanVcyxFoh2nmadp6tyBY" +
                "sYESzX6PuYDN4nr+bJudjFqiBjxvuc6gdtdlrFHGuP0bZsB2+0" +
                "49q23tW3K8CpmAzeJ6/mybnYxaogY8b7nOoHbXZaxRxrj9GqrA" +
                "ZvtOPaqt7VtyvAqZgM3iev5sm52MWqIGPG+5zqB212WsUca4/R" +
                "uqwHb7Tj2rre1bcrwKmYDN4nr+bJudjFqiBjxvuc6gdtdlrFHG" +
                "uP0bIsB2+049qy3tW3K8CpmAzeJ6/mybnYxaogY8b7nOoHbXZa" +
                "xRxrLb576p7g1SdeFQELpH3rGmancXMYo8KsBLFF6mmsqjgZuL" +
                "z2E/ahe4BXH42ABdK+dQ2z07g5jFFhVoLYIvU0VlWcDFxe+3Cm" +
                "bsstLGJEsFyj72MyeJ+8mifnYhergowZ73OqH7TZaRVzrD1Gm7" +
                "IZvNGOa5t6V9+uAKdiMnifvJon52IXq4KMGe9zqh+02WkVc6y1" +
                "+G7X7eDVJ53VXc0wbysmfk078p7DzWGMCrMSxBapp7Gq4mTg8t" +
                "pP2MfuEV59NAIWSPvWNcxO4+YwRoVZCWKL1NNYVXEycHntJ+xz" +
                "9wyvPhoBC6R96xpmp3FzGKPCrASxRepprKo4Gbi89uH6C+2Lva" +
                "M=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 9, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 14, 0, 15, 0, 16, 0, 0, 2, 17, 0, 0, 0, 0, 0, 0, 0, 18, 3, 0, 19, 0, 20, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 4, 0, 0, 22, 5, 0, 23, 24, 0, 25, 0, 0, 26, 0, 1, 0, 27, 0, 6, 28, 2, 0, 29, 0, 0, 0, 30, 31, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 32, 10, 33, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 1, 11, 0, 0, 0, 12, 13, 0, 0, 0, 2, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 14, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 15, 16, 0, 0, 0, 2, 0, 35, 0, 0, 0, 0, 17, 3, 3, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 37, 18, 0, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 38, 0, 19, 0, 4, 0, 0, 5, 1, 0, 0, 0, 39, 0, 20, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 2, 0, 7, 0, 0, 41, 4, 0, 42, 0, 0, 0, 43, 0, 0, 0, 44, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 8, 0, 0, 46, 7, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 47, 10, 0, 0, 0, 0, 0, 21, 22, 23, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 25, 26, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 30, 0, 0, 0, 4, 0, 0, 31, 0, 1, 32, 2, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 35, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 1, 4, 0, 38, 0, 1, 39, 0, 0, 0, 6, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0, 9, 42, 43, 0, 0, 44, 0, 5, 6, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46, 1, 0, 0, 0, 0, 0, 0, 0, 47, 2, 0, 0, 3, 0, 7, 48, 0, 0, 0, 1, 7, 0, 0, 8, 49, 0, 8, 50, 0, 0, 0, 0, 51, 0, 0, 0, 9, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 53, 54, 0, 55, 0, 56, 57, 58, 0, 59, 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 61, 62, 10, 0, 0, 0, 0, 11, 0, 0, 63, 0, 0, 0, 64, 12, 13, 0, 0, 0, 65, 66, 0, 0, 0, 4, 0, 0, 50, 5, 0, 0, 67, 1, 0, 0, 0, 14, 68, 0, 0, 0, 15, 0, 1, 0, 51, 0, 0, 0, 0, 0, 0, 52, 0, 0, 0, 6, 0, 3, 0, 0, 0, 0, 0, 12, 0, 0, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 19, 0, 0, 0, 1, 0, 0, 0, 11, 0, 69, 70, 12, 0, 53, 71, 0, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 72, 73, 0, 74, 75, 76, 77, 0, 1, 0, 2, 15, 16, 17, 18, 19, 20, 21, 78, 22, 54, 23, 24, 25, 26, 27, 28, 29, 30, 31, 0, 32, 0, 33, 34, 37, 0, 38, 39, 79, 40, 41, 42, 43, 80, 44, 45, 46, 47, 48, 49, 0, 0, 1, 0, 0, 0, 81, 0, 0, 0, 50, 0, 82, 83, 9, 0, 0, 2, 0, 84, 0, 0, 85, 1, 0, 86, 3, 0, 0, 0, 0, 0, 87, 2, 0, 0, 0, 0, 0, 0, 88, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 90, 91, 0, 3, 4, 0, 0, 0, 92, 1, 93, 0, 0, 0, 94, 95, 96, 0, 51, 97, 98, 99, 100, 0, 101, 55, 102, 1, 103, 0, 56, 104, 105, 106, 57, 52, 2, 53, 0, 0, 107, 0, 0, 0, 0, 108, 0, 109, 0, 110, 111, 5, 0, 0, 0, 0, 0, 0, 0, 10, 0, 1, 0, 0, 0, 112, 4, 0, 1, 113, 114, 0, 0, 3, 1, 0, 2, 115, 0, 6, 116, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 117, 118, 119, 0, 120, 0, 54, 3, 58, 0, 121, 7, 0, 0, 122, 123, 0, 0, 0, 0, 0, 5, 0, 1, 0, 2, 0, 0, 124, 0, 55, 125, 126, 127, 128, 59, 129, 0, 130, 131, 132, 133, 134, 135, 136, 137, 56, 0, 138, 139, 140, 141, 0, 0, 5, 0, 0, 0, 0, 0, 0, 57, 0, 142, 1, 2, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 13, 0, 0, 6, 143, 0, 144, 58, 0, 59, 0, 1, 1, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 60, 0, 0, 61, 1, 0, 2, 145, 146, 0, 0, 147, 148, 7, 0, 0, 0, 149, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 150, 1, 151, 0, 152, 0, 0, 4, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 11, 0, 0, 12, 0, 13, 153, 8, 0, 154, 155, 0, 14, 0, 0, 0, 15, 0, 156, 0, 0, 0, 0, 62, 0, 2, 0, 0, 0, 8, 0, 0, 6, 0, 0, 0, 0, 157, 158, 2, 0, 1, 0, 1, 0, 3, 159, 160, 0, 0, 0, 0, 0, 7, 0, 0, 0, 60, 0, 0, 0, 0, 0, 61, 0, 0, 161, 0, 0, 0, 9, 0, 0, 0, 162, 163, 164, 0, 10, 0, 165, 0, 11, 16, 0, 0, 2, 0, 166, 0, 4, 2, 167, 0, 0, 17, 168, 0, 0, 0, 18, 12, 0, 0, 0, 0, 63, 0, 0, 0, 0, 1, 0, 169, 2, 0, 3, 0, 0, 0, 13, 0, 170, 0, 0, 0, 0, 0, 171, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 172, 0, 173, 19, 0, 0, 0, 0, 4, 0, 5, 6, 0, 1, 0, 7, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 174, 2, 0, 175, 176, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 4, 0, 5, 0, 0, 0, 0, 0, 21, 0, 0, 0, 22, 0, 0, 177, 0, 178, 179, 0, 20, 0, 21, 0, 6, 0, 0, 0, 0, 0, 8, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 181, 22, 18, 0, 0, 0, 0, 0, 0, 182, 0, 0, 1, 0, 0, 19, 183, 0, 3, 0, 7, 10, 0, 1, 0, 0, 0, 1, 0, 184, 23, 0, 0, 0, 0, 24, 0, 0, 20, 11, 12, 0, 13, 0, 14, 0, 0, 0, 0, 0, 15, 0, 16, 0, 0, 0, 0, 0, 185, 0, 186, 0, 0, 0, 187, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 188, 0, 0, 189, 190, 0, 191, 21, 0, 0, 192, 0, 0, 22, 0, 0, 0, 62, 0, 26, 0, 193, 0, 0, 0, 0, 0, 0, 0, 194, 23, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 1, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 195, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 20, 21, 0, 22, 0, 0, 23, 24, 24, 25, 26, 0, 27, 28, 0, 29, 30, 31, 32, 0, 196, 0, 65, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 5, 0, 6, 0, 7, 3, 0, 0, 0, 197, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 1, 198, 199, 0, 1, 26, 0, 0, 0, 0, 3, 0, 0, 1, 200, 201, 13, 0, 0, 0, 0, 0, 0, 0, 0, 202, 67, 0, 0, 203, 0, 0, 204, 205, 0, 0, 0, 0, 0, 0, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 207, 0, 0, 0, 208, 68, 0, 209, 0, 0, 3, 0, 0, 0, 69, 0, 0, 65, 0, 0, 27, 28, 0, 0, 3, 0, 0, 29, 0, 0, 210, 0, 211, 0, 0, 66, 212, 0, 28, 213, 0, 214, 215, 0, 0, 29, 30, 0, 216, 217, 0, 31, 218, 0, 0, 219, 220, 221, 222, 30, 223, 32, 224, 225, 226, 33, 227, 0, 228, 229, 6, 230, 231, 31, 0, 232, 233, 0, 0, 0, 0, 0, 70, 0, 0, 0, 2, 234, 235, 0, 236, 34, 0, 0, 0, 237, 0, 238, 35, 0, 0, 36, 0, 0, 23, 0, 0, 0, 32, 33, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 4, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 239, 0, 240, 0, 1, 37, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 36, 0, 0, 241, 0, 0, 0, 242, 243, 0, 0, 0, 244, 0, 0, 0, 245, 1, 0, 0, 0, 5, 2, 0, 0, 37, 246, 0, 40, 0, 247, 0, 38, 248, 249, 39, 250, 0, 251, 0, 0, 0, 0, 0, 252, 40, 253, 41, 0, 0, 0, 0, 0, 254, 0, 255, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 256, 257, 0, 0, 258, 0, 7, 0, 0, 0, 42, 0, 259, 260, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 261, 43, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 71, 262, 263, 264, 0, 0, 0, 0, 0, 0, 0, 265, 0, 0, 0, 0, 0, 8, 0, 0, 0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 266, 0, 0, 0, 0, 2, 0, 267, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 1, 0, 0, 2, 0, 268, 44, 0, 0, 0, 269, 0, 0, 0, 270, 44, 11, 0, 0, 12, 0, 13, 5, 0, 0, 0, 45, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 271, 0, 0, 272, 0, 0, 0, 0, 0, 273, 0, 0, 0, 45, 0, 0, 0, 46, 0, 274, 0, 0, 0, 47, 0, 0, 0, 0, 275, 276, 277, 0, 0, 48, 278, 0, 279, 49, 50, 0, 0, 8, 280, 0, 2, 281, 282, 0, 0, 0, 8, 51, 283, 0, 284, 52, 285, 0, 0, 53, 0, 4, 286, 287, 0, 288, 0, 0, 0, 0, 0, 0, 289, 290, 0, 54, 0, 0, 55, 0, 0, 56, 0, 24, 0, 0, 25, 5, 291, 6, 292, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 4, 0, 0, 0, 2, 0, 293, 3, 294, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 295, 0, 296, 0, 0, 0, 0, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 297, 0, 0, 0, 0, 0, 0, 298, 0, 0, 0, 7, 299, 0, 0, 0, 58, 0, 300, 0, 0, 301, 0, 0, 302, 303, 0, 46, 304, 0, 0, 0, 59, 67, 0, 0, 0, 305, 306, 60, 0, 61, 0, 2, 19, 0, 0, 0, 0, 0, 4, 0, 9, 0, 10, 307, 0, 8, 308, 0, 0, 0, 0, 0, 62, 0, 0, 0, 0, 68, 0, 0, 0, 2, 47, 0, 0, 309, 310, 311, 63, 0, 0, 0, 3, 0, 0, 312, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 313, 0, 51, 314, 52, 73, 0, 315, 53, 64, 0, 0, 0, 0, 0, 0, 0, 65, 0, 0, 316, 0, 66, 0, 0, 317, 67, 68, 0, 54, 0, 318, 69, 319, 0, 70, 55, 320, 321, 71, 72, 0, 56, 0, 322, 323, 0, 73, 57, 324, 0, 58, 0, 74, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 325, 59, 326, 60, 0, 0, 6, 0, 1, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 327, 0, 6, 0, 0, 21, 0, 0, 0, 0, 0, 0, 328, 0, 0, 0, 0, 0, 0, 0, 0, 329, 0, 3, 0, 7, 0, 0, 33, 8, 0, 1, 0, 61, 330, 331, 0, 0, 62, 332, 0, 63, 333, 0, 64, 334, 65, 0, 0, 75, 0, 0, 335, 336, 0, 0, 76, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 66, 337, 0, 67, 0, 0, 0, 0, 338, 339, 69, 0, 0, 0, 77, 0, 4, 5, 0, 6, 0, 3, 0, 0, 0, 340, 0, 341, 0, 342, 0, 0, 0, 78, 0, 0, 79, 343, 0, 0, 0, 0, 68, 0, 80, 0, 344, 0, 81, 69, 345, 0, 346, 347, 348, 82, 83, 0, 349, 70, 84, 350, 0, 351, 352, 353, 85, 0, 0, 354, 0, 0, 0, 0, 0, 0, 0, 0, 0, 71, 72, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 355, 1, 0, 4, 0, 5, 0, 0, 6, 0, 356, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 0, 86, 87, 74, 0, 75, 357, 88, 76, 77, 358, 0, 359, 360, 0, 0, 361, 362, 0, 0, 0, 7, 0, 0, 78, 0, 79, 363, 70, 89, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 364, 0, 365, 0, 366, 0, 367, 0, 90, 0, 368, 369, 0, 91, 370, 371, 372, 373, 92, 93, 0, 0, 0, 374, 0, 375, 376, 377, 0, 94, 95, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 96, 0, 0, 6, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 378, 379, 0, 380, 0, 381, 382, 0, 0, 0, 0, 97, 98, 0, 0, 0, 383, 0, 0, 71, 72, 7, 0, 0, 0, 0, 0, 99, 100, 101, 384, 0, 102, 103, 0, 0, 0, 0, 80, 0, 0, 104, 0, 0, 0, 0, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 106, 0, 82, 107, 0, 83, 385, 386, 0, 0, 84, 0, 8, 0, 0, 387, 0, 0, 108, 0, 0, 85, 0, 388, 0, 0, 86, 0, 389, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 390, 0, 0, 0, 0, 391, 0, 392, 0, 87, 0, 393, 0, 88, 109, 110, 89, 0, 0, 111, 0, 394, 0, 112, 395, 396, 0, 113, 397, 0, 0, 0, 0, 0, 398, 0, 0, 0, 0, 34, 114, 115, 0, 116, 399, 0, 400, 0, 0, 0, 117, 401, 0, 118, 119, 402, 0, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 121, 122, 0, 123, 0, 0, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 1, 0, 0, 2, 3, 4, 0, 2, 5, 0, 0, 2, 5, 6, 7, 0, 1, 0, 0, 2, 0, 8, 0, 5, 0, 9, 0, 6, 9, 1, 10, 0, 11, 0, 12, 1, 1, 0, 9, 9, 0, 13, 14, 11, 3, 11, 0, 15, 3, 2, 16, 17, 8, 2, 11, 18, 15, 16, 3, 0, 19, 20, 21, 0, 2, 22, 23, 1, 24, 25, 0, 26, 6, 8, 6, 27, 9, 1, 28, 1, 15, 29, 30, 3, 4, 0, 0, 31, 32, 6, 2, 33, 34, 4, 0, 0, 0, 6, 10, 35, 36, 16, 37, 38, 0, 39, 40, 6, 41, 0, 42, 0, 0, 43, 44, 7, 5, 45, 5, 46, 47, 48, 7, 9, 0, 5, 49, 50, 51, 38, 8, 8, 0, 52, 0, 53, 54, 15, 8, 55, 56, 0, 57, 0, 18, 0, 58, 59, 60, 8, 61, 19, 62, 1, 63, 3, 64, 8, 65, 66, 67, 2, 68, 2, 19, 69, 70, 71, 72, 73, 0, 3, 74, 10, 0, 0, 75, 0, 76, 77, 2, 10, 0, 1, 78, 4, 0, 79, 0, 80, 0, 81, 0, 82, 83, 84, 0, 85, 86, 87, 88, 3, 89, 9, 0, 11, 90, 13, 4, 91, 92, 93, 94, 12, 95, 96, 0, 0, 97, 98, 3, 99, 0, 100, 22, 22, 9, 1, 24, 15, 101, 0, 4, 102, 1, 1, 3, 103, 0, 11, 104, 105, 0, 106, 107, 108, 109, 110, 111, 10, 0, 112, 23, 16, 0, 0, 13, 1, 0, 113, 19, 1, 26, 13, 4, 14, 114, 7, 1, 13, 115, 27, 116, 117, 0, 0, 18, 18, 5, 118, 8, 0, 0, 6, 20, 0, 2, 119, 1, 27, 1, 0, 120, 121, 49, 18, 9, 3, 19, 122, 1, 7, 123, 124, 22, 125, 12, 126, 0, 5, 127, 128, 129, 130, 131, 132, 29, 31, 133, 134, 12, 20, 135, 32, 13, 16, 136, 137, 21, 0, 5, 12, 138, 139, 140, 13, 141, 2, 142, 143, 144, 35, 15, 145, 146, 147, 38, 148, 2, 6, 4, 149, 150, 0, 39, 151, 152, 2, 153, 0, 154, 40, 26, 41, 155, 156, 4, 157, 49, 22, 13, 158, 159, 8, 42, 160, 161, 162, 0, 163, 164, 22, 0, 165, 166, 47, 5, 0, 31, 167, 168, 169, 16, 170, 171, 14, 0, 172, 173, 174, 34, 6, 0, 51, 0, 0, 13, 175, 1, 22, 23, 15, 3, 0, 41, 1, 176, 25, 177, 178, 7, 7, 0, 179, 2, 180, 2, 181, 182, 24, 183, 29, 184, 23, 1, 2, 185, 186, 187, 31, 0, 26, 0, 3, 1, 188, 8, 27, 9, 189, 190, 2, 191, 192, 52, 193, 18, 194, 195, 196, 0, 0, 197, 198, 4, 199, 53, 3, 28, 200, 34, 20, 201, 202, 3, 203, 0, 25, 204, 205, 206, 207, 208, 209, 5, 210, 211, 212, 213, 214, 3 };

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
            final int compressedBytes = 1352;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXEuS2zYQbcDIFDzewCkt5B0cq1JZ5wTtTWqWWWapqvFBMJ" +
                "vU5AjZzdzEvkmOEkj8CCIB/gSSIKZf1XD0o0igXz90N5t6ZE/w" +
                "vIfT9o6bT3/iHeiveDh8fIZ/gH3+T8gX4GK3l09/fzu84u/4AP" +
                "gbfGEf/vqx+/cIBAKhDWX/ZOhNXj3A7Q/0CxiQdqRWPwQ3ewVW" +
                "Pxj+qu1bzwBHI96/8J/B6oe5Q6HhF3jQ+Gz/fdj/gE+kHzfjsV" +
                "u/v5N+543K/16t/93X/nfQwKybgTbW/pzLs/99L/wPHsj/CITs" +
                "IMu4o4Su4g8BzD6rPiJOUcf5GSrMaPSid3oUUYRAIBAIzYXTyd" +
                "vbL6siay+XmK/F+nmEGOvn409UfyMkAHamuXRYf36kS49Quukk" +
                "PIn6lVv/eLdV/+HlRohSY1hDlgLxbQbxe10/VG798HBi26V+ca" +
                "4f/lHWDyHB+qFrHD60vKtJdFLClbU66/cx/c9fP6/4fzzzn+rn" +
                "hDWKJ0gTM9h/16q/u/px71s/H6r1sz4+znH81vihNX5M9/pDFP" +
                "7LQP54XX+FTOuvlECF6+8tWhVsuNhfpDCAVa4fdMc/bvx/d4n/" +
                "dYLxjxgyvRtCKDuhqyhdlh1SP8SY9cONkD9Z9NRfB/bPJJD/h+" +
                "p3HmJe1e9kewTl4/urM5QzJwh9568D558Q1FjBX/f8o/Cvp/66" +
                "UP/ZdP9D9hmMOrFP2v0YnvZBAeKjgffAGJ6taL9eMnN4eSpCHp" +
                "vcMfvlO4Pdp8TA6fVsMyWe/1cHEcKGTwH+ZVo/RmuKlv3g2n66" +
                "tN+rx37D48+t1J+pfrNlmHoDQfVwPhCRP6NiONWpqnz9AFHM+O" +
                "ntpSWya+TqtJX1cqSBa4d9xSL37lQEkDuHeEY6+uKdv31NAy7r" +
                "r5NhUneFhjMFzSP9MlYOjMuwOrpMxBgXGzyu04JeDqGRFMgwh9" +
                "gq48xSP0ZEn8LrG7wyB3pkyETbP5FAtP9dXNLPVgJbhYA6pHyq" +
                "QxhZXB9IQ4l9A2Zm/fVz0CwtdkSMPBXidj/VY+JsA4SpSYronT" +
                "/dZz1+AyfCLMF+6vD1w40Eqcdg501xZHsmmZPmzBx/yE36DEvn" +
                "VKR25pJ3kdC0n8imQTlkjIUFAbfBnwsL5AgRkxnwYcx4xXQWUR" +
                "iyuUw9X86vg97+S1//Uuv6EV2/yTT/TOv4eSPcf9tyqyKyjNd/" +
                "JYj/SZ9/voHKpP7Z6P0L212/suk/JpD+pq5/ia8/C/dv6pipvk" +
                "eBl+3fXbnU0Tv+3BOsqf3f6/GfMC8aTX06j1FtOZRPcP3z9r+D" +
                "v/9dD+p/50NHuUVCYkKUycShR0RkhFHqv9j9Jzfpz/D7N0h/kq" +
                "RZo3/8WPePn+9Fmt4/PkIir/o3jPNGq39DpBlHDBc6Q5zLa4Un" +
                "EAgR9R+8+o9VRUORjtIqkjrY2zKXuC42ajf3xUCUVMSPce5f7E" +
                "+2KaQYEv/znvlXRnXN/2z+Q+IcPytRCdkf5x0qYYB+64uMtu3P" +
                "oNBfgGX8P05YOXH8nvWr+EHvXXv9SqGk8MbDzZ78QTTu/8Hlhq" +
                "SG8691tdb5FSTfhVyhSRQpXSLEjzt9oadOnE+io9sD/Weuox05" +
                "Xv62SNJGWkCYwf8CEWiTbbLe0PpNSJi8/dBE1pSzPbWQSZSTf5" +
                "kkfv/J3OICypuCtc+f8qdk7f/GxDvN+JsQHzz0atzf71MD9xcp" +
                "ecGqZtEjl3VT1hqmhgWD94+YNxMIBAKB4i/CgrEMNpdlMbKEYO" +
                "IlygRC8pDjvC2s33yKfnffv8RzmTjCxPwbC9YFtFeahRnyP8Pf" +
                "MrQ=");
            
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
            final int compressedBytes = 1219;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUty2zAMJVk1w34WnI4X6Y79HISTVY+Ro7BdZZkj5Cq5Wa" +
                "04tiyZlCkRIEEZb5FMG1MEQBB4gChZCBCY5E96gQM190d7RYgu" +
                "KqpZoeIGIGEv52lr213X+fBHK0P6yP4C+nLM4b/c+KM24pKomL" +
                "O/UYP/+5Gbq6PabqQQ9SXVhV3H9D/12cIqexaSvOst9aHbm1Lv" +
                "zqzm9dgzNghbOR7YaHrwgsEoCsUmoJsvujlewGA06tdRSkTVs0" +
                "2cxwUC6JFA2fXqHPnbnzh/E8j8rWr8CfDXXar+D2n8N8F+KxQF" +
                "rJ924qMOEUN1EM1O5vJH+af283X8h8FYv//NzP7ViPuXOTTXIG" +
                "D5o+H9V7J/pHj/gREtC8MfWoe55O3z8LTk/7UXSO+X4q946pS/" +
                "N+JOWOl+9yv2tF9k3+kXpcTuXvs711nxc18ouKf9ry/3r+L7Y/" +
                "v6j+OPhYu/VjAS4+ctxw/C+acbJwYkh9bRDPT56pQIBIi3bXYu" +
                "nKxs2fUjD4z8587dNiFIyLQFZGCR50ZDDfcf2H6M7UFCh7aN+v" +
                "+N7t/N07Q0N9dDyvZ4Aiw/f6gyxxPQf64yTCkluuh29aGLasEo" +
                "UuOogkUQIdXL6p91fmIb5z+6pVzFDCNd6P6T6z/xpv9Lr6Xs9Z" +
                "dH/fug6i/u/xI5UmRLELkr86uF8+fmHx5fd3xu/sXhH+n8gdef" +
                "x9fkv63Pf+v2zxgPcv7ntvdf/fqXx1PsHxQICVtBGW3VZgzs69" +
                "vf5uePh2L6ugWlKjwc7uUn9hch+8t3+wuE/kmu5y3xn5Xy463/" +
                "MvmNXyO/EwxsLzzfP+J8/Q7Wf+NxI/8TNPyPUWr95+LPTPxNGs" +
                "9gQOWfNfk/Lf6J1fEPh0HZYuObX/8V/MkskoK2/xCsX4f66Tn9" +
                "+W8fqZ8C779Sg++H7j50trj6eP3H7PMXAOP1QvZu8f0v8/xJrf" +
                "MvGkh/Pcyvp/OXOElXyn75/efa/gPcv/UF4xfl/KNii8Lnz67H" +
                "75L5s/r6VeUvRhyOO50UPnzoW4CCypX8lEEWNsWZicXbDbleif" +
                "d/0KrzPEz8C9jPJttPk+r/2fE/cMWSiNd2y+ef+L+6WL9/Mf//" +
                "ir9+ZoEXboT/SdEsrOBbOoxc/m1m9q8uMF6sGZ8FgP5FkfIHr/" +
                "+Q2z/B9j+3RgzTEE+u3b9nYAKDp5kpP3N15Ei8uKNXh50sWf/7" +
                "T2o/f9T8+Nz8Vfv56Xr228D9TwL3n0g+/9eE/xGOH9x/YCwj4x" +
                "GaFqnQJNDODcfv9+f3P72c4vfo+f3nPn6beKkNXFL6qnvKJ6wU" +
                "JC+vOD+jWEVnm5IWuv9Qov/hULYjTjiylecHKzfJ+cJF7+DWv5" +
                "aNv5aubgxl++d6r29CUrNSfguVvfJSUXx+da5N3vu/gOqPmt4Y" +
                "73+9NRBO8gf7X7d3fomW/RkMBqPd+Afx/s0x9KL5637/Xsv8Ia" +
                "y/mu2lqkJiuFKzD1O5SvMzGDhbmWLrxYeLvWWDCKEJ+efnl+nP" +
                "X8up3t4FcvaP8UcDorh+2CNoTwYsf+/g37/Wuv9se//E/F+H/N" +
                "8ukZsxs55Yp1j46eyV5IAC+PwQUP2ZWP/i18+GZOl0cf4cj15V" +
                "5nrz9of1HysqfP9MHv4DI/zowg==");
            
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
            final int compressedBytes = 901;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnVly3CAQhoGQFK7yAw/zkFTlgaRyEMon8VE6PkGOMEfNaB" +
                "aNNJY0CCHW/3uw5YUBuptehBbG6oIzcEVNHl6QEA/kD3Nww1Y/" +
                "Qw/9m76l7r6q2y9O38X1UHRf6Cy9L/LkmdWBUd+e1Fmuw/bk0b" +
                "50/W+VXxnzpwLXhI618rLTP0WUVgn2v79Eh/O/+1ZxX33ntPY0" +
                "/1F6UX9cCuM/2pFT2PzVDGxNFO/PE6T9hcUPGXdZfuo1Tf9t1x" +
                "9txN+sKy/IH/G7Zvul/e3X//wFAk2DC9qWMH4J/SF+gDb84SAa" +
                "iY0fJiDPpOmQcPgfAAAI4XDsVP1lu6rs7ImOXfbDu/qL3+qvbj" +
                "OBFuovFXX84faPR+31p/aX+YvL/P917XWM6XkVnKH6Ttk/AOtT" +
                "U+PWKKQ1m/EPKBdLth/MP3aQeYi/71H3LwLsn6jZkixGpebkyK" +
                "je/pP3huHPov3Xn5lff6Zff4oBAJ6AhBQAkGP8BwAgCcHUAQAA" +
                "rCfv63fh/7O2nErvn6hns8s660880Z+OqL90xS+VOoVm92dTex" +
                "Lc/5I31KZ1b/U/qc8/tt5/lb4S+gdO8VOJ3nPfL1Q4Z50qdKYj" +
                "Fy66tpN/kqZ6+SN/AQCshDs4i3z8QX/+g25jNAfX81dvJfvvx/" +
                "mzlPNfjr+PlmNXxp/8M966b0m1rEmWnj8cGYL9gn3j59z+z8dc" +
                "/Hh19gsWYq4cMXM8Sa1nZQzzu62Sjxy8gTXlGydBPPnrFZ4C57" +
                "8AyA7deHuWd/+ydv1vrT9at5/Wx1+G/NI+f2E5mdIL+RT3l6Bz" +
                "ewDAjvzqVyQxQUqzb8xw++fql8zJBR1Phz+/MxLdNRe/b8H4Rf" +
                "9lP94hvxTIBJ9uU4Qlr/5la7YQ6v6P9dkOYS2CuGs/X7Tn+t32" +
                "/Npnz399Ofbrf9R+l+e/qpb1n3x52OcyE9V5gwf7F3773+Guf1" +
                "Z4/wDYlr/tcP2fyTxxk7PppS5Fixs4sK9qSjHi8tHmQX100z+o" +
                "qv4CIBlqKVxg/Lv6/+bzl4v4+PMcxfDQ+Use91+UUIXXBOUqJ7" +
                "1Kj4UkEDZx+9iYModdsPzTXT9Rcf7vdP/U3Pwpq/nTFv3R5Pg/" +
                "or2/RbOc39+SWH+p/dcO7+/TueWftmL/t4P+VtUPw/bkKb8Dk5" +
                "MOesP5Q75u/Q3nf0+AxN16+HWgo/Big61ft/7VRP+b7fc/fJui" +
                "SQ==");
            
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
            final int compressedBytes = 855;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnUty4yAQhoFhoanKgoUPQG5CzUnmKF05QY6Qo05ke1R2rA" +
                "eIVwP/t3AplWAQ3XT/gESEmNFiElYs3C/V9dPNH/J6eREPTLff" +
                "eLD3d1oI81y/FeqxfrqW/qW/mzBdBC0Fyb/+Y3RMyZ/tv/i2/8" +
                "/uN5uz9Qf130p5k6D98vlHEnmJst+nv/0owH4gloeIdI9FpfyJ" +
                "2c3vhIUM4/9necoZf0vEf9exm2hTN3/CfgP5nxo1EwXnG1m5PA" +
                "AxdOV/0kNWcZd9AAAfgVVWpGjh1vSvm1XxtSVfc+PkrH/lf/07" +
                "xyPKrb/Ni/6+1a9u9X/O9ZvEnWEKmBd0jpK5RDkALahtALqVZw" +
                "D91xyspca0uf+p+7fM2f0H8t//ep4/JZ6/Mes/9XL/H+v9J+it" +
                "o7hUZqAoxmNTRRZUnuLWtmWy7Pkj3/NDtfUH+ZpxI/5Swee/6t" +
                "kv7Pkr7+ePAvdvdFKblsjfopP8yyb/TVu2dq+/mktYHuOnA/3e" +
                "qf8cZA+buH7Mv8HI4y9B/rUN5l8O+sFmi2t94Fja/y10/afj9Q" +
                "/AAJsgelSKPmrjehUDU4OyFJ9/jvH8NkYyOOUhtIyRpPt30q+8" +
                "fND/Z+pfysP/u54/Q9/znEPqge8dgMSRD/tvqZgY2e/+/sfvr8" +
                "V+T88vrb7/McGEYNilgHaFajsSRWfrlrP7z7Z/p2nXrerlT7ut" +
                "f+yif6b4wUFuJfW+H7qim4v95RBOT7ZfJGp/mfrdbgjpEJw/Ao" +
                "4GUUezBd1yyzF/Z9P/Fv0fMTnA/WP8w//ayh8p33+hTv3XMR6/" +
                "LZzf2dr4JWb9Hz//y93+E/q748n3DYP4Uyz+9L3+WSF+DhY/xl" +
                "0/h/7lP/9vff6D9iei9fPb/dsvg6NA2vxRu34AKqVThV5gY4vH" +
                "wFIwYJhK0cpU6eSo91di2z/6+XUguciqe/627U20ApA0f9hl/W" +
                "CqUr9IWf8IIM8CAECR/Mnu/FwA+wWC9eNG9Fcf/tuu/2R4ftHw" +
                "sx8d94Ln+4+E8e/LtHo5zPoDRZ6fScnOz3Rt+k8gMZsXQecvgj" +
                "zYilGVKuZlGVM/U/0E/Y38PzpD6J/d2072HcT2bmP/f/nu/Eud" +
                "mn+xe/9oRA9uxf9av38AQuT9mO0nuABX/gGvv96y");
            
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
            final int compressedBytes = 723;
            final int uncompressedBytes = 38721;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtXUtOwzAQnYQImZ2FugCJhUEcZMRJOIq5SY9K09LSRdo6Te" +
                "yZsd+rFEVRHXvG83nj/IiqRDfv74NyceK1kftx6yj8HQnU/+32" +
                "+6Y8bh+GnUrc5uxE0RGv0P8CedwaZ7IgPxPQMjTPf4L/+OX+07" +
                "T+Fscftqe/Lm/+0Kb/O/hDsBR/8vEP+I8E/1rPfsvwr3rtvw77" +
                "Af9tAj1UoC3+ruP/0v3rRrcibeuE2wO3HKHP4KwAYAdulsPwFP" +
                "/lkRXvPWk7sr9u5L/dkf+OMSxm5L/OtPaTSVaEpRrmbwCwYP0g" +
                "Fl0/mIhMw7on7RMZb9A3hZ4O6e4kxGGSnulw8JIsPlF+KiK/st" +
                "I++fqBhfVnzforU/8vRb75lx5/Gf3bpoqK/ezf/uLJ/jap9vfV" +
                "CH9kAhI9OkANQEv5G/Vfdv4aZ/Cf84rF3awQGgnsxcR0wu3JbP" +
                "+c7m6VRhH0D1TKvzlj/gTW4J927Rf3L8rKn4H/zlp/OW8fM8nP" +
                "iu2/TP6M2eSH/eq333L1c0D+BxQiCrcHAKAYf/BCwnhhZXrTpn" +
                "AkEDJWCAAAcEBV629sXP6Z62d33P+Tu/6OpuXPz9/s5l/c/1oD" +
                "/6s7fwjFDze5K5D/gEJwUIHN+nOQ7Hky/4XL8Sec8p/L17+m/M" +
                "sC+q+Jv7Bi+XH9HPFbmf+qsz+D6ycAkFIYFm8PANXGP9/0NAXC" +
                "84v2YP/9G9aZu+n61/r1H+nxI3+3Xf9Yt7/343B2vz46T48UOv" +
                "4cD7nRFwfa7nbfXij2NAT6OE7mk/+h1++rDMrdaxi4DAD/T23f" +
                "wvo7wwQXLnNE1M+YfwBoAuBPAAD+LcgfVs2/8vwnZqs/8P7ath" +
                "NfC/dvG6xfqvr+JfSH90c0AQ8V2ASen7Ya/5TGX+QfRfUXK54/" +
                "6/wXz0+0LT8A/iHPuhd8P0tn/mnG/xF/6o/fDPkBoNn6gY2Pnz" +
                "B+AABy4Bczou9t");
            
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
            final int compressedBytes = 3968;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWwmY1EQWfq+SVJIeFBB1GB2Ue0ZABRUFQQeBUUEREBW5PP" +
                "AAQeRUEcFB5fJAPBdhOFe8AA9kREF0XVEUVkU8VvFYdVV28WQ9" +
                "V92l99WRTro7PZOZnhlkd/N9nVQqlUpSf/3v/fWqGhAMtgJccN" +
                "gAqAv1IY9Nh3xrGBwELaHAuJE9hrn8WXY4tIV2xk243t0LOrA8" +
                "uB2KjE+gGxvEe8BpcDp7APrDAPyH1c+cZIxwDBgK58NwGMGLwc" +
                "TboY7VGX81GuMS2Bf2Y9exUrMXfGPPhcbmCvjVuBKas77mYCjE" +
                "OdDKOc/9GBfBoWwZHAFHw7HGBNYfitlMXAY94RTjVewJvdwDjD" +
                "vZeNbPuB0GsdPhAvYNDMMG+LVRAjGoZ5jWJXgb7AMNYH/IZV/B" +
                "gdAEmkIzdq15gjEK2uBHcBhbYr0BR8ExrA070P0IOsNxuBT/Zv" +
                "aArnCyvZJNgz7Ql+2EO+AMOBMGwhC4C85h+XAxXGJtAgYWOwY4" +
                "2PglG8J2QQ7sxf8Ie0ND/DQ2hZ0PjbAHHIzfGlvtF6EFHAKtzZ" +
                "64A7ezgaw3/g4OhyOhvTEYOvIh0AmOhy7QHU6CE63N0ANONaZC" +
                "b9gF/eAsOJuvtt6EwXCuUxfOY8vhIriQnxOPs5asldmHFcAoNs" +
                "DsC5fG4/xfrND5IS4348Z4HHMpb4Y824HrY0tZC5aH3eNxp308" +
                "bo63d8UTGzvS+dDpSHhZXg4vptx2VPZ11tRsgUtUrt2Z321eIV" +
                "LWz+YY4zsqdw9/RF1zzo/Fg5sxga+RJTeoc+twmXsn1dLfuJ1q" +
                "l3jRWzag3MfiaRv7Kqm2UZTTln7PW9vk1TbeFWueX8pcJ6/txO" +
                "Lgvfw8sXfl67GHde1D1NGVX8Pax0rYd7r0sby100Wn78JdvIUs" +
                "0TpQ3y/B2q3NXgqHJfLeTLxRbxhJdxBe5gww4DLFL9YV6puz8A" +
                "rWDUYTvyZAAU4wGmGuOxHHw5XQzm6Gk+Jxya+roCjWGMbyJ9xi" +
                "4td4u0jwi3dyP3FHAr274JfCC0w+S77plfQmkl/xeOwos5dJPQ" +
                "Ea853UhvSFrK89AQopp5V8z/lwKKWJX9QSy1n/4HdhT4n4wepM" +
                "8UvmC7zWUk49ryQ0UHgRv8YJfvGf+TzBL4nXEutDwS8+k9N7Kn" +
                "5RW/SAMXAyXZ0mn3E1Xblc8Etjk0/nl8iaLfa4jxfk0HcSvwRe" +
                "Od2MA6GRs1bwS5YklAS/2EGyNZax7oJfiTeU/KIj8cvdT/CL0r" +
                "1xio+X4BflCX5NhAvd0ylN9tDuErSHVi/I50cre2idZN6CnWKb" +
                "eEdhD1kP1tnqCR3ks4pYJ+jmHE+phD3kN7jb3dHyqraH7olgsn" +
                "ZQh/Vkt9H7HiPw4v+m1N1WH4GXM0LgBc2dy2PH+njRUeNF9vAi" +
                "YQ9lHtlDebxa8cvHy54Msk8a7wp7qK9Ie6jx0vZQ8EvgBYcZQ6" +
                "3PBF66LOGlU10VXsoe0rm2h5Sivi3sYaxU2sO1wh56eHn2kPDq" +
                "LvCiPK8/SXvI57NbNb6nKntIVzp6eAXtocCLHafsYQpeZA8lhs" +
                "2JX1MEXsbzTiuob2ywp8FUuIbwKoECuNYqwN729TBZ8Mt4Aa4T" +
                "/FJ40W8sL4u1hNPkOeEVj+dA7AW4Pplfmv1kQe3pil+S4YKFje" +
                "k4xm4izmPHheFF/FoTD9mMzz286HeB+4HO/aI8fil7CG2cLXSc" +
                "a21Px4svUPzSbbszmV+ypMevpwN5OXJP/HJeo5pT8FL8svvpN9" +
                "yYiV+09/D6IJ1fZA+nabxam03hZvJf0926cBPM5vV5Q1ZI/JoD" +
                "N8Isnkd45br17FlAPYRTC7gNWItgy8WS2jOHxTby3ICNLoaZOn" +
                "W14FfgK7/Rx1/5vrKem+Khm7s8LJdL1Pn+soaEPeR14xVswn/J" +
                "4zrrs3gWG3su9F33FXgFc0D6faeZfaa+rzUcXl69nHob7ArgdU" +
                "Oiplv0caEt9ktgUeBtCh2ABQmPkZtcp9ksCSEz6eyU2EtJzy+G" +
                "Up0iBeOgl++08PEypIeOzc6AVzi/7gy0ie+/3o2CV0zY0x/Drj" +
                "rN0/NgftLZ73ULvRl2f2wytXH3pPJSxRC/zoqCl+BXEC9KL06k" +
                "luoj+S+4R9hD4pf0X+ZtkO92VP5L4RUj7IT/Mm8V/ArYw26EUG" +
                "PhvxL28P6cwqD/UvYQ6pAtND3/5fNL2EOl5+nO4eH+K7YXpZL9" +
                "V69Ue0i/YRKvL1L9FyyjlPZfnj2knMPMu2mfZg+dAuW/KIf8lz" +
                "ym+S/aC/+1TfkvZQ9BvCP5L8EvviHdfxFeZwftYZj/8u2hp+fl" +
                "/doe0u9eZQ/5q7CCv4XzzYWxJXAfPAAPWr34FnMB3wYPwXLSG4" +
                "txgfsVi8OjcL9ZSuOvBrASF/J3QCpa/nbOacl9JGdeMr+odyWY" +
                "a08P6Nc+iVzpv3Km6js+SumzbaLxS+mNqPbQXBbK5aGR7eGPme" +
                "wh35qeb/ZMpJZUXLenD/l7/P3AVz7i84v2AX7hFZAfK1D8wgnw" +
                "GD6Y05/0vODXKpyUyi+lDz1+JWrX/KKU5pfsv+jzyx6i+CVLSz" +
                "uUgV8nh/Er8ZwEv8L1IawK55f1Whi/PH1YMb9MIxO/3PHp/Arg" +
                "XFIxv3BKKL9Wa341iRViGTwFT/CmsBbWwDrcBE+S/yozVxPWG+" +
                "m3FV/GZ+XTrsH1SLzCVUj6CGkEi8/ja7JPPKP7xhZ8FB/Dx0N6" +
                "zSu4xvdfQb2RUm5zZX1/gF8lUfnFD45ntVl7VVGnlET4Hu2/ME" +
                "nTgG5TsocGf0vwizc1PiZ+rbeH8S3WROLX01BgX0zPAFme9DzV" +
                "sV7pebdE6/m3Bb88PW9dLVPP+Hpe8Mu6KvBUn1/DfX7pa5Jfyh" +
                "76er6CbxuUag9T9bzk17hkfim8QvkV0PPyPIOet+qH6XlZ89Z0" +
                "PR/AeVLFet74q28PA/z6g+e/MM+lERMeyF+06Ovt71g9gZfuDy" +
                "6rw+ozR7fx97ieccq1E/7p7aRed01oX5R4sZQeaf+UQdF+lAW/" +
                "3q01fuVmVORby71vUsV1+3i51wbszgE+v9zpgl/y2+s7xawB3y" +
                "LiUXRW4OPl8Ytx6MBsbARFfLbHL8wP+q9gPEr5Lx+vwHj5xnR+" +
                "WTOqi194WwX82pwlv/Kqxi8V761gvHycj1eAXxMVv1iRF9/g2w" +
                "VepAq2wVRs6sU3qMQJlD9Z4ZUa31B6w49vyNT1KfZwVpg9VPGo" +
                "ZLxEfD41vlFDeGVrD4/IhBelssQr6fk+Xjq+wUaDwWS8l+9glx" +
                "K/drBh2Fz4L2xG+rAFG87GsBGKX+xiWQfhxUYG8XL+rv1XaShe" +
                "of7L+Tzcf1UPXjXuv46uGl5R/Fc4XthS4UVnml/KHpL/GoeFUm" +
                "+U+PYQCwRe7kl4iLKH4fyyZEwEW4XhlWoPhf/afXjZeVniNaS2" +
                "8fL4JVu4nT62hdmk/d6ieifiEagjJ2yOvPPWCF54YUiLzgzyK6" +
                "A3hmfj7734Rore+CKq3rBbZqk3zouohWak6o3y44dp96fFD5H6" +
                "MNxMR0IdjyK0vpT1TgzY3NyKahX2UN61KDpezufxatoqGz+UeB" +
                "2aJV4jahsvbB/IJbxgOv1ugtmxJgovEZ/HzrJkp8j8WlwJfv1U" +
                "vXhVTs/beVniNWp38Uv4LywK6kN7mMRL6vnYdR6/lD709EYG/y" +
                "WjY9glm/FyVf2Xx6/a0Rv2tbWuN05Q/suoa9TBMjwVT+E7NPM2" +
                "SbyUdkiKR4nxV1g8KjZHxaOspSoeZQTGxlCq8BLxqCj2MKt4VA" +
                "2Ml8PnU+ybI949rzL8UvMpiZZ4LtN8CvZR+lDFe+mYL/Whnk8x" +
                "ZI8V8d40fqXEe617wuK91lVevDfIL3kWId4rz9LWA0SN92p+pc" +
                "V7M+rDiPFee05YvDeMX8nxXsWv8uO99CtnPsXYH1YkEO0n5lOM" +
                "PKU38IxgPAq9GTcOK6WefzjEqt8bPR5FT95nz/VfzoYq+r1JVX" +
                "0i6hlqHIKDsT8OwIE4yJB9y+jq68Pk+KHCKxg/THmb+yqD1+7U" +
                "G8bccsp0r/iZ7gu1jtfZie/1+XWO4Jen51GPMVBGMOFRPFf5L1" +
                "hJx6dD+XU/fe2wcLzonjWR3mvznsAv96Waxyt5PsXjl0yX6eP5" +
                "vt7QOZH0hjf/ZT2g9cbwdLxS9cbuxas8fkXZYpfXOl5DA3qjTK" +
                "5nG6/nlzf5egM34kiFl1yPPVryq4PAC4pwA3QTeAm9gc9IvfGg" +
                "xmuspzeMcZ7eUHhVrDcUXr91vUH7GtcbUh+mzS8D4mWk58fhGI" +
                "EXnWu8KNUyyC9oS187OplfCi8qebrm13IPL1mzwGuW4JePVwaG" +
                "NM/ErzC8wvjl45XIJTWDo3W6STK/xPr5DG/SVR81XvosgZc8Y3" +
                "KfwIvSKjbaMLTGQ9L55eGlS6T5Sx+vQLuM1Uc58oOn5H4tToV1" +
                "Or5R5ttD+iXZQ2MO6vWS+Lw+KrxWKLzC/Ndvyx5m67+qHBepuj" +
                "0s8ewhTsPpdC5mtiS/1PgryC81/vLxEnpD5nVTeInxl3yblcYd" +
                "Pl5q/OXxqzy9UR6/nEU1wS+ndI/j1/X6C+731kep+RTKWaXiUT" +
                "hDzKcYDwfnl+X4K/N8ykOy5plR5lOC/qu8eJTzSE3Mpzgrs4tH" +
                "JZXJGI8yllbjfIpev2GUGauNJ/X3JhhgTcSHEiMuOV+ZvLGRoW" +
                "x/OPP4q+Y2XPnfaQ8zfO1b+Gd8G9+g1DtSqS7mDSVe78k4G30X" +
                "JlYtivXz+Ho5byO5wHMrh5daP58xkrAo9J7A+nkfr+jr552Nv0" +
                "28eL0K8foJv0f57zz8p9jnvKjx+qUKeD1aA3g9UiN4vbSn4mVs" +
                "CqQDvp5NZlNS7SG7pnx7WBNbOL/C7WHFWwKv0t2DV5T1ohXUMN" +
                "ePb+jvlwiyeZWPb3jre42XQ1q1xvR8VfDKNr5R5daeX1U9n/iC" +
                "bWDgD0IfCnsIct0qK/PmK+V90h56+hBfL2e+cpUs0SrKfGVUfV" +
                "jB2CtJH8qcWllvE00f1sT6KONLf70NWy9zvg3G542dlYjPa54a" +
                "XweeOLP24vPR+VVb+rAm1m+kjr90fEOv76WWrm98H3H8Jdfksz" +
                "9VJ7/cZjXBL7fJHscvbz3Az2CwV5LGy98G8fL4FQmvJ2RP6GJ8" +
                "XX3j5f/bw2S8gvMp7P0s51Oe1Hpj1+6JH0a3h26rPS5+qOdTxP" +
                "wy7QfgQPahyjF5leeX12aOb/xv+q/qi2+wv/h4mXvjAPapd8Vs" +
                "VGW81lU/Xm7fGuFX7z0HL7NhEC9/PQBvyj4Lrgdg23V56WW89T" +
                "ZUXo53QtcDrN8d8cOq4JXt/79qn1/eegD4D1B2z/M=");
            
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
            final int compressedBytes = 1691;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmntsFEUcx3+zN3O3d22VajUSHhVpfVWQKiCKilJUqGgVUr" +
                "EKPkALVpBqfLRGbRSNwQfagoYomoIY/1EipuIfApZaSLAitCqg" +
                "YhUFCZoAgij+YdbZ2enePq97e3u97mUnmdmZud3Zx2d/v99350" +
                "aSJAm1SCwJB+QSbZUkUqf0oC00d6IvUStvr0draLkWbaDl5zS3" +
                "ox2sf6Nckg1oO/oQfYTHSZpE6tke29A6yUFCX+jb0cW9HQFzJM" +
                "cptICP+ryUkUQed74vatO1ZvO7RcIJ1ILvYvWTaR6ItsJgmRec" +
                "reUFo8y8oEzhBdM4r42c151stHlQo/CC3MS8oMiOF1zGymtYWQ" +
                "7X2/GCubweU3tPoe/fv7w+TM8LRsbOsLmSiXx7E9ys6Z1F8x00" +
                "V7OWwMowRNTf81hpOSacZ+YFY2i+VN1jkumY6XAL3Grg9WAPr5" +
                "CIn+V1xgs/F+clRIVcIV8QaWsUt8EwzfxKoYxvp/Gr+Ux3Vh0v" +
                "IS+BhRSZ+4TV6eQlNPcFL7woFV4Ga1vEx1yKm/AybPBCpA53qG" +
                "d9zbG1t1r21qfXx6D3k/eHdvbVn/yhdRIWaurzGZ+V8fhF++YJ" +
                "tUINr1ervfdbXs0mcx/uTDcvN/ErNtHpEXhH/+CFu/j2XQjRe4" +
                "6CyO49n97TUVYbSvM5QjR0hPvDx6BU8YcwTvaHMIFm5lPhRlbO" +
                "gCrOsiN0CGbD3fxpYt6r+IwCOE3zpM/UPfdzaT6f10fQfBGM7Z" +
                "XV7UZeMECtncq3g+AhOAuGK7zgAsaL+iAYDZfwPS6HK3itFibr" +
                "RnsEKuE2Tfs+VhLdPjmsPEltD6G5kNeLaS7R8OqAC+Fidc/xcC" +
                "VcRbfXwbW0nAJTaVmhG3smUC0AVF2gq+Ee5k1moZmoBVUJ+/Aa" +
                "7l/c68M/ud74wDt9mCb7KvebPhS6uX1twmrUIWXx+KWxxDanZw" +
                "gLmYhfrnhN9Vv86tHz+BvLcevI0yqvbx3zwn7RG0K7X/UG3hX/" +
                "Xtb6Q7I6eX8Yns794U7v/GGsKi36cIbf/KHK6zvcamlf/7jwh/" +
                "f6xb4iA/1qX5THj6gF/0S3e3X29Z8L+3qU21d3X+oNV/Y1x6/2" +
                "xX5pCTdo/aFAlXUY2fESiu140a3C6xeLc/QrXpmyL6HEC14mv9" +
                "ZIc6V6jlr8q9SPkyv7WpAhXpNS9oa/4d/xfnyQ1o6Fm/BhfAD/" +
                "IVBdH67BR/kef+n23+fyPEfwoSB+ecDrb9kfRoheH9L+juTjl+" +
                "oPj5sstjnwh4xXEt991v6QREmE8hKd8AqvcsaLiKbrLHet59/J" +
                "Jj2fOi/+/pt8VWS/SjQ3k9Ep9l428Qof9iQCmL6XKa8xkbFG+x" +
                "KecmhfBYE+tLGvhtTtC0IyLxDJ6exOvlZ4iUOM/hBKQ5vjvGCC" +
                "npcyPx+f7zXOzyu8ep+fV3iJg+T5eWf3JQ7VjTLAdH/yWOr8fN" +
                "y+rObnaX2y4ehKXcvh/Lzt+7JFd+R4mtn8PGtNYWWFlld8fl4s" +
                "VObn+T3rLEL+/tK0aonuiQjFybxR4gPpfmfdzPf64fvL8BwXxv" +
                "U8KZT1PKFvO9fzlJfYYKfn8fC+1POx9VmlN0pSHYGMtI5fscFm" +
                "fShWOIxfI4L45cQfuolfzL5G44PiXHysx77odkWs1Gxf+E0338" +
                "v47ZTsa7eFb6j2q33hZm/GEefb2G+UPKGsB1B7NOujHF1hJz8u" +
                "z5/xy9v1G+71fM/6DQsP+aRc5hSJH2vXsyl6PnleKue08cpWPW" +
                "9D5xmq54/Leh6d0OpDUVWlaA9/j9l6G9Sl6A15vY3uLVfX2/C2" +
                "Qc/zlsP1NunW8zKvTOh5vd5IrOdZy6TnyWL5/8rokgSW2JbK+x" +
                "DoeW/8YY+eJy/I+jB2g1Ef8rq7+d6d3unDnHxP9ZmP5qNs5ntf" +
                "xK29RLo2KYMppyCbeHkWs99I8NsKE+OX/HqfmfaHqSeyjDSiFv" +
                "IKWUKayOvkZbI0dX9IXu3L7+Vsta9E/6eQ5cnYl39TFtjXKkqE" +
                "/R8c1/MGXnt0ra4+jl/DgvhlImIxf5iSP1zX3/2hH+zL6foo8q" +
                "l2Pko7v+FmPirV+Y2chwP7StJjbs6onq8PeCVpGbVkWwZ5NQa8" +
                "rOMX2e5Z/Poq0PN9F7/MvGi24MXa7WSXmZflqG7no5YHvJLVh2" +
                "R3cvblKa+3Al5u9Tz53qE//MFDXisDXkkcw/2h2ma86Fb1hwYt" +
                "2W1nX3Rfd7zWBry850V+Tpc/DPSGG15kb2JeaYxfnwS8tAn+Bx" +
                "frhO8=");
            
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
            final int compressedBytes = 555;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmktLw0AUhZ2kM7Vqhf4KNyqCW1Fxr4Ib0a3gq6jV3yXiox" +
                "QXKqIiKIqPtQqioKjUUq2WLhynNZSUptaUxuam58A0nQxJCB/n" +
                "3juTkVJKfidNYkem/4eqXai2a/S32Ir6XWPbRv+AneeOO0b/jK" +
                "2ydWkhdspi8g9ix9JB6XOSjNie/Wv4/Q+v/D1yvNQxz6voKSV4" +
                "qRHwqhIvFq3EX/wB/nKdvx7hL0q8stIi/El6RJR4lXBQ3HfNov" +
                "yFP/tureMhf2UnVvGQ7ZeKh74bxEOn/MUT+oa+abxNzNpf2mzR" +
                "uTD8VUOSZeoNu/7iSfjLMX+9qZFUbjxd4rqrgt4l8ldN89c7eJ" +
                "Fll0L+cj2jD/iLFK80eJGqDT95xooXy1DhxZbryl9f8Bdxgua6" +
                "IqC1aCGt0XRGqOa3f1ctCF7VkGiAv8gx84MXGVZNWD8kx6wZ/v" +
                "KOtIgIeuVdvPL9S7Ti+5fr5pUV79+wy0u0gZeTvMpkt3bvxHYP" +
                "1IddovN3f4lu2/7qgL+c8pfoEf0qf/VVNR72gpeT8VAMoJ73hs" +
                "SgqudDBdV9heuH4PUvvIbAi9x8uYiXGAYvF3lqxEQH31NoeSuQ" +
                "9ZcYRTwkxCwixjBfdouM9cNxrB8SmS9PFL1T0hwP9YT9/KXHkb" +
                "8cqy4m62k/gAeqwSnwIsVrGrxI8ZoBL1K8wuBFitc8eJHitQBe" +
                "1FT1/RsRt86XSVGxni8vwl+k4uESeFFSwzdWTlJF");
            
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
            final int compressedBytes = 565;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtnEkvBEEUx1XrqiIhwcHZwcXBV5A4SJz4BtxEkJDYnaxHO/" +
                "MNLJGYIBMnIxNEQojljETCyXliTlr16NAzxhZdpp75v6S6q3rL" +
                "5P3q/d+rTmccJ91E91vfKrSKrFKrwHdEqCadH5tV7MACMNHjOC" +
                "zu9lgi8xXsKmV0CZ9llVcveJHi1Q9epHgNgBcpXoPgRYrXEHiR" +
                "4jUhRlhEjIlRx+EhMczn2ZGPzqFqF+yExbzxDgur7SaLqu2+ag" +
                "fsPHl81zt/xjbYFp/JQP2UbX/n97DjnPH8+Bee2Ht3R9O3njuN" +
                "WW0Q5ZkvV7q18JJBvJb4LIvwaT7F53iIT0IPTbH3epjktYz8RS" +
                "1/KV4rvut+zctuBi9d8fXCK2X0a15iFbz08hJriC8i+SuMeiPn" +
                "9XADvLTF1ybqQ0r1oTj1rYw7Mq6Xu6w2r9fyeqwd89/YeXAHH5" +
                "hk9rXSw3u1vw2sPryBHurKX1Znuh6Kh5TzrdBD40gGXR8+Ir5I" +
                "1fMJ8CLF6wm8dPGSecHz4tXgpTO+PjdZCN9RMqtLlsAL/7k+lK" +
                "XQQ235qyx4XnYcvCjVh7IcvEjxqgAvUrwqwSs79fwLL984yUvt" +
                "o974IC0bVrm8PngWeJkaXzWIL1K86sCLFK968NLPSzYEtv5aB6" +
                "/s1Bve+6hGeM8Uk934/jDn81cveOniJd3/B4jIAbfPQ7IP8fVn" +
                "yjb4U17u94dyLvj4yi8GL1J6uABe2vRw0Y59fp+9B9+ZY3nPWX" +
                "zffQ==");
            
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
            final int compressedBytes = 667;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmstrE1EUxj2Z+1r7X/gP6M4HunUhqLhyIS5EF7ZbcdFqfW" +
                "x8tTZqEYv7KlpCEbS11FJsqPjYaGkrCtJStVXEUnFzvZmMITFq" +
                "knaOubd+BzIz904mIec337nfnRt9XfdQTt+wpaDxsuMxa3UvTd" +
                "Bw0n5At932Lg267SP3GqVncf9Qcv4p3aF+fc1WBT2hAVtHUN4i" +
                "ipkYWdl1mRZ9D9nzimSuovWLvuh5w/q6D31x6qvISw/V4hUdrY" +
                "9X1ApeTa2HD5E9bygu63Faio++VfR/Lx1NV/S/8O4X9PmaW/2Y" +
                "5VPzUeInooHf6ytzuKrvCO50X8PVwwlkwRvNTooZN/96Za14m5" +
                "o/fAm/0Ty/oaeQO58i1td0mvoSr6EvLn3pGV3KbpSH31gDI9wb" +
                "5CAoXnPIgVc8FiintpTVzdX7w48Yv5rnDzMtRiF7voRZL4Zr+M" +
                "cRZMkz5aW8nmI2oB5y1UOzUV6inLwgz8tOmZXnZNfqecmL4MXG" +
                "a5Nqd37jpDphrcyqNvD6V6E6GuWl9jte29TWtP2h2gxeXPqiZb" +
                "MD619MXm47x6cWnh+anXh+GIa+RJfZVYNnN3IX1AxtN3LgT4hZ" +
                "8d7sEfOOy17X+iTmxIfkzJdk/7Xi/e9W+D2fxSKy7el8eR/GL6" +
                "7xq456eAC586oiFvzhQfjDUPQV8zoEXqHwMsdq6A/P59e63zgO" +
                "fXHpy7Qz8GoDLzZep37yMh2pjV+3wIuN1xkGfZ0GLzZeZ/F/gP" +
                "/eb2C9ktPPdxZ5Yf4VBi8GfV0Gr6B4dYMX8/OoLOphIP7wSvr6" +
                "EkvgxcVLzJqrbjuftMrWK01P0of1ymDC9Bb0VcY81pfbDybt0a" +
                "q7wunrD/cL9JWS3/jLNWON8TI3wYuX17ofXppSDQ==");
            
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
            final int compressedBytes = 610;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmk1rE0EYx/skO7PznHvwIuKhrRTaky8303wCDwUtXkpPHq" +
                "yHnorai1avvlXIBxARa7E0ZRGxjQ1pEFoU7bm00LOgn6Csu5sc" +
                "VtLEBGeSmeb/h31nsuzzy3+eZ3YnDMOQgjAl2kntf4mWPfpK5f" +
                "rxJq1G6yKVovV2tFTpR3L+c/36d1qjdfUqbBB9ow9hG6LdEKpF" +
                "otJ5G/U65pX6jYRXtC3Vj6sNd4l4Nbk/eGnipd1fb+Cv3vgr8d" +
                "hbxM4meQcUqOVoe6TLX94h/OVUf/gOvMzxUis1XvDXKclfRcTO" +
                "HolfSf5aFz+b+Uv8hr9Oef76BF7meakNbfnrPXg55a8SeJnipb" +
                "b08xIKvJzyVxm8jPlrX7ygQDwTT8WSKIgn4qUGfz0HL2O8DmJ/" +
                "+XeT2r7gz4NXt+Tf65SXnI54HXvl1u28Cv7rljkvlb+Y/t9fPA" +
                "B/daPeYI+z6A/t5sW+zFMgJ3TWhzIHXib91VqsEDubFL/v5TP4" +
                "ntK/42V/wywvPtvPvBJ/nYO/XOGllmJ/8Xl8/3Kp3uChpleGET" +
                "t7x8s6+kMegb/M+cvfzX6s7WVPjGhmLnO74dxsL5+EL/R9PT+K" +
                "esO98TKPueEvqCXFccTAKV6XEAOLaFz+V30oL3b8vncc+ctk/t" +
                "Jez18BL7O8OKeV11XwMswrr5XXBHiZ45WMv65h/OVm/sL8Dbt5" +
                "8VSaF1/X0h/eAK/u+Itvgpe9vHiyrRHajBNPN9hHJHWPv27BX8" +
                "by1x35kAL5SC7G87HlA8w/7Jbk4055xfOx/2J3/6R2eD/vlngB" +
                "MbBHA38AeAaiZg==");
            
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
            final int compressedBytes = 499;
            final int uncompressedBytes = 19441;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtms8uA1EUxntqbqbnLkQisRGsLbrFQrAVD2HPxsrSG6Atkb" +
                "BohY2E0MoEEUUQidLgAUg8hLAbo238qwVyr5l7+52k05lpuvl+" +
                "+b5zz53hKXeJPDfrvxVdfDg/9303R1d0XLs+oM3gWKBicDwNPm" +
                "d0U7l/WPv9mvK07S76dUVl2vF/UFTyUVUlTv72P05Bu4iR9D5d" +
                "ffEX3f7WX5yGv3T5i+fV8xJJ8NKZh8r9hf5lFq8seOnj5dyRx7" +
                "ng+0EVL+cevLT1r2X1/nIewcuoPFwBL6N4rYKXUbzWwMsoXuvg" +
                "Fd5+VHyCN6Ce1ftRW/CXUXmYB6/w8pAL0M7yPNyFv0Jdb+xBPX" +
                "OK96GB5XlYRB7qykM+wvNK6/rXJdQzpwJeZagQ1f7FpKB/xZCH" +
                "/7G/wY47KeYU9K8UeOnjxU94X9Q0f/GzyvUheOnk9fq+jRR438" +
                "YoXgxe9sxfshnqRacq/mqBvyzyVyvUi+q8rMJfCcxfGv2VyFR5" +
                "qctD2QZeunjJ9vfzptL3eRgfq7s3Dj3DzEMeUJmHskOvv2Rnw+" +
                "9vDKnkxYOaeXU1/LzcjfW8Rev5JNSLSskekSZPzIoZkRELYhrP" +
                "U0zoX7JXZf9yRsFLLy+187LsAy9t81c/8tCu9YYchnZRm5cV5+" +
                "EI/KXLX7EXJLm1tQ==");
            
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
                "eNrtm71KA0EUhXNlxySzveBD+CqCjYVWClpZ2OoDWPmv+AQKIi" +
                "omLFZG4g+CYkgCglr4E7BRA6lsvU40RUARITPM3vVc2CW7252P" +
                "c+bMQJiZKeK2ofO232fmqtIlFVvPB7Rr7jkqmPuJuU6p8vn+sP" +
                "W9THuU1/38bahE+/yHoQvGfClx/NNbPWCfV2YGvFzxYhf+GgQv" +
                "UbyGwMslr9+na1IPQ71YkbTtrxH4S1QejoKX1zwcg3qJzsNx+E" +
                "tUHk6Al9c8nIJ6csbwmoYKcRk9q5YoUgtqXi2rNTWnVjrPQ7WI" +
                "PPSXh3oV2sW3H2apc39lU/CXqH64Dl6ieG2Al9c+vwn1En2+sQ" +
                "V/efXXNtSLzf4rF9xRlL5hDmq2/JW+hr887r/y0E7SmDwsQIVE" +
                "940i8tD9/ksf2eIV7ICXx/WrBO1ENcgyNBDFqwoNEt43rrB+ue" +
                "8bFnndgpcoXvfgJYrXA3i549U8P9SPNs8PA/jLGS9ds++v4A28" +
                "PO6Xn6Bdwvv8M/wlqm+8gJfjvvGKviGkb9TRN0TxatjnpfrAS9" +
                "T69Q5e/vp82A3t4jTNvhH2oG9I6odhL3j93/Urg/9/OeOV+gCp" +
                "HRb2");
            
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
                "eNrt2MtKw1AQxnEHAlmcRxDduPMKXjZenkRFN259Gwt2UcG1aC" +
                "1BEFopVgS1XvoACj6E63iQLBTUKuTYfOY/kKRpFinzY2bOaZr2" +
                "CzecEoUKSz7cXb37fOmPnnWtnd037dCfj63lzx1/XNjD2/dn2f" +
                "N7q1vDjXzyjls7+dFvucYjy8T5n3mN4hXOK3q0xI3563NeXtET" +
                "XiHrq8/8Gid3SuEmyEGBNCbjqiVx7ev5Fe/9th/Gu/RDqfXGFF" +
                "5SXtN4SXnN4CXlNYuXlNccXoH3y/PslzW83EL+9RW94DXA/zcW" +
                "yV3BJPOeX0vUV+D5tcz8EplfK8yv0q/nV/GS8lrDK/D8Wmd+lb" +
                "i+NvAKXF+b1JeU1xZeJe6H23hJee3gJeVVwUvKq4qXlFcNLymv" +
                "fbxCen0f7oDcFUwy7/o6or6k+mEdLymvBl5SXqd4SXk18ZLyau" +
                "El5dXGa4D7rw65++f7rxvqS6ofdvGS8rrDS8qrh1cor6FXhIIv" +
                "tQ==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 0, 18, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
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
            final int compressedBytes = 98;
            final int uncompressedBytes = 38977;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrt2MENACAIBDD2n5kENuDFC9oJjHcSNQIAAAAAAABgR43SBs" +
                "lf/+SLfuif82l/5Sd/0D8AAAAADvB/BQAAgPcnAOY/gPlsPgMA" +
                "7j/WDwAAAAAAAAAAAAAAAACvNNjyDN0=");
            
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
                "eNrt3KENADAMA7D+f/Ok7YORtCqxeQIDUwUAdLtfJ86n/QAAAA" +
                "AAAAAAAAAAAAAAAADT/CMA2G8AAAAAAAAAAAAAAAAAAAAAgE7+" +
                "UYA1D5bruTU=");
            
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
            final int rows = 24;
            final int cols = 123;
            final int compressedBytes = 130;
            final int uncompressedBytes = 11809;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtmMcNgDAUxT59MjJXlqbXEbgCtryBrZcoyZEfSEUOYdBvOq" +
                "CQGh1gdj3qANN60QGm9awDzH1d6wCz60sHmNaHDryvxdby2daT" +
                "DjCtBx1g3lyVDjC7PnWA2XWrA0zrTgeYM3zXAab1qgPMGa4D/8" +
                "3kf7sudeCuxdbyTuIGJLKS2A==");
            
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
