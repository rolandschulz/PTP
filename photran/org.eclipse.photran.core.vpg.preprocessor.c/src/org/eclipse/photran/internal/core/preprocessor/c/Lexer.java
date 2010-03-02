/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Photran modifications
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
/**
 * Class edited by Matthew Michelotti.
 * 
 * Overview of changes to Lexer:
 * -Redirected the original IToken, Token, TokenWithImage, TokenForDigraph,
 *  and OffsetLimitReachedException class references to references in my
 *  package of edited versions of those classes.
 * -Changed the fetchToken function to construct tokens which remember
 *  the white spaces that proceed them. This was done by making a new int
 *  named spacesStart which corresponds to the start index of the spaces,
 *  and passing this as an extra variable to all of the token-generating
 *  functions that fetchToken calls.
 * -Updated the functions newToken (2 functions with this name),
 *  stringLiteral, charLiteral, identifier, headerName, number, and
 *  newDigraphToken to accept an additional parameter called spacesStart.
 *  These functions are all called by fetchToken to get a certain type
 *  of token. I changed the code so that it would also obtain the
 *  characters from spacesStart to offset, consider these to be white-space
 *  characters, and add them to the token using my edited constructors
 *  for Token, TokenWithImage, and TokenForDigraph.
 *  
 * -Added a field called fParentToken, which is an IToken that will be
 *  set as the parent of each token that this lexer fetches.
 * -Re-named fetchToken method as innerFetchToken, and made a new fetchToken
 *  method. The new fetchToken method will call innerFetchToken, and also
 *  adds the field fParentToken to the token. NOTE: this functionality should
 *  probably be moved to the original function in the future, to avoid
 *  a wrapper function
 * -Added a constructor that allows fParentToken to be passed in as an
 *  argument.
 *  
 * -Added a function getRawChars to get characters from fInput given
 *  an offset and endOffset.
 *  
 * -Edited the new fetchToken function to remember trigraphs and "\r\n" 's
 *  as parents of the constructed token, by checking if the length of the
 *  token image matches the difference in start and end offset.
 * -Added a token type tPRE_PHASE_3 which represents a token parent for
 *  a trigraph or "\r\n" ( which is changed in nextCharPhase3() )
 * 
 * 
 * Note: These modifications give tokens information about the white
 * spaces that come before them. This includes '\\' and '\n' characters in
 * line-splices, and comments. No modifications were made to the
 * nextDirective method.
 */
package org.eclipse.photran.internal.core.preprocessor.c;

import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IProblem;

/**
 * In short this class converts line endings (to '\n') and trigraphs 
 * (to their corresponding character), 
 * removes line-splices, comments and whitespace other than newline.
 * Returns preprocessor tokens.
 * <p>
 * In addition to the preprocessor tokens the following tokens may also be returned:
 * {@link #tBEFORE_INPUT}, {@link IToken#tEND_OF_INPUT}, {@link IToken#tCOMPLETION}.
 * <p>
 * Number literals are split up into {@link IToken#tINTEGER} and {@link IToken#tFLOATINGPT}. 
 * No checks are done on the number literals.
 * <p>
 * UNCs are accepted, however characters from outside of the basic source character set are
 * not converted to UNCs. Rather than that they are tested with 
 * {@link Character#isUnicodeIdentifierPart(char)} and may be accepted as part of an 
 * identifier.
 * <p>
 * The characters in string literals and char-literals are left as they are found, no conversion to
 * an execution character-set is performed.
 */

final public class Lexer {
    public static final int tBEFORE_INPUT   = IToken.FIRST_RESERVED_SCANNER;
    public static final int tNEWLINE        = IToken.FIRST_RESERVED_SCANNER + 1;
    public static final int tQUOTE_HEADER_NAME    = IToken.FIRST_RESERVED_SCANNER + 2;
    public static final int tSYSTEM_HEADER_NAME   = IToken.FIRST_RESERVED_SCANNER + 3;
    public static final int tOTHER_CHARACTER      = IToken.FIRST_RESERVED_SCANNER + 4;
    
    public static final int tPRE_PHASE_3    = IToken.FIRST_RESERVED_SCANNER + 5; //added by MM
    
    private static final int END_OF_INPUT = -1;
    private static final int ORIGIN_LEXER = OffsetLimitReachedException.ORIGIN_LEXER;
    
