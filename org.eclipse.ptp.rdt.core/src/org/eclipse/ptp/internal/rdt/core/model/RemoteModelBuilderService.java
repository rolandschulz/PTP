/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;

public class RemoteModelBuilderService extends AbstractRemoteService implements IModelBuilderService {

	public RemoteModelBuilderService(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}

	public ITranslationUnit getModel(IWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		ICIndexSubsystem subsystem = getSubSystem();
		ICProject cProject = workingCopy.getCProject();
		subsystem.checkProject(cProject.getProject(), monitor);
		
		ITranslationUnit unit = adaptWorkingCopy(workingCopy);

		return subsystem.getModel(unit, monitor);
	}
}
