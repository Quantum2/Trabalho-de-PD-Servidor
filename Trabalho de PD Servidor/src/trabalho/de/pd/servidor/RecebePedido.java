/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class RecebePedido extends Thread {
    
    Pedido pedido;
    Servidor servidor = null;
    Socket socket = null;
    Boolean running = null;
    
    public RecebePedido(Servidor servidor,Socket socket) {
        this.servidor = servidor;
        this.socket = socket;
        running = true;
    }
    
    public void termina() {
        running = false;
    }
    
    @Override
    public void run() {
        System.out.println("Thread RecebePedido a correr...."); //falta diferenciar o primario e o secundario
        do {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                pedido = (Pedido) ois.readObject();
                switch (pedido.getTipoPedido()) {
                    case Pedido.DOWNLOAD:
                        if(pedido.getSocketCliente()==null)
                            servidor.arrancaThreadEnviaFicheiro(socket, pedido);
                        else
                            servidor.arrancaThreadEnviaFicheiro(pedido, pedido);
                        break;
                    case Pedido.UPLOAD:
                        if(pedido.getSocketCliente()==null)
                            servidor.arrancaThreadRecebeFicheiro(socket);
                        else
                            servidor.arrancaThreadRecebeFicheiro(pedido.getSocketCliente());
                        break;           
                    case Pedido.ELIMINAR:
                        servidor.arrancaThreadEliminaFicheiro(pedido);
                        break;
                    case Pedido.ACTUALIZACAO:
                        ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
                        ListaFicheiros lsend=new ListaFicheiros();
                        File directory = new File(System.getProperty("user.dir")+"\\ServerFicheiros");
                        File[] fList = directory.listFiles();
                        for(int i=0;i<fList.length;i++){
                            lsend.addFicheiro(new Ficheiro(fList[i].getName(),fList[i].getTotalSpace(),0));
                        }
                        oos.writeObject(lsend);
                        break;
                    default:
                        break;
                }
            } catch (IOException ex) {
                System.out.println("RecebePedido Timeout");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebePedido.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (running);
    }
}
