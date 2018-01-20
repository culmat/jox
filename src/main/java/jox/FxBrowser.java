package jox;

import static java.lang.Thread.sleep;

import java.awt.Dimension;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

public class FxBrowser extends JFrame {
	WebView webView;
	
	public FxBrowser(String title) {
		super(title);
		JFXPanel jfxPanel = new JFXPanel();
		add(jfxPanel);
		Platform.runLater(() -> {
			webView = new WebView();
			jfxPanel.setScene(new Scene(webView));
		});
		
		setPreferredSize(new Dimension(400, 800));
		pack();
	}
	
	public void load(String url) {
		Platform.runLater(() -> {
			webView.getEngine().load(url);
		});
	}
	
	public static void main(String[] args) throws Exception {
		FxBrowser browser = new FxBrowser("Browser");
		browser.load("https://duckduckgo.com/");
		browser.setVisible(true);
		browser.setDefaultCloseOperation(EXIT_ON_CLOSE);
		sleep(3000);
		browser.setVisible(false);
		browser.dispose();
	}

}
