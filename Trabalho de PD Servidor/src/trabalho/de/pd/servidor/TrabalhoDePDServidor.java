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
    private boolean debug,Primario;
    /**
     * @param args the command line arguments
     */
    public void main(String[] args) throws SocketException{
        // TODO code application logic here
        socket=null;
        packet=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE);
        socket=new DatagramSocket(7000);
        Primario=false;
        this.debug=debug;
        try{
            run();
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(SocketException e){
            System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t"+e);
        }catch(IOException e){
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t"+e);
        }finally{
                closeSocket();
        }
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
            receiveMSG=receive();    
            if(receiveMSG==null)
            {
                continue;
            }
            send();
        }
    }
    
    public String receive() throws IOException                
    {
        String recv;
        
        if(socket==null)
        {
            return null;
        }
        
        socket.receive(packet);
        recv=new String(packet.getData(),0,packet.getLength());
        if(debug)
        {
            System.out.println("Recevido:"+recv+" do IP:"+packet.getAddress().getHostAddress()+" e de Porto:"+packet.getPort());
        }
        return recv;
    }
    
    public void send() throws IOException
    {
        sendMSG=("Enviado do servidor");
        packet.setData(sendMSG.getBytes());
        socket.send(packet);
    }
    
    
    public void closeSocket()
    {
        if(socket!=null)
            socket.close();
    }
}
