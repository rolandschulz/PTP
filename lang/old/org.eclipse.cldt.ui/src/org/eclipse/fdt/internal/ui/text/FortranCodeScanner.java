package org.eclipse.fdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.fdt.core.parser.FortranKeywordSetKey;
import org.eclipse.fdt.internal.core.parser.token.FortranKeywordSets;
import org.eclipse.fdt.internal.ui.text.IFortranColorConstants;
import org.eclipse.fdt.internal.ui.text.util.FortranWordDetector;
import org.eclipse.fdt.internal.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;


/**
 * A C code scanner.
 */
public final class FortranCodeScanner extends AbstractFortranScanner {
	
	/** Constants which are additionally colored. */
    private static String[] fgConstants= { 
		".EQ.", ".NE.", ".LT.",  		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
		".GT.", ".GE.", ".LG.",		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		".NOT.", ".N.", ".AND.",		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		".A.", ".OR.", ".O.",			//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		".EQV.", ".NEQV.", ".XOR.",	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		".TRUE.", ".FALSE."			//$NON-NLS-1$ //$NON-NLS-2$
 	};
	
    /** Properties for tokens. */
	private static String[] fgTokenProperties= {
		IFortranColorConstants.FORTRAN_KEYWORD,
		IFortranColorConstants.FORTRAN_TYPE,
		IFortranColorConstants.FORTRAN_STRING,
		IFortranColorConstants.FORTRAN_OPERATOR,
		IFortranColorConstants.FORTRAN_BRACES,
		IFortranColorConstants.FORTRAN_NUMBER,
		IFortranColorConstants.FORTRAN_DEFAULT,
	};
	
	/**
	 * Creates a C code scanner.
     * @param manager Color manager.
     * @param store Preference store.
	 */
	public FortranCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}
	
	/**
	 * @see AbstractFortranScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/**
	 * @see AbstractFortranScanner#createRules()
	 */
	protected List createRules() {
				
		List rules= new ArrayList();		
		
		// Add rule for strings
		Token token= getToken(IFortranColorConstants.FORTRAN_STRING);
		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new SingleLineRule("\"", "\"", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
				
		// Add generic white space rule.
		//rules.add(new WhitespaceRule(new CWhitespaceDetector()));

		// Add word rule for keywords, types, and constants.
		token= getToken(IFortranColorConstants.FORTRAN_DEFAULT);
		IgnoreCaseWordRule wordRule= new IgnoreCaseWordRule(new FortranWordDetector(), token);
		
		token= getToken(IFortranColorConstants.FORTRAN_KEYWORD);
		Iterator i = FortranKeywordSets.getKeywords( FortranKeywordSetKey.KEYWORDS ).iterator();
		while( i.hasNext() )
			wordRule.addWord((String) i.next(), token);
 
		for (int j=0; j<fgConstants.length; j++)
			wordRule.addWord(fgConstants[j], token);

		token= getToken(IFortranColorConstants.FORTRAN_TYPE);
		i = FortranKeywordSets.getKeywords( FortranKeywordSetKey.TYPES ).iterator();
		while( i.hasNext() )
			wordRule.addWord((String) i.next(), token);
		

        rules.add(wordRule);

        token = getToken(IFortranColorConstants.FORTRAN_NUMBER);
        NumberRule numberRule = new NumberRule(token);
        rules.add(numberRule);
        
        token = getToken(IFortranColorConstants.FORTRAN_OPERATOR);
        FortranOperatorRule opRule = new FortranOperatorRule(token);
        rules.add(opRule);

        token = getToken(IFortranColorConstants.FORTRAN_BRACES);
        FortranBraceRule braceRule = new FortranBraceRule(token);
        rules.add(braceRule);
        
        /*token = getToken(IFortranColorConstants.FORTRAN_TYPE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new FortranWordDetector(), token);
		
		i = ParserFactory.getKeywordSet( KeywordSetKey.PP_DIRECTIVE, ParserLanguage.C ).iterator();
		while( i.hasNext() )
			preprocessorRule.addWord((String) i.next(), token);
		
		rules.add(preprocessorRule);
		*/
		setDefaultReturnToken(getToken(IFortranColorConstants.FORTRAN_DEFAULT));
		return rules;
	}

	/**
     * @see org.eclipse.jface.text.rules.RuleBasedScanner#setRules(org.eclipse.jface.text.rules.IRule[])
	 */
    public void setRules(IRule[] rules) {
		super.setRules(rules);	
	}

	/**
	 * @see AbstractFortranScanner#affectsBehavior(PropertyChangeEvent)
	 */	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return super.affectsBehavior(event);
	}

	/**
	 * @see AbstractFortranScanner#adaptToPreferenceChange(PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
			
		if (super.affectsBehavior(event)) {
			super.adaptToPreferenceChange(event);
		}
	}
}