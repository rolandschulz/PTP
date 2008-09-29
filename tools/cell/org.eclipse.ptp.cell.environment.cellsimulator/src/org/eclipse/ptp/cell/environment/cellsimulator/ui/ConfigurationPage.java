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
package org.eclipse.ptp.cell.environment.cellsimulator.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.AbstractTargetControl;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigFactory;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigurationBean;
import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalConfigurationBean;
import org.eclipse.ptp.cell.environment.cellsimulator.core.remote.RemoteConfigurationBean;
import org.eclipse.ptp.cell.simulator.SimulatorPlugin;
import org.eclipse.ptp.cell.simulator.conf.Parameters;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.core.IllegalConfigurationException;
import org.eclipse.ptp.cell.simulator.extensions.Architecture;
import org.eclipse.ptp.cell.simulator.extensions.ArchitectureManager;
import org.eclipse.ptp.cell.simulator.extensions.LaunchProfile;
import org.eclipse.ptp.cell.simulator.extensions.LaunchProfileManager;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.utils.ui.swt.AuthenticationFrame;
import org.eclipse.ptp.utils.ui.swt.AuthenticationFrameMold;
import org.eclipse.ptp.utils.ui.swt.ComboGroup;
import org.eclipse.ptp.utils.ui.swt.ComboGroupItem;
import org.eclipse.ptp.utils.ui.swt.ComboMold;
import org.eclipse.ptp.utils.ui.swt.ControlsRelationshipHandler;
import org.eclipse.ptp.utils.ui.swt.FileGroup;
import org.eclipse.ptp.utils.ui.swt.FileMold;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
import org.eclipse.ptp.utils.ui.swt.SpinnerGroup;
import org.eclipse.ptp.utils.ui.swt.SpinnerMold;
import org.eclipse.ptp.utils.ui.swt.TextGroup;
import org.eclipse.ptp.utils.ui.swt.TextMold;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;


