package org.eclipse.ptp.rm.jaxb.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeDefinitions;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Commands;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFile;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.Parsers;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.StreamParser;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.xml.sax.SAXException;

public class JAXBUtils implements IJAXBNonNLSConstants {

	private JAXBUtils() {
	}

	public static URL getURL(String xml) throws IOException {
		URL instance = JAXBCorePlugin.getResource(xml);
		if (instance == null) {
			File f = new File(xml);
			if (f.exists() && f.isFile()) {
				instance = f.toURL();
			} else {
				throw new FileNotFoundException(xml);
			}
		}
		return instance;
	}

	public static ResourceManagerData initializeRMData(String xml) throws IOException, SAXException, URISyntaxException,
			JAXBException {
		ResourceManagerData rmdata = null;
		URL instance = getURL(xml);
		rmdata = unmarshalResourceManagerData(instance);
		if (rmdata != null) {
			Control control = rmdata.getControl();
			initialize(control);
		}
		return rmdata;
	}

	public static void serializeScript(Map<String, Object> env, Control control) {
	}

	public static void validate(String xml) throws SAXException, IOException, URISyntaxException {
		URL instance = getURL(xml);
		URL xsd = JAXBCorePlugin.getResource(RM_XSD);
		SchemaFactory factory = SchemaFactory.newInstance(XMLSchema);
		Schema schema = factory.newSchema(xsd);
		Validator validator = schema.newValidator();
		Source source = new StreamSource(instance.openStream());
		validator.validate(source);
	}

	private static void addAttributes(Map<String, Object> env, Control control) {
		AttributeDefinitions adefs = control.getAttributeDefinitions();
		if (adefs == null) {
			return;
		}
		List<JobAttribute> jobAttributes = adefs.getJobAttribute();
		for (JobAttribute jobAttribute : jobAttributes) {
			String name = jobAttribute.getName();
			env.put(name, jobAttribute);
		}
	}

	private static void addCommands(Map<String, Object> env, Control control) {
		Commands comms = control.getCommands();
		if (comms == null) {
			return;
		}
		List<Command> commands = comms.getCommand();
		for (Command command : commands) {
			env.put(command.getName(), command);
		}
	}

	private static void addFiles(Map<String, Object> env, Control control) {
		ManagedFiles mf = control.getManagedFiles();
		if (mf == null) {
			return;
		}
		List<ManagedFile> files = mf.getManagedFile();
		for (ManagedFile file : files) {
			env.put(file.getName(), file);
		}
	}

	private static void addParsers(Map<String, Object> env, Control control) {
		Parsers prsrs = control.getParsers();
		if (prsrs == null) {
			return;
		}
		List<StreamParser> parsers = prsrs.getStreamParser();
		for (StreamParser parser : parsers) {
			env.put(parser.getName(), parser);
		}
	}

	private static void addProperties(Map<String, Object> env, Control control) {
		List<Property> properties = control.getProperty();
		for (Property property : properties) {
			env.put(property.getName(), null);
		}
	}

	private static void initialize(Control control) {
		Map<String, Object> env = RMVariableMap.getInstance().getVariables();
		addProperties(env, control);
		addAttributes(env, control);
		addCommands(env, control);
		addFiles(env, control);
		addParsers(env, control);
	}

	private static ResourceManagerData unmarshalResourceManagerData(URL xml) throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance(JAXB_CONTEXT, JAXBUtils.class.getClassLoader());
		Unmarshaller u = jc.createUnmarshaller();
		ResourceManagerData rmdata = (ResourceManagerData) u.unmarshal(xml.openStream());
		return rmdata;
	}
}
