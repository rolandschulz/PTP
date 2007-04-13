package org.eclipse.photran.internal.core.lexer;

import java.util.Iterator;

public class TokenList
{
    private Token[] array = new Token[4096]; // Heuristic
    private int size = 0;

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
    
    public int find(Token token)
    {
        for (int i = 0; i < size; i++)
            if (array[i].equals(token))
                return i;
        return -1;
    }
    
    public Token findOffsetLength(int offset, int length)
    {
        int low = 0, high = size - 1;
        for (int mid = (high+low)/2; low <= high; mid = (high+low)/2)
        {
            int value = array[mid].getOffset();
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

    public Iterator/*<Token>*/ iterator()
    {
        return new Iterator/*<Token>*/()
        {
            int index = 0;
            
            public boolean hasNext()
            {
                return index < size;
            }

            public Object next()
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
