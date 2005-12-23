/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.sourcelookup; 

import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
 
/**
 * Computes the default source lookup path for a launch configuration.
 */
public class PSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	/** 
	 * Constructor for CSourcePathComputerDelegate. 
	 */
	public PSourcePathComputerDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers( ILaunchConfiguration configuration, IProgressMonitor monitor ) throws CoreException {
		ISourceContainer[] common = PTPDebugCorePlugin.getDefault().getCommonSourceLookupDirector().getSourceContainers();
		ArrayList containers = new ArrayList( common.length + 1 );
		containers.addAll( Arrays.asList( common ) );
		String projectName = configuration.getAttribute( IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null );
		if ( projectName != null ) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( project.exists() ) {
				containers.add( 0, new ProjectSourceContainer( project, true ) );
			}
		}
		return (ISourceContainer[])containers.toArray( new ISourceContainer[containers.size()] );
	}
}
