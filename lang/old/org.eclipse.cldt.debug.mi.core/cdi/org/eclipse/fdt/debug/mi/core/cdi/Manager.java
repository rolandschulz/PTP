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

package org.eclipse.fdt.debug.mi.core.cdi;

import org.eclipse.fdt.debug.core.cdi.CDIException;
import org.eclipse.fdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.fdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.fdt.debug.mi.core.cdi.model.Target;

/**
 * Manager
 *
 */
public abstract class Manager extends SessionObject {

	boolean autoUpdate;

	public Manager(Session session, boolean update) {
		super(session);
		autoUpdate = update;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.ICDIUpdateManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoUpdate = update;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.ICDIUpdateManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	protected abstract void update (Target target) throws CDIException;

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.impl.Manager#update()
	 */
	public void update() throws CDIException {
		ICDITarget[] targets = getSession().getTargets();
		for (int i = 0; i < targets.length; ++i) {
			if (targets[i] instanceof Target) {
				update((Target)targets[i]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.fdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents(ICDIEvent[] events) {
	}
	
}
