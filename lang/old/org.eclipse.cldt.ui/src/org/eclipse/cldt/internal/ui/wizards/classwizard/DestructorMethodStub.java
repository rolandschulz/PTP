/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.ui.wizards.classwizard;

import org.eclipse.cldt.core.browser.IQualifiedTypeName;
import org.eclipse.cldt.core.parser.ast.ASTAccessVisibility;


public final class DestructorMethodStub extends AbstractMethodStub {
    
    private static String NAME = NewClassWizardMessages.getString("NewClassCodeGeneration.stub.destructor.name"); //$NON-NLS-1$
    
    public DestructorMethodStub() {
        this(ASTAccessVisibility.PUBLIC, true, false);
    }

    public DestructorMethodStub(ASTAccessVisibility access, boolean isVirtual, boolean isInline) {
        super(NAME, access, isVirtual, isInline);
    }

    public String createMethodDeclaration(IQualifiedTypeName className, IBaseClassInfo[] baseClasses, String lineDelimiter) {
        //TODO should use code templates
        StringBuffer buf = new StringBuffer();
    	if (fIsVirtual){
    	    buf.append("virtual "); //$NON-NLS-1$
    	}
    	buf.append("~"); //$NON-NLS-1$
    	buf.append(className.toString());
    	buf.append("()"); //$NON-NLS-1$
    	if (fIsInline) {
    	    buf.append(" {}"); //$NON-NLS-1$
    	} else {
    	    buf.append(";"); //$NON-NLS-1$
    	}
        return buf.toString();
    }

    public String createMethodImplementation(IQualifiedTypeName className, IBaseClassInfo[] baseClasses, String lineDelimiter) {
        //TODO should use code templates
    	if (fIsInline) {
    		return ""; //$NON-NLS-1$
    	}
    	StringBuffer buf = new StringBuffer();
    	buf.append(className.toString());
    	buf.append("::~"); //$NON-NLS-1$
    	buf.append(className.toString());
    	buf.append("()"); //$NON-NLS-1$
    	buf.append(lineDelimiter);
    	buf.append('{');
    	buf.append(lineDelimiter);
    	//buf.append("// TODO Auto-generated destructor stub");
    	//buf.append(lineDelimiter);
    	buf.append('}');
    	return buf.toString();
    }

    public boolean isDestructor() {
        return true;
    }
}