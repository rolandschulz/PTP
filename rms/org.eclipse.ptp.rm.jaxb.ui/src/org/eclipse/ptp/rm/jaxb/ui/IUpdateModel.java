package org.eclipse.ptp.rm.jaxb.ui;

public interface IUpdateModel extends IJAXBUINonNLSConstants {

	Object getValue();

	String getValueAsString();

	void refreshValue();

	void setValue(Object value);

	void setValueAsString(String value);
}
