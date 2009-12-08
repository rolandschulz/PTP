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
package org.eclipse.photran.internal.core.parser;

import java.io.PrintStream;
import java.util.Iterator;

import java.util.List;

import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTAttrSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isCodimension; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isParameter; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isIntent; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isSave; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isTarget; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isAllocatable; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isValue; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTAttrSpecNode
    ASTCoarraySpecNode coarraySpec; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isProtected; // in ASTAttrSpecNode
    ASTAccessSpecNode accessSpec; // in ASTAttrSpecNode
    ASTLanguageBindingSpecNode languageBindingSpec; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isPointer; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isExternal; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isIntrinsic; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isContiguous; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isVolatile; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isOptional; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isAsync; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isDimension; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTAttrSpecNode
    ASTArraySpecNode arraySpec; // in ASTAttrSpecNode
    ASTIntentSpecNode intentSpec; // in ASTAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAttrSpecNode

    public boolean isCodimension()
    {
        return this.isCodimension != null;
    }

    public void setIsCodimension(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isCodimension = newValue;
    }


    public boolean isParameter()
    {
        return this.isParameter != null;
    }

    public void setIsParameter(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isParameter = newValue;
    }


    public boolean isIntent()
    {
        return this.isIntent != null;
    }

    public void setIsIntent(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isIntent = newValue;
    }


    public boolean isSave()
    {
        return this.isSave != null;
    }

    public void setIsSave(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isSave = newValue;
    }


    public boolean isTarget()
    {
        return this.isTarget != null;
    }

    public void setIsTarget(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isTarget = newValue;
    }


    public boolean isAllocatable()
    {
        return this.isAllocatable != null;
    }

    public void setIsAllocatable(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAllocatable = newValue;
    }


    public boolean isValue()
    {
        return this.isValue != null;
    }

    public void setIsValue(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isValue = newValue;
    }


    public ASTCoarraySpecNode getCoarraySpec()
    {
        return this.coarraySpec;
    }

    public void setCoarraySpec(ASTCoarraySpecNode newValue)
    {
        this.coarraySpec = newValue;
    }


    public boolean isProtected()
    {
        return this.isProtected != null;
    }

    public void setIsProtected(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isProtected = newValue;
    }


    public ASTAccessSpecNode getAccessSpec()
    {
        return this.accessSpec;
    }

    public void setAccessSpec(ASTAccessSpecNode newValue)
    {
        this.accessSpec = newValue;
    }


    public ASTLanguageBindingSpecNode getLanguageBindingSpec()
    {
        return this.languageBindingSpec;
    }

    public void setLanguageBindingSpec(ASTLanguageBindingSpecNode newValue)
    {
        this.languageBindingSpec = newValue;
    }


    public boolean isPointer()
    {
        return this.isPointer != null;
    }

    public void setIsPointer(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPointer = newValue;
    }


    public boolean isExternal()
    {
        return this.isExternal != null;
    }

    public void setIsExternal(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isExternal = newValue;
    }


    public boolean isIntrinsic()
    {
        return this.isIntrinsic != null;
    }

    public void setIsIntrinsic(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isIntrinsic = newValue;
    }


    public boolean isContiguous()
    {
        return this.isContiguous != null;
    }

    public void setIsContiguous(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isContiguous = newValue;
    }


    public boolean isVolatile()
    {
        return this.isVolatile != null;
    }

    public void setIsVolatile(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isVolatile = newValue;
    }


    public boolean isOptional()
    {
        return this.isOptional != null;
    }

    public void setIsOptional(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isOptional = newValue;
    }


    public boolean isAsync()
    {
        return this.isAsync != null;
    }

    public void setIsAsync(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAsync = newValue;
    }


    public boolean isDimension()
    {
        return this.isDimension != null;
    }

    public void setIsDimension(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDimension = newValue;
    }


    public ASTArraySpecNode getArraySpec()
    {
        return this.arraySpec;
    }

    public void setArraySpec(ASTArraySpecNode newValue)
    {
        this.arraySpec = newValue;
    }


    public ASTIntentSpecNode getIntentSpec()
    {
        return this.intentSpec;
    }

    public void setIntentSpec(ASTIntentSpecNode newValue)
    {
        this.intentSpec = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAttrSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 25;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isCodimension;
        case 1:  return this.isParameter;
        case 2:  return this.isIntent;
        case 3:  return this.isSave;
        case 4:  return this.isTarget;
        case 5:  return this.isAllocatable;
        case 6:  return this.isValue;
        case 7:  return this.hiddenTLbracket;
        case 8:  return this.coarraySpec;
        case 9:  return this.hiddenTRbracket;
        case 10: return this.isProtected;
        case 11: return this.accessSpec;
        case 12: return this.languageBindingSpec;
        case 13: return this.isPointer;
        case 14: return this.isExternal;
        case 15: return this.isIntrinsic;
        case 16: return this.isContiguous;
        case 17: return this.isVolatile;
        case 18: return this.isOptional;
        case 19: return this.isAsync;
        case 20: return this.isDimension;
        case 21: return this.hiddenTLparen;
        case 22: return this.arraySpec;
        case 23: return this.intentSpec;
        case 24: return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isCodimension = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.isParameter = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.isIntent = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.isSave = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.isTarget = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.isAllocatable = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.isValue = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.coarraySpec = (ASTCoarraySpecNode)value; return;
        case 9:  this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.isProtected = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 11: this.accessSpec = (ASTAccessSpecNode)value; return;
        case 12: this.languageBindingSpec = (ASTLanguageBindingSpecNode)value; return;
        case 13: this.isPointer = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 14: this.isExternal = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 15: this.isIntrinsic = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 16: this.isContiguous = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 17: this.isVolatile = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 18: this.isOptional = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 19: this.isAsync = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 20: this.isDimension = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 21: this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 22: this.arraySpec = (ASTArraySpecNode)value; return;
        case 23: this.intentSpec = (ASTIntentSpecNode)value; return;
        case 24: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

