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
import java.util.Collections;
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
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPDebugEventListener;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.events.IPDebugEvent;
import org.eclipse.ptp.debug.core.events.IPDebugInfo;
import org.eclipse.ptp.debug.core.events.IPDebugSuspendInfo;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Clement chu
 * 
 */
public class PAnnotationManager implements IJobChangedListener, IPDebugEventListener {
	private static PAnnotationManager instance = null;
	protected UIDebugManager uiDebugManager = null;
	private Map annotationMap = Collections.synchronizedMap(new HashMap());

	/** Constructor
	 * @param uiDebugManager
	 */
	public PAnnotationManager(UIDebugManager uiDebugManager) {
		this.uiDebugManager = uiDebugManager;
		uiDebugManager.addJobChangedListener(this);
		PTPDebugCorePlugin.getDefault().addDebugEventListener(this);
		// make sure every time created the same object reference
		if (instance == null)
			instance = this;
	}
	/** Get instance of PAnnotationManager
	 * @return
	 */
	public static PAnnotationManager getDefault() {
		if (instance == null)
			instance = new PAnnotationManager(PTPDebugUIPlugin.getUIDebugManager());
		return instance;
	}
	/** Clean all settings and listeners
	 * 
	 */
	public void shutdown() {
		uiDebugManager.removeJobChangedListener(this);
		PTPDebugCorePlugin.getDefault().removeDebugEventListener(this);
		clearAllAnnotations();
		annotationMap = null;
	}
	/** Clean all stored annotations
	 * 
	 */
	public synchronized void clearAllAnnotations() {
		for (Iterator i = annotationMap.values().iterator(); i.hasNext();) {
			((AnnotationGroup) i.next()).removeAnnotations();
		}
		annotationMap.clear();
	}
	/** Remove annotation group by given job
	 * @param job_id job ID
	 */
	public synchronized void removeAnnotationGroup(String job_id) {
		AnnotationGroup annotationGroup = (AnnotationGroup) annotationMap.remove(job_id);
		if (annotationGroup != null) {
			annotationGroup.removeAllMarkers();
			annotationGroup.clear();
		}
	}
	/** Add annotation to given job
	 * @param job_id Job ID
	 * @param annotationGroup
	 */
	public void putAnnotationGroup(String job_id, AnnotationGroup annotationGroup) {
		annotationMap.put(job_id, annotationGroup);
	}
	/** Get annotation by given hob ID
	 * @param job_id job ID
	 * @return
	 */
	public AnnotationGroup getAnnotationGroup(String job_id) {
		return (AnnotationGroup) annotationMap.get(job_id);
	}
	/** Get top stack frame of given thread
	 * @param thread
	 * @return
	 */
	protected IStackFrame getTopStackFrame(IThread thread) {
		try {
			return thread.getTopStackFrame();
		} catch (DebugException de) {
			return null;
		}
	}
	/** Get editor and open editor if it is not opened or focus on it if it is already opened
	 * @param page
	 * @param input
	 * @param id
	 * @return
	 */
	protected IEditorPart openEditor(final IWorkbenchPage page, final IEditorInput input, final String id) {
		final IEditorPart[] editor = new IEditorPart[] { null };
		Runnable r = new Runnable() {
			public void run() {
				try {
					editor[0] = page.openEditor(input, id, false);
				} catch (PartInitException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot open editor", e);
				}
			}
		};
		BusyIndicator.showWhile(Display.getDefault(), r);
		return editor[0];
	}
	/** Get editor part
	 * @param file
	 * @return
	 */
	protected IEditorPart getEditorPart(final IFile file) {
		String fileExt = file.getFileExtension();
		// FIXME hard the extension
		if (fileExt == null || (!fileExt.equals("c") && !fileExt.equals("cpp")))
			return null;
		final IEditorPart[] editor = new IEditorPart[] { null };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				// FIXME hard the CEditor id
				final String id = "org.eclipse.cdt.ui.editor.CEditor";
				IWorkbenchPage page = PTPDebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
				IEditorPart editorPart = page.getActiveEditor();
				if (editorPart != null) {
					IEditorInput editorInput = editorPart.getEditorInput();
					if (editorInput instanceof IFileEditorInput) {
						if (((IFileEditorInput) editorInput).getFile().equals(file)) {
							page.bringToTop(editorPart);
							editor[0] = editorPart;
							return;
						}
					}
				}
				if (editor == null) {
					IEditorReference[] refs = page.getEditorReferences();
					for (int i = 0; i < refs.length; i++) {
						IEditorPart refEditor = refs[i].getEditor(false);
						IEditorInput editorInput = refEditor.getEditorInput();
						if (editorInput instanceof IFileEditorInput) {
							if (((IFileEditorInput) editorInput).getFile().equals(file)) {
								page.bringToTop(refEditor);
								editor[0] = refEditor;
								return;
							}
						}
					}
				}
				try {
					editor[0] = page.openEditor(new FileEditorInput(file), id, false);
				} catch (PartInitException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot open editor", e);
				}
			}
		});
		return editor[0];
	}
	/** Find file
	 * @param fileName
	 * @return
	 */
	protected IFile findFile(String fileName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = new Path(fileName);
		if (path.isAbsolute())
			return workspaceRoot.getFileForLocation(path);
		return workspaceRoot.getFile(path);
	}
	/** Get text editor
	 * @param editorPart
	 * @return
	 */
	protected ITextEditor getTextEditor(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor)
			return (ITextEditor) editorPart;
		return (ITextEditor) editorPart.getAdapter(ITextEditor.class);
	}
	/** Get file
	 * @param editorInput
	 * @return
	 */
	protected IFile getFile(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput)
			return ((IFileEditorInput) editorInput).getFile();
		return null;
	}
	/** Create poistion in the source file
	 * @param lineNumber
	 * @param doc
	 * @return
	 */
	protected Position createPosition(int lineNumber, IDocument doc) {
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
	/** Get tasks from debug target
	 * @param debugTarget
	 * @return
	 */
	public BitList getTasks(IDebugTarget debugTarget) {
		if (debugTarget instanceof IPDebugTarget) {
			int taskId = ((IPDebugTarget) debugTarget).getTargetID();
			if (taskId == -1)
				return null;
			// FIXME it later
			BitList tasks = new BitList(100);
			tasks.set(taskId);
			return tasks;
		}
		return null;
	}
	/** Get tasks from thread
	 * @param thread
	 * @return
	 */
	public BitList getTasks(IThread thread) {
		return getTasks(thread.getDebugTarget());
	}
	/** Get tasks from stack frame
	 * @param stackFrame
	 * @return
	 */
	public BitList getTasks(IStackFrame stackFrame) {
		return getTasks(stackFrame.getDebugTarget());
	}
	/** Is given type register type
	 * @param type
	 * @return
	 */
	private boolean isRegisterType(String type) {
		return (type.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT) || type.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY));
	}
	/** Focus to annotation in source editor
	 * @param editorPart
	 * @param stackFrame
	 * @throws CoreException
	 */
	public void focusAnnotation(IEditorPart editorPart, IStackFrame stackFrame) throws CoreException {
		ITextEditor textEditor = getTextEditor(editorPart);
		int charStart = stackFrame.getCharStart();
		if (charStart > 0) {
			textEditor.selectAndReveal(charStart, 0);
			return;
		}
		int lineNumber = stackFrame.getLineNumber();
		lineNumber--;
		IRegion region= getLineInformation(textEditor, lineNumber);
		if (region != null) {
			textEditor.selectAndReveal(region.getOffset(), 0);
		}
	}
	/** Get line region in source editor
	 * @param editor
	 * @param lineNumber
	 * @return
	 */
	private IRegion getLineInformation(ITextEditor editor, int lineNumber) {
		IDocumentProvider provider= editor.getDocumentProvider();
		IEditorInput input= editor.getEditorInput();
		try {
			provider.connect(input);
		} catch (CoreException e) {
			return null;
		}
		try {
			IDocument document= provider.getDocument(input);
			if (document != null)
				return document.getLineInformation(lineNumber);
		} catch (BadLocationException e) {
		} finally {
			provider.disconnect(input);
		}
		return null;
	}
	// called by debug view
	/** Add annotation called from Debug View
	 * @param editorPart
	 * @param stackFrame
	 * @throws CoreException
	 */
	public synchronized void addAnnotation(IEditorPart editorPart, IStackFrame stackFrame) throws CoreException {
		int lineNumber = stackFrame.getLineNumber();
		if (lineNumber > -1) {
			String job_id = uiDebugManager.getCurrentJobId();
			if (job_id.length() == 0)
				return;
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
			String type = (tos == null || stackFrame.equals(tos)) ? IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT : IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY;			
			AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
			if (annotationGroup == null) {
				annotationGroup = new AnnotationGroup();
				putAnnotationGroup(job_id, annotationGroup);
			}
			synchronized (tasks) {
				PInstructionPointerAnnotation annotation = findAnnotation(annotationGroup, tasks);
				if (annotation == null) {
					addAnnotation(annotationGroup, textEditor, file, lineNumber, tasks, type);
				}
				else {
					textEditor.selectAndReveal(annotation.getPosition().getOffset(), 0);
				}
			}
		}
	}
	// called by event
	/** Add annotation called from debug event
	 * @param job_id
	 * @param fullPathFileName
	 * @param lineNumber
	 * @param tasks
	 * @param isRegister
	 * @throws CoreException
	 */
	public synchronized void addAnnotation(String job_id, String fullPathFileName, int lineNumber, BitList tasks, boolean isRegister) throws CoreException {
		if (lineNumber > -1) {
			if (tasks.isEmpty())
				return;
			IFile file = findFile(fullPathFileName);
			if (file == null)
				throw new CoreException(Status.CANCEL_STATUS);
			IEditorPart editorPart = getEditorPart(file);
			if (editorPart == null)
				throw new CoreException(Status.CANCEL_STATUS);
			ITextEditor textEditor = getTextEditor(editorPart);
			if (textEditor == null)
				throw new CoreException(Status.CANCEL_STATUS);
			
			String type = isRegister ? IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT : ((containsCurrentSet(tasks)) ? IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT : IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT);
			AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
			if (annotationGroup == null) {
				annotationGroup = new AnnotationGroup();
				putAnnotationGroup(job_id, annotationGroup);
			}
			synchronized (tasks) {
				//if (uiDebugManager.getCurrentJobId().equals(job_id)) {
					addAnnotation(annotationGroup, textEditor, file, lineNumber, tasks, type);
				//}
				// remove marker if it is not in current job
				//if (!uiDebugManager.getCurrentJobId().equals(job_id)) {
					//annotationGroup.removeAllMarkers();
				//}
			}
		}
	}
	/** Is given tasks in the current set
	 * @param aTasks
	 * @return
	 */
	public boolean containsCurrentSet(BitList aTasks) {
		String set_id = uiDebugManager.getCurrentSetId();
		if (set_id.equals(IElementHandler.SET_ROOT_ID))
			return true;
		BitList tasks = PTPDebugCorePlugin.getDebugModel().getTasks(uiDebugManager.getCurrentJobId(), set_id);
		return (tasks != null && tasks.intersects(aTasks));
	}
	// generic
	/** Add annotation
	 * @param annotationGroup
	 * @param textEditor
	 * @param file
	 * @param lineNumber
	 * @param tasks
	 * @param type
	 * @throws CoreException
	 */
	public synchronized void addAnnotation(AnnotationGroup annotationGroup, final ITextEditor textEditor, final IFile file, final int lineNumber, final BitList tasks, final String type) throws CoreException {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
		if (annotationModel == null)
			throw new CoreException(Status.CANCEL_STATUS);
		final Position position = createPosition(lineNumber, docProvider.getDocument(textEditor.getEditorInput()));
		if (position == null)
			throw new CoreException(Status.CANCEL_STATUS);
		boolean isRegister = isRegisterType(type);
		PInstructionPointerAnnotation annotation = findAnnotation(annotationGroup, position, type);
		if (annotation == null) {
			IMarker marker = annotationGroup.createMarker(file, type);
			annotation = new PInstructionPointerAnnotation(marker, position, annotationModel);
			annotationGroup.addAnnotation(annotation);
			annotationModel.addAnnotation(annotation, position);
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				textEditor.selectAndReveal(position.getOffset(), 0);
			}
		});
		annotation.addTasks(tasks);
		annotation.setMessage(isRegister);
	}
	/** Find annotation
	 * @param annotationGroup
	 * @param position
	 * @param type
	 * @return
	 */
	public PInstructionPointerAnnotation findAnnotation(AnnotationGroup annotationGroup, Position position, String type) {
		for (Iterator i = annotationGroup.getAnnotationIterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation) i.next();
			if (annotation.getPosition().equals(position)) {
				String annotationType = annotation.getType();
				if (annotationType.equals(type)) {
					return (PInstructionPointerAnnotation) annotation;
				}
				/*
				 * if (!isRegister) { if (annotationType.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT) || annotationType.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT)) {
				 * return (PInstructionPointerAnnotation) annotation; } }
				 */
			}
		}
		return null;
	}
	// called by debug view
	/** Remove annotation called from Debug View
	 * @param editorPart
	 * @param thread
	 * @throws CoreException
	 */
	public void removeAnnotation(IEditorPart editorPart, IThread thread) throws CoreException {
		BitList tasks = getTasks(thread);
		if (tasks == null)
			throw new CoreException(Status.CANCEL_STATUS);
		String job_id = uiDebugManager.getCurrentJobId();
		AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
		if (annotationGroup != null) {
			synchronized (tasks) {
				removeAnnotation(annotationGroup, tasks);
				if (annotationGroup.isEmpty())
					removeAnnotationGroup(job_id);
			}
		}
	}
	// called by event
	/** Remove annotation called from debug event
	 * @param job_id
	 * @param tasks
	 * @throws CoreException
	 */
	public void removeAnnotation(String job_id, BitList tasks) throws CoreException {
		if (tasks == null || tasks.isEmpty())
			return;
		AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
		if (annotationGroup != null) {
			synchronized (tasks) {
				removeAnnotation(annotationGroup, tasks);
				if (annotationGroup.isEmpty())
					removeAnnotationGroup(job_id);
			}
		}
	}
	// generic
	/** Remove annotation
	 * @param annotationGroup
	 * @param tasks
	 * @throws CoreException
	 */
	public synchronized void removeAnnotation(AnnotationGroup annotationGroup, BitList tasks) throws CoreException {
		List removedList = new ArrayList(0);
		for (Iterator i = annotationGroup.getAnnotationIterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation) i.next();
			annotation.removeTasks(tasks);
			if (annotation.isEmpty()) {
				if (annotation.deleteMarker())
					removedList.add(annotation);
			} else {
				annotation.setMessage(isRegisterType(annotation.getType()));
			}
		}
		annotationGroup.removeAnnotations(removedList);
	}
	/** Get iterator of stored annotations
	 * @param annotationGroup
	 * @param type
	 * @return
	 */
	public Iterator findAnnotationIterator(AnnotationGroup annotationGroup, String type) {
		List annotations = new ArrayList();
		for (Iterator i = annotationGroup.getAnnotationIterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation) i.next();
			if (annotation.getType().equals(type)) {
				annotations.add(annotation);
			}
		}
		return annotations.iterator();
	}
	/** Get position of annotation
	 * @param annotationGroup
	 * @param tasks
	 * @return
	 */
	public Position findAnnotationPosition(AnnotationGroup annotationGroup, BitList tasks) {
		PInstructionPointerAnnotation annotation = findAnnotation(annotationGroup, tasks);
		if (annotation != null)
			return annotation.getPosition();
		return null;
	}
	public synchronized PInstructionPointerAnnotation findAnnotation(AnnotationGroup annotationGroup, BitList tasks) {
		if (tasks.isEmpty())
			return null;
		for (Iterator i = annotationGroup.getAnnotationIterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation) i.next();
			if (annotation.contains(tasks))
				return annotation;
		}
		return null;
	}
	/** Find other annotation
	 * @param annotationGroup
	 * @param position
	 * @param isRegister
	 * @return
	 */
	public synchronized PInstructionPointerAnnotation findOtherAnnotation(AnnotationGroup annotationGroup, Position position, boolean isRegister) {
		if (position == null)
			return null;
		for (Iterator i = annotationGroup.getAnnotationIterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation) i.next();
			if (annotation.getPosition().equals(position)) {
				String annotationType = annotation.getType();
				if (isRegister) {
					if (annotationType.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT) || annotationType.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT))
						return annotation;
				} else {
					if (annotationType.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT) || annotationType.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY))
						return annotation;
				}
			}
		}
		return null;
	}
	/** Change annotation type
	 * @param annotation
	 * @param type
	 */
	public void changeAnnotationType(PInstructionPointerAnnotation annotation, String type) {
		if (!annotation.getType().equals(type)) {
			annotation.setType(type);
		}
	}
	// change set
	/** Update annotation
	 * @param currentSet current set
	 * @param preSet previous set
	 * @throws CoreException
	 */
	public void updateAnnotation(final IElementSet currentSet, final IElementSet preSet) {
		WorkbenchJob uiJob = new WorkbenchJob("Updating annotation...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				AnnotationGroup annotationGroup = getAnnotationGroup(uiDebugManager.getCurrentJobId());
				if (annotationGroup != null) {
					BitList tasks = PTPDebugCorePlugin.getDebugModel().getTasks(uiDebugManager.getCurrentJobId(), currentSet.getID());
					for (Iterator i = annotationGroup.getAnnotationIterator(); i.hasNext();) {
						PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation) i.next();
						// change icon for unregistered processes only if the set is changed
						if (!isRegisterType(annotation.getType())) {
							// if all the tasks in current is not match the unregistered tasks, display SET_ANN
							// simply only display SET_ANN when the current set only contain registered tasks
							if (currentSet.isRootSet())
								changeAnnotationType(annotation, IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT);
							else
								changeAnnotationType(annotation, annotation.contains(tasks) ? IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT : IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT);
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	public void register(final BitList tasks) {
		/*
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Update Annotation") {
					protected IStatus run(IProgressMonitor pmonitor) {
					*/
						String job_id = uiDebugManager.getCurrentJobId();
						AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
						if (annotationGroup != null) {
							PInstructionPointerAnnotation annotation = findAnnotation(annotationGroup, tasks);
							if (annotation != null) {
								boolean isRegister = isRegisterType(annotation.getType());
								try {
									if (isRegister)// register annotation
										addToExistedAnnotation(annotationGroup, annotation, tasks, isRegister);
									else // unregister annotation
										removeFromExistedAnnotation(annotationGroup, annotation, tasks, isRegister);
								} catch (CoreException e) {
									PTPDebugUIPlugin.log(e);
									//return e.getStatus();
								}
							}
						}
						/*
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		*/
	}
	public void unregister(final BitList tasks) {
		/*
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Update Annotation") {
					protected IStatus run(IProgressMonitor pmonitor) {
					*/
						String job_id = uiDebugManager.getCurrentJobId();
						AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
						if (annotationGroup != null) {
							PInstructionPointerAnnotation annotation = findAnnotation(annotationGroup, tasks);
							if (annotation != null) {
								boolean isRegister = isRegisterType(annotation.getType());
								try {
									if (isRegister)// register annotation
										removeFromExistedAnnotation(annotationGroup, annotation, tasks, isRegister);
									else // unregister annotation
										addToExistedAnnotation(annotationGroup, annotation, tasks, isRegister);
								} catch (CoreException e) {
									//return e.getStatus();
									PTPDebugUIPlugin.log(e);
								}
							}
						}
						/*
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		*/
	}
	/** Add tasks to existed annotation
	 * @param annotationGroup
	 * @param annotation
	 * @param tasks
	 * @param isRegister
	 * @throws CoreException
	 */
	private synchronized void addToExistedAnnotation(AnnotationGroup annotationGroup, PInstructionPointerAnnotation annotation, BitList tasks, boolean isRegister) throws CoreException {
		IResource file = annotation.getMarker().getResource();
		Position position = annotation.getPosition();
		annotation.addTasks(tasks);
		annotation.setMessage(isRegister);
		PInstructionPointerAnnotation oAnnotation = findOtherAnnotation(annotationGroup, annotation.getPosition(), isRegister);
		if (oAnnotation != null) {
			oAnnotation.removeTasks(tasks);
			if (oAnnotation.isEmpty()) {
				if (oAnnotation.deleteMarker())
					annotationGroup.removeAnnotation(oAnnotation);
			} else
				oAnnotation.setMessage(!isRegister);
		} else {
			String type = isRegister ? IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT : ((containsCurrentSet(tasks)) ? IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT : IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT);
			IMarker marker = annotationGroup.createMarker(file, type);
			oAnnotation = new PInstructionPointerAnnotation(marker, position, annotation.getAnnotationModel());
			annotationGroup.addAnnotation(oAnnotation);
			annotation.getAnnotationModel().addAnnotation(oAnnotation, position);
			oAnnotation.addTasks(tasks);
			oAnnotation.setMessage(!isRegister);
		}
	}
	/** Remove tasks from existed annotation
	 * @param annotationGroup
	 * @param annotation
	 * @param tasks Tasks being removed
	 * @param isRegister
	 * @throws CoreException
	 */
	private synchronized void removeFromExistedAnnotation(AnnotationGroup annotationGroup, PInstructionPointerAnnotation annotation, BitList tasks, boolean isRegister) throws CoreException {
		IResource file = annotation.getMarker().getResource();
		Position position = annotation.getPosition();
		annotation.removeTasks(tasks);
		if (annotation.isEmpty()) {
			if (annotation.deleteMarker())
				annotationGroup.removeAnnotation(annotation);
		} else
			annotation.setMessage(isRegister);

		PInstructionPointerAnnotation oAnnotation = findOtherAnnotation(annotationGroup, annotation.getPosition(), isRegister);
		if (oAnnotation != null) {
			oAnnotation.addTasks(tasks);
			oAnnotation.setMessage(!isRegister);
		} else {
			String type = isRegister ? ((containsCurrentSet(tasks)) ? IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT : IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT) : IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT;
			IMarker marker = annotationGroup.createMarker(file, type);
			oAnnotation = new PInstructionPointerAnnotation(marker, position, annotation.getAnnotationModel());
			annotationGroup.addAnnotation(oAnnotation);
			annotation.getAnnotationModel().addAnnotation(oAnnotation, position);
			oAnnotation.addTasks(tasks);
			oAnnotation.setMessage(!isRegister);
		}
	}
	/***************************************************************************************************************************************************************************************************
	 * Job Change Listener
	 **************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.listeners.IJobChangedListener#jobChangedEvent(int, java.lang.String, java.lang.String)
	 */
	public void jobChangedEvent(final int type, final String cur_job_id, final String pre_job_id) {
		WorkbenchJob uiJob = new WorkbenchJob("Updating annotation...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				doJobChangedEvent(type, cur_job_id, pre_job_id, monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	private void doJobChangedEvent(int type, String cur_job_id, String pre_job_id, IProgressMonitor monitor) {
		if (type == IJobChangedListener.REMOVED || (pre_job_id != null && !pre_job_id.equals(IManager.EMPTY_ID))) {
			AnnotationGroup preAnnotationGroup = getAnnotationGroup(pre_job_id);
			if (preAnnotationGroup != null) {
				preAnnotationGroup.removeAllMarkers();
			}
		}
		if (cur_job_id != null) {
			AnnotationGroup curAnnotationGroup = getAnnotationGroup(cur_job_id);
			if (curAnnotationGroup != null) {
				curAnnotationGroup.retrieveAllMarkers();
			}
		}		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugEventListener#handleDebugEvent(org.eclipse.ptp.debug.core.events.IPDebugEvent)
	 */
	public void handleDebugEvent(final IPDebugEvent event) {
		WorkbenchJob uiJob = new WorkbenchJob("Updating annotation...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				doHandleDebugEvent(event, monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	private void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		IPDebugInfo info = event.getInfo();
		IPJob job = info.getJob();
		switch(event.getKind()) {
			case IPDebugEvent.CREATE:
				switch (event.getDetail()) {
				case IPDebugEvent.REGISTER:
					register(info.getAllRegisteredProcesses());
					break;
				}
				break;
			case IPDebugEvent.TERMINATE:
				switch (event.getDetail()) {
				case IPDebugEvent.DEBUGGER:
					removeAnnotationGroup(job.getIDString());
					break;
				case IPDebugEvent.REGISTER:
					unregister(info.getAllUnregisteredProcesses());
					break;
				default:
					removeAnnotationAction(job, info.getAllProcesses());
					break;
				}
				break;
			case IPDebugEvent.RESUME:
				removeAnnotationAction(job, info.getAllProcesses());
				break;
			case IPDebugEvent.SUSPEND:
				IPDebugSuspendInfo susInfo = (IPDebugSuspendInfo)info; 
				try {
					addAnnotation(job.getIDString(), susInfo.getFilename(), susInfo.getLineNumber(), info.getAllUnregisteredProcesses(), false);
					addAnnotation(job.getIDString(), susInfo.getFilename(), susInfo.getLineNumber(), info.getAllRegisteredProcesses(), true);
				} catch (final CoreException e) {
					PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
						public void run() {
							PTPDebugUIPlugin.errorDialog("Error", e);
						}
					});
				}
				break;
			case IPDebugEvent.CHANGE:
				break;
			case IPDebugEvent.ERROR:
				removeAnnotationAction(job, info.getAllProcesses());
				break;
		}
	}	
	private void removeAnnotationAction(IPJob job, BitList tasks) {
		try {
			removeAnnotation(job.getIDString(), tasks);
		} catch (final CoreException e) {
			PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
				public void run() {
					PTPDebugUIPlugin.errorDialog("Error", e);
				}
			});
		}
	}
}
