/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class HeartbeatsRecebe extends Thread{

    Servidor servidor=null;
    public static final int MAX_SIZE=10000;
    int contador = 0;
    protected DatagramPacket packet = null;
    protected boolean running = false;
    public InetAddress IpPrimario = null;
    public int PortoPrimario = 0;

    public HeartbeatsRecebe(Servidor servidor) throws SocketException {
        this.servidor=servidor;
        running = true;
    }

    public InetAddress getIpPrimario() {
        return IpPrimario;
    }

    public int getPortoPrimario() {
        return PortoPrimario;
    }

    public void termina() {
        running = false;
    }

    @Override
    public void run()  {   //falta fazer quando ha mais do que 1 primario
        System.out.println("Thread HeartbeatsRecebe a correr...");
        do {
            try {
                System.out.println("Recebe esta a correr");
                packet=new DatagramPacket(new byte[MAX_SIZE],MAX_SIZE);
                servidor.getMulticastSocket().receive(packet);
                
                ObjectInputStream recv = new ObjectInputStream(new ByteArrayInputStream(packet.getData(),0,packet.getLength()));
                
                boolean msg =(boolean) recv.readObject();
                if (msg) {
                    if (!servidor.isPrimario()) {
                        IpPrimario = packet.getAddress();
                        PortoPrimario = packet.getPort();
                        servidor.conectaServidorPrimario(IpPrimario, PortoPrimario);
                        servidor.recebeListaFicheiros();
                    }else{}
                    //falta fazer quando ha mais do que 1 primario
                }
            } catch (NumberFormatException e) {
                System.out.println("O porto de escuta deve ser um inteiro positivo.");
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
            } catch (SocketTimeoutException e) {
                
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }finally {
                if (servidor.isPrimario()) {
                    contador=0;
                }else{
                    if(contador==3){
                        servidor.setPrimario(true);
                    }else{
                       contador++;
                    }                   
                }
            }
        } while (running);
        System.out.println("Acabou HearbeatsRecebe...");
    }
}
