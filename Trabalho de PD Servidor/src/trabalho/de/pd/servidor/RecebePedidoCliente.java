/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class RecebePedidoCliente extends Thread {

    Pedido pedido;
    Servidor servidor = null;
    Socket socket = null;
    Boolean running = null;

    public RecebePedidoCliente(Servidor servidor, Socket socket) {
        this.servidor = servidor;
        this.socket = socket;
        running = true;
    }

    public void termina() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("[SERVIDOR] Thread RecebePedidoCliente - " + socket.getInetAddress().getHostAddress() + ":"
                + socket.getLocalPort());
        servidor.enviaListaFicheiros(socket);
        
        ObjectInputStream ois=null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(RecebePedidoCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        do {
            try {
                socket.setSoTimeout(0);
                ObjectOutputStream oos = null;
                Object msg = ois.readObject();
                if (msg instanceof Pedido) {
                    pedido = (Pedido) msg;
                    switch (pedido.getTipoPedido()) {
                        case Pedido.DOWNLOAD:
                            servidor.arrancaThreadEnviaFicheiro(socket, pedido);
                            break;
                        case Pedido.UPLOAD:
                            if (servidor.isPrimario()) {
                                ListaFicheiros auxLF = servidor.getListaFicheiros();
                                if (auxLF.hasFicheiro(((Pedido) msg).getNomeFicheiro())) {
                                    pedido.setAceite(false);
                                    oos = new ObjectOutputStream(socket.getOutputStream());
                                    oos.writeObject(pedido);
                                    oos.flush();
                                } else {
                                    pedido.setAceite(true);
                                    oos = new ObjectOutputStream(socket.getOutputStream());
                                    oos.writeObject(pedido);
                                    oos.flush();
                                    for (int i = 0; i < servidor.getArrayPedidoSecundario().size(); i++) {
                                        servidor.getArrayPedidoSecundario().get(i).termina();
                                        servidor.getArrayPedidoSecundario().get(i).join();
                                    }
                                    servidor.arrancaThreadRecebeFicheiro(socket, pedido).join();
                                    for (int i = 0; i < servidor.getArrayPedidoSecundario().size(); i++) {
                                        servidor.getArrayPedidoSecundario().get(i).run();
                                    }
                                }                     
                            }else{
                                servidor.arrancaThreadRecebeFicheiro(socket, pedido).join();
                            }                           
                            break;
                        case Pedido.ELIMINAR:
                            if (servidor.isPrimario()) {
                                ListaFicheiros auxLFR = servidor.getListaFicheiros();
                                if (auxLFR.hasFicheiro(pedido.getNomeFicheiro())) {
                                    for (int i = 0; i < servidor.getArrayPedidoSecundario().size(); i++) {
                                        servidor.getArrayPedidoSecundario().get(i).termina();
                                        servidor.getArrayPedidoSecundario().get(i).join();
                                    }
                                    servidor.arrancaThreadEliminaFicheiro(pedido).join();
                                    servidor.enviaListaFicheiros(socket);
                                    for (int i = 0; i < servidor.getArrayPedidoSecundario().size(); i++) {
                                        servidor.getArrayPedidoSecundario().get(i).run();
                                    }
                                }
                            }else{
                                servidor.arrancaThreadEliminaFicheiro(pedido).join();
                            }
                            break;
                        case Pedido.ACTUALIZACAO:
                            servidor.enviaListaFicheiros(socket);
                            break;
                        default:
                            break;
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout RecebePedidoCliente\n");
            } catch (IOException ex) {
                System.out.println("Socket removido");
                servidor.removeEscutaSocket(socket);
                running = false;
                Logger.getLogger(RecebePedidoCliente.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebePedidoCliente.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(RecebePedidoCliente.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                
            }
        } while (running);
    }
}
