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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Clement chu
 * 
 */
public class ElementIconCanvas extends AbstractIconCanvas {
	// Set
	private IElementSet cur_element_set = null;
	private AbstractParallelElementView view = null;
	private Color registerColor = null;

	public ElementIconCanvas(AbstractParallelElementView view, Composite parent, int style) {
		super(parent, style);
		this.view = view;
		registerColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	}
	public void dispose() {
		super.dispose();
		cur_element_set = null;
	}
	public void setRegisterColor(Color color) {
		registerColor = color;
	}
	public void setElementSet(IElementSet e_set) {
		this.cur_element_set = e_set;
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				setTotal(cur_element_set == null ? 0 : cur_element_set.size());
			}
		});
	}
	public IElementSet getCurrentElementSet() {
		return cur_element_set;
	}
	public IElement getElement(int index) {
		if (cur_element_set == null)
			return null;
		return cur_element_set.getSortedElements()[index];
	}
	public String getToolTipText(int index) {
		return view.getToolTipText(index);
	}
	public void drawRectangle(int index, GC gc, int x_loc, int y_loc, int width, int height) {
		if (cur_element_set != null) {
			IElement element = getElement(index);
			if (element.isRegistered()) {
				gc.setForeground(registerColor);
				gc.drawRectangle(x_loc, y_loc, width, height);
				gc.setForeground(getForeground());
			}
		}
	}
	public Image getStatusIcon(int index, boolean isSelected) {
		if (cur_element_set == null)
			return null;
		int status = view.getUIManager().getStatus(getElement(index).getID());
		return view.getImage(status, isSelected ? 1 : 0);
	}
	public void deleteElements(int[] indexes) {
		view.doRemoveElements(getElements(indexes));
	}
	public void doubleClickAction(int index) {
		view.doDoubleClickAction(getElement(index));
	}
	public IElement[] getElements(int[] indexes) {
		if (cur_element_set == null)
			return new IElement[0];
		List selectedElements = new ArrayList();
		for (int i = 0; i < indexes.length; i++) {
			selectedElements.add(cur_element_set.get(indexes[i]));
		}
		return (IElement[]) selectedElements.toArray(new IElement[selectedElements.size()]);
	}
	public IElement[] getSelectedElements() {
		return getElements(getSelectedIndexes());
	}
}
