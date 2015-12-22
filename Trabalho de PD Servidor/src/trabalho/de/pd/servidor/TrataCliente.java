/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.IOException;
import java.net.Socket;
/**
 *
 * @author Carlos Oliveira
 */
public class TrataCliente extends Thread{

    Servidor servidor = null;
    Socket pedidosSocketCliente = null;
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
        System.out.println("Thread TrataCliente a correr..");
        do {
            try {
                pedidosSocketCliente = servidor.getServerSocketCliente().accept();
                System.out.println("[SERVIDOR] Aceitou conecção " + pedidosSocketCliente.getInetAddress().getHostAddress() +" porto: " +pedidosSocketCliente.getPort());
                servidor.addEscutaCliente(pedidosSocketCliente);
            } catch (IOException ex) {
                System.err.println("[SERVIDOR] A fazer accept pedido socket cliente - " + ex);
            }
        } while (running);
    }    
}
