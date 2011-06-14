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
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IUsing;

public class Using extends SourceManipulation implements IUsing {
	private static final long serialVersionUID = 1L;

	protected boolean fIsDirective;
	
	public Using(Parent parent, String qualifiedName, boolean isDirective) {
		super(parent, ICElement.C_USING, qualifiedName);
		fIsDirective = isDirective;
	}
	
	public Using(Parent parent, IUsing element) throws CModelException {
		super(parent, element, (ISourceReference) element);
	}

	public boolean isDirective() {
		return fIsDirective;
	}
}
