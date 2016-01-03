/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class RecebeFicheiro extends Thread {

    Servidor servidor = null;
    Socket socketPedido = null;
    Pedido pedido=null;

    public RecebeFicheiro(Servidor servidor, Socket socketPedido,Pedido pedido) {
        this.servidor = servidor;
        this.socketPedido = socketPedido;
        try {
            socketPedido.setSoTimeout(2000);
        } catch (SocketException ex) {
            Logger.getLogger(RecebeFicheiro.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.pedido=pedido;
    }

    @Override
    public void run() {
        FileOutputStream fileOut = null;
        boolean flg=true;
        try {
            if (servidor.isPrimario()) {
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
                
                if (flg) {
                    File folder = new File(servidor.getDiretoria() + "\\" + pedido.getNomeFicheiro());
                    try {
                        int nbytes;
                        byte[] filechunck = new byte[Servidor.MAX_SIZE];
                        InputStream inputFicheiro = socketPedido.getInputStream();
                        if (!folder.exists()) {
                            folder.createNewFile();
                        }
                        String localFilePath = servidor.diretoria + "\\" + pedido.getNomeFicheiro();
                        fileOut = new FileOutputStream(localFilePath);
                        while ((nbytes = inputFicheiro.read(filechunck)) > 0) {
                            fileOut.write(filechunck, 0, nbytes);
                        }
                        fileOut.flush();
                        fileOut.close();
                    } catch (SocketTimeoutException ex) {
                        System.out.println("[SERVIDOR] Timeout RecebeFicheiro");
                    }
                }
                
                for(int i=0;i<servidor.getSocketSecundarios().size();i++){
                    ObjectOutputStream oos=new ObjectOutputStream(servidor.getSocketSecundarios().get(i).getOutputStream());
                    oos.writeBoolean(flg);
                    oos.flush();
                }          
            }else{
                ObjectOutputStream oos =new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                pedido.setSocketCliente(socketPedido);
                oos.writeObject(pedido);
                oos.flush();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EnviaFicheiro.class.getName()).log(Level.SEVERE, null, ex);
        }catch(SocketTimeoutException ex){
            System.out.println("[SERVIDOR] Timeout RecebeFicheiro");
        }catch (IOException ex) {
            System.out.println("[SERVIDOR] A receber ficheiros");
        }finally{
                servidor.actualizaListaFicheiros();
            }
    }
}
