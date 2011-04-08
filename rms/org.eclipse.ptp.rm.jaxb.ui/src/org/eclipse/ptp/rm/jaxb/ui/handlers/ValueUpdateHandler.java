package org.eclipse.ptp.rm.jaxb.ui.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.IValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.ui.utils.WidgetListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

public class ValueUpdateHandler implements IValueUpdateHandler {

	private final Map<Object, IUpdateModel> controlToModelMap;
	private final List<WidgetListener> listeners;
	private Widget proxy;

	public ValueUpdateHandler() {
		controlToModelMap = new HashMap<Object, IUpdateModel>();
		listeners = new ArrayList<WidgetListener>();
	}

	public void addListener(WidgetListener listener) {
		listeners.add(listener);
	}

	public void addUpdateModelEntry(Object control, IUpdateModel model) {
		if (proxy == null && control instanceof Widget) {
			proxy = (Widget) control;
		}
		controlToModelMap.put(control, model);
	}

	public Map<Object, IUpdateModel> getControlToModelMap() {
		return controlToModelMap;
	}

	/**
	 * Broadcasts update request to all other controls by invoking refresh on
	 * their model objects.
	 * 
	 * The value can largely be ignored.
	 * 
	 * @param source
	 *            the control which has been modified
	 * @param value
	 *            the new value (if any) produced (unused here)
	 */
	public void handleUpdate(Object source, Object value) {
		for (Object control : controlToModelMap.keySet()) {
			if (control == source) {
				continue;
			}
			controlToModelMap.get(control).refreshValueFromMap();
		}

		for (Object control : controlToModelMap.keySet()) {
			if (control instanceof Viewer) {
				WidgetActionUtils.refreshViewer((Viewer) control);
			}
		}

		/*
		 * we send an empty event to the WidgetListener so as to trigger any
		 * necessary updates which will set the "dirty" flag.
		 */
		fireModifyEvent();
	}

	/*
	 * It is understood that the listeners will not need the source data of the
	 * event.
	 */
	protected void fireModifyEvent() {
		Event e = new Event();
		e.widget = proxy;
		ModifyEvent me = new ModifyEvent(e);
		for (WidgetListener l : listeners) {
			l.modifyText(me);
		}
	}
}
