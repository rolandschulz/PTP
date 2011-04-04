/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArgImpl;
import org.eclipse.ptp.rm.jaxb.core.variables.LTVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.ILaunchTabValueHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.widgets.Control;

public class WidgetMap implements ILaunchTabValueHandler, IJAXBUINonNLSConstants {
	private final Map<Control, Widget> map;

	public WidgetMap() {
		map = new HashMap<Control, Widget>();
	}

	public void add(Control control, Widget widget) {
		map.put(control, widget);
	}

	public Widget get(Control control) {
		return map.get(control);
	}

	public Control[] getControls() {
		return map.keySet().toArray(new Control[0]);
	}

	public void getValuesFromMap(LTVariableMap ltMap, boolean initializing) {
		Map<String, String> vars = ltMap.getVariables();
		for (Control c : map.keySet()) {
			String value = null;
			Widget w = map.get(c);
			Widget.DynamicText va = w.getDynamicText();
			if (va != null) {
				List<Arg> arglist = va.getArg();
				if (arglist != null) {
					value = ArgImpl.toString(null, arglist, ltMap);
				}
			}
			if (va == null && !initializing) {
				continue;
			}
			if (w.getFixedText() != null) {
				continue;
			}
			String ref = w.getSaveValueTo();
			if (ref != null) {
				value = vars.get(ref);
			}
			if (value == null) {
				WidgetActionUtils.setValue(c, null);
			} else {
				WidgetActionUtils.setValue(c, value.toString());
			}
		}
	}

	public Widget[] getWidgets() {
		return map.values().toArray(new Widget[0]);
	}

	public void setValuesOnMap(LTVariableMap ltMap) {
		Map<String, String> vars = ltMap.getVariables();
		for (Control c : map.keySet()) {
			Widget w = map.get(c);
			String name = w.getSaveValueTo();
			if (name == null) {
				continue;
			}
			String value = WidgetActionUtils.getValueString(c);
			if (value == null) {
				vars.remove(name);
			} else {
				vars.put(name, value);
			}
		}
	}

	public void validateControlValues(RMVariableMap rmMap, IRemoteFileManager manager) throws Throwable {
		Map<String, Object> vars = rmMap.getVariables();
		/*
		 * If there are validators, run them against the value
		 */
		for (Control c : map.keySet()) {
			Widget w = map.get(c);
			String name = w.getSaveValueTo();
			if (name == null) {
				continue;
			}
			Object o = vars.get(name);
			String value = null;
			if (o instanceof Attribute) {
				Attribute ja = (Attribute) o;
				Validator v = ja.getValidator();
				if (v != null) {
					value = WidgetActionUtils.validate(c, v, manager);
				}
			}
			if (value == null) {
				String defaultV = null;
				if (o instanceof Property) {
					defaultV = ((Property) o).getDefault();
				} else if (o instanceof Attribute) {
					defaultV = ((Attribute) o).getDefault();
				}
				if (defaultV != null) {
					WidgetActionUtils.setValue(c, defaultV);
				}
			}
		}
	}
}
