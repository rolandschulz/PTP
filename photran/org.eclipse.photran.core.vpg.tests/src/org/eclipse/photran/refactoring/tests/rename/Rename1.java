package org.eclipse.photran.refactoring.tests.rename;

import junit.framework.Test;

import org.eclipse.photran.core.vpg.util.LineCol;

public class Rename1 extends RenameTestSuite
{
    ///////////////////////////////////////////////////////////////////////////
    //
    // RECORD POSITIONS OF ALL IDENTIFIERS IN RENAME1.F90, AND
    // GROUP THEM ACCORDING TO WHICH ONES SHOULD BE RENAMED TOGETHER
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private String filename = "rename1.f90";
    
    private Ident intInMain = var(filename, "int", new LineCol[] { lc(11,10), lc(17,16), lc(23,20) });
    private Ident subInMain = var(filename, "sub", new LineCol[] { lc(24,16), lc(25,20) });
    
    private Ident[] mainVars = new Ident[] {
        var(filename, "two", new LineCol[] { lc(2,27), lc(7,40), lc(9,23) }),
        var(filename, "three", new LineCol[] { lc(3,27), lc(4,23), lc(5,13), lc(6,15), lc(7,33), lc(7,66), lc(9,28) }),
        var(filename, "a", new LineCol[] { lc(4,16), lc(9,14) }),
        var(filename, "b", new LineCol[] { lc(4,19), lc(9,17) }),
        var(filename, "not_shadowed", new LineCol[] { lc(5,23), lc(9,35), lc(21,16) }),
        var(filename, "c", new LineCol[] { lc(6,25), lc(9,20) }),
        var(filename, "shadow_this_1", new LineCol[] { lc(7,16), lc(9,49) }),
        var(filename, "shadow_this_2", new LineCol[] { lc(7,46), lc(9,64) }),
        var(filename, "implicit", new LineCol[] { lc(8,5), lc(9,79), lc(21,64) }),
        intInMain
    };
    
    private Ident shadowThis1IntShadow = var(filename, "shadow_this_1", new LineCol[] { lc(18,18), lc(21,30) });
    private Ident shadowThis2IntShadow = var(filename, "shadow_this_2", new LineCol[] { lc(19,18), lc(21,45) });
    
    private Ident main = var(filename, "Main", new LineCol[] { lc(1,9), lc(26,13) });
    private Ident ext = var(filename, "ext", new LineCol[] { lc(12,10), lc(22,12), lc(27,12), lc(33,16) });
    
    private Ident twoInExt = var(filename, "two", new LineCol[] { lc(28,14), lc(31,18) });
    
    private Ident intContained = var(filename, "int", new LineCol[] { lc(30,16), lc(32,20) });
    
    private Ident[] otherVars = new Ident[] { shadowThis1IntShadow, shadowThis2IntShadow, main, ext, twoInExt, intContained };
    
    private Ident intrinsic = var(filename, "selected_real_kind", new LineCol[] { lc(3,35) });
    
    ///////////////////////////////////////////////////////////////////////////
    //
    // TEST CASES
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public static Test suite() throws Exception
    {
        return new Rename1();
    }
    
    public Rename1() throws Exception
    {
        addSuccessTests();
        addFailNotAnIdentifier();
        addFailIntrinsicProcedure();
        addFailConflictInSameScope();
        addFailConflictInReference();
    }
    
    public void addSuccessTests()
    {
        startTests("Renaming local variables in main program");
        for (Ident v : mainVars)
        {
            addSuccessTests(v, v.getName() + v.getName());
            addSuccessTests(v, "z");
            addSuccessTests(v, "if");
        }
        endTests();
        
        startTests("Renaming variables outside main program");
        for (Ident v : otherVars)
        {
            addSuccessTests(v, v.getName() + v.getName());
            addSuccessTests(v, "z");
            addSuccessTests(v, "if");
        }
        endTests();
    }

    public void addFailNotAnIdentifier() throws Exception
    {
        startTests("Check: Not an identifier");
        for (Ident v : mainVars)
        {
            addPreconditionTests(v, "");
            addPreconditionTests(v, "\u0080"); // Euro symbol
            addPreconditionTests(v, "3");
            addPreconditionTests(v, "hello world");
            addPreconditionTests(v, "\"hello world\"");
        }
        endTests();
    }
    
    public void addFailIntrinsicProcedure() throws Exception
    {
        startTests("Check: Cannot rename intrinsic procedures");
        addPreconditionTests(intrinsic, "new_name");
        endTests();
    }
    
    public void addFailConflictInSameScope() throws Exception
    {
        startTests("Check: Local variable name conflicts in local scope (main program)");
        for (int i = 0; i < mainVars.length; i++)
            addPreconditionTests(mainVars[i], mainVars[Math.max(i-1, 0)].getName());
        endTests();

        startTests("Check: Local variable name conflicts with capitalization changed");
        for (int i = 0; i < mainVars.length; i++)
            addPreconditionTests(mainVars[i], mainVars[Math.max(i-1, 0)].getName().toUpperCase());
        endTests();
    }
    
    public void addFailConflictInReference() throws Exception
    {
        startTests("Check: Conflicts outside local scope");
        addSuccessTests(main, "int");
        addPreconditionTests(main, "ext");
        addSuccessTests(intInMain, "Main");
        addPreconditionTests(intInMain, "ext");
        addPreconditionTests(subInMain, "int");
        addPreconditionTests(subInMain, "ext");
        addPreconditionTests(ext, "int"); // conflict due to reference in Main
        addPreconditionTests(ext, "sub"); // will change interpretation of ext call in Main
        addPreconditionTests(ext, "Main");
        addSuccessTests(intContained, "sub");
        //addSuccessTests(intContained, "Main"); // TODO: This is a special case; won't worry about it
        endTests();
    }
}
