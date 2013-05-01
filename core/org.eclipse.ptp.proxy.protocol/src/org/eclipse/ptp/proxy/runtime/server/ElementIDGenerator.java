/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dieter Krachtus - Initial API and implementation
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/

package org.eclipse.ptp.proxy.runtime.server;

import org.eclipse.ptp.proxy.messages.Messages;
import org.eclipse.ptp.proxy.util.RangeSet;

/**
 * @since 4.0
 */
public class ElementIDGenerator {
	public static ElementIDGenerator getInstance() {
		if (instance == null)
			instance = new ElementIDGenerator();
		return instance;
	}

	private int baseIDoffset = 0;
	private int base_ID = -1;

	private static ElementIDGenerator instance = null;

	private ElementIDGenerator() {
	}

	public int getBaseID() {
		return base_ID;
	}

	public int getUniqueID() {
		baseIDoffset++;
		return (base_ID + baseIDoffset);
	}

	public RangeSet getUniqueIDs(int size) {
		baseIDoffset++;
		RangeSet range = new RangeSet(base_ID + baseIDoffset, base_ID + baseIDoffset + size);
		baseIDoffset += size - 1;
		return range;
	}

	/**
	 * @since 5.0
	 */
	public void setBaseID(int base_ID) {
		if (this.base_ID != -1) {
			throw new RuntimeException(Messages.getString("ElementIDGenerator.0")); //$NON-NLS-1$
		}
		this.base_ID = base_ID;
	}
}
