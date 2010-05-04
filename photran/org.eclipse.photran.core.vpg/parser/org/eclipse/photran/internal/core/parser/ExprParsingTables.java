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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 3, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 0, 45, 46, 5, 7, 47, 48, 49, 50, 7, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 11, 66, 67, 68, 69, 8, 70, 9, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 18, 80, 24, 81, 25, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 29, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 16, 127, 128, 129, 130, 131, 41, 132, 133, 17, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
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
                "eNrtWMtuAyEMnEWratvTHnskUQ79hF77ZxzzCTnmU7vso00IMA" +
                "aziirVlfqICxg/xmMwwKE3sBjgZXB49z8/pk+AEYfxYnBCf8QL" +
                "uk/g4FwHO9oBV4uv6e/xvOiHVY9Fj1CPuB7+3BNgpi//azeZ48" +
                "8FnF0M8h+Y5f/u7FuF2E/393JCUpj9N/4xMf8w+5n/QOPD7DtH" +
                "4yeNz6Sf/XOc7pDdf8ivNw/rXZP7bfFLxVfun7r7bfbX5WeL+g" +
                "nvj8f6UdjH1jP/sfrj+HLJ3z9p/3U5Z0BeyPqU/2/qO5SxBD9m" +
                "6f03E9+C4dveelD/Ze+/yXa7rvR+q3+S6wNJ6VPraXyE9aPQ5/" +
                "0nrM9//R/VB/m5V/3vlp+cvynyW49ftD/tXN+C/iXsT291/S3I" +
                "sCg+9jG9k+FrYN/rbN/4ax85v7j/h/z6x/5QXKIDlPL75+r1/F" +
                "g2/6T4o4i/MIhQ8Jtm/L1yvgHOqvmC81+1/Y3mI1OFT6L6RCb+" +
                "RK/df17fo7frfG5L8YXhG50fpPiVwF/Kj8dSfl44/wjff1LzUb" +
                "T+W9h3P5+k+X1ZfCP9VRdfbf1U85/r3fxUn1+EPwjnt8z6XfGl" +
                "oH8ZEYGK2feAL5jss7L9Wf41u18ZvsrxX3e+yP99ff9Q5yfRq/" +
                "lrs/eZuvzQ45du//r+mM2lAnyRvs8xfosq/tIgPlXvl3h6f7JS" +
                "+3h/0/JHhi8K/FH7r8X7qYIf1b1fM/wu7x/s/Tep/wYvtJwu");
            
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
            final int compressedBytes = 2670;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWl2sHVUVPlwID1XThPCiMdEnvBELaUMUyK3uM+Ppb0w06o" +
                "NGffCNJvqkEcvf9sGAGIMxIii0otFojDFR6O3Pvbf3tvRHaGIM" +
                "Rom2UIr0D0EUrYj86FrznW+vtWfOmTttuTE9K2u+71vr2/vMmZ" +
                "kzZ+acE9/Zk0dcJvkjycsr9aZ4mSzf0b9ZlsslvzU42kuPONG/" +
                "OV4q+Nb4dtWV683xLbJ8GxzFCxpxmeS7iheoDcHiLDR7cdJ6qM" +
                "UrzBH+pBGXSf5W8vKhvmyI747LK5yDrviE5KWSsp6pJutpjvJK" +
                "jfCH8speD0it0euBxS3Q7CHpyrmOqrbRJl2WBZZAakNzaMYbmi" +
                "7wmPZRhdk+qvjyXu0RJ+IX/T6KX6jvI7+ecUX1fGvKNcNnriGZ" +
                "Lss1aT2dy5ycE4+FXrwarP/l3sgH6pyz6Sqe0YibimeUUxuCUZ" +
                "Nr6pxW847wY424CSj7uYZkFqzonPXaMA5ryPF5WOpVahDjKjBq" +
                "cqRVPZdRLfs9bh693yWvadvv4YiGrOcR5dSGYNTkSKt6Ho4UT2" +
                "vI+/1p2cpVahiCUZPL+z31vG8YZzRkzjNSrVLDEIyaHGlVz4sz" +
                "5ZRG/Gk5JUfsVHXsTgHj9UDU4UMv99Ivs1wHFU5qyPY8KVukSg" +
                "1inAKjJkda1XMZtToWcW1cFtfLM4Xq+QKQGhj7cRDXlCG+Hxq9" +
                "uCFulFqgS/gH4rou55BiW34sFdv8saTd2nn+mIbso2PKqQ3BqM" +
                "mRVvUcytazeg3rynVkOZLpMn4orwGH/RUacVm5Qjm1oYz+sGn2" +
                "kFb1XGKlhsy5UqpVahhW7qTZQ1rV83Jl+KOGHAkpqTXiR8Coc6" +
                "9VPYcSvcNj2EFtHXuEHfHrWqUr3pmPSMdH+7G0o3Ys7fCfR9qt" +
                "nZdOaCgyqTXiR8Goc69VPQ8nij9ryLOlpAaCUedeq3oOlT/KDe" +
                "UGshzl1X0813VX/Jjb7js9hp3U1sm9SPTit/MRyXfQYzioSN7r" +
                "De6ue5FwaNePaNvvw4peK36v8Xnkr0O+y/2etsZ7NRSR8RPQ8Z" +
                "NAeOjLvVb1nKOGYz+IJZDa0BzNmq8WxzUU46eA1EAw6uT9tPVQ" +
                "k2va5Ch7GvIMvfgZIDWwl2nynuuVNV52ulaM97fuo/uwj+Jn+d" +
                "5sm7M/5jqkvzm7R9hcn7P4m4YikxoIRp17rVptz+Qo36MhWyMl" +
                "NRCMOvda1XOoxqu7uqY/32t5xM813pW/tqUpResU0/mIYhp9OL" +
                "TrR3S7R4gzrfv9zsY5+XFbmlK0TlzI1vNxJhzVZ4cb0W09B99p" +
                "W0/t5uvZac572q6TtTvq/mj0fu/fNOZe5qbxqtpr92goMqmBYN" +
                "S516qeF8NXFaY9humQjh5lcV+2j6aZcFX7yI1Ivu0ew3ZqVOKh" +
                "uhcJVzWnG9Hxs+Nwt+Oztp4PewwPK5LLiKfqXiQc1Xq6Ecm3z2" +
                "PYpxj29W+zjvf2b0MfPbhQyXxzHsOcYpjr32Id7+3fgj56cKGS" +
                "+Q54DAcUw4F+tI739iP66MGFSuZb8BgWFMNC/1breG//VvTRgw" +
                "uVzLfbY9itSN6YczfTen5Et+vPEffF7liqaqO+D1mCOcNej2Gv" +
                "Innjte9lWs+PSLUZj2FGkbwx5wzTen5Equ3xGPYokjfm3MO0nh" +
                "+RavMew7wieWPOeab1/IhU2+Ux7FIkb8y5i2k9PyLVZj2GWUXy" +
                "xpyzTOv5Eam232PYr0jemHM/03p+hOAjtjSlmHfcnI8wredHnN" +
                "v3iuf+PrLzfNc5a6/nQY/hQWrr5F6kdzV9w6v8EksgtaE5mjVf" +
                "DYdsaUox77j1PMS0nh8h+KgtTSnmHTfno0zr+RFLs9/L92koIu" +
                "U+rtJyH1chPPTlXqt6zlFLciw95DE8RG2d3Iv0rhG+bR7DtrCt" +
                "3sm9SO/yvuJfGorxK0BqIBg1OdKqnstMXe4Rvtple9p9XJf7o2" +
                "J7m6aKX0vju3xXubP2/dLO7PulncP1/Ea63/y7hmK8C0gNBKNO" +
                "3m9aDzW530yO4n4N6Y5BMOpRPXCZ0zkW357dHrY9y4s0FJnUQD" +
                "Dq3GtVz8uLwlMactSmpAaCUedeq3ou8SsNqaSkBoJR516rei7x" +
                "pIZUUlIDwahzr1U9lzimIZWU1EAw6txrVc/DsaU4Jxf/1FBkUg" +
                "PBqHOvVT2HSsfVAEsgtaE5mrV6tfaJvLHcSJZj+k51Y+43xHeq" +
                "4aiGIpMaCEade63qeTj6BnwXdHejcm77/Zq2/d6/UUORSQ0Eo8" +
                "69VvW8f2N5rYZs4WHK53ul5fO9wmrrVz2Gea3qeXltOK0hWzgl" +
                "NRCMOvda1fNwulytIc8yTF0C4/VA9hh1L/3l6nidqWoG/T2uX/" +
                "X6QGpDczRrvtr/koYikxoIRp17reo5VFpX/F61tlw7fOYakumy" +
                "XvPV8ioNRSY1EB7q3GtVz8urwhMastdSUgPBqHOvVT2XOKUhlZ" +
                "TUQDDq3GtVz6Fq56X15XqyHMl06X6LdK5hf5WGIpMaCA917rWq" +
                "5+Wq4pSGnKVPybVNhdRAMOrkvdd6qMl1SHIMbtfo9cYhGPWoXr" +
                "0/uL35GwoTv5KAxS3+NxRZq/bfUCY0ZGtMxLuA1EAw6uTdaj3U" +
                "4hXm6HT9WfsuvZjJrj9nhtefD5zNd9TdPt/dbyiPaYxHMOpRvX" +
                "ofnUWv53e1aar4w/P8jGu9tlmS7fl9jfEIRj2qV+9L3KchtZTU" +
                "QDDq3GtV30dn0euQn5zl3QK258/e0OvPlzUU5bz0stdAMOrk/b" +
                "n1UJP3e3J0uof9xdnt9yU5lk5oKMZfAqmBYNTJO2k91OS1J8fg" +
                "Dg05p45BMOpRvXp/cMeF8t7sdF6abdNUcfuFcV7q9ltkenULbb" +
                "po/HJTbNEYj2DUo3r1frElHNeQK6iU1EAw6txrVc8lGv/RZOKf" +
                "mmCDo/6/mtZtjqn+xdnlHDJ/lu/30xqKTGogGHXutarnEi9pSO" +
                "WluAdIDQSjTt5J63nfME5qSCUlNRCMOvda1XOoxY7Pwb1tnz7N" +
                "7v/tfbS7TRe7O32W1uecb9NF45ew4gGN8QhGPapX70u8qCG1F+" +
                "NBIDUQjDp5J62HmnweJceFcv4s/qOhyKQGglHnXqt6LvGshlSe" +
                "le1ZITUQjDp5J62HmmzP5Cj+rSHdlNRAMOrca1XPJZ7TkMpzcg" +
                "1WITUQjDp5f2M91GQ9k6O8WEPuni6OB4HUQDDq5J20Hmpyz+Uc" +
                "Hd5Hc226aPyLoviHhiKTGghGnXut6jnU+X0/Hx87l3PIIu7fNV" +
                "77KxqKcny+4jUQjDp5J62Hmuz35Cj+oiHdlNRAMOrca1XPJV7T" +
                "kMprcnxWSA0Eo07e31sPNVnP5FiK68/yEg1FJjUQjDr3WtXz8p" +
                "Jiq4a8ijEIRj2qV+8XWy+Yc/KrGopMaiAYde61qufFq0tyH/cD" +
                "jfEIRj2qV++js+j9+xOt7/cj53IOiUdb3U82ziHPayjKOeR5r4" +
                "Fg1Mk7aT3U5L2ZHBfM8fm6hqK89te9BoJRJ++k9VCT154cxV81" +
                "pJuSGghGnXut6jnUovv9eO+8H405T5zd+OK/GopMaiAYde61qu" +
                "ei/gelLEwX");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 6, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 0, 12, 2, 0, 0, 0, 0, 0, 13, 2, 0, 0, 0, 0, 0, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 3, 0, 4, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 1, 1, 0, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
            final int compressedBytes = 450;
            final int uncompressedBytes = 14785;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW1tywyAMXFM+/MlHDuCjMD2ZjtAjd1KnUzL1AzACgbUfnm" +
                "QSx0Er7Uo4sYADLGYsWLHAvB6a54H88/hhMWF+gPALmuGxi+n9" +
                "KWFsPOMXIIxfGCUXvmmNny0Zf8OzOg/FPvMhf+aEP0curn6uYm" +
                "oZEzHV7s5flMGf513qDarwTz+xxd+EVT+BOvVXAKbb+G/4348e" +
                "vcUfZ/GXlNHEGLl//v+I9f/P8FNSsexeH+nX546fP1oCO5h1ws" +
                "Xol2XSr0T+G+knH/9p63eUs37tn3v0Hzn6oyjB/5F+Hehv1PkK" +
                "RSn/yfH/OP1Dtv7J9e/e9TuS/4z+yyV9C9n5U4GF/PnpK35+o+" +
                "LzW+/zj+W5/yBs/iex8VfcIX9zYIbhyEfzd77/X4+/dvuf1OsS" +
                "DG6K1krC3b8QFDL7l6v+t3G+K3//oZZ/tPJ/e+XMAvefRlRHyo" +
                "3fi3/qYn65yn9a/VJs/iT+fsxW0sEq+le8/iTrV43re0b9VAw3" +
                "vw/nfxmYkqPI2v/rdCZbP8VNp9H9A4N/j7D/x9R/tap/Yl5/6/" +
                "yX3b800A9h/z/hm796z5/e9Vf3T+7t/z30b/z+r/nXDN/E337F");
            
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
                "eNrNlM9rE0EUx7/BlthLsBRNICQQkx6kEoQSW8hRj5KCBxU8qF" +
                "h/gII/Cm1F+4MqxoMUSqEeSxEFEWnRVmwPPbQYrLSIIP66aft/" +
                "pC+zs7uzuzObbDI0DbzNDDt89vu+771BGAXEkUEfIoghiyTy6E" +
                "ACx/AGF3AOIZxCCw7hBDpxC0cQRRrtOIoDaMVBpHAabTiM2+Uy" +
                "ChQZighFliJPkUAvPTmHVhXORdwrWz90WSvGQZhWcYODGD2TFB" +
                "U9axUORYjC0HMTQy49x5GyOehheXWzvE6aHOtrNmcSIy5OTsk5" +
                "48OZwqiaw09bebGdm3PW0MP2UQrOoRXnWKcdnIrPIsf02eTQ87" +
                "zBMX2mJ/cZl1heV+y8cNXtD62ceV229aCfnb5WrvIz81K+V9X9" +
                "PW649Cx49Fx3+iNyTJ/xxVOvkuEP7sh89ui5yzmbMo5Tj4yD+8" +
                "68ZH3oyWuget3xzVP3klD3QXfdlZwfag6Ga+tDPJD2oW8/46Gi" +
                "Xv+U9UpXnYtHQl47fv7Qv2IufOeU6WG7sZr0jPvNKd+r9EzUNl" +
                "9V5y+Mp5RXkc/7E7rnH8vmnZ9Wz/szXXrk8+72Warnuc+cBuFM" +
                "ixzM2Pd8QE5O4LzYB/7MSu6fOTcnYN0b0fPSMxevdPnD+/m1/r" +
                "zwNlj/OPS8a1BP0a4X5uvh8PeLgmMf9rwPl5T+fGxKH37SxFn2" +
                "+LyiuZ9X6/ZnfR/cP581+byhifNV4Gzp8Yd/57vmuv+st+78/a" +
                "+m1v23pnr9Ud4bf5tyb/zXxNkWOLtKDBN8");
            
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
