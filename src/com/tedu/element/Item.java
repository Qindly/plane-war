package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.manager.GameLoad;

public class Item extends ElementObj {

    private int speed = 1;
    private String type; // e.g., "powerup", "score"

    public Item() {}

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
            this.setW(30);
            this.setH(30);
        }
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(), this.getX(), this.getY(), this.getW(), this.getH(), null);
        }
    }

    @Override
    protected void move() {
        // Items can just fall down slowly
        this.setY(this.getY() + speed);

        if (this.getY() > 600) {
            this.setLive(false);
        }
    }

    // A method to apply the item's effect to the player
    public void applyEffect(Play player, long gameTime) {
        // logic for different item types
        if ("item_powerup".equals(this.type)) {
            player.powerUp(gameTime);
        } else if ("item_health".equals(this.type)) {
            player.heal();
        }
        this.setLive(false); // Item disappears after being collected
    }
} 