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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.internal.core.FortranCorePlugin;

/**
 * Entrypoint for determining the source form (fixed form, free form, free form with C preprocessor
 * directives, etc.) associated with a particular file.
 * <p>
 * The source form of a file is determined (1) by looking at the source form project properties for
 * the enclosing project, if any, and then (2) by looking at the filename extension and finding a
 * source form that has a hard-coded association with that extension.  If both of these fail, the
 * file is assumed to be {@link UnpreprocessedFreeSourceForm}.
 * <p>
 * Internally, this class is used as a Strategy object in {@link LexerFactory}.
 * 
 * @author Jeff Overbey
 */
public final class SourceForm
{
    private static final String SOURCE_FORM_EXTENSION_POINT_ID = "org.eclipse.photran.core.sourceForms"; //$NON-NLS-1$

    private SourceForm() {;}

    public static ISourceForm of(IFile file, String filename)
    {
        if (file != null)
            return SourceForm.of(file);
        else
            return SourceForm.of(filename, null);
    }

    public static ISourceForm of(IFile file)
    {
        return SourceForm.of(determineFilename(file), propertiesFor(file));
    }

    /**
     * @return the name of the source form that will handle the given file (i.e., the <i>name</i>
     *         attribute of the contribution to the {@value #SOURCE_FORM_EXTENSION_POINT_ID}
     *         extension point)
     */
    public static String descriptionFor(IFile file)
    {
        return SourceForm.descriptionFor(determineFilename(file), propertiesFor(file));
    }

    private static SourceFormProperties propertiesFor(IFile file)
    {
        if (file == null || file.getProject() == null)
            return null;
        else
            return new SourceFormProperties(file.getProject());
    }

    public static String determineFilename(IFile file)
    {
        if (file == null) return null;
        
        IPath location = file.getLocation();
        if (location != null)
            return location.toOSString();
        else
            return file.getFullPath().toOSString();
    }

    /**
     * @param spec a content type specification, as returned by
     *            {@link #allConfiguredContentTypeAssociations()}
     * @return the description (name) of the source form which will handle the given content type
     *         (i.e., the <i>name</i> attribute of the contribution to the
     *         {@value #SOURCE_FORM_EXTENSION_POINT_ID} extension point)
     */
    public static String descriptionForContentType(String spec)
    {
        return SourceForm.descriptionFor(spec, null);
    }

