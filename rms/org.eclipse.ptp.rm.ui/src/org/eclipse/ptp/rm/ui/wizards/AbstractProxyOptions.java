/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract base class for proxy options area on resource manager configuration.
 * 
 * Clients should provide an implementation of
 * {@link #createContents(Composite)} that creates the widgets required for the
 * proxy options. An implementation of the {@link #save()} method should be
 * provided that saves the current state of the widgets to the configuration.
 * page.
 * 
 * The client is responsible for validating the contents of the widgets prior to
 * {@link #save()} being called.
 * 
 * @since 2.0
 */
public abstract class AbstractProxyOptions {

	private Shell fShell;
	private final WizardPage fWizardPage;
	private final IRemoteResourceManagerConfiguration fConfig;
	private IRemoteConnection fRemoteConnection;

	public AbstractProxyOptions(WizardPage wizardPage, IRemoteResourceManagerConfiguration config) {
		fWizardPage = wizardPage;
		fConfig = config;
	}

	/**
	 * Create the widgets for the proxy options configuration area
	 * 
	 */
	protected abstract Composite createContents(Composite parent);

	/**
	 * @return
	 */
	protected IRemoteResourceManagerConfiguration getConfiguration() {
		return fConfig;
	}

	/**
	 * @return
	 */
	protected IRemoteConnection getRemoteConnection() {
		return fRemoteConnection;
	}

	/**
	 * @return
	 */
	protected Shell getShell() {
		return fShell;
	}

	/**
	 * @return
	 */
	protected WizardPage getWizardPage() {
		return fWizardPage;
	}

	/**
	 * Called when configuration should be saved
	 */
	protected abstract void save();

	/**
	 * @param conn
	 */
	protected void setRemoteConnection(IRemoteConnection conn) {
		fRemoteConnection = conn;
	}

	/**
	 * @param shell
	 */
	protected void setShell(Shell shell) {
		fShell = shell;
	}
}
