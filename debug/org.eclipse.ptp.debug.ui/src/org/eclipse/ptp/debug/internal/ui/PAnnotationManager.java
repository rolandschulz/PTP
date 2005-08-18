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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
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
		
	private ITextEditor getTextEditor(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor)				
			return (ITextEditor)editorPart;

		return (ITextEditor) editorPart.getAdapter(ITextEditor.class);
	}
	public IResource getResource(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput)
			return ((IFileEditorInput)editorInput).getFile();

		return null;
	}
	public IStackFrame getTopStackFrame(IThread thread) {
		try {
			return thread.getTopStackFrame();
		} catch (DebugException de) {
			return null;
		}
	}
	public IMarker createMarker(IResource resource, IThread thread, IStackFrame stackFrame) throws CoreException {
		IStackFrame topStack = getTopStackFrame(thread);
		return resource.createMarker((topStack == null || stackFrame.equals(topStack))?IPTPDebugUIConstants.ANN_INSTR_POINTER_CURRENT:IPTPDebugUIConstants.ANN_INSTR_POINTER_SECONDARY);
	}
	public Position createPosition(IStackFrame stackFrame, IDocument doc) {
		if (doc == null)
			return null;
		
		int charStart = -1;
		int length = -1; 
		try {
			charStart = stackFrame.getCharStart();
			length = stackFrame.getCharEnd() - charStart;
		} catch (DebugException de) {}

		if (charStart < 0) {
			try {
				int lineNumber = stackFrame.getLineNumber() - 1;
				IRegion region = doc.getLineInformation(lineNumber);
				charStart = region.getOffset();
				length = region.getLength();
			} catch (BadLocationException ble) {
				return null;
			} catch (DebugException de) {
				return null;
			}
		}
		if (charStart < 0)
			return null;

		return new Position(charStart, length);
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
	
	private Annotation createAnnotation(IAnnotationModel annModel, Position position, IResource resource, IThread thread, IStackFrame stackFrame) throws CoreException {
		boolean appendMsg = false;
		PInstructionPointerAnnotation ptrAnnotation = null;
		for (Iterator i=annModel.getAnnotationIterator(); i.hasNext();) {
			Annotation annotation = (Annotation)i.next();
			if (annotation instanceof PInstructionPointerAnnotation) {
				if (annModel.getPosition(annotation).equals(position)) {
					ptrAnnotation = (PInstructionPointerAnnotation)annotation;
					annModel.removeAnnotation(annotation);
					appendMsg = true;
					break;
				}
			}
		}
		
		if (ptrAnnotation == null) {
			IMarker marker = createMarker(resource, thread, stackFrame);
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
