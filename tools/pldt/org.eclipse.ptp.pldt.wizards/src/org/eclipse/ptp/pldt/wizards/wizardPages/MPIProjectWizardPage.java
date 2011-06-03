/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.core.prefs.MPIPreferencePage;
import org.eclipse.ptp.pldt.mpi.internal.core.MpiIDs;
import org.eclipse.ptp.pldt.wizards.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard Page for collecting info about MPI project - appended to end of
 * "C project" or "C++ project" wizard.
 * 
 * Abstract class encapsulates common behavior between C and C++
 * <p>
 * TODO remove dup code, share MPI&OpenMP version in a common class etc.
 * 
 * @author Beth Tibbitts
 * 
 */
public abstract class MPIProjectWizardPage extends AbstractProjectWizardPage {
	public static final String DOT = "."; //$NON-NLS-1$
	private static final boolean traceOn = false;
	public static final boolean wizardTraceOn = false;

	/**
	 * make this modifiable in preferences later?
	 */
	private boolean DONT_REMIND_MPI_INCLUDE_PATH = false;
	/**
	 * if move to preferences, must move this up to mpi.core for example.
	 */
	private static String DONT_REMIND_MPI_INCLUDE_PATH_KEY = "dontRemindMPIincludePath"; //$NON-NLS-1$

	private Composite composite;
	public static final String PAGE_ID = "org.eclipse.ptp.pldt.wizards.wizardPages.MPIProjectWizardPage"; //$NON-NLS-1$

	// The following are IDs for storing info in MBSPageData so it can be retrieved in MpiProjectProcess (ProcessRunner)
	// when the wizard is done.
	/**
	 * Store in MBSPageData (with this ID) whether user wants to include MPI info in the project.<br>
	 * Note this is a slight misnomer should be named "includeMPIprojectInfo" or something,<br>
	 * because it includes more than just include path info
	 */
	public static final String DO_MPI_INCLUDES = "doMPIincludes"; //$NON-NLS-1$
	/**
	 * store in MBSPageData (with this ID) what the include path to MPI will be.
	 */
	public static final String INCLUDE_PATH_PROP_ID = "includePath"; //$NON-NLS-1$
	/**
	 * store in MBSPageData (with this ID) what the library name for the linker is.
	 */
	public static final String LIB_PROP_ID = "lib"; //$NON-NLS-1$
	/**
	 * store in MBSPageData (with this ID) what the library search path is.
	 */
	public static final String LIBRARY_SEARCH_PATH_PROP_ID = "libPath"; //$NON-NLS-1$

	public static final String MPI_COMPILE_COMMAND_PROP_ID = "mpiCompileCommand"; //$NON-NLS-1$
	public static final String MPI_LINK_COMMAND_PROP_ID = "mpiLinkCommand"; //$NON-NLS-1$

	private String currentMpiIncludePath;
	private String currentLibName;
	private String currentLibPath;
	private String currentMpiCompileCommand;
	private String currentMpiLinkCommand;

	private String defaultMpiIncludePath;
	private String defaultMpiLibName;
	private String defaultMpiLibPath;
	private String defaultMpiBuildCommand;

	private Text includePathField;
	private Text libNameField;
	private Text libPathField;
	private Text mpiCompileCommandField, mpiLinkCommandField;

	private Label includePathLabel, libLabel, libPathLabel, mpiCompileCommandLabel, mpiLinkCommandLabel;

	private Button browseButton;
	private Button browseButton2;

	private Button useDefaultsButton;
	private Button useMpiProjectSettingsButton;
	private static boolean defaultUseMpiIncludes = true;

	private Button mpiSampleButton;

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	/**
	 * By default we DO use MPI project settings in this project.<br>
	 */
	private boolean useMpiProjectSettings = true;
	private String desc = Messages.MPIProjectWizardPage_mpi_project_page;

	public static final String MPI_PROJECT_TYPE_C = "C"; //$NON-NLS-1$
	public static final String MPI_PROJECT_TYPE_CPP = "C++"; //$NON-NLS-1$

