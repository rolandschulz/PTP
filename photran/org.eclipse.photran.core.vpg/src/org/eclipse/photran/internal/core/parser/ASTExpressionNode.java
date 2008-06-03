package org.eclipse.photran.internal.core.parser;

//import java.util.List;
//
//import org.eclipse.photran.internal.core.analysis.types.Type;
//import org.eclipse.photran.internal.core.analysis.types.TypeError;
//import org.eclipse.photran.internal.core.parser.Parser.CSTNode;
//import org.eclipse.photran.internal.core.parser.Parser.Production;
//
//public class ASTExpressionNode extends ASTExpressionBase implements ITypedNode
//{
//    ASTExpressionNode(Production production,
//                      List<CSTNode> childNodes,
//                      List<CSTNode> discardedSymbols)
//    {
//        super(production, childNodes, discardedSymbols);
//    }
//    
//    public Type getType() throws TypeError
//    {
//        // TODO Implement -- This is nowhere near correct since it doesn't consider what operator is being applied
//        if (isUnary())
//            return getRHS().getType();
//        else
//            return getLHS().getType().getCommonType(getRHS().getType());
//    }
//    
//    public ITypedNode getLHS()
//    {
//        if (hasLhsPrimary())
//            return getLhsPrimary();
//        else
//            return getLhsExpr();
//    }
//    
//    public ITypedNode getRHS()
//    {
//        if (hasRhsPrimary())
//            return getRhsPrimary();
//        else
//            return getRhsExpr();
//    }
//    
//    public boolean isUnary()
//    {
//        return getLHS() == null;
//    }
//}
