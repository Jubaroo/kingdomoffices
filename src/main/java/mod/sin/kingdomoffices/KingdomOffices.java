package mod.sin.kingdomoffices;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mod.sin.kingdomoffices.actions.TokenAction;
import mod.sin.kingdomoffices.tokens.TokenType;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.ItemTemplatesCreatedListener;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class KingdomOffices
implements WurmServerMod, Configurable, PreInitable, ItemTemplatesCreatedListener, ServerStartedListener {
	protected static KingdomOffices instance = new KingdomOffices();
    boolean bDebug = false;
    private Logger logger;

    public KingdomOffices() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    public void configure(Properties properties) {
        this.bDebug = Boolean.parseBoolean(properties.getProperty("debug", Boolean.toString(this.bDebug)));
        try {
            String logsPath = Paths.get("mods", new String[0]) + "/logs/";
            File newDirectory = new File(logsPath);
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }
            FileHandler fh = new FileHandler(String.valueOf(String.valueOf(logsPath)) + this.getClass().getSimpleName() + ".log", 10240000, 200, true);
            if (this.bDebug) {
                fh.setLevel(Level.INFO);
            } else {
                fh.setLevel(Level.WARNING);
            }
            fh.setFormatter(new SimpleFormatter());
            this.logger.addHandler(fh);
        }
        catch (IOException ie) {
            System.err.println(String.valueOf(this.getClass().getName()) + ": Unable to add file handler to logger");
        }
        //this.logger.log(Level.INFO, "Property: " + this.somevalue);
        this.Debug("Debugging messages are enabled.");
    }

    private void Debug(String x) {
        if (this.bDebug) {
            System.out.println(String.valueOf(this.getClass().getSimpleName()) + ": " + x);
            System.out.flush();
            this.logger.log(Level.INFO, x);
        }
    }

    public void preInit() {
        try {
        	ModActions.init();
    		logger.info("Running Kingdom Tokens init()");
    		ClassPool classPool = HookManager.getInstance().getClassPool();
	        classPool.appendClassPath("./mods/kingdomoffices/kingdomoffices.jar");
            CtClass ctPlayers = classPool.get("com.wurmonline.server.Players");
            // Hook the daily poll to faith resets.
            ctPlayers.getDeclaredMethod("resetFaithGain").insertBefore(""
            		+ "  KingdomOffices.staticPoll();");
            // Destroy tokens when creation fails.
            CtClass ctSimpleCreationEntry = classPool.get("com.wurmonline.server.items.SimpleCreationEntry");
            ctSimpleCreationEntry.getDeclaredMethod("run").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("broadCastAction")) {
                        m.replace("$_ = $proceed($$);"
                        		+ "if($1.contains(\"fails with\")){"
                        		+ "  if(target.getTemplateId() == 22765){"
                        		+ "    com.wurmonline.server.Items.destroyItem(target.getWurmId());"
                        		+ "  }"
                        		+ "  if(source.getTemplateId() == 22765){"
                        		+ "    com.wurmonline.server.Items.destroyItem(source.getWurmId());"
                        		+ "  }"
                        		+ "}");
                        return;
                    }
                }
            });
        }
        catch (CannotCompileException | NotFoundException e) {
            throw new HookException((Throwable)e);
        }
    }

	@Override
	public void onItemTemplatesCreated() {
		try{
			logger.info("Running Kingdom Tokens onItemTemplatesCreated()");
			Connection dbcon = ModSupportDb.getModSupportDb();
        	if (!ModSupportDb.hasTable(dbcon, "TOKENS")) {
        		logger.info("No table for TOKENS. Creating new table now...");
				try (Statement statement = dbcon.createStatement()) {
					statement.execute("CREATE TABLE TOKENS (PLAYER BIGINT, OFFICE INT, VALUE INT)");
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		ItemCreator.createItems();
	}

	public void addAppointment(long playerid, Appointment a, King k, int amount) throws NoSuchPlayerException, NotFoundException {
		Player p = Players.getInstance().getPlayer(playerid);
        Communicator pc = p.getCommunicator();
        Appointments apps = King.getCurrentAppointments(p.getKingdomId());
        String overtakeString = "";
        switch (a.getType()) {
            case 2: {
                if (apps.officials[a.getId() - 1500] == p.getWurmId()) {
                    //pc.sendNormalServerMessage(p.getName() + " is already appointed to the office of " + a.getNameForGender(p.getSex()) + ".");
                    return;
                }
                if (apps.officials[a.getId() - 1500] > 0) {
                    Player op = Players.getInstance().getPlayerOrNull(apps.officials[a.getId() - 1500]);
                    PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(apps.officials[a.getId() - 1500]);
                    if (pinf != null) {
	                    overtakeString = "overtakes "+pinf.getName()+" and ";
                    }
                    if (op == null && pinf != null) {
                        pc.sendNormalServerMessage("Unable to notify " + pinf.getName() + " of their removal from office.");
                    } else if(op != null) {
                        op.getCommunicator().sendNormalServerMessage("You are hereby notified that you have been removed from the office of " + a.getNameForGender(op.getSex()) + ".", (byte) 2);
                    }
                }
                apps.setOfficial(a.getId(), p.getWurmId());
                p.achievement(323);
                k.addAppointment(a);
                break;
            }
            default: {
                pc.sendNormalServerMessage("That appointment is invalid.");
                return;
            }
        }
        k.addAppointment(a);
        //pc.sendNormalServerMessage("You award the " + a.getNameForGender(p.getSex()) + " of " + Kingdoms.getNameFor(p.getKingdomId()) + " to " + p.getName() + ".", (byte) 2);
        pc.sendNormalServerMessage("You have graciously been awarded the " + a.getNameForGender(p.getSex()) + " of " + Kingdoms.getNameFor(p.getKingdomId()) + "!", (byte) 2);
        HistoryManager.addHistory(p.getName(), overtakeString+"receives the " + a.getNameForGender(p.getSex()) + " of " + Kingdoms.getNameFor(p.getKingdomId()) + " with a bid of "+amount+" tokens!");
        logger.info("Appointed "+p.getName()+" as the new "+a.getNameForGender(p.getSex())+"!");
        final String otstring = overtakeString;
        Runnable r = new Runnable(){
        	public void run(){
		        com.wurmonline.server.Message mess;// = new Message(null, (byte)16, "Server", p.getName()+" "+overtakeString+"receives the " + a.getNameForGender(p.getSex()) + " of " + Kingdoms.getNameFor(p.getKingdomId()) + "!", 0, 0, 255);
		        for(Player rec : Players.getInstance().getPlayers()){
		        	mess = new com.wurmonline.server.Message(rec, (byte)16, "Server", p.getName()+" "+otstring+"receives the " + a.getNameForGender(p.getSex()) + " of " + Kingdoms.getNameFor(p.getKingdomId()) + " with a bid of "+amount+" tokens!", 0, 255, 255);
		        	rec.getCommunicator().sendMessage(mess);
		        }
        	}
        };
        r.run();
    }
	
	public void pollOffice(long wurmid, TokenType tokenType, int amount) throws NoSuchPlayerException, InvocationTargetException{
		Player p = Players.getInstance().getPlayer(wurmid);
		p.getCommunicator().sendSafeServerMessage("Added token for office "+tokenType.getName()+". Current bid: "+amount);
		int officeId = tokenType.getOfficeId();
		if(amount < 10){
			//logger.info(officeId+", "+amount+": Not enough tokens to appoint.");
			return;
		}
		try{
			//logger.info("Checking to see if "+officeId+" has a new appointment from "+wurmid+" for "+amount+"...");
			Connection dbcon = ModSupportDb.getModSupportDb();
			PreparedStatement ps = dbcon.prepareStatement("SELECT * FROM TOKENS WHERE OFFICE=? AND VALUE>? AND PLAYER<>?");
			ps.setInt(1, officeId);
			int amountNeeded = (int) (amount * 0.98);
			ps.setInt(2, amountNeeded);
			ps.setLong(3, wurmid);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				//logger.info("Player "+wurmid+" is now the new official for "+officeId+" with "+amount+" tokens!");
				King k = King.getKing(p.getKingdomId());
				if(k != null){
					Appointments a = Appointments.getAppointments(k.era);
					if(a != null){
						//Player king = Players.getInstance().getPlayer(k.kingid);
						Appointment app = a.getAppointment(officeId);
						//p.addAppointment(app, king);
						//ReflectionUtil.callPrivateMethod(p, ReflectionUtil.getMethod(p.getClass(), "addAppointment"), app, king);
						addAppointment(wurmid, app, k, amount);
					}
				}
			}else{
				//logger.info("Someone has more than the player for office "+rs.getInt("OFFICE")+": "+rs.getLong("PLAYER")+" with "+rs.getInt("VALUE")+". Failed appointment.");
			}
			DbUtilities.closeDatabaseObjects(ps, rs);
		}catch(SQLException | IllegalArgumentException | NotFoundException e){
			e.printStackTrace();
		}
	}

	public void poll(){
		try{
			Connection dbcon = ModSupportDb.getModSupportDb();
			PreparedStatement ps = dbcon.prepareStatement("SELECT * FROM TOKENS");
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				long playerid = rs.getLong("PLAYER");
				int office = rs.getInt("OFFICE");
				int value = rs.getInt("VALUE");
				value = (int) (value * 0.9);
				if(value != 0){
					ps = dbcon.prepareStatement("UPDATE TOKENS SET VALUE=? WHERE PLAYER=? AND OFFICE=?");
					ps.setInt(1, value);
					ps.setLong(2, playerid);
					ps.setInt(3, office);
					ps.executeUpdate();
				}else{
					logger.info("Deleting record for player "+playerid+" in office "+office+".");
					ps = dbcon.prepareStatement("DELETE FROM TOKENS WHERE PLAYER=? AND OFFICE=?");
					ps.setLong(1, playerid);
					ps.setInt(2, office);
					ps.executeUpdate();
				}
			}
			DbUtilities.closeDatabaseObjects(ps, rs);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	public static KingdomOffices getInstance(){
		return instance;
	}
	
	public static void staticPoll(){
		getInstance().logger.info("Polling Kingdom Tokens...");
		getInstance().poll();
	}
	
	@SuppressWarnings("resource")
	public void addPlayerTokens(long wurmid, TokenType tokenType, int value) {
		try {
			//logger.info("Attempting database connection...");
			Connection dbcon = ModSupportDb.getModSupportDb();
			PreparedStatement ps;
			ps = dbcon.prepareStatement("SELECT * FROM TOKENS WHERE PLAYER=? AND OFFICE=?");
			ps.setLong(1, wurmid);
			ps.setInt(2, tokenType.getOfficeId());
			ResultSet rs = ps.executeQuery();
			int amount = value;
			if(rs.next()){
				amount = rs.getInt("VALUE")+value;
				//logger.info("Found database entry for WurmId "+wurmid+" at Office "+tokenType.getOfficeId());
				ps = dbcon.prepareStatement("UPDATE TOKENS SET VALUE=? WHERE PLAYER=? AND OFFICE=?");
				ps.setInt(1, amount);
				ps.setLong(2, wurmid);
				ps.setInt(3, tokenType.getOfficeId());
				ps.executeUpdate();
			}else{
				//logger.info("Did not find database entry for WurmId "+wurmid+" at Office "+tokenType.getOfficeId());
				ps = dbcon.prepareStatement("INSERT INTO TOKENS(PLAYER, OFFICE, VALUE) VALUES(?, ?, ?)");
				ps.setLong(1, wurmid);
				ps.setInt(2, tokenType.getOfficeId());
				ps.setInt(3, value);
				ps.executeUpdate();
			}
			pollOffice(wurmid, tokenType, amount);
			DbUtilities.closeDatabaseObjects(ps, rs);
		} catch (SQLException | NoSuchPlayerException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onServerStarted() {
		ModActions.registerAction(new TokenAction());
		ItemCreator.initCreationEntries();
	}

}