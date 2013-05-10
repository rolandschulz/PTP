package org.eclipse.ptp.etfw.tau.papiselect.papic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.tau.papiselect.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class EventTreeDialog extends Dialog {

	class ETreeCellLabelProvider extends CellLabelProvider {

		@Override
		public int getToolTipDisplayDelayTime(Object object) {
			return 1000;
		}

		@Override
		public Point getToolTipShift(Object object) {
			return new Point(5, 5);
		}

		@Override
		public String getToolTipText(Object o) {
			if (o instanceof ETItem) {
				return ((ETItem) o).desc;
			}
			return null;
		}

		@Override
		public int getToolTipTimeDisplayed(Object object) {
			return 50000;
		}

		@Override
		public void update(ViewerCell cell) {
			cell.setText(((ETItem) cell.getElement()).label);
		}

	}

	CheckboxTreeViewer treeV;
	EventTree et;
	private final IFileStore toolPath;
	// private Tree tree;
	PapiCSelect cSelect;

	// private final String treeTop="treeTop";

	private final IBuildLaunchUtils utilBlob;

	/**
	 * @since 4.0
	 */
	public EventTreeDialog(Shell parentShell, IFileStore tp, IBuildLaunchUtils utilBlob) {
		super(parentShell);
		this.utilBlob = utilBlob;
		this.setShellStyle(this.getShellStyle() | SWT.RESIZE);
		toolPath = tp;
	}

	public ArrayList<String> checkCommands() {

		// EventTree ett=(EventTree) treeV.getContentProvider();

		final ArrayList<String> selE = new ArrayList<String>();

		for (int i = 0; i < et.children.size(); i++) {
			for (int j = 0; j < et.children.get(i).children.size(); j++) {
				for (int k = 0; k < et.children.get(i).children.get(j).children.size(); k++) {
					final Event e = (Event) et.children.get(i).children.get(j).children.get(k);
					if (e.getCheck() && e.children.size() > 0 && e.testCommand().indexOf(":") < 0) //$NON-NLS-1$
					{
						selE.add(e.testCommand());
					}
				}
			}
		}

		return selE;
	}

	private void checkGray(ArrayList<ETItem> all, Set<Integer> grey) {
		boolean isGrey = false;
		Event test = null;
		for (int i = 0; i < all.size(); i++) {
			test = (Event) all.get(i);
			isGrey = (!test.checked) && grey.contains(new Integer(test.index));
			if (isGrey) {
				treeV.setGrayChecked(test, true);// grey.contains(new
													// Integer(((Event)all.get(i)).index)));
			} else {

				treeV.setGrayed(test, false);
				if (!test.checked) {
					treeV.setChecked(test, false);
				}

			}
			// if(isGrey)treeV.setChecked(all.get(i),isGrey);
		}
	}

	@Override
	protected Control createDialogArea(Composite top) {
		top.getShell().setText(Messages.EventTreeDialog_PapiEventSelection);
		final Composite parent = (Composite) super.createDialogArea(top);
		final GridLayout gl = new GridLayout();
		parent.setLayout(gl);

		final GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 500;
		gd.widthHint = 350;

		treeV = new CheckboxTreeViewer(parent, SWT.BORDER);

		ColumnViewerToolTipSupport.enableFor(treeV);

		// et=new EventTree();

		cSelect = new PapiCSelect(toolPath, utilBlob);// "E:\\PAPIProject\\ptest2.xml");//
		et = cSelect.getEventTree();

		treeV.setContentProvider(et);

		// tree=treeV.getTree();

		treeV.getTree().setLayoutData(gd);
		// tree.setLayoutData(gd);
		treeV.setLabelProvider(new ETreeCellLabelProvider());

		treeV.setInput(et);
		treeV.setGrayedElements(new Object[0]);
		treeV.expandToLevel(3);
		treeV.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				final Object element = event.getElement();

				if (treeV.getGrayed(element)) {
					treeV.setChecked(element, true);
					return;
				}

				if (element instanceof ETItem) {
					doCheck((ETItem) element, event.getChecked());
				}
			}

		});

		return parent;
	}

	private void doCheck(ETItem element, boolean checked) {
		if (element instanceof EventTree || element instanceof Component || element instanceof EventSet) {
			final ETItem eti = element;
			ETItem toCheck = null;
			eti.setCheck(checked);
			for (int i = 0; i < eti.children.size(); i++) {
				toCheck = eti.children.get(i);
				if (treeV.getGrayed(toCheck) || (treeV.getChecked(toCheck) == checked)) {
					continue;
				}
				treeV.setChecked(toCheck, checked);
				doCheck(toCheck, checked);
			}

		} else {
			if (element instanceof Modifier) {
				if (!treeV.getGrayed(element)) {
					(element).checked = checked;// event.getChecked();
				}
			} else {
				if (element instanceof Event) {
					if (!treeV.getGrayed(element)) {
						final Event e = (Event) element;
						e.setCheck(checked);// event.getChecked());

						final Component c = (Component) e.parent.parent;
						// ((ETItem)element).checked=event.getChecked();

						final Set<Integer>[] av = cSelect.getAvailable(c.index, c.eNames);

						if (av == null) {
							return;
						}

						Set<Integer> grey = new HashSet<Integer>(((EventSet) c.children.get(0)).fullSet);

						// Object[]test=((EventSet)c.children.get(0)).fullSet.toArray();
						// for(int i=0;i<test.length;i++)
						// {
						// System.out.println("All: "+test[i]);
						// }
						//
						// test=av[0].toArray();
						// for(int i=0;i<test.length;i++)
						// {
						// System.out.println("Av: "+test[i]);
						// }

						grey.removeAll(av[0]);

						// test=grey.toArray();
						// for(int i=0;i<test.length;i++)
						// {
						// System.out.println("Grey: "+test[i]);
						// }

						// boolean isGrey;
						ArrayList<ETItem> all = ((EventSet) c.children.get(0)).children;

						checkGray(all, grey);

						// for(int i=0;i<all.size();i++){
						// isGrey=!((Event)all.get(i)).checked&&grey.contains(new
						// Integer(((Event)all.get(i)).index));
						// treeV.setGrayed(all.get(i), isGrey);
						// //System.out.println(all.get(i).label+" "+
						// isGrey);//grey.contains(new
						// Integer(((Event)all.get(i)).index)));
						// }

						grey = new HashSet<Integer>(((EventSet) c.children.get(1)).fullSet);
						grey.removeAll(av[1]);

						all = ((EventSet) c.children.get(1)).children;
						checkGray(all, grey);

						// treeV.setChecked(element, checked);

						// for(int i=0;i<all.size();i++){
						// isGrey=(!((Event)all.get(i)).checked)&&grey.contains(new
						// Integer(((Event)all.get(i)).index));
						// treeV.setGrayed(all.get(i),isGrey);//
						// grey.contains(new
						// Integer(((Event)all.get(i)).index)));
						// if(isGrey)treeV.setChecked(all.get(i),isGrey);
						// }

					} else {
						treeV.setChecked(element, true);
					}
					// treeV.refresh();
				}
			}

		}
	}

	public ArrayList<String> getCommands() {

		// EventTree ett=(EventTree) treeV.getContentProvider();

		final ArrayList<String> selE = new ArrayList<String>();

		for (int i = 0; i < et.children.size(); i++) {
			for (int j = 0; j < et.children.get(i).children.size(); j++) {
				for (int k = 0; k < et.children.get(i).children.get(j).children.size(); k++) {
					final Event e = (Event) et.children.get(i).children.get(j).children.get(k);
					if (e.getCheck()) {
						selE.add(e.getCommand());
						// System.out.println(e.getCommand());
						// for(int l=0;l<e.children.size();l++)
						// {
						// if(e.children.get(l).checked)
						// {
						// System.out.print(e.children.get(l).label);
						// }
						// }
						// System.out.println();
					}
				}
			}
		}

		return selE;
	}

	@Override
	protected void okPressed() {
		final ArrayList<String> badCom = checkCommands();
		if (badCom.size() > 0) {
			String events = ""; //$NON-NLS-1$
			for (int i = 0; i < badCom.size(); i++) {
				events += badCom.get(i);
				if (i != badCom.size() - 1) {
					events += ", "; //$NON-NLS-1$
				}
			}
			MessageDialog.openWarning(this.getShell(), Messages.EventTreeDialog_EventModifiersRequired,
					Messages.EventTreeDialog_PleaseSelectAtLeastOneMod + events);
			return;
		}

		super.okPressed();
	}
}
