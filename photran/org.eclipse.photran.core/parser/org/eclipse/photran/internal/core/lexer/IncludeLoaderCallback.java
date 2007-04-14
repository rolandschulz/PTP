package org.eclipse.photran.internal.core.lexer;

public interface IncludeLoaderCallback
{
    /**
     * Called back when an INCLUDE file cannot be loaded.  The given message is displayed to the user.
     * @param message
     * @param filename
     * @return <code>null</code> to continue, or a <code>String</code> containing a message if an <code>Exception</code>
     * should be thrown and parsing/binding aborted
     */
    String onUnableToLoad(String message, String filename);
    
    public static IncludeLoaderCallback DEFAULT = new IncludeLoaderCallback()
    {
        public String onUnableToLoad(String message, String filename)
        {
            return message;
        }
    };
    
    public static IncludeLoaderCallback IGNORE = new IncludeLoaderCallback()
    {
        public String onUnableToLoad(String message, String filename)
        {
            return null;
        }
    };
}
