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

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Richard Maciel
 *
 */
public class FileGroup extends TextGroup {

	public FileGroup(Composite parent, FileMold mold) {
		super(parent, mold.getTextGroupMold());
		
		generateButtonHandler(mold);
	}
	
	protected void generateButtonHandler(FileMold mold) {
		SelectionListener btnlisten;
		
		if((mold.bitmask & FileMold.DIRECTORY_SELECTION) != 0) {
			btnlisten = new DirectoryButtonSelectionListener(
				getText(), mold.dialogLabel, mold.dialogMessage);
		} else {
			btnlisten = new FileButtonSelectionListener(
					getText(), mold.dialogLabel);
		}
		
		getButton().addSelectionListener(btnlisten);
	}

	public String getString() {
		return text.getText();
	}
	
	public void setString(String s) {
		if (s != null) {
			text.setText(s);
		} else {
			text.setText(""); //$NON-NLS-1$
		}
	}
}
