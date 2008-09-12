package org.eclipse.ptp.perf.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ParametricParameterTab extends AbstractLaunchConfigurationTab implements IPerformanceLaunchConfigurationConstants{

	private Button useParam;
	
	private Text processors;
	private Text optLevels;
	//private Table cmpTab;
	private Table argTab;
	private Table varTab;
	
	private Button allCom;
	
	private Text trial;
	private Text script;
	private Button scriptBrowse;
	
	/**
	 * Listen for activity in the options widgets
	 * @author wspear
	 *
	 */
	protected class WidgetListener extends SelectionAdapter implements
			ModifyListener, IPropertyChangeListener {
		public void widgetSelected(SelectionEvent e) {
			
			updateLaunchConfigurationDialog();
		}

		public void propertyChange(PropertyChangeEvent event) {
			updateLaunchConfigurationDialog();
		}

		public void modifyText(ModifyEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}
	
	private WidgetListener wl = new WidgetListener();
	
	public void createControl(Composite comp) {
		
		final ScrolledComposite sparent = new ScrolledComposite(comp, SWT.V_SCROLL);
		setControl(sparent);

		final Composite parent = new Composite(sparent,SWT.NONE);
		
		//FillLayout fl = new FillLayout();
		//fl.type=SWT.VERTICAL;
		
		//parent.setLayout(createGridLayout(2, false, 0, 0));
		//parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		parent.setLayout(createGridLayout(3, false, 0, 0));
		parent.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		createVerticalSpacer(parent, 3);
		
		useParam=new Button(parent,SWT.CHECK);
		useParam.setText("Enable Parametric Testing");
		useParam.addSelectionListener(wl);
		
		GridData fill3=new GridData(GridData.FILL_HORIZONTAL);
		fill3.horizontalSpan=3;
		
		Label lab;
		
		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);
		
		lab = new Label(parent,SWT.NONE);
		lab.setText("Optimization Levels");
		lab.setToolTipText("Leave blank for default, may not work with some compilers");
		
		optLevels=new Text(parent,SWT.BORDER);
		optLevels.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		optLevels.addModifyListener(wl);
		
		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);
		
		
		lab = new Label(parent,SWT.NONE);
		lab.setText("MPI Processes");
		
		processors=new Text(parent,SWT.BORDER);
		processors.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		processors.addModifyListener(wl);
		
		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);
		
		
		
		//cmpTab=makeArgTable(parent,"Flags","Variables");//TODO: Make a compiler table
		allCom=new Button(parent,SWT.CHECK);
		allCom.setText("Run jobs for all combinations");
		allCom.setToolTipText("Perform one run for every combination of values: compiler * mpi-processes * arguments * environment-variables");
		allCom.addSelectionListener(wl);
		//simWeak.setLayoutData(fill3);
		
		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);
		
		argTab=makeArgTable(parent,"Name","Values","Application Arguments");
		
		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);
		
		varTab=makeArgTable(parent,"Name","Values","Environment Variables");
		
		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);
		
