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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

public class FileButtonSelectionListener extends SelectionAdapter {
	
	private Text text;
	private String title;

	public FileButtonSelectionListener(Text text, String title) {
		this.text = text;
		this.title = title;
	}
	
	public void widgetSelected(SelectionEvent e) {
		super.widgetSelected(e);
		
		FileDialog fileDialog = new FileDialog(text.getShell(), SWT.OPEN);

		/*
		 * Filter path must be set to the directory that contains the file.
		 */
		String fileName = text.getText();
		IPath path = new Path(fileName);
		fileDialog.setFilterPath(path.removeLastSegments(1).toString());
		fileDialog.setText(title);
		fileDialog.setFileName(path.lastSegment());
				
		String result = fileDialog.open();

		if (result != null) {
			text.setText(result);
		}
	}
}
    
    