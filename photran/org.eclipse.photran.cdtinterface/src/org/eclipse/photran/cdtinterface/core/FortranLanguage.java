package org.eclipse.photran.cdtinterface.core;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
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
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.model.FortranModelBuilder;

/**
 * CDT extension language for Fortran
 * 
 * @author joverbey
 */
public class FortranLanguage extends AbstractLanguage
{
	public static final String LANGUAGE_ID = "org.eclipse.photran.cdtinterface.fortran";

	public String getId()
	{
		return LANGUAGE_ID;
	}

	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu)
	{
		return new FortranModelBuilder(tu, tu.getContentTypeId().equals(FortranCorePlugin.FIXED_FORM_CONTENT_TYPE));
	}

	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException
	{
		return null;
	}

	public IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset)
			throws CoreException
	{
		return null;
	}
	
	/**
	 * Gather the list of IASTNames that appear the selection with the given start offset
	 * and length in the given ITranslationUnit.
	 * 
	 * @param tu
	 * @param start
	 * @param length
	 * @param style
	 * @return
	 */
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length)
	{
		// TODO This needs to be implemented.  I just added an empty stub to satisfy the interface (C.E.Rasmussen)
		return new IASTName[0];
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
}
