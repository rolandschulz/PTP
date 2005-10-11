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

import java.util.Iterator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.listeners.IPaintListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author clement chu
 * 
 */
public abstract class AbstractParallelElementView extends AbstractParallelView implements IPaintListener {
	protected IManager manager = null;
	// Set
	protected IElementSet cur_element_set = null;
	protected int cur_set_size = 0;
	protected ElementIconCanvas canvas = null;
	// title
	protected final String EMPTY_TITLE = " ";

	public void createPartControl(Composite parent) {
		createView(parent);
		setContentDescription(EMPTY_TITLE);
		manager.addPaintListener(this);
	}
	protected void createView(Composite parent) {
		createElementView(parent);
	}
	public IManager getUIManager() {
		return manager;
	}
	public IElementHandler getCurrentElementHandler() {
		return manager.getElementHandler(getCurrentID());
	}
	protected void changeTitle(String title, String setName, int size) {
		changeTitle(" " + title + " - " + setName + " [" + size + "]");
	}
	protected void changeTitle(final String message) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				setContentDescription(message);
			}
		});
	}
	protected Composite createElementView(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		canvas = new ElementIconCanvas(this, composite, SWT.NONE);
		return composite;
	}
	public void setSelection(ISelection selection) {
		if (selection instanceof StructuredSelection) {
			for (Iterator i = ((StructuredSelection) selection).iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof IElement) {
					canvas.selectElement(((IElement) obj).getIDNum());
				}
			}
		}
	}
	public void dispose() {
		manager.removePaintListener(this);
		super.dispose();
	}
	public void setFocus() {
		canvas.setFocus();
	}
	public ISelection getSelection() {
		if (cur_element_set == null)
			return new StructuredSelection();
		return new StructuredSelection(canvas.getSelectedElements());
	}
	public String getCurrentSetID() {
		return manager.getCurrentSetId();
	}
	public void fireChangeEvent(IElementSet cur_set, IElementSet pre_set) {
		manager.fireEvent(IManager.CHANGE_SET_TYPE, null, cur_set, pre_set);
	}
	public void selectSet(IElementSet set) {
		fireChangeEvent(set, cur_element_set);
		cur_element_set = set;
		cur_set_size = 0;
		if (cur_element_set != null) {
			cur_set_size = cur_element_set.size();
			manager.setCurrentSetId(cur_element_set.getID());
		}
		canvas.setElementSet(cur_element_set);
	}
	public void doRemoveElements(IElement[] elements) {
		if (!manager.getCurrentSetId().equals(IElementHandler.SET_ROOT_ID)) {
			removeElements(canvas.getSelectedElements());
		}
	}
	public IElementSet getCurrentSet() {
		return cur_element_set;
	}
	public void refresh() {
		refresh(null);
	}
	public void refresh(final Object condition) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateView(condition);
				if (!canvas.isDisposed())
					canvas.redraw();
			}
		});
	}
	public void doDoubleClickAction(IElement element) {
		if (cur_element_set != null && element != null) {
			doubleClick(element);
		}
	}
	// Set element info
	protected abstract void initialView();
	// Set element to display
	protected abstract void initialElement();
	// set update view details
	public abstract void update();
	public abstract void updateTitle();
	public abstract String getCurrentID();
	public abstract String getToolTipText(int index);
	public abstract Image getImage(int index1, int index2);
	protected abstract void doubleClick(IElement element);
	protected abstract void removeElements(IElement[] elements);
	protected abstract void updateView(Object condition);
	/**
	 * Paint Listener
	 */
	public void repaint(Object condition) {
		refresh(condition);
	}
}
