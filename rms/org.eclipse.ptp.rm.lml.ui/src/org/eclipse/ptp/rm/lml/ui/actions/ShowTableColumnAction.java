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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.swt.widgets.Display;

public class ShowTableColumnAction extends AbstractHandler {
	
	private String gid;
	
	
	public ShowTableColumnAction(String gid, String name) {
		this.gid = gid; 
	}
	
	public void run() {
		// TODO Remove
		MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Click!", "There will be an update and an additional tablecolumn () will be seen");
		LMLCorePlugin.getDefault().getLMLManager().setTableColumnActive(gid, "");
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

}
