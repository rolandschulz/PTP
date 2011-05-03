package org.eclipse.ptp.rm.lml.internal.core.model.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads a license text from a text-file. Adds this text to target xml-files as first comment-node.
 * The license text is added before the root-element. If there is another comment, it is replaced
 * by a comment with the new license text.
 * 
 * A list of xml-files can be passed. Every single file is processed.
 * 
 * @author karbach
 *
 */
public class LicenseReplace {

	private static final String licenseTextPath="schema/license.txt";

	/**
	 * Read the license-text from file and return it as string
	 * @param path Path to license-text-filde
	 * @return full string license
	 * @throws FileNotFoundException 
	 */
	private static String readLicenseText(String path) throws FileNotFoundException{
		Scanner in=new Scanner(new File(path));

		String res="";
		while(in.hasNext()){
			res+=in.nextLine()+"\n";
		}

		return res;
	}

	public static void main( String[] args ) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException
	{
		if(args.length==0){
			System.out.println("Usage: pass at least one argument. The passed files' license texts are changed to the text in license.txt");
			System.exit(1);
		}

		for(int i=0; i<args.length; i++){
			String filepath=args[i];

			DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();

			DocumentBuilder        builder  = factory.newDocumentBuilder();

			Document doc = builder.parse(new File(filepath));

			NodeList nodes=doc.getChildNodes();

			String licenseText=readLicenseText(licenseTextPath);//The new text for the license

			if(nodes.getLength()>=1 ){//at least one child expected

				Node node=nodes.item(0);
				if(node.getNodeType() == Node.COMMENT_NODE){//first node is comment node
					node.setNodeValue(licenseText);
				}
				else{//Insert new node
					Node comment=doc.createComment(licenseText);
					doc.insertBefore(comment, node);
				}
			}
			else{//No node inside => insert only license
				Node comment=doc.createComment(licenseText);
				doc.appendChild(comment);
			}

			//Print out new xml-file
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource        source = new DOMSource( doc );

			FileOutputStream f = new FileOutputStream(new File( filepath ));
			StreamResult     result = new StreamResult( f );
			transformer.transform( source, result );

			System.out.println("New license-text for "+filepath);

		}

	}


}
