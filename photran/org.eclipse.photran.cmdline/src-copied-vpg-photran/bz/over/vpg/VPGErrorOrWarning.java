package bz.over.vpg;

/**
 * An entry in the VPG error/warning log.
 * 
 * @author Jeff Overbey
 */
public class VPGErrorOrWarning<T, R extends TokenRef<T>>
{
	private boolean isWarning;
	private String message;
	private R tokenRef;
	
	public VPGErrorOrWarning(boolean isWarningOnly, String message, R tokenRef)
	{
		this.isWarning = isWarningOnly;
		this.message = message;
		this.tokenRef = tokenRef;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Accessors
    ///////////////////////////////////////////////////////////////////////////

	/** @return true iff this in a warning entry */
	public boolean isWarning()
	{
		return isWarning;
	}
	
	/** @return true iff this is an error entry */
	public boolean isError()
	{
		return !isWarning;
	}
	
	/** @return the message to display to the user */
	public String getMessage()
	{
		return message;
	}

	/** @return the token associated with this error message, or <code>null</code> */
	public R getTokenRef()
	{
		return tokenRef;
	}
}
