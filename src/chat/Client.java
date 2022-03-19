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
			// cria os stremas de entrada com o servidor
			BufferedReader entrada_servidor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
			
			do {
				// Apresenta a mensagem enviada pelo servidor no console do cliente
				msg_recebida = entrada_servidor.readLine();	
				if(msg_recebida != null) System.out.println(msg_recebida);			
			} while (msg_recebida != null);
			
			conexao.close();
			System.out.println(nome_cliente.concat(" saiu do chat!").concat("\n"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {

		System.out.println("Cliente Ativo!");
		String msg_digitada;
		// Cria os stremas de entrada do cliente com o teclado
		BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

		// Solicita um nome do cliente
		System.out.println("Informe o nome do cliente:");
		nome_cliente = teclado.readLine();

		// Cria o socket de acesso ao server hostname na porta 8567
		Socket cliente = new Socket("192.168.0.9", 8567);
		System.out.println(nome_cliente + " entrou no chat!");

		// Cria os strems de saída com o servidor
		DataOutputStream saida_servidor = new DataOutputStream(cliente.getOutputStream());
		
		//Instancia uma nova threads
		Thread t = new Client(cliente);
		t.start();

		System.out.println("Escolha um tema: \n"
				+ "[ 1 ] Entretenimento" + "\n"
				+ "[ 2 ] Economia" + "\n"
				+ "[ 3 ] Tecnologia" + "\n"
				+ "[ 4 ] Fim");
		// Recebi o assunto selecionado
		msg_digitada = teclado.readLine();
		saida_servidor.writeBytes(msg_digitada + "\n");
		
		while(true) {
			// Envia para o servidor o nome do cliente
			saida_servidor.writeBytes(nome_cliente.concat("\n"));
			
			// Receber mensagem do cliente
			msg_digitada = teclado.readLine();
			saida_servidor.writeBytes(msg_digitada.concat("\n"));
			
		}
		
	}

}
