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
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;

public class NullJob extends PlatformObject implements IPJob {

	private static final IPProcess NULL_PROCESS = new NullProcess();
	private static final String[] NO_STRINGS = new String[0];
	private static final String EMPTY_STRING = "";
	private static final IPProcess[] NO_PROCESSES = new IPProcess[0];
	private static final IPMachine[] NO_MACHINES = new IPMachine[0];
	private static final IPNode[] NO_NODES = new IPNode[0];
	private static final IPUniverse NULL_UNIVERSE = new NullUniverse();

	public IPProcess findProcess(String processNumber) {
		// no-op
		return NULL_PROCESS;
	}

	public IPProcess findProcessByName(String pname) {
		// no-op
		return NULL_PROCESS;
	}

	public IPProcess findProcessByTaskId(int taskId) {
		// no-op
		return NULL_PROCESS;
	}

	public Object getAttribute(String key) {
		// no-op
		return EMPTY_STRING;
	}

	public String[] getAttributeKeys() {
		// no-op
		return NO_STRINGS;
	}

	public String getIDString() {
		// no-op
		return EMPTY_STRING;
	}

	public String getJobNumber() {
		// no-op
		return EMPTY_STRING;
	}

	public int getJobNumberInt() {
		// no-op
		return 0;
	}

	public IPMachine[] getMachines() {
		// no-op
		return NO_MACHINES;
	}

	public String getName() {
		// no-op
		return EMPTY_STRING;
	}

	public IPNode[] getNodes() {
		// no-op
		return NO_NODES;
	}

	public IPProcess[] getProcesses() {
		// no-op
		return NO_PROCESSES;
	}

	public IPNode[] getSortedNodes() {
		// no-op
		return NO_NODES;
	}

	public IPProcess[] getSortedProcesses() {
		// no-op
		return NO_PROCESSES;
	}

	public IPUniverse getUniverse() {
		return NULL_UNIVERSE;
	}

	public boolean isAllStop() {
		// no-op
		return false;
	}

	public boolean isDebug() {
		// no-op
		return false;
	}

	public void removeAllProcesses() {
		// no-op
	}

	public void setAttribute(String key, Object o) {
		// no-op
	}

	public void setDebug() {
		// no-op
	}

	public int totalNodes() {
		// no-op
		return 0;
	}

	public int totalProcesses() {
		// no-op
		return 0;
	}

	public String getElementName() {
		// no-op
		return EMPTY_STRING;
	}

	public int getID() {
		// no-op
		return 0;
	}

}
