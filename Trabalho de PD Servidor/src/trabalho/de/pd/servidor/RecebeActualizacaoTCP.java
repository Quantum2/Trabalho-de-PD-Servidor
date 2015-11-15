/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.FileInputStream;
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
    public ObjectOutputStream sendobject = null;

    private Socket RecebeSocketTCP = null;
    boolean Actualizado = false;
    public InetAddress IpPrimario = null;
    public int PortoPrimario = 0;
    public String Diretoria=null;

    public RecebeActualizacaoTCP(InetAddress Ip, int porto,String Diretoria) throws IOException {
        this.IpPrimario = Ip;
        this.PortoPrimario = porto;
        this.Diretoria=Diretoria;
        RecebeSocketTCP = new Socket(IpPrimario, PortoPrimario);
    }

    @Override
    public void run() {                     //tem que se ver as melhores formas de mandar e receber ficheiros
        try {
            do {              
                sendobject = new ObjectOutputStream(RecebeSocketTCP.getOutputStream());
                sendobject.writeBoolean(Actualizado);
                sendobject.flush();
                sendobject.close();
            
                int tamanho;
                byte[] bytes = new byte[MAX_SIZE];
                InputStream in = RecebeSocketTCP.getInputStream();
                OutputStream ou = new FileOutputStream(Diretoria);
                while ((tamanho = in.read(bytes)) > 0) {
                    ou.write(bytes, 0, tamanho);
                    Actualizado = true;
                }
                ou.close();
                in.close();
            } while (Actualizado);
            
        } catch (IOException ex) {
            Logger.getLogger(RecebeActualizacaoTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
