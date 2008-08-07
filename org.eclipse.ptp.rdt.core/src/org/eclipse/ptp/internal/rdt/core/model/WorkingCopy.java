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

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;

@SuppressWarnings("unchecked")
public class WorkingCopy extends TranslationUnit implements IRemoteWorkingCopy {
	private static final long serialVersionUID = 1L;
	
	String fContents;

	public WorkingCopy(Parent parent, ITranslationUnit unit, String contents) {
		super(parent, unit);
		fContents = contents;
	}
	
	public String getText() {
		return fContents;
	}
	
	@Override
	public char[] getContents() {
		return fContents.toCharArray();
	}
	
	@Override
	public CodeReader getCodeReader() {
		return new CodeReader(getContents());
	}
}
