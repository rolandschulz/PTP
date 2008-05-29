/*******************************************************************************
 * Copyright (c) 2006,2008 IBM Corp. and others.
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
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.openmp.core.OpenMPIDs;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard Page for collecting info about OpenMP project - appended to end of
 * "New  C project" wizard
 * 
 * TODO remove dup code, share with MPI version in a common class etc.
 * @author Beth Tibbitts
 * 
 */
public class OpenMPProjectWizardPage extends AbstractProjectWizardPage {
	public static final String DOT = ".";
	private static final boolean traceOn=false;
	public static final boolean wizardTraceOn=false;

	private Composite composite;
	public static final String PAGE_ID="org.eclipse.ptp.pldt.wizards.wizardPages.OpenMPProjectWizardPage";

	// The following are IDs for storing info in MBSPageData so it can be retrieved in OpenMPProjectProcess (ProcessRunner)
	// when the wizard is done.
	/**
	 * Store in MBSPageData  (with this ID) whether user wants to include OpenMP info in the project.
	 */
	public static final String DO_OpenMP_INCLUDES = "doOpenMPincludes";
	/**
	 * store in MBSPageData (with this ID) what the include path to OpenMP will be.
	 */
	public static final String INCLUDE_PATH_PROP_ID = "includePath";
	/**
	 * store in MBSPageData (with this ID) what the library name for the linker is.
	 */
	public static final String LIB_PROP_ID = "lib";
	/**
	 * store in MBSPageData (with this ID) what the library search path is.
	 */
	public static final String LIBRARY_SEARCH_PATH_PROP_ID = "libPath";
	
	public static final String OpenMP_COMPILE_COMMAND_PROP_ID = "OpenMPCompileCommand";
	public static final String OpenMP_LINK_COMMAND_PROP_ID = "OpenMPLinkCommand";

	private String currentOpenMPIncludePath;
	private String currentLibName;
	private String currentLibPath;
	private String currentOpenMPCompileCommand;
	private String currentOpenMPLinkCommand;

	private String defaultOpenMPIncludePath;
	private String defaultOpenMPLibName;
	private String defaultOpenMPLibPath;
	private String defaultOpenMPBuildCommand;
	
	private Text includePathField;
	private Text libNameField;
	private Text libPathField;
	private Text openMPCompileCommandField, openMPLinkCommandField;
	
	private Label includePathLabel, libLabel, libPathLabel, openMPCompileCommandLabel, openMPLinkCommandLabel;

	private Button browseButton;
	private Button browseButton2;

	private Button useDefaultsButton;
	private Button useOpenMPProjectSettingsButton;
	private static boolean defaultUseOpenMPIncludes=true;
	
	private Button OpenMPSampleButton;

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	/**
	 * By default we DO use OpenMP project settings in a new project.<br>
	 */
	private boolean useOpenMPProjectSettings=true;
	private String desc = "OpenMP Project Page";
	
	/**
	 * The CDT new project wizard page for OpenMP projects.  
	 * Adds the include paths, library information, etc. for an MPI project.
	 * @throws CoreException 
	 * 
	 */
	public OpenMPProjectWizardPage() throws CoreException {
		super("OpenMP Project Settings");
		prefIDincludes=OpenMPIDs.OpenMP_INCLUDES;
		if(wizardTraceOn)System.out.println("OpenMPProjectWizardPage().ctor...");

		//CommonPlugin.log(IStatus.ERROR,"Test error");
		//CommonPlugin.log(IStatus.WARNING,"Test warning");
		
		// access the preference store from the OpenMP plugin
		preferenceStore = OpenMPPlugin.getDefault().getPreferenceStore();
		String mip=preferenceStore.getString(prefIDincludes);
		if(traceOn)System.out.println("Got OpenMP include pref from other plugin: "+mip);

		// Set the defaults here in the wizard page constructor and just
		// overwrite them if the user changes them.
		defaultOpenMPIncludePath = preferenceStore.getString(prefIDincludes);
		if(defaultOpenMPIncludePath.length()==0) {
			// warn if no OpenMP preferences have been set
			String newIncludePath=showNoPrefs("OpenMP",prefIDincludes);
			defaultOpenMPIncludePath=newIncludePath;
		}
		setDefaultOtherNames(defaultOpenMPIncludePath);
		// the following sets what will be remembered when we leave the page.
		setCurrentOpenMPIncludePath(defaultOpenMPIncludePath);
		
		defaultOpenMPBuildCommand=preferenceStore.getString(OpenMPIDs.OpenMP_BUILD_CMD);
		setCurrentOpenMPCompileCommand(defaultOpenMPBuildCommand);
		setCurrentOpenMPLinkCommand(defaultOpenMPBuildCommand);		
	}

