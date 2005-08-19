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
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
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
	protected UIDebugManager uiDebugManager = null;
	
	public PAnnotationManager() {
		uiDebugManager = PTPDebugUIPlugin.getDefault().getUIDebugManager();
	}
	public static PAnnotationManager getDefault() {
		if (instance == null)
			instance = new PAnnotationManager();
		return instance;
	}
	
	public IStackFrame getTopStackFrame(IThread thread) {
		try {
			return thread.getTopStackFrame();
		} catch (DebugException de) {
			return null;
		}
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
	public IFile getFile(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput)
			return ((IFileEditorInput)editorInput).getFile();

		return null;
	}
	public Position createPosition(int lineNumber, IDocument doc) {
		if (doc == null)
			return null;
		
		try {
			IRegion region = doc.getLineInformation(lineNumber - 1);
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
	
	public BitSet getTaskId(IDebugTarget debugTarget) {
		if (debugTarget instanceof IPDebugTarget) {
			int taskId = ((IPDebugTarget)debugTarget).getTargetId();
			if (taskId == -1)
				return null;
			
			BitSet bitSet = new BitSet();
			bitSet.set(taskId);
			return bitSet;
		}
		return null;
	}
	
	public BitSet getTaskId(IThread thread) {
		return getTaskId(thread.getDebugTarget());
	}
	public BitSet getTaskId(IStackFrame stackFrame) {
		return getTaskId(stackFrame.getDebugTarget());
	}
	
	//called by debug view
	public void addAnnotation(IEditorPart editorPart, IStackFrame stackFrame) throws CoreException {
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IFile file = getFile(textEditor.getEditorInput());
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		BitSet taskId = getTaskId(stackFrame);
		if (taskId == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IStackFrame tos = getTopStackFrame(stackFrame.getThread());
		String type = (tos == null || stackFrame.equals(tos))?IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT: IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY;

		addAnnotation(textEditor, file, stackFrame.getLineNumber(), taskId, type);
	}
	
	public boolean containsCurrentSet(BitSet taskId) {
		String job_id = uiDebugManager.getCurrentJobId();
		String set_id = uiDebugManager.getCurrentSetId();
		IElementHandler handler = uiDebugManager.getElementHandler(job_id);
		IElementSet set = handler.getSet(set_id);
		//FIXME should be improved this checking method
		taskId.cardinality();
		for(int i=taskId.nextSetBit(0); i>=0; i=taskId.nextSetBit(i+1)) {
			if (set.contains(String.valueOf(i)))
				return true;
		}
		return false;
	}
	
	//called by event
	public void addAnnotation(String fullPathFileName, int lineNumber, BitSet taskId) throws CoreException {
		IFile file = findFile(fullPathFileName);
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IEditorPart editorPart = openEditor(file);
		if (editorPart == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		String type = (containsCurrentSet(taskId))?IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT:IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT;		

		addAnnotation(textEditor, file, lineNumber, taskId, type);
	}
	
	public void addAnnotation(ITextEditor textEditor, IFile file, int lineNumber, BitSet taskId, String type) throws CoreException {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
		if (annotationModel == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		Position position = createPosition(lineNumber, docProvider.getDocument(textEditor.getEditorInput()));
		if (position == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		PInstructionPointerAnnotation annotation = findAnnotation(annotationModel, position, taskId);
		if (annotation == null) {
			IMarker marker = createMarker(file, type);
			annotation = new PInstructionPointerAnnotation(marker);
		}
		else {
			annotationModel.removeAnnotation(annotation);
		}

		annotation.addTasks(taskId);
		annotation.setMessage();
		annotationModel.addAnnotation(annotation, position);
	}
	
	public PInstructionPointerAnnotation findAnnotation(IAnnotationModel annotationModel, Position position, BitSet taskId) {
		for (Iterator i=annotationModel.getAnnotationIterator(); i.hasNext();) {
			Annotation annotation = (Annotation)i.next();
			if (annotation instanceof PInstructionPointerAnnotation) {
				if (annotationModel.getPosition(annotation).equals(position)) {
					PInstructionPointerAnnotation pAnnotation = (PInstructionPointerAnnotation)annotation;
					if (pAnnotation.getType().equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT) || pAnnotation.getType().equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY)) {
						if (pAnnotation.contains(taskId))
							return pAnnotation;
						
						continue;
					}					
					return pAnnotation;
				}
			}
		}
		return null;
	}

	//called by debug view
	public void removeAnnotation(IEditorPart editorPart, IThread thread) throws CoreException {
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IFile file = getFile(textEditor.getEditorInput());
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		BitSet taskId = getTaskId(thread);
		if (taskId == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		removeAnnotation(textEditor, file, taskId);
	}
	
	//called by event
	public void removeAnnotation(String fullPathFileName, BitSet taskId) throws CoreException {
		IFile file = findFile(fullPathFileName);
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IEditorPart editorPart = openEditor(file);
		if (editorPart == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
	
		removeAnnotation(textEditor, file, taskId);
	}
	
	public void removeAnnotation(ITextEditor textEditor, IFile file, BitSet taskId) throws CoreException {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
		if (annotationModel == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		for (Iterator i=findAnnotationIterator(annotationModel); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)i.next();
			annotation.removeTasks(taskId);
			Position position = annotationModel.getPosition(annotation);
			
			if (annotation.isEmpty()) {
				removeMarkerAnnotation(annotationModel, annotation);
			} else {
				annotationModel.removeAnnotation(annotation);
				annotation.setMessage();
				annotationModel.addAnnotation(annotation, position);
			}
		}
	}
	
	public void removeMarkerAnnotation(IAnnotationModel annotationModel, PInstructionPointerAnnotation annotation) {
		try {
			annotation.getMarker().delete();
		} catch (CoreException e) {}
		annotationModel.removeAnnotation(annotation);
	}
	
	public Iterator findAnnotationIterator(IAnnotationModel annotationModel) {
		List annotations = new ArrayList();
		for (Iterator i=annotationModel.getAnnotationIterator(); i.hasNext();) {
			Annotation annotation = (Annotation)i.next();
			if (annotation instanceof PInstructionPointerAnnotation) {
				annotations.add(annotation);
			}
		}
		return annotations.iterator();
	}
	
}
