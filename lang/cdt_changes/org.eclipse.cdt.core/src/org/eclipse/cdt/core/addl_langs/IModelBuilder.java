package org.eclipse.cdt.core.addl_langs;

import java.util.Map;

/**
 * Interface supported by model builders for all languages. Model builders parse
 * a <code>TranslationUnit</code> (i.e., a file) and return a hierarchy of
 * <code>ICElement</code>s which represent the high-level structure of that
 * file (what modules, classes, functions, and similar constructs are contained
 * in it, and on what line(s) the definition occurs).
 * 
 * The translation unit to parse and the initial element map are given to
 * <code>IAdditionalLanguage#createModelBuilder</code>, which will presumably
 * pass that information on to the model builder constructor.
 * 
 * @author Jeff Overbey
 */
public interface IModelBuilder {

	/**
	 * Callback used when a <code>TranslationUnit</code> needs to have its
	 * model builder parse it.
	 * 
	 * The translation unit to parse and the initial element map are given to
	 * <code>IAdditionalLanguage#createModelBuilder</code>, which will
	 * presumably pass that information on to the model builder constructor.
	 * 
	 * @param quickParseMode
	 * @return a Map taking <code>ICElement</code>s to
	 *         <code>CElementInfo</code>s, which contains all of the elements
	 *         that should appear in the Outline for the translation unit
	 *         associated with this model builder.
	 */
	public abstract Map parse(boolean quickParseMode) throws Exception;

}