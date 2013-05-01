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
package org.eclipse.ptp.internal.debug.ui.views;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.debug.core.IPDebugEventListener;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author Clement Chu
 */
public abstract class AbstractPDebugViewEventHandler implements IPDebugEventListener {
	/**
	 * This event handler's view
	 */
	private IViewPart fView;

	protected PDebugEventWorkbenchJob eventWorkbenchJob = new PDebugEventWorkbenchJob();

	/**
	 * Constructs an event handler for the given view.
	 * 
	 * @param view
	 *            debug view
	 */
	public AbstractPDebugViewEventHandler(IViewPart view) {
		setView(view);
		PTPDebugCorePlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		PTPDebugCorePlugin.getDefault().removeDebugEventListener(this);
	}

	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}

	public void handleDebugEvent(final IPDebugEvent event) {
		eventWorkbenchJob.addEvent(event);
		/*
		 * WorkbenchJob uiJob = new WorkbenchJob("Handling Debug Event") {
		 * public IStatus runInUIThread(IProgressMonitor monitor) {
		 * doHandleDebugEvent(event, monitor);
		 * //updateForDebugEvent(event, monitor);
		 * monitor.done();
		 * return Status.OK_STATUS;
		 * }
		 * };
		 * uiJob.setSystem(false);
		 * uiJob.schedule();
		 */
	}

	/**
	 * Updates this view for the given debug events. Unlike
	 * doHandleDebugEvent(IPDebugEvent) which is only called if the view is
	 * visible, this method is always called. This allows the view to perform
	 * updating that must always be performed, even when the view is not
	 * visible.
	 */
	// protected void updateForDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {}
	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor);

	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected abstract void refresh(boolean all);

	/**
	 * Refresh the viewer - must be called in UI thread.
	 */
	public void refresh() {
		refresh(false);
	}

	/**
	 * Returns the view this event handler is updating.
	 * 
	 * @return debug view
	 */
	protected IViewPart getView() {
		return this.fView;
	}

	/**
	 * Sets the view this event handler is updating.
	 * 
	 * @param view
	 *            debug view
	 */
	private void setView(IViewPart view) {
		this.fView = view;
	}

	class PDebugEventWorkbenchJob extends WorkbenchJob {
		Vector<IPDebugEvent> events = new Vector<IPDebugEvent>(10);

		PDebugEventWorkbenchJob() {
			super(Messages.AbstractPDebugViewEventHandler_0);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IPDebugEvent[] pEvents;
			synchronized (events) {
				pEvents = events.toArray(new IPDebugEvent[0]);
				events.clear();
			}
			for (IPDebugEvent e : pEvents) {
				doHandleDebugEvent(e, monitor);
			}
			// updateForDebugEvent(event, monitor);
			monitor.done();
			return Status.OK_STATUS;
		}

		public void addEvent(IPDebugEvent event) {
			synchronized (events) {
				events.add(event);
			}
			schedule();
		}
	}
}
