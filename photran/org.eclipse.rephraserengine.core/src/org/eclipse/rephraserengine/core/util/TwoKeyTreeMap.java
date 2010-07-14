package org.eclipse.rephraserengine.core.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is essentially a table -- a big block of cells indexed by row and column.
 * 
 * @author Jeff Overbey
 * 
 * @param <RowType>
 * @param <ColType>
 * @param <CellType>
 * 
 * @since 3.0
 * 
 * @see TwoKeyHashMap
 */
public final class TwoKeyTreeMap<RowType, ColType, CellType> implements Iterable<CellType>
{
    protected TreeMap<RowType, TreeMap<ColType, CellType>> table = new TreeMap<RowType, TreeMap<ColType, CellType>>();

    /**
     * Add an entry, or if one already exists, replace it.
     * @param k1 - First key
     * @param k2
     * @param v
     */
    public CellType put(RowType k1, ColType k2, CellType v)
    {
        // Map the first key...
        if (!table.containsKey(k1)) table.put(k1, new TreeMap<ColType, CellType>());
        TreeMap<ColType, CellType> thisRow = table.get(k1);

        // ...and the second one to the table entry
        thisRow.put(k2, v);
        
        return v;
    }

    /**
     * Retrieve the entry keyed by the given symbols.
     * @param k1
     * @param k2
     * @return value
     */
    public CellType getEntry(RowType k1, ColType k2)
    {
        // Map the first key (row ID)...
        if (!table.containsKey(k1)) return null;
        TreeMap<ColType, CellType> thisRow = table.get(k1);

        // ...and the second one (column ID)...
        if (!thisRow.containsKey(k2)) return null;

        // ...to the table entry
        return thisRow.get(k2);
    }

    /**
     * Retrieve all entries keyed by the given symbol.
     * @param k1
     * @return value
     */
    public TreeMap<ColType, CellType> getAllEntriesFor(RowType k1)
    {
        // Map the first key (row ID)...
        if (!table.containsKey(k1)) return null;
        return table.get(k1);
    }

    /**
     * @return a <code>Set</code> of all of the (first) keys. Use <code>getAllEntriesFor</code> to get the corresponding entries.
     */
    public Set<RowType> keySet()
    {
        return table.keySet();
    }

    /**
     * Returns true iff the entry keyed by the given symbols is not null (i.e., returns true if there is no entry keyed by the given symbols).
     * @param k1
     * @param k2
     * @return boolean
     */
    public boolean containsEntry(RowType k1, ColType k2)
    {
        return getEntry(k1, k2) != null;
    }

    /**
     * Removes all entries.
     */
    public void clear()
    {
        table.clear();
    }

    /**
     * Removes all entries keyed by the given value.
     * @param key
     */
    public void remove(RowType key)
    {
        table.remove(key);
    }

    /**
     * Removes all entries keyed by the given values.
     * @param k1
     * @param k2
     */
    public void remove(RowType k1, ColType k2)
    {
        if (table.containsKey(k1)) table.get(k1).remove(k2);
    }

    @Override public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("{"); //$NON-NLS-1$
        for (RowType r : table.keySet())
        {
            TreeMap<ColType, CellType> thisRow = table.get(r);
            if (thisRow != null)
            {
                for (ColType c : thisRow.keySet())
                {
                    sb.append("   "); //$NON-NLS-1$
                    sb.append("("); //$NON-NLS-1$
                    sb.append(r);
                    sb.append(", "); //$NON-NLS-1$
                    sb.append(c);
                    sb.append(") ==> "); //$NON-NLS-1$
                    sb.append(thisRow.get(c));
                }
            }
        }
        sb.append("   }"); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * An <code>Iterator</code> that iterates over all of the values (cells) in this <code>TwoKeyTreeMap</code>.
     */
    protected final class ValueIterator implements Iterator<CellType>
    {
        protected State currentState;

        protected Iterator<TreeMap<ColType, CellType>> rowIterator;

        protected Iterator<CellType> colIterator;

        protected abstract class State
        {
            protected State() {}
            abstract CellType getNextValue();
        }

        protected final State INITIAL = new State()
        {
            @Override CellType getNextValue()
            {
                rowIterator = table.values().iterator();

                currentState = STARTING_NEW_ROW;
                return currentState.getNextValue();
            }
        };

        protected final State STARTING_NEW_ROW = new State()
        {
            @Override CellType getNextValue()
            {
                if (rowIterator.hasNext())
                {
                    colIterator = rowIterator.next().values().iterator();
                    currentState = WORKING_THROUGH_ROW;
                }
                else
                {
                    currentState = DONE;
                }
                return currentState.getNextValue();
            }
        };

        protected final State WORKING_THROUGH_ROW = new State()
        {
            @Override CellType getNextValue()
            {
                if (colIterator.hasNext())
                    return colIterator.next();
                else
                {
                    currentState = STARTING_NEW_ROW;
                    return currentState.getNextValue();
                }
            }
        };

        protected final State DONE = new State()
        {
            @Override CellType getNextValue()
            {
                return null;
            }
        };

        // /////////////////////////////////////////////////////////////////////////////////////////

        protected CellType nextValue;

        ValueIterator()
        {
            currentState = INITIAL;
            nextValue = currentState.getNextValue();
        }

        public boolean hasNext()
        {
            return nextValue != null;
        }

        public CellType next()
        {
            CellType currentValue = nextValue;
            nextValue = currentState.getNextValue();
            if (currentValue == null && nextValue == null) throw new NoSuchElementException();
            return currentValue;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator<CellType> iterator()
    {
        return new ValueIterator();
    }
}
