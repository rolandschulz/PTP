package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.internal.core.util.LineCol;

/**
 * Rename refactoring tests ensuring that renaming external subprograms also
 * updates references in other files and in INTERFACE blocks
 * 
 * @author Jeff Overbey
 */
public class RenameExtSub1 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF SUBPROGRAM IDENTIFIERS IN RENAME-EXTSUB1-*.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String file1 = "rename-extsub1-a.f90";
    private String file2 = "rename-extsub1-b.f90";

    private Ident ext1 = var("ext1");
    private Ident ext2 = var("ext2");
    private Ident ext3 = var("ext3");
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new RenameExtSub1();
    }
    
    public RenameExtSub1() throws Exception
    {
        ext1.addReferences(file1, new LineCol[] { lc(1,12), lc(2,16), lc(6,16), lc(7,20), lc(17,8) });
        
        ext2.addReferences(file1, new LineCol[] { lc(9,31), lc(11,18), lc(18,12) });
        ext2.addReferences(file2, new LineCol[] { lc(1,27), lc(6,3), lc(7,14) });
        
        ext3.addReferences(file1, new LineCol[] { lc(13,22), lc(14,18), lc(19,12), lc(23,10), lc(25,14) });
        ext3.addReferences(file2, new LineCol[] { lc(3,12), lc(5,7) });

        startTests("Renaming file testing aggressive external subprogram binding resolution");
        addSuccessTests(ext1, "something_else");
        addSuccessTests(ext2, "something_else");
        addSuccessTests(ext3, "something_else");
        endTests();
    }
}
