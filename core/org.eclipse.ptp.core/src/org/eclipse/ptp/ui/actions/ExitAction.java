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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

/**
  *
 */
public class ExitAction extends ParallelAction {
	private IWorkbenchWindow window;
	private static ExitAction instance = null;

	public ExitAction(ViewPart viewPart) {
	    super(viewPart);
	}
	
	public ExitAction(ViewPart viewPart, boolean isEnable) {
	    super(viewPart, isEnable);	    
	}

	public void init(boolean isEnable) {
	    /*
	    this.setText(UIMessage.getResourceString("ExitAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ExitAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_EXIT_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_EXIT_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_EXIT_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().getCurrentState() == LaunchManager.STATE_RUN);
	    */
	}
	
	public void run() {
	    System.out.println("exit action - run");
	    try {
	        getLaunchManager().mpiexit();
	    } catch (CoreException e) {
	        System.out.println("exit action error: " + e.getMessage());
	    }
	}
}