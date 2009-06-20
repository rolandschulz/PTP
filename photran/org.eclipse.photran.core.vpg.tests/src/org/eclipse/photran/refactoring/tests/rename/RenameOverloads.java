package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

/**
 * Rename refactoring tests ensuring that renaming external subprograms also
 * updates references in other files and in INTERFACE blocks
 * 
 * @author Jeff Overbey
 */
public class RenameOverloads extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF SUBPROGRAM IDENTIFIERS IN RENAME-EXTSUB1-*.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String file = "rename-overloads.f90";

    private Ident module_overload = var("module_overload");
    private Ident overload_int = var("overload_int");
    private Ident mp_overload_char = var("overload_char");
    private Ident ext_overload_char = var("overload_char");

    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new RenameOverloads();
    }
    
    public RenameOverloads() throws Exception
    {
        module_overload.addReferences(file, new LineCol[] { lc(2,13), lc(7,17), lc(39,10), lc(40,10) });
        overload_int.addReferences(file, new LineCol[] { lc(3,16), lc(5,20), lc(15,12), lc(18,16), lc(22,16), lc(24,20), lc(33,8), lc(41,10) });
        mp_overload_char.addReferences(file, new LineCol[] { lc(6,22), lc(9,14), lc(12,18), lc(42,10) });
        ext_overload_char.addReferences(file, new LineCol[] { lc(26,16), lc(28,20), lc(34,8), lc(46,12), lc(49,16) });

        startTests("Renaming file testing bindings in overload declarations");
        addSuccessTests(module_overload, "something_else");
        addSuccessTests(overload_int, "something_else");
        addSuccessTests(mp_overload_char, "something_else");
        addSuccessTests(ext_overload_char, "something_else");
        endTests();
    }
}
