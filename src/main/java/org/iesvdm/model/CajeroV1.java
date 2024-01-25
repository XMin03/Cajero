package org.iesvdm.model;

import java.sql.*;
import java.util.Scanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class CajeroV1 {

    private static final String URL = "jdbc:mysql://localhost:3306/cuentas_bancarias_nueva?serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "123456";

    public static void main(String[] args) {
        System.setProperty("jdbc.drivers", "com.mysql.cj.jdbc.Driver");
        Scanner scanner = new Scanner(System.in);
        int opcion;
        String numCuenta;

        try (Connection connection = DriverManager.getConnection(URL, USUARIO, CONTRASENA)) {
            do {
                System.out.println("1- Retirar fondos");
                System.out.println("2- Ingresar fondos");
                System.out.println("3- Consulta de movimientos");
                System.out.println("4- listar cuentas de un cliente");
                System.out.println("5- informacion de un cliente");

                System.out.println("0- Salir");
                System.out.print("Seleccione una opción: ");
                opcion = scanner.nextInt();

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese el número de cuenta: ");
                        scanner.nextLine(); // Limpiar el buffer
                        numCuenta = scanner.nextLine();
                        retirarFondos(connection, numCuenta);
                        break;

                    case 2:
                        System.out.print("Ingrese el numero de cuenta: ");
                        scanner.nextLine(); // Limpiar el buffer
                        numCuenta = scanner.nextLine();
                        ingresarFondos(connection, numCuenta);
                        break;

                    case 3:
                        System.out.print("Ingrese el número de cuenta: ");
                        scanner.nextLine(); // Limpiar el buffer
                        numCuenta = scanner.nextLine();
                        consultarMovimientos(connection, numCuenta);
                        break;

                    case 4:
                        System.out.print("Ingrese el DNI del cliente: ");
                        scanner.nextLine(); // Limpiar el buffer
                        String dniCliente = scanner.nextLine();
                        listarCuentasCliente(connection, dniCliente);
                        break;
                    case 5:
                        System.out.print("Ingrese el DNI del cliente: ");
                        scanner.nextLine(); // Limpiar el buffer
                        dniCliente = scanner.nextLine();
                        consultarInformacion(connection, dniCliente);
                        break;

                    case 0:
                        System.out.println("Saliendo del programa...");
                        break;

                    default:
                        System.out.println("Opción no válida.");
                }
            } while (opcion != 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void retirarFondos(Connection connection, String numCuenta) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese la cantidad a retirar: ");
        double cantidadRetirar = scanner.nextDouble();

        // Verificar si hay suficiente saldo para la retirada
        if (verificarSaldoSuficiente(connection, numCuenta, cantidadRetirar)) {
            // Realizar la retirada y actualizar la base de datos
            realizarRetirada(connection, numCuenta, cantidadRetirar);
            System.out.println("Retiro exitoso. Saldo actualizado.");
        } else {
            System.out.println("Operación no válida. Saldo insuficiente.");
        }
    }

    private static void ingresarFondos(Connection connection, String numCuenta) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese la cantidad a ingresar: ");
        double cantidadIngresar = scanner.nextDouble();

        // Realizar el ingreso y actualizar la base de datos
        realizarIngreso(connection, numCuenta, cantidadIngresar);
        System.out.println("Ingreso exitoso. Saldo actualizado.");
    }

    private static boolean verificarSaldoSuficiente(Connection connection, String numCuenta, double cantidad) throws SQLException {
        String query = "SELECT saldo_actual FROM cuentas WHERE num_cuenta = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, numCuenta);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    double saldoActual = resultSet.getDouble("saldo_actual");
                    return cantidad <= saldoActual;
                }
            }
        }
        return false;
    }

    private static void realizarRetirada(Connection connection, String numCuenta, double cantidad) throws SQLException {
        String insertMovimientoQuery = "INSERT INTO movimientos (num_cuenta, fecha, importe) VALUES (?, NOW(), ?)";
        String updateSaldoQuery = "UPDATE cuentas SET saldo_actual = saldo_actual - ? WHERE num_cuenta = ?";

        try (PreparedStatement insertMovimientoStatement = connection.prepareStatement(insertMovimientoQuery);
             PreparedStatement updateSaldoStatement = connection.prepareStatement(updateSaldoQuery)) {

            // Insertar movimiento de retirada
            insertMovimientoStatement.setString(1, numCuenta);
            insertMovimientoStatement.setDouble(2, -cantidad);
            insertMovimientoStatement.executeUpdate();

            // Actualizar saldo en la cuenta
            updateSaldoStatement.setDouble(1, cantidad);
            updateSaldoStatement.setString(2, numCuenta);
            updateSaldoStatement.executeUpdate();
        }
    }

    private static void realizarIngreso(Connection connection, String numCuenta, double cantidad) throws SQLException {
        String insertMovimientoQuery = "INSERT INTO movimientos (num_cuenta, fecha, importe) VALUES (?, NOW(), ?)";
        String updateSaldoQuery = "UPDATE cuentas SET saldo_actual = saldo_actual + ? WHERE num_cuenta = ?";

        try (PreparedStatement insertMovimientoStatement = connection.prepareStatement(insertMovimientoQuery);
             PreparedStatement updateSaldoStatement = connection.prepareStatement(updateSaldoQuery)) {

            // Insertar movimiento de ingreso
            insertMovimientoStatement.setString(1, numCuenta);
            insertMovimientoStatement.setDouble(2, cantidad);
            insertMovimientoStatement.executeUpdate();

            // Actualizar saldo en la cuenta
            updateSaldoStatement.setDouble(1, cantidad);
            updateSaldoStatement.setString(2, numCuenta);
            updateSaldoStatement.executeUpdate();
        }
    }


    private static void consultarMovimientos(Connection connection, String numCuenta) throws SQLException {
        String query = "SELECT fecha, importe FROM movimientos WHERE num_cuenta = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, numCuenta);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("Movimientos de la cuenta " + numCuenta + ":");
                while (resultSet.next()) {
                    Date fecha = resultSet.getTimestamp("fecha");
                    double importe = resultSet.getDouble("importe");
                    System.out.println("Fecha: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fecha)
                            + ", Importe: " + importe);
                }
            }
        }
    }

    private static void listarCuentasCliente(Connection connection, String dniCliente) throws SQLException {
        String query = "SELECT num_cuenta FROM cuentas WHERE dni = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dniCliente);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("Cuentas del cliente con DNI " + dniCliente + ":");
                while (resultSet.next()) {
                    String numCuenta = resultSet.getString("num_cuenta");
                    System.out.println("Número de cuenta: " + numCuenta);
                }
            }
        }
    }
    private static void consultarInformacion(Connection connection, String dni) throws SQLException {
        String query = "SELECT * FROM clientes natural join cuentas WHERE dni = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dni);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("Cliente con dni " + dni + ":");
                while (resultSet.next()) {
                    String apellidos = resultSet.getString("apellidos");
                    String nombre = resultSet.getString("nombre");
                    String num_cuenta = resultSet.getString("num_cuenta");
                    double saldo_apertura = resultSet.getDouble("saldo_apertura");
                    double saldo_actual = resultSet.getDouble("saldo_actual");

                    System.out.println("Apellidos: "+apellidos);
                    System.out.println("Nombre: "+nombre);
                    System.out.println("Num_cuenta: "+num_cuenta);
                    System.out.println("Saldo_apertura: "+saldo_apertura);
                    System.out.println("Saldo_actual : "+saldo_actual);
                }
            }
        }
    }
}


