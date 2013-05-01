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
package org.eclipse.ptp.internal.debug.ui.actions;

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.ui.PDebugModelPresentation;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * @author Clement chu
 * 
 */
public class ShowFullPathsAction extends ViewFilterAction {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		final StructuredViewer viewer = getStructuredViewer();
		IDebugView view = (IDebugView) getView().getAdapter(IDebugView.class);
		if (view != null) {
			IDebugModelPresentation pres = view.getPresentation(PTPDebugUIPlugin.getUniqueIdentifier());
			if (pres != null) {
				pres.setAttribute(PDebugModelPresentation.DISPLAY_FULL_PATHS, (getValue() ? Boolean.TRUE : Boolean.FALSE));
				BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
					public void run() {
						viewer.refresh();
						String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
						Preferences.setBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), key, getValue());
						Preferences.savePreferences(PTPDebugCorePlugin.getUniqueIdentifier());
					}
				});
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
	 * .Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.ui.actions.ViewFilterAction#getPreferenceKey
	 * ()
	 */
	@Override
	protected String getPreferenceKey() {
		return IPDebugConstants.PREF_SHOW_FULL_PATHS;
	}
}
