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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.utils.BitList;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.listeners.IRegListener;
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
public class PAnnotationManager implements IRegListener {
	private static PAnnotationManager instance = null;
	protected UIDebugManager uiDebugManager = null;
	private List annotationList = new ArrayList();
	
	public PAnnotationManager(UIDebugManager uiDebugManager) {
		this.uiDebugManager = uiDebugManager;
		uiDebugManager.addRegListener(this);
	}
	public static PAnnotationManager getDefault() {
		if (instance == null)
			instance = new PAnnotationManager(PTPDebugUIPlugin.getDefault().getUIDebugManager());
		return instance;
	}
	
	public void shutdown() {
		uiDebugManager.removeRegListener(this);
		annotationList.clear();
		annotationList = null;
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
	
	public BitList getTasks(IDebugTarget debugTarget) {
		if (debugTarget instanceof IPDebugTarget) {
			int taskId = ((IPDebugTarget)debugTarget).getTargetId();
			if (taskId == -1)
				return null;
			
			BitList tasks = new BitList();
			tasks.set(taskId);
			return tasks;
		}
		return null;
	}
	
	public BitList getTasks(IThread thread) {
		return getTasks(thread.getDebugTarget());
	}
	public BitList getTasks(IStackFrame stackFrame) {
		return getTasks(stackFrame.getDebugTarget());
	}
	
	//called by debug view
	public void addAnnotation(IEditorPart editorPart, IStackFrame stackFrame) throws CoreException {
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IFile file = getFile(textEditor.getEditorInput());
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		BitList tasks = getTasks(stackFrame);
		if (tasks == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IStackFrame tos = getTopStackFrame(stackFrame.getThread());
		String type = (tos == null || stackFrame.equals(tos))?IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT: IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY;

		addAnnotation(textEditor, file, stackFrame.getLineNumber(), tasks, type);
	}
	
	public boolean containsCurrentSet(BitList aTasks) {
		String job_id = uiDebugManager.getCurrentJobId();
		String set_id = uiDebugManager.getCurrentSetId();
		if (set_id.equals(IElementHandler.SET_ROOT_ID))
			return true;
		
		IElementHandler handler = uiDebugManager.getElementHandler(job_id);
		IElementSet set = handler.getSet(set_id);
		BitList tasks = (BitList)set.getData(UIDebugManager.BITSET_KEY);
		return (tasks != null && tasks.intersects(aTasks));
		/*
		taskId.cardinality();
		for(int i=taskId.nextSetBit(0); i>=0; i=taskId.nextSetBit(i+1)) {
			if (set.contains(String.valueOf(i)))
				return true;
		}
		*/
	}
	
	//called by event
	public void addAnnotation(String fullPathFileName, int lineNumber, BitList tasks) throws CoreException {
		IFile file = findFile(fullPathFileName);
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IEditorPart editorPart = openEditor(file);
		if (editorPart == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		String type = (containsCurrentSet(tasks))?IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT:IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT;		

		addAnnotation(textEditor, file, lineNumber, tasks, type);
	}
	
	public void addAnnotation(ITextEditor textEditor, IFile file, int lineNumber, BitList tasks, String type) throws CoreException {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
		if (annotationModel == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		Position position = createPosition(lineNumber, docProvider.getDocument(textEditor.getEditorInput()));
		if (position == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		PInstructionPointerAnnotation annotation = findAnnotation(annotationModel, position, type);
		if (annotation == null) {
			IMarker marker = createMarker(file, type);
			annotation = new PInstructionPointerAnnotation(marker, position);
			annotationList.add(annotation);
			annotationModel.addAnnotation(annotation, position);
		}

		annotation.addTasks(tasks);
		annotation.setMessage();
	}
	
	public PInstructionPointerAnnotation findAnnotation(IAnnotationModel annotationModel, Position position, String type) {
		for (Iterator i=annotationList.iterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)i.next();
			if (annotationModel.getPosition(annotation).equals(position)) {
				String annotationType = annotation.getType();
				if (annotationType.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT) || annotationType.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT))
					return (PInstructionPointerAnnotation)annotation;
				
				if (annotationType.equals(type))
					return (PInstructionPointerAnnotation)annotation;
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
		
		BitList tasks = getTasks(thread);
		if (tasks == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		removeAnnotation(textEditor, file, tasks);
	}
	
	//called by event
	public void removeAnnotation(String fullPathFileName, BitList tasks) throws CoreException {
		IFile file = findFile(fullPathFileName);
		if (file == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IEditorPart editorPart = openEditor(file);
		if (editorPart == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
	
		removeAnnotation(textEditor, file, tasks);
	}
	
	public void removeAnnotation(ITextEditor textEditor, IFile file, BitList tasks) throws CoreException {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
		if (annotationModel == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		for (int i=0; i<annotationList.size(); i++) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)annotationList.get(i);
			annotation.removeTasks(tasks);
			if (annotation.isEmpty()) {
				removeMarkerAnnotation(annotation);
			} else {
				annotation.setMessage();
			}
		}
	}
	
	public void removeMarkerAnnotation(PInstructionPointerAnnotation annotation) {
		try {
			annotation.getMarker().delete();
		} catch (CoreException e) {}
		annotationList.remove(annotation);
	}
	
	public Iterator findAnnotationIterator(String type) {
		List annotations = new ArrayList();
		for (Iterator i=annotationList.iterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)i.next();
			if (annotation.getType().equals(type)) {
				annotations.add(annotation);
			}
		}
		return annotations.iterator();
	}
	public Position findAnnotationPosition(BitList tasks) {
		if (tasks.isEmpty())
			return null;
		
		for (Iterator i=annotationList.iterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)i.next();
			if (annotation.contains(tasks))
				return annotation.getPosition();
		}
		return null;
	}

	public PInstructionPointerAnnotation findAnnotation(BitList tasks) {
		if (tasks.isEmpty())
			return null;
		
		for (Iterator i=annotationList.iterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)i.next();
			if (annotation.contains(tasks))
				return annotation;
		}
		return null;
	}	
	
	public PInstructionPointerAnnotation findUnregAnnotation(Position position) {
		if (position == null)
			return null;
		
		for (Iterator i=annotationList.iterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)i.next();
			String annotationType = annotation.getType();
			if (annotationType.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT) || annotationType.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT))
				if (annotation.getPosition().equals(position))
					return annotation;
		}
		return null;
	}
	
