package org.eclipse.cldt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


/**
 * Manages SWT color objects for given color keys and
 * given <code>RGB</code> objects. Until the <code>dispose</code> 
 * method is called, the same color object is returned for
 * equal keys and equal <code>RGB</code> values.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IJavaColorConstants
 */
public interface IColorManager {
	
	/**
	 * Returns a color object for the given key. The color objects 
	 * are remembered internally; the same color object is returned 
	 * for equal keys.
	 *
	 * @param key the color key
	 * @return the color object for the given key
	 */
	Color getColor(String key);
	
	/**
	 * Returns the color object for the value represented by the given
	 * <code>RGB</code> object.
	 *
	 * @param rgb the rgb color specification
	 * @return the color object for the given rgb value
	 */
	Color getColor(RGB rgb);	
	
	/**
	 * Disposes all color objects remembered by this color manager.
	 */
	void dispose();
}