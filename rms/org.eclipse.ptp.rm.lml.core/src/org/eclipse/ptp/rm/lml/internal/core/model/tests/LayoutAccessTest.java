package org.eclipse.ptp.rm.lml.internal.core.model.tests;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.internal.core.elements.AbslayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ChartlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ComponentlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoboxlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.SplitlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TablelayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.UsagebarlayoutType;
import org.eclipse.ptp.rm.lml.internal.core.model.LayoutAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Tests functions of class LayoutAccess.
 * Every test is designed for one function of LayoutAccess.
 * 
 * @author karbach
 *
 */
public class LayoutAccessTest extends TestCase{
	
	private static final String samplePath="src/org/eclipse/ptp/rm/lml/internal/core/model/tests/data/jugene_example.xml";
	private static final String sampleLayoutPath="src/org/eclipse/ptp/rm/lml/internal/core/model/tests/data/jugene_layout.xml";
	
	private LguiType model;
	private LguiItem lguihandler;
	private LayoutAccess layoutaccess;
	
	private int counter;
	
	/**
	 * @return Object of LguiType parsed out of an lml-File
	 * @throws JAXBException
	 */
	protected static LguiType parseLML(Object urlOrInputStream, URL xsd) throws JAXBException{
		//Causes errors while used in applet
		
		JAXBContext jc = JAXBContext.newInstance("org.eclipse.ptp.rm.lml.internal.core.elements");
		
		Unmarshaller unmar=jc.createUnmarshaller();

		
		if(xsd!=null){
			Schema mySchema;
			SchemaFactory sf =
				SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
			try {
				mySchema = sf.newSchema( xsd );
			} catch( SAXException saxe ){
				// ...(error handling)
				mySchema = null;
			}

			//Connect schema to unmarshaller
			unmar.setSchema(mySchema);
		}
		
		//Validate lml-file and unmarshall in one step
		JAXBElement<LguiType> doc;
		if( urlOrInputStream instanceof URL )
			doc=(JAXBElement<LguiType>)unmar.unmarshal( (URL)urlOrInputStream);
		else if(urlOrInputStream instanceof InputStream)
			doc=(JAXBElement<LguiType>)unmar.unmarshal( (InputStream)urlOrInputStream);
		else throw new JAXBException("Cannot read from instance "+urlOrInputStream+".");
		//Get root-element
		LguiType lml=doc.getValue();
		
		return lml;
	}
	
	/**
	 * Test if parsing the sample file works
	 * Otherwise all other tests will not work
	 * @throws MalformedURLException
	 * @throws JAXBException
	 */
	@Test
	public void testLMLParsing() throws MalformedURLException, JAXBException{
		
		File lmlfile=new File(samplePath);
		//File must exist
		assertTrue("example-lml-file must exist in given folder", lmlfile.exists());
		URI uri=lmlfile.toURI();
		
		LguiType lml=parseLML( new URL(uri.toString()), null);
		assertNotNull("LML-parsing should return an object-instance not null", lml);
	}
	
	//Called before every test
	@Before
	public void setUp()  throws MalformedURLException, JAXBException{
		File lmlfile=new File(samplePath);
		URI uri=lmlfile.toURI();
		
		model=parseLML( new URL(uri.toString()), null);
		
		lguihandler=new LguiItem(model); 
		
		layoutaccess=lguihandler.getLayoutAccess();	
		
		counter=0;
	}
	