	/**
	 * Warn user that the OpenMP project preferences aren't set, and thus the new project wizard will not be very useful.
	 * <br>
	 * TODO: do we need a "do not show this message again" setting? (af - yes please! ;)
	 */
	private static boolean alreadyShown;
	private static void showNoPrefs1() {
		if(!alreadyShown) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			StringBuffer buf=new StringBuffer("No OpenMP Preferences set; ");
			buf.append("Default project setting will be more useful if OpenMP preferences are set first. ");
			buf.append("\nUse Window > Preferences and select Parallel Language Development Tools, which may be under PTP preferences.");
			buf.append("You can cancel out of new project wizard to enter OpenMP preferences now.");
			MessageDialog.openWarning(shell, "No OpenMP Preferences set", buf.toString());
			alreadyShown= true;
		}
	}

	/**
	 * Set the default lib name and lib path based on what the 
	 * include path is.  We assume something like
	 * <code>openMPIncludepath=/my/path/include</code>
	 * in which case we set
	 * <code>libName="lib" </code>and <code> defaultOpenMPLibPath=/my/path/lib</code>.
	 * <p>
	 * Also, set the initial default values in the MBS page data,
	 * so if user changes nothing on this page, the default values
	 * will be picked up.
	 * 
	 * @param openMPincludePath
	 */
	private void setDefaultOtherNames(String openMPincludePath) {
		defaultOpenMPLibName="openmp";
		setCurrentOpenMPLibName(defaultOpenMPLibName);
		
		// if >1 path in openmp include path, use just the first
		// one to guess at the libpath
		String tempPath=openMPincludePath;
		int sepLoc=tempPath.indexOf(java.io.File.pathSeparatorChar);
		if(-1!=sepLoc) {
			tempPath=openMPincludePath.substring(0, sepLoc);
		}
		IPath path = Path.fromOSString(tempPath);
		path=path.removeLastSegments(1);
		path=path.addTrailingSeparator();

		defaultOpenMPLibPath=path.toString()+"lib";
		//System.out.println("defaultOpenMPLibPath="+defaultOpenMPLibPath);
		setCurrentOpenMPLibPath(defaultOpenMPLibPath);
		
		//standardize format for openmp include path, too
		path = Path.fromOSString(openMPincludePath);
		String temp=path.toString();
		temp=stripTrailingSeparator(temp);
		defaultOpenMPIncludePath=temp;
		setCurrentOpenMPIncludePath(defaultOpenMPIncludePath);	
			
		setCurrentOpenMPCompileCommand(defaultOpenMPBuildCommand);
	}

	/**
	 * This sets what will be remembered for OpenMP include path when we leave the wizard page
	 * (so we can retrieve the information from the ProcessRunner to actually do the change
	 * to the project info)
	 * 
	 * @param path
	 */
	private void setCurrentOpenMPIncludePath(String path) {
		currentOpenMPIncludePath = path;
		pageData.put(PAGE_ID+DOT+INCLUDE_PATH_PROP_ID, path);
	}
	/**
	 * This sets what will be remembered for library name when we leave the wizard page
	 * (so we can retrieve the information from the ProcessRunner to actually do the change
	 * to the project info)
	 * 
	 * @param path
	 */
	private void setCurrentOpenMPLibName(String name) {
		currentOpenMPIncludePath = name;
		pageData.put(PAGE_ID+DOT+LIB_PROP_ID, name);
	}
	
	Map<String, String> pageData= new HashMap<String, String>();
	
	/**
	 * This sets what will be remembered for library search path when we leave the wizard page
	 * (so we can retrieve the information from the ProcessRunner to actually do the change
	 * to the project info)
	 * 
	 * @param path
	 */
	private void setCurrentOpenMPLibPath(String path) {
		currentOpenMPIncludePath = path;
		pageData.put(PAGE_ID+DOT+LIBRARY_SEARCH_PATH_PROP_ID, path);
	}
	private void setCurrentOpenMPCompileCommand(String buildCommand) {
		currentOpenMPCompileCommand = buildCommand;
		pageData.put(PAGE_ID+DOT+OpenMP_COMPILE_COMMAND_PROP_ID, buildCommand);
	}
	private void setCurrentOpenMPLinkCommand(String buildCommand) {
		currentOpenMPLinkCommand = buildCommand;
		pageData.put(PAGE_ID+DOT+OpenMP_LINK_COMMAND_PROP_ID, buildCommand);
	}

	public String getName() {
		return new String(desc + " name");
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
		if(traceOn)System.out.println("MPWP.updateLocationField to " + selectedPath);
		includePathField.setText(selectedPath);
	}
	/**
	 * Update the lib path field based on the selected path.
	 * 
	 * @param selectedPath
	 */
	private void updateLibPathField(String selectedPath) {
		if(traceOn)System.out.println("MPWP.updateLocationField to " + selectedPath);
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
		dialog.setMessage("OpenMP Include path:");

		dialog.setFilterPath(dirName);

		selectedDirectory = dialog.open();

		if (selectedDirectory != null) {
			updateIncludePathField(selectedDirectory);

			includePathField.setText(selectedDirectory);
			if(traceOn)System.out.println("Directory found via browse: " + selectedDirectory);
			// set value to where we can find it in the ProcessRunner later
			setCurrentOpenMPIncludePath(selectedDirectory);
		}
	}
	private void handleLocationBrowseButton2Pressed() {

		String selectedDirectory = null;
		String dirName = getPathFromPathField(libPathField);

		DirectoryDialog dialog = new DirectoryDialog(libPathField.getShell());
		dialog.setMessage("OpenMP library search path:");

		dialog.setFilterPath(dirName);

		selectedDirectory = dialog.open();

		if (selectedDirectory != null) {
			updateLibPathField(selectedDirectory);

			libPathField.setText(selectedDirectory);
			if(traceOn)System.out.println("Directory found via browse: " + selectedDirectory);
			// set value to where we can find it in the ProcessRunner later
			setCurrentOpenMPLibPath(selectedDirectory);
		}
	}

	/**
	 * Remove any trailing device separator characther (e.g. ; on windows or : on Linux)
	 * @param str
	 * @return the string without any trailing separator
	 */
	private String stripTrailingSeparator(String str) {
		if(str.length()==0)return str;
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
	 * @param composite to put it in
	 * @param defaultEnabled indicates if the "use defaults" checkbox is to be initially selected.
	 */
	private void createUserEntryArea(Composite composite, boolean defaultEnabled) {
		//?? err? causes things to happen in wrong order??   IProject project = this.getProject();
		//String name = project.getName();
		if(wizardTraceOn)System.out.println("OpenMPProjectWizardPage.createUserEntryArea() " );
		
		includePathLabel = new Label(composite, SWT.NONE);
		includePathLabel.setText("Include path:");
		includePathLabel.setToolTipText("Location of OpenMP include path(s)");

		// Include path location  entry field
		includePathField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		data.horizontalSpan = 2;
		includePathField.setLayoutData(data);
		includePathField.setText(defaultOpenMPIncludePath);
		includePathField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentOpenMPIncludePath(includePathField.getText());
				if(traceOn)System.out.println("locationField.modifyText(): " + currentOpenMPIncludePath);
			}
		});

		// browse button
		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(traceOn)System.out.println("Browse button pressed.");
				handleLocationBrowseButtonPressed();

			}
		});

		// how do we know when next/finish button pushed? we don't.
		// we just store all info where we can find it when the OpenMPProjectProcess(ProcessRunner) runs after all the wizard pages are done.
		
		libLabel=new Label(composite, SWT.NONE);
		libLabel.setText("Library name:");
		libLabel.setToolTipText("Library name:");
		
		libNameField=new Text(composite,SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint=SIZING_TEXT_FIELD_WIDTH;
		gd.horizontalSpan=2;
		libNameField.setLayoutData(gd);
		libNameField.setText(defaultOpenMPLibName);
		libNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentOpenMPLibName(libNameField.getText());
				if(traceOn)System.out.println("libNameField.modifyText(): " + currentLibName);
			}
		});
		
		(new Label(composite,SWT.NONE)).setText(" ");//spacer
		
		libPathLabel=new Label(composite, SWT.NONE);
		libPathLabel.setText("Library search path:");
		libPathLabel.setToolTipText("Library name:");
		
		
		libPathField=new Text(composite,SWT.BORDER);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.widthHint=SIZING_TEXT_FIELD_WIDTH;
		gd2.horizontalSpan=2;
		libPathField.setLayoutData(gd2);
		libPathField.setText(defaultOpenMPLibPath);
		libPathField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentOpenMPLibPath(libPathField.getText());
				if(traceOn)System.out.println("libPathField.modifyText(): " + currentLibPath);
			}
		});
		
