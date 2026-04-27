package gui;

import java.awt.*;

public class CustomButton extends Button{
    
    private Color buttonColor;
    private Color textColor;
    private Font textFont;
    private Dimension buttonDimension;

    public CustomButton(String text){
        super(text);

        this.buttonColor = new Color(128, 24, 24);
        this.textColor = Color.WHITE;
        this.textFont = new Font("Courier New", Font.BOLD, 30);
        this.buttonDimension = new Dimension(450, 100);

        this.setBackground(buttonColor);
        this.setForeground(textColor);
        this.setFont(textFont);
        this.setPreferredSize(buttonDimension);
    }
}
