/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 * 
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.ibmpe.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class PEResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration {

	public static IResourceManagerConfiguration load(PEResourceManagerFactory factory,
			IMemento memento) {

		RemoteConfig remoteConfig = loadRemote(factory, memento);
		
		PEResourceManagerConfiguration config = 
			new PEResourceManagerConfiguration(factory, remoteConfig);

		return config;
	}
	
	public PEResourceManagerConfiguration(PEResourceManagerFactory factory,
			RemoteConfig remoteConfig) {
		super(remoteConfig, factory);
	}
	
	public PEResourceManagerConfiguration(PEResourceManagerFactory factory) {
		this(factory, new RemoteConfig());
		setDefaultNameAndDesc();
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = "PE";
		String conn = getConnectionName();
		if (conn != null && !conn.equals("")) {
			name += "@" + conn;
		}
		setName(name);
		setDescription("PE Resource Manager");
	}
}