//		 browse button

		browseButton2 = new Button(composite, SWT.PUSH);
		browseButton2.setText("Browse...");
		browseButton2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(traceOn)System.out.println("Browse button pressed. DO SOMETHING HERE.");
				handleLocationBrowseButton2Pressed();

			}
		});
		openMPCompileCommandLabel= new Label(composite,SWT.NONE);
		openMPCompileCommandLabel.setText("OpenMP compile command: ");
		openMPCompileCommandField=new Text(composite,SWT.BORDER);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		gd3.widthHint=SIZING_TEXT_FIELD_WIDTH;
		gd3.horizontalSpan=2;
		openMPCompileCommandField.setLayoutData(gd3);
		openMPCompileCommandField.setText(defaultOpenMPBuildCommand);
		openMPCompileCommandField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentOpenMPCompileCommand(openMPCompileCommandField.getText());
				if(traceOn)System.out.println("OpenMPCompileCommandField.modifyText(): " + currentOpenMPCompileCommand);
			}
		});
		(new Label(composite,SWT.NONE)).setText(" ");//spacer
		
		openMPLinkCommandLabel= new Label(composite,SWT.NONE);
		openMPLinkCommandLabel.setText("OpenMP link command: ");
		openMPLinkCommandField=new Text(composite,SWT.BORDER);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		gd4.widthHint=SIZING_TEXT_FIELD_WIDTH;
		gd4.horizontalSpan=2;
		openMPLinkCommandField.setLayoutData(gd3);
		openMPLinkCommandField.setText(defaultOpenMPBuildCommand);
		openMPLinkCommandField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setCurrentOpenMPLinkCommand(openMPLinkCommandField.getText());
				if(traceOn)System.out.println("OpenMPLinkCommandField.modifyText(): " + currentOpenMPLinkCommand);
			}
		});
		(new Label(composite,SWT.NONE)).setText(" ");//spacer

		
	}


	/**
	 * Create the contents of the wizard page.
	 * 
	 * @param composite parent composite in which these widgets will reside
	 * 
	 * @param defaultEnabled do we use default mpi include path?
	 */
	private void createContents(Composite composite, boolean defaultEnabled) {

		int columns = 4;

		Composite group = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		useOpenMPProjectSettingsButton = new Button(group, SWT.CHECK | SWT.RIGHT);
		useOpenMPProjectSettingsButton.setText("Add OpenMP project settings to this project");
		GridData gd=new GridData();
		gd.horizontalSpan=columns;
		useOpenMPProjectSettingsButton.setLayoutData(gd);
		useOpenMPProjectSettingsButton.setSelection(useOpenMPProjectSettings);
		useOpenMPProjectSettingsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useOpenMPProjectSettings = useOpenMPProjectSettingsButton.getSelection();
				// set value so we can read it later
				pageData.put(PAGE_ID+DOT+DO_OpenMP_INCLUDES, Boolean.toString(useOpenMPProjectSettings));
				
				useDefaultsButton.setEnabled(useOpenMPProjectSettings);
				if(OpenMPSampleButton!=null)
				   OpenMPSampleButton.setEnabled(useOpenMPProjectSettings);
				if(useOpenMPProjectSettings) {
				  boolean useDefaults=useDefaultsButton.getSelection();
				  setUserAreaEnabled(!useDefaults);
				}
				
				else
					setUserAreaEnabled(false);
				
			}
		});

		useDefaultsButton = new Button(group, SWT.CHECK | SWT.RIGHT);
		useDefaultsButton.setText("Use default information");
		useDefaultsButton.setSelection(defaultEnabled);
		useDefaultsButton.setEnabled(false);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = columns;
		useDefaultsButton.setLayoutData(buttonData);
		useDefaultsButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean useDefaults = useDefaultsButton.getSelection();

				if (useDefaults) { 
					// reset all fields and values back to the defaults
					includePathField.setText(defaultOpenMPIncludePath);
					setCurrentOpenMPIncludePath(defaultOpenMPIncludePath);
					
					libPathField.setText(defaultOpenMPLibName);
					setCurrentOpenMPLibName(defaultOpenMPLibName);
					
					libNameField.setText(defaultOpenMPLibName);
					setCurrentOpenMPLibName(defaultOpenMPLibName);
					
					libPathField.setText(defaultOpenMPLibPath);
					setCurrentOpenMPLibPath(defaultOpenMPLibPath);
					
					openMPCompileCommandField.setText(defaultOpenMPBuildCommand);
					setCurrentOpenMPCompileCommand(defaultOpenMPBuildCommand);
					
					openMPLinkCommandField.setText(defaultOpenMPBuildCommand);
					setCurrentOpenMPLinkCommand(defaultOpenMPBuildCommand);
				}
				setUserAreaEnabled(!useDefaults);
			}
		});

		createUserEntryArea(group, defaultEnabled);
