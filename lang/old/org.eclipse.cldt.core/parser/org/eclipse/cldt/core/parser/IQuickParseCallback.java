/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.core.parser;
import java.util.Iterator;

import org.eclipse.cldt.core.parser.ast.IASTCompilationUnit;
/**
 * @author jcamelon
 *
 */
public interface IQuickParseCallback extends ISourceElementRequestor
{
    public abstract Iterator getInclusions();
    public abstract Iterator getMacros();
    /**
     * @return
     */
    public abstract IASTCompilationUnit getCompilationUnit();
    public abstract Iterator iterateOffsetableElements();
    public boolean hasNoProblems();
}