	public void changeAnnotationType(PInstructionPointerAnnotation annotation, String type) {
		if (annotation.getType().equals(type)) {
			annotation.setType(type);
		}
	}

	//change set
	//TODO make sure marker is changed icon if tpye is changed
	public void updateAnnotation(final IElementSet currentSet, final IElementSet preSet) throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Update Annotation") {
					protected IStatus run(IProgressMonitor pmonitor) {
						boolean isRoot = currentSet.isRootSet();
						BitList tasks = (BitList)currentSet.getData(UIDebugManager.BITSET_KEY);					
						for (Iterator i=annotationList.iterator(); i.hasNext();) {
							PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation)i.next();
							String annotationType = annotation.getType();
							if (annotationType.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT) || annotationType.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT)) {
								if (isRoot)
									changeAnnotationType(annotation, IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT);
								else
									changeAnnotationType(annotation, annotation.contains(tasks)?IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT:IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT);
							}
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);		
	}

	/*****
	 * Register listener
	 *****/
	public void register(final BitList tasks) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Update Annotation") {
					protected IStatus run(IProgressMonitor pmonitor) {
						PInstructionPointerAnnotation regAnnotation = findAnnotation(tasks);
						if (regAnnotation != null) {
							PInstructionPointerAnnotation unregAnnotation = findUnregAnnotation(regAnnotation.getPosition());
							if (unregAnnotation != null) {
								unregAnnotation.removeTasks(tasks);
								unregAnnotation.setMessage();
							}
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	public void unregister(final BitList tasks) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Update Annotation") {
					protected IStatus run(IProgressMonitor pmonitor) {
						PInstructionPointerAnnotation regAnnotation = findAnnotation(tasks);
						if (regAnnotation != null) {
							PInstructionPointerAnnotation unregAnnotation = findUnregAnnotation(regAnnotation.getPosition());
							if (unregAnnotation != null) {
								unregAnnotation.addTasks(tasks);
								unregAnnotation.setMessage();
							}
							//remove task id manually
							regAnnotation.removeTasks(tasks);
							regAnnotation.setMessage();
							if (regAnnotation.isEmpty())
								removeMarkerAnnotation(regAnnotation);
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
}
