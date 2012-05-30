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
import org.eclipse.ptp.core.messages.Messages;

/**
 * Error attributes
 */
@Deprecated
public class MessageAttributes {
	public enum Level {
		FATAL,
		ERROR,
		WARNING,
		INFO,
		DEBUG,
		UNDEFINED
	};

	private static final String LEVEL_ATTR_ID = "messageLevel"; //$NON-NLS-1$
	private static final String CODE_ATTR_ID = "messageCode"; //$NON-NLS-1$
	private static final String TEXT_ATTR_ID = "messageText"; //$NON-NLS-1$

	private final static EnumeratedAttributeDefinition<Level> levelAttrDef = new EnumeratedAttributeDefinition<Level>(
			LEVEL_ATTR_ID, "Message Level", //$NON-NLS-1$
			Messages.MessageAttributes_0, true, Level.UNDEFINED);

	private final static IntegerAttributeDefinition codeAttrDef = new IntegerAttributeDefinition(CODE_ATTR_ID, "Message Code", //$NON-NLS-1$
			Messages.MessageAttributes_1, true, 0);

	private final static StringAttributeDefinition textAttrDef = new StringAttributeDefinition(TEXT_ATTR_ID, "Message Text", //$NON-NLS-1$
			Messages.MessageAttributes_2, true, ""); //$NON-NLS-1$

	public static IntegerAttributeDefinition getCodeAttributeDefinition() {
		return codeAttrDef;
	}

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { levelAttrDef, codeAttrDef, textAttrDef };
	}

	public static EnumeratedAttributeDefinition<Level> getLevelAttributeDefinition() {
		return levelAttrDef;
	}

	public static StringAttributeDefinition getTextAttributeDefinition() {
		return textAttrDef;
	}
}
