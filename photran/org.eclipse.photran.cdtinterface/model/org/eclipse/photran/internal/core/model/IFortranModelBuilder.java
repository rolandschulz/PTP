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
package org.eclipse.photran.internal.core.model;

import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * This interface must be implemented by any Fortran model builder.
 * <p>
 * See the org.eclipse.photran.cdtinterface.modelbuilder extension point.
 * 
 * @author Jeff Overbey
 */
public interface IFortranModelBuilder extends IContributedModelBuilder
{
    /**
     * Tells the model builder which translation unit to parse when {@link #parse(boolean)} is
     * invoked.
     * <p>
     * This method will always be called before {@link #parse(boolean)}. 
     */
    void setTranslationUnit(ITranslationUnit tu);
    
    /**
     * Tells the model builder to ignore the platform content type of a file and, instead, parse it
     * as either free or fixed form according to the <code>isFixedForm</code> argument.
     */
    void setIsFixedForm(boolean isFixedForm);
}
