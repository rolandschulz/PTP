package org.eclipse.core.runtime;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.core.vpg.PhotranVPG;

public class ContentTypeManager
{
    private static final String[] fixedFormExtensions = { ".f77", ".f", ".fix", ".F77", ".F", ".FIX", };
    private static final String[] freeFormExtensions = { ".f90", ".f95", ".f03", ".fre", ".F90", ".F95", ".F03", ".FRE" };
    
    private static IContentType FIXED = new IContentType()
    {
        public String getId()
        {
            return PhotranVPG.FIXED_FORM_CONTENT_TYPE;
        }
    };
    
    private static IContentType FREE = new IContentType()
    {
        public String getId()
        {
            return PhotranVPG.FREE_FORM_CONTENT_TYPE;
        }
    };
    
    private static IContentType UNKNOWN = new IContentType()
    {
        public String getId()
        {
            return "(unknown content type)";
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
}
