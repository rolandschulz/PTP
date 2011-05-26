/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.ui.providers;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.AlignType;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfodataType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.internal.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.PictureType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck.SchemeAndData;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus.Updatable;
import org.eclipse.ptp.rm.lml.ui.providers.BorderLayout.BorderData;
import org.eclipse.ptp.rm.lml.ui.views.NodedisplayView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;


/**
 * SWT-Translation of NodedisplayPanel, which is a Swing-Component
 * 
 * Composite to create output of a nodedisplay.
 * 
 * Inner composites are NodedisplayComp again (recursive)
 * 
 * A NodedisplayComp represents one physical element within the nodedisplay-tag.
 * This might be a row, midplane, node or cpu. Through NodedisplayComp the reduced
 * collapsed tree of lml is fully expanded to maxlevel. Every visible component
 * in the nodedisplay is shown by a NodedisplayComp.
 * There is one exception: For performance reasons the lowest-level rectangles are
 * painted directly. So for the rectangles (physical elements) in the lowest level
 * no composites are created.
 * 
 * The look of the nodedisplay is defined by the lml-Nodedisplay-Layout.
 *
 */
public class NodedisplayComp extends LguiWidget implements Updatable{ 

	private NodedisplayView nodeview;//parent composite for zooming
	
	//Parameters which are not in apref yet
	private static int iconwidth=15;
	
	private String title;//implicit name of this node
	private Color jobColor;//Color of the job, to which this panel is connected
	
	private Font fontObject;
	private Nodedisplay model;//surrounding lml-Nodedisplay
	
	private DisplayNode node;//Node-model which has to be displayed
	private int maxLevel; //Deepest level, which should be displayed
	
	private Composite pictureFrame;//this panel contains pictures as direct children and the mainpanel in center 
	private Composite mainPanel;//Important panel where everything but pictures is in
	
	private Composite innerPanel;
	private Label titleLabel;//For title-line
	
	//Settings for lower level
	private Nodedisplayelement apref;
	
	//current level, which is displayed
	private int currentLevel;
	
	private GridData gridData;//layout definitions for gridlayouts, makes inner panels resize
	
	private Color backgroundColor;//Current backgroundcolor
	private Color titleBackgroundColor;
	
	//Borders
	private BorderComposite borderFrame;//The only frame which has a border, borderwidth changes when mouse touches the panel
	
	private ArrayList<ImageComp> pictures;
	private Image centerPic;//Picture in center is special
	
	private ArrayList<NodedisplayComp> innerComps;//Save created inner NodedisplayComp-instances for disposing them if needed
	
	private static final int httpsport=4444;//Special port for https-access
	
	private NodedisplayComp parentNodedisplay=null;//Reference to parent nodedisplay
	
	private int minWidth=0, minHeight=0;//Minimal width and height of this nodedisplay
	
	private int x=0, y=0;//Position of this nodedisplay within surrounding grid
	
	//Saves rectpaintlistener, which is needed for fast painting of inner rectangles
	private RectPaintListener rectpaintlistener=null;
	
	/**
	 * Class for comparing integer-values in ascending or descending way.
	 * 
	 * @author karbach
	 *
	 */
	public static class NumberComparator implements Comparator<Integer> {

		private boolean ascending;//if true => sort ascending, otherwise descending
		
		public NumberComparator(boolean ascending){
			this.ascending = ascending;
		}
		
		public int compare(Integer o1, Integer o2) {
			if (ascending) {
				return o1 - o2;
			}
			else return o2 - o1;
		}
		
	}
	
