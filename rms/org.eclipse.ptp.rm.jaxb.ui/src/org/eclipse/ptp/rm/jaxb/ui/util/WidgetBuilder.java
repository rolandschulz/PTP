/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.FontDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBRMConfigurableAttributesTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class WidgetBuilder implements IJAXBUINonNLSConstants {

	private final JAXBRMConfigurableAttributesTab tab;
	private final Object gridData;
	private final boolean readOnly;
	private final String title;
	private final String type;

	private Object value;
	private String fixed;
	private String initialValue;
	private String choice;
	private String tooltip;
	private Integer min;
	private Integer max;
	private final String background;
	private final String foreground;
	private final FontDescriptor font;
	private int style;

	public WidgetBuilder(Widget widget, RMVariableMap rmMap, JAXBRMConfigurableAttributesTab tab) {
		this.tab = tab;
		title = widget.getTitle();
		LayoutDataDescriptor descriptor = widget.getLayoutData();
		gridData = LaunchTabBuilder.createLayoutData(descriptor);
		style = WidgetBuilderUtils.getStyle(widget.getStyle());
		readOnly = widget.isReadOnly();
		if (readOnly) {
			style |= SWT.READ_ONLY;
		}
		type = widget.getType();
		tooltip = rmMap.getString(widget.getTooltip());
		if (tooltip == null) {
			tooltip = ZEROSTR;
		} else {
			tooltip = WidgetBuilderUtils.fitToLineLength(64, tooltip);
		}
		Map<String, Object> vars = rmMap.getVariables();
		String ref = widget.getSaveValueTo();
		if (ref != null) {
			Object data = vars.get(ref);
			if (data != null) {
				setData(data);
			}
		}
		fixed = widget.getFixedValue();
		if (fixed != null) {
			fixed = rmMap.getString(fixed);
		}
		background = widget.getBackground();
		foreground = widget.getForeground();
		font = widget.getFont();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Control createControl(final Composite parent) {
		Control c = null;
		if (LABEL.equals(type)) {
			c = WidgetBuilderUtils.createLabel(parent, fixed, style, gridData);
			c.setToolTipText(tooltip);
		} else if (TEXT.equals(type)) {
			if (!ZEROSTR.equals(fixed)) {
				initialValue = fixed;
			}
			Text t = WidgetBuilderUtils.createText(parent, style, gridData, readOnly, initialValue);
			c = t;
			c.setToolTipText(tooltip);
		} else if (RADIOBUTTON.equals(type)) {
			c = WidgetBuilderUtils.createRadioButton(parent, title, initialValue, null);
			c.setToolTipText(tooltip);
		} else if (CHECKBOX.equals(type)) {
			c = WidgetBuilderUtils.createCheckButton(parent, title, null);
			c.setToolTipText(tooltip);
		} else if (SPINNER.equals(type)) {
			Spinner s = WidgetBuilderUtils.createSpinner(parent, gridData, title, min, max, min, null);
			c = s;
			c.setToolTipText(tooltip);
		} else if (COMBO.equals(type)) {
			String[] items = null;
			if (choice != null) {
				items = choice.split(CM);
			} else if (value instanceof Collection) {
				items = (String[]) ((Collection) value).toArray(new String[0]);
			}
			Combo cc = WidgetBuilderUtils.createCombo(parent, style, gridData, items, initialValue, title, tooltip, null);
			c = cc;
		} else if (BROWSELOCAL.equals(type)) {
			final Text t = WidgetBuilderUtils.createText(parent, style, gridData, readOnly, initialValue);
			c = t;
			c.setToolTipText(tooltip);
			WidgetBuilderUtils.createButton(parent, gridData, title, style, new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					try {
						URI uri = new URI(t.getText());
						int type = readOnly ? SWT.OPEN : SWT.SAVE;
						FileDialog d = new FileDialog(parent.getShell(), type);
						d.setFileName(uri.getPath());
						String f = d.open();
						if (f != null) {
							t.setText(new File(f).toURI().toString());
						} else {
							t.setText(ZEROSTR);
						}
					} catch (Throwable t) {
						JAXBUIPlugin.log(t);
					}
				}
			});
			c.setToolTipText(tooltip);
		} else if (BROWSEREMOTE.equals(type)) {
			final Text t = WidgetBuilderUtils.createText(parent, style, gridData, readOnly, initialValue);
			c = t;
			c.setToolTipText(tooltip);
			WidgetBuilderUtils.createButton(parent, gridData, title, style, new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					try {
						URI uri = new URI(t.getText());
						uri = RemoteUIServicesUtils.browse(parent.getShell(), uri, tab.getDelegate(), true, readOnly);
						if (uri != null) {
							t.setText(uri.toString());
						} else {
							t.setText(ZEROSTR);
						}
					} catch (Throwable t) {
						JAXBUIPlugin.log(t);
					}
				}
			});
			c.setToolTipText(tooltip);
		}

		if (c != null) {
			if (foreground != null) {
				c.setForeground(WidgetBuilderUtils.getColor(foreground));
			}
			if (background != null) {
				c.setBackground(WidgetBuilderUtils.getColor(background));
			}
			if (font != null) {
				c.setFont(WidgetBuilderUtils.getFont(font));
			}
		}

		return c;
	}

	private void setData(Object data) {
		if (data instanceof Attribute) {
			Attribute a = (Attribute) data;
			value = a.getValue();
			initialValue = a.getDefault();
			choice = a.getChoice();
			min = a.getMin();
			max = a.getMax();
		} else {
			Property p = (Property) data;
			value = p.getValue();
			if (initialValue == null) {
				initialValue = p.getDefault();
			}
		}
	}
}
