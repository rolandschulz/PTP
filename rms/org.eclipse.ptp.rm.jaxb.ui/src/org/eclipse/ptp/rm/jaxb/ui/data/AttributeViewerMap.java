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

public class AttributeViewerMap implements ILaunchTabValueHandler, IJAXBUINonNLSConstants {

	private final Map<ColumnViewer, AttributeViewer> map;

	public AttributeViewerMap() {
		map = new HashMap<ColumnViewer, AttributeViewer>();
	}

	public void add(ColumnViewer viewer, AttributeViewer descriptor) {
		map.put(viewer, descriptor);
	}

	public AttributeViewer get(ColumnViewer viewer) {
		return map.get(viewer);
	}

	public void getValuesFromMap(LTVariableMap ltMap) {
		Map<String, ?> vars = ltMap.getVariables();
		Map<String, ?> disc = ltMap.getDiscovered();
		for (ColumnViewer viewer : map.keySet()) {
			AttributeViewerData data = (AttributeViewerData) viewer.getInput();
			if (data == null) {
				continue;
			}
			List<AttributeViewerRowData> rows = data.getAllRows();
			for (AttributeViewerRowData row : rows) {
				String name = row.getColumnDisplayValue(COLUMN_NAME);
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
			}
			WidgetActionUtils.refreshViewer(viewer);
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
			List<AttributeViewerRowData> rows = data.getAllRows();
			for (AttributeViewerRowData row : rows) {
				String name = row.getColumnDisplayValue(COLUMN_NAME);
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
					row.setValueFromString(defaultValue);
				}
			}
			WidgetActionUtils.refreshViewer(viewer);
		}
	}

	public void setValuesOnMap(LTVariableMap ltMap) {
		Map<String, String> vars = ltMap.getVariables();
		Map<String, String> disc = ltMap.getDiscovered();
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
			List<AttributeViewerRowData> rows = data.getAllRows();
			String name = null;
			Object value = null;
			Iterator<AttributeViewerRowData> i = rows.iterator();
			AttributeViewerRowData row = null;
			if (i.hasNext()) {
				row = i.next();
				name = row.getColumnDisplayValue(COLUMN_NAME);
				value = row.getValue();
				if (value != null && row.isSelected()) {
					if (row.isDiscovered()) {
						disc.put(name, String.valueOf(value));
					} else {
						vars.put(name, String.valueOf(value));
					}
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
				name = row.getColumnDisplayValue(COLUMN_NAME);
				value = row.getValue();
				if (value != null && row.isSelected()) {
					if (row.isDiscovered()) {
						disc.put(name, String.valueOf(value));
					} else {
						vars.put(name, String.valueOf(value));
					}
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
			List<AttributeViewerRowData> rows = vdata.getAllRows();
			for (AttributeViewerRowData row : rows) {
				Object data = row.getData();
				String value = String.valueOf(row.getValue());
				if (value == null) {
					String defaultV = null;
					if (data instanceof Property) {
						defaultV = ((Property) data).getDefault();
					} else if (data instanceof Attribute) {
						defaultV = ((Attribute) data).getDefault();
					}
					if (defaultV != null) {
						row.setValueFromString(defaultV);
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
