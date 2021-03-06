package ogr.user12043.talkOnLan.dao;

import java.util.List;

/**
 * Created by user12043 on 9.05.2020 - 09:43
 * part of project: talk-onLan
 */
public interface Dao<T, P> {
    List<T> find();

    T findById(P p);

    void save(T t);

    void delete(T t);

    void deleteById(P p);
}
