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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Clement chu
 */
public abstract class AbstractListenerActionDelegate  extends AbstractDebugActionDelegate implements IDebugEventSetListener, IActionDelegate2 {
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(final DebugEvent[] events) {
		if (getWindow() == null || getAction() == null) {
			return;
		}
		Shell shell= getWindow().getShell();
		if (shell == null || shell.isDisposed()) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				for (int i = 0; i < events.length; i++) {
					if (events[i].getSource() != null) {
						doHandleDebugEvent(events[i]);
					}
				}
			}
		};
		
		shell.getDisplay().asyncExec(r);
	}
	
	/**
	 * Default implementation to update on specific debug events.
	 * Subclasses should override to handle events differently.
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		switch (event.getKind()) {
			case DebugEvent.TERMINATE :
				update(getAction(), getSelection());
				break;
			case DebugEvent.RESUME :
				if (!event.isEvaluation() || !((event.getDetail() & DebugEvent.EVALUATION_IMPLICIT) != 0)) {
					update(getAction(), getSelection());
				}
				break;
			case DebugEvent.SUSPEND :
				// Update on suspend events (even for evaluations), in case the user changed
				// the selection during an implicit evaluation.
				update(getAction(), getSelection());
				break;
		}
	}		

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		super.init(window);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().addDebugEventListener(this);
		setWindow(view.getViewSite().getWorkbenchWindow());
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
}
