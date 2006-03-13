package org.eclipse.photran.core;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.addl_langs.IContributedModelBuilder;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.photran.internal.core.model.FortranModelBuilder;

/**
 * CDT extension language for Fortran
 * 
 * @author joverbey
 */
public class FortranLanguage implements ILanguage
{
//    public String getName()
//    {
//        return "Fortran";
//    }
//
    public Collection getRegisteredContentTypeIds()
    {
        return Arrays.asList(new String[]{FortranCorePlugin.FIXED_FORM_CONTENT_TYPE, FortranCorePlugin.FREE_FORM_CONTENT_TYPE});
    }
//
//    public IModelBuilder createModelBuilder(TranslationUnit tu, Map newElements)
//    {
//        return new FortranModelBuilder(tu);
//    }

	public String getId()
	{
		return FortranCorePlugin.LANGUAGE_ID;
	}

	public IASTTranslationUnit getTranslationUnit(IFile file, int style)
	{
		return null;
	}

	public IASTTranslationUnit getTranslationUnit(IStorage file, IProject project, int style)
	{
		return null;
	}

	public IASTTranslationUnit getTranslationUnit(IWorkingCopy workingCopy, int style)
	{
		return null;
	}

	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy, int offset)
	{
		return null;
	}

	public Object getAdapter(Class adapter)
	{
		return null;
	}

	public IContributedModelBuilder createModelBuilder(TranslationUnit tu) {
		return new FortranModelBuilder(tu);
	}
}
