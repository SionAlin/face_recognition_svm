package gui;

import hog.*;
import svm.*;
import java.io.*;
import java.util.*;
import java.util.Comparator;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.*;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class CameraPanel extends Panel implements Runnable{

    private SVMClassifier headDetector;
    private SVMClassifier personClf;
    private ArrayList<SVMClassifier> recognizer;
    private ArrayList<Object[]> detections;
    private MainFrame mainFrame;
    private VideoCapture videoCapture;
    private CustomLabel label;
    private Mat frame;
    private Color backgroundColor; 
    private Image image;
    private Graphics graphics;
    private Thread thread;
    private HOG hog = new HOG();

    public CameraPanel(MainFrame mainFrame){
        this.detections = new ArrayList<>();

        this.mainFrame = mainFrame;
        this.setLayout(null);

        this.backgroundColor = new Color(47, 47, 47);
        this.setBackground(this.backgroundColor);

        this.label = new CustomLabel("LIVE CAMERA FEED");
        this.label.setBounds(20, 10, 400, 40);
        this.add(label);

        this.videoCapture = new VideoCapture(0);
        this.frame = new Mat();
        this.videoCapture.read(frame);

        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            if(videoCapture != null && !frame.empty() && videoCapture.read(frame)){

                ArrayList<Object[]> tempDetection = slidingWindow(frame);

                synchronized(this){
                    detections = tempDetection;
                }
                repaint();
            }
            try{
                Thread.sleep(200);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private ArrayList<Object[]> slidingWindow(Mat mat){
        int width = mat.cols();
        int height = mat.rows();
        int step = 40;

        java.util.List<Rectangle> rects = new java.util.ArrayList<>();
        java.util.List<double[]> hogVecs = new java.util.ArrayList<>();

        Mat window;
        BufferedImage img;
        double[] hogVec;

        for(int i = 0; i <= height - 128; i+=step){
            for(int j = 0; j <= width - 128; j+=step){
                window = mat.submat(i, i+128, j, j+128);
                img = matToBufferedImage(window);
                hogVec = hog.computeHOG(img);
                if(headDetector != null && headDetector.predict(hogVec) == 1){
                    rects.add(new Rectangle(j, i, 128, 128));
                    hogVecs.add(hogVec);
                }
                window.release();
            }
        }

        if(rects.isEmpty()){
            return new ArrayList<>();
        }

        java.util.List<java.util.List<Integer>> groups = new java.util.ArrayList<>();
        boolean[] used = new boolean[rects.size()];

        for(int i = 0; i < rects.size(); i++){
            if(!used[i]){
                java.util.List<Integer> group = new java.util.ArrayList<>();
                java.util.Queue<Integer> queue = new java.util.LinkedList<>();
                queue.add(i);
                used[i] = true;
                while(!queue.isEmpty()){
                    int curr = queue.poll();
                    group.add(curr);
                    Rectangle rCurr = rects.get(curr);
                    for(int j = 0; j < rects.size(); j++){
                        if(!used[j]){
                            Rectangle rJ = rects.get(j);
                            if(calculateIoU(rCurr, rJ) > 0.5){
                                used[j] = true;
                                queue.add(j);
                            }
                        }
                    }
                }
                groups.add(group);
            }
        }

        java.util.List<Integer> bestGroup = groups.stream().max(Comparator.comparingInt(java.util.List::size)).orElse(java.util.Collections.emptyList());
        if(bestGroup.isEmpty()){
            return new ArrayList<>();
        }

        int sumX = 0, sumY = 0;
        for(int idx : bestGroup){
            Rectangle r = rects.get(idx);
            sumX += r.x;
            sumY += r.y;
        }
        int avgX = sumX / bestGroup.size();
        int avgY = sumY / bestGroup.size();

        double[] bestHog = hogVecs.get(bestGroup.get(0));

        ArrayList<Object[]> result = new ArrayList<>();
        result.add(new Object[]{avgX, avgY, 128, 128, bestHog});
        return result;
    }

    private double calculateIoU(Rectangle r1, Rectangle r2){
        int xA = Math.max(r1.x, r2.x);
        int yA = Math.max(r1.y, r2.y);

        int xB = Math.min(r1.x + r1.width, r2.x + r2.width);
        int yB = Math.min(r1.y + r1.height, r2.y + r2.height);

        int interArea = Math.max(0, xB - xA) * Math.max(0, yB - yA);
        int area1 = r1.width * r1.height;
        int area2 = r2.width * r2.height;

        return (double) interArea / (area1 + area2 - interArea);
    }


    public void update(Graphics g){
        Image buffer = createImage(getWidth(), getHeight());
        Graphics bg = buffer.getGraphics();
        paint(bg);
        g.drawImage(buffer,0,0,this);
        bg.dispose();
    }

    public void paint(Graphics g){
        if(frame == null || frame.empty()) return;

        Mat display = frame.clone();
        synchronized(this){
            if(detections.size() > 0 && detections != null){
                Object[] det = detections.get(0);
                int x = (int)det[0];
                int y = (int)det[1];
                System.out.println("x = " + x + "\ny = " + y);
                double[] hog = (double[])det[4];
                Imgproc.rectangle(display, new org.opencv.core.Point(x, y), new org.opencv.core.Point(x+128, y+128), new Scalar(96,16,8), 2);
                if(recognizer != null){
                    for(SVMClassifier clf : recognizer){
                        if(clf.predict(hog) == 1){
                            Imgproc.putText(display, clf.personName, new org.opencv.core.Point(x, y-10), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(96,16,8), 2);
                            break;
                        }
                    }
                }
            }
        }

        g.drawImage(matToImage(display), 30, 60, getWidth()-60, getHeight()-90, this);
        display.release();
    }

    private BufferedImage matToBufferedImage(Mat mat){
        int width = mat.width();
        int height = mat.height();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[width * height];
        byte[] data = new byte[width * height * 3];
        mat.get(0, 0, data);
        for(int i = 0; i < width * height; i++){
            int r = data[i*3+2] & 0xff;
            int g = data[i*3+1] & 0xff;
            int b = data[i*3] & 0xff;
            pixels[i] = (r << 16) | (g << 8) | b;
        }
        img.setRGB(0, 0, width, height, pixels, 0, width);
        return img;
    }

    private Image matToImage(Mat mat){
        int width = mat.width();
        int height = mat.height();
        int[] pixels = new int[width * height];
        byte[] data = new byte[width * height * 3];
        mat.get(0,0,data);
        for(int i = 0; i < width * height; i++){
            int r = data[i*3+2] & 0xff;
            int g = data[i*3+1] & 0xff;
            int b = data[i*3] & 0xff;
            pixels[i] = (0xff << 24) | (r << 16) | (g << 8) | b;
        }
        return createImage(new MemoryImageSource(width, height, pixels, 0, width));
    }

    public void startCamera(){

        File modelFile = new File("data/face_models/head_detector.model");
        if(modelFile.exists())
            headDetector = SVMClassifier.load("data/face_models/head_detector.model");

        File modelDir = new File("data/face_models/");
        if(modelDir.exists()){
            File[] modelFiles = modelDir.listFiles(File::isFile);
            recognizer = new ArrayList<>();

            for(File model : modelFiles){
                if(model.getName().equals("head_detector.model")) continue;
                SVMClassifier clf = SVMClassifier.load(model.getPath());
                recognizer.add(clf);
            }
        }

        if(!videoCapture.isOpened())
            videoCapture.open(0);

        thread = new Thread(this);
        thread.start();
    }

    public void stopCamera(){
        thread.interrupt();
        videoCapture.release();
    }
}
