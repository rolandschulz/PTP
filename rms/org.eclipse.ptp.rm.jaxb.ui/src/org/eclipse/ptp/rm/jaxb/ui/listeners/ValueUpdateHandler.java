package org.eclipse.ptp.rm.jaxb.ui.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.IValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.rm.ui.utils.WidgetListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;

public class ValueUpdateHandler implements IValueUpdateHandler {

	private final Map<Object, IUpdateModel> controlToModelMap;
	private final List<WidgetListener> listeners;

	public ValueUpdateHandler() {
		controlToModelMap = new HashMap<Object, IUpdateModel>();
		listeners = new ArrayList<WidgetListener>();
	}

	public void addListener(WidgetListener listener) {
		listeners.add(listener);
	}

	public void addUpdateModelEntry(Object control, IUpdateModel model) {
		controlToModelMap.put(control, model);
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
	 *            the new value (if any) produced
	 */
	public void handleUpdate(Object source, Object value) {
		List<IUpdateModel> delayed = new ArrayList<IUpdateModel>();
		/*
		 * First we invoke refresh on the viewers, because they produce two data
		 * strings which may be accessed by the other widgets.
		 */
		for (IUpdateModel model : controlToModelMap.values()) {
			if (model == source) {
				continue;
			}
			if (model instanceof ViewerUpdateModel) {
				model.refreshValue();
			} else {
				delayed.add(model);
			}
		}

		for (IUpdateModel model : delayed) {
			model.refreshValue();
		}

		/*
		 * we send three empty events to the WidgetListener so as to trigger any
		 * necessary updates which will set the "dirty" flag.
		 */
		fireModifyEvent();
		fireWidgetSelectedEvent();
		fireWidgetDefaultSelectedEvent();
	}

	/*
	 * It is understood that the listeners will not need the source data of the
	 * event.
	 */
	protected void fireModifyEvent() {
		Event e = new Event();
		ModifyEvent me = new ModifyEvent(e);
		for (WidgetListener l : listeners) {
			l.modifyText(me);
		}
	}

	/*
	 * It is understood that the listeners will not need the source data of the
	 * event.
	 */
	protected void fireWidgetDefaultSelectedEvent() {
		Event e = new Event();
		SelectionEvent se = new SelectionEvent(e);
		for (WidgetListener l : listeners) {
			l.widgetDefaultSelected(se);
		}
	}

	/*
	 * It is understood that the listeners will not need the source data of the
	 * event.
	 */
	protected void fireWidgetSelectedEvent() {
		Event e = new Event();
		SelectionEvent se = new SelectionEvent(e);
		for (WidgetListener l : listeners) {
			l.widgetSelected(se);
		}
	}
}
