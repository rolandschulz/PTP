/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.vpg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * VPG error/warning log.
 * 
 * @author Jeff Overbey
 * @author Kurt Hendle
 * 
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 * 
 * @since 1.0
 */
public final class VPGLog<T, R extends IVPGNode<T>>
{
    public class Entry
	{
		private boolean isWarning;
		private String message;
		private R tokenRef;

		/** @since 3.0 */
		public Entry(boolean isWarningOnly, String message, R tokenRef)
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

		/** @return the token associated with this error message, or <code>null</code> 
		 * @since 3.0*/
		public R getTokenRef()
		{
			return tokenRef;
		}
	}

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    /** @since 3.0 */
    protected final File logFile;
    
    /** @since 3.0 */
    protected IVPGComponentFactory<?, T, R> locator;
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @since 3.0
     */
    public VPGLog(File logFile, IVPGComponentFactory<?, T, R> locator)
    {
        this.logFile = logFile;
        this.locator = locator;
    }

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    /** The entries in the log */
	protected List<Entry> log = new ArrayList<Entry>();

	/** Clears the error/warning log. */
	public void clear()
	{
		log.clear();
		notifyListeners();
	}

	/** Removes all entries for the given file from the error/warning log. */
	public void clearEntriesFor(String filename)
	{
	    List<Entry> newLog = new LinkedList<Entry>();

	    // Iterate using indices to avoid ConcurrentModificationException
		for (int i = 0; i < log.size(); i++)
		{
		    Entry entry = log.get(i);
			R tokenRef = entry.getTokenRef();
			if (tokenRef == null || !tokenRef.getFilename().equals(filename))
				newLog.add(entry);
		}

		log = newLog;

		notifyListeners();
	}

	/**
	 * Adds the given warning to the error/warning log.
	 *
	 * @param message the warning message to display to the user
	 */
	public void logWarning(String message)
	{
		log.add(new Entry(true, message, null));
		notifyListeners();
	}

    /**
     * Adds the given warning to the error/warning log.
     *
     * @param message the warning message to display to the user
     * @param filename the file with which the warning is associated
     */
	public void logWarning(String message, String filename)
	{
		log.add(new Entry(true, message, locator.getVPGNode(filename, 0, 0)));
		notifyListeners();
	}

    /**
     * Adds the given warning to the error/warning log.
     *
     * @param message the warning message to display to the user
     * @param tokenRef a specific token with which the warning is associated;
     *                 for example, if an identifier was used without being
     *                 initialized, it could reference that identifier
     * @since 3.0
     */
	public void logWarning(String message, R tokenRef)
	{
		log.add(new Entry(true, message, tokenRef));
		notifyListeners();
	}

    /**
     * Adds the given error to the error/warning log.
     *
     * @param e an exception that will be displayed to the user
     */
    public void logError(Throwable e)
    {
        logError(e, null);
    }

    /**
     * Adds the given error to the error/warning log.
     *
     * @param e an exception that will be displayed to the user
     * @param tokenRef a specific token with which the warning is associated;
     *                 for example, if an identifier was used without being
     *                 initialized, it could reference that identifier
     * @since 3.0
     */
	public void logError(Throwable e, R tokenRef)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.getClass().getName());
		sb.append(": "); //$NON-NLS-1$
		sb.append(e.getMessage());
		sb.append("\n"); //$NON-NLS-1$
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(bs));
		sb.append(bs);

		log.add(new Entry(false, sb.toString(), tokenRef));
		notifyListeners();
	}

    /**
     * Adds the given error to the error/warning log.
     *
     * @param message the error message to display to the user
     */
	public void logError(String message)
	{
		log.add(new Entry(false, message, null));
		notifyListeners();
	}

