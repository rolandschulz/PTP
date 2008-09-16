package org.eclipse.ptp.rm.mpi.openmpi.ui.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.ui.OpenMPIUIPlugin;
import org.eclipse.ptp.rm.ui.launch.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.utils.ui.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class BasicOpenMpiRMLaunchConfigurationDynamicTab extends
		AbstractRMLaunchConfigurationDynamicTab {

	private Composite control;
	private Spinner numProcsSpinner;
	private Button bySlotButton;
	private Button noOversubscribeButton;
	private Button noLocalButton;
	private Button usePrefixButton;
	private Text prefixText;
	private Text hostFileText;
	private Button hostFileButton;
	private Text hostListText;
	private Button hostListButton;
	private Button browseButton;

	class WidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {
		public WidgetListener(AbstractRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		@Override
		protected void doModifyText(ModifyEvent e) {
			if (e.getSource() == numProcsSpinner || e.getSource() == prefixText || e.getSource() == hostFileText || e.getSource() == hostListText) {
//				getDataSource().justValidate();
			} else{
				super.doModifyText(e);
			}
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			if (e.getSource() == bySlotButton || e.getSource() == noOversubscribeButton || e.getSource() == noLocalButton || e.getSource() == usePrefixButton) {
//				getDataSource().justValidate();
			} else if (e.getSource() == usePrefixButton || e.getSource() == hostFileButton || e.getSource() == hostListButton) {
//				getDataSource().justValidate();
				updateControls();
			} else {
				super.doWidgetSelected(e);
			}
		}
	}

	class DataSource extends RMLaunchConfigurationDynamicTabDataSource {

		private int numProcs;
		private boolean bySlot;
		private boolean noOversubscribe;
		private boolean noLocal;
		private boolean usePrefix;
		private String prefix;
		private boolean useHostFile;
		private String hostFile;
		private boolean useHostList;
		private String hostList;

		protected DataSource(AbstractRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			numProcs = numProcsSpinner.getSelection();
			bySlot = bySlotButton.getSelection();
			noOversubscribe = noOversubscribeButton.getSelection();
			noLocal = noLocalButton.getSelection();
			usePrefix = usePrefixButton.getSelection();
			prefix = extractText(prefixText);
			useHostFile = hostFileButton.getSelection();
			hostFile = extractText(hostFileText);
			useHostList = hostListButton.getSelection();
			hostList = extractText(hostListText);
		}

		@Override
		protected void copyToFields() {
			numProcsSpinner.setSelection(numProcs);
			bySlotButton.setSelection(bySlot);
			noOversubscribeButton.setSelection(noOversubscribe);
			noLocalButton.setSelection(noLocal);
			usePrefixButton.setSelection(usePrefix);
			applyText(prefixText, prefix);
			applyText(hostFileText, hostFile);
			hostFileButton.setSelection(useHostFile);
			applyText(hostListText, hostListToText(hostList));
			hostListButton.setSelection(useHostList);
		}

		@Override
		protected void copyToStorage() {
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS, numProcs);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_BYSLOT, bySlot);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_NOOVERSUBSCRIBE, noOversubscribe);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_NOLOCAL, noLocal);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEPREFIX, usePrefix);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_PREFIX, prefix);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTFILE, useHostFile);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTFILE, hostFile);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTLIST, useHostList);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTLIST, hostList);
		}

		@Override
		protected void loadDefault() {
			numProcs = OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS;
			bySlot = OpenMPILaunchConfigurationDefaults.ATTR_BYSLOT;
			noOversubscribe = OpenMPILaunchConfigurationDefaults.ATTR_NOOVERSUBSCRIBE;
			noLocal = OpenMPILaunchConfigurationDefaults.ATTR_NOLOCAL;
			usePrefix = OpenMPILaunchConfigurationDefaults.ATTR_USEPREFIX;
			prefix = OpenMPILaunchConfigurationDefaults.ATTR_PREFIX;
			hostFile = OpenMPILaunchConfigurationDefaults.ATTR_HOSTFILE;
			useHostFile = OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTFILE;
			hostList = OpenMPILaunchConfigurationDefaults.ATTR_HOSTLIST;
			useHostList = OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTLIST;

		}

		@Override
		protected void loadFromStorage() {
			try {
				numProcs = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS, OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS);
				bySlot = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_BYSLOT, OpenMPILaunchConfigurationDefaults.ATTR_BYSLOT);
				noOversubscribe = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_NOOVERSUBSCRIBE, OpenMPILaunchConfigurationDefaults.ATTR_NOOVERSUBSCRIBE);
				noLocal = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_NOLOCAL, OpenMPILaunchConfigurationDefaults.ATTR_NOLOCAL);
				usePrefix = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEPREFIX, OpenMPILaunchConfigurationDefaults.ATTR_USEPREFIX);
				prefix = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_PREFIX, OpenMPILaunchConfigurationDefaults.ATTR_PREFIX);
				hostFile = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_HOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_HOSTFILE);
				useHostFile = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTFILE);
				hostList = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_HOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_HOSTLIST);
				useHostList = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTLIST);
			} catch (CoreException e) {
				// TODO handle exception?
				PTPCorePlugin.log(e);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (numProcs < 1) {
				throw new ValidationException("Must specify at least one process");
			}
			if (usePrefix && prefix == null) {
				throw new ValidationException("Prefix cannot be empty");
			}
			if (useHostFile && hostFile == null) {
				throw new ValidationException("Must provide a host file");
			}
			if (useHostList && hostList == null) {
				throw new ValidationException("Must provide at least one host name");
			}
		}

		/**
		 * Convert a comma separated list into one host per line
		 *
		 * @param list
		 * @return
		 */
		private String hostListToText(String list) {
			if (list == null) {
				return "";
			}
			String result = "";
			String[] values = list.split(",");
			for (int i = 0; i < values.length; i++) {
				if (!values[i].equals("")) {
					if (i > 0) {
						result += "\r";
					}
					result += values[i];
				}
			}
			return result;
		}
	}

	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new DataSource(this);
	}

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new WidgetListener(this);
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return "Basic";
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		control.setLayout(layout);

		Label label  = new Label(control, SWT.NONE);
		label.setText("Number of processes:");

		numProcsSpinner = new Spinner(control, SWT.BORDER);
		numProcsSpinner.addModifyListener(getListener());
		numProcsSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		final Group optionsGroup = new Group(control, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		optionsGroup.setText("Options");
		layout = new GridLayout();
		layout.numColumns = 3;
		optionsGroup.setLayout(layout);

		bySlotButton = new Button(optionsGroup, SWT.CHECK);
		bySlotButton.addSelectionListener(getListener());
		bySlotButton.setText("By slot");

		noOversubscribeButton = new Button(optionsGroup, SWT.CHECK);
		noOversubscribeButton.addSelectionListener(getListener());
		noOversubscribeButton.setText("No oversubscribe");

		noLocalButton = new Button(optionsGroup, SWT.CHECK);
		noLocalButton.addSelectionListener(getListener());
		noLocalButton.setText("No local");

		usePrefixButton = new Button(optionsGroup, SWT.CHECK);
		usePrefixButton.addSelectionListener(getListener());
		usePrefixButton.setText("Prefix:");

		prefixText = new Text(optionsGroup, SWT.BORDER);
		prefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		prefixText.addModifyListener(getListener());

		final Group hostGroup = new Group(control, SWT.NONE);
		hostGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		hostGroup.setText("Hosts");
		layout = new GridLayout();
		layout.numColumns = 3;
		hostGroup.setLayout(layout);

		hostFileButton = new Button(hostGroup, SWT.CHECK);
		hostFileButton.addSelectionListener(getListener());
		hostFileButton.setText("Host file:");

		hostFileText = new Text(hostGroup, SWT.BORDER);
		hostFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hostFileText.addModifyListener(getListener());

		browseButton = new Button(hostGroup, SWT.NONE);
		browseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		browseButton.addSelectionListener(getListener());
		PixelConverter pixelconverter = new PixelConverter(control);
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = pixelconverter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		browseButton.setLayoutData(gd);
		browseButton.setText("Browse");

		hostListButton = new Button(hostGroup, SWT.CHECK);
		hostListButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		hostListButton.addSelectionListener(getListener());
		hostListButton.setText("Host list:");

		hostListText = new Text(hostGroup, SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 20;
		hostListText.setLayoutData(gd);
		hostListText.addModifyListener(getListener());


	}

	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm,
			IPQueue queue, ILaunchConfiguration configuration)
			throws CoreException {

		List<IAttribute<?,?,?>> attrs = new ArrayList<IAttribute<?,?,?>>();

		int numProcs = configuration.getAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS, OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS);
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(numProcs));
		} catch (IllegalValueException e) {
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIUIPlugin.getDefault().getBundle().getSymbolicName(), "Invalid configuration", e));
		}

		attrs.add(OpenMPILaunchAttributes.getLaunchArgumentsAttributeDefinition().create(OpenMPILaunchConfiguration.calculateArguments(configuration)));

		return attrs.toArray(new IAttribute<?,?,?>[attrs.size()]);
	}

	public Control getControl() {
		return control;
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS, OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_BYSLOT, OpenMPILaunchConfigurationDefaults.ATTR_BYSLOT);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_NOOVERSUBSCRIBE, OpenMPILaunchConfigurationDefaults.ATTR_NOOVERSUBSCRIBE);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_NOLOCAL, OpenMPILaunchConfigurationDefaults.ATTR_NOLOCAL);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_USEPREFIX, OpenMPILaunchConfigurationDefaults.ATTR_USEPREFIX);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_PREFIX, OpenMPILaunchConfigurationDefaults.ATTR_PREFIX);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTFILE);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_HOSTFILE);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTLIST);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_HOSTLIST);
		return new RMLaunchValidation(true, null);
	}

	private DataSource getBasicDataSource() {
		return (DataSource)getDataSource();
	}

	@Override
	public void updateControls() {
		prefixText.setEnabled(usePrefixButton.getSelection());
		browseButton.setEnabled(hostFileButton.getSelection());
		hostFileText.setEnabled(hostFileButton.getSelection());
		hostListText.setEnabled(hostListButton.getSelection());
	}

	private DataSource getLocalDataSource() {
		return (DataSource)super.getDataSource();
	}
}
