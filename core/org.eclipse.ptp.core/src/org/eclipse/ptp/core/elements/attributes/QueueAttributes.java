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
 * Queue attributes
 */
public class QueueAttributes {
	public enum State {
		NORMAL,
		COLLECTING,
		DRAINING,
		STOPPED
	};

	private static final String STATUS_ATTR_ID = "queueStatus"; //$NON-NLS-1$

	private final static StringAttributeDefinition statusAttrDef = 
		new StringAttributeDefinition(STATUS_ATTR_ID, "status", Messages.QueueAttributes_0, //$NON-NLS-1$
				true, ""); //$NON-NLS-1$
	
	public static StringAttributeDefinition getStatusAttributeDefinition() {
		return statusAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{statusAttrDef};
	}
}
