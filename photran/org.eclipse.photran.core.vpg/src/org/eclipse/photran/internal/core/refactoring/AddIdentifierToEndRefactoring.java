/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;

/**
 * Refactoring to add the identifier to an END statement in Fortran files.
 *
 * @author Matthew Fotzler
 */
public class AddIdentifierToEndRefactoring extends FortranResourceRefactoring
{
    @Override
    public String getName()
    {
        return Messages.AddIdentifierToEndRefactoring_Name;
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if (ast == null)
                    status.addError(Messages.bind(Messages.AddIdentifierToEndRefactoring_SelectedFileCannotBeParsed, file.getName()));
                
                makeChangesTo(file, ast, status, pm);
                vpg.releaseAST(file);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm)
    {
        if (ast == null) return;
        
        ast.accept(new ReplaceEndTokenVisitor());
        addChangeFromModifiedAST(file, pm);
    }

    private final class ReplaceEndTokenVisitor extends ASTVisitor
    {
        @Override
        public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
        {
            if (!node.getEndSubroutineStmt().hasEndSubroutine())
            {
                node.getEndSubroutineStmt().accept(new ASTVisitor()
                {
                    @Override
                    public void visitToken(Token token)
                    {
                        if(token.getTerminal() == Terminal.T_END)
                        {
                            token.replaceWith(new Token(Terminal.T_ENDSUBROUTINE,
                                token.getWhiteBefore(),
                                "end subroutine", //$NON-NLS-1$
                                "")); //$NON-NLS-1$
                        }
                    }
                });
            }
            
            String identifier = node.getSubroutineStmt().getSubroutineName().getSubroutineName().getWhiteBefore() + 
                node.getSubroutineStmt().getSubroutineName().getSubroutineName().getText();
            node.getEndSubroutineStmt().setEndName(new Token(Terminal.T_IDENT, identifier));
            
            traverseChildren(node);
        }

        @Override
        public void visitASTMainProgramNode(ASTMainProgramNode node)
        {
            if(node.getProgramStmt() != null)
                replaceEndProgramStmt(node);
            
            traverseChildren(node);
        }

        private void replaceEndProgramStmt(ASTMainProgramNode node)
        {
            if(!node.getEndProgramStmt().hasEndProgram())
            {
                node.getEndProgramStmt().accept(new ASTVisitor()
                {
                    @Override
                    public void visitToken(Token token)
                    {
                        if(token.getTerminal() == Terminal.T_END)
                        {
                            token.replaceWith(new Token(Terminal.T_ENDPROGRAM,
                                token.getWhiteBefore(),
                                "end program", //$NON-NLS-1$
                                "")); //$NON-NLS-1$ 
                        }
                    }
                });
            }
            
            String identifier = node.getProgramStmt().getProgramName().getProgramName().getWhiteBefore() +
                node.getProgramStmt().getProgramName().getProgramName().getText();
            node.getEndProgramStmt().setEndName(new Token(Terminal.T_ENDPROGRAM, identifier));
        }

        @Override
        public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
        {
            if(!node.getEndFunctionStmt().hasEndFunction())
            {
                node.getEndFunctionStmt().accept(new ASTVisitor()
                {
                    @Override
                    public void visitToken(Token token)
                    {
                        if(token.getTerminal() == Terminal.T_END)
                        {
                            token.replaceWith(new Token(Terminal.T_ENDFUNCTION,
                                token.getWhiteBefore(),
                                "end function", //$NON-NLS-1$
                                "")); //$NON-NLS-1$
                        }
                    }
                });
            }
            
            String identifier = node.getFunctionStmt().getFunctionName().getFunctionName().getWhiteBefore() + 
                node.getFunctionStmt().getFunctionName().getFunctionName().getText();
            node.getEndFunctionStmt().setEndName(new Token(Terminal.T_ENDFUNCTION, identifier));
            
            traverseChildren(node);
        }

        @Override
        public void visitASTModuleNode(ASTModuleNode node)
        {
            if(!node.getEndModuleStmt().hasEndModule())
            {
                node.accept(new ASTVisitor()
                {
                    @Override
                    public void visitToken(Token token)
                    {
                        if(token.getTerminal() == Terminal.T_END)
                        {
                            token.replaceWith(new Token(Terminal.T_ENDMODULE,
                                token.getWhiteBefore(),
                                "end module", //$NON-NLS-1$
                                "")); //$NON-NLS-1$
                        }
                    }
                });
            }
            
            String identifier = ((ASTModuleNode)node).getModuleStmt().getModuleName().getModuleName().getWhiteBefore() +
                ((ASTModuleNode)node).getModuleStmt().getModuleName().getModuleName().getText();
            ((ASTModuleNode)node).getEndModuleStmt().setEndName(new Token(Terminal.T_ENDMODULE, identifier));
            
            traverseChildren(node);
        }
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    }
}
