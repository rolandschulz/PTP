package org.eclipse.ptp.rmsystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.eclipse.jface.text.Assert;
import org.eclipse.ptp.core.PTPCorePlugin;

/**
 * Serializes resource manager as character or byte stream and reads the same format
 * back.
 * 
 */
public class ResourceManagerReaderWriter {

	private static final String RM_ROOT = "resourcemanagers"; //$NON-NLS-1$
	private static final String RM_ELEMENT = "resourcemanager"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private static final String HOST_ATTRIBUTE= "host"; //$NON-NLS-1$
	private static final String PORT_ATTRIBUTE= "port"; //$NON-NLS-1$

	/**
	 * Create a new instance.
	 */
	public ResourceManagerReaderWriter() {
	}

	/**
	 * Reads resource managers from a reader and returns them. The reader must present
	 * a serialized form as produced by the <code>save</code> method.
	 *
	 * @param reader the reader to read resource managers from
	 * @return the read resource managers
	 * @throws IOException if reading from the stream fails
	 */
	public IResourceManager[] read(Reader reader) throws IOException {
		return read(reader, null);
	}

	/**
	 * Reads the resource manager with identifier <code>id</code> from a reader and
	 * returns it. The reader must present a serialized form as produced by the
	 * <code>save</code> method.
	 *
	 * @param reader the reader to read resource managers from
	 * @param id the id of the resource manager to return
	 * @return the read resource manager
	 * @throws IOException if reading from the stream fails
	 */
	public IResourceManager readSingle(Reader reader, String id) throws IOException {
		IResourceManager[] datas= read(new InputSource(reader), null, id);
		if (datas.length > 0)
			return datas[0];
		return null;
	}

	/**
	 * Reads resource managers from a stream and adds them to the resource managers.
	 *
	 * @param reader the reader to read resource managers from
	 * @param bundle a resource bundle to use for translating the read resource managers, or <code>null</code> if no translation should occur
	 * @return the read resource managers
	 * @throws IOException if reading from the stream fails
	 */
	public IResourceManager[] read(Reader reader, ResourceBundle bundle) throws IOException {
		return read(new InputSource(reader), bundle, null);
	}

	/**
	 * Reads resource managers from a stream and adds them to the resource managers.
	 *
	 * @param stream the byte stream to read resource managers from
	 * @param bundle a resource bundle to use for translating the read resource managers, or <code>null</code> if no translation should occur
	 * @return the read resource managers
	 * @throws IOException if reading from the stream fails
	 */
	public IResourceManager[] read(InputStream stream, ResourceBundle bundle) throws IOException {
		return read(new InputSource(stream), bundle, null);
	}

	/**
	 * Reads resource managers from an <code>InputSource</code> and adds them to the resource managers.
	 *
	 * @param source the input source
	 * @param bundle a resource bundle to use for translating the read resource managers, or <code>null</code> if no translation should occur
	 * @param singleId the resource manager id to extract, or <code>null</code> to read in all resource managers
	 * @return the read resource managers
	 * @throws IOException if reading from the stream fails
	 */
	private IResourceManager[] read(InputSource source, ResourceBundle bundle, String singleId) throws IOException {
		try {
			Collection rms = new ArrayList();

			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder parser= factory.newDocumentBuilder();
			Document document= parser.parse(source);

			NodeList elements= document.getElementsByTagName(RM_ELEMENT);

			int count= elements.getLength();
			for (int i= 0; i != count; i++) {
				Node node= elements.item(i);
				NamedNodeMap attributes= node.getAttributes();

				if (attributes == null)
					continue;

				String name= getStringValue(attributes, NAME_ATTRIBUTE);

				String description= getStringValue(attributes, DESCRIPTION_ATTRIBUTE, ""); //$NON-NLS-1$

				String rm= getStringValue(attributes, ID_ATTRIBUTE);

				if (name == null || rm == null)
					throw new IOException("Missing attribute"); //$NON-NLS-1$

				String host = getStringValue(attributes, HOST_ATTRIBUTE, "");
				int port = getIntValue(attributes, PORT_ATTRIBUTE, -1);

				IResourceManagerConfiguration config = new ResourceManagerConfiguration(name, description, rm, host, port);
				IResourceManagerFactory rmFactory = PTPCorePlugin.getDefault().getResourceManagerFactory(rm);
				if (rmFactory != null) 
				{
					rms.add(rmFactory.create(config));
				}

			}

			return (IResourceManager[]) rms.toArray(new IResourceManager[rms.size()]);

		} catch (ParserConfigurationException e) {
			Assert.isTrue(false);
		} catch (SAXException e) {
			Throwable t= e.getCause();
			if (t instanceof IOException)
				throw (IOException) t;
			else if (t != null)
				throw new IOException(t.getMessage());
			else
				throw new IOException(e.getMessage());
		}

		return null; // dummy
	}

