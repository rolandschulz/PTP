package org.eclipse.photran.internal.tests.refactoring.rename;

import junit.framework.Test;

import org.eclipse.photran.internal.core.util.LineCol;

public class RenameBug278106_Visibility extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN BUG278106.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String file = "bug278106-visibility.f90";

    private Ident m1_m1a = var("m1a");
    private Ident m1_m1b = var("m1b");
    
    private Ident s1_m1b = var("m1b");

    private Ident s2_m1a = var("m1a");
    private Ident s2_m1b = var("m1b");
    
    private Ident s3_m1b = var("m1b");

    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new RenameBug278106_Visibility();
    }
    
    public RenameBug278106_Visibility() throws Exception
    {
        m1_m1a.addReferences(file, new LineCol[] { lc(2,14), lc(14,12), lc(32,13), lc(39,12) });
        m1_m1b.addReferences(file, new LineCol[] { lc(2,19), lc(7,14) });
        
        s1_m1b.addReferences(file, new LineCol[] { lc(13,14), lc(14,17) });

        s2_m1a.addReferences(file, new LineCol[] { lc(24,14), lc(26,12) });
        s2_m1b.addReferences(file, new LineCol[] { lc(25,14), lc(26,17) });
        
        s3_m1b.addReferences(file, new LineCol[] { lc(38,14), lc(39,17) });

        startTests("Renaming file exercising Bug 278324");
        addSuccessTests(m1_m1a, "something_else");
        addSuccessTests(m1_m1b, "something_else");
        addSuccessTests(s1_m1b, "something_else");
        addSuccessTests(s2_m1a, "something_else");
        addSuccessTests(s2_m1b, "something_else");
        addSuccessTests(s3_m1b, "something_else");
        endTests();
    }
}
