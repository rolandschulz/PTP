package org.eclipse.ptp.rm.lml.core.model;

/**
 * This interface presents the layout of one column of a table,
 * @author Claudia Knobloch
 */
public interface ITableColumnLayout {
	
	/**
	 * Getting the title of the column. 
	 * @return title of the column
	 */
	public String getTitle();
	
	/**
	 * Getting the width of the column.
	 * @return width of the table
	 */
	public int getWidth();
	
	/**
	 * Getting the style (LEFT or RIGHT) of the column.
	 * @return style of the column
	 */
	public String getStyle();
}
