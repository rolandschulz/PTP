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
package org.eclipse.fdt.core.dom.ast;

/**
 * @author jcamelon
 */
public interface IASTFunctionStyleMacroParameter extends IASTNode {

   public static final IASTFunctionStyleMacroParameter[] EMPTY_PARAMETER_ARRAY = new IASTFunctionStyleMacroParameter[0];
   
   public String getParameter();
   public void   setParameter( String value );
   
}
