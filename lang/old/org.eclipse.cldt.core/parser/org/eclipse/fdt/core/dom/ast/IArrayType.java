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

/*
 * Created on Dec 13, 2004
 */
package org.eclipse.fdt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IArrayType extends IType {

    IType getType() throws DOMException;
}
