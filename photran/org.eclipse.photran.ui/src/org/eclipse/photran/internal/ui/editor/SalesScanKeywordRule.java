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
package org.eclipse.photran.internal.ui.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * Syntax highlighting rule which determines whether a word should be a keyword or an identifier.
 * <p>
 * The logic is based on Sale's algorithm, extended to handle Fortran 90, 2003, and 2008.
 * <p>
 * This is only vaguely similar to FreeFormLexerPhase2; this version is much faster but less
 * precise, as it is based on a character-by-character scan rather than a proper tokenization.
 * It only scans one line at a time and is therefore oblivious to continuation lines; this
 * makes it highlight continuation lines incorrectly (see Bugs 302318 and 301712).
 *
 * @see org.eclipse.jface.text.rules.WordRule
 *
 * @author Jeff Overbey (based on WordRule)
 */
public class SalesScanKeywordRule extends WordRule implements IRule
{
    /** The word detector used by this rule. */
    protected IWordDetector fDetector;

    /** The default token to be returned on success and if nothing else has been specified. */
    protected IToken fDefaultToken;

    /** The table of predefined keywords and token for this rule. */
    protected Map fWords = new HashMap();

    /** The table of predefined identifiers and token for this rule. */
    protected Map fIdentifiers = new HashMap();

    /** Buffer used for pattern detection. */
    private StringBuffer fBuffer = new StringBuffer();

    /** Buffer used to store the entire line of text. */
    private StringBuffer fLineBuffer = new StringBuffer();

    /** The (1-based) column on which the scanned word starts. */
    private int fWordCol = 0;

    /**
     * Creates a rule which, with the help of an word detector, will return the token associated
     * with the detected word. If no token has been associated, the scanner will be rolled back and
     * an undefined token will be returned in order to allow any subsequent rules to analyze the
     * characters.
     *
     * @param detector the word detector to be used by this rule, may not be <code>null</code>
     * @see #addWord(String, IToken)
     */
    public SalesScanKeywordRule(IWordDetector detector)
    {
        this(detector, Token.UNDEFINED);
    }

    /**
     * Creates a rule which, with the help of a word detector, will return the token associated with
     * the detected word. If no token has been associated, the specified default token will be
     * returned.
     *
     * @param detector the word detector to be used by this rule, may not be <code>null</code>
     * @param defaultToken the default token to be returned on success if nothing else is specified,
     *            may not be <code>null</code>
     * @see #addWord(String, IToken)
     */
    public SalesScanKeywordRule(IWordDetector detector, IToken defaultToken)
    {
        super(detector);

        Assert.isNotNull(detector);
        Assert.isNotNull(defaultToken);

        fDetector = detector;
        fDefaultToken = defaultToken;
    }

    /**
     * Adds a keyword and the token to be returned if it is used as a keyword
     * rather than an identifier.
     *
     * @param word the word this rule will search for, may not be <code>null</code>
     * @param token the token to be returned if the word has been found, may not be
     *            <code>null</code>
     */
    public void addWord(String word, IToken token)
    {
        Assert.isNotNull(word);
        Assert.isNotNull(token);

        fWords.put(word, token);
    }

