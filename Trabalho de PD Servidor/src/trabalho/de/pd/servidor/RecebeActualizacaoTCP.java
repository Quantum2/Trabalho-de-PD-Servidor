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
public class RecebeActualizacaoTCP extends Thread {

    public static final int MAX_SIZE = 256;
    public boolean flg, running;

    Servidor servidor = null;

    InputStream inputStreamFicheiro = null;

    public RecebeActualizacaoTCP(Servidor servidor) throws IOException {
        this.servidor = servidor;
        inputStreamFicheiro = servidor.primarioSocketTCP.getInputStream();
        running = true;
    }

    public void actualizaListaFicheiros() {
        File folder = new File(servidor.getDiretoria() + "\\ServerFicheiros");
        File[] arrayFicheiros = folder.listFiles();
        servidor.setListaFicheiros(new ListaFicheiros());
        for (File ficheiro : arrayFicheiros) {
            Ficheiro ficheiroTemp = new Ficheiro(ficheiro.getName(), ficheiro.length(),0);
            servidor.getListaFicheiros().addFicheiro(ficheiroTemp);
        }
    }

    public void termina() {
        running = false;
    }

    @Override
    public void run() {
        if (!servidor.isPrimario()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(servidor.getPrimarioSocketTCP().getInputStream());
                Object msg = ois.readObject();

                if (msg instanceof ListaFicheiros) {
                    ListaFicheiros primarioListaFicheiros = (ListaFicheiros) msg;

                    for (int i = 0; i < primarioListaFicheiros.getArrayListFicheiro().size(); i++) {
                        flg = true;
                        for (int j = 0; j < servidor.getListaFicheiros().getSize(); j++) {
                            if (primarioListaFicheiros.getArrayListFicheiro().get(i).getNome().equals(servidor.getListaFicheiros().getArrayListFicheiro().get(j).getNome())) {
                                flg = false;
                            }
                        }
                        ObjectOutputStream pedeFicheiro = new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                        if (flg) {
                            Pedido p = new Pedido(primarioListaFicheiros.getArrayListFicheiro().get(i).getNome(), Pedido.DOWNLOAD,false);
                            pedeFicheiro.writeObject(p);

                            int nbytes;
                            byte[] filechunck = new byte[MAX_SIZE];
                            FileOutputStream fOut = new FileOutputStream(servidor.diretoria);
                            while ((nbytes = inputStreamFicheiro.read(filechunck)) > 0) {
                                fOut.write(filechunck, 0, nbytes);
                            }
                        }
                    }
                    actualizaListaFicheiros();
                } else {
                    if (msg instanceof Ficheiro) {
                        ObjectOutputStream oos = new ObjectOutputStream(servidor.getPrimarioSocketTCP().getOutputStream());
                        Ficheiro ficheiro = (Ficheiro) msg;
                        File file = new File(servidor.getDiretoria() + "\\ServerFicheiros\\" + ficheiro.getNome());
                        if (ficheiro.pedido == Ficheiro.ELIMINAR) {
                            oos.writeBoolean(file.canRead());
                            oos.flush();
                        }
                        if (ficheiro.pedido == Ficheiro.UPLOAD) {
                            oos.writeBoolean(!file.exists());
                            oos.flush();
                        }
                        Boolean confirma;
                        confirma = ois.readBoolean();
                        if (confirma == true) {
                            if (ficheiro.pedido == Ficheiro.ELIMINAR) {
                                file.delete();
                            }
                            if (ficheiro.pedido == Ficheiro.UPLOAD) {
                                Pedido p = new Pedido(file.getName(), 1,false);
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
                }
            } catch (IOException ex) {
                Logger.getLogger(RecebeActualizacaoTCP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebeActualizacaoTCP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
