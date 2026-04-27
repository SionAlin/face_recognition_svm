package gui; 

import java.awt.*;

class CustomTextField extends TextField{

    private Color backgroundColor;
    private Color textColor;
    private Font font;

    public CustomTextField(String placeholder){
        super(placeholder);

        this.backgroundColor = Color.WHITE;
        this.textColor = new Color(128, 24, 24);
        this.font = new Font("Courier New", Font.BOLD, 24);

        this.setBackground(this.backgroundColor);
        this.setForeground(this.textColor);
        this.setFont(this.font);

    }
}
