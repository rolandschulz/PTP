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
package org.eclipse.fdt.internal.core.dom;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.fdt.core.browser.IWorkingCopyProvider;
import org.eclipse.fdt.core.dom.CDOM;
import org.eclipse.fdt.core.dom.ICodeReaderFactory;
import org.eclipse.fdt.core.parser.CodeReader;
import org.eclipse.fdt.core.parser.ParserUtil;
import org.eclipse.fdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class PartialWorkingCopyCodeReaderFactory 
        implements ICodeReaderFactory {

    private final IWorkingCopyProvider provider;

    /**
     * @param provider
     */
    public PartialWorkingCopyCodeReaderFactory(IWorkingCopyProvider provider) {
        this.provider = provider;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
     */
    public int getUniqueIdentifier() {
        return CDOM.PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
     */
    public CodeReader createCodeReaderForTranslationUnit(String path) {
        return ParserUtil.createReader( path, createWorkingCopyIterator() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    public CodeReader createCodeReaderForInclusion(String path) {
        return ParserUtil.createReader( path, EmptyIterator.EMPTY_ITERATOR );
    }

    /**
     * @return
     */
    protected Iterator createWorkingCopyIterator() {
        if( provider == null ) return EmptyIterator.EMPTY_ITERATOR;
        return Arrays.asList( provider.getWorkingCopies() ).iterator();
    }

}