    public final static class LexerOptions implements Cloneable {
        /**is '$' an allowed character of an identifier*/
        public boolean fSupportDollarInIdentifiers= true;
        /**is '@' an allowed character of an identifier*/
        public boolean fSupportAtSignInIdentifiers= true;
        /**should "&lt;?" and ">?" be IGCCToken.tMIN/tMAX tokens*/
        public boolean fSupportMinAndMax= true;
        /**not used by Lexer class...*/
        public boolean fCreateImageLocations= true;
        /**is "/% ... %/" treated as a comment*/
        public boolean fSupportSlashPercentComments= false;
        
        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

    // configuration
    private final LexerOptions fOptions;
    /**when true, handles END_OF_INPUT token differently*/
    private boolean fSupportContentAssist= false;
    private final ILexerLog fLog;
    /**the source Object for all Tokens that this Lexer makes*/
    private final Object fSource;
    
    // the input to the lexer
    /**array of characters to parse*/
    private final char[] fInput;
    /**fInput index of place to start parsing*/
    private int fStart;
    /**fInput index of place to stop parsing*/
    private int fLimit;

    // after phase 3 (newline, trigraph, line-splice)
    /**offset of fCharPhase3 character, trigraph, or "\r\n"*/
    private int fOffset;
    /**end of offset of fCharPhase3 character, trigraph, or "\r\n"*/
    private int fEndOffset;
    /**A char or END_OF_INPUT. This is the current char represented by
     * a char in the input, a trigraph, or a "\r\n".*/
    private int fCharPhase3;
    
    private boolean fInsideIncludeDirective= false;
    private Token fToken;
    private Token fLastToken;
    
    // for the few cases where we have to lookahead more than one character
    // ( these variables are only used by markPhase3() and restorePhase3() )
    private int fMarkOffset;
    private int fMarkEndOffset;
    private int fMarkPrefetchedChar;
    
    /**If applicable, this is the "#include" token that invoked this Lexer*/
    private IToken fParentToken = null; //added by MM
    
    
    public Lexer(char[] input, LexerOptions options, ILexerLog log, Object source) {
        this(input, 0, input.length, options, log, source);
    }
    
    //function added by MM
    public Lexer(char[] input, LexerOptions options, ILexerLog log, Object source, IToken parentToken) {
        this(input, 0, input.length, options, log, source);
        fParentToken = parentToken;
    }

    public Lexer(char[] input, int start, int end, LexerOptions options, ILexerLog log, Object source) {
        fInput= input;
        fStart= fOffset= fEndOffset= start;
        fLimit= end;
        fOptions= options;
        fLog= log;
        fSource= source;
        fLastToken= fToken= new Token(tBEFORE_INPUT, source, start, start);
        nextCharPhase3();
    }
    
    //added by MM
    public char[] getRawChars(int offset, int endOffset) {
        if(offset < fStart || endOffset > fLimit) return null;
        char[] result = new char[endOffset-offset];
        System.arraycopy(fInput, offset, result, 0, result.length);
        return result;
    }
    
    //added by MM
    public IToken getParentToken() {
        return fParentToken;
    }
    
    /**
     * Returns the source that is attached to the tokens generated by this lexer
     */
    public Object getSource() {
        return fSource;
    }

    /**
     * Resets the lexer to the first char and prepares for content-assist mode. 
     */
    public void setContentAssistMode(int offset) {
        fSupportContentAssist= true;
        fLimit= Math.min(offset, fInput.length);
        // re-initialize 
        fOffset= fEndOffset= fStart;
        nextCharPhase3();
    }

    /**
     * Call this before consuming the name-token in the include directive. It causes the header-file 
     * tokens to be created. 
     */
    public void setInsideIncludeDirective(boolean val) {
        fInsideIncludeDirective= val;
    }
    
    /** 
     * Returns the current preprocessor token, does not advance.
     */
    public Token currentToken() {
        return fToken;
    }

    /**
     * Returns the endoffset of the token before the current one.
     */
    public int getLastEndOffset() {
        return fLastToken.getEndOffset();
    }

    /**
     * Advances to the next token, skipping whitespace other than newline.
     * @throws OffsetLimitReachedException when completion is requested in a literal or a header-name.
     */
    public Token nextToken() throws OffsetLimitReachedException {
        fLastToken= fToken;
        return fToken= fetchToken();
    }

    public boolean currentTokenIsFirstOnLine() {
        final int type= fLastToken.getType();
        return type == tNEWLINE || type == tBEFORE_INPUT;
    }
    
