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
package org.eclipse.ptp.rm.ompi.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class OMPIResourceManagerConfiguration extends
		AbstractRemoteResourceManagerConfiguration implements Cloneable {
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String TAG_ORTED_PATH = "ortedPath"; //$NON-NLS-1$
	private static final String TAG_ORTED_ARGS = "ortedArgs"; //$NON-NLS-1$
	private static final String TAG_ORTED_DEFAULTS = "ortedDefaults"; //$NON-NLS-1$

	/**
	 * @param factory
	 * @param memento
	 * @return
	 */
	public static IResourceManagerConfiguration load(
			OMPIResourceManagerFactory factory, IMemento memento) {

		RemoteConfig remoteConfig = loadRemote(factory, memento);

		String ortedPath = memento.getString(TAG_ORTED_PATH);
		String ortedArgs = memento.getString(TAG_ORTED_ARGS);
		boolean useDefaults = Boolean.parseBoolean(memento
				.getString(TAG_ORTED_DEFAULTS));

		OMPIResourceManagerConfiguration config = new OMPIResourceManagerConfiguration(
				factory, remoteConfig, ortedPath, ortedArgs, useDefaults);

		return config;
	}

	private String ortedPath;
	private String ortedArgs;
	private boolean useDefaults;

	public OMPIResourceManagerConfiguration(OMPIResourceManagerFactory factory) {
		this(factory, new RemoteConfig(), EMPTY_STRING, EMPTY_STRING, true);
	}

	public OMPIResourceManagerConfiguration(OMPIResourceManagerFactory factory,
			RemoteConfig config, String ortedPath, String ortedArgs,
			boolean useDefaults) {
		super(config, factory);
		setOrtedPath(ortedPath);
		setOrtedArgs(ortedArgs);
		setUseDefaults(useDefaults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		CommonConfig commonConf = new CommonConfig(getName(),
				getDescription(), getUniqueName());
		RemoteConfig remoteConf = new RemoteConfig(commonConf,
				getRemoteServicesId(), getConnectionName(),
				getProxyServerPath(), getLocalAddress(),
				getInvocationOptionsStr(), getOptions());
		return new OMPIResourceManagerConfiguration(
				(OMPIResourceManagerFactory) getFactory(), remoteConf,
				getOrtedPath(), getOrtedArgs(), useDefaults());
	}

	/**
	 * @return the ortedArgs
	 */
	public String getOrtedArgs() {
		return ortedArgs;
	}

	/**
	 * @return the ortedPath
	 */
	public String getOrtedPath() {
		return ortedPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_ORTED_PATH, ortedPath);
		memento.putString(TAG_ORTED_ARGS, ortedArgs);
		memento.putString(TAG_ORTED_DEFAULTS, Boolean.toString(useDefaults));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = "OMPI"; //$NON-NLS-1$
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("OMPI Resource Manager"); //$NON-NLS-1$
	}

	/**
	 * @param ortedArguments
	 *            the ortedArgs to set
	 */
	public void setOrtedArgs(String ortedArgs) {
		this.ortedArgs = ortedArgs;
	}

	/**
	 * @param ortedPath
	 *            the ortedPath to set
	 */
	public void setOrtedPath(String ortedPath) {
		this.ortedPath = ortedPath;
	}

	/**
	 * @param useDefaults
	 *            the useDefaults to set
	 */
	public void setUseDefaults(boolean useDefaults) {
		this.useDefaults = useDefaults;
	}

	/**
	 * @return the useDefaults
	 */
	public boolean useDefaults() {
		return useDefaults;
	}

}