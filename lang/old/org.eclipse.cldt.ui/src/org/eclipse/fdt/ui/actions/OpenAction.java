/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ISourceReference;
import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.fdt.internal.ui.ICStatusConstants;
import org.eclipse.fdt.internal.ui.actions.ActionMessages;
import org.eclipse.fdt.internal.ui.actions.ActionUtil;
import org.eclipse.fdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.fdt.internal.ui.actions.SelectionConverter;
import org.eclipse.fdt.internal.ui.editor.FortranEditor;
import org.eclipse.fdt.internal.ui.util.ExceptionHandler;
import org.eclipse.fdt.ui.FortranUIPlugin;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * This action opens a Java editor on a Java element or file.
 * <p>
 * The action is applicable to selections containing elements of
 * type <code>ICompilationUnit</code>, <code>IMember</code>
 * or <code>IFile</code>.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p> 
 * 
 * @since 2.0
 */
public class OpenAction extends SelectionDispatchAction {
	
	private FortranEditor fEditor;
	
	/**
	 * Creates a new <code>OpenAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public OpenAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.getString("OpenAction.label")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("OpenAction.tooltip")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("OpenAction.description")); //$NON-NLS-1$		
		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_ACTION);
	}
	
	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 */
	public OpenAction(FortranEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
		setText(ActionMessages.getString("OpenAction.declaration.label")); //$NON-NLS-1$
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(ITextSelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}
	
	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		for (Iterator iter= selection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof ISourceReference)
				continue;
			if (element instanceof IFile)
				continue;
			if (element instanceof IStorage)
				continue;
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void run(ITextSelection selection) {
		if (!ActionUtil.isProcessable(getShell(), fEditor))
			return;
		try {
			ICElement element= SelectionConverter.codeResolve(fEditor, getShell(), getDialogTitle(), 
				ActionMessages.getString("OpenAction.select_element")); //$NON-NLS-1$
			if (element == null) {
				IEditorStatusLine statusLine= (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
				if (statusLine != null)
					statusLine.setMessage(true, ActionMessages.getString("OpenAction.error.messageBadSelection"), null); //$NON-NLS-1$
				getShell().getDisplay().beep();
				return;
			}
			ICElement input= SelectionConverter.getInput(fEditor);
			int type= element.getElementType();
			if (type == ICElement.C_PROJECT || type == ICElement.C_CCONTAINER)
				element= input;
			run(new Object[] {element} );
		} catch (CModelException e) {
			showError(e);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	public void run(IStructuredSelection selection) {
		if (!checkEnabled(selection))
			return;
		run(selection.toArray());
	}
	
	/**
	 * Note: this method is for internal use only. Clients should not call this method.
	 */
	public void run(Object[] elements) {
		if (elements == null)
			return;
		for (int i= 0; i < elements.length; i++) {
			Object element= elements[i];
			try {
				element= getElementToOpen(element);
				boolean activateOnOpen= fEditor != null ? true : OpenStrategy.activateOnOpen();
				OpenActionUtil.open(element, activateOnOpen);
			} catch (CModelException e) {
				FortranUIPlugin.getDefault().log(new Status(IStatus.ERROR, FortranUIPlugin.getPluginId(),
					ICStatusConstants.INTERNAL_ERROR, ActionMessages.getString("OpenAction.error.message"), e)); //$NON-NLS-1$
				
				ErrorDialog.openError(getShell(), 
					getDialogTitle(),
					ActionMessages.getString("OpenAction.error.messageProblems"),  //$NON-NLS-1$
					e.getStatus());
			
			} catch (PartInitException x) {
								
				String name= null;
				
				if (element instanceof ICElement) {
					name= ((ICElement) element).getElementName();
				} else if (element instanceof IStorage) {
					name= ((IStorage) element).getName();
				} else if (element instanceof IResource) {
					name= ((IResource) element).getName();
				}
				
				if (name != null) {
					MessageDialog.openError(getShell(),
						ActionMessages.getString("OpenAction.error.messageProblems"),  //$NON-NLS-1$
						ActionMessages.getFormattedString("OpenAction.error.messageArgs",  //$NON-NLS-1$
							new String[] { name, x.getMessage() } ));			
				}
			}		
		}
	}
	
	/**
	 * Note: this method is for internal use only. Clients should not call this method.
	 */
	public Object getElementToOpen(Object object) throws CModelException {
		return object;
	}	
	
	private String getDialogTitle() {
		return ActionMessages.getString("OpenAction.error.title"); //$NON-NLS-1$
	}
	
	private void showError(CoreException e) {
		ExceptionHandler.handle(e, getShell(), getDialogTitle(), ActionMessages.getString("OpenAction.error.message")); //$NON-NLS-1$
	}
}
