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
package org.eclipse.ptp.debug.external.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;

/**
 * @author Clement chu
 * 
 */
public class ProcessManager extends Manager {
	static final Target[] EMPTY_TARGETS = new Target[0];
	Map debugTargetMap = null;

	public ProcessManager(Session session) {
		super(session, true);
		debugTargetMap = Collections.synchronizedMap(new HashMap());
	}
	public void shutdown() {
		debugTargetMap.clear();
	}
	public Target[] getTargets() {
		return (Target[]) debugTargetMap.values().toArray(new Target[0]);
	}
	public IPCDITarget[] getCDITargets() {
		return (IPCDITarget[]) debugTargetMap.values().toArray(new IPCDITarget[0]);
	}
	public BitList getRegisteredTargets() {
		BitList regTasks = ((Session)getSession()).createEmptyBitList();
		synchronized (debugTargetMap) {
			for (Iterator i=debugTargetMap.keySet().iterator(); i.hasNext();) {
				regTasks.set(((Integer)i.next()).intValue());
			}
		}
		return regTasks;
	}
	public int[] getRegisteredTargetIDs() {
		return getRegisteredTargets().toArray();
	}
	public IPCDITarget[] addTargets(BitList tasks) {
		List targets = new ArrayList();
		int[] ids = tasks.toArray();
		for (int i = 0; i<ids.length; i++) {
			Integer key = new Integer(ids[i]);
			if (containTarget(key)) {
				//remove already registered process id
				tasks.clear(ids[i]);
				continue;
			}
			IPCDITarget target = new Target((Session)getSession(), ids[i]);
			targets.add(target);
			debugTargetMap.put(key, target);
		}
		return (IPCDITarget[])targets.toArray(new IPCDITarget[0]);
	}
	public boolean removeTarget(int target_id) {
		Integer key = new Integer(target_id);
		if (!debugTargetMap.containsKey(key))
			return false;
		
		debugTargetMap.remove(key);
		return true;
	}
	public void removeTargets(BitList tasks) {
		int[] ids = tasks.toArray();
		for (int i = 0; i<ids.length; i++) {
			if (!removeTarget(ids[i])) {
				tasks.clear(ids[i]);
			}
		}
	}
	public boolean containTarget(Integer key) {
		return debugTargetMap.containsKey(key);
	}
	public boolean containTarget(int target_id) {
		return containTarget(new Integer(target_id));
	}
	public Target getTarget(int target_id) {
		return (Target)debugTargetMap.get(new Integer(target_id));
	}
	public void update(Target target) throws PCDIException {
		//Do nothing here
	}
}
