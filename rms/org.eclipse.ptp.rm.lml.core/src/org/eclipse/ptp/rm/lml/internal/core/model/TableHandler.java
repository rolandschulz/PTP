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
import java.util.Map;

import org.eclipse.ptp.rm.lml.core.LMLManager;
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
import org.eclipse.ptp.rm.lml.internal.core.model.jobs.JobStatusData;

public class TableHandler extends LguiHandler {

	public class TableListener implements ILguiListener {

		public void handleEvent(ILguiUpdatedEvent e) {
			update(e.getLguiItem().getLguiType());
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

	public void changeTableColumnsOrder(String gid, int[] order) {
		if (this.gid != gid || cids == null) {
			this.gid = gid;
		}
		getCidsToPosition();
		final List<ColumnlayoutType> newColumnLayouts = new ArrayList<ColumnlayoutType>();
		final List<ColumnlayoutType> oldColumnLayouts = lguiItem.getLayoutAccess().getTableLayout(gid).getColumn();
		for (int i = 0; i < order.length; i++) {
			for (final ColumnlayoutType column : oldColumnLayouts) {
				if (BigInteger.valueOf(order[i]).equals(column.getPos())) {
					final ColumnlayoutType columnNew = column;
					columnNew.setPos(BigInteger.valueOf(i));
					newColumnLayouts.add(columnNew);
					lguiItem.getLayoutAccess().getTableLayout(gid).getColumn().remove(column);
					break;
				}
			}
		}
		for (final ColumnlayoutType column : newColumnLayouts) {
			lguiItem.getLayoutAccess().getTableLayout(gid).getColumn().add(column);
		}
	}

	public void changeTableColumnsWidth(Double[] widths, String gid) {
		if (this.gid != gid || cids == null) {
			getCidsToPosition();
			this.gid = gid;
		}
		for (int i = 0; i < widths.length; i++) {
			for (final ColumnlayoutType column : lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid)) {
				if (column.getPos() != null && BigInteger.valueOf(i).equals(column.getPos())) {
					column.setWidth(widths[i]);
					break;
				}
			}
		}
	}

	private void getCidsToPosition() {
		final TablelayoutType layout = lguiItem.getLayoutAccess().getTableLayout(gid);
		int activeColumn = 0;
		for (final ColumnlayoutType column : layout.getColumn()) {
			if (column.isActive()) {
				activeColumn++;
			}
		}
		cids = new BigInteger[activeColumn];
		for (int i = 0; i < activeColumn; i++) {
			for (final ColumnlayoutType column : layout.getColumn()) {
				if (column.getPos() != null && column.getPos().equals(BigInteger.valueOf(i))) {
					cids[i] = column.getCid();
				}
			}
		}
	}

	private ColumnType[] getColumnsToCids() {
		final ColumnType[] columns = new ColumnType[cids.length];
		for (int i = 0; i < cids.length; i++) {
			for (final ColumnType column : getTable(gid).getColumn()) {
				if (column.getId().equals(cids[i])) {
					columns[i] = column;
					break;
				}
			}
		}
		return columns;
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
		final TablelayoutType layout = lguiItem.getLayoutAccess().getTableLayout(gid);
		int activeColumn = 0;
		for (final ColumnlayoutType column : layout.getColumn()) {
			if (column.isActive()) {
				activeColumn++;
			}
		}
		return activeColumn;
	}

	public int getNumberOfTables() {
		return getTables().size();
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
		for (final TableType tag : getTables()) {
			if (tag.getId().equals(gid)) {
				return tag;
			}
		}
		return null;
	}

	public String[] getTableColumnActive(String gid) {
		if (this.gid != gid || cids == null) {
			cids = null;
			this.gid = gid;
		}
		final TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		final ArrayList<String> tableColumnNonActive = new ArrayList<String>();
		for (int i = 0; i < tableLayout.getColumn().size(); i++) {
			if (tableLayout.getColumn().get(i).isActive()) {
				tableColumnNonActive.add(tableLayout.getColumn().get(i).getKey());
			}
		}
		return tableColumnNonActive.toArray(new String[tableColumnNonActive.size()]);
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
		final TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		if (tableLayout.getColumn().size() <= 0) {
			return null;
			// tableLayout = lguiItem.getLayoutAccess().getDefaultTableLayout(gid);
		}

		getCidsToPosition();
		final ColumnType[] columns = getColumnsToCids();
		final ColumnlayoutType[] layoutColumns = lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid);
		final ITableColumnLayout[] tableColumnLayouts = new ITableColumnLayout[cids.length];

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

	public String[] getTableColumnNonActive(String gid) {
		if (this.gid != gid || cids == null) {
			cids = null;
			this.gid = gid;
		}
		final TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		final ArrayList<String> tableColumnNonActive = new ArrayList<String>();
		for (int i = 0; i < tableLayout.getColumn().size(); i++) {
			if (!tableLayout.getColumn().get(i).isActive()) {
				tableColumnNonActive.add(tableLayout.getColumn().get(i).getKey());
			}
		}
		return tableColumnNonActive.toArray(new String[tableColumnNonActive.size()]);
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
		final ColumnType[] columns = getColumnsToCids();
		final String[] tableColumnNames = new String[cids.length];

		for (int i = 0; i < cids.length; i++) {
			tableColumnNames[i] = columns[i].getName();
		}
		return tableColumnNames;
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
		final ColumnType[] columns = getColumnsToCids();
		final String[] tableColumnStyles = new String[cids.length];

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
		final ColumnlayoutType[] layoutColumns = lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid);
		final int[] tableColumnWidths = new int[cids.length];
		double sumWidthColumns = 0;
		for (int i = 0; i < cids.length; i++) {
			sumWidthColumns += layoutColumns[i].getWidth();
		}
		for (int i = 0; i < cids.length; i++) {
			tableColumnWidths[i] = (int) (layoutColumns[i].getWidth() * widthTable / sumWidthColumns);
		}
		return tableColumnWidths;
	}

