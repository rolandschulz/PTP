/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.editorHelp;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.help.IHelpResource;

public class CHelpResourceDescriptorImpl implements ICHelpResourceDescriptor
{
    ICHelpBook  book;
    String        name;
    String        label;
    String        href;
    IHelpResource [] resources;

    public CHelpResourceDescriptorImpl(ICHelpBook helpBook, IFunctionSummary functionSummary, String pluginId)
    {
        book = helpBook;
        name = functionSummary.getName();
        //href = "/"+pluginId + "/html/" + name + ".html";
        StringBuffer buf=new StringBuffer();
        buf.append("/").append(pluginId).append("/html/").append(name).append(".html");
        href=buf.toString();
System.out.println("looking for help file: "+href);
        
        label = functionSummary.getPrototype().getPrototypeString(false);
        resources = new IHelpResource[1];
        resources[0] = new IHelpResource() {
            public String getHref()
            {
                return href;
            }

            public String getLabel()
            {
                return label;
            }
        };
    }

    public ICHelpBook getCHelpBook()
    {
        return book;
    }

    public IHelpResource[] getHelpResources()
    {
        return resources;
    }
    public String toString() {
    	return name+" -> "+href;
    }
}
