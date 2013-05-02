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
package org.eclipse.ptp.internal.etfw.ui;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.cdt.launch.ui.ICDTLaunchHelpContextIds;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Extends the standard main tab for launching C/C++/Fortran applications The
 * primary change is having the user select build configurations rather than
 * executable files, because the performance analysis system rebuilds the
 * executables
 * 
 * @author wspear
 * 
 */
@SuppressWarnings("restriction")
public class ToolRecompMainTab extends CMainTab implements ILaunchConfigurationTab {

	protected Combo projectCombo = null;
	protected Combo buildConfCombo = null;
	protected String projString = null;

	Composite buildConfComp = null;
	Composite exeComp = null;

	public ToolRecompMainTab() {
		super();
	}

	public ToolRecompMainTab(boolean x) {
		super(x);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem()
				.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createProjectGroup(comp, 1);
		createBuildConfGroup(comp, 1);
		createExeFileGroup(comp, 1);
		createVerticalSpacer(comp, 1);
		if (wantsTerminalOption() /* && ProcessFactory.supportesTerminal() */) {
			createTerminalOption(comp, 1);
		}
		LaunchUIPlugin.setDialogShell(parent.getShell());
	}

	/**
	 * Defines the area of the tab where the project is selected
	 */
	@Override
	protected void createProjectGroup(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText("&Project:"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);

		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				int bDex = buildConfCombo.getSelectionIndex();
				String bString = buildConfCombo.getText();
				initConfCombo();
				if (bDex >= 0 && buildConfCombo.getItemCount() > bDex && buildConfCombo.getItem(bDex).equals(bString))
					buildConfCombo.select(bDex);
				else if (buildConfCombo.getItemCount() > 0)
					buildConfCombo.select(0);
				updateLaunchConfigurationDialog();
			}
		});

		fProjButton = createPushButton(projComp, LaunchMessages.Launch_common_Browse_1,null);//.getString("Launch.common.Browse_1"), null); //$NON-NLS-1$
		fProjButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
				int bDex = buildConfCombo.getSelectionIndex();
				String bString = buildConfCombo.getText();
				initConfCombo();
				if (bDex >= 0 && buildConfCombo.getItemCount() > bDex && buildConfCombo.getItem(bDex).equals(bString))
					buildConfCombo.select(bDex);
				else if (buildConfCombo.getItemCount() > 0)
					buildConfCombo.select(0);
				updateLaunchConfigurationDialog();
			}
		});
	}

	@Override
	protected void createExeFileGroup(Composite parent, int colSpan) {
		exeComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		exeComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		exeComp.setLayoutData(gd);
		fProgLabel = new Label(exeComp, SWT.NONE);
		fProgLabel.setText("C/C++/Fortran Application"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fProgLabel.setLayoutData(gd);
		fProgText = new Text(exeComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProgText.setLayoutData(gd);
		fProgText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fSearchButton = createPushButton(exeComp, "Searc&h Project...", null); //$NON-NLS-1$
		fSearchButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		Button fBrowseForBinaryButton;
		fBrowseForBinaryButton = createPushButton(exeComp, "B&rowse...", null); //$NON-NLS-1$
		fBrowseForBinaryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleBinaryBrowseButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * Defines the area of the tab where the project's build configuration is
	 * selected
	 */
	protected void createBuildConfGroup(Composite parent, int colSpan) {
		buildConfComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		buildConfComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		buildConfComp.setLayoutData(gd);
		fProgLabel = new Label(buildConfComp, SWT.NONE);
		fProgLabel.setText(Messages.ToolRecompMainTab_LangBuildConf);
		gd = new GridData();
		gd.horizontalSpan = 3;
		fProgLabel.setLayoutData(gd);
		buildConfCombo = new Combo(buildConfComp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		buildConfCombo.setLayoutData(gd);
		buildConfCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * Initializes the configuration-selection combo box. Prompts the user for
	 * the project if none is already selected. Otherwise adds all of the build
	 * configurations in the selected project to the configuration-selection
	 * combo-box
	 */
	protected void initConfCombo() {
		buildConfCombo.removeAll();
		ICProject project = getCProject();
		if (project == null) {
			MessageDialog.openInformation(getShell(), LaunchMessages.CMainTab_Project_required,//.getString("CMainTab.Project_required"), //$NON-NLS-1$
					LaunchMessages.CMainTab_Enter_project_before_searching_for_program);//.getString("CMainTab.Enter_project_before_searching_for_program")); //$NON-NLS-1$
			return;
		}

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project.getResource());
		if (info == null) {
			MessageDialog.openInformation(getShell(), LaunchMessages.CMainTab_Project_required,//.getString("CMainTab.Project_required"), //$NON-NLS-1$
					LaunchMessages.CMainTab_Enter_project_before_searching_for_program);//.getString("CMainTab.Enter_project_before_searching_for_program")); //$NON-NLS-1$
			return;
		}

		IManagedProject imp = info.getManagedProject();
		if(imp==null)
			return;
		
		IConfiguration[] confs = imp.getConfigurations();

		for (int i = 0; i < confs.length; i++) {
			buildConfCombo.add(confs[i].getName());
		}

	}

	/**
	 * Confirm that all values needed to proceed with the program launch are
	 * present and valid
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {

		setErrorMessage(null);
		setMessage(null);

		String name = fProjText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage(LaunchMessages.CMainTab_Project_not_specified);//.getString("CMainTab.Project_not_specified")); //$NON-NLS-1$
			return false;
		}
		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
			setErrorMessage(LaunchMessages.Launch_common_Project_does_not_exist);//.getString("Launch.common.Project_does_not_exist")); //$NON-NLS-1$
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (!project.isOpen()) {
			setErrorMessage(LaunchMessages.CMainTab_Project_must_be_opened);//.getString("CMainTab.Project_must_be_opened")); //$NON-NLS-1$
			return false;
		}

		boolean reVal = false;
		try {
			if (config.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_RECOMPILE, false)) {
				name = buildConfCombo.getText();
				if (name == null || name.length() == 0) {
					setErrorMessage(Messages.ToolRecompMainTab_BuildConfNoSpeced);
					return false;
				}

				String bcdne = Messages.ToolRecompMainTab_BuildConfNoExist;

				if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
					setErrorMessage(bcdne);
					return false;
				}

				ICProject thisProject = getCProject();
				if (thisProject == null) {
					setErrorMessage(LaunchMessages.Launch_common_Project_does_not_exist);//.getString("Launch.common.Project_does_not_exist")); //$NON-NLS-1$
					return false;
				}

				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(thisProject.getResource());
				if (info == null) {
					setErrorMessage(Messages.ToolRecompMainTab_ProjNoValidBuildInfo);
					return false;
				}

				IConfiguration[] configs = info.getManagedProject().getConfigurations();

				if (configs.length < 1) {
					setErrorMessage(Messages.ToolRecompMainTab_NoValidConfs);
					return false;
				}

				for (int i = 0; i < configs.length; i++) {
					if (configs[i].getName().equals(buildConfCombo.getText()))
						reVal = true;
				}
				if (!reVal) {
					setErrorMessage(bcdne);
					return false;
				}
			} else
			// if(config.getAttribute(IToolLaunchConfigurationConstants.USE_EXEC_UTIL,
			// false)&&!config.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_RECOMPILE,
			// false))
			{
				name = fProgText.getText().trim();
				if (name.length() == 0) {
					setErrorMessage("Program not specified"); //$NON-NLS-1$
					return false;
				}
				if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
					setErrorMessage("Program does not exist"); //$NON-NLS-1$
					return false;
				}
				IPath exePath = new Path(name);
				if (!exePath.isAbsolute()) {
					if (!project.getFile(name).exists()) {
						setErrorMessage("Program does not exist"); //$NON-NLS-1$
						return false;
					}
					exePath = new Path(project.getFile(name).getLocationURI().getPath());
				} else {
					if (!exePath.toFile().exists()) {
						setErrorMessage("Program does not exist"); //$NON-NLS-1$
						return false;
					}
				}
//				try {
//					if (!isBinary(project, exePath)) {
//						setErrorMessage("Program is not a recognized executable."); //$NON-NLS-1$
//						return false;
//					}
//				} catch (CoreException e) {
//					LaunchUIPlugin.log(e);
//					setErrorMessage(e.getLocalizedMessage());
//					return false;
//				}
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Load the last known selected project from this launch configuration If
	 * the currently selected build configuration is not the same as the one
	 * loaded, reinitialize the configuration-selection combo-box
	 */
	@Override
	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String curProj = fProjText.getText();
		String projectName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		fProjText.setText(projectName);
		if (!curProj.equals(projectName))
			initConfCombo();
	}

	/**
	 * Initializes the program-selection combo-box with the selection previously
	 * saved in this configuration or with the first selection in the list if
	 * none is saved
	 */
	@Override
	protected void updateProgramFromConfig(ILaunchConfiguration config) {
		String programName = EMPTY_STRING;

		try {
			// TODO: If both usage types are false we go with TAU (recompile
			// only). Find a better way to check this.
			// boolean
			// useExecUtil=config.getAttribute(IToolLaunchConfigurationConstants.USE_EXEC_UTIL,
			// false);
			boolean perfRecompile = config.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_RECOMPILE, false);

			if (perfRecompile)// ||(!perfRecompile&&!useExecUtil))
			{
				buildConfComp.setEnabled(true);
				buildConfCombo.setEnabled(true);
				try {
					programName = config.getAttribute(IToolLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME,
							EMPTY_STRING);
				} catch (CoreException ce) {
					LaunchUIPlugin.log(ce);
				}
				if (!programName.equals(EMPTY_STRING))
					buildConfCombo.select(buildConfCombo.indexOf(programName));
				else
					buildConfCombo.select(0);

				exeComp.setEnabled(false);
				fProgText.setEnabled(false);
			} else {
				exeComp.setEnabled(true);
				fProgText.setEnabled(true);
				try {
					programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
				} catch (CoreException ce) {
					LaunchUIPlugin.log(ce);
				}
				fProgText.setText(programName);
				buildConfComp.setEnabled(false);
				buildConfCombo.setEnabled(false);

			}

			// if(useExecUtil&&!perfRecompile)
			// {
			// exeComp.setEnabled(true);
			// fProgText.setEnabled(true);
			// try {
			// programName =
			// config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
			// EMPTY_STRING);
			// } catch (CoreException ce) {
			// LaunchUIPlugin.log(ce);
			// }
			// fProgText.setText(programName);
			// }
			// else
			// {
			// exeComp.setEnabled(false);
			// fProgText.setEnabled(false);
			// }
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Applies the selected options
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, fProgText.getText());
		config.setAttribute(IToolLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, buildConfCombo.getText());
		if (fTerminalButton != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, fTerminalButton.getSelection());
		}
	}

}
