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
 * Created on Nov 29, 2004
 */
package org.eclipse.cldt.internal.core.dom.parser.cpp;

import org.eclipse.cldt.core.dom.ast.IASTNode;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cldt.core.parser.util.ArrayUtil;

/**
 * @author aniefer
 */
public class CPPNamespaceScope extends CPPScope implements ICPPNamespaceScope{
	IASTNode[] usings = null;
	
    public CPPNamespaceScope( IASTNode physicalNode ) {
		super( physicalNode );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope#getUsingDirectives()
	 */
	public IASTNode[] getUsingDirectives() {
		return (IASTNode[]) ArrayUtil.trim( IASTNode.class, usings, true );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope#addUsingDirective(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective)
	 */
	public void addUsingDirective(IASTNode directive) {
		usings = (IASTNode[]) ArrayUtil.append( IASTNode.class, usings, directive );
	}
}
