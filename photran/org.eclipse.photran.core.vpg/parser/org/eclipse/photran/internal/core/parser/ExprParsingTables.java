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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 3, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 45, 0, 46, 5, 7, 47, 48, 49, 50, 11, 7, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 8, 9, 70, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 16, 80, 24, 81, 26, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 28, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 17, 127, 128, 129, 130, 131, 41, 132, 133, 18, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
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
            final int compressedBytes = 553;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWEFuwyAQHCNU0Z587JFEPfQJfR7HPCHHPLVgp61rA7Owti" +
                "JVJWqSegIMu8vsAhwCrIGHQ2ou4DV9vscn8T+cxqvBG+wZTxg+" +
                "gFMIA/zoHW4ePkT8MuPujmPGscaRx9McEYeJr/R1QHo2xr/gZ0" +
                "LpgZl/94vfvRH+dPzU3lBsjP/CPiZnH8af2Q/UP4zfJes/qX8i" +
                "PtnnHNeg4ufq45vN+EEYP6j6V2wfGr/19R/jX86fzb+1D7b7y/" +
                "buL3D7kP3H9eUqWv+W322e26HeSP+S/Rf7e93GFv2Ymk1vJj8E" +
                "07ejcVD7VdcvHf9r9UNmiGSfKr5oJbzUX72/tPiKX8b/Cvv+43" +
                "8bl+9vRfyy+KvjkvruQP3S5m8tLshfwvz00pffVgpT0tctHmT6" +
                "uuL3PPEbf/iR+Zvz/7q+/ua/bqGqsPL6/rG4tv6Xnn9K9aGofm" +
                "ESoKhv5PYzR/mnfv7BhdjXCc8nx/CXn49Mlz6J9icq/ie4dvyp" +
                "v4X19/O5b9UXpm/0/CDVr4L+0vp5bK3PG88/wvuf0vkou//34C" +
                "c7P7T6N5Nfdf7V7p/u+meRf1XxReoHSX9b7X+ovjTkLyMqoHL8" +
                "NvqCyM/Lxmfxt9v62vRVrv+6+UX2t/35Qx2fBFfXr7vdz/TFh1" +
                "6/dOP358dqLDXoi/R+jtW36KpfdvBP1/0lHp6fvJQfz2/a+pHp" +
                "i0J/1PZj96MH10d999dMv9vzB7v/LeKfDfC4Nw==");
            
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
            final int compressedBytes = 2678;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWlusXVUVPVQopGqawI/RxMQfvDExtSWSUFHX2buHRx9UjX" +
                "5o0A//bKJfmmJ5LT/8kGIgRhNpQaHSaIwxUaDtbe/tvX0LTYzR" +
                "CKktr5C+FHmIohUR5tzjjDXn2vucfXdbb0zPzNxjjDnHWnef/T" +
                "p7n3PjB3ryioskH5a8vFLvjFfI8v3922S5WPJ7gxd66RUX9G+L" +
                "CwXfE9+nunK9K75blu+Fo3hNIy6S/GDxGrUhWJyCZi9OWA+1eK" +
                "U5wp804iLJ30lePtRXDPFDcXGF09AVXyC5UFLWM9VkPc1RLtEI" +
                "T5ZLej0gtUavBxYfgGYPSVfOdVS1jdbpsrwOSyC1oTk041eaLv" +
                "CY9lGF2T6q+OJe7RUXxG/4fRS/Xt9Hfj3jh6u/t7JcOfzLNSTT" +
                "ZbkyradzmZNz4jXbi8Mt0v9mb+QLdc7ZdBWnNOK64pRyakMwan" +
                "JNndNq3hG2asR1QNnPNSSzYEXnrNeGcVRDjs+jUq9SgxiXgVGT" +
                "I63quYxq2e9x/ej9LnlV234PxzRkPY8ppzYEoyZHWtXzcKw4oS" +
                "Hn+wnZylVqGIJRk8v5nnreN4wzGjLnGalWqWEIRk2OtKrnxZky" +
                "aMSflUGO2FAduwEYlwNRhw+93Eu/zHLNcLYO52Yxme+jYtLvI+" +
                "3W9tFJDdlHJ5VTp/rHwajJkVb1XEZ9Mpbxxrgo6tm6onoPK4DU" +
                "wFjE6+N15Yr4CWj04qq4Wmp9uoSHeENxXEP20XF5F1VqGIJRky" +
                "Ot6jmUbc/q760uV5PlSKbLuCavAYf9pRpxUblUObWhjP6UafaQ" +
                "VvW8XBqOaMg+OiJbuUoNYvw0GDU50qqehyPlRzXkr6SkBlZrkH" +
                "TutarnHBV2eAw7qK1jr7AjbtQqXfGufISoExqKTGqN+Bkw6txr" +
                "Vc+h5jyPpmrn0ZT/jNNu7T7kpIYikxoIRp17reo5lKz3pMcwGd" +
                "IZHiYb23OSiV78fj5i1Ku8qbyJLEcZ/7lc113xs24bjdiew4ps" +
                "z8F9jc8Otz21i+1Zez+HPIZDiuQyYlPdi4Qj3pePkDVerqGIjJ" +
                "+Hjl8AwkNf7rWq5xw1HHsDlkBqQ3M0a75anNZQjDcDqYFg1Mn7" +
                "ReuhJve0yVFerCF/4eL4JSA1EIyaHGlVz2WmDp9H8f62/R43Y7" +
                "/HL/M8apuzv2H0fUh/Q/aMsKE+Z/F3DUUmNRCMOvdatdqeyVF+" +
                "REO2RkpqIBh17rWq51CNd7ekpr/Wdi7HrzbOoN/Y0pSidYqd+Y" +
                "hiJ/pwaNeP6PaMEHe17ve7GvchT9nSlKJ14my2nk8x4ag+O9yI" +
                "bus52NR6XdpUX89Oc25uu08ebB79fDR6v/dvHfMsc+t4Ve21zR" +
                "qKTGogGHXutarnxfBdhW0ewzZqVOL+bB9tY8JV7SM3Ivm2ewzb" +
                "qVGJh+teJFzVnG5Et8+jeLTb8Vlbz/0ew35FchnxXN2LhKNaTz" +
                "ci+fZ5DPsUw77+ndbx3v6d6KMHFyqZb9pjmFYM0/3breO9/dvR" +
                "Rw8uVDLfQY/hoGI42I/W8d5+RB89uFDJfLMew6ximO3fYR3v7d" +
                "+BPnpwoZL5dnsMuxXJG3PuZlrPj2g7lsZ/H5IfS1Vt1Pch8zBn" +
                "2Osx7FUkb7z3vUzr+RGptstj2KVI3phzF9N6fkSqzXgMM4rkjT" +
                "lnmNbzI1Jtj8ewR5G8MecepvX8iFTb6THsVCRvzLmTaT0/ItWm" +
                "PIYpRfLGnFNM6/kRqXbAYzigSN6Y8wDTen6E4OO2NKWYd9ycjz" +
                "Ot50ec2/eK534e2XW+65y19/OIx/AItXVyL9K7mr7hXf71WAKp" +
                "Dc3RrPlqOGxLU4p5x63nYab1/AjBJ2xpSjHvuDmfYFrPj5if/V" +
                "5+TEMRKc9xlZbnuArhoS/3WtVzjpqXY+lRj+FRauvkXqR3jfA9" +
                "5jE8Rm2d3Iv0Lu8r/q2hGL8FpAaCUZMjreq5zNTlGeHbXbanPc" +
                "d1eT4qdrVpqvidNL7Ld6rTte+CprPvgqaH6/nd9Lz5Dw3FeA+Q" +
                "GghGnbz3Wg81ed5MjuJBDemOQTDqUT1wmdM55t6e3V62PctLNB" +
                "SZ1EAw6txrVc/LS8JzGnLUpqQGglHnXqt6LvFrDamkpAaCUede" +
                "q3ou8YyGVFJSA8Goc69VPZd4XkMqKamBYNS516qeh+fn45pc/E" +
                "tDkUkNBKPOvVb1HCodVzdiCaQ2NEezVq/WPpHXlmvJckzff67N" +
                "/Yb4/jM8q6HIpAaCUedeq3oOdZ7fBf2gUTm3/X5V237v36KhyK" +
                "QGglHnXqt63r+lvFZDtvAw5fO90vL5XmG19asew7xW9by8tr9e" +
                "Q/5KSmogGHXutarn/fXhtIbstZTUQDDq3GtVz8Ppsq8haz5MXQ" +
                "LjciB7jLqX/rIfrzFVzaC/xw2q3gBIbWiOZq1eTfPiN/1V5aqh" +
                "q4ZkuqzXfLVcpqHIpAbCQ517rep5uSw8rSFbOCU1EIw691rVc4" +
                "lTGlJJSQ0Eo869VvUcqnZdWlOuIcuRTJexVgMO+1drKDKpgfBQ" +
                "516rel5eXbyoIVfpF+XepkJqIBh18v7QeqjJfUhyDDZq9HrjEI" +
                "x6VK/eH2xs/obCxK8kYPEB/xuKrFX7bygLNWRrLIz3AKmBYNTJ" +
                "+yProRavNEen+8+Z2v3nTHb/OTO8/3zwbL6j7vb57n5DeVJjPI" +
                "JRj+rV++jMeT+/u01TxS3n+RnXem8zL9vzJxrjEYx6VK/el/ix" +
                "htRSUgPBqHOvVX0fnTnvQ356lk8L2J4//5/ef76poSjXpTe9Bo" +
                "JRJ+8vrIeanO/J0ekZ9pdnt9/n5Vj6s4Zi/BWQGghGnbwT1kNN" +
                "3ntyDO7WkGvqGASjHtWr9wd3XyjnZqfr0mybporbL4zrUrffIt" +
                "O729+m612pPKQxHsGoR/Xq/eKhcFxD7qBSUgPBqHOvVT2XaPyP" +
                "JhP/qQk2eMH/r6Z1m2Oq/+Lscg2ZOcvz/a8aikxqIBh17rWq5x" +
                "JvaEjljbgHSA0Eo07eCet53zD+oiGVlNRAMOrca1XPoeY6Pgf3" +
                "t336NLv/t/Nob5su9nb6LK3Pua9NF41flIuHNcYjGPWoXr0v8b" +
                "qG1F6Ph4DUQDDq5J2wHmryeZQcF8r1s/ivhiKTGghGnXut6rnE" +
                "SxpSeUm2Z4XUQDDq5J2wHmqyPZOj+I+GdFNSA8Goc69VPZd4RU" +
                "Mqr8g9WIXUQDDq5P2t9VCT9UyO8lINeXq6NB4CUgPBqJN3wnqo" +
                "yTOXc3Q4j/a06aLxi3LxTw1FJjUQjDr3WtVzqPP7fj7+/lyuIX" +
                "O4/9B4729pKMrx+ZbXQDDq5J2wHmqy35OjeFlDuimpgWDUudeq" +
                "nhcvlxdpyFFxkTy/V0gNBKNO3j9aDzU5lpJjPu4/y8s0FJnUQD" +
                "Dq3GtVz8vLii0asjXGIBj1qF69X2y5UK7JZRU9l2WGvUzn3p6r" +
                "ece8PMdt1RiPYNSjevV+sbXT8/vTref7sXO5hsRnW93PNK4hr2" +
                "ooyjXkVa+BYNTJO2E91OQakhwXzPG5QENRPuMWeA0Eo07eCeuh" +
                "Jtel5Cj+piFbJiU1EIw691rVc6g59/vx3nm/GnOeOLvx5Ts0FJ" +
                "nUQDDq3GtVz0W9DfWy9Uw=");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 0, 1, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 12, 0, 2, 0, 0, 0, 0, 0, 13, 0, 2, 0, 0, 0, 0, 0, 0, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 16, 17, 0, 3, 4, 0, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
                "eNrtW0uOwyAMfWFYZMmiB8hR0JzMR5gjj9p0NFRNUggYDPFbRK" +
                "3aBPDnPRtaCzjAYsaCFQvM86W5X8jfr18WE+YbCH+gGR67mF7f" +
                "EsbG3X4BQvuFVnLhl1b72ZL2Nzyr81Dsez70n/ngP0cuLn9yMb" +
                "W0CfVvv418TfqweoY5TcVM/l7+afQ9/ias/AvUjL8smNPr39Cv" +
                "B5+8rB/c65cU0cQYeW/6f4vV/+/wKalYdsdH+vjc9vNHS2AHc5" +
                "67GP6xTPyT6P9G/Lfv/7T5O2oxf62f6+q3DP2Sw1+KEvFzxH8H" +
                "/B11v0L3D3L0P47/0C3/jcufkf4/UX+5pFnIjp8KXjjfP/3E92" +
                "9UvH/rvX61POcPwvp/Emt/xRXi9wzMMD7y0f77fP5Qz3/97uhT" +
                "KxMYXBStmYS7fiEoZNYvufq3cb8rf/5QSz9a6b/NubPA+dOI7E" +
                "hn7ff0P3XRv+T6Py1/KTZ+En8/ZivxYBX+K55/kvmrxviekT8V" +
                "w/Xvw+nfCUzJVmSt/7U7k82f4rrT6PqBQb9H2P9jqr9a5T8xr7" +
                "91/MuuXxrwh7D/n/D1X73HT+/8q/sn19b/Huo3fv3X+GuGX7Yu" +
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
            final int compressedBytes = 588;
            final int uncompressedBytes = 3961;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrNlM9rE0EUx7/BlthLsFANgYQEkx6kEjw0thCPek0PggoeVK" +
                "w/QMEfhdKq/UEV40EKpVCPpYiCiLRoK7aHHloMVlpEEH/dtP0/" +
                "0pfZ2d3Z3ZlNNhmaBt5mhh0++33f994gjALiyKAPEcSQRRJ5dC" +
                "CBY3iDCziHEE6jBYdwAp24hSOIIo12HMUBtOIgUjiDNhzG7XIZ" +
                "BYoMRYQiS5GnSKCXnpxDqwrnIu6VrR+6rBXjIEyruMFBjJ5Jio" +
                "qetQqHIkRh6LmJIZee40jZHPSwvLpZXidNjvU1mzOJERcnp+Sc" +
                "8uFMYVTN4aetvNjOzTlr6GH7KAXn0IpzrNMOTsVnkWP6bHLoed" +
                "7gmD7Tk/uMSyyvK3ZeuOr2h1bOvC7betDPTl8rV/mZeSnfq+r+" +
                "HjdcehY8eq47/RE5ps/44qlXyfAHd2Q+e/Tc5ZxNGcepR8bBfW" +
                "desj705DVQve745ql7Saj7oLvuSs4PNQfDtfUhHkj70Lef8VBR" +
                "r3/KeqWrzsUjIa8dP3/oXzEXvnPK9LDdWE16xv3mlO9VeiZqm6" +
                "+q8xfGU8qryOf9Cd3zj2Xzzk+r5/2ZLj3yeXf7LNXz3GdOg3Cm" +
                "RQ5m7Hs+ICcncF7sA39mJffPnJsTsO6N6HnpmYtXuvzh/fxaf1" +
                "54G6x/HHreNainaNcL8/Vw+PtFwbEPe96HS0p/PjalDz9p4ix7" +
                "fF7R3M+rdfuzvg/un8+afN7QxPkqcLb0+MO/811z3X/WW3f+/l" +
                "dT6/5bU73+KO+Nv025N/5r4mwLnF0IjxPh");
            
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