    /**
     * Adds an identifier and the token to be returned if it is not used as a keyword.
     *
     * @param word the word this rule will search for, may not be <code>null</code>
     * @param token the token to be returned if the word has been found, may not be
     *            <code>null</code>
     */
    public void addIdentifier(String word, IToken token)
    {
        Assert.isNotNull(word);
        Assert.isNotNull(token);

        fIdentifiers.put(word, token);
    }

    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        int c = scanner.read();
        if (c != ICharacterScanner.EOF && fDetector.isWordStart((char)c))
        {
                scanner.unread();
                populateBuffers(scanner);

                String buffer = fBuffer.toString();
                IToken token = (IToken)fWords.get(buffer);

                Iterator iter = fWords.keySet().iterator();
                while (iter.hasNext())
                {
                    String key = (String)iter.next();
                    if (buffer.equalsIgnoreCase(key))
                    {
                        token = (IToken)fWords.get(key);
                        break;
                    }
                }

                if (token != null)
                    return salesScan(token, (IToken)fIdentifiers.get(buffer));

                if (fDefaultToken.isUndefined())
                    for (int i = fBuffer.length() - 1; i >= 0; i--)
                        scanner.unread();

                return fIdentifiers.containsKey(buffer) ? (IToken)fIdentifiers.get(buffer) : fDefaultToken;
        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    private void populateBuffers(ICharacterScanner scanner)
    {
        fBuffer.setLength(0);
        fLineBuffer.setLength(0);

        readPrefix(scanner);
        readWord(scanner);
        readSuffix(scanner);

        fixLineBuffer();
    }

    private void readPrefix(ICharacterScanner scanner)
    {
        fWordCol = scanner.getColumn();

        for (int i = 0; i < fWordCol; i++)
            scanner.unread();

        for (int i = 0; i < fWordCol; i++)
            fLineBuffer.append((char)scanner.read());

    }

    private void readWord(ICharacterScanner scanner)
    {
        int c = scanner.read();
        do
        {
            fBuffer.append((char)c);
            fLineBuffer.append((char)c);
            c = scanner.read();
        }
        while (c != ICharacterScanner.EOF && fDetector.isWordPart((char)c));
        scanner.unread();
    }

    // There is a better way to detect end-of-lines
    // (see org.eclipse.jface.text.rules.PatternRule#endSequenceDetected)
    private void readSuffix(ICharacterScanner scanner)
    {
        int startCol = scanner.getColumn();
        int count = 1;

        int c = scanner.read();
        if (c == ICharacterScanner.EOF || scanner.getColumn() < startCol) return;
        do
        {
            count++;
            fLineBuffer.append((char)c);
            c = scanner.read();
        }
        while (c != ICharacterScanner.EOF && scanner.getColumn() > startCol);

        for (int i = 0; i < count; i++)
            scanner.unread();
    }

    private void fixLineBuffer()
    {
        removeStringLiterals();
        removeTrailingComment();
        removeConcatenatedStatements();
    }

    private void removeStringLiterals()
    {
        boolean inString = false;

        for (int i = 0, length = fLineBuffer.length(); i < length; i++)
        {
            char thisChar = fLineBuffer.charAt(i);
            char nextChar = i+1 < length ? fLineBuffer.charAt(i+1) : '\0';

            if ((thisChar == '\"' || thisChar == '\'') && !inString)
                inString = true;
            else if ((thisChar == '\"' || thisChar == '\'') && inString)
                inString = (nextChar == '\"');

            if (inString || thisChar == '\"' || thisChar == '\'')
                fLineBuffer.setCharAt(i, ' ');
        }
    }

    private void removeTrailingComment()
    {
       int excl = fLineBuffer.lastIndexOf("!");
       if (excl >= 0)
       {
           for (int i = excl, length = fLineBuffer.length(); i < length; i++)
               fLineBuffer.setCharAt(i, ' ');
       }
    }

    private void removeConcatenatedStatements()
    {
        int precedingSemicolon = fLineBuffer.indexOf(";");
        while (precedingSemicolon > 0 && precedingSemicolon < fWordCol)
        {
            for (int i = 0; i <= precedingSemicolon; i++)
                fLineBuffer.setCharAt(i, ' ');

            precedingSemicolon = fLineBuffer.indexOf(";", precedingSemicolon+1);
        }

        int followingSemicolon = fLineBuffer.indexOf(";", fWordCol);
        if (followingSemicolon > 0)
            for (int i = followingSemicolon, length = fLineBuffer.length(); i < length; i++)
                fLineBuffer.setCharAt(i, ' ');
    }

    private IToken salesScan(IToken tokenIfKeyword, IToken tokenIfIdentifier)
    {
        SalesScanner salesScanner = new SalesScanner(fLineBuffer.toString(), fBuffer.toString());
        boolean result = salesScanner.retainAsKeyword(fWordCol);

//        System.out.println();
//        System.out.println("\"" + fLineBuffer + "\"");
//        System.out.println("\"" + fBuffer + "\"");
//        System.out.println("Position " + fWordCol);
//        System.out.println("First token at column " + (salesScanner.firstTokenPos));
//        System.out.println("First token following ) at " + (salesScanner.tokenFollowingParentheticalPos));
//        System.out.println("Open context comma?   " + salesScanner.openContextComma);
//        System.out.println("Open context equals?  " + salesScanner.openContextEquals);
//        System.out.println("Letter follows paren? " + salesScanner.letterFollowsParenthetical);

        return result ? tokenIfKeyword : (tokenIfIdentifier == null ? fDefaultToken : tokenIfIdentifier);
    }

    /**
     * Sale's algorithm determines whether the first token is a keyword
     * or an identifier as well as the first token after the first
     * closing parenthesis at the top level.
     * <p>
     * To apply Sale's algorithm, we must identify whether there is
     * <ul>
     * <li>an open context comma,
     * <li>an open context equals, and/or
     * <li>a letter immediately following a right parenthesis
     * </ul>
     */
    private static class SalesScanner
    {
        private String line, keyword;
        private int length;
        private int pos;

        private boolean lineContainsColonColon;
        private boolean[] inParens;

        private boolean openContextComma;
        private boolean openContextEquals;
        private boolean letterFollowsParenthetical;

        private int firstTokenPos = -1;
        private int tokenFollowingParentheticalPos = -1;

        public SalesScanner(String line, String keyword)
        {
            this.line = line;
            this.keyword = keyword;
            this.length = line.length();
            this.pos = 0;
            this.inParens = new boolean[line.length()];

            this.firstTokenPos = findFirstToken();
            scan();
        }

        private int findFirstToken()
        {
            int start = 0;

            int cc = line.indexOf("::");
            lineContainsColonColon = cc > 0;
            if (lineContainsColonColon) start = cc + 2;

            int pos = skipWhitespace(start);
            if (pos >= 0 && Character.isDigit(line.charAt(pos)))
            {
                // Line (possibly) starts with an integer label
                pos = skipInteger(pos);
                pos = Math.max(pos, 0);
                pos = skipWhitespace(pos);
            }
            return pos;
        }

        private int skipWhitespace(int start)
        {
            for (pos = start; pos < length; pos++)
                if (!Character.isWhitespace(line.charAt(pos)))
                    return pos;

            return -1;
        }

        private int skipInteger(int start)
        {
            for (pos = start; pos < length; pos++)
                if (!Character.isDigit(line.charAt(pos)))
                    return pos;

            return -1;
        }

        private void scan()
        {
            openContextComma = false;
            openContextEquals = false;
            letterFollowsParenthetical = false;

            int currentParenDepth = 0;          // Track nested parentheses
            boolean justClosedParen = false;    // Does this token immediately follow T_RPAREN?
            boolean firstParenthetical = true;  // Is this the first non-nested T_RPAREN?
            boolean inArrayLiteral = false;     // Are we inside an (/ array literal /)
            boolean followingThen = false;      // Have we seen a THEN token

            for (pos = 0; pos < length; pos++)
            {
                if (currentParenDepth == 0)
                {
                    if (match("(/"))
                        inArrayLiteral = true;
                    else if (match("/)"))
                        inArrayLiteral = false;
                    else if (match("then"))
                        followingThen = true;

                    if (!inArrayLiteral)
                    {
                        // The pos test ensures that we don't match the = in "integer, kind=3 :: if"
                        if (match(",") && pos >= firstTokenPos)
                            openContextComma = true;
                        else if (match("=") && pos >= firstTokenPos && !followingThen)
                            openContextEquals = true;

                        if (justClosedParen && firstParenthetical)
                        {
                            firstParenthetical = false;
                            tokenFollowingParentheticalPos = nextPos();

                            letterFollowsParenthetical = letterIsNext();
                        }
                    }
                }
                justClosedParen = false;
                if (match("("))
                {
                    currentParenDepth++;
                }
                else if (match(")"))
                {
                    currentParenDepth--;
                    justClosedParen = true;
                }

                inParens[pos] = currentParenDepth > 0;
            }
        }

        private boolean match(String pattern)
        {
            return match(pattern, pos);
        }

        private boolean match(String pattern, int matchAtPosition)
        {
            if (matchAtPosition < 0) return false;

            int patternLength = pattern.length();

            if (matchAtPosition + patternLength > length) return false;

            for (int i = 0; i < patternLength; i++)
                if (Character.toLowerCase(line.charAt(matchAtPosition+i)) != Character.toLowerCase(pattern.charAt(i)))
                    return false;
            return true;
        }

        private boolean letterIsNext()
        {
            for (int i = pos; i < length; i++)
            {
                if (Character.isWhitespace(line.charAt(i)))
                    continue;
                else if (Character.isLetter(line.charAt(i)))
                    return true;
                else
                    return false;
            }
            return false;
        }

        private int nextPos()
        {
            for (int i = pos; i < length; i++)
            {
                if (Character.isWhitespace(line.charAt(i)))
                    continue;
                else
                    return i;
            }
            return length-1;
        }

        public boolean retainAsKeyword(int column)
        {
//            System.out.println();
//            System.out.println("Column " + column + ": " + line.substring(column));
//            System.out.println("OC,: " + openContextComma + "\tOC=: " + openContextEquals + "\t)L: " + letterFollowsParenthetical);
//            System.out.println("Token following parenthetical starts at column " + tokenFollowingParentheticalPos);
//            int precedingKeywordOffset = findPrecedingKeyword(column);
//            String precedingKeyword = precedingKeywordAsString(column, precedingKeywordOffset);
//            System.out.println("Preceding token is " + precedingKeyword + " (column " + precedingKeywordOffset + ")");
//            System.out.println("Retain preceding token as keyword? " + internalRetainAsKeyword(precedingKeywordOffset, precedingKeyword));
//            System.out.println("Line starting at first token: " + line.substring(firstTokenPos));
//            System.out.println("Retain as keyword? " + internalRetainAsKeyword(column, keyword));

            return internalRetainAsKeyword(column, keyword);
        }

        private boolean internalRetainAsKeyword(int column, String keyword)
        {
            if (column < 0) return false;
            if (salesRetainAsKeyword(column)) return true;
            return applyKeywordRules(column, keyword);
        }

        private boolean applyKeywordRules(int column, String keyword)
        {
            // N.B. These rules apply regardless of the token preceding this one
            if (keyword.equalsIgnoreCase("only"))
                return openContextComma && match("use", firstTokenPos);
            else if (keyword.equalsIgnoreCase("result"))
                return letterFollowsParenthetical;
            else if (keyword.equalsIgnoreCase("then"))
                return !openContextEquals && !openContextComma && (match("if", firstTokenPos) || match("else", firstTokenPos));
            // BEGIN FORTRAN 2003
            else if (keyword.equalsIgnoreCase("bind"))
                return openContextComma && (match("enum", firstTokenPos) || match("type", firstTokenPos))
                    || match("function", firstTokenPos)
                    || match("subroutine", firstTokenPos);
            else if (keyword.equalsIgnoreCase("procedure"))
                return match("procedure", firstTokenPos) || match("module", firstTokenPos) /* F08 */  || match("end", firstTokenPos);
            else if (keyword.equalsIgnoreCase("pointer"))
                return openContextComma;
            else if (keyword.equalsIgnoreCase("operator")
                || keyword.equalsIgnoreCase("assignment")
                || keyword.equalsIgnoreCase("read")
                || keyword.equalsIgnoreCase("write"))
                return lineContainsColonColon;
            // END FORTRAN 2003
            else
            {
                int precedingKeywordOffset = findPrecedingKeyword(column);
                if (precedingKeywordOffset == column) return false;
                String precedingKeyword = precedingKeywordAsString(column, precedingKeywordOffset);
                if (!internalRetainAsKeyword(precedingKeywordOffset, precedingKeyword)) return false;
                return applyPrecedingKeywordRules(keyword, precedingKeyword);
            }
        }

        private boolean applyPrecedingKeywordRules(String keyword, String precedingKeyword)
        {
            // BEGIN FORTRAN 2008
            if (keyword.equalsIgnoreCase("module"))
                return isPrefixSpec(precedingKeyword) && !precedingKeyword.equalsIgnoreCase("module")
                    || precedingKeyword.equalsIgnoreCase("end");
            // END FORTRAN 2008
            // N.B. These rules depend on the token preceding this one
            else if (isType(keyword) && !keyword.equalsIgnoreCase("type"))
                return isPrefixSpec(precedingKeyword)|| precedingKeyword.equalsIgnoreCase("implicit");
            else if (isPrefixSpec(keyword))
                return isType(precedingKeyword);
            else if (keyword.equalsIgnoreCase("case"))
                return precedingKeyword.equalsIgnoreCase("select");
            else if (keyword.equalsIgnoreCase("data"))
                return precedingKeyword.equalsIgnoreCase("block");
            else if (keyword.equalsIgnoreCase("subroutine"))
                return precedingKeyword.equalsIgnoreCase("end") || isPrefixSpec(precedingKeyword);
            else if (keyword.equalsIgnoreCase("function"))
                return precedingKeyword.equalsIgnoreCase("end") || isType(precedingKeyword) || isPrefixSpec(precedingKeyword) || match("type", firstTokenPos);
            else if (keyword.equalsIgnoreCase("if"))
                return precedingKeyword.equalsIgnoreCase("else") || precedingKeyword.equalsIgnoreCase("end");
            else if (keyword.equalsIgnoreCase("none"))
                return precedingKeyword.equalsIgnoreCase("implicit");
            else if (keyword.equalsIgnoreCase("precision"))
                return precedingKeyword.equalsIgnoreCase("double");
            else if (keyword.equalsIgnoreCase("while"))
                return precedingKeyword.equalsIgnoreCase("do");
            // BEGIN FORTRAN 2003
            else if (keyword.equalsIgnoreCase("type"))
                return isPrefixSpec(precedingKeyword)
                    || precedingKeyword.equalsIgnoreCase("implicit")
                    || precedingKeyword.equalsIgnoreCase("end")
                    || precedingKeyword.equalsIgnoreCase("select");
            else if (keyword.equalsIgnoreCase("interface"))
                return precedingKeyword.equalsIgnoreCase("abstract") || precedingKeyword.equalsIgnoreCase("end");
            else if (keyword.equalsIgnoreCase("is"))
                return precedingKeyword.equalsIgnoreCase("type") || precedingKeyword.equalsIgnoreCase("class");
            else if (keyword.equalsIgnoreCase("default"))
                return precedingKeyword.equalsIgnoreCase("case") || precedingKeyword.equalsIgnoreCase("class");
            // END FORTRAN 2003
            // BEGIN FORTRAN 2008
            else if (keyword.equalsIgnoreCase("stop"))
                return precedingKeyword.equalsIgnoreCase("all");
            else if (keyword.equalsIgnoreCase("all")
                  || keyword.equalsIgnoreCase("images")
                  || keyword.equalsIgnoreCase("memory"))
                return precedingKeyword.equalsIgnoreCase("sync");
            // END FORTRAN 2008
            else
                return precedingKeyword.equalsIgnoreCase("end");
        }

        private boolean salesRetainAsKeyword(int column)
        {
            if (column == firstTokenPos)
                return retainFirstTokenAsKeyword();
            else if (column == tokenFollowingParentheticalPos)
                return retainTokenFollowingParentheticalAsKeyword();
            else if (lineContainsColonColon)
                return column < firstTokenPos && !inParens[column];
            else
                return false;
        }

        private boolean retainFirstTokenAsKeyword()
        {
            if (!openContextComma && !openContextEquals)
                return !lineContainsColonColon;
            else if (openContextEquals && !openContextComma)
                return !lineContainsColonColon
                    && letterFollowsParenthetical
                    && (match("if", firstTokenPos) || match("where", firstTokenPos));
            else if (openContextComma)
                return true;
            else if (letterFollowsParenthetical)
                return true;
            else
                return false;
        }

        private boolean retainTokenFollowingParentheticalAsKeyword()
        {
            return letterFollowsParenthetical && match("if", firstTokenPos) && !openContextEquals;
        }

        private boolean isType(String kw)
        {
            return kw.equalsIgnoreCase("character")
                || kw.equalsIgnoreCase("complex")
                || kw.equalsIgnoreCase("double")
                || kw.equalsIgnoreCase("doubleprecision")
                || kw.equalsIgnoreCase("integer")
                || kw.equalsIgnoreCase("logical")
                || kw.equalsIgnoreCase("real")
                || kw.equalsIgnoreCase("type");
        }

        private boolean isPrefixSpec(String kw)
        {
            return kw.equalsIgnoreCase("recursive")
                || kw.equalsIgnoreCase("pure")
                || kw.equalsIgnoreCase("elemental")
                || kw.equalsIgnoreCase("impure")  // F08
                || kw.equalsIgnoreCase("module"); // F08
        }

        private int findPrecedingKeyword(int wordCol)
        {
            return backOverIdentifier(backOverSpaces(wordCol-1))+1;
        }

        private int backOverSpaces(int offset)
        {
            while (offset >= 0 && Character.isWhitespace(line.charAt(offset)))
                offset--;
            return offset;
        }

        private int backOverIdentifier(int offset)
        {
            if (offset >= 0 && line.charAt(offset) == ')')
                offset = backOverParens(offset);

            while (offset >= 0 && Character.isJavaIdentifierPart(line.charAt(offset)))
                offset--;
            return offset;
        }

        private int backOverParens(int offset)
        {
            int depth = 0;

            while (offset >= 0)
            {
                if (line.charAt(offset) == ')')
                    depth++;
                else if (line.charAt(offset) == '(')
                    depth--;

                offset--;

                if (depth == 0) break;
            }

            return offset;
        }

        private String precedingKeywordAsString(int column, int precedingKeywordOffset)
        {
            String precedingKeyword = line.substring(precedingKeywordOffset, column);
            if (precedingKeyword.indexOf("(") >= 0) precedingKeyword = precedingKeyword.substring(0, precedingKeyword.indexOf("("));
            precedingKeyword = precedingKeyword.trim();
            return precedingKeyword;
        }
    }
}
