package gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.io.File;
import java.text.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class LearningSetPanel extends Panel implements Runnable, ActionListener{

    private ArrayList<String> savedImages;
    private int currentIndex = 0;
    private int pictureCount = 0;

    private MainFrame mainFrame;
    private VideoCapture videoCapture;
    private CustomLabel label;
    private Mat frame;
    private Color backgroundColor; 
    private Image image;
    private Graphics graphics;
    private Thread thread;

    private CustomButton newSetButton;
    private CustomLabel nameLabel;
    private CustomTextField nameTextField;

    private Image previewImage;
    private Image defaultImage;

    private CustomButton leftButton;
    private CustomButton deleteButton;
    private CustomButton rightButton;

    private CustomButton takePictureButton;
    private CustomButton autoPictureButton;
    private CustomTextField timeTextField;
    private CustomButton saveSetButton;

    public LearningSetPanel(MainFrame mainFrame){
        this.mainFrame = mainFrame;
        this.setLayout(null);

        this.backgroundColor = new Color(47, 47, 47);
        this.setBackground(this.backgroundColor);
        this.savedImages = new ArrayList<>();

        this.label = new CustomLabel("LEARNING FEED");
        this.label.setBounds(20, 10, 400, 40);
        this.add(label);

        this.videoCapture = new VideoCapture(0);
        this.frame = new Mat();
        this.videoCapture.read(frame);

        this.newSetButton = new CustomButton("New Set");
        this.newSetButton.setBounds(560, 60, 810, 50);
        this.newSetButton.addActionListener(this);
        this.add(newSetButton);

        this.nameLabel = new CustomLabel("Name");
        this.nameLabel.setBounds(560, 120, 77, 50);
        this.add(nameLabel);

        this.nameTextField = new CustomTextField("Alin");
        this.nameTextField.setBounds(645, 120, 725, 50);
        this.add(nameTextField);

        this.autoPictureButton = new CustomButton("Manual");
        this.autoPictureButton.setBounds(30, 465, 500, 50);
        this.autoPictureButton.addActionListener(this);
        this.add(autoPictureButton);

        this.timeTextField = new CustomTextField("100");
        this.timeTextField.setBounds(30, 525, 500, 50);
        this.timeTextField.setEditable(false);
        this.add(timeTextField);

        this.takePictureButton = new CustomButton("Take Picture");
        this.takePictureButton.setBounds(30, 585, 500, 50);
        this.takePictureButton.addActionListener(this);
        this.takePictureButton.setEnabled(false);
        this.add(takePictureButton);

        try{
            this.defaultImage = ImageIO.read(new File("/home/sion/Documents/Portofoliu/face_recognition_svm/resources/default.jpg"));
        }catch(IOException e){
            e.printStackTrace();
        }

        this.leftButton = new CustomButton("<");
        this.leftButton.setBounds(560, 525, 260, 50);
        this.leftButton.addActionListener(this);
        this.leftButton.setEnabled(false);
        this.add(leftButton);

        this.deleteButton = new CustomButton("Delete");
        this.deleteButton.setBounds(835, 525, 260, 50);
        this.deleteButton.addActionListener(this);
        this.deleteButton.setEnabled(false);
        this.add(deleteButton);

        this.rightButton = new CustomButton(">");
        this.rightButton.setBounds(1110, 525, 260, 50);
        this.rightButton.addActionListener(this);
        this.rightButton.setEnabled(false);
        this.add(rightButton);

        this.saveSetButton = new CustomButton("Save");
        this.saveSetButton.setBounds(560, 585, 810, 50);
        this.saveSetButton.addActionListener(this);
        this.saveSetButton.setEnabled(false);
        this.add(saveSetButton);

        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() == autoPictureButton){
            String text = autoPictureButton.getLabel();
            if(text.equals("Manual")){
                autoPictureButton.setLabel("Auto");
                timeTextField.setEditable(true);
            }else{
                autoPictureButton.setLabel("Manual");
                timeTextField.setEditable(false);
            }
        }
        if(event.getSource() == newSetButton){
            pictureCount = 0;
            String name = nameTextField.getText();
            if(!name.equals("")){
                newSetButton.setEnabled(false);
                nameTextField.setEnabled(false);
                autoPictureButton.setEnabled(false);
                takePictureButton.setEnabled(true);
                savedImages.clear();
                currentIndex = 0;
                new File("/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/" + name).mkdirs();
            }
        }
        if(event.getSource() == takePictureButton){
            leftButton.setEnabled(true);
            deleteButton.setEnabled(true);
            rightButton.setEnabled(true);

            String text = autoPictureButton.getLabel();
            if(text.equals("Manual")){
                manualTakePictures();
                if(pictureCount == 500)
                    saveSetButton.setEnabled(true);
            }else{
                takePictureButton.setEnabled(false);
                new Thread(() -> { 
                    autoTakePictures();
                    takePictureButton.setEnabled(true);
                    saveSetButton.setEnabled(true);
                }).start();
            }
        }
        if(event.getSource() == leftButton){
            if(currentIndex > 0){
                currentIndex--;
                loadPreview();
            }
        }
        if(event.getSource() == rightButton){
            if(currentIndex < savedImages.size() - 1){
                currentIndex++;
                loadPreview();
            }
        }
        if(event.getSource() == deleteButton){
            if(savedImages.size() > 0){
                new File(savedImages.get(currentIndex)).delete();
                savedImages.remove(currentIndex);

                if(currentIndex >= savedImages.size())
                    currentIndex = savedImages.size() - 1;

                if(savedImages.size() > 0)
                    loadPreview();
                else
                    previewImage = null;

                repaint();
            }
        }
        if(event.getSource() == saveSetButton){
            nameTextField.setEnabled(true);
            newSetButton.setEnabled(true);
            autoPictureButton.setEnabled(true);
            takePictureButton.setEnabled(false);
            saveSetButton.setEnabled(false);
            pictureCount = 0;
            savedImages.clear();
            previewImage = null;
            repaint();
        }
    }

    @Override
    public void run(){
        while(true){
            videoCapture.read(frame);
            if(!frame.empty()){
                repaint();
            } 
            try{
                Thread.sleep(100);
            }catch(InterruptedException e){
                break;
            }
        }
    }

    private void manualTakePictures(){
        String name, path;
        try{
            if(pictureCount >= 500) return;
            name = nameTextField.getText();
            path = "/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/" + name + "/" + name + "_" + pictureCount + ".jpg";
            ImageIO.write(rescale(matToBufferedImage(frame)), "jpg", new File(path));
            savedImages.add(path);
            currentIndex = savedImages.size() - 1;
            pictureCount++;
            loadPreview();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void autoTakePictures(){
        try{
            for(int i = 0; i < 500; i++){
                manualTakePictures();
                Thread.sleep(Integer.parseInt(timeTextField.getText()));
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public void loadPreview(){
        try{
            File file = new File(savedImages.get(currentIndex));
            previewImage = ImageIO.read(file);
            repaint();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void update(Graphics g){
        Image buffer = createImage(getWidth(), getHeight());
        Graphics bg = buffer.getGraphics();
        paint(bg);
        g.drawImage(buffer, 0, 0, this);
        bg.dispose();
    }

    public void paint(Graphics g){
        if(frame != null && !frame.empty()){
            g.drawImage(matToImage(frame), 30, 60, 500, 400, this);
        }
        if(previewImage != null){
            g.drawImage(previewImage, 800, 180, 330, 330, this);
        }else{
            g.drawImage(defaultImage, 800, 180, 330, 330, this);
        }
    }

    private Image matToImage(Mat mat){
        int width = mat.width();
        int height = mat.height();
        int[] pixels = new int[width * height];
        byte[] data = new byte[width * height *3];
        mat.get(0,0,data);
        for(int i = 0; i < width * height; i++){
            int r = data[i*3+2] & 0xff;
            int g = data[i*3+1] & 0xff;
            int b = data[i*3] & 0xff;
            pixels[i] = (0xff << 24) | (r << 16) | (g << 8) | b;
        }
        return createImage(new MemoryImageSource(width, height, pixels, 0, width));
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

    private BufferedImage rescale(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        double scaleX = width / 128.0;
        double scaleY = height / 128.0;
        int srcX, srcY;
        BufferedImage newImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < 128; i++){
            for(int j = 0; j < 128; j++){
                srcX = (int)(j * scaleX);
                srcY = (int)(i * scaleY);
                newImage.setRGB(j, i, image.getRGB(srcX, srcY));
            }
        }
        return newImage;
    }

    public void startCamera(){
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
