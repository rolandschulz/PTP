/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.ptp.internal.rdt.core.index.DummyFileLocation;

public class SimpleName implements IName, Serializable {

	private static final long serialVersionUID = 1L;
	
	private IASTFileLocation fileLocation;
	private boolean isDeclaration;
	private boolean isDefinition;
	private boolean isReference;
	private char[] name;
	
	
	public SimpleName(IASTFileLocation location, char[] name) {
		setFileLocation(location);
		setName(name);
	}
	
	public SimpleName(IASTFileLocation location, String name) {
		this(location, name.toCharArray());
	}
	
	public SimpleName(IName name) {
		this(name.getFileLocation(), name.toCharArray());
	}

	
	public IASTFileLocation getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(IASTFileLocation fileLocation) {
		this.fileLocation = new DummyFileLocation(fileLocation);
	}

	public boolean isDeclaration() {
		return isDeclaration;
	}

	public void setDeclaration(boolean isDeclaration) {
		this.isDeclaration = isDeclaration;
	}

	public boolean isDefinition() {
		return isDefinition;
	}

	public void setDefinition(boolean isDefinition) {
		this.isDefinition = isDefinition;
	}

	public boolean isReference() {
		return isReference;
	}

	public void setReference(boolean isReference) {
		this.isReference = isReference;
	}

	public char[] getName() {
		return name;
	}

	public void setName(char[] name) {
		this.name = name;
	}
	
	
	public char[] toCharArray() {
		return getName();
	}
	
	@Override
	public String toString() {
		return new String(name);
	}

	public char[] getSimpleID() {
		return name;
	}
}
