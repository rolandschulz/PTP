/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.tests.preprocessor.c.source_reproduction;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.photran.internal.core.tests.preprocessor.c.CppIsolatedSourceReproductionTestSuite;

public class PrintPOP
{
    public static Test suite() throws FileNotFoundException, IOException
    {
        return new CppIsolatedSourceReproductionTestSuite("../../org.eclipse.photran-projects.confidential.pop", false, false) {};
    }
}