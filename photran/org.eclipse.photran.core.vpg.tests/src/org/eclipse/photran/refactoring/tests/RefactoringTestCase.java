/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.refactoring.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.util.LineCol;

public abstract class RefactoringTestCase extends BaseTestFramework
{
    private static HashMap<String, ArrayList<Integer>> lineMaps = new HashMap<String, ArrayList<Integer>>();
    
    public RefactoringTestCase()
    {
    	if (!PhotranVPG.inTestingMode()) fail("WHEN RUNNING JUNIT TESTS, THE \"TESTING\" ENVIRONMENT VARIABLE MUST BE SET");
    }
    
    protected IFile importFile(String srcDir, String filename) throws Exception
    {
        //project.getProject().getFile(filename).delete(true, new NullProgressMonitor());
        IFile result = super.importFile(filename, readTestFile(srcDir, filename));
        //project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        return result;
    }

    protected String readTestFile(String srcDir, String filename) throws IOException, URISyntaxException
    {
        ArrayList<Integer> lineMap = new ArrayList<Integer>(50);
        lineMaps.put(filename, lineMap);
        lineMap.add(0); // Offset of line 1
        return readStream(lineMap, Activator.getDefault().getBundle().getResource(srcDir + "/" + filename).openStream());
    }
    
    protected String readStream(ArrayList<Integer> lineMap, InputStream inputStream) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        for (int offset = 0, ch = in.read(); ch >= 0; ch = in.read())
        {
            sb.append((char)ch);
            offset++;
            
            if (ch == '\n' && lineMap != null)
            {
                //System.out.println("Line " + (lineMap.size()+1) + " starts at offset " + offset);
                lineMap.add(offset);
            }
        }
        in.close();
        return sb.toString();
    }

    protected String readStream(InputStream inputStream) throws IOException
    {
        return readStream(null, inputStream);
    }

    protected String readWorkspaceFile(String filename) throws IOException, CoreException
    {
        return readStream(project.getFile(filename).getContents());
    }

    /**
     * @param filename
     * @param line line number, starting at 1
     * @param col column number, starting at 1
     */
    protected int getLineColOffset(String filename, LineCol lineCol)
    {
        return lineMaps.get(filename).get(lineCol.getLine()-1) + (lineCol.getCol()-1);
    }
    
    public void testLevenshtein()
    {
        assertEquals(0, levenshteinDistance("", ""));
        assertEquals(0, levenshteinDistance("kitten", "kitten"));
        assertEquals(6, levenshteinDistance("", "kitten"));
        assertEquals(6, levenshteinDistance("kitten", ""));
        assertEquals(3, levenshteinDistance("kitten", "sitting"));
        assertEquals(3, levenshteinDistance("kitten", "kit"));
        assertEquals(1, levenshteinDistance("kitten", "kittten"));
        assertEquals(2, levenshteinDistance("kitten", "kien"));
    }
    
    /**
     * Computes the Levenshtein distance between two strings.
     * 
     * Based on pseudocode from http://en.wikipedia.org/wiki/Levenshtein_distance
     * 
     * @return the Levenshtein distance between <code>s</code> and <code>t</code>
     */
    protected int levenshteinDistance(String s, String t)
    {
        int m = s.length(), n = t.length();
        
        int[][] d = new int[m+1][n+1];
      
        for (int i = 1; i <= m; i++)
            d[i][0] = i;
        for (int j = 1; j <= n; j++)
            d[0][j] = j;
      
        for (int i = 1; i <= m; i++)
        {
            for (int j = 1; j <= n; j++)
            {
                int cost = s.charAt(i-1) == t.charAt(j-1) ? 0 : 1;
                d[i][j] = min(d[i-1][j] + 1,       // deletion
                              d[i][j-1] + 1,       // insertion
                              d[i-1][j-1] + cost); // substitution
            }
        }
      
        return d[m][n];
    }
    
    private int min(int a, int b, int c)
    {
        return Math.min(Math.min(a, b), c);
    }
    
    protected void assertLevDist(String errorMessage, int expected, String s, String t)
    {
        int actual = levenshteinDistance(s, t);
        
        if (actual != expected)
        {
            // Use assertEquals so that JUnit will pop up a comparison viewer
            assertEquals("Unexpected Levenshtein distance " + actual + " (expected " + expected + ") " + errorMessage,
                         s,
                         t);
        }
    }
    
    protected String compileAndRunFortranProgram() throws Exception
    {
        String compiler = System.getenv("COMPILER");
        if (compiler == null) return "";
        
        String exe = System.getenv("EXECUTABLE");
        if (exe == null) return "";
        
        ArrayList<String> args = new ArrayList<String>(8);
        args.add(compiler);
        args.add("-o");
        args.add(exe);
        for (IResource res : project.members())
            if (res instanceof IFile && res.getFileExtension().startsWith("f"))
                args.add(res.getName());
        
        System.out.println(toString(args));
        
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(project.getLocation().toFile());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        
        ConcurrentReader output = new ConcurrentReader(process.getInputStream());
        output.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0)
            return "Process exited abnormally with exit code " + exitCode + "\n" + output.toString();
        else
            return output.toString();
    }

    private String toString(ArrayList<String> args)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++)
            sb.append((i == 0 ? "" : " ") + args.get(i));
        return sb.toString();
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
        
        @Override public String toString()
        {
            return sb.toString();
        }
    }
}
