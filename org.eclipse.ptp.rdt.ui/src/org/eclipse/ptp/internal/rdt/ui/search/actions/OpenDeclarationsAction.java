/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction
 * Version: 1.73
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.model.WorkingCopy;
import org.eclipse.ptp.internal.rdt.core.navigation.INavigationService;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.IServiceModelManager;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenDeclarationsAction extends SelectionParseAction {

	private class WrapperJob extends Job {
		WrapperJob() {
			super(CEditorMessages.OpenDeclarations_dialog_title);
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				return performNavigation(monitor);
			}
			catch (CoreException e) {
				return e.getStatus();
			}
		}		
	}
	
	
	ITextSelection fTextSelection;
	private String fSelectedText;


	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super( editor );
		setText(CEditorMessages.OpenDeclarations_label);
		setToolTipText(CEditorMessages.OpenDeclarations_tooltip);
		setDescription(CEditorMessages.OpenDeclarations_description);
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
	
	
	protected IStatus performNavigation(IProgressMonitor monitor) throws CoreException {
		ITranslationUnit workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		
		if (workingCopy == null)
			return Status.CANCEL_STATUS;
				
		ICProject project = workingCopy.getCProject();
		
		// go to the remote index
		if (fEditor.isDirty()) { 
			// need to send the working copy to the remote side
			String contents = fEditor.getViewer().getDocument().get();
			workingCopy = new WorkingCopy(null, workingCopy, contents);
		}
		else {
			workingCopy = ModelAdapter.adaptElement(null, workingCopy, 0, true);
		}
		
		Scope scope = new Scope(workingCopy.getCProject().getProject().getName());
		int selectionStart  = fTextSelection.getOffset();
		int selectionLength = fTextSelection.getLength();
		
		INavigationService service = getNavigationService(workingCopy.getCProject().getProject());
		OpenDeclarationResult result = service.openDeclaration(scope, workingCopy, fSelectedText, selectionStart, selectionLength, monitor);
		if(result == null) // can happen when using the null service provider
			return Status.OK_STATUS;
		
		switch(result.getResultType()) {
			case RESULT_NAMES:
				IName[] names = (IName[]) result.getResult();
				navigateOneLocation(names, project);
				break;
			case RESULT_C_ELEMENTS:
				ICElement[] elements = (ICElement[]) result.getResult();
				navigateCElements(elements);
				break;
			case RESULT_INCLUDE_PATH:
				String path = (String) result.getResult();
				open(path, project);
				break;
			case FAILURE_SYMBOL_LOOKUP:
				String symbol = (String) result.getResult();
				reportSymbolLookupFailure(symbol);
				break;
			default:
				reportSelectionMatchFailure();
				break;
		}
		
		return Status.OK_STATUS;
	}

	
	/**
	 * Replaces the path portion of the given URI.
	 */
	private URI replacePath(URI u, String path) {
		try {
			return new URI(u.getScheme(), u.getUserInfo(), u.getHost(), u.getPort(),
			               path, // replaced! 
			               u.getQuery(),u.getFragment());
		} catch (URISyntaxException e) {
			RDTLog.logError(e);
			return null;
		}
	}
	
	
	private void open(String path, ICElement element) {
		open(path, element, -1, -1);
	}
	
	private void open(String path, final ICElement element, final int offset, final int length) {
		final URI uri = replacePath(element.getLocationURI(), path);
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
					clearStatusLine();
					IEditorPart editor = EditorUtility.openInEditor(uri, element);
					if (editor instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor)editor;
						if(offset >=0 && length >= 0)
							textEditor.selectAndReveal(offset, length);
					} else {
						reportSourceFileOpenFailure(uri.toString());
					}
						
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		});
	}
	
	
	private void navigateCElements(final ICElement[] elements) {
		if (elements == null || elements.length == 0)
			return;

		runInUIThread(new Runnable() {
			public void run() {
				ICElement target;
				if (elements.length == 1) {
					target= elements[0];
				}
				else {
					target = OpenActionUtil.selectCElement(elements, getSite().getShell(),
							CEditorMessages.OpenDeclarationsAction_dialog_title, CEditorMessages.OpenDeclarationsAction_selectMessage,
							CElementBaseLabels.ALL_DEFAULT | CElementBaseLabels.ALL_FULLY_QUALIFIED | CElementBaseLabels.MF_POST_FILE_QUALIFIED, 0);
				}
				
				if (target instanceof ISourceReference) {
					try {
						ISourceRange sourceRange = ((ISourceReference) target).getSourceRange();
						URI uri = replacePath(target.getLocationURI(), target.getPath().toString());
						open(uri, target, sourceRange.getIdStartPos(), sourceRange.getIdLength());
					} catch (CModelException e) {
						RDTLog.logError(e);
					}
				}
			}
		});
	}
	
	
	protected void reportSourceFileOpenFailure(String path) {
    	showStatusLineMessage(MessageFormat.format(
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
				
				open(filePath, project, offset, length);
				return;
			}
		}
	}




	@Override
	public void run() {
		computeSelectedWord();
		if (fTextSelection != null) {
			new WrapperJob().schedule();
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

	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	public void runSync() throws CoreException {
		computeSelectedWord();
		if (fTextSelection != null) {
			performNavigation(new NullProgressMonitor());
		}
	}


	private void computeSelectedWord() {
		fTextSelection = getSelectedStringFromEditor();
		fSelectedText= null;
		if (fTextSelection != null) {
			if (fTextSelection.getLength() > 0) {
				fSelectedText= fTextSelection.getText();
			}
			else {
				IDocument document= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
				IRegion reg= CWordFinder.findWord(document, fTextSelection.getOffset());
				if (reg != null && reg.getLength() > 0) {
					try {
						fSelectedText= document.get(reg.getOffset(), reg.getLength());
					} catch (BadLocationException e) {
						RDTLog.logError(e);
					}
				}
			}
		}
	}
}

