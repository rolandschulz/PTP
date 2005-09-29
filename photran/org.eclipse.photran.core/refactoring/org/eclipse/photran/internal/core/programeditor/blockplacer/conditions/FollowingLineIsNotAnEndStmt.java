package org.eclipse.photran.internal.core.programeditor.blockplacer.conditions;

/**
 * A block placement condition that the next source line must not be the "end" statement for a
 * function, program, derived type, etc.
 * FIXME-Jeff: Implement this 
 * 
 * @author joverbey
 */
public class FollowingLineIsNotAnEndStmt extends BlockPlacementCondition
{
    public FollowingLineIsNotAnEndStmt()
    {

    }
}