    /**
     * Advances to the next newline or the end of input. The newline will not be consumed. If the
     * current token is a newline no action is performed.
     * Returns the end offset of the last token before the newline. 
     * @param origin parameter for the {@link OffsetLimitReachedException} when it has to be thrown.
     * @since 5.0
     */
    @SuppressWarnings("fallthrough")
    public final int consumeLine(int origin) throws OffsetLimitReachedException {
        Token t= fToken;
        Token lt= null;
        while(true) {
            switch(t.getType()) {
            case IToken.tCOMPLETION:
                if (lt != null) {
                    fLastToken= lt;
                }
                fToken= t;
                throw new OffsetLimitReachedException(origin, t);
            case IToken.tEND_OF_INPUT:
                if (fSupportContentAssist) {
                    t.setType(IToken.tCOMPLETION);
                    throw new OffsetLimitReachedException(origin, t);
                }
                // no break;
            case Lexer.tNEWLINE:
                fToken= t;
                if (lt != null) {
                    fLastToken= lt;
                }
                return getLastEndOffset();
            }
            lt= t;
            t= fetchToken();
        }
    }

    /** 
     * Advances to the next pound token that starts a preprocessor directive. 
     * @return pound token of the directive or end-of-input.
     * @throws OffsetLimitReachedException when completion is requested in a literal or an header-name.
     */
    public Token nextDirective() throws OffsetLimitReachedException {
        fInsideIncludeDirective= false;
        final Token t= fToken;
        boolean haveNL= t==null || t.getType() == tNEWLINE;
        while(true) {
            final boolean hadNL= haveNL;
            haveNL= false;
            final int start= fOffset;
            final int c= fCharPhase3;
            
            // optimization avoids calling nextCharPhase3
            int d;
            final int pos= fEndOffset;
            if (pos+1 >= fLimit) {
                d= nextCharPhase3();
            }
            else {
                d= fInput[pos];
                switch(d) {
                case '\\': 
                    d= nextCharPhase3();
                    break;
                case '?':
                    if (fInput[pos+1] == '?') {
                        d= nextCharPhase3();
                        break;
                    }
                    fOffset= pos;
                    fCharPhase3= d;
                    fEndOffset= pos+1;
                    break;
                default:
                    fOffset= pos;
                    fCharPhase3= d;
                    fEndOffset= pos+1;
                    break;
                }
            }

            switch(c) {
            case END_OF_INPUT:
                fLastToken= fToken= newToken(IToken.tEND_OF_INPUT, start, start);
                return fToken;
            case '\n':
                haveNL= true;
                continue;
            case ' ':
            case '\t':
            case 0xb:  // vertical tab
            case '\f': 
            case '\r':
                haveNL= hadNL;
                continue;
                
            case '"':
                stringLiteral(start, start, false);
                continue;

            case '\'':
                charLiteral(start, start, false);
                continue;

            case '/':
                switch (d) {
                case '/':
                    nextCharPhase3();
                    lineComment(start);
                    continue; 
                case '*':
                    blockComment(start, '*');
                    continue;
                case '%':
                    if (fOptions.fSupportSlashPercentComments) {
                        blockComment(start, '%');
                    }
                    continue;
                }
                continue;
            
            case '%':
                if (hadNL) {
                    if (d == ':') {
                        // found at least '#'
                        final int e= nextCharPhase3();
                        if (e == '%') {
                            markPhase3();
                            if (nextCharPhase3() == ':') {
                                // found '##'
                                nextCharPhase3();
                                continue;
                            }
                            restorePhase3();
                        }
                        fLastToken= new Token(tNEWLINE, fSource, 0, start); // offset not significant
                        fToken= newDigraphToken(IToken.tPOUND, start, start);
                        return fToken;
                    }
                }
                continue;

            case '#':
                if (hadNL && d != '#') {
                    fLastToken= new Token(tNEWLINE, fSource, 0, start); // offset not significant
                    fToken= newToken(IToken.tPOUND, start, start);
                    return fToken;
                }
                continue;

            default:
                continue;
            }
        }
    }
    
    //added by MM (re-named original fetchToken...)
    private Token fetchToken() throws OffsetLimitReachedException {
        Token t = innerFetchToken();
        Token ancestor = t;
        
        //remember a parent token for trigraphs and "\r\n"
        int tOffset = t.getOffset();
        int tEndOffset = t.getEndOffset();
        
        int charImageLength;
        if(t.getType() == tNEWLINE) charImageLength = 1;
        else charImageLength = t.getCharImage().length;
        
        if(tEndOffset - tOffset != charImageLength) {
            Token invoker = new TokenWithImage(tPRE_PHASE_3, fSource, tOffset, tEndOffset, this.getRawChars(tOffset, tEndOffset), t.getCharPrecedingWhiteSpace());
            ancestor.setParent(invoker);
            ancestor = invoker;
        }
        
        
        //if applicable, make an "#include" directive be a parent of this token
        if(fParentToken != null) ancestor.setParent(fParentToken);
        
        return t;
    }
    