/**
 * Dialog Page where the user can edit the target attributes.
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public class ConfigurationPage extends AbstractEnvironmentDialogPage {
	CommonConfigurationBean bean;

	public ConfigurationPage(String targetName, CommonConfigurationBean bean) {
		super(targetName);
		this.bean = bean;
		this.targetName = targetName;
	}

	private boolean hasError = false;
	
	private String targetName;
	
	private boolean availableAutomaticWorkDirectory;
	private boolean availableRemoteConnection;
	private boolean availableAutomaticNetwork;
	private boolean availableAutomaticPort;

	// Tab control that contains all tab items
	TabFolder tabFolder;
	// Tab items
	TabItem targetConnectionTabItem;
	TabItem showattributesMap;
	TabItem emulatedHardwareTabItem;
	TabItem simulatorOptionsTabItem;
	TabItem connectivityTabItem;
	TabItem launchTabItem;
	
	// Controls
	TextGroup targetNameText;
	
	AuthenticationFrame targetConnectionFrame;
	
	Frame hardwareConfig;
		ComboGroup architectureId;
		SpinnerGroup memorySize;
	Frame filesystemInfo;
		// normal
		ComboGroup rootImagePersistence;
		FileGroup rootImageJournalPath;
		JournalFileEnabler rootJournalFileEnabler;
		// advanced
		FileGroup kernelImagePath;
		FileGroup filesystemImagePath;
		Button extraImageInit;
		ControlsRelationshipHandler additionalStorageControlsHandler;
		JournalFileEnabler extraJournalFileEnabler;
		FileGroup extraImagePath;
		ComboGroup extraImageFilesystem;
		TextGroup extraImageMountpoint;
		ComboGroup extraImagePersistence;
		FileGroup extraImageJournalPath;
	Frame simulatorOptions;
		// normal
		Button showSimulatorGUI;
		Button consoleShowLinux;
		Button consoleShowSimulator;
		Button automaticWorkDirectory;
		FileGroup workDirectory;
		ControlsRelationshipHandler workDirectoryControlHandler;
		// advanced
		FileGroup simulatorBaseDirectory;
		ComboGroup launchProfileId;
		TextGroup extraCommandLineSwitches;
		TextGroup customizationScript;
		Button automaticPortConfig;
		TextGroup consoleSocketPort;
		TextGroup javaAPISocketPort;
		ControlsRelationshipHandler portAutoControlsHandler;
	Frame sshConnectionConfig;
		Button automaticNetwork;
		TextGroup macSimulator;
		TextGroup ipSimulator;
		TextGroup ipHost;
		ControlsRelationshipHandler connAutoControlsHandler;
	Frame authenticationInfo;
		Button automaticAutentication;
		TextGroup simulatorUserName;
		TextGroup simulatorPassword;
		TextGroup simulatorTimeout;
		ComboGroup simulatorCipherType;
		ControlsRelationshipHandler authAutoControlsHandler;
	Frame launchFrame;
		TextGroup systemWorkspaceGroup;
	
	class TargetConnectionModifyListener implements ModifyListener {
		int counter = 0;

		public synchronized void enable() {
			counter++;
		}

		public synchronized void disable() {
			counter--;
		}

		/**
		 * Update all attributes dependent on the text controls
		 */
		public void modifyText(ModifyEvent e) {
			if (counter < 0) {
				return;
			}
			readTargetConnectionControls();

			// updateButtons() will call is Valid(), that will call
			// validateFields()
			getContainer().updateButtons();
		}
	}

	class TextModifyListener implements ModifyListener {
		int counter = 0;
		public synchronized void enable() {
			counter++;
		}
		public synchronized void disable() {
			counter--;
		}
		/**
		 * Update all attributes dependent on the text controls
		 */
		public void modifyText(ModifyEvent e) {
			if (counter < 0) {
				return;
			}
			readTextControls();

			// updateButtons() will call is Valid(), that will call validateFields()
			getContainer().updateButtons();
		}
	}
	
	class ButtonSelectionListener extends SelectionAdapter {
		int counter = 0;
		public synchronized void enable() {
			counter++;
		}
		public synchronized void disable() {
			counter--;
		}
		/**
		 * Update all attributes dependent on the buttons controls
		 */
		public void widgetSelected(SelectionEvent e) {
			if (counter < 0) {
				return;
			}
			
			readButtonControls();
		
			// updateButtons() will call is Valid(), that will call validateFields()
			getContainer().updateButtons();
		}
	}
	
	class JournalFileEnabler {
		ComboGroup typeGroup;
		FileGroup journalGroup;
		Button masterButton;
		
		JournalFileEnabler(ComboGroup typeGroup, FileGroup journalGroup) {
			this(typeGroup, journalGroup, null);
		}
		
		JournalFileEnabler(ComboGroup typeGroup, FileGroup journalGroup, Button masterButton) {
			this.typeGroup = typeGroup;
			this.journalGroup = journalGroup;
			this.masterButton = masterButton;
			typeGroup.getCombo().addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					update();
				}	
			});
			if (masterButton != null) {
				masterButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						update();
					}
				});
			}
			update();
		}
				
		public void update() {
			if (masterButton != null) {
				if (! masterButton.getSelection()) {
					journalGroup.setEnabled(false);
					return;
				}
			}
			String id = typeGroup.getSelectionId();
			if (id == null) {
				journalGroup.setEnabled(false);
			} else {
				journalGroup.setEnabled(id.equals(CommonConfigurationBean.ID_PERSISTENCE_JOURNAL));
			};
		}
	}
	
	private TextModifyListener textModifyListener;
	private ButtonSelectionListener buttonSelectionListener;
	private TargetConnectionModifyListener targetConnectionModifyListener;
	
	/**
	 * Generate all controls inside the provided Composite.
	 */
	public void createControl(Composite parent) {

		if (availableRemoteConnection) {
			this.setDescription(Messages.ConfigurationPage_DialogDescription_RemoteSimulator);
			this.setTitle(Messages.ConfigurationPage_DialogTitle_RemoteSimulator);
		} else {
			this.setDescription(Messages.ConfigurationPage_DialogDescription_LocalSimulator);
			this.setTitle(Messages.ConfigurationPage_DialogTitle_LocalSimulator);
		}
		this.setErrorMessage(null);

		// Generate the top control, parent of all the following controls.
		GridLayout topLayout = new GridLayout();
		final Composite topControl = new Composite(parent, SWT.NONE);
		setControl(topControl);
		topControl.setLayout(topLayout);
					
		// Target name
		TextMold targetMold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL, Messages.ConfigurationPage_LabelTargetName);
		targetNameText = new TextGroup(topControl, targetMold);
		targetNameText.setString(targetName);
		
		// Tab
		tabFolder = new TabFolder(topControl, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		if (availableRemoteConnection) {
			// Tabitem for remote connection to target that will launch the simulator
			targetConnectionTabItem = new TabItem(tabFolder, SWT.NONE);
			targetConnectionTabItem.setText(Messages.ConfigurationPage_TabItemTitleRemoteHost);
			Composite tcComp = generateTabItemComposite(targetConnectionTabItem);
			createRemoteHostFrame(tcComp);
		}
		
		// Tabitem for hardware configuration
		emulatedHardwareTabItem = new TabItem(tabFolder, SWT.NONE);
		emulatedHardwareTabItem.setText(Messages.ConfigurationPage_TabItemTitleHardwareProperties);
		Composite hwComp = generateTabItemComposite(emulatedHardwareTabItem);
		createHardwareConfigFrame(hwComp);
		createFileSystemInfoFrame(hwComp);
		
		// Tabitem for simulator options
		simulatorOptionsTabItem = new TabItem(tabFolder, SWT.NONE);
		simulatorOptionsTabItem.setText(Messages.ConfigurationPage_TabItemTitleSimulatorProperties);
		createSimulatorOptionsFrame(generateTabItemComposite(simulatorOptionsTabItem));
		
		// Tabitem for connectivity
		connectivityTabItem = new TabItem(tabFolder, SWT.None);
		connectivityTabItem.setText(Messages.ConfigurationPage_TabItemTitleConnectivityProperties);
		Composite sshComp = generateTabItemComposite(connectivityTabItem);
		createSSHConnectionInfoFrame(sshComp);
		createSSHAuthenticationFrame(sshComp);
		
		// Tabitem for launch properties
		launchTabItem = new TabItem(tabFolder, SWT.None);
		launchTabItem.setText(Messages.ConfigurationPage_TabItemLaunchProperties);
		Composite launchComp = generateTabItemComposite(launchTabItem);
		createLaunchFrame(launchComp);
		
		// Create listeners
		textModifyListener = new TextModifyListener();
		buttonSelectionListener = new ButtonSelectionListener();
		
		fillControls();
		validateFields();
		registerListeners();
	}

	private Composite createLaunchFrame(Composite launchComp) {
		Frame frame = new Frame(launchComp, Messages.ConfigurationPage_Launch_FrameTitle);
		TextMold mold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_Launch_LabelSystemWorkspace);
		systemWorkspaceGroup = new TextGroup(frame.getComposite(), mold);
		return frame;
	}

	private Composite createRemoteHostFrame(Composite tcComp) {
		AuthenticationFrameMold amold = new AuthenticationFrameMold(Messages.ConfigurationPage_RemoteTarget_FrameTitle);
		amold.setLabelHideAdvancedOptions(Messages.ConfigurationPage_RemoteTarget_LabelHideAdvancedOptions);
		amold.setLabelHostAddress(Messages.ConfigurationPage_RemoteTarget_LabelHostAddress);
		amold.setLabelHostPort(Messages.ConfigurationPage_RemoteTarget_LabelHostPort);
		amold.setLabelIsPasswordBased(Messages.ConfigurationPage_RemoteTarget_LabelIsPasswordBased);
		amold.setLabelIsPublicKeyBased(Messages.ConfigurationPage_RemoteTarget_LabelIsPublicKeyBased);
		amold.setLabelPassphrase(Messages.ConfigurationPage_RemoteTarget_LabelPassphrase);
		amold.setLabelPassword(Messages.ConfigurationPage_RemoteTarget_LabelPassword);
		amold.setLabelPublicKeyPath(Messages.ConfigurationPage_RemoteTarget_LabelPublicKeyPath);
		amold.setLabelPublicKeyPathButton(Messages.ConfigurationPage_RemoteTarget_LabelPublicKeyPathButton);
		amold.setLabelPublicKeyPathTitle(Messages.ConfigurationPage_RemoteTarget_LabelPublicKeyPathTitle);
		amold.setLabelShowAdvancedOptions(Messages.ConfigurationPage_RemoteTarget_LabelShowAdvancedOptions);
		amold.setLabelTimeout(Messages.ConfigurationPage_RemoteTarget_LabelTimeout);
		amold.setLabelUserName(Messages.ConfigurationPage_RemoteTarget_LabelUserName);
		targetConnectionFrame = new AuthenticationFrame(tcComp, amold);
		targetConnectionFrame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		return targetConnectionFrame;
	}

	/**
	 * Tabs composites only accept one control, so this method generates composites, insert the in the tabitem
	 * and return them so other controls can be instanciated inside the tabItem. 
	 * 
	 * @return
	 */
	private Composite generateTabItemComposite(TabItem tabitem) {
		Composite comp = new Composite(tabFolder, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		//glayout.marginWidth = 0;
		comp.setLayout(glayout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL));
		tabitem.setControl(comp);
		return comp;
	}
	
	private void createSSHConnectionInfoFrame(Composite parent) {
//		 Set frame
		FrameMold fmold = new FrameMold(Messages.ConfigurationPage_SshConnection_FrameTitle, 2, false);
		sshConnectionConfig = new Frame(parent, fmold);
//		sshConnectionConfig.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		automaticNetwork = new Button(sshConnectionConfig.getTopUserReservedComposite(), SWT.CHECK);
		automaticNetwork.setText(Messages.ConfigurationPage_SshConnection_LabelAutomaticNetwork);
		GridData netGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		netGridData.horizontalSpan = 2;
		automaticNetwork.setLayoutData(netGridData);
		/*
		 * Network options
		 */
		
		TextMold tmold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_SshConnection_LabelMacAddres);
		macSimulator = new TextGroup(sshConnectionConfig.getTopUserReservedComposite(), tmold);
		
		tmold.setLabel(Messages.ConfigurationPage_SshConnection_LabelSimulatorIP);
		ipSimulator = new TextGroup(sshConnectionConfig.getTopUserReservedComposite(), tmold);
		
		tmold.setLabel(Messages.ConfigurationPage_SshConnection_LabelHostIP);
		ipHost = new TextGroup(sshConnectionConfig.getTopUserReservedComposite(), tmold);
		
