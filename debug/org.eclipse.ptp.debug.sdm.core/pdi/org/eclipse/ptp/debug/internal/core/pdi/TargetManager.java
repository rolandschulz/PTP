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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDITargetManager;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.internal.core.pdi.model.Target;

/**
 * @author clement
 *
 */
public class TargetManager extends Manager implements IPDITargetManager {
	private Map<BitList, Target> targetMap;

	public TargetManager(Session session) {
		super(session, true);
		targetMap = new HashMap<BitList, Target>();
	}
	public void shutdown() {
		targetMap.clear();
	}
	public void cleanup() {
		targetMap.clear();
	}
	public Target getTarget(BitList qTasks) {
		synchronized (targetMap) {
			return (Target)targetMap.get(qTasks);
		}
	}
	public Target[] getTargets() {
		return (Target[]) targetMap.values().toArray(new Target[0]);
	}
	public Target addTarget(BitList qTasks) {
		if (getTarget(qTasks) == null) {
			Target target = new Target(session, qTasks);
			targetMap.put(qTasks, target);
			return target;
		}
		return null;
	}
	public boolean removeTarget(BitList qTasks) {
		return (targetMap.remove(qTasks) != null);
	}
	public void update(BitList qTasks) throws PDIException {
	}
}
