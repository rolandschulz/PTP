/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchMatch
 * Version: 1.9
 */
package org.eclipse.ptp.internal.rdt.core.search;

import java.io.Serializable;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.browser.IndexModelUtil;

public class RemoteSearchMatch implements Serializable {
	private static final long serialVersionUID = 1L;

	private ITypeInfo fTypeInfo;
	private IIndexName fName;
	private int fOffset;
	private int fLength;
	
	private boolean fIsPolymorphicCall;


	public RemoteSearchMatch(IIndex index, IIndexBinding binding, IIndexName name, int nodeOffset, int nodeLength) {
		try {
			int elementType = IndexModelUtil.getElementType(binding);
			String[] qualifiedNameParts;
			if (binding instanceof ICPPBinding) {
				qualifiedNameParts = ((ICPPBinding)binding).getQualifiedName();
			} 
			else if (binding instanceof IField) {
				IField field= (IField) binding;
				ICompositeType owner= field.getCompositeTypeOwner();
				qualifiedNameParts = new String[] {owner.getName(), field.getName()};	
			}
			else {
				qualifiedNameParts = new String[] {binding.getName()};
			}
			QualifiedTypeName qualifiedName = new QualifiedTypeName(qualifiedNameParts);
			fTypeInfo = new RemoteTypeInfo(null, elementType, qualifiedName);
		} catch (DOMException e) {
			// TODO: We shouldn't get here but it could happen...
		}
		fName = name;
		fOffset = nodeOffset;
		fLength = nodeLength;
	}

	public RemoteSearchMatch(IIndexName name, ITypeInfo typeInfo, int offset, int length) {
		fName = name;
		fTypeInfo = new RemoteTypeInfo(null, typeInfo.getCElementType(), typeInfo.getQualifiedTypeName());
		fOffset = offset;
		fLength = length;
	}

	public IIndexName getName() {
		return fName;
	}

	public ITypeInfo getTypeInfo() {
		return fTypeInfo;
	}

	public int getOffset() {
		return fOffset;
	}

	public int getLength() {
		return fLength;
	}
	
	public void setIsPolymorphicCall() {
		fIsPolymorphicCall= true;
	}
	
	public boolean isPolymorphicCall() {
		return fIsPolymorphicCall;
	}

	
}
