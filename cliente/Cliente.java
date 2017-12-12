
import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Rodrigo
 */
public class Cliente {

    private static String HOST = "localhost";
    private static int PUERTO = 1337;
    private static String keyStore = "llaveCliente.jks";
    private static String trustStore = "truststore_cliente.jks";
    private static char[] pass = "asd123".toCharArray();

    public static void main(String args[]) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {
        SSLSocket socket;
        PrintWriter salida;
        BufferedReader entrada, teclado;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keyStore), pass);

            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(new FileInputStream(trustStore), pass);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); //SunX509
            kmf.init(ks, pass); //usar solo una password para todas las llaves  

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            //Creamos nuestro socket con SSL
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLSocketFactory factory = context.getSocketFactory();
            socket = (SSLSocket) factory.createSocket(HOST, PUERTO);
            socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

            System.out.println("IP del servidor: " + socket.getSession().getPeerHost());
            System.out.println("Algoritmo de criptografia: " + socket.getSession().getCipherSuite());
            System.out.println("Version SSL: " + socket.getSession().getProtocol());
            teclado = new BufferedReader(new InputStreamReader(System.in));
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Ingrese palabra para enviar al servidor");

            //Leyendo palabra desde teclado
            String palabra = teclado.readLine();
            //Enviamos palabra
            salida.println(palabra);
            //leemos el retorno de la palabra desde el socket 
            String eco = entrada.readLine();
            System.out.println("Respuesta desde el servidor: " + eco);
            eco = entrada.readLine();
            System.out.println(eco);
            //Cerramos la conexión
            socket.close();
            System.out.println("Conexion terminada");
        } catch (UnknownHostException e) {
            System.out.println("El host no existe o no está activo. " + e.getLocalizedMessage());
        } catch (IOException e) {
            System.out.println("Error de entrada/salida. " + e.getLocalizedMessage());
        }

    }
}
