/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.views;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * @author Clement chu
 * 
 */
public interface IImageProvider {
	/**
	 * Get icon status
	 * 
	 * @param index
	 *            Tagret index position
	 * @param isSelected
	 *            whether it is selected or not
	 * @return Image
	 * @since 7.0
	 */
	public Image getStatusIcon(int index, boolean isSelected);

	/**
	 * Draw special on the image
	 * 
	 * @param index
	 *            Tagret index position
	 * @param gc
	 *            GC
	 * @param x_loc
	 *            x coordinate
	 * @param y_loc
	 *            y corrdinate
	 * @param width
	 *            image size
	 * @param height
	 *            image height
	 * @since 7.0
	 */
	public void drawSpecial(int index, GC gc, int x_loc, int y_loc, int width, int height);
}
