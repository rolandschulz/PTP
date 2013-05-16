/*******************************************************************************
 * Copyright (c) 2011,2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to display a File tree and other options, where users can select the patterns/files to be sync'ed.<br>
 * This class is used for two scenarios:<br>
 * 1. Filter page for new sync project - to specify settings for new project<br>
 * 2. Filter page for existing project - to alter existing Sync Filter settings<br>
 * 
 * Uses ResourceMatchers (e.g. PathResourceMatcher, RegexResourceMatcher, and WildcardResourceMatcher) to match the
 * "patterns" entered to actual files in the project.
 */
public class ManageConfigurationDialog extends Dialog {
	private final IProject fProject;
	private ManageConfigurationWidget fWidget;

	/**
	 * Constructor for a new filter dialog. Behavior of the page varies based on whether arguments are null and whether
	 * targetFilter is set. Specifically, whether the file view is shown, how or if the filter is saved, and if preference page
	 * functionality is available. See comments for the default constructor and static open methods for details.
	 * 
	 * @param p
	 *            project
	 * @param targetFilter
	 */
	public ManageConfigurationDialog(Shell parent, IProject project) {
		super(parent);
		fProject = project;
		setShellStyle(SWT.RESIZE | getShellStyle());
		setReturnCode(CANCEL);
	}

	/**
	 * Configures the shell (sets window title)
	 * 
	 * @param shell
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.NewSyncFileFilterPage_Title);
	}

	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite controls = (Composite) super.createDialogArea(parent);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 400;
		controls.setLayoutData(data);

		fWidget = new ManageConfigurationWidget(controls, SWT.NONE);
		fWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fWidget.setProject(fProject);

		return controls;
	}

	@Override
	protected void okPressed() {
		fWidget.commit();
		setReturnCode(OK);
		close();
	}
}