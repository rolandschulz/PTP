/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.core.elements;

import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;


public interface IPProcess extends IPElement {
	/**
	 * @deprecated
	 */
	public static final String STARTING = "starting";
	/**
	 * @deprecated
	 */
	public static final String RUNNING = "running";
	/**
	 * @deprecated
	 */
	public static final String EXITED = "exited";
	/**
	 * @deprecated
	 */
	public static final String EXITED_SIGNALLED = "exited-signalled";
	/**
	 * @deprecated
	 */
	public static final String STOPPED = "stopped";
	/**
	 * @deprecated
	 */
	public static final String ERROR = "error";

	public boolean isTerminated();
	public void setTerminated(boolean isTerminate);
	public int getPid();
	public int getTaskId();
	public ProcessAttributes.State getStatus();
	public int getExitCode();
	public String getSignalName();
	public void setStatus(ProcessAttributes.State status);
	public void removeProcess();
	public String getContents();
	public String[] getOutputs();
	public void clearOutput();
	public void addOutput(String output);
	/* returns the parent job that this process is encompassed by */
	public IPJob getJob();
	/* sets the node that this process is running on */
	public void setNode(IPNode node);
	/* returns the node that this process is running on */
	public IPNode getNode();
	
	/**
	 * @return
	 */
	public boolean isAllStop();
}
