/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.fdt.managedbuilder.makegen.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.fdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.fdt.managedbuilder.makegen.IManagedDependencyGenerator;

/**
 * This is the dependency calculator used by the makefile generation system when 
 * nothing is defined for a tool.
 *  
 * @since 2.0
 */
public class DefaultNoDependencyCalculator implements IManagedDependencyGenerator {

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource, org.eclipse.core.resources.IProject)
	 */
	public IResource[] findDependencies(IResource resource, IProject project) {
		// Never answers any dependencies
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	public int getCalculatorType() {
		return TYPE_NODEPS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand(org.eclipse.core.resources.IResource)
	 */
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		// Never answers this call with an actual value
		return null;
	}

}
