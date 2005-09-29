package org.eclipse.photran.internal.core.refactoring.preconditions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.core.programrepresentation.Presentation;
import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;

/**
 * A precondition for a Fortran refactoring
 * 
 * @author joverbey
 */
public abstract class AbstractPrecondition extends AbstractPreconditionContainer
{
    protected ParseTreeNode parseTree;

    protected Presentation presentation;

    protected SymbolTable symbolTable;

    protected List/* <Precondition> */prereqPreconditions = new LinkedList();

    protected boolean prereqPreconditionsCheckedAndPassed = false;

    protected boolean thisPreconditionCheckedAndPassed = false;
    
    public AbstractPrecondition()
    {
    }

    public AbstractPrecondition(Program program)
    {
        if (program != null)
        {
            this.parseTree = program.getParseTree();
            this.presentation = program.getPresentation();
            this.symbolTable = program.getSymbolTable();
        }
    }

    public final boolean check()
    {
        if (thisPreconditionCheckedAndPassed)
            return true;
        else
        {
            thisPreconditionCheckedAndPassed = checkPreconditions(prereqPreconditions)
                && checkThisPrecondition();
            return thisPreconditionCheckedAndPassed;
        }
    }

    /**
     * Subclasses are expected to override this method, which is called only after all prerequisite
     * preconditions have been met.
     * 
     * @return true iff this precondition has been met
     */
    protected abstract boolean checkThisPrecondition();

    protected String describeTokenPos(Token token)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append(token.getFilename());
        sb.append(", line ");
        sb.append(token.getStartLine());
        sb.append(", column ");
        sb.append(token.getStartCol());
        sb.append(")");
        return sb.toString();
    }

    public final boolean hasBeenCheckedAndPassed()
    {
        return thisPreconditionCheckedAndPassed;
    }
}
