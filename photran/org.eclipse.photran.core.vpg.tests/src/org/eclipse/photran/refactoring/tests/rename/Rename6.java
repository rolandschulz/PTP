package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

public class Rename6 extends RenameTestSuite
{
    private String filename = "rename6.f90";
    
    private Ident[] vars = new Ident[] {
        var(filename, "outer", new LineCol[] { lc(1,21), lc(3,8), lc(9,6) }),
        var(filename, "i", new LineCol[] { lc(6,23), lc(7,15), lc(15,22), lc(22,30), lc(24,14), lc(26,12), lc(28,23), lc(28,29) }),
        var(filename, "hi", new LineCol[] { lc(7,27), lc(18,16), lc(18,30), lc(22,6) }),
        var(filename, "bye", new LineCol[] { lc(7,38), lc(18,20), lc(18,40), lc(22,9) }),
        var(filename, "ty", new LineCol[] { lc(15,2), lc(15,2), lc(15,9), lc(22,13) }),
        var(filename, "array", new LineCol[] { lc(10,12), lc(24,6), lc(26,6), lc(28,7) }),
        var(filename, "nl1", new LineCol[] { lc(18,11), lc(19,14) }),
        var(filename, "nl2", new LineCol[] { lc(18,25) }),
        var(filename, "nl3", new LineCol[] { lc(18,35) }),
        var(filename, "j", new LineCol[] { lc(5,12), lc(28,13), lc(28,17) }),
    };
    
    public static Test suite() throws Exception
    {
        return new Rename6();
    }
    
    public Rename6() throws Exception
    {
        startTests("Renaming local variables: nested derived type and namelists");
        for (Ident v : vars)
            addSuccessTests(v, "zzzzz");
        endTests();
    }
}
