import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

//Name: Uvwie Omafume, Dayo Asaolu, Fahad Ijaz
//Student Number: 201658754, 201460649, 201158839
//This Program shows the result of the application of Mean, Median, Gaussian, and kuwuhara filter on an image.
//It displays the source image as well as the result of the filter application on the target. 
//The original image can be updated to the result of the original image.

//BUGS:(i) The mean filter does not have incremental or separable property implemented.
//		

// Main class
public class SmoothingFilter extends Frame implements ActionListener {
	BufferedImage input, output;
	ImageCanvas source, target;
	TextField texSigma;
	int width, height;
	// Constructor
	public SmoothingFilter(String name) {
		super("Smoothing Filters");
		// load image
		try {
			input = ImageIO.read(new File(name));
                        output = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(output);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Add noise");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 mean");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Sigma:"));
		texSigma = new TextField("1", 1);
		controls.add(texSigma);
		button = new Button("5x5 Gaussian");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 median");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 Kuwahara");
		button.addActionListener(this);
		controls.add(button);
                button = new Button("Update Source");
                controls.add(button);
                button.addActionListener(this);
                
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+200, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// example -- add random noise
		if ( ((Button)e.getSource()).getLabel().equals("Add noise") ) {
			Random rand = new Random();
			int dev = 64;
			for ( int y=0, i=0 ; y<height ; y++ )
				for ( int x=0 ; x<width ; x++, i++ ) {
					Color clr = new Color(source.image.getRGB(x, y));
					int red = clr.getRed() + (int)(rand.nextGaussian() * dev);
					int green = clr.getGreen() + (int)(rand.nextGaussian() * dev);
					int blue = clr.getBlue() + (int)(rand.nextGaussian() * dev);
					red = red < 0 ? 0 : red > 255 ? 255 : red;
					green = green < 0 ? 0 : green > 255 ? 255 : green;
					blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
					source.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
	
				}
			source.repaint();
		}
                
                
                if ( ((Button)e.getSource()).getLabel().equals("5x5 Gaussian") ) {
                    double[] g_kernel = guassian1D_kernel(Double.parseDouble (texSigma.getText()));
                    int g_kernel_radius = g_kernel.length/2;
					int r =0, g=0, b=0;
					
					for (int i = 0; i< width; i++){
						for (int j =0; j< height; j++){
							Color clr = new Color(source.image.getRGB(i, j));
							r = clr.getRed();
							g = clr.getGreen();
							b = clr.getBlue();
							System.out.print("====="+r+" "+ g + " " +b+" ");
						}
					}
                    //Apply 1-D Gaussian Filter horizontally
                    //For all pixels
                    for(int i=0; i <height; i++){
                        for(int j=0; j<width; j++){

                            double redSum = 0;
                            double greenSum = 0;
                            double blueSum = 0;
                            //Place the centre of the kernel on current pixel
                            for(int a=-2; a <=2; a++){
                                
                                int red = 0;
                                int green = 0;
                                int blue = 0;
                                //If a pixel neighbor is out of bound use the nearest one
                                try{
                                    Color clr = new Color(source.image.getRGB(i, j+a));
                                    red = clr.getRed();
                                    green = clr.getGreen();
                                    blue = clr.getBlue();
                                    
                                }catch(Exception x){
                                    Color clr = new Color(source.image.getRGB(i, j));
                                    red = clr.getRed();
                                    green = clr.getGreen();
                                    blue = clr.getBlue();
                                }
                                //Multiply pixel intensity by corresponding kernel intensity.
                                //Add to the redSum
                                redSum += red * g_kernel[a+g_kernel_radius];
                                greenSum += green * g_kernel[a+g_kernel_radius];
                                blueSum += blue * g_kernel[a+g_kernel_radius];
                                
                            }
                            redSum = (redSum < 0) ? 0 : (redSum > 255) ? 255 : redSum;
                            greenSum = (greenSum < 0) ? 0 : (greenSum > 255) ? 255 : greenSum;
                            blueSum = (blueSum < 0) ? 0 : (blueSum > 255) ? 255 : blueSum;
                            
                            target.image.setRGB(i, j, (new Color((int)redSum, (int)greenSum, (int)blueSum)).getRGB());
                            
                        }
                    }
                    
                    
                    //Apply 1D Gaussian Filter vertically
                    for(int i=0; i <width; i++){
                        for(int j=0; j<height; j++){
                            double redSum = 0;
                            double greenSum = 0;
                            double blueSum = 0;
                            
                            //Place the centre of the kernel on current pixel
                            for(int a=-2; a <=2; a++){
                                
                                int red = 0;
                                int green = 0;
                                int blue = 0;
                                //If a pixel neighbor is out of bound use the nearest one
                                try{
                                    Color clr = new Color(source.image.getRGB(i, j+a));
                                    red = clr.getRed();
                                    green = clr.getGreen();
                                    blue = clr.getBlue();
                                    
                                }catch(Exception x){
                                    Color clr = new Color(source.image.getRGB(i, j));
                                    red = clr.getRed();
                                    green = clr.getGreen();
                                    blue = clr.getBlue();
                                }
                                //Multiply pixel intensity by corresponding kernel intensity.
                                //Add to the redSum
                                redSum += red * g_kernel[a+g_kernel_radius];
                                greenSum += green * g_kernel[a+g_kernel_radius];
                                blueSum += blue * g_kernel[a+g_kernel_radius];
                                
                            }
                            redSum = (redSum < 0) ? 0 : (redSum > 255) ? 255 : redSum;
                            greenSum = (greenSum < 0) ? 0 : (greenSum > 255) ? 255 : greenSum;
                            blueSum = (blueSum < 0) ? 0 : (blueSum > 255) ? 255 : blueSum;
                            
                            target.image.setRGB(i, j, (new Color((int)redSum, (int)greenSum, (int)blueSum)).getRGB());
                        }
                    }
          
                    target.repaint();
		}
        		if ( ((Button)e.getSource()).getLabel().equals("5x5 mean") ) 
        		{

        			for( int q=0; q<height;q++)
        			{

        				for(int p=0; p<width;p++)
        				{
        					int sum_red=0;
        					int sum_blue=0;
        					int sum_green=0;
        					for(int v=-2; v<=2;v++)
        					{
        						for(int u=-2;u<=2;u++)
        						{
        							try
        							{
        								Color clr_new = new Color(source.image.getRGB(q+v, p+u));
        								sum_red+=clr_new.getRed();
        								sum_blue+=clr_new.getBlue();
        								sum_green+=clr_new.getGreen();
        							}
        							catch(Exception x)
        							{
        								Color clr_new = new Color(source.image.getRGB(q, p));
        								sum_red+=clr_new.getRed();
        								sum_blue+=clr_new.getBlue();
        								sum_green+=clr_new.getGreen();
        							}

        						}
        					}
        					sum_red= sum_red/25;
        					sum_blue= sum_blue/25;
        					sum_green= sum_green/25;
        					target.image.setRGB(q, p, (new Color(sum_red, sum_green, sum_blue).getRGB()));

        					
        				}
        				
        		
        			}
        			target.repaint();
        		}
        		if ( ((Button)e.getSource()).getLabel().equals("5x5 median") ) 
        		{
        			int[][] red=new int [height][width];
        			int[][] blue=new int[height][width];
        			int[][] green=new int[height][width];
        			


        			for ( int y=0, i=0 ; y<height ; y++ )
        				for ( int x=0 ; x<width ; x++, i++ ) 
        				{
        					Color clr = new Color(source.image.getRGB(x, y));
        					red[y][x]=clr.getRed();
        					blue[y][x]=clr.getBlue();
        					green[y][x]=clr.getGreen();
        					

        				} 
        		
        			for ( int q=0; q<height ; q++ ){
        				for ( int p=0 ; p<width ; p++) {

        					int[] red_median = new int[25];
        					int[] green_median = new int[25];
        					int[] blue_median = new int[25];
        					
        					int count_index=0;

        					for (int u = -2; u<=2; u++){
        						for(int v = -2; v <=2; v++){

        								
        							int r = q-u;
        							r=r<0?0:r>255?255:r;
        							int s = p-v;
        							s=s<0?0:s>255?255:s;
        							red_median[count_index] = red[r][s];
        							green_median[count_index] = green[r][s];
        							blue_median[count_index] = blue[r][s];
        							count_index=count_index+1;




        						}
        					}
        					Arrays.sort(red_median);
        					Arrays.sort(green_median);
        					Arrays.sort(blue_median);
        		
        					int index=(count_index%2 == 0)?count_index/2-1:count_index/2;

        					target.image.setRGB(p, q, (new Color(red_median[index], green_median[index], blue_median[index]).getRGB()));
        		}
        			}target.repaint();
        		}
        		if ( ((Button)e.getSource()).getLabel().equals("5x5 Kuwahara") ) {
        			int [][] newRed = new int[width][height];
        			int [][] newGreen = new int[width][height];
        			int [][] newBlue  = new int[width][height];

        			int [][] Red_kernel = new int[5][5];
        			int [][] Blue_kernel = new int[5][5];
        			int [][] Green_kernel = new int[5][5];
        			
        			int R_mean = 0, G_mean =0, B_mean = 0;
        			for ( int y=0; y<height ; y++ ){
        				for ( int x=0 ; x<width ; x++) {
        					Red_kernel = Red5x5(x, y);
        					R_mean = LowestMean(Red_kernel);
        					newRed[y][x] = R_mean;


        					Green_kernel = Green5x5(x,y);
        					G_mean = LowestMean(Green_kernel);
        					newGreen[y][x] = G_mean;

        					Blue_kernel = Blue5x5(x, y);
        					B_mean = LowestMean(Blue_kernel);
        					newBlue[y][x] = B_mean;
        				}
        			}	
        				

        			for ( int y=0; y<height ; y++ ) {
        				for ( int x=0 ; x<width ; x++) {
        					int red = newRed[y][x];
        					int green = newGreen[y][x];
        					int blue = newBlue[y][x];
        					red = red < 0 ? 0 : red > 255 ? 255 : red;
        					green = green < 0 ? 0 : green > 255 ? 255 : green;
        					blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
        					target.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
        				}
        			}
        			target.repaint();
        		}
                        
                        
                
