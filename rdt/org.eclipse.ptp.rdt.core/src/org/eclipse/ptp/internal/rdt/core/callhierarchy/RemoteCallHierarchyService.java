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
package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * @author crecoskie
 *
 */
public class RemoteCallHierarchyService extends AbstractRemoteService implements ICallHierarchyService {
	
	public RemoteCallHierarchyService(IConnectorService connectorService) {
		super(connectorService);
	}

	public RemoteCallHierarchyService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findCalledBy(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CalledByResult findCalledBy(Scope scope, ICElement callee, IProgressMonitor pm) throws CoreException, InterruptedException {
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkAllProjects(pm);
		
		ICElement target = ModelAdapter.adaptElement(null, callee, 0, true);
		return subsystem.getCallers(scope, target, pm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findCalls(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public CallsToResult findCalls(Scope scope, ICElement caller, IProgressMonitor pm) throws CoreException, InterruptedException {
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkAllProjects(pm);
		
		ICElement target = ModelAdapter.adaptElement(null, caller, 0, true);
		return subsystem.getCallees(scope, target, pm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement)
	 */
	public ICElement[] findDefinitions(Scope scope, ICElement input, IProgressMonitor pm) {
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkAllProjects(pm);
		
		try {
			ICElement target = ModelAdapter.adaptElement(null, input, 0, true);
			return subsystem.getCHDefinitions(scope, target, pm);
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
		return new ICElement[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICProject, org.eclipse.cdt.core.model.IWorkingCopy, int, int)
	 */
	public ICElement[] findDefinitions(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart,
			int selectionLength, IProgressMonitor pm) throws CoreException {
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkAllProjects(pm);
		
		ITranslationUnit unit = adaptWorkingCopy(workingCopy);
		return subsystem.getCHDefinitions(scope, unit, selectionStart, selectionLength, pm);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService#findDefinitions(org.eclipse.ptp.internal.rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement)
	 */
	public Map<String, ICElement[]> findOverriders(Scope scope, ICElement input, IProgressMonitor pm) {
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkAllProjects(pm);
		
		try {
			ICElement target = ModelAdapter.adaptElement(null, input, 0, true);
			return subsystem.findOverriders(scope, target, pm);
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
		return new HashMap<String, ICElement[]>();
	}

}
