/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class FieldInfo extends SourceManipulationInfo {
	private static final long serialVersionUID = 1L;

	public FieldInfo(CElement parent) {
		super(parent);
	}

	public void setVisibility(ASTAccessVisibility currentVisibility) {
		((Field) fParent).setVisibility(currentVisibility);
	}

}
