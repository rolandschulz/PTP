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
 * Created on Dec 8, 2004
 */
package org.eclipse.cldt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IFunctionType extends IType {
    public IType getReturnType() throws DOMException;
    
    public IType [] getParameterTypes() throws DOMException;
}