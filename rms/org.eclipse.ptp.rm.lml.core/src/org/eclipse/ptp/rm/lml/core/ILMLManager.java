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
package org.eclipse.ptp.rm.lml.core;

import java.net.URL;

import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.listeners.IListener;
import org.eclipse.ptp.rm.lml.core.listeners.IViewListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;

/**
 * This interface manages all different ILguiItems and the associated listeners.
 */
public interface ILMLManager{
	
	/**
	 * A new listener for the current considered ILguiItem is added.
	 * @param listener 
	 */
	public void addListener(IViewListener listener, String view);
	
	public void addListener(ILMLListener listener, String view);
	
	public void addListener(IViewListener listener);
	
	public void removeListener(ILMLListener listener);
	
	public void removeListener(IViewListener listener);
	
	public IListener getListener(String view);
	
	/**
	 * Checks if a new ILguiItem has to be created or if there is an existing ILguiItem. The method also creates the ILguItem. 
	 * @param xmlFile the source of the XML file
	 * @return true, if a new ILguiItem was created; otherwise false
	 */
	public boolean addLgui(URL xmlFile);
	
	/**
	 * The current considered IlguiItem was sorted.
	 */
	public void sortLgui();
	
	public void selectLgui(int index);
	
	/**
	 * Getting the current considered ILguiItem.
	 * @return the current considered ILguiItem
	 */
	public ILguiItem getSelectedLguiItem();
	
	public String[] getLguis();
	
	public int getSelectedLguiIndex(String title);
	
	public void removeLgui(String title);
	
}
