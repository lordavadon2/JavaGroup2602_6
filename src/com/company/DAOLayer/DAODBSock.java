package com.company.DAOLayer;

import com.company.ModelLayer.ISock;
import com.company.ModelLayer.SockData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brainacad4 on 10.05.2018.
 */
public class DAODBSock implements IDAOSock {

    private static String url = "jdbc:mysql://{serverAddr}:{portAddr}/sockdb";
    private static String selectSockByIdQuery = "Select id_socks,color_socks,size_socks,type_sock_type \n" +
            "FROM sockdb.socks \n" +
            "left join sockdb.sock_types ON sock_types.id_sock_types = socks.type_socks \n";

    private static String insertSockQuery = "Insert into sockdb.socks \n" +
            "(color_socks,size_socks,type_socks) \n" +
            "values (?,?,?)";

    private static String selectIdByTypeName = "Select id_sock_types from sockdb.sock_types where type_sock_type = ? limit 1";

    private static String insertSockType = "Insert into sockdb.sock_types (type_sock_type) values (?)";


    private static Connection con;
    PreparedStatement pstm;


    public DAODBSock(String serverAddres, int port, String user,String password) throws SQLException {
        url = url.replace("{serverAddr}",serverAddres);
        url = url.replace("{portAddr}",((Integer)port).toString());
        con = DriverManager.getConnection(url, user, password);
    }
    private int getTypeId(String type)
    {
        ResultSet rs = null;
        int result = -1;
        try {
            pstm = con.prepareStatement(selectIdByTypeName);
            pstm.setString(1,type);
            rs = pstm.executeQuery();
            if (rs.next())
            {
                result = rs.getInt(1);
            }
        }
        catch (SQLException ex)
        {
            System.out.println("getTypeId(String type) error." + ex.getMessage());
        }
        finally {
            closeStatment(pstm);
            closeResult(rs);
            return  result;
        }
    }

    private int getTypeIdOrAdd(String type)
    {
        int result = getTypeId(type);
        if ( result == -1)
        {
            try
            {
                pstm = con.prepareStatement(insertSockType);
                pstm.setString(1,type);
                pstm.executeUpdate();
                result = getTypeId(type);
            }
            catch (SQLException ex)
            {
                System.out.println("getTypeIdOrAdd(String type) error" + ex.getMessage());
            }
            finally {
                closeStatment(pstm);
            }

        }
        return result;
    }
    @Override
    public int addSock(ISock sock) {
        int typeId = getTypeIdOrAdd(sock.getType());
        try {
            pstm = con.prepareStatement(insertSockQuery);
            pstm.setString(1,sock.getColor());
            pstm.setInt(2,sock.getSize());
            pstm.setInt(3,typeId);
            return pstm.executeUpdate();
        }
        catch (SQLException ex)
        {
            System.out.println(" addSock(ISock sock) error" + ex.getMessage());
        }
        finally {
            closeStatment(pstm);
        }
        return -1;
    }

    @Override
    public int[] addSock(ISock[] sock) {
        return new int[0];
    }

    @Override
    public List<ISock> getSockCollection() {
        List<ISock> result = new ArrayList<>();
        ResultSet rs = null;
        try {
            pstm = con.prepareStatement(selectSockByIdQuery);
            rs = pstm.executeQuery();
            while (rs.next())
            {
                result.add(convertFromResultSet(rs));
            }
        }
        catch (SQLException ex)
        {
            System.out.println("getSockCollection() error." + ex.getMessage());
        }
        finally {
            closeResult(rs);
            closeStatment(pstm);
            return result;
        }

    }

    private ISock convertFromResultSet(ResultSet rs) throws SQLException
    {
        int result_id = rs.getInt(1);
        String colorSock = rs.getString(2);
        int sizeSock = rs.getInt(3);
        String typeSock = rs.getString(4);
        return new SockData(typeSock, colorSock, sizeSock, result_id);
    }

    private void closeStatment(PreparedStatement ps)
    {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void closeResult(ResultSet rs)
    {
        if (rs != null)
        {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ISock readSock(int id) {
        ISock result = null;
        ResultSet rs = null;
        String queryWithCondition = selectSockByIdQuery + "WHERE id_socks = ?";
        try {
            pstm = con.prepareStatement(queryWithCondition);
            pstm.setInt(1,id);
            rs = pstm.executeQuery();
            if(rs.next())
            {
              result = convertFromResultSet(rs);
            }
        }
        catch (SQLException ex)
        {
            System.out.println("ReadSock(int id) error." + ex.getMessage());
        }
        finally {
            closeStatment(pstm);
            closeResult(rs);
            return  result;
        }
    }

    @Override
    public boolean updateSock(ISock changedSock) {
        return false;
    }

    @Override
    public boolean deleteSock(int id) {
        return false;
    }

    @Override
    public boolean deleteSock(int[] id) {
        return false;
    }
}
