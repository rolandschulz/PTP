package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

public class Rename4 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN RENAME4*.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String file = "rename4.f90";
    private String filea = "rename4a.fh";
    private String fileb = "rename4b.fh";

    private Ident myProgram = var("MyProgram");
    private Ident hello = var("hello");
    private Ident goodbye = var("goodbye");
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new Rename4();
    }
    
    public Rename4() throws Exception
    {
        myProgram.addReferences(file, new LineCol[] { lc(1,9) });
        hello.addReferences(file, new LineCol[] { lc(3,12) });
        goodbye.addReferences(file, new LineCol[] {});
        
        myProgram.addReferences(filea, new LineCol[] {});
        hello.addReferences(filea, new LineCol[] { lc(1,12) });
        goodbye.addReferences(filea, new LineCol[] { lc(2,12) });
        
        myProgram.addReferences(fileb, new LineCol[] {});
        hello.addReferences(fileb, new LineCol[] { lc(1,10) });
        goodbye.addReferences(fileb, new LineCol[] {});

        startTests("Renaming file with Fortran INCLUDE lines");
        addSuccessTests(myProgram, "ziggySockyZiggySockyHoyHoyHoy");
        addSuccessTests(myProgram, "hello");
        addPreconditionTests(hello, "Goodbye");
        addPreconditionTests(hello, "MyProgram");
        addPreconditionTests(hello, "ziggySockyZiggySockyHoyHoyHoy");
        endTests();
    }
}
