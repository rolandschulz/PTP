package org.eclipse.photran.cdtinterface.core;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.model.FortranModelBuilder;

/**
 * CDT extension language for Fortran
 * 
 * @author joverbey
 */
public class FortranLanguage extends PlatformObject implements ILanguage
{
	public static final String LANGUAGE_ID = "org.eclipse.photran.cdtinterface.fortran";
    
    public Collection getRegisteredContentTypeIds()
    {
        return Arrays.asList(new String[]{FortranCorePlugin.FIXED_FORM_CONTENT_TYPE, FortranCorePlugin.FREE_FORM_CONTENT_TYPE});
    }

	public String getId()
	{
		return LANGUAGE_ID;
	}

	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu)
	{
		return new FortranModelBuilder(tu);
	}

	public Object getAdapter(Class adapter)
	{
		System.out.println("getAdapter " + adapter.getName());
		
        if (adapter == IPDOMLinkageFactory.class)
			return new IPDOMLinkageFactory()
			{
				public PDOMLinkage getLinkage(PDOM pdom, int record)
				{
					return null; //return new PDOMFortranLinkage(pdom, record);
				}

				public PDOMLinkage createLinkage(PDOM pdom) throws CoreException
				{
					return null; //return new PDOMFortranLinkage(pdom);
				}
			};
		else
			return super.getAdapter(adapter);
	}

    public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, ICodeReaderFactory codeReaderFactory, int style)
    {
        return getASTTranslationUnit(file, style);
    }
    
    public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, int style)
    {
        System.out.println("getASTTranslationUnit");
        
//        //IResource resource = file.getResource();
//        ICProject project = file.getCProject();
//        //IProject rproject = project.getProject();
//        
//        PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project).getAdapter(PDOM.class);
//        String path;
//        if (file instanceof IWorkingCopy)
//        {
//            IFile rfile = (IFile)file.getResource();
//            path = rfile.getLocation().toOSString();
//            // Maybe later we can get the working copy contents using file.getContents()
//        }
//        else
//        {
//            path = file.getPath().toOSString();
//        }
//        
//        // Parse
//        IASTTranslationUnit ast = new FortranIASTTranslationUnitAdapter(path);
//
//        if ((style & AST_USE_INDEX) != 0) 
//            ast.setIndex(pdom);
//
//        return ast;
        return null;
    }
	
	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy, int offset)
	{
		System.out.println("getCompletionNode");
		
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
}
