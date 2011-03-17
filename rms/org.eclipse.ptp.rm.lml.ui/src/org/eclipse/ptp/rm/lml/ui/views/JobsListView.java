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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.elements.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author Claudia Knobloch
 *
 */
public class JobsListView extends ViewPart {
	
	/**
	 * 
	 * @author Claudia Knobloch
	 *
	 */
	private final class LguiJobsListListener implements ILguiListener {
		
		private TableViewer viewer;  
		
		public LguiJobsListListener(TableViewer viewer) {
			this.viewer = viewer;
		}
		
		public LguiJobsListListener() {
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.lml.core.listeners.ILguiListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.events.ILguiAddedEvent)
		 */
		public void handleEvent(ILguiAddedEvent e) {
			
			viewer.getTable().setRedraw(false);
			if (fSelectedLguiItem != null) {
				viewer.remove(viewer.getInput());
			}
			
			fSelectedLguiItem = e.getLgui();
			createColumns();
			
			viewer.add(fSelectedLguiItem.getJobTableData(tableType));
			
			viewer.getTable().setRedraw(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.lml.core.listeners.ILguiListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.events.ILguiAddedEvent)
		 */
		public void handleEvent(ILguiRemovedEvent e) {
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.lml.core.listeners.ILguiListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.events.IJobListSortEvent)
		 */
		public void handleEvent(IJobListSortedEvent e) {
			if (viewer.getInput() != null) {
				viewer.remove(viewer.getInput());
				viewer.setInput(fSelectedLguiItem.getJobTableData(tableType));
			} else {
				fSelectedLguiItem = e.getLgui();
				viewer.setInput(fSelectedLguiItem.getJobTableData(tableType));
			}
			
		}
	}
	
	/**
	 * 
	 */
	private Composite composite;
	
	/**
	 * 
	 */
	private Table table;
	
	/**
	 * 
	 */
	private TableColumn[] tableColumns;
	
	/**
	 * 
	 */
	public TableViewer viewer;

	/**
	 * 
	 */
	
	public ILguiItem fSelectedLguiItem = null;
	
	
	public String tableType = "joblist_RUN";
	/**
	 * 
	 */
	private ILguiListener lguiListener = new LguiJobsListListener();
	
	private ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
	
	/**
	 * 
	 */
	public JobsListView() {
		
	}

	/**
	 * 
	 */
	public void createPartControl(Composite parent) {
		this.composite = parent;
		parent.setLayout(new FillLayout());
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table = viewer.getTable();
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		lguiListener = new LguiJobsListListener(viewer);
		
		
		//
		createColumns();
		
		//
		viewer.setLabelProvider(new ITableLabelProvider() {

			@Override
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				return ((String[]) element)[columnIndex];
			}
			
		});
		viewer.setContentProvider(new ArrayContentProvider());
		
		viewer.setInput(null);
		
		getSite().setSelectionProvider(viewer);
		
		lmlManager.addListener(lguiListener);
	}

	/**
	 * 
	 */
	public void setFocus() {
	}
	
	private int getColumnAlignment(String alignment) {
		if (alignment.equals("LEFT")) {
			return SWT.LEAD;
		}
		if (alignment.equals("RIGHT")) {
			return SWT.TRAIL;
		}
			
		return 0;
	}
	
	/**
	 * 
	 * @param tableViewer
	 * @param fSelected
	 */
	private void createColumns() {
		// Disposing the old columns and listeners
		TableColumn[] oldColumns = viewer.getTable().getColumns();
		for (int i = 0; i < oldColumns.length; i++) {
			Listener[]  oldListeners = oldColumns[i].getListeners(SWT.Selection);
			for (int j = 0; j < oldListeners.length; j++) {
				oldColumns[i].removeListener(SWT.Selection, oldListeners[j]);
			}
			oldColumns[i].dispose();
		}
		
		
		fSelectedLguiItem = lmlManager.getSelectedLguiItem();
		
		// If there is no ILguiItem is selected the program stops
		if (fSelectedLguiItem == null) {
			return;
		}
		
		// Creating an array of TableColumns
		tableColumns = new TableColumn[fSelectedLguiItem.getTableColumnNumber(tableType)];
		
		Point sizeViewer = composite.getSize();
		
		ITableColumnLayout[] jobTableColumnLayouts = fSelectedLguiItem.getJobTableColumnLayout(tableType, sizeViewer.x);
		// If there is no tablelayout the program stops
		if (fSelectedLguiItem.isLayout()) {
			return;
		}
		
		
		// Creating the TableColumns and setting size and style
		for (int i = 0; i < jobTableColumnLayouts.length; i++) {
			TableColumn tableColumn = new TableColumn(table, getColumnAlignment(jobTableColumnLayouts[i].getStyle()));
			tableColumn.setText(jobTableColumnLayouts[i].getTitle());
			tableColumn.setWidth(jobTableColumnLayouts[i].getWidth());
			tableColumn.setMoveable(true);
			tableColumns[i] = tableColumn;
		}
		
		// Defining a sort listener
		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				TableColumn currentColumn = (TableColumn) e.widget;
				
				if (table.getSortColumn() == currentColumn) {
					table.setSortDirection(table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
				} else {
					table.setSortColumn(currentColumn);
					table.setSortDirection(SWT.UP);
				}
				int sortIndex = 0;
				for (int i = 0; i < tableColumns.length; i++) {
					if (tableColumns[i] == table.getSortColumn()) {
						sortIndex = i;
					}
				}
				fSelectedLguiItem.sort(tableType, SWT.UP, sortIndex, table.getSortDirection());
				
				table.setSortDirection(table.getSortDirection());
				LMLCorePlugin.getDefault().getLMLManager().sortLgui();
			}
		};
		
		// To every TableColumn the sort listener is assigned 
		for (int i = 0; i < tableColumns.length; i++) {
			tableColumns[i].addListener(SWT.Selection, sortListener);
		}
	}

}
