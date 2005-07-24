package org.eclipse.cdt.core.addl_langs;

import org.eclipse.cdt.core.model.ICElement;

/**
 * Extensions of the ICElement provided by additional languages.
 * 
 * @author joverbey
 */
public interface IAdditionalLanguageElement extends ICElement {

	/**
	 * @return an <code>ImageDescriptor</code> for displaying alongside the
	 *         element in the Outline (or similar view)
	 *         
	 * The return type is <code>Object</code> for now so that we don't
	 * have JFace as a dependency, but this should probably be changed...
	 * 
	 * Note...
	 * 
	 * In the <code>ICElement</code> hierarchy, icons are determined by a
	 * giant <code>case</code> statement in
	 * <code>CElementImageProvider#getBaseImageDescriptor</code>. Since
	 * <code>case</code> statements based on types are bad (and error-prone)
	 * and we prefer to have everything in one place, we simply ask each
	 * <code>FortranElement</code> to tell us what its pictorial
	 * representation is. Yeah, if you want to argue that this is user interface
	 * stuff, it's in the wrong place... but for us, the chances are much higher
	 * that we will change the hierarchy than change the icons, so putting
	 * everything in one place is a better move.
	 * 
	 * To make this work, at the top of
	 * <code>CElementImageProvider#getBaseImageDescriptor</code>, I added
	 * these lines: <code>
	 * if (celement instanceof FortranElement)
	 *     return ((FortranElement)celement).getBaseImageDescriptor();
	 * </code>
	 */
	public abstract Object getBaseImageDescriptor();
}
