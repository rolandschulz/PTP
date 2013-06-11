/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.toolopts;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * A reference implementation for the type of listener that can be associated with a ToolPane's tools
 * 
 * @author wspear
 * 
 * @since 5.0
 */
public class ToolPaneListener extends SelectionAdapter implements ModifyListener {

	IToolUITab thisTool;

	@SuppressWarnings("unused")
	private ToolPaneListener() {

	}

	/**
	 * @since 5.0
	 */
	public ToolPaneListener(IToolUITab tool) {
		thisTool = tool;
	}

	protected void localAction() {
		// updateLaunchConfigurationDialog();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent evt) {
		// Object source = evt.getSource();
		// if (thisTool.updateOptField(source))
		// localAction();
		// TODO: This should only be set off once per reload. For now, don't use it at all.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		thisTool.OptUpdate();
		thisTool.updateOptDisplay();
		localAction();
	}

}
