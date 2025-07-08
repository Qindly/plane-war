package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameJFrame;

public class Background extends ElementObj {

    private int y2; // y-coordinate for the second image

    public Background() {}

    @Override
    public ElementObj createElement(String str) {
        this.setX(0);
        this.setY(0);
        ImageIcon icon = GameLoad.imgMap.get("background");
        if (icon != null) {
            this.setW(GameJFrame.GameX);
            this.setH(icon.getIconHeight());
            this.setIcon(icon);
        } else {
            // Set default size for the window
            this.setW(GameJFrame.GameX);
            this.setH(GameJFrame.GameY);
        }
        this.y2 = -this.getH(); // Position the second image directly above the first
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(), this.getX(), this.getY(), this.getW(), this.getH(), null);
            g.drawImage(this.getIcon().getImage(), this.getX(), this.y2, this.getW(), this.getH(), null);
        }
    }

    @Override
    protected void move() {
        this.setY(this.getY() + 1);
        this.y2 += 1;
        
        if (this.getY() >= this.getH()) {
            this.setY(-this.getH());
        }
        if (this.y2 >= this.getH()) {
            this.y2 = -this.getH();
        }
    }
} 