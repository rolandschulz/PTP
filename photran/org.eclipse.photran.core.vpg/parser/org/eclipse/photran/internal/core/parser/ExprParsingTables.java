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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 10, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 45, 0, 46, 5, 7, 47, 48, 49, 50, 11, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 7, 66, 67, 68, 69, 8, 70, 9, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 16, 80, 24, 81, 25, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 28, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 17, 127, 128, 129, 130, 131, 41, 132, 133, 18, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 3, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 16, 17, 18, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 21, 22, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 25, 0, 0, 26, 27, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 30, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 34, 0, 0, 0, 0, 0, 0, 0, 35, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 0, 38, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 0, 0 };

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
            final int compressedBytes = 547;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWEFyAiEQ7KWo1CanPeaIloc8Ic/j6BM8+tTAronKLvTAYK" +
                "xKBStqtgWaGeiZASM8rIHDiNhGj/f4+RGehP+wm04GB9g9XjB8" +
                "AjvvB7jJjTg7OB/w44KPFxwLjhTHNh7nCDhMeMWvA+KzKfx5tx" +
                "CKD8zyuzt+l0b40/FjOyDbGP8b+5gW+2jXx/kdN/0n9U/AZ/vs" +
                "A8cs/zK/H/vf9/ed9g+K/pXbx5Txkv9sq/+gXz+bf2UfrM9XD/" +
                "65/UHOH9eXk2j9a37nZe4R5Ub65+w/XudP21SjH3Oz8c1sD8H0" +
                "7dE4qP2K65eO/736YWOIaJ8iftNyeK6/+nxp8YRfnf//8T+A68" +
                "6PEFfs3zI/nr/9yvp4fmSecr4F8UsYn97a4luiMDl9XeNepq8J" +
                "v9eZ33TlR+avjv8JfuWfNl9UWHl+/1xcm/9L659cfijKX5hEKP" +
                "Kbbvl7a32Do6q+4Pmvmn+n+sg06ZPofKLgf4Jrx5/7W1h3qc9d" +
                "rb4wfaP1g1S/MvpL8+epNj+vrH+E9z+5+mjz/PfgJ6sfav27EV" +
                "91/tWen+b85yb+qvYXyR8k/W2x/0P1pSJ+GVECtcVvpS8I/Jxs" +
                "fLb/uq2vTl/l+q+bX2R/2x4/1PuT4Or8tdv9TNv+0OuXbvz2+F" +
                "jcSxX6Ir2fY/ktmvKXDv5pur/E0+OTk/Lj8U2bPzJ9UeiP2n7s" +
                "fvTB+VHb/TXT7/r4we5/s/gX2U64Nw==");
            
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
            final int compressedBytes = 2671;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWlusHWUV3hQpUDVNCC8aY+ILnpgYsE1KQKv/zGSXSwvUqA" +
                "8a9cE3m+CTplhuvw8+iJEao4kXilhAjTEmCvR2ek5P70ITYjTa" +
                "aMvtgdNWRS6iaEV0rfn296/1z+w9Z9p6YrpX1nzft9b3z54z/+" +
                "y57H3iuwbyisskH5K8rFZviZfL8p3FHbJcLvmN4fwgveKS4o64" +
                "VPBt8R2qa9eb41tl+XY4ylc14jLJd5evUhuCxd3Q7MUp66EWrz" +
                "BH+INGXCb5K8nLRvryEb4nLq9xBrrmSySXSsp2pppspzmqqzTC" +
                "76qrBgMgtcZgABa3QLOHpCvnOqreRxt0WV2HJZDa0Bya8bNtF3" +
                "hMc1RjNkc1Xz5ovOKS+AU/R/HzzTny2xnfW7/f2mrt6J0bSKbL" +
                "am3aTucyJ9eJ19wgXglWfHEw9oU619l2lac04obylHJqQzBqck" +
                "1dp9W8IzysETcAZZ4bSGbBiq6zWRvFMQ05Po9JvU4NYlwBRk2O" +
                "tKrnMqpj3uPG8fMuubJr3sNxDdnO48qpDcGoyZFW9TwcL09oyO" +
                "f9hOzlOjUMwajJ5fOeet43itMass7TUq1TwxCMmhxpVc/L01Wh" +
                "EX9cFXLEFvWxWwDjtUDU4UMv99Iva7lmtLYen81yVz5H5S4/R9" +
                "rN5yiujlW8Pi6LN8g7Dev3GwKpgbGIa6J04geh0Ys3xrVSC3QJ" +
                "/1C8LpzQkHmX/YzUIMZ1YNTkSKt6Hk6U8xoyR3J1QGoYglGTI6" +
                "3qeTm6znB/1n/DTdVNZDmS6TI2asBRf4VGXFatUE5tKKPXm2YP" +
                "aVXPJVZpyDpXSbVODcPanTR7SKt6Xq0Kv9eQPZySWiN+GIw691" +
                "rVcyjROzyGHdTWsVfYEb+qVbriPfmIdBx3H/MzjWN+xl+PtNs4" +
                "L81rKDKpNeJHwKhzr1U9D/PlSQ15t5TUQDDq3GtVz6HkPXZ6DD" +
                "upreP2504mevGb+Yhxr+qW6hayHGX8x3LddMWPds/RqCJzNLyv" +
                "de1wc6RdzFHj7znsMRxWJJcR3216kXDE7+QjZIvfr6GIjB+Hjp" +
                "8AwkNf7rWq5xw1GnsDlkBqQ3O0a75a/lFDMX4SSA0Eo07eT1kP" +
                "NbmnTY7qIg15h4vip4HUQDBqcqRVPZc19bgexc55j9/DvMfPpO" +
                "tRxzqLTePvQ4pN2TPCpuY6y79pKDKpgWDUudeq9f5Mjup9GrI3" +
                "UlIDwahzr1U9h2r9dVc29Oe6Psvx1tYn6Je2NKVonXI6H1FOow" +
                "+Hdv2Ifs8Icbpz3u9pnZOP2tKUonXiXLadR5lw1NcON6Lfdg63" +
                "dJ6XtrTul/qs8/6u+2Ttjns+Gj/vxe0TnmVun6zqWbtPQ5FJDQ" +
                "Sjzr1W9bwcfZLDNo9hGzUq8UA2R9uYcNVz5EYk33aPYTs1KvFI" +
                "04uEq16nG9HvehSP9Ts+G9u532PYr0guI55tepFw1NvpRiTfAY" +
                "/hgGI4UNxtHe8t7kYfPbhQyXwzHsOMYpgp7rSO9xZ3oo8eXKhk" +
                "vkMewyHFcKiI1vHeIqKPHlyoZL45j2FOMcwVd1nHe4u70EcPLl" +
                "T63yuOeYZ1817Xxnw2w6zHMKtI3trOWab1/IhF3c59HsM+RfLW" +
                "du5jWs+PSLVpj2Fakby1zmmm9fyIVNvjMexRJG+tcw/Ten5Equ" +
                "31GPYqkrfWuZdpPT8i1XZ5DLsUyVvr3MW0nh+Rars9ht2K5K11" +
                "7mZaz49ItYMew0FF8tY6DzKt50cIPm5LU4p5x63zcab1/Iiz+1" +
                "6x3zHffZ7vu87G3/OIx/AItXVyL9K72r7RXf71WAKpDc3Rrvlq" +
                "OGJLU4p5x23nEab1/AjBJ2xpSjHvuHU+wbSeH7E48159QEMRKc" +
                "9xtZbnuBrhoS/3WtVzjlqUY+lRj+FRauvkXqR3jfE95jE8Rm2d" +
                "3Iv0Lu8r/6WhGL8EpAaCUZMjreq5rKnPM8KX++xPe47r83xU7u" +
                "7SVPEraXyf71RnG98vzWbfL82OtvNr6Xnz7xqKcTOQGghGnbxf" +
                "tx5q8ryZHOUPNKQ7AcGox/XAZZ3OsfD+7Pey/Vkt1VBkUgPBqH" +
                "OvVT2vloZnNeSoTUkNBKPOvVb1XOIXGlJJSQ0Eo869VvVc4mkN" +
                "qaSkBoJR516rei7xnIZUUlIDwahzr1U9D88txjm5/KeGIpMaCE" +
                "ade63qOVQ6rm7EEkhtaI52rVltXJHXV+vJckzff67P/Yb4/jM8" +
                "o6HIpAaCUedeq3oOdY7fBX2rVTm7eV/ZNe/FbRqKTGogGHXuta" +
                "rnxW3Vag3Zw6OU63ut5fpeY7336x7DvFb1vFpdbNSQd0lJDQSj" +
                "zr1W9bzYWJUa8i6j1CUwXgtkj9H00l+V8RpT9Rr02+A1dW8NkN" +
                "rQHO2ar4ZTGopMaiAYde61qudQaVvxm/66at3onRtIpstmzVer" +
                "lRqKTGogPNS516qeVyvDUxqy5SmpgWDUudeqnkuc1JBKSmogGH" +
                "Xutarn4eSY89LN1c1kOZLp0v0W6Vyj/tUaikxqIDzUudeqnldX" +
                "ly9oyFn6Bbm3qZEaCEadvN+2HmpyH5Icw3s1BoNJCEY9rtfsD+" +
                "9t/4bCxK8kYHGL/w1Ftqr7N5SLNWRvXBw3A6mBYNTJe7/1UItX" +
                "mKPX/edc4/5zLrv/nBvdfz5wJt9R97u+u99QjmpMRjDqcb1mvz" +
                "za635+T5emilvP8RrXeW+zKPvzIY3JCEY9rtfsSzygIbWU1EAw" +
                "6txrVd9HZ8H7kB+d4dMC9udP/qf3n29oKMp56Q2vgWDUyftT66" +
                "Emn/fk6PUM+7Mzm/dFOZb+pKEYfw6kBoJRJ++U9VCTvz05hps1" +
                "5Jw6AcGox/Wa/eHm8+Wz2eu8tLdLU8Xt58d5qd9vkemvO9ily9" +
                "Y3zuVWjckIRj2u1+yXW8PzGnIHlZIaCEade63quUTrfzSZ+E9N" +
                "sOG8/19N67bH1P/F2eccsucMP+9/0VBkUgPBqHOvVT2XeF1DKq" +
                "/HvUBqIBh18k5Zz/tG8WcNqaSkBoJR516reg610PE5/H7X1afd" +
                "/b99jvZ36Wa35zoPdOmy9Yty+bDGZASjHtdr9iVe05Daa/EwkB" +
                "oIRp28U9ZDTa5HyXG+nD/L/2goMqmBYNS516qeS7yoIZUXZX/W" +
                "SA0Eo07eKeuhJvszOcp/a0g3JTUQjDr3WtVziZc1pPKy3IPVSA" +
                "0Eo07eJ62HmmxnclSXaMjT0yXxMJAaCEadvFPWQ02euZyjx+do" +
                "X5cuW7+ml//QUGRSA8Goc69VPYc6t+/n46/P5hyygPs3rW8q6l" +
                "CUOco0cJDp5J2q3GjlMkdJlS9pyN5ISQ0Eo869VvW8fKlaoiHv" +
                "uESe32ukBoJRJ+9vrYeabGdyLMb9Z3WphiKTGghGnXut6nl1af" +
                "mghuyNCQhGPa7X7JcPni/n5OoCDUUmNRCMOvda1fPqgkV5jvuh" +
                "xmQEox7Xa/bRWfD5/anOz/vxszmHxGc63U+3zp+vaCjKNe4Vr4" +
                "Fg1Mk7ZT3U5NqRHOfN8XmhhqKcPy/0GghGnbxT1kNNzkvJUf5V" +
                "Q/ZMSmogGHXutarnUAvO+/ODc3611jl/ZuOrN2koMqmBYNS516" +
                "qei/ovNqfg1w==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 12, 0, 2, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 14, 0, 15, 0, 0, 0, 0, 0, 0, 0, 2, 0, 16, 17, 0, 3, 0, 4, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 1, 1, 0, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
            final int compressedBytes = 452;
            final int uncompressedBytes = 14785;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW0FywyAMXFMOPnLIA/wUpi/TE/rkjut0SqZ2DBghgdmDJ5" +
                "nExqzErmQSCzjAYsaCDQvM86VZD+TX44fFhPkBwi9ohschpte3" +
                "hL6x8hcg5C9kyYVf2vizJfk3PLPzGDiOfBg/cxI/Ry5u/VzFJM" +
                "kJaeHPZX9YGdczwd18Ff7pJ/byZ8Kmn0Cd9acAJpu/Hf/60ZMX" +
                "/sDNn6aMJsbM/ef/j1j//wyvkorlcHykj8/Nn383hQbU+SzRz/" +
                "XLMulXYvyF9PM4/mn370ji/kf9XNf/dfjXQNn4cerPG/2tMv6A" +
                "bvDVH7aI/8fpH8T0T7Z+kx//cvwz6i+XdBe686dCFPL7p6/4/o" +
                "2K92+t16+WZ/9BWf9PavkfuEP+5sB0EyMfHb/z/Yd68ZN7/kmt" +
                "TsHgppBWEu76hTCgs3656n8757vy+w+1/EPK/+2VMwvsP/Wojp" +
                "TL3zP+1ET/cjX+aeuXYvMn8fdjtpIOVtG/4utPs37VGN8z6udA" +
                "d/17d/6XgSmZRdb6f3RnuvVTXXcaXT8w+HcPz/+Y6i+p9U/M85" +
                "fOf931i4B+KPv/CV//1Xr+tK6/4/nJvf2/hfqN3/9H/onhG/cu" +
                "fsU=");
            
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
            final int compressedBytes = 589;
            final int uncompressedBytes = 3961;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNlM9rE0EUx7/BlthLsBRNIIQEkx6kEjw0thBP4jkFDyp4UL" +
                "H+AAV/FEqr9gdVjAcplEI9liIKItKirdgeemgxWGkRQfx10/b/" +
                "SF9mZ3dnd2c22WRoGnibGXb47Pd933uDMAqII4M+RBBDFknk0Y" +
                "EEjuENLuAcQjiNFhzCCXTiFo4gijTacRQH0IqDSOEM2nAYt8tl" +
                "FCgyFBGKLEWeIoFeenIOrSqci7hXtn7oslaMgzCt4gYHMXomKS" +
                "p61iocihCFoecmHrj0HEfK5qCH5dXN8jppcqyv2ZxJjLg4OSXn" +
                "lA9nCqNqDj9t5cV2bs5ZQw/bRyk4h1acY512cCo+ixzTZ5NDz/" +
                "MGx/SZntxnXGJ5XbHzwlW3P7Ry5nXZ1oN+dvpaucrPzEv5XlX3" +
                "97jh0rPg0XPd6Y/IMX3GF0+9SoY/uCPz2aPnLudsyjhOPTIO7j" +
                "vzkvWhJ6+B6nXHN0/dS0LdB911V3J+qDkYqq0PMSztQ99+xkNF" +
                "vf4p65WuOhePhLx2/Pyhf8Vc+M4p08N2YzXpGfebU75X6Zmobb" +
                "6qzl8YTymvIp/3J3TPP5bNOz+tnvdnuvTI593ts1TPc585DcKZ" +
                "FjmYse/5gJycwHmxD/yZldw/c25OwLo3ouelZy5e6fKH9/Nr/X" +
                "nhbbD+ceh516Ceol0vzNfD4e8XBcc+7HkfLin9+diUPvykibPs" +
                "8XlFcz+v1u3P+j64fz5r8nlDE+erwNnS4w//znfNdf9Zb935+1" +
                "9NrftvTfX6o7w3/jbl3vivibMtcHYBTK8TaQ==");
            
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
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

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
