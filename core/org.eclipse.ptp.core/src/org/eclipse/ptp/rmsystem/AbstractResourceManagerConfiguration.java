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

import java.util.UUID;

import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceProvider;

/**
 * @since 5.0
 */
public abstract class AbstractResourceManagerConfiguration extends ServiceProvider implements IResourceManagerConfiguration,
		IResourceManagerComponentConfiguration {
	public static final String BASE = "base."; //$NON-NLS-1$
	public static final String CONTROL = "control."; //$NON-NLS-1$
	public static final String MONITOR = "monitor."; //$NON-NLS-1$

	private static final String TAG_AUTOSTART = "autoStart"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$

	private IServiceProvider fServiceProvider = null;
	private String fNamespace = BASE;

	public AbstractResourceManagerConfiguration() {
	}

	public AbstractResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		/*
		 * Only allow one level of nesting
		 */
		if (provider instanceof AbstractResourceManagerConfiguration) {
			IServiceProvider baseProvider = ((AbstractResourceManagerConfiguration) provider).getServiceProvider();
			if (baseProvider != null) {
				provider = baseProvider;
			}
		}
		fServiceProvider = provider;
		fNamespace = namespace;
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
		return getBoolean(BASE, TAG_AUTOSTART, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#getBoolean(java.lang.String
	 * , boolean)
	 */
	@Override
	public boolean getBoolean(String key, boolean defaultVal) {
		return getBoolean(fNamespace, key, defaultVal);
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
		return getString(BASE, TAG_DESCRIPTION, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#getInt(java.lang.String,
	 * int)
	 */
	@Override
	public int getInt(String key, int defaultVal) {
		return getInt(fNamespace, key, defaultVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getName()
	 */
	@Override
	public String getName() {
		return getString(BASE, TAG_NAME, ""); //$NON-NLS-1$
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
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#getString(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getString(String key, String defaultVal) {
		return getString(fNamespace, key, defaultVal);
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
		String name = getString(BASE, TAG_UNIQUE_NAME, null);
		if (name == null) {
			name = UUID.randomUUID().toString();
			putString(BASE, TAG_UNIQUE_NAME, name);
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
	 * org.eclipse.ptp.services.core.ServiceProvider#putBoolean(java.lang.String
	 * , boolean)
	 */
	@Override
	public void putBoolean(String key, boolean value) {
		putBoolean(fNamespace, key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.ServiceProvider#putInt(java.lang.String,
	 * int)
	 */
	@Override
	public void putInt(String key, int value) {
		putInt(fNamespace, key, value);
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
		putString(fNamespace, key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setAutoStart(boolean
	 * )
	 */
	public void setAutoStart(boolean flag) {
		putBoolean(BASE, TAG_AUTOSTART, flag);
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
		putString(BASE, TAG_DESCRIPTION, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setName(java.lang
	 * .String)
	 */
	public void setName(String name) {
		putString(BASE, TAG_NAME, name);
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
	 * Set the IResourceManagerConfiguration unique name. This is only used to
	 * transition to the new service model framework. It is set to the name of
	 * the service configuration that was created for this service provider.
	 * 
	 * @param id
	 */
	public void setUniqueName(String id) {
		putString(BASE, TAG_UNIQUE_NAME, id);
	}

	private boolean getBoolean(String namespace, String key, boolean defaultVal) {
		if (fServiceProvider != null) {
			return fServiceProvider.getBoolean(namespace + key, defaultVal);
		}
		return super.getBoolean(namespace + key, defaultVal);
	}

	private int getInt(String namespace, String key, int defaultVal) {
		if (fServiceProvider != null) {
			return fServiceProvider.getInt(namespace + key, defaultVal);
		}
		return super.getInt(namespace + key, defaultVal);
	}

	private String getString(String namespace, String key, String defaultVal) {
		if (fServiceProvider != null) {
			return fServiceProvider.getString(namespace + key, defaultVal);
		}
		return super.getString(namespace + key, defaultVal);
	}

	private void putBoolean(String namespace, String key, boolean value) {
		if (fServiceProvider != null) {
			fServiceProvider.putBoolean(namespace + key, value);
		} else {
			super.putBoolean(namespace + key, value);
		}
	}

	private void putInt(String namespace, String key, int value) {
		if (fServiceProvider != null) {
			fServiceProvider.putInt(namespace + key, value);
		} else {
			super.putInt(namespace + key, value);
		}
	}

	private void putString(String namespace, String key, String value) {
		if (fServiceProvider != null) {
			fServiceProvider.putString(namespace + key, value);
		} else {
			super.putString(namespace + key, value);
		}
	}

	protected IServiceProvider getServiceProvider() {
		return fServiceProvider;
	}

}
