/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2015/05/14 13:52:20  mmaloney
 * RC08 prep
 *
 * Revision 1.5  2015/04/03 19:53:27  mmaloney
 * Fixed CWMS-5626 where by some GUIs would silently exit if user had no privilege.
 *
 * Revision 1.4  2015/02/06 18:46:59  mmaloney
 * Config option to not display certain buttons: Time Series, Groups, Run Comps, & Algorithms.
 *
 * Revision 1.3  2014/10/07 12:34:37  mmaloney
 * DecodesSetting.showNetlistEditor
 *
 * Revision 1.2  2014/05/22 12:14:09  mmaloney
 * Disable TS buttons when database is XML.
 *
 * Revision 1.1.1.1  2014/05/19 15:28:59  mmaloney
 * OPENDCS 6.0 Initial Checkin
 *
 * Revision 1.8  2013/04/26 14:29:23  mmaloney
 * gif not png.
 *
 * Revision 1.7  2013/04/26 14:22:28  mmaloney
 * Added ts list
 *
 * Revision 1.6  2013/04/09 13:33:14  mmaloney
 * Removed "Retrieval and Decoding" button that doesn't do anything.
 *
 * Revision 1.5  2013/03/26 17:48:18  mmaloney
 * Change title to OPENDCS <version>
 *
 * Revision 1.4  2013/03/25 18:56:03  mmaloney
 * Plug in Process Monitor
 *
 * Revision 1.3  2013/02/28 16:46:24  mmaloney
 * Start pdt maintenance thread.
 *
 * Revision 1.2  2012/07/24 13:41:04  mmaloney
 * Clean up resource strings.
 *
 * Revision 1.1  2012/05/17 15:14:31  mmaloney
 * Initial implementation for USBR.
 *
 *
 * This is open-source software written by Sutron Corporation under
 * contract to the federal government. Anyone is free to copy and use this
 * source code for any purpos, except that no part of the information
 * contained in this file may be claimed to be proprietary.
 *
 * Except for specific contractual terms between Sutron and the federal 
 * government, this source code is provided completely without warranty.
 */
package decodes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import ilex.cmdline.BooleanToken;
import ilex.cmdline.TokenOptions;
import ilex.gui.GuiApp;
import ilex.util.AsciiUtil;
import ilex.util.EnvExpander;
import ilex.util.LoadResourceBundle;
import ilex.util.Logger;
import ilex.util.ProcWaiterThread;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import lrgs.gui.DecodesInterface;
import lrgs.gui.MessageBrowser;
import lrgs.nledit.NetlistEditFrame;
import lrgs.rtstat.RtStat;

import decodes.db.Platform;
import decodes.dbeditor.DbEditorFrame;
import decodes.gui.TopFrame;
import decodes.platwiz.Frame1;
import decodes.platwiz.PlatformWizard;
import decodes.tsdb.algoedit.AlgorithmWizard;
import decodes.tsdb.compedit.CAPEdit;
import decodes.tsdb.comprungui.RunComputationsFrame;
import decodes.tsdb.comprungui.RunComputationsFrameTester;
import decodes.tsdb.groupedit.TsDbGrpEditorFrame;
import decodes.tsdb.groupedit.TsListMain;
import decodes.tsdb.procmonitor.ProcessMonitor;
import decodes.util.DecodesException;
import decodes.util.DecodesSettings;
import decodes.util.DecodesVersion;
import decodes.util.ResourceFactory;
import decodes.util.CmdLineArgs;


@SuppressWarnings("serial")
public class LauncherFrame extends JFrame
{
	private static ResourceBundle labels = null;
	String myArgs[] = null;
	String compArgs[] = null;
	JPanel fullPanel = new JPanel();
	GridBagLayout fullLayout = new GridBagLayout();

	JPanel dcstoolButtonPanel = new JPanel();
	GridLayout dcstoolLayout = new GridLayout();
	JButton lrgsStatButton = new JButton();
	TitledBorder dcstoolButtonBorder;
	JButton browserButton = new JButton();
	JButton netlistButton = new JButton();
	JButton dbeditButton = new JButton();
	JButton platwizButton = new JButton();
	JButton toolkitConfigButton = new JButton();

	JPanel tsdbButtonPanel = new JPanel();
	GridLayout tsdbLayout = new GridLayout();
	TitledBorder tsdbButtonBorder;
	JButton tseditButton = new JButton();
	JButton groupEditButton = new JButton();
	JButton compeditButton = new JButton();
	JButton runcompButton = new JButton();
	private JButton procstatButton = new JButton();
	JButton algoeditButton = new JButton();

