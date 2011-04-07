package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

public class TextUpdateModel extends DynamicControlUpdateModel implements ModifyListener {

	private final Text text;

	public TextUpdateModel(List<Arg> args, ValueUpdateHandler handler, Text text) {
		super(args, handler);
		this.text = text;
		text.addModifyListener(this);
	}

	public TextUpdateModel(String name, ValueUpdateHandler handler, Text text) {
		super(name, handler);
		this.text = text;
		text.addModifyListener(this);
	}

	@Override
	public Object getControl() {
		return text;
	}

	public Object getValueFromControl() {
		return text.getText();
	}

	public void modifyText(ModifyEvent e) {
		if (refreshing) {
			return;
		}
		storeValue();
	}

	public void refreshValueFromMap() {
		refreshing = true;
		if (!canSave) {
			mapValue = getResolvedDynamic();
		} else {
			mapValue = lcMap.get(name);
		}
		String s = ZEROSTR;
		if (mapValue != null) {
			s = (String) mapValue;
		}
		text.setText(s);
		refreshing = false;
	}
}
