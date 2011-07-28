/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation
 *******************************************************************************/ 


package org.eclipse.ptp.internal.rdt.ui.navigation;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.core.miners.OpenDeclarationHandler;
import org.eclipse.ptp.internal.rdt.core.miners.OpenDeclarationHandler.INavigationErrorLogger;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ui.texteditor.ITextEditor;


public class LocalNavigationService implements INavigationService {


	public OpenDeclarationResult openDeclaration(ITextEditor editor, String selectedText, int selectionStart, int selectionLength, IProgressMonitor monitor) {
		ITranslationUnit workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
		
		try {
			monitor.beginTask("Open Declaration", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			return handleOpenDeclarationLocally(workingCopy, selectedText, selectionStart, selectionLength);
		}
		finally {
			monitor.done();
		}
	}

	
	/**
	 * Locally we can use the editor's ASTCache.
	 */
	private static OpenDeclarationResult handleOpenDeclarationLocally(final ITranslationUnit translationUnit, final String selectedText, 
			                                                         final int selectionStart, final int selectionLength) {
		
		final IIndex index;
		try {
			index = CCorePlugin.getIndexManager().getIndex(translationUnit.getCProject(),
					                   IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
		} catch (CoreException e1) {
			return OpenDeclarationResult.failureUnexpectedError();
		}

		final INavigationErrorLogger logger = new INavigationErrorLogger() {
			@Override public void logDebugMessage(String message) {
				RDTLog.logInfo(message);
			}
			@Override public void logError(String message, Throwable e) {
				RDTLog.logError(e, message);
			}
		};
		
		final OpenDeclarationResult[] result = new OpenDeclarationResult[1];
		
		ASTRunnable astRunnable = new ASTRunnable() {
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
				result[0] = OpenDeclarationHandler.doHandleOpenDeclaration(ast, translationUnit, selectedText, selectionStart, selectionLength, index, logger);
				return Status.OK_STATUS;
			}
		};
		
		try {
			index.acquireReadLock();
		} catch (InterruptedException e) {
			return OpenDeclarationResult.failureUnexpectedError();
		}

		try {
			ASTProvider.getASTProvider().runOnAST(translationUnit, ASTProvider.WAIT_ACTIVE_ONLY, new NullProgressMonitor(), astRunnable);
		} finally {
			index.releaseReadLock();
		}
		
		return result[0];
	}
}
