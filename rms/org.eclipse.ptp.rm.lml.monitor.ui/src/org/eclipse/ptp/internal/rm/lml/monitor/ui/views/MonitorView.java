/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.monitor.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.MonitorImages;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.core.listeners.IMonitorChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 6.0
 */
public class MonitorView extends ViewPart {
	private abstract class CenterImageLabelProvider extends OwnerDrawLabelProvider {

		protected abstract Image getImage(Object element);

		@Override
		protected void measure(Event event, Object element) {
			// Do nothing
		}

		@Override
		protected void paint(Event event, Object element) {

			Image img = getImage(element);

			if (img != null) {
				Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
				Rectangle imgBounds = img.getBounds();
				bounds.width /= 2;
				bounds.width -= imgBounds.width / 2;
				bounds.height /= 2;
				bounds.height -= imgBounds.height / 2;

				int x = bounds.width > 0 ? bounds.x + bounds.width : bounds.x;
				int y = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;

				event.gc.drawImage(img, x, y);
			}
		}
	}

	private class MonitorChangedListener implements IMonitorChangedListener {

		@Override
		public void monitorAdded(IMonitorControl[] monitors) {
			fViewer.refresh();
		}

		@Override
		public void monitorRemoved(IMonitorControl[] monitors) {
			fViewer.refresh();
		}

		@Override
		public void monitorUpdated(IMonitorControl[] monitors) {
			fViewer.refresh();
		}
	}

	private class SystemsViewSorter extends ViewerComparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface
		 * .viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			String name1 = null;
			String name2 = null;
			if (e1 instanceof IMonitorControl) {
				name1 = ((IMonitorControl) e1).getConfigurationName();
			}
			if (e2 instanceof IMonitorControl) {
				name2 = ((IMonitorControl) e2).getConfigurationName();
			}
			if (name1 != null && name2 != null) {
				int res = name1.compareTo(name2);
				if (res == 0) {
					res = ((IMonitorControl) e1).getConnectionName().compareTo(((IMonitorControl) e2).getConnectionName());
				}
				return res;
			}
			return super.compare(viewer, e1, e2);
		}
	}

	private TableViewer fViewer;
	private final TableColumnLayout fTableColumnLayout = new TableColumnLayout();
	private final MonitorChangedListener fMonitorChangedListener = new MonitorChangedListener();

	private void createColumns() {
		String[] columnTitles = { Messages.MonitorView_Status, Messages.MonitorView_ConnectionName,
				Messages.MonitorView_ConfigurationName };

		// the remaining columns
		for (int i = 0; i < columnTitles.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(fViewer, SWT.NONE);
			final int cellNumber = i;
			switch (cellNumber) {
			case 0:
				tableViewerColumn.setLabelProvider(new CenterImageLabelProvider() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.jface.viewers.ColumnLabelProvider
					 * #getImage(java.lang.Object)
					 */
					@Override
					public Image getImage(Object element) {
						IMonitorControl monitor = (IMonitorControl) element;
						return monitor.isActive() ? MonitorImages.get(MonitorImages.IMG_STARTED) : null;
					}
				});
				break;
			case 1:
			case 2:
				tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.jface.viewers.ColumnLabelProvider#getText
					 * (java.lang.Object)
					 */
					@Override
					public String getText(Object element) {
						IMonitorControl monitor = (IMonitorControl) element;
						switch (cellNumber) {
						case 1:
							return monitor.getConnectionName();
						case 2:
							return monitor.getConfigurationName();
						}
						return null;
					}
				});
				break;
			}
			tableViewerColumn.getColumn().setText(columnTitles[i]);
			tableViewerColumn.getColumn().pack();
			int width = tableViewerColumn.getColumn().getWidth();
			fTableColumnLayout.setColumnData(tableViewerColumn.getColumn(), new ColumnWeightData((i > 0) ? 100 : 0, width));
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.None);
		composite.setLayout(fTableColumnLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		fViewer = new TableViewer(composite, SWT.SINGLE | SWT.FULL_SELECTION);
		fViewer.setContentProvider(ArrayContentProvider.getInstance());
		fViewer.setComparator(new SystemsViewSorter());
		fViewer.setUseHashlookup(true);
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				MonitorControlManager.getInstance().fireSelectionChanged(event);
			}
		});
		fViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					final IMonitorControl control = (IMonitorControl) selection.getFirstElement();
					Job job = new Job(Messages.MonitorView_ToggleMonitor) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								if (!control.isActive()) {
									control.start(monitor);
								} else {
									final boolean[] stop = new boolean[1];
									Display.getDefault().syncExec(new Runnable() {
										@Override
										public void run() {
											stop[0] = MessageDialog.openQuestion(fViewer.getControl().getShell(),
													Messages.MonitorView_StopMonitor, Messages.MonitorView_AreYouSure);
										}

									});
									if (stop[0]) {
										control.stop();
									}
								}
							} catch (CoreException e) {
								final String message = e.getLocalizedMessage();
								final IStatus status = e.getStatus();
								Display.getDefault().syncExec(new Runnable() {
									@Override
									public void run() {
										ErrorDialog.openError(fViewer.getControl().getShell(), Messages.MonitorView_ToggleMonitor,
												message, status);
									}

								});
							}
							return Status.OK_STATUS;
						}

					};
					job.schedule();
				}
			}
		});

		createColumns();

		fViewer.setInput(MonitorControlManager.getInstance().getMonitorControls());

		fViewer.getTable().setLinesVisible(true);
		fViewer.getTable().setHeaderVisible(true);

		/*
		 * Enable property sheet updates when tree items are selected. Note for
		 * this to work each item in the tree must either implement
		 * IPropertySource, or support IPropertySource.class as an adapter type
		 * in its AdapterFactory.
		 */
		getSite().setSelectionProvider(fViewer);

		MonitorControlManager.getInstance().addMonitorChangedListener(fMonitorChangedListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		MonitorControlManager.getInstance().removeMonitorChangedListener(fMonitorChangedListener);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		// TODO Auto-generated method stub
		super.init(site);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
