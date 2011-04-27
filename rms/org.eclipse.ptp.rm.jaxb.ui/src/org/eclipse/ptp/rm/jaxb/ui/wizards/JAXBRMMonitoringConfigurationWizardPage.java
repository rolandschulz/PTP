/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.net.URI;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.SiteType;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.RemoteUIServicesUtils;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.widgets.Composite;

/**
 * Generic Wizard for the JAXB Resource Manager Monitoring. Provides connection
 * configuration.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMMonitoringConfigurationWizardPage extends AbstractRemoteResourceManagerConfigurationWizardPage implements
		IJAXBUINonNLSConstants {

	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard) {
		this(wizard, Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
	}

	/**
	 * @param wizard
	 * @param pageName
	 */
	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard, String pageName) {
		super(wizard, pageName);
		setPageComplete(false);
		setTitle(Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConnectionWizardPage_Description);
		setEnableUseDefault(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_3b);
	}

	/*
	 * Exports as default remote connection information whatever is set in the
	 * Site subtree of the JAXB data tree. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.
	 * AbstractRemoteResourceManagerConfigurationWizardPage
	 * #doCreateContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Composite doCreateContents(Composite parent) {
		Composite comp = super.doCreateContents(parent);
		try {
			SiteType site = getBaseConfiguration().getResourceManagerData().getSiteData();
			if (site != null && site.getMonitorConnection() != null && getConfiguration().getRemoteServicesId() == null) {
				/*
				 * Configuration has not be initialized, so initialize now with
				 * values from XML
				 */
				URI uri = new URI(site.getMonitorConnection());
				IRemoteServices services = PTPRemoteUIPlugin.getDefault().getRemoteServices(uri, getWizard().getContainer());
				if (services != null) {
					getConfiguration().setRemoteServicesId(services.getId());

					String auth = uri.getAuthority();
					String host = uri.getHost();
					String user = uri.getUserInfo();
					int port = uri.getPort();

					if (auth != null && host != null) {
						IRemoteConnection conn = services.getConnectionManager().getConnection(host);
						if (conn == null && services.canCreateConnections()) {
							conn = services.getConnectionManager().newConnection(host);
							conn.setAddress(host);

							if (!auth.equals(host)) {
								if (user != null) {
									conn.setUsername(user);
								}
								if (port != -1) {
									conn.setPort(port);
								}
							}
						}
						if (conn != null) {
							getConfiguration().setConnectionName(conn.getName());
							RemoteUIServicesUtils.setConnectionHints(connectionWidget, conn);
						}
					}
				}
			}
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
		}
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#getConfiguration()
	 */
	@Override
	protected IJAXBResourceManagerConfiguration getConfiguration() {
		return (IJAXBResourceManagerConfiguration) super.getConfiguration();
	}

	private IJAXBResourceManagerConfiguration getBaseConfiguration() {
		return (IJAXBResourceManagerConfiguration) ((IRMConfigurationWizard) getWizard()).getBaseConfiguration();
	}
}
