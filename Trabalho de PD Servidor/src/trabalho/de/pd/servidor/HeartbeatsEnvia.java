/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static trabalho.de.pd.servidor.Servidor.MAX_SIZE;

/**
 *
 * @author ASUS
 */
public class HeartbeatsEnvia extends Thread {

    Servidor servidor=null;
    private DatagramPacket packet = null;
    protected boolean running = false;
    public HeartbeatsRecebe heartRECV=null;

    public HeartbeatsEnvia(Servidor servidor) throws SocketException {
        this.servidor=servidor;
        running = true;
    }

    public void termina() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("Thread HearbeatEnvia a correr.....");
        do{
            try {
                HeartBeat msg=new HeartBeat(servidor.getTcpPort(),servidor.isPrimario(),InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
                ByteArrayOutputStream byteout = new ByteArrayOutputStream();
                ObjectOutputStream send = new ObjectOutputStream(byteout);
                send.writeObject(msg);
                send.flush();
                
                packet=new DatagramPacket(byteout.toByteArray(),byteout.size(),servidor.getGroup(),7000);
                servidor.getMulticastSocket().send(packet); //teste
                send.close();
                sleep(5000);
            } catch (NumberFormatException e) {
                System.out.println("O porto de escuta deve ser um inteiro positivo.");
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            } catch (InterruptedException ex) {
                Logger.getLogger(HeartbeatsEnvia.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

            }
        }while(running);
        System.out.println("Acabou HeartbeatsEnvia...");
    }
    
    

}
