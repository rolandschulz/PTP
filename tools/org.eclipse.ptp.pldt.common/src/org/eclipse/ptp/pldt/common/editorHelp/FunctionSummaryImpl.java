/**********************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.editorHelp;

import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;

/**
 * Used by all four PLDT types MPI, OpenMP, LAPI and UPC, to create the structures that hover help needs
 * @author Beth Tibbitts
 *
 */
public class FunctionSummaryImpl implements IFunctionSummary
{
	/**
	 * This name is used for the name of the html help file referenced by the F1 (dynamic help) function key.
	 */
    private String                    name;
    private String                    namespace;
    private String                    description;
    private IFunctionPrototypeSummary prototype;
    private IRequiredInclude[]        includes;

    public FunctionSummaryImpl()
    {
    	
    }
    /**
     * 
     * @param name - used for  the name of the html help file referenced by the F1 (dynamic help) function key.  Other use too? assumed.
     * @param namespace
     * @param description
     * @param prototype
     * @param includes
     */
    public FunctionSummaryImpl(String name, String namespace, String description,
            IFunctionPrototypeSummary prototype, IRequiredInclude[] includes)
    {
        this.name = name;
        this.namespace = namespace;
        this.description = description;
        this.prototype = prototype;
        this.includes = includes;
    }

    public String getName()
    {
        return this.name;
    }

    public String getNamespace()
    {
        return this.namespace;
    }

    public String getDescription()
    {
        return this.description;
    }

    public IFunctionPrototypeSummary getPrototype()
    {
        return this.prototype;
    }

    public IRequiredInclude[] getIncludes()
    {
        return this.includes;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setIncludes(IRequiredInclude[] includes)
    {
        this.includes = includes;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public void setPrototype(IFunctionPrototypeSummary prototype)
    {
        this.prototype = prototype;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("name=").append(this.getName()).append('\n'); //$NON-NLS-1$
        buf.append("namespace=").append(this.getNamespace()).append('\n'); //$NON-NLS-1$
        buf.append("desc=").append(this.getDescription()).append('\n'); //$NON-NLS-1$
        buf.append("..."); //$NON-NLS-1$
        return buf.toString();
    }
}
