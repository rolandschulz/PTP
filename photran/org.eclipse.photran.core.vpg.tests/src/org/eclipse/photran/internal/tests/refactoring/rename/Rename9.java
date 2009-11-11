package org.eclipse.photran.internal.tests.refactoring.rename;

import junit.framework.Test;

import org.eclipse.photran.internal.core.util.LineCol;

public class Rename9 extends RenameTestSuite
{
    private String filename = "rename9.f90";
    
    private Ident i1 = var(filename, "i1", new LineCol[] { lc(3,14), lc(36,12) });
    private Ident j1 = var(filename, "j1", new LineCol[] { lc(4,23) });
    private Ident k1 = var(filename, "k1", new LineCol[] { lc(5,14), lc(7,31) });
    private Ident f1 = var(filename, "f1", new LineCol[] { lc(12,20), lc(12,26), lc(36,24) });
    private Ident i2 = var(filename, "i2", new LineCol[] { lc(17,14) });
    private Ident j2 = var(filename, "j2", new LineCol[] { lc(18,22), lc(39,16) });
    private Ident k2 = var(filename, "k2", new LineCol[] { lc(20,13), lc(22,31), lc(39,20) });
    private Ident f2 = var(filename, "f2", new LineCol[] { lc(27,20), lc(27,26) });
    private Ident j1implicit = var(filename, "j1", new LineCol[] { lc(36,16) });
    private Ident k1implicit = var(filename, "k1", new LineCol[] { lc(36,20) });
    private Ident zimplicit = var(filename, "z", new LineCol[] { lc(36,30) });
    private Ident i2implicit = var(filename, "i2", new LineCol[] { lc(39,12) });
    private Ident f2implicit = var(filename, "f2", new LineCol[] { lc(39,29) });
    
    // Note that the order here determines the order in the expectSuccess matrix below
    private Ident[] allVars = new Ident[]
    {
        i1, j1, k1, f1, i2, j2, k2, f2, j1implicit, k1implicit, zimplicit, i2implicit, f2implicit
    };

    private boolean[][] expectSuccess = new boolean[][]
      {
          // IMPORTANT:
          // * Subprogram arguments cannot be renamed
          // * x cannot be renamed to x
          // * j1 and j1implicit have the SAME NAME, so their columns should be identical (same for k1, etc.)
          
          /* vvv can be renamed to >>>     i1     j1     k1     f1     i2     j2     k2     f2     j1implicit k1implicit zimplicit i2implicit f2implicit   
          /* i1         */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* j1         */ new boolean[] { false, false, false, false, true,  true,  true,  true,  false,     false,     true,     true,      true  },
          /* k1         */ new boolean[] { false, false, false, false, true,  true,  true,  true,  false,     false,     false,    true,      true  },
          /* f1         */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* i2         */ new boolean[] { true,  true,  true,  true,  false, false, false, false, true,      true,      true,     false,     false },
          /* j2         */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* k2         */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* f2         */ new boolean[] { true,  true,  true,  true,  false, false, false, false, true,      true,      true,     false,     false },
          /* j1implicit */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* k1implicit */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* zimplicit  */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* i2implicit */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
          /* f2implicit */ new boolean[] { false, false, false, false, false, false, false, false, false,     false,     false,    false,     false },
      };

    public static Test suite() throws Exception
    {
        return new Rename9();
    }
    
    public Rename9() throws Exception
    {
        startTests("Renaming in module with interfaces and visibilities");
        for (int i = 0; i < allVars.length; i++)
        {
            addSuccessTests(allVars[i], "something_different");
            for (int j = 0; j < allVars.length; j++)
            {
                if (expectSuccess[i][j])
                    addSuccessTests(allVars[i], allVars[j].getName());
                else
                    addPreconditionTests(allVars[i], allVars[j].getName());
            }
        }
        endTests();
    }
}
