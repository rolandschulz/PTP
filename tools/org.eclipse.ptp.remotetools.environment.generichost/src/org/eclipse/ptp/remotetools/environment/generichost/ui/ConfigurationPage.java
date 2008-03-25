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
package org.eclipse.ptp.remotetools.environment.generichost.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;
import org.eclipse.ptp.remotetools.environment.generichost.core.TargetControl;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;
import org.eclipse.ptp.remotetools.utils.ui.swt.AuthenticationFrame;
import org.eclipse.ptp.remotetools.utils.ui.swt.AuthenticationFrameMold;
import org.eclipse.ptp.remotetools.utils.ui.swt.ComboGroup;
import org.eclipse.ptp.remotetools.utils.ui.swt.ComboGroupItem;
import org.eclipse.ptp.remotetools.utils.ui.swt.Frame;
import org.eclipse.ptp.remotetools.utils.ui.swt.TextGroup;
import org.eclipse.ptp.remotetools.utils.ui.swt.TextMold;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


/**
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 *
 */
public class ConfigurationPage extends AbstractEnvironmentDialogPage {
	ConfigFactory configFactory = null;
	
	public ConfigurationPage(String targetName, Map attributesMap) {
		super(targetName);
		
		/*if (targetName == null) {
			this.targetName = Messages.ConfigurationPage_DefaultTargetName;
		} else {*/
		this.targetName = targetName;
		//}

		configFactory = new ConfigFactory(attributesMap);
	}
	
	public ConfigurationPage() {
		super(Messages.ConfigurationPage_DefaultTargetName);
		this.targetName = Messages.ConfigurationPage_DefaultTargetName;
		
		configFactory = new ConfigFactory();
	}

	private String targetName;
	private TextGroup targetNameGroup;
	private AuthenticationFrame remoteAuthFrame;
	private TextGroup systemWorkspaceGroup;
	
	class DataModifyListener implements ModifyListener {
		int counter = 0;
		public synchronized void enable() {
			counter++;
		}
		public synchronized void disable() {
			counter--;
		}
		public synchronized void modifyText(ModifyEvent e) {
			if (counter < 0) {
				return;
			}
			readControls();
			
			// updateButtons() will call is Valid(), that will call validateFields()
			getContainer().updateButtons();
		}
	}
	
	private DataModifyListener dataModifyListener;
	
	public void createControl(Composite parent) {
		
		this.setDescription(Messages.ConfigurationPage_DialogDescription);
		this.setTitle(Messages.ConfigurationPage_DialogTitle);
		this.setErrorMessage(null);

		GridLayout topLayout = new GridLayout();
		final Composite topControl = new Composite(parent, SWT.NONE);
		setControl(topControl);
		topControl.setLayout(topLayout);

		/*
		 * Environment name Label and text controls.
		 */
//		TextGroupMold tmold = new TextGroupMold(TextGroupMold.GRID_DATA_ALIGNMENT_FILL
//				| TextGroupMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_LabelTargetName, TextGroup.MAX_SIZE);
		TextMold mold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_LabelTargetName);
		targetNameGroup = new TextGroup(topControl, mold);
		
		createAuthControl(topControl);

