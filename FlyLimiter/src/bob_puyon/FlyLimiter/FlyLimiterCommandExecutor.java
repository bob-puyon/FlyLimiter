package bob_puyon.FlyLimiter;

import java.util.Iterator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class FlyLimiterCommandExecutor implements CommandExecutor {
	private FlyLimiter plg;

	FlyLimiterCommandExecutor(FlyLimiter instance) {
		this.plg = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO:実装すべきコマンドとは？
		// args:
		// 1.status (alias:st)
		// 2.list
		// 3.buy
		//***以下,admin系コマンド***
		// 4.reload
		// 5.give <player>(後で実装)

		//TODO:コマンド送信者がコンソールだった場合に拒否できる？
		if ( !(sender instanceof Player) ) {
			return true;
		}

		if( args.length < 1 ){
			((Player)sender).sendMessage(  FlyLimiter.msgPrefix + "コマンドの指定が不足しています");
			((Player)sender).sendMessage(  FlyLimiter.msgPrefix + "[ /flylimiter help ] で確認できます");
			return true;
		}

		if( 1 < args.length ){
			((Player)sender).sendMessage(  FlyLimiter.msgPrefix + "コマンドの指定が多すぎます");
			((Player)sender).sendMessage(  FlyLimiter.msgPrefix + "[ /flylimiter help ] で確認できます");
			return true;
		}

		if( args[0].equalsIgnoreCase("list") ){
			//飛行状態の一覧表示コマンド

			//パーミッションのチェック
			if( !((Player) sender).hasPermission("flylimiiter.list") ){ return true; }
			//現在飛行状態のユーザーの表示
			this.showFlyingList((Player)sender);

		}else if( args[0].equalsIgnoreCase("status") ){
			//残り飛行時間の確認コマンド

			//パーミッションのチェック
			if( !((Player) sender).hasPermission("flylimiiter.status") ){ return true; }
			// 現在飛行しているプレイヤーのリストアップ
			this.showFlyingList( ((Player) sender) );

		}else if( args[0].equalsIgnoreCase("buy") ){
			//ポイントの取得と飛行有効化コマンド

			//パーミッションのチェック
			if( !sender.hasPermission("flylimiter.buy") ){
				//FlyLimiter.logger.info( ((Player) sender).getName() + " is denied flylimiter add!!");
				return true;
			}

			//TEST:リンゴが手中にあるときだけ有効化

			/*
			if( ((Player) sender).getItemInHand().getTypeId() == Material.APPLE.getId()){
				final int add_flypoint = 12;

				plg.cfgRestFlyTime.set( "flyinguser." + ((Player)sender).getName(), add_flypoint);
				plg.flying_state.add( sender.getName() );
				((Player)sender).setAllowFlight(true);
				((Player)sender).sendMessage( FlyLimiter.msgPrefix + "飛べるよ！飛んじゃいなよ！！" );
				((Player)sender).sendMessage( FlyLimiter.msgPrefix + "これからおよそ " + add_flypoint * FlyLimiter.REALTIME_PERIOD + " 分とちょっと飛行が可能になりました！" );
			}else{
				((Player)sender).sendMessage( FlyLimiter.msgPrefix + "You should have Apple in Hand!!" );
			}
			 */

			//Main:パーミッションの確認と経済概念差し引きの追加
			if( FlyLimiter.econ.hasAccount( ((Player)sender).getName()) ){
				if( FlyLimiter.econ.has( ((Player)sender).getName(), FlyLimiter.FLY_MONEY ) ){

					//TODO:前回のタスクが回ってない事も確認する必要がある
					//飛行中のユーザーがいない場合、飛行ポイントチェックスレッドの実行
					if( this.plg.flying_state.isEmpty() && !plg.getServer().getScheduler().isCurrentlyRunning(FlyLimiter.chk_taskid) ){
						FlyLimiter.chk_taskid  = this.plg.getServer().getScheduler().
								scheduleSyncRepeatingTask( plg , new FlyLimiterChecker(plg), FlyLimiter.TASK_PERIOD , FlyLimiter.TASK_PERIOD);
					}

					final int add_flypoint = 12;
					FlyLimiter.econ.withdrawPlayer( ((Player)sender).getName(), FlyLimiter.FLY_MONEY );
					plg.cfgRestFlyTime.set( "flyinguser." + ((Player)sender).getName(), add_flypoint);
					plg.flying_state.add( sender.getName() );
					((Player)sender).setAllowFlight(true);
					((Player)sender).sendMessage( FlyLimiter.msgPrefix + "飛べるよ！飛んじゃいなよ！！" );
					((Player)sender).sendMessage( FlyLimiter.msgPrefix + "これからおよそ " + add_flypoint * FlyLimiter.REALTIME_PERIOD + " 分とちょっと飛行が可能になりました！" );




				}else{
					((Player)sender).sendMessage( FlyLimiter.msgPrefix + "飛ぶためのお金が足りません…" );
					((Player)sender).sendMessage( FlyLimiter.msgPrefix + "約１時間飛ぶためには " + FlyLimiter.FLY_MONEY + "Fil 必要です");
				}
			}





		}else{
			((Player)sender).sendMessage(  FlyLimiter.msgPrefix + "指定されたコマンドが存在しませんでした。");
			((Player)sender).sendMessage(  FlyLimiter.msgPrefix + "[ /flylimiter help ] で確認できます");
		}

		return true;
	}

	public void showFlyingList(Player p){

		if(this.plg.flying_state.isEmpty()){
			p.sendMessage(  FlyLimiter.msgPrefix + "現在飛行中のプレイヤーはいません");
			return;
		}

		p.sendMessage( "***** 現在飛行中のプレイヤー一覧 *****" );
		for(Iterator<String> it = this.plg.flying_state.iterator(); it.hasNext();){
			String flyinguser = it.next();
			p.sendMessage( flyinguser);
		}
		p.sendMessage( "**************************************" );
	}



}


