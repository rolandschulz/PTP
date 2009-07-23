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
package org.eclipse.photran.internal.core.lexer;

import java.util.Iterator;

import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

/**
 * Maintains a list of all of the tokens in a program, allowing them to be
 * located quickly by offset.
 * 
 * @author Jeff Overbey
 */
public class TokenList
{
    private Token[] array;
    private int size;
    
    public TokenList()
    {
    	this.array = new Token[4096]; // Heuristic
    	this.size = 0;
    }
    
    public TokenList(ASTExecutableProgramNode ast)
    {
    	this();
    	ast.accept(new ASTVisitorWithLoops()
    	{
			public void visitToken(Token token)
			{
				add(token);
			}
    	});
    }

    public void add(Token token)
    {
        ensureCapacity();
        array[size++] = token;
    }

    private void ensureCapacity()
    {
        if (size == array.length) expandArray();
    }

    private void expandArray()
    {
        Token[] newTokenArray = new Token[array.length*2];
        System.arraycopy(array, 0, newTokenArray, 0, array.length);
        array = newTokenArray;
    }
    
    public void add(int index, Token token)
    {
        if (index < 0 || index > size) throw new IllegalArgumentException("Invalid index " + index);
        
        ensureCapacity();
        for (int i = size; i >= index; i--)
            array[i+1] = array[i];
        array[index] = token;
        size++;
    }

    public boolean remove(Token tokenToRemove)
    {
        int index = find(tokenToRemove);
        return index < 0 ? false : remove(index);
    }
    
    public boolean remove(int index)
    {
        if (index < 0 || index >= size) throw new IllegalArgumentException("Invalid index " + index);
        
        for (int i = index + 1; i < size; i++)
            array[i-1] = array[i];
        size--;
        return true;
    }
    
    public Token get(int index)
    {
        if (index < 0 || index >= size) throw new IllegalArgumentException("Invalid index " + index);

        return array[index];
    }
    
    public int size()
    {
    	return size;
    }
    
    public int find(Token token)
    {
        for (int i = 0; i < size; i++)
            if (array[i].equals(token))
                return i;
        return -1;
    }
    
    public Token findStreamOffsetLength(int offset, int length)
    {
        int low = 0, high = size - 1;
        for (int mid = (high+low)/2; low <= high; mid = (high+low)/2)
        {
            int value = array[mid].getStreamOffset();
            if (offset > value)
                low = mid + 1;
            else if (offset < value)
                high = mid - 1;
            else // (value == offset)
                return array[mid].getLength() == length ? array[mid] : null;
        }
        return null;
    }
    
    public Token findFirstTokenOnLine(int line)
    {
        int index = findIndexOfAnyTokenOnLine(line);
        if (index < 0) return null;
        while (index > 0 && array[index-1].getLine() == line)
            index--;
        return array[index];
    }
    
    private int findIndexOfAnyTokenOnLine(int line)
    {
        int low = 0, high = size - 1;
        for (int mid = (high+low)/2; low <= high; mid = (high+low)/2)
        {
            int value = array[mid].getLine();
            if (line > value)
                low = mid + 1;
            else if (line < value)
                high = mid - 1;
            else // (value == offset)
                return mid;
        }
        return -1;
    }
    
    public Token findLastTokenOnLine(int line)
    {
        int index = findIndexOfAnyTokenOnLine(line);
        if (index < 0) return null;
        while (index+1 < array.length && array[index+1].getLine() == line)
            index++;
        return array[index];
    }

    public Iterator<Token> iterator()
    {
        return new Iterator<Token>()
        {
            int index = 0;
            
            public boolean hasNext()
            {
                return index < size;
            }

            public Token next()
            {
                return index < size ? array[index++] : null;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
