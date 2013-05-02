/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.internal.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;

public class DirectoryButtonSelectionListener extends SelectionAdapter {
	
	private Text text;
	private String title;
	private String message;

	public DirectoryButtonSelectionListener(Text text, String title, String message) {
		this.text = text;
		this.title = title;
		this.message = message;
	}
	
	public void widgetSelected(SelectionEvent e) {
		super.widgetSelected(e);
		
		DirectoryDialog directoryDialog = new DirectoryDialog(text.getShell(), SWT.OPEN);
		
		directoryDialog.setFilterPath(text.getText());
		directoryDialog.setText(title);
		
		if (message != null) {
			directoryDialog.setMessage(message);
		}
		String newPath = directoryDialog.open();

		if (newPath != null) {
			text.setText(newPath);
		}
	}
}