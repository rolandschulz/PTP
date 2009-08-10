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
package org.eclipse.ptp.services.internal.core;

import java.util.EventObject;

import org.eclipse.ptp.services.core.IServiceModelEvent;


public class ServiceModelEvent extends EventObject implements IServiceModelEvent {
	private static final long serialVersionUID = 1L;
	
	private int type;

	public ServiceModelEvent(Object source, int type) {
		super(source);
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.IServiceModelEvent#getType()
	 */
	public int getType() {
		return type;
	}
}
