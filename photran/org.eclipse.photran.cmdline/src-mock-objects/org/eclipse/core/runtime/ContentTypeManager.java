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

import org.eclipse.core.runtime.content.IContentType;

public class ContentTypeManager
{
    private static final String[] fixedFormExtensions = { ".f77", ".f", ".fix", ".F77", ".F", ".FIX", };
    private static final String[] freeFormExtensions = { ".f90", ".f95", ".f03", ".fre", ".F90", ".F95", ".F03", ".FRE" };

    // Copied from PhotranVPG to avoid dependence on that class
    public static final String FIXED_FORM_CONTENT_TYPE = "org.eclipse.photran.core.fixedFormFortranSource";
    public static final String FREE_FORM_CONTENT_TYPE = "org.eclipse.photran.core.freeFormFortranSource";

    private static IContentType FIXED = new IContentType()
    {
        public String getId()
        {
            return FIXED_FORM_CONTENT_TYPE;
        }
        
        public boolean isKindOf(IContentType result)
        {
            return result != null && result.getId().equals(FIXED_FORM_CONTENT_TYPE);
        }
    };

    private static IContentType FREE = new IContentType()
    {
        public String getId()
        {
            return FREE_FORM_CONTENT_TYPE;
        }
        
        public boolean isKindOf(IContentType result)
        {
            return result != null && result.getId().equals(FREE_FORM_CONTENT_TYPE);
        }
    };

    private static IContentType UNKNOWN = new IContentType()
    {
        public String getId()
        {
            return "(unknown content type)";
        }
        
        public boolean isKindOf(IContentType result)
        {
            return result != null && result.getId().equals("(unknown content type)");
        }
    };

    public IContentType findContentTypeFor(String filename)
    {
        for (String ext : fixedFormExtensions)
            if (filename.endsWith(ext))
                return FIXED;

        for (String ext : freeFormExtensions)
            if (filename.endsWith(ext))
                return FREE;

        return UNKNOWN;
    }

    public IContentType[] findContentTypesFor(String filename)
    {
        return new IContentType[] { findContentTypeFor(filename) };
    }

    public IContentType getContentType(String id)
    {
        if (id.equals(FIXED_FORM_CONTENT_TYPE))
            return FIXED;
        else if (id.equals(FREE_FORM_CONTENT_TYPE))
            return FREE;
        else
            return null;
    }
}
