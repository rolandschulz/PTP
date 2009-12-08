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
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.preferences.FortranRGBPreference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Scans for for Fortran keywords (for syntax highlighting)
 *
 * @author Jeff Overbey
 */
public class FortranKeywordRuleBasedScanner extends RuleBasedScanner
{
    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes - for handling column-based issues in fixed form sources
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Rule which detects any string occurring past column 72 as a comment
     */
    private final class FixedFormColumn72CommentRule implements IRule
    {
        public IToken evaluate(ICharacterScanner scanner)
        {
            IToken result = Token.UNDEFINED;
            scanner.read();
            if (scanner.getColumn() > 72)
            {
                result = colorComments;
                do
                    scanner.read();
                while (scanner.getColumn() > 72);
            }
            scanner.unread();
            return result;
        }
    }

//    /**
//     * Rule which detects identifiers between columns 7 and 72
//     *
//     * @see org.eclipse.jface.text.rules.WordRule
//     */
//    private static final class FixedFormIdentifierWordRule extends WordRule
//    {
//        private StringBuffer fBuffer = new StringBuffer();
//
//        private FixedFormIdentifierWordRule(IWordDetector detector, IToken token)
//        {
//            super(detector, token);
//        }
//
//        public IToken evaluate(ICharacterScanner scanner)
//        {
//            // int c= scanner.read();
//            // boolean canStart = fDetector.isWordStart((char)c) && scanner.getColumn() >= 7;
//            // scanner.unread();
//            // return canStart ? super.evaluate(scanner) : Token.UNDEFINED;
//
//            int c = scanner.read();
//            if (fDetector.isWordStart((char)c) && scanner.getColumn() >= 7)
//            {
//                fBuffer.setLength(0);
//                do
//                {
//                    fBuffer.append((char)c);
//                    c = scanner.read();
//                }
//                while (c != ICharacterScanner.EOF && fDetector.isWordPart((char)c) && scanner.getColumn() <= 72);
//                scanner.unread();
//
//                IToken token = (IToken)fWords.get(fBuffer.toString());
//                if (token != null) return token;
//
//                if (fDefaultToken.isUndefined()) unreadBuffer(scanner);
//
//                return fDefaultToken;
//            }
//
//            scanner.unread();
//            return Token.UNDEFINED;
//        }
//    }

    /**
     * Word detector for Fortran identifiers and generic operators
     */
    private static final class FortranWordDetector implements IWordDetector
    {
        private boolean startedWithDigit = false;
        private boolean startedWithPeriod = false;
        private boolean reachedSecondPeriod = false;
        
        public boolean isWordStart(char c)
        {
            if (c == '.')
            {
                startedWithPeriod = true;
                reachedSecondPeriod = false;
                startedWithDigit = false;
                return true;
            }
            else if (Character.isDigit(c))
            {
                startedWithPeriod = false;
                reachedSecondPeriod = false;
                startedWithDigit = true;
                return true;
            }
            else return Character.isJavaIdentifierStart(c);
        }

