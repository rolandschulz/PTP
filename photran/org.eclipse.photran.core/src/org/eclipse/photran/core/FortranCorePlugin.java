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
package org.eclipse.photran.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.photran.internal.core.parser.Parser;
import org.osgi.framework.BundleContext;

/**
 * The main plug-in class for the Photran Core plug-in
 * 
 * @author (generated)
 */
public class FortranCorePlugin extends Plugin
{
    public static final String FIXED_FORM_CONTENT_TYPE = "org.eclipse.photran.core.fixedFormFortranSource";
    public static final String FREE_FORM_CONTENT_TYPE = "org.eclipse.photran.core.freeFormFortranSource";
    
    // The shared instance.
    private static FortranCorePlugin plugin;
    
    private static Parser parser = new Parser();

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
    
    /**
     * @return the Fortran 95 parser
     */
    public static Parser getParser()
    {
        return parser;
    }
}
