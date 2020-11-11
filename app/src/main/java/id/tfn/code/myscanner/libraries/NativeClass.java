package id.tfn.code.myscanner.libraries;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import id.tfn.code.myscanner.helpers.ImageUtils;
import id.tfn.code.myscanner.helpers.MathUtils;

import static id.tfn.code.myscanner.helpers.ImageUtils.bitmapToMat;
import static id.tfn.code.myscanner.helpers.ImageUtils.matToBitmap;

public class NativeClass {

    static {
        System.loadLibrary("opencv_java3");
    }

    int THRESHOLD_LEVEL = 1;
    double AREA_LOWER_THRESHOLD = 0.2;
    double AREA_UPPER_THRESHOLD = 0.98;
    private static final double DOWNSCALE_IMAGE_SIZE = 600f;

    public Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        id.tfn.code.myscanner.libraries.PerspectiveTransformation perspective = new id.tfn.code.myscanner.libraries.PerspectiveTransformation();
        MatOfPoint2f rectangle = new MatOfPoint2f();
        rectangle.fromArray(new Point(x1, y1), new Point(x2, y2), new Point(x3, y3), new Point(x4, y4));
        Mat dstMat = perspective.transform(bitmapToMat(bitmap), rectangle);
        return matToBitmap(dstMat);
    }

    private static Comparator<MatOfPoint2f> AreaDescendingComparator = new Comparator<MatOfPoint2f>() {
        public int compare(MatOfPoint2f m1, MatOfPoint2f m2) {
            double area1 = Imgproc.contourArea(m1);
            double area2 = Imgproc.contourArea(m2);
            return (int) Math.ceil(area2 - area1);
        }
    };

    public Bitmap FilterGray(Bitmap bitmap){
        Mat src = bitmapToMat(bitmap);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        bitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bitmap);
        return bitmap;
    }

    public Bitmap FilterMagic(Bitmap bitmap) {
        Mat mbgra = bitmapToMat(bitmap);
        Imgproc.cvtColor(mbgra, mbgra, Imgproc.COLOR_RGB2GRAY);
        bitmap = Bitmap.createBitmap(mbgra.cols(), mbgra.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mbgra, bitmap);
        Mat dst = mbgra.clone();
        float alpha = 1.9f;
        float beta = -80;
        dst.convertTo(dst, -1, alpha, beta);

        return matToBitmap(dst);
    }

    public MatOfPoint2f getPoint(Bitmap bitmap) {

        Mat src = bitmapToMat(bitmap);

        // Downscale image for better performance.
        double ratio = DOWNSCALE_IMAGE_SIZE / Math.max(src.width(), src.height());
        Size downscaledSize = new Size(src.width() * ratio, src.height() * ratio);
        Mat downscaled = new Mat(downscaledSize, src.type());
        Imgproc.resize(src, downscaled, downscaledSize);

        List<MatOfPoint2f> rectangles = getPoints(downscaled);
        if (rectangles.size() == 0) {
            Log.i("THRESHOLD LEVEL", THRESHOLD_LEVEL + "");
            THRESHOLD_LEVEL += 10;
            if(THRESHOLD_LEVEL > 50){
                return null;
            }
            return getPoint(bitmap);
        }
        Collections.sort(rectangles, AreaDescendingComparator);
        MatOfPoint2f largestRectangle = rectangles.get(0);
        MatOfPoint2f result = MathUtils.scaleRectangle(largestRectangle, 1f / ratio);
        return result;
    }

    //public native float[] getPoints(Bitmap bitmap);
    public List<MatOfPoint2f> getPoints(Mat src) {

        // Blur the image to filter out the noise.
        Mat blurred = new Mat();
        Imgproc.medianBlur(src, blurred, 9);

        // Set up images to use.
        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U);
        Mat gray = new Mat();

        // For Core.mixChannels.
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint2f> rectangles = new ArrayList<>();

        List<Mat> sources = new ArrayList<>();
        sources.add(blurred);
        List<Mat> destinations = new ArrayList<>();
        destinations.add(gray0);

        // To filter rectangles by their areas.
        int srcArea = src.rows() * src.cols();

        // Find squares in every color plane of the image.
        for (int c = 0; c < 3; c++) {
            int[] ch = {c, 0};
            MatOfInt fromTo = new MatOfInt(ch);

            Core.mixChannels(sources, destinations, fromTo);

            // Try several threshold levels.
            for (int l = 0; l < THRESHOLD_LEVEL; l++) {
                if (l == 0) {

                    // HACK: Use Canny instead of zero threshold level.
                    // Canny helps to catch squares with gradient shading.
                    // NOTE: No kernel size parameters on Java API.
                    Imgproc.Canny(gray0, gray, 80, 100);

                    // Dilate Canny output to remove potential holes between edge segments.
                    Imgproc.dilate(gray, gray, Mat.ones(new Size(3, 3), 0));
                } else {
                    int threshold = (l + 1) * 255 / THRESHOLD_LEVEL;
                    Imgproc.threshold(gray0, gray, threshold, 255, Imgproc.THRESH_BINARY);
                }


                // Find contours and store them all as a list.
                Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f contourFloat = MathUtils.toMatOfPointFloat(contour);
                    double arcLen = Imgproc.arcLength(contourFloat, true) * 0.02;

                    // Approximate polygonal curves.
                    MatOfPoint2f approx = new MatOfPoint2f();
                    Imgproc.approxPolyDP(contourFloat, approx, arcLen, true);

                    if (isRectangle(approx, srcArea)) {
                        rectangles.add(approx);
                    }
                }
            }
        }

        return rectangles;

    }

    private boolean isRectangle(MatOfPoint2f polygon, int srcArea) {
        MatOfPoint polygonInt = MathUtils.toMatOfPointInt(polygon);

        if (polygon.rows() != 4) {
            return false;
        }

        double area = Math.abs(Imgproc.contourArea(polygon));
        if (area < srcArea * AREA_LOWER_THRESHOLD || area > srcArea * AREA_UPPER_THRESHOLD) {
            return false;
        }

        if (!Imgproc.isContourConvex(polygonInt)) {
            return false;
        }

        // Check if the all angles are more than 72.54 degrees (cos 0.3).
        double maxCosine = 0;
        Point[] approxPoints = polygon.toArray();

        for (int i = 2; i < 5; i++) {
            double cosine = Math.abs(MathUtils.angle(approxPoints[i % 4], approxPoints[i - 2], approxPoints[i - 1]));
            maxCosine = Math.max(cosine, maxCosine);
        }

        if (maxCosine >= 0.3) {
            return false;
        }

        return true;
    }

}
