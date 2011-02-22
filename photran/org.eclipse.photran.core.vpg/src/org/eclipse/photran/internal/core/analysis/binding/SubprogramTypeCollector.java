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
package org.eclipse.photran.internal.core.analysis.binding;

import java.util.List;

import org.eclipse.photran.internal.core.analysis.types.FunctionType;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTFunctionParNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineParNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.vpg.AnnotationType;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;

/**
 * Phase 5 of name-binding analysis.
 * <p>
 * Visits subprogram declarations, setting their types in the VPG.
 *
 * @author Jeff Overbey
 * @see Binder
 */
class SubprogramTypeCollector extends BindingCollector
{
    @Override public void visitASTSubroutineStmtNode(ASTSubroutineStmtNode node)
    {
        super.traverseChildren(node);
        
        PhotranTokenRef tokenRef = node.getSubroutineName().getSubroutineName().getTokenRef();
        updateDefinitionWithTypeInfo(tokenRef, typeOf(node));
    }

    @Override public void visitASTFunctionStmtNode(ASTFunctionStmtNode node)
    {
        super.traverseChildren(node);
        
        PhotranTokenRef tokenRef = node.getFunctionName().getFunctionName().getTokenRef();
        updateDefinitionWithTypeInfo(tokenRef, typeOf(node));
    }
    
    private void updateDefinitionWithTypeInfo(PhotranTokenRef tokenRef, FunctionType type)
    {
        Definition def = vpg.getDefinitionFor(tokenRef);
        def.setType(type);
        vpgProvider.setDefinitionFor(tokenRef, def);
    }

    private FunctionType typeOf(ASTSubroutineStmtNode node)
    {
        FunctionType type = new FunctionType(node.getSubroutineName().getSubroutineName().getText());
        
        type.setReturnType(Type.VOID);
        
        if (node.getSubroutinePars() != null)
        {
            for (ASTSubroutineParNode param : node.getSubroutinePars())
            {
                if (param.isAsterisk())
                {
                    // TODO: type.addArgument(name, type, intent)
                }
                else
                {
                    type.addArgument(
                        param.getVariableName().getText(),
                        typeOf(param.getVariableName()),
                        intentOf(param.getVariableName()));
                }
            }
        }
        return type;
    }

    private FunctionType typeOf(ASTFunctionStmtNode node)
    {
        FunctionType type = new FunctionType(node.getFunctionName().getFunctionName().getText());
        
        if (node.getPrefixSpecList() != null)
        {
            for (int i = 0; i < node.getPrefixSpecList().size(); i++)
            {
                ASTTypeSpecNode typeSpec = node.getPrefixSpecList().get(i).getTypeSpec();
                if (typeSpec != null)
                    type.setReturnType(Type.parse(typeSpec));
            }
        }
        
        if (node.hasResultClause())
            type.setReturnType(typeOf(node.getName()));
        
        if (node.getFunctionPars() != null)
        {
            for (ASTFunctionParNode param : node.getFunctionPars())
                type.addArgument(
                    param.getVariableName().getText(),
                    typeOf(param.getVariableName()),
                    intentOf(param.getVariableName()));
        }
        return type;
    }

    private Type typeOf(Token variableName)
    {
        Definition def = bindUniquely(variableName);
        return def == null ? Type.UNKNOWN : def.getType();
    }

    private VariableAccess intentOf(Token variableName)
    {
        Definition def = bindUniquely(variableName);
        if (def == null) return VariableAccess.RW;
        
        if (def.isIntentIn() && def.isIntentOut())
            return VariableAccess.RW;
        else if (def.isIntentIn())
            return VariableAccess.READ;
        else if (def.isIntentOut())
            return VariableAccess.WRITE;
        else
            return VariableAccess.RW;
    }
    
    private Definition bindUniquely(Token ident)
    {
        List<PhotranTokenRef> bindings = bind(ident);
        if (bindings.size() >= 1)
            return bindings.get(0).getAnnotation(AnnotationType.DEFINITION_ANNOTATION_TYPE);
        else
            return null;
    }
}
