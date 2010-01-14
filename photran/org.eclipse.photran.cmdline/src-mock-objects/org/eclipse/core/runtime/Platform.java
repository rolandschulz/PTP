/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.eclipse.core.resources.Util;
import org.eclipse.photran.internal.core.analysis.preservation.ASTNodeAdapterFactory;

public class Platform
{
    public static ContentTypeManager getContentTypeManager()
    {
        return new ContentTypeManager();
    }

    public static IExtensionRegistry getExtensionRegistry()
    {
        return new IExtensionRegistry()
        {
            public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId)
            {
                Util.displayWarning("WARNING: IExtensionRegistry#getConfigurationElementsFor not implemented");
                return new IConfigurationElement[0];
            }
        };
    }

    public static IAdapterManager getAdapterManager()
    {
        return new IAdapterManager()
        {
            public Object getAdapter(Object node, Class<?> clazz)
            {
                return new ASTNodeAdapterFactory().getAdapter(node, clazz);
            }
        };
    }
}
