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
package org.eclipse.ptp.debug.internal.ui.actions;

import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Clement chu
 *
 */
public abstract class ViewFilterAction extends ViewerFilter implements IViewActionDelegate, IActionDelegate2 {
	private IViewPart view = null;
	private IAction action = null;

	/** Constructor
	 * 
	 */
	public ViewFilterAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		this.view = view;
		action.setChecked(getPreferenceValue(view));
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		this.action = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		StructuredViewer viewer = getStructuredViewer();
		ViewerFilter[] filters = viewer.getFilters();
		ViewerFilter filter = null;
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] == this) {
				filter = filters[i];
				break;
			}
		}
		if (filter == null) {
			viewer.addFilter(this);
		}
		viewer.refresh();
		IPreferenceStore store = getPreferenceStore();
		String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
		store.setValue(key, action.isChecked());
		PTPDebugUIPlugin.getDefault().savePluginPreferences();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}

	/** Get preference store
	 * @return
	 */
	protected IPreferenceStore getPreferenceStore() {
		return PTPDebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/** Get preference value
	 * @param part
	 * @return
	 */
	protected boolean getPreferenceValue(IViewPart part) {
		String baseKey = getPreferenceKey();
		String viewKey = part.getSite().getId();
		String compositeKey = viewKey + "." + baseKey; //$NON-NLS-1$
		IPreferenceStore store = getPreferenceStore();
		boolean value = false;
		if (store.contains(compositeKey)) {
			value = store.getBoolean(compositeKey);
		} else {
			value = store.getBoolean(baseKey);
		}
		return value;		
	}
	
	/** Get preference key
	 * @return
	 */
	protected abstract String getPreferenceKey(); 

	/** Get view
	 * @return
	 */
	protected IViewPart getView() {
		return view;
	}
	
	/** Get structured viewer
	 * @return
	 */
	protected StructuredViewer getStructuredViewer() {
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		if (view != null) {
			Viewer viewer = view.getViewer();
			if (viewer instanceof StructuredViewer) {
				return (StructuredViewer)viewer;
			}
		}		
		return null;
	}
	
	/** Check if action is checked
	 * @return true is action is checked
	 */
	protected boolean getValue() {
		return action.isChecked();
	}
}