        public boolean isWordPart(char c)
        {
            if (startedWithPeriod && reachedSecondPeriod)
                return false;
            else if (c == '.' && (startedWithPeriod || startedWithDigit))
            {
                reachedSecondPeriod = true;
                return true;
            }
            else
                return Character.isJavaIdentifierPart(c);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Class - updates the display when the editor's colors are changed
    ///////////////////////////////////////////////////////////////////////////

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
    // Constants - Words to highlight
    ///////////////////////////////////////////////////////////////////////////

    private static String[] fgKeywords = { "ACCESS", "ACTION", "ADVANCE", "ALLOCATABLE", "ALLOCATE", "ASSIGN", "ASSIGNMENT", "ASSOCIATE", "ASYNCHRONOUS", "BACKSPACE",
        "BIND", "BLANK", "BLOCK", "BLOCKDATA", "CALL", "CASE", "CLOSE", "CLASS", "COMMON", "CONTAINS", "CONTINUE", "CYCLE", "DATA", "DEALLOCATE", "DEFAULT", "DELIM", "DIMENSION",
        "DIRECT", "DO", "DOUBLE", "DOUBLEPRECISION", "ELSE", "ELSEIF", "ELSEWHERE", "END", "ENDBLOCK", "ENDBLOCKDATA", "ENDDO", "ENDFILE", "ENDIF", "ENTRY", "EOR", "EQUIVALENCE", "ERR", "EXIST", "EXIT",
        "EXTENDS", "EXTENSIBLE", "EXTERNAL", "FILE", "FMT", "FLUSH", "FORALL", "FORM", "FORMAT", "FORMATTED", "FUNCTION", "GO", "IF", "IMPLICIT", "IN", "INOUT",
        "INCLUDE", "INQUIRE", "INTENT", "INTERFACE", "INTRINSIC", "IOLENGTH", "IOSTAT", "INSTRINSIC", "KIND", "LEN", "MODULE", "NAME", "NAMED", "NAMELIST", "NEXTREC",
        "NML", "NONE", "NON_OVERRIDABLE", "NOPASS", "NULLIFY", "NUMBER", "ONLY", "OPEN", "OPENED", "OPERATOR", "OPTIONAL", "OUT", "PAD", "PARAMETER", "PASS", "PAUSE",
        "POINTER", "POSITION", "PRECISION", "PRINT", "PRIVATE", "PROCEDURE", "PROGRAM", "PROTECTED", "PUBLIC", "PURE", "READ", "READWRITE", "REC", "RECL", "RECURSIVE", "RESULT",
        "RETURN", "REWIND", "SAVE", "SELECT", "SEQUENCE", "SEQUENTIAL", "SIZE", "STAT", "STATUS", "STOP", "SUBROUTINE", "TARGET", "THEN", "TO", "TYPE", "UNFORMATTED",
        "UNIT", "USE", "VOLATILE", "WHERE", "WHILE", "WRITE",
        // Fortran 2003 keywords to highlight
        "EXTENDS", "ABSTRACT", "BIND", "GENERIC", "PASS", "NOPASS", "NON_OVERRIDABLE", "DEFERRED", "FINAL",
        "ENUM", "ENUMERATOR", "CLASS", "VALUE", "ASSOCIATE", "IS",
        "WAIT", "NON_INTRINSIC", "IMPORT",
        // Fortran 2008 keywords to highlight
        "SUBMODULE", "ENDSUBMODULE", "ENDPROCEDURE", "IMPURE", "CODIMENSION", "CONTIGUOUS",
        "CRITICAL", "ENDCRITICAL", "ALL", "ALLSTOP", "SYNC", "SYNCALL", "SYNCIMAGES",
        "IMAGES", "SYNCMEMORY", "MEMORY", "LOCK", "UNLOCK"
        };

    private static String[] fgTextualOperators = { ".AND.", ".EQ.", ".EQV.", ".FALSE.", ".GE.", ".GT.", ".LE.", ".LT.", ".NE.", ".NEQV.", ".NOT.", ".OR.", ".TRUE." };

    private static String[] fgIntrinsics =
    {
         // From Metcalf and Reid, "Fortran 90/95 Explained", Chapter 8
         // Functions
         "associated",
         "present",
         "kind",
         "abs",
         "aimag",
         "aint",
         "anint",
         "ceiling",
         "cmplx",
         "floor",
         "int",
         "nint",
         "real",
         "conjg",
         "dim",
         "max",
         "min",
         "mod",
         "modulo",
         "sign",
         "acos",
         "asin",
         "atan",
         "atan2",
         "cos",
         "cosh",
         "exp",
         "log",
         "log10",
         "sin",
         "sinh",
         "sqrt",
         "tan",
         "tanh",
         "achar",
         "char",
         "iachar",
         "ichar",
         "lge",
         "lgt",
         "lle",
         "llt",
         "adjustl",
         "adjustr",
         "index",
         "len_trim",
         "scan",
         "verify",
         "logical",
         "len",
         "repeat",
         "trim",
         "digits",
         "epsilon",
         "huge",
         "maxexponent",
         "minexponent",
         "precision",
         "radix",
         "range",
         "tiny",
         "exponent",
         "fraction",
         "nearest",
         "rrspacing",
         "scale",
         "set_exponent",
         "spacing",
         "selected_int_kind",
         "selected_real_kind",
         "bit_size",
         "btest",
         "iand",
         "ibclr",
         "ibits",
         "ibset",
         "ieor",
         "ior",
         "ishft",
         "ishftc",
         "not",
         "dot_product",
         "matmul",
         "all",
         "any",
         "count",
         "maxval",
         "minval",
         "product",
         "sum",
         "allocated",
         "lbound",
         "shape",
         "size",
         "ubound",
         "merge",
         "pack",
         "unpack",
         "reshape",
         "spread",
         "cshift",
         "eoshift",
         "transpose",
         "maxloc",
         "minloc",
         "null",

         // Subroutines
         "mvbits",
         "date_and_time",
         "system_clock",
         "cpu_time",
         "random_number",
         "random_seed"
    };

    /* { "ABS", "ACHAR", "ACOS", "ADJUSTL", "ADJUSTR", "AIMAG", "AINT", "ALL", "ALLOCATED", "AND", "ANINT", "ANY", "ASIN",
        "ASSOCIATED", "ATAN", "ATAN2", "BIT_SIZE", "BTEST", "CEILING", "CHAR", "CMPLX", "CONJG", "COS", "COSH", "COUNT", "CSHIFT", "DATE_AND_TIME", "DBLE", "DIGITS",
        "DIM", "DOT_PRODUCT", "DPROD", "EOSHIFT", "EPSILON", "EQV", "EXP", "EXPONENT", "FALSE", "FLOOR", "FRACTION", "HUGE", "IACHAR", "IAND", "IBCLR", "IBITS", "IBSET",
        "ICHAR", "IEOR", "INDEX", "INT", "IOR", "ISHFT", "ISHFTC", "KIND", "LBOUND", "LEN", "LEN_TRIM", "LGE", "LGT", "LLE", "LLT", "LOG", "LOG10", "LOGICAL", "MATMUL",
        "MAX", "MAXEXPONENT", "MAXLOC", "MAXVAL", "MERGE", "MIN", "MINEXPONENT", "MINLOC", "MINVAL", "MOD", "MODULO", "MVBITS", "NEAREST", "NEQV", "NINT", "NOT", "OR",
        "PACK", "PRECISION", "PRESENT", "PRODUCT", "RADIX", "RANDOM_NUMBER", "RANDOM_SEED", "RANGE", "REAL", "REPEAT", "RESHAPE", "RRSPACING", "SCALE", "SCAN",
        "SELECTED_INT_KIND", "SELECTED_REAL_KIND", "SET_EXPONENT", "SHAPE", "SIGN", "SIN", "SINH", "SIZE", "SPACING", "SPREAD", "SQRT", "SUM", "SYSTEM_CLOCK", "TAN",
        "TANH", "TINY", "TRANSFER", "TRANSPOSE", "TRIM", "TRUE", "UBOUND", "UNPACK", "VERIFY" }; */

    private static String[] fgTypes = { "REAL", "INTEGER", "CHARACTER", "LOGICAL", "COMPLEX" };

    private static String[] fgPreprocessor = { "INCLUDE", "#include", "#error", "#warning", "#pragma", "#ifdef", "#ifndef", "#if", "#else", "#elif", "#endif", "#line" };

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

        IRule[] rules = new IRule[isFixedForm ? 6+(128-33+1) : 6];
        int i = 0;

        rules[i++] = new EndOfLineRule("#", colorCpp);

        rules[i++] = new MultiLineRule("\"", "\"", colorStrings);
        rules[i++] = new MultiLineRule("'", "'", colorStrings);

        rules[i++] = new EndOfLineRule("!", colorComments);

        if (isFixedForm)
        {
            for (char ch = 33; ch < 128; ch++)
            {
                EndOfLineRule c1 = new EndOfLineRule(new String(new char[] { ch }), colorComments);
                c1.setColumnConstraint(0);
                rules[i++] = c1;
            }

            IRule c3 = new FixedFormColumn72CommentRule();
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

        for (int i = 0; i < fgIntrinsics.length; i++)
            salesRule.addIdentifier(fgIntrinsics[i], colorIntrinsics);
        for (int i = 0; i < fgPreprocessor.length; i++)
            salesRule.addWord(fgPreprocessor[i], colorKeywords);
        for (int i = 0; i < fgTypes.length; i++)
            salesRule.addWord(fgTypes[i], colorKeywords);
        for (int i = 0; i < fgKeywords.length; i++)
            salesRule.addWord(fgKeywords[i], colorKeywords);
    }

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
        protected Map fWords= new HashMap();
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
                    IToken token= (IToken)fWords.get(buffer);

                    if(fIgnoreCase) {
                        Iterator iter= fWords.keySet().iterator();
                        while (iter.hasNext()) {
                            String key= (String)iter.next();
                            if(buffer.equalsIgnoreCase(key)) {
                                token= (IToken)fWords.get(key);
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
