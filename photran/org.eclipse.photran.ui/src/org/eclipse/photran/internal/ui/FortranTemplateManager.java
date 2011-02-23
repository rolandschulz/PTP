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
package org.eclipse.photran.internal.ui;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.photran.internal.ui.editor.FortranTemplateContext;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;

/**
 * Provides access to the {@link ContextTypeRegistry}, {@link TemplateStore}, etc. for Fortran code templates.
 * 
 * @author Jeff Overbey
 */
public final class FortranTemplateManager
{
    private static final String TEMPLATE_KEY = FortranUIPlugin.PLUGIN_ID + ".templates"; //$NON-NLS-1$

    private static FortranTemplateManager instance;

    public static FortranTemplateManager getInstance()
    {
        if (instance == null)
            instance = new FortranTemplateManager();
        return instance;
    }

    private TemplateStore templateStore;
    private ContributionContextTypeRegistry contextTypeRegistry;

    public TemplateStore getTemplateStore()
    {
        if (templateStore == null)
        {
            templateStore = new ContributionTemplateStore(
                getContextTypeRegistry(),
                FortranUIPlugin.getDefault().getPreferenceStore(),
                TEMPLATE_KEY);
            try
            {
                templateStore.load();
            }
            catch (IOException e)
            {
                FortranUIPlugin.log(e);
            }
        }
        return templateStore;
    }

    public ContextTypeRegistry getContextTypeRegistry()
    {
        if (contextTypeRegistry == null)
        {
            contextTypeRegistry = new ContributionContextTypeRegistry();
            contextTypeRegistry.addContextType(FortranTemplateContext.ID);
        }
        return contextTypeRegistry;
    }

    public IPreferenceStore getPreferenceStore()
    {
        return FortranUIPlugin.getDefault().getPreferenceStore();
    }

    @SuppressWarnings("deprecation")
    public void savePluginPreferences()
    {
        FortranUIPlugin.getDefault().savePluginPreferences();
    }
}
