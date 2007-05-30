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


/**
 * Queue attributes
 */
public class QueueAttributes {
	public enum State {
		NORMAL,
		COLLECTING,
		DRAINING,
		STOPPED
	};

	private static final String STATE_ATTR_ID = "queueState";
	private static final String ID_ATTR_ID = "queueId";

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
		new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "state", "Queue State",
				State.NORMAL);
	
	private final static IntegerAttributeDefinition idAttrDef = 
		new IntegerAttributeDefinition(ID_ATTR_ID, ID_ATTR_ID, "Queue ID", 0);
	
	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}

	public static IntegerAttributeDefinition getIdAttributeDefinition() {
		return idAttrDef;
	}
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, idAttrDef};
	}
}
