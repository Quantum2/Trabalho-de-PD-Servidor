/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class RecebeActualizacaoTCP extends Thread {

    public static final int MAX_SIZE=256;
    public boolean flg;
    
    Servidor servidor = null;
    
    InputStream inputStreamFicheiro = null;

    public RecebeActualizacaoTCP(Servidor servidor) throws IOException {
        this.servidor = servidor;
        inputStreamFicheiro = servidor.primarioSocketTCP.getInputStream();
    }

    public void actualizaListaFicheiros(){
        File folder = new File(servidor.getDiretoria());                        
        File[] arrayFicheiros = folder.listFiles();
        servidor.setListaFicheiros(new ListaFicheiros());
        for(File ficheiro : arrayFicheiros){
            Ficheiro ficheiroTemp=new Ficheiro(ficheiro.getName(),ficheiro.length());
            servidor.getListaFicheiros().addFicheiro(ficheiroTemp);
        }
    }
    
    @Override
    public void run() {//tem que se ver as melhores formas de mandar e receber ficheiros
        try {
            ObjectInputStream ois=new ObjectInputStream(servidor.getPrimarioSocketTCP().getInputStream());
            ListaFicheiros primarioListaFicheiros=(ListaFicheiros)ois.readObject();
            
            for(int i=0;i<primarioListaFicheiros.getArrayListFicheiro().size();i++)
            {
                flg=false;
                for(int j=0;j<servidor.getListaFicheiros().getSize();j++){
                    if(primarioListaFicheiros.getArrayListFicheiro().get(i).getNome().equals(servidor.getListaFicheiros().getArrayListFicheiro().get(j).getNome())){
                        flg=true;
                    }
                }
                ObjectOutputStream pedeFicheiro=new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                if(flg){
                    Pedido p=new Pedido(primarioListaFicheiros.getArrayListFicheiro().get(i).getNome(),1);
                    pedeFicheiro.writeObject(p);
                    
                    int nbytes;
                    byte [] filechunck = new byte [MAX_SIZE];
                    FileOutputStream fOut = new FileOutputStream(servidor.diretoria);
                    while((nbytes=inputStreamFicheiro.read(filechunck))>0) {
                        fOut.write(filechunck,0,nbytes);
                    }
                }
            }  
            actualizaListaFicheiros();
        } catch (IOException ex) {
            Logger.getLogger(RecebeActualizacaoTCP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RecebeActualizacaoTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
