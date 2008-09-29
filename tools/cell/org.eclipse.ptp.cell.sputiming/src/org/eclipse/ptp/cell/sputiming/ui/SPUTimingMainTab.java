/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.sputiming.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.cell.preferences.ui.PreferenceConstants;
import org.eclipse.ptp.cell.sputiming.core.LaunchConfigurationConstants;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;


public class SPUTimingMainTab extends AbstractLaunchConfigurationTab {
	// Message group widgets
	protected Label fMessageLabel;

	// Compiler group widgets
	protected Label fCompilerNameLabel, fCompilerFlagsLabel;

	protected Text fCompilerNameText, fCompilerFlagsText;

	protected Button fCompilerCommandSearch;

	// Profile group widgets
	protected Label fProfilerTargetLabel;

	protected Text fProfilerTargetText;

	protected Button fProfilerTargetSearch;

	protected Label fProfilerProjectLabel;

	protected Text fProfilerProjectText;

	protected Button fProfilerProjectSearch;

	// SPUTiming group widgets
	protected Label fSPUTimingExecutableLabel;

	protected Text fSPUTimingExecutableText;

	protected Label fSPUTimingSourceFileLabel;

	protected Text fSPUTimingAssemblyFileText;

	private Label fSPUTimingArchitectureLabel;

	private Combo fSPUTimingArchitectureCombo;

	// Listener object
	protected ModifyListener textModifyListener;

	public SPUTimingMainTab() {
		super();
		textModifyListener = new TextBoxListener();
	}

	/*
	 * Create visual components for this tab. These are: 3 textboxes (compiler
	 * name, options and source file to be profiled) and a button (creates a new
	 * window which displays all source files for the selected project).
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = 0;
		topLayout.verticalSpacing = 1;
		// topLayout.marginWidth = 2;
		comp.setLayout(topLayout);

		createMessageGroup(comp, 1);

		// createVerticalSpacer(comp, 1);
		createProfilerGroup(comp, 1);
		// createVerticalSpacer(comp, 1);
		createCompilationGroup(comp, 1);
		// createVerticalSpacer(comp, 1);
		createSPUTimingGroup(comp, 1);
	}

	/**
	 * Generates a group containing a simple message in a label
	 * 
	 * @param comp
	 * @param i
	 */
	protected void createMessageGroup(Composite parent, int colSpan) {
		// Composite projComp = new Composite(parent, SWT.NONE);
		// GridLayout projLayout = new GridLayout();
		// projLayout.numColumns = 2;
		// projLayout.verticalSpacing = 0;
		// projLayout.marginHeight = 0;
		// projLayout.marginWidth = 3;
		// projComp.setLayout(projLayout);
		// GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		// gd.horizontalSpan = colSpan;
		// projComp.setLayoutData(gd);
		//
		// fProfilerProjectLabel = new Label(projComp, SWT.WRAP);
		// fProfilerProjectLabel.setText(GUIMessages.SPUTimingMainTab_MessageGroup_);
		// gd = new GridData(GridData.FILL_HORIZONTAL);
		// gd.horizontalSpan = colSpan;
		// fProfilerProjectLabel.setLayoutData(gd);
	}

	/*
	 * Responsible for the Textbox and Button which selects the source file to
	 * be profiled /** Show a dialog that lists all main types
	 */
	// TODO: change strings and make it look for source files, not executable
	// ones.
	// TODO: check the utility of below functions. Class CElementLabelProvider
	// is probably virtual.
	protected void handleSearchSourceButtonAction() {
		if (getCProject() == null) {
			MessageDialog.openInformation(getShell(), "Project required!", //$NON-NLS-1$
					"Enter project before searching for source file"); //$NON-NLS-1$
			return;
		}

		IFile csource = selectCSource();
		if (csource == null)
			return;

		fProfilerTargetText.setText(csource.getProjectRelativePath()
				.toOSString());
	}

