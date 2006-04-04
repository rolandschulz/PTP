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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.internal.ui.views.IconCanvas;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Clement chu
 * 
 */
public class ElementIconCanvas extends IconCanvas {
	private IElementSet cur_element_set = null;

	public ElementIconCanvas(AbstractParallelElementView view, Composite parent, int style) {
		super(parent, style);
		IPreferenceStore store = PTPUIPlugin.getDefault().getPreferenceStore();
		setIconSpace(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_X), store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
		setIconSize(store.getInt(IPTPUIConstants.VIEW_ICON_WIDTH), store.getInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
		setTooltip(store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME), store.getLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT), store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP));
	}
	public void dispose() {
		super.dispose();
	}
	public void setElementSet(IElementSet e_set) {
		this.cur_element_set = e_set;
		getDisplay().syncExec(new Runnable() {
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
