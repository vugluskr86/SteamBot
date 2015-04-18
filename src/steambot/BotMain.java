package steambot;

import steambot.api.Helper;
import steambot.api.SteamRep;
import steambot.api.Trade;
import steambot.api.TradeOffer;
import steambot.api.TradeUser;
import steambot.api.Inventory.Description;

public class BotMain {
	public static void main(String[] args) {
		try {
			TradeUser bot = new TradeUser();
			
			bot.addCookie("steamLogin", "snip", false);
			bot.addCookie("steamLoginSecure", "snip", true);
			bot.addCookie("steamMachineAuth snip", "snip", true);
			bot.addCookie("steamRememberLogin", "snip", false);
		

			int accepted = 0;
			int declined = 0;
			long startTime = System.currentTimeMillis();

			while (true) {
				System.out.println("Loop.");
				System.out.println("\tTime running: " + format(System.currentTimeMillis() - startTime));
				System.out.println("\tAccepted: " + accepted);
				System.out.println("\tDeclined: " + declined);
				for (TradeOffer to : bot.getIncomingTradeOffers()) {

					if (to.isActive()) {

						Trade trade = to.getTrade();

						Trade.TradeStatusUser me = trade.tradeStatus.me;
						Trade.TradeStatusUser them = trade.tradeStatus.them;

						if (SteamRep.isBanned(trade.getPartner().render())) {
							trade.decline();
							continue;
						}

						for (Trade.TradeAsset tradeAsset : me.assets) {
							System.out.println("\t" + me.getDescription(tradeAsset));
						}
						for (Trade.TradeAsset tradeAsset : me.assets) {
							Description pdescription = me.getDescription(tradeAsset);
							if (pdescription.appid.equals("440")) {
								String pname = pdescription.name;
								String pdefindex = pdescription.app_data.get("def_index");
								String pclassid = pdescription.classid;
								String pmyprice = "0.0";
								String ptheirprice = "0.0";
								System.out.println("ITEM_INFO.put(\"" + pname + "\", new HashMap<String, String>(){{put(\"name\", \"" + pname + "\");put(\"defindex\", \"" + pdefindex
										+ "\");put(\"classid\", \"" + pclassid + "\");put(\"myprice\", \"" + pmyprice + "\");put(\"theirprice\", \"" + ptheirprice + "\");}});");

							}
						}

						double myprice = Helper.getAssetWorth(me, true);

						System.out.println(myprice);

						for (Trade.TradeAsset tradeAsset : them.assets) {
							System.out.println("\t" + them.getDescription(tradeAsset));
						}

						for (Trade.TradeAsset tradeAsset : them.assets) {
							Description pdescription = them.getDescription(tradeAsset);
							if (pdescription.appid.equals("440")) {
								String pname = pdescription.name;
								String pdefindex = pdescription.app_data.get("def_index");
								String pclassid = pdescription.classid;
								String pmyprice = "0.0";
								String ptheirprice = "0.0";
								System.out.println("ITEM_INFO.put(\"" + pname + "\", new HashMap<String, String>(){{put(\"name\", \"" + pname + "\");put(\"defindex\", \"" + pdefindex
										+ "\");put(\"classid\", \"" + pclassid + "\");put(\"myprice\", \"" + pmyprice + "\");put(\"theirprice\", \"" + ptheirprice + "\");}});");
							}
						}

						double theirprice = Helper.getAssetWorth(them, false);
						System.out.println(theirprice);

						// checks -1 incase my inventory has items that are not
						// for sale (not in IDs.ITEM_INFO)
						if ((myprice == -1) || (theirprice + .01 < myprice)) {
							trade.decline();
							declined++;

						} else {
							trade.accept();
							accepted++;
						}

					}

				}
				Thread.sleep(20000);
				System.out.println("\r\n");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String format(long milliSeconds) {
		long secs = milliSeconds / 1000L;
		return String.format("%02d:%02d:%02d", new Object[] { Long.valueOf(secs / 3600L), Long.valueOf((secs % 3600L) / 60L), Long.valueOf(secs % 60L) });
	}
}
