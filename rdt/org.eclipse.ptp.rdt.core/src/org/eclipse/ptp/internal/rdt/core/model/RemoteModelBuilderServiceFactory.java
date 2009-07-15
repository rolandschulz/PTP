/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.IServiceModelManager;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;

public class RemoteModelBuilderServiceFactory {
	public IModelBuilderService getModelBuilderService(IProject project)
	{
		if (project == null)
			return null;
		
		boolean remoteProject = false;
		
		IProjectDescription description;
		try
		{
			description = project.getDescription();
		}
		catch (CoreException e)
		{
			e.printStackTrace();
			return null;
		}
		
		String[] natures = description.getNatureIds();
		
		for (String nature : natures) 
		{
			if (RemoteNature.REMOTE_NATURE_ID.equals(nature))
			{
				remoteProject = true;
				break;
			}
		}
		
		
		if (remoteProject)
		{
			IServiceModelManager smm = ServiceModelManager.getInstance();
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project.getProject());

			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);

			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);

			if (serviceProvider instanceof IIndexServiceProvider) 
			{
				IModelBuilderService service = ((IIndexServiceProvider) serviceProvider).getModelBuilderService();
				
				return service;
			}
		}
		return null;
	}
}
