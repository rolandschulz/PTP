/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

/**
 * Provides services for managing a set of indices.  Each index managed by
 * this service is identified by an <code>IScope</code>.  There is a one-to-one mapping between the set of
 * scopes managed and the underlying indices that correspond to them.
 */
public interface IIndexLifecycleService {
	/**
	 * Creates a new scope.
	 * @param scope
	 * @param elements the list of elements which comprise the scope.
	 */
	Scope createScope(String name, List<ICElement> elements, IProgressMonitor monitor);
	
	/**
	 * Returns all of the scopes managed by this service.
	 * @return all of the scopes managed by this service.
	 */
	Set<Scope> getScopes();
	
	/**
	 * Returns the scope with the given name.
	 * @return the scope with the given name.
	 */
	Scope getScope(String name);
	
	/**
	 * Re-indexes the scope elements corresponding to the contents of
	 * <code>elements</code>.
	 *   
	 * @param scope
	 * @param elements The elements to re-index within the scope.  Must not be null.
	 * @param monitor
	 * @param task that this operation originates from
	 */
	void reindex(Scope scope, List<ICElement> elements, IProgressMonitor monitor, RemoteIndexerTask task);
	
	/**
	 * Re-indexes a scope in its entirety.
	 * 
	 * @param scope
	 * @param monitor
	 * @param task that this operation originates from
	 */
	void reindex(Scope scope, IProgressMonitor monitor, RemoteIndexerTask task);
	
	/**
	 * Updates the index identified by <code>scope</code>.  The elements
	 * in <code>newElements</code> are added to the index; those in
	 * <code>deleted</code> are removed.  Elements in
	 * <code>changedElements</code> are re-indexed.
	 * 
	 * @param scope
	 * @param newElements
	 * @param changedElements
	 * @param deletedElements
	 * @param monitor
	 * @param task that this operation originates from
	 */
	void update(Scope scope, List<ICElement> newElements, List<ICElement> changedElements, List<ICElement> deletedElements, IProgressMonitor monitor, RemoteIndexerTask task);
}