/*		
 		// mpi sample file now provided by project template (also openmp)
		OpenMPSampleButton = new Button(group, SWT.CHECK | SWT.RIGHT);
		OpenMPSampleButton.setText("Include sample OpenMP source file?");
		OpenMPSampleButton.setSelection(false);
		OpenMPSampleButton.setEnabled(false);
		GridData gdSample=new GridData();
		gdSample.horizontalSpan = columns;
		OpenMPSampleButton.setLayoutData(gdSample);
		OpenMPSampleButton.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean doit=OpenMPSampleButton.getSelection();
				setCurrentOpenMPSample(doit);
			}
		
		});
		*/
		setUserAreaEnabled(!defaultEnabled);

	}

	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		boolean defaultEnabled = true;
		createContents(composite, defaultEnabled);
	}

	public void dispose() {
		composite.dispose();

	}

	public Control getControl() {
		return composite;
	}

	public String getDescription() {
		String tmp="Select the OpenMP include path, lib name, library search path, and build command information to be automatically be added to the new project.";
		return tmp;
	}

	public String getErrorMessage() {
		return null;
		// return new String("My error msg");
	}

	public Image getImage() {
		return getWizard().getDefaultPageImage();
	}

	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		return "OpenMP Project Settings";
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
		if(traceOn)System.out.println("OpenMPProjectWizardPage.setVisible: " + visible);

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
		
		openMPCompileCommandLabel.setEnabled(enabled);
		openMPCompileCommandField.setEnabled(enabled);
		openMPLinkCommandLabel.setEnabled(enabled);
		openMPLinkCommandField.setEnabled(enabled);
	}
	
	/**
	 * What's the default, do we include OpenMP includes or not?
	 * If there is any difficulty getting information, use this default
	 * setting.
	 * @return
	 */
	public static boolean getDefaultUseOpenMPIncludes() {
		return defaultUseOpenMPIncludes;
	}

	public Map<String,String> getPageData() {
		return pageData;
	}
	@Override
	protected IPreferencePage getPreferencePage() {
		if(preferencePage == null) {
			preferencePage = new org.eclipse.ptp.pldt.openmp.core.prefs.OpenMPPreferencePage();
		}
		return preferencePage;
	}

}
