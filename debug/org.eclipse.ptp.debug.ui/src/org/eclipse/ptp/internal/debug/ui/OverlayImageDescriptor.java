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
package org.eclipse.ptp.internal.debug.ui;

import java.util.Arrays;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * @author Clement chu
 * 
 */
public class OverlayImageDescriptor extends CompositeImageDescriptor {
	private static final int DEFAULT_WIDTH = 16;
	private static final int DEFAULT_HEIGHT = 16;
	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;
	// the base image
	private Image base;
	// the overlay images
	private ImageDescriptor[] overlays;
	// the size
	private Point size;

	/** Constructor
	 * @param base
	 * @param overlays
	 */
	public OverlayImageDescriptor(Image base, ImageDescriptor[] overlays) {
		this(base, overlays, new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT));
	}
	/** Constructor
	 * @param base
	 * @param overlays
	 * @param size
	 */
	public OverlayImageDescriptor(Image base, ImageDescriptor[] overlays, Point size) {
		setBase(base);
		setOverlays(overlays);
		setSize(size);
	}
	/** Draw overlays
	 * @param overlays
	 */
	protected void drawOverlays(ImageDescriptor[] overlays) {
		Point size = getSize();
		for (int i = 0; i < overlays.length; i++) {
			ImageDescriptor overlay = overlays[i];
			if (overlay == null)
				continue;
			ImageData overlayData = overlay.getImageData();
			// Use the missing descriptor if it is not there.
			if (overlayData == null)
				overlayData = ImageDescriptor.getMissingImageDescriptor().getImageData();
			switch (i) {
			case TOP_LEFT:
				drawImage(overlayData, 0, 0);
				break;
			case TOP_RIGHT:
				drawImage(overlayData, size.x - overlayData.width, 0);
				break;
			case BOTTOM_LEFT:
				drawImage(overlayData, 0, size.y - overlayData.height);
				break;
			case BOTTOM_RIGHT:
				drawImage(overlayData, size.x - overlayData.width, size.y - overlayData.height);
				break;
			}
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof OverlayImageDescriptor))
			return false;
		OverlayImageDescriptor other = (OverlayImageDescriptor) o;
		return getBase().equals(other.getBase()) && Arrays.equals(getOverlays(), other.getOverlays());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int code = getBase().hashCode();
		for (int i = 0; i < getOverlays().length; i++) {
			if (getOverlays()[i] != null)
				code ^= getOverlays()[i].hashCode();
		}
		return code;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	protected void drawCompositeImage(int width, int height) {
		drawImage(getBase().getImageData(), 0, 0);
		drawOverlays(getOverlays());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	protected Point getSize() {
		return size;
	}
	/** Get base image
	 * @return
	 */
	private Image getBase() {
		return base;
	}
	/** Set base image
	 * @param base
	 */
	private void setBase(Image base) {
		this.base = base;
	}
	/** Get overlays images
	 * @return
	 */
	private ImageDescriptor[] getOverlays() {
		return this.overlays;
	}
	/** Set overlays images
	 * @param overlays
	 */
	private void setOverlays(ImageDescriptor[] overlays) {
		this.overlays = overlays;
	}
	/** Set size
	 * @param size
	 */
	private void setSize(Point size) {
		this.size = size;
	}
}
