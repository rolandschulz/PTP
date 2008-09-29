/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.remotesimulator.ui;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.cell.environment.remotesimulator.core.ConfigFactory;
import org.eclipse.ptp.cell.environment.remotesimulator.core.TargetControl;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.utils.ui.swt.AuthenticationFrame;
import org.eclipse.ptp.utils.ui.swt.AuthenticationFrameMold;
import org.eclipse.ptp.utils.ui.swt.ComboGroup;
import org.eclipse.ptp.utils.ui.swt.ComboGroupItem;
import org.eclipse.ptp.utils.ui.swt.TextGroup;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog which contains the remote simulator custom configuration.
 * 
 * @author Richard Maciel
 *
 */
public class SimulatorConfigDialog extends TitleAreaDialog {

	Composite dialogArea;

	AuthenticationFrame remoteSimGroup;
	
	//TextGroup texttest;

	ControlAttributes attributes, originalAttributes;
	
	boolean hasError;
	
	/**
	 * Default constructor
	 * 
	 * @param parentShell
	 */
	public SimulatorConfigDialog(Shell parentShell, ControlAttributes attributes) {
		super(parentShell);
		this.attributes = attributes;
		try {
			originalAttributes = (ControlAttributes)attributes.clone();
		} catch (CloneNotSupportedException e) {
			// ignore, bacause close is known to be supported.
		}
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);

		// Set title of this window.
		setTitle(Messages.SimulatorConfigDialog_RemoteSimulatorCustomConfigTitle);
		//setMessage("Teste message");
		
		// Fetch width of the previous window and apply to this control.
		((GridData)composite.getLayoutData()).widthHint = parent.getShell().getSize().y;
		
		// Create the unique control
		AuthenticationFrameMold fmold = new AuthenticationFrameMold(Messages.SimulatorConfigDialog_Test);
		remoteSimGroup = new AuthenticationFrame(composite, fmold);
		remoteSimGroup.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL));
		
		/*TextGroupMold tmold = new TextGroupMold(TextGroupMold.GRID_DATA_ALIGNMENT_FILL, "Teste", TextGroup.MAX_SIZE);
		texttest = new TextGroup(parent, tmold);*/
		
