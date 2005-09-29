package org.eclipse.photran.internal.core.programeditor.blockplacer.conditions;

import java.util.LinkedList;
import java.util.List;

/**
 * A block placement condition that several other block placement conditions must all apply.
 * FIXME-Jeff: Implement this 
 * 
 * @author joverbey
 */
public class And extends BlockPlacementCondition
{
    private List/*<BlockPlacementCondition>*/ conditions = new LinkedList();
    
    public And(BlockPlacementCondition c1, BlockPlacementCondition c2)
    {
        conditions.add(c1);
        conditions.add(c2);
    }
}