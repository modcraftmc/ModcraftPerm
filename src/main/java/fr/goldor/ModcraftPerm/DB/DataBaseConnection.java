package fr.goldor.ModcraftPerm.DB;

import fr.goldor.ModcraftPerm.ModcraftPerm;

import java.sql.*;

public class DataBaseConnection {
    Connection con;
    public boolean isConnected;
    public Statement statement;

    public DataBaseConnection(){
        isConnected = false;
        con = null;
        statement = null;
}

    public void Connect(String URL,String User,String PassWord){

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + URL, User, PassWord);
            isConnected = true;
            statement = con.createStatement();
            ModcraftPerm.GetLogger().info("Successfully connected to the DataBase !");

        } catch (SQLException | ClassNotFoundException e) {
            ModcraftPerm.GetLogger().warn("Cannot connect to the DataBase !");
            e.printStackTrace();
        }

    }

    public ResultSet QuerySQLCommand(String sql) {

        if(isConnected){

            try {
                return statement.executeQuery(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                ModcraftPerm.LoadConfig();
            }

        }
        else{
            ModcraftPerm.GetLogger().info("No sql connection a further error may appear.");
        }

        return null;
    }

    public void ExecuteSQLCommand(String sql) {

        if(isConnected){

            try {
                statement.execute(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                ModcraftPerm.LoadConfig();
            }

        }
        else{
            ModcraftPerm.GetLogger().info("No sql connection a further error may appear.");
        }
    }

    public PreparedStatement PrepareStatement(String sql) {

        if(isConnected){

            try {
                return con.prepareStatement(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                ModcraftPerm.LoadConfig();
            }

        }
        else{
            ModcraftPerm.GetLogger().info("No sql connection a further error may appear.");
        }

        return null;
    }

    public Connection GetSQLConnection(){

        if(isConnected){
            return con;
        }
        else{
            ModcraftPerm.GetLogger().info("No sql connection a further error may appear.");
        }

        return null;
    }

    public boolean isConnected(){
        return isConnected;
    }

}