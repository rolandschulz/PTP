package org.eclipse.ptp.pldt.common;

import org.eclipse.ptp.pldt.common.util.SourceInfo;

/**
 * Artifacts contain information about something found in source code.
 * 
 * @author tibbitts
 *
 */
public interface IArtifact
{
    public int getColumn();
    public void setColumn(int col);
    public String getDescription();
    public String getFileName();
    public String getId();
    public int getLine();
    public void setLine(int lineNo);
    public String getPrimaryfileName();
    public String getShortName();
    public void setShortName(String name);
    public SourceInfo getSourceInfo();
    
    
    
    
    
    
    
    
}
