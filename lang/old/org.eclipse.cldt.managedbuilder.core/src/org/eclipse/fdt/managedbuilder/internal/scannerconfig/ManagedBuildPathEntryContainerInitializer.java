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

package org.eclipse.fdt.managedbuilder.internal.scannerconfig;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.model.CoreModel;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.PathEntryContainerInitializer;

/**
 * @since 2.0
 */
public class ManagedBuildPathEntryContainerInitializer extends PathEntryContainerInitializer {
	private static final String TRACE_FOOTER = "]: ";	//$NON-NLS-1$
	private static final String TRACE_HEADER = "PathEntryContainerInitializer trace [";	//$NON-NLS-1$
	public static boolean VERBOSE = false;

	/**
	 * Need a zero-argument constructor to allow the system to create 
	 * the intitializer 
	 */
	public ManagedBuildPathEntryContainerInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.PathEntryContainerInitializer#initialize(org.eclipse.core.runtime.IPath, org.eclipse.fdt.core.model.ICProject)
	 */
	public void initialize(IPath containerPath, ICProject project) throws CoreException {
		if (VERBOSE) {
			System.out.println(TRACE_HEADER + 
					project.getProject().getName() + 
					TRACE_FOOTER + 
					"Initializing path entry container");	//$NON-NLS-1$
		}
		CoreModel.getDefault().setPathEntryContainer(new ICProject[]{project}, new ManagedBuildCPathEntryContainer(project.getProject()), null);
	}

}
