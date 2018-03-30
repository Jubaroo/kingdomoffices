package mod.sin.kingdomoffices;

import java.io.IOException;

import mod.sin.kingdomoffices.tokens.*;

public class ItemCreator {
	// Creature Token
	public static CreatureToken CREATURE_TOKEN = new CreatureToken();
	// Kingdom Tokens
	public static AvengerToken AVENGER_TOKEN = new AvengerToken();
	public static CookToken COOK_TOKEN = new CookToken();
	public static EconomicToken ECONOMIC_TOKEN = new EconomicToken();
	public static HeraldToken HERALD_TOKEN = new HeraldToken();
	public static InformationToken INFORMATION_TOKEN = new InformationToken();
	public static JesterToken JESTER_TOKEN = new JesterToken();
	public static LoverToken LOVER_TOKEN = new LoverToken();
	public static MageToken MAGE_TOKEN = new MageToken();
	public static PriestToken PRIEST_TOKEN = new PriestToken();
	public static SmithingToken SMITHING_TOKEN = new SmithingToken();
	public static WarmongerToken WARMONGER_TOKEN = new WarmongerToken();
	
	public static void createItems(){
		try{
			// Creature Tokens
			CREATURE_TOKEN.createTemplate();
			// Kingdom Tokens
			AVENGER_TOKEN.createTemplate();
			COOK_TOKEN.createTemplate();
			ECONOMIC_TOKEN.createTemplate();
			HERALD_TOKEN.createTemplate();
			INFORMATION_TOKEN.createTemplate();
			JESTER_TOKEN.createTemplate();
			LOVER_TOKEN.createTemplate();
			MAGE_TOKEN.createTemplate();
			PRIEST_TOKEN.createTemplate();
			SMITHING_TOKEN.createTemplate();
			WARMONGER_TOKEN.createTemplate();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void initCreationEntries(){
		// Kingdom Tokens
		AVENGER_TOKEN.initCreationEntry();
		COOK_TOKEN.initCreationEntry();
		ECONOMIC_TOKEN.initCreationEntry();
		HERALD_TOKEN.initCreationEntry();
		INFORMATION_TOKEN.initCreationEntry();
		JESTER_TOKEN.initCreationEntry();
		LOVER_TOKEN.initCreationEntry();
		MAGE_TOKEN.initCreationEntry();
		PRIEST_TOKEN.initCreationEntry();
		SMITHING_TOKEN.initCreationEntry();
		WARMONGER_TOKEN.initCreationEntry();
	}
}
