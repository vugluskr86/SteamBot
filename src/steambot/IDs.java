package steambot;

import java.util.HashMap;
import java.util.Map;

public class IDs {
	public static Map<String, Map<String, String>> ITEM_INFO;
	
	static {
		ITEM_INFO = new HashMap<String, Map<String, String>>();
		
		//ITEM_INFO.put("Mann Co. Supply Crate Key", new HashMap<String, String>(){{put("name", "Mann Co. Supply Crate Key");put("defindex", "5021");put("classid", "101785959");put("myprice", "14");put("theirprice", "12");}});
		ITEM_INFO.put("Refined Metal", new HashMap<String, String>(){{put("name", "Refined Metal");put("defindex", "5002");put("classid", "2674");put("myprice", "1");put("theirprice", "1");}});
		ITEM_INFO.put("Reclaimed Metal", new HashMap<String, String>(){{put("name", "Reclaimed Metal");put("defindex", "5001");put("classid", "5564");put("myprice", ".33");put("theirprice", ".33");}});
		ITEM_INFO.put("Scrap Metal", new HashMap<String, String>(){{put("name", "Scrap Metal");put("defindex", "5000");put("classid", "2675");put("myprice", ".11");put("theirprice", ".11");}});
		//ITEM_INFO.put("Name Tag", new HashMap<String, String>(){{put("name", "Name Tag");put("defindex", "5020");put("classid", "83");put("myprice", "4.55");put("theirprice", "4.44");}});
		//ITEM_INFO.put("Description Tag", new HashMap<String, String>(){{put("name", "Description Tag");put("defindex", "5044");put("classid", "119");put("myprice", "1.77");put("theirprice", "1.66");}});
		//ITEM_INFO.put("Name Tag UNCRAFTABLE", new HashMap<String, String>(){{put("name", "Name Tag");put("defindex", "5020");put("classid", "260673173");put("myprice", "4.55");put("theirprice", "4.44");}});
		//ITEM_INFO.put("Mann Co. Orange", new HashMap<String, String>(){{put("name", "Mann Co. Orange");put("defindex", "5032");put("classid", "63");put("myprice", "6.66");put("theirprice", "5.66");}});
		ITEM_INFO.put("Tour of Duty Ticket", new HashMap<String, String>(){{put("name", "Tour of Duty Ticket");put("defindex", "725");put("classid", "260673174");put("myprice", "7.0");put("theirprice", "7.11");}});


	}
}
