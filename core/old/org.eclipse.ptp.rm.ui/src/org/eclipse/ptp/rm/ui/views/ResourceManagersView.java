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
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.views;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rm.ui.util.AutoResizeTableLayout;
import org.eclipse.ptp.rm.core.ResourceManagerFactory;
import org.eclipse.ptp.rm.core.ResourceManagerPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/**
 * This is the view for the extant set of resource managers. It also allows one
 * to chose the ResourceManagerPlugin's current resource manager.
 * 
 * @author rsqrd
 * 
 */
public class ResourceManagersView extends ViewPart {
	/**
	 * The {@link {@link LabelProvider}} required by {@link TableViewer}
	 * 
	 * @author rsqrd
	 * 
	 */
	public class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return getImage(element);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			ResourceManagerFactory factory = (ResourceManagerFactory) element;
			switch (columnIndex) {
			case 0:
				return factory.getName();
			case 1:
				return factory.getType();
			default:
				throw new IllegalArgumentException(
						"bad column index in Resource Managers View");
			}
		}

	}

	/**
	 * Listener for changes in the checked state of the table data.
	 * 
	 * @author rsqrd
	 * 
	 */
	class CheckStateListener implements ICheckStateListener {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		public void checkStateChanged(CheckStateChangedEvent event) {

			// ignore unchecking events
			if (!event.getChecked()) {
				viewer.setChecked(event.getElement(), true);
				return;
			}

			if (checkedFactory != null) {
				// uncheck the currently checked factory
				viewer.setChecked(checkedFactory, false);
			}

			// Set the current factory (and therefore the current manager?)
			// to the checked factory.
			
			checkedFactory = (ResourceManagerFactory) event.getElement();
			ResourceManagerPlugin.getDefault()
					.setCurrentFactory(checkedFactory);
		}

	}

	/**
	 * The {@link {@link IStructuredContentProvider}} required by
	 * {@link {@link TableViewer}}.
	 * 
	 * @author rsqrd
	 * 
	 */
	class ViewContentProvider implements IStructuredContentProvider {

		public ViewContentProvider() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// empty
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return factories;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// empty
		}

	}

	private CheckboxTableViewer viewer;

	private ResourceManagerFactory[] factories;

	private ResourceManagerFactory checkedFactory = null;

	public ResourceManagersView() {
		// empty
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		factories = getFactories();

		viewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		createTableStructure();

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.addCheckStateListener(new CheckStateListener());
		viewer.setInput(getViewSite());

		checkedFactory = ResourceManagerPlugin.getDefault().getCurrentFactory();
		viewer.setChecked(checkedFactory, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * create the table column structure
	 */
	private synchronized void createTableStructure() {
		final int numColumns = 2;

		final TableColumn[] tableColumns = new TableColumn[numColumns];

		final Table table = viewer.getTable();
		final AutoResizeTableLayout layout = new AutoResizeTableLayout(table);
		table.setLayout(layout);

		tableColumns[0] = new TableColumn(table, SWT.LEFT);
		tableColumns[0].setText("Name");
		layout.addColumnData(new ColumnWeightData(50));

		tableColumns[1] = new TableColumn(table, SWT.LEFT);
		tableColumns[1].setText("Type");
		layout.addColumnData(new ColumnWeightData(50));

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	/**
	 * @return the extant factories from the ResourceManagerPlugin 
	 * 
	 */
	private synchronized ResourceManagerFactory[] getFactories() {
		return ResourceManagerPlugin.getDefault().getFactories();
	}

}
