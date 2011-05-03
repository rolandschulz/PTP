/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 * 
 * Modified by:
 * 		Claudia Knobloch, Foprschungszentrum Juelich GmbH
 *******************************************************************************/

package org.eclipse.ptp.rm.lml.ui.actions;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.util.RemoteServicesDelegate;
import org.eclipse.ptp.rm.lml.ui.LMLUIPlugin;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.ui.util.RemoteUIServicesUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class AddLguiAction extends Action {
	
	private final Shell shell;

	public AddLguiAction(Shell shell) {
		super(Messages.AddLguiAction);
		this.shell = shell;
	}

	public void dispose() {
	}

	public void run() {
		RemoteServicesDelegate remote = new RemoteServicesDelegate(null	, null);
		URI uri = null;
		try {
			uri = RemoteUIServicesUtils.browse(shell, new URI("file:///home/"), remote, false, false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		boolean existingLgui = LMLCorePlugin.getDefault().getLMLManager().addLgui(uri);
		if (existingLgui && !LMLCorePlugin.getDefault().getLMLManager().getSelectedLguiItem().getXmlFile().getPath().equals(uri.getPath())) {
			MessageBox messageBox = new MessageBox(LMLUIPlugin.getDisplay().getActiveShell(), SWT.OK | SWT.CANCEL);
			messageBox.setMessage("The LML file " + uri.getPath() + " is already loaded. Should this file be selected?");
			int rc = messageBox.open();
			if (rc == SWT.OK) {
				LMLCorePlugin.getDefault().getLMLManager().selectLgui(uri);
			}
		} 
	}

	public void selectionChanged(IAction action, ISelection selection) {
	
	}

}