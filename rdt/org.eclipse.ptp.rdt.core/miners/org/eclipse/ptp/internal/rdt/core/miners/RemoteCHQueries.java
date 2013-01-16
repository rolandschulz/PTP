/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 




package org.eclipse.ptp.internal.rdt.core.miners;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CalledByResult;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallsToResult;
import org.eclipse.ptp.internal.rdt.core.index.DummyName;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.internal.rdt.core.model.ICProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.IIndexLocationConverterFactory;
import org.eclipse.ptp.internal.rdt.core.model.RemoteCProjectFactory;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

public class RemoteCHQueries {
	public static final String LOG_TAG = "RemoteCHQueries"; //$NON-NLS-1$
	
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CHQueries
	 * Version: 1.20
	 */
	//copied from private static void findCalledBy(ICElement callee, int linkageID, IIndex index, CalledByResult result) 
	public static CalledByResult findCalledBy(ICElement callee, String path, IIndex project_index, IIndex serach_scope_index, String scheme, String hostName, CalledByResult result, DataStore _dataStore, IIndexLocationConverterFactory converter, DataElement status) 
	throws CoreException, URISyntaxException, InterruptedException {
		
		final ICProject project = callee.getCProject();
		IBinding calleeBinding = null;
		
		UniversalServerUtilities.logDebugMessage(LOG_TAG, "Acquiring read lock for project_index", _dataStore); //$NON-NLS-1$
		project_index.acquireReadLock();
		try {
			
			calleeBinding=IndexQueries.elementToBinding(project_index, callee, path);
		} finally {
			UniversalServerUtilities.logDebugMessage(LOG_TAG, "Releasing read lock for project_index", _dataStore); //$NON-NLS-1$
			project_index.releaseReadLock();
		}
		
		if (calleeBinding != null) {
			
			UniversalServerUtilities.logDebugMessage(LOG_TAG, "Acquiring read lock for serach_scope_index", _dataStore); //$NON-NLS-1$
			serach_scope_index.acquireReadLock();
			try{
				findCalledBy1(serach_scope_index, calleeBinding, true, project, scheme, hostName, result, _dataStore, converter, status);
				if (calleeBinding instanceof ICPPMethod) {
					IBinding[] overriddenBindings= ClassTypeHelper.findOverridden((ICPPMethod) calleeBinding, null);
					for (IBinding overriddenBinding : overriddenBindings) {
						
						if(CDTMiner.isCancelled(status)) {
							break;
						}
						
						findCalledBy1(serach_scope_index, overriddenBinding, false, project, scheme, hostName, result, _dataStore, converter, status);
					}
				}
			} finally{
				UniversalServerUtilities.logDebugMessage(LOG_TAG, "Releasing read lock for serach_scope_index", _dataStore); //$NON-NLS-1$
				serach_scope_index.releaseReadLock();
			}
		}
		return result;
	}
	
	private static void findCalledBy1(IIndex index, IBinding callee, boolean includeOrdinaryCalls,
			ICProject project, String scheme, String hostName, CalledByResult result, DataStore _dataStore, IIndexLocationConverterFactory converter, DataElement status) throws CoreException, URISyntaxException {
		
		findCalledBy2(index, callee, includeOrdinaryCalls, project, scheme, hostName, result, _dataStore, converter, status);
		List<? extends IBinding> specializations = IndexQueries.findSpecializations(callee);
		for (IBinding spec : specializations) {
			
			if(CDTMiner.isCancelled(status)) {
				break;
			}
			
			findCalledBy2(index, spec, includeOrdinaryCalls, project, scheme, hostName, result, _dataStore, converter, status);
		}
	}

	
	