	private RowType[] getTableData() {
		final TableType table = getTable(gid);
		final RowType[] tableData = new RowType[table.getRow().size()];
		for (int i = 0; i < tableData.length; i++) {
			final RowType row = table.getRow().get(i);
			tableData[i] = new RowType();
			if (row.getOid() != null) {
				tableData[i].setOid(row.getOid());
			}
			for (final BigInteger cid : cids) {
				for (final CellType cell : row.getCell()) {
					if (cell.getCid().equals(cid)) {
						tableData[i].getCell().add(cell);
						break;
					}
				}
			}
		}
		return tableData;
	}

	public Row[] getTableDataWithColor(String gid) {
		if (this.gid != gid) {
			this.gid = gid;
		}
		final Map<String, String> map = lguiItem.revert(lguiItem.getUserJobMap(gid));
		getCidsToPosition();
		final TableType table = getTable(gid);
		final Row[] tableData = new Row[table.getRow().size()];
		for (int i = 0; i < tableData.length; i++) {
			final RowType row = table.getRow().get(i);
			tableData[i] = new Row();
			if (row.getOid() != null) {
				tableData[i].setOid(row.getOid());
				tableData[i].setColor(lguiItem.getOIDToObject().getColorById(row.getOid()));
				if (map.containsKey(row.getOid())) {
					final JobStatusData status = LMLManager.getInstance().getJobStatusData(lguiItem.toString(),
							map.get(row.getOid()));
					tableData[i].setJobStatusData(status);
				}

			}
			final Cell[] tableDataRow = new Cell[cids.length];
			for (int j = 0; j < cids.length; j++) {
				for (final CellType cell : row.getCell()) {
					if (cell.getCid().equals(cids[j])) {
						tableDataRow[j] = new Cell(cell.getValue(), tableData[i]);
						break;
					}
				}
				if (tableDataRow[j] == null) {
					tableDataRow[j] = new Cell("?", tableData[i]);
				}
			}
			tableData[i].setCells(tableDataRow);
		}
		return tableData;
	}

	/**
	 * Getting a list of all elements of type TableType from LguiType.
	 * 
	 * @return list of elements(TableType)
	 */
	List<TableType> getTables() {
		final List<TableType> tables = new LinkedList<TableType>();
		for (final GobjectType tag : lguiItem.getOverviewAccess().getGraphicalObjects()) {
			if (tag instanceof TableType) {
				tables.add((TableType) tag);
			}
		}
		return tables;
	}

	public String getTableTitle(String gid) {
		if (this.gid != gid) {
			cids = null;
			this.gid = gid;
		}
		return getTable(gid).getTitle();
	}

	private String getTypeTableColumn(int index) {
		for (final ColumnType column : getTable(gid).getColumn()) {
			if (column.getId().equals(cids[index])) {
				return column.getSort().value();
			}
		}
		return null;
	}

	public boolean isEmpty(String gid) {
		return (getTable(gid) == null);
	}

	private void reduceColumnPos(int pos, int i) {
		if (pos == i) {
			return;
		}
		final List<ColumnlayoutType> columnLayouts = lguiItem.getLayoutAccess().getTableLayout(gid).getColumn();
		for (final ColumnlayoutType column : columnLayouts) {
			if (column.getPos() != null && column.getPos().intValue() == pos) {
				column.setPos(BigInteger.valueOf(pos - 1));
				reduceColumnPos(pos + 1, i);
				break;
			}
		}

	}

	public void setTableColumnActive(String gid, String text, boolean activeTableColumn) {
		// gid correction
		if (this.gid != gid || cids == null) {
			this.gid = gid;
		}
		getCidsToPosition();
		final List<ColumnlayoutType> columnLayouts = lguiItem.getLayoutAccess().getTableLayout(gid).getColumn();
		BigInteger cid = BigInteger.valueOf(-1);
		for (final ColumnlayoutType column : columnLayouts) {
			if (column.getKey().equals(text)) {
				cid = column.getCid();
				column.setActive(activeTableColumn);
				if (activeTableColumn) {
					column.setPos(BigInteger.valueOf(cids.length));
				} else {
					reduceColumnPos(column.getPos().intValue() + 1, cids.length);
					column.setPos(null);
				}
				break;
			}
		}
		if (activeTableColumn) {
			for (final RowType row : getTable(gid).getRow()) {
				boolean cellExisting = false;
				for (final CellType cell : row.getCell()) {
					if (cell.getCid() == cid) {
						cellExisting = true;
					}
				}
				if (!cellExisting) {
					final CellType cell = new CellType();
					cell.setValue("");
					cell.setCid(cid);
					row.getCell().add(cell);
				}
			}
		}
	}

	public void sort(String gid, int sortDirectionComparator, int sortIndex, int sortDirection) {
		if (this.gid != gid || cids == null) {
			getCidsToPosition();
			this.gid = gid;
		}
		final RowType[] jobTableData = getTableData();
		Arrays.sort(jobTableData, new TableSorter(getTypeTableColumn(sortIndex), sortDirectionComparator, sortIndex, sortDirection));
		getTable(gid).getRow().clear();
		for (final RowType element : jobTableData) {
			getTable(gid).getRow().add(element);
		}
	}

}
