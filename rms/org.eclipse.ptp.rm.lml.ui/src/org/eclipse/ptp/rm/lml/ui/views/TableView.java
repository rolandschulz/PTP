/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */

package org.eclipse.ptp.rm.lml.ui.views;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.internal.core.model.Cell;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.internal.core.model.jobs.JobStatusData;
import org.eclipse.ptp.rm.lml.ui.UIUtils;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.ui.providers.EventForwarder;
import org.eclipse.ptp.rm.lml.ui.providers.LMLViewPart;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

public class TableView extends LMLViewPart {

	private final class LMLTableListListener implements ILMLListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.ILguiListener# handleEvent
		 * (org.eclipse.ptp.core.events.IJobListSortEvent)
		 */
		public void handleEvent(IJobListSortedEvent e) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					setViewerInput();
				}
			});

		}

		public void handleEvent(ILguiAddedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					if (composite != null) {
						fSelectedLguiItem = lmlManager.getSelectedLguiItem();
						if (fSelectedLguiItem != null) {
							disposeTable();
							createTable();
							fSelectedLguiItem.getObjectStatus().addComponent(eventForwarder);
							componentAdded = true;
						}
					}
				}
			});

		}

		public void handleEvent(ILguiRemovedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					if (composite != null) {
						if (componentAdded) {
							fSelectedLguiItem.getObjectStatus().removeComponent(eventForwarder);
							componentAdded = false;
						}
						fSelectedLguiItem = null;
						setViewerInput();
						disposeTable();
					}
				}
			});
		}

		public void handleEvent(IMarkObjectEvent event) {
			selectedOid = event.getOid();
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					if (!composite.isDisposed()) {
						viewer.refresh();
					}
				}
			});
		}

		public void handleEvent(ISelectedObjectChangeEvent event) {
			final String oid = event.getOid();
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					if (!composite.isDisposed() && viewer.getInput() != null) {
						tree.deselectAll();

						Row[] rows = null;
						if (viewer.getInput() instanceof Row[]) {
							rows = (Row[]) viewer.getInput();
						}
						int index = -1;
						if (rows != null) {
							for (int i = 0; i < rows.length; i++) {
								if (rows[i].oid.equals(oid)) {
									index = i;
									break;
								}
							}
						}
						if (index > -1) {
							tree.select(tree.getItem(index));
						}
					}

				}
			});

		}

		public void handleEvent(ITableColumnChangeEvent e) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					disposeTable();
					createTable();
				}
			});

		}

		public void handleEvent(IUnmarkObjectEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					selectedOid = null;
					if (!composite.isDisposed()) {
						viewer.refresh();
					}
				}
			});

		}

		public void handleEvent(IUnselectedObjectEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					if (!composite.isDisposed()) {
						tree.deselectAll();
					}
				}
			});
		}

		public void handleEvent(IViewUpdateEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					if (composite != null) {
						if (selectedItem != null) {
							lmlManager.unmarkObject(selectedItem.getData().toString());
							selectedItem = null;
						}
						if (componentAdded) {
							fSelectedLguiItem.getObjectStatus().removeComponent(eventForwarder);
							componentAdded = false;
						}
						fSelectedLguiItem = lmlManager.getSelectedLguiItem();
						setViewerInput();
						fSelectedLguiItem.getObjectStatus().addComponent(eventForwarder);
						componentAdded = true;
					}
				}
			});
		}
	}

	private Composite composite;
	private Menu headerMenu;
	private Tree tree;
	private TreeColumn[] treeColumns;
	private int[] savedColumnWidths;
	private TreeColumnLayout treeColumnLayout;
	private TreeViewer viewer;
	private ILguiItem fSelectedLguiItem = null;
	private String gid = null;
	private final ILMLListener lmlListener = new LMLTableListListener();
	private final LMLManager lmlManager = LMLManager.getInstance();
	private TreeItem selectedItem = null;
	private String selectedOid = null;

	private boolean componentAdded = false;
	private boolean isMouseDown = false;

	private final EventForwarder eventForwarder = new EventForwarder();

	private final Map<String, JobStatusData> jobs = Collections.synchronizedMap(new TreeMap<String, JobStatusData>());

	private static final int UNDEFINED = -1;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	/**
	 * Exercises a control operation on the remote job.
	 * 
	 * @param job
	 * @param autoStart
	 * @param operation
	 * @throws CoreException
	 */
	public void callDoControl(JobStatusData job, boolean autoStart, String operation, IProgressMonitor monitor)
			throws CoreException {
		final IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(job.getRmId());
		final IResourceManagerControl control = rm.getControl();
		if (checkControl(rm, control, autoStart)) {
			control.control(job.getJobId(), operation, monitor);
			maybeUpdateJobState(job, autoStart, monitor);
		}
	}

	private boolean checkControl(IResourceManager manager, final IResourceManagerControl control, boolean autoStart)
			throws CoreException {
		boolean ok = false;
		if (control != null) {
			if (manager.getState().equals(IResourceManager.STARTED_STATE)) {
				ok = true;
			} else if (autoStart) {
				final Job j = new Job(IResourceManager.STARTING_STATE + JobStatusData.COSP + manager.getName()) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							control.start(monitor);
						} catch (final CoreException t) {
							return CoreExceptionUtils.getErrorStatus(t.getMessage(), t);
						}
						return Status.OK_STATUS;
					}
				};
				j.schedule();

				try {
					j.join();
				} catch (final InterruptedException ignored) {
				}

				ok = j.getResult().getSeverity() == IStatus.OK;
			}
		}
		return ok;
	}

	/**
	 * 
	 * @param tableViewer
	 * @param fSelected
	 */
	private void createColumns() {
		// Creating the columns
		if (fSelectedLguiItem.isLayout()) {
			return;
		}

		final ITableColumnLayout[] tableColumnLayouts = fSelectedLguiItem.getTableHandler().getTableColumnLayout(gid);
		if (tableColumnLayouts == null) {
			return;
		}

		int numCols = fSelectedLguiItem.getTableHandler().getNumberOfTableColumns(gid);
		treeColumns = new TreeColumn[numCols];
		savedColumnWidths = new int[numCols];

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				final Display display = treeColumns[0].getDisplay();
				final Image image = new Image(display, 12, 12);
				final GC gc = new GC(image);
				if (((Row) element).oid != null && (selectedOid == null || ((Row) element).oid.equals(selectedOid))) {
					final LMLColor color = ((Row) element).color;
					gc.setBackground(new Color(display, color.getRed(), color.getGreen(), color.getBlue()));
				} else {
					gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
				}
				gc.fillRectangle(image.getBounds().x, image.getBounds().y, image.getBounds().width, image.getBounds().height);
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				gc.drawRectangle(image.getBounds().x, image.getBounds().y, image.getBounds().width - 1,
						image.getBounds().height - 1);
				gc.dispose();
				return image;
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setMoveable(true);
		createMenuItem(headerMenu, treeColumn, 0);
		treeColumnLayout.setColumnData(treeColumn, new ColumnPixelData(40, true));

		for (int i = 0; i < tableColumnLayouts.length; i++) {
			treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
			final int cellNumber = i;
			treeViewerColumn.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(ViewerCell cell) {
					cell.setText(((Row) cell.getElement()).cells[cellNumber].value);
				}
			});
			treeColumn = treeViewerColumn.getColumn();
			treeColumn.setMoveable(true);
			treeColumn.setText(tableColumnLayouts[i].getTitle());
			treeColumn.setAlignment(getColumnAlignment(tableColumnLayouts[i].getStyle()));
			createMenuItem(headerMenu, treeColumn, i + 1);
			treeColumnLayout.setColumnData(treeColumn, new ColumnWeightData(tableColumnLayouts[i].getWidth(),
					ColumnWeightData.MINIMUM_WIDTH, true));
			treeColumns[i] = treeColumn;
		}

		// Sorting of every column
		final Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				final TreeColumn currentColumn = (TreeColumn) e.widget;

				if (tree.getSortColumn() == currentColumn) {
					tree.setSortDirection(tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
				} else {
					tree.setSortColumn(currentColumn);
					tree.setSortDirection(SWT.UP);
				}
				int sortIndex = 0;
				for (int i = 0; i < treeColumns.length; i++) {
					if (treeColumns[i] == tree.getSortColumn()) {
						sortIndex = i;
					}
				}
				fSelectedLguiItem.getTableHandler().sort(gid, SWT.UP, sortIndex, tree.getSortDirection());
				tree.setSortDirection(tree.getSortDirection());
				lmlManager.sortLgui();
			}
		};
		for (final TreeColumn col : treeColumns) {
			col.addListener(SWT.Selection, sortListener);
		}

		// Mouse action (in combination with nodedisplay)
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				isMouseDown = true;
				if (e.button == 1) {
					final TreeItem item = tree.getItem(new Point(e.x, e.y));
					if (!composite.isDisposed()) {
						lmlManager.markObject(item.getData().toString());
					}
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					final TreeItem item = tree.getItem(new Point(e.x, e.y));
					if (!composite.isDisposed()) {
						lmlManager.unmarkObject(item.getData().toString());
					}
				}
				isMouseDown = false;
			}

		});
		tree.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				final TreeItem item = tree.getItem(new Point(e.x, e.y));
				if (item == null) {
					return;
				}
				if (selectedItem != null && !selectedItem.equals(item)) {
					if (!composite.isDisposed()) {
						lmlManager.unselectObject(selectedItem.getData().toString());
					}
				}
				selectedItem = item;
				if (!composite.isDisposed()) {
					lmlManager.selectObject(selectedItem.getData().toString());
				}
			}

		});
		tree.addMouseTrackListener(new MouseTrackListener() {

			public void mouseEnter(MouseEvent e) {
				// nothing
			}

			public void mouseExit(MouseEvent e) {
				if (selectedItem != null) {
					lmlManager.unselectObject(selectedItem.getData().toString());
					selectedItem = null;
				}
			}

			public void mouseHover(MouseEvent e) {
				// nothing
			}
		});

	}

	private void createMenuItem(Menu parent, final TreeColumn column, final int index) {
		final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
		itemName.setText(column.getText());
		itemName.setSelection(column.getResizable());
		itemName.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (itemName.getSelection()) {
					column.setWidth(savedColumnWidths[index]);
					column.setResizable(true);
				} else {
					savedColumnWidths[index] = column.getWidth();
					column.setWidth(0);
					column.setResizable(false);
				}
			}
		});

	}

	@Override
	public void createPartControl(Composite parent) {
		gid = getViewSite().getId();
		composite = new Composite(parent, SWT.None);
		treeColumnLayout = new TreeColumnLayout();
		composite.setLayout(treeColumnLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		viewer = new TreeViewer(composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.VIRTUAL);
		viewer.getTree().setLayout(new TableLayout());
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ILazyTreeContentProvider() {
			private Row[] rows;

			public void dispose() {
				// Nothing
			}

			public Object getParent(Object element) {
				if (element instanceof Cell) {
					return ((Cell) element).row;
				}
				return rows;
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				this.rows = (Row[]) newInput;
			}

			public void updateChildCount(Object element, int currentChildCount) {
				// Nothing
			}

			public void updateElement(Object parent, int index) {
				Object element;
				if (parent instanceof Row) {
					element = ((Row) parent).cells[index];
				} else {
					element = rows[index];
				}
				viewer.replace(parent, index, element);
				updateChildCount(element, -1);
			}
		});
		viewer.setUseHashlookup(true);

		headerMenu = new Menu(composite);

		tree = viewer.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tree.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				tree.setMenu(headerMenu);
			}
		});

		/*
		 * Get the selected LguiItem (if there is one) so that the table will be
		 * populated when the it is first created
		 */
		fSelectedLguiItem = lmlManager.getSelectedLguiItem();

		createTable();
	}

	private void createTable() {
		if (fSelectedLguiItem == null) {
			return;
		}
		createColumns();

		// Insert the input
		setViewerInput();

		// Part for the controlling Monitor - context menu
		final MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		final Control control = viewer.getControl();
		final Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(contextMenu, viewer);

		composite.layout();
	}

	private void disposeTable() {
		final TreeColumn[] oldColumns = tree.getColumns();
		for (final TreeColumn oldColumn : oldColumns) {
			final Listener[] oldListeners = oldColumn.getListeners(SWT.Selection);
			for (final Listener oldListener : oldListeners) {
				oldColumn.removeListener(SWT.Selection, oldListener);
			}
			oldColumn.dispose();
		}
		treeColumns = new TreeColumn[0];
		getViewSite().getActionBars().getMenuManager().removeAll();
	}

	/**
	 * Fetches the remote stdout/stderr contents. This is functionality imported
	 * from JAXB core to avoid dependencies.
	 * 
	 * @param rmId
	 *            resource manager unique name
	 * @param path
	 *            of remote file.
	 * @param autoStart
	 *            start the resource manager if it is not started
	 * @return contents of the file, or empty string if path is undefined.
	 */
	public String doRead(final String rmId, final String path, final boolean autoStart) throws CoreException {

		if (path == null) {
			return JobStatusData.ZEROSTR;
		}
		final StringBuffer sb = new StringBuffer();
		final Job j = new Job(path) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
				final IResourceManagerControl control = rm.getControl();
				final SubMonitor progress = SubMonitor.convert(monitor, 100);
				try {
					if (checkControl(rm, control, autoStart)) {
						final String remoteServicesId = control.getControlConfiguration().getRemoteServicesId();
						if (remoteServicesId != null) {
							final IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(
									remoteServicesId, progress.newChild(25));
							final IRemoteConnectionManager remoteConnectionManager = remoteServices.getConnectionManager();
							final String remoteConnectionName = control.getControlConfiguration().getConnectionName();
							final IRemoteConnection remoteConnection = remoteConnectionManager.getConnection(remoteConnectionName);
							final IRemoteFileManager remoteFileManager = remoteServices.getFileManager(remoteConnection);
							final IFileStore lres = remoteFileManager.getResource(path);
							final BufferedInputStream is = new BufferedInputStream(lres.openInputStream(EFS.NONE,
									progress.newChild(25)));
							final byte[] buffer = new byte[COPY_BUFFER_SIZE];
							int rcvd = 0;
							try {
								while (true) {
									try {
										rcvd = is.read(buffer, 0, COPY_BUFFER_SIZE);
									} catch (final EOFException eof) {
										break;
									}

									if (rcvd == UNDEFINED) {
										break;
									}
									if (progress.isCanceled()) {
										break;
									}
									sb.append(new String(buffer, 0, rcvd));
								}
							} finally {
								try {
									is.close();
								} catch (final IOException ioe) {
									ioe.printStackTrace();
								}
								monitor.done();
							}
						}
					}
				} catch (final Throwable t) {
					return CoreExceptionUtils.getErrorStatus(t.getMessage(), t);
				}
				return Status.OK_STATUS;
			}
		};

		j.schedule();

		try {
			j.join();
		} catch (final InterruptedException ignored) {
		}

		return sb.toString();
	}

	private int getColumnAlignment(String alignment) {
		if (alignment.equals("LEFT")) { //$NON-NLS-1$
			return SWT.LEAD;
		}
		if (alignment.equals("RIGHT")) { //$NON-NLS-1$
			return SWT.TRAIL;
		}
		return SWT.LEAD;
	}

	@Override
	public void init(IViewSite site) {
		try {
			super.init(site);
		} catch (final PartInitException e) {
			e.printStackTrace();
		}
		lmlManager.addListener(lmlListener, this.getClass().getName());
	}

	/**
	 * Set the flags if this update carries ready info for the output files.
	 * 
	 * @param job
	 */
	private void maybeCheckFiles(JobStatusData job) {
		if (IJobStatus.JOB_OUTERR_READY.equals(job.getStateDetail())) {
			if (job.getOutputPath() != null) {
				job.setOutReady(true);
			}
			if (job.getErrorPath() != null) {
				job.setErrReady(true);
			}
		}
	}

	/**
	 * 
	 * @param job
	 * @param autoStart
	 * @param monitor
	 * @throws CoreException
	 */
	public void maybeUpdateJobState(JobStatusData job, boolean autoStart, IProgressMonitor monitor) throws CoreException {
		final IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(job.getRmId());
		final IResourceManagerControl control = rm.getControl();
		if (checkControl(rm, control, autoStart)) {
			final IJobStatus refreshed = control.getJobStatus(job.getJobId(), monitor);
			job.updateState(refreshed);
			maybeCheckFiles(job);
			refresh();
		}
	}

	@Override
	public void prepareDispose() {
		lmlManager.removeListener(lmlListener);
	}

	/**
	 * Refresh the viewer.
	 */
	public void refresh() {
		new UIJob(Messages.JobListUpdate) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				viewer.setInput(jobs.values());
				viewer.refresh();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/**
	 * Delete job status entry and refresh.
	 * 
	 * @param jobId
	 */
	public void removeJob(String jobId) {
		jobs.remove(jobId);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void setViewerInput() {
		/*
		 * Don't change input if mouse is down as this causes a SIGSEGV in SWT!
		 */
		if (!isMouseDown) {
			Row[] input = new Row[0];
			if (fSelectedLguiItem != null) {
				input = fSelectedLguiItem.getTableHandler().getTableDataWithColor(gid);
			}
			if (!composite.isDisposed()) {
				viewer.setInput(input);
				viewer.getTree().setItemCount(input.length);
			}
		}
	}
}
