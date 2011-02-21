/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California and others.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 * Contributors:
 *    Albert L. Rossi (NCSA)  -- modified to disable proxy path for
 *    							 automatically deployed RMs
 *    						  -- modified to allow subclasses to expose extra properties/widgets (2010/11/04)
 *******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.rm.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.xml.JAXBUtils;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetUtils;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;

/**
 * Generic Wizard for the JAXB Resource Manager.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMControlConfigurationWizardPage extends AbstractControlMonitorRMConfigurationWizardPage {

	private String[] types;
	private Combo rmTypes;
	private final Properties rmXmlNames;
	private IJAXBResourceManagerConfiguration jaxbConfig;

	public JAXBRMControlConfigurationWizardPage(IRMConfigurationWizard wizard) {
		this(wizard, Messages.JAXBRMControlConfigurationWizardPage_Title);
	}

	public JAXBRMControlConfigurationWizardPage(IRMConfigurationWizard wizard, String pageName) {
		super(wizard, pageName);
		setPageComplete(false);
		isValid = false;
		setTitle(Messages.JAXBRMControlConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConfigurationWizardPage_Description);
		rmXmlNames = new Properties();
		setAvailableConfigurations();
		targetPathEnabled = true;
		targetOptionsEnabled = false;
		multiplexingEnabled = false;
		fManualLaunchEnabled = false;
		connectionSharingEnabled = false;
	}

	@Override
	protected void addCustomWidgets(Composite parent) {
		new Label(parent, SWT.NONE);
		Group mxGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(3, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 3));
		mxGroup.setText(Messages.JAXBRMSchemaComboGroupTitle);

		rmTypes = WidgetUtils.createItemCombo(mxGroup, Messages.JAXBRMSchemaComboTitle, types, ZEROSTR, ZEROSTR, true, null, 2);
		rmTypes.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				String text = rmTypes.getText();
				if (text != null && text.length() > 0) {
					String location = rmXmlNames.getProperty(text);
					jaxbConfig.setRMInstanceXMLLocation(location);
					try {
						ResourceManagerData data = JAXBUtils.initializeRMData(location);
						if (data != null)
							jaxbConfig.setResourceManagerData(data);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					updateSettings();
				}
			}
		});
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button.
	 * Calls handleRemoteServicesSelected() to update the connection combo with
	 * the new connection.
	 * 
	 * TODO should probably select the new connection
	 */
	@Override
	protected void handleNewRemoteConnectionSelected() {
		if (uiConnectionManager != null) {
			Map<String, String> defaults = new HashMap<String, String>();
			defaults.put(ConfigFactory.ATTR_CONNECTION_ADDRESS, jaxbConfig.getDefaultControlHost());
			defaults.put(ConfigFactory.ATTR_CONNECTION_PORT, jaxbConfig.getDefaultControlPort());
			handleRemoteServiceSelected(uiConnectionManager.newConnection(getShell(), defaults));
		}
	}

	@Override
	protected void initContents() {
		super.initContents();
		jaxbConfig = (IJAXBResourceManagerConfiguration) config;
		String rmConfigPath = jaxbConfig.getRMInstanceXMLLocation();
		if (rmConfigPath != null && rmConfigPath.length() != 0)
			for (int i = 0; i < types.length; i++) {
				String path = rmXmlNames.getProperty(types[i]);
				if (rmConfigPath.equals(path)) {
					rmTypes.select(i);
					break;
				}
			}
	}

	@Override
	protected boolean isValidSetting() {
		String choice = rmTypes.getText();
		if (choice == null || choice.length() == 0)
			return false;
		return super.isValidSetting();
	}

	@Override
	protected void loadConnectionOptions() {
		targetPath = config.getControlPath();
		targetArgs = config.getControlInvocationOptionsStr();
	}

	@Override
	protected void setConnectionName(String name) {
		if (name != null)
			jaxbConfig.setConnectionName(name, CONTROL_CONNECTION_NAME);
	}

	@Override
	protected void setConnectionOptions() {
		config.setControlPath(targetPath);
		config.setControlInvocationOptions(targetArgs);
	}

	@Override
	protected void updateSettings() {
		super.updateSettings();
		handleActivate();
	}

	private void getAvailableConfigurations() throws IOException {
		rmXmlNames.clear();
		URL url = null;
		if (JAXBCorePlugin.getDefault() != null) {
			Bundle bundle = JAXBCorePlugin.getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(DATA + RM_CONFIG_PROPS), null);
		} else
			url = new File(RM_CONFIG_PROPS).toURL();

		if (url == null)
			return;
		InputStream s = null;
		try {
			s = url.openStream();
			rmXmlNames.load(s);
		} finally {
			try {
				if (s != null)
					s.close();
			} catch (IOException e) {
			}
		}
	}

	private void handleActivate() {
		boolean deactivate = rmTypes.getText().length() == 0;
		if (targetPathText != null) {
			if (deactivate)
				targetPathText.setText(ZEROSTR);
			else
				targetPathText.setText(jaxbConfig.getDefaultControlPath());
			targetPathText.setEnabled(!deactivate);
		}
		if (remoteCombo != null) {
			if (deactivate)
				remoteCombo.setText(ZEROSTR);
			remoteCombo.setEnabled(!deactivate);
		}
		if (localAddrCombo != null) {
			if (deactivate)
				localAddrCombo.setText(ZEROSTR);
			localAddrCombo.setEnabled(!deactivate);
		}
		if (connectionCombo != null) {
			if (deactivate)
				connectionCombo.setText(ZEROSTR);
			connectionCombo.setEnabled(!deactivate);
		}
		if (null != optionsButton)
			optionsButton.setEnabled(!deactivate);
		if (null != browseButton)
			browseButton.setEnabled(!deactivate);
		if (null != noneButton)
			noneButton.setEnabled(!deactivate);
		if (null != portForwardingButton)
			portForwardingButton.setEnabled(!deactivate);
		if (null != manualButton)
			manualButton.setEnabled(!deactivate);
		if (null != newConnectionButton)
			newConnectionButton.setEnabled(!deactivate);
		if (null != shareConnectionButton)
			shareConnectionButton.setEnabled(!deactivate);
	}

	private void setAvailableConfigurations() {
		try {
			getAvailableConfigurations();
			types = rmXmlNames.keySet().toArray(new String[0]);
		} catch (IOException t) {
			t.printStackTrace();
			types = new String[0];
		}
	}
}
