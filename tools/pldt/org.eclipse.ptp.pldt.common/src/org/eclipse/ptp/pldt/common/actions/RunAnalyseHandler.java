/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.actions;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * RunAnalyseHandler - keeps state information for the analysis handlers in
 * the dropdown analysis menu.
 * Common behavior is in (derived class) RunAnalyseHandlerBase.
 * 
 * That is, the AnalysisDropdownHandler will repeat the action of the last RunAnalyseHandler
 * that was executed - they are cached as one of these.
 * 
 * @author Beth Tibbitts
 */
public abstract class RunAnalyseHandler extends AbstractHandler {
	/**
	 * the current selection is cached here
	 */
	protected IStructuredSelection selection;

	/**
	 * Constructor
	 */
	public RunAnalyseHandler() {
	}

	/**
	 * Get the current selection from the handler event. If it's a structured selection
	 * (e.g. resource in the project explorer) then return it.
	 * If it's e.g. a text selection in the editor, we don't care about that
	 * 
	 * Note that we cache the last structured selection (like the previous "action" version
	 * of this class) since we don't get selection changed events.
	 * However, AnalysisDropDownHandler does get these events, and its value
	 * will be used if HanderUtil doesn't have any information yet.
	 * 
	 * @param event
	 * @return the current selection if it's a structured selection e.g. in the navigator
	 */
	public IStructuredSelection getSelection(ExecutionEvent event) {
		ISelection curSel = HandlerUtil.getCurrentSelection(event);
		if (curSel instanceof IStructuredSelection) {
			selection = (IStructuredSelection) curSel;
		}

		if (selection == null) {
			selection = AnalysisDropdownHandler.getInstance().getLastSelection();
		}
		// If there isn't a current selection appropriate for us,
		// get the last one used in any analysis.
		// Since we now register as a selection listener,
		// I doubt this is ever utilized.
		if (selection == null) {
			selection = AnalysisDropdownHandler.getLastAnalysisSelection();
		}
		return selection;

	}

	/**
	 * Counts the number of files in the selection (leaf nodes only - Files -
	 * not the directories/containers) <br>
	 * Note that this makes no distinction about what type of files.
	 * 
	 * @return number of files
	 */
	@SuppressWarnings("unchecked")
	protected int countFilesSelected() {
		int count = 0;
		// Get elements of a possible multiple selection
		Iterator iter = this.selection.iterator();
		while (iter.hasNext()) {
			Object obj = (Object) iter.next();
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				final IResource res = (IResource) ((IAdaptable) obj)
						.getAdapter(IResource.class);
				count = count + countFiles(res);
			}
		}
		// System.out.println("number of files: " + count);
		return count;
	}

	/**
	 * Count the number of files in this resource (file or container).
	 * 
	 * @param res
	 * @return
	 */
	protected int countFiles(IResource res) {
		if (res instanceof IFile) {
			return 1;
		} else if (res instanceof IContainer) {
			int count = 0;

			try {
				IResource[] kids = ((IContainer) res).members();
				for (int i = 0; i < kids.length; i++) {
					IResource child = kids[i];
					count = count + countFiles(child);
				}
				return count;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	protected int countFilesSelected(String[] exts) {
		int count = 0;
		// Get elements of a possible multiple selection
		Iterator iter = this.selection.iterator();
		while (iter.hasNext()) {
			Object obj = (Object) iter.next();
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				final IResource res = (IResource) ((IAdaptable) obj)
						.getAdapter(IResource.class);
				count = count + countFiles(res, exts);
			}
		}
		// System.out.println("number of files: " + count);
		return count;
	}

	/**
	 * count files ending in one of the given file extensions
	 * 
	 * @param res
	 * @param exts
	 *            array of extensions e.g. ".h", ".hpp" etc
	 * @return
	 */
	protected int countFiles(IResource res, String[] exts) {
		if (res instanceof IFile) {
			IFile file = (IFile) res;
			String filename = file.getName();
			for (int i = 0; i < exts.length; i++) {
				String ext = exts[i];
				if (filename.endsWith(ext)) {
					System.out.println("found " + ext + " in file: " + file.getName() + "  count+1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return 1;
				}
			}
			return 0; // not found

		} else if (res instanceof IContainer) {
			int count = 0;

			try {
				IResource[] kids = ((IContainer) res).members();
				for (int i = 0; i < kids.length; i++) {
					IResource child = kids[i];
					count = count + countFiles(child, exts);
				}
				return count;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

}