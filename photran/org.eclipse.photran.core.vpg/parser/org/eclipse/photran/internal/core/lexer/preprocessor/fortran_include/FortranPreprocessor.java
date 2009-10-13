/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.FreeFormLexerPhase1;
import org.eclipse.photran.internal.core.lexer.LineInputStream;

/**
 * An <code>InputStream</code> that recognizes and processes Fortran INCLUDE lines.
 * <p> 
 * This class implements an {@link InputStream} the provides the contents of a
 * Fortran file <i>after</i> INCLUDE lines have been processed.  Photran's
 * machine-constructed, free-form lexer ({@link FreeFormLexerPhase1}) tokenizes
 * this InputStream, oblivious to the fact that it is actually tokenizing
 * preprocessed text.  However, a special subclass
 * ({@link PreprocessingFreeFormLexerPhase1}) takes care of mapping the
 * preprocessed tokens back to their images and locations in the original files.
 * This is done by invoking the various public methods provided by this class
 * ({@link #getDirectiveAtOffset(int)}, {@link #getFilenameAtOffset(int)},
 * and several others); the lexer provides an offset in the preprocessed stream,
 * and those methods provide the INCLUDE line (if any) that was active at that
 * offset, or the filename from which the text at that offset came, etc.  
 * 
 * @author Jeff Overbey
 */
public final class FortranPreprocessor extends InputStream
{
    /**
     * Utility class.  Originally, we simply used a <pre>Stack<LineInputStream></pre>,
     * but a profile revealed that Stack#peek was consuming a large amount of time
     * due to repeated invocations.  Now, the #topStream field in this class is
     * accessed instead, resulting in a significant performance improvement
     * (3700 ms down to 2500 ms on HugeFile#testLexHugeFile unit test).
     */
    private static final class StreamStack
    {
        private Stack<LineInputStream> streamStack = new Stack<LineInputStream>();
        private LineInputStream topStream = null;
        
        public void push(LineInputStream lineInputStream)
        {
            streamStack.push(lineInputStream);
            topStream = lineInputStream;
        }

        public int size()
        {
            return streamStack.size();
        }

        public void pop()
        {
            streamStack.pop();
            topStream = streamStack.isEmpty() ? null : streamStack.peek();
        }
    }
    
	private static final Pattern INCLUDE_LINE = Pattern.compile("[ \t]*[Ii][Nn][Cc][Ll][Uu][Dd][Ee][ \t]+[\"']([^\r\n\"]*)[\"'][ \t]*(![^\r\n]*)?[\r\n]*");
    private static final int INCLUDE_LINE_CAPTURING_GROUP_OF_FILENAME = 1;
    
    private IncludeLoaderCallback callback;
    
    private IFile topLevelFile;
    
    private StreamStack streamStack;
    
    private int offset = 0, line = 1;
    
    private LinkedList<FortranIncludeDirective> directivesInTopLevelFile;
    private ArrayList<Integer> directiveStartOffsets;
    
    private LinkedList<String> fileNames;
    private ArrayList<Integer> fileStartOffsets;
    private ArrayList<Integer> fileStartOffsetAdjustments;
    private ArrayList<Integer> fileStartLines;
    private ArrayList<Integer> fileStartLineAdjustments;
    
    public FortranPreprocessor(InputStream readFrom, IFile file, String filename, IncludeLoaderCallback callback) throws IOException
    {
        this.topLevelFile = file;
        this.callback = callback;
        
        streamStack = new StreamStack();
        streamStack.push(new LineInputStream(readFrom, filename));
        
        directivesInTopLevelFile = new LinkedList<FortranIncludeDirective>();
        directivesInTopLevelFile.add(null);
        directiveStartOffsets = new ArrayList<Integer>();
        directiveStartOffsets.add(new Integer(0));
        
        fileNames = new LinkedList<String>();
        fileNames.add(filename);
        fileStartOffsets = new ArrayList<Integer>();
        fileStartOffsets.add(new Integer(0));
        fileStartOffsetAdjustments = new ArrayList<Integer>();
        fileStartOffsetAdjustments.add(new Integer(0));
        fileStartLines = new ArrayList<Integer>();
        fileStartLines.add(new Integer(1));
        fileStartLineAdjustments = new ArrayList<Integer>();
        fileStartLineAdjustments.add(new Integer(0));
    }
    
    public String getFilenameAtOffset(int offset)
    {
        for (int i = fileStartOffsets.size()-1; i >= 0; i--)
            if (((Integer)fileStartOffsets.get(i)).intValue() <= offset)
                return (String)fileNames.get(i);
        
        return null;
    }
    
