package org.eclipse.ptp.rm.jaxb.ui;

public interface IValueUpdateHandler {

	void addUpdateModelEntry(Object control, IUpdateModel model);

	void handleUpdate(Object source, Object value);
}
