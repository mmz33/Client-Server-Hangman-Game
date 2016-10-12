import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientTCP extends ClientGame {
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;
	private Socket clientSocket;

	public ClientTCP() {
		super("outTCP.dat");
	}

	public static void main(String[] args) {
		ClientTCP tcpClient = new ClientTCP();
		tcpClient.run();
	}

	@Override
	public void run() {
		super.run();
		try {
			this.clientSocket = new Socket(Server.HOST_IP_ADDRESS, ServerTCP.HANGMAN_PORT_TCP);
			this.outToServer = new DataOutputStream(this.clientSocket.getOutputStream());
			this.inFromServer = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			toPlayOrNotToPlay();
		} catch (IOException ex) {
			handleFatalException(ex);
		}
	}

	@Override
	protected boolean play() {
		while (!this.clientSocket.isClosed()) {
			try {
				String response = this.inFromServer.readLine();
				if (response.contains("-")) {
					askForGuess(response);
				} else if (response.startsWith(Server.GAME_WON) || response.startsWith(Server.GAME_LOST)) {
					// String tries = this.inFromServer.readLine();
					gameWonOrLost(response);
					return true;
				} else if (response.equals(Server.GAME_OVER)) {
					gameOver();
					return false;
				}
			} catch (IOException ex) {
				handleFatalException(ex);
			}
		}
		return false;
	}

	@Override
	protected void writeToServer(String msg) {
		try {
			this.outToServer.writeBytes(msg + '\n');
			this.outToServer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void stopGame() {
		super.stopGame();
	}

	@Override
	protected void cleanUp() {
		try {
			this.consoleReader.close();
			this.outToServer.close();
			this.inFromServer.close();
			closeConnection();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void closeConnection() {
		try {
			if (this.clientSocket != null && !this.clientSocket.isClosed()) {
				this.clientSocket.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
