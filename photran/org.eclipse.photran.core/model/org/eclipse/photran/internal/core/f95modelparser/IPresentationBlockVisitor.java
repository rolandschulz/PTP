package org.eclipse.photran.internal.core.f95modelparser;


/**
 * Visitor for <code>Token</code>s and <code>NonTreeToken</code>s
 * 
 * @author joverbey
 */
public interface IPresentationBlockVisitor
{
	public void visitToken(Token token);
	
	public void visitNonTreeToken(NonTreeToken nonTreeToken);
}
