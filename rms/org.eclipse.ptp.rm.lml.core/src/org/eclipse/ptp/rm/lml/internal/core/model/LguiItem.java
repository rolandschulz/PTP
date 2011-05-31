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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiHandler;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.CellType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ColumnType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfodataType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RowType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.internal.core.model.jobs.JobStatusData;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Class of the interface ILguiItem
 */
public class LguiItem implements ILguiItem {

	private static class LMLNamespacePrefixMapper extends NamespacePrefixMapper {
		@Override
		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
			if (lmlNamespace.equals(namespaceUri)) {
				return "lml";
			}
			return suggestion;
		}
	}

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
			final Unmarshaller unmarshaller = LMLCorePlugin.getDefault().getUnmarshaller();

			final JAXBElement<LguiType> doc = (JAXBElement<LguiType>) unmarshaller.unmarshal(xml.toURL());

			lml = doc.getValue();
		} catch (final JAXBException e) {
			e.printStackTrace();
		}

		return lml;

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
	 * 
	 */
	private final LMLManager lmlManager = LMLManager.getInstance();

	/*
	 * String for the to saved layout.
	 */
	private final String savedLayout = null;

	private static String lmlNamespace = "http://www.llview.de";

	/*
	 * Map of running jobs.
	 */
	public Map<String, String> jobsRunningMap = new HashMap<String, String>();

	/*
	 * Map of waiting jobs.
	 */
	public Map<String, String> jobsWaitingMap = new HashMap<String, String>();

	/*
	 * Map of other jobs.
	 */
	// public Map<String, String> jobsFurtherMap = new HashMap<String, String>();

	public static final String LAYOUT = "layout";

	public static final String JOB = "job";

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

	/**************************************************************************************************************
	 * Parsing methods
	 **************************************************************************************************************/

	/**
	 * Empty Constructor.
	 */
	public LguiItem(String name) {
		this.name = name;
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
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		createLguiHandlers();
		setCid();
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

	/**************************************************************************************************************
	 * Further methods for setting up
	 **************************************************************************************************************/

	public void addUserJob(String name, JobStatusData status) {
		final Map<String, String> map = findMap(status);
		if (status.getJobInfo() == null) {
			map.put(name, null);
		} else {
			map.put(name, status.getJobInfo().getOid());
		}

	}

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

	private Map<String, String> findMap(JobStatusData status) {
		if (status.getState().equals("RUNNING")) {
			return jobsRunningMap;
		} else {
			return jobsWaitingMap;
		}
	}

	private Map<String, String> findMap(String name) {
		if (jobsRunningMap.containsKey(name)) {
			return jobsRunningMap;
		} else {
			return jobsWaitingMap;
		}
	}

	private LguiType firstRequest() {
		final ObjectFactory objectFactory = new ObjectFactory();

		final LguiType layoutLgui = objectFactory.createLguiType();
		layoutLgui.setVersion("1");
		layoutLgui.setLayout(true);

		final RequestType request = objectFactory.createRequestType();
		request.setGetDefaultData(true);
		layoutLgui.setRequest(request);

		return layoutLgui;
	}

	public void getCurrentLayout(OutputStream output) {
		LguiType layoutLgui = null;
		if (lgui == null) {
			layoutLgui = firstRequest();
		} else {
			layoutLgui = getLayoutAccess().getLayoutFromModel();
		}
		final Marshaller marshaller = LMLCorePlugin.getDefault().getMarshaller();
		try {
			marshaller.setProperty("jaxb.schemaLocation", lmlNamespace + " lgui.xsd");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			final QName tagname = new QName(lmlNamespace, "lgui", "lml");

			final JAXBElement<LguiType> rootElement = new JAXBElement<LguiType>(tagname, LguiType.class, layoutLgui);
			marshaller.marshal(rootElement, output);
			output.close(); // Must close to flush stream
		} catch (final PropertyException e) {
			e.printStackTrace();
		} catch (final JAXBException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
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

	public LguiType getLguiType() {
		return lgui;
	}

	private List<String> getListNullElements(Map<String, String> map) {
		final List<String> list = new ArrayList<String>();
		for (final Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue() == null) {
				list.add(entry.getKey());
			}
		}
		return list;
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

	public OverviewAccess getOverviewAccess() {
		if (lguiHandlers.get(OverviewAccess.class) == null) {
			return null;
		}
		return (OverviewAccess) lguiHandlers.get(OverviewAccess.class);
	}

	public void getRequestXml(FileOutputStream output) {
		LguiType layoutLgui = null;
		if (lgui == null) {
			layoutLgui = firstRequest();
		} else {
			layoutLgui = getLayoutAccess().getLayoutFromModel();
		}
		final Marshaller marshaller = LMLCorePlugin.getDefault().getMarshaller();
		try {
			marshaller.setProperty("jaxb.schemaLocation", lmlNamespace + " lgui.xsd");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			final QName tagname = new QName(lmlNamespace, "lgui", "lml");

			final JAXBElement<LguiType> rootElement = new JAXBElement<LguiType>(tagname, LguiType.class, layoutLgui);
			marshaller.marshal(rootElement, output);
			output.close();
		} catch (final PropertyException e) {
			e.printStackTrace();
		} catch (final JAXBException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	public TableHandler getTableHandler() {
		if (lguiHandlers.get(TableHandler.class) == null) {
			return null;
		}
		return (TableHandler) lguiHandlers.get(TableHandler.class);
	}

	public Map<String, String> getUserJobMap(String gid) {
		if (gid.equals("joblistrun")) {
			return jobsRunningMap;
		} else {
			return jobsWaitingMap;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getVersion()
	 */
	public String getVersion() {
		return lgui.getVersion();
	}

	public boolean isEmpty() {
		if (lgui == null) {
			return true;
		} else {
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#isLayout()
	 */
	public boolean isLayout() {
		return lgui.isLayout();
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
		final LguiType old = lgui;
		LguiType lml = null;
		try {
			final Unmarshaller unmarshaller = LMLCorePlugin.getDefault().getUnmarshaller();

			final JAXBElement<LguiType> doc = (JAXBElement<LguiType>) unmarshaller.unmarshal(stream);

			lml = doc.getValue();
		} catch (final JAXBException e) {
			e.printStackTrace();
			lml = old;
		}

		return lml;
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

	public void removeUserJob(String name) {
		final Map<String, String> map = findMap(name);
		map.remove(name);
	}

	public void restoreUserJobs(Map<String, JobStatusData> map) {
		for (final Map.Entry<String, JobStatusData> entry : map.entrySet()) {
			addUserJob(entry.getKey(), entry.getValue());
		}
	}

	public Map<String, String> revert(Map<String, String> map) {
		final Map<String, String> revertMap = new HashMap<String, String>();
		for (final Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				revertMap.put(entry.getValue(), entry.getKey());
			}
		}
		return revertMap;
	}

	private void setCid() {
		for (final TableType table : getTableHandler().getTables()) {
			for (final RowType row : table.getRow()) {
				int cid = 1;
				for (final CellType cell : row.getCell()) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#toString
	 */
	@Override
	public String toString() {
		return name;
	}

	public void update() {
		final ILguiUpdatedEvent e = new LguiUpdatedEvent(this);
		for (final ILguiListener listener : listeners) {
			listener.handleEvent(e);
		}
	}

	public void update(InputStream stream) {
		lgui = parseLML(stream);
		if (listeners.isEmpty()) {
			createLguiHandlers();
		}
		updateJobData();
		update();
		setCid();
	}

	public boolean update(String name, JobStatusData status) {
		final Map<String, String> oldMap = findMap(name);
		final Map<String, String> newMap = findMap(status);

		if (oldMap != newMap) {
			oldMap.remove(name);
			newMap.put(name, status.getJobInfo().getOid());
			return true;
		}
		return false;
	}

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

		final LguiUpdatedEvent event = new LguiUpdatedEvent(this);
		for (final ILguiListener l : listeners) {
			l.handleEvent(event);
		}
	}

	private void updateJobData() {
		// JobInfo to JobStatusData
		updateJobData(jobsRunningMap);
		updateJobData(jobsWaitingMap);
		// updateJobData(jobsFurtherMap);

		// JobStatusData to Table
		updateJobDate(revert(jobsRunningMap), getListNullElements(jobsRunningMap), "joblistrun");
		updateJobDate(revert(jobsWaitingMap), getListNullElements(jobsWaitingMap), "joblistwait");
		// updateJobDate(revert(jobsFurtherMap), "joblistfurther");
	}

	private void updateJobData(Map<String, String> map) {
		for (final Map.Entry<String, String> entry : map.entrySet()) {
			String oid = entry.getValue();
			if (oid == null) {
				final String key = entry.getKey();
				oid = getOverviewAccess().getOIDByJobName(key);
				if (oid == null) {
					System.out.println("Error");
					return;
				} else {
					map.put(key, oid);
				}
			}
			lmlManager.updateJobData(toString(), entry.getKey(), getOIDToInformation().getInfoByOid(oid));
		}
	}

	private void updateJobDate(Map<String, String> map, List<String> list, String gid) {
		final TableType table = getTableHandler().getTable(gid);
		if (table == null) {
			return;
		}
		for (final RowType row : table.getRow()) {
			map.remove(row.getOid());
		}
		if (map.size() > 0) {
			for (final Map.Entry<String, String> entry : map.entrySet()) {
				final InfoType info = lmlManager.getJobStatusDataInfo(toString(), entry.getValue());
				final RowType row = new RowType();
				row.setOid(entry.getKey());
				for (final ColumnType column : table.getColumn()) {
					if (info != null) {
						for (final InfodataType data : info.getData()) {
							if (column.getName().equals(data.getKey())) {
								final CellType cell = new CellType();
								cell.setCid(column.getId());
								cell.setValue(data.getValue());
								row.getCell().add(cell);
								break;
							}
						}
					} else {
						if (column.getName().equals("owner") || column.getName().equals("status")) {
							final CellType cell = new CellType();
							cell.setCid(column.getId());
							cell.setValue(lmlManager.getJobStatusData(toString(), entry.getValue()).getState());
							row.getCell().add(cell);
							break;
						}
					}

				}

				table.getRow().add(row);
			}
		}
		if (list.size() > 0) {
			for (final String entry : list) {
				final JobStatusData data = lmlManager.getJobStatusData(toString(), entry);
				final RowType row = new RowType();
				for (final ColumnType column : table.getColumn()) {
					if (column.getName().equals("owner") || column.getName().equals("status")) {
						final CellType cell = new CellType();
						cell.setCid(column.getId());
						cell.setValue(data.getState());
						row.getCell().add(cell);
						break;
					}
				}
				table.getRow().add(row);
			}
		}
	}
}