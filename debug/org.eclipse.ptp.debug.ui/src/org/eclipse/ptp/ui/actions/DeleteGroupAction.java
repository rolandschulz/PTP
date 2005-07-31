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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.UIDialog;
import org.eclipse.ptp.debug.ui.view.DebugParallelProcessView;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementGroup;
import org.eclipse.ptp.ui.view.AbstractParallelView;
import org.eclipse.swt.SWT;

/**
 * @author clement chu
 *
 */
public class DeleteGroupAction extends ParallelAction {
	public static final String name = "Delete Group";
	
	public DeleteGroupAction(AbstractParallelView debugView) {
		super(name, debugView);
	    setImageDescriptor(ImageUtil.ID_ICON_DELETEGROUP_NORMAL);
	    setDisabledImageDescriptor(ImageUtil.ID_ICON_DELETEGROUP_DISABLE);
	    setId(name);
	}

	public void run(IElement[] elements) {}
	public void run() {
		if (debugView instanceof DebugParallelProcessView) {
			DebugParallelProcessView view = (DebugParallelProcessView)debugView;

			IElementGroup group = view.getCurrentGroup();
			if (group != null && group.size() > 0) {
				 if (UIDialog.showDialog(getShell(), name + " " + group.getID(), "All elements in this group will be deleted.", SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL) == SWT.OK) {
					IMenuManager manager = view.getViewSite().getActionBars().getMenuManager();
					manager.remove(group.getID());
					view.getUIDebugManger().removeGroup(group.getID());
									
					IContributionItem[] items = manager.getItems();
					if (items.length > 0) {
						IContributionItem lastItem = items[items.length-1];				
						if (lastItem != null && lastItem instanceof ActionContributionItem) {
							((ActionContributionItem)lastItem).getAction().run();
						}
					}
				 }
			}
		}		
	}
}
