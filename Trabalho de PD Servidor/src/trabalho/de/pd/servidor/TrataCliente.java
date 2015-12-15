/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Carlos Oliveira
 */
public class TrataCliente extends Thread{

    Servidor servidor = null;
    Socket pedidosSocketTCP = null;
    Boolean running = null;
    
    public TrataCliente(Servidor servidor) {
        this.servidor = servidor;
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
                Pedido pedido = null;
                pedidosSocketTCP = servidor.getServerSocketTCP().accept();
                System.out.println("[SERVIDOR] Aceitou conecção " + pedidosSocketTCP.getInetAddress().getHostAddress());
                ObjectInputStream ois = new ObjectInputStream(pedidosSocketTCP.getInputStream());
                pedido = (Pedido) ois.readObject();
                if (pedido.getTipoPedido() == Pedido.DOWNLOAD) {
                    servidor.arrancaThreadEnviaFicheiro(pedidosSocketTCP, pedido);
                } else {
                    if (pedido.getTipoPedido() == Pedido.UPLOAD) {
                        servidor.arrancaThreadRecebeFicheiro(pedidosSocketTCP);
                    } else {
                        if (pedido.getTipoPedido() == Pedido.ELIMINAR) {
                            servidor.arrancaThreadEliminaFicheiro(pedido);
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("RecebePedido Timeout");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebePedido.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (running);
    }
    
}
