/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.internal.core.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiHandler;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.AbslayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.CellType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ChartType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ChartgroupType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataType;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoboxType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.PaneType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RowType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SplitlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TextboxType;
import org.eclipse.ptp.rm.lml.internal.core.elements.UsagebarType;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.internal.core.model.jobs.JobStatusData;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ui.IMemento;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Class of the interface ILguiItem
 */
public class LguiItem implements ILguiItem {
	
	
	private static class LMLNamespacePrefixMapper extends NamespacePrefixMapper {
		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		        if( lmlNamespace.equals(namespaceUri) )
		            return "lml";
		        return suggestion;
		    }
		}


	/*
	 * Source of the XML-file from which the LguiType was generated.
	 */
	private String name;

	/*
	 * The generated LguiType
	 */
	private LguiType lgui;

	/**
	 * collects listeners, which listen for changes in model
	 */
	private final List<ILguiListener> listeners = new LinkedList<ILguiListener>();

	/**
	 * List of encapsulated classes, which handle parts of the lml-hierarchy
	 */
	private final Map<Class<? extends ILguiHandler>, ILguiHandler> lguiHandlers = Collections
			.synchronizedMap(new HashMap<Class<? extends ILguiHandler>, ILguiHandler>());

	/*
	 * List of Jobs
	 */
	private final Map<String, JobStatusData> jobList = Collections.synchronizedMap(new TreeMap<String, JobStatusData>());

	/*
	 * ObjectFactory
	 */
	private static ObjectFactory objectFactory = new ObjectFactory();

	/*
	 * String for the to saved layout.
	 */
	private String savedLayout = null;

	/*
	 * 
	 */
	
	private static String lmlNamespace="http://www.llview.de";


	public static final String LAYOUT = "layout";

	public static final String JOB = "job";

	/**************************************************************************************************************
	 * Constructors
	 **************************************************************************************************************/

	/**
	 * Empty Constructor.
	 */
	public LguiItem(String name) {
		this.name = name;
	}

	/**
	 * Constructor with LML-model as argument
	 * 
	 * @param lgui
	 *            LML-model
	 */
	public LguiItem(LguiType lgui) {
		this.lgui = lgui;
		createLguiHandlers();
	}

	/**
	 * Constructor with one argument, an URI. Within the constructor the method
	 * for parsing an XML-file into LguiItem is called.
	 * 
	 * @param xmlFile
	 *            the source of the XML file.
	 */
	public LguiItem(URI xmlFile) {
		name = xmlFile.getPath();
		try {
			lgui = parseLML(xmlFile);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		createLguiHandlers();
		setCid();
	}

	/**************************************************************************************************************
	 * Parsing methods
	 **************************************************************************************************************/

	/**
	 * Parsing an XML file. The method generates from an XML file an instance of
	 * LguiType.
	 * 
	 * @param xml
	 *            the URL source of the XML file
	 * @return the generated LguiType
	 * @throws MalformedURLException
	 * @throws JAXBException
	 */
	private static LguiType parseLML(URI xml) throws MalformedURLException {
		LguiType lml = null;
		try {
			Unmarshaller unmarshaller = LMLCorePlugin.getDefault().getUnmarshaller();

			JAXBElement<LguiType> doc = (JAXBElement<LguiType>) unmarshaller.unmarshal(xml.toURL());

			lml = doc.getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return lml; 

	}

	/**
	 * Parsing an XML file. The method generates from an XML file an instance of
	 * LguiType.
	 * 
	 * @param stream
	 *            the input stream of the XML file
	 * @return the generated LguiType
	 * @throws JAXBException
	 */
	private LguiType parseLML(InputStream stream) {
		LguiType old = lgui;
		LguiType lml = null;
		try {
			Unmarshaller unmarshaller = LMLCorePlugin.getDefault().getUnmarshaller();

			JAXBElement<LguiType> doc = (JAXBElement<LguiType>) unmarshaller.unmarshal(stream);

			lml = doc.getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
			lml = old;
		}

		return lml;
	}

	/**************************************************************************************************************
	 * Further methods for setting up
	 **************************************************************************************************************/

	/**
	 * The instance lgui is filled with a new data-model. This method creates
	 * all modules, which handle the data. These modules can then be accessed by
	 * corresponding getter-functions.
	 */
	private void createLguiHandlers() {
		lguiHandlers.put(OverviewAccess.class, new OverviewAccess(this, lgui));
		lguiHandlers.put(LayoutAccess.class, new LayoutAccess(this, lgui));
		lguiHandlers.put(OIDToObject.class, new OIDToObject(this, lgui));
		lguiHandlers.put(ObjectStatus.class, new ObjectStatus(this, lgui));
		lguiHandlers.put(OIDToInformation.class, new OIDToInformation(this, lgui));
		lguiHandlers.put(TableHandler.class, new TableHandler(this, lgui));
		lguiHandlers.put(NodedisplayAccess.class, new NodedisplayAccess(this, lgui));
	}

	private void setCid() {
		for (TableType table : getTableHandler().getTables()) {
			for (RowType row : table.getRow()) {
				int cid = 1;
				for (CellType cell : row.getCell()) {
					if (cell.getCid() == null) {
						cell.setCid(BigInteger.valueOf(cid));
					} else {
						cid = cell.getCid().intValue();
					}
					cid++;
				}
			}
		}
	}

	public void save(IMemento memento) {
		Marshaller marshaller = LMLCorePlugin.getDefault().getMarshaller();
		LguiType lgui = getLayoutFromModell();
		System.out.println(lgui == null);
		StringWriter writer = new StringWriter();
		try {
			marshaller.marshal(lgui, writer);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		savedLayout = writer.toString();
		memento.putString(LAYOUT, savedLayout);
		for (Entry<String, JobStatusData> entry : jobList.entrySet()) {
			memento.createChild(JOB, entry.getKey());
			entry.getValue().save(memento);
		}
	}

	public void restore(IMemento memento) {
		savedLayout = memento.getString(LAYOUT);
		// TODO in LayoutType umbauen
		IMemento[] mementoChilds = memento.getChildren(JOB);
		for (IMemento mementoChild : mementoChilds) {
			jobList.put(mementoChild.getID(), new JobStatusData(memento));
		}

	}

	/**************************************************************************************************************
	 * Getting LguiHandlers
	 **************************************************************************************************************/

	public OverviewAccess getOverviewAccess() {
		if (lguiHandlers.get(OverviewAccess.class) == null) {
			return null;
		}
		return (OverviewAccess) lguiHandlers.get(OverviewAccess.class);
	}

	public TableHandler getTableHandler() {
		if (lguiHandlers.get(TableHandler.class) == null) {
			return null;
		}
		return (TableHandler) lguiHandlers.get(TableHandler.class);
	}

	/**
	 * @return a class, which provides an index for fast access to objects
	 *         within the objects tag of LML. You can pass the id of the objects
	 *         to the returned object. It then returns the corresponding
	 *         objects.
	 */
	public OIDToObject getOIDToObject() {
		if (lguiHandlers.get(OIDToObject.class) == null) {
			return null;
		}
		return (OIDToObject) lguiHandlers.get(OIDToObject.class);
	}

	/**
	 * @return a object, which saves which object has to be highlighted. All
	 *         user interactions are saved globally for all components in this
	 *         object.
	 */
	public ObjectStatus getObjectStatus() {
		if (lguiHandlers.get(ObjectStatus.class) == null) {
			return null;
		}
		return (ObjectStatus) lguiHandlers.get(ObjectStatus.class);
	}

	/**
	 * @return object for getting infos for objects
	 */
	public OIDToInformation getOIDToInformation() {
		if (lguiHandlers.get(OIDToInformation.class) == null) {
			return null;
		}
		return (OIDToInformation) lguiHandlers.get(OIDToInformation.class);
	}

	/**
	 * @return NodedisplayAccess-instance for accessing layouts of nodedisplays
	 */
	public NodedisplayAccess getNodedisplayAccess() {
		if (lguiHandlers.get(NodedisplayAccess.class) == null) {
			return null;
		}
		return (NodedisplayAccess) lguiHandlers.get(NodedisplayAccess.class);
	}

	/**
	 * @return object to map component-ids to corresponding layout definitions
	 */
	public LayoutAccess getLayoutAccess() {
		if (lguiHandlers.get(LayoutAccess.class) == null) {
			return null;
		}
		return (LayoutAccess) lguiHandlers.get(LayoutAccess.class);
	}

	/**************************************************************************************************************
	 * Update
	 **************************************************************************************************************/

	/**
	 * Inform all listeners, that something changed in the data-model. Handlers
	 * should use this event to update their model-references. Otherwise
	 * inconsistent return-values will be the result.
	 */
	public void updateData() {
		updateData(lgui);
	}

	/**
	 * Call this method, if lml-model changed. The new model is passed to the
	 * listening handlers. All getter-functions accessing the handler will then
	 * return data, which is collected from this new model
	 * 
	 * @param lgui
	 *            new lml-data-model
	 */
	public void updateData(LguiType lgui) {

		this.lgui = lgui;

		LguiUpdatedEvent event = new LguiUpdatedEvent(this);
		for (ILguiListener l : listeners) {
			l.handleEvent(event);
		}
	}

	// public void updateXML() {
	// lgui = null;
	// try {
	// xmlFile = new URI(xmlFile.toString());
	// } catch (URISyntaxException e) {
	// e.printStackTrace();
	// }
	// try {
	// lgui = parseLML(xmlFile);
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// }
	//
	// ILguiUpdatedEvent e = new LguiUpdatedEvent(this);
	// for (ILguiListener listener : listeners) {
	// listener.handleEvent(e);
	// }
	// }

	public void update() {
		ILguiUpdatedEvent e = new LguiUpdatedEvent(this);
		for (ILguiListener listener : listeners) {
			listener.handleEvent(e);
		}
	}

	public void update(InputStream stream) {
		lgui = parseLML(stream);
		if (listeners.isEmpty()) {
			createLguiHandlers();
		}
		setCid();
		update();
	}
	
	public void getRequestXml(FileOutputStream output) {
		LguiType layoutLgui = getLayoutFromModell();
		Marshaller marshaller = LMLCorePlugin.getDefault().getMarshaller();
		try {
			marshaller.setProperty("jaxb.schemaLocation", lmlNamespace+" lgui.xsd");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			QName tagname = new QName(lmlNamespace, "lgui", "lml");

			JAXBElement<LguiType> rootElement = new JAXBElement<LguiType>(tagname, LguiType.class, layoutLgui);
			marshaller.marshal(rootElement, output);
			output.close();
		} catch (PropertyException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public boolean isEmpty() {
		return lgui.getObjectsAndRelationsAndInformation().isEmpty();
	}

	/**************************************************************************************************************
	 * Layout
	 **************************************************************************************************************/
	public void getCurrentLayout(OutputStream output) {
		LguiType layoutLgui = getLayoutFromModell();
		Marshaller marshaller = LMLCorePlugin.getDefault().getMarshaller();
		try {
			marshaller.setProperty("jaxb.schemaLocation", lmlNamespace+" lgui.xsd");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			QName tagname = new QName(lmlNamespace, "lgui", "lml");

			JAXBElement<LguiType> rootElement = new JAXBElement<LguiType>(tagname, LguiType.class, layoutLgui);
			marshaller.marshal(rootElement, output);
			output.close(); // Must close to flush stream
		} catch (PropertyException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private LguiType firstRequest() {
		ObjectFactory objectFactory = new ObjectFactory();

		LguiType layoutLgui = objectFactory.createLguiType();
		layoutLgui.setVersion("1");
		layoutLgui.setLayout(true);

		RequestType request = objectFactory.createRequestType();
		request.setGetDefaultData(true);
		layoutLgui.setRequest(request);

		return layoutLgui;
	}

	/**
	 * Remove all real data from modell return only layout-information and data,
	 * which is needed to make lml-model valid
	 * 
	 * @param model
	 *            lml-modell with data and layout-information
	 * @return
	 */
	private LguiType getLayoutFromModell() {
		if (lgui == null) {
			return firstRequest();
		}
		LguiType result = objectFactory.createLguiType();
		HashSet<String> neededComponents = new HashSet<String>();

		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			Object value = tag.getValue();

			// add normal global layouts
			if (value instanceof LayoutType) {
				result.getObjectsAndRelationsAndInformation().add(tag);

				if (value instanceof SplitlayoutType) {
					SplitlayoutType splitLayout = (SplitlayoutType) value;
					// Collect needed components from layout recursively
					if (splitLayout.getLeft() != null) {
						collectComponents(splitLayout.getLeft(), neededComponents);
						collectComponents(splitLayout.getRight(), neededComponents);
					}
				} else if (value instanceof AbslayoutType) {

					AbslayoutType absLayout = (AbslayoutType) value;
					// Just traverse comp-list for gid-attributes
					for (ComponentType comp : absLayout.getComp()) {
						neededComponents.add(comp.getGid());
					}
				}

			} else if (value instanceof ComponentlayoutType) {
				if (((ComponentlayoutType) value).isActive()) {
					result.getObjectsAndRelationsAndInformation().add(tag);

					ComponentlayoutType componentLayout = (ComponentlayoutType) value;
					neededComponents.add(componentLayout.getGid());
				}
			}

		}
		HashMap<String, GobjectType> idToGobject = new HashMap<String, GobjectType>();
		// Search needed components in data-tag to discover, which type the
		// needed components have
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			Object value = tag.getValue();
			// is it a graphical object?
			if (value instanceof GobjectType) {
				GobjectType gObject = (GobjectType) value;
				if (neededComponents.contains(gObject.getId())) {
					idToGobject.put(gObject.getId(), gObject);
				}
			}
		}
		// Add all gobjects in idtoGobject to the result, so that lml-modell is
		// valid
		for (GobjectType gObject : idToGobject.values()) {
			JAXBElement<GobjectType> min = minimizeGobjectType(gObject);
			result.getObjectsAndRelationsAndInformation().add(min);
		}
		// Set layout-attribute
		result.setLayout(true);

		return result;
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
	private static void collectComponents(PaneType pane, HashSet<String> neededComponents) {

		if (pane.getGid() != null) {
			neededComponents.add(pane.getGid());
		} else if (pane.getBottom() != null) {// top and bottom components?
			collectComponents(pane.getBottom(), neededComponents);
			collectComponents(pane.getTop(), neededComponents);
		} else {// Left and right
			collectComponents(pane.getLeft(), neededComponents);
			collectComponents(pane.getRight(), neededComponents);
		}
	}

	/**
	 * Take a graphical object and minimize the data so that this instance is
	 * valid against the LML-Schema but at the same time as small as possible.
	 * 
	 * @param gObject
	 * @return a copy of gobj with minimal size, only attributes in GobjectType
	 *         are copied and lower special elements which are needed to make
	 *         lml-model valid
	 */
	private static JAXBElement<GobjectType> minimizeGobjectType(GobjectType gObject) {

		String qName = "table";
		Class<GobjectType> classGobject = (Class<GobjectType>) gObject.getClass();

		GobjectType value = objectFactory.createGobjectType();

		if (gObject instanceof TableType) {
			TableType tableType = objectFactory.createTableType();
			value = tableType;
			qName = "table";
		} else if (gObject instanceof UsagebarType) {
			UsagebarType usagebarType = objectFactory.createUsagebarType();
			usagebarType.setCpucount(BigInteger.valueOf(0));
			value = usagebarType;
			qName = "usagebar";
		} else if (gObject instanceof TextboxType) {
			TextboxType textboxType = objectFactory.createTextboxType();
			textboxType.setText("");
			value = textboxType;
			qName = "text";
		} else if (gObject instanceof InfoboxType) {
			InfoboxType infoboxType = objectFactory.createInfoboxType();
			value = infoboxType;
			qName = "infobox";
		} else if (gObject instanceof Nodedisplay) {// Create minimal
													// nodedisplay
			Nodedisplay nodedisplay = objectFactory.createNodedisplay();
			value = nodedisplay;
			SchemeType scheme = objectFactory.createSchemeType();
			scheme.getEl1().add(objectFactory.createSchemeElement1());
			nodedisplay.setScheme(scheme);
			DataType data = objectFactory.createDataType();
			data.getEl1().add(objectFactory.createDataElement1());
			nodedisplay.setData(data);
			qName = "nodedisplay";
		} else if (gObject instanceof ChartType) {
			ChartType chartType = objectFactory.createChartType();
			value = chartType;
			qName = "chart";
		} else if (gObject instanceof ChartgroupType) {
			ChartgroupType chartgroupType = objectFactory.createChartgroupType();
			// Add lower chart-elements to the minimized chart-group
			ChartgroupType origin = (ChartgroupType) gObject;
			// Go through all charts minimize them and add them to ut
			for (ChartType chartType : origin.getChart()) {
				ChartType min = (ChartType) (minimizeGobjectType(chartType).getValue());
				chartgroupType.getChart().add(min);
			}
			value = chartgroupType;
			qName = "chartgroup";
		}

		value.setDescription(gObject.getDescription());
		value.setId(gObject.getId());
		value.setTitle(gObject.getTitle());

		JAXBElement<GobjectType> result = new JAXBElement<GobjectType>(new QName(qName), classGobject, value);
		return result;
	}

	/**************************************************************************************************************
	 * Job related methods
	 **************************************************************************************************************/

	public void addJob(IJobStatus jobStatus) {

	}

	public void updateJob(IJobStatus jobStatus) {

	}

	public void removeJob(IJobStatus jobStatus) {

	}

	/**************************************************************************************************************
	 * Further methods
	 **************************************************************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#toString
	 */
	@Override
	public String toString() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getVersion()
	 */
	public String getVersion() {
		return lgui.getVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#isLayout()
	 */
	public boolean isLayout() {
		return lgui.isLayout();
	}

	public LguiType getLguiType() {
		return lgui;
	}

	/**
	 * Add a lml-data-listener. It listens for data-changes.
	 * 
	 * @param listener
	 *            new listening instance
	 */
	public void addListener(ILguiListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a lml-data-listener.
	 * 
	 * @param listener
	 *            listening instance
	 */
	public void removeListener(ILguiListener listener) {
		listeners.remove(listener);
	}

}