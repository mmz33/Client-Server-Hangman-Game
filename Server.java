import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Abstract Server that contains communication protocol commands for the Hangman Client-Server game and other interface
 * methods to be implemented at both TCP and UDP Servers.
 *
 */
public abstract class Server {
	public static final String HOST_IP_ADDRESS = "127.0.0.1"; // localhost

	/**
	 * List of protocol commands.
	 */
	public static final String GAME_PLAY = "PLAY";
	public static final String GAME_STOP = "STOP";
	public static final String GAME_OVER = "OVER";
	public static final String GAME_WON = "WON";
	public static final String GAME_LOST = "LOST";

	protected static Map<String, Integer> hangmanGames; /* HangmanWord --> NumberOfTries */
	protected static List<String> hangmanGamesList; /* HangmanWords */

	/**
	 * Threads that will handle new Client connections.
	 */
	protected Executor executors;
	private static final int NTHREADS = 6;

	public Server() {
		this(null);
	}

	public Server(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			fileName = "in.dat";
		}
		Server.hangmanGames = FileUtils.getAllHangmanGames(fileName);
		Server.hangmanGamesList = new ArrayList<String>(Server.hangmanGames.keySet());

		this.executors = Executors.newFixedThreadPool(Server.NTHREADS);
	}

	/**
	 * Starts the Server!
	 */
	public abstract void run();

	public abstract void stopServer();

	/**
	 * Handles any non-recoverable exceptions that might occur. Possibly during starting the Server.
	 *
	 * The Server will be shut down then.
	 *
	 * @param ex
	 *            the exception that occurred
	 */
	protected void handleFatalException(Exception ex) {
		ex.printStackTrace();
		System.err.println("A fatal problem occurred. Server is shutting down now...");
		stopServer();
		System.exit(1);
	}
}
