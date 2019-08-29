import java.io.IOException;
import java.net.ConnectException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //todo grab settings from args, else to the below
        while (true) {
            Scanner in = new Scanner(System.in);
            System.out.print("Server to connect to? (IP:Port): ");
            String IPPort = in.nextLine();
            try {
                Client client = new Client(IPPort.split(":")[0], Integer.valueOf(IPPort.split(":")[1]));
                break;
            } catch (ArrayIndexOutOfBoundsException ignored) {
                System.out.println("Invalid address.");
            } catch (ConnectException ignored) {
                System.out.println("Connection refused by server.");
            } catch (IOException e) {
                e.printStackTrace();//todo deal with exceptions in a user-friendly way
            }
        }
    }
}
