/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.ibm.ll.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration;

public interface IIBMLLResourceManagerConfiguration extends IRemoteResourceManagerConfiguration {

	public String getArgsMessage();

	public String getDebugLoop();

	public String getDefaultMulticluster();

	public String getErrorMessage();

	public String getFatalMessage();

	public String getForceProxyLocal();

	public String getForceProxyMulticluster();

	public String getGuiArgsMessage();

	public String getGuiErrorMessage();

	public String getGuiFatalMessage();

	public String getGuiInfoMessage();

	public String getGuiTraceMessage();

	public String getGuiWarningMessage();

	public String getInfoMessage();

	public int getJobPolling();

	public String getLibraryPath();

	public int getMaxNodePolling();

	public int getMinNodePolling();

	public String getSuppressTemplateWrite();

	public String getTemplateFile();

	public String getTemplateWriteAlways();

	public String getTraceOption();

	public String getWarningMessage();

	public void setArgsMessage(String option);

	public void setDebugLoop(String option);

	public void setDefaultMulticluster(String option);

	public void setErrorMessage(String option);

	public void setFatalMessage(String option);

	public void setForceProxyLocal(String option);

	public void setForceProxyMulticluster(String option);

	public void setGuiArgsMessage(String option);

	public void setGuiErrorMessage(String option);

	public void setGuiFatalMessage(String option);

	public void setGuiInfoMessage(String option);

	public void setGuiTraceMessage(String option);

	public void setGuiWarningMessage(String option);

	public void setInfoMessage(String option);

	public void setJobPolling(int value);

	public void setLibraryPath(String path);

	public void setMaxNodePolling(int value);

	public void setMinNodePolling(int value);

	public void setSuppressTemplateWrite(String option);

	public void setTemplateFile(String file);

	public void setTemplateWriteAlways(String option);

	public void setTraceOption(String option);

	public void setWarningMessage(String option);

}