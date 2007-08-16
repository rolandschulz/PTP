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

/**
 * FixedFormLexerPrepass preprocesses the input stream. It discards all
 * whitespaces and comment lines and concatenates continuation lines. Additional
 * it holds a mapping to the character-positions in the file (for correct
 * start/end line/col in the Token objects).
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

	private int state = inStart;
    int hollerithLength = -2; //-1: hollerith could start, -2: hollerith cant start

	private int actLinePos = 0;
	private PreLexerLine actLine = null;
	private OffsetLineReader in;

	private DynamicIntArray lineMapping = new DynamicIntArray(1000);
	private DynamicIntArray columnMapping = new DynamicIntArray(1000);
	private DynamicIntArray offsetMapping = new DynamicIntArray(1000);
	
	
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

	private void markPosition(int line, int col, int offset) {
		offsetMapping.pushBack(offset);
		lineMapping.pushBack(line);
		columnMapping.pushBack(col);
	}
	
	private int internalRead() throws Exception {
		
		for (;;) {
			if (actLine==null) {
				actLine=getNextNonCommentLine();
				if (actLine==null) {
					markPosition(EOFLinePos,EOFColPos,EOFOffsetPos);
					return -1;
				}
				else actLinePos=0;
			} else if (actLinePos==actLine.length()
					|| actLinePos==PreLexerLine.COLWIDTH) { //test if continuation-line follows, else send \n

				PreLexerLine prevLine=actLine;
				actLine=getNextNonCommentLine();
				if (actLine==null) { //end of file
					hollerithLength=-2;
					state=inStart;
					markPosition(prevLine.linePos,actLinePos,prevLine.offset+actLinePos+1);
					return '\n';
				} else if (actLine.type==PreLexerLine.CONTINUATION) {
					actLinePos=6;
				} else {
					actLinePos=0;
					hollerithLength=-2;
					state=inStart;
					markPosition(prevLine.linePos,prevLine.length(),prevLine.offset+prevLine.length());
					return '\n';
				}
			}
			
			actLinePos = getNextSigPos(actLine,actLinePos);
			//TODO: save non-tree tokens 

			if (actLinePos<0) {
				actLinePos=actLine.length();
			} else {
				markPosition(actLine.linePos,actLinePos,actLine.offset+actLinePos);
				return actLine.charAt(actLinePos++);
			}
		}
	}

	private PreLexerLine getNextNonCommentLine() {
		for (;;) {
			PreLexerLine line = getNextLine();
			if (line==null) return null;
			if (line.type!=PreLexerLine.COMMENT) return line;
			else {
				//TODO: save non-tree tokens 
			}
		}
	}

	private PreLexerLine getNextLine() {
		try {
			int actOffset=in.getOffset();
			String line = in.readLine();
			if (line==null) return null;
			EOFLinePos=in.getLineNumber()-1;
			EOFColPos=line.length();
			EOFOffsetPos=actOffset;
			PreLexerLine pll = new PreLexerLine(line,in.getLineNumber()-1,actOffset);
			return pll;
		} catch (IOException e) {
			return null;
		}
	}

	// return: -1 : end of line reached
	private int getNextSigPos(PreLexerLine line, int startPos) {

		  for (int charPos=startPos;charPos<line.length();++charPos)  {
			char c = line.charAt(charPos);
			
			if (line.type==PreLexerLine.CPPDIRECTIVE) return charPos;
			if (line.type==PreLexerLine.CONTINUATION && charPos<=5) continue;
			
			if (state==inStart) {
				if (c==' ' || c=='\t') continue;
				else if (c=='!') {
					return -1; // rest of line is comment
				} else if (charPos<=4 && !Character.isDigit(c)) continue; //only allow digits(label) in column 0-4
				else if (c=='\'') {
					hollerithLength=-1;
					state=inQuote;
				} else if (c=='\"') {
					hollerithLength=-1;
					state=inDblQuote;
				} else if ((c=='h') || (c=='H')) {
					if (hollerithLength>0) state=inHollerith;
					else if (hollerithLength<0) hollerithLength=-2;
				} else if (hollerithLength!=-2 && Character.isDigit(c)) {
					if (hollerithLength==-1) hollerithLength=Character.digit(c,10);
					else hollerithLength=hollerithLength*10+Character.digit(c,10);
				} else if (Character.isLetter(c) || c=='_') {
					hollerithLength=-2;
				} else {
					if (charPos==0) hollerithLength=-2;// ignore label at start of line
					else hollerithLength=-1;
				}
				return charPos;
				
			} else if (state==inQuote) {
				if (c=='\'') state=inQuoteEnd;
				return charPos;
			} else if (state==inQuoteEnd) {
				if (c=='\'') {
					state=inQuote;
					return charPos;
				} else {
					state=inStart;
					charPos--;
				}
			} else if (state==inDblQuote) {
				if (c=='\"') state=inQuoteEnd;
				return charPos;
			} else if (state==inDblQuoteEnd) {
				if (c=='\"') {
					state=inDblQuote;
					return charPos;
				} else {
					state=inStart;
					charPos--;
				}				
			} else if (state==inHollerith) {
				hollerithLength--;
				if (hollerithLength==0) state=inStart;
				return charPos;
			} else { //undefined state
				throw new RuntimeException("Undefined state in FixedFormPreLexer");
			}
		}
		return -1; //end of line reached
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
		if (_lineText.length()>COLWIDTH) {
			lineText=_lineText.substring(0,COLWIDTH);
		} else {
			lineText=_lineText;
		}

		
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
	

	public String toString() {
		return "Line "+linePos+": "+lineText; 
	}
}


class OffsetLineReader {
	private BufferedReader bReader;
	private StringBuffer sBuf;
	private int lineNumber=0;
	private int offset=0;

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
				charBuf=getNextChar();
				lineNumber++;
				break;
			} else if (charBuf=='\r') {
				charBuf=getNextChar();
				if (charBuf=='\n') charBuf=getNextChar();
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
}
