import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Abstract Client-side game to handle common functions such as asking for user input.
 *
 */
public abstract class ClientGame {
	protected final BufferedReader consoleReader;
	protected PrintWriter out;
	protected int tries;
	private boolean fileInitialized = false;

	public ClientGame(String fileName) {
		this.consoleReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			this.out = new PrintWriter(new File(fileName));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Initialize resources and start the Client game.
	 */
	public void run() {
		System.out.println("Client-Server Hangman game is starting...");
	}

	protected void toPlayOrNotToPlay() {
		String userInput = null;
		while (true) {
			System.out.print("Do you want to play? (y/n) ");
			try {
				userInput = this.consoleReader.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
				stopGame();
				break;
			}
			userInput = userInput.trim().toLowerCase();
			if (userInput == null) {
				stopGame();
				break;
			} else if (userInput.equals("y")) {
				if (!this.fileInitialized) {
					FileUtils.initFile(this.out);
					this.fileInitialized = !this.fileInitialized;
				}
				writeToServer(Server.GAME_PLAY);
				this.tries = 0;
				if (!play()) {
					break;
				}
			} else if (userInput.equals("n")) {
				stopGame();
				break;
			} else {
				System.out.println("Invalid input.");
			}
		}
	}

	/**
	 * Play a Hangman game interactively.
	 *
	 * Show the user the encoded word and ask for letter/digit input as guess.
	 *
	 * @return <code>true</code> if the game is won or lost, and <code>false</code> if the server runs out of games or
	 *         an exception occurs
	 */
	protected abstract boolean play();

	/**
	 * Asks the user to input a guess character.
	 *
	 * @param response
	 *            the returned data from the Server which is the encoded word
	 */
	protected void askForGuess(String response) {
		System.out.println("Guess the word: " + response);
		while (true) {
			System.out.print("Enter a character: ");
			try {
				String userInput = this.consoleReader.readLine();
				if (userInput == null) {
					stopGame();
					break;
				} else {
					userInput = userInput.trim().toLowerCase();
					if (!userInput.matches("^[A-Za-z0-9]$")) {
						System.out.println("Invalid input. Type only one letter or number!");
					} else {
						writeToServer(userInput);
						this.tries++;
						break;
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				stopGame();
				break;
			}
		}
	}

	/**
	 * The current Hangman game is over; either won or lost. Then, write the details to a file.
	 *
	 * @param response
	 *            the result received from the Server
	 *
	 */
	protected void gameWonOrLost(String response) {
		String won = response.substring(0, response.indexOf(":"));
		String word = response.substring(response.indexOf(":") + 1);
		System.out.print("You " + won.toLowerCase() + "! ");
		System.out.println("The word was '" + word + "'.\n");
		FileUtils.printToFile(this.out, word, (won.equals("WON") ? "Yes" : "No"), this.tries);
	}

	/**
	 * The Server has no more Hangman games. Clean up the resources then.
	 */
	protected void gameOver() {
		cleanUp();
		System.out.println("Sorry, no more games!");
	}

	protected void handleFatalException(Exception ex) {
		ex.printStackTrace();
		System.err.println("A fatal problem occurred. Closing the game now...");
		closeConnection();
	}

	/**
	 * Write a String message to the Server and flush the output stream to force sending the data.
	 *
	 * @param msg
	 *            the data to be sent to the Server in String representation
	 */
	protected abstract void writeToServer(String msg);

	/**
	 * Cleans up the resources for this Client app.
	 */
	protected void cleanUp() {
		try {
			this.consoleReader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Sends to the Server that the Client no longer wants to play.
	 *
	 * And frees up the resources.
	 */
	protected void stopGame() {
		writeToServer(Server.GAME_STOP);
		cleanUp();
	}

	protected abstract void closeConnection();
}
