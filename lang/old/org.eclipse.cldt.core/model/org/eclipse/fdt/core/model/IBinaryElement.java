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
package org.eclipse.fdt.core.model;

import org.eclipse.fdt.core.IAddress;


/**
 */
public interface IBinaryElement extends ICElement {

	/**
 	 * Returns the address of the function. This method will return,
 	 * the address of a symbol for children of IBinaryObject.
 	 *
 	 * @exception CModelException if this element does not have address
 	 * information.
 	 */
 	IAddress getAddress() throws CModelException;

	/**
 	 * Returns the binary object the element belongs to.
	 *
 	 */
	IBinary getBinary();
}
