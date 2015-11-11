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
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */

public class TrabalhoDePDServidor implements Serializable {
    public static final int MAX_SIZE=256;
    public static final String TIME_REQUEST="";
    
    public Runnable HeartbeatsEnviaPrimario=null;
    public Runnable Heartbeats=null;
    
    private DatagramSocket socket;
    private DatagramPacket packet;
    Object receiveMSG,sendMSG;
    private boolean debug,Primario;
    /**
     * @param args the command line arguments
     */
    public void main(String[] args) throws SocketException {
        // TODO code application logic here
        socket=null;
        packet=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE);
        socket=new DatagramSocket(7000);
        Primario=false;
        try{
            constroiThreads();
            run();
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
        HeartbeatsEnviaPrimario=new Runnable(){          
            @Override
            public void run() {
                try{
                ByteArrayOutputStream byteout = new ByteArrayOutputStream(MAX_SIZE);
                ObjectOutputStream send = new ObjectOutputStream(byteout);
                    send.writeObject(sendMSG);
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
            }
        };
        
        
    }
    
    public void run() throws IOException,InterruptedException
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
