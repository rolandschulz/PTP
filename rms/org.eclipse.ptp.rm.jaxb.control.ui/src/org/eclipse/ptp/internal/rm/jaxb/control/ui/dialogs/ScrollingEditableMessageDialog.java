/*******************************************************************************
 * Copyright (c) 2010 University of Illinois. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors:
 * 	Albert L. Rossi (NCSA) - design and implementation
 *                           added readOnly option 05/11/2010
 *                         - modified (09/14/2010) to use non-nls interface
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Displays text contents for editing/saving. Note: the font hint used is
 * Courier, which should be available on Mac, Windows and Linux platforms. More
 * lightweight than a full-blown text editor.
 * 
 * @author arossi
 */
public class ScrollingEditableMessageDialog extends MessageDialog {
	public static final int DEFAULT_INDEX = 0;
	public static final String[] DEFAULT_LABELS = { Messages.DialogClose };

	protected Button okButton;
	protected boolean readOnly;
	protected Text scrollable;
	protected String title;
	protected String value;

	/**
	 * @param parentShell
	 * @param name
	 *            message label for text
	 * @param value
	 *            initial text value
	 */
	public ScrollingEditableMessageDialog(Shell parentShell, String name, String value) {
		this(parentShell, name, value, false);
	}

	/**
	 * @param parentShell
	 * @param name
	 *            message label for text
	 * @param value
	 *            initial text value
	 * @param readOnly
	 *            if the text box if read only
	 */
	public ScrollingEditableMessageDialog(Shell parentShell, String name, String value, boolean readOnly) {
		this(parentShell, name, value, name, null, MessageDialog.NONE, DEFAULT_LABELS, DEFAULT_INDEX);
		this.readOnly = readOnly;
	}

	/**
	 * @param parentShell
	 * @param name
	 *            message label for text
	 * @param value
	 *            initial text value
	 * @param dialogTitle
	 * @param dialogTitleImage
	 * @param dialogImageType
	 * @param dialogButtonLabels
	 * @param defaultIndex
	 */
	public ScrollingEditableMessageDialog(Shell parentShell, String name, String value, String dialogTitle, Image dialogTitleImage,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, name, dialogImageType, dialogButtonLabels, defaultIndex);
		this.value = value;
		readOnly = false;
	}

	/**
	 * The entered value.
	 */
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.MessageDialog#open()
	 */
	@Override
	public int open() {
		createDialogArea(getParentShell());
		return super.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.MessageDialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			value = scrollable.getText();
		} else {
			value = null;
		}
		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.MessageDialog#configureShell(org.eclipse.swt
	 * .widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.MessageDialog#createButtonsForButtonBar(org
	 * .eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		if (!readOnly) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
		// do this here because setting the text will also set enablement on the
		// OK button
		scrollable.setFocus();
		if (value != null) {
			scrollable.setText(value);
			scrollable.selectAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.MessageDialog#createDialogArea(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Control c = super.createDialogArea(parent);
		GridData data = WidgetBuilderUtils.createGridDataFill(convertWidthInCharsToPixels(160), 550, 1);
		scrollable = WidgetBuilderUtils.createText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL, data,
				readOnly, null);
		WidgetBuilderUtils.applyMonospace(scrollable);
		return c;
	}
}
