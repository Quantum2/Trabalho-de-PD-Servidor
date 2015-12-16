/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class RecebeFicheiro extends Thread {

    Servidor servidor = null;
    Socket socketPedido = null;

    public RecebeFicheiro(Servidor servidor, Socket socketPedido) {
        this.servidor = servidor;
        this.socketPedido = socketPedido;
    }

    @Override
    public void run() {
        FileOutputStream fileOut = null;
        try {
            if (servidor.isPrimario()) {
                int nbytes;
                byte[] filechunck = new byte[Servidor.MAX_SIZE];
                InputStream inputFicheiro = socketPedido.getInputStream();
                fileOut = new FileOutputStream(servidor.diretoria);
                while ((nbytes = inputFicheiro.read(filechunck)) > 0) {
                    fileOut.write(filechunck, 0, nbytes);
                }
                //manda os secundarios fazer
                
            }else{
                ObjectOutputStream oos =new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                Pedido p=new Pedido("",2,false);
                oos.writeObject(p);
                oos.flush();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EnviaFicheiro.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnviaFicheiro.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileOut.close();
            } catch (IOException ex) {
                Logger.getLogger(EnviaFicheiro.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
