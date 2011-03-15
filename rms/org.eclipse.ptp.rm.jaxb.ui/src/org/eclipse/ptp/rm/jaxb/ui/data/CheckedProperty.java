package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;

public class CheckedProperty implements IJAXBUINonNLSConstants {
	private String name;
	private String description;
	private boolean checked;
	private boolean configurable;

	public CheckedProperty(Object o) {
		if (o instanceof JobAttribute) {
			JobAttribute ja = (JobAttribute) o;
			name = ja.getName();
			checked = ja.isSelected();
			description = ja.getDescription();
			configurable = ja.isConfigurable();
		} else if (o instanceof Property) {
			Property p = (Property) o;
			name = p.getName();
			checked = p.isSelected();
			description = ZEROSTR;
			configurable = p.isConfigurable();
		} else if (o instanceof String) {
			checked = true;
			name = o.toString();
			description = ZEROSTR;
			configurable = true;
		}
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public boolean isChecked() {
		return checked;
	}

	public boolean isConfigurable() {
		return configurable;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setConfigurable(boolean configurable) {
		this.configurable = configurable;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}
}
