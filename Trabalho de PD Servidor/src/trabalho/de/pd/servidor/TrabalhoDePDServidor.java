/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author Rafael
 */

public class TrabalhoDePDServidor {
    public static final int MAX_SIZE=256;
    public static final String TIME_REQUEST="";
    
    private DatagramSocket socket;
    private DatagramPacket packet;
    String receiveMSG,sendMSG;
    private boolean debug;
    /**
     * @param args the command line arguments
     */
    public void main(String[] args) throws SocketException{
        // TODO code application logic here
        socket=null;
        packet=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE);
        socket=new DatagramSocket(7000);       
        run();
        
        
    }
    
    public void run() throws IOException
    {     
        if(socket==null)
        {
            return;
        }
        
        if(debug)
        {
            System.out.println("UDP do server iniciado...");
        }
        
        while(true)
        {
            
        }
    }
    
    public String receive() throws IOException
    {
        String recv;
        
        
    }
    
}
