package org.eclipse.photran.internal.core.parser;

//import java.util.List;
//
//import org.eclipse.photran.internal.core.analysis.types.Type;
//import org.eclipse.photran.internal.core.analysis.types.TypeError;
//import org.eclipse.photran.internal.core.lexer.Token;
//import org.eclipse.photran.internal.core.parser.Parser.CSTNode;
//import org.eclipse.photran.internal.core.parser.Parser.Production;
//
//public class ASTPrimaryNode extends ASTPrimaryExtension implements ITypedNode
//{
//    ASTPrimaryNode(Production production,
//                   List<CSTNode> childNodes,
//                   List<CSTNode> discardedSymbols)
//    {
//        super(production, childNodes, discardedSymbols);
//    }
//
//    public Type getType() throws TypeError
//    {
//        if (isComplexConst())       // These may have kinds, which we aren't considering
//            return Type.COMPLEX;
//        else if (isDblConst())
//            return Type.DOUBLEPRECISION;
//        else if (isIntConst())
//            return Type.INTEGER;
//        else if (isLogicalConst())
//            return Type.LOGICAL;
//        else if (isRealConst())
//            return Type.REAL;
//        else if (isStringConst())   // Should also consider is hasSubstringRange() if determining length
//            return Type.CHARACTER;
//        else if (isArrayConstructor())
//            return typeArrayConstructor();
//        else if (isNestedExpression())
//            return getNestedExpression().getType();
//        else if (hasSFDataRef())
//            return typeSFDataRef();
//        else if (hasName())
//            return typeVarOrArrayOrFnCall();
//        else
//            throw new TypeError(this, "Unknown type");
//    }
//
////    # R432
////    <ArrayConstructor> ::= -:T_LPARENSLASH ^:<AcValueList> -:T_SLASHRPAREN
//
//    private Type typeArrayConstructor() throws TypeError
//    {
//        ASTArrayConstructorNode ac = getArrayConstructor();
//        
//        Type type = typeAcValue(ac.getAcValue(0));
//        for (int i = 1, size = ac.size(); i < size; i++)
//            type = type.getCommonType(typeAcValue(ac.getAcValue(i)));
//        return type;
//    }
//
////    # R433
////    <AcValue> ::=
////       | *:<Expr>
////       | *:<AcImpliedDo>
//    
//    private Type typeAcValue(ASTAcValueNode acValue) throws TypeError
//    {
//        if (acValue.hasExpr())
//            return acValue.getExpr().getType();
//        else
//            return typeImpliedDo(acValue.getAcImpliedDo());
//    }
//
////    # R434
////    <AcImpliedDo> ::=
////      | -:T_LPAREN                *:<Expr>        -:T_COMMA <ImpliedDoVariable> -:T_EQUALS lb:<Expr> -:T_COMMA ub:<Expr>                       -:T_RPAREN
////      | -:T_LPAREN                *:<Expr>        -:T_COMMA <ImpliedDoVariable> -:T_EQUALS lb:<Expr> -:T_COMMA ub:<Expr> -:T_COMMA step:<Expr> -:T_RPAREN
////      | -:T_LPAREN nestedImpliedDo*:<AcImpliedDo> -:T_COMMA <ImpliedDoVariable> -:T_EQUALS lb:<Expr> -:T_COMMA ub:<Expr>                       -:T_RPAREN
////      | -:T_LPAREN nestedImpliedDo*:<AcImpliedDo> -:T_COMMA <ImpliedDoVariable> -:T_EQUALS lb:<Expr> -:T_COMMA ub:<Expr> -:T_COMMA step:<Expr> -:T_RPAREN
//    
//    private Type typeImpliedDo(ASTAcImpliedDoNode acImpliedDo) throws TypeError
//    {
//        // TODO: The implied do variable is not bound anywhere, so the expression won't type
//        Type expressionType = findExpression(acImpliedDo).getType();
//        int dimensions = countDimensions(acImpliedDo);
//        return expressionType; // TODO: Array type
//    }
//
//    private ASTExpressionNode findExpression(ASTAcImpliedDoNode acImpliedDo)
//    {
//        if (acImpliedDo.hasNestedImpliedDo())
//            return findExpression(acImpliedDo.getNestedImpliedDo());
//        else
//            return acImpliedDo.getExpr();
//    }
//
//    private int countDimensions(ASTAcImpliedDoNode acImpliedDo)
//    {
//        if (acImpliedDo.hasNestedImpliedDo())
//            return 1 + countDimensions(acImpliedDo.getNestedImpliedDo());
//        else
//            return 1;
//    }
//
////    # R612
////    <SFDataRef> ::= (class=ASTDataRefNode)
////      | <Name>                                                                                  hasDerivedTypeComponentName+:T_PERCENT Component^:<Name>
////      | <Name>        -:T_LPAREN primarySectionSubscriptList*:<SectionSubscriptList> -:T_RPAREN
////      | @:<SFDataRef>                                                                           hasDerivedTypeComponentName+:T_PERCENT Component^:<Name>
////      | @:<SFDataRef> -:T_LPAREN primarySectionSubscriptList*:<SectionSubscriptList> -:T_RPAREN hasDerivedTypeComponentName+:T_PERCENT Component^:<Name>
//
//    private Type typeSFDataRef() throws TypeError
//    {
//        ASTDataRefNode dr = getSFDataRef();
//        
//        Type type = typeIdentifier(dr.getName(0));
//        for (int i = 0, size = dr.size(); i < size; i++)
//        {
//            if (dr.hasPrimarySectionSubscriptList(i))
//                type = type.resolveSectionSubscriptList(dr.getPrimarySectionSubscriptList(i));
//            
//            if (dr.hasDerivedTypeComponentName(i))
//                type = type.getTypeOfComponent(dr.getComponentName(i));
//        }
//        return type;
//    }
//
//    private Type typeIdentifier(Token name) throws TypeError
//    {
//        // TODO Implement
//        throw new TypeError(this, "NOT IMPLEMENTED");
//    }
//
//    /** @return the type of a variable/array, function call, or derived type component */
//    private Type typeVarOrArrayOrFnCall() throws TypeError
//    {
//        // TODO Implement
//        throw new TypeError(this, "NOT IMPLEMENTED");
//    }
//}
