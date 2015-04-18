package steambot.api;

import steambot.api.TradeUser.REQUEST_TYPES;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Trade {

	private TradeUser tradeUser;
	private String sessionId;
	private String partnerName;
	private SteamID partner;
	private int tradeId;
	private String inventoryLoadUrl;
	private String partnerInventoryLoadUrl;
	public TradeStatus tradeStatus;
	private HashMap<Long, HashMap<Long, Inventory>> myInventoryCache;
	private HashMap<Long, HashMap<Long, Inventory>> theirInventoryCache;

	/**
	 * Creates a new Trade
	 * 
	 * @param tradeUser
	 * @param id
	 * @throws Exception
	 */
	protected Trade(TradeUser tradeUser, int id, SteamID tradePartner) throws Exception {
		theirInventoryCache = new HashMap<Long, HashMap<Long, Inventory>>();
		myInventoryCache = new HashMap<Long, HashMap<Long, Inventory>>();

		this.tradeUser = tradeUser;
		this.tradeId = id;

		Gson gson = new Gson();

		String html;
		if (id == 0) {
			System.out.println("http://steamcommunity.com/tradeoffer/new/?partner=" + tradePartner.getAccountId());
			html = tradeUser.fetch("http://steamcommunity.com/tradeoffer/new/?partner=" + tradePartner.getAccountId(), REQUEST_TYPES.GET, null, false);
		} else {
			html = tradeUser.fetch("http://steamcommunity.com/tradeoffer/" + id + "/", REQUEST_TYPES.GET, null, false);
		}
		Pattern pattern = Pattern.compile("^\\s*var\\s+(g_.+?)\\s+=\\s+(.+?);\\r?$", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(html);
		Map<String, String> javascriptGlobals = new HashMap<String, String>();
		while (matcher.find()) {
			javascriptGlobals.put(matcher.group(1), matcher.group(2));
		}

		//System.out.println("SESSIONID " + sessionId );
		tradeStatus = gson.fromJson(javascriptGlobals.get("g_rgCurrentTradeStatus"), TradeStatus.class);
		partner = new SteamID(Long.parseLong(gson.fromJson(javascriptGlobals.get("g_ulTradePartnerSteamID"), String.class)));
		partnerName = gson.fromJson(javascriptGlobals.get("g_strTradePartnerPersonaName"), String.class);
		sessionId = gson.fromJson(javascriptGlobals.get("g_sessionID"), String.class);
		inventoryLoadUrl = gson.fromJson(javascriptGlobals.get("g_strInventoryLoadURL"), String.class);
		partnerInventoryLoadUrl = gson.fromJson(javascriptGlobals.get("g_strTradePartnerInventoryLoadURL"), String.class);
		//System.out.println("SESSIONID " + sessionId );
		tradeStatus.trade = this;

		tradeStatus.me.tradeStatus = tradeStatus;
		tradeStatus.me.gameContextMap = gson.fromJson(javascriptGlobals.get("g_rgAppContextData"), new TypeToken<Map<String, GameContext>>() {
		}.getType());
		tradeStatus.me.isPartner = false;

		tradeStatus.them.tradeStatus = tradeStatus;
		tradeStatus.them.gameContextMap = gson.fromJson(javascriptGlobals.get("g_rgPartnerAppContextData"), new TypeToken<Map<String, GameContext>>() {
		}.getType());
		tradeStatus.them.isPartner = true;

		String tradeOfferMessage = "";
		String tradeOfferJson = gson.toJson(tradeStatus);

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("sessionid", sessionId));
		data.add(new BasicNameValuePair("partner", Long.toString(partner.getCommunityId())));
		data.add(new BasicNameValuePair("tradeoffermessage", tradeOfferMessage));
		data.add(new BasicNameValuePair("json_tradeoffer", tradeOfferJson));
		data.add(new BasicNameValuePair("tradeofferid_countered", Integer.toString(id)));
	}

	private Inventory fetchMyInventory(long appId, long contextId) throws Exception {
		if (!myInventoryCache.containsKey(appId)) {
			myInventoryCache.put(appId, new HashMap<Long, Inventory>());
		}

		if (myInventoryCache.get(appId).containsKey(contextId)) {
			return myInventoryCache.get(appId).get(contextId);
		}

		Gson gson = new Gson();
		Inventory inventory = gson.fromJson(tradeUser.fetch(inventoryLoadUrl + appId + "/" + contextId + "/?trading=1", REQUEST_TYPES.GET, null, true), Inventory.class);
		inventory.appId = appId;
		inventory.contextId = contextId;
		inventory.updateItems();
		myInventoryCache.get(appId).put(contextId, inventory);
		return inventory;
	}

	private Inventory fetchTheirInventory(long appId, long contextId) throws Exception {
		if (!theirInventoryCache.containsKey(appId)) {
			theirInventoryCache.put(appId, new HashMap<Long, Inventory>());
		}

		if (theirInventoryCache.get(appId).containsKey(contextId)) {
			return theirInventoryCache.get(appId).get(contextId);
		}
		Gson gson = new Gson();

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("sessionid", sessionId));
		data.add(new BasicNameValuePair("partner", Long.toString(partner.getCommunityId())));
		data.add(new BasicNameValuePair("appid", Long.toString(appId)));
		data.add(new BasicNameValuePair("contextid", Long.toString(contextId)));

		//System.out.println(partnerInventoryLoadUrl);
		//System.out.println(data.toString());
		//System.out.println(tradeUser.fetch(partnerInventoryLoadUrl, "POST", data, true, "https://steamcommunity.com/tradeoffer/" + tradeId + "/"));
	
		Inventory inventory = gson.fromJson(tradeUser.fetch(partnerInventoryLoadUrl, REQUEST_TYPES.POSTURI, data, true, "https://steamcommunity.com/tradeoffer/" + tradeId + "/"), Inventory.class);
		inventory.appId = appId;
		inventory.contextId = contextId;
		inventory.updateItems();
		theirInventoryCache.get(appId).put(contextId, inventory);
		return inventory;
	}

	/**
	 * Sends a new trade offer OR counter offer, depending on if this is a new
	 * trade offer
	 * 
	 * @param message
	 *            The message to be sent along with the trade offer
	 * @throws Exception
	 */
	public boolean update(String message) throws Exception {
		tradeStatus.version++;
		tradeStatus.newversion = true;
		Gson gson = new Gson();
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("sessionid", sessionId));
		data.add(new BasicNameValuePair("partner", Long.toString(partner.getCommunityId())));
		data.add(new BasicNameValuePair("tradeoffermessage", message));
		data.add(new BasicNameValuePair("json_tradeoffer", gson.toJson(tradeStatus)));
		int serverId = 1;
		data.add(new BasicNameValuePair("serverid", Integer.toString(serverId)));
		data.add(new BasicNameValuePair("trade_offer_create_params", "{}"));
	
		if (tradeId != 0) {
			data.add(new BasicNameValuePair("'tradeofferid_countered'", Integer.toString(tradeId)));
		}
		System.out.println(Arrays.asList(data).toString());
		String result = tradeUser.fetch("https://steamcommunity.com/tradeoffer/new/send", REQUEST_TYPES.POST, data, true);
		// TODO: parse/return the result

		//System.out.println(result);
		System.out.println(new Gson().toJson(result));
		Gson r = new Gson();
		SentTradeResult str = r.fromJson(result,SentTradeResult.class);
		return str.strError == null;
	}

	/**
	 * Accepts the trade After calling this, the trade object will be in an
	 * unusable state
	 * 
	 * @throws Exception
	 */
	public void accept() throws Exception {
		int serverId = 1;
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("sessionid", sessionId));
		data.add(new BasicNameValuePair("tradeofferid", Long.toString(tradeId)));
		data.add(new BasicNameValuePair("serverid", Integer.toString(serverId)));
		data.add(new BasicNameValuePair("partner", Long.toString(partner.getCommunityId())));
		String result = tradeUser.fetch("https://steamcommunity.com/tradeoffer/" + tradeId + "/accept", REQUEST_TYPES.POST, data, true);
		// TODO: parse/return the result

		System.out.println(result);

		//System.out.println(new Gson().toJson(result));
	}

	/**
	 * Declines the trade After calling this, the trade object will be in an
	 * unusable state
	 * 
	 * @throws Exception
	 */
	public void decline() throws Exception {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("sessionid", sessionId));
		System.out.println("sessionid: " + sessionId);
		String result = tradeUser.fetch("https://steamcommunity.com/tradeoffer/" + tradeId + "/decline", REQUEST_TYPES.POST, data, true);
		// TODO: parse/return the result
		//System.out.println(result);
		//System.out.println(new Gson().toJson(result));
	}

	/*
	 * These classes are mostly for GSON purposes
	 */
	public class TradeStatus {
		public boolean newversion;
		public int version;
		public TradeStatusUser me;
		public TradeStatusUser them;
		public transient Trade trade;

	}

	public class TradeStatusUser {
		boolean ready;
		private transient boolean isPartner;
		public ArrayList<TradeAsset> assets;
		transient Map<String, GameContext> gameContextMap;
		public transient TradeStatus tradeStatus;
		
		
		//TODO idk what this is
		public ArrayList<Object> currency;
		
		
		public Inventory fetchInventory(long appId, long contextId) throws Exception {
			if (isPartner)
				return tradeStatus.trade.fetchTheirInventory(appId, contextId);
			else
				return tradeStatus.trade.fetchMyInventory(appId, contextId);
		}

		public Inventory.Description getDescription(TradeAsset tradeAsset) throws Exception {
			return fetchInventory(tradeAsset.appid, tradeAsset.contextid).getDescription(tradeAsset);
		}

		public boolean addItem(Inventory.Item item) {
			return addItem(item.inventory.appId, item.inventory.contextId, item.id);
		}

		private boolean addItem(long appId, long contextId, String id) {
			TradeAsset tradeAsset = new TradeAsset();
			tradeAsset.appid = appId;
			tradeAsset.contextid = contextId;
			tradeAsset.assetid = Long.parseLong(id);
			addItem(tradeAsset);
			return true;
		}

		public boolean addItem(TradeAsset asset) {
			if (!assets.contains(asset)) {
				assets.add(asset);
				return true;
			}
			return false;
		}

		public boolean removeItem(TradeAsset asset) {
			if (assets.contains(asset)) {
				assets.remove(asset);
				return true;
			}
			return false;
		}

		public boolean containsItem(int appId, int contextId, int assetId) {
			for (TradeAsset tradeAsset : assets) {
				if (tradeAsset.appid == appId && tradeAsset.contextid == contextId && tradeAsset.assetid == assetId)
					return true;
			}
			return false;
		}

	}

	public class TradeAsset {
		public long appid;
		public long contextid;
		public long amount;
		public long assetid;
	}

	public class Context {
		public int asset_count;
		public String id;
		public String name;

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Context context = (Context) o;

			if (asset_count != context.asset_count)
				return false;
			if (!id.equals(context.id))
				return false;
			if (!name.equals(context.name))
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = asset_count;
			result = 31 * result + id.hashCode();
			result = 31 * result + name.hashCode();
			return result;
		}
	}

	public class GameContext {
		public int appid;
		public String name;
		public String icon;
		public int asset_count;
		public String inventory_logo;
		public String trade_permissions;
		public HashMap<String, Context> rgContexts;
		@Override
		public String toString() {
			return "GameContext [appid=" + appid + ", name=" + name + ", icon=" + icon + ", asset_count=" + asset_count + ", inventory_logo=" + inventory_logo + ", trade_permissions="
					+ trade_permissions + ", rgContexts=" + rgContexts + "]";
		}
	}

	public class SentTradeResult {
		public long tradeofferid;
		public String strError;
	}
	@Override
	public String toString() {
		return "Trade [tradeUser=" + tradeUser + ", sessionId=" + sessionId + ", partnerName=" + partnerName + ", partner=" + partner + ", tradeId=" + tradeId + ", inventoryLoadUrl="
				+ inventoryLoadUrl + ", partnerInventoryLoadUrl=" + partnerInventoryLoadUrl + ", tradeStatus=" + tradeStatus + ", myInventoryCache=" + myInventoryCache + ", theirInventoryCache="
				+ theirInventoryCache + "]";
	}

	public SteamID getPartner() {
		return partner;
	}

}
