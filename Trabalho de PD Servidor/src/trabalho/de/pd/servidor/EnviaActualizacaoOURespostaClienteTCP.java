/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho.de.pd.servidor;

import java.net.Socket;

/**
 *
 * @author ASUS
 */
public class EnviaActualizacaoOURespostaClienteTCP extends Thread{   //se recebe um boolean do tipo falso, comunica com o secundario e manda actualizacao.
                                                    //se for verdadeiro, comunica com um cliente e responde.
    private Socket EnviaSocketTCP=null;
    
    public EnviaActualizacaoOURespostaClienteTCP()
    {
        
    }
    
    @Override
    public void run() {
        if (Primario == true) {
            SocketComSecundario = socketTCP.accept();
            InputStream iss = SocketComSecundario.getInputStream();
            reciveobject = new ObjectInputStream(iss);
            Object msgrecebida = reciveobject.readObject();
            if (msgrecebida instanceof Boolean) {
                if ((Boolean) msgrecebida == false) {
                    File folder = new File(diretoria);
                    File[] ListadosFicheiros = folder.listFiles();
                    OutputStream ou = SocketComSecundario.getOutputStream();
                    InputStream in = null;
                    for (int i = 0; i < ListadosFicheiros.length; i++) {
                        long length = ListadosFicheiros[i].length();
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
            } else {
                if (msgrecebida instanceof Cliente) //nao esquecer mudar para socketcomcliente
                {
                    Cliente c = (Cliente) msgrecebida;
                    if (c.download == true) {
                        File folder = new File(diretoria);
                        File[] ListadosFicheiros = folder.listFiles();
                        OutputStream ou = SocketComSecundario.getOutputStream();
                        InputStream in = null;
                        for (int i = 0; i < ListadosFicheiros.length; i++) {
                            long length = ListadosFicheiros[i].length();
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
                    if (c.Upload == true) {
                        int tamanho;
                        byte[] bytes = new byte[MAX_SIZE];
                        InputStream in = null;
                        OutputStream ou = null;
                        in = SocketComSecundario.getInputStream();
                        ou = new FileOutputStream(diretoria);
                        while ((tamanho = in.read(bytes)) > 0) {
                            ou.write(bytes, 0, tamanho);
                        }
                        ou.close();
                        in.close();
                    }
                    if (c.Ver == true) //acho que estou a mandar todos os ficheiros num object e nao o nome deles-boa ideia para os ocdigos anteriores se funcionar
                    {
                        int tamanho;
                        File folder = new File(diretoria);
                        File[] ListadosFicheiros = folder.listFiles();
                        OutputStream oss = SocketComSecundario.getOutputStream();
                        ObjectOutputStream sendcliente = new ObjectOutputStream(oss);
                        sendcliente.writeObject(ListadosFicheiros);
                        sendcliente.close();
                    }
                }
            }
        }

    }
}
