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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.ptp.rdt.core.RDTLog;

public class Field extends VariableDeclaration implements IField {
	private static final long serialVersionUID = 1L;
	
	protected boolean fIsMutable;
	protected ASTAccessVisibility fVisibility;
	
	public Field(Parent parent, String variableName) {
		super(parent, ICElement.C_FIELD, variableName);
		fIsMutable = true;
	}

	public Field(Parent parent, IField element) throws CModelException {
		super(parent, element);
		fIsMutable = element.isMutable();
		fVisibility = element.getVisibility();
	}

	public Field(Parent parent, org.eclipse.cdt.core.dom.ast.IField binding) throws DOMException {
		super(parent, ICElement.C_FIELD, binding);
		if (binding instanceof ICPPMember) {
			ICPPMember member= (ICPPMember) binding;
			switch (member.getVisibility()) {
			case ICPPMember.v_private:
				fVisibility = ASTAccessVisibility.PRIVATE;
				break;
			case ICPPMember.v_protected:
				fVisibility = ASTAccessVisibility.PROTECTED;
				break;
			case ICPPMember.v_public:
				fVisibility = ASTAccessVisibility.PUBLIC;
				break;
			}
		}
	}

	public boolean isMutable() throws CModelException {
		return fIsMutable;
	}

	public ASTAccessVisibility getVisibility() throws CModelException {
		return fVisibility;
	}

	public void setMutable(boolean mutable) {
		fIsMutable = mutable;
	}
	
	@Override
	public CElementInfo getElementInfo() {
		if (fInfo == null) {
			fInfo = new FieldInfo(this);
		}
		return fInfo;
	}

	public void setVisibility(ASTAccessVisibility currentVisibility) {
		fVisibility = currentVisibility;
	}

}
