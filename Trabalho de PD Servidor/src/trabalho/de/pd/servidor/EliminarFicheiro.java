/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class EliminarFicheiro extends Thread {

    Servidor servidor = null;
    Socket socketPedido = null;
    Pedido pedido=null;

    public EliminarFicheiro(Servidor servidor, Pedido pedido) {
        this.servidor = servidor;
        this.pedido = pedido;
    }

    @Override
    public void run() {
        if (servidor.isPrimario()) {
            String dir = (servidor.getDiretoria());
            File file = new File(dir);
            file.delete();
        }else{
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                oos.writeObject(pedido);
            } catch (IOException ex) {
                Logger.getLogger(EliminarFicheiro.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
}
