/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.io.Serializable;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.model.SourceRange;

public class DummyName implements IIndexName, Serializable {
	private static final long serialVersionUID = 1L;
	
	SourceRange fRange;
	boolean fIsDeclaration;
	boolean fIsDefinition;
	String fName;

	IIndexFile fFile;
	
	public DummyName(String name, IIndexFileLocation location, ISourceRange range, boolean isDefinition, boolean isDeclaration) {
		fName = name;
		fFile = new DummyFile(location);
		fRange = new SourceRange(range);
	}
	
	public DummyName(ISourceReference element) throws CModelException {
		fName = ((ICElement) element).getElementName();
		fFile = new DummyFile(null);
		fRange = new SourceRange(element.getSourceRange());
		switch (((ICElement) element).getElementType()) {
		case ICElement.C_CLASS:
		case ICElement.C_ENUMERATION:
		case ICElement.C_ENUMERATOR:
		case ICElement.C_FIELD:
		case ICElement.C_FUNCTION:
		case ICElement.C_INCLUDE:
		case ICElement.C_MACRO:
		case ICElement.C_METHOD:
		case ICElement.C_NAMESPACE:
		case ICElement.C_STRUCT:
		case ICElement.C_TEMPLATE_CLASS:
		case ICElement.C_TEMPLATE_FUNCTION:
		case ICElement.C_TEMPLATE_METHOD:
		case ICElement.C_TEMPLATE_STRUCT:
		case ICElement.C_TEMPLATE_UNION:
		case ICElement.C_TEMPLATE_VARIABLE:
		case ICElement.C_UNION:
		case ICElement.C_VARIABLE:
			fIsDefinition = true;
		case ICElement.C_CLASS_DECLARATION:
		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:
		case ICElement.C_UNION_DECLARATION:
		case ICElement.C_VARIABLE_DECLARATION:
			// Assume definitions are declarations too.
			fIsDeclaration = true;
		}
	}
	
	public DummyName(IASTName name, IASTFileLocation location, IIndexFileLocation fileLocation) {
		this(name.toString(), fileLocation, new SourceRange(name, location), name.isDefinition(), name.isDeclaration());
	}
	
	public DummyName(IIndexName name, IASTFileLocation location, IIndexFileLocation fileLocation) {
		this(name.toString(), fileLocation, new SourceRange(name, location), name.isDefinition(), name.isDeclaration());
	}

	public IIndexName[] getEnclosedNames() throws CoreException {
		return null;
	}

	public IIndexName getEnclosingDefinition() throws CoreException {
		return null;
	}

	public IIndexFile getFile() throws CoreException {
		return fFile;
	}

	public int getNodeLength() {
		return fRange.getIdLength();
	}

	public int getNodeOffset() {
		return fRange.getIdStartPos();
	}

	public boolean isBaseSpecifier() throws CoreException {
		return false;
	}

	public IASTFileLocation getFileLocation() {
		return new DummyFileLocation(fRange);
	}

	public boolean isDeclaration() {
		return fIsDeclaration;
	}

	public boolean isDefinition() {
		return fIsDefinition;
	}

	public boolean isReference() {
		return false;
	}

	public char[] toCharArray() {
		return fName.toCharArray();
	}

	public boolean couldBePolymorphicMethodCall() throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReadAccess() throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isWriteAccess() throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString() {
		return fName;
	}
}
