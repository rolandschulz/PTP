package org.eclipse.photran.internal.core.model;

import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;

public interface IFortranModelBuilder extends IContributedModelBuilder
{
    void setTranslationUnit(ITranslationUnit tu);
    void setIsFixedForm(boolean isFixedForm);
}
