/*******************************************************************************
 * Copyright (c) 2010 University of Illinois. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors:
 * 	Albert L. Rossi (NCSA) - design and implementation
 *                         - modified (09/14/2010) to use non-nls interface
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Allows the user to make a selection from a combo box with fixed entries.
 * 
 * @author arossi
 */
public class ComboEntryDialog extends Dialog implements IPBSNonNLSConstants {
	private String[] choices;

	private String chosen;
	private Combo combo;
	private final String title;

	public ComboEntryDialog(Shell parentShell, String dialogTitle, String[] initialValues) {
		super(parentShell);
		this.title = dialogTitle;
		if (initialValues == null)
			choices = new String[0];
		else
			choices = initialValues;
	}

	public String getChoice() {
		return chosen;
	}

	@Override
	public int open() {
		super.open();
		return getReturnCode();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID)
			chosen = WidgetUtils.getSelected(combo);
		else
			chosen = null;
		super.buttonPressed(buttonId);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		combo = WidgetUtils.createItemCombo(composite, title, choices, ZEROSTR, ZEROSTR, true, null, 2);
		combo.setFocus();
		applyDialogFont(composite);
		return composite;
	}
}
