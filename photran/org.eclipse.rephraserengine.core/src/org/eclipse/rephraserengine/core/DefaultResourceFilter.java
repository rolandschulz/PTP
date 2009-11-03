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
package org.eclipse.rephraserengine.core;

import org.eclipse.core.resources.IResource;

/**
 * A resource filter that accepts all resources, as long as they are accessible.
 *
 * @author Jeff Overbey
 *
 * @see IResourceFilter
 * @see IResource#isAccessible()
 * 
 * @since 1.0
 */
public class DefaultResourceFilter implements IResourceFilter
{
    public boolean shouldProcess(IResource resource)
    {
       return resource.isAccessible();
    }
    
    public String getError(IResource resource)
    {
        return "The resource (" + resource.getName() + ") is not accessible.";
    }
}
