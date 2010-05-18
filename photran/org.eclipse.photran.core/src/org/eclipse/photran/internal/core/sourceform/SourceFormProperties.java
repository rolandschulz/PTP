/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.sourceform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.photran.internal.core.properties.AbstractProperties;

/**
 * Provides access to the source form settings for a project.
 * <p>
 * The user may set these via the Fortran &gt; Source Forms category in the project properties
 * dialog.
 * 
 * @author Jeff Overbey
 * 
 * @see org.eclipse.photran.internal.ui.properties.SourceFormPropertyPage
 */
public class SourceFormProperties extends AbstractProperties
{
    public static final String SOURCE_FORMS_PROPERTY_NAME = "SourceForms"; //$NON-NLS-1$

    public SourceFormProperties(IProject proj)
    {
        setProject(proj);
    }

    @Override
    protected void initializeDefaults(IProject proj, IPreferenceStore properties)
    {
        List<String> defaults = new ArrayList<String>();
        for (String spec : SourceForm.allConfiguredContentTypeAssociations())
            defaults.add(spec + "=" + SourceForm.descriptionForContentType(spec)); //$NON-NLS-1$
        properties.setDefault(SOURCE_FORMS_PROPERTY_NAME,
            createList(defaults.toArray(new String[defaults.size()])));
    }

    public String sourceFormForExtension(String extension)
    {
        Map<String, String> parsedValue = parseValue();
        return ifNull(parsedValue.get("*." + extension), ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String sourceFormForFilename(String filenameWithoutPath)
    {
        Map<String, String> parsedValue = parseValue();
        return ifNull(parsedValue.get(filenameWithoutPath), ""); //$NON-NLS-1$
    }

    private String ifNull(String value, String defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    private Map<String, String> parseValue()
    {
        if (getPropertyStore() == null)
            throw new IllegalArgumentException(
                "This method cannot be called unless the explicit-value constructor was called"); //$NON-NLS-1$
        return parseValue(getPropertyStore().getString(SOURCE_FORMS_PROPERTY_NAME));
    }

    public static Map<String, String> parseValue(String propertyValue)
    {
        Map<String, String> result = new TreeMap<String, String>();
        for (String kvPair : parseString(propertyValue))
        {
            int equals = kvPair.indexOf('=');
            if (equals > 0)
            {
                String key = kvPair.substring(0, equals);
                String value = kvPair.substring(equals + 1);
                result.put(key, value);
            }
        }
        return result;
    }

    public static String unparseValue(Map<String, String> map)
    {
        List<String> values = new ArrayList<String>(map.size());
        for (String spec : map.keySet())
            values.add(spec + "=" + map.get(spec)); //$NON-NLS-1$
        return createList(values.toArray(new String[values.size()]));
    }
}
