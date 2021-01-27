import java.awt.*;
import java.util.*;

// an interface for all objects to be plotted
interface Plotable {
    public void plot(Graphics g, int xoffset, int yoffset);
}

// Canvas for plotting graph
class PlotCanvas2 extends Canvas {
    // size of plot area
    int width, height;
    // Axes and objects to be plotted
    Axis x_axis, y_axis;
    Vector<Plotable> objects;

    //Lines for all three channels
    LineSegment red, green, blue;
    //A collection of short line segment
    LineSegment[] redLine, greenLine; 
    LineSegment[] blueLine;

    boolean showHistogram = false;
    
    //Set histogram for plot
    //Use LineSegment to place two points and connect them
    //Type-cast float values to int to plots
    public void drawHistogram(int hist[],int hist2[],int hist3[], Boolean x ){
        redLine = new LineSegment[255];
        greenLine=new LineSegment[255];
	blueLine=new LineSegment[255];
	System.out.println(x);
        if (x){
            for (int i=0; i < 255; i++){
                redLine[i] = new LineSegment(Color.GRAY, i, hist2[i]/25, i+1, hist2[i+1]/25);
                blueLine[i] = new LineSegment(Color.GRAY, i, hist2[i]/25, i+1, hist2[i+1]/25);
                greenLine[i] = new LineSegment(Color.GRAY, i, hist2[i]/25, i+1, hist2[i+1]/25);
            }
        }
        else{
            for (int i=0; i < 255; i++){
                redLine[i] = new LineSegment(Color.RED, i, hist[i]/3, i+1, hist[i+1]/3);
                blueLine[i] = new LineSegment(Color.BLUE, i, hist2[i]/3, i+1, hist2[i+1]/3);
                greenLine[i] = new LineSegment(Color.GREEN, i, hist3[i]/3, i+1, hist3[i+1]/3);
            }
        }
        
        showHistogram = true;
        repaint();
     }
        
        
	public PlotCanvas2(int wid, int hgt) {
		width = wid;
		height = hgt;
		x_axis = new Axis(true, width);
		y_axis = new Axis(false, height);
		objects = new Vector<Plotable>();
	}
        
	// add objects to plot
	public void addObject(Plotable obj) {
		objects.add(obj);
		repaint();
	}
	public void clearObjects() {
		objects.clear();
		repaint();
	}
        
        
	// redraw the canvas
	public void paint(Graphics g) {
            
            // draw axis
            int xoffset = (getWidth() - width) / 2;
            int yoffset = (getHeight() + height) / 2;
            x_axis.plot(g, xoffset, yoffset);
            y_axis.plot(g, xoffset, yoffset);
            // plot each object
            Iterator<Plotable> itr = objects.iterator();
            while(itr.hasNext())
                    itr.next().plot(g, xoffset, yoffset);
            
            if (showHistogram){
                for (int i=0; i< redLine.length; i++){
                    redLine[i].draw(g, xoffset, yoffset, getHeight());
                }
                for (int i=0; i< greenLine.length; i++){
                    greenLine[i].draw(g, xoffset, yoffset, getHeight());
                }
                for (int i=0; i< blueLine.length; i++){
                    blueLine[i].draw(g, xoffset, yoffset, getHeight());
                }
                showHistogram = true;
            }
	}
        
        
}

// Axis class for plotting X or Y axis
class Axis implements Plotable {
	// type and length of the axis
	boolean xAxis;
	int length, size=15;
	// Constructor
	public Axis(boolean isX, int len) {
		xAxis = isX;
		length = len;
	}
	// plot axis with arrow
	public void plot(Graphics g, int xoffset, int yoffset) {
		g.setColor(Color.BLACK);
		if ( xAxis ) {
			g.drawLine(xoffset-size, yoffset, xoffset+length+size, yoffset);
			g.fillArc(xoffset+length, yoffset-size, size*2, size*2, 160, 40);
		}
		else {
			g.drawLine(xoffset, yoffset+size, xoffset, yoffset-length-size);
			g.fillArc(xoffset-size, yoffset-length-size*2, size*2, size*2, 250, 40);
		}
	}
}

// Bar class defines for ploting a vertical line
class VerticalBar implements Plotable {
	// color, location, and length of the line
	Color color;
	int pos, length;
	// Constructor
	public VerticalBar(Color clr, int p, int len) {
		color = clr;
		pos = p; length = len;
	}
	public void plot(Graphics g, int xoffset, int yoffset) {
		g.setColor(color);
		g.drawLine(xoffset+pos, yoffset, xoffset+pos, yoffset-length);
	}
}

// LineSegment class defines line segments to be plotted
class LineSegment {
    // location and color of the line segment
    int x0, y0, x1, y1;
    Color color;
    // Constructor
    public LineSegment(Color clr, int x0, int y0, int x1, int y1) {
        color = clr;
        this.x0 = x0; this.x1 = x1;
        this.y0 = y0; this.y1 = y1;
    }
    public void draw(Graphics g, int xoffset, int yoffset, int height) {
        g.setColor(color);
        g.drawLine(x0+xoffset, yoffset-y0, x1+xoffset, yoffset-y1);
    }
}



