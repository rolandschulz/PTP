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
package org.eclipse.ptp.rm.pbs.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Service provider for PBS batch scheduler.
 */
public class PBSResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration implements IPBSNonNLSConstants {

	public PBSResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		super(namespace, provider);
		setDescription(Messages.PBSResourceManagerConfiguration_PBSResourceManager);
	}

	/**
	 * @since 5.0
	 */
	public void addTemplate(String name, String serialized) {
		addTemplateName(name);
		putString(TEMPLATE_PREFIX + name, serialized);
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
		if (nameList == null) {
			return new String[0];
		}
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
		putString(TEMPLATE_PREFIX + name, null);
		String nameList = getString(TEMPLATE_NAMES, null);
		if (nameList != null) {
			String[] names = nameList.split(CM);
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(name)) {
					names[i] = null;
					break;
				}
			}
			StringBuffer sb = new StringBuffer();
			if (names.length > 0) {
				if (names[0] != null) {
					sb.append(names[0]);
				}
				for (int i = 1; i < names.length; i++) {
					if (names[i] != null) {
						sb.append(CM).append(names[i]);
					}
				}
			}
			putString(TEMPLATE_NAMES, sb.toString());
		}
	}

	/**
	 * @since 5.0
	 */
	public void removeValidAttributeSet() {
		putString(ATTRIBUTES, null);
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
		if (conn != null && !conn.equals(ZEROSTR)) {
			name += MARKER + conn;
		}
		setName(name);
		setDescription(Messages.PBSResourceManagerConfiguration_PBSResourceManager);
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
		for (String nm : names) {
			if (name.equals(nm)) {
				return;
			}
		}
		putString(TEMPLATE_NAMES, nameList + CM + name);
	}
}
