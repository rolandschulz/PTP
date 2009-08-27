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
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.internal.core.indexer.ILanguageMapper;
import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CalledByResult;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallsToResult;
import org.eclipse.ptp.internal.rdt.core.contentassist.CompletionProposalComputer;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIndexIncludeValue;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IndexIncludeValue;
import org.eclipse.ptp.internal.rdt.core.index.DummyName;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.internal.rdt.core.model.CModelBuilder2;
import org.eclipse.ptp.internal.rdt.core.model.CProject;
import org.eclipse.ptp.internal.rdt.core.model.RemoteCProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.WorkingCopy;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraph;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.TypeHierarchyUtil;

/**
 * @author crecoskie
 *
 */
public class CDTMiner extends Miner {
	
	// index management
	public static final String C_INDEX_START = "C_INDEX_START"; //$NON-NLS-1$
	public static final String C_INDEX_REINDEX = "C_INDEX_REINDEX"; //$NON-NLS-1$
	public static final String C_INDEX_DELTA = "C_INDEX_DELTA"; //$NON-NLS-1$
	public static final String T_INDEX_STATUS_DESCRIPTOR = "Type.Index.Status"; //$NON-NLS-1$
	public static final String T_INDEX_STRING_DESCRIPTOR = "Type.Index.String"; //$NON-NLS-1$
	public static final String T_INDEX_FILENAME_DESCRIPTOR = "Type.Scope.Filename"; //$NON-NLS-1$
	public static final String T_INDEX_INT_DESCRIPTOR = "Type.Index.Int"; //$NON-NLS-1$
	public static final String T_INDEX_DELTA_CHANGED = "Type.Index.Delta.Changed"; //$NON-NLS-1$
	public static final String T_INDEX_DELTA_ADDED = "Type.Index.Delta.Added"; //$NON-NLS-1$
	public static final String T_INDEX_DELTA_REMOVED = "Type.Index.Delta.Removed"; //$NON-NLS-1$
	public static final String T_INDEX_SCANNER_INFO_PROVIDER = "Type.Index.ScannerInfoProvider"; //$NON-NLS-1$
	public static final String C_REMOVE_INDEX_FILE = "C_REMOVE_INDEX_FILE"; //$NON-NLS-1$
	public static final String C_MOVE_INDEX_FILE = "C_MOVE_INDEX_FILE"; //$NON-NLS-1$
	public static final String T_MOVE_INDEX_FILE_RESULT = "Type.Index.MoveResult";  //$NON-NLS-1$
	
	// indexer progress
	public static final String T_INDEXER_PROGRESS_INFO = "Type.Indexer.ProgressInfo"; //$NON-NLS-1$
		
	// scope management
	public static final String C_SCOPE_REGISTER = "C_SCOPE_REGISTER"; //$NON-NLS-1$
	public static final String C_SCOPE_UNREGISTER = "C_SCOPE_UNREGISTER"; //$NON-NLS-1$
	public static final String C_SCOPE_DELTA = "C_SCOPE_DELTA"; //$NON-NLS-1$
	public static final String C_SCOPE_COUNT_ELEMENTS = "C_SCOPE_COUNT_ELEMENTS"; //$NON-NLS-1$
	public static final String T_SCOPE_SCOPENAME_DESCRIPTOR = "Type.Scope.Scopename"; //$NON-NLS-1$
	public static final String T_SCOPE_CONFIG_LOCATION = "Type.Scope.ConfigLocation"; //$NON-NLS-1$
	
	// call hierarchy service
	public static final String C_CALL_HIERARCHY_GET_CALLS = "C_CALL_HIERARCHY_GET_CALLS"; //$NON-NLS-1$
	public static final String T_CALL_HIERARCHY_RESULT = "Type.CallHierarchy.Result"; //$NON-NLS-1$
	public static final String C_CALL_HIERARCHY_GET_CALLERS = "C_CALL_HIERARCHY_GET_CALLERS"; //$NON-NLS-1$
	public static final String C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_ELEMENT = "C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_ELEMENT"; //$NON-NLS-1$
	public static final String C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_WORKING_COPY = "C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_WORKING_COPY"; //$NON-NLS-1$

	// search service
	public static final String C_SEARCH_RUN_QUERY = "C_SEARCH_RUN_QUERY"; //$NON-NLS-1$
	public static final String T_SEARCH_RESULT = "Type.Search.Result"; //$NON-NLS-1$
	
	public static final String C_CONTENT_ASSIST_COMPUTE_PROPOSALS = "C_CONTENT_ASSIST_COMPUTE_PROPOSALS"; //$NON-NLS-1$
	
	// type hierarchy service
	public static final String C_TYPE_HIERARCHY_COMPUTE_TYPE_GRAPH = "C_TYPE_HIERARCHY_COMPUTE_TYPE_GRAPH"; //$NON-NLS-1$
	public static final String C_TYPE_HIERARCHY_FIND_INPUT1 = "C_TYPE_HIERARCHY_FIND_INPUT1"; //$NON-NLS-1$
	public static final String C_TYPE_HIERARCHY_FIND_INPUT2 = "C_TYPE_HIERARCHY_FIND_INPUT2"; //$NON-NLS-1$
	
	// navigation
	public static final String C_NAVIGATION_OPEN_DECLARATION = "C_NAVIGATION_OPEN_DECLARATION"; //$NON-NLS-1$
	public static final String T_NAVIGATION_RESULT = "Type.Navigation.Result"; //$NON-NLS-1$
	
	// includes
	public static final String C_INCLUDES_FIND_INCLUDES_TO = "C_INCLUDES_FIND_INCLUDES_TO"; //$NON-NLS-1$
	public static final String C_INCLUDES_FIND_INCLUDES_TO_RESULT = "C_INCLUDES_FIND_INCLUDES_TO_RESULT"; //$NON-NLS-1$
	public static final String C_INCLUDES_FIND_INCLUDED_BY = "C_INCLUDES_FIND_INCLUDED_BY"; //$NON-NLS-1$
	public static final String C_INCLUDES_FIND_INCLUDED_BY_RESULT = "C_INCLUDES_FIND_INCLUDED_BY_RESULT"; //$NON-NLS-1$
	public static final String C_INCLUDES_IS_INDEXED = "C_INCLUDES_IS_INDEXED"; //$NON-NLS-1$
	public static final String C_INCLUDES_IS_INDEXED_RESULT = "C_INCLUDES_IS_INDEXED_RESULT"; //$NON-NLS-1$
	public static final String C_INCLUDES_FIND_INCLUDE = "C_INCLUDES_FIND_INCLUDE"; //$NON-NLS-1$
	public static final String C_INCLUDES_FIND_INCLUDE_RESULT = "C_INCLUDES_FIND_INCLUDE_RESULT"; //$NON-NLS-1$
	
