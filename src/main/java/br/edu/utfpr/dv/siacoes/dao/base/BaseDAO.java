package br.edu.utfpr.dv.siacoes.dao.base;

import br.edu.utfpr.dv.siacoes.dao.ConnectionDAO;
import br.edu.utfpr.dv.siacoes.log.UpdateEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    private final Connection conn;

    public BaseDAO() throws SQLException {
        this.conn = ConnectionDAO.getInstance().getConnection();
    }

    public T findById(int id) throws SQLException {
        String query = this.findByIdQuery();

        try(
            PreparedStatement stmt = this.conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            stmt.setInt(1, id);

            if(rs.next()){
                return this.loadObject(rs);
            }else{
                return null;
            }
        }
    }

    protected List<T> list(String query) throws SQLException {
        try(
            Statement stmt = this.conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)
        ) {
            List<T> list = new ArrayList<T>();

            while(rs.next()){
                list.add(this.loadObject(rs));
            }

            return list;
        }
    }

    private int insert(int idUser, T object) throws SQLException {
        String query = this.insertQuery();

        try (
            PreparedStatement stmt = this.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            PreparedStatement newStmt = this.insertStatementStep(stmt, object);
            newStmt.execute();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return this.insertResultSetStep(idUser, conn, rs, object);
            }
        }
    }

    private int insert(T object) throws SQLException {
        String query = this.insertQuery();

        try (
            PreparedStatement stmt = this.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            PreparedStatement newStmt = this.insertStatementStep(stmt, object);
            newStmt.execute();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return this.insertResultSetStep(conn, rs, object);
            }
        }
    }

    public int save(int idUser, T object) throws SQLException {
        return this.getId(object) == 0
                ? this.insert(idUser, object)
                : this.update(idUser, object);
    }

    public int save(T object) throws SQLException {
        return this.getId(object) == 0
                ? this.insert(object)
                : this.update(object);
    }

    private int update(int idUser, T object) throws SQLException {
        String query = this.updateQuery();

        try (
            PreparedStatement stmt = this.conn.prepareStatement(query)
        ) {
            PreparedStatement newStmt = this.updateStatementStep(stmt, object);
            newStmt.execute();
            new UpdateEvent(conn).registerUpdate(idUser, object);
            return this.getId(object);
        }
    }

    private int update(T object) throws SQLException {
        String query = this.updateQuery();

        try (
            PreparedStatement stmt = this.conn.prepareStatement(query)
        ) {
            PreparedStatement newStmt = this.updateStatementStep(stmt, object);
            newStmt.execute();
            return this.getId(object);
        }
    }

    protected abstract String findByIdQuery();

    protected abstract String insertQuery();

    protected abstract String updateQuery();

    protected abstract int getId(T object);

    protected abstract PreparedStatement insertStatementStep(PreparedStatement stmt, T object) throws SQLException;

    protected abstract int insertResultSetStep(int idUser, Connection conn, ResultSet rs, T object) throws SQLException;

    protected abstract int insertResultSetStep(Connection conn, ResultSet rs, T object) throws SQLException;

    protected abstract PreparedStatement updateStatementStep(PreparedStatement stmt, T object) throws SQLException;

    protected abstract T loadObject(ResultSet rs) throws SQLException;
}
