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
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.preferences.FortranRGBPreference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Simple scanner for Fortran keywords (for syntax highlighting)
 * 
 * @author Jeff Overbey based on code from Photran 1.0
 *         (by Vaishali De or Julia Dragan-Chirila?)
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

    /**
     * Rule which detects identifiers between columns 7 and 72 
     * 
     * @see org.eclipse.jface.text.rules.WordRule
     */
    private static final class FixedFormIdentifierWordRule extends WordRule
    {
        private StringBuffer fBuffer = new StringBuffer();

        private FixedFormIdentifierWordRule(IWordDetector detector, IToken token)
        {
            super(detector, token);
        }

        public IToken evaluate(ICharacterScanner scanner)
        {
            // int c= scanner.read();
            // boolean canStart = fDetector.isWordStart((char)c) && scanner.getColumn() >= 7;
            // scanner.unread();
            // return canStart ? super.evaluate(scanner) : Token.UNDEFINED;

            int c = scanner.read();
            if (fDetector.isWordStart((char)c) && scanner.getColumn() >= 7)
            {
                fBuffer.setLength(0);
                do
                {
                    fBuffer.append((char)c);
                    c = scanner.read();
                }
                while (c != ICharacterScanner.EOF && fDetector.isWordPart((char)c) && scanner.getColumn() <= 72);
                scanner.unread();

                IToken token = (IToken)fWords.get(fBuffer.toString());
                if (token != null) return token;

                if (fDefaultToken.isUndefined()) unreadBuffer(scanner);

                return fDefaultToken;
            }

            scanner.unread();
            return Token.UNDEFINED;
        }
    }

    /**
     * Word detector for Fortran identifiers and generic operators
     */
    private static final class FortranWordDetector implements IWordDetector
    {
        public boolean isWordStart(char c)
        {
            return Character.isJavaIdentifierStart(c) || c == '.';
        }

        public boolean isWordPart(char c)
        {
            return Character.isJavaIdentifierPart(c) || c == '.';
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
        "BIND", "BLANK", "BLOCK", "CALL", "CASE", "CLOSE", "CLASS", "COMMON", "CONTAINS", "CONTINUE", "CYCLE", "DATA", "DEALLOCATE", "DEFAULT", "DELIM", "DIMENSION",
        "DIRECT", "DO", "DOUBLE", "DOUBLEPRECISION", "ELSE", "ELSEWHERE", "END", "ENDDO", "ENDFILE", "ENDIF", "ENTRY", "EOR", "EQUIVALENCE", "ERR", "EXIST", "EXIT",
        "EXTENDS", "EXTENSIBLE", "EXTERNAL", "FILE", "FMT", "FLUSH", "FORALL", "FORM", "FORMAT", "FORMATTED", "FUNCTION", "GO", "IF", "IMPLICIT", "IN", "INOUT",
        "INCLUDE", "INQUIRE", "INTENT", "INTERFACE", "INTRINSIC", "IOLENGTH", "IOSTAT", "INSTRINSIC", "KIND", "LEN", "MODULE", "NAME", "NAMED", "NAMELIST", "NEXTREC",
        "NML", "NONE", "NON_OVERRIDABLE", "NOPASS", "NULLIFY", "NUMBER", "ONLY", "OPEN", "OPENED", "OPERATOR", "OPTIONAL", "OUT", "PAD", "PARAMETER", "PASS", "PAUSE",
        "POINTER", "POSITION", "PRECISION", "PRINT", "PRIVATE", "PROCEDURE", "PROGRAM", "PROTECTED", "PUBLIC", "PURE", "READ", "READWRITE", "REC", "RECL", "RECURSIVE", "RESULT",
        "RETURN", "REWIND", "SAVE", "SELECT", "SEQUENCE", "SEQUENTIAL", "SIZE", "STAT", "STATUS", "STOP", "SUBROUTINE", "TARGET", "THEN", "TO", "TYPE", "UNFORMATTED",
        "UNIT", "USE", "VOLATILE", "WHERE", "WHILE", "WRITE" };

    private static String[] fgTextualOperators = { ".EQ.", ".EQV.", ".FALSE.", ".GE.", ".GT.", ".LE.", ".NE.", ".NEQV.", ".NOT.", ".OR.", ".TRUE." };

    private static String[] fgIntrinsics = { "ABS", "ACHAR", "ACOS", "ADJUSTL", "ADJUSTR", "AIMAG", "AINT", "ALL", "ALLOCATED", "AND", "ANINT", "ANY", "ASIN",
        "ASSOCIATED", "ATAN", "ATAN2", "BIT_SIZE", "BTEST", "CEILING", "CHAR", "CMPLX", "CONJG", "COS", "COSH", "COUNT", "CSHIFT", "DATE_AND_TIME", "DBLE", "DIGITS",
        "DIM", "DOT_PRODUCT", "DPROD", "EOSHIFT", "EPSILON", "EQV", "EXP", "EXPONENT", "FALSE", "FLOOR", "FRACTION", "HUGE", "IACHAR", "IAND", "IBCLR", "IBITS", "IBSET",
        "ICHAR", "IEOR", "INDEX", "INT", "IOR", "ISHFT", "ISHFTC", "KIND", "LBOUND", "LEN", "LEN_TRIM", "LGE", "LGT", "LLE", "LLT", "LOG", "LOG10", "LOGICAL", "MATMUL",
        "MAX", "MAXEXPONENT", "MAXLOC", "MAXVAL", "MERGE", "MIN", "MINEXPONENT", "MINLOC", "MINVAL", "MOD", "MODULO", "MVBITS", "NEAREST", "NEQV", "NINT", "NOT", "OR",
        "PACK", "PRECISION", "PRESENT", "PRODUCT", "RADIX", "RANDOM_NUMBER", "RANDOM_SEED", "RANGE", "REAL", "REPEAT", "RESHAPE", "RRSPACING", "SCALE", "SCAN",
        "SELECTED_INT_KIND", "SELECTED_REAL_KIND", "SET_EXPONENT", "SHAPE", "SIGN", "SIN", "SINH", "SIZE", "SPACING", "SPREAD", "SQRT", "SUM", "SYSTEM_CLOCK", "TAN",
        "TANH", "TINY", "TRANSFER", "TRANSPOSE", "TRIM", "TRUE", "UBOUND", "UNPACK", "VERIFY" };

    private static String[] fgTypes = { "REAL", "INTEGER", "CHARACTER", "LOGICAL", "COMPLEX" };

    private static String[] fgPreprocessor = { "INCLUDE", "#include", "#error", "#warning", "#pragma", "#ifdef", "#ifndef", "#if", "#else", "#elif", "#endif", "#line" };

    ///////////////////////////////////////////////////////////////////////////
    // Fields - colors
    ///////////////////////////////////////////////////////////////////////////
    
    private Token colorStrings = createTokenFromRGBPreference(FortranPreferences.COLOR_STRINGS);

    private Token colorComments = createTokenFromRGBPreference(FortranPreferences.COLOR_COMMENTS);

    private Token colorIdentifiers = createTokenFromRGBPreference(FortranPreferences.COLOR_IDENTIFIERS);

    private Token colorIntrinsics = createTokenFromRGBPreference(FortranPreferences.COLOR_INTRINSICS);

    private Token colorKeywords = createTokenFromRGBPreference(FortranPreferences.COLOR_KEYWORDS);

    private static Token createTokenFromRGBPreference(FortranRGBPreference p)
    {
        int style = (p == FortranPreferences.COLOR_KEYWORDS ? SWT.BOLD : (p == FortranPreferences.COLOR_INTRINSICS ? SWT.ITALIC : SWT.NONE));
        return new Token(new TextAttribute(new Color(null, p.getValue()), null, style));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////
    
    public FortranKeywordRuleBasedScanner(boolean isFixedForm, ISourceViewer sourceViewer)
    {
        FortranCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(new PreferenceChangeListener(sourceViewer));

        IRule[] rules = new IRule[isFixedForm ? 8 : 5];
        int i = 0;

        rules[i++] = new MultiLineRule("\"", "\"", colorStrings);
        rules[i++] = new MultiLineRule("'", "'", colorStrings);

        rules[i++] = new EndOfLineRule("!", colorComments);
        
        WordRule salesRule, wordRule;
        if (isFixedForm)
        {
            EndOfLineRule c1 = new EndOfLineRule("c", colorComments);
            c1.setColumnConstraint(0);
            rules[i++] = c1;

            EndOfLineRule c2 = new EndOfLineRule("C", colorComments);
            c2.setColumnConstraint(0);
            rules[i++] = c2;

            IRule c3 = new FixedFormColumn72CommentRule();
            rules[i++] = c3;

            //salesRule = new FixedFormIdentifierWordRule(new FortranWordDetector(), colorIdentifiers);
            //wordRule = new FixedFormIdentifierWordRule(new FortranWordDetector(), colorIdentifiers);
            salesRule = new SalesScanKeywordRule(new FortranWordDetector(), colorIdentifiers);
            wordRule = new WordRule(new FortranWordDetector(), colorIdentifiers);
        }
        else
        {
            salesRule = new SalesScanKeywordRule(new FortranWordDetector(), colorIdentifiers);
            wordRule = new WordRule(new FortranWordDetector(), colorIdentifiers);
        }
        createSpecialWordRules(salesRule, wordRule);
        rules[i++] = salesRule;
        rules[i++] = wordRule;

        setRules(rules);
        
        // Punctuation, numbers, etc.
        setDefaultReturnToken(new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 0, 0)))));
    }

    private void createSpecialWordRules(WordRule salesRule, WordRule wordRule)
    {
        // If a word appears more than once (e.g., "real" or "len"), the LAST rule assigned to it will apply
        for (int i = 0; i < fgPreprocessor.length; i++)
        {
            salesRule.addWord(fgPreprocessor[i], colorKeywords);
        }
        for (int i = 0; i < fgTypes.length; i++)
        {
            salesRule.addWord(fgTypes[i].toLowerCase(), colorKeywords);
            salesRule.addWord(fgTypes[i], colorKeywords);
        }
        for (int i = 0; i < fgKeywords.length; i++)
        {
            salesRule.addWord(fgKeywords[i].toLowerCase(), colorKeywords);
            salesRule.addWord(fgKeywords[i], colorKeywords);
        }

        for (int i = 0; i < fgTextualOperators.length; i++)
        {
            wordRule.addWord(fgTextualOperators[i].toLowerCase(), colorKeywords);
            wordRule.addWord(fgTextualOperators[i], colorKeywords);
        }
        for (int i = 0; i < fgIntrinsics.length; i++)
        {
            wordRule.addWord(fgIntrinsics[i].toLowerCase(), colorIntrinsics);
            wordRule.addWord(fgIntrinsics[i], colorIntrinsics);
        }
    }
}
