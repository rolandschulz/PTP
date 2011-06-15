/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
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
 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI
 * Version: 1.25
 */
package org.eclipse.ptp.internal.rdt.ui.callhierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.callhierarchy.CHMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class CallHierarchyUtil {

	private static boolean sIsJUnitTest= false;

	public static void open(final ICallHierarchyService service, final ITextEditor editor, final ITextSelection sel) {
		if (editor != null) {
			final IWorkingCopy inputCElement = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
			if (inputCElement != null) {
				final int selectionStart = sel.getOffset();
				final int selectionLength = sel.getLength();
				
				final ICProject project= inputCElement.getCProject();
				final Display display= Display.getCurrent();
	
				Job job= new Job(CHMessages.CallHierarchyUI_label) {
	        		@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							StatusLineHandler.clearStatusLine(editor.getSite());
							Scope scope = new Scope(project.getProject());
							final ICElement[] elems= service.findDefinitions(scope, project, inputCElement, selectionStart, selectionLength, monitor);
							if (elems.length > 0) {
								display.asyncExec(new Runnable() {
									public void run() {
										internalOpen(editor.getSite().getWorkbenchWindow(), elems);
									}});
							} else {
								StatusLineHandler.showStatusLineMessage(editor.getSite(), 
										CHMessages.CallHierarchyUI_openFailureMessage);
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

	public static void open(final ICallHierarchyService service, final IWorkbenchWindow window, final ICElement input) {
	    if (input != null) {
	    	final Display display= Display.getCurrent();
	
	    	Job job= new Job(CHMessages.CallHierarchyUI_label) {
	    		protected IStatus run(IProgressMonitor monitor) {
	    			Scope scope = new Scope(input.getCProject().getProject());
	    			final ICElement[] elems= service.findDefinitions(scope, input, monitor);
					if (elems != null && elems.length > 0) {
						display.asyncExec(new Runnable() {
							public void run() {
								internalOpen(window, elems);
							}});
					} 
					return Status.OK_STATUS;
	    		}
	    	};
	    	job.setUser(true);
	    	job.schedule();
	    }
	}

	private static RemoteCHViewPart internalOpen(IWorkbenchWindow window, ICElement input) {
	    IWorkbenchPage page= window.getActivePage();
	    try {
	        RemoteCHViewPart result= (RemoteCHViewPart)page.showView(UIPlugin.CALL_HIERARCHY_VIEW_ID);
	        result.setInput(input);
	        return result;
	    } catch (CoreException e) {
	        ExceptionHandler.handle(e, window.getShell(), CHMessages.OpenCallHierarchyAction_label, null); 
	    }
	    return null;        
	}

	private static RemoteCHViewPart internalOpen(IWorkbenchWindow window, ICElement[] input) {
		ICElement elem = null;
		switch (input.length) {
		case 0:
			break;
		case 1:
			elem = input[0];
			break;
		default:
			if (sIsJUnitTest) {
				throw new RuntimeException("ambiguous input"); //$NON-NLS-1$
			}
			elem = OpenActionUtil.selectCElement(input, window.getShell(),
					CHMessages.CallHierarchyUI_label, CHMessages.CallHierarchyUI_selectMessage,
					CElementLabels.ALL_DEFAULT | CElementLabels.MF_POST_FILE_QUALIFIED, 0);
			break;
		}
		if (elem != null) {
			return internalOpen(window, elem);
		} 
		return null;
	}

	public static void setIsJUnitTest(boolean val) {
		sIsJUnitTest= val;
	}

}
