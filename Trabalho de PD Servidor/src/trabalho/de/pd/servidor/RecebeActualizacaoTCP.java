/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class RecebeActualizacaoTCP extends Thread {

    public static final int MAX_SIZE=256;
    
    Servidor servidor = null;
    
    InputStream inputStreamFicheiro = null;

    public RecebeActualizacaoTCP(Servidor servidor) throws IOException {
        this.servidor = servidor;
        inputStreamFicheiro = servidor.primarioSocketTCP.getInputStream();
    }

    @Override
    public void run() {                     //tem que se ver as melhores formas de mandar e receber ficheiros
        try {
            int nbytes;
            byte [] filechunck = new byte [MAX_SIZE];
            FileOutputStream fOut = new FileOutputStream(servidor.diretoria);
            while((nbytes=inputStreamFicheiro.read(filechunck))>0) {
                fOut.write(filechunck,0,nbytes);
            }
        } catch (IOException ex) {
            Logger.getLogger(RecebeActualizacaoTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
