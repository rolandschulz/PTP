package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.List;

import org.eclipse.swt.widgets.Control;

public class WidgetRow {
	private List<Control> controls;
	private List<WidgetRow> children;

	public List<WidgetRow> getChildren() {
		return children;
	}

	public List<Control> getControls() {
		return controls;
	}

	public void setChildren(List<WidgetRow> children) {
		this.children = children;
	}

	public void setControls(List<Control> controls) {
		this.controls = controls;
	}
}
