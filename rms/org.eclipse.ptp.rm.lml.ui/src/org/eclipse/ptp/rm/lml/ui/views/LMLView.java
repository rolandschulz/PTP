/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiSelectedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.IViewListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.actions.AddLguiAction;
import org.eclipse.ptp.rm.lml.ui.actions.RemoveLguiAction;
import org.eclipse.ptp.rm.lml.ui.actions.UpdateLguiAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

public class LMLView extends ViewPart {
	private final class LMLViewListener implements IViewListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILguiListener# handleEvent
		 * (org.eclipse.ptp.core.events.ILguiAddedEvent)
		 */
		public synchronized void handleEvent(ILguiAddedEvent e) {
			fSelected = e.getLguiItem();
			createList();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILguiListener# handleEvent
		 * (org.eclipse.ptp.core.events.ILguiRemovedEvent)
		 */
		public synchronized void handleEvent(ILguiRemovedEvent e) {
			fSelected = e.getLguiItem();
			createList();
		}

		public void handleEvent(ILguiSelectedEvent e) {
			fSelected = e.getLguiItem();
			createList();

		}

	}

	public final class ListSelectionListener implements SelectionListener {
		public void widgetSelected(SelectionEvent e) {
			int selectedItem = list.getSelectionIndex();
			lmlManager.selectLgui(selectedItem);
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			lmlManager.selectLgui(0);
		}

	}

	/**
	 * 
	 */
	public ListViewer viewer;
	private AddLguiAction addLguiAction;
	private RemoveLguiAction removeLguiAction;
	private UpdateLguiAction updateLguiAction;
	private ILguiItem fSelected = null;
	private final ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
	private LMLViewListener lmlViewListener = null;
	private List list = null;
	private final ListSelectionListener listListener = new ListSelectionListener();

	public LMLView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new ListViewer(parent, SWT.SINGLE);
		viewer.setLabelProvider(new LabelProvider(){
			public String getText(Object obj) {
				return (String) obj;
			}
		});
		viewer.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object parent) {
				return null;
			}
		});

		lmlViewListener = (LMLViewListener) lmlManager.getListener(this.getClass().getName());
		if (lmlViewListener == null) {
			lmlViewListener = new LMLViewListener();
			lmlManager.addListener(lmlViewListener, this.getClass().getName());
		}

		fSelected = lmlManager.getSelectedLguiItem();
		createList();
	}

	private void createList() {
		list = viewer.getList();
		list.removeAll();
		list.removeSelectionListener(listListener);

		createContextMenu();
		if (fSelected != null) {
			for (String lgui : lmlManager.getLguis()) {
				viewer.add(lgui);
			}
			list.setSelection(lmlManager.getSelectedLguiIndex(fSelected.toString()));
		}
		list.addSelectionListener(listListener);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
	}

	private void createContextMenu() {
		final Shell shell = getSite().getShell();
		addLguiAction = new AddLguiAction(shell);
		removeLguiAction = new RemoveLguiAction(shell);
		updateLguiAction = new UpdateLguiAction(shell);

		MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		manager.add(addLguiAction);
		boolean inContextForLgui = selection.size() > 0;
		boolean inContextForRemoveLgui = inContextForLgui;
		boolean inContextForUpdateLgui = inContextForLgui;
		manager.add(removeLguiAction);
		removeLguiAction.setEnabled(inContextForRemoveLgui);
		manager.add(new Separator());
		manager.add(updateLguiAction);
		updateLguiAction.setEnabled(inContextForUpdateLgui);
	}

}