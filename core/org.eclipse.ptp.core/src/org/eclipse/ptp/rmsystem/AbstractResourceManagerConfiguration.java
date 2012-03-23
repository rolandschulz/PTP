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

import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * @since 5.0
 */
public abstract class AbstractResourceManagerConfiguration implements IResourceManagerConfiguration,
		IResourceManagerComponentConfiguration {
	public static final String BASE = "base."; //$NON-NLS-1$
	public static final String CONTROL = "control."; //$NON-NLS-1$
	public static final String MONITOR = "monitor."; //$NON-NLS-1$

	private static final String TAG_AUTOSTART = "autoStart"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$

	private final IServiceProvider fServiceProvider;
	private final String fNamespace;

	public AbstractResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		fServiceProvider = provider;
		fNamespace = namespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.ServiceProvider#equals(java.lang.Object)
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
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		}
		if (adapter == IServiceProvider.class) {
			return fServiceProvider;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getAutoStart()
	 */
	public boolean getAutoStart() {
		return getBoolean(BASE, TAG_AUTOSTART, false);
	}

	/**
	 * Get boolean option from namespace
	 * 
	 * @param key
	 *            option key
	 * @param defaultVal
	 *            default value
	 * @return boolean value of key or defaultVal if no option is defined
	 */
	public boolean getBoolean(String key, boolean defaultVal) {
		return getBoolean(fNamespace, key, defaultVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration# getConnectionName()
	 */
	public String getConnectionName() {
		return getString(TAG_CONNECTION_NAME, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getDescription()
	 */
	public String getDescription() {
		return getString(BASE, TAG_DESCRIPTION, ""); //$NON-NLS-1$
	}

	/**
	 * Get integer option from namespace
	 * 
	 * @param key
	 *            option key
	 * @param defaultVal
	 *            default value
	 * @return integer value of key or defaultVal if no option is defined
	 */
	public int getInt(String key, int defaultVal) {
		return getInt(fNamespace, key, defaultVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getName()
	 */
	public String getName() {
		return getString(BASE, TAG_NAME, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration# getRemoteServicesId()
	 */
	public String getRemoteServicesId() {
		return getString(TAG_REMOTE_SERVICES_ID, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getResourceManagerId ()
	 */
	public String getResourceManagerId() {
		return fServiceProvider.getId();
	}

	/**
	 * Get string option from namespace
	 * 
	 * @param key
	 *            option key
	 * @param defaultVal
	 *            default value
	 * @return string value of key or defaultVal if no option is defined
	 */
	public String getString(String key, String defaultVal) {
		return getString(fNamespace, key, defaultVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getType()
	 */
	public String getType() {
		return fServiceProvider.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getUniqueName()
	 */
	public String getUniqueName() {
		return ((ResourceManagerServiceProvider) fServiceProvider).getUniqueName();
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
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration# needsDebuggerLaunchHelp()
	 */
	public boolean needsDebuggerLaunchHelp() {
		return false;
	}

	/**
	 * Put boolean option into namespace
	 * 
	 * @param key
	 *            option key
	 * @param value
	 *            option value
	 */
	public void putBoolean(String key, boolean value) {
		putBoolean(fNamespace, key, value);
	}

	/**
	 * Put integer option into namespace
	 * 
	 * @param key
	 *            option key
	 * @param value
	 *            option value
	 */
	public void putInt(String key, int value) {
		putInt(fNamespace, key, value);
	}

	/**
	 * Put string option into namespace
	 * 
	 * @param key
	 *            option key
	 * @param value
	 *            option value
	 */
	public void putString(String key, String value) {
		putString(fNamespace, key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setAutoStart(boolean )
	 */
	public void setAutoStart(boolean flag) {
		putBoolean(BASE, TAG_AUTOSTART, flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration# setConnectionName(java.lang.String)
	 */
	public void setConnectionName(String name) {
		putString(TAG_CONNECTION_NAME, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDescription (java.lang.String)
	 */
	public void setDescription(String description) {
		putString(BASE, TAG_DESCRIPTION, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setName(java.lang .String)
	 */
	public void setName(String name) {
		putString(BASE, TAG_NAME, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration# setRemoteServicesId(java.lang.String)
	 */
	public void setRemoteServicesId(String id) {
		putString(TAG_REMOTE_SERVICES_ID, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setUniqueName(java.lang.String)
	 */
	/**
	 * @since 6.0
	 */
	public void setUniqueName(String name) {
		ModelManager.getInstance().changeResourceManagerUniqueName(getUniqueName(), name);
		((ResourceManagerServiceProvider) fServiceProvider).setUniqueName(name);
	}

	private boolean getBoolean(String namespace, String key, boolean defaultVal) {
		return fServiceProvider.getBoolean(namespace + key, defaultVal);
	}

	private int getInt(String namespace, String key, int defaultVal) {
		return fServiceProvider.getInt(namespace + key, defaultVal);
	}

	private String getString(String namespace, String key, String defaultVal) {
		return fServiceProvider.getString(namespace + key, defaultVal);
	}

	private void putBoolean(String namespace, String key, boolean value) {
		fServiceProvider.putBoolean(namespace + key, value);
	}

	private void putInt(String namespace, String key, int value) {
		fServiceProvider.putInt(namespace + key, value);
	}

	private void putString(String namespace, String key, String value) {
		fServiceProvider.putString(namespace + key, value);
	}
}
