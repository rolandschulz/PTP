/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.core;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.photran.cdtinterface.dom.IFortranDOMParser;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.model.IFortranModelBuilder;
import org.eclipse.photran.internal.core.model.SimpleFortranModelBuilder;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * CDT extension language for Fortran
 * 
 * @author Jeff Overbey
 */
public class FortranLanguage extends AbstractLanguage
{
    public static final String LANGUAGE_ID = "org.eclipse.photran.cdtinterface.fortran";

    public static final String FORTRAN_MODEL_BUILDER_EXTENSION_POINT_ID = "org.eclipse.photran.cdtinterface.modelbuilder";
    public static final String FORTRAN_DOM_PARSER_EXTENSION_POINT_ID = "org.eclipse.photran.cdtinterface.domparser";

	public String getId()
	{
		return LANGUAGE_ID;
	}

	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu)
	{
	    IFortranModelBuilder modelBuilder = null;
	    
	    IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(FORTRAN_MODEL_BUILDER_EXTENSION_POINT_ID);
	    if (configs.length > 0)
	    {
	        int index = findPreferredModelBuilder(configs);
	        try { modelBuilder = (IFortranModelBuilder)configs[index].createExecutableExtension("class"); }
	        catch (CoreException e) {;}
	    }
	        
	    if (modelBuilder == null) modelBuilder = new SimpleFortranModelBuilder();
	    
	    modelBuilder.setTranslationUnit(tu);
	    modelBuilder.setIsFixedForm(tu.getContentTypeId().equals(FortranCorePlugin.FIXED_FORM_CONTENT_TYPE));
	    return modelBuilder;
	}

    private int findPreferredModelBuilder(IConfigurationElement[] configs)
    {
        for (int i = 0; i < configs.length; i++)
            if (configs[i].getAttribute("id").equals(FortranPreferences.PREFERRED_MODEL_BUILDER.getValue()))
                return i;
        return 0;
    }

    public IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException
	{
        IFortranDOMParser domParser = getDOMParser();
        if (domParser == null)
            return null;
        else
            return domParser.getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
	}

	public IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset)
			throws CoreException
	{
        IFortranDOMParser domParser = getDOMParser();
        if (domParser == null)
            return null;
        else
            return domParser.getCompletionNode(reader, scanInfo, fileCreator, index, log, offset);
	}
	
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length)
	{
        IFortranDOMParser domParser = getDOMParser();
        if (domParser == null)
            return new IASTName[0];
        else
            return domParser.getSelectedNames(ast, start, length);
	}

    private IFortranDOMParser getDOMParser()
    {
        IFortranDOMParser modelBuilder = null;
        
        IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(FORTRAN_DOM_PARSER_EXTENSION_POINT_ID);
        if (configs.length > 0)
        {
            int index = findPreferredDOMParser(configs);
            try { modelBuilder = (IFortranDOMParser)configs[index].createExecutableExtension("class"); }
            catch (CoreException e) {;}
        }
        return modelBuilder;
    }

    private int findPreferredDOMParser(IConfigurationElement[] configs)
    {
        for (int i = 0; i < configs.length; i++)
            if (configs[i].getAttribute("id").equals(FortranPreferences.PREFERRED_DOM_PARSER.getValue()))
                return i;
        return 0;
    }

	// JO - This is not required as of CDT 4.0, but it is used by the Fortran dependency calculator, so I'm leaving it in...
    public Collection getRegisteredContentTypeIds()
    {
        return Arrays.asList(new String[] { FortranCorePlugin.FIXED_FORM_CONTENT_TYPE, FortranCorePlugin.FREE_FORM_CONTENT_TYPE });
    }

//	public Object getAdapter(Class adapter)
//	{
//		//System.out.println("getAdapter " + adapter.getName());
//		
//        if (adapter == IPDOMLinkageFactory.class)
//			return new IPDOMLinkageFactory()
//			{
//				public PDOMLinkage getLinkage(PDOM pdom, int record)
//				{
//					return null; //return new PDOMFortranLinkage(pdom, record);
//				}
//
//				public PDOMLinkage createLinkage(PDOM pdom) throws CoreException
//				{
//					return null; //return new PDOMFortranLinkage(pdom);
//				}
//			};
//		else
//			return super.getAdapter(adapter);
//	}

	public int getLinkageID()
	{
		return ILinkage.FORTRAN_LINKAGE_ID;
	}
}
