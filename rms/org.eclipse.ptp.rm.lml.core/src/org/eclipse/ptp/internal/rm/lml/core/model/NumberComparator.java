/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

import java.util.Comparator;

/**
 * Class for comparing integer-values in ascending or descending way.
 */
public class NumberComparator implements Comparator<Integer> {

	// if true => sort ascending, otherwise descending
	private final boolean ascending;

	public NumberComparator(boolean ascending) {
		this.ascending = ascending;
	}

	public int compare(Integer o1, Integer o2) {
		if (ascending) {
			return o1 - o2;
		} else {
			return o2 - o1;
		}
	}

}
