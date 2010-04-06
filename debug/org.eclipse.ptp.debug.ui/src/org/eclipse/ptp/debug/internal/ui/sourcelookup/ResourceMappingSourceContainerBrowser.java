/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.ui.sourcelookup;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.debug.internal.core.sourcelookup.ResourceMappingSourceContainer;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The browser for adding a mapping source container.
 * 
 * @since 4.0
 */
public class ResourceMappingSourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		IRemoteConnection conn = getRemoteConnection(director.getLaunchConfiguration());
		if (conn != null) {
			ResourceMappingSourceContainerDialog dialog = new ResourceMappingSourceContainerDialog(shell,  new WorkbenchLabelProvider(), new WorkbenchContentProvider(), conn);
			
			if (dialog.open() == Window.OK) {
				ArrayList<ResourceMappingSourceContainer> containers = new ArrayList<ResourceMappingSourceContainer>();			
				containers.add(new ResourceMappingSourceContainer(dialog.getPath(), dialog.getContainer()));
				return (ISourceContainer[])containers.toArray(new ISourceContainer[containers.size()]);	
			}			
		}
		return new ISourceContainer[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return containers.length == 1 && containers[0].getType().getId().equals(FolderSourceContainer.TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		IRemoteConnection conn = getRemoteConnection(director.getLaunchConfiguration());
		if (conn != null) {
			ResourceMappingSourceContainerDialog dialog = new ResourceMappingSourceContainerDialog(shell,  new WorkbenchLabelProvider(), new WorkbenchContentProvider(), conn);
			ResourceMappingSourceContainer container = (ResourceMappingSourceContainer) containers[0];
			dialog.setInitialSelection(container.getContainer());
			if (dialog.open() == Window.OK) {
				container.dispose();
				ArrayList<ResourceMappingSourceContainer> list = new ArrayList<ResourceMappingSourceContainer>();			
				list.add(new ResourceMappingSourceContainer(dialog.getPath(), dialog.getContainer()));
				return (ISourceContainer[])list.toArray(new ISourceContainer[list.size()]);	
			}
		}
		return new ISourceContainer[0];
	}
	
	private IRemoteConnection getRemoteConnection(ILaunchConfiguration configuration) {
		String rmName = null;
		try {
			rmName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String)null);
		} catch (CoreException e) {
		}
		if (rmName != null) {
			IResourceManagerControl rm = (IResourceManagerControl)PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmName);
			if (rm != null) {
				String remId = rm.getConfiguration().getRemoteServicesId();
				String connName = rm.getConfiguration().getConnectionName();
				IRemoteServices rsrv = PTPRemoteCorePlugin.getDefault().getRemoteServices(remId);
				if (rsrv != null) {
					IRemoteConnectionManager connMgr = rsrv.getConnectionManager();
					if (connMgr != null) {
						return connMgr.getConnection(connName);
					}
				}
			}
		}
		return null;
	}
	
}