    /**
     * Computes the next token.
     */
    private Token innerFetchToken() throws OffsetLimitReachedException {
        final int spacesStart = fOffset;
        
        while(true) {
            final int start= fOffset;
            final int c= fCharPhase3;
            final int d= nextCharPhase3();
            switch(c) {
            case END_OF_INPUT:
                return newToken(IToken.tEND_OF_INPUT, spacesStart, start);
            case '\n':
                fInsideIncludeDirective= false;
                return newToken(Lexer.tNEWLINE, spacesStart, start);
            case ' ':
            case '\t':
            case 0xb:  // vertical tab
            case '\f': 
            case '\r':
                continue;

            case 'L':
                switch(d) {
                case '"':
                    nextCharPhase3();
                    return stringLiteral(spacesStart, start, true);
                case '\'':
                    nextCharPhase3();
                    return charLiteral(spacesStart, start, true);
                }
                return identifier(spacesStart, start, 1);

            case '"':
                if (fInsideIncludeDirective) {
                    return headerName(spacesStart, start, true);
                }
                return stringLiteral(spacesStart, start, false);

            case '\'':
                return charLiteral(spacesStart, start, false);

            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': 
            case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': 
            case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I':
            case 'J': case 'K':           case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': 
            case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
            case '_':
                return identifier(spacesStart, start, 1);

            case '$':
                if (fOptions.fSupportDollarInIdentifiers) {
                    return identifier(spacesStart, start, 1);
                }
                break;
            case '@':
                if (fOptions.fSupportAtSignInIdentifiers) {
                    return identifier(spacesStart, start, 1);
                }
                break;

            case '\\':
                switch(d) {
                case 'u': case 'U':
                    nextCharPhase3();
                    return identifier(spacesStart, start, 2);
                }
                return newToken(tOTHER_CHARACTER, spacesStart, start, 1);

            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                return number(spacesStart, start, 1, false);

            case '.':
                switch(d) {
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    nextCharPhase3();
                    return number(spacesStart, start, 2, true);

                case '.':
                    markPhase3();
                    if (nextCharPhase3() == '.') {
                        nextCharPhase3();
                        return newToken(IToken.tELLIPSIS, spacesStart, start);
                    }
                    restorePhase3();
                    break;

                case '*':
                    nextCharPhase3();
                    return newToken(IToken.tDOTSTAR, spacesStart, start);
                }
                return newToken(IToken.tDOT, spacesStart, start);

            case '#':
                if (d == '#') {
                    nextCharPhase3();
                    return newToken(IToken.tPOUNDPOUND, spacesStart, start);
                }
                return newToken(IToken.tPOUND, spacesStart, start);

            case '{':
                return newToken(IToken.tLBRACE, spacesStart, start);
            case '}':
                return newToken(IToken.tRBRACE, spacesStart, start);
            case '[':
                return newToken(IToken.tLBRACKET, spacesStart, start);
            case ']':
                return newToken(IToken.tRBRACKET, spacesStart, start);
            case '(':
                return newToken(IToken.tLPAREN, spacesStart, start);
            case ')':
                return newToken(IToken.tRPAREN, spacesStart, start);
            case ';':
                return newToken(IToken.tSEMI, spacesStart, start);

            case ':':
                switch(d) {
                case ':':
                    nextCharPhase3();
                    return newToken(IToken.tCOLONCOLON, spacesStart, start);
                case '>': 
                    nextCharPhase3();
                    return newDigraphToken(IToken.tRBRACKET, spacesStart, start);
                }
                return newToken(IToken.tCOLON, spacesStart, start);

            case '?':
                return newToken(IToken.tQUESTION, spacesStart, start);

            case '+':
                switch (d) {
                case '+':
                    nextCharPhase3();
                    return newToken(IToken.tINCR, spacesStart, start);
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tPLUSASSIGN, spacesStart, start);
                }
                return newToken(IToken.tPLUS, spacesStart, start);

            case '-':
                switch (d) {
                case '>': 
                    int e= nextCharPhase3();
                    if (e == '*') {
                        nextCharPhase3();
                        return newToken(IToken.tARROWSTAR, spacesStart, start);
                    }
                    return newToken(IToken.tARROW, spacesStart, start);

                case '-':
                    nextCharPhase3();
                    return newToken(IToken.tDECR, spacesStart, start);
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tMINUSASSIGN, spacesStart, start);
                }
                return newToken(IToken.tMINUS, spacesStart, start);

            case '*':
                if (d == '=') {
                    nextCharPhase3();
                    return newToken(IToken.tSTARASSIGN, spacesStart, start);
                }
                return newToken(IToken.tSTAR, spacesStart, start);

            case '/':
                switch (d) {
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tDIVASSIGN, spacesStart, start);
                case '/':
                    nextCharPhase3();
                    lineComment(start);
                    continue; 
                case '*':
                    blockComment(start, '*');
                    continue;
                case '%':
                    if (fOptions.fSupportSlashPercentComments) {
                        blockComment(start, '%');
                        continue;
                    }
                    break;
                }
                return newToken(IToken.tDIV, spacesStart, start);

