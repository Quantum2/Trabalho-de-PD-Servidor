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
import java.net.MulticastSocket;
import java.net.SocketException;
import static trabalho.de.pd.servidor.Servidor.MAX_SIZE;

/**
 *
 * @author ASUS
 */
public class HeartbeatsEnvia extends Thread {

    private boolean Primario=false;
    private DatagramSocket SocketComDiretoria = null;
    private DatagramPacket packet = null;
    protected boolean running = false;
    public HeartbeatsRecebe heartRECV=null;

    public HeartbeatsEnvia(DatagramPacket SendpacketUDP, HeartbeatsRecebe heartRECV) throws SocketException {
        SocketComDiretoria = new DatagramSocket();
        packet = SendpacketUDP;
        this.heartRECV=heartRECV;
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
                System.out.println("Envia esta a correr");
                synchronized (heartRECV) {
                    heartRECV.wait();
                    Primario=heartRECV.getPrimario();
                }
                ByteArrayOutputStream byteout = new ByteArrayOutputStream();
                ObjectOutputStream send = new ObjectOutputStream(byteout);
                send.writeObject(Primario);
                send.flush();

                packet.setData(byteout.toByteArray());
                packet.setLength(byteout.size());
                SocketComDiretoria.send(packet); //teste
                send.close();
                System.out.println("Envia heartbeat...");
            } catch (NumberFormatException e) {
                System.out.println("O porto de escuta deve ser um inteiro positivo.");
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            } catch (InterruptedException e) {
                System.out.println("Ocorreu um erro no sleep");
            } finally {

            }
        }while(running);
        System.out.println("Acabou HeartbeatsEnvia...");
    }
    
    

}
