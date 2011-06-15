/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.ISourceReference;

public class Enumerator extends SourceManipulation implements IEnumerator {
	private static final long serialVersionUID = 1L;
	
	protected String fConstantExpression;
	
	public Enumerator(Parent enumerator, String simpleName) {
		super(enumerator, ICElement.C_ENUMERATOR, simpleName);
	}

	public Enumerator(Parent parent, IEnumerator element) throws CModelException {
		super(parent, element, (ISourceReference) element);
		fConstantExpression = element.getConstantExpression();
	}

	public Enumerator(Parent parent, org.eclipse.cdt.core.dom.ast.IEnumerator binding) {
		super(parent, ICElement.C_ENUMERATOR, binding.getName());
	}

	public String getConstantExpression() {
		return fConstantExpression;
	}

	public void setConstantExpression(String expressionString) {
		fConstantExpression = expressionString;		
	}

}
