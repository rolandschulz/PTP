package org.eclipse.ptp.rm.ui.preferences;


public class PreferenceWidgetListener {
	private boolean listenerEnabled = true;

	public synchronized void enable() { listenerEnabled = true; }
	public synchronized void disable() { listenerEnabled = false; }

	public synchronized boolean isEnabled() {
		return listenerEnabled;
	}
	
}
