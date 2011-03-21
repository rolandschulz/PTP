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
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;

/**
 * Keeps attributes specific for SMOA Machines
 */
public class SMOAMachineAttributes extends AttributeManager {

	public SMOAMachineAttributes() {
		try {
			addAttribute(MachineAttributes.getNumNodesAttributeDefinition()
					.create());
			addAttribute(MachineAttributes.getStateAttributeDefinition()
					.create(MachineAttributes.State.UP));
		} catch (final IllegalValueException e) {
			throw new RuntimeException("This will never hapen", e); //$NON-NLS-1$
		}
		addAttribute(ElementAttributes.getNameAttributeDefinition().create());
	}

}
