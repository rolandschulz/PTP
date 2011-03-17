/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */
package org.eclipse.ptp.rm.lml.core;

import java.net.URL;

import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;

/**
 * This interface manages all different ILguiItems and the associated listeners.
 * 
 * @author Claudia Knobloch
 */
public interface ILMLManager{
	
	/**
	 * A new listener for the current considered ILguiItem is added.
	 * @param listener 
	 */
	public void addListener(ILguiListener listener);
	
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
	
	/**
	 * Getting the current LMLManager instance. 
	 * @return a reference on the current LMLManager
	 */
	public ILMLManager getManager();
	
	/**
	 * Getting the current considered ILguiItem.
	 * @return the current considered ILguiItem
	 */
	public ILguiItem getSelectedLguiItem();
	
}
