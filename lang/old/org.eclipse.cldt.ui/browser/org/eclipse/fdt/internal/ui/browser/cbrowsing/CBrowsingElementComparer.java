/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.ui.browser.cbrowsing;

import org.eclipse.fdt.internal.ui.fview.FortranViewElementComparer;

public class CBrowsingElementComparer extends FortranViewElementComparer {

	public boolean equals(Object o1, Object o2) {
		//TODO compare ITypeInfos
	    return super.equals(o1, o2);
	}

	public int hashCode(Object o1) {
		//TODO compare ITypeInfos
	    return super.hashCode(o1);
	}
}
