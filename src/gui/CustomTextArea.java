package gui;

import java.awt.*;

public class CustomTextArea extends TextArea{

    private Color backgroundColor;
    private Color textColor;
    private Font font;

    public CustomTextArea(String text){
        super(text);

        this.setEditable(false);

        this.backgroundColor = Color.WHITE;
        this.textColor = new Color(128, 24, 24);
        this.font = new Font("Courier New", Font.BOLD, 24);

        this.setBackground(this.backgroundColor);
        this.setForeground(this.textColor);
        this.setFont(this.font);

    }
}
