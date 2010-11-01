/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.TypeHierarchyUI
 * Version: 1.13
 */
package org.eclipse.ptp.internal.rdt.ui.typehierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.typehierarchy.Messages;
import org.eclipse.cdt.internal.ui.typehierarchy.TypeHierarchyUI;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class TypeHierarchyUtil {
	public static RemoteTHViewPart open(ITypeHierarchyService service, ICElement input, IWorkbenchWindow window) {
	    if (!TypeHierarchyUI.isValidInput(input)) {
	    	return null;
	    }
	    ICElement memberInput= null;
	    if (!TypeHierarchyUI.isValidTypeInput(input)) {
	    	memberInput= input;
	    	input= memberInput.getParent();
	    	if (!TypeHierarchyUI.isValidTypeInput(input)) {
	    		IProgressMonitor monitor = new NullProgressMonitor();
	    		final ICProject project = input.getCProject();
	    		Scope scope = new Scope(project.getProject());
	    		ICElement[] inputs= service.findInput(scope, memberInput, monitor);
	    		if (inputs != null) {
	    			input= inputs[0];
	    			memberInput= inputs[1];
	    		}
	    	}
	    }
	    		
	    if (TypeHierarchyUI.isValidTypeInput(input)) {
	    	return openInViewPart(window, input, memberInput);
	    }
	    return null;
	}

	public static void open(final ITypeHierarchyService service, final ITextEditor editor, final ITextSelection sel) {
		if (editor != null) {
			ICElement inputCElement = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
			if (inputCElement != null) {
				final ICProject project= inputCElement.getCProject();
				final IEditorInput editorInput = editor.getEditorInput();
				final Display display= Display.getCurrent();

				Job job= new Job(Messages.TypeHierarchyUI_OpenTypeHierarchy) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							StatusLineHandler.clearStatusLine(editor.getSite());
							
							IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
							int selectionStart = sel.getOffset();
							int selectionLength = sel.getLength();
							Scope scope = new Scope(project.getProject());
							final ICElement[] elems= service.findInput(scope, project, workingCopy, selectionStart, selectionLength, monitor);
							if (elems != null && elems.length == 2) {
								display.asyncExec(new Runnable() {
									public void run() {
										openInViewPart(editor.getSite().getWorkbenchWindow(), elems[0], elems[1]);
									}});
							} else {
								StatusLineHandler.showStatusLineMessage(editor.getSite(), 
										Messages.TypeHierarchyUI_OpenFailure_message);
							}
							return Status.OK_STATUS;
						} 
						catch (CoreException e) {
							return e.getStatus();
						}
					}
				};
				job.setUser(true);
				job.schedule();
			}
		}
	}

	private static RemoteTHViewPart openInViewPart(IWorkbenchWindow window, ICElement input, ICElement member) {
	    IWorkbenchPage page= window.getActivePage();
	    try {
	        RemoteTHViewPart result= (RemoteTHViewPart)page.showView(UIPlugin.TYPE_HIERARCHY_VIEW_ID);
	        result.setInput(input, member);
	        return result;
	    } catch (CoreException e) {
	        ExceptionHandler.handle(e, window.getShell(), Messages.TypeHierarchyUI_OpenTypeHierarchy, null); 
	    }
	    return null;        
	}

}
