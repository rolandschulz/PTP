/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Feb 11, 2005
 */
package org.eclipse.cldt.internal.core.dom.parser.cpp;

import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.IBinding;
import org.eclipse.cldt.core.dom.ast.IProblemBinding;
import org.eclipse.cldt.core.dom.ast.IType;
import org.eclipse.cldt.core.dom.ast.ITypedef;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPPointerToMemberType;

/**
 * @author aniefer
 */
public class CPPPointerToMemberType extends CPPPointerType implements
		ICPPPointerToMemberType {

	private ICPPClassType clsType = null;
	/**
	 * @param type
	 * @param operator
	 */
	public CPPPointerToMemberType(IType type, ICPPASTPointerToMember operator) {
		super(type, operator);
	}

	public boolean equals( Object o ){
	    if( !super.equals( o ) )
	        return false;
	    
	    if( !( o instanceof CPPPointerToMemberType ) ) 
	        return false;
	    
	    
	    if( o instanceof ITypedef )
	        return o.equals( this );
	    
	    
	    CPPPointerToMemberType pt = (CPPPointerToMemberType) o;
	    IBinding cls = pt.getMemberOfClass();
	    if( cls != null )
	        return cls.equals( getMemberOfClass() );
	    return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType#getMemberOfClass()
	 */
	public IBinding getMemberOfClass() {
		if( clsType == null ){ 
			ICPPASTPointerToMember pm = (ICPPASTPointerToMember) operator;
			IASTName name = pm.getName();
			if( name instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
				if( ns.length > 1 )
					name = ns[ ns.length - 2 ];
				else 
					name = ns[ ns.length - 1 ]; 
			}
			
			IBinding binding = name.resolveBinding();
			if( binding instanceof ICPPClassType ){
				clsType = (ICPPClassType) binding;
			} else {
				clsType = new CPPClassType.CPPClassTypeProblem( IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray() );
			}
		}
		return clsType;
	}

}
