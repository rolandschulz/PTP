package org.eclipse.cdt.core.addl_langs;

import java.util.Collection;
import java.util.Map;

/**
 * The <code>org.eclipse.cdt.core.AdditionalLanguages</code> extension point requires
 * that each additional language supply a class which implements this interface.
 * 
 * @author Jeff Overbey
 */
public interface IAdditionalLanguage
{
	/**
	 * @return the name of the language, as a free-form string
	 */
	public String getName();

	/**
	 * @return a <code>Collection</code> of the content types supported by the
	 *         language, as <code>String</code>s.  Content types are specified
	 *         in plugin.xml by a <code>content-type</code> tag (for example,
	 *         <code>org.eclipse.cdt.core.cSource</code>) and are mapped
	 *         to file extensions via a <code>file-association</code> tag.
	 */
	public Collection/*<String>*/ getRegisteredContentTypeIds();
	
	/**
	 * @param  tu  the <code>TranslationUnit</code> to be parsed
	 * @param  newElements  a Map taking <code>ICElement</code>s to
	 * 		   <code>CElementInfo</code>s, which contains all of the elements
	 * 		   that should appear in the Outline
	 * @return a model builder, which parses the given translation unit and returns
	 *         the elements of its model
	 */
	public IModelBuilder createModelBuilder(org.eclipse.cdt.internal.core.model.TranslationUnit tu, Map newElements);
}
