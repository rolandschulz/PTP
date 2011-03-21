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
import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;

/**
 * Keeps attributes specific for SMOA ResourceManager
 */

public class SMOARMAttributes extends AttributeManager {

	static private StringAttributeDefinition queuingSystem = new StringAttributeDefinition(
			"queuingSystem", Messages.SMOARMAttributes_QueueingSystem, Messages.SMOARMAttributes_QueueingSystem, true, null); //$NON-NLS-1$
	static private StringAttributeDefinition commonName = new StringAttributeDefinition(
			"commonName", Messages.SMOARMAttributes_CommonName, Messages.SMOARMAttributes_CommonName, true, null); //$NON-NLS-1$

	static private BooleanAttributeDefinition acceptsActivities = new BooleanAttributeDefinition(
			"isIsAcceptingNewActivities", Messages.SMOARMAttributes_AcceptsNewActvities, //$NON-NLS-1$
			Messages.SMOARMAttributes_AcceptsNewActvities, true, null);

	static public final BooleanAttributeDefinition getAcceptsActivitiesDef() {
		return acceptsActivities;
	}

	static public final StringAttributeDefinition getCommonNameDef() {
		return commonName;
	}

	static public final StringAttributeDefinition getQueuingSystemDef() {
		return queuingSystem;
	}
}
