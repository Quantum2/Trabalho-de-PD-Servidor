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
import static java.lang.Thread.sleep;
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
        
        if(args.length!=2){
             System.out.println("Argumentos errados");
        }else{
            Servidor s=new Servidor(args[0],Integer.parseInt(args[1])); //port 0 para qualquer uma
            s.começa();          
        }
    }
}
