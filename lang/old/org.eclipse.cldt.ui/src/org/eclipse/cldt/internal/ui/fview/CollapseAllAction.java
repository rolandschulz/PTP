/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.ui.fview;

import org.eclipse.cldt.internal.ui.FortranPluginImages;
import org.eclipse.cldt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Collapse all nodes.
 */
class CollapseAllAction extends Action {
	
	private FortranView cview;
	
	CollapseAllAction(FortranView part) {
		super(FortranViewMessages.getString("CollapseAllAction.label")); //$NON-NLS-1$
		setDescription(FortranViewMessages.getString("CollapseAllAction.description")); //$NON-NLS-1$
		setToolTipText(FortranViewMessages.getString("CollapseAllAction.tooltip")); //$NON-NLS-1$
		FortranPluginImages.setImageDescriptors(this, FortranPluginImages.T_LCL, FortranPluginImages.IMG_MENU_COLLAPSE_ALL);
		cview = part;
		WorkbenchHelp.setHelp(this, ICHelpContextIds.COLLAPSE_ALL_ACTION);
	}
 
	public void run() { 
		cview.collapseAll();
	}
}