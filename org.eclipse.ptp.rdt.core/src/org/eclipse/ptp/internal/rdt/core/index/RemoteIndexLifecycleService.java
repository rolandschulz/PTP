/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProviderFactory;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * @author crecoskie
 *
 */
public class RemoteIndexLifecycleService extends AbstractRemoteService implements IIndexLifecycleService {
	
	private Map<String, Scope> fStringToScopeMap = new TreeMap<String, Scope>();
	public RemoteIndexLifecycleService(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#createScope(java.lang.String)
	 */
	public Scope createScope(String name, List<ICElement> elements, IProgressMonitor monitor) {
		Scope scope = new Scope(name);
		fStringToScopeMap.put(name, scope);
		
		ICIndexSubsystem indexSubsystem = getSubSystem();
		
		indexSubsystem.registerScope(scope, elements, monitor);
		
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#getScope(java.lang.String)
	 */
	public Scope getScope(String name) {
		return fStringToScopeMap.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#getScopes()
	 */
	public Set<Scope> getScopes() {
		Set<Scope> set = new TreeSet<Scope>();
		set.addAll(fStringToScopeMap.values());
		return set;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#reindex(org.eclipse.ptp.internal.rdt.core.model.Scope, java.util.List, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask)
	 */
	public void reindex(Scope scope, List<ICElement> changedElements, IProgressMonitor monitor, RemoteIndexerTask task) {
		ICIndexSubsystem indexSubsystem = getSubSystem();
		IRemoteIndexerInfoProvider provider = RemoteIndexerInfoProviderFactory.getProvider(changedElements);
		// TODO:  handle changedElements
		indexSubsystem.reindexScope(scope, provider, monitor, task);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#update(org.eclipse.ptp.internal.rdt.core.model.Scope, java.util.List, java.util.List, java.util.List)
	 */
	public void update(Scope scope, List<ICElement> newElements,
			List<ICElement> changedElements, List<ICElement> deletedElements, IProgressMonitor monitor, RemoteIndexerTask task) {
		
		List<ICElement> elements = new ArrayList<ICElement>(newElements);
		elements.addAll(changedElements);
		
		IRemoteIndexerInfoProvider provider = RemoteIndexerInfoProviderFactory.getProvider(elements);
		ICIndexSubsystem indexSubsystem = getSubSystem();
		indexSubsystem.indexDelta(scope, provider, newElements, changedElements, deletedElements, monitor, task);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#reindex(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask)
	 */
	public void reindex(Scope scope, IProgressMonitor monitor, RemoteIndexerTask task) {
		IRemoteIndexerInfoProvider provider = RemoteIndexerInfoProviderFactory.getProvider(scope.getName());
		ICIndexSubsystem indexSubsystem = getSubSystem();
		
		indexSubsystem.reindexScope(scope, provider, monitor, task);
	}

}