	private static void findCalledBy2(IIndex index, IBinding callee, boolean includeOrdinaryCalls, ICProject project, String scheme, String hostName, CalledByResult result,  DataStore _dataStore, IIndexLocationConverterFactory converter, DataElement status) 
		throws CoreException, URISyntaxException {
		IIndexName[] names= index.findNames(callee, IIndex.FIND_REFERENCES | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		for (IIndexName rname : names) {
			
			if(CDTMiner.isCancelled(status)) {
				break;
			}
			
			if (includeOrdinaryCalls || rname.couldBePolymorphicMethodCall()) {
				IIndexName caller= rname.getEnclosingDefinition();
				if (caller != null) {
					ICElement elem= IndexQueries.getCElementForName(project, index, caller, converter, new RemoteCProjectFactory());
					if (elem != null) {
						IIndexFileLocation indexLocation = createLocation(scheme, hostName, rname.getFile().getLocation(), _dataStore);
						IIndexName reference = new DummyName(rname, rname.getFileLocation(), indexLocation);
						result.add(elem, reference);
					} 
				}
			}
		}
	}
	
	/**
	 * Searches for all calls that are made within a given range.
	 * @param status 
	 * @throws InterruptedException 
	 * @throws URISyntaxException 
	 */
	public static CallsToResult findCalls(ICElement caller, String path, IIndex project_index, IIndex workspace_scope_index, String scheme, String hostName, DataStore _dataStore, IIndexLocationConverterFactory converter, DataElement status) 
			throws CoreException, InterruptedException, URISyntaxException {
		
		CallsToResult result = new CallsToResult();
		IIndexName callerName = null;
		
		UniversalServerUtilities.logDebugMessage(LOG_TAG, "Acquiring read lock for project_index", _dataStore); //$NON-NLS-1$
		project_index.acquireReadLock();
		try {
			
			callerName= IndexQueries.remoteElementToName(project_index, caller, path);
			
		}

		finally {
			UniversalServerUtilities.logDebugMessage(CDTMiner.LOG_TAG, "Releasing read lock for project_index", _dataStore); //$NON-NLS-1$
			project_index.releaseReadLock();
			
		}
		if (callerName != null) {
			UniversalServerUtilities.logDebugMessage(LOG_TAG, "Acquiring read lock for workspace_scope_index", _dataStore); //$NON-NLS-1$
			workspace_scope_index.acquireReadLock();
			try{
				IIndexName[] refs= callerName.getEnclosedNames();
				final ICProject project = caller.getCProject();
				ICProjectFactory projectFactory = new RemoteCProjectFactory();
				for (IIndexName name : refs) {
					
					if (CDTMiner.isCancelled(status)) {
						break;
					}
					
					IBinding binding= workspace_scope_index.findBinding(name);
					if (isRelevantForCallHierarchy(binding)) {
						for(;;) {
							
							if (CDTMiner.isCancelled(status)) {
								break;
							}
							
							ICElement[] defs= null;
							if (binding instanceof ICPPMethod && name.couldBePolymorphicMethodCall()) {
								defs = findOverriders(workspace_scope_index, (ICPPMethod) binding, converter, project, projectFactory);
							}
							if (defs == null) {
								defs= IndexQueries.findRepresentative(workspace_scope_index, binding, converter, project, projectFactory);
							}
							if (defs != null && defs.length > 0) {
								IIndexFileLocation indexLocation = createLocation(scheme, hostName, name.getFile().getLocation(), _dataStore);
								IIndexName reference = new DummyName(name, name.getFileLocation(), indexLocation);
								
								UniversalServerUtilities.logDebugMessage(LOG_TAG, "Found a callee: " + defs[0].getElementName() + "\n", _dataStore); //$NON-NLS-1$ //$NON-NLS-2$
								result.add(defs, reference);
							} else if (binding instanceof ICPPSpecialization) {
								binding= ((ICPPSpecialization) binding).getSpecializedBinding();
								if (binding != null)
									continue;
							}
							break;
						}
					}
				}
			}finally {
				UniversalServerUtilities.logDebugMessage(LOG_TAG, "Releasing read lock for workspace_scope_index", _dataStore); //$NON-NLS-1$
				workspace_scope_index.releaseReadLock();
			}
		}
		return result;
	}

	/**
	 * Searches for overriders of method and converts them to ICElement, returns null, if there are none.
	 */
	private static ICElement[] findOverriders(IIndex index, ICPPMethod binding, IIndexLocationConverterFactory converter, ICProject project, ICProjectFactory projectFactory)	throws CoreException {
		IBinding[] virtualOverriders= ClassTypeHelper.findOverriders(index, binding);
		if (virtualOverriders.length > 0) {
			ArrayList<ICElement> list= new ArrayList<ICElement>();
			list.addAll(Arrays.asList(IndexQueries.findRepresentative(index, binding, converter, project, projectFactory)));
			for (IBinding overrider : virtualOverriders) {
				list.addAll(Arrays.asList(IndexQueries.findRepresentative(index, overrider, converter, project, projectFactory)));
			}
			return list.toArray(new ICElement[list.size()]);
		}
		return null;
	}


	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI
	 * Version: 1.25
	 */
	public static boolean isRelevantForCallHierarchy(IBinding binding) {
		if (binding instanceof ICExternalBinding ||
				binding instanceof IEnumerator ||
				binding instanceof IFunction ||
				binding instanceof IVariable) {
			return true;
		}
		return false;
	}
	
	private static IIndexFileLocation createLocation(String scheme, String hostName, IIndexFileLocation location, DataStore _dataStore) throws URISyntaxException {
		URI uri = location.getURI();
		String path = uri.getPath();
		URI newURI = null;
		
		if(scheme == null || scheme.equals("")) { //$NON-NLS-1$
			scheme = ScopeManager.getInstance().getSchemeForFile(path);
		}
		
		// create the URI
		newURI = URICreatorManager.getDefault(_dataStore).createURI(scheme, hostName, path);
		
		return new RemoteIndexFileLocation(null, newURI);
	}
	
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CHContentProvider
	 * Version: 1.21
	 */
	public static Map<String, ICElement[]> handleGetOverriders(IIndex project_index, ICElement subject, String path, DataStore _dataStore, IIndexLocationConverterFactory converter) throws InterruptedException, CoreException {
		
		
		

		UniversalServerUtilities.logDebugMessage(LOG_TAG, "Acquiring read lock", _dataStore); //$NON-NLS-1$
		
		project_index.acquireReadLock();
		Map<String, ICElement[]> result = new HashMap<String, ICElement[]>();
		try {
			
			if (subject instanceof IMethod) {
				IIndexName methodName= IndexQueries.remoteElementToName(project_index, subject, path);
				if (methodName != null) {
					IBinding methodBinding= project_index.findBinding(methodName);
					if (methodBinding instanceof ICPPMethod) {
						final ICProject project = subject.getCProject();
						ICProjectFactory projectFactory = new RemoteCProjectFactory();
						ICElement[] defs= findOverriders(project_index, (ICPPMethod) methodBinding, converter, project, projectFactory);
						if (defs != null && defs.length > 0) {
							result.put(methodBinding.getLinkage().getLinkageID()+"", defs); //$NON-NLS-1$
						}
					}
				}
			}
		}
		finally {
			project_index.releaseReadLock();
		}
		
		
		return result;
		
	}
	
	
	

}
