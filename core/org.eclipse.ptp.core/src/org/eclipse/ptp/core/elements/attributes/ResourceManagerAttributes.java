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
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Resource manager attributes
 */
public class ResourceManagerAttributes {
	public enum State {
		ERROR,
		STARTING,
		STARTED,
		STOPPING,
		STOPPED,
	    SUSPENDED
	}
	
	private final static String STATE_ATTR_ID = "rmState";

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
		new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "state", "RM State", State.STOPPED);
	
	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	private final static String DESC_ATTR_ID = "rmDescription";
	private final static StringAttributeDefinition descAttrDef = 
		new StringAttributeDefinition(DESC_ATTR_ID, "description", "RM description", "");
	public static StringAttributeDefinition getDescriptionAttributeDefinition() {
		return descAttrDef;
	}
	
	private final static String RMID_ATTR_ID = "rmID";
	private final static StringAttributeDefinition rmIDAttrDef = 
		new StringAttributeDefinition(RMID_ATTR_ID, "RM ID", "RM unique identifier", "");
	public static StringAttributeDefinition getRmIDAttributeDefinition() {
		return rmIDAttrDef;
	}
	
	private final static String TYPE_ATTR_ID = "rmType";
	private final static StringAttributeDefinition typeAttrDef = 
		new StringAttributeDefinition(TYPE_ATTR_ID, "type", "RM type", "unspecified");
	public static StringAttributeDefinition getTypeAttributeDefinition() {
		return typeAttrDef;
	}
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, descAttrDef, typeAttrDef, rmIDAttrDef};
	}
}