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
import java.util.List;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.debug.core.IPDebugEventListener;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.event.IPDebugSuspendInfo;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPThread;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListStackFramesRequest;
import org.eclipse.ptp.debug.core.sourcelookup.PSourceLookupDirector;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugLocator;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugStackFrame;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/*
 * @author Clement chu
 * 
 */
public class PAnnotationManager implements IJobChangedListener, IPDebugEventListener {
	private static PAnnotationManager instance = null;
	private final Object LOCK = new Object();
	protected Map<String, AnnotationGroup> annotationMap = Collections.synchronizedMap(new HashMap<String, AnnotationGroup>());
	protected UIDebugManager uiDebugManager = null;

	/*
	 * Constructor
	 * 
	 * @param uiDebugManager
	 */
	public PAnnotationManager(UIDebugManager uiDebugManager) {
		this.uiDebugManager = uiDebugManager;
		// make sure every time created the same object reference
		if (instance == null) {
			uiDebugManager.addJobChangedListener(this);
			PTPDebugCorePlugin.getDefault().addDebugEventListener(this);
			instance = this;
		}
	}

	/*
	 * Get instance of PAnnotationManager
	 * 
	 * @return
	 */
	public static PAnnotationManager getDefault() {
		if (instance == null) {
			instance = new PAnnotationManager(PTPDebugUIPlugin.getUIDebugManager());
		}
		return instance;
	}

	/*
	 * Clean all settings and listeners
	 */
	public void shutdown() {
		clearAllAnnotations();
		PTPDebugCorePlugin.getDefault().removeDebugEventListener(this);
		uiDebugManager.removeJobChangedListener(this);
		annotationMap = null;
	}

	/*
	 * Clean all stored annotations
	 */
	protected void clearAllAnnotations() {
		synchronized (LOCK) {
			for (AnnotationGroup annotationGroup : annotationMap.values()) {
				annotationGroup.removeAnnotations();
			}
			annotationMap.clear();
		}
	}

	/*
	 * Remove annotation group by given job
	 * 
	 * @param job_id job ID
	 */
	protected void removeAnnotationGroup(String job_id) {
		synchronized (LOCK) {
			AnnotationGroup annotationGroup = annotationMap.remove(job_id);
			if (annotationGroup != null) {
				annotationGroup.removeAnnotations();
			}
		}
	}

	/*
	 * Add annotation to given job
	 * 
	 * @param job_id Job ID
	 * 
	 * @param annotationGroup
	 */
	protected void putAnnotationGroup(String job_id, AnnotationGroup annotationGroup) {
		synchronized (LOCK) {
			annotationMap.put(job_id, annotationGroup);
		}
	}

	/*
	 * Get annotation by given hob ID
	 * 
	 * @param job_id job ID
	 * 
	 * @return
	 */
	protected AnnotationGroup getAnnotationGroup(String job_id) {
		synchronized (LOCK) {
			return annotationMap.get(job_id);
		}
	}

