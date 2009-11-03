/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.preferences;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.photran.internal.cdtinterface.core.FortranLanguage;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * CDT integration features Fortran preference page
 * 
 * @author Jeff Overbey
 */
public class CDTFortranPreferencePage extends AbstractFortranPreferencePage
{
    protected void setDescription()
    {
    }

    protected void initializeDefaults()
    {
        FortranPreferences.PREFERRED_MODEL_BUILDER.setDefault();
        FortranPreferences.PREFERRED_DOM_PARSER.setDefault();
    }

    protected void createFieldEditors()
    {
        String[][] modelBuilders = listModelBuilders();
        if (modelBuilders != null)
            addField(new ComboFieldEditor(FortranPreferences.PREFERRED_MODEL_BUILDER.getName(),
                                            "Preferred Model Builder",
                                            modelBuilders,
                                            getFieldEditorParent()));
        
        String[][] domParsers = listDOMParsers();
        if (domParsers != null)
            addField(new ComboFieldEditor(FortranPreferences.PREFERRED_DOM_PARSER.getName(),
                                            "Preferred DOM Parser",
                                            domParsers,
                                            getFieldEditorParent()));
    }

    private String[][] listModelBuilders()
    {
        IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(
            FortranLanguage.FORTRAN_MODEL_BUILDER_EXTENSION_POINT_ID);
        return configs.length == 0 ? null : createKeyValuePairs(configs);
    }

    private String[][] listDOMParsers()
    {
        IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(
            FortranLanguage.FORTRAN_DOM_PARSER_EXTENSION_POINT_ID);
        return configs.length == 0 ? null : createKeyValuePairs(configs);
    }

    private String[][] createKeyValuePairs(IConfigurationElement[] configs)
    {
        String[][] result = new String[configs.length][];
        for (int i = 0; i < configs.length; i++)
            result[i] = new String[] { configs[i].getAttribute("name"), configs[i].getAttribute("id") };
        Arrays.sort(result, new Comparator()
        {
            public int compare(Object arg0, Object arg1)
            {
                String[] kvPair1 = (String[])arg0;
                String[] kvPair2 = (String[])arg1;
                return kvPair1[0].compareTo(kvPair2[0]);
            }
        });
        return result;
    }
}