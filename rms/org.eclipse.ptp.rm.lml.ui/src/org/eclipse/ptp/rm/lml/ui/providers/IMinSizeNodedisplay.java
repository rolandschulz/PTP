/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.swt.graphics.Point;

/**
 * Nodedisplays implement this interface, if they are able
 * to manage minimum defined rectangle sizes. Nodedisplays
 * can paint inner rectangles so small, that they always fit
 * into the given space. Or they define a minimum size for the
 * rectangles. If a rectangle's size would go below this minimum
 * size the nodedisplay could provide scrollbars, use the
 * given space, but show not all information, in other words
 * all lower elements at the same time.
 * 
 * 
 */
public interface IMinSizeNodedisplay {

	/**
	 * Calculate minimal size allowed for this nodedisplay.
	 * The surrounding composite should make sure, that
	 * this nodedisplay has at least this space given
	 * as its painting area.
	 * 
	 * @return minimal size, in which this nodedisplay can be painted well, x=width, y=height
	 */
	public Point getMinimalSize();

	/**
	 * This method is called from components, which use nodedisplays.
	 * It forces the nodedisplay to paint the lowest-level rectangles
	 * at least as high as passed through this method.
	 * 
	 * @param minHeight
	 *            minimum height of lowest level rectangles in pixels
	 */
	public void setMinimumRectangleHeight(int minHeight);

	/**
	 * This method is called from components, which use nodedisplays.
	 * It forces the nodedisplay to paint the lowest-level rectangles
	 * at least as wide as passed through this method.
	 * 
	 * @param minWidth
	 *            minimum width of lowest level rectangles in pixels
	 */
	public void setMinimumRectangleWidth(int minWidth);
}
