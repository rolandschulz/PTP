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
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.listeners.IPaintListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.preferences.IPreferencesListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author clement chu
 * 
 */
public abstract class AbstractParallelElementView extends AbstractParallelView implements IPaintListener, IIconCanvasActionListener, IToolTipProvider, IImageProvider, IPreferencesListener {
	protected IManager manager = null;
	// Set
	protected IElementSet cur_element_set = null;
	protected int cur_set_size = 0;
	protected ElementIconCanvas canvas = null;
	// title
	protected final String EMPTY_TITLE = " ";
	protected Color registerColor = null;

	public void createPartControl(Composite parent) {
		createView(parent);
		setContentDescription(EMPTY_TITLE);
		manager.addPaintListener(this);
		registerColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	}
	public void setRegisterColor(Color color) {
		this.registerColor = color;
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
		canvas.setImageProvider(this);
		canvas.setToolTipProvider(this);
		canvas.addActionListener(this);
		canvas.setDisplayRuler(PTPUIPlugin.getDefault().getPluginPreferences().getBoolean(IPTPUIConstants.SHOW_RULER));
		PTPUIPlugin.getDefault().addPreferenceListener(this);
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
		canvas.removeActionListener(this);
		PTPUIPlugin.getDefault().removePreferenceListener(this);
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
				if (!canvas.isDisposed()) {
					canvas.redraw();
				}
			}
		});
	}
	// Set element info
	protected abstract void initialView();
	// Set element to display
	protected abstract void initialElement();
	// set update view details
	public abstract void update();
	public abstract void updateTitle();
	public abstract String getCurrentID();
	protected abstract void doubleClick(IElement element);
	protected abstract void updateView(Object condition);
	protected abstract Image getImage(int index1, int index2);
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Paint Listener
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public void repaint(Object condition) {
		refresh(condition);
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Image Provider
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public Image getStatusIcon(int index, boolean isSelected) {
		if (cur_element_set == null)
			return null;
		int status = manager.getStatus(canvas.getElement(index).getID());
		return getImage(status, isSelected ? 1 : 0);
	}
	public void drawSpecial(int index, GC gc, int x_loc, int y_loc, int width, int height) {
		if (cur_element_set != null) {
			IElement element = canvas.getElement(index);
			if (element.isRegistered()) {
				gc.setForeground(registerColor);
				gc.drawRectangle(x_loc, y_loc, width, height);
				gc.setForeground(canvas.getForeground());
			}
		}
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * IIconCanvasActionListener
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public void handleAction(int type, int index) {
		if (type == IIconCanvasActionListener.DOUBLE_CLICK_ACTION) {
			doubleClick(canvas.getElement(index));
		}
	}
	public void setDisplayRuler(final boolean showRuler) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateView(null);
				if (!canvas.isDisposed()) {
					canvas.setDisplayRuler(showRuler);
				}
			}
		});
	}
	public boolean isDisplayRuler() {
		if (!canvas.isDisposed()) {
			return canvas.isDisplayRuler();
		}
		return false;
	}
	public void preferenceUpdated() {
		//setDisplayRuler(PTPUIPlugin.getDefault().getPluginPreferences().getBoolean(IPTPUIConstants.SHOW_RULER));
	}
}
