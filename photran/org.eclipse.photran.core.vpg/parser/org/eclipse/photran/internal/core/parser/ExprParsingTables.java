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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 3, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 45, 0, 46, 5, 7, 47, 48, 49, 50, 11, 7, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 8, 9, 70, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 16, 80, 24, 81, 26, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 29, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 17, 127, 128, 129, 130, 131, 41, 132, 133, 18, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 3, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 15, 16, 17, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 20, 21, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 24, 0, 0, 25, 26, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 29, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 34, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 37, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0 };

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
            final int compressedBytes = 553;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWLtuwzAMPAtCoXby2FEJMvQTuvbPNOYTMuZTa9l5ypaOEm" +
                "0EKMoCfYQVdSLFIyk4BFgDD4coLuAz/vwaPgF67PqTwQF2jzd0" +
                "38AuhA6+9w5nj5/h7/74rMekR6p3y3rEfQ+AGb7ir90AJ+4LBD" +
                "8Bih+Y6f+e8F2E4Kf2oxyQFX6+m3/Mkn8YfuofGh+h/5P4QR6/" +
                "0T/74QwqfK5s38zsBxG+a/xy8dXez/v68vm3iW/L+THPH8vzpy" +
                "2/BP4h+cf55VQ+fxbfedrHoSxkfc7/D/mdSl/DH6PY+M0sm2D8" +
                "trUe1H/F80vtX0/fLZiI/inqHySnz62n8Vkpf7L6BN9C/BX+/d" +
                "f/bb08vxX3l92/sl7S323IX9r6rdUL6pewPn201beEYXL8OtcH" +
                "Gb8m+N5HfP0dH9m/uv6n/fUNfyqhyLDy/v61em3/L51/cv2jqH" +
                "9hFKDob+T+M1vFpzz/4Ej864TzyTb45fORaeInUX6iEH+i19of" +
                "11tYf5nPfS2/MH6j84OUvzL8S/vnvrY/r5x/2t5/XHG+WQOfbH" +
                "6oje9CfdXFV5s/zf3PQ/1V3S/SP0jW2+L6Tfmlon4ZUQO1hG/G" +
                "LxjweZl9dv9WO18dv8r5X7e/yP+2vX6o7yfRq/vX1d5n2u6Hnr" +
                "909tvrY/EuVfCL9H2O9bdo6l9WiE/T+yVeXp+8FB+vb9r+kfGL" +
                "gn/U/mPvoxv3R23v14y/6+sHe//N6n8BTyecLg==");
            
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
            final int compressedBytes = 2498;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWlusXVUVvVrDR9U0gR+jH8YfvVGRCImKKbrO3twWWk3U6I" +
                "dG/eCPJvqlQXkuP4yKMRgjD+UhGo3GGBOFvmhLL1CoSmIMRgxt" +
                "obRCCwjiCxURdI01z9hjzr0P55yW3piembnHHGOOu88+e+33vv" +
                "kNC+WTV5f8fslTK3tlPq1MXz+6NK8p+I2lgwvdJ68q6ikFX5Nf" +
                "Bz66tNSvyq8u09fS0zyNyKtLvql5mjxi3mmMSl4E0oM6v9E72r" +
                "cg8uqSvyl56pifNsY1dbrLWK1XlTylZFnOTivLKcfCQtpX4/60" +
                "L7/ZkFyYbxwzKpb3O031vm4tbcK0bWxqKE6XeNvkC71GdTy3bo" +
                "wqhjEquWah98mr8mf9GOXPDMfIL2c+vX7junbd+LvXaRp5u65b" +
                "TueVU/O0z/JCPsOq0ecXJnxM5TwneZpHEHlT8whq8ohkrJGYp7" +
                "ToSD9A5E2G5H1UdH9xoXrRVdh+RF5d8sy0nzwiGWtLqb4Gmzbu" +
                "+aJJ417yrOnjng4gynIeQE0ekYy1pVRfgzWHEWV/P4yaPCIZ67" +
                "K/H67j0mk9xzOIMs9nUJNHJGNtKdXXYO1aRP5RuxZ13XrXGuZ3" +
                "k9Nlneiku8zjbLLZ+2azOY5Rs9mPEbqDMTqKKGN0NJ+TjpJHJG" +
                "NtKdXXYPk9+dx8Xl6dN5TlT/VXJEPx4mry+ryuTfm9xq2TN+b3" +
                "FW1EV6lTPr8s+yFEGaNDqMkjkrG2lOprY3591m88rz2PlaaR5/" +
                "dHzbCrT0eUc8fpqMmF+QNi7FhK9TVYegBRxuiB/MH0AHlEMtaW" +
                "Un0N1r4dAWSSG3o1OqX62lj9lm0e07bI/Sdty1+FSk++0vvHni" +
                "MIYP6QIbmQjLWlVF8bm7kfbevtR9v8OQ7dwXXIHxBAJrmQLDql" +
                "+tpY/9NuaDew0rQu40ci9wyYPxzW6naPaXvkfael9fI3vT+spQ" +
                "nrs1ZlfS5dPTh3uPWJrq3Pwbfv9Zj2Apn5232npXnzt7x/vB7e" +
                "gQBa5o8azx8zZIchp1RfG3Pr+1ybGor3+0Mtqs2jCGD+uCG5kK" +
                "xzfgIotV7TOkdbA5g/aUguJGNtueC06Jh9Pso3TBv3fL2Ne75A" +
                "+9G0eY4unnQdUlR/j3DxcJ7NnxFAJrmQLDql1vXpHO1bEUAmuZ" +
                "AsOqX62tiEveqMwD69MOWTPzVhD/qFpmJAZrMl+pst1jEvuvLP" +
                "e4+Qd0wd9ysnXIf8XlMxIDMvh+XsOuat547OP+9yLl0z9bh0zX" +
                "A555jntdOuk9GdfH80edxHl0y8l7nkxdl4m78WAWSSC8miU6qv" +
                "m+5XpS0e0xbP854wRluY5qlj1PmDc6vHtNXzfG/faWmeOs/OP+" +
                "/5KO+fb/scLOddHtNdQGZ+uO+0NG9dzs4fnHs8pj3AtGd0Rdoz" +
                "+Paxap4y7lfI33Pu8ph2AdOu0WXUvdNU85R5XiZ/z3mPx3QPMN" +
                "0zytS901TzlHlm+XvOZY9pGZiWR5en5cE8x6p5yjwvl3/2uL/Y" +
                "s4s47lWbuG+m2z2m24HModNSPflXfDl3eEw7gMyh01I9+YPzTo" +
                "/pTiBz6LRUT/7gvMNjugPIHDot1ZM/OHd7TLuBzKHTUj35g/M2" +
                "j+k2IHPotFRP/uDc6THtBDKHTkv15A/Ouz2mu4HModNSPfnH/J" +
                "eaigGZvXl2HfXkP97nivNu89OO8/POc/CLbvGYbom877RUz9eD" +
                "O7jWpobi/f5Qi2q6V1MxILO3nF1HPfnH/FeaigGZvXl2HfXkX7" +
                "lxb9+JAFqW+7jKy31cRXYYckr1tbEV25Zu9ZhujbzvtFTP1865" +
                "2WPaHHnfaamer+s13j8QwPwFQ3IhGWtLqb4Gm+Me4YvzrE9/Hz" +
                "f7/qjpXQdGTpa/4v5+9jPV7b1nQdvDs6Dt4+X8mrvf/AsCmK8y" +
                "JBeSdc6vA6XW+03vuAExHcliR2qdp3PMXp/zffz6bF+GADLJhW" +
                "TRKdXXYOlhBJBJLiSLTqm+rt2fI4BMciFZdEr1de0+hAAyyYVk" +
                "0SnV17V7CAFkkgvJolOqr8FW4pjc/B0BZJILyaJTqq+Nue1qya" +
                "aG4v3+UOurgzPyxnYjK03dM9WN0S3kM9V0EAFkkgvJolOqr9PB" +
                "E/As6OoJ2vGM+1nTx330OQSQSS4ki06pvgZr34UAWpbze+Xl/F" +
                "6RHYacUn0NNroIAWSSC8miU6qvwdLjCCCTXEgWnVJ9DdaegwBa" +
                "mjJ+bzjmdE1y0t2ek88WG48d3sfZm7WRoXi3fXe8r/VVN197Y7" +
                "W+XT/2rdc08r4W1fZtCCCT3NCr0SnV12DpQQSQSS4ki06pvq7d" +
                "xxBAJrmQLDql+trY4Lh0fns+K00jd+8inberz0QAmeSGXo1Oqb" +
                "4Gax5DAMu1zWOeC8k653VN/YXSynWIcyx9CTEdyWJHqu+DDd+h" +
                "MPWeJN/o35CUpZr1DuXlCGC+ypBcSNY5bwJKxTy9Y47rz96z9G" +
                "ZHuP7cMb7+vPlYnlHPd34P71DuQ0xHstiR6vvWmXk933t2EjlZ" +
                "/t5LPMfNuLZZkfX5HcR0JIsdqb5f2fUIIJNcSBadUn3fOjOvQ3" +
                "54zPcLtj5/fEKvP59FAMtx6VnPhWSd8ydAqXV/d4457mF/eqzj" +
                "viLb0hEEMP/MkFxI1jkXmyP6W9TltzvH0pcR05EsdqT6PtjJsm" +
                "/OcVzqPX+NnCxvPTmOS/O+ixz/ut77lsib5QnvIm9ETEey2JHq" +
                "+2DpUQSQSS4ki06pvq7dwf9mMjE1XDro/xMz9uJfmDLHMWT3Me" +
                "/vjyOATHIhWXRK9XXt/hMBzHcYkgvJOuciUKqvq+MoAsgkF5JF" +
                "p1RfG5u1fS5dN21bntT9P+1HvfeBkTe3z3kujfPsvb+KvNk9Yd" +
                "+8GTEdyWJHqu9X9lcEMO81JBeSdc5FoNR6PnKOk+X42fwbAWSS" +
                "C8miU6qva/cJBLCszyc8F5J1zkWg1Lo+veNfCCCTXEgWnVJ9Xb" +
                "tPIoDlGuxJz4VknfPXQKl1OZ2jXYUA5r2G5EKyzrkIlFrvuZxj" +
                "jv2o978PkTe7JuxHf0MAmeRCsuiU6mtjL+35fL7veI4hM7y/nf" +
                "Dbn0MAy/b5nOdCss65CJRax907/ogAMsmFZNEp1de1+zwCWLbP" +
                "5z0XknXO3wGl1uV0jpW4/mxfgQAyyYVk0SnV12DNTYjpSBY7Un" +
                "0f7KQ5Jv8HAWSSC8miU6qvwVbkPu67iOlIFjtSfd86M+/fH5y6" +
                "vx84nmNIPjjV+9CEY8hTCGA5hjzluZCscy4CpdZ90zlOmu3zBQ" +
                "Sw/PYXPBeSdc5FoNT6273jTwggk1xIFp1SfW1s5rg/unACPr15" +
                "HjnWv2/+iwAyyYVk0SnV15X9D5wQS+s=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 12, 0, 2, 0, 0, 0, 0, 0, 13, 0, 2, 0, 0, 0, 0, 0, 14, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 3, 4, 0, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
            final int compressedBytes = 448;
            final int uncompressedBytes = 14785;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW0uOwyAMfTAssmTRA+QoaE7mI8yRR2k6GqomLZAYDPFbRK" +
                "3aBPDnPRtaB3jAYcKMFTPs46VdLhSW65eDwXQD4Q80IWAX5vkt" +
                "YWws9osQ2y+2ko+/tNrPnWl/y7O6AMW+52P/2Q/+8+TT8ucoTE" +
                "ubUP/228jXrA+rZ5jXVDzI3/M/jb7Gn8HKv0C1+LPN1r+hX3c+" +
                "eVo/uNcvKaKJ0fIv+n9L1f/v+Cm5mHfHR/743PYL75bADmad8S" +
                "n845j4J9P/Ffkvzf958/fUYv5aP19Rv+Twl/qfu357w99VxleM" +
                "sX9Qqv9p/Idu+W9c/kz0f0H95bNmITt+KnihvH/6Se/f6PT+rf" +
                "f61fGcPwjr/0ms/RVXiN8S2GF8FJL99/n8oZ7/+t3Rp1YmsLgo" +
                "WjMJd/1CUMisX47q38b9/vzzh1r60Ur/3ZE7Tzh/GpEdqdR+D/" +
                "9TF/3LUf/n5S+lxk/m78dcJR6swn+n559k/qoxfmDkT8Vw/ftw" +
                "+lcAk21F1vpfuzPZ/CmuO02uHxj0e4T9P6b6q1X+E/P6W8e/7P" +
                "qlAX8I+/8JX//Ve/z0zr+6f3Jt/e+hfuPXf42/ZvgFE71+xQ==");
            
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
                "eNrNlM9rE0EUx7+RlthLsFANgSQEkx6kEjw0thBv4jU9CCp4UL" +
                "H+AAV/FEqr9gdVjAcplEI9liIKItKirdgeemgxWGkRQfx10/b/" +
                "SF9mZ3dnd2c22WRoGnibGXb47Pd933uDMAqII4M+RBBDFknk0Y" +
                "EEjuENLuAcQjiNFhzCCXTiFo4gijTacRQH0IqDSOEM2nAYt8tl" +
                "FCgyFBGKLEWeIoFeenIOrSqci7hXtn7oslaMgzCt4gYHMXomKS" +
                "p61iocihCFoecmhlx6jiNlc9DD8upmeZ00OdbXbM4kRlycnJJz" +
                "yoczhVE1h5+28mI7N+esoYftoxScQyvOsU47OBWfRY7ps8mh53" +
                "mDY/pMT+4zLrG8rth54arbH1o587ps60E/O32tXOVn5qV8r6r7" +
                "e9xw6Vnw6Lnu9EfkmD7ji6deJcMf3JH57NFzl3M2ZRynHhkH95" +
                "15yfrQk9dA9brjm6fuJaHug+66Kzk/1BwM19aHeCDtQ99+xkNF" +
                "vf4p65WuOhePhLx2/Pyhf8Vc+M4p08N2YzXpGfebU75X6Zmobb" +
                "6qzl8YTymvIp/3J3TPP5bNOz+tnvdnuvTI593ts1TPc585DcKZ" +
                "FjmYse/5gJycwHmxD/yZldw/c25OwLo3ouelZy5e6fKH9/Nr/X" +
                "nhbbD+ceh516Ceol0vzNfD4e8XBcc+7HkfLin9+diUPvykibPs" +
                "8XlFcz+v1u3P+j64fz5r8nlDE+erwNnS4w//znfNdf9Zb935+1" +
                "9NrftvTfX6o7w3/jbl3vivibMtcHYBui4TlA==");
            
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
