/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.model.aif;

/**
 * AIF Format Exception
 * 
 * @since 4.0
 */
public class AIFFormatException extends AIFException {

	private static final long serialVersionUID = 6576643238109867525L;

	public AIFFormatException(String msg) {
		super(msg);
	}
}
