package org.eclipse.ptp.pldt.common.util;

/**
 * Class encapsulates locational information for creating  markers representing Artifacts.
 * 
 * 
 */
public class SourceInfo
{
	/** starting line location within in the file */
    private int startingLine;
    /** line number within the file */
    private int start;
    /** end position (character position) relative to start of file */
    private int end;
    /** type of construct, e.g. Artifact.CONSTANT, etc. */
    private int constructType;

    /** explicit default contstructor with no info; assumed to
     * be filled in with set methods.
     *
     */
    public SourceInfo(){
    }
    
    /** ctor with info already filled in */
    public SourceInfo(int startLine, int start, int end, int construct){
    	this.startingLine=startLine;
    	this.start=start;
    	this.end=end;
    	this.constructType=construct;
    }
    /**
     * @return Returns the end position, relative to start of file
     */
    public int getEnd()
    {
        return end;
    }

    /**
     * @param end The end to set.
     */
    public void setEnd(int end)
    {
        this.end = end;
    }

    /**
     * @return Returns the start position, relative to start of file
     */
    public int getStart()
    {
        return start;
    }

    /**
     * @param start The start to set.
     */
    public void setStart(int start)
    {
        this.start = start;
    }

    /**
     * @return Returns the startingLine.
     */
    public int getStartingLine()
    {
        return startingLine;
    }

    /**
     * @param startingLine The startingLine to set.
     */
    public void setStartingLine(int startingLine)
    {
        this.startingLine = startingLine;
    }

    /**
     * @return Returns the constructType.
     */
    public int getConstructType()
    {
        return constructType;
    }

    /**
     * @param constructType The constructType to set.
     */
    public void setConstructType(int constructType)
    {
        this.constructType = constructType;
    }
    /**
     * A string representation of the object
     */
    public String toString(){
    	StringBuffer s = new StringBuffer();
    	s.append("SourceInfo line:").append(getStartingLine());
    	s.append(" startPos:").append(getStart());
    	s.append(" endPos:").append(getEnd());
    	s.append(" constructType:").append(getConstructType());
    	return s.toString();
    }
}
