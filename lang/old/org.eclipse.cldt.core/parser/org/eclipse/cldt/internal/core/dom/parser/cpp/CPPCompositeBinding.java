/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 16, 2004
 */
package org.eclipse.cldt.internal.core.dom.parser.cpp;

import org.eclipse.cldt.core.dom.ast.DOMException;
import org.eclipse.cldt.core.dom.ast.IASTNode;
import org.eclipse.cldt.core.dom.ast.IBinding;
import org.eclipse.cldt.core.dom.ast.IScope;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPCompositeBinding;
import org.eclipse.cldt.core.parser.util.ArrayUtil;

/**
 * @author aniefer
 */
public class CPPCompositeBinding implements ICPPCompositeBinding {

	IBinding [] bindings = null;
	
	public CPPCompositeBinding( IBinding[] bindingList ){
		bindings = (IBinding[]) ArrayUtil.trim( IBinding.class, bindingList, true );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return bindings[0].getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return bindings[0].getNameCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() throws DOMException {
		return bindings[0].getScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPCompositeBinding#getBindings()
	 */
	public IBinding[] getBindings() {
		return bindings;
	}

}
