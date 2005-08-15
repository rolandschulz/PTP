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
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.debug.core.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CDirectorySourceLocation;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CProjectSourceLocation;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceManager;

/**
 * Enter type comment.
 * 
 * @since Jul 14, 2003
 */
public class SourceLookupFactory
{
	public static IProjectSourceLocation createProjectSourceLocation( IProject project )
	{
		return new CProjectSourceLocation( project );
	}

	public static IProjectSourceLocation createProjectSourceLocation( IProject project, boolean generated )
	{
		return new CProjectSourceLocation( project, generated );
	}

	public static IDirectorySourceLocation createDirectorySourceLocation( IPath directory, IPath association, boolean searchSubfolders )
	{
		return new CDirectorySourceLocation( directory, association, searchSubfolders );
	}

	public static ICSourceLocator createSourceLocator( IProject project )
	{
		return new CSourceManager( new CSourceLocator( project ) );
	}
}
