/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.model;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * A Fortran model builder that produces an empty model.
 * 
 * @author Jeff Overbey
 */
public class EmptyFortranModelBuilder implements IFortranModelBuilder
{
    public void parse(boolean quickParseMode) throws Exception
    {
    }

    public void setTranslationUnit(ITranslationUnit tu)
    {
    }

    public void setIsFixedForm(boolean isFixedForm)
    {
    }
}
