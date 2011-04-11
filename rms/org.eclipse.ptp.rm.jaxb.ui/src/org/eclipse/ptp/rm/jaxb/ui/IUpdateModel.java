/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;

/**
 * Basic API for model objects associated with SWT controls.
 * 
 * @author arossi
 * 
 */
public interface IUpdateModel extends IJAXBUINonNLSConstants {

	/**
	 * @return the control to which this model is bound
	 */
	Object getControl();

	/**
	 * @return identifier for the value of the model
	 */
	String getName();

	/**
	 * @return the value retrieved from the associated control/widget
	 */
	Object getValueFromControl();

	/**
	 * Load values and settings from Launch Tab environment.
	 * 
	 * @param lcMap
	 *            Launch Tab environment
	 */
	void initialize(LCVariableMap lcMap);

	/**
	 * Update call triggered by the handler.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler
	 */
	void refreshValueFromMap();

	/**
	 * Called by the Launch Tab.
	 */
	void restoreDefault();

	/**
	 * @param validator
	 *            used to validate the value entered by the user
	 * @param remoteFileManager
	 *            from the resource manager's services
	 */
	void setValidator(Validator validator, IRemoteFileManager remoteFileManager);
}
