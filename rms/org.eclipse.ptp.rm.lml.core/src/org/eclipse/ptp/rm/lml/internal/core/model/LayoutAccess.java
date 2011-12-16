/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.lml.core.ILMLCoreConstants;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.util.JAXBUtil;
import org.eclipse.ptp.rm.lml.internal.core.elements.AbslayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ChartlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ColumnlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoboxlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TablelayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.UsagebarlayoutType;

/**
 * This class provides access to component-layout-definitions. Returns
 * layout-objects for standard-lml-objects
 * 
 * Moreover with this class one is able to manipulate layout definitions
 * LML-Models can be transformed into layout-only definitions There are
 * functions, which allow to merge layouts to one lml-model
 */
public class LayoutAccess extends LguiHandler {
	/*
	 * create an objectfactory for all functions in this class
	 */
	private static ObjectFactory objectFactory = new ObjectFactory();

	private static final String LAYOUT_ENDING = "_layout";//$NON-NLS-1$

	private static String DEAFULT_ABS = "abs_default";//$NON-NLS-1$

	private static JAXBUtil jaxbUtil = JAXBUtil.getInstance();

	/**
	 * This method merges the layout information given by the "layout"-instance
	 * with the layout, which is included in "data". Component-layouts in "data"
	 * are replaced with corresponding layouts in layout. Global layouts of
	 * "layout" replace global layouts in data with the same name. New layouts
	 * in "layout" are added. Remark: "data" is modified by this method. A
	 * reference to data is returned.
	 * 
	 * @param data
	 *            model of lml-data, contingently with layout-information, this
	 *            object will be modified and returned
	 * @param layout
	 *            more important layout-data, overwrite componentlayouts of
	 *            data-model, but add additional abs/-splitlayouts
	 * @return merged lml-model
	 */
	public static LguiType mergeLayouts(LguiType data, LguiType layout) {

		if (data == null || layout == null) {
			return data;
		}
		final LguiItem lguiData = new LguiItem(data);
		// Replace component-layouts
		for (final Object object : jaxbUtil.getObjects(layout)) {
			if (object instanceof ComponentlayoutType) {
				jaxbUtil.replaceComponentLayout(data, lguiData, (ComponentlayoutType) object);
			}
		}

		/**
		 * really merge layouts, do not overwrite //Collect existing layout-ids
		 * HashSet<String> layoutids=new HashSet<String>();
		 * 
		 * for(JAXBElement<?> el:data.getObjectsAndRelationsAndInformation()){
		 * if(el.getValue() instanceof LayoutType){ LayoutType lay=(LayoutType)
		 * el.getValue(); layoutids.add(lay.getId()); } }
		 * 
		 * //Generate new ids if layout-ids already exist in data, then add new
		 * layout to data for(JAXBElement<?>
		 * el:layout.getObjectsAndRelationsAndInformation()){ if(el.getValue()
		 * instanceof LayoutType){ LayoutType lay=(LayoutType) el.getValue();
		 * 
		 * String newid=lay.getId(); while(layoutids.contains( newid )){
		 * newid=newid+"'"; }
		 * 
		 * lay.setId(newid); layoutids.add(newid); //Add this layout with new id
		 * to data-instance data.getObjectsAndRelationsAndInformation().add(el);
		 * } }
		 **/

		JAXBUtil.replaceGlobalLayout(layout, data);
		return data;
	}

	/**
	 * @param lguiItem
	 *            LML-data-handler, which groups this handler and others to a
	 *            set of LMLHandler. This instance is needed to notify all
	 *            LMLHandler, if any data of the LguiType-instance was changed.
	 */
	public LayoutAccess(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);

