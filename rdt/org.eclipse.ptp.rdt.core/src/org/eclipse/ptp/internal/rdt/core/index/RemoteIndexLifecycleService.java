/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProviderFactory;
import org.eclipse.ptp.internal.rdt.core.RemoteProjectResourcesUtil;
import org.eclipse.ptp.internal.rdt.core.index.IRemoteFastIndexerUpdateEvent.EventType;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * @author crecoskie
 *
 */
public class RemoteIndexLifecycleService extends AbstractRemoteService implements IIndexLifecycleService {
	
	private Map<String, Scope> fStringToScopeMap = new TreeMap<String, Scope>();

	public RemoteIndexLifecycleService(IConnectorService connectorService) {
		super(connectorService);
	}

	public RemoteIndexLifecycleService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}

	public Scope getScope(String name) {
		return fStringToScopeMap.get(name);
	}


	public Set<Scope> getScopes() {
		Set<Scope> set = new TreeSet<Scope>();
		set.addAll(fStringToScopeMap.values());
		return set;
	}


	public void reindex(Scope scope, String indexLocation, List<ICElement> changedElements, IProgressMonitor monitor, RemoteIndexerTask task) {
//		ICIndexSubsystem indexSubsystem = getSubSystem();
//		IRemoteIndexerInfoProvider provider = RemoteIndexerInfoProviderFactory.getProvider(changedElements);
//		
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		IWorkspaceRoot workspaceRoot = workspace.getRoot();
//		IProject project = workspaceRoot.getProject(scope.getName());
//		if (project != null && project.isOpen()){
//			indexSubsystem.checkProject(project, new NullProgressMonitor());
//		}
//		
//		// TODO:  handle changedElements
//		indexSubsystem.reindexScope(scope, provider, indexLocation, monitor, task);
		reindex(scope, indexLocation, monitor, task);
	}


	public void update(Scope scope, List<ICElement> newElements,
			List<ICElement> changedElements, List<ICElement> deletedElements, IProgressMonitor monitor, RemoteIndexerTask task) {
		
		List<ICElement> filertedNewElements = RemoteProjectResourcesUtil.filterElements(newElements);
		List<ICElement> filteredChangedElements = RemoteProjectResourcesUtil.filterElements(changedElements);
		if(filertedNewElements.isEmpty() && filteredChangedElements.isEmpty() && deletedElements.isEmpty()){
			return;
		}
		List<ICElement> elements = new ArrayList<ICElement>(filertedNewElements);
		elements.addAll(filteredChangedElements);
		
		IRemoteIndexerInfoProvider provider = RemoteIndexerInfoProviderFactory.getProvider(elements);
		ICIndexSubsystem indexSubsystem = getSubSystem();
		indexSubsystem.indexDelta(scope, provider, filertedNewElements, filteredChangedElements, deletedElements, monitor, task);
	}
	

	public void reindex(Scope scope, String indexLocation, IProgressMonitor monitor, RemoteIndexerTask task) {
		IRemoteIndexerInfoProvider provider = RemoteIndexerInfoProviderFactory.getProvider(scope.getName());
		ICIndexSubsystem indexSubsystem = getSubSystem();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject project = workspaceRoot.getProject(scope.getName());
		if (project != null && project.isOpen()){
			indexSubsystem.checkProject(project, new NullProgressMonitor());
		}
			
		indexSubsystem.reindexScope(scope, provider, indexLocation, monitor, task);
			
	}

	
	public String moveIndexFile(String scopeName, String newIndexLocation, IProgressMonitor monitor) {
		ICIndexSubsystem indexSubsystem = getSubSystem();
		return indexSubsystem.moveIndexFile(scopeName, newIndexLocation, monitor);
	}
		


	public EventType getReIndexEventType() {
		
		return getSubSystem().getReIndexEventType();
	}
	
}
