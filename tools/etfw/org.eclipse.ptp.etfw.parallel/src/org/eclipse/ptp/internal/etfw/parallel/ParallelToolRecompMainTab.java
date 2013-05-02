/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw.parallel;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.internal.etfw.parallel.messages.Messages;
import org.eclipse.ptp.launch.ui.tabs.ApplicationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Extends the default PTP main launch configuration tab, primarily changing the binary selection to build-configuration selection
 * 
 * @author wspear
 * 
 */
public class ParallelToolRecompMainTab extends ApplicationTab {

	protected class PerfRecompWidgetListener extends WidgetListener {
		@Override
		public void modifyText(ModifyEvent e) {
			super.modifyText(e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			super.widgetSelected(e);
			Object source = e.getSource();
			if (source == projButton) {
				int bDex = buildConfCombo.getSelectionIndex();
				String bString = buildConfCombo.getText();
				initConfCombo();
				if (bDex >= 0 && buildConfCombo.getItemCount() > bDex && buildConfCombo.getItem(bDex).equals(bString)) {
					buildConfCombo.select(bDex);
				} else if (buildConfCombo.getItemCount() > 0) {
					buildConfCombo.select(0);
				}

			}
		}
	}

	private Combo buildConfCombo = null;
	protected WidgetListener listener = new PerfRecompWidgetListener();

	private void createAppControl(Composite comp) {

		Composite mainComp = new Composite(comp, SWT.NONE);
		mainComp.setLayout(createGridLayout(2, false, 0, 0));
		mainComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label appLabel = new Label(mainComp, SWT.NONE);
		appLabel.setText(Messages.ParallelToolRecompMainTab_LangBuildConf);
		appLabel.setLayoutData(spanGridData(-1, 2));

		buildConfCombo = new Combo(mainComp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		buildConfCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buildConfCombo.addModifyListener(listener);
	}

	@Override
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		comp.setLayout(new GridLayout());
		// createVerticalSpacer(comp, 1);

		super.createControl(comp);
		setControl(comp);
		createAppControl(comp);

	}

	/**
	 * Allow the user to choose a project
	 */
	@Override
	protected void handleProjectButtonSelected() {
		IProject project = chooseProject();
		if (project == null) {
			return;
		}

		String projectName = project.getName();
		projText.setText(projectName);
		initConfCombo();
	}

	private void initializeConfCombo(ILaunchConfiguration configuration) {
		try {

			initConfCombo();

			String programName = configuration.getAttribute(
					IToolLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, EMPTY_STRING);

			int progDex = buildConfCombo.indexOf(programName);

			if (!programName.equals(EMPTY_STRING) && progDex >= 0) {
				buildConfCombo.select(progDex);
			} else {
				buildConfCombo.select(0);
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {

		super.initializeFrom(configuration);

		initializeConfCombo(configuration);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);

		configuration.setAttribute(IToolLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME,
				buildConfCombo.getText());
	}

	/**
	 * Return the ICProject corresponding to the project name in the project name text field, or null if the text does not match a
	 * project name.
	 */
	protected ICProject getCProject() {
		String projectName = projText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return CoreModel.getDefault().getCModel().getCProject(projectName);
	}

	/**
	 * Initialize the combo box listing the available build configurations for this project
	 * 
	 */
	@SuppressWarnings("restriction")
	protected void initConfCombo() {
		buildConfCombo.removeAll();
		ICProject project = getCProject();
		if (project == null) {
			// MessageDialog.openInformation(getShell(),
			// org.eclipse.cdt.launch.internal.ui.LaunchMessages.getString("CMainTab.Project_required"),
			// org.eclipse.cdt.launch.internal.ui.LaunchMessages.getString("CMainTab.Enter_project_before_searching_for_program"));
			return;
		}

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project.getResource());
		if (info == null) {
			MessageDialog.openInformation(getShell(),
					org.eclipse.cdt.launch.internal.ui.LaunchMessages.CMainTab_Enter_project_before_searching_for_program,
					org.eclipse.cdt.launch.internal.ui.LaunchMessages.CMainTab_Enter_project_before_searching_for_program);// .getString("CMainTab.Enter_project_before_searching_for_program"));
			return;
		}

		IConfiguration[] confs = info.getManagedProject().getConfigurations();

		for (IConfiguration conf : confs) {
			buildConfCombo.add(conf.getName());
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {

		String name = null;
		boolean recompiles = false;
		try {
			recompiles = config.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_RECOMPILE, false);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		boolean status = super.isValid(config);

		/*
		 * If we are not in a recompilation workflow we don't care about the build configuration
		 */
		if (!recompiles) {
			return status;
		} else {
			name = getFieldContent(buildConfCombo.getText());
			if (name == null) {
				setErrorMessage(Messages.ParallelToolRecompMainTab_BuildConfNotSpeced);
				return false;
			}
		}

		return status;
	}
}