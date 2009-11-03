package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.internal.core.util.LineCol;

public class RenameFnResult extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN RENAME-FN-RESULT.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String file = "rename-fn-result.f90";

    private Ident f = var("f");
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new RenameFnResult();
    }
    
    public RenameFnResult() throws Exception
    {
        f.addReferences(file, new LineCol[] { lc(1,12), lc(3,12), lc(4,16), lc(5,5), lc(6,16) });

        startTests("Renaming function and implied result variable simultaneously");
        addSuccessTests(f, "something_else");
        endTests();
    }
}
