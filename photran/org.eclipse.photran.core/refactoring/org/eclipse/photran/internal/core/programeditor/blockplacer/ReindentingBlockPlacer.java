package org.eclipse.photran.internal.core.programeditor.blockplacer;

import org.eclipse.photran.internal.core.f95parser.Terminal;
import org.eclipse.photran.internal.core.programeditor.blockplacer.conditions.And;
import org.eclipse.photran.internal.core.programeditor.blockplacer.conditions.FollowingLineIsNotAnEndStmt;
import org.eclipse.photran.internal.core.programeditor.blockplacer.conditions.IsFirstTokenOnLine;
import org.eclipse.photran.internal.core.programeditor.blockplacer.rules.IndentSameAsFollowingLine;
import org.eclipse.photran.internal.core.programeditor.blockplacer.rules.IndentSameAsPrecedingLinePlus;

/**
 * A block placer that retains existing formatting except for indentation, which is corrected so
 * that the pasted lines are correctly indented in their new context.
 * 
 * @author joverbey
 */
public class ReindentingBlockPlacer extends AbstractBlockPlacer
{
    public ReindentingBlockPlacer()
    {
        final int INDENT_SIZE = 4;  // TODO-Jeff: Read from preferences
        
        beginRuleListFor(Terminal.T_IMPLICIT);
        applyRule(new IndentSameAsFollowingLine(),
            when(
                 new And(
                     new IsFirstTokenOnLine(),
                     new FollowingLineIsNotAnEndStmt()
                 )
            )
        );
        applyRule(new IndentSameAsPrecedingLinePlus(INDENT_SIZE),
            when(new IsFirstTokenOnLine())  // Otherwise
        );
        endRuleList();
        
        applySameRulesTo(Terminal.T_INTEGER);
        applySameRulesTo(Terminal.T_REAL);
        // FIXME-Jeff: Apply to all tokens that start action or declaration statements
    }
}
