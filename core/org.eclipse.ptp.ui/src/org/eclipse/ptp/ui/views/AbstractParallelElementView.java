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

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
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
 *
 * @author clement chu
 * 
 */
public abstract class AbstractParallelElementView extends AbstractParallelView implements IIconCanvasActionListener, IToolTipProvider, IImageProvider, IContentProvider, ISelectionChangedListener {
	protected IManager manager = null;
	// Set
	protected IElementSet cur_element_set = null;
	protected int cur_set_size = 0;
	protected ElementIconCanvas canvas = null;
	// title
	protected final String EMPTY_TITLE = " ";
	protected Color registerColor = null;
	
	/**
	 * update preference setting 
	 */
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
						refresh(false);
					}
				});
			}
		}
	};
	
	public AbstractParallelElementView(IManager manager) {
		super();
		this.manager = manager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		createView(parent);
		setContentDescription(EMPTY_TITLE);
		registerColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	}
	/** Set the color of registered element
	 * @param color
	 */
	public void setRegisterColor(Color color) {
		this.registerColor = color;
	}
	/** Create Element View
	 * @param parent
	 */
	protected void createView(Composite parent) {
		createElementView(parent);
	}
	/** Get IManager
	 * @return IManager
	 */
	public IManager getUIManager() {
		return manager;
	}
	public IElementHandler getElementHandler(String id) {
		return manager.getElementHandler(id);
	}
	/** Get current element handler
	 * @return IElementHandler
	 */
	public IElementHandler getCurrentElementHandler() {
		return getElementHandler(getCurrentID());
	}
	/** Change view title
	 * @param title title
	 * @param setName set name
	 * @param size element size
	 */
	protected void changeTitle(String title, String setName, int size) {
		changeTitle(" " + title + " - " + setName + " [" + size + "]");
	}
	/** Change view title
	 * @param message Message of title
	 */
	protected void changeTitle(final String message) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				setContentDescription(message);
			}
		});
	}
	/** Create element videw
	 * @param parent parent composite
	 * @return composite
	 */
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
		canvas.addSelectionChangedListener(this);
		PTPUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		getSite().setSelectionProvider(canvas);
		return composite;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		canvas.removeActionListener(this);
		canvas.removeSelectionChangedListener(this);
		canvas.dispose();
		PTPUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		canvas.setFocus();
	}
	/** Get current set ID
	 * @return current set ID
	 */
	public String getCurrentSetID() {
		return manager.getCurrentSetId();
	}
	/** Fire set change event
	 * @param cur_set current set 
	 * @param pre_set previous set
	 */
	public void fireChangeEvent(IElementSet cur_set, IElementSet pre_set) {
		manager.fireSetEvent(IManager.CHANGE_SET_TYPE, null, cur_set, pre_set);
	}
	/** Select set
	 * @param set Target set
	 */
	public void selectSet(IElementSet set) {
		if (!canvas.isDisposed()) {
			cur_set_size = 0;
			if (set != null) {
				cur_set_size = set.size();
				manager.setCurrentSetId(set.getID());
			}
			canvas.setElementSet(set);
			fireChangeEvent(set, cur_element_set);
			cur_element_set = set;
		}
		updateAction();
	}
	/** Get current set
	 * @return current set
	 */
	public IElementSet getCurrentSet() {
		return cur_element_set;
	}
	public void build() {
		initialView();
	}
	/** Refresh view
	 * 
	 */
	public void refresh(final boolean all) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (all)
					updateAction();
				repaint(all);
				if (!canvas.isDisposed()) {
					canvas.redraw();
				}
			}
		});
	}
	public abstract void updateAction();
	// Set element info
	/** Initial view setting
	 * 
	 */
	protected abstract void initialView();
	// Set element to display
	/** Initial elements setting
	 * 
	 */
	protected abstract void initialElement();
	// set update view details
	/** Update view details
	 * 
	 */
	public abstract void update();
	/** Update view title
	 * 
	 */
	public abstract void updateTitle();
	/** Get current ID
	 * @return element ID
	 */
	public abstract String getCurrentID();
	/** Double click action
	 * @param element Target element
	 */
	protected abstract void doubleClick(IElement element);
	/** Get element image
	 * @param index1 first array index
	 * @param index2 second array index
	 * @return image
	 */
	protected abstract Image getImage(int index1, int index2);
	/** Get tooltip text
	 * @param obj Selected element
	 * @return text of tooltip
	 */
	protected abstract String[] getToolTipText(Object obj);
	/** Find actual object
	 * @param element Target element
	 * @return object represent of element
	 */
	protected abstract Object convertElementObject(IElement element);
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * IContentProvider
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IContentProvider#getObject(int)
	 */
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
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IToolTipProvider#toolTipText(java.lang.Object)
	 */
	public String[] toolTipText(Object obj) {
		if (obj instanceof IElement) {			
			return getToolTipText(convertElementObject((IElement)obj));
		}
		return new String[] { "" };
	}	
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Image Provider
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IImageProvider#getStatusIcon(java.lang.Object, boolean)
	 */
	public Image getStatusIcon(Object obj, boolean isSelected) {
		if (cur_element_set != null && obj instanceof IElement) {
			int status = manager.getStatus((IElement)obj);
			return getImage(status, isSelected ? 1 : 0);
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IImageProvider#drawSpecial(java.lang.Object, org.eclipse.swt.graphics.GC, int, int, int, int)
	 */
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
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IIconCanvasActionListener#handleAction(int, int)
	 */
	public void handleAction(int type, int index) {
		if (index > -1) {
			IElement element = canvas.getElement(index);
			switch (type) {
				case IIconCanvasActionListener.DOUBLE_CLICK_ACTION:
					doubleClick(element);
					break;
			}
		}
	}
	/** Show ruler
	 * @param showRuler true if show ruler
	 */
	public void setDisplayRuler(final boolean showRuler) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!canvas.isDisposed()) {
					canvas.setDisplayRuler(showRuler);
				}
				refresh(false);
			}
		});
	}
	/** Is ruler displayed
	 * @return true if ruler is displayed
	 */
	public boolean isDisplayRuler() {
		if (!canvas.isDisposed()) {
			return canvas.isDisplayRuler();
		}
		return false;
	}
	
	public ISelection getSelection() {
		return canvas.getSelection();
	}
	
    public void selectionChanged(SelectionChangedEvent event) {}
}
