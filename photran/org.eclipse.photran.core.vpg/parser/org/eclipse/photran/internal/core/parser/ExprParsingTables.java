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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 3, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 45, 0, 46, 5, 7, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 7, 11, 66, 67, 68, 69, 8, 70, 9, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 18, 80, 24, 81, 25, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 28, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 16, 127, 128, 129, 130, 131, 41, 132, 133, 17, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
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
                "eNrtWMtuwyAQHCMrcnvysUcS5dBPyOdxzCfkmE8t2EmaYMMsLF" +
                "ZUqVRNW08Nyz5mBzDAoTewGBDG4PAVfn77J/4v7MeLwRH9ATt0" +
                "J2DvXAc72gFXC+s8fn7FMeOI8WEdD2t4HMZ/hV87hGej/3Z2Ni" +
                "g8MPP/vdh3G8R+On8YRyQH39/DP2bNP8x+6h8aH6H/o/hBHr/J" +
                "Pwe/hxxuFriTxV+5v3v8UvGV+8fU5e/N/rr85PGn+2frL/yDZX" +
                "21sD/hP1Z/nF8uov0v7bvOaw/ID/J+yv9P9R2PsYQ/ptGHD7M+" +
                "BeO3rXFQ/2X3L53/vvtuZYrgnyz+NFJ46n11fTXCU/X1j/95PF" +
                "8fUX5uVf+K/CT2U/2m4wclTvvTxvUt6F/C/vRZ19+iDEvx6xJ3" +
                "Mn6N7PuY7Bt/7SPrF/f/WF8/7I+Hy1aYXN+/F9frY9n5J6UPRf" +
                "qFUYRC3zTQ79nzC3BudH7YVepf4fmjen2o1mf8IqpPZOJPcO38" +
                "0/s9ens7n9tSfmH8Rs8PUv5K8C/Vz2OpPi88/9Td/wzZ800L+2" +
                "Tnh9L4rvRXXXy19VOtf576ryq/iH6QvN9n39+UXwr6lxEJqDX7" +
                "FvwCb5+Vzc/yr9n+yvhVzv+69UX+7+v7hzo/Ca7Wr83uZ+ryQ8" +
                "9fuvnr+2M2lwr4RXo/x/QtqvRLg/hU3V/i7f3JSu3j/U2rHxm/" +
                "KPhH7T92P7qxPqq7v2b8Xd4/2P1vEv8BtBu4Nw==");
            
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
            final int compressedBytes = 2503;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVmlusHXUVxo9WChawD/CiMSa+6ImJIZEoIqizZzyttlRb2j" +
                "5o1AffaKJPGpXr3wcfxBiMEeMN46UFRGOiQC+0pafXUyAxRiPG" +
                "tlx8gGpoURFBvOH/+6/9zbfWzGbv3ZYT07Oy5lu/tb4znTO3vW" +
                "fvpjfO5J+0LOfmnBcXOj9dlJdvGFyflmf92tzJmfYnLcndpVlf" +
                "m14PHlyf6wvShXn5OnrqFxBpWc431y+Qo6ZdRuykWSg9qNObvK" +
                "N5OyIty/mrnBcP+aKhLi/L3UalXpJzac68nW0vb6ccMzPVkRIP" +
                "V0fSW0zJ0nTbkNixfNj1VB9p99ImLJurbGkqpkvcXJWu8T12h2" +
                "trj1HRcIxyLp/p/KQl6TP+GKVP94+R38701vIvrm3WDv/ttVpG" +
                "bta22+m8cmqd9jM/ky6xavD5mRE/1uU6R3nqpxFpU/00anJUEm" +
                "sk1qledFRbEGmTKbmrivY3rtEsujIdRaRlOd9WHSVHJbG2VNfX" +
                "oHHHPV076rjnvHT8ca+OIfJ2HkNNjkpibamur0H1CUS+3k+gJk" +
                "clsc7X+4lyXNpex/EiIq/zRdTkqCTWlur6GtSsQKQ7mxWoy9m7" +
                "wjS9i0yXTaKT7ryOy0np3WmQVqRl6f25v6pMV5mKs6tKc+l9za" +
                "p0pbFN0gdS9qb30pXr96SVed8eR+RjdDytro6To5JYW6rra9Dk" +
                "e0i9J55L9R5/LmHau88/hcjH6CnU5Kgk1pbq+trIb2fZM1c3V7" +
                "PSMnJaE3umbX0ZIr92XIaaLE1rRZxYquvrMr0Ckdd5BWqy1Hc5" +
                "sVTX16Dq9whoWmdKlpJYW6rra6PS2e612h7Z/1Tb05fRpSfd7P" +
                "3hDBl3Lu3rnEv7/OsRpr370pMIaFpvSpaSWFuq62tQfRIBZZKl" +
                "pOhU19d1+z6o2uG12hE57M8dTJulr3v/S/00G5oNrLQsv78xsi" +
                "do2jDpGJUqH6O5H/VeO9wxwtSOUe8vWvBaLUCZ6dtdp6V507e8" +
                "f7jNFQJqmT5snD5iyglDTnV9beT2zQdtaSruzvu92K3/jICmj5" +
                "qSpaTW+TGouuU9rXM0yxDQ9HFTspTE2lJdX4Mm3+fTd8cd9/Qd" +
                "O+7pE7o2x61zMPJ9SO76Z4Rr++us/4mAMslSUnSqW/anczTvQE" +
                "CZZCkpOtX1tdGIO98lgT417kpOnxxxBR3WUgRl1vPRX8/bxLyY" +
                "yj/tM0LaOfa43zzinvw7LUVQZorb2U7MW147Wv+02zm3eex9aX" +
                "N/O6dY55Zx75MxHf18NPq4D64b+Sxz3UvT8Jz/AQLKJEtJ0amu" +
                "r43Knt/qtdrqOR0Ix2gr0zzlGLX+4NzmtdrmOT3UdVqap6yz9U" +
                "/7epSOTnd+9rbzgNfqAJSZHu86Lc1btrP1B+d+r9V+aLV/cBP7" +
                "3mld8+TjfpP8Hedur9VuaLV7cAP73mld8+R13iB/x3nIa3UIWh" +
                "0aJPa907rmyetM8nec816reWg1P7ixmu+tc9g1T17njfJ3nPd7" +
                "re6HMvtOS83kn/b954jnYnculd7oz0MWYZ3VPq/VPiiz77TUTP" +
                "7g3Om12gll9p2WmskfnHu9VnuhzL7TUjP5g3OP12oPlNl3Wmom" +
                "f3De57W6D8rsOy01kz84d3mtdkGZfaelZvIH50Gv1UEos++01E" +
                "z+IT+gpQjK7KyznWgm/+l+rngm1xHv89Ous/cX3e21ujty12mp" +
                "ma97T1trbNmsidyd93uxWz2kpQjK7GxnO9FM/iE/qKUIyuyss5" +
                "1oJv/iHfdmgIBa5ue4wvk5rignDDnV9bXRop1L93it7oncdVpq" +
                "5mvnvNdrdW/krtNSM1+X/VACmr7QBJaSWFvOuF50TPGM8MVp9q" +
                "d/jpv8fFR3XhMik9KX3O9P/qxyf+fzpf3h86X9w+38inve/BcC" +
                "mm4xJUtJrfOrUHXL86Z33I4Yr6Q4Ubes0zkm78/pfvz+bM5HQJ" +
                "lkKSk61fU1qHocAWWSpaToVNfXZfoLBJRJlpKiU11fl+mjCCiT" +
                "LCVFp7q+LtM/IKBMspQUner6GrQY9+T6vwgokywlRae6vjZy59" +
                "WHbGkq7s77vW6394q8sdnISkv3+efG6Jby88/qMQSUSZaSolNd" +
                "Xxud4WdBt47onc5xv3T8cR98DgFlkqWk6FTX16CmRkAt8+t74f" +
                "z6XpQThpzq+rpMVyKgltYZfh83ZLpGOeluVqbLRcN9gu/jVpfp" +
                "alNxe9603O3FbvUnBJRJlpKiU11fgwafRUCZZCkpOtX1tZH7++" +
                "07/XXNuuHfs07LyN1e7DbvRECZZFPfjU51fQ2qHkFAmWQpKTrV" +
                "9XWZ/hEBZZKlpOhU19dGvfvS+mY9Ky0ju+8inbetr0RAmWRT34" +
                "1OdX0Nqp9BQPN7m2c8S0mt85tQdcv7EOeYuxUxXklxoq6fg/rf" +
                "oTD1PUm6zX9Dkrdq0ncoFyCg6RZTspTUOr8HVRfr9I4p3n8e7L" +
                "z/PBjefx4cvv/8/ql8Rj3d63v4DuUYYryS4kRdP7fJxPfznc9K" +
                "I5PSD8/wNW7Ce5tF2Z8/RoxXUpyo6+eFtiCgTLKUFJ3q+nm9Za" +
                "r3IXec8vOC7c+7XtbPBJYgoPnaXOJZSmqdP4WqW65N55jiGfZn" +
                "p3rcF+Vc+gsCmn5uSpaSWucsVN1yr3OOuW8gxispTtT1c9DZcm" +
                "1OcV/qfI8RmZS2nR33pWm/ixz+dZ3PASPXD474LvIOxHglxYm6" +
                "fg6qnkBAmWQpKTrV9XWZ9v5vJrMqd0Lo3En/PzHjLP6Gdaa4h+" +
                "w55ev9bwgokywlRae6vgY1r0BA015TspTUOmeh6voaVP8VAWWS" +
                "paToVNfXRpPOz7nbx53Lo6b/p+vocOcqOTxuOuU6O9/NRK4fGH" +
                "Ft3oUYr6Q4UdfPC/0bAU0LpmQpqXXOQtUtr0fOcbbcP5tXIaBM" +
                "spQUner6GlQ/i4Dm/fmsZympdc5C1S370zmaVyKgTLKUFJ3q+h" +
                "pUP4eA5mfD5zxLSa3zl1B1y3Y6R3MhApoWTMlSUuucbcr3MOrl" +
                "93XOMcV1tNC5ShbGTUvvPwgokywlRae6vjY6s8/n069P5x4ywf" +
                "ubEZ9UnIOA5mN0jmcpqXXOQtUtx8g56r8joEyylBSd6voa1JyL" +
                "gOZnhHM9S0mt87dQdct2OsdivP9sXoOAMslSUnSq62tQfSdivJ" +
                "LiRF0/B5019+SlCCiTLCVFp7q+Bi3Kc9xPEOOVFCfq+rlNJj6/" +
                "PzL2ej92OveQ9NhY76Mj7p/PI6D5Ne55z1JS65yFqlteO5zjrD" +
                "k/z0NA8/3zPM9SUuuchapb7kvOUf8DAWWSpaToVNfXRhOP+xMz" +
                "L8NPZ51PnurvN69GQJlkKSk61fV1of8BHUN17A==");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 12, 0, 2, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 16, 17, 0, 3, 0, 4, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
            final int compressedBytes = 452;
            final int uncompressedBytes = 14785;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtW0uOwyAMNRELNKssegB6Ex8t6gnmyCM6HxFN0vIztglvET" +
                "WqSIifec8mrQVYASw48PAND8vPRxMO+DwxD4DNfgDCL9D+jTiA" +
                "2Z8ijI0Qvwhx/OIouf0Y/zw2jP9C83QeJs6Zj/lb3vDn0KWtn1" +
                "oYzphsrS7kir9Uxl+9Pq4XX4UuYuOAv5CTQT8B+qw/ATDF8Tvy" +
                "r7Cmd/ED6vg5QbFEwsz95/+3VP+/x1cpFIya+uPeLX6eVfOIdW" +
                "JN0a8HkX5l8s+kn+f8583fIcf8Z/3c1/9l+NdEY/5e6M8L/Uwa" +
                "PzE2eOuHPP8t8f80/QM2/eOOv3r+C+ovlzUL2fnTgYXy/ukzvX" +
                "/D5v2b9vrV0rx/ENb/o9j4T1whf0tghuHIJ/P3fv+/H3/l+58b" +
                "/xR4sMBFgcz3p65fECZk1i+1/ncwfm3//qGXf3D5v60Z2eD9k1" +
                "7/r10z5/yjiv6llv+89Yup+ZP5+zHbSQe76F/z9SdZv3rc3xPq" +
                "58Rw/ftw/kff/Mvev9RZf2jST77nr60fCPx7hP0/ovqLa/0j8f" +
                "Nz57/s+oVBP4T9/4Su/9KeP9r1d+6fXNv/NdRv9P4/848NX6xt" +
                "c9Y=");
            
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
                "eNrNlM9rE0EUx7+RlthLsAVNIIQEkx6kEjw0tpCbXiUFDyp4UL" +
                "H+AAV/FNqK9gdVjAcplEI9liIKItKirdgeemgxWGkRQfx10/b/" +
                "SF9mZ3dnd2c22WRoGnibGXb47Pd933uDMAqII4M+RBBDFknk0Y" +
                "EEjuENLuAcQjiFFhzCCXTiFo4gijTacRQH0IqDSOE02nAYt8tl" +
                "FCgyFBGKLEWeIoFeenIOrSqci7hXtn7oslaMgzCt4gYHMXomKS" +
                "p61iocihCFoecmHrj0HEfK5qCH5dXN8jppcqyv2ZxJjLg4OSXn" +
                "jA9nCqNqDj9t5cV2bs5ZQw/bRyk4h1acY512cCo+ixzTZ5NDz/" +
                "MGx/SZntxnXGJ5XbHzwlW3P7Ry5nXZ1oN+dvpaucrPzEv5XlX3" +
                "97jh0rPg0XPd6Y/IMX3GF0+9SoY/uCPz2aPnLudsyjhOPTIO7j" +
                "vzkvWhJ6+B6nXHN0/dS0LdB911V3J+qDkYqq0PMSztQ99+xkNF" +
                "vf4p65WuOhePhLx2/Pyhf8Vc+M4p08N2YzXpGfebU75X6Zmobb" +
                "6qzl8YTymvIp/3J3TPP5bNOz+tnvdnuvTI593ts1TPc585DcKZ" +
                "FjmYse/5gJycwHmxD/yZldw/c25OwLo3ouelZy5e6fKH9/Nr/X" +
                "nhbbD+ceh516Ceol0vzNfD4e8XBcc+7HkfLin9+diUPvykibPs" +
                "8XlFcz+v1u3P+j64fz5r8nlDE+erwNnS4w//znfNdf9Zb935+1" +
                "9NrftvTfX6o7w3/jbl3vivibMtcHYBkVkTJQ==");
            
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
