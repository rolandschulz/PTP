package org.eclipse.photran.internal.core.f95modelparser;

import org.eclipse.photran.internal.core.f95modelparser.Token;

/**
 * Represents a program error discovered after lexing and parsing have
 * completed successfully (presently, while building symbol tables).
 * 
 * @author joverbey
 */
public class SemanticError extends Error
{
    private static final long serialVersionUID = 1L;
    
    /**
	 * The token being processed when a syntax error was detected
	 */
	private Token errorToken;

	/**
	 * Creates a new <code>SemanticError</code>, indicating that an error was discovered after lexing and parsing had
	 * completed successfully.
	 * 
	 * @param errorToken
	 *            The token at which the error occurs, or <code>null</code>.
	 *            This is used to create a message to display to the user.
	 */
	public SemanticError(Token errorToken, String message)
    {
		super("Error"
		    	+ (errorToken != null
		    		? " (" + errorToken.getFilename() + ", line "
						+ errorToken.getStartLine() + ", column "
						+ errorToken.getStartCol()
						//+ ", on " + errorToken.getTerminal().getDescription()
						//+ " \"" + errorToken.getText() + "\""
						+ ")"
				    : "")
				+ ": " + message);
		this.errorToken = errorToken;
	}

	/**
	 * @return the token being processed when the semantic error was detected
	 */
	public Token getErrorToken()
    {
		return errorToken;
	}
}
