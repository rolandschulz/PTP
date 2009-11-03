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
 * Implements the
 * <code>org.eclipse.photran.cdtinterface.addAllBinaryParsers</code>
 * template process runner, which enables all binary parsers on a project.
 * <p>
 * When this should be run, it is specified in the template.xml file.
 *
 * @author Jeff Overbey
 */
public class AddAllBinaryParsersProcessRunner extends PhotranBaseProcessRunner
{
    @Override
    protected void modify(IProject proj, IConfiguration cf) throws CoreException
    {
        ArrayList<String> binaryParsers = new ArrayList<String>(16);

        IExtensionPoint extPt = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.cdt.core.BinaryParser");
        for (IExtension ext : extPt.getExtensions())
        {
            String thisBinaryParser = ext.getNamespaceIdentifier() + "." + ext.getSimpleIdentifier();

            if (!contains(binaryParsers, thisBinaryParser))
                binaryParsers.add(thisBinaryParser);
        }

        ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(proj);
        CoreModelUtil.setBinaryParserIds(desc.getConfigurations(),
            binaryParsers.toArray(new String[binaryParsers.size()]));
        CoreModel.getDefault().setProjectDescription(proj, desc);
    }

    private boolean contains(ArrayList<String> binaryParsers, String thisBinaryParser)
    {
        for (String id : binaryParsers)
            if (id.equalsIgnoreCase(thisBinaryParser))
                return true;
        return false;
    }
}