	boolean exitOnClose;
	DbEditorFrame dbEditorFrame;
	WindowAdapter dbEditorReaper;
	MessageBrowser browserFrame;
	WindowAdapter browserReaper;
	NetlistEditFrame netlistEditFrame;
	WindowAdapter netlistEditReaper;
	private TopFrame routMonFrame;
	WindowAdapter routMonReaper;
	Runnable afterDecodesInit;
	InitDecodesFrame initDecodesFrame;
	TopFrame groupEditFrame;// Time Series Button
	TsDbGrpEditorFrame tsDbGrpEditorFrame; // Time Series Groups Button
	JFrame procMonFrame;// Process Status Button
	WindowAdapter tsEditorReaper;
	WindowAdapter logViewerReaper;
	TopFrame tseditFrame;// Limit Status Button
	WindowAdapter tseditReaper;
	RunComputationsFrame runComputationsFrame;// Test Computations Button
	WindowAdapter runComputationsReaper;
	TopFrame compEditFrame;// Computations Button
	WindowAdapter compEditReaper;
	WindowAdapter procMonReaper;
	TopFrame algorithmWizFrame;// Algorithms Button
	WindowAdapter algorithmWizReaper;
	Frame1 platformWizFrame;// Platform Wizard button
	WindowAdapter platformWizReaper;
	TopFrame alarmsFrame;// Alarms Button
	WindowAdapter alarmsReaper;
	private static BooleanToken noCompFilterToken;
	private static BooleanToken justPrintVersionToken;
	WindowAdapter statContactReaper;
	private TsdbType tsdbType = TsdbType.NONE;
	private TopFrame lrgsStatFrame = null;
	private WindowAdapter lrgsStatReaper;
	private String installDir;
	TopFrame toolkitSetupFrame = null;
	WindowAdapter setupFrameReaper;

