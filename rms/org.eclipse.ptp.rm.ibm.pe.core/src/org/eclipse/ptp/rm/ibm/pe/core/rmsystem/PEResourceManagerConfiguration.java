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

final public class PEResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration
{

    	// Values of tags used as memento keys should be prefixed with 'PE_' to ensure no
    	// collisions with other proxy's settings
    private static final String TAG_USE_LOADLEVELER = "PE_UseLoadLeveler";
    private static final String TAG_DEBUG_LEVEL = "PE_DebugLevel";
    private static final String TAG_RUN_MINIPROXY = "PE_RunMiniproxy";
    private static final String TAG_SUSPEND_PROXY = "PE_SuspendProxy";
    private static final String TAG_LOADLEVELER_MODE = "PE_LoadLevelerMode";
    private static final String TAG_MIN_NODE_POLL_INTERVAL = "PE_NodeMinPollInterval";
    private static final String TAG_MAX_NODE_POLL_INTERVAL = "PE_NodeMaxPollInterval";
    private static final String TAG_JOB_POLL_INTERVAL = "PE_JobPollInterval";
    private static final String TAG_LIBRARY_OVERRIDE = "PE_LibraryOverride";
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
	PEResourceManagerConfiguration config = new PEResourceManagerConfiguration(factory, remoteConfig,
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
	this(factory, new RemoteConfig(), "N", "Y", "None", "N", "d", "", "", "", "");
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
				getDescription(), getUniqueName());
		RemoteConfig remoteConf = new RemoteConfig(commonConf,
				getRemoteServicesId(), getConnectionName(),
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
	String name = "PE";
	String conn = getConnectionName();
	if (conn != null && !conn.equals("")) {
	    name += "@" + conn;
	}
	setName(name);
	setDescription("PE Resource Manager");
    }

    /**
     * Get flag indicating LoadLeveler is used to allocate nodes
     * 
     * @return the LoadLeveler flag
     */
    public String getUseLoadLeveler()
    {
	return useLoadLeveler;
    }

    /**
     * Get flag indicating the miniproxy is to run following main proxy shutdown
     * 
     * @return Miniproxy flag
     */
    public String getRunMiniproxy()
    {
	return runMiniproxy;
    }

    /**
     * Get the proxy debug level
     * 
     * @return debug level
     */
    public String getDebugLevel()
    {
	return debugLevel;
    }

    /**
     * Get the flag indicating proxy should be suspended at startup (for debugging)
     * 
     * @return the suspend proxy flag
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

    /**
     * Set flag indicating whether LoadLeveler is used to allocate nodes
     * @param useLoadLeveler - flag inidcating LoadLeveler used to allocate nodes
     */
    public void setUseLoadLeveler(String useLoadLeveler)
    {
        this.useLoadLeveler = useLoadLeveler;
    }

    /**
     * Set flag indicating whether miniproxy should run following proxy shutdown
     * @param runMiniproxy Flag indicating miniproxy should run
     */
    public void setRunMiniproxy(String runMiniproxy)
    {
        this.runMiniproxy = runMiniproxy;
    }

    /**
     * Set the debug level for the proxy
     * @param debugLevel Debug level
     */
    public void setDebugLevel(String debugLevel)
    {
        this.debugLevel = debugLevel;
    }

    /**
     * Set flag indicating whether proxy should be suspended at startup (for debugging)
     * @param suspendProxy proxy suspension flag
     */
    public void setSuspendProxy(String suspendProxy)
    {
        this.suspendProxy = suspendProxy;
    }

    /**
     * Get LoadLeveler run mode (local, multicluster, default)
     * @return the loadLevelerMode
     */
    public String getLoadLevelerMode()
    {
        return loadLevelerMode;
    }

    /**
     * Set LoadLeveler run mode (local, multicluster, default)
     * @param loadLevelerMode the loadLevelerMode to set
     */
    public void setLoadLevelerMode(String loadLevelerMode)
    {
        this.loadLevelerMode = loadLevelerMode;
    }

    /**
     * Get the minimum interval to poll LoadLeveler for node status
     * @return the nodePollMinInterval
     */
    public String getNodeMinPollInterval()
    {
        return nodeMinPollInterval;
    }

    /**
     * Set the minimum interval to poll LoadLeveler for node status
     * @param nodePollMinInterval the nodePollMinInterval to set
     */
    public void setNodeMinPollInterval(String nodeMinPollInterval)
    {
        this.nodeMinPollInterval = nodeMinPollInterval;
    }

    /**
     * Get the maximum interval to poll LoadLeveler for node status
     * @return the nodePollMaxInterval
     */
    public String getNodeMaxPollInterval()
    {
        return nodeMaxPollInterval;
    }

    /**
     * Set the maximum interval to poll LoadLeveler for node status
     * @param nodePollMaxInterval the nodePollMaxInterval to set
     */
    public void setNodeMaxPollInterval(String nodeMaxPollInterval)
    {
        this.nodeMaxPollInterval = nodeMaxPollInterval;
    }

    /**
     * Get the interval to poll LoadLeveler for job status
     * @return the jobPollInterval
     */
    public String getJobPollInterval()
    {
        return jobPollInterval;
    }

    /**
     * Set the interval to poll LoadLeveler for job status
     * @param jobPollInterval the jobPollInterval to set
     */
    public void setJobPollInterval(String jobPollInterval)
    {
        this.jobPollInterval = jobPollInterval;
    }

    /**
     * Get the alternate library path for the LoadLeveler API library
     * @return the libraryOverride
     */
    public String getLibraryOverride()
    {
        return libraryOverride;
    }

    /**
     * Set the alternate library path for the LoadLeveler API library
     * @param libraryOverride the libraryOverride to set
     */
    public void setLibraryOverride(String libraryOverride)
    {
        this.libraryOverride = libraryOverride;
    }
}