package org.eclipse.core.runtime;

public class Platform
{
    public static ContentTypeManager getContentTypeManager()
    {
        return new ContentTypeManager();
    }
}
