
/**
 * Maintains an abstract connection on the Server side with a certain Client to send and receive data.
 *
 */
public abstract class ServerClientConnection {
	/**
	 * Tracks the number of played games for this client.
	 */
	protected int gamesCounter;

	public ServerClientConnection() {
		this.gamesCounter = 0;
	}

	public abstract void dealWithClient();

	/**
	 * Handles the PLAY/STOP request from the Client.
	 *
	 * @param request
	 *            the command protocol for requesting a new game
	 * @return <code>true</code> if the Client is now playing and <code>false</code> otherwise
	 */
	protected boolean handleRequest(String request) {
		if (request.equals(Server.GAME_PLAY)) {
			if (this.gamesCounter == Server.hangmanGames.size()) {
				writeToClient(Server.GAME_OVER);
				return false;
			} else {
				String word = Server.hangmanGamesList.get(this.gamesCounter);
				int tries = Server.hangmanGames.get(word);
				Hangman game = new Hangman(word, tries);
				playThisGame(game);
				this.gamesCounter++;
				return true;
			}
		} else if (request.equals(Server.GAME_STOP)) {
			return false;
		}
		return false;
	}

	/**
	 * Checks that the Client input is valid and checks the guess character in the game.
	 *
	 * @param game
	 *            the Hangman game being currently played
	 * @param input
	 *            the guess character of the Client
	 * @return <code>true</code> if the Client entered valid character and <code>false</code> otherwise
	 */
	protected boolean checkGuess(Hangman game, String input) {
		if (input != null && input.length() > 0) {
			input = input.trim();
			if (input.equals(Server.GAME_STOP)) {
				cleanUp();
			} else if (input.length() == 1) {
				char c = input.charAt(0);
				game.guess(c);
				return true;
			}
		}
		return false;
	}

	protected void playThisGame(Hangman game) {
		if (game.won()) {
			writeToClient(Server.GAME_WON + ":" + game.getWord());
			// writeToClient(String.valueOf(game.getRemainingTries()));
		} else if (game.lost()) {
			writeToClient(Server.GAME_LOST + ":" + game.getWord());
			// writeToClient(String.valueOf(game.getRemainingTries()));
		}
	}

	protected void handleConnectionException(Exception ex) {
		ex.printStackTrace();
		System.err.println("A problem occurred. Closing connection with Client now...");
		closeClientConnection();
	}

	protected abstract void closeClientConnection();

	protected abstract void writeToClient(String msg);

	protected abstract void cleanUp();
}