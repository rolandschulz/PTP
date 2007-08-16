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
    void setTranslationUnit(ITranslationUnit tu);
    void setIsFixedForm(boolean isFixedForm);
}
