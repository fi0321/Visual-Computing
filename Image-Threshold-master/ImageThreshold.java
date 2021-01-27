// Skeletal program for the "Image Threshold" assignment
// Written by:  Minglun Gong

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageThreshold extends Frame implements ActionListener {
	BufferedImage input,output;
	int width, height;
	TextField texThres, texOffset;
	ImageCanvas source, target;
	PlotCanvas2 plot;

	// Constructor
	public ImageThreshold(String name) {
		super("Image Histogram");
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
		plot = new PlotCanvas2(256, 200);
		target = new ImageCanvas(width, height);
        target.resetImage(input);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		controls.add(new Label("Threshold:"));
		texThres = new TextField("128", 2);
		controls.add(texThres);
		Button button = new Button("Manual Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Automatic Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Otsu's Method");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Offset:"));
		texOffset = new TextField("10", 2);
		controls.add(texOffset);
		button = new Button("Adaptive Mean-C");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
        displayHistogram();
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
        
        public void displayHistogram(){
            
            int redValue = 0;
            int greenValue = 0;
            int blueValue = 0;
            
            Boolean grayDe = false;
            int TotalR = 0;
            int TotalG = 0;
            int TotalB = 0;
            
            //Get  the number of pixels with a specific intenisty on each colour channel
            int red[] = new int[width];
            int green[] = new int[width];
            int blue[] = new int[width];
            
            for(int y=0; y < height; y++){
                for(int x=0; x<width; x++){
                    Color clr = new Color(source.image.getRGB(x,y));
                    redValue = clr.getRed();
                    greenValue = clr.getGreen();
                    blueValue = clr.getBlue();
                    
                    //Count the number of times an intensity appears(Frequency)
                    red[redValue]++;
                    green[greenValue]++;
                    blue[blueValue]++;
                    
                    TotalR+=redValue;
                    TotalG+=blueValue;
                    TotalB+=greenValue;
                }
            }
            if ((TotalR == TotalG) && (TotalG == TotalB)){
                grayDe = true;
            }else{
                grayDe = false;
            }			
            boolean isittrue=BandW();
			if (isittrue==true)
			{
				System.out.println("It is black and white");
			}
			if (isittrue==false)
			{
				System.out.println(" it is colored");
			}
        
            plot.drawHistogram(red, green, blue,grayDe);
            System.out.println("Yes");
            
        }
        
		public BufferedImage meanSmooth(){
            BufferedImage smoothSource = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for( int q=0; q<height;q++){
                for(int p=0; p<width;p++){
                    int sum_red=0;
                    int sum_blue=0;
                    int sum_green=0;
                    for(int v=-2; v<=2;v++){
                        for(int u=-2;u<=2;u++){
                            Color clr_new;
                            try
                            {
                                clr_new = new Color(source.image.getRGB(p+u, q+v));
                            }
                            catch(Exception x)
                            {         
                                clr_new = new Color(source.image.getRGB(p, q)); 
                            }
                            sum_red+=clr_new.getRed();
                            sum_blue+=clr_new.getBlue();
                            sum_green+=clr_new.getGreen();
                        }
                    }
                    sum_red= sum_red/25;
                    sum_blue= sum_blue/25;
                    sum_green= sum_green/25;


                    //Set the pixel of the buffered image to the new colour
                    Color targetColor = new Color (sum_red, sum_green, sum_blue);
                    int tgt_rgb = targetColor.getRGB();
                    smoothSource.setRGB(p,q,tgt_rgb);
                }
            }
            return smoothSource;
		}
        public BufferedImage subtractImage(BufferedImage a, BufferedImage b){
            BufferedImage result = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);
            int resultRed = 0;
            int resultGreen = 0;
            int resultBlue = 0;
            int offset = 100;
            
            for(int i=0; i < height; i++){
                for(int j=0; j < width; j++){
                    Color aclr = new Color(a.getRGB(j, i));
                    Color bclr = new Color(b.getRGB(j, i));
                    
                    //Calculate the intensity of new pixel
                    resultRed = (aclr.getRed()+offset) - bclr.getRed();
                    resultGreen = (aclr.getGreen()+offset) - bclr.getGreen();
                    resultBlue = (aclr.getBlue()+offset) - bclr.getBlue();
                    
                    
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
        
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) 
	{
        //displayHistogram();
		// example -- compute the average color for the image
		if ( ((Button)e.getSource()).getLabel().equals("Manual Selection") ) 
		{
			//The target image
	        BufferedImage tgt_image1= new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	        
	        //User input for the threshold
			int threshold;
			try {
				threshold = Integer.parseInt(texThres.getText());
			} catch (Exception ex) {
				texThres.setText("128");
				threshold = 128;
			}
			
			//Assigning new intensities to the colorchannel based on the threshold
			for (int y = 0; y < height; y++){
				for (int x = 0; x < width; x++){
					Color clr = new Color(source.image.getRGB(x, y));
					int red = clr.getRed();
					red = red < threshold ? 0: 255;
					int green = clr.getGreen();
					green = green < threshold ? 0: 255;
					int blue = clr.getBlue();
					blue = blue < threshold ? 0: 255;
					
					Color A_tgtclr=new Color(red,green,blue);//assigning intensities to color channel
					int A_rgb= A_tgtclr.getRGB();
					tgt_image1.setRGB(x,y,A_rgb);//Drawing the pixels
				}
			}
			
			//Checking if the image is black or white
			boolean isittrue=BandW();
			if (isittrue==true)
			{
				System.out.println("It is black and white");
			}
			if (isittrue==false)
			{
				System.out.println(" it is colored");
			}
	       
			target.resetImage(tgt_image1);//resetting the target image
			plot.clearObjects();
			
			//Drawing the new threshold
			if (isittrue==true)
			{
				plot.addObject(new VerticalBar(Color.GRAY, threshold, 150));

			}
			else
			{
				plot.addObject(new VerticalBar(Color.RED, threshold, 150));
				plot.addObject(new VerticalBar(Color.GREEN, threshold, 150));
				plot.addObject(new VerticalBar(Color.BLUE, threshold, 150));
			}
		}

		if ( ((Button)e.getSource()).getLabel().equals("Adaptive Mean-C") ) {
			//Remove global illumination by subtracting smooth image from original image
			BufferedImage smoothInput = meanSmooth();
			BufferedImage highFrequency = subtractImage(input,smoothInput);
			BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			
			//Get Constant 'C', and adaptive thresholding is 7x7 window '2*w+1=7'
			int c = Integer.parseInt(texOffset.getText());
			int w = 3;
			
			//Sum of the red, blue, and green intensity in a window
			int redSum = 0;
			int greenSum = 0;
			int blueSum = 0;
			
			//Average of the sum
			int meanRed, meanGreen, meanBlue = 0;
			
			//Final red, green and blue intensities of a pixel after thresholding
			int red, green, blue = 0;
			
			//Use a the 7x7 window and calculate the mean at each window
			//Use 'mean-c' as the threshold for that pixel
			//and then decide if the pixel is 0 or 255 based on the pixel.
			for(int q=0; q<height; q++){
				for(int p=0; p<width; p++){
					redSum =0; greenSum = 0; blueSum = 0;
					
					for (int v = -w; v <= w; v++){
						for (int u = -w; u <= w; u++){ 
							int temp = Clamp(p+u, 0, width-1);
							int temp1 = Clamp(q+v, 0, width-1);
							
							Color clr = new Color(highFrequency.getRGB(temp,temp1));
							redSum += clr.getRed();
							greenSum += clr.getGreen();
							blueSum += clr.getBlue();
						}
		}
					meanRed = (int) redSum/49;
					meanGreen = (int) greenSum/49;
					meanBlue = (int) blueSum/49;
					
					Color clr1 = new Color(highFrequency.getRGB(p,q));
					int red1 = clr1.getRed();
					red = setThreshold(red1, meanRed-c);
		  
					int green1 = clr1.getGreen();
					green = setThreshold(green1, meanGreen-c);
					
					int blue1 = clr1.getBlue();
					blue = setThreshold(blue1, meanBlue-c);
					
					output.setRGB(p,q, red << 16 | green << 8 | blue);
				}
			}
			target.resetImage(output);
			System.out.println("Cannot display threshold for Adaptive Mean-C because the 'mean-c' threshold is different "
					+ "for every window across every channel");
		}

		if ( ((Button)e.getSource()).getLabel().equals("Automatic Selection") ) 
		{
	        BufferedImage tgt_image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//The target image
	        
	        //Getting user input for threshold
			int thresRed = Integer.parseInt(texThres.getText());
			int thresGreen = Integer.parseInt(texThres.getText());
			int thresBlue = Integer.parseInt(texThres.getText());
			
			//initializing variables for new theshold
			int temp_Red=0;
			int temp_Green=0;
			int temp_Blue=0;
			
			//Used to verify the channel for calculating new threshold
			String Ch_red="red";
			String Ch_green="green";
			String Ch_blue="blue";
			
			//Making the difference between the old threshold and new one small
			while(Math.abs(thresRed-temp_Red)!=0)
			{
				temp_Red=thresRed;
				thresRed=auto_calculator(thresRed,Ch_red);
			}
			while(Math.abs(thresGreen-temp_Green)!=0)
			{
				temp_Green=thresGreen;
				thresGreen=auto_calculator(thresGreen,Ch_green);

			}
			while(Math.abs(thresBlue-temp_Blue)!=0)
			{
				temp_Blue=thresBlue;
				thresBlue=auto_calculator(thresBlue,Ch_blue);

			}
			
			//Assigning new intensities to the color channel based on the new threshold
			System.out.println(temp_Red);
			System.out.println(temp_Green);
			System.out.println(temp_Blue);

			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					Color clr = new Color(source.image.getRGB(x, y));
					int red = clr.getRed();
					red = red < temp_Red ? 0: 255;
					int green = clr.getGreen();
					green = green < temp_Green ? 0: 255;
					int blue= clr.getBlue();
					blue = blue < temp_Blue ? 0: 255;
					
					Color A_tgtclr=new Color(red,green,blue);//Assigning RGB values
					int A_rgb= A_tgtclr.getRGB();
					tgt_image2.setRGB(x,y,A_rgb);//Redrawing the image pixel by pixel

				}
			}
			
	        target.resetImage(tgt_image2);//Resetting the target image
	        plot.clearObjects();
			
	        // verifying if the image is black and white
			boolean isittrue=BandW();
			if (isittrue==true)
			{
				System.out.println("It is black and white");
			}
			if (isittrue==false)
			{
				System.out.println(" it is colored");
			}

			if (isittrue==true)
			{
				plot.addObject(new VerticalBar(Color.GRAY, temp_Red, 150));

			}
			else
			{
			plot.addObject(new VerticalBar(Color.RED, temp_Red, 150));
			plot.addObject(new VerticalBar(Color.GREEN, temp_Green, 150));
			plot.addObject(new VerticalBar(Color.BLUE, temp_Blue, 150));
			}
		}

		if ( ((Button)e.getSource()).getLabel().equals("Otsu's Method") ) {
			BufferedImage tgt_image3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//The target image

			int redPixels[] = new int[width];
			int greenPixels[] = new int[width];
			int bluePixels[] = new int[width];
			
			float RedNormal[] = new float[width];
			float GreenNormal[] = new float[width];
			float BlueNormal[] = new float[width];


			for (int b=0; b < height; b++ ){
				for (int c = 0; c < width; c++){
					Color clr1 = new Color(source.image.getRGB(c, b));
					int red = clr1.getRed();
					int green = clr1.getGreen();
					int blue = clr1.getBlue();
					redPixels[red]++;
					greenPixels[green]++;
					bluePixels[blue]++;
				}
			}
			float r, g, b;
			for (int i=0; i<256; i++){
				r = (float) redPixels[i]/(width*height);
				g = (float) greenPixels[i]/(width*height);
				b = (float) bluePixels[i]/(width*height);
				RedNormal[i] = r;
				GreenNormal[i] = g;
				BlueNormal[i] = b;
			}

			int redThreshold = OstuThreshold(RedNormal);
			int greenThreshold = OstuThreshold(GreenNormal);
			int blueThreshold = OstuThreshold(BlueNormal);

			System.out.println(redThreshold);
			System.out.println(greenThreshold);
			System.out.println(blueThreshold);

			for (int y = 0; y < height; y++){
				for (int x = 0; x < width; x++){
					Color clr = new Color(source.image.getRGB(x, y));

					int r_get = clr.getRed();
					int g_get = clr.getGreen();
					int b_get = clr.getBlue();

					int RED = r_get < redThreshold ? 0: 255;
					int GREEN = g_get < greenThreshold ? 0: 255;
					int BLUE = b_get < blueThreshold ? 0: 255;
		

					Color A_tgtclr1=new Color(RED,GREEN,BLUE);//Assigning RGB values
					int A_rgb= A_tgtclr1.getRGB();
					tgt_image3.setRGB(x,y,A_rgb);//Redrawing the image pixel by pixel
		

				}
			}

			target.resetImage(tgt_image3);//Resetting the target image
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.RED, redThreshold, 100));
			plot.addObject(new VerticalBar(Color.GREEN, greenThreshold, 100));
			plot.addObject(new VerticalBar(Color.BLUE, blueThreshold, 100));


			boolean isittrue=BandW();
			if (isittrue==true)
			{
				System.out.println("It is black and white");
			}
			if (isittrue==false)
			{
				System.out.println(" it is colored");
			}

			if (isittrue==true)
			{
				plot.addObject(new VerticalBar(Color.GRAY, redThreshold, 150));

			}
			else
			{
			plot.addObject(new VerticalBar(Color.RED, redThreshold, 150));
			plot.addObject(new VerticalBar(Color.GREEN, greenThreshold, 150));
			plot.addObject(new VerticalBar(Color.BLUE, redThreshold, 150));
			}

		}

	}

	public int OstuThreshold(float [] pixel){
		float maximum_thresh = 0, f_max_thresh = 0;
		int ThresholdA = 0;

		float tempw0 = 0, tempw1 = 1, tmean = 0, tmean1 = 0; 
		float cur =0, curw1 = 0, mean_now = 0, mean_now1= 0;
		tmean1 = meanOtsu(pixel);

		for(int t = 1; t < width; t++){
			curw1 = tempw1 - pixel[t];
			cur = tempw0 + pixel[t];


			mean_now = (cur == 0) ? 0 : (tempw0*tmean+((pixel[t])*t))/cur;
			mean_now1 = (curw1 == 0) ? 0 :(tempw1*tmean1-((pixel[t])*t))/curw1;

			float nMean = mean_now - mean_now1;
			maximum_thresh = (float) Math.pow(nMean,2)*cur*curw1;
			if(f_max_thresh < maximum_thresh){
				f_max_thresh = maximum_thresh;
				ThresholdA = t;
			}
			tempw0 = cur; 
			tempw1 = curw1;
			tmean = mean_now;
			tmean1 = mean_now1;
		}
		return ThresholdA;
	}

	public float meanOtsu(float [] pixel){
		float sum = 0, sum1 = 0, fsum = 0;
		for(int i = 0; i < width; i++){
			sum += (i*pixel[i]);
			sum1 += pixel[i];
		}
		fsum = sum/sum1;
		return fsum;
	}

	public int setThreshold(int x, int y){
		if(x > y)
			x = 255;
		else if(x < y)
			x = 0;
		return x;
}
	
	public int Clamp(int val, int min, int max){ //Clamps an integer value between 2 other values 
		if (val < min) return min;
		if (val > max) return max;
		return val;
}
	
	
	//Method to verify if the image is black or white
	public Boolean BandW()
	{
		Boolean result = false;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				Color clr = new Color(source.image.getRGB(x, y));
				int red = clr.getRed();
				int green = clr.getGreen();
				int blue = clr.getBlue();
				if (red == green && red == blue && green == blue)
				{
					result = true;
				}
				else
				{
					result = false;
					break;
				}
			}
		}
		return result;
	}
	//The method for automatically calculating the new threshold. Takes in oldthreshold and specific color channel as input
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
	

	public static void main(String[] args) {
		new ImageThreshold(args.length==1 ? args[0] : "fingerprint.png");
	}



	
}

