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
    
    Servidor servidor=null;
    
    public RecebePedido(Servidor servidor){
        this.servidor=servidor;
    }

    @Override
    public void run() {
        try {
            Socket socketPedido = servidor.serverSocketTCP.accept();
            ObjectInputStream ois = new ObjectInputStream(socketPedido.getInputStream());
            Pedido pedido = (Pedido) ois.readObject();
            if (pedido.getTipoPedido() == Pedido.DOWNLOAD) {
                EnviaFicheiro envia = new EnviaFicheiro(servidor,socketPedido,pedido.getNomeFicheiro());
            } else if (pedido.getTipoPedido() == Pedido.ELIMINAR) {
                EliminaFicheiro elimina = new EliminaFicheiro();
            }
        } catch (IOException ex) {
            Logger.getLogger(RecebePedido.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RecebePedido.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