	@Test
	public void testReplaceComponentLayout(){
		
		ObjectFactory fact=new ObjectFactory();
		
		ComponentlayoutType nodedisplaylayout=fact.createNodedisplaylayoutType();
		nodedisplaylayout.setGid("jugene");
		nodedisplaylayout.setId("newlayout");
		
		
		//Listen for updates
		lguihandler.addListener(new ILguiListener() {
			public void handleEvent(ILguiUpdatedEvent e) {
				counter++;
			}
		});
		
		//Replace an existant layout
		layoutaccess.replaceComponentLayout( nodedisplaylayout );
		
		assertEquals( "The model was changed. The update-event has to be passed to the listener.", 1, counter );
		
		List<ComponentlayoutType> layouts=layoutaccess.getComponentLayoutByGID("jugene");
		
		assertEquals( "Only one Nodedisplaylayout for nodedisplay should exist", 1, layouts.size());
		assertTrue( "The old nodedisplaylayout should be replaced by the new one" ,layouts.get(0).getId().equals("newlayout"));
		
		//Insert new componentlayout
		ComponentlayoutType tablelayout=fact.createTablelayoutType();
		tablelayout.setGid("2");
		tablelayout.setId("tablelayout");
		
		layoutaccess.replaceComponentLayout( tablelayout );
		assertEquals( "The model was changed again. The update-event has to be passed to the listener.", 2, counter );
		
		List<ComponentlayoutType> tablelayouts=layoutaccess.getComponentLayoutByGID("2");
		assertEquals( "Only one Tablelayout should be inserted", 1, tablelayouts.size());
		assertTrue( "Make sure that the passed tabelayout was inserted" ,tablelayouts.get(0).getId().equals("tablelayout"));
		
		//Replace two componentlayouts
		TablelayoutType tablelayout2=fact.createTablelayoutType();
		tablelayout2.setGid("2");
		tablelayout2.setId("the second");
		model.getObjectsAndRelationsAndInformation().add(new JAXBElement<TablelayoutType>(new QName("tablelayout"), TablelayoutType.class, tablelayout2)  ) ;
		
		tablelayouts=layoutaccess.getComponentLayoutByGID("2");
		assertEquals( "Now there are two tablelayouts for graphical object with id 2", 2, tablelayouts.size());
		
		//Replace the new one again with the old
		layoutaccess.replaceComponentLayout( tablelayout );
		assertEquals( "The model was changed again. The update-event has to be passed to the listener.", 3, counter );
		
		tablelayouts=layoutaccess.getComponentLayoutByGID("2");
		assertEquals( "Only one Tablelayout after replacing", 1, tablelayouts.size());
		assertTrue( "Make sure that the passed tabelayout was inserted" ,tablelayouts.get(0).getId().equals("tablelayout"));
		
		//Replace with the second
		layoutaccess.replaceComponentLayout( tablelayout2 );
		tablelayouts=layoutaccess.getComponentLayoutByGID("2");
		assertEquals( "Only one Tablelayout after replacing", 1, tablelayouts.size());
		assertTrue( "Make sure that the passed tabelayout was inserted" ,tablelayouts.get(0).getId().equals("the second"));
	}
	
	@Test
	public void testgetUsagebarLayout(){
		
		UsagebarlayoutType layout=layoutaccess.getUsagebarLayout("3");
		
		assertTrue( "Usagebarlayout id should be equal to ubl1", layout.getId().equals("ubl1") );
		
		layout=layoutaccess.getUsagebarLayout("notexistant");
		
		assertNull( "A default layout-instance should be returned. As a result the gid-attribute is null", layout.getGid() );
	}
	
	@Test
	public void testgetChartLayout(){
		
		ChartlayoutType layout=layoutaccess.getChartLayout("7");
		
		
		assertTrue( "Chartlayout id should be equal to cl1", layout.getId().equals("cl1") );
		
		layout=layoutaccess.getChartLayout("notexistant");
		
		assertNull( "A default layout-instance should be returned. As a result the gid-attribute is null", layout.getGid() );
	}
	
	@Test
	public void testgetInfoboxLayout(){
		
		InfoboxlayoutType layout=layoutaccess.getInfoboxLayout("4");
		
		
		assertTrue( "Infoboxlayout id should be equal to infolayout", layout.getId().equals("infolayout") );
		
		layout=layoutaccess.getInfoboxLayout("notexistant");
		
		assertNull( "A default layout-instance should be returned. As a result the gid-attribute is null", layout.getGid() );
	}
	
	@Test
	public void testgetTextboxLayout(){
		
		InfoboxlayoutType layout=layoutaccess.getTextboxLayout("6");
		
		
		assertTrue( "Textlayout id should be equal to infolayout", layout.getId().equals("textlayout") );
		
		layout=layoutaccess.getTextboxLayout("notexistant");
		
		assertNull( "A default layout-instance should be returned. As a result the gid-attribute is null", layout.getGid() );
	}
	
	@Test
	public void testmergeLayouts() throws MalformedURLException, JAXBException{
		
		//Read layout-file
		File lmlfile=new File(sampleLayoutPath);
		URI uri=lmlfile.toURI();
		
		LguiType layout=parseLML( new URL(uri.toString()), null);
		
		assertEquals("Layout consists of 6 tags within root-tag.", 6, layout.getObjectsAndRelationsAndInformation().size());
		
		assertEquals("mergeLayouts should return reference to model ", model, LayoutAccess.mergeLayouts(model, layout) );
		
		assertEquals( "Textboxlayout should be replaced by new one.", "newtextlayout" , layoutaccess.getTextboxLayout("6").getId() );
		
		assertEquals( "After replacement only one layout for every component should remain.", 1, layoutaccess.getComponentLayoutByGID("6").size() );
		
		assertEquals("Nodedisplaylayout has to be replaced by new one.", "newnodedisplaylayout", layoutaccess.getComponentLayoutByGID("jugene").get(0).getId());
		
		assertNull("Nodedisplay tag itself should not be replaced by the stub-tag from layout-file.", lguihandler.getNodedisplayAccess().getNodedisplayById("jugene").getDescription());
		
		assertEquals( "One absolute layout is replaced.", 2, lguihandler.getOverviewAccess().getAbslayouts().size());
		
		List<AbslayoutType> abslayouts=lguihandler.getOverviewAccess().getAbslayouts();
		AbslayoutType abs1=null;
		for(AbslayoutType alayout:abslayouts){
			if(alayout.getId().equals("abs1")){
				abs1=alayout;
			}
		}
		
		assertEquals( "abs1 should be replaced.", 1, abs1.getComp().size() );
		
		assertEquals( "One splitlayout is added.", 2, lguihandler.getOverviewAccess().getSplitlayouts().size());
	}
	
