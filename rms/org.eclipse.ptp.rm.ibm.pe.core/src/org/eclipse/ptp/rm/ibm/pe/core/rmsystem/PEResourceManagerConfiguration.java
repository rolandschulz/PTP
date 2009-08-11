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
 * 
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.rm.ibm.pe.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class PEResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration implements IPEResourceManagerConfiguration
{

    	// Values of tags used as memento keys should be prefixed with 'PE_' to ensure no
    	// collisions with other proxy's settings
    private static final String TAG_USE_LOADLEVELER = "PE_UseLoadLeveler"; //$NON-NLS-1$
    private static final String TAG_DEBUG_LEVEL = "PE_DebugLevel"; //$NON-NLS-1$
    private static final String TAG_RUN_MINIPROXY = "PE_RunMiniproxy"; //$NON-NLS-1$
    private static final String TAG_SUSPEND_PROXY = "PE_SuspendProxy"; //$NON-NLS-1$
    private static final String TAG_LOADLEVELER_MODE = "PE_LoadLevelerMode"; //$NON-NLS-1$
    private static final String TAG_MIN_NODE_POLL_INTERVAL = "PE_NodeMinPollInterval"; //$NON-NLS-1$
    private static final String TAG_MAX_NODE_POLL_INTERVAL = "PE_NodeMaxPollInterval"; //$NON-NLS-1$
    private static final String TAG_JOB_POLL_INTERVAL = "PE_JobPollInterval"; //$NON-NLS-1$
    private static final String TAG_LIBRARY_OVERRIDE = "PE_LibraryOverride"; //$NON-NLS-1$
    private String useLoadLeveler;
    private String runMiniproxy;
    private String debugLevel;
    private String suspendProxy;
    private String loadLevelerMode;
    private String nodeMinPollInterval;
    private String nodeMaxPollInterval;
    private String jobPollInterval;
    private String libraryOverride;

    public static IResourceManagerConfiguration load(PEResourceManagerFactory factory, IMemento memento)
    {

	RemoteConfig remoteConfig;
	String useLoadLeveler;
	String runMiniproxy;
	String debugLevel;
	String suspendProxy;
	String loadLevelerMode;
	String nodeMinPollInterval;
	String nodeMaxPollInterval;
	String jobPollInterval;
	String libraryOverride;

	remoteConfig = loadRemote(factory, memento);
	useLoadLeveler = memento.getString(TAG_USE_LOADLEVELER);
	runMiniproxy = memento.getString(TAG_RUN_MINIPROXY);
	debugLevel = memento.getString(TAG_DEBUG_LEVEL);
	suspendProxy = memento.getString(TAG_SUSPEND_PROXY);
	nodeMinPollInterval = memento.getString(TAG_MIN_NODE_POLL_INTERVAL);
	nodeMaxPollInterval = memento.getString(TAG_MAX_NODE_POLL_INTERVAL);
	jobPollInterval = memento.getString(TAG_JOB_POLL_INTERVAL);
	libraryOverride = memento.getString(TAG_LIBRARY_OVERRIDE);
	loadLevelerMode = memento.getString(TAG_LOADLEVELER_MODE);
	IPEResourceManagerConfiguration config = new PEResourceManagerConfiguration(factory, remoteConfig,
		useLoadLeveler, runMiniproxy, debugLevel, suspendProxy, loadLevelerMode, libraryOverride, 
		nodeMinPollInterval, nodeMaxPollInterval, jobPollInterval);

	return config;
    }

    public PEResourceManagerConfiguration(PEResourceManagerFactory factory, RemoteConfig remoteConfig,
	    String useLoadLeveler, String runMiniproxy, String debugLevel, String suspendProxy,
	    String loadLevelerMode, String libraryOverride, String nodeMinPollInterval, 
	    String nodeMaxPollInterval, String jobPollInterval)
    {
	super(remoteConfig, factory);
	this.useLoadLeveler = useLoadLeveler;
	this.runMiniproxy = runMiniproxy;
	this.debugLevel = debugLevel;
	this.suspendProxy = suspendProxy;
	this.loadLevelerMode = loadLevelerMode;
	this.libraryOverride = libraryOverride;
	this.nodeMinPollInterval = nodeMinPollInterval;
	this.nodeMaxPollInterval = nodeMaxPollInterval;
	this.jobPollInterval = jobPollInterval;
    }

    public PEResourceManagerConfiguration(PEResourceManagerFactory factory)
    {
	this(factory, new RemoteConfig(), "N", "Y", "None", "N", "d", "", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	setDefaultNameAndDesc();
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
		return new PEResourceManagerConfiguration(
				(PEResourceManagerFactory) getFactory(), remoteConf,
				getUseLoadLeveler(), getRunMiniproxy(), getDebugLevel(), 
				getSuspendProxy(), getLoadLevelerMode(), getLibraryOverride(),
				getNodeMinPollInterval(), getNodeMaxPollInterval(),
				getJobPollInterval());
	}
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
     */
    public void setDefaultNameAndDesc()
    {
	String name = "PE"; //$NON-NLS-1$
	String conn = getConnectionName();
	if (conn != null && !conn.equals("")) { //$NON-NLS-1$
	    name += "@" + conn; //$NON-NLS-1$
	}
	setName(name);
	setDescription("PE Resource Manager"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getUseLoadLeveler()
	 */
    public String getUseLoadLeveler()
    {
	return useLoadLeveler;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getRunMiniproxy()
	 */
    public String getRunMiniproxy()
    {
	return runMiniproxy;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getDebugLevel()
	 */
    public String getDebugLevel()
    {
	return debugLevel;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getSuspendProxy()
	 */
    public String getSuspendProxy()
    {
	return suspendProxy;
    }

//    /*
//     * Save option settings for resource manager definition (non-Javadoc)
//     * 
//     * @see org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration#doSave(org.eclipse.ui.IMemento)
//     */
//    protected void doSave(IMemento memento)
//    {
//	super.doSave(memento);
//	memento.putString(TAG_USE_LOADLEVELER, useLoadLeveler);
//	memento.putString(TAG_RUN_MINIPROXY, runMiniproxy);
//	memento.putString(TAG_DEBUG_LEVEL, debugLevel);
//	memento.putString(TAG_MIN_NODE_POLL_INTERVAL, nodeMinPollInterval);
//	memento.putString(TAG_MAX_NODE_POLL_INTERVAL, nodeMaxPollInterval);
//	memento.putString(TAG_JOB_POLL_INTERVAL, jobPollInterval);
//	memento.putString(TAG_LOADLEVELER_MODE, loadLevelerMode);
//	memento.putString(TAG_LIBRARY_OVERRIDE, libraryOverride);
//	memento.putString(TAG_SUSPEND_PROXY, suspendProxy);
//    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setUseLoadLeveler(java.lang.String)
	 */
    public void setUseLoadLeveler(String useLoadLeveler)
    {
        this.useLoadLeveler = useLoadLeveler;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setRunMiniproxy(java.lang.String)
	 */
    public void setRunMiniproxy(String runMiniproxy)
    {
        this.runMiniproxy = runMiniproxy;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setDebugLevel(java.lang.String)
	 */
    public void setDebugLevel(String debugLevel)
    {
        this.debugLevel = debugLevel;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setSuspendProxy(java.lang.String)
	 */
    public void setSuspendProxy(String suspendProxy)
    {
        this.suspendProxy = suspendProxy;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getLoadLevelerMode()
	 */
    public String getLoadLevelerMode()
    {
        return loadLevelerMode;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setLoadLevelerMode(java.lang.String)
	 */
    public void setLoadLevelerMode(String loadLevelerMode)
    {
        this.loadLevelerMode = loadLevelerMode;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getNodeMinPollInterval()
	 */
    public String getNodeMinPollInterval()
    {
        return nodeMinPollInterval;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setNodeMinPollInterval(java.lang.String)
	 */
    public void setNodeMinPollInterval(String nodeMinPollInterval)
    {
        this.nodeMinPollInterval = nodeMinPollInterval;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getNodeMaxPollInterval()
	 */
    public String getNodeMaxPollInterval()
    {
        return nodeMaxPollInterval;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setNodeMaxPollInterval(java.lang.String)
	 */
    public void setNodeMaxPollInterval(String nodeMaxPollInterval)
    {
        this.nodeMaxPollInterval = nodeMaxPollInterval;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getJobPollInterval()
	 */
    public String getJobPollInterval()
    {
        return jobPollInterval;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setJobPollInterval(java.lang.String)
	 */
    public void setJobPollInterval(String jobPollInterval)
    {
        this.jobPollInterval = jobPollInterval;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#getLibraryOverride()
	 */
    public String getLibraryOverride()
    {
        return libraryOverride;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration#setLibraryOverride(java.lang.String)
	 */
    public void setLibraryOverride(String libraryOverride)
    {
        this.libraryOverride = libraryOverride;
    }
}