	/**
	 * The CDT new project wizard page for MPI projects.
	 * Adds the include paths, library information, etc. for an MPI project.
	 * 
	 * @throws CoreException
	 * 
	 */
	public MPIProjectWizardPage() throws CoreException {
		super(Messages.MPIProjectWizardPage_mpi_project_settings);
		prefIDincludes = MpiIDs.MPI_INCLUDES;
		if (wizardTraceOn)
			System.out.println("MPIProjectWizardPage().ctor..."); //$NON-NLS-1$

		// CommonPlugin.log(IStatus.ERROR,"Test error");
		// CommonPlugin.log(IStatus.WARNING,"Test warning");

		// access the preference store from the MPI plugin
		preferenceStore = MpiPlugin.getDefault().getPreferenceStore();
		boolean allowPrefixOnlyMatch = preferenceStore.getBoolean(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		String mip = preferenceStore.getString(prefIDincludes);
		if (traceOn)
			System.out.println("Got mpi include pref from other plugin: " + mip); //$NON-NLS-1$

		// Set the defaults here in the wizard page constructor and just
		// overwrite them if the user changes them.
		defaultMpiIncludePath = mip;
		// We only need to force MPI prefs to be set now if a prefix-only match isn't allowed.
		// Note that if the user doesn't set include path, we can't make a guess at what the lib path etc is.
		if (traceOn)
			System.out.println("MPWP: allowPrefixOnlyMatch=" + allowPrefixOnlyMatch); //$NON-NLS-1$
		if (!allowPrefixOnlyMatch && defaultMpiIncludePath.length() == 0) {
			// warn if no MPI preferences have been set and allow user to set them right there
			String newMip = showNoPrefs(Messages.MPIProjectWizardPage_mpi, prefIDincludes);
			defaultMpiIncludePath = newMip;
		}
		/*
		 * else {
		 * DONT_REMIND_MPI_INCLUDE_PATH = preferenceStore.getBoolean(DONT_REMIND_MPI_INCLUDE_PATH_KEY);
		 * if (!DONT_REMIND_MPI_INCLUDE_PATH) {
		 * // fixme externalize message strings
		 * String title = "MPI include path";
		 * StringBuffer msgBuf = new StringBuffer("If your project shows errors and can't recognize MPI symbols, ");
		 * msgBuf.append("make sure your MPI header file is in the include path for the project. ");
		 * msgBuf.append(" Since mpicc may do that for you for the build, Eclipse may not know about it.  ");
		 * msgBuf.append("You may want to add it to Project Properties, C/C++ General, Paths and Symbols, ");
		 * msgBuf.append(" so that the editor and indexer can find MPI symbols.");
		 * String msg = msgBuf.toString();
		 * String toggleMsg = "Don't remind me about this again";
		 * final String key = DONT_REMIND_MPI_INCLUDE_PATH_KEY;
		 * final boolean val = DONT_REMIND_MPI_INCLUDE_PATH;
		 * MessageDialogWithToggle.openInformation(null, title, msg, toggleMsg, val, preferenceStore, key);
		 * System.out.println("DONT_REMIND include path is: " + preferenceStore.getBoolean(DONT_REMIND_MPI_INCLUDE_PATH_KEY));
		 * }
		 * }
		 */
		setDefaultOtherNames(defaultMpiIncludePath);
		// the following sets what will be remembered when we leave the page.
		setCurrentMpiIncludePath(defaultMpiIncludePath);

		// defaultMpiBuildCommand depends on project type (will be different for C vs C++ for example)
		defaultMpiBuildCommand = getDefaultMpiBuildCommand();
		setCurrentMpiCompileCommand(defaultMpiBuildCommand);
		setCurrentMpiLinkCommand(defaultMpiBuildCommand);
	}

	abstract protected String getDefaultMpiBuildCommand();

	private static boolean alreadyShown;

	/**
	 * Warn user that the MPI project preferences aren't set, and thus the new project wizard will not be very useful. <br>
	 */
	@SuppressWarnings("unused")
	private static void showNoPrefs1() {
		if (!alreadyShown) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			StringBuffer buf = new StringBuffer(Messages.MPIProjectWizardPage_no_mpi_preferences_set);
			buf.append(Messages.MPIProjectWizardPage_default_project_setting_better_if_prefs_set);
			buf.append(Messages.MPIProjectWizardPage_use_window_prefs_and_select_pldt);
			buf.append(Messages.MPIProjectWizardPage_you_can_cancel_out_to_do_mpi_prefs);
			MessageDialog.openWarning(shell, Messages.MPIProjectWizardPage_no_mpi_prefs_set, buf.toString());
			alreadyShown = true;
		}
	}

