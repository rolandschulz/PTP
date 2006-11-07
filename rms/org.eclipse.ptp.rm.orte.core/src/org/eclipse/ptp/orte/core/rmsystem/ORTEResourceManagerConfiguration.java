/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.orte.core.rmsystem;

import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class ORTEResourceManagerConfiguration implements IResourceManagerConfiguration {
	
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_FACTORY_ID = "factoryId"; //$NON-NLS-1$
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_LAUNCH_MANUALLY = "launchManually"; //$NON-NLS-1$

	public static IResourceManagerConfiguration load(ORTEResourceManagerFactory factory,
			IMemento memento) {
		String factoryId = memento.getString(TAG_FACTORY_ID);
		if (!factoryId.equals(factory.getId())) {
			throw new IllegalStateException("Incompatable factory with factoryId"
					+ " stored id" + factoryId 
					+ ", factory id:" + factory.getId());
		}
		String name = memento.getString(TAG_NAME);
		String desc = memento.getString(TAG_DESCRIPTION);
		String orteServerFile = memento.getString(TAG_PROXY_PATH);
		boolean launchManually = Boolean.parseBoolean(memento.getString(TAG_LAUNCH_MANUALLY));
		
		ORTEResourceManagerConfiguration config = new ORTEResourceManagerConfiguration(factory,
				name, desc, orteServerFile, launchManually);
		
		return config;
	}
	private String description;
	private String name;
	private final String factoryId;
	private String orteServerFile;

	private boolean launchManually;
	
	public ORTEResourceManagerConfiguration(ORTEResourceManagerFactory factory,
			String orteServerFile, boolean launchManually) {
		this(factory, "", "", orteServerFile, launchManually);
		setDefaultNameAndDesc();
	}
	
	public ORTEResourceManagerConfiguration(ORTEResourceManagerFactory factory, String name, String desc,
			String orteServerFile, boolean launchManually) {
		this.factoryId = factory.getId();
		this.name = name;
		this.description = desc;
		this.orteServerFile = orteServerFile;
		this.launchManually = launchManually;
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}
	
	public String getOrteServerFile() {
		return orteServerFile;
	}

	public String getResourceManagerId() {
		return this.factoryId;
	}

	public boolean isLaunchManually() {
		return launchManually;
	}

	public void save(IMemento memento) {
		memento.putString(TAG_FACTORY_ID, getResourceManagerId());
		memento.putString(TAG_NAME, name);
		memento.putString(TAG_DESCRIPTION, description);
		memento.putString(TAG_PROXY_PATH, orteServerFile);
		memento.putString(TAG_LAUNCH_MANUALLY, Boolean.toString(launchManually));
	}

	public void setDefaultNameAndDesc() {
		this.name = "ORTE";
		this.description = "ORTE Resource Manager";
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setManualLaunch(boolean launchManually) {
		this.launchManually = launchManually;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrteServerFile(String orteServerFile) {
		this.orteServerFile = orteServerFile;
	}
}