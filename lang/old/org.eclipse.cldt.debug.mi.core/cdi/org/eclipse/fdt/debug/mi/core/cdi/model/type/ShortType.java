/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.debug.mi.core.cdi.model.type;

import org.eclipse.fdt.debug.core.cdi.model.type.ICDIShortType;
import org.eclipse.fdt.debug.mi.core.cdi.model.Target;

/**
 */
public class ShortType extends IntegralType implements ICDIShortType {

	/**
	 * @param typename
	 */
	public ShortType(Target target, String typename) {
		this(target, typename, false);
	}

	public ShortType(Target target, String typename, boolean usigned) {
		super(target, typename, usigned);
	}
}
