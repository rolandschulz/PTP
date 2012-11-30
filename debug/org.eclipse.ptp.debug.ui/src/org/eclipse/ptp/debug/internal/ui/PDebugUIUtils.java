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

import java.io.File;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.internal.core.sourcelookup.PSourceNotFoundElement;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Clement chu
 * 
 */
public class PDebugUIUtils {
	/**
	 * Show tasks with standard format
	 * 
	 * @param array
	 * @return
	 */
	public static String arrayToString(int[] array) {
		if (array.length == 0) {
			return ""; //$NON-NLS-1$
		}
		String msg = ""; //$NON-NLS-1$
		int preTask = array[0];
		msg += preTask;
		boolean isContinue = false;
		for (int i = 1; i < array.length; i++) {
			if (preTask == (array[i] - 1)) {
				preTask = array[i];
				isContinue = true;
				if (i == (array.length - 1)) {
					msg += "-" + array[i]; //$NON-NLS-1$
					break;
				}
				continue;
			}
			if (isContinue) {
				msg += "-" + preTask; //$NON-NLS-1$
			}
			msg += "," + array[i]; //$NON-NLS-1$
			isContinue = false;
			preTask = array[i];
		}
		return msg;
	}

	/**
	 * Find region by given offset in given document
	 * 
	 * @param document
	 * @param offset
	 * @return
	 */
	public static IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;
		try {
			int pos = offset;
			char c;
			while (pos >= 0) {
				c = document.getChar(pos);
				// TODO check java char?
				if (!Character.isJavaIdentifierPart(c)) {
					break;
				}
				--pos;
			}
			start = pos;
			pos = offset;
			int length = document.getLength();
			while (pos < length) {
				c = document.getChar(pos);
				// TODO check java char?
				if (!Character.isJavaIdentifierPart(c)) {
					break;
				}
				++pos;
			}
			end = pos;
		} catch (BadLocationException x) {
		}
		if (start > -1 && end > -1) {
			if (start == offset && end == offset) {
				return new Region(offset, 0);
			} else if (start == offset) {
				return new Region(start, end - start);
			} else {
				return new Region(start + 1, end - start - 1);
			}
		}
		return null;
	}

	/**
	 * Get current stack frame
	 * 
	 * @return
	 */
	public static IPStackFrame getCurrentStackFrame() {
		IAdaptable context = DebugUITools.getDebugContext();
		return (context != null) ? (IPStackFrame) context.getAdapter(IPStackFrame.class) : null;
	}

	public static String getEditorId(IEditorInput input, Object element) {
		if (element instanceof PSourceNotFoundElement) {
			return ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
		}
		if (input != null) {
			return getEditorId(input);
		}
		return null;
	}

	public static String getEditorId(Object element) {
		String name = null;
		if (element instanceof IFile) {
			name = ((IFile) element).getName();
		}
		if (element instanceof IEditorInput) {
			name = ((IEditorInput) element).getName();
		}
		if (name != null) {
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor descriptor = registry.getDefaultEditor(name);
			return (descriptor != null) ? descriptor.getId() : CUIPlugin.EDITOR_ID;
		}
		return ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
	}

	public static IEditorInput getEditorInput(Object element) {
		if (element instanceof IMarker) {
			IResource resource = ((IMarker) element).getResource();
			if (resource instanceof IFile) {
				return new FileEditorInput((IFile) resource);
			}
		}
		if (element instanceof IFile) {
			return new FileEditorInput((IFile) element);
		}
		if (element instanceof IPBreakpoint) {
			IPBreakpoint b = (IPBreakpoint) element;
			IFile file = null;
			try {
				String handle = b.getSourceHandle();
				if (handle != null) {
					IPath path = new Path(handle);
					if (path.isValidPath(handle)) {
						IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
						if (files.length > 0) {
							file = files[0];

							// now try to match any finding to the project in
							// the breakpoint
							IProject project = b.getMarker().getResource().getProject();
							for (IFile f : files) {
								if (f.getProject().equals(project)) {
									file = f;
									break;
								}
							}
						} else {
							File fsfile = new File(handle);
							if (fsfile.isFile() && fsfile.exists()) {
								// create an ExternalEditorInput with an
								// external tu so when you
								// open the file from the breakpoints view it
								// opens in the
								// proper editor.
								IProject project = b.getMarker().getResource().getProject();
								if (project != null) {
									ICProject cproject = CoreModel.getDefault().create(project);
									String id = CoreModel.getRegistedContentTypeId(project, path.lastSegment());
									ExternalTranslationUnit tu = new ExternalTranslationUnit(cproject, URIUtil.toURI(path), id);
									return new ExternalEditorInput(tu);
								} else {
									return new ExternalEditorInput(path);
								}
							}
						}
					}
				}
			} catch (CoreException e) {
			}
			if (file == null) {
				file = (IFile) b.getMarker().getResource().getAdapter(IFile.class);
			}
			if (file != null) {
				return new FileEditorInput(file);
			}
		}
		if (element instanceof LocalFileStorage) {
			return new ExternalEditorInput(((IStorage) element).getFullPath());
		}
		if (element instanceof PSourceNotFoundElement) {
			return new CommonSourceNotFoundEditorInput(element);
		}
		return null;
	}

	// self testing
	public static void main(String[] args) {
		TaskSet tasks = new TaskSet(0);
		System.out.println(showBitList(tasks));
	}

	/**
	 * Show tasks with standard format
	 * 
	 * @param tasks
	 * @return
	 */
	public static String showBitList(TaskSet tasks) {
		if (tasks == null || tasks.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		return arrayToString(tasks.toArray());
	}
}
