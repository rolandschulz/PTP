/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.core.elements.events;

import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent;

/**
 * @author Greg Watson
 *
 */
public class ResourceManagerRemoveMachineEvent implements
	IResourceManagerRemoveMachineEvent {

	private final IResourceManager rm;
	private final IPMachine machine;

	public ResourceManagerRemoveMachineEvent(IResourceManager manager, IPMachine machine) {
		this.rm = manager;
		this.machine = machine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachineEvent#getMachine()
	 */
	public IPMachine getMachine() {
		return machine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachineEvent#getSource()
	 */
	public IResourceManager getSource() {
		return rm;
	}

}
