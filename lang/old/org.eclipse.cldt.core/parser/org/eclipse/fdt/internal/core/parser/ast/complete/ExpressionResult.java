/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.core.parser.ast.complete;

import org.eclipse.fdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.fdt.internal.core.parser.pst.TypeInfoProvider;

/**
 * @author hamer
 *
 */
public class ExpressionResult {
	
 
 private ITypeInfo result;
 private boolean failedToDereference = false;
 
 ExpressionResult(){
 	result = TypeInfoProvider.newTypeInfo();
 }
 ExpressionResult(ITypeInfo result){
 	this.result = result;
 }
/**
 * @return
 */
public ITypeInfo getResult() {
	return result;
}

/**
 * @param info
 */
public void setResult(ITypeInfo info) {
	result = info;
}

/**
 * @return
 */
public boolean isFailedToDereference() {
	return failedToDereference;
}

/**
 * @param b
 */
public void setFailedToDereference(boolean b) {
	failedToDereference = b;
}

}
