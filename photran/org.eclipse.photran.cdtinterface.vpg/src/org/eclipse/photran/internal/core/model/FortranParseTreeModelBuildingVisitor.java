package org.eclipse.photran.internal.core.model;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.internal.core.analysis.loops.GenericASTVisitorWithLoops;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;

/**
 * THIS IS AN INTERNAL CLASS.
 * <p>
 * This AST visitor is used to build the model (i.e., Outline view) when the user chooses the
 * "(Debugging) Show entire abstract syntax tree rather than Outline view" option in the workspace
 * preferences.
 * <p>
 * The normal Outline view is created by a {@link FortranModelBuildingVisitor} instead.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public final class FortranParseTreeModelBuildingVisitor extends GenericASTVisitorWithLoops
{
    // --INFRASTRUCTURE--------------------------------------------------

    private org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;

    private FortranModelBuilder modelBuilder;

    public FortranParseTreeModelBuildingVisitor(
        org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit,
        FortranModelBuilder modelBuilder)
    {
        this.translationUnit = translationUnit;
        this.modelBuilder = modelBuilder;
    }

    private LinkedList<IASTNode> parentParseTreeNodeStack = new LinkedList<IASTNode>();

    private LinkedList<FortranElement> parentElementStack = new LinkedList<FortranElement>();

    private Parent getCurrentParent()
    {
        if (parentElementStack.isEmpty())
            return translationUnit;
        else
            return (Parent)parentElementStack.getLast();
    }

    private boolean isCurrentParent(IASTNode node)
    {
        if (parentParseTreeNodeStack.isEmpty())
            return false;
        else
            return node == (IASTNode)parentParseTreeNodeStack.getLast();
    }

    private void beginAddingChildrenFor(IASTNode parseTreeNode, FortranElement element)
    {
        parentParseTreeNodeStack.addLast(parseTreeNode);
        parentElementStack.addLast(element);
    }

    private void doneAddingChildrenFor(IASTNode node)
    {
        if (isCurrentParent(node))
        {
            parentParseTreeNodeStack.removeLast();
            parentElementStack.removeLast();
        }
    }

    // --VISITOR METHODS-------------------------------------------------

    private String methodNameDescPrefix = "";
    
    @Override public void visitASTNode(IASTNode node)
    {
        FortranElement element = addElementForNode(node);
        
        beginAddingChildrenFor(node, element);
        addMethodDescriptions(node);
        doneAddingChildrenFor(node);
    }

    private FortranElement addElementForNode(IASTNode node)
    {
        String description = methodNameDescPrefix + node.getClass().getSimpleName();
        FortranElement element = new FortranElement.UnknownNode(getCurrentParent(), description);

//        Token firstToken = ParseTreeSearcher.findFirstTokenIn(node);
//        Token lastToken = ParseTreeSearcher.findLastTokenIn(node);
//        if (firstToken != null && lastToken != null)
//        {
//            element.setPos(firstToken.getOffset(), (lastToken.getOffset() + lastToken.getLength())
//                - firstToken.getOffset() - 1);
//            element.setIdPos(firstToken.getOffset(), firstToken.getLength());
//            element.setLines(firstToken.getStartLine(), lastToken.getEndLine());
//        }

        try
        {
            modelBuilder.addF90Element(element);
        }
        catch (CModelException e)
        {
            ;
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    private void addMethodDescriptions(IASTNode node)
    {
        if (node instanceof IASTListNode<?>)
            addListEltDescriptions((IASTListNode<? extends IASTNode>)node);
        else
            for (java.lang.reflect.Method m : node.getClass().getMethods())
                if (shouldIncludeMethodDesc(node, m))
                    addMethodDesc(node, m);
    }

    private void addListEltDescriptions(IASTListNode< ? extends IASTNode> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            methodNameDescPrefix = "get(" + i + "): ";
            if (list.get(i) != null)
            	list.get(i).accept(this);
        }
    }

    private boolean shouldIncludeMethodDesc(IASTNode node, java.lang.reflect.Method m)
    {
        return m.getDeclaringClass() == node.getClass()
                && m.getReturnType() != null
                && m.getParameterTypes().length == 0
                && !m.getName().equals("getParent")
                && !m.getName().equals("getChildren")
                && !m.getName().equals("findFirstToken")
                && !m.getName().equals("findLastToken");
    }

    private void addMethodDesc(IASTNode node, java.lang.reflect.Method m)
    {
        try
        {
            methodNameDescPrefix = m.getName() + "(): ";
            
            if (IASTNode.class.isAssignableFrom(m.getReturnType()))
            {
                describeASTGetterMethod(node, m);
            }
            else if (m.getReturnType() != null)
            {
                describeOtherMethod(node, m);
            }
        }
        catch (IllegalArgumentException e) {;}
        catch (IllegalAccessException e) {;}
        catch (InvocationTargetException e) {;}
        catch (CModelException e) {;}
    }

    private void describeASTGetterMethod(IASTNode node, java.lang.reflect.Method m)
        throws IllegalAccessException, InvocationTargetException, CModelException
    {
        IASTNode n = ((IASTNode)m.invoke(node, (Object[])null));
        if (n == null)
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    methodNameDescPrefix + "null"));
        else
            n.accept(this);
    }

    private void describeOtherMethod(IASTNode node, java.lang.reflect.Method m)
        throws IllegalAccessException, InvocationTargetException, CModelException
    {
        Object n = m.invoke(node, (Object[])null);
        if (n == null)
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    methodNameDescPrefix + "null"));
        else
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    methodNameDescPrefix + n));
    }

    @Override public void visitToken(Token token)
    {
        String description = methodNameDescPrefix + token.getClass().getSimpleName();
        FortranElement element = new FortranElement.UnknownNode(getCurrentParent(), description);
        modelBuilder.configureElement(element, token);
        try
        {
            modelBuilder.addF90Element(element);
            
            beginAddingChildrenFor(token, element);
            
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    "getTerminal(): Terminal." + findTerminal(token.getTerminal())));
            
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    "getText(): \"" + token.getText().replaceAll("\\n", "\\\\n") + "\""));
            
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    "getPreprocessorDirective(): " + token.getPreprocessorDirective()));
            
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    "getPhysicalFile(): " + token.getPhysicalFile()));
            
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    "getFileOffset(): " + token.getFileOffset()));
            
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    "getLogicalFile(): " + token.getLogicalFile()));
            
            modelBuilder.addF90Element(
                new FortranElement.UnknownNode(getCurrentParent(),
                    "getStreamOffset(): " + token.getStreamOffset()));
            
            doneAddingChildrenFor(token);
        }
        catch (CModelException e)
        {
            ;
        }
    }

    private String findTerminal(Terminal terminal)
    {
        for (java.lang.reflect.Field f : Terminal.class.getFields())
        {
            try
            {
                if (f.get(null) == terminal)
                    return f.getName();
            }
            catch (IllegalArgumentException e)
            {
                ;
            }
            catch (IllegalAccessException e)
            {
                ;
            }
        }
        
        return "?";
    }
}
