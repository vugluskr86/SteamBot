package steambot.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import steambot.IDs;

public class Helper {

	public static double metalValue(Trade.TradeStatusUser tradeStatusUser) throws Exception {
		double total = 0.0;
		for (Trade.TradeAsset tradeAsset : tradeStatusUser.assets) {
			total += getWorth(tradeStatusUser.getDescription(tradeAsset));
		}
		return round(total, 2);
	}

	public static void printItems(Trade.TradeStatusUser tradeStatusUser) throws Exception {
		for (Trade.TradeAsset tradeAsset : tradeStatusUser.assets) {
			System.out.println("\t" + tradeStatusUser.getDescription(tradeAsset).name);
		}
	}

	public static boolean containsInvalidItem(Trade.TradeStatusUser tradeStatusUser) throws Exception {
		for (Trade.TradeAsset tradeAsset : tradeStatusUser.assets) {
			if (!isMetal(tradeStatusUser.getDescription(tradeAsset)) || !isWeapon(tradeStatusUser.getDescription(tradeAsset)))
				return false;
		}
		return true;
	}

	/**
	 * 
	 * @param tradeStatusUser
	 * @param me
	 * @return value of items in inventory. -1 if me == true && inventory has
	 *         items not listed int IDs.ITEM_INFO
	 * @throws Exception
	 */

	public static double getAssetWorth(Trade.TradeStatusUser tradeStatusUser, boolean me) throws Exception {
		double totalWorth = 0.0;
		for (Trade.TradeAsset tradeAsset : tradeStatusUser.assets) {
			double w = getWorth(tradeStatusUser.getDescription(tradeAsset), me);
			if (w == -1)
				return -1;
			totalWorth += w;
		}
		return totalWorth;
	}

	public static double getWorth(Inventory.Description description, boolean me) {
		if (!description.appid.equals("440")) {
			if (me) {
				return -1;
			} else {
				return 0.0;
			}
		}
		String name = description.name;
		String defIndex = description.app_data.get("def_index");
		String classid = description.classid;

		for (Entry<String, Map<String, String>> entry : IDs.ITEM_INFO.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			if (entry.getValue().get("name").equals(name) && entry.getValue().get("defindex").equals(defIndex) && entry.getValue().get("classid").equals(classid)) {
				System.out.println("\t Key = " + entry.getKey() + ", Value = " + entry.getValue());
				if (me) {
					System.out.println("\t" + entry.getValue().get("myprice"));
					return Double.valueOf(entry.getValue().get("myprice"));
				} else {
					System.out.println("\t" + entry.getValue().get("theirprice"));
					return Double.valueOf(entry.getValue().get("theirprice"));
				}
			}

		}

		if (me) {
			return -1;
		} else {
			return 0;
		}
	}

	public static double getWorth(Inventory.Description description) {
		if (isMetal(description)) {
			String defIndex = description.app_data.get("def_index");
			if (defIndex.equals("5000")) {
				return 0.11;
			} else if (defIndex.equals("5001")) {
				return 0.33;
			} else if (defIndex.equals("5002")) {
				return 1.0;
			}
		}
		return 0.0;
	}

	public static boolean isMetal(Inventory.Description description) {
		if (!description.appid.equals("440"))
			return false;
		String defIndex = description.app_data.get("def_index");
		if (!defIndex.equals("5000") && !defIndex.equals("5001") && !defIndex.equals("5002"))
			return false;
		return true;
	}

	public static boolean isKey(Inventory.Description description) {
		if (!description.appid.equals("440"))
			return false;
		String defIndex = description.app_data.get("def_index");
		return (defIndex.equals("5021"));
	}

	public static boolean onlyMetal(Trade.TradeStatusUser tradeStatusUser) throws Exception {
		for (Trade.TradeAsset tradeAsset : tradeStatusUser.assets) {
			if (!isMetal(tradeStatusUser.getDescription(tradeAsset))) {
				return false;
			}
		}
		return true;
	}

	public static boolean onlyKeys(Trade.TradeStatusUser tradeStatusUser) throws Exception {
		for (Trade.TradeAsset tradeAsset : tradeStatusUser.assets) {
			if (!isKey(tradeStatusUser.getDescription(tradeAsset))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isWeapon(Inventory.Description description) {
		if (!description.appid.equals("440"))
			return false;
		if (!description.app_data.get("quality").equals("6")) // not unique
																// quality
			return false;
		String type = getType(description);
		if (!type.equals("primary") && !type.equals("secondary") && !type.equals("melee") && !type.equals("pda") && !type.equals("pda2"))
			return false;
		return true;
	}

	public static boolean isCraftableWeapon(Inventory.Description description) {
		if (!description.appid.equals("440"))
			return false;
		if (!description.app_data.get("quality").equals("6"))
			return false;
		String type = getType(description);
		if (!type.equals("primary") && !type.equals("secondary") && !type.equals("melee") && !type.equals("pda") && !type.equals("pda2"))
			return false;
		return true;
	}

	public static String getType(Inventory.Description description) {
		for (HashMap<String, String> tag : description.tags) {
			if (tag.get("category").equals("Type")) {
				return tag.get("internal_name");
			}
		}
		return "unknown";
	}

	public static double round(double x, int scale) {
		return round(x, scale, BigDecimal.ROUND_HALF_UP);
	}

	public static double round(double x, int scale, int roundingMethod) {
		try {
			return (new BigDecimal(Double.toString(x)).setScale(scale, roundingMethod)).doubleValue();
		} catch (NumberFormatException ex) {
			if (Double.isInfinite(x)) {
				return x;
			} else {
				return Double.NaN;
			}
		}
	}

}
