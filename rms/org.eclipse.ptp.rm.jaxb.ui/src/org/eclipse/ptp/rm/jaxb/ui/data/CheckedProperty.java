package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;

public class CheckedProperty implements IJAXBUINonNLSConstants {
	private String name;
	private String description;
	private boolean checked;
	private boolean visible;

	public CheckedProperty(Object o) {
		if (o instanceof Attribute) {
			Attribute ja = (Attribute) o;
			name = ja.getName();
			checked = ja.isSelected();
			description = ja.getDescription();
			visible = ja.isVisible();
		} else if (o instanceof Property) {
			Property p = (Property) o;
			name = p.getName();
			checked = p.isSelected();
			description = ZEROSTR;
			visible = p.isVisible();
		} else if (o instanceof String) {
			checked = true;
			name = o.toString();
			description = ZEROSTR;
			visible = true;
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
		return visible;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setConfigurable(boolean configurable) {
		this.visible = configurable;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}
}
