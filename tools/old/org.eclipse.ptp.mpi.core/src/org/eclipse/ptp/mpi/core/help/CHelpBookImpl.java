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

package org.eclipse.ptp.mpi.core.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.ptp.mpi.core.MpiPlugin;

public class CHelpBookImpl implements ICHelpBook
{
    private String title                                              = "Generic C Help Book";
    private String pluginId                                           = MpiPlugin.getPluginId();

    protected Map /* String -> IFunctionSummary */funcName2FuncInfo = new HashMap();

    public CHelpBookImpl()
    {
    }

    public CHelpBookImpl(String pluginId)
    {
        this.pluginId = pluginId;
    }

    public String getTitle()
    {
        return title;
    }

    protected void setTitle(String title)
    {
        this.title = title;
    }

    public int getCHelpType()
    {
        return ICHelpBook.HELP_TYPE_C;
    }

    public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, String name)
    {
        return (IFunctionSummary) funcName2FuncInfo.get(name);
    }

    public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, String prefix)
    {
        List functionSummaryList = new ArrayList();
        for (Iterator it = funcName2FuncInfo.keySet().iterator(); it.hasNext();) {
            String funcName = (String) it.next();
            if (funcName != null && funcName.toUpperCase().startsWith(prefix.toUpperCase())) {
                functionSummaryList.add(funcName2FuncInfo.get(funcName));
            }
        }

        IFunctionSummary[] functionSummaryArray = null;

        // populate array
        if (!functionSummaryList.isEmpty()) {
            functionSummaryArray = new IFunctionSummary[functionSummaryList.size()];
            int i = 0;
            for (Iterator it = functionSummaryList.iterator(); it.hasNext(); i++) {
                functionSummaryArray[i] = (IFunctionSummary) it.next();
            }
        }

        return functionSummaryArray;
    }

    public ICHelpResourceDescriptor[] getHelResources(ICHelpInvocationContext context, String name)
    {
        IFunctionSummary functionSummary = getFunctionInfo(context, name);
        if (functionSummary == null) return null;

        ICHelpResourceDescriptor resourceDescriptor[] = new ICHelpResourceDescriptor[1];
        resourceDescriptor[0] = new CHelpResourceDescriptorImpl(this, functionSummary, this.pluginId);
        return resourceDescriptor;
    }
}
