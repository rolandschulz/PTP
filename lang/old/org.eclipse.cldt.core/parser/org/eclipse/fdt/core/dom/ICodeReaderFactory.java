/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.core.dom;

import org.eclipse.fdt.core.parser.CodeReader;

/**
 * @author jcamelon
 */
public interface ICodeReaderFactory {
    
    public int getUniqueIdentifier();
    public CodeReader createCodeReaderForTranslationUnit( String path );
    public CodeReader createCodeReaderForInclusion( String path );
}
