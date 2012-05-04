/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * Configuration object used for persisting values between sessions. Also contains (in memory) the JAXB data object tree and the
 * active instance of the environment map.<br>
 * <br>
 * There are actually three such configurations associated with a JAXB resource manager instance: the base configuration, and the
 * control and monitor configurations. The latter two contain references to their parent base provider. <br>
 * This abstract class provides the access and construction functionality for the data tree and the variable map.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractJAXBResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration implements
		IJAXBResourceManagerConfiguration {

	protected ResourceManagerData rmdata;

	/**
	 * @param namespace
	 *            base, control or monitor
	 * @param provider
	 *            base provider configuration
	 */
	protected AbstractJAXBResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		super(namespace, provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration# getResourceManagerData()
	 */
	@Override
	public ResourceManagerData getResourceManagerData() {
		if (rmdata == null) {
			try {
				realizeRMDataFromXML();
			} catch (CoreException e) {
			}
		}
		return rmdata;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration#initialize()
	 */
	@Override
	public void initialize() throws CoreException {
		realizeRMDataFromXML();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration# setRMConfigurationURL(java.net.URL)
	 */
	@Override
	public void setRMConfigurationURL(URL url) {
		if (url != null) {
			putString(JAXBCoreConstants.RM_URL, url.toExternalForm());
		}
	}

	/**
	 * Nulls out the tree as well as related service ids.
	 */
	private void clearRMData() {
		rmdata = null;
		setRemoteServicesId(null);
		setConnectionName(null);
		setInvocationOptions(JAXBCoreConstants.ZEROSTR);
		setLocalAddress(JAXBCoreConstants.ZEROSTR);
	}

	/*
	 * return JAXBInitializationUtils.getRMConfigurationXML(url);
	 */

	/**
	 * @return the configuration XML used to construct the data tree.
	 */
	private String getRMConfigurationXML() {
		String xml = getString(JAXBCoreConstants.RM_XML, JAXBCoreConstants.ZEROSTR);
		if (JAXBCoreConstants.ZEROSTR.equals(xml)) {
			return null;
		}
		return xml;
	}

	/**
	 * Unmarshals the XML into the JAXB data tree.<br>
	 * <br>
	 * If the current xml is <code>null</code>, or if the "force reload" preference is set, a fresh attempt is made to store the xml
	 * from the location. Otherwise, the cached xml is used.
	 * 
	 * @throws unmarshaling
	 *             or URL exceptions
	 */
	private void realizeRMDataFromXML() throws CoreException {
		String xml = getRMConfigurationXML();
		boolean force = Preferences.getBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD);
		if (xml == null || force) {
			String location = getString(JAXBCoreConstants.RM_URL, null);
			if (location != null) {
				try {
					xml = JAXBInitializationUtils.getRMConfigurationXML(new URL(location));
					setRMConfigurationXML(xml);
				} catch (Throwable t) {
					if (xml != null) {
						new UIJob(Messages.CachedDefinitionWarning) {
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.CachedDefinitionWarning,
										Messages.UsingCachedDefinition);
								return Status.OK_STATUS;
							}
						}.schedule();
					}
				}
			}
		}
		if (xml == null) {
			throw CoreExceptionUtils.newException(Messages.FailedToCreateRmData, null);
		}
		try {
			rmdata = JAXBInitializationUtils.initializeRMData(xml);
		} catch (Exception e) {
			throw CoreExceptionUtils.newException(Messages.FailedToCreateRmData, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration# setRMConfigurationXML(java.lang.String)
	 */
	private void setRMConfigurationXML(String xml) {
		if (xml != null) {
			putString(JAXBCoreConstants.RM_XML, xml);
			clearRMData();
		}
	}
}
