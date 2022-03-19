package chat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

public class Server extends Thread {

	private Socket conexao;

	public final static String ENTRETENIMENTO = "1";
	public final static String ECONOMIA = "2";
	public final static String TECNOLOGIA = "3";
	public final static String FIM = "4";

	public static Vector<DataOutputStream> VEntretenimento = new Vector<DataOutputStream>();
	public static Vector<DataOutputStream> VEconomia = new Vector<DataOutputStream>();
	public static Vector<DataOutputStream> VTecnologia = new Vector<DataOutputStream>();

	Server(Socket socket) {
		conexao = socket;
	}

	@Override
	public void run() {
		String msg_recebida; // lida do cliente
		String msg_enviada; // enviada ao cliente
		String nome_cliente;
		String assunto = null;

		try {
			// cria streams de entrada e saida com o cliente que chegou
			BufferedReader entrada_cliente = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
			DataOutputStream saida_cliente = new DataOutputStream(conexao.getOutputStream());

			// lê mensagem do cliente

			msg_recebida = entrada_cliente.readLine();

			switch (msg_recebida) {
			case ENTRETENIMENTO:
				VEntretenimento.add(saida_cliente);
				assunto = ENTRETENIMENTO;
				break;
			case ECONOMIA:
				VEconomia.add(saida_cliente);
				assunto = ECONOMIA;
				break;

			case TECNOLOGIA:
				VTecnologia.add(saida_cliente);
				assunto = TECNOLOGIA;
				break;
			}
			msg_recebida = entrada_cliente.readLine();
			while (msg_recebida != null && !(msg_recebida.trim().equals("")) && !(msg_recebida.startsWith("4"))) {

				System.out.println(msg_recebida);
				// monta retorno para o cliente
				msg_enviada = msg_recebida + '\n';
				enviarParaTodosOsClientes(saida_cliente, msg_enviada, assunto);
				msg_recebida = entrada_cliente.readLine();

			}
			System.out.println("Cliente desconectado!");
			conexao.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void enviarParaTodosOsClientes(DataOutputStream saida_cliente, String msg_enviada, String assunto) {
		Map<String, Vector<DataOutputStream>> assuntos = new HashMap<>();
		assuntos.put(ENTRETENIMENTO, VEntretenimento);
		assuntos.put(ECONOMIA, VEconomia);
		assuntos.put(TECNOLOGIA, VTecnologia);

		Iterator<Entry<String, Vector<DataOutputStream>>> iterator = assuntos.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Vector<DataOutputStream>> entry = iterator.next();
			if (entry.getKey().equals(assunto)) {
				for (DataOutputStream saida : entry.getValue()) {
					if (saida_cliente != saida) {
						try {
							saida.writeBytes("Entretenimento: " + msg_enviada);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {

		// cria socket de comunicação com os clientes na porta 8657
		ServerSocket servidor = new ServerSocket(8567);

		System.out.println("Esperando cliente se conectar...");

		// espera msg de algum cliente e trata
		while (true) {

			// espera conexão de algum cliente
			Socket client = servidor.accept();
			Thread t = new Server(client);
			System.out.println("Cliente conectado!");
			t.start();

		}
	}
}
