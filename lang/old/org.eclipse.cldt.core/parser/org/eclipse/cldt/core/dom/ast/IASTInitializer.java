/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.core.dom.ast;

/**
 * This represents an initializer for a declarator.
 * 
 * @author Doug Schaefer
 */
public interface IASTInitializer extends IASTNode {
    public final static IASTInitializer[] EMPTY_INIALIZER_ARRAY = new IASTInitializer[0];

}
