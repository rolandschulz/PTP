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
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.messages.Messages;

/**
 * Element attributes
 */
@Deprecated
public class ElementAttributes {
	/*
	 * Predefine attributes. These are attributes that the UI knows about.
	 */
	private final static String ATTR_ID = "id"; //$NON-NLS-1$
	private final static String ATTR_NAME = "name"; //$NON-NLS-1$

	private final static StringAttributeDefinition idAttributeDefinition = new StringAttributeDefinition(ATTR_ID,
			"ID", Messages.ElementAttributes_0, false, ""); //$NON-NLS-1$ //$NON-NLS-2$

	private final static StringAttributeDefinition nameAttributeDefinition = new StringAttributeDefinition(ATTR_NAME,
			"Name", Messages.ElementAttributes_1, true, ""); //$NON-NLS-1$ //$NON-NLS-2$

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { idAttributeDefinition, nameAttributeDefinition };
	}

	public static StringAttributeDefinition getIdAttributeDefinition() {
		return idAttributeDefinition;
	}

	public static StringAttributeDefinition getNameAttributeDefinition() {
		return nameAttributeDefinition;
	}
}
