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

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.AbslayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ChartType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ChartgroupType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ChartlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ColumnType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ColumnlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataType;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoboxType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoboxlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.PaneType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SplitlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TablelayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TextboxType;
import org.eclipse.ptp.rm.lml.internal.core.elements.UsagebarType;
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
		final LguiItem lgui = new LguiItem(data);
		final LayoutAccess la = new LayoutAccess(lgui, lgui.getLguiType());
		// Replace component-layouts
		for (final JAXBElement<?> el : layout.getObjectsAndRelationsAndInformation()) {
			if (el.getValue() instanceof ComponentlayoutType) {
				la.replaceComponentLayout((ComponentlayoutType) el.getValue());
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

		// Overwrite layouts with the same name
		for (final JAXBElement<?> el : layout.getObjectsAndRelationsAndInformation()) {
			if (el.getValue() instanceof LayoutType) {
				if (!replaceGlobalLayout((LayoutType) el.getValue(), data)) {
					// If not replaced, insert it
					data.getObjectsAndRelationsAndInformation().add(el);
				}
			}
		}

		return data;
	}

	/**
	 * Replace a global layout within the lml-model by a new one
	 * 
	 * @param newlayout
	 *            new layout, which replaces the old in model with the same id
	 * @param model
	 *            lgui-instance, which is changed
	 */
	@SuppressWarnings("unchecked")
	public static boolean replaceGlobalLayout(LayoutType newlayout, LguiType model) {

		final List<JAXBElement<?>> all = model.getObjectsAndRelationsAndInformation();

		// Go through all objects, search for layouttypes with newlayout.getId
		// as id and replace them with this layout
		for (int i = 0; i < all.size(); i++) {
			final JAXBElement<?> aobj = all.get(i);
			if (aobj.getValue() instanceof LayoutType) {
				final LayoutType old = (LayoutType) aobj.getValue();
				if (old.getId().equals(newlayout.getId())) {
					((JAXBElement<LayoutType>) aobj).setValue(newlayout);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Search for gid-attributes of a pane and put it into neededComponents
	 * Recursively search all graphical objects referenced by this pane
	 * 
	 * @param p
	 *            part of SplitLayout, which is scanned for gid-attributes
	 * @param neededComponents
	 *            resulting Hashset
	 */
	private static void collectComponents(PaneType p, HashSet<String> neededComponents) {

		if (p.getGid() != null) {
			neededComponents.add(p.getGid());
		} else {
			// top and bottom components?
			if (p.getBottom() != null) {
				collectComponents(p.getBottom(), neededComponents);
				collectComponents(p.getTop(), neededComponents);
			} else {// Left and right
				collectComponents(p.getLeft(), neededComponents);
				collectComponents(p.getRight(), neededComponents);
			}
		}

	}

	/**
	 * Take a graphical object and minimize the data so that this instance is
	 * valid against the LML-Schema but at the same time as small as possible.
	 * 
	 * @param gobj
	 * @return a copy of gobj with minimal size, only attributes in GobjectType
	 *         are copied and lower special elements which are needed to make
	 *         lml-model valid
	 */
	@SuppressWarnings("unchecked")
	private static JAXBElement<GobjectType> minimizeGobjectType(GobjectType gobj) {

		String qname = "table"; //$NON-NLS-1$
		final Class<GobjectType> c = (Class<GobjectType>) gobj.getClass();

		GobjectType value = objectFactory.createGobjectType();

		if (gobj instanceof TableType) {
			final TableType tt = objectFactory.createTableType();
			final TableType orig = (TableType) gobj;
			tt.setContenttype(orig.getContenttype());
			// copy all columns with pattern to table
			for (final ColumnType col : orig.getColumn()) {
				if (col.getPattern() != null) {
					tt.getColumn().add(col);
				}
			}

			value = tt;

			qname = "table"; //$NON-NLS-1$
		} else if (gobj instanceof UsagebarType) {
			final UsagebarType ut = objectFactory.createUsagebarType();

			ut.setCpucount(BigInteger.valueOf(0));

			value = ut;

			qname = "usagebar"; //$NON-NLS-1$
		} else if (gobj instanceof TextboxType) {
			final TextboxType ut = objectFactory.createTextboxType();

			ut.setText(""); //$NON-NLS-1$

			value = ut;

			qname = "text"; //$NON-NLS-1$
		} else if (gobj instanceof InfoboxType) {
			final InfoboxType ut = objectFactory.createInfoboxType();

			value = ut;

			qname = "infobox"; //$NON-NLS-1$
		} else if (gobj instanceof Nodedisplay) {// Create minimal nodedisplay
			final Nodedisplay ut = objectFactory.createNodedisplay();

			value = ut;
			final SchemeType scheme = objectFactory.createSchemeType();
			scheme.getEl1().add(objectFactory.createSchemeElement1());
			ut.setScheme(scheme);

			final DataType dat = objectFactory.createDataType();
			dat.getEl1().add(objectFactory.createDataElement1());
			ut.setData(dat);

			qname = "nodedisplay"; //$NON-NLS-1$
		} else if (gobj instanceof ChartType) {
			final ChartType ut = objectFactory.createChartType();

			value = ut;

			qname = "chart"; //$NON-NLS-1$
		} else if (gobj instanceof ChartgroupType) {
			final ChartgroupType ut = objectFactory.createChartgroupType();
			// Add lower chart-elements to the minimized chart-group
			final ChartgroupType orig = (ChartgroupType) gobj;
			// Go through all charts minimize them and add them to ut
			for (final ChartType chart : orig.getChart()) {
				final ChartType min = (ChartType) (minimizeGobjectType(chart).getValue());
				ut.getChart().add(min);
			}

			value = ut;

			qname = "chartgroup"; //$NON-NLS-1$
		}

		value.setDescription(gobj.getDescription());
		value.setId(gobj.getId());
		value.setTitle(gobj.getTitle());

		final JAXBElement<GobjectType> res = new JAXBElement<GobjectType>(new QName(qname), c, value);

		return res;
	}

	/**
	 * @param obj
	 *            LguiType-instance
	 * @param output
	 *            OutputStream to save xml-representation of obj in
	 * @throws JAXBException
	 */
	@SuppressWarnings("unused")
	private static void objToLML(LguiType obj, OutputStream output) throws JAXBException {

		final JAXBContext jc = JAXBContext.newInstance("lml"); //$NON-NLS-1$

		final Marshaller mar = jc.createMarshaller();

		mar.setProperty("jaxb.schemaLocation", "http://www.llview.de lgui.xsd"); //$NON-NLS-1$ //$NON-NLS-2$

		final QName tagname = new QName("http://www.llview.de", "lgui", "lml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		final JAXBElement<LguiType> rootel = new JAXBElement<LguiType>(tagname, LguiType.class, obj);

		mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		mar.marshal(rootel, output);
	}

	// DefaultLayouts
	private final UsagebarlayoutType defaultUsagebar;

	private final ChartlayoutType defaultChart;

	private final TablelayoutType defaultTable;

	private final InfoboxlayoutType defaultInfobox;

	/*
	 * create an objectfactory for all functions in this class
	 */
	private static ObjectFactory objectFactory = new ObjectFactory();

	/**
	 * @param lguiItem
	 *            LML-data-handler, which groups this handler and others to a
	 *            set of LMLHandler. This instance is needed to notify all
	 *            LMLHandler, if any data of the LguiType-instance was changed.
	 */
	public LayoutAccess(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);
		defaultUsagebar = objectFactory.createUsagebarlayoutType();
		defaultChart = objectFactory.createChartlayoutType();
		defaultTable = objectFactory.createTablelayoutType();
		defaultInfobox = objectFactory.createInfoboxlayoutType();

		this.lguiItem.addListener(new ILguiListener() {
			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLgui());
			}
		});
	}

	/**
	 * Add a new created layout to the model
	 * 
	 * @param layout
	 *            absolute or splitlayout
	 */
	public void addLayoutTag(LayoutType layout) {

		if (layout.getId() == null) {
			layout.setId(""); //$NON-NLS-1$
		}

		JAXBElement<? extends LayoutType> jaxbel = null;
		// Create jaxbelement corresponding to the class-type
		if (layout instanceof AbslayoutType) {

			final AbslayoutType abslayout = (AbslayoutType) layout;

			jaxbel = new JAXBElement<AbslayoutType>(new QName("abslayout"), AbslayoutType.class, abslayout); //$NON-NLS-1$

		} else if (layout instanceof SplitlayoutType) {

			final SplitlayoutType splitlayout = (SplitlayoutType) layout;

			jaxbel = new JAXBElement<SplitlayoutType>(new QName("splitlayout"), SplitlayoutType.class, splitlayout); //$NON-NLS-1$

		} else {
			return;
		}

		lgui.getObjectsAndRelationsAndInformation().add(jaxbel);

		lguiItem.notifyListeners();

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

		// Collect active components
		final List<GobjectType> gobjects = lguiItem.getOverviewAccess().getGraphicalObjects();

		final ArrayList<GobjectType> activeobjects = new ArrayList<GobjectType>();
		// Go through all graphical objects
		for (final GobjectType gobj : gobjects) {
			// Get layouts for this object, normally there is only one
			final List<ComponentlayoutType> layouts = getComponentLayoutByGID(gobj.getId());

			if (layouts.size() == 0) {// assume gobj to be active if there is no
										// componentlayout
				activeobjects.add(gobj);
			}

			// Search for a componentlayout which declares gobj to be active
			for (final ComponentlayoutType complayout : layouts) {
				if (complayout.isActive()) {
					activeobjects.add(gobj);
					break;
				}
			}
		}

		// Now activeobjects contains all active graphical objects, which have
		// to be arranged on the screen

		final AbslayoutType res = objectFactory.createAbslayoutType();

		res.setId("abs_default"); //$NON-NLS-1$

		// Try to create as many columns as rows
		int columns = (int) Math.round(Math.sqrt(activeobjects.size()));

		if (columns == 0) {
			columns = 1;
		}

		int rows = (int) Math.ceil((double) activeobjects.size() / columns);

		if (rows == 0) {
			rows = 1;
		}
		// Calculate width and height of graphical objects
		int index = 0;
		final int rectwidth = width / columns;
		final int rectheight = height / rows;

		for (final GobjectType gobj : activeobjects) {

			final ComponentType pos = objectFactory.createComponentType();
			pos.setGid(gobj.getId());
			// Positioning the component
			pos.setW(BigInteger.valueOf(rectwidth));
			pos.setH(BigInteger.valueOf(rectheight));

			pos.setX(BigInteger.valueOf((index % columns) * rectwidth));
			pos.setY(BigInteger.valueOf((index / columns) * rectheight));

			// Add this component position to the layout
			res.getComp().add(pos);

			index++;
		}

		return res;
	}

	public String[] getActiveNodedisplayLayoutGid() {
		final ArrayList<String> nodedisplayID = new ArrayList<String>();
		final List<NodedisplaylayoutType> nodedisplayLayouts = getNodedisplayLayouts();
		for (final NodedisplaylayoutType nodedisplayLayout : nodedisplayLayouts) {
			if (nodedisplayLayout.isActive()) {
				nodedisplayID.add(nodedisplayLayout.getGid());
			}
		}
		return nodedisplayID.toArray(new String[nodedisplayID.size()]);
	}

	public String[] getActiveTableLayoutsGid() {
		final ArrayList<String> tableLayoutsId = new ArrayList<String>();
		final List<TablelayoutType> tableLayouts = getTableLayouts();
		for (final TablelayoutType tableLayout : tableLayouts) {
			if (tableLayout.isActive()) {
				tableLayoutsId.add(tableLayout.getGid());
			}
		}
		return tableLayoutsId.toArray(new String[tableLayoutsId.size()]);
	}

	/**
	 * Simply returns the first layout found for a chart with the given id or a
	 * default-layout
	 * 
	 * @param chartID
	 * @return defaultlayout for a chart or first layout for chart with id
	 *         chartid given by lml-file
	 */
	public ChartlayoutType getChartLayout(String chartID) {
		final List<ChartlayoutType> chartLayouts = getChartLayouts();
		for (final ChartlayoutType chartLayout : chartLayouts) {
			if (chartLayout.getGid().equals(chartID)) {
				return chartLayout;
			}
		}
		return defaultChart;
	}

	public List<ChartlayoutType> getChartLayouts() {
		final List<ChartlayoutType> chartLayouts = new LinkedList<ChartlayoutType>();
		for (final ComponentlayoutType tag : getComponentLayouts()) {
			if (tag instanceof ChartlayoutType) {
				chartLayouts.add((ChartlayoutType) tag);
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
	public List<ComponentlayoutType> getComponentLayoutByGID(String gid) {

		final List<ComponentlayoutType> complayouts = getComponentLayouts();

		final ArrayList<ComponentlayoutType> res = new ArrayList<ComponentlayoutType>();

		for (final ComponentlayoutType alayout : complayouts) {

			if (alayout.getGid() != null && alayout.getGid().equals(gid)) {

				res.add(alayout);
			}
		}

		return res;
	}

	/**
	 * Getting a list of all elements of type ComponentlayoutType from LguiType.
	 * 
	 * @return list of elements(ComponentlayoutType)
	 */
	public List<ComponentlayoutType> getComponentLayouts() {
		final List<ComponentlayoutType> layouts = new LinkedList<ComponentlayoutType>();
		for (final JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof ComponentlayoutType) {
				layouts.add((ComponentlayoutType) tag.getValue());
			}
		}
		return layouts;
	}

	public TablelayoutType getDefaultTableLayout(String gid) {
		TablelayoutType tableLayout = getTableLayout(gid);
		if (tableLayout == null) {
			tableLayout = new TablelayoutType();
			tableLayout.setGid(gid);
			getTableLayouts().add(tableLayout);
		}
		if (tableLayout.getColumn().size() <= 0) {
			tableLayout.setId(gid + "_layout"); //$NON-NLS-1$
			tableLayout.setGid(gid);
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
		final List<ComponentlayoutType> objects = getComponentLayouts();
		final ArrayList<String> inactive = new ArrayList<String>();
		final Map<String, String> inactiveMap = new HashMap<String, String>();
		for (final ComponentlayoutType object : objects) {
			if (!object.isActive()) {
				if (object.getClass().getSimpleName().equals("TablelayoutType")) { //$NON-NLS-1$
					final TableType table = lguiItem.getTableHandler().getTable(object.getGid());
					if (table != null) {
						inactiveMap.put(table.getTitle(), object.getGid());
					}
				} else if (object.getClass().getSimpleName().equals("NodedisplaylayoutType")) { //$NON-NLS-1$
					inactiveMap
							.put(lguiItem.getNodedisplayAccess().getNodedisplayById(object.getGid()).getTitle(), object.getGid());
				}
				inactive.add(object.getGid());
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
	public InfoboxlayoutType getInfoboxLayout(String infoID) {
		final List<InfoboxlayoutType> infoboxLayouts = getInfoboxLayout();
		for (final InfoboxlayoutType infoboxLayout : infoboxLayouts) {
			if (infoboxLayout.getGid().equals(infoID)) {
				return infoboxLayout;
			}
		}
		return defaultInfobox;
	}

	public ColumnlayoutType[] getLayoutColumsToCids(BigInteger[] cids, String gid) {
		final ColumnlayoutType[] columns = new ColumnlayoutType[cids.length];
		for (int i = 0; i < cids.length; i++) {
			for (final ColumnlayoutType column : getTableLayout(gid).getColumn()) {
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

		// This is gid for all nodedisplaylayouts in requests => id for all
		// nodedisplays
		final String dummystring = "__dummy_nd__";//$NON-NLS-1$

		final LguiType res = objectFactory.createLguiType();

		final HashSet<String> neededComponents = new HashSet<String>();

		for (final JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {

			final Object value = tag.getValue();

			// add normal global layouts
			if (value instanceof LayoutType) {
				res.getObjectsAndRelationsAndInformation().add(tag);

				if (value instanceof SplitlayoutType) {
					final SplitlayoutType splitlayout = (SplitlayoutType) value;
					// Collect needed components from layout recursively
					if (splitlayout.getLeft() != null) {
						collectComponents(splitlayout.getLeft(), neededComponents);
						collectComponents(splitlayout.getRight(), neededComponents);
					}
				} else if (value instanceof AbslayoutType) {

					final AbslayoutType abslayout = (AbslayoutType) value;
					// Just traverse comp-list for gid-attributes
					for (final ComponentType comp : abslayout.getComp()) {
						neededComponents.add(comp.getGid());
					}

				}

			} else if (value instanceof ComponentlayoutType) {
				if (((ComponentlayoutType) value).isActive()) {
					res.getObjectsAndRelationsAndInformation().add(tag);

					final ComponentlayoutType complayout = (ComponentlayoutType) value;
					neededComponents.add(complayout.getGid());

					// Workaround for nodedisplay
					if (value instanceof NodedisplaylayoutType) {
						final NodedisplaylayoutType nlayout = (NodedisplaylayoutType) value;
						nlayout.setGid(dummystring);
					}

				}
			}

		}

		final HashMap<String, GobjectType> idtoGobject = new HashMap<String, GobjectType>();
		// Search needed components in data-tag to discover, which type the
		// needed components have
		for (final JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {

			final Object value = tag.getValue();
			// is it a graphical object?
			if (value instanceof GobjectType) {
				final GobjectType gobj = (GobjectType) value;
				if (neededComponents.contains(gobj.getId())) {
					idtoGobject.put(gobj.getId(), gobj);
				}
			}
		}

		// Add all gobjects in idtoGobject to the result, so that lml-modell is
		// valid
		for (final GobjectType gobj : idtoGobject.values()) {
			final JAXBElement<GobjectType> min = minimizeGobjectType(gobj);

			// Workaround for nodedisplay
			final GobjectType newgobj = min.getValue();
			if (newgobj instanceof Nodedisplay) {
				((Nodedisplay) newgobj).setId(dummystring);
			}

			res.getObjectsAndRelationsAndInformation().add(min);
		}

		// Set layout-attribute
		res.setLayout(true);

		return res;
	}

	/**
	 * Getting a list of elements of type NodedisplaylayoutType.
	 * 
	 * @return list of elements(NodedisplaylayoutType)
	 */
	public List<NodedisplaylayoutType> getNodedisplayLayouts() {
		final List<NodedisplaylayoutType> nodedisplayLayouts = new LinkedList<NodedisplaylayoutType>();
		for (final ComponentlayoutType tag : getComponentLayouts()) {
			if (tag instanceof NodedisplaylayoutType) {
				nodedisplayLayouts.add((NodedisplaylayoutType) tag);
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
	public TablelayoutType getTableLayout(String tablelayoutID) {
		for (final TablelayoutType tag : getTableLayouts()) {
			if (tag.getGid().equals(tablelayoutID)) {
				return tag;
			}
		}
		return defaultTable;
	}

	/**
	 * Getting a list of all elements of type TablelayoutType.
	 * 
	 * @return list of elements(TablelayoutType)
	 */
	public List<TablelayoutType> getTableLayouts() {
		final List<TablelayoutType> tableLayouts = new LinkedList<TablelayoutType>();
		for (final ComponentlayoutType tag : getComponentLayouts()) {
			if (tag instanceof TablelayoutType) {
				tableLayouts.add((TablelayoutType) tag);
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
	public InfoboxlayoutType getTextboxLayout(String textID) {
		return getInfoboxLayout(textID);
	}

	/**
	 * Simply returns the first layout found for a usagebar with the given id or
	 * a default-layout
	 * 
	 * @param usagebarID
	 * @return defaultlayout for a usagebar or first layout for usagebar with id
	 *         usagebarid given by lml-file
	 */
	public UsagebarlayoutType getUsagebarLayout(String usagebarID) {
		final List<UsagebarlayoutType> usagebarLayouts = getUsagebarLayouts();
		// Over all objects in lml-file
		for (final UsagebarlayoutType usagebarLayout : usagebarLayouts) {
			if (usagebarLayout.getGid().equals(usagebarID)) {
				return usagebarLayout;
			}
		}
		return defaultUsagebar;
	}

	public List<UsagebarlayoutType> getUsagebarLayouts() {
		final List<UsagebarlayoutType> usagebarLayouts = new LinkedList<UsagebarlayoutType>();
		for (final ComponentlayoutType tag : getComponentLayouts()) {
			if (tag instanceof UsagebarlayoutType) {
				usagebarLayouts.add((UsagebarlayoutType) tag);
			}
		}
		return usagebarLayouts;
	}

	/**
	 * Replace all componentlayouts for a graphical object with given gid
	 * through newlayout.getGid() with newlayout
	 * 
	 * @param newLayout
	 *            new layout, which is placed into the positions of old layouts
	 */
	@SuppressWarnings("unchecked")
	public void replaceComponentLayout(ComponentlayoutType newlayout) {
		if (newlayout == null) {
			return;
		}
		final String gid = newlayout.getGid();

		final List<JAXBElement<?>> allobjects = lgui.getObjectsAndRelationsAndInformation();

		boolean replaced = false;

		// Over all objects in lml-file
		for (int i = 0; i < allobjects.size(); i++) {
			final JAXBElement<?> aobj = allobjects.get(i);

			// Over all Componentlayouts
			if (aobj.getValue() instanceof ComponentlayoutType) {

				final ComponentlayoutType alayout = (ComponentlayoutType) aobj.getValue();

				if (alayout.getGid() != null && alayout.getGid().equals(gid)) {

					if (!replaced) {

						((JAXBElement<ComponentlayoutType>) aobj).setValue(newlayout);
						lguiItem.notifyListeners();
						replaced = true;
					} else {// Delete this object
						allobjects.remove(aobj);
						// One step back
						i--;
					}
				}

			}
		}

		if (!replaced) {// Insert new layout, if there was nothing to replace
			// Takes any componentlayout
			JAXBElement<?> newel = null;

			// Differ between several layouts, create different JAXBElements
			if (newlayout instanceof TablelayoutType) {
				newel = new JAXBElement<TablelayoutType>(new QName("tablelayout"), TablelayoutType.class, //$NON-NLS-1$
						(TablelayoutType) newlayout);
			} else if (newlayout instanceof NodedisplaylayoutType) {
				newel = new JAXBElement<NodedisplaylayoutType>(new QName("nodedisplaylayout"), NodedisplaylayoutType.class, //$NON-NLS-1$
						(NodedisplaylayoutType) newlayout);
			}

			if (newel != null) {
				lgui.getObjectsAndRelationsAndInformation().add(newel);
				lguiItem.notifyListeners();
			}
		}

	}

	/**
	 * Setting Changes in the Layout.
	 */
	public void setChangesLayoutColumn() {

	}

	public String setComponentActive(String gid, boolean active) {
		String type = null;
		final ComponentlayoutType component = getComponent(gid);
		if (component != null) {
			if (component instanceof TablelayoutType) {
				type = "table"; //$NON-NLS-1$
			} else if (component instanceof NodedisplaylayoutType) {
				type = "nodedisplay"; //$NON-NLS-1$
			}
			component.setActive(active);
		}
		return type;
	}

	private ComponentlayoutType getComponent(String gid) {
		final List<ComponentlayoutType> objects = getComponentLayouts();
		for (final ComponentlayoutType object : objects) {
			if (object.getGid().equals(gid)) {
				return object;
			}
		}
		return null;
	}

	private List<InfoboxlayoutType> getInfoboxLayout() {
		final List<InfoboxlayoutType> infoboxLayouts = new LinkedList<InfoboxlayoutType>();
		for (final ComponentlayoutType tag : getComponentLayouts()) {
			if (tag instanceof InfoboxlayoutType) {
				infoboxLayouts.add((InfoboxlayoutType) tag);
			}
		}
		return infoboxLayouts;
	}

}
