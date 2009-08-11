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
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.rm.ibm.ll.core.rmsystem;

import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceConstants;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceManager;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class IBMLLResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration implements IIBMLLResourceManagerConfiguration {

	public static IResourceManagerConfiguration load(IBMLLResourceManagerFactory factory,
			IMemento memento) {

		RemoteConfig remoteConfig = loadRemote(factory, memento);
		
		IIBMLLResourceManagerConfiguration config = 
			new IBMLLResourceManagerConfiguration(factory, remoteConfig);

		return config;
	}

	public IBMLLResourceManagerConfiguration(IBMLLResourceManagerFactory factory) {
		this(factory, new RemoteConfig());
		setDefaultNameAndDesc();
	}

	public IBMLLResourceManagerConfiguration(IBMLLResourceManagerFactory factory,
			RemoteConfig remoteConfig) {
		super(remoteConfig, factory);
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
		return new IBMLLResourceManagerConfiguration(
				(IBMLLResourceManagerFactory) getFactory(), remoteConf);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getArgsMessage()
	 */
	public String getArgsMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getDebugLoop()
	 */
	public String getDebugLoop() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_DEBUG_LOOP);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getDefaultMulticluster()
	 */
	public String getDefaultMulticluster() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getErrorMessage()
	 */
	public String getErrorMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getFatalMessage()
	 */
	public String getFatalMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getForceProxyLocal()
	 */
	public String getForceProxyLocal() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_FORCE_LOCAL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getForceProxyMulticluster()
	 */
	public String getForceProxyMulticluster() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getGuiArgsMessage()
	 */
	public String getGuiArgsMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.GUI_ARGS_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getGuiErrorMessage()
	 */
	public String getGuiErrorMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.GUI_ERROR_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getGuiFatalMessage()
	 */
	public String getGuiFatalMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.GUI_FATAL_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getGuiInfoMessage()
	 */
	public String getGuiInfoMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.GUI_INFO_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getGuiTraceMessage()
	 */
	public String getGuiTraceMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.GUI_TRACE_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getGuiWarningMessage()
	 */
	public String getGuiWarningMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.GUI_WARNING_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getInfoMessage()
	 */
	public String getInfoMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_INFO_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getJobPolling()
	 */
	public int getJobPolling() {
		return IBMLLPreferenceManager.getPreferences().getInt(IBMLLPreferenceConstants.PROXY_JOB_POLLING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getLibraryPath()
	 */
	public String getLibraryPath() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_LOADLEVELER_LIBRARY_PATH);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getMaxNodePolling()
	 */
	public int getMaxNodePolling() {
		return IBMLLPreferenceManager.getPreferences().getInt(IBMLLPreferenceConstants.PROXY_MAX_NODE_POLLING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getMinNodePolling()
	 */
	public int getMinNodePolling() {
		return IBMLLPreferenceManager.getPreferences().getInt(IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getSuppressTemplateWrite()
	 */
	public String getSuppressTemplateWrite() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getTemplateFile()
	 */
	public String getTemplateFile() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_LOADLEVELER_TEMPLATE_FILE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getTemplateWriteAlways()
	 */
	public String getTemplateWriteAlways() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getTraceOption()
	 */
	public String getTraceOption() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#getWarningMessage()
	 */
	public String getWarningMessage() {
		return IBMLLPreferenceManager.getPreferences().getString(IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setArgsMessage(java.lang.String)
	 */
	public void setArgsMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setDebugLoop(java.lang.String)
	 */
	public void setDebugLoop(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setDefaultMulticluster(java.lang.String)
	 */
	public void setDefaultMulticluster(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = "IBMLL"; //$NON-NLS-1$
		String conn = getConnectionName();
		if (conn != null && !conn.equals("")) { //$NON-NLS-1$
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("IBMLL Resource Manager"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setFatalMessage(java.lang.String)
	 */
	public void setFatalMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setForceProxyLocal(java.lang.String)
	 */
	public void setForceProxyLocal(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setForceProxyMulticluster(java.lang.String)
	 */
	public void setForceProxyMulticluster(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setGuiArgsMessage(java.lang.String)
	 */
	public void setGuiArgsMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setGuiErrorMessage(java.lang.String)
	 */
	public void setGuiErrorMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setGuiFatalMessage(java.lang.String)
	 */
	public void setGuiFatalMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setGuiInfoMessage(java.lang.String)
	 */
	public void setGuiInfoMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setGuiTraceMessage(java.lang.String)
	 */
	public void setGuiTraceMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setGuiWarningMessage(java.lang.String)
	 */
	public void setGuiWarningMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setInfoMessage(java.lang.String)
	 */
	public void setInfoMessage(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setJobPolling(int)
	 */
	public void setJobPolling(int value) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setLibraryPath(java.lang.String)
	 */
	public void setLibraryPath(String path) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setMaxNodePolling(int)
	 */
	public void setMaxNodePolling(int value) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setMinNodePolling(int)
	 */
	public void setMinNodePolling(int value) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setSuppressTemplateWrite(java.lang.String)
	 */
	public void setSuppressTemplateWrite(String option) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setTemplateFile(java.lang.String)
	 */
	public void setTemplateFile(String file) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setTemplateWriteAlways(java.lang.String)
	 */
	public void setTemplateWriteAlways(String option) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setTraceOption(java.lang.String)
	 */
	public void setTraceOption(String option) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration#setWarningMessage(java.lang.String)
	 */
	public void setWarningMessage(String option) {
		// TODO Auto-generated method stub
		
	}
}