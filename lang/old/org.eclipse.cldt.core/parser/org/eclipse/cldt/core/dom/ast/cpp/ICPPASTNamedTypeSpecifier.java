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
package org.eclipse.cldt.core.dom.ast.cpp;

import org.eclipse.cldt.core.dom.ast.IASTNamedTypeSpecifier;

/**
 * @author jcamelon
 */
public interface ICPPASTNamedTypeSpecifier extends IASTNamedTypeSpecifier, ICPPASTDeclSpecifier {
    
    public boolean isTypename();
    public void setIsTypename( boolean value );

}
