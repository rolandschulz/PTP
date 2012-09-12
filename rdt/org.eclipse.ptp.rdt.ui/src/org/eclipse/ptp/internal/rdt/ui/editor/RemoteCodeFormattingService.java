/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteDefaultCodeFormatterOptions;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Vivian Kong
 *
 */
public class RemoteCodeFormattingService extends AbstractRemoteService implements IRemoteCodeFormattingService {
	public RemoteCodeFormattingService(IConnectorService connectorService) {
		super(connectorService);
	}

	public RemoteCodeFormattingService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}

	public TextEdit computeCodeFormatting(ITranslationUnit tu, String source, RemoteDefaultCodeFormatterOptions preferences, int offset, int length, IProgressMonitor monitor) throws CoreException  {
		ICIndexSubsystem subsystem = getSubSystem();
		ICProject cProject = tu.getCProject();
		subsystem.checkProject(cProject.getProject(), monitor);
		
		ITranslationUnit unit = adaptWorkingCopy(tu.getWorkingCopy());
		
		Scope projectScope = new Scope(cProject.getProject());

		return subsystem.computeCodeFormatting(projectScope, unit, source, preferences, offset, length, monitor);
	}
}