	void createProfilerGroup(Composite parent, int colSpan) {
		Group projComp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		projComp.setText(Messages.SPUTimingMainTab_ProjectGroup_GroupLabel);
		GridLayout projLayout = new GridLayout();
		projLayout.verticalSpacing = 0;
		projLayout.numColumns = 2;
		projLayout.marginTop = 0;
		projLayout.marginBottom = 1;
		projLayout.marginWidth = 3;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		fProfilerProjectLabel = new Label(projComp, SWT.NONE);
		fProfilerProjectLabel
				.setText(Messages.SPUTimingMainTab_ProjectGroup_ProjectLabel);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProfilerProjectLabel.setLayoutData(gd);

		fProfilerProjectText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProfilerProjectText.setLayoutData(gd);
		fProfilerProjectText.addModifyListener(textModifyListener);
		/*
		 * new ModifyListener() { public void modifyText(ModifyEvent event) {
		 * updateLaunchConfigurationDialog(); } });
		 */

		fProfilerProjectSearch = createPushButton(projComp,
				Messages.SPUTimingMainTab_ProjectGroup_SearchProjectButton,
				null);
		fProfilerProjectSearch.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleSearchProjectButtonAction();
				updateLaunchConfigurationDialog();
			}
		});

		fProfilerTargetLabel = new Label(projComp, SWT.NONE);
		fProfilerTargetLabel
				.setText(Messages.SPUTimingMainTab_ProjectGroup_SourceLabel);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProfilerTargetLabel.setLayoutData(gd);

		fProfilerTargetText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProfilerTargetText.setLayoutData(gd);
		fProfilerTargetText.addModifyListener(textModifyListener);
		/*
		 * new ModifyListener() { public void modifyText(ModifyEvent event) {
		 * updateLaunchConfigurationDialog(); } });
		 */

		fProfilerTargetSearch = createPushButton(projComp,
				Messages.SPUTimingMainTab_ProjectGroup_SearchTargetButton,
				null);
		fProfilerTargetSearch.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleSearchSourceButtonAction();
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 * 
	 * Also, select the default compiler and compiler options and fill the
	 * appropriate textbox.
	 */
	protected void handleSearchProjectButtonAction() {
		ICProject cproject = selectCProject();
		if (cproject == null) {
			return;
		}

		String projectName = cproject.getElementName();
		fProfilerProjectText.setText(projectName);

	}

	/**
	 * Realize a C Project selection dialog and return the first selected
	 * project, or null if there was none.
	 */
	protected ICProject selectCProject() {
		try {
			ICProject[] projects = getCProjects();

			// // TODO: check if CElementLabelProvider is necessary.
			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					getShell(), labelProvider);
			dialog.setTitle("Projects"); //$NON-NLS-1$
			dialog.setMessage("Select the project whose file will be profiled"); //$NON-NLS-1$
			dialog.setElements(projects);

			ICProject cProject = getCProject();
			if (cProject != null) {
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == Window.OK) {
				return (ICProject) dialog.getFirstResult();
			}
		} catch (CModelException e) {
			System.out.println("CModelException " + e.getMessage()); //$NON-NLS-1$
			// LaunchUIPlugin.errorDialog("Launch UI internal error", e);
			// //$NON-NLS-1$
		}
		return null;
	}

	/*
	 * Responsible for the two textbox which describes the compiler line to be
	 * called and its flags.
	 */
	protected void createCompilationGroup(Composite parent, int colSpan) {
		Group projComp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		projComp.setText(Messages.SPUTimingMainTab_CompilerGroup_GroupLabel);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginTop = 0;
		projLayout.marginBottom = 1;
		projLayout.marginWidth = 3;
		projLayout.verticalSpacing = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		fCompilerNameLabel = new Label(projComp, SWT.NONE);
		fCompilerNameLabel
				.setText(Messages.SPUTimingMainTab_CompilerGroup_CompilerLabel);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fCompilerNameLabel.setLayoutData(gd);

		fCompilerNameText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCompilerNameText.setLayoutData(gd);
		fCompilerNameText.addModifyListener(textModifyListener);
		/*
		 * new ModifyListener() { public void modifyText(ModifyEvent event) {
		 * updateLaunchConfigurationDialog(); } });
		 */

		/*
		 * fCompilerCommandSearch = createPushButton(projComp, "Extract
		 * Compiler", null); fCompilerCommandSearch.addSelectionListener(new
		 * SelectionAdapter() {
		 * 
		 * public void widgetSelected(SelectionEvent event) {
		 * handleSearchCCompilerButtonAction();
		 * updateLaunchConfigurationDialog(); }
		 * 
		 * });
		 */

		fCompilerFlagsLabel = new Label(projComp, SWT.NONE);
		fCompilerFlagsLabel
				.setText(Messages.SPUTimingMainTab_CompilerGroup_FlagsLabel);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fCompilerFlagsLabel.setLayoutData(gd);

		fCompilerFlagsText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCompilerFlagsText.setLayoutData(gd);
		fCompilerFlagsText.addModifyListener(textModifyListener);

	}

	protected void createSPUTimingGroup(Composite parent, int colSpan) {
		Group projComp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		projComp
				.setText(Messages.SPUTimingMainTab_SPUTimingGroup_GroupLabel);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginTop = 0;
		projLayout.marginBottom = 1;
		projLayout.marginWidth = 3;
		projLayout.verticalSpacing = 2;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		fSPUTimingExecutableLabel = new Label(projComp, SWT.NONE);
		fSPUTimingExecutableLabel
				.setText(Messages.SPUTimingMainTab_SPUTimingGroup_SPUTimingLabel);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fSPUTimingExecutableLabel.setLayoutData(gd);

		fSPUTimingExecutableText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSPUTimingExecutableText.setLayoutData(gd);
		fSPUTimingExecutableText.addModifyListener(textModifyListener);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		fSPUTimingExecutableText.setLayoutData(gd);

		fSPUTimingSourceFileLabel = new Label(projComp, SWT.NONE);
		fSPUTimingSourceFileLabel
				.setText(Messages.SPUTimingMainTab_SPUTimingGroup_AssemblyFileLabel);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fSPUTimingSourceFileLabel.setLayoutData(gd);

		fSPUTimingAssemblyFileText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSPUTimingAssemblyFileText.setLayoutData(gd);
		fSPUTimingAssemblyFileText.addModifyListener(textModifyListener);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		fSPUTimingAssemblyFileText.setLayoutData(gd);

		fSPUTimingArchitectureLabel = new Label(projComp, SWT.NONE);
		fSPUTimingArchitectureLabel
				.setText(Messages.SPUTimingMainTab_SPUTimingGroup_ArchitectureComboLabel);
		// gd = new GridData();
		// gd.horizontalSpan = 2;
		// fSPUTimingSourceFileLabel.setLayoutData(gd);

		fSPUTimingArchitectureCombo = new Combo(projComp, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		fSPUTimingArchitectureCombo
				.add(Messages.SPUTimingMainTab_SPUTimingGroup_ArchitectureCell);
		fSPUTimingArchitectureCombo
				.add(Messages.SPUTimingMainTab_SPUTimingGroup_ArchitectureSoma);
		fSPUTimingArchitectureCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
			
		});

	}

	/**
	 * Extract ICProject information from the ProjectText field
	 */
	protected ICProject getCProject() {
		String projectName = fProfilerProjectText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		ICProject cproj = CoreModel.getDefault().getCModel().getCProject(
				projectName);

		if (!cproj.exists())
			return null;

		return cproj;
	}

	/**
	 * Return an array of ICProjects.
	 * 
	 * Filter projects. Allowed only those who dont have Build information
	 * (standard make) and those who have build information and project type
	 * which are for the spu (executable and library).
	 */
	protected ICProject[] getCProjects() throws CModelException {
		ICProject cproject[] = CoreModel.getDefault().getCModel()
				.getCProjects();
		ArrayList list = new ArrayList(cproject.length);

		// 
		for (int i = 0; i < cproject.length; i++) {
			IProject project = cproject[i].getProject();

			if (ManagedBuildManager.canGetBuildInfo(project) == false)
				list.add(cproject[i]);
			/*
			 * else { IProjectType projecttype = ManagedBuildManager.
			 * getBuildInfo(project).getManagedProject().getProjectType();
			 * if(projecttype.getBaseId().contains("cell.managedbuild.target.cell.spu"))
			 * list.add(cproject[i]); }
			 */
		}
		return (ICProject[]) list.toArray(new ICProject[list.size()]);
	}

	/**
	 * Create a dialog containing all sources of the project. Return the
	 * selected one.
	 */
	protected IFile selectCSource() {

		IFile[] sourcefiles = getCSources();

		if (sourcefiles == null)
			return null;

		// Build dialog with a list of sources
		// TODO: dont use the CElementLabelProvider. Build another, if
		// necessary.
		ILabelProvider lblProvider = new CElementLabelProvider();
		ElementListSelectionDialog sourcedialog = new ElementListSelectionDialog(
				getShell(), lblProvider);
		sourcedialog
				.setTitle(Messages.SPUTimingMainTab_SelectCSource_DialogTitle);
		sourcedialog
				.setMessage(Messages.SPUTimingMainTab_SelectCSource_DialogMessage);
		sourcedialog.setElements(sourcefiles);

		// Select the first source
		if (sourcefiles != null)
			sourcedialog.setInitialSelections(new Object[] { sourcefiles[0] });

		if (sourcedialog.open() == Window.OK)
			return (IFile) sourcedialog.getFirstResult();

		return null;
	}

	/*
	 * Return the right set of tools from a managed project, considering project
	 * nature and configuration.
	 */
	/*
	 * protected ITool [] getCCompilerTools(IProject project) {
	 * 
	 * 
	 * if(ManagedBuildManager.canGetBuildInfo(project.getProject())) { String
	 * compilername, compilerflags;
	 *  // Is managed, so extract buildinfo. IManagedBuildInfo buildinfo =
	 * ManagedBuildManager. getBuildInfo(project.getProject());
	 * 
	 * String toolidmatching; // Checks for c or cpp nature and choose filter
	 * based on that // info. if(CoreModel.hasCNature(project.getProject()))
	 * toolidmatching = "c.compiler"; else
	 * if(CoreModel.hasCCNature(project.getProject())) toolidmatching =
	 * "cpp.compiler"; else // Not a valid C or C++ project. Get out! return
	 * null;
	 *  // Receives a collection of tools and filter those whose IDs have //
	 * compiler on its name. ITool [] tools;
	 * if(buildinfo.getSelectedConfiguration() != null) tools =
	 * buildinfo.getSelectedConfiguration().getTools();//ManagedBuildManager.
	 * else tools = buildinfo.getDefaultConfiguration().getTools();
	 * 
	 * return tools;
	 *  } else // Cannot extract build information. Return null. return null; }
	 */

	/*
	 * Extracts IFile information from the fileName field.
	 */
	protected IFile getCSource() {
		String fileName = fProfilerTargetText.getText().trim();
		if (fileName.length() < 1) {
			return null;
		}

		ICProject cproject = getCProject();

		if (cproject == null)
			return null;

		IFile cfile = cproject.getProject().getFile(fileName);

		if (!cfile.exists())
			return null;

		return cfile;
	}

	/*
	 * Return all C (or cpp) sources from a given (previously selected) project
	 */
	protected IFile[] getCSources() {
		// Receive Project info.
		ICProject cproj = getCProject();

		LinkedList filelist = new LinkedList();
		// Vector filelist = new Vector();
		// Get all resources from the project.
		IResource[] resourcelist;
		try {
			resourcelist = cproj.getProject().members();
		} catch (CoreException e1) {
			return null;
		}

		// Verifies if the the project is managed or not.
		if (ManagedBuildManager.canGetBuildInfo(cproj.getProject())) {
			// Is managed, so extract buildinfo.
			IManagedBuildInfo buildinfo = ManagedBuildManager
					.getBuildInfo(cproj.getProject());

			String[] toolidmatchinglist = new String[2];
			// Checks for c and cpp nature and choose filter based on that
			// info.
			if (CoreModel.hasCNature(cproj.getProject())) { // Keep the
															// cell.spu.xl.c.compiler
															// for matching
															// purposes
				toolidmatchinglist[0] = "cell.spu.xl.c.compiler"; //$NON-NLS-1$
				toolidmatchinglist[1] = "cell.spu.gnu.c.compiler"; //$NON-NLS-1$

				if (CoreModel.hasCCNature(cproj.getProject())) {
					// C++ project has both natures active
					toolidmatchinglist[0] = "cell.spu.xl.cpp.compiler"; //$NON-NLS-1$
					toolidmatchinglist[1] = "cell.spu.gnu.cpp.compiler"; //$NON-NLS-1$
				}
			} else
				// Not a valid C or C++ project. Get out!
				return null;

			// Receives a collection of tools and filter those whose IDs have
			// compiler on its name.
			// Use that information to filter the input extensions that will
			// be used
			ITool[] tools;
			tools = buildinfo.getDefaultConfiguration().getFilteredTools();

			HashSet extensionset = new HashSet();

			for (int j = 0; j < tools.length; j++) {
				// ITool tool = (ITool)it.next();
				ITool tool = tools[j];

				if ((tool.getId().indexOf(toolidmatchinglist[0]) != -1)
						|| (tool.getId().indexOf(toolidmatchinglist[1]) != -1)) {
					String[] extensions = tool.getAllInputExtensions();

					for (int i = 0; i < extensions.length; i++)
						extensionset.add(extensions[i]);
				}
			}

			for (int i = 0; i < resourcelist.length; i++) {
				if (resourcelist[i] instanceof IFile) {
					IFile tempfile = (IFile) resourcelist[i];

					if (extensionset.contains(tempfile.getFileExtension()))
						filelist.add(tempfile);
				}
			}
		} else {
			// Dont have any information about supported file extensions.
			// Just show all files.
			try {
				filelist.addAll(collectSourceFiles(resourcelist));
			} catch (CoreException e) {
				System.out
						.println(Messages.SPUTimingMainTab_GetCSources_SourceListError);
			}

			/*
			 * for(int i=0; i < resourcelist.length; i++) { if((resourcelist[i]
			 * instanceof IFile) && (resourcelist[i].getFileExtension() !=
			 * null)){
			 * if(resourcelist[i].getFileExtension().equalsIgnoreCase("c") ||
			 * resourcelist[i].getFileExtension().equalsIgnoreCase("c++") ||
			 * resourcelist[i].getFileExtension().equalsIgnoreCase("cc") ||
			 * resourcelist[i].getFileExtension().equalsIgnoreCase("cxx") ||
			 * resourcelist[i].getFileExtension().equalsIgnoreCase("cpp"))
			 * filelist.add((IFile)resourcelist[i]); }
			 *  }
			 */
		}
		/*
		 * IFile [] temp = new IFile[filelist.size()]; for(int i=0; i <
		 * temp.length; i++) temp[i ]= (IFile)filelist.get(i); return temp;
		 */
		IFile[] temp = new IFile[1];
		// Object [] objlist = filelist.toArray(temp);
		return (IFile[]) filelist.toArray(temp);// (IFile [])objlist;
	}

	/**
	 * Loop over all IFile resources of a given resourcelist. If the resource is
	 * of the IFile kind, add it to the a list. If the resource is of the
	 * IDirectory kind, enter it and execute the algorithm again.
	 * 
	 * @return IFile [] The list of IFile kind resources.
	 * @throws CoreException
	 */
	protected LinkedList collectSourceFiles(IResource[] resourcelist)
			throws CoreException {
		LinkedList filelist = new LinkedList();

		for (int i = 0; i < resourcelist.length; i++) {
			if ((resourcelist[i] instanceof IFile)
					&& (resourcelist[i].getFileExtension() != null)
					&& isSourceFile((IFile) resourcelist[i])) {
				filelist.add((IFile) resourcelist[i]);
			} else if (resourcelist[i] instanceof IFolder) {
				IFolder tempfolder = (IFolder) resourcelist[i];
				IResource[] sublist = tempfolder.members();
				filelist.addAll(collectSourceFiles(sublist));
			}
		}
		return filelist;
	}

	/**
	 * Validate the source file
	 * 
	 * @return Boolean The IFile is a valid source file or not
	 */
	protected boolean isSourceFile(IFile file) {
		if (file.getFileExtension().equalsIgnoreCase("c") //$NON-NLS-1$
				|| file.getFileExtension().equalsIgnoreCase("c++") //$NON-NLS-1$
				|| file.getFileExtension().equalsIgnoreCase("cc") //$NON-NLS-1$
				|| file.getFileExtension().equalsIgnoreCase("cxx") //$NON-NLS-1$
				|| file.getFileExtension().equalsIgnoreCase("cpp")) //$NON-NLS-1$
			return true;

		return false;
	}

	/**
	 * Return the compiler used by the resource (if it exists), or the compiler
	 * associated to the source file extension.
	 * 
	 * @return ITool
	 */
	protected ITool getCCompiler() {
		IProject project = getCProject().getProject();
		IFile csource = getCSource();

		// Only return tools if managed info is available.
		if (ManagedBuildManager.canGetBuildInfo(project)) {
			IManagedBuildInfo buildinfo = ManagedBuildManager
					.getBuildInfo(project);

			IResourceConfiguration resconf = buildinfo
					.getDefaultConfiguration().getResourceConfiguration(
							csource.getFullPath().toOSString());

			// Prefer the tool connected to the resourceConfiguration
			// to the associated with the source file extension.
			if (resconf == null)
				return buildinfo.getToolFromInputExtension(csource
						.getFileExtension());

			return resconf.getTools()[0];
		} else
			return null;
	}

	/**
	 * Handles the pressing of the Search Compiler button
	 * 
	 * @author Richard Maciel
	 * @since 1.0
	 */
	protected void handleSearchCCompilerButtonAction() {
		if (getCProject() == null) {
			MessageDialog.openInformation(getShell(), "Project required!", //$NON-NLS-1$
					"Enter project before searching for compiler information"); //$NON-NLS-1$
			return;
		}

		IFile source = getCSource();
		if (source == null) {
			MessageDialog
					.openInformation(getShell(), "Source file required!", //$NON-NLS-1$
							"Enter source file before searching for compiler information"); //$NON-NLS-1$
			return;
		}

		ITool tool = getCCompiler();
		if (tool == null) {
			MessageDialog
					.openInformation(getShell(), "No managed build info!", //$NON-NLS-1$
							"Cannot extract compiler information from a non-managed build project"); //$NON-NLS-1$
			return;
		}

		// Put command tool on the user text box.
		String toolcommand = tool.getToolCommand();
		if (toolcommand == null)
			fCompilerNameText.setText(""); //$NON-NLS-1$
		else
			fCompilerNameText.setText(toolcommand);
		String flaglist = null;
		try {

			// Get an extension used by the assembler tool.
			ITool assemblertool = ManagedBuildManager
					.getExtensionTool("cell.managedbuild.tool.cell.spu.gnu.assembler"); //$NON-NLS-1$

			if (assemblertool == null) {
				MessageDialog
						.openInformation(
								getShell(),
								Messages.SPUTimingMainTab_SearchCCompilerButtonAction_CDTErrorTitle,
								Messages.SPUTimingMainTab_SearchCCompilerButtonAction_CDTErrorMessage);
				return;
			}

			// Get the first extension available as input extension.
			String[] assemblerextlist = assemblertool.getAllInputExtensions();

			if (assemblerextlist.length == 0) {
				MessageDialog
						.openInformation(
								getShell(),
								Messages.SPUTimingMainTab_SearchCCompilerButtonAction_CDTErrorTitle,
								Messages.SPUTimingMainTab_SearchCCompilerButtonAction_CDTErrorMessage);
				return;
			}

			// Generate the output IPath from the input
			IManagedBuildInfo imbi = ManagedBuildManager
					.getBuildInfo(getCProject().getProject());
			String builddir = imbi.getConfigurationName();
			IPath outputfilenamewoext = source.getFullPath()
					.removeFileExtension().makeRelative()
					.removeFirstSegments(1);
			String outputextension = imbi.getOutputExtension(source
					.getFileExtension());
			IPath outputfilename = source.getLocation().removeLastSegments(1)
					.addTrailingSeparator().append(builddir)
					.addTrailingSeparator().append(
							outputfilenamewoext.toString()
									+ "." + outputextension); //$NON-NLS-1$

			// Get and alter the command flag.
			flaglist = tool.getToolCommandFlagsString(source.getLocation(),
					outputfilename)
					+ " -S"; //$NON-NLS-1$
		} catch (BuildException e) {
			System.out
					.println(Messages.SPUTimingMainTab_SearchCCompilerButtonAction_FlagsFetchingMessage);
			fCompilerFlagsText.setText(""); //$NON-NLS-1$
		}

		// Set the flag text field.
		if (flaglist != null)
			fCompilerFlagsText.setText(flaglist);

		// Extract sputiming name too.
		// String SPUexec = getSPUTimingExecutableName();
		// if(SPUexec != null)
		// fSPUTimingExecutableText.setText(SPUexec);
	}

	/**
	 * Extracts the SPUTiming executable name from the compiler ID info.
	 * 
	 * @return Name of the SPUTiming executable
	 * @author Richard Maciel
	 * @since 1.0
	 */
	protected String getSPUTimingExecutableName() {
		ITool ccompiler = getCCompiler();

		if (ccompiler != null) {
			/*
			 * Commented code below doesnt work anymore on the SDK 1.1. Cause:
			 * convergence of sputiming executable?
			 */
			/*
			 * if(ccompiler.getId().contains("xlc")) return "spuxlctiming"; else
			 * if(ccompiler.getId().contains("gnu")) return "spu-gcc_timing";
			 */
			return "spu_timing"; //$NON-NLS-1$
		}

		return null;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 * @author Richard Maciel
	 * @since 1.0
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		// setMessage(null);
		setMessage(Messages.SPUTimingMainTab_MessageGroup_);

		// Project
		String projName = fProfilerProjectText.getText().trim();
		if (projName.length() == 0) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_NoProjectNameError);
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
		if (! project.exists()) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_ProjectMustExistError);
			return false;
		}
		if (!project.isOpen()) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_ProjectMustBeOpened);
			return false;
		}

		// Source
		String sourceName = fProfilerTargetText.getText().trim();
		if (sourceName.length() == 0) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_NoSourceFileError);
			return false;
		}
		if (!project.getFile(sourceName).exists()) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_SourceFileMustExistError);
			return false;
		}
		if (sourceName.equals(".") || sourceName.equals("..")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_InvalidFilenameError);
			return false;
		}

		// Compiler
		String compCmd = fCompilerNameText.getText().trim();
		if (compCmd.length() == 0) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_NoCompilerError);
			return false;
		}

		// Compiler flags
		if (fCompilerFlagsText.getText().trim().length() == 0) {
			setMessage(Messages.SPUTimingMainTab_IsValid_AssemblyFlagMessage
					+ "flag to generate output file name " + "compatible with the assembly file name field"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// SPUTiming command
		String sputCommand = fSPUTimingExecutableText.getText().trim();
		File sputCmdFile = new File(sputCommand);
		if (!sputCmdFile.exists() || !sputCmdFile.isFile()) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_ValidSPUTimingPathError);
			return false;
		}

		// Assembly file name
		String asmFile = fSPUTimingAssemblyFileText.getText().trim();
		// Path asmFilePath = new Path();
		if (asmFile.length() == 0) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_ValidAssemblyFilenameError);
			return false;
		}

		int index = fSPUTimingArchitectureCombo.getSelectionIndex();
		if (index == -1) {
			setErrorMessage(Messages.SPUTimingMainTab_IsValid_ValidArchitectureError);
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 * 
	 * Receive information from the configuration and set the text fields.
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		// Only get compiler information if the project is a managed builder
		// one.

		try {
			fCompilerNameText.setText(configuration.getAttribute(
					LaunchConfigurationConstants.COMPILER_NAME, "")); //$NON-NLS-1$
			fCompilerFlagsText.setText(configuration.getAttribute(
					LaunchConfigurationConstants.COMPILER_FLAGS, "")); //$NON-NLS-1$
			fProfilerTargetText.setText(configuration.getAttribute(
					LaunchConfigurationConstants.SOURCE_FILE_NAME, "")); //$NON-NLS-1$
			fProfilerProjectText.setText(configuration.getAttribute(
					LaunchConfigurationConstants.PROJECT_NAME, "")); //$NON-NLS-1$
			fSPUTimingAssemblyFileText.setText(configuration.getAttribute(
					LaunchConfigurationConstants.ASSEMBLY_FILE_NAME, "")); //$NON-NLS-1$

			if (configuration.getAttribute(
					LaunchConfigurationConstants.SPU_EXECUTABLE_NAME, "") == null || //$NON-NLS-1$
					configuration.getAttribute(
							LaunchConfigurationConstants.SPU_EXECUTABLE_NAME,
							"") == "") //$NON-NLS-1$ //$NON-NLS-2$
			{
				PreferenceConstants preferences = PreferenceConstants
						.getInstance();

				fSPUTimingExecutableText.setText(preferences.getTIMING_SPUBIN()
						.toOSString());
			} else {
				fSPUTimingExecutableText.setText(configuration.getAttribute(
						LaunchConfigurationConstants.SPU_EXECUTABLE_NAME, "")); //$NON-NLS-1$
			}
			
			if (configuration.getAttribute(LaunchConfigurationConstants.SPU_ARCH_TYPE, LaunchConfigurationConstants.SPU_ARCH_SPU).equals(LaunchConfigurationConstants.SPU_ARCH_SPU)) {
				fSPUTimingArchitectureCombo.select(0);
			} else if (configuration.getAttribute(LaunchConfigurationConstants.SPU_ARCH_TYPE, LaunchConfigurationConstants.SPU_ARCH_SPU).equals(LaunchConfigurationConstants.SPU_ARCH_SPUEFP)) {
				fSPUTimingArchitectureCombo.select(1);
			} else {
				fSPUTimingArchitectureCombo.select(0);
			}
			
		} catch (CoreException e) {
			// TODO: handle exception properly
		}

	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		PreferenceConstants preferences = PreferenceConstants.getInstance();

		configuration.setAttribute(
				LaunchConfigurationConstants.SPU_EXECUTABLE_NAME, preferences
						.getTIMING_SPUBIN().toOSString());
		configuration.setAttribute(LaunchConfigurationConstants.SPU_ARCH_TYPE, LaunchConfigurationConstants.SPU_ARCH_SPU);
		// fSPUTimingExecutableText.setText(
		// );
		// configuration.setAttribute(LaunchConfigurationConstants.DEFAULT_WORKING_DIR,

		/*
		 * configuration.setAttribute(LaunchConfigurationConstants.COMPILER_NAME,
		 * "gcc");
		 * configuration.setAttribute(LaunchConfigurationConstants.COMPILER_FLAGS,
		 * "-S");
		 * configuration.setAttribute(LaunchConfigurationConstants.PROJECT_NAME,
		 * "");
		 * configuration.setAttribute(LaunchConfigurationConstants.FILE_NAME,
		 * "");
		 */
	}

	// Receive information from the text fields and set configuration
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationConstants.COMPILER_NAME,
				fCompilerNameText.getText());
		configuration.setAttribute(LaunchConfigurationConstants.COMPILER_FLAGS,
				fCompilerFlagsText.getText());
		configuration.setAttribute(LaunchConfigurationConstants.PROJECT_NAME,
				fProfilerProjectText.getText());
		configuration.setAttribute(
				LaunchConfigurationConstants.SOURCE_FILE_NAME,
				fProfilerTargetText.getText());
		configuration.setAttribute(
				LaunchConfigurationConstants.SPU_EXECUTABLE_NAME,
				fSPUTimingExecutableText.getText());
		configuration.setAttribute(
				LaunchConfigurationConstants.ASSEMBLY_FILE_NAME,
				fSPUTimingAssemblyFileText.getText());

		// Also updates working directory info.
		IFile file = getCSource();

		if (file != null) {
			configuration.setAttribute(
					LaunchConfigurationConstants.PROJECT_DIR, getCProject()
							.getProject().getLocation().toOSString());

			// file.getParent().getLocation().toString());
			// TODO: Assembler output file is hardcoded on .s - Must find a way
			// to change that.
			/*
			 * String sourcefilename = fProfilerTargetText.getText(); String
			 * extension = file.getFileExtension(); String filenamewoext =
			 * sourcefilename. substring(0, sourcefilename.length() -
			 * extension.length()); String assemblyfilename =
			 * filenamewoext.concat("s");
			 * configuration.setAttribute(LaunchConfigurationConstants.ASSEMBLY_FILE_NAME,
			 * assemblyfilename);
			 */
		} else {
			configuration.setAttribute(
					LaunchConfigurationConstants.PROJECT_DIR, (String) null);
		}
		
		if (fSPUTimingArchitectureCombo.getSelectionIndex() == 0) {
			configuration.setAttribute(LaunchConfigurationConstants.SPU_ARCH_TYPE, LaunchConfigurationConstants.SPU_ARCH_SPU);
		} else if (fSPUTimingArchitectureCombo.getSelectionIndex() == 1) {
			configuration.setAttribute(LaunchConfigurationConstants.SPU_ARCH_TYPE, LaunchConfigurationConstants.SPU_ARCH_SPUEFP);
		}
	}

	public String getName() {
		return Messages.SPUTimingMainTab_Title;
	}

	public class TextBoxListener implements ModifyListener {
		public void modifyText(ModifyEvent event) {
			updateLaunchConfigurationDialog();
		}
	}

	/*
	 * new ModifyListener() { public void modifyText(ModifyEvent event) {
	 * updateLaunchConfigurationDialog(); } });
	 */

}
