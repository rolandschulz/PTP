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
package org.eclipse.cldt.internal.core.dom;

import org.eclipse.cldt.core.browser.IWorkingCopyProvider;
import org.eclipse.cldt.core.dom.CDOM;
import org.eclipse.cldt.core.dom.ICodeReaderFactory;
import org.eclipse.cldt.core.parser.CodeReader;
import org.eclipse.cldt.core.parser.ParserUtil;

/**
 * @author jcamelon
 */
public class WorkingCopyCodeReaderFactory extends
        PartialWorkingCopyCodeReaderFactory implements ICodeReaderFactory {

    /**
     * @param provider
     */
    public WorkingCopyCodeReaderFactory(IWorkingCopyProvider provider) {
        super(provider);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
     */
    public int getUniqueIdentifier() {
        return CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    public CodeReader createCodeReaderForInclusion(String path) {
        return ParserUtil.createReader(path, createWorkingCopyIterator());
    }

}
