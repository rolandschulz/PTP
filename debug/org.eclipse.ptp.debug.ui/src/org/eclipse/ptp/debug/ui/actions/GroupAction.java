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
package org.eclipse.ptp.debug.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.views.AbstractDebugParallelView;
import org.eclipse.ptp.debug.ui.views.DebugParallelProcessView;
/**
 * @author clement chu
 *
 */
public class GroupAction extends ParallelDebugAction {
	private int groupIndex = 0;
	
	public GroupAction(int groupIndex, AbstractDebugParallelView debugView) {
		this("Group " + (groupIndex + 1), debugView);
		this.groupIndex = groupIndex;
	}
	public GroupAction(String text, AbstractDebugParallelView debugView) {
		super(text, IAction.AS_DROP_DOWN_MENU, debugView);
	    this.setImageDescriptor(ImageUtil.ID_ICON_GROUP_NORMAL);
	    this.setDisabledImageDescriptor(ImageUtil.ID_ICON_GROUP_NORMAL);
	    this.setHoverImageDescriptor(ImageUtil.ID_ICON_GROUP_NORMAL);
	    this.setEnabled(true);
	}

	public void run(Object[] elements) {
	}
	public void run() {
		if (debugView instanceof DebugParallelProcessView) {
			this.setChecked(true);
			DebugParallelProcessView view = (DebugParallelProcessView)debugView;
			view.setCurrentGroupID(groupIndex);
			view.setCurrentProcessGroup(groupIndex);
			view.redraw();
		}
	}

}
