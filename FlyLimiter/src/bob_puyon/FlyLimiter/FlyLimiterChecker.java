package bob_puyon.FlyLimiter;

import java.util.Iterator;

import org.bukkit.OfflinePlayer;

public class FlyLimiterChecker implements Runnable {

	FlyLimiter plg;

	public FlyLimiterChecker(FlyLimiter instance) {
		this.plg = instance;
		FlyLimiter.logger.info( FlyLimiter.logPrefix + "PointCheck Thread Start !!" );
	}

	@Override
	public void run() {
		//飛行中のプレイヤーが存在しない場合、タスクを終了
		if( this.plg.flying_state.isEmpty() ){
			plg.getServer().getScheduler().cancelTask(FlyLimiter.chk_taskid);
			FlyLimiter.logger.info( FlyLimiter.logPrefix + "FlyingPlayer does not exist - Checktask is stopped");
		}

		//飛行が有効状態のオンラインプレイヤーを走査
		for(Iterator<String> it = this.plg.flying_state.iterator() ; it.hasNext();){

			String flyinguser = it.next();

			//飛行状態に該当するプレイヤーの残りポイントを取得
			int restpoint = plg.cfgRestFlyTime.getInt( "flyinguser." + flyinguser );;

			//該当プレイヤーのポイントを減算する
			plg.cfgRestFlyTime.set( "flyinguser." + flyinguser, --restpoint);

			OfflinePlayer off_player = plg.getServer().getOfflinePlayer( flyinguser );

			if( restpoint < 0 ){
				//該当プレイヤーのポイントがマイナスになった場合

				//コンフィグ変数より該当プレイヤーの削除
				plg.cfgRestFlyTime.set( "flyinguser."+flyinguser, restpoint );
				//飛行状態リストからの削除
				plg.flying_state.remove( flyinguser );
				//落下ダメージキャンセルフラグ追加
				plg.fallingPlayers.add( flyinguser );

				if( off_player.isOnline() ){
					//TODO:終了時に空中にいた場合の処置
					off_player.getPlayer().setAllowFlight(false);
					off_player.getPlayer().sendMessage( FlyLimiter.msgPrefix + "飛行可能時間が無くなりました！");
					off_player.getPlayer().sendMessage( FlyLimiter.msgPrefix + "ダメージ無しでで地上への着地を開始します！！");
				}
			}else{
				//ポイントがまだ残っている場合
				String rest_min = String.valueOf( restpoint * FlyLimiter.REALTIME_PERIOD + FlyLimiter.REALTIME_PERIOD);

				//プレイヤーに通知可能な場合は通知を行う
				if( off_player.isOnline() ){
					if(restpoint < 2 ){
						rest_min = "\u00A7c\u00A7l".concat(rest_min);
						rest_min = rest_min.concat("\u00A7r\u00A7f");
					}
					off_player.getPlayer().sendMessage( FlyLimiter.msgPrefix + "飛行可能時間が残り " + rest_min + "分 になりました！");
					off_player.getPlayer().sendMessage( FlyLimiter.msgPrefix + "飛行が終わるときはマグマなどの上にいないでください！");
				}
			}
		}
	}
}
