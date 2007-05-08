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

import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class ORTEResourceManagerConfiguration extends AbstractResourceManagerConfiguration {
	
	private static final String TAG_PROXY_PATH = "proxyPath"; //$NON-NLS-1$
	private static final String TAG_LAUNCH_MANUALLY = "launchManually"; //$NON-NLS-1$

	public static IResourceManagerConfiguration load(ORTEResourceManagerFactory factory,
			IMemento memento) {

		CommonConfig commonConfig = loadCommon(factory, memento);

		String orteServerFile = memento.getString(TAG_PROXY_PATH);
		boolean launchManually = Boolean.parseBoolean(memento.getString(TAG_LAUNCH_MANUALLY));

		ORTEResourceManagerConfiguration config = 
			new ORTEResourceManagerConfiguration(factory, commonConfig, orteServerFile,
					launchManually);

		return config;
	}
	private String orteServerFile;

	private boolean launchManually;
	
	public ORTEResourceManagerConfiguration(ORTEResourceManagerFactory factory,
			CommonConfig commonConfig,
			String orteServerFile, boolean launchManually) {
		super(commonConfig, factory);
		this.orteServerFile = orteServerFile;
		this.launchManually = launchManually;
	}
	
	public ORTEResourceManagerConfiguration(ORTEResourceManagerFactory factory,
			String orteServerFile, boolean launchManually) {
		this(factory, new CommonConfig(), orteServerFile, launchManually);
		setDefaultNameAndDesc();
	}

	public String getOrteServerFile() {
		return orteServerFile;
	}

	public boolean isLaunchManually() {
		return launchManually;
	}

	public void setDefaultNameAndDesc() {
		setName("ORTE");
		setDescription("ORTE Resource Manager");
	}

	public void setManualLaunch(boolean launchManually) {
		this.launchManually = launchManually;
	}

	public void setOrteServerFile(String orteServerFile) {
		this.orteServerFile = orteServerFile;
	}

	protected void doSave(IMemento memento) {
		memento.putString(TAG_PROXY_PATH, orteServerFile);
		memento.putString(TAG_LAUNCH_MANUALLY, Boolean.toString(launchManually));
	}
}