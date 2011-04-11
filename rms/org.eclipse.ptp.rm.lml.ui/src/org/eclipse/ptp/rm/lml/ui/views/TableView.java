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
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.ui.providers.LMLViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 
 * @author Claudia Knobloch
 *
 */
public class TableView extends LMLViewPart {
	private final class LMLJobsListListener implements ILMLListener {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.lml.core.listeners.ILguiListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.events.IJobListSortEvent)
		 */
		public void handleEvent(IJobListSortedEvent e) {
			viewer.getTable().removeAll();
			viewer.add(fSelectedLguiItem.getTableHandler().getTableData(tableType));
		}
	}
	
	private Composite composite;
	private Table table;
	private TableColumn[] tableColumns;
	public TableViewer viewer;
	public Point sizeViewer;
	public ILguiItem fSelectedLguiItem = null;
	public int tableNumber = 0;
	public String tableType = null;
	private ILMLListener lmlListener = new LMLJobsListListener();
	private ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
	
	public void createPartControl(Composite parent) {
		this.composite = parent;
		parent.setLayout(new FillLayout());
		viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER| SWT.Resize);
		viewer.setLabelProvider(new ITableLabelProvider() {
			public void addListener(ILabelProviderListener listener) {				
			}
			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
				
			}

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				return ((String[]) element)[columnIndex];
			}
			
		});
		viewer.setContentProvider(new ArrayContentProvider());
		lmlManager.addListener(lmlListener, this.getClass().getName());
		composite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (fSelectedLguiItem == null) {
					return;
				}
				TableColumn[] tableColumns = table.getColumns();
				Double[] widths = new Double[tableColumns.length];
				Double widthColumn = Integer.valueOf(sizeViewer.x).doubleValue() / tableColumns.length;
				for (int i = 0; i < tableColumns.length; i++) {
					widths[i] = tableColumns[i].getWidth() / widthColumn;
					fSelectedLguiItem.getTableHandler().changeTableColumnsWidth(widths, tableType);
				}
				disposeTable();
				createTable();
			}
		});
	}
	
	public void generateTable(int tableNumber) {
		this.tableNumber = tableNumber;
		fSelectedLguiItem = lmlManager.getSelectedLguiItem();
		createTable();
	}
	
	private void disposeTable() {
		if (fSelectedLguiItem != null) {
			viewer.getTable().removeAll();
			
			TableColumn[] oldColumns = viewer.getTable().getColumns();
			for (int i = 0; i < oldColumns.length; i++) {
				Listener[]  oldListeners = oldColumns[i].getListeners(SWT.Selection);
				for (int j = 0; j < oldListeners.length; j++) {
					oldColumns[i].removeListener(SWT.Selection, oldListeners[j]);
				}
				oldColumns[i].dispose();
			}
		}
	}
	
	private void createTable() {
		table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		tableType = fSelectedLguiItem.getTableHandler().getTableTitle(tableNumber);
		createColumns();
		viewer.add(fSelectedLguiItem.getTableHandler().getTableData(tableType));
	}

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
		this.setPartName(tableType);
		tableColumns = new TableColumn[fSelectedLguiItem.getTableHandler().getTableColumnNumber(tableType)];
		sizeViewer = composite.getSize();
		ITableColumnLayout[] jobTableColumnLayouts = fSelectedLguiItem.getTableHandler().getTableColumnLayout(tableType, sizeViewer.x);
		if (fSelectedLguiItem.isLayout()) {
			return;
		}
		for (int i = 0; i < jobTableColumnLayouts.length; i++) {
			TableColumn tableColumn = new TableColumn(table, getColumnAlignment(jobTableColumnLayouts[i].getStyle()));
			
			tableColumn.setText(jobTableColumnLayouts[i].getTitle());
			tableColumn.setWidth(jobTableColumnLayouts[i].getWidth());
			tableColumn.setMoveable(true);
			tableColumns[i] = tableColumn;	
		}
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
				fSelectedLguiItem.getTableHandler().sort(tableType, SWT.UP, sortIndex, table.getSortDirection());
				table.setSortDirection(table.getSortDirection());
				LMLCorePlugin.getDefault().getLMLManager().sortLgui();
			}
		}; 
		for (int i = 0; i < tableColumns.length; i++) {
			tableColumns[i].addListener(SWT.Selection, sortListener);
			// TODO ColumnChange
//			tableColumns[i].addControlListener(new ControlAdapter() {
//
//				public void controlMoved(ControlEvent e) {
//					System.out.println(e.getSource());
//				}
//			});
		}

	}
	public void prepareDispose() {
		TableColumn[] tableColumns = table.getColumns();
		Double[] widths = new Double[tableColumns.length];
		Double widthColumn = Integer.valueOf(sizeViewer.x).doubleValue() / tableColumns.length;
		for (int i = 0; i < tableColumns.length; i++) {
			widths[i] = tableColumns[i].getWidth() / widthColumn;
			fSelectedLguiItem.getTableHandler().changeTableColumnsWidth(widths, tableType);
		}
		lmlManager.removeListener(lmlListener);
	}

}