		/*
		 * System workspace
		 */
		Frame frame = new Frame(topControl, "Remote application launcher:");
		mold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.ConfigurationPage_LabelSystemWorkspace);
		systemWorkspaceGroup = new TextGroup(frame.getTopUserReservedComposite(), mold);
		
		fillControls();
		registerListeners();
	}
	
	private void registerListeners() {
		dataModifyListener = new DataModifyListener();
		targetNameGroup.addModifyListener(dataModifyListener);
		remoteAuthFrame.addModifyListener(dataModifyListener);
		systemWorkspaceGroup.addModifyListener(dataModifyListener);
	}
	
	private void fillControls() {
		ControlAttributes attributes = configFactory.getAttributes();
		targetNameGroup.setString(targetName);
		remoteAuthFrame.setLocalhostSelected(attributes.getBoolean(ConfigFactory.ATTR_LOCALHOST_SELECTION));
		remoteAuthFrame.setHostPort(attributes.getInteger(ConfigFactory.ATTR_CONNECTION_PORT));
		remoteAuthFrame.setHostAddress(attributes.getString(ConfigFactory.ATTR_CONNECTION_ADDRESS));
		remoteAuthFrame.setUserName(attributes.getString(ConfigFactory.ATTR_LOGIN_USERNAME));
		remoteAuthFrame.setPassword(attributes.getString(ConfigFactory.ATTR_LOGIN_PASSWORD));
		remoteAuthFrame.setPublicKeyPath(attributes.getString(ConfigFactory.ATTR_KEY_PATH));
		remoteAuthFrame.setPassphrase(attributes.getString(ConfigFactory.ATTR_KEY_PASSPHRASE));
		remoteAuthFrame.setTimeout(attributes.getInteger(ConfigFactory.ATTR_CONNECTION_TIMEOUT));
		remoteAuthFrame.setPasswordBased(attributes.getBoolean(ConfigFactory.ATTR_IS_PASSWORD_AUTH));
		systemWorkspaceGroup.setString(attributes.getString(ConfigFactory.ATTR_SYSTEM_WORKSPACE));
		
		// Fill the combobox with available cipher types
		Map cipherMap = TargetControl.getCipherTypesMap();
		Set cKeySet = cipherMap.keySet();
		ComboGroup cipherGroup = remoteAuthFrame.getCipherTypeGroup();
		for(Iterator it = cKeySet.iterator(); it.hasNext();) {
			String key = (String)it.next();
			String value = (String)cipherMap.get(key);
			
			cipherGroup.add(new ComboGroupItem(key, value));
		}
		// Select the cipher type based on the attributes map.
		cipherGroup.selectIndexUsingID(attributes.getString(ConfigFactory.ATTR_CIPHER_TYPE));
		
		
		//org.eclipse.ptp.remotetools.internal.ssh.systemWorkspaceGroup.setString(attributes.getString(ConfigFactory.ATTR_SYSTEM_WORKSPACE));
	}
	
	private void readControls() {
		ControlAttributes attributes = configFactory.getAttributes();
		targetName = targetNameGroup.getString();
		attributes.setBooleanAttribute(ConfigFactory.ATTR_LOCALHOST_SELECTION, remoteAuthFrame.isLocalhostSelected());
		attributes.setStringAttribute(ConfigFactory.ATTR_LOGIN_USERNAME, remoteAuthFrame.getUserName());
		attributes.setStringAttribute(ConfigFactory.ATTR_LOGIN_PASSWORD, remoteAuthFrame.getPassword());
		attributes.setStringAttribute(ConfigFactory.ATTR_CONNECTION_ADDRESS, remoteAuthFrame.getHostAddress());
		attributes.setStringAttribute(ConfigFactory.ATTR_CONNECTION_PORT, Integer.toString(remoteAuthFrame.getHostPort()));
		attributes.setStringAttribute(ConfigFactory.ATTR_KEY_PATH, remoteAuthFrame.getPublicKeyPath());	
		attributes.setStringAttribute(ConfigFactory.ATTR_KEY_PASSPHRASE, remoteAuthFrame.getPassphrase());
		attributes.setStringAttribute(ConfigFactory.ATTR_CONNECTION_TIMEOUT, Integer.toString(remoteAuthFrame.getTimeout()));
		attributes.setBooleanAttribute(ConfigFactory.ATTR_IS_PASSWORD_AUTH, remoteAuthFrame.isPasswordBased());
		attributes.setStringAttribute(ConfigFactory.ATTR_SYSTEM_WORKSPACE, systemWorkspaceGroup.getString());
		attributes.setStringAttribute(ConfigFactory.ATTR_CIPHER_TYPE, remoteAuthFrame.getSelectedCipherType().getId());
	}

	private void createAuthControl(Composite topControl) {
		AuthenticationFrameMold amold = new AuthenticationFrameMold(Messages.ConfigurationPage_ConnectionFrameTitle);
		amold.setBitmask(AuthenticationFrameMold.SHOW_HOST_TYPE_RADIO_BUTTON);
		amold.setLabelLocalhost(Messages.ConfigurationPage_LabelLocalhost);
		amold.setLabelRemoteHost(Messages.ConfigurationPage_LabelRemoteHost);
		amold.setLabelHideAdvancedOptions(Messages.ConfigurationPage_LabelHideAdvancedOptions);
		amold.setLabelHostAddress(Messages.ConfigurationPage_LabelHostAddress);
		amold.setLabelHostPort(Messages.ConfigurationPage_LabelHostPort);
		amold.setLabelIsPasswordBased(Messages.ConfigurationPage_LabelIsPasswordBased);
		amold.setLabelIsPublicKeyBased(Messages.ConfigurationPage_LabelIsPublicKeyBased);
		amold.setLabelPassphrase(Messages.ConfigurationPage_LabelPassphrase);
		amold.setLabelPassword(Messages.ConfigurationPage_LabelPassword);
		amold.setLabelPublicKeyPath(Messages.ConfigurationPage_LabelPublicKeyPath);
		amold.setLabelPublicKeyPathButton(Messages.ConfigurationPage_LabelPublicKeyPathButton);
		amold.setLabelPublicKeyPathTitle(Messages.ConfigurationPage_LabelPublicKeyPathTitle);
		amold.setLabelShowAdvancedOptions(Messages.ConfigurationPage_LabelShowAdvancedOptions);
		amold.setLabelTimeout(Messages.ConfigurationPage_LabelTimeout);
		amold.setLabelCipherType(Messages.ConfigurationPage_CipherType);
		amold.setLabelUserName(Messages.ConfigurationPage_LabelUserName);

		this.remoteAuthFrame = new AuthenticationFrame(topControl, amold);
//		remoteAuthFrame.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
//		remoteAuthFrame.addModifyListener(dataModifyListener);
	}

	public Map getAttributes() {
		return configFactory.getMap();
	}

	public boolean isValid() {
		try {
			remoteAuthFrame.validateFields();
			configFactory.createTargetConfig();
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		return true;
	}

	public String getName() {
	    return targetName;
	}

}
