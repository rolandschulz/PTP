/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.navigation;

import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProviderFactory;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.internal.rdt.core.model.WorkingCopy;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.ui.texteditor.ITextEditor;


public class RemoteNavigationService extends AbstractRemoteService implements INavigationService {

	public RemoteNavigationService(IConnectorService connectorService) {
		super(connectorService);
	}

	public RemoteNavigationService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}
	
	public OpenDeclarationResult openDeclaration(ITextEditor editor, String selectedText, int selectionStart, int selectionLength, IProgressMonitor monitor) {
		ITranslationUnit workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
		
		if (editor.isDirty()) { 
			// if the editor is dirty then we need to send the working copy to the remote side
			String contents = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
			workingCopy = new WorkingCopy(null, workingCopy, contents);
		}
		else {
			try {
				workingCopy = ModelAdapter.adaptElement(null, workingCopy, 0, true);
			} catch (CModelException e) {
				RDTLog.logError(e);
				return OpenDeclarationResult.failureUnexpectedError();
			}
		}
		
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkProject(workingCopy.getCProject().getProject(), monitor);
		
		if(workingCopy instanceof TranslationUnit) {
			IScannerInfo scannerInfo;
			if(workingCopy.getResource() == null) {
				// external translation unit... get scanner info from the context?
				scannerInfo = RemoteIndexerInfoProviderFactory.getScannerInfo(workingCopy.getCProject().getProject());
			}
			else {
				scannerInfo = RemoteIndexerInfoProviderFactory.getScannerInfo(workingCopy.getResource());
			}

			Map<String,String> langaugeProperties = null;
			try {
				String languageId = workingCopy.getLanguage().getId();
				IProject project = workingCopy.getCProject().getProject();
				langaugeProperties = RemoteIndexerInfoProviderFactory.getLanguageProperties(languageId, project);
			} catch(Exception e) {
				RDTLog.logError(e);
			}
			
			if(workingCopy instanceof TranslationUnit)
				((TranslationUnit)workingCopy).setASTContext(scannerInfo, langaugeProperties);
		}
		
		Scope scope = new Scope(workingCopy.getCProject().getProject());
		return subsystem.openDeclaration(scope, workingCopy, selectedText, selectionStart, selectionLength, monitor);
	}
	
}
