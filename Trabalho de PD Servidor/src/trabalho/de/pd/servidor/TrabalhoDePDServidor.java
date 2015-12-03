/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */

public class TrabalhoDePDServidor implements Serializable {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException, IOException, UnknownHostException, InterruptedException {
        // TODO code application logic here
        /*boolean flg=false;
        int TCPport=5000;
        do{
            try{
                Socket s=new Socket("127.0.0.1",TCPport);
                s.close();
                flg=true;
            }catch(Exception e){
                TCPport++;
            }
        }while(!flg);*/
        
        String dir=(""+System.getProperty("user.dir")+"\\ServerFicheiros");
        if(!new File(dir).exists()){
            new File(dir).mkdir();
        }
        Servidor s=new Servidor(dir,5000); //port 0 para qualquer uma
        s.começa();
    }
}
