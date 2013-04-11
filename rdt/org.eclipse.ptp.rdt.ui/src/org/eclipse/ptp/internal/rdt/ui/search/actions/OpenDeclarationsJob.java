/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *	  Sergey Prigogin (Google)
 *    IBM Corporation
******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsJob
 * Version: 1.22
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.model.WorkingCopy;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.ui.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.ui.util.PathReplacer;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

class OpenDeclarationsJob extends Job{

	private final SelectionParseAction fAction;
	private final ITranslationUnit fTranslationUnit;
	private final ITextSelection fTextSelection;
	private final String fSelectedText;
	private CEditor fEditor;

	OpenDeclarationsJob(SelectionParseAction action, ITranslationUnit editorInput, ITextSelection textSelection, String text, CEditor editor) {
		super(CEditorMessages.OpenDeclarations_dialog_title);
		fAction= action;
		fTranslationUnit= editorInput;
		fTextSelection= textSelection;
		fSelectedText= text;
		fEditor = editor;
	}
	
	private INavigationService getNavigationService(IProject project) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
		if (!(serviceProvider instanceof IIndexServiceProvider2)) {
			return null;
		}
		INavigationService service = ((IIndexServiceProvider2) serviceProvider).getNavigationService();
		return service;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			return performNavigation(monitor);
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	IStatus performNavigation(IProgressMonitor monitor) throws CoreException {
		ITranslationUnit workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		if (workingCopy == null)
			return Status.CANCEL_STATUS;
				
		ICProject project = workingCopy.getCProject();
		int selectionStart  = fTextSelection.getOffset();
		int selectionLength = fTextSelection.getLength();
	
		INavigationService service = getNavigationService(project.getProject());	
		OpenDeclarationResult result = service.openDeclaration(fEditor, fSelectedText, selectionStart, selectionLength, monitor);
		
		if(result == null) // can happen when using the null service provider
			return Status.OK_STATUS;
		
		switch(result.getResultType()) {
			case RESULT_NAMES:
				IName[] names = (IName[]) result.getResult();
				navigateOneLocation(names, project);
				break;
			case RESULT_C_ELEMENTS:
				ICElement[] elements = (ICElement[]) result.getResult();
				navigateCElements(elements, project);
				break;
			case RESULT_INCLUDE_PATH:
				String path = (String) result.getResult();
				open(project.getProject(), path, project);
				break;
			case RESULT_NAME:
				IName name = (IName) result.getResult();
				navigateToName(name, project);
				break;
			case RESULT_LOCATION:
				IASTFileLocation location = (IASTFileLocation) result.getResult();
				navigateToLocation(location, project);
				break;
			case FAILURE_SYMBOL_LOOKUP:
				String symbol = (String) result.getResult();
				fAction.reportSymbolLookupFailure(symbol);
				break;
			case FAILURE_INCLUDE_LOOKUP:
				String includedPath = (String) result.getResult();
				fAction.reportIncludeLookupFailure(includedPath);
				break;
			default:
				fAction.reportSelectionMatchFailure();
				break;
		}
		
		return Status.OK_STATUS;
	}

	private void open(IProject project, String path, ICElement element) {
		open(project, path, element, -1, -1);
	}
	
	private void open(IProject project, String path, final ICElement element, final int offset, final int length) {
		final URI uri = PathReplacer.replacePath(project, element.getLocationURI(), path);
		if(uri == null)
			return;
		open(uri, element, offset, length);
	}
	
	/**
	 * Opens the editor.
	 */
	private void open(final URI uri, final ICElement element, final int offset, final int length) {
		runInUIThread(new Runnable() {
			public void run() {
				try {
					fAction.clearStatusLine();
					IEditorPart editor = EditorUtility.openInEditor(uri, element);
					if (editor instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor)editor;
						if(offset >=0 && length >= 0)
							textEditor.selectAndReveal(offset, length);
					} else {
						reportSourceFileOpenFailure(uri.toString());
					}

				} catch (CoreException e) {
					RDTLog.logError(e);
				}
			}
		});
	}
	
	
	private void navigateCElements(final ICElement[] elements, final ICProject project) {
		if (elements == null || elements.length == 0)
			return;

		final ICElement[] uniqueElements;
		if (elements.length < 2) {
			uniqueElements= elements;
		} else {
			// Make sure only one element per location is proposed
			Set<String> sigs= new HashSet<String>();
			sigs.add(null);
			List<ICElement> uniqueElementsList= new ArrayList<ICElement>();
			for (ICElement elem : elements) {
				if (sigs.add(getLocationSignature((ISourceReference) elem))) {
					uniqueElementsList.add(elem);

				}
			}
			uniqueElements = uniqueElementsList.toArray(new ICElement[uniqueElementsList.size()]);
		}

		runInUIThread(new Runnable() {
			public void run() {
				ICElement target;
				if (uniqueElements.length == 1) {
					target= uniqueElements[0];
				}
				else {
					target = OpenActionUtil.selectCElement(elements, fAction.getSite().getShell(),
							CEditorMessages.OpenDeclarationsAction_dialog_title, CEditorMessages.OpenDeclarationsAction_selectMessage,
							CElementLabels.ALL_DEFAULT | CElementLabels.ALL_FULLY_QUALIFIED | CElementLabels.MF_POST_FILE_QUALIFIED, 0);
				}
				
				if (target instanceof ISourceReference) {
					try {
						ISourceRange sourceRange = ((ISourceReference) target).getSourceRange();
						URI uri = PathReplacer.replacePath(project.getProject(), target.getLocationURI(), target.getPath().toString());
						
						open(uri, project, sourceRange.getIdStartPos(), sourceRange.getIdLength());
						
					} catch (CoreException e) {
						RDTLog.logError(e);
					}
				}
			}
		});
	}
	
	private String getLocationSignature(ISourceReference elem) {
		ITranslationUnit tu= elem.getTranslationUnit();
		ISourceRange sourceRange;
		try {
			sourceRange = elem.getSourceRange();
			if (tu != null && sourceRange != null) {
				return tu.getPath().toString() + IPath.SEPARATOR + sourceRange.getIdStartPos() + IPath.SEPARATOR + sourceRange.getIdLength();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return null;
	}

	
	
	protected void reportSourceFileOpenFailure(String path) {
		fAction.showStatusLineMessage(MessageFormat.format(
    			CSearchMessages.SelectionParseAction_FileOpenFailure_format, 
    			path));
    }
	


	/** 
	 * Opens the editor on the first name in the array that has 
	 * location information.
	 */
	private void navigateOneLocation(IName[] declNames, ICProject project) {
		for(IName name : declNames) {
			IASTFileLocation fileloc = name.getFileLocation();
			if(fileloc != null) {
				String filePath = fileloc.getFileName();
				int offset = fileloc.getNodeOffset();
				int length = fileloc.getNodeLength();
				
				open(project.getProject(), filePath, project, offset, length);
				return;
			}
		}
	}
	
	private void navigateToName(IName name, ICProject project) {
		navigateToLocation(name.getFileLocation(), project);
	}

	private void navigateToLocation(IASTFileLocation fileloc, ICProject project) {
		if (fileloc != null) {			
			String filePath = fileloc.getFileName();
			int offset = fileloc.getNodeOffset();
			int length = fileloc.getNodeLength();
			
			open(project.getProject(), filePath, project, offset, length);
		}
	}


	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			Display.getDefault().asyncExec(runnable);
		}
	}
}