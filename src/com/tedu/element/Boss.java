package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

public class Boss extends ElementObj {

    private int speedX = 2;
    private int speedY = 1;
    private boolean movingRight = true;
    private int health = 10;
    
    private long attackCooldown = 100; // frames
    private long attackStartTime = 0;

    public Boss() {}

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        this.setX(Integer.parseInt(arr[0]));
        this.setY(Integer.parseInt(arr[1]));
        ImageIcon icon = GameLoad.imgMap.get("boss");
        if (icon != null) {
            this.setW(icon.getIconWidth());
            this.setH(icon.getIconHeight());
            this.setIcon(icon);
        } else {
            this.setW(150);
            this.setH(100);
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
        // Phase 1: Move down into the screen
        if (this.getY() < 100) {
            this.setY(this.getY() + speedY);
        } else {
            // Phase 2: Move left and right
            if (movingRight) {
                this.setX(this.getX() + speedX);
                if (this.getX() > 800 - this.getW()) {
                    movingRight = false;
                }
            } else {
                this.setX(this.getX() - speedX);
                if (this.getX() < 0) {
                    movingRight = true;
                }
            }
        }
    }

    @Override
    protected void add(long gameTime) {
        if ((gameTime - attackStartTime > attackCooldown) && this.getY() > 0) {
            attackStartTime = gameTime;
            // Simple attack: a single bullet downwards
            ElementObj bullet = GameLoad.getObj("bullet").createElement(getBulletCreationString());
            ElementManager.getManager().addElement(bullet, GameElement.ENEMYFILE);
        }
    }

    private String getBulletCreationString() {
        int bulletX = this.getX() + this.getW() / 2;
        int bulletY = this.getY() + this.getH();
        return "boss," + bulletX + "," + bulletY;
    }

    public void getHit() {
        this.health--;
        if (this.health <= 0) {
            this.setLive(false);
        }
    }
} 