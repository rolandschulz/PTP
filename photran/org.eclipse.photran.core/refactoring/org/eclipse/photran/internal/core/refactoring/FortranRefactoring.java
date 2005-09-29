package org.eclipse.photran.internal.core.refactoring;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.core.programrepresentation.Presentation;
import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.refactoring.preconditions.AbstractPreconditionContainer;

/**
 * A generic Fortran refactoring
 * 
 * Subclasses are expected to add <code>Precondition</code>s to the
 * <code>initialPreconditions</code> and <code>finalPreconditions</code> lists.
 * 
 * @author joverbey
 */
public abstract class FortranRefactoring extends AbstractPreconditionContainer
{
    protected final List/* <Precondition> */initialPreconditions = new LinkedList();

    protected final List/* <Precondition> */finalPreconditions = new LinkedList();

    protected boolean initialPreconditionsCheckedAndPassed = false;

    protected boolean finalPreconditionsCheckedAndPassed = false;

    protected final Program program;

    protected final ParseTreeNode parseTree;

    protected final Presentation presentation;

    protected final SymbolTable symbolTable;

    public FortranRefactoring(Program program)
    {
        this.program = program;
        this.parseTree = program.getParseTree();
        this.presentation = program.getPresentation();
        this.symbolTable = program.getSymbolTable();
    }

    /**
     * Checks the initial preconditions for the refactoring, i.e., those that can be checked before
     * getting any input from the user.
     * 
     * Subclasses are not allowed to override this; they should set their initial preconditions by
     * adding them to the <code>initialPreconditions</code> list.
     * 
     * @return true iff all initial preconditions pass
     */
    public final boolean checkInitialPreconditions()
    {
        initialPreconditionsCheckedAndPassed = checkPreconditions(initialPreconditions);
        return initialPreconditionsCheckedAndPassed;
    }

    /**
     * Checks the final preconditions for the refactoring, i.e., those that can be checked only
     * after input has been received from the user. If no user input is required, there may be no
     * final preconditions.
     * 
     * Subclasses are not allowed to override this; they should set their final preconditions by
     * adding them to the <code>finalPreconditions</code> list.
     * 
     * @return true iff all final preconditions pass
     */
    public final boolean checkFinalPreconditions()
    {
        if (!initialPreconditionsCheckedAndPassed)
            throw new Error("Must check and pass initial preconditions before final preconditions");

        finalPreconditionsCheckedAndPassed = checkPreconditions(finalPreconditions);
        return finalPreconditionsCheckedAndPassed;
    }

    /**
     * Preforms the refactoring.
     * 
     * @return true iff the refactoring succeeded
     */
    public abstract boolean perform();

    /**
     * Calls <code>program.rebuildSymbolTable()</code>, returning <code>true</code> iff it
     * succeeds. If it fails, the <code>error</code> field will be set appropriately.
     * 
     * @return true iff successful
     */
    protected boolean rebuildSymbolTable()
    {
        try
        {
            program.rebuildSymbolTable();
            return true;
        }
        catch (Exception e)
        {
            error = "Error rebuilding symbol table\n\n" + e.getMessage();
            return false;
        }
    }
}
