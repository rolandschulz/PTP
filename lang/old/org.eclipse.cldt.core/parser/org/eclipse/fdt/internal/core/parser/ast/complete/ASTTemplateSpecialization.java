/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.core.parser.ast.complete;

import org.eclipse.fdt.core.parser.ast.IASTScope;
import org.eclipse.fdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.fdt.internal.core.parser.pst.ITemplateSymbol;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateSpecialization extends ASTTemplateDeclaration implements IASTTemplateSpecialization
{
    /**
     * @param filename
     * 
     */
    public ASTTemplateSpecialization( ITemplateSymbol template, IASTScope scope, char []filename  )
    {
        super(template, scope, null, filename);
    }
}
