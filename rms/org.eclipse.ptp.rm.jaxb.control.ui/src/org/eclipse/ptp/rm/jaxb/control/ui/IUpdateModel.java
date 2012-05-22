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

import org.eclipse.ptp.rm.jaxb.control.ui.launch.IJAXBParentLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ValidatorType;

/**
 * Basic API for model objects associated with SWT controls.
 * 
 * @author arossi
 * 
 */
public interface IUpdateModel {

	/**
	 * @return the control to which this model is bound
	 */
	public Object getControl();

	/**
	 * @return identifier for the value of the model
	 */
	public String getName();

	/**
	 * @return the value retrieved from the associated control/widget
	 */
	public Object getValueFromControl();

	/**
	 * Load values and settings from Launch Tab environment.
	 * 
	 * @param rmMap
	 *            ResourceManager environment
	 * @param lcMap
	 *            Launch Tab environment
	 */
	public void initialize(IVariableMap rmMap, LCVariableMap lcMap);

	/**
	 * Whether the value of the associated control can be written to the environment.
	 * 
	 * @return true is the value can be written
	 */
	public boolean isWritable();

	/**
	 * Update call triggered by the handler.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler
	 */
	public void refreshValueFromMap();

	/**
	 * Called by the Launch Tab.
	 */
	public void restoreDefault();

	/**
	 * @param validator
	 *            used to validate the value entered by the user
	 * @param tab
	 *            parent tab (from which to get the remote services delegate)
	 */
	public void setValidator(ValidatorType validator, IJAXBParentLaunchConfigurationTab tab);
}
