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
package org.eclipse.fdt.debug.mi.core.cdi;

import org.eclipse.fdt.debug.core.cdi.ICDIEndSteppingRange;

/**
 */
public class EndSteppingRange extends SessionObject implements ICDIEndSteppingRange  {

	public EndSteppingRange(Session session) {
		super(session);
	}
}
