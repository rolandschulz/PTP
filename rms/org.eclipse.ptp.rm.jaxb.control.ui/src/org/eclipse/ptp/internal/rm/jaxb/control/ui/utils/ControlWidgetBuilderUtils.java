/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.utils;

import java.util.List;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.cell.AttributeViewerEditingSupport;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.providers.TableDataContentProvider;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.providers.TreeDataContentProvider;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.providers.ViewerDataCellLabelProvider;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.sorters.AttributeViewerSorter;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnDataType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Convenience methods for constructing and configuring control-specific
 * widgets.
 * 
 * @author arossi
 * 
 */
public class ControlWidgetBuilderUtils {

	/**
	 * Configures the CheckboxTableViewer. Calls
	 * {@link #setupCommon(ColumnViewer, List, ISelectionChangedListener, boolean)}
	 * and
	 * {@link #setupSpecific(CheckboxTableViewer, List, Boolean, boolean, boolean)}
	 * .
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param listener
	 * @param sortByName
	 * @param tooltip
	 * @param headerVisible
	 * @param linesVisible
	 */
	public static void setupAttributeTable(final CheckboxTableViewer viewer, List<ColumnDataType> columnData,
			ISelectionChangedListener listener, boolean sortByName, boolean tooltip, boolean headerVisible, boolean linesVisible) {
		setupSpecific(viewer, columnData, sortByName, headerVisible, linesVisible);
		setupCommon(viewer, columnData, listener, tooltip);
	}

	/**
	 * Configures the CheckboxTreeViewer. Calls
	 * {@link #setupCommon(ColumnViewer, List, ISelectionChangedListener, boolean)}
	 * and
	 * {@link #setupSpecific(CheckboxTreeViewer, List, Boolean, boolean, boolean)}
	 * .
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param listener
	 * @param sortByName
	 * @param tooltip
	 * @param headerVisible
	 * @param linesVisible
	 */
	public static void setupAttributeTree(final CheckboxTreeViewer viewer, List<ColumnDataType> columnData,
			ISelectionChangedListener listener, boolean sortByName, boolean tooltip, boolean headerVisible, boolean linesVisible) {
		setupSpecific(viewer, columnData, sortByName, headerVisible, linesVisible);
		setupCommon(viewer, columnData, listener, tooltip);
	}

	/**
	 * Creates sorter adapter for viewer column.
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.sorters.AttributeViewerSorter
	 * 
	 * @param viewer
	 * @return adapter for the name column
	 */
	private static SelectionAdapter getAttributeViewerSelectionAdapter(final ColumnViewer viewer) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AttributeViewerSorter sorter = (AttributeViewerSorter) viewer.getSorter();
				if (sorter != null) {
					sorter.toggle();
					viewer.refresh();
				}
			}
		};
	}

	/**
	 * Configure parts of viewer common to Table and Tree types.
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param listener
	 * @param tooltip
	 */
	private static void setupCommon(final ColumnViewer viewer, List<ColumnDataType> columnData, ISelectionChangedListener listener,
			boolean tooltip) {
		String[] columnProperties = new String[columnData.size()];
		for (int i = 0; i < columnData.size(); i++) {
			ColumnDataType columnDescriptor = columnData.get(i);
			columnProperties[i] = columnDescriptor.getName();
		}
		viewer.setColumnProperties(columnProperties);
		if (tooltip) {
			ColumnViewerToolTipSupport.enableFor(viewer);
		}
		if (listener != null) {
			viewer.addSelectionChangedListener(listener);
		}
		viewer.setLabelProvider(new ViewerDataCellLabelProvider(columnData));
	}

	/**
	 * Configure parts of viewer specific to Table type.
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param sortOnName
	 * @param headerVisible
	 * @param linesVisible
	 */
	private static void setupSpecific(final CheckboxTableViewer viewer, List<ColumnDataType> columnData, Boolean sortOnName,
			boolean headerVisible, boolean linesVisible) {
		for (int i = 0; i < columnData.size(); i++) {
			ColumnDataType columnDescriptor = columnData.get(i);
			TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = viewerColumn.getColumn();
			String name = columnDescriptor.getName();
			column.setText(name);
			column.setMoveable(columnDescriptor.isMoveable());
			column.setResizable(columnDescriptor.isResizable());
			String tt = columnDescriptor.getTooltip();
			if (tt != null) {
				column.setToolTipText(tt);
			}
			if (JAXBControlUIConstants.UNDEFINED != columnDescriptor.getWidth()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (null != columnDescriptor.getAlignment()) {
				column.setAlignment(WidgetBuilderUtils.getStyle(columnDescriptor.getAlignment()));
			}
			if (JAXBControlUIConstants.COLUMN_NAME.equals(name)) {
				if (sortOnName != null) {
					if (sortOnName) {
						viewer.setSorter(new AttributeViewerSorter());
						column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));
					}
				}
			}
			if (JAXBControlUIConstants.COLUMN_VALUE.equals(columnDescriptor.getName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer));
			}
		}
		viewer.setContentProvider(new TableDataContentProvider());
		viewer.getTable().setHeaderVisible(headerVisible);
		viewer.getTable().setLinesVisible(linesVisible);
	}

	/**
	 * Configure parts of viewer specific to Tree type.
	 * 
	 * @param viewer
	 * @param columnData
	 *            JAXB data element describing viewer columns
	 * @param sortOnName
	 * @param headerVisible
	 * @param linesVisible
	 */
	private static void setupSpecific(final CheckboxTreeViewer viewer, List<ColumnDataType> columnData, Boolean sortOnName,
			boolean headerVisible, boolean linesVisible) {
		for (int i = 0; i < columnData.size(); i++) {
			ColumnDataType columnDescriptor = columnData.get(i);
			TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
			TreeColumn column = viewerColumn.getColumn();
			String name = columnDescriptor.getName();
			column.setText(name);
			column.setMoveable(columnDescriptor.isMoveable());
			column.setResizable(columnDescriptor.isResizable());
			String tt = columnDescriptor.getTooltip();
			if (tt != null) {
				column.setToolTipText(tt);
			}
			if (JAXBControlUIConstants.UNDEFINED != columnDescriptor.getWidth()) {
				column.setWidth(columnDescriptor.getWidth());
			}
			if (null != columnDescriptor.getAlignment()) {
				column.setAlignment(WidgetBuilderUtils.getStyle(columnDescriptor.getAlignment()));
			}
			if (JAXBControlUIConstants.COLUMN_NAME.equals(name)) {
				if (sortOnName != null) {
					if (sortOnName) {
						viewer.setSorter(new AttributeViewerSorter());
						column.addSelectionListener(getAttributeViewerSelectionAdapter(viewer));
					}
				}
			}
			if (JAXBControlUIConstants.COLUMN_VALUE.equals(columnDescriptor.getName())) {
				viewerColumn.setEditingSupport(new AttributeViewerEditingSupport(viewer));
			}
		}
		viewer.setContentProvider(new TreeDataContentProvider());
		viewer.getTree().setHeaderVisible(headerVisible);
		viewer.getTree().setLinesVisible(linesVisible);
	}
}
