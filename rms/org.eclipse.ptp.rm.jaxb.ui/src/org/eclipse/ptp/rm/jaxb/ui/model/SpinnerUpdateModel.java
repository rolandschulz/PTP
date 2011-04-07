package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Spinner;

public class SpinnerUpdateModel extends AbstractUpdateModel implements ModifyListener {

	private final Spinner spinner;

	public SpinnerUpdateModel(String name, ValueUpdateHandler handler, Spinner spinner) {
		super(name, handler);
		this.spinner = spinner;
		spinner.addModifyListener(this);
	}

	@Override
	public Object getControl() {
		return spinner;
	}

	public Object getValueFromControl() {
		return spinner.getSelection();
	}

	public void modifyText(ModifyEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}

	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.get(name);
		int i = spinner.getMinimum();
		if (mapValue != null) {
			if (mapValue instanceof String) {
				i = Integer.parseInt((String) mapValue);
			} else {
				i = (Integer) mapValue;
			}
		}
		spinner.setSelection(i);
		refreshing = false;
	}
}
