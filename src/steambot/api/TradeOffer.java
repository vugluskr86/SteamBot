package steambot.api;

public class TradeOffer {
	protected int id;
	protected boolean active;
	protected TradeUser user;

	protected TradeOffer(int id, boolean active, TradeUser user) {
		this.id = id;
		this.active = active;
		this.user = user;
	}

	public int getId() {
		return id;
	}

	public Trade getTrade() throws Exception {
		return user.getTrade(getId());
	}

	@Override
	public String toString() {
		
		String t;
		try {
			t = getTrade().toString();
		} catch (Exception e) {
			t = e.toString();
		}
		
		return "TradeOffer [id=" + id + ", active=" + active + ", getTrade()=" + t + "]";
	}

	public boolean isActive() {
		return active;
	}

	public TradeUser getUser() {
		return user;
	} 

}