		this.lguiItem.addListener(new ILguiListener() {
			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLgui());
			}
		});
	}

	/**
	 * Generates an absolute layout without needing a layout tag. Active
	 * components are placed in a grid on the screen. Use this function, if no
	 * layout was specified by the lml-file.
	 * 
	 * @param width
	 *            width in pixels of the area, on which this layout is shown
	 * @param height
	 *            height in pixels of the area, on which this layout is shown
	 * @return default absolute layout with all active components in it
	 */
	public AbslayoutType generateDefaultAbsoluteLayout(int width, int height) {

		final ArrayList<GobjectType> activeObjects = new ArrayList<GobjectType>();
		// Go through all graphical objects
		for (final GobjectType gobject : lguiItem.getOverviewAccess()
				.getGraphicalObjects()) {
			// Get layouts for this object, normally there is only one
			final List<ComponentlayoutType> layouts = getComponentLayoutByGid(gobject
					.getId());

			if (layouts.size() == 0) {// assume gobject to be active if there is
										// no componentlayout
				activeObjects.add(gobject);
			}

			// Search for a componentlayout which declares gobject to be active
			for (final ComponentlayoutType componentLayout : layouts) {
				if (componentLayout.isActive()) {
					activeObjects.add(gobject);
					break;
				}
			}
		}

		// Now activeObjects contains all active graphical objects, which have
		// to be arranged on the screen

		final AbslayoutType result = objectFactory.createAbslayoutType();

		result.setId(DEAFULT_ABS);

		// Try to create as many columns as rows
		int columns = (int) Math.round(Math.sqrt(activeObjects.size()));

		if (columns == 0) {
			columns = 1;
		}

		int rows = (int) Math.ceil((double) activeObjects.size() / columns);

		if (rows == 0) {
			rows = 1;
		}
		// Calculate width and height of graphical objects
		int index = 0;
		final int rectWidth = width / columns;
		final int rectHeight = height / rows;

		for (final GobjectType gobject : activeObjects) {

			final ComponentType component = objectFactory.createComponentType();
			component.setGid(gobject.getId());
			// Positioning the component
			component.setW(BigInteger.valueOf(rectWidth));
			component.setH(BigInteger.valueOf(rectHeight));

			component.setX(BigInteger.valueOf((index % columns) * rectWidth));
			component.setY(BigInteger.valueOf((index / columns) * rectHeight));

			// Add this component position to the layout
			result.getComp().add(component);

			index++;
		}

		return result;
	}

	/**
	 * Simply returns the first layout found for a chart with the given id or a
	 * default-layout
	 * 
	 * @param chartId
	 * @return defaultlayout for a chart or first layout for chart with id
	 *         chartid given by lml-file
	 */
	public ChartlayoutType getChartLayout(String chartId) {
		for (final ChartlayoutType chartLayout : getChartLayouts()) {
			if (chartLayout.getGid().equals(chartId)) {
				return chartLayout;
			}
		}
		return objectFactory.createChartlayoutType();
	}

	public List<ChartlayoutType> getChartLayouts() {
		final List<ChartlayoutType> chartLayouts = new LinkedList<ChartlayoutType>();
		for (final ComponentlayoutType layout : getComponentLayouts()) {
			if (layout instanceof ChartlayoutType) {
				chartLayouts.add((ChartlayoutType) layout);
			}
		}
		return chartLayouts;
	}

	/**
	 * Search in component-layouts for layout for the graphical object with id
	 * gid.
	 * 
	 * @param gid
	 *            id of corresponding graphical object, for which layouts are
	 *            searched
	 * @return list of componentlayouts corresponding to the graphical object id
	 *         gid
	 */
	public List<ComponentlayoutType> getComponentLayoutByGid(String gid) {
		final ArrayList<ComponentlayoutType> result = new ArrayList<ComponentlayoutType>();
		for (final ComponentlayoutType layout : getComponentLayouts()) {
			if (layout.getGid() != null && layout.getGid().equals(gid)) {
				result.add(layout);
			}
		}
		return result;
	}

	/**
	 * Getting a list of all elements of type ComponentlayoutType from LguiType.
	 * 
	 * @return list of elements(ComponentlayoutType)
	 */
	public List<ComponentlayoutType> getComponentLayouts() {
		final List<ComponentlayoutType> layouts = new LinkedList<ComponentlayoutType>();
		for (final Object object : jaxbUtil.getObjects(lgui)) {
			if (object instanceof ComponentlayoutType) {
				layouts.add((ComponentlayoutType) object);
			}
		}
		return layouts;
	}

	public TablelayoutType getDefaultTableLayoutFromTable(String gid) {
		TablelayoutType tableLayout = getTableLayout(gid);
		if (tableLayout == null) {
			tableLayout = new TablelayoutType();
			tableLayout.setGid(gid);
			getTableLayouts().add(tableLayout);
		}
		if (tableLayout.getColumn().size() <= 0) {
			tableLayout.setId(gid + LAYOUT_ENDING);
			final TableType table = lguiItem.getTableHandler().getTable(gid);
			if (table != null) {
				for (int i = 0; i < table.getColumn().size(); i++) {
					final ColumnlayoutType column = new ColumnlayoutType();
					column.setCid(BigInteger.valueOf(i + 1));
					column.setPos(BigInteger.valueOf(i));
					column.setWidth(Double.valueOf(1));
					column.setActive(true);
					column.setKey(table.getColumn().get(i).getName());
					tableLayout.getColumn().add(column);
				}
			}
		}
		return tableLayout;
	}

	public Map<String, String> getInactiveComponents() {
		final Map<String, String> inactiveMap = new HashMap<String, String>();
		for (final ComponentlayoutType object : getComponentLayouts()) {
			if (!object.isActive()) {
				if (object instanceof TablelayoutType) {
					final TableType table = lguiItem.getTableHandler()
							.getTable(object.getGid());
					if (table != null) {
						inactiveMap.put(table.getTitle(), object.getGid());
					}
				} else if (object instanceof NodedisplaylayoutType) {
					inactiveMap.put(lguiItem.getNodedisplayAccess()
							.getNodedisplayById(object.getGid()).getTitle(),
							object.getGid());
				}
			}
		}
		return inactiveMap;
	}

	/**
	 * Simply returns the first layout found for a infobox with the given id or
	 * a default-layout
	 * 
	 * @param infoID
	 * @return defaultlayout for a table or first layout for table with id
	 *         tableid given by lml-file
	 */
	public InfoboxlayoutType getInfoboxLayout(String infoId) {
		for (final InfoboxlayoutType infoboxLayout : getInfoboxLayout()) {
			if (infoboxLayout.getGid().equals(infoId)) {
				return infoboxLayout;
			}
		}
		return objectFactory.createInfoboxlayoutType();
	}

	public ColumnlayoutType[] getLayoutColumsToCids(BigInteger[] cids,
			String gid) {
		final ColumnlayoutType[] columns = new ColumnlayoutType[cids.length];
		for (int i = 0; i < cids.length; i++) {
			for (final ColumnlayoutType column : getTableLayout(gid)
					.getColumn()) {
				if (column.getCid().equals(cids[i])) {
					columns[i] = column;
					break;
				}
			}
		}

		return columns;
	}

	/**
	 * Remove all real data from model return only layout-information and data,
	 * which is needed to make lml-model valid
	 * 
	 * @param model
	 *            lml-model with data and layout-information
	 * @return
	 */
	public LguiType getLayoutFromModel() {
		final LguiType result = objectFactory.createLguiType();

		final HashSet<String> components = new HashSet<String>();

		jaxbUtil.getLayoutComponents(result, lgui, components);

		final List<GobjectType> gobjectList = new LinkedList<GobjectType>();
		for (final Object object : jaxbUtil.getObjects(lgui)) {
			// is it a graphical object?
			if (object instanceof GobjectType) {
				if (components.contains(((GobjectType) object).getId())) {
					gobjectList.add((GobjectType) object);
				}
			}
		}

		// Add all gobjects in idtoGobject to the result, so that lml-model is
		// valid
		for (final GobjectType gobject : gobjectList) {
			result.getObjectsAndRelationsAndInformation().add(
					JAXBUtil.minimizeGobjectType(gobject, objectFactory));
		}

		// Set layout-attribute
		result.setLayout(true);

		return result;
	}

	/**
	 * Getting a list of elements of type NodedisplaylayoutType.
	 * 
	 * @return list of elements(NodedisplaylayoutType)
	 */
	public List<NodedisplaylayoutType> getNodedisplayLayouts() {
		final List<NodedisplaylayoutType> nodedisplayLayouts = new LinkedList<NodedisplaylayoutType>();
		for (final ComponentlayoutType layout : getComponentLayouts()) {
			if (layout instanceof NodedisplaylayoutType) {
				nodedisplayLayouts.add((NodedisplaylayoutType) layout);
			}
		}
		return nodedisplayLayouts;

	}

	/**
	 * Getting the layout of a given table, the identifier of the layout is the
	 * shared ID of table and layout.
	 * 
	 * @param tablelayoutID
	 *            ID of the desired tablelayout
	 * @return Corresponding layout of a table
	 */
	public TablelayoutType getTableLayout(String tablelayoutId) {
		for (final TablelayoutType layout : getTableLayouts()) {
			if (layout.getGid().equals(tablelayoutId)) {
				return layout;
			}
		}
		return objectFactory.createTablelayoutType();
	}

	/**
	 * Getting a list of all elements of type TablelayoutType.
	 * 
	 * @return list of elements(TablelayoutType)
	 */
	public List<TablelayoutType> getTableLayouts() {
		final List<TablelayoutType> tableLayouts = new LinkedList<TablelayoutType>();
		for (final ComponentlayoutType layout : getComponentLayouts()) {
			if (layout instanceof TablelayoutType) {
				tableLayouts.add((TablelayoutType) layout);
			}
		}
		return tableLayouts;
	}

	/**
	 * This function is only for easier understanding this class Textboxlayouts
	 * are identical to infoboxlayouts, so you could call
	 * getInfoboxLayout(textid) and would get the same result.
	 * 
	 * @param textID
	 *            id of a textbox
	 * @return layout for a textbox with an info-tag in it
	 */
	public InfoboxlayoutType getTextboxLayout(String textId) {
		return getInfoboxLayout(textId);
	}

	/**
	 * Simply returns the first layout found for a usagebar with the given id or
	 * a default-layout
	 * 
	 * @param usagebarId
	 * @return defaultlayout for a usagebar or first layout for usagebar with id
	 *         usagebarid given by lml-file
	 */
	public UsagebarlayoutType getUsagebarLayout(String usagebarId) {
		// Over all objects in lml-file
		for (final UsagebarlayoutType usagebarLayout : getUsagebarLayouts()) {
			if (usagebarLayout.getGid().equals(usagebarId)) {
				return usagebarLayout;
			}
		}
		return objectFactory.createUsagebarlayoutType();
	}

	public List<UsagebarlayoutType> getUsagebarLayouts() {
		final List<UsagebarlayoutType> usagebarLayouts = new LinkedList<UsagebarlayoutType>();
		for (final ComponentlayoutType layout : getComponentLayouts()) {
			if (layout instanceof UsagebarlayoutType) {
				usagebarLayouts.add((UsagebarlayoutType) layout);
			}
		}
		return usagebarLayouts;
	}

	public String setComponentActive(String gid, boolean active) {
		String type = null;
		final ComponentlayoutType component = getComponent(gid);
		if (component != null) {
			if (component instanceof TablelayoutType) {
				type = ILMLCoreConstants.TABLE_ELEMENT;
			} else if (component instanceof NodedisplaylayoutType) {
				type = ILMLCoreConstants.NODEDISPLAY_ELEMENT;
			}
			component.setActive(active);
		}
		return type;
	}

	private ComponentlayoutType getComponent(String gid) {
		for (final ComponentlayoutType object : getComponentLayouts()) {
			if (object.getGid().equals(gid)) {
				return object;
			}
		}
		return null;
	}

	private List<InfoboxlayoutType> getInfoboxLayout() {
		final List<InfoboxlayoutType> infoboxLayouts = new LinkedList<InfoboxlayoutType>();
		for (final ComponentlayoutType layout : getComponentLayouts()) {
			if (layout instanceof InfoboxlayoutType) {
				infoboxLayouts.add((InfoboxlayoutType) layout);
			}
		}
		return infoboxLayouts;
	}

}
