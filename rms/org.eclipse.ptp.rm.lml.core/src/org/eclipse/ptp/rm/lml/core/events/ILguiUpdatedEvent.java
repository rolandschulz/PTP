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

import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;

/**
 * Interface to manage the event that an LguiItem has been updated.
 * @author Claudia Knobloch
 */
public interface ILguiUpdatedEvent {
	/**
	 * Getting the involved IlguiItem.
	 * @return the involved ILguiItem
	 */
	public LguiItem getLguiItem();

}
