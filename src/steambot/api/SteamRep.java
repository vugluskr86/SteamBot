package steambot.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class SteamRep {
	public static boolean isBanned(String renderedSteamID) {
		try {
			return getUrlSource("http://steamrep.com/id2rep.php?steamID32=" + renderedSteamID).contains("SCAMMER");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String getUrlSource(String url) throws IOException {
		URL yahoo = new URL(url);
		URLConnection yc = yahoo.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuilder a = new StringBuilder();
		while ((inputLine = in.readLine()) != null)
			a.append(inputLine);
		in.close();

		return a.toString();
	}
}
