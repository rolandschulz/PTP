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
package org.eclipse.photran.internal.core.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

/**
 * Preprocesses the input stream, discarding all whitespaces and comment lines and concatenating
 * continuation lines.  Additionally, it holds a mapping to the character-positions in the file (for
 * correct start/end line/col in the {@link IToken} objects).
 * 
 * @author Dirk Rossow
 */
class FixedFormLexerPrepass {
	static final int inStart=0;
	static final int inHollerith=1;
	static final int inDblQuote=2;
	static final int inDblQuoteEnd=3;
	static final int inQuote=4;
	static final int inQuoteEnd=5;
	
	//private String fileContent = "";
	StringBuilder strBuilder = new StringBuilder();

	private int state = inStart;
    int hollerithLength = -2; //-1: hollerith could start, -2: hollerith cant start

	private int actLinePos = 0;
	private PreLexerLine actLine = null;
	private OffsetLineReader in;

	private DynamicIntArray lineMapping = new DynamicIntArray(1000);
	private DynamicIntArray columnMapping = new DynamicIntArray(1000);
	private DynamicIntArray offsetMapping = new DynamicIntArray(1000);
	
	//Maps whitespace(string) to position in file (line, col, offset) tuple
	private HashMap whiteSpaceMapping = new HashMap();	
	//Used to accumulate whitespace between lines (multi-line comments, etc) because
	// the string is processed on per-line basis
	private String prevWhiteSpace = "";
	
	private int EOFLinePos=0;
	private int EOFColPos=0;
	private int EOFOffsetPos=0;
	
	
	public FixedFormLexerPrepass(Reader in) {
		this.in = new OffsetLineReader(in);
	}
	
	public FixedFormLexerPrepass(InputStream in) {
		this(new InputStreamReader(in));
	}
	
	public int getLine(int absCharPos) {
		if (absCharPos<0) return 0;
        int lastCharPos = lineMapping.length() - 1;
        return lineMapping.get(Math.min(absCharPos, lastCharPos));
	}
	
	public int getColumn(int absCharPos) {
		if (absCharPos<0) return 0;
        int lastCharPos = lineMapping.length() - 1;
		return columnMapping.get(Math.min(absCharPos, lastCharPos));
	}
	
	public int getOffset(int absCharPos) {
		if (absCharPos<0) return absCharPos;
        int lastCharPos = lineMapping.length() - 1;
		return offsetMapping.get(Math.min(absCharPos, lastCharPos));
	}
	
	public int read() throws Exception {
		int c = internalRead();
		//System.out.print((char)c);
		return c;
	}
	
	public String getTokenText(int offset, int length)
	{
	    int strLen = strBuilder.length();
	    if(offset >= 0 && length > 0 && offset < strLen && length <= strLen && offset+length < strLen)
	    {
	        String res = strBuilder.substring(offset, offset+length);
	        return res;
	    }
	    return "";
	}
	
	public String getTokenText(int offset)
	{
	    if(offset >= 0 && offset < strBuilder.length())
	    {
	        String res = strBuilder.substring(offset);
	        return res;
	    }
	    return "";
	}
	
	public String getTrailingWhitespace()
	{
	    String trimmed = strBuilder.toString().trim();
	    //This gets the index of the beginning of the whitespace AFTER the first "end of line" symbol on the last line
	    // with actual text of the file
	    int start = strBuilder.indexOf(trimmed) + trimmed.length() + in.getFileEOL().length();
	    String res = strBuilder.substring(start);
	    return res;
	}
	
	private void markPosition(int line, int col, int offset) {
		offsetMapping.pushBack(offset);
		lineMapping.pushBack(line);
		columnMapping.pushBack(col);
	}
	
