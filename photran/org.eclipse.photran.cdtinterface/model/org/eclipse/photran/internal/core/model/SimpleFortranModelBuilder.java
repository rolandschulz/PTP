/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.FixedFormLexerPhase2;
import org.eclipse.photran.internal.core.lexer.FreeFormLexerPhase1;
import org.eclipse.photran.internal.core.lexer.FreeFormLexerPhase2;
import org.eclipse.photran.internal.core.lexer.ILexer;
import org.eclipse.photran.internal.core.lexer.IToken;
import org.eclipse.photran.internal.core.lexer.SimpleToken;
import org.eclipse.photran.internal.core.lexer.SimpleTokenFactory;
import org.eclipse.photran.internal.core.lexer.Terminal;

/**
 * This is a minimal Fortran model builder based only on lexical analysis.  See
 * {@link IFortranModelBuilder}.
 * <p>
 * <i>This is probably <b>not</b> the model builder you are looking for.</i>  Generally, Photran
 * uses a parser-based model builder from the cdtinterface.vpg plug-in instead.  This one is used
 * only if the VPG plug-ins are not available, or if the user specifically chooses to use this
 * model builder instead (in the workspace preferences).
 * <p>
 * Editors can force the model builder to use fixed or free format for a given file by calling
 * {@link #setIsFixedForm(boolean)}.  Otherwise, the format is determined by content type (i.e., by
 * the filename extension and the user's workspace preferences).
 * 
 * @author Jeff Overbey
 *
 * @see IFortranModelBuilder
 * @see IContributedModelBuilder
 */
@SuppressWarnings("restriction")
public class SimpleFortranModelBuilder implements IFortranModelBuilder
{
    private TranslationUnit translationUnit = null;
    private Map newElements = null;
    private boolean isFixedForm = false;

    public void setTranslationUnit(ITranslationUnit tu)
    {
        if (!(tu instanceof ITranslationUnit)) throw new Error("Unexpected subclass of ITranslationUnit");
        
        this.translationUnit = (TranslationUnit)tu;
    }

    public void setIsFixedForm(boolean isFixedForm)
    {
        this.isFixedForm = isFixedForm;
    }

    public void parse(boolean quickParseMode) throws Exception
    {
        this.newElements = new HashMap();
        
        InputStream in = new ByteArrayInputStream(translationUnit.getBuffer().getContents().getBytes());
        String filename = translationUnit.getElementName();
        boolean wasSuccessful = true;

        try
        {
//            FortranElement note = new FortranElement.UnknownNode(translationUnit, isFixedForm ? "<Fixed Form Source>" : "<Free Form Source>");
//            this.addF90Element(note);

              IFile file = translationUnit.getFile();
              ILexer lexer =
                  isFixedForm ? (ILexer)new FixedFormLexerPhase2(in, file, filename, SimpleTokenFactory.getInstance())
                              : (ILexer)new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, file, filename, SimpleTokenFactory.getInstance(), false));

              createSourceFormNode();
              buildModel(lexer);

                
              /* For whatever reason, we have to only set the *parent* while we
               * parse, then go back through (now) and traverse the model in
               * preorder, executing addChild().  This doesn't make any sense,
               * but the "obvious" way of doing it all while parsing (in
               * BuildModelParserAction.java) doesn't work.
               */
              //newElements.putAll(newElts);
              //this.addF90Elements(newElements);
        }
        catch (Exception e)
        {
            createParseFailureNode(translationUnit, e.getMessage());
            wasSuccessful = false;
        }

        // From CDT: important to know if the unit has parse errors or not
        setIsStructureKnown(wasSuccessful);
    }

    public void setIsStructureKnown(boolean isStructureKnown)
    {
        translationUnit.setIsStructureKnown(isStructureKnown);
    }

    private FortranElement createSourceFormNode() throws CModelException
    {
        String sourceForm = isFixedForm ? "<Fixed Form Source>" : "<Free Form Source>";
        FortranElement element = new FortranElement.UnknownNode(translationUnit, sourceForm);
        translationUnit.addChild(element);
        this.newElements.put(element, element.getElementInfo());
        return element;

    }

    private FortranElement createParseFailureNode(Parent parent, String errorMessage)
        throws CModelException
    {

        FortranElement element = new FortranElement.ErrorNode(parent, errorMessage);
        parent.addChild(element);
        this.newElements.put(element, element.getElementInfo());
        return element;

    }

