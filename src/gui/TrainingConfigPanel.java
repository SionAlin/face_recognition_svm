package gui;

import hog.*;
import svm.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TrainingConfigPanel extends Panel implements ActionListener{

    private SVMClassifier headDetector;
    private SVMClassifier personClf;
    private MainFrame mainFrame;
    private Color backgroundColor;
    private CustomLabel label;
    private CustomButton extractHOGButton;
    private CustomButton trainFaceDetectorButton;
    private CustomButton trainFaceRecognitionButton;
    private CustomButton saveModelButton;
    private CustomTextArea feedbackTextArea;

    public TrainingConfigPanel(MainFrame mainFrame){
        this.mainFrame = mainFrame;
        this.setLayout(null);

        this.backgroundColor = new Color(47, 47, 47);
        this.setBackground(this.backgroundColor);

        this.label = new CustomLabel("TRAINING CONFIG FEED");
        this.label.setBounds(20, 10, 400, 40);
        this.add(label);

        this.extractHOGButton = new CustomButton("Extract HOG");
        this.extractHOGButton.setBounds(20, 60, 685, 50);
        this.extractHOGButton.addActionListener(this);
        this.add(this.extractHOGButton);

        this.trainFaceDetectorButton = new CustomButton("Train Face Detector");
        this.trainFaceDetectorButton.setBounds(20, 320, 685, 50);
        this.trainFaceDetectorButton.addActionListener(this);
        this.add(this.trainFaceDetectorButton);

        this.trainFaceRecognitionButton = new CustomButton("Train Face Recognition");
        this.trainFaceRecognitionButton.setBounds(20, 585, 1360, 50);
        this.trainFaceRecognitionButton.addActionListener(this);
        this.add(this.trainFaceRecognitionButton);

        this.feedbackTextArea = new CustomTextArea("");
        this.feedbackTextArea.setBounds(715, 60, 660, 515);
        this.add(this.feedbackTextArea);

    }

    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() == extractHOGButton){
            new Thread(() -> {
                File rawDir = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/");
                File[] persons = rawDir.listFiles(File::isDirectory);

                for(File person : persons){
                    String name = person.getName();
                    if(name.equals("faces") || name.equals("non_faces")) continue;
                    feedbackTextArea.append("Extracting HOG for " + name + " ... \n");
                    extractHOG(name);
                    feedbackTextArea.append("Done " + name + "!\n");
                }
            }).start();
        }
        if(event.getSource() == trainFaceDetectorButton){
            new Thread(() -> {
            feedbackTextArea.append("Extracting HOG for faces ...\n");
            extractHOG("faces");
            feedbackTextArea.append("Done!\n");

            feedbackTextArea.append("Extracting HOG for non faces ...\n");
            extractHOG("non_faces");
            feedbackTextArea.append("Done!\n");

            double[][] facesHOG = Read("/home/sion/Documents/Portofoliu/face_recognition_svm/data/hog_training/faces.hog");
            double[][] nonFacesHOG = Read("/home/sion/Documents/Portofoliu/face_recognition_svm/data/hog_training/non_faces.hog");

            int total = facesHOG.length + nonFacesHOG.length;
            double[][] allHOG = new double[total][];
            int[] labels = new int[total];

            for(int i = 0; i < facesHOG.length; i++){
                allHOG[i] = facesHOG[i];
                labels[i] = 1;
            }

            for(int i = 0; i < nonFacesHOG.length; i++){
                allHOG[facesHOG.length + i] = nonFacesHOG[i];
                labels[facesHOG.length + i] = -1;
            }

            SMO smo = new SMO(0.6, 1e-3, 200, 1e-4, 0.0);

            feedbackTextArea.append("Training model ... \n");
            headDetector = smo.train(allHOG, labels, "head_detector");

            feedbackTextArea.append("Head detector trained!\n");
            headDetector.save("/home/sion/Documents/Portofoliu/face_recognition_svm/data/face_models/head_detector.model");

            feedbackTextArea.append("Done!");
            }).start();
        }
        if(event.getSource() == trainFaceRecognitionButton){
            new Thread(() -> {
                feedbackTextArea.append("Reading... \n");
                File rawDir = new File("/home/sion/Documents/Portofoliu/face_recognition_svm/data/hog_training/");
                File[] persons = rawDir.listFiles(File::isFile);

                for(File person : persons){
                    String name = person.getName().replace(".hog", "");
                    if(name.equals("faces") || name.equals("non_faces")) continue;

                    feedbackTextArea.append("Training for " + name + " ...\n");

                    double[][] personHOG = Read(person.getPath());

                    int negCount = 0;
                    for(File p : persons){
                        String pName = p.getName().replace(".hog", "");
                        if(pName.equals(name) || pName.equals("faces") || pName.equals("non_faces")) continue;
                        double[][] other = Read(p.getPath());
                        negCount += other.length;
                    }

                    int total = personHOG.length + negCount;
                    double[][]allHOG = new double[total][];
                    int[] labels = new int[total];

                    for(int i = 0; i < personHOG.length; i++){
                        allHOG[i] = personHOG[i];
                        labels[i] = 1;
                    }

                    int idx = personHOG.length;
                    for(File p : persons){
                        String pName = p.getName().replace(".hog", "");
                        if(pName.equals(name) || pName.equals("faces") || pName.equals("non_faces")) continue;
                        double[][] other = Read(p.getPath());
                        for(int i = 0; i < other.length; i++){
                            allHOG[idx] = other[i];
                            labels[idx] = -1;
                            idx++;
                        }
                    }

                    SMO smo = new SMO(0.6, 1e-3, 300, 1e-6, -1.0);

                    feedbackTextArea.append("Training model ...\n");
                    personClf = smo.train(allHOG, labels, name);

                    feedbackTextArea.append(name + " model trained!\n");
                    personClf.save("/home/sion/Documents/Portofoliu/face_recognition_svm/data/face_models/" + name + ".model");

                    feedbackTextArea.append("Done " + name + "!\n");
                }
            }).start();
        }
    }

    private void extractHOG(String name){
        try {
            String pathFrom = "/home/sion/Documents/Portofoliu/face_recognition_svm/data/raw/" + name;
            String pathTo = "/home/sion/Documents/Portofoliu/face_recognition_svm/data/hog_training/";

            File dir = new File(pathFrom);
            File[] files = dir.listFiles(File::isFile);
            BufferedImage image;
            HOG hog = new HOG();
            double[][] allHOG = new double[files.length][];

            for(int i = 0; i < files.length; i++){
                image = ImageIO.read(files[i]);
                allHOG[i] = hog.computeHOG(image);
                if(image == null) continue;
                allHOG[i] = hog.computeHOG(image);
            }

            Write(pathTo + name + ".hog", allHOG);

        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void Write(String path, double[][] arrayHOG){
        try{
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(arrayHOG);
        oos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private double[][] Read(String path){
        double[][] allHOG = null;
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
            allHOG = (double[][]) ois.readObject();
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        return allHOG;
    }
}