	private int internalRead() throws Exception 
	{
	    PreLexerLine prevLine = null;  
		for (;;) 
		{
			if (actLine==null) 
			{
			    actLine=getNextLine();
				if (actLine==null) 
				{
					markPosition(EOFLinePos,EOFColPos,EOFOffsetPos);
					return -1;
				}
				else 
				    actLinePos=0;
			} 
			else if(actLinePos==actLine.length() || 
			        actLinePos==PreLexerLine.COLWIDTH) 
			{ //test if continuation-line follows, else send \n
				prevLine=actLine;
				actLine=getNextLine();
				if (actLine==null) 
				{ //end of file
					hollerithLength=-2;
					state=inStart;
					markPosition(prevLine.linePos,actLinePos,prevLine.offset+actLinePos+1);
					return '\n';
				} 
				else if (actLine.type==PreLexerLine.CONTINUATION) 
				{
					actLinePos=6;
				} 
				else if(actLine.type==PreLexerLine.COMMENT)
				{
				    prevWhiteSpace=prevWhiteSpace.concat(actLine.getText());
				    prevWhiteSpace=prevWhiteSpace.concat(in.getFileEOL());
				    actLinePos = actLine.length();
				}
				else 
				{
					actLinePos=0;
					hollerithLength=-2;
					state=inStart;
					markPosition(prevLine.linePos,prevLine.length(),prevLine.offset+prevLine.length());
					return '\n';
				}
			}
			
			actLinePos = getNextSigPos(actLine,actLinePos);

			if (actLinePos<0) 
			{
				actLinePos=actLine.length();
			} 
			else 
			{
				markPosition(actLine.linePos,actLinePos,actLine.offset+actLinePos);
				return actLine.charAt(actLinePos++);
			}
		}
	}

//	private PreLexerLine getNextNonCommentLine() {
//		for (;;) {
//			PreLexerLine line = getNextLine();
//			if (line==null) return null;
//			if (line.type!=PreLexerLine.COMMENT) return line;
//			else {
//				//TODO: save non-tree tokens 
//			}
//		}
//	}
	
	private PreLexerLine getNextLine() {
		try {
			int actOffset=in.getOffset();
			String line = in.readLine();
			if (line==null) 
			    return null;
			
			strBuilder.append(line);
			strBuilder.append(in.getFileEOL());
			//fileContent = fileContent.concat(line);
			//fileContent = fileContent.concat(in.getFileEOL());
			EOFLinePos=in.getLineNumber()+1;//-1; //Move that token past the last line
			EOFColPos=0;//line.length();
			EOFOffsetPos=actOffset+line.length()+in.getFileEOL().length();//To accomodate for End-of-line statement
			PreLexerLine pll = new PreLexerLine(line,in.getLineNumber()-1,actOffset);
			return pll;
		} catch (IOException e) {
			return null;
		}
	}
	
	private boolean isWhitespace(char c)
	{
	    return c==' ' || c=='\t' || c=='\r' || c=='\n';
	}
	
	public String getWhitespaceBefore(int ln, int lastCol, int lastOffset)
	{
	    int colBefore = lastCol;
	    int offsetBefore = lastOffset;
	    
	    if(colBefore < 0 || offsetBefore < 0)
	        return "";
	    
	    //Create a positionInFile object, with line,col and offset set to the END of the potential whitespace
	    PositionInFile posInFile = new PositionInFile(ln, colBefore, offsetBefore, false);
	    String result = (String)whiteSpaceMapping.get(posInFile);
	    /* Iterator iter = whiteSpaceMapping.keySet().iterator();
	    while(iter.hasNext())
	    {
	        PositionInFile temp = (PositionInFile)iter.next();
	        if(posInFile.isSameEnd(temp))
	        {
	            return (String)whiteSpaceMapping.get(temp);
	        }
	    }*/
	    if(result==null)
	        return "";
	    
	    return result;
	}
	
