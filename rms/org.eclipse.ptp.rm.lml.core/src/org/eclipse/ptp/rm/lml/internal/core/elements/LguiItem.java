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
package org.eclipse.ptp.rm.lml.internal.core.elements;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.elements.ITableColumnLayout;
import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;

/**
 * Class of the interface ILguiItem
 * @author Claudia Knobloch
 */
public class LguiItem implements ILguiItem {
	
	/*
	 * Source of the XML-file from which the LguiType was generated.
	 */
	private URL xmlFile;
	
	/*
	 * The generated LguiType 
	 */
	private LguiType lgui;
	
	
	
	/*
	 * Constructor
	 */
	/**
	 * Constructor with no arguments
	 */
	public LguiItem(){
		
	}
	
	/**
	 * Constructor with one argument, an URL.
	 * Within the constructor the method for parsing an XML-file into LguiItem is called.
	 * @param xmlFile the source of the XML file.
	 */
	public LguiItem(URL xmlFile) {
		
		this.xmlFile = xmlFile;
		lgui = parseLML(xmlFile);
	}
	
	
	/*
	 * Methods for operating with the system
	 */
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getXMLFile()
	 */
	public URL getXmlFile() {
		return xmlFile;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#toString
	 */
	public String toString() {
		return getXmlFile().getPath();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getVersion()
	 */
	public String getVersion() {
		return lgui.getVersion();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#isLayout()
	 */
	public boolean isLayout() {
		return lgui.isLayout();
	}
	
	/**
	 * Getting a list with all elements of type ObjectsType from LguiType.
	 * @return list of elements(ObjectsType)
	 */
	private List<ObjectsType> getObjects() {
		List<ObjectsType> objects = new LinkedList<ObjectsType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof ObjectsType) {
				objects.add((ObjectsType) tag.getValue());
			}
		}
		return objects;
	}
	
	/**
	 * Getting a list of all elements of type InformationsType from LguiType.
	 * @return list of elements(InfomationsType)
	 */
	private List<InformationType> getInformations() {
		List<InformationType> informations = new LinkedList<InformationType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof InformationType) {
				informations.add((InformationType) tag.getValue());
			}
		}
		return informations;
	}
	
	/**
	 * Getting a list of all elements of type GobjectType from LguiType.
	 * @return list of elements(GobjectsType)
	 */
	private List<GobjectType> getGraphicalObjects() {
		List<GobjectType> objects = new LinkedList<GobjectType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof GobjectType) {
				objects.add((GobjectType) tag.getValue());
			}
		}
		return objects;
	}
	
	/**
	 * Getting a list of all elements of type TableType from LguiType.
	 * @return list of elements(TableType)
	 */
	private List<TableType> getTables() {
		List<TableType> tables = new LinkedList<TableType>();
		for (GobjectType tag : getGraphicalObjects()) {
			if (tag instanceof TableType) {
				tables.add((TableType) tag);
			}
		}
		return tables;
	}
	
	/**
	 * Getting a list of all elements of type Nodedisplay from LguiType.
	 * @return list of elements(Nodedisplay)
	 */
	private List<Nodedisplay> getNodedisplays() {
		List<Nodedisplay> tables = new LinkedList<Nodedisplay>();
		for (GobjectType tag : getGraphicalObjects()) {
			if (tag instanceof Nodedisplay) {
				tables.add((Nodedisplay) tag);
			}
		}
		return tables;
	}
	
	/**
	 * Getting a list of all elements of type ComponentlayoutType from LguiType.
	 * @return list of elements(ComponentlayoutType)
	 */
	private List<ComponentlayoutType> getComponentlayouts() {
		List<ComponentlayoutType> layouts = new LinkedList<ComponentlayoutType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof ComponentlayoutType) {
				layouts.add((ComponentlayoutType) tag.getValue());
			}
		}
		return layouts;
	}
	
	/**
	 * Getting a list of all elements of type TablelayoutType.
	 * @return list of elements(TablelayoutType)
	 */
	private List<TablelayoutType> getTablelayouts() {
		List<TablelayoutType> tablelayouts = new LinkedList<TablelayoutType>();
		for (ComponentlayoutType tag : getComponentlayouts()) {
			if (tag instanceof TablelayoutType) {
				tablelayouts.add((TablelayoutType) tag);
			}
		}
		return tablelayouts;
	}
	
	/**
	 * Getting a list of elements of type NodedisplaylayoutType.
	 * @return list of elements(NodedisplaylayoutType)
	 */
	private List<NodedisplaylayoutType> getNodedisplaylayouts() {
		List<NodedisplaylayoutType> tables = new LinkedList<NodedisplaylayoutType>();
		for (ComponentlayoutType tag : getComponentlayouts()) {
			if (tag instanceof NodedisplaylayoutType) {
				tables.add((NodedisplaylayoutType) tag);
			}
		}
		return tables;
	}
	
	/**
	 * Getting a list of all elements of type LayoutType.
	 * @return list of elements(LayoutType)
	 */
	private List<LayoutType> getLayouts() {
		List<LayoutType> layouts = new LinkedList<LayoutType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof LayoutType) {
				layouts.add((LayoutType) tag.getValue());
			}
		}
		return layouts;
	}
	
	/**
	 * Getting a list of all elements of type Splitlayout.
	 * @return list of elements(Splitlayout)
	 */
	private List<SplitlayoutType> getSplitlayouts() {
		List<SplitlayoutType> tables = new LinkedList<SplitlayoutType>();
		for (LayoutType tag : getLayouts()) {
			if (tag instanceof SplitlayoutType) {
				tables.add((SplitlayoutType) tag);
			}
		}
		return tables;
	}
	
	/**
	 * Getting an element(Table Type) which has an equal title to the argument tableType.
	 * @param tableType Title of the desired table
	 * @return Corresponding table to the desired table title
	 */
	private TableType getTable(String tableType) {
		for (TableType tag : getTables()) {
			if (tag.getTitle().equals(tableType)) {
				return tag;
			}
		}
		return null;
	}
	
	/**
	 * Getting the layout of a given table, the identifier of the layout is the shared ID of table and layout.
	 * @param tablelayoutID ID of the desired tablelayout
	 * @return Corresponding layout of a table
	 */
	private TablelayoutType getTablelayout(String tablelayoutID) {
		for (TablelayoutType tag : getTablelayouts()) {
			if (tag.getGid().equals(tablelayoutID)) {
				return tag;
			}
		}
		return null;
	}
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsNumber(String tableType)
	 */
	public int getTableColumnNumber(String tableType) {
		TableType table = getTable(tableType);
		if (table != null) {
			return table.getColumn().size();
		}
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsNames(String tableType)
	 */
	public String[] getTableColumnsNames(String tableType) {
		TableType table = getTable(tableType);
		String[] names = new String[table.getColumn().size()];
		for (int i = 0; i < names.length; i ++) {
			names[i] = table.getColumn().get(i).getName();
		}
		return names;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsWidth(String tableType, int widthTable)
	 */
	public int[] getTableColumnsWidth(String tableType, int widthTable) {
		TableType table = getTable(tableType);
		TablelayoutType tablelayout = getTablelayout(table.getId());
		int[] widthInt = new int[table.getColumn().size()];
		
		double sumWidthColumns = 0;
		
		for(int i = 0; i < table.getColumn().size(); i++) {
			sumWidthColumns += tablelayout.getColumn().get(i).getWidth();		
		}
		
		
		for (int i = 0; i < widthInt.length; i ++) {
			widthInt[i] = (int) (tablelayout.getColumn().get(i).getWidth() * widthTable / sumWidthColumns);
		}
		return widthInt;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTableColumnsStyle(String tableType)
	 */
	public String[] getTableColumnsStyle(String tableType) {
		TableType table = getTable(tableType);
		String[] style = new String[table.getColumn().size()];
		for (int i = 0; i < style.length; i++) {
			switch (table.getColumn().get(i).getSort().value().charAt(0)) {
			case 'a' :
			case 'd' :
				style[i] = "LEFT";
				break;
			case 'n' :
				style[i] = "RIGHT";
				break;
			default :
			}
		}
		return style;
	}
	
	/**
	 * Parsing an XML file.
	 * The method generates from an XML file an instance of LguiType.
	 * @param xml the URL source of the XML file
	 * @return sthe generated LguiType
	 * @throws JAXBException
	 */
	private static LguiType parseLML(URL xml){
		LguiType lml = null;
		try {
			Unmarshaller unmarshaller = LMLCorePlugin.getDefault().getUnmarshaller();
			
			JAXBElement<LguiType> doc = (JAXBElement<LguiType>)unmarshaller.unmarshal(xml);
			
			lml = doc.getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return lml;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getJobTableColumnLayout(String tableType, int widthTable)
	 */
	public ITableColumnLayout[] getJobTableColumnLayout(String tableType,
			int widthTable) {
		TableType table = getTable(tableType);
		TablelayoutType tablelayout = getTablelayout(table.getId());
		ITableColumnLayout[] jobTableColumnLayouts = new ITableColumnLayout[table.getColumn().size()];
		
		double sumWidthColumns = 0;
		
		for(int i = 0; i < table.getColumn().size(); i++) {
			sumWidthColumns += tablelayout.getColumn().get(i).getWidth();		
		}
		
		
		for (int i = 0; i < jobTableColumnLayouts.length; i ++) {
			String style;
			switch (table.getColumn().get(i).getSort().value().charAt(0)) {
			case 'a' :
			case 'd' :
				style = "LEFT";
				break;
			case 'n' :
				style = "RIGHT";
				break;
			default :
				style = "LEFT";
			}
			jobTableColumnLayouts[i] = new TableColumnLayout(
					table.getColumn().get(i).getName(),
					(int) (tablelayout.getColumn().get(i).getWidth() * widthTable / sumWidthColumns),
					style);
		}
		return jobTableColumnLayouts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getJobTableData(String tableType)
	 */
	public String[][] getJobTableData(String tableType) {
		TableType table = getTable(tableType);
		String[][] jobTableData = new String[table.getRow().size()][];
		for (int i = 0; i < jobTableData.length; i++) {
			String[] jobTableDataRow = new String[table.getRow().get(i).getCell().size()];
			for (int j = 0; j < jobTableDataRow.length; j++) {
				jobTableDataRow[j] = table.getRow().get(i).getCell().get(j).getValue();
			}
			jobTableData[i] = jobTableDataRow;
		}
		return jobTableData;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getTypeJobTableColumn(String tableType, int index)
	 */
	public String getTypeJobTableColumn(String tableType, int index) {
		return getTable(tableType).getColumn().get(index).getSort().value();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiItem#sort(String tableType, int sortDirectionComparator, int sortIndex, int sortDirection)
	 */
	public void sort(String tableType, int sortDirectionComparator, int sortIndex, int sortDirection) {
		RowType[] jobTableData = getTable(tableType).getRow().toArray(new RowType[getTable(tableType).getRow().size()]);
		Arrays.sort(jobTableData, new TableSorter(getTypeJobTableColumn(tableType, sortIndex), sortDirectionComparator, sortIndex, sortDirection));
		getTable(tableType).setRow(Arrays.asList(jobTableData));
	}


}