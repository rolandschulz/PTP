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

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Error attributes
 */
public class MessageAttributes {
	public enum Level {
		FATAL,
		ERROR,
		WARNING,
		INFO,
		DEBUG,
		UNDEFINED
	};
	
	private static final String LEVEL_ATTR_ID = "messageLevel";
	private static final String CODE_ATTR_ID = "messageCode";
	private static final String TEXT_ATTR_ID = "messageText";

	private final static EnumeratedAttributeDefinition<Level> levelAttrDef = 
		new EnumeratedAttributeDefinition<Level>(LEVEL_ATTR_ID, "Message Level", 
				"Level of the message", Level.UNDEFINED);

	private final static IntegerAttributeDefinition codeAttrDef = 
		new IntegerAttributeDefinition(CODE_ATTR_ID, "Message Code", 
				"Code assigned to this message", 0);

	private final static StringAttributeDefinition textAttrDef = 
		new StringAttributeDefinition(TEXT_ATTR_ID, "Message Text",
				"Text of message", "");

	public static EnumeratedAttributeDefinition<Level> getLevelAttributeDefinition() {
		return levelAttrDef;
	}
	
	public static IntegerAttributeDefinition getCodeAttributeDefinition() {
		return codeAttrDef;
	}

	public static StringAttributeDefinition getTextAttributeDefinition() {
		return textAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				levelAttrDef, 
				codeAttrDef, 
				textAttrDef
			};
	}
}