	@Test
	public void testgetLayoutFromModell() throws  MalformedURLException, SAXException, JAXBException{
		
		LguiType layout=LayoutAccess.getLayoutFromModell(model);
		
		//Marshall LguiType-instance in order to validate it
		JAXBContext jc = JAXBContext.newInstance("org.eclipse.ptp.rm.lml.internal.core.elements");

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Schema schema = schemaFactory.newSchema(new File("schema/lgui.xsd")); 

		QName tagname=new QName("http://www.llview.de", "lgui", "lml");
		JAXBElement<LguiType> rootel=new JAXBElement<LguiType>(tagname, LguiType.class, layout);
		
		//Tests if created JAXB-lml-tree is valid against XSD
		//if not, an exception is thrown
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setSchema(schema);
		marshaller.marshal(rootel, new DefaultHandler());
		
		LguiItem item=new LguiItem(layout);
		assertEquals( "The splitlayout should be copied to layout-instance.", 1, item.getOverviewAccess().getSplitlayouts().size());
		
		assertEquals( "The abslayouts should be copied to layout-instance.", 2, item.getOverviewAccess().getAbslayouts().size());
		
		assertEquals( "All componentlayouts should be copied.", "cl1", item.getLayoutAccess().getChartLayout("7").getId());
	}
	
	@Test
	public void testgetComponentLayoutByGID(){
		
		List<ComponentlayoutType> layouts=layoutaccess.getComponentLayoutByGID("6");
		
		assertEquals("There are two layouts for graphical object with id 6.", 2, layouts.size());
		
		layouts=layoutaccess.getComponentLayoutByGID("jugene");
		assertEquals("There is one layout for the nodedisplay jugene.", 1, layouts.size());
	}
	
	@Test
	public void testaddLayoutTag(){
		
		AbslayoutType abslayout=layoutaccess.generateDefaultAbsoluteLayout(100, 100);
		
		abslayout.setId("newabsolute");
		
		lguihandler.addListener(new ILguiListener() {
			
			@Override
			public void handleEvent(ILguiUpdatedEvent e) {
				counter++;
			}
		});
		
		layoutaccess.addLayoutTag(abslayout);
		
		assertEquals("A new absolute layout inserted. The listenere should be informed.", 1, counter );
		
		assertEquals("The new abslayout must be found in model.", 3, lguihandler.getOverviewAccess().getAbslayouts().size());
		
		//add a splitlayout
		SplitlayoutType split=lguihandler.getOverviewAccess().getSplitlayouts().get(0);
		split.setId("newsplit");
		layoutaccess.addLayoutTag(split);
		
		assertEquals("A new splitlayout inserted. The listenere should be informed.", 2, counter );
		
		assertEquals("The new splitlayout must be found in model.", 2, lguihandler.getOverviewAccess().getSplitlayouts().size());
	}
	
	@Test
	public void testgenerateDefaultAbsoluteLayout(){
		
		AbslayoutType abslayout=layoutaccess.generateDefaultAbsoluteLayout(100, 100);
		
		assertEquals("All active components should be covered by the layout.", 7,abslayout.getComp().size());
		
	}
	
	@Test
	public void testgetDefaultTableLayout(){
		
		List<GobjectType> gobjs=lguihandler.getOverviewAccess().getGraphicalObjects();
		
		TableType table=null;
		
		for(GobjectType gobj:gobjs){
			if(gobj instanceof TableType){
				table=(TableType)gobj;
			}
		}
		
		TablelayoutType tlayout=layoutaccess.getDefaultTableLayout(table.getId());
		
		assertEquals("Amount of columnlayouts is equal to amount of column-definitions in table.", table.getColumn().size(), tlayout.getColumn().size());
		
	}
	
	@Test
	public void testgetComponentLayouts(){
		
		assertEquals("Test expected amount of componentlayouts", 7, layoutaccess.getComponentLayouts().size());
	}
	
	@Test
	public void testgetNodedisplayLayouts(){
		
		assertEquals("Test expected amount of nodedisplaylayouts", 1, layoutaccess.getNodedisplayLayouts().size());
	}
	
	@Test
	public void testgetTableLayouts(){
		
		assertEquals("Test expected amount of tablelayouts", 0, layoutaccess.getTableLayouts().size());
	}
	
	
	@Test
	public void testgetUsagebarLayouts(){
		
		assertEquals("Test expected amount of usagebarlayouts", 1, layoutaccess.getUsagebarLayouts().size());
	}
	
	@Test
	public void testgetChartLayouts(){
		
		assertEquals("Test expected amount of chartlayouts", 2, layoutaccess.getChartLayouts().size());
	}
	
	
}
