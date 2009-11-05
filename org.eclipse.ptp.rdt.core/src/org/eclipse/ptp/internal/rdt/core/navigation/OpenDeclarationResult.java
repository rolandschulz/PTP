/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.navigation;

import java.io.Serializable;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.model.ICElement;

/**
 * The result of the open declaration action from the remote index.
 * Use static factory methods to instantiate.
 *
 * @author Mike Kucera
 */
public class OpenDeclarationResult implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum ResultType { 
		/** The result is an array of IName */
		RESULT_NAMES, 
		/** The result is an array of ICElement */
		RESULT_C_ELEMENTS,
		/** */
		RESULT_INCLUDE_PATH,
		
		/** Could not find the symbol */
		FAILURE_SYMBOL_LOOKUP,
		/** Could not find the include */
		FAILURE_INCLUDE_LOOKUP,
		/** There was an unexpected error */
		FAILURE_UNEXPECTED_ERROR
	};
	
	
	private final Object result;
	private final ResultType resultType;
	
	
	// static factory methods
	
	public static OpenDeclarationResult resultNames(IName[] names) {
		return new OpenDeclarationResult(ResultType.RESULT_NAMES, names);
	}
	
	public static OpenDeclarationResult resultCElements(ICElement[] elements) {
		return new OpenDeclarationResult(ResultType.RESULT_C_ELEMENTS, elements);
	}
	
	public static OpenDeclarationResult resultIncludePath(String path) {
		return new OpenDeclarationResult(ResultType.RESULT_INCLUDE_PATH, path);
	}
	
	public static OpenDeclarationResult failureSymbolLookup(String symbolName) {
		return new OpenDeclarationResult(ResultType.FAILURE_SYMBOL_LOOKUP, symbolName); 
	}
	
	public static OpenDeclarationResult failureIncludeLookup(String path) {
		return new OpenDeclarationResult(ResultType.FAILURE_INCLUDE_LOOKUP, path); 
	}
	
	public static OpenDeclarationResult failureUnexpectedError() {
		return new OpenDeclarationResult(ResultType.FAILURE_UNEXPECTED_ERROR);
	}
	
	/**
	 * Constructor is private, use factory methods to instantiate.
	 */
	private OpenDeclarationResult(ResultType resultType, Object result) {
		if(result == null)
			throw new NullPointerException();
		this.result = result;
		this.resultType = resultType;
	}
	

	private OpenDeclarationResult(ResultType resultType) {
		this.result = null;
		this.resultType = resultType;
	}
	
	
	public ResultType getResultType() {
		return resultType;
	}
	
	public Object getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		return resultType + ": " + String.valueOf(result); //$NON-NLS-1$
	}
}
