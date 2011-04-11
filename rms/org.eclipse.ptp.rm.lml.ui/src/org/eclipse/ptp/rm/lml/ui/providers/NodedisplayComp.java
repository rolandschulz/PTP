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
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus.Updatable;
import org.eclipse.ptp.rm.lml.internal.core.nodedisplay.LMLCheck;
import org.eclipse.ptp.rm.lml.internal.core.nodedisplay.LMLCheck.SchemeAndData;
import org.eclipse.ptp.rm.lml.ui.views.NodedisplayView;
import org.eclipse.ptp.rm.lml.ui.providers.BorderLayout;
import org.eclipse.ptp.rm.lml.ui.providers.BorderLayout.BorderData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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

 */
public class NodedisplayComp extends LMLWidget implements Updatable{ 

	private NodedisplayView nodeview;//parent composite for zooming
	
	//Parameters which are not in apref yet
	private static int iconwidth=15;
	
	private String title;//implicit name of this node
	private Color jobcolor;//Color of the job, to which this panel is connected
	
	private Font fontobject;
	private Nodedisplay model;//surrounding lml-Nodedisplay
	
	private DisplayNode node;//Node-model which has to be displayed
	private int maxlevel; //Deepest level, which should be displayed
	
	private Composite pictureframe;//this panel contains pictures as direct children and the mainpanel in center 
	private Composite mainpanel;//Important panel where everything but pictures is in
	
	private Composite innerpanel;
	private Label titlelabel;//For title-line
	
	//Settings for lower level
	private Nodedisplayelement apref;
	
	//current level, which is displayed
	private int currentlevel;
	
	private GridData gridData;//layout definitions for gridlayouts, makes inner panels resize
	
	private Color backgroundcolor;//Current backgroundcolor
	private Color titlebackgroundcolor;
	
	//Borders
	private BorderComposite borderframe;//The only frame which has a border, borderwidth changes when mouse touches the panel
	
	private ArrayList<ImageComp> pictures;
	private Image centerpic;//Picture in center is special
	
	private ArrayList<NodedisplayComp> innercomps;//Save created inner NodedisplayComp-instances for disposing them if needed
	
	private static final int httpsport=4444;//Special port for https-access
	
	/**
	 * Class for comparing integer-values in ascending or descending way.
	 *
	 */
	public static class NumberComparator implements Comparator<Integer>{

		private boolean asc;//if true => sort ascending, otherwise descending
		
		public NumberComparator(boolean ascending){
			asc=ascending;
		}
		
		public int compare(Integer o1, Integer o2) {
			if(asc){
				return o1-o2;
			}
			else return o2-o1;
		}
		
	}
	
	public NodedisplayComp(ILguiItem lguiItem, Nodedisplay pmodel, NodedisplayView pnodeview, int style){

		this(lguiItem, pmodel, null, pnodeview, style);
		
	}
	
	public NodedisplayComp(ILguiItem lguiItem, Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview, int rellevel, Composite parent, int style){
		
		super(lguiItem, parent, style);
		
		//Calculate maxlevel with rellevel
		
		//Set Preferences
		calculateCurrentlevel(pnode);
		
		maxlevel=currentlevel+rellevel;
		
		init(pmodel,pnode, pnodeview);		
	}
	
	public NodedisplayComp(ILguiItem lguiItem, Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview, int style){
		
		super(lguiItem, pnodeview, style);
		
		//get maxlevel from lml-file
		calculateCurrentlevel(pnode);		
		
		model=pmodel;
		node=pnode;
		
		apref=findLayout();//Searches for corresponding layout-definitions
		
		maxlevel=10;
		if(apref.getMaxlevel()!=null)
			maxlevel=apref.getMaxlevel().intValue();
		
		init(pmodel, pnode, pnodeview);
	}
	