                if ( ((Button)e.getSource()).getLabel().equals("Update Source") ) {
                    source.image = target.image;
                    source.repaint();
                }
                
	}
        
        
        public double[] guassian1D_kernel(double weight){
            double[] kernel = new double[5];
            double euler_part = 0;
            double pi_part = 0;
            double sum = 0;
            int radius = 2;
            for(int i=-radius; i <= radius; i++){
                double number = (double)(Math.pow(i,2)/(2*Math.pow(weight,2)));
                euler_part =(double)Math.exp(-number);
                pi_part = (double)(Math.sqrt(2*Math.PI)*weight);
                double g_output = (double)((1.0/pi_part)*euler_part);
                kernel[i+radius] = g_output;
                sum += g_output;
            }
            
            System.out.println("The 1D gaussian Kernel:");
            for(int i=0; i < 5; i++){
                kernel[i]= (double)(kernel[i]/sum);
                System.out.print(kernel[i]+"  ");
            }
            System.out.println();
            return kernel;
        }
    	public int LowestMean(int [][] kernel){
    		int smallest_mean = 0,  total = 0, count=0;
    		double mean = 0, variance = 0, val_mean = 0, high_V=700000;



    		for (int kX=0; kX < 3; kX = kX+2){
    			for (int ky=0; ky<3; ky = ky+2){
    				total = 0;
    				for (int i=kX; i<kX+3; i++){
    					for (int j = ky; j< ky+3; j++){
    						total += kernel[i][j];
    						count = count +1;
    					}
    				}
    				mean = total/count;
    				count = 0;
    				variance = 0;

    				for (int i=kX; i<kX+3; i++){
    					for (int j = ky; j< ky+3; j++){
    						val_mean = kernel[i][j] - mean;
    						variance += Math.pow(val_mean, 2);
    					}
    				}
    				
    				if (high_V>variance){
    					high_V = variance;
    					smallest_mean = (int) mean;
    				}
    			}
    		}
    		return smallest_mean;
    	}
    	


    	public int [][] Red5x5(int x, int y){
    		int list1 [][] = new int[5][5];
    		for (int i = y-2, a = 0; i <= y+2; i++, a++){
    			for (int j = x-2, b = 0; j <= x+2; j++, b++){
    				int yaxis = i < 0 ? 0 :i > 255 ? 255 : i;
    				int xaxis = j < 0 ? 0 :j > 255 ? 255 : j;
    				Color clr = new Color(source.image.getRGB(xaxis,yaxis));

    				list1[a][b] = clr.getRed();
    			}
    		}
    		return list1;
    		
    	}


    	public int [][] Green5x5(int x, int y){
    		int list1 [][] = new int[5][5];
    		for (int i = y-2, a = 0; i <= y+2; i++, a++){
    			for (int j = x-2, b = 0; j <= x+2; j++, b++){	
    				int yaxis = i < 0 ? 0 :i > 255 ? 255 : i;
    				int xaxis = j < 0 ? 0 :j > 255 ? 255 : j;
    				Color clr = new Color(source.image.getRGB(xaxis,yaxis));
    				list1[a][b] = clr.getGreen();
    			}
    		}
    		return list1;
    	}

    	public int [][] Blue5x5(int x, int y){
    		int list1 [][] = new int[5][5];
    		for (int i = y-2, a = 0; i <= y+2; i++, a++){
    			for (int j = x-2, b = 0; j <= x+2; j++, b++){
    				int yaxis = i < 0 ? 0 :i > 255 ? 255 : i;
    				int xaxis = j < 0 ? 0 :j > 255 ? 255 : j;
    				Color clr = new Color(source.image.getRGB(xaxis,yaxis));
    				list1[a][b] = clr.getBlue();
    			}
			}
			
    		return list1;
    	}
        
        
	public static void main(String[] args) {
		new SmoothingFilter(args.length==1 ? args[0] : "baboon.png");
	}
}