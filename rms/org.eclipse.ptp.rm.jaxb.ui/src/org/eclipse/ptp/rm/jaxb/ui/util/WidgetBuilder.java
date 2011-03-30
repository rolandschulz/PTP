package org.eclipse.ptp.rm.jaxb.ui.util;

import java.util.Collection;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.GridDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBRMConfigurableAttributesTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class WidgetBuilder implements IJAXBUINonNLSConstants {

	private final JAXBRMConfigurableAttributesTab tab;
	private final GridData data;
	private Object value;
	private String initialValue;
	private String choice;
	private final String title;
	private final String tooltip;
	private Integer min;
	private Integer max;
	private int style;
	private final boolean readOnly;
	private final String type;

	public WidgetBuilder(Widget widget, RMVariableMap rmMap, JAXBRMConfigurableAttributesTab tab) {
		this.tab = tab;
		title = widget.getLabel();
		GridDataDescriptor gdd = widget.getGridData();
		this.data = LaunchTabBuilder.addGridData(gdd);
		style = WidgetBuilderUtils.getStyle(widget.getStyle());
		readOnly = widget.isReadOnly();
		if (readOnly) {
			style |= SWT.READ_ONLY;
		}
		type = widget.getType();
		tooltip = widget.getTooltip();
		Map<String, Object> vars = rmMap.getVariables();
		Object data = vars.get(widget.getSaveValueTo());
		if (data != null) {
			setData(data);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Control createControl(Composite parent) {
		Control c = null;

		if (LABEL.equals(type)) {
			c = WidgetBuilderUtils.createLabel(parent, title, style, data);
			c.setToolTipText(tooltip);
		} else if (TEXT.equals(type)) {
			Text t = WidgetBuilderUtils.createText(parent, style, data, readOnly, initialValue);
			t.addModifyListener(tab.getWidgetListener());
			t.addSelectionListener(tab.getWidgetListener());
			c = t;
			c.setToolTipText(tooltip);
		} else if (RADIOBUTTON.equals(type)) {
			c = WidgetBuilderUtils.createRadioButton(parent, title, initialValue, tab.getWidgetListener());
			c.setToolTipText(tooltip);
		} else if (CHECKBOX.equals(type)) {
			c = WidgetBuilderUtils.createCheckButton(parent, title, tab.getWidgetListener());
			c.setToolTipText(tooltip);
		} else if (SPINNER.equals(type)) {
			Spinner s = WidgetBuilderUtils.createSpinner(parent, data, title, min, max, min, tab.getWidgetListener());
			s.addSelectionListener(tab.getWidgetListener());
			c = s;
			c.setToolTipText(tooltip);
		} else if (COMBO.equals(type)) {
			String[] items = null;
			if (choice != null) {
				items = choice.split(CM);
			} else if (value instanceof Collection) {
				items = (String[]) ((Collection) value).toArray(new String[0]);
			}
			Combo cc = WidgetBuilderUtils.createCombo(parent, style, data, items, initialValue, title, tooltip,
					tab.getWidgetListener());
			cc.addModifyListener(tab.getWidgetListener());
			c = cc;
		} else if (BROWSELOCAL.equals(type)) {
			// need both a read-only text field and a button;
			// the field needs to be associated with the widget == that would be
			// the 'c'

			// create a text field,
			// create the button
			// rely on the data /layout to put them where they need to be

			c = WidgetBuilderUtils.createButton(parent, data, title, style, new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {

				}
			});
			c.setToolTipText(tooltip);
		} else if (BROWSEREMOTE.equals(type)) {
			c = WidgetBuilderUtils.createButton(parent, data, title, style, new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});
			c.setToolTipText(tooltip);
		}
		return c;
	}

	private void setData(Object data) {
		if (data instanceof Attribute) {
			Attribute a = (Attribute) data;
			value = a.getValue();
			if (!(value instanceof Collection) && !(value instanceof Map)) {
				initialValue = String.valueOf(value);
			}
			if (initialValue == null) {
				initialValue = a.getDefault();
			}
			choice = a.getChoice();
			min = a.getMin();
			max = a.getMax();
		} else {
			Property p = (Property) data;
			value = p.getValue();
			if (!(value instanceof Collection) && !(value instanceof Map)) {
				initialValue = String.valueOf(value);
			}
			if (initialValue == null) {
				initialValue = p.getDefault();
			}
		}
	}
}
