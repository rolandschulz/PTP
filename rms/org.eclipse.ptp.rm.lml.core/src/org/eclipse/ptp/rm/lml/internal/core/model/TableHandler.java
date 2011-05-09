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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.internal.core.elements.CellType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ColumnType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ColumnlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RowType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TablelayoutType;

public class TableHandler extends LguiHandler {

	public class TableListener implements ILguiListener {

		public void handleEvent(ILguiUpdatedEvent e) {
			gid = null;
			cids = null;
		}

	}

	private final TableListener tableListener = new TableListener();
	private BigInteger[] cids = null;
	private String gid = null;

	public TableHandler(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);
		lguiItem.addListener(tableListener);
	}

	public String getTableTitle(String gid) {
		if (this.gid != gid) {
			cids = null;
			this.gid = gid;
		}
		return getTable(gid).getTitle();
	}

	/**
	 * Getting a list of all elements of type TableType from LguiType.
	 * 
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

	/**
	 * Getting an element(Table Type) which has an equal title to the argument
	 * tableType.
	 * 
	 * @param tableType
	 *            Title of the desired table
	 * @return Corresponding table to the desired table title
	 */
	TableType getTable(String gid) {
		if (this.gid != gid) {
			cids = null;
			this.gid = gid;
		}
		for (TableType tag : getTables()) {
			if (tag.getId().equals(gid)) {
				return tag;
			}
		}
		return null;
	}

	public int getNumberOfTables() {
		return getTables().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsNumber(
	 * String tableType)
	 */
	public int getNumberOfTableColumns(String gid) {
		if (this.gid != gid) {
			cids = null;
			this.gid = gid;
		}
		TablelayoutType layout = lguiItem.getLayoutAccess().getTableLayout(gid);
		int activeColumn = 0;
		for (ColumnlayoutType column : layout.getColumn()) {
			if (column.isActive()) {
				activeColumn++;
			}
		}
		return activeColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsNames(String
	 * tableType)
	 */
	public String[] getTableColumnsName(String gid) {
		if (this.gid != gid || cids == null) {
			this.gid = gid;
		}
		getCidsToPosition();
		ColumnType[] columns = getColumnsToCids();
		String[] tableColumnNames = new String[cids.length];

		for (int i = 0; i < cids.length; i++) {
			tableColumnNames[i] = columns[i].getName();
		}
		return tableColumnNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsWidth(String
	 * tableType, int widthTable)
	 */
	public int[] getTableColumnsWidth(String gid, int widthTable) {
		TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		if (tableLayout.getColumn().size() <= 0) {
			tableLayout = lguiItem.getLayoutAccess().getDefaultTableLayout(gid);
		}
		if (this.gid != gid || cids == null) {
			this.gid = gid;
		}
		getCidsToPosition();
		ColumnlayoutType[] layoutColumns = lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid);
		int[] tableColumnWidths = new int[cids.length];
		double sumWidthColumns = 0;
		for (int i = 0; i < cids.length; i++) {
			sumWidthColumns += layoutColumns[i].getWidth();
		}
		for (int i = 0; i < cids.length; i++) {
			tableColumnWidths[i] = (int) (layoutColumns[i].getWidth() * widthTable / sumWidthColumns);
		}
		return tableColumnWidths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsStyle(String
	 * tableType)
	 */
	public String[] getTableColumnsStyle(String gid) {
		if (this.gid != gid || cids == null) {
			getCidsToPosition();
			this.gid = gid;
		}
		ColumnType[] columns = getColumnsToCids();
		String[] tableColumnStyles = new String[cids.length];

		for (int i = 0; i < cids.length; i++) {
			switch (columns[i].getSort().value().charAt(0)) {
			case 'a':
			case 'd':
				tableColumnStyles[i] = "LEFT";
				break;
			case 'n':
				tableColumnStyles[i] = "RIGHT";
				break;
			default:
				tableColumnStyles[i] = "LEFT";
			}
		}
		return tableColumnStyles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getJobTableColumnLayout
	 * (String tableType, int widthTable)
	 */
	public ITableColumnLayout[] getTableColumnLayout(String gid, int widthTable) {
		if (this.gid != gid) {
			this.gid = gid;
		}
		TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		if (tableLayout.getColumn().size() <= 0) {
			tableLayout = lguiItem.getLayoutAccess().getDefaultTableLayout(gid);
		}

		getCidsToPosition();

		ColumnType[] columns = getColumnsToCids();
		ColumnlayoutType[] layoutColumns = lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid);
		ITableColumnLayout[] tableColumnLayouts = new ITableColumnLayout[cids.length];

		double sumWidthColumns = 0;
		for (int i = 0; i < cids.length; i++) {
			sumWidthColumns += layoutColumns[i].getWidth();
		}

		for (int i = 0; i < cids.length; i++) {
			if (columns[i] == null) {
				tableColumnLayouts[i] = new TableColumnLayout(layoutColumns[i].getKey(), (int) (layoutColumns[i].getWidth()
						* widthTable / sumWidthColumns), "LEFT");
			} else {
				String style;
				switch (columns[i].getSort().value().charAt(0)) {
				case 'a':
				case 'd':
					style = "LEFT";
					break;
				case 'n':
					style = "RIGHT";
					break;
				default:
					style = "LEFT";
				}
				tableColumnLayouts[i] = new TableColumnLayout(columns[i].getName(),
						(int) (layoutColumns[i].getWidth() * widthTable / sumWidthColumns), style);
			}
		}
		return tableColumnLayouts;
	}

	public Row[] getTableDataWithColor(String gid) {
		if (this.gid != gid) {
			this.gid = gid;
		}
		getCidsToPosition();
		TableType table = getTable(gid);
		Row[] tableData = new Row[table.getRow().size()];
		for (int i = 0; i < tableData.length; i++) {
			tableData[i] = new Row(table.getRow().get(i).getOid());
			tableData[i].setColor(lguiItem.getOIDToObject().getColorById(table.getRow().get(i).getOid()));
			Cell[] tableDataRow = new Cell[cids.length];
			for (int j = 0; j < cids.length; j++) {
				for (CellType cell : table.getRow().get(i).getCell()) {
					if (cell.getCid().equals(cids[j])) {
						tableDataRow[j] = new Cell(cell.getValue(), tableData[i]);
						break;
					}
				}
				if (tableDataRow[j] == null) {
					tableDataRow[j] = new Cell("", tableData[i]);
				}
			}
			tableData[i].setCells(tableDataRow);
		}
		return tableData;
	}

	public void sort(String gid, int sortDirectionComparator, int sortIndex, int sortDirection) {
		if (this.gid != gid || cids == null) {
			getCidsToPosition();
			this.gid = gid;
		}
		RowType[] jobTableData = getTableData();
		Arrays.sort(jobTableData, new TableSorter(getTypeTableColumn(sortIndex), sortDirectionComparator, sortIndex, sortDirection));
		getTable(gid).getRow().clear();
		for (int i = 0; i < jobTableData.length; i++) {
			getTable(gid).getRow().add(jobTableData[i]);
		}
	}

	public String[] getTableColumnNonActive(String gid) {
		if (this.gid != gid || cids == null) {
			cids = null;
			this.gid = gid;
		}
		TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		ArrayList<String> tableColumnNonActive = new ArrayList<String>();
		for (int i = 0; i < tableLayout.getColumn().size(); i++) {
			if (!tableLayout.getColumn().get(i).isActive()) {
				tableColumnNonActive.add(tableLayout.getColumn().get(i).getKey());
			}
		}
		return tableColumnNonActive.toArray(new String[tableColumnNonActive.size()]);
	}

	public void changeTableColumnsWidth(Double[] widths, String gid) {
		if (this.gid != gid || cids == null) {
			getCidsToPosition();
			this.gid = gid;
		}
		for (int i = 0; i < widths.length; i++) {
			for (ColumnlayoutType column : lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid)) {
				if (column.getPos() != null && BigInteger.valueOf(i).equals(column.getPos())) {
					column.setWidth(widths[i]);
					break;
				}
			}
		}
	}

	public void changeTableColumnsOrder(String gid, int[] order) {
		if (this.gid != gid || cids == null) {
			this.gid = gid;
		}
		getCidsToPosition();
		List<ColumnlayoutType> newColumnLayouts = new ArrayList<ColumnlayoutType>();
		List<ColumnlayoutType> oldColumnLayouts = lguiItem.getLayoutAccess().getTableLayout(gid).getColumn();
		for (int i = 0; i < order.length; i++) {
			for (ColumnlayoutType column : oldColumnLayouts) {
				if (BigInteger.valueOf(order[i]).equals(column.getPos())) {
					ColumnlayoutType columnNew = column;
					columnNew.setPos(BigInteger.valueOf(i));
					newColumnLayouts.add(columnNew);
					lguiItem.getLayoutAccess().getTableLayout(gid).getColumn().remove(column);
					break;
				}
			}
		}
		for (ColumnlayoutType column : newColumnLayouts) {
			lguiItem.getLayoutAccess().getTableLayout(gid).getColumn().add(column);
		}
	}

	public void setTableColumnActive(String gid, String text, boolean activeTableColumn) {
		if (this.gid != gid || cids == null) {
			this.gid = gid;
		}
		List<ColumnlayoutType> columnLayouts = lguiItem.getLayoutAccess().getTableLayout(gid).getColumn();
		BigInteger cid = BigInteger.valueOf(-1);
		for (ColumnlayoutType column : columnLayouts) {
			if (column.getKey().equals(text)) {
				cid = column.getCid();
				column.setActive(activeTableColumn);
				column.setPos(BigInteger.valueOf(cids.length));
				break;
			}
		}
		if (activeTableColumn) {
			for (RowType row : getTable(gid).getRow()) {
				boolean cellExisting = false;
				for (CellType cell : row.getCell()) {
					if (cell.getCid() == cid) {
						cellExisting = true;
					}
				}
				if (!cellExisting) {
					CellType cell = new CellType();
					cell.setValue("");
					cell.setCid(cid);
					row.getCell().add(cell);
				}
			}
		}
	}

	private ColumnType[] getColumnsToCids() {
		ColumnType[] columns = new ColumnType[cids.length];
		for (int i = 0; i < cids.length; i++) {
			for (ColumnType column : getTable(gid).getColumn()) {
				if (column.getId().equals(cids[i])) {
					columns[i] = column;
					break;
				}
			}
		}
		return columns;
	}

	private void getCidsToPosition() {
		TablelayoutType layout = lguiItem.getLayoutAccess().getTableLayout(gid);
		int activeColumn = 0;
		for (ColumnlayoutType column : layout.getColumn()) {
			if (column.isActive()) {
				activeColumn++;
			}
		}
		cids = new BigInteger[activeColumn];
		for (int i = 0; i < activeColumn; i++) {
			for (ColumnlayoutType column : layout.getColumn()) {
				if (column.getPos() != null && column.getPos().equals(BigInteger.valueOf(i))) {
					cids[i] = column.getCid();
				}
			}
		}
	}

	private String getTypeTableColumn(int index) {
		for (ColumnType column : getTable(gid).getColumn()) {
			if (column.getId().equals(cids[index])) {
				return column.getSort().value();
			}
		}
		return null;
	}

	private RowType[] getTableData() {
		TableType table = getTable(gid);
		RowType[] tableData = new RowType[table.getRow().size()];
		for (int i = 0; i < tableData.length; i++) {
			tableData[i] = new RowType();
			tableData[i].setOid(table.getRow().get(i).getOid());
			for (int j = 0; j < cids.length; j++) {
				for (CellType cell : table.getRow().get(i).getCell()) {
					if (cell.getCid().equals(cids[j])) {
						tableData[i].getCell().add(cell);
						break;
					}
				}
			}
		}
		return tableData;
	}
}
