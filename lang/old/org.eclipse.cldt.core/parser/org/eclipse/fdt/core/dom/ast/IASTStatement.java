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
package org.eclipse.fdt.core.dom.ast;

/**
 * This is the root interface for statements.
 * 
 * @author Doug Schaefer
 */
public interface IASTStatement extends IASTNode {
    public static final IASTStatement[] EMPTY_STATEMENT_ARRAY = new IASTStatement[0];

}
