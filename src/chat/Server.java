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

	public final static String ENTRETENIMENTO = "1", ECONOMIA = "2", TECNOLOGIA = "3", FIM = "4";

	public static Vector<DataOutputStream> VEntretenimento = new Vector<DataOutputStream>();
	public static Vector<DataOutputStream> VEconomia = new Vector<DataOutputStream>();
	public static Vector<DataOutputStream> VTecnologia = new Vector<DataOutputStream>();
	
	Map<String, Vector<DataOutputStream>> assuntos = new HashMap<>();
	
	Server(Socket socket) {
		conexao = socket;
	}

	@Override
	public void run() {
		String msg_recebida, // lida do cliente
			   msg_enviada, // enviada ao cliente
		       nome_cliente,
		       assunto = null;

		try {
			// cria streams de entrada e saida com o cliente que chegou
			BufferedReader entrada_cliente = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
			DataOutputStream saida_cliente = new DataOutputStream(conexao.getOutputStream());

			// lê mensagem do cliente, seleção do assunto
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
			
			while (msg_recebida != null && !(msg_recebida.trim().equals(""))) {
				msg_recebida = entrada_cliente.readLine();
				nome_cliente = entrada_cliente.readLine();
				if(msg_recebida.startsWith(FIM)) {
					msg_enviada = nome_cliente.concat(" saiu do chat!").concat("\n");
					enviarMensagemParaTodosOsClientes(saida_cliente, msg_enviada, assunto);	
					break;
				}
					
				// monta retorno para os clientes
				msg_enviada = "<".concat(nome_cliente).concat("> ").concat(msg_recebida).concat("\n");
				System.out.println(msg_enviada);
				enviarMensagemParaTodosOsClientes(saida_cliente, msg_enviada, assunto);
			}
			System.out.println("Cliente desconectado!");
			removeClient(saida_cliente, assunto);
			conexao.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, Vector<DataOutputStream>> getMappingAssuntos(){
		Map<String, Vector<DataOutputStream>> assuntos = new HashMap<>();
		assuntos.put(ENTRETENIMENTO, VEntretenimento);
		assuntos.put(ECONOMIA, VEconomia);
		assuntos.put(TECNOLOGIA, VTecnologia);
		return assuntos;
	}
	
	private Iterator<Entry<String, Vector<DataOutputStream>>> getIteratorAssuntos(){
		return getMappingAssuntos().entrySet().iterator();
	}

	private void removeClient(DataOutputStream saida_cliente, String assunto) {
		Iterator<Entry<String, Vector<DataOutputStream>>> assuntos = getIteratorAssuntos();
		while (assuntos.hasNext()) {
			Entry<String, Vector<DataOutputStream>> mapAssunto = assuntos.next();
			if (mapAssunto.getKey().equals(assunto)) {
				mapAssunto.getValue().removeIf(saida -> saida_cliente == saida);
			}
		}
	}

	private void enviarMensagemParaTodosOsClientes(DataOutputStream saida_cliente, String msg_enviada, String assunto) {
		
		Iterator<Entry<String, Vector<DataOutputStream>>> assuntos = getIteratorAssuntos();
		while (assuntos.hasNext()) {
			Entry<String, Vector<DataOutputStream>> mapAssunto = assuntos.next();
			if (mapAssunto.getKey().equals(assunto)) {
				for (DataOutputStream saida : mapAssunto.getValue()) {
					if (saida_cliente != saida) {
						try {
							saida.writeBytes(getAssunto(assunto).concat(": ").concat(msg_enviada));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private String getAssunto(String assunto) {
		switch (assunto) {
		case ENTRETENIMENTO: return "Entretenimento";
		case       ECONOMIA: return "Economia";
		case     TECNOLOGIA: return "Tecnologia";
		default: 	         return "";
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
