package org.eclipse.photran.internal.tests.refactoring.rename;

import junit.framework.Test;

import org.eclipse.photran.internal.core.util.LineCol;

public class RenameBug278103 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN BUG278103-*.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String filem = "bug278103-mod.f90";
    private String filep = "bug278103-prog.f90";
    private String files = "bug278103-subs.f90";

    private Ident dim = var("DIM");
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new RenameBug278103();
    }
    
    public RenameBug278103() throws Exception
    {
        dim.addReferences(filem, new LineCol[] { lc(3,27), lc(11,22) });
        dim.addReferences(filep, new LineCol[] { lc(14,24), lc(16,22) });
        dim.addReferences(files, new LineCol[] { lc(5,22), lc(9,14), lc(11,23) });

        startTests("Renaming file exercising Bug 278103");
        addSuccessTests(dim, "something_else");
        endTests();
    }
}
