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
package org.eclipse.ptp.remote.remotetools.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.utils.ui.swt.AuthenticationFrame;
import org.eclipse.ptp.remotetools.utils.ui.swt.AuthenticationFrameMold;
import org.eclipse.ptp.remotetools.utils.ui.swt.ComboGroup;
import org.eclipse.ptp.remotetools.utils.ui.swt.ComboGroupItem;
import org.eclipse.ptp.remotetools.utils.ui.swt.TextGroup;
import org.eclipse.ptp.remotetools.utils.ui.swt.TextMold;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 *
 */
public class ConfigurationDialog extends Dialog {
	
	class DataModifyListener implements ModifyListener {
		int counter = 0;
		public synchronized void disable() {
			counter--;
		}
		public synchronized void enable() {
			counter++;
		}
		public synchronized void modifyText(ModifyEvent e) {
			if (counter < 0) {
				return;
			}
			readControls();
			try {
				remoteAuthFrame.validateFields();
			} catch (CoreException e1) {
				return;
			}
			if (!targetNameGroup.getText().equals("")) {
				okButton.setEnabled(true);
			}
			// updateButtons() will call is Valid(), that will call validateFields()
			//getContainer().updateButtons();
		}
	}
	
	private static int DEFAULT_PORT = 22;
	private static int DEFAULT_TIMEOUT = 5;

	private String targetName;
	private TextGroup targetNameGroup;
	private AuthenticationFrame remoteAuthFrame;
	private Button okButton;
	private ConfigFactory configFactory = null;
	
	private DataModifyListener dataModifyListener;
	
	public ConfigurationDialog(Shell parent) {
		super(parent);
		configFactory = new ConfigFactory();
	}
	
	public Control createButtonBar(Composite parent) {
		Control buttonBar = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(false);
		}
		return buttonBar;
	}
	
	public Control createDialogArea(Composite parent) {
		
		GridLayout topLayout = new GridLayout();
		final Composite topControl = new Composite(parent, SWT.NONE);
		topControl.setLayout(topLayout);

		/*
		 * Environment name Label and text controls.
		 */
		TextMold mold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, 
				Messages.ConfigurationDialog_LabelConnectionName);
		targetNameGroup = new TextGroup(topControl, mold);
		
		createAuthControl(topControl);

		fillControls();
		registerListeners();
		
		return topControl;
	}
	
	public Map<String, String> getAttributes() {
		return configFactory.getMap();
	}
	
	public String getName() {
	    return targetName;
	}
	
	public boolean isValid() {
		try {
			remoteAuthFrame.validateFields();
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	private void createAuthControl(Composite topControl) {
		AuthenticationFrameMold amold = new AuthenticationFrameMold(Messages.ConfigurationDialog_ConnectionFrameTitle);
		amold.setLabelRemoteHost(Messages.ConfigurationDialog_LabelRemoteHost);
		amold.setLabelHideAdvancedOptions(Messages.ConfigurationDialog_LabelHideAdvancedOptions);
		amold.setLabelHostAddress(Messages.ConfigurationDialog_LabelHostAddress);
		amold.setLabelHostPort(Messages.ConfigurationDialog_LabelHostPort);
		amold.setLabelIsPasswordBased(Messages.ConfigurationDialog_LabelIsPasswordBased);
		amold.setLabelIsPublicKeyBased(Messages.ConfigurationDialog_LabelIsPublicKeyBased);
		amold.setLabelPassphrase(Messages.ConfigurationDialog_LabelPassphrase);
		amold.setLabelPassword(Messages.ConfigurationDialog_LabelPassword);
		amold.setLabelPublicKeyPath(Messages.ConfigurationDialog_LabelPublicKeyPath);
		amold.setLabelPublicKeyPathButton(Messages.ConfigurationDialog_LabelPublicKeyPathButton);
		amold.setLabelPublicKeyPathTitle(Messages.ConfigurationDialog_LabelPublicKeyPathTitle);
		amold.setLabelShowAdvancedOptions(Messages.ConfigurationDialog_LabelShowAdvancedOptions);
		amold.setLabelTimeout(Messages.ConfigurationDialog_LabelTimeout);
		amold.setLabelCipherType(Messages.ConfigurationDialog_CipherType);
		amold.setLabelUserName(Messages.ConfigurationDialog_LabelUserName);

		this.remoteAuthFrame = new AuthenticationFrame(topControl, amold);
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

		// Fill the combobox with available cipher types
		Map cipherMap = RemotetoolsPlugin.getCipherTypesMap();
		Set cKeySet = cipherMap.keySet();
		ComboGroup cipherGroup = remoteAuthFrame.getCipherTypeGroup();
		for(Iterator it = cKeySet.iterator(); it.hasNext();) {
			String key = (String)it.next();
			String value = (String)cipherMap.get(key);
			
			cipherGroup.add(new ComboGroupItem(key, value));
		}
		cipherGroup.selectIndexUsingID(RemotetoolsPlugin.CIPHER_DEFAULT);
	}

	private void readControls() {
	}

	private void registerListeners() {
		dataModifyListener = new DataModifyListener();
		targetNameGroup.addModifyListener(dataModifyListener);
		remoteAuthFrame.addModifyListener(dataModifyListener);
	}

}
