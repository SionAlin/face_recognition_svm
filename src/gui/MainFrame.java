package gui;

import java.awt.*;
import java.awt.event.*;

public class MainFrame extends Frame implements ActionListener{

    private int height, width;
    private Color backgroundColor;
    public CameraPanel cameraPanel;
    public LearningSetPanel learningSetPanel;
    public TrainingConfigPanel trainingConfigPanel;
    private CustomButton cameraButton;
    private CustomButton learningSetButton;
    private CustomButton trainingConfigButton;

    public MainFrame(){
        this.setTitle("Face Recognition");

        this.height = 900;
        this.width = 1500;
        this.setSize(this.width, this.height);

        this.setResizable(false);
        this.setLayout(null);

        this.backgroundColor = new Color(206, 206, 206);
        this.setBackground(backgroundColor);

        //Panels
        this.cameraPanel = new CameraPanel(this);
        this.learningSetPanel = new LearningSetPanel(this);
        this.trainingConfigPanel = new TrainingConfigPanel(this);
        
        this.cameraPanel.setBounds(50, 70, 1400, 655);
        this.learningSetPanel.setBounds(50, 70, 1400, 655);
        this.trainingConfigPanel.setBounds(50, 70, 1400, 655);

        this.cameraPanel.setVisible(true);
        this.learningSetPanel.setVisible(false);
        this.trainingConfigPanel.setVisible(false);

        this.add(this.cameraPanel);
        this.add(this.learningSetPanel);
        this.add(this.trainingConfigPanel);

        //Buttons
        this.cameraButton = new CustomButton("Live Camera");
        this.cameraButton.addActionListener(this);
        this.cameraButton.setBounds(50, 775, 453, 75);

        this.learningSetButton = new CustomButton("Learning Set");
        this.learningSetButton.addActionListener(this);
        this.learningSetButton.setBounds(523, 775, 453, 75);

        this.trainingConfigButton = new CustomButton("Training Config");
        this.trainingConfigButton.addActionListener(this);
        this.trainingConfigButton.setBounds(996, 775, 453, 75);

        this.add(this.cameraButton);
        this.add(this.learningSetButton);
        this.add(this.trainingConfigButton);

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                cameraPanel.stopCamera();
                learningSetPanel.stopCamera();
                System.exit(0);
            }
        });

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event){
        cameraPanel.stopCamera();
        learningSetPanel.stopCamera();

        cameraPanel.setVisible(false);
        learningSetPanel.setVisible(false);
        trainingConfigPanel.setVisible(false);

        if(event.getSource() == cameraButton){
            cameraPanel.setVisible(true);
            cameraPanel.startCamera();
        }else if(event.getSource() == learningSetButton){
            learningSetPanel.setVisible(true);
            learningSetPanel.startCamera();
        }else if(event.getSource() == trainingConfigButton)
            trainingConfigPanel.setVisible(true);

        repaint();
    }

}
