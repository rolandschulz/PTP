package org.eclipse.ptp.cell.environment.launcher.pdt.ui;

import java.io.File;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.ptp.cell.environment.launcher.pdt.debug.Debug;
import org.eclipse.ptp.cell.environment.launcher.pdt.internal.IPdtLaunchAttributes;
import org.eclipse.ptp.cell.environment.launcher.pdt.internal.PdtRemoteLaunchDelegate;
import org.eclipse.ptp.remotetools.environment.launcher.core.LinuxPath;
import org.eclipse.ptp.utils.ui.swt.ControlsRelationshipHandler;
import org.eclipse.ptp.utils.ui.swt.FileGroup;
import org.eclipse.ptp.utils.ui.swt.FileMold;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
import org.eclipse.ptp.utils.ui.swt.TextGroup;
import org.eclipse.ptp.utils.ui.swt.TextMold;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class PdtEnvironmentTab extends AbstractLaunchConfigurationTab {
	
	ICProject selectedProject;
	
	// Xml configuration file info
	Frame xmlFileInfo;
	Button copyXmlFile;
	TextGroup remoteXmlDir;
//	FileGroup localXmlPath;
	FileGroup localXmlFile;
	Button workspaceBrowse;
	Button useRemoteXmlFile;
	TextGroup remoteXmlFile;
	ControlsRelationshipHandler buttonHandler;
	
	// Trace file info
	Frame traceInfo;
	TextGroup remoteTraceFile;
	TextGroup traceFilePrefix;
	//FileGroup localTraceDir;
	FileGroup localTraceDir;

	//private Object projectDirectory;
	
	public PdtEnvironmentTab() {
		super();
	}
	
	public void createControl(Composite parent) {
		Composite topControl = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topControl.setLayout(topLayout);

		setControl(topControl);
		
		ModifyListener pdtModifyListener = new PdtModifyListener();
		
		// Create controls and set their listeners
		FrameMold frMold = new FrameMold(""); //$NON-NLS-1$
		TextMold tmold = new TextMold(TextMold.GRID_DATA_SPAN | TextMold.GRID_DATA_ALIGNMENT_FILL 
				 | TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.LABELABOVE, 
				""); //$NON-NLS-1$
		/*FileMold fmold = new FileMold(TextMold.GRID_DATA_ALIGNMENT_FILL 
				| TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.LABELABOVE, "Local XML file Path " +
						"(leave empty if file already on the remote machine):",
				"XML file selection", "Select the XML File from the filesystem");*/
		FileMold fmold = new FileMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_SPAN
				| TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.LABELABOVE, Messages.getString("PdtEnvironmentTab.FileControl_Label_LocalXmlFile"), //$NON-NLS-1$
				Messages.getString("PdtEnvironmentTab.FileControl_Title_SelectXmlFile"), Messages.getString("PdtEnvironmentTab.FileControl_Message_SelectXmlFile")); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		// XML file Info frame
		frMold.setColumns(2);
		frMold.setTitle(Messages.getString("PdtEnvironmentTab.FrameControl_Label_XmlConfFileInfo")); //$NON-NLS-1$
		xmlFileInfo = new Frame(topControl, frMold);
		
		// Trace file Info Frame
		frMold.setColumns(1);
		frMold.setTitle(Messages.getString("PdtEnvironmentTab.FrameControl_Label_TraceFileInfo")); //$NON-NLS-1$
		traceInfo = new Frame(topControl, frMold);
		
		
		// Controls from the XML file info frame
		copyXmlFile = new Button(xmlFileInfo.getTopUserReservedComposite(), SWT.RADIO);
		copyXmlFile.setText(Messages.getString("PdtEnvironmentTab.ButtonControl_Label_CopyXmlFileToRemote")); //$NON-NLS-1$
		GridData cpXmlFileLayData = new GridData(SWT.FILL, SWT.FILL, false, true);
		copyXmlFile.setLayoutData(cpXmlFileLayData);
		
		useRemoteXmlFile = new Button(xmlFileInfo.getTopUserReservedComposite(), SWT.RADIO);
		useRemoteXmlFile.setText(Messages.getString("PdtEnvironmentTab.ButtonControl_Label_UseRemoteXmlFile")); //$NON-NLS-1$
		GridData useXmlFileLayData = GridDataFactory.copyData(cpXmlFileLayData);
		useRemoteXmlFile.setLayoutData(useXmlFileLayData);
		
		PdtSelectionListener pdtSelectionListener = new PdtSelectionListener();
		copyXmlFile.addSelectionListener(pdtSelectionListener);
		//useRemoteXmlFile.addSelectionListener(pdtSelectionListener);
		
		
		tmold.setLabel(Messages.getString("PdtEnvironmentTab.TextControl_Label_RemoteDestXmlDir")); //$NON-NLS-1$
		remoteXmlDir = new TextGroup(xmlFileInfo.getTopUserReservedComposite(), tmold);
		remoteXmlDir.addModifyListener(pdtModifyListener);
		
		localXmlFile = new FileGroup(xmlFileInfo.getTopUserReservedComposite(), fmold);
		localXmlFile.addModifyListener(pdtModifyListener);
		
		tmold.setLabel(Messages.getString("PdtEnvironmentTab.TextControl_Label_RemoteXmlPath")); //$NON-NLS-1$
		remoteXmlFile = new TextGroup(xmlFileInfo.getTopUserReservedComposite(), tmold);
		remoteXmlFile.addModifyListener(pdtModifyListener);
		
		buttonHandler = new ControlsRelationshipHandler();
		buttonHandler.addControlRelationship(copyXmlFile, new Control[] {localXmlFile, remoteXmlDir});
		buttonHandler.addControlRelationship(useRemoteXmlFile, remoteXmlFile);
		/*localXmlPath.getWorkspaceButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog treeSelect = new ElementTreeSelectionDialog(getShell(), new LabelProvider(), new WorkbenchContentProvider())
			}
		});*/
		
		//workspaceBrowse = new Button(xmlFileInfo.getTopUserReservedComposite(), SWT.PUSH);
		//workspaceBrowse.setText("Workspace");
		/*workspaceBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleXmlWorkspaceBrowseAction();
				updateLaunchConfigurationDialog();
			}
			
		});*/
		
		// Controls from the Trace file info frame
		tmold.setLabel(Messages.getString("PdtEnvironmentTab.TextControl_Label_RemoteTraceDir")); //$NON-NLS-1$
		remoteTraceFile = new TextGroup(traceInfo.getTopUserReservedComposite(), tmold);
		remoteTraceFile.addModifyListener(pdtModifyListener);
		
		tmold.setLabel(Messages.getString("PdtEnvironmentTab.TextControl_Label_TraceFilePrefix")); //$NON-NLS-1$
		traceFilePrefix = new TextGroup(traceInfo.getTopUserReservedComposite(), tmold);
		traceFilePrefix.addModifyListener(pdtModifyListener);

		FileMold fMoldLocalTrace = new FileMold(FileMold.GRID_DATA_ALIGNMENT_FILL 
				| FileMold.GRID_DATA_GRAB_EXCESS_SPACE | FileMold.LABELABOVE | FileMold.DIRECTORY_SELECTION, Messages.getString("PdtEnvironmentTab.FileControl_Label_LocalTraceDir"), //$NON-NLS-1$
				Messages.getString("PdtEnvironmentTab.FileControl_Title_SelectTraceFileDir"), Messages.getString("PdtEnvironmentTab.FileControl_Message_SelectTraceFileDir")); //$NON-NLS-1$ //$NON-NLS-2$
		//fMoldLocalTrace.setDialogLabel("Trace file local directory selection");
		//fMoldLocalTrace.setDialogMessage("Select the directory where the generated trace file will be downloaded to");
		localTraceDir = new FileGroup(traceInfo.getTopUserReservedComposite(), fMoldLocalTrace);
		localTraceDir.addModifyListener(pdtModifyListener);
		
	}

	public String getName() {
		return Messages.getString("PdtEnvironmentTab.Tab_Title_PDT"); //$NON-NLS-1$
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		// Stores the selected project directory for use in the workspace browse handler
		//projectDirectory = getProjectDirectory(configuration);
		Debug.read();
		
		try {
			selectedProject = PdtRemoteLaunchDelegate.getCProject(configuration);
		} catch (CoreException e1) {
			//throw new RuntimeException("Error getting C project");
			Debug.POLICY.logError(e1);
			setErrorMessage(Messages.getString("PdtEnvironmentTab.InitializeFrom_Error_RetrivingCProj")); //$NON-NLS-1$
		}
		//selectedProject = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, null);
		
		//selectedProject.
		
		// Basically configuration.getAttribute to get attributes from
		// the configuration to the UI controls
		try {
			remoteXmlDir.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_DIR,
							IPdtLaunchAttributes.DEFAULT_REMOTE_XML_DIR));
			localXmlFile.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_LOCAL_XML_FILE,
							IPdtLaunchAttributes.DEFAULT_LOCAL_XML_FILE));
			
			Boolean copyEnabled = configuration.getAttribute(IPdtLaunchAttributes.ATTR_COPY_XML_FILE, 
					IPdtLaunchAttributes.DEFAULT_COPY_XML_FILE);
			copyXmlFile.setSelection(copyEnabled);
			useRemoteXmlFile.setSelection(!copyEnabled);
			buttonHandler.manageDependentControls(copyEnabled?copyXmlFile:useRemoteXmlFile);
			
			remoteXmlFile.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_FILE, 
							IPdtLaunchAttributes.DEFAULT_REMOTE_XML_FILE));
			remoteTraceFile.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_TRACE_DIR,
							IPdtLaunchAttributes.DEFAULT_REMOTE_TRACE_DIR));
			traceFilePrefix.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX,
							IPdtLaunchAttributes.DEFAULT_TRACE_FILE_PREFIX));
			localTraceDir.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_LOCAL_TRACE_DIR,
							IPdtLaunchAttributes.DEFAULT_LOCAL_TRACE_DIR));
		} catch (CoreException e) {
			Debug.POLICY.logError(e, Messages.getString("PdtEnvironmentTab.InitializeFrom_Error_ReadingConf")); //$NON-NLS-1$
		}
	}

	/**
	 * Get the current project from an ILaunchConfiguration
	 * 
	 * @param configuration
	 * @return
	 */
	private String getProjectDirectory(ILaunchConfiguration configuration) {
		return selectedProject.getPath().toOSString();
		
		/*System.out.println(configuration.getLocation().toOSString());
		IFile file = configuration.getFile();
		if (file != null) {
			IContainer parent = file.getParent();
			if (parent != null) {
				parent.get
				containerName = parent.getFullPath().toOSString();
			}
			System.out.println(file.getFullPath().toOSString());
			return file.getFullPath().toOSString();
		}
		return null;*/
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// Basically uses configuration.setAttribute to pass attributes to launcher
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_COPY_XML_FILE, 
				copyXmlFile.getSelection());
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_DIR,
				remoteXmlDir.getString());
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_LOCAL_XML_FILE,
				localXmlFile.getString());
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_FILE, 
				remoteXmlFile.getString());
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_REMOTE_TRACE_DIR,
				remoteTraceFile.getString());
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX,
				traceFilePrefix.getString());
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_LOCAL_TRACE_DIR,
				localTraceDir.getString());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// configuration.setAttribute but using default values
		
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_COPY_XML_FILE, 
				IPdtLaunchAttributes.DEFAULT_COPY_XML_FILE);
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_DIR,
						IPdtLaunchAttributes.DEFAULT_REMOTE_XML_DIR);
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_LOCAL_XML_FILE,
				IPdtLaunchAttributes.DEFAULT_LOCAL_XML_FILE);
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_FILE, 
				IPdtLaunchAttributes.DEFAULT_REMOTE_XML_FILE);
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_REMOTE_TRACE_DIR,
						IPdtLaunchAttributes.DEFAULT_REMOTE_TRACE_DIR);
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX,
						IPdtLaunchAttributes.DEFAULT_TRACE_FILE_PREFIX);
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_LOCAL_TRACE_DIR,
				IPdtLaunchAttributes.DEFAULT_LOCAL_TRACE_DIR);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (!super.isValid(launchConfig)) {
			return false;
		}
		
		Debug.read();
		
		// Just check if the paths are valid unix paths.
		// launchConfig.getAttribute(attributeName, defaultValue)
		// For now just return true
		try {
				Boolean copyXmlFile = launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_COPY_XML_FILE, 
						IPdtLaunchAttributes.DEFAULT_COPY_XML_FILE);
			
				// Check XML file related fields
				if(copyXmlFile) {
					if(!validateLocalPath(launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_LOCAL_XML_FILE, IPdtLaunchAttributes.DEFAULT_LOCAL_XML_FILE), true)) {
						setErrorMessage(Messages.getString("PdtEnvironmentTab.IsValid_Error_LocalXmlFileInvalid")); //$NON-NLS-1$
						return false;
					} else if(!validateRemotePath(launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_DIR, IPdtLaunchAttributes.DEFAULT_REMOTE_XML_DIR))) {
						setErrorMessage(Messages.getString("PdtEnvironmentTab.IsValid_Error_RemoteXmlDirInvalid")); //$NON-NLS-1$
						return false;
					}
				} else {
					if(!validateRemotePath(launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_XML_FILE, IPdtLaunchAttributes.DEFAULT_REMOTE_XML_FILE))) {
						setErrorMessage(Messages.getString("PdtEnvironmentTab.IsValid_Error_RemoteXmlFileInvalid")); //$NON-NLS-1$
						return false;
					}
				}
				
				// Check trace file related checks
				String prefix = launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX, IPdtLaunchAttributes.DEFAULT_TRACE_FILE_PREFIX);
				if(!validateRemotePath(launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_TRACE_DIR, IPdtLaunchAttributes.DEFAULT_REMOTE_TRACE_DIR))) {
					setErrorMessage(Messages.getString("PdtEnvironmentTab.IsValid_Error_RemoteTraceDirInvalid")); //$NON-NLS-1$
					return false;			
				} else if(!validateLocalPath(launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_LOCAL_TRACE_DIR, IPdtLaunchAttributes.DEFAULT_LOCAL_TRACE_DIR), false)) {
					setErrorMessage(Messages.getString("PdtEnvironmentTab.IsValid_Error_LocalTraceDirInvalid")); //$NON-NLS-1$
					return false;
				} else if(prefix == null || prefix.length() == 0) {
					setErrorMessage(Messages.getString("PdtEnvironmentTab.IsValid_Error_PrefixInvalid")); //$NON-NLS-1$
					return false;
				}
				
		} catch (CoreException e) {
			Debug.POLICY.logError(e);
			return false;
		}
		
		setErrorMessage(null);
		return true;
	}
	
	static protected boolean validateRemotePath(String remotePathString) {
		if(remotePathString == null || remotePathString.length() == 0) {
			return false;
		}
		
		IPath remotePath = LinuxPath.fromString(remotePathString);
		if(!remotePath.isAbsolute()) {
			return false;
		}
		
		return true;
	}
	
	static protected Boolean validateLocalPath(String localPathString, Boolean canBeEmpty) {
		if((localPathString == null) || (!canBeEmpty && localPathString.length() == 0)) {
			return false;
		}
		
		if(localPathString.length() != 0) {
			File localFile = new File(localPathString);
			if(!localFile.exists()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean canSave() {
		return true;
	}
	
	/**
	 * Handler of the xml workspace browse button.
	 */
	/*public void handleXmlWorkspaceBrowseAction()
	{
		IFile xmlConfigFile = selectXmlConfigFile();
		
		if(xmlConfigFile == null) {
			return;
		}
		String xmlConfigFilename = xmlConfigFile.getProjectRelativePath().toOSString();
		localXmlPath.setString(xmlConfigFilename);
	}*/
	
	/**
	 * Displays a list of available xml files in the selected project workspace and let the
	 * user choose one.
	 * 
	 * @return an IFile containing the selected xml file.
	 */
	/*private IFile selectXmlConfigFile() {
		// Get the selected project.
		//this.
		//getProjectDirectory(configuration);
		
		ILabelProvider labelProvider = new LabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), labelProvider);
		dialog.setTitle("Configuration Files");
		dialog.setMessage("Select the xml configuration file");
		//dialog.setElements(projects);
		
		if (dialog.open() == Window.OK) {
			return (IFile) dialog.getFirstResult();
		}
		
		return null;
	}*/

	private void updateLaunchDlgDebugSupport() {
		Debug.read();
		try {
			updateLaunchConfigurationDialog();
		} catch(Exception e) {
			Debug.POLICY.logError(e);
		}
	}
	
	/**
	 * Simple listener of the tab's controls
	 * 
	 * @author Richard Maciel
	 *
	 */
	private class PdtModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			updateLaunchDlgDebugSupport();
		}
	}
	
	private class PdtSelectionListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			updateLaunchDlgDebugSupport();
		}
		
		public void widgetSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			updateLaunchDlgDebugSupport();
		}
	}
	
}
