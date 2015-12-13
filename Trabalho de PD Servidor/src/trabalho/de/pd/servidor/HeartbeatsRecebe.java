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
    public InetAddress ipPrimario = null;
    public int portoPrimario = 0;

    long lastPrimaryBeat;
    
    public HeartbeatsRecebe(Servidor servidor) throws SocketException {
        this.servidor=servidor;
        running = true;
    }

    public InetAddress getIpPrimario() {
        return ipPrimario;
    }

    public int getPortoPrimario() {
        return portoPrimario;
    }

    public void termina() {
        running = false;
    }

    @Override
    public void run()  {   //falta fazer quando ha mais do que 1 primario && o fazer o tempo de 5 segundos a espera e nao com o timeout
        System.out.println("Thread HeartbeatsRecebe a correr...");
        HeartBeat msg=null;
        do {
            try {
                msg = null;
                packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                servidor.getMulticastSocket().receive(packet);
                ObjectInputStream recv = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                msg = (HeartBeat) recv.readObject();
                System.out.println("[SERVIDOR] Received Heartbeat " + packet.getAddress().getHostAddress() + " Tipo:" + msg.getPrimario());
                if (msg.getPrimario() && servidor.isPrimario()) {
                    //compara ip dos dois primarios
                    if (servidor.getServerSocketTCP().getInetAddress().getHostAddress().compareTo(packet.getAddress().getHostAddress()) > 0) {
                        servidor.setPrimario(false);
                    }
                }
                if (msg.getPrimario()) {
                    lastPrimaryBeat = System.currentTimeMillis();
                }
            } catch (NumberFormatException e) {
                System.out.println("O porto de escuta deve ser um inteiro positivo.");
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t" + e);
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout\n\t");
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    /*
                    if(msg==null || msg.getPrimario()==false){
                    servidor.setTFinal(System.currentTimeMillis());
                    if(((servidor.getTFinal()-servidor.getTStart())/1000.0)>5)
                    contador++;
                    }
                    if(servidor.isPrimario())
                    contador=0;
                    */
                    sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(HeartbeatsRecebe.class.getName()).log(Level.SEVERE, null, ex);
                }
                long auxTempo = System.currentTimeMillis();
                long resultado = auxTempo-lastPrimaryBeat;
                if (resultado / 1000.0 > 15) {
                    break;
                }
            }
        } while (contador!=3 && running);
    }
}