//		lab=new Label(parent,SWT.NONE);
//		lab.setText("Trial ID");
//		trial=new Text(parent,SWT.BORDER);
//		trial.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		new Label(parent,SWT.NONE);
		
		lab=new Label(parent,SWT.NONE);
		lab.setText("Analysis Application");
		script=new Text(parent,SWT.BORDER);
		script.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		script.addModifyListener(wl);
		scriptBrowse=new Button(parent,SWT.NONE);
		scriptBrowse.setText("Browse");
		scriptBrowse.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				FileDialog fd=new FileDialog(parent.getShell());
				fd.setText("Select a PerfExplorer script");
				String s =fd.open();
				if(s!=null){
					script.setText(s);
				}
			}
			
		});
		
		int thisHeight=parent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		sparent.setMinSize(400, thisHeight);
		sparent.setExpandHorizontal(true);
		sparent.setExpandVertical(true);
		sparent.setContent(parent);
	}
	
	private Table makeArgTable(Composite suParent,String c1, String c2, String title){
		
		Group parent=new Group(suParent,SWT.NONE);
		parent.setLayout(createGridLayout(3, false, 0, 0));
		parent.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		parent.setText(title);
		
		final Table argTab = new Table(parent,SWT.BORDER|SWT.V_SCROLL|SWT.MULTI|SWT.CHECK);//SWT.FULL_SELECTION|
		argTab.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				if(e.detail==SWT.CHECK){
					updateLaunchConfigurationDialog();
				}
				
			}
			
		});
		
		GridData tableGD=new GridData(GridData.FILL_HORIZONTAL);
		tableGD.heightHint=80;
		tableGD.horizontalSpan=3;
		argTab.setLayoutData(tableGD);
		
		GridData span2=new GridData(GridData.FILL_HORIZONTAL);
		span2.horizontalSpan=2;
		
		
		
		TableColumn tc = new TableColumn(argTab,SWT.NONE);
		tc.setText(c1);
		tc.setWidth(80);
		
		tc = new TableColumn(argTab,SWT.NONE);
		tc.setText(c2);
		tc.setWidth(80);
		
		argTab.setHeaderVisible(true);
		
		final TableEditor te=new TableEditor(argTab);
		te.horizontalAlignment=SWT.LEFT;
		te.grabHorizontal=true;
		te.minimumWidth=50;
		
		//TODO: Eventually enable line-editing
