package org.eclipse.photran.internal.core.analysis.binding;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.parser.Parser.Nonterminal;

public class ChainedVisitor extends GenericParseTreeVisitor
{
    protected ArrayList<ASTVisitor> tdVisitors = new ArrayList<ASTVisitor>();
    protected ArrayList<ASTVisitor> buVisitors = new ArrayList<ASTVisitor>();
    
    protected ArrayList<ArrayList<InteriorNode>> tdPlan;
    protected ArrayList<ArrayList<InteriorNode>> buPlan;
    
    public void addTopDownVisitor(ASTVisitor visitor)
    {
        tdVisitors.add(visitor);
    }
    
    public void addBottomUpVisitor(ASTVisitor visitor)
    {
        buVisitors.add(visitor);
    }
    
    @Override
    public void preparingToVisitChildrenOf(InteriorNode node)
    {
        if (node.getParent() == null)
        {
            tdPlan = new ArrayList<ArrayList<InteriorNode>>();
            buPlan = new ArrayList<ArrayList<InteriorNode>>();
        }
        
        for (int i = 0; i < tdVisitors.size(); i++)
            if (!getVisitMethodFor(node.getNonterminal(), tdVisitors.get(i)).getDeclaringClass().equals(ASTVisitor.class))
                tdPlan.get(i).add(node);
    }

    @Override
    public void doneVisitingChildrenOf(InteriorNode node)
    {
        for (int i = 0; i < buVisitors.size(); i++)
            if (!getVisitMethodFor(node.getNonterminal(), buVisitors.get(i)).getDeclaringClass().equals(ASTVisitor.class))
                buPlan.get(i).add(node);
        
        if (node.getParent() == null) executePlan();
    }

    @Override
    public void visitToken(Token token)
    {
        ;
    }

    private Method getVisitMethodFor(Nonterminal nonterminal, ASTVisitor visitor)
    {
        try
        {
            return visitor.getClass().getMethod("visit" + nonterminal.getClass().getSimpleName(), nonterminal.getClass());
        }
        catch (SecurityException e)
        {
            throw new Error(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new Error(e);
        }
    }

    private void executePlan()
    {
        for (int visitor = 0; visitor < tdPlan.size(); visitor++)
            for (InteriorNode node : tdPlan.get(visitor))
                node.visitOnlyThisNodeUsing(tdVisitors.get(visitor));
        
        for (int visitor = 0; visitor < buPlan.size(); visitor++)
            for (InteriorNode node : buPlan.get(visitor))
                node.visitOnlyThisNodeUsing(buVisitors.get(visitor));
    }
}
