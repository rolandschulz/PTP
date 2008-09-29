/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.preferences.events;

import java.util.EventListener;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public interface ICellPreferencesChangeListener extends EventListener {

	/**
	 * Notification that a property has changed.
	 * <p>
	 * This method gets called when the observed object fires a property
	 * change event.
	 * </p>
	 *
	 * @param event the property change event object describing which
	 *    property changed and how
	 */
	public void propertyChange(CellPreferencesChangeEvent event);
}
