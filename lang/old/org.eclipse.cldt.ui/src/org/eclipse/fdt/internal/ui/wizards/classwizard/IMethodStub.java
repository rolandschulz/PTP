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
package org.eclipse.fdt.internal.ui.wizards.classwizard;

import org.eclipse.fdt.core.browser.IQualifiedTypeName;
import org.eclipse.fdt.core.parser.ast.ASTAccessVisibility;


public interface IMethodStub {
    public String getName();
    public String getDescription();

    public ASTAccessVisibility getAccess();
    public boolean canModifyAccess();
    public void setAccess(ASTAccessVisibility access);

    public boolean isVirtual();
    public boolean canModifyVirtual();
    public void setVirtual(boolean isVirtual);

    public boolean isInline();
    public boolean canModifyInline();
    public void setInline(boolean isVirtual);
    
    public boolean isConstructor();
    public boolean isDestructor();
    
    public String createMethodDeclaration(IQualifiedTypeName className, IBaseClassInfo[] baseClasses, String lineDelimiter);
    public String createMethodImplementation(IQualifiedTypeName className, IBaseClassInfo[] baseClasses, String lineDelimiter);
}