	/**
	 * Calculates current level and saves it to currentlevel
	 * @param pnode
	 */
	private void calculateCurrentlevel(DisplayNode pnode){
		currentlevel=0;
		if(pnode!=null){
			currentlevel=pnode.getLevel();
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
		
		innercomps=new ArrayList<NodedisplayComp>();
		//Do extra stuff when disposing
		this.addDisposeListener(new DisposeListener() {

	         public void widgetDisposed(DisposeEvent e) {
	        	 removeUpdatable();
	        	 
	        	 if(titlelabel!=null)
	        		 titlelabel.dispose();
	         }

	     });
		
		
		//Transfer parameters
		node=pnode;
		model=pmodel;
		if(apref==null)
			apref=findLayout();//Searches for corresponding layout-definitions
		
//		if(apref.getMaxlevel() != null){//Overwrite maxlevel-definition from upper levels, if maxlevel is defined
//			maxlevel=apref.getMaxlevel().intValue();
//		}
		
		//Check if maxlevel is bigger than deepest possible level within this scheme-part
		if(node!=null){
			//calculation of absolute maximal level within this part of tree
			//realmax=current node level + deepest possible level
			int realmax=LMLCheck.getSchemeLevel(node.getScheme()) + LMLCheck.getDeepestSchemeLevel(node.getScheme() ) - 1 ;
			if(realmax < maxlevel){
				maxlevel=realmax;
			}
		}
		
		backgroundcolor=ColorConversion.getColor( LMLColor.stringToColor(apref.getBackground()) );
		
		pictureframe=new Composite(this, SWT.None);
		pictureframe.setLayout(new BorderLayout());
		//Redo layout when resized
		pictureframe.addListener(SWT.Resize, new Listener(){

			public void handleEvent(Event event) {
				pictureframe.layout(true);
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
		
		mainpanel=new Composite(pictureframe, SWT.None);
		
		if(node!=null)
			jobcolor=ColorConversion.getColor( lguiItem.getOIDToObject().getColorById(node.getData().getOid()) );
		else jobcolor=ColorConversion.getColor( lguiItem.getOIDToObject().getColorById(null) );
			
		fontobject=this.getDisplay().getSystemFont();
		
		//Create output
		mainpanel.setLayout(new BorderLayout());
		mainpanel.setLayoutData( BorderData.CENTER );
		
		//Insert title if needed
		if(node!=null)
			title=apref.isShowfulltitle()?node.getFullImplicitName():node.getImplicitName();
		else title=model.getTitle();
		
		//Get an imageicon for showing power load
		ImageIcon icon=getPowerLoadImageIcon();
		
		if(apref.isShowtitle()){
			
			titlebackgroundcolor=ColorConversion.getColor( LMLColor.stringToColor(apref.getTitlebackground()) );
			
			titlelabel=new Label(mainpanel, SWT.None);
			titlelabel.setText(title);
			titlelabel.setFont(fontobject);
			titlelabel.setBackground(titlebackgroundcolor);
			titlelabel.setLayoutData(BorderData.NORTH);
			
			addZoomFunction();
			
//			titlelabel.setOpaque(true);

//			if(icon!=null){
//				titlelabel.setImage(icon.getImage());
//			}
			
			
		}
		
		innerpanel=null;
		
		if(node!=null)
			insertInnerPanel();
		else
			insertInnerPanel(0, model.getScheme(), model.getData(), new ArrayList<Integer>());
		
		lguiItem.getObjectStatus().addComponent(this);
		
		addListener();
		
		pictureframe.setBackground(backgroundcolor);

	}
	
	/**
	 * Remove recursively this component and all inner NodedisplayComp-instances
	 * from ObjectStatus. Call this function before this Composite is disposed
	 */
	private void removeUpdatable(){
		
		lguiItem.getObjectStatus().removeComponent(this);
		
		for(NodedisplayComp nd:innercomps){
			nd.removeUpdatable();
		}
	}
	
	/**
	 * Add Listener to titlelabel, which allow to zoom in and zoom out
	 */
	private void addZoomFunction(){
		
		//Zoom in by clicking on title-panels
		titlelabel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				
				titlelabel.setBackground(titlebackgroundcolor);
				
				if(node==null) return;
				
				if(nodeview.getRootNodedisplay()==NodedisplayComp.this){
					nodeview.zoomOut();
				}
				else nodeview.zoomIn(node.getFullImplicitName());
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		titlelabel.addMouseMoveListener(new MouseMoveListener() {
			
			public void mouseMove(MouseEvent e) {
				titlelabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
			}
		});
		
		titlelabel.addListener(SWT.MouseExit, new Listener(){

			public void handleEvent(Event event) {
				titlelabel.setBackground(titlebackgroundcolor);
			}
		});
		
	}
	
	/**
	 * Create an icon which displayes power load
	 * @return ImageIcon for placing into a title-label
	 */
	private ImageIcon getPowerLoadImageIcon(){
		
		ImageIcon icon=null;
		
		if(lguiItem.getOIDToObject().getObjectById(title) != null){
			//Show power usage
			double curpower=0;//current power usage in percent
			double maxpower=1;//maximal power usage
			
			//get power information from info-tags
			List<InfoType> infos=lguiItem.getOIDToInformation().getInfosById(title);
			
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
	private static Color lowcolor=Display.getCurrent().getSystemColor(SWT.COLOR_GREEN), middlecolor=Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), highcolor=Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	
	/**
	 * 
	 * @param percent power usage in percent
	 * @return Color for usage within pie chart, for fast overview of power load
	 */
	private static Color getColorForPowerPie(double percent){
		
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
				icomp = new ImageComp(pictureframe, SWT.None, aurl,picture.getWidth(), picture.getHeight());
			} catch (IOException e) {
				continue;
			}
			
			if(node==null)
				icomp.setBackground(backgroundcolor);
			else icomp.setBackground(this.getParent().getBackground());
			
			//Set inner alignment
//			ipan.setAlign(picture.getInneralign());
			
			if( picture.getAlign()==AlignType.CENTER )//Add borderpics to pictures and save center-pic in centerpic-variable
				centerpic=icomp.getImage();
			else pictures.add(icomp);
			
			switch(picture.getAlign()){
			case WEST: icomp.setLayoutData(BorderData.WEST);break;
			case EAST: icomp.setLayoutData(BorderData.EAST);break;
			case NORTH: icomp.setLayoutData(BorderData.NORTH);break;
			case SOUTH: icomp.setLayoutData(BorderData.SOUTH);break;
			}			
		}
	}
	
	/**
	 * Search for a layout-section for this nodedisplay-panel
	 * @return lml-Nodedisplay-layout-section for this displaynode, or default-layout if no layout is defined
	 */
	public Nodedisplayelement findLayout(){
		
		ArrayList<NodedisplaylayoutType> allnodedisplaylayouts=lguiItem.getNodedisplayAccess().getLayouts(model.getId());
		
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
	 */
	public void addListener(){
		
		if(currentlevel==maxlevel){
			
			MouseMoveListener mousemove=new MouseMoveListener() {

				public void mouseMove(MouseEvent e) {
					if(node!=null)
						lguiItem.getObjectStatus().mouseover(node.getConnectedObject());
				}
			};
			
			MouseListener mouselistener=new MouseListener() {
				
				public void mouseUp(MouseEvent e) {
					if(e.x>=0 && e.x<=getSize().x &&
							e.y>=0 && e.y<=getSize().y ){
						if(node!=null)
							lguiItem.getObjectStatus().mouseup(node.getConnectedObject());					
					}
				}
				
				@Override
				public void mouseDown(MouseEvent e) {
					if(node!=null)
						lguiItem.getObjectStatus().mousedown(node.getConnectedObject());
				}
				
				public void mouseDoubleClick(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			};
			
			Listener mouseexit=new Listener(){

				public void handleEvent(Event event) {
					lguiItem.getObjectStatus().mouseExitLast();
				}
				
			};
			
			borderframe.addMouseMoveListener(mousemove);
			borderframe.addMouseListener(mouselistener);
			borderframe.addListener(SWT.MouseExit, mouseexit);
			
			innerpanel.addMouseMoveListener(mousemove);
			innerpanel.addMouseListener(mouselistener);
			innerpanel.addListener(SWT.MouseExit, mouseexit);
		}
		
		pictureframe.addListener( SWT.Resize,  new Listener () {
		    public void handleEvent (Event e) {
		    	resizePictures();
		      }
		   });
		
	}
	
	/**
	 * Resize pictures after resize of surrounding Panel
	 */
	public void resizePictures(){
		
		Point asize=getSize();
		
		//Resize all inner panels with pictures in it
//		for(ImagePanel ipan: pictures){
//			ipan.setPreferredSize(new Dimension( (int)Math.round(ipan.getPercentWidth()*asize.width) ,
//					 (int)Math.round(ipan.getPercentHeight()*asize.height )));
//		}
//		//Also resize centerpic
//		if(centerpic!=null){
//			((ImagePanel) mainpanel).setCurrentPictureSize(new Dimension( (int)Math.round(centerpic.getPercentWidth()*asize.width) ,
//					 (int)Math.round(centerpic.getPercentHeight()*asize.height )));
//		}
//		
//		if(pictures.size()>0 || centerpic!=null){
//			updateUI();
//		}
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

			DisplayNode dispnode=new DisplayNode(lguiItem, lguiItem.getNodedisplayAccess().getTagname(model.getId(), alevel+1),
					lowdata, schemesForIds.get(aid), levels, model );

			levels.remove(levels.size()-1);

			res.add(dispnode);			
		}
		
		return res;
		
	}
	
	/**
	 * A listener, which paints dispnodes within a panel. This is used for the lowest level
	 * rectangles. It is faster to paint rects than using a gridlayout and inserting nodedisplay-
	 * composites for every rectangle. Problem is less general layouts (for example: it is more
	 * difficult to react for cursor-focus on these rectangles, texts as titles have to be painted
	 * and cant be inserted by using a layout-manager)
	 *
	 */
	private class RectPaintListener implements Listener{

		private ArrayList<DisplayNode> dispnodes;//nodes which are painted in the composite
		private HashMap<DisplayNode, Color> dispnodetocolor;//map for fast access to displaynode-colors
		private HashMap<DisplayNode, Rectangle> dispnodetorectangle;//map containing positions of dispnodes, they might change in every paint
		private int COLUMNCOUNT;//count of columns in the grid
		private Composite composite;//The composite which is painted by this listener
		
		public Color bordercolor=Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		
		public int marginWidth=1;//space around the grid in x-direction
		public int marginHeight=1;//space around the grid in y-direction
		public int horizontalSpacing=1;//space between two rectangles in x
		public int verticalSpacing=1;//space between two rectangles in y
		
		public int normalborder=0;//Border shown if displaynodes are not focussed
		public int mouseborder=1;//Border shown if displaynodes are focussed
		
		/**
		 * Create the listener, initialize attributes and generate dispnodetocolor-map
		 * @param pdispnodes nodes, which should be painted
		 * @param pcolumncount count of columns in the grid
		 * @param pcomposite composite, to which this listener is added
		 */
		public RectPaintListener( ArrayList<DisplayNode> pdispnodes, int pcolumncount, Composite pcomposite){
			dispnodes=pdispnodes;
			COLUMNCOUNT=pcolumncount;
			composite=pcomposite;
			
			dispnodetocolor=new HashMap<DisplayNode, Color>();
			for(DisplayNode dispnode:dispnodes){
				dispnodetocolor.put(dispnode, ColorConversion.getColor(lguiItem.getOIDToObject().getColorById(dispnode.getData().getOid()) ));
			}
			
			dispnodetorectangle=new HashMap<DisplayNode, Rectangle>();
		}
		
		/**
		 * Pass a relative mouse-position on the composite, which uses this listener.
		 * Then the displaynode on focus will be returned. If no node is focussed
		 * null is returned.
		 * @param px x-position of cursor within the composite
		 * @param py y-position of cursor within the composite
		 * @return focussed DisplayNode or null, if nothing is focussed
		 */
		public DisplayNode getDisplayNodeAtPos(int px, int py){
			
			int x=px-marginWidth;
			int y=py-marginHeight;
			
			if(x<0 || y<0) return null;//Outside grid, left or top
			
			if(rectwidth==0 || rectheight==0) return null;
			
			int col=x/rectwidth;
			int row=y/rectheight;
			
			if( col >= COLUMNCOUNT || row>=rowcount )//Outside grid, right or bottom
				return null;
			
			int index=row*COLUMNCOUNT+col;
			
			if(index>=dispnodes.size())
				return null;
			
			return dispnodes.get(index);			
		}
		
		private int w,h,rowcount,rectwidth,rectheight;//Parameters, which are changed when painting, they can be used by getDisplayNodeAtPos
		
		public void handleEvent(Event event) {
			
			Point size=composite.getSize();
			//Generate available size
			w=size.x-marginWidth*2;
			h=size.y-marginHeight*2;
			
			if(COLUMNCOUNT<=0)
				COLUMNCOUNT=1;
			
			//Calculate how many rows have to be painted
			rowcount=dispnodes.size() / COLUMNCOUNT;
			if(dispnodes.size() % COLUMNCOUNT != 0)
				rowcount++;
			
			if(rowcount==0)
				return;
			
			rectwidth=w/COLUMNCOUNT;
			
			rectheight=h/rowcount;
			
			for(int x=0; x<COLUMNCOUNT; x++){
				
				for(int y=0; y<rowcount; y++){
					//get index of displaynode
					int index=y*COLUMNCOUNT+x;
					if(index>=dispnodes.size())
						break;
					
					//Rectangle frame
					Rectangle r=new Rectangle(marginWidth+rectwidth*x ,  marginHeight+ rectheight*y,
							rectwidth-horizontalSpacing, rectheight-verticalSpacing);
					
					//Paint outer rectangle
					event.gc.setBackground( bordercolor);
					event.gc.fillRectangle( r.x, r.y, r.width, r.height );
					
					DisplayNode dispnode=dispnodes.get(index);
					//Paint it
					if(lguiItem.getObjectStatus().isAnyMousedown() && !lguiItem.getObjectStatus().isMousedown(dispnode.getConnectedObject())){//Change color
						event.gc.setBackground( ColorConversion.getColor( lguiItem.getOIDToObject().getColorById(null) ) );
					}
					else event.gc.setBackground( dispnodetocolor.get(dispnode) );
					
					int border=normalborder;
					
					if( lguiItem.getObjectStatus().isMouseover(dispnode.getConnectedObject()) ){
						border=mouseborder;
					}
					
					event.gc.fillRectangle( r.x+border, r.y+border, r.width-2*border, r.height-2*border );
					
					dispnodetorectangle.put(dispnode, r);//save the current rectangle 
				}
				
			}
		}
		
	}
	
	/**
	 * Call this function if displaynode != null
	 */
	private void insertInnerPanel(){
		insertInnerPanel(node.getLevel(), node.getScheme(), node.getData(), node.getLevelNrs());
	}
	
	/**
	 * If inner panels exist and maxlevel is greater than alevel, then insert lower level-panels
	 */
	private void insertInnerPanel(int alevel, Object ascheme, Object adata, ArrayList<Integer> levels){

		//At least insert one panel with backgroundcolor as bordercomposite
		borderframe=new BorderComposite(mainpanel, SWT.NONE);
		//Bordercolor is defined by this parameter
		Color bordercolor=ColorConversion.getColor( LMLColor.stringToColor(apref.getBordercolor()) );
		
		borderframe.setBorderColor( bordercolor );
		borderframe.setBorderWidth(apref.getBorder().intValue());
		borderframe.setLayoutData(BorderData.CENTER);
		
		if(maxlevel <= alevel){//Do other panels have to be inserted?
			innerpanel=new Composite(borderframe, SWT.NONE);
			innerpanel.setBackground(jobcolor);			
			return;
		}

		innerpanel=new Composite(borderframe, SWT.NONE);
		
		innerpanel.setBackground(backgroundcolor);
		
		if(centerpic!=null){
			innerpanel.setBackgroundImage(centerpic);
		}
		
		//Generate all displaynodes for elements in lml-tree which are childs of the current node
		ArrayList<DisplayNode> dispnodes=getLowerDisplayNodes(ascheme, adata, levels, model, apref.isHighestrowfirst());
		
		
		if(alevel==maxlevel-1){//Paint rects instead of use composites for lowest-level-rectangles
			
			final RectPaintListener listener= new RectPaintListener(dispnodes, apref.getCols().intValue(), innerpanel);
			listener.horizontalSpacing=apref.getHgap().intValue();
			listener.verticalSpacing=apref.getVgap().intValue();
			
			listener.normalborder=apref.getBorder().intValue();
			listener.mouseborder=apref.getMouseborder().intValue();
			
			listener.bordercolor=bordercolor;
			
			//Add all listeners to the innerpanel now
			innerpanel.addListener(SWT.Paint, listener);
			
			innerpanel.addMouseMoveListener(new MouseMoveListener() {
				
				public void mouseMove(MouseEvent e) {
					
					DisplayNode focussed=listener.getDisplayNodeAtPos(e.x, e.y);
					
					if(focussed!=null)
						lguiItem.getObjectStatus().mouseover(focussed.getConnectedObject());
					
				}
			});
			
			innerpanel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {
					DisplayNode focussed=listener.getDisplayNodeAtPos(e.x, e.y);
					
					if(focussed!=null)
						lguiItem.getObjectStatus().mouseup(focussed.getConnectedObject());
				}
				
				@Override
				public void mouseDown(MouseEvent e) {
					DisplayNode focussed=listener.getDisplayNodeAtPos(e.x, e.y);
					
					if(focussed!=null)
						lguiItem.getObjectStatus().mousedown(focussed.getConnectedObject());
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
			});
			
			innerpanel.addListener(SWT.MouseExit, new Listener(){

				@Override
				public void handleEvent(Event event) {
					lguiItem.getObjectStatus().mouseExitLast();
				}
				
			});
		}		
		else{ //insert lower nodedisplays, nest composites
			
			//Set Gridlayout only if needed
			GridLayout layout=new GridLayout(apref.getCols().intValue(), true);
			layout.horizontalSpacing=apref.getHgap().intValue();
			layout.verticalSpacing=apref.getVgap().intValue();
			
			layout.marginWidth=1;
			layout.marginHeight=1;
			
			innerpanel.setLayout(layout);
			
			for(DisplayNode dispnode: dispnodes){

				NodedisplayComp ainner=new NodedisplayComp(lguiItem, model, dispnode, nodeview, maxlevel-currentlevel-1, innerpanel, SWT.NONE);
				innercomps.add(ainner);

				if(gridData==null){
					gridData = new GridData();
					gridData.grabExcessHorizontalSpace = true;
					gridData.grabExcessVerticalSpace = true;
					gridData.horizontalAlignment = GridData.FILL;
					gridData.verticalAlignment = GridData.FILL;
				}

				ainner.setLayoutData(gridData);

			}
		}
	}
		
	
	/**
	 * Show title or name for this panel
	 */
	public void showTitle(){
		titlelabel.setVisible(true);
		apref.setShowtitle(true);
	}
	
	/**
	 * Hide title or name for this panel
	 */
	public void hideTitle(){
		titlelabel.setVisible(false);
		apref.setShowtitle(false);
	}

	//Needed for actions at firstpaint, right after reading the lguiItem-model
	private boolean firstpaint=true;
	
	public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown) {
		
		if(node==null) return;
		
		ObjectType conobject=node.getConnectedObject();
		if(currentlevel==maxlevel){
			
			if(lguiItem.getObjectStatus().isMouseover(conobject)){
				borderframe.setBorderWidth(apref.getMouseborder().intValue());
			}
			else borderframe.setBorderWidth(apref.getBorder().intValue());
			
			if(lguiItem.getObjectStatus().isAnyMousedown() && !lguiItem.getObjectStatus().isMousedown(conobject)){//Change color
				innerpanel.setBackground( ColorConversion.getColor( lguiItem.getOIDToObject().getColorById(null) ) );
			}
			else innerpanel.setBackground(jobcolor);
		}
		else if(currentlevel==maxlevel-1){//For rectangle-paint of lowest-level-elements
			innerpanel.redraw();
		}
	}
	
}
