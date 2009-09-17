/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mike Kucera (IBM) - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.services.internal.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceCategory;

public class ServiceCategory implements IServiceCategory {

	private final String id;
	private final String name;
	
	private final Set<IService> services = new HashSet<IService>();
	
	public ServiceCategory(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addService(IService service) {
		if(service != null)
			services.add(service);
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<IService> getServices() {
		return Collections.unmodifiableSet(services);
	}

}
