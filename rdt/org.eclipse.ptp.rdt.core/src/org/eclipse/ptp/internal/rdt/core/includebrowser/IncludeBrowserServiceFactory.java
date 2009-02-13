package org.eclipse.ptp.internal.rdt.core.includebrowser;

import org.eclipse.cdt.core.model.ICProject;
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

public class IncludeBrowserServiceFactory
{

	public IIncludeBrowserService getIncludeBrowserService(ICProject project)
	{
		if (project == null)
			return null;
		
		boolean remoteProject = false;
		
		IProjectDescription description;
		try
		{
			description = project.getProject().getDescription();
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
				IIncludeBrowserService service = ((IIndexServiceProvider) serviceProvider).getIncludeBrowserService();
				
				return service;
			}
		}
		else
		{
			return new LocalIncludeBrowserService();
		}

		return null;
	}
}
