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
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.IASTDeclaration;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;

/**
 * @author jcamelon
 */
public class CPPASTTemplateSpecialization extends CPPASTNode implements
        ICPPASTTemplateSpecialization {

    private IASTDeclaration declaration;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization#getDeclaration()
     */
    public IASTDeclaration getDeclaration() {
        return declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization#setDeclaration(org.eclipse.fdt.core.dom.ast.IASTDeclaration)
     */
    public void setDeclaration(IASTDeclaration declaration) {
        this.declaration = declaration;
    }

}