//		 Fill and validate controls
		fillSimulatorConfigurationGroup();
		
		// Set its listener
		remoteSimGroup.addModifyListener(new SimulatorConfigListener());

		
		//validateFieldsAndUpdateInterface();
		
		return composite;
	}
	
	private void fillSimulatorConfigurationGroup() {
		//remoteSimGroup.setUserName("Teste");
		//texttest.setString("Teste");
		
		fillTextGroup(remoteSimGroup.getHostAddrTextGroup(), ConfigFactory.ATTR_SIMULATOR_CONNECTION_ADDRESS, 
				ConfigFactory.DEFAULT_SIMULATOR_CONNECTION_ADDRESS);
		fillTextGroup(remoteSimGroup.getHostPortTextGroup(), ConfigFactory.ATTR_SIMULATOR_CONNECTION_PORT, 
				Integer.toString(ConfigFactory.DEFAULT_SIMULATOR_CONNECTION_PORT));
		fillTextGroup(remoteSimGroup.getUsernameTextGroup(), ConfigFactory.ATTR_SIMULATOR_LOGIN_USERNAME, 
				ConfigFactory.DEFAULT_SIMULATOR_LOGIN_USERNAME);
		fillTextGroup(remoteSimGroup.getPasswordTextGroup(), ConfigFactory.ATTR_SIMULATOR_LOGIN_PASSWORD, 
				ConfigFactory.DEFAULT_SIMULATOR_LOGIN_PASSWORD);
		fillTextGroup(remoteSimGroup.getPublicKeyPathGroup(), ConfigFactory.ATTR_SIMULATOR_KEY_PATH, 
				ConfigFactory.DEFAULT_SIMULATOR_KEY_PATH);
		fillTextGroup(remoteSimGroup.getPassphraseTextGroup(), ConfigFactory.ATTR_SIMULATOR_KEY_PASSPHRASE, 
				ConfigFactory.DEFAULT_SIMULATOR_KEY_PASSPHRASE);
		
		remoteSimGroup.setPasswordBased(attributes.getBooleanAttribute(ConfigFactory.ATTR_SIMULATOR_IS_PASSWORD_AUTH, 
				ConfigFactory.DEFAULT_SIMULATOR_IS_PASSWORD_AUTH));
		
		fillTextGroup(remoteSimGroup.getTimeoutTextGroup(), ConfigFactory.ATTR_SIMULATOR_CONNECTION_TIMEOUT, 
				Integer.toString(ConfigFactory.DEFAULT_SIMULATOR_CONNECTION_TIMEOUT));
		
//		 Fill the combobox with available cipher types
		Map cipherMap = TargetControl.getCipherTypesMap();
		Set cKeySet = cipherMap.keySet();
		ComboGroup cipherGroup = remoteSimGroup.getCipherTypeGroup();
		for(Iterator it = cKeySet.iterator(); it.hasNext();) {
			String key = (String)it.next();
			String value = (String)cipherMap.get(key);
			
			cipherGroup.add(new ComboGroupItem(key, value));
		}
		// Select the cipher type based on the attributes map.
		cipherGroup.selectIndexUsingID(attributes.getString(ConfigFactory.ATTR_SIMULATOR_CIPHER_TYPE));
		
	}
	

	void fillTextGroup(TextGroup tgroup, String key, String defaultValue) {
		Text tbox = tgroup.getText();
		//tbox.setT
		//tbox.setText("Teste");
		tbox.setText(attributes.getStringAttribute(key, defaultValue));
	}

	public void validateFieldsAndUpdateInterface() {
		
		hasError = false;
		setErrorMessage(null);
		
		try {
			remoteSimGroup.validateFields();
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			hasError = true;
		}
		
		// If has any error, don't let the user save the configuration. Disable the ok button.
		getButton(IDialogConstants.OK_ID).setEnabled(!hasError);
		
		//ConfigFactory factory = new ConfigFactory(attributes);
		//IStatus status = factory.checkTargetConfig();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		super.cancelPressed();
		
		attributes = originalAttributes;
	}

	public ControlAttributes getModifiedAttributes() {
		return attributes;
	}
	
	class SimulatorConfigListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			
			if(e.widget instanceof Text) {
				String textValue = ((Text)e.widget).getText();
				
				if(e.widget == remoteSimGroup.getHostAddrTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SIMULATOR_CONNECTION_ADDRESS, textValue);
				} else if(e.widget == remoteSimGroup.getHostPortTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SIMULATOR_CONNECTION_PORT, textValue);
				} else if(e.widget == remoteSimGroup.getUsernameTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SIMULATOR_LOGIN_USERNAME, textValue);
				} else if(e.widget == remoteSimGroup.getPasswordTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SIMULATOR_LOGIN_PASSWORD, textValue);
				} else if(e.widget == remoteSimGroup.getPublicKeyPathGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SIMULATOR_KEY_PATH, textValue);
				} else if(e.widget == remoteSimGroup.getPassphraseTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SIMULATOR_KEY_PASSPHRASE, textValue);
				} else if(e.widget == remoteSimGroup.getTimeoutTextGroup().getText()) {
					attributes.setStringAttribute(ConfigFactory.ATTR_SIMULATOR_CONNECTION_TIMEOUT, textValue);
				}
			} else if(e.widget instanceof Button) {
				boolean boolValue = e.widget == remoteSimGroup.getAuthKindSelectionButtons()[0];
				
				attributes.setBooleanAttribute(ConfigFactory.ATTR_SIMULATOR_IS_PASSWORD_AUTH, boolValue);
			} else if(e.widget instanceof Combo) {
				attributes.setAttribute(ConfigFactory.ATTR_SIMULATOR_CIPHER_TYPE, remoteSimGroup.getSelectedCipherType().getId());
			}

			// Validate fields will check the fields and update the Ok button as needed. 
			validateFieldsAndUpdateInterface();
		}
	}
	
	
	
}
