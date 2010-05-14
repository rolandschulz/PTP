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
final class ExprParsingTables extends ParsingTables
{
    private static ExprParsingTables instance = null;

    public static ExprParsingTables getInstance()
    {
        if (instance == null)
            instance = new ExprParsingTables();
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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 10, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 0, 45, 46, 5, 7, 47, 48, 49, 50, 7, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 11, 66, 67, 68, 69, 8, 9, 70, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 18, 80, 24, 81, 26, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 28, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 16, 127, 128, 129, 130, 131, 41, 132, 133, 17, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 3, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 16, 17, 18, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 22, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 25, 0, 0, 26, 27, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 30, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 35, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 38, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0 };

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
            final int rows = 231;
            final int cols = 8;
            final int compressedBytes = 561;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWM1uwyAM/oLQxHbKcUda9bBH2HVvxrGP0GMfdSHJlBUwNj" +
                "hRNWmetJ9+A2xjf7aBQ4A18HCI4gLe48+P6RNgxGm8GVxgz3jB" +
                "8AmcQhjgR+9w9/ia/h6vC+5WHAuOFEcZRzz3ApjpK/46TOrEc4" +
                "HgF4XiB2b5vwf9VmH0Z/ePcgEpnP6//GN6/KO1L9MvtPlfgM/+" +
                "OU86Vte7+nqTrQ+b/S32ZfGD6v2K7Wfj19D3Z+n7E90/ab8gf5" +
                "jzc/8gzy/bG38C/zD5x/PLTWR/rt99OcehLsx6yv9uOz+VsYU/" +
                "ZrHxmylvwfHb0ThY/1Xt/5Ef64ZW+1b/kOsToXBqvTq/9Hjdf0" +
                "z+/eN/HE/i86j8PzA+mfwX9HcH8pe2fmtxQf0S1qe3vvqWRFiR" +
                "H20JDzJ+TfR7nfUbN/2Y85vrf4Jv+qcSiArQ2t8/F9f3x7L5h+" +
                "oPRf0LRwGK/kbuP3PIfANcVfMF3/+q9d9pPjJd/CTKT1Tun8G1" +
                "+8/rLaxf53Pfyi8cv7Hzg5S/CP5l++OxtT9vnH+E7z/UfFTM/z" +
                "30e5xP6P6+7X4L9VV3v9r86e5/7g/zU398Mf2DcH6rrD+UXxrq" +
                "lxE1UCX9Mn7BpJ+X7c/F3272tfGrnP9154v8b/vrhzo+GVzdv+" +
                "72PtMXH3r+0u3fXx+rsdTAL9L3Oa6/RVf/ssP9dL1f4un1yUv1" +
                "4+ubtn/k+EXBP2r/7fF+quiP+t6vOf5urx/c+y+JfwOPdJwu");
            
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

    protected static int lookupSigmap(int row, int col)
    {
        return sigmap[row][col];
    }

    protected static int[][] value = null;

    protected static void valueInit()
    {
        try
        {
            final int rows = 179;
            final int cols = 21;
            final int compressedBytes = 2675;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWlusXGUVHgrhoWofCA9GExNf8EQsxIYokFb/2dvpNSZq9E" +
                "GjPvhmE33SiOX2+2BAjMEYuSi0otFojDFR6OX09LSnd6GJMRgl" +
                "tYXSSm8IomhF5KJr7W++f61/75l99jnlxJxZWfv7vrW+f8++z9" +
                "4zE9/Zk1dcKvljySsr9eZ4hUzf0b9FpsskvzM40UuvuKR/S7xc" +
                "8K3x7aor15viW2T6NjiKFzTiUsl3FS9QG4LFXdDsxQnroRavMk" +
                "f4k0ZcKvk7ySuH+oohvjsuq3AauuJLJC+XlOVMNVlOc5RXa4Q/" +
                "llf3ekBqjV4PLG6GZg9JV851VLWNNuq0LDAFUhuaQzN+vukCj2" +
                "kfVZjto4ov69VecUn8st9H8Uv1feSXMy6v3m91uXr4zjUk02m5" +
                "Oi2nc5mT88RrphevBet/tTfyhTrn2XQVz2jEjcUzyqkNwajJNX" +
                "WeVvOO8BONuBEo+7mGZBas6DzrtWEc05Dj85jUq9QgxhVg1ORI" +
                "q3ouo1r2e9w0er9LXte234tTGnJunlJObQhGTS7n5ikbnfPiVD" +
                "iuIet+XJa+Sg1DMGpypFU9D8eLCxqynBfknarUMASjJkda1fPi" +
                "QrlSI/6sXClH7Mrq2F0JjDcCUYcPvdxLv8zlBqhwVkPW/awsfZ" +
                "UaxLgSjJocaVXPZdQHYhHXxKVxnbxTqN4vAKmBsR8HcXUZ4ipo" +
                "9OL6uEFqgS7hH4xru1xDiq35sVRs9ceSdmvH0kkN2UcnlVMbgl" +
                "GTI63qOZQtZ7UOa8u1ZDmS6TR+OK8Bh/3lGnFpuVw5taGM/ohp" +
                "9pBW9bxcHo5qyH4/KnuuSg1i/CgYNTnSqp6Ho+V7NeRdUlIDqy" +
                "VIOvda1XOOCjs8hh3U1rFX2BG/qVW64l35CFFnNBSZ1BrxY2DU" +
                "udeqnkPNenzuqB2fO/xnnHZrx+efNRSZ1EAw6txrVc+hZLknPY" +
                "ZJauu4rTXJRC9+Nx8x6lWuL9eT5SjjP5Hruit+3G2jEdtzWJHt" +
                "Obin8dnhtqd2sT1r63PYYzisSC4jvl/3IuGI38tHyBK/T0MRGT" +
                "8JHT8FhIe+3GtVzzlqOPZDmAKpDc3RrPlqcVpDMX4aSA0Eo07e" +
                "z1gPNbmnTY6ypyHv0IufBVIDe5km77leWeNlp3vF+GDbfo8PYL" +
                "/Hz/E8aptnf8x9SH9T9oywqT7P4m8aikxqIBh17rVqtT2To3yP" +
                "hmyNlNRAMOrca1XPoRprd21Nf7HtXI5faJxBv7GpKUXrFNvyEc" +
                "U29OHQrh/R7RkhTrXu97vq18/whE1NKVonzmTL+QQTjuqzw43o" +
                "tpyDe1uvS/fWl7PTPO9ru0/W7qjno9H7vX/zmGeZm8eraq/dp6" +
                "HIpAaCUedeq3peDNcqbPMYtoV09CiLB7J9tI0JV7WP3Ijk2+4x" +
                "bKdGJR6pe5FwVfN0I7p9HsVj3Y7P2nIe8BgOKJLLiKfrXiQc1X" +
                "K6Ecm332PYrxj292+3jvf2b0cfPbhQyXzTHsO0Ypju32od7+3f" +
                "ij56cKGS+Q55DIcUw6F+tI739iP66MGFSuab8RhmFMNM/zbreG" +
                "//NvTRgwuVzLfbY9itSN6Y526m9fyItmNp/Pch+bFU1UZ9H7IA" +
                "8wz7PIZ9iuSNdd/HtJ4fkWpTHsOUInljnlNM6/kRqbbXY9irSN" +
                "6Y516m9fyIVNvjMexRJG/Mcw/Ten5Equ30GHYqkjfmuZNpPT8i" +
                "1XZ5DLsUyRvz3MW0nh+Ragc9hoOK5I15HmRaz48QfNSmphTzjp" +
                "vno0zr+RHz+15x/ueRXee7zrO2Pg97DA9TWyf3Ir2r6Rve5ZeY" +
                "AqkNzdGs+Wo4YlNTinnHLecRpvX8CMHHbGpKMe+4eT7GtJ4fsT" +
                "D7vXy/hiJSnuMqLc9xFcJDX+61qucctSDH0iMewyPU1sm9SO8a" +
                "4dvqMWwNW+ud3Iv0Lu8r/qWhGL8GpAaCUZMjreq5zKnLM8LXu2" +
                "xPe47r8nxUbG/TVPEbaXyX7yona98FTWbfBU0Ol/Nb6Xnz7xqK" +
                "8W4gNRCMOnm/bT3U5HkzOYoHNaQ7BsGoR/XAZZ7OMfv27Pay7V" +
                "leoqHIpAaCUedeq3peXhKe1pCjNiU1EIw691rVc4lfa0glJTUQ" +
                "jDr3WtVziac0pJKSGghGnXut6rnESQ2ppKQGglHnXqt6Hk4uxD" +
                "W5+KeGIpMaCEade63qOVQ6rgaYAqkNzdGs1au1T+QN5QayHNP3" +
                "nxtyvyG+/wwnNBSZ1EAw6txrVc/DiTfgu6B7GpX57ffr2vZ7/y" +
                "YNRSY1EIw691rV8/5N5fUasoWHKZ/vlZbP9wqrrV/1GOa1qufl" +
                "9eG8hmzhlNRAMOrca1XPw/lylYa8yzB1Cow3Atlj1L30l6viDa" +
                "aqOejvcf2q1wdSG5qjWfPV/lc0FJnUQDDq3GtVz6HSsuL3qjXl" +
                "muE715BMp/War5bXaCgyqYHwUOdeq3peXhOe1JC9lpIaCEade6" +
                "3qucQ5DamkpAaCUedeq3oOVbsurSvXkeVIplP3W6RzDfsrNBSZ" +
                "1EB4qHOvVT0vVxTnNOQqfU7ubSqkBoJRJ+/91kNN7kOSY3CHRq" +
                "83DsGoR/Xq/cEdzd9QmPiVBCxu9r+hyFK1/4ayREO2xpJ4N5Aa" +
                "CEadvFush1q8yhyd7j9r36UXU9n959Tw/vOhuXxH3e3z3f2G8r" +
                "jGeASjHtWr99GZ9X5+Z5umij+6yM+41nubBdmeP9AYj2DUo3r1" +
                "vsQDGlJLSQ0Eo869VvV9dGa9D/npHJ8WsD1//obef76soSjXpZ" +
                "e9BoJRJ+8vrIeanO/J0ekZ9pdz2+8Lciyd0VCMvwJSA8Gok3fC" +
                "eqjJuifH4E4NuaaOQTDqUb16f3DnYjk3O12XdrVpqrh9cVyXuv" +
                "0WmdZupk0XjV9uis0a4xGMelSv3i82h9MacgeVkhoIRp17req5" +
                "ROM/mkz8UxNscML/V9O6zTHVvzi7XEP2zPF8P6+hyKQGglHnXq" +
                "t6LvGShlReinuB1EAw6uSdsJ73DeOshlRSUgPBqHOvVT2Hmu34" +
                "HNzf9unT7P7fzqPdbbrY3emztD7PPW26aPwSVjykMR7BqEf16n" +
                "2JFzWk9mI8DKQGglEn74T1UJPPo+RYLNfP4j8aikxqIBh17rWq" +
                "5xLPakjlWdmeFVIDwaiTd8J6qMn2TI7i3xrSTUkNBKPOvVb1XO" +
                "I5Dak8J/dgFVIDwaiT97fWQ02WMznKSzXk6enSeBhIDQSjTt4J" +
                "66Emz1zO0eE8mm7TReNfFMU/NBSZ1EAw6txrVc+hLu77+fj4fK" +
                "4hs7h/31j3VzQU5fh8xWsgGHXyTlgPNdnvyVH8RUO6KamBYNS5" +
                "16qeS7ymIZXX5PiskBoIRp28f7AearKcybEQ95/lZRqKTGogGH" +
                "Xutarn5WXFFg1ZizEIRj2qV+8XWxbNNflVDUUmNRCMOvda1fPi" +
                "1QV5jvuhxngEox7Vq/fRmfX5/cnW8/34fK4h8USr+6nGNeR5DU" +
                "W5hjzvNRCMOnknrIeanJvJsWiOz9c1FGXdX/caCEadvBPWQ03W" +
                "PTmKv2pINyU1EIw691rVc6hZ9/vp3kW/GvM8M7fxxX81FJnUQD" +
                "Dq3GtVz0X9D57xS44=");
            
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
        valueInit();
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 0, 12, 2, 0, 0, 0, 0, 0, 13, 2, 0, 0, 0, 0, 0, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 3, 4, 0, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 1, 1, 0, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 7, 0, 0, 9, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 28, 0, 29, 0, 0, 9, 0, 0, 4, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 35, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0 };

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
            final int rows = 231;
            final int cols = 16;
            final int compressedBytes = 451;
            final int uncompressedBytes = 14785;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW1GSwiAMfWX56CcfHqBHYfZkOcIeeUfrzuJoW6AEAs376O" +
                "hoxeQlLwmoBRxgMWPBigXm+dDcL+Tv1y+LCfMNhD/QDI9NTK9P" +
                "CWPj7r8Aof9CL7nwTav/bEn/Gx7rPBTbzIf8mQP+HLms/OkLYr" +
                "LdHb/IxF/1DHOaiif1e/mX0Xf+J6z6C7DwzwGTbf+H+vXQoxf7" +
                "wW2/pIgmxsh7q/+32Pr/HX5KKpbN9ZG+Prf//J4J7GDOcxejP5" +
                "ZJfxL5b6R/fPyn2e8ox37tn+vW7/L1q63+KErwv6dfO/obdb9C" +
                "Uar+5NT/OP1D1f69pH6Oq7+R/Gf0Xy7pW8iOnwos5M9PP/HzGx" +
                "Wf33qffyzP+YOw+Z/E+l9xhfjNgRmGIx/N3/H+fz3+2u1/Uq8m" +
                "GFwUrZWEu38hKGT2L2fr34f7Xfnzh1r1o1X9t2fuLHD+NKI6Uq" +
                "7/nvxTF/PLWf7T8pdi4yfx92O2kg5W0b/i+SdZv2qs7xn1UzHc" +
                "/D5c/cvAlOxF1v5fpzPZ+iluOo3uHxjq9wj7f0z9V6v8J2b7W8" +
                "e/7P6lgX4I+/8J3/zVe/z0rr+6f3Lt+t9D/8Zf/zX+muEXm99+" +
                "xQ==");
            
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

    protected static int lookupSigmap(int row, int col)
    {
        return sigmap[row][col];
    }

    protected static int[][] value = null;

    protected static void valueInit()
    {
        try
        {
            final int rows = 55;
            final int cols = 18;
            final int compressedBytes = 588;
            final int uncompressedBytes = 3961;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNlM9rE0EUx7/BlthTLEVDICHBpAepBKHEFnLUo6QHQQUPKt" +
                "YfoOCPQlvR/qCK8SCFUqjHUkRBRFq0FdtDDy0GKy0iiL9u2v4f" +
                "6cvs7O7s7swmmwxNA28zww6f/b7ve28QRgFxZNCHCGLIIok8Op" +
                "DAMbzBBZxDCKfQgkM4gU7cwhFEkUY7juIAWnEQKZxGGw7jdrmM" +
                "AkWGIkKRpchTJNBLT86hVYVzEffK1g9d1opxEKZV3OAgRs8kRU" +
                "XPWoVDEaIw9NzEkEvPcaRsDnpYXt0sr5Mmx/qazZnEiIuTU3LO" +
                "+HCmMKrm8NNWXmzn5pw19LB9lIJzaMU51mkHp+KzyDF9Njn0PG" +
                "9wTJ/pyX3GJZbXFTsvXHX7QytnXpdtPehnp6+Vq/zMvJTvVXV/" +
                "jxsuPQsePded/ogc02d88dSrZPiDOzKfPXrucs6mjOPUI+Pgvj" +
                "MvWR968hqoXnd889S9JNR90F13JeeHmoPh2voQD6R96NvPeKio" +
                "1z9lvdJV5+KRkNeOnz/0r5gL3zllethurCY9435zyvcqPRO1zV" +
                "fV+QvjKeVV5PP+hO75x7J556fV8/5Mlx75vLt9lup57jOnQTjT" +
                "Igcz9j0fkJMTOC/2gT+zkvtnzs0JWPdG9Lz0zMUrXf7wfn6tPy" +
                "+8DdY/Dj3vGtRTtOuF+Xo4/P2i4NiHPe/DJaU/H5vSh580cZY9" +
                "Pq9o7ufVuv1Z3wf3z2dNPm9o4nwVOFt6/OHf+a657j/rrTt//6" +
                "updf+tqV5/lPfG36bcG/81cbYFzi7KkxPv");
            
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
        valueInit();
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

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
            final int rows = 231;
            final int cols = 8;
            final int compressedBytes = 30;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtwQENAAAAwqD3T20ON6AAAAAAAAAATgwc4QAB");
            
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

    protected static int lookupSigmap(int row, int col)
    {
        return sigmap[row][col];
    }

    protected static int[][] value = null;

    protected static void valueInit()
    {
        try
        {
            final int rows = 1;
            final int cols = 1;
            final int compressedBytes = 7;
            final int uncompressedBytes = 5;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNpjYAACAA==");
            
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
        valueInit();
    }
    }

}
