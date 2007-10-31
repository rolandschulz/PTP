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
package org.eclipse.ptp.debug.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.internal.core.PBreakpointManager;
import org.eclipse.ptp.debug.internal.core.PSetManager;

public interface IPSession extends IAdaptable {
	boolean isReady();
	void dispose();
	IPDISession getPDISession();
	IPJob getJob();
	IPLaunch getLaunch();
	IPDebugTarget findDebugTarget(BitList tasks);
	BitList getTasks(int id);
	BitList getTasks();

	void deleteDebugTargets(boolean register);
	void reloadDebugTargets(BitList tasks, boolean refresh, boolean register);
	void createDebugTarget(BitList tasks, boolean refresh, boolean register);
	void deleteDebugTarget(BitList tasks, boolean refresh, boolean register);
	
	PSetManager getSetManager();
	PBreakpointManager getBreakpointManager();
	
	void forceStoppedDebugger(ProcessAttributes.State state);
}
