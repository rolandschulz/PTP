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
package org.eclipse.cldt.core.dom;
import org.eclipse.cldt.core.browser.IWorkingCopyProvider;
import org.eclipse.cldt.core.dom.ICodeReaderFactory;
import org.eclipse.cldt.core.dom.IParserConfiguration;
import org.eclipse.cldt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cldt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cldt.internal.core.dom.InternalASTServiceProvider;
import org.eclipse.cldt.internal.core.dom.PartialWorkingCopyCodeReaderFactory;
import org.eclipse.cldt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.cldt.internal.core.dom.WorkingCopyCodeReaderFactory;
import org.eclipse.core.resources.IFile;

/**
 * @author jcamelon
 * 
 * This class serves as the manager of the AST/DOM mechanisms for the CDT.
 * It should be eventually added to FortranCorePlugin for startup.  
 */
public class CDOM implements IASTServiceProvider {
    
    private CDOM() 
    {
    }
    
    private static CDOM instance = new CDOM();
    public static CDOM getInstance()
    {
        return instance;
    }
    
    private IASTServiceProvider defaultService = new InternalASTServiceProvider();    

    
    public IASTServiceProvider getASTService() {
        //CDOM itself is not so much "the" AST service as it acts as a proxy 
        //to different AST services
        //Should we see the need to provide an extension point for this
        //rather than purely proxying the calls to IASTServiceProvider#*
        //we would have to do some discovery and co-ordination on behalf of the 
        //client
        return this;
    }
    

    public static final int PARSE_SAVED_RESOURCES = 0; 
    public static final int PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS = 1;
    public static final int PARSE_WORKING_COPY_WHENEVER_POSSIBLE = 2;
    private IWorkingCopyProvider provider;
    
    public ICodeReaderFactory getCodeReaderFactory( int key )
    {
        //TODO - eventually these factories will need to hook into the 
        //CodeReader caches
        switch( key )
        {
        	case PARSE_SAVED_RESOURCES: 
        	    return SavedCodeReaderFactory.getInstance();
        	case PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS:
        	    return new PartialWorkingCopyCodeReaderFactory( provider );
        	case PARSE_WORKING_COPY_WHENEVER_POSSIBLE:
        	    return new WorkingCopyCodeReaderFactory( provider );
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.core.resources.IFile)
     */
    public IASTTranslationUnit getTranslationUnit(IFile fileToParse) throws UnsupportedDialectException {
        return defaultService.getTranslationUnit(fileToParse);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.core.resources.IFile, org.eclipse.cdt.core.dom.ICodeReaderFactory)
     */
    public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
        return defaultService.getTranslationUnit(fileToParse, fileCreator );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.core.resources.IFile, org.eclipse.cdt.core.dom.ICodeReaderFactory, org.eclipse.cdt.core.dom.IParserConfiguration)
     */
    public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator, IParserConfiguration configuration) throws UnsupportedDialectException {
        return defaultService.getTranslationUnit(fileToParse, fileCreator, configuration );
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getCompletionNode(org.eclipse.core.resources.IFile, int, org.eclipse.cdt.core.dom.ICodeReaderFactory)
	 */
	public ASTCompletionNode getCompletionNode(IFile fileToParse, int offset,
			ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
		return defaultService.getCompletionNode(fileToParse, offset, fileCreator);
	}

    /**
     * @param workingCopyProvider
     */
    public void setWorkingCopyProvider(IWorkingCopyProvider workingCopyProvider) {
        this.provider = workingCopyProvider;
    }

}
