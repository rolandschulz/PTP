package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

public class Rename3 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN RENAME3*.F90
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String file = "rename3.f90";
    private String filea = "rename3a.f90";
    private String fileb = "rename3b.f90";
    private String filec = "rename3c.f90";

    private Ident myProgram = var("MyProgram");
    private Ident aRenamed3 = var("a_renamed3");
    private Ident bRenamed3 = var("b_renamed3");
    private Ident contained = var("contained");
    private Ident external = var("external");
    
    private Ident moduleA = var("module_a");
    private Ident aSub1of3 = var("a_sub1of3");
    private Ident aSub2of3 = var("a_sub2of3");
    private Ident aSub3of3 = var("a_sub3of3");
    
    private Ident moduleB = var("module_b");
    private Ident bSub1of3 = var("b_sub1of3");
    private Ident bSub2of3 = var("b_sub2of3");
    private Ident bSub3of3 = var("b_sub3of3");
    
    private Ident moduleC = var("module_c");
    private Ident cSub = var("c_sub");
    
    // Note that the order here determines the order in the expectSuccess matrix below
    private Ident[] allVars = new Ident[]
    {
        myProgram, aRenamed3, bRenamed3, contained, external,
        moduleA, aSub1of3, aSub2of3, aSub3of3,
        moduleB, bSub1of3, bSub2of3, bSub3of3,
        moduleC, cSub
    };
    
    private boolean[][] expectSuccess = new boolean[][]
    {
        // IMPORTANT:
        // * Modules can't be renamed, hence the rows of "false" for moduleA, moduleB, and moduleC
        // * The rename refactoring requires that the new name not be identical to the old name, hence "false" along the diagonal
        
        /* vvv can be renamed to >>>    myProgram, aRenamed3, bRenamed3, contained, external, moduleA, aSub1of3, aSub2of3, aSub3of3, moduleB, bSub1of3, bSub2of3, bSub3of3, moduleC, cSub
        /* myProgram */ new boolean[] { false,     false,     false,     false,     false,    true,    false,    false,    true,     true,    true,     false,    true,     true,    true  },
        /* aRenamed3 */ new boolean[] { false,     false,     false,     false,     false,    true,    false,    false,    true,     true,    true,     false,    true,     true,    true  },
        /* bRenamed3 */ new boolean[] { false,     false,     false,     false,     false,    true,    false,    false,    true,     true,    true,     false,    true,     true,    true  },
        /* contained */ new boolean[] { false,     false,     false,     false,     false,    true,    false,    false,    true,     true,    true,     false,    true,     true,    true  },
        /* external  */ new boolean[] { false,     false,     false,     false,     false,    true,    false,    false,    true,     true,    true,     false,    true,     true,    true  },
        /* moduleA   */ new boolean[] { false,     false,     false,     false,     false,    false,   false,    false,    false,    false,   false,    false,    false,    false,   false },
        /* aSub1of3  */ new boolean[] { false,     false,     false,     false,     false,    false,   false,    false,    false,    true,    true,     false,    true,     true,    true  },
        /* aSub2of3  */ new boolean[] { false,     false,     false,     false,     false,    false,   false,    false,    false,    true,    true,     false,    true,     true,    true  },
        /* aSub3of3  */ new boolean[] { true,      true,      true,      true,      true,     false,   false,    false,    false,    true,    true,     true,     true,     true,    true  },
        /* moduleB   */ new boolean[] { false,     false,     false,     false,     false,    false,   false,    false,    false,    false,   false,    false,    false,    false,   false },
        /* bSub1of3  */ new boolean[] { true,      true,      true,      true,      true,     true,    true,     true,     true,     false,   false,    false,    false,    true,    false },
        /* bSub2of3  */ new boolean[] { false,     false,     false,     false,     false,    true,    false,    false,    true,     false,   false,    false,    false,    true,    false,},
        /* bSub3of3  */ new boolean[] { true,      true,      true,      true,      true,     true,    true,     true,     true,     false,   false,    false,    false,    true,    false },
        /* moduleC   */ new boolean[] { false,     false,     false,     false,     false,    false,   false,    false,    false,    false,   false,    false,    false,    false,   false },
        /* cSub      */ new boolean[] { true,      true,      true,      true,      true,     true,    true,     true,     true,     false,   false,    false,    false,    false,   false },
    };
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new Rename3();
    }
    
    public Rename3() throws Exception
    {
        myProgram.addReferences(file, new LineCol[] { lc(1,9) });
        aRenamed3.addReferences(file, new LineCol[] { lc(2,17), lc(9,8) });
        bRenamed3.addReferences(file, new LineCol[] { lc(3,34), lc(11,8) });
        contained.addReferences(file, new LineCol[] { lc(15,14), lc(12,8) });
        external.addReferences(file, new LineCol[] { lc(18,12), lc(13,8) });
        moduleA.addReferences(file, new LineCol[] { lc(2,7) });
        aSub1of3.addReferences(file, new LineCol[] { lc(7,8) });
        aSub2of3.addReferences(file, new LineCol[] { lc(8,8) });
        aSub3of3.addReferences(file, new LineCol[] { lc(2,31) });
        moduleB.addReferences(file, new LineCol[] { lc(3,7) });
        bSub1of3.addReferences(file, new LineCol[] {});
        bSub2of3.addReferences(file, new LineCol[] { lc(3, 23), lc(10,8) });
        bSub3of3.addReferences(file, new LineCol[] { lc(3,48) });
        moduleC.addReferences(file, new LineCol[] {});
        cSub.addReferences(file, new LineCol[] {});
        
        myProgram.addReferences(filea, new LineCol[] {});
        aRenamed3.addReferences(filea, new LineCol[] {});
        bRenamed3.addReferences(filea, new LineCol[] {});
        contained.addReferences(filea, new LineCol[] {});
        external.addReferences(filea, new LineCol[] {});
        moduleA.addReferences(filea, new LineCol[] { lc(1,8) });
        aSub1of3.addReferences(filea, new LineCol[] { lc(3,16) });
        aSub2of3.addReferences(filea, new LineCol[] { lc(4,16) });
        aSub3of3.addReferences(filea, new LineCol[] { lc(5,16) });
        moduleB.addReferences(filea, new LineCol[] {});
        bSub1of3.addReferences(filea, new LineCol[] {});
        bSub2of3.addReferences(filea, new LineCol[] {});
        bSub3of3.addReferences(filea, new LineCol[] {});
        moduleC.addReferences(filea, new LineCol[] {});
        cSub.addReferences(filea, new LineCol[] {});
        
        myProgram.addReferences(fileb, new LineCol[] {});
        aRenamed3.addReferences(fileb, new LineCol[] {});
        bRenamed3.addReferences(fileb, new LineCol[] {});
        contained.addReferences(fileb, new LineCol[] {});
        external.addReferences(fileb, new LineCol[] {});
        moduleA.addReferences(fileb, new LineCol[] {});
        aSub1of3.addReferences(fileb, new LineCol[] {});
        aSub2of3.addReferences(fileb, new LineCol[] {});
        aSub3of3.addReferences(fileb, new LineCol[] {});
        moduleB.addReferences(fileb, new LineCol[] { lc(1,8) });
        bSub1of3.addReferences(fileb, new LineCol[] { lc(4,16) });
        bSub2of3.addReferences(fileb, new LineCol[] { lc(5,16) });
        bSub3of3.addReferences(fileb, new LineCol[] { lc(6,16) });
        moduleC.addReferences(fileb, new LineCol[] { lc(2,9) });
        cSub.addReferences(fileb, new LineCol[] { lc(7,12) });
        
        myProgram.addReferences(filec, new LineCol[] {});
        aRenamed3.addReferences(filec, new LineCol[] {});
        bRenamed3.addReferences(filec, new LineCol[] {});
        contained.addReferences(filec, new LineCol[] {});
        external.addReferences(filec, new LineCol[] {});
        moduleA.addReferences(filec, new LineCol[] {});
        aSub1of3.addReferences(filec, new LineCol[] {});
        aSub2of3.addReferences(filec, new LineCol[] {});
        aSub3of3.addReferences(filec, new LineCol[] {});
        moduleB.addReferences(filec, new LineCol[] {});
        bSub1of3.addReferences(filec, new LineCol[] {});
        bSub2of3.addReferences(filec, new LineCol[] {});
        bSub3of3.addReferences(filec, new LineCol[] {});
        moduleC.addReferences(filec, new LineCol[] { lc(1,8) });
        cSub.addReferences(filec, new LineCol[] { lc(3,16) });

        startTests("Renaming subroutines imported from modules");
        for (int i = 0; i < allVars.length; i++)
        {
            for (int j = 0; j < allVars.length; j++)
            {
//        for (int i = 6; i == 6; i++)
//        {
//            for (int j = 1; j == 1; j++)
//            {
                if (expectSuccess[i][j])
                    addSuccessTests(allVars[i], allVars[j].getName());
                else
                    addPreconditionTests(allVars[i], allVars[j].getName());
            }
        }
        endTests();
    }
}
