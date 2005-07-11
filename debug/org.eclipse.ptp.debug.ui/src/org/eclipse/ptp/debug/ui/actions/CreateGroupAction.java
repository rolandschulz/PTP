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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.UIDialog;
import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
import org.eclipse.ptp.debug.ui.model.IGroupManager;
import org.eclipse.ptp.debug.ui.model.internal.Element;
import org.eclipse.ptp.debug.ui.model.internal.ElementGroup;
import org.eclipse.ptp.debug.ui.views.AbstractDebugParallelView;
import org.eclipse.ptp.debug.ui.views.DebugParallelProcessView;
import org.eclipse.swt.SWT;
/**
 * @author clement chu
 *
 */
public class CreateGroupAction extends ParallelDebugAction {
	public static final String name = "Create Group";

	public CreateGroupAction(AbstractDebugParallelView debugView) {
		super(name, debugView);
	    this.setImageDescriptor(ImageUtil.ID_ICON_CREATEGROUP_NORMAL);
	    this.setDisabledImageDescriptor(ImageUtil.ID_ICON_CREATEGROUP_NORMAL);
	}

	public void run(Element[] elements) {
		if (elements == null || elements.length == 0) {
			UIDialog.showDialog(getShell(), "No selected elements", "Please select some elements first", SWT.ICON_ERROR | SWT.OK);
			return;
		}	
		
		if (debugView instanceof DebugParallelProcessView) {
			final DebugParallelProcessView view = (DebugParallelProcessView)debugView;

			IGroupManager groupManager = view.getGroupManager();
			final IElementGroup group = new ElementGroup(true);
			for (int i=0; i<elements.length; i++) {
				IElement newElement = elements[i].cloneElement();
				newElement.setSelected(false);
				group.addElement(newElement);
			}
			groupManager.addGroup(group);

			IMenuManager manager = view.getViewSite().getActionBars().getMenuManager();
			manager.add(new GroupAction(group.getID(), view));
			view.selectGroup(group.getID());
			view.updateMenu(manager);			
			view.redraw();
		}
	}	
}
