/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;

/**
 * Basic API for model objects associated with SWT controls.
 * 
 * @author arossi
 * @since 1.1
 * 
 */
public interface IUpdateModel {

	/**
	 * Get the control to which this model is bound
	 * 
	 * @return control to which this model is bound
	 */
	public Object getControl();

	/**
	 * Get the identifier for the value of the model
	 * 
	 * @return identifier for the value of the model
	 */
	public String getName();

	/**
	 * Get the value from the associated control
	 * 
	 * @return the value retrieved from the associated control/widget
	 */
	public Object getValueFromControl();

	/**
	 * Load values and settings from one attribute map to another for a launch configuration.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param toMap
	 *            destination attribute map
	 * @param fromMap
	 *            source attribute map
	 */
	public void initialize(ILaunchConfiguration configuration, IVariableMap toMap, IVariableMap fromMap);

	/**
	 * Tests if the value of the associated control can be written to the map.
	 * 
	 * @return true if the value can be written
	 */
	public boolean isWritable();

	/**
	 * Update call triggered by the handler.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler
	 */
	public void refreshValueFromMap();

	/**
	 * Restore the value of the control to its default value
	 */
	public void restoreDefault();

	/**
	 * Set the validator for this attribute's value
	 * 
	 * @param validator
	 *            validator for this attribute's value
	 */
	public void setValidator(IValidator validator);

	/**
	 * Retrieves the value from the control and stores it to the current attribute map. Calls the update handler if necessary.
	 * 
	 * @return value from the control
	 */
	public Object storeValue() throws Exception;
}
