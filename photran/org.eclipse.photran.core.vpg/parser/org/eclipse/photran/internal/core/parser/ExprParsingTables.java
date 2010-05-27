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

        protected static final int[] rowmap = { 0, 1, 2, 3, 4, 2, 5, 6, 7, 8, 9, 3, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 1, 37, 38, 4, 39, 40, 41, 42, 43, 44, 0, 6, 0, 45, 46, 5, 7, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 11, 7, 66, 67, 68, 69, 8, 70, 9, 71, 72, 73, 74, 75, 76, 77, 78, 10, 79, 12, 17, 80, 24, 81, 25, 82, 83, 84, 0, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 28, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 13, 109, 30, 31, 110, 36, 111, 112, 113, 114, 115, 116, 117, 0, 14, 118, 119, 120, 121, 15, 122, 37, 123, 124, 125, 126, 16, 127, 128, 129, 130, 131, 41, 132, 133, 18, 134, 42, 135, 0, 1, 136, 2, 137, 43, 138, 44, 139, 19, 140, 45, 3, 141, 48, 142, 143, 144, 145, 146, 147, 148, 49, 149, 150, 20, 151, 152, 153, 154, 2, 21, 155, 156, 157, 158, 159, 160, 161, 162, 22, 163, 164, 165, 166, 167, 50, 168, 169, 170, 51, 171, 172, 52, 173, 53, 174, 175, 176, 177, 178, 54, 55 };
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
            final int compressedBytes = 561;
            final int uncompressedBytes = 7393;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrtWM1uwyAM/oKiiu2U44606mGPsOvejGMeocc+6kKSdgsBbG" +
                "O6atI8aT/1ANvYnz8DC4/ewMEiiPV4Cz/fp0+AAcfhYnBGf8IB" +
                "3Qdw9L6DG5zF1eFz+nsYt3osesR6m9YjnHsGzPQVfu0mc8K5gH" +
                "eLQeEDs/zfxr5VCPvJ/YOckRXav3t8TCo+lP1kfMj7GZP3A+n9" +
                "5PVzfE6TDyW92em9zH5bZ9/t/nL3y/ff1OXv6l9dfkLvP3X+Lj" +
                "7Y11cL+zPxo+qPxpcLy/+9fdflHIuyEOtz8f9R37EMEvyYpQ/f" +
                "THoLCt8erQcZv6L/N7l510n9W+OTXR9JTp9br66vRvpcff3r/7" +
                "g+yr9EfZfrp1F9K/KTqG+Sv/2KfzQ/Mk+pb0b/Yvan17r+FmVg" +
                "Eh/7lN7z8DWy72W2b/i2jzhf3P9jfn23Pxaf6QBSfv9cvZj/S+" +
                "crgh+y+AsFEQp+04C/F+cXYGw0Pxwq+S9z/sCj5yNThU+s+kTh" +
                "/gm9dv95fY/erfO5k+ILhW/k/MDFrwz+kvx4kPJz4fxT9/5ji/" +
                "NNC/u280me38vuN9FfdferrZ9q/nPdzE/1+UXwB+b8Vlj/UHwR" +
                "9C/DIlAp+3b4gsk+x9ufyr9m/snwlY//uvNZ8e/r+4c6Pwm9mr" +
                "82e5+pyw89fun2r++PxVwS4Av3fY7it6jiLw3up+r9Ek/vT45r" +
                "H93ftPyRwhcF/qjj1+L9VMGP6t6vKfyW9w/q/Ter/wK5kpwu");
            
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
            final int compressedBytes = 2493;
            final int uncompressedBytes = 15037;
            
            byte[] decoded = new byte[compressedBytes];
            base64Decode(decoded,
                "eNrVWlusXVUVvVrDR1V+4EdjTPzRG7USCFExRdfZh9unJhr1Q6" +
                "N+8EcT/dIo7+WHUTEGY+ShUESj0RhjotDHpS29paVVSYzBiJEW" +
                "Siu0BUF8oSKCrrHmGXvMuffhnNPSG9MzM/eYY8xx9t17r/08++" +
                "Y3zJVPXlnyeyXPqeyV+dwyff3gqnx2wa8vHJ5rP3lFUc8q+Jr8" +
                "OvDBVaV+VX51mb6WnuZpRF5Z8k3N0+QR805jVPI8kB7U+Y3eMX" +
                "wLIq8s+euS54z4uSM8u053Gav1ipJnlSzL2WplOeWYm0sP1ngg" +
                "PZjfbEguzJtHjIrlA05T/WC7lTZhOmxsaihOl/iwyZd5jepobu" +
                "0YVQxjVPLsuc4nr8if8WOUP90fI7+ceVX9i2uGa0Z/e42mkQ/X" +
                "tMvpvHJqnvZZmsvnWTW4fG7Mx1TOc5yneRSRNzWPoiaPSMYaiX" +
                "lKi470fUTeZEjeRUX7jcvUi67CDiLyypIXpIPkEclYW0r1Ndik" +
                "cc+Xjxv3khdOHvfmKKIcm0dRk0ckY12OzaP6bqzB0iFEWfdDqM" +
                "kjkrG2lOprsOYZRFnOZ1CTRyRjbSnV12DD1Yj8w+Fq1HXvXW2Y" +
                "30VOl3Wik+4yj4vI8sW5yWvzyryu6Kl2k6F4caW8kC8ZprzauH" +
                "Xy+ryhaO+hq9TvzmtmOd6bLXHcmy1+3NHtjns6jihjdDy/Nx0n" +
                "j0jG2lKqr8GaI4gyRkdQk0ckY20p1dfG/LrXLbNuuI6VppHn90" +
                "XNsK1XIcq1YxVqcmF+vxg7llJ9XbvnI8o8z0dNLvQqO5ZSfQ2W" +
                "fo8A5g8YkgvJWFtK9bWxqmz3mLZH7j9pe/4KVHrydd4f9rpJ++" +
                "f2zv653V+P0O3tn8cQwPxBQ3IhGWtLqb4Ga/6AADLJhWTRKdXX" +
                "xupfWfSYFiMP23ORab38De9/sc9ww3ADK03r9z8cuWfA/KFpY1" +
                "SrMkYLN/SuHW6M0LUx6q3RAY/pAJCZv9V1Wpo3f9P7R8v8dgTQ" +
                "Mn/EeP6oITsMOaX62pjbNpfY1FC82+9rUW0eQwDzxwzJhWSt8+" +
                "NAqfWe1jmGNYD5E4bkQjLWlnNOi47p145866Rxz7fYuOdLdWxO" +
                "mufginH3IUX1zwhX9OfZ/BkBZJILyaJTat2ezjF8KwLIJBeSRa" +
                "dUXxsbc+Y7L7BPTTqS8yfHHEE/11QMyGy2Rn+z1TrmRVf+WZ8R" +
                "8o6J437dmHPy7zQVAzLzUljOtmPeeu1o/bMu58KNE89LN/aXc4" +
                "Z53jTpPhnd8c9H48d9cOXYZ5krX5yN9vmbEEAmuZAsOqX6umnX" +
                "Km31mLZ6nveFMdrKNE8do9YfnNs8pm2e5/u6Tkvz1Hm2/lmvR/" +
                "ngbPtnbzn3eUz7gMz8SNdpad66nK0/OPd6THuBae/gWureaap5" +
                "yrhfK3/Huctj2gVMuwZXU/dOU81T5nm1/B3nfo9pPzDtH2Tq3m" +
                "mqeco8s/wd55LHtARMS4Nr0lJvniPVPGWe18g/+73imGdYN+5V" +
                "G3tsprs9pruBzL7TUj35l305d3hMO4DMvtNSPfmD8x6P6R4gs+" +
                "+0VE/+4NztMe0GMvtOS/XkD849HtMeILPvtFRP/uC8y2O6C8js" +
                "Oy3Vkz84d3pMO4HMvtNSPfmD816P6V4gs++0VE/+Ef+FpmJAZm" +
                "eebUc9+U/1d8VZ9/lJ5/lZ59lbozs8pjsi7zot1fN172lraFND" +
                "8W6/r0U13aepGJDZWc62o578I/5LTcWAzM4824568i/fuA/fgQ" +
                "Balue4ystzXEV2GHJK9bWxZduX7vSY7oy867RUz9fOucVj2hJ5" +
                "12mpnq/rPd4/EMD8eUNyIRlrS6m+BpvhGeELs2xP/xw3/fmo6d" +
                "wHRk6Wv+y+P/33z8XO70uL4felxdFyftU9b/4FAczXG5ILyVrn" +
                "14BS6/Omd9yKmIxksSO1ztM5pm/P2T5+ew5fhgAyyYVk0SnV12" +
                "DpEQSQSS4ki06pvq7dnyGATHIhWXRK9XXtPowAMsmFZNEp1de1" +
                "ewQBZJILyaJTqq/BluOc3PwdAWSSC8miU6qvjbn9asGmhuLdfl" +
                "/rqr0r8sbhRlaaut8/N0a3kL9/psMIIJNcSBadUn2dDp+G34Ju" +
                "GKOdyrhfOHncB59DAJnkQrLolOprsOE7EUDLcn2vvFzfK7LDkF" +
                "Oqr2v3YgTQ0pTR+7gRp2uck+7hxfkisdE2wfufQe0ODMXb/abl" +
                "XS2qg88igExyIVl0SvU1WHocAWSSC8miU6qvjbn1tzdWa4drR+" +
                "uzVtPIu1pUh29DAJnkhl6NTqm+BksPIYBMciFZdEr1de2eQACZ" +
                "5EKy6JTqa2O989L64XpWmkbu3kU6b1tfgAAyyQ29Gp1SfQ3WnE" +
                "AAy73NCc+FZK3z5qauobRyH+IcC19ETEay2JHq+2D9dyhMvSfJ" +
                "m/0bkrJU096hvBwBzNcbkgvJWudtQKmYp3fMcP/Z+S292RHuP3" +
                "eM7j9vP5nfqGe7vod3KPcjJiNZ7Ej1fetMvZ/v/HYSOVn+7ku8" +
                "xk25t1mW7fltxGQkix2pvl/ZLQggk1xIFp1Sfd86U+9DfnDSzw" +
                "u2PX90Wu8/n0UAy3npWc+FZK3zx0Cp9Xh3jhmeYX9ysuO+LPvS" +
                "MQQw/9SQXEjWOuebY/ou6rLuzrHwJcRkJIsdqb4PdqYcmzOclz" +
                "q/v0ZOlredGeelWd9Fjtau874l8mZpzLvIzYjJSBY7Un0fLD2G" +
                "ADLJhWTRKdXXtdv730wmpoYLh/1/YsZe/IYpM5xDdp/08f44As" +
                "gkF5JFp1Rf1+4/EcC8x5BcSNY654FSfV0dxxFAJrmQLDql+trY" +
                "tP1z4eZJ+/K47v/pOOq8D4y8uXvGa2mcZ+edWOTN7jHH5u2IyU" +
                "gWO1J9v7K/IoD5gCG5kKx1zgOl1uuRc5wp58/m3wggk1xIFp1S" +
                "fV27TyCAZXs+4bmQrHXOA6XW7ekd/0IAmeRCsuiU6uvafRIBLP" +
                "dgT3ouJGudvwJKrcvpHMMVCGA+YEguJGud80Cp9ZnLOWY4jjr/" +
                "+xB5s2vMcfQ3BJBJLiSLTqm+NvbSfp/P95/KOWSK9zdj1v05BL" +
                "Dsn895LiRrnfNAqXXcveOPCCCTXEgWnVJ9XbvPI4Bl/3zecyFZ" +
                "6/wtUGpdTudYjvvP4SsQQCa5kCw6pfoarLkNMRnJYkeq74OdMe" +
                "fk/yCATHIhWXRK9TXYsjzHfQcxGcliR6rvW2fq8/tDE4/3Q6dy" +
                "DsmHJ3ofHnMOeQoBLOeQpzwXkrXOeaDUemw6xxmzf76AAJZ1f8" +
                "FzIVnrnAdKrevuHX9CAJnkQrLolOprY1PH/bG50/DpzPPYyX6/" +
                "+S8CyCQXkkWnVF9X9j8vWUxC");
            
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

        protected static final int[] rowmap = { 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7, 8, 9, 2, 0, 0, 1, 0, 10, 0, 11, 0, 1, 0, 12, 2, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0, 14, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 16, 17, 0, 3, 0, 4, 0, 0, 18, 1, 19, 0, 0, 0, 20, 21, 22, 0, 3, 0, 23, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 24, 0, 0, 1, 0, 0, 5, 25, 0, 2, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 26, 5, 0, 0, 0, 0, 6, 7, 27, 28, 0, 6, 29, 0, 30, 31, 0, 0, 7, 0, 32, 0, 8, 33, 34, 9, 35, 0, 36, 37, 8, 38, 0, 39, 9, 40, 0, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 2, 42, 0, 0, 0, 10, 0, 43, 11, 12, 0, 10, 44, 45, 0, 0, 11, 46, 0, 12, 13, 13, 0, 47, 0, 14, 15, 14, 0, 15, 48, 0, 49, 0, 50, 51, 0, 52, 0, 0, 16, 17, 0, 16, 53, 0, 54, 0, 17, 0, 18, 19, 0, 0, 0 };
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
                "eNrtW0FywyAMXFMOPnLIA/wUpi/TE/rkTup0iqd2DBgJgdmDJ5" +
                "nExqzErmQSCzjAYsaCFQvM66V5Hsg/jx8WE+YHCL+gGR6HmLZv" +
                "CX3jyV+AkL+QJRd+aeXPluTf8MzOY+A48mH8zEn8HLms9dMWiq" +
                "12d/4hE//C8LxU3WAV/ukn9uI/YdVPQGX8OWCy+dvxrx892vAH" +
                "bv40ZTQxZu4//3/E+v9neJVULIfjI318bv78uyk0oM5niX6uX5" +
                "ZJvxLjX0k/+eKfNn9HOfMf9bOs/+vwr8H/ln9O/XijnyLjD/QN" +
                "Sf/J8f84/UM1/atbv9Uf/3L8M+ovl3QXuvNHIAr5/dNXfP9Gxf" +
                "u31vsfy7P/oKz/J7X8D9whf3NguomRj47f+f6BXPzyn39S/VsY" +
                "Kasmw3XXT8IZfVPw1S9X/W/nfFd+/0HKP2r5v71yZoH9px7VkX" +
                "L5e8WfmuhfrsY/bf1SbP4k/n7MCumgiP4VX3+a9UtifM+onwPd" +
                "9e/d+V8GpmQWWev/0Z3p1k913Wl0/cDg3z08/2Oqv2qtf2Kef+" +
                "38112/VNAPZf8/4eu/Ws+f1vV3PD+5t/+3UL/x+//Iv2r4Bnlu" +
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
                "eNrNlM9rE0EUx7+RlthTbEETCCHBpAepBKHEFnISj0IKHlTwoG" +
                "L9AQr+KLRF7Q+qGA9SKIV6LEUURKRFW7E99NBisNIigvjrpu3/" +
                "kb7Mzu7O7s5sssnQNPA2M+zw2e/7vvcGYRQQRwZ9iCCGLJLIow" +
                "MJHMMbXMA5hHAKLTiEE+jELRxBFGm04ygOoBUHkcJptOEwbpfL" +
                "KFBkKCIUWYo8RQK99OQcWlU4F3GvbP3QZa0YB2FaxQ0OYvRMUl" +
                "T0rFU4FCEKQ89NPHDpOY6UzUEPy6ub5XXS5FhfszmTGHFxckrO" +
                "GR/OFEbVHH7ayovt3Jyzhh62j1JwDq04xzrt4FR8FjmmzyaHnu" +
                "cNjukzPbnPuMTyumLnhatuf2jlzOuyrQf97PS1cpWfmZfyvaru" +
                "73HDpWfBo+e60x+RY/qML556lQx/cEfms0fPXc7ZlHGcemQc3H" +
                "fmJetDT14D1euOb566l4S6D7rrruT8UHMwVFsfYljah779jIeK" +
                "ev1T1itddS4eCXnt+PlD/4q58J1TpoftxmrSM+43p3yv0jNR23" +
                "xVnb8wnlJeRT7vT+iefyybd35aPe/PdOmRz7vbZ6me5z5zGoQz" +
                "LXIwY9/zATk5gfNiH/gzK7l/5tycgHVvRM9Lz1y80uUP7+fX+v" +
                "PC22D949DzrkE9RbtemK+Hw98vCo592PM+XFL687EpffhJE2fZ" +
                "4/OK5n5erduf9X1w/3zW5POGJs5XgbOlxx/+ne+a6/6z3rrz97" +
                "+aWvffmur1R3lv/G3KvfFfE2db4OwCug4TTA==");
            
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
