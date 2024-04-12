
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class MulticastUDP {
    static String nombre;
    static volatile boolean terminado = false;

    @SuppressWarnings("deprecation")
    public static void main(String args[]){
        try{
            //establecemos un puerto acordado para multicast
            int puerto = 8080;
            //creamos un grupo multicast
            InetAddress grupo = InetAddress.getByName("224.0.0.0");
            //creamos un socket multicast
            MulticastSocket socket = new MulticastSocket(puerto);

            //solicitamos el nombre del cliente
            Scanner scanner= new Scanner(System.in);
            System.out.println("Ingrese su nombre: ");
            nombre = scanner.nextLine();

            //se une al grupo
            socket.joinGroup(grupo);

            Thread hilo = new Thread(new HiloLetura(socket, grupo, puerto));
            hilo.start();

            System.out.println("Puede comenzar a escribir en el grupo");

            //esperamos la respuesta no mayor a 1024 bytes
            byte[] bufer = new byte[1024];
            String linea;

            //se queda a la espera de mensajes al grupo
            // hasta recibir "Adios" por parte de algun nuevo miembro
            while (true) {
                linea = scanner.nextLine();
                if (linea.equalsIgnoreCase("Adios")){
                    terminado = true;

                    linea = nombre + ": ha terminidado la conexion";
                    bufer = linea.getBytes();
                    DatagramPacket mensajeSalida = new DatagramPacket(bufer, bufer.length, grupo, puerto);
                    socket.leaveGroup(grupo);
                    break;
                }
            }

            scanner.close();
            socket.close();

        }catch(SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }catch(IOException e){
            System.out.println("IO: " + e.getMessage());
        }
    }
}

class HiloLetura implements Runnable{
    private MulticastSocket socket;
    private InetAddress grupo;
    private int port;

    HiloLetura(MulticastSocket socket, InetAddress grupo, int port){
        this.socket = socket;
        this.grupo = grupo;
        this.port = port;
    }

    @Override
    public void run(){
        byte[] bufer = new byte[1024];
        String linea;

        while (!MulticastUDP.terminado){
            try{
                DatagramPacket mensajeEntrada = new DatagramPacket(bufer, bufer.length, grupo, port);
                socket.receive(mensajeEntrada);
                linea = new String(bufer, 0, mensajeEntrada.getLength());
                if (linea.startsWith(MulticastUDP.nombre)) 
                    System.out.println(linea);
            }catch(IOException e){
                System.out.println("Comunicacion y socket cerrados.");
            }
        }
    }
}
