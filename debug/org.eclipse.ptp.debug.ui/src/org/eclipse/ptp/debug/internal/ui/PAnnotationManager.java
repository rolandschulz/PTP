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
package org.eclipse.ptp.debug.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Clement chu
 *
 */
public class PAnnotationManager {
	private static PAnnotationManager instance = null;
	protected Map fDebugTargetMap = null;
	
	public PAnnotationManager() {
		fDebugTargetMap = new HashMap();
	}
	
	public static PAnnotationManager getDefault() {
		if (instance == null)
			instance = new PAnnotationManager();
		return instance;
	}
	
	public void shutdown() {
		removeAllAnnotations();
		fDebugTargetMap = null;
	}
		
	public IStackFrame getTopStackFrame(IThread thread) {
		try {
			return thread.getTopStackFrame();
		} catch (DebugException de) {
			return null;
		}
	}
	
	//FIXME Need to redo it
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame stackFrame) {
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			return false;
		
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IEditorInput editorInput = textEditor.getEditorInput();
		
		IResource resource = getResource(editorInput);
		if (resource == null)
			return false;
		
        IThread thread = stackFrame.getThread();
        
        IAnnotationModel annModel = docProvider.getAnnotationModel(editorInput);
        if (annModel == null)
        		return false;

        Position position = createPosition(stackFrame, docProvider.getDocument(editorInput));
        if (position == null)
        		return false;
        
		Annotation instPtrAnnotation = null; 
		try {	
			instPtrAnnotation = createAnnotation(annModel, position, resource, thread, stackFrame);
		} catch (CoreException e) {
			return false;
		}
		
		annModel.addAnnotation(instPtrAnnotation, position);
		
		IDebugTarget debugTarget = stackFrame.getDebugTarget();
		Map threadMap = (Map)fDebugTargetMap.get(debugTarget);
		if (threadMap == null) {
			threadMap = new HashMap();	
			fDebugTargetMap.put(debugTarget, threadMap);		
		}
		List contextList = (List)threadMap.get(thread);
		if (contextList == null) {
			contextList = new ArrayList();
			threadMap.put(thread, contextList);
		}
		
		PInstructionPointerContext context = new PInstructionPointerContext(textEditor, instPtrAnnotation);
		contextList.remove(context);
		contextList.add(context);
		return true;
	}
	
	public IEditorPart openEditor(final IWorkbenchPage page, final IEditorInput input, final String id) {
		final IEditorPart[] editor = new IEditorPart[] {null};
		Runnable r = new Runnable() {
			public void run() {
				try {
					editor[0] = page.openEditor(input, id, false);
				} catch (PartInitException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot open editor", e);
				}					
			}
		}; 
		BusyIndicator.showWhile(PTPDebugUIPlugin.getDisplay(), r);
		return editor[0];
	}	
	
	//FIXME hard the CEditor id
	public IEditorPart openEditor(IFile file) {
		String fileExt = file.getFileExtension();
		if (!fileExt.equals("c") && !fileExt.equals("cpp"))
			return null;
		
		String id = "org.eclipse.cdt.ui.editor.CEditor";
		IWorkbenchPage page = PTPDebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = page.getActiveEditor();
		if (editor != null) {
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof IFileEditorInput)
				if (((IFileEditorInput)editorInput).getFile().equals(file)) {
					page.bringToTop(editor);
					return editor;
				}
		}
		
		if (editor == null) {
			IEditorReference[] refs = page.getEditorReferences();
			for (int i = 0; i < refs.length; i++) {
				IEditorPart refEditor = refs[i].getEditor(false);
				IEditorInput editorInput = refEditor.getEditorInput();
				if (editorInput instanceof IFileEditorInput) {
					if (((IFileEditorInput)editorInput).getFile().equals(file)) {
						editor = refEditor;
						page.bringToTop(editor);
						return editor;
					}
				}
			}
		}
		return openEditor(page, new FileEditorInput(file), id);
	}
	public IFile findFile(String fullPathFileName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return workspaceRoot.getFile(new Path(fullPathFileName));
	}
	public ITextEditor getTextEditor(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor)				
			return (ITextEditor)editorPart;

		return (ITextEditor) editorPart.getAdapter(ITextEditor.class);
	}
	public IFile getResource(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput)
			return ((IFileEditorInput)editorInput).getFile();

		return null;
	}
	public Position createPosition(int lineNumber, IDocument doc) {
		if (doc == null)
			return null;
		
		try {
			IRegion region = doc.getLineInformation(lineNumber);
			int charStart = region.getOffset();
			int length = region.getLength();
			if (charStart < 0)
				return null;

			return new Position(charStart, length);
		} catch (BadLocationException ble) {
			return null;
		}
	}
	public IMarker createMarker(IResource resource, String type) throws CoreException {
		return resource.createMarker(type);
	}
	public void addAnnotation(String fullPathFileName, int lineNumber, int[] taskId) throws CoreException {
		IFile file = findFile(fullPathFileName);
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IEditorPart editor = openEditor(file);
		if (editor == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		addAnnotation(editor, file, lineNumber, taskId);
	}
	
	public void addAnnotation(IEditorPart editor, IFile file, int lineNumber, int[] taskId) throws CoreException {
		ITextEditor textEditor = getTextEditor(editor);
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IAnnotationModel annotationModel = docProvider.getAnnotationModel(editor.getEditorInput());
		if (annotationModel == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		Position position = createPosition(lineNumber, docProvider.getDocument(editor.getEditorInput()));
		if (position == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		
	}
	
	public Annotation createAnnotation(IAnnotationModel annotationModel, Position position, IFile file, int lineNumber, String type) {
		PInstructionPointerAnnotation ptrAnnotation = null;
		for (Iterator i=annotationModel.getAnnotationIterator(); i.hasNext();) {
			Annotation annotation = (Annotation)i.next();
			if (annotation instanceof PInstructionPointerAnnotation) {
				if (annotationModel.getPosition(annotation).equals(position)) {
					ptrAnnotation = (PInstructionPointerAnnotation)annotation;
					annotationModel.removeAnnotation(annotation);
					break;
				}
			}
		}
		
		if (ptrAnnotation == null) {
			IMarker marker = createMarker(file, type);
			ptrAnnotation = new PInstructionPointerAnnotation(marker, stackFrame);
		}
		createStackFrameMessage(ptrAnnotation.getMarker(), stackFrame, appendMsg);
		return ptrAnnotation;
	}
	
	public void createStackFrameMessage(IMarker marker, IStackFrame stackFrame, boolean append) throws CoreException {
		//msg is task id
		String msg = stackFrame.getThread().getName();
		if (append) {
			String markerMsg = marker.getAttribute(IMarker.MESSAGE, "");
			msg = markerMsg + "," + msg; 
		}
		marker.setAttribute(IMarker.MESSAGE, msg);
	}
	
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		IDebugTarget debugTarget = thread.getDebugTarget();
		Map threadMap = (Map)fDebugTargetMap.get(debugTarget);
		if (threadMap != null) {
			removeAnnotations(thread, threadMap);
		}
	}
	private void removeAnnotations(IThread thread, Map threadMap) {
		List contextList = (List)threadMap.get(thread);
		if (contextList != null) {
			Iterator contextIterator = contextList.iterator();
			while (contextIterator.hasNext()) {
				PInstructionPointerContext context = (PInstructionPointerContext) contextIterator.next();
				removeAnnotation(context.getTextEditor(), context.getAnnotation());
			}
		}
		threadMap.remove(thread);						
	}
	private void removeAnnotation(ITextEditor textEditor, Annotation annotation) {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		if (docProvider != null) {
			IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
			if (annotationModel != null) {
				annotationModel.removeAnnotation(annotation);
			}
		}
	}
	
	public void removeAllAnnotations() {
		for (Iterator i=fDebugTargetMap.values().iterator(); i.hasNext();) {
			Map threadMap = (Map)i.next();
			if (threadMap != null) {
				for (Iterator j=threadMap.values().iterator(); j.hasNext();) {
					List contextList = (List)j.next();
					if (contextList != null) {
						Iterator contextIterator = contextList.iterator();
						while (contextIterator.hasNext()) {
							PInstructionPointerContext context = (PInstructionPointerContext) contextIterator.next();
							removeAnnotation(context.getTextEditor(), context.getAnnotation());
						}
					}
				}
				threadMap.clear();
			}
		}
		fDebugTargetMap.clear();
	}
}
