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
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.listeners.IPaintListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
public abstract class AbstractParallelElementView extends AbstractParallelView implements IPaintListener, IIconCanvasActionListener, IToolTipProvider, IImageProvider, IContentProvider {
	protected IManager manager = null;
	// Set
	protected IElementSet cur_element_set = null;
	protected int cur_set_size = 0;
	protected ElementIconCanvas canvas = null;
	// title
	protected final String EMPTY_TITLE = " ";
	protected Color registerColor = null;

	protected IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			final String preferenceType = event.getProperty();
			final Object value = event.getNewValue();
			if (value != null) {
				//getDisplay().asyncExec(new Runnable() {
				BusyIndicator.showWhile(getDisplay(), new Runnable() {
					public void run() {
						if (!canvas.isDisposed()) {
							if (preferenceType.startsWith("icon")) {
								IPreferenceStore store = PTPUIPlugin.getDefault().getPreferenceStore();
								canvas.setIconSpace(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_X), store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
								canvas.setIconSize(store.getInt(IPTPUIConstants.VIEW_ICON_WIDTH), store.getInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
							}
							else if (preferenceType.equals(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME)) {
								canvas.showTooltipAllthetime(new Boolean((String)value).booleanValue());
							}
							else if (preferenceType.equals(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT)) {
								canvas.setTooltipTimeout(new Long((String)value).longValue());
							}
							else if (preferenceType.equals(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP)) {
								canvas.setTooltipWrap(new Boolean((String)value).booleanValue());
							}
							canvas.resetCanvas();
						}
						updateView(null);
					}
				});
			}
		}
	};

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
		canvas.setContentProvider(this);
		canvas.setImageProvider(this);
		canvas.setToolTipProvider(this);
		canvas.addActionListener(this);
		PTPUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
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
		PTPUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
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
	protected abstract String[] getToolTipText(Object obj);
	protected abstract Object convertElementObject(IElement element);
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Paint Listener
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public void repaint(Object condition) {
		refresh(condition);
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * IContentProvider
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public Object getObject(int index) {
		if (canvas != null && manager != null) {
			return canvas.getElement(index);
		}
		return null;
	}
	public String getRulerIndex(Object obj, int index) {
		return String.valueOf(index);
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * IToolTipProvider
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public String[] toolTipText(Object obj) {
		if (obj instanceof IElement) {			
			return getToolTipText(convertElementObject((IElement)obj));
		}
		return new String[] { "" };
	}	
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Image Provider
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public Image getStatusIcon(Object obj, boolean isSelected) {
		if (cur_element_set != null && obj instanceof IElement) {
			int status = manager.getStatus((IElement)obj);
			return getImage(status, isSelected ? 1 : 0);
		}
		return null;
	}
	public void drawSpecial(Object obj, GC gc, int x_loc, int y_loc, int width, int height) {
		if (cur_element_set != null && obj instanceof IElement) {
			if (((IElement)obj).isRegistered()) {
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
				if (!canvas.isDisposed()) {
					canvas.setDisplayRuler(showRuler);
				}
				updateView(null);
			}
		});
	}
	public boolean isDisplayRuler() {
		if (!canvas.isDisposed()) {
			return canvas.isDisplayRuler();
		}
		return false;
	}
}
