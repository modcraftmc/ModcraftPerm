package fr.goldor.ModcraftPerm.utils;

import com.google.gson.*;

import fr.goldor.ModcraftPerm.ModcraftPerm;

import java.io.*;
import java.util.ArrayList;

public class JsonManager {

    public static  boolean RemoveJsonObject(JsonObject jsonObject, String filePath, String fileName) {
        return true;
    }

    public static ArrayList<JsonArray> ReadJsonArrays(String filePath, String fileName) {

        JsonElement  jsonContentReadied = JsonRead(filePath,fileName);

        ArrayList<JsonArray> readJsonArrays = new ArrayList<>();

        if(jsonContentReadied != null) {

            if(jsonContentReadied.isJsonArray()) {

                for (JsonElement jsonElement : jsonContentReadied.getAsJsonArray()) {

                    if (jsonElement.isJsonArray()) {
                        readJsonArrays.add(jsonElement.getAsJsonArray());
                    }
                    else {

                    }

                }

            }
            else {
                ModcraftPerm.GetLogger().warn(filePath+"/"+fileName+".json is empty or unreadable !");
            }


            return readJsonArrays;
        }
        else {

            ModcraftPerm.GetLogger().warn("Error during the parse");


            return null;
        }

    }

    public static ArrayList<JsonObject> ReadJsonObjects(String filePath, String fileName) {

        JsonElement jsonContentReadied = JsonRead(filePath,fileName);

        ArrayList<JsonObject> readJsonObjects = new ArrayList<>();

        if(jsonContentReadied != null) {

            if(jsonContentReadied.isJsonArray()) {

                for (JsonElement jsonElement : jsonContentReadied.getAsJsonArray()) {

                    if (jsonElement.isJsonObject()) {
                        readJsonObjects.add(jsonElement.getAsJsonObject());
                    }
                    else {

                    }

                }

            }
            else {
                ModcraftPerm.GetLogger().warn(filePath+"/"+fileName+".json is empty or unreadable !");
            }


            return readJsonObjects;
        }
        else {

            ModcraftPerm.GetLogger().warn("Error during the parse");


            return null;
        }



    }

    public static boolean WriteJsonArray(JsonArray jsonArray, String filePath, String fileName) {

        if(!FileExist(filePath,fileName)){

            File file = new File(filePath+"/"+fileName+".json");

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        JsonElement jsonContentReadied = JsonRead(filePath,fileName);

        if(jsonContentReadied != null) {

            if(jsonContentReadied.isJsonArray()) {

                if (!jsonContentReadied.getAsJsonArray().contains(jsonArray)) {
                    jsonContentReadied.getAsJsonArray().add(jsonArray);
                }

            }
            else {

                jsonContentReadied = new JsonArray();
                jsonContentReadied.getAsJsonArray().add(jsonArray);

            }

            Gson json = new GsonBuilder().setPrettyPrinting().create();

            Write(filePath, fileName, json.toJson(jsonContentReadied));


            return true;
        }
        else {

            ModcraftPerm.GetLogger().warn("Error during the parse");


            return false;
        }
    }

    public static boolean WriteJsonObject(JsonObject jsonObject, String filePath, String fileName) {

        if(!FileExist(filePath,fileName)){

            File file = new File(filePath+"/"+fileName+".json");

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        JsonElement jsonContentReadied = JsonRead(filePath,fileName);

        if(jsonContentReadied != null) {

            if(jsonContentReadied.isJsonArray()){

                if (!jsonContentReadied.getAsJsonArray().contains(jsonObject)) {
                    jsonContentReadied.getAsJsonArray().add(jsonObject);
                }

            }
            else{

                jsonContentReadied = new JsonArray();
                jsonContentReadied.getAsJsonArray().add(jsonObject);

            }

            Gson json = new GsonBuilder().setPrettyPrinting().create();

            Write(filePath, fileName, json.toJson(jsonContentReadied));


            return true;
        }
        else {

            ModcraftPerm.GetLogger().warn("Error during the parse");


            return false;
        }
    }

    private static JsonElement JsonRead(String filePath, String fileName) {

            FileReader fileReader = Read(filePath,fileName);

            if(fileReader != null){
                JsonParser jsonParser = new JsonParser();


                try{
                    JsonElement returnValue = jsonParser.parse(fileReader);
                    fileReader.close();


                    return returnValue;
                }catch (Exception e){
                    ModcraftPerm.GetLogger().error(filePath+"/"+fileName+".json can't be parse !");
                    e.printStackTrace();

                    try {
                        fileReader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }


                    return null;
                }
            }
            else {
                ModcraftPerm.GetLogger().error(filePath+"/"+fileName+".json can't be parse !");
                ModcraftPerm.GetLogger().warn("because of the error a functionality may not run, a further error will describe the problem");

                return null;
            }
    }

    private static void Write(String filePath, String fileName, String content) {

        File file = new File(filePath+"/"+fileName+".json");

        if(FileExist(filePath,fileName)) {

            try {
                FileWriter writer = new FileWriter(file);

                writer.write(content);

                writer.flush();
                writer.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                FileWriter writer = new FileWriter(file);

                writer.write(content);

                writer.flush();
                writer.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public static boolean FileExist(String filePath, String fileName) {

        File test = new File(filePath+"/"+fileName+".json");

        if(test.exists()){


            return true;
        }
        else {


            return false;
        }

    }

    private static FileReader Read(String filePath, String fileName) {

        File test = new File(filePath+"/"+fileName+".json");

        if(FileExist(filePath,fileName)){

            FileReader reader = null;

            try {
                reader = new FileReader(test);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            return reader;
        }
        else {


            return null;
        }

    }

}