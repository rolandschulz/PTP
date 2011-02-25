/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.List;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.FixedFormReplacement;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.FortranIncludeDirective;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
import org.eclipse.photran.internal.core.parser.ISpecificationPartConstruct;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;

/**
 * This class is for documentation only.  It provides several code snippets that illustrate how
 * to find things in a Photran AST and how to modify source source code using an AST.
 * 
 * @author Jeff Overbey
 */
// Why is this documentation here rather than the Photran Developer's Guide?  Two reasons.
// 1. This Java code actually gets compiled.  Examples in a PDF file don't.
// 2. When Eclipse renames classes and methods, it also updates links in JavaDoc.  It does not
//    refactor examples in a PDF file.
@SuppressWarnings("all")
public class _AST_VPG_HOWTO
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TRAVERSAL & SEARCH
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void iterateThroughAllTokensIn(IASTNode node)
    {
        node.accept(new ASTVisitor()
        {
            @Override public void visitToken(Token token)
            {
                // Do something with token
            }
        });
    }
    
    void iterateThroughAllSubroutinesIn(IASTNode node)
    {
        node.accept(new ASTVisitor()
        {
            @Override public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
            {
                // Do something with node
            }
        });
    }
    
    void iterateThroughAllDoLoopsIn(IFortranAST ast)
    {
        // See the Photran Developer's Guide: If you want an AST to have a "proper" structure
        // for DO-loops, you must call this method:
        LoopReplacer.replaceAllLoopsIn(ast.getRoot());
        
        // ...and then subclass from a different visitor class:
        ast.accept(new ASTVisitorWithLoops()
        {
            @Override public void visitASTProperLoopConstructNode(ASTProperLoopConstructNode node)
            {
                // Do something with node
            }
        });
    }

    void findFirstTokenIn(IASTNode node)
    {
        Token token = node.findFirstToken(); // Returns null if the node contains no tokens
    }

    void findRootOfTheAST(IASTNode node)
    {
        ASTExecutableProgramNode root = node.findNearestAncestor(ASTExecutableProgramNode.class);
    }

    void findLastSpecificationStmtIn(ASTSubroutineSubprogramNode subroutine)
    {
        ISpecificationPartConstruct lastStmt =
            subroutine.getBody().findLast(ISpecificationPartConstruct.class);
            // May be null if there are no specification statements
    }

    void findFirstExecutableStmtIn(ASTSubroutineSubprogramNode subroutine)
    {
        IExecutableConstruct firstStmt =
            subroutine.getBody().findFirst(IExecutableConstruct.class);
            // May be null if there are no executable statements
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // WHITETEXT (COMMENTS, WHITESPACE) AND SOURCE CODE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void printTheSourceCodeFromAnASTNode(IASTNode node)
    {
        System.out.println(node);
    }
    
    void getCommentsAndWhitespacePreceding(IASTNode node)
    {
        String commentsAndWhitespace = node.findFirstToken().getWhiteBefore();
    }
    
    void getCommentsAndWhitespaceAtEndOfFile(IFortranAST ast)
    {
        String commentsAndWhitespace;
        
        Token lastTokenInFile = ast.getRoot().findLastToken();
        if (lastTokenInFile == null)
            commentsAndWhitespace = "";
        else
            commentsAndWhitespace = lastTokenInFile.getWhiteAfter();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PREPROCESSOR DIRECTIVES
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void dealWithPreprocessing(Token token)
    {
        if (token.getPreprocessorDirective() == null)
        {
            // No preprocessing; the token looks exactly the same in the user's source code
        }
        else if (token.getPreprocessorDirective() instanceof FixedFormReplacement)
        {
            // Token has spaces in the middle of it which were removed by the fixed form prepass
        }
        else if (token.getPreprocessorDirective() instanceof FortranIncludeDirective)
        {
            // Token is in a file included via a Fortran INCLUDE line
        }
        /*
        else if (token.getPreprocessorDirective() instanceof CPreprocessorReplacement)
        {
            // Token is in a file #included via the C preprocessor,
            // or it is the result of a C preprocessor macro expansion,
            // or it is contains trigraphs that were replaced by the C preprocessor,
            // or something like that.
        }
        */
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // NAMES, SCOPES, & BINDINGS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void findScopeOf(IASTNode node)
    {
        ScopingNode scope = node.findNearestAncestor(ScopingNode.class);
        
        // Note that several different AST nodes are subclasses of ScopingNode --
        // ASTExecutableProgramNode (the root of the AST), ASTMainProgramNode,
        // ASTFunctionSubprogramNode, etc. -- so any of these may be returned.
    }

    void findSubroutineContaining(IASTNode node)
    {
        ASTSubroutineSubprogramNode subroutine =
            node.findNearestAncestor(ASTSubroutineSubprogramNode.class);
            // Returns null if there is no enclosing subroutine
        
        /* Note that if this is invoked on part of "INTEGER :: I" in
         * SUBROUTINE S()
         * CONTAINS
         *     FUNCTION F()
         *         INTEGER :: I
         *     END FUNCTION
         * END SUBROUTINE
         * this will return the ASTSubroutineSubprogramNode for S.
         */
    }
    
    void findAllSymbolsDefinedInTheScopeOf(ScopingNode scope)
    {
        for (Definition symbol : scope.getAllDefinitions())
            if (symbol != null)
                System.out.println(symbol.getCanonicalizedName());
    }
    
    void findAllPublicallyAccessibleSymbolsDefinedIn(ScopingNode scope)
    {
        for (Definition symbol : scope.getAllPublicDefinitions())
            System.out.println(symbol.getCanonicalizedName());
    }
    
    void determineWhatSymbolIsDefinedBy(Token identifier)
    {
        if (identifier.isIdentifier()) // same as identifier.getTerminal() == Terminal.T_IDENT
        {
            List<Definition> definitions = identifier.resolveBinding();
            
            if (definitions.isEmpty())
            {
                // The symbol was not declared
            }
            else if (definitions.size() > 1)
            {
                // The symbol was declared multiple times, i.e., it is ambiguous
            }
            else
            {
                Definition symbol = definitions.get(0);
            }
        }
    }
    
    void findAllPlacesWhereSymbolIsUsed(Definition symbol)
    {
        for (PhotranTokenRef location : symbol.findAllReferences(true))
        {
            Token reference = location.findToken();
        }
    }
    
    void determineIfASymbolHasThePARAMETERAttribute(Definition symbol)
    {
        if (symbol.isParameter())
            System.out.println(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // REWRITING
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void makeCopyOf(IASTNode node)
    {
        IASTNode copy = (IASTNode)node.clone();
    }
    
    void changeTheTextOfAToken(Token token)
    {
        token.setText("whatever");
    }
    
    void replacePartOfAnAST()
    {
        IASTNode node1 = whatever(), node2 = whatever();
        
        node1.replaceWith(node2);
        
        node1.replaceWith("You can also replace a node with a string, like this.");
    }
    
    void deletePartOfAnAST(IASTNode node)
    {
        node.removeFromTree();
    }

    private IASTNode whatever() { throw new Error(); }
}
