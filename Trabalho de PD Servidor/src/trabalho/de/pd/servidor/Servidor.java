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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author ASUS
 */
public class Servidor implements Serializable{
    public static final int MAX_SIZE=1000;
    public static final String TIME_REQUEST="";
    public InetAddress group;
    public int port;
    
    public Runnable HeartbeatsEnvia=null;
    public Runnable HeartbeatsRecebe=null;
    
    protected MulticastSocket socket=null;
    private DatagramPacket packet=null;
    Object receiveMSG,sendMSG;
    private boolean debug,Primario;
    
    public Servidor(String diretoria,int Porto) throws UnknownHostException, IOException, InterruptedException 
    {       
        group=InetAddress.getByName("225.15.15.15");
        packet=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE);
        port=Porto;
        socket = new MulticastSocket(port);
        socket.joinGroup(group);
        Primario=false;
        debug=true;
        
        
        try{
            constroiThreads();
            começa();
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(SocketException e){
            System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t"+e);
        }catch(IOException e){
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t"+e);
        }catch(InterruptedException e){
            System.out.println("Ocorreu um erro no sleep");
        }finally{
                closeSocket();
        }
    }
    
    public void constroiThreads() throws IOException,InterruptedException
    {
        //Socket toClientSocket;
        HeartbeatsEnvia = new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        ByteArrayOutputStream byteout = new ByteArrayOutputStream(MAX_SIZE);
                        ObjectOutputStream send = new ObjectOutputStream(byteout);
                        send.writeObject(Primario);
                        packet.setData(byteout.toByteArray());
                        packet.setLength(byteout.size());
                        socket.send(packet);
                        Thread.sleep(5000);
                    } catch (NumberFormatException e) {
                        System.out.println("O porto de escuta deve ser um inteiro positivo.");
                    } catch (SocketException e) {
                        System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
                    } catch (IOException e) {
                        System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
                    } catch (InterruptedException e) {
                        System.out.println("Ocorreu um erro no sleep");
                    } finally {
                        closeSocket();
                    }
                } while (debug);
            }
        };
        
        HeartbeatsRecebe = new Runnable(){

            @Override
            public void run() {
                
            }
            
        };
        
    }
    
    public void começa() throws IOException,InterruptedException
    {     
        debug=true;
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
            /*receiveMSG=receive();    
            if(receiveMSG==null)
            {
                continue;
            }
            */
            //send();
            Thread.sleep(5000);          
        }
    }
    
    public Object receive() throws IOException                
    {
        ObjectInputStream recv;
        
        if(socket==null)
        {
            return null;
        }
        
        socket.receive(packet);
        recv=new ObjectInputStream(new ByteArrayInputStream(packet.getData()));                       //ClassCastException | ClassNotFoundException 
        
        if(debug)
        {
            System.out.println("Recevido do IP:"+packet.getAddress().getHostAddress()+" e de Porto:"+packet.getPort());
        }
        return recv;
    }
    
    
    
    
    public void closeSocket()
    {
        if(socket!=null)
            socket.close();
    }
}
