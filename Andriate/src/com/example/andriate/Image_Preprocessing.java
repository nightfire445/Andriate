import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;
import org.opencv.core.*;

import java.awt.image.*;
import java.awt.Graphics2D;

import javax.imageio.*;
import javax.imageio.ImageIO;

import java.io.File;
import java.util.*;

class Image_Preprocessing {	
	
	public static void Draw_Lines(Mat drawn_lines, Mat lines) {
		
		for( int i = 0; i < lines.cols(); i++ )
		{
		    double[] data = lines.get(0, i);
		    double rho = data[0], theta = data[1];
		    if (theta > .5 && theta < 2.5) {
			    Point pt1 = new Point();
  		  	    Point pt2 = new Point();
	  		    double a = Math.cos(theta), b = Math.sin(theta);
			    //System.out.println("rho: " + rho + "\t theta: " + theta);
			    //System.out.println("a: " + a + "\t b: " + b);
			    double x0 = a*rho, y0 = b*rho;
			    pt1.x = Math.round(x0 + 1000*(-b));
			    pt1.y = Math.round(y0 + 1000*(a));
			    pt2.x = Math.round(x0 - 1000*(-b));
			    pt2.y = Math.round(y0 - 1000*(a));
			    Core.line( drawn_lines, pt1, pt2, new Scalar(255,0,0), 3);
		    }
		}
	}
	
	public static double Calculate_Skew(Mat lines) {
		double skew_angle = 0;
		for( int i = 0; i < lines.cols(); i++ )
		{
		  double[] data = lines.get(0, i);
		  double theta = data[1];
		  if (theta > .5 && theta < 2.5) {
			  double a = Math.cos(theta);
			  skew_angle += a;
		  }
		}
		skew_angle /= lines.cols();
		skew_angle *= (-180/Math.PI);
		return skew_angle;
	}
	
	public static void Find_Rhos(Mat lines, double[] rhos) {
		for( int i = 0; i < lines.cols(); i++ )
		{
		  double[] data = lines.get(0, i);
		  double rho = data[0], theta = data[1];
		  if (theta > .5 && theta < 2.5) {
			  rhos[i] = rho;
		  }
		}
	}
	
	
	/*public static double Calculate_Skew(Mat lines) {
		double skew_angle = 0;
		for( int i = 0; i < lines.cols(); i++ )
		{
		  double[] data = lines.get(0, i);
		  double rho = data[0], theta = data[1];
		  if (theta > .5 && theta < 2.5) {
			  rhos[i] = rho;
			  double a = Math.cos(theta);
			  skew_angle += a;
		  }
		}
		skew_angle /= lines.cols();
		skew_angle *= (-180/Math.PI);
		return skew_angle;
	}
	*/
	
	public static void Find_Line_Positions(double[] rhos, double range, int cols) {
		Arrays.sort(rhos);
					
		for (int j = 0; j < cols;) {
			double total = rhos[j];
			if (total < 1) {
				j++;
				continue;
			}
			int number = 1;
			double first = rhos[j];
			int i = j + 1;
			while (i < cols) {
				if ((rhos[i] - first) > range) {
					j = i;
					System.out.println("Line position = " + total/number);
					break;
				}
				else {
					total += rhos[i];
					number++;
					i++;
				}
			}
			if (i >= cols) {
				System.out.println("Line position = " + total/number);
				break;
			}
		}
	}
	
	public static Mat Deskew(Mat binary, double skew_angle) {
		Point center = new Point(binary.cols()/2, binary.rows()/2);
		Mat rotImage = Imgproc.getRotationMatrix2D(center, skew_angle, 1);
		Mat warp_dst = new Mat();
		Imgproc.warpAffine(binary, warp_dst, rotImage, binary.size(), Imgproc.INTER_CUBIC);
		return warp_dst;
	}
	
	public static Mat Convert_to_Binary(Mat source) {
		Mat binary = new Mat(source.rows(), source.cols(), source.type());
		Imgproc.threshold(source, binary, 0, 255, Imgproc.THRESH_OTSU);
		return binary;
	}
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try {
			
			//Read in picture and create new matrices
			BufferedImage original = ImageIO.read(new File("src/sample3.jpg"));
			ImageIO.write(original, "png", new File("src/phone2.png"));
			Mat source = Highgui.imread("src/phone2.png", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			Mat binary = Convert_to_Binary(source);
			Core.bitwise_not(binary, binary);
			Mat edges = new Mat(source.rows(), source.cols(), source.type());
			Mat lines = new Mat();
			Mat drawn_lines = new Mat(source.rows(), source.cols(), source.type());
			
			Imgproc.Canny(binary, edges, 50, 200);
			
			int threshold = source.width() / 10;
			Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
			
			//Draw_Lines(drawn_lines, lines);
			double skew_angle = Calculate_Skew(lines);					
			Mat warp_dst = Deskew(binary, skew_angle);
			
			
			Imgproc.Canny(warp_dst, edges, 50, 200);
			Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
			Draw_Lines(drawn_lines, lines);
			
			
			double[] rhos = new double[lines.cols()];	
			Find_Rhos(lines, rhos);
			double range = source.rows() * .025;
			Find_Line_Positions(rhos, range, lines.cols());
			
			
			Highgui.imwrite("src/new_measure7.png", warp_dst);
			
			System.out.println("Success");
			
		} catch(Exception e) {
			System.out.println(e);
			System.out.println("Fail");
		}
		
	}		
}