	public static String LINE_SEPARATOR;
	
	public static final String DELIMITER = ";;;"; //$NON-NLS-1$
	public static final String C_MODEL_BUILDER = "C_MODEL_BUILDER"; //$NON-NLS-1$;
	public static final String C_MODEL_RESULT= "C_MODEL_RESULT"; //$NON-NLS-1$;

	/* (non-Javadoc)
	 * @see org.eclipse.dstore.core.miners.Miner#getVersion()
	 */
	public String getVersion() {
		return "0.0.1";  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dstore.core.miners.Miner#handleCommand(org.eclipse.dstore.core.model.DataElement)
	 */
	public DataElement handleCommand(DataElement theCommand) {
		String name = getCommandName(theCommand);
		DataElement status = getCommandStatus(theCommand);
		//DataElement subject = getCommandArgument(theCommand, 0);
		
		if (name.equals(C_SCOPE_REGISTER)) {

			DataElement scopeName = getCommandArgument(theCommand, 1);
			DataElement configLocation = getCommandArgument(theCommand, 2);
			

			ArrayList<DataElement> fileNames = new ArrayList<DataElement>();

			for (int i = 3; i < theCommand.getNestedSize() - 1; i++) {
				DataElement fileName = getCommandArgument(theCommand, i);
				String type = fileName.getType();

				if (type.equals(T_INDEX_FILENAME_DESCRIPTOR)) {
					System.out.println("found a file\n"); //$NON-NLS-1$
					System.out.flush();

					fileNames.add(fileName);
				}

				else {
					System.out.println("bad datatype in call to RegisterScope()"); //$NON-NLS-1$
					System.out.flush();
				}
			}

			System.out.println("about to register scope\n"); //$NON-NLS-1$
			System.out.flush();
			handleRegisterScope(scopeName, configLocation.getName(), fileNames, status);

		}
		
		else if(name.equals(C_SCOPE_UNREGISTER))
		{
			DataElement scopeName = getCommandArgument(theCommand, 1);
			
			handleUnregisterScope(scopeName, status);
			
		}
		
		else if(name.equals(C_REMOVE_INDEX_FILE))
		{
			DataElement scopeName = getCommandArgument(theCommand, 1);
	
			handleIndexFileRemove(scopeName, status);
		}

		else if (name.equals(C_INDEX_START)) {
			try {
				String scopeName = getString(theCommand, 1);
				IRemoteIndexerInfoProvider provider = (IRemoteIndexerInfoProvider) Serializer.deserialize(getString(theCommand, 2));
	
				System.out.println("Indexing scope " + scopeName); //$NON-NLS-1$
	
				handleIndexStart(scopeName, provider, status);
				
				System.out.println("Indexing complete."); //$NON-NLS-1$
				System.out.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		else if(name.equals(C_INDEX_DELTA)) {
			String scopeName = getString(theCommand, 1);
			IRemoteIndexerInfoProvider provider;
			try {
				provider = (IRemoteIndexerInfoProvider) Serializer.deserialize(getString(theCommand, 2));
			} catch (IOException e) {
				e.printStackTrace();
				return status;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return status;
			}
			
			System.out.println("Indexing delta for scope " + scopeName); //$NON-NLS-1$
			System.out.flush();
			
			List<String> addedFiles = new LinkedList<String>();
			List<String> changedFiles = new LinkedList<String>();
			List<String> removedFiles = new LinkedList<String>();
			
			for (int i = 3; i < theCommand.getNestedSize() - 1; i++) {
				DataElement changeElement = getCommandArgument(theCommand, i);
				String type = changeElement.getType();

				if (type.equals(T_INDEX_DELTA_ADDED)) {
					System.out.println("added a file: " + changeElement.getName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					System.out.flush();

					addedFiles.add(changeElement.getName());
				}
				
				else if (type.equals(T_INDEX_DELTA_CHANGED)) {
					System.out.println("changed a file: " + changeElement.getName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					System.out.flush();

					changedFiles.add(changeElement.getName());
				}
				
				else if (type.equals(T_INDEX_DELTA_REMOVED)) {
					System.out.println("removed a file: " + changeElement.getName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					System.out.flush();

					removedFiles.add(changeElement.getName());
				}

				else {
					System.out.println("bad datatype in call to RegisterScope()"); //$NON-NLS-1$
					System.out.flush();
				}
			}
			
			handleIndexDelta(scopeName, addedFiles, changedFiles, removedFiles, provider, status);
			
			System.out.println("Indexing complete."); //$NON-NLS-1$
			System.out.flush();
			
		}
		
		else if(name.equals(C_INDEX_REINDEX))
		{
			try {
				String scopeName = getString(theCommand, 1);
				IRemoteIndexerInfoProvider provider = (IRemoteIndexerInfoProvider) Serializer.deserialize(getString(theCommand, 2));
				String newIndexLocation = getString(theCommand, 3);
	
				System.out.println("Re-indexing scope " + scopeName); //$NON-NLS-1$
	
				handleReindex(scopeName, newIndexLocation, provider, status);
				
				System.out.println("Reindexing complete."); //$NON-NLS-1$
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

		else if(name.equals(C_MOVE_INDEX_FILE)) {
			try {
				String scopeName = getString(theCommand, 1);
				String newIndexLocation = getString(theCommand, 2);
				
				handleIndexFileMove(scopeName, newIndexLocation, status);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else if (name.equals(C_CALL_HIERARCHY_GET_CALLERS)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				ICElement subject = (ICElement) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Getting callers..."); //$NON-NLS-1$
				System.out.flush();
				
				handleGetCallers(scopeName, subject, hostName, status);
				
				System.out.println("Finished getting callers."); //$NON-NLS-1$
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		else if (name.equals(C_CALL_HIERARCHY_GET_CALLS)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				ICElement subject = (ICElement) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Getting callees..."); //$NON-NLS-1$
				System.out.flush();
				
				handleGetCallees(scopeName, subject, hostName, status);
				
				System.out.println("Finished getting callees."); //$NON-NLS-1$
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		else if(name.equals(C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_ELEMENT)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				ICElement subject = (ICElement) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Getting definitions..."); //$NON-NLS-1$
				System.out.flush();
				
				handleGetDefinitions(scopeName, hostName, subject, status);
				
				System.out.println("Finished getting definitions."); //$NON-NLS-1$
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		else if(name.equals(C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_WORKING_COPY)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				ITranslationUnit unit = (ITranslationUnit) Serializer.deserialize(getString(theCommand, 3));
				int selectionStart = getInteger(theCommand, 4);
				int selectionLength = getInteger(theCommand, 5);
				
				System.out.println("Getting definitions..."); //$NON-NLS-1$
				System.out.flush();
				
				handleGetDefinitions(scopeName, hostName, unit, selectionStart, selectionLength, status);
				
				System.out.println("Finished getting definitions."); //$NON-NLS-1$
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		else if (name.equals(C_SEARCH_RUN_QUERY)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				RemoteSearchQuery query = (RemoteSearchQuery) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Finding matches based on a pattern..."); //$NON-NLS-1$
				System.out.flush();
				
				handleRunQuery(scopeName, query, hostName, status);
				
				System.out.println("Finished finding matches"); //$NON-NLS-1$
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		else if (name.equals(C_CONTENT_ASSIST_COMPUTE_PROPOSALS)) {
			try {
				String scopeName = getString(theCommand, 1);
				RemoteContentAssistInvocationContext context = (RemoteContentAssistInvocationContext) Serializer.deserialize(getString(theCommand, 2));
				ITranslationUnit unit = (ITranslationUnit) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Computing completions..."); //$NON-NLS-1$
				System.out.flush();
				
				handleComputeCompletionProposals(scopeName, context, unit, status);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		else if (name.equals(C_TYPE_HIERARCHY_COMPUTE_TYPE_GRAPH)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				ICElement input = (ICElement) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Computing type graph..."); //$NON-NLS-1$
				System.out.flush();
				
				handleComputeTypeGraph(scopeName, hostName, input, status);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if (name.equals(C_TYPE_HIERARCHY_FIND_INPUT1)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				ICElement input = (ICElement) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Finding type hierarchy input from element selection..."); //$NON-NLS-1$
				System.out.flush();
				
				String projectName = input.getCProject().getElementName();
				handleFindTypeHierarchyInput(scopeName, hostName, projectName, input, status);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if (name.equals(C_TYPE_HIERARCHY_FIND_INPUT2)) {
			try {
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				ITranslationUnit unit = (ITranslationUnit) Serializer.deserialize(getString(theCommand, 3));
				int selectionStart = getInteger(theCommand, 4);
				int selectionLength = getInteger(theCommand, 5);
				
				System.out.println("Finding type hierarchy input from text selection..."); //$NON-NLS-1$
				System.out.flush();
				
				String projectName = unit.getCProject().getElementName();
				handleFindTypeHierarchyInput(scopeName, hostName, unit, projectName, selectionStart, selectionLength, status);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if (name.equals(C_NAVIGATION_OPEN_DECLARATION)) {
			try {
				String scopeName = getString(theCommand, 1);
				ITranslationUnit unit = (ITranslationUnit) Serializer.deserialize(getString(theCommand, 2));
				String selectedText = getString(theCommand, 3);
				int selectionStart = getInteger(theCommand, 4);
				int selectionLength = getInteger(theCommand, 5);
				
				System.out.println("Open declaration..."); //$NON-NLS-1$
				System.out.flush();
				
				OpenDeclarationResult result = OpenDeclarationHandler.handleOpenDeclaration(scopeName, unit, selectedText, selectionStart, selectionLength);
				
				String resultString = Serializer.serialize(result);
				status.getDataStore().createObject(status, T_NAVIGATION_RESULT, resultString);
				statusDone(status);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
			
		}
		else if (name.equals(C_INCLUDES_FIND_INCLUDES_TO)) 
		{
			try 
			{
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				IIndexFileLocation location = (IIndexFileLocation) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Finding includes for location " + location + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();
				
				handleFindIncludesTo(scopeName, hostName, location, status);
				
				System.out.println("Finished finding includes"); //$NON-NLS-1$
				System.out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		else if (name.equals(C_INCLUDES_FIND_INCLUDED_BY)) 
		{
			try 
			{
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				IIndexFileLocation location = (IIndexFileLocation) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Finding included by for location " + location + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();
				
				handleFindIncludedBy(scopeName, hostName, location, status);
				
				System.out.println("Finished finding included by"); //$NON-NLS-1$
				System.out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		else if (name.equals(C_INCLUDES_FIND_INCLUDE)) 
		{
			try 
			{
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				IIndexFileLocation location = (IIndexFileLocation) Serializer.deserialize(getString(theCommand, 3));
				String includeName = getString(theCommand, 4);
				int offset = getInteger(theCommand, 5);
				
				System.out.println("Finding include for location " + location + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();
				
				handleFindInclude(scopeName, hostName, location, includeName, offset, status);
				
				System.out.println("Finished finding include"); //$NON-NLS-1$
				System.out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		else if (name.equals(C_INCLUDES_IS_INDEXED)) 
		{
			try 
			{
				String scopeName = getString(theCommand, 1);
				String hostName = getString(theCommand, 2);
				IIndexFileLocation location = (IIndexFileLocation) Serializer.deserialize(getString(theCommand, 3));
				
				System.out.println("Finding if location is indexed: " + location + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();
				
				handleIsIndexed(scopeName, hostName, location, status);

				System.out.println("Finished finding if location is indexed"); //$NON-NLS-1$
				System.out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		else if (name.equals(C_MODEL_BUILDER)) 
		{
			try 
			{
				ITranslationUnit workingCopy = (ITranslationUnit) Serializer.deserialize(getString(theCommand, 1));
				System.out.println("Model Builder: building working copy: " + workingCopy.getElementName() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();
				
				handleGetModel(workingCopy, status);

				System.out.println("Finished building model."); //$NON-NLS-1$
				System.out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}			
		}
		
		return status;
	}
	
	
	protected void handleIndexFileMove(String scopeName, String newIndexLocation, DataElement status) throws IOException {
		String actualLocation = RemoteIndexManager.getInstance().moveIndexFile(scopeName, newIndexLocation);
		status.getDataStore().createObject(status, T_MOVE_INDEX_FILE_RESULT, actualLocation);
		statusDone(status);
	}
	
	
	protected void handleIndexFileRemove(DataElement scopeName, DataElement status) {
		String scope = scopeName.getName();
		RemoteIndexManager.getInstance().removeIndexFile(scope);
		statusDone(status);
	}
	
	
	/**
	 * Builds a model using content from given translation unit
	 * @param unit
	 * @param status
	 */
	protected void handleGetModel(ITranslationUnit unit, DataElement status) {
		try {
			System.out.println("CDTMiner: get model started"); //$NON-NLS-1$
			System.out.flush();
			
			WorkingCopy workingCopy;
			
			if (unit instanceof WorkingCopy) {
				workingCopy = (WorkingCopy) unit;
			
				CModelBuilder2 builder = new CModelBuilder2(workingCopy, new NullProgressMonitor());
				
				IASTTranslationUnit ast = workingCopy.getAST();
				builder.parse(ast);
	
				// create the result object
				String resultString = Serializer.serialize(workingCopy);
				status.getDataStore().createObject(status, C_MODEL_RESULT, resultString);
			}
		} catch (IOException e) {
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			statusDone(status);
		}
	}

	protected void handleFindIncludesTo(String scopeName, String hostName, IIndexFileLocation location, DataElement status) 
	{
		try 
		{
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();

			try
			{
				IIndexIncludeValue[] includesToReturn;
				IIndexFile[] files= index.getFiles(location);
				
				if (files.length == 0)
				{
					System.out.println("No index files found"); //$NON-NLS-1$
					System.out.flush();

					includesToReturn = new IIndexIncludeValue[0];
				}
				else if (files.length == 1) 
				{
//					System.out.println("Found 1 index file"); //$NON-NLS-1$ 
//					System.out.flush();

					IIndexInclude[] includes = index.findIncludes(files[0]);
					
					if (includes == null)
						includesToReturn = new IIndexIncludeValue[0];
					else
					{
						includesToReturn = new IIndexIncludeValue[includes.length];
						for (int i = 0; i < includes.length; i ++)
						{
							includesToReturn[i] = new IndexIncludeValue(includes[i], getLocationConverter(hostName));
							
							System.out.println("IndexIncludeValue to return: " + includesToReturn[i]); //$NON-NLS-1$
						}
						
						System.out.flush();
					}
				}
				else 
				{
//					System.out.println("Found " + files.length + " index files"); //$NON-NLS-1$ //$NON-NLS-2$
//					System.out.flush();

					ArrayList<IIndexIncludeValue> list= new ArrayList<IIndexIncludeValue>();
					HashSet<IIndexFileLocation> handled= new HashSet<IIndexFileLocation>();
					
					for (int i = 0; i < files.length; i++) 
					{
						final IIndexInclude[] includes = index.findIncludes(files[i]);

//						System.out.println("Found " + includes.length + " includes"); //$NON-NLS-1$ //$NON-NLS-2$
//						System.out.flush();

						for (int j = 0; j < includes.length; j++) 
						{
							IIndexInclude indexInclude = includes[j];
							if (handled.add(indexInclude.getIncludesLocation())) 
							{
								IIndexIncludeValue value = new IndexIncludeValue(indexInclude, getLocationConverter(hostName));
								list.add(value);

								System.out.println("IndexIncludeValue to return: " + value); //$NON-NLS-1$
							}
							
							System.out.flush();
						}
					}
					
					includesToReturn = list.toArray(new IIndexIncludeValue[list.size()]);
				}
	
				// create the result object
				String resultString = Serializer.serialize(includesToReturn);
				status.getDataStore().createObject(status, C_INCLUDES_FIND_INCLUDES_TO_RESULT, resultString);
			}			         
			finally 
			{
				if (index != null)
					index.releaseReadLock();
				
				statusDone(status);

				System.out.println("Released read lock"); //$NON-NLS-1$
				System.out.flush();
				
			}
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		} 
		
	}
	
	protected void handleFindInclude(String scopeName, String hostName, IIndexFileLocation location, String name, int offset, DataElement status) 
	{
		try 
		{
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();

			try
			{
				IIndexIncludeValue includeToReturn = null;
				IIndexFile[] files= index.getFiles(location);
				
				if (files.length == 0)
				{
					System.out.println("No index files found"); //$NON-NLS-1$
					System.out.flush();
				}
				else 
				{
					IIndexInclude best= null;

					for (int j = 0; j < files.length; j ++) 
					{
						IIndexFile file = files[j];

						IIndexInclude[] includes= index.findIncludes(file);
						int bestDiff= Integer.MAX_VALUE;

						for (int i = 0; i < includes.length; i++) 
						{
							IIndexInclude candidate = includes[i];
							int diff = Math.abs(candidate.getNameOffset()- offset);
							if (diff > bestDiff) 
							{
								break;
							}
							if (candidate.getName().endsWith(name)) 
							{
								bestDiff= diff;
								best= candidate;
							}
						}
						
						if (best != null)
						{
							includeToReturn = new IndexIncludeValue(best);
							break;
						}
					}
					
				}
	
				// create the result object
				String resultString = Serializer.serialize(includeToReturn);
				status.getDataStore().createObject(status, C_INCLUDES_FIND_INCLUDE_RESULT, resultString);
			}			         
			finally 
			{
				if (index != null)
					index.releaseReadLock();
				
				statusDone(status);

				System.out.println("Released read lock"); //$NON-NLS-1$
				System.out.flush();
				
			}
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		} 
		
	}

	protected void handleFindIncludedBy(String scopeName, String hostName, IIndexFileLocation location, DataElement status) 
	{
		try 
		{
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();

			try
			{
				IIndexIncludeValue[] includesToReturn;
				IIndexFile[] files= index.getFiles(location);
				
				if (files.length == 0)
				{
					System.out.println("No index files found"); //$NON-NLS-1$
					System.out.flush();

					includesToReturn = new IIndexIncludeValue[0];
				}
				else if (files.length == 1) 
				{
//					System.out.println("Found 1 index file"); //$NON-NLS-1$ 
//					System.out.flush();

					IIndexInclude[] includes = index.findIncludedBy(files[0]);
					
					if (includes == null)
						includesToReturn = new IIndexIncludeValue[0];
					else
					{
						includesToReturn = new IIndexIncludeValue[includes.length];
						for (int i = 0; i < includes.length; i ++)
						{
							includesToReturn[i] = new IndexIncludeValue(includes[i], getLocationConverter(hostName));
							
							System.out.println("IndexIncludeValue to return: " + includesToReturn[i]); //$NON-NLS-1$
						}
						
						System.out.flush();
					}
				}
				else 
				{
//					System.out.println("Found " + files.length + " index files"); //$NON-NLS-1$ //$NON-NLS-2$
//					System.out.flush();

					ArrayList<IIndexIncludeValue> list= new ArrayList<IIndexIncludeValue>();
					HashSet<IIndexFileLocation> handled= new HashSet<IIndexFileLocation>();
					
					for (int i = 0; i < files.length; i++) 
					{
						final IIndexInclude[] includes = index.findIncludedBy(files[i]);

//						System.out.println("Found " + includes.length + " includes"); //$NON-NLS-1$ //$NON-NLS-2$
//						System.out.flush();

						for (int j = 0; j < includes.length; j++) 
						{
							IIndexInclude indexInclude = includes[j];
							if (handled.add(indexInclude.getIncludedByLocation())) 
							{
								IIndexIncludeValue value = new IndexIncludeValue(indexInclude, getLocationConverter(hostName));
								list.add(value);

								System.out.println("IndexIncludeValue to return: " + value); //$NON-NLS-1$
							}
							
							System.out.flush();
						}
					}
					
					includesToReturn = list.toArray(new IIndexIncludeValue[list.size()]);
				}
	
				// create the result object
				String resultString = Serializer.serialize(includesToReturn);
				status.getDataStore().createObject(status, C_INCLUDES_FIND_INCLUDED_BY_RESULT, resultString);
			}			         
			finally 
			{
				if (index != null)
					index.releaseReadLock();
				
				statusDone(status);

				System.out.println("Released read lock"); //$NON-NLS-1$
				System.out.flush();
				
			}
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		} 
		
	}
	
	protected void handleIsIndexed(String scopeName, String hostName, IIndexFileLocation location, DataElement status) 
	{
		try 
		{
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();

			try
			{
				IIndexFile[] files= index.getFiles(location);
				
				System.out.println("Found " + files.length + " index files"); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();

				// create the result object
				String resultString = Serializer.serialize(new Boolean(files.length > 0));
				status.getDataStore().createObject(status, C_INCLUDES_IS_INDEXED_RESULT, resultString);
			}			         
			finally 
			{
				if (index != null)
					index.releaseReadLock();
				
				statusDone(status);

				System.out.println("Released read lock"); //$NON-NLS-1$
				System.out.flush();
				
			}
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		} 
		
	}

	protected void handleFindTypeHierarchyInput(String scopeName, String hostName, ITranslationUnit unit, String projectName, int selectionStart, int selectionLength, DataElement status) {
		try {
			System.out.println("File: " + unit.getLocationURI()); //$NON-NLS-1$
			System.out.println("Element: " + unit.getElementName()); //$NON-NLS-1$
			System.out.flush();
			
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();
			try {
				IIndexLocationConverter converter = getLocationConverter(hostName);
				ICElement[] result = null;
				ICProject project = new CProject(projectName);
				IASTName name= IndexQueries.getSelectedName(index, unit, selectionStart, selectionLength);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (TypeHierarchyUtil.isValidInput(binding)) {
						ICElement member= null;
						if (!TypeHierarchyUtil.isValidTypeInput(binding)) {
							member= TypeHierarchyUtil.findDeclaration(project, index, name, binding, converter, new RemoteCProjectFactory());
							name= null;
							binding= TypeHierarchyUtil.findTypeBinding(binding);
						}
						if (TypeHierarchyUtil.isValidTypeInput(binding)) {
							ICElement input= TypeHierarchyUtil.findDefinition(project, index, name, binding, converter, new RemoteCProjectFactory());
							if (input != null) {
								result = new ICElement[] {input, member};
							}
						}
					}
				}

				if (result != null) {
					System.out.println("Found input."); //$NON-NLS-1$
					System.out.println("Details: " + result.toString()); //$NON-NLS-1$
					System.out.flush();
				}
				
				String resultString = Serializer.serialize(result);
				status.getDataStore().createObject(status, T_SEARCH_RESULT, resultString);
			} finally {
				index.releaseReadLock();
				statusDone(status);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	protected void handleFindTypeHierarchyInput(String scopeName, String hostName, String projectName, ICElement input, DataElement status) {
		try {
			System.out.println("File: " + input.getLocationURI()); //$NON-NLS-1$
			System.out.println("Element: " + input.getElementName()); //$NON-NLS-1$
			System.out.flush();
			
			IIndexLocationConverter converter = getLocationConverter(hostName);
			
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();
			try {
				ICElement[] result = null;
				ICProject project = new CProject(projectName);
				ICElement member = input;
				IIndexName name= IndexQueries.remoteElementToName(index, member);
				if (name != null) {
					member= IndexQueries.getCElementForName(project, index, name, converter, new RemoteCProjectFactory());
					IBinding binding= index.findBinding(name);
					binding= TypeHierarchyUtil.findTypeBinding(binding);
					if (TypeHierarchyUtil.isValidTypeInput(binding)) {
						ICElement definition= TypeHierarchyUtil.findDefinition(project, index, null, binding, converter, new RemoteCProjectFactory());
						if (input != null) {
							result = new ICElement[] {definition, member};
						}
					}
				}

				if (result != null) {
					System.out.println("Found input."); //$NON-NLS-1$
					System.out.flush();
				}
				
				String resultString = Serializer.serialize(result);
				status.getDataStore().createObject(status, T_SEARCH_RESULT, resultString);
			} finally {
				index.releaseReadLock();
				statusDone(status);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected void handleComputeTypeGraph(String scopeName, String hostName, ICElement input, DataElement status) {
		try {
			System.out.println("File: " + input.getLocationURI()); //$NON-NLS-1$
			System.out.println("Element: " + input.getElementName()); //$NON-NLS-1$
			System.out.flush();
			
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();
			try {
				IProgressMonitor monitor = new NullProgressMonitor();
				THGraph graph = new THGraph();
				graph.setLocationConverter(getLocationConverter(hostName));
				final RemoteCProjectFactory projectFactory = new RemoteCProjectFactory();
				graph.defineInputNode(index, input, projectFactory);
				graph.addSuperClasses(index, monitor, projectFactory);
				graph.addSubClasses(index, monitor, projectFactory);

				System.out.println("Found " + graph.getLeaveNodes().size() + " leaf node(s)."); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();

				String resultString = Serializer.serialize(graph);
				status.getDataStore().createObject(status, T_SEARCH_RESULT, resultString);
			} finally {
				index.releaseReadLock();
				statusDone(status);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected void handleComputeCompletionProposals(String scopeName, RemoteContentAssistInvocationContext context, ITranslationUnit unit, DataElement status) {
		try {
			System.out.println("File: " + unit.getLocationURI()); //$NON-NLS-1$
			System.out.println("Offset: " + context.getInvocationOffset()); //$NON-NLS-1$
			System.out.flush();
			
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();
			try {
				int style = ITranslationUnit.AST_SKIP_INDEXED_HEADERS | ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;
				int offset = context.getInvocationOffset();
				IASTCompletionNode completionNode = unit.getCompletionNode(index, style, offset);
				context.setCompletionNode(completionNode);
				
				List<Proposal> proposals;
				if (completionNode == null) {
					proposals = Collections.emptyList();
				} else {
					// If the completion node can provide us with a
					// (usually more accurate) prefix, use that.
					String prefix = completionNode.getPrefix();
					if (prefix == null) {
						prefix = context.computeIdentifierPrefix().toString();
					}
					CompletionProposalComputer computer = new CompletionProposalComputer();
					proposals = computer.computeCompletionProposals(context, completionNode, prefix);
				}

				System.out.println("Found " + proposals.size() + " proposal(s)."); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();

				String resultString = Serializer.serialize(proposals);
				status.getDataStore().createObject(status, T_SEARCH_RESULT, resultString);
			} finally {
				index.releaseReadLock();
				statusDone(status);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected void handleRunQuery(String scopeName, RemoteSearchQuery query, String hostName, DataElement status) {
		try {
			
			IIndex index;
			ICProject[] projects = query.getProjects();
			System.out.println("Searching for: \"" + query + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			
			if (projects == null)
				System.out.println("scope: " + scopeName); //$NON-NLS-1$
			else
				System.out.println("scope: " + query.getScopeDescription()); //$NON-NLS-1$
			
			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();
						
			if (projects == null) {
				index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			}
			else{			
				index = RemoteIndexManager.getInstance().getIndexForProjects(projects);
			}

			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();
			try {
				query.runWithIndex(index, getLocationConverter(hostName), getProgressMonitor());
				List<RemoteSearchMatch> matches = query.getMatches();

				System.out.println("Found " + matches.size() + " match(es)"); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.flush();
				
				String resultString = Serializer.serialize(matches);
				status.getDataStore().createObject(status, T_SEARCH_RESULT, resultString);
			} finally {
				index.releaseReadLock();
				statusDone(status);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private IIndexLocationConverter getLocationConverter(String hostName) {
		return new SimpleLocationConverter("rse", hostName); //$NON-NLS-1$
	}

	private boolean getBoolean(DataElement theCommand, int i) {
		DataElement element = getCommandArgument(theCommand, i);
		return Boolean.parseBoolean(element.getName());
	}

	private String getString(DataElement command, int index) {
		DataElement element = getCommandArgument(command, index);
		return element.getName();
	}
	
	private int getInteger(DataElement command, int index) {
		DataElement element = getCommandArgument(command, index);
		return Integer.parseInt(element.getName());
	}
	
	protected void handleIndexDelta(String scopeName, List<String> addedFiles,
			List<String> changedFiles, List<String> removedFiles, IRemoteIndexerInfoProvider provider, DataElement status) {
		try {
//			statusWorking(status);
			
			StandaloneFastIndexer indexer = RemoteIndexManager.getInstance().getIndexerForScope(scopeName, provider);
			ScopeManager scopeManager = ScopeManager.getInstance();
			
			// update the scope if required
			for(String file : addedFiles) {
				scopeManager.addFileToScope(scopeName, file);
			}
			
			for(String file : changedFiles) {
				scopeManager.addFileToScope(scopeName, file);
			}
			
			for(String file : removedFiles) {
				scopeManager.removeFileFromScope(scopeName, file);
			}
			
			try {
				indexer.setTraceStatistics(true);
				indexer.setShowProblems(true);
				indexer.setShowActivity(true);
				indexer.handleDelta(addedFiles, changedFiles, removedFiles, getProgressMonitor(indexer, status));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		finally {
			statusDone(status);
		}
		
	}
	
	private IProgressMonitor getProgressMonitor(StandaloneFastIndexer indexer, DataElement status) {
		return new RemoteIndexProgressMonitor(indexer, status);
	}

	private IProgressMonitor getProgressMonitor() {
		return new StdoutProgressMonitor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dstore.core.model.ISchemaExtender#extendSchema(org.eclipse.dstore.core.model.DataElement)
	 */
	public void extendSchema(DataElement schemaRoot) {
		
		System.out.println("Extended schema from CDTMiner"); //$NON-NLS-1$
		
		// scope management
		createCommandDescriptor(schemaRoot, "Register Scope", C_SCOPE_REGISTER, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Unregister Scope", C_SCOPE_UNREGISTER, false); //$NON-NLS-1$
		
		// index management
		createCommandDescriptor(schemaRoot, "Start Index", C_INDEX_START, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Reindex", C_INDEX_REINDEX, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Index Delta", C_INDEX_DELTA, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Remove Index File", C_REMOVE_INDEX_FILE, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Move Index File", C_MOVE_INDEX_FILE, false); //$NON-NLS-1$
		
		// call hierarchy
		createCommandDescriptor(schemaRoot, "Get Callers", C_CALL_HIERARCHY_GET_CALLERS, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Get Calls", C_CALL_HIERARCHY_GET_CALLS, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Get definitions from element", C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_ELEMENT, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Get definitions from working copy", C_CALL_HIERARCHY_GET_DEFINITIONS_FROM_WORKING_COPY, false); //$NON-NLS-1$
		
		// search
		createCommandDescriptor(schemaRoot, "Run query", C_SEARCH_RUN_QUERY, false); //$NON-NLS-1$
		
		// content assist
		createCommandDescriptor(schemaRoot, "Compute completion proposals", C_CONTENT_ASSIST_COMPUTE_PROPOSALS, false); //$NON-NLS-1$
		
		// type hierarchy
		createCommandDescriptor(schemaRoot, "Compute type graph", C_TYPE_HIERARCHY_COMPUTE_TYPE_GRAPH, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Find input from element", C_TYPE_HIERARCHY_FIND_INPUT1, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Find input from text selection", C_TYPE_HIERARCHY_FIND_INPUT2, false); //$NON-NLS-1$
		
		// navigation
		createCommandDescriptor(schemaRoot, "Open declaration", C_NAVIGATION_OPEN_DECLARATION, false); //$NON-NLS-1$
		
		//includes
		createCommandDescriptor(schemaRoot, "Find includes to", C_INCLUDES_FIND_INCLUDES_TO, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Find included by", C_INCLUDES_FIND_INCLUDED_BY, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Is indexed", C_INCLUDES_IS_INDEXED, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Find include", C_INCLUDES_FIND_INCLUDE, false); //$NON-NLS-1$
		
		//get model
		createCommandDescriptor(schemaRoot, "Get model", C_MODEL_BUILDER, false); //$NON-NLS-1$
		
		_dataStore.refresh(schemaRoot);
	}
	
	protected void handleGetDefinitions(String scopeName, String hostName, ICElement subject, DataElement status) {
		try {
			System.out.println("Getting definitions for subject " + subject.getElementName()); //$NON-NLS-1$
			System.out.println("scope: " + scopeName); //$NON-NLS-1$
			System.out.println("path: " + subject.getLocationURI()); //$NON-NLS-1$

			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();

			// search the index for the name
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);

			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();
			try {
				ICElement[] definitions = null;
				if (subject instanceof ISourceReference) {
					ISourceReference input = (ISourceReference) subject;
					ITranslationUnit tu = input.getTranslationUnit();
					
					if (needToFindDefinition(subject)) {
						IBinding binding= IndexQueries.elementToBinding(index, subject);
						if (binding != null) {
							ICElement[] result= IndexQueries.findAllDefinitions(index, binding, getLocationConverter(hostName), subject.getCProject(), new RemoteCProjectFactory());
							if (result.length > 0) {
								definitions = result;
							}
						}
					}
					if (definitions == null) {
						IIndexName name= IndexQueries.remoteElementToName(index, subject);
						if (name != null) {
							ICElement handle= IndexQueries.getCElementForName(tu, index, name, new RemoteCProjectFactory());
							definitions = new ICElement[] {handle};
						}
					}
				}
				// create the result object
				String resultString = Serializer.serialize(definitions);
				status.getDataStore().createObject(status, T_CALL_HIERARCHY_RESULT, resultString);
			}
			finally {
				index.releaseReadLock();
				statusDone(status);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void handleGetDefinitions(String scopeName, String hostName, ITranslationUnit workingCopy, int selectionStart, int selectionLength, DataElement status) {
		try {
			System.out.println("Getting definitions for subject " + workingCopy.getElementName()); //$NON-NLS-1$
			System.out.println("scope: " + scopeName); //$NON-NLS-1$
			System.out.println("path: " + workingCopy.getLocationURI()); //$NON-NLS-1$
			System.out.println("offset: " + selectionStart); //$NON-NLS-1$
			System.out.println("length: " + selectionLength); //$NON-NLS-1$

			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();

			// search the index for the name
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);

			System.out.println("Acquiring read lock"); //$NON-NLS-1$
			System.out.flush();
			index.acquireReadLock();
			try {
				ICElement[] definitions = null;
				ICProject project = workingCopy.getCProject();

				IASTName name= IndexQueries.getSelectedName(index, workingCopy, selectionStart, selectionLength);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (isRelevantForCallHierarchy(binding)) {
						if (name.isDefinition()) {
							IIndexLocationConverter converter = getLocationConverter(hostName);
							ICElement elem= IndexQueries.getCElementForName(project, index, name, converter, new RemoteCProjectFactory());
							if (elem != null) {
								definitions = new ICElement[]{elem};
							}
						}
						else {
							ICElement[] elems= IndexQueries.findAllDefinitions(index, binding, getLocationConverter(hostName), project, new RemoteCProjectFactory());
							if (elems.length == 0) {
								ICElement elem= null;
								if (name.isDeclaration()) {
									elem= IndexQueries.getCElementForName(project, index, name, getLocationConverter(hostName), new RemoteCProjectFactory());
								}
								else {
									elem= IndexQueries.findAnyDeclaration(index, project, binding, getLocationConverter(hostName), new RemoteCProjectFactory());
								}
								if (elem != null) {
									elems= new ICElement[]{elem};
								}
							}
							definitions = elems;
						}
					}
				}

				// create the result object
				String resultString = Serializer.serialize(definitions);
				status.getDataStore().createObject(status, T_CALL_HIERARCHY_RESULT, resultString);
			}
			finally {
				index.releaseReadLock();
				statusDone(status);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static boolean needToFindDefinition(ICElement elem) {
		switch (elem.getElementType()) {
		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			return true;
		}
		return false;
	}
	
	private IIndexFileLocation createLocation(String hostName, IIndexFileLocation location) throws URISyntaxException {
		URI uri = location.getURI();
		URI newURI = new URI("rse", hostName, uri.getPath(), null, null); //$NON-NLS-1$
		return new RemoteIndexFileLocation(null, newURI);
	}

	protected void handleUnregisterScope(DataElement scopeName, DataElement status) {
		String scope = scopeName.getName();
		
		ScopeManager.getInstance().removeScope(scope);
		statusDone(status);
		
	}
	

	protected void handleGetCallers(String scopeName, ICElement subject, String hostName, DataElement status) {
		String subjectName = subject.getElementName();
		System.out.println("Getting callers for subject " + subjectName); //$NON-NLS-1$
		System.out.println("scope: " + scopeName); //$NON-NLS-1$
		System.out.println("path: " + subject.getLocationURI()); //$NON-NLS-1$
		System.out.flush();
		
		try {
			CalledByResult result = new CalledByResult();

			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();

			IIndexLocationConverter converter = getLocationConverter(hostName);
			
			// search the index for the name
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			try {
				System.out.println("Acquiring read lock"); //$NON-NLS-1$
				System.out.flush();
				index.acquireReadLock();

				IBinding callee= IndexQueries.elementToBinding(index, subject);
				ICProject project= subject.getCProject();
				if (subject != null) {
					IIndexName[] names= index.findReferences(callee);
					for (int i = 0; i < names.length; i++) {
						IIndexName rname = names[i];
						IIndexName caller= rname.getEnclosingDefinition();
						if (caller != null) {
							ICElement elem= IndexQueries.getCElementForName(project, index, caller, converter, new RemoteCProjectFactory());
							if (elem != null) {
								IIndexFileLocation indexLocation = createLocation(hostName, rname.getFile().getLocation());
								IIndexName name = new DummyName(rname, rname.getFileLocation(), indexLocation);
								result.add(elem, name);
							} 
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			finally {
				index.releaseReadLock();
			}

			// create the result object
			String resultString = Serializer.serialize(result);
			status.getDataStore().createObject(status, T_CALL_HIERARCHY_RESULT, resultString);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		finally {
			statusDone(status);
		}
	}

	public static boolean isRelevantForCallHierarchy(IBinding binding) {
		if (binding instanceof ICExternalBinding ||
				binding instanceof IEnumerator ||
				binding instanceof IFunction ||
				binding instanceof IVariable) {
			return true;
		}
		return false;
	}
	
	protected void handleGetCallees(String scopeName, ICElement subject, String hostName, DataElement status) {
		String subjectName = subject.getElementName();
		System.out.println("Getting callees for subject " + subjectName); //$NON-NLS-1$
		System.out.println("scope: " + scopeName); //$NON-NLS-1$
		System.out.println("path: " + subject.getLocationURI()); //$NON-NLS-1$
		
		try {
			CallsToResult result = new CallsToResult();
			
			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();

			IIndexLocationConverter converter = getLocationConverter(hostName);
			
			// search the index for the name
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);
			try {
				System.out.println("Acquiring read lock"); //$NON-NLS-1$
				System.out.flush();
				index.acquireReadLock();

				IIndexName callerName= IndexQueries.remoteElementToName(index, subject);
				if (callerName != null) {
					IIndexName[] refs= callerName.getEnclosedNames();
					for (int i = 0; i < refs.length; i++) {
						IIndexName name = refs[i];
						IBinding binding= index.findBinding(name);
						if (isRelevantForCallHierarchy(binding)) {
							ICElement[] defs = IndexQueries.findRepresentative(index, binding, converter, subject.getCProject(), new RemoteCProjectFactory());
							if (defs != null && defs.length > 0) {
								IIndexFileLocation indexLocation = createLocation(hostName, name.getFile().getLocation());
								IIndexName reference = new DummyName(name, name.getFileLocation(), indexLocation);
								result.add(defs, reference);
							}
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return;
			}

			finally {
				index.releaseReadLock();
				statusDone(status);
			}

			// create the result object
			String resultString = Serializer.serialize(result);
			status.getDataStore().createObject(status, T_CALL_HIERARCHY_RESULT, resultString);
		}

		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected void handleIndexStart(String scopeName, IRemoteIndexerInfoProvider provider, DataElement status) {
		try {
			StandaloneFastIndexer indexer = RemoteIndexManager.getInstance().getIndexerForScope(scopeName, provider);
			Set<String> sources = ScopeManager.getInstance().getFilesForScope(scopeName);
			List<String> sourcesList = new LinkedList<String>(sources);

			try {
				indexer.rebuild(sourcesList, getProgressMonitor());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		finally {
			statusDone(status);
		}
	}
	
	protected void handleReindex(String scopeName, String newIndexLocation, IRemoteIndexerInfoProvider provider, DataElement status) {
		RemoteIndexManager indexManager = RemoteIndexManager.getInstance();
		indexManager.setIndexFileLocation(scopeName, newIndexLocation);
		StandaloneFastIndexer indexer = indexManager.getIndexerForScope(scopeName, provider);
		Set<String> sources = ScopeManager.getInstance().getFilesForScope(scopeName);
		
		List<String> sourcesList = new LinkedList<String>(sources);
	
		try { 
			indexer.setTraceStatistics(true);
			indexer.setShowProblems(true);
			indexer.setShowActivity(true);
			indexer.rebuild(sourcesList, getProgressMonitor());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		statusDone(status);
	}

	/**
	 * @param scopeName DataElement containing the string name of the scope
	 * @param fileNames a list of DataElements which each store the string pathname of a file in the scope
	 */
	protected void handleRegisterScope(DataElement scopeName, String configLocation, List<DataElement> fileNames, DataElement status) {
		String scope = scopeName.getName();
		
		Iterator<DataElement> iterator = fileNames.iterator();
		
		Set<String> files = new LinkedHashSet<String>();
		
		System.out.println("Added scope " + scope + " at " + configLocation + " Files:\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		System.out.flush();
		
		while(iterator.hasNext())
		{
			DataElement element = iterator.next();
			String fileName = element.getName();
			
			files.add(fileName);
			
			System.out.println(fileName + "\n"); //$NON-NLS-1$
			System.out.flush();
		}
		
		ScopeManager.getInstance().addScope(scope, files);
		RemoteIndexManager.getInstance().setIndexFileLocation(scope, configLocation);
		
		statusDone(status);
		
	}
	
	public static DataElement statusWorking(DataElement status) {
		status.setAttribute(DE.A_NAME, DataStoreResources.model_working);
		status.getDataStore().refresh(status);
		return status;
	}

	public static DataElement statusWorked(DataElement status, int numWorked) {
		status.setAttribute(DE.A_NAME, (new Integer(numWorked).toString()));
		status.getDataStore().refresh(status);
		return status;
	}
	
	/**
	 * Complete status.
	 */
	public static DataElement statusDone(DataElement status) {
		status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
		status.getDataStore().refresh(status);
		return status;
	}

	/**
	 * Cancel status.
	 */
	public static DataElement statusCancelled(DataElement status) {
		status.setAttribute(DE.A_NAME, "cancelled"); //$NON-NLS-1$
		status.getDataStore().refresh(status);
		return status;
	}

}
