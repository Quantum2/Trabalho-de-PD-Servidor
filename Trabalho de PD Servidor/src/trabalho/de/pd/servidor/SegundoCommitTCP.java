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
import java.net.Socket;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class SegundoCommitTCP extends Thread {

    public static final int MAX_SIZE = 256;
    public boolean flg, running;

    Servidor servidor = null;

    InputStream inputStreamFicheiro = null;

    public SegundoCommitTCP(Servidor servidor) throws IOException {
        this.servidor = servidor;
        inputStreamFicheiro = servidor.primarioSocketTCP.getInputStream();
        running = true;
    }

    public void actualizaListaFicheiros() {
        File folder = new File(servidor.getDiretoria() + "\\ServerFicheiros");
        File[] arrayFicheiros = folder.listFiles();
        servidor.setListaFicheiros(new ListaFicheiros());
        for (File ficheiro : arrayFicheiros) {
            Ficheiro ficheiroTemp = new Ficheiro(ficheiro.getName(), ficheiro.length());
            servidor.getListaFicheiros().addFicheiro(ficheiroTemp);
        }
    }

    public void termina() {
        running = false;
    }

    @Override
    public void run() {
        do {
            if (!servidor.isPrimario()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(servidor.getPrimarioSocketTCP().getInputStream());
                    Object msg = ois.readObject();
                    if (msg instanceof Pedido) {
                        ObjectOutputStream oos = new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                        Pedido pedido = (Pedido) msg;
                        File file = new File(servidor.getDiretoria() + pedido.getNomeFicheiro());
                        if (pedido.getTipoPedido() == Pedido.ELIMINAR) {
                            oos.writeBoolean(file.canRead());
                            oos.flush();
                        }
                        if (pedido.getTipoPedido() == Pedido.UPLOAD) {
                            oos.writeBoolean(!file.exists());
                            oos.flush();
                        }
                        Boolean confirma;
                        confirma = ois.readBoolean();
                        if (confirma == true) {
                            if (pedido.getTipoPedido() == Pedido.ELIMINAR) {
                                file.delete();
                            }
                            if (pedido.getTipoPedido() == Pedido.UPLOAD) {
                                Pedido p = new Pedido(file.getName(), 1, false);
                                oos.writeObject(p);
                                oos.flush();

                                int nbytes;
                                byte[] filechunck = new byte[MAX_SIZE];
                                FileOutputStream fOut = new FileOutputStream(servidor.diretoria);
                                while ((nbytes = inputStreamFicheiro.read(filechunck)) > 0) {
                                    fOut.write(filechunck, 0, nbytes);
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SegundoCommitTCP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SegundoCommitTCP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } while (running);
    }
}
