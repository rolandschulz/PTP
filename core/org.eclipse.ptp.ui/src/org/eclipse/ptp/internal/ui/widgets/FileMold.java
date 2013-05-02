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

import org.eclipse.ptp.internal.ui.messages.Messages;

/**
 * Mold to a {@link FileGroup}. This isn't a derivated class
 * from {@link TextMold} because some of the its fields doesn't make sense
 * for this class.
 * 
 * @author Richard Maciel
 * 
 */
public class FileMold extends GenericControlMold {

	public static final int DIRECTORY_SELECTION = 1 << index++;

	protected static final String BUTTON_TEXT = Messages.BrowseButtonText;

	int bitmask;

	/*
	 * String controlLabel;
	 * 
	 * boolean labelAbove;
	 */

	String controlValue;

	String dialogLabel;
	String dialogMessage;

	public FileMold(int bitmask, String label, String dialogLabel, String dialogMessage) {
		super(bitmask, label);
		this.bitmask = bitmask;
		// this.controlLabel = label;
		// this.controlValue = value;
		this.dialogLabel = dialogLabel;
		this.dialogMessage = dialogMessage;
	}

	public String getControlValue() {
		return controlValue;
	}

	public void setControlValue(String controlValue) {
		this.controlValue = controlValue;
	}

	public String getDialogLabel() {
		return dialogLabel;
	}

	public void setDialogLabel(String dialogLabel) {
		this.dialogLabel = dialogLabel;
	}

	public String getDialogMessage() {
		return dialogMessage;
	}

	public void setDialogMessage(String dialogMessage) {
		this.dialogMessage = dialogMessage;
	}

	protected TextMold getTextGroupMold() {

		int bitmask = this.bitmask;

		// Must have button!
		bitmask |= HASBUTTON;

		TextMold tmold = new TextMold(bitmask, label);
		tmold.setButtonLabel(BUTTON_TEXT);

		return tmold;
	}
}
