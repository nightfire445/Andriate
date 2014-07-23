package com.example.andriate;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;
import org.opencv.core.*;
import org.opencv.core.Core.MinMaxLocResult;

//import java.awt.image.*;
//import java.awt.Graphics2D;

//import javax.imageio.*;

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
			    Core.line( drawn_lines, pt1, pt2, new Scalar(255,0,0), 2);
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
	
	public static void Find_Line_Positions(double[] rhos, double range, int cols, List<Double> positions) {
		Arrays.sort(rhos);
					
		for (int j = 0; j < cols;) {
			double total = rhos[j];
			//System.out.println("total = " + total);
			if (total < 1) {
				j++;
				//System.out.println("Continue");
				continue;
			}
			int number = 1;
			double first = rhos[j];
			int i = j + 1;
			while (i < cols) {
				if ((rhos[i] - first) > range) {
					j = i;
					System.out.println("1. Line position = " + total/number);
					positions.add(total/number);
					break;
				}
				else {
					total += rhos[i];
					number++;
					i++;
				}
			}
			if (i >= cols) {
				System.out.println("2. Line position = " + total/number);
				positions.add(total/number);
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
	
	public Mat Convert_to_Binary(Mat source) {
		Mat binary = new Mat(source.rows(), source.cols(), source.type());
		Imgproc.threshold(source, binary, 0, 255, Imgproc.THRESH_OTSU);
		return binary;
	}
	
	/*public class customComparator implements Comparator<Point>{
		@Override
		public int compare(Point pnt1, Point pnt2) {
			return pnt1.x.compare(pnt2.x);
		}
	}*/
	
	public static List<RotatedRect> Find_Note_Heads(Mat binary) {
		
		Imgproc.erode(binary, binary, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(40,40)));  
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
		List<RotatedRect> ellipses = new ArrayList<RotatedRect>();
		
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint tmp = contours.get(i);
			if (tmp.rows() >= 5) {
				MatOfPoint2f newtmp = new MatOfPoint2f(tmp.toArray());
				RotatedRect tmpEllipse = Imgproc.fitEllipse(newtmp);
				if ((tmpEllipse.size).area() > 3000) {
					System.out.println(tmpEllipse.center);
					ellipses.add(tmpEllipse);
				}
			}
		}

		return ellipses;
	}
	
	public static void Find_Pitch(List<Double> line_positions, RotatedRect head) {
		double height = head.center.y;
		if (height < line_positions.get(0)) return;
		for (int i = 1; i < line_positions.size(); i++) {
			double high = line_positions.get(i);
			if (height < high) {
				//System.out.println("high = " + high);
				double low = line_positions.get(i-1);
				//System.out.println("low = " + low);
				double range = high - low;
				//System.out.println("range = " + range);
				double middle = low + (range/2);
				//System.out.println("middle = " + middle);
				double threshold = (middle-low)/2;
				//System.out.println("threshold = " + threshold);
				if (low+threshold > height) {
					System.out.println("1. Note head is at line " + (i-1));
				} else if (middle-threshold < height && middle+threshold > height) {
					System.out.println("2. Note head is between line " + (i-1) + " and " + i);
				} else {
					System.out.println("3. Note head is at line " + i);
				}
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		/*
		try {
			
			//Read in picture and create new matrices
			//BufferedImage original = ImageIO.read(new File("src/sample6.jpg"));
			//ImageIO.write(original, "png", new File("src/phone2.png"));
			Mat source = Highgui.imread("src/phone2.png", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			Mat binary = Convert_to_Binary(source);
			Core.bitwise_not(binary, binary);
			Mat edges = new Mat(source.rows(), source.cols(), source.type());
			Mat lines = new Mat();
			Mat drawn_lines = new Mat(source.rows(), source.cols(), source.type());
			
			Imgproc.Canny(binary, edges, 50, 200);
			
			int threshold = source.width() / 10;
			//int threshold = 100;
			Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
			
			//Draw_Lines(drawn_lines, lines);
			double skew_angle = Calculate_Skew(lines);					
			Mat warp_dst = Deskew(binary, skew_angle);
			
			
			Imgproc.Canny(warp_dst, edges, 50, 200);
			Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
			Draw_Lines(drawn_lines, lines);
			
			
			double[] rhos = new double[lines.cols()];	
			Find_Rhos(lines, rhos);
			double range = source.rows() * .04;
			System.out.println("Range = " + range);
			List<Double> line_positions = new ArrayList<Double>();
			Find_Line_Positions(rhos, range, lines.cols(), line_positions);
			
			double space_between_lines = 0;
			for (int i = 0; i < line_positions.size()-1; i++) {
				System.out.println(line_positions.get(i));
				space_between_lines += (line_positions.get(i+1)-line_positions.get(i));
			}
			space_between_lines /= line_positions.size()-1;
			System.out.println(space_between_lines);

			List<RotatedRect> note_heads = Find_Note_Heads(binary);
			//Mat contour_dst = new Mat(source.rows(), source.cols(), source.type());
			for (int i = 0; i < note_heads.size(); i++) {
				//Imgproc.drawContours(contour_dst, contours, i, new Scalar(255, 0, 0));
				Core.ellipse(source, note_heads.get(i), new Scalar(255, 0, 0), 2, 8);
			}
			
			for (int i = 0; i < note_heads.size(); i++) {
				Find_Pitch(line_positions, note_heads.get(i));
			}
						
			Highgui.imwrite("src/new_measure8.jpg", drawn_lines);
			
			System.out.println("Success");
			
		} catch(Exception e) {
			System.out.println(e);
			System.out.println("Fail");
		}
	}		
	*/
	}
}


