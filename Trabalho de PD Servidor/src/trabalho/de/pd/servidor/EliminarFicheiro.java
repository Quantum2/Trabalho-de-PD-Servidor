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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class EliminarFicheiro extends Thread {

    Servidor servidor = null;
    Socket socketPedido = null;
    Pedido pedido=null;

    public EliminarFicheiro(Servidor servidor, Pedido pedido) {
        this.servidor = servidor;
        this.pedido = pedido;
    }

    @Override
    public void run() {
        boolean flg=true;
        if (servidor.isPrimario()) {
            try{
                for(int i=0;i<servidor.getSocketSecundarios().size();i++){
                    ObjectOutputStream oos=new ObjectOutputStream(servidor.getSocketSecundarios().get(i).getOutputStream());
                    oos.writeObject(pedido);
                    oos.flush();
                }
                for(int i=0;i<servidor.getSocketSecundarios().size();i++){
                    ObjectInputStream iss=new ObjectInputStream(servidor.getSocketSecundarios().get(i).getInputStream());
                    if(!iss.readBoolean()){
                        flg=false;
                        break;
                    }
                }
                for(int i=0;i<servidor.getSocketSecundarios().size();i++){
                    ObjectOutputStream oos=new ObjectOutputStream(servidor.getSocketSecundarios().get(i).getOutputStream());
                    oos.writeBoolean(flg);
                    oos.flush();
                }
            
            if(flg){
                String dir = (servidor.getDiretoria());
                File file = new File(dir);
                file.delete();
            }
            }catch (IOException ex) {
                Logger.getLogger(EliminarFicheiro.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                servidor.actualizaListaFicheiros();
            }
        }else{
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                oos.writeObject(pedido);
                oos.flush();
            } catch (IOException ex) {
                Logger.getLogger(EliminarFicheiro.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                servidor.actualizaListaFicheiros();
            }
            
        }
    }
}
