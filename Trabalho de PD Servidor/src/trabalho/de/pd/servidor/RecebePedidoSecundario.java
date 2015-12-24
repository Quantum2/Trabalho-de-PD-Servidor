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

    public RecebePedidoSecundario(Servidor servidor, Socket socket) {
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
                socket.setSoTimeout(1000);
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Object msg = ois.readObject();
                if (msg instanceof Pedido) {
                    pedido = (Pedido) msg;
                    switch (pedido.getTipoPedido()) {
                        case Pedido.DOWNLOAD:
                            servidor.arrancaThreadEnviaFicheiro(socket, pedido);
                            break;
                        case Pedido.UPLOAD:
                            ListaFicheiros auxLF = servidor.getListaFicheiros();
                            if (auxLF.hasFicheiro(pedido.getNomeFicheiro())) {
                                pedido.setAceite(false);
                                ObjectOutputStream oos = new ObjectOutputStream(pedido.getSocketCliente().getOutputStream());
                                oos.writeObject(pedido);
                                oos.flush();
                            } else {
                                pedido.setAceite(true);
                                ObjectOutputStream oos = new ObjectOutputStream(pedido.getSocketCliente().getOutputStream());
                                oos.writeObject(pedido);
                                oos.flush();
                                servidor.arrancaThreadRecebeFicheiro(pedido.getSocketCliente(), pedido).join();
                            }
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
                }
            } catch (SocketTimeoutException e) {
                System.out.print("Timeout RecebePedidoSecundario\n");
            } catch (IOException ex) {
                System.out.println("Socket removido");
                 servidor.removeEscutaSocket(socket);
                 running=false;
                Logger.getLogger(RecebePedidoCliente.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebePedidoCliente.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(RecebePedidoSecundario.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (running);
    }
}
