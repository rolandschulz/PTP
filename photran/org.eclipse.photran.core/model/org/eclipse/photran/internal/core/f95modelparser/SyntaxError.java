package org.eclipse.photran.internal.core.f95modelparser;

import org.eclipse.photran.internal.core.f95modelparser.Token;

/**
 * Thrown when the parser encounters a syntax error.
 * Gives access to the token on which the error occurred, which includes
 * line and column information.
 * 
 * @author joverbey
 */
public final class SyntaxError extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The token being processed when a syntax error was detected
	 */
	private Token errorToken;

	/**
	 * Creates a new <code>SyntaxError</code>, indicating that the input file
	 * did not match the Fortran grammar.
	 * 
	 * @param errorToken
	 *            The token being processed when a syntax error was detected.
	 *            This is used to create a message to display to the user.
	 */
	public SyntaxError(Token errorToken) {
		super("Syntax error (" + errorToken.getFilename() + ", line "
				+ errorToken.getStartLine() + ", column "
				+ errorToken.getStartCol() + ": Unexpected "
				+ errorToken.getTerminal().getDescription() + " \""
				+ errorToken.getText() + "\"");
		this.errorToken = errorToken;
	}

	/**
	 * @return the token being processed when the syntax error was detected
	 */
	public Token getErrorToken() {
		return errorToken;
	}
}
