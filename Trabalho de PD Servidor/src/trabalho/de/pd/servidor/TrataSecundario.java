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
public class TrataSecundario extends Thread{

    Servidor servidor = null;
    Socket pedidosSocketSecundario = null;
    Boolean running = null;
    
    public TrataSecundario(Servidor servidor) {
        this.servidor = servidor;
        running = true;
    }
    
    public void termina() {
        running = false;
    }
    
    @Override
    public void run() {
        System.out.println("Thread TrataSecundario a correr..");
        do {
            try {
                pedidosSocketSecundario = servidor.getServerSocketSecundario().accept();
                System.out.println("[SERVIDOR] Aceitou conecção " + pedidosSocketSecundario.getInetAddress().getHostAddress()+" porto: " +pedidosSocketSecundario.getPort());
                servidor.addEscutaSecundario(pedidosSocketSecundario);
            } catch (IOException ex) {
                System.err.println("[SERVIDOR] A fazer accept pedido socket servidor secundário - " + ex);
            }
        } while (running);
    }
}