//		 Create handler to enable/disable custom connection config
		Control [] cList = {macSimulator, ipSimulator, ipHost}; 
		connAutoControlsHandler = new ControlsRelationshipHandler(automaticNetwork, cList, false);
		
		// Manage the controls state based on the loaded values.
		connAutoControlsHandler.manageDependentControls(automaticNetwork);
	}
	
	private void createSSHAuthenticationFrame(Composite parent) {
		
//		 Set frame
		FrameMold authMold = new FrameMold(Messages.ConfigurationPage_Authentication_FrameTitle, 2, true);
		authenticationInfo = new Frame(parent, authMold);
//		authenticationInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		/*
		 * Authentication options
		 */
		
		
		automaticAutentication = new Button(authenticationInfo.getTopUserReservedComposite(), SWT.CHECK);
		automaticAutentication.setText(Messages.ConfigurationPage_Authentication_LabelAutomaticAuthentication);
		GridData authGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		authGridData.horizontalSpan = 2;
		automaticAutentication.setLayoutData(authGridData);
		
		TextMold tmold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_Authentication_LabelLogin);
		simulatorUserName = new TextGroup(authenticationInfo.getTopUserReservedComposite(), tmold);
		
		tmold.setLabel(Messages.ConfigurationPage_Authentication_LabelPassword);
		tmold.addBitmask(TextMold.PASSWD_FIELD);
		simulatorPassword = new TextGroup(authenticationInfo.getTopUserReservedComposite(), tmold);
		tmold.removeBitmask(TextMold.PASSWD_FIELD);
		
		tmold.setLabel(Messages.ConfigurationPage_Authentication_LabelTimeout);
		tmold.addBitmask(TextMold.LIMIT_SIZE | TextMold.WIDTH_PROPORTIONAL_NUM_CHARS);
		tmold.setTextFieldWidth(6);
		simulatorTimeout = new TextGroup(authenticationInfo.getBottomUserReservedComposite(), tmold);
		
		ComboMold cmold = new ComboMold(ComboMold.GRID_DATA_SPAN | 
				ComboMold.GRID_DATA_ALIGNMENT_FILL | ComboMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_Authentication_CipherType);
		simulatorCipherType = new ComboGroup(authenticationInfo.getBottomUserReservedComposite(), cmold);
		
