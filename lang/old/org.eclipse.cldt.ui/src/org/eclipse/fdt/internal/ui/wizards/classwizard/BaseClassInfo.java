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

import org.eclipse.fdt.core.browser.ITypeInfo;
import org.eclipse.fdt.core.parser.ast.ASTAccessVisibility;


public class BaseClassInfo implements IBaseClassInfo {
    
    private ITypeInfo fType;
    private ASTAccessVisibility fAccess;
    private boolean fIsVirtual;
    
    public BaseClassInfo(ITypeInfo type, ASTAccessVisibility access, boolean isVirtual) {
        fType = type;
        fAccess = access;
        fIsVirtual = isVirtual;
    }

    public ITypeInfo getType() {
        return fType;
    }
    
    public ASTAccessVisibility getAccess() {
        return fAccess;
    }
    
	public boolean isVirtual() {
	    return fIsVirtual;
	}

    public void setAccess(ASTAccessVisibility access) {
        fAccess = access;
    }

    public void setVirtual(boolean isVirtual) {
        fIsVirtual = isVirtual;
    }
}