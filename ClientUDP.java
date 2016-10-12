import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

public class ClientUDP extends ClientGame {
	private DatagramSocket clientSocket;

	public ClientUDP() {
		super("outUDP.dat");
	}

	public static void main(String[] args) {
		ClientUDP udpClient = new ClientUDP();
		udpClient.run();
	}

	@Override
	public void run() {
		super.run();
		try {
			this.clientSocket = new DatagramSocket(findRandomPort(), InetAddress.getLocalHost());
			toPlayOrNotToPlay();
		} catch (IOException ex) {
			handleFatalException(ex);
		}
	}

	@Override
	protected boolean play() {
		while (!this.clientSocket.isClosed()) {
			try {
				byte[] receiveData = new byte[1024];
				DatagramPacket fromServerPacket = new DatagramPacket(receiveData, receiveData.length);
				this.clientSocket.receive(fromServerPacket);
				String response = new String(fromServerPacket.getData(), fromServerPacket.getOffset(),
						fromServerPacket.getLength());
				if (response.contains("-")) {
					askForGuess(response);
				} else if (response.startsWith(Server.GAME_WON) || response.startsWith(Server.GAME_LOST)) {
					// fromServerPacket = new DatagramPacket(receiveData, receiveData.length);
					// this.clientSocket.receive(fromServerPacket);
					// String tries = new String(fromServerPacket.getData(), fromServerPacket.getOffset(),
					// fromServerPacket.getLength());
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
			byte[] sendData = msg.getBytes();
			DatagramPacket toServerPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(),
					ServerUDP.HANGMAN_PORT_UDP);
			this.clientSocket.send(toServerPacket);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		closeConnection();
	}

	private int findRandomPort() {
		for (int i = 0; i < 25; i++) {
			try {
				int port = ThreadLocalRandom.current().nextInt(1231, 65535);
				new ServerSocket(port).close();
				return port;
			} catch (Exception ex) {
				// try next port
			}
		}
		return -1;
	}

	@Override
	protected void closeConnection() {
		if (this.clientSocket != null && !this.clientSocket.isClosed()) {
			this.clientSocket.close();
		}
	}
}
