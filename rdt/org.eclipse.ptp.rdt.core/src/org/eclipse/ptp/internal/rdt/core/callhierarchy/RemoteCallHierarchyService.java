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
package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.index.DummyName;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * @author crecoskie
 *
 */
public class RemoteCallHierarchyService extends AbstractRemoteService implements ICallHierarchyService {
	
	public RemoteCallHierarchyService(IHost host, IConnectorService connectorService) {
		fHost = host;
		fConnectorService = connectorService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findCalledBy(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CalledByResult findCalledBy(Scope scope, ICElement callee, IProgressMonitor pm) throws CoreException,
			InterruptedException {
		ICIndexSubsystem subsystem = getSubSystem();
		List<String> serializedCallers = subsystem.getCallers(scope, callee, pm);
		Iterator<String> iterator = serializedCallers.iterator();
		
		CalledByResult result = new CalledByResult();
		
		while(iterator.hasNext()) {
			Object obj = null;
			try {
				obj = Serializer.deserialize(iterator.next());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(obj instanceof ICElement) {
				ICElement element = (ICElement) obj;
				ISourceReference callerSrcRef = null;
				
				if(element instanceof ISourceReference) {
					callerSrcRef = (ISourceReference) element;
				}
				
				DummyName indexName = new DummyName(callerSrcRef);
				
				result.add(element, indexName);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findCalls(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CallsToResult findCalls(Scope scope, ICElement caller, IProgressMonitor pm) throws CoreException, InterruptedException {
		ICIndexSubsystem subsystem = getSubSystem();
		List<String> serializedCallees = subsystem.getCallees(scope, caller, pm);
		Iterator<String> iterator = serializedCallees.iterator();
		
		CallsToResult result = new CallsToResult();
		
		while(iterator.hasNext()) {
			Object obj = null;
			try {
				obj = Serializer.deserialize(iterator.next());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(obj instanceof ICElement) {
				ICElement element = (ICElement) obj;
				ISourceReference callerSrcRef = null;
				
				if(element instanceof ISourceReference) {
					callerSrcRef = (ISourceReference) element;
				}
				
				DummyName indexName = new DummyName(callerSrcRef);
				
				ICElement[] elementArray = {element};
				
				result.add(elementArray, indexName);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement)
	 */
	public ICElement[] findDefinitions(Scope scope, ICElement input, IProgressMonitor pm) {
		ICIndexSubsystem subsystem = getSubSystem();
		List<String> serializedCallers = subsystem.getCHDefinitions(scope, input, pm);
		Iterator<String> iterator = serializedCallers.iterator();
		
		List<ICElement> results = new LinkedList<ICElement>();
		
		while(iterator.hasNext()) {
			Object obj = null;
			try {
				obj = Serializer.deserialize(iterator.next());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(obj instanceof ICElement) {
				ICElement element = (ICElement) obj;
								
				results.add(element);
			}
		}
		return results.toArray(new ICElement[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICProject, org.eclipse.cdt.core.model.IWorkingCopy, int, int)
	 */
	public ICElement[] findDefinitions(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart,
			int selectionLength, IProgressMonitor pm) throws CoreException {
		ICIndexSubsystem subsystem = getSubSystem();
		ICElement selectedElement = workingCopy.getElementAtOffset(selectionStart);
		List<String> serializedCallers = subsystem.getCHDefinitions(scope, selectedElement, pm);
		Iterator<String> iterator = serializedCallers.iterator();
		
		List<ICElement> results = new LinkedList<ICElement>();
		
		while(iterator.hasNext()) {
			Object obj = null;
			try {
				obj = Serializer.deserialize(iterator.next());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(obj instanceof ICElement) {
				ICElement element = (ICElement) obj;
								
				results.add(element);
			}
		}
		return results.toArray(new ICElement[0]);
	}

}
