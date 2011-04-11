/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/

package org.eclipse.ptp.rm.lml.core.events;

import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;

/**
 * Interface to manage the event that an LguiItem has been removed.
 * @author Claudia Knobloch
 */
public interface ILguiRemovedEvent {
	
	/**
	 * Getting the involved IlguiItem.
	 * @return the involved ILguiItem
	 */
	public ILguiItem getLguiItem();

	/**
	 * Getting the involved LMLManager.
	 * @return the involved LMLManager
	 */
	public ILMLManager getLMLManager();
}