package gui;

import java.awt.*;

public class CustomLabel extends Label{

    private Color color;
    private Font font;

    public CustomLabel(String text){
        super(text);

        this.color = Color.WHITE;
        this.font = new Font("Courier New", Font.BOLD, 25);

        this.setForeground(this.color);
        this.setFont(this.font);
    }

}
