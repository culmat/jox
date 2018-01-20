package jox;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pnuts.awt.PnutsLayout;

public class Token {
	public final String clientID, clientSecret, accessToken, refreshToken;
	public final boolean hasAccessToken;
	public final int port;

	private static Path path() {
		return Paths.get(System.getProperty("user.home"), "jox.tokens.txt");
	}

	private Token(int port, String clientID, String clientSecret, String accessToken, String refreshToken) {
		this.port = port;
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		hasAccessToken = accessToken.length() > 0 && refreshToken.length() > 0;
	}

	public static Token load() {
		if (path().toFile().exists()) {
			try {
				List<String> lines = Files.readAllLines(path());
				switch (lines.size()) {
				case 4:
				case 3:
					return new Token(parseInt(lines.get(0)), lines.get(1), lines.get(2), "", "");
				case 5:
					return new Token(parseInt(lines.get(0)), lines.get(1), lines.get(2), lines.get(3), lines.get(4));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return askForConfiguration();
	}

	private static Token askForConfiguration() {
		JTextField portField = new JTextField(10);
		JTextField clientIDField = new JTextField(32);
		JTextField clientSecretField = new JTextField(32);
		
		JPanel panel = new JPanel(new PnutsLayout(2));
		String labelConstraint = "align=right";
		String inputConstraint = "expand=x, fill=x";
		panel.add(new JLabel("port:"), labelConstraint);
		panel.add(portField, inputConstraint);
		panel.add(new JLabel("client id :"), labelConstraint);
		panel.add(clientIDField, inputConstraint);
		panel.add(new JLabel("client secret :"), labelConstraint);
		panel.add(clientSecretField, inputConstraint);
		
		int result = JOptionPane.showConfirmDialog(null, panel, "Please enter box application config",
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			Token token = new Token(parseInt(portField.getText()), clientIDField.getText(), clientSecretField.getText(), "", "");
			token.save();
			return token;
		}
		System.err.println("FATAL : configuration is needed.\nexit.");
		System.exit(0);
		return null;
	}

	public Token save() {
		try {
			Files.write(path(),
					(port + "\n" + clientID + "\n" + clientSecret + "\n" + accessToken + "\n" + refreshToken)
							.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public String toString() {
		return String.format("access: %s, refresh: %s", accessToken, refreshToken);
	}

	public static void main(String[] args) {
		System.out.println(askForConfiguration());
	}

	public Token withAccessToken(String accessToken, String refreshToken) {
		return new Token(port, clientID, clientSecret, accessToken, refreshToken);
	}

	public Token withOutAccessToken() {
		return withAccessToken("", "");
	}
}
