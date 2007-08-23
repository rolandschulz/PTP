package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

public class Rename8 extends RenameTestSuite
{
    private String filename = "rename8.f90";
    
    private Ident f = var(filename, "f", new LineCol[] { lc(4,10), lc(28,14) });
    private Ident g_paramOfF = var(filename, "g", new LineCol[] { lc(4,12), lc(11,31), lc(23,18) });
    private Ident q = var(filename, "q", new LineCol[] { lc(4,22), lc(7,23), lc(26,3) });
    private Ident h = var(filename, "h", new LineCol[] { lc(11,33), lc(15,35), lc(18,22) });
    private Ident x_paramOfG = var(filename, "x", new LineCol[] { lc(11,36), lc(21,39) });
    private Ident x_paramOfH = var(filename, "x", new LineCol[] { lc(15,37), lc(17,43) });
    private Ident g_function = var(filename, "g", new LineCol[] { lc(30,18), lc(30,23), lc(30,43) });
    
    // Note that the order here determines the order in the expectSuccess matrix below
    private Ident[] allVars = new Ident[]
    {
        f, g_paramOfF, q, h, x_paramOfG, x_paramOfH, g_function
    };

    private boolean[][] expectSuccess = new boolean[][]
      {
          // IMPORTANT:
          // * Subprogram arguments cannot be renamed
          // * x cannot be renamed to x
          
          /* vvv can be renamed to >>>     f      g_paramOfF q      h      x_paramOfG x_paramOfH g_function   
          /* f          */ new boolean[] { false, false,     true,  true,  true,      true,      false },
          /* g_paramOfF */ new boolean[] { false, false,     false, false, false,     false,     false },
          /* q          */ new boolean[] { true,  false,     false, true,  true,      true,      false },
          /* h          */ new boolean[] { false, false,     false, false, false,     false,     false },
          /* x_paramOfG */ new boolean[] { false, false,     false, false, false,     false,     false },
          /* x_paramOfH */ new boolean[] { false, false,     false, false, false,     false,     false },
          /* g_function */ new boolean[] { false, false,     true,  true,  true,      true,      false },
      };

    public static Test suite() throws Exception
    {
        return new Rename8();
    }
    
    public Rename8() throws Exception
    {
        startTests("Renaming subroutine arguments");
        for (int i = 0; i < allVars.length; i++)
        {
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
