/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi;

import java.util.Comparator;

/**
 * @since 5.0
 */
public class PDILocatorComparator implements Comparator<IPDILocator> {

	public static final PDILocatorComparator SINGLETON = new PDILocatorComparator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(IPDILocator x, IPDILocator y) {
		if (!x.getFile().equals(y.getFile()))
			return x.getFile().compareTo(y.getFile());
		else if (!x.getFunction().equals(y.getFunction()))
			return x.getFunction().compareTo(y.getFunction());
		else if (x.getLineNumber() != y.getLineNumber())
			return x.getLineNumber() - y.getLineNumber();
		else
			return x.getAddress().compareTo(y.getAddress());
	}

}
