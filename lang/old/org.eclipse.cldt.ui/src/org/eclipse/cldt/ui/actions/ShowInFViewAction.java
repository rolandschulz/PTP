/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cldt.ui.actions;

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.internal.ui.actions.SelectionConverter;
import org.eclipse.cldt.internal.ui.editor.FortranEditorMessages;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class will open the Fortran Projects view and highlight the
 * selected resource matching the current resouce being edited in
 * the Fortran Editor.  It uses the IShowInSource/IShowInTarget to 
 * accomplish this task so as to provide some additional portability
 * and future proofing.
 */
public class ShowInFViewAction extends SelectionProviderAction {

	private IWorkbenchPage page;
	private ITextEditor fEditor;

	public ShowInFViewAction(IWorkbenchSite site) {
		this(site.getPage(), site.getSelectionProvider());
	}

	public ShowInFViewAction(ITextEditor editor) {	
		this(editor.getEditorSite().getWorkbenchWindow().getActivePage(), editor.getSelectionProvider());
		fEditor = editor;
	}

	public ShowInFViewAction(IWorkbenchPage page, ISelectionProvider viewer) {
		super(viewer, FortranEditorMessages.getString("ShowInFView.label")); //$NON-NLS-1$
		setToolTipText(FortranEditorMessages.getString("ShowInFView.tooltip")); //$NON-NLS-1$
		setDescription(FortranEditorMessages.getString("ShowInFView.description")); //$NON-NLS-1$
		this.page = page;
		setDescription(FortranEditorMessages.getString("ShowInFView.toolTip")); //$NON-NLS-1$
		//WorkbenchHelp.setHelp(this, ICHelpContextIds.SHOW_IN_CVIEW_ACTION);
	}

	/**
	 * @see IAction#actionPerformed
	 */
	public void run() {
		ISelection selection = getSelection();
		if (selection instanceof ITextSelection) {
			run(fEditor);
		} else if (selection instanceof IStructuredSelection) {
			run((IStructuredSelection)selection);
		}

	}

	public void run(IStructuredSelection selection) {
		if (page == null) {
			page = FortranUIPlugin.getActivePage();
			if (page == null) {
				return;
			}
		}

		//Locate a source and a target for us to use
		try {
			IWorkbenchPart part = page.showView(FortranUIPlugin.FVIEW_ID);
			if (part instanceof ISetSelectionTarget) {
				((ISetSelectionTarget) part).selectReveal(selection);
			}
		} catch(PartInitException ex) {
		}
	}

	public void run(ITextEditor editor) {
		if (editor != null) {
			try {
				ICElement celement = SelectionConverter.getElementAtOffset(editor);
				if (celement != null) {
					run(new StructuredSelection(celement));
				}
			} catch (CModelException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * Method declared on SelectionProviderAction.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(!getSelection().isEmpty());
	}

}
