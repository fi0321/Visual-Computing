import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
//Group 6
//Name: Uvwie Omafume, Dayo Asaolu, Fahad Ijaz
//Student Number: 201658754, 201460649, 201158839
//We're not sure if the Response function is working correctly, hence it might effect other functions dependent on it.
public class CornerDetection extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	double sensitivity=.1;
	int threshold=20;
	ImageCanvas source, target;
	CheckboxGroup metrics = new CheckboxGroup();
	// Constructor
	public CornerDetection(String name) {
		super("Corner Detection");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		target = new ImageCanvas(width, height);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Derivatives");
		button.addActionListener(this);
		controls.add(button);
		// Use a slider to change sensitivity
		JLabel label1 = new JLabel("sensitivity=" + sensitivity);
		controls.add(label1);
		JSlider slider1 = new JSlider(1, 25, (int)(sensitivity*100));
		slider1.setPreferredSize(new Dimension(50, 20));
		controls.add(slider1);
		slider1.addChangeListener(changeEvent -> {
			sensitivity = slider1.getValue() / 100.0;
			label1.setText("sensitivity=" + (int)(sensitivity*100)/100.0);
		});
		button = new Button("Corner Response");
		button.addActionListener(this);
		controls.add(button);
		JLabel label2 = new JLabel("threshold=" + threshold);
		controls.add(label2);
		JSlider slider2 = new JSlider(0, 100, threshold);
		slider2.setPreferredSize(new Dimension(50, 20));
		controls.add(slider2);
		slider2.addChangeListener(changeEvent -> {
			threshold = slider2.getValue();
			label2.setText("threshold=" + threshold);
		});
		button = new Button("Thresholding");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Non-max Suppression");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Display Corners");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(Math.max(width*2+100,850), height+110);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
            
            BufferedImage visualise_DoG;
            BufferedImage Ix2;
            BufferedImage Iy2;
            BufferedImage IxIy;
                    
            // generate Moravec corner detection result
            if ( ((Button)e.getSource()).getLabel().equals("Derivatives") ){
                //derivatives();
                ConverttoGreyscale();
                gaussian();
                BufferedImage Ix = derivativeOnX(target.image);
                BufferedImage Iy = derivativeOnY(target.image);

                Ix2 = productImage(Ix,Ix);
                Iy2 = productImage(Iy,Iy);
                IxIy = productImage(Ix,Iy);

                visualise_DoG = SetChannel3(Ix2,IxIy,Iy2); 

                target.resetImage(visualise_DoG);
            }
            
            if ( ((Button)e.getSource()).getLabel().equals("Corner Response") ){
                
                //ConverttoGreyscale();
                gaussian();
                BufferedImage Ix = derivativeOnX(target.image);
                BufferedImage Iy = derivativeOnY(target.image);

                Ix2 = productImage(Ix,Ix);
                Iy2 = productImage(Iy,Iy);
                IxIy = productImage(Ix,Iy);
                
                BufferedImage R_display = R_Image(Ix2, IxIy, IxIy, Iy2);
                target.resetImage(R_display);
            }
            
            if ( ((Button)e.getSource()).getLabel().equals("Thresholding") ){
                
                ConverttoGreyscale();
                gaussian();
                BufferedImage Ix = derivativeOnX(target.image);
                BufferedImage Iy = derivativeOnY(target.image);

                Ix2 = productImage(Ix,Ix);
                Iy2 = productImage(Iy,Iy);
                IxIy = productImage(Ix,Iy);
                
                BufferedImage R_display = R_Image(Ix2, IxIy, IxIy, Iy2);
                BufferedImage R_threshDisplay = R_Thresholded(R_display);
                target.resetImage(R_threshDisplay);
            }
            if ( ((Button)e.getSource()).getLabel().equals("Non-max Suppression") ){
                
                ConverttoGreyscale();
                gaussian();
                BufferedImage Ix = derivativeOnX(target.image);
                BufferedImage Iy = derivativeOnY(target.image);

                Ix2 = productImage(Ix,Ix);
                Iy2 = productImage(Iy,Iy);
                IxIy = productImage(Ix,Iy);
                
                BufferedImage R_display = R_Image(Ix2, IxIy, IxIy, Iy2);
                BufferedImage R_threshDisplay = R_Thresholded(R_display);
                BufferedImage supres = suppression(R_threshDisplay);
                target.resetImage(R_threshDisplay);
                
            }
            if ( ((Button)e.getSource()).getLabel().equals("Display Corners") ){
                
                ConverttoGreyscale();
                gaussian();
                BufferedImage Ix = derivativeOnX(target.image);
                BufferedImage Iy = derivativeOnY(target.image);

                Ix2 = productImage(Ix,Ix);
                Iy2 = productImage(Iy,Iy);
                IxIy = productImage(Ix,Iy);
                
                BufferedImage R_display = R_Image(Ix2, IxIy, IxIy, Iy2);
                BufferedImage R_threshDisplay = R_Thresholded(R_display);
                BufferedImage cirle = curveCircle(R_threshDisplay);
                target.resetImage(R_threshDisplay);
            }
            
            
	}
	public static void main(String[] args) {
		new CornerDetection(args.length==1 ? args[0] : "fingerprint.png");
	}

	// moravec implementation
	void derivatives() {
		int l, t, r, b, dx, dy;
		Color clr1, clr2;
		int gray1, gray2;

		for ( int q=0 ; q<height ; q++ ) {
			t = q==0 ? q : q-1;
			b = q==height-1 ? q : q+1;
			for ( int p=0 ; p<width ; p++ ) {
				l = p==0 ? p : p-1;
				r = p==width-1 ? p : p+1;
				clr1 = new Color(source.image.getRGB(l,q));
				clr2 = new Color(source.image.getRGB(r,q));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dx = (gray2 - gray1) / 3;
				clr1 = new Color(source.image.getRGB(p,t));
				clr2 = new Color(source.image.getRGB(p,b));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dy = (gray2 - gray1) / 3;
				dx = Math.max(-128, Math.min(dx, 127));
				dy = Math.max(-128, Math.min(dy, 127));
				target.image.setRGB(p, q, new Color(dx+128, dy+128, 128).getRGB());
			}
		}
		target.repaint();
	}
        
        
        public void gaussian(){
            double[] g_kernel = guassian1D_kernel(5);
            int g_kernel_radius = g_kernel.length/2;

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
                            Color clr = new Color(source.image.getRGB(j, i));
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

                    target.image.setRGB(j, i, (new Color((int)redSum, (int)greenSum, (int)blueSum)).getRGB());

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
        
        public BufferedImage derivativeOnX(BufferedImage a){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            //int offset = 100;
            
            for(int i=0; i < height; i++){
                for(int j=1; j < width; j++){
                    //Get the intensity of the right and left pixel
                    Color right_clr = new Color(a.getRGB(j, i));
                    Color left_clr = new Color(a.getRGB(j-1,i));
                    
                    //Calculate the difference
                    resultRed = Math.abs(right_clr.getRed() - left_clr.getRed());
                    resultGreen = Math.abs(right_clr.getGreen() - left_clr.getGreen());
                    resultBlue = Math.abs(right_clr.getBlue() - left_clr.getBlue());
                    
                    //Adjust values that are greater than 255 
                    resultRed = (resultRed < 0) ? 0 : (resultRed > 255) ? 255 : resultRed;
                    resultGreen = (resultGreen < 0) ? 0 : (resultGreen > 255) ? 255 : resultGreen;
                    resultBlue = (resultBlue < 0) ? 0 : (resultBlue > 255) ? 255 : resultBlue;
                    
                    //Create a new color object, set the pixel to have that color in the buffered image
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j-1,i,tgt_rgb);
                }
            }
            return result;
        }
        
        public BufferedImage derivativeOnY(BufferedImage b){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            //int offset = 100;
            
            for(int i=0; i < width; i++){
                for(int j=1; j < height; j++){
                    //Get the intensity of the top and bottom pixel
                    Color top_clr = new Color(b.getRGB(i,j-1));
                    Color bottom_clr = new Color(b.getRGB(i,j));
                    
                    
                    //Calculate the difference
                    resultRed = Math.abs(top_clr.getRed() - bottom_clr.getRed());
                    resultGreen = Math.abs(top_clr.getGreen() - bottom_clr.getGreen());
                    resultBlue = Math.abs(top_clr.getBlue() - bottom_clr.getBlue());
                    
                    //Adjust values that are greater than 255 
                    resultRed = (resultRed < 0) ? 0 : (resultRed > 255) ? 255 : resultRed;
                    resultGreen = (resultGreen < 0) ? 0 : (resultGreen > 255) ? 255 : resultGreen;
                    resultBlue = (resultBlue < 0) ? 0 : (resultBlue > 255) ? 255 : resultBlue;
                    
                    //Create a new color object, set the pixel to have that color in the buffered image
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(i,j-1,tgt_rgb);
                }
            }
            return result;       
        }
        
        //Multiply two buffered Images
        public BufferedImage productImage(BufferedImage a, BufferedImage b){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    //Get the intensity 
                    Color a_clr = new Color(a.getRGB(j, i));
                    Color b_clr = new Color(b.getRGB(j,i));
                    
                    //Calculate the difference
                    resultRed = a_clr.getRed() * b_clr.getRed();
                    resultGreen = a_clr.getGreen() * b_clr.getGreen();
                    resultBlue = a_clr.getBlue() * b_clr.getBlue();
                    
                    //Adjust values that are greater than 255 
                    resultRed = (resultRed < 0) ? 0 : (resultRed > 255) ? 255 : resultRed;
                    resultGreen = (resultGreen < 0) ? 0 : (resultGreen > 255) ? 255 : resultGreen;
                    resultBlue = (resultBlue < 0) ? 0 : (resultBlue > 255) ? 255 : resultBlue;
                    
                    //Create a new color object, set the pixel to have that color in the buffered image
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j,i,tgt_rgb);
                }
            }
            
            return result;
        }
        
        public BufferedImage subtractImage(BufferedImage a, BufferedImage b){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    Color aclr = new Color(a.getRGB(j, i));
                    Color bclr = new Color(b.getRGB(j, i));
                    
                    //Calculate the intensity of new pixel
                    resultRed = Math.abs(aclr.getRed() - bclr.getRed());
                    resultGreen = Math.abs(aclr.getGreen() - bclr.getGreen());
                    resultBlue = Math.abs(aclr.getBlue() - bclr.getBlue());
                    
                    
                    //System.out.println(aclr.getBlue() + " - " + bclr.getBlue() + " = "+ resultBlue);
                    //Adjust values that are less than 0 and greater than 255
                    resultRed = (resultRed < 0) ? 0 : (resultRed > 255) ? 255 : resultRed;
                    resultGreen = (resultGreen < 0) ? 0 : (resultGreen > 255) ? 255 : resultGreen;
                    resultBlue = (resultBlue < 0) ? 0 : (resultBlue > 255) ? 255 : resultBlue;
                    
                    //Create a new color object, set the pixel to have that color in the buffered image
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j,i,tgt_rgb);
                }
            }
            //target.resetImage(result);
            return result;
        }
        
        public BufferedImage SetChannelHSV(BufferedImage a, BufferedImage b, BufferedImage c){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    Color a_clr = new Color(a.getRGB(j, i));
                    Color b_clr = new Color(b.getRGB(j,i));
                    Color c_clr = new Color(c.getRGB(j,i));
                    
                    double hsvRed [] =  RGBtoHSV(a_clr.getRed(), a_clr.getBlue(), a_clr.getGreen());
                    resultRed = (int)hsvRed[2];
                    
                    double hsvGreen [] =  RGBtoHSV(b_clr.getRed(), b_clr.getBlue(), b_clr.getGreen());
                    resultGreen = (int)hsvGreen[2];
                    
                    double hsvBlue[] =  RGBtoHSV(c_clr.getRed(), c_clr.getBlue(), c_clr.getGreen());
                    resultBlue = (int)hsvBlue[2];
                    
                    //Create a new color object, set the pixel to have that color in the buffered image
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j,i,tgt_rgb);
                }
            }
            
            
            
            return result;
        }
        
        //Generate 1D Gaussian kernel
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
       
        public static double[] RGBtoHSV(double r, double g, double b){
            double h, s, v;

            double min, max, delta;

            min = Math.min(Math.min(r, g), b);
            max = Math.max(Math.max(r, g), b);

            // V
            v = max;

            delta = max - min;

            // S
             if( max != 0 )
                s = delta / max;
             else {
                s = 0;
                h = -1;
                return new double[]{h,s,v};
             }

            // H
             if( r == max )
                h = ( g - b ) / delta; // between yellow & magenta
             else if( g == max )
                h = 2 + ( b - r ) / delta; // between cyan & yellow
             else
                h = 4 + ( r - g ) / delta; // between magenta & cyan

             h *= 60;    // degrees

            if( h < 0 )
                h += 360;
            return new double[]{h,s,v};
        }
        
        public void ConverttoGreyscale(){
	    BufferedImage img = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
	    File f = null;

	    //read image

	    //get image width and height

	    int red, blue, green;

	    //convert to grayscale
	    for(int y = 0; y < height; y++){
	      for(int x = 0; x < width; x++){
                  
                  Color nclr=new Color(input.getRGB(x,y));
                  red=nclr.getRed();
                  blue=nclr.getBlue();
                  green=nclr.getRed();


	         //calculate average
	        int avg = (red+blue+green)/3;

	        //replace RGB value with avg
	        Color targetColor = new Color (avg,avg,avg);
            int tgt_rgb = targetColor.getRGB();
            img.setRGB(x,y,tgt_rgb);;
	      }
	    }
	    source.resetImage(img);
	    source.repaint();
	    //write image

	  }//main() ends here
        
        public BufferedImage  SetChannel3(BufferedImage a, BufferedImage b, BufferedImage c ){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    Color a_clr = new Color(a.getRGB(j, i));
                    Color b_clr = new Color(b.getRGB(j,i));
                    Color c_clr = new Color(c.getRGB(j,i));
                    
                    resultRed = a_clr.getRed();
                    resultGreen = b_clr.getGreen();
                    resultBlue = c_clr.getBlue();
                    
                    //Create a new color object, set the pixel to have that color in the buffered image
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j,i,tgt_rgb);
                }
            }
            return result;
        }
        
        public BufferedImage addImage(BufferedImage a, BufferedImage b){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    Color aclr = new Color(a.getRGB(j, i));
                    Color bclr = new Color(b.getRGB(j, i));
                    
                    //Calculate the intensity of new pixel
                    resultRed = Math.abs(aclr.getRed() + bclr.getRed());
                    resultGreen = Math.abs(aclr.getGreen() + bclr.getGreen());
                    resultBlue = Math.abs(aclr.getBlue() + bclr.getBlue());
                    
                    
                    //System.out.println(aclr.getBlue() + " - " + bclr.getBlue() + " = "+ resultBlue);
                    //Adjust values that are less than 0 and greater than 255
                    resultRed = (resultRed < 0) ? 0 : (resultRed > 255) ? 255 : resultRed;
                    resultGreen = (resultGreen < 0) ? 0 : (resultGreen > 255) ? 255 : resultGreen;
                    resultBlue = (resultBlue < 0) ? 0 : (resultBlue > 255) ? 255 : resultBlue;
                    
                    //Create a new color object, set the pixel to have that color in the buffered image
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j,i,tgt_rgb);
                }
            }
            return result;
        }
        public BufferedImage scaleImage(double num, BufferedImage a){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    Color aclr = new Color(a.getRGB(j, i));
                    
                    resultRed = (int)(num * aclr.getRed());
                    resultGreen = (int)(num * aclr.getGreen());
                    resultBlue = (int)(num * aclr.getBlue());
                    
                    resultRed = (resultRed < 0) ? 0 : (resultRed > 255) ? 255 : resultRed;
                    resultGreen = (resultGreen < 0) ? 0 : (resultGreen > 255) ? 255 : resultGreen;
                    resultBlue = (resultBlue < 0) ? 0 : (resultBlue > 255) ? 255 : resultBlue;
                    
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j,i,tgt_rgb);
                }
            }
            return result;
        }
        public BufferedImage R_Image(BufferedImage a, BufferedImage b, BufferedImage c, BufferedImage d){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            BufferedImage ad = productImage(a,d);
            BufferedImage bc = productImage(b,c);
            BufferedImage ad_bc = subtractImage(ad,bc);
            double k = sensitivity;
            BufferedImage aPlusd = addImage(a,d);
            BufferedImage aPlusd_squared = productImage(aPlusd,aPlusd);
            BufferedImage scaled_aPlusdSquared = scaleImage(k,aPlusd_squared);
            result = subtractImage(ad_bc, scaled_aPlusdSquared);
  
            return result;        
        }
        
        public BufferedImage R_Thresholded(BufferedImage a){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            
            int thresh = threshold; 
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    Color aclr = new Color(a.getRGB(j, i));
                    
                    resultRed = aclr.getRed() < thresh ? 0 : 255 ;
                    resultGreen = aclr.getGreen() < thresh ? 0 : 255;
                    resultBlue = aclr.getBlue() < thresh ? 0 : 255;
                    
                    Color targetColor = new Color (resultRed, resultGreen, resultBlue);
                    int tgt_rgb = targetColor.getRGB();
                    result.setRGB(j,i,tgt_rgb);
                }
            }
            return result;    
        }
        
        
        private BufferedImage suppression(BufferedImage a) {
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);

		boolean[][] Suppression = new boolean[height][width];
		boolean r, l , t, b, tr, tl, br, bl;
		for (int y = 2; y < height - 2; y++) {
			for (int x = 2; x < width - 2; x++) {
				Suppression[y][x] = false;
				r = l = t = b = tr = tl = br = bl = false;
				// First checks if the target pixel is white, then checks if any surrounding pixels are white if so
				if (new Color(a.getRGB(x, y)).getBlue() == 255) {
					if (new Color(a.getRGB(x + 1, y)).getBlue() == 255) r = true;
					if (new Color(a.getRGB(x - 1, y)).getBlue() == 255) l = true;
					if (new Color(a.getRGB(x, y - 1)).getBlue() == 255) t = true;
					if (new Color(a.getRGB(x, y + 1)).getBlue() == 255) b = true;
					if (new Color(a.getRGB(x + 1, y - 1)).getBlue() == 255) tr = true;
					if (new Color(a.getRGB(x - 1, y + 1)).getBlue() == 255) bl = true;
					if (new Color(a.getRGB(x + 1, y + 1)).getBlue() == 255) br = true;
					if (new Color(a.getRGB(x - 1, y - 1)).getBlue() == 255) tl = true;
					if (!((tl && br) || (t && b) || (tr && bl) || (r && l))) Suppression[y][x] = true;
				}
			}
		}
		for (int y = 2; y < height - 2; y++) {
			for (int x = 2; x < width - 2; x++) {
				if (!(Suppression[y][x]))
					a.setRGB(x, y, new Color(0, 0, 0).getRGB());
			}
		}
	
                return result;
	}

	private BufferedImage curveCircle(BufferedImage a) {
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);

		Color red_cirle = new Color(230, 21, 21);
		int  l, l_leng, r, r_leng;
		int origin, radius = 1;
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
					for (l = x-radius; l<= x+radius; l++) {
						if(new Color(a.getRGB(x, y)).getBlue() == 255) {
						if(l<0 || l>=width) continue;
						l_leng = l-x;
						r_leng = (int) Math.round(Math.sqrt((radius*radius)-(l_leng * l_leng)));
						r = y+r_leng;
						origin = y-r_leng;
						if (r<height) a.setRGB(l, r, red_cirle.getRGB());
						if(origin>=0) a.setRGB(l, origin, red_cirle.getRGB());
					}
				}

			}
		}
                return result;
	}
         public int auto_calculator(int thresh_old, String channel)
	{
		//calculating sum for intensities above threshold and below threshold
		int aboveSum=0;
		int belowSum=0;
		//counting pixels above threshold and below threshold
		int belCount=0;
		int aboCount=0;
		//the average intensity of pixels above threshold and below threshold
		int aboAvrg=0;
		int belAvrg=0;
		int totAvrg=0;//the total average intensity of the image in a specific color channel
		
		int intense=0;//intensity of pixel
		if(channel=="red")
		{
			for (int y=0; y<height;y++)
			{
				for (int x=0; x<width;x++)
				{
					Color clr = new Color(source.image.getRGB(x, y));
					intense=clr.getRed();
					
					//summing up intensity above and below threshold and counting the pixels
					if(intense>=thresh_old)
					{
						aboveSum+=intense;
						aboCount++;
					}
					if(intense<thresh_old)
					{
						belowSum+=intense;
						belCount++;
					}
	
				}
			
			}
			
			//Calculating average intensities
			if (aboCount!=0)
			{
				aboAvrg=aboveSum/aboCount;
			}
			else
			{
				aboAvrg=0;
			}
			if (belCount!=0)
			{
				belAvrg=belowSum/belCount;
			}
			else
			{
				belAvrg=0;
			}
			
			totAvrg=(aboAvrg+belAvrg)/2;
		}
		
		if(channel=="green")
		{
			for (int y=0; y<height;y++)
			{
				for (int x=0; x<width;x++)
				{
					Color clr = new Color(source.image.getRGB(x, y));
					intense=clr.getGreen();
					
					//summing up intensity above and below threshold and counting the pixels
					if(intense>=thresh_old)
					{
						aboveSum+=intense;
						aboCount++;
					}
					if(intense<thresh_old)
					{
						belowSum+=intense;
						belCount++;
					}
	
				}
			}
			
			//Calculating average intensities
			if (aboCount!=0)
			{
				aboAvrg=aboveSum/aboCount;
			}
			else
			{
				aboAvrg=0;
			}
			if (belCount!=0)
			{
				belAvrg=belowSum/belCount;
			}
			else
			{
				belAvrg=0;
			}
			totAvrg=(aboAvrg+belAvrg)/2;
			
		}
		if(channel=="blue")
		{
			for (int y=0; y<height;y++)
			{
				for (int x=0; x<width;x++)
				{
					Color clr = new Color(source.image.getRGB(x, y));
					intense=clr.getBlue();
					//summing up intensity above and below threshold and counting the pixels

					if(intense>=thresh_old)
					{
						aboveSum+=intense;
						aboCount++;
					}
					if(intense<thresh_old)
					{
						belowSum+=intense;
						belCount++;
					}
	
				}
			}
			
			//Calculating average intensities
			if (aboCount!=0)
			{
				aboAvrg=aboveSum/aboCount;
			}
			else
			{
				aboAvrg=0;
			}
			if (belCount!=0)
			{
				belAvrg=belowSum/belCount;
			}
			else
			{
				belAvrg=0;
			}
			totAvrg=(aboAvrg+belAvrg)/2;
			
		}
		//returning total average intensity
		return totAvrg;

	}
}
