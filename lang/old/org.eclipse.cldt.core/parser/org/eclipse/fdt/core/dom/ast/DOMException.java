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
 * Created on Jan 31, 2005
 */
package org.eclipse.fdt.core.dom.ast;

import org.eclipse.fdt.internal.core.dom.parser.cpp.CPPSemantics;

/**
 * @author aniefer
 */
public class DOMException extends Exception {
    IProblemBinding problemBinding;
    /**
     * 
     */
    public DOMException( IProblemBinding problem ) {
        super( problem != null ? problem.getMessage() : CPPSemantics.EMPTY_NAME );
        problemBinding = problem;
    }

    public IProblemBinding getProblem(){
        return problemBinding;
    }
}
