/**
 * 
 */
package org.eclipse.ptp.etfw.feedback.core.obj;

import java.util.List;

/**
 * 
 * An IFeedbackItem has some standard info, like name, file, lineNo, etc
 * <br>
 * And some variable data, which will fill in the (variable number of) other columns.
 * @author beth
 *
 */
public interface IFeedbackItemOrig {
	/**
	 * number of columns (attributes?) besides name, filename, cols etc.
	 * Could be static 
	 * @return
	 */
	public int getNumColumns();
	/**
	 * Name of column.
	 * <br>This method could be static, not associated with instance data
	 * @param colNo
	 * @return
	 */
	public String getColumnName(int colNo);
	
	/**
	 * Value from instance for this column number
	 * @return
	 */
	public String getColumnValue(int colNo);
	
	///==========rest of info is not related to variable data
	
	public String getName();
	public List<IFeedbackItemOrig>  getChildren();
	public boolean hasChildren();
	/**
	 * these will be the column values
	 * @return
	 */
	public  String[] getAttributeNames();
	/**
	 * get the value for a given attribute.
	 * @param attrName
	 * @return
	 */
	public String getAttributeValue(String attrName);
	public String getAttributeName(int colNo);
	
	public String getFile();
	
	/**
	 * an IFeedbackItem can have either a lineno range (start/end)
	 * or a Character start/end.
	 * If useLineNo() returns true, use getStartingLineNo();
	 * If the lineNo range is more than on eline, getEndlingLineNo() will tell the last line.
	 * @return
	 */
	public boolean useLineNo();
	
	public int getLineNoStart();
	public int getLineNoEnd();
	
	/**
	 * Character locations are relative to the beginning of the file.
	 * If useLine() returns false, we assume that getStartingChar() returns a
	 * value >= 0;  and getEndingChar() returns a value >= getStartingChar() and not past the length of the file.
	 * 
	 * @return
	 */
	public int getCharStart();
	public int getCharEnd();
	/**
	 * unique id for this type of item e.g. to use as parent id
	 * @return
	 */
	public String getID();
	
	/** A longer description */
	public String getDescription();
	
	
	
}
