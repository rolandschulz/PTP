package org.eclipse.ptp.rtsystem.event;

public class RuntimeNodeGeneralChangedEvent implements IRuntimeNodeGeneralChangedEvent {
	public String[] keys;
	public String[] values;
	public RuntimeNodeGeneralChangedEvent(String[] keys, String[] values) {
		this.keys = keys;
		this.values = values;
	}
	public String[] getKeys() {
		return keys;
	}
	public String[] getValues() {
		return values;
	}
}
