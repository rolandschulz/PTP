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

import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;

public abstract class Manager extends SessionObject {
	boolean autoUpdate;

	public Manager(Session session, boolean update) {
		super(session);
		autoUpdate = update;
	}
	public void setAutoUpdate(boolean update) {
		autoUpdate = update;
	}
	public boolean isAutoUpdate() {
		return autoUpdate;
	}
	protected abstract void update (Target target) throws PCDIException;
	protected abstract void shutdown();

	public void update() throws PCDIException {
		IPCDITarget[] targets = getSession().getTargets();
		for (int i = 0; i < targets.length; ++i) {
			if (targets[i] instanceof Target) {
				update((Target)targets[i]);
			}
		}
	}
	//protected void handleDebugEvents(IPCDIEvent[] events);
}
