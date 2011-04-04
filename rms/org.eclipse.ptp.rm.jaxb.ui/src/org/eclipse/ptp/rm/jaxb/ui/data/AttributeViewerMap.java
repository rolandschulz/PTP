/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Template;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.variables.LTVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.ILaunchTabValueHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.widgets.Button;

public class AttributeViewerMap implements ILaunchTabValueHandler, IJAXBUINonNLSConstants {

	private final Map<ColumnViewer, Button> showHide;

	private final Map<ColumnViewer, AttributeViewer> map;

	public AttributeViewerMap() {
		map = new HashMap<ColumnViewer, AttributeViewer>();
		showHide = new HashMap<ColumnViewer, Button>();
	}

	public void add(ColumnViewer viewer, AttributeViewer descriptor, Button toggle) {
		map.put(viewer, descriptor);
		showHide.put(viewer, toggle);
	}

	public void getValuesFromMap(LTVariableMap ltMap, boolean initializing) {
		if (!initializing) {
			return;
		}
		Map<String, String> vars = ltMap.getVariables();
		Map<String, String> selected = ltMap.getSelected();

		for (ColumnViewer viewer : map.keySet()) {
			AttributeViewerData data = (AttributeViewerData) viewer.getInput();
			if (data == null) {
				continue;
			}
			ICheckable checkT = (ICheckable) viewer;
			List<AttributeViewerCellData> rows = data.getRows();
			for (AttributeViewerCellData row : rows) {
				String name = row.getDisplayValue(COLUMN_NAME);
				String value = vars.get(name);
				if (name == null) {
					continue;
				}
				if (selected.containsKey(name)) {
					row.setSelected(true);
					checkT.setChecked(row, true);
					row.setValue(value); // set through editor FIXME
				} else {
					row.setSelected(false);
					checkT.setChecked(row, false);
					row.setValue(null); // set through editor FIXME
				}

				// need to set checked on the viewer
			}

			Button toggle = showHide.get(viewer);
			String b = vars.get(SHOW_ALL);
			toggle.setSelection(Boolean.parseBoolean(b));
		}
	}

	public void setValuesOnMap(LTVariableMap ltMap) {
		Map<String, String> vars = ltMap.getVariables();
		Map<String, String> selected = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer();
		for (ColumnViewer viewer : map.keySet()) {
			buffer.setLength(0);
			AttributeViewer descriptor = map.get(viewer);
			Template template = descriptor.getValue();
			String pattern = template.getPattern();
			String separator = template.getSeparator();
			AttributeViewerData data = (AttributeViewerData) viewer.getInput();
			if (data == null) {
				continue;
			}
			List<AttributeViewerCellData> rows = data.getRows();
			String name = null;
			String value = null;
			for (AttributeViewerCellData row : rows) {
				name = row.getDisplayValue(COLUMN_NAME);
				value = row.getDisplayValue(COLUMN_VALUE);
				if (row.isSelected()) {
					selected.put(name, name);
				}
				if (!ZEROSTR.equals(value)) {
					vars.put(name, value);
					value = row.getReplaced(pattern);
					if (!ZEROSTR.equals(value)) {
						buffer.append(separator).append(value);
					}
				} else {
					vars.remove(name);
				}
			}
			buffer.delete(0, separator.length());
			String tName = descriptor.getName();
			if (tName != null) {
				vars.put(tName, buffer.toString());
			}
			ltMap.setAllSelected(selected);

			Button toggle = showHide.get(viewer);
			vars.put(SHOW_ALL, String.valueOf(toggle.getSelection()));
		}
	}

	public void validateControlValues(RMVariableMap rmMap, IRemoteFileManager manager) throws Throwable {
		/*
		 * If there are validators, run them against the value
		 */
		for (ColumnViewer viewer : map.keySet()) {
			AttributeViewerData vdata = (AttributeViewerData) viewer.getInput();
			if (vdata == null) {
				continue;
			}
			List<AttributeViewerCellData> rows = vdata.getRows();
			for (AttributeViewerCellData row : rows) {
				Object data = row.getData();
				String value = row.getDisplayValue(COLUMN_VALUE);
				if (value == null) {
					String defaultV = null;
					if (data instanceof Property) {
						defaultV = ((Property) data).getDefault();
					} else if (data instanceof Attribute) {
						defaultV = ((Attribute) data).getDefault();
					}
					if (defaultV != null) {
						row.setValue(defaultV);
					}
				} else if (data instanceof Attribute) {
					Attribute ja = (Attribute) data;
					Validator v = ja.getValidator();
					if (v != null) {
						WidgetActionUtils.validate(value, v, manager);
					}
				}
			}
		}
	}
}
