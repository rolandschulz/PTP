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
package org.eclipse.ptp.internal.debug.core.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.event.IPDebugRegisterInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;

/**
 * @author Clement
 */
public class PDebugRegisterInfo extends PDebugInfo implements IPDebugRegisterInfo {
	private final boolean refresh;

	/**
	 * @since 5.0
	 */
	public PDebugRegisterInfo(IPLaunch launch, TaskSet allProcesses, TaskSet allRegProcesses, TaskSet allUnregProcesses,
			boolean refresh) {
		super(launch, allProcesses, allRegProcesses, allUnregProcesses);
		this.refresh = refresh;
	}

	public PDebugRegisterInfo(IPDebugInfo info, boolean refresh) {
		super(info);
		this.refresh = refresh;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.event.IPDebugRegisterInfo#isRefresh()
	 */
	public boolean isRefresh() {
		return refresh;
	}
}
