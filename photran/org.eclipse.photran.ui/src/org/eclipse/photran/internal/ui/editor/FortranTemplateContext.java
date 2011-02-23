/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * Context for all Fortran templates.
 * 
 * @author Jeff Overbey
 */
public class FortranTemplateContext extends TemplateContextType
{
    public static final String ID = "org.eclipse.photran.ui.template.context"; //$NON-NLS-1$
    
    public FortranTemplateContext()
    {
        // Determines what variables will be available in templates
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.User());
        addResolver(new GlobalTemplateVariables.Dollar());
    }
}