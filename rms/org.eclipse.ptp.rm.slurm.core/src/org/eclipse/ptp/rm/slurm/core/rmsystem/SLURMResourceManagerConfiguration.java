/*******************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 			Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.rm.slurm.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class SLURMResourceManagerConfiguration extends
		AbstractRemoteResourceManagerConfiguration implements Cloneable {
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String TAG_SLURMD_PATH = "slurmdPath"; //$NON-NLS-1$
	private static final String TAG_SLURMD_ARGS = "slurmdArgs"; //$NON-NLS-1$
	private static final String TAG_SLURMD_DEFAULTS = "slurmdDefaults"; //$NON-NLS-1$

	/**
	 * @param factory
	 * @param memento
	 * @return
	 */
	public static IResourceManagerConfiguration load(
			SLURMResourceManagerFactory factory, IMemento memento) {

		RemoteConfig remoteConfig = loadRemote(factory, memento);

		String slurmdPath = memento.getString(TAG_SLURMD_PATH);
		String slurmdArgs = memento.getString(TAG_SLURMD_ARGS);
		boolean useDefaults = Boolean.parseBoolean(memento
				.getString(TAG_SLURMD_DEFAULTS));

		SLURMResourceManagerConfiguration config = new SLURMResourceManagerConfiguration(
				factory, remoteConfig, slurmdPath, slurmdArgs, useDefaults);

		return config;
	}

	private String slurmdPath;
	private String slurmdArgs;
	private boolean useDefaults;

	public SLURMResourceManagerConfiguration(SLURMResourceManagerFactory factory) {
		this(factory, new RemoteConfig(), EMPTY_STRING, EMPTY_STRING, true);
	}

	public SLURMResourceManagerConfiguration(SLURMResourceManagerFactory factory,
			RemoteConfig config, String slurmdPath, String slurmdArgs,
			boolean useDefaults) {
		super(config, factory);
		setSlurmdPath(slurmdPath);
		setSlurmdArgs(slurmdArgs);
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
				getDescription(), getUniqueName(),
				getRemoteServicesId(), getConnectionName());
		RemoteConfig remoteConf = new RemoteConfig(commonConf,
				getProxyServerPath(), getLocalAddress(),
				getInvocationOptionsStr(), getOptions());
		return new SLURMResourceManagerConfiguration(
				(SLURMResourceManagerFactory) getFactory(), remoteConf,
				getSlurmdPath(), getSlurmdArgs(), useDefaults());
	}

	/**
	 * @return the slurmdArgs
	 */
	public String getSlurmdArgs() {
		return slurmdArgs;
	}

	/**
	 * @return the slurmdPath
	 */
	public String getSlurmdPath() {
		return slurmdPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_SLURMD_PATH, slurmdPath);
		memento.putString(TAG_SLURMD_ARGS, slurmdArgs);
		memento.putString(TAG_SLURMD_DEFAULTS, Boolean.toString(useDefaults));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = "SLURM"; //$NON-NLS-1$
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("SLURM Resource Manager"); //$NON-NLS-1$
	}

	/**
	 * @param slurmdArguments
	 *            the slurmdArgs to set
	 */
	public void setSlurmdArgs(String slurmdArgs) {
		this.slurmdArgs = slurmdArgs;
	}

	/**
	 * @param slurmdPath
	 *            the slurmdPath to set
	 */
	public void setSlurmdPath(String slurmdPath) {
		this.slurmdPath = slurmdPath;
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