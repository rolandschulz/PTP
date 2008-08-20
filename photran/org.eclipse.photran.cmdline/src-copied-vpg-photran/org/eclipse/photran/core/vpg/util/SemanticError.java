/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.core.vpg.util;

/**
 * A <code>SemanticError</code> is thrown when some some sort of non-syntactic error is detected in a program
 * (e.g., a module does not exist, a name is multiply declared, or an identifier cannot be resolved).
 * 
 * @author Jeff Overbey
 */
public class SemanticError extends Exception
{
    private static final long serialVersionUID = 1L;

    public SemanticError(String message)
    {
        super(message);
    }
}
