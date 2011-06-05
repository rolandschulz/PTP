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

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
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
import org.eclipse.ptp.rm.lml.internal.core.elements.PatternMatchType;
import org.eclipse.ptp.rm.lml.internal.core.elements.PatternType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RowType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SortingType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TablelayoutType;

public class TableHandler extends LguiHandler {

	public class TableListener implements ILguiListener {

		public void handleEvent(ILguiUpdatedEvent e) {
			update(e.getLguiItem().getLguiType());
		}

	}

	private final TableListener tableListener = new TableListener();

	public TableHandler(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);
		lguiItem.addListener(tableListener);
	}

	/**
	 * Change the table column order
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @param order
	 *            new order of columns
	 */
	public void changeTableColumnsOrder(String gid, int[] order) {
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

	/**
	 * Change the table column widths
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @param widths
	 *            new column widths
	 */
	public void changeTableColumnsWidth(String gid, Double[] widths) {
		final BigInteger[] cids = getActiveCids(gid);
		for (int i = 0; i < widths.length; i++) {
			for (final ColumnlayoutType column : lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid)) {
				if (column.getPos() != null && BigInteger.valueOf(i).equals(column.getPos())) {
					column.setWidth(widths[i]);
					break;
				}
			}
		}
	}

	public TableType generateDefaultTable(String gid) {
		final TableType table = new TableType();
		table.setId(gid);
		table.setTitle("title_" + gid);
		table.setContenttype("jobs");

		for (final ColumnlayoutType columnLayout : lguiItem.getLayoutAccess().getTableLayout(gid).getColumn()) {
			if (columnLayout.isActive()) {
				final ColumnType column = new ColumnType();
				column.setId(columnLayout.getCid());
				column.setName(columnLayout.getKey());
				generateDefaultSorting(column);
				if (columnLayout.getKey().equals("owner")) {
					generateDefaultPattern(".*", column);
				}
				if (columnLayout.getKey().equals("status")) {
					column.setType("mandatory");
					if (gid.equals("joblistrun")) {
						generateDefaultPattern("RUNNING", column);
					} else {
						generateDefaultPattern("SUBMITTED", column);
					}
				}
				table.getColumn().add(column);
			}
		}
		lgui.getObjectsAndRelationsAndInformation().add(new JAXBElement<TableType>(new QName("table"), TableType.class, table));
		return table;
	}

	public String getCellValue(TableType table, RowType row, String colName) {
		final BigInteger index = getColumnIndex(table, colName);
		if (index != null) {
			for (final CellType cell : row.getCell()) {
				if (cell.getCid().equals(index)) {
					return cell.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Get the number of table columns for this gid
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @return
	 */
	public int getNumberOfTableColumns(String gid) {
		final TablelayoutType layout = lguiItem.getLayoutAccess().getTableLayout(gid);
		int activeColumn = 0;
		for (final ColumnlayoutType column : layout.getColumn()) {
			if (column.isActive()) {
				activeColumn++;
			}
		}
		return activeColumn;
	}

	/**
	 * Currently unused
	 */
	public int getNumberOfTables() {
		return getTables().size();
	}

	/**
	 * Getting an element(Table Type) which has an equal title to the argument
	 * tableType.
	 * 
	 * @param gid
	 *            ID of the desired table
	 * @return Corresponding table to the desired table title
	 */
	public TableType getTable(String gid) {
		for (final TableType tag : getTables()) {
			if (tag.getId().equals(gid)) {
				return tag;
			}
		}
		LMLCorePlugin.log("No table found for gid \"" + gid + "\"!"); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}

	/**
	 * Currently unused
	 */
	public String[] getTableColumnActive(String gid) {
		final TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		final ArrayList<String> tableColumnNonActive = new ArrayList<String>();
		for (int i = 0; i < tableLayout.getColumn().size(); i++) {
			if (tableLayout.getColumn().get(i).isActive()) {
				tableColumnNonActive.add(tableLayout.getColumn().get(i).getKey());
			}
		}
		return tableColumnNonActive.toArray(new String[tableColumnNonActive.size()]);
	}

	/**
	 * Get the column layout for the table
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @return column layout
	 */
	public ITableColumnLayout[] getTableColumnLayout(String gid) {
		final TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		if (tableLayout.getColumn().size() <= 0) {
			return null;
			// tableLayout =
			// lguiItem.getLayoutAccess().getDefaultTableLayout(gid);
		}

		final BigInteger[] cids = getActiveCids(gid);
		final ColumnType[] columns = getActiveColumns(gid);
		final ColumnlayoutType[] layoutColumns = lguiItem.getLayoutAccess().getLayoutColumsToCids(cids, gid);
		final ITableColumnLayout[] tableColumnLayouts = new ITableColumnLayout[cids.length];

		for (int i = 0; i < cids.length; i++) {
			if (columns[i] == null) {
				tableColumnLayouts[i] = new TableColumnLayout(layoutColumns[i].getKey(), (int) (layoutColumns[i].getWidth() * 100),
						"LEFT"); //$NON-NLS-1$
			} else {
				String style;
				switch (columns[i].getSort().value().charAt(0)) {
				case 'a':
				case 'd':
					style = "LEFT"; //$NON-NLS-1$
					break;
				case 'n':
					style = "RIGHT"; //$NON-NLS-1$
					break;
				default:
					style = "LEFT"; //$NON-NLS-1$
				}
				tableColumnLayouts[i] = new TableColumnLayout(columns[i].getName(), (int) (layoutColumns[i].getWidth() * 100),
						style);
			}
		}
		return tableColumnLayouts;
	}

	/**
	 * Not currently used
	 */
	public String[] getTableColumnNonActive(String gid) {
		final TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		final ArrayList<String> tableColumnNonActive = new ArrayList<String>();
		for (int i = 0; i < tableLayout.getColumn().size(); i++) {
			if (!tableLayout.getColumn().get(i).isActive()) {
				tableColumnNonActive.add(tableLayout.getColumn().get(i).getKey());
			}
		}
		return tableColumnNonActive.toArray(new String[tableColumnNonActive.size()]);
	}

	/**
	 * Not currently used
	 */
	public String[] getTableColumnsName(String gid) {
		final ColumnType[] columns = getActiveColumns(gid);
		final String[] tableColumnNames = new String[columns.length];

		for (int i = 0; i < columns.length; i++) {
			tableColumnNames[i] = columns[i].getName();
		}
		return tableColumnNames;
	}

	/**
	 * Not currently used
	 */
	public String[] getTableColumnsStyle(String gid) {
		final ColumnType[] columns = getActiveColumns(gid);
		final String[] tableColumnStyles = new String[columns.length];

		for (int i = 0; i < columns.length; i++) {
			switch (columns[i].getSort().value().charAt(0)) {
			case 'a':
			case 'd':
				tableColumnStyles[i] = "LEFT"; //$NON-NLS-1$
				break;
			case 'n':
				tableColumnStyles[i] = "RIGHT"; //$NON-NLS-1$
				break;
			default:
				tableColumnStyles[i] = "LEFT"; //$NON-NLS-1$
			}
		}
		return tableColumnStyles;
	}

	/**
	 * Not currently used
	 */
	public int[] getTableColumnsWidth(String gid, int widthTable) {
		TablelayoutType tableLayout = lguiItem.getLayoutAccess().getTableLayout(gid);
		if (tableLayout.getColumn().size() <= 0) {
			tableLayout = lguiItem.getLayoutAccess().getDefaultTableLayout(gid);
		}
		final BigInteger[] cids = getActiveCids(gid);
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

	/**
	 * Get the table with color information added
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @return rows with color information added
	 */
	public Row[] getTableDataWithColor(String gid, boolean addColor) {
		final BigInteger[] cids = getActiveCids(gid);
		final TableType table = getTable(gid);
		if (table == null) {
			return new Row[0];
		}
		final Row[] tableData = new Row[table.getRow().size()];
		for (int i = 0; i < tableData.length; i++) {
			final RowType row = table.getRow().get(i);
			tableData[i] = new Row();
			if (row.getOid() != null) {
				tableData[i].setOid(row.getOid());
				if (addColor) {
					tableData[i].setColor(lguiItem.getOIDToObject().getColorById(row.getOid()));
				}
			}
			final BigInteger jobIdIndex = getColumnIndex(table, ILguiItem.JOB_ID);
			final Cell[] tableDataRow = new Cell[cids.length];
			for (int j = 0; j < cids.length; j++) {
				for (final CellType cell : row.getCell()) {
					if (cell.getCid() != null && cell.getCid().equals(cids[j])) {
						tableDataRow[j] = new Cell(cell.getValue(), tableData[i]);
						break;
					}
				}
				if (tableDataRow[j] == null) {
					tableDataRow[j] = new Cell("?", tableData[i]); //$NON-NLS-1$
				}
				if (cids[j].equals(jobIdIndex)) {
					final JobStatusData status = LMLManager.getInstance().getUserJob(lguiItem.toString(), tableDataRow[j].value);
					tableData[i].setJobStatusData(status);
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
	public List<TableType> getTables() {
		final List<TableType> tables = new LinkedList<TableType>();
		for (final GobjectType tag : lguiItem.getOverviewAccess().getGraphicalObjects()) {
			if (tag instanceof TableType) {
				tables.add((TableType) tag);
			}
		}
		return tables;
	}

	/**
	 * Not currently used
	 */
	public String getTableTitle(String gid) {
		final TableType table = getTable(gid);
		if (table == null) {
			return ""; //$NON-NLS-1$
		}
		return table.getTitle();
	}

	/**
	 * Not currently used
	 */
	public boolean isEmpty(String gid) {
		return (getTable(gid) == null);
	}

	public String setCellValue(TableType table, RowType row, String colName, String value) {
		final BigInteger index = getColumnIndex(table, colName);
		if (index != null) {
			for (final CellType cell : row.getCell()) {
				if (cell.getCid().equals(index)) {
					final String oldVal = cell.getValue();
					cell.setValue(value);
					return oldVal;
				}
			}
		}
		return null;
	}

	/**
	 * Not currently used
	 */
	public void setTableColumnActive(String gid, String text, boolean activeTableColumn) {
		// gid correction
		final BigInteger[] cids = getActiveCids(gid);
		final List<ColumnlayoutType> columnLayouts = lguiItem.getLayoutAccess().getTableLayout(gid).getColumn();
		BigInteger cid = BigInteger.valueOf(-1);
		for (final ColumnlayoutType column : columnLayouts) {
			if (column.getKey().equals(text)) {
				cid = column.getCid();
				column.setActive(activeTableColumn);
				if (activeTableColumn) {
					column.setPos(BigInteger.valueOf(cids.length));
				} else {
					reduceColumnPos(gid, column.getPos().intValue() + 1, cids.length);
					column.setPos(null);
				}
				break;
			}
		}
		final TableType table = getTable(gid);
		if (activeTableColumn && table != null) {
			for (final RowType row : table.getRow()) {
				boolean cellExisting = false;
				for (final CellType cell : row.getCell()) {
					if (cell.getCid() == cid) {
						cellExisting = true;
					}
				}
				if (!cellExisting) {
					final CellType cell = new CellType();
					cell.setValue(""); //$NON-NLS-1$
					cell.setCid(cid);
					row.getCell().add(cell);
				}
			}
		}
	}

	/**
	 * Sort the table
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @param sortDirectionComparator
	 * @param sortIndex
	 * @param sortDirection
	 */
	public void sort(String gid, int sortDirectionComparator, int sortIndex, int sortDirection) {
		final TableType table = getTable(gid);
		if (table != null) {
			final BigInteger[] cids = getActiveCids(gid);
			if (cids.length > sortIndex) {
				final RowType[] jobTableData = getTableData(gid);
				Arrays.sort(jobTableData, new TableSorter(getColumnSortProperty(table, cids, sortIndex), sortDirectionComparator,
						sortIndex, sortDirection));
				table.getRow().clear();
				for (final RowType element : jobTableData) {
					table.getRow().add(element);
				}
			}
		}
	}

	private void generateDefaultPattern(String regexp, ColumnType column) {
		final PatternType pattern = new PatternType();
		final PatternMatchType patternMatch = new PatternMatchType();
		patternMatch.setRegexp(regexp);
		pattern.getIncludeAndExclude().add(
				new JAXBElement<PatternMatchType>(new QName("include"), PatternMatchType.class, patternMatch));
		column.setPattern(pattern);
	}

	private void generateDefaultSorting(ColumnType column) {
		if (column.getName().equals("step") || column.getName().equals("owner") || column.getName().equals("queue")
				|| column.getName().equals("status")) {
			column.setSort(SortingType.ALPHA);
		} else if (column.getName().equals("wall") || column.getName().equals("totalcores")) {
			column.setSort(SortingType.NUMERIC);
		} else {
			column.setSort(SortingType.DATE);
		}

	}

	/**
	 * Get column indexes of active columns for the table layout
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @return array of column indexes
	 */
	private BigInteger[] getActiveCids(String gid) {
		final TablelayoutType layout = lguiItem.getLayoutAccess().getTableLayout(gid);
		int activeColumn = 0;
		for (final ColumnlayoutType column : layout.getColumn()) {
			if (column.isActive()) {
				activeColumn++;
			}
		}
		final BigInteger[] cids = new BigInteger[activeColumn];
		for (int i = 0; i < activeColumn; i++) {
			for (final ColumnlayoutType column : layout.getColumn()) {
				if (column.getPos() != null && column.getPos().equals(BigInteger.valueOf(i))) {
					cids[i] = column.getCid();
				}
			}
		}
		return cids;
	}

	/**
	 * Get active columns for the table layout
	 * 
	 * @param gid
	 *            ID of the table layout
	 * @return active columns
	 */
	private ColumnType[] getActiveColumns(String gid) {
		final TableType table = getTable(gid);
		if (table == null) {
			return new ColumnType[0];
		}
		final BigInteger[] cids = getActiveCids(gid);
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

	private BigInteger getColumnIndex(TableType table, String colName) {
		if (table != null) {
			for (final ColumnType column : table.getColumn()) {
				if (colName.equals(column.getName())) {
					return column.getId();
				}
			}
		}
		return null;
	}

	/**
	 * Get the column sort property for the given column index
	 * 
	 * @param table
	 *            table containing columns
	 * @param cids
	 *            column indexes
	 * @param index
	 *            index of column
	 * @return sort property
	 */
	private String getColumnSortProperty(TableType table, BigInteger[] cids, int index) {
		for (final ColumnType column : table.getColumn()) {
			if (column != null) {
				if (column.getId().equals(cids[index])) {
					return column.getSort().value();
				}
			}
		}
		return null;
	}

	/**
	 * @param gid
	 *            ID of the table layout
	 * @return
	 */
	private RowType[] getTableData(String gid) {
		final TableType table = getTable(gid);
		if (table == null) {
			return new RowType[0];
		}
		final RowType[] tableData = new RowType[table.getRow().size()];
		final BigInteger[] cids = getActiveCids(gid);
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

	/**
	 * Not currently used
	 */
	private void reduceColumnPos(String gid, int pos, int i) {
		if (pos == i) {
			return;
		}
		final List<ColumnlayoutType> columnLayouts = lguiItem.getLayoutAccess().getTableLayout(gid).getColumn();
		for (final ColumnlayoutType column : columnLayouts) {
			if (column.getPos() != null && column.getPos().intValue() == pos) {
				column.setPos(BigInteger.valueOf(pos - 1));
				reduceColumnPos(gid, pos + 1, i);
				break;
			}
		}
	}

}
