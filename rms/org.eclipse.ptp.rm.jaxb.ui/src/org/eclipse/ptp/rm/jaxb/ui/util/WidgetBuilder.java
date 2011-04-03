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
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.FontDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArgImpl;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBRMConfigurableAttributesTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

public class WidgetBuilder implements IJAXBUINonNLSConstants {

	private final JAXBRMConfigurableAttributesTab tab;

	private Object gridData;
	private boolean readOnly;
	private String title;
	private String type;
	private String label;
	private String initialValue;
	private String choice;
	private String tooltip;
	private List<String> valueList;
	private Integer min;
	private Integer max;
	private String background;
	private String foreground;
	private FontDescriptor font;
	private int style;

	public WidgetBuilder(Widget widget, RMVariableMap rmMap, JAXBRMConfigurableAttributesTab tab) {
		this.tab = tab;
		setWidgetData(widget);
		setMapDependentData(widget, rmMap);
	}

	@SuppressWarnings({})
	public Control createControl(final Composite parent) {
		Control c = null;
		if (LABEL.equals(type)) {
			c = WidgetBuilderUtils.createLabel(parent, label, style, gridData);
		} else if (TEXT.equals(type)) {
			c = createText(parent);
		} else if (RADIOBUTTON.equals(type)) {
			c = WidgetBuilderUtils.createRadioButton(parent, title, initialValue, null);
		} else if (CHECKBOX.equals(type)) {
			c = WidgetBuilderUtils.createCheckButton(parent, title, null);
		} else if (SPINNER.equals(type)) {
			c = WidgetBuilderUtils.createSpinner(parent, gridData, title, min, max, min, null);
		} else if (COMBO.equals(type)) {
			c = createCombo(parent);
		} else if (BROWSELOCAL.equals(type)) {
			c = createBrowseLocal(parent);
		} else if (BROWSEREMOTE.equals(type)) {
			c = createBrowseRemote(parent);
		}

		if (c != null) {
			if (!ZEROSTR.equals(tooltip)) {
				c.setToolTipText(tooltip);
			}
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

	private Control createBrowseLocal(final Composite parent) {
		final Text t = WidgetBuilderUtils.createText(parent, style, gridData, readOnly, initialValue);
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
		return t;
	}

	private Control createBrowseRemote(final Composite parent) {
		final Text t = WidgetBuilderUtils.createText(parent, style, gridData, readOnly, initialValue);
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
		return t;
	}

	private Control createCombo(Composite parent) {
		String[] items = null;
		if (valueList != null) {
			items = valueList.toArray(new String[0]);
		} else if (choice != null) {
			items = choice.split(CM);
		}
		if (items == null) {
			items = new String[0];
		}
		return WidgetBuilderUtils.createCombo(parent, style, gridData, items, initialValue, title, tooltip, null);
	}

	private Control createText(final Composite parent) {
		if (!ZEROSTR.equals(label)) {
			initialValue = label;
		}
		Control c = WidgetBuilderUtils.createText(parent, style, gridData, readOnly, initialValue);
		return c;
	}

	private void setMapDependentData(Widget widget, RMVariableMap rmMap) {
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
				setPropertyData(data);
			}
		}
		ref = widget.getValueListFrom();
		if (ref != null) {
			Object data = vars.get(ref);
			if (data != null) {
				setValueListData(data);
			}
		}

		label = widget.getFixedText();
		if (label == null) {
			Widget.DynamicText dt = widget.getDynamicText();
			if (dt != null) {
				if (dt.getArg() != null) {
					label = ArgImpl.toString(null, dt.getArg(), rmMap);
				}
			}
		} else {
			label = rmMap.getString(label);
		}

	}

	private void setPropertyData(Object data) {
		if (data instanceof Attribute) {
			Attribute a = (Attribute) data;
			initialValue = a.getDefault();
			choice = a.getChoice();
			min = a.getMin();
			max = a.getMax();
		} else {
			Property p = (Property) data;
			if (initialValue == null) {
				initialValue = p.getDefault();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setValueListData(Object data) {
		if (data instanceof Attribute) {
			Attribute a = (Attribute) data;
			Object value = a.getValue();
			if (value instanceof List<?>) {
				valueList = (List<String>) value;
			}
		} else {
			Property p = (Property) data;
			Object value = p.getValue();
			if (value instanceof List<?>) {
				valueList = (List<String>) value;
			}
		}
	}

	private void setWidgetData(Widget widget) {
		title = widget.getTitle();
		LayoutDataDescriptor descriptor = widget.getLayoutData();
		gridData = LaunchTabBuilder.createLayoutData(descriptor);
		style = WidgetBuilderUtils.getStyle(widget.getStyle());
		readOnly = widget.isReadOnly();
		if (readOnly) {
			style |= SWT.READ_ONLY;
		}
		type = widget.getType();
		background = widget.getBackground();
		foreground = widget.getForeground();
		font = widget.getFont();
	}
}
