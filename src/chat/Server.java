package chat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
		String msg_recebida,  // lida do cliente
				msg_enviada,  // enviada ao cliente
				nome_cliente, assunto = null;

		Boolean accessChat = true; // Flag para tratar o acesso do cliente ao chat

		try {
			// Cria streams de entrada e saida com o cliente que chegou
			BufferedReader entrada_cliente = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
			DataOutputStream saida_cliente = new DataOutputStream(conexao.getOutputStream());

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
			LocalDateTime dateTime = LocalDateTime.now();
			String dateNow = "";
			// Lê a seleção do assunto pelo cliente
			msg_recebida = entrada_cliente.readLine();
			
			// Verifica qual assunto o cliente selecionou
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
			
			// Apresenta para o cliente qual o tema do chat que ele acessou, a data e hora
			dateTime = LocalDateTime.now();
			dateNow = dateTime.format(formatter);
			msg_enviada = "Voce entrou no chat do tema ".concat(getAssunto(assunto)).concat(" ").concat(dateNow).concat("!\n");
			enviarMensagemParaCliente(saida_cliente, msg_enviada, assunto);
			
			while (msg_recebida != null && !(msg_recebida.trim().equals(""))) {
				
				// Recebe o nome do cliente
				nome_cliente = entrada_cliente.readLine();
				/**
				* Verifica se o loop é o primeiro acesso ao chat,
				* Caso seja envia aos outros clientes, que o usuário acessou o chat 
				*/
				if (accessChat) {
					msg_enviada = ": ".concat(nome_cliente).concat(" entrou no chat!").concat("\n");
					enviarMensagemParaTodosOsClientes(saida_cliente, msg_enviada, assunto);
					accessChat = !accessChat;
				}
				
				// Recebe a mensagem do cliente
				msg_recebida = entrada_cliente.readLine();

				/**
				 * Verifica se enviou FIM para sair,
				 * Caso sim, envia aos outros clientes que este cliente está saindo do chat
				 */
				if (msg_recebida.startsWith(FIM)) {
					msg_enviada = nome_cliente.concat(" saiu do chat!").concat("\n");
					enviarMensagemParaTodosOsClientes(saida_cliente, msg_enviada, assunto);
					break;
				}

				// Recupera a hora do servidor
				formatter = DateTimeFormatter.ofPattern("HH:mm");
				dateTime = LocalDateTime.now();
				dateNow = dateTime.format(formatter);
				
				// Monta mensagem de retorno e envia para os clientes
				msg_enviada = " <".concat(nome_cliente).concat(">: ").concat(msg_recebida).concat(" (").concat(dateNow)
						.concat(")").concat("\n");
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

	/**
	 * Método criado para criar uma map dos vetores de assuntos
	 * @return Map<Key, Vector>
	 */
	private Map<String, Vector<DataOutputStream>> getMappingAssuntos() {
		Map<String, Vector<DataOutputStream>> assuntos = new HashMap<>();
		assuntos.put(ENTRETENIMENTO, VEntretenimento);
		assuntos.put(ECONOMIA, VEconomia);
		assuntos.put(TECNOLOGIA, VTecnologia);
		return assuntos;
	}

	/**
	 * Método criado para retornar o Iterator do Map de assuntos
	 * @return Iterator<Map<Key, Vector>>
	 */
	private Iterator<Entry<String, Vector<DataOutputStream>>> getIteratorAssuntos() {
		// Retorna o iterator
		return getMappingAssuntos().entrySet().iterator();
	}

	private void removeClient(DataOutputStream saida_cliente, String assunto) {
		// Recupera o Iterator(Lista) de assuntos
		Iterator<Entry<String, Vector<DataOutputStream>>> assuntos = getIteratorAssuntos();
		// Repete o bloco enquanto no Iterator(Lista) existir assunto
		while (assuntos.hasNext()) {
			// Recupera cada valor do Iterator(Lista) 
			Entry<String, Vector<DataOutputStream>> vetor = assuntos.next();
			// Verifica se o Key do map de assuntos é o mesmo do assunto selecionado pelo cliente
			if (vetor.getKey().equals(assunto)) {
				// Percorre o vetor de assunto contendo os clientes e remove o cliente informado(saida_cliente)
				vetor.getValue().removeIf(saida -> saida_cliente == saida);
			}
		}
	}

	private void enviarMensagemParaTodosOsClientes(DataOutputStream cliente_enviando, String msg_enviada,
			String assunto) {
		// Recupera o Iterator(Lista) de assuntos
		Iterator<Entry<String, Vector<DataOutputStream>>> assuntos = getIteratorAssuntos();
		// Repete o bloco enquanto no Iterator(Lista) existir assunto
		while (assuntos.hasNext()) {
			// Recupera cada valor do Iterator(Lista)
			Entry<String, Vector<DataOutputStream>> vetor = assuntos.next();
			// Verifica se o Key do map de assuntos é o mesmo do assunto selecionado pelo cliente
			if (vetor.getKey().equals(assunto)) {
				// Percorre o vetor de assunto contendo os clientes
				for (DataOutputStream cliente : vetor.getValue()) {
					/**
					 * Verifica se o cliente que está no vetor de assuntos 
					 * é diferente do cliente que está enviando a mensagem 
					 */
					if (cliente_enviando != cliente) {
						try {
							// Caso seja, envia a mensagem para o cliente do vetor
							cliente.writeBytes(getAssunto(assunto).concat(msg_enviada));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void enviarMensagemParaCliente(DataOutputStream cliente_enviando, String msg_enviada, String assunto) {
		// Recupera o Iterator(Lista) de assuntos
		Iterator<Entry<String, Vector<DataOutputStream>>> assuntos = getIteratorAssuntos();
		// Repete o bloco enquanto no Iterator(Lista) existir assunto
		while (assuntos.hasNext()) {
			// Recupera cada valor do Iterator(Lista)
			Entry<String, Vector<DataOutputStream>> mapAssunto = assuntos.next();
			// Verifica se o Key do map de assuntos é o mesmo do assunto selecionado pelo cliente
			if (mapAssunto.getKey().equals(assunto)) {
				// Percorre o vetor de assunto contendo os clientes
				for (DataOutputStream cliente : mapAssunto.getValue()) {
					/**
					 * Verifica se o cliente que está no vetor de assuntos 
					 * é mesmo cliente que está enviando a mensagem 
					 */
					if (cliente_enviando == cliente) {
						try {
							// Caso seja, envia a mensagem para o cliente do vetor
							cliente.writeBytes(msg_enviada);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	/**
	 * Método criado para recperar o texto do assunto
	 * @param assunto
	 * @return nome assunto 
	 */
	private String getAssunto(String assunto) {
		switch (assunto) {
		case ENTRETENIMENTO:
			return "Entretenimento";
		case ECONOMIA:
			return "Economia";
		case TECNOLOGIA:
			return "Tecnologia";
		default:
			return "";
		}
	}

	public static void main(String[] args) throws IOException {

		// Cria socket de comunicação com os clientes na porta 8657
		ServerSocket servidor = new ServerSocket(8567);
		System.out.println("Esperando cliente se conectar...");

		// Espera mensagem de algum cliente e trata
		while (true) {
			// Espera conexão de algum cliente
			Socket client = servidor.accept();
			// Cria e inicia a thread
			Thread t = new Server(client);
			System.out.println("Cliente conectado!");
			t.start();
		}
	}
}
