package me.cakenggt.CakesRadio;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CakesRadio extends JavaPlugin {
	private Listener playerListener;
	private Listener moveListener;
	private Listener blockListener;
	private CakesRadioBroadcast broadcast;
	private int size = 10000;
	private Map<World, Boolean> worldsTable = new HashMap<World, Boolean>();



	private List<Location> radios;

	private int pipboyID = 345;

	private final String configname = "config.yml";
    
    public void onDisable() {
        // TODO: Place any custom disable code here.
        System.out.println(this + " is now disabled!");
    }
    
    public void onEnable() {
    	if(!loadConfig()) {
    		System.out.println(this + " has encountered an error while reading the configuration file," 
    				+ " continuing with defaults");
    	}

    	playerListener = new CakesRadioPlayerInteract(this);

    	blockListener = new CakesRadioBlockListener(this);


    	try {

    		checkRadios();
			loadRadios();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	getServer().getPluginManager().registerEvents(playerListener, this);
    	
    	getServer().getPluginManager().registerEvents(blockListener, this);
 
    	this.broadcast = new CakesRadioBroadcast(this);
    	try {

			checkRadios();
		} catch (IOException e) {
			//e.printStackTrace();
		}
 

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.broadcast, 20L, 100L);


        System.out.println(this + " is now enabled!");
    }

    private void setupRefresh() {
    	Timer refreshTimer = new Timer();
	     refreshTimer.scheduleAtFixedRate(new TimerTask() {
	       public void run() {
	    	   Player[] players = getServer().getOnlinePlayers();
	       		for (Player p : players) {
	       			p.setSneaking(false);
	       			p.setSneaking(true);
	       		}
	       	
	       }
	     }
	     , 500L, 5000L);
	   }
    
	private boolean loadConfig() {
	    YamlConfiguration config = new YamlConfiguration();
	    getDataFolder().mkdirs();
	    File configfile = new File(getDataFolder(),configname);
	    try {
	      config.load(configfile);
	    } catch (FileNotFoundException e) {
	      config.set("size", size);


		config.set("pipboyID", pipboyID);


	      try {
	        config.save(configfile);
	      } catch (IOException e1) {
	        System.out.println(this + " was unable to create the default config file!");
	      }
	    } catch (IOException e) {
	      e.printStackTrace();
	      return false;
	    } catch (InvalidConfigurationException e) {
	      e.printStackTrace();
	      return false;
	    }
	    this.setSize(config.getInt("size", this.getSize()));
	    for (World world : getServer().getWorlds()){
	    	this.setOn(world, config.getBoolean("worlds." + world.getName(), false));
	    }

	    this.setPipboyID(config.getInt("pipboyID", this.getPipboyID()));

	    return true;
	  }


	
	public void checkRadios() throws IOException {
		if (new File("plugins/CakesRadio/").mkdirs())
			System.out.println("radio file created");
		File myFile = new File("plugins/CakesRadio/radios.txt");
		if (!myFile.exists()){
			PrintWriter outputFile = new PrintWriter("plugins/CakesRadio/radios.txt");
			System.out.println("radio file created");
			outputFile.close();
		}
		Scanner inputFile = new Scanner(myFile);
		List<Location> radios = new ArrayList<Location>();
		while (inputFile.hasNextLine()){
			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
			radios.add(a);
			inputFile.nextLine();
		}
		inputFile.close();
		PrintWriter outputFile = new PrintWriter(myFile);
		if (radios == null){
			outputFile.close();
			return;
		}
		for (Location radio : radios){
			Block block = radio.getBlock();
			if (block.isBlockIndirectlyPowered()){
				outputFile.println(block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
			}
		}
		outputFile.close();
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}


	public void setOn(World world, boolean isOn){
		this.worldsTable.put(world, isOn);
	}

	public Map<World, Boolean> getOn(){
		return worldsTable;
	}

	
	public void setPipboyID(int id) {
		this.pipboyID = id;
	}
	
	public int getPipboyID() {
		return pipboyID;
	}
	


	public void loadRadios() throws IOException {
		File myFile = new File("plugins/CakesRadio/radios.txt");
		Scanner inputFile = new Scanner(myFile);
		List<Location> radios = new ArrayList<Location>();
		while (inputFile.hasNextLine()){
			Location a = new Location(Bukkit.getServer().getWorld(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()), Double.valueOf(inputFile.next()));
			radios.add(a);
			inputFile.nextLine();
		}
		inputFile.close();
		this.radios = radios;
	}
	public List<Location> getRadios() {
		return radios;
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
	   	if(cmd.getName().equalsIgnoreCase("radio")){ // If the player typed /radio then do the following...
	   		Player player = null;
    		if (sender instanceof Player) {
    			player = (Player) sender;
	    	}
	   		if (player == null) {
	   			sender.sendMessage("this command can only be run by a player");
	   			return false;
	   		}
	   		if (player.getItemInHand().getTypeId() != getPipboyID()){
	   			player.sendMessage("You must be holding a compass to work the radio");
	   			return true;
	   		}
    		if (args.length != 1){
	    		sender.sendMessage(cmd.getUsage());
	   			return true;
	   		}
    		if (args[0].equalsIgnoreCase("scan")){
    			try {
					setFrequency(sender, args[0].toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
    		}
    		if (args[0].equalsIgnoreCase("version")){
    			String name = this.toString();
    			player.sendMessage(name);
    			return true;
    		}
    		try {
				Double.parseDouble(args[0].toString());
		    }
			catch (NumberFormatException e) {
				sender.sendMessage(cmd.getUsage());
				return true;
		    }
	   		String inputString = args[0].toString();
	   		try {
	   			setFrequency(sender, inputString);
    		} catch (IOException e) {
	    		e.printStackTrace();
    		}
    		return true;
	    }

    	return false;
	}
	    
	   public void setFrequency (CommandSender sender, String inputString) throws IOException{
		   if (new File("plugins/CakesRadio/").mkdirs())
				System.out.println("Frequencies file created");
		   File myFile = new File("plugins/CakesRadio/frequencies.txt");
		   if (myFile.exists()){
			   Scanner inputFileCheck = new Scanner(myFile);
	           int j = 0;
	           while (inputFileCheck.hasNext()) {
            	inputFileCheck.nextLine();
            	j++;
	           }
	           int size = (j + 1) / 2;
	           String[] nameArray = new String[size];
	           String[] circleArray = new String[size];
	           inputFileCheck.close();
	           Scanner inputFile = new Scanner(myFile);
	           for (int i = 0; i < size; i++) {
	               nameArray[i] = inputFile.nextLine();
	               circleArray[i] = inputFile.nextLine();
	           }
	           boolean isInFile = false;
	           for (int i = 0; i < size; i++) {
	               if (nameArray[i].equalsIgnoreCase(sender.getName())){
	               		circleArray[i] = inputString;
	               		sender.sendMessage("Frequency " + inputString + " set successfully!");
	               		isInFile = true;
	               }
	           }
	           inputFile.close();
	           PrintWriter outputFile = new PrintWriter("plugins/CakesRadio/frequencies.txt");
	           for (int i = 0; i < size; i++) {
	               outputFile.println(nameArray[i]);
	               outputFile.println(circleArray[i]);
	           }
	           if (!isInFile){
	        	   outputFile.println(sender.getName());
	               outputFile.println(inputString);
	           }
	           outputFile.close();
		}
		else{
			PrintWriter outputFile = new PrintWriter("plugins/CakesRadio/frequencies.txt");
	        outputFile.println(sender.getName());
	        outputFile.println(inputString);
	        sender.sendMessage("frequency " + inputString + " set successfully!");
	        outputFile.close();
		}
    }
	   

}
