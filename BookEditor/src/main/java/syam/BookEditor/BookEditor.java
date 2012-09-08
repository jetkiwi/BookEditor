/**
 * BookEditor - Package: syam.BookEditor
 * Created: 2012/09/08 12:08:02
 */
package syam.BookEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import syam.BookEditor.Book.BookManager;
import syam.BookEditor.Command.BaseCommand;
import syam.BookEditor.Command.CopyCommand;
import syam.BookEditor.Command.HelpCommand;
import syam.BookEditor.Command.ReloadCommand;
/**
 * BookEditor (BookEditor.java)
 * @author syam
 */
public class BookEditor extends JavaPlugin{
	// ** Logger **
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[BookEditor] ";
	public final static String msgPrefix = "&6[BookEditor] &f";

	// ** Listener **

	// ** Commands **
	public static List<BaseCommand> commands = new ArrayList<BaseCommand>();

	// ** Private Classes **
	private ConfigurationManager config;
	private BookManager bm;

	// ** Instance **
	private static BookEditor instance;

	// ** Hookup Plugins **
	public static Vault vault = null;
	public static Economy economy = null;

	/**
	 * プラグイン起動処理
	 */
	@Override
	public void onEnable(){
		instance  = this;
		PluginManager pm = getServer().getPluginManager();
		config = new ConfigurationManager(this);

		// loadconfig
		try{
			config.loadConfig(true);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
		}

		// プラグインフック
		setupVault(); // Vault

		// プラグインを無効にした場合進まないようにする
		if (!pm.isPluginEnabled(this)){
			return;
		}

		// Regist Listeners
		//TODO: pm.registerEvents(playerListener, this);

		// コマンド登録
		registerCommands();

		// マネージャ
		//bm = new BookManager(this);

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is enabled!");
	}

	/**
	 * プラグイン停止処理
	 */
	@Override
	public void onDisable(){
		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	/**
	 * コマンドを登録
	 */
	private void registerCommands(){
		// Intro Commands
		commands.add(new HelpCommand());

		// General Commands
		commands.add(new CopyCommand());

		// Admin Commands
		commands.add(new ReloadCommand());

	}

	/**
	 * Vaultプラグインにフック
	 */
	private void setupVault(){
		Plugin plugin = this.getServer().getPluginManager().getPlugin("Vault");
		if(plugin != null & plugin instanceof Vault) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			// 経済概念のプラグインがロードされているかチェック
			if(economyProvider==null){
	        	log.warning(logPrefix+"Economy plugin not Fount. Disabling plugin.");
		        getPluginLoader().disablePlugin(this);
		        return;
			}

			try{
				vault = (Vault) plugin;
				economy = economyProvider.getProvider();
			} // 例外チェック
			catch(Exception e){
				log.warning(logPrefix+"Could NOT be hook to Vault. Disabling plugin.");
		        getPluginLoader().disablePlugin(this);
		        return;
			}
			log.info(logPrefix+"Hooked to Vault!");
		} else {
			// Vaultが見つからなかった
	        log.warning(logPrefix+"Vault was NOT found! Disabling plugin.");
	        getPluginLoader().disablePlugin(this);
	        return;
	    }
	}

	/**
	 * コマンドが呼ばれた
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
		if (cmd.getName().equalsIgnoreCase("book")){
			if(args.length == 0){
				// 引数ゼロはヘルプ表示
				args = new String[]{"help"};
			}

			outer:
			for (BaseCommand command : commands.toArray(new BaseCommand[0])){
				String[] cmds = command.name.split(" ");
				for (int i = 0; i < cmds.length; i++){
					if (i >= args.length || !cmds[i].equalsIgnoreCase(args[i])){
						continue outer;
					}
					// 実行
					return command.run(this, sender, args, commandLabel);
				}
			}
			// 有効コマンドなし ヘルプ表示
			new HelpCommand().run(this, sender, args, commandLabel);
			return true;
		}
		return false;
	}

	/* getter */

	/**
	 * ゲームマネージャを返す
	 * @return GameManager
	 */
	public BookManager getManager(){
		return bm;
	}

	/**
	 * 設定マネージャを返す
	 * @return ConfigurationManager
	 */
	public ConfigurationManager getConfigs() {
		return config;
	}

	/**
	 * インスタンスを返す
	 * @return BookEditorインスタンス
	 */
	public static BookEditor getInstance(){
		return instance;
	}
}