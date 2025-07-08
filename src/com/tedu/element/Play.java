package com.tedu.element;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

public class Play extends ElementObj {

    private boolean left = false;
    private boolean up = false;
    private boolean right = false;
    private boolean down = false;
    
    private long attackStartTime = 0;
    private long attackCooldown = 20; // Initial cooldown: 20 frames

    private int speed = 5;
    private int health = 3;
    private boolean isInvincible = false;
    private long invincibleStartTime = 0;
    private final long invincibleDuration = 180; // 3 seconds at 60fps approx (3 * 60)

    private boolean isPoweredUp = false;
    private long powerUpStartTime = 0;
    private final long powerUpDuration = 300; // 5 seconds
    private final long baseAttackCooldown = 20;
    private final long poweredUpAttackCooldown = 5;

    public Play() {
        this.attackCooldown = baseAttackCooldown;
    }

    @Override
    public ElementObj createElement(String str) {
        String[] arr = str.split(",");
        this.setX(Integer.parseInt(arr[0]));
        this.setY(Integer.parseInt(arr[1]));
        ImageIcon icon = GameLoad.imgMap.get("play");
        this.setW(icon.getIconWidth());
        this.setH(icon.getIconHeight());
        this.setIcon(icon);
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        if (isInvincible) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.drawImage(this.getIcon().getImage(),
                    this.getX(), this.getY(),
                    this.getW(), this.getH(), null);
            g2d.dispose();
        } else {
            g.drawImage(this.getIcon().getImage(),
                    this.getX(), this.getY(),
                    this.getW(), this.getH(), null);
        }
    }

    @Override
    public void keyClick(boolean bl, int key) {
        if (bl) {
            switch (key) {
                case KeyEvent.VK_W: up = true; break;
                case KeyEvent.VK_S: down = true; break;
                case KeyEvent.VK_A: left = true; break;
                case KeyEvent.VK_D: right = true; break;
            }
        } else {
            switch (key) {
                case KeyEvent.VK_W: up = false; break;
                case KeyEvent.VK_S: down = false; break;
                case KeyEvent.VK_A: left = false; break;
                case KeyEvent.VK_D: right = false; break;
            }
        }
    }

    @Override
    protected void move() {
        if (isInvincible && (System.currentTimeMillis() / 50 - invincibleStartTime > invincibleDuration)) {
            isInvincible = false;
        }

        if (left && this.getX() > 0) {
            this.setX(this.getX() - speed);
        }
        if (right && this.getX() < 800 - this.getW()) { // Assuming screen width is 800
            this.setX(this.getX() + speed);
        }
        if (up && this.getY() > 0) {
            this.setY(this.getY() - speed);
        }
        if (down && this.getY() < 600 - this.getH()) { // Assuming screen height is 600
            this.setY(this.getY() + speed);
        }
    }
    
    @Override
    protected void add(long gameTime) {
        if (gameTime - attackStartTime > attackCooldown) {
            attackStartTime = gameTime;
            // pass player's center x for bullet creation
            int bulletX = this.getX() + this.getW() / 2;
            ElementObj bullet = new Bullet().createElement("play," + bulletX + "," + this.getY());
            ElementManager.getManager().addElement(bullet, GameElement.PLAYFILE);
        }
    }
    
    public void powerUp(long gameTime) {
        isPoweredUp = true;
        this.attackCooldown = poweredUpAttackCooldown;
        this.powerUpStartTime = gameTime;
    }

    @Override
    public final void model(long gameTime) {
		if (isInvincible && (gameTime - invincibleStartTime > invincibleDuration)) {
            isInvincible = false;
        }
        if (isPoweredUp && (gameTime - powerUpStartTime > powerUpDuration)) {
            isPoweredUp = false;
            this.attackCooldown = baseAttackCooldown;
        }
		super.model(gameTime);
	}

    public void getHit(long gameTime) {
        if (!isInvincible) {
            this.health--;
            if (this.health <= 0) {
                this.setLive(false);
            } else {
                isInvincible = true;
                invincibleStartTime = gameTime;
            }
        }
    }

    public void heal() {
        this.health++;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {
        // toString isn't used for bullet creation anymore, but can be kept for debugging
        return "play," + getX() + "," + getY();
    }
} 