/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 *     						  - modifications to store template and memento
 *     							(05/11/2010)
 *                            - modifications to use new interface methods and
 *                              constants (09/14/2010)
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerServiceProvider;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;

/**
 * Service provider for PBS batch scheduler.
 */
public class PBSServiceProvider extends AbstractRemoteResourceManagerServiceProvider implements IPBSResourceManagerConfiguration,
		IPBSNonNLSConstants {

	public PBSServiceProvider() {
		super();
		setDescription(Messages.PBSResourceManager);
	}

	/**
	 * Constructor for creating a working copy of the service provider
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public PBSServiceProvider(IServiceProvider provider) {
		super(provider);
	}

	/**
	 * @since 5.0
	 */
	public void addTemplate(String name, String serialized) {
		addTemplateName(name);
		putString(TEMPLATE_PREFIX + name, serialized);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.ServiceProvider#copy()
	 */
	@Override
	public IServiceProviderWorkingCopy copy() {
		return new PBSServiceProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IResourceManagerControl.class) {
			return new PBSResourceManager(PTPCorePlugin.getDefault().getModelManager().getUniverse(), this);
		}
		return null;
	}

	/**
	 * @return name of the current template for this resource manager (set in
	 *         the edit wizard).
	 * @since 5.0
	 */
	public String getCurrentTemplateName() {
		return getString(getResourceManagerId() + CURR_TEMPLATE, ZEROSTR);
	}

	/**
	 * @since 5.0
	 */
	public String getProxyConfiguration() {
		return getString(PROXY_CONFIG_TYPE, ZEROSTR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * getResourceManagerId()
	 */
	@Override
	public String getResourceManagerId() {
		return getId();
	}

	/**
	 * @since 5.0
	 */
	public String getTemplate(String name) {
		return getString(TEMPLATE_PREFIX + name, null);
	}

	/**
	 * @since 5.0
	 */
	public String[] getTemplateNames() {
		String nameList = getString(TEMPLATE_NAMES, null);
		if (nameList == null)
			return new String[0];
		return nameList.split(CM);
	}

	/**
	 * @since 5.0
	 */
	public String getValidAttributeSet() {
		return getString(ATTRIBUTES, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

	/**
	 * @since 5.0
	 */
	public void removeTemplate(String name) {
		keySet().remove(TEMPLATE_PREFIX + name);
		String nameList = getString(TEMPLATE_NAMES, null);
		if (nameList != null) {
			String[] names = nameList.split(CM);
			for (int i = 0; i < names.length; i++)
				if (names[i].equals(name)) {
					names[i] = null;
					break;
				}
			StringBuffer sb = new StringBuffer();
			if (names.length > 0) {
				if (names[0] != null)
					sb.append(names[0]);
				for (int i = 1; i < names.length; i++)
					if (names[i] != null)
						sb.append(CM).append(names[i]);
			}
			putString(TEMPLATE_NAMES, sb.toString());
		}
	}

	/**
	 * @since 5.0
	 */
	public void removeValidAttributeSet() {
		keySet().remove(ATTRIBUTES);
	}

	/**
	 * @param name
	 *            of the current template for this resource manager (set in the
	 *            edit wizard).
	 * @since 5.0
	 */
	public void setCurrentTemplateName(String name) {
		putString(getResourceManagerId() + CURR_TEMPLATE, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc
	 * ()
	 */
	public void setDefaultNameAndDesc() {
		String name = PBS;
		String conn = getConnectionName();
		if (conn != null && !conn.equals(ZEROSTR))
			name += MARKER + conn;
		setName(name);
		setDescription(Messages.PBSResourceManager);
	}

	/**
	 * @since 5.0
	 */
	public void setProxyConfiguration(String type) {
		putString(PROXY_CONFIG_TYPE, type);
	}

	/**
	 * @since 5.0
	 */
	public void setValidAttributeSet(String serialized) {
		putString(ATTRIBUTES, serialized);
	}

	private void addTemplateName(String name) {
		String nameList = getString(TEMPLATE_NAMES, null);
		if (nameList == null) {
			putString(TEMPLATE_NAMES, name);
			return;
		}

		String[] names = nameList.split(CM);
		for (String nm : names)
			if (name.equals(nm))
				return;
		putString(TEMPLATE_NAMES, nameList + CM + name);
	}
}
