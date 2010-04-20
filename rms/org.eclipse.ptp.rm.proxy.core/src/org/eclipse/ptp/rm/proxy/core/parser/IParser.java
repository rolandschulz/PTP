/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation

 *******************************************************************************/

package org.eclipse.ptp.rm.proxy.core.parser;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.ptp.rm.proxy.core.attributes.AttributeDefinition;
import org.eclipse.ptp.rm.proxy.core.element.IElement;

/**
 * The Interface Parser.
 */
public interface IParser {

	/**
	 * Parses the input stream and returns elements according to the attribute
	 * definition.
	 * 
	 * @param attrDef
	 *            the attribute definition
	 * @param in
	 *            the input stream
	 * @return the set of elements read
	 */
	public Set<IElement> parse(AttributeDefinition attrDef, InputStream in);
}
