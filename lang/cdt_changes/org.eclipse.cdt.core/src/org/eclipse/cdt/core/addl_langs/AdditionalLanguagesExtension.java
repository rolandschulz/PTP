package org.eclipse.cdt.core.addl_langs;

import java.util.Iterator;

/**
 * Utility class for processing additional languages supplied through the
 * <code>AdditionalLanguages</code> extension point.
 * 
 * @author Jeff Overbey
 */
public final class AdditionalLanguagesExtension
// Java 1.5: add implements Iterable/*<IAdditionalLanguage>*/
{
	public static String EXTENSION_POINT_ID = "AdditionalLanguages";
	
	// Begin Singleton implementation
	private AdditionalLanguagesExtension() {}
	private static AdditionalLanguagesExtension instance = null;
	public static AdditionalLanguagesExtension getInstance()
	{
		if (instance == null)
			instance = new AdditionalLanguagesExtension();
		return instance;
	}
	// End Singleton implementation
	
	/**
	 * Allows you to iterate through all of the <code>IAdditionalLanguage</code>s
	 * contributed through the <code>AdditionalLanguages</code> extension point.
	 */
	public Iterator iterator() {
		return new AdditionalLanguagesIterator();
	}
	
	/**
	 * Iterates through all the languages (if any) added through the <code>AdditionalLanguages</code>
	 * extension point, calling the given callback to perform some action.
	 */
	public void processAdditionalLanguages(IAdditionalLanguageCallback callback)
	{
		for (Iterator it = this.iterator(); it.hasNext(); )
			callback.performAction((IAdditionalLanguage)it.next());
	}
	
	/**
	 * @param contentTypeID a string identifying a content type (registered with Eclipse via plugin.xml),
	 * 	   such as <code>XyzLanguagePlugIn.xyzSource</code> or <code>org.eclipse.cdt.core.fortranSource</code> 
	 * @return true iff some additional language matches the given content type ID
	 */
	public boolean someAdditionalLanguageMatchesSourceContentType(String contentTypeID)
	{
		for (Iterator it = this.iterator(); it.hasNext(); )
		{
			IAdditionalLanguage thisLanguage = (IAdditionalLanguage)it.next();
			if (thisLanguage.getRegisteredContentTypeIds().contains(contentTypeID))
				return true;
		}
		return false;
	}
	
	/**
	 * @param contentTypeID a string identifying a content type (registered with Eclipse via plugin.xml),
	 * 	   such as <code>XyzLanguagePlugIn.xyzSource</code> or <code>org.eclipse.cdt.core.fortranSource</code> 
	 * @return true iff some additional language matches the given content type ID
	 */
	public IAdditionalLanguage getLanguageForSourceContentType(String contentTypeID)
	{
		for (Iterator it = this.iterator(); it.hasNext(); )
		{
			IAdditionalLanguage thisLanguage = (IAdditionalLanguage)it.next();
			if (thisLanguage.getRegisteredContentTypeIds().contains(contentTypeID))
				return thisLanguage;
		}
		return null;
	}
}
