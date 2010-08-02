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
package org.eclipse.ptp.launch.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.launch.internal.rulesengine.DownloadRule;
import org.eclipse.ptp.launch.internal.rulesengine.UploadRule;
import org.eclipse.ptp.launch.internal.ui.DownloadRuleDialog;
import org.eclipse.ptp.launch.internal.ui.UploadRuleDialog;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.swt.widgets.Shell;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 */
public class RuleDialogFactory {

	/**
	 * Returns a new dialog that is able to edit a rule.
	 * 
	 * @param shell
	 *            The SWT shell for the dialog
	 * @param rule
	 *            The rule to edit
	 * @return The dialog or null if no dialog is known for the rule.
	 * @since 5.0
	 */
	public static Dialog createDialogForRule(Shell shell, ISynchronizationRule rule) {
		if (rule instanceof DownloadRule) {
			DownloadRule downloadRule = (DownloadRule) rule;
			return new DownloadRuleDialog(shell, downloadRule);
		} else if (rule instanceof UploadRule) {
			UploadRule uploadRule = (UploadRule) rule;
			return new UploadRuleDialog(shell, uploadRule);
		} else {
			return null;
		}
	}
}