//		 Create handler to enable/disable additional filesystem storage controls
		Control [] cList = {simulatorUserName, simulatorPassword}; 
		authAutoControlsHandler = new ControlsRelationshipHandler(automaticAutentication, cList, false);
		
		// Manage the controls state based on the loaded values.
		authAutoControlsHandler.manageDependentControls(automaticAutentication);
		
	}

	private void createSimulatorOptionsFrame(Composite parent) {
		// Set frame
		FrameMold fmold = new FrameMold(Messages.ConfigurationPage_SimulatorOptions_FrameTitle, 2, true);
		simulatorOptions = new Frame(parent, fmold);
//		simulatorOptions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		/*
		 * Common options 
		 */
		
		// show sim control window option
		showSimulatorGUI = new Button(simulatorOptions.getTopUserReservedComposite(), SWT.CHECK);
		showSimulatorGUI.setText(Messages.ConfigurationPage_SimulatorOptions_LabelShowGUI);
		showSimulatorGUI.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		// show linux console
		consoleShowLinux = new Button(simulatorOptions.getTopUserReservedComposite(), SWT.CHECK);
		consoleShowLinux.setText(Messages.ConfigurationPage_SimulatorOptions_LabelShowLinuxConsole);
		consoleShowLinux.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		// show TCL console
		consoleShowSimulator = new Button(simulatorOptions.getTopUserReservedComposite(), SWT.CHECK);
		consoleShowSimulator.setText(Messages.ConfigurationPage_SimulatorOptions_LabelShowTCLConsole);
		consoleShowSimulator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		// an empty placeholder
		new Label(simulatorOptions.getTopUserReservedComposite(), SWT.NONE);
		
		// Working directory
		if (availableAutomaticWorkDirectory) {
			automaticWorkDirectory = new Button(simulatorOptions.getTopUserReservedComposite(), SWT.CHECK);
			automaticWorkDirectory.setText(Messages.DefaultWorkingDirectoryButtonText);
			automaticWorkDirectory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		FileMold fsmold = new FileMold(FileMold.GRID_DATA_ALIGNMENT_FILL |
				FileMold.GRID_DATA_GRAB_EXCESS_SPACE | FileMold.GRID_DATA_SPAN 
				| FileMold.DIRECTORY_SELECTION,
				Messages.ConfigurationPage_SimulatorOptions_LabelWorkingDirectory, Messages.ConfigurationPage_SimulatorOptions_WorkingDirectoryDialog_Title, Messages.ConfigurationPage_SimulatorOptions_WorkingDirectoryDialog_Description);
		workDirectory = new FileGroup(simulatorOptions.getTopUserReservedComposite(), fsmold);
		workDirectory.setButtonEnabled(! availableRemoteConnection);
		if (automaticWorkDirectory != null) {
			workDirectoryControlHandler = new ControlsRelationshipHandler(automaticWorkDirectory, workDirectory, false);
			workDirectoryControlHandler.manageDependentControls(automaticWorkDirectory);
		}
		
		/*
		 * Advanced options
		 */
		
		// Base path
		fsmold.setLabel(Messages.ConfigurationPage_SimulatorOptions_LabelInstallationPath);
		fsmold.setDialogLabel(Messages.ConfigurationPage_SimulatorOptions_InstallationPathDialog_Title);
		fsmold.setDialogMessage(Messages.ConfigurationPage_SimulatorOptions_InstallationPathDialog_Description);
		simulatorBaseDirectory = new FileGroup(simulatorOptions.getBottomUserReservedComposite(), fsmold);
		simulatorBaseDirectory.setButtonEnabled(! availableRemoteConnection);
		
		// Launch profile
		ComboMold cmold = new ComboMold(ComboMold.GRID_DATA_ALIGNMENT_FILL | 
				ComboMold.GRID_DATA_GRAB_EXCESS_SPACE | ComboMold.GRID_DATA_SPAN, 
				Messages.ConfigurationPage_SimulatorOptions_LabelLaunchProfile);
		launchProfileId = new ComboGroup(simulatorOptions.getBottomUserReservedComposite(), cmold);
		// Fetch possible values and set its index
		LaunchProfileManager lManager = SimulatorPlugin.getLaunchProfileManager();
		LaunchProfile [] profilss = lManager.getLaunchProfiles();
		for(int i=0; i < profilss.length; i++) {
			// Build it on a ComboGroupItem (using id and name) and insert on the ComboGroup  
			ComboGroupItem citem = new ComboGroupItem(profilss[i].getId(), profilss[i].getName());
			launchProfileId.add(citem);
		}
		
		// Extra switches
		TextMold tmold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | 
				TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.GRID_DATA_SPAN,
				Messages.ConfigurationPage_SimulatorOptions_LabelExtraCommandLineSwitches);
		extraCommandLineSwitches = new TextGroup(simulatorOptions.getBottomUserReservedComposite(), tmold);
		
		// Custom scripts
		tmold.addBitmask(TextMold.LABELABOVE | TextMold.MULTILINE_TEXT);
		tmold.setNumberOfLines(6);
		tmold.setLabel(Messages.ConfigurationPage_SimulatorOptions_LabelCustoScript);
		customizationScript = new TextGroup(simulatorOptions.getBottomUserReservedComposite(), tmold);
		
		// Console and Java API Control ports
		automaticPortConfig = new Button(simulatorOptions.getBottomUserReservedComposite(), SWT.CHECK);
		automaticPortConfig.setText(Messages.ConfigurationPage_SimulatorOptions_LabelAutomaticPorts);
		GridData portOptGD = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		portOptGD.horizontalSpan = 2;
		automaticPortConfig.setLayoutData(portOptGD);
		
		TextMold tmoldSimple = new TextMold(TextMold.WIDTH_PROPORTIONAL_NUM_CHARS | TextMold.LIMIT_SIZE,
				Messages.ConfigurationPage_SimulatorOptions_LabelConsolePort);
		tmoldSimple.setTextFieldWidth(6);
		consoleSocketPort = new TextGroup(simulatorOptions.getBottomUserReservedComposite(), tmoldSimple);

		if (Parameters.doUseJavaAPI()) {
			tmoldSimple.setLabel(Messages.ConfigurationPage_SimulatorOptions_LabelJavaAPIPort);
			javaAPISocketPort = new TextGroup(simulatorOptions.getBottomUserReservedComposite(), tmoldSimple);
		}
		
		// Set the port controls handler
		Control [] portAutoSlaves = null;
		if (Parameters.doUseJavaAPI()) {
			portAutoSlaves = new Control[] {consoleSocketPort, javaAPISocketPort};
			if (Parameters.doHandleJavaApiGuiIssue()) {
				showSimulatorGUI.setEnabled(false);
			}
		} else {
			portAutoSlaves = new Control[] {consoleSocketPort};			
		}
		portAutoControlsHandler = new ControlsRelationshipHandler(automaticPortConfig, portAutoSlaves, false);
		portAutoControlsHandler.manageDependentControls(automaticPortConfig);
	}

	/**
	 * Generates the interface for disk and filesystem information. This includes kernel image, persistence type and
	 * additional storage attributes.
	 * 
	 * @param parent
	 */
	private void createFileSystemInfoFrame(Composite parent) {
		// Set frame
		FrameMold fmold = new FrameMold(Messages.ConfigurationPage_FileSystem_FrameTitle, 1, true);
		filesystemInfo = new Frame(parent, fmold);
//		filesystemInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		/*
		 * Common options
		 */
		
		// Storage type
		ComboGroupItem [] storageTypes = {new ComboGroupItem(CommonConfigurationBean.ID_PERSISTENCE_DISCARD, Messages.ConfigurationPage_FileSystem_OptionNonPersistent),
									 new ComboGroupItem(CommonConfigurationBean.ID_PERSISTENCE_WRITE, Messages.ConfigurationPage_FileSystem_OptionPersistent),
									 new ComboGroupItem(CommonConfigurationBean.ID_PERSISTENCE_JOURNAL, Messages.ConfigurationPage_FileSystem_OptionJournaled) };
		String storageTypeTooltip = Messages.ConfigurationPage_FileSystem_ToolTipRootImagePersistence;
		ComboMold storageTypeComboMold = new ComboMold(ComboMold.GRID_DATA_GRAB_EXCESS_SPACE | 
				ComboMold.GRID_DATA_ALIGNMENT_FILL | ComboMold.HASTOOLTIP, 
				Messages.ConfigurationPage_FileSystem_LabelRootImagePersistence);
		storageTypeComboMold.setTooltip(storageTypeTooltip);
		rootImagePersistence = new ComboGroup(filesystemInfo.getTopUserReservedComposite(), storageTypeComboMold);
		// Fill values
		rootImagePersistence.add(storageTypes[0]); rootImagePersistence.add(storageTypes[1]); rootImagePersistence.add(storageTypes[2]); 
		
		// Journal file
		FileMold fsmold = new FileMold(FileMold.GRID_DATA_ALIGNMENT_FILL | 
				FileMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_FileSystem_LabelJournalFile, Messages.ConfigurationPage_FileSystem_JournalFileDialogTitle, 
				Messages.ConfigurationPage_FileSystem_JournalFileDialogDescription);
		rootImageJournalPath = new FileGroup(filesystemInfo.getTopUserReservedComposite(), fsmold);
		rootImageJournalPath.setButtonEnabled(! availableRemoteConnection);
		
		rootJournalFileEnabler = new JournalFileEnabler(rootImagePersistence, rootImageJournalPath);

		/*
		 * Advanced options
		 */
		
		// Kernel image file
		fsmold.setLabel(Messages.ConfigurationPage_FileSystem_LabelKernelImage);
		fsmold.setDialogLabel(Messages.ConfigurationPage_FileSystem_KernelFileDialog_Title);
		fsmold.setDialogMessage(Messages.ConfigurationPage_FileSystem_KernelFileDialog_Description);
		kernelImagePath = new FileGroup(filesystemInfo.getTopUserReservedComposite(), fsmold);
		kernelImagePath.setButtonEnabled(! availableRemoteConnection);
		
		// File system image
		fsmold.setLabel(Messages.ConfigurationPage_FileSystem_LabelRoot);
		fsmold.setDialogLabel(Messages.ConfigurationPage_FileSystem_RootFileDialog_Title);
		fsmold.setDialogMessage(Messages.ConfigurationPage_FileSystem_RootFileDialog_Description);
		filesystemImagePath = new FileGroup(filesystemInfo.getTopUserReservedComposite(), fsmold);
		filesystemImagePath.setButtonEnabled(! availableRemoteConnection);
		
		// Mount additional Storage?
		extraImageInit = new Button(filesystemInfo.getBottomUserReservedComposite(), SWT.CHECK);
		extraImageInit.setText(Messages.ConfigurationPage_FileSystem_LabelEnableExtraStorage);
		extraImageInit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		// Image localization
		FileMold imagePathMold = new FileMold(FileMold.GRID_DATA_GRAB_EXCESS_SPACE | 
				FileMold.GRID_DATA_ALIGNMENT_FILL, 
				Messages.ConfigurationPage_FileSystem_LabelExtraImagePath,
				Messages.ConfigurationPage_FileSystem_ExtraImagePathFileDialog_Title, 
				Messages.ConfigurationPage_FileSystem_ExtraImagePathFileDialog_Description);
		extraImagePath = new FileGroup(filesystemInfo.getBottomUserReservedComposite(), imagePathMold);
		
		// Image filesystem typetooltip
		ComboMold fsTypeMold = new ComboMold(ComboMold.GRID_DATA_GRAB_EXCESS_SPACE | 
				ComboMold.GRID_DATA_ALIGNMENT_FILL, 
				Messages.ConfigurationPage_FileSystem_LabelExtraFilesystemType);
		extraImageFilesystem = new ComboGroup(filesystemInfo.getBottomUserReservedComposite(), fsTypeMold);
		// Fill values
		ComboGroupItem [] fsTypes = {
				new ComboGroupItem(CommonConfigurationBean.ID_FILESYSTEM_EXT3, 
						Messages.ConfigurationPage_FileSystem_TypeOption_Ext3), 
				new ComboGroupItem(CommonConfigurationBean.ID_FILESYSTEM_EXT2, 
						Messages.ConfigurationPage_FileSystem_TypeOption_Ext2),
				new ComboGroupItem(CommonConfigurationBean.ID_FILESYSTEM_ISO9660, 
						Messages.ConfigurationPage_FileSystem_TypeOption_Iso9660)
				};
		extraImageFilesystem.add(fsTypes[0]); extraImageFilesystem.add(fsTypes[1]); extraImageFilesystem.add(fsTypes[2]);
		
		// Image mountpoint
		TextMold tmold = new TextMold(TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.GRID_DATA_ALIGNMENT_FILL,
				Messages.ConfigurationPage_FileSystem_LabelExtraMountPoint);
		extraImageMountpoint = new TextGroup(filesystemInfo.getBottomUserReservedComposite(), tmold);
		extraImageMountpoint.setButtonEnabled(! availableRemoteConnection);
		
		// Mold comes from the combo control above
		extraImagePersistence = new ComboGroup(filesystemInfo.getBottomUserReservedComposite(), storageTypeComboMold);
		// Fill values
		extraImagePersistence.add(storageTypes[0]); extraImagePersistence.add(storageTypes[1]); extraImagePersistence.add(storageTypes[2]); 

		fsmold.setLabel(Messages.ConfigurationPage_FileSystem_LabelExtraJournalFile);
		fsmold.setDialogLabel(Messages.ConfigurationPage_FileSystem_ExtraJournalFileDialog_Title);
		fsmold.setDialogLabel(Messages.ConfigurationPage_FileSystem_ExtraJournalFileDialog_Description);
		extraImageJournalPath = new FileGroup(filesystemInfo.getBottomUserReservedComposite(),
				fsmold);
		extraImageJournalPath.setButtonEnabled(! availableRemoteConnection);
		
		// Create handler to enable/disable additional filesystem storage controls
		//Control [] cList = {device, additionalStorageType, additionalJournalFile};
		Control [] cList = {extraImagePath, extraImageFilesystem, extraImageMountpoint, extraImagePersistence};
		additionalStorageControlsHandler = new ControlsRelationshipHandler(extraImageInit, cList, true);
		
		// Manage the controls state based on the loaded values.
		additionalStorageControlsHandler.manageDependentControls(extraImageInit);
		extraJournalFileEnabler = new JournalFileEnabler(extraImagePersistence, extraImageJournalPath, extraImageInit);

	}

	/**
	 * Generates the interface for Hardware Config parameters. This includes the simulated processor and
	 * memory size.
	 * 
	 * @param parent Composite Parent of this frame.
	 */
	private void createHardwareConfigFrame(Composite parent) {
		// Set frame
		FrameMold fmold = new FrameMold(Messages.ConfigurationPage_Hardware_FrameTitle, 2, false);
		hardwareConfig = new Frame(parent, fmold);
//		hardwareConfig.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		//mainTab.setControl(hardwareConfig);
		
		// Processor type group
		ComboMold cmold = new ComboMold(ComboMold.GRID_DATA_GRAB_EXCESS_SPACE | 
				ComboMold.GRID_DATA_ALIGNMENT_FILL, Messages.ConfigurationPage_Hardware_LabelArchitectureType);
		architectureId = new ComboGroup(hardwareConfig.getTopUserReservedComposite(), cmold);
		// Get the list of available processors
		ArchitectureManager aManager = SimulatorPlugin.getArchitectureManager();
		Architecture [] archs = aManager.getArchitectures();
		for(int i=0; i < archs.length; i++) {
			// Build it on a ComboGroupItem (using id and name) and insert on the ComboGroup  
			ComboGroupItem citem = new ComboGroupItem(archs[i].getId(), archs[i].getName());
			architectureId.add(citem);
		}
		
		// Ram amount
		SpinnerMold smold = new SpinnerMold(SpinnerMold.NONE, Messages.ConfigurationPage_Hardware_LabelMemorySize, 0, SpinnerGroup.MAX_VALUE, 1);
		memorySize = new SpinnerGroup(hardwareConfig.getTopUserReservedComposite(), smold);
		
	}

	/**
	 * Create simulator parameters and target config objects, which will validate the attributes inside the map
	 * that the configFactory keeps. If an attribute is invalid, throws an exception.
	 *
	 */
	public void validateFields() {
		/*
		 * First, assume no error nor warning.
		 * Then test target configuration, and if no error is found,
		 * then check simulator configuration.
		 */
		
		try {
			if (targetConnectionFrame != null) {
				targetConnectionFrame.validateFields();
			}
			CommonConfigFactory factory = bean.createFactory();
			factory.createTargetConfig();
			ISimulatorParameters parameters = factory.createSimulatorParameters();
			parameters.verify();
			hasError = false;
			setErrorMessage(null);
		} catch (CoreException e) {
			hasError = true;
			setErrorMessage(e.getLocalizedMessage());
		} catch (IllegalConfigurationException e) {
			hasError = true;
			setErrorMessage(e.getLocalizedMessage());
		}
	}

	public Map getAttributes() {
		return bean.getMap();
	}

	public boolean isValid() {
		validateFields();
		return ! hasError;
	}

	public String getName() {
	    return targetName;
	}

	private void readTextControls() {
		targetName = targetNameText.getString();
		
		ControlAttributes attributes = bean.getAttributes();
		
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_MEMORY_SIZE, String.valueOf(memorySize.getValue()));
		
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_SIMULATOR_BASE_DIRECTORY, simulatorBaseDirectory.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_WORK_DIRECTORY, workDirectory.getString());
		
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_EXTRA_COMMAND_LINE_SWITCHES, extraCommandLineSwitches.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_USERNAME, simulatorUserName.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_PASSWORD, simulatorPassword.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_TIMEOUT, simulatorTimeout.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_SIMULATOR_CIPHER_TYPE, simulatorCipherType.getSelectionId());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_IP_HOST, ipHost.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_IP_SIMULATOR, ipSimulator.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_MAC_SIMULATOR, macSimulator.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_EXTRA_IMAGE_PATH, extraImagePath.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_EXTRA_IMAGE_JOURNAL_PATH, extraImageJournalPath.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_KERNEL_IMAGE_PATH, kernelImagePath.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_ROOT_IMAGE_PATH, filesystemImagePath.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_ROOT_IMAGE_JOURNAL_PATH, rootImageJournalPath.getString());
		if (javaAPISocketPort != null) {
			attributes.setStringAttribute(CommonConfigurationBean.ATTR_JAVA_API_SOCKET_PORT, javaAPISocketPort.getString());
		}
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_CONSOLE_SOCKET_PORT, consoleSocketPort.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_CUSTOMIZATION_SCRIPT, customizationScript.getString());
		
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_PROFILE_ID, launchProfileId.getSelectedItem().getId());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_ARCHITECTURE_ID, 
				architectureId.getSelectedItem().getId());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_EXTRA_IMAGE_PERSISTENCE, 
				extraImagePersistence.getSelectedItem().getId());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_EXTRA_IMAGE_TYPE, 
				extraImageFilesystem.getSelectionId());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_EXTRA_IMAGE_PATH, 
				extraImagePath.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_EXTRA_IMAGE_MOUNTPOINT, 
				extraImageMountpoint.getString());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_ROOT_IMAGE_PERSISTENCE, 
				rootImagePersistence.getSelectedItem().getId());
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_SYSTEM_WORKSPACE,
				systemWorkspaceGroup.getString());
	}

	private void readButtonControls() {
		ControlAttributes attributes = bean.getAttributes();
		
		attributes.setBooleanAttribute(CommonConfigurationBean.ATTR_EXTRA_IMAGE_INIT, extraImageInit.getSelection());
		attributes.setBooleanAttribute(CommonConfigurationBean.ATTR_SHOW_SIMULATOR_GUI, showSimulatorGUI.getSelection());
		attributes.setBooleanAttribute(CommonConfigurationBean.ATTR_AUTOMATIC_AUTHENTICATION, automaticAutentication.getSelection());
		attributes.setBooleanAttribute(CommonConfigurationBean.ATTR_AUTOMATIC_PORTCONFIG, automaticPortConfig.getSelection());
		attributes.setBooleanAttribute(CommonConfigurationBean.ATTR_AUTOMATIC_NETWORK, automaticNetwork.getSelection());
		attributes.setBooleanAttribute(CommonConfigurationBean.ATTR_CONSOLE_SHOW_LINUX, consoleShowLinux.getSelection());
		attributes.setBooleanAttribute(CommonConfigurationBean.ATTR_CONSOLE_SHOW_SIMULATOR, consoleShowSimulator.getSelection());

		// this is registered in both listeners
		attributes.setStringAttribute(CommonConfigurationBean.ATTR_MEMORY_SIZE, String.valueOf(memorySize.getValue()));
		
		if (automaticWorkDirectory != null) {
			attributes.setBooleanAttribute(LocalConfigurationBean.AUTOMATIC_WORK_DIRECTORY, automaticWorkDirectory.getSelection());
		}
	}
	
	private void readTargetConnectionControls() {
		ControlAttributes attributes = bean.getAttributes();
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_LOGIN_USERNAME, targetConnectionFrame.getUserName());
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_LOGIN_PASSWORD, targetConnectionFrame.getPassword());
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_CONNECTION_ADDRESS, targetConnectionFrame.getHostAddress());
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_CONNECTION_PORT, Integer.toString(targetConnectionFrame.getHostPort()));
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_KEY_PATH, targetConnectionFrame.getPublicKeyPath());	
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_KEY_PASSPHRASE, targetConnectionFrame.getPassphrase());
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_TIMEOUT, Integer.toString(targetConnectionFrame.getTimeout()));
		attributes.setStringAttribute(RemoteConfigurationBean.ATTR_REMOTE_CIPHER_TYPE, targetConnectionFrame.getSelectedCipherType().getId());
		attributes.setBooleanAttribute(RemoteConfigurationBean.ATTR_REMOTE_IS_PASSWORD_AUTH, targetConnectionFrame.isPasswordBased());
	}

	/**
	 * Register all controls to their respective listeners
	 * 
	 */
	private void registerListeners() {
		// Generate lists of containing controls of the same kind, then iterates
		// over them and register them in the
		// appropriate listeners.
		Text[] textArray = null;
		if (javaAPISocketPort != null) {
			textArray = new Text [] { targetNameText.getText(),
				rootImageJournalPath.getText(), kernelImagePath.getText(),
				filesystemImagePath.getText(), extraImagePath.getText(),
				extraImageJournalPath.getText(), extraImageMountpoint.getText(),
				workDirectory.getText(),
				simulatorBaseDirectory.getText(),
				extraCommandLineSwitches.getText(),
				customizationScript.getText(), consoleSocketPort.getText(),
				// ***
				javaAPISocketPort.getText(),
				// ***
				macSimulator.getText(),
				ipSimulator.getText(), ipHost.getText(),
				simulatorUserName.getText(), simulatorPassword.getText(),
				simulatorTimeout.getText() 
			};
		} else {
			textArray = new Text [] { targetNameText.getText(),
				rootImageJournalPath.getText(), kernelImagePath.getText(),
				filesystemImagePath.getText(), extraImagePath.getText(),
				extraImageMountpoint.getText(),
				extraImageJournalPath.getText(), workDirectory.getText(),
				simulatorBaseDirectory.getText(),
				extraCommandLineSwitches.getText(),
				customizationScript.getText(), consoleSocketPort.getText(),
				// ***
				// ***
				macSimulator.getText(),
				ipSimulator.getText(), ipHost.getText(),
				simulatorUserName.getText(), simulatorPassword.getText(),
				simulatorTimeout.getText() 
			};
		}
		Button[] btnArray = { extraImageInit, showSimulatorGUI,
				consoleShowLinux, consoleShowSimulator, automaticNetwork,
				automaticAutentication, automaticPortConfig };
		Combo[] cbArray = { architectureId.getCombo(),
				rootImagePersistence.getCombo(),
				extraImagePersistence.getCombo(), launchProfileId.getCombo(),
				simulatorCipherType.getCombo(), extraImageFilesystem.getCombo()};

		for (int i = 0; i < textArray.length; i++) {
			Text text = textArray[i];
			text.addModifyListener(textModifyListener);
		}
		for (int i = 0; i < btnArray.length; i++) {
			Button button = btnArray[i];
			button.addSelectionListener(buttonSelectionListener);
			}
		for (int i = 0; i < cbArray.length; i++) {
			Combo combo = cbArray[i];
			combo.addModifyListener(textModifyListener);
		}
		memorySize.getSpinner().addModifyListener(textModifyListener);
		memorySize.getSpinner().addSelectionListener(buttonSelectionListener);
		if (targetConnectionFrame != null) {
			targetConnectionModifyListener = new TargetConnectionModifyListener();
			targetConnectionFrame
					.addModifyListener(targetConnectionModifyListener);
		}
		if (automaticWorkDirectory != null) {
			automaticWorkDirectory.addSelectionListener(buttonSelectionListener);
		}
	}

	private void fillControls() {
		// textModifyListener.disable();
		// buttonSelectionListener.disable();
		// if (targetConnectionModifyListener != null)
		// targetConnectionModifyListener.disable();

		ControlAttributes attributes = bean.getAttributes();

		simulatorBaseDirectory
				.setString(attributes
						.getString(CommonConfigurationBean.ATTR_SIMULATOR_BASE_DIRECTORY));
		workDirectory.setString(attributes
				.getString(CommonConfigurationBean.ATTR_WORK_DIRECTORY));
		if (automaticWorkDirectory != null) {
			automaticWorkDirectory.setSelection(attributes.getBoolean(LocalConfigurationBean.AUTOMATIC_WORK_DIRECTORY));
			workDirectoryControlHandler.manageDependentControls(automaticWorkDirectory);
		}
		architectureId.selectIndexUsingID(attributes
				.getString(CommonConfigurationBean.ATTR_ARCHITECTURE_ID));
		memorySize.setValue(attributes
				.getInteger(CommonConfigurationBean.ATTR_MEMORY_SIZE));
		launchProfileId.setSelectionIndex(attributes
				.getInteger(CommonConfigurationBean.ATTR_PROFILE_ID));
		extraCommandLineSwitches
				.setString(attributes
						.getString(CommonConfigurationBean.ATTR_EXTRA_COMMAND_LINE_SWITCHES));
		showSimulatorGUI.setSelection(attributes
				.getBoolean(CommonConfigurationBean.ATTR_SHOW_SIMULATOR_GUI));
		automaticAutentication
				.setSelection(attributes
						.getBoolean(CommonConfigurationBean.ATTR_AUTOMATIC_AUTHENTICATION));
		authAutoControlsHandler.manageDependentControls(automaticAutentication);
		simulatorUserName.setString(attributes
				.getString(CommonConfigurationBean.ATTR_USERNAME));
		simulatorPassword.setString(attributes
				.getString(CommonConfigurationBean.ATTR_PASSWORD));
		simulatorTimeout.setString(String.valueOf(attributes
				.getInteger(CommonConfigurationBean.ATTR_TIMEOUT)));
		
		// Get the list of available cipher types.
//		 and fill the combobox with their names
		Map cipherMap = AbstractTargetControl.getCipherTypesMap();
		Set cKeySet = cipherMap.keySet();
		for(Iterator it = cKeySet.iterator(); it.hasNext();) {
			String key = (String)it.next();
			String value = (String)cipherMap.get(key);
			
			simulatorCipherType.add(new ComboGroupItem(key, value));
		}
		// Select the cipher type based on the attributes map.
		simulatorCipherType.selectIndexUsingID(attributes.getString(CommonConfigurationBean.ATTR_SIMULATOR_CIPHER_TYPE));
		
		if (availableAutomaticNetwork) {
			automaticNetwork
					.setSelection(attributes
							.getBoolean(CommonConfigurationBean.ATTR_AUTOMATIC_NETWORK));
		} else {
			automaticNetwork.setSelection(false);
			automaticNetwork.setEnabled(false);
		}
		connAutoControlsHandler.manageDependentControls(automaticNetwork);
		ipHost.setString(attributes
				.getString(CommonConfigurationBean.ATTR_IP_HOST));
		ipSimulator.setString(attributes
				.getString(CommonConfigurationBean.ATTR_IP_SIMULATOR));
		macSimulator.setString(attributes
				.getString(CommonConfigurationBean.ATTR_MAC_SIMULATOR));
		extraImageInit.setSelection(attributes
				.getBoolean(CommonConfigurationBean.ATTR_EXTRA_IMAGE_INIT));
		additionalStorageControlsHandler
				.manageDependentControls(extraImageInit);
		extraImagePath.setString(attributes
				.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_PATH));
		extraImagePersistence
				.setSelectionIndex(attributes
						.getInteger(CommonConfigurationBean.ATTR_EXTRA_IMAGE_PERSISTENCE));
		extraImageJournalPath
				.setString(attributes
						.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_JOURNAL_PATH));
		extraImagePath.setString(attributes.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_PATH));
		extraImageMountpoint.setString(attributes.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_MOUNTPOINT));
		extraImageFilesystem.selectIndexUsingID(
				attributes.getString(CommonConfigurationBean.ATTR_EXTRA_IMAGE_TYPE));
		
		kernelImagePath.setString(attributes
				.getString(CommonConfigurationBean.ATTR_KERNEL_IMAGE_PATH));
		filesystemImagePath.setString(attributes
				.getString(CommonConfigurationBean.ATTR_ROOT_IMAGE_PATH));
		rootImagePersistence
				.selectIndexUsingID(attributes
						.getString(CommonConfigurationBean.ATTR_ROOT_IMAGE_PERSISTENCE));
		rootImageJournalPath
				.setString(attributes
						.getString(CommonConfigurationBean.ATTR_ROOT_IMAGE_JOURNAL_PATH));
		if (javaAPISocketPort != null) {
			javaAPISocketPort
					.setString(String
							.valueOf(attributes
									.getInteger(CommonConfigurationBean.ATTR_JAVA_API_SOCKET_PORT)));
		}
		consoleSocketPort.setString(String.valueOf(attributes
				.getInteger(CommonConfigurationBean.ATTR_CONSOLE_SOCKET_PORT)));
		consoleShowLinux.setSelection(attributes
				.getBoolean(CommonConfigurationBean.ATTR_CONSOLE_SHOW_LINUX));
		consoleShowSimulator
				.setSelection(attributes
						.getBoolean(CommonConfigurationBean.ATTR_CONSOLE_SHOW_SIMULATOR));
		if (availableAutomaticPort) {
			automaticPortConfig
					.setSelection(attributes
							.getBoolean(CommonConfigurationBean.ATTR_AUTOMATIC_PORTCONFIG));
		} else {
			automaticPortConfig.setSelection(false);
			automaticPortConfig.setEnabled(false);
		}
		portAutoControlsHandler.manageDependentControls(automaticPortConfig);
		if (availableRemoteConnection) {
			targetConnectionFrame
					.setHostPort(attributes
							.getInteger(RemoteConfigurationBean.ATTR_REMOTE_CONNECTION_PORT));
			targetConnectionFrame
					.setHostAddress(attributes
							.getString(RemoteConfigurationBean.ATTR_REMOTE_CONNECTION_ADDRESS));
			targetConnectionFrame
					.setUserName(attributes
							.getString(RemoteConfigurationBean.ATTR_REMOTE_LOGIN_USERNAME));
			targetConnectionFrame
					.setPassword(attributes
							.getString(RemoteConfigurationBean.ATTR_REMOTE_LOGIN_PASSWORD));
			targetConnectionFrame.setPublicKeyPath(attributes
					.getString(RemoteConfigurationBean.ATTR_REMOTE_KEY_PATH));
			targetConnectionFrame
					.setPassphrase(attributes
							.getString(RemoteConfigurationBean.ATTR_REMOTE_KEY_PASSPHRASE));
			targetConnectionFrame.setTimeout(attributes
					.getInteger(RemoteConfigurationBean.ATTR_REMOTE_TIMEOUT));
			
//			 Fill the combobox with available cipher types for the remote host
			Map cMap = AbstractTargetControl.getCipherTypesMap();
			Set ckSet = cipherMap.keySet();
			ComboGroup cipherGroup = targetConnectionFrame.getCipherTypeGroup();
			for(Iterator it = ckSet.iterator(); it.hasNext();) {
				String key = (String)it.next();
				String value = (String)cMap.get(key);
				
				cipherGroup.add(new ComboGroupItem(key, value));
			}
			// Select the cipher type based on the attributes map.
			cipherGroup.selectIndexUsingID(attributes.getString(RemoteConfigurationBean.ATTR_REMOTE_CIPHER_TYPE));
			
			targetConnectionFrame
					.setPasswordBased(attributes
							.getBoolean(RemoteConfigurationBean.ATTR_REMOTE_IS_PASSWORD_AUTH));
		}
		systemWorkspaceGroup.setString(attributes.getString(CommonConfigurationBean.ATTR_SYSTEM_WORKSPACE));
		// textModifyListener.enable();
		// buttonSelectionListener.enable();
		// if (targetConnectionModifyListener != null)
		// targetConnectionModifyListener.enable();
	}

	public void setAvailableAutomaticNetwork(boolean availableAutomaticNetwork) {
		this.availableAutomaticNetwork = availableAutomaticNetwork;
	}

	public void setAvailableAutomaticPort(boolean availableAutomaticPort) {
		this.availableAutomaticPort = availableAutomaticPort;
	}
	
	public void setAvailableRemoteConnection(boolean availableRemoteConnection) {
		this.availableRemoteConnection = availableRemoteConnection;
	}
	
	public void setAvailableAutomaticWorkDirectory(
			boolean availableAutomaticWorkDirectory) {
		this.availableAutomaticWorkDirectory = availableAutomaticWorkDirectory;
	}
}
