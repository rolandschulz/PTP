/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.includebrowser;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteIndexFileLocation;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.subsystems.IConnectorService;

public class RemoteIncludeBrowserService extends AbstractRemoteService implements IIncludeBrowserService
{
	public RemoteIncludeBrowserService(IConnectorService connectorService)
	{
		super(connectorService);
	}
	
	public RemoteIncludeBrowserService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}

	public IIndexIncludeValue findInclude(IInclude include, IProgressMonitor monitor) throws CoreException
	{
		if (include != null) 
		{
			
			//String projectLocation = include.getCProject().getProject().getLocationURI().getPath();
			//if(include.getLocationURI().getPath().startsWith(projectLocation)) {
				// internal files use the URI field
				IIndexFileLocation location = new RemoteIndexFileLocation(include.getParent().getPath().toString(), include.getLocationURI());
			//}
			
			//else {
				// external files use the fullp
			//}
			
			ICIndexSubsystem subsystem = getSubSystem();
			subsystem.checkProject(include.getCProject().getProject(), null);
			
			String elementName= include.getElementName();
			elementName= elementName.substring(elementName.lastIndexOf('/')+1);
			
			ISourceRange pos= include.getSourceRange();
			int offset = pos.getIdStartPos();

			return subsystem.findInclude(Scope.WORKSPACE_ROOT_SCOPE, location, elementName, offset, monitor);
		}
		
		return null;
	}

	public IIndexIncludeValue[] findIncludedBy(IIndexFileLocation location, ICProject project, IProgressMonitor monitor)
	{
		if (project != null && location != null) 
		{
			ICIndexSubsystem subsystem = getSubSystem();
			subsystem.checkProject(project.getProject(), monitor);
			
			//location = new RemoteIndexFileLocation(location.getURI().getPath(), null);
			location = new RemoteIndexFileLocation(location);
			
			return subsystem.findIncludedBy(Scope.WORKSPACE_ROOT_SCOPE, location, monitor);
		}

		return new IIndexIncludeValue[0];
	}

	public IIndexIncludeValue[] findIncludesTo(IIndexFileLocation location, ICProject project, IProgressMonitor monitor)
	{
		if (project != null && location != null) 
		{
			ICIndexSubsystem subsystem = getSubSystem();
			subsystem.checkProject(project.getProject(), monitor);
			
			//location = new RemoteIndexFileLocation(location.getURI().getPath(), null);
			 location = new RemoteIndexFileLocation(location);
			
			return subsystem.findIncludesTo(Scope.WORKSPACE_ROOT_SCOPE, location, monitor);
		}

		return new IIndexIncludeValue[0];
	}

	public boolean isIndexed(IIndexFileLocation location, ICProject project, IProgressMonitor monitor)
	{
		if (project != null && location != null) 
		{
			ICIndexSubsystem subsystem = getSubSystem();
			subsystem.checkProject(project.getProject(), monitor);
			
			//location = new RemoteIndexFileLocation(location.getURI().getPath(), null);
			 location = new RemoteIndexFileLocation(location);
			
			return subsystem.isIndexed(Scope.WORKSPACE_ROOT_SCOPE, location, monitor);
		}
		
		return false;
	}

}
