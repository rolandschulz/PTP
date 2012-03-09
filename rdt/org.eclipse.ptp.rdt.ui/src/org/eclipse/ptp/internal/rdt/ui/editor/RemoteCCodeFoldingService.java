/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 


package org.eclipse.ptp.internal.rdt.ui.editor;

import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProviderFactory;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.internal.rdt.core.navigation.FoldingRegionsResult;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.core.subsystems.IConnectorService;

/**
 * A service for computing code folding on a remote host.
 */
public class RemoteCCodeFoldingService extends AbstractRemoteService implements IRemoteCCodeFoldingService {
	public RemoteCCodeFoldingService(IConnectorService connectorService) {
		super(connectorService);
	}

	public RemoteCCodeFoldingService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}

	public FoldingRegionsResult computeCodeFoldingRegions(IWorkingCopy workingCopy, int docLength, boolean fPreprocessorBranchFoldingEnabled, boolean fStatementsFoldingEnabled) {
		ICIndexSubsystem subsystem = getSubSystem();
		if (subsystem == null) 
			return null;
		
		ITranslationUnit unit = workingCopy.getTranslationUnit();
		
    	ITranslationUnit targetUnit;
		try {
			targetUnit = ModelAdapter.adaptElement(null, unit, 0, true);
		} catch (CModelException e1) {
			RDTLog.logError(e1);
			return null;
		} catch (Exception e) {
			RDTLog.logError(e);
			return null;
		}
		
		// TODO: This can potentially take a while.  But we need
		//       to trigger scope initialization in case it hasn't
		//       been done for the project.
		IProject project = unit.getCProject().getProject();
		IProgressMonitor monitor = new NullProgressMonitor();
		subsystem.checkProject(project, monitor);
		
		if(targetUnit instanceof TranslationUnit) {
			IScannerInfo scannerInfo = RemoteIndexerInfoProviderFactory.getScannerInfo(unit.getResource());
			Map<String,String> langaugeProperties = null;
			try {
				String languageId = unit.getLanguage().getId();
				langaugeProperties = RemoteIndexerInfoProviderFactory.getLanguageProperties(languageId, project);
			} catch(Exception e) {
				RDTLog.logError(e);
			}
			((TranslationUnit)targetUnit).setASTContext(scannerInfo, langaugeProperties);
		}
		
		return subsystem.computeFoldingRegions(targetUnit, docLength, fPreprocessorBranchFoldingEnabled, fStatementsFoldingEnabled); 
	}
	

}
