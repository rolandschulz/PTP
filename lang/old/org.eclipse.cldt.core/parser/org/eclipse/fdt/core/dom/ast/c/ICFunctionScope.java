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
 * Created on Nov 17, 2004
 */
package org.eclipse.fdt.core.dom.ast.c;

import org.eclipse.fdt.core.dom.ast.DOMException;
import org.eclipse.fdt.core.dom.ast.IBinding;
import org.eclipse.fdt.core.dom.ast.IScope;

/**
 * @author aniefer
 */
public interface ICFunctionScope extends ICScope {
    public IScope getBodyScope() throws DOMException;
    public IBinding getBinding( char[] name ) throws DOMException;

}
