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
package org.eclipse.ptp.debug.internal.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PDebugModel;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Clement chu
 *
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTarget {
	private UIDebugManager uiDebugManager = null;
	/** Constructor
	 * 
	 */
	public ToggleBreakpointAdapter() {
		uiDebugManager = PTPDebugUIPlugin.getUIDebugManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		String errorMessage = null;
		if (part instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor)part;
			IEditorInput input = textEditor.getEditorInput();
			if (input == null) {
				errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Empty_editor_1");
			}
			else {
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if (document == null) {
					errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Missing_document_1");
				} else {
					IResource resource = getResource(textEditor);
					if (resource == null) {
						errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Missing_resource_1");
					} else {
						BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
						int lineNumber = bv.getValidLineBreakpointLocation(document, ((ITextSelection)selection).getStartLine());
						if (lineNumber == -1) {
							errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Invalid_line_1");
						} else {
							IPJob job = uiDebugManager.getJob();
							String sid = uiDebugManager.getCurrentSetId();
							sid = (sid == null || sid.length() == 0)?IElementHandler.SET_ROOT_ID:sid;
							String sourceHandle = getSourceHandle(input);
							IPLineBreakpoint[] breakpoints = PDebugModel.lineBreakpointsExists(sourceHandle, resource, lineNumber);
							if (breakpoints.length > 0) {// remove breakpoint if found any breakpoint in current job
								IPLineBreakpoint breakpoint = PDebugModel.lineBreakpointExists(breakpoints, job);
								if (breakpoint != null) {
									if (breakpoint.isGlobal() && job == null) {
										DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);												
									} else {
										IPSession session = PTPDebugCorePlugin.getDebugModel().getSession(job);
										if (session != null) {
											session.getBreakpointManager().deleteBreakpoint(breakpoint);
										}
									}
								} else {// create a new breakpoint
									PDebugModel.createLineBreakpoint(sourceHandle, resource, lineNumber, true, 0, "", true, sid, job);
								}
							} else {// no breakpoint found and create a new one
								PDebugModel.createLineBreakpoint(sourceHandle, resource, lineNumber, true, 0, "", true, sid, job);
							}
							return;
						}
					}
				}
			}
		}
		else {
			errorMessage = ActionMessages.getString("RunToLineAdapter.Operation_is_not_supported_1");
		}
		throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, errorMessage, null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		/*
		 * TODO DisassemblyView
		 */
		return (selection instanceof ITextSelection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		PDebugUtils.println("*** Not Implemented YET ***");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// FIXME: not implemented yet
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		// FIXME: not implemented yet
		return false;
	}

	/** report
	 * @param message
	 * @param part
	 */
	protected void report(String message, IWorkbenchPart part) {
		IEditorStatusLine statusLine = (IEditorStatusLine)part.getAdapter(IEditorStatusLine.class);
		if (statusLine != null) {
			if (message != null)
				statusLine.setMessage(true, message, null);
			else
				statusLine.setMessage(true, null, null);
		}
		if (message != null && PTPDebugUIPlugin.getActiveWorkbenchShell() != null) {
			PTPDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
		}
	}
	/** Get resource
	 * @param part
	 * @return
	 */
	protected static IResource getResource(IWorkbenchPart part) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if ( part instanceof IEditorPart ) {
			IEditorInput editorInput = ((IEditorPart)part).getEditorInput();
			if ( editorInput instanceof IFileEditorInput ) {
				return ((IFileEditorInput)editorInput).getFile();
			}
			ILocationProvider provider = (ILocationProvider)editorInput.getAdapter( ILocationProvider.class );
			if ( provider != null ) {
				IPath location = provider.getPath( editorInput );
				IFile[] files = root.findFilesForLocation( location );
				if ( files.length > 0 )
					return files[0];
			}
		}
		return root;
	}

	/** Get source handle
	 * @param input
	 * @return
	 * @throws CoreException
	 */
	private String getSourceHandle(IEditorInput input) throws CoreException {
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput)input).getFile().getLocation().toOSString();
		}
		if (input instanceof IStorageEditorInput) {
			return ((IStorageEditorInput)input).getStorage().getFullPath().toOSString();
		}
		/*
		 * TODO DisassemblyView
		 */
		return "";
	}

	private class BreakpointLocationVerifier {
		/** Get valid line breakpoint location
		 * @param doc
		 * @param lineNumber
		 * @return
		 */
		public int getValidLineBreakpointLocation(IDocument doc, int lineNumber) {
			return lineNumber + 1;
		}
		/** Get valid address breakpoint location
		 * @param doc
		 * @param lineNumber
		 * @return
		 */
		public int getValidAddressBreakpointLocation(IDocument doc, int lineNumber) {
			return lineNumber + 1;
		}
	}
}