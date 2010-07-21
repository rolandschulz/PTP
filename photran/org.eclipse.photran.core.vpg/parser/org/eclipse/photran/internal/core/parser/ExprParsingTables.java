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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 3, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 45, 0, 46, 5, 7, 47, 48, 49, 50, 11, 7, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 8, 70, 9, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 16, 80, 24, 81, 25, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 29, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 17, 127, 128, 129, 130, 131, 41, 132, 133, 18, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 3, 0, 0, 0, 4, 0, 0, 5, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 13, 0, 0, 0, 14, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 15, 16, 17, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 20, 21, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 24, 0, 0, 25, 26, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28, 29, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 34, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 0, 37, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0 };

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
                "eNrtWEFuwyAQHCOroj352COJcugT+jyOfkKOeWrBTtLEBmZhbU" +
                "WqStW09RQ87C6zu8DCozdwsIjDenzGn1/hSfgLh+FscEJ/xBu6" +
                "b+DgfQc3OIuLg/MBH59xzDiWuE3j8R0Bhwlf8dcO8dkQvr2bCc" +
                "UHZv6/J37XQfjT9eM4ITv4/u72MSn7MP7UPtQ/Y9I/qPVPHp/s" +
                "cwx7UPGz5fXNan0v4nfzX86/8v2btvi97r8tPiGxT+X+sT4/An" +
                "5q/rn4IOeP68u5vP8sv8v8bovyIPNz9n8438sx1OjHNPr4YdJL" +
                "MH3bGwe1X3H/0vVvu+8SS0T7FPGHkcNz86l/Njo/WXzBL+F/hX" +
                "3/8b+Ny8+3In7L8cfrN50+KHGan3Y+34L8JcxPH235baEwOX1d" +
                "416mrwt+7xO/4ZcfeX91/l/W13f+y+GLCiuv71+LV9f/tf0VqR" +
                "9F9QuTCEV9s1n9vlf/g5HY1wr7k334y/sj06RPovOJgv8Jrl1/" +
                "mt+jd9f+3NXqC9M32j9I9Sujv7R+Hmrr88r+p+3+xxb7my34yf" +
                "qHWv8m8qvOv9rz01z/PORfVXyR+kEyvy/O31VfKvKXERVQKX4r" +
                "fUHg52Trs/jbbH91+irXf937Rfbv2/OHOj4Jrq5fN7ufaYsPvX" +
                "7p1m/Pj8VYqtAX6f0cq2/RVL9s4J+m+0u8PD85KT+e37T1I9MX" +
                "hf6o7cfuR3euj9rur5l+1+cPdv+bxX8A9eG4Nw==");
            
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
            final int compressedBytes = 2500;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWlusHWUZPVKoetA+wJPGxPiiJyaGhGJCTYA5MxxpgZa26o" +
                "NGfPCNJvqkQbn+PvggxoBGE28UrFSIMSYKtIX20NPrUUmM0Yjp" +
                "hYsP0FNREUG84eVf/7fXrO+b2e6923Jiur98s761vrXnzJ5/7n" +
                "PSO6byJ03nvC/nBYWdny7M07fP3pJWZfzK3NJU+0krsroy41vS" +
                "28Bnb8n1m9Kb8/St9NSvINJ0znfVr5BHTHuMUUkzQHpQp3d6R3" +
                "MxIk3n/EXOCwb8wgGuKtN5Y6VekXNlzrycrZaXU46pqepoiSeq" +
                "o+ndhuTCdPeAUbF8wmmqj7ZraQumzTqbGorTJd6sSzd4jepgbu" +
                "0YFQxjlHPVVOeTVqRP+zFKn+qPkV/O9J7yF9c36wd/e72mkTfr" +
                "2+V0Xjk1T/ssTKWLrJr97NSQj6mc5zBP/TwibamfR00ekYw1Ev" +
                "OUFh3VdkTaYkjeRUX7jRvUi67MjiHSdM6Lq2PkEclYW0r1Ndio" +
                "cU83Dhv3nKtHj3t9EpH3zZOoySOSsc775kl9N9Zg1XFE/u3HUZ" +
                "NHJGNtKdXXYPWriLycr6Imj0jG2lKqr8Ga8kkPNA3qsvU2hul9" +
                "5HRZJzrpzvNYQzZ+36z3xDGq9/gxQrc7RtUJRF6fJ9Jl1QnyiG" +
                "SsLaX6GixdkZq0Lk2nq/PyX1V+xVWG4tlVp/enrKfLjVsnXZOu" +
                "zdosXbmu0tq87EuIPEZLqMkjkrG2lOrruj3PcH2Wv3hdcx0rTS" +
                "NP66Nm2NaXIPK54xLU5MK0UYwdS6m+Lt1LEXmel6ImF3qVHUup" +
                "vgarjiCAaZMhuZCMtaVUXxsryi6P1a7I/afalb4IlZ50h/eHLX" +
                "nUNr+3s83v9ecjdHvb/HMIYPqAIbmQjLWlVF+D1b9DAJnkQrLo" +
                "lOprY91Ps6nZxErT8rs/FLlnwPTBsOYf8Vg9EnnXaWm99FXvHz" +
                "dGpcpjNLe1d+5wY4SujVHvry96rBaBzPTNrtPSvOkb3j9YD5ch" +
                "gJbpw8bTRwzZYcgp1dfG3Pq+xqaG4t1+X4tq/XsEMH3UkFxI1j" +
                "qvB0ot17TO0bweAUwfMyQXkrG2lOprsPHno/TtUeOevmXjnj6u" +
                "fXPUPGdvGnYdklV/j3BTf571XxFAJrmQLDqllvXpHM1qBJBJLi" +
                "SLTqm+NjZkr7oosE9OjfikTwzZg36iqRiQWc9Hfz1vHfOiK/+k" +
                "9whp98hxv2PIMfk3mooBmWkhLGfbMW85d7T+SZdz7p6Rx6V7+s" +
                "s5wTzvHXWdjO7w+6Ph4z5789B7mZv/Nxts81sRQCa5kCw6pfq6" +
                "bo/g1Q6P1Q7P08EwRjuY5ilj1PqDc6fHaqfn6fGu09I8ZZ6tf9" +
                "LzUTo22fbZW86DHquDQGZ6puu0NG9ZztYfnAc8VgeA1YHZ26l7" +
                "p6nmyeN+u/wd57zHah5Yzc/eWs335jlQzZPneav8Hedhj9VhYH" +
                "V4NlH3TlPNk+eZ5O84FzxWC8BqYfa2aqE3z4FqnjzP2+TvOB/z" +
                "WD0GZPadlurJP+n155D7YrctFW3485BlmGe122O1G8jsOy3Vkz" +
                "8493us9gOZfaelevIH516P1V4gs++0VE/+4NznsdoHZPadlurJ" +
                "H5yPeqweBTL7Tkv15A/OPR6rPUBm32mpnvzBechjdQjI7Dst1Z" +
                "N/wH+qqRiQ2Zln21FP/tN9rngm+xGP85POs/eLHvRYPRh512mp" +
                "nq97d3BX29RQvNvva1GtHtdUDMjsLGfbUU/+Af+ZpmJAZmeebU" +
                "c9+Zdv3JvLEUDLfB9XeL6PK8gOQ06pvja2bNvSQx6rhyLvOi3V" +
                "87VzPuyxejjyrtNSPV+Xa7x/IYDpc4bkQjLWllJ9DTbBPcLnJ1" +
                "mf/j5u/P1R3TlvR06WvuC+P/6Z6kLn+dJCeL60MFjOL7n7zb8h" +
                "gOlOQ3IhWeu8Cyi13G96x3cRo5EsdqSWeTrH+PU52cevz+YNCC" +
                "CTXEgWnVJ9DVY9gwAyyYVk0SnV16X7YwSQSS4ki06pvi7dpxBA" +
                "JrmQLDql+rp0f4sAMsmFZNEp1ddgy3FMrv+JADLJhWTRKdXXxt" +
                "x2da1NDcW7/b7WVXtn5M3NZlaaumeqm6NbyGeq1dMIIJNcSBad" +
                "Un1t7AyfBX1tiHY647569LjPfgYBZJILyaJTqq/BmisQQMt8fi" +
                "88n98LssOQU6qvwWZvRACZ5EKy6JTqa7DqJALIJBeSRadUX4M1" +
                "VyKAlqYM3hsOOF3DnHQ3V6Y1YoOxw/Xj2tJdayjebt8t72pd1c" +
                "3X3ulvaDYMfBs0jbyrRbV5LwLIJDf0anRK9TVY9SQCyCQXkkWn" +
                "VF+X7hICyCQXkkWnVF9XS0OPSxubjaw0jdy9i3Tetl6DADLJDb" +
                "0anVJ9DVa/gADma5sXPBeStc6vA6WW6xDnmLsLMRrJYkeq74P1" +
                "36Ew9Z4k3e3fkOSlGvcO5Y0IYLrTkFxI1jq3AqVint4xwfXn/s" +
                "715/5w/bl/cP1576k8o57s/B7eoRxBjEay2JHq+/WRia7nO89j" +
                "IidL287wHDfm2mZZ1uf3EKORLHak+n5h2xBAJrmQLDql+n69ba" +
                "LrkPtP+X7B1uf3X9NnAiWAed8MXEjWOn/QuO+izvumZ+PvYX94" +
                "quO+LNvSHxDA9CNDciFZ65wBSi3HOueY+zJiNJLFjlTfBztb9s" +
                "0Jjkud9y2Rk6WdZ8dxadJ3kYNft9j57YujukW7DzEayWJHqu+D" +
                "Vc8igExyIVl0SvV16fb+N5OJqeHckv9PzNiL3zBlgmPI3lPe3/" +
                "+EADLJhWTRKdXXpftvBDDtMyQXkrXOGaBUXxfHHxFAJrmQLDql" +
                "+trYuO1z7jujtuVh3f/TftR5NxN5fWjCc2mcZ+f9auT14SH75v" +
                "2I0UgWO1J9v7C/I4Bp0ZBcSNY6Z4BSy/nIOc6W42fzOgSQSS4k" +
                "i06pvgarX0QA8/p80XMhWeucAUot69M7/oMAMsmFZNEp1del+x" +
                "ICmO8NX/JcSNY6fw6UWpbTOZppBDAtGpILyVrnTFPGVFq+rnOO" +
                "Cfajzv9oRN7tFu0fCCCTXEgWnVJ9bezMns+nX57OMWSM91dDnl" +
                "ScgwDmMTrHcyFZ65wBSi1j5Bz1nxFAJrmQLDql+hqsORcBzPcI" +
                "53ouJGudvwZKLcvpHMtx/dmcjwAyyYVk0SnV12D1dsRoJIsdqb" +
                "5fbz+LjskrEEAmuZAsOqX6GmxZ7uMeQIxGstiR6vvWGXv//uTI" +
                "/f346RxD0tMjvU8NOX6+jADmc9zLngvJWucMUGo5dzjHWbN9no" +
                "cA5uPneZ4LyVrnDFBqOS45R/0XBJBJLiSLTqm+NjZ23J+deg0+" +
                "nXk+d6rfb1YigExyIVl0SvV1Yf8Fjxy1sw==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 12, 0, 2, 0, 0, 0, 0, 0, 13, 0, 2, 0, 0, 0, 0, 0, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 3, 0, 4, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
            final int compressedBytes = 444;
            final int uncompressedBytes = 14785;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW0tywyAMfaYsvGSRA/goTE+mI/TIHdfplExth59AfN7Ck0" +
                "yCwU/SkwSJBgygsWLDgQ3q+VLtF7L79UNjwfoA4Re0wuISy+tb" +
                "Qt/Y+XPg8ueyZNwvHfzpnPwrnqezmLi2vGs/9cZ+hkxU/LQFqs" +
                "9fHpjoD4tHmBk8Cv/0E2f+s+DQT6Ck/yRBVePvJH/96NELf+Dm" +
                "T5JHEyPz//L/wzf/f7p3CcV2OT/C5+fmz949AjuYdcL46Jdm0q" +
                "9A+1fSz2v7h63fUI31z/p5xPwlR78mcvjPnf7d6LfX+Im5f5CS" +
                "//30D8Pqn1z99bR/RP1lglYh238KWCG+f/ry798oe//Wev2qec" +
                "4fhPX/JJb/iRH8NwaqGxtZb/u9P38oZ792d/SpFgUKg6K2knDX" +
                "L4QJmfVLav47GW/ynz+Uyh+18r9OGZnh/KlHdaRY/p72pyb6l1" +
                "T7h8Uv+fpP4O/HdCEdLKJ/2eNPsn6VmN8y6udEd/17d/kvAksw" +
                "i6z1/+zOZOunuO7Uu35gyN897P8x1V+14p+Yn7+2/8uuXyroh7" +
                "D/n/D1X637T+v6O/dPxs7/LdRv/Pl/+l81fAMcfX7F");
            
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
                "eNrNlM9rE0EUx7+RlthTLFQTCAnBpAepBA+NLcSjXlPwoIIHFe" +
                "sPUPBHobRqf1DFeJBCKdRjKaIgIi3aiu2hhxaDlRYRxF83bf+P" +
                "9GV2dnd2d2aTTYamgbeZYYfPft/3vTcIo4A4MuhDBDFkkUQeHU" +
                "jgGN7gAs4hhNNowSGcQCdu4QiiSKMdR3EArTiIFM6gDYdxu1xG" +
                "gSJDEaHIUuQpEuilJ+fQqsK5iHtl64cua8U4CNMqbnAQo2eSoq" +
                "JnrcKhCFEYem5iyKXnOFI2Bz0sr26W10mTY33N5kxixMXJKTmn" +
                "fDhTGFVz+GkrL7Zzc84aetg+SsE5tOIc67SDU/FZ5Jg+mxx6nj" +
                "c4ps/05D7jEsvrip0Xrrr9oZUzr8u2HvSz09fKVX5mXsr3qrq/" +
                "xw2XngWPnutOf0SO6TO+eOpVMvzBHZnPHj13OWdTxnHqkXFw35" +
                "mXrA89eQ1Urzu+eepeEuo+6K67kvNDzcFwbX2IB9I+9O1nPFTU" +
                "65+yXumqc/FIyGvHzx/6V8yF75wyPWw3VpOecb855XuVnona5q" +
                "vq/IXxlPIq8nl/Qvf8Y9m889PqeX+mS4983t0+S/U895nTIJxp" +
                "kYMZ+54PyMkJnBf7wJ9Zyf0z5+YErHsjel565uKVLn94P7/Wnx" +
                "feBusfh553Deop2vXCfD0c/n5RcOzDnvfhktKfj03pw0+aOMse" +
                "n1c09/Nq3f6s74P757Mmnzc0cb4KnC09/vDvfNdc95/11p2//9" +
                "XUuv/WVK8/ynvjb1Pujf+aONsCZxfkKxN0");
            
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