	public LauncherFrame()
	{
		exitOnClose = true;
		dbEditorFrame = null;
		browserFrame = null;
		netlistEditFrame = null;
		routMonFrame = null;
		afterDecodesInit = null;
		initDecodesFrame = null;
		groupEditFrame = null;
		tsDbGrpEditorFrame = null;
		procMonFrame = null;
		tseditFrame = null;
		runComputationsFrame = null;
		compEditFrame = null;
		algorithmWizFrame = null;
		platformWizFrame = null;
		alarmsFrame = null;
		
		String dbClass = DecodesSettings.instance().dbClassName;
		if (dbClass != null)
		{
			if (dbClass.contains("Hdb"))
				tsdbType = TsdbType.HDB;
			else if (dbClass.contains("OpenTsdb"))
				tsdbType = TsdbType.OPENTSDB;
			else if (dbClass.contains("Cwms"))
				tsdbType = TsdbType.CWMS;
			else
				tsdbType = TsdbType.NONE;
		}
		
		DecodesInterface.setGUI(true);

		installDir = EnvExpander.expand("$DECODES_INSTALL_DIR");
		try
		{
			jbInit();
			ImageIcon lrgsStatIcon = new ImageIcon(installDir
					+ File.separator + "icons" + File.separator
					+ "retproc48x48.gif");
			ImageIcon browserIcon = new ImageIcon(installDir
					+ File.separator + "icons" + File.separator
					+ "msgbrowser48x48.gif");
			ImageIcon netlistIcon = new ImageIcon(installDir
					+ File.separator + "icons" + File.separator
					+ "netlist48x48.gif");
			ImageIcon dbeditIcon = new ImageIcon(installDir
					+ File.separator + "icons" + File.separator
					+ "dbedit48x48.gif");
			ImageIcon platwizIcon = new ImageIcon(installDir
					+ File.separator + "icons" + File.separator
					+ "platwiz48x48.gif");
			ImageIcon setupIcon = new ImageIcon(installDir
					+ File.separator + "icons" + File.separator
					+ "setup48x48.gif");
			
			lrgsStatButton.setIcon(lrgsStatIcon);
			lrgsStatButton.setHorizontalAlignment(SwingConstants.CENTER);
			lrgsStatButton.setBackground(Color.white);
			
			browserButton.setIcon(browserIcon);
			browserButton.setHorizontalAlignment(SwingConstants.CENTER);
			browserButton.setBackground(Color.white);
			netlistButton.setIcon(netlistIcon);
			netlistButton.setHorizontalAlignment(SwingConstants.CENTER);
			netlistButton.setBackground(Color.white);
			dbeditButton.setIcon(dbeditIcon);
			dbeditButton.setHorizontalAlignment(SwingConstants.CENTER);
			dbeditButton.setBackground(Color.white);
			// platwizButton.setIcon(wizIcon);
			platwizButton.setText(labels
				.getString("LauncherFrame.platformWizardButton"));
			platwizButton.setIcon(platwizIcon);
			platwizButton.setHorizontalAlignment(SwingConstants.CENTER);
			platwizButton.setBackground(Color.white);
			toolkitConfigButton.setIcon(setupIcon);
			toolkitConfigButton.setHorizontalAlignment(SwingConstants.CENTER);
			toolkitConfigButton.setBackground(Color.white);

			setTitle(DecodesVersion.getAbbr() + " " + DecodesVersion.getVersion());
			// setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

			final LauncherFrame myframe = this;
			addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					String msg = null;
					if (dbEditorFrame != null)
						msg = LoadResourceBundle
							.sprintf(
								labels
									.getString("LauncherFrame.windowClosingMsg"),
								labels
									.getString("LauncherFrame.DECODESDBEditorButton"));
					else if (netlistEditFrame != null)
						msg = LoadResourceBundle
							.sprintf(
								labels
									.getString("LauncherFrame.windowClosingMsg"),
								labels
									.getString("LauncherFrame.networkListMaintButton"));
					else if (routMonFrame != null)
						msg = LoadResourceBundle
							.sprintf(
								labels
									.getString("LauncherFrame.windowClosingMsg"),
								labels
									.getString("LauncherFrame.retrievalProcessesButton"));
					else if (groupEditFrame != null)
						msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.windowClosingMsg"),
							labels.getString("LauncherFrame.timeSeriesButton"));
					else if (tsDbGrpEditorFrame != null)
						msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.windowClosingMsg"),
							labels.getString("LauncherFrame.timeSeriesButton"));
					else if (tseditFrame != null)
						msg = LoadResourceBundle
							.sprintf(
								labels
									.getString("LauncherFrame.windowClosingMsg"),
								labels
									.getString("LauncherFrame.limitStatusButton"));
					else if (runComputationsFrame != null)
						msg = LoadResourceBundle
							.sprintf(
								labels
									.getString("LauncherFrame.windowClosingMsg"),
								labels
									.getString("LauncherFrame.testComputationsButton"));
					else if (compEditFrame != null)
						msg = LoadResourceBundle.sprintf(labels
							.getString("LauncherFrame.windowClosingMsg"),
							labels
								.getString("LauncherFrame.computationsButton"));
					else if (algorithmWizFrame != null)
						msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.windowClosingMsg"),
							labels.getString("LauncherFrame.algorithmsButton"));
					else if (platformWizFrame != null)
						msg = LoadResourceBundle
							.sprintf(
								labels
									.getString("LauncherFrame.windowClosingMsg"),
								labels
									.getString("LauncherFrame.platformWizardButton"));
					if (msg != null)
					{
						JOptionPane.showMessageDialog(null,
							AsciiUtil.wrapString(msg, 60), "Error!",
							JOptionPane.ERROR_MESSAGE);
						return;
					}

					myframe.cleanupBeforeExit();
					System.exit(0);
				}
			});
			dbEditorReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					dbEditorFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"DbEditorFrame closed");
				}
			};
			setupFrameReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					toolkitSetupFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Setup frame closed");
				}
			};
			browserReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					browserFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Message Browser closed");
				}
			};
			netlistEditReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					netlistEditFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Network List Editor closed");
				}
			};
			routMonReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					routMonFrame = null;
					Logger.instance().debug1(
						"Retrieval Process Mon & Ctrl screen closed");
				}
			};
			tsEditorReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					groupEditFrame = null;
					tsDbGrpEditorFrame = null;
					
					if (tsdbType == TsdbType.CWMS
					 || tsdbType == TsdbType.HDB)
						Logger.instance().log(Logger.E_DEBUG1,
						"TS DB Group Editor screen closed");
					else
						Logger.instance().log(Logger.E_DEBUG1,
						"TS DB Editor screen closed");
				}
			};
			logViewerReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					procMonFrame = null;
					Logger.instance().info("Process Status screen closed");
				}
			};
			tseditReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					tseditFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Limits Status screen closed");
				}
			};

			runComputationsReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					runComputationsFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Test Computations screen closed");
				}
			};
			compEditReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					compEditFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Computations screen closed");
				}
			};
			procMonReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					procMonFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Process Monitor screen closed");
				}
			};
			algorithmWizReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					algorithmWizFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Algorithms screen closed");
				}
			};
			platformWizReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					platformWizFrame = null;
					Logger.instance().log(Logger.E_DEBUG1,
						"Platform Wizard screen closed");
				}
			};
			alarmsReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					alarmsFrame = null;
					Logger.instance().log(Logger.E_DEBUG1, "Alarms closed");
				}
			};

			lrgsStatReaper = new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					lrgsStatFrame = null;
					Logger.instance().debug1("LRGS Stat closed");
				}
			};
			ImageIcon tkIcon = new ImageIcon(
				ResourceFactory.instance().getIconPath());
			setIconImage(tkIcon.getImage());

			pack();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void cleanupBeforeExit()
	{
		if (initDecodesFrame != null)
			initDecodesFrame.dispose();
	}

	public boolean getExitOnClose()
	{
		return exitOnClose;
	}

	public void setExitOnClose(boolean tf)
	{
		exitOnClose = tf;
	}

	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_OPENED)
		{
			File f = new File(EnvExpander.expand("$DECODES_INSTALL_DIR/FIRST_RUN.TXT"));
			if (f.exists())
			{
				doFirstRunChecks();
				if (!f.delete())
					showError(LoadResourceBundle.sprintf(
						labels.getString("LauncherFrame.cantDelete"),
						f.getPath()));
			}
		}
	}

	/**
	 * Called once the first time the toolkit is run after an update or install.
	 * We do one-time checks here to make sure the DB and environment are
	 * correct for this version of the toolkit.
	 */
	private void doFirstRunChecks()
	{
	}

	private void jbInit() throws Exception
	{
		JPanel contentPane = (JPanel) this.getContentPane();

		dcstoolButtonBorder = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148,
			145, 140)), labels.getString("LauncherFrame.dcsToolKitCompTitle"));
		dcstoolButtonPanel.setLayout(dcstoolLayout);
		int rows = 4 + (DecodesSettings.instance().showPlatformWizard ? 1 : 0)
			+ (DecodesSettings.instance().showNetlistEditor ? 1 : 0);
		dcstoolLayout.setRows(rows);
		dcstoolLayout.setColumns(1);
		dcstoolButtonPanel.setBorder(dcstoolButtonBorder);

		lrgsStatButton.setText(labels.getString("LauncherFrame.lrgsStatButton"));
		lrgsStatButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				lrgsStatButton_actionPerformed(e);
			}
		});

		browserButton.setText(labels.getString("LauncherFrame.messageBrowserButton"));
		browserButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				browserButton_actionPerformed(e);
			}
		});
		netlistButton.setText(labels.getString("LauncherFrame.networkListMaintButton"));
		netlistButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				netlistButton_actionPerformed(e);
			}
		});
		dbeditButton.setText(labels.getString("LauncherFrame.DECODESDBEditorButton"));
		dbeditButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dbeditButtonPressed();
			}
		});
		platwizButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				platwizButtonButtonPressed();
			}
		});
		toolkitConfigButton.setText(labels.getString("LauncherFrame.DCSToolkitSetupButton"));
		toolkitConfigButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setupPressed();
			}
		});

		dcstoolButtonPanel.add(lrgsStatButton, null);
		dcstoolButtonPanel.add(browserButton, null);

		// Don't add unless showNetlistButton is true
		if (DecodesSettings.instance().showNetlistEditor)
			dcstoolButtonPanel.add(netlistButton, null);
		dcstoolButtonPanel.add(dbeditButton, null);
		if (DecodesSettings.instance().showPlatformWizard)
			dcstoolButtonPanel.add(platwizButton, null);

		dcstoolButtonPanel.add(toolkitConfigButton, null);

		String borderString = labels.getString("LauncherFrame.hydroMetCompTitle");
		if (tsdbType == TsdbType.CWMS)
			borderString = "CWMS Database Components";
		else if (tsdbType == TsdbType.HDB)
			borderString = "HDB Database Components";

		tsdbButtonBorder = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145,
			140)), borderString);
		tsdbButtonPanel.setLayout(tsdbLayout);

		rows = 2 +
			(DecodesSettings.instance().showTimeSeriesEditor ? 1 : 0) +
			(DecodesSettings.instance().showGroupEditor ? 1 : 0) +
			(DecodesSettings.instance().showTestCmputations ? 1 : 0) +
			(DecodesSettings.instance().showAlgorithmEditor ? 1 : 0);
		tsdbLayout.setRows(rows);
		tsdbLayout.setColumns(1);
		tsdbButtonPanel.setBorder(tsdbButtonBorder);

		// ROW 0: Time Series Button
		tseditButton.setIcon(new ImageIcon(installDir + File.separator + "icons" + File.separator
			+ "tsedit48x48.gif"));
		tseditButton.setText("Time Series");
		tseditButton.setHorizontalAlignment(SwingConstants.CENTER);
		tseditButton.setBackground(Color.white);
		tseditButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tseditButtonPressed();
			}
		});
		if (DecodesSettings.instance().showTimeSeriesEditor)
			tsdbButtonPanel.add(tseditButton);

		// ROW 1: Time Series Groups Button
		groupEditButton.setIcon(new ImageIcon(installDir + File.separator + "icons" + File.separator
			+ "groups48x48.png"));
		groupEditButton.setText("Time Series Groups");
		groupEditButton.setHorizontalAlignment(SwingConstants.CENTER);
		groupEditButton.setBackground(Color.white);
		groupEditButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				groupEditButtonPressed();
			}
		});
		if (DecodesSettings.instance().showGroupEditor)
			tsdbButtonPanel.add(groupEditButton, null);

		// ROW 2: Computations Button
		compeditButton.setIcon(new ImageIcon(installDir + File.separator + "icons" + File.separator
			+ "compedit48x48.gif"));
		compeditButton.setText(labels.getString("LauncherFrame.computationsButton"));
		compeditButton.setHorizontalAlignment(SwingConstants.CENTER);
		compeditButton.setBackground(Color.white);
		compeditButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				compeditButtonPressed();
			}
		});
		tsdbButtonPanel.add(compeditButton, null);

		// ROW 3: Run/Test Computations Button
		runcompButton.setIcon(new ImageIcon(installDir + File.separator + "icons" + File.separator
			+ "runcomp48x48.gif"));
		runcompButton.setText(labels.getString("LauncherFrame.testComputationsButton"));
		runcompButton.setHorizontalAlignment(SwingConstants.CENTER);
		runcompButton.setBackground(Color.white);
		runcompButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				runcompButtonPressed();
			}
		});
		if (DecodesSettings.instance().showTestCmputations)
			tsdbButtonPanel.add(runcompButton, null);

		// ROW 4: Process Status Button
		procstatButton.setIcon(new ImageIcon(installDir + File.separator + "icons" + File.separator
			+ "procstat48x48.gif"));
		procstatButton.setText(labels.getString("LauncherFrame.processStatusButton"));
		procstatButton.setHorizontalAlignment(SwingConstants.CENTER);
		procstatButton.setBackground(Color.white);
		procstatButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				procstatButtonPressed();
			}
		});
		tsdbButtonPanel.add(procstatButton, null);

		// ROW 4: Algorithm Edit Button
		algoeditButton.setIcon(new ImageIcon(installDir + File.separator + "icons" + File.separator
			+ "algoedit48x48.gif"));
		algoeditButton.setText(labels.getString("LauncherFrame.algorithmsButton"));
		algoeditButton.setHorizontalAlignment(SwingConstants.CENTER);
		algoeditButton.setBackground(Color.white);
		algoeditButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				algoeditButtonPressed();
			}
		});
		if (DecodesSettings.instance().showAlgorithmEditor)
			tsdbButtonPanel.add(algoeditButton, null);

		fullPanel.setLayout(fullLayout);
		fullPanel.add(dcstoolButtonPanel, new GridBagConstraints(0, 0, 1, 1, .5, .5,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		// Verify if user install the Tempest Tsdb components
		if (tsdbType != TsdbType.NONE)
		{
			fullPanel.add(tsdbButtonPanel, new GridBagConstraints(0, 1, 1, 1, .5, .5,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		}
		contentPane.add(fullPanel, BorderLayout.CENTER);
		setupSaved();
	}

	void retProcButton_actionPerformed(ActionEvent e)
	{
		if (routMonFrame != null)
		{
			routMonFrame.toFront();
			return;
		}
		try
		{
			String cmd = DecodesSettings.instance().browserCmd;
			if (cmd == null)
			{
				if (System.getProperty("os.name").toLowerCase().startsWith("win"))
					cmd = "rundll32 url.dll,FileProtocolHandler";
				else if (System.getProperty("os.name").toLowerCase().startsWith("mac"))
					cmd = "open";
				else
					cmd = "firefox";
			}
			String url = EnvExpander.expand(DecodesSettings.instance().routingMonitorUrl);
			cmd = cmd + " " + url;
			ProcWaiterThread.runBackground(cmd, "Retrieval and Decoding");
		}
		catch (Exception ex)
		{
			Logger.instance().log(Logger.E_FAILURE,
				"Cannot initialize DECODES: '" + ex);
		}
	}

	void lrgsStatButton_actionPerformed(ActionEvent e)
	{
		if (lrgsStatFrame != null)
		{
			lrgsStatFrame.toFront();
			return;
		}
		try
		{
			RtStat rtStat = new RtStat(myArgs);
			lrgsStatFrame = rtStat.getFrame();
			lrgsStatFrame.setExitOnClose(false);
			centerWindow(lrgsStatFrame);
			lrgsStatFrame.addWindowListener(lrgsStatReaper);
			lrgsStatFrame.setVisible(true);
		}
		catch(Exception ex)
		{
			showError(ex.getMessage());
		}
	}

	
	void browserButton_actionPerformed(ActionEvent e)
	{
		if (browserFrame != null)
		{
			browserFrame.toFront();
			return;
		}
		// DB init in progress!
		if (afterDecodesInit != null)
			return;

		try
		{
			final LauncherFrame launcherFrame = this;
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					String hostname = "";
					int port = 16003;
					String user = "";
					launcherFrame.browserFrame = new MessageBrowser(hostname,
						port, user);
					launcherFrame.browserFrame
						.addWindowListener(launcherFrame.browserReaper);
					launcherFrame.browserFrame.startup(100, 100);
				}
			};
			completeDecodesInit();
		}
		catch (DecodesException ex)
		{
			Logger.instance().log(Logger.E_FAILURE,
				"Cannot initialize DECODES: '" + ex);
		}
	}

	void netlistButton_actionPerformed(ActionEvent e)
	{
		if (netlistEditFrame != null)
		{
			netlistEditFrame.toFront();
			return;
		}
		NetlistEditFrame.netlistDir = 
			EnvExpander.expand("$DECODES_INSTALL_DIR/netlist");
		netlistEditFrame = new NetlistEditFrame();
		netlistEditFrame.setStandAlone(false);
		centerWindow(netlistEditFrame);
		netlistEditFrame.addWindowListener(netlistEditReaper);
		netlistEditFrame.setVisible(true);
	}

	void dbeditButtonPressed()
	{
		if (dbEditorFrame != null)
		{
			dbEditorFrame.toFront();
			return;
		}
		// DB init in progress!
		if (afterDecodesInit != null)
			return;

		try
		{
			final LauncherFrame launcherFrame = this;
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					launcherFrame.dbEditorFrame = new DbEditorFrame();
//					launcherFrame.centerWindow(launcherFrame.dbEditorFrame);
					launcherFrame.dbEditorFrame.setExitOnClose(false);
					launcherFrame.dbEditorFrame.addWindowListener(launcherFrame.dbEditorReaper);
					launcherFrame.dbEditorFrame.setVisible(true);
				}
			};
			completeDecodesInit();
		}
		catch (DecodesException ex)
		{
			Logger.instance().log(Logger.E_FAILURE,
				"Cannot initialize DECODES: '" + ex);
		}
	}

	void setupPressed()
	{
		if (toolkitSetupFrame != null)
		{
			toolkitSetupFrame.toFront();
			return;
		}
		toolkitSetupFrame = new DecodesSetupFrame(this);
		toolkitSetupFrame.setExitOnClose(false);
		toolkitSetupFrame.addWindowListener(setupFrameReaper);
		toolkitSetupFrame.setVisible(true);
	}
	
	/**
	 * This method is called after a setup change.
	 * Enable or Disable the time-series buttons depending on the selected database type.
	 */
	void setupSaved()
	{
		boolean dbSupportsTS = DecodesSettings.instance().editDatabaseTypeCode != 
			DecodesSettings.DB_XML;
		tseditButton.setEnabled(dbSupportsTS);
		groupEditButton.setEnabled(dbSupportsTS);
		compeditButton.setEnabled(dbSupportsTS);
		runcompButton.setEnabled(dbSupportsTS);
		algoeditButton.setEnabled(dbSupportsTS);
	}

	void completeDecodesInit() throws DecodesException
	{
		initDecodesFrame = new InitDecodesFrame(getLabels());
		centerWindow(initDecodesFrame);
		initDecodesFrame.setVisible(true);

		/*
		 * Coordinate with GUI thread as follows: - DB init is done in separate
		 * thread, not GUI thread. - This method must return so that splash
		 * frame can be displayed (in the GUI thread). - DB init thread calls
		 * 'doAfterDbInit' when it's finished. - that method runs a Runnable in
		 * the GUI thread to display either the message browser or the db
		 * editor.
		 */
		final LauncherFrame launcherFrame = this;
		Thread initThread = new Thread()
		{
			public void run()
			{
				// On Linux, sometimes the init is so fast that it happens
				// before the splash screen paints. Thus the dismiss comes
				// before the paint and we are left with a splash screen up.
				// The following fixes this by pausing a half-second
				try { sleep(500L); } catch(InterruptedException ex) {}
				
				Platform.configSoftLink = true;

				// Note: Editing is a superset of decoding:
				try
				{
					if (!DecodesInterface.isInitialized())
					{
						doInitDecodes();
						DecodesInterface.initializeForEditing();
					}
				}
				catch (DecodesException ex)
				{
					final String msg = labels
						.getString("LauncherFrame.cannotInitDecodes") + ex;
					Logger.instance().log(Logger.E_FAILURE, msg);
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							JOptionPane.showMessageDialog(null,
								AsciiUtil.wrapString(msg, 60), "Error!",
								JOptionPane.ERROR_MESSAGE);
						}
					});
					afterDecodesInit = null;
				}
				launcherFrame.doAfterDbInit();
			}
		};
		initThread.start();
	}

	public void doAfterDbInit()
	{
		if (initDecodesFrame != null)
			initDecodesFrame.setVisible(false);
		initDecodesFrame = null;
		if (afterDecodesInit != null)
			SwingUtilities.invokeLater(afterDecodesInit);
		afterDecodesInit = null;
	}

	// private void centerWindow(JFrame frame)
	private void centerWindow(Window frame)
	{
		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2,
			(screenSize.height - frameSize.height) / 2);
	}

	static CmdLineArgs cmdLineArgs = new CmdLineArgs(true, "gui.log");

	public static void main(String[] args)
	{

		noCompFilterToken = new BooleanToken("L",
			"Disable Computation List filter (default=enabled)", "",
			TokenOptions.optSwitch, false);
		cmdLineArgs.addToken(noCompFilterToken);
		justPrintVersionToken = new BooleanToken("V", "Print Version and Exit",
			"", TokenOptions.optSwitch, false);
		cmdLineArgs.addToken(justPrintVersionToken);
		cmdLineArgs.parseArgs(args);
		if (justPrintVersionToken.getValue())
		{
			System.out.println(ResourceFactory.instance().startTag());
			System.exit(0);
		}

		Logger.instance().log(
			Logger.E_INFORMATION,
			"DCS Toolkit GUI Version " + ResourceFactory.instance().startTag()
					+ " Starting");

		// Verify if we need to set the Java Locale
		DecodesSettings settings = DecodesSettings.instance();
		LoadResourceBundle.setLocale(settings.language);

		labels = getLabels();

		LauncherFrame frame = new LauncherFrame();

		// compArgs is used as argument only to Computaions and Test Computaions
		// gui.
		frame.compArgs = args;

		// myArgs is used as argument for all gui's launched though DCSToolkit.
		// It does not contain -L.

		ArrayList<String> argsArray = new ArrayList<String>();
		for (int i = 0; i < args.length; i++)
		{
			if (!args[i].equals("-L"))
				argsArray.add(args[i]);
		}
		frame.myArgs = new String[argsArray.size()];
		argsArray.toArray(frame.myArgs);

		frame.setExitOnClose(true);

		// Initialize the 'GuiApp' object used by LRGS & DECODES GUI's:
		GuiApp.setAppName("DCS Toolkit GUI");
		String propFile = EnvExpander.expand(
			"$DECODES_INSTALL_DIR/lrgsgui.properties", System.getProperties());
		try
		{
			GuiApp.loadProperties(propFile);
		}
		catch (FileNotFoundException ex)
		{
			Logger.instance().log(Logger.E_DEBUG1,
				"Cannot read '" + propFile + "' -- will use defaults.");
		}

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}
		frame.setLocation(0, 0);
		
		DecodesInterface.maintainGoesPdt();
		
		frame.setVisible(true);
	}

	public static ResourceBundle getLabels()
	{
		if (labels == null)
		{
			DecodesSettings settings = DecodesSettings.instance();
			// Return the main label descriptions for launchre frame App
			labels = LoadResourceBundle.getLabelDescriptions(
				"decodes/resources/launcherframe", settings.language);
		}
		return labels;
	}

	private void doInitDecodes() throws DecodesException
	{
		// Load the DCS Tool Configuration

		// Initialize minimal DECODES database.
		try
		{
			DecodesInterface.setGUI(true);
			DecodesInterface.initDecodes(
				EnvExpander.expand(
					"$DECODES_INSTALL_DIR/decodes.properties"));
		}
		catch (DecodesException ex)
		{
			String msg = "Error initializing DECODES database: " + ex;
			Logger.instance().log(Logger.E_FAILURE, msg);
			System.err.println(msg);
			// showError(msg);
			throw ex;
		}
	}

	/**
	 * Starts a modal error dialog with the passed message.
	 * 
	 * @param msg
	 *            the error message
	 */
	public void showError(String msg)
	{
		Logger.instance().log(Logger.E_FAILURE, msg);
		JOptionPane.showMessageDialog(this, AsciiUtil.wrapString(msg, 60),
			"Error!", JOptionPane.ERROR_MESSAGE);
	}

	// Time Series Button
	private void groupEditButtonPressed()
	{
		if (groupEditFrame != null)
		{
			groupEditFrame.toFront();
			return;
		}

		// DB init in progress!
		if (afterDecodesInit != null)
			return;
		
		try
		{
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					try
					{
						groupEditFrame = ResourceFactory.instance().getTsdbEditorFrame(myArgs);
						if (groupEditFrame != null)
						{
							groupEditFrame.setExitOnClose(false);
							groupEditFrame.addWindowListener(tsEditorReaper);
						}
					}
					catch (Exception ex)
					{
						groupEditFrame = null;
						String msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.cannotLaunch"),
							labels.getString("LauncherFrame.timeSeriesButton"))
								+ ex;
						Logger.instance().warning(msg);
						System.err.println(msg);
						ex.printStackTrace();
						showError(msg);
					}
				}
			};
			completeDecodesInit();
		}
		catch (DecodesException ex)
		{
			Logger.instance().log(Logger.E_FAILURE, "Cannot initialize DECODES: '" + ex);
		}
	}
	
	protected void tseditButtonPressed()
	{
		if (tseditFrame != null)
		{
			tseditFrame.toFront();
			return;
		}

		// DB init in progress!
		if (afterDecodesInit != null)
			return;

		try
		{
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					try
					{
						TsListMain tsListMain = new TsListMain();
						tsListMain.setExitOnClose(false);
						tsListMain.execute(myArgs);
						tseditFrame = tsListMain.getFrame();
						if (tseditFrame != null)
						{
							tseditFrame.setExitOnClose(false);
							tseditFrame.addWindowListener(tseditReaper);
						}
					}
					catch (Exception ex)
					{
						tseditFrame = null;
						String msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.cannotLaunch"),
							"Time Series Editor")
								+ ex;
						Logger.instance().warning(msg);
						System.err.println(msg);
						ex.printStackTrace();
						showError(msg);
					}
				}
			};
			completeDecodesInit();
		}
		catch (DecodesException ex)
		{
			Logger.instance().log(Logger.E_FAILURE, "Cannot initialize DECODES: '" + ex);
		}
	}

	private void compeditButtonPressed()
	{
		if (compEditFrame != null)
		{
			compEditFrame.toFront();
			return;
		}

		// DB init in progress!
		if (afterDecodesInit != null)
			return;

		try
		{
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					CAPEdit compEdit = new CAPEdit();
					try
					{
						compEdit.execute(compArgs);
						compEditFrame = compEdit.getFrame();
						if (compEditFrame != null)
						{
							compEdit.setExitOnClose(false);
							compEditFrame.addWindowListener(compEditReaper);
						}
					}
					catch(Exception ex)
					{
						compEditFrame = null;
						String msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.cannotLaunch"),
							labels.getString("LauncherFrame.computationsButton"))
								+ ex;
						Logger.instance().warning(msg);
						System.err.println(msg);
						ex.printStackTrace();
						showError(msg);
					}
				}
			};
			completeDecodesInit();
		}
		catch (DecodesException ex)
		{
			Logger.instance().log(Logger.E_FAILURE, "Cannot initialize DECODES: '" + ex);
		}
	}

	private void runcompButtonPressed()
	{
		if (runComputationsFrame != null)
		{
			runComputationsFrame.toFront();
			return;
		}

		// DB init in progress!
		if (afterDecodesInit != null)
			return;

		try
		{
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					try
					{
						RunComputationsFrameTester runComputations = new RunComputationsFrameTester();
						runComputations.execute(compArgs);

						runComputationsFrame = runComputations.getFrame();

						if (runComputationsFrame != null)
						{
							runComputationsFrame.setRunCompFrametester(runComputations);
							runComputationsFrame.setExitOnClose(false);
							runComputationsFrame.addWindowListener(runComputationsReaper);
						}
					}
					catch (Exception ex)
					{
						runComputationsFrame = null;
						String msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.cannotLaunch"),
							labels.getString("LauncherFrame.testComputationsButton"))
								+ ex;
						Logger.instance().warning(msg);
						System.err.println(msg);
						ex.printStackTrace();
						showError(msg);
					}
				}
			};
			completeDecodesInit();
		}
		catch (DecodesException ex)
		{
			Logger.instance().log(Logger.E_FAILURE, "Cannot initialize DECODES: '" + ex);
		}
	}

	private void procstatButtonPressed()
	{
		if (procMonFrame != null)
		{
			procMonFrame.toFront();
			return;
		}

		// DB init in progress!
		if (afterDecodesInit != null)
			return;
		
		try
		{
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					try
					{
						ProcessMonitor procMon = new ProcessMonitor();
						procMon.execute(compArgs);
						procMonFrame = procMon.getFrame();
						if (procMonFrame != null)
						{
							procMon.setExitOnClose(false);
							procMonFrame.addWindowListener(procMonReaper);
						}
					}
					catch (Exception ex)
					{
						procMonFrame = null;
						String msg = LoadResourceBundle.sprintf(
							labels.getString("LauncherFrame.cannotLaunch"),
							labels.getString("LauncherFrame.processStatusButton"))
								+ ex;
						Logger.instance().warning(msg);
						System.err.println(msg);
						ex.printStackTrace();
						showError(msg);
					}
				}
			};
			completeDecodesInit();
		}
		catch (DecodesException ex)
		{
			Logger.instance().log(Logger.E_FAILURE, "Cannot initialize DECODES: '" + ex);
		}
	}

	private void algoeditButtonPressed()
	{
		if (algorithmWizFrame != null)
		{
			algorithmWizFrame.toFront();
			return;
		}

		try
		{
			AlgorithmWizard algoWiz = new AlgorithmWizard();
			algoWiz.execute(myArgs);
			algorithmWizFrame = algoWiz.getFrame();
			if (algorithmWizFrame != null)
			{
				algoWiz.setExitOnClose(false);
				algorithmWizFrame.addWindowListener(algorithmWizReaper);
			}
		}
		catch (Exception ex)
		{
			algorithmWizFrame = null;
			String msg = LoadResourceBundle.sprintf(
				labels.getString("LauncherFrame.cannotLaunch"),
				labels.getString("LauncherFrame.algorithmsButton"))
					+ ex;
			Logger.instance().warning(msg);
			System.err.println(msg);
			ex.printStackTrace();
			showError(msg);
		}
	}

	private void platwizButtonButtonPressed()
	{
		if (platformWizFrame != null)
		{
			platformWizFrame.toFront();
			return;
		}

		// DB init in progress!
		if (afterDecodesInit != null)
			return;
		try
		{
			final LauncherFrame launcherFrame = this;
			afterDecodesInit = new Runnable()
			{
				public void run()
				{
					// PlatformWizard platWiz = new PlatformWizard();
					PlatformWizard.resetInstance();
					PlatformWizard platWiz = PlatformWizard.instance();
					// NOTE NOTE
					// If we do not set this to false the Define Platform
					// Sensor Panel does not work - It does not let me add
					// a configuration
					Platform.configSoftLink = false;
					decodes.db.Database db = decodes.db.Database.getDb();
					db.platformConfigList.countPlatformsUsing();
					platWiz.show();
					launcherFrame.platformWizFrame = platWiz.getFrame();
					if (launcherFrame.platformWizFrame != null)
					{
						launcherFrame.platformWizFrame.exitOnClose = false;
						launcherFrame.platformWizFrame
							.addWindowListener(launcherFrame.platformWizReaper);
					}
				}
			};
			completeDecodesInit();
			// decodes.db.Database db = decodes.db.Database.getDb();
			// Platform.configSoftLink = false;
			// db.platformConfigList.countPlatformsUsing();
			// PlatformWizard platWiz = new PlatformWizard();
			// //PlatformWizard platWiz = PlatformWizard.instance();
			// platWiz.show();
			// platformWizFrame = platWiz.getFrame();
			// if (platformWizFrame != null)
			// {
			// platformWizFrame.exitOnClose = false;
			// platformWizFrame.addWindowListener(platformWizReaper);
			// }
		}
		catch (Exception ex)
		{
			platformWizFrame = null;
			String msg = LoadResourceBundle.sprintf(
				labels.getString("LauncherFrame.cannotLaunch"),
				labels.getString("LauncherFrame.platformWizardButton"))
					+ ex;
			Logger.instance().warning(msg);
			System.err.println(msg);
			ex.printStackTrace();
			showError(msg);
		}
	}

}
