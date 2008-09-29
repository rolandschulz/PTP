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
package org.eclipse.ptp.cell.environment.remotesimulator.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.cell.environment.remotesimulator.core.ConfigFactory;
import org.eclipse.ptp.cell.environment.remotesimulator.core.TargetControl;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.utils.ui.swt.AuthenticationFrame;
import org.eclipse.ptp.utils.ui.swt.AuthenticationFrameMold;
import org.eclipse.ptp.utils.ui.swt.ComboGroup;
import org.eclipse.ptp.utils.ui.swt.ComboGroupItem;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
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
import org.eclipse.swt.widgets.Text;


/**
 * @author Daniel Felix Ferber
 * @since 1.2.0
 */
public class ConfigurationPage extends AbstractEnvironmentDialogPage {

	class TextModifyListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			
			if(e.widget instanceof Text) {
				String textValue = ((Text)e.widget).getText();
				
				// TODO: remote this verification
				if (e.widget == targetNameText.getText()) {
					targetName = textValue;
				}
				
				if(e.widget == remoteConnGroup.getHostAddrTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_REMOTE_CONNECTION_ADDRESS, textValue);
				} else if(e.widget == remoteConnGroup.getHostPortTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_REMOTE_CONNECTION_PORT, textValue);
				} else if(e.widget == remoteConnGroup.getUsernameTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_REMOTE_LOGIN_USERNAME, textValue);
				} else if(e.widget == remoteConnGroup.getPasswordTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_REMOTE_LOGIN_PASSWORD, textValue);
				} else if(e.widget == remoteConnGroup.getPublicKeyPathGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_REMOTE_KEY_PATH, textValue);
				} else if(e.widget == remoteConnGroup.getPassphraseTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_REMOTE_KEY_PASSPHRASE, textValue);
				} else if(e.widget == remoteConnGroup.getTimeoutTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_REMOTE_CONNECTION_TIMEOUT, textValue);
				} else if(e.widget == systemWorkspaceGroup.getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SYSTEM_WORKSPACE, textValue);
				}
			} else if(e.widget instanceof Button) {
				boolean passwdBased = e.widget == remoteConnGroup.getAuthKindSelectionButtons()[0];
				remoteConnGroup.setPasswordBased(passwdBased);
				attributes.setBooleanAttribute(ConfigFactory.ATTR_REMOTE_IS_PASSWORD_AUTH, passwdBased);
			} else if(e.widget instanceof Combo) {
				attributes.setAttribute(ConfigFactory.ATTR_REMOTE_CIPHER_TYPE, 
						remoteConnGroup.getSelectedCipherType().getId());
			}

			// updateButtons() will call is Valid(), that will call validateFields()
			getContainer().updateButtons();
		}
	}
	
	private Map attributesMap;
	private ControlAttributes attributes; 
	
	private boolean hasWarning = false;
	private boolean hasError = false;
	
	// TODO: remote this field
	private String targetName;

	// TODO: remote this widget
	private TextGroup targetNameText;
	
	// Remote simulator options source
	private Button optionAutomatic;
	private Button optionCustom;
	private Button openCustomWindow;
	
	private AuthenticationFrame remoteConnGroup;
	
	private ModifyListener textModifyListener;
	private TextGroup systemWorkspaceGroup;
	
	public ConfigurationPage(String targetName, Map attributesMap) {
		super(targetName);
		
		/*if (attributesMap == null) {
			this.attributesMap = ConfigFactory.createDefaultConfig();
		} else {*/
		this.attributesMap = attributesMap;
		//}
		
		/*if (targetName == null) {
			this.targetName = "Remote Cell Simulator";
		} else {*/
		this.targetName = targetName;
		//}
		
		attributes = new ControlAttributes(this.attributesMap);
	}
	
	public ConfigurationPage() {
		super(Messages.ConfigurationPage_RemoteCellSimulatorLabel);
		this.attributesMap = ConfigFactory.createDefaultConfig();
		this.targetName = Messages.ConfigurationPage_RemoteCellSimulatorLabel;
		attributes = new ControlAttributes(this.attributesMap);
	}
	
	public void createControl(Composite parent) {
		
		this.setDescription(Messages.ConfigurationPage_RemoteCellSimulatorConnectionProperties);
		this.setTitle(Messages.ConfigurationPage_RemoteCellSimulatorLabel);
		this.setErrorMessage(null);

		GridLayout topLayout = new GridLayout();
		final Composite topControl = new Composite(parent, SWT.NONE);
		setControl(topControl);
		topControl.setLayout(topLayout);
		
		textModifyListener = new TextModifyListener();

		/*
		 * TODO: The name attribute will be removed.
		 */
		TextMold tmold = new TextMold(TextMold.GRID_DATA_SPAN | TextMold.GRID_DATA_ALIGNMENT_FILL, 
				Messages.ConfigurationPage_TargetNameLabel);
		tmold.setValue(targetName);
		targetNameText = new TextGroup(topControl, tmold);
		targetNameText.getText().addModifyListener(textModifyListener);
		
		createRemoteConnectionGroup(topControl);
		
		fillRemoteConnectionGroup();
		
		registerRemoteConnectionGroup();

		createSimulatorConfigurationGroup(topControl);
		
		fillSimulatorConfigurationOption();
		
		registerSimulatorConfigurationGroup();
		
		// Generate already fills and register
		generateSystemWorkspaceConfigurationGroup(topControl);
		
		validateFields();
	}
	
	private void generateSystemWorkspaceConfigurationGroup(Composite topControl) {
		/*
		 * System workspace
		 */
		Frame frame = new Frame(topControl, Messages.ConfigurationPage_CellApplicationLaunch);
		TextMold mold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_BaseDir);
		systemWorkspaceGroup = new TextGroup(frame.getTopUserReservedComposite(), mold);
		
		// Fill it with data
		systemWorkspaceGroup.setString(attributes.getString(ConfigFactory.ATTR_SYSTEM_WORKSPACE));
		
		// Register it
		systemWorkspaceGroup.addModifyListener(new TextModifyListener());
	}

	private void registerSimulatorConfigurationGroup() {
		// Custom push button is associated with the custom option button
		openCustomWindow.addSelectionListener(new RemoteSimulatorPushButtonListener());
		
		// Create listener and set them to the controls
		RemoteSimulatorOptionButtonListener rsimoptlisten = new RemoteSimulatorOptionButtonListener();
		//optionCustom.addSelectionListener(rsimoptlisten);
		optionAutomatic.addSelectionListener(rsimoptlisten);
		
	}

	private void registerRemoteConnectionGroup() {
		remoteConnGroup.addModifyListener(textModifyListener);
	}

	private void fillSimulatorConfigurationOption() {
		boolean boolValue = attributes.getBooleanAttribute(ConfigFactory.ATTR_SIMULATOR_IS_AUTOMATIC_CONFIG, 
				ConfigFactory.DEFAULT_SIMULATOR_IS_AUTOMATIC_CONFIG);
		
		remoteSimulatorOptionButtonsAction(boolValue); 
	}

	private void fillRemoteConnectionGroup() {
		fillTextGroup(remoteConnGroup.getHostAddrTextGroup(), ConfigFactory.ATTR_REMOTE_CONNECTION_ADDRESS, 
				ConfigFactory.DEFAULT_REMOTE_CONNECTION_ADDRESS);
		fillTextGroup(remoteConnGroup.getHostPortTextGroup(), ConfigFactory.ATTR_REMOTE_CONNECTION_PORT, 
				Integer.toString(ConfigFactory.DEFAULT_REMOTE_CONNECTION_PORT));
		fillTextGroup(remoteConnGroup.getUsernameTextGroup(), ConfigFactory.ATTR_REMOTE_LOGIN_USERNAME, 
				ConfigFactory.DEFAULT_REMOTE_LOGIN_USERNAME);
		fillTextGroup(remoteConnGroup.getPasswordTextGroup(), ConfigFactory.ATTR_REMOTE_LOGIN_PASSWORD, 
				ConfigFactory.DEFAULT_REMOTE_LOGIN_PASSWORD);
		fillTextGroup(remoteConnGroup.getPublicKeyPathGroup(), ConfigFactory.ATTR_REMOTE_KEY_PATH, 
				ConfigFactory.DEFAULT_REMOTE_KEY_PATH);
		fillTextGroup(remoteConnGroup.getPassphraseTextGroup(), ConfigFactory.ATTR_REMOTE_KEY_PASSPHRASE, 
				ConfigFactory.DEFAULT_REMOTE_KEY_PASSPHRASE);
		
		remoteConnGroup.setPasswordBased(attributes.getBooleanAttribute(ConfigFactory.ATTR_REMOTE_IS_PASSWORD_AUTH, 
				ConfigFactory.DEFAULT_REMOTE_IS_PASSWORD_AUTH));
		
		fillTextGroup(remoteConnGroup.getTimeoutTextGroup(), ConfigFactory.ATTR_REMOTE_CONNECTION_TIMEOUT, 
				Integer.toString(ConfigFactory.DEFAULT_REMOTE_CONNECTION_TIMEOUT));
		
//		 Fill the combobox with available cipher types
		Map cipherMap = TargetControl.getCipherTypesMap();
		Set cKeySet = cipherMap.keySet();
		ComboGroup cipherGroup = remoteConnGroup.getCipherTypeGroup();
		for(Iterator it = cKeySet.iterator(); it.hasNext();) {
			String key = (String)it.next();
			String value = (String)cipherMap.get(key);
			
			cipherGroup.add(new ComboGroupItem(key, value));
		}
		// Select the cipher type based on the attributes map.
		cipherGroup.selectIndexUsingID(attributes.getString(ConfigFactory.ATTR_REMOTE_CIPHER_TYPE));
	}

	private void fillTextGroup(TextGroup tgroup, String key, String defaultValue) {
		Text tbox = tgroup.getText();
		tbox.setText(attributes.getStringAttribute(key, defaultValue));
	}

	/**
	 * Create a group that let the user choose between an automatic configuration and a custom configuration.
	 * In the case of a custom configuration, pushing the button will open a window so the user can enter 
	 * the custom configuration.
	 * 
	 * @param topControl
	 */
	private void createSimulatorConfigurationGroup(Composite topControl) {
		FrameMold mold = new FrameMold( Messages.ConfigurationPage_RemoteHostToSimulatorConnection, 2, false);
		Frame fdesc = new Frame(topControl, mold);
//		fdesc.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL ));
		
		// Two Option buttons (automatic and custom); the custom buttom has a button that opens a window with more
		// options.
		optionAutomatic = new Button(fdesc.getTopUserReservedComposite(), SWT.RADIO);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 2;
		optionAutomatic.setLayoutData(gdata);
		optionAutomatic.setText(Messages.ConfigurationPage_Automatic);
		
		optionCustom = new Button(fdesc.getTopUserReservedComposite(), SWT.RADIO);
		gdata = new GridData();
		optionCustom.setLayoutData(gdata);
		optionCustom.setText(Messages.ConfigurationPage_Custom);
		
		
		openCustomWindow = new Button(fdesc.getTopUserReservedComposite(), SWT.PUSH);
		openCustomWindow.setLayoutData(new GridData());
		openCustomWindow.setText(Messages.ConfigurationPage_Set);
	}

	/**
	 * Manages the option buttons behavior, so the openCustomWindow button is only enabled
	 * when the optionCustom is selected.
	 * 
	 * @author Richard Maciel
	 *
	 */
	class RemoteSimulatorOptionButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent arg0) {
			boolean auto = optionAutomatic.getSelection();
			remoteSimulatorOptionButtonsAction(auto);
		}
	}
	
	/**
	 * Manages the push button, so it opens a configuration window when pushed.
	 * 
	 * @author Richard Maciel
	 *
	 */
	class RemoteSimulatorPushButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent arg0) {
			createSimulatorConfigurationWindow();
		}
	}
	
	/**
	 * Execute an action based on which control was received as parameter.
	 * 
	 * @param option Widget Received widget
	 */
	private void remoteSimulatorOptionButtonsAction(boolean isAutoConf) {
		optionAutomatic.setSelection(isAutoConf);
		optionCustom.setSelection(!isAutoConf);
		
		// Only enable button if configuration is manual.
		openCustomWindow.setEnabled(!isAutoConf);
		
		attributes.setBooleanAttribute(ConfigFactory.ATTR_SIMULATOR_IS_AUTOMATIC_CONFIG, isAutoConf);
	}
	
	/**
	 * Generate a new window which contains the fields to configurate the access to the simulator itself.
	 *
	 */
	private void createSimulatorConfigurationWindow() {
		SimulatorConfigDialog dialog = new SimulatorConfigDialog(this.getShell(), attributes);
		
		dialog.open();
		attributes = dialog.getModifiedAttributes();
		
		//System.out.println("Teste");
	}
	
	public Map getAttributes() {
		return attributesMap;
	}

	public boolean isValid() {
		validateFields();
		return ! hasError;
	}

	public String getName() {
	    return targetName;
	}

	public void validateFields() {
		
		/*
		 * First, assume not error nor warning.
		 * Then test target configuration, and if no error is found,
		 * then check simulator configuration.
		 */
		hasWarning = false;
		hasError = false;

		try {
			remoteConnGroup.validateFields();
		} catch (CoreException e) {
			hasError = true;
			setErrorMessage(e.getMessage());
		}
		
		ConfigFactory factory = new ConfigFactory(attributes);
		IStatus status = factory.checkTargetConfig();
		
		
		/* *//** First, assume not error nor warning.
		 * Then test target configuration, and if no error is found,
		 * then check simulator configuration.*//*
		 
		hasWarning = false;
		hasError = false;

		ConfigFactory factory = new ConfigFactory(attributes);
		IStatus status = factory.checkTargetConfig();
		
		if (status.isOK()) {
			setErrorMessage(null);
		}
		else if ((status.getSeverity() == IStatus.ERROR)
				|| 
				(status.getSeverity() == IStatus.WARNING)) {
			if (status instanceof MultiStatus) {
				MultiStatus multiStatus = (MultiStatus) status;
				// What is the plural of 'status'?
				IStatus statuses [] = multiStatus.getChildren();
				String message = null;
				for (int i = 0; i < statuses.length; i++) {
					IStatus thisStatus = statuses[i];
					if (thisStatus.getSeverity() == IStatus.WARNING) {
						hasWarning = true;
						if (message != null) {
							message = thisStatus.getMessage();
						}
					}
					else if (thisStatus.getSeverity() == IStatus.ERROR) {
						hasError = true;
						message = thisStatus.getMessage();
						break;
					}
				}
				setErrorMessage(message);
			} else {
				Status singleStatus = (Status) status;
				if (singleStatus.getSeverity() == IStatus.ERROR) {
					hasError = true;
					hasWarning = false;
					setErrorMessage(singleStatus.getMessage());
				}
				else if (singleStatus.getSeverity() == IStatus.WARNING) {
					hasError = false;
					hasWarning = true;
					setErrorMessage(singleStatus.getMessage());
				}
			}
		}*/
	}	

	private void createRemoteConnectionGroup(Composite parent) {
		AuthenticationFrameMold amold = new AuthenticationFrameMold(Messages.ConfigurationPage_RemoteHostInfo);
		remoteConnGroup = new AuthenticationFrame(parent, amold);
		remoteConnGroup.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL));
		
	}
}
