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
package org.eclipse.photran.internal.core.analysis.binding;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTImplicitSpecListNode;
import org.eclipse.photran.internal.core.parser.ASTImplicitSpecNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;

/**
 * An IMPLICIT specification.
 * 
 * @author Jeff Overbey
 */
public class ImplicitSpec implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String toString = "Implicit Enabled - Default Implicit Spec: real (a-h), integer (i-n), real (o-z)";
    private Type[] typeMap = { Type.REAL,    // A
                               Type.REAL,    // B
                               Type.REAL,    // C
                               Type.REAL,    // D
                               Type.REAL,    // E
                               Type.REAL,    // F
                               Type.REAL,    // G
                               Type.REAL,    // H
                               Type.INTEGER, // I
                               Type.INTEGER, // J
                               Type.INTEGER, // K
                               Type.INTEGER, // L
                               Type.INTEGER, // M
                               Type.INTEGER, // N
                               Type.REAL,    // O
                               Type.REAL,    // P
                               Type.REAL,    // Q
                               Type.REAL,    // R
                               Type.REAL,    // S
                               Type.REAL,    // T
                               Type.REAL,    // U
                               Type.REAL,    // V
                               Type.REAL,    // W
                               Type.REAL,    // X
                               Type.REAL,    // Y
                               Type.REAL     // Z
    };

    /**
     * Create an implicit spec with the default type map (see above)
     */
    public ImplicitSpec()
    {
    }
    
    /**
     * Create an implicit spec for the given <T_xImplicitSpecList> parse tree node
     * @param txImplicitSpecList
     */
    public ImplicitSpec(ASTImplicitSpecListNode implicitSpecList)
    {
    	toString = "Implicit Enabled -" + getSourceCodeFromASTNode(implicitSpecList);
    	
        implicitSpecList.visitTopDownUsing(new ASTVisitor()
        {
            @Override
            public void visitASTImplicitSpecNode(ASTImplicitSpecNode implicitSpec)
            {
                // <T_xImplicitSpec> ::= <xTypeSpec> T_xImpl
                
                Type type = Type.parse(implicitSpec.getTypeSpec());
                Token impl = implicitSpec.getTXImpl();
                setRangesToType(impl, type);
            }

            private void setRangesToType(Token txImpl, Type type)
            {
                // Range = [a-zA-Z](-[a-zA-Z])?
                // xImpl = "(" [ \t]* {Range} ([ \t]* "," [ \t]* {Range})* ")"
                
                String rangeList = txImpl.getText().replaceAll("[ \t]", "");
                rangeList = rangeList.substring(1, rangeList.length()-1); // Trim commas
                String[] ranges = rangeList.split(",");
                
                for (String range : ranges)
                    setRangeToType(range, type);
            }

            private void setRangeToType(String range, Type type)
            {
                char rangeStart = range.charAt(0);
                char rangeEnd = (range.length() == 3 ? range.charAt(2) : rangeStart);
                
                for (char letter = rangeStart; letter <= rangeEnd; letter++)
                    setType(letter, type);
            }
        });
    }
    
    public ImplicitSpec(ImplicitSpec original)
    {
        this.typeMap = (original == null || original.typeMap == null ? null : original.typeMap.clone());
    }
    
    /**
     * Indicates that non-declared identifiers beginning with <code>letter</code> should
     * implicitly have type <code>type</code>.
     * 
     * @param letter
     * @param type
     */
    public void setType(char letter, Type type)
    {
        if (!Character.isLetter(letter)) throw new Error("Non-letter passed to setType");
        letter = Character.toUpperCase(letter);
        typeMap[letter - 'A'] = type;
    }

    /**
     * @param letter
     * @return the type that non-declared identifiers beginning with <code>letter</code>
     *         should have
     */
    public Type getType(char letter)
    {
        if (!Character.isLetter(letter))
            return Type.REAL;
        else
        {
            letter = Character.toUpperCase(letter);
            return typeMap[letter - 'A'];
        }
    }
    
    // Copied from SourcePrinter; refactor somehow
    private static String getSourceCodeFromASTNode(CSTNode node)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        node.printOn(new PrintStream(out), null);
        return out.toString();
    }
    
    @Override public String toString()
    {
    	return toString;
    }
}
