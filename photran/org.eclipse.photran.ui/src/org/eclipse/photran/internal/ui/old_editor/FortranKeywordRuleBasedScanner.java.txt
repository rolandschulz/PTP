package org.eclipse.photran.internal.ui.old_editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.photran.internal.ui.preferences.ColorPreferencePage;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class FortranKeywordRuleBasedScanner extends RuleBasedScanner {
    private final class FortranWordDetector implements IWordDetector {
        public boolean isWordStart(char c) {
            return Character.isJavaIdentifierStart(c) || c == '.';
        }

        public boolean isWordPart(char c) {
            return Character.isJavaIdentifierPart(c) || c == '.';
        }
    }

	private static String[] fgKeywords = { "ACCESS", "ACTION", "ADVANCE",
			" ALLOCATABLE ", " ALLOCATE ", " ASSIGN ", " ASSIGNMENT ",
			" ASSOCIATE ", " ASYNCHRONOUS ", " BACKSPACE ", " BIND ",
			" BLANK ", " BLOCK ", " CALL ", " CASE ", " CLOSE ", " CLASS ",
			" COMMON ", " CONTAINS ", " CONTINUE ", " CYCLE ", " DATA ",
			" DEALLOCATE ", " DEFAULT ", " DELIM ", " DIMENSION ", " DIRECT ",
			" DO ", " DOUBLE ", " DOUBLEPRECISION ", " ELSE ", " ELSEWHERE ",
			" END ", " ENDDO ", " ENDFILE ", " ENDIF ", " ENTRY ", " EOR ",
			" EQUIVALENCE ", " ERR ", " EXIST ", " EXIT ", " EXTENDS ",
			" EXTENSIBLE ", " EXTERNAL ", " FILE ", " FMT ", " FLUSH ",
			" FORALL ", " FORM ", " FORMAT ", " FORMATTED ", " FUNCTION ",
			" GO ", " IF ", " IMPLICIT ", " IN ", " INOUT ", " INCLUDE ",
			" INQUIRE ", " INTENT ", " INTERFACE ", " INTRINSIC ",
			" IOLENGTH ", " IOSTAT ", " INSTRINSIC ", " KIND ", " LEN ",
			" MODULE ", " NAME ", " NAMED ", " NAMELIST ", " NEXTREC ",
			" NML ", " NONE ", " NON_OVERRIDABLE ", " NOPASS ", " NULLIFY ",
			" NUMBER ", " ONLY ", " OPEN ", " OPENED ", " OPERATOR ",
			" OPTIONAL ", " OUT ", " PAD ", " PARAMETER ", " PASS ", " PAUSE ",
			" POINTER ", " POSITION ", " PRECISION ", " PRINT ", " PRIVATE ",
			" PROCEDURE ", " PROGRAM ", " PROTECTED ", " PUBLIC ", " READ ",
			" READWRITE ", " REC ", " RECL ", " RECURSIVE ", " RESULT ",
			" RETURN ", " REWIND ", " SAVE ", " SELECT ", " SEQUENCE ",
			" SEQUENTIAL ", " SIZE ", " STAT ", " STATUS ", " STOP ",
			" SUBROUTINE ", " TARGET ", " THEN ", " TO ", " TYPE ",
			" UNFORMATTED ", " UNIT ", " USE ", " VOLATILE ", " WHERE ",
			" WHILE ", " WRITE " };
    
    private static String[] fgTextualOperators = { ".EQ.", ".EQV.", ".FALSE.", ".GE.", ".GT.", ".LE.", ".NE.", ".NEQV.", ".NOT.", ".OR.", ".TRUE." };

	private static String[] fgIntrinsics = { " ABS ", " ACHAR ", " ACOS ",
			" ADJUSTL ", " ADJUSTR ", " AIMAG ", " AINT ", " ALL ",
			" ALLOCATED ", " AND ", " ANINT ", " ANY ", " ASIN ",
			" ASSOCIATED ", " ATAN ", " ATAN2 ", " BIT_SIZE ", " BTEST ",
			" CEILING ", " CHAR ", " CMPLX ", " CONJG ", " COS ", " COSH ",
			" COUNT ", " CSHIFT ", " DATE_AND_TIME ", " DBLE ", " DIGITS ",
			" DIM ", " DOT_PRODUCT ", " DPROD ", " EOSHIFT ", " EPSILON ",
			" EQV ", " EXP ", " EXPONENT ", " FALSE ", " FLOOR ", " FRACTION ",
			" HUGE ", " IACHAR ", " IAND ", " IBCLR ", " IBITS ", " IBSET ",
			" ICHAR ", " IEOR ", " INDEX ", " INT ", " IOR ", " ISHFT ",
			" ISHFTC ", " KIND ", " LBOUND ", " LEN ", " LEN_TRIM ", " LGE ",
			" LGT ", " LLE ", " LLT ", " LOG ", " LOG10 ", " LOGICAL ",
			" MATMUL ", " MAX ", " MAXEXPONENT ", " MAXLOC ", " MAXVAL ",
			" MERGE ", " MIN ", " MINEXPONENT ", " MINLOC ", " MINVAL ",
			" MOD ", " MODULO ", " MVBITS ", " NEAREST ", " NEQV ", " NINT ",
			" NOT ", " OR ", " PACK ", " PRECISION ", " PRESENT ", " PRODUCT ",
			" RADIX ", " RANDOM_NUMBER ", " RANDOM_SEED ", " RANGE ", " REAL ",
			" REPEAT ", " RESHAPE ", " RRSPACING ", " SCALE ", " SCAN ",
			" SELECTED_INT_KIND ", " SELECTED_REAL_KIND ", " SET_EXPONENT ",
			" SHAPE ", " SIGN ", " SIN ", " SINH ", " SIZE ", " SPACING ",
			" SPREAD ", " SQRT ", " SUM ", " SYSTEM_CLOCK ", " TAN ", " TANH ",
			" TINY ", " TRANSFER ", " TRANSPOSE ", " TRIM ", " TRUE ",
			" UBOUND ", " UNPACK ", " VERIFY " };

	private static String[] fgTypes = { "REAL", "INTEGER", "CHARACTER",
			"LOGICAL", "COMPLEX" };

	private static String[] fgPreprocessor = { "INCLUDE", "#include", "#error",
			"#warning", "#pragma", "#ifdef", "#ifndef", "#if", "#else",
			"#elif", "#endif", "#line" };

	private static IPreferenceStore store = FortranUIPlugin.getDefault()
			.getPreferenceStore();

	Token F90_STRING_CONSTANTS_COLOR = new Token(new TextAttribute(
			new Color(null, PreferenceConverter.getColor(store,
					ColorPreferencePage.F90_STRING_CONSTANTS_COLOR_PREF))));

	Token F90_COMMENT_COLOR = new Token(new TextAttribute(new Color(
			null, PreferenceConverter.getColor(store,
					ColorPreferencePage.F90_COMMENT_COLOR_PREF))));

    Token F90_IDENTIFIER_COLOR = new Token(new TextAttribute(new Color(
            null, PreferenceConverter.getColor(store,
                    ColorPreferencePage.F90_IDENTIFIER_COLOR_PREF))));

    Token F90_INTRINSIC_COLOR = new Token(new TextAttribute(new Color(
            null, PreferenceConverter.getColor(store,
                    ColorPreferencePage.F90_INTRINSIC_COLOR_PREF)), null, SWT.ITALIC));

	Token F90_KEYWORD_COLOR = new Token(new TextAttribute(new Color(
			null, PreferenceConverter.getColor(store,
					ColorPreferencePage.F90_KEYWORD_COLOR_PREF)), null,
			SWT.BOLD));

	IPropertyChangeListener colorPreferenceListener = new IPropertyChangeListener() {
		/*
		 * @see IPropertyChangeListener.propertyChange()
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (ColorPreferencePage.respondToPreferenceChange(event)) {
				System.out.println(event.getProperty());
				updateColorPreferences();
				updateColorPreferences();
			}
		}
	};

	public FortranKeywordRuleBasedScanner(boolean isFixedForm) {
		FortranUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(colorPreferenceListener);

        IRule[] rules = new IRule[isFixedForm ? 7 : 4];
        int i = 0;

        rules[i++] = new MultiLineRule("\"", "\"", F90_STRING_CONSTANTS_COLOR);
        rules[i++] = new MultiLineRule("'", "'", F90_STRING_CONSTANTS_COLOR);

        rules[i++] = new EndOfLineRule("!", F90_COMMENT_COLOR);
        if (isFixedForm)
        {
            EndOfLineRule c1 = new EndOfLineRule("c", F90_COMMENT_COLOR);
            c1.setColumnConstraint(0);
            rules[i++] = c1;
            
            EndOfLineRule c2 = new EndOfLineRule("C", F90_COMMENT_COLOR);
            c2.setColumnConstraint(0);
            rules[i++] = c2;
            
            IRule c3 = new IRule()
            {
                public IToken evaluate(ICharacterScanner scanner)
                {
                    IToken result = Token.UNDEFINED;
                    scanner.read();
                    if (scanner.getColumn() > 72)
                    {
                        result = F90_COMMENT_COLOR;
                        do scanner.read(); while (scanner.getColumn() > 72);
                    }
                    scanner.unread();
                    return result;
                }
            };
            rules[i++] = c3;
        }
                
        WordRule rule = isFixedForm
            ? new WordRule(new FortranWordDetector(), F90_IDENTIFIER_COLOR)
              {
                 private StringBuffer fBuffer= new StringBuffer();

                 public IToken evaluate(ICharacterScanner scanner)
                 {
//                    int c= scanner.read();
//                    boolean canStart = fDetector.isWordStart((char)c) && scanner.getColumn() >= 7;
//                    scanner.unread();
//                    return canStart ? super.evaluate(scanner) : Token.UNDEFINED;

                    int c= scanner.read();
                    if (fDetector.isWordStart((char) c) && scanner.getColumn() > 6) {
                        fBuffer.setLength(0);
                        do {
                            fBuffer.append((char) c);
                            c= scanner.read();
                        } while (c != ICharacterScanner.EOF
                                 && fDetector.isWordPart((char) c)
                                 && scanner.getColumn() <= 72);
                        scanner.unread();

                        IToken token= (IToken) fWords.get(fBuffer.toString());
                        if (token != null)
                            return token;

                        if (fDefaultToken.isUndefined())
                            unreadBuffer(scanner);

                        return fDefaultToken;
                    }

                    scanner.unread();
                    return Token.UNDEFINED;
                 }
              }
            : new WordRule(new FortranWordDetector(), F90_IDENTIFIER_COLOR);
        createSpecialWordRules(rule);
        rules[i++] = rule;
        
        setRules(rules);
        setDefaultReturnToken(new Token(new TextAttribute(new Color(Display.getCurrent(), new RGB(0, 0, 0))))); // Punctuation, numbers, etc.
	}

	protected void updateColorPreferences() {
		F90_KEYWORD_COLOR.setData(new TextAttribute(new Color(null, PreferenceConverter.getColor(store,
				ColorPreferencePage.F90_KEYWORD_COLOR_PREF)
		), null,SWT.BOLD));
		F90_STRING_CONSTANTS_COLOR.setData(new TextAttribute(new Color(null,
				PreferenceConverter.getColor(store,
						ColorPreferencePage.F90_STRING_CONSTANTS_COLOR_PREF))));
        F90_IDENTIFIER_COLOR.setData(new TextAttribute(new Color(null,
                                                                PreferenceConverter.getColor(store,
                                                                        ColorPreferencePage.F90_IDENTIFIER_COLOR_PREF))));
        F90_INTRINSIC_COLOR.setData(new TextAttribute(new Color(null,
                                                                PreferenceConverter.getColor(store,
                                                                        ColorPreferencePage.F90_INTRINSIC_COLOR_PREF))));
		F90_COMMENT_COLOR.setData(new TextAttribute(new Color(null,
				PreferenceConverter.getColor(store,
						ColorPreferencePage.F90_COMMENT_COLOR_PREF))));
	}

    private void createSpecialWordRules(WordRule rule) {
        // If a word appears more than once (e.g., "real" or "len"), the LAST rule assigned to it will apply
        for (int i = 0; i < fgTextualOperators.length; i++) {
            rule.addWord(fgTextualOperators[i].toLowerCase().trim(), F90_KEYWORD_COLOR);
            rule.addWord(fgTextualOperators[i].trim(), F90_KEYWORD_COLOR);
        }
        for (int i = 0; i < fgPreprocessor.length; i++) {
            rule.addWord(fgPreprocessor[i].trim(), F90_KEYWORD_COLOR);
        }
        for (int i = 0; i < fgIntrinsics.length; i++) {
            rule.addWord(fgIntrinsics[i].toLowerCase().trim(), F90_INTRINSIC_COLOR);
            rule.addWord(fgIntrinsics[i].trim(), F90_INTRINSIC_COLOR);
        }
        for (int i = 0; i < fgTypes.length; i++) {
            rule.addWord(fgTypes[i].toLowerCase().trim(), F90_KEYWORD_COLOR);
            rule.addWord(fgTypes[i].trim(), F90_KEYWORD_COLOR);
        }
        for (int i = 0; i < fgKeywords.length; i++) {
            rule.addWord(fgKeywords[i].toLowerCase().trim(), F90_KEYWORD_COLOR);
            rule.addWord(fgKeywords[i].trim(), F90_KEYWORD_COLOR);
        }
  }
}