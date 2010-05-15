/****************************************************************************
 * Copyright (c) 2010, University of Florida
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Max Billingsley III - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.ppw;

import java.io.File;
import java.lang.reflect.Field;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ptp.etfw.AbstractToolDataManager;
import org.eclipse.ptp.etfw.ppw.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class PPWDataManager extends AbstractToolDataManager {
	boolean externalTarget = false;

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "process-PPW"; //$NON-NLS-1$
	}

	@Override
	public void setExternalTarget(boolean external) {
		externalTarget = external;
	}

	@Override
	public void process(String projname, ILaunchConfiguration configuration, final String directory) throws CoreException {
		try {
			// Argument(s) to pass to PPW -- the name of the PAR file to open
			final String[] args = new String[1];

			if (externalTarget || projname == null) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						Shell s = PlatformUI.getWorkbench().getDisplay().getActiveShell();

						if (s == null) {
							s = PlatformUI.getWorkbench().getDisplay().getShells()[0];
						}

						FileDialog dl = new FileDialog(s, SWT.OPEN);
						dl.setFilterPath(directory);
						dl.setFilterExtensions(new String[] { "*.par" }); //$NON-NLS-1$
						dl.setText(Messages.PPWDataManager_0);
						String file = dl.open();
						if (file != null) {
							args[0] = file;
						} else {
							// Dialog was canceled or an error occurred... so
							// just return
							return;
						}
					}
				});
			} else {
				boolean renameSuccess = false;
				File parFile = new File(directory + File.separator + "ppw_eclipse.par"); //$NON-NLS-1$

				File newFile = null;
				final int FILE_CNT_LIM = 64;
				for (int i = 1; i < FILE_CNT_LIM; i++) {
					newFile = new File(directory + File.separator + projname + "_" + i + ".par"); //$NON-NLS-1$ //$NON-NLS-2$
					if (!newFile.exists()) {
						renameSuccess = parFile.renameTo(newFile);
						break;
					}
				}

				if (renameSuccess) {
					args[0] = newFile.getPath();
				} else {
					args[0] = parFile.getPath();
				}
			}

			new PPWController(this, args);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean highlightSourceLine(final String filename, final int line) {
		class SourceView implements Runnable {
			public void run() {
				openSource(filename, line);
			}
		}

		SourceView sv = new SourceView();
		Display.getDefault().syncExec(sv);

		return true;
	}

	/*
	 * Adapted from org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView
	 */
	void openSource(String filename, int line) {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();

			IFile file = getFile(filename, root.members());

			if (file == null) {
				return;
			}
			IEditorInput iEditorInput = new FileEditorInput(file);

			IWorkbenchPage p = getActivePage();
			String editorid = "org.eclipse.cdt.ui.editor.CEditor"; //$NON-NLS-1$

			IEditorPart part = null;
			if (p != null) {
				part = p.openEditor(iEditorInput, editorid, true);
			}

			// IEditorPart part = EditorUtility.openInEditor(file);

			TextEditor textEditor = (TextEditor) part;

			final int start = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).getLineOffset(line - 1);
			final int end = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).getLineOffset(line);

			textEditor.setHighlightRange(start, end - start, true);

			AbstractTextEditor abstractTextEditor = textEditor;

			ISourceViewer viewer = null;

			final Field fields[] = AbstractTextEditor.class.getDeclaredFields();
			for (int i = 0; i < fields.length; ++i) {
				if ("fSourceViewer".equals(fields[i].getName())) { //$NON-NLS-1$
					Field f = fields[i];
					f.setAccessible(true);
					viewer = (ISourceViewer) f.get(abstractTextEditor);
					break;
				}
			}

			if (viewer != null) {
				viewer.revealRange(start, end - start);
				viewer.setSelectedRange(start, end - start);
			}

		} catch (Throwable t) {
			// t.printStackTrace();
		}
	}

	/*
	 * Borrowed from org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView
	 */
	IFile getFile(String filename, IResource[] resources) {
		try {
			for (int j = 0; j < resources.length; j++) {
				if (resources[j] instanceof IFile) {
					IFile f = (IFile) resources[j];
					if (f.getName().equals(filename)) {
						return f;
					}
				} else if (resources[j] instanceof IFolder) {
					IFile f = getFile(filename, ((IFolder) resources[j]).members());
					if (f != null) {
						return f;
					}
				} else if (resources[j] instanceof IProject) {
					IFile f = getFile(filename, ((IProject) resources[j]).members());
					if (f != null) {
						return f;
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	/*
	 * Borrowed from org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	@Override
	public void view() {
		// ---- currently unused ----
	}

}
