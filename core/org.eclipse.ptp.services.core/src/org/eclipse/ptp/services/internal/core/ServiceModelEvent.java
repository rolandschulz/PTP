/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.ptp.services.core.IServiceProvider;


public class ServiceModelEvent extends EventObject implements IServiceModelEvent {
	private static final long serialVersionUID = 1L;
	
	private int type;
	private IServiceProvider oldProvider;

	public ServiceModelEvent(Object source, int type) {
		super(source);
		this.type = type;
	}

	public ServiceModelEvent(Object source, int type, IServiceProvider oldProvider) {
		this(source, type);
		this.oldProvider = oldProvider;
	}
	
	public int getType() {
		return type;
	}

	public IServiceProvider getOldProvider() {
		return oldProvider;
	}
	
}
