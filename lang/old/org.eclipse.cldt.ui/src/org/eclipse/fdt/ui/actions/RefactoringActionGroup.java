/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.fdt.internal.ui.IContextMenuConstants;
import org.eclipse.fdt.internal.ui.actions.ActionMessages;
import org.eclipse.fdt.internal.ui.editor.FortranEditor;
import org.eclipse.fdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.fdt.internal.ui.refactoring.actions.RedoRefactoringAction;
import org.eclipse.fdt.internal.ui.refactoring.actions.RenameRefactoringAction;
import org.eclipse.fdt.internal.ui.refactoring.actions.UndoRefactoringAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.Page;

/**
 * Action group that adds refactor actions (for example Rename..., Move..., etc)
 * to a context menu and the global menu bar.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class RefactoringActionGroup extends ActionGroup {
	
	/**
	 * Pop-up menu: id of the refactor sub menu (value <code>org.eclipse.fdt.ui.refactoring.menu</code>).
	 * 
	 * @since 2.1
	 */
	public static final String MENU_ID= "org.eclipse.fdt.ui.refactoring.menu"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the reorg group of the refactor sub menu (value
	 * <code>reorgGroup</code>).
	 * 
	 * @since 2.1
	 */
	public static final String GROUP_REORG= "reorgGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the type group of the refactor sub menu (value
	 * <code>typeGroup</code>).
	 * 
	 * @since 2.1
	 */
	public static final String GROUP_TYPE= "typeGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the coding group of the refactor sub menu (value
	 * <code>codingGroup</code>).
	 * 
	 * @since 2.1
	 */
	public static final String GROUP_CODING= "codingGroup"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Rename Element global action
	 * (value <code>"org.eclipse.fdt.ui.actions.refactor.Rename"</code>).
	 */
	public static final String REFACTOR_RENAME= "org.eclipse.fdt.ui.actions.refactor.RenameAction"; //$NON-NLS-1$
	/**
	 * Refactor menu: name of standard Undo global action
	 * (value <code>"org.eclipse.fdt.ui.actions.refactor.undo"</code>).
	 */
	public static final String REFACTOR_UNDO= "org.eclipse.fdt.ui.actions.refactor.UndoAction"; //$NON-NLS-1$
	/**
	 * Refactor menu: name of standard Redo global action
	 * (value <code>"org.eclipse.fdt.ui.actions.refactor.redo"</code>).
	 */
	public static final String REFACTOR_REDO= "org.eclipse.fdt.ui.actions.refactor.RedoAction"; //$NON-NLS-1$
	
	private IWorkbenchSite fSite;
	private FortranEditor fEditor;
	private String fGroupName= IContextMenuConstants.GROUP_REORGANIZE;

	private RenameRefactoringAction fRenameAction;	
	private RedoRefactoringAction 	fRedoAction;	
	private UndoRefactoringAction 	fUndoAction;	
	private List fEditorActions;
	
	private static class NoActionAvailable extends Action {
		public NoActionAvailable() {
			setEnabled(false);
			setText(RefactoringMessages.getString("RefactorActionGroup.no_refactoring_available")); //$NON-NLS-1$
		}
	}
	private Action fNoActionAvailable= new NoActionAvailable(); 
		
	/**
	 * Creates a new <code>RefactorActionGroup</code>. The group requires
	 * that the selection provided by the part's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param part the view part that owns this action group
	 */
	public RefactoringActionGroup(IViewPart part, String groupName) {
		this(part.getSite(), groupName);
	}	
	
	/**
	 * Creates a new <code>RefactorActionGroup</code>. The action requires
	 * that the selection provided by the page's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param page the page that owns this action group
	 */
	public RefactoringActionGroup(Page page, String groupName) {
		this(page.getSite(), groupName);
	}
	
	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 */
	public RefactoringActionGroup(FortranEditor editor, String groupName) {
		fSite= editor.getEditorSite();		
		fEditor= editor;
		if((groupName != null) && (groupName.length() > 0))
			fGroupName= groupName;
		
		ISelectionProvider provider= editor.getSelectionProvider();
		ISelection selection= provider.getSelection();
		fEditorActions= new ArrayList(3);

		fRenameAction= new RenameRefactoringAction(editor);
		fRenameAction.update(selection);
		editor.setAction("RenameElement", fRenameAction); //$NON-NLS-1$
		fEditorActions.add(fRenameAction);

		fUndoAction= new UndoRefactoringAction(editor);
		fUndoAction.update(selection);
		editor.setAction("UndoAction", fUndoAction); //$NON-NLS-1$
		fEditorActions.add(fUndoAction);

		fRedoAction= new RedoRefactoringAction(editor);
		fRedoAction.update(selection);
		editor.setAction("RedoAction", fRedoAction); //$NON-NLS-1$
		fEditorActions.add(fRedoAction);
	}

	public RefactoringActionGroup(IWorkbenchSite site, String groupName) {
		fSite= site;
		if((groupName != null) && (groupName.length() > 0))
			fGroupName= groupName;
		ISelectionProvider provider= fSite.getSelectionProvider();
		ISelection selection= provider.getSelection();

		fRenameAction= new RenameRefactoringAction(site);
		initAction(fRenameAction, provider, selection);

		fUndoAction= new UndoRefactoringAction(site);
		initAction(fUndoAction, provider, selection);

		fRedoAction= new RedoRefactoringAction(site);
		initAction(fRedoAction, provider, selection);

	}

	private static void initAction(SelectionDispatchAction action, ISelectionProvider provider, ISelection selection){
		action.update(selection);
		provider.addSelectionChangedListener(action);
	}
	
	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(REFACTOR_RENAME, fRenameAction);
		actionBars.setGlobalActionHandler(REFACTOR_UNDO, fUndoAction);
		actionBars.setGlobalActionHandler(REFACTOR_REDO, fRedoAction);
	}
	
	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		addRefactorSubmenu(menu);
	}
	
	/*
	 * @see ActionGroup#dispose()
	 */
	public void dispose() {
		ISelectionProvider provider= fSite.getSelectionProvider();
		
		if (fRenameAction != null) {
			disposeAction(fRenameAction, provider);
			fRenameAction= null;
		}
		
		if (fUndoAction != null) {
			disposeAction(fUndoAction, provider);
			fUndoAction.dispose();
			fUndoAction= null;
		}
		
		if (fRedoAction != null) {
			disposeAction(fRedoAction, provider);
			fRedoAction.dispose();
			fRedoAction= null;
		}
		
		if (fEditorActions != null) {
			fEditorActions.clear();
			fEditorActions= null;
		}
		
		super.dispose();
	}
	
	private void disposeAction(ISelectionChangedListener action, ISelectionProvider provider) {
		provider.removeSelectionChangedListener(action);
	}
	
	private void addRefactorSubmenu(IMenuManager menu) {
		IMenuManager refactorSubmenu= new MenuManager(ActionMessages.getString("RefactorMenu.label"), MENU_ID);  //$NON-NLS-1$
		if (fEditor != null) {
			ITextSelection textSelection= (ITextSelection)fEditor.getSelectionProvider().getSelection();								
			for (Iterator iter= fEditorActions.iterator(); iter.hasNext(); ) {
				SelectionDispatchAction action= (SelectionDispatchAction)iter.next();
				action.update(textSelection);
			}
			refactorSubmenu.removeAll();
			 if (fillRefactorMenu(refactorSubmenu) == 0)
				refactorSubmenu.add(fNoActionAvailable);					
			menu.appendToGroup(fGroupName, refactorSubmenu);
		} else {
			if (fillRefactorMenu(refactorSubmenu) > 0){
				menu.appendToGroup(fGroupName, refactorSubmenu);
			}
		}
	}
	
	private int fillRefactorMenu(IMenuManager refactorSubmenu) {
		int added= 0;
		refactorSubmenu.add(new Separator(GROUP_REORG));
		added+= addAction(refactorSubmenu, fRenameAction);
		added+= addAction(refactorSubmenu, fUndoAction);
		added+= addAction(refactorSubmenu, fRedoAction);
		return added;
	}
	
	private int addAction(IMenuManager menu, IAction action) {
		if (action != null && action.isEnabled()) {
			menu.add(action);
			return 1;
		}
		return 0;
	}
			
}
