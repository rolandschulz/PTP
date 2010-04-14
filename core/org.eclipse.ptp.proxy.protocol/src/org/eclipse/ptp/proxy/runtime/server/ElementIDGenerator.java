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
import org.eclipse.ptp.utils.core.RangeSet;

public class ElementIDGenerator {
	private int baseIDoffset = 0;
	private int base_ID;
	private static ElementIDGenerator instance = null;

	ElementIDGenerator(int base_ID) {
		if (instance != null) {
			throw new RuntimeException(Messages.ElementIDGenerator_0);
		}
		this.base_ID = base_ID;
		instance = this;
	}

	public static ElementIDGenerator getInstance() {
		return instance;
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

	public int getBaseID() {
		return base_ID;
	}
}
