package database;

import java.util.List;

/**
 * Interface generic untuk semua DAO.
 * Mendefinisikan operasi CRUD standar.
 */
public interface IDAO<T> {

    List<T> getAll();

    T getById(int id);

    boolean insert(T entity);

    boolean update(T entity);

    boolean delete(int id);
}
