/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.AbstractToggleLinkingAction;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.cview.SelectionTransferDragAdapter;
import org.eclipse.cdt.internal.ui.cview.SelectionTransferDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.CDTViewerDragAdapter;
import org.eclipse.cdt.internal.ui.dnd.DelegatingDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.TransferDragSourceListener;
import org.eclipse.cdt.internal.ui.dnd.TransferDropTargetListener;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinerProvider;
import org.eclipse.cdt.internal.ui.editor.LexicalSortingAction;
import org.eclipse.cdt.internal.ui.editor.OpenIncludeAction;
import org.eclipse.cdt.internal.ui.editor.TogglePresentationAction;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.internal.rdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ptp.internal.rdt.ui.search.actions.SelectionSearchGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;


/**
 * Abstract outline page based on CModel.
 *
 * @since 5.0
 */
public abstract class AbstractCModelOutlinePage extends Page implements IContentOutlinePage, ISelectionChangedListener {

	protected static class IncludeGroupingAction extends Action {
		AbstractCModelOutlinePage fOutLinePage;

		public IncludeGroupingAction(AbstractCModelOutlinePage outlinePage) {
			super(ActionMessages.getString("IncludesGroupingAction.label")); //$NON-NLS-1$
			setDescription(ActionMessages.getString("IncludesGroupingAction.description")); //$NON-NLS-1$
			setToolTipText(ActionMessages.getString("IncludeGroupingAction.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_GROUP_INCLUDE);

			boolean enabled= isIncludesGroupingEnabled();
			setChecked(enabled);
			fOutLinePage = outlinePage;
		}

		/**
		 * Runs the action.
		 */
		public void run() {
			boolean oldValue = isIncludesGroupingEnabled();
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_INCLUDES, isChecked());
			if (oldValue != isChecked()) {
				fOutLinePage.contentUpdated();
			}
		}

		public boolean isIncludesGroupingEnabled () {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES);
		}

	}

	/**
	 * This action toggles whether this C Outline page links
	 * its selection to the active editor.
	 * 
	 * @since 3.0
	 */
	public class ToggleLinkingAction extends AbstractToggleLinkingAction {
	
		/**
		 * Constructs a new action.
		 */
		public ToggleLinkingAction() {
			setChecked(isLinkingEnabled());
		}

