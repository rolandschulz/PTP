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
package org.eclipse.ptp.internal.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.ptp.internal.ui.IElementManager;
import org.eclipse.ptp.internal.ui.IPTPUIConstants;
import org.eclipse.ptp.internal.ui.PTPUIPlugin;
import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.model.IElementHandler;
import org.eclipse.ptp.internal.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * 
 * @author clement chu
 * @since 7.0
 * 
 */
public abstract class AbstractParallelElementView extends AbstractParallelView implements IIconCanvasActionListener,
		IToolTipProvider, IImageProvider, IContentProvider, ISelectionChangedListener {

	protected final String DEFAULT_TITLE = Messages.AbstractParallelElementView_0;
	protected IElementManager manager = null;
	// Set
	protected IElementSet cur_element_set = null;
	protected int cur_set_size = 0;
	protected ElementIconCanvas canvas = null;
	// title
	protected final String EMPTY_TITLE = " "; //$NON-NLS-1$
	protected Color registerColor = null;

	protected IconRefreshWorkbenchJob iconreFreshJob = new IconRefreshWorkbenchJob();

	private final boolean debug = false;

	/**
	 * update preference setting
	 */
	protected IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			final String preferenceType = event.getProperty();
			final Object value = event.getNewValue();
			if (value != null) {
				showWhile(new Runnable() {
					@Override
					public void run() {
						if (!canvas.isDisposed()) {
							if (preferenceType.startsWith("icon")) { //$NON-NLS-1$
								IPreferenceStore store = PTPUIPlugin.getDefault().getPreferenceStore();
								canvas.setIconSpace(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_X),
										store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
								canvas.setIconSize(store.getInt(IPTPUIConstants.VIEW_ICON_WIDTH),
										store.getInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
							} else if (preferenceType.equals(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME)) {
								canvas.showTooltipAllthetime(new Boolean((String) value).booleanValue());
							} else if (preferenceType.equals(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT)) {
								canvas.setTooltipTimeout(new Long((String) value).longValue());
							} else if (preferenceType.equals(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP)) {
								canvas.setTooltipWrap(new Boolean((String) value).booleanValue());
							}
							canvas.resetCanvas();
						}
						refresh(false);
					}
				});
			}
		}
	};

	public AbstractParallelElementView(IElementManager manager) {
		this.manager = manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		registerPartListener();
		PTPUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		createView(parent);
		setContentDescription(EMPTY_TITLE);
		registerColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	}

	/**
	 * Set the color of registered element
	 * 
	 * @param color
	 */
	public void setRegisterColor(Color color) {
		this.registerColor = color;
	}

	/**
	 * Create Element View
	 * 
	 * @param parent
	 */
	protected void createView(Composite parent) {
		createElementView(parent);
	}

	/**
	 * Get IElementManager
	 * 
	 * @return IElementManager
	 */
	public IElementManager getUIManager() {
		return manager;
	}

	public IElementHandler getElementHandler(String id) {
		return manager.getElementHandler(id);
	}

	/**
	 * Get current element handler
	 * 
	 * @return IElementHandler
	 */
	public IElementHandler getCurrentElementHandler() {
		return getElementHandler(getCurrentID());
	}

	/**
	 * Change view title
	 * 
	 * @param title
	 *            title
	 * @param setName
	 *            set name
	 * @param size
	 *            element size
	 */
	protected void changeTitle(String title, String setName, int size) {
		changeTitle(" " + title + " - " + setName + " [" + size + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * Change view title
	 * 
	 * @param message
	 *            Message of title
	 */
	protected void changeTitle(final String message) {
		asyncExec(new Runnable() {
			@Override
			public void run() {
				setContentDescription(message);
			}
		});
	}

	/**
	 * Create element videw
	 * 
	 * @param parent
	 *            parent composite
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
		// getSite().setSelectionProvider(canvas);
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		canvas.removeActionListener(this);
		canvas.removeSelectionChangedListener(this);
		canvas.dispose();
		PTPUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
		deregisterPartListener();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		canvas.setFocus();
	}

	/**
	 * Get current set ID
	 * 
	 * @return current set ID
	 */
	public String getCurrentSetID() {
		return manager.getCurrentSetId();
	}

	/**
	 * Fire set change event
	 * 
	 * @param cur_set
	 *            current set
	 * @param pre_set
	 *            previous set
	 */
	public void fireSetChangeEvent(IElementSet cur_set, IElementSet pre_set) {
		manager.fireSetEvent(IElementManager.CHANGE_SET_TYPE, null, cur_set, pre_set);
	}

	/**
	 * Select set
	 * 
	 * @param set
	 *            Target set
	 */
	public void selectSet(IElementSet set) {
		if (!canvas.isDisposed()) {
			cur_set_size = 0;
			if (set != null) {
				cur_set_size = set.size();
				manager.setCurrentSetId(set.getID());
			}
			canvas.setElementSet(set);
			// send event if current viewer is visible
			if (isVisible()) {
				fireSetChangeEvent(set, cur_element_set);
			}
			cur_element_set = set;
		}
		// updateAction();
	}

	/**
	 * Get current set
	 * 
	 * @return current set
	 */
	public IElementSet getCurrentSet() {
		return cur_element_set;
	}

	public void build() {
		initialView();
	}

	/**
	 * Refresh view
	 */
	public void refresh(boolean all, boolean force) {
		if (isVisible()) {
			iconreFreshJob.schedule(all, force);
		}
	}

	public void refresh(boolean all) {
		refresh(all, false);
	}

	public abstract void updateAction();

	// Set element info
	/**
	 * Initial view setting
	 * 
	 */
	protected abstract void initialView();

	// Set element to display
	/**
	 * Initial elements setting
	 * 
	 */
	protected abstract void initialElement();

	// set update view details
	/**
	 * Update view details
	 * 
	 */
	public abstract void update();

	/**
	 * Update view title
	 * 
	 */
	public abstract void updateTitle();

	/**
	 * Get current ID
	 * 
	 * @return element ID
	 */
	public abstract String getCurrentID();

	/**
	 * Double click action
	 * 
	 * @param element
	 *            Target element
	 * @since 7.0
	 */
	protected abstract void doubleClick(int element);

	/**
	 * Get tooltip text
	 * 
	 * @param element
	 *            Selected element
	 * @return text of tooltip
	 * @since 7.0
	 */
	protected abstract String[] getToolTipText(int element);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IContentProvider#hasElement(int)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public boolean hasElement(int index) {
		if (canvas != null && manager != null) {
			return canvas.hasElement(index);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.ui.views.IContentProvider#getElement(int)
	 */
	@Override
	public int getElement(int index) {
		if (canvas != null && manager != null) {
			return canvas.getElement(index);
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IToolTipProvider#toolTipText(int)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public String[] toolTipText(int index) {
		return getToolTipText(index);
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IToolTipProvider#update(int, java.lang.String)
	 */
	@Override
	public void update(int index, String content) {
		canvas.updateToolTipText(content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IImageProvider#getStatusIcon(int, boolean)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public Image getStatusIcon(int index, boolean isSelected) {
		if (cur_element_set != null) {
			int element = cur_element_set.getElement(index);
			if (element >= 0) {
				return manager.getImage(element, isSelected);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IImageProvider#drawSpecial(int, org.eclipse.swt.graphics.GC, int, int, int, int)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public void drawSpecial(int index, GC gc, int x_loc, int y_loc, int width, int height) {
		if (cur_element_set != null) {
			int element = cur_element_set.getElement(index);
			if (element >= 0 && getCurrentElementHandler().isRegistered(element)) {
				gc.setForeground(registerColor);
				gc.drawRectangle(x_loc, y_loc, width, height);
				gc.setForeground(canvas.getForeground());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IIconCanvasActionListener#handleAction(int, int)
	 */
	@Override
	public void handleAction(int type, int index) {
		if (index > -1 && type == IIconCanvasActionListener.DOUBLE_CLICK_ACTION && cur_element_set != null) {
			doubleClick(cur_element_set.getElement(index));
		}
	}

	/**
	 * Show ruler
	 * 
	 * @param showRuler
	 *            true if show ruler
	 */
	public void setDisplayRuler(final boolean showRuler) {
		asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!canvas.isDisposed()) {
					canvas.setDisplayRuler(showRuler);
				}
				refresh(false);
			}
		});
	}

	/**
	 * Is ruler displayed
	 * 
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

	class IconRefreshWorkbenchJob extends WorkbenchJob {
		private final ReentrantLock waitLock = new ReentrantLock();
		private final List<Boolean> refreshList = new ArrayList<Boolean>();

		public IconRefreshWorkbenchJob() {
			super(Messages.AbstractParallelElementView_1);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			boolean refreshAll = isRefreshAll();
			if (debug) {
				System.err.println("---------- IconRefreshWorkbenchJob refresh: " + refreshAll); //$NON-NLS-1$
			}
			repaint(refreshAll);
			if (!canvas.isDisposed()) {
				canvas.redraw();
			}

			// if last refresh object is true and previous refresh is false, then refresh again
			boolean lastValue = isRefreshAll();
			waitLock.lock();
			try {
				refreshList.clear();
				if (refreshAll != lastValue && !refreshAll) {
					refreshList.add(new Boolean(true));
					schedule();
				}
			} finally {
				waitLock.unlock();
			}
			return Status.OK_STATUS;
		}

		@Override
		public boolean shouldSchedule() {
			int size = size();
			if (debug) {
				System.err.println("---------- IconRefreshWorkbenchJob: " + refreshList.size()); //$NON-NLS-1$
			}
			return (size > 0);
		}

		private int size() {
			waitLock.lock();
			try {
				return refreshList.size();
			} finally {
				waitLock.unlock();
			}
		}

		private boolean isRefreshAll() {
			waitLock.lock();
			try {
				return refreshList.get(refreshList.size() - 1).booleanValue();
			} finally {
				waitLock.unlock();
			}
		}

		public void schedule(boolean refresh_all, boolean force) {
			waitLock.lock();
			try {
				if (force) {
					refreshList.clear();
				}
				refreshList.add(new Boolean(refresh_all));
			} finally {
				waitLock.unlock();
			}
			schedule();
		}
	}
}
