/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.Linkage;

/**
 * @author jcamelon
 */
public class FortranASTName extends FortranASTNode implements IASTName {

    private final char[] name;

    private static final char[] EMPTY_CHAR_ARRAY = {};
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private IBinding binding = null;

    /**
     * @param name
     */
    public FortranASTName(char[] name) {
        this.name = name;
    }

    public FortranASTName() {
        name = EMPTY_CHAR_ARRAY;
    }

    public IBinding resolveBinding() {
        if (binding == null) {
       		FortranVisitor.createBinding(this);
        }

        return binding;
    }

    public IBinding getBinding() {
        return binding;
    }

    public IASTCompletionContext getCompletionContext() {
        IASTNode node = getParent();
    	while (node != null) {
    		if (node instanceof IASTCompletionContext) {
    			return (IASTCompletionContext) node;
    		}
    		node = node.getParent();
    	}
    	
    	return null;
    }

    public void setBinding(IBinding binding) {
        this.binding = binding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (name == EMPTY_CHAR_ARRAY)
            return EMPTY_STRING;
        return new String(name);
    }

    public char[] toCharArray() {
        return name;
    }

    public boolean accept(ASTVisitor action) {
        if (action.shouldVisitNames) {
            switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        
        if (action.shouldVisitNames) {
            switch (action.leave(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTName#isDeclaration()
     */
    public boolean isDeclaration() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
            case IASTNameOwner.r_unclear:
                return false;
            default:
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTName#isReference()
     */
    public boolean isReference() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_definition:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}
}