    public int getStartOffsetOfFileContainingStreamOffset(int offset)
    {
        //System.out.println("getFileOffsetFromStreamOffset " + offset);
        for (int i = fileStartOffsets.size()-1; i >= 0; i--)
        {
            int fileStartOffset = ((Integer)fileStartOffsets.get(i)).intValue();
            if (fileStartOffset <= offset)
                return fileStartOffset;
        }
        
        throw new IllegalArgumentException();
    }

    public FortranIncludeDirective getDirectiveAtOffset(int offset)
    {
        for (int i = directiveStartOffsets.size()-1; i >= 0; i--)
            if (((Integer)directiveStartOffsets.get(i)).intValue() <= offset)
                return (FortranIncludeDirective)directivesInTopLevelFile.get(i);
        
        throw new IllegalArgumentException();
    }

    public int getFileLineFromStreamLine(int line)
    {
        for (int i = fileStartLines.size()-1; i >= 0; i--)
            if (((Integer)fileStartLines.get(i)).intValue() <= line)
                return line - ((Integer)fileStartLineAdjustments.get(i)).intValue();
        
        throw new IllegalArgumentException();
    }
    
    public int getFileOffsetFromStreamOffset(int offset)
    {
        for (int i = fileStartOffsets.size()-1; i >= 0; i--)
            if (((Integer)fileStartOffsets.get(i)).intValue() <= offset)
                return offset - ((Integer)fileStartOffsetAdjustments.get(i)).intValue();
        
        throw new IllegalArgumentException();
    }
    
    public int read() throws IOException
    {
        LineInputStream currentStream = streamStack.topStream;
        if (currentStream.atBOL()) checkForInclude();
        currentStream = streamStack.topStream;
        if (currentStream.atEOF()) finishInclude();
        int result = currentStream.read();
        if (result >= 0)
        {
            offset++;
            if (result == '\n') line++;
        }
        return result;
    }

    private boolean inTopLevelFile()
    {
        return streamStack.size() <= 1;
    }

    private void checkForInclude() throws FileNotFoundException, IOException
    {
        Matcher m = INCLUDE_LINE.matcher(streamStack.topStream);
        if (m.matches())
        {
            LineInputStream origStream = streamStack.topStream;
            String includeLine = origStream.currentLine();
            String fileToInclude = m.group(INCLUDE_LINE_CAPTURING_GROUP_OF_FILENAME);
            
            InputStream newStream = findIncludedFile(fileToInclude);
            if (newStream != null)
            {
                if (inTopLevelFile())
                {
                    directivesInTopLevelFile.add(new FortranIncludeDirective(includeLine));
                    directiveStartOffsets.add(new Integer(offset));
                }

                origStream.setRestartOffset(offset + includeLine.length());
                origStream.setRestartLine(line + 1);
                
                streamStack.push(new LineInputStream(newStream, fileToInclude, offset, line));
                
                fileNames.add(fileToInclude);
                fileStartOffsets.add(new Integer(offset));
                fileStartOffsetAdjustments.add(new Integer(getOffsetAdjustment(0)));
                fileStartLines.add(new Integer(line));
                fileStartLineAdjustments.add(new Integer(getLineAdjustment(1)));
                
                // No line++: Make sure the included file's stream line is the same as the INCLUDE line's
                origStream.advanceToNextLine();
            }
        }
    }

    protected InputStream findIncludedFile(String fileToInclude) throws IOException
    {
        try
        {
            return callback.getIncludedFileAsStream(fileToInclude);
        }
        catch (FileNotFoundException e)
        {
            callback.logError(
                "Unable to locate INCLUDE file \""
        	    + fileToInclude + "\""
        	    //+ " (working directory: "
                //+ new File(".").getCanonicalPath()
                //+ ")"
                , topLevelFile, offset);
            return null;
        }
    }

    private void finishInclude() throws IOException
    {
        if (!inTopLevelFile())
        {
            streamStack.topStream.close();
            streamStack.pop();
            
            fileNames.add(streamStack.topStream.getFilename());
            fileStartOffsets.add(new Integer(offset));
            fileStartOffsetAdjustments.add(new Integer(getOffsetAdjustment(streamStack.topStream.getRestartOffset())));
            fileStartLines.add(new Integer(line));
            fileStartLineAdjustments.add(new Integer(getLineAdjustment(streamStack.topStream.getRestartLine())));
        }
        
        // The above may have returned us to the top-level file
        
        if (inTopLevelFile())
        {
            directivesInTopLevelFile.add(null);
            directiveStartOffsets.add(new Integer(offset));
        }
    }

    private int getOffsetAdjustment(int desiredOffset)
    {
        // Want offset-adjustment = desiredOffset
        //   =>       -adjustment = desiredOffset - offset
        //   =>        adjustment = offset - desiredOffset;
        return offset - desiredOffset;
    }

    private int getLineAdjustment(int desiredLine)
    {
        // Similar to above 
        return line - desiredLine;
    }
    
    public IFile getTopLevelFile()
    {
        return topLevelFile;
    }
}
