package gui;

import hog.*;
import svm.*;
import java.io.*;
import java.util.*;
import java.awt.*;
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

    public CameraPanel(MainFrame mainFrame){
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
        while(true){
            videoCapture.read(frame);
            if(!frame.empty()){
                detections = slidingWindow(frame);
                repaint();
            }
            try{
                Thread.sleep(100);
            }catch(InterruptedException e){
                break;
            }
        }
    }

    private ArrayList<Object[]> slidingWindow(Mat mat){
        int width = mat.width();
        int height = mat.height();

        int step = 64;

        detections = new ArrayList<>();
        HOG hog = new HOG();

        for(int i = 0; i + 128 < height; i+=step){
            for(int j = 0; j + 128 < width; j+=step){
                Mat window = mat.submat(i, i+128, j, j+128);
                BufferedImage img = matToBufferedImage(window);
                double[] hogVec = hog.computeHOG(img);
                if(headDetector != null && headDetector.predict(hogVec) == 1){
                    detections.add(new Object[]{j, i, 128, 128, hogVec});
                }
            }
        }
        return detections;
    }

    public void update(Graphics g){
        Image buffer = createImage(getWidth(), getHeight());
        Graphics bg = buffer.getGraphics();
        paint(bg);
        g.drawImage(buffer,0,0,this);
        bg.dispose();
    }

    public void paint(Graphics g){
        for(Object[] det : detections){
            int x = (int)det[0];
            int y = (int)det[1];
            double[] hog = (double[])det[4];
            Imgproc.rectangle(frame, new org.opencv.core.Point(x, y), new org.opencv.core.Point(x+128, y+128), new Scalar(96,16,8), 2);
            if(recognizer != null){
                for(SVMClassifier clf : recognizer){
                    if(clf.predict(hog) == 1){
                        Imgproc.putText(frame, clf.personName, new org.opencv.core.Point(x, y-10), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(96,16,8), 2);
                        break;
                    }
                }
            }
        }
        if(frame != null && !frame.empty())
            g.drawImage(matToImage(frame), 30, 60, getWidth()-60, getHeight()-90, this);
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
