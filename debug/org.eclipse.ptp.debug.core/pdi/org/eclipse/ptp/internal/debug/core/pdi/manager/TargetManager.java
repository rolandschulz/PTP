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
package org.eclipse.ptp.internal.debug.core.pdi.manager;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;

/**
 * @author clement
 *
 */
public class TargetManager extends AbstractPDIManager implements IPDITargetManager {
	private Map<TaskSet, IPDITarget> targetMap;

	public TargetManager(IPDISession session) {
		super(session, true);
		targetMap = new HashMap<TaskSet, IPDITarget>();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager#addTarget(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPDITarget addTarget(TaskSet qTasks) {
		if (getTarget(qTasks) == null) {
			IPDITarget target = session.getModelFactory().newTarget(session, qTasks);
			targetMap.put(qTasks, target);
			return target;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager#cleanup()
	 */
	public void cleanup() {
		targetMap.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager#getTarget(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPDITarget getTarget(TaskSet qTasks) {
		synchronized (targetMap) {
			return targetMap.get(qTasks);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager#getTargets()
	 */
	public IPDITarget[] getTargets() {
		return targetMap.values().toArray(new IPDITarget[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager#removeTarget(org.eclipse.ptp.core.util.TaskSet)
	 */
	public boolean removeTarget(TaskSet qTasks) {
		return (targetMap.remove(qTasks) != null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.manager.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
		targetMap.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.manager.AbstractPDIManager#update(org.eclipse.ptp.core.util.TaskSet)
	 */
	public void update(TaskSet qTasks) throws PDIException {
	}
}
