/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.core.dom.ast.c;

import org.eclipse.fdt.core.dom.ast.DOMException;
import org.eclipse.fdt.core.dom.ast.IArrayType;

/**
 * @author dsteffle
 */
public interface ICArrayType extends IArrayType {
	public boolean isConst() throws DOMException;
	public boolean isRestrict() throws DOMException;
	public boolean isVolatile() throws DOMException;
	public boolean isStatic() throws DOMException;
	public boolean isVariableLength() throws DOMException;
}
