package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.ptp.rm.ui.utils.WidgetListener;

public interface IValueUpdateHandler {

	void addUpdateModelEntry(Object control, IUpdateModel model);

	void addListener(WidgetListener listener);

	void handleUpdate(Object source, Object value);
}
