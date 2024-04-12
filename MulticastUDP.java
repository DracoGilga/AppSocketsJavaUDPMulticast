import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class MulticastUDP {

    static String nombre;
    static volatile boolean terminado = false;

    public static void main(String[] args) {

        int puerto = 8080;

        InetAddress grupo = null;
        MulticastSocket socket = null;
        Scanner scanner = null;

        try {

            grupo = InetAddress.getByName("224.0.0.5");

            socket = new MulticastSocket(puerto);

            scanner = new Scanner(System.in);
            System.out.println("Ingrese su nombre: ");
            nombre = scanner.nextLine();

            socket.joinGroup(grupo);

            Thread hilo = new Thread(new HiloLetura(socket));
            hilo.start();

            System.out.println("Puede comenzar a escribir en el grupo");

            byte[] buffer = new byte[1024];
            String linea;

            while (!terminado) {
                linea = scanner.nextLine();
                if (linea.equalsIgnoreCase("Adios")) {
                    terminado = true;

                    socket.leaveGroup(grupo);
                    break;
                }

                String mensaje = nombre + ": " + linea;
                buffer = mensaje.getBytes();
                DatagramPacket mensajeSalida = new DatagramPacket(buffer, buffer.length, grupo, puerto);
                socket.send(mensajeSalida);
            }
        } catch (IOException e) {
            System.out.println("Error de E/S: " + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.leaveGroup(grupo);
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar el socket: " + e.getMessage());
                }
            }
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}

class HiloLetura implements Runnable {
    private MulticastSocket socket;
    private static final int BUFFER_SIZE = 1024;

    HiloLetura(MulticastSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];

        while (!MulticastUDP.terminado) {
            try {
                DatagramPacket mensajeEntrada = new DatagramPacket(buffer, BUFFER_SIZE);
                socket.receive(mensajeEntrada);

                String linea = new String(buffer, 0, mensajeEntrada.getLength());

                System.out.println(linea);
            } catch (IOException e) {
                System.out.println("Error al recibir mensaje: " + e.getMessage());
            }
        }
        System.out.println("El hilo de lectura ha terminado.");
    }
}
