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
package org.eclipse.fdt.make.ui.views;


import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.fdt.make.ui.IMakeHelpContextIds;
import org.eclipse.fdt.make.ui.MakeContentProvider;
import org.eclipse.fdt.make.ui.MakeLabelProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

public class MakeView extends ViewPart {

	private BuildTargetAction buildTargetAction;
	private EditTargetAction editTargetAction;
	private DeleteTargetAction deleteTargetAction;
	AddTargetAction addTargetAction;
	TreeViewer viewer;
	DrillDownAdapter drillDownAdapter;

	public MakeView() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		WorkbenchHelp.setHelp(parent, IMakeHelpContextIds.MAKE_VIEW);
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new MakeContentProvider());
		viewer.setLabelProvider(new MakeLabelProvider());

		drillDownAdapter = new DrillDownAdapter(viewer);

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					handleDeleteKeyPressed();
				}
			}
		});

		viewer.setContentProvider(new MakeContentProvider());
		viewer.setLabelProvider(new MakeLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		getSite().setSelectionProvider(viewer);

		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void makeActions() {
		buildTargetAction = new BuildTargetAction(viewer.getControl().getShell());
		addTargetAction = new AddTargetAction(viewer.getControl().getShell());
		deleteTargetAction = new DeleteTargetAction(viewer.getControl().getShell());
		editTargetAction = new EditTargetAction(viewer.getControl().getShell());
	}
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager toolBar) {
		drillDownAdapter.addNavigationActions(toolBar);
		toolBar.add(buildTargetAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				MakeView.this.fillContextMenu(manager);
				updateActions((IStructuredSelection)viewer.getSelection());
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		//		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(buildTargetAction);
		manager.add(addTargetAction);
		manager.add(deleteTargetAction);
		manager.add(editTargetAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);

		// Other plug-ins can contribute there actions here
		//		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void handleDeleteKeyPressed() {
		deleteTargetAction.run();
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
		buildTargetAction.run();
	}

	void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();
		updateActions(sel);
	}

	
	void updateActions(IStructuredSelection sel) {
		addTargetAction.selectionChanged(sel);
		buildTargetAction.selectionChanged(sel);
		deleteTargetAction.selectionChanged(sel);
		editTargetAction.selectionChanged(sel);
	}
}
