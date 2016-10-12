import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerUDP extends Server {
	public static final int HANGMAN_PORT_UDP = 9879;
	private DatagramSocket serverSocket;
	// private Queue<Pair<InetAddress, Integer>> clients; // keep track of clients ip_address and port

	public ServerUDP(String fileName) {
		super(fileName);
		try {
			this.serverSocket = new DatagramSocket(ServerUDP.HANGMAN_PORT_UDP);
			System.out.println("ServerUDP is now running...");
			// this.clients = new LinkedList<Pair<InetAddress, Integer>>();
		} catch (IOException ex) {
			handleFatalException(ex);
		}
	}

	public static void main(String[] args) {
		String fileName = null;
		if (args.length > 0) {
			fileName = args[0];
		}
		ServerUDP udpServer = new ServerUDP(fileName);
		udpServer.run();
	}

	@Override
	public void run() {
		while (!this.serverSocket.isClosed()) {
			try {
				new ClientConnection().dealWithClient();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void stopServer() {
		if (this.serverSocket != null && !this.serverSocket.isClosed()) {
			this.serverSocket.close();
		}
	}

	private class ClientConnection extends ServerClientConnection {
		private DatagramPacket fromClientPacket;
		private InetAddress clientIpAddress;
		private int clientPort;

		public ClientConnection() {
			super();
			byte[] receiveData = new byte[1024];
			this.fromClientPacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				ServerUDP.this.serverSocket.receive(this.fromClientPacket);
			} catch (IOException ex) {
				handleConnectionException(ex);
				throw new RuntimeException();
			}
			this.clientIpAddress = this.fromClientPacket.getAddress();
			this.clientPort = this.fromClientPacket.getPort();
			// ServerUDP.this.clients.add(new Pair<InetAddress, Integer>(this.clientIpAddress, this.clientPort));
		}

		@Override
		public void dealWithClient() {
			while (true) {
				try {
					String request = new String(this.fromClientPacket.getData(), this.fromClientPacket.getOffset(),
							this.fromClientPacket.getLength());
					if (!handleRequest(request)) {
						break;
					}
					byte[] receiveData = new byte[1024];
					this.fromClientPacket = new DatagramPacket(receiveData, receiveData.length);
					ServerUDP.this.serverSocket.receive(this.fromClientPacket);
				} catch (Exception ex) {
					handleConnectionException(ex);
					break;
				}
			}
		}

		@Override
		protected void playThisGame(Hangman game) {
			while (!game.won() && !game.lost()) {
				writeToClient(game.getEncodedWord());
				try {
					byte[] receiveData = new byte[1024];
					this.fromClientPacket = new DatagramPacket(receiveData, receiveData.length);
					ServerUDP.this.serverSocket.receive(this.fromClientPacket);
					String input = new String(this.fromClientPacket.getData(), this.fromClientPacket.getOffset(),
							this.fromClientPacket.getLength());
					if (!checkGuess(game, input)) {
						break;
					}
				} catch (Exception ex) {
					handleConnectionException(ex);
					return;
				}
			}
			super.playThisGame(game);
		}

		@Override
		protected void writeToClient(String msg) {
			try {
				byte[] sendData = msg.getBytes();
				DatagramPacket toClientPacket = new DatagramPacket(sendData, sendData.length, this.clientIpAddress,
						this.clientPort);
				ServerUDP.this.serverSocket.send(toClientPacket);
			} catch (IOException ex) {
				handleConnectionException(ex);
			}
		}

		@Override
		protected void cleanUp() {

		}

		@Override
		protected void closeClientConnection() {

		}
	}

	// public class Pair<T1, T2> {
	// private T1 first;
	// private T2 second;
	//
	// public Pair(T1 first, T2 second) {
	// this.first = first;
	// this.second = second;
	// }
	//
	// public T1 getFirst() {
	// return this.first;
	// }
	//
	// public T2 getSecond() {
	// return this.second;
	// }
	// }
}
