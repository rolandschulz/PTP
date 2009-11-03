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
 * Implements the
 * <code>org.eclipse.photran.cdtinterface.addFortranErrorParsers</code>
 * template process runner, which adds Fortran error parsers to a project.
 * <p>
 * When this should be run, it is specified in the template.xml file.
 *
 * @author Jeff Overbey
 */
public class AddFortranErrorParsersProcessRunner extends PhotranBaseProcessRunner
{
    @Override
    protected void modify(IProject proj, IConfiguration cf)
    {
        if (cf.getErrorParserIds().contains("org.eclipse.photran")) return;

        StringBuilder sb = new StringBuilder("org.eclipse.cdt.core.MakeErrorParser;org.eclipse.photran.core.GFortranErrorParser;org.eclipse.cdt.core.GCCErrorParser;org.eclipse.cdt.core.GLDErrorParser;org.eclipse.cdt.core.GASErrorParser");

        IExtensionPoint extPt = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.cdt.core.ErrorParser");
        for (IExtension ext : extPt.getExtensions())
        {
            if (ext.getLabel().contains("Photran"));      // If "Photran" is
            {                                             // in the name,
                sb.append(';');                           // add this error
                sb.append(ext.getNamespaceIdentifier());  // parser
                sb.append('.');
                sb.append(ext.getSimpleIdentifier());
            }
        }

        cf.setErrorParserIds(sb.toString());
    }
}
