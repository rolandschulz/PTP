/*******************************************************************************
 * Copyright (c) 2007-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.indexer.ILanguageMapper;

/**
 * @author crecoskie
 *
 */
public class RemoteLanguageMapper implements ILanguageMapper {
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.indexer.ILanguageMapper#getLanguage(java.lang.String)
	 */
	public ILanguage getLanguage(String file) {
		String extension = getFileExtension(file);
		
		if(extension.equals(".cpp") || extension.equals(".C") || extension.equals(".cxx") || extension.equals(".cc")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return new GPPLanguage();
		
		else if(extension.equals(".c")) //$NON-NLS-1$
			return new GCCLanguage();
		
		else if(extension.equals(".h") || extension.equals(".hpp") || extension.equals(".hxx") || extension.equals(".hbh")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return new GPPLanguage();
		
		else
			return null;
	}
	
	private String getFileExtension(String file) {
		int index = file.lastIndexOf("."); //$NON-NLS-1$
		
		return file.substring(index);
	}

}
