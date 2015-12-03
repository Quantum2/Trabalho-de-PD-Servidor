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
 * @author ASUS
 */
public class RecebePedido extends Thread {
    
    Servidor servidor = null;
    Socket pedidosSocketTCP = null;
    Boolean running = null;
    
    public RecebePedido(Servidor servidor) {
        this.servidor = servidor;
        running = true;
    }
    
    @Override
    public void run() {
        do {
            try {
                Pedido pedido = null;
                pedidosSocketTCP = servidor.getServerSocketTCP().accept();
                ObjectInputStream ois = new ObjectInputStream(pedidosSocketTCP.getInputStream());
                pedido = (Pedido) ois.readObject();
                if (pedido.getTipoPedido() == 1) {
                    servidor.arrancaThreadEnviaFicheiro(pedidosSocketTCP, pedido);
                } else {
                    if (pedido.getTipoPedido() == 2) {
                        servidor.arrancaThreadRecebeFicheiro(pedidosSocketTCP);
                    } else {
                        if (pedido.getTipoPedido() == 3) {
                            servidor.arrancaThreadEliminaFicheiro(pedido);
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(RecebePedido.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebePedido.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (running);
    }
}
