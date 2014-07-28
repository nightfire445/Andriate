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
			    pt1.x = Math.round(x0 + 5000*(-b));
			    pt1.y = Math.round(y0 + 5000*(a));
			    pt2.x = Math.round(x0 - 5000*(-b));
			    pt2.y = Math.round(y0 - 5000*(a));
			    Core.line( drawn_lines, pt1, pt2, new Scalar(255,0,0), 2);
		    }
		}
	}
	
	public static void Draw_Lines2(Mat drawn_lines, Mat lines) {
		
		for( int i = 0; i < lines.cols(); i++ )
		{
		    double[] data = lines.get(0, i);
		    double rho = data[0], theta = data[1];
		    if (theta < .5 || theta > 2.5) {
			    Point pt1 = new Point();
  		  	    Point pt2 = new Point();
	  		    double a = Math.cos(theta), b = Math.sin(theta);
			    //System.out.println("rho: " + rho + "\t theta: " + theta);
			    //System.out.println("a: " + a + "\t b: " + b);
			    double x0 = a*rho, y0 = b*rho;
			    pt1.x = Math.round(x0 + 5000*(-b));
			    pt1.y = Math.round(y0 + 5000*(a));
			    pt2.x = Math.round(x0 - 5000*(-b));
			    pt2.y = Math.round(y0 - 5000*(a));
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
	
	public static void Find_Horizontal_Rhos(Mat lines, List<Double> horizontal_rhos) {
		for( int i = 0; i < lines.cols(); i++ )
		{
		  double[] data = lines.get(0, i);
		  double rho = data[0], theta = data[1];
		  if (theta > .5 && theta < 2.5) {
			  horizontal_rhos.add(rho);
		  }
		}
		Collections.sort(horizontal_rhos);
	}
	
	public static void Find_Vertical_Rhos(Mat lines, List<Double[]> vertical_rhos) {
		for( int i = 0; i < lines.cols(); i++ )
		{
		  double[] data = lines.get(0, i);
		  double rho = data[0], theta = data[1];
		  if (theta < .5 || theta > 2.5) {
			  if (rho < 0) {
				  rho *= -1;
			  }
			  Double[] tmp = new Double[2];
			  tmp[0] = rho;
			  tmp[1] = theta;
			  
			  vertical_rhos.add(tmp);
			  System.out.println("Theta = " + tmp[1] + " and rho = " + tmp[0]);
		  }
		}	
		Collections.sort(vertical_rhos, new Comparator<Double[]>() {
			@Override
			public int compare(Double[] dbl1, Double[] dbl2) {
				return dbl1[0].compareTo(dbl2[0]);
			}
		});
	}
	
	public static void Find_Line_Positions(Mat binary, List<Double> rhos, double range, List<Double> positions) {
		for (int j = 0; j < rhos.size();) {
			double total = rhos.get(j);
			int number = 1;
			double first = rhos.get(j);
			int i = j + 1;
			while (i < rhos.size()) {
				if ((rhos.get(i) - first) > range) {
					j = i;
					System.out.println("1. Line position = " + total/number);
					positions.add(total/number);
					break;
				}
				else {
					total += rhos.get(i);
					number++;
					i++;
				}
			}
			if (i >= rhos.size()) {
				System.out.println("2. Line position = " + total/number);
				positions.add(total/number);
				break;
			}
		}
	}
	
	public static void Find_Vertical_Lines(Mat binary, List<Double[]> rhos, double range, List<Double[]> positions) {
		for (int j = 0; j < rhos.size();) {
			double total = rhos.get(j)[0];
			double angle = rhos.get(j)[1];
			if (angle > Math.PI/2) {
				angle -= Math.PI;
			}
			int number = 1;
			double first = rhos.get(j)[0];
			int i = j + 1;
			while (i < rhos.size()) {
				if ((rhos.get(i)[0] - first) > range) {
					j = i;
					Double[] tmp = new Double[2];
					tmp[0] = total/number;
					tmp[1] = (angle/number);
					System.out.println("1. Vertical line position = " + tmp[0] + " and angle = " + tmp[1]);
					positions.add(tmp);
					break;
				}
				else {
					total += rhos.get(i)[0];
					if (rhos.get(i)[1] > Math.PI/2) {
						angle += rhos.get(i)[1]-Math.PI;
					} else {
						angle += rhos.get(i)[1];
					}
					number++;
					i++;
				}
			}
			if (i >= rhos.size()) {
				Double[] tmp = new Double[2];
				tmp[0] = total/number;
				tmp[1] = (angle/number);
				System.out.println("2. Vertical line position = " + total/number + " and angle = " + tmp[1]);
				positions.add(tmp);
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
	
	public static List<RotatedRect> Find_Note_Heads(Mat binary, double range) {
		
		Size s1 = new Size(range, range);
		//s1.height = 50;
		//s1.width = 50;
		Imgproc.erode(binary, binary, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, s1));  
		Imgproc.dilate(binary, binary, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, s1));
		//Highgui.imwrite("src/new_measure9.jpg", binary);
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
		List<RotatedRect> ellipses = new ArrayList<RotatedRect>();
		
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint tmp = contours.get(i);
			if (tmp.rows() >= 5) {
				MatOfPoint2f newtmp = new MatOfPoint2f(tmp.toArray());
				RotatedRect tmpEllipse = Imgproc.fitEllipse(newtmp);
				double min_area = binary.rows() * binary.cols() * .002;
				//System.out.println("min_area = " + min_area);
				//System.out.println(tmpEllipse.size);
				if (tmpEllipse.size.width > tmpEllipse.size.height*3) continue;
				if (tmpEllipse.size.height > tmpEllipse.size.width*3) continue;
				if ((tmpEllipse.size).area() > min_area) {
					//System.out.println("Area of binary = " + binary.cols()*binary.rows());
					//System.out.println("Area = " + tmpEllipse.size.area());
					ellipses.add(tmpEllipse);
				}
			}
		}
		
		Collections.sort(ellipses, new Comparator<RotatedRect>() {
			@Override
			public int compare(RotatedRect r1, RotatedRect r2) {
				return Double.compare(r1.center.x, r2.center.x);
			}
		});

		return ellipses;
	}
	
	public static void Find_Pitch(List<Double> line_positions, RotatedRect head, double avg_dist) {
		double height = head.center.y;
		if (height < line_positions.get(0)) {
			//System.out.println("Note is above the staff");
			double difference = line_positions.get(0) - height;
			double tmp = difference / avg_dist;
			//System.out.println("tmp = " + tmp);
			double num_lines = Math.floor(tmp);
			//System.out.println("num_lines = " + num_lines);
			tmp -= num_lines;
			if (tmp > .25 && tmp < .75) {
				System.out.println("4. Note head is between line " + (4+(int)num_lines) + " and " + (5+(int)num_lines));
			} else if (tmp > .75) {
				System.out.println("5. Note head is at line " + (5+(int)num_lines));
			} else {
				System.out.println("6. Note head is at line " + (4+(int)num_lines));
			}
		} else if (height > line_positions.get(line_positions.size()-1)) {
			//System.out.println("Note is below the staff");
			double difference = height - line_positions.get(line_positions.size()-1);
			double tmp = difference / avg_dist;
			//System.out.println("tmp = " + tmp);
			double num_lines = Math.floor(tmp);
			//System.out.println("num_lines = " + num_lines);
			tmp -= num_lines;
			if (tmp > .25 && tmp < .75) {
				System.out.println("7. Note head is between line " + (0-(int)num_lines) + " and " + (-1-(int)num_lines));
			} else if (tmp > .75) {
				System.out.println("8. Note head is at line " + (1-(int)num_lines));
			} else {
				System.out.println("9. Note head is at line " + (0-(int)num_lines));
			}
		} else {
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
						System.out.println("1. Note head is at line " + (4-i+1));
					} else if (middle-threshold < height && middle+threshold > height) {
						System.out.println("2. Note head is between line " + (4-i) + " and " + (4-i+1));
					} else {
						System.out.println("3. Note head is at line " + (4-i));
					}
					return;
				}	
			}
		}
	}
	
	public static void Remove_Lines(Mat binary, List<Double> staff_line_positions, List<Double[]> note_line_positions, double range) {
		for (int i = 0; i < staff_line_positions.size(); i++) {
			Point pnt1 = new Point();
			Point pnt2 = new Point();
			pnt1.y = (staff_line_positions.get(i));
			pnt2.y = (staff_line_positions.get(i));
			pnt1.x = 0;
			for (int j = 0; j < note_line_positions.size(); j++) {
				Double[] tmp = new Double[2];
				tmp[0] = note_line_positions.get(j)[0];	
				tmp[1] = note_line_positions.get(j)[1];
				pnt2.x = (tmp[0] - pnt2.y*Math.tan(tmp[1]))-(range);
				if (pnt1.x > pnt2.x) continue;
				Core.line(binary, pnt1, pnt2, new Scalar(0, 255, 0), (int)range);
				pnt1.x = (tmp[0] - pnt1.y*Math.tan(tmp[1]))+(range);
			}
			pnt2.x = binary.cols();
			Core.line(binary, pnt1, pnt2, new Scalar(0, 255, 0), (int)range);
		}
	}
	
	public static void Find_Notes(List<RotatedRect> note_heads, List<Double[]> vertical_lines, double range) {
		if (note_heads.size() == 0 || vertical_lines.size() == 0) {
			note_heads.clear();
			vertical_lines.clear();
			return;
		}
		
		int i = 0;
		int j = 0;
		while (i < note_heads.size() && j < vertical_lines.size()) {
			double x_note = note_heads.get(i).center.x;
			double x_line = vertical_lines.get(i)[0];
			//System.out.println("x_note = " + x_note + " and x_line = " + x_line);
			if (x_note <= x_line) {
				if (x_note + (4*range) >= x_line) {
					i++;
					j++;
					continue;
				}
				
				note_heads.remove(i);
			} else {
				if (x_line + (4*range) >= x_note) {
					i++;
					j++;
					continue;
				}
				vertical_lines.remove(i);
			}
		}
		
		while (i < note_heads.size()) {
			note_heads.remove(i);
		}
		while (j < vertical_lines.size()) {
			vertical_lines.remove(j);
		}
	}
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		/*
		try {
			
			//Read in picture and create new matrices
			BufferedImage original = ImageIO.read(new File("src/below_staff_test3.jpg"));
			ImageIO.write(original, "png", new File("src/phone2.png"));
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
			Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
			//Imgproc.HoughLines(edges2, lines, 1, Math.PI/180, 100);
			
			Draw_Lines(source, lines);
			double skew_angle = Calculate_Skew(lines);					
			Mat warp_dst = Deskew(binary, skew_angle);
			Mat warp_dst2 = warp_dst.clone();
			
			Imgproc.Canny(warp_dst, edges, 50, 200);
			Mat edges2 = edges.clone();
			Mat lines2 = new Mat();
			Imgproc.HoughLines(edges, lines, 1, Math.PI/180, threshold);
			Imgproc.HoughLines(edges2, lines2, 1, Math.PI/180, 100);
			//Draw_Lines2(source, lines2);
			
			List<Double> horizontal_rhos = new ArrayList<Double>();	
			Find_Horizontal_Rhos(lines, horizontal_rhos);
			double range = source.rows() * .04;
			System.out.println("Range = " + range);
			List<Double> line_positions = new ArrayList<Double>();
			Find_Line_Positions(warp_dst, horizontal_rhos, range, line_positions);
			
			
			List<Double[]> vertical_rhos = new ArrayList<Double[]>();
			Find_Vertical_Rhos(lines2, vertical_rhos);
			List<Double[]> line_positions2 = new ArrayList<Double[]>();
			Find_Vertical_Lines(warp_dst, vertical_rhos, range, line_positions2);
					
			double space_between_lines = 0;
			for (int i = 0; i < line_positions.size()-1; i++) {
				//System.out.println(line_positions.get(i));
				space_between_lines += (line_positions.get(i+1)-line_positions.get(i));
			}
			
			space_between_lines /= line_positions.size()-1;
			//System.out.println(space_between_lines);

			List<RotatedRect> note_heads = Find_Note_Heads(warp_dst2, range);
			//Mat contour_dst = new Mat(source.rows(), source.cols(), source.type());
			
			Find_Notes(note_heads, line_positions2, range);
			System.out.println("After Find_Notes");
			
			for (int i = 0; i < note_heads.size(); i++) {
				Find_Pitch(line_positions, note_heads.get(i), space_between_lines);
			}
			
			Remove_Lines(warp_dst, line_positions, line_positions2, range);
			
			for (int i = 0; i < note_heads.size(); i++) {
				//Core.ellipse(source, note_heads.get(i), new Scalar(255, 0, 0), 2);
				Core.ellipse(warp_dst, note_heads.get(i), new Scalar(255, 0, 0), -1);
			}

			Mat notes_only = new Mat(warp_dst.rows(), warp_dst.cols(), warp_dst.type());
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(warp_dst, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
			double area = notes_only.size().area();
			
			for (int i = 0; i < contours.size(); i++) {
				if (contours.get(i).size().area() < area*.00015) continue;
				//System.out.println(area*.00015);
				Imgproc.drawContours(notes_only, contours, i, new Scalar(255, 0, 0));
				//System.out.println(contours.get(i).size().area());
			}
			
			
			//IGNORE THIS FOR NOW
			
			
			/*Mat template = Highgui.imread("src/template4.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			Mat template_binary = Convert_to_Binary(template);
			Mat new_template = new Mat(template.rows(), template.cols(), template.type());
			
			List<MatOfPoint> template_contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(template_binary, template_contours , new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
			
			
			for (int i = 0; i < template_contours.size(); i++) {
				//if (contours.get(i).size().area() < area*.00015) continue;
				//System.out.println(area*.00015);
				Imgproc.drawContours(new_template, template_contours, i, new Scalar(255, 0, 0));
				//System.out.println("1. " + template_contours.get(i).size().area());
			}
			
			for (int i = 0; i < contours.size(); i++) {
				if (contours.get(i).size().area() < area*.00015) continue;
				//System.out.println(Imgproc.matchShapes(contours.get(i), template_contours.get(1), Imgproc.CV_CONTOURS_MATCH_I1, 0));
				double match = Imgproc.matchShapes(contours.get(i), template_contours.get(1), Imgproc.CV_CONTOURS_MATCH_I1, 0);
				if (match < 2) continue;
				Imgproc.drawContours(notes_only, contours, i, new Scalar(255, 0, 0));
			}
			
			Mat new_binary = new Mat(binary.rows(), binary.cols(), binary.type());
			
			//Imgproc.GaussianBlur(binary, binary, new Size(51,51), 0);
			
			Mat new_edges = new Mat(binary.rows(), binary.cols(), binary.type());			
			
			
			
			List<MatOfPoint> binary_contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(binary, binary_contours , new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
			for (int i = 0; i < binary_contours.size(); i++) {
				//Imgproc.drawContours(new_binary, binary_contours, i, new Scalar(255, 0, 0));
			}
			
			Imgproc.Canny(binary, new_edges, 50, 200);			
			
			Mat corners = new Mat(binary.rows(), binary.cols(), binary.type());
			
			//Imgproc.cornerHarris(new_binary, corners, 100, 15, .04);
			
			Mat houghlines_dst = new Mat();
			
			System.out.println(threshold);
			Imgproc.HoughLines(new_edges, houghlines_dst, 1, Math.PI/180, 600);
			
			Mat new_houghlines = new Mat(binary.rows(), binary.cols(), binary.type());
			
			Draw_Lines(new_edges, houghlines_dst);
			
			Highgui.imwrite("src/new_measure9.jpg", notes_only);
			
			System.out.println("Success");
			
		} catch(Exception e) {
			System.out.println(e);
			System.out.println("Fail");
		}
	}		
	*/
	}
}


