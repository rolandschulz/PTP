/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator.core;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.simulator.conf.AttributeNames;


/**
 * Thrown when trying to launch the simulator with a launch configuration
 * that contains invalid attributes.
 * The exception describes which attribute is invalid.
 * @author Daniel Felix Ferber
 *
 */
public class IllegalParameterException extends IllegalConfigurationException {
	private static final long serialVersionUID = 3901449356414045131L;
	/** The name of the invalid attribute. Names should be taken from{@link AttributeNames}
	 * for consistent and uniform use of names.
	 */
	String parameter;
	/** The value of the invalid attribute. */
	String value;

	public IllegalParameterException(String message, String parameter, String value) {
		super(message);
		this.parameter=parameter;
		this.value=value;
	}
	
	public IllegalParameterException(String message, String parameter, int value) {
		this(message, parameter, Integer.toString(value));
	}
	
	public String getMessage() {
		String valueString = null;
		if (value == null) {
			valueString = Messages.IllegalParameterException_NullValue;
		} else if (value.trim().length() == 0) {
			valueString = Messages.IllegalParameterException_EmptyValue;
		} else {
			valueString = value;
		}
		return NLS.bind(Messages.IllegalParameterException_DefaultMessage, new String[] {parameter, valueString, super.getMessage()});
	}
}