	private int extractWhitespace(PreLexerLine line, int startPos)
	{
	    String whiteAgg = "";
	    int charPos = startPos;
	    int startWhitespace = -1;
	    int length = line.length();
	    
	    if(line.type == PreLexerLine.COMMENT)
	    {
	        if(startWhitespace == -1)
                startWhitespace = charPos;
	        //Append current line to prevWhiteSpace
            prevWhiteSpace = prevWhiteSpace.concat(line.getText().substring(charPos));
            //Since PreLexerLine throws away whitespace, attach a "new line" character to our whitespace
            prevWhiteSpace = prevWhiteSpace.concat(in.getFileEOL()); 
            charPos = -1; //Finished line
            //Don't insert the white-space because full-line comments are associated with whatever token
            // you find on the NEXT line, so don't add them to the map yet
            return charPos;
	    }
	    
	    for(;charPos<line.length();charPos++)
	    {
	        char c = line.charAt(charPos);
	        //If it is a continuation line, treat character at position 6 as whitespace
            if(line.type == PreLexerLine.CONTINUATION && charPos == 6)
            {
                if(startWhitespace == -1)
                    startWhitespace = 0;
                String prevLineWhite = in.getFileEOL().concat(line.getText().substring(0, 6));
                whiteAgg = whiteAgg.concat(prevLineWhite);
            }
            
            if(isWhitespace(c))
	        {
	            if(startWhitespace == -1)
	                startWhitespace = charPos;
	            whiteAgg = whiteAgg.concat(String.valueOf(c));
	        }
	        else if(c=='!' || charPos >= PreLexerLine.COLWIDTH) //It a comment, grab the rest of the line
	        {
	            if(startWhitespace == -1)
                    startWhitespace = charPos;
	            whiteAgg = whiteAgg.concat(line.getText().substring(charPos));
	            charPos = length;  //Finished line
	            break;
	        }
	        else //Not a whitespace character
	        {
	            break;
	        }
	    }
	    if((whiteAgg.length() != 0 || prevWhiteSpace.length() != 0) && startWhitespace != -1)
	    {
    	    PositionInFile posInFile = new PositionInFile(line.linePos, 
    	                                                  startWhitespace,
    	                                                  charPos,
    	                                                  line.offset+startWhitespace,
    	                                                  line.offset+charPos);
    	    String combinedWhite = prevWhiteSpace.concat(whiteAgg);
            whiteSpaceMapping.put(posInFile, combinedWhite);
            prevWhiteSpace = "";
            
            //If we moved into the comments, return -1 since we gobbled those up
            if(charPos >= PreLexerLine.COLWIDTH)
                charPos = -1;
	    }
	    if(charPos >= length) //If we "gobbled up" the entire line, return -1 to
	        return -1;                 //signify the end of the line
	    
        return charPos;
	}
	