	/*
	 * Get editor and open editor if it is not opened or focus on it if it is
	 * already opened
	 * 
	 * @param page
	 * 
	 * @param input
	 * 
	 * @param id
	 * 
	 * @return
	 */
	protected IEditorPart openEditor(final IWorkbenchPage page, final IEditorInput input, final String id) {
		final IEditorPart[] editor = new IEditorPart[] { null };
		Runnable r = new Runnable() {
			public void run() {
				try {
					editor[0] = page.openEditor(input, id, false);
				} catch (PartInitException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), Messages.PAnnotationManager_0,
							Messages.PAnnotationManager_1, e);
				}
			}
		};
		BusyIndicator.showWhile(Display.getDefault(), r);
		return editor[0];
	}

	/*
	 * Get editor part
	 * 
	 * @param file
	 * 
	 * @return
	 */
	protected IEditorPart getEditorPart(final IFile file) {
		final IEditorPart[] editor = new IEditorPart[] { null };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
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
				if (editor[0] == null) {
					for (IEditorReference refs : page.getEditorReferences()) {
						IEditorPart refEditor = refs.getEditor(false);
						if (refEditor == null) {
							continue;
						}
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
					editor[0] = page.openEditor(PDebugUIUtils.getEditorInput(file), PDebugUIUtils.getEditorId(file), false);
				} catch (PartInitException e) {
					// PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(),
					// "Error", "Cannot open editor", e);
				}
			}
		});
		return editor[0];
	}

	protected void displaySource(final ISourceLookupResult result) {
		UIJob uiJob = new UIJob(Messages.PAnnotationManager_2) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbenchPage page = PTPDebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
				if (!monitor.isCanceled() && result != null && page != null) {
					DebugUITools.displaySource(result, page);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.schedule();
	}

	/*
	 * Find file
	 * 
	 * @param fileName
	 * 
	 * @return
	 */
	protected IFile findFile(String filename) {
		return findFile(new Path(filename));
	}

	protected IFile findFile(IPath location) {
		IPath normalized = FileBuffers.normalizeLocation(location);
		if (normalized.segmentCount() >= 2) {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(normalized);
		}
		return null;
	}

	/*
	 * Get text editor
	 * 
	 * @param editorPart
	 * 
	 * @return
	 */
	protected ITextEditor getTextEditor(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			return (ITextEditor) editorPart;
		}
		return (ITextEditor) editorPart.getAdapter(ITextEditor.class);
	}

	/*
	 * Get file
	 * 
	 * @param editorInput
	 * 
	 * @return
	 */
	protected IFile getFile(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput) editorInput).getFile();
		}
		if (editorInput instanceof IStorageEditorInput) {
			try {
				return findFile(((IStorageEditorInput) editorInput).getStorage().getFullPath());
			} catch (CoreException e) {
				return null;
			}
		}
		if (editorInput instanceof IStorage) {
			return findFile(((IStorage) editorInput).getName());
		}
		return null;
	}

	/*
	 * Create position in the source file
	 * 
	 * @param lineNumber
	 * 
	 * @param doc
	 * 
	 * @return
	 */
	protected Position createPosition(int lineNumber, IDocument doc) {
		if (doc == null) {
			return null;
		}
		try {
			IRegion region = doc.getLineInformation(lineNumber - 1);
			int charStart = region.getOffset();
			int length = region.getLength();
			if (charStart < 0) {
				return null;
			}
			return new Position(charStart, length);
		} catch (BadLocationException ble) {
			return null;
		}
	}

	/*
	 * Get tasks from debug target
	 * 
	 * @param debugTarget
	 * 
	 * @return
	 */
	protected TaskSet getTasks(IDebugTarget debugTarget) {
		if (debugTarget instanceof IPDebugTarget) {
			return ((IPDebugTarget) debugTarget).getTasks();
		}
		return null;
	}

	/*
	 * Get tasks from thread
	 * 
	 * @param thread
	 * 
	 * @return
	 */
	protected TaskSet getTasks(IThread thread) {
		return getTasks(thread.getDebugTarget());
	}

	/*
	 * Is given type register type
	 * 
	 * @param type
	 * 
	 * @return
	 */
	protected boolean isRegisterType(String type) {
		return (type.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT) || type
				.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY));
	}

	/*
	 * Focus to annotation in source editor
	 * 
	 * @param editorPart
	 * 
	 * @param stackFrame
	 * 
	 * @throws CoreException
	 */
	protected void focusAnnotation(IEditorPart editorPart, IStackFrame stackFrame) throws CoreException {
		ITextEditor textEditor = getTextEditor(editorPart);
		int charStart = stackFrame.getCharStart();
		if (charStart > 0) {
			textEditor.selectAndReveal(charStart, 0);
			return;
		}
		int lineNumber = stackFrame.getLineNumber();
		lineNumber--;
		IRegion region = getLineInformation(textEditor, lineNumber);
		if (region != null) {
			textEditor.selectAndReveal(region.getOffset(), 0);
		}
	}

	/*
	 * Get line region in source editor
	 * 
	 * @param editor
	 * 
	 * @param lineNumber
	 * 
	 * @return
	 */
	protected IRegion getLineInformation(ITextEditor editor, int lineNumber) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IEditorInput input = editor.getEditorInput();
		try {
			provider.connect(input);
		} catch (CoreException e) {
			return null;
		}
		try {
			IDocument document = provider.getDocument(input);
			if (document != null) {
				return document.getLineInformation(lineNumber);
			}
		} catch (BadLocationException e) {
		} finally {
			provider.disconnect(input);
		}
		return null;
	}

	// called by debug view - PDebugModelPresentation
	/*
	 * Add annotation called from Debug View
	 * 
	 * @param editorPart
	 * 
	 * @param stackFrame
	 * 
	 * @throws CoreException
	 */
	public void addAnnotation(IEditorPart editorPart, IPStackFrame stackFrame) throws CoreException {
		IPStackFrame selectedFrame = stackFrame;

		TaskSet tasks = getTasks(stackFrame.getDebugTarget());
		if (tasks == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		IFile file = getFile(textEditor.getEditorInput());
		if (file == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		// try to find the stack frame with line number
		if (selectedFrame.getLineNumber() == 0) {
			IStackFrame[] frames = selectedFrame.getThread().getStackFrames();
			for (IStackFrame frame : frames) {
				if (frame.getLineNumber() > 0) {
					selectedFrame = (IPStackFrame) frame;
					break;
				}
			}
		}

		if (selectedFrame.getLineNumber() > 0) {
			String job_id = uiDebugManager.getCurrentJobId();
			synchronized (LOCK) {
				AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
				if (annotationGroup == null) {
					annotationGroup = new AnnotationGroup();
					putAnnotationGroup(job_id, annotationGroup);
				}
				removeAnnotation(annotationGroup, tasks);
				addAnnotation(annotationGroup, textEditor, file, selectedFrame.getLineNumber(), tasks,
						(selectedFrame.getLevel() > 1) ? IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY
								: IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT);
			}
		}
	}

	/**
	 * Get the source locator for this debug launch
	 * 
	 * @param job_id
	 *            id of debug job
	 * @return source locator or null if not found
	 */
	private PSourceLookupDirector getSourceLocator(String job_id) {
		IPSession session = uiDebugManager.getDebugSession(job_id);
		if (session != null) {
			ISourceLocator locator = session.getLaunch().getSourceLocator();
			if (locator instanceof PSourceLookupDirector) {
				return (PSourceLookupDirector) locator;
			}
		}
		return null;
	}

	private IPath getFilePath(String job_id, String filename) {
		PSourceLookupDirector locator = getSourceLocator(job_id);
		if (locator != null) {
			Object object = locator.getSourceElement(filename);
			if (object instanceof IFile) {
				return ((IFile) object).getFullPath();
			}
			if (object instanceof LocalFileStorage) {
				return ((LocalFileStorage) object).getFullPath();
			}
		}
		return new Path(filename);
	}

	// called by event
	/*
	 * Add annotation called from debug event
	 */
	protected void addUnregisterAnnotation(String job_id, int level, String filename, int lineNumber, TaskSet tasks)
			throws CoreException {
		if (tasks.isEmpty()) {
			return;
		}

		IFile file = findFile(getFilePath(job_id, filename));
		if (file == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		IEditorPart editorPart = getEditorPart(file);
		if (editorPart == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		synchronized (LOCK) {
			AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
			if (annotationGroup == null) {
				annotationGroup = new AnnotationGroup();
				putAnnotationGroup(job_id, annotationGroup);
			}
			addAnnotation(annotationGroup, textEditor, file, lineNumber, tasks,
					((level > 1) ? IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT
							: ((containsCurrentSet(tasks)) ? IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT
									: IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT)));
		}
	}

	/**
	 * Add annotation given filename and line number
	 * 
	 * @param job_id
	 * @param level
	 * @param filename
	 * @param lineNumber
	 * @param rTasks
	 * @param uTasks
	 * @throws CoreException
	 */
	protected void addAnnotation(String job_id, int level, String filename, int lineNumber, TaskSet rTasks, TaskSet uTasks)
			throws CoreException {
		IFile file = findFile(getFilePath(job_id, filename));
		if (file == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		IEditorPart editorPart = getEditorPart(file);
		if (editorPart == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		synchronized (LOCK) {
			AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
			if (annotationGroup == null) {
				annotationGroup = new AnnotationGroup();
				putAnnotationGroup(job_id, annotationGroup);
			}
			addAnnotation(annotationGroup, textEditor, file, lineNumber, rTasks,
					((level > 1) ? IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY
							: IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT));
			addAnnotation(annotationGroup, textEditor, file, lineNumber, uTasks,
					((level > 1) ? IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT
							: ((containsCurrentSet(uTasks)) ? IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT
									: IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT)));
		}
	}

	/*
	 * Is given tasks in the current set
	 * 
	 * @param aTasks
	 * 
	 * @return
	 */
	protected boolean containsCurrentSet(TaskSet aTasks) {
		String set_id = uiDebugManager.getCurrentSetId();
		if (set_id.equals(IElementHandler.SET_ROOT_ID)) {
			return true;
		}
		try {
			TaskSet tasks = uiDebugManager.getTasks(set_id);
			return (tasks != null && tasks.intersects(aTasks));
		} catch (CoreException e) {
			return false;
		}
	}

	// generic
	/*
	 * Add annotation
	 * 
	 * @param annotationGroup
	 * 
	 * @param textEditor
	 * 
	 * @param file
	 * 
	 * @param lineNumber
	 * 
	 * @param tasks
	 * 
	 * @param type
	 * 
	 * @throws CoreException
	 */
	protected void addAnnotation(AnnotationGroup annotationGroup, final ITextEditor textEditor, final IFile file,
			final int lineNumber, final TaskSet tasks, final String type) throws CoreException {
		if (!tasks.isEmpty()) {
			IDocumentProvider docProvider = textEditor.getDocumentProvider();
			IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
			if (annotationModel == null) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			final Position position = createPosition(lineNumber, docProvider.getDocument(textEditor.getEditorInput()));
			if (position == null) {
				throw new CoreException(Status.CANCEL_STATUS);
			}

			synchronized (LOCK) {
				PInstructionPointerAnnotation2 annotation = findAnnotation(annotationGroup, position, type);
				if (annotation == null) {
					annotation = new PInstructionPointerAnnotation2(file, type, position, annotationModel);
					annotationGroup.addAnnotation(annotation);
				}
				annotation.addTasks(tasks);
				annotation.setMessage(isRegisterType(type));
			}
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					textEditor.selectAndReveal(position.getOffset(), 0);
				}
			});
		}
	}

	/*
	 * Find annotation
	 * 
	 * @param annotationGroup
	 * 
	 * @param position
	 * 
	 * @param type
	 * 
	 * @return
	 */
	protected PInstructionPointerAnnotation2 findAnnotation(AnnotationGroup annotationGroup, Position position, String type) {
		synchronized (LOCK) {
			for (PInstructionPointerAnnotation2 annotation : annotationGroup.getAnnotations()) {
				if (annotation.getPosition().equals(position)) {
					if (annotation.getType().equals(type)) {
						return annotation;
					}
				}
			}
			return null;
		}
	}

	// called by debug view
	/*
	 * Remove annotation called from Debug View
	 * 
	 * @param editorPart
	 * 
	 * @param thread
	 * 
	 * @throws CoreException
	 */
	protected void removeAnnotation(IEditorPart editorPart, IPThread thread) throws CoreException {
		removeAnnotation(uiDebugManager.getCurrentJobId(), getTasks(thread));
	}

	// called by event
	/*
	 * Remove annotation called from debug event
	 * 
	 * @param job_id
	 * 
	 * @param tasks
	 * 
	 * @throws CoreException
	 */
	protected void removeAnnotation(String job_id, TaskSet tasks) throws CoreException {
		if (tasks == null || tasks.isEmpty()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		synchronized (LOCK) {
			AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
			if (annotationGroup != null) {
				removeAnnotation(annotationGroup, tasks);
				if (annotationGroup.isEmpty()) {
					removeAnnotationGroup(job_id);
				}
			}
		}
	}

	// generic
	/*
	 * Remove annotation
	 * 
	 * @param annotationGroup
	 * 
	 * @param tasks
	 * 
	 * @throws CoreException
	 */
	protected void removeAnnotation(AnnotationGroup annotationGroup, TaskSet tasks) throws CoreException {
		synchronized (LOCK) {
			List<PInstructionPointerAnnotation2> removedList = new ArrayList<PInstructionPointerAnnotation2>(0);
			for (PInstructionPointerAnnotation2 annotation : annotationGroup.getAnnotations()) {
				annotation.removeTasks(tasks);
				if (annotation.isEmpty()) {
					annotation.removeAnnotation();
					removedList.add(annotation);
				} else {
					annotation.setMessage(isRegisterType(annotation.getType()));
				}
			}
			annotationGroup.removeAnnotations(removedList);
		}
	}

	/*
	 * Get iterator of stored annotations
	 * 
	 * @param annotationGroup
	 * 
	 * @param type
	 * 
	 * @return
	 */
	public PInstructionPointerAnnotation2[] findAnnotations(AnnotationGroup annotationGroup, String type) {
		synchronized (LOCK) {
			List<PInstructionPointerAnnotation2> foundAnnotations = new ArrayList<PInstructionPointerAnnotation2>();
			for (PInstructionPointerAnnotation2 annotation : annotationGroup.getAnnotations()) {
				if (annotation.getType().equals(type)) {
					foundAnnotations.add(annotation);
				}
			}
			return foundAnnotations.toArray(new PInstructionPointerAnnotation2[0]);
		}
	}

	protected PInstructionPointerAnnotation2[] findAnnotations(AnnotationGroup annotationGroup, TaskSet tasks) {
		synchronized (LOCK) {
			List<PInstructionPointerAnnotation2> foundAnnotations = new ArrayList<PInstructionPointerAnnotation2>();
			if (tasks.isEmpty()) {
				return new PInstructionPointerAnnotation2[0];
			}

			for (PInstructionPointerAnnotation2 annotation : annotationGroup.getAnnotations()) {
				if (!annotation.isMarkDeleted() && annotation.contains(tasks)) {
					if (!foundAnnotations.contains(annotation)) {
						foundAnnotations.add(annotation);
					}
				}
			}
			return foundAnnotations.toArray(new PInstructionPointerAnnotation2[0]);
		}
	}

	/*
	 * Find other annotation
	 * 
	 * @param annotationGroup
	 * 
	 * @param position
	 * 
	 * @param isRegister
	 * 
	 * @return
	 */
	protected PInstructionPointerAnnotation2 findOtherTypeAnnotation(AnnotationGroup annotationGroup, Position position,
			boolean isRegister) {
		synchronized (LOCK) {
			for (PInstructionPointerAnnotation2 annotation : annotationGroup.getAnnotations()) {
				if (annotation.getPosition().equals(position)) {
					String annotationType = annotation.getType();
					if (isRegister) {
						if (annotationType.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT)
								|| annotationType.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT)) {
							return annotation;
						}
					} else {
						if (annotationType.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT)
								|| annotationType.equals(IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_SECONDARY)) {
							return annotation;
						}
					}
				}
			}
			return null;
		}
	}

	/*
	 * Change annotation type
	 * 
	 * @param annotation
	 * 
	 * @param type
	 */
	protected void changeAnnotationType(PInstructionPointerAnnotation2 annotation, String type) {
		if (!annotation.getType().equals(type)) {
			annotation.setType(type);
		}
	}

	protected void register(String job_id, TaskSet tasks) {
		synchronized (LOCK) {
			AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
			if (annotationGroup != null) {
				for (PInstructionPointerAnnotation2 annotation : findAnnotations(annotationGroup, tasks)) {
					TaskSet cpTasks = tasks.copy();
					cpTasks.and(annotation.getTasks());
					if (cpTasks.isEmpty()) {
						continue;
					}

					updateExistedAnnotation(annotationGroup, annotation, cpTasks, false);
					/*
					 * boolean isRegister =
					 * isRegisterType(annotation.getType()); if (!isRegister)//
					 * unregister annotation
					 * updateExistedAnnotation(annotationGroup, annotation,
					 * cpTasks, isRegister);
					 */
				}
			}
		}
	}

	protected void unregister(String job_id, TaskSet tasks) {
		synchronized (LOCK) {
			AnnotationGroup annotationGroup = getAnnotationGroup(job_id);
			if (annotationGroup != null) {
				for (PInstructionPointerAnnotation2 annotation : findAnnotations(annotationGroup, tasks)) {
					TaskSet cpTasks = tasks.copy();
					cpTasks.and(annotation.getTasks());
					if (cpTasks.isEmpty()) {
						continue;
					}

					updateExistedAnnotation(annotationGroup, annotation, cpTasks, true);
					/*
					 * boolean isRegister =
					 * isRegisterType(annotation.getType()); if (isRegister)//
					 * register annotation
					 * updateExistedAnnotation(annotationGroup, annotation,
					 * cpTasks, isRegister);
					 */
				}
			}
		}
	}

	/*
	 * Remove tasks from existed annotation
	 * 
	 * @param annotationGroup
	 * 
	 * @param annotation
	 * 
	 * @param tasks Tasks being removed
	 * 
	 * @param isRegister
	 * 
	 * @throws CoreException
	 */
	protected void updateExistedAnnotation(AnnotationGroup annotationGroup, PInstructionPointerAnnotation2 annotation,
			TaskSet tasks, boolean isRegister) {
		synchronized (LOCK) {
			IResource file = annotation.getMakerResource();
			Position position = annotation.getPosition();
			annotation.removeTasks(tasks);
			if (annotation.isEmpty()) {
				annotation.removeAnnotation();
				annotationGroup.removeAnnotation(annotation);
			} else {
				annotation.setMessage(isRegister);
			}

			PInstructionPointerAnnotation2 oAnnotation = findOtherTypeAnnotation(annotationGroup, position, isRegister);
			if (oAnnotation != null) {
				oAnnotation.addTasks(tasks);
				oAnnotation.setMessage(!isRegister);
			} else {
				// String type = isRegister ? ((containsCurrentSet(tasks) ?
				// IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT :
				// IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT)) :
				// IPTPDebugUIConstants.REG_ANN_INSTR_POINTER_CURRENT;
				String type = IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT;
				oAnnotation = new PInstructionPointerAnnotation2(file, type, position, annotation.getAnnotationModel());
				annotationGroup.addAnnotation(oAnnotation);
				oAnnotation.addTasks(tasks);
				oAnnotation.setMessage(!isRegister);
			}
		}
	}

	/***************************************************************************************************************************************************************************************************
	 * Set Change Event
	 **************************************************************************************************************************************************************************************************/
	/*
	 * Update annotation
	 * 
	 * @param currentSet current set
	 * 
	 * @param preSet previous set
	 * 
	 * @throws CoreException
	 */
	public void updateAnnotation(final IElementSet currentSet, final IElementSet preSet) {
		WorkbenchJob uiJob = new WorkbenchJob(Messages.PAnnotationManager_3) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				AnnotationGroup annotationGroup = getAnnotationGroup(uiDebugManager.getCurrentJobId());
				if (annotationGroup != null) {
					try {
						TaskSet tasks = uiDebugManager.getTasks(currentSet.getID());
						synchronized (LOCK) {
							for (PInstructionPointerAnnotation2 annotation : annotationGroup.getAnnotations()) {
								// change icon for unregistered processes only
								// if the set is changed
								if (!isRegisterType(annotation.getType())) {
									// if all the tasks in current is not match
									// the unregistered tasks, display SET_ANN
									// simply only display SET_ANN when the
									// current set only contain registered tasks
									if (currentSet.isRootSet()) {
										changeAnnotationType(annotation, IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT);
									} else {
										changeAnnotationType(annotation,
												annotation.contains(tasks) ? IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT
														: IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT);
									}
								}
							}
						}
					} catch (CoreException e) {
						return e.getStatus();
					}
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}

	/***************************************************************************************************************************************************************************************************
	 * Job Change Listener
	 **************************************************************************************************************************************************************************************************/
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.listeners.IJobChangedListener#jobChangedEvent(int,
	 * java.lang.String, java.lang.String)
	 */
	public void jobChangedEvent(final int type, final String cur_job_id, final String pre_job_id) {
		WorkbenchJob uiJob = new WorkbenchJob(Messages.PAnnotationManager_3) {
			@Override
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
		if (type == IJobChangedListener.REMOVED || pre_job_id != null) {
			AnnotationGroup preAnnotationGroup = getAnnotationGroup(pre_job_id);
			if (preAnnotationGroup != null) {
				preAnnotationGroup.throwAllAnnotations();
			}
		}
		if (cur_job_id != null) {
			AnnotationGroup curAnnotationGroup = getAnnotationGroup(cur_job_id);
			if (curAnnotationGroup != null) {
				curAnnotationGroup.retrieveAllAnnontations();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.IPDebugEventListener#handleDebugEvent(org.
	 * eclipse.ptp.debug.core.events.IPDebugEvent)
	 */
	public void handleDebugEvent(final IPDebugEvent event) {
		WorkbenchJob uiJob = new WorkbenchJob(Messages.PAnnotationManager_3) {
			@Override
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

	/**
	 * Process the debug event
	 * 
	 * @param event
	 * @param monitor
	 */
	private void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		IPDebugInfo info = event.getInfo();
		String jobId = info.getLaunch().getJobId();
		switch (event.getKind()) {
		case IPDebugEvent.CREATE:
			switch (event.getDetail()) {
			case IPDebugEvent.REGISTER:
				register(jobId, info.getAllTasks());
				break;
			case IPDebugEvent.BREAKPOINT:
				break;
			}
			break;
		case IPDebugEvent.TERMINATE:
			switch (event.getDetail()) {
			case IPDebugEvent.DEBUGGER:
				removeAnnotationGroup(jobId);
				break;
			case IPDebugEvent.REGISTER:
				unregister(jobId, info.getAllTasks());
				break;
			case IPDebugEvent.BREAKPOINT:
				break;
			default:
				removeAnnotationAction(jobId, info.getAllTasks());
				break;
			}
			break;
		case IPDebugEvent.RESUME:
			removeAnnotationAction(jobId, info.getAllTasks());
			break;
		case IPDebugEvent.SUSPEND:
			IPDebugSuspendInfo susInfo = (IPDebugSuspendInfo) info;
			try {
				addAnnotation(jobId, susInfo);
			} catch (final CoreException e) {
				PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
					public void run() {
						PTPDebugUIPlugin.errorDialog(Messages.PAnnotationManager_0, e);
					}
				});
			}
			break;
		case IPDebugEvent.CHANGE:
			break;
		case IPDebugEvent.ERROR:
			// removeAnnotationAction(job, info.getAllTasks());
			break;
		}
	}

	/**
	 * Add annotation for a suspend event
	 * 
	 * @param jobId
	 *            debug job ID
	 * @param info
	 *            suspend event information
	 * @throws CoreException
	 */
	private void addAnnotation(String jobId, IPDebugSuspendInfo info) throws CoreException {
		int line = info.getLineNumber();
		if (line == 0) {
			// FIXME: this method is only for unreg tasks, reg tasks depends on
			// selection on stack frame on debug view
			addAnnotationWithSourceFound(jobId, info.getAllUnregisteredTasks(), info.getLevel() + 1, info.getDepth());
		} else {
			addAnnotation(jobId, info.getLevel(), info.getFilename(), line, info.getAllRegisteredTasks(),
					info.getAllUnregisteredTasks());
		}
	}

	private void addAnnotationWithSourceFound(String jobId, TaskSet tasks, int low, int high) throws CoreException {
		if (tasks.isEmpty()) {
			return;
		}

		IPSession session = uiDebugManager.getDebugSession(jobId);
		if (session != null) {
			if (!session.isReady()) {
				return;
			}

			IPDIListStackFramesRequest request = session.getPDISession().getRequestFactory()
					.getListStackFramesRequest(session.getPDISession(), tasks, low, high);
			try {
				session.getPDISession().getEventRequestManager().addEventRequest(request);
				Map<TaskSet, Object> map = request.getResultMap(tasks);
				for (TaskSet sTasks : map.keySet()) {
					Object value = map.get(sTasks);
					if (value instanceof ProxyDebugStackFrame[]) {
						ProxyDebugStackFrame[] frames = (ProxyDebugStackFrame[]) value;
						for (ProxyDebugStackFrame frame : frames) {
							ProxyDebugLocator locator = frame.getLocator();
							if (locator.getLineNumber() > 0) {
								addUnregisterAnnotation(jobId, frame.getLevel(), locator.getFile(), locator.getLineNumber(), sTasks);
								break;
							}
						}
					}
				}
			} catch (PDIException e) {
				// throw new CoreException(new Status(IStatus.ERROR,
				// PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
				// e.getMessage(), null));
			}
		}
	}

	private void removeAnnotationAction(String jobId, TaskSet tasks) {
		try {
			removeAnnotation(jobId, tasks);
		} catch (final CoreException e) {
			PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
				public void run() {
					PTPDebugUIPlugin.errorDialog(Messages.PAnnotationManager_0, e);
				}
			});
		}
	}
}
