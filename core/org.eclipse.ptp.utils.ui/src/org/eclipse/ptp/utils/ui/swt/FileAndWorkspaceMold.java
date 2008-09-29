/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.utils.ui.swt;

/**
 * Component that displays a textbox (which will be used to enter a path)
 * with a button to find the path in the workspace and another to find the
 * path in the filesystem. The path can be a file path or a directory path.
 * 
 * @author Richard Maciel
 *
 */
public class FileAndWorkspaceMold extends GenericControlMold {

	public static final int DIRECTORY_SELECTION = 1 << index++;
	
	String workspaceLabel;
	
	String browseWindowLabel;
	String browseWindowMessage;
	
	public FileAndWorkspaceMold(int bitmask, String label, String workspaceLabel, String fileLabel) {
		super(bitmask | GenericControlMold.HASBUTTON, label);
		setButtonLabel(fileLabel);
		this.workspaceLabel = workspaceLabel;
	}

	public String getBrowseWindowLabel() {
		return browseWindowLabel;
	}

	public void setBrowseWindowLabel(String browseWindowLabel) {
		this.browseWindowLabel = browseWindowLabel;
	}

	public String getBrowseWindowMessage() {
		return browseWindowMessage;
	}

	public void setBrowseWindowMessage(String browseWindowMessage) {
		this.browseWindowMessage = browseWindowMessage;
	}
	
}