	/**
	 * easy constructor for a nodedisplay as root-node
	 * @param lgui wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param pmodel lml-model for the nodedisplay, which should be shown in this panel
	 * @param lgui complete lml-model containing this nodedisplay
	 * @param parent parameter for calling super constructor
	 * @param style parameter for calling super constructor
	 */
	public NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, NodedisplayView pnodeview, int style){

		this(lgui, pmodel, null, pnodeview, style);
		
	}
	
	/**
	 * Call this constructor for inner or lower elements, rellevel is counted to zero with every level
	 * 
	 * @param lgui wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param pmodel lml-model, which has data for this Nodedisplay
	 * @param pnode current node, which is root-data-element of this NodedisplayComp
	 * @param pnodeview root-nodedisplay is needed for zooming
	 * @param pparentNodedisplay father of this nodedisplay
	 * @param px horizontal position of this nodedisplay in surrounding grid
	 * @param py horizontal position of this nodedisplay in surrounding grid
	 * @param rellevel relative level, rellevel==0 means show no lower elements
	 * @param parent parent composite for SWT constructor
	 * @param style SWT-style of this nodedisplay
	 */
	protected NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, DisplayNode pnode,
							   NodedisplayView pnodeview, NodedisplayComp pparentNodedisplay,
							   int px, int py,
							   int rellevel, Composite parent, int style){
		
		super(lgui, parent, style);
		
		//Calculate maxlevel with rellevel
		
		//Set Preferences
		calculateCurrentlevel(pnode);
		
		maxLevel=currentLevel+rellevel;
		//Save father reference
		parentNodedisplay=pparentNodedisplay;
		
		//Save grid-positions
		x=px;
		y=py;
		
		init(pmodel,pnode, pnodeview);		
	}
	
	/**
	 * Call this constructor for start, maxlevel is chosen from lml-file
	 * @param lgui wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param pmodel lml-model, which has data for this Nodedisplay
	 * @param pnodeview root-nodedisplay is needed for zooming
	 * @param pnode current node, which is root-data-element of this NodedisplayComp
	 * @param style SWT Style
	 */
	public NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview, int style){
		
		super(lgui, pnodeview.getScrollPane(), style);
		
		//get maxlevel from lml-file
		calculateCurrentlevel(pnode);		
		
		model=pmodel;
		node=pnode;
		
		apref=findLayout();//Searches for corresponding layout-definitions
		
		maxLevel=10;
		if(apref.getMaxlevel()!=null)
			maxLevel=apref.getMaxlevel().intValue();
		
		init(pmodel, pnode, pnodeview);
		
		setScrollBarPreferences();
		
		addPaintListener();
	}
	
	/**
	 * @return lml-data-access to data shown by this nodedisplay
	 */
	public ILguiItem getLguiItem(){
		return lgui;
	}
	
	/**
	 * Adds a listener, which calls showMinRectangleSize everytime
	 * this component is painted. This is needed to change minimum
	 * size in surrounding ScrollPane.
	 */
	private void addPaintListener(){
		
		Listener l=new Listener() {
			
			public void handleEvent(Event event) {
				showMinRectangleSizes();
			}
		};
		
		innerPanel.addListener(SWT.Paint, l);
		
	}
	
	/**
	 * Forward call to calculate desired minimum size to
	 * all inner components. If this nodedisplay paints rectangles
	 * itself, this method calls the calculateResize-function of
	 * the rectangle-painting-listener.
	 */
	private void callMinSizeCalculation(){
		
		Point size=getSize();
		setMinSize(size.x, size.y);
		
		for(NodedisplayComp icomp: innerComps){
			if(icomp.x==0 || icomp.y==0){
				icomp.callMinSizeCalculation();
			}
		}
		
		if(rectpaintlistener != null){
			rectpaintlistener.calculateResize();
		}
		
	}
	
	/**
	 * Calculate minimum size of this nodedisplay, which
	 * is needed to show all painted rectangles in defined
	 * minimum size.
	 * 
	 */
	public void showMinRectangleSizes(){		
		callMinSizeCalculation();
		
		nodeview.getScrollPane().setMinSize(minWidth, minHeight);
	}
	
	/**
	 * Set default preferences for scrollpane.
	 * This function is only used for root-NodedisplayComposites.
	 */
	private void setScrollBarPreferences(){
		ScrolledComposite sc=nodeview.getScrollPane();
		sc.setContent(this);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
	}
	
	/**
	 * Set minimum size for this nodedisplay.
	 * At least this size should be used to display it.
	 * Use this function to initialize minWidth and minHeight.
	 * No forwarding to parent components.
	 * 
	 * @param width minimum width
	 * @param height minimum height
	 */
	private void setMinSize(int width, int height){
		setMinWidth(width);
		setMinHeight(height);
	}
	
	/**
	 * Set minimum width for this nodedisplay.
	 * At least this width should be used to display it.
	 * No forwarding to parent components.
	 * 
	 * @param width  minimum width
	 */
	private synchronized void setMinWidth(int width){
		minWidth=width;
	}
	
	/**
	 * Add width-parameter to minWidth
	 * 
	 * @param width difference to be added
	 */
	public synchronized void increaseMinWidth(int width){
		minWidth+=width;
		
		if(parentNodedisplay != null){
			//Only first row sends minwidth-increases to parent composites
			if(y==0)
				parentNodedisplay.increaseMinWidth(width);
		}
	}
	
	/**
	 * Set minimum height for this nodedisplay.
	 * At least this height should be used to display it.
	 * No forwarding to parent components.
	 * 
	 * @param height minimum height
	 */
	private synchronized void setMinHeight(int height){
		minHeight=height;
	}
	
	/**
	 * Add height-parameter to minHeight
	 * 
	 * @param height difference to be added
	 */
	public synchronized void increaseMinHeight(int height){
		minHeight+=height;
		
		if(parentNodedisplay != null){
			//Only first column sends minheight-increases to parent composites
			if(x==0)
				parentNodedisplay.increaseMinHeight(height);
		}
	}
	
	/**
	 * @return default GridData instance, which simulates Swing-behaviour of gridlayout
	 */
	public static GridData getDefaultGridData(){
		GridData griddata=new GridData();
		griddata.grabExcessHorizontalSpace = true;
		griddata.grabExcessVerticalSpace = true;
		griddata.horizontalAlignment = GridData.FILL;
		griddata.verticalAlignment = GridData.FILL;
		
		return griddata;
		
	}
	
	/**
	 * Calculates current level and saves it to currentlevel
	 * @param pnode
	 */
	private void calculateCurrentlevel(DisplayNode pnode){
		currentLevel=0;
		if(pnode!=null){
			currentLevel=pnode.getLevel();
		}
	}
	
	/**
	 * @return implicit name of node within nodedisplay, which is shown by this NodedisplayPanel
	 */
	public String getShownImpname(){
		if(node==null)
			return null;
		
		return node.getFullImplicitName();
	}
	
	/**
	 * Part of constructor, which is equal in two constructors
	 * therefore outsourced
	 * @param pmodel
	 */
	private void init(Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview){
		
		nodeview=pnodeview;
		
		setLayout(new FillLayout());
		
		innerComps=new ArrayList<NodedisplayComp>();
		//Do extra stuff when disposing
		//Remove this nodedisplay and its children from Objectstatus
		this.addDisposeListener(new DisposeListener() {

	         public void widgetDisposed(DisposeEvent e) {
	        	 removeUpdatable();
	        	 
	        	 if(titleLabel!=null)
	        		 titleLabel.dispose();
	         }

	     });
		
		
		//Transfer parameters
		node=pnode;
		model=pmodel;
		if(apref==null)
			apref=findLayout();//Searches for corresponding layout-definitions
		
		//Check if maxlevel is bigger than deepest possible level within this scheme-part
		if(node!=null){
			//calculation of absolute maximal level within this part of tree
			//realmax=current node level + deepest possible level
			int realmax=LMLCheck.getSchemeLevel(node.getScheme()) + LMLCheck.getDeepestSchemeLevel(node.getScheme() ) - 1 ;
			if(realmax < maxLevel){
				maxLevel=realmax;
			}
		}
		
		backgroundColor=ColorConversion.getColor( LMLColor.stringToColor(apref.getBackground()) );
		
		pictureFrame=new Composite(this, SWT.None);
		pictureFrame.setLayout(new BorderLayout());
		//Redo layout when resized
		pictureFrame.addListener(SWT.Resize, new Listener(){

			public void handleEvent(Event event) {
				pictureFrame.layout(true);
			}
			  
		  });
		
		insertPictures();
		
//		if(centerpic==null)
//			mainpanel=new JPanel();
//		else{
//			ImagePanel picturepanel=new ImagePanel(centerpic.getPic(), centerpic.getPercentWidth(), centerpic.getPercentHeight());
//			picturepanel.setAlign(centerpic.getAlign());			
//			
//			mainpanel=picturepanel;
//		}
		
		mainPanel=new Composite(pictureFrame, SWT.None);
		
		if(node!=null)
			jobColor=ColorConversion.getColor( lgui.getOIDToObject().getColorById(node.getData().getOid()) );
		else jobColor=ColorConversion.getColor( lgui.getOIDToObject().getColorById(null) );
			
		fontObject=this.getDisplay().getSystemFont();
		
		//Create output
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setLayoutData(new BorderData(BorderLayout.MFIELD));
		
		//Insert title if needed
		if(node!=null)
			title=apref.isShowfulltitle()?node.getFullImplicitName():node.getImplicitName();
		else 
			title= lgui.getNodedisplayAccess().getNodedisplayTitel(0);
		
		//Get an imageicon for showing power load
		ImageIcon icon=getPowerLoadImageIcon();
		
		if(apref.isShowtitle()){
			titleBackgroundColor=ColorConversion.getColor( LMLColor.stringToColor(apref.getTitlebackground()) );
			
			titleLabel=new Label(mainPanel, SWT.None);
			titleLabel.setText(title);
			titleLabel.setFont(fontObject);
			titleLabel.setBackground(titleBackgroundColor);
			titleLabel.setLayoutData(new BorderData(BorderLayout.NFIELD));
			
			addZoomFunction();
			
//			titlelabel.setOpaque(true);

//			if(icon!=null){
//				titlelabel.setImage(icon.getImage());
//			}
			
			
		}
		
		innerPanel=null;
		
		if(node!=null)
			insertInnerPanel();
		else
			insertInnerPanel(0, lgui.getNodedisplayAccess().getNodedisplayScheme(0), lgui.getNodedisplayAccess().getNodedisplayData(0), new ArrayList<Integer>());
		
		lgui.getObjectStatus().addComponent(this);
		
		addListener();
		
		pictureFrame.setBackground(backgroundColor);

	}
	
	/**
	 * Remove recursively this component and all inner NodedisplayComp-instances
	 * from ObjectStatus. Call this function before this Composite is disposed
	 */
	private void removeUpdatable(){
		
		lgui.getObjectStatus().removeComponent(this);
		
		for(NodedisplayComp nd:innerComps){
			nd.removeUpdatable();
		}
	}
	
	/**
	 * Add Listener to titlelabel, which allow to zoom in and zoom out
	 */
	private void addZoomFunction(){
		
		//Zoom in by clicking on title-panels
		titleLabel.addMouseListener(new MouseListener() {
			
			public void mouseUp(MouseEvent e) {
				
			}
			
			public void mouseDown(MouseEvent e) {
				
				titleLabel.setBackground(titleBackgroundColor);
				
				if(node==null) return;
				
				if(nodeview.getRootNodedisplay()==NodedisplayComp.this){
					nodeview.zoomOut();
				}
				else nodeview.zoomIn(node.getFullImplicitName());
				
			}
			
			public void mouseDoubleClick(MouseEvent e) {
				
			}
		});
		//Show different background if titlelabel is covered by the mouse
		titleLabel.addMouseMoveListener(new MouseMoveListener() {
			
			public void mouseMove(MouseEvent e) {
				titleLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
			}
		});
		
		titleLabel.addListener(SWT.MouseExit, new Listener(){

			public void handleEvent(Event event) {
				titleLabel.setBackground(titleBackgroundColor);
			}
		});
		
	}
	
	/**
	 * Create an icon which displayes power load
	 * @return ImageIcon for placing into a title-label
	 */
	private ImageIcon getPowerLoadImageIcon(){
		
		ImageIcon icon=null;
		
		if(lgui.getOIDToObject().getObjectById(title) != null){
			//Show power usage
			double curpower=0;//current power usage in percent
			double maxpower=1;//maximal power usage
			
			//get power information from info-tags
			List<InfoType> infos=lgui.getOIDToInformation().getInfosById(title);
			
			if(infos!=null){				
				for(InfoType inftype: infos){//<info>
					List<InfodataType> data=inftype.getData();
					for(InfodataType date:data){//<data>
						if( date.getKey().equals("maxpower") ){//<data key="maxpower" value="100"/>
							maxpower=Double.parseDouble(date.getValue());
						}
						else if(date.getKey().equals("curpower")){
							curpower=Double.parseDouble(date.getValue());
						}
					}
				}

				//Create Buffered Image filled with a pie-chart
//				PieChart pc=new PieChart(PieChart.anydataToPercentValues(new double[]{maxpower-curpower, curpower}),
//						new Color[]{Color.gray, getColorForPowerPie(curpower/maxpower)}, null);
//
//				BufferedImage bufImage =  new BufferedImage(iconwidth,iconwidth,BufferedImage.TYPE_INT_ARGB );
//				Graphics g =  bufImage.getGraphics();
//
//				pc.paint(g, new Dimension(iconwidth,iconwidth));
//
//				icon=new ImageIcon(bufImage);
			}
		}
		
		return icon;
	}
	
	//Some colors for pie charts presenting power usage
	private static Color lowcolor, middlecolor, highcolor;
	
	/**
	 * 
	 * @param percent power usage in percent
	 * @return Color for usage within pie chart, for fast overview of power load
	 */
	private static Color getColorForPowerPie(double percent){
		
		if(lowcolor==null){
			lowcolor=Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
			middlecolor=Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
			highcolor=Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		}
		
		if(percent<0.3)
			return lowcolor;
		
		if(percent<0.6)
			return middlecolor;
		
		return highcolor;
	}
	
	/**
	 * Search for pictures in layout-definition and add them at right position
	 */
	private void insertPictures(){
		
		pictures=new ArrayList<ImageComp>();
		
		//insert pictures
		List<PictureType> lmlpictures=apref.getImg();
		for(PictureType picture:lmlpictures){
			
			URL aurl=null;
			
			try{
				//Get picture from url and put it into a panel
				try {
					URL httpURL=new URL(picture.getSrc());
					aurl = new URL( "https", httpURL.getHost(), httpsport, httpURL.getFile() );
				} catch (MalformedURLException e) {
					continue;
				}
			}
			catch(AccessControlException er){
				aurl=null;
			}
			
			if(aurl==null) continue;
			
			ImageComp icomp;
			try {
				icomp = new ImageComp(pictureFrame, SWT.None, aurl,picture.getWidth(), picture.getHeight());
			} catch (IOException e) {
				continue;
			}
			
			if(node==null)
				icomp.setBackground(backgroundColor);
			else icomp.setBackground(this.getParent().getBackground());
			
			//Set inner alignment
//			ipan.setAlign(picture.getInneralign());
			
			if( picture.getAlign()==AlignType.CENTER )//Add borderpics to pictures and save center-pic in centerpic-variable
				centerPic=icomp.getImage();
			else pictures.add(icomp);
			
			switch(picture.getAlign()){
			case WEST: icomp.setLayoutData(new BorderData(BorderLayout.WFIELD));break;
			case EAST: icomp.setLayoutData(new BorderData(BorderLayout.EFIELD));break;
			case NORTH: icomp.setLayoutData(new BorderData(BorderLayout.NFIELD));break;
			case SOUTH: icomp.setLayoutData(new BorderData(BorderLayout.SFIELD));break;
			}			
		}
	}
	
	/**
	 * Search for a layout-section for this nodedisplay-panel
	 * @return lml-Nodedisplay-layout-section for this displaynode, or default-layout if no layout is defined
	 */
	public Nodedisplayelement findLayout(){
		
		ArrayList<NodedisplaylayoutType> allnodedisplaylayouts=lgui.getNodedisplayAccess().getLayouts(model.getId());
		
		//Is there any Layout for this nodedisplay?
		if( allnodedisplaylayouts==null || allnodedisplaylayouts.size()==0 )
			return NodedisplayAccess.getDefaultLayout();
		//Later it should be possible to choose one of possibly multiple defined layouts
		NodedisplaylayoutType first=allnodedisplaylayouts.get(0);
		
		if(node==null){//Root-level => return el0-Nodedisplayelement
			if(first.getEl0() != null){
				return first.getEl0();
			}
			else return NodedisplayAccess.getDefaultLayout();
		}
		
		if(first.getEl0()==null) return NodedisplayAccess.getDefaultLayout();
		
		//Copy level-numbers
		ArrayList<Integer> levelnrs=LMLCheck.copyArrayList(node.getLevelNrs());
		
		//deeper-level => traverse layout-tree
		Nodedisplayelement res=LMLCheck.getNodedisplayElementByLevels(levelnrs , first.getEl0());
		
		if(res==null) return NodedisplayAccess.getDefaultLayout();
		else return res;
		
	}
	
	/**
	 * Add listener which react when user focuses this panel
	 * This method is not needed, if elements on the lowest level are painted with rects
	 * instead of painting composites.
	 */
	private void addListener(){
		
		if(currentLevel==maxLevel){//Only insert listeners for the lowest level-composites, which are painted as rectangles
			
			MouseMoveListener mousemove=new MouseMoveListener() {

				public void mouseMove(MouseEvent e) {
					if(node!=null){
						lgui.getObjectStatus().mouseover(node.getConnectedObject());
					}
				}
			};
			
			MouseListener mouselistener=new MouseListener() {
				
				public void mouseUp(MouseEvent e) {
					if(e.x>=0 && e.x<=getSize().x &&
							e.y>=0 && e.y<=getSize().y ){
						if(node!=null)
							lgui.getObjectStatus().mouseup(node.getConnectedObject());					
					}
				}
				
				public void mouseDown(MouseEvent e) {
					if(node!=null)
						lgui.getObjectStatus().mousedown(node.getConnectedObject());
				}
				
				public void mouseDoubleClick(MouseEvent e) {
					
				}
			};
			
			Listener mouseexit=new Listener(){

				public void handleEvent(Event event) {
					lgui.getObjectStatus().mouseExitLast();
				}
				
			};
			
			borderFrame.addMouseMoveListener(mousemove);
			borderFrame.addMouseListener(mouselistener);
			borderFrame.addListener(SWT.MouseExit, mouseexit);
			
			innerPanel.addMouseMoveListener(mousemove);
			innerPanel.addMouseListener(mouselistener);
			innerPanel.addListener(SWT.MouseExit, mouseexit);
		}
	}
	
	/**
	 * Find maximum count of elements per level
	 * Traverses the whole scheme
	 * @param scheme lml-scheme
	 * @param level current level (first is 1)
	 * @param maxcounts array with length at least scheme.depth 
	 */
	private static void findMaximum(Object scheme, int level, int[] maxcounts){
		
		List subschemes=LMLCheck.getLowerSchemeElements(scheme);
		
		int sum=0;
		
		for(Object subscheme: subschemes){
			
			SchemeElement sub=(SchemeElement)subscheme;
			
			if(sub.getList()!=null){//list-attribute
				sum+=LMLCheck.getNumbersFromNumberlist(sub.getList()).length;
			}
			else{//min- max-attributes
				
				int min=sub.getMin().intValue();
				int max=min;
				if(sub.getMax()!=null){
					max=sub.getMax().intValue();
				}

				int step=sub.getStep().intValue();
				
				sum+=(max-min)/step+1;
			}
			//Recursive call for lower level
			findMaximum(sub, level+1, maxcounts);
		}
		
		if(sum>0 && sum > maxcounts[level-1]){
			maxcounts[level-1]=sum;
		}
		
	}
	
	
	/**
	 * Parses through scheme and generates suitable preferences
	 * for every level of the nodedisplay-tree
	 * @param scheme scheme of nodedisplay
	 * @return preferences for every level
	 */
	public static ArrayList<Nodedisplayelement> generatePrefsFromScheme(SchemeType scheme){
		int depth=LMLCheck.getDeepestSchemeLevel(scheme);
		
		ArrayList<Nodedisplayelement> res=new ArrayList<Nodedisplayelement>();
		ObjectFactory objf=new ObjectFactory();//For creating lml-objects
	
		int[] maxcounts=new int[depth];
		for(int i=0; i<maxcounts.length; i++){
			maxcounts[i]=0;
		}
		//Search for maximum amount of elements defined in scheme
		findMaximum(scheme, 1, maxcounts);
		
		for(int i=0; i<depth; i++){
			
			int cols=8;
			if(maxcounts[i] < cols){
				cols=maxcounts[i];
			}
			
			Nodedisplayelement apref=objf.createNodedisplayelement();
			apref.setCols(BigInteger.valueOf(cols));
			
			//Only level 1 shows title
			if(i!=1) apref.setShowtitle(false);
			//bigger Level-0 gap between level1-panels
			if(i==0){
				apref.setHgap(BigInteger.valueOf(3));
				apref.setHgap(BigInteger.valueOf(4));
			}
			
			res.add(apref);
		}
		
		//For last level, which does not have inner elements
		res.add(NodedisplayAccess.getDefaultLayout() );
		
		return res;
	}
	
	/**
	 * Generates DisplayNodes for every visible component, which
	 * is a child of the element identified by the level-ids.
	 * @param ascheme current lml-scheme describing a node 
	 * @param adata current lml-data for this node
	 * @param levels ids for every level to identify a node in the lml-tree (1,1,1) means first cpu in first nodecard in first row
	 * @param model full LML-Model for a nodedisplay
	 * @param highestRowfirst defines how the result will be sorted, true=> descending, false ascending
	 * @return list of DisplayNodes
	 */
	public ArrayList<DisplayNode> getLowerDisplayNodes( Object ascheme, Object adata, ArrayList<Integer> levels, Nodedisplay model, boolean highestRowfirst ){
		
		int alevel=levels.size();
		ArrayList<DisplayNode> res=new ArrayList<DisplayNode>();
		
		HashMap<Integer, SchemeElement> schemesForIds=new HashMap<Integer, SchemeElement>();
		
		ArrayList<Integer> numbers=new ArrayList<Integer>();//Indices within scheme of lower elements
		List lscheme=LMLCheck.getLowerSchemeElements(ascheme);
		for(int i=0; i<lscheme.size(); i++){

			//getNumbers
			SchemeElement lascheme=(SchemeElement)lscheme.get(i);
			if(lascheme.getList()!=null){//get list-elements

				int[] anumbers=LMLCheck.getNumbersFromNumberlist(lascheme.getList());
				for(int j=0; j<anumbers.length; j++){
					numbers.add(anumbers[j]);
					schemesForIds.put(anumbers[j], lascheme);
				}

			}
			else{//min- max-attributes

				int min=lascheme.getMin().intValue();
				int max=min;
				if(lascheme.getMax()!=null){
					max=lascheme.getMax().intValue();
				}

				int step=lascheme.getStep().intValue();

				for(int j=min; j<=max; j+=step){
					numbers.add(j);
					schemesForIds.put(j, lascheme);
				}

			}
		}
		

		Collections.sort(numbers, new NumberComparator(! highestRowfirst));

		//Process numbers
		for(int j=0; j<numbers.size(); j++){
			int aid=numbers.get(j);
			//Get all information for new DisplayNode
			SchemeAndData schemedata=LMLCheck.getSchemeAndDataByLevels(aid, adata, ascheme);

			DataElement lowdata=schemedata.data;

			if(LMLCheck.getDataLevel(adata) < LMLCheck.getSchemeLevel(ascheme) //Was adata already not at the right level (inheritance of attributes) ?
					&& adata instanceof DataElement){
				lowdata=(DataElement)adata;//Then do not go deeper , makes no sense
			}

			levels.add(aid);

			DisplayNode dispnode=new DisplayNode(lgui, lgui.getNodedisplayAccess().getTagname(model.getId(), alevel+1),
					lowdata, schemesForIds.get(aid), levels, model );

			levels.remove(levels.size()-1);

			res.add(dispnode);			
		}
		
		return res;
		
	}
	
	/**
	 * Generates a hashmap, which connects DisplayNodes to their SWT-colors.
	 * 
	 * @param dispnodes The DisplayNodes, which are keys from the resulting hashmap
	 * @return hashmap, which connects DisplayNodes to their SWT-colors
	 */
	public HashMap<DisplayNode, Color> generateDisplayNodeToColorMap(ArrayList<DisplayNode> dispnodes){
		
		OIDToObject oidtoobj=lgui.getOIDToObject();
		
		HashMap<DisplayNode, Color> dispnodetocolor=new HashMap<DisplayNode, Color>();
		for(DisplayNode dispnode:dispnodes){
			String conOID=dispnode.getData().getOid();
			dispnodetocolor.put(dispnode, 
								ColorConversion.getColor( oidtoobj.getColorById( conOID ) ) );
		}
		
		return dispnodetocolor;		
	}
	
	/**
	 * @return 1 if layout defines 0 for cols, otherwise the value set by the layout
	 */
	private int getCols(){
		if(apref.getCols().intValue()==0){
			return 1;
		}
		else return apref.getCols().intValue();
	}
	
	/**
	 * Call this function if displaynode != null
	 */
	private void insertInnerPanel(){
		insertInnerPanel(node.getLevel(), node.getScheme(), node.getData(), node.getLevelNrs());
	}
	
	
	private void createFramePanels( Color bordercolor ){
		
		//At least insert one panel with backgroundcolor as bordercomposite
		borderFrame=new BorderComposite(mainPanel, SWT.NONE);
		
		borderFrame.setBorderColor( bordercolor );
		borderFrame.setBorderWidth(apref.getBorder().intValue());
		borderFrame.setLayoutData(new BorderData(BorderLayout.MFIELD));
		
		innerPanel=new Composite(borderFrame, SWT.NONE);
		
		innerPanel.setBackground(backgroundColor);
		
		if(centerPic!=null){
			innerPanel.setBackgroundImage(centerPic);
		}
		
//		if( (apref.isHighestrowfirst() && !apref.isHighestcolfirst()) || (!apref.isHighestrowfirst() && apref.isHighestcolfirst()) ){
//				innerpanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
//		}
		
	}
	
	private void initRectPaintListener(ArrayList<DisplayNode> dispnodes, Color bordercolor){
		rectpaintlistener= new RectPaintListener(dispnodes, apref.getCols().intValue(), this, innerPanel);
		rectpaintlistener.horizontalSpacing=apref.getHgap().intValue();
		rectpaintlistener.verticalSpacing=apref.getVgap().intValue();
		
		rectpaintlistener.normalborder=apref.getBorder().intValue();
		rectpaintlistener.mouseborder=apref.getMouseborder().intValue();
		
		rectpaintlistener.bordercolor=bordercolor;
		
		//Add all listeners to the innerpanel now
		innerPanel.addListener(SWT.Paint, rectpaintlistener);
	}
	
	private void addMouseListenerToInnerPanel(){

		innerPanel.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {

				DisplayNode focussed=rectpaintlistener.getDisplayNodeAtPos(e.x, e.y);

				if(focussed!=null){
					lgui.getObjectStatus().mouseover(focussed.getConnectedObject());
				}
				else
					lgui.getObjectStatus().mouseExitLast();

			}
		});

		innerPanel.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
				DisplayNode focussed=rectpaintlistener.getDisplayNodeAtPos(e.x, e.y);

				if(focussed!=null)
					lgui.getObjectStatus().mouseup(focussed.getConnectedObject());
			}

			public void mouseDown(MouseEvent e) {
				DisplayNode focussed=rectpaintlistener.getDisplayNodeAtPos(e.x, e.y);

				if(focussed!=null)
					lgui.getObjectStatus().mousedown(focussed.getConnectedObject());
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		innerPanel.addListener(SWT.MouseExit, new Listener(){

			public void handleEvent(Event event) {
				lgui.getObjectStatus().mouseExitLast();
			}

		});

	}
	
	/**
	 * If inner panels exist and maxlevel is greater than alevel, then insert lower level-panels
	 */
	private void insertInnerPanel(int alevel, Object ascheme, Object adata, ArrayList<Integer> levels){

		//Bordercolor is defined by this parameter
		Color bordercolor=ColorConversion.getColor( LMLColor.stringToColor(apref.getBordercolor()) );
		
		createFramePanels(bordercolor);
		
		//Generate all displaynodes for elements in lml-tree which are childs of the current node
		ArrayList<DisplayNode> dispnodes=getLowerDisplayNodes(ascheme, adata, levels, model, apref.isHighestrowfirst());
		
		
		if(alevel==maxLevel-1){//Paint rects instead of use composites for lowest-level-rectangles
			
			initRectPaintListener(dispnodes, bordercolor);
			
			addMouseListenerToInnerPanel();
			
		}		
		else{ //insert lower nodedisplays, nest composites
			
			//Set Gridlayout only if needed
			int cols=apref.getCols().intValue();
			GridLayout layout=new GridLayout(cols, true);
			layout.horizontalSpacing=apref.getHgap().intValue();
			layout.verticalSpacing=apref.getVgap().intValue();
			
			layout.marginWidth=1;
			layout.marginHeight=1;
			
			innerPanel.setLayout(layout);
			
			int index=0;
			
			for(DisplayNode dispnode: dispnodes){
				
				NodedisplayComp ainner = new NodedisplayComp(lgui, model, dispnode,
															  nodeview, this,
															  index % cols, index / cols,
															  maxLevel-currentLevel-1, innerPanel, SWT.NONE);
				innerComps.add(ainner);

				if(gridData==null){
					gridData = getDefaultGridData();
				}

				ainner.setLayoutData(gridData);
				
				index++;
			}
		}
		
//		innerpanel.setOpaque(false);
		
//		mainpanel.add(innerpanel, BorderLayout.CENTER);
	}
	
	/**
	 * Show title or name for this panel
	 */
	public void showTitle(){
		titleLabel.setVisible(true);
		apref.setShowtitle(true);
	}
	
	/**
	 * Hide title or name for this panel
	 */
	public void hideTitle(){
		titleLabel.setVisible(false);
		apref.setShowtitle(false);
	}
	
	public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown) {
		
		if(node==null){
			if(innerPanel != null){
				innerPanel.redraw();
			}
			return;
		}
		
		ObjectType conobject=node.getConnectedObject();
		if(currentLevel==maxLevel){
			
			if(lgui.getObjectStatus().isMouseover(conobject)){
				borderFrame.setBorderWidth(apref.getMouseborder().intValue());
			}
			else borderFrame.setBorderWidth(apref.getBorder().intValue());
			
			if(lgui.getObjectStatus().isAnyMousedown() && !lgui.getObjectStatus().isMousedown(conobject)){//Change color
				innerPanel.setBackground( ColorConversion.getColor( lgui.getOIDToObject().getColorById(null) ) );
			}
			else innerPanel.setBackground(jobColor);
		}
		else if(currentLevel==maxLevel-1){//For rectangle-paint of lowest-level-elements
			innerPanel.redraw();
		}
	}
	
}