/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.debug.core.model.IJumpToLine;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.IPTPUIConstants;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Clement chu
 * 
 */
public class ResumeAtLineAdapter implements IResumeAtLineTarget {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.ui.actions.IResumeAtLineTarget#resumeAtLine(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void resumeAtLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
		String errorMessage = null;
		if (part instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) part;
			IEditorInput input = textEditor.getEditorInput();
			if (input == null) {
				errorMessage = Messages.ResumeAtLineAdapter_0;
			} else {
				IDocument document = textEditor.getDocumentProvider().getDocument(input);
				if (document == null) {
					errorMessage = Messages.ResumeAtLineAdapter_1;
				} else {
					final String fileName = getFileName(input);
					ITextSelection textSelection = (ITextSelection) selection;
					final int lineNumber = textSelection.getStartLine() + 1;
					if (target instanceof IAdaptable) {
						final IJumpToLine jumpToLine = (IJumpToLine) ((IAdaptable) target).getAdapter(IJumpToLine.class);
						if (jumpToLine != null && jumpToLine.canJumpToLine(fileName, lineNumber)) {
							Runnable r = new Runnable() {
								public void run() {
									try {
										jumpToLine.jumpToLine(fileName, lineNumber);
									} catch (DebugException e) {
										failed(e);
									}
								}
							};
							runInBackground(r);
						}
					}
					return;
				}
			}
		}
		/*
		 * TODO DisassemblyView
		 * else if (part instanceof DisassemblyView) {
		 * IEditorInput input = ((DisassemblyView)part).getInput();
		 * if (!(input instanceof DisassemblyEditorInput)) {
		 * errorMessage = ActionMessages.getString("ResumeAtLineAdapter.2");
		 * }
		 * else {
		 * ITextSelection textSelection = (ITextSelection)selection;
		 * int lineNumber = textSelection.getStartLine() + 1;
		 * final IAddress address = ((DisassemblyEditorInput)input).getAddress(lineNumber);
		 * if (address != null && target instanceof IAdaptable) {
		 * final IJumpToAddress jumpToAddress = (IJumpToAddress)((IAdaptable)target).getAdapter(IJumpToAddress.class);
		 * if (jumpToAddress != null && jumpToAddress.canJumpToAddress(address)) {
		 * Runnable r = new Runnable() {
		 * public void run() {
		 * try {
		 * jumpToAddress.jumpToAddress(address);
		 * } catch(DebugException e) {
		 * failed(e);
		 * }
		 * }
		 * };
		 * runInBackground(r);
		 * }
		 * }
		 * return;
		 * }
		 * }
		 */
		else {
			errorMessage = Messages.ResumeAtLineAdapter_2;
		}
		throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR,
				errorMessage, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.ui.actions.IResumeAtLineTarget#canResumeAtLine(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canResumeAtLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
		if (target instanceof IAdaptable) {
			if (part instanceof IEditorPart) {
				IJumpToLine jumpToLine = (IJumpToLine) ((IAdaptable) target).getAdapter(IJumpToLine.class);
				if (jumpToLine == null) {
					return false;
				}
				IEditorPart editorPart = (IEditorPart) part;
				IEditorInput input = editorPart.getEditorInput();
				if (input == null) {
					return false;
				}
				if (!(editorPart instanceof ITextEditor)) {
					return false;
				}
				ITextEditor textEditor = (ITextEditor) editorPart;
				IDocument document = textEditor.getDocumentProvider().getDocument(input);
				if (document == null) {
					return false;
				}
				String fileName;
				try {
					fileName = getFileName(input);
				} catch (CoreException e) {
					return false;
				}
				ITextSelection textSelection = (ITextSelection) selection;
				int lineNumber = textSelection.getStartLine() + 1;
				return jumpToLine.canJumpToLine(fileName, lineNumber);
			}
			/*
			 * TODO DisassemblyView
			 * if (part instanceof DisassemblyView) {
			 * IJumpToAddress jumpToAddress = (IJumpToAddress)((IAdaptable)target).getAdapter(IJumpToAddress.class);
			 * if (jumpToAddress == null)
			 * return false;
			 * IEditorInput input = ((DisassemblyView)part).getInput();
			 * if (!(input instanceof DisassemblyEditorInput)) {
			 * return false;
			 * }
			 * ITextSelection textSelection = (ITextSelection)selection;
			 * int lineNumber = textSelection.getStartLine() + 1;
			 * IAddress address = ((DisassemblyEditorInput)input).getAddress(lineNumber);
			 * return jumpToAddress.canJumpToAddress(address);
			 * }
			 */
		}
		return false;
	}

	/**
	 * Get file name
	 * 
	 * @param input
	 * @return
	 * @throws CoreException
	 */
	private String getFileName(IEditorInput input) throws CoreException {
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile().getName();
		}
		if (input instanceof IStorageEditorInput) {
			return ((IStorageEditorInput) input).getStorage().getName();
		}
		return null;
	}

	/**
	 * Run a job in background
	 * 
	 * @param r
	 */
	private void runInBackground(Runnable r) {
		DebugPlugin.getDefault().asyncExec(r);
	}

	/**
	 * Failed to store status handler
	 * 
	 * @param e
	 */
	protected void failed(Throwable e) {
		MultiStatus ms = new MultiStatus(PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.STATUS_CODE_ERROR,
				Messages.ResumeAtLineAdapter_3, null);
		ms.add(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.STATUS_CODE_ERROR, e.getMessage(),
				e));
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(ms);
		if (handler != null) {
			try {
				handler.handleStatus(ms, this);
			} catch (CoreException ex) {
			}
		}
	}
}
