/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.internal.core;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiSelectedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.listeners.IListener;
import org.eclipse.ptp.rm.lml.core.listeners.IViewListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.events.JobListSortedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiAddedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiSelectedEvent;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;

/**
 * Class of the interface ILMLManager
 */
public class LMLManager implements ILMLManager{
	
	/*
	 * Map of all ILguioItems 
	 */
	protected final Map<String, ILguiItem> LGUIS = new HashMap<String, ILguiItem>();
	
	/*
	 * The current considered ILguiItem
	 */
	private ILguiItem fSelectedLguiItem = null;
	
	/*
	 * A list of all listeners on the ILguiItem
	 */
	private ListenerList lmlListeners = new ListenerList();
	
	private ListenerList viewListeners = new ListenerList();
	
	private final Map<String, IListener> listeners = new HashMap<String, IListener>();

	public void addListener(IViewListener listener) {
		viewListeners.add(listener);
		listeners.put("ViewManager", listener);
	}
	
	public void addListener(IViewListener listener, String view) {
		viewListeners.add(listener);
		listeners.put(view,listener);
	}
	
	public void addListener(ILMLListener listener, String view) {
		lmlListeners.add(listener);
//		listeners.put(view, (IListener) listener);
	}
	
	public IListener getListener(String view) {
		return listeners.get(view);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.ILMLManager#addLgui(URL xmlFile)
	 */
	public boolean addLgui(URL xmlFile) {
		if (!LGUIS.containsKey(xmlFile.getPath())) {
			fSelectedLguiItem = new LguiItem(xmlFile);
			synchronized (LGUIS){
				LGUIS.put(xmlFile.getPath(), fSelectedLguiItem);
			}
			fireNewLgui();
			return false;
		} else {
			return true;
		}	
	}
	
	public String[] getLguis() {
		Set<String> lguis = LGUIS.keySet();
		return lguis.toArray(new String[lguis.size()]);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.ILMLManager#sortLgui
	 */
	public void sortLgui() {
		fireSortedLgui();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.ILMLManager#getSelectedLguiItem()
	 */
	public ILguiItem getSelectedLguiItem() {
		return fSelectedLguiItem;
	}
	
	/**
	 * Method is called when a new ILguiItem was generated.
	 */
	private void fireNewLgui() {
		ILguiAddedEvent event = new LguiAddedEvent(this, fSelectedLguiItem);
		for (Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}
	
	/**
	 * Method is called when an ILguiItem was sorted.
	 */
	private void fireSortedLgui() {
		IJobListSortedEvent event = new JobListSortedEvent(this, fSelectedLguiItem);
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}
	
	private void fireSelectedLgui() {
		ILguiSelectedEvent event = new LguiSelectedEvent(this, fSelectedLguiItem);
		for (Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}

	private String getSelectedLguiTitle(int index) {
		String[] lguis = LGUIS.keySet().toArray(new String[LGUIS.size()]);
		return lguis[index];
	}
	
	public void selectLgui(int index) {
		fSelectedLguiItem = LGUIS.get(getSelectedLguiTitle(index));
		fireSelectedLgui();
	}
	
	public void selectLgui(URL xmlFile) {
		fSelectedLguiItem = LGUIS.get(xmlFile.getPath());
		fireSelectedLgui();
	}

	public int getSelectedLguiIndex(String title) {
		String[] lguis = LGUIS.keySet().toArray(new String[LGUIS.size()]);
		int index = 0;
		for (int i = 0; i < lguis.length; i++) {
			if (title.equals(lguis[i])) {
				index = i;
			}
		}
		return index;
	}

	public void removeLgui(String title) {
		LGUIS.remove(title);
		if (LGUIS.isEmpty()) {
			fSelectedLguiItem = null;
		} else {
			fSelectedLguiItem = LGUIS.get(getLguis()[0]);
		}
		fireSelectedLgui();
	}

	public void removeListener(ILMLListener listener) {
		lmlListeners.remove(listener);
	}
	
	public void removeListener(IViewListener listener) {
		viewListeners.remove(listener);
		listeners.remove("ViewManager");
	}
}