	/**
	 * Set the default lib name and lib path based on what the
	 * include path is. We assume something like <code>mpiIncludepath=/my/path/include</code> in which case we set
	 * <code>libName="lib" </code>and <code> defaultMpiLibPath=/my/path/lib</code>.
	 * <p>
	 * Also, set the initial default values in the MBS page data, so if user changes nothing on this page, the default values will
	 * be picked up.
	 * 
	 * @param mpiIncludePath
	 */
	private void setDefaultOtherNames(String mpiIncludePath) {
		defaultMpiLibName = "mpi"; //$NON-NLS-1$
		setCurrentMpiLibName(defaultMpiLibName);

		// if >1 path in mpi include path, use just the first
		// one to guess at the libpath
		String tempPath = mpiIncludePath;
		int sepLoc = tempPath.indexOf(java.io.File.pathSeparatorChar);
		if (-1 != sepLoc) {
			tempPath = mpiIncludePath.substring(0, sepLoc);
		}
		// if no mpi include path, then don't bother to calculate
		// a libpath based on it.
		if (mpiIncludePath.length() > 0) {
			IPath path = Path.fromOSString(tempPath);
			path = path.removeLastSegments(1);
			path = path.addTrailingSeparator();
			defaultMpiLibPath = path.toString() + "lib"; //$NON-NLS-1$
			setCurrentMpiLibPath(defaultMpiLibPath);
			// standardize format for mpi include path, too
			path = Path.fromOSString(mpiIncludePath);
			String temp = path.toString();
			temp = stripTrailingSeparator(temp);
			defaultMpiIncludePath = temp;
			setCurrentMpiIncludePath(defaultMpiIncludePath);
		}
		else {
			defaultMpiLibPath = "";// must be non-null to set in text fields //$NON-NLS-1$
		}

		setCurrentMpiCompileCommand(defaultMpiBuildCommand);
	}

	/**
	 * This sets what will be remembered for MPI include path when we leave the wizard page
	 * (so we can retrieve the information from the ProcessRunner to actually do the change
	 * to the project info)
	 * 
	 * @param path
	 */
	private void setCurrentMpiIncludePath(String path) {
		currentMpiIncludePath = path;
		pageData.put(PAGE_ID + DOT + INCLUDE_PATH_PROP_ID, path);
	}

	/**
	 * This sets what will be remembered for library name when we leave the wizard page
	 * (so we can retrieve the information from the ProcessRunner to actually do the change
	 * to the project info)
	 * 
	 * @param path
	 */
	private void setCurrentMpiLibName(String name) {
		// currentMpiIncludePath = name;//???
		pageData.put(PAGE_ID + DOT + LIB_PROP_ID, name);
	}

	Map<String, String> pageData = new HashMap<String, String>();

	/**
	 * This sets what will be remembered for library search path when we leave the wizard page
	 * (so we can retrieve the information from the ProcessRunner to actually do the change
	 * to the project info)
	 * 
	 * @param path
	 */
	private void setCurrentMpiLibPath(String path) {
		currentMpiIncludePath = path;
		pageData.put(PAGE_ID + DOT + LIBRARY_SEARCH_PATH_PROP_ID, path);
	}

	private void setCurrentMpiCompileCommand(String buildCommand) {
		currentMpiCompileCommand = buildCommand;
		pageData.put(PAGE_ID + DOT + MPI_COMPILE_COMMAND_PROP_ID, buildCommand);
	}

	private void setCurrentMpiLinkCommand(String buildCommand) {
		currentMpiLinkCommand = buildCommand;
		pageData.put(PAGE_ID + DOT + MPI_LINK_COMMAND_PROP_ID, buildCommand);
	}

	public String getName() {
		return new String(desc + " name"); //$NON-NLS-1$
	}

