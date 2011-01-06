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

import java.util.Collection;

import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;

/**
 * @author Greg Watson
 * 
 */
public class RemoveMachineEvent implements
		IRemoveMachineEvent {

	private final IPResourceManager rm;
	private final Collection<IPMachine> machines;

	public RemoveMachineEvent(IPResourceManager manager, Collection<IPMachine> machines) {
		this.rm = manager;
		this.machines = machines;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachineEvent#getMachines
	 * ()
	 */
	public Collection<IPMachine> getMachines() {
		return machines;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachineEvent#getSource
	 * ()
	 */
	public IPResourceManager getSource() {
		return rm;
	}

}
