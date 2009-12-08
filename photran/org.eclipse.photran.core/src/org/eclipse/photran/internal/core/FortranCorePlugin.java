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
package org.eclipse.photran.internal.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.content.IContentType;
import org.osgi.framework.BundleContext;

/**
 * Activator class for the Photran Core plug-in.
 * 
 * @author (generated)
 */
public class FortranCorePlugin extends Plugin
{
    private static final String FIXED_FORM_CONTENT_TYPE = "org.eclipse.photran.core.fixedFormFortranSource";
    private static final String FREE_FORM_CONTENT_TYPE = "org.eclipse.photran.core.freeFormFortranSource";
    
    public static final IContentType fixedFormContentType()
    {
        return Platform.getContentTypeManager().getContentType(FIXED_FORM_CONTENT_TYPE);
    }

    public static final IContentType freeFormContentType()
    {
        return Platform.getContentTypeManager().getContentType(FREE_FORM_CONTENT_TYPE);
    }
    
    public static final boolean isFixedFormContentType(String contentTypeID)
    {
        if (contentTypeID == null) return false;
        
        IContentType ct = Platform.getContentTypeManager().getContentType(contentTypeID);
        return ct == null ? false : ct.isKindOf(fixedFormContentType());
    }
    
    public static final String[] getAllFortranContentTypes()
    {
        return new String[] { FortranCorePlugin.FIXED_FORM_CONTENT_TYPE, FortranCorePlugin.FREE_FORM_CONTENT_TYPE };
    }

    // The shared instance.
    private static FortranCorePlugin plugin;

    /**
     * The constructor.
     */
    public FortranCorePlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static FortranCorePlugin getDefault()
    {
        return plugin;
    }
}
