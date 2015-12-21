/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class EnviaFicheiro extends Thread {
    
    Servidor servidor=null;
    Socket socketPedido=null;
    String nomeFicheiro=null;
    
    public EnviaFicheiro(Servidor servidor,Socket socketPedido,String nomeFicheiro) {
        this.servidor = servidor;
        this.socketPedido=socketPedido;
        this.nomeFicheiro=nomeFicheiro;
    }
    
    @Override
    public void run() {
        FileInputStream fileIn = null;
        try {
            int nbytes;
            byte [] filechunck = new byte [Servidor.MAX_SIZE];
            OutputStream outputFicheiro = socketPedido.getOutputStream();
            fileIn = new FileInputStream(servidor.diretoria);
            while((nbytes=fileIn.read(filechunck))>0) {
                outputFicheiro.write(filechunck);
                outputFicheiro.flush();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EnviaFicheiro.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnviaFicheiro.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
