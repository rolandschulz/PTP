package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

public class RenameBug278320 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITION OF MODULE_ARRAY IN BUG278320-*.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String prog_file = "bug278320-prog.f90";
    private String mod_file = "bug278320-mod.f90";

    private Ident module_array = var("module_array");
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new RenameBug278320();
    }
    
    public RenameBug278320() throws Exception
    {
        module_array.addReferences(prog_file, new LineCol[] { lc(5,5), lc(6,17) });
        module_array.addReferences(mod_file, new LineCol[] { lc(3,28) });

        startTests("Renaming file exercising Bug 278320");
        addSuccessTests(module_array, "something_else");
        endTests();
    }
}