	// return: -1 : end of line reached
	private int getNextSigPos(PreLexerLine line, int startPos) {

		  for (int charPos=startPos;charPos<line.length();++charPos)  
		  {
			char c = line.charAt(charPos);
			
			if (line.type==PreLexerLine.CPPDIRECTIVE) 
			    return charPos;
			if (line.type==PreLexerLine.CONTINUATION && charPos<=5) 
			    continue;
			
			//A bit ugly. This pretty much goes through and stores
			// all the whitespace from a given character until the 
			// first non-whitespace character in a map PositionInFile -> String, 
			// so that it can later be attached to appropriate tokens. 
			if (state==inStart && 
			    (
    			    isWhitespace(c) || 
                    c=='!' || 
                    line.type == PreLexerLine.COMMENT || 
                    charPos >= PreLexerLine.COLWIDTH || 
                    line.type == PreLexerLine.CONTINUATION
                 ))
            {
                charPos = extractWhitespace(line, charPos); 
                //If we got to the end of the line, no need to continue
                if(charPos >= 0 && charPos < line.length())
                    c = line.charAt(charPos);
                else
                    return -1;
            }
			
			if (state==inStart) 
			{                                              
				if (charPos<=4 && !Character.isDigit(c)) 
				    continue; //only allow digits(label) in column 0-4
				else if (c=='\'') 
				{
					hollerithLength=-1;
					state=inQuote;
				} 
				else if (c=='\"') 
				{
					hollerithLength=-1;
					state=inDblQuote;
				} 
				else if ((c=='h') || (c=='H')) 
				{
					if (hollerithLength>0) 
					    state=inHollerith;
					else if (hollerithLength<0) 
					    hollerithLength=-2;
				} 
				else if (hollerithLength!=-2 && Character.isDigit(c)) 
				{
					if (hollerithLength==-1) 
					    hollerithLength=Character.digit(c,10);
					else 
					    hollerithLength=hollerithLength*10+Character.digit(c,10);
				} 
				else if (Character.isLetter(c) || c=='_') 
				{
					hollerithLength=-2;
				} 
				else 
				{
					if (charPos==0) 
					    hollerithLength=-2;// ignore label at start of line
					else 
					    hollerithLength=-1;
				}
				return charPos;
				
			} 
			else if (state==inQuote) 
			{
				if (c=='\'') 
				    state=inQuoteEnd;
				return charPos;
			} else if (state==inQuoteEnd) 
			{
				if (c=='\'') 
				{
					state=inQuote;
					return charPos;
				} 
				else 
				{
					state=inStart;
					charPos--;
				}
			} 
			else if (state==inDblQuote) 
			{
				if (c=='\"') 
				    state=inQuoteEnd;
				return charPos;
			} 
			else if (state==inDblQuoteEnd) 
			{
				if (c=='\"') 
				{
					state=inDblQuote;
					return charPos;
				} 
				else 
				{
					state=inStart;
					charPos--;
				}				
			} 
			else if (state==inHollerith) 
			{
				hollerithLength--;
				if (hollerithLength==0) 
				    state=inStart;
				return charPos;
			} 
			else 
			{ //undefined state
				throw new RuntimeException("Undefined state in FixedFormPreLexer");
			}
		}
		return -1; //end of line reached
	}
    
    public String getFileEOL()
    {
        return in.getFileEOL();
    }
}

class DynamicIntArray {
	final double RESIZEFAC=1.1;
	final int RESIZEADD=10;
	
	private int[] v=null;
	private int length=0;
	
	DynamicIntArray(int reserveSize) {
		ensureSize(reserveSize);
	}

	DynamicIntArray() {
	}

	int length() {
		return length;
	}
	
	int get(int pos) {
		if (pos<0 || pos>=length) {
			throw new ArrayIndexOutOfBoundsException(pos);
		}
		return v[pos];
	}
	
	void pushBack(int value) {
		ensureSize(length+1);
		v[length]=value;
		length++;
	}
	
	private void ensureSize(int size) {
		if (v==null || v.length<size) {
			int[] newArray = new int[(int) (size*RESIZEFAC+RESIZEADD)];
			if (v!=null) System.arraycopy(v,0,newArray,0,length);
			v=newArray;
		}
	}
	
	
}

class PreLexerLine {
	static public final int COLWIDTH=72;

	static final int COMMENT=0;
	static final int CONTINUATION=1;
	static final int STMT=2;
	static final int CPPDIRECTIVE=3;
	
	final int linePos;
	final int offset;
	final int type;
	private final String lineText;
	
	
		
	PreLexerLine(String _lineText, int linePos, int offset) {
		this.linePos=linePos;
		this.offset=offset;

		/* truncate anything beyond 72 characters */	    
		/*if (_lineText.length()>COLWIDTH) {
			lineText=_lineText.substring(0,COLWIDTH);
		} else {
			lineText=_lineText;
		}*/
		lineText = _lineText;

		
		String trimmedText=lineText.trim();

//check for empty line
		if (trimmedText.length()==0) type=COMMENT;

//		check for f77-style comment
		else if (lineText.charAt(0)=='C'||
				lineText.charAt(0)=='c'||
				lineText.charAt(0)=='*'||
				lineText.charAt(0)=='$') type=COMMENT;

//check for f90-style comment
		else if (trimmedText.startsWith("!")) type=COMMENT;

//check for cpp-dirctive
		else if (trimmedText.startsWith("#")) type=CPPDIRECTIVE;
		
//check if line is empty up to COLWIDTH
		else if (lineText.length()>COLWIDTH && lineText.substring(0,COLWIDTH).trim().length()==0) type=COMMENT;
		
//check for tab in column 0-5
		else if (lineText.indexOf('\t')>=0 && lineText.indexOf('\t')<=5) type=STMT;

//check for continuation
		else if (lineText.length()>=6 &&
				lineText.charAt(5)!='0' && lineText.charAt(5)!=' ') type = CONTINUATION;

		else type=STMT;
	}
	 
