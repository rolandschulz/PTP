package org.eclipse.ptp.rm.jaxb.ui.data;

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
import org.eclipse.ptp.rm.jaxb.ui.ILaunchTabValueHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.widgets.Control;

public class WidgetMap implements ILaunchTabValueHandler {
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

	public void getValuesFromMap(LTVariableMap ltMap) {
		Map<String, ?> vars = ltMap.getVariables();
		Map<String, ?> disc = ltMap.getDiscovered();
		StringBuffer b = new StringBuffer();
		for (Control c : map.keySet()) {
			Object value = null;
			Widget w = map.get(c);
			String ref = w.getSaveValueTo();
			if (ref != null) {
				Object o = vars.get(ref);
				if (o == null) {
					o = disc.get(ref);
				}
				if (o instanceof Property) {
					value = ((Property) o).getValue();
				} else if (o instanceof Attribute) {
					value = ((Attribute) o).getValue();
				}
			} else {
				Widget.ValueArgs va = w.getValueArgs();
				if (va != null) {
					List<Arg> arglist = va.getArg();
					if (arglist != null) {
						b.setLength(0);
						ArgImpl.toString(null, arglist, ltMap, b);
						value = b.toString();
					}
				}
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

	/*
	 * Defaults are recorded in the Property or JobAttribute definitions and are
	 * accessed via the RMVariableMap.
	 * 
	 * The default value does not overwrite a non-null value.
	 */
	public void setDefaultValuesOnControl(RMVariableMap rmMap) {
		Map<String, Object> vars = rmMap.getVariables();
		Map<String, Object> disc = rmMap.getDiscovered();
		for (Control c : map.keySet()) {
			Widget w = map.get(c);

			String name = w.getSaveValueTo();
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
				WidgetActionUtils.setValue(c, defaultValue);
			}
		}
	}

	public void setValuesOnMap(LTVariableMap ltMap) {
		Map<String, String> vars = ltMap.getVariables();
		Map<String, String> disc = ltMap.getDiscovered();
		for (Control c : map.keySet()) {
			Widget w = map.get(c);
			String name = w.getSaveValueTo();
			if (name == null) {
				continue;
			}
			String value = WidgetActionUtils.getValueString(c);
			if (vars.containsKey(name)) {
				vars.put(name, value);
			} else {
				disc.put(name, value);
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
