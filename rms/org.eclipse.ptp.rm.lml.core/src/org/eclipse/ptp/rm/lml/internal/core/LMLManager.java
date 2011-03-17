package org.eclipse.ptp.rm.lml.internal.core;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiItem;
import org.eclipse.ptp.rm.lml.internal.core.events.JobListSortedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiAddedEvent;

/**
 * Class of the interface ILMLManager
 * @author Claudia Knobloch
 */
public class LMLManager implements ILMLManager{
	
	/*
	 * The shared instance 
	 */
	private ILMLManager manager;
	
	/*
	 * Map of all ILguioItems 
	 */
	protected final Map<String, ILguiItem> LGUIS = new HashMap<String, ILguiItem>();
	
	/*
	 * Source of the xmlFile 
	 */
	private URL xmlFile;
	
	/*
	 * The current considered ILguiItem
	 */
	private ILguiItem fSelectedLguiItem = null;
	
	/*
	 * A list of all listeners on the ILguiItem
	 */
	private final ListenerList lguiListeners = new ListenerList();
	
	/**
	 * Constructor
	 */
	public LMLManager() {
		manager = this;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.ILMLManager#addListener(ILguiListner listener)
	 */
	public void addListener(ILguiListener listener) {
		//TODO Think about adding Listeners also to the ILguiItem
		lguiListeners.add(listener);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.ILMLManager#addLgui(URL xmlFile)
	 */
	public boolean addLgui(URL xmlFile) {
		this.xmlFile = xmlFile;
		if (!LGUIS.containsKey(xmlFile.getPath())) {
			fSelectedLguiItem = new LguiItem(xmlFile);
			synchronized (LGUIS){
				LGUIS.put(xmlFile.getPath(), fSelectedLguiItem);
			}
			fireNewLgui();
			return true;
		} else {
			//TODO load another ILguiItem
			return false;
		}
		
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
	 * org.eclipse.ptp.rm.lml.core.ILMLManager#getManager()
	 */
	public ILMLManager getManager() {
		if (manager == null) {
			manager = new LMLManager();
		}
		return manager;
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
		for (Object listener : lguiListeners.getListeners()) {
			((ILguiListener) listener).handleEvent(event);
		}
	}
	
	/**
	 * Method is called when an ILguiItem was sorted.
	 */
	private void fireSortedLgui() {
		IJobListSortedEvent event = new JobListSortedEvent(this, fSelectedLguiItem);
		for (Object listener : lguiListeners.getListeners()) {
			((ILguiListener) listener).handleEvent(event);
		}
	}

	
	
}
