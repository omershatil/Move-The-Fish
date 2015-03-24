package omer.fish.dao;

import java.util.List;

import org.hibernate.Session;

public interface BaseDao {
    public Session getCurrentSession();

    /* Support basic CRUD operations for all DAO classes. */
	public <T> T save(T object);
	public <T> List<T> save(List<T> objects);
	public <T> T merge(T object);
	public <T> void delete(T object);
	public <T> void delete(long id);
	public <T> boolean exists(Class<T> c, long id);
	public <T> T find(Class<T> c, long id);
	public <T> List<T> findAll(Class<T> c);
	public void flush();
}
