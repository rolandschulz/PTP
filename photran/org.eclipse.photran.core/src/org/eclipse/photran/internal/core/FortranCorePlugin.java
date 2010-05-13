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
    public static final String PLUGIN_ID = "org.eclipse.photran.core";

    public static final String FORTRAN_CONTENT_TYPE = "org.eclipse.photran.core.fortranSource";

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

//    public static final boolean isFixedFormContentType(String contentTypeID)
//    {
//        if (contentTypeID == null) return false;
//        //Content types are defined in .xml files. Currently they are defined by file extensions.
//        // cppPreprocessor content types are children of Free and Fixed form content types,
//        // which in turn are children of FortranContentType
//        IContentType ct = Platform.getContentTypeManager().getContentType(contentTypeID);
//        return ct == null ? false : ct.isKindOf(fixedFormContentType());
//    }
    
    protected static final IContentType getContentTypeOf(String filename)
    {
        return findContentType(filename);
    }
//  
//    protected static final IContentType cppFixedFormContentType()
//    {
//        return Platform.getContentTypeManager().getContentType(C_PREPROCESSOR_FIXED_FORM_CONTENT_TYPE);
//    }
//    
//    protected static final IContentType cppFreeFormContentType()
//    {
//        return Platform.getContentTypeManager().getContentType(C_PREPROCESSOR_FREE_FORM_CONTENT_TYPE);
//    }
//    
//    public static boolean hasFixedFormContentType(IFile file)
//    {
//        return hasFixedFormContentType(getFilenameForIFile(file));
//    }
//
//    public static boolean hasFreeFormContentType(IFile file)
//    {
//        return hasFreeFormContentType(getFilenameForIFile(file));
//    }
//    
//    public static boolean hasCppFixedFormContentType(IFile file)
//    {
//        return hasCppFixedFormContentType(getFilenameForIFile(file));
//    }
//
//    public static boolean hasCppFreeFormContentType(IFile file)
//    {
//        return hasCppFreeFormContentType(getFilenameForIFile(file));
//    }
//    
//    public static boolean hasCppContentType(IFile file)
//    {
//        return hasCppContentType(getFilenameForIFile(file));
//    }

    private static String getFilenameForIFile(IFile file)
    {
        return file == null ? null : file.getFullPath().toString();
    }

//    public static boolean hasFixedFormContentType(String filename)
//    {
//        if (inTestingMode()) // Fortran content types not set in testing workspace
//            return filename.endsWith(".f");
//        else
//        {
//            IContentType ct = getContentTypeOf(filename);
//            return ct != null && ct.isKindOf(fixedFormContentType());
//        }
//    }
//    
//    public static boolean hasFreeFormContentType(String filename)
//    {
//        if (inTestingMode()) // Fortran content types not set in testing workspace
//            return filename.endsWith(".f90");
//        else
//        {
//            IContentType ct = getContentTypeOf(filename);
//            return ct != null && ct.isKindOf(freeFormContentType());
//        }
//    }
//    
//    public static boolean hasCppFixedFormContentType(String filename)
//    {
//        if (inTestingMode()) // Fortran content types not set in testing workspace
//            return filename.endsWith(".F"); 
//        else
//        {
//            IContentType ct = getContentTypeOf(filename);
//            return ct != null && ct.isKindOf(cppFixedFormContentType());
//        }
//    }
//    
//    public static boolean hasCppFreeFormContentType(String filename)
//    {
//        if (inTestingMode()) // Fortran content types not set in testing workspace
//            return filename.endsWith(".F90"); 
//        else
//        {
//            IContentType ct = getContentTypeOf(filename);
//            return ct != null && ct.isKindOf(cppFreeFormContentType());
//        }
//    }
//    
//    public static boolean hasCppContentType(String filename)
//    {
//        return hasCppFreeFormContentType(filename) || 
//               hasCppFixedFormContentType(filename);
//    }
    
    public static final String[] getAllFortranContentTypes()
    {
        return new String[] { FortranCorePlugin.FORTRAN_CONTENT_TYPE, 
                            };
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
                     "org.eclipse.core.contenttype.contentTypes"))
        {
            if (elt.getName().equals("file-association")
                && elt.getAttribute("content-type").equals(contentType))
            {
                Set<String> result = new HashSet<String>();
                String fileExts = elt.getAttribute("file-extensions");
                if(fileExts == null)
                    continue;
                for (String ext : fileExts.split(","))
                    result.add(ext.trim());
                return result;
            }
        }
        return Collections.emptySet();
    }
    
    public static boolean inTestingMode()
    {
        return System.getenv("TESTING") != null;
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
