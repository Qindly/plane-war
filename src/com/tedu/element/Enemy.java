package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

public class Enemy extends ElementObj {

    private int speed = 1;
    private String type;
    private long attackCooldown = 150; // frames
    private long attackStartTime = 0;

    public Enemy() {}

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        this.setX(Integer.parseInt(arr[0]));
        this.setY(Integer.parseInt(arr[1]));
        this.type = arr[2];
        ImageIcon icon = GameLoad.imgMap.get(this.type);
        if (icon != null) {
            this.setW(icon.getIconWidth());
            this.setH(icon.getIconHeight());
            this.setIcon(icon);
        } else {
             // Set default size if image not found, or handle error
            this.setW(50);
            this.setH(50);
        }
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        if(this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(), this.getX(), this.getY(), this.getW(), this.getH(), null);
        }
    }

    @Override
    protected void move() {
        // Simple downward movement
        this.setY(this.getY() + speed);
        
        // If enemy goes off screen, set it to not live
        if (this.getY() > 600) { // assuming 600 is screen height
            this.setLive(false);
        }
    }

    @Override
    protected void add(long gameTime) {
        if ((gameTime - attackStartTime > attackCooldown) && this.getY() > 0) { // don't shoot when off-screen
            attackStartTime = gameTime;
            ElementObj bullet = GameLoad.getObj("bullet").createElement(getBulletCreationString());
            ElementManager.getManager().addElement(bullet, GameElement.ENEMYFILE);
        }
    }

    private String getBulletCreationString() {
        int bulletX = this.getX() + this.getW() / 2;
        int bulletY = this.getY() + this.getH();
        return "enemy," + bulletX + "," + bulletY;
    }
} 