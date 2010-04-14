/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/

package org.eclipse.ptp.rm.pbs.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSNodeProtocolAttributes;

/**
 * Node attributes
 */
public class PBSNodeAttributes extends PBSNodeProtocolAttributes {

	private static final IAttributeDefinition<?,?,?>[] attrDefs = new IAttributeDefinition[]{
				new StringAttributeDefinition(NAME_ATTR_ID, NAME_ATTR_ID, Messages.PBSNodeAttributes_0, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(STATE_ATTR_ID, STATE_ATTR_ID, Messages.PBSNodeAttributes_1, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(NP_ATTR_ID, NP_ATTR_ID, Messages.PBSNodeAttributes_2, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(PROPERTIES_ATTR_ID, PROPERTIES_ATTR_ID, Messages.PBSNodeAttributes_3, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(NTYPE_ATTR_ID, NTYPE_ATTR_ID, Messages.PBSNodeAttributes_4, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(STATUS_ATTR_ID, STATUS_ATTR_ID, Messages.PBSNodeAttributes_5, true, ""), //$NON-NLS-1$
	};
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return attrDefs;
	}
}
