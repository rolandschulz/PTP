/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.core.parser.extension;

import org.eclipse.fdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionDialect extends Enum {

	public static final ExtensionDialect GCC = new ExtensionDialect( 1 );
	/**
	 * @param enumValue
	 */
	public ExtensionDialect(int enumValue) {
		super(enumValue);
	}

}