	public int length() {
		return lineText.length();
	}
	
	public char charAt(int pos) {
		return lineText.charAt(pos);
	}
	
	public String getText()
	{
	    return this.lineText;
	}

	public String toString() {
		return "Line "+linePos+": "+lineText; 
	}
}

class PositionInFile
{
    private int line = -1;
    private int startCol = -1;
    private int startOffset = -1;
    private int endCol = -1;
    private int endOffset = -1;
    
    public PositionInFile(int ln, int stCl, int endCl, int stOfst, int endOfst)
    {
        this.line = ln;
        this.startCol = stCl;
        this.endCol = endCl;
        this.startOffset = stOfst;
        this.endOffset = endOfst;
    }
    
    public PositionInFile(int ln, int cl, int ofst, boolean isStart)
    {
        this.line = ln;
        if(isStart)
        {
            this.startCol = cl;
            this.startOffset = ofst;
        }
        else
        {
            this.endCol = cl;
            this.endOffset = ofst;
        }
    }
    
    public int getLine()
    {
        return this.line;
    }
    
    public int getStartCol()
    {
        return this.startCol;
    }
    
    public int getStartOffset()
    {
        return this.startOffset;
    }
    
    public int getEndCol()
    {
        return this.startCol;
    }
    
    public int getEndOffset()
    {
        return this.startOffset;
    }
    
    public boolean isSameStart(PositionInFile other)
    {
        return (other.line == this.line &&
                other.startCol == this.startCol &&
                other.startOffset == this.startOffset);
    }
    
    public boolean isSameEnd(PositionInFile other)
    {
        return (other.line == this.line &&
                other.endCol == this.endCol &&
                other.endOffset == this.endOffset);
    }
    
    //Override
    public int hashCode()
    {
        return this.endOffset;
    }
    
    public boolean equals(Object obj)
    {
        return ((PositionInFile)obj).endOffset == this.endOffset;
    }
}

class OffsetLineReader {
	private BufferedReader bReader;
	private StringBuffer sBuf;
	private int lineNumber=0;
	private int offset=0;
	private String fileEOL = null;


	private int charBuf=-1;
	
	public OffsetLineReader(Reader reader) {
		bReader=new BufferedReader(reader);
		sBuf=new StringBuffer();
	}
	
	private int getNextChar() throws IOException {
		int c = bReader.read();
		if (c!=-1) offset++;
		return c;
	}
	
	public String readLine() throws IOException {
		if (charBuf<0) {
			charBuf=getNextChar();
			if (charBuf<0) return null;
		}
		
		sBuf.setLength(0);
		while(true) {
			if (charBuf=='\n') {
			    if (fileEOL == null) fileEOL = "\n";
				charBuf=getNextChar();
				lineNumber++;
				break;
			} else if (charBuf=='\r') {
				charBuf=getNextChar();
				if (charBuf=='\n')
			    {
                    if (fileEOL == null) fileEOL = "\r\n";
				    charBuf=getNextChar();
			    }
				else
				{
				    if (fileEOL == null) fileEOL = "\r";
				}
				lineNumber++;
				break;
			} else if (charBuf==-1) {
				break;
			} else {
				sBuf.append((char) charBuf);
				charBuf=getNextChar();
			}
		}
		return sBuf.toString();
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getOffset() {
		if (charBuf<0) return offset;
		else return offset-1;
	}
	
	public String getFileEOL() {
	    return fileEOL;
	}
}
