/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import java.util.Map;
import java.util.UUID;

import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ui.IMemento;

/**
 * @since 5.0
 */
public abstract class AbstractResourceManagerConfiguration extends ServiceProvider implements IResourceManagerConfiguration,
		IServiceProviderWorkingCopy {
	private static final String TAG_AUTOSTART = "autoStart"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$
	private static final String TAG_STATE = "state"; //$NON-NLS-1$

	/*
	 * If we're a working copy, keep a copy of the original
	 */
	private boolean fIsDirty = false;

	private IServiceProvider fServiceProvider = null;

	public AbstractResourceManagerConfiguration() {
	}

	/**
	 * Constructor for creating a working copy of the service provider Don't
	 * register listeners as this copy will just be discarded at some point.
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public AbstractResourceManagerConfiguration(IServiceProvider provider) {
		fServiceProvider = provider;
		setProperties(provider.getProperties());
		setDescriptor(provider.getDescriptor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AbstractResourceManagerConfiguration)) {
			return false;
		}
		return getUniqueName().equals(((AbstractResourceManagerConfiguration) obj).getUniqueName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getAutoStart()
	 */
	public boolean getAutoStart() {
		return getBoolean(TAG_AUTOSTART, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getConnectionName
	 * ()
	 */
	public String getConnectionName() {
		return getString(TAG_CONNECTION_NAME, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getDescription()
	 */
	public String getDescription() {
		return getString(TAG_DESCRIPTION, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.ServiceProvider#getName()
	 */
	@Override
	public String getName() {
		return getString(TAG_NAME, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#getOriginal()
	 */
	public IServiceProvider getOriginal() {
		return fServiceProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getRemoteServicesId
	 * ()
	 */
	public String getRemoteServicesId() {
		return getString(TAG_REMOTE_SERVICES_ID, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getResourceManagerId
	 * ()
	 */
	public String getResourceManagerId() {
		return super.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getState()
	 */
	public ResourceManagerAttributes.State getState() {
		return ResourceManagerAttributes.State.valueOf(getString(TAG_STATE, ResourceManagerAttributes.State.STOPPED.toString()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getType()
	 */
	public String getType() {
		return super.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getUniqueName()
	 */
	public String getUniqueName() {
		String name = getString(TAG_UNIQUE_NAME, null);
		if (name == null) {
			name = UUID.randomUUID().toString();
			putString(TAG_UNIQUE_NAME, name);
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		return !getConnectionName().equals("") && getRemoteServicesId() != null; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#isDirty()
	 */
	public boolean isDirty() {
		return fIsDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#
	 * needsDebuggerLaunchHelp()
	 */
	public boolean needsDebuggerLaunchHelp() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#putString(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void putString(String key, String value) {
		fIsDirty = true;
		super.putString(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProviderWorkingCopy#save()
	 */
	public void save() {
		if (fServiceProvider != null) {
			fServiceProvider.setProperties(getProperties());
			fIsDirty = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#save(org.eclipse
	 * .ui.IMemento)
	 */
	public void save(IMemento memento) {
		/*
		 * Not needed (needs to be @deprecated). Currently used to bridge
		 * between RM configurations and service configurations.
		 */
		memento.putString(TAG_UNIQUE_NAME, getUniqueName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setAutoStart(boolean
	 * )
	 */
	public void setAutoStart(boolean flag) {
		putBoolean(TAG_AUTOSTART, flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setConnectionName
	 * (java.lang.String)
	 */
	public void setConnectionName(String name) {
		putString(TAG_CONNECTION_NAME, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDescription
	 * (java.lang.String)
	 */
	public void setDescription(String description) {
		putString(TAG_DESCRIPTION, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setName(java.lang
	 * .String)
	 */
	public void setName(String name) {
		putString(TAG_NAME, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#setProperties(java.util
	 * .Map)
	 */
	@Override
	public void setProperties(Map<String, String> properties) {
		fIsDirty = true;
		super.setProperties(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setRemoteServicesId
	 * (java.lang.String)
	 */
	public void setRemoteServicesId(String id) {
		putString(TAG_REMOTE_SERVICES_ID, id);
	}

	/**
	 * @param id
	 */
	public void setResourceManagerId(String id) {
		// Do nothing (needs to be @deprecated)
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setState(org.eclipse
	 * .ptp.core.elements.attributes.ResourceManagerAttributes.State)
	 */
	public void setState(ResourceManagerAttributes.State state) {
		putString(TAG_STATE, state.name());
	}

	/**
	 * Set the IResourceManagerConfiguration unique name. This is only used to
	 * transition to the new service model framework. It is set to the name of
	 * the service configuration that was created for this service provider.
	 * 
	 * @param id
	 */
	public void setUniqueName(String id) {
		putString(TAG_UNIQUE_NAME, id);
	}
}
