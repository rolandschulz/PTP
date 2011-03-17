/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */
package org.eclipse.ptp.rm.lml.core.elements;

import java.net.URL;

/**
 * Interface to manage the handling of an LguiType. It helps to work with LguiType without knowing the exact build of LguiType. 
 * @author Claudia Knobloch
 */
public interface ILguiItem  {
	
	/**
	 * Getting the source of the XML file from whcih the corresponding LguiType has been generated. 
	 * @return the source of the XML file
	 */
	public URL getXmlFile();
	
	/**
	 * Getting a string representing the ILguiItem.
	 * @return string 
	 */
	public String toString();
	
	/**
	 * Getting the version of the LguiType:
	 * @return version of the LguiType
	 */
	public String getVersion();
	
	/**
	 * Checking if any layout is present.
	 * @return 
	 */
	public boolean isLayout();
	
	/**
	 * Getting the number of columns of a certain table. 
	 * @param tableType title of the desired table
	 * @return number of columns of the desired table
	 */
	public int getTableColumnNumber(String tableType);
	
	/**
	 * Getting the titles of every column in the table.
	 * @param tableType title of a desired table
	 * @return the titles of every column in the table
	 */
	public String[] getTableColumnsNames(String tableType);
	
	/**
	 * Getting the widths of every column in a certain table.
	 * The method calculates the widths of the columns.
	 * @param tableType title of the desired table
	 * @param widthTable width of the whole table
	 * @return the widths of every column in the table
	 */
	public int[] getTableColumnsWidth(String tableType, int widthTable);
	
	/**
	 * Getting the styles (LEFT, RIGHT) of every column in a certain table.
	 * @param tableType title of the desired table
	 * @return the styles of every column in the table
	 */
	public String[] getTableColumnsStyle(String tableType);
	
	/**
	 * Getting the layouts (title, width and style) of every column in a certain table.
	 * @param tableType title of the desired table
	 * @param widthTable width of the whole table
	 * @return the layouts of every column in the table 
	 */
	public ITableColumnLayout[] getJobTableColumnLayout(String tableType, int widthTable); 
	
	/**
	 * Getting the data of a certain table.
	 * @param tableType title of the desired table
	 * @return the data of a table
	 */
	public String[][] getJobTableData(String tableType);
	
	/**
	 * Getting the style of a certain column of a certain table.
	 * @param tableType title of the desired table
	 * @param index index of the requested column
	 * @return the style of the requested column
	 */
	public String getTypeJobTableColumn(String tableType, int index);
	
	/**
	 * Sorting the data of a certain table.
	 * This functions sorts the data of a certain table. As arguments the function gets the title of the desired table, the value of the sorting direction up, the index of the to sorting column and the requested sorting direction.
	 * The second argument is very important, so that this class is independent from a fixed UI-API. This argument serves as an comparator to the requested sorting direction.
	 * @param tableType title of the desired table
	 * @param sortDirectionComparator value for sorting direction up
	 * @param sortIndex index of the to sorting column
	 * @param sortDirection requested sorting direction(up, down)
	 */
	public void sort(String tableType, int sortDirectionComparator, int sortIndex, int sortDirection);
}
