/**
 * 
 */
package postprocessing;

//import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
//import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import odEstimation.LinkCosts;
import odEstimation.LinkCounts;
import odEstimation.ODCounts;
import odEstimation.ODPairs;
import odEstimation.RouteCosts;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import parser.GraphParser;

/**
 * @author Iacer
 *
 */
public class VisualizationApplet extends JApplet
{
	/**
	 * The applet background color
	 */
	private static final Color DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
	
	/**
	 * The applet dimensions (x,y)
	 */
	private static final Dimension DEFAULT_SIZE = new Dimension( 1024, 768 );
	
	/**
	 * JGraphT to JGraph adapter
	 */
	private JGraphModelAdapter m_jgAdapter;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5769416905387407214L;
	
	HashMap <String, String> strVertices = null;
	
	private LinkCounts linkCounts = null;
	private ODCounts odCounts = null;
	private RouteCosts routeCosts = null;
	private LinkCosts linkCosts = null;
	private ODPairs odPairs = null;
	private DefaultDirectedWeightedGraph <String, DefaultWeightedEdge> problem = null;
	
	/**
	 * Constructor
	 */
	public VisualizationApplet()
	{
		this.problem = new DefaultDirectedWeightedGraph <String, DefaultWeightedEdge>
				(DefaultWeightedEdge.class);
	}
	
	/**
	 * Constructor
	 */
	public VisualizationApplet(DirectedGraph g)
	{
		this.problem = (DefaultDirectedWeightedGraph)g;
	}
	
	public void init()
	{
		final JButton b = new JButton( "File" );
		b.addActionListener(
				new ActionListener()
				{
					public void actionPerformed( ActionEvent ae )
					{
						JFileChooser fc = new JFileChooser();
						fc.setCurrentDirectory( new File( "/home/iacer/workspace/FLPM2_cplex/src/instances/v6/" ) );
						int returnVal = fc.showSaveDialog( VisualizationApplet.this );
						if ( returnVal == JFileChooser.APPROVE_OPTION )
						{
							File aFile = fc.getSelectedFile();
							openFile(aFile);
							
							// Create a JGraphT graph
							ListenableGraph lg = new ListenableDirectedGraph( problem );

							// create a visualization using JGraph, via an adapter
							m_jgAdapter = new JGraphModelAdapter( problem );
							
							// Create a visualization using JGraph, via the adapter
							JGraph jgraph = new JGraph( m_jgAdapter );
							
							adjustDisplaySettings( jgraph );
							getContentPane(  ).add( jgraph );
							resize( DEFAULT_SIZE );
							
							// Remove the button from the panel
							getContentPane().remove( b );
							
							// Position the graph's elements
							positionGraph();
						}
					}
				}
		);
		getContentPane().add( b );
	}
	
	private void adjustDisplaySettings( JGraph jg ) {
		jg.setPreferredSize( DEFAULT_SIZE );
		
		Color c = DEFAULT_BG_COLOR;
		String colorStr = null;
		
		try {
			colorStr = getParameter( "bgcolor" );
		} catch( Exception e ) {}
		
		if( colorStr != null ) {
			c = Color.decode( colorStr );
		}
		
		jg.setBackground( c );
	}
	
	private void positionGraph() {
		Iterator <?> itVertices = problem.vertexSet().iterator();
		while (itVertices.hasNext())
		{
			String strVertex = (String) itVertices.next();
			
			// Position them randomly within the screen limits
			Random generator = new Random();
			int randomX = generator.nextInt( 1024 );
			int randomY = generator.nextInt( 768 );
			
			positionVertexAt(strVertex, randomX, randomY);
		}
	}
	
	private void positionVertexAt(Object vertex, int x, int y)
	{
		DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
		AttributeMap attr = cell.getAttributes();
		Rectangle2D bounds = GraphConstants.getBounds(attr);
		
		Rectangle2D newBounds = new Rectangle2D.Double(
				x,
				y,
				bounds.getWidth(),
				bounds.getHeight()
		);
		
		GraphConstants.setBounds(attr, newBounds);
		
		AttributeMap cellAttr = new AttributeMap();
		cellAttr.put(cell, attr);
		m_jgAdapter.edit(cellAttr, null, null, null);
	}
	
	/**
	 * Opens the file passed as a parameter and interprets it
	 * @param file File
	 */
	private void openFile(File file)
	{
		// Create the parser object
		GraphParser parser = new GraphParser();
		parser.setStrVertices(strVertices);
		parser.parse(file);
		
		// Get the model objects from the parser
		strVertices = parser.getStrVertices();
		linkCounts = parser.getLinkCounts();
		odCounts = parser.getOdCounts();
		routeCosts = parser.getRouteCosts();
		linkCosts = parser.getLinkCosts();
		odPairs = parser.getOdPairs();
		problem = parser.getG();
	}
}