	/**
	 * Return the path in one of the path fields.
	 * 
	 * @return String
	 */
	private String getPathFromPathField(Text textField) {
		URI fieldURI;
		try {
			fieldURI = new URI(textField.getText());
		} catch (URISyntaxException e) {
			return textField.getText();
		}
		return fieldURI.getPath();
	}

	/**
	 * Update the include path field based on the selected path.
	 * 
	 * @param selectedPath
	 */
	private void updateIncludePathField(String selectedPath) {
		if (traceOn)
			System.out.println("MPWP.updateLocationField to " + selectedPath); //$NON-NLS-1$
		includePathField.setText(selectedPath);
	}

	/**
	 * Update the lib path field based on the selected path.
	 * 
	 * @param selectedPath
	 */
	private void updateLibPathField(String selectedPath) {
		if (traceOn)
			System.out.println("MPWP.updateLocationField to " + selectedPath); //$NON-NLS-1$
		libPathField.setText(selectedPath);
	}

	/**
	 * Open an appropriate directory browser and get selected directory <br>
	 * TODO: can we use the dialog that CDT uses here? Allows direct typing.
	 */
	private void handleLocationBrowseButtonPressed() {

		String selectedDirectory = null;
		String dirName = getPathFromPathField(includePathField);

		DirectoryDialog dialog = new DirectoryDialog(includePathField.getShell());
		dialog.setMessage(Messages.MPIProjectWizardPage_mpi_include_path);

		dialog.setFilterPath(dirName);

		selectedDirectory = dialog.open();

		if (selectedDirectory != null) {
			updateIncludePathField(selectedDirectory);

			includePathField.setText(selectedDirectory);
			if (traceOn)
				System.out.println("Directory found via browse: " + selectedDirectory); //$NON-NLS-1$
			// set value to where we can find it in the ProcessRunner later
			setCurrentMpiIncludePath(selectedDirectory);
		}
	}

	private void handleLocationBrowseButton2Pressed() {

		String selectedDirectory = null;
		String dirName = getPathFromPathField(libPathField);

		DirectoryDialog dialog = new DirectoryDialog(libPathField.getShell());
		dialog.setMessage(Messages.MPIProjectWizardPage_mpi_lib_search_path);

		dialog.setFilterPath(dirName);

		selectedDirectory = dialog.open();

		if (selectedDirectory != null) {
			updateLibPathField(selectedDirectory);

			libPathField.setText(selectedDirectory);
			if (traceOn)
				System.out.println("Directory found via browse: " + selectedDirectory); //$NON-NLS-1$
			// set value to where we can find it in the ProcessRunner later
			setCurrentMpiLibPath(selectedDirectory);
		}
	}

	/**
	 * Remove any trailing device separator characther (e.g. ; on windows or : on Linux)
	 * 
	 * @param str
	 * @return the string without any trailing separator
	 */
	private String stripTrailingSeparator(String str) {
		if (str.length() == 0)
			return str;
		char lastChar = str.charAt(str.length() - 1);
		if (lastChar == java.io.File.pathSeparatorChar) {
			String temp = str.substring(0, str.length() - 1);
			return temp;
		}
		return str;

	}

