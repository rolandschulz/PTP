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
import org.eclipse.ptp.core.messages.Messages;

/**
 * Resource manager attributes
 */
public class ResourceManagerAttributes {
	public enum State {
		STOPPED, STARTED, STARTING, ERROR
	}

	private final static String STATE_ATTR_ID = "rmState"; //$NON-NLS-1$
	private final static String DESC_ATTR_ID = "rmDescription"; //$NON-NLS-1$
	private final static String RMID_ATTR_ID = "rmID"; //$NON-NLS-1$
	private final static String TYPE_ATTR_ID = "rmType"; //$NON-NLS-1$

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = new EnumeratedAttributeDefinition<State>(
			STATE_ATTR_ID, "state", //$NON-NLS-1$
			Messages.ResourceManagerAttributes_0, true, State.STOPPED);
	private final static StringAttributeDefinition descAttrDef = new StringAttributeDefinition(DESC_ATTR_ID, "description", //$NON-NLS-1$
			Messages.ResourceManagerAttributes_1, true, ""); //$NON-NLS-1$
	private final static StringAttributeDefinition rmIDAttrDef = new StringAttributeDefinition(RMID_ATTR_ID, "RM ID", //$NON-NLS-1$
			Messages.ResourceManagerAttributes_2, false, ""); //$NON-NLS-1$
	private final static StringAttributeDefinition typeAttrDef = new StringAttributeDefinition(TYPE_ATTR_ID, "type", //$NON-NLS-1$
			Messages.ResourceManagerAttributes_3, false, Messages.ResourceManagerAttributes_4);

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { stateAttrDef, descAttrDef, typeAttrDef, rmIDAttrDef };
	}

	public static StringAttributeDefinition getDescriptionAttributeDefinition() {
		return descAttrDef;
	}

	public static StringAttributeDefinition getRmIDAttributeDefinition() {
		return rmIDAttrDef;
	}

	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}

	public static StringAttributeDefinition getTypeAttributeDefinition() {
		return typeAttrDef;
	}
}