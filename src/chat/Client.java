package chat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

public class Client extends Thread {

	public Socket conexao;
	static String nome_cliente;

	public Client(Socket socket) {
		conexao = socket;
	}

	@Override
	public void run() {

		String msg_recebida = null; // mensagem recebida

		try {
			// cria os stremas de entrada e saída com o servidor
			BufferedReader entrada_servidor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
			
			do {
				// apresenta a linha do servidor na console
				msg_recebida = entrada_servidor.readLine();
				if(msg_recebida != null)
					System.out.println(msg_recebida);			
			} while (msg_recebida != null);
			
			conexao.close();
			System.out.println(nome_cliente.concat(" saiu do chat!").concat("\n"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {

		System.out.println("Cliente Ativo!");
		String msg_digitada;
		BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

		// Solicita um nome do cliente
		System.out.println("Informe o nome do cliente:");
		nome_cliente = teclado.readLine();

		// cria o socket de acesso ao server hostname na porta 80
		Socket cliente = new Socket("192.168.0.9", 8567);
		System.out.println(nome_cliente + " entrou no chat!");

		DataOutputStream saida_servidor = new DataOutputStream(cliente.getOutputStream());
		
		Thread t = new Client(cliente);
		t.start();

		System.out.println("Escolha um tema: \n"
				+ "[ 1 ] Entretenimento" + "\n"
				+ "[ 2 ] Economia" + "\n"
				+ "[ 3 ] Tecnologia" + "\n"
				+ "[ 4 ] Fim");
		// Assunto

		msg_digitada = teclado.readLine();

		saida_servidor.writeBytes(msg_digitada + "\n");
		
		while(true) {
			msg_digitada = teclado.readLine();
			saida_servidor.writeBytes(msg_digitada.concat("\n"));
			saida_servidor.writeBytes(nome_cliente.concat("\n"));
		}
		
	}

}
