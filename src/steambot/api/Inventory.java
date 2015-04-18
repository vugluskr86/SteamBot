package steambot.api;

import java.util.ArrayList;
import java.util.HashMap;

public class Inventory {
	public class Item {
		public String id;
		public String classid;
		public String instanceid;
		public String amount;
		public String pos;
		public transient Description description;
		public transient Inventory inventory;

	}

	public class Description {
		public String appid;
		public String classid;
		public String instanceid;
		public String name;
		public ArrayList<HashMap<String, String>> tags;
		public HashMap<String, String> app_data;
		@Override
		public String toString() {
			return "Description [appid=" + appid + ", classid=" + classid + ", instanceid=" + instanceid + ", name=" + name + ", tags=" + tags + ", app_data=" + app_data + "]";
		}
	}

	public boolean success;
	public transient long appId;
	public transient long contextId;
	public HashMap<String, Item> rgInventory;
	public HashMap<String, Description> rgDescriptions;

	protected void updateItems() {
		//if (rgInventory != null && rgInventory.values() != null)
			for (Item item : rgInventory.values()) {
				item.inventory = this;
				item.description = rgDescriptions.get(item.classid + "_" + item.instanceid);
			}
	}

	public ArrayList<Item> getItems() {
		return new ArrayList<Item>(rgInventory.values());
	}

	public Description getDescription(Trade.TradeAsset tradeAsset) {
		//if (rgInventory != null && rgInventory.values() != null){
			return rgInventory.get(Long.toString(tradeAsset.assetid)).description;
		//} else {
			//Description nil = new Description();
		//	return new Description();
		//}
		
	}
}
