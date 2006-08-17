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
package org.eclipse.ptp.internal.rmsystem;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IProcessListener;

public class NullProcess extends PlatformObject implements IPProcess {

	private static final String EMPTY_STRING = "";
	private static final String[] NO_STRINGS = new String[0];
	private static final IPJob NULL_JOB = new NullJob();
	private static final IPNode NULL_NODE = new NullNode();
	private static final IPProcess NULL_PROCESS = new NullProcess();

	public void addOutput(String output) {
		// no-op
	}

	public void addProcessListener(IProcessListener listener) {
		// no-op
	}

	public void clearOutput() {
		// no-op
	}

	public Object getAttribute(String key) {
		// no-op
		return EMPTY_STRING;
	}

	public String[] getAttributeKeys() {
		// no-op
		return NO_STRINGS;
	}

	public String getContents() {
		// no-op
		return EMPTY_STRING;
	}

	public String getExitCode() {
		// no-op
		return EMPTY_STRING;
	}

	public IPJob getJob() {
		// no-op
		return NULL_JOB;
	}

	public String getName() {
		// no-op
		return EMPTY_STRING;
	}

	public IPNode getNode() {
		// no-op
		return NULL_NODE;
	}

	public int getNumChildProcesses() {
		// no-op
		return 0;
	}

	public String[] getOutputs() {
		// no-op
		return NO_STRINGS;
	}

	public IPProcess getParentProcess() {
		return NULL_PROCESS;
	}

	public String getPid() {
		// no-op
		return EMPTY_STRING;
	}

	public String getProcessNumber() {
		// no-op
		return EMPTY_STRING;
	}

	public String getSignalName() {
		// no-op
		return EMPTY_STRING;
	}

	public String getStatus() {
		// no-op
		return EMPTY_STRING;
	}

	public int getTaskId() {
		// no-op
		return 0;
	}

	public boolean isAllStop() {
		// no-op
		return false;
	}

	public boolean isTerminated() {
		// no-op
		return false;
	}

	public void removeProcess() {
		// no-op
	}

	public void removerProcessListener(IProcessListener listener) {
		// no-op
	}

	public void setAttribute(String key, Object o) {
		// no-op
	}

	public void setExitCode(String code) {
		// no-op
	}

	public void setNode(IPNode node) {
		// no-op
	}

	public void setPid(String pid) {
		// no-op
	}

	public void setSignalName(String signalName) {
		// no-op
	}

	public void setStatus(String status) {
		// no-op
	}

	public void setTerminated(boolean isTerminate) {
		// no-op
	}

	public String getElementName() {
		// no-op
		return EMPTY_STRING;
	}

	public int getID() {
		// no-op
		return 0;
	}

	public String getIDString() {
		// no-op
		return EMPTY_STRING;
	}

}
