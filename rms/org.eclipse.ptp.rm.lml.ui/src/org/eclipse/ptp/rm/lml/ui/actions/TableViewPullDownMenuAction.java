/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */
package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class TableViewPullDownMenuAction extends Action {
	
	public TableViewPullDownMenuAction(String name) {
		setText(name);
	}
	
	public void run() {
		MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Click!", "There will be an update and an additional tablecolumn ("+ getText()+") will be seen");
	}

}
