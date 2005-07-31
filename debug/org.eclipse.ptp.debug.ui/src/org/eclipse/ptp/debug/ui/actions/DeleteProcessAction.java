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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
import org.eclipse.ptp.debug.ui.views.AbstractParallelView;
import org.eclipse.ptp.debug.ui.views.DebugParallelProcessView;

/**
 * @author clement chu
 *
 */
public class DeleteProcessAction extends ParallelDebugAction {
	public static final String name = "Delete Process";
	
	public DeleteProcessAction(AbstractParallelView debugView) {
		super(name, debugView);
	    setImageDescriptor(ImageUtil.ID_ICON_DELETEPROCESS_NORMAL);
	    setDisabledImageDescriptor(ImageUtil.ID_ICON_DELETEPROCESS_DISABLE);
	}

	public void run(IElement[] elements) {
		if (validation(elements)) {
			if (debugView instanceof DebugParallelProcessView) {
				DebugParallelProcessView view = (DebugParallelProcessView)debugView;

				IElementGroup group = view.getCurrentGroup();
				if (group.size() == elements.length) {
					callDeleteGroupAction(view);
				} else {
					view.getUIDebugManger().removeFromGroup(elements, group.getID());
					view.selectGroup(group.getID());
					view.updateTitle();
					view.redraw();
				}
			}
		}		
	}
	
	private void callDeleteGroupAction(DebugParallelProcessView view) {
		IToolBarManager manager = view.getViewSite().getActionBars().getToolBarManager();
		IContributionItem item = manager.find(DeleteGroupAction.name);
		if (item != null && item instanceof ActionContributionItem) {
			((ActionContributionItem)item).getAction().run();
		}
	}
}
