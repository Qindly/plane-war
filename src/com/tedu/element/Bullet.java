package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;

public class Bullet extends ElementObj {

    private int speed = 10;
    private String direction; // "up" or "down" depending on who shot it

    public Bullet() {}

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        String owner = arr[0];
        int x, y;
        ImageIcon icon;

        switch(owner) {
            case "play":
                icon = GameLoad.imgMap.get("play_bullet");
                x = Integer.parseInt(arr[1]) - icon.getIconWidth()/2;
                y = Integer.parseInt(arr[2]);
                this.direction = "up";
                break;
            case "boss":
                icon = GameLoad.imgMap.get("boss_bullet");
                x = Integer.parseInt(arr[1]) - icon.getIconWidth()/2;
                y = Integer.parseInt(arr[2]);
                this.direction = "down";
                break;
            case "enemy":
                // similar logic for enemy bullets if they have different sprites/starting positions
                icon = GameLoad.imgMap.get("enemy_bullet");
                 x = Integer.parseInt(arr[1]) - icon.getIconWidth()/2;
                y = Integer.parseInt(arr[2]);
                this.direction = "down";
                 break;
            default:
                // default case, maybe an error or a generic bullet
                icon = GameLoad.imgMap.get("play_bullet");
                x = Integer.parseInt(arr[1]);
                y = Integer.parseInt(arr[2]);
                this.direction = "up";
        }
        
        this.setX(x);
        this.setY(y);
        this.setW(icon.getIconWidth());
        this.setH(icon.getIconHeight());
        this.setIcon(icon);

        return this;
    }

    @Override
    public void showElement(Graphics g) {
        g.drawImage(this.getIcon().getImage(), this.getX(), this.getY(), this.getW(), this.getH(), null);
    }

    @Override
    protected void move() {
        if ("up".equals(direction)) {
            this.setY(this.getY() - speed);
        } else if ("down".equals(direction)) {
            this.setY(this.getY() + speed);
        }

        // if bullet goes off screen, set it to not live
        if (this.getY() < -this.getH() || this.getY() > 600) { // assuming 600 is screen height
            this.setLive(false);
        }
    }
} 