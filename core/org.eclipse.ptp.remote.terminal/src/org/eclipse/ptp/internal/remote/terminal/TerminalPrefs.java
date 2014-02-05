/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.internal.remote.terminal.messages.Messages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TerminalPrefs extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String SHELL_STARTUP_DEFAULT = "export PTP_TERM=1"; //$NON-NLS-1$

	public void init(IWorkbench arg0) {
		IPreferenceStore prefs;
		setPreferenceStore(prefs = TerminalPlugin.getDefault().getPreferenceStore());
		setDefault(prefs, Messages.SHELL_STARTUP_COMMAND, SHELL_STARTUP_DEFAULT);
	}

	private void setDefault(IPreferenceStore prefs, String key, String defaultVal) {
		String val = prefs.getString(key);
		if (val == null || "".equals(val)) //$NON-NLS-1$
			prefs.setDefault(key, val);
	}

	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new StringFieldEditor(Messages.SHELL_STARTUP_COMMAND, Messages.STARTUP_COMMAND_TITLE, parent));
	}
}
