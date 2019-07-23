package MutiThread;


import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MutiThreadServer {
    private static Map<String,Socket> clientMap = new ConcurrentHashMap<>();
    private static class ExecureClient implements Runnable{
        private Socket client;

        public ExecureClient(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                Scanner in = new Scanner(client.getInputStream());
                String strFromclient;
                while (true) {
                    if (in.hasNext()) {
                        strFromclient = in.nextLine();
                        //windows下默认换行/r/n中的/r替换为字符串
                        //给定正则表达式a*b ，输入"aabfooaabfooabfoob"和替换字符串"-" ，在该表达式的匹配器上调用此方法将产生字符串"-foo-foo-foo-" 。
                        //将给定的正则表达式编译为模式。
                        //参数
                        //regex - 要编译的表达式
                        //结果
                        //给定的正则表达式编译成一个模式
                        Pattern pattern = Pattern.compile("\r");
                        //创建一个匹配器，匹配给定的输入与此模式。
                        //结果
                        //这种模式的新匹配器
                        Matcher matcher = pattern.matcher(strFromclient);

                        //将与模式匹配的输入序列的每个子序列替换为给定的替换字符串。
                        strFromclient = matcher.replaceAll("");
                        //注册流程
                        if(strFromclient.startsWith("userName")){
                            String strName = strFromclient.split(":")[1];
                            registerUser(strName,client);
                            continue;
                        }
                        //群聊流程
                        if(strFromclient.startsWith("G")){
                            String fromUserName = null;
                            for(String keyName : clientMap.keySet()){
                                if(clientMap.get(keyName).equals(client))
                                    fromUserName = keyName;
                            }
                            String strMsg = strFromclient.split(":")[1];
                            groupChat(fromUserName,strMsg);
                            continue;
                        }
                        //私聊
                        if(strFromclient.startsWith("p")){
                            String fromUserName = null;
                            for(String keyName : clientMap.keySet()){
                                if(clientMap.get(keyName).equals(client))
                                    fromUserName = keyName;
                            }
                            String userMsg = strFromclient.split(":")[1];
                            String strMsg = userMsg.split("-")[1];
                            String toUserName = userMsg.split("-")[0];
                            privateChat(fromUserName,toUserName,strMsg);
                            continue;
                        }
                        //用户退出
                        if(strFromclient.contains("byebye")){
                            String userName = null;
                            for(String keyName : clientMap.keySet()){
                                if(clientMap.get(keyName).equals(client))
                                    userName = keyName;
                            }
                            String msg = "用户" + userName + "下线了";
                            clientMap.remove(userName);
                            serviceInform(msg);
                            continue;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //注册方法
        private void registerUser(String userName,Socket client){
            String userOnline = "用户" + userName+"已上线";
            clientMap.put(userName,client);
            String groupNumber = "当前群聊人数为" + clientMap.size() + "人";
            serviceInform(userOnline);
            serviceInform(groupNumber);
            try {
                //输出使用PrintStream，支持println()方法
                //out - 要打印值和对象的输出流
                //autoFlush - 一个布尔值 如果为真，每当写入一个字节数组时，输出缓冲区将被刷新，其中一个println方法被调用，或换行字符（ '\n' ）被写入
                //encoding - 支持的名称 character encoding
                PrintStream out = new PrintStream(client.getOutputStream(),true,"UTF-8");
                //告知用户注册成功
                out.println("用户注册成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //群聊
        private void groupChat(String fromUserName,String msg){
            //Collection进行ConcurrentHashMap遍历,取出所有的Client进行发送消息
            Collection<Socket> clientSet = clientMap.values();
            for(Socket l:clientSet){
                PrintStream out = null;
                try {
                    out = new PrintStream(l.getOutputStream(),true,"UTF-8");
                    out.println("群聊信息为:" + fromUserName + ":"+ msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        //服务器通知流程
        private void serviceInform(String msg){
            //Collection进行ConcurrentHashMap遍历,取出所有的Client进行发送消息
            Collection<Socket> clientSet = clientMap.values();
            for(Socket l:clientSet){
                PrintStream out = null;
                try {
                    out = new PrintStream(l.getOutputStream(),true,"UTF-8");
                    out.println("通知通知:"+ msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        //私聊流程
        private void privateChat(String fromUserName,String userName,String msg) {
            Socket privateSocket = clientMap.get(userName);
            PrintStream out = null;
            if(privateSocket == null) {
                try {
                    out = new PrintStream(client.getOutputStream(),
                            true, "UTF-8");
                    out.println("没有这个联系人");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                out = new PrintStream(privateSocket.getOutputStream(),
                        true,"UTF-8");
                out.println(fromUserName + "给你的私信：" + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        ServerSocket serverSocket = new ServerSocket(6666);
        for(int i = 0; i < 20; i++){
            System.out.println("等待客户端链接...");
            Socket client = serverSocket.accept();
            System.out.println("有新的客户端链接，端口号为：" + client.getPort());
            executorService.submit(new ExecureClient(client));
        }
        executorService.shutdown();
        serverSocket.close();
    }
}
