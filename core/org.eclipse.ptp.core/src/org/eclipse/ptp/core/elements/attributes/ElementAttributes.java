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


/**
 * Job attributes
 */
public class ElementAttributes {
	/*
	 * Predefine attributes. These are attributes that
	 * the UI knows about.
	 */
	private final static String ATTR_ID = "id";
	private final static String ATTR_NAME = "name";
	
	private final static StringAttributeDefinition idAttributeDefinition = 
		new StringAttributeDefinition(ATTR_ID, "ID", "Unique ID of element", "");

	private final static StringAttributeDefinition nameAttributeDefinition = 
		new StringAttributeDefinition(ATTR_NAME, "Name", "Name of element", "");

	public static StringAttributeDefinition getIdAttributeDefinition() {
		return idAttributeDefinition;
	}
	
	public static StringAttributeDefinition getNameAttributeDefinition() {
		return nameAttributeDefinition;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				idAttributeDefinition, 
				nameAttributeDefinition
			};
	}
}
