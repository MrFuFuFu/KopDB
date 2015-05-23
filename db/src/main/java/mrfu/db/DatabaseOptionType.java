package mrfu.db;

public enum DatabaseOptionType {
	OPTION_INSERT(1),
	OPTION_UPDATE(2),
	OPTION_REPLACE(3),
	OPTION_DELETE(4);
	int VALUE;
	DatabaseOptionType(int value){
		this.VALUE = value;
	}
	public static DatabaseOptionType VALUEOF(int value){
		DatabaseOptionType type = null;
		switch(value){
			case 1:
				type = OPTION_INSERT;
				break;
			case 2:
				type = OPTION_UPDATE;
				break;
			case 3:
				type = OPTION_REPLACE;
				break;
			case 4:
				type = OPTION_DELETE;
				break;
			default:
				break;
		}
		return type;
	}
}
