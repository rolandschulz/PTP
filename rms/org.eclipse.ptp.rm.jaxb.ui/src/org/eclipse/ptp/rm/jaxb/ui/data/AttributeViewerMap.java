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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnViewer;
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

	public AttributeViewer get(ColumnViewer viewer) {
		return map.get(viewer);
	}

	public void getValuesFromMap(LTVariableMap ltMap) {
		Map<String, String> vars = ltMap.getVariables();
		Map<String, String> disc = ltMap.getDiscovered();
		Map<String, String> selected = getSelected(ltMap);
		for (ColumnViewer viewer : map.keySet()) {
			AttributeViewerData data = (AttributeViewerData) viewer.getInput();
			if (data == null) {
				continue;
			}
			List<AttributeViewerCellData> rows = data.getRows();
			for (AttributeViewerCellData row : rows) {
				String name = row.getDisplayValue(COLUMN_NAME);
				Object value = vars.get(name);
				if (name == null) {
					continue;
				}
				if (value == null) {
					value = disc.get(name);
					if (value != null) {
						row.setDiscovered(true);
					}
				}
				row.setValue(value);
				if (selected.containsKey(name)) {
					row.setSelected(true);
				} else {
					row.setSelected(false);
				}
			}
			WidgetActionUtils.refreshViewer(viewer);

			Button toggle = showHide.get(viewer);
			String b = vars.get(SHOW_ALL);
			toggle.setSelection(Boolean.parseBoolean(b));
		}
	}

	public ColumnViewer[] getViewers() {
		return map.keySet().toArray(new ColumnViewer[0]);
	}

	/*
	 * Defaults are recorded in the Property or JobAttribute definitions and are
	 * accessed via the RMVariableMap.
	 * 
	 * The default value does not overwrite a non-null value.
	 */
	public void setDefaultValuesOnControl(RMVariableMap rmMap) {
		Map<String, Object> vars = rmMap.getVariables();
		Map<String, Object> disc = rmMap.getDiscovered();
		for (ColumnViewer viewer : map.keySet()) {
			AttributeViewerData data = (AttributeViewerData) viewer.getInput();
			if (data == null) {
				continue;
			}
			List<AttributeViewerCellData> rows = data.getRows();
			for (AttributeViewerCellData row : rows) {
				String name = row.getDisplayValue(COLUMN_NAME);
				if (name == null) {
					continue;
				}
				Object o = vars.get(name);
				String defaultValue = null;
				if (o == null) {
					o = disc.get(name);
				}
				if (o instanceof Property) {
					defaultValue = ((Property) o).getDefault();
				} else if (o instanceof Attribute) {
					defaultValue = ((Attribute) o).getDefault();
				}
				if (defaultValue != null) {
					row.setValue(defaultValue);
				}
			}
			WidgetActionUtils.refreshViewer(viewer);
		}
	}

	public void setValuesOnMap(LTVariableMap ltMap) {
		Map<String, String> vars = ltMap.getVariables();
		Map<String, String> disc = ltMap.getDiscovered();
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
			Iterator<AttributeViewerCellData> i = rows.iterator();
			AttributeViewerCellData row = null;
			if (i.hasNext()) {
				row = i.next();
				name = row.getDisplayValue(COLUMN_NAME);
				value = row.getDisplayValue(COLUMN_VALUE);
				if (value != null && row.isSelected()) {
					if (row.isDiscovered()) {
						disc.put(name, value);
					} else {
						vars.put(name, value);
					}
					selected.put(name, name);
					buffer.append(row.getReplaced(pattern));
				} else {
					if (row.isDiscovered()) {
						disc.remove(name);
					} else {
						vars.remove(name);
					}
				}
			}
			while (i.hasNext()) {
				row = i.next();
				name = row.getDisplayValue(COLUMN_NAME);
				value = row.getDisplayValue(COLUMN_VALUE);
				if (value != null && row.isSelected()) {
					if (row.isDiscovered()) {
						disc.put(name, value);
					} else {
						vars.put(name, value);
					}
					selected.put(name, name);
					buffer.append(separator).append(row.getReplaced(pattern));
				} else {
					if (row.isDiscovered()) {
						disc.remove(name);
					} else {
						vars.remove(name);
					}
				}
			}
			String tName = descriptor.getName();
			if (tName != null) {
				vars.put(tName, buffer.toString());
			}
			setSelected(selected, ltMap);

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

	public static Map<String, String> getSelected(LTVariableMap ltMap) {
		Map<String, String> map = new HashMap<String, String>();
		String selected = ltMap.getVariables().get(SELECTED_ATTRIBUTES);
		if (selected != null) {
			String[] attr = selected.split(SP);
			for (String s : attr) {
				map.put(s, s);
			}
		}
		return map;
	}

	public static void setSelected(Map<String, String> map, LTVariableMap ltMap) {
		StringBuffer buffer = new StringBuffer();
		for (String s : map.keySet()) {
			buffer.append(s).append(SP);
		}
		ltMap.getVariables().put(SELECTED_ATTRIBUTES, buffer.toString().trim());
	}
}
