/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.templates;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Implements the <code>org.eclipse.photran.cdtinterface.addFortranErrorParsers</code> template
 * process runner, which adds Fortran error parsers to a project.
 * <p>
 * When this should be run, it is specified in a template.xml file.
 * 
 * @author Jeff Overbey
 */
public class AddFortranErrorParsersProcessRunner extends PhotranBaseProcessRunner
{
    private static final String ERROR_PARSERS_EXTENSION_POINT = "org.eclipse.cdt.core.ErrorParser"; //$NON-NLS-1$

    /**
     * A semicolon-separated list of (non-Photran) error parsers that will be enabled on the project
     * in addition to Photran's error parsers.
     */
    private static final String DEFAULT_ERROR_PARSERS =
        "org.eclipse.cdt.core.MakeErrorParser;" +         //$NON-NLS-1$
        "org.eclipse.photran.core.GFortranErrorParser;" + //$NON-NLS-1$
        "org.eclipse.cdt.core.GCCErrorParser;" +          //$NON-NLS-1$
        "org.eclipse.cdt.core.GLDErrorParser;" +          //$NON-NLS-1$
        "org.eclipse.cdt.core.GASErrorParser";            //$NON-NLS-1$

    @Override
    protected void modify(IProject proj, IConfiguration cf)
    {
        if (!containsAtLeastOnePhotranErrorParser(cf))
            addAllPhotranErrorParsersTo(cf);
    }

    private boolean containsAtLeastOnePhotranErrorParser(IConfiguration cf)
    {
        return cf.getErrorParserIds().contains("org.eclipse.photran"); //$NON-NLS-1$
    }

    private void addAllPhotranErrorParsersTo(IConfiguration cf)
    {
        StringBuilder sb = new StringBuilder(DEFAULT_ERROR_PARSERS);

        for (IExtension ext : errorParsersExtPt().getExtensions())
        {
            if (ext.getLabel().contains("Photran"));      // If "Photran" is //$NON-NLS-1$
            {                                             // in the name,
                sb.append(';');                           // add this error
                sb.append(ext.getNamespaceIdentifier());  // parser
                sb.append('.');
                sb.append(ext.getSimpleIdentifier());
            }
        }

        cf.setErrorParserIds(sb.toString());
    }

    private IExtensionPoint errorParsersExtPt()
    {
        return Platform.getExtensionRegistry().getExtensionPoint(ERROR_PARSERS_EXTENSION_POINT);
    }
}
