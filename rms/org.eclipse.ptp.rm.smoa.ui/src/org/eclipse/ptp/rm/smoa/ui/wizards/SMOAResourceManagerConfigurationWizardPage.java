/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.wizards;

import java.io.File;

import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration.AuthType;
import org.eclipse.ptp.rm.smoa.core.rmsystem.SMOAResourceManager;
import org.eclipse.ptp.rm.smoa.core.rmsystem.SMOAResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * GUI for configuring {@link SMOAResourceManager} (or rather:
 * {@link SMOAResourceManagerConfiguration}).
 * 
 * Is part of the new Resource Manager wizard, pointed by
 * {@link SMOAProviderContributor}.
 */
public class SMOAResourceManagerConfigurationWizardPage extends RMConfigurationWizardPage {

	private final SMOAResourceManagerConfiguration conf;

	// GUI elements

	// main part
	private Text host;
	private Spinner port;

	// selecting authentication
	private Button anonymous;
	private Button username;
	private Button gsiauth;

	// adaptive GUI
	private Composite userData;
	private StackLayout stackLayout;

	// adaptive GUI composites
	private Composite anonymousTab;
	private Composite userpasswdTab;
	private Composite gsiTab;

	// contents of userpasswdTab
	private Text user;
	private Text passwd;
	private Text cacert;
	private Text dn;

	// contents of gsiTab
	private Text gsi_dn;

	AuthType previousAuth = null;

