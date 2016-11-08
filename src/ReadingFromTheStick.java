
import org.json.simple.JSONArray;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.*;
import java.util.concurrent.TimeoutException;

public class ReadingFromTheStick {

    private static volatile boolean userReady = false;           //user is ready -
    private static boolean firstReport = false;
//    private static ArrayList<String> result = new ArrayList<>();

    private static JSONArray result = new JSONArray();

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("Reading from the stick - ver 8.1");
        RabbitMQClient.getInstance();
        while (true) {
            while (userReady) {
                System.out.println(" ----- START ----- ");
                if (firstReport) {
                    JsonObject commandToJson_0 = Json.createObjectBuilder().add("action","start").add("data","null").build();
                    sendMessage(commandToJson_0);
                    firstReport = false;
                }
//                File first = new File("/home/java_dev/Изображения");            //фотки у меня на компе
//                File first = new File("/media/java_dev");            //мой комп - флешка
                File first = new File("/media/zaharov");           //Комп Сереги
//                File second = new File("/run/user/1000/gvfs");         //Android
                if (!first.exists()) {
                    System.out.println(first + " - папка не существует!");
                } else if (first.listFiles().length>0)    {         //first.listFiles().length>0
                    userReady = false;
                    JsonObject commandToJson_1 = Json.createObjectBuilder().add("action", "device").add("data", "usb").build();
                    sendMessage(commandToJson_1);
                    getMeList(first);
                    if (result.size()>0)    {
//                        String resultWork = result.get(0);
//                        for (int ii = 1; ii < result.size(); ii++) {
//                            resultWork = resultWork.concat(result.get(ii));
//                        }
//                        JsonObject commandToJson = Json.createObjectBuilder().add("list_of_links",resultWork).build();
//                        sendMessage(commandToJson);

                        JsonObject commandToJson_2 = Json.createObjectBuilder().add("action","list_of_links").add("data",result.toJSONString()).build();
                        sendMessage(commandToJson_2);

                        JsonObject commandToJson_3 = Json.createObjectBuilder().add("action","stop").add("data","successfully").build();
                        sendMessage(commandToJson_3);
                        result.clear();
                    }   else    {
                        JsonObject commandToJson_4 = Json.createObjectBuilder().add("action","stop").add("data","photo_not_found").build();
                        sendMessage(commandToJson_4);
                        result.clear();
                        System.out.println("НЕТ ФОТО");
                    }
                }
                /*
                if (!second.exists()) {
                    System.out.println(second + " - папка не существует!");         //Когда не подкл андроид пишет что папка не существует...
                } else if (second.listFiles().length>0)    {
                    userReady = false;
                    JsonObject commandToJson_4 = Json.createObjectBuilder().add("the_work_was_started","android").build();
                    sendMessage(commandToJson_4);
                    getMeList(second);
                    if (result.size()>0)    {
                        String resultWork = result.get(0);
                        for (int ii = 1; ii < result.size(); ii++) {
                            resultWork = resultWork.concat(result.get(ii));
                        }
                        JsonObject commandToJson = Json.createObjectBuilder().add("list_of_links",resultWork).build();
                        sendMessage(commandToJson);
                        JsonObject commandToJson_1 = Json.createObjectBuilder().add("the_work_was_completed","successfully").build();
                        sendMessage(commandToJson_1);
                        result.clear();
                    }   else    {
                        JsonObject commandToJson_2 = Json.createObjectBuilder().add("the_work_was_completed","photo_not_found").build();
                        sendMessage(commandToJson_2);
                        result.clear();
                    }
                }
                */
                try {                                       //для разгрузки ЦП и паузой между обращениями
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Command thread sleep - not OK!");
                }
                System.out.println(" ----- STOP ----- ");
            }
            try {                                       //для разгрузки ЦП и паузой между обращениями
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                System.out.println("Command thread sleep - not OK!");
            }
        }
    }

    private static void getMeList(File f) {
//        System.out.println ("Call from path: "+f.getPath());
        try {
            for (File file : f.listFiles()) {
                if (file.isDirectory()&&!file.getName().contains(".")) {
                    getMeList(file);
                } else {            //.JPG .jpeg .JPEG  .png  .PNG
                    if (getFileExtension(file).equals("JPG")||getFileExtension(file).equals("JPEG")||getFileExtension(file).equals("PNG")) {
//                        System.out.println("File: " + file);
                        result.add(file.toString());
                    }
                }
            }
        } catch (Exception ex)  {
            System.out.println("----- Exception -----");
            ex.printStackTrace();
        }
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        // если в имени файла есть точка и она не является первым символом в названии файла
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".")+1).toUpperCase();
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        else return "";
    }

    static void receivingMessage(JsonObject command) throws IOException, TimeoutException {
        if (command.getString("action").equals("start")) {
            userReady = true;
            firstReport = true;
        }
    }

    private static void sendMessage(JsonObject commandToJson)   {
        try {
            RabbitMQClient.getInstance().sendMessage(commandToJson.toString());
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        try {                                       //для разгрузки ЦП и паузой между обращениями
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("sendMessage - command thread sleep - not OK!");
        }
        System.out.println("pause");
    }
}

