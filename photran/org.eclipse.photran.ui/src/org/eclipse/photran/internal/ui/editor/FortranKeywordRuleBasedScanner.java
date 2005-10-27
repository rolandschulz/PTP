package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
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

public class FortranKeywordRuleBasedScanner extends RuleBasedScanner {
	private final class FortranWordDetector implements IWordDetector {
		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}

		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
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

	public FortranKeywordRuleBasedScanner() {
		FortranUIPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(colorPreferenceListener);

		IRule[] rules = new IRule[3];
		// Add rule for processing instructions
		rules[0] = new MultiLineRule("\"", "\"", F90_STRING_CONSTANTS_COLOR);
		// Add generic whitespace rule.
		rules[1] = new EndOfLineRule("!", F90_COMMENT_COLOR);
		WordRule rule = new WordRule(new FortranWordDetector(),
				F90_IDENTIFIER_COLOR);

		createWordRules(rule);

		rules[2] = rule;
		setRules(rules);
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
		F90_COMMENT_COLOR.setData(new TextAttribute(new Color(null,
				PreferenceConverter.getColor(store,
						ColorPreferencePage.F90_COMMENT_COLOR_PREF))));
	}

	private void createWordRules(WordRule rule) {
		for (int i = 0; i < fgKeywords.length; i++) {
			rule.addWord(fgKeywords[i].toLowerCase().trim(), F90_KEYWORD_COLOR);
			rule.addWord(fgKeywords[i].trim(), F90_KEYWORD_COLOR);
		}
		for (int i = 0; i < fgIntrinsics.length; i++) {
			rule.addWord(fgIntrinsics[i].toLowerCase().trim(),
					F90_KEYWORD_COLOR);
			rule.addWord(fgIntrinsics[i].trim(), F90_KEYWORD_COLOR);
		}
		for (int i = 0; i < fgTypes.length; i++) {
			rule.addWord(fgTypes[i].toLowerCase().trim(), F90_KEYWORD_COLOR);
			rule.addWord(fgTypes[i].trim(), F90_KEYWORD_COLOR);
		}
		for (int i = 0; i < fgPreprocessor.length; i++)
			rule.addWord(fgPreprocessor[i].trim(), F90_KEYWORD_COLOR);
	}
}