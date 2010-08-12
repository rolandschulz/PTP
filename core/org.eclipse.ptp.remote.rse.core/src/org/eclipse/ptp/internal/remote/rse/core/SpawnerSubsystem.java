/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.remote.rse.core.miners.SpawnerMiner;
import org.eclipse.ptp.remote.rse.core.RSEAdapterCorePlugin;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.internal.services.dstore.shells.DStoreShellService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.dstore.util.DStoreStatusMonitor;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;

/**
 * @author crecoskie
 *
 */
@SuppressWarnings("restriction")
public class SpawnerSubsystem extends SubSystem implements ISubSystem {

	private boolean fIsInitializing;

	protected SpawnerSubsystem(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
		setHidden(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public synchronized void initializeSubSystem(IProgressMonitor monitor)
			throws SystemMessageException {
		
		boolean isFirstCall = false;
		if(!fIsInitializing) {
			fIsInitializing = true;
			isFirstCall = true;
		}
		
		try {
			super.initializeSubSystem(monitor);
			DataStore dataStore = getDataStore(monitor);
			DataElement status = dataStore
					.activateMiner("org.eclipse.ptp.internal.remote.rse.core.miners.SpawnerMiner"); //$NON-NLS-1$
			
			if (status != null) {
				DStoreStatusMonitor statusMonitor = new DStoreStatusMonitor(dataStore);

				// wait for the miner to be fully initialized
				try {
					statusMonitor.waitForUpdate(status, monitor);
				} catch (InterruptedException e) {
					RSEAdapterCorePlugin.log(e);
				}
			}
		}

		finally {
			if (isFirstCall)
				fIsInitializing = false;
		}
	}
	
	protected synchronized DataStore getDataStore(IProgressMonitor monitor)
	{
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
			
		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			RSEAdapterCorePlugin.log(e);
			return null;
		}
			
		IConnectorService connectorService = getConnectorService();
		
		if(connectorService instanceof DStoreConnectorService) {
			DStoreConnectorService dstoreConnectorService = (DStoreConnectorService) connectorService;
			if(!fIsInitializing && !dstoreConnectorService.isConnected()) {
				try {
					dstoreConnectorService.connect(monitor);
				} catch (Exception e) {
					RSEAdapterCorePlugin.log(e);
				}
			}
			return dstoreConnectorService.getDataStore();

		}
		return null;
	}
	
	@SuppressWarnings("restriction")
	public synchronized IHostShell spawnRedirected(String cmd, String workingDirectory, String encoding, String[] envp, IProgressMonitor monitor) {
		DataStore dataStore = getDataStore(monitor);
		   
	    if (dataStore != null)
	    {	     	
	    	
	    	monitor.beginTask("Launching command: " + cmd, 100); //$NON-NLS-1$
	   
	        DataElement queryCmd = dataStore.localDescriptorQuery(dataStore.getDescriptorRoot(), SpawnerMiner.C_SPAWN_REDIRECTED);
            if (queryCmd != null)
            {
            	
            	ArrayList<Object> args = new ArrayList<Object>();
            	
            	DataElement dataElement = dataStore.createObject(null, SpawnerMiner.T_SPAWNER_STRING_DESCRIPTOR, cmd);
            	args.add(dataElement);
            	
            	dataElement = dataStore.createObject(null, SpawnerMiner.T_SPAWNER_STRING_DESCRIPTOR, workingDirectory);
            	args.add(dataElement);
            	
            	for(String envVar : envp) {
            		dataElement = dataStore.createObject(null, SpawnerMiner.T_SPAWNER_STRING_DESCRIPTOR, envVar);
                	args.add(dataElement);
            	}
            	
            	// execute the command
            	DataElement status = dataStore.command(queryCmd, args, dataStore.getDescriptorRoot());
            	
            	IShellService shellService = getShellServiceSubSystem().getShellService();
            	
            	DStoreStatusMonitor statusMonitor;
            	
            	// should be the DStoreShellService
            	if(shellService instanceof DStoreShellService) {
            		statusMonitor = ((DStoreShellService)shellService).getStatusMonitor(dataStore);
            		return new DStoreHostShell(statusMonitor, status, dataStore, workingDirectory, cmd, encoding, envp);
            	}
            }
	    }
	    
	    return null;
	}
	
	public IShellServiceSubSystem getShellServiceSubSystem() {
		ISubSystem[] subsystems = getConnectorService().getSubSystems();
		
		for(ISubSystem subsystem : subsystems) {
			if(subsystem instanceof IShellServiceSubSystem)
				return (IShellServiceSubSystem) subsystem;
		}
		
		return null;
	}
            
}
