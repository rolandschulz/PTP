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
package org.eclipse.ptp.debug.external.cdi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.cdi.model.Target;

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
		debugTargetMap = null;
	}
	public Target[] getTargets() {
		return (Target[]) debugTargetMap.values().toArray(new Target[debugTargetMap.size()]);
	}
	public ICDITarget[] getCDITargets() {
		return (ICDITarget[]) debugTargetMap.values().toArray(new ICDITarget[debugTargetMap.size()]);
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
	public void addTargets(IPCDITarget[] targets, BitList regTasks) {
		for (int i = 0; i<targets.length; i++) {
			IPCDITarget target = targets[i];
			Integer key = new Integer(target.getTargetID());
			if (debugTargetMap.containsKey(key))
				continue;
			
			debugTargetMap.put(key, target);
			regTasks.set(target.getTargetID());
		}
	}
	public boolean removeTarget(int target_id) {
		Integer key = new Integer(target_id);
		if (!debugTargetMap.containsKey(key))
			return false;
		
		debugTargetMap.remove(key);
		return true;
	}
	public void removeTargets(int[] target_ids, BitList regTasks) {
		for (int i = 0; i<target_ids.length; i++) {
			if (removeTarget(target_ids[i]))
				regTasks.set(target_ids[i]);
		}
	}
	public void removeTargets(IPCDITarget[] targets, BitList regTasks) {
		for (int i = 0; i<targets.length; i++) {
			if (removeTarget(targets[i].getTargetID()))
				regTasks.set(targets[i].getTargetID());
		}
	}
	public boolean containTarget(int target_id) {
		return debugTargetMap.containsKey(new Integer(target_id));
	}
	public Target getTarget(int target_id) {
		return (Target)debugTargetMap.get(new Integer(target_id));
	}
	public void update(Target target) throws CDIException {
		//Do nothing here
	}
}
