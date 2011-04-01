/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class with static methods to run a command line program and return its output.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public final class Spawner
{
    /**
     * If the spawner exits with an error status, this determines whether the output will be dumped
     * to standard error before an exception is thrown.
     * 
     * @since 3.0
     */
    public static boolean SHOW_OUTPUT_ON_ERROR = true;
    
    private Spawner() {}

    /**
     * Attempts to run the given operating system program+arguments in the current working directory.
     * @return the process's combined output to standard output and standard error
     * @throws Exception if the process exits with a nonzero exit code
     */
    public static String run(String... args) throws Exception
    {
        return run(null, Arrays.asList(args));
    }

    /**
     * Attempts to run the given operating system program+arguments in the current working directory.
     * @return the process's combined output to standard output and standard error
     * @throws Exception if the process exits with a nonzero exit code
     */
    public static String run(List<String> args) throws Exception
    {
        return run(null, args);
    }

    /**
     * Attempts to run the given operating system program+arguments in the given working directory.
     * @return the process's combined output to standard output and standard error
     * @throws Exception if the process exits with a nonzero exit code
     */
    public static String run(File workingDirectory, String... args) throws Exception
    {
        return run(workingDirectory, Arrays.asList(args));
    }

    /**
     * Attempts to run the given operating system program+arguments in the given working directory.
     * @return the process's combined output to standard output and standard error
     * @throws Exception if the process exits with a nonzero exit code
     */
    public static String run(File workingDirectory, List<String> args) throws Exception
    {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(workingDirectory);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        ConcurrentReader output = new ConcurrentReader(process.getInputStream());
        synchronized (output)
        {
            output.start();
            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                if (SHOW_OUTPUT_ON_ERROR)
                    System.err.println(output.toString());
                throw new Exception(
                    Messages.bind(Messages.Spawner_ProcessExitedAbnormally,
                                  exitCode,
                                  output.toString()));
            }
            waitFor(output);
        }
        return output.toString();
    }

    private static void waitFor(ConcurrentReader output)
    {
        try
        {
            output.wait();
        }
        catch (InterruptedException e)
        {
        }
    }

    private static class ConcurrentReader extends Thread
    {
        private InputStream stdout;
        private StringBuilder sb;

        public ConcurrentReader(InputStream stdout)
        {
            this.stdout = stdout;
            this.sb = new StringBuilder();
        }

        @Override public void run()
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(stdout));
            try
            {
                for (String line = in.readLine(); line != null; line = in.readLine())
                {
                    sb.append(line);
                    sb.append('\n');
                }
                done();
            }
            catch (IOException e)
            {
                sb.append(e.toString());
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    sb.append(e.toString());
                }
            }
        }

        private synchronized void done()
        {
            notifyAll();
        }

        @Override public String toString()
        {
            return sb.toString();
        }
    }
}
