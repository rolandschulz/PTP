package org.eclipse.photran.cdtinterface.core;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.photran.cdtinterface.core.pdom.PDOMFortranLinkage;
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
				public PDOMLinkage getLinkage(PDOMDatabase pdom, int record)
				{
					return new PDOMFortranLinkage(pdom, record);
				}

				public PDOMLinkage createLinkage(PDOMDatabase pdom) throws CoreException
				{
					return new PDOMFortranLinkage(pdom);
				}
			
			};
		else
			return super.getAdapter(adapter);
	}
	
	public IASTTranslationUnit getTranslationUnit(IStorage file, IProject project, int style)
	{
		System.out.println("getTranslationUnit 1");
		
		return getTranslationUnit(file.getFullPath().toOSString(), project, style);
	}
	
	public IASTTranslationUnit getTranslationUnit(IFile file, int style)
	{
		System.out.println("getTranslationUnit 2");
		
		return getTranslationUnit(file.getLocation().toOSString(), file.getProject(), style);
	}
	
	public IASTTranslationUnit getTranslationUnit(IWorkingCopy workingCopy, int style)
	{
		System.out.println("getTranslationUnit 3");
		
		IFile file = (IFile)workingCopy.getResource();
		String path = file.getLocation().toOSString();
		return getTranslationUnit(path, file.getProject(), style);
        // If workingCopy.getContents() returned a String rather than a char[], it could be useful to us
	}

	protected IASTTranslationUnit getTranslationUnit(String path, IProject project, int style)
	{
		IPDOM pdom = PDOM.getPDOM(project);
        
        // Parse the file
		IASTTranslationUnit ast = new FortranIASTTranslationUnitAdapter(path);

        // Tell the translation unit it's been indexed and where to find its index (PDOM)
		if ((style & AST_USE_INDEX) != 0) ast.setIndex(pdom);

		return ast;
	}
	
	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy, int offset)
	{
		System.out.println("getCompletionNode");
		
		return null;
	}
}
