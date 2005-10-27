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
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import java.util.Hashtable;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.ptp.debug.core.cdi.IPCDIModelManager;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcess;
import org.eclipse.ptp.debug.external.cdi.model.Target;

/**
 * @deprecated
 */
public class ModelManager extends Manager implements IPCDIModelManager {
	// private Hashtable currentProcessSetList;
	private Hashtable processListCache;

	public ModelManager(Session session) {
		super(session, false);
		// currentProcessSetList = new Hashtable();
		processListCache = new Hashtable();
	}
	protected void update(Target target) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}
	public IPCDIDebugProcess getProcess(int proc) {
		IPCDIDebugProcess p = (IPCDIDebugProcess) processListCache.get(new Integer(proc));
		if (p == null) {
			p = new DebugProcess((IPCDISession) getSession(), ((Session) getSession()).getDebugger().getProcess(proc));
			processListCache.put(new Integer(proc), p);
		}
		return p;
	}
	/*
	 * public IPCDIDebugProcessSetNamed newProcessSet(String name, int[] procs) { if (currentProcessSetList.containsKey(name)) return (IPCDIDebugProcessSetNamed) currentProcessSetList.get(name); IPCDIDebugProcessSetNamed newSet = new DebugProcessSetNamed((IPCDISession) getSession(), name); newSet.addProcess(procs); currentProcessSetList.put(newSet.getName(), newSet); return newSet; } public
	 * IPCDIDebugProcessSetNamed newProcessSet(String name, BitList list) { if (currentProcessSetList.containsKey(name)) return (IPCDIDebugProcessSetNamed) currentProcessSetList.get(name); IPCDIDebugProcessSetNamed newSet = new DebugProcessSetNamed((IPCDISession) getSession(), name, list); currentProcessSetList.put(newSet.getName(), newSet); return newSet; }
	 * 
	 * public void delProcessSet(String name) { currentProcessSetList.remove(name); }
	 * 
	 * public IPCDIDebugProcessSetNamed[] getProcessSets() { int size = currentProcessSetList.size(); IPCDIDebugProcessSetNamed[] pSets = new IPCDIDebugProcessSetNamed[size]; int index = 0;
	 * 
	 * Iterator it = currentProcessSetList.keySet().iterator(); while (it.hasNext()) { String procSetName = (String) it.next(); IPCDIDebugProcessSetNamed procSet = (IPCDIDebugProcessSetNamed) currentProcessSetList.get(procSetName); pSets[index++] = procSet; } return pSets; } public IPCDIDebugProcessSetNamed getProcessSet(String name) { return (IPCDIDebugProcessSetNamed)
	 * currentProcessSetList.get(name); }
	 * 
	 * public BitList getTasks(String setName) { return ((IPCDIDebugProcessSetNamed) currentProcessSetList.get(setName)).toBitList(); }
	 */
}
