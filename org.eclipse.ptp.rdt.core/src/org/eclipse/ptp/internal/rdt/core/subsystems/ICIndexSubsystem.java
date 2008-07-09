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
package org.eclipse.ptp.internal.rdt.core.subsystems;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;



/**
 * @author crecoskie
 * 
 * Provides index related services from a remote host.
 * 
 * This interface is implemented by the indexing subsystem.  It is not meant to be implemented by clients.
 * 
 * It is expected that there is only one ICIndexSubsystem active on a given connector service.
 *
 */
public interface ICIndexSubsystem {

	// index management
	/**
	 * Incrementally indexes the given scope
	 * 
	 * @param scopeName
	 * @param monitor
	 * @return IStatus indicating success or failure
	 */
	public IStatus startIndexOfScope(Scope scope, IProgressMonitor monitor);

	/**
	 * Re-indexes the given scope.
	 * 
	 * @param scope
	 * @param monitor
	 * @return IStatus indicating success or failure
	 */
	public IStatus reindexScope(Scope scope, IProgressMonitor monitor);
	
	/**
	 * Incrementally indexes based on a delta of added, changed, and removed elements.  Valid elements can be
	 * of type ITranslationUnit, ICContainer, or ICProject. 
	 * 
	 * @param scope
	 * @param newElements
	 * @param changedElements
	 * @param deletedElements
	 * @param monitor
	 * @return IStatus indicating success or failure
	 */
	public IStatus indexDelta(Scope scope, List<ICElement> newElements, List<ICElement> changedElements, List<ICElement> deletedElements, IProgressMonitor monitor);
	
	// scope management
	/**
	 * Registers a scope for later reference.
	 * 
	 * @param scope
	 * @param elements The elements which comprise the scope.  Valid element types are ITranslationUnit, ICContainer, and ICProject
	 * @param monitor
	 * @return IStatus indicating success or failure.
	 */
	public IStatus registerScope(Scope scope, List<ICElement> elements, IProgressMonitor monitor);
	
	/**
	 * Unregisters a scope.  Effectively, the scope manager will remove it from its list of managed scopes.
	 * 
	 * @param scope
	 * @param monitor
	 * @return
	 */
	public IStatus unregisterScope(Scope scope, IProgressMonitor monitor);

	// call hierarchy service
	/**
	 * Gets the callers of a given selection.  This method will only succeed if the selection corresponds to
	 * a function, method, or constructor.
	 * 
	 * @param scope
	 * @param subject The name of the ICElement that is the subject of the operation.
	 * @param filePath The path to the file in which the subject resides.
	 * @param selectionStart The offset into the file at which the subject was selected.
	 * @param selectionLength The length of the selection in which the subject was selected.
	 * @param monitor
	 * @return A List of Strings, each corresponding to the serialized String representation of an ICElement that calls the subject.
	 */
	//public List<String> getCallers(Scope scope, String subjectName, String filePath, int selectionStart, int selectionLength, IProgressMonitor monitor);
	public List<String> getCallers(Scope scope, ICElement subject, IProgressMonitor monitor);
	
	/**
	 * Gets the callees of a given selection.  This method will only succeed if the selection corresponds to
	 * a function, method, or constructor.
	 * 
	 * @param scope
	 * @param subject The name of the ICElement that is the subject of the operation.
	 * @param filePath The path to the file in which the subject resides.
	 * @param selectionStart The offset into the file at which the subject was selected.
	 * @param selectionLength The length of the selection in which the subject was selected.
	 * @param monitor
	 * @return A List of Strings, each corresponding to the serialized String representation of an ICElement that calls the subject.
	 */	
	public List<String> getCallees(Scope scope, ICElement subject, IProgressMonitor monitor);
	
	/**
	 * Gets the definition of the element in a given selection.
	 * 
	 * @param scope
	 * @param subject The name of the ICElement that is the subject of the operation.
	 * @param filePath The path to the file in which the subject resides.
	 * @param selectionStart The offset into the file at which the subject was selected.
	 * @param selectionLength The length of the selection in which the subject was selected.
	 * @param monitor
	 * @return A List of Strings, each corresponding to the serialized String representation of an ICElement that calls the subject.
	 */
	public List<String> getCHDefinitions(Scope scope, ICElement subject, IProgressMonitor monitor);
	
	public List<String> runQuery(Scope scope, RemoteSearchQuery query, IProgressMonitor monitor);
}
