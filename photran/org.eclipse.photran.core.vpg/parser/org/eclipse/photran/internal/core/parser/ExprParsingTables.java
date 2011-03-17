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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 10, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 0, 45, 46, 5, 7, 47, 48, 49, 50, 7, 11, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 8, 9, 70, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 17, 80, 24, 81, 26, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 29, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 16, 127, 128, 129, 130, 131, 41, 132, 133, 18, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
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
            final int compressedBytes = 551;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWMtOwzAQnFgWMpxy5OhWHPgEPs/HfkKP/VTspA/FjT1rb6" +
                "JKiEUUyGJnX56dNRwCrIGHQxIX8Jl+fscn8S8cxrPBF+wRbxh+" +
                "gEMIA/zoHS4ePkT9aanHrEeud+v69I6oh4lf6dcB6dkYv4OfDU" +
                "oPzPx/C/uuQuyn+yf5QlG4f/f4mGp8Sv4r/RPHP8sf5Pmb4nOM" +
                "Nnbll9t/z89yfRDZd8tfKb/y+Oj8K+VPFx90+I/n82N5ffXVH2" +
                "j82Pnj+HKu+1+07zK/26EuZH0p/u7x/lzGFvyYxKYPs74Fw7e9" +
                "9aDxq/p/k5t3Q6t/1/gU12dS0pfW0/xsdH4q+qb4tdXHv/5v6+" +
                "Xne7f6pPUr4Hc74hftTzufb0H/Evanj77+liHIKj7aNX2Q4Wtm" +
                "3/tk3/iwj7y/uf9n+of9uYQqgsr5/Wv1Wv4vnX9K/FHEXxgEKP" +
                "jNZvy9Pz/V+Qc4kfg64Xyyj/3y+ch04ZPofKKSf6LX7j+tt7D+" +
                "Op/7Vnxh+EbnByl+FfCX8uOxlZ83zj999z+uOt9sYd9yPinz+7" +
                "b8rvRXXX6156eb/1wW81N/fRH+IJzfKut3xZeG/mVEBGrNvid8" +
                "QbTPy/Zn9beZf234Ksd/3ftF8bf9/UNdn0Sv5q+b3c/01Ycev3" +
                "T79/fHai014Iv0fo7xW3Txlw3y03V/iZf3Jy+1j/c3LX9k+KLA" +
                "H3X8trg/VfCjvvtrht/t/YPd/xb1v1GwuDc=");
            
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
            final int compressedBytes = 2506;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVmlusHXUVxo9WCh7AJvCkPhhf9MTEkBgUEaKzZ3Ja7UXbQh" +
                "806oNvNNEnjcj174MPYgzGaAIqRo3QgsZEgbbQlp5eT4HEGI0Y" +
                "23LxAaqhYkUE8Yb/77/2N99aM5u9d1tOTPfKmm/91vrOnH1m9s" +
                "ye2fukt8/kR5rN+eOcFxe6IF2Ul28b3JBWZP3m/MmZ9pGW5e7y" +
                "rG9ObwUPbsj1+enCvHwLPfXLiDSb8531y+SoaZcRO2kOSg/q9A" +
                "7vaN6LSLM5f5Xz4iFfNNQVZbnbqNTLci7PmZ9n28vPU46ZmepI" +
                "iceqI+ldpmRpumNI7Fg+5nqqj7RbaTOWzVpbmorpEjdr0zW+x+" +
                "5wbe0+Khr2Uc4VM51HWpa+4PdR+nx/H/nnmd5dfuP6Zv3wd6/X" +
                "MnKzvn2eziun1mmPhZl0iVWDL82MeFiX6xzlqZ9DpM31c6jJUU" +
                "mskVinetFR3YlIm03JXVW0P3GNZtGV6SgizeZ8T3WUHJXE2lJd" +
                "X4PG7fd07aj9nvPS8fu9OobIz/MYanJUEmtLdX0Nqk8g8vF+Aj" +
                "U5Kol1Pt5PlP3S9jqOVxB5na+gJkclsbZU19egZiUibW1Woi6v" +
                "3pWm6QNkumwSnXTndVxOqo4j8vY8nq6sjpOjklhbqutr0OTjvd" +
                "4T93u9x+93TLv7PX0oNekjaTatzs9/dfkrVpuKs6tOq9LKZnX6" +
                "oLFN0pq0NvcGdOW6Sh/Ov+dZRN5Hz6ImRyWxtlTX10b+by+/cW" +
                "OzkZWWkdO62DNt68sQ+b3jMtRkaVov4sRSXV+X6RWIvM4rUJOl" +
                "vsuJpbq+BlW/R0DTBlOylMTaUl1fG5XODq/Vjsj+Ue1IX0OXnn" +
                "SL9w89zyCgaaMpWUpibamur40mvub3dV7z+/x7HKa965A/I6BM" +
                "spQUner62qj7aK5urmalZXmOmyJ7gqYwqR7wWj0Queu0tFn6lv" +
                "eHrTRie5Yqb8/5u3rvHW57Ymrbs/fbF71Wi1Bm+k7XaWnedLv3" +
                "D7dDhYBapo8bp0+YcsKQU11fG7nt/VFbmoq7834vduu/IKDpk6" +
                "ZkKal1fgqqbrmmdY5mFgFNnzYlS0msLdX1NWjye0f63rj9nr5r" +
                "+z19xr13jFnn4LpR1yG56+8Rruuvs/4nAsokS0nRqW7Zns7RvA" +
                "8BZZKlpOhU19dGI46qSwJ9bmbMI312xBF0WEsRlFkvRH+9YBPz" +
                "Yir/tPcIaefY/X7LiGvF32kpgjJTfJ7txLzlvaP1T/s857eMPS" +
                "9tGXFtM3mdW8ddJ2M6+v5o9H4fXD/yXub6V6fha/6HCCiTLCVF" +
                "p7q+NipbfpvXapvndCDso21M85R91PqDc7vXarvn9GjXaWmess" +
                "7WP+37UTo63euz9zwPeK0OQJnpqa7T0rzlebb+4NzvtdoPrfYP" +
                "bmbfO61rnrzfb5a/49zttdoNrXYPbmTfO61rnrzOG+XvOA95rQ" +
                "5Bq0ODxL53Wtc8eZ1J/o5zwWu1AK0WBjdVC711Drvmyeu8Sf6O" +
                "8yGv1UNQZt9pqZn8k15Lr/Z5SHwtld7oz0OWYJ3VPq/VPiiz77" +
                "TUTP7g3Om12gll9p2WmskfnHu8VnugzL7TUjP5g3Ov12ovlNl3" +
                "Wmomf3A+6LV6EMrsOy01kz84d3mtdkGZfaelZvIH50Gv1UEos+" +
                "+01Ez+IT+spQjK7KyznWgm/+l+rngmxxHP89Ous/cX3eu1ujdy" +
                "12mpma97d3DrbNmsi9yd93uxWz2qpQjK7DzPdqKZ/EN+REsRlN" +
                "lZZzvRTP6l2+/NAAG1zPdxhfN9XFFOGHKq62ujJXst3ee1ui9y" +
                "12mpma+d836v1f2Ru05LzXxdtkMJaPpyE1hKYm0543rRMcU9wl" +
                "em2Z7+Pm7y/VHdOX9HJqWvup+f/Pnn/s5nQfvDZ0H7h8/z6+5+" +
                "818IaLrVlCwltc5vQNUt95vecRdivJLiRN2yTueYvD2ne/jt2Z" +
                "yPgDLJUlJ0qutrUPUUAsokS0nRqa6vy/QXCCiTLCVFp7q+LtMn" +
                "EFAmWUqKTnV9XaZ/QECZZCkpOtX1NWgpzsn1fxFQJllKik51fW" +
                "3kXlcfs6WpuDvv97rd3jvypmYTKy3dZ6qbolvKz1SrJxFQJllK" +
                "ik51fW10hp8FfXtE73T2+6Xj9/vgWgSUSZaSolNdX4OaGgG1zO" +
                "/vhfP7e1FOGHKq62tQ9ScElEmWkqJTXV+DBl9EQJlkKSk61fU1" +
                "qFmFgFpaZ/i94ZDpGuWku1mVLhcN9x2+T1tTpmtMxe3ru+Vur9" +
                "t167Xv9Dc0G4a+DVpG7vZit3k/Asokm/pudKrra1D1OALKJEtJ" +
                "0amur8v0jwgokywlRae6vjbqnZeuaq5ipWVk912k87b1lQgok2" +
                "zqu9Gprq9B9fMIaL62ed6zlNQ6b4OqW65DnGP+NsR4JcWJun4O" +
                "6n+HwtT3JOkO/w1JflaTvkO5AAFNt5qSpaTW+X2oulind0xx/X" +
                "mwc/15MFx/Hhxef/7gVD6jnu79PXyHcgwxXklxoq6f22Ti9Xzn" +
                "s9LIpPSjM3yPm3BtsyTb827EeCXFibp+XuhOBJRJlpKiU10/t8" +
                "nE65Atp3y/YNvzntf0M4FlCGg+Npd5lpJa50+h6pZj0zmmuIf9" +
                "2anu9yV5LZ1EQNPPTclSUuucq0/qZ1Hnc51zzN+OGK+kOFHXz0" +
                "Fny7E5xXmp8z1GZFLafnacl6b9LnL413U+B4xcPzLiu8gtiPFK" +
                "ihN1/RxUPY2AMslSUnSq6+sy7f1vJrMqZ0Lo/En/n5hxFn/COl" +
                "OcQ/ac8vH+NwSUSZaSolNdX4Oa1yGgaa8pWUpqnXNQdX0Nqv+K" +
                "gDLJUlJ0qutro0mvz/m7x72WR03/T8fR4c5RcnjcdMp1dr6biV" +
                "w/POLYvAcxXklxoq6fF/o3ApoWTclSUuucg6pb3o+c42w5fzZv" +
                "QECZZCkpOtX1Nah+AQHN2/MFz1JS65yDqlu2p3M0r0dAmWQpKT" +
                "rV9TWofhEBzfeGL3qWklrnL6HqlufpHM2FCGhaNCVLSa1zrinf" +
                "w6iXr+ucY4rjaLFzlCyOm5befxBQJllKik51fW10Zp/Pp1+fzj" +
                "lkgvc3Iz6pOAcBzfvoHM9SUuucg6pb9pFz1H9HQJlkKSk61fU1" +
                "qDkXAc33COd6lpJa52+h6pbn6RxLcf3ZvAkBZZKlpOhU19egei" +
                "tivJLiRF0/B5015+TlCCiTLCVFp7q+Bi3JfdxPEOOVFCfq+rlN" +
                "Jt6/Pz72eD92OueQ9ORY7xMjzp8vIaD5Pe4lz1JS65yDqlveO5" +
                "zjrHl9noeA5vPneZ6lpNY5B1W3nJeco/4HAsokS0nRqa6vjSbu" +
                "96dnXoNHZ53PnOrPN29EQJlkKSk61fV1of8BpgN2eg==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 0, 12, 2, 0, 0, 0, 0, 0, 13, 2, 0, 0, 0, 0, 0, 0, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 3, 4, 0, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
    protected static final int[] columnmap = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 7, 0, 0, 9, 0, 0, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 14, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 21, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 24, 0, 0, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 28, 0, 29, 0, 0, 9, 0, 0, 4, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 35, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0 };

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
                "eNrtW0uOwjAMdaosoll1MQcIN/HRKk4wRx6FgZlUUyA/20nqt6" +
                "ioUCj+vWcnYAFWAAsOPPzAw3J/acIFbzfmCrDZD0B4AO3vigOY" +
                "/S3C3Aj+ixD7L/aS26/xt2tD/y801nlQPI98HL/lTfwcurT6qY" +
                "WR9Mk2i/9d8ZuZqOfHVUuxkr//suEgf0JOB/4F4Klf5hLe23+k" +
                "X6Gmd/YDtf2uo/xAQs//0//PVP2/xJ9SSBg1/ceFzX9elPOI+5" +
                "81hX+uRPyTGX9G/uOJf579Dkvs1/55dP2S5R9FF/nzgv9e8HfS" +
                "eoXuH9Tofxr/AWv/3pI/5+XfxPgX9F8u61v0nT8MUSifn77S5z" +
                "dsPr+NPv9YmvOHzuZ/7Nb/ijPkbwnMNDHyyfF7f37AFz83rL83" +
                "KRcscFKg8POp+xcERZ/9S63+Haxf258/cOmHlP7bmpUNzp/G1f" +
                "/amnkefxxifqmNf179Ymr+ZP5+zDLxIAv/Na+/nvmL4/mekD8V" +
                "083v0+kf/fDf9/7lmP3HSPwpZ39t/0Cg3zPs/xH1X1L1j8T2S+" +
                "d/3/2LAH909v8Tuvlr9PwZnX91/+Tc+j9C/0av/5p/YvgGus9z" +
                "1g==");
            
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
                "eNrNlM9rE0EUx7+RlthLsFBNDkkIJj1IJQglthC86TU9CCp4UL" +
                "H+AAV/FFqq9gdVjAcplEI9liIKItKirdgeemgxWGkRQfx10/b/" +
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
                "9NrftvTfX6o7w3/jbl3vivibMtcHYBaw0Tag==");
            
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
