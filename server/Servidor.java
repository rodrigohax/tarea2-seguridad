
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.cert.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rodrigo
 */
public class Servidor {

    private static int PUERTO = 1337;
    private static String keyStore = "llaveServer.jks";
    private static String trustStore = "truststore_server.jks";
    private static char[] keyPassword = "asd123".toCharArray();

    public static SSLServerSocket crearServerSocket(int puerto) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyStore), keyPassword);

        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(trustStore), keyPassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); //SunX509
        kmf.init(ks, keyPassword); //usar solo una password para todas las llaves

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        /*
            inicialiamos con el contexto de la truststore y keystore
         */
        SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
        sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        ServerSocketFactory factory = sslcontext.getServerSocketFactory();
        SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(PUERTO);
        server.setEnabledCipherSuites(server.getSupportedCipherSuites());
        return server;
    }

    public static void main(String args[]) {
        BufferedReader entrada;
        PrintWriter salida;
        SSLServerSocket server;

        try {
            server = crearServerSocket(PUERTO);
            server.setNeedClientAuth(false);
            ArrayList<String> ips = new ArrayList<>();
            boolean stop = false;
            while (!stop) {
                System.out.println("Esperando una conexión...");
                SSLSocket client = (SSLSocket) server.accept();
                SSLSession session = client.getSession();
                Certificate[] cchain2 = session.getLocalCertificates();
                System.out.println("Un cliente se ha conectado...");
                System.out.println("Se presento al cliente con la siguiente identificacion");
                for (int i = 0; i < cchain2.length; i++) {
                    System.out.println(((X509Certificate) cchain2[i]).getSubjectDN());
                }
                System.out.println("Un cliente se ha conectado...");
                /*
                    Guardamos IP
                 */
                String ip = client.getInetAddress().getHostAddress();
                ips.add(ip);
                System.out.println("IP del cliente: " + ip);
                System.out.println("Algoritmo de criptografia: " + session.getCipherSuite());
                System.out.println("Version SSL: " + session.getProtocol());

                // Para los canales de entrada y salida de datos 
                entrada = new BufferedReader(new InputStreamReader(client.getInputStream()));
                salida = new PrintWriter(client.getOutputStream(), true);

                String str = entrada.readLine();
                System.out.println("Confirmando recepcion de mensaje del cliente:" + str);
                salida.println(str.toUpperCase());
                /* 
                     Hora y fecha
                 */
                Date date = new Date();
                salida.println(date);
                client.close();
            }//while
            // Cerrando la conexión
            server.close();

        } catch (Exception e) {
            System.out.println("Excepcion." + e.getMessage());
            e.printStackTrace();
        }

    }//main
}
