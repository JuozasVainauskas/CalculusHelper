import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2345);
        Thread serverSocketHandling = new ServerSocketHandler(serverSocket);
        serverSocketHandling.start();
        while (true) {
            Socket clientConnection = null;
            try {
                clientConnection = serverSocket.accept();
                System.out.println(serverSocket);
                System.out.println("New Client:" + clientConnection);
                DataInputStream getInformation = new DataInputStream(clientConnection.getInputStream());
                DataOutputStream sendInformation = new DataOutputStream(clientConnection.getOutputStream());

                Thread specificClient = new MultiClientHandler(clientConnection, getInformation, sendInformation);
                specificClient.start();
            } catch (Exception e) {
                clientConnection.close();
                System.out.println(e);
            }
        }
    }
}

class ServerSocketHandler extends Thread {
    final ServerSocket serverSocket;

    public ServerSocketHandler(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        while (true) {
            try {
                Scanner reader = new Scanner(System.in);
                String text = reader.nextLine();
                if (text.equals("Off")) {
                    break;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        try {
            this.serverSocket.close();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Closing error: " + e);
        }
    }
}

class MultiClientHandler extends Thread {
    final DataInputStream clientMessage;
    final DataOutputStream serverMessage;
    final Socket clientConnection;

    public MultiClientHandler(Socket clientConnection, DataInputStream getInformation,
            DataOutputStream sendInformation) {
        this.clientConnection = clientConnection;
        this.clientMessage = getInformation;
        this.serverMessage = sendInformation;
    }

    public void run() {
        String equation;
        while (true) {
            try {
                serverMessage.writeUTF("Please write your equation:");
                equation = clientMessage.readUTF();
                if (equation.equals("Exit")) {
                    System.out.println("Closing connection");
                    this.clientConnection.close();
                    System.out.println("Connection with" + this.clientConnection + "is closed");
                    break;
                }
                equation = Calculator.calculate(equation);
                serverMessage.writeUTF(equation);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        try {
            this.clientMessage.close();
            this.serverMessage.close();
        } catch (IOException e) {
            System.out.println("Closing error: " + e);
        }
    }
}

class Calculator {
    public static String calculate(String equation) {
        String answer = "";
        StringTokenizer divideEquation = new StringTokenizer(equation);
        boolean isFirstComponent = true;

        while (divideEquation.hasMoreTokens()) {
            String component = divideEquation.nextToken();
            if (component.contains("x")) {
                int[] numberAndIndex = GettingDerivativeInformation.getNumber(component);
                int number = numberAndIndex[0];
                int i = numberAndIndex[1];

                answer = answer + differentiate(i, component, number, isFirstComponent);
                isFirstComponent = false;

            }
        }
        answer = "y'= " + answer;
        return answer;
    }

    public static String differentiate(int i, String component, int number, boolean isFirstComponent) {
        StringBuilder derivative = new StringBuilder("");
        if ((i < component.length()) && (component.charAt(i) == 'x')) {
            derivative = RegularDerivative.regular(i, component, derivative, number, isFirstComponent);
        } else if (component.substring(i, (i + 6)).equals("sin(x)")) {
            derivative = Trigonometry.sin(i, component, derivative, number, isFirstComponent);
        } else if (component.substring(i, (i + 6)).equals("cos(x)")) {
            derivative = Trigonometry.cos(i, component, derivative, number, isFirstComponent);
        }
        return derivative.toString();
    }
}

class GettingDerivativeInformation {
    public static int[] getNumber(String component) {
        int[] numberAndIndex = new int[2];

        int i = 0;
        Boolean isPositive = true;
        int number = 1;

        if (component.charAt(i) == '-') {
            isPositive = false;
            i++;
        } else if (component.charAt(i) == '+') {
            i++;
        }
        int numberStart = i;
        while ((i < component.length()) && ((component.charAt(i) >= '0') && (component.charAt(i) <= '9'))) {
            i++;
        }
        if (numberStart != i) {
            number = Integer.parseInt(component.substring(numberStart, i));
        }
        if (isPositive == false) {
            number = number * (-1);
        }
        numberAndIndex[0] = number;
        numberAndIndex[1] = i;
        return numberAndIndex;
    }

    public static int[] getPower(String component, int i) 
    {
        int[] powerAndIndex = new int[2];
        int powerNumberStart = i;
        int power;
        if ((i < component.length())&&(component.charAt(i) == '(')) 
        {
            powerNumberStart++;
            i++;
        }
            if ((i < component.length())&&(component.charAt(i) == '-'))
            {
                powerNumberStart++;
                i++;
                while ((i < component.length()) && ((component.charAt(i) >= '0') && (component.charAt(i) <= '9'))) 
                {
                    i++;
                }
                power = (-1) * Integer.parseInt(component.substring(powerNumberStart, i));
            } 
            else if ((i < component.length())&&(component.charAt(i) == '+'))
            {
                powerNumberStart++;
                i++;
                while ((i < component.length()) && ((component.charAt(i) >= '0') && (component.charAt(i) <= '9'))) {
                    i++;
                }
                power = Integer.parseInt(component.substring(powerNumberStart, i));
            } 
            else 
            {
                while ((i < component.length()) && ((component.charAt(i) >= '0') && (component.charAt(i) <= '9'))) 
                {
                    i++;
                }
                power = Integer.parseInt(component.substring(powerNumberStart, i));
            }
            if ((i < component.length())&&(component.charAt(i) == ')')) 
            {
                i++;
            }
        powerAndIndex[0] = power;
        powerAndIndex[1] = i;
        return powerAndIndex;
    }
}

class RegularDerivative {
    public static StringBuilder regular(int i, String component, StringBuilder derivative, int number,
            boolean isFirstComponent) {
        i++;
        if ((i < component.length()) && (component.charAt(i) == '^')) {
            i++;
            int[] powerAndIndex = GettingDerivativeInformation.getPower(component, i);
            int power = powerAndIndex[0];
            i = powerAndIndex[1];
            int newNumber = number * power;

            if (isFirstComponent == false) {
                if (newNumber > 0) 
                {
                    derivative.append("+" + number * power);
                } else if (newNumber < 0) 
                {
                    derivative.append(number * power);
                }
                
                if (power == 2) 
                {
                    derivative.append("x");
                } else if (power != 1) 
                {
                    if((power-1)>0)
                    {
                    derivative.append("x" + "^" + (power - 1));
                    }
                    else
                    {
                        derivative.append("x" + "^(" + (power - 1)+")");
                    }
                }
            } else if (isFirstComponent == true) {

                derivative.append(number * power);
                if (power == 2) {
                    derivative.append("x");
                } else if (power != 1) {
                    if((power-1)>0)
                    {
                    derivative.append("x" + "^" + (power - 1));
                    }
                    else
                    {
                        derivative.append("x" + "^(" + (power - 1)+")");
                    }
                }
            }
        } else if ((i >= component.length())) {
            if (number > 0) {
                if (isFirstComponent == false) {
                    derivative.append("+" + number);
                } else {
                    derivative.append(number);
                }
            } else if (number < 0) {
                derivative.append(number);
            }
        }
        isFirstComponent = false;
        derivative.append(" ");
        return derivative;
    }
}

class Trigonometry {
    public static StringBuilder sin(int i, String component, StringBuilder derivative, int number,
            boolean isFirstComponent) {
        i = i + 6;
        if ((i < component.length()) && (component.charAt(i) == '^')) {
            i++;
            int powerNumberStart = i;
            while ((i < component.length()) && ((component.charAt(i) >= '0') && (component.charAt(i) <= '9'))) {
                i++;
            }
            int power = Integer.parseInt(component.substring(powerNumberStart, i));
            int newNumber = number * power;
            if (isFirstComponent == false) {
                if (newNumber > 0) {
                    derivative.append("+" + number * power);
                } else if (newNumber < 0) {
                    derivative.append(number * power);
                }

                if (power == 2) {
                    derivative.append("cos(x)");
                } else if (power != 1) 
                {
                    derivative.append("cos(x)" + "^" + (power - 1));
                }
            } else if (isFirstComponent == true) {

                derivative.append(number * power);
                if (power == 2) {
                    derivative.append("cos(x)");
                } else if (power != 1) {
                    derivative.append("cos(x)" + "^" + (power - 1));
                }
            }
        } else if ((i >= component.length())) {
            if (number > 0) {
                if (isFirstComponent == false) {
                    if (number == 1) {
                        derivative.append("+" + "cos(x)");
                    } else {
                        derivative.append("+" + number + "cos(x)");
                    }
                } else {
                    if (number == 1) {
                        derivative.append("cos(x)");
                    } else {
                        derivative.append(number + "cos(x)");
                    }
                }
            } else if (number < 0) {
                derivative.append(number + "cos(x)");
            }
        }
        derivative.append(" ");
        isFirstComponent = false;
        return derivative;
    }

    public static StringBuilder cos(int i, String component, StringBuilder derivative, int number,
            boolean isFirstComponent) {
        i = i + 6;
        if ((i < component.length()) && (component.charAt(i) == '^')) {
            i++;
            int[] powerAndIndex = GettingDerivativeInformation.getPower(component, i);
            int power = powerAndIndex[0];
            i = powerAndIndex[1];
            int newNumber = number * power;
            if (isFirstComponent == false) {
                if (newNumber > 0) {
                    derivative.append("+" + number * power);
                } else if (newNumber < 0) {
                    derivative.append(number * power);
                }

                if (power == 2) {
                    derivative.append("-sin(x)");
                } else if (power != 1) {
                    if((power-1)>0)
                    {
                        derivative.append("-sin(x)" + "^" + (power - 1));
                    }
                    else
                    {
                        derivative.append("-sin(x)" + "^(" + (power - 1)+")");
                    }
                }
            } else if (isFirstComponent == true) {

                derivative.append(number * power);
                if (power == 2) {
                    derivative.append("-sin(x)");
                } else if (power != 1) {
                    if((power-1)>0)
                    {
                        derivative.append("-sin(x)" + "^" + (power - 1));
                    }
                    else
                    {
                        derivative.append("-sin(x)" + "^(" + (power - 1)+")");
                    }
                }
            }
        } else if ((i >= component.length())) {
            if (number > 0) {
                if (isFirstComponent == false) {
                    if (number == 1) {
                        derivative.append("+" + "-sin(x)");
                    } else {
                        derivative.append("+" + number + "-sin(x)");
                    }
                } else {
                    if (number == 1) {
                        derivative.append("-sin(x)");
                    } else {
                        derivative.append(number + "-sin(x)");
                    }
                }
            } else if (number < 0) {
                derivative.append(number + "-sin(x)");
            }
        }
        derivative.append(" ");
        isFirstComponent = false;
        return derivative;
    }
}