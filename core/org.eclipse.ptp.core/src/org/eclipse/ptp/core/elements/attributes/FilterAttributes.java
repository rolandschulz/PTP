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

import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;

/**
 * Filter attributes
 */
@Deprecated
public class FilterAttributes {
	private final static String ATTR_FILTER_CHILDREN = "filterChildren"; //$NON-NLS-1$

	private final static BooleanAttributeDefinition filterChildrenAttributeDefinition = new BooleanAttributeDefinition(
			ATTR_FILTER_CHILDREN, ATTR_FILTER_CHILDREN, ATTR_FILTER_CHILDREN, false, false);

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { filterChildrenAttributeDefinition };
	}

	public static BooleanAttributeDefinition getFilterChildrenAttributeDefinition() {
		return filterChildrenAttributeDefinition;
	}
}