		/**
		 * Runs the action.
		 */
		public void run() {
			boolean checked = isChecked();
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_LINK_TO_EDITOR, checked);
			if (checked && fEditor != null)
				synchronizeSelectionWithEditor();
		}
	}
	
	private static final int TEXT_FLAGS = AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | CElementBaseLabels.F_APP_TYPE_SIGNATURE | CElementBaseLabels.M_APP_RETURNTYPE;
	private static final int IMAGE_FLAGS = AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS;
	protected ITextEditor fEditor;
	protected ITranslationUnit fInput;
	private ProblemTreeViewer fTreeViewer;
	private ListenerList fSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);
	protected TogglePresentationAction fTogglePresentation;
	protected String fContextMenuId;
	private Menu fMenu;
	protected OpenIncludeAction fOpenIncludeAction;
	private IncludeGroupingAction fIncludeGroupingAction;
	private ToggleLinkingAction fToggleLinkingAction;
	private ActionGroup fMemberFilterActionGroup;
	private SelectionSearchGroup fSelectionSearchGroup;
	private ActionGroup fRefactoringActionGroup;
	private OpenViewActionGroup fOpenViewActionGroup;
	/**
	 * Custom filter action group.
	 * @since 3.0
	 */
	private ActionGroup fCustomFiltersActionGroup;

	/**
	 * Create a new outline page for the given editor.
	 * @param contextMenuId  The id of this page's context menu
	 * @param editor  the editor associated with this outline page
	 */
	public AbstractCModelOutlinePage(String contextMenuId, ITextEditor editor) {
		super();
		fEditor= editor;
		fInput= null;
		fContextMenuId= contextMenuId;

		fTogglePresentation= new TogglePresentationAction();
		fTogglePresentation.setEditor(editor);
		
		fOpenIncludeAction= new OpenIncludeAction(this);
	}

	public boolean isLinkingEnabled() {
		return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_LINK_TO_EDITOR);
	}

	public ICElement getRoot() {
		return fInput;
	}

	/**
	 * Called by the editor to signal that the content has updated.
	 */
	public void contentUpdated() {
		if (fInput != null) {				
			final TreeViewer treeViewer= getTreeViewer();
			if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!treeViewer.getControl().isDisposed()) {
							ISelection sel= treeViewer.getSelection();
							treeViewer.setSelection(updateSelection(sel));		
							treeViewer.refresh();
						}
					}
				});
			}
		}
	}

	protected ISelection updateSelection(ISelection sel) {
		ArrayList newSelection= new ArrayList();
		if (sel instanceof IStructuredSelection) {
			Iterator iter= ((IStructuredSelection)sel).iterator();
			for (;iter.hasNext();) {
				//ICElement elem= fInput.findEqualMember((ICElement)iter.next());
				Object o = iter.next();
				if (o instanceof ICElement) {
					newSelection.add(o);
				}
			}
		}
		return new StructuredSelection(newSelection);
	}

	/**
	 * Sets the selected element to the one at the current cursor position in the editor.
	 */
	public void synchronizeSelectionWithEditor() {
		if(fInput == null || fEditor == null || fTreeViewer == null)
			return;
	
		ITextSelection editorSelection = (ITextSelection) fEditor.getSelectionProvider().getSelection();
		if(editorSelection == null)
			return;
		
		int offset = editorSelection.getOffset();
		
		ICElement editorElement;
		try {
			editorElement = fInput.getElementAtOffset(offset);
		} catch (CModelException e) {
			return;
		}
	
		if (editorElement != null) {
			IStructuredSelection selection = new StructuredSelection(editorElement);
			fTreeViewer.setSelection(selection, true);
		}
	}

	/**
	 * called to create the context menu of the outline
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		CUIPlugin.createStandardGroups(menu);
		
		ISelection selection= getSelection();
		if (fOpenViewActionGroup != null && OpenViewActionGroup.canActionBeAdded(selection)){
			fOpenViewActionGroup.fillContextMenu(menu);
		}
	
		if (OpenIncludeAction.canActionBeAdded(selection)) {
			menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, fOpenIncludeAction);
		}
	
		if (fSelectionSearchGroup != null && SelectionSearchGroup.canActionBeAdded(selection)){
			fSelectionSearchGroup.fillContextMenu(menu);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.fillContextMenu(menu);
		}
	}

	protected CContentOutlinerProvider createContentProvider(TreeViewer viewer) {
		IWorkbenchPart part= getSite().getPage().getActivePart();
		if (part == null) {
			return new CContentOutlinerProvider(viewer);
		}
		return new CContentOutlinerProvider(viewer, part.getSite());
	}

	protected ProblemTreeViewer createTreeViewer(Composite parent) {
		fTreeViewer = new ProblemTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fTreeViewer.setContentProvider(createContentProvider(fTreeViewer));
		fTreeViewer.setLabelProvider(new DecoratingCLabelProvider(new AppearanceAwareLabelProvider(TEXT_FLAGS, IMAGE_FLAGS), true));
		fTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		fTreeViewer.setUseHashlookup(true);
		fTreeViewer.addSelectionChangedListener(this);
		return fTreeViewer;
	}

	public void createControl(Composite parent) {
		fTreeViewer = createTreeViewer(parent);
		initDragAndDrop();
	
		MenuManager manager= new MenuManager(fContextMenuId);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		Control control= fTreeViewer.getControl();
		fMenu= manager.createContextMenu(control);
		control.setMenu(fMenu);
	
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (fOpenIncludeAction != null) {
					fOpenIncludeAction.run();
				}
			}
		});
		// register global actions
		IPageSite site= getSite();
		site.registerContextMenu(fContextMenuId, manager, fTreeViewer);
		site.setSelectionProvider(fTreeViewer);
		
		IActionBars bars= site.getActionBars();		
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY, fTogglePresentation);
	
		fSelectionSearchGroup = createSearchActionGroup();
		fOpenViewActionGroup = createOpenViewActionGroup();
		fRefactoringActionGroup= createRefactoringActionGroup();
		// Custom filter group
		fCustomFiltersActionGroup= createCustomFiltersActionGroup();
	
		// Do this before setting input but after the initializations of the fields filtering
		registerActionBars(bars);
	
		fTreeViewer.setInput(fInput);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, ICHelpContextIds.COUTLINE_VIEW);
	}

	public void dispose() {
		if (fTreeViewer != null) {
			fTreeViewer.removeSelectionChangedListener(this);
			fTreeViewer= null;
		}
		
		if (fTogglePresentation != null) {
			fTogglePresentation.setEditor(null);
			fTogglePresentation= null;
		}
		
		if (fMemberFilterActionGroup != null) {
			fMemberFilterActionGroup.dispose();
			fMemberFilterActionGroup= null;
		}
		
		if (fOpenViewActionGroup != null) {
		    fOpenViewActionGroup.dispose();
		    fOpenViewActionGroup= null;
		}
	
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.dispose();
			fRefactoringActionGroup= null;
		}
	
		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.dispose();
			fSelectionSearchGroup= null;
		}
	
		if (fCustomFiltersActionGroup != null) {
			fCustomFiltersActionGroup.dispose();
			fCustomFiltersActionGroup= null;
		}
	
		if (fSelectionChangedListeners != null) {
			fSelectionChangedListeners.clear();
			// don't set the listeners to null, the outline page may be reused.
		}
	
		if (fMenu != null && !fMenu.isDisposed()) {
			fMenu.dispose();
			fMenu= null;
		}
	
		fInput= null;
		
		super.dispose();
	}

	/**
	 * Register actions to the action bars.
	 * 
	 * @param actionBars
	 */
	protected void registerActionBars(IActionBars actionBars) {
		IToolBarManager toolBarManager= actionBars.getToolBarManager();
		
		LexicalSortingAction action= new LexicalSortingAction(getTreeViewer());
		toolBarManager.add(action);
	
		fMemberFilterActionGroup= createMemberFilterActionGroup();
		if (fMemberFilterActionGroup != null) {
			fMemberFilterActionGroup.fillActionBars(actionBars);
		}
		if (fCustomFiltersActionGroup != null) {
			fCustomFiltersActionGroup.fillActionBars(actionBars);
		}
		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.fillActionBars(actionBars);
		}
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.fillActionBars(actionBars);
		}
	
		IMenuManager menu= actionBars.getMenuManager();
		menu.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
		
		fToggleLinkingAction= new ToggleLinkingAction();
		menu.add(fToggleLinkingAction);
		fIncludeGroupingAction= new IncludeGroupingAction(this);
		menu.add(fIncludeGroupingAction);
	}

	/**
	 * return an ActionGroup contributing search actions or
	 *         <code>null</code> if search is not supported
	 */
	protected SelectionSearchGroup createSearchActionGroup() {
		// default: no search action group
		return null;
	}

	/**
	 * @return an OpenViewActionGroup contributing open view actions or
	 *         <code>null</code> if open view actions are not wanted
	 */
	protected OpenViewActionGroup createOpenViewActionGroup() {
		// default: no open view action group
		return null;
	}

	/**
	 * @return an ActionGroup contributing refactoring actions or
	 *         <code>null</code> if refactoring is not supported
	 */
	protected ActionGroup createRefactoringActionGroup() {
		// default: no refactoring actions
		return null;
	}

	/**
	 * @return an ActionGroup instance to provide custom filters or
	 *         <code>null</code> if this action group is not wanted
	 */
	protected ActionGroup createCustomFiltersActionGroup() {
		// default: no custom filters
		return null;
	}

	/**
	 * @return an ActionGroup contributing member filters or <code>null</code>
	 *         if member filters are not wanted
	 */
	protected ActionGroup createMemberFilterActionGroup() {
		// default: no member filters
		return null;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.add(listener);
	}

	/**
	 * Fires a selection changed event.
	 *
	 * @param selection the new selection
	 */
	protected void fireSelectionChanged(ISelection selection) {
		// create an event
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
	
		// fire the event
		Object[] listeners = fSelectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((ISelectionChangedListener) listeners[i]).selectionChanged(event);
		}
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.setContext(new ActionContext(selection));
			fRefactoringActionGroup.updateActionBars();
		}
	}

	public Control getControl() {
		if (fTreeViewer == null)
			return null;
		return fTreeViewer.getControl();
	}

	public ISelection getSelection() {
		if (fTreeViewer == null)
			return StructuredSelection.EMPTY;
		return fTreeViewer.getSelection();
	}

	/**
	 * Returns this page's tree viewer.
	 *
	 * @return this page's tree viewer, or <code>null</code> if 
	 *   <code>createControl</code> has not been called yet
	 */
	protected TreeViewer getTreeViewer() {
		return fTreeViewer;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.remove(listener);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		fireSelectionChanged(event.getSelection());
	}

	/**
	 * Sets focus to a part in the page.
	 */
	public void setFocus() {
		fTreeViewer.getControl().setFocus();
	}

	public void setSelection(ISelection selection) {
		if (fTreeViewer != null) 
			fTreeViewer.setSelection(selection);
	}

	/**
	 * Set the current input to the content provider.  
	 * @param unit
	 */
	public void setInput(ITranslationUnit unit) {
		fInput = unit;
		if (fTreeViewer != null) {
			fTreeViewer.setInput (fInput);
		}
	}

	private void initDragAndDrop() {
		int ops= DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transfers= new Transfer[] {
			LocalSelectionTransfer.getInstance()
		};
		
		// Drop Adapter
		TransferDropTargetListener[] dropListeners= new TransferDropTargetListener[] {
			new SelectionTransferDropAdapter(fTreeViewer)
		};
		fTreeViewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, new DelegatingDropAdapter(dropListeners));
		
		// Drag Adapter
		TransferDragSourceListener[] dragListeners= new TransferDragSourceListener[] {
			new SelectionTransferDragAdapter(fTreeViewer)
		};
		fTreeViewer.addDragSupport(ops, transfers, new CDTViewerDragAdapter(fTreeViewer, dragListeners));
	}

}