//    private void addF90Elements(Map elts) throws CModelException
//    {
//        // Add all top-level elements (those with no parent) and recurse
//        addF90ElementsFor(null, elts);
//    }
//
//    private void addF90ElementsFor(Parent parent, Map elts) throws CModelException
//    {
//        Iterator it = elts.keySet().iterator();
//        while (it.hasNext())
//        {
//            FortranElement e = (FortranElement)it.next();
//            if (e.getParent() == parent)
//            {
//                e.setIdentifier(e.getIdentifier()); // It seems to forget position info
//                addF90ElementsFor(e, elts);
//            }
//        }
//    }
    
    // --NODE HIERARCHY--------------------------------------------------
    
    private Stack/*<Parent>*/ stack = new Stack/*<Parent>*/();
    
    private Parent currentParent()
    {
        return stack.isEmpty() ? translationUnit : (Parent)stack.peek();
    }
    
    private void startNesting(FortranElement element) throws CModelException
    {
        //System.out.println("Begin " + element.getElementName());
        
        currentParent().addChild(element);
        newElements.put(element, element.getElementInfo());

        stack.add(element);
    }
    
    private void doneNesting()
    {
        //System.out.println("End");
        
        if (!stack.isEmpty()) stack.pop();
    }

    // --STAREMENT ANALYSIS----------------------------------------------
    
    private ArrayList/*<IToken>*/ statement = new ArrayList/*<IToken>*/(64);

    private boolean readStatement(ILexer lexer) throws Exception
    {
        statement.clear();
        IToken tok;
        do
        {
            tok = lexer.yylex();
            statement.add(tok);
        } while (tok.getTerminal() != Terminal.T_EOS && tok.getTerminal() != Terminal.END_OF_INPUT);
        
        return tok.getTerminal() != Terminal.END_OF_INPUT;
    }
    
    private boolean statementStartsWith(Terminal t)
    {
        boolean startsWithLabel = ((IToken)statement.get(0)).getTerminal() == Terminal.T_ICON;
        int firstTokenIndex = startsWithLabel ? 1 : 0;
        return (((IToken)statement.get(firstTokenIndex)).getTerminal() == t);
    }
    
    private boolean statementContains(Terminal t)
    {
        for (int i = 0, length = statement.size(); i < length; i++)
            if (((IToken)statement.get(i)).getTerminal() == t)
                return true;
        
        return false;
    }
    
    private IToken tokenFollowing(Terminal afterTerminal)
    {
        boolean foundToken = (afterTerminal == null);
        
        for (int i = 0, length = statement.size(); i < length; i++)
        {
            IToken thisToken = (IToken)statement.get(i);
            
            if (thisToken.getTerminal() == afterTerminal)
                foundToken = true;
            else if (foundToken)
                return thisToken;
        }
        
        return new SimpleToken(Terminal.T_IDENT, "???");
    }
    
    private IToken firstIdentifierAfter(Terminal afterTerminal)
    {
        return firstIdentifierAfter(afterTerminal, "(anonymous)");
    }
        
    private IToken firstIdentifierAfter(Terminal afterTerminal, String textIfNotFound)
    {
        boolean foundToken = (afterTerminal == null);
        
        for (int i = 0, length = statement.size(); i < length; i++)
        {
            IToken thisToken = (IToken)statement.get(i);
            
            if (thisToken.getTerminal() == afterTerminal)
                foundToken = true;
            else if (foundToken && thisToken.getTerminal() == Terminal.T_IDENT)
                return thisToken;
        }
        
        return new SimpleToken(Terminal.T_IDENT, textIfNotFound);
    }
    
    private boolean isEndStatement()
    {
        if (statementStartsWith(Terminal.T_ENDBLOCK)
         || statementStartsWith(Terminal.T_ENDBLOCKDATA)
         || statementStartsWith(Terminal.T_ENDFUNCTION)
         || statementStartsWith(Terminal.T_ENDINTERFACE)
         || statementStartsWith(Terminal.T_ENDMODULE)
         || statementStartsWith(Terminal.T_ENDPROGRAM)
         || statementStartsWith(Terminal.T_ENDSUBROUTINE))
         //|| statementStartsWith(Terminal.T_ENDTYPE)
         {
            return true;
         }
         else if (statementStartsWith(Terminal.T_END))
         {
             Terminal next = tokenFollowing(Terminal.T_END).getTerminal();
             
             return next == Terminal.T_BLOCK
                 || next == Terminal.T_BLOCKDATA
                 || next == Terminal.T_FUNCTION
                 || next == Terminal.T_INTERFACE
                 || next == Terminal.T_MODULE
                 || next == Terminal.T_PROGRAM
                 || next == Terminal.T_SUBROUTINE;
                 //|| next == Terminal.T_TYPE;
         }
         else return false;
    }

    // --NODE CREATION METHODS-------------------------------------------
    
    private void buildModel(ILexer lexer) throws Exception
    {
        while (readStatement(lexer))
            processStatement();
    }

    private void processStatement() throws CModelException
    {
        if (isEndStatement()) // Make sure this is checked before the check for T_FUNCTION below
            doneNesting();

        else if (statementStartsWith(Terminal.T_PROGRAM))
            startNesting(new FortranElement.MainProgram(currentParent(), firstIdentifierAfter(Terminal.T_PROGRAM)));
        
        else if (statementStartsWith(Terminal.T_MODULE))
            startNesting(new FortranElement.Module(currentParent(), firstIdentifierAfter(Terminal.T_MODULE)));
        
//        else if (statementStartsWith(Terminal.T_TYPE))
//            startNesting(new FortranElement.Module(currentParent(), firstIdentifierAfter(Terminal.T_MODULE)));
        
        else if (statementContains(Terminal.T_FUNCTION))
            startNesting(new FortranElement.Function(currentParent(), firstIdentifierAfter(Terminal.T_FUNCTION)));
        
        else if (statementStartsWith(Terminal.T_SUBROUTINE))
            startNesting(new FortranElement.Subroutine(currentParent(), firstIdentifierAfter(Terminal.T_SUBROUTINE)));
        
        else if (statementStartsWith(Terminal.T_INTERFACE))
            startNesting(new FortranElement.Interface(currentParent(), firstIdentifierAfter(Terminal.T_INTERFACE, "(Interface Block)")));
        
        else if (statementStartsWith(Terminal.T_BLOCK))
            startNesting(new FortranElement.BlockData(currentParent(), firstIdentifierAfter(Terminal.T_BLOCK)));
        else if (statementStartsWith(Terminal.T_BLOCKDATA))
            startNesting(new FortranElement.BlockData(currentParent(), firstIdentifierAfter(Terminal.T_BLOCKDATA)));
    }
}