            case '%':
                switch (d) {
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tMODASSIGN, spacesStart, start);
                case '>':
                    nextCharPhase3();
                    return newDigraphToken(IToken.tRBRACE, spacesStart, start);
                case ':':
                    final int e= nextCharPhase3();
                    if (e == '%') {
                        markPhase3();
                        if (nextCharPhase3() == ':') {
                            nextCharPhase3();
                            return newDigraphToken(IToken.tPOUNDPOUND, spacesStart, start);
                        }
                        restorePhase3();
                    }
                    return newDigraphToken(IToken.tPOUND, spacesStart, start);
                }
                return newToken(IToken.tMOD, spacesStart, start);

            case '^':
                if (d == '=') {
                    nextCharPhase3();
                    return newToken(IToken.tXORASSIGN, spacesStart, start);
                }
                return newToken(IToken.tXOR, spacesStart, start);

            case '&':
                switch (d) {
                case '&':
                    nextCharPhase3();
                    return newToken(IToken.tAND, spacesStart, start);
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tAMPERASSIGN, spacesStart, start);
                }
                return newToken(IToken.tAMPER, spacesStart, start);

            case '|':
                switch (d) {
                case '|':
                    nextCharPhase3();
                    return newToken(IToken.tOR, spacesStart, start);
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tBITORASSIGN, spacesStart, start);
                }
                return newToken(IToken.tBITOR, spacesStart, start);

            case '~':
                return newToken(IToken.tBITCOMPLEMENT, spacesStart, start);

            case '!':
                if (d == '=') {
                    nextCharPhase3();
                    return newToken(IToken.tNOTEQUAL, spacesStart, start);
                }
                return newToken(IToken.tNOT, spacesStart, start);

            case '=':
                if (d == '=') {
                    nextCharPhase3();
                    return newToken(IToken.tEQUAL, spacesStart, start);
                }
                return newToken(IToken.tASSIGN, spacesStart, start);

            case '<':
                if (fInsideIncludeDirective) {
                    return headerName(spacesStart, start, false);
                }

                switch(d) {
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tLTEQUAL, spacesStart, start);
                case '<':
                    final int e= nextCharPhase3();
                    if (e == '=') {
                        nextCharPhase3();
                        return newToken(IToken.tSHIFTLASSIGN, spacesStart, start);
                    } 
                    return newToken(IToken.tSHIFTL, spacesStart, start);
                case '?':
                    if (fOptions.fSupportMinAndMax) {
                        nextCharPhase3();
                        return newToken(IGCCToken.tMIN, spacesStart, start);
                    } 
                    break;
                case ':':
                    nextCharPhase3();
                    return newDigraphToken(IToken.tLBRACKET, spacesStart, start);
                case '%':
                    nextCharPhase3();
                    return newDigraphToken(IToken.tLBRACE, spacesStart, start);
                }
                return newToken(IToken.tLT, spacesStart, start);

            case '>':
                switch(d) {
                case '=':
                    nextCharPhase3();
                    return newToken(IToken.tGTEQUAL, spacesStart, start);
                case '>':
                    final int e= nextCharPhase3();
                    if (e == '=') {
                        nextCharPhase3();
                        return newToken(IToken.tSHIFTRASSIGN, spacesStart, start);
                    } 
                    return newToken(IToken.tSHIFTR, spacesStart, start);
                case '?':
                    if (fOptions.fSupportMinAndMax) {
                        nextCharPhase3();
                        return newToken(IGCCToken.tMAX, spacesStart, start);
                    } 
                    break;
                }
                return newToken(IToken.tGT, spacesStart, start);

            case ',':
                return newToken(IToken.tCOMMA, spacesStart, start);

            default:
                // in case we have some other letter to start an identifier
                if (Character.isUnicodeIdentifierStart((char) c)) {
                    return identifier(spacesStart, start, 1);
                }
                break;
            }
            // handles for instance @
            return newToken(tOTHER_CHARACTER, spacesStart, start, 1);
        }
    }

    private Token newToken(int kind, int spacesStart, int offset) {
        char[] spaces = new char[offset - spacesStart];
        System.arraycopy(fInput, spacesStart, spaces, 0, spaces.length);
        return new Token(kind, fSource, offset, fOffset, spaces);
    }

    private Token newDigraphToken(int kind, int spacesStart, int offset) {
        char[] spaces = new char[offset - spacesStart];
        System.arraycopy(fInput, spacesStart, spaces, 0, spaces.length);
        return new TokenForDigraph(kind, fSource, offset, fOffset, spaces);
    }

    private Token newToken(final int kind, final int spacesStart, final int offset, final int imageLength) {
        final int endOffset= fOffset;
        final int sourceLen= endOffset-offset;
        char[] image;
        if (sourceLen != imageLength) {
            image= getCharImage(offset, endOffset, imageLength);
        }
        else {
            image= new char[imageLength];
            System.arraycopy(fInput, offset, image, 0, imageLength);
        }
        
        char[] spaces = new char[offset - spacesStart];
        System.arraycopy(fInput, spacesStart, spaces, 0, spaces.length);
        
        return new TokenWithImage(kind, fSource, offset, endOffset, image, spaces);
    }

    private void handleProblem(int problemID, char[] arg, int offset) {
        fLog.handleProblem(problemID, arg, offset, fOffset);
    }

    @SuppressWarnings("fallthrough")
    private Token headerName(final int spacesStart, final int start, final boolean expectQuotes) throws OffsetLimitReachedException {
        int length= 1;
        boolean done = false;
        int c= fCharPhase3;
        loop: while (!done) {
            switch (c) {
            case END_OF_INPUT:
                if (fSupportContentAssist) {
                    throw new OffsetLimitReachedException(ORIGIN_LEXER, 
                            newToken((expectQuotes ? tQUOTE_HEADER_NAME : tSYSTEM_HEADER_NAME), spacesStart, start, length));
                }
                // no break;
            case '\n':
                handleProblem(IProblem.SCANNER_UNBOUNDED_STRING, getInputChars(start, fOffset), start);
                break loop;
                
            case '"':
                done= expectQuotes;
                break;
            case '>':
                done= !expectQuotes;
                break;
            }
            length++;
            c= nextCharPhase3();
        }
        return newToken((expectQuotes ? tQUOTE_HEADER_NAME : tSYSTEM_HEADER_NAME), spacesStart, start, length);
    }

    private void blockComment(final int start, final char trigger) {
        // we can ignore line-splices, trigraphs and windows newlines when searching for the '*'
        int pos= fEndOffset;
        while(pos < fLimit) {
            if (fInput[pos++] == trigger) {
                fEndOffset= pos;
                if (nextCharPhase3() == '/') {
                    nextCharPhase3();
                    fLog.handleComment(true, start, fOffset);
                    return;
                }
            }
        }
        fCharPhase3= END_OF_INPUT;
        fOffset= fEndOffset= pos;
        fLog.handleComment(true, start, pos);
    }

    private void lineComment(final int start) {
        int c= fCharPhase3;
        while(true) {
            switch (c) {
            case END_OF_INPUT:
            case '\n':
                fLog.handleComment(false, start, fOffset);
                return;
            }
            c= nextCharPhase3();
        }
    }

    @SuppressWarnings("fallthrough")
    private Token stringLiteral(final int spacesStart, final int start, final boolean wide) throws OffsetLimitReachedException {
        boolean escaped = false;
        boolean done = false;
        int length= wide ? 2 : 1;
        int c= fCharPhase3;
        
        loop: while (!done) {
            switch(c) {
            case END_OF_INPUT:
                if (fSupportContentAssist) {
                    throw new OffsetLimitReachedException(ORIGIN_LEXER,
                        newToken(wide ? IToken.tLSTRING : IToken.tSTRING, spacesStart, start, length));
                }
                // no break;
            case '\n':
                handleProblem(IProblem.SCANNER_UNBOUNDED_STRING, getInputChars(start, fOffset), start);
                break loop;
                
            case '\\': 
                escaped= !escaped;
                break;
            case '"':
                if (!escaped) {
                    done= true;
                }
                escaped= false;
                break;
            default:
                escaped= false;
                break;
            }
            length++;
            c= nextCharPhase3();
        }
        return newToken(wide ? IToken.tLSTRING : IToken.tSTRING, spacesStart, start, length);
    }
    
    @SuppressWarnings("fallthrough")
    private Token charLiteral(final int spacesStart, final int start, boolean wide) throws OffsetLimitReachedException {
        boolean escaped = false;
        boolean done = false;
        int length= wide ? 2 : 1;
        int c= fCharPhase3;
        
        loop: while (!done) {
            switch(c) {
            case END_OF_INPUT:
                if (fSupportContentAssist) {
                    throw new OffsetLimitReachedException(ORIGIN_LEXER,
                        newToken(wide ? IToken.tLCHAR : IToken.tCHAR, spacesStart, start, length));
                }
                // no break;
            case '\n':
                handleProblem(IProblem.SCANNER_BAD_CHARACTER, getInputChars(start, fOffset), start);
                break loop;
            case '\\': 
                escaped= !escaped;
                break;
            case '\'':
                if (!escaped) {
                    done= true;
                }
                escaped= false;
                break;
            default:
                escaped= false;
                break;
            }
            length++;
            c= nextCharPhase3();
        }
        return newToken(wide ? IToken.tLCHAR : IToken.tCHAR, spacesStart, start, length);
    }
    
    private Token identifier(int spacesStart, int start, int length) {
        int tokenKind= IToken.tIDENTIFIER;
        boolean isPartOfIdentifier= true;
        int c= fCharPhase3;
        while (true) {
            switch(c) {
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': 
            case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': 
            case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I':
            case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': 
            case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
            case '_': 
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
                
            case '\\': // universal character name
                markPhase3();
                switch(nextCharPhase3()) {
                case 'u': case 'U':
                    length++;
                    break;
                default:
                    restorePhase3();
                    isPartOfIdentifier= false;
                    break;
                }
                break;

            case END_OF_INPUT:
                if (fSupportContentAssist) {
                    tokenKind= IToken.tCOMPLETION;
                }
                isPartOfIdentifier= false;
                break;
            case ' ': case '\t': case 0xb: case '\f': case '\r': case '\n':
                isPartOfIdentifier= false;
                break;

            case '$':
                isPartOfIdentifier= fOptions.fSupportDollarInIdentifiers;
                break;
            case '@':
                isPartOfIdentifier= fOptions.fSupportAtSignInIdentifiers;
                break;
                
            case '{': case '}': case '[': case ']': case '#': case '(': case ')': case '<': case '>':
            case '%': case ':': case ';': case '.': case '?': case '*': case '+': case '-': case '/':
            case '^': case '&': case '|': case '~': case '!': case '=': case ',': case '"': case '\'':
                isPartOfIdentifier= false;
                break;
                
            default:
                isPartOfIdentifier= Character.isUnicodeIdentifierPart((char) c);
                break;
            }
            
            if (!isPartOfIdentifier) {
                break;
            }
            
            length++;
            c= nextCharPhase3();
        }

        return newToken(tokenKind, spacesStart, start, length);
    }
    
    private Token number(final int spacesStart, final int start, int length, boolean isFloat) throws OffsetLimitReachedException {
        boolean isPartOfNumber= true;
        int c= fCharPhase3;
        while (true) {
            switch(c) {
            // non-digit
            case 'a': case 'b': case 'c': case 'd':           case 'f': case 'g': case 'h': case 'i': 
            case 'j': case 'k': case 'l': case 'm': case 'n': case 'o':           case 'q': case 'r': 
            case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
            case 'A': case 'B': case 'C': case 'D':           case 'F': case 'G': case 'H': case 'I':
            case 'J': case 'K': case 'L': case 'M': case 'N': case 'O':           case 'Q': case 'R': 
            case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
            case '_': 
                
            // digit
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                break;
                
            // period
            case '.':
                isFloat= true;
                break;
                
            // sign
            case 'p':
            case 'P':
            case 'e':
            case 'E':
                length++;
                c= nextCharPhase3();
                switch (c) {
                case '+': case '-':
                case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                    isFloat= true;
                    length++;
                    c= nextCharPhase3();
                    break;
                }
                continue;
                
            // universal character name (non-digit)
            case '\\':
                markPhase3();
                switch(nextCharPhase3()) {
                case 'u': case 'U':
                    length++;
                    break;
                default:
                    restorePhase3();
                    isPartOfNumber= false;
                    break;
                }
                break;
            
            case END_OF_INPUT:
                if (fSupportContentAssist) {
                    throw new OffsetLimitReachedException(ORIGIN_LEXER, 
                            newToken((isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER), spacesStart, start, length));
                }
                isPartOfNumber= false;
                break;
                
            default:
                isPartOfNumber= false;
                break;
            }
            if (!isPartOfNumber) {
                break;
            }
            
            c= nextCharPhase3();
            length++;
        }
        
        return newToken((isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER), spacesStart, start, length);
    }
    
    
    /**
     * Saves the current state of phase3, necessary for '...', '%:%:' and UNCs.
     */
    private void markPhase3() {
        fMarkOffset= fOffset;
        fMarkEndOffset= fEndOffset;
        fMarkPrefetchedChar= fCharPhase3;
    }
    
    /**
     * Restores a previously saved state of phase3.
     */
    private void restorePhase3() {
        fOffset= fMarkOffset;
        fEndOffset= fMarkEndOffset;
        fCharPhase3= fMarkPrefetchedChar;
    }
    
    /**
     * Perform phase 1-3: Replace \r\n with \n, handle trigraphs, detect line-splicing.
     * Changes fOffset, fEndOffset and fCharPhase3, stateless otherwise.
     */
    @SuppressWarnings("fallthrough")
    private int nextCharPhase3() {
        int pos= fEndOffset;
        do {
            if (pos+1 >= fLimit) {
                if (pos >= fLimit) {
                    fOffset= fLimit;
                    fEndOffset= fLimit;
                    fCharPhase3= END_OF_INPUT;
                    return END_OF_INPUT;
                }
                fOffset= pos;
                fEndOffset= pos+1;
                fCharPhase3= fInput[pos];
                return fCharPhase3;
            }
            
            final char c= fInput[pos];
            fOffset= pos;
            fEndOffset= ++pos;
            fCharPhase3= c;
            switch(c) {
            // windows line-ending
            case '\r':
                if (fInput[pos] == '\n') {
                    fEndOffset= pos+1;
                    fCharPhase3= '\n';
                    return '\n';
                }
                return c;

                // trigraph sequences
            case '?':
                if (fInput[pos] != '?' || pos+1 >= fLimit) {
                    return c;
                }
                final char trigraph= checkTrigraph(fInput[pos+1]);
                if (trigraph == 0) {
                    return c;
                }
                if (trigraph != '\\') {
                    fEndOffset= pos+2;
                    fCharPhase3= trigraph;
                    return trigraph;
                }
                pos+= 2;
                // no break, handle backslash

            case '\\':
                final int lsPos= findEndOfLineSpliceSequence(pos);
                if (lsPos > pos) {
                    pos= lsPos;
                    continue;
                }
                fEndOffset= pos;
                fCharPhase3= '\\';
                return '\\';    // don't return c, it may be a '?'

            default:
                return c;
            }
        }
        while(true);
    }
    
    /**
     * Maps a trigraph to the character it encodes.
     * @param c trigraph without leading question marks.
     * @return the character encoded or 0.
     */
    private char checkTrigraph(char c) {
        switch(c) {
        case '=': return '#';
        case '\'':return '^';
        case '(': return '[';
        case ')': return ']';
        case '!': return '|';
        case '<': return '{';
        case '>': return '}';
        case '-': return '~';
        case '/': return '\\';
        }
        return 0;
    }

    /**
     * Returns the endoffset for a line-splice sequence, or -1 if there is none.
     */
    @SuppressWarnings("fallthrough")
    private int findEndOfLineSpliceSequence(int pos) {
        boolean haveBackslash= true;
        int result= -1;
        loop: while(pos < fLimit) {
            switch(fInput[pos++]) {
            case '\n':  
                if (haveBackslash) {
                    result= pos;
                    haveBackslash= false;
                    continue loop;
                }
                return result;                  
        
            case '\r': case ' ': case '\f': case '\t': case 0xb: // vertical tab  
                if (haveBackslash) {
                    continue loop;
                }
                return result;
            
            case '?':
                if (pos+1 >= fLimit || fInput[pos] != '?' || fInput[++pos] != '/') {
                    return result;
                }
                // fall through to backslash handling
                    
            case '\\':
                if (!haveBackslash) {
                    haveBackslash= true;
                    continue loop;
                }
                return result;

            default:
                return result;
            }
        }
        return result;
    }

    /**
     * Returns the image from the input without any modification.
     */
    public char[] getInputChars(int offset, int endOffset) {
        final int length= endOffset-offset;
        final char[] result= new char[length];
        System.arraycopy(fInput, offset, result, 0, length);
        return result;
    }

    char[] getInput() {
        return fInput;
    }
    
    /**
     * Returns the image with trigraphs replaced and line-splices removed.
     */
    private char[] getCharImage(int offset, int endOffset, int imageLength) {
        final char[] result= new char[imageLength];
        markPhase3();
        fEndOffset= offset;
        for (int idx=0; idx<imageLength; idx++) {
            result[idx]= (char) nextCharPhase3();
        }
        restorePhase3();
        return result;
    }
}
