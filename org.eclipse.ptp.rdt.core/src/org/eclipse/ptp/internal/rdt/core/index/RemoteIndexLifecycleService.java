/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IProgressMonitor;
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
	public RemoteIndexLifecycleService(IHost host,
			IConnectorService connectorService) {
		fHost = host;
		fConnectorService = connectorService;
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#reindex(org.eclipse.ptp.internal.rdt.core.model.Scope, java.util.List)
	 */
	public void reindex(Scope scope, List<ICElement> changedElements, IProgressMonitor monitor) {
		ICIndexSubsystem indexSubsystem = getSubSystem();
		
		// TODO:  handle changedElements
		indexSubsystem.reindexScope(scope, monitor);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.index.IIndexLifecycleService#update(org.eclipse.ptp.internal.rdt.core.model.Scope, java.util.List, java.util.List, java.util.List)
	 */
	public void update(Scope scope, List<ICElement> newElements,
			List<ICElement> changedElements, List<ICElement> deletedElements, IProgressMonitor monitor) {
		
		ICIndexSubsystem indexSubsystem = getSubSystem();
		indexSubsystem.indexDelta(scope, newElements, changedElements, deletedElements, monitor);

	}
	
	public void reindex(Scope scope, IProgressMonitor monitor) {
		ICIndexSubsystem indexSubsystem = getSubSystem();
		
		indexSubsystem.reindexScope(scope, monitor);
	}

}
