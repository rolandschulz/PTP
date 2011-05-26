/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 * Modified by:
 * 		Claudia Konbloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.ui.managers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiSelectedEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewDisposedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.IViewListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.ILMLUIConstants;
import org.eclipse.ptp.rm.lml.ui.LMLUIPlugin;
import org.eclipse.ptp.rm.lml.ui.UIUtils;
import org.eclipse.ptp.rm.lml.ui.providers.EventForwarder;
import org.eclipse.ptp.rm.lml.ui.providers.LMLViewPart;
import org.eclipse.ptp.rm.lml.ui.views.NodesView;
import org.eclipse.ptp.rm.lml.ui.views.TableView;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

public class ViewManager {

	public class ViewListener implements IViewListener {

		public void handleEvent(final ILguiAddedEvent e) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					selectedLgui = e.getLguiItem();
					generateNewViews();
					selectedLgui.getObjectStatus().addComponent(new EventForwarder());
				}
			});
			
		}

		public void handleEvent(final ILguiRemovedEvent e) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					deleteOldViews();
					selectedLgui = null;
					generateNewViews();
				}
			});
		}

		public void handleEvent(final ILguiSelectedEvent e) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					deleteOldViews();
					selectedLgui = e.getLguiItem();
					if (selectedLgui != null) {
						generateNewViews();
					}
				}
			});
		}

		public void handleEvent(final IViewAddedEvent e) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					addView(e.getGid(), e.getType());
				}
			});
		}

		public void handleEvent(IViewDisposedEvent e) {
		}

	}

	public class RunNodedisplayUIJob extends UIJob {

		private String gid = null;
		private IViewPart view = null;

		public RunNodedisplayUIJob(String gid, IViewPart view) {
			super(gid);
			this.gid = gid;
			this.view = view;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			((NodesView) view).generateNodesdisplay(gid);
			return Status.OK_STATUS;
		}

	}

	public class RunTableUIJob extends UIJob {

		private String gid = null;
		private IViewPart view = null;

		public RunTableUIJob(String gid, IViewPart view) {
			super(gid);
			this.gid = gid;
			this.view = view;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			((TableView) view).generateTable(gid);
			return Status.OK_STATUS;
		}

	}

	public static IWorkbenchPage getPage() {
		final IWorkbenchPage page[] = new IWorkbenchPage[] { null };
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {

			public void run() throws Exception {
				System.out.println(PTPUIPlugin.getActivePage());
				page[0] = PTPUIPlugin.getActivePage();
			}
		});
		return page[0];
	}

	protected ILguiItem selectedLgui = null;

	public static LMLManager lmlManager = LMLManager.getInstance();

	public IViewListener viewListener = new ViewListener();

	public int i = 0;

	public int j = 0;

	public ViewManager() {
		lmlManager.addListener(viewListener);
	}

	public void shutDown() {
		lmlManager.removeListener(viewListener);
	}

	private void deleteOldViews() {
		
		IWorkbenchPage activePage = LMLUIPlugin.getActiveWorkbenchWindow().getActivePage();
		IViewReference[] views = activePage.getViewReferences();
		for (IViewReference view : views) {
			if (view.getPartName().equals("Resource Managers")) {
			} else {
				if (view.getView(false) instanceof LMLViewPart) {
					((LMLViewPart) view.getView(false)).prepareDispose();
				}
				activePage.hideView(view);
				view = null;
			} 
		}
		i = 0;
		j = 0;
	}

	private void generateTable(String gid) {
		try {
			IWorkbenchPage activePage = LMLUIPlugin.getActiveWorkbenchWindow().getActivePage();
			IViewPart view = activePage.showView(ILMLUIConstants.VIEW_TABLE, Integer.toString(i), activePage.VIEW_VISIBLE);
			RunTableUIJob job = new RunTableUIJob(gid, view);
			job.setUser(true);
			job.schedule();
			i++;
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private void generateNodedisplay(String gid) {
		try {
			IWorkbenchPage activePage = LMLUIPlugin.getActiveWorkbenchWindow().getActivePage();
			IViewPart view = activePage.showView(ILMLUIConstants.VIEW_PARALLELNODES, Integer.toString(j), activePage.VIEW_VISIBLE);
			RunNodedisplayUIJob job = new RunNodedisplayUIJob(gid, view);
			job.setUser(true);
			job.schedule();
			j++;
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private void generateNewViews() {
		String[] activeTableLayoutsGid = new String[0]; 
		if (selectedLgui != null) {
			activeTableLayoutsGid = selectedLgui.getLayoutAccess().getActiveTableLayoutsGid();
		}
		for (String gid : activeTableLayoutsGid) {
			generateTable(gid);
		}
		if (activeTableLayoutsGid.length == 0) {
			IWorkbenchPage activePage = LMLUIPlugin.getActiveWorkbenchWindow().getActivePage();
			try {
				IViewPart view = activePage.showView(ILMLUIConstants.VIEW_TABLE, Integer.toString(i), activePage.VIEW_VISIBLE);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			i++;
		}
		String[] activeNodedisplayLayoutGid = new String[0];
		if (selectedLgui !=  null) {
			activeNodedisplayLayoutGid = selectedLgui.getLayoutAccess().getActiveNodedisplayLayoutGid();
		}
		for (String gid : activeNodedisplayLayoutGid) {
			generateNodedisplay(gid);
		}
		if (activeNodedisplayLayoutGid.length == 0) {
			IWorkbenchPage activePage = LMLUIPlugin.getActiveWorkbenchWindow().getActivePage();
			try {
				IViewPart view = activePage.showView(ILMLUIConstants.VIEW_PARALLELNODES, Integer.toString(j), activePage.VIEW_VISIBLE);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			j++;
		}
	}

	private void addView(String gid, String type) {
		if (type.equals("table")) {
			generateTable(gid);
		} else if (type.equals("nodedisplay")) {
			generateNodedisplay(gid);
		} else {
			MessageBox messageBox = new MessageBox(LMLUIPlugin.getDisplay().getActiveShell(), SWT.OK | SWT.ERROR);
			messageBox.setMessage("There is no graphical component with this id.");
		}
	}
}
