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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.osgi.framework.BundleContext;

/**
 * Activator class for the Photran Core plug-in.
 * 
 * @author (generated)
 * @author Jeff Overbey - added utility methods (content type checking, etc.)
 */
public class FortranCorePlugin extends Plugin
{
    public static final String PLUGIN_ID = "org.eclipse.photran.core"; //$NON-NLS-1$

    public static final String FORTRAN_CONTENT_TYPE = "org.eclipse.photran.core.fortranSource"; //$NON-NLS-1$

    public static final IContentType fortranContentType()
    {
        return Platform.getContentTypeManager().getContentType(FORTRAN_CONTENT_TYPE);
    }

    public static boolean hasFortranContentType(IFile file)
    {
        return hasFortranContentType(getFilenameForIFile(file));
    }

    public static boolean hasFortranContentType(String filename)
    {
        IContentType ct = getContentTypeOf(filename);
        return ct != null && ct.isKindOf(fortranContentType());
    }
    
    protected static final IContentType getContentTypeOf(String filename)
    {
        return findContentType(filename);
    }

    private static String getFilenameForIFile(IFile file)
    {
        return file == null ? null : file.getFullPath().toString();
    }
    
    public static final String[] getAllFortranContentTypes()
    {
        return new String[] { FortranCorePlugin.FORTRAN_CONTENT_TYPE };
    }
    
    public static IContentType findContentType(String filename)
    {
        IContentType[] cts = Platform.getContentTypeManager().findContentTypesFor(filename);
        if (cts.length == 0)
            return null;
        else if (cts.length == 1)
            return cts[0];
        
        // Annoyingly, Eclipse does not do case-sensitive matching of filename
        // extensions (at least on case-insensitive filesystems), which is
        // important for Fortran filenames; we have to do that manually
        
        List<IContentType> possibilities = new ArrayList<IContentType>(cts.length);
        
        String ext = filename.substring(filename.lastIndexOf('.')+1);
        for (IContentType ct : cts)
            if (getFilenameExtensions(ct.getId()).contains(ext))
                possibilities.add(ct);

        if (possibilities.isEmpty()) return cts[0];

        // Now find the most specific of the possible content types
        
        IContentType result = null;
        for (IContentType ct : possibilities)
        {
            if (result == null)
                result = ct;
            else if (ct.isKindOf(result))
                result = ct;
        }
        return result;
    }
    
    private static Set<String> getFilenameExtensions(String contentType)
    {
        for (IConfigurationElement elt :
                 Platform.getExtensionRegistry().getConfigurationElementsFor(
                     "org.eclipse.core.contenttype.contentTypes")) //$NON-NLS-1$
        {
            if (elt.getName().equals("file-association") //$NON-NLS-1$
                && elt.getAttribute("content-type").equals(contentType)) //$NON-NLS-1$
            {
                Set<String> result = new HashSet<String>();
                String fileExts = elt.getAttribute("file-extensions"); //$NON-NLS-1$
                if(fileExts == null)
                    continue;
                for (String ext : fileExts.split(",")) //$NON-NLS-1$
                    result.add(ext.trim());
                return result;
            }
        }
        return Collections.emptySet();
    }
    
    public static boolean inTestingMode()
    {
        //return System.getenv("TESTING") != null;
        
        // This will return true if Photran is being run via the JUnit Plug-in Test runner
        String app = System.getProperty("eclipse.application"); //$NON-NLS-1$
        return app != null && app.toLowerCase().contains("junit"); //$NON-NLS-1$
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
    @Override public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override public void stop(BundleContext context) throws Exception
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
    
    public static void log(Throwable e) {
        log("Error", e); //$NON-NLS-1$
    }

    public static void log(String message, Throwable e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }
}
