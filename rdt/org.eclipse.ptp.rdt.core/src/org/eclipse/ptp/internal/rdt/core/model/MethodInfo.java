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

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class MethodInfo extends FunctionInfo {
	private static final long serialVersionUID = 1L;

	public MethodInfo(CElement parent) {
		super(parent);
	}

	public void setVirtual(boolean virtual) {
		((MethodDeclaration) fParent).setVirtual(virtual);
	}

	public void setInline(boolean isInline) {
		((MethodDeclaration) fParent).setInline(isInline);
	}

	public void setFriend(boolean isFriend) {
		((MethodDeclaration) fParent).setFriend(isFriend);
	}

	public void setVolatile(boolean isVolatile) {
		((MethodDeclaration) fParent).setVolatile(isVolatile);
	}

	public void setVisibility(ASTAccessVisibility visibility) {
		((MethodDeclaration) fParent).setVisibility(visibility);
	}

	public void setPureVirtual(boolean isPureVirtual) {
		((MethodDeclaration) fParent).setPureVirtual(isPureVirtual);
	}

}
