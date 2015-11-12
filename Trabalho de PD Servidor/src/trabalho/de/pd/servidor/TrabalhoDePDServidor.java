/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        Servidor s=new Servidor("",7000);
    }
}
