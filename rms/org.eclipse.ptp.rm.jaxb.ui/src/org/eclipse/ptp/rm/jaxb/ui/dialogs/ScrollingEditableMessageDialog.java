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
package org.eclipse.ptp.rm.jaxb.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Displays text contents for editing/saving. Note: the font used is fixed to be
 * Courier, which should be available on Mac, Windows and Linux platforms. More
 * lightweight than a full-blown text editor.
 * 
 * @author arossi
 */
public class ScrollingEditableMessageDialog extends MessageDialog implements IJAXBUINonNLSConstants {
	public static final int DEFAULT_INDEX = 0;
	public static final String[] DEFAULT_LABELS = { Messages.DialogClose };

	protected Button okButton;
	protected boolean readOnly;
	protected Text scrollable;
	protected String title;
	protected String value;

	public ScrollingEditableMessageDialog(Shell parentShell, String name, String value) {
		this(parentShell, name, value, false);
	}

	public ScrollingEditableMessageDialog(Shell parentShell, String name, String value, boolean readOnly) {
		this(parentShell, name, value, name, null, MessageDialog.NONE, DEFAULT_LABELS, DEFAULT_INDEX);
		this.readOnly = readOnly;
	}

	public ScrollingEditableMessageDialog(Shell parentShell, String name, String value, String dialogTitle, Image dialogTitleImage,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, name, dialogImageType, dialogButtonLabels, defaultIndex);
		this.value = value;
		readOnly = false;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int open() {
		createDialogArea(getParentShell());
		return super.open();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			value = scrollable.getText();
		} else {
			value = null;
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will also set enablement on the
		// OK button
		scrollable.setFocus();
		if (value != null) {
			scrollable.setText(value);
			scrollable.selectAll();
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control c = super.createDialogArea(parent);
		GridData data = WidgetBuilderUtils.createGridDataFill(convertWidthInCharsToPixels(160), 550, 1);
		scrollable = WidgetBuilderUtils.createText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL, data,
				readOnly, null);
		Display d = Display.getCurrent();
		// three fonts for Mac, Linux, Windows ...
		FontData[][] f = { d.getFontList(COURIER, true), d.getFontList(COURIER, false), d.getFontList(COURIER, true),
				d.getFontList(COURIER, false), d.getFontList(COURIER, true), d.getFontList(COURIER, false) };
		int i = 0;
		for (; i < f.length; i++) {
			if (f[i].length > 0) {
				scrollable.setFont(new Font(d, f[i]));
				break;
			}
		}
		if (i == f.length) {
			applyDialogFont(scrollable);
		}
		return c;
	}
}
