package postprocessing;
/*
VisAD Tutorial
Copyright (C) 2000 Ugo Taddei
*/

// Import needed classes

import visad.*;
import visad.java2d.DisplayImplJ2D;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.*;

/**
  VisAD Tutorial example 3_01
  A function pixel_value = f(row, column)
  with MathType ( (row, column) -> pixel ) is plotted
  The domain set is an Integer1DSet
  Run program with "java P3_01"
 */


public class P3_01{

// Declare variables
  // The quantities to be displayed in x- and y-axes: row and column
  // The quantity pixel will be mapped to RGB color

  private RealType row, column, pixel;

  // A Tuple, to pack row and column together, as the domain

  private RealTupleType domain_tuple;


  // The function ( (row, column) -> pixel )
  // That is, (domain_tuple -> pixel )

  private FunctionType func_dom_pix;


   // Our Data values for the domain are represented by the Set

  private Set domain_set;


  // The Data class FlatField

  private FlatField vals_ff;

  // The DataReference from data to display

  private DataReferenceImpl data_ref;

  // The 2D display, and its the maps

  private DisplayImpl display;
  private ScalarMap rowMap, colMap, pixMap;


  public P3_01(String []args)
    throws RemoteException, VisADException {

    // Create the quantities
    // Use RealType(String name);

    row = RealType.getRealType("ROW");
    column = RealType.getRealType("COLUMN");

    domain_tuple = new RealTupleType(row, column);

    pixel = RealType.getRealType("PIXEL");


   // Create a FunctionType (domain_tuple -> pixel )
   // Use FunctionType(MathType domain, MathType range)

    func_dom_pix = new FunctionType( domain_tuple, pixel);

    // Create the domain Set, with 5 columns and 6 rows, using an
    // Integer2DSet(MathType type, int lengthX, lengthY)

    int NCOLS = 5;
    int NROWS = 6;

    domain_set = new Integer2DSet(domain_tuple, NROWS, NCOLS );


    // Our pixel values, given as a float[6][5] array

    float[][] pixel_vals = new float[][]{{0, 6, 12, 18, 24},
    					 {1, 7, 12, 19, 25},
					 {2, 8, 14, 20, 26},
					 {3, 9, 15, 21, 27},
					 {4, 10, 16, 22, 28},
					 {5, 11, 17, 23, 29}  };

    // We create another array, with the same number of elements of
    // pixel_vals[][], but organized as float[1][ number_of_samples ]

    float[][] flat_samples = new float[1][NCOLS * NROWS];

    // ...and then we fill our 'flat' array with the original values
    // Note that the pixel values indicate the order in which these values
    // are stored in flat_samples

    for(int c = 0; c < NCOLS; c++)
      for(int r = 0; r < NROWS; r++)

	flat_samples[0][ c * NROWS + r ] = pixel_vals[r][c];


    // Create a FlatField
    // Use FlatField(FunctionType type, Set domain_set)

    vals_ff = new FlatField( func_dom_pix, domain_set);

     // ...and put the pixel values above into it

    vals_ff.setSamples( flat_samples );

    // Create Display and its maps

    // A 2D display

    display = new DisplayImplJ2D("display1");

    // Get display's graphics mode control and draw scales

    GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
    dispGMC.setScaleEnable(true);


    // Create the ScalarMaps: column to XAxis, row to YAxis and pixel to RGB
    // Use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)

    colMap = new ScalarMap( column, Display.XAxis );
    rowMap = new ScalarMap( row,    Display.YAxis );
    pixMap = new ScalarMap( pixel,  Display.RGB );

    // Add maps to display

    display.addMap( colMap );
    display.addMap( rowMap );
    display.addMap( pixMap );


    // Create a data reference and set the FlatField as our data

    data_ref = new DataReferenceImpl("data_ref");

    data_ref.setData( vals_ff );

    // Add reference to display

    display.addReference( data_ref );


    // Create application window and add display to window

    JFrame jframe = new JFrame("VisAD Tutorial example 3_01");
    jframe.getContentPane().add(display.getComponent());


    // Set window size and make it visible

    jframe.setSize(300, 300);
    jframe.setVisible(true);


  }


  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new P3_01(args);
  }

} //end of Visad Tutorial Program 3_01
