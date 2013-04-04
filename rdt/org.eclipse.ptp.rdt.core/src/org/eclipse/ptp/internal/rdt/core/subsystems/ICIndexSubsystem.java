/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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
import java.util.Map;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CalledByResult;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallsToResult;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteDefaultCodeFormatterOptions;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIndexIncludeValue;
import org.eclipse.ptp.internal.rdt.core.index.IRemoteFastIndexerUpdateEvent.EventType;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerTask;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.navigation.FoldingRegionsResult;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraph;
import org.eclipse.text.edits.TextEdit;



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

	/**
	 * Checks the state of the given project to ensure it is indexing services
	 * for it is initialized and consistent.
	 * 
	 * @param project
	 * @param monitor
	 */
	public void checkProject(IProject project, IProgressMonitor monitor);
	
	/**
	 * Checks the state of all the projects in the workspace to ensure
	 * indexing services for them are all initialized and consistent.
	 * 
	 * @param monitor
	 */
	public void checkAllProjects(IProgressMonitor monitor);

	// index management

	/**
	 * Re-indexes the given scope.
	 * 
	 * As a precaution the index location is taken as a parameter to ensure
	 * that the index file is always generated in the correct location.
	 * 
	 * @param scope
	 * @param monitor
	 * @param indexLocation Path to the location of the index file.
	 * @param task that this operation originates from
	 * @return IStatus indicating success or failure
	 */
	public IStatus reindexScope(Scope scope, IRemoteIndexerInfoProvider provider, String indexLocation, IProgressMonitor monitor, RemoteIndexerTask task);
	
	/**
	 * Incrementally indexes based on a delta of added, changed, and removed elements.  Valid elements can be
	 * of type ITranslationUnit, ICContainer, or ICProject. 
	 * 
	 * @param scope
	 * @param newElements
	 * @param changedElements
	 * @param deletedElements
	 * @param monitor
	 * @param task that this operation originates from
	 * @return IStatus indicating success or failure
	 */
	public IStatus indexDelta(Scope scope, IRemoteIndexerInfoProvider provider,	List<ICElement> newElements, List<ICElement> changedElements, 
			List<ICElement> deletedElements, IProgressMonitor monitor, RemoteIndexerTask task);
	
	
	// scope management
	/**
	 * Registers a scope for later reference.
	 * 
	 * @param scope
	 * @param elements The elements which comprise the scope.  Valid element types are ITranslationUnit, ICContainer, and ICProject
	 * @param indexLocation The path of the directory that contains the index file.
	 * @param monitor
	 * @return IStatus indicating success or failure.
	 */
	public IStatus registerScope(Scope scope, List<ICElement> elements, String indexLocation, IProgressMonitor monitor);
	
	/**
	 * Unregisters a scope.  Effectively, the scope manager will remove it from its list of managed scopes.
	 * 
	 * @param scope
	 * @param monitor
	 * @return
	 */
	public IStatus unregisterScope(Scope scope, IProgressMonitor monitor);
	
	/**
	 * Remove index file on remote host.
	 * 
	 * @param scope
	 * @param monitor
	 * @return
	 */
	public IStatus removeIndexFile(Scope scope, IProgressMonitor monitor);


	// call hierarchy service
	/**
	 * Gets the callers of a given selection.  This method will only succeed if the selection corresponds to
	 * a function, method, or constructor.
	 * 
	 * @param scope
	 * @param subject The ICElement that is the subject of the operation.
	 * @param monitor
	 */
	public CalledByResult getCallers(Scope scope, ICElement subject, IProgressMonitor monitor);
	
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
	 */	
	public CallsToResult getCallees(Scope scope, ICElement subject, IProgressMonitor monitor);
	
	/**
	 * Gets the definition of the element in a given selection.
	 * 
	 * @param scope
	 * @param subject The name of the ICElement that is the subject of the operation.
	 * @param monitor
	 * @return the ICElements corresponding to each known definition of the subject.
	 */
	public ICElement[] getCHDefinitions(Scope scope, ICElement subject, IProgressMonitor monitor);
	
	/**
	 * Gets the definition of the element in a given selection.
	 * 
	 * @param scope
	 * @param unit The ITranslationUnit that contains the selection
	 * @param selectionStart The offset into the translation unit at which the subject was selected.
	 * @param selectionLength The length of the selection in which the subject was selected.
	 * @param monitor
	 * @return the ICElements corresponding to each known definition of the subject.
	 */
	public ICElement[] getCHDefinitions(Scope scope, ITranslationUnit unit, int selectionStart, int selectionLength, IProgressMonitor pm);
	
	/**
	 * Find the overrider of the given subject ICElement.
	 * @param scope
	 * @param subject
	 * @param monitor
	 * @return the overrider in the <code>Map<String, ICElement[]></code>, which has linkageid as the key and 
	 * <code>ICElement[]</code> as the value for overrider.
	 * @since 4.0
	 */
	public Map<String, ICElement[]> findOverriders(Scope scope, ICElement subject, IProgressMonitor monitor);
	
	/**
	 * Runs the following search query and returns the results.
	 * 
	 * @param scope
	 * @param query The query to be run.
	 * @param monitor
	 * @return A List of RemoteSearchMatch objects containing the search results.
	 * @deprecated
	 */
	public List<RemoteSearchMatch> runQuery(Scope scope, RemoteSearchQuery query, IProgressMonitor monitor);
	
	
	/**
	 * Runs the following search query and returns the results.
	 * 
	 * @param scope
	 * @param query The query to be run.
	 * @param monitor
	 * @return A RemoteSearchQuery that contains a List of RemoteSearchMatch objects containing the search results.
	 * @since 4.0
	 */
	public RemoteSearchQuery runQuery2(Scope scope, RemoteSearchQuery query, IProgressMonitor monitor);

	/**
	 * Returns content assist completion proposals for the given context.
	 * 
	 * @param scope
	 * @param context describes the conditions under which content assist was
	 *                invoked.
	 * @param unit the translation unit in which content assist was invoked.
	 * @return completion proposals for the given context.
	 */
	public List<Proposal> computeCompletionProposals(Scope scope, RemoteContentAssistInvocationContext context, ITranslationUnit unit);

	/**
	 * Returns the supertype and subtype graph for the class associated with
	 * with given input.
	 * 
	 * @param scope
	 * @param input the element for which a supertype and subtype graph should be
	 *              computed.
	 * @param monitor
	 * @return
	 */
	public THGraph computeTypeGraph(Scope scope, ICElement input, IProgressMonitor monitor);

	/**
	 * Returns a pair of <code>ICElement</code>s corresponding to the
	 * definition of the associated type of the given input, as well as the
	 * definition of the input.
	 * 
	 * @param scope
	 * @param input the element from which the associated type will be computed.
	 * @return
	 */
	public ICElement[] findTypeHierarchyInput(Scope scope, ICElement input);

	/**
	 * Returns a pair of <code>ICElement</code>s corresponding to the
	 * definition of the parent type of the given selection in the
	 * <code>ITranslationUnit</code>, as well as the definition of the
	 * selection.
	 * 
	 * @param scope
	 * @param unit the <code>ITranslationUnit</code> that should be searched.
	 * @param selectionStart the character offset of the selection.
	 * @param selectionLength the total characters in the selection.
	 * @return
	 */
	public ICElement[] findTypeHierarchyInput(Scope scope, ITranslationUnit unit, int selectionStart, int selectionLength);

	/**
	 * Returns a list of names that represent the declarations of the 
	 * given selection in the ITranslationUnit.
	 *  
	 * @param scope
	 * @param unit the <code>ITranslationUnit</code> that should be searched.
	 * @param selectedText used as a fallback for searching the index
	 * @param selectionStart the character offset of the selection.
	 * @param selectionLength the total characters in the selection.
	 * @param monitor
	 * @return
	 */
	public OpenDeclarationResult openDeclaration(Scope scope, ITranslationUnit unit, String selectedText, int selectionStart, int selectionLength, IProgressMonitor monitor);
	
	
	public IIndexIncludeValue[] findIncludesTo(Scope scope, IIndexFileLocation location, IProgressMonitor monitor);
	
	public IIndexIncludeValue[] findIncludedBy(Scope scope, IIndexFileLocation location, IProgressMonitor monitor);

	public boolean isIndexed(Scope scope, IIndexFileLocation location, IProgressMonitor monitor);
	
	public IIndexIncludeValue findInclude(Scope scope, IIndexFileLocation location, String name, int offset, IProgressMonitor monitor);
	
	/**
	 * Returns a model built using the content of the given translation unit
	 * @param unit
	 * @param monitor
	 * @return
	 */
	public ITranslationUnit getModel(ITranslationUnit unit, IProgressMonitor monitor);

	public String moveIndexFile(String scopeName, String newIndexLocation, IProgressMonitor monitor);
	/**
	 * 
	 * @return the reindex event type
	 */
	public EventType getReIndexEventType();

	public String computeHighlightPositions(ITranslationUnit targetUnit);
	
	public String computeInactiveHighlightPositions(ITranslationUnit targetUnit);
	
	public FoldingRegionsResult computeFoldingRegions(ITranslationUnit targetUnit, int docLength, boolean fPreprocessorBranchFoldingEnabled, boolean fStatementsFoldingEnabled);
	
	public TextEdit computeCodeFormatting(Scope scope, ITranslationUnit targetUnit, String source, RemoteDefaultCodeFormatterOptions preferences, int offset, int length, IProgressMonitor monitor);
	
}
