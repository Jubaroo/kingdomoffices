package mod.sin.kingdomoffices.tokens;

public enum TokenType {
	INFORMATION_MINISTER("Disinformation Minister", 1500, 22721),
	COURT_MAGUS("Court Trixter", 1501, 22720),
	EARL_MARSHAL("Earl Warmonger", 1502, 22711),
	COURT_SMITH("Court Rustfriend", 1503, 22719),
	COURT_JESTER("Royal Jest", 1504, 22714),
	CHIEF_OF_ECONOMY("Economic catastrophe", 1505, 22716),
	ROYAL_PRIEST("Abbot", 1506, 22712),
	ROYAL_LOVER("Royal Page", 1507, 22713),
	ROYAL_AVENGER("Royal Bully", 1508, 22718),
	ROYAL_COOK("Royal Poisoner", 1509, 22717),
	ROYAL_HERALD("Royal Bragger", 1510, 22715);
	
	private int officeId;
	private int templateId;
	private String name;
	public int getOfficeId(){
		return officeId;
	}
	public int getTemplateId(){
		return templateId;
	}
	public String getName(){
		return name;
	}
	TokenType(String name, int officeId, int templateId){
		this.name = name;
		this.officeId = officeId;
		this.templateId = templateId;
	}
	public static TokenType getTokenByTemplate(int templateId){
		for(TokenType type : values()){
			if(type.getTemplateId() == templateId){
				return type;
			}
		}
		return null;
	}
	public static TokenType getTokenByOffice(int officeId){
		for(TokenType type : values()){
			if(type.getOfficeId() == officeId){
				return type;
			}
		}
		return null;
	}
	public static boolean isOfficeToken(int templateId){
		if(getTokenByTemplate(templateId) != null){
			return true;
		}
		return false;
	}
}