	public SMOAResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.SMOAResourceManagerConfigurationWizardPage_SmoaComputingConfiguration);
		setTitle(Messages.SMOAResourceManagerConfigurationWizardPage_SmoaComputingConfiguration);
		setDescription(Messages.SMOAResourceManagerConfigurationWizardPage_SmoaComputingConfiguration_description);
		conf = (SMOAResourceManagerConfiguration) wizard.getBaseConfiguration();
	}

	/** Host and port part */
	private void createAddress(Composite parent) {

		final Composite myMainComp = new Composite(parent, SWT.NONE);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;

		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		myMainComp.setLayoutData(gd);

		myMainComp.setLayout(gridLayout);

		Label label;
		// URI

		label = new Label(myMainComp, SWT.NONE);
		label.setText(Messages.SMOAResourceManagerConfigurationWizardPage_Hostname);
		label.setLayoutData(new GridData());

		host = new Text(myMainComp, SWT.SINGLE | SWT.BORDER);
		if (conf.getUrl() != null) {
			host.setText(conf.getUrl());
		}
		host.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		host.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				setPageComplete(isValid());
				conf.setUrl(host.getText());
			}
		});

		// PORT

		label = new Label(myMainComp, SWT.NONE);
		label.setText(Messages.SMOAResourceManagerConfigurationWizardPage_Port);
		label.setLayoutData(new GridData());

		port = new Spinner(myMainComp, SWT.SINGLE | SWT.BORDER);
		port.setMaximum(65535);
		port.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		port.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				final int portNo = Integer.parseInt(port.getText());
				assert portNo >= 0 && portNo < 65536 : "Java spinner violates boundaries"; //$NON-NLS-1$
				conf.setPort(portNo);
			}
		});
		if (conf.getPort() != null) {
			port.setSelection(conf.getPort());
		} else {
			port.setSelection(19000);
		}
	}

	/** Anonymous authentication part */
	private Composite createAnonymous(Composite topComposite) {
		return new Composite(topComposite, SWT.NONE);
	}

	private void createContents(Composite parent) {

		final GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		mainLayout.marginWidth = 0;

		parent.setLayout(mainLayout);

		createAddress(parent);

		new Label(parent, SWT.NONE).setLayoutData(new GridData());

		createUserData(parent);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		createContents(composite);
		setControl(composite);
		setPageComplete(isValid());
	}

	/** GSI authentication part */
	private Composite createGSI(Composite topComposite) {

		final Composite c = new Composite(topComposite, SWT.NONE);
		c.setLayout(new GridLayout(2, false));

		new Label(c, SWT.NONE).setText(Messages.SMOAResourceManagerConfigurationWizardPage_ServiceDN);

		gsi_dn = new Text(c, SWT.SINGLE | SWT.BORDER);
		if (conf.getServiceDN() != null) {
			gsi_dn.setText(conf.getServiceDN());
		}
		gsi_dn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gsi_dn.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				if (!dn.getText().equals(gsi_dn.getText())) {
					dn.setText(gsi_dn.getText());
				}
			}
		});

		return c;
	}

	/** Authentication selection part */
	private void createUserData(Composite mainComp) {

		final Composite authType = new Composite(mainComp, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		authType.setLayout(gridLayout);

		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		authType.setLayoutData(gd);

		final Label l = new Label(authType, SWT.NONE);
		l.setText(Messages.SMOAResourceManagerConfigurationWizardPage_ChooseAuthenticationType);
		final GridData gridData = new GridData(GridData.CENTER);
		gridData.horizontalSpan = 3;
		l.setLayoutData(gridData);

		anonymous = new Button(authType, SWT.RADIO);
		anonymous.setText(Messages.SMOAResourceManagerConfigurationWizardPage_AuthTypeAnonymous);
		anonymous.setLayoutData(new GridData());

		username = new Button(authType, SWT.RADIO);
		username.setText(Messages.SMOAResourceManagerConfigurationWizardPage_AuthTypeUserTokenProfile);
		username.setLayoutData(new GridData());

		gsiauth = new Button(authType, SWT.RADIO);
		gsiauth.setText(Messages.SMOAResourceManagerConfigurationWizardPage_AuthTypeGSI);
		gsiauth.setLayoutData(new GridData());

		// ///////

		userData = new Composite(mainComp, SWT.NONE);
		userData.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		stackLayout = new StackLayout();
		userData.setLayout(stackLayout);

		anonymousTab = createAnonymous(userData);
		userpasswdTab = createUserPassword(userData);
		gsiTab = createGSI(userData);

		userData.layout();

		previousAuth = conf.getAuthType();
		switch (previousAuth) {
		case Anonymous:
			anonymous.setSelection(true);
			toggleUsernamePassword();
			break;
		case UsernamePassword:
			username.setSelection(true);
			toggleUsernamePassword();
			break;
		case GSI:
			gsiauth.setSelection(true);
			toggleUsernamePassword();
			break;
		}

		anonymous.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			public void widgetSelected(SelectionEvent arg0) {
				setPageComplete(isValid());
				toggleUsernamePassword();
			}
		});

		username.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			public void widgetSelected(SelectionEvent arg0) {
				setPageComplete(isValid());
				toggleUsernamePassword();
			}
		});

		gsiauth.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			public void widgetSelected(SelectionEvent arg0) {
				setPageComplete(isValid());
				toggleUsernamePassword();
			}
		});

	}

	/** User name & password + certificate authentication part */
	private Composite createUserPassword(Composite topComposite) {
		GridData gridData;
		Label l;

		final Composite userPassword = new Composite(topComposite, SWT.NONE);

		userPassword.setLayout(new GridLayout(3, false));

		final GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.horizontalSpan = 2;
		userPassword.setLayoutData(gd2);

		// User name

		final Label userLabel = new Label(userPassword, SWT.NONE);
		userLabel.setText(Messages.SMOAResourceManagerConfigurationWizardPage_Username);
		userLabel.setLayoutData(new GridData());

		user = new Text(userPassword, SWT.SINGLE | SWT.BORDER);
		if (conf.getUser() != null) {
			user.setText(conf.getUser());
		}
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		user.setLayoutData(gridData);

		user.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				setPageComplete(isValid());
				conf.setUser(user.getText());
			}
		});

		// Password

		final Label passwdLabel = new Label(userPassword, SWT.NONE);
		passwdLabel.setText(Messages.SMOAResourceManagerConfigurationWizardPage_Password);
		passwdLabel.setLayoutData(new GridData());

		passwd = new Text(userPassword, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		if (conf.getPassword() != null) {
			passwd.setText(conf.getPassword());
		}
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		passwd.setLayoutData(gridData);

		passwd.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				conf.setPassword(passwd.getText());

			}
		});

		// CA cert

		l = new Label(userPassword, SWT.NONE);
		l.setText(Messages.SMOAResourceManagerConfigurationWizardPage_CaCert);
		l.setLayoutData(new GridData());

		cacert = new Text(userPassword, SWT.SINGLE | SWT.BORDER);
		if (conf.getCaCertPath() != null) {
			cacert.setText(conf.getCaCertPath());
		}
		cacert.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button cacertBrowser = new Button(userPassword, SWT.PUSH);
		cacertBrowser.setText(Messages.SMOAResourceManagerConfigurationWizardPage_Browse);
		cacertBrowser.setLayoutData(new GridData());
		cacertBrowser.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				final FileDialog dialog = new FileDialog(new Shell(), SWT.OPEN);
				dialog.setFileName(cacert.getText());
				final String _ = dialog.open();
				if (null != _) {
					cacert.setText(_);
				}
			}
		});

		// DN

		final Label label = new Label(userPassword, SWT.NONE);
		label.setText(Messages.SMOAResourceManagerConfigurationWizardPage_ServiceDN);
		label.setLayoutData(new GridData());

		dn = new Text(userPassword, SWT.SINGLE | SWT.BORDER);
		if (conf.getServiceDN() != null) {
			dn.setText(conf.getServiceDN());
		}
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		dn.setLayoutData(gridData);
		dn.setEnabled(!cacert.getText().isEmpty());
		dn.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				gsi_dn.setText(dn.getText());
				conf.setServiceDn(dn.getText());
				setPageComplete(isValid());
			}
		});

		// Finalizing

		cacert.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				conf.setCacertPath(cacert.getText());
				dn.setEnabled(!cacert.getText().isEmpty());
				setPageComplete(isValid());
			}
		});

		return userPassword;
	}

	/**
	 * Checks if the settings are acceptable (i.e. is it worth to try to connect
	 * using them)
	 */
	public boolean isValid() {
		if (host != null && host.getText().isEmpty()) {
			return false;
		}

		if (username != null && username.getSelection()) {
			if (user.getText().isEmpty()) {
				return false;
			}
			if ((!cacert.getText().isEmpty()) && (!new File(cacert.getText()).exists())) {
				return false;
			}
		}

		return true;
	}

	/** Reacts on the authentication type change */
	private void toggleUsernamePassword() {

		if (username.getSelection()) {
			conf.setAuthType(AuthType.UsernamePassword);
			stackLayout.topControl = userpasswdTab;
			userData.layout();
			if (previousAuth != AuthType.UsernamePassword) {
				try {
					previousAuth = AuthType.UsernamePassword;
					conf.trigerSecureStorage();
				} catch (final StorageException e) {
					final MessageBox mb = new MessageBox(getShell(), SWT.ERROR | SWT.OK | SWT.ICON_ERROR);
					mb.setText(Messages.SMOAResourceManagerConfigurationWizardPage_SecureStorageError);
					mb.setMessage(e.getLocalizedMessage());
					mb.open();
				}
			}
		} else if (anonymous.getSelection()) {
			conf.setAuthType(AuthType.Anonymous);
			stackLayout.topControl = anonymousTab;
			userData.layout();
		} else if (gsiauth.getSelection()) {
			conf.setAuthType(AuthType.GSI);
			stackLayout.topControl = gsiTab;
			userData.layout();
		} else {
			throw new RuntimeException("Boo!"); //$NON-NLS-1$
		}

		previousAuth = conf.getAuthType();
		setPageComplete(isValid());
	}
}
