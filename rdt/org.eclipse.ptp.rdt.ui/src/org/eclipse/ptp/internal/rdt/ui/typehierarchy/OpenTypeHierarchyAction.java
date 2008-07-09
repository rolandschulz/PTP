/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.OpenTypeHierarchyAction
 * Version: 1.2
 */
package org.eclipse.ptp.internal.rdt.ui.typehierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.typehierarchy.Messages;
import org.eclipse.cdt.internal.ui.typehierarchy.TypeHierarchyUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.LocalTypeHierarchyService;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;



public class OpenTypeHierarchyAction extends SelectionDispatchAction {

	private static final ITypeHierarchyService fService = new LocalTypeHierarchyService();
	
	private ITextEditor fEditor;

	public OpenTypeHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(Messages.OpenTypeHierarchyAction_label);
		setToolTipText(Messages.OpenTypeHierarchyAction_tooltip);
	}
	
	public OpenTypeHierarchyAction(ITextEditor editor) {
		this(editor.getSite());
		fEditor= editor;
		setEnabled(fEditor != null && CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput()) != null);
	}

	public void run(ITextSelection sel) {
		TypeHierarchyUtil.open(fService, fEditor, sel);
	}
	
	public void run(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			Object selectedObject= selection.getFirstElement();
			ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
			if (elem != null) {
				TypeHierarchyUtil.open(fService, elem, getSite().getWorkbenchWindow());
			}
		}
	}

	public void selectionChanged(ITextSelection sel) {
	}
			
	public void selectionChanged(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			setEnabled(false);
			return;
		}
		
		Object selectedObject= selection.getFirstElement();
		ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
		if (elem != null) {
			setEnabled(TypeHierarchyUI.isValidInput(elem));
		}
		else {
			setEnabled(false);
		}
	}

	private Object getAdapter(Object object, Class desiredClass) {
		if (desiredClass.isInstance(object)) {
			return object;
		}
		if (object instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable) object;
			return adaptable.getAdapter(desiredClass);
		}
		return null;
	}
}
