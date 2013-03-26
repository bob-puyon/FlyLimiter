package bob_puyon.FlyLimiter;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class EventListener implements Listener{

	FlyLimiter plg;

	public EventListener(FlyLimiter instance) {
		this.plg = instance;
	}

	@EventHandler
	public void onPlayerFallDamage(EntityDamageEvent ev) {
		if (ev.getEntityType() == EntityType.PLAYER && ev.getCause() == DamageCause.FALL) {
			Player player = (Player)ev.getEntity();
			if ( plg.fallingPlayers.contains(player.getName()) ) {
				ev.setCancelled(true);
				plg.getLogger().info("Preventing " + Integer.toString(ev.getDamage()) + " Fall Damage from" + player.getName() );
				plg.fallingPlayers.remove( player.getName() );
			}
		}
	}

	@EventHandler
	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent evt){
		Player player = evt.getPlayer();
		if( !plg.flying_state.contains( player.getName() ) ){
			return;
		}

		if( player.hasPermission( "flylimiter.buy" ) && !plg.disabled_flyarea.contains( player.getWorld().getName() ) ){
			player.setAllowFlight(true);
			player.sendMessage( FlyLimiter.msgPrefix + "飛行許可エリアに出ました" );
			player.sendMessage( FlyLimiter.msgPrefix + "飛行モードを有効化しました" );
		}else{
			player.setAllowFlight(false);
			player.sendMessage( FlyLimiter.msgPrefix + "飛行禁止エリアに出ました" );
			player.sendMessage( FlyLimiter.msgPrefix + "飛行モードは無効化されますが飛行可能時間は消費されます" );
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt){
		final Player player = evt.getPlayer();
		if( plg.flying_state.contains(player.getName())){
			//残り時間を分で計算
			int rest_min = plg.cfgRestFlyTime.getInt( "flyinguser." + player.getName() ) * FlyLimiter.REALTIME_PERIOD;
			player.sendMessage( FlyLimiter.msgPrefix + "飛行状態が有効になりました");
			player.sendMessage( FlyLimiter.msgPrefix + "残りおよそ "+ (rest_min+FlyLimiter.REALTIME_PERIOD) + " 分飛行可能です");
			player.sendMessage( FlyLimiter.msgPrefix + "あなたがログアウト時に飛行中だった場合" );
			player.sendMessage( FlyLimiter.msgPrefix + "メインワールドのスポーン地点に戻されている場合があります" );
			player.setAllowFlight(true);

		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		final Player player = event.getPlayer();

		//ログアウトの瞬間に飛行状態の場合、「world」ワールドのリスポーン地点に移動
		if( player.isFlying() ){
			Location spawn = plg.getServer().getWorld("world").getSpawnLocation();
			player.teleport( spawn, TeleportCause.PLUGIN );
		}

		//MEMO:あるXZ座標における一番高いブロックのY座標取得
		/*
		final Location playerLocation = player.getLocation();
		final int topX = playerLocation.getBlockX();
		final int topZ = playerLocation.getBlockZ();
		final int topY = playerLocation.getWorld().getHighestBlockYAt(topX, topZ);
		*/


	}





}
