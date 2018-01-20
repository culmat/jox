package jox;

import static com.box.sdk.BoxAPIConnection.getAuthorizationURL;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.stop;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxUser;
import com.box.sdk.BoxUser.Info;

import javafx.application.Platform;
import spark.Request;

public class Jox {
	private String state = randomUUID().toString();
	private Token token;
	private BoxAPIConnection api;
	private Consumer<BoxAPIConnection> task;
	private FxBrowser browser;
	private boolean loggedIn;

	public synchronized void withBoxApi(Consumer<BoxAPIConnection> task) {
		this.task = task;
		if(loggedIn) {
			task.accept(api);
		} else {
			token = Token.load();
			if(token.hasAccessToken) {
				api = new BoxAPIConnection(token.clientID, token.clientSecret, token.accessToken, token.refreshToken);
				doLogin();
			} else {
				browserLogin();
			}
		}
	}

	private void doLogin() {
		try {
			BoxUser user = BoxUser.getCurrentUser(api);
			Info info = user.getInfo("login");
			System.out.println(String.format("logged in as %s", info.getLogin()));
			token.withAccessToken(api.getAccessToken(), api.getRefreshToken()).save();
		} catch (BoxAPIException e) {
			e.printStackTrace();
			token = token.withOutAccessToken();
			browserLogin();
			return;
		}
		loggedIn = true;
		task.accept(api);
	}

	private void browserLogin() {
		URI redirectUri = uri("http://localhost:"+token.port+"/");
		URL authorizationURL = getAuthorizationURL(token.clientID , redirectUri, state, asList("root_readwrite"));
		
		port(token.port);
		get("*", (req, res) -> {
			login(req);
			stop();
			return "processing ...";
		});
		if(browser == null) {
			browser = new FxBrowser("Browser");
		}
		browser.load(authorizationURL.toString());
		browser.setVisible(true);
		while (browser != null) {
			try {
				Thread.sleep(223);
			} catch (InterruptedException e) {}
		}
	}

	private void login(Request req) {
		String code = req.queryParamOrDefault("code", null);
		if(code != null) {
			String state = req.queryParamOrDefault("state", "");
			if(state.equals(this.state)) {
				api = new BoxAPIConnection(token.clientID, token.clientSecret, code);
				browser.setVisible(false);
				browser.dispose();
				browser= null;
				Platform.exit();
				doLogin();
			} else {
				System.err.println("states do not match");
			}
		} else {
			System.err.println(req.queryParamOrDefault("error", ""));
			System.err.println(req.queryParamOrDefault("error_description", ""));
		}
	}


	private URI uri(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(uri);
		}
	}


	public static void main(String[] args) throws Exception {
		Consumer<BoxAPIConnection> task = api -> {
			BoxFolder rootFolder = BoxFolder.getRootFolder(api);
			for (BoxItem.Info itemInfo : rootFolder) {
			    System.out.format("[%s] %s\n", itemInfo.getID(), itemInfo.getName());
			}
		};
		Jox jox = new Jox();
		jox.withBoxApi(task);
		jox.withBoxApi(task);

	}

}
