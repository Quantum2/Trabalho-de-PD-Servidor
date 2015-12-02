/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class EnviaActualizacaoOURespostaClienteTCP extends Thread {   //se recebe um boolean do tipo falso, comunica com o secundario e manda actualizacao.
    //se for verdadeiro, comunica com um cliente e responde.

    private Socket Socket = null;
    boolean running = false;
    public String Diretoria = null;
    public ObjectInputStream recvobject = null;
    public static final int MAX_SIZE = 256;

    public EnviaActualizacaoOURespostaClienteTCP(Socket sockettcp, String diretoria) {
        this.Socket = sockettcp;
        running = true;
        this.Diretoria = diretoria;
    }

    @Override
    public void run() {
        try {
            InputStream iss = Socket.getInputStream();
            recvobject = new ObjectInputStream(iss);
            Object msgrecebida = recvobject.readObject();
            iss.close();
            if (msgrecebida instanceof Boolean) {
                if ((Boolean) msgrecebida == false) {
                    File folder = new File(Diretoria);
                    File[] ListadosFicheiros = folder.listFiles();
                    OutputStream ou = Socket.getOutputStream();
                    InputStream in = null;
                    for (int i = 0; i < ListadosFicheiros.length; i++) {
                        byte[] bytes = new byte[MAX_SIZE];
                        in = new FileInputStream(ListadosFicheiros[i]);
                        int tamanho;
                        while ((tamanho = in.read(bytes)) > 0) {
                            ou.write(bytes, 0, tamanho);
                        }
                    }
                    ou.close();
                    in.close();
                } else {
                    if (msgrecebida instanceof Pedido) //nao esquecer mudar para socketcomcliente
                    {
                        Pedido c = (Pedido) msgrecebida;
                        if (c.tipoPedido == Pedido.DOWNLOAD) {
                            File folder = new File(Diretoria);
                            File[] ListadosFicheiros = folder.listFiles();
                            OutputStream ou = Socket.getOutputStream();
                            InputStream in = null;
                            for (int i = 0; i < ListadosFicheiros.length; i++) {
                                byte[] bytes = new byte[MAX_SIZE];
                                in = new FileInputStream(ListadosFicheiros[i]);
                                int tamanho;
                                while ((tamanho = in.read(bytes)) > 0) {
                                    ou.write(bytes, 0, tamanho);
                                }
                            }
                            ou.close();
                            in.close();
                        }
                        if (c.tipoPedido == Pedido.UPLOAD) {
                            int tamanho;
                            byte[] bytes = new byte[MAX_SIZE];
                            InputStream in = null;
                            OutputStream ou = null;
                            in = Socket.getInputStream();
                            ou = new FileOutputStream(Diretoria);
                            while ((tamanho = in.read(bytes)) > 0) {
                                ou.write(bytes, 0, tamanho);
                            }
                            ou.close();
                            in.close();
                        }
                        if (c.tipoPedido == Pedido.ELIMINAR)
                        {
                            int tamanho;
                            File folder = new File(Diretoria);
                            File[] ListadosFicheiros = folder.listFiles();
                            ArrayList<String> NomesFicheiros=new ArrayList<String>();
                            for(int i=0;i<ListadosFicheiros.length;i++)
                            {
                                NomesFicheiros.add(ListadosFicheiros[i].getName());
                            }
                            OutputStream oss = Socket.getOutputStream();
                            ObjectOutputStream sendcliente = new ObjectOutputStream(oss);
                            sendcliente.writeObject(NomesFicheiros);
                            sendcliente.close();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(EnviaActualizacaoOURespostaClienteTCP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EnviaActualizacaoOURespostaClienteTCP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
