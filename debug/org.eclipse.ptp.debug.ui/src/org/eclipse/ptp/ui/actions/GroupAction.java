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
import org.eclipse.ptp.debug.ui.views.DebugParallelProcessView;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelView;
/**
 * @author clement chu
 *
 */
public class GroupAction extends ParallelAction {
	public static final String GROUP_ROOT = "Root";
	public static final String name = "Group";
	
	public GroupAction(String id, AbstractParallelView debugView) {
		super(name + " " + id, IAction.AS_CHECK_BOX, debugView);
	    setEnabled(true);
		setId(id);
	}

	public void run(IElement[] elements) {}
	
	public void run() {
		if (debugView instanceof DebugParallelProcessView) {
			DebugParallelProcessView view = (DebugParallelProcessView)debugView;
			view.selectGroup(getId());
			//deselect all elements each time changed the group
			view.getCurrentGroup().setAllSelect(false);
			view.updateMenu(view.getViewSite().getActionBars().getMenuManager());
			view.redraw();
		}
	}	
}
