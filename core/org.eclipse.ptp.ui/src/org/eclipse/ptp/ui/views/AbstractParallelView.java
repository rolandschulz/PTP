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

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.events.IModelManagerChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.IModelManagerNewResourceManagerEvent;
import org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
/**
 * @author clement chu
 *
 */
public abstract class AbstractParallelView extends ViewPart implements IModelManagerResourceManagerListener {
	protected final String DEFAULT_TITLE = "Parallel";

	/** Constructor to add paralell launch listener by default
	 * 
	 */
	public AbstractParallelView() {
		PTPCorePlugin.getDefault().getModelManager().addListener(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeListener(this);
		super.dispose();
	}

	/** Get Display
	 * @return display
	 */
	protected Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}
	public void asyncExec(Runnable r) {
		getDisplay().asyncExec(r);
	}
	
	public abstract void refresh(boolean all);
	public abstract void build();
	public abstract void repaint(boolean all);
	
	public abstract ISelection getSelection();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerChangedResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerChangedResourceManagerEvent e) {
		// Doesn't matter
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerNewResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerNewResourceManagerEvent e) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				build();
				refresh(true);
			}
		});	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerRemoveResourceManagerEvent e) {
		// TODO implement remove resource manager
	}
}
