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
package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.ui.LegendDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 */

public class ShowLegendAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public void run(IAction action) {
		new LegendDialog(window.getShell()).open();
	}
	public void selectionChanged(IAction action, ISelection selection) {
	}	
	public void dispose() {
	}	
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}	
}

/*
public class ShowLegendAction extends ParallelAction {
	public ShowLegendAction(ViewPart view) {
		super(view);
	}

	protected void init(boolean isEnable) {
	    this.setText(UIMessage.getResourceString("ShowLegendAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ShowLegendAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWLEGEND_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWLEGEND_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWLEGEND_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().isMPIRuning());
	}

	public void run() {
		LegendDialog ld = new LegendDialog(getShell());
		ld.open();
	}
}
*/
