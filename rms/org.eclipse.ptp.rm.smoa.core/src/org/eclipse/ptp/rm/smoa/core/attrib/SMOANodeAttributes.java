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
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;

/**
 * Keeps attributes specific for SMOA Node
 */

public class SMOANodeAttributes extends AttributeManager {

	private static final IntegerAttributeDefinition cpuCount = new IntegerAttributeDefinition(
			"cpu_count", Messages.SMOANodeAttributes_CpuCount, Messages.SMOANodeAttributes_CpuCount, true, 1); //$NON-NLS-1$

	private static final StringAttributeDefinition cpuArch = new StringAttributeDefinition(
			"cpu_arch", Messages.SMOANodeAttributes_CpuArch, Messages.SMOANodeAttributes_CpuArch, true, ""); //$NON-NLS-1$ //$NON-NLS-2$

	private static final StringAttributeDefinition memorySize = new StringAttributeDefinition(
			"phys_mem", Messages.SMOANodeAttributes_PhysicalMemory, Messages.SMOANodeAttributes_PhysicalMemory, true, null); //$NON-NLS-1$

	public static StringAttributeDefinition getCpuArchDef() {
		return cpuArch;
	}

	public static IntegerAttributeDefinition getCpuCountDef() {
		return cpuCount;
	}

	public static StringAttributeDefinition getMemorySizeDef() {
		return memorySize;
	}

	public SMOANodeAttributes() {
		addAttribute(ElementAttributes.getNameAttributeDefinition().create());
	}

}
