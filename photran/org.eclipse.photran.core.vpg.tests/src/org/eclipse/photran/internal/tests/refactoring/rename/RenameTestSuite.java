/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring.rename;

import java.util.HashMap;
import java.util.Set;

import junit.framework.TestSuite;

import org.eclipse.photran.internal.core.util.LineCol;

public abstract class RenameTestSuite extends TestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN RENAME1.F90, AND
    // GROUP THEM ACCORDING TO WHICH ONES SHOULD BE RENAMED TOGETHER
    //
    ///////////////////////////////////////////////////////////////////////////
    
    protected static class Ident
    {
        private String name;
        private HashMap<String, LineCol[]> references;
        
        public Ident(String name)
        {
            this.name = name;
            this.references = new HashMap<String, LineCol[]>();
        }
        
        public Ident(String filename, String name, LineCol[] referencesInFile)
        {
            this(name);
            addReferences(filename, referencesInFile);
        }
        
        public void addReferences(String filename, LineCol[] referencesInFile)
        {
            this.references.put(filename, referencesInFile);
        }

        public String getName()
        {
            return this.name;
        }

        public Set<String> getFiles()
        {
            return this.references.keySet();
        }
        
        public LineCol[] getReferences(String filename)
        {
            return this.references.get(filename);
        }
    }

    protected Ident var(String name) { return new Ident(name); }
    protected Ident var(String filename, String name, LineCol[] refs) { return new Ident(filename, name, refs); }
    protected LineCol lc(int line, int col) { return new LineCol(line, col); }
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // SUITE BUILDING
    //
    ///////////////////////////////////////////////////////////////////////////

    private TestSuite currentSubSuite = null;
    
    protected void startTests(String description)
    {
        currentSubSuite = new TestSuite();
        currentSubSuite.setName(description);
    }
    
    protected void addSuccessTests(Ident ident, String newName)
    {
        TestSuite subSubSuite = new TestSuite();
        subSubSuite.setName("Renaming " + describe(ident, newName));
        
        for (String filename : ident.getFiles())
            for (LineCol position : ident.getReferences(filename))
                subSubSuite.addTest(new RenameTestCase.ExpectSuccess(filename, ident, position, newName));
        
        currentSubSuite.addTest(subSubSuite);
    }

    protected void addPreconditionTests(Ident ident, String newName)
    {
        TestSuite subSubSuite = new TestSuite();
        subSubSuite.setName("Attempting to rename " + describe(ident, newName));
        
        for (String filename : ident.getFiles())
            for (LineCol position : ident.getReferences(filename))
                subSubSuite.addTest(new RenameTestCase.ExpectFailure(filename, ident, position, newName));
        
        currentSubSuite.addTest(subSubSuite);
    }
    
    private String describe(Ident ident, String newName)
    {
        return ident.name + " to " + newName;
    }
    
    protected void endTests()
    {
        addTest(currentSubSuite);
        currentSubSuite = null;
    }
}
