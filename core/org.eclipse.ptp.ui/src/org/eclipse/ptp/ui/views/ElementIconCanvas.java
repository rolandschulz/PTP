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

import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Clement chu
 *
 */
public abstract class ElementIconCanvas extends IconCanvas {
	// Set
	protected IManager manager = null;
	protected IElementSet cur_element_set = null;
	protected int cur_set_size = 0;
	protected int fisrt_selected_element_id = -1;
	
	public ElementIconCanvas(Composite parent, int style) {
		super(parent, style);
	}
	
	protected void setElementSet(IElementSet e_set) {
		this.cur_element_set = e_set;
		setTotal(cur_element_set.size());
	}
	protected IElementSet getCurrentElementSet() {
		return cur_element_set;
	}
	protected IElement getElement(int index) {
		return cur_element_set.getSortedElements()[index];
	}
	protected String getToolTipText(int index) {
		return getToolTipText(getElement(index));
	}	
	protected void drawRectangle(int index, GC gc, int x_loc, int y_loc, int width, int height) {
		IElement element = getElement(index);
		if (element.isRegistered())
			gc.drawRectangle(x_loc, y_loc, width, height);
	}
	protected Image getStatusIcon(int index) {
		return getStatusIcon(getElement(index));
	}
	protected abstract Image getStatusIcon(IElement element);
	protected abstract String getToolTipText(IElement element);
}
