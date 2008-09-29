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
 * that misses a mandatory attribute.
 * The exception describes which attribute is missing.
 * @author Daniel Felix Ferber
 *
 */
public class MissingParameterException extends IllegalConfigurationException {
	private static final long serialVersionUID = 4658405362914329507L;
	/** The name of the invalid attribute. Names should be taken from{@link AttributeNames}
	 * for consistent and uniform use of names.
	 */
	String parameter;
	
	public MissingParameterException(String parameter) {
		super();
		this.parameter = parameter;
	}
	
	public String getMessage() {
		return NLS.bind(Messages.MissingParameterException_DefaultMessage, parameter);
	}
	
	public String getParameter() {
		return parameter;
	}
}