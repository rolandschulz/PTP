/*******************************************************************************
 * Copyright (c) 2007-2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.util;

import java.util.Iterator;

/**
 * Maintains a list of all of the tokens in a program, allowing them to be
 * located quickly by offset.
 * 
 * @author Jeff Overbey
 */
public abstract class TokenList<T> implements Iterable<T>
{
    private T[] array;
    private int size;
    
    public TokenList()
    {
    	this.array = internalCreateTokenArray(4096); // Heuristic
    	this.size = 0;
    }

    protected abstract T[] createTokenArray(int size);
    
    protected abstract int getStreamOffset(T token);
    
    protected abstract int getLength(T token);
    
    protected abstract int getLine(T token);

    private T[] internalCreateTokenArray(int size)
    {
        T[] result = createTokenArray(size);
        
        if (result.length != size)
            throw new IllegalStateException(
                "TokenList#createTokenArray is not implemented correctly.  It was " +
                "asked to create an array of length " + size + ", but it created " +
                "an array of length " + result.length + " instead.");
        
        return result;
    }

    public void add(T token)
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
        T[] newTokenArray = internalCreateTokenArray(array.length*2);
        System.arraycopy(array, 0, newTokenArray, 0, array.length);
        array = newTokenArray;
    }
    
    public void add(int index, T token)
    {
        if (index < 0 || index > size) throw new IllegalArgumentException("Invalid index " + index);
        
        ensureCapacity();
        for (int i = size; i >= index; i--)
            array[i+1] = array[i];
        array[index] = token;
        size++;
    }

    public boolean remove(T tokenToRemove)
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
    
    public T get(int index)
    {
        if (index < 0 || index >= size) throw new IllegalArgumentException("Invalid index " + index);

        return array[index];
    }
    
    public int size()
    {
    	return size;
    }
    
    public int find(T token)
    {
        for (int i = 0; i < size; i++)
            if (array[i].equals(token))
                return i;
        return -1;
    }
    
    public T findTokenContainingStreamOffset(int offset)
    {
        int low = 0, high = size - 1;
        for (int mid = (high+low)/2; low <= high; mid = (high+low)/2)
        {
            int value = getStreamOffset(array[mid]);
            if (value <= offset && offset < value+getLength(array[mid]))
                return array[mid];
            else if (offset > value)
                low = mid + 1;
            else // (offset < value)
                high = mid - 1;
        }
        return null;
    }
    
    public T findStreamOffsetLength(int offset, int length)
    {
        int low = 0, high = size - 1;
        for (int mid = (high+low)/2; low <= high; mid = (high+low)/2)
        {
            int value = getStreamOffset(array[mid]);
            if (offset > value)
                low = mid + 1;
            else if (offset < value)
                high = mid - 1;
            else // (value == offset)
                return getLength(array[mid]) == length ? array[mid] : null;
        }
        return null;
    }
    
    public T findFirstTokenOnLine(int line)
    {
        int index = findIndexOfAnyTokenOnLine(line);
        if (index < 0) return null;
        while (index > 0 && getLine(array[index-1]) == line)
            index--;
        return array[index];
    }
    
    private int findIndexOfAnyTokenOnLine(int line)
    {
        int low = 0, high = size - 1;
        for (int mid = (high+low)/2; low <= high; mid = (high+low)/2)
        {
            int value = getLine(array[mid]);
            if (line > value)
                low = mid + 1;
            else if (line < value)
                high = mid - 1;
            else // (value == offset)
                return mid;
        }
        return -1;
    }
    
    public T findLastTokenOnLine(int line)
    {
        int index = findIndexOfAnyTokenOnLine(line);
        if (index < 0) return null;
        while (index+1 < array.length && getLine(array[index+1]) == line)
            index++;
        return array[index];
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            int index = 0;
            
            public boolean hasNext()
            {
                return index < size;
            }

            public T next()
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