	/**
	 * Create the area in which the user can enter MPI include path and other information
	 * 
	 * @param composite
	 *            to put it in
	 * @param defaultEnabled
	 *            indicates if the "use defaults" checkbox is to be initially selected.
	 */
	private void createUserEntryArea(Composite composite, boolean defaultEnabled) {
		if (wizardTraceOn)
			System.out.println("MPIProjectWizardPage.createUserEntryArea() "); //$NON-NLS-1$

		// set help context?

		includePathLabel = new Label(composite, SWT.NONE);
		includePathLabel.setText(Messages.MPIProjectWizardPage_include_path);
		includePathLabel.setToolTipText(Messages.MPIProjectWizardPage_locn_of_mpi_incl_path);

		// Include path location entry field
		includePathField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		data.horizontalSpan = 2;
		includePathField.setLayoutData(data);
		includePathField.setText(defaultMpiIncludePath);
		includePathField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentMpiIncludePath(includePathField.getText());
				if (traceOn)
					System.out.println("locationField.modifyText(): " + currentMpiIncludePath); //$NON-NLS-1$
			}
		});

		// browse button
		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(Messages.MPIProjectWizardPage_browse);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (traceOn)
					System.out.println("Browse button pressed."); //$NON-NLS-1$
				handleLocationBrowseButtonPressed();

			}
		});

		// how do we know when next/finish button pushed? we don't.
		// we just store all info where we can find it when the MPIProjectProcess (ProcessRunner) runs after all the wizard pages
		// are done.

		libLabel = new Label(composite, SWT.NONE);
		libLabel.setText(Messages.MPIProjectWizardPage_library_name);
		libLabel.setToolTipText(Messages.MPIProjectWizardPage_library_name);

		libNameField = new Text(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = SIZING_TEXT_FIELD_WIDTH;
		gd.horizontalSpan = 2;
		libNameField.setLayoutData(gd);
		// don't set libname text field if there is no libpath.
		if (defaultMpiLibName != null && defaultMpiLibPath != null & defaultMpiLibPath.length() > 0) {
			libNameField.setText(defaultMpiLibName);
		}
		libNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentMpiLibName(libNameField.getText());
				if (traceOn)
					System.out.println("libNameField.modifyText(): " + currentLibName); //$NON-NLS-1$
			}
		});

		(new Label(composite, SWT.NONE)).setText(" ");//spacer //$NON-NLS-1$

		libPathLabel = new Label(composite, SWT.NONE);
		libPathLabel.setText(Messages.MPIProjectWizardPage_library_search_path);
		libPathLabel.setToolTipText(Messages.MPIProjectWizardPage_library_name);

		libPathField = new Text(composite, SWT.BORDER);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.widthHint = SIZING_TEXT_FIELD_WIDTH;
		gd2.horizontalSpan = 2;
		libPathField.setLayoutData(gd2);
		if (defaultMpiLibPath != null) {
			libPathField.setText(defaultMpiLibPath);// what if null? https://bugs.eclipse.org/bugs/show_bug.cgi?id=314927
		}
		libPathField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentMpiLibPath(libPathField.getText());
				if (traceOn)
					System.out.println("libPathField.modifyText(): " + currentLibPath); //$NON-NLS-1$
			}
		});

		// browse button

		browseButton2 = new Button(composite, SWT.PUSH);
		browseButton2.setText(Messages.MPIProjectWizardPage_browse);
		browseButton2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (traceOn)
					System.out.println("Browse button pressed. DO SOMETHING HERE."); //$NON-NLS-1$
				handleLocationBrowseButton2Pressed();

			}
		});
		mpiCompileCommandLabel = new Label(composite, SWT.NONE);
		mpiCompileCommandLabel.setText(Messages.MPIProjectWizardPage_mpi_compile_cmd);
		mpiCompileCommandField = new Text(composite, SWT.BORDER);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		gd3.widthHint = SIZING_TEXT_FIELD_WIDTH;
		gd3.horizontalSpan = 2;
		mpiCompileCommandField.setLayoutData(gd3);
		mpiCompileCommandField.setText(defaultMpiBuildCommand);
		mpiCompileCommandField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentMpiCompileCommand(mpiCompileCommandField.getText());
				if (traceOn)
					System.out.println("mpiCompileCommandField.modifyText(): " + currentMpiCompileCommand); //$NON-NLS-1$
			}
		});
		(new Label(composite, SWT.NONE)).setText(" ");//spacer //$NON-NLS-1$

		mpiLinkCommandLabel = new Label(composite, SWT.NONE);
		mpiLinkCommandLabel.setText(Messages.MPIProjectWizardPage_mpi_link_cmd);
		mpiLinkCommandField = new Text(composite, SWT.BORDER);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		gd4.widthHint = SIZING_TEXT_FIELD_WIDTH;
		gd4.horizontalSpan = 2;
		mpiLinkCommandField.setLayoutData(gd4);
		mpiLinkCommandField.setText(defaultMpiBuildCommand);
		mpiLinkCommandField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentMpiLinkCommand(mpiLinkCommandField.getText());
				if (traceOn)
					System.out.println("mpiLinkCommandField.modifyText(): " + currentMpiLinkCommand); //$NON-NLS-1$
			}
		});
		(new Label(composite, SWT.NONE)).setText(" ");//spacer //$NON-NLS-1$

		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		gd5.widthHint = SIZING_TEXT_FIELD_WIDTH;
		gd5.horizontalSpan = 4;
		Label msg0 = new Label(composite, SWT.NONE);
		msg0.setLayoutData(gd5);
		msg0.setText(" "); // spacer //$NON-NLS-1$

		// String str = "\nIf your project shows errors and can't recognize MPI symbols, make sure your \n"
		// + "MPI header file is in the include path for the project.  Since mpicc may do that for you\n"
		// + "for the build, Eclipse may not know about it.  You may want to add it to \n"
		// + "      Project Properties, C/C++ General, Paths and Symbols\n"
		// + "so that the editor and indexer can find MPI symbols.  On the next page, \n"
		// + "you can access Project Properties directly with the 'Advanced settings...' button.\n";

		String str = Messages.MPIProjectWizardPage_includeFileHint_longMsg1
				+ Messages.MPIProjectWizardPage_includeFileHint_longMsg2
				+ Messages.MPIProjectWizardPage_includeFileHint_longMsg3
				+ Messages.MPIProjectWizardPage_includeFileHint_longMsg4
				+ Messages.MPIProjectWizardPage_includeFileHint_longMsg5
				+ Messages.MPIProjectWizardPage_includeFileHint_longMsg6;

		// from CDTConfigWizardPage - single label w/i a group looks like grey text area
		Group gr = new Group(composite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gr.setLayoutData(gd);
		gr.setLayout(new FillLayout());
		Label lb = new Label(gr, SWT.NONE);
		lb.setText(str);

		/*
		 * StringBuffer msgBuf = new StringBuffer("If your project shows errors and can't recognize MPI symbols, ");
		 * msgBuf.append("make sure your MPI header file is in the include path for the project. ");
		 * msgBuf.append(" Since mpicc may do that for you for the build, Eclipse may not know about it.  ");
		 * msgBuf.append("You may want to add it to Project Properties, C/C++ General, Paths and Symbols, ");
		 * msgBuf.append(" so that the editor and indexer can find MPI symbols.");
		 */
	}

	/**
	 * Create the contents of the wizard page.
	 * 
	 * @param composite
	 *            parent composite in which these widgets will reside
	 * 
	 * @param defaultEnabled
	 *            do we use default mpi include path?
	 */
	private void createContents(Composite composite, boolean defaultEnabled) {
		int columns = 4;
		Composite group = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		useMpiProjectSettingsButton = new Button(group, SWT.CHECK | SWT.RIGHT);
		useMpiProjectSettingsButton.setText(Messages.MPIProjectWizardPage_add_mpi_proj_settings_to_proj);
		GridData gd = new GridData();
		gd.horizontalSpan = columns;
		useMpiProjectSettingsButton.setLayoutData(gd);
		useMpiProjectSettingsButton.setSelection(useMpiProjectSettings);
		useMpiProjectSettingsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useMpiProjectSettings = useMpiProjectSettingsButton.getSelection();
				// set value so we can read it later
				pageData.put(PAGE_ID + DOT + DO_MPI_INCLUDES, Boolean.toString(useMpiProjectSettings));

				useDefaultsButton.setEnabled(useMpiProjectSettings);
				if (mpiSampleButton != null)
					mpiSampleButton.setEnabled(useMpiProjectSettings);
				if (useMpiProjectSettings) {
					boolean useDefaults = useDefaultsButton.getSelection();
					setUserAreaEnabled(!useDefaults);
				}

				else
					setUserAreaEnabled(false);

			}
		});

		useDefaultsButton = new Button(group, SWT.CHECK | SWT.RIGHT);
		useDefaultsButton.setText(Messages.MPIProjectWizardPage_use_default_info);
		useDefaultsButton.setSelection(defaultEnabled);
		useDefaultsButton.setEnabled(useMpiProjectSettings);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = columns;
		useDefaultsButton.setLayoutData(buttonData);
		useDefaultsButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean useDefaults = useDefaultsButton.getSelection();

				if (useDefaults) {
					// reset all fields and values back to the defaults
					includePathField.setText(defaultMpiIncludePath);
					setCurrentMpiIncludePath(defaultMpiIncludePath);

					libPathField.setText(defaultMpiLibName);
					setCurrentMpiLibName(defaultMpiLibName);

					libNameField.setText(defaultMpiLibName);
					setCurrentMpiLibName(defaultMpiLibName);

					libPathField.setText(defaultMpiLibPath);
					setCurrentMpiLibPath(defaultMpiLibPath);

					mpiCompileCommandField.setText(defaultMpiBuildCommand);
					setCurrentMpiCompileCommand(defaultMpiBuildCommand);

					mpiLinkCommandField.setText(defaultMpiBuildCommand);
					setCurrentMpiLinkCommand(defaultMpiBuildCommand);
				}
				setUserAreaEnabled(!useDefaults);
			}
		});

		createUserEntryArea(group, defaultEnabled);
		setUserAreaEnabled(!defaultEnabled);
	}

	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		boolean defaultEnabled = true;
		createContents(composite, defaultEnabled);
	}

	public Control getControl() {
		return composite;
	}

	public String getDescription() {
		String tmp = Messages.MPIProjectWizardPage_select_mpi_incl_path_libname_and_etc;
		return tmp;
	}

	public String getErrorMessage() {
		return null;
	}

	public Image getImage() {
		return getWizard().getDefaultPageImage();
	}

	public String getMessage() {
		return null;
	}

	public String getTitle() {
		return Messages.MPIProjectWizardPage_mpi_project_settings;
	}

	public void performHelp() {
		// do nothing

	}

	public void setDescription(String description) {
		// do nothing

	}

	public void setImageDescriptor(ImageDescriptor image) {
		// do nothing

	}

	public void setTitle(String title) {
		// do nothing

	}

	/**
	 * If you need to init data/widgets on the page when it is displayed based
	 * on the data set in previous pages, you can override
	 * IDialogPage.setVisible(boolean visible) and setup everything when visible ==
	 * true.
	 * 
	 * Otherwise, do inits in the ctor.
	 */
	public void setVisible(boolean visible) {
		composite.setVisible(visible);
		if (traceOn)
			System.out.println("MPIProjectWizardPage.setVisible: " + visible); //$NON-NLS-1$

	}

	/**
	 * Determines the status of the wizard's finish button. If we don't want the
	 * user to press finish yet, set this to false. <br>
	 * The Next/Finish/Cancel methods belong to the wizard itself and not the
	 * pages.
	 * 
	 * @return boolean true if finish button should be enabled; false otherwise.
	 */
	protected boolean isCustomPageComplete() {
		// BRT if isDefaults is not checked then includePathField etc. should be
		// filled in.
		// BRT if isDefaults is checked then there should actually BE some
		// defaults to use.
		return true;
	}

	/**
	 * Enable/disable "user area" which is the place user can type and make
	 * changes (includePathField, its label and button, etc.)
	 * 
	 * @param enabled
	 */
	private void setUserAreaEnabled(boolean enabled) {

		includePathField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
		browseButton2.setEnabled(enabled);
		libNameField.setEnabled(enabled);
		libPathField.setEnabled(enabled);

		includePathLabel.setEnabled(enabled);
		libPathLabel.setEnabled(enabled);
		libLabel.setEnabled(enabled);

		mpiCompileCommandLabel.setEnabled(enabled);
		mpiCompileCommandField.setEnabled(enabled);
		mpiLinkCommandLabel.setEnabled(enabled);
		mpiLinkCommandField.setEnabled(enabled);
	}

	/**
	 * What's the default, do we include MPI includes or not?
	 * If there is any difficulty getting information, use this default
	 * setting.
	 * This is a slight misnomer. It should be called "MPI Project info" because
	 * it can include build command as well as include and linker info.
	 * 
	 * @return
	 */
	public static boolean getDefaultUseMpiIncludes() {
		return defaultUseMpiIncludes;
	}

	public Map<String, String> getPageData() {
		return pageData;
	}

	/**
	 * Determines whether we are in a C or C++ project template
	 * 
	 * @return
	 */
	abstract protected String getMpiProjectType();

	@Override
	protected IPreferencePage getPreferencePage() {
		if (preferencePage == null) {
			preferencePage = new MPIPreferencePage();
		}
		return preferencePage;
	}

}
