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

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Implements the <code>org.eclipse.photran.cdtinterface.addAllBinaryParsers</code> template process
 * runner, which enables all binary parsers on a project.
 * <p>
 * When this should be run, it is specified in a template.xml file.
 * 
 * @author Jeff Overbey
 */
public class AddAllBinaryParsersProcessRunner extends PhotranBaseProcessRunner
{
    private static final String BINARY_PARSERS_EXTENSION_POINT = "org.eclipse.cdt.core.BinaryParser"; //$NON-NLS-1$

    @Override
    protected void modify(IProject proj, IConfiguration cf) throws CoreException
    {
        setBinaryParsers(proj, collectAllBinaryParsers());
    }

    private ArrayList<String> collectAllBinaryParsers()
    {
        ArrayList<String> binaryParsers = new ArrayList<String>(16);
        for (IExtension ext : binaryParsersExtPt().getExtensions())
        {
            String thisBinaryParser = ext.getNamespaceIdentifier()
                + "." + ext.getSimpleIdentifier(); //$NON-NLS-1$

            if (!contains(binaryParsers, thisBinaryParser)) binaryParsers.add(thisBinaryParser);
        }
        return binaryParsers;
    }

    private IExtensionPoint binaryParsersExtPt()
    {
        return Platform.getExtensionRegistry().getExtensionPoint(BINARY_PARSERS_EXTENSION_POINT);
    }

    private boolean contains(ArrayList<String> binaryParsers, String thisBinaryParser)
    {
        for (String id : binaryParsers)
            if (id.equalsIgnoreCase(thisBinaryParser)) return true;
        return false;
    }

    private void setBinaryParsers(IProject proj, ArrayList<String> binaryParsers)
        throws CoreException
    {
        ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(proj);
        CoreModelUtil.setBinaryParserIds(desc.getConfigurations(),
            binaryParsers.toArray(new String[binaryParsers.size()]));
        CoreModel.getDefault().setProjectDescription(proj, desc);
    }
}