    private static ISourceForm of(String filename, SourceFormProperties properties)
    {
        if (filename == null) return defaultSourceForm();
        
        try
        {
            IConfigurationElement config = findExtensionHandling(filename, properties);
            
            if (config != null)
                return (ISourceForm)config.createExecutableExtension("class"); //$NON-NLS-1$
            else
                return defaultSourceForm();
        }
        catch (CoreException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static ISourceForm defaultSourceForm()
    {
        try
        {
            return (ISourceForm)findDefaultSourceFormExtension().createExecutableExtension("class"); //$NON-NLS-1$
        }
        catch (CoreException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static IConfigurationElement findDefaultSourceFormExtension()
    {
        for (IConfigurationElement config : allSourceFormConfigs())
            if (isDefaultHandler(config))
                return config;
        
        throw new IllegalStateException("No default source form defined"); //$NON-NLS-1$
    }

    private static boolean isDefaultHandler(IConfigurationElement config)
    {
        return config.getAttribute("priority").equals("default"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String descriptionFor(String filename, SourceFormProperties properties)
    {
        IConfigurationElement config = findExtensionHandling(filename, properties);
        
        if (config != null)
            return nameOf(config);
        else
            return nameOf(findDefaultSourceFormExtension());
    }
    
    private static IConfigurationElement findExtensionHandling(String filename, SourceFormProperties properties)
    {
        if (filename == null) return null;

        for (IConfigurationElement config : allSourceFormConfigs())
            if (isHighPriorityHandler(config, filename, properties))
                return config;

        for (IConfigurationElement config : allSourceFormConfigs())
            if (isMediumPriorityHandler(config, filename, properties))
                return config;

        for (IConfigurationElement config : allSourceFormConfigs())
            if (canHandle(config, filename, properties))
                return config;
        
        return null;
    }

    private static IConfigurationElement[] allSourceFormConfigs()
    {
        return Platform.getExtensionRegistry()
                       .getConfigurationElementsFor(SOURCE_FORM_EXTENSION_POINT_ID);
    }

    private static String nameOf(IConfigurationElement config)
    {
        return config.getAttribute("name"); //$NON-NLS-1$
    }

    /**
     * @return a list with the name of every available source form (i.e., the <i>name</i> attribute
     *         of every source form contributed to the {@value #SOURCE_FORM_EXTENSION_POINT_ID}
     *         extension point)
     */
    public static Set<String> allSourceForms()
    {
        Set<String> result = new TreeSet<String>();
        for (IConfigurationElement config : allSourceFormConfigs())
            result.add(nameOf(config));
        return result;
    }

    /**
     * @return a list of the filenames and filename extensions associated with the Fortran content
     *         type.  Filenames ({@link IContentType#FILE_NAME_SPEC}) are returned as-is; filename
     *         extensions ({@link IContentType#FILE_EXTENSION_SPEC}) are returned as the string
     *         <tt>*.<i>extension</i></tt>.
     */
    public static List<String> allConfiguredContentTypeAssociations()
    {
        List<String> result = new ArrayList<String>(32);
        
        for (String spec : FortranCorePlugin.fortranContentType().getFileSpecs(IContentType.FILE_EXTENSION_SPEC))
            result.add("*." + spec.toLowerCase()); //$NON-NLS-1$

        for (String spec : FortranCorePlugin.fortranContentType().getFileSpecs(IContentType.FILE_EXTENSION_SPEC))
            result.add("*." + spec.toUpperCase()); //$NON-NLS-1$

        for (String spec : FortranCorePlugin.fortranContentType().getFileSpecs(IContentType.FILE_NAME_SPEC))
            result.add(spec);
        
        return result;
    }
    
    private static boolean isHighPriorityHandler(IConfigurationElement config, String filename, SourceFormProperties properties)
    {
        return isHighPriorityHandler(config) && canHandle(config, filename, properties);
    }

    private static boolean isHighPriorityHandler(IConfigurationElement config)
    {
        return config.getAttribute("priority").equals("high"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private static boolean isMediumPriorityHandler(IConfigurationElement config, String filename, SourceFormProperties properties)
    {
        return isMediumPriorityHandler(config) && canHandle(config, filename, properties);
    }

    private static boolean isMediumPriorityHandler(IConfigurationElement config)
    {
        return config.getAttribute("priority").equals("medium"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static boolean canHandle(IConfigurationElement config, String filename, SourceFormProperties properties)
    {
        if (properties != null)
        {
            String extension = filenameExtensionOf(filename);
            String filenameWithoutPath = filenameWithoutPath(filename);
            return properties.sourceFormForExtension(extension).equals(nameOf(config))
                || properties.sourceFormForFilename(filenameWithoutPath).equals(nameOf(config));
        }
        else
        {
            String exts = config.getAttribute("defaultForFilenameExtensions"); //$NON-NLS-1$
            if (exts == null)
                return false;
            else
                return Arrays.asList(exts.split(",[ \t]*")) //$NON-NLS-1$
                             .contains(filenameExtensionOf(filename));
        }
    }

    private static String filenameExtensionOf(String filename)
    {
        return filename.substring(filename.lastIndexOf('.')+1);
    }

    private static String filenameWithoutPath(String filename)
    {
        return filename.substring(filename.lastIndexOf(File.separatorChar)+1);
    }

    public static boolean isFixedForm(IFile file)
    {
        return SourceForm.of(file).isFixedForm();
    }

    public static boolean isCPreprocessed(IFile file)
    {
        return SourceForm.of(file).isCPreprocessed();
    }
}
