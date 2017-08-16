package com.sddev.botmaker;

import org.opencv.core.*;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public final class ImageFinder {
    private static final int matchMethod = Imgproc.TM_CCOEFF_NORMED;
    public static final java.awt.Point findImage(BufferedImage parentImage, BufferedImage childImage, int tolerance) {
        Mat parentMat = new Mat(parentImage.getHeight(), parentImage.getWidth(), CvType.CV_8UC3);
        Mat childMat = new Mat(childImage.getHeight(), childImage.getWidth(), CvType.CV_8UC3);
        BufferedImage pI = new BufferedImage(parentImage.getWidth(), parentImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage cI = new BufferedImage(childImage.getWidth(), childImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        pI.getGraphics().drawImage(parentImage, 0, 0, null);
        cI.getGraphics().drawImage(childImage, 0, 0, null);
        parentMat.put(0, 0, ((DataBufferByte) pI.getRaster().getDataBuffer()).getData());
        childMat.put(0, 0, ((DataBufferByte) cI.getRaster().getDataBuffer()).getData());
        int result_cols = parentMat.cols() - childMat.cols() + 1;
        int result_rows = parentMat.rows() - childMat.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
        Imgproc.matchTemplate(parentMat, childMat, result, matchMethod);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc;
        if (matchMethod == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }

        for(int i = 0 ; i < cI.getWidth() && i < cI.getHeight() ; i++) {
            Color c1 = new Color(cI.getRGB(i, i));
            Color c2 = new Color(pI.getRGB((int) matchLoc.x + i, (int) matchLoc.y + i));
            if(Math.abs(c1.getRed() - c2.getRed()) > tolerance || Math.abs(c1.getGreen() - c2.getGreen()) > tolerance || Math.abs(c1.getBlue() - c2.getBlue()) > tolerance) {
                return null;
            }
        }

        return new java.awt.Point((int) matchLoc.x, (int) matchLoc.y);
    }
    static {
        System.loadLibrary("opencv_java249");
    }
}