	/**
	 * Saves the resource managers as XML, encoded as UTF-8 onto the given byte stream.
	 *
	 * @param resource managers the resource managers to save
	 * @param stream the byte output to write the resource managers to in XML
	 * @throws IOException if writing the resource managers fails
	 */
	public void save(IResourceManager[] rms, OutputStream stream) throws IOException {
		save(rms, new StreamResult(stream));
	}

	/**
	 * Saves the resource managers as XML.
	 *
	 * @param rms the resource managers to save
	 * @param writer the writer to write the resource managers to in XML
	 * @throws IOException if writing the resource managers fails
	 */
	public void save(IResourceManager[] rms, Writer writer) throws IOException {
		save(rms, new StreamResult(writer));
	}

	/**
	 * Saves the resource managers as XML.
	 *
	 * @param rms the resource managers to save
	 * @param result the stream result to write to
	 * @throws IOException if writing the targets fails
	 */
	private void save(IResourceManager[] rms, StreamResult result) throws IOException {
		try {
			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder= factory.newDocumentBuilder();
			Document document= builder.newDocument();

			Node root= document.createElement(RM_ROOT);
			document.appendChild(root);

			for (int i= 0; i < rms.length; i++) {
				IResourceManager target= rms[i];
				
				
				Node node= document.createElement(RM_ELEMENT);
				root.appendChild(node);

				NamedNodeMap attributes= node.getAttributes();
				
				if (target != null) {
					IResourceManagerConfiguration config = target.getConfiguration();
					
					Attr name= document.createAttribute(NAME_ATTRIBUTE);
					name.setValue(config.getName());
					attributes.setNamedItem(name);

					Attr description= document.createAttribute(DESCRIPTION_ATTRIBUTE);
					description.setValue(config.getDescription());
					attributes.setNamedItem(description);

					Attr context= document.createAttribute(ID_ATTRIBUTE);
					context.setValue(config.getResourceManagerId());
					attributes.setNamedItem(context);

					if (config.getHost().trim().length()==0) {
						Attr host= document.createAttribute(HOST_ATTRIBUTE);
						host.setValue(config.getHost());
						attributes.setNamedItem(host);
					}

					if (config.getPort() >=0) {
						Attr port= document.createAttribute(PORT_ATTRIBUTE);
						port.setValue(new Integer(config.getPort()).toString());
						attributes.setNamedItem(port);
					}
				}
			}

			Transformer transformer=TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			DOMSource source = new DOMSource(document);

			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			Assert.isTrue(false);
		} catch (TransformerException e) {
			if (e.getException() instanceof IOException)
				throw (IOException) e.getException();
			Assert.isTrue(false);
		}
	}
	
	private boolean getBooleanValue(NamedNodeMap attributes, String attribute, boolean defaultValue) throws SAXException {
		Node enabledNode= attributes.getNamedItem(attribute);
		if (enabledNode == null)
			return defaultValue;
		else if (enabledNode.getNodeValue().equals(Boolean.toString(true)))
			return true;
		else if (enabledNode.getNodeValue().equals(Boolean.toString(false)))
			return false;
		else
			throw new SAXException("Illegal boolean attribute");
	}

	private int getIntValue(NamedNodeMap attributes, String name, int defaultValue) throws SAXException {
		Node node= attributes.getNamedItem(name);
		return node == null	? defaultValue : Integer.parseInt(node.getNodeValue());
	}

	private String getStringValue(NamedNodeMap attributes, String name) throws SAXException {
		String val= getStringValue(attributes, name, null);
		if (val == null)
			throw new SAXException("Missing attribute");
		return val;
	}

	private String getStringValue(NamedNodeMap attributes, String name, String defaultValue) {
		Node node= attributes.getNamedItem(name);
		return node == null	? defaultValue : node.getNodeValue();
	}

}

