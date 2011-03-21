/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.attrib;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;

/**
 * Keeps attributes specific for SMOA Queues
 */

public class SMOAQueueAttributes extends AttributeManager {
	public SMOAQueueAttributes() {
		addAttribute(QueueAttributes.getStateAttributeDefinition().create());
		addAttribute(ElementAttributes.getNameAttributeDefinition().create());
		addAttribute(ElementAttributes.getIdAttributeDefinition().create());
	}
}
