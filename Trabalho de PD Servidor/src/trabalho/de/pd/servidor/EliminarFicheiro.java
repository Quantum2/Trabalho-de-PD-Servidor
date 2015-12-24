/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class EliminarFicheiro extends Thread {

    Servidor servidor = null;
    Socket socketPedido = null;
    Pedido pedido = null;

    public EliminarFicheiro(Servidor servidor, Pedido pedido) {
        this.servidor = servidor;
        this.pedido = pedido;
    }

    @Override
    public void run() {
        Boolean flg = true;
        if (servidor.isPrimario()) {
            try {
                ArrayList<Socket> socketsSecundarios = servidor.getSocketSecundarios();
                for (int i = 0; i < socketsSecundarios.size(); i++) {
                    Socket socket = socketsSecundarios.get(i);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(pedido);
                    oos.flush();
                }
                for (int i = 0; i < socketsSecundarios.size(); i++) {   //da aqui problemas, as vezes nao aceita o header, outras apanha um objeto diferente, outra o secundario nao se encontra no array
                    ObjectInputStream iss = new ObjectInputStream(socketsSecundarios.get(i).getInputStream());
                    if (!iss.readBoolean()) {
                        flg = false;
                        break;
                    }
                }
                
                if (flg) {
                    String dir = (servidor.getDiretoria() + "\\" + pedido.getNomeFicheiro());
                    File file = new File(dir);
                    System.gc();
                    System.out.println("Elimina ficheiro: "+file.delete());
                    for (int i = 0; i < socketsSecundarios.size(); i++) {
                        ObjectOutputStream oos = new ObjectOutputStream(socketsSecundarios.get(i).getOutputStream());
                        oos.writeBoolean((Boolean)flg);
                        oos.flush();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(EliminarFicheiro.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                servidor.actualizaListaFicheiros();
            }
        } else {
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                oos.writeObject(pedido);
                oos.flush();
            } catch (IOException ex) {
                Logger.getLogger(EliminarFicheiro.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                servidor.actualizaListaFicheiros();
            }

        }
    }
}
