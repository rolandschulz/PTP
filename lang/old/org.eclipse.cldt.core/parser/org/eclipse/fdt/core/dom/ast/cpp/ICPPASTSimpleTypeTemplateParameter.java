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
package org.eclipse.fdt.core.dom.ast.cpp;

import org.eclipse.fdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public interface ICPPASTSimpleTypeTemplateParameter extends
        ICPPASTTemplateParameter {

    public static final int st_class = 1;
    public static final int st_typename = 2;
    
    public int getParameterType();
    public void setParameterType( int value );
    
    public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty( "Name" ); //$NON-NLS-1$
    public IASTName getName();
    public void setName( IASTName name );

    public static final ASTNodeProperty DEFAULT_TYPE = new ASTNodeProperty( "Default Type"); //$NON-NLS-1$
    public IASTTypeId getDefaultType();
    public void setDefaultType( IASTTypeId typeId );

}
