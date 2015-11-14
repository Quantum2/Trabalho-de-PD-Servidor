/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author ASUS
 */
public class RecebeActualizacaoTCP extends Thread{
    
    private Socket RecebeSocketTCP=null;
    boolean Actualizado=false;
    
    public RecebeActualizacaoTCP()
    {
        
    }
    
    @Override
    public void run() {                     //tem que se ver as melhores formas de mandar e receber ficheiros
        OutputStream oss = SocketComPrimario.getOutputStream();
        sendobject = new ObjectOutputStream(oss);
        sendobject.writeBoolean(Actualizado);
        sendobject.close();
        do {
            int tamanho;
            byte[] bytes = new byte[MAX_SIZE];
            InputStream in = null;
            OutputStream ou = null;
            in = SocketComSecundario.getInputStream();
            ou = new FileOutputStream(diretoria);
            while ((tamanho = in.read(bytes)) > 0) {
                ou.write(bytes, 0, tamanho);
                Actualizado = true;
            }
        } while (Actualizado);
        ou.close();
        in.close();
    }
}