//		argTab.addSelectionListener(new SelectionAdapter(){
//			public void widgetSelected(SelectionEvent e){
//				final int selCol=1;//e.x;//TODO: This x coord may not work
//				
//				Control oldE=te.getEditor();
//				if(oldE!=null){
//					oldE.dispose();
//				}
//				TableItem ti=(TableItem)e.item;
//				
//				
//				if(ti==null){
//					return;
//				}
//				
//				Text iEd=new Text(argTab,SWT.NONE);
//				iEd.setText(ti.getText(selCol));
//				iEd.addModifyListener(new ModifyListener(){
//					public void modifyText(ModifyEvent me){
//						Text text=(Text)te.getEditor();
//						te.getItem().setText(selCol,text.getText());
//					}
//				});
//				iEd.selectAll();
//				iEd.setFocus();
//				te.setEditor(iEd,ti,selCol);
//			}
//		});
		
		
		
		
		Label lab = new Label(parent,SWT.NONE);
		lab.setText(c1);
		final Text flags=new Text(parent,SWT.BORDER);
		flags.setLayoutData(span2);
		
		lab = new Label(parent,SWT.NONE);
		lab.setText(c2);
		final Text vars=new Text(parent,SWT.BORDER);
		vars.setLayoutData(span2);
		
		Button addVar = new Button(parent,SWT.None);
		addVar.setText("Add");
		addVar.addSelectionListener(
				new SelectionListener(){

					public void widgetDefaultSelected(SelectionEvent e) {}

					public void widgetSelected(SelectionEvent e) {

						TableItem it = new TableItem(argTab,SWT.NONE);
						
						it.setText(0, flags.getText());
						it.setText(1,vars.getText());
						updateLaunchConfigurationDialog();
					}

				}
		);
		
		
		Button removeVar = new Button(parent,SWT.None);
		removeVar.setText("Remove");
		removeVar.addSelectionListener(
				new SelectionListener(){

					public void widgetDefaultSelected(SelectionEvent e) {}

					public void widgetSelected(SelectionEvent e) {
						if(argTab.getSelectionIndices().length>0)
						{
							argTab.remove(argTab.getSelectionIndices());
							updateLaunchConfigurationDialog();
						}
					}
					
				});
		new Label(parent,SWT.NULL);
		
		return argTab;
	}

	public String getName() {
		return "Parametric Study";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		
		
		
		try {
			processors.setText(configuration.getAttribute(PARA_NUM_PROCESSORS, "1"));
			optLevels.setText(configuration.getAttribute(PARA_OPT_LEVELS, ""));
			setTableList(argTab,configuration.getAttribute(PARA_ARG_NAMES, (List<String>)null),configuration.getAttribute(PARA_ARG_VALUES, (List<String>)null),configuration.getAttribute(PARA_ARG_BOOLS, (List<String>)null));
			setTableList(varTab,configuration.getAttribute(PARA_VAR_NAMES, (List<String>)null),configuration.getAttribute(PARA_VAR_VALUES, (List<String>)null),configuration.getAttribute(PARA_VAR_BOOLS, (List<String>)null));
			
			useParam.setSelection(configuration.getAttribute(PARA_USE_PARAMETRIC, false));			
			allCom.setSelection(configuration.getAttribute(PARA_ALL_COMBO, false));
			
			script.setText(configuration.getAttribute(PARA_PERF_SCRIPT, ""));
		
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static void setTableList(Table ta, List<String> data0,List<String> data1, List<String> checkList){
		
		if(data0==null||data1==null){
			return;
		}
		ta.removeAll();
		for(int i=0;i<data0.size();i++){
			TableItem ti = new TableItem(ta,SWT.NONE);
			ti.setText(0,data0.get(i));
			ti.setText(1,data1.get(i));
			if(checkList.get(i).equals("1")){
				ti.setChecked(true);
			}
		}
	}

	private static List<String> getTableList(Table table, int index){
		List<String> l = new ArrayList<String>();
		TableItem[] tiA=table.getItems();
		
		for(TableItem it : tiA){
			l.add(it.getText(index));
		}
		
		return l;
	}
	
	private static List<String> getTableChecks(Table table){
		List<String> l = new ArrayList<String>();
		TableItem[] tiA=table.getItems();
		
		for(TableItem it : tiA){
			if(it.getChecked())
				l.add("1");
			else{
				l.add("0");
			}
		}
		
		return l;
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PARA_NUM_PROCESSORS, processors.getText());
		configuration.setAttribute(PARA_OPT_LEVELS, optLevels.getText());
		configuration.setAttribute(PARA_ARG_NAMES, getTableList(argTab,0));
		configuration.setAttribute(PARA_ARG_VALUES, getTableList(argTab,1));
		configuration.setAttribute(PARA_ARG_BOOLS, getTableChecks(argTab));
		
		configuration.setAttribute(PARA_VAR_NAMES, getTableList(varTab,0));
		configuration.setAttribute(PARA_VAR_VALUES, getTableList(varTab,1));
		configuration.setAttribute(PARA_VAR_BOOLS, getTableChecks(varTab));
		
		
		configuration.setAttribute(PARA_USE_PARAMETRIC, useParam.getSelection());
		
		
		configuration.setAttribute(PARA_ALL_COMBO, allCom.getSelection());
		
		configuration.setAttribute(PARA_PERF_SCRIPT, script.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {	
		
	}

	private static final String weakError="Checked parameters must have at most 1 flag/name and exactly as many variables a there are processor counts";
	
	public boolean isValid(ILaunchConfiguration config){
		
		setErrorMessage(null);
		setMessage(null);
		
		super.isValid(config);
		
		
		boolean all=allCom.getSelection();
		
		
		int numProcArgs=getComArgs(processors.getText()).size();
		
		boolean ok = checkTableCounts(argTab,numProcArgs,!all);
		ok &= checkTableCounts(varTab,numProcArgs,!all);
		
		if(!ok)
		{
			setErrorMessage(weakError);
		}
		
		return ok;
	}
	
	private static boolean checkTableCounts(Table t, int n,boolean all){
		
		TableItem[] tis=t.getItems();
		
		for(TableItem ti:tis){
			if(ti.getChecked()||all){
				if(getComArgs(ti.getText(0)).size()>1  ||  getComArgs(ti.getText(1)).size()!=n){
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * Returns a new GridLayout
	 * @param columns Number of columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected static GridLayout createGridLayout(int columns, boolean isEqual, int mh,
			int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}
	
	protected static GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}

	/**
	 * Given a string of comma separated strings, returns an array of the
	 * strings
	 * 
	 * @param combined
	 *            The string to be tokenized by commas
	 * @return
	 */
	static List<String> getComArgs(String combined) {
		StringTokenizer st = new StringTokenizer(combined, ",");
		List<String> numProcesses = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			numProcesses.add(st.nextToken());
		}
		return numProcesses;
	}

}
