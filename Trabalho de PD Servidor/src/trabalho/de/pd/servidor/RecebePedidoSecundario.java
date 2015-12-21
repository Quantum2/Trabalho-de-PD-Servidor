/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class RecebePedidoSecundario extends Thread {
    
    Pedido pedido;
    Servidor servidor = null;
    Socket socket = null;
    Boolean running = null;
    
    public RecebePedidoSecundario(Servidor servidor,Socket socket) {
        this.servidor = servidor;
        this.socket = socket;
        running = true;
    }
    
    public void termina() {
        running = false;
    }
    
    @Override
    public void run() {
        System.out.println("Thread RecebePedido a correr...."); //falta diferenciar o primario e o secundario
        do {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                pedido = (Pedido) ois.readObject();
                switch (pedido.getTipoPedido()) {
                    case Pedido.UPLOAD:
                        servidor.arrancaThreadRecebeFicheiro(pedido.getSocketCliente(),pedido);
                        break;           
                    case Pedido.ELIMINAR:
                        servidor.arrancaThreadEliminaFicheiro(pedido);
                        break;
                    case Pedido.ACTUALIZACAO:
                        servidor.enviaListaFicheiros(socket);
                        break;
                    default:
                        break;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout RecebePedidoSecundario\n");
            } catch (IOException ex) {
                System.out.println("Socket removido");
                servidor.removeEscutaSocket(socket);
                running=false;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebePedidoCliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (running);
    }
}
