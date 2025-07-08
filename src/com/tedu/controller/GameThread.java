package com.tedu.controller;

import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.element.Item;
import com.tedu.element.Play;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏关卡，游戏运行时自动化
 * 		游戏判定；游戏地图切换 资源释放和重新读取。。。
 * @author renjj
 * @继承 使用继承的方式实现多线程(一般建议使用接口实现)
 */
enum GameState {
	WAVE, BOSS, COOLDOWN
}

public class GameThread extends Thread{
	private ElementManager em;
	private GameState gameState = GameState.WAVE;
	private long waveTimer = 0;
	private final long waveDuration = 1200; // 20 seconds
	private final long cooldownDuration = 300; // 5 seconds
	
	public GameThread() {
		em=ElementManager.getManager();
	}
	@Override
	public void run() {//游戏的run方法  主线程
		while(true) { //扩展,可以讲true变为一个变量用于控制结束
			gameLoad();
			gameRun();

			em.setGameOver(true);
			while(em.isGameOver()) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			gameOver();
		}
	}
	/**
	 * 游戏的加载
	 */
	private void gameLoad() {
		GameLoad.loadImg(); //加载图片
		GameLoad.loadObj(); // 加载类
		
		// 加载背景
		ElementObj background = GameLoad.getObj("background").createElement("");
		em.addElement(background, GameElement.BACKGROUND);
		
		// 加载主角
		ElementObj play = GameLoad.getObj("play").createElement("350,500");
		em.addElement(play, GameElement.PLAY);
	}
	
	/**
	 * @说明  游戏进行时
	 * @任务说明  游戏过程中需要做的事情：1.自动化玩家的移动，碰撞，死亡
	 *                                 2.新元素的增加(NPC死亡后出现道具)
	 *                                 3.暂停等等。。。。。
	 * 先实现主角的移动
	 * */
	
	private void gameRun() {
		long gameTime=0L;
		
		while(true) {
			Map<GameElement, List<ElementObj>> all = em.getGameElements();
			List<ElementObj> plays = all.get(GameElement.PLAY);
			if (plays.isEmpty()) {
				return;
			}
			
			switch (gameState) {
				case WAVE:
					runWaveState(all, gameTime);
					break;
				case BOSS:
					runBossState(all, gameTime);
					break;
				case COOLDOWN:
					runCooldownState(gameTime);
					break;
			}

			moveAndUpdate(all,gameTime);
			
			// Collision detection
			List<ElementObj> enemies = all.get(GameElement.ENEMY);
			List<ElementObj> bossList = all.get(GameElement.BOSS);
			List<ElementObj> playFiles = all.get(GameElement.PLAYFILE);
			List<ElementObj> enemyFiles = all.get(GameElement.ENEMYFILE);
			List<ElementObj> items = all.get(GameElement.ITEM);

			// Player bullets vs enemies & boss
			checkPlayerBulletCollisions(playFiles, enemies);
			checkPlayerBulletCollisions(playFiles, bossList);

			// Player vs enemies, boss, items, and enemy bullets
			checkPlayerCollisions(plays, enemies, gameTime);
			checkPlayerCollisions(plays, bossList, gameTime);
			checkPlayerCollisions(plays, items, gameTime);
			checkPlayerCollisions(plays, enemyFiles, gameTime);
			
			gameTime++;
			try {
				sleep(10);//默认理解为 1秒刷新100次 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void runWaveState(Map<GameElement, List<ElementObj>> all, long gameTime) {
		waveTimer++;
		if (gameTime > 0 && gameTime % 80 == 0) {
			int x = (int)(Math.random() * 750);
			ElementObj enemy = GameLoad.getObj("enemy").createElement(x + ",-50,enemy");
			em.addElement(enemy, GameElement.ENEMY);
		}
		if (waveTimer > waveDuration) {
			gameState = GameState.BOSS;
			waveTimer = 0;
		}
	}

	private void runBossState(Map<GameElement, List<ElementObj>> all, long gameTime) {
		List<ElementObj> bossList = all.get(GameElement.BOSS);
		if (bossList.isEmpty()) {
			ElementObj boss = GameLoad.getObj("boss").createElement("300,-150");
			em.addElement(boss, GameElement.BOSS);
		} else {
			if (!bossList.get(0).isLive()) {
				gameState = GameState.COOLDOWN;
				waveTimer = 0; // reset timer for cooldown
			}
		}
	}

	private void runCooldownState(long gameTime) {
		waveTimer++;
		if (waveTimer > cooldownDuration) {
			gameState = GameState.WAVE;
			waveTimer = 0;
		}
	}
	
	private void checkPlayerBulletCollisions(List<ElementObj> bullets, List<ElementObj> targets) {
		for (int i = 0; i < bullets.size(); i++) {
			ElementObj bullet = bullets.get(i);
			for (int j = 0; j < targets.size(); j++) {
				ElementObj target = targets.get(j);
				if (bullet.pk(target)) {
					bullet.setLive(false);
					
					if (target instanceof com.tedu.element.Boss) {
						((com.tedu.element.Boss)target).getHit();
					} else {
						target.setLive(false);
					}

					if (target instanceof com.tedu.element.Enemy) {
						double rand = Math.random();
						if (rand < 0.05) { // 5% chance for health
							ElementObj item = GameLoad.getObj("item").createElement(target.getX() + "," + target.getY() + ",item_health");
							em.addElement(item, GameElement.ITEM);
						} else if (rand < 0.15) { // 10% chance for powerup (5% + 10%)
							ElementObj item = GameLoad.getObj("item").createElement(target.getX() + "," + target.getY() + ",item_powerup");
							em.addElement(item, GameElement.ITEM);
						}
					}
					break;
				}
			}
		}
	}

	private void checkPlayerCollisions(List<ElementObj> players, List<ElementObj> others, long gameTime) {
		if (players.isEmpty() || others.isEmpty()) return;
		Play player = (Play) players.get(0);

		for (int i = 0; i < others.size(); i++) {
			ElementObj other = others.get(i);
			if (player.pk(other)) {
				if (other instanceof Item) {
					((Item) other).applyEffect(player, gameTime);
				} else {
					player.getHit(gameTime);
				}
				other.setLive(false);
			}
		}
	}
	
	public void ElementPK(List<ElementObj> listA, List<ElementObj> listB) {
		// This method will be deprecated
	}
	
//	游戏元素自动化方法
	public void moveAndUpdate(Map<GameElement, List<ElementObj>> all,long gameTime) {
//		GameElement.values();//隐藏方法  返回值是一个数组,数组的顺序就是定义枚举的顺序
		for(GameElement ge:GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			if(list == null) continue;
//			编写这样直接操作集合数据的代码建议不要使用迭代器。
//			for(int i=0;i<list.size();i++) {
			for(int i=list.size()-1;i>=0;i--){	
				ElementObj obj=list.get(i);//读取为基类
				if(!obj.isLive()) {//如果死亡
//					list.remove(i--);  //可以使用这样的方式
//					启动一个死亡方法(方法中可以做事情例如:死亡动画 ,掉装备)
					obj.die();//需要大家自己补充
					list.remove(i);
					continue;
				}
				obj.model(gameTime);//调用的模板方法 不是move
			}
		}	
	}
	
	/**游戏切换关卡*/
	private void gameOver() {
		em.setGameOver(false); // Reset for the next game
		// clear all elements, or handle end of level logic
		Map<GameElement, List<ElementObj>> all = em.getGameElements();
		for(GameElement ge:GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			if(list != null) {
				list.clear();
			}
		}
	}
}





