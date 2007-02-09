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
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
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
		//if (!PTPDebugUIPlugin.isPTPDebugPerspective())
			//return;
		
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
				}
				else {
					IResource resource = getResource(textEditor);
					if (resource == null) {
						errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Missing_resource_1");
					}
					else {
						BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
						int lineNumber = bv.getValidLineBreakpointLocation(document, ((ITextSelection)selection).getStartLine());
						if (lineNumber == -1) {
							errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Invalid_line_1");
						}
						else {
							String jid = uiDebugManager.getCurrentJobId();
							String jobName = "";
							if (!PTPDebugUIPlugin.isPTPDebugPerspective() || uiDebugManager.isNoJob(jid) || uiDebugManager.isJobStop(jid)) {
								jid = IPBreakpoint.GLOBAL;
								jobName = IPBreakpoint.GLOBAL;
							}
							else 
								jobName = uiDebugManager.getName(jid);
							
							String sid = uiDebugManager.getCurrentSetId();
							sid = (sid == null || sid.length() == 0)?IElementHandler.SET_ROOT_ID:sid;
							String sourceHandle = getSourceHandle(input);
							IPLineBreakpoint[] breakpoints = PTPDebugCorePlugin.getDebugModel().lineBreakpointsExists(sourceHandle, resource, lineNumber);
							if (jid.equals(IPBreakpoint.GLOBAL) && breakpoints.length > 0)//remove all breakpoints if found any breakpoints in none job selected mode
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);
							else if (breakpoints.length > 0) {//remove breakpoint if found any breakpoint in current job
								IPLineBreakpoint breakpoint = PTPDebugCorePlugin.getDebugModel().lineBreakpointExists(breakpoints, jid);
								if (breakpoint != null) {
									if (!breakpoint.isGlobal())//remove breakpoint when it is not in none job selected mode
										DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
								}
								else // create a new breakpoint
									PTPDebugCorePlugin.getDebugModel().createLineBreakpoint(sourceHandle, resource, lineNumber, true, 0, "", true, sid, jid, jobName);
							}
							else // no breakpoint found and create a new one
								PTPDebugCorePlugin.getDebugModel().createLineBreakpoint(sourceHandle, resource, lineNumber, true, 0, "", true, sid, jid, jobName);
							return;
						}
					}
				}
			}
		}
		/*
		 * TODO DisassemblyView
		else if (part instanceof DisassemblyView) {
			IEditorInput input = ((DisassemblyView)part).getInput();
			if (!(input instanceof DisassemblyEditorInput)) {
				errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Empty_editor_1");
			}
			else {
				BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
				int lineNumber = bv.getValidAddressBreakpointLocation( null, ((ITextSelection)selection).getStartLine() );
				if (lineNumber == -1) {
					errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Invalid_line_1");
				}
				else {
					IAddress address = ((DisassemblyEditorInput)input).getAddress(lineNumber);
					if (address == null) {
						errorMessage = ActionMessages.getString("ToggleBreakpointAdapter.Invalid_line_1");						
					}
					else {
						ICLineBreakpoint breakpoint = ((DisassemblyEditorInput)input).breakpointExists(address);
						if (breakpoint != null) {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
						}
						else {
							String module = ((DisassemblyEditorInput)input).getModuleFile();
							IResource resource = getAddressBreakpointResource(((DisassemblyEditorInput)input).getSourceFile());
							String sourceHandle = getSourceHandle( input );
							CDIDebugModel.createAddressBreakpoint(module, sourceHandle, resource, ((DisassemblyEditorInput)input).getSourceLine(lineNumber), address, true, 0, "", true);
						}
						return;
					}
				}
			}
		}
		*/
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
		if (part instanceof DisassemblyView) {
			IEditorInput input = ((DisassemblyView)part).getInput();
			if (!(input instanceof DisassemblyEditorInput) || ((DisassemblyEditorInput)input).equals(DisassemblyEditorInput.EMPTY_EDITOR_INPUT)) {
				return false;
			}			
		}
		*/
		return (selection instanceof ITextSelection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		PDebugUtils.println("*** Not Implemented YET ***");
		/*
		 * FIXME doesn't implemenet yet..
		if (selection instanceof ITextSelection) {
			String text = ((ITextSelection)selection).getText();
			if (text != null ) {
				IResource resource = getResource(part);
				if (resource instanceof IFile) {
					ITranslationUnit tu = getTranslationUnit((IFile)resource);
					if (tu != null) {
						try {
							ICElement element = tu.getElement(text.trim());
							if (element instanceof IFunction || element instanceof IMethod) {
								toggleMethodBreakpoints0((IDeclaration)element);
							}
						}
						catch(CModelException e) {
						}
					}
				}
			}
		}
		else if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1 && (ss.getFirstElement() instanceof IFunction || ss.getFirstElement() instanceof IMethod)) {
				toggleMethodBreakpoints0((IDeclaration)ss.getFirstElement());
			}
		}
		*/
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		/*
		 * FIXME doesn't implemenet yet..
		if (selection instanceof ITextSelection) {
			String text = ((ITextSelection)selection).getText();
			if (text != null) {
				IResource resource = getResource(part);
				if ( resource instanceof IFile ) {
					ITranslationUnit tu = getTranslationUnit((IFile)resource);
					if (tu != null) {
						try {
							ICElement element = tu.getElement( text.trim() );
							return (element instanceof IFunction || element instanceof IMethod);
						}
						catch(CModelException e) {
						}
					}
				}
			}
		}
		else if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1) {
				return (ss.getFirstElement() instanceof IFunction || ss.getFirstElement() instanceof IMethod);
			}
		}
		*/
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		/*
		 * FIXME doesn't implemenet yet..
		if (selection instanceof ITextSelection) {
			String text = ((ITextSelection)selection).getText();
			if (text != null) {
				IResource resource = getResource(part);
				if ( resource instanceof IFile ) {
					ITranslationUnit tu = getTranslationUnit((IFile)resource);
					if (tu != null) {
						try {
							ICElement element = tu.getElement(text.trim());
							if (element instanceof IVariable) {
								toggleVariableWatchpoint(part, (IVariable)element);
							}
						}
						catch(CModelException e) {
						}
					}
				}
			}
		}
		else if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1 && ss.getFirstElement() instanceof IVariable) {
				toggleVariableWatchpoint(part, (IVariable)ss.getFirstElement());
			}
		}
		*/
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		/*
		 * FIXME doesn't implemenet yet..
		if (selection instanceof ITextSelection) {
			String text = ((ITextSelection)selection).getText();
			if ( text != null ) {
				IResource resource = getResource(part);
				if (resource instanceof IFile) {
					ITranslationUnit tu = getTranslationUnit((IFile)resource);
					if (tu != null) {
						try {
							return (tu.getElement(text.trim()) instanceof IVariable);
						}
						catch(CModelException e) {
						}
					}
				}
			}
		}
		else if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1) {
				return (ss.getFirstElement() instanceof IVariable);
			}
		}
		*/
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
		if (input instanceof DisassemblyEditorInput) {
			String sourceFile = ((DisassemblyEditorInput)input).getSourceFile();
			if (sourceFile != null) {
				return sourceFile;
			}
			return ((DisassemblyEditorInput)input).getModuleFile();
		}
		*/
		return "";
	}

	/*
	 * FIXME doesn't implemenet yet..
	private void toggleVariableWatchpoint(IWorkbenchPart part, IVariable variable) throws CoreException {
		String sourceHandle = getSourceHandle(variable);
		IResource resource = getElementResource(variable);
		String expression = getVariableName( variable );
		IPWatchpoint watchpoint = CDIDebugModel.watchpointExists(sourceHandle, resource, expression);
		if (watchpoint != null) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(watchpoint, true);
		}
		else {
			AddWatchpointDialog dlg = new AddWatchpointDialog(part.getSite().getShell(), true, false, expression, false);
			if (dlg.open() != Window.OK)
				return;
			expression = dlg.getExpression();
			int lineNumber = -1;
			int charStart = -1;
			int charEnd = -1;
			try {
				ISourceRange sourceRange = variable.getSourceRange();
				if ( sourceRange != null ) {
					charStart = sourceRange.getStartPos();
					charEnd = charStart + sourceRange.getLength();
					if ( charEnd <= 0 ) {
						charStart = -1;
						charEnd = -1;
					}
					lineNumber = sourceRange.getStartLine();
				}
			}
			catch(CModelException e) {
				DebugPlugin.log(e);
			}
			CDIDebugModel.createWatchpoint(sourceHandle, resource, charStart, charEnd, lineNumber, dlg.getWriteAccess(), dlg.getReadAccess(), expression, true, 0, "", true );
		}
	}
	*/

	/*
	 * FIXME doesn't implemenet yet..
	private String getSourceHandle(IDeclaration declaration) {
		ITranslationUnit tu = declaration.getTranslationUnit();
		if (tu != null) {
			IResource resource = tu.getResource();
			if (resource != null)
				return resource.getLocation().toOSString();
			return tu.getPath().toOSString();
		}
		return "";
	}
	*/

	/*
	 * FIXME doesn't implemenet yet..
	private IResource getElementResource(IDeclaration declaration) {
		return declaration.getUnderlyingResource();
	}
	*/

	/*
	 * FIXME doesn't implemenet yet..
	private String getFunctionName(IFunction function) {
		String functionName = function.getElementName();
		StringBuffer name = new StringBuffer( functionName );
		ITranslationUnit tu = function.getTranslationUnit();
		if (tu != null && tu.isCXXLanguage()) {
			appendParameters(name, function);
		}
		return name.toString();
	}
	*/

	/*
	 * FIXME doesn't implemenet yet..
	private String getMethodName(IMethod method) {
		StringBuffer name = new StringBuffer();
		String methodName = method.getElementName();
		ICElement parent = method.getParent();
		while (parent != null && ( parent.getElementType() == ICElement.C_NAMESPACE || parent.getElementType() == ICElement.C_CLASS)) {
			name.append(parent.getElementName() ).append( "::");
			parent = parent.getParent();
		}
		name.append(methodName);
		appendParameters(name, method);
		return name.toString();
	}
	*/

	/*
	 * FIXME doesn't implemenet yet..
	private void appendParameters(StringBuffer sb, IFunctionDeclaration fd) {
		String[] params = fd.getParameterTypes();
		sb.append('(');
		for(int i = 0; i < params.length; ++i) {
			sb.append(params[i]);
			if (i != params.length - 1)
				sb.append(',');
		}
		sb.append(')');
	}
	*/

	/*
	 * FIXME doesn't implemenet yet..
	private String getVariableName(IVariable variable) {
		return variable.getElementName();
	}
	*/

	/*
	 * FIXME doesn't implemenet yet..
	private ITranslationUnit getTranslationUnit(IFile file) {
		Object element = CoreModel.getDefault().create(file);
		if (element instanceof ITranslationUnit) {
			return (ITranslationUnit)element;
		}
		return null;
	}
	*/
	
	/*
	 * FIXME doesn't implemenet yet..
	private void toggleMethodBreakpoints0(IDeclaration declaration) throws CoreException {
		String sourceHandle = getSourceHandle(declaration);
		IResource resource = getElementResource(declaration);
		String functionName = (declaration instanceof IFunction) ? getFunctionName((IFunction)declaration) : getMethodName((IMethod)declaration);
		ICFunctionBreakpoint breakpoint = CDIDebugModel.functionBreakpointExists(sourceHandle, resource, functionName);
		if (breakpoint != null) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
		}
		else {
			int lineNumber = -1;
			int charStart = -1;
			int charEnd = -1;
			try {
				ISourceRange sourceRange = declaration.getSourceRange();
				if (sourceRange != null) {
					charStart = sourceRange.getStartPos();
					charEnd = charStart + sourceRange.getLength();
					if (charEnd <= 0) {
						charStart = -1;
						charEnd = -1;
					}
					lineNumber = sourceRange.getStartLine();
				}
			}
			catch (CModelException e) {
				DebugPlugin.log(e);
			}
			CDIDebugModel.createFunctionBreakpoint(sourceHandle, resource, functionName, charStart, charEnd, lineNumber, true,  0, "", true );
		}
	}
	*/

	/*
	private IResource getAddressBreakpointResource(String fileName) {
		if (fileName != null) {
			IPath path = new Path(fileName);
			if (path.isValidPath(fileName) ) {
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
				if (files.length > 0)
					return files[0];
			}
		}
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	*/
	
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