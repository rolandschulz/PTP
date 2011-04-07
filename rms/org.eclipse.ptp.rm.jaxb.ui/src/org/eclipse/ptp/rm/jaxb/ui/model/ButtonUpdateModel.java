package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

public class ButtonUpdateModel extends AbstractUpdateModel implements SelectionListener {

	private final Button button;

	public ButtonUpdateModel(String name, ValueUpdateHandler handler, Button button) {
		super(name, handler);
		this.button = button;
		button.addSelectionListener(this);
	}

	@Override
	public Object getControl() {
		return button;
	}

	public Object getValueFromControl() {
		return button.getSelection();
	}

	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.get(name);
		boolean b = false;
		if (mapValue != null) {
			if (mapValue instanceof String) {
				b = Boolean.parseBoolean((String) mapValue);
			} else {
				b = (Boolean) mapValue;
			}
		}
		button.setSelection(b);
		refreshing = false;
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}

	public void widgetSelected(SelectionEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}
}
