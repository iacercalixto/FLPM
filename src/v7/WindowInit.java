/**
 * 7th version of the FLPSOD, including Sherali et al(1994)
 */
package v7;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import v7.FLIPSOD.ArcCostUpdatingStrategyName;
import v7.FLIPSOD.BPRFunctionName;
import v7.FLIPSOD.MethodClassName;
import v7.FLIPSOD.SimpleEstimationModelName;

/**
 * @author Iacer Calixto
 */
public class WindowInit extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	static private final String newline = "\n";
	JButton openButton, saveButton;
	JTextArea log;
	JFileChooser fc;
	MethodName method;
	
	public enum MethodName
	{
		FLIPSOD,
		SHERALI,
		BOTH
	}
	
	/**
	 * Class constructor
	 */
	public WindowInit()
	{
		super(new BorderLayout());
		
		// Estimate OD matrices using both FLIPSOD and Sherali's methods
		this.method = MethodName.FLIPSOD;
		
		//Create the log first, because the action listeners
		//need to refer to it.
		this.log = new JTextArea(5,20);
		this.log.setMargin(new Insets(5,5,5,5));
		this.log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(this.log);
		
		//Create a file chooser
		//this.fc = new JFileChooser("C:/Iacer/eclipse-workspace/FLPM2_cplex/src/instances/v6");
		this.fc = new JFileChooser("/home/iacer/workspace/FLPM2_cplex/src/instances/v7");
		
		//Uncomment one of the following lines to try a different
		//file selection mode.  The first allows just directories
		//to be selected (and, at least in the Java look and feel,
		//shown).  The second allows both files and directories
		//to be selected.  If you leave these lines commented out,
		//then the default mode (FILES_ONLY) will be used.
		//
		//fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		//Create the open button.  We use the image from the JLF
		//Graphics Repository (but we extracted it from the jar).
		this.openButton = new JButton("Open a File...", createImageIcon("../images/open16.png"));
		this.openButton.addActionListener(this);

		//Create the save button.  We use the image from the JLF
		//Graphics Repository (but we extracted it from the jar).
		this.saveButton = new JButton("Save a File...", createImageIcon("../images/save16.png"));
		this.saveButton.addActionListener(this);

		//For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); //use FlowLayout
		buttonPanel.add(this.openButton);
		buttonPanel.add(this.saveButton);

		//Add the buttons and the log to this panel.
		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Method activated when an event is thrown in the JPanel
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.openButton)
		{
			// Handle open button action.
			
			int returnVal = fc.showOpenDialog(WindowInit.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = this.fc.getSelectedFile();
				
				// This is where a real application would open the file.
				this.log.append("Opening: " + file.getName() + "." + WindowInit.newline);
				
				// The next lines opens the file and do the estimation
				if (method.equals(MethodName.BOTH))
				{
					doEstimationFLIPSOD(file);
					doEstimationSherali(file);
				}
				
				if (method.equals(MethodName.FLIPSOD))
					doEstimationFLIPSOD(file);
				
				if (method.equals(MethodName.SHERALI))
					doEstimationSherali(file);
				
				System.exit(0);
			} else
			{
				this.log.append("Open command cancelled by user." + WindowInit.newline);
			}
			this.log.setCaretPosition(this.log.getDocument().getLength());
		} else if (e.getSource() == this.saveButton)
		{
			//Handle save button action.
			
			int returnVal = this.fc.showSaveDialog(WindowInit.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				//File file = fc.getSelectedFile();
				// This is where a real application would save the file.
				// this.log.append("Saving: " + file.getName() + "." + this.newline);
			} else
			{
				this.log.append("Save command cancelled by user." + WindowInit.newline);
			}
			this.log.setCaretPosition(this.log.getDocument().getLength());
		}
	}
	
	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 * 
	 * @param path the path to the image in the disk
	 * @return the image icon object
	 */
	protected static ImageIcon createImageIcon(String path)
	{
		java.net.URL imgURL = WindowInit.class.getResource(path);
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 */
	private static void createAndShowGUI()
	{
		//Create and set up the window.
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Add content to the window.
		frame.add(new WindowInit());
		
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE); 
				createAndShowGUI();
			}
		});
	}
	
	/**
	 * Called to do estimation using the FLIPSOD method
	 * 
	 * @param file the file name to use as input to the method
	 */
	private void doEstimationFLIPSOD(File file)
	{
		String filePath;
		
		// Estimate using FLIPSOD
		FLIPSOD flipsod = new FLIPSOD();
		
		// Which version of the method to use?
		flipsod.setMethodClassName(
				MethodClassName.FuzzyLPModel_Original
		);
		
		// Do not use the errors in the input file.
		// Rather, use the errors described in the
		// ErrorValueMapping object
		flipsod.setUseCodedErrors( true );
		flipsod.setUseGradientErrors( false );
		flipsod.setA( 1f );
		flipsod.setB( 1f );
		flipsod.setD( 1f );
		flipsod.setE( 1f );
		
		//flipsod.setA( 0.1000000f );
		//flipsod.setB( 0.1000000f );
		//flipsod.setD( 0.1000000f );
		//flipsod.setE( 0.2500000f );
		
		// What is the smoothing multiplier to use
		// in order to avoid oscillations
		flipsod.setSmoothingMultiplier( 0.2 );
		
		// What is the maximum number of routes per OD pair
		// to take into consideration
		flipsod.setNumberOfRoutesPerODPair( 5 );
		
		// What is the maximum difference between two lambdas
		// for them to still be considered equal?
		flipsod.setLambdaMaximumError( 0.1 );
		
		// What is the maximum difference between two sets of
		// arc flows for them to still be considered equal?
		flipsod.setArcFlowsMaximumError( 1 );
		
		// What is the maximum difference between two OD matrices
		// for them to still be considered equal?
		flipsod.setOdMatrixMaximumError( 150 );
		
		// Calculate user-equilibrium or system optimal
		// assignment
		flipsod.setUserEquilibrium( true );
		
		// Use mixed integer programming or continuous
		// optimization?
		flipsod.setMixedIntegerProgramming( false );
		
		// Use M3 or M8 to do the one-step estimation?
		flipsod.setSimpleEstimationModel(
				SimpleEstimationModelName.M8
		);
		
		// Shall we calculate the arc costs using PETGyn
		// or using the BPR function?
		flipsod.setArcCostUpdatingStrategy(
				ArcCostUpdatingStrategyName.BPR
		);
		
		// Use the BPR function with constants calibrated
		// to the Brazilian scenario
		flipsod.setBPRFunctionName(
				BPRFunctionName.BPR
		);
		
		// Set the input file to use and load it
		flipsod.setInputFile(file);
		flipsod.loadFile();
		
		//flipsod.createMatrix();
		//if (true) return;
		
		filePath = "/home/iacer/workspace/FLPM2_cplex/newEstimation/";
		
		try {
			// Do the estimation
			//flipsod.doEstimation(filePath);
			flipsod.iterateFLIPSODUsingPETGynFindMinimalErrorValues();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Called to do estimation using the Sherali LP(TT) method
	 * 
	 * @param file the file name to use as input to the method
	 */
	private void doEstimationSherali(File file)
	{
		// Estimate using Sherali
		Sherali sherali = new Sherali();
		
		sherali.createMatrix();
		if (true) return;
		
		if (sherali.openFile(file))
			sherali.iterateSheraliBilevelOptimization();
		else
			sherali.iterateSheraliBilevelOptimization();
	}
}