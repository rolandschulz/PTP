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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.core.IModelListener;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.events.IModelErrorEvent;
import org.eclipse.ptp.core.events.IModelEvent;
import org.eclipse.ptp.core.events.IModelRuntimeNotifierEvent;
import org.eclipse.ptp.core.events.IModelSysChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
/**
 * @author clement chu
 *
 */
public abstract class AbstractParallelView extends ViewPart implements IModelListener {
	protected final String DEFAULT_TITLE = "Parallel";

	/** Constructor to add paralell launch listener by default
	 * 
	 */
	public AbstractParallelView() {
		PTPCorePlugin.getDefault().getModelPresentation().addModelListener(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		PTPCorePlugin.getDefault().getModelPresentation().removeModelListener(this);
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
    
	public void modelEvent(IModelEvent event) {
		if (event instanceof IModelErrorEvent) {
			build();
			refresh(true);
		}
		else if (event instanceof IModelRuntimeNotifierEvent) {
			refresh(true);
		}
		else if (event instanceof IModelSysChangedEvent) {
			IModelSysChangedEvent sysEvent = (IModelSysChangedEvent)event;
			switch (sysEvent.getType()) {
			case IModelSysChangedEvent.MAJOR_SYS_CHANGED:
			case IModelSysChangedEvent.MONITORING_SYS_CHANGED:
				build();
				break;
			case IModelSysChangedEvent.SYS_STATUS_CHANGED:
				break;
			}
			refresh(true);			
		}
	}
}
