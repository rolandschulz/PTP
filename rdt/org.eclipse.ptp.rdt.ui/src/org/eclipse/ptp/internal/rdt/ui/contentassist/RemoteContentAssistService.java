/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.contentassist;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProviderFactory;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.internal.rdt.core.model.WorkingCopy;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * A service for computing content assist completions on a remote host.
 */
public class RemoteContentAssistService extends AbstractRemoteService implements IContentAssistService {

	public RemoteContentAssistService(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}

	public List<Proposal> computeCompletionProposals(Scope scope, ContentAssistInvocationContext context, ITranslationUnit unit) {
		if (!(context instanceof CContentAssistInvocationContext)) {
			return Collections.emptyList();
		}
		CContentAssistInvocationContext cContext = (CContentAssistInvocationContext) context;
		
		ITranslationUnit targetUnit;
		// If dirty, we need to send the working copy too
		if (cContext.getEditor().isDirty()) {
			String contents = cContext.getDocument().get();
			targetUnit = new WorkingCopy(null, unit, contents);
		} else {
			try {
				targetUnit = ModelAdapter.adaptElement(null, unit, 0, true);
			} catch (CModelException e) {
				RDTLog.logError(e);
				return Collections.emptyList();
			}
		}
		
		RemoteContentAssistInvocationContext remoteContext = ContentAssistUtil.adaptContext(cContext);
		ICIndexSubsystem subsystem = getSubSystem();
		
		// TODO: This can potentially take a while.  But we need
		//       to trigger scope initialization in case it hasn't
		//       been done for the project.
		IProject project = unit.getCProject().getProject();
		IProgressMonitor monitor = new NullProgressMonitor();
		subsystem.checkProject(project, monitor );
		
		if(targetUnit instanceof TranslationUnit) {
			IScannerInfo scannerInfo = RemoteIndexerInfoProviderFactory.getScannerInfo(unit.getResource());
			((TranslationUnit)targetUnit).setASTContext(scannerInfo);
		}
		
		return subsystem.computeCompletionProposals(scope, remoteContext , targetUnit);
	}

}
