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

import java.io.IOException;
import java.net.URI;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;

@SuppressWarnings("unchecked")
public class WorkingCopy extends TranslationUnit implements IRemoteWorkingCopy {
	private static final long serialVersionUID = 1L;
	
	private String fContents;

	public WorkingCopy(Parent parent, ITranslationUnit unit, String contents) {
		super(parent, unit);
		fContents = contents;
	}
	
	public WorkingCopy(Parent parent, ITranslationUnit unit, char[] contents) {
		this(parent, unit, new String(contents));
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
		
		URI uri = null;
		
		if(fManagedLocation != null)
			uri = fManagedLocation;
		else
			uri = fLocation;
		
		if(uri == null)
			return null;
		
		String filePath = fRemotePath != null ? fRemotePath : uri.getPath();
		
		return new CodeReader(filePath, getContents());
		
	}
	
	@Override
	public boolean isWorkingCopy() {
		return true;
	}
}
