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
package org.eclipse.ptp.debug.external.cdi.event;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

/**
 */
public class MemoryChangedEvent implements ICDIMemoryChangedEvent {

	public BigInteger[] getAddresses() {
		// Auto-generated method stub
		System.out.println("MemoryChangedEvent.getAddresses()");
		return null;
	}

	public ICDIObject getSource() {
		// Auto-generated method stub
		System.out.println("MemoryChangedEvent.getSource()");
		return null;
	}
}
