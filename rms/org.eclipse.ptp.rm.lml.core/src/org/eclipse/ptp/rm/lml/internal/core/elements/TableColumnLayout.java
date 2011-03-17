package org.eclipse.ptp.rm.lml.internal.core.elements;

import org.eclipse.ptp.rm.lml.core.elements.ITableColumnLayout;

/**
 * Class of the interface ITableColumnLayout
 * @author Claudia Knobloch
 */
public class TableColumnLayout implements ITableColumnLayout {
	
	/*
	 * Title of the column
	 */
	private String title;
	
	/*
	 * Width of the column
	 */
	private int width;
	
	/*
	 * Style of the column 
	 */
	private String style;
	
	/**
	 * Constructor
	 * @param title title of the column
	 * @param size width of the column
	 * @param width style of the column
	 */
	public TableColumnLayout(String title, int size, String width) {
		this.title = title;
		this.width = size;
		this.style = width;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getTitle()
	 */
	public String getTitle() {
		return title;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getWidth()
	 */
	public int getWidth() {
		return width;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getStyle()
	 */
	public String getStyle() {
		return style;
	}

}
