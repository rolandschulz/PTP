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
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPBlockScope;

/**
 * @author aniefer
 */
public class CPPBlockScope extends CPPNamespaceScope implements ICPPBlockScope {
	public CPPBlockScope( IASTNode physicalNode ){
		super( physicalNode );
	}
}
