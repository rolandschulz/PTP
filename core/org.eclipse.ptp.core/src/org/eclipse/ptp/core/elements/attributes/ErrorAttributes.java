/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Error attributes
 */
public class ErrorAttributes {
	private static final String CODE_ATTR_ID = "errorCode";
	private static final String MESSAGE_ATTR_ID = "errorMsg";

	private final static IntegerAttributeDefinition codeAttrDef = 
		new IntegerAttributeDefinition(CODE_ATTR_ID, "Error Code", 
				"Code assigned to this error", 0);

	private final static StringAttributeDefinition msgAttrDef = 
		new StringAttributeDefinition(MESSAGE_ATTR_ID, "Error Message",
				"Text of error message", "");

	public static IntegerAttributeDefinition getCodeAttributeDefinition() {
		return codeAttrDef;
	}

	public static StringAttributeDefinition getMsgAttributeDefinition() {
		return msgAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				codeAttrDef, 
				msgAttrDef
			};
	}
}
