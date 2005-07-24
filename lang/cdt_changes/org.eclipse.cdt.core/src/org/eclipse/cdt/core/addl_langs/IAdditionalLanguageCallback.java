package org.eclipse.cdt.core.addl_langs;

/**
 * <code>IAdditionalLanguageCallback</code>s are passed as parameters to
 * <code>AdditionalLanguagesExtension.getInstance().processAdditionalLanguages(...)</code>.
 * They have only one method, <code>performAction</code>, which is called for each
 * additional language contributed through the <code>AdditionalLanguages</code> extension
 * point.
 * 
 * @author Jeff Overbey
 */
public interface IAdditionalLanguageCallback
{
	public void performAction(IAdditionalLanguage langClass);
}
