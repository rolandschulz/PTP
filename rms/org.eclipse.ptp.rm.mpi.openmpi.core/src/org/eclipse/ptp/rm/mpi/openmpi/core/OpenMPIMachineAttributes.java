/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core;
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
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;


/**
 * Node attributes
 * @author Daniel Felix Ferber
 */
public class OpenMPIMachineAttributes {

	private static final String STATUS_MESSAGE = "Open_MPI_statusMessage"; //$NON-NLS-1$

	private final static StringAttributeDefinition statusMessageAttrDef =
		new StringAttributeDefinition(STATUS_MESSAGE, Messages.OpenMPIMachineAttributes_statusMessageAttrDef_title,
				Messages.OpenMPIMachineAttributes_statusMessageAttrDef_description, true, ""); //$NON-NLS-1$

	/**
	 * Status message if it was not possible to discover the machine.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getStatusMessageAttributeDefinition() {
		return statusMessageAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{statusMessageAttrDef};
	}

}
