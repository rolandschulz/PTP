package org.eclipse.photran.internal.core.model;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

/**
 * This visitor is used to build the Outline view when the user chooses the (debugging) option to
 * have the <i>entire parse tree</i> displayed instead of the normal Outline view.
 * 
 * The normal Outline view is created by a <code>FortranModelBuildingVisitor</code> instead.
 * 
 * @author joverbey
 */
public final class FortranParseTreeModelBuildingVisitor extends GenericASTVisitor
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

    private LinkedList/* <ParseTreeNode> */parentParseTreeNodeStack = new LinkedList();

    private LinkedList/* <F90Elements.F90Element> */parentElementStack = new LinkedList();

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

    private String methodName = "";
    
    @SuppressWarnings("unchecked")
    @Override public void visitASTNode(IASTNode node)
    {
        String description = methodName + node.getClass().getSimpleName();
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

        beginAddingChildrenFor(node, element);

        if (node instanceof IASTListNode<?>)
        {
            IASTListNode<? extends IASTNode> list = (IASTListNode<? extends IASTNode>)node;
            
            for (int i = 0; i < list.size(); i++)
            {
                methodName = "get(" + i + "): ";
                list.get(i).accept(this);
            }
        }
        else
        {
            for (java.lang.reflect.Method m : node.getClass().getMethods())
            {
                if (m.getDeclaringClass() == node.getClass()
                        && m.getReturnType() != null
                        && m.getParameterTypes().length == 0
                        && !m.getName().equals("getParent")
                        && !m.getName().equals("getChildren")
                        && !m.getName().equals("findFirstToken")
                        && !m.getName().equals("findLastToken")
                        && IASTNode.class.isAssignableFrom(m.getReturnType()))
                {
                    try
                    {
                        methodName = m.getName() + "(): ";
                        IASTNode n = ((IASTNode)m.invoke(node, (Object[])null));
                        if (n == null)
                            modelBuilder.addF90Element(
                                new FortranElement.UnknownNode(getCurrentParent(),
                                    methodName + "null"));
                        else
                            n.accept(this);
                    }
                    catch (IllegalArgumentException e)
                    {
                        ;
                    }
                    catch (IllegalAccessException e)
                    {
                        ;
                    }
                    catch (InvocationTargetException e)
                    {
                        ;
                    }
                    catch (CModelException e)
                    {
                        ;
                    }
                }
            }
        }
        
        doneAddingChildrenFor(node);
    }

    @Override public void visitToken(Token token)
    {
        String description = methodName + token.getClass().getSimpleName();
        FortranElement element = new FortranElement.UnknownNode(getCurrentParent(), description);
        element.setIdentifier(token);
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
