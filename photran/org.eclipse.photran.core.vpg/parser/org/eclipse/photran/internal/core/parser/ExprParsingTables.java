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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 3, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 0, 45, 46, 5, 7, 47, 48, 49, 50, 11, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 7, 66, 67, 68, 69, 8, 70, 9, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 16, 80, 24, 81, 25, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 28, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 17, 127, 128, 129, 130, 131, 41, 132, 133, 18, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
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
            final int compressedBytes = 557;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWMtuwyAQHCOrcnvysUcS5dBP6LV/xjGfkGM+tcbOo8bALC" +
                "xppKpbqY9sgX2ws7NggENvYDHAy+Dw7n9+TJ8AI3bjyeCAfo8X" +
                "dJ/AzrkOdrQDzhZf09/jca3HokeoH+J6+HMPgJm+/K/dZI4/F3" +
                "B2Mch/YJb/W9l3EWI/3d/LAUnh/t3iY2LxYfbT+ND8COMf5A/y" +
                "/M3x2U8+VNp3i/96vRPdD2bfNX+p/Grv53193v+6+9nAf3b+Jj" +
                "7Y1lcL+1PxIfXH8eUk8n9r33k5Z0BeyPpU/H/UdyhjCX7M0vtv" +
                "Jr4Fw7dH60Hjl/X/KlfvulL/LvFJrg8kpU+tV9eXVh/YV5b/f/" +
                "0f0Ofrp1F9K+4vqW/K337FP86PzFPqW9C/hP3pra6/BQgTxcc+" +
                "pncyfA3se53tG+/2kfOL+3/Ir2/2h+KyCCvn98/Va/m/dP5J8U" +
                "MRf2EQoeA38viZ2vjn5xscVfMF579q+xvNR6YKn0T1iUz+iV67" +
                "/7y+R28v87ktxReGb3R+kOJXAn8pPx5L+Xnh/FP3/jNk55sW9q" +
                "3nkzS/L8tvpL/q8qutn2r+c17NT/X3i/AH4fyWWf9QfCnoX0ZE" +
                "oGL2bfAFk31Wtj+7f838K8NXOf7rzhfFv6/vH+r7SfRq/trsfa" +
                "bufujxS7d/fX/M3qUCfJG+zzF+iyr+0iA/Ve+XeHp/slL7eH/T" +
                "8keGLwr8Ucevxfupgh/VvV8z/C7vH+z9N6n/BseFnC4=");
            
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
            final int compressedBytes = 2501;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWl2sHWUVvVrDQ9U0ITwYY0x80Ru1EglRMaDfmfG20GqiUR" +
                "806oNvNNEnDcrv54NRMQZj5EehiEajMcZEoT+XUnoLhaokxmDE" +
                "lAKFCi0giH+oiKDf+vZZs/aeOZxzWnpjenb2rL3WXnfOnPnmf2" +
                "5+3UL55LUlv1/y1Mpenk8r09eOLs7rCn5j6dBC98lrinpKwVfl" +
                "14CPLi71K/Iry/TV9DRPIfLakm9oniKPmG8xRiUvAulBnV/vHe" +
                "2bEHltyd+UPHXMTxvjujrdbazWa0qeUrIsZ6eV5ZRjYSHdW+Oe" +
                "dG9+oyG5MG8dMyqW9zhN9b3dWtqCadvY1FCcLvG2yed7jep4bt" +
                "0YVQxjVHLdQu+T1+TP+jHKnxmOkV/OvL5+44Z2w/i7N2gaebuh" +
                "W07nlVPztM/KQj7dqtHnFyZ8TOU8J3mahxF5S/MwavKIZKyRmK" +
                "e06Eg/QOQthuR9VHR/cb560VXYQUReW/KMdJA8IhlrS6m+Bps2" +
                "7vmCSeNe8szp457uQ5TlvA81eUQy1pZSfQ3WHEaU/f0wavKIZK" +
                "zL/n64jkun9RxPI8o8n0ZNHpGMtaVUX4O1ZyPyj9qzUdet92zD" +
                "/E5yuqwTnXSXeZxFNnvfbLbFMWq2+TFCtz9G+Zzc5nPz2nxe+a" +
                "5UvzEZihfXKC/lDW3K7zJunbwpby5a5yr1u/PGMl5HEWXcj+b3" +
                "pqPkEclYW0r1NVjzEKKM0UOoySOSsbaU6mtjfn3WX3Fuey4rTS" +
                "PP74uaYVevR5Rzx3rU5ML8fjF2LKX6GiwdQJT1eSB/IB0gj0jG" +
                "2lKqr8HatyKATHJDr0anVF8bq9+y02PaGbn/pJ35q1DpyZd7f9" +
                "iSp23zO3vb/E5/PkJ3cFw6ggDmDxqSC8lYW0r1NVjzBwSQSS4k" +
                "i06pvjZWv2XZY1qOPKzPZab18je9/4U+7aZ2EytN699/OHLPgP" +
                "lDs8aoVmWMlq4cnDvcGKFrYzT4Rfs9pv1AZv5232lp3vwt7x8v" +
                "89sQQMv8EeP5o4bsMOSU6mtjbt28x6aG4v3+UItq8wgCmD9mSC" +
                "4k65wfB0qt17TO0dYA5k8YkgvJWFsuOC06Zp+P8nXTxj1fa+Oe" +
                "P+nOR1PmObpw0nVIUf09woXDeTZ/RgCZ5EKy6JRa16dztG9GAJ" +
                "nkQrLolOprYxOOfKcH9ulpe3L+1IQ96BeaigGZzfbob7Zbx7zo" +
                "yj/vPULeNXXcL59wTP69pmJAZl4Jy9l1zFvPHZ1/3uVcumrqce" +
                "mqCddLs+d59bTrZHQn3x9NHvfRRRPvZS56YTbe5q9GAJnkQrLo" +
                "lOrrpvtVabvHtN3zvC+M0XameeoYdf7g3OEx7fA839V3Wpqnzr" +
                "Pzz3s+ygfn2z4Hy3m7x3Q7kJkf7DstzVuXs/MH5z6PaR8w7Rtd" +
                "lvYNvn2smqeM+2Xy95y7PabdwLR7dAl17zTVPGWel8jfc97pMd" +
                "0JTHeOMnXvNNU8ZZ5Z/p5zxWNaAaaV0aVpZTDPsWqeMs9L5Z//" +
                "WnHCPawb96pN3DfTrR7TrUDm0Gmpnvyrvpy3eUy3AZlDp6V68g" +
                "fnLo9pF5A5dFqqJ39w7vGY9gCZQ6elevIH516PaS+QOXRaqid/" +
                "cN7sMd0MZA6dlurJH5y3eEy3AJlDp6V68gfnHR7THUDm0Gmpnv" +
                "xj/ktNxYDM3jy7jnryH+9zxXm3+WnH+XnnOfhFN3pMN0bed1qq" +
                "5+vB3VZrU0Pxfn+oRTXdpakYkNlbzq6jnvxj/itNxYDM3jy7jn" +
                "ryr964t29HAC3LfVzl5T6uIjsMOaX62tiqbUs3eUw3Rd53Wqrn" +
                "a+fc5jFti7zvtFTP1/Ua7x8IYP6CIbmQjLWlVF+DzXGP8MV51q" +
                "e/j5t9f9T0rgMjJ8tfcX8/+5nqcu/50nJ4vrQ8Xs6vufvNvyCA" +
                "+QpDciFZ5/w6UGq93/SO6xDTkSx2pNZ5Osfs9Tnfx6/P9iUIIJ" +
                "NcSBadUn0Nlh5EAJnkQrLolOrr2v05AsgkF5JFp1Rf1+4DCCCT" +
                "XEgWnVJ9XbsPIYBMciFZdEr1NdhqHJObvyOATHIhWXRK9bUxt1" +
                "0t2dRQvN8fan11cEbe3G5mpal7/rk5uoV8/pkOIYBMciFZdEr1" +
                "dTp0Ap4FXTlBO55xP3P6uI8+hwAyyYVk0SnV12DtOxBAy3J+r7" +
                "yc3yuyw5BTqq/BRhcggExyIVl0SvU1WHsOAmhpyvgd35jTNclJ" +
                "d3tOPktsvJ7xPm5UuyND8W5b7Hhfi2p6DAFkkgvJolOqr425Zb" +
                "U3VhvbjePv3qhp5H0tqu1bEEAmuaFXo1Oqr8HS/Qggk1xIFp1S" +
                "fV27jyKATHIhWXRK9bWxwXHpvPY8VppG7t5FOm9Xn4EAMskNvR" +
                "qdUn0N1jyKAJZrm0c9F5J1zmua+gullesQ51j6EmI6ksWOVN8H" +
                "G75DYeo9Sd7q35CUpZr1DuWlCGC+wpBcSNY5rwdKxTy9Y47rz9" +
                "6z9GZXuP7cNb7+vOFYnlHPd34P71DuRkxHstiR6vvWmXk933t2" +
                "EjlZ/t6LPMfNuLZZlfX5HcR0JIsdqb5f2bUIIJNcSBadUn3fOj" +
                "OvQ354zPcLtj5/fEKvP59BAMtx6RnPhWSd8ydAqXV/d4457mF/" +
                "eqzjvirb0hEEMP/MkFxI1jkXmyP6W9TltzvH0pcR05EsdqT6Pt" +
                "jJsm/OcVzqPX+NnCzvODmOS/O+ixz/ut77lsiblQnvIrcipiNZ" +
                "7Ej1fbD0CALIJBeSRadUX9fu4H8zmZgaLh3y/4kZe/EvTJnjGL" +
                "LnmPf3xxBAJrmQLDql+rp2/4kA5r2G5EKyzrkIlOrr6jiKADLJ" +
                "hWTRKdXXxmZtn0vXTNuWJ3X/T/tR731g5M2tc55L4zx778Qib/" +
                "ZM2DdvQExHstiR6vuV/RUBzPsNyYVknXMRKLWej5zjZDl+Nv9G" +
                "AJnkQrLolOrr2n0cASzr83HPhWSdcxEota5P7/gXAsgkF5JFp1" +
                "Rf1+4TCGC5BnvCcyFZ5/w1UGpdTudo1yCAeb8huZCscy4CpdZ7" +
                "LueYYz/q/e9D5M3uCfvR3xBAJrmQLDql+trYi3s+n+8+nmPIDO" +
                "9vJ/z2ZxHAsn0+67mQrHMuAqXWcfeOPyKATHIhWXRK9XXtPocA" +
                "lu3zOc+FZJ3zd0CpdTmdYzWuP9uXIYBMciFZdEr1NVhzPWI6ks" +
                "WOVN8HO2mOyf9BAJnkQrLolOprsFW5j/suYjqSxY5U37fOzPv3" +
                "+6fu7/cdzzEkH5rqfWDCMeRJBLAcQ570XEjWOReBUuu+6Rwnzf" +
                "b5PAJYfvvzngvJOuciUGr97d7xJwSQSS4ki06pvjY2c9wfWTgB" +
                "n948jxzr3zf/RQCZ5EKy6JTq68r+B0LmTCI=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 0, 12, 2, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 14, 0, 15, 0, 0, 0, 0, 0, 0, 0, 2, 0, 16, 17, 0, 3, 0, 4, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 1, 1, 0, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
                "eNrtW0FywyAMXFMOPnLIA/wUpi/TE/rkjut0SqZ2DBghgdmDJ5" +
                "nEIazErgSJBRxgMWPBhgXm+dCsF/Lr9cNiwvwA4Rc0w+MQ0+tT" +
                "Qt9Y+QsQ8hey5MI3bfzZkvwbntl5DBxHPoyfOYmfIxe3fq5iku" +
                "SEtPDnsl+sjOuZ4G6+Cv/0E3v5M2HTT6DO+lMAk83fjn/96MkL" +
                "f+DmT1NGE2Pm/vP/R6z/f4afkorlcHykj8/Nn383hQbU+SzRz/" +
                "XLMulXYvyF9JMv/mnzd5Qz/1E/1/V/Hf41UDZ+nPrzRn+rjD+g" +
                "GzX9J8f/4/QPYvonW7/Jj385/hn1l0v6Frrzp0IU8vunr/j+jY" +
                "r3b633P5bn/EFZ/09q+R+4Q/7mwHQTIx8dv/Pzh3rxk9v/pFan" +
                "YHBTSCsJd/1CGNBZv1z1v537Xfnzh1r+IeX/9sqdBc6felRHyu" +
                "XvGX9qon+5Gv+09Uux+ZP4+zFbSQer6F/x9adZv2qM7xn1c6C7" +
                "/r07/8vAlMwia/0/ujPd+qmuO42uHxj8u4f9P6b6S2r9E/P8pf" +
                "Nfd/0ioB/K/n/C13+1nj+t6+/YP7m3/7dQv/H7/8g/MXwDRy5+" +
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
                "eNrNlM9rE0EUx7+RlthLsBQNIYQEkx6kEoQSW4gn8ZyCBxU8qF" +
                "h/gII/CqVV+4MqxoMUSqEeSxEFEWnRVmwPPbQYrLSIIP66aft/" +
                "pC+zs7uzuzObbDI0DbzNDDt89vu+771BGAXEkUEfIoghiyTy6E" +
                "ACx/AGF3AOIZxGCw7hBDpxC0cQRRrtOIoDaMVBpHAGbTiM2+Uy" +
                "ChQZighFliJPkUAvPTmHVhXORdwrWz90WSvGQZhWcYODGD2TFB" +
                "U9axUORYjC0HMTD1x6jiNlc9DD8upmeZ00OdbXbM4kRlycnJJz" +
                "yoczhVE1h5+28mI7N+esoYftoxScQyvOsU47OBWfRY7ps8mh53" +
                "mDY/pMT+4zLrG8rth54arbH1o587ps60E/O32tXOVn5qV8r6r7" +
                "e9xw6Vnw6Lnu9EfkmD7ji6deJcMf3JH57NFzl3M2ZRynHhkH95" +
                "15yfrQk9dA9brjm6fuJaHug+66Kzk/1BwM1daHGJb2oW8/46Gi" +
                "Xv+U9UpXnYtHQl47fv7Qv2IufOeU6WG7sZr0jPvNKd+r9EzUNl" +
                "9V5y+Mp5RXkc/7E7rnH8vmnZ9Wz/szXXrk8+72Warnuc+cBuFM" +
                "ixzM2Pd8QE5O4LzYB/7MSu6fOTcnYN0b0fPSMxevdPnD+/m1/r" +
                "zwNlj/OPS8a1BP0a4X5uvh8PeLgmMf9rwPl5T+fGxKH37SxFn2" +
                "+LyiuZ9X6/ZnfR/cP581+byhifNV4Gzp8Yd/57vmuv+st+78/a" +
                "+m1v23pnr9Ud4bf5tyb/zXxNkWOLsaCxNk");
            
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
