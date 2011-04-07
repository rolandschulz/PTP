package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

public class ComboUpdateModel extends AbstractUpdateModel implements ModifyListener, SelectionListener {

	private final Combo combo;

	public ComboUpdateModel(String name, ValueUpdateHandler handler, Combo combo) {
		super(name, handler);
		this.combo = combo;
		this.combo.addModifyListener(this);
		this.combo.addSelectionListener(this);
	}

	@Override
	public Object getControl() {
		return combo;
	}

	public Object getValueFromControl() {
		return WidgetActionUtils.getSelected(combo);
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
		String s = ZEROSTR;
		if (mapValue != null) {
			s = (String) mapValue;
		}
		s = WidgetActionUtils.select(combo, s);
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
