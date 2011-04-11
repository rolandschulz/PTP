/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RowType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TablelayoutType;

public class TableHandler extends LguiHandler {
	
	public class TableListener implements ILguiListener {

		@Override
		public void handleEvent(ILguiUpdatedEvent e) {
			
		}
		
	}
	
	private TableListener tableListener = new TableListener();
	

	/**
	 * Getting a list of all elements of type TableType from LguiType.
	 * @return list of elements(TableType)
	 */
	List<TableType> getTables() {
		List<TableType> tables = new LinkedList<TableType>();
		for (GobjectType tag : lguiItem.getOverviewAccess().getGraphicalObjects()) {
			if (tag instanceof TableType) {
				tables.add((TableType) tag);
			}
		}
		return tables;
	}
	
	public void changeTableColumnsWidth(Double[] widths, String tableType) {
		TableType table = getTable(tableType);
		TablelayoutType layout = lguiItem.getLayoutAccess().getTableLayout(table.getId());
		for (int i = 0; i < widths.length; i++) {
			layout.getColumn().get(i).setWidth(widths[i]);
		}
	}
	
	/**
	 * Getting an element(Table Type) which has an equal title to the argument tableType.
	 * @param tableType Title of the desired table
	 * @return Corresponding table to the desired table title
	 */
	TableType getTable(String tableType) {
		for (TableType tag : getTables()) {
			if (tag.getTitle().equals(tableType)) {
				return tag;
			}
		}
		return null;
	}
	
	public TableHandler(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);
		lguiItem.addListener(tableListener);
	}
	
	
	public int getNumberOfTables() {
		return getTables().size();
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsNumber(String tableType)
	 */
	public int getTableColumnNumber(String tableType) {
		TableType table = getTable(tableType);
		if (table != null) {
			return table.getColumn().size();
		}
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsNames(String tableType)
	 */
	public String[] getTableColumnsName(String tableType) {
		TableType table = getTable(tableType);
		String[] names = new String[table.getColumn().size()];
		for (int i = 0; i < names.length; i ++) {
			names[i] = table.getColumn().get(i).getName();
		}
		return names;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsWidth(String tableType, int widthTable)
	 */
	public int[] getTableColumnsWidth(String tableType, int widthTable) {
		TableType table = getTable(tableType);
		TablelayoutType tablelayout = lguiItem.getLayoutAccess().getTableLayout(table.getId());
		int[] widthInt = new int[table.getColumn().size()];
		
		double sumWidthColumns = 0;
		
		for(int i = 0; i < table.getColumn().size(); i++) {
			sumWidthColumns += tablelayout.getColumn().get(i).getWidth();		
		}
		
		
		for (int i = 0; i < widthInt.length; i ++) {
			widthInt[i] = (int) (tablelayout.getColumn().get(i).getWidth() * widthTable / sumWidthColumns);
		}
		return widthInt;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsStyle(String tableType)
	 */
	public String[] getTableColumnsStyle(String tableType) {
		TableType table = getTable(tableType);
		String[] style = new String[table.getColumn().size()];
		for (int i = 0; i < style.length; i++) {
			switch (table.getColumn().get(i).getSort().value().charAt(0)) {
			case 'a' :
			case 'd' :
				style[i] = "LEFT";
				break;
			case 'n' :
				style[i] = "RIGHT";
				break;
			default :
			}
		}
		return style;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getJobTableColumnLayout(String tableType, int widthTable)
	 */
	public ITableColumnLayout[] getTableColumnLayout(String tableType,
			int widthTable) {
		TableType table = getTable(tableType);
		TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(table.getId());
		if (tableLayout.getColumn().size() <= 0) {
			tableLayout = lguiItem.getLayoutAccess().getDefaultTableLayout(table);
		}
		ITableColumnLayout[] jobTableColumnLayouts = new ITableColumnLayout[table.getColumn().size()];
		double sumWidthColumns = 0;
		
		for(int i = 0; i < table.getColumn().size(); i++) {
			sumWidthColumns += tableLayout.getColumn().get(i).getWidth();		
		}
		
		
		for (int i = 0; i < jobTableColumnLayouts.length; i ++) {
			String style;
			switch (table.getColumn().get(i).getSort().value().charAt(0)) {
			case 'a' :
			case 'd' :
				style = "LEFT";
				break;
			case 'n' :
				style = "RIGHT";
				break;
			default :
				style = "LEFT";
			}
			jobTableColumnLayouts[i] = new TableColumnLayout(
					table.getColumn().get(i).getName(),
					(int) (tableLayout.getColumn().get(i).getWidth() * widthTable / sumWidthColumns),
					style);
		}
		return jobTableColumnLayouts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getJobTableData(String tableType)
	 */
	public String[][] getTableData(String tableType) {
		TableType table = getTable(tableType);
		String[][] jobTableData = new String[table.getRow().size()][];
		for (int i = 0; i < jobTableData.length; i++) {
			String[] jobTableDataRow = new String[table.getRow().get(i).getCell().size()];
			for (int j = 0; j < jobTableDataRow.length; j++) {
				jobTableDataRow[j] = table.getRow().get(i).getCell().get(j).getValue();
			}
			jobTableData[i] = jobTableDataRow;
		}
		return jobTableData;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTypeJobTableColumn(String tableType, int index)
	 */
	public String getTypeTableColumn(String tableType, int index) {
		return getTable(tableType).getColumn().get(index).getSort().value();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#sort(String tableType, int sortDirectionComparator, int sortIndex, int sortDirection)
	 */
	public void sort(String tableType, int sortDirectionComparator, int sortIndex, int sortDirection) {
		RowType[] jobTableData = getTable(tableType).getRow().toArray(
				new RowType[getTable(tableType).getRow().size()]);
		Arrays.sort(jobTableData, new TableSorter(getTypeTableColumn(tableType, sortIndex), sortDirectionComparator, sortIndex, sortDirection));
		getTable(tableType).getRow().clear();
		for (int i = 0; i < jobTableData.length; i++) {
			getTable(tableType).getRow().add(jobTableData[i]);
		}
	}
	
	public String getTableTitle(int i) {
		return getTables().get(i).getTitle();
	}
}
