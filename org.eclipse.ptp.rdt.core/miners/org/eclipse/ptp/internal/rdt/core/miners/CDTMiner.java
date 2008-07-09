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
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.indexer.ILanguageMapper;
import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.model.BindingAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Path;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;

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
		
	// scope management
	public static final String C_SCOPE_REGISTER = "C_SCOPE_REGISTER"; //$NON-NLS-1$
	public static final String C_SCOPE_UNREGISTER = "C_SCOPE_UNREGISTER"; //$NON-NLS-1$
	public static final String C_SCOPE_DELTA = "C_SCOPE_DELTA"; //$NON-NLS-1$
	public static final String C_SCOPE_COUNT_ELEMENTS = "C_SCOPE_COUNT_ELEMENTS"; //$NON-NLS-1$
	public static final String T_SCOPE_SCOPENAME_DESCRIPTOR = "Type.Scope.Scopename"; //$NON-NLS-1$
	
	// call hierarchy service
	public static final String C_CALL_HIERARCHY_GET_CALLS = "C_CALL_HIERARCHY_GET_CALLS"; //$NON-NLS-1$
	public static final String T_CALL_HIERARCHY_RESULT = "Type.CallHierarchy.Result"; //$NON-NLS-1$
	public static final String C_CALL_HIERARCHY_GET_CALLERS = "C_CALL_HIERARCHY_GET_CALLERS"; //$NON-NLS-1$
	public static final String C_CALL_HIERARCHY_GET_DEFINITIONS = "C_CALL_HIERARCHY_GET_DEFINITIONS"; //$NON-NLS-1$

	// search service
	public static final String C_SEARCH_RUN_QUERY = "C_SEARCH_RUN_QUERY"; //$NON-NLS-1$
	public static final String T_SEARCH_RESULT = "Type.Search.Result"; //$NON-NLS-1$
	
	public static String LINE_SEPARATOR;
	
	public static final String DELIMITER = ";;;"; //$NON-NLS-1$

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

			ArrayList<DataElement> fileNames = new ArrayList<DataElement>();

			for (int i = 2; i < theCommand.getNestedSize() - 1; i++) {
				DataElement fileName = getCommandArgument(theCommand, i);
				String type = fileName.getType();

				if (type.equals(T_INDEX_FILENAME_DESCRIPTOR)) {
					System.out.println("found a file\n"); //$NON-NLS-1$
					System.out.flush();

					fileNames.add(fileName);
				}

				else {
					System.out
							.println("bad datatype in call to RegisterScope()"); //$NON-NLS-1$
					System.out.flush();
				}
			}

			System.out.println("about to register scope\n"); //$NON-NLS-1$
			System.out.flush();
			handleRegisterScope(scopeName, fileNames, status);

		}
		
		else if(name.equals(C_SCOPE_UNREGISTER))
		{
			DataElement scopeName = getCommandArgument(theCommand, 1);
			
			handleUnregisterScope(scopeName, status);
			
		}

		else if (name.equals(C_INDEX_START)) {
			String scopeName = getString(theCommand, 1);

			System.out.println("Indexing scope " + scopeName); //$NON-NLS-1$

			handleIndexStart(scopeName, status);
			
			
			System.out.println("Indexing complete."); //$NON-NLS-1$
			System.out.flush();

		}
		
		else if(name.equals(C_INDEX_DELTA)) {
			String scopeName = getString(theCommand, 1);
			
			System.out.println("Indexing delta for scope " + scopeName); //$NON-NLS-1$
			System.out.flush();
			
			List<String> addedFiles = new LinkedList<String>();
			List<String> changedFiles = new LinkedList<String>();
			List<String> removedFiles = new LinkedList<String>();
			
			for (int i = 2; i < theCommand.getNestedSize() - 1; i++) {
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
					System.out
							.println("bad datatype in call to RegisterScope()"); //$NON-NLS-1$
					System.out.flush();
				}
			}
			
			handleIndexDelta(scopeName, addedFiles, changedFiles, removedFiles, status);
			
			System.out.println("Indexing complete."); //$NON-NLS-1$
			System.out.flush();
			
		}
		
		else if(name.equals(C_INDEX_REINDEX))
		{
			String scopeName = getString(theCommand, 1);

			System.out.println("Re-indexing scope " + scopeName); //$NON-NLS-1$

			handleReindex(scopeName, status);
			
			System.out.println("Reindexing complete."); //$NON-NLS-1$
			System.out.flush();

		}

		else if (name.equals(C_CALL_HIERARCHY_GET_CALLERS)) {
			String scopeName = getString(theCommand, 1);
			String subjectName = getString(theCommand, 2);
			String filePath = getString(theCommand, 3);
			int selectionStart = getInteger(theCommand, 4);
			int selectionLength = getInteger(theCommand, 5);
			String projectName = getString(theCommand, 6);
			String hostName = getString(theCommand, 7);
			
			System.out.println("Getting callers..."); //$NON-NLS-1$
			System.out.flush();
			
			handleGetCallers(scopeName, projectName, subjectName, filePath, selectionStart, selectionLength, hostName, status);
			
			System.out.println("Finished getting callers."); //$NON-NLS-1$
			System.out.flush();
		}

		else if (name.equals(C_CALL_HIERARCHY_GET_CALLS)) {
			String scopeName = getString(theCommand, 1);
			String subjectName = getString(theCommand, 2);
			String filePath = getString(theCommand, 3);
			int selectionStart = getInteger(theCommand, 4);
			int selectionLength = getInteger(theCommand, 5);
			String projectName = getString(theCommand, 6);
			String hostName = getString(theCommand, 7);
			
			System.out.println("Getting callees..."); //$NON-NLS-1$
			System.out.flush();
			
			handleGetCallees(scopeName, projectName, subjectName, filePath, selectionStart, selectionLength, hostName, status);
			
			System.out.println("Finished getting callees."); //$NON-NLS-1$
			System.out.flush();
		}
		
		else if(name.equals(C_CALL_HIERARCHY_GET_DEFINITIONS)) {
			String scopeName = getString(theCommand, 1);
			String subjectName = getString(theCommand, 2);
			String filePath = getString(theCommand, 3);
			int selectionStart = getInteger(theCommand, 4);
			int selectionLength = getInteger(theCommand, 5);
			String projectName = getString(theCommand, 6);
			String hostName = getString(theCommand, 7);
			
			System.out.println("Getting definitions..."); //$NON-NLS-1$
			System.out.flush();
			
			handleGetDefinitions(scopeName, projectName, subjectName, filePath, selectionStart, selectionLength, hostName, status);
			
			System.out.println("Finished getting definitions."); //$NON-NLS-1$
			System.out.flush();
		}
		
		else if (name.equals(C_SEARCH_RUN_QUERY)) {
			try {
				String scopeName = getString(theCommand, 1);
				RemoteSearchQuery query = (RemoteSearchQuery) Serializer.deserialize(getString(theCommand, 2));
				String hostName = getString(theCommand, 3);
				
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

		return status;
	}

	protected void handleRunQuery(String scopeName, RemoteSearchQuery query, String hostName, DataElement status) {
		try {
			System.out.println("Searching for: \"" + query + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("scope: " + scopeName); //$NON-NLS-1$
			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);

			try {
				System.out.println("Acquiring read lock"); //$NON-NLS-1$
				System.out.flush();
				index.acquireReadLock();
				
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
			List<String> changedFiles, List<String> removedFiles, DataElement status) {
		try {

//			statusWorking(status);
			
			StandaloneFastIndexer indexer = RemoteIndexManager.getInstance()
					.getIndexerForScope(scopeName);
			
			ScopeManager scopeManager = ScopeManager.getInstance();
			
			
			// update the scope if required
			Iterator<String> iterator = addedFiles.iterator();
			
			while(iterator.hasNext()) {
				scopeManager.addFileToScope(scopeName, iterator.next());
			}
			
			iterator = removedFiles.iterator();
			
			while(iterator.hasNext()) {
				scopeManager.removeFileFromScope(scopeName, iterator.next());
			}
			
			try {
				indexer.setTraceStatistics(true);
				indexer.setShowProblems(true);
				indexer.setShowActivity(true);
				indexer.handleDelta(addedFiles, changedFiles, removedFiles, getProgressMonitor());
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
		
		// call hierarchy
		createCommandDescriptor(schemaRoot, "Get Callers", C_CALL_HIERARCHY_GET_CALLERS, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Get Calls", C_CALL_HIERARCHY_GET_CALLS, false); //$NON-NLS-1$
		createCommandDescriptor(schemaRoot, "Get definitions", C_CALL_HIERARCHY_GET_DEFINITIONS, false); //$NON-NLS-1$
		
		// search
		createCommandDescriptor(schemaRoot, "Run query", C_SEARCH_RUN_QUERY, false); //$NON-NLS-1$
		
		_dataStore.refresh(schemaRoot);
	}
	
	protected void handleGetDefinitions(String scopeName, String projectName, String subjectName,
			String filePath, int selectionStart, int selectionLength,
			String hostName, DataElement status) {
		try {

			List<String> results = new LinkedList<String>();

			System.out.println("Getting definitions for subject " + subjectName); //$NON-NLS-1$
			System.out.println("scope: " + scopeName); //$NON-NLS-1$
			System.out.println("path: " + filePath); //$NON-NLS-1$
			System.out.println("selection: " + selectionStart + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ selectionLength);

			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();

			// search the index for the name
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);

			try {
				System.out.println("Acquiring read lock"); //$NON-NLS-1$
				System.out.flush();
				index.acquireReadLock();

				// figure out what we've got selected

				// first, get the AST for the file
				System.out.println("Getting AST"); //$NON-NLS-1$
				System.out.flush();
				
				IASTTranslationUnit unit = getASTTranslationUnit(filePath, index);

				System.out.println("Finding name in AST"); //$NON-NLS-1$
				System.out.flush();

				FindNameForSelectionVisitor finder = new FindNameForSelectionVisitor(
						filePath, selectionStart, selectionLength);
				unit.accept(finder);

				IASTName astName = finder.getSelectedName();

				System.out.println("Got selected name of " //$NON-NLS-1$
						+ new String(astName.toCharArray()));
				System.out.flush();

				System.out.println("Getting binding"); //$NON-NLS-1$
				System.out.flush();
				IBinding binding = astName.resolveBinding();

				System.out.println("binding is of type " //$NON-NLS-1$
						+ binding.getClass().getName());
				System.out.flush();

				// get the definitions out of the binding
				IIndexName[] indexNames = index.findDefinitions(binding);
				
				for (int k = 0; k < indexNames.length; k++) {
					String fileName = indexNames[k].getFile().getLocation()
							.getFullPath();

					System.out.println("File: " + fileName); //$NON-NLS-1$
					System.out.flush();
					
					int offset = indexNames[k].getNodeOffset();
					
					int length = indexNames[k].getNodeLength();
					
					// create the result
					IIndexFileLocation location = createLocation(hostName, indexNames[k].getFile().getLocation());
					ICElement cElement = BindingAdapter.adaptBinding(getFakeTranslationUnitForFile(projectName, location), binding, offset, length, true);
					
					if(cElement == null) {
						System.out.println("ICElement is null"); //$NON-NLS-1$
						System.out.flush();
					}
					
					if (cElement instanceof Serializable) {
						results.add(Serializer.serialize(cElement));
					}
					
					else {
						System.out.println("ICElement is not serializable"); //$NON-NLS-1$
						System.out.flush();
					}
				}
				
				Iterator<String> iterator = results.iterator();

				while (iterator.hasNext()) {

					String resultString = (String) iterator.next();

					// create the result object
					status.getDataStore().createObject(status, T_CALL_HIERARCHY_RESULT, resultString);
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

		}

		catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

	private IIndexFileLocation createLocation(String hostName, IIndexFileLocation location) throws URISyntaxException {
		return new IndexFileLocation(new URI("rse://" + hostName + location.getFullPath()), null); //$NON-NLS-1$
	}

	protected void handleUnregisterScope(DataElement scopeName, DataElement status) {
		String scope = scopeName.getName();
		
		ScopeManager.getInstance().removeScope(scope);
		statusDone(status);
		
	}

	protected void handleGetCallers(String scopeName, String projectName, String subjectName,
			String filePath, int selectionStart, int selectionLength,
			String hostName, DataElement status) {

		try {
			List<String> results = new LinkedList<String>();
			System.out.println("Getting callers for subject " + subjectName); //$NON-NLS-1$
			System.out.println("scope: " + scopeName); //$NON-NLS-1$
			System.out.println("path: " + filePath); //$NON-NLS-1$
			System.out.println("selection: " + selectionStart + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ selectionLength);

			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();


				// search the index for the name
				IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);

				try {
					System.out.println("Acquiring read lock"); //$NON-NLS-1$
					System.out.flush();
					index.acquireReadLock();

					// figure out what we've got selected

					// first, get the AST for the file
					System.out.println("Getting AST"); //$NON-NLS-1$
					System.out.flush();
					IASTTranslationUnit unit = getASTTranslationUnit(filePath,
							index);

					System.out.println("Finding name in AST"); //$NON-NLS-1$
					System.out.flush();

					FindNameForSelectionVisitor finder = new FindNameForSelectionVisitor(
							filePath, selectionStart, selectionLength);
					unit.accept(finder);

					IASTName astName = finder.getSelectedName();

					System.out.println("Got selected name of " //$NON-NLS-1$
							+ new String(astName.toCharArray()));
					System.out.flush();

					System.out.println("Getting binding"); //$NON-NLS-1$
					System.out.flush();
					IBinding binding = astName.resolveBinding();

					System.out.println("binding is of type " //$NON-NLS-1$
							+ binding.getClass().getName());
					System.out.flush();

					if (binding instanceof IFunction
							|| binding instanceof ICPPFunction) {
						System.out.println("Binding is a function"); //$NON-NLS-1$
						System.out.flush();
						// look at the callers
						IIndexName[] names = null;
						try {
							System.out
									.println("Getting references to function"); //$NON-NLS-1$
							System.out.flush();
							names = index.findReferences(binding);
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						for (int i = 0; i < names.length; i++) {
							IIndexName rname = names[i];
							IIndexName caller;

							try {
								caller = rname.getEnclosingDefinition();
								IBinding callerBinding = index
										.findBinding(caller);

								System.out.println("Found a caller: " //$NON-NLS-1$
										+ new String(caller.toCharArray()));
								System.out.flush();

								System.out.println("Getting file location..."); //$NON-NLS-1$
								System.out.flush();

								String fileName = caller.getFile()
										.getLocation().getFullPath();

								System.out.println("File: " + fileName); //$NON-NLS-1$
								System.out.flush();

								int offset = caller.getNodeOffset();

								int length = caller.getNodeLength();

								IIndexFileLocation location = createLocation(hostName, caller.getFile().getLocation());
								// create the result
								ICElement cElement = BindingAdapter
										.adaptBinding(
												getFakeTranslationUnitForFile(projectName, location),
												callerBinding, offset, length,
												true);
								System.out.println(cElement);
								if (cElement instanceof Serializable) {

									results.add(Serializer.serialize(cElement));

								}

							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

					else {
						// error
						System.out
								.println("Attempt to do call hierarchy on non-function.  Name: " //$NON-NLS-1$
										+ subjectName);

					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					return;
				}

				finally {

					index.releaseReadLock();
				}


			Iterator<String> resultsIterator = results.iterator();

			while (resultsIterator.hasNext()) {

				String resultString = resultsIterator.next();

				// create the result object
				status.getDataStore().createObject(status,
						T_CALL_HIERARCHY_RESULT, resultString);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		finally {
			statusDone(status);
		}
	}
	
	
	private ITranslationUnit getFakeTranslationUnitForFile(String projectName,
			IIndexFileLocation location) {
		TranslationUnit tu = new TranslationUnit(null, location.getFullPath(), projectName);
		
		tu.setPath(new Path(location.getFullPath()));
		tu.setLocationURI(location.getURI());
		
		return tu;
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
	
	protected void handleGetCallees(String scopeName, String projectName, String subjectName,
			String filePath, int selectionStart, int selectionLength,
			String hostName, DataElement status) {
		try {

			List<String> results = new LinkedList<String>();

			System.out.println("Getting callees for subject " + subjectName); //$NON-NLS-1$
			System.out.println("scope: " + scopeName); //$NON-NLS-1$
			System.out.println("path: " + filePath); //$NON-NLS-1$
			System.out.println("selection: " + selectionStart + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ selectionLength);

			System.out.println("Getting index"); //$NON-NLS-1$
			System.out.flush();

			// search the index for the name
			IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scopeName);

			try {
				System.out.println("Acquiring read lock"); //$NON-NLS-1$
				System.out.flush();
				index.acquireReadLock();

				// figure out what we've got selected

				// first, get the AST for the file
				System.out.println("Getting AST"); //$NON-NLS-1$
				System.out.flush();
				IASTTranslationUnit unit = getASTTranslationUnit(filePath, index);

				System.out.println("Finding name in AST"); //$NON-NLS-1$
				System.out.flush();

				FindNameForSelectionVisitor finder = new FindNameForSelectionVisitor(
						filePath, selectionStart, selectionLength);
				unit.accept(finder);

				IASTName astName = finder.getSelectedName();

				System.out.println("Got selected name of " //$NON-NLS-1$
						+ new String(astName.toCharArray()));
				System.out.flush();

				System.out.println("Getting binding"); //$NON-NLS-1$
				System.out.flush();
				IBinding binding = astName.resolveBinding();

				System.out.println("binding is of type " //$NON-NLS-1$
						+ binding.getClass().getName());
				System.out.flush();

				if (binding instanceof IFunction
						|| binding instanceof ICPPFunction) {
					System.out.println("Binding is a function"); //$NON-NLS-1$
					System.out.flush();

					IIndexName[] names = index.findDefinitions(binding);

					// for each definition, find the callees and add them to the
					// results
					for (int k = 0; k < names.length; k++) {
						IIndexName[] refs = names[k].getEnclosedNames();
						for (int i = 0; i < refs.length; i++) {
							IBinding calleeBinding = index.findBinding(refs[i]);
							if (isRelevantForCallHierarchy(calleeBinding)) {
								IIndexName[] defs = index
										.findDefinitions(calleeBinding);
								
								for (int j = 0; j < defs.length; j++) {
										IIndexName callee = defs[j];
									
										System.out.println("Found a callee: " //$NON-NLS-1$
												+ new String(callee
														.toCharArray()));
										System.out.flush();

										System.out
												.println("Getting file location..."); //$NON-NLS-1$
										System.out.flush();

										String fileName = callee.getFile()
												.getLocation().getFullPath();

										System.out.println("File: " + fileName); //$NON-NLS-1$
										System.out.flush();


										int offset = callee.getNodeOffset();
										
										int length = callee.getNodeLength();
										
										IIndexFileLocation location = createLocation(hostName, callee.getFile().getLocation());

										// create the result
										ICElement cElement = BindingAdapter.adaptBinding(getFakeTranslationUnitForFile(projectName, location), calleeBinding, offset, length, true);
										
										if (cElement instanceof Serializable) {
											results.add(Serializer.serialize(cElement));
										}
								}
							}
						}
					}
				}
					
					


				else {
					// error
					System.out
							.println("Attempt to do call hierarchy on non-function.  Name: " //$NON-NLS-1$
									+ subjectName);

				}

				Iterator<String> iterator = results.iterator();

				while (iterator.hasNext()) {

					String resultString = iterator.next();

					// create the result object
					status.getDataStore().createObject(
							status, T_CALL_HIERARCHY_RESULT, resultString);
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

		}

		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	protected void handleIndexStart(String scopeName, DataElement status) {

		try {

			StandaloneFastIndexer indexer = RemoteIndexManager.getInstance()
					.getIndexerForScope(scopeName);

			Set<String> sources = ScopeManager.getInstance()
					.getFilesForScope(scopeName);
			
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
	
	protected void handleReindex(String scopeName, DataElement status) {
		StandaloneFastIndexer indexer = RemoteIndexManager.getInstance().getIndexerForScope(scopeName);
		
		Set<String> sources = ScopeManager.getInstance().getFilesForScope(scopeName);
		
		List<String> sourcesList = new LinkedList<String>(sources);
	
		try {
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
	protected void handleRegisterScope(DataElement scopeName, List<DataElement> fileNames, DataElement status) {
		String scope = scopeName.getName();
		
		Iterator<DataElement> iterator = fileNames.iterator();
		
		Set<String> files = new LinkedHashSet<String>();
		
		System.out.println("Added scope " + scope + "Files:\n"); //$NON-NLS-1$ //$NON-NLS-2$
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


	 public static ParserLanguage getLanguage(String path) {

		if (path.endsWith(".cpp") || path.endsWith(".hpp") || path.endsWith(".C") || path.endsWith(".H")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return ParserLanguage.CPP;
		} else if (path.endsWith(".c") || path.endsWith(".h")) { //$NON-NLS-1$ //$NON-NLS-2$
			return ParserLanguage.C;
		} else {
			return ParserLanguage.CPP;
		}

	}
	 
	public IASTTranslationUnit getASTTranslationUnit(String filePath, IIndex index) {
		ILanguageMapper languageMapper = new RemoteLanguageMapper();
		
		ILanguage language = languageMapper.getLanguage(filePath);
		
		// TODO: handle real scanner info
		IASTTranslationUnit tu = null;
		try {
			tu = language.getASTTranslationUnit(new CodeReader(filePath), new ScannerInfo(),
					StandaloneSavedCodeReaderFactory.getInstance(), index, getParserOptions(filePath), new NullLogService());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tu;
	}
	
	public int getParserOptions(String filePath) {
		if (filePath.endsWith(".cpp") || filePath.endsWith(".C") || filePath.endsWith(".c")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return ILanguage.OPTION_IS_SOURCE_UNIT;
		}
//			else if (filePath.endsWith(".hpp")  || filePath.endsWith(".h") || filePath.endsWith(".H")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			return 0;
//		} 
			
		return 0;
	}
}
