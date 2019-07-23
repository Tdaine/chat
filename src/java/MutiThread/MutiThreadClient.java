package MutiThread;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class FromClient implements Runnable{
    private Socket client;

    public FromClient(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        Scanner in = null;
        try {
            in = new Scanner(client.getInputStream());
            //in.useDelimiter("\n");
            while(true){

                if(in.hasNext())
                    System.out.println("从服务器发来的信息：" + in.nextLine());
                if(client.isClosed()){
                    System.out.println("此客户端关闭");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            in.close();
        }
    }
}

class ToClient implements Runnable{
    private Socket client;

    public ToClient(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        PrintStream out = null;
        Scanner scanner = null;
        try {
            out = new PrintStream(client.getOutputStream(),true,"UTF-8");
            while(true){
                System.out.println("请输入要发送的消息");
                scanner = new Scanner(System.in);
                //scanner按照\n作为分隔符
                //scanner.useDelimiter("\n");
                String strToServer;
                if(scanner.hasNext()){
                    strToServer = scanner.nextLine();
                    out.println(strToServer);
                    if(strToServer.contains("byebye")){
                        System.out.println("关闭客户端");
                        break;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.close();
            scanner.close();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

public class MutiThreadClient {
    public static void main(String[] args) {
        try {
            Socket client = new Socket("127.0.0.1",6666);
            Thread thread1 = new Thread(new FromClient(client));
            Thread thread2 = new Thread(new ToClient(client));
            thread1.start();
            thread2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

