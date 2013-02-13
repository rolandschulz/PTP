/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.control.ui;

/**
 * @since 1.1
 */
public interface IValidator {
	/**
	 * Get the message to display if validation fails
	 * 
	 * @return validation failed message
	 */
	public String getErrorMessage();

	/**
	 * Validate the value using the validation rules specified in the XML configuration
	 * 
	 * @param value
	 *            value to be validated
	 * @return validated value
	 * @throws Exception
	 *             if the value is not valid
	 */
	public Object validate(Object value) throws Exception;
}
