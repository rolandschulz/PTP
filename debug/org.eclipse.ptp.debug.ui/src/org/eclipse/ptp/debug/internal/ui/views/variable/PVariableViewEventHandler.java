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
package org.eclipse.ptp.debug.internal.ui.views.variable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.events.IPDebugEvent;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugEventHandler;
import org.eclipse.ptp.debug.ui.PJobVariableManager;

/**
 * @author Clement chu
 */
public class PVariableViewEventHandler extends AbstractPDebugEventHandler {
	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view signals view
	 */
	public PVariableViewEventHandler(PVariableView view) {
		super(view);
	}
	public PVariableView getPView() {
		return (PVariableView)getView();
	}
	public void refresh(boolean all) {
		getPView().refresh();
	}
	protected void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		switch(event.getKind()) {
			case IPDebugEvent.CREATE:
				switch (event.getDetail()) {
				case IPDebugEvent.DEBUGGER:
					getPView().setActionsEnable(true);
					refresh();
					break;
				}
				break;
			case IPDebugEvent.TERMINATE:
				switch (event.getDetail()) {
				case IPDebugEvent.DEBUGGER:
					getPView().setActionsEnable(false);					
					PJobVariableManager jobMgr = getPView().getUIManager().getJobVariableManager();
					jobMgr.removeJobVariable(event.getInfo().getJob().getIDString());
					refresh();
					break;
				}
				break;
		}
	}
}
