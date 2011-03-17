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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.ui.actions.AddLguiAction;
import org.eclipse.ptp.rm.lml.ui.providers.LMLListLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author Claudia Knobloch
 * 
 */
public class LMLView extends ViewPart {

	private final class LMLListener implements ILguiListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILguiListener# handleEvent
		 * (org.eclipse.ptp.core.events.ILguiAddedEvent)
		 */
		@Override
		public synchronized void handleEvent(ILguiAddedEvent e) {
			viewer.getList().setRedraw(false);
			try {
				fSelected = e.getLgui();
				viewer.add(fSelected);
			} finally {
				viewer.getList().setRedraw(true);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILguiListener# handleEvent
		 * (org.eclipse.ptp.core.events.ILguiRemovedEvent)
		 */
		@Override
		public synchronized void handleEvent(ILguiRemovedEvent e) {
			viewer.getList().setRedraw(false);
			try {
				viewer.remove(e.getLgui());
			} finally {
				viewer.getList().setRedraw(true);
			}
		}

		@Override
		public void handleEvent(IJobListSortedEvent e) {

		}
	}

	/**
	 * 
	 */
	public ListViewer viewer;
	private AddLguiAction addLguiAction;
	private ILguiItem fSelected = null;
	private final ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
	private final LMLListener lmlListener = new LMLListener();

	public LMLView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new ListViewer(parent, SWT.MULTI);
		viewer.setLabelProvider(new LMLListLabelProvider());
		viewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object parent) {
				return null;
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

			}
		});
		viewer.setInput(fSelected);
		createContextMenu();

		getSite().setSelectionProvider(viewer);

		ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
		lmlManager.addListener(lmlListener);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private void createContextMenu() {
		final Shell shell = getSite().getShell();
		addLguiAction = new AddLguiAction(shell);

		MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
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
	}

}