//    /**
//     * Adds the given error to the error/warning log.
//     *
//     * @param message the error message to display to the user
//     * @param filename the file with which the warning is associated
//     */
//	public void logError(String message, String filename)
//	{
//		log.add(new Entry(false, message, createTokenRef(filename, 0, 0)));
//		notifyListeners();
//	}

    /**
     * Adds the given error to the error/warning log.
     *
     * @param message the error message to display to the user
     * @param tokenRef a specific token with which the error is associated;
     *                 for example, if an identifier was used but not
     *                 declared, it could reference that identifier
     * @since 3.0
     */
	public void logError(String message, R tokenRef)
	{
		log.add(new Entry(false, message, tokenRef));
		notifyListeners();
	}

	/** @return true iff at least one error exists in the error/warning log */
	public boolean hasErrorsLogged()
	{
        for (int i = 0; i < log.size(); i++)
            if (log.get(i).isError())
				return true;

		return false;
	}

    /** @return true iff at least one entry exists in the error/warning log */
	public boolean hasErrorsOrWarningsLogged()
	{
		return !log.isEmpty();
	}

	/** @return the error/warning log */
	public List<Entry> getEntries()
	{
		return log;
	}

	/** Prints the error/warning log on the given <code>PrintStream</code> */
	public void printOn(PrintStream out)
	{
        for (int i = 0; i < log.size(); i++)
        {
            Entry entry = log.get(i);

	        out.print(entry.isError() ? Messages.VPGLog_ErrorLabel : Messages.VPGLog_WarningLabel);
	        out.println(entry.getMessage());

	        R t = entry.getTokenRef();
	        if (t != null)
	        {
	            out.print(
	                Messages.bind(
	                    Messages.VPGLog_FilenameOffsetLength,
	                    new Object[] { t.getFilename(), t.getOffset(), t.getLength() }));
	        }
	    }
	}

	////////////////////////////////////////////////////////////////////////////////
	// Listener Support
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * An object which acts as an Observer [GoF] on the VPG error/warning log.
     *
     * @author Jeff Overbey
     */
    public static interface ILogListener
    {
        /** Callback method invoked when the VPG error/warning log changes. */
        void onLogChange();
    }

    private Set<ILogListener> listeners = new HashSet<ILogListener>();

    /** Adds the given object as a Observer [GoF] of the VPG error/warning log */
    public void addLogListener(ILogListener listener)
    {
        listeners.add(listener);
        listener.onLogChange();
    }

    /** Removes the given object as a Observer [GoF] of the VPG error/warning log */
    public void removeLogListener(ILogListener listener)
    {
        listeners.remove(listener);
    }

    protected void notifyListeners()
    {
        for (ILogListener listener : listeners)
            listener.onLogChange();
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Persistence Support
    ////////////////////////////////////////////////////////////////////////////////

    private static final String EOL = System.getProperty("line.separator"); //$NON-NLS-1$

    private static final String EOL_ESCAPE = "&EOL;"; //$NON-NLS-1$
    
    /**
     * Writes the log to a file.
     * @since 2.0
     */
    public void writeToFile() throws IOException
    {
        Writer output = new BufferedWriter(new FileWriter(logFile));
        
        try
        {
            R tokenRef = null;
            for (int i = 0; i < log.size(); i++)
            {
                Entry entry = log.get(i);

                output.write(Boolean.toString(entry.isWarning())+
                    EOL);
                
                tokenRef = entry.getTokenRef();
                if (tokenRef == null)
                    output.write(EOL);
                else
                    output.write(tokenRef.getFilename() + "," + //$NON-NLS-1$
                        Integer.toString(tokenRef.getOffset()) + "," + //$NON-NLS-1$
                        Integer.toString(tokenRef.getLength()) +
                        EOL);
                
                output.write(entry.getMessage().replaceAll(EOL, EOL_ESCAPE) + EOL);
            }
        }
        finally
        {
            output.close();
        }
    }
    
    /**
     * Reads the log which from a file back into memory.
     * <p>
     * Log entries have the format:
     * <pre>
     *      isWarning
     *      TokenRef filename, offset, length
     *      message
     * </pre>
     * @since 2.0
     */
    public void readLogFromFile()
    {
        try
        {
            FileInputStream fstream = new FileInputStream(logFile);
            BufferedReader bRead = new BufferedReader(new InputStreamReader(fstream));
            
            clear();
            
            String line;
            while ((line = bRead.readLine()) != null)
            {
                boolean isWarning = Boolean.parseBoolean(line);
                
                //read tokenRef values
                line = bRead.readLine();
                R tokenRef;
                if (line.trim().equals("")) //$NON-NLS-1$
                {
                    tokenRef = null;
                }
                else
                {
                    String[] tokenRefString = line.split("\\,"); //$NON-NLS-1$
                    tokenRef = locator.getVPGNode(
                        tokenRefString[0],
                        Integer.parseInt(tokenRefString[1]),
                        Integer.parseInt(tokenRefString[2]));
                }
                
                //read message
                line = bRead.readLine();
                log.add(new Entry(isWarning, line.replaceAll(EOL_ESCAPE, EOL), tokenRef));
            }
            
            bRead.close();
            fstream.close();
        }
        catch (Exception e)
        {
            return;
        }
        finally
        {
            notifyListeners();
        }
    }
}
