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
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class EliminarFicheiro extends Thread{
    
        
    Servidor servidor=null;
    Socket socketPedido=null;
    String nomeFicheiro=null;
    
    public EliminarFicheiro(Servidor servidor,String nomeFicheiro) {
        this.servidor = servidor;
        this.nomeFicheiro=nomeFicheiro;
    }
    
    @Override
    public void run() {
        String dir=(servidor.getDiretoria());
        File file=new File(dir);
        file.delete();
    }
}
