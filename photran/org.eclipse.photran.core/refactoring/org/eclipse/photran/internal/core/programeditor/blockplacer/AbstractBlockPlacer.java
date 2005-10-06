package org.eclipse.photran.internal.core.programeditor.blockplacer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.photran.core.programrepresentation.ParseTreePres;
import org.eclipse.photran.internal.core.f95parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.IPresentationBlock;
import org.eclipse.photran.internal.core.f95parser.NonTreeToken;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.programeditor.ListRange;
import org.eclipse.photran.internal.core.programeditor.ParseTreeEditor;
import org.eclipse.photran.internal.core.programeditor.PartialProgram;
import org.eclipse.photran.internal.core.programeditor.ProgramEditor;
import org.eclipse.photran.internal.core.programeditor.TextRange;
import org.eclipse.photran.internal.core.programeditor.blockplacer.conditions.BlockPlacementCondition;
import org.eclipse.photran.internal.core.programeditor.blockplacer.rules.BlockPlacementRule;

/**
 * The BlockPlacer is responsible for positioning tokens when they are inserted into a presentation
 * or parse tree. Subclass to create different rule sets, e.g., for token insertion,
 * formatting/prettyprinting, etc.
 * 
 * TODO-Jeff: Common superclass for Terminal and Nonterminal
 * 
 * @author joverbey
 */
public abstract class AbstractBlockPlacer
{
    private Map/* <TerminalOrNonterminal, LinkedList<RuleConditionPair>> */rules = new HashMap();

    private Object/*TerminalOrNonterminal*/ currentSymbol = null;

    private LinkedList/* <BlockPlacementRule> */currentRuleSet = null;
    
    private class RuleConditionPair
    {
        public BlockPlacementRule rule;
        public BlockPlacementCondition cond;
        
        public RuleConditionPair(BlockPlacementRule rule, BlockPlacementCondition cond)
        {
            this.cond = cond;
            this.rule = rule;
        }
    }
    
    // --METHODS FOR OUR SUBCLASSES TO USE-------------------------------------

    protected void beginRuleListFor(Object/*TerminalOrNonterminal*/ symbol)
    {
        currentSymbol = symbol;
        if (rules.containsKey(symbol))
            throw new Error("Attempted to define rules for " + symbol.toString()
                + " more than once");
        currentRuleSet = new LinkedList/* <BlockPlacementRule> */();
    }
    
    protected void applyRule(BlockPlacementRule rule, BlockPlacementCondition cond)
    {
        currentRuleSet.add(new RuleConditionPair(rule, cond));
    }
    
    protected BlockPlacementCondition when(BlockPlacementCondition cond)
    {
        return cond;
    }
    
    protected void endRuleList()
    {
        rules.put(currentSymbol, currentRuleSet);
    }
    
    protected void applySameRulesTo(Object/*TerminalOrNonterminal*/ otherSymbol)
    {
        if (rules.containsKey(otherSymbol))
            throw new Error("Attempted to applySameRulesTo " + otherSymbol.toString()
                + " after rules have already been defined for it");
        
        rules.put(otherSymbol, currentRuleSet);
    }
    
    // --BLOCK PLACEMENT/RULE INTERPRETATION CODE------------------------------
    
    /**
     * Repositions the blocks of the partial program, retaining the existing formatting of the
     * partial program. See <code>placeRelative</code>. Call <code>format</code> after
     * insertion is actually complete to reformat the inserted subtree according to the rules
     * specified in the constructor.
     * 
     * @return index at which the partial program's non-tree tokens should be inserted into the
     *         (full) program's <code>Presentation</code> object
     */
    public int prepareForInsertion(ParseTreePres program, PartialProgram ppToInsert, ParseTreeNode newParent, int insertIndex)
    {
        Token precedingToken = findPrecedingToken(program, newParent, insertIndex);
        Token followingToken = findFollowingToken(program, newParent, insertIndex);
        
        return placeRelative(program, ppToInsert, precedingToken, followingToken);
    }

    /**
     * Returns the <code>Token</code> in the parse tree just to the left of the insertion point, or <code>null</code>
     * if the insertion point is the beginning of the program.
     * @param program
     * @param newParent
     * @param insertIndex
     * @return <code>Token</code>
     */
    private Token findPrecedingToken(ParseTreePres program, ParseTreeNode newParent, int insertIndex)
    {
        if (insertIndex > 0)
            return ParseTreeSearcher.findLastTokenIn((ParseTreeNode)newParent.getChildren().get(insertIndex-1));
        else
            return ParseTreeSearcher.findLastTokenPreceding(program.getParseTree(), newParent);
    }

    /**
     * Returns the <code>Token</code> in the parse tree just to the right of the insertion point, or <code>null</code>
     * if the insertion point is the end of the program.
     * @param program
     * @param newParent
     * @param insertIndex
     * @return <code>Token</code>
     */
    private Token findFollowingToken(ParseTreePres program, ParseTreeNode newParent, int insertIndex)
    {
        if (insertIndex < newParent.getChildren().size()-1)
            return ParseTreeSearcher.findFirstTokenIn((ParseTreeNode)newParent.getChildren().get(insertIndex+1));
        else
            return ParseTreeSearcher.findFirstTokenFollowing(program.getParseTree(), newParent);
    }

    /**
     * The partial program to insert has been normalized, i.e., all of its block positions are
     * relative to line 1, column 1. This shifts all of its blocks so that a block at line 1, column
     * 1 moves to the beginning of the insertion point, line 2, column 1 is the line after that,
     * etc. For example, if the normalized tokens were at (line,col) positions (1,3), (1,5), and
     * (2,1), and they will be "pasted" into a program at line 5, column 6, their new positions will
     * be (5,8), (5,10), and (6,1). (Note that the column shift applies only to the first line!)
     * 
     * @param program
     * @param ppToInsert
     * @param precedingToken
     * @param followingToken
     * @return index in the <code>program</code>'s <code>Presentation</code> at which the
     *         partial program's non-tree tokens should be inserted
     */
    private int placeRelative(ParseTreePres program, PartialProgram ppToInsert, Token precedingToken, Token followingToken)
    {
        if (!ppToInsert.isNormalized())
            throw new Error("placeRelative can only operate on normalized partial programs");
        
        int pasteLine = precedingToken.getEndLine();
        int pasteCol = precedingToken.getEndCol();

        TextRange normalizedInsertRange = ProgramEditor.determineOccupiedArea(ppToInsert);
        
        int partialProgramLineShiftAmount = pasteLine - 1;
        int partialProgramColShiftAmount = pasteCol - 1;
        ParseTreeEditor.shiftParseTreeContents(ppToInsert.getParseTree(), 1, 1, partialProgramLineShiftAmount, partialProgramColShiftAmount);
        ppToInsert.getPresentation().shiftContentsOnLine(1, 1, partialProgramLineShiftAmount, partialProgramColShiftAmount);

        TextRange actualInsertRange = ProgramEditor.determineOccupiedArea(ppToInsert);

        int startShiftingRemainingProgramOnLine = actualInsertRange.getLastLine();
        int startShiftingRemainingProgramOnCol = actualInsertRange.getLastCol();
        int remainingProgramLineShiftAmount = normalizedInsertRange.getLastLine() - 1;  // Normalized = relative to 1,1
        int remainingProgramColShiftAmount = normalizedInsertRange.getLastCol() - 1;    // Normalized = relative to 1,1
        ParseTreeEditor.shiftParseTreeContents(program.getParseTree(), startShiftingRemainingProgramOnLine, startShiftingRemainingProgramOnCol, remainingProgramLineShiftAmount, remainingProgramColShiftAmount);
        int indexOfFirstShiftedPresBlock = ppToInsert.getPresentation().shiftContentsOnLine(startShiftingRemainingProgramOnLine, startShiftingRemainingProgramOnCol, remainingProgramLineShiftAmount, remainingProgramColShiftAmount);
        
        return indexOfFirstShiftedPresBlock;
    }

    /**
     * Applies the rules (usually set up in the block placer's constructor) to reposition tokens in
     * (part of) a program.
     * 
     * @param program
     * @param subTreeToFormat
     * @param presRangeToFormat
     */
    public void format(final ParseTreePres program, final ParseTreeNode subTreeToFormat, final ListRange presRangeToFormat)
    {
        if (program == null || subTreeToFormat == null || presRangeToFormat == null)
            throw new Error("format: all parameters must be non-null");
        if (!ParseTreeSearcher.contains(program.getParseTree(), subTreeToFormat))
            throw new Error("format: subTreeToFormat must be a subtree of the program's parse tree "
                + " (did you forget to actually insert the tree into the main program before trying"
                + " to format it?)");
        
        subTreeToFormat.visitUsing(new GenericParseTreeVisitor()
        {
            public void visitToken(Token token)
            {
                applyRules(program, subTreeToFormat, presRangeToFormat, token);
            }
        });
        
        Iterator/*<NonTreeToken>*/ it = program.getPresentation().getNonTreeTokens(presRangeToFormat).iterator();
        while (it.hasNext())
            applyRules(program, subTreeToFormat, presRangeToFormat, (NonTreeToken)it.next());
    }
    
    protected void applyRules(ParseTreePres program, ParseTreeNode subTreeToFormat, ListRange presRangeToFormat, IPresentationBlock tokenToFormat)
    {
        // FIXME-Jeff: Implement applyRules
    }
}
