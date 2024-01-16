import java.util.Scanner;
public class Cajero {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double saldo = 1000.0;

        int opcion;
        do {
            System.out.println("1- Retirar fondos");
            System.out.println("2- Ingresar fondos");
            System.out.println("0- Salir");
            System.out.print("Seleccione una opción: ");
            opcion = scanner.nextInt();

            switch (opcion) {
                case 1:
                    System.out.println("Fondos retirados")
                    break;
                case 2:
                    System.out.println("Fondos ingresados")
                break;
            }
        } while (opcion != 0);
    }
}
