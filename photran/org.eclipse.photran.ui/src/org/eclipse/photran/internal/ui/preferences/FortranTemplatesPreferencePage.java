/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.photran.internal.ui.FortranTemplateManager;
import org.eclipse.photran.internal.ui.FortranUIPlugin;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * Preference page for Fortran code templates.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("deprecation")
public class FortranTemplatesPreferencePage extends TemplatePreferencePage implements IWorkbenchPreferencePage
{
    public FortranTemplatesPreferencePage()
    {
        setPreferenceStore(new AbstractFortranPreferencePage.PreferencesAdapter(FortranUIPlugin.getDefault().getPluginPreferences()));
        setTemplateStore(FortranTemplateManager.getInstance().getTemplateStore());
        setContextTypeRegistry(FortranTemplateManager.getInstance().getContextTypeRegistry());
    }
    
    @Override protected boolean isShowFormatterSetting()
    {
        return false;
    }
}