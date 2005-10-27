package org.eclipse.photran.internal.core.f95parser.symboltable;

/**
 * Represents an unexpected problem encountered while creating symbol tables.
 * 
 * Often used when the grammar guarantees that something will be true (e.g.,
 * one node finding another as a child) but might not be in a screwed up
 * parse tree.
 * 
 * @author joverbey
 */
public class SymbolTableError extends Error
{
	private static final long serialVersionUID = 1L;

	public SymbolTableError(String message)
    {
        super(message);
    }
}
