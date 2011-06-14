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
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ISourceReference;

public class Macro extends SourceManipulation implements IMacro {
	private static final long serialVersionUID = 1L;
	private boolean fFunctionStyle = false;

	public Macro(Parent parent, String simpleName) {
		super(parent, ICElement.C_MACRO, simpleName);
	}

	public Macro(Parent parent, IMacro element) throws CModelException {
		super(parent, element, (ISourceReference) element);
		setFunctionStyle(element.isFunctionStyle());
	}

	public String getIdentifierList() {
		return null;
	}

	public String getTokenSequence() {
		return null;
	}

	public boolean isFunctionStyle() {
		return fFunctionStyle;
	}
	
	public void setFunctionStyle(boolean isFunctionStyle) {
		this.fFunctionStyle = isFunctionStyle;
	}
}
