/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.internal.core.parser.ast.complete;

import org.eclipse.cldt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cldt.core.parser.ISourceElementRequestor;
import org.eclipse.cldt.core.parser.ast.IASTField;
import org.eclipse.cldt.core.parser.ast.IASTFieldReference;
import org.eclipse.cldt.core.parser.ast.IASTReference;


public class ASTFieldReference extends ASTReference
		implements
			IASTReference,
			IASTFieldReference {
	private IASTField referencedElement;
	/**
	 * @param offset
	 * @param field
	 */
	public ASTFieldReference(int offset, IASTField field) {
		super(offset);
		referencedElement = field;
	}
	/**
	 * 
	 */
	public ASTFieldReference() {
		super(0);
		referencedElement = null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
	 */
	public ISourceElementCallbackDelegate getReferencedElement() {
		return referencedElement;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		try {
			requestor.acceptFieldReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate re) {
		initialize(o);
		this.referencedElement = (IASTField) re;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
	 */
	public void reset() {
		resetOffset();
		this.referencedElement = null;
	}
}
