package org.eclipse.photran.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.core.addl_langs.IAdditionalLanguage;
import org.eclipse.cdt.core.addl_langs.IModelBuilder;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.photran.internal.core.model.FortranModelBuilder;

/**
 * CDT extension language for Fortran
 * 
 * @author joverbey
 */
public class FortranLanguage implements IAdditionalLanguage
{
    public String getName()
    {
        return "Fortran";
    }

    public Collection getRegisteredContentTypeIds()
    {
        ArrayList/* <String> */contentTypes = new ArrayList(1);

        contentTypes.add("org.eclipse.photran.core.fortranSource");
        // contentTypes.add("org.eclipse.photran.core.fortranHeader");

        return contentTypes;
    }

    public IModelBuilder createModelBuilder(TranslationUnit tu, Map newElements)
    {
        return new FortranModelBuilder(tu);
    }

}
