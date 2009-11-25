/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.model;

import java.util.Set;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.ptp.internal.rdt.core.miners.ScopeManager;

/**
 * @author crecoskie
 *
 */
public class RemoteCProjectFactory implements ICProjectFactory {

	
	private ICProject defaultProject = null;
	
	public RemoteCProjectFactory() {
	}


	public RemoteCProjectFactory(ICProject defaultProject) {
		this.defaultProject = defaultProject;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.model.ICProjectFactory#getProjectForFile(java.lang.String)
	 */
	public ICProject getProjectForFile(String filename) {
		// try to use the scope manager to determine the project
		Set<String> projectNames = ScopeManager.getInstance().getScopesForFile(filename);

		// use the first project we find
		for (String projName : projectNames) {
			return new CProject(projName);
		}
		
		return defaultProject;
	}

	
	public void setDefaultProject(ICProject defaultProject) {
		this.defaultProject = defaultProject;
	}
}
