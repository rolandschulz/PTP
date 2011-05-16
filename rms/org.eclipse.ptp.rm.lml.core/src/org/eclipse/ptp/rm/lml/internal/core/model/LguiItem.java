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

import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiHandler;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.CellType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RowType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiUpdatedEvent;

/**
 * Class of the interface ILguiItem
 */
public class LguiItem implements ILguiItem {

	/*
	 * Source of the XML-file from which the LguiType was generated.
	 */
	private URI xmlFile;

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
	private final HashMap<Class<? extends ILguiHandler>, ILguiHandler> lguiHandlers = new HashMap<Class<? extends ILguiHandler>, ILguiHandler>();
	
	/**************************************************************************************************************
	 * Constructors
	 **************************************************************************************************************/

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
	 * Constructor which get an InputStream as argument 
	 * 
	 * @param stream InputStream
	 */
	public LguiItem(InputStream stream) {
		
		lgui = parseLML(stream);
		createLguiHandlers();
		setCid();
	}

	/**
	 * Constructor with one argument, an URI. Within the constructor the method
	 * for parsing an XML-file into LguiItem is called.
	 * 
	 * @param xmlFile
	 *            the source of the XML file.
	 */
	public LguiItem(URI xmlFile) {
		this.xmlFile = xmlFile;
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
		LguiType lml = null;
		try {
			Unmarshaller unmarshaller = LMLCorePlugin.getDefault().getUnmarshaller();

			JAXBElement<LguiType> doc = (JAXBElement<LguiType>) unmarshaller.unmarshal(stream);

			lml = doc.getValue();
		} catch (JAXBException e) {
			e.printStackTrace();
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

	public void updateXML() {
		lgui = null;
		try {
			xmlFile = new URI(xmlFile.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		try {
			lgui = parseLML(xmlFile);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		ILguiUpdatedEvent e = new LguiUpdatedEvent(this);
		for (ILguiListener listener : listeners) {
			listener.handleEvent(e);
		}
	}

	public void update() {

		for (ILguiListener listener : listeners) {
			ILguiUpdatedEvent e = new LguiUpdatedEvent(this);
			listener.handleEvent(e);
		}
	}

	/**************************************************************************************************************
	 * Further methods
	 **************************************************************************************************************/
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#getXMLFile()
	 */
	public URI getXmlFile() {
		return xmlFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.elements.ILguiItem#toString
	 */
	@Override
	public String toString() {
		if (getXmlFile().getPath() == null) {
			return "es gibt hier nix";
		}
		return getXmlFile().getPath();
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

	

	

	public void addJob() {
		LguiType newLgui = lgui;
		lgui.getObjectsAndRelationsAndInformation();
		updateData(newLgui);
	}

}