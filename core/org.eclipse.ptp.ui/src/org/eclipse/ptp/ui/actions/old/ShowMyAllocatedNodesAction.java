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
package org.eclipse.ptp.ui.actions.old;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.ui.old.ParallelImages;
import org.eclipse.ptp.ui.old.UIMessage;
import org.eclipse.ptp.ui.views.old.AbstractParallelView;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class ShowMyAllocatedNodesAction extends ParallelAction {

	public ShowMyAllocatedNodesAction(ViewPart view) {
		super(view, Action.AS_RADIO_BUTTON);
	}

	protected void init(boolean isEnable) {
	    this.setText(UIMessage.getResourceString("ShowMyAllocatedNodesAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ShowMyAllocatedNodesAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWMYALLOCNODES_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWMYALLOCNODES_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWMYALLOCNODES_ACTION_HOVER));
	    /*this.setEnabled(getLaunchManager().isMPIRuning());*/
	    this.setEnabled(true);
	}

	public void run() {
		((AbstractParallelView)getViewPart()).showMyAllocatedNodes();
	}

}
