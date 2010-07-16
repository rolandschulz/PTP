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
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.intrinsics.IntrinsicProcDescription;
import org.eclipse.photran.internal.core.intrinsics.Intrinsics;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.preferences.FortranRGBPreference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Scans for Fortran keywords to perform syntax highlighting.
 *
 * @author Jeff Overbey
 */
@SuppressWarnings("deprecation")
public class FortranKeywordRuleBasedScanner extends RuleBasedScanner
{
    ///////////////////////////////////////////////////////////////////////////
    // Constants - Words to highlight
    ///////////////////////////////////////////////////////////////////////////

    private static String[] fgKeywords = { "ACCESS", "ACTION", "ADVANCE", "ALLOCATABLE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                          "ALLOCATE", "ASSIGN", "ASSIGNMENT", "ASSOCIATE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                          "ASYNCHRONOUS", "BACKSPACE", "BIND", "BLANK", "BLOCK", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "BLOCKDATA", "CALL", "CASE", "CLOSE", "CLASS", "COMMON", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                                          "CONTAINS", "CONTINUE", "CYCLE", "DATA", "DEALLOCATE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "DEFAULT", "DELIM", "DIMENSION", "DIRECT", "DO", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "DOUBLE", "DOUBLECOMPLEX", "DOUBLEPRECISION", "ELEMENTAL", "ELSE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "ELSEIF", "ELSEWHERE", "END", "ENDBLOCK", "ENDBLOCKDATA", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "ENDDO", "ENDFILE", "ENDIF", "ENTRY", "EOR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "EQUIVALENCE", "ERR", "EXIST", "EXIT", "EXTENDS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "EXTENSIBLE", "EXTERNAL", "FILE", "FMT", "FLUSH", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "FORALL", "FORM", "FORMAT", "FORMATTED", "FUNCTION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "GOTO", "IF", "IMPLICIT", "IN", "INOUT", "INCLUDE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                                          "INQUIRE", "INTENT", "INTERFACE", "INTRINSIC", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                          "IOLENGTH", "IOSTAT", "INSTRINSIC", "KIND", "LEN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "MODULE", "NAME", "NAMED", "NAMELIST", "NEXTREC", "NML", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                                          "NONE", "NON_OVERRIDABLE", "NOPASS", "NULLIFY", "NUMBER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "ONLY", "OPEN", "OPENED", "OPERATOR", "OPTIONAL", "OUT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                                          "PAD", "PARAMETER", "PASS", "PAUSE", "POINTER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "POSITION", "PRECISION", "PRINT", "PRIVATE", "PROCEDURE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "PROGRAM", "PROTECTED", "PUBLIC", "PURE", "READ", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "READWRITE", "REC", "RECL", "RECURSIVE", "RESULT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "RETURN", "REWIND", "SAVE", "SELECT", "SEQUENCE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "SEQUENTIAL", "SIZE", "STAT", "STATUS", "STOP", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "SUBROUTINE", "TARGET", "THEN", "TO", "TYPE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "UNFORMATTED", //$NON-NLS-1$
                                          "UNIT", //$NON-NLS-1$
                                          "USE", //$NON-NLS-1$
                                          "VOLATILE", //$NON-NLS-1$
                                          "WHERE", //$NON-NLS-1$
                                          "WHILE", //$NON-NLS-1$
                                          "WRITE", //$NON-NLS-1$
                                          // Fortran 2003 keywords to highlight
                                          "EXTENDS", "ABSTRACT", "BIND", "GENERIC", "PASS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "NOPASS", "NON_OVERRIDABLE", "DEFERRED", "FINAL", "ENUM", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "ENUMERATOR", "CLASS", "VALUE", "ASSOCIATE", "IS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "WAIT", //$NON-NLS-1$
                                          "NON_INTRINSIC", //$NON-NLS-1$
                                          "IMPORT", //$NON-NLS-1$
                                          // Fortran 2008 keywords to highlight
                                          "SUBMODULE", "ENDSUBMODULE", "ENDPROCEDURE", "IMPURE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                          "CODIMENSION", "CONTIGUOUS", "CRITICAL", "ENDCRITICAL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                          "ALL", "ALLSTOP", "SYNC", "SYNCALL", "SYNCIMAGES", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                          "IMAGES", "SYNCMEMORY", "MEMORY", "LOCK", "UNLOCK" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    //private static String[] fgTextualOperators = { ".AND.", ".EQ.", ".EQV.", ".FALSE.", ".GE.", ".GT.", ".LE.", ".LT.", ".NE.", ".NEQV.", ".NOT.", ".OR.", ".TRUE." };
    private static String[] fgTextualOperators = { "AND", "EQ", "EQV", "FALSE", "GE", "GT", "LE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                                                   "LT", "NE", "NEQV", "NOT", "OR", "TRUE" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    private static String[] fgTypes = { "REAL", "INTEGER", "CHARACTER", "LOGICAL", "COMPLEX" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private static String[] fgPreprocessor = { "INCLUDE", "#include", "#error", "#warning", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                              "#pragma", "#ifdef", "#ifndef", "#if", "#else", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                              "#elif", "#endif", "#line" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    ///////////////////////////////////////////////////////////////////////////
    // Fields - colors
    ///////////////////////////////////////////////////////////////////////////

    private Token colorCpp = createTokenFromRGBPreference(FortranPreferences.COLOR_CPP);

    private Token colorStrings = createTokenFromRGBPreference(FortranPreferences.COLOR_STRINGS);

    private Token colorComments = createTokenFromRGBPreference(FortranPreferences.COLOR_COMMENTS);

    private Token colorIdentifiers = createTokenFromRGBPreference(FortranPreferences.COLOR_IDENTIFIERS);

    private Token colorIntrinsics = createTokenFromRGBPreference(FortranPreferences.COLOR_INTRINSICS);

    private Token colorKeywords = createTokenFromRGBPreference(FortranPreferences.COLOR_KEYWORDS);

    private static Token createTokenFromRGBPreference(FortranRGBPreference p)
    {
        int style;
        if (p == FortranPreferences.COLOR_KEYWORDS || p == FortranPreferences.COLOR_CPP)
            style = SWT.BOLD;
        else if (p == FortranPreferences.COLOR_INTRINSICS)
            style = SWT.ITALIC;
        else
            style = SWT.NONE;

        return new Token(new TextAttribute(new Color(null, p.getValue()), null, style));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    public FortranKeywordRuleBasedScanner(boolean isFixedForm, ISourceViewer sourceViewer)
    {
        FortranCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(new PreferenceChangeListener(sourceViewer));

        IRule[] rules = new IRule[isFixedForm ? 6+4+1 : 6];
        int i = 0;

        rules[i++] = new EndOfLineRule("#", colorCpp); //$NON-NLS-1$

        rules[i++] = new MultiLineRule("\"", "\"", colorStrings); //$NON-NLS-1$ //$NON-NLS-2$
        rules[i++] = new MultiLineRule("'", "'", colorStrings); //$NON-NLS-1$ //$NON-NLS-2$

        rules[i++] = new EndOfLineRule("!", colorComments); //$NON-NLS-1$

        if (isFixedForm)
        {
            for (char ch : new char[] { 'c', 'C', '!', '*' })
            {
                EndOfLineRule c1 = new EndOfLineRule(new String(new char[] { ch }), colorComments);
                c1.setColumnConstraint(0);
                rules[i++] = c1;
            }

            IRule c3 = new FixedFormColumnCommentRule();
            rules[i++] = c3;

            //salesRule = new FixedFormIdentifierWordRule(new FortranWordDetector(), colorIdentifiers);
            //wordRule = new FixedFormIdentifierWordRule(new FortranWordDetector(), colorIdentifiers);
        }

        Eclipse33WordRule wordRule = new Eclipse33WordRule(new FortranWordDetector(), Token.UNDEFINED, true);
        SalesScanKeywordRule salesRule = new SalesScanKeywordRule(new FortranWordDetector(), colorIdentifiers);

        createSpecialWordRules(salesRule, wordRule);
        rules[i++] = wordRule;
        rules[i++] = salesRule; // Apply Sales last, since it will mark "everything else" as an identifier

        setRules(rules);

        // Punctuation, numbers, etc.
        setDefaultReturnToken(new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 0, 0)))));
    }

    private void createSpecialWordRules(SalesScanKeywordRule salesRule, Eclipse33WordRule wordRule)
    {
        // If a word appears more than once (e.g., "real" or "len"), the LAST rule assigned to it will apply

        for (int i = 0; i < fgTextualOperators.length; i++)
            wordRule.addWord(fgTextualOperators[i], colorKeywords);

        for (IntrinsicProcDescription proc : Intrinsics.getAllIntrinsicProcedures())
            salesRule.addIdentifier(proc.genericName, colorIntrinsics);
        for (int i = 0; i < fgPreprocessor.length; i++)
            salesRule.addWord(fgPreprocessor[i], colorKeywords);
        for (int i = 0; i < fgTypes.length; i++)
            salesRule.addWord(fgTypes[i], colorKeywords);
        for (int i = 0; i < fgKeywords.length; i++)
            salesRule.addWord(fgKeywords[i], colorKeywords);
    }

    /**
     * Updates the display when the editor's colors are changed
     */
    private final class PreferenceChangeListener implements IPropertyChangeListener
    {
        private ISourceViewer sourceViewer;

        PreferenceChangeListener(ISourceViewer sourceViewer)
        {
            this.sourceViewer = sourceViewer;
        }

        public void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event)
        {
            String property = event.getProperty();
            String newVal = event.getNewValue() instanceof String ? (String)event.getNewValue() : null;

            if (property.equals(FortranPreferences.COLOR_COMMENTS.getName()))
                updateToken(colorComments, newVal);
            else if (property.equals(FortranPreferences.COLOR_CPP.getName()))
                updateToken(colorCpp, newVal);
            else if (property.equals(FortranPreferences.COLOR_STRINGS.getName()))
                updateToken(colorStrings, newVal);
            else if (property.equals(FortranPreferences.COLOR_IDENTIFIERS.getName()))
                updateToken(colorIdentifiers, newVal);
            else if (property.equals(FortranPreferences.COLOR_INTRINSICS.getName()))
                updateToken(colorIntrinsics, newVal);
            else if (property.equals(FortranPreferences.COLOR_KEYWORDS.getName()))
                updateToken(colorKeywords, newVal);
        }

        private void updateToken(Token token, String newColor)
        {
            Object data = token.getData();
            if (data instanceof TextAttribute)
            {
                TextAttribute oldAttr = (TextAttribute)data;
                token.setData(new TextAttribute(new Color(null, StringConverter.asRGB(newColor)),
                                                oldAttr.getBackground(),
                                                oldAttr.getStyle()));
            }

            // Force redraw of entire editor text
            sourceViewer.invalidateTextPresentation();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Class - for handling column-based issues in fixed form sources
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Rule for highlighting fixed form code which detects tokens past column 72
     * (or whatever column the user specified in their workspace preferences)
     * as a comment.
     */
    private final class FixedFormColumnCommentRule implements IRule
    {
        public IToken evaluate(ICharacterScanner scanner)
        {
            final int commentColumn = FortranPreferences.FIXED_FORM_COMMENT_COLUMN.getValue();
            
            IToken result = Token.UNDEFINED;
            scanner.read();
            if (scanner.getColumn() > commentColumn)
            {
                result = colorComments;
                do
                    scanner.read();
                while (scanner.getColumn() > commentColumn);
            }
            scanner.unread();
            return result;
        }
    }

    /**
     * Word detector for Fortran identifiers and generic operators.
     */
    private static final class FortranWordDetector implements IWordDetector
    {
        public boolean isWordStart(char c)
        {
            return Character.isJavaIdentifierStart(c);
        }

        public boolean isWordPart(char c)
        {
            return Character.isJavaIdentifierPart(c);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Class - WordRule from Eclipse 3.3
    ///////////////////////////////////////////////////////////////////////////

    /*******************************************************************************
     * Copyright (c) 2000, 2006 IBM Corporation and others.
     * All rights reserved. This program and the accompanying materials
     * are made available under the terms of the Eclipse Public License v1.0
     * which accompanies this distribution, and is available at
     * http://www.eclipse.org/legal/epl-v10.html
     *
     * Contributors:
     *     IBM Corporation - initial API and implementation
     *******************************************************************************/
    /**
     * An implementation of <code>IRule</code> capable of detecting words
     * Word rules also allow for the association of tokens with specific words.
     * That is, not only can the rule be used to provide tokens for exact matches,
     * but also for the generalized notion of a word in the context in which it is used.
     * A word rules uses a word detector to determine what a word is.
     *
     * @see IWordDetector
     */
    private static class Eclipse33WordRule implements IRule {

        /** Internal setting for the un-initialized column constraint. */
        protected static final int UNDEFINED= -1;

        /** The word detector used by this rule. */
        protected IWordDetector fDetector;
        /** The default token to be returned on success and if nothing else has been specified. */
        protected IToken fDefaultToken;
        /** The column constraint. */
        protected int fColumn= UNDEFINED;
        /** The table of predefined words and token for this rule. */
        protected Map<String, IToken> fWords= new HashMap<String, IToken>();
        /** Buffer used for pattern detection. */
        private StringBuffer fBuffer= new StringBuffer();
        /**
         * Tells whether this rule is case sensitive.
         * @since 3.3
         */
        private boolean fIgnoreCase= false;

        /**
         * Creates a rule which, with the help of an word detector, will return the token
         * associated with the detected word. If no token has been associated, the scanner
         * will be rolled back and an undefined token will be returned in order to allow
         * any subsequent rules to analyze the characters.
         *
         * @param detector the word detector to be used by this rule, may not be <code>null</code>
         * @see #addWord(String, IToken)
         */
        @SuppressWarnings("unused")
        public Eclipse33WordRule(IWordDetector detector) {
            this(detector, Token.UNDEFINED, false);
        }

        /**
         * Creates a rule which, with the help of a word detector, will return the token
         * associated with the detected word. If no token has been associated, the
         * specified default token will be returned.
         *
         * @param detector the word detector to be used by this rule, may not be <code>null</code>
         * @param defaultToken the default token to be returned on success
         *          if nothing else is specified, may not be <code>null</code>
         * @see #addWord(String, IToken)
         */
        @SuppressWarnings("unused")
        public Eclipse33WordRule(IWordDetector detector, IToken defaultToken) {
            this(detector, defaultToken, false);
        }

        /**
         * Creates a rule which, with the help of a word detector, will return the token
         * associated with the detected word. If no token has been associated, the
         * specified default token will be returned.
         *
         * @param detector the word detector to be used by this rule, may not be <code>null</code>
         * @param defaultToken the default token to be returned on success
         *          if nothing else is specified, may not be <code>null</code>
         * @param ignoreCase the case sensitivity associated with this rule
         * @see #addWord(String, IToken)
         * @since 3.3
         */
        public Eclipse33WordRule(IWordDetector detector, IToken defaultToken, boolean ignoreCase) {
            Assert.isNotNull(detector);
            Assert.isNotNull(defaultToken);

            fDetector= detector;
            fDefaultToken= defaultToken;
            fIgnoreCase= ignoreCase;
        }

        /**
         * Adds a word and the token to be returned if it is detected.
         *
         * @param word the word this rule will search for, may not be <code>null</code>
         * @param token the token to be returned if the word has been found, may not be <code>null</code>
         */
        public void addWord(String word, IToken token) {
            Assert.isNotNull(word);
            Assert.isNotNull(token);

            fWords.put(word, token);
        }

        /**
         * Sets a column constraint for this rule. If set, the rule's token
         * will only be returned if the pattern is detected starting at the
         * specified column. If the column is smaller then 0, the column
         * constraint is considered removed.
         *
         * @param column the column in which the pattern starts
         */
        @SuppressWarnings("unused")
        public void setColumnConstraint(int column) {
            if (column < 0)
                column= UNDEFINED;
            fColumn= column;
        }

        /*
         * @see IRule#evaluate(ICharacterScanner)
         */
        public IToken evaluate(ICharacterScanner scanner) {
            int c= scanner.read();
            if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
                if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

                    fBuffer.setLength(0);
                    do {
                        fBuffer.append((char) c);
                        c= scanner.read();
                    } while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
                    scanner.unread();

                    String buffer= fBuffer.toString();
                    IToken token= fWords.get(buffer);

                    if(fIgnoreCase) {
                        for (String key : fWords.keySet()) {
                            if(buffer.equalsIgnoreCase(key)) {
                                token= fWords.get(key);
                                break;
                            }
                        }
                    } else
                        token= (IToken)fWords.get(buffer);

                    if (token != null)
                        return token;

                    if (fDefaultToken.isUndefined())
                        unreadBuffer(scanner);

                    return fDefaultToken;
                }
            }

            scanner.unread();
            return Token.UNDEFINED;
        }

        /**
         * Returns the characters in the buffer to the scanner.
         *
         * @param scanner the scanner to be used
         */
        protected void unreadBuffer(ICharacterScanner scanner) {
            for (int i= fBuffer.length() - 1; i >= 0; i--)
                scanner.unread();
        }

    }
}
