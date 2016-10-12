import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP extends Server {
	public static final int HANGMAN_PORT_TCP = 2221;
	private ServerSocket serverSocket;

	public ServerTCP(String fileName) {
		super(fileName);
		try {
			this.serverSocket = new ServerSocket(ServerTCP.HANGMAN_PORT_TCP);
			System.out.println("ServerTCP is now running...");
		} catch (IOException ex) {
			handleFatalException(ex);
		}
	}

	public static void main(String[] args) {
		String fileName = null;
		if (args.length > 0) {
			fileName = args[0];
		}
		ServerTCP tcpServer = new ServerTCP(fileName);
		tcpServer.run();
	}

	@Override
	public void run() {
		while (!this.serverSocket.isClosed()) {
			try {
				final Socket connectionSocket = this.serverSocket.accept();
				this.executors.execute(new Runnable() {
					@Override
					public void run() {
						new ClientConnection(connectionSocket).dealWithClient();
					}
				});
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void stopServer() {
		if (this.serverSocket != null && !this.serverSocket.isClosed()) {
			try {
				this.serverSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private class ClientConnection extends ServerClientConnection {
		private Socket connectionSocket;
		private BufferedReader inFromClient;
		private DataOutputStream outToClient;

		public ClientConnection(Socket connectionSocket) {
			this.connectionSocket = connectionSocket;
			try {
				this.inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				this.outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			} catch (Exception ex) {
				handleConnectionException(ex);
			}
		}

		@Override
		public void dealWithClient() {
			while (!this.connectionSocket.isClosed()) {
				try {
					String request = this.inFromClient.readLine();
					if (!handleRequest(request)) {
						break;
					}
				} catch (Exception ex) {
					handleConnectionException(ex);
				}
			}
		}

		@Override
		protected void playThisGame(Hangman game) {
			while (!game.won() && !game.lost()) {
				writeToClient(game.getEncodedWord());
				try {
					String input = this.inFromClient.readLine();
					if (!checkGuess(game, input)) {
						break;
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					break;
				}
			}
			super.playThisGame(game);
		}

		@Override
		protected void writeToClient(String msg) {
			try {
				this.outToClient.writeBytes(msg + '\n');
				this.outToClient.flush();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		@Override
		protected void cleanUp() {
			try {
				this.outToClient.close();
				this.inFromClient.close();
				closeClientConnection();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		@Override
		protected void closeClientConnection() {
			try {
				this.